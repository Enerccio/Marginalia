package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;
import com.github.enerccio.marginalia.domain.service.manual.HelpPrompts;
import com.github.enerccio.marginalia.domain.service.manual.HelpTools;
import com.github.enerccio.marginalia.domain.service.manual.HelpToolsImpl;
import com.github.enerccio.marginalia.domain.service.manual.InteractiveHelpTools;
import com.github.enerccio.marginalia.domain.service.manual.InteractiveHelpToolsImpl;
import com.github.enerccio.marginalia.domain.service.manual.UiStateSource;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HelpAssistantServiceImpl implements HelpAssistantService {

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private UserService userService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ManualService manualService;

    @Autowired
    private User currentUser;

    /** What a request uses: the AI of the settings, and whether the user's limits apply. */
    private record Target(AI ai, boolean metered) {}

    @Override
    @CommonTxReadOnly
    public Settings getSettings() throws Exception {
        adminGuard.requireAdmin();
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        return new Settings(app.getHelpEnabled(), app.getHelpAiUuid(), app.getHelpApplyLimits(),
                app.getHelpPromptAdmin(), app.getHelpPromptUser(), app.getHelpInteractions());
    }

    @Override
    @CommonTx
    public void saveSettings(Settings settings) throws Exception {
        adminGuard.requireAdmin();
        String aiUuid = StringUtils.trimToNull(settings.aiUuid());
        if (aiUuid != null && !isOwnedByCurrentUser(load(aiService, aiUuid))) {
            throw new IllegalArgumentException("The help AI has to be your own AI");
        }
        if (settings.enabled() && aiUuid == null) {
            throw new IllegalArgumentException("The help assistant needs an AI");
        }
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        app.setHelpEnabled(settings.enabled());
        app.setHelpAiUuid(aiUuid);
        app.setHelpApplyLimits(settings.applyLimits());
        app.setHelpInteractions(settings.interactions());
        app.setHelpPromptAdmin(promptOrNull(settings.adminPrompt(), HelpPrompts.ADMIN));
        app.setHelpPromptUser(promptOrNull(settings.userPrompt(), HelpPrompts.USER));
        settingService.save(app);
    }

    @Override
    @NoTx
    public boolean isAvailable() throws Exception {
        return target().isPresent();
    }

    @Override
    @NoTx
    public Info getInfo() throws Exception {
        return target().map(t -> new Info(t.ai().getName(),
                t.ai() instanceof OpenAICompatible openAi ? openAi.getModelName() : null, t.metered())).orElse(null);
    }

    @Override
    @NoTx
    public boolean isMetered() throws Exception {
        return target().map(Target::metered).orElse(false);
    }

    @Override
    @NoTx
    public CancellationToken stream(List<LLMChatMessage> payload, FunctionSet functions, InferenceAsyncCallback callback)
            throws Exception {
        Target target = target().orElseThrow(() -> new InferenceException(Type.NOT_AVAILABLE, null, null, null));
        InferenceService service = inferenceServices.forAIOrThrow(target.ai());
        if (target.metered()) {
            return service.stream(payload, null, functions, callback);
        }
        try (UnmeteredScope _ = UnmeteredScope.open()) {
            return service.stream(payload, null, functions, callback);
        }
    }

    @Override
    @NoTx
    public CancellationToken ask(List<LLMChatMessage> history, UiStateSource uiState, InferenceAsyncCallback callback)
            throws Exception {
        // only with a screen to move through: a request that does not come from a page has nothing to interact with
        boolean interactive = uiState != null && settingService.getOrCreateApp(AppSettings.class).getHelpInteractions();
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, instructions() + (interactive ? HelpPrompts.INTERACTIVE : "")));
        payload.addAll(history);
        FunctionSet tools = interactive
                ? inferenceServices.getFunctionRegistry().bind(InteractiveHelpTools.class,
                new InteractiveHelpToolsImpl(manualService, uiState))
                : inferenceServices.getFunctionRegistry().bind(HelpTools.class, new HelpToolsImpl(manualService, uiState));
        return stream(payload, tools, callback);
    }

    /** A blank prompt or one that is the default is not stored, so that the default can change with a new version. */
    private static String promptOrNull(String prompt, String defaultPrompt) {
        return StringUtils.isBlank(prompt) || prompt.strip().equals(defaultPrompt.strip()) ? null : prompt;
    }

    /**
     * The prompt for the current user (administrators have their own, an administrator can change both) with the
     * index of the manual in place of {@code {{manualIndex}}}.
     */
    private String instructions() throws Exception {
        boolean administrator = adminGuard.isAdmin();
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        String stored = administrator ? app.getHelpPromptAdmin() : app.getHelpPromptUser();
        String prompt = StringUtils.isBlank(stored) ? HelpPrompts.defaultPrompt(administrator) : stored;
        return prompt.replace(HelpPrompts.MANUAL_INDEX, manualIndex());
    }

    private String manualIndex() {
        StringBuilder sb = new StringBuilder();
        for (ManualPage page : manualService.listPages()) {
            String description = page.description().length() > 110 ? page.description().substring(0, 110) + "..." : page.description();
            sb.append("- ").append(page.path()).append(": ").append(page.title());
            if (!description.isEmpty()) {
                sb.append(" - ").append(description);
            }
            sb.append('\n');
        }
        return sb.toString().stripTrailing();
    }

    private Optional<Target> target() throws Exception {
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        if (!app.getHelpEnabled()) {
            return Optional.empty();
        }
        AI ai = load(aiService, app.getHelpAiUuid());
        if (!ownedByAdministrator(ai)) {
            return Optional.empty();
        }
        return Optional.of(new Target(ai, ai.isShared() && app.getHelpApplyLimits()));
    }

    /**
     * The entity with the uuid whoever owns it, null when there is none or it is deleted.
     */
    private <T extends OwnedEntity> T load(BaseService<T, ?> service, String uuid) throws Exception {
        if (StringUtils.isBlank(uuid)) {
            return null;
        }
        Long id = service.find(uuid);
        T entity = id == null ? null : service.find(id);
        return entity == null || entity.isDeleted() ? null : entity;
    }

    private boolean isOwnedByCurrentUser(OwnedEntity entity) {
        return entity != null && entity.getOwner() != null
                && Objects.equals(entity.getOwner().getId(), currentUser.getId());
    }

    /**
     * An AI of somebody who is no longer an administrator (or of nobody) is never used: the setting must not hand
     * a private AI of an ordinary user to everybody.
     */
    private boolean ownedByAdministrator(OwnedEntity entity) throws Exception {
        if (entity == null || entity.getOwner() == null) {
            return false;
        }
        User owner = userService.find(entity.getOwner().getId());
        return owner != null && !owner.isDeleted() && owner.isAdmin();
    }
}
