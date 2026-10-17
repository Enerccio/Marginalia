package com.github.enerccio.marginalia.ui.dialogs.search;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ThreadAccessDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Dialog for a search and replace over some kind of objects, it knows nothing about what is searched. The subclass
 * supplies the options that are specific to the objects ({@link #createOptions}) and the work itself
 * ({@link #createSearcher}); the dialog does the rest, for the desktop and, with {@code phone} set, for the phone (the
 * fields and the buttons are below each other and the table has one column, in which a row shows what the columns of
 * the desktop show).
 * <p>
 * The dialog goes through states: it waits for the search ({@link State#INPUT}), searches on a worker thread with the
 * dialog frozen but the Stop button ({@link State#SEARCHING}), shows what it found with the inputs locked
 * ({@link State#PREPARED}, Search turns into Search again, which throws the found away and goes back to the first
 * state) and replaces on a worker thread with everything frozen ({@link State#REPLACING}), after which it is in the
 * first state again with an empty table.
 * <p>
 * The table lists every found object, which opens to the matches in it.
 */
@Configurable(preConstruction = true)
public abstract class SearchReplaceDialog extends ThreadAccessDialog {

    private static final Logger log = LoggerFactory.getLogger(SearchReplaceDialog.class);

    /**
     * A matched or replaced text longer than this is not shown in the table, its placeholder is.
     */
    private static final int MAX_SHOWN_TEXT = 40;

    // the progress bar is not pushed to the browser more often than this
    private static final long PROGRESS_INTERVAL = 150_000_000L;

    public enum State {
        INPUT,
        SEARCHING,
        PREPARED,
        REPLACING
    }

    /**
     * What the user typed.
     */
    public record SearchRequest(String search, String replacement, boolean regex, boolean caseSensitive) {
    }

    /**
     * An object with matches, a row of the table.
     *
     * @param type text for the type column
     * @param id   text for the id column
     * @param hits the matches in it
     */
    public record SearchReplaceItem(String type, String id, List<TextHit> hits) {
    }

    /**
     * The search, runs on a worker thread. It does not change anything, it prepares everything the replace needs.
     */
    public interface Searcher {

        SearchResult search(TaskProgress progress) throws Exception;

    }

    /**
     * The replace of what was found, runs on a worker thread.
     */
    public interface Replacer {

        /**
         * @return the message to show the user when it is done
         */
        String replace(TaskProgress progress) throws Exception;

    }

    /**
     * What a search found.
     *
     * @param items    the objects with matches
     * @param warning  question for the user to ask before the replace, null when there is nothing to warn about
     * @param replacer does the replace
     */
    public record SearchResult(List<SearchReplaceItem> items, String warning, Replacer replacer) {

        public int hitCount() {
            return items.stream().mapToInt(item -> item.hits().size()).sum();
        }
    }

    /**
     * A row of the table: an object or a match in it.
     */
    private static final class Row {
        private final SearchReplaceItem item;
        private final TextHit hit;
        private final List<Row> children;

        private Row(SearchReplaceItem item, TextHit hit, List<Row> children) {
            this.item = item;
            this.hit = hit;
            this.children = children;
        }
    }

    @Autowired
    protected Localization loc;

    protected final boolean phone;

    private final List<Component> inputs = new ArrayList<>();
    private final TaskProgress progress = new Progress();
    private volatile State state = State.INPUT;
    private volatile boolean stopRequested;
    private Runnable task;
    private SearchResult prepared;
    private Runnable onReplaced;

    protected TextField searchField;
    protected TextField replaceField;
    protected Checkbox regexBox;
    protected Checkbox caseSensitiveBox;

    private Button searchButton;
    private Button replaceButton;
    private Button stopButton;
    private Button closeButton;
    private ProgressBar progressBar;
    private Span statusLabel;
    private TreeGrid<Row> grid;

    protected SearchReplaceDialog(boolean phone) {
        super("searchReplaceThread", true);
        this.phone = phone;
    }

    /**
     * Title of the dialog.
     */
    protected abstract String getTitle();

    /**
     * Header of the second column of the table, what the second text of an {@link SearchReplaceItem} is.
     */
    protected String getIdColumnHeader() {
        return loc.getValue(L.LABEL_ID);
    }

    /**
     * Options of the search that are specific to the objects searched, shown with the regex option. They are locked
     * with the other inputs while the dialog works.
     */
    protected List<Component> createOptions() {
        return List.of();
    }

    /**
     * Makes the search of what the user typed. Runs on the UI thread, so it is the place to read the {@link #createOptions
     * options}; the search itself runs later on a worker thread.
     *
     * @throws TextSearch.InvalidSearchException to tell the user that the regex is wrong
     */
    protected abstract Searcher createSearcher(SearchRequest request) throws Exception;

    /**
     * Runs after a replace, with the UI lock: the owner shows the changed objects again.
     */
    public void onReplaced(Runnable action) {
        this.onReplaced = action;
    }

    public State getState() {
        return state;
    }

    /**
     * Builds the dialog, the subclass calls it at the end of its constructor (the options need its fields).
     */
    protected void create() {
        setHeaderTitle(getTitle());
        setWidth(phone ? "96vw" : "min(1200px, 95vw)");
        setHeight(phone ? "90vh" : "85vh");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        searchField = new TextField(loc.getValue(L.LABEL_SEARCH_FOR));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(_ -> searchField.setInvalid(false));
        replaceField = new TextField(loc.getValue(L.LABEL_REPLACE_WITH));
        replaceField.setWidthFull();
        replaceField.setClearButtonVisible(true);
        regexBox = new Checkbox(loc.getValue(L.LABEL_SEARCH_BY_REGEX), false);
        caseSensitiveBox = new Checkbox(loc.getValue(L.LABEL_CASE_SENSITIVE), true);
        // a regular expression says itself whether the case matters, with (?i)
        regexBox.addValueChangeListener(e -> caseSensitiveBox.setEnabled(!e.getValue()));

        inputs.add(searchField);
        inputs.add(replaceField);
        inputs.add(regexBox);
        inputs.add(caseSensitiveBox);

        Component options = createOptionsLayout();

        searchButton = new Button(loc.getValue(L.LABEL_SEARCH), Solid.SEARCH.create(), _ -> onSearchButton());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        replaceButton = new Button(loc.getValue(L.LABEL_REPLACE), Solid.EXCHANGE_ALT.create(), _ -> onReplaceButton());
        replaceButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Component inputArea;
        if (phone) {
            // no room at the side: the buttons are in a row below the fields
            VerticalLayout fields = new VerticalLayout(searchField, replaceField, options);
            fields.setPadding(false);
            fields.setSpacing(false);
            fields.setWidthFull();
            searchButton.setWidthFull();
            replaceButton.setWidthFull();
            // side by side when they fit, below each other when they do not
            Div buttons = new Div(searchButton, replaceButton);
            buttons.addClassName(CssStyles.SEARCH_BUTTON_ROW);
            VerticalLayout area = new VerticalLayout(fields, buttons);
            area.setPadding(false);
            area.setSpacing(true);
            area.setWidthFull();
            inputArea = area;
        } else {
            // each button is at the side of its field, on the line of the input (the label is above it); the column of
            // the buttons is as wide as the longer text, whatever the language. The elements are in the order the
            // tab key goes through them (the fields, the options, the buttons), the grid places them, see the style.
            Div area = new Div(searchField, replaceField, options, searchButton, replaceButton);
            area.addClassName(CssStyles.SEARCH_INPUT_GRID);
            searchButton.setWidthFull();
            replaceButton.setWidthFull();
            inputArea = area;
        }

        progressBar = new ProgressBar();
        progressBar.setMin(0);
        progressBar.setMax(1);
        progressBar.setWidthFull();
        stopButton = phone ? new Button(Solid.STOP.create(), _ -> stopRequested = true)
                : new Button(loc.getValue(L.LABEL_STOP), Solid.STOP.create(), _ -> stopRequested = true);
        stopButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        if (phone) {
            UIUtils.addTooltip(stopButton, loc.getValue(L.LABEL_STOP));
        }
        HorizontalLayout progressRow = new HorizontalLayout(progressBar, stopButton);
        progressRow.setWidthFull();
        progressRow.setPadding(false);
        progressRow.setAlignItems(FlexComponent.Alignment.CENTER);
        progressRow.expand(progressBar);

        statusLabel = new Span();
        statusLabel.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);

        grid = createGrid();

        VerticalLayout layout = new VerticalLayout(inputArea, progressRow, statusLabel, grid);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setFlexGrow(1, grid);
        add(layout);

        closeButton = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(closeButton);

        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                prepared = null;
            }
        });

        setState(State.INPUT);
    }

    /**
     * The options below the fields: next to each other on the desktop, below each other on the phone.
     */
    private Component createOptionsLayout() {
        List<Component> options = new ArrayList<>();
        options.add(regexBox);
        options.add(caseSensitiveBox);
        for (Component option : createOptions()) {
            options.add(option);
            inputs.add(option);
        }
        if (phone) {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            options.forEach(layout::add);
            return layout;
        }
        HorizontalLayout layout = new HorizontalLayout();
        layout.setPadding(false);
        layout.setWrap(true);
        layout.setSpacing(true);
        options.forEach(layout::add);
        return layout;
    }

    private TreeGrid<Row> createGrid() {
        TreeGrid<Row> treeGrid = new TreeGrid<>();
        treeGrid.setSizeFull();
        treeGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        if (phone) {
            // the columns would not fit: one column with all of it, the type and id first, below them the matches
            treeGrid.addComponentHierarchyColumn(this::phoneCell).setAutoWidth(false).setFlexGrow(1);
        } else {
            treeGrid.addHierarchyColumn(row -> row.item.type())
                    .setHeader(loc.getValue(L.LABEL_TYPE)).setAutoWidth(false).setFlexGrow(0).setWidth("12em");
            treeGrid.addColumn(row -> row.item.id())
                    .setHeader(getIdColumnHeader()).setAutoWidth(false).setFlexGrow(0).setWidth("10em");
            treeGrid.addComponentColumn(row -> row.hit == null ? null : context(row.hit, false))
                    .setHeader(loc.getValue(L.LABEL_SEARCH_CONTEXT)).setFlexGrow(1);
            treeGrid.addComponentColumn(row -> row.hit == null ? null : context(row.hit, true))
                    .setHeader(loc.getValue(L.LABEL_REPLACE_CONTEXT)).setFlexGrow(1);
        }
        return treeGrid;
    }

    private Component phoneCell(Row row) {
        Span title = new Span(row.item.type() + " · " + row.item.id());
        title.addClassName(row.hit == null ? CssStyles.TEXT_SEMIBOLD : CssStyles.TEXT_SECONDARY);
        VerticalLayout cell = new VerticalLayout(title);
        cell.setPadding(false);
        cell.setSpacing(false);
        if (row.hit != null) {
            cell.add(labeled(loc.getValue(L.LABEL_SEARCH), context(row.hit, false)));
            cell.add(labeled(loc.getValue(L.LABEL_REPLACE), context(row.hit, true)));
        }
        return cell;
    }

    private static Component labeled(String label, Component content) {
        Span name = new Span(label + ": ");
        name.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);
        Div div = new Div(name, content);
        div.addClassName(CssStyles.SEARCH_HIT_CONTEXT);
        return div;
    }

    /**
     * The text around a match, with the match, or with what replaces it, highlighted. The matched and the replacing
     * text are shown in whole only when short.
     */
    private Component context(TextHit hit, boolean replacement) {
        String text = oneLine(replacement ? hit.replacement() : hit.match());
        boolean tooLong = text.length() > MAX_SHOWN_TEXT;
        Span middle = new Span(tooLong ? loc.getValue(replacement ? L.LABEL_REPLACE_TEXT_PLACEHOLDER : L.LABEL_SEARCHED_TEXT_PLACEHOLDER) : text);
        middle.addClassName(replacement ? CssStyles.SEARCH_HIT_REPLACEMENT : CssStyles.SEARCH_HIT_MATCH);
        if (tooLong) {
            middle.addClassName(CssStyles.SEARCH_HIT_PLACEHOLDER);
        }
        Div div = new Div(new Span(hit.before()), middle, new Span(hit.after()));
        div.addClassName(CssStyles.SEARCH_HIT_CONTEXT);
        return div;
    }

    private static String oneLine(String text) {
        return text.replaceAll("\\s+", " ");
    }

    private void setState(State newState) {
        state = newState;
        boolean input = newState == State.INPUT;
        inputs.forEach(component -> {
            if (component instanceof HasEnabled enabled) {
                enabled.setEnabled(input);
            }
        });
        // the case of the search does not matter for a regular expression, so the box stays locked
        caseSensitiveBox.setEnabled(input && !regexBox.getValue());
        searchButton.setText(loc.getValue(newState == State.PREPARED ? L.LABEL_SEARCH_AGAIN : L.LABEL_SEARCH));
        searchButton.setEnabled(input || newState == State.PREPARED);
        replaceButton.setEnabled(newState == State.PREPARED);
        stopButton.setEnabled(newState == State.SEARCHING);
        closeButton.setEnabled(input || newState == State.PREPARED);
        grid.setEnabled(newState != State.REPLACING && newState != State.SEARCHING);
        if (input) {
            prepared = null;
            clearGrid();
            resetProgress();
        }
    }

    private void onSearchButton() {
        if (state == State.PREPARED) {
            // start over, nothing of the found is kept
            statusLabel.setText("");
            setState(State.INPUT);
            return;
        }
        if (state != State.INPUT) {
            return;
        }
        if (searchField.getValue().isEmpty()) {
            searchField.setInvalid(true);
            searchField.focus();
            return;
        }

        Searcher searcher;
        try {
            searcher = createSearcher(new SearchRequest(searchField.getValue(), replaceField.getValue(), regexBox.getValue(), caseSensitiveBox.getValue()));
        } catch (TextSearch.InvalidSearchException e) {
            Notification.error(String.format(loc.getValue(e.getKind() == TextSearch.InvalidSearchException.Kind.SEARCH
                    ? L.ERROR_INVALID_REGEX : L.ERROR_INVALID_REPLACEMENT), e.getMessage()));
            return;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }

        setState(State.SEARCHING);
        statusLabel.setText("");
        stopRequested = false;
        task = () -> {
            try {
                SearchResult result = searcher.search(progress);
                vaadinLocked(() -> searchDone(result));
            } catch (CancellationException e) {
                vaadinLocked(() -> {
                    Notification.success(loc.getValue(L.MSG_SEARCH_STOPPED));
                    setState(State.INPUT);
                });
            } catch (TextSearch.InvalidSearchException e) {
                vaadinLocked(() -> {
                    Notification.error(String.format(loc.getValue(e.getKind() == TextSearch.InvalidSearchException.Kind.SEARCH
                            ? L.ERROR_INVALID_REGEX : L.ERROR_INVALID_REPLACEMENT), e.getMessage()));
                    setState(State.INPUT);
                });
            } catch (Exception e) {
                log.error("Search failed", e);
                vaadinLocked(() -> {
                    UIUtils.internalServerError(loc, e);
                    setState(State.INPUT);
                });
            }
        };
        run();
    }

    private void searchDone(SearchResult result) {
        if (result.items().isEmpty()) {
            Notification.success(loc.getValue(L.MSG_SEARCH_NOTHING_FOUND));
            setState(State.INPUT);
            return;
        }
        prepared = result;
        List<Row> roots = new ArrayList<>();
        for (SearchReplaceItem item : result.items()) {
            List<Row> children = new ArrayList<>();
            for (TextHit hit : item.hits()) {
                children.add(new Row(item, hit, List.of()));
            }
            roots.add(new Row(item, null, children));
        }
        grid.setItems(roots, row -> row.children);
        statusLabel.setText(String.format(loc.getValue(L.MSG_SEARCH_FOUND), result.hitCount(), result.items().size()));
        progressBar.setIndeterminate(false);
        progressBar.setValue(1);
        setState(State.PREPARED);
    }

    private void onReplaceButton() {
        if (state != State.PREPARED || prepared == null) {
            return;
        }
        if (prepared.warning() != null) {
            confirm(prepared.warning(), this::replace);
        } else {
            replace();
        }
    }

    /**
     * Asks the user whether to go on with the replace in spite of the warning of the search.
     */
    protected void confirm(String warning, Runnable yes) {
        ConfirmDialog.show(warning, yes);
    }

    private void replace() {
        Replacer replacer = prepared.replacer();
        setState(State.REPLACING);
        resetProgress();
        task = () -> {
            try {
                String message = replacer.replace(progress);
                vaadinLocked(() -> {
                    Notification.success(message);
                    setState(State.INPUT);
                    statusLabel.setText("");
                    if (onReplaced != null) {
                        onReplaced.run();
                    }
                });
            } catch (Exception e) {
                log.error("Replace failed", e);
                vaadinLocked(() -> {
                    UIUtils.internalServerError(loc, e);
                    setState(State.INPUT);
                    if (onReplaced != null) {
                        // part of the changes may have been made
                        onReplaced.run();
                    }
                });
            }
        };
        run();
    }

    private void clearGrid() {
        grid.setItems(List.<Row>of(), row -> row.children);
    }

    private void resetProgress() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
    }

    @Override
    protected void runInThread() {
        task.run();
    }

    /**
     * Tells the progress bar how far the worker thread is, not more often than it is worth pushing.
     */
    private final class Progress implements TaskProgress {

        private long last;

        @Override
        public void update(long done, long total) {
            long now = System.nanoTime();
            if (done < total && now - last < PROGRESS_INTERVAL) {
                return;
            }
            last = now;
            vaadinLocked(() -> {
                if (total <= 0) {
                    progressBar.setIndeterminate(true);
                    statusLabel.setText("");
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(Math.min(1.0, (double) done / total));
                    statusLabel.setText(done + " / " + total);
                }
            });
        }

        @Override
        public boolean isCancelled() {
            return stopRequested;
        }
    }

}
