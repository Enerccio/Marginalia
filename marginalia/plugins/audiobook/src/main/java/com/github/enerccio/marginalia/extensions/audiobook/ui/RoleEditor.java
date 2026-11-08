package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.model.Role;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/** Edits a {@link Role}: its voice, and the voices for a mood (a whisper) or a language (a Japanese clip). */
class RoleEditor extends VerticalLayout {

    /** The languages a piece can be recognised as by its script (the code is what the role stores). */
    private static final List<String> LANGUAGES = List.of("ja", "zh", "ko", "ru", "el", "he", "ar", "hi");

    private final VoiceChoices choices;
    private final VoiceField voice;
    private final VerticalLayout rows = new VerticalLayout();
    private final List<Row> variantRows = new ArrayList<>();

    RoleEditor(Role role, VoiceChoices choices) {
        this.choices = choices;
        setPadding(false);
        setSpacing(false);
        voice = new VoiceField(T.localized(VOICE), choices);
        voice.setValue(role.getVoice());
        rows.setPadding(false);
        rows.setSpacing(false);
        role.getVariants().forEach(this::addRow);
        Button add = new Button(T.localized(A_VOICE_FOR_A_MOOD_OR_A_LANGUAGE), Solid.PLUS.create(), e -> addRow(nextKey(), ""));
        add.setThemeName("tertiary small");
        add(voice, rows, add);
    }

    /** Writes what the editor shows into the role. */
    void read(Role target) {
        target.setVoice(voice.getValue().strip());
        Map<String, String> variants = new LinkedHashMap<>();
        for (Row row : variantRows) {
            String value = row.voice.getValue().strip();
            if (row.key.getValue() != null && !value.isEmpty()) {
                variants.put(row.key.getValue(), value);
            }
        }
        target.setVariants(variants);
    }

    private static List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (Delivery delivery : Delivery.values()) {
            if (delivery != Delivery.NEUTRAL) {
                keys.add(Role.moodKey(delivery.name()));
            }
        }
        LANGUAGES.forEach(code -> keys.add(Role.languageKey(code)));
        return keys;
    }

    private static String keyLabel(String key) {
        if (Role.isMoodKey(key)) {
            return T.localized(MOOD_NAME).formatted(key.substring("mood:".length()).toLowerCase(Locale.ROOT));
        }
        String code = key.substring("lang:".length());
        return T.localized(LANGUAGE_NAME).formatted(Locale.forLanguageTag(code).getDisplayLanguage() + " (" + code + ")");
    }

    private String nextKey() {
        List<String> used = variantRows.stream().map(r -> r.key.getValue()).toList();
        return keys().stream().filter(k -> !used.contains(k)).findFirst().orElse(keys().getFirst());
    }

    private void addRow(String key, String voiceText) {
        Row row = new Row(key, voiceText);
        variantRows.add(row);
        rows.add(row.layout);
    }

    private final class Row {
        private final Select<String> key = new Select<>();
        private final VoiceField voice = new VoiceField(T.localized(VOICE), choices);
        private HorizontalLayout layout;

        Row(String keyValue, String voiceText) {
            List<String> items = keys();
            if (!items.contains(keyValue)) {
                items.add(keyValue);
            }
            key.setLabel(T.localized(WHEN));
            key.setItems(items);
            key.setItemLabelGenerator(RoleEditor::keyLabel);
            key.setValue(keyValue);
            voice.setValue(voiceText);
            Button remove = new Button(Solid.TRASH.create(), e -> {
                variantRows.remove(this);
                rows.remove(this.layout);
            });
            remove.setThemeName("tertiary icon");
            remove.setTooltipText(T.localized(REMOVE));
            layout = new HorizontalLayout(key, voice, remove);
            layout.setAlignItems(FlexComponent.Alignment.END);
            layout.setWrap(true);
        }
    }
}
