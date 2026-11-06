package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerPrompts;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.DesignerService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * A conversation with the designer about a language: the author asks, the designer answers with the language as it is; an answer that comes
 * with new words or sections has a button that opens them as a proposal to review. The conversation is kept only while the window is open.
 */
public class AskDialog extends Dialog {

    private final Language language;
    private final Runnable changed;
    private final DesignerService service = new DesignerService();
    private final List<String> history = new ArrayList<>();
    private final Div messages = new Div();
    private final TextArea input = new TextArea();
    private final Button send = new Button(T.localized(SEND));

    public AskDialog(Language language, Runnable changed) {
        this.language = language;
        this.changed = changed;

        setHeaderTitle(T.localized(ASK_THE_DESIGNER));
        setWidth("820px");
        setHeight("78vh");
        setMaxWidth("96vw");
        setMaxHeight("96vh");
        setCloseOnOutsideClick(false);
        setResizable(true);
        setDraggable(true);

        messages.addClassName(ConlangStyles.SCROLL);
        messages.addClassName(ConlangStyles.FILL);
        messages.setWidthFull();
        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        messages.add(list);
        this.list = list;

        input.setWidthFull();
        input.setMinHeight("5rem");
        input.setPlaceholder(T.localized(ASK_ABOUT_THE_LANGUAGE));
        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        send.addClickListener(_ -> ask());
        DialogShortcuts.submitWith(send, input);
        DialogFocus.onOpen(this, input);
        HorizontalLayout bar = new HorizontalLayout(input, send);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.END);
        bar.setFlexGrow(1, input);

        VerticalLayout content = new VerticalLayout(messages, bar);
        content.setPadding(false);
        content.setSizeFull();
        content.setFlexGrow(1, messages);
        add(content);
        getFooter().add(new Button(T.localized(CLOSE), _ -> close()));
    }

    private final VerticalLayout list;

    private void ask() {
        String question = input.getValue() == null ? "" : input.getValue().strip();
        if (question.isEmpty()) {
            return;
        }
        input.clear();
        say(ConlangStyles.CHAT_AUTHOR, question);
        Language snapshot = ConlangStore.copy(language);
        List<String> earlier = List.copyOf(history);
        history.add("Author: " + question);
        send.setEnabled(false);
        Designing.run(T.localized(THE_DESIGNER_IS_THINKING), () -> service.answer(DesignerPrompts.ask(snapshot, question, earlier, service.lore(snapshot))), answer -> {
            send.setEnabled(true);
            String reply = answer.message().isBlank() ? T.localized(NOTHING_FOUND) : answer.message();
            history.add("Designer: " + reply);
            Div box = say(ConlangStyles.CHAT_DESIGNER, reply);
            if (answer.hasChanges()) {
                Button review = new Button(T.localized(REVIEW_THE_PROPOSED_CHANGES), _ ->
                        Proposals.show(T.localized(ASK_THE_DESIGNER), language, service.proposal(ConlangStore.copy(language), answer), changed));
                list.add(review);
            }
            scrollDown(box);
        });
    }

    private Div say(String style, String text) {
        Div box = new Div(text);
        box.addClassName(style);
        list.add(box);
        return box;
    }

    private void scrollDown(Div last) {
        last.scrollIntoView();
    }

    /** Whether the answer is one a button should be offered for. */
    static boolean offersChanges(DesignerAnswer answer) {
        return answer.hasChanges();
    }
}
