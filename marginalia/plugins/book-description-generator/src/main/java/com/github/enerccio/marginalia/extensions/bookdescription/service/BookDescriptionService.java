package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.bookdescription.model.*;
import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner.BranchEntry;
import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner.Piece;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.enerccio.marginalia.extensions.bookdescription.BookDescriptionLocalization.T;

@Configurable
public class BookDescriptionService {

    /** How many rounds of notes at most: notes of notes of notes... A story that is still too long after that is not described. */
    static final int MAX_NOTES_ROUNDS = 4;

    /** The model and the protocol the generation runs with. */
    public record ModelSelection(AI ai, Protocol protocol) {
    }

    /**
     * A message of the active branch that has text, for the range fields of the dialog.
     *
     * @param number the place among the messages with text, from 1
     * @param order  the place among all the parts of the branch, from 1
     */
    public record StoryMessage(int number, int order, String text) {
    }

    /** The active branch of a book: the messages with text to choose the range from and all its parts for the planner. */
    public record Branch(List<StoryMessage> messages, List<BranchEntry> entries) {
    }

    /**
     * What a run tells while it works. Called on the thread that runs.
     */
    public interface Progress {

        /** A round of notes starts: {@code round} from 1; the notes of the earlier rounds were too long to be sent at once. */
        void notesRound(int round, int parts);

        /** The notes of a part are ready. */
        void notes(int part, int parts, String notes);

        /** The name and the description are being asked for. */
        void describing(boolean fromNotes);
    }

    /**
     * What a run came to.
     *
     * @param description the name and the description; null when the answer could not be read
     * @param answer      the answer of the model as it came
     * @param notes       the notes the description was made of, empty when the story was sent as it is
     */
    public record Result(AnswerParser.Parsed description, String answer, List<String> notes) {
    }

    /** The logger of the extension (Admin → Extensions → Log), set when the extension loads. */
    private Logger log;

    @Autowired
    private SettingService settingService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private User currentUser;

    private final Gson gson = new Gson();

    public void setLogger(Logger log) {
        this.log = log;
    }

    // --- settings of the user ---

    /** Settings of the current user. */
    public BookDescriptionSettings getSettings() throws Exception {
        return getSettings(settingService.getOrCreate(UserSetting.class));
    }

    public BookDescriptionSettings getSettings(UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        BookDescriptionSettings settings = null;
        if (attrs != null && attrs.has(BookDescriptionSettings.KEY)) {
            settings = gson.fromJson(attrs.get(BookDescriptionSettings.KEY), BookDescriptionSettings.class);
        }
        return settings != null ? settings : new BookDescriptionSettings();
    }

    /**
     * The settings of the user, or blank ones when there are none, and nothing is stored: the way to read them where a prompt is
     * resolved (a book's dialog, which must not write).
     */
    public BookDescriptionSettings settingsOrBlank() {
        try {
            UserSetting userSetting = settingService.find(UserSetting.class, currentUser);
            return userSetting == null ? new BookDescriptionSettings() : getSettings(userSetting);
        } catch (Exception e) {
            // asked from suppliers, which cannot throw a checked exception
            throw new IllegalStateException("The settings of the user can't be read", e);
        }
    }

    public void saveSettings(BookDescriptionSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    /** Puts the settings into {@code userSetting}, saving the setting is up to the caller. */
    public void saveSettings(BookDescriptionSettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(BookDescriptionSettings.KEY, gson.toJsonTree(settings));
    }

    // --- the prompts of a book ---

    /** The prompt for the name and the description that applies to the book: its own, its folders', the user's, the built-in one. */
    public String descriptionPrompt(Manuscript book) throws Exception {
        return StringUtils.defaultIfBlank(manuscriptService.resolveExtension(book, BookDescriptionInherited.DESCRIPTION_PROMPT).value(),
                T.defaults().descriptionPrompt());
    }

    /** The prompt for the notes that applies to the book, resolved like {@link #descriptionPrompt}. */
    public String notesPrompt(Manuscript book) throws Exception {
        return StringUtils.defaultIfBlank(manuscriptService.resolveExtension(book, BookDescriptionInherited.NOTES_PROMPT).value(),
                T.defaults().notesPrompt());
    }

    // --- the model ---

    /**
     * The model and protocol the dialog starts with: those of the book, and when the book has no model the defaults of the user.
     *
     * @return null when neither the book nor the user has a model
     */
    public ModelSelection resolveModel(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        Manuscript book = current != null ? current : manuscript;
        AI ai = book.getAi();
        if (ai != null && !ai.isDeleted()) {
            Protocol protocol = book.getProtocol();
            return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        AI defaultAi = userSetting.getDefaultModel() != null ? aiService.find(userSetting.getDefaultModel()) : null;
        if (defaultAi == null || defaultAi.isDeleted()) {
            return null;
        }
        Protocol protocol = userSetting.getDefaultProtocol() != null ? protocolService.find(userSetting.getDefaultProtocol()) : null;
        return new ModelSelection(defaultAi, protocol != null && !protocol.isDeleted() ? protocol : null);
    }

    // --- the story ---

    /**
     * The active branch of the book as it is stored now (the dialog keeps the book it was opened with, the branch may have changed):
     * the messages with text, and every part with the summary that stands in for it where there is a valid one.
     *
     * @return empty when the book has no active branch
     */
    public Branch activeBranch(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        ChatMessage leaf = (current != null ? current : manuscript).getActiveLeaf();
        if (leaf == null) {
            return new Branch(List.of(), List.of());
        }
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(leaf);

        // the summaries: a block belongs to the message that holds the summary and reaches back to the one after the next older block
        Map<Long, SummaryBlock> blocks = new HashMap<>();
        for (SummaryBlock block : summaryService.collectBlocks(new ArrayList<>(branch.reversed()))) {
            blocks.put(block.head().getId(), block);
        }

        List<StoryMessage> messages = new ArrayList<>();
        List<BranchEntry> entries = new ArrayList<>();
        int order = 0;
        int previousHead = 0;
        for (ChatMessage message : branch) {
            order++;
            String text = partTexts.forModel(message);
            int number = 0;
            long tokens = 0;
            if (StringUtils.isNotBlank(text)) {
                number = messages.size() + 1;
                tokens = message.getTokenCount() > 0 ? message.getTokenCount() : countTokens(text);
                messages.add(new StoryMessage(number, order, text));
            }
            BranchEntry entry = BranchEntry.message(order, number, text, tokens);
            SummaryBlock block = blocks.get(message.getId());
            if (block != null) {
                if (block.isValid() && block.summary() != null && StringUtils.isNotBlank(block.summary().getSummary())) {
                    Summary summary = block.summary();
                    long summaryTokens = summary.getSummaryTokens() != null && summary.getSummaryTokens() > 0
                            ? summary.getSummaryTokens() : countTokens(summary.getSummary());
                    entry = entry.withSummary(summary.getSummary(), summaryTokens, previousHead + 1);
                }
                // an outdated summary is not used, but the block still ends here
                previousHead = order;
            }
            entries.add(entry);
        }
        return new Branch(messages, entries);
    }

    /** The approximate number of tokens of the text. */
    public long countTokens(String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        try {
            return tokenizerService.countTokensApprox(text);
        } catch (Exception e) {
            // a rough estimate does for a counter
            return text.length() / 4;
        }
    }

    // --- the prompts ---

    /**
     * The prompt for the name and the description, with the instructions for the format of the answer after it.
     *
     * @param story     the story (or the notes made of it), may be empty to measure the prompt without it
     * @param fromNotes {@code story} is the notes
     * @param format    {@link OutputFormat#AUTO} is asked like {@link OutputFormat#JSON}
     * @throws Exception when the template is not valid
     */
    public String buildDescriptionPrompt(Manuscript book, String template, String story, boolean fromNotes, OutputFormat format) throws Exception {
        DescriptionTemplateData data = new DescriptionTemplateData();
        Manuscript current = current(book);
        data.setStory(story);
        data.setFromNotes(fromNotes);
        data.setBookName(StringUtils.defaultString(current.getName()));
        data.setBookDescription(StringUtils.defaultString(current.getDescription()));
        return templateService.processTemplate(template, "bookDescription", data).stripTrailing()
                + "\n\n" + T.defaults().outputInstructions(format);
    }

    /**
     * The prompt for the notes of one part of the story.
     *
     * @throws Exception when the template is not valid
     */
    public String buildNotesPrompt(Manuscript book, String template, String story, int part, int parts) throws Exception {
        NotesTemplateData data = new NotesTemplateData();
        Manuscript current = current(book);
        data.setStory(story);
        data.setPart(part);
        data.setParts(parts);
        data.setBookName(StringUtils.defaultString(current.getName()));
        data.setBookDescription(StringUtils.defaultString(current.getDescription()));
        return templateService.processTemplate(template, "bookDescriptionNotes", data);
    }

    private Manuscript current(Manuscript book) throws Exception {
        Manuscript current = manuscriptService.find(book);
        return current != null ? current : book;
    }

    /** What of the prompt the model has room for: a little is kept back for the inexact count of the tokens. */
    public static long usable(int available) {
        return available - available / 20;
    }

    /**
     * Whether the pieces are more than the model takes with the prompt: the story is then made into notes first.
     *
     * @param overhead  the tokens of the description prompt without the story
     * @param available what the model takes as a prompt, 0 when it is not known (then nothing is ever too long)
     */
    public static boolean needsNotes(List<Piece> pieces, long overhead, int available) {
        return available > 0 && StoryPlanner.cost(pieces) + overhead > usable(available);
    }

    // --- the run ---

    /**
     * Writes the name and the description of the book. A story that fits in the model with the prompt is sent as it is; a longer
     * one is first divided into parts that fit, the notes prompt is run on each of them, and the notes (divided again when they are
     * still too long) are what the description prompt gets. Blocks until it is done, the request is cancelled when the thread is
     * interrupted.
     *
     * @param book   the book, the tokens of the requests are counted for it
     * @param pieces what to describe, see {@link StoryPlanner#pieces}
     * @throws CancellationException when the run was stopped
     * @throws IllegalStateException when the story can't be made to fit
     * @throws UnsupportedOperationException when the format needs a schema and the model's service can't take one
     */
    public Result generate(ModelSelection model, Manuscript book, List<Piece> pieces, String descriptionTemplate, String notesTemplate,
                           OutputFormat format, Progress progress) throws Exception {
        if (pieces.isEmpty()) {
            throw new IllegalStateException("There is nothing to describe");
        }
        int available = TokenLimits.promptTokens(model.ai(), model.protocol());
        long overhead = countTokens(buildDescriptionPrompt(book, descriptionTemplate, "", true, format));

        List<String> notes = new ArrayList<>();
        String story = StoryPlanner.render(pieces);
        boolean fromNotes = false;
        if (needsNotes(pieces, overhead, available)) {
            fromNotes = true;
            long notesOverhead = countTokens(buildNotesPrompt(book, notesTemplate, "", 1, 1));
            List<Piece> current = pieces;
            int round = 0;
            while (true) {
                round++;
                if (round > MAX_NOTES_ROUNDS) {
                    throw new IllegalStateException("The story is still too long after " + MAX_NOTES_ROUNDS + " rounds of notes");
                }
                List<List<Piece>> parts = StoryPlanner.chunk(current, usable(available) - notesOverhead);
                if (log != null) {
                    log.info("The story of '{}' is too long for the model: round {} of notes, {} parts", book.getName(), round, parts.size());
                }
                progress.notesRound(round, parts.size());
                notes.clear();
                for (int i = 0; i < parts.size(); i++) {
                    String prompt = buildNotesPrompt(book, notesTemplate, StoryPlanner.render(parts.get(i)), i + 1, parts.size());
                    String text = AnswerParser.withoutReasoning(complete(model, book, prompt, null)).strip();
                    if (text.isEmpty()) {
                        throw new IllegalStateException("The model wrote no notes for part " + (i + 1) + " of " + parts.size());
                    }
                    notes.add(text);
                    progress.notes(i + 1, parts.size(), text);
                }
                List<Piece> notePieces = new ArrayList<>();
                for (int i = 0; i < notes.size(); i++) {
                    notePieces.add(new Piece("Notes on part " + (i + 1) + " of " + notes.size(), notes.get(i), Math.max(1, countTokens(notes.get(i)))));
                }
                if (!needsNotes(notePieces, overhead, available)) {
                    story = StoryPlanner.render(notePieces);
                    break;
                }
                if (parts.size() <= 1) {
                    // one part of notes and still too long: another round would give the same
                    throw new IllegalStateException("The notes are too long for the model and can't be made shorter");
                }
                current = notePieces;
            }
        }

        progress.describing(fromNotes);
        OutputFormat asked = format;
        String answer;
        try {
            answer = describe(model, book, descriptionTemplate, story, fromNotes, asked);
        } catch (UnsupportedOperationException e) {
            if (format != OutputFormat.AUTO) {
                throw e;
            }
            // a service that can not take a schema: ask in Markdown
            asked = OutputFormat.MARKDOWN;
            answer = describe(model, book, descriptionTemplate, story, fromNotes, asked);
        }
        return new Result(AnswerParser.parse(answer), answer, List.copyOf(notes));
    }

    private String describe(ModelSelection model, Manuscript book, String template, String story, boolean fromNotes, OutputFormat format) throws Exception {
        String prompt = buildDescriptionPrompt(book, template, story, fromNotes, format);
        return complete(model, book, prompt, format.schema() ? StructuredOutputs.schemaOf(GeneratedDescription.class) : null);
    }

    /** The prompt as the system message, with a short user turn some providers require. */
    List<LLMChatMessage> toPayload(String systemPrompt) {
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, systemPrompt), LLMChatMessage.of(LLMRole.USER, "[ Start ]"));
    }

    /**
     * Asks the model and waits for the whole answer (without its reasoning). The request is cancelled when the waiting thread is
     * interrupted, which is how a stopped run ends.
     *
     * @param schema the schema of the answer, null for free text
     */
    String complete(ModelSelection model, Manuscript book, String prompt, JsonNode schema) throws Exception {
        CompletableFuture<String> answer = new CompletableFuture<>();
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open("book-description-generator"); UsageContext _ = UsageContext.open(book)) {
            token = inferenceServices.forAIOrThrow(model.ai()).infer(toPayload(prompt), model.protocol(), schema,
                    new InferenceService.InferenceResultCallback() {
                        @Override
                        public void onResult(String text, SchemaCheck check) {
                            answer.complete(text);
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
}
