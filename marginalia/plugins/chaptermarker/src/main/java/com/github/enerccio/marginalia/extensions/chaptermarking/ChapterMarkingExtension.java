package com.github.enerccio.marginalia.extensions.chaptermarking;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configurable
public class ChapterMarkingExtension implements MarginaliaExtension {

    private static final Pattern HEADER_PATTERN = Pattern.compile("(?m)^\\s*#+\\s*(.+)$");

    @Autowired
    private Localization loc;

    private ExtensionDecorator sidebarDecorator;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            sidebarDecorator = new ExtensionDecorator() {

                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {
                    // No enter logic needed
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (throwing != null) {
                        return;
                    }

                    ChatMessage msg = context.getMethodArgument("msg", ChatMessage.class);
                    Integer orderId = context.getMethodArgument("orderId", int.class);

                    if (msg == null || StringUtils.isBlank(msg.getResponse())) {
                        return;
                    }

                    String chapterTitle = extractChapterTitle(msg.getResponse());
                    if (chapterTitle == null) {
                        return;
                    }

                    VerticalLayout sidebarList = context.getReflectiveFieldValue(instrumented, "sidebarList", VerticalLayout.class);
                    if (sidebarList == null || sidebarList.getComponentCount() == 0) {
                        return;
                    }

                    Button sidebarBtn = null;
                    if (context.hasLocalVariable("sidebarBtn", Button.class)) {
                        sidebarBtn = context.getLocalVariable("sidebarBtn", Button.class);
                    } else if (sidebarList.getComponentAt(sidebarList.getComponentCount() - 1) instanceof Button lastBtn) {
                        sidebarBtn = lastBtn;
                    }

                    if (sidebarBtn != null) {
                        String formattedText = String.format("%d. %s", orderId != null ? orderId : 0, chapterTitle);
                        sidebarBtn.setText(formattedText);

                        sidebarBtn.getStyle().set("color", "var(--lumo-primary-color)");
                        sidebarBtn.getStyle().set("font-weight", "600");
                    }
                }
            };

            // Register decorator for createSidebarButton method in ManuscriptStoryPart
            extensionService.registerDecorator(
                    sidebarDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "createSidebarButton"
            );

        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (sidebarDecorator != null) {
            extensionService.unregisterDecorator(sidebarDecorator);
            sidebarDecorator = null;
        }
    }

    /**
     * Searches message text for the first Markdown heading line starting with `#` and extracts the header title.
     */
    private String extractChapterTitle(String responseText) {
        if (StringUtils.isBlank(responseText)) {
            return null;
        }
        Matcher matcher = HEADER_PATTERN.matcher(responseText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}