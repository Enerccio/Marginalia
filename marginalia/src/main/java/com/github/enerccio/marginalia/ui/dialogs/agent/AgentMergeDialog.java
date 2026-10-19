package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMergeService;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeResult;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.agent.MergeDiff;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Field;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Type;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The review of what a finished (or stopped and kept) run changed: every change of the agent in a list the user ticks, the
 * detail of the one they look at with the value of the book now and the value the apply writes, which they can edit. A
 * change both the user and the agent made to the same text is a conflict, and starts unticked. The picked changes are applied
 * to the book in place after a backup of it; the whole result can be discarded instead.
 */
@Configurable
public class AgentMergeDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private AgentRunService agentRunService;

    private final String agentName;
    private final AgentRun run;
    private final Runnable onChange;

    private MergePlan plan;
    private Grid<Item> grid;
    private Span summaryLabel;
    private Span hintLabel;
    private Checkbox branchBox;
    private VerticalLayout detail;
    private Button applyButton;
    private Button discardButton;
    // the text the user wrote in place of the agent's, by item id and field name
    private final Map<String, Map<String, String>> edits = new HashMap<>();
    // true while the dialog changes the selection itself
    private boolean adjusting;
    private Item focused;

    /**
     * @param onChange called after the changes were applied or the result was discarded, the run is not what it was
     */
    public AgentMergeDialog(String agentName, AgentRun run, Runnable onChange) {
        this.agentName = agentName;
        this.run = run;
        this.onChange = onChange;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_REVIEW_CHANGES) + " - " + agentName);
        setWidth("1300px");
        setMaxWidth("96%");
        setHeight("92%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);

        summaryLabel = new Span();
        summaryLabel.addClassName(CssStyles.TEXT_SEMIBOLD);
        hintLabel = new Span(loc.getValue(L.HELP_MERGE));
        hintLabel.addClassName(CssStyles.HINT_TEXT);

        Button allButton = small(L.LABEL_SELECT_ALL, event -> pick(item -> item.applicable()));
        Button noneButton = small(L.LABEL_SELECT_NONE, event -> pick(item -> false));
        Button newButton = small(L.LABEL_SELECT_NEW_PARTS, event ->
                pick(item -> item.applicable() && item.kind() == Kind.MESSAGE && item.change() == Change.ADDED));
        Button cleanButton = small(L.LABEL_SELECT_WITHOUT_CONFLICTS, event -> pick(this::isClean));
        branchBox = new Checkbox(loc.getValue(L.LABEL_SWITCH_TO_AGENT_BRANCH));
        HorizontalLayout toolbar = new HorizontalLayout(allButton, noneButton, newButton, cleanButton, branchBox);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addColumn(item -> loc.getValue(kindText(item.kind()))).setHeader(loc.getValue(L.LABEL_TYPE)).setFlexGrow(0).setWidth("110px");
        grid.addColumn(item -> loc.getValue(changeText(item.change()))).setHeader(loc.getValue(L.LABEL_STATE)).setFlexGrow(0).setWidth("100px");
        grid.addColumn(Item::label).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(1);
        grid.addComponentColumn(this::states).setHeader("").setFlexGrow(0).setWidth("170px");
        grid.addItemClickListener(event -> show(event.getItem()));
        grid.asMultiSelect().addSelectionListener(event -> {
            if (!adjusting) {
                keepConsistent(event.getAddedSelection(), event.getRemovedSelection());
            }
            updateApply();
        });

        detail = new VerticalLayout();
        detail.setPadding(true);
        detail.setSpacing(true);
        detail.setSizeFull();
        detail.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO);

        SplitLayout split = new SplitLayout(grid, detail);
        split.setSizeFull();
        split.setSplitterPosition(42);
        split.addClassName(CssStyles.MIN_HEIGHT_0);

        VerticalLayout content = new VerticalLayout(summaryLabel, hintLabel, toolbar, split);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S);
        content.expand(split);
        add(content);

        discardButton = new Button(loc.getValue(L.LABEL_DISCARD_RESULT), Solid.TRASH.create(), event ->
                ConfirmDialog.open(loc.getValue(L.MSG_MERGE_CONFIRM_DISCARD), this::discard, () -> {}));
        discardButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        applyButton = new Button(loc.getValue(L.LABEL_APPLY_PICKED_CHANGES), Solid.CHECK.create(), event -> confirmApply());
        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout footer = new HorizontalLayout(discardButton, closeButton, applyButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        load();
    }

    private Button small(L label, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(loc.getValue(label), listener);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        return button;
    }

    /**
     * Reads the plan again and ticks what can be taken over without a decision.
     */
    void load() {
        try {
            plan = mergeService.plan(run);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            plan = new MergePlan(List.of(), Map.of(), false);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            plan = new MergePlan(List.of(), Map.of(), false);
        }
        edits.clear();
        grid.setItems(plan.items());
        summaryLabel.setText(plan.isEmpty() ? loc.getValue(L.MSG_MERGE_NO_CHANGES) : String.format(loc.getValue(L.MSG_MERGE_SUMMARY),
                plan.items().size(), count(Kind.MESSAGE), count(Kind.LOREBOOK), count(Kind.ENTRY), count(Kind.BOOK), plan.conflicts()));
        hintLabel.setText(loc.getValue(plan.bookChanged() ? L.MSG_MERGE_BOOK_CHANGED : L.HELP_MERGE));
        pick(this::isClean);
        showFirst();
    }

    private long count(Kind kind) {
        return plan.items().stream().filter(item -> item.kind() == kind).count();
    }

    /**
     * What is ticked at first: what the agent changed and the user did not, and that belongs to this book alone.
     */
    private boolean isClean(Item item) {
        return item.applicable() && !item.conflict() && item.sharedWith().isEmpty();
    }

    private void pick(java.util.function.Predicate<Item> wanted) {
        Set<Item> chosen = plan.items().stream().filter(wanted).collect(Collectors.toCollection(LinkedHashSet::new));
        // what a pick needs comes with it, and what needs a part that is not picked goes
        Set<String> ids = chosen.stream().map(Item::id).collect(Collectors.toSet());
        chosen.removeIf(item -> item.dependsOn() != null && !ids.contains(item.dependsOn()));
        adjusting = true;
        try {
            grid.asMultiSelect().setValue(chosen);
        } finally {
            adjusting = false;
        }
        updateApply();
    }

    /**
     * An item that is picked brings what it needs, an item that is dropped drops what needs it, and what cannot be applied
     * is not picked at all.
     */
    private void keepConsistent(Set<Item> added, Set<Item> removed) {
        Set<Item> selected = new HashSet<>(grid.asMultiSelect().getSelectedItems());
        Map<String, Item> byId = plan.items().stream().collect(Collectors.toMap(Item::id, item -> item, (a, b) -> a));
        boolean blocked = false;
        for (Item item : added) {
            if (!item.applicable()) {
                selected.remove(item);
                blocked = true;
                continue;
            }
            for (String id : MergeDiff.chain(plan, item)) {
                Item needed = byId.get(id);
                if (needed != null && needed.applicable()) {
                    selected.add(needed);
                }
            }
        }
        for (Item item : removed) {
            for (String id : MergeDiff.dependents(plan, item.id())) {
                selected.remove(byId.get(id));
            }
        }
        if (blocked) {
            Notification.warning(loc.getValue(L.ENUM_MERGE_STATE_BLOCKED));
        }
        if (!selected.equals(grid.asMultiSelect().getSelectedItems())) {
            adjusting = true;
            try {
                grid.asMultiSelect().setValue(selected);
            } finally {
                adjusting = false;
            }
        }
    }

    private void updateApply() {
        if (applyButton == null) {
            return;
        }
        int picked = grid.asMultiSelect().getSelectedItems().size();
        applyButton.setEnabled(picked > 0);
        applyButton.setText(loc.getValue(L.LABEL_APPLY_PICKED_CHANGES) + (picked > 0 ? " (" + picked + ")" : ""));
    }

    // --- the list

    private static L kindText(Kind kind) {
        return switch (kind) {
            case BOOK -> L.ENUM_MERGE_KIND_BOOK;
            case MESSAGE -> L.ENUM_MERGE_KIND_MESSAGE;
            case LOREBOOK -> L.ENUM_MERGE_KIND_LOREBOOK;
            case ENTRY -> L.ENUM_MERGE_KIND_ENTRY;
        };
    }

    private static L changeText(Change change) {
        return switch (change) {
            case ADDED -> L.ENUM_MERGE_CHANGE_ADDED;
            case CHANGED -> L.ENUM_MERGE_CHANGE_CHANGED;
            case DELETED -> L.ENUM_MERGE_CHANGE_DELETED;
        };
    }

    private Component states(Item item) {
        HorizontalLayout states = new HorizontalLayout();
        states.setSpacing(false);
        states.setPadding(false);
        if (!item.applicable()) {
            states.add(badge(L.ENUM_MERGE_STATE_BLOCKED, "error"));
        } else if (item.conflict()) {
            states.add(badge(L.ENUM_MERGE_STATE_CONFLICT, "error"));
        }
        if (!item.sharedWith().isEmpty()) {
            states.add(badge(L.ENUM_MERGE_STATE_SHARED, "contrast"));
        }
        return states;
    }

    private Span badge(L text, String theme) {
        Span badge = new Span(loc.getValue(text));
        badge.getElement().getThemeList().add("badge small " + theme);
        return badge;
    }

    // --- the detail

    private void showFirst() {
        show(plan.items().isEmpty() ? null : plan.items().getFirst());
    }

    void show(Item item) {
        focused = item;
        detail.removeAll();
        if (item == null) {
            return;
        }
        Span title = new Span(loc.getValue(kindText(item.kind())) + " - " + loc.getValue(changeText(item.change())) + ": " + item.label());
        title.addClassName(CssStyles.TEXT_SEMIBOLD);
        detail.add(title);
        if (item.problem() != null) {
            detail.add(note(problem(item.problem()), "error"));
        } else if (item.conflict()) {
            detail.add(note(loc.getValue(L.ENUM_MERGE_STATE_CONFLICT), "error"));
        }
        if (!item.sharedWith().isEmpty()) {
            detail.add(note(String.format(loc.getValue(L.MSG_MERGE_SHARED), String.join(", ", item.sharedWith())), "contrast"));
        }
        for (Field field : item.fields()) {
            detail.add(fieldView(item, field));
        }
    }

    private Span note(String text, String theme) {
        Span note = new Span(text);
        note.getElement().getThemeList().add("badge " + theme);
        return note;
    }

    private String problem(String problem) {
        return loc.getValue(switch (problem) {
            case MergePlan.PROBLEM_NEEDS_PARENT -> L.MSG_MERGE_PROBLEM_NEEDS_PARENT;
            case MergePlan.PROBLEM_NEEDS_LOREBOOK -> L.MSG_MERGE_PROBLEM_NEEDS_LOREBOOK;
            default -> L.MSG_MERGE_PROBLEM_GONE;
        });
    }

    private Component fieldView(Item item, Field field) {
        VerticalLayout view = new VerticalLayout();
        view.setPadding(false);
        view.setSpacing(false);
        view.addClassName(CssStyles.GAP_S);
        Span name = new Span(loc.getValue(fieldText(field.name())));
        name.addClassName(CssStyles.TEXT_SEMIBOLD);
        view.add(name);
        if (field.conflict()) {
            view.add(note(loc.getValue(L.ENUM_MERGE_STATE_CONFLICT), "error"));
        }
        if (field.type() == Type.TEXT && field.editable() && item.change() != Change.DELETED && item.applicable()) {
            view.add(textView(item, field));
        } else if (field.type() == Type.SET) {
            view.add(setView(field));
        } else {
            view.add(readOnly(L.LABEL_MERGE_BEFORE_RUN, named(field.type(), field.base())));
            view.add(readOnly(L.LABEL_MERGE_IN_BOOK_NOW, named(field.type(), field.book())));
            if (item.change() != Change.DELETED) {
                view.add(readOnly(L.LABEL_MERGE_TO_APPLY, named(field.type(), field.agent())));
            }
        }
        return view;
    }

    /**
     * The book's text next to the text that is applied; the second one is written over when the user wants.
     */
    private Component textView(Item item, Field field) {
        TextArea toApply = new TextArea(loc.getValue(L.LABEL_MERGE_TO_APPLY));
        toApply.setWidthFull();
        toApply.setMinRows(3);
        toApply.setMaxRows(14);
        toApply.setValue(edits.getOrDefault(item.id(), Map.of()).getOrDefault(field.name(), field.agent()));
        toApply.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
        toApply.addValueChangeListener(event -> edit(item, field, event.getValue()));
        if (item.change() == Change.ADDED) {
            return toApply;
        }
        TextArea inBook = readOnlyArea(L.LABEL_MERGE_IN_BOOK_NOW, field.book());
        Button useBook = new Button(loc.getValue(L.LABEL_USE_BOOK_VERSION), event -> toApply.setValue(field.book()));
        Button useAgent = new Button(loc.getValue(L.LABEL_USE_AGENT_VERSION), event -> toApply.setValue(field.agent()));
        for (Button button : List.of(useBook, useAgent)) {
            button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        }
        VerticalLayout both = new VerticalLayout(inBook, toApply, new HorizontalLayout(useBook, useAgent));
        both.setPadding(false);
        both.setSpacing(false);
        both.setWidthFull();
        return both;
    }

    private void edit(Item item, Field field, String text) {
        Map<String, String> fields = edits.computeIfAbsent(item.id(), id -> new HashMap<>());
        if (text == null || text.equals(field.agent())) {
            fields.remove(field.name());
        } else {
            fields.put(field.name(), text);
        }
    }

    private Component setView(Field field) {
        Set<String> before = lines(field.base());
        Set<String> after = lines(field.agent());
        VerticalLayout view = new VerticalLayout();
        view.setPadding(false);
        view.setSpacing(false);
        for (String added : after) {
            if (!before.contains(added)) {
                view.add(new Span("+ " + name(added)));
            }
        }
        for (String removed : before) {
            if (!after.contains(removed)) {
                view.add(new Span("- " + name(removed)));
            }
        }
        return view;
    }

    private static Set<String> lines(String text) {
        Set<String> values = new LinkedHashSet<>();
        if (text != null) {
            for (String line : text.split("\n")) {
                if (!line.isEmpty()) {
                    values.add(line);
                }
            }
        }
        return values;
    }

    /**
     * The name of a lorebook given by its key; anything else as it is.
     */
    private String name(String key) {
        String name = plan.names().get(key);
        return name != null ? name : key;
    }

    private String named(Type type, String value) {
        if (type == Type.REF) {
            return name(value);
        }
        return value == null ? "" : value;
    }

    private Component readOnly(L label, String value) {
        return readOnlyArea(label, value);
    }

    private TextArea readOnlyArea(L label, String value) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setReadOnly(true);
        area.setWidthFull();
        area.setMinRows(1);
        area.setMaxRows(10);
        area.setValue(value == null ? "" : value);
        return area;
    }

    private L fieldText(String field) {
        return switch (field) {
            case "name" -> L.LABEL_NAME;
            case "description" -> L.LABEL_DESCRIPTION;
            case "language" -> L.LABEL_LANGUAGE;
            case "pov" -> L.LABEL_POV;
            case "tense" -> L.LABEL_TENSE;
            case "style" -> L.LABEL_STYLE;
            case "template" -> L.LABEL_MASTER_TEMPLATE;
            case "userPrompt" -> L.LABEL_TOKEN_USER_PROMPT;
            case "summaryPrompt" -> L.LABEL_SUMMARY_PROMPT;
            case "metaSummaryPrompt" -> L.LABEL_META_SUMMARY_PROMPT;
            case "tags" -> L.LABEL_TAGS;
            case "negativeTags" -> L.LABEL_NEGATIVE_TAGS;
            case "response" -> L.LABEL_PART_TEXT;
            case "instructions" -> L.LABEL_INSTRUCTIONS;
            case "sceneSetting" -> L.LABEL_SCENE_SETTING;
            case "povCharacter" -> L.LABEL_POV_CHARACTER;
            case "presentCharacters" -> L.LABEL_PRESENT_CHARACTERS;
            case "summary" -> L.LABEL_SUMMARY;
            case "enabled" -> L.LABEL_ENABLED;
            case "subbooks" -> L.LABEL_SUBBOOKS;
            case "payload" -> L.LABEL_CONTENT;
            case "comment" -> L.LABEL_NOTE;
            case "order" -> L.LABEL_ORDER;
            case "filtering" -> L.LABEL_FILTERING;
            case "filteringMode" -> L.LABEL_FILTERING_MODE;
            case "insertionMode" -> L.LABEL_INSERTION_MODE;
            case "lorebook" -> L.LABEL_LOREBOOK;
            default -> L.LABEL_BOOK;
        };
    }

    // --- apply and discard

    private void confirmApply() {
        Set<Item> picked = grid.asMultiSelect().getSelectedItems();
        if (picked.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_MERGE_NOTHING_PICKED));
            return;
        }
        long conflicts = picked.stream().filter(Item::conflict).count();
        if (conflicts > 0) {
            ConfirmDialog.open(String.format(loc.getValue(L.MSG_MERGE_CONFIRM_CONFLICTS), conflicts), this::apply, () -> {});
        } else {
            apply();
        }
    }

    /**
     * Applies the picked changes with the texts the user wrote.
     */
    void apply() {
        Set<String> ids = grid.asMultiSelect().getSelectedItems().stream().map(Item::id).collect(Collectors.toSet());
        Map<String, Map<String, String>> written = new HashMap<>();
        edits.forEach((id, fields) -> {
            if (ids.contains(id) && !fields.isEmpty()) {
                written.put(id, new HashMap<>(fields));
            }
        });
        try {
            MergeResult result = mergeService.apply(run, new MergeSelection(ids, written, branchBox.getValue()));
            Notification.success(String.format(loc.getValue(L.MSG_MERGE_APPLIED), result.total()));
            if (!result.skipped().isEmpty()) {
                Notification.warning(String.format(loc.getValue(L.MSG_MERGE_SKIPPED), result.skipped().size(),
                        result.skipped().stream().map(skipped -> skipped.label() + " (" + skipped.reason() + ")").collect(Collectors.joining("; "))));
            }
            close();
            onChange.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // the rules of the service, its text says what; the book may have changed under the review
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            load();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    void discard() {
        try {
            agentRunService.stop(run, false);
            close();
            onChange.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests of the dialog

    Grid<Item> getGrid() {
        return grid;
    }

    Button getApplyButton() {
        return applyButton;
    }

    Button getDiscardButton() {
        return discardButton;
    }

    VerticalLayout getDetail() {
        return detail;
    }

    Checkbox getBranchBox() {
        return branchBox;
    }

    Span getSummaryLabel() {
        return summaryLabel;
    }

    MergePlan getPlan() {
        return plan;
    }

    Item getFocused() {
        return focused;
    }

    Map<String, Map<String, String>> getEdits() {
        return edits;
    }
}
