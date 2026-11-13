package com.github.enerccio.marginalia.ui.jobs;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The rows of a grid that is refreshed by a poll every few seconds. A page left open for days polls thousands of times, so a poll that
 * finds nothing new must cost the browser nothing: the grid keeps one data provider and is touched only when what its rows show
 * changed, and the widths of the auto width columns are measured again only when the number of rows or the longest text changed.
 *
 * @param <T> the row
 */
final class PolledRows<T> {

    private final Grid<T> grid;
    private final Function<T, String> shownAs;
    private final ListDataProvider<T> provider = new ListDataProvider<>(new ArrayList<>());
    private List<String> shown;
    private int longest = -1;

    /**
     * @param shownAs everything a row shows, so that two rows with the same text are the same to the browser
     */
    PolledRows(Grid<T> grid, Function<T, String> shownAs) {
        this.grid = grid;
        this.shownAs = shownAs;
        grid.setItems(provider);
    }

    /**
     * @return whether the grid changed (false: the rows show what they showed, nothing was sent to the browser)
     */
    boolean update(List<T> rows) {
        List<String> now = rows.stream().map(shownAs).toList();
        if (now.equals(shown)) {
            return false;
        }
        boolean sameCount = shown != null && shown.size() == now.size();
        int widest = now.stream().mapToInt(String::length).max().orElse(0);
        provider.getItems().clear();
        provider.getItems().addAll(rows);
        provider.refreshAll();
        if (!sameCount || widest != longest) {
            // the counts and durations grow while a job runs: the widths measured on the first rows would cut them
            grid.recalculateColumnWidths();
        }
        shown = now;
        longest = widest;
        return true;
    }
}
