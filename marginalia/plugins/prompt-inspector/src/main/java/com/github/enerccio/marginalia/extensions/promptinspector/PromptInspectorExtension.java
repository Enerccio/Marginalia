package com.github.enerccio.marginalia.extensions.promptinspector;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuKeys;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.notification.Notification;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.promptinspector.PromptInspectorLocalization.*;

/**
 * Shows the prompt of a generation to the user before it is sent to the model, when the user switched that on in the
 * settings menu of the story. The dialog is opened on the page that started the generation with {@code GenerationControllerEvent.accessUI}, and the
 * generation waits for the answer (see the plugin development page of the developer manual).
 */
@Configurable
public class PromptInspectorExtension implements MarginaliaExtension {

    /** In the attributes of the user settings: a boolean, whether the prompt is shown before it is sent. */
    static final String KEY = "com.github.enerccio.marginalia.extensions.promptinspector.enabled";

    private static final String MENU_ITEM_KEY = "promptinspector.toggle";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private User currentUser;

    private Registration inferenceRegistration;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.tools(MENU_ITEM_KEY, l -> T.localized(INSPECT_PROMPT_BEFORE_SENDING), story -> toggle())
                        .icon(Solid.MAGNIFYING_GLASS::create)
                        .checkedWhen(story -> isEnabled())
                        .path(StoryMenuKeys.GROUP_ANALYSIS));
        // the last event before the request: the payload is what the model gets, after every extension changed it
        inferenceRegistration = storyGenerationService.addEventListener(Events.BEFORE_INFERENCE, this::inspect);
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        menuRegistry.unregisterAll(bundle.getSymbolicName());
        if (inferenceRegistration != null) {
            inferenceRegistration.unregister();
            inferenceRegistration = null;
        }
    }

    /**
     * Whether the user wants to see the prompts. Never writes: it is also asked on the thread of a generation.
     */
    private boolean isEnabled() {
        try {
            UserSetting setting = settingService.find(UserSetting.class, currentUser);
            JsonObject attributes = setting == null ? null : setting.getAttributes();
            return attributes != null && attributes.has(KEY) && attributes.get(KEY).getAsBoolean();
        } catch (Exception e) {
            log.warn("Failed to read the setting: {}", e.getMessage(), e);
            return false;
        }
    }

    private void toggle() {
        try {
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            JsonObject attributes = setting.getAttributes();
            if (attributes == null) {
                attributes = new JsonObject();
                setting.setAttributes(attributes);
            }
            boolean enabled = attributes.has(KEY) && attributes.get(KEY).getAsBoolean();
            attributes.addProperty(KEY, !enabled);
            settingService.save(setting);
        } catch (Exception e) {
            log.error("Failed to save the setting: {}", e.getMessage(), e);
            Notification.show(T.localized(FAILED_TO_SAVE_THE_SETTING).formatted(e.getMessage()));
        }
    }

    /**
     * Listeners are global, the generation of every user comes here: only the ones of users who switched it on are stopped to ask.
     */
    private void inspect(GenerationControllerEvent event, EventChain chain) {
        boolean asked = false;
        try {
            if (event.getPayload() != null && isEnabled()) {
                asked = ask(event, chain, format(event.getPayload()));
            }
        } catch (Exception e) {
            // generate without asking rather than fail the generation
            log.warn("Failed to show the prompt: {}", e.getMessage(), e);
        } finally {
            if (!asked) {
                chain.next();
            }
        }
    }

    /**
     * Shows the prompt in a dialog of the page that started the generation and goes on when the user answered: Send sends the
     * request, Cancel stops the generation before it. The generation is also released when it is stopped some other way (from
     * another window) and when the page goes away with the dialog open, which must not leave it waiting.
     *
     * @return whether the dialog is out, so that the chain goes on by the answer; false when nobody can be shown it (an agent or a
     * command started the generation, or the page is gone), then the generation goes on unasked
     */
    private boolean ask(GenerationControllerEvent event, EventChain chain, String prompt) {
        return GenerationQuestion.ask(event, chain, log, (send, cancel) -> new PromptInspectorDialog(prompt, send, cancel));
    }

    private static String format(List<LLMChatMessage> payload) {
        StringBuilder text = new StringBuilder();
        for (LLMChatMessage message : payload) {
            if (!text.isEmpty()) {
                text.append("\n\n");
            }
            text.append('[').append(message.getRole()).append("]\n");
            if (message.getContent() != null) {
                text.append(message.getContent().strip());
            }
            if (!message.getAttachments().isEmpty()) {
                text.append('\n').append(T.localized(ATTACHMENTS).formatted(message.getAttachments().size()));
            }
        }
        return text.toString();
    }
}
