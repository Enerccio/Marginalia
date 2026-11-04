package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Origin;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Translates the direct speech of a part into the language of the book: asks the model (one request for a part), checks the answer, writes the
 * translations into the text of the part (one edit, with a version in the history, not flagged as the author's), makes a passage of every
 * speech with its English source, and adds the words the model invented to the lexicon.
 * <p>
 * The part is read as the model reads it (so a part that is translated already is translated again from its source). The writes of a book are
 * one at a time. A run that asks the model and is stopped or fails writes nothing.
 */
@Configurable
public class TranslationService {

    /** What came of a translation. */
    public enum Status {
        /** The speeches were written into the text. */
        TRANSLATED,
        /** The model found no speech in the language: nothing was changed. */
        NOTHING_TO_TRANSLATE,
        /** The book has no language, or the one it names is not in the library. */
        NO_LANGUAGE,
        /** There is no model for the request. */
        NO_MODEL,
        /** The part has no text. */
        EMPTY,
        /** The model did not answer in the expected form; nothing was changed. */
        BAD_ANSWER
    }

    /**
     * @param status      what came of it
     * @param translated  how many speeches were written into the text
     * @param newWords    how many words were added to the lexicon
     * @param dropped     how many speeches of the answer could not be used (quote not found, another language, overlap)
     * @param needsRepair how many written speeches have words that the model did not report and the lexicon does not have
     * @param note        what the user should know, may be empty
     */
    public record Outcome(Status status, int translated, int newWords, int dropped, int needsRepair, String note) {

        static Outcome of(Status status, String note) {
            return new Outcome(status, 0, 0, 0, 0, note);
        }
    }

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private PassageService passageService;
    @Autowired
    private PartTextRegistry partTexts;
    @Autowired
    private SettingService settingService;
    @Autowired
    private InferenceServices inferenceServices;
    @Autowired
    private AIService aiService;
    @Autowired
    private ProtocolService protocolService;

    private final PassageAccess passages = new PassageAccess();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    /**
     * The part as it is stored now.
     */
    public ChatMessage find(ChatMessage part) throws Exception {
        return chatMessageService.find(part);
    }

    /**
     * The settings of the book as it is stored now.
     */
    public BookSettings settings(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return resolve(current != null ? current : manuscript);
    }

    /**
     * What applies to the book: the language, the automatic run, the model and the protocol are the inherited settings (the book's own, else a
     * folder's, else the user's), the number of earlier parts and the size limit of the lexicon are the book's own.
     */
    BookSettings resolve(Manuscript book) throws Exception {
        BookSettings settings = ConlangStore.readBook(book.getAttributes());
        settings.setLanguageId(StringUtils.trimToNull(manuscriptService.resolveExtension(book, ConlangInherited.LANGUAGE).value()));
        settings.setAuto(ConlangInherited.YES.equals(manuscriptService.resolveExtension(book, ConlangInherited.AUTO).value()));
        settings.setAiUuid(StringUtils.trimToNull(manuscriptService.resolveExtension(book, ConlangInherited.AI).value()));
        settings.setProtocolUuid(StringUtils.trimToNull(manuscriptService.resolveExtension(book, ConlangInherited.PROTOCOL).value()));
        return settings;
    }

    /**
     * The language of the library the book names, null when it names none or the language is gone.
     */
    public Language languageOf(BookSettings settings) throws Exception {
        UserSetting user = settingService.getOrCreate(UserSetting.class);
        return ConlangStore.find(user.getAttributes(), settings.getLanguageId());
    }

    /**
     * Translates the speech of the part.
     */
    public Outcome translate(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        ChatMessage current = chatMessageService.find(part);
        if (book == null || current == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        BookSettings settings = resolve(book);
        Language language = languageOf(settings);
        if (language == null) {
            return Outcome.of(Status.NO_LANGUAGE, settings.getLanguageId() == null ? "The book has no language." : "The language of the book is not in your library.");
        }
        // the text the model reads: a part that is translated already is translated again from its English
        Reverse.Result read = readOf(current);
        String source = read.text();
        if (StringUtils.isBlank(source)) {
            return Outcome.of(Status.EMPTY, "The part has no text.");
        }
        ModelSelection model = resolveModel(book, settings);
        if (model == null) {
            return Outcome.of(Status.NO_MODEL, "There is no model for the translation: the book has none and neither have your defaults.");
        }

        TranslationPrompt.Request request = TranslationPrompt.build(language, source, scene(book, current, settings), settings.getMaxLexiconChars());
        String raw = ask(model, book, request);
        TranslationAnswer answer = TranslationAnswer.parse(raw);
        if (answer == null) {
            return Outcome.of(Status.BAD_ANSWER, "The model did not answer in the expected form.");
        }
        AnswerChecker.Checked checked = AnswerChecker.check(source, answer, language);
        TranslationPlan plan = TranslationPlan.make(source, checked.accepted());
        int dropped = (int) checked.speeches().stream().filter(item -> !item.accepted()).count();
        if (plan.isEmpty()) {
            return new Outcome(Status.NOTHING_TO_TRANSLATE, 0, 0, dropped, 0, "");
        }
        Object lock = locks.computeIfAbsent(book.getUuid(), uuid -> new Object());
        synchronized (lock) {
            write(book, current, language, model, plan, checked, read);
        }
        int repair = (int) checked.accepted().stream().filter(item -> !item.unknownWords().isEmpty()).count();
        return new Outcome(Status.TRANSLATED, plan.passages().size(), checked.words().size(), dropped, repair,
                repair > 0 ? repair + " translation(s) use words that are not in the lexicon and were not reported." : "");
    }

    /**
     * The part as a model reads it, with the passages that were edited by the author (they are left as they are).
     */
    private Reverse.Result readOf(ChatMessage part) throws Exception {
        List<com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located> located = passages.of(List.of(part))
                .getOrDefault(part.getId(), List.of());
        return Reverse.toModel(part.getResponse(), located);
    }

    private TranslationPrompt.Scene scene(Manuscript book, ChatMessage part, BookSettings settings) throws Exception {
        List<String> earlier = new ArrayList<>();
        if (settings.getContextParts() > 0) {
            List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(part);
            int index = branch.size() - 1;
            for (int i = 0; i < branch.size(); i++) {
                if (branch.get(i).getId().equals(part.getId())) {
                    index = i;
                    break;
                }
            }
            List<ChatMessage> before = branch.subList(Math.max(0, index - settings.getContextParts()), index);
            earlier.addAll(partTexts.forModel(before));
        }
        return new TranslationPrompt.Scene(StringUtils.defaultString(part.getPovCharacter()), StringUtils.defaultString(part.getPresentCharacters()), earlier);
    }

    /** What is written: the text, the passages, the new words. The part is read again under the lock, so an edit made meanwhile is not lost. */
    private void write(Manuscript book, ChatMessage part, Language language, ModelSelection model, TranslationPlan plan,
                       AnswerChecker.Checked checked, Reverse.Result read) throws Exception {
        ChatMessage fresh = chatMessageService.find(part);
        if (!StringUtils.defaultString(fresh.getResponse()).equals(StringUtils.defaultString(part.getResponse()))) {
            throw new IllegalStateException("The part was changed while it was being translated");
        }
        // the old passages go with the text they stood in, but the ones the author edited stay: they hold his words
        for (AnchoredPassage old : passages.passagesOf(fresh)) {
            var located = PassageAccess.locate(old);
            boolean editedByAuthor = located != null && read.edited().stream().anyMatch(e -> e.start() == located.start() && e.end() == located.end());
            boolean unusable = located == null;
            if (!editedByAuthor && !unusable) {
                passageService.remove(book, old);
            }
        }
        boolean untouched = fresh.getOriginalResponse() != null && fresh.getOriginalResponse().equals(fresh.getResponse());
        chatMessageService.replaceText(book, fresh, plan.stored(), plan.source(), untouched ? plan.stored() : null,
                RevisionOrigin.automatic(RevisionDetail.TRANSLATION, false));
        long now = System.currentTimeMillis();
        String modelName = model.ai().getName();
        for (TranslationPlan.Planned planned : plan.passages()) {
            AnchoredPassage passage = passageService.create(book, fresh, Span.KIND, planned.start(), planned.end());
            Span span = new Span(language.getId(), planned.english(), planned.conlang());
            span.setSpeaker(StringUtils.defaultString(planned.speech().speaker()));
            span.setUnderstood(planned.speech().isUnderstood());
            span.setRegister(StringUtils.defaultString(planned.speech().register()));
            span.setModel(modelName);
            span.setTime(now);
            span.write(passage.getAttributes());
            passageService.save(passage);
        }
        if (!checked.words().isEmpty()) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            Language stored = ConlangStore.find(user.getAttributes(), language.getId());
            if (stored != null) {
                NewWords.addTo(stored, checked.words(), Origin.model(fresh.getUuid(), modelName, now));
                ConlangStore.save(user.getAttributes(), stored);
                settingService.save(user);
            }
        }
    }

    // --- the model ---

    /** The model and the protocol of a request. */
    record ModelSelection(AI ai, Protocol protocol) {
    }

    /**
     * The model and the protocol of the book's settings of this plugin, else the book's, else the defaults of the user; null when there is none.
     */
    ModelSelection resolveModel(Manuscript book, BookSettings settings) throws Exception {
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
     * Asks the model and waits for the whole answer.
     */
    private String ask(ModelSelection model, Manuscript book, TranslationPrompt.Request request) throws Exception {
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, request.system()), LLMChatMessage.of(LLMRole.USER, request.user()));
        CompletableFuture<String> answer = new CompletableFuture<>();
        StringBuilder text = new StringBuilder();
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open("conlang-translator"); UsageContext _ = UsageContext.open(book)) {
            token = inferenceServices.forAIOrThrow(model.ai()).stream(payload, model.protocol(), new InferenceService.InferenceAsyncCallback() {
                @Override
                public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String chunk) throws Exception {
                    if (chunkType != InferenceService.ChunkType.REASONING) {
                        text.append(chunk);
                    }
                    controller.continueInference();
                }

                @Override
                public void onCompletion() {
                    answer.complete(text.toString());
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
