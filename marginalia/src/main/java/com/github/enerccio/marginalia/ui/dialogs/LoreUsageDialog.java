package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.PartLoreUsage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.LoreUsageService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The lorebook entries the prompt of one part used, with the tokens each one cost and what found it (the prompt, or the
 * content of other entries when the lorebook scans deeper). Read from the record the generation kept
 * ({@code MessagePrompt#getLoreUsage()}).
 */
@Configurable
public class LoreUsageDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private LoreUsageService loreUsageService;

    private final ChatMessage part;

    public LoreUsageDialog(ChatMessage part) {
        this.part = part;
    }

    public void create() throws Exception {
        setHeaderTitle(loc.getValue(L.LABEL_LORE_USAGE));
        setWidth("750px");
        setMaxWidth("100vw");
        setResizable(true);
        setDraggable(true);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        PartLoreUsage usage = loreUsageService.forPart(part);
        if (!usage.recorded()) {
            content.add(note(loc.getValue(L.MSG_LORE_USAGE_PART_NOT_RECORDED)));
        } else if (usage.entries().isEmpty()) {
            content.add(note(loc.getValue(L.MSG_LORE_USAGE_PART_NOTHING)));
        } else {
            content.add(note(String.format(loc.getValue(L.MSG_LORE_USAGE_PART_SUMMARY), usage.entries().size(),
                    String.format("%,d", usage.totalTokens()))));
            content.add(table(usage));
        }
        add(content);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        closeButton.setThemeName("primary");
        getFooter().add(closeButton);
    }

    private Grid<PartLoreUsage.Used> table(PartLoreUsage usage) {
        Grid<PartLoreUsage.Used> grid = new Grid<>(PartLoreUsage.Used.class, false);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.setAllRowsVisible(usage.entries().size() <= 15);
        grid.addColumn(used -> used.name() == null ? loc.getValue(L.LABEL_LORE_USAGE_UNKNOWN_ENTRY) : used.name())
                .setHeader(loc.getValue(L.ENUM_MERGE_KIND_ENTRY)).setFlexGrow(3).setSortable(true);
        grid.addColumn(used -> used.lorebook() == null ? "" : used.lorebook())
                .setHeader(loc.getValue(L.LABEL_LOREBOOK)).setFlexGrow(2).setSortable(true);
        grid.addColumn(used -> used.depth() == 0 ? loc.getValue(L.LABEL_LORE_USAGE_IN_PROMPT)
                        : String.format(loc.getValue(L.MSG_LORE_USAGE_IN_ENTRIES), used.depth()))
                .setHeader(loc.getValue(L.LABEL_LORE_USAGE_FOUND_IN)).setFlexGrow(2);
        grid.addColumn(used -> String.format("%,d", used.tokens())).setHeader(loc.getValue(L.LABEL_TOKENS))
                .setComparator(java.util.Comparator.comparingLong(PartLoreUsage.Used::tokens)).setTextAlign(ColumnTextAlign.END);
        grid.setItems(usage.entries());
        return grid;
    }

    private Div note(String text) {
        Div note = new Div(text);
        note.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        return note;
    }
}
