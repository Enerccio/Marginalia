package com.github.enerccio.marginalia.domain.service.agent;

import java.util.List;

/**
 * What unpacking a zip file made.
 *
 * @param files        files written
 * @param folders      folders made
 * @param bytes        size of the files written
 * @param skipped      number of entries that were left out, with the reason
 * @param skippedNames the first of them, as the name and the reason (the names are cleaned for the model)
 */
public record UnzipResult(int files, int folders, long bytes, int skipped, List<String> skippedNames) {
}
