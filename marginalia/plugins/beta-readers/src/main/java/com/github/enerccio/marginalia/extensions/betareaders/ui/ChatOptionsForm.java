package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.model.ReplyFormat;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * What a chat may choose for itself instead of inheriting it: the medium, the model, the protocol and the format of the replies. The
 * fields are empty for "inherited" and say what that is. Used by the window that starts a chat and the one that changes its settings.
 */
public class ChatOptionsForm extends VerticalLayout {

    /**
     * What the chat gets when it chooses nothing, as the names shown in the empty fields ("Inherited: ...").
     */
    public record Inherited(String medium, String ai, String protocol) {
    }

    final ComboBox<Medium> medium = new ComboBox<>(T.localized("Medium"));
    final ComboBox<Choice> ai = new ComboBox<>(T.localized("Model"));
    final ComboBox<Choice> protocol = new ComboBox<>(T.localized("Protocol"));
    final Select<ReplyFormat> format = new Select<>();

    private final Inherited inherited;

    public ChatOptionsForm(List<Medium> mediums, List<Choice> ais, List<Choice> protocols, Inherited inherited) {
        this.inherited = inherited;
        setPadding(false);

        medium.setItems(mediums);
        medium.setItemLabelGenerator(Medium::getName);
        medium.setPlaceholder(inheritedText(inherited.medium()));
        ai.setItems(ais);
        ai.setItemLabelGenerator(Choice::label);
        ai.setPlaceholder(inheritedText(inherited.ai()));
        protocol.setItems(protocols);
        protocol.setItemLabelGenerator(Choice::label);
        protocol.setPlaceholder(inheritedText(inherited.protocol()));
        for (ComboBox<?> combo : List.of(medium, ai, protocol)) {
            combo.setClearButtonVisible(true);
            combo.setWidthFull();
        }

        format.setLabel(T.localized("Reply format"));
        format.setItems(ReplyFormat.AUTO, ReplyFormat.JSON, ReplyFormat.TEXT);
        format.setItemLabelGenerator(ChatOptionsForm::label);
        format.setValue(ReplyFormat.AUTO);
        format.setWidthFull();
        format.setHelperText(T.localized("Automatic asks for JSON when the model can follow a schema, free text otherwise"));

        add(medium, ai, protocol, format);
    }

    private static String inheritedText(String value) {
        return T.localized("Inherited: %s").formatted(value == null || value.isBlank() ? T.localized("none") : value);
    }

    private static String label(ReplyFormat format) {
        return switch (format) {
            case AUTO -> T.localized("Automatic");
            case JSON -> T.localized("JSON");
            case TEXT -> T.localized("Free text");
        };
    }

    /**
     * Shows the choices the chat has now.
     */
    public void load(BetaChat chat) {
        medium.setValue(medium.getListDataView().getItems().filter(item -> item.getId().equals(chat.getMedium())).findFirst().orElse(null));
        ai.setValue(ai.getListDataView().getItems().filter(item -> item.id().equals(chat.getAi())).findFirst().orElse(null));
        protocol.setValue(protocol.getListDataView().getItems().filter(item -> item.id().equals(chat.getProtocol())).findFirst().orElse(null));
        format.setValue(chat.getFormat());
    }

    /** The id of the medium chosen; null for the inherited one. */
    public String mediumId() {
        return medium.getValue() == null ? null : medium.getValue().getId();
    }

    /** The name of the medium the chat reads the text as with this choice. */
    public String mediumName() {
        return medium.getValue() != null ? medium.getValue().getName() : inherited.medium();
    }

    public String aiId() {
        return ai.getValue() == null ? null : ai.getValue().id();
    }

    public String protocolId() {
        return protocol.getValue() == null ? null : protocol.getValue().id();
    }

    public ReplyFormat replyFormat() {
        return format.getValue() == null ? ReplyFormat.AUTO : format.getValue();
    }
}
