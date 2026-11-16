package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.AnnotationTemplate;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The editor of the annotation templates of the user settings ({@link AnnotationTemplate}), which the entries of the phrase watch lists
 * name: a block per template with its name, the note ({@code {{match}}}, {@code {{phrase}}}), the type and whether the note is given to
 * the model with the part. Like {@link WatchedPhrasesEditor} it only holds the values, the settings page saves them.
 */
public class AnnotationTemplatesEditor extends VerticalLayout {

    private final Localization loc;
    private final VerticalLayout blocks = new VerticalLayout();
    private final List<Block> entries = new ArrayList<>();
    private Runnable onChange = () -> {
    };
    private boolean loading;

    public AnnotationTemplatesEditor(Localization loc) {
        this.loc = loc;
        setPadding(false);
        setWidthFull();
        addClassName(CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_ANNOTATION_TEMPLATES));
        help.addClassName(CssStyles.HINT_TEXT);

        blocks.setPadding(false);
        blocks.setSpacing(false);
        blocks.setWidthFull();
        blocks.addClassName(CssStyles.GAP_S);

        Button add = new Button(loc.getValue(L.LABEL_ADD_ANNOTATION_TEMPLATE), VaadinIcon.PLUS.create(), _ -> {
            Block block = addBlock(new AnnotationTemplate("", AnnotationTemplate.MATCH, null, false));
            block.name.focus();
            changed();
        });
        add.addThemeVariants(ButtonVariant.LUMO_SMALL);

        add(help, blocks, add);
    }

    /**
     * Told about every edit of the user.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {
        } : onChange;
    }

    public void setValue(List<AnnotationTemplate> templates) {
        loading = true;
        try {
            entries.clear();
            blocks.removeAll();
            if (templates != null) {
                templates.forEach(template -> addBlock(template.copy()));
            }
        } finally {
            loading = false;
        }
    }

    /**
     * The templates; a template keeps its uuid, so the entries that name it still do.
     */
    public List<AnnotationTemplate> getValue() {
        return entries.stream().map(Block::toTemplate).toList();
    }

    /**
     * Marks a template without a name or a note.
     *
     * @return false when one is not complete and the templates must not be saved
     */
    public boolean validate() {
        boolean valid = true;
        for (Block block : entries) {
            boolean name = StringUtils.isNotBlank(block.name.getValue());
            boolean text = StringUtils.isNotBlank(block.text.getValue());
            block.name.setInvalid(!name);
            block.text.setInvalid(!text);
            if ((!name || !text) && valid) {
                valid = false;
                Notification.warning(loc.getValue(L.ERROR_ANNOTATION_TEMPLATE_INCOMPLETE));
            }
        }
        return valid;
    }

    private Block addBlock(AnnotationTemplate template) {
        Block block = new Block(template);
        entries.add(block);
        blocks.add(block.layout);
        return block;
    }

    private void changed() {
        if (!loading) {
            onChange.run();
        }
    }

    private final class Block {
        private final String uuid;
        private final TextField name = new TextField(loc.getValue(L.LABEL_NAME));
        private final TextField type = new TextField(loc.getValue(L.LABEL_TYPE));
        private final TextArea text = new TextArea(loc.getValue(L.LABEL_NOTE));
        private final Checkbox sendToPrompt = new Checkbox(loc.getValue(L.LABEL_ANNOTATION_SEND_TO_PROMPT));
        private final VerticalLayout layout = new VerticalLayout();

        private Block(AnnotationTemplate template) {
            uuid = template.getUuid();
            name.setValue(StringUtils.defaultString(template.getName()));
            name.addValueChangeListener(_ -> {
                name.setInvalid(false);
                changed();
            });
            type.setValue(StringUtils.defaultString(template.getType()));
            type.setPlaceholder(loc.getValue(L.MSG_ANNOTATION_TYPE_PLACEHOLDER));
            type.addValueChangeListener(_ -> changed());
            text.setValue(StringUtils.defaultString(template.getText()));
            text.setWidthFull();
            text.addValueChangeListener(_ -> {
                text.setInvalid(false);
                changed();
            });
            sendToPrompt.setValue(template.isSendToPrompt());
            sendToPrompt.addValueChangeListener(_ -> changed());

            Button remove = new Button(VaadinIcon.TRASH.create(), _ -> {
                entries.remove(this);
                blocks.remove(layout);
                changed();
            });
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
            remove.setTooltipText(loc.getValue(L.LABEL_REMOVE));
            remove.setAriaLabel(loc.getValue(L.LABEL_REMOVE));

            HorizontalLayout top = new HorizontalLayout(name, type, sendToPrompt, remove);
            top.setWidthFull();
            top.addClassName(CssStyles.FLEX_WRAP);
            top.setAlignItems(FlexComponent.Alignment.BASELINE);
            top.setFlexGrow(1, name);

            layout.add(top, text);
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.setWidthFull();
            layout.addClassNames(CssStyles.BORDERED_PANEL, CssStyles.GAP_S);
        }

        private AnnotationTemplate toTemplate() {
            AnnotationTemplate template = new AnnotationTemplate(StringUtils.strip(name.getValue()), text.getValue(),
                    StringUtils.trimToNull(type.getValue()), sendToPrompt.getValue());
            template.setUuid(uuid);
            return template;
        }
    }
}
