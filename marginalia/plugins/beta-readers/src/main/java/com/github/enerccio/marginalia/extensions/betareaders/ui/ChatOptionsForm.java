package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.model.ReplyFormat;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * What a chat may choose for itself instead of inheriting it: the medium, the model, the protocol and the format of the replies, and
 * how much of the story the personas read (the spoiler limit, the first part, the summaries). The fields are empty for "inherited" and
 * say what that is. Used by the window that starts a chat and the one that changes its settings.
 */
public class ChatOptionsForm extends VerticalLayout {

    /**
     * What the chat gets when it chooses nothing, as the names shown in the empty fields ("Inherited: ...").
     */
    public record Inherited(String medium, String ai, String protocol, String scene, int maxMessages) {

        public Inherited(String medium, String ai, String protocol, String scene) {
            this(medium, ai, protocol, scene, 0);
        }

        public Inherited(String medium, String ai, String protocol) {
            this(medium, ai, protocol, null, 0);
        }
    }

    final TextField userName = new TextField(T.localized(YOUR_NAME));
    final ComboBox<Medium> medium = new ComboBox<>(T.localized(MEDIUM));
    final TextArea scene = new TextArea(T.localized(SCENE));
    final ComboBox<Choice> ai = new ComboBox<>(T.localized(MODEL));
    final ComboBox<Choice> protocol = new ComboBox<>(T.localized(PROTOCOL));
    final Select<ReplyFormat> format = new Select<>();
    final IntegerField spoilerLimit = new IntegerField(T.localized(PERSONAS_READ_UP_TO_PART));
    final IntegerField fromPart = new IntegerField(T.localized(START_FROM_PART));
    final IntegerField maxMessages = new IntegerField(T.localized(MOST_MESSAGES_IN_A_TURN));
    final Checkbox summaries = new Checkbox(T.localized(USE_THE_SUMMARIES));

    private final Inherited inherited;
    /** Whether the form has the first and the last part the personas read (the chat panel has them under the box to write in). */
    private boolean partRange = true;

    public ChatOptionsForm(List<Medium> mediums, List<Choice> ais, List<Choice> protocols, Inherited inherited) {
        this.inherited = inherited;
        setPadding(false);

        userName.setWidthFull();
        userName.setMaxLength(60);
        userName.setClearButtonVisible(true);
        userName.setHelperText(T.localized(EMPTY_NOT_NAMED));

        medium.setItems(mediums);
        medium.setItemLabelGenerator(Medium::getName);
        medium.setPlaceholder(inheritedText(inherited.medium()));
        scene.setWidthFull();
        scene.setMinHeight("6rem");
        scene.setPlaceholder(inheritedText(shorten(inherited.scene())));
        scene.setHelperText(T.localized(EMPTY_INHERITED_WHERE_AND_HOW));
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

        format.setLabel(T.localized(REPLY_FORMAT));
        format.setItems(ReplyFormat.AUTO, ReplyFormat.JSON, ReplyFormat.JSON_NO_SCHEMA, ReplyFormat.TEXT);
        format.setItemLabelGenerator(ChatOptionsForm::label);
        format.setValue(ReplyFormat.AUTO);
        format.setWidthFull();
        format.setHelperText(T.localized(AUTOMATIC_ASKS_FOR_JSON) + ". " + T.localized(JSON_WITHOUT_A_SCHEMA_HELP));

        spoilerLimit.setMin(1);
        spoilerLimit.setStepButtonsVisible(true);
        spoilerLimit.setWidthFull();
        spoilerLimit.setHelperText(T.localized(EMPTY_THE_WHOLE_STORY));
        fromPart.setMin(1);
        fromPart.setStepButtonsVisible(true);
        fromPart.setWidthFull();
        fromPart.setHelperText(T.localized(EMPTY_FROM_THE_FIRST_PART));
        summaries.setValue(true);

        maxMessages.setMin(1);
        maxMessages.setMax(BetaReadersDefaults.MAX_MESSAGES_LIMIT);
        maxMessages.setStepButtonsVisible(true);
        maxMessages.setWidthFull();
        maxMessages.setPlaceholder(inheritedText(inherited.maxMessages() > 0 ? String.valueOf(inherited.maxMessages())
                : T.localized(AUTOMATIC)));
        maxMessages.setHelperText(T.localized(EMPTY_INHERITED_THE_MOST_MESSAGES));

        add(userName, spoilerLimit, fromPart, summaries, maxMessages, medium, scene, ai, protocol, format);
    }

    /**
     * Leaves the first and the last part the personas read out of the form: a chat that exists has them in its panel, where they
     * are changed as the story is read on, and the form neither shows nor changes them.
     */
    public ChatOptionsForm withoutPartRange() {
        partRange = false;
        spoilerLimit.setVisible(false);
        fromPart.setVisible(false);
        return this;
    }

    /**
     * What is wrong with the choices, or null when they are fine.
     */
    public String problem() {
        Integer first = fromPart.getValue();
        Integer last = spoilerLimit.getValue();
        Integer most = maxMessages.getValue();
        if (most != null && (most < 1 || most > BetaReadersDefaults.MAX_MESSAGES_LIMIT)) {
            return T.localized(THE_MOST_MESSAGES_MUST_BE).formatted(BetaReadersDefaults.MAX_MESSAGES_LIMIT);
        }
        if (partRange && first != null && last != null && first > last) {
            return T.localized(THE_FIRST_PART_CANT_COME);
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
        return T.localized(INHERITED).formatted(value == null || value.isBlank() ? T.localized(NONE) : value);
    }

    private static String label(ReplyFormat format) {
        return switch (format) {
            case AUTO -> T.localized(AUTOMATIC);
            case JSON -> T.localized(JSON);
            case JSON_NO_SCHEMA -> T.localized(JSON_WITHOUT_A_SCHEMA);
            case TEXT -> T.localized(FREE_TEXT);
        };
    }

    /**
     * Shows the choices the chat has now.
     */
    public void load(BetaChat chat) {
        medium.setValue(medium.getListDataView().getItems().filter(item -> item.getId().equals(chat.getMedium())).findFirst().orElse(null));
        scene.setValue(chat.getScene() == null ? "" : chat.getScene());
        userName.setValue(chat.getUserName());
        ai.setValue(ai.getListDataView().getItems().filter(item -> item.id().equals(chat.getAi())).findFirst().orElse(null));
        protocol.setValue(protocol.getListDataView().getItems().filter(item -> item.id().equals(chat.getProtocol())).findFirst().orElse(null));
        format.setValue(chat.getFormat());
        spoilerLimit.setValue(chat.getSpoilerLimit() > 0 ? chat.getSpoilerLimit() : null);
        fromPart.setValue(chat.getFromPart() > 0 ? chat.getFromPart() : null);
        summaries.setValue(chat.isSummaries());
        maxMessages.setValue(chat.getMaxMessages() > 0 ? chat.getMaxMessages() : null);
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
        chat.setUserName(userName.getValue());
        chat.setAi(aiId());
        chat.setProtocol(protocolId());
        chat.setFormat(replyFormat());
        if (partRange) {
            chat.setSpoilerLimit(spoilerLimit.getValue() == null ? 0 : spoilerLimit.getValue());
            chat.setFromPart(fromPart.getValue() == null ? 0 : fromPart.getValue());
        }
        chat.setSummaries(summaries.getValue());
        chat.setMaxMessages(maxMessages.getValue() == null ? 0 : maxMessages.getValue());
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
