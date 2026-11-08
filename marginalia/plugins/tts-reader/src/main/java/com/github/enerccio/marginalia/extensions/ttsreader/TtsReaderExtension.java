package com.github.enerccio.marginalia.extensions.ttsreader;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.ttsreader.ui.TtsControl;
import com.github.enerccio.marginalia.extensions.ttsreader.ui.TtsReaderStyles;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Viewer;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;

/**
 * Read aloud in the viewer of the published books: play / pause / stop controls, read from the part that is on screen
 * (or has the selection) to the end, the next parts are loaded as the reading goes on. The speech is the one of the
 * browser ({@code speechSynthesis}); the voice, the speed and the pitch are the reader's choice, kept in the browser.
 * See {@code tts-reader.js}.
 */
public class TtsReaderExtension implements MarginaliaExtension, ExtensionHasCss {

    private Logger log;
    private ExtensionDecorator viewerDecorator;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        viewerDecorator = new ExtensionDecorator() {
            @Override
            public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
            }

            @Override
            public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                // no book (not found) or no parts to read: nothing is added
                if (throwing != null || !context.hasLocalVariable("scrollPanel") || !context.hasLocalVariable("contentWrapper")
                        || !context.hasLocalVariable("container")) {
                    return;
                }
                VerticalLayout container = context.getLocalVariable("container", VerticalLayout.class);
                var scrollPanel = context.getLocalVariable("scrollPanel", com.github.enerccio.marginalia.ui.widgets.ScrollPanel.class);
                VerticalLayout contentWrapper = context.getLocalVariable("contentWrapper", VerticalLayout.class);
                if (container == null || scrollPanel == null || contentWrapper == null) {
                    return;
                }
                container.add(new TtsControl(scrollPanel, contentWrapper, container.getElement().getAttribute("lang")));
            }
        };
        try {
            extensionService.registerDecorator(viewerDecorator, "com.github.enerccio.marginalia.ui.main.Viewer", "renderManuscriptReadingView");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Read Aloud extension", e);
        }
        log.info("Read Aloud loaded");
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        return entryPoint instanceof Viewer ? TtsReaderStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        if (viewerDecorator != null) {
            extensionService.unregisterDecorator(viewerDecorator);
            viewerDecorator = null;
        }
        log.info("Read Aloud unloaded");
    }
}
