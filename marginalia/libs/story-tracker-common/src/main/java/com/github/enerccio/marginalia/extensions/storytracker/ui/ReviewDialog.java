package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerLocalization.*;

/**
 * What the model proposed, a table of changes grouped (by tracked character): apply or not, a label (what kind of change), a body the
 * plugin makes (the text, editable before it is applied), the quote from the part that shows it and which part it is in. Apply applies
 * the checked rows; Cancel stops the run and leaves everything as it was. The dialog is opened by the user, never by itself.
 */
public class ReviewDialog extends Dialog {

    /** One change, as the plugin shows it. */
    public interface Row {
        /** The key of the group (the id of the character); the rows of a group keep the order they are given in. */
        String group();

        /** The heading of the group (the name of the character). */
        String groupTitle();

        boolean isApply();

        void setApply(boolean apply);

        /** What kind of change it is (Add, Retract, Set, Remove...). */
        String label();

        /** Whether the label is shown as taking something away. */
        boolean negative();

        /** What the change is, made by the plugin; it may hold fields that edit the change before it is applied. */
        Component body();

        /** The quote of the part (or, in a tidy, the reason). */
        String evidence();

        /** The number of the part the quote is in, 0 for none. */
        int messageNumber();

        /** Whether the quote was found in the part. */
        boolean evidenceFound();
    }

    /**
     * @param title     the title of the dialog
     * @param note      what the user should know about the run, may be blank
     * @param rows      the changes
     * @param reasoning what the model thought first, may be blank
     * @param hint      what happens to an unchecked row
     * @param dropped   the answers that were left out, one line each
     * @param tidy      the rows are of a tidy (the evidence is a reason, not a quote)
     * @param onApply   called after Apply, the rows carry the choices
     * @param onCancel  called after Cancel
     */
    public ReviewDialog(String title, String note, List<? extends Row> rows, String reasoning, String hint, List<String> dropped, boolean tidy,
                        Runnable onApply, Runnable onCancel) {
        setHeaderTitle(title);
        setWidth("900px");
        setMaxWidth("96vw");
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.addClassName(StoryTrackerStyles.REVIEW);
        if (note != null && !note.isBlank()) {
            content.add(counts(note));
        }

        // grouped, in the order of the answer
        Map<String, List<Row>> groups = new LinkedHashMap<>();
        for (Row row : rows) {
            groups.computeIfAbsent(row.group(), _ -> new ArrayList<>()).add(row);
        }
        for (List<Row> group : groups.values()) {
            content.add(createGroup(group, tidy));
        }

        if (reasoning != null && !reasoning.isBlank()) {
            // what the model thought before it answered: to judge a change, collapsed because it is long
            Div thought = new Div(new Span(reasoning));
            thought.addClassName(StoryTrackerStyles.REASONING);
            content.add(new Details(T.localized(WHAT_THE_MODEL_THOUGHT), thought));
        }

        if (hint != null && !hint.isBlank()) {
            content.add(counts(hint));
        }
        if (dropped != null && !dropped.isEmpty()) {
            VerticalLayout lines = new VerticalLayout();
            lines.setPadding(false);
            lines.setSpacing(false);
            dropped.forEach(line -> lines.add(counts(line)));
            content.add(new Details(T.localized(LEFT_OUT_N).formatted(dropped.size()), lines));
        }
        add(content);

        Button apply = new Button(T.localized(APPLY), _ -> {
            close();
            onApply.run();
        });
        apply.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(T.localized(CANCEL), _ -> {
            close();
            onCancel.run();
        });
        getFooter().add(cancel, apply);
    }

    private static Div counts(String text) {
        Div div = new Div(new Span(text));
        div.addClassName(StoryTrackerStyles.COUNTS);
        return div;
    }

    private Div createGroup(List<Row> group, boolean tidy) {
        Div box = new Div();
        box.addClassName(StoryTrackerStyles.REVIEW_GROUP);

        Div name = new Div(new Span(group.getFirst().groupTitle() + " (" + group.size() + ")"));
        name.addClassNames(StoryTrackerStyles.NAME, StoryTrackerStyles.CARD_TITLE);
        List<Checkbox> boxes = new ArrayList<>();
        Button all = new Button(T.localized(CHECK_ALL), _ -> boxes.forEach(b -> b.setValue(true)));
        Button none = new Button(T.localized(UNCHECK_ALL), _ -> boxes.forEach(b -> b.setValue(false)));
        all.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        none.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        Div header = new Div(name, all, none);
        header.addClassName(StoryTrackerStyles.CARD_HEADER);
        box.add(header);

        for (Row row : group) {
            box.add(createRow(row, boxes, tidy));
        }
        return box;
    }

    private Div createRow(Row change, List<Checkbox> boxes, boolean tidy) {
        Checkbox apply = new Checkbox(change.isApply());
        apply.setAriaLabel(T.localized(APPLY));
        apply.addValueChangeListener(e -> change.setApply(e.getValue()));
        boxes.add(apply);

        Span type = new Span(change.label());
        type.addClassName(StoryTrackerStyles.KIND);
        if (change.negative()) {
            type.addClassName(StoryTrackerStyles.KIND_NOT);
        }

        Div body = new Div(change.body());
        body.addClassName(StoryTrackerStyles.REVIEW_BODY);
        String evidenceText = tidy || change.messageNumber() <= 0 ? T.localized(WHY).formatted(change.evidence())
                : T.localized(IN_PART_N_QUOTE).formatted(change.messageNumber(), change.evidence());
        body.add(counts(evidenceText));
        if (!tidy && !change.evidenceFound()) {
            Div warning = new Div(new Span(T.localized(THE_QUOTE_IS_NOT_IN_THE_PART)));
            warning.addClassName(StoryTrackerStyles.STALE);
            body.add(warning);
        }

        Div row = new Div(apply, type, body);
        row.addClassName(StoryTrackerStyles.REVIEW_ROW);
        return row;
    }
}
