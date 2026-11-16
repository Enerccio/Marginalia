package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.PhraseMatcher;
import com.github.enerccio.marginalia.domain.model.AnnotationTemplate;
import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.PhraseWatchService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The editor of one level of the phrase watch list (the user settings, a folder, a book): a row per entry with the phrase, whether it is
 * a regular expression, the annotation template of its hits and whether it is off (an entry that is off takes the same phrase of a
 * level above out). Under it, for a folder and a book, what the level inherits, each with a button that switches it off here.
 * <p>
 * It only holds the values: {@link #setValue} fills it, {@link #getValue} gives the entries back and the owner saves them;
 * {@link #setOnChange} is told about every edit of the user.
 */
public class WatchedPhrasesEditor extends VerticalLayout {

    private final Localization loc;
    private final VerticalLayout rows = new VerticalLayout();
    private final Div inherited = new Div();
    private final List<Row> entries = new ArrayList<>();
    private List<AnnotationTemplate> templates = List.of();
    private Runnable onChange = () -> {
    };
    private boolean loading;

    public WatchedPhrasesEditor(Localization loc) {
        this.loc = loc;
        setPadding(false);
        setWidthFull();
        addClassName(CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_PHRASE_WATCH_LIST));
        help.addClassName(CssStyles.HINT_TEXT);

        rows.setPadding(false);
        rows.setSpacing(false);
        rows.setWidthFull();
        rows.addClassName(CssStyles.GAP_S);

        Button add = new Button(loc.getValue(L.LABEL_ADD_PHRASE), VaadinIcon.PLUS.create(), _ -> {
            Row row = addRow(new WatchedPhrase());
            row.phrase.focus();
            changed();
        });
        add.addThemeVariants(ButtonVariant.LUMO_SMALL);

        inherited.setWidthFull();
        inherited.setVisible(false);

        add(help, rows, add, inherited);
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {
        } : onChange;
    }

    /**
     * The templates the entries can name; the template an entry names stays chosen when it is still there.
     */
    public void setTemplates(List<AnnotationTemplate> templates) {
        this.templates = templates == null ? List.of() : List.copyOf(templates);
        boolean was = loading;
        loading = true;
        try {
            for (Row row : entries) {
                String chosen = row.template.getValue() == null ? row.missingTemplate : row.template.getValue().getUuid();
                row.template.setItems(this.templates);
                row.select(chosen);
            }
        } finally {
            loading = was;
        }
    }

    public void setValue(List<WatchedPhrase> phrases) {
        loading = true;
        try {
            entries.clear();
            rows.removeAll();
            if (phrases != null) {
                phrases.forEach(this::addRow);
            }
        } finally {
            loading = false;
        }
    }

    /**
     * The entries, the ones with an empty phrase left out.
     */
    public List<WatchedPhrase> getValue() {
        List<WatchedPhrase> phrases = new ArrayList<>();
        for (Row row : entries) {
            WatchedPhrase phrase = row.toPhrase();
            if (StringUtils.isNotBlank(phrase.getPhrase())) {
                phrases.add(phrase);
            }
        }
        return phrases;
    }

    /**
     * Marks the entries that can't be used (an expression that does not compile or matches nothing at all).
     *
     * @return false when one can't be used and the list must not be saved
     */
    public boolean validate() {
        boolean valid = true;
        for (Row row : entries) {
            WatchedPhrase phrase = row.toPhrase();
            String problem = StringUtils.isBlank(phrase.getPhrase()) ? null : PhraseMatcher.validate(phrase);
            row.phrase.setInvalid(problem != null);
            row.phrase.setErrorMessage(problem == null ? null : String.format(loc.getValue(L.ERROR_PHRASE_INVALID), problem));
            if (problem != null && valid) {
                valid = false;
                Notification.warning(String.format(loc.getValue(L.ERROR_PHRASE_INVALID), phrase.getPhrase() + ": " + problem));
            }
        }
        return valid;
    }

    /**
     * Shows what the level inherits (the user settings and the folders above it); an empty list hides it.
     */
    public void setInherited(List<PhraseWatchService.Effective> phrases) {
        inherited.removeAll();
        inherited.setVisible(phrases != null && !phrases.isEmpty());
        if (phrases == null || phrases.isEmpty()) {
            return;
        }
        Span title = new Span(loc.getValue(L.LABEL_INHERITED_PHRASES));
        title.addClassName(CssStyles.TEXT_SECONDARY);
        inherited.add(title);
        for (PhraseWatchService.Effective entry : phrases) {
            WatchedPhrase phrase = entry.phrase();
            Span text = new Span(phrase.getPhrase() + (phrase.isRegex() ? " (" + loc.getValue(L.LABEL_REGULAR_EXPRESSION) + ")" : "")
                    + " · " + templateName(phrase.getTemplate()) + " · "
                    + PromptSettingsForm.origin(loc, new EffectiveSetting<>(phrase.getPhrase(), entry.origin(), entry.folder())));
            text.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
            Button off = new Button(loc.getValue(L.LABEL_SWITCH_OFF_HERE), _ -> {
                WatchedPhrase copy = phrase.copy();
                copy.setDisabled(true);
                addRow(copy);
                changed();
            });
            off.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            HorizontalLayout line = new HorizontalLayout(text, off);
            line.setWidthFull();
            line.setAlignItems(FlexComponent.Alignment.CENTER);
            line.setFlexGrow(1, text);
            inherited.add(line);
        }
    }

    private String templateName(String uuid) {
        if (uuid == null) {
            return loc.getValue(L.MSG_PHRASE_BUILTIN_NOTE);
        }
        return templates.stream().filter(template -> uuid.equals(template.getUuid())).map(AnnotationTemplate::getName)
                .filter(StringUtils::isNotBlank).findFirst().orElse(loc.getValue(L.MSG_PHRASE_BUILTIN_NOTE));
    }

    private Row addRow(WatchedPhrase phrase) {
        Row row = new Row(phrase);
        entries.add(row);
        rows.add(row.layout);
        return row;
    }

    private void changed() {
        if (!loading) {
            onChange.run();
        }
    }

    private final class Row {
        private final TextField phrase = new TextField();
        private final Checkbox regex = new Checkbox();
        private final ComboBox<AnnotationTemplate> template = new ComboBox<>();
        private final Checkbox off = new Checkbox();
        private final HorizontalLayout layout = new HorizontalLayout();
        // the uuid of a template the entry names that is not among the templates (deleted): kept until the author picks another
        private String missingTemplate;

        private Row(WatchedPhrase value) {
            phrase.setPlaceholder(loc.getValue(L.LABEL_WATCHED_PHRASE));
            phrase.setAriaLabel(loc.getValue(L.LABEL_WATCHED_PHRASE));
            phrase.setValue(StringUtils.defaultString(value.getPhrase()));
            phrase.setMinWidth("12em");
            phrase.addValueChangeListener(_ -> {
                phrase.setInvalid(false);
                changed();
            });

            regex.setLabel(loc.getValue(L.LABEL_REGULAR_EXPRESSION));
            regex.setValue(value.isRegex());
            regex.addValueChangeListener(_ -> changed());

            template.setItems(templates);
            template.setItemLabelGenerator(item -> StringUtils.defaultIfBlank(item.getName(), "?"));
            template.setPlaceholder(loc.getValue(L.MSG_PHRASE_BUILTIN_NOTE));
            template.setAriaLabel(loc.getValue(L.LABEL_ANNOTATION_TEMPLATE));
            template.setClearButtonVisible(true);
            select(value.getTemplate());
            template.addValueChangeListener(event -> {
                if (event.isFromClient()) {
                    missingTemplate = null;
                }
                changed();
            });

            off.setLabel(loc.getValue(L.LABEL_PHRASE_OFF));
            off.setValue(value.isDisabled());
            off.addValueChangeListener(_ -> changed());

            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                entries.remove(this);
                rows.remove(layout);
                changed();
            });
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
            remove.setTooltipText(loc.getValue(L.LABEL_REMOVE));
            remove.setAriaLabel(loc.getValue(L.LABEL_REMOVE));

            layout.add(phrase, regex, template, off, remove);
            layout.setWidthFull();
            layout.addClassName(CssStyles.FLEX_WRAP);
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.setFlexGrow(1, phrase);
        }

        private void select(String uuid) {
            AnnotationTemplate found = uuid == null ? null
                    : templates.stream().filter(item -> uuid.equals(item.getUuid())).findFirst().orElse(null);
            template.setValue(found);
            missingTemplate = found == null ? uuid : null;
        }

        private WatchedPhrase toPhrase() {
            WatchedPhrase value = new WatchedPhrase(StringUtils.strip(phrase.getValue()), regex.getValue(),
                    template.getValue() != null ? template.getValue().getUuid() : missingTemplate);
            value.setDisabled(off.getValue());
            return value;
        }
    }
}
