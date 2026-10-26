package com.github.enerccio.marginalia.extensions.instructionexporter;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configurable
public class InstructionExporterExtension implements MarginaliaExtension {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    private static final String MENU_ITEM_KEY = "instructionexporter.export";

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        // an item of the settings menu of the story, on the desktop and on the phone; the story editor loads other books into the same
        // instance, so the book is read when the item is chosen
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.tools(MENU_ITEM_KEY, l -> "Export instructions", story -> exportInstructions(story.getCurrentManuscript()))
                        .icon(Solid.CLIPBOARD_LIST::create));
    }

    private void exportInstructions(Manuscript manuscript) {
        try {
            if (manuscript == null || manuscript.getActiveLeaf() == null) {
                Notification.show("There is no active branch to export");
                return;
            }

            // root to the active leaf, a block of fields per part
            List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf());
            String text = branch.stream()
                    .map(InstructionExporterExtension::formatMessage)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining("\n\n"));

            if (text.isEmpty()) {
                Notification.show("There is nothing to export in the active branch");
                return;
            }

            UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", text);
            Notification.show("Copied to clipboard");
        } catch (Exception e) {
            log.error("Failed to export: {}", e.getMessage(), e);
            Notification.show("Failed to export: " + e.getMessage());
        }
    }

    /**
     * The filled fields of the part as {@code Field:\nContents} blocks, one under another; empty if it has none.
     */
    private static String formatMessage(ChatMessage message) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Scene Setting", message.getSceneSetting());
        fields.put("POV Character", message.getPovCharacter());
        fields.put("Present Characters", message.getPresentCharacters());
        fields.put("Instructions", message.getInstructions());

        return fields.entrySet().stream()
                .filter(field -> StringUtils.isNotBlank(field.getValue()))
                .map(field -> field.getKey() + ":\n" + field.getValue().strip())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        // the settings menus that are open hide the item and it runs nothing any more
        menuRegistry.unregisterAll(bundle.getSymbolicName());
    }
}
