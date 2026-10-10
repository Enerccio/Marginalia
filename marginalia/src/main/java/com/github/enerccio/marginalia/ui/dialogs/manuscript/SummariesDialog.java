package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.SharedStyles;
import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.SummaryNode;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Overview of the summaries of the active branch: the ones generation uses with the summaries a meta summary stands in
 * for under them. Summaries can be edited and deleted, and merged into a meta summary.
 */
@Configurable(preConstruction = true)
@Extendable
public class SummariesDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private SummaryService summaryService;

    private final Manuscript manuscript;

    private MenuBar menuBar;
    private Span tokensSpan;
    private TreeGrid<SummaryNode> treeGrid;

    // top level summaries ticked for the meta summary
    private final Set<SummaryNode> checked = Collections.newSetFromMap(new IdentityHashMap<>());
    private List<SummaryNode> roots = List.of();

    public SummariesDialog(Manuscript manuscript) {
        this.manuscript = manuscript;

        setHeaderTitle(loc.getValue(L.LABEL_SUMMARIES));
        setWidth("90vw");
        setHeight("85vh");
        setMaxWidth("1600px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        menuBar = new MenuBar();
        populateMenuBar(menuBar);
        layout.add(createToolbar());
        layout.add(createGrid());
        layout.setFlexGrow(1, treeGrid);
        add(layout);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(closeButton);

        refresh();
    }

    /**
     * Runs when the dialog is closed, the summaries may have changed.
     */
    public void onClosed(Runnable action) {
        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                action.run();
            }
        });
    }

    /**
     * The menu bar above the grid is empty, this is where extensions add their tools (decorate this method, the
     * argument {@code bar} is the menu bar).
     */
    private void populateMenuBar(MenuBar bar) {
    }

    private HorizontalLayout createToolbar() {
        tokensSpan = new Span();
        tokensSpan.getStyle().set("font-weight", "bold");

        Button createMetaButton = new Button(loc.getValue(L.LABEL_CREATE_META_SUMMARY), Solid.LAYER_GROUP.create(), _ -> createMetaSummary());
        createMetaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout toolbar = new HorizontalLayout(menuBar, tokensSpan, createMetaButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.expand(menuBar);
        return toolbar;
    }

    private TreeGrid<SummaryNode> createGrid() {
        treeGrid = new TreeGrid<>();
        treeGrid.setSizeFull();
        treeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        treeGrid.addHierarchyColumn(node -> node.getSummary().getId() == null ? "-" : String.valueOf(node.getSummary().getId()))
                .setHeader(loc.getValue(L.LABEL_BUNDLE_ID)).setAutoWidth(true).setFlexGrow(0);
        treeGrid.addColumn(node -> node.getMessage().getId())
                .setHeader(loc.getValue(L.LABEL_MESSAGE_ID)).setAutoWidth(true).setFlexGrow(0);
        treeGrid.addColumn(SummaryNode::getOrder)
                .setHeader(loc.getValue(L.LABEL_ORDER)).setAutoWidth(true).setFlexGrow(0);
        treeGrid.addComponentColumn(SummaryContent::new)
                .setHeader(loc.getValue(L.LABEL_CONTENT)).setFlexGrow(1).setWidth("50%");
        treeGrid.addColumn(SummaryNode::getTokens)
                .setHeader(loc.getValue(L.LABEL_TOKENS)).setAutoWidth(true).setFlexGrow(0);
        treeGrid.addComponentColumn(this::createCheckbox)
                .setHeader(loc.getValue(L.LABEL_SELECT)).setAutoWidth(true).setFlexGrow(0);
        treeGrid.addComponentColumn(this::createDeleteButton).setAutoWidth(true).setFlexGrow(0);
        return treeGrid;
    }

    private boolean isTopLevel(SummaryNode node) {
        return roots.contains(node);
    }

    private Component createCheckbox(SummaryNode node) {
        if (!isTopLevel(node)) {
            return new Span();
        }
        Checkbox checkbox = new Checkbox(checked.contains(node));
        checkbox.addValueChangeListener(e -> {
            if (e.getValue()) {
                checked.add(node);
            } else {
                checked.remove(node);
            }
        });
        return checkbox;
    }

    private Component createDeleteButton(SummaryNode node) {
        if (!isTopLevel(node)) {
            return new Span();
        }
        Button delete = new Button(Solid.TRASH.create(), _ -> SummaryRemoval.confirmAndRemove(
                loc, summaryService, node.getMessage(), _ -> refresh()));
        delete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        UIUtils.addTooltip(delete, loc.getValue(L.LABEL_DELETE));
        return delete;
    }

    private void createMetaSummary() {
        List<SummaryNode> selected = checked.stream().sorted(Comparator.comparingInt(SummaryNode::getOrder)).toList();
        if (selected.size() < 2) {
            Notification.warning(loc.getValue(L.MSG_META_SUMMARY_SELECTION));
            return;
        }
        // everything between the newest and the oldest ticked summary is merged, ticked or not
        SummaryNode from = selected.getLast();
        SummaryNode to = selected.getFirst();

        SummaryDialog dialog = new SummaryDialog(manuscript, from.getMessage(), to.getMessage(), true);
        dialog.onClosed(this::refresh);
        dialog.open();
        dialog.startGeneration();
    }

    /**
     * Loads the summaries again; called after every change.
     */
    private void refresh() {
        try {
            checked.clear();
            roots = summaryService.collectTree(manuscript);
            treeGrid.setItems(roots, SummaryNode::getChildren);
            long tokens = roots.stream().mapToLong(SummaryNode::getTokens).sum();
            tokensSpan.setText(String.format(loc.getValue(L.MSG_ACTIVE_SUMMARIES_TOKENS), tokens));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Reasoning and text of a summary in a scroll area of the same height for every summary, so the rows of the grid are
     * uniform. Summaries generation uses have a pencil in the gutter next to the text that turns it into a text area.
     */
    @Extendable
    private class SummaryContent extends Div {
        private static final String HEIGHT = "150px";
        private static final String GUTTER = "40px";

        private final SummaryNode node;
        private final Div view = new Div();
        private final Div editor = new Div();
        private final VerticalLayout viewActions = new VerticalLayout();
        private final VerticalLayout editActions = new VerticalLayout();
        private final TextArea area = new TextArea();

        SummaryContent(SummaryNode node) {
            this.node = node;
            Summary summary = node.getSummary();
            setWidthFull();
            setHeight(HEIGHT);
            getStyle().set("position", "relative");
            getStyle().set("box-sizing", "border-box");
            getStyle().set("padding-right", GUTTER);

            view.setSizeFull();
            view.getStyle().set("overflow", "auto");
            view.getStyle().set("box-sizing", "border-box");

            HorizontalLayout badges = new HorizontalLayout();
            badges.setSpacing(true);
            if (node.isReplaced()) {
                badges.add(badge(loc.getValue(L.LABEL_REPLACED_SUMMARY)));
            }
            if (summary.getSummaryType() == SummaryType.META_SUMMARY) {
                badges.add(badge(loc.getValue(L.LABEL_META_SUMMARY)));
            }
            if (badges.getComponentCount() > 0) {
                view.add(badges);
            }

            if (StringUtils.isNotBlank(summary.getReasoning())) {
                Markdown reasoning = new Markdown(summary.getReasoning());
                reasoning.addClassName(SharedStyles.CHAT_MESSAGE_MARKDOWN);
                UIUtils.applyMarkdownStyles(reasoning);
                Details details = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoning);
                details.setWidthFull();
                view.add(details);
            }

            view.add(plainText(StringUtils.defaultString(summary.getSummary())));
            add(view);

            editor.setSizeFull();
            area.setSizeFull();
            editor.add(area);

            // summaries a meta summary stands in for are part of its hash, changing them would invalidate it
            if (isTopLevel(node)) {
                createActions();
            }
        }

        /**
         * Summaries are plain text, the model's single line breaks must stay line breaks (markdown would join the lines).
         */
        private Div plainText(String text) {
            Div div = new Div();
            div.setText(text);
            div.setWidthFull();
            div.getStyle().set("white-space", "pre-wrap");
            div.getStyle().set("overflow-wrap", "anywhere");
            return div;
        }

        private Span badge(String text) {
            Span badge = new Span(text);
            badge.getElement().getThemeList().add("badge");
            return badge;
        }

        private void createActions() {
            Button edit = floatingButton(Solid.PEN.create(), loc.getValue(L.LABEL_EDIT));
            edit.addClickListener(_ -> startEditing());
            viewActions.add(edit);

            Button save = floatingButton(Solid.CHECK.create(), loc.getValue(L.LABEL_SAVE));
            save.getStyle().set("color", "var(--lumo-success-color)");
            save.addClickListener(_ -> save());
            Button abandon = floatingButton(Solid.TRASH.create(), loc.getValue(L.LABEL_DISCARD_CHANGES));
            abandon.getStyle().set("color", "var(--lumo-error-color)");
            abandon.addClickListener(_ -> stopEditing());
            editActions.add(save, abandon);

            for (VerticalLayout actions : List.of(viewActions, editActions)) {
                actions.setSpacing(false);
                actions.setPadding(false);
                actions.setWidth(GUTTER);
                actions.getStyle().set("position", "absolute");
                actions.getStyle().set("top", "0");
                actions.getStyle().set("right", "0");
            }
            add(viewActions);
        }

        private Button floatingButton(Icon icon, String tooltip) {
            Button button = new Button(icon);
            button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            UIUtils.addTooltip(button, tooltip);
            return button;
        }

        private void startEditing() {
            area.setValue(StringUtils.defaultString(node.getSummary().getSummary()));
            replace(view, editor);
            replace(viewActions, editActions);
        }

        private void stopEditing() {
            replace(editor, view);
            replace(editActions, viewActions);
        }

        private void save() {
            try {
                summaryService.updateSummaryText(manuscript, node.getSummary(), area.getValue());
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
    }
}
