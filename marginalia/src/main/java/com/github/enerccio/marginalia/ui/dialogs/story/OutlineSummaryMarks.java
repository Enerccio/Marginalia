package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

/**
 * Shows in the buttons of the outline which parts have a summary: an icon after the text of the button. The icon is
 * a separate part of the button, so extensions that change the text of the button (chapter titles) keep it.
 */
public final class OutlineSummaryMarks {

    private static final String PART_ID = OutlineSummaryMarks.class.getName() + ".PART_ID";

    private OutlineSummaryMarks() {
    }

    /**
     * Marks the button of a part of the outline, called when the button is made.
     */
    public static void init(Button button, ChatMessage message, Long partId, Localization loc) {
        ComponentUtil.setData(button, PART_ID, partId);
        mark(button, message != null && message.getSummary() != null, loc);
    }

    /**
     * Marks the buttons again from the parts as they are in the database now.
     */
    public static void refresh(VerticalLayout outline, List<ChatMessage> branch, Localization loc) {
        outline.getChildren().forEach(child -> {
            if (child instanceof Button button && ComponentUtil.getData(button, PART_ID) instanceof Long id) {
                mark(button, branch.stream().anyMatch(m -> id.equals(m.getId()) && m.getSummary() != null), loc);
            }
        });
    }

    private static void mark(Button button, boolean hasSummary, Localization loc) {
        if (hasSummary) {
            button.setIcon(Solid.FILE_ALT.create());
            button.setIconAfterText(true);
            UIUtils.addTooltip(button, loc.getValue(L.LABEL_SUMMARY));
        } else {
            button.setIcon(null);
            button.getElement().removeAttribute("title");
        }
    }
}
