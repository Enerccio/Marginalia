package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionPrompt;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.ExtractionRuns;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * What the <i>Word extraction</i> tab needs from the extension: the runs, the parts of the active branch as they are now, and the prompts to choose from.
 *
 * @param runs    starts the extraction over a range
 * @param parts   the parts of the active branch that have text, in order
 * @param prompts the built-in prompts and the user's
 */
public record ExtractionTools(ExtractionRuns runs, Supplier<List<RangePart>> parts, Callable<List<ExtractionPrompt>> prompts) {
}
