package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.PartMerge.AttributeChange;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.MergePreview;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The review of merging a part into the part before it (the desktop and the phone story tab): the prompt fields of both
 * turns combined without the start they share (editable), what joins the two texts and the data kept on the parts value by
 * value, with the choice which one stays. Merging itself is {@link ChatMessageService#mergeIntoPrevious}.
 */
@Configurable(preConstruction = true)
public class PartMergeDialog extends Dialog {

    private enum Take {
        LATER, PREVIOUS, LEFT_OUT
    }

    private static final String ABSENT = "—";

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;
    private final ChatMessage part;
    private final MergePreview preview;
    private final Consumer<ChatMessage> onMerged;

    private final ComboBox<PartMerge.Join> join = new ComboBox<>();
    private final TextArea sceneSetting = new TextArea();
    private final TextArea povCharacter = new TextArea();
    private final TextArea presentCharacters = new TextArea();
    private final TextArea instructions = new TextArea();
    private final Grid<Row> attributes = new Grid<>();
    private final List<Row> rows = new ArrayList<>();
    private final Checkbox showUnchanged = new Checkbox();

    /**
     * Opens the dialog when the part can be merged, otherwise tells why not.
     *
     * @param onMerged gets the part the other was merged into, after the merge
     */
    public static void open(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onMerged) {
        PartMergeDialog dialog = create(manuscript, part, onMerged);
        if (dialog != null) {
            dialog.open();
        }
    }

    /**
     * @return the dialog, not opened; null (after telling why) when the part cannot be merged
     */
    static PartMergeDialog create(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onMerged) {
        PartMergeDialog dialog = new PartMergeDialog(manuscript, part, onMerged);
        return dialog.preview == null ? null : dialog;
    }

    private PartMergeDialog(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onMerged) {
        this.manuscript = manuscript;
        this.part = part;
        this.onMerged = onMerged;
        MergePreview found = null;
        try {
            found = chatMessageService.previewMergeIntoPrevious(manuscript, part);
        } catch (IllegalStateException e) {
            Notification.show(loc.getValue(L.MSG_PART_MERGE_NOT_POSSIBLE));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        this.preview = found;
        if (preview == null) {
            return;
        }
        build();
    }

    private void build() {
        setHeaderTitle(loc.getValue(L.LABEL_MERGE_INTO_PREVIOUS));
        setWidth("min(900px, 95vw)");
        setHeight("85vh");

        Span info = new Span(loc.getValue(L.MSG_PART_MERGE_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        join.setLabel(loc.getValue(L.LABEL_PART_JOIN));
        join.setItems(PartMerge.Join.values());
        join.setItemLabelGenerator(this::label);
        join.setValue(preview.join());
        join.setAllowCustomValue(false);
        join.setWidthFull();

        Span promptInfo = new Span(loc.getValue(L.MSG_PART_MERGE_PROMPT_INFO));
        promptInfo.addClassName(CssStyles.HINT_TEXT);
        field(sceneSetting, L.LABEL_SCENE_SETTING, preview.input().sceneSetting());
        field(povCharacter, L.LABEL_POV_CHARACTER, preview.input().povCharacter());
        field(presentCharacters, L.LABEL_PRESENT_CHARACTERS, preview.input().presentCharacters());
        field(instructions, L.LABEL_INSTRUCTIONS, preview.input().instructions());

        VerticalLayout content = new VerticalLayout(info, join, promptInfo, sceneSetting, povCharacter, presentCharacters, instructions);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        content.add(createAttributes());
        add(content);

        Button merge = new Button(loc.getValue(L.LABEL_MERGE), e -> merge());
        merge.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), e -> close()), merge);
    }

    private void field(TextArea area, L label, String value) {
        area.setLabel(loc.getValue(label));
        area.setValue(value == null ? "" : value);
        area.setWidthFull();
        area.setMinRows(1);
        area.setMaxRows(6);
    }

    private VerticalLayout createAttributes() {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(false);
        box.setWidthFull();
        box.addClassName(CssStyles.GAP_S);

        Span title = new Span(loc.getValue(L.LABEL_ATTRIBUTES));
        title.addClassName(CssStyles.TEXT_SEMIBOLD);
        box.add(title);
        for (AttributeChange change : preview.attributes()) {
            rows.add(new Row(change, change.takesPreviousByDefault() ? Take.PREVIOUS : Take.LATER));
        }
        if (rows.isEmpty()) {
            Span none = new Span(loc.getValue(L.MSG_PART_MERGE_NO_DATA));
            none.addClassName(CssStyles.HINT_TEXT);
            box.add(none);
            return box;
        }

        showUnchanged.setLabel(loc.getValue(L.LABEL_PART_SHOW_UNCHANGED));
        showUnchanged.addValueChangeListener(e -> refreshRows());
        attributes.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        attributes.addColumn(row -> row.change.name()).setHeader(loc.getValue(L.LABEL_NAME)).setAutoWidth(true).setFlexGrow(0);
        attributes.addColumn(row -> label(row.change.kind())).setHeader(loc.getValue(L.LABEL_PART_CHANGE)).setAutoWidth(true).setFlexGrow(0);
        attributes.addComponentColumn(row -> value(row.change.previous())).setHeader(loc.getValue(L.LABEL_PART_PREVIOUS)).setFlexGrow(1);
        attributes.addComponentColumn(row -> value(row.change.later())).setHeader(loc.getValue(L.LABEL_PART_MERGED)).setFlexGrow(1);
        attributes.addComponentColumn(this::takeBox).setHeader(loc.getValue(L.LABEL_RESULT)).setAutoWidth(true).setFlexGrow(0);
        attributes.setAllRowsVisible(true);
        attributes.setWidthFull();
        refreshRows();
        box.add(showUnchanged, attributes);
        return box;
    }

    private ComboBox<Take> takeBox(Row row) {
        ComboBox<Take> take = new ComboBox<>();
        take.setItems(Take.values());
        take.setItemLabelGenerator(this::label);
        take.setValue(row.take);
        take.setAllowCustomValue(false);
        take.setWidth("14em");
        take.addValueChangeListener(e -> {
            if (e.getValue() == null) {
                take.setValue(e.getOldValue());
            } else {
                row.take = e.getValue();
            }
        });
        return take;
    }

    private void refreshRows() {
        boolean all = showUnchanged.getValue();
        attributes.setItems(rows.stream().filter(row -> all || row.change.kind() != PartMerge.Kind.SAME).toList());
    }

    private Span value(JsonElement value) {
        Span span = new Span(show(value));
        span.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE);
        return span;
    }

    private String show(JsonElement value) {
        if (value == null) {
            return ABSENT;
        }
        return value.isJsonPrimitive() ? value.getAsString() : value.toString();
    }

    private String label(PartMerge.Join value) {
        return loc.getValue(switch (value) {
            case PARAGRAPH -> L.ENUM_PART_JOIN_PARAGRAPH;
            case LINE -> L.ENUM_PART_JOIN_LINE;
            case SPACE -> L.ENUM_PART_JOIN_SPACE;
            case NONE -> L.ENUM_PART_JOIN_NONE;
        });
    }

    private String label(PartMerge.Kind kind) {
        return loc.getValue(switch (kind) {
            case SAME -> L.ENUM_PART_CHANGE_SAME;
            case DIFFERENT -> L.ENUM_PART_CHANGE_DIFFERENT;
            case ONLY_PREVIOUS -> L.ENUM_PART_CHANGE_ONLY_PREVIOUS;
            case ONLY_LATER -> L.ENUM_PART_CHANGE_ONLY_MERGED;
        });
    }

    private String label(Take take) {
        return loc.getValue(switch (take) {
            case LATER -> L.LABEL_PART_TAKE_MERGED;
            case PREVIOUS -> L.LABEL_PART_TAKE_PREVIOUS;
            case LEFT_OUT -> L.LABEL_JSON_LEAVE_OUT;
        });
    }

    private void merge() {
        Set<List<String>> fromPrevious = new HashSet<>();
        Set<List<String>> dropped = new HashSet<>();
        for (Row row : rows) {
            switch (row.take) {
                case PREVIOUS -> fromPrevious.add(row.change.path());
                case LEFT_OUT -> dropped.add(row.change.path());
                case LATER -> {
                }
            }
        }
        TurnInput input = new TurnInput(
                blankToNull(sceneSetting.getValue()), blankToNull(povCharacter.getValue()),
                blankToNull(presentCharacters.getValue()), blankToNull(instructions.getValue()));
        try {
            ChatMessage merged = chatMessageService.mergeIntoPrevious(manuscript, part, input, join.getValue(), fromPrevious, dropped);
            close();
            Notification.show(loc.getValue(L.MSG_PART_MERGED));
            onMerged.accept(merged);
        } catch (IllegalStateException e) {
            Notification.show(loc.getValue(L.MSG_PART_MERGE_NOT_POSSIBLE));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    private static final class Row {
        private final AttributeChange change;
        private Take take;

        private Row(AttributeChange change, Take take) {
            this.change = change;
            this.take = take;
        }
    }

}
