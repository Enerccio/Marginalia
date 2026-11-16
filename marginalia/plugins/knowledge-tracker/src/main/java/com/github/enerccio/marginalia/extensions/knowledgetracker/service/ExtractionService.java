package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.KnowledgeBlockBuilder;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.storytracker.llm.Live;
import com.github.enerccio.marginalia.extensions.storytracker.llm.ModelCall;
import com.github.enerccio.marginalia.extensions.storytracker.llm.StoryLore;
import com.github.enerccio.marginalia.extensions.storytracker.llm.TrackerModels;
import com.github.enerccio.marginalia.extensions.storytracker.llm.TrackerModels.ModelSelection;
import com.github.enerccio.marginalia.extensions.storytracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.process.PartReader;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * Asks the model what the tracked characters learned in the next parts of the story, and commits what the user (or the
 * automatic mode) accepts. A run is {@link #propose} then {@link #commit}; nothing changes before the commit, so a run the
 * user cancels leaves the state and the cursor as they were and the same parts are proposed again next time.
 */
@Configurable
public class ExtractionService {

    /** The longest text a read of an earlier part gives back. */
    private static final int MAX_READ_CHARS = 12000;
    /** Below this, a read is not worth it and the model gets no function to read. */
    private static final int MIN_READ_CHARS = 1500;
    /** Characters per token, roughly, to turn the room that is left into the length of a read. */
    private static final int CHARS_PER_TOKEN = 3;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ManuscriptService manuscriptService;

    private final TrackerModels models = new TrackerModels();
    private final ModelCall modelCall = new ModelCall();
    private final StoryLore storyLore = new StoryLore();
    private final KnowledgeStateService stateService;
    private final KnowledgeProcessService processService;

    public ExtractionService(KnowledgeStateService stateService, KnowledgeProcessService processService) {
        this.stateService = stateService;
        this.processService = processService;
    }

    /** The models of the user, for the settings. */
    public List<AI> availableModels() throws Exception {
        return models.availableModels();
    }

    /** The protocols of the user, for the settings. */
    public List<Protocol> availableProtocols() throws Exception {
        return models.availableProtocols();
    }

    /**
     * What the model proposed for the next parts.
     *
     * @param parts   the parts the run covers (the cursor moves over them when it is committed), may be fewer than asked for
     *                when more did not fit the context of the model
     * @param changes the checked changes, empty when nothing was asked or nothing changed
     * @param asked   whether the model was asked at all (not when no tracked character is present in the parts)
     * @param note    what the user should know about the run: the lorebook did not fit, parts were left for the next run
     * @param raw     what the model answered, for the log
     * @param pending how many settled parts were not read by this run because it was limited
     * @param reasoning what the model thought before it answered, when the provider sends it; empty when it does not
     * @param tidy    a proposal of the tidy pass (it has no parts and moves no cursor)
     */
    public record Proposal(List<Part> parts, ChangeSet changes, boolean asked, String note, String raw, int pending, String reasoning,
                           boolean tidy) implements TrackerRun.Proposal {

        public Proposal(List<Part> parts, ChangeSet changes, boolean asked, String note, String raw, int pending) {
            this(parts, changes, asked, note, raw, pending, "", false);
        }

        public Proposal(List<Part> parts, ChangeSet changes, boolean asked, String note, String raw, int pending, String reasoning) {
            this(parts, changes, asked, note, raw, pending, reasoning, false);
        }

        @Override
        public boolean isEmpty() {
            return parts.isEmpty();
        }

        @Override
        public int changeCount() {
            return changes.changes().size();
        }

        @Override
        public int dropped() {
            return changes.dropped().size();
        }
    }

    /**
     * The model and the protocol of the extraction: those of the book's settings of this plugin, else the book's, else the
     * defaults of the user.
     *
     * @return null when there is none
     */
    public ModelSelection resolveModel(Manuscript manuscript, BookSettings settings) throws Exception {
        return models.resolve(manuscript, settings.getAiUuid(), settings.getProtocolUuid());
    }

    /**
     * Reads the next settled parts and asks the model. Blocks until the answer is there; an interrupt of the calling thread
     * cancels the request. Nothing is stored.
     *
     * @param maxParts    at most this many parts are read (fewer when they do not fit the context)
     * @param lastSettled whether the last part of the branch counts as settled
     * @param excludeFrom a part that is being replaced, it and the later ones are left alone; may be {@code null}
     * @return a proposal without parts when nothing is waiting
     */
    public Proposal propose(Manuscript manuscript, int maxParts, boolean lastSettled, String excludeFrom) throws Exception {
        return propose(manuscript, maxParts, lastSettled, excludeFrom, null);
    }

    /** As above; {@code live} (may be {@code null}) is told what the model writes while it writes it. */
    public Proposal propose(Manuscript manuscript, int maxParts, boolean lastSettled, String excludeFrom, Live live) throws Exception {
        KnowledgeState state = stateService.load(manuscript);
        BookSettings settings = state.getSettings();
        List<Part> branch = processService.branch(manuscript);
        List<String> separators = KnowledgeBlockBuilder.effectiveSeparators(state, stateService.getUserSeparators());
        String systemPrompt = manuscriptService.resolveExtension(manuscript, KnowledgeInherited.EXTRACTION_PROMPT).value();
        List<Part> pending = BranchProcessor.pending(state, branch, BranchProcessor.settledCount(branch, lastSettled, excludeFrom),
                Integer.MAX_VALUE);
        if (pending.isEmpty()) {
            return new Proposal(List.of(), new ChangeSet(List.of(), List.of()), false, "", "", 0);
        }
        List<Part> wanted = pending.subList(0, Math.min(maxParts, pending.size()));

        // nobody tracked is in these parts: nothing to ask, no model needed, the cursor can move on over all of them
        // the model may read what came before the run, to see where an item was learned (the first part of the run does not change below)
        ExtractionPrompt.Readable readable = settings.isReadEarlierParts() ? ExtractionPrompt.Readable.of(branch, wanted) : null;
        ExtractionPrompt.Request request = ExtractionPrompt.build(state, "", wanted, separators, readable, systemPrompt);
        if (request.isEmpty()) {
            return new Proposal(List.copyOf(wanted), new ChangeSet(List.of(), List.of()), false, "", "", pending.size() - wanted.size());
        }

        ModelSelection model = resolveModel(manuscript, settings);
        if (model == null) {
            throw new IllegalStateException("There is no model for the extraction: the book has none and neither have your defaults");
        }
        InferenceService service = inferenceServices.forAIOrThrow(model.ai());
        long budget = TrackerModels.promptBudget(model.ai());

        // as many parts as fit, at least one
        int count = wanted.size();
        while (count > 1 && tokens(service, request) > budget) {
            count--;
            request = ExtractionPrompt.build(state, "", wanted.subList(0, count), separators, readable, systemPrompt);
        }
        if (tokens(service, request) > budget) {
            throw new IllegalStateException("A single part does not fit the context of the model together with what the characters know");
        }
        List<Part> run = wanted.subList(0, count);
        int left = pending.size() - count;
        StringBuilder note = new StringBuilder();
        if (count < wanted.size()) {
            note.append(wanted.size() - count).append(" part(s) did not fit the context and are left for the next run. ");
        }

        if (request.isEmpty()) {
            // the parts that were dropped to fit may have been the only ones with a tracked character in them
            return new Proposal(List.copyOf(run), new ChangeSet(List.of(), List.of()), false, note.toString().strip(), "", left);
        }

        String lore = storyLore.lore(manuscript, settings.getLoreMode(), run, budget - tokens(service, request), service, note);
        request = ExtractionPrompt.build(state, lore, run, separators, readable, systemPrompt);

        // what is left of the context is what the reads may take: a part is cut to its share, and with no room for a useful read there is none
        PartReader reader = null;
        if (readable != null) {
            long spare = budget - tokens(service, request);
            int maxChars = (int) Math.min(MAX_READ_CHARS, spare * CHARS_PER_TOKEN / PartReader.MAX_READS);
            if (maxChars >= MIN_READ_CHARS) {
                reader = new PartReader(branch, readable.readable(), maxChars, null);
            }
        }
        ModelCall.Answered answered = ask(model, manuscript, request, live, reader);
        String raw = answered.text();
        ExtractionAnswer answer;
        try {
            answer = com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs.parse(raw, ExtractionAnswer.class);
        } catch (Exception e) {
            throw new IllegalStateException("The model did not answer in the expected form: " + e.getMessage(), e);
        }
        ChangeSet changes = ChangeSetBuilder.build(state, request, answer);
        return new Proposal(List.copyOf(run), changes, true, note.toString().strip(), raw, left, answered.reasoning());
    }

    /**
     * Applies the changes (with the choices the user made in them) and moves the cursor over the parts of the run.
     *
     * @throws StaleProposalException when the parts that are next in line are not the ones of the proposal any more
     */
    public ChangeApplier.Result commit(Manuscript manuscript, Proposal proposal) throws Exception {
        return commit(manuscript, proposal, false);
    }

    /**
     * @param logged whether what is applied goes into the change log of the book (an automatic run, nobody reviewed it)
     */
    public ChangeApplier.Result commit(Manuscript manuscript, Proposal proposal, boolean logged) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            List<Part> branch = processService.branch(manuscript);
            ChangeApplier.Result result = RunCommit.commit(state, branch, proposal.parts(), proposal.changes(),
                    KnowledgeBlockBuilder.effectiveSeparators(state, stateService.getUserSeparators()), System.currentTimeMillis(), logged);
            if (result == null) {
                throw new StaleProposalException("The story changed while the changes were being reviewed");
            }
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
            return result;
        }
    }

    /**
     * The tidy pass: asks the model which of the items it learned (and the user did not lock) to drop or merge, with the last processed
     * parts for context. Blocks until the answer is there. Nothing is stored; without anything to tidy the proposal is empty and the
     * model is not asked.
     */
    public Proposal proposeTidy(Manuscript manuscript) throws Exception {
        return proposeTidy(manuscript, null);
    }

    /** As above; {@code live} (may be {@code null}) is told what the model writes while it writes it. */
    public Proposal proposeTidy(Manuscript manuscript, Live live) throws Exception {
        KnowledgeState state = stateService.load(manuscript);
        BookSettings settings = state.getSettings();
        List<Part> branch = processService.branch(manuscript);
        BranchProcessor.align(state, branch);
        int cursor = BranchProcessor.cursorPosition(state, branch);
        String systemPrompt = manuscriptService.resolveExtension(manuscript, KnowledgeInherited.TIDY_PROMPT).value();
        List<Part> recent = new ArrayList<>(branch.subList(Math.max(0, cursor - settings.getTidyContextParts()), Math.min(cursor, branch.size())));

        ExtractionPrompt.Request request = TidyPrompt.build(state, recent, settings.getTidyMinItems(), systemPrompt);
        if (request.isEmpty()) {
            return new Proposal(List.of(), new ChangeSet(List.of(), List.of()), false, "", "", 0, "", true);
        }
        ModelSelection model = resolveModel(manuscript, settings);
        if (model == null) {
            throw new IllegalStateException("There is no model for the tidy pass: the book has none and neither have your defaults");
        }
        InferenceService service = inferenceServices.forAIOrThrow(model.ai());
        long budget = TrackerModels.promptBudget(model.ai());
        StringBuilder note = new StringBuilder();
        // the context goes first when the request does not fit; the items are what is asked about
        while (!recent.isEmpty() && tokens(service, request) > budget) {
            recent.removeFirst();
            request = TidyPrompt.build(state, recent, settings.getTidyMinItems(), systemPrompt);
            note.setLength(0);
            note.append("Only the last ").append(recent.size()).append(" part(s) fit as context.");
        }

        ModelCall.Answered answered = ask(model, manuscript, request, live, null);
        TidyAnswer answer;
        try {
            answer = com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs.parse(answered.text(), TidyAnswer.class);
        } catch (Exception e) {
            throw new IllegalStateException("The model did not answer in the expected form: " + e.getMessage(), e);
        }
        ChangeSet changes = TidyChangeSetBuilder.build(state, request.numbering(), answer);
        return new Proposal(List.of(), changes, true, note.toString(), answered.text(), 0, answered.reasoning(), true);
    }

    /** A tidy found nothing to change: the parts are counted from here for the automatic tidy. */
    public void markTidied(Manuscript manuscript) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            state.tidied();
            stateService.save(manuscript, state);
        }
    }

    /**
     * Applies the reviewed changes of the tidy pass. The cursor does not move. A change whose item is not there any more (or was locked
     * or edited since) is skipped.
     */
    public ChangeApplier.Result commitTidy(Manuscript manuscript, Proposal proposal, boolean logged) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            state.tidied();
            ChangeApplier.Result result = ChangeApplier.apply(state, proposal.changes(), logged, System.currentTimeMillis());
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
            return result;
        }
    }

    // --- the request ---

    private long tokens(InferenceService service, ExtractionPrompt.Request request) throws Exception {
        return TrackerModels.tokens(service, request.system(), request.user());
    }

    /** Asks the model (streamed, the reasoning kept); the model may read earlier parts through {@code reader} when it is given. */
    private ModelCall.Answered ask(ModelSelection model, Manuscript book, ExtractionPrompt.Request request, Live live, PartReader reader)
            throws Exception {
        return modelCall.ask(model, book, KnowledgeSteps.SOURCE, request.system(), request.user(), live,
                reader == null ? null : new ModelCall.Reading<>(reader, StoryFunctions.class, StoryFunctionsImpl::new));
    }
}
