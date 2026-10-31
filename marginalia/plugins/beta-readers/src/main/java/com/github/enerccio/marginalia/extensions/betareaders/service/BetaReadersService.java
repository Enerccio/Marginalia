package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersTemplateData;
import com.github.enerccio.marginalia.extensions.betareaders.model.BookData;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore.Available;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.StoryPart;
import com.github.enerccio.marginalia.extensions.betareaders.service.PartsTracker.Summaries;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

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
