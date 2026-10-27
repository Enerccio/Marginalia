package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
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
    private Grid<LorebookEntryVerdict> grid;
    private final Map<Long, String> bookNames = new HashMap<>();

    /**
     * @param root the lorebook the preview starts from, as the book that uses it would
     */
    public LorebookActivationPreviewDialog(Lorebook root) {
        this.root = root;
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
        bookBox.setItemLabelGenerator(book -> StringUtils.defaultString(book.getName()));
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

        VerticalLayout content = new VerticalLayout(hint, textField, bookBox, summary, grid);
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
            List<String> parts = bookBox.getValue() == null ? List.of()
                    : activationService.recentParts(bookBox.getValue().getActiveLeaf(),
                    activationService.partsNeeded(activationService.orderedEntries(books)), false);
            List<LorebookEntryVerdict> verdicts = activationService.evaluateAll(root, books, tags,
                    StringUtils.defaultString(textField.getValue()), parts);
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
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private String reason(Verdict verdict) {
        String text = loc.getValue(loc.getActivationReason(verdict.reason()));
        return StringUtils.isBlank(verdict.detail()) ? text : text + " (" + verdict.detail() + ")";
    }
}
