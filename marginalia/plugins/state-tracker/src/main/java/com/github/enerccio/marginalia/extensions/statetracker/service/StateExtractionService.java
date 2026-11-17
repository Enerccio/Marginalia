package com.github.enerccio.marginalia.extensions.statetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.statetracker.block.StateBlockBuilder;
import com.github.enerccio.marginalia.extensions.statetracker.extract.*;
import com.github.enerccio.marginalia.extensions.statetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.storytracker.llm.Live;
import com.github.enerccio.marginalia.extensions.storytracker.llm.ModelCall;
import com.github.enerccio.marginalia.extensions.storytracker.llm.StoryLore;
import com.github.enerccio.marginalia.extensions.storytracker.llm.TrackerModels;
import com.github.enerccio.marginalia.extensions.storytracker.llm.TrackerModels.ModelSelection;
import com.github.enerccio.marginalia.extensions.storytracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Asks the model what changed in the state of the tracked characters in the next parts of the story, and applies what the user
 * (or the automatic mode) accepts. A run is {@link #propose} then {@link #commit}; nothing changes before the commit, so a run the
 * user cancels leaves the state and the cursor as they were and the same parts are proposed again next time.
 */
@Configurable
public class StateExtractionService {

    /** The source of the requests and of the runs (the log of the jobs, the panel of the runs). */
    public static final String SOURCE = "state-tracker";

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ManuscriptService manuscriptService;

    private final TrackerModels models = new TrackerModels();
    private final ModelCall modelCall = new ModelCall();
    private final StoryLore storyLore = new StoryLore();
    private final StateService stateService;
    private final StateProcessService processService;

    public StateExtractionService(StateService stateService, StateProcessService processService) {
        this.stateService = stateService;
        this.processService = processService;
    }

    /**
     * What the model proposed for the next parts.
     *
     * @param parts     the parts the run covers (the cursor moves over them when it is committed)
     * @param changes   the checked changes, empty when nothing was asked or nothing changed
     * @param clock     what the parts say about the time of the story (the book follows it), checked
     * @param left      what was dropped from the answer, one line each
     * @param asked     whether the model was asked at all (not when no tracked character is present in the parts)
     * @param note      what the user should know about the run
     * @param raw       what the model answered
     * @param pending   how many settled parts were not read by this run because it was limited
     * @param reasoning what the model thought before it answered, empty when the provider sends none
     * @param tidy      a proposal of the tidy (it has no parts and moves no cursor)
     */
    public record Proposal(List<Part> parts, List<StateChange> changes, List<ClockChange> clock, List<String> left, boolean asked,
                           String note, String raw, int pending, String reasoning, boolean tidy) implements TrackerRun.Proposal {

        static Proposal nothing(List<Part> parts, String note, int pending) {
            return new Proposal(List.copyOf(parts), List.of(), List.of(), List.of(), false, note, "", pending, "", false);
        }

        static Proposal nothingToTidy() {
            return new Proposal(List.of(), List.of(), List.of(), List.of(), false, "", "", 0, "", true);
        }

        @Override
        public int changeCount() {
            return changes.size() + clock.size();
        }

        @Override
        public int dropped() {
            return left.size();
        }
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
     * Reads the next settled parts and asks the model. Blocks until the answer is there; an interrupt of the calling thread cancels
     * the request. Nothing is stored.
     *
     * @param maxParts    at most this many parts are read (fewer when they do not fit the context)
     * @param lastSettled whether the last part of the branch counts as settled
     * @param excludeFrom a part that is being replaced, it and the later ones are left alone; may be {@code null}
     * @param live        told what the model writes while it writes it, may be {@code null}
     * @return a proposal without parts when nothing is waiting
     */
    public Proposal propose(Manuscript manuscript, int maxParts, boolean lastSettled, String excludeFrom, Live live) throws Exception {
        StateBook state = stateService.load(manuscript);
        BookSettings settings = state.getSettings();
        List<Part> branch = processService.branch(manuscript);
        Collection<String> separators = StateBlockBuilder.effectiveSeparators(state);
        String system = manuscriptService.resolveExtension(manuscript, StateInherited.EXTRACTION_PROMPT).value();
        List<Part> pending = BranchProcessor.pending(state, branch, BranchProcessor.settledCount(branch, lastSettled, excludeFrom),
                Integer.MAX_VALUE);
        if (pending.isEmpty()) {
            return Proposal.nothing(List.of(), "", 0);
        }
        List<Part> wanted = pending.subList(0, Math.min(Math.max(1, maxParts), pending.size()));

        // nobody tracked is in these parts: nothing to ask, the cursor can move on over all of them
        StatePrompt.Request request = StatePrompt.build(state, "", wanted, separators, system);
        if (request.isEmpty()) {
            return Proposal.nothing(wanted, "", pending.size() - wanted.size());
        }

        ModelSelection model = models.resolve(manuscript, settings.getAiUuid(), settings.getProtocolUuid());
        if (model == null) {
            throw new IllegalStateException("There is no model for the State Tracker: the book has none and neither have your defaults");
        }
        InferenceService service = inferenceServices.forAIOrThrow(model.ai());
        long budget = TrackerModels.promptBudget(model.ai());

        // as many parts as fit, at least one
        int count = wanted.size();
        while (count > 1 && tokens(service, request) > budget) {
            count--;
            request = StatePrompt.build(state, "", wanted.subList(0, count), separators, system);
        }
        if (tokens(service, request) > budget) {
            throw new IllegalStateException("A single part does not fit the context of the model together with the state of the characters");
        }
        List<Part> run = wanted.subList(0, count);
        int leftParts = pending.size() - count;
        StringBuilder note = new StringBuilder();
        if (count < wanted.size()) {
            note.append(wanted.size() - count).append(" part(s) did not fit the context and are left for the next run. ");
        }
        if (request.isEmpty()) {
            // the parts that were dropped to fit may have been the only ones with a tracked character in them
            return Proposal.nothing(run, note.toString().strip(), leftParts);
        }

        String lore = storyLore.lore(manuscript, settings.getLoreMode(), run, budget - tokens(service, request), service, note);
        request = StatePrompt.build(state, lore, run, separators, system);

        ModelCall.Answered answered = modelCall.ask(model, manuscript, SOURCE, request.system(), request.user(), live, null);
        StateAnswer answer;
        try {
            answer = StructuredOutputs.parse(answered.text(), StateAnswer.class);
        } catch (Exception e) {
            throw new IllegalStateException("The model did not answer in the expected form: " + e.getMessage(), e);
        }
        StateChangeBuilder.Result checked = StateChangeBuilder.build(state, request, answer);
        for (StateChange change : checked.changes()) {
            change.setPartNumber(indexOf(branch, change.getPart()) + 1);
        }
        ClockBuilder.Result time = ClockBuilder.build(request, answer == null ? null : answer.clock());
        for (ClockChange change : time.changes()) {
            change.setPartNumber(indexOf(branch, change.getMark().getPart()) + 1);
        }
        List<String> left = new java.util.ArrayList<>(time.dropped());
        left.addAll(checked.dropped());
        return new Proposal(List.copyOf(run), checked.changes(), time.changes(), left, true, note.toString().strip(), answered.text(),
                leftParts, answered.reasoning(), false);
    }

    /**
     * Moves the cursor over the parts of the run, then applies the changes (with the choices the user made in them). The order
     * matters: moving the cursor first brings the state in line with the branch, which would take back changes applied before it.
     *
     * @param logged whether what is applied goes into the change log (nobody reviewed it)
     * @throws StaleProposalException when the parts that are next in line are not the ones of the proposal any more
     */
    public StateApplier.Result commit(Manuscript manuscript, Proposal proposal, boolean logged) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            List<Part> branch = processService.branch(manuscript);
            List<Part> next = BranchProcessor.pending(state, branch, branch.size(), proposal.parts().size());
            if (!sameParts(next, proposal.parts())) {
                throw new StaleProposalException("The story changed while the changes were being reviewed");
            }
            if (!proposal.parts().isEmpty()) {
                int end = indexOf(branch, proposal.parts().getLast().uuid()) + 1;
                BranchProcessor.process(state, branch, end, StateBlockBuilder.effectiveSeparators(state), System.currentTimeMillis(),
                        proposal.parts().size());
                state.partsRead(proposal.parts().size());
                // the clock first: the values the run sets remember the story day of their part
                ClockBuilder.commit(state, proposal.parts(), proposal.clock());
            }
            StateApplier.Result result = StateApplier.apply(state, proposal.changes(), System.currentTimeMillis(), logged);
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
            return result;
        }
    }

    /**
     * The tidy: asks the model which current values of the tracked characters present in the last K processed parts faded, moved on
     * or are the same entry twice, with those parts. Blocks until the answer is there. Nothing is stored; without anything to tidy
     * the proposal is empty and the model is not asked.
     *
     * @param live told what the model writes while it writes it, may be {@code null}
     */
    public Proposal proposeTidy(Manuscript manuscript, Live live) throws Exception {
        StateBook state = stateService.load(manuscript);
        BookSettings settings = state.getSettings();
        List<Part> branch = processService.branch(manuscript);
        BranchProcessor.align(state, branch);
        int cursor = Math.min(BranchProcessor.cursorPosition(state, branch), branch.size());
        if (cursor <= 0) {
            return Proposal.nothingToTidy();
        }
        Collection<String> separators = StateBlockBuilder.effectiveSeparators(state);
        List<Part> recent = new ArrayList<>(branch.subList(Math.max(0, cursor - settings.getTidyEveryParts()), cursor));
        String part = branch.get(cursor - 1).uuid();
        String system = manuscriptService.resolveExtension(manuscript, StateInherited.TIDY_PROMPT).value();

        StatePrompt.Request request = TidyPrompt.build(state, recent, separators, system);
        if (request.isEmpty()) {
            return Proposal.nothingToTidy();
        }
        ModelSelection model = models.resolve(manuscript, settings.getAiUuid(), settings.getProtocolUuid());
        if (model == null) {
            throw new IllegalStateException("There is no model for the State Tracker: the book has none and neither have your defaults");
        }
        InferenceService service = inferenceServices.forAIOrThrow(model.ai());
        long budget = TrackerModels.promptBudget(model.ai());
        StringBuilder note = new StringBuilder();
        // the oldest parts go first when the request does not fit; who is present is judged from the parts that are left
        while (recent.size() > 1 && tokens(service, request) > budget) {
            recent.removeFirst();
            request = TidyPrompt.build(state, recent, separators, system);
            note.setLength(0);
            note.append("Only the last ").append(recent.size()).append(" part(s) fit.");
        }
        if (request.isEmpty()) {
            return Proposal.nothingToTidy();
        }
        if (tokens(service, request) > budget) {
            throw new IllegalStateException("The last part does not fit the context of the model together with the state of the characters");
        }

        ModelCall.Answered answered = modelCall.ask(model, manuscript, SOURCE, request.system(), request.user(), live, null);
        TidyAnswer answer;
        try {
            answer = StructuredOutputs.parse(answered.text(), TidyAnswer.class);
        } catch (Exception e) {
            throw new IllegalStateException("The model did not answer in the expected form: " + e.getMessage(), e);
        }
        StateChangeBuilder.Result checked = TidyChangeBuilder.build(state, request, answer, part);
        return new Proposal(List.of(), checked.changes(), List.of(), checked.dropped(), true, note.toString(), answered.text(), 0,
                answered.reasoning(), true);
    }

    /**
     * Applies the reviewed changes of the tidy; the cursor does not move and the parts are counted again from here for the
     * automatic tidy. A change whose value is not current any more, or was locked meanwhile, is skipped.
     */
    public StateApplier.Result commitTidy(Manuscript manuscript, Proposal proposal, boolean logged) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            state.tidied();
            StateApplier.Result result = StateApplier.apply(state, proposal.changes(), System.currentTimeMillis(), logged);
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
            return result;
        }
    }

    /** A tidy found nothing to change: the parts are counted from here for the automatic tidy. */
    public void markTidied(Manuscript manuscript) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            state.tidied();
            stateService.save(manuscript, state);
        }
    }

    /** Whether the automatic tidy is due: a run that applied without asking, the setting on, K parts read since the last tidy. */
    public static boolean tidyDue(StateBook state, boolean guided) {
        BookSettings settings = state.getSettings();
        return !guided && settings.isAutoTidy() && state.getPartsSinceTidy() >= settings.getTidyEveryParts();
    }

    private static boolean sameParts(List<Part> a, List<Part> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).uuid().equals(b.get(i).uuid())) {
                return false;
            }
        }
        return true;
    }

    private static int indexOf(List<Part> branch, String uuid) {
        for (int i = 0; i < branch.size(); i++) {
            if (branch.get(i).uuid().equals(uuid)) {
                return i;
            }
        }
        return -1;
    }

    private static long tokens(InferenceService service, StatePrompt.Request request) throws Exception {
        return TrackerModels.tokens(service, request.system(), request.user());
    }
}
