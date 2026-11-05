package com.github.enerccio.marginalia.extensions.reallangtranslator;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.PassageKinds;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.ExportTextRegistry;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PluginLog;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechPassageKind;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.SpeechTransformer;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.main.Viewer;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Translates the direct speech of the characters into a real language (Japanese, for a story set in Japan) while the story keeps the English
 * behind it, like the Conlang Translator does for an invented one; what the two share is the library {@code translator-common}, embedded in both
 * bundles. See TODO.md for the plan.
 * <p>
 * What is hooked so far: the passage kind of the translated speech (the mark, the click that shows the English) and the transformer that makes
 * every model read the English of such a speech, so that a book that already holds these passages is read right while the translating itself is
 * built.
 */
@Configurable
public class ReallangTranslatorExtension implements MarginaliaExtension, ExtensionHasCss {

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private PassageKinds passageKinds;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private ExportTextRegistry exportTexts;

    private final PassageAccess passageAccess = new PassageAccess(ReallangKind.ID);
    private final SpeechPassageKind kind = new SpeechPassageKind(ReallangKind.ID, ReallangKind.MARK);
    private final SpeechTransformer transformer = new SpeechTransformer(passageAccess);
    private String owner;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        PluginLog.set(log);
        owner = bundle.getSymbolicName();
        passageKinds.register(kind);
        partTexts.register(owner, transformer);
        log.info("Real Language Translator loaded");
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        return entryPoint instanceof Main || entryPoint instanceof Viewer ? CommonStyles.css() + "\n" + ReallangStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        passageKinds.unregister(kind);
        partTexts.unregisterAll(owner);
        exportTexts.unregisterAll(owner);
        log.info("Real Language Translator unloaded");
    }
}
