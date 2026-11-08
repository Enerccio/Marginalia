package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.SpokenSettings;
import com.github.enerccio.marginalia.extensions.audiobook.script.SpokenForm;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * How the text of the book is spelled for the voice (kept with the book): the English rules for numbers, abbreviations and numbered headings, the word
 * before a chapter number, and the pronunciation dictionary (a word of the book and what is said for it: a respelling, or a reading in kana). A text can be
 * tried at once. The audio is made again only for the pieces whose spoken text changed.
 */
public class SpokenFormDialog extends Dialog {

    private final AudiobookService service;
    private final Manuscript manuscript;
    private final Runnable saved;

    private final Select<SpokenSettings.Rules> rules = new Select<>();
    private final TextField chapterWord = new TextField(T.localized(WORD_BEFORE_A_CHAPTER_NUMBER));
    private final VerticalLayout words = new VerticalLayout();
    private final List<Row> rows = new ArrayList<>();
    private final TextArea sample = new TextArea(T.localized(TRY_A_TEXT));
    private final Checkbox asHeading = new Checkbox(T.localized(AS_A_HEADING));
    private final TextArea result = new TextArea(T.localized(IT_IS_SAID_AS));
    private final Button save = new Button(T.localized(SAVE), e -> save());

    /** @param saved called after the settings are stored */
    public SpokenFormDialog(AudiobookService service, Manuscript manuscript, Runnable saved) {
        this.service = service;
        this.manuscript = manuscript;
        this.saved = saved;
        SpokenSettings settings = service.spokenOf(manuscript);

        setHeaderTitle(T.localized(PRONUNCIATION));
        setWidth("min(52em, 95vw)");
        setCloseOnOutsideClick(false);

        rules.setLabel(T.localized(NUMBERS_AND_ABBREVIATIONS));
        rules.setItems(SpokenSettings.Rules.values());
        rules.setItemLabelGenerator(r -> r == SpokenSettings.Rules.ENGLISH ? T.localized(RULES_ENGLISH) : T.localized(RULES_NONE));
        rules.setWidth("32em");
        rules.setValue(settings.getRules());
        chapterWord.setValue(settings.getChapterWord());
        chapterWord.setWidth("14em");
        chapterWord.setHelperText(T.localized(FOR_A_HEADING_THAT_IS_ONLY_A_NUMBER));
        Span help = new Span(T.localized(PRONUNCIATION_HELP));
        help.addClassName(CssStyles.HINT_TEXT);

        words.setPadding(false);
        words.setSpacing(false);
        settings.getDictionary().forEach(e -> addRow(e.getWord(), e.getSpoken()));
        Button add = new Button(T.localized(ADD_A_WORD), Solid.PLUS.create(), e -> addRow("", ""));

        sample.setWidthFull();
        sample.setMinHeight("5em");
        sample.setValue("# IV. The Arch\n\nDr. Kos came in 1999 with 3 boxes, 25% of the 1,200 pounds.");
        result.setWidthFull();
        result.setReadOnly(true);
        result.setMinHeight("5em");
        sample.addValueChangeListener(e -> preview());
        asHeading.addValueChangeListener(e -> preview());
        rules.addValueChangeListener(e -> preview());
        chapterWord.addValueChangeListener(e -> preview());

        VerticalLayout content = new VerticalLayout(rules, chapterWord, help, new H4(T.localized(DICTIONARY)), words, add, new H4(T.localized(TRY_A_TEXT)),
                sample, asHeading, result);
        content.setPadding(false);
        add(content);

        save.addThemeName("primary");
        getFooter().add(new Button(T.localized(CANCEL), e -> close()), save);
        DialogFocus.onOpen(this, chapterWord);
        DialogShortcuts.submitWith(save, chapterWord);
        preview();
    }

    private void addRow(String word, String spoken) {
        Row row = new Row(word, spoken);
        rows.add(row);
        words.add(row.layout);
    }

    /** What the fields say. */
    private SpokenSettings read() {
        SpokenSettings settings = new SpokenSettings();
        settings.setRules(rules.getValue() == null ? SpokenSettings.Rules.ENGLISH : rules.getValue());
        settings.setChapterWord(chapterWord.getValue().strip());
        settings.setDictionary(rows.stream().filter(r -> !r.word.getValue().isBlank())
                .map(r -> new SpokenSettings.Entry(r.word.getValue().strip(), r.spoken.getValue())).toList());
        return settings;
    }

    /** Pieces of the sample are spelled one paragraph at a time, as the script does. */
    private void preview() {
        SpokenForm form = SpokenForm.of(read());
        StringBuilder out = new StringBuilder();
        for (String paragraph : sample.getValue().split("\\R\\s*\\R")) {
            boolean heading = asHeading.getValue() || paragraph.stripLeading().startsWith("#");
            String said = form.apply(paragraph.strip().replaceFirst("^#+\\s*", "").replaceAll("\\s*\\R\\s*", " "), heading);
            out.append(said.isEmpty() ? T.localized(NOTHING_SCENE_BREAK) : said).append("\n\n");
        }
        result.setValue(out.toString().strip());
    }

    private void save() {
        try {
            service.saveSpoken(manuscript, read());
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The pronunciation could not be stored: {}", e.getMessage(), e);
            Notification.error(T.localized(THE_PRONUNCIATION_COULD_NOT_BE_STORED).formatted(e.getMessage()));
            return;
        }
        saved.run();
        close();
    }

    private final class Row {
        private final TextField word = new TextField(T.localized(WORD_IN_THE_BOOK));
        private final TextField spoken = new TextField(T.localized(SAID_AS));
        private HorizontalLayout layout;

        Row(String wordText, String spokenText) {
            word.setValue(wordText);
            word.setWidth("14em");
            spoken.setValue(spokenText);
            spoken.setWidth("20em");
            word.addValueChangeListener(e -> preview());
            spoken.addValueChangeListener(e -> preview());
            Button remove = new Button(Solid.TRASH.create(), e -> {
                rows.remove(this);
                words.remove(this.layout);
                preview();
            });
            remove.setThemeName("tertiary icon");
            remove.setTooltipText(T.localized(REMOVE));
            layout = new HorizontalLayout(word, spoken, remove);
            layout.setAlignItems(FlexComponent.Alignment.END);
            layout.setWrap(true);
            DialogShortcuts.submitWith(save, word, spoken);
        }
    }
}
