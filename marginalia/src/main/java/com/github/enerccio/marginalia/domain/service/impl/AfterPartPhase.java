package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.impl.generation.AfterPart;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.vaadin.flow.component.UI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The second phase of one generation ({@link AfterPart}): the steps bound to the stages, for the part the generation completed. Started
 * by the engine after its cleanup, on a worker thread; it has its own stop and its own end, and never touches the generation (which is
 * over: its token, its run and its page are done with).
 */
final class AfterPartPhase {

    private static final Logger log = LoggerFactory.getLogger(AfterPartPhase.class);

    private enum State {PENDING, RUNNING, DONE, SKIPPED, STOPPED, FAILED}

    private static final class Step {
        private final AfterPart.Binding binding;
        private volatile State state = State.PENDING;

        private Step(AfterPart.Binding binding) {
            this.binding = binding;
        }

        private String name() {
            try {
                return StringUtils.defaultIfBlank(binding.name().get(), binding.id());
            } catch (RuntimeException e) {
                return binding.id();
            }
        }
    }

    private final Localization loc;
    private final ChatMessageService chatMessageService;
    private final ManuscriptService manuscriptService;
    private final ExecutorService executor;
    private final RequestAttributes attributes;
    private final GenerationListener listener;
    private final Manuscript book;
    private final ChatMessage part;
    private final GenerationRequest request;
    private final AI ai;
    private final Protocol protocol;
    private final TurnInput input;
    private final Object page;
    private final Map<String, Object> properties;
    private final List<Runnable> stopActions = new CopyOnWriteArrayList<>();
    private final List<Step> steps = new ArrayList<>();
    private volatile boolean stopped;
    private Runs.Run run;
    private int position;

    AfterPartPhase(Localization loc, ChatMessageService chatMessageService, ManuscriptService manuscriptService, ExecutorService executor,
                   RequestAttributes attributes, GenerationListener listener, Manuscript book, ChatMessage part, GenerationRequest request,
                   AI ai, Protocol protocol, TurnInput input, Object page, Map<String, Object> properties) {
        this.loc = loc;
        this.chatMessageService = chatMessageService;
        this.manuscriptService = manuscriptService;
        this.executor = executor;
        this.attributes = attributes;
        this.listener = listener;
        this.book = book;
        this.part = part;
        this.request = request;
        this.ai = ai;
        this.protocol = protocol;
        this.input = input;
        this.page = page;
        this.properties = Collections.synchronizedMap(new HashMap<>(properties));
    }

    /**
     * Starts the phase on a worker thread.
     *
     * @param bindings every binding, in the order they were made
     */
    void start(List<AfterPart.Binding> bindings) {
        List<AfterPart.Binding> ordered = new ArrayList<>(bindings);
        // by stage; inside a stage in the order of the bindings (the sort is stable)
        ordered.sort(Comparator.comparing(AfterPart.Binding::stage));
        executor.submit(() -> {
            try (InRequestScope _ = new InRequestScope(attributes)) {
                run(ordered);
            } catch (Throwable e) {
                log.error("What follows the generated part failed: {}", e.getMessage(), e);
            }
        });
    }

    private void run(List<AfterPart.Binding> bindings) throws Exception {
        Manuscript storedBook = manuscriptService.find(book);
        ChatMessage stored = chatMessageService.find(part);
        if (storedBook == null || stored == null || stored.isDeleted()) {
            return;
        }
        for (AfterPart.Binding binding : bindings) {
            try {
                if (binding.handler().appliesTo(storedBook)) {
                    steps.add(new Step(binding));
                }
            } catch (Exception e) {
                log.warn("The step {} could not say whether it applies: {}", binding.id(), e.getMessage(), e);
            }
        }
        if (steps.isEmpty()) {
            final_(stored);
            return;
        }
        position = chatMessageService.getBranchFromLeaf(stored).size();
        // the part stays where and what it is until its text is final; the cards show the phase
        run = Runs.run(Runs.AFTER_PART, this::label).book(storedBook).hold(stored, Runs.Mode.STRUCTURE).page(page)
                .onStop(this::stopped).start();
        run.setAdopts(true);
        Step failed = null;
        Throwable failure = null;
        boolean finalSent = false;
        try (Runs.Binding _ = run.bind()) {
            for (AfterPart.Stage stage : AfterPart.Stage.values()) {
                if (stopped) {
                    break;
                }
                if (!finalSent && stage.compareTo(AfterPart.Stage.EXTRACT) >= 0) {
                    // the text of the part is final: what reads it may go on, the page's hook is called
                    finalSent = true;
                    run.hold(stored, Runs.Mode.READ);
                    final_(chatMessageService.find(part));
                }
                List<Step> ofStage = steps.stream().filter(step -> step.binding.stage() == stage).toList();
                if (ofStage.isEmpty()) {
                    continue;
                }
                run.setBlocksGeneration(stage == AfterPart.Stage.REWRITE);
                for (Step step : ofStage) {
                    if (step.binding.mode() != AfterPart.Mode.SEQUENTIAL) {
                        continue;
                    }
                    if (stopped) {
                        break;
                    }
                    Throwable error = runStep(step);
                    if (error != null) {
                        failed = step;
                        failure = error;
                    }
                }
                List<Step> parallel = ofStage.stream().filter(step -> step.binding.mode() == AfterPart.Mode.PARALLEL).toList();
                if (!stopped && !parallel.isEmpty()) {
                    Map<Step, Future<Throwable>> running = new LinkedHashMap<>();
                    for (Step step : parallel) {
                        running.put(step, executor.submit(() -> {
                            try (InRequestScope _ = new InRequestScope(attributes); Runs.Binding _ = run.bind()) {
                                return runStep(step);
                            }
                        }));
                    }
                    for (Map.Entry<Step, Future<Throwable>> each : running.entrySet()) {
                        Throwable error = each.getValue().get();
                        if (error != null && failed == null) {
                            failed = each.getKey();
                            failure = error;
                        }
                    }
                }
                run.setBlocksGeneration(false);
            }
            if (!stopped && !finalSent) {
                final_(chatMessageService.find(part));
            }
        } finally {
            run.setBlocksGeneration(false);
            // told before the end: what ends it is over then
            report(failed, failure);
            run.end(failure != null ? Runs.Outcome.FAILED : stopped ? Runs.Outcome.STOPPED : Runs.Outcome.FINISHED);
        }
    }

    /** What the author is told when the phase was stopped or failed: where, and what did not run. */
    private void report(Step failed, Throwable failure) {
        String notRun = notRun();
        String rest = notRun.isEmpty() ? "" : " " + String.format(loc.getValue(L.MSG_AFTER_PART_NOT_RUN), notRun);
        if (failure != null) {
            tell(String.format(loc.getValue(L.MSG_AFTER_PART_FAILED), failed.name(), failureText(failure)) + rest);
        } else if (stopped) {
            Step at = steps.stream().filter(step -> step.state == State.STOPPED).findFirst().orElse(null);
            if (at != null) {
                tell(String.format(loc.getValue(L.MSG_AFTER_PART_STOPPED), at.name()) + rest);
            } else if (!rest.isEmpty()) {
                tell(rest.strip());
            }
        }
    }

    /**
     * Runs one step; a failure stops the phase.
     *
     * @return what it failed with, null when it did not fail
     */
    private Throwable runStep(Step step) {
        step.state = State.RUNNING;
        run.relabel();
        StepContext context;
        try {
            context = new StepContext();
        } catch (Exception e) {
            step.state = State.FAILED;
            run.stop();
            return e;
        }
        try {
            step.binding.handler().run(context);
            step.state = context.skipped ? State.SKIPPED : State.DONE;
            return null;
        } catch (AfterPart.Stopped e) {
            step.state = State.STOPPED;
            return null;
        } catch (Throwable e) {
            if (stopped) {
                step.state = State.STOPPED;
                log.debug("The step {} ended after the stop: {}", step.binding.id(), e.getMessage());
                return null;
            }
            log.warn("The step {} after the generated part failed: {}", step.binding.id(), e.getMessage(), e);
            step.state = State.FAILED;
            // a failure is a Stop: the other steps of the stage stop with it
            run.stop();
            return e;
        } finally {
            if (step.state == State.RUNNING) {
                step.state = State.STOPPED;
            }
            context.waitForAuthor(null);
            run.relabel();
        }
    }

    private static String failureText(Throwable failure) {
        Throwable cause = failure instanceof ExecutionException && failure.getCause() != null ? failure.getCause() : failure;
        return StringUtils.defaultIfBlank(cause.getMessage(), cause.getClass().getSimpleName());
    }

    /** The steps that did not run, by name. */
    private String notRun() {
        return steps.stream().filter(step -> step.state == State.PENDING).map(Step::name).collect(Collectors.joining(", "));
    }

    /** "After part 14: annotate ✓ · redraft … · translate". */
    private String label() {
        String list = steps.stream().filter(step -> step.state != State.SKIPPED).map(step -> step.name() + switch (step.state) {
            case DONE -> " ✓";
            case RUNNING -> " …";
            case FAILED, STOPPED -> " ✕";
            default -> "";
        }).collect(Collectors.joining(" · "));
        return String.format(loc.getValue(L.MSG_AFTER_PART_RUN), position, list);
    }

    private void stopped() {
        if (stopped) {
            return;
        }
        stopped = true;
        for (Runnable action : stopActions) {
            try {
                action.run();
            } catch (RuntimeException e) {
                log.debug("A stop of a step failed: {}", e.getMessage(), e);
            }
        }
    }

    /** The text-changing steps are over: the page that generated calls its hook (the old one, {@code AutoAnnotate.finished}). */
    private void final_(ChatMessage stored) {
        if (stored == null) {
            return;
        }
        try {
            listener.onPartFinal(stored);
        } catch (RuntimeException e) {
            log.debug("The page of the generation is gone: {}", e.getMessage());
        }
    }

    private void tell(String text) {
        try {
            listener.onWarning(text);
        } catch (RuntimeException e) {
            log.debug("The page of the generation is gone: {}", e.getMessage());
        }
    }

    /**
     * What a step has in its hands: the book and the part as they are stored when it starts.
     */
    private final class StepContext implements AfterPart.Context {
        private final Manuscript storedBook;
        private final ChatMessage storedPart;
        private volatile boolean skipped;

        private StepContext() throws Exception {
            this.storedBook = manuscriptService.find(book);
            this.storedPart = chatMessageService.find(part);
            if (storedBook == null || storedPart == null || storedPart.isDeleted()) {
                throw new IllegalStateException(loc.getValue(L.MSG_RUN_PART_GONE));
            }
        }

        @Override
        public Manuscript getManuscript() {
            return storedBook;
        }

        @Override
        public ChatMessage getPart() {
            return storedPart;
        }

        @Override
        public GenerationRequest getRequest() {
            return request;
        }

        @Override
        public AI getAi() {
            return ai;
        }

        @Override
        public Protocol getProtocol() {
            return protocol;
        }

        @Override
        public TurnInput getInput() {
            return input;
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public Runs.Run getRun() {
            return run;
        }

        @Override
        public boolean isStopped() {
            return stopped;
        }

        @Override
        public void onStop(Runnable action) {
            stopActions.add(action);
            if (stopped && stopActions.remove(action)) {
                action.run();
            }
        }

        @Override
        public void stop() {
            run.stop();
        }

        @Override
        public <T> T await(CompletableFuture<T> future) throws Exception {
            onStop(() -> future.cancel(true));
            try {
                return future.get();
            } catch (CancellationException e) {
                throw new AfterPart.Stopped();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof Exception cause) {
                    throw cause;
                }
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AfterPart.Stopped();
            }
        }

        @Override
        public void changed() {
            try {
                ChatMessage now = chatMessageService.find(part);
                if (now != null) {
                    listener.onPartChanged(now);
                }
            } catch (Exception e) {
                log.debug("The page of the generation could not be told: {}", e.getMessage());
            }
        }

        @Override
        public void skip() {
            skipped = true;
        }

        @Override
        public void waitForAuthor(Runnable open) {
            run.setOnOpen(open);
            run.setWaitingForUser(open != null);
        }

        @Override
        public boolean accessUI(Consumer<UI> action) {
            try {
                return listener.accessUI(action);
            } catch (RuntimeException e) {
                return false;
            }
        }
    }
}
