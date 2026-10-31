package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.model.ReplyFormat;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * What a chat may choose for itself instead of inheriting it: the medium, the model, the protocol and the format of the replies, and
 * how much of the story the personas read (the spoiler limit, the first part, the summaries). The fields are empty for "inherited" and
 * say what that is. Used by the window that starts a chat and the one that changes its settings.
 */
public class ChatOptionsForm extends VerticalLayout {

    /**
     * What the chat gets when it chooses nothing, as the names shown in the empty fields ("Inherited: ...").
     */
    public record Inherited(String medium, String ai, String protocol, String scene) {

        public Inherited(String medium, String ai, String protocol) {
            this(medium, ai, protocol, null);
        }
    }

    final ComboBox<Medium> medium = new ComboBox<>(T.localized("Medium"));
    final TextArea scene = new TextArea(T.localized("Scene"));
    final ComboBox<Choice> ai = new ComboBox<>(T.localized("Model"));
    final ComboBox<Choice> protocol = new ComboBox<>(T.localized("Protocol"));
    final Select<ReplyFormat> format = new Select<>();
    final IntegerField spoilerLimit = new IntegerField(T.localized("Personas read up to part"));
    final IntegerField fromPart = new IntegerField(T.localized("Start from part"));
    final Checkbox summaries = new Checkbox(T.localized("Use the summaries of the story"));

    private final Inherited inherited;

    public ChatOptionsForm(List<Medium> mediums, List<Choice> ais, List<Choice> protocols, Inherited inherited) {
        this.inherited = inherited;
        setPadding(false);

        medium.setItems(mediums);
        medium.setItemLabelGenerator(Medium::getName);
        medium.setPlaceholder(inheritedText(inherited.medium()));
        scene.setWidthFull();
        scene.setMinHeight("6rem");
        scene.setPlaceholder(inheritedText(shorten(inherited.scene())));
        scene.setHelperText(T.localized("Empty: inherited. Where and how the conversation takes place; it goes into the system prompt."));
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

        spoilerLimit.setMin(1);
        spoilerLimit.setStepButtonsVisible(true);
        spoilerLimit.setWidthFull();
        spoilerLimit.setHelperText(T.localized("Empty: the whole story. The personas never get a part after this one."));
        fromPart.setMin(1);
        fromPart.setStepButtonsVisible(true);
        fromPart.setWidthFull();
        fromPart.setHelperText(T.localized("Empty: from the first part. Earlier parts are not sent, not even as summaries."));
        summaries.setValue(true);

        add(spoilerLimit, fromPart, summaries, medium, scene, ai, protocol, format);
    }

    /**
     * What is wrong with the choices, or null when they are fine.
     */
    public String problem() {
        Integer first = fromPart.getValue();
        Integer last = spoilerLimit.getValue();
        if (first != null && last != null && first > last) {
            return T.localized("The first part can't come after the last one.");
        }
        return null;
    }

    private static String shorten(String text) {
        if (text == null) {
            return null;
        }
        String line = text.replaceAll("\\s+", " ").strip();
        return line.length() > 120 ? line.substring(0, 117) + "..." : line;
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
        scene.setValue(chat.getScene() == null ? "" : chat.getScene());
        ai.setValue(ai.getListDataView().getItems().filter(item -> item.id().equals(chat.getAi())).findFirst().orElse(null));
        protocol.setValue(protocol.getListDataView().getItems().filter(item -> item.id().equals(chat.getProtocol())).findFirst().orElse(null));
        format.setValue(chat.getFormat());
        spoilerLimit.setValue(chat.getSpoilerLimit() > 0 ? chat.getSpoilerLimit() : null);
        fromPart.setValue(chat.getFromPart() > 0 ? chat.getFromPart() : null);
        summaries.setValue(chat.isSummaries());
    }

    /**
     * Puts the choices into the chat: all of them, the medium and the scene too, which is right for a chat that is made now. A chat that
     * exists has them changed with {@code ChatEdits.changeMedium} and {@code changeScene}, which leave a line in the history.
     */
    public void apply(BetaChat chat) {
        chat.setMedium(mediumId());
        chat.setScene(sceneText());
        applyExceptHistoryLines(chat);
    }

    /**
     * Puts the choices into the chat but the medium and the scene.
     */
    public void applyExceptHistoryLines(BetaChat chat) {
        chat.setAi(aiId());
        chat.setProtocol(protocolId());
        chat.setFormat(replyFormat());
        chat.setSpoilerLimit(spoilerLimit.getValue() == null ? 0 : spoilerLimit.getValue());
        chat.setFromPart(fromPart.getValue() == null ? 0 : fromPart.getValue());
        chat.setSummaries(summaries.getValue());
    }

    /** The id of the medium chosen; null for the inherited one. */
    public String mediumId() {
        return medium.getValue() == null ? null : medium.getValue().getId();
    }

    /** The name of the medium the chat reads the text as with this choice. */
    public String mediumName() {
        return medium.getValue() != null ? medium.getValue().getName() : inherited.medium();
    }

    /** The scene typed; null for the inherited one. */
    public String sceneText() {
        return scene.getValue() == null || scene.getValue().isBlank() ? null : scene.getValue().strip();
    }

    /** The scene the chat has with this choice. */
    public String effectiveScene() {
        String own = sceneText();
        return own != null ? own : inherited.scene();
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
