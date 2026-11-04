package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtendableMethodContext;
import com.github.enerccio.marginalia.domain.service.ExtensionService.ExtensionDecorator;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.GenerationOutcome;
import com.github.enerccio.marginalia.extensions.ExtensionHasCss;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationRuns;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangPassageKind;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangTransformer;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.main.ApplicationEntryPoint;
import com.github.enerccio.marginalia.ui.main.Main;
import com.github.enerccio.marginalia.ui.menu.StoryMenuItem;
import com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Translates the direct speech of the characters who speak a constructed language into that language and lets the reader reveal the English
 * source. The story holds the conlang; every path that gives the story to a model reads the English source back (the transformer in the
 * {@link PartTextRegistry}); the plugin keeps each translated speech as a passage of its own kind. See TODO.md for the plan and what is left.
 * <p>
 * What is hooked: the registration of the passage kind and of the transformer, the <b>Translate the speech</b> action of the menu of a part,
 * the automatic run when a generated part is final (after the automatic annotation and redraft of the book, if it has them), and the removal
 * of the passages of a part that is generated again.
 */
@Configurable
public class ConlangTranslatorExtension implements MarginaliaExtension, ExtensionHasCss {

    private static final String MENU_ITEM_KEY = "conlangtranslator.translate";

    /** The logger of this extension, its lines go to the extension log (Admin → Extensions → Log). */
    private Logger log;

    @Autowired
    private PassageKinds passageKinds;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private PassageService passageService;

    private final TranslationService translationService = new TranslationService();
    private final PassageAccess passageAccess = new PassageAccess();
    private final ConlangPassageKind kind = new ConlangPassageKind();
    private final ConlangTransformer transformer = new ConlangTransformer(passageAccess);

    private TranslationRuns runs;
    private ExtensionDecorator partFinishedDecorator;
    private Registration cleanupRegistration;
    private String owner;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        log = parentService.createLogger(bundle);
        owner = bundle.getSymbolicName();
        runs = new TranslationRuns(translationService, log);
        try {
            passageKinds.register(kind);
            partTexts.register(owner, transformer);

            menuRegistry.register(owner, StoryMenuItem.card(MENU_ITEM_KEY, l -> T.localized(TRANSLATE_THE_SPEECH),
                            (story, card) -> translateByHand(story.getCurrentManuscript(), card))
                    .icon(Solid.LANGUAGE::create));

            // a generated part is final once the automatic annotation and redraft (if the book has them) are done
            partFinishedDecorator = new ExtensionDecorator() {
                @Override
                public void onMethodEnter(Object instrumented, ExtendableMethodContext context) {
                }

                @Override
                public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
                    if (throwing == null) {
                        translateFinishedPart(context.getReflectiveFieldValue(instrumented, "manuscript", Manuscript.class),
                                context.getMethodArgument("part", ChatMessage.class),
                                context.getReflectiveFieldValue(instrumented, "onChanged", Runnable.class));
                    }
                }
            };
            extensionService.registerDecorator(partFinishedDecorator, "com.github.enerccio.marginalia.ui.dialogs.story.AutoAnnotate", "finished");

            // a part that is generated again has a new English text: the passages of the old one go
            cleanupRegistration = storyGenerationService.addEventListener(Events.AFTER_CLEANUP, this::dropRegenerated);
            log.info("Conlang Translator loaded");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load the Conlang Translator extension", e);
        }
    }

    /**
     * The action of the menu of a part: translates its speech on a worker thread and shows what came of it.
     */
    private void translateByHand(Manuscript manuscript, StoryCard card) {
        if (manuscript == null || card == null || card.getMessage() == null) {
            return;
        }
        ChatMessage part = card.getMessage();
        Notification.success(T.localized(TRANSLATING_THE_SPEECH));
        boolean started = runs.start(manuscript, part, (outcome, failure) -> {
            report(outcome, failure);
            if (outcome != null && outcome.status() == TranslationService.Status.TRANSLATED) {
                try {
                    card.updateResponse(translationService.find(part).getResponse());
                } catch (Exception e) {
                    log.warn("Failed to show the translated part: {}", e.getMessage(), e);
                }
            }
        });
        if (!started) {
            Notification.error(T.localized(THE_PART_IS_BEING_TRANSLATED));
        }
    }

    /**
     * The new part is final: when the book translates by itself, a run starts on a worker thread, and the story is loaded again when it is
     * over. Otherwise nothing happens. A failure never reaches the story.
     */
    private void translateFinishedPart(Manuscript manuscript, ChatMessage part, Runnable onChanged) {
        try {
            if (manuscript == null || part == null) {
                return;
            }
            BookSettings settings = translationService.settings(manuscript);
            if (!settings.isAuto() || settings.getLanguageId() == null) {
                return;
            }
            runs.start(manuscript, part, (outcome, failure) -> {
                report(outcome, failure);
                if (outcome != null && outcome.status() == TranslationService.Status.TRANSLATED && onChanged != null) {
                    onChanged.run();
                }
            });
        } catch (Exception e) {
            log.warn("Failed to start the translation of a finished part: {}", e.getMessage(), e);
        }
    }

    private void report(Outcome outcome, Throwable failure) {
        if (failure != null) {
            Notification.error(String.format(T.localized(THE_TRANSLATION_FAILED), failure.getMessage()));
            return;
        }
        switch (outcome.status()) {
            case TRANSLATED -> {
                Notification.success(String.format(T.localized(TRANSLATED_SPEECHES), outcome.translated(), outcome.newWords()));
                if (outcome.needsRepair() > 0) {
                    Notification.error(String.format(T.localized(SOME_WORDS_NOT_IN_THE_LEXICON), outcome.needsRepair()));
                }
            }
            case NOTHING_TO_TRANSLATE -> Notification.success(T.localized(NO_SPEECH_IN_THE_LANGUAGE));
            case NO_LANGUAGE -> Notification.error(T.localized(THE_BOOK_HAS_NO_LANGUAGE));
            case NO_MODEL -> Notification.error(T.localized(THE_BOOK_HAS_NO_MODEL));
            case EMPTY -> Notification.error(T.localized(THE_PART_HAS_NO_TEXT));
            case BAD_ANSWER -> Notification.error(T.localized(THE_MODEL_ANSWERED_WRONG));
        }
    }

    private void dropRegenerated(GenerationControllerEvent event, EventChain chain) {
        try {
            GenerationRequest request = event.getRequest();
            if (request != null && request.getRequestType() == GenerationRequestType.REGENERATE && request.getNode() != null
                    && event.getManuscript() != null && producedNewText(event)) {
                for (AnchoredPassage passage : passageService.listForParts(java.util.List.of(request.getNode().getId()), Span.KIND)) {
                    passageService.remove(event.getManuscript(), passage);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to remove the passages of a regenerated part: {}", e.getMessage(), e);
        } finally {
            chain.next();
        }
    }

    /** A regenerate that was stopped or failed before any text came leaves the old text: its passages stay. */
    private static boolean producedNewText(GenerationControllerEvent event) {
        if (event.getProperty(GenerationProperties.OUTCOME) == GenerationOutcome.COMPLETED) {
            return true;
        }
        ChatMessage original = event.getProperty(GenerationProperties.ORIGINAL_MESSAGE);
        ChatMessage now = event.getMessage();
        String text = now == null ? null : now.getResponse();
        return text != null && !text.isBlank() && (original == null || !text.equals(original.getResponse()));
    }

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the story is read in the workspace; the viewer gets its own display later
        return entryPoint instanceof Main ? ConlangStyles.css() : null;
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        passageKinds.unregister(kind);
        partTexts.unregisterAll(owner);
        menuRegistry.unregisterAll(owner);
        if (partFinishedDecorator != null) {
            extensionService.unregisterDecorator(partFinishedDecorator);
            partFinishedDecorator = null;
        }
        if (cleanupRegistration != null) {
            cleanupRegistration.unregister();
            cleanupRegistration = null;
        }
        log.info("Conlang Translator unloaded");
    }
}
