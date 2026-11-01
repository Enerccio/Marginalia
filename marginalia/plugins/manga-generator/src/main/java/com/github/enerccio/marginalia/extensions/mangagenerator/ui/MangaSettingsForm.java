package com.github.enerccio.marginalia.extensions.mangagenerator.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSettings;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.Defaults;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.MangaService;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.*;

/**
 * The defaults a new manga starts with, and the text put before every request: a panel of <i>Settings → Extension Settings</i>.
 * Saved with the settings of the user. A prompt that is empty or the built-in one is not stored: the built-in one is used.
 */
public class MangaSettingsForm extends VerticalLayout {

    private final MangaService service;

    private TextArea initialQuery;
    private IntegerField tokenBudget;
    private IntegerField splitCount;
    private IntegerField contextLeft;
    private IntegerField contextRight;
    private TextArea prompt;
    private TextArea tagsPrompt;
    private TextArea rules;

    public MangaSettingsForm(MangaService service) {
        this.service = service;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        Span hint = new Span(T.localized(A_NEW_MANGA_STARTS));
        hint.addClassName(CssStyles.HINT_TEXT);

        initialQuery = area(T.localized(TEXT_BEFORE_EVERY_REQUEST),
                T.localized(PUT_INTO_THE_SYSTEM_MESSAGE), "8em");
        tokenBudget = number(T.localized(TOKEN_BUDGET), 512);
        splitCount = number(T.localized(PARTS_PER_SECTION), 1);
        contextLeft = number(T.localized(CONTEXT_PARTS_BEFORE), 0);
        contextRight = number(T.localized(CONTEXT_PARTS_AFTER), 0);
        HorizontalLayout numbers = new HorizontalLayout(tokenBudget, splitCount, contextLeft, contextRight);
        numbers.setWidthFull();
        numbers.setWrap(true);

        prompt = area(T.localized(DEFAULT_SCRIPT_PROMPT), T.localized(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT), "16em");
        tagsPrompt = area(T.localized(DEFAULT_TAGS_PROMPT), T.localized(EMPTY_OR_UNCHANGED_THE_BUILT_IN_PROMPT), "12em");
        rules = area(T.localized(DEFAULT_ADDITIONAL_RULES), T.localized(PUT_IN_PLACE_OF_ADDITIONALRULES), "6em");

        add(hint, initialQuery, numbers, prompt, tagsPrompt, rules);
        refresh();
    }

    private static TextArea area(String label, String helper, String minHeight) {
        TextArea area = new TextArea(label);
        area.setWidthFull();
        area.setMinHeight(minHeight);
        area.setHelperText(helper);
        return area;
    }

    private static IntegerField number(String label, int min) {
        IntegerField field = new IntegerField(label);
        field.setMin(min);
        field.setStepButtonsVisible(true);
        return field;
    }

    /**
     * Shows the stored settings.
     */
    public void refresh() {
        MangaSettings settings = service.getSettings();
        initialQuery.setValue(settings.getInitialQuery());
        tokenBudget.setValue(settings.getDefaultTokenBudget());
        splitCount.setValue(settings.getDefaultSplitCount());
        contextLeft.setValue(settings.getDefaultContextLeft());
        contextRight.setValue(settings.getDefaultContextRight());
        prompt.setValue(StringUtils.defaultIfBlank(settings.getDefaultPromptOverride(), Defaults.DEFAULT_PROMPT));
        tagsPrompt.setValue(StringUtils.defaultIfBlank(settings.getDefaultTagsPromptOverride(), Defaults.DEFAULT_TAGS_PROMPT));
        rules.setValue(StringUtils.defaultString(settings.getDefaultAdditionalRules()));
    }

    /**
     * The stored settings with what the form shows. The stored ones are read again: the manga window keeps the chosen model
     * in them, and the form does not know about it.
     */
    public MangaSettings save() {
        MangaSettings settings = service.getSettings();
        settings.setInitialQuery(initialQuery.getValue());
        settings.setDefaultTokenBudget(valueOr(tokenBudget, MangaSettings.DEFAULT_TOKEN_BUDGET));
        settings.setDefaultSplitCount(valueOr(splitCount, MangaSettings.DEFAULT_SPLIT_COUNT));
        settings.setDefaultContextLeft(valueOr(contextLeft, MangaSettings.DEFAULT_CONTEXT));
        settings.setDefaultContextRight(valueOr(contextRight, MangaSettings.DEFAULT_CONTEXT));
        settings.setDefaultPromptOverride(override(prompt, Defaults.DEFAULT_PROMPT));
        settings.setDefaultTagsPromptOverride(override(tagsPrompt, Defaults.DEFAULT_TAGS_PROMPT));
        settings.setDefaultAdditionalRules(StringUtils.defaultIfBlank(rules.getValue(), null));
        return settings;
    }

    private static int valueOr(IntegerField field, int fallback) {
        Integer value = field.getValue();
        return value == null || value < field.getMin() ? fallback : value;
    }

    private static String override(TextArea area, String builtIn) {
        String value = area.getValue();
        return StringUtils.isBlank(value) || value.equals(builtIn) ? null : value;
    }
}
