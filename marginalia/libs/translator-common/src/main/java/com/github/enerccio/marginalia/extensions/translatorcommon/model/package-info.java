/**
 * The data of a translated speech and the choices around it. Plain objects, no service, no UI.
 * <ul>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.model.Span}: what a plugin keeps for one translated passage of a part (the source
 *   the models read, the speaker, the language, a hash of the translated text). It is stored as JSON in the attributes of an {@code AnchoredPassage}.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers} and
 *   {@link com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote}: who speaks the language in a book, with a note on each
 *   character.</li>
 *   <li>{@link com.github.enerccio.marginalia.extensions.translatorcommon.model.ReaderMode} and
 *   {@link com.github.enerccio.marginalia.extensions.translatorcommon.model.ExportMode}: what a reader of a published book, and an export, is shown for a
 *   translated speech.</li>
 * </ul>
 * The classes are Gson-friendly (fields and default constructors), so a plugin stores them in its own attributes without a converter.
 */
package com.github.enerccio.marginalia.extensions.translatorcommon.model;
