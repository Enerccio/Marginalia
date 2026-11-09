package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Available;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Scope;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * Starts a chat: the kind (a chat with one persona, or a group, decided here for good), the personas, and what a chat may choose
 * for itself instead of inheriting it - the medium, the model, the protocol and the format of the replies. The dialog knows no
 * service: it gets what to choose from, and hands the new chat to the caller.
 */
public class NewChatDialog extends Dialog {

    private static final int TITLE_LENGTH = 40;

    private final Consumer<BetaChat> onCreate;

    final RadioButtonGroup<ChatKind> kind = new RadioButtonGroup<>();
    final TextField title = new TextField(T.localized(TITLE));
    final RadioButtonGroup<Available> singlePersona = new RadioButtonGroup<>(T.localized(PERSONA));
    final CheckboxGroup<Available> groupPersonas = new CheckboxGroup<>(T.localized(PERSONAS));
    final ChatOptionsForm options;
    final Button create = new Button(T.localized(CREATE));

    /**
     * @param personas the personas to choose from (the ones of the book and of the library)
     * @param onCreate told with the new chat, which has copies of the personas chosen
     */
    public NewChatDialog(List<Available> personas, List<Medium> mediums, List<Choice> ais, List<Choice> protocols,
                         ChatOptionsForm.Inherited inherited, Consumer<BetaChat> onCreate) {
        this.onCreate = onCreate;
        setHeaderTitle(T.localized(NEW_CHAT));
        setWidth("460px");
        setMaxWidth("96vw");
        setCloseOnOutsideClick(false);

        kind.setLabel(T.localized(KIND));
        kind.setItems(ChatKind.SINGLE, ChatKind.GROUP);
        kind.setItemLabelGenerator(chatKind -> chatKind == ChatKind.SINGLE ? T.localized(ONE_PERSONA) : T.localized(A_GROUP));
        kind.setValue(ChatKind.GROUP);
        kind.addValueChangeListener(_ -> showPersonaChoice());

        Div kindHint = new Div(T.localized(A_CHAT_WITH_ONE_PERSONA_STAYS));
        kindHint.addClassName(BetaReadersStyles.HINT);

        title.setWidthFull();
        title.setPlaceholder(T.localized(EMPTY_THE_NAMES));
        title.setMaxLength(120);

        singlePersona.setItems(personas);
        singlePersona.setItemLabelGenerator(NewChatDialog::label);
        singlePersona.addThemeVariants(com.vaadin.flow.component.radiobutton.RadioGroupVariant.LUMO_VERTICAL);
        groupPersonas.setItems(personas);
        groupPersonas.setItemLabelGenerator(NewChatDialog::label);
        groupPersonas.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        options = new ChatOptionsForm(mediums, ais, protocols, inherited);

        // what a chat may choose for itself is out of the way: most chats inherit all of it
        Details optionsSection = new Details(T.localized(STORY_MEDIUM_MODEL_AND_FORMAT), options);

        VerticalLayout content = new VerticalLayout(kind, kindHint, title, singlePersona, groupPersonas, optionsSection);
        content.setPadding(false);
        add(content);
        showPersonaChoice();

        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        create.addClickListener(_ -> create());
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, create);
        DialogShortcuts.submitWith(create, title);
        DialogFocus.onOpen(this, title);
    }

    private void showPersonaChoice() {
        boolean single = kind.getValue() == ChatKind.SINGLE;
        singlePersona.setVisible(single);
        groupPersonas.setVisible(!single);
    }

    private static String label(Available available) {
        String name = available.persona().getName();
        return available.scope() == Scope.BOOK ? name + " (" + T.localized(THIS_BOOK) + ")" : name;
    }

    /**
     * The personas the user ticked, or the one picked for a chat with one persona.
     */
    private List<Available> chosen() {
        if (kind.getValue() == ChatKind.SINGLE) {
            return singlePersona.getValue() == null ? List.of() : List.of(singlePersona.getValue());
        }
        return groupPersonas.getSelectedItems().stream().sorted(java.util.Comparator.comparingInt(a -> indexOf(a))).toList();
    }

    private int indexOf(Available available) {
        return groupPersonas.getListDataView().getItems().toList().indexOf(available);
    }

    private void create() {
        String problem = options.problem();
        if (problem != null) {
            Notification.warning(problem);
            return;
        }
        List<Available> personas = chosen();
        if (personas.isEmpty()) {
            Notification.warning(kind.getValue() == ChatKind.SINGLE ? T.localized(CHOOSE_A_PERSONA) : T.localized(CHOOSE_AT_LEAST_ONE_PERSONA));
            return;
        }
        String name = title.getValue() == null ? "" : title.getValue().strip();
        if (name.isEmpty()) {
            name = defaultTitle(personas.stream().map(available -> available.persona().getName()).toList());
        }
        BetaChat chat = new BetaChat(name, kind.getValue());
        options.apply(chat);
        personas.forEach(available -> chat.addPersona(available.persona()));
        close();
        onCreate.accept(chat);
    }

    /**
     * The title of a chat that was given none: the names of its personas, shortened.
     */
    static String defaultTitle(List<String> names) {
        String joined = names.stream().collect(Collectors.joining(", "));
        return joined.length() <= TITLE_LENGTH ? joined : joined.substring(0, TITLE_LENGTH - 1).stripTrailing() + "…";
    }
}
