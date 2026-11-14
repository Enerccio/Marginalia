package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.AnnotateService;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.RedraftService;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.impl.generation.AfterPart;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.DesktopNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.dialog.Dialog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * The core's steps after a generated part ({@link AfterPart}), as the book sets them in {@link AutoAnnotateDialog}: the annotation
 * ({@link AfterPart.Stage#REVIEW}) and the redraft that addresses the notes ({@link AfterPart.Stage#REWRITE}). They run without a window;
 * a result that the book does not take without asking waits for the author, and its window opens in the page that generated (and from
 * the <b>Review</b> of the panel in any page of the user). Closing that window without taking the result stops what follows the part.
 */
public class AutoAnnotateSteps implements InitializingBean, DisposableBean {

    /** The steps' ids, the same as their notification sources. */
    public static final String ANNOTATE = NotificationService.ANNOTATE;
    public static final String REDRAFT = NotificationService.REDRAFT;
    /** How many notes the annotation added (a property of the phase), for the redraft. */
    static final String NOTES_ADDED = AutoAnnotateSteps.class.getName() + ".notesAdded";

    @Autowired
    private Localization loc;

    @Autowired
    private StoryGenerationService storyGenerationService;

    @Autowired
    private AnnotateService annotateService;

    @Autowired
    private RedraftService redraftService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final List<Registration> registrations = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        registrations.add(storyGenerationService.addAfterPartBinding(new AfterPart.Binding(ANNOTATE,
                () -> loc.getValue(L.LABEL_ANNOTATE_RUN), AfterPart.Stage.REVIEW, AfterPart.Mode.SEQUENTIAL, new AfterPart.Handler() {
            @Override
            public boolean appliesTo(Manuscript book) {
                return AutoAnnotate.isEnabled(book);
            }

            @Override
            public void run(AfterPart.Context context) throws Exception {
                annotate(context);
            }
        })));
        registrations.add(storyGenerationService.addAfterPartBinding(new AfterPart.Binding(REDRAFT,
                () -> loc.getValue(L.LABEL_REDRAFT), AfterPart.Stage.REWRITE, AfterPart.Mode.SEQUENTIAL, new AfterPart.Handler() {
            @Override
            public boolean appliesTo(Manuscript book) {
                return AutoAnnotate.isEnabled(book) && book.getAutoAnnotateAccept() && book.getAutoAnnotateRedraft();
            }

            @Override
            public void run(AfterPart.Context context) throws Exception {
                redraft(context);
            }
        })));
    }

    @Override
    public void destroy() {
        registrations.forEach(Registration::unregister);
        registrations.clear();
    }

    private record Found(List<AnnotateService.Proposal> proposals, String raw) {
    }

    private record Redrafted(String reasoning, String text) {
    }

    /**
     * The model annotates the part with what the book set; the notes are added, or wait for the author.
     */
    void annotate(AfterPart.Context context) throws Exception {
        Manuscript book = context.getManuscript();
        ChatMessage part = context.getPart();
        if (StringUtils.isBlank(part.getResponse())) {
            context.skip();
            return;
        }
        AI chosenAi = AutoAnnotate.byUuid(aiService.findAllForUser(), book.getAutoAnnotateModel());
        Protocol chosenProtocol = AutoAnnotate.byUuid(protocolService.findAllForUser(), book.getAutoAnnotateProtocol());
        GenerationOptions options = chosenAi != null ? GenerationOptions.defaults().withModel(chosenAi, chosenProtocol)
                : annotateService.defaultOptions();
        AI used = options.ai() != null ? options.ai() : book.getAi();
        String modelName = used == null ? null : used.getName();

        CompletableFuture<Found> done = new CompletableFuture<>();
        CancellationToken token;
        try {
            token = annotateService.annotate(book, part, new AnnotateService.Request(book.getAutoAnnotateInstructions(), null, options,
                    AnnotateService.Mode.AUTO, null, null), new AnnotateService.Callback() {
                @Override
                public void onProgress(String reasoning, String text) {
                }

                @Override
                public void onFinished(List<AnnotateService.Proposal> proposals, String raw) {
                    done.complete(new Found(proposals, raw));
                }

                @Override
                public void onUnreadable(String raw, String reason) {
                    done.completeExceptionally(new IllegalStateException(String.format(loc.getValue(L.MSG_ANNOTATE_UNREADABLE), reason)));
                }

                @Override
                public void onTerminated(String raw) {
                    done.cancel(false);
                }

                @Override
                public void onError(Throwable throwable) {
                    done.completeExceptionally(throwable);
                }
            });
        } catch (AnnotateService.ContextInsufficient e) {
            throw new IllegalStateException(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT), e);
        }
        if (token == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_ANNOTATE_FAILED));
        }
        context.onStop(token::cancel);
        Found found = context.await(done);
        context.getProperties().put(NOTES_ADDED, 0);
        if (found.proposals().isEmpty()) {
            notify(context, NotificationService.ANNOTATE, book);
            return;
        }
        if (book.getAutoAnnotateAccept()) {
            ChatMessage stored = chatMessageService.find(part);
            if (stored == null || stored.isDeleted()) {
                throw new IllegalStateException(loc.getValue(L.MSG_RUN_PART_GONE));
            }
            int made = annotateService.accept(book, stored, found.proposals(), modelName).size();
            context.getProperties().put(NOTES_ADDED, made);
            context.changed();
            notify(context, NotificationService.ANNOTATE, book);
            return;
        }
        // the author chooses the notes
        notify(context, NotificationService.ANNOTATE, book);
        AnnotateDialog.Auto auto = new AnnotateDialog.Auto(book.getAutoAnnotateInstructions(), chosenAi, chosenProtocol, false);
        review(context, (taken, closed) -> AnnotateDialog.openResult(book, part, auto, found.proposals(), found.raw(), modelName,
                context.getRun(), taken, closed));
        context.changed();
    }

    /**
     * The model writes the part again to address its open notes; the redraft is applied, or waits for the author (also when the part
     * was changed while the model wrote).
     */
    void redraft(AfterPart.Context context) throws Exception {
        Object notes = context.getProperties().get(NOTES_ADDED);
        if (!(notes instanceof Integer count) || count == 0) {
            // nothing to address: no notes were added
            context.skip();
            return;
        }
        Manuscript book = context.getManuscript();
        ChatMessage part = context.getPart();
        String base = StringUtils.defaultString(part.getResponse());
        List<Annotation> open = annotationService.listForPart(book, part).stream()
                .filter(annotation -> annotation.getStatus() == AnnotationStatus.OPEN).toList();

        CompletableFuture<Redrafted> done = new CompletableFuture<>();
        CancellationToken token;
        try {
            token = redraftService.redraft(book, part, "", null, GenerationOptions.defaults(), !open.isEmpty(), null,
                    RedraftService.Length.SAME, new RedraftService.Callback() {
                        @Override
                        public void onProgress(String reasoning, String text) {
                        }

                        @Override
                        public void onFinished(String reasoning, String text) {
                            done.complete(new Redrafted(StringUtils.defaultIfEmpty(reasoning, null), text));
                        }

                        @Override
                        public void onTerminated(String reasoning, String text) {
                            done.cancel(false);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            done.completeExceptionally(throwable);
                        }
                    });
        } catch (RedraftService.ContextInsufficient e) {
            throw new IllegalStateException(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT), e);
        }
        if (token == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_REDRAFT_FAILED));
        }
        context.onStop(token::cancel);
        Redrafted redrafted = context.await(done);
        if (StringUtils.isBlank(redrafted.text())) {
            throw new IllegalStateException(loc.getValue(L.ERROR_REDRAFT_FAILED));
        }
        notify(context, NotificationService.REDRAFT, book);
        if (book.getAutoAnnotateAcceptRedraft()) {
            ChatMessage current = chatMessageService.find(part);
            if (current == null || current.isDeleted()) {
                throw new IllegalStateException(loc.getValue(L.MSG_RUN_PART_GONE));
            }
            if (StringUtils.defaultString(current.getResponse()).equals(base)) {
                chatMessageService.updateResponse(book, current, redrafted.text(),
                        RevisionOrigin.model(RevisionSource.REDRAFT, modelAndProtocol(book), false, redrafted.reasoning()));
                for (Annotation annotation : open) {
                    annotationService.setStatus(book, annotation, AnnotationStatus.RESOLVED);
                }
                context.changed();
                return;
            }
            // the author edited the part while the model wrote: they decide
        }
        review(context, (taken, closed) -> RedraftDialog.openResult(book, part, base, redrafted.reasoning(), redrafted.text(),
                context.getRun(), _ -> taken.run(), closed));
        context.changed();
    }

    private static String modelAndProtocol(Manuscript book) {
        try {
            return (book.getAi() == null ? "" : book.getAi().getName()) + " · " + (book.getProtocol() == null ? "" : book.getProtocol().getName());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Tells the author in the page that generated, when it is there (else the panel of another page does not: it is automatic). */
    private static void notify(AfterPart.Context context, String source, Manuscript book) {
        context.accessUI(ui -> new DesktopNotifier(ui).finished(source, true, book.getName()));
    }

    /** A window of the author's decision, and the page it is in. */
    private record Shown(UI ui, Dialog dialog) {
    }

    /**
     * Waits until the author takes the result in its window. The window opens in the page that generated, if it is still there, and
     * from the panel's Review in the page of the click (a window opened in another page before is closed). Closing the window without
     * taking the result stops the phase; a stopped phase closes the window.
     *
     * @param opener opens the window: the first argument is called when the author took the result, the second when the window was
     *               closed without it
     */
    private void review(AfterPart.Context context, BiFunction<Runnable, Runnable, Dialog> opener) throws Exception {
        CompletableFuture<Boolean> decided = new CompletableFuture<>();
        AtomicReference<Shown> shown = new AtomicReference<>();
        Runnable open = () -> {
            UI ui = UI.getCurrent();
            Shown previous = shown.get();
            if (previous != null && previous.ui() == ui && previous.dialog().isAttached()) {
                return;
            }
            AtomicReference<Shown> self = new AtomicReference<>();
            Dialog dialog = opener.apply(() -> {
                if (shown.get() == self.get()) {
                    decided.complete(true);
                }
            }, () -> {
                if (shown.get() == self.get()) {
                    context.stop();
                }
            });
            self.set(new Shown(ui, dialog));
            shown.set(self.get());
            if (previous != null) {
                close(previous);
            }
        };
        context.waitForAuthor(open);
        context.accessUI(_ -> open.run());
        try {
            context.await(decided);
        } finally {
            Shown last = shown.getAndSet(null);
            if (last != null) {
                close(last);
            }
        }
    }

    private static void close(Shown shown) {
        if (UI.getCurrent() == shown.ui()) {
            shown.dialog().close();
            return;
        }
        try {
            shown.ui().access(() -> {
                shown.dialog().close();
                UIPushGuard.push(shown.ui());
            });
        } catch (UIDetachedException e) {
            // the page is gone, and the window with it
        }
    }
}
