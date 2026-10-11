package com.github.enerccio.marginalia.extensions.instructionexporter;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.notification.Notification;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Configurable
public class InstructionExporterExtension implements MarginaliaExtension {

    private static final Logger log = LoggerFactory.getLogger(InstructionExporterExtension.class);

    private static final String KEY = "com.github.enerccio.marginalia.extensions.instructionexporter.InstructionExporterExtension";
    private static final String MENU_ITEM_KEY = KEY + ".menuItem";

    @Autowired
    private ChatMessageService chatMessageService;

    private ExtensionDecorator cogsDecorator;

    // the settings menu items of story editors that are open, to remove the item when the extension is unloaded
    private final Set<MenuItem> activeCogs = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            cogsDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (throwing != null) {
                        return;
                    }
                    MenuItem cogs = context.getLocalVariable("cogs", MenuItem.class);
                    if (cogs == null || ComponentUtil.getData(cogs, MENU_ITEM_KEY) != null) {
                        return;
                    }

                    // the story editor loads other books into the same instance - read the book when clicked
                    MenuItem item = cogs.getSubMenu().addItem(
                            UIUtils.menuItemWithIcon(Solid.CLIPBOARD_LIST.create(), "Export instructions"),
                            e -> exportInstructions(context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class)));
                    ComponentUtil.setData(cogs, MENU_ITEM_KEY, item);
                    activeCogs.add(cogs);
                }
            };

            // the settings menu of the story, on the desktop and on the phone
            extensionService.registerDecorator(
                    cogsDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "createCogsMenuItem"
            );
            extensionService.registerDecorator(
                    cogsDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.phone.manuscript.PhoneManuscriptStoryPart",
                    "createCogsMenuItem"
            );
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Instruction Exporter extension", e);
        }
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
        if (cogsDecorator != null) {
            extensionService.unregisterDecorator(cogsDecorator);
            cogsDecorator = null;
        }

        // the components belong to the UIs of all users - change each one in its own session
        synchronized (activeCogs) {
            for (MenuItem cogs : activeCogs) {
                UIUtils.accessComponent(cogs, () -> {
                    MenuItem item = (MenuItem) ComponentUtil.getData(cogs, MENU_ITEM_KEY);
                    if (item != null) {
                        cogs.getSubMenu().remove(item);
                        ComponentUtil.setData(cogs, MENU_ITEM_KEY, null);
                    }
                });
            }
            activeCogs.clear();
        }
    }
}
