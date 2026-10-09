package com.github.enerccio.marginalia.extensions.authorsnote;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.OsgiServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.authorsnote.model.AuthorsNoteData;
import com.github.enerccio.marginalia.extensions.authorsnote.service.AuthorsNoteService;
import com.github.enerccio.marginalia.extensions.authorsnote.ui.AuthorsNoteView;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ComponentUtil;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Configurable
public class AuthorsNoteExtension implements MarginaliaExtension {

    private static final Logger log = LoggerFactory.getLogger(AuthorsNoteExtension.class);

    private static final String AUTHORS_NOTE_VIEW_KEY = AuthorsNoteData.KEY + ".view";

    @Autowired
    private StoryGenerationService storyGenerationService;

    private final AuthorsNoteService authorsNoteService = new AuthorsNoteService();

    private ExtensionDecorator storyPartDecorator;
    private Registration payloadRegistration;

    private final Set<VTabSheet> activeTabSheets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        try {
            // the Author's Note tab in the left sidebar of the story view, next to the outline
            storyPartDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) throws Exception {}

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    VTabSheet leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", VTabSheet.class);
                    Manuscript currentManuscript = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);

                    if (leftBar != null && currentManuscript != null) {
                        if (ComponentUtil.getData(leftBar, AUTHORS_NOTE_VIEW_KEY) != null) {
                            return;
                        }

                        AuthorsNoteView view = new AuthorsNoteView(currentManuscript, authorsNoteService);
                        leftBar.add("Author's Note", view);

                        ComponentUtil.setData(leftBar, AUTHORS_NOTE_VIEW_KEY, view);
                        activeTabSheets.add(leftBar);
                    }
                }
            };

            extensionService.registerDecorator(
                    storyPartDecorator,
                    "com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart",
                    "renderStoryContent"
            );

            // the payload is complete after PREPARE_PAYLOAD, what the listener sets is sent to the model
            payloadRegistration = storyGenerationService.addEventListener(Events.AFTER_PREPARE_PAYLOAD,
                    this::insertAuthorsNote);

        } catch (Exception e) {
            // there may be no UI (loading at startup), the extension service logs it and skips the extension
            throw new IllegalStateException("Failed to load the Author's Note extension", e);
        }
    }

    private void insertAuthorsNote(GenerationControllerEvent event, EventChain chain) {
        try {
            // listeners are global - every generation of every user comes here, the note is per book
            Manuscript manuscript = event.getManuscript();
            if (manuscript != null) {
                AuthorsNoteData data = authorsNoteService.loadCurrent(manuscript);
                event.setPayload(authorsNoteService.insertNote(event.getPayload(), data));
            }
        } catch (Exception e) {
            // generate without the note rather than fail the generation
            log.warn("Failed to insert the author's note: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        if (storyPartDecorator != null) {
            extensionService.unregisterDecorator(storyPartDecorator);
        }
        if (payloadRegistration != null) {
            payloadRegistration.unregister();
            payloadRegistration = null;
        }

        // remove the tab from story views that are open
        synchronized (activeTabSheets) {
            for (VTabSheet leftBar : activeTabSheets) {
                // the components belong to the UIs of all users - change each one in its own session
                UIUtils.accessComponent(leftBar, () -> {
                    AuthorsNoteView view = (AuthorsNoteView) ComponentUtil.getData(leftBar, AUTHORS_NOTE_VIEW_KEY);
                    if (view != null) {
                        leftBar.remove(view);
                        ComponentUtil.setData(leftBar, AUTHORS_NOTE_VIEW_KEY, null);
                    }
                });
            }
            activeTabSheets.clear();
        }
    }
}
