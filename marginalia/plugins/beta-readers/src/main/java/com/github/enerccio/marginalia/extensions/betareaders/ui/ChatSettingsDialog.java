package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * Changes what an existing chat chooses for itself: the medium, the model, the protocol and the format of the replies. The kind of the
 * chat and its personas are not here (the kind never changes, the personas are in the panel of the chat). The caller applies the choice,
 * which takes effect with the next message.
 */
public class ChatSettingsDialog extends Dialog {

    final ChatOptionsForm options;
    final Button save = new Button(T.localized(SAVE));

    /**
     * @param onSave told with the form after <i>Save</i>
     */
    public ChatSettingsDialog(BetaChat chat, List<Medium> mediums, List<Choice> ais, List<Choice> protocols,
                              ChatOptionsForm.Inherited inherited, Consumer<ChatOptionsForm> onSave) {
        setHeaderTitle(T.localized(CHAT_SETTINGS));
        setWidth("460px");
        setMaxWidth("96vw");
        setCloseOnOutsideClick(false);

        options = new ChatOptionsForm(mediums, ais, protocols, inherited);
        options.load(chat);
        add(options);

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(_ -> {
            String problem = options.problem();
            if (problem != null) {
                Notification.warning(problem);
                return;
            }
            close();
            onSave.accept(options);
        });
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), save);
    }
}
