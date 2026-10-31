package com.github.enerccio.marginalia.domain.components;

/**
 * The counters of the books of one folder over a range, by the exact path the rows keep (empty for the books in no folder).
 */
public record FolderStatistic(String folderPath, StatisticCounts counts) {
}
