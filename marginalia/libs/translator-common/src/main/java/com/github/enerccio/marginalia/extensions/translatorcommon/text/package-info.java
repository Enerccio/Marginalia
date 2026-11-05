/**
 * The text of a translated story for the places that are not the story card: the models, the export and the reader of a published book.
 * <ul>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechTransformer}: a {@code PartTextRegistry.Transformer} that makes every
 *   model read the source of a translated speech.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechExportContributor} and
 *   {@link com.github.enerccio.marginalia.extensions.translatorcommon.text.ExportText}: what the export changes in the text (the core's
 *   {@code ExportTextRegistry}).</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.text.ReaderText}: the markdown the reader of a published book gets.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechPassageKind}: the passage kind a plugin registers (its own id and mark).</li>
 * </ul>
 * A passage is used only when the text at its range still has the hash it was made for ({@code Span.isEdited}): the author's edits stay safe, and the passage
 * of another plugin whose range was shifted drops out quietly.
 */
package com.github.enerccio.marginalia.extensions.translatorcommon.text;
