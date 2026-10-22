package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode.Choice;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode.Side;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonText;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.WordDiffView;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonElement;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Puts together the data an agent and the book both keep (what extensions store in attributes), the versions of it laid over each
 * other as a tree: the data when the run started, the data in the book now and the data of the agent, and what ends up in the
 * result. A place the user selects can take the data of any version, be left out or be put together from the places inside it;
 * the elements of a list are matched by what they are and the user corrects the guess. A place can be opened to work with it alone,
 * and the data of the agent in it can be edited as text, which is checked when the user goes back to the tree.
 * <p>
 * The places are made when they are opened (see {@link JsonMergeNode}). The data is shown pretty printed.
 */
@Configurable
@Extendable
public class JsonMergeDialog extends Dialog {

    @Autowired
    private Localization loc;

    /**
     * What the text view shows: the agent's data, which can be edited, or one of the other versions.
     */
    enum View {
        AGENT, RESULT, BOOK, BASE
    }

    private final String title;
    private final JsonMergeNode root;
    private final Consumer<String> onTake;

    // the place the dialog works with, the whole document at first
    private JsonMergeNode level;
    private View shownView = View.AGENT;
    // true while the dialog sets its own components
    private boolean adjusting;

    private HorizontalLayout breadcrumb;
    private Span statusLabel;
    private Checkbox onlyChanges;
    private Tabs tabs;
    private Tab treeTab;
    private Tab textTab;
    private VerticalLayout treeContent;
    private VerticalLayout textContent;
    private TreeGrid<JsonMergeNode> grid;
    private Hierarchy provider;
    private Button agentButton;
    private Button bookButton;
    private Button baseButton;
    private Button leaveOutButton;
    private Button mergedButton;
    private Button openButton;
    private Button matchButton;
    private Button splitButton;
    private Select<View> viewSelect;
    private Select<View> diffSelect;
    private TextArea textArea;
    private Span textStatus;
    private WordDiffView diffView;
    private Button takeButton;
    private Button startOverButton;

    /**
     * @param title  what the data is, for the header
     * @param root   the three versions of the data laid over each other; the dialog changes it, what the user decided stays in it
     * @param onTake called with the result as compact JSON (empty for no data) when the user takes it
     */
    public JsonMergeDialog(String title, JsonMergeNode root, Consumer<String> onTake) {
        this.title = title;
        this.root = root;
        this.level = root;
        this.onTake = onTake;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "hint", type = Span.class),
                    @APILocals(name = "top", type = HorizontalLayout.class),
                    @APILocals(name = "content", type = VerticalLayout.class),
                    @APILocals(name = "cancel", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_JSON_MERGE) + " - " + title);
        setWidth("1500px");
        setMaxWidth("96%");
        setHeight("92%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        statusLabel = new Span();
        statusLabel.addClassName(CssStyles.TEXT_SEMIBOLD);
        Span hint = new Span(loc.getValue(L.HELP_JSON_MERGE));
        hint.addClassName(CssStyles.HINT_TEXT);

        breadcrumb = new HorizontalLayout();
        breadcrumb.setSpacing(false);
        breadcrumb.setPadding(false);
        breadcrumb.setWrap(true);
        breadcrumb.setAlignItems(FlexComponent.Alignment.CENTER);

        onlyChanges = new Checkbox(loc.getValue(L.LABEL_JSON_ONLY_CHANGES), true);
        onlyChanges.addValueChangeListener(event -> refreshTree());

        treeTab = new Tab(loc.getValue(L.LABEL_TREE));
        textTab = new Tab(loc.getValue(L.LABEL_TEXT));
        tabs = new Tabs(treeTab, textTab);
        tabs.addSelectedChangeListener(event -> {
            if (adjusting) {
                return;
            }
            if (event.getPreviousTab() == textTab && !commitText()) {
                setAdjusting(() -> tabs.setSelectedTab(textTab));
                return;
            }
            showSelectedTab();
        });

        HorizontalLayout top = new HorizontalLayout(tabs, onlyChanges, statusLabel);
        top.setWidthFull();
        top.setWrap(true);
        top.setAlignItems(FlexComponent.Alignment.CENTER);

        treeContent = createTree();
        textContent = createText();
        textContent.setVisible(false);

        VerticalLayout content = new VerticalLayout(hint, breadcrumb, top, treeContent, textContent);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S);
        content.expand(treeContent, textContent);
        add(content);

        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        startOverButton = new Button(loc.getValue(L.LABEL_JSON_START_OVER), event -> startOver());
        takeButton = new Button(loc.getValue(L.LABEL_JSON_TAKE_RESULT), event -> take());
        takeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        HorizontalLayout footer = new HorizontalLayout(startOverButton, cancel, takeButton);
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        refreshAll();
    }

    // --- the tree

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = VerticalLayout.class),
            locals = {@APILocals(name = "toolbar", type = HorizontalLayout.class),
                    @APILocals(name = "layout", type = VerticalLayout.class)})
    private VerticalLayout createTree() {
        agentButton = small(L.LABEL_USE_AGENT_VERSION, event -> choose(Choice.AGENT));
        bookButton = small(L.LABEL_USE_BOOK_VERSION, event -> choose(Choice.BOOK));
        baseButton = small(L.LABEL_JSON_USE_BASE, event -> choose(Choice.BASE));
        leaveOutButton = small(L.LABEL_JSON_LEAVE_OUT, event -> choose(Choice.NONE));
        mergedButton = small(L.LABEL_JSON_PUT_TOGETHER, event -> choose(Choice.MERGED));
        openButton = small(L.LABEL_OPEN, event -> open(selected()));
        matchButton = small(L.LABEL_JSON_MATCH_WITH, event -> match(selected()));
        splitButton = small(L.LABEL_JSON_SPLIT, event -> split(selected()));
        HorizontalLayout toolbar = new HorizontalLayout(agentButton, bookButton, baseButton, leaveOutButton, mergedButton, openButton,
                matchButton, splitButton);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        provider = new Hierarchy();
        grid = new TreeGrid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setDataProvider(provider);
        grid.addHierarchyColumn(JsonMergeNode::title).setHeader(loc.getValue(L.LABEL_JSON_PLACE)).setFlexGrow(0).setWidth("260px")
                .setResizable(true);
        grid.addComponentColumn(this::statusBadge).setHeader("").setFlexGrow(0).setWidth("140px");
        if (!UIUtils.isPhone()) {
            addVersionColumn(Side.BASE, L.LABEL_MERGE_BEFORE_RUN);
        }
        addVersionColumn(Side.BOOK, L.LABEL_MERGE_IN_BOOK_NOW);
        addVersionColumn(Side.AGENT, L.ENUM_DOMAIN_AGENT);
        grid.addComponentColumn(this::resultCell).setHeader(loc.getValue(L.LABEL_RESULT)).setFlexGrow(1).setResizable(true);
        grid.addComponentColumn(this::choiceCell).setHeader("").setFlexGrow(0).setWidth("130px");
        grid.asSingleSelect().addValueChangeListener(event -> updateButtons());
        grid.addItemDoubleClickListener(event -> open(event.getItem()));

        VerticalLayout layout = new VerticalLayout(toolbar, grid);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S);
        layout.setSizeFull();
        layout.expand(grid);
        return layout;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "side", type = JsonMergeNode.Side.class),
                    @APIParameter(name = "header", type = L.class)})
    private void addVersionColumn(Side side, L header) {
        grid.addComponentColumn(node -> versionCell(node, side)).setHeader(loc.getValue(header)).setFlexGrow(1).setResizable(true);
    }

    /**
     * The places shown under a place: those that changed, or all of them.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "parent", type = JsonMergeNode.class)},
            locals = {@APILocals(name = "all", type = List.class),
                    @APILocals(name = "changed", type = List.class)})
    private List<JsonMergeNode> visibleChildren(JsonMergeNode parent) {
        if (parent == null) {
            return List.of(level);
        }
        List<JsonMergeNode> all = parent.children();
        if (!onlyChanges.getValue()) {
            return all;
        }
        List<JsonMergeNode> changed = all.stream().filter(JsonMergeNode::changed).toList();
        // a place that is not the same, its parts all are (a list in another order): they are what there is to look at
        return changed.isEmpty() && parent.changed() ? all : changed;
    }

    private final class Hierarchy extends AbstractBackEndHierarchicalDataProvider<JsonMergeNode, Void> {
        @Override
        public int getChildCount(HierarchicalQuery<JsonMergeNode, Void> query) {
            return visibleChildren(query.getParent()).size();
        }

        @Override
        protected Stream<JsonMergeNode> fetchChildrenFromBackEnd(HierarchicalQuery<JsonMergeNode, Void> query) {
            return visibleChildren(query.getParent()).stream().skip(query.getOffset()).limit(query.getLimit());
        }

        @Override
        public boolean hasChildren(JsonMergeNode item) {
            return item.hasChildren() && (!onlyChanges.getValue() || item.changed());
        }
    }

    /**
     * What a version has in a place on one line; an object or a list only says how many places it has.
     */
    static String preview(JsonElement value) {
        if (value == null) {
            return "—";
        }
        if (value.isJsonObject()) {
            return "{" + value.getAsJsonObject().size() + "}";
        }
        if (value.isJsonArray()) {
            return "[" + value.getAsJsonArray().size() + "]";
        }
        String text = JsonText.compact(value);
        return text.length() > 80 ? text.substring(0, 80) + "…" : text;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "value", type = JsonElement.class)},
            locals = {@APILocals(name = "cell", type = Span.class),
                    @APILocals(name = "full", type = String.class)})
    private Span cell(JsonElement value) {
        Span cell = new Span(preview(value));
        cell.addClassName(CssStyles.JSON_MERGE_CELL);
        if (value == null) {
            cell.addClassName(CssStyles.JSON_MERGE_ABSENT);
        } else {
            String full = JsonText.pretty(value);
            cell.setTitle(full.length() > 600 ? full.substring(0, 600) + "…" : full);
        }
        return cell;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class),
                    @APIParameter(name = "side", type = JsonMergeNode.Side.class)},
            locals = {@APILocals(name = "cell", type = Span.class)})
    private Component versionCell(JsonMergeNode node, Side side) {
        Span cell = cell(node.get(side));
        boolean changed = side == Side.AGENT ? node.agentChanged() : side == Side.BOOK && node.bookChanged();
        if (changed) {
            cell.addClassName(node.conflict() ? CssStyles.JSON_MERGE_CELL_CONFLICT
                    : side == Side.AGENT ? CssStyles.JSON_MERGE_CELL_AGENT : CssStyles.JSON_MERGE_CELL_BOOK);
        }
        return cell;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)})
    private Component resultCell(JsonMergeNode node) {
        return cell(node.effectiveResult());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)},
            locals = {@APILocals(name = "choice", type = Span.class)})
    private Component choiceCell(JsonMergeNode node) {
        Span choice = new Span(loc.getValue(choiceText(node.effectiveChoice())));
        if (node.isOverruled()) {
            choice.addClassName(CssStyles.JSON_MERGE_ABSENT);
        } else if (node.conflict() && !node.isDecided() && node.container() == null) {
            choice.addClassName(CssStyles.TEXT_ERROR);
        }
        return choice;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)},
            locals = {@APILocals(name = "badge", type = Span.class)})
    Span statusBadge(JsonMergeNode node) {
        Span badge = new Span(loc.getValue(statusText(node.status())));
        badge.getElement().getThemeList().add("badge small " + switch (node.status()) {
            case CONFLICT -> "error";
            case UNCHANGED -> "contrast";
            case SAME, BOOK -> "success";
            case AGENT, BOTH -> "";
        });
        return badge;
    }

    private static L statusText(JsonMergeNode.Status status) {
        return switch (status) {
            case UNCHANGED -> L.ENUM_JSON_STATUS_UNCHANGED;
            case AGENT -> L.ENUM_JSON_STATUS_AGENT;
            case BOOK -> L.ENUM_JSON_STATUS_BOOK;
            case BOTH -> L.ENUM_JSON_STATUS_BOTH;
            case SAME -> L.ENUM_JSON_STATUS_SAME;
            case CONFLICT -> L.ENUM_MERGE_STATE_CONFLICT;
        };
    }

    private static L choiceText(Choice choice) {
        return switch (choice) {
            case MERGED -> L.ENUM_JSON_CHOICE_MERGED;
            case AGENT -> L.ENUM_JSON_CHOICE_AGENT;
            case BOOK -> L.ENUM_JSON_CHOICE_BOOK;
            case BASE -> L.ENUM_JSON_CHOICE_BASE;
            case NONE -> L.ENUM_JSON_CHOICE_NONE;
        };
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "listener", type = ComponentEventListener.class)},
            locals = {@APILocals(name = "button", type = Button.class)})
    private Button small(L label, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(loc.getValue(label), listener);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        return button;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = JsonMergeNode.class))
    private JsonMergeNode selected() {
        return grid.asSingleSelect().getValue() == null ? level : grid.asSingleSelect().getValue();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "node", type = JsonMergeNode.class)})
    private void updateButtons() {
        JsonMergeNode node = selected();
        mergedButton.setEnabled(node.container() != null && node.choice() != Choice.MERGED);
        openButton.setEnabled(node != level);
        matchButton.setEnabled(node.isListElement() && !node.matchCandidates().isEmpty());
        splitButton.setEnabled(node.canSplit());
    }

    // --- what the user does with a place

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "choice", type = JsonMergeNode.Choice.class)})
    private void choose(Choice choice) {
        selected().choose(choice);
        changed();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)},
            locals = {@APILocals(name = "candidates", type = List.class),
                    @APILocals(name = "pick", type = Dialog.class),
                    @APILocals(name = "select", type = Select.class),
                    @APILocals(name = "ok", type = Button.class)})
    private void match(JsonMergeNode node) {
        List<JsonMergeNode> candidates = node.matchCandidates();
        if (candidates.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_JSON_NO_MATCH));
            return;
        }
        Dialog pick = new Dialog();
        pick.setHeaderTitle(loc.getValue(L.LABEL_JSON_MATCH_WITH));
        pick.setModality(ModalityMode.STRICT);
        Select<JsonMergeNode> select = new Select<>();
        select.setLabel(String.format(loc.getValue(L.MSG_JSON_MATCH_PICK), node.title()));
        select.setWidthFull();
        select.setItems(candidates);
        select.setItemLabelGenerator(candidate -> candidate.title() + " (" + sidesOf(candidate) + ") " + preview(firstOf(candidate)));
        select.setValue(candidates.getFirst());
        Button ok = new Button(loc.getValue(L.LABEL_JSON_MATCH_WITH), event -> {
            JsonMergeNode joined = node.match(select.getValue());
            pick.close();
            changed();
            grid.select(joined);
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        pick.add(select);
        pick.getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), event -> pick.close()), ok);
        pick.open();
    }

    private static JsonElement firstOf(JsonMergeNode node) {
        return node.get(Side.AGENT) != null ? node.get(Side.AGENT) : node.get(Side.BOOK) != null ? node.get(Side.BOOK) : node.get(Side.BASE);
    }

    /**
     * Which versions have the element, in words.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)},
            locals = {@APILocals(name = "sides", type = List.class)})
    private String sidesOf(JsonMergeNode node) {
        List<String> sides = new ArrayList<>();
        if (node.get(Side.AGENT) != null) {
            sides.add(loc.getValue(L.ENUM_JSON_CHOICE_AGENT));
        }
        if (node.get(Side.BOOK) != null) {
            sides.add(loc.getValue(L.ENUM_JSON_CHOICE_BOOK));
        }
        if (node.get(Side.BASE) != null) {
            sides.add(loc.getValue(L.ENUM_JSON_CHOICE_BASE));
        }
        return String.join(" + ", sides);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)},
            locals = {@APILocals(name = "parts", type = List.class)})
    private void split(JsonMergeNode node) {
        List<JsonMergeNode> parts = node.split();
        changed();
        if (!parts.isEmpty()) {
            grid.select(parts.getFirst());
        }
    }

    /**
     * Works with the place alone: the tree starts at it and the text is its data.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "node", type = JsonMergeNode.class)})
    private void open(JsonMergeNode node) {
        if (node == null || node == level || !commitText()) {
            return;
        }
        level = node;
        refreshAll();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void startOver() {
        // what was written in the text is given up with the rest
        root.reset();
        level = root;
        refreshAll();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "result", type = String.class)})
    private void take() {
        if (!commitText()) {
            return;
        }
        String result = JsonText.compact(root.result());
        close();
        onTake.accept(result);
    }

    // --- showing it

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "action", type = Runnable.class)})
    private void setAdjusting(Runnable action) {
        boolean before = adjusting;
        adjusting = true;
        try {
            action.run();
        } finally {
            adjusting = before;
        }
    }

    /**
     * Everything is shown again: the level changed or the document did.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "path", type = List.class),
                    @APILocals(name = "node", type = JsonMergeNode.class),
                    @APILocals(name = "button", type = Button.class)})
    private void refreshAll() {
        breadcrumb.removeAll();
        List<JsonMergeNode> path = level.path();
        for (int i = 0; i < path.size(); i++) {
            JsonMergeNode node = path.get(i);
            Button button = new Button(node.title(), event -> open(node));
            button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            button.setEnabled(node != level);
            if (i > 0) {
                breadcrumb.add(new Span("›"));
            }
            breadcrumb.add(button);
        }
        refreshTree();
        refreshText();
        updateStatus();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void refreshTree() {
        provider.refreshAll();
        grid.expand(level);
        updateButtons();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void updateStatus() {
        statusLabel.setText(String.format(loc.getValue(L.MSG_JSON_MERGE_STATUS), root.conflicts(), root.unresolved()));
    }

    /**
     * Something was decided or changed: what is shown follows.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void changed() {
        provider.refreshAll();
        updateStatus();
        updateButtons();
        if (textContent.isVisible()) {
            refreshText();
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void showSelectedTab() {
        boolean text = tabs.getSelectedTab() == textTab;
        treeContent.setVisible(!text);
        textContent.setVisible(text);
        if (text) {
            refreshText();
        } else {
            refreshTree();
        }
    }

    // --- the text

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = VerticalLayout.class),
            locals = {@APILocals(name = "applyText", type = Button.class),
                    @APILocals(name = "selects", type = HorizontalLayout.class),
                    @APILocals(name = "layout", type = VerticalLayout.class)})
    private VerticalLayout createText() {
        viewSelect = new Select<>();
        viewSelect.setLabel(loc.getValue(L.LABEL_JSON_SHOW));
        viewSelect.setItems(View.values());
        viewSelect.setItemLabelGenerator(this::viewText);
        viewSelect.setValue(View.AGENT);
        viewSelect.addValueChangeListener(event -> {
            if (adjusting) {
                return;
            }
            if (event.getOldValue() == View.AGENT && !commitText()) {
                setAdjusting(() -> viewSelect.setValue(View.AGENT));
                return;
            }
            shownView = event.getValue();
            refreshText();
        });

        diffSelect = new Select<>();
        diffSelect.setLabel(loc.getValue(L.LABEL_JSON_COMPARE_WITH));
        diffSelect.setItems(View.RESULT, View.BOOK, View.BASE);
        // the empty choice is shown with no label
        diffSelect.setItemLabelGenerator(view -> view == null ? "" : viewText(view));
        diffSelect.setEmptySelectionAllowed(true);
        diffSelect.addValueChangeListener(event -> updateDiff());

        Button applyText = new Button(loc.getValue(L.LABEL_JSON_APPLY_TEXT), event -> {
            if (commitText()) {
                refreshText();
            }
        });
        applyText.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout selects = new HorizontalLayout(viewSelect, diffSelect, applyText);
        selects.setAlignItems(FlexComponent.Alignment.END);
        selects.setWrap(true);

        textArea = new TextArea();
        textArea.setWidthFull();
        textArea.setMinRows(10);
        textArea.addClassName(CssStyles.JSON_MERGE_TEXT);
        textArea.setValueChangeMode(ValueChangeMode.LAZY);
        textArea.setValueChangeTimeout(500);
        textArea.addValueChangeListener(event -> {
            if (!adjusting) {
                checkText();
                updateDiff();
            }
        });

        textStatus = new Span();
        diffView = new WordDiffView(loc.getValue(L.MSG_MERGE_NO_DIFFERENCE));

        VerticalLayout layout = new VerticalLayout(selects, textArea, textStatus, diffView);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S, CssStyles.OVERFLOW_AUTO);
        layout.setSizeFull();
        layout.expand(textArea);
        return layout;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "view", type = JsonMergeDialog.View.class)})
    private String viewText(View view) {
        return loc.getValue(switch (view) {
            case AGENT -> L.LABEL_JSON_AGENT_DATA;
            case RESULT -> L.LABEL_RESULT;
            case BOOK -> L.LABEL_MERGE_IN_BOOK_NOW;
            case BASE -> L.LABEL_MERGE_BEFORE_RUN;
        });
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "view", type = JsonMergeDialog.View.class)})
    private String textOf(View view) {
        return switch (view) {
            case AGENT -> level.agentText();
            case RESULT -> JsonText.pretty(level.effectiveResult());
            case BOOK -> JsonText.pretty(level.get(Side.BOOK));
            case BASE -> JsonText.pretty(level.get(Side.BASE));
        };
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void refreshText() {
        setAdjusting(() -> {
            viewSelect.setValue(shownView);
            textArea.setReadOnly(shownView != View.AGENT);
            textArea.setLabel(viewText(shownView));
            textArea.setValue(textOf(shownView));
        });
        checkText();
        updateDiff();
    }

    /**
     * Says whether the text is JSON, as it is written.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = JsonText.InvalidJsonException.class)})
    private void checkText() {
        textArea.setInvalid(false);
        textStatus.removeClassNames(CssStyles.TEXT_ERROR, CssStyles.TEXT_SUCCESS);
        if (shownView != View.AGENT) {
            textStatus.setText("");
            return;
        }
        try {
            JsonText.parse(textArea.getValue());
            textStatus.setText(loc.getValue(L.MSG_JSON_VALID));
            textStatus.addClassName(CssStyles.TEXT_SUCCESS);
        } catch (JsonText.InvalidJsonException e) {
            textArea.setInvalid(true);
            textArea.setErrorMessage(e.getMessage());
            textStatus.setText(String.format(loc.getValue(L.MSG_JSON_INVALID), e.getMessage()));
            textStatus.addClassName(CssStyles.TEXT_ERROR);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "against", type = JsonMergeDialog.View.class)})
    private void updateDiff() {
        View against = diffSelect.getValue();
        diffView.setVisible(against != null);
        if (against != null) {
            diffView.show(textOf(against), textArea.getValue());
        }
    }

    /**
     * Takes the text the user wrote as the data of the agent in the open place, when it is JSON; else says what is wrong and nothing
     * changes. True when there is nothing left to take or it was taken.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "text", type = String.class),
                    @APILocals(name = "written", type = JsonElement.class),
                    @APILocals(name = "e", type = JsonText.InvalidJsonException.class)})
    boolean commitText() {
        if (textArea == null || shownView != View.AGENT) {
            return true;
        }
        String text = textArea.getValue();
        try {
            JsonElement written = JsonText.parse(text);
            if (!JsonText.compact(written).equals(JsonText.compact(level.get(Side.AGENT)))) {
                level.setAgentText(text);
                changed();
            }
            return true;
        } catch (JsonText.InvalidJsonException e) {
            checkText();
            Notification.warning(String.format(loc.getValue(L.MSG_JSON_FIX_FIRST), e.getMessage()));
            return false;
        }
    }

    // for the tests of the dialog

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = JsonMergeNode.class))
    JsonMergeNode getLevel() {
        return level;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TreeGrid.class))
    TreeGrid<JsonMergeNode> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextArea.class))
    TextArea getTextArea() {
        return textArea;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Tabs.class))
    Tabs getTabs() {
        return tabs;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Tab.class))
    Tab getTextTab() {
        return textTab;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getOnlyChanges() {
        return onlyChanges;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class))
    Span getStatusLabel() {
        return statusLabel;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Select.class))
    Select<View> getViewSelect() {
        return viewSelect;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getTakeButton() {
        return takeButton;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getStartOverButton() {
        return startOverButton;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = HorizontalLayout.class))
    HorizontalLayout getBreadcrumb() {
        return breadcrumb;
    }
}
