package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.Defaults;

import java.util.List;

/**
 * Implemented by a {@link MarginaliaExtension} that brings the built-in prompts of Marginalia in languages of its own.
 * They are registered right after the extension is loaded and removed when it is unloaded; a language with prompts is
 * offered as a <em>Prompt language</em>. A language that already has prompts is refused. The languages of the UI are
 * {@link ExtensionHasLocalization}.
 */
public interface ExtensionHasLocalizedPrompts {

    /**
     * The built-in prompts in the languages of the extension (a subclass of {@link Defaults} per language; what it does
     * not override stays English). Called when the extension is loaded.
     */
    List<Defaults> getPromptDefaults();
}
