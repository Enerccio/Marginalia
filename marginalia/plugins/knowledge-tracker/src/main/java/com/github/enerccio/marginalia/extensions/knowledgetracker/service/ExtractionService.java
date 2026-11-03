package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.KnowledgeBlockBuilder;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.LoreMode;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Asks the model what the tracked characters learned in the next parts of the story, and commits what the user (or the
 * automatic mode) accepts. A run is {@link #propose} then {@link #commit}; nothing changes before the commit, so a run the
 * user cancels leaves the state and the cursor as they were and the same parts are proposed again next time.
 */
@Configurable
public class ExtractionService {

    /** Tokens kept free below the context of the model for the parts of the request this class cannot count. */
    private static final long MARGIN_TOKENS = 300;
    /** The answer of a model that thinks first is long; this is assumed when the model has no limit of its own. */
    private static final long DEFAULT_ANSWER_TOKENS = 4096;

    @Autowired
    private InferenceServices inferenceServices;
    @Autowired
    private AIService aiService;
    @Autowired
    private ProtocolService protocolService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private LorebookEntryService lorebookEntryService;
    @Autowired
    private LorebookActivationService activationService;
    @Autowired
    private TagRelationService tagRelationService;
    @Autowired
    private SummaryService summaryService;

    private final KnowledgeStateService stateService;
    private final KnowledgeProcessService processService;

    public ExtractionService(KnowledgeStateService stateService, KnowledgeProcessService processService) {
        this.stateService = stateService;
        this.processService = processService;
    }

    /** The models of the user, for the settings. */
    public List<AI> availableModels() throws Exception {
        return aiService.findAllForUser().stream().filter(ai -> !ai.isDeleted()).toList();
    }

    /** The protocols of the user, for the settings. */
    public List<Protocol> availableProtocols() throws Exception {
        return protocolService.findAllForUser().stream().filter(p -> !p.isDeleted()).toList();
    }

    /** The model and the protocol a run uses. */
    public record ModelSelection(AI ai, Protocol protocol) {
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
                           boolean tidy) {

        public Proposal(List<Part> parts, ChangeSet changes, boolean asked, String note, String raw, int pending) {
            this(parts, changes, asked, note, raw, pending, "", false);
        }

        public Proposal(List<Part> parts, ChangeSet changes, boolean asked, String note, String raw, int pending, String reasoning) {
            this(parts, changes, asked, note, raw, pending, reasoning, false);
        }

        public boolean isEmpty() {
            return parts.isEmpty();
        }
    }

    /**
     * The model and the protocol of the extraction: those of the book's settings of this plugin, else the book's, else the
     * defaults of the user.
     *
     * @return null when there is none
     */
    public ModelSelection resolveModel(Manuscript manuscript, BookSettings settings) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        Manuscript book = current != null ? current : manuscript;
        AI ai = settings.getAiUuid() != null ? aiService.findForUser(settings.getAiUuid()) : null;
        Protocol protocol = settings.getProtocolUuid() != null ? protocolService.findForUser(settings.getProtocolUuid()) : null;
        if (ai == null || ai.isDeleted()) {
            ai = book.getAi();
            protocol = protocol != null && !protocol.isDeleted() ? protocol : book.getProtocol();
        }
        if (ai == null || ai.isDeleted()) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            ai = user.getDefaultModel() != null ? aiService.find(user.getDefaultModel()) : null;
            protocol = user.getDefaultProtocol() != null ? protocolService.find(user.getDefaultProtocol()) : protocol;
        }
        if (ai == null || ai.isDeleted()) {
            return null;
        }
        return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
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
        KnowledgeState state = stateService.load(manuscript);
        BookSettings settings = state.getSettings();
        List<Part> branch = processService.branch(manuscript);
        List<String> separators = KnowledgeBlockBuilder.effectiveSeparators(state, stateService.getUserSeparators());
        List<Part> pending = BranchProcessor.pending(state, branch, BranchProcessor.settledCount(branch, lastSettled, excludeFrom),
                Integer.MAX_VALUE);
        if (pending.isEmpty()) {
            return new Proposal(List.of(), new ChangeSet(List.of(), List.of()), false, "", "", 0);
        }
        List<Part> wanted = pending.subList(0, Math.min(maxParts, pending.size()));

        // nobody tracked is in these parts: nothing to ask, no model needed, the cursor can move on over all of them
        ExtractionPrompt.Request request = ExtractionPrompt.build(state, "", wanted, separators);
        if (request.isEmpty()) {
            return new Proposal(List.copyOf(wanted), new ChangeSet(List.of(), List.of()), false, "", "", pending.size() - wanted.size());
        }

        ModelSelection model = resolveModel(manuscript, settings);
        if (model == null) {
            throw new IllegalStateException("There is no model for the extraction: the book has none and neither have your defaults");
        }
        InferenceService service = inferenceServices.forAIOrThrow(model.ai());
        long budget = promptBudget(model.ai());

        // as many parts as fit, at least one
        int count = wanted.size();
        while (count > 1 && tokens(service, request) > budget) {
            count--;
            request = ExtractionPrompt.build(state, "", wanted.subList(0, count), separators);
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

        String lore = lore(manuscript, settings.getLoreMode(), run, budget - tokens(service, request), service, note);
        request = ExtractionPrompt.build(state, lore, run, separators);

        Answered answered = ask(model, manuscript, request);
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
        KnowledgeState state = stateService.load(manuscript);
        BookSettings settings = state.getSettings();
        List<Part> branch = processService.branch(manuscript);
        BranchProcessor.align(state, branch);
        int cursor = BranchProcessor.cursorPosition(state, branch);
        List<Part> recent = new ArrayList<>(branch.subList(Math.max(0, cursor - settings.getTidyContextParts()), Math.min(cursor, branch.size())));

        ExtractionPrompt.Request request = TidyPrompt.build(state, recent, settings.getTidyMinItems());
        if (request.isEmpty()) {
            return new Proposal(List.of(), new ChangeSet(List.of(), List.of()), false, "", "", 0, "", true);
        }
        ModelSelection model = resolveModel(manuscript, settings);
        if (model == null) {
            throw new IllegalStateException("There is no model for the tidy pass: the book has none and neither have your defaults");
        }
        InferenceService service = inferenceServices.forAIOrThrow(model.ai());
        long budget = promptBudget(model.ai());
        StringBuilder note = new StringBuilder();
        // the context goes first when the request does not fit; the items are what is asked about
        while (!recent.isEmpty() && tokens(service, request) > budget) {
            recent.removeFirst();
            request = TidyPrompt.build(state, recent, settings.getTidyMinItems());
            note.setLength(0);
            note.append("Only the last ").append(recent.size()).append(" part(s) fit as context.");
        }

        Answered answered = ask(model, manuscript, request);
        TidyAnswer answer;
        try {
            answer = com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs.parse(answered.text(), TidyAnswer.class);
        } catch (Exception e) {
            throw new IllegalStateException("The model did not answer in the expected form: " + e.getMessage(), e);
        }
        ChangeSet changes = TidyChangeSetBuilder.build(state, request.numbering(), answer);
        return new Proposal(List.of(), changes, true, note.toString(), answered.text(), 0, answered.reasoning(), true);
    }

    /**
     * Applies the reviewed changes of the tidy pass. The cursor does not move. A change whose item is not there any more (or was locked
     * or edited since) is skipped.
     */
    public ChangeApplier.Result commitTidy(Manuscript manuscript, Proposal proposal, boolean logged) throws Exception {
        synchronized (processService.lockOf(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            ChangeApplier.Result result = ChangeApplier.apply(state, proposal.changes(), logged, System.currentTimeMillis());
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
            return result;
        }
    }

    // --- the request ---

    private long tokens(InferenceService service, ExtractionPrompt.Request request) throws Exception {
        return service.countTokensApprox(request.system() + "\n" + request.user());
    }

    /** What the request may take: the context minus the room for the answer (which includes the reasoning) and a margin. */
    private long promptBudget(AI ai) {
        long context = ai.getMaxContext() != null && ai.getMaxContext() > 0 ? ai.getMaxContext() : 8192;
        long answer = ai.getMaxCompletionTokens() != null && ai.getMaxCompletionTokens() > 0 ? ai.getMaxCompletionTokens() : DEFAULT_ANSWER_TOKENS;
        return Math.max(1000, context - answer - MARGIN_TOKENS);
    }

    /** What the model sent: the answer and, when the provider reports it, what it thought first. */
    private record Answered(String text, String reasoning) {
    }

    /**
     * Asks the model and waits for the whole answer. The request is streamed so that the reasoning of the model, which a plain
     * {@code infer} drops, can be kept for the review.
     */
    private Answered ask(ModelSelection model, Manuscript book, ExtractionPrompt.Request request) throws Exception {
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, request.system()),
                LLMChatMessage.of(LLMRole.USER, request.user()));
        CompletableFuture<Answered> answer = new CompletableFuture<>();
        StringBuilder text = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open("knowledge-tracker"); UsageContext _ = UsageContext.open(book)) {
            token = inferenceServices.forAIOrThrow(model.ai()).stream(payload, model.protocol(),
                    new InferenceService.InferenceAsyncCallback() {
                        @Override
                        public void onChunk(InferenceService.InferenceAsyncController controller,
                                            InferenceService.ChunkType chunkType, String chunk) throws Exception {
                            (chunkType == InferenceService.ChunkType.REASONING ? reasoning : text).append(chunk);
                            controller.continueInference();
                        }

                        @Override
                        public void onCompletion() {
                            answer.complete(new Answered(text.toString(), reasoning.toString().strip()));
                        }

                        @Override
                        public void onCancel() {
                            answer.cancel(false);
                        }

                        @Override
                        public void onError(Throwable exception) {
                            answer.completeExceptionally(exception);
                        }

                        @Override
                        public boolean isDead() {
                            return answer.isDone();
                        }
                    });
        }
        try {
            return answer.get();
        } catch (InterruptedException e) {
            token.cancel();
            Thread.currentThread().interrupt();
            throw new CancellationException("The request was stopped");
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }

    // --- the lorebook ---

    /**
     * The lore for the request. {@link LoreMode#WHOLE} that does not fit becomes {@link LoreMode#RESOLVE}, and entries that
     * do not fit the budget are left out (the note says so).
     */
    private String lore(Manuscript manuscript, LoreMode mode, List<Part> run, long budget, InferenceService service,
                        StringBuilder note) throws Exception {
        if (mode == LoreMode.NONE || budget <= 0) {
            return "";
        }
        Manuscript current = manuscriptService.find(manuscript);
        Manuscript book = current != null ? current : manuscript;
        Lorebook root = book.getLorebook() != null ? lorebookService.find(book.getLorebook()) : null;
        if (root == null) {
            return "";
        }
        List<ChatMessage> branch = new ArrayList<>(chatMessageService.getBranchFromLeaf(book.getActiveLeaf()));
        int last = -1;
        for (int i = 0; i < branch.size(); i++) {
            if (branch.get(i).getUuid().equals(run.getLast().uuid())) {
                last = i;
            }
        }
        List<ChatMessage> upToRun = last >= 0 ? branch.subList(0, last + 1) : branch;
        LorebookTemplateData data = templateData(book, upToRun);

        if (mode == LoreMode.WHOLE) {
            String whole = wholeLore(root, data);
            if (service.countTokensApprox(whole) <= budget) {
                return whole;
            }
            note.append("The whole lorebook does not fit the context, only the entries of these parts are sent. ");
        }
        return resolvedLore(book, root, upToRun, data, budget, service, note);
    }

    private String wholeLore(Lorebook root, LorebookTemplateData data) throws Exception {
        List<LorebookEntry> entries = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Queue<Lorebook> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook lorebook = queue.remove();
            if (lorebook.isDeleted() || !seen.add(lorebook.getId())) {
                continue;
            }
            lorebookEntryService.getEntriesForLorebook(lorebook).stream().filter(e -> !e.isDeleted()).forEach(entries::add);
            queue.addAll(lorebookService.getSubbooks(lorebook));
        }
        entries.sort(Comparator.comparingInt(LorebookEntry::getOrder));
        StringBuilder lore = new StringBuilder();
        for (LorebookEntry entry : entries) {
            String payload = activationService.renderPayload(entry, data);
            if (StringUtils.isNotBlank(payload)) {
                lore.append('[').append(entry.getName()).append("]:\n").append(payload.strip()).append("\n\n");
            }
        }
        return lore.toString().strip();
    }

    private String resolvedLore(Manuscript book, Lorebook root, List<ChatMessage> upToRun, LorebookTemplateData data, long budget,
                                InferenceService service, StringBuilder note) throws Exception {
        List<Lorebook> books = activationService.collectLorebooks(root);
        List<String> tags = tagRelationService.getTagsForObject(book).stream().map(Tag::getValue).toList();
        StringBuilder matched = new StringBuilder();
        // the parts of the run are the ones after the cursor: the last ones of upToRun; matching on all of the run is enough
        for (ChatMessage message : upToRun.subList(Math.max(0, upToRun.size() - 10), upToRun.size())) {
            for (String piece : new String[]{message.getResponse(), message.getInstructions(), message.getSceneSetting(),
                    message.getPovCharacter(), message.getPresentCharacters()}) {
                if (StringUtils.isNotBlank(piece)) {
                    matched.append(piece.strip()).append('\n');
                }
            }
        }
        StringBuilder lore = new StringBuilder();
        long used = 0;
        int skipped = 0;
        for (LorebookEntryVerdict verdict : activationService.evaluateAll(root, books, tags, matched.toString())) {
            if (!verdict.verdict().activated()) {
                continue;
            }
            String payload = activationService.renderPayload(verdict.entry(), data);
            if (StringUtils.isBlank(payload)) {
                continue;
            }
            String entry = "[" + verdict.entry().getName() + "]:\n" + payload.strip();
            long tokens = service.countTokensApprox(entry);
            if (skipped > 0 || used + tokens > budget) {
                skipped++;
                continue;
            }
            lore.append(entry).append("\n\n");
            used += tokens;
        }
        if (skipped > 0) {
            note.append(skipped).append(" lorebook entries did not fit and were left out. ");
        }
        return lore.toString().strip();
    }

    /** Template data for the entries, set up like a quiet generation after the last part of the run. */
    private LorebookTemplateData templateData(Manuscript manuscript, List<ChatMessage> branch) throws Exception {
        TemplateContext context = new TemplateContext();
        ChatMessage last = branch.isEmpty() ? null : branch.getLast();
        if (last != null) {
            context.setPovCharacter(last.getPovCharacter());
            context.setPresentCharacters(last.getPresentCharacters());
            context.setSceneSetting(last.getSceneSetting());
            context.setInstructions(last.getInstructions());
            context.setLastInstructions(last.getInstructions());
            context.setLastMessageTime(last.getRequest());
        }
        String pov = manuscriptService.getPov(manuscript);
        String tense = manuscriptService.getTense(manuscript);
        String style = manuscriptService.getStyle(manuscript);
        context.setNarrativePov(pov);
        context.setNarrativeTense(tense);
        context.setStyle(style);
        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));
        context.setGenerationType("quiet");
        List<String> story = new ArrayList<>();
        for (ChatMessage message : branch) {
            if (StringUtils.isNotBlank(message.getResponse())) {
                story.add(message.getResponse());
            }
        }
        context.setStoryMessages(story);
        for (ChatMessage message : branch.reversed()) {
            if (message.getSummary() != null) {
                Summary summary = summaryService.find(message.getSummary());
                if (summary != null) {
                    context.setLatestSummary(summary.getSummary());
                }
                break;
            }
        }
        if (last != null) {
            context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, last.getAttributes());
        }
        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());

        LorebookTemplateData data = new LorebookTemplateData();
        if (last != null) {
            data.setPovCharacter(last.getPovCharacter());
            data.setSceneSetting(last.getSceneSetting());
            data.setPresentCharacters(last.getPresentCharacters());
            data.setInstructions(last.getInstructions());
        }
        data.setNarrativePov(pov);
        data.setNarrativeTense(tense);
        data.setStyle(style);
        data.setTemplateContext(context);
        return data;
    }
}
