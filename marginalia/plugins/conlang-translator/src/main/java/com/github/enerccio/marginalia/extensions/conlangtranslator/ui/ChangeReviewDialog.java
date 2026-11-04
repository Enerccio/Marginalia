package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Audience;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryKind;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * The window in which the differences between two languages are reviewed (an import that meets a language, the comparison with a copy, later the
 * proposals of the designer and the work of an agent): the changes in a list with a check box each, grouped by what they are about, and for the
 * selected change its fields as they are here, as the other language has them and as they will be written, which the user may edit. A field in
 * conflict keeps our value until the user takes theirs.
 */
public class ChangeReviewDialog extends Dialog {

    private static final String ALL = "";

    private final ChangeSet set;
    private final ListDataProvider<Change> data;
    private final Grid<Change> grid = new Grid<>();
    private final Span summary = new Span();
    private final VerticalLayout details = new VerticalLayout();
    private final Button apply = new Button();
    private final Div note = new Div();

    /**
     * @param title   what the window says
     * @param set     the changes; the picks and the values of the fields are changed in it
     * @param onApply told with the set after <i>Apply</i>; it writes the picked changes ({@code LanguageDiff.apply})
     */
    public ChangeReviewDialog(String title, ChangeSet set, Consumer<ChangeSet> onApply) {
        this.set = set;
        this.data = new ListDataProvider<>(set.changes());

        setHeaderTitle(title);
        setWidth("1240px");
        setHeight("86vh");
        setMaxWidth("98vw");
        setMaxHeight("98vh");
        setCloseOnOutsideClick(false);
        setResizable(true);
        setDraggable(true);

        summary.addClassName(ConlangStyles.HINT);
        Button all = new Button(T.localized(SELECT_ALL), _ -> pick(Mode.ALL));
        Button none = new Button(T.localized(SELECT_NONE), _ -> pick(Mode.NONE));
        Button clean = new Button(T.localized(SELECT_WITHOUT_CONFLICTS), _ -> pick(Mode.CLEAN));
        ComboBox<String> only = new ComboBox<>();
        only.setItems(ALL, Target.WORD.name(), Target.RULE.name(), Target.CATEGORY.name(), Target.REGISTER.name(), Target.LANGUAGE.name());
        only.setItemLabelGenerator(value -> value.equals(ALL) ? T.localized(EVERYTHING) : T.localized(targetLabel(Target.valueOf(value))));
        only.setValue(ALL);
        only.setAllowCustomValue(false);
        only.addValueChangeListener(event -> {
            String chosen = event.getValue();
            data.setFilter(change -> chosen == null || chosen.equals(ALL) || change.target().name().equals(chosen));
        });
        HorizontalLayout bar = new HorizontalLayout(summary, all, none, clean, only);
        bar.setWidthFull();
        bar.setWrap(true);
        bar.setAlignItems(FlexComponent.Alignment.CENTER);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        grid.setDataProvider(data);
        grid.addComponentColumn(change -> {
            Checkbox box = new Checkbox(change.isPicked());
            box.addValueChangeListener(event -> {
                set.pick(change, event.getValue());
                grid.getDataProvider().refreshAll();
                refreshSummary();
            });
            return box;
        }).setHeader("").setFlexGrow(0).setWidth("4rem");
        grid.addComponentColumn(change -> {
            Span badge = new Span(T.localized(kindLabel(change.kind())));
            badge.addClassName(ConlangStyles.BADGE);
            return badge;
        }).setHeader(T.localized(CHANGE)).setFlexGrow(0).setWidth(phoneWidth());
        boolean phone = UIUtils.isPhone();
        if (phone) {
            // a narrow screen: what it is, and a mark where there is a conflict; the rest is in the details below
            grid.addColumn(Change::label).setHeader(T.localized(NAME)).setFlexGrow(1);
            grid.addComponentColumn(change -> {
                Span mark = new Span(change.hasConflict() ? "!" : "");
                mark.addClassName(ConlangStyles.CONFLICT);
                return mark;
            }).setHeader("!").setFlexGrow(0).setWidth("3rem");
        } else {
            grid.addColumn(change -> T.localized(targetLabel(change.target()))).setHeader(T.localized(ABOUT)).setFlexGrow(0).setWidth("9rem");
            grid.addColumn(Change::label).setHeader(T.localized(NAME)).setFlexGrow(2);
            grid.addColumn(change -> change.fields().stream().map(field -> fieldLabel(field.field())).collect(Collectors.joining(", ")))
                    .setHeader(T.localized(FIELDS)).setFlexGrow(2);
            grid.addComponentColumn(change -> {
                Span mark = new Span(conflictText(change));
                mark.addClassName(ConlangStyles.CONFLICT);
                return mark;
            }).setHeader(T.localized(CONFLICT_TEXT)).setFlexGrow(2);
        }
        grid.getColumns().forEach(column -> column.setResizable(true));
        grid.addSelectionListener(_ -> showDetails(grid.getSelectedItems().stream().findFirst().orElse(null)));

        details.setPadding(true);
        details.setSpacing(true);
        details.setWidthFull();
        Div detailsScroll = new Div(details);
        detailsScroll.setSizeFull();
        detailsScroll.addClassName(ConlangStyles.SCROLL);

        SplitLayout split = new SplitLayout(grid, detailsScroll);
        split.setOrientation(SplitLayout.Orientation.VERTICAL);
        split.setSplitterPosition(45);
        split.setSizeFull();

        note.addClassName(ConlangStyles.DIFF_TEXT);
        note.setVisible(false);
        VerticalLayout content = new VerticalLayout(note, bar, split);
        content.setPadding(false);
        content.setSizeFull();
        content.setFlexGrow(1, split);
        add(content);

        apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        apply.addClickListener(_ -> {
            close();
            onApply.accept(set);
        });
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), apply);
        refreshSummary();
        showDetails(null);
    }

    /**
     * Something to read above the changes: what the designer says about them, and what it proposed that was left out.
     */
    public ChangeReviewDialog withNote(String text) {
        note.setText(text == null ? "" : text);
        note.setVisible(text != null && !text.isBlank());
        return this;
    }

    private static String phoneWidth() {
        return UIUtils.isPhone() ? "5.5rem" : "7rem";
    }

    private enum Mode {
        ALL, NONE, CLEAN
    }

    private void pick(Mode mode) {
        switch (mode) {
            case ALL -> set.pickAll();
            case NONE -> set.pickNone();
            case CLEAN -> set.pickClean();
        }
        data.refreshAll();
        refreshSummary();
    }

    private void refreshSummary() {
        summary.setText(T.localized(D_CHANGES_D_IN_CONFLICT).formatted(set.changes().size(), set.conflicts()));
        apply.setText(T.localized(APPLY_D_PICKED).formatted(set.pickedCount()));
        apply.setEnabled(set.pickedCount() > 0);
    }

    // ---- the selected change

    private void showDetails(Change change) {
        details.removeAll();
        if (change == null) {
            Span hint = new Span(T.localized(SELECT_A_CHANGE_TO_SEE_ITS_FIELDS));
            hint.addClassName(ConlangStyles.HINT);
            details.add(hint);
            return;
        }
        Span title = new Span(T.localized(targetLabel(change.target())) + ": " + change.label());
        title.addClassName(ConlangStyles.WORD);
        details.add(title);
        if (change.conflict() != null) {
            Span why = new Span(conflictText(change));
            why.addClassName(ConlangStyles.CONFLICT);
            details.add(why);
        }
        if (!change.dependsOn().isEmpty()) {
            Span needs = new Span(T.localized(NEEDS_THE_CHANGE_OF_ANOTHER_THING));
            needs.addClassName(ConlangStyles.HINT);
            details.add(needs);
        }
        if (change.kind() == Change.Kind.DELETE) {
            Span hint = new Span(T.localized(THE_THING_IS_DELETED));
            hint.addClassName(ConlangStyles.HINT);
            details.add(hint);
        } else if (change.kind() == Change.Kind.MOVE) {
            Span hint = new Span(T.localized(THE_THING_IS_MOVED));
            hint.addClassName(ConlangStyles.HINT);
            details.add(hint);
        }
        for (FieldChange field : change.fields()) {
            details.add(fieldBox(change, field));
        }
    }

    /** The fields whose text the user may write; the others (a kind, a status, a link, a list) are taken or left. */
    private static final java.util.Set<String> EDITABLE = java.util.Set.of("description", "title", "text", "note", "gender", "voice", "word", "script",
            "rationale", "rule", "notes", "name");

    private Component fieldBox(Change change, FieldChange field) {
        boolean adding = change.kind() == Change.Kind.ADD;
        VerticalLayout box = new VerticalLayout();
        box.addClassName(ConlangStyles.DIFF_FIELD);
        box.setPadding(false);
        box.setSpacing(false);

        boolean editable = EDITABLE.contains(field.field()) && !field.isList();
        TextArea value = new TextArea(adding ? fieldLabel(field.field()) : T.localized(WILL_BE_WRITTEN));
        value.setWidthFull();
        value.setMinHeight("2.5rem");
        value.setValue(editable ? field.value() : shown(field.field(), field.value()));
        if (editable) {
            value.addValueChangeListener(event -> field.setValue(event.getValue()));
        } else {
            value.setReadOnly(true);
        }
        if (adding) {
            // an add has nothing here to compare with: the field is its value
            box.add(value);
            return box;
        }

        Span name = new Span(fieldLabel(field.field()) + (field.isConflict() ? " – " + T.localized(BOTH_CHANGED_THIS) : ""));
        name.addClassName(ConlangStyles.WORD);
        if (field.isConflict()) {
            name.addClassName(ConlangStyles.CONFLICT);
        }
        Div columns = new Div();
        columns.addClassName(ConlangStyles.DIFF_COLUMNS);
        columns.add(valueBlock(T.localized(HERE_NOW), shown(field.field(), field.now())),
                valueBlock(T.localized(IN_THE_OTHER_LANGUAGE), shown(field.field(), field.after())));
        box.add(name, columns);
        if (field.isConflict() || field.isList() || !editable) {
            Runnable refresh = () -> value.setValue(editable ? field.value() : shown(field.field(), field.value()));
            Button mine = new Button(T.localized(KEEP_MINE), _ -> {
                field.setValue(field.now());
                refresh.run();
            });
            Button theirs = new Button(T.localized(TAKE_THEIRS), _ -> {
                field.setValue(field.after());
                refresh.run();
            });
            if (field.isConflict()) {
                box.add(new HorizontalLayout(mine, theirs));
            }
        }
        box.add(value);
        return box;
    }

    private static Component valueBlock(String heading, String text) {
        Span title = new Span(heading);
        title.addClassName(ConlangStyles.HINT);
        Div body = new Div(new Span(text));
        body.addClassName(ConlangStyles.DIFF_TEXT);
        Div block = new Div(title, body);
        return block;
    }

    // ---- the words of the review

    private static String conflictText(Change change) {
        Conflict conflict = change.conflict();
        if (conflict == null) {
            return change.fields().stream().anyMatch(FieldChange::isConflict) ? T.localized(SOME_FIELDS_CHANGED_ON_BOTH_SIDES) : "";
        }
        return switch (conflict) {
            case CHANGED_HERE_DELETED_THERE -> T.localized(CHANGED_HERE_DELETED_THERE_TEXT);
            case DELETED_HERE_CHANGED_THERE -> T.localized(DELETED_HERE_CHANGED_THERE_TEXT);
            case SAME_WORD_EXISTS -> T.localized(THE_SAME_WORD_IS_HERE);
            case BOTH_MOVED -> T.localized(MOVED_ON_BOTH_SIDES);
        };
    }

    static String targetLabel(Target target) {
        return switch (target) {
            case LANGUAGE -> TARGET_LANGUAGE;
            case RULE -> TARGET_RULE;
            case CATEGORY -> TARGET_CATEGORY;
            case REGISTER -> TARGET_REGISTER;
            case WORD -> TARGET_WORD;
        };
    }

    private static String kindLabel(Change.Kind kind) {
        return switch (kind) {
            case ADD -> KIND_ADD;
            case CHANGE -> KIND_CHANGE;
            case DELETE -> KIND_DELETE;
            case MOVE -> KIND_MOVE;
        };
    }

    /** The name of a field of the model as the user knows it. */
    static String fieldLabel(String field) {
        return T.localized(switch (field) {
            case "name" -> NAME;
            case "description" -> DESCRIPTION;
            case "hyphenStructural" -> A_HYPHEN_IS_PART_OF_A_WORD;
            case "title" -> TITLE;
            case "audience" -> READ_BY;
            case "text" -> TEXT;
            case "note" -> NOTE;
            case "alwaysSend" -> ALWAYS_SENT;
            case "word" -> WORD;
            case "script" -> IN_THE_SCRIPT;
            case "category" -> CATEGORY;
            case "kind" -> KIND;
            case "register" -> REGISTER;
            case "status" -> STATUS;
            case "rationale" -> WHY_THE_WORD_IS_AS_IT_IS;
            case "rule" -> RULE_IT_FOLLOWS;
            case "notes" -> NOTES;
            case "senses" -> MEANINGS;
            case "variants" -> OTHER_FORMS_OF_THE_SAME_CONCEPT;
            default -> field;
        });
    }

    /** What the user reads for the raw value of an enum or a boolean; any other value comes back as it is. */
    static String valueText(String field, String raw) {
        try {
            return switch (field) {
                case "kind" -> T.localized(EntryDialog.kindLabel(EntryKind.valueOf(raw)));
                case "status" -> T.localized(EntryDialog.statusLabel(EntryStatus.valueOf(raw)));
                case "audience" -> T.localized(LanguageEditorDialog.audienceLabel(Audience.valueOf(raw)));
                case "hyphenStructural", "alwaysSend" -> Boolean.parseBoolean(raw) ? T.localized(YES_TEXT) : T.localized(NO_TEXT);
                default -> raw;
            };
        } catch (IllegalArgumentException e) {
            return raw;
        }
    }

    /** The value of a field as the user reads it: an enum or a boolean in words, a link by the name of what it links to. */
    private String shown(String field, String raw) {
        String words = valueText(field, raw);
        return words.equals(raw) ? set.display(field, raw) : words;
    }
}
