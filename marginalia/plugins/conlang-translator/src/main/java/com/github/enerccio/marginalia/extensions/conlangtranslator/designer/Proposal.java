package com.github.enerccio.marginalia.extensions.conlangtranslator.designer;

import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;

import java.util.List;

/**
 * What the designer proposes for a language: the changes to review ({@code set}, made against the language it was made for), what the model said,
 * and what was left out of its answer and why.
 *
 * @param set      the changes; empty when the answer proposes nothing
 * @param incoming the language with everything of the answer in it
 * @param message  what the model wrote to the author
 * @param dropped  the proposals that were not kept, each with the reason
 */
public record Proposal(ChangeSet set, Language incoming, String message, List<String> dropped) {
}
