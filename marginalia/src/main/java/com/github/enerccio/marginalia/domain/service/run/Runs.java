package com.github.enerccio.marginalia.domain.service.run;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.job.Jobs;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The registry of the runs that work on the books now: a generation, the window of a book, the windows that annotate or redraft a part,
 * a translation. In memory (a restart ends every run, and with them what they held); static like {@link
 * com.github.enerccio.marginalia.domain.service.job.Jobs}, so the libraries the plugins embed and the base of the repositories reach the
 * same one.
 * <p>
 * A run <b>holds</b> objects in a {@link Mode}, and the holds are what the rest asks about: {@link #check} refuses an {@link Operation}
 * that would pull a held object from under its run (the base {@code delete} of every repository asks it, so a service or a plugin
 * cannot forget it), and {@link #busyWith} tells the cards of the story which of their parts a run works on. A hold is never stored:
 * it lives with its run and ends with it ({@link Run#end()}, in the {@code finally} of the work), so it cannot be stuck apart from it.
 * <p>
 * The agent lock is not here: it is a state of the agent's run in the database ({@code AgentRunService.findLockingRun}), checked by the
 * services that change a book.
 */
public final class Runs {

    private static final Logger log = LoggerFactory.getLogger(Runs.class);

    /**
     * How a run holds an object.
     */
    public enum Mode {
        /** It writes the object: no delete. */
        WRITE,
        /** The object must stay where and what it is (a window works on the part): no delete; its text may be saved. */
        STRUCTURE,
        /** It only reads the object: nothing is refused, a delete ends the run. */
        READ,
        /** The window of the book: only one per book; a delete, a restore or an import of the book is refused to other pages. */
        OPEN
    }

    /**
     * What is asked of a held object.
     */
    public enum Operation {
        /** Delete it (soft or hard); for a book also everything in it. */
        DELETE,
        /** Replace the book as a whole: restore a backup into it, import into it. */
        REPLACE,
        /** Generate the next part of the book. */
        GENERATE,
        /** Open the window of the book. */
        OPEN,
        /** Write the object (a translation of a part): refused while another run writes it. */
        WRITE,
        /** Change the text of a part by hand (an edit, a redraft or a translation applied, a merge into it): refused while a generation writes it. */
        EDIT,
        /** Change where a part is in the tree (merge it into the one before, insert a part after it): refused by a hold that writes it or needs it to stay. */
        MOVE,
        /** Change the end of the active branch of a book (swipe, switch to a branch): refused while a generation writes the book's next part. */
        SWITCH
    }

    /**
     * Where a generation is. Before the inference it builds the prompt from one state of the book: its page holds everything; from the
     * inference on it holds only the part it writes and the branch above it.
     */
    public enum Stage {
        /** Building the prompt and creating the part (seconds). */
        PREPARE,
        /** The model writes the part. */
        INFERENCE
    }

    /** The source of a generation of the story. */
    public static final String GENERATION = "generation";
    /** The source of the window of a book. */
    public static final String BOOK_WINDOW = "book-window";
    /** The source of what follows a generated part (its annotation, redraft, translation...): the second phase of the generation. */
    public static final String AFTER_PART = "after-part";

    /**
     * What a run holds: the object (its class and id) and the book it is in.
     */
    public record Hold(Class<?> type, Long id, Long bookId, Mode mode) {
    }

    /**
     * A conflicting operation: the run that is in the way.
     */
    public static final class HeldException extends com.github.enerccio.marginalia.domain.service.ManuscriptLockedException {
        private final transient Run run;

        HeldException(Run run) {
            super("In use by a run: " + run.getLabel(), run.getLabel());
            this.run = run;
        }

        /** The run that holds the object. */
        public Run getRun() {
            return run;
        }
    }

    /**
     * How a run ended, for the notification the panel sends when the page that started it is gone.
     */
    public enum Outcome {
        FINISHED, FAILED, STOPPED
    }

    /**
     * What the panel says at the end of a run whose page is gone: the source of the notification ({@code NotificationService.Source}),
     * whether the run started by itself, and what it was about (the title of the book).
     */
    public record Notice(String source, boolean automatic, String detail) {
    }

    /**
     * What the model writes for a run now: the end of its reasoning and of its answer in the request that goes on, and its place in
     * the queue of the AI. Fed by the gate of the connections for every request made on a thread the run is {@link Run#bind bound}
     * to; read by the panel.
     */
    public static final class Text {
        /** The end of each text that is kept (the panel shows only the end). */
        public static final int KEPT = 4000;

        private final StringBuilder reasoning = new StringBuilder();
        private final StringBuilder answer = new StringBuilder();
        private int waiting = -1;
        private int requests;

        /** The end of what the model thought in the request that goes on (or the last one), empty when nothing. */
        public synchronized String getReasoning() {
            return reasoning.toString();
        }

        /** The end of what the model answered in the request that goes on (or the last one), empty when nothing. */
        public synchronized String getAnswer() {
            return answer.toString();
        }

        /** How many requests are before the run's at the AI, -1 when it does not wait. */
        public synchronized int getWaiting() {
            return waiting;
        }

        /** How many requests the run sent so far. */
        public synchronized int getRequests() {
            return requests;
        }

        synchronized void started() {
            reasoning.setLength(0);
            answer.setLength(0);
            waiting = -1;
            requests++;
        }

        synchronized void waiting(int ahead) {
            waiting = ahead <= 0 ? -1 : ahead;
        }

        synchronized void append(boolean isReasoning, String piece) {
            StringBuilder box = isReasoning ? reasoning : answer;
            box.append(piece);
            if (box.length() > KEPT * 2) {
                box.delete(0, box.length() - KEPT);
            }
        }
    }

    /**
     * One run.
     */
    public static final class Run {
        private final long id;
        private final String source;
        private final Supplier<String> label;
        private final Long bookId;
        private final String bookName;
        private final Long ownerId;
        private final String ownerLogin;
        private final WeakReference<Object> page;
        private final boolean marksParts;
        private volatile Runnable stop;
        private final Notice notice;
        private final Instant started = Instant.now();
        private final List<Hold> holds = new CopyOnWriteArrayList<>();
        private final Text text = new Text();
        private final AtomicBoolean noticed = new AtomicBoolean();
        private volatile boolean ended;
        private volatile boolean stopping;
        private volatile boolean waitingForUser;
        private volatile Outcome outcome;
        private volatile Stage stage;
        // the run whose step started this one: the panel shows it in that run's entry, its text goes there too
        private final Run parent;
        private final List<Run> children = new CopyOnWriteArrayList<>();
        // a run that takes the runs started on its thread as its own
        private volatile boolean adopts;
        private volatile boolean blocksGeneration;
        private volatile Runnable onOpen;

        private Run(Builder builder) {
            this.id = SEQUENCE.incrementAndGet();
            this.source = builder.source;
            this.label = builder.label;
            this.bookId = builder.bookId;
            this.bookName = builder.bookName;
            Jobs.Owner user = Jobs.currentOwner();
            this.ownerId = builder.ownerId != null ? builder.ownerId : user == null ? null : user.userId();
            this.ownerLogin = user != null && Objects.equals(user.userId(), ownerId) ? user.login() : null;
            this.page = builder.page == null ? null : new WeakReference<>(builder.page);
            this.marksParts = builder.marksParts;
            this.stop = builder.stop;
            this.notice = builder.notice;
            this.holds.addAll(builder.holds);
            this.stage = builder.stage;
            Run current = current();
            this.parent = builder.parent != null ? builder.parent : current != null && current.adopts ? current : null;
            if (parent != null) {
                parent.children.add(this);
            }
        }

        /**
         * The run whose work started this one (a translation started by a step after the part), null for a run of its own. The panel
         * does not list it apart: it is a part of the parent's entry, which shows its text and whose Stop stops it.
         */
        public Run getParent() {
            return parent;
        }

        /**
         * The runs started on a thread bound to this run become its {@link #getParent() children} (the second phase of a
         * generation: what its steps start is one entry).
         */
        public void setAdopts(boolean adopts) {
            this.adopts = adopts;
        }

        /** Whether a new generation of its book is refused while it runs (the part is being written again). */
        public boolean blocksGeneration() {
            return blocksGeneration && !ended;
        }

        public void setBlocksGeneration(boolean blocks) {
            if (blocksGeneration != blocks) {
                blocksGeneration = blocks;
                changed();
            }
        }

        /**
         * What the panel's <b>Review</b> does for a run that waits for the user (opens the result in the page of the click), null when it
         * has nothing to open.
         */
        public Runnable getOnOpen() {
            return ended ? null : onOpen;
        }

        public void setOnOpen(Runnable open) {
            this.onOpen = open;
            changed();
        }

        /** What it does changed (its label is read again): the pages show it again. */
        public void relabel() {
            if (!ended) {
                changed();
            }
        }

        /** The login of the user it works for, null when it is not known (started without a session). */
        public String getOwnerLogin() {
            return ownerLogin;
        }

        /** The title of the book when the run started, null when it is not known. */
        public String getBookName() {
            return bookName;
        }

        /** What the model writes for it now. */
        public Text getText() {
            return text;
        }

        /** Whether it can be stopped (it was given a way to stop). */
        public boolean canStop() {
            return stop != null && !ended;
        }

        /** Whether it was asked to stop and has not ended yet. */
        public boolean isStopping() {
            return stopping && !ended;
        }

        /** What stops it, for a run whose work exists only after it started (a batch that makes its own token). */
        public void setOnStop(Runnable stop) {
            this.stop = stop;
            changed();
        }

        /** Whether it waits for the user (a proposal to review), not for the model. */
        public boolean isWaitingForUser() {
            return waitingForUser;
        }

        public void setWaitingForUser(boolean waiting) {
            if (waitingForUser != waiting) {
                waitingForUser = waiting;
                changed();
            }
        }

        /** How it ended, null while it runs or when it ended without saying. */
        public Outcome getOutcome() {
            return outcome;
        }

        /** What the panel says at its end when its page is gone, null for nothing. */
        public Notice getNotice() {
            return notice;
        }

        /** True once, for the one page that sends the notification of its end. */
        public boolean claimNotice() {
            return notice != null && noticed.compareAndSet(false, true);
        }

        /**
         * Makes this the run of the current thread until the result is closed: the requests to a model made on it report to its
         * {@link #getText() text} (also those made later from their callbacks).
         */
        public Binding bind() {
            Run before = CURRENT.get();
            CURRENT.set(this);
            return () -> {
                if (before == null) {
                    CURRENT.remove();
                } else {
                    CURRENT.set(before);
                }
            };
        }

        /** A request of the run goes to the model (it got its connection). */
        public void requestStarted() {
            text.started();
            textChanged(this);
            changed();
            if (parent != null) {
                parent.requestStarted();
            }
        }

        /** The request of the run waits for a connection: {@code ahead} before it, 0 when it is sent. */
        public void waiting(int ahead) {
            int before = text.getWaiting();
            text.waiting(ahead);
            if (before != text.getWaiting()) {
                changed();
            }
            if (parent != null) {
                parent.waiting(ahead);
            }
        }

        /** A piece of what the model writes for the run. */
        public void chunk(boolean reasoning, String piece) {
            if (piece == null || piece.isEmpty() || ended) {
                return;
            }
            text.append(reasoning, piece);
            textChanged(this);
            if (parent != null) {
                parent.chunk(reasoning, piece);
            }
        }

        public long getId() {
            return id;
        }

        /** What started it, e.g. {@value #GENERATION}, {@code reallang-translator}. */
        public String getSource() {
            return source;
        }

        /** What it does, as it is shown (read each time, it may be localized). */
        public String getLabel() {
            try {
                String text = label.get();
                return text == null ? source : text;
            } catch (RuntimeException e) {
                return source;
            }
        }

        /** The book it works on, null when none. */
        public Long getBookId() {
            return bookId;
        }

        /** The user the book belongs to, null when it is not known. */
        public Long getOwnerId() {
            return ownerId;
        }

        /** The page (the {@link UI}) that started it, null when it has none or the page is gone. */
        public Object getPage() {
            return page == null ? null : page.get();
        }

        /** Whether the cards of the parts it holds show it (a generation does not: the page shows it already). */
        public boolean marksParts() {
            return marksParts;
        }

        public Instant getStarted() {
            return started;
        }

        public List<Hold> getHolds() {
            return List.copyOf(holds);
        }

        public boolean isEnded() {
            return ended;
        }

        /** Where a generation is, null for a run that has no stages. */
        public Stage getStage() {
            return stage;
        }

        /** The run moved on (a generation starts its inference). */
        public void setStage(Stage stage) {
            if (ended || this.stage == stage) {
                return;
            }
            this.stage = stage;
            changed();
        }

        /** The id of the part the run writes, null when it writes none (or more than one). */
        public Long getWrittenPart() {
            Long found = null;
            for (Hold hold : holds) {
                if (hold.type() == ChatMessage.class && hold.mode() == Mode.WRITE) {
                    if (found != null) {
                        return null;
                    }
                    found = hold.id();
                }
            }
            return found;
        }

        /** The ids of the parts held in the mode. */
        public Set<Long> getParts(Mode mode) {
            Set<Long> ids = new HashSet<>();
            for (Hold hold : holds) {
                if (hold.type() == ChatMessage.class && hold.mode() == mode) {
                    ids.add(hold.id());
                }
            }
            return ids;
        }

        /**
         * Holds one more object (the part a generation created).
         */
        public void hold(BaseEntity entity, Mode mode) {
            Hold hold = holdOf(entity, mode);
            if (hold == null || ended) {
                return;
            }
            holds.removeIf(h -> h.type() == hold.type() && Objects.equals(h.id(), hold.id()));
            holds.add(hold);
            changed();
        }

        /**
         * Holds exactly these parts (and nothing else of theirs).
         */
        public void holdParts(Collection<? extends BaseEntity> parts, Mode mode) {
            if (ended) {
                return;
            }
            holds.removeIf(h -> h.type() == ChatMessage.class);
            for (BaseEntity part : parts) {
                Hold hold = holdOf(part, mode);
                if (hold != null) {
                    holds.add(hold);
                }
            }
            changed();
        }

        /**
         * Holds these parts too, in the mode, beside what it holds already (the branch above the part a generation writes); a part it
         * holds already keeps its hold.
         */
        public void holdMore(Collection<? extends BaseEntity> parts, Mode mode) {
            if (ended) {
                return;
            }
            Set<Long> held = new HashSet<>();
            for (Hold hold : holds) {
                if (hold.type() == ChatMessage.class) {
                    held.add(hold.id());
                }
            }
            List<Hold> added = new ArrayList<>();
            for (BaseEntity part : parts) {
                Hold hold = holdOf(part, mode);
                if (hold != null && held.add(hold.id())) {
                    added.add(hold);
                }
            }
            if (!added.isEmpty()) {
                holds.addAll(added);
                changed();
            }
        }

        /**
         * Asks the run to stop (the work stops, then it ends its run); a window closes. Nothing happens when it ended.
         */
        public void stop() {
            for (Run child : children) {
                child.stop();
            }
            Runnable action = stop;
            if (ended || action == null) {
                return;
            }
            boolean first = !stopping;
            stopping = true;
            try {
                action.run();
            } catch (RuntimeException e) {
                log.warn("The run {} could not be stopped: {}", getSource(), e.getMessage());
            }
            if (first) {
                changed();
            }
        }

        /**
         * The run is over: its holds are given back. Called in the {@code finally} of the work; again is harmless.
         */
        public void end() {
            end(null);
        }

        /**
         * As {@link #end()}, saying how it went (the panel's notification when the page is gone); a run that was stopped ended
         * {@link Outcome#STOPPED}.
         */
        public void end(Outcome how) {
            if (ended) {
                return;
            }
            outcome = stopping && how != Outcome.FAILED ? Outcome.STOPPED : how;
            ended = true;
            RUNS.remove(this);
            if (parent != null) {
                parent.children.remove(this);
            }
            changed();
            for (Consumer<Run> listener : END_LISTENERS) {
                try {
                    listener.accept(this);
                } catch (RuntimeException e) {
                    log.warn("A listener of the end of a run failed: {}", e.getMessage());
                    log.debug(e.getMessage(), e);
                }
            }
        }

        @Override
        public String toString() {
            return "Run " + id + " " + source + " book " + bookId;
        }
    }

    /**
     * What is asked to start a run.
     */
    public static final class Builder {
        private final String source;
        private final Supplier<String> label;
        private Long bookId;
        private String bookName;
        private Long ownerId;
        private Object page;
        private boolean marksParts = true;
        private Runnable stop;
        private Notice notice;
        private Stage stage;
        private Run parent;
        private final List<Hold> holds = new ArrayList<>();

        private Builder(String source, Supplier<String> label) {
            this.source = source;
            this.label = label;
        }

        /** The book it works on (needed: the holds of its parts are by book). */
        public Builder book(Manuscript book) {
            if (book != null) {
                this.bookId = idOf(book);
                // a proxy of a book read in another session is not touched
                boolean plain = !(book instanceof HibernateProxy);
                this.ownerId = !plain || book.getOwner() == null ? null : idOf(book.getOwner());
                this.bookName = plain ? book.getName() : null;
            }
            return this;
        }

        /** The user it works for, when it has no book or the book is a proxy (the session's user). */
        public Builder owner(Long userId) {
            if (userId != null) {
                this.ownerId = userId;
            }
            return this;
        }

        /**
         * What the panel of a live page of the user says when the run ends and the page that started it is gone (the page itself
         * says it as before).
         *
         * @param source the id of the source of the notification
         */
        public Builder notifies(String source, boolean automatic, String detail) {
            this.notice = source == null ? null : new Notice(source, automatic, detail);
            return this;
        }

        public Builder hold(BaseEntity entity, Mode mode) {
            Hold hold = holdOf(entity, mode, bookId);
            if (hold != null) {
                holds.add(hold);
            }
            return this;
        }

        /** The page that started it; the page itself is never refused by the {@link Mode#OPEN} hold of its own book window. */
        public Builder page(Object page) {
            this.page = page;
            return this;
        }

        /** The current page, when there is one. */
        public Builder currentPage() {
            return page(UI.getCurrent());
        }

        /** The cards of its parts do not show it. */
        public Builder quiet() {
            this.marksParts = false;
            return this;
        }

        /** The first stage of a run that has stages. */
        public Builder stage(Stage stage) {
            this.stage = stage;
            return this;
        }

        /**
         * The run it is a part of ({@link Run#getParent()}); without it, the run bound to the thread when that one
         * {@link Run#setAdopts adopts} what it starts.
         */
        public Builder partOf(Run parent) {
            this.parent = parent;
            return this;
        }

        /** What {@link Run#stop()} does. */
        public Builder onStop(Runnable stop) {
            this.stop = stop;
            return this;
        }

        /**
         * Starts the run without asking anything: the caller checked what it had to ({@link #check}).
         */
        public Run start() {
            Run run = new Run(this);
            RUNS.add(run);
            changed();
            return run;
        }

        /**
         * Starts the run unless a run refuses the operation on the object; the check and the start are one step, so two callers can not
         * both get through.
         *
         * @throws HeldException naming the run that is in the way
         */
        public Run startUnless(BaseEntity entity, Operation operation) {
            return startUnless(List.of(entity), operation);
        }

        /**
         * As {@link #startUnless(BaseEntity, Operation)} for several objects (the parts of a range).
         */
        public Run startUnless(Collection<? extends BaseEntity> entities, Operation operation) {
            Run run;
            synchronized (RUNS) {
                for (BaseEntity entity : entities) {
                    check(entity, operation);
                }
                run = new Run(this);
                RUNS.add(run);
            }
            changed();
            return run;
        }
    }

    /**
     * Ends {@link Run#bind} without a checked exception.
     */
    public interface Binding extends AutoCloseable {
        @Override
        void close();
    }

    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final List<Run> RUNS = new CopyOnWriteArrayList<>();
    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<Run>> TEXT_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<Run>> END_LISTENERS = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<Run> CURRENT = new ThreadLocal<>();

    private Runs() {
    }

    /** The run the current thread works for ({@link Run#bind}), null when none or it ended. */
    public static Run current() {
        Run run = CURRENT.get();
        return run == null || run.isEnded() ? null : run;
    }

    /** The runs of a user (the owner of their books, or the user the run said), oldest first. */
    public static List<Run> ofUser(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return RUNS.stream().filter(run -> userId.equals(run.getOwnerId())).toList();
    }

    /**
     * Called when the model writes for a run (often: a listener that shows it throttles itself), on the thread of the request.
     */
    public static Registration addTextListener(Consumer<Run> listener) {
        TEXT_LISTENERS.add(listener);
        return () -> TEXT_LISTENERS.remove(listener);
    }

    /**
     * Called once when a run ends, after the {@link #addListener listeners} were told, on the thread that ended it.
     */
    public static Registration addEndListener(Consumer<Run> listener) {
        END_LISTENERS.add(listener);
        return () -> END_LISTENERS.remove(listener);
    }

    private static void textChanged(Run run) {
        for (Consumer<Run> listener : TEXT_LISTENERS) {
            try {
                listener.accept(run);
            } catch (RuntimeException e) {
                log.debug("A listener of the text of the runs failed: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * A new run.
     *
     * @param source what starts it (the id used for notifications, e.g. {@code reallang-translator})
     * @param label  what it does, read each time it is shown
     */
    public static Builder run(String source, Supplier<String> label) {
        return new Builder(source, label);
    }

    /** Every run, oldest first. */
    public static List<Run> all() {
        return List.copyOf(RUNS);
    }

    /** The runs of a book. */
    public static List<Run> ofBook(Long bookId) {
        if (bookId == null) {
            return List.of();
        }
        return RUNS.stream().filter(run -> bookId.equals(run.getBookId())).toList();
    }

    /** The generation of a book that runs now, null when none. */
    public static Run generationOf(Long bookId) {
        for (Run run : ofBook(bookId)) {
            if (GENERATION.equals(run.getSource()) && !run.isEnded()) {
                return run;
            }
        }
        return null;
    }

    /**
     * Called after every change of a run (started, held more, moved on to a stage, ended), on the thread that changed it.
     */
    public static Registration addListener(Runnable listener) {
        LISTENERS.add(listener);
        return () -> LISTENERS.remove(listener);
    }

    /**
     * What a run that shows itself on the cards does to the part, null when no such run holds it.
     */
    public static String busyWith(Long partId) {
        if (partId == null) {
            return null;
        }
        for (Run run : RUNS) {
            if (run.marksParts() && !run.isEnded()) {
                for (Hold hold : run.holds) {
                    if (hold.type() == ChatMessage.class && partId.equals(hold.id()) && hold.mode() != Mode.READ) {
                        return run.getLabel();
                    }
                }
            }
        }
        return null;
    }

    /**
     * The run that refuses the operation on the object, null when nothing is in the way. The {@link Mode#OPEN} hold of the current
     * page (its own book window) is never in the way.
     */
    public static Run holder(BaseEntity entity, Operation operation) {
        if (entity == null || entity.getId() == null || RUNS.isEmpty()) {
            return null;
        }
        Class<?> type = type(entity);
        Object current = currentPage();
        for (Run run : RUNS) {
            if (run.isEnded()) {
                continue;
            }
            if (type == Manuscript.class) {
                if (refusesBook(run, entity.getId(), operation, current)) {
                    return run;
                }
            } else {
                for (Hold hold : run.holds) {
                    if (hold.type() == type && entity.getId().equals(hold.id()) && refuses(run, hold.mode(), operation)) {
                        return run;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Whether a hold of an object (not a book) in the mode refuses the operation on it.
     */
    private static boolean refuses(Run run, Mode mode, Operation operation) {
        return switch (operation) {
            case DELETE, MOVE -> mode == Mode.WRITE || mode == Mode.STRUCTURE;
            case WRITE -> mode == Mode.WRITE;
            // a translation writes its part, the author may still edit it (the translation sees the change): only a generation refuses
            case EDIT -> mode == Mode.WRITE && GENERATION.equals(run.getSource());
            case REPLACE, GENERATE, OPEN, SWITCH -> false;
        };
    }

    private static boolean refusesBook(Run run, Long bookId, Operation operation, Object current) {
        if (!bookId.equals(run.getBookId())) {
            return false;
        }
        boolean window = BOOK_WINDOW.equals(run.getSource());
        boolean ownWindow = window && current != null && current == run.getPage();
        return switch (operation) {
            // a part is being written again after it was generated: the next part would be written after the old text
            case GENERATE -> GENERATION.equals(run.getSource()) || run.blocksGeneration();
            case OPEN -> window && !ownWindow;
            case DELETE, REPLACE -> !ownWindow;
            // the generation itself moves the end of the branch to its new part while it prepares
            case SWITCH -> GENERATION.equals(run.getSource()) && run.getStage() == Stage.INFERENCE;
            case WRITE, EDIT, MOVE -> false;
        };
    }

    /**
     * Refuses the operation when a run holds the object.
     *
     * @throws HeldException naming the run
     */
    public static void check(BaseEntity entity, Operation operation) {
        Run run = holder(entity, operation);
        if (run != null) {
            throw new HeldException(run);
        }
    }

    /**
     * The check of the base {@code delete} (and of a save that marks the object deleted): refused when a run writes the object or needs
     * it to stay; a run that only reads it is stopped.
     */
    public static void deleting(BaseEntity entity) {
        if (entity == null || entity.getId() == null || RUNS.isEmpty()) {
            return;
        }
        check(entity, Operation.DELETE);
        Class<?> type = type(entity);
        for (Run run : RUNS) {
            for (Hold hold : run.holds) {
                if (hold.mode() == Mode.READ && hold.type() == type && entity.getId().equals(hold.id())) {
                    run.stop();
                    break;
                }
            }
        }
    }

    /** Ends every run (for a test that starts over). */
    public static void reset() {
        for (Run run : List.copyOf(RUNS)) {
            run.end();
        }
    }

    private static Object currentPage() {
        try {
            return UI.getCurrent();
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    private static void changed() {
        for (Runnable listener : LISTENERS) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                log.warn("A listener of the runs failed: {}", e.getMessage());
                log.debug(e.getMessage(), e);
            }
        }
    }

    private static Hold holdOf(BaseEntity entity, Mode mode) {
        return holdOf(entity, mode, null);
    }

    private static Hold holdOf(BaseEntity entity, Mode mode, Long fallbackBook) {
        if (entity == null || entity.getId() == null) {
            return null;
        }
        Class<?> type = type(entity);
        Long book = fallbackBook;
        if (entity instanceof Manuscript) {
            book = entity.getId();
        } else if (entity instanceof ChatMessage part && part.getParentScript() != null) {
            book = idOf(part.getParentScript());
        }
        return new Hold(type, entity.getId(), book, mode);
    }

    /** The id of an entity without initializing a proxy (it may be detached). */
    private static Long idOf(Object entity) {
        if (entity instanceof HibernateProxy proxy) {
            Object id = proxy.getHibernateLazyInitializer().getIdentifier();
            return id instanceof Long value ? value : null;
        }
        return entity instanceof BaseEntity base ? base.getId() : null;
    }

    /** The class of the entity, without initializing a proxy. */
    private static Class<?> type(BaseEntity entity) {
        return entity instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : entity.getClass();
    }
}
