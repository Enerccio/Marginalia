package com.github.enerccio.marginalia.domain.components;

import java.time.LocalDate;

/**
 * The counters of one day, summed over the books and sides of a filter.
 */
public record DayStatistic(LocalDate day, StatisticCounts counts) {
}
