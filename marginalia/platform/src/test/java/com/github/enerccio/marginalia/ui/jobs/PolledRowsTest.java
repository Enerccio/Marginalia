package com.github.enerccio.marginalia.ui.jobs;

import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A poll that finds nothing new sends nothing to the browser: the page of the running jobs is left open for days.
 */
class PolledRowsTest {

    private final Grid<String> grid = new Grid<>();
    private final PolledRows<String> rows = new PolledRows<>(grid, row -> row);

    @Test
    void theFirstUpdateShowsTheRowsEvenWhenThereAreNone() {
        assertThat(rows.update(List.of())).isTrue();
    }

    @Test
    void rowsThatShowTheSameAreNotSentAgain() {
        rows.update(List.of("a", "b"));

        assertThat(rows.update(List.of("a", "b"))).isFalse();
        assertThat(rows.update(List.of())).isTrue();
        assertThat(rows.update(List.of())).isFalse();
    }

    @Test
    void aChangedRowIsSent() {
        rows.update(List.of("a", "b"));

        assertThat(rows.update(List.of("a", "c"))).isTrue();
        assertThat(rows.update(List.of("a", "c", "d"))).isTrue();
        assertThat(grid.getListDataView().getItems()).containsExactly("a", "c", "d");
    }

    @Test
    void theGridKeepsOneDataProvider() {
        var provider = grid.getDataProvider();
        rows.update(List.of("a"));
        rows.update(List.of("b"));

        assertThat(grid.getDataProvider()).isSameAs(provider);
    }
}
