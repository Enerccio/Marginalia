package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Available;
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
import java.util.Map;
import java.util.function.Consumer;
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
    private PartTextRegistry partTexts;

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

    /**
     * The most messages the model may write in a turn of the chat: its own limit, else the one in the settings of the user, else the
     * built-in one.
     */
    public int maxMessages(BetaChat chat) {
        return BetaReadersDefaults.maxMessages(chat.getKind(), chat.present().size(), chat.getMaxMessages(),
                settingsOrDefaults().getMaxMessages());
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
     * The prompt of a chat with the medium ({@code {{medium}}}) and the scene ({@code {{scene}}}) in it, as the system prompt before the
     * description of the answer is added. A prompt that does not use {@code {{scene}}} gets the scene after it.
     *
     * @param medium the prompt fragment of the medium; empty for none
     * @param scene  the scene; empty for none
     * @throws Exception when the prompt is not a valid template
     */
    public String renderPrompt(String template, String medium, String scene) throws Exception {
        BetaReadersTemplateData data = new BetaReadersTemplateData();
        data.setMedium(medium);
        data.setScene(scene);
        return PromptBuilder.withScene(template, templateService.processTemplate(template, "betaReadersPrompt", data), scene);
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
            parts.add(new StoryPart(String.valueOf(branch.get(i).getId()), i + 1, partTexts.forModel(branch.get(i))));
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
     * The system prompt of the chat: the prompt of its kind (the chain of the book) with the medium and the scene (the ones of the chat,
     * else the chain's) in it.
     *
     * @throws Exception when the prompt is not a valid template
     */
    public String systemPrompt(Manuscript manuscript, BetaChat chat) throws Exception {
        Manuscript current = currentBook(manuscript);
        String template = manuscriptService.resolveExtension(current, chat.getKind() == ChatKind.SINGLE
                ? BetaReadersInherited.SINGLE_PROMPT : BetaReadersInherited.GROUP_PROMPT).value();
        Medium medium = resolveMedium(manuscript, chat);
        return renderPrompt(template, medium == null ? "" : medium.getPrompt(), resolveScene(manuscript, chat));
    }

    /**
     * The scene of the chat: its own, else the one of the settings chain (the book, the folders above it, the settings of the user, the
     * built-in one).
     *
     * @param chat the chat; null for what a chat of this book would inherit
     */
    public String resolveScene(Manuscript manuscript, BetaChat chat) throws Exception {
        if (chat != null && chat.getScene() != null && !chat.getScene().isBlank()) {
            return chat.getScene();
        }
        return manuscriptService.resolveExtension(currentBook(manuscript), BetaReadersInherited.SCENE).value();
    }

    /**
     * The medium of the chat: its own choice, else the one of the settings chain. Null when there is none (the medium was deleted
     * and is not a built-in one either).
     *
     * @param chat the chat; null for what a chat of this book would inherit
     */
    public Medium resolveMedium(Manuscript manuscript, BetaChat chat) throws Exception {
        String id = chat != null && chat.getMedium() != null && !chat.getMedium().isBlank() ? chat.getMedium()
                : manuscriptService.resolveExtension(currentBook(manuscript), BetaReadersInherited.MEDIUM).value();
        return findMedium(settingsOrDefaults(), id);
    }

    /**
     * The medium with the id: from the list of the user, else a built-in one (the user's list lost it); null when there is none.
     */
    static Medium findMedium(BetaReadersSettings settings, String id) {
        return java.util.stream.Stream.concat(settings.getMediums().stream(), BetaReadersDefaults.builtInMediums().stream())
                .filter(medium -> medium.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Takes the current definition of a persona of the chat from the book or the library (the one with its uuid, else the one with its
     * name; the one of the book wins) into the chat's copy.
     *
     * @return whether there is such a persona in the book or the library
     */
    public boolean refreshPersona(Manuscript manuscript, BetaChat chat, String uuid) throws Exception {
        Persona copy = chat.getPersonas().stream().map(member -> member.getPersona()).filter(persona -> persona.getUuid().equals(uuid))
                .findFirst().orElse(null);
        if (copy == null) {
            return false;
        }
        List<Available> available = available(manuscript);
        Persona source = available.stream().map(Available::persona).filter(persona -> persona.getUuid().equals(uuid)).findFirst()
                .orElseGet(() -> available.stream().map(Available::persona)
                        .filter(persona -> BetaReadersStore.nameKey(persona.getName()).equals(BetaReadersStore.nameKey(copy.getName())))
                        .findFirst().orElse(null));
        if (source == null) {
            return false;
        }
        // a persona found by its name is another one with the same name: the copy keeps its identity
        Persona fresh = source.copy();
        fresh.setUuid(uuid);
        ChatEdits.refresh(chat, fresh);
        saveChat(manuscript, chat);
        return true;
    }

    /**
     * Remembers which chat tab is open in the book.
     */
    public void setActiveChat(Manuscript manuscript, String uuid) throws Exception {
        BookData data = loadCurrentBook(manuscript);
        if (!java.util.Objects.equals(data.getActiveChat(), uuid)) {
            data.setActiveChat(uuid);
            saveBook(manuscript, data);
        }
    }

    /**
     * Sends a message of the user and asks the model for the reply. The message (with the parts of the story it gives to the personas,
     * when asked to) is added to the chat and stored first; when the reply is finished it is added and stored too, before the listener
     * hears of it. Call it from a thread with the request of the user (the UI); the listener is called on other threads.
     *
     * @param includeLatest whether the message gives the personas the parts of the story they were not given yet (see
     *                      {@link PartsTracker}); the story they read is built from the story as it is now for every request
     * @return the message that was added and what stops the request
     * @throws NoModelException         when there is no AI to ask
     * @throws IllegalArgumentException when there is nothing to send
     */
    public Asked ask(Manuscript manuscript, BetaChat chat, String text, boolean includeLatest, ChatRunner.Listener listener)
            throws Exception {
        return ask(manuscript, chat, text, includeLatest, false, listener);
    }

    /**
     * Sends a scene: the text is a change of scene or a description of what the user does, not something the user says, and the model
     * is asked for the reply to it. It gives no parts of the story. Otherwise it is {@link #ask}.
     *
     * @throws NoModelException         when there is no AI to ask
     * @throws IllegalArgumentException when the text is empty
     */
    public Asked askScene(Manuscript manuscript, BetaChat chat, String text, ChatRunner.Listener listener) throws Exception {
        return ask(manuscript, chat, text, false, true, listener);
    }

    private Asked ask(Manuscript manuscript, BetaChat chat, String text, boolean includeLatest, boolean scene,
                      ChatRunner.Listener listener) throws Exception {
        Run run = prepare(manuscript, chat);

        Story story = storyFor(manuscript, chat, includeLatest);
        BetaMessage user = scene ? BetaMessage.scene(text) : userMessage(chat, story, text, includeLatest);
        if (user.getText().isBlank() && user.getSentParts().isEmpty()) {
            throw new IllegalArgumentException("There is nothing to send");
        }
        chat.getMessages().add(user);
        saveChat(manuscript, chat);
        return new Asked(user, start(manuscript, chat, run, storyBlocks(chat, chat, run, story), listener));
    }

    /**
     * The story as it is now, when a request needs it: the chat includes the latest parts, or it was given parts that the personas read.
     * Null when it needs none (reading the book costs, so a chat that was only talked to does not).
     */
    private Story storyFor(Manuscript manuscript, BetaChat chat, boolean includeLatest) throws Exception {
        if (!includeLatest && PartsTracker.sentBy(chat).isEmpty()) {
            return null;
        }
        return loadStory(manuscript, chat.getSpoilerLimit(), chat.isSummaries());
    }

    /**
     * The message of the user as it would be sent: the text and, when asked to, the parts it gives to the personas. It is not added to
     * the chat.
     */
    private BetaMessage userMessage(BetaChat chat, Story story, String text, boolean includeLatest) {
        BetaMessage user = BetaMessage.user(text == null ? "" : text.strip());
        if (includeLatest && story != null) {
            user.setSentParts(new ArrayList<>(PartsTracker.unreleased(chat, story.parts(), chat.getFromPart(), chat.getSpoilerLimit())));
        }
        return user;
    }

    /**
     * The story the personas read in a request: the parts they were given (by the messages of {@code view}, which is the chat or a view
     * of it with a message about to be sent), as they are now, in front of the messages that gave them. It gets what is left of the
     * prompt after the system prompt and the history, and at least half of it, the history is trimmed to make room.
     *
     * @param chat the chat that has the settings (the first part, the spoiler limit)
     */
    private Map<String, String> storyBlocks(BetaChat chat, BetaChat view, Run run, Story story) {
        if (story == null) {
            return Map.of();
        }
        PromptBuilder.Options options = new PromptBuilder.Options(ChatRunner.mode(chat.getFormat()).json(),
                maxMessages(chat), 0, run.tokens());
        int used = PromptBuilder.build(view, run.system(), options).tokens();
        int budget = run.limit() > 0 ? Math.max(run.limit() / 2, run.limit() - used) : 0;
        return PartsTracker.blocks(view, story.parts(), story.summaries(), chat.getFromPart(), chat.getSpoilerLimit(), budget, run.tokens())
                .byMessage();
    }

    /**
     * Asks again: takes the last reply back (when the history ends with one) and asks the model for another. The parts the message
     * before it gave stay given. Also the way to try again after a request that failed, when the history ends with the message of the user.
     *
     * @throws NoModelException       when there is no AI to ask
     * @throws IllegalStateException when there is nothing to answer
     */
    public Asked regenerate(Manuscript manuscript, BetaChat chat, ChatRunner.Listener listener) throws Exception {
        Run run = prepare(manuscript, chat);
        BetaMessage dropped = ChatEdits.dropLastReply(chat);
        if (chat.getMessages().isEmpty() || !chat.getMessages().getLast().isUserSide()) {
            if (dropped != null) {
                chat.getMessages().add(dropped);
            }
            throw new IllegalStateException("There is nothing to answer");
        }
        saveChat(manuscript, chat);
        return new Asked(null, start(manuscript, chat, run, storyBlocks(chat, chat, run, storyFor(manuscript, chat, false)), listener));
    }

    /**
     * A request that was started.
     *
     * @param message the message of the user that was added to the chat; null when the request answers what is there already
     * @param token   stops the request
     */
    public record Asked(BetaMessage message, CancellationToken token) {
    }

    /**
     * How the story stands against what the chat was given: the number of parts to include, see {@link PartsTracker#status}.
     */
    public PartsTracker.Status partsStatus(Manuscript manuscript, BetaChat chat) throws Exception {
        return PartsTracker.status(chat, loadStory(manuscript, 0, false).parts(), chat.getFromPart(), chat.getSpoilerLimit());
    }

    /**
     * How much of the context the chat uses.
     *
     * @param tokens the tokens of the request the chat would send now, without a new message
     * @param limit  the tokens the model takes for a prompt; 0 when not known
     */
    public record ContextUsage(int tokens, int limit) {
    }

    /**
     * The size of the request the chat would send now; fails like {@link #ask} when there is no AI.
     */
    public ContextUsage contextUsage(Manuscript manuscript, BetaChat chat) throws Exception {
        return contextUsage(manuscript, chat, "", false);
    }

    /**
     * The size of the request the chat would send now with the message of the user that is being written: the text typed and, when
     * the box is ticked, the parts of the story it would bring.
     */
    public ContextUsage contextUsage(Manuscript manuscript, BetaChat chat, String draft, boolean includeLatest) throws Exception {
        Run run = prepare(manuscript, chat);
        Story story = storyFor(manuscript, chat, includeLatest);
        BetaChat view = chat;
        if (includeLatest || (draft != null && !draft.isBlank())) {
            BetaMessage user = userMessage(chat, story, draft, includeLatest);
            if (!user.getText().isBlank() || !user.getSentParts().isEmpty()) {
                view = chat.preview(user);
            }
        }
        PromptBuilder.Options options = new PromptBuilder.Options(ChatRunner.mode(chat.getFormat()).json(),
                maxMessages(chat), 0, run.tokens(), Map.of());
        // a message that is not sent yet may bring parts: the story is the one the request would read
        options = options.withStory(storyBlocks(chat, view, run, story));
        return new ContextUsage(PromptBuilder.build(view, run.system(), options).tokens(), run.limit());
    }

    /** What a request of a chat needs, found before anything of the chat is changed. */
    private record Run(Model model, String system, InferenceService inference, ToIntFunction<String> tokens, int limit) {
    }

    private Run prepare(Manuscript manuscript, BetaChat chat) throws Exception {
        Model model = resolveModel(manuscript, chat);
        if (model.ai() == null) {
            throw new NoModelException();
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

    private CancellationToken start(Manuscript manuscript, BetaChat chat, Run run, Map<String, String> story, ChatRunner.Listener listener)
            throws Exception {
        // the reply ends on another thread: it is stored as the user who asked
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        ChatRunner.Request request = new ChatRunner.Request(chat, run.system(),
                maxMessages(chat), run.limit(), run.tokens(), story);
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

    /**
     * The AI and the protocol that write a persona: the ones of the chats of the book (the chain), or, without a book (the settings of
     * the user), the ones of the user's settings of this plugin, else the default model and protocol of the user.
     */
    public Model resolvePersonaModel(Manuscript manuscript) throws Exception {
        if (manuscript != null) {
            return resolveModel(manuscript, null);
        }
        BetaReadersSettings settings = settingsOrDefaults();
        UserSetting user = settingService.getOrCreate(UserSetting.class);
        AI ai = ModelChoice.firstUsable(Arrays.asList(settings.getAi()), aiService::findForUser, found -> !found.isDeleted());
        if (ai == null && user.getDefaultModel() != null) {
            AI found = aiService.find(user.getDefaultModel());
            ai = found != null && !found.isDeleted() ? found : null;
        }
        Protocol protocol = ModelChoice.firstUsable(Arrays.asList(settings.getProtocol()), protocolService::findForUser,
                found -> !found.isDeleted());
        if (protocol == null && user.getDefaultProtocol() != null) {
            Protocol found = protocolService.find(user.getDefaultProtocol());
            protocol = found != null && !found.isDeleted() ? found : null;
        }
        return new Model(ai, protocol);
    }

    /**
     * Lets the model write a persona from a few words, see {@link PersonaWriter}.
     *
     * @param manuscript the book the persona is for; null for a persona of the user's library
     * @param taken      the names the persona must not take
     * @param done       told when it ended, on a thread of the inference service
     * @return what stops it
     * @throws NoModelException when there is no AI to ask
     */
    public CancellationToken writePersona(Manuscript manuscript, String brief, List<String> taken, Consumer<PersonaWriter.Outcome> done)
            throws Exception {
        Model model = resolvePersonaModel(manuscript);
        if (model.ai() == null) {
            throw new NoModelException();
        }
        InferenceService inference = inferenceServices.forAIOrThrow(model.ai());
        try (InferenceSession _ = InferenceSession.open("beta-readers");
             UsageContext _ = manuscript != null ? UsageContext.open(manuscript) : null) {
            return PersonaWriter.run(inference, model.protocol(), brief, taken, done);
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
