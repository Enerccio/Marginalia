/**
 * <b>translator-common</b>: what the Conlang Translator and the Real Language Translator share, and what a plugin that keeps a story in one language and
 * lets the models read another can build on. A plain jar that each plugin embeds in its bundle ({@code Embed-Dependency translator-common;inline=true}); it
 * is never loaded as an extension. See the developer manual, <i>Plugin development / Libraries / translator-common</i>.
 * <p>
 * <b>The idea.</b> The text of the story holds the translation; the English it was made from is the {@code Span} of an anchored passage. Everything that
 * gives the story to a model reads the English ({@code text.SpeechTransformer}), the export and the reader of a published book choose
 * ({@code text.SpeechExportContributor}, {@code text.ReaderText}), and a click shows the English ({@code ui.SourcePopover}).
 * <p>
 * <b>Rules of a library</b> (each plugin has its own copy of every class): no state two plugins would have to share, what differs is a parameter (the
 * passage kind id, the mark class, the thread names, the texts of the export field), and objects are never handed from one plugin to the other.
 */
package com.github.enerccio.marginalia.extensions.translatorcommon;
