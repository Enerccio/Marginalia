package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.components.AnnotationTexts;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.InferenceService.TypedInferenceCallback;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException;
import com.github.enerccio.marginalia.domain.templates.AnnotateTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.LLMField;
import com.github.enerccio.marginalia.domain.traits.LLMSchema;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.utils.QuoteFinder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.request.RequestAttributes;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotateServiceImpl implements AnnotateService {

    private static final Logger log = LoggerFactory.getLogger(AnnotateServiceImpl.class);

    /**
     * Tokens kept free for what the count does not see (the headers of the messages).
     */
    private static final long BUFFER_TOKENS = 100;

    /**
     * What the model is told about the shape of its answer, by mode. It is a part of the prompt (the variable {@code outputFormat}) so that
     * a prompt the author wrote does not have to know the modes.
     */
    static final String STRUCTURED_FORMAT = """
            ### OUTPUT FORMAT
            Answer with a JSON object {"findings": [...]}. Each finding has "quote" (the passage, copied letter for letter from the part), "note" (what is wrong, for the author) and, optionally, "type" (one word: continuity, lore, style, pacing, dialogue...). An empty list when there is nothing to flag. Nothing else.""";

    static final String DELIMITED_FORMAT = """
            ### OUTPUT FORMAT
            Answer with the notes only, one after another, separated by a line with three dashes (---). Each note is written like this:
            > the passage, copied letter for letter from the part
            the note for the author (it may take several lines; start it with [type] to give the kind: continuity, lore, style...)

            When there is nothing to flag, answer with the word NONE. Write nothing else, no introduction and no summary.""";

    static final String CRITIQUE_FORMAT = """
            ### OUTPUT FORMAT
            Write your review as free text. Quote the passages you mean letter for letter from the part, so that a note can be tied to them.""";

    private static final String EXTRACT_PROMPT = """
            An editor reviewed one part of a story and wrote a free critique. Turn the critique into notes for the author: one finding for each problem the editor names, with "quote" (the passage the editor means, copied letter for letter from the PART), "note" (what the editor says, shortened) and, optionally, "type" (one word for the kind). Findings the editor did not make are not yours to add. When the critique names no problem, the list is empty.

            """ + STRUCTURED_FORMAT;

    private static final Set<String> NOTHING = Set.of("none", "no notes", "no problems", "no findings", "nothing", "nothing to flag", "n/a");
    private static final Pattern SEPARATOR_LINE = Pattern.compile("^\\s*(?:-{3,}|\\*{3,}|_{3,})\\s*$");
    private static final Pattern TYPE_TAG = Pattern.compile("^\\s*\\[([^\\]\\n]{1,40})]\\s*");
    private static final String QUOTE_CHARS = "\"'“”‘’«»`";

    /**
     * What the model answers with in the structured mode.
     */
    @LLMSchema(description = "The notes of the editor on a part of the story")
    public record Findings(
            @LLMField(description = "The notes, none when there is nothing to flag") List<Finding> findings) {
    }

    @LLMSchema(description = "A note about a passage of the part")
    public record Finding(
            @LLMField(description = "The passage, copied letter for letter from the part", minLength = 1) String quote,
            @LLMField(description = "What is wrong or weak, for the author", minLength = 1) String note,
            @LLMField(description = "One word for the kind of the note: continuity, lore, style, pacing, dialogue...", required = false) String type) {
    }

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SettingService settingService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Override
    @CommonTxReadOnly
    public GenerationOptions defaultOptions() throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        AI ai = setting.getDefaultAnnotateModel() == null ? null : aiService.find(setting.getDefaultAnnotateModel());
        Protocol protocol = setting.getDefaultAnnotateProtocol() == null ? null : protocolService.find(setting.getDefaultAnnotateProtocol());
        if (ai != null && ai.isDeleted()) {
            ai = null;
        }
        if (protocol != null && protocol.isDeleted()) {
            protocol = null;
        }
        return GenerationOptions.defaults().withModel(ai, protocol);
    }

    @Override
    @CommonTxReadOnly
    public CancellationToken annotate(Manuscript manuscript, ChatMessage part, Request request, Callback callback) throws Exception {
        if (manuscript == null || part == null || callback == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        Manuscript book = (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        ChatMessage stored = (ChatMessage) Hibernate.unproxy(chatMessageService.find(part));
        if (stored == null || stored.isDeleted() || stored.getParentScript() == null
                || !Objects.equals(stored.getParentScript().getId(), book.getId())) {
            throw new IllegalArgumentException("The part does not exist");
        }
        if (StringUtils.isBlank(stored.getResponse())) {
            throw new IllegalArgumentException("The part has no text");
        }
        Request asked = request == null ? Request.of(null) : request;
        GenerationOptions chosen = asked.options() == null ? GenerationOptions.defaults() : asked.options();
        AI wanted = chosen.ai() != null ? chosen.ai() : book.getAi();
        Protocol wantedProtocol = chosen.protocol() != null ? chosen.protocol() : book.getProtocol();
        if (wanted == null || wantedProtocol == null) {
            return null;
        }
        AI ai = aiService.find(wanted);
        Protocol protocol = protocolService.find(wantedProtocol);
        if (ai == null || protocol == null) {
            return null;
        }
        InferenceService inference = inferenceServices.forAIOrThrow(ai);

        Mode mode = asked.mode() == null ? Mode.AUTO : asked.mode();
        String format = switch (mode) {
            case TWO_STAGE -> CRITIQUE_FORMAT;
            case DELIMITED -> DELIMITED_FORMAT;
            default -> STRUCTURED_FORMAT;
        };
        List<LLMChatMessage> payload = createPayload(book, stored, StringUtils.trimToEmpty(asked.instructions()), asked.prompt(), format,
                ai, protocol, inference);

        Run run = new Run(stored.getResponse(), callback);
        run.attributes = ThreadCopyRequestAttributes.create();
        try {
            switch (mode) {
                case DELIMITED -> run.stream(inference, protocol, payload, false, null, null);
                case TWO_STAGE -> {
                    AI extractAi = asked.extractAi() == null ? null : aiService.find(asked.extractAi());
                    Protocol extractProtocol = asked.extractProtocol() == null ? null : protocolService.find(asked.extractProtocol());
                    run.stream(inference, protocol, payload, true, extractAi == null ? ai : extractAi,
                            extractAi == null ? protocol : (extractProtocol == null ? protocol : extractProtocol));
                }
                default -> run.structured(inference, protocol, payload, mode == Mode.AUTO);
            }
        } catch (Exception e) {
            log.error("Failed to start the annotation inference", e);
            run.fail(e);
        }
        return run.token;
    }

    @Override
    @CommonTx
    public List<Annotation> accept(Manuscript manuscript, ChatMessage part, List<Proposal> proposals, String label) throws Exception {
        List<Annotation> made = new ArrayList<>();
        for (Proposal proposal : proposals) {
            if (StringUtils.isBlank(proposal.note())) {
                continue;
            }
            made.add(annotationService.createFromQuote(manuscript, part, proposal.quote(), null, null, proposal.note(),
                    AnnotationSource.AI, proposal.type(), label));
        }
        return made;
    }

    // ---- one run

    @FunctionalInterface
    private interface Done<T> {
        void accept(T result) throws Exception;
    }

    /**
     * One annotation in progress: the callback, the token that stops it and the steps (structured answer, stream, extraction).
     */
    private class Run {

        private final String text;
        private final Callback callback;
        private final CancellationToken token = new CancellationToken();
        private RequestAttributes attributes;

        Run(String text, Callback callback) {
            this.text = text;
            this.callback = callback;
        }

        /**
         * The answer is JSON matching a schema, one more try when it doesn't. A service that can't infer makes the answer text in
         * blocks when {@code fallBack}.
         */
        void structured(InferenceService inference, Protocol protocol, List<LLMChatMessage> payload, boolean fallBack) throws Exception {
            try {
                ask(inference, protocol, payload, Findings.class, 1, result -> finish(proposalsOf(result), null));
            } catch (UnsupportedOperationException e) {
                if (!fallBack) {
                    throw e;
                }
                // the provider can't be asked for a schema: the notes are asked for as text
                stream(inference, protocol, withFormat(payload, STRUCTURED_FORMAT, DELIMITED_FORMAT), false, null, null);
            }
        }

        /**
         * The answer is the JSON of the class. When it can't be read the model is told what is wrong and asked once more; after that the
         * answer is handed over as it is.
         */
        <T> void ask(InferenceService inference, Protocol protocol, List<LLMChatMessage> payload, Class<T> type, int retries,
                     Done<T> done) throws Exception {
            inference.infer(payload, protocol, type, new TypedInferenceCallback<T>() {
                @Override
                public void onResult(T result, String raw) throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        done.accept(result);
                    }
                }

                @Override
                public void onCancel() throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        callback.onTerminated("");
                    }
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        if (exception instanceof StructuredOutputException unreadable) {
                            if (retries > 0 && !token.isCancelled()) {
                                List<LLMChatMessage> again = new ArrayList<>(payload);
                                again.add(LLMChatMessage.of(LLMRole.ASSISTANT, unreadable.getAnswer()));
                                again.add(LLMChatMessage.of(LLMRole.USER, "That answer could not be read: "
                                        + String.join("; ", unreadable.getErrors()) + ". Answer again with the JSON object only."));
                                try {
                                    ask(inference, protocol, again, type, retries - 1, done);
                                } catch (Exception e) {
                                    callback.onError(e);
                                }
                                return;
                            }
                            callback.onUnreadable(unreadable.getAnswer(), String.join("; ", unreadable.getErrors()));
                            return;
                        }
                        callback.onError(exception);
                    }
                }

                @Override
                public boolean isDead() {
                    return token.isCancelled();
                }
            });
        }

        /**
         * The answer is a stream of text. Read as blocks, or (the first stage of the two stage mode) given to the second model.
         */
        void stream(InferenceService inference, Protocol protocol, List<LLMChatMessage> payload, boolean twoStage, AI extractAi,
                    Protocol extractProtocol) throws Exception {
            StringBuilder reasoning = new StringBuilder();
            StringBuilder answer = new StringBuilder();
            inference.stream(payload, protocol, new InferenceAsyncCallback() {
                @Override
                public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String chunk) throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        if (chunkType == ChunkType.REASONING) {
                            reasoning.append(chunk);
                        } else {
                            answer.append(chunk);
                        }
                        callback.onProgress(reasoning.toString(), answer.toString());
                        controller.continueInference();
                    }
                }

                @Override
                public void onCompletion() throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        String raw = answer.toString().strip();
                        if (!twoStage) {
                            finish(proposalsOf(parseBlocks(raw)), raw);
                            return;
                        }
                        extract(raw, extractAi, extractProtocol);
                    }
                }

                @Override
                public void onCancel() throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        callback.onTerminated(answer.toString().strip());
                    }
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        callback.onError(exception);
                    }
                }

                @Override
                public boolean isDead() {
                    return token.isCancelled();
                }
            });
        }

        /**
         * The second stage: another model turns the critique into notes.
         */
        private void extract(String critique, AI ai, Protocol protocol) throws Exception {
            if (StringUtils.isBlank(critique)) {
                finish(List.of(), critique);
                return;
            }
            InferenceService inference = inferenceServices.forAIOrThrow(ai);
            List<LLMChatMessage> payload = List.of(
                    LLMChatMessage.of(LLMRole.SYSTEM, EXTRACT_PROMPT),
                    LLMChatMessage.of(LLMRole.USER, "=== PART START ===\n" + text + "\n=== PART END ===\n\n=== CRITIQUE START ===\n"
                            + critique + "\n=== CRITIQUE END ==="));
            try {
                ask(inference, protocol, payload, Findings.class, 1, result -> finish(proposalsOf(result), critique));
            } catch (UnsupportedOperationException e) {
                // the second model can't answer in a schema: it is asked for text
                List<LLMChatMessage> asText = List.of(LLMChatMessage.of(LLMRole.SYSTEM,
                        EXTRACT_PROMPT.replace(STRUCTURED_FORMAT, DELIMITED_FORMAT)), payload.get(1));
                StringBuilder answer = new StringBuilder();
                inference.stream(asText, protocol, new InferenceAsyncCallback() {
                    @Override
                    public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String chunk) throws Exception {
                        if (chunkType != ChunkType.REASONING) {
                            answer.append(chunk);
                        }
                        controller.continueInference();
                    }

                    @Override
                    public void onCompletion() throws Exception {
                        try (InRequestScope _ = new InRequestScope(attributes)) {
                            finish(proposalsOf(parseBlocks(answer.toString().strip())), critique);
                        }
                    }

                    @Override
                    public void onCancel() throws Exception {
                        try (InRequestScope _ = new InRequestScope(attributes)) {
                            callback.onTerminated(critique);
                        }
                    }

                    @Override
                    public void onError(Throwable exception) throws Exception {
                        try (InRequestScope _ = new InRequestScope(attributes)) {
                            callback.onError(exception);
                        }
                    }

                    @Override
                    public boolean isDead() {
                        return token.isCancelled();
                    }
                });
            }
        }

        /**
         * Ties the notes to the passages they quote.
         */
        private List<Proposal> proposalsOf(Findings findings) {
            List<Proposal> proposals = new ArrayList<>();
            if (findings != null && findings.findings() != null) {
                for (Finding finding : findings.findings()) {
                    proposals.add(anchored(finding.quote(), finding.note(), finding.type()));
                }
            }
            return proposals;
        }

        private List<Proposal> proposalsOf(List<Finding> findings) {
            List<Proposal> proposals = new ArrayList<>();
            for (Finding finding : findings) {
                proposals.add(anchored(finding.quote(), finding.note(), finding.type()));
            }
            return proposals;
        }

        private Proposal anchored(String quote, String note, String type) {
            QuoteFinder.Match match = StringUtils.isBlank(quote) ? null : QuoteFinder.find(text, quote, null, null, -1);
            return new Proposal(StringUtils.defaultString(quote), StringUtils.defaultString(note).strip(), StringUtils.trimToNull(type),
                    match == null ? null : match.start(), match == null ? null : match.end());
        }

        private void finish(List<Proposal> proposals, String raw) throws Exception {
            callback.onFinished(proposals, raw);
        }

        void fail(Throwable error) {
            try (InRequestScope _ = new InRequestScope(attributes)) {
                callback.onError(error);
            } catch (Exception e) {
                log.error("The annotation failed and the failure could not be reported", e);
            }
        }
    }

    /**
     * The same prompt with the output format of another mode: the format is the last thing of the prompt.
     */
    private static List<LLMChatMessage> withFormat(List<LLMChatMessage> payload, String from, String to) {
        List<LLMChatMessage> changed = new ArrayList<>();
        for (LLMChatMessage message : payload) {
            String content = message.getContent();
            changed.add(content != null && content.contains(from)
                    ? LLMChatMessage.of(message.getRole(), content.replace(from, to)) : message);
        }
        return changed;
    }

    // ---- reading the answer

    /**
     * Reads the notes of an answer in blocks: separated by a line of dashes (or a quote after a blank line), a quote is the lines that
     * start with {@code >}, the note is the rest of the block, its first words in brackets are the type. Leniently: quotation marks
     * around the quote are dropped, a block without a quote is a note about the whole part, a block without a note is dropped.
     */
    public static List<Finding> parseBlocks(String answer) {
        List<Finding> findings = new ArrayList<>();
        if (StringUtils.isBlank(answer) || NOTHING.contains(normalized(answer))) {
            return findings;
        }
        List<List<String>> blocks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        boolean noteStarted = false;
        for (String line : answer.split("\\R", -1)) {
            boolean separator = SEPARATOR_LINE.matcher(line).matches();
            // a quote after the note of the one before starts the next block, also without a separator
            boolean newQuote = line.stripLeading().startsWith(">") && noteStarted;
            if (separator || newQuote) {
                if (!current.isEmpty()) {
                    blocks.add(current);
                }
                current = new ArrayList<>();
                noteStarted = false;
                if (separator) {
                    continue;
                }
            }
            current.add(line);
            if (!line.stripLeading().startsWith(">") && !line.isBlank()) {
                noteStarted = true;
            }
        }
        if (!current.isEmpty()) {
            blocks.add(current);
        }

        for (List<String> block : blocks) {
            StringBuilder quote = new StringBuilder();
            StringBuilder note = new StringBuilder();
            for (String line : block) {
                String stripped = line.stripLeading();
                if (stripped.startsWith(">")) {
                    if (!quote.isEmpty()) {
                        quote.append(' ');
                    }
                    quote.append(stripped.substring(1).strip());
                } else {
                    if (!note.isEmpty() || !line.isBlank()) {
                        note.append(line.strip()).append('\n');
                    }
                }
            }
            String text = note.toString().strip();
            if (text.isEmpty() || NOTHING.contains(normalized(text)) && quote.isEmpty()) {
                continue;
            }
            String type = null;
            Matcher tag = TYPE_TAG.matcher(text);
            if (tag.find()) {
                type = tag.group(1).strip();
                text = text.substring(tag.end()).strip();
            }
            findings.add(new Finding(unquoted(quote.toString()), text, type));
        }
        return findings;
    }

    private static String normalized(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z/ ]", "").strip();
    }

    /**
     * The quote without the quotation marks the model put around it.
     */
    private static String unquoted(String quote) {
        String result = quote.strip();
        while (result.length() > 1 && QUOTE_CHARS.indexOf(result.charAt(0)) >= 0 && QUOTE_CHARS.indexOf(result.charAt(result.length() - 1)) >= 0) {
            result = result.substring(1, result.length() - 1).strip();
        }
        return result;
    }

    // ---- the prompt

    private List<LLMChatMessage> createPayload(Manuscript book, ChatMessage part, String instructions, String prompt, String outputFormat,
                                               AI ai, Protocol protocol, InferenceService inferenceService) throws Exception {
        List<ChatMessage> upToPart = new ArrayList<>(chatMessageService.getBranchFromLeaf(part));
        List<ChatMessage> before = upToPart.isEmpty() ? new ArrayList<>() : new ArrayList<>(upToPart.subList(0, upToPart.size() - 1));
        List<ChatMessage> after = StoryAround.partsAfter(chatMessageService, book, part);

        String template = StringUtils.isNotBlank(prompt) ? prompt : manuscriptService.getAnnotatePrompt(book);
        int maxTokens = TokenLimits.promptTokens(ai, protocol);

        AnnotateTemplateData data = new AnnotateTemplateData();
        data.setBackgroundLore(StringUtils.defaultString(messagePromptService.getBackgroundLore(part)));
        data.setNarrativePov(manuscriptService.getPov(book));
        data.setNarrativeTense(manuscriptService.getTense(book));
        data.setStyle(manuscriptService.getStyle(book));
        data.setPovCharacter(StringUtils.defaultString(part.getPovCharacter()));
        data.setSceneSetting(StringUtils.defaultString(part.getSceneSetting()));
        data.setPresentCharacters(StringUtils.defaultString(part.getPresentCharacters()));
        data.setInstructions(StringUtils.defaultString(part.getInstructions()));
        data.setText(part.getResponse());
        data.setAnnotateInstructions(instructions);
        data.setAnnotations(AnnotationTexts.openAnnotations(annotationService.listForPart(book, part)));
        data.setOutputFormat(outputFormat);
        data.setSummaries("");
        data.setPreviousStory("");
        data.setFollowingStory("");

        TemplateContext context = SummaryServiceImpl.createTemplateContext(book, part, ai, protocol, new ArrayList<>(before.reversed()));
        // estimation render must not change variables or the real render would apply them twice
        data.setTemplateContext(context.fork());

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + StoryAround.SEPARATOR : "";

        long base = inferenceService.countTokens(jailbreak + templateService.processTemplate(template, "annotatePrompt", data)) + BUFFER_TOKENS;
        long room = maxTokens - base;
        if (room < 0) {
            throw new ContextInsufficient(base, maxTokens);
        }
        StoryAround.Around around = StoryAround.collect(summaryService, inferenceService, before, after, room);
        data.setSummaries(around.summaries());
        data.setPreviousStory(around.previous());
        data.setFollowingStory(around.following());
        data.setTemplateContext(context);
        String rendered = jailbreak + templateService.processTemplate(template, "annotatePrompt", data);
        long tokens = inferenceService.countTokens(rendered);
        if (tokens > maxTokens) {
            throw new ContextInsufficient(tokens, maxTokens);
        }
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, rendered));
    }

}
