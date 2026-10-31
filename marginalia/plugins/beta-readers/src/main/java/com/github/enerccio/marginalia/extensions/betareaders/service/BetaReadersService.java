package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersTemplateData;
import com.github.enerccio.marginalia.extensions.betareaders.model.BookData;
import com.github.enerccio.marginalia.extensions.betareaders.model.ChatKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Available;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Plan;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.StoryPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Summaries;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Stores what the plugin keeps: its settings in the user settings and the personas (and later the chats) in the book. What is
 * stored and how it is read is {@link BetaReadersStore}; this class only loads and saves the entities.
 */
@Configurable
public class BetaReadersService {

    @Autowired
    private SettingService settingService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private InferenceServices inferenceServices;

    /**
     * The settings of the user. The first time there are none, the defaults (the starter personas and the built-in mediums) are stored,
     * so the user edits and deletes them like their own; use this where the user works with the settings.
     */
    public BetaReadersSettings getSettings() throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        BetaReadersSettings settings = BetaReadersStore.readSettings(userSetting.getAttributes());
        if (settings == null) {
            settings = BetaReadersDefaults.newSettings();
            saveSettings(settings, userSetting);
            settingService.save(userSetting);
        }
        return settings;
    }

    /**
     * The settings of the user, or the defaults when there are none, and nothing is stored: the way to read them where a setting is
     * resolved (also from a generation), which must not write.
     */
    public BetaReadersSettings settingsOrDefaults() {
        try {
            BetaReadersSettings settings = BetaReadersStore.readSettings(settingService.getOrCreate(UserSetting.class).getAttributes());
            return settings != null ? settings : BetaReadersDefaults.newSettings();
        } catch (Exception e) {
            // asked from suppliers, which cannot throw a checked exception
            throw new IllegalStateException("The settings of the user can't be read", e);
        }
    }

    public void saveSettings(BetaReadersSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    /**
     * Puts the settings into the user settings, which the caller stores (the Settings screen stores its own).
     */
    public void saveSettings(BetaReadersSettings settings, UserSetting userSetting) {
        JsonObject attributes = userSetting.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            userSetting.setAttributes(attributes);
        }
        BetaReadersStore.writeSettings(attributes, settings);
    }

    /**
     * What the book stores, as of the book given.
     */
    public BookData getBook(Manuscript manuscript) {
        return BetaReadersStore.readBook(manuscript.getAttributes());
    }

    /**
     * What the book stores now: a book a window holds may be older than the stored one.
     */
    public BookData loadCurrentBook(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return getBook(current != null ? current : manuscript);
    }

    public void saveBook(Manuscript manuscript, BookData data) throws Exception {
        Manuscript stored = manuscriptService.find(manuscript);
        JsonObject attributes = stored.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            stored.setAttributes(attributes);
        }
        BetaReadersStore.writeBook(attributes, data);
        manuscriptService.save(stored);
    }

    /**
     * The personas the book can use: its own and the ones of the library with other names.
     */
    public List<Available> available(Manuscript manuscript) throws Exception {
        return BetaReadersStore.available(settingsOrDefaults(), loadCurrentBook(manuscript));
    }

    /**
     * The prompt of a chat with the medium in it ({@code {{medium}}}), as the system prompt before the description of the answer is
     * added.
     *
     * @param medium the prompt fragment of the medium; empty for none
     * @throws Exception when the prompt is not a valid template
     */
    public String renderPrompt(String template, String medium) throws Exception {
        BetaReadersTemplateData data = new BetaReadersTemplateData();
        data.setMedium(medium);
        return templateService.processTemplate(template, "betaReadersPrompt", data);
    }

    /**
     * The story as the personas can read it now.
     *
     * @param parts     the parts of the active branch (the empty ones are left to the tracker), first to last, numbered from 1
     * @param summaries the summaries that stand for the first parts; none unless asked for
     */
    public record Story(List<StoryPart> parts, Summaries summaries) {
    }

    /**
     * The active branch of the book as it is stored now.
     *
     * @param maxNumber     the last part the personas may read (the spoiler limit), 0 for no limit; the summaries are gathered from
     *                      the story up to it, so none tells what comes after
     * @param withSummaries whether the summaries are gathered (the first message of a chat needs them, the count of new parts does
     *                      not)
     */
    public Story loadStory(Manuscript manuscript, int maxNumber, boolean withSummaries) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        ChatMessage leaf = current != null && current.getActiveLeaf() != null ? chatMessageService.find(current.getActiveLeaf()) : null;
        List<ChatMessage> branch = leaf != null ? chatMessageService.getBranchFromLeaf(leaf) : List.of();

        List<StoryPart> parts = new ArrayList<>();
        for (int i = 0; i < branch.size(); i++) {
            parts.add(new StoryPart(String.valueOf(branch.get(i).getId()), i + 1, branch.get(i).getResponse()));
        }
        if (!withSummaries) {
            return new Story(parts, Summaries.NONE);
        }

        // the summaries of the story as a generation gathers them: oldest first, a meta summary in place of the ones it merged; one
        // whose story was changed since it was made is left out
        List<ChatMessage> readable = maxNumber > 0 && maxNumber < branch.size() ? branch.subList(0, maxNumber) : branch;
        List<SummaryService.SummaryBlock> blocks = summaryService.collectBlocks(new ArrayList<>(readable.reversed())).stream()
                .filter(SummaryService.SummaryBlock::isValid).toList();
        if (blocks.isEmpty()) {
            return new Story(parts, Summaries.NONE);
        }
        List<String> texts = blocks.reversed().stream().map(block -> block.summary().getSummary()).toList();
        return new Story(parts, new Summaries(texts, String.valueOf(blocks.getFirst().head().getId())));
    }

    // ------------------------------------------------------------------------------------------------------------
    // Chats
    // ------------------------------------------------------------------------------------------------------------

    /**
     * The AI and the protocol a chat uses.
     */
    public record Model(AI ai, Protocol protocol) {
    }

    /**
     * The chats the book has, as stored now.
     */
    public List<BetaChat> getChats(Manuscript manuscript) throws Exception {
        return loadCurrentBook(manuscript).getChats();
    }

    /**
     * Stores the chat: replaces the stored chat with its uuid, or adds it.
     */
    public void saveChat(Manuscript manuscript, BetaChat chat) throws Exception {
        BookData data = loadCurrentBook(manuscript);
        List<BetaChat> chats = data.getChats();
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).getUuid().equals(chat.getUuid())) {
                chats.set(i, chat);
                saveBook(manuscript, data);
                return;
            }
        }
        chats.add(chat);
        saveBook(manuscript, data);
    }

    public void deleteChat(Manuscript manuscript, String uuid) throws Exception {
        BookData data = loadCurrentBook(manuscript);
        if (data.getChats().removeIf(chat -> chat.getUuid().equals(uuid))) {
            saveBook(manuscript, data);
        }
    }

    /**
     * The AI and the protocol of the chat: the choice of the chat itself, else the settings chain (the book, the folders above it,
     * the user level of the plugin), else the ones of the book. One that is gone (deleted) counts as not set. The protocol is optional;
     * the AI is null when there is none to be found.
     */
    public Model resolveModel(Manuscript manuscript, BetaChat chat) throws Exception {
        Manuscript current = currentBook(manuscript);
        AI ai = ModelChoice.firstUsable(Arrays.asList(chat == null ? null : chat.getAi(),
                manuscriptService.resolveExtension(current, BetaReadersInherited.AI).value()), aiService::findForUser, found -> !found.isDeleted());
        if (ai == null && current.getAi() != null && !current.getAi().isDeleted()) {
            ai = current.getAi();
        }
        Protocol protocol = ModelChoice.firstUsable(Arrays.asList(chat == null ? null : chat.getProtocol(),
                manuscriptService.resolveExtension(current, BetaReadersInherited.PROTOCOL).value()), protocolService::findForUser,
                found -> !found.isDeleted());
        if (protocol == null && current.getProtocol() != null && !current.getProtocol().isDeleted()) {
            protocol = current.getProtocol();
        }
        return new Model(ai, protocol);
    }

    /**
     * The system prompt of the chat: the prompt of its kind (the chain of the book) with the medium (the one of the chat, else the
     * chain's) in it.
     *
     * @throws Exception when the prompt is not a valid template
     */
    public String systemPrompt(Manuscript manuscript, BetaChat chat) throws Exception {
        Manuscript current = currentBook(manuscript);
        String template = manuscriptService.resolveExtension(current, chat.getKind() == ChatKind.SINGLE
                ? BetaReadersInherited.SINGLE_PROMPT : BetaReadersInherited.GROUP_PROMPT).value();
        String mediumId = chat.getMedium() != null && !chat.getMedium().isBlank() ? chat.getMedium()
                : manuscriptService.resolveExtension(current, BetaReadersInherited.MEDIUM).value();
        return renderPrompt(template, mediumPrompt(settingsOrDefaults(), mediumId));
    }

    /**
     * The prompt fragment of the medium; the built-in one when the user's list lost it, empty when there is none.
     */
    static String mediumPrompt(BetaReadersSettings settings, String id) {
        return java.util.stream.Stream.concat(settings.getMediums().stream(), BetaReadersDefaults.builtInMediums().stream())
                .filter(medium -> medium.getId().equals(id)).map(Medium::getPrompt).findFirst().orElse("");
    }

    /**
     * Sends a message of the user and asks the model for the reply. The message (with the parts of the story it brings, when asked
     * to) is added to the chat and stored first; when the reply is finished it is added and stored too, before the listener hears of
     * it. Call it from a thread with the request of the user (the UI); the listener is called on other threads.
     *
     * @param includeLatest whether the message sends the story: the context when the chat has sent none yet, else the parts that are
     *                      new (see {@link PartsTracker})
     * @return what stops the request
     * @throws IllegalStateException    when there is no AI to ask
     * @throws IllegalArgumentException when there is nothing to send
     */
    public CancellationToken ask(Manuscript manuscript, BetaChat chat, String text, boolean includeLatest, ChatRunner.Listener listener)
            throws Exception {
        Run run = prepare(manuscript, chat);

        BetaMessage user = BetaMessage.user(text == null ? "" : text.strip());
        if (includeLatest) {
            Story story = loadStory(manuscript, 0, true);
            // what is left of the prompt after the system prompt, the history and the question goes to the story
            chat.getMessages().add(user);
            int used = PromptBuilder.build(chat, run.system(), new PromptBuilder.Options(true, true,
                    BetaReadersDefaults.maxMessages(chat.getKind(), chat.present().size()), 0, run.tokens())).tokens();
            chat.getMessages().removeLast();
            int budget = run.limit() > 0 ? Math.max(1, run.limit() - used) : 0;
            Plan plan = PartsTracker.plan(chat, story.parts(), story.summaries(), 0, budget, run.tokens());
            user.setContext(plan.text());
            user.setSentParts(new ArrayList<>(plan.sent()));
            user.setGoneParts(new ArrayList<>(plan.gone()));
        }
        if (user.getText().isBlank() && user.getContext().isBlank()) {
            throw new IllegalArgumentException("There is nothing to send");
        }
        chat.getMessages().add(user);
        saveChat(manuscript, chat);
        return start(manuscript, chat, run, listener);
    }

    /**
     * Asks again: takes the last reply back (when the history ends with one) and asks the model for another. The parts the message
     * before it sent stay sent. Also the way to try again after a request that failed, when the history ends with the message of the user.
     *
     * @throws IllegalStateException when there is no AI to ask, or there is nothing to answer
     */
    public CancellationToken regenerate(Manuscript manuscript, BetaChat chat, ChatRunner.Listener listener) throws Exception {
        Run run = prepare(manuscript, chat);
        BetaMessage dropped = ChatEdits.dropLastReply(chat);
        if (chat.getMessages().isEmpty() || !chat.getMessages().getLast().isUserSide()) {
            if (dropped != null) {
                chat.getMessages().add(dropped);
            }
            throw new IllegalStateException("There is nothing to answer");
        }
        saveChat(manuscript, chat);
        return start(manuscript, chat, run, listener);
    }

    /** What a request of a chat needs, found before anything of the chat is changed. */
    private record Run(Model model, String system, InferenceService inference, ToIntFunction<String> tokens, int limit) {
    }

    private Run prepare(Manuscript manuscript, BetaChat chat) throws Exception {
        Model model = resolveModel(manuscript, chat);
        if (model.ai() == null) {
            throw new IllegalStateException("There is no AI to ask");
        }
        String system = systemPrompt(manuscript, chat);
        InferenceService inference = inferenceServices.forAIOrThrow(model.ai());
        ToIntFunction<String> tokens = text -> {
            try {
                return (int) inference.countTokensApprox(text);
            } catch (Exception e) {
                return PromptBuilder.Options.estimate(text);
            }
        };
        return new Run(model, system, inference, tokens, TokenLimits.promptTokens(model.ai(), model.protocol()));
    }

    private CancellationToken start(Manuscript manuscript, BetaChat chat, Run run, ChatRunner.Listener listener) throws Exception {
        // the reply ends on another thread: it is stored as the user who asked
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        ChatRunner.Request request = new ChatRunner.Request(chat, run.system(), Boolean.TRUE.equals(run.model().ai().getEnabledReasoning()),
                BetaReadersDefaults.maxMessages(chat.getKind(), chat.present().size()), run.limit(), run.tokens());
        ChatRunner.Listener storing = new ChatRunner.Listener() {
            @Override
            public void onProgress(ReplyReader.Reading reading) {
                listener.onProgress(reading);
            }

            @Override
            public void onFinished(ChatRunner.Outcome outcome) {
                ChatRunner.Outcome result = outcome;
                if (outcome.reply() != null) {
                    chat.getMessages().add(outcome.reply());
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        saveChat(manuscript, chat);
                    } catch (Exception e) {
                        result = new ChatRunner.Outcome(outcome.reply(), outcome.cancelled(), e, outcome.prompt(), outcome.json());
                    }
                }
                listener.onFinished(result);
            }
        };
        try (InferenceSession _ = InferenceSession.open("beta-readers"); UsageContext _ = UsageContext.open(manuscript)) {
            return ChatRunner.run(run.inference(), run.model().protocol(), request, storing);
        }
    }

    private Manuscript currentBook(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return current != null ? current : manuscript;
    }

    /**
     * The AIs of the user to choose from for the inherited setting, by uuid.
     */
    public List<Choice> aiChoices() {
        try {
            return getAvailableAis().stream().map(ai -> new Choice(ai.getUuid(), ai.getName())).toList();
        } catch (Exception e) {
            throw new IllegalStateException("The AIs can't be listed", e);
        }
    }

    /**
     * The protocols of the user to choose from for the inherited setting, by uuid.
     */
    public List<Choice> protocolChoices() {
        try {
            return getAvailableProtocols().stream().map(protocol -> new Choice(protocol.getUuid(), protocol.getName())).toList();
        } catch (Exception e) {
            throw new IllegalStateException("The protocols can't be listed", e);
        }
    }

    /**
     * The AIs of the user that are not deleted.
     */
    public List<AI> getAvailableAis() throws Exception {
        return aiService.findAllForUser().stream().filter(ai -> !ai.isDeleted()).toList();
    }

    /**
     * The protocols of the user that are not deleted.
     */
    public List<Protocol> getAvailableProtocols() throws Exception {
        return protocolService.findAllForUser().stream().filter(protocol -> !protocol.isDeleted()).toList();
    }
}
