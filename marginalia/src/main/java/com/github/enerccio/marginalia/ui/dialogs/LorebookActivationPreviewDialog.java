package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.components.LoreConditions;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.BookLabels;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Which entries of a lorebook (and its subbooks) a turn with a given text would use, and why: the rules of the generation
 * ({@link LorebookActivationService}), the scan depth of the lorebook included, without a generation - so what extensions
 * change in a real generation is not shown. The tags of the book decide too, so a book can be picked to take them from.
 * The result is updated while the text is typed. Nothing is saved.
 */
@Configurable
public class LorebookActivationPreviewDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookActivationService activationService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TokenizerService tokenizerService;

    private final Lorebook root;

    private TextArea textField;
    private ComboBox<Manuscript> bookBox;
    private Span summary;
    // the entry the preview was opened for (the tab of the full-screen editor): its row is selected and its verdict spelled out
    private String focus;
    private Span focusLine;
    private Grid<LorebookEntryVerdict> grid;
    private final Map<Long, String> bookNames = new HashMap<>();

    /**
     * @param root the lorebook the preview starts from, as the book that uses it would
     */
    public LorebookActivationPreviewDialog(Lorebook root) {
        this.root = root;
    }

    /**
     * The preview is for this entry: its row is selected and the line above the table says whether it is used and why.
     */
    public LorebookActivationPreviewDialog focus(LorebookEntry entry) {
        this.focus = entry == null ? null : entry.getUuid();
        return this;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_PREVIEW_ACTIVATION) + ": " + StringUtils.defaultString(root.getName()));
        setWidth("1000px");
        setMaxWidth("96%");
        setHeight("85%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        Span hint = new Span(loc.getValue(L.HELP_ACTIVATION_PREVIEW));
        hint.addClassName(CssStyles.HINT_TEXT);

        textField = new TextArea(loc.getValue(L.LABEL_ACTIVATION_PREVIEW_TEXT));
        textField.setWidthFull();
        textField.setMinHeight("120px");
        textField.setValueChangeMode(ValueChangeMode.LAZY);
        textField.addValueChangeListener(event -> update());

        bookBox = new ComboBox<>(loc.getValue(L.LABEL_TAKE_TAGS_OF_BOOK));
        new BookLabels().apply(bookBox);
        bookBox.setClearButtonVisible(true);
        bookBox.setWidthFull();
        bookBox.addValueChangeListener(event -> update());
        try {
            bookBox.setItems(manuscriptService.findAllRegularForUser().stream()
                    .sorted(Comparator.comparing(book -> StringUtils.defaultString(book.getName()), String.CASE_INSENSITIVE_ORDER))
                    .toList());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        summary = new Span();
        summary.addClassName(CssStyles.HINT_TEXT);
        focusLine = new Span();
        focusLine.setVisible(false);

        grid = new Grid<>();
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addComponentColumn(verdict -> {
            var icon = (verdict.verdict().activated() ? Solid.CHECK_CIRCLE : Solid.MINUS_CIRCLE).create();
            if (verdict.verdict().activated()) {
                icon.addClassName(CssStyles.TEXT_SUCCESS);
            } else {
                icon.addClassName(CssStyles.HINT_TEXT);
            }
            return icon;
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        grid.addColumn(verdict -> StringUtils.defaultString(verdict.entry().getName()))
                .setHeader(loc.getValue(L.LABEL_ENTRY_NAME)).setFlexGrow(2);
        grid.addColumn(verdict -> bookNames.getOrDefault(verdict.entry().getId(), ""))
                .setHeader(loc.getValue(L.LABEL_LOREBOOK)).setFlexGrow(1);
        grid.addColumn(verdict -> verdict.entry().getOrder()).setHeader(loc.getValue(L.LABEL_ORDER))
                .setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);
        grid.addColumn(verdict -> verdict.depth() > 0 ? String.valueOf(verdict.depth()) : "")
                .setHeader(loc.getValue(L.LABEL_ACTIVATION_PASS)).setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);
        grid.addColumn(verdict -> reason(verdict.verdict())).setHeader(loc.getValue(L.LABEL_REASON)).setFlexGrow(3);

        Button refresh = new Button(loc.getValue(L.LABEL_REFRESH), Solid.REFRESH.create(), event -> update());
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        HorizontalLayout footer = new HorizontalLayout(refresh, close);
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.END);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        getFooter().add(footer);

        VerticalLayout content = new VerticalLayout(hint, textField, bookBox, summary, focusLine, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(grid);
        add(content);

        update();
    }

    /**
     * Evaluates the text again and shows the verdicts in the order the entries are used.
     */
    private void update() {
        try {
            List<String> tags = bookBox.getValue() == null ? List.of()
                    : tagRelationService.getTagsForObject(bookBox.getValue()).stream().map(Tag::getValue).toList();
            List<Lorebook> books = activationService.collectLorebooks(root);
            bookNames.clear();
            for (Lorebook book : books) {
                for (LorebookEntry entry : book.getCachedEntries()) {
                    bookNames.put(entry.getId(), StringUtils.defaultString(book.getName()));
                }
            }
            // the parts of the picked book's story, as far back as an entry looks
            LorebookActivationService.StoryText story = bookBox.getValue() == null ? LorebookActivationService.StoryText.NONE
                    : activationService.storyText(activationService.orderedEntries(books), bookBox.getValue().getActiveLeaf(), false);
            // the part the next turn of the picked book creates, for the entries limited to some parts
            Integer position = bookBox.getValue() == null
                    || !activationService.positionNeeded(activationService.orderedEntries(books)) ? null
                    : activationService.turnPosition(bookBox.getValue().getActiveLeaf(), false);
            List<LorebookEntryVerdict> verdicts = activationService.evaluateAll(root, books, tags,
                    StringUtils.defaultString(textField.getValue()), story, position, conditionsFor(bookBox.getValue()));
            grid.setItems(verdicts);

            long used = 0;
            long tokens = 0;
            for (LorebookEntryVerdict verdict : verdicts) {
                if (verdict.verdict().activated()) {
                    used++;
                    tokens += tokenizerService.countTokensApprox(StringUtils.defaultString(verdict.entry().getPayload()));
                }
            }
            summary.setText(String.format(loc.getValue(L.MSG_ACTIVATION_PREVIEW_SUMMARY), used, verdicts.size(), tokens));
            showFocus(verdicts);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * What the conditions of the entries are rendered against: the text as the instructions of the turn and, with a
     * book picked, the book's variables and the POV, the present characters and the setting of its last part.
     */
    private LoreConditions conditionsFor(Manuscript manuscript) throws Exception {
        LorebookTemplateData data = new LorebookTemplateData();
        data.setInstructions(StringUtils.defaultString(textField.getValue()));
        TemplateContext context = data.templateContext();
        context.setInstructions(data.getInstructions());
        if (manuscript != null) {
            ChatMessage last = manuscript.getActiveLeaf();
            if (last != null) {
                data.setPovCharacter(last.getPovCharacter());
                data.setPresentCharacters(last.getPresentCharacters());
                data.setSceneSetting(last.getSceneSetting());
                context.setPovCharacter(last.getPovCharacter());
                context.setPresentCharacters(last.getPresentCharacters());
                context.setSceneSetting(last.getSceneSetting());
                context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, last.getAttributes());
            }
            context.setManuscriptName(manuscript.getName());
            context.setManuscriptDescription(manuscript.getDescription());
            context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());
        }
        return new LoreConditions(data);
    }

    private void showFocus(List<LorebookEntryVerdict> verdicts) {
        if (focus == null) {
            return;
        }
        LorebookEntryVerdict mine = verdicts.stream().filter(verdict -> focus.equals(verdict.entry().getUuid())).findFirst().orElse(null);
        focusLine.setVisible(mine != null);
        focusLine.removeClassNames(CssStyles.TEXT_SUCCESS, CssStyles.HINT_TEXT);
        if (mine == null) {
            grid.deselectAll();
            return;
        }
        boolean used = mine.verdict().activated();
        focusLine.setText(String.format(loc.getValue(used ? L.MSG_ACTIVATION_PREVIEW_ENTRY_USED : L.MSG_ACTIVATION_PREVIEW_ENTRY_NOT_USED),
                StringUtils.defaultString(mine.entry().getName()), reason(mine.verdict())));
        focusLine.addClassName(used ? CssStyles.TEXT_SUCCESS : CssStyles.HINT_TEXT);
        grid.select(mine);
        grid.scrollToItem(mine);
    }

    private String reason(Verdict verdict) {
        String text = loc.getValue(loc.getActivationReason(verdict.reason()));
        return StringUtils.isBlank(verdict.detail()) ? text : text + " (" + verdict.detail() + ")";
    }
}
