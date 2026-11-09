/**
 * The screens the translator plugins share, built on Vaadin components with no plugin-specific text: the popover over a translated speech, the reader's
 * part of a published book, the editor of who speaks the language, a list editor and a small edit dialog, and the field of the export dialog.
 * <p>
 * The styles are {@link com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles} (the rules are in {@code translatorcommon.css}); the
 * plugin's {@code ExtensionHasCss} returns them with its own. The texts are
 * {@link com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization}, a {@code PluginLocalizationBundle} with the Japanese
 * and Chinese translations, which each plugin embeds with the library.
 */
package com.github.enerccio.marginalia.extensions.translatorcommon.ui;
