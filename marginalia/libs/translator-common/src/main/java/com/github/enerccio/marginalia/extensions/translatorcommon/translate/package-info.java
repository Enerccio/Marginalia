/**
 * The text functions of a translation, with no model, no database and no UI.
 * <ul>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse}: the stored text of a part with its translated passages turned
 *   back into their source, which is what every model reads.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.translate.TranslationPlan}: what a checked answer does to the text of a part (the
 *   new text and the passages in it); the reverse of the plan is the source again.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech}: one direct speech as the model answered for it.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.translate.SelectionRange}: the range a user selected, without its white space.</li>
 * </ul>
 * What decides whether a speech of the answer is <i>acceptable</i> (the quote is in the part, the words are known...) is the plugin's: it hands the
 * accepted ones to {@code TranslationPlan.make} as {@code TranslationPlan.Accepted}.
 */
package com.github.enerccio.marginalia.extensions.translatorcommon.translate;
