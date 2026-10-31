package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The editors of the settings that extensions added to the books and the folders ({@link InheritedSetting}), the section of the
 * settings dialogs of a book and of a folder that follows {@link PromptSettingsForm}: one group per extension, each setting a line,
 * a text area or a choice. Like the fields of that form an empty one is not set but inherited, which it shows by the inherited value
 * as the placeholder and a line saying where that value comes from.
 * <p>
 * The form knows nothing about what it edits: {@link #load} takes the values and the folder the empty ones are inherited from,
 * {@link #values()} gives the values back and the owner saves them. A change by the user is reported to the {@code onChange}
 * callback. The settings are those registered when the form is {@link #create created}; with none registered the form is
 * {@link #isEmpty() empty} and the owner leaves it out.
 */
@Configurable
public class ExtensionSettingsForm {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private InheritedSettingRegistry registry;

    /**
     * One setting and its editor, whatever the editor is.
     */
    private record Editor(InheritedSetting setting, Component component, Supplier<String> value, Consumer<String> setValue,
                          Consumer<String> setPlaceholder, Consumer<String> setHelper, Consumer<String> setDefault) {
    }

    private final Map<String, Editor> editors = new LinkedHashMap<>();
    private final Map<String, EffectiveSetting<String>> inherited = new LinkedHashMap<>();
    private final String areaHeight;
    private final Runnable onChange;

    private VerticalLayout layout;
    private boolean loading = false;

    /**
     * @param areaHeight the height of the text areas of the form, e.g. "160px"
     * @param onChange   told after the user changed a field, may be null
     */
    public ExtensionSettingsForm(String areaHeight, Runnable onChange) {
        this.areaHeight = areaHeight;
        this.onChange = onChange;
    }

    /**
     * Makes the editors, which the form is laid out from; it is done after the construction because the services and
     * texts are injected after it.
     *
     * @return this form
     */
    public ExtensionSettingsForm create() {
        layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setWidthFull();
        List<InheritedSettingRegistry.Group> groups = registry.groups();
        if (!groups.isEmpty()) {
            Span title = new Span(loc.getValue(L.LABEL_EXTENSION_SETTINGS));
            title.addClassName(CssStyles.SECTION_TITLE);
            layout.add(title);
        }
        for (InheritedSettingRegistry.Group group : groups) {
            layout.add(new H4(group.title()));
            for (InheritedSetting setting : group.settings()) {
                Editor editor = createEditor(setting);
                editors.put(setting.getKey(), editor);
                layout.add(editor.component());
            }
        }
        return this;
    }

    private Editor createEditor(InheritedSetting setting) {
        String label = setting.label();
        switch (setting.getKind()) {
            case TEXT_AREA -> {
                // a text area is mostly a prompt: the info icon offers the inherited text to be copied into the field
                TextAreaPopoverComponent area = new TextAreaPopoverComponent(label);
                area.setWidthFull();
                ResizableTextArea.install(loc, area, areaHeight);
                area.addValueChangeListener(this::changed);
                return new Editor(setting, area, area::getValue, area::setValue, area::setPlaceholder, area::setHelperText,
                        text -> area.setPopoverContent(TemplateHints.create(loc, area, area.getPopover(), null, text)));
            }
            case CHOICE -> {
                ComboBox<Choice> combo = new ComboBox<>(label);
                combo.setWidthFull();
                combo.setItems(setting.choices());
                combo.setItemLabelGenerator(Choice::label);
                combo.setClearButtonVisible(true);
                combo.setAllowCustomValue(false);
                combo.addValueChangeListener(this::changed);
                return new Editor(setting, combo, () -> combo.getValue() == null ? "" : combo.getValue().id(),
                        id -> combo.setValue(choiceOf(setting.choices(), id)),
                        id -> combo.setPlaceholder(labelOf(setting.choices(), id)), combo::setHelperText, id -> {
                        });
            }
            default -> {
                TextField line = new TextField(label);
                line.setWidthFull();
                line.addValueChangeListener(this::changed);
                return new Editor(setting, line, line::getValue, line::setValue, line::setPlaceholder, line::setHelperText,
                        text -> {
                        });
            }
        }
    }

    private static Choice choiceOf(List<Choice> choices, String id) {
        return choices.stream().filter(choice -> choice.id().equals(id)).findFirst().orElse(null);
    }

    private static String labelOf(List<Choice> choices, String id) {
        Choice choice = choiceOf(choices, id);
        // an id that is not (any more) among the choices is shown as it is
        return choice == null ? StringUtils.defaultString(id) : choice.label();
    }

    /**
     * Whether no extension registered a setting, so there is nothing to show.
     */
    public boolean isEmpty() {
        return editors.isEmpty();
    }

    /**
     * The groups of editors under a title, for the owner to lay out.
     */
    public Component layout() {
        return layout;
    }

    /**
     * Puts the values into the editors and works out what an empty one inherits.
     *
     * @param own           what is set where the form is for by key, a blank text is not set
     * @param inheritedFrom the folder the book or folder is in: the empty fields are inherited from it, the folders above
     *                      it and then the user level of the extension (null: the top level)
     */
    public void load(Function<String, String> own, Folder inheritedFrom) {
        loading = true;
        try {
            for (Editor editor : editors.values()) {
                String key = editor.setting().getKey();
                EffectiveSetting<String> fallback = manuscriptService.resolveExtensionInherited(inheritedFrom, key);
                inherited.put(key, fallback);
                editor.setPlaceholder().accept(fallback.value());
                editor.setDefault().accept(fallback.value());
                editor.setValue().accept(StringUtils.defaultString(own.apply(key)));
                refreshHelper(editor);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        } finally {
            loading = false;
        }
    }

    /**
     * What the editors hold now: every setting with its text, blank when it is not set.
     */
    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        editors.forEach((key, editor) -> values.put(key, StringUtils.defaultString(editor.value().get())));
        return values;
    }

    private void changed(com.vaadin.flow.component.HasValue.ValueChangeEvent<?> event) {
        if (loading) {
            return;
        }
        editors.values().stream().filter(editor -> editor.component() == event.getHasValue()).findFirst()
                .ifPresent(this::refreshHelper);
        if (event.isFromClient() && onChange != null) {
            onChange.run();
        }
    }

    /**
     * The line under the field: what the setting is for and, while the field is empty, where its inherited value comes from.
     */
    private void refreshHelper(Editor editor) {
        String help = editor.setting().help();
        String origin = StringUtils.isBlank(editor.value().get()) && inherited.containsKey(editor.setting().getKey())
                ? PromptSettingsForm.origin(loc, inherited.get(editor.setting().getKey())) : null;
        String text = StringUtils.join(new String[]{help, origin}, " ").trim();
        editor.setHelper().accept(text.isEmpty() ? null : text);
    }
}
