package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentCopyService;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyBranch;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyEntry;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyLorebook;
import com.github.enerccio.marginalia.domain.service.AgentCopyService.CopyPart;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The working copy of the book of the run, to read: a branch of its story part by part (as the agent has left it, or as it is
 * now while it works) and its lorebooks with their entries. It is a view, not a window of the book: nothing in it can be changed,
 * no backup of the copy is made and no plugin sees a book open. It is made when the tab is shown and when <b>Refresh</b> is
 * pressed, not while the run works.
 */
@Configurable
@Extendable
public class AgentCopyView extends VerticalLayout implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private AgentCopyService copyService;

    private final Supplier<AgentRun> runSupplier;

    private Span noCopyLabel;
    private VerticalLayout content;
    private TabSheet tabs;
    private ComboBox<CopyBranch> branchBox;
    private VerticalLayout partsBox;
    private Grid<Row> loreGrid;
    private VerticalLayout loreDetail;
    // true while the view sets the branch itself
    private boolean adjusting;
    // the part the story is scrolled to and the entry the lorebooks are set to, when the view was told where to look
    private String focusedPart;
    private String focusedEntry;
    // the parts that are shown, by uuid
    private final Map<String, Component> cards = new HashMap<>();

    /**
     * A line of the lorebooks: a lorebook, or one of its entries.
     */
    record Row(CopyLorebook book, CopyEntry entry) {

        String uuid() {
            return entry != null ? entry.uuid() : book.uuid();
        }

        String name() {
            return entry != null ? entry.name() : book.name();
        }
    }

    /**
     * @param runSupplier the run the console looks at now, null when there is none
     */
    public AgentCopyView(Supplier<AgentRun> runSupplier) {
        this.runSupplier = runSupplier;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "help", type = Span.class),
                    @APILocals(name = "refreshButton", type = Button.class),
                    @APILocals(name = "story", type = VerticalLayout.class),
                    @APILocals(name = "lore", type = SplitLayout.class),
                    @APILocals(name = "header", type = HorizontalLayout.class)})
    public void create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_AGENT_COPY));
        help.addClassName(CssStyles.HINT_TEXT);
        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refresh());
        refreshButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL);
        noCopyLabel = new Span(loc.getValue(L.MSG_AGENT_COPY_NONE));
        noCopyLabel.addClassName(CssStyles.HINT_TEXT);

        branchBox = new ComboBox<>(loc.getValue(L.LABEL_BRANCH));
        branchBox.setWidthFull();
        branchBox.setAllowCustomValue(false);
        branchBox.setItemLabelGenerator(branch -> String.format(loc.getValue(L.MSG_AGENT_COPY_BRANCH), branch.number(),
                branch.preview(), branch.parts()));
        branchBox.addValueChangeListener(event -> {
            if (!adjusting && event.getValue() != null) {
                focusedPart = null;
                showBranch(event.getValue().uuid());
            }
        });
        partsBox = new VerticalLayout();
        partsBox.setPadding(false);
        partsBox.setSpacing(false);
        partsBox.setWidthFull();
        VerticalLayout story = new VerticalLayout(branchBox, partsBox);
        story.setSizeFull();
        story.setPadding(false);
        story.setSpacing(false);
        story.addClassNames(CssStyles.OVERFLOW_AUTO, CssStyles.GAP_S);

        loreGrid = new Grid<>();
        loreGrid.setSizeFull();
        loreGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        loreGrid.addComponentColumn(row -> {
            Span name = new Span(StringUtils.defaultString(row.name()));
            if (row.entry() == null) {
                name.addClassName(CssStyles.TEXT_SEMIBOLD);
            }
            return name;
        }).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(1);
        loreGrid.addColumn(row -> loc.getValue(row.entry() == null ? L.ENUM_MERGE_KIND_LOREBOOK : L.ENUM_MERGE_KIND_ENTRY))
                .setHeader(loc.getValue(L.LABEL_TYPE)).setFlexGrow(0).setWidth("110px");
        loreGrid.addSelectionListener(event -> showRow(event.getFirstSelectedItem().orElse(null)));
        loreDetail = new VerticalLayout();
        loreDetail.setPadding(true);
        loreDetail.setSpacing(true);
        loreDetail.setSizeFull();
        loreDetail.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);
        SplitLayout lore = new SplitLayout(loreGrid, loreDetail);
        lore.setSizeFull();
        lore.setSplitterPosition(40);
        if (UIUtils.isPhone()) {
            lore.setOrientation(SplitLayout.Orientation.VERTICAL);
        }
        lore.addClassName(CssStyles.MIN_HEIGHT_0);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addThemeVariants(TabSheetVariant.LUMO_NO_PADDING);
        tabs.add(loc.getValue(L.LABEL_STORY_PART), story);
        tabs.add(loc.getValue(L.LABEL_LOREBOOKS), lore);

        content = new VerticalLayout(tabs);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);

        HorizontalLayout header = new HorizontalLayout(help, refreshButton);
        header.setWidthFull();
        header.setPadding(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(help);
        add(header, noCopyLabel, content);
        expand(content);
        showContent(false);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "hasCopy", type = boolean.class)})
    private void showContent(boolean hasCopy) {
        noCopyLabel.setVisible(!hasCopy);
        content.setVisible(hasCopy);
    }

    /**
     * Reads the copy again and shows it; keeps the branch and the entry that are shown when they are still there.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "run", type = AgentRun.class),
                    @APILocals(name = "branches", type = List.class),
                    @APILocals(name = "shown", type = AgentCopyService.CopyBranch.class),
                    @APILocals(name = "uuid", type = String.class),
                    @APILocals(name = "e", type = IllegalStateException.class),
                    @APILocals(name = "e", type = IllegalArgumentException.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void refresh() {
        if (tabs == null) {
            return;
        }
        AgentRun run = runSupplier.get();
        if (run == null || run.getWorkingCopy() == null) {
            showContent(false);
            return;
        }
        try {
            List<CopyBranch> branches = copyService.branches(run);
            CopyBranch shown = branchBox.getValue();
            adjusting = true;
            try {
                branchBox.setItems(branches);
            } finally {
                adjusting = false;
            }
            String uuid = focusedPart != null ? focusedPart : shown == null ? null : shown.uuid();
            showBranch(uuid);
            showLorebooks(run);
            showContent(true);
        } catch (IllegalStateException e) {
            // the copy is gone: the run ended while the view was open
            showContent(false);
        } catch (IllegalArgumentException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            showContent(false);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- the story

    /**
     * Shows the branch that has the part, scrolled to it.
     *
     * @param partUuid the uuid of a part of the copy
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "partUuid", type = String.class)})
    public void showPart(String partUuid) {
        focusedPart = partUuid;
        tabs.setSelectedIndex(0);
        refresh();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "partUuid", type = String.class)},
            locals = {@APILocals(name = "run", type = AgentRun.class),
                    @APILocals(name = "parts", type = List.class),
                    @APILocals(name = "part", type = AgentCopyService.CopyPart.class),
                    @APILocals(name = "card", type = Component.class),
                    @APILocals(name = "leaf", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void showBranch(String partUuid) {
        AgentRun run = runSupplier.get();
        try {
            List<CopyPart> parts = copyService.branch(run, partUuid);
            partsBox.removeAll();
            cards.clear();
            for (CopyPart part : parts) {
                Component card = card(part);
                cards.put(part.uuid(), card);
                partsBox.add(card);
            }
            if (!parts.isEmpty()) {
                String leaf = parts.getLast().uuid();
                branchBox.getListDataView().getItems().filter(branch -> branch.uuid().equals(leaf)).findFirst().ifPresent(branch -> {
                    adjusting = true;
                    try {
                        branchBox.setValue(branch);
                    } finally {
                        adjusting = false;
                    }
                });
            }
            if (focusedPart != null && cards.containsKey(focusedPart)) {
                JavascriptLibrary.scrollToAndHighlight(cards.get(focusedPart).getElement());
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "part", type = AgentCopyService.CopyPart.class)},
            locals = {@APILocals(name = "card", type = Div.class),
                    @APILocals(name = "head", type = Div.class),
                    @APILocals(name = "facts", type = List.class),
                    @APILocals(name = "line", type = Span.class)})
    private Component card(CopyPart part) {
        Div card = new Div();
        card.addClassName(CssStyles.AGENT_COPY_PART);
        Div head = new Div(new Span("#" + part.position()));
        head.addClassName(CssStyles.AGENT_COPY_PART_HEAD);
        if (part.swipes() > 1) {
            head.add(new Span(loc.getValue(L.LABEL_SWIPE) + " " + part.swipe() + "/" + part.swipes()));
        }
        head.add(new Span(loc.getValue(L.LABEL_WORDS) + ": " + part.words()));
        card.add(head, new Markdown(StringUtils.defaultString(part.text())));
        List<String> facts = new ArrayList<>();
        fact(facts, L.LABEL_SCENE_SETTING, part.sceneSetting());
        fact(facts, L.LABEL_POV_CHARACTER, part.povCharacter());
        fact(facts, L.LABEL_PRESENT_CHARACTERS, part.presentCharacters());
        if (!facts.isEmpty()) {
            Span line = new Span(String.join(" · ", facts));
            line.addClassName(CssStyles.HINT_TEXT);
            card.add(line);
        }
        if (StringUtils.isNotBlank(part.instructions())) {
            card.add(new Details(loc.getValue(L.LABEL_INSTRUCTIONS), new Markdown(part.instructions())));
        }
        if (StringUtils.isNotBlank(part.summary())) {
            card.add(new Details(loc.getValue(L.LABEL_SUMMARY), new Markdown(part.summary())));
        }
        return card;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "facts", type = List.class),
                    @APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "value", type = String.class)})
    private void fact(List<String> facts, L label, String value) {
        if (StringUtils.isNotBlank(value)) {
            facts.add(loc.getValue(label) + ": " + value.strip());
        }
    }

    // --- the lorebooks

    /**
     * Shows the entry (or the lorebook) of the copy, in the lorebooks.
     *
     * @param uuid the uuid of an entry or a lorebook of the copy
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "uuid", type = String.class)})
    public void showEntry(String uuid) {
        focusedEntry = uuid;
        tabs.setSelectedIndex(1);
        refresh();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "run", type = AgentRun.class)},
            locals = {@APILocals(name = "rows", type = List.class),
                    @APILocals(name = "book", type = AgentCopyService.CopyLorebook.class),
                    @APILocals(name = "entry", type = AgentCopyService.CopyEntry.class),
                    @APILocals(name = "keep", type = String.class),
                    @APILocals(name = "selected", type = AgentCopyView.Row.class)})
    private void showLorebooks(AgentRun run) throws Exception {
        List<Row> rows = new ArrayList<>();
        for (CopyLorebook book : copyService.lorebooks(run)) {
            rows.add(new Row(book, null));
            for (CopyEntry entry : book.entries()) {
                rows.add(new Row(book, entry));
            }
        }
        String keep = focusedEntry != null ? focusedEntry : loreGrid.getSelectedItems().stream().map(Row::uuid).findFirst().orElse(null);
        loreGrid.setItems(rows);
        Row selected = rows.stream().filter(row -> row.uuid().equals(keep)).findFirst().orElse(null);
        if (selected != null) {
            loreGrid.select(selected);
        } else {
            loreDetail.removeAll();
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "row", type = AgentCopyView.Row.class)},
            locals = {@APILocals(name = "book", type = AgentCopyService.CopyLorebook.class),
                    @APILocals(name = "entry", type = AgentCopyService.CopyEntry.class)})
    private void showRow(Row row) {
        loreDetail.removeAll();
        if (row == null) {
            return;
        }
        if (row.entry() == null) {
            CopyLorebook book = row.book();
            loreDetail.add(title(book.name()), line(L.LABEL_ENABLED, yes(book.enabled())),
                    line(L.LABEL_SUBBOOKS, String.join(", ", book.subbooks())), line(L.LABEL_ENTRIES, String.valueOf(book.entries().size())));
            return;
        }
        CopyEntry entry = row.entry();
        loreDetail.add(title(row.book().name() + " / " + entry.name()), line(L.LABEL_ENABLED, yes(entry.enabled())),
                line(L.LABEL_ORDER, String.valueOf(entry.order())), line(L.LABEL_TAGS, String.join(", ", entry.tags())),
                line(L.LABEL_NEGATIVE_TAGS, String.join(", ", entry.negativeTags())),
                line(L.LABEL_FILTERING_MODE, StringUtils.defaultString(entry.filteringMode())),
                line(L.LABEL_INSERTION_MODE, StringUtils.defaultString(entry.insertionMode())),
                line(L.LABEL_FILTERING, StringUtils.defaultString(entry.filtering())),
                area(L.LABEL_CONTENT, entry.payload()), area(L.LABEL_NOTE, entry.comment()));
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "value", type = boolean.class)})
    private String yes(boolean value) {
        return loc.getValue(value ? L.LABEL_YES : L.LABEL_NO);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "text", type = String.class)},
            locals = {@APILocals(name = "title", type = Span.class)})
    private Span title(String text) {
        Span title = new Span(text);
        title.addClassName(CssStyles.TEXT_SEMIBOLD);
        return title;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "line", type = Span.class)})
    private Component line(L label, String value) {
        Span line = new Span(loc.getValue(label) + ": " + value);
        return line;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextArea.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "area", type = TextArea.class)})
    private TextArea area(L label, String value) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setReadOnly(true);
        area.setWidthFull();
        area.setMinRows(2);
        area.setMaxRows(16);
        area.setValue(StringUtils.defaultString(value));
        return area;
    }

    // for the tests of the view

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = VerticalLayout.class))
    VerticalLayout getPartsBox() {
        return partsBox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<CopyBranch> getBranchBox() {
        return branchBox;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Grid.class))
    Grid<Row> getLoreGrid() {
        return loreGrid;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = VerticalLayout.class))
    VerticalLayout getLoreDetail() {
        return loreDetail;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TabSheet.class))
    TabSheet getTabs() {
        return tabs;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class))
    Span getNoCopyLabel() {
        return noCopyLabel;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Map.class))
    Map<String, Component> getCards() {
        return cards;
    }
}
