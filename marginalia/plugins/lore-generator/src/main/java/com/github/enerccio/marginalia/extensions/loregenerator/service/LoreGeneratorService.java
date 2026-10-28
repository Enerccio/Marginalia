package com.github.enerccio.marginalia.extensions.loregenerator.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.tool.ToolRequest;
import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.domain.service.tool.ToolSet;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreEntryGeneration;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configurable
public class LoreGeneratorService {

    public static final String DEFAULT_GENERATION_PROMPT = "DEFAULT";

    /** Header of the lore in the system prompt. */
    public static final String LORE_HEADER = "[ Existing Lore ]";

    /** Replaced by the generated text in the compaction prompt. */
    public static final String COMPACTION_PLACEHOLDER = "{{compactionData}}";

    /** The lore the generated entry is written against. */
    public enum LoreScope {
        /** The lorebook and all its subbooks, transitively. */
        ALL_LORE,
        /** Only the lorebook itself, its subbooks are ignored. */
        CURRENT_LOREBOOK
    }

    /** Which entries of the lore are shown to the model. */
    public enum EntryLimit {
        /** Entries that come before the generated entry (by order, ties by creation). */
        UP_TO_POSITION,
        ALL_ENTRIES
    }

    /** The compaction prompt asks for a fenced block to be copyable, an entry should not contain the fence. */
    private static final Pattern FENCED = Pattern.compile("\\A\\s*```[^\\n]*\\n(.*?)\\n?```\\s*\\z", Pattern.DOTALL);

    /** The model and the protocol the generation runs with, taken from the user settings. */
    public record ModelSelection(AI ai, Protocol protocol) {
    }

    /** The logger of the extension (Admin → Extensions → Log), set when the extension loads. */
    private Logger log;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ToolService toolService;

    /** Set when the extension loads, to tell the tool providers who asks. */
    private Bundle bundle;

    private final Gson gson = new Gson();

    public void setLogger(Logger log) {
        this.log = log;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * The functions and the hints of the tools the selection turns on (the suites that are installed, the items that are
     * still offered); {@link ToolSet#NONE} when there are none.
     *
     * @throws IllegalStateException when two suites have a function of the same name
     */
    public ToolSet tools(ToolSelection selection) {
        return toolService.resolve(selection, new ToolRequest(bundle, LoreGeneratorService.class.getName()));
    }

    /**
     * Data of the current user. Without any generation prompt the {@value #DEFAULT_GENERATION_PROMPT} one is added.
     */
    public LoreGeneratorData getData() throws Exception {
        return getData(settingService.getOrCreate(UserSetting.class));
    }

    public LoreGeneratorData getData(UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        LoreGeneratorData data = null;
        if (attrs != null && attrs.has(LoreGeneratorData.KEY)) {
            data = gson.fromJson(attrs.get(LoreGeneratorData.KEY), LoreGeneratorData.class);
        }
        if (data == null) {
            data = new LoreGeneratorData();
        }
        data.migrateLegacyNameTools();
        if (data.getTools() == null) {
            data.setTools(new ToolSelection());
        }
        if (data.getGenerationPrompts() == null) {
            data.setGenerationPrompts(new LinkedHashMap<>());
        }
        if (data.getGenerationPrompts().isEmpty()) {
            data.getGenerationPrompts().put(DEFAULT_GENERATION_PROMPT, Defaults.GENERATION_PROMPT);
        }
        return data;
    }

    /**
     * The options the last generation copied to the entry was made with, null when there is none.
     */
    public LoreEntryGeneration getEntryGeneration(LorebookEntry entry) {
        JsonObject attrs = entry.getAttributes();
        if (attrs == null || !attrs.has(LoreGeneratorData.KEY)) {
            return null;
        }
        try {
            return gson.fromJson(attrs.get(LoreGeneratorData.KEY), LoreEntryGeneration.class);
        } catch (RuntimeException e) {
            if (log != null) {
                log.warn("Unreadable generation options of lore entry '{}': {}", entry.getName(), e.getMessage());
            }
            return null;
        }
    }

    /** Puts the options into the attributes of the entry, saving the entry is up to the caller. */
    public void putEntryGeneration(LorebookEntry entry, LoreEntryGeneration generation) {
        if (entry.getAttributes() == null) {
            entry.setAttributes(new JsonObject());
        }
        entry.getAttributes().add(LoreGeneratorData.KEY, gson.toJsonTree(generation));
    }

    public void saveData(LoreGeneratorData data) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveData(data, userSetting);
        settingService.save(userSetting);
    }

    /**
     * Puts the data into {@code userSetting}, saving the setting is up to the caller.
     */
    public void saveData(LoreGeneratorData data, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(LoreGeneratorData.KEY, gson.toJsonTree(data));
    }

    /**
     * The default model and protocol of the user. The protocol is optional.
     *
     * @return null when the user has no default model
     */
    public ModelSelection resolveModel() throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        AI ai = userSetting.getDefaultModel() != null ? aiService.find(userSetting.getDefaultModel()) : null;
        if (ai == null || ai.isDeleted()) {
            return null;
        }
        Protocol protocol = userSetting.getDefaultProtocol() != null ? protocolService.find(userSetting.getDefaultProtocol()) : null;
        if (protocol != null && protocol.isDeleted()) {
            protocol = null;
        }
        return new ModelSelection(ai, protocol);
    }

    /**
     * The prompt for generating {@code target}: the lore the user selected followed by the generation prompt, and the
     * hints of the tools the model has (the user's or the default one of each suite).
     *
     * @param tools what the model gets, {@link ToolSet#NONE} or null for none
     */
    public String buildGenerationPrompt(Lorebook current, LorebookEntry target, LoreScope scope, EntryLimit limit,
                                        String generationPrompt, ToolSet tools) throws Exception {
        String prompt = buildLoreBlock(current, target, scope, limit) + "\n\n" + generationPrompt;
        if (tools != null && !tools.isEmpty() && StringUtils.isNotBlank(tools.hint())) {
            prompt += "\n\n" + tools.hint();
        }
        return prompt;
    }

    /**
     * The prompt for compacting {@code generated}: the same lore as in the generation followed by the compaction prompt
     * of the user ({@link Defaults#COMPACTION_PROMPT} when blank) with the generated text in it.
     */
    public String buildCompactionPrompt(Lorebook current, LorebookEntry target, LoreScope scope, EntryLimit limit,
                                        String generated) throws Exception {
        String prompt = StringUtils.defaultIfBlank(getData().getCompactionPrompt(), Defaults.COMPACTION_PROMPT);
        // not a template: the generated text and the prompt may contain any braces
        if (prompt.contains(COMPACTION_PLACEHOLDER)) {
            prompt = StringUtils.replace(prompt, COMPACTION_PLACEHOLDER, generated);
        } else {
            prompt = prompt + "\n\n" + generated;
        }
        return buildLoreBlock(current, target, scope, limit) + "\n\n" + prompt;
    }

    /** The prompt as the system message, with a short user turn some providers require. */
    public List<LLMChatMessage> toPayload(String systemPrompt) {
        return toPayload(systemPrompt, List.of());
    }

    /**
     * Like {@link #toPayload(String)}, the attachments (images, files...) are sent with the user turn.
     */
    public List<LLMChatMessage> toPayload(String systemPrompt, List<LLMAttachment> attachments) {
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, systemPrompt));
        LLMChatMessage user = LLMChatMessage.of(LLMRole.USER, "[ Start ]");
        attachments.forEach(user::addAttachment);
        payload.add(user);
        return payload;
    }

    /**
     * The lore of the system prompt. Which entries are used depends only on the scope and the limit: enabled flags,
     * tags and filtering of lorebooks and entries are ignored, and so is the entry being generated.
     */
    String buildLoreBlock(Lorebook current, LorebookEntry target, LoreScope scope, EntryLimit limit) throws Exception {
        StringBuilder lore = new StringBuilder(LORE_HEADER).append('\n');
        // one template data for all entries, like the generation does: variables set in an entry are visible in the next ones
        LorebookTemplateData templateData = new LorebookTemplateData();
        for (LorebookEntry entry : collectEntries(current, target, scope, limit)) {
            String payload = render(entry, templateData);
            if (StringUtils.isNotBlank(payload)) {
                lore.append('\n').append(payload.trim()).append('\n');
            }
        }
        return lore.toString();
    }

    /**
     * The entries of the lore in the order of the generation (order, ties broken by the creation).
     */
    List<LorebookEntry> collectEntries(Lorebook current, LorebookEntry target, LoreScope scope, EntryLimit limit) throws Exception {
        List<Lorebook> lorebooks = scope == LoreScope.ALL_LORE ? withSubbooks(current) : List.of(current);

        Comparator<LorebookEntry> position = Comparator
                .comparingInt(LorebookEntry::getOrder)
                .thenComparing(LorebookEntry::getCreation, Comparator.nullsLast(Comparator.<Date>naturalOrder()))
                .thenComparing(LorebookEntry::getId, Comparator.nullsLast(Comparator.<Long>naturalOrder()));

        List<LorebookEntry> entries = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Lorebook lorebook : lorebooks) {
            if (lorebook.isDeleted()) {
                continue;
            }
            for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(lorebook)) {
                if (entry.isDeleted() || Objects.equals(entry.getId(), target.getId()) || !seen.add(entry.getId())) {
                    continue;
                }
                if (limit == EntryLimit.UP_TO_POSITION && position.compare(entry, target) > 0) {
                    continue;
                }
                entries.add(entry);
            }
        }
        entries.sort(position);
        return entries;
    }

    /** The lorebook and all its subbooks, transitively. */
    private List<Lorebook> withSubbooks(Lorebook root) throws Exception {
        List<Lorebook> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Lorebook> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook book = queue.remove();
            if (!visited.add(book.getId())) {
                continue;
            }
            result.add(book);
            queue.addAll(lorebookService.getSubbooks(book));
        }
        return result;
    }

    private String render(LorebookEntry entry, LorebookTemplateData templateData) {
        String payload = entry.getPayload();
        if (StringUtils.isBlank(payload)) {
            return payload;
        }
        try {
            return templateService.processTemplate(payload, "lorebookEntry", templateData);
        } catch (Exception e) {
            // a broken entry must not break the generation, use it as it is (the same as the story generation does)
            if (log != null) {
                log.warn("Failed to process template of lorebook entry '{}': {}", entry.getName(), e.getMessage());
            }
            return payload;
        }
    }

    /**
     * The text without the code fence the compaction prompt asks the model to wrap it in; the stripped text when there
     * is no fence.
     */
    public static String unfence(String text) {
        Matcher matcher = FENCED.matcher(text);
        return matcher.matches() ? matcher.group(1) : text.strip();
    }

    /**
     * Asks the model and waits for the whole answer (without its reasoning). The request is cancelled when the waiting
     * thread is interrupted, which is how a stopped command ends.
     *
     * @throws CancellationException when the request was cancelled
     */
    public String complete(ModelSelection model, List<LLMChatMessage> payload) throws Exception {
        CompletableFuture<String> answer = new CompletableFuture<>();
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open("lore-generator")) {
            token = inferenceServices.forAIOrThrow(model.ai()).infer(payload, model.protocol(),
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
        } catch (java.util.concurrent.ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }

}
