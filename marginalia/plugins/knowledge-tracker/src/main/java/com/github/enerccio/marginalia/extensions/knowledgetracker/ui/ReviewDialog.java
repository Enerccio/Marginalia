package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.Change;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionService.Proposal;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * What the model proposed, a table of changes grouped by tracked character: apply or not, retract or add, the text (an
 * addition is editable before it is applied), the quote from the part that shows it and which part it is in. Apply applies the
 * checked rows; Cancel stops the run and leaves everything as it was, the same parts are proposed again next time.
 * The dialog is opened by the user from the progress panel, never by itself.
 */
public class ReviewDialog extends Dialog {

    /**
     * @param run     the number of the run, for the title
     * @param onApply called after Apply, the changes carry the choices
     * @param onCancel called after Cancel
     */
    public ReviewDialog(int run, Proposal proposal, Runnable onApply, Runnable onCancel) {
        setHeaderTitle(proposal.tidy() ? T.localized(REVIEW_THE_TIDY) : T.localized(REVIEW_THE_CHANGES_RUN).formatted(run));
        setWidth("900px");
        setMaxWidth("96vw");
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.addClassName(KnowledgeTrackerStyles.REVIEW);
        if (!proposal.note().isBlank()) {
            Div note = new Div(new Span(proposal.note()));
            note.addClassName(KnowledgeTrackerStyles.COUNTS);
            content.add(note);
        }

        // grouped by character, in the order of the answer
        Map<String, List<Change>> groups = new LinkedHashMap<>();
        for (Change change : proposal.changes().changes()) {
            groups.computeIfAbsent(change.getCharacterId(), _ -> new java.util.ArrayList<>()).add(change);
        }
        for (List<Change> group : groups.values()) {
            content.add(createGroup(group));
        }

        if (!proposal.reasoning().isBlank()) {
            // what the model thought before it answered: to judge a change, collapsed because it is long
            Div thought = new Div(new Span(proposal.reasoning()));
            thought.addClassName(KnowledgeTrackerStyles.REASONING);
            content.add(new Details(T.localized(WHAT_THE_MODEL_THOUGHT), thought));
        }

        Div hint = new Div(new Span(proposal.tidy() ? T.localized(AN_UNCHECKED_TIDY_CHANGE_LEAVES_THE_ITEMS) : T.localized(UNCHECKED_ADDITIONS_ARE_KEPT)));
        hint.addClassName(KnowledgeTrackerStyles.COUNTS);
        content.add(hint);
        if (!proposal.changes().dropped().isEmpty()) {
            VerticalLayout dropped = new VerticalLayout();
            dropped.setPadding(false);
            dropped.setSpacing(false);
            proposal.changes().dropped().forEach(line -> {
                Div row = new Div(new Span(line));
                row.addClassName(KnowledgeTrackerStyles.COUNTS);
                dropped.add(row);
            });
            content.add(new Details(T.localized(LEFT_OUT_N).formatted(proposal.changes().dropped().size()), dropped));
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

    private Div createGroup(List<Change> group) {
        Div box = new Div();
        box.addClassName(KnowledgeTrackerStyles.REVIEW_GROUP);

        Div name = new Div(new Span(group.getFirst().getCharacterName() + " (" + group.size() + ")"));
        name.addClassName(KnowledgeTrackerStyles.NAME);
        List<Checkbox> boxes = new java.util.ArrayList<>();
        Button all = new Button(T.localized(CHECK_ALL), _ -> boxes.forEach(b -> b.setValue(true)));
        Button none = new Button(T.localized(UNCHECK_ALL), _ -> boxes.forEach(b -> b.setValue(false)));
        all.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        none.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        Div header = new Div(name, all, none);
        header.addClassName(KnowledgeTrackerStyles.CARD_HEADER);
        name.addClassName(KnowledgeTrackerStyles.CARD_TITLE);
        box.add(header);

        for (Change change : group) {
            box.add(createRow(change, boxes));
        }
        return box;
    }

    private Div createRow(Change change, List<Checkbox> boxes) {
        Checkbox apply = new Checkbox(change.isApply());
        apply.setAriaLabel(T.localized(APPLY));
        apply.addValueChangeListener(e -> change.setApply(e.getValue()));
        boxes.add(apply);

        Span type = new Span(change.getType() == Change.Type.RETRACT ? T.localized(RETRACT) : T.localized(ADD));
        type.addClassName(KnowledgeTrackerStyles.KIND);
        if (change.getType() == Change.Type.RETRACT) {
            type.addClassName(KnowledgeTrackerStyles.KIND_NOT);
        }

        Div body = new Div();
        body.addClassName(KnowledgeTrackerStyles.REVIEW_BODY);
        if (change.getType() == Change.Type.ADD) {
            Select<ItemKind> kind = new Select<>();
            kind.setItems(ItemKind.KNOWS, ItemKind.DOES_NOT_KNOW);
            kind.setItemLabelGenerator(k -> k == ItemKind.KNOWS ? T.localized(KNOWS) : T.localized(DOES_NOT_KNOW));
            kind.setValue(change.getKind());
            kind.addClassName(KnowledgeTrackerStyles.REVIEW_KIND);
            kind.addValueChangeListener(e -> {
                if (e.getValue() != null) {
                    change.setKind(e.getValue());
                }
            });
            TextField text = new TextField();
            text.setWidthFull();
            text.setValue(change.getText());
            text.addValueChangeListener(e -> change.setText(e.getValue()));
            Div line = new Div(kind, text);
            line.addClassName(KnowledgeTrackerStyles.REVIEW_ROW);
            body.add(line);
        } else {
            Div old = new Div(new Span((change.getKind() == ItemKind.KNOWS ? T.localized(KNOWS) : T.localized(DOES_NOT_KNOW))
                    + ": " + change.getItemText()));
            old.addClassName(KnowledgeTrackerStyles.OLD_ITEM);
            body.add(old);
        }
        Div evidence = new Div(new Span(change.isTidy() ? T.localized(WHY).formatted(change.getEvidence())
                : T.localized(IN_PART_N_QUOTE).formatted(change.getMessageNumber(), change.getEvidence())));
        evidence.addClassName(KnowledgeTrackerStyles.COUNTS);
        body.add(evidence);
        if (!change.isEvidenceFound()) {
            Div warning = new Div(new Span(T.localized(THE_QUOTE_IS_NOT_IN_THE_PART)));
            warning.addClassName(KnowledgeTrackerStyles.STALE);
            body.add(warning);
        }

        Div row = new Div(apply, type, body);
        row.addClassName(KnowledgeTrackerStyles.REVIEW_ROW);
        return row;
    }
}
