package com.github.enerccio.marginalia.extensions.chaptermarking;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configurable
public class ChapterMarkingExtension implements MarginaliaExtension {

    private static final Pattern HEADER_PATTERN = Pattern.compile("(?m)^\\s*#+\\s*(.+)$");
    private static final String MSG_UUID = "com.github.enerccio.marginalia.extensions.chaptermarking.ChapterMarkingExtension.MSG_UUID";
    private static final String MSG_SET = "com.github.enerccio.marginalia.extensions.chaptermarking.ChapterMarkingExtension.MSG_SET";
    private static final String MSG_OLD_TEXT = "com.github.enerccio.marginalia.extensions.chaptermarking.ChapterMarkingExtension.MSG_OLD_TEXT";
    private static final String MSG_ORDER = "com.github.enerccio.marginalia.extensions.chaptermarking.ChapterMarkingExtension.MSG_ORDER";

    private ExtensionDecorator sidebarDecorator;
    private ExtensionDecorator sidebarEditDecorator;

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
                        ComponentUtil.setData(sidebarBtn, MSG_OLD_TEXT, sidebarBtn.getText());
                        ComponentUtil.setData(sidebarBtn, MSG_ORDER, orderId);

                        String chapterTitle = extractChapterTitle(msg.getResponse());
                        if (chapterTitle == null) {
                            ComponentUtil.setData(sidebarBtn, MSG_SET, false);
                        } else {
                            String formattedText = String.format("%d. %s", orderId != null ? orderId : 0, chapterTitle);
                            sidebarBtn.setText(formattedText);

                            sidebarBtn.getStyle().set("color", "var(--lumo-primary-color)");
                            sidebarBtn.getStyle().set("font-weight", "600");
                            ComponentUtil.setData(sidebarBtn, MSG_SET, true);
                        }
                        ComponentUtil.setData(sidebarBtn, MSG_UUID, msg.getUuid());
                    }
                }
            };

            // Register decorator for createSidebarButton method in ManuscriptStoryPart
            extensionService.registerDecorator(
                    sidebarDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "createSidebarButton"
            );

            sidebarEditDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {

                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    ChatMessage chatMessage = context.getReflectiveFieldValue(instrumented, "message", ChatMessage.class);
                    if (chatMessage != null) {
                        ManuscriptStoryPart parentPart = context.getReflectiveFieldValue(instrumented, "this$0", ManuscriptStoryPart.class);
                        if (parentPart != null) {
                            VerticalLayout sidebarList = context.getReflectiveFieldValue(parentPart, "sidebarList", VerticalLayout.class);
                            if (sidebarList != null) {
                                sidebarList.getChildren().forEach(c -> {
                                    String uuid = (String) ComponentUtil.getData(c, MSG_UUID);
                                    if (chatMessage.getUuid().equals(uuid)) {
                                        Button sidebarBtn = (Button) c;

                                        String chapterTitle = extractChapterTitle(chatMessage.getResponse());
                                        if (chapterTitle == null) {
                                            if ((Boolean) ComponentUtil.getData(sidebarBtn, MSG_SET)) {
                                                sidebarBtn.getStyle().remove("color");
                                                sidebarBtn.getStyle().remove("font-weight");
                                                sidebarBtn.setText((String) ComponentUtil.getData(sidebarBtn, MSG_OLD_TEXT));
                                            }
                                            ComponentUtil.setData(sidebarBtn, MSG_SET, false);
                                        } else {
                                            Integer orderId = (Integer) ComponentUtil.getData(sidebarBtn, MSG_ORDER);
                                            String formattedText = String.format("%d. %s", orderId != null ? orderId : 0, chapterTitle);
                                            sidebarBtn.setText(formattedText);

                                            sidebarBtn.getStyle().set("color", "var(--lumo-primary-color)");
                                            sidebarBtn.getStyle().set("font-weight", "600");
                                            ComponentUtil.setData(sidebarBtn, MSG_SET, true);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            };

            extensionService.registerDecorator(
                    sidebarEditDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart$ChatMessageCard",
                    "autosaveAndSwapToMarkdown"
            );
        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Chapter Marking extension", e);
        }
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (sidebarDecorator != null) {
            extensionService.unregisterDecorator(sidebarDecorator);
            sidebarDecorator = null;
        }
        if (sidebarEditDecorator != null) {
            extensionService.unregisterDecorator(sidebarEditDecorator);
            sidebarEditDecorator = null;
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