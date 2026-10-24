package com.github.enerccio.marginalia.extensions.stconverter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.extensions.stconverter.STConverterLog;
import com.github.enerccio.marginalia.extensions.stconverter.model.*;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Keeps the chat and the turn details with the book and asks the model for the turn details.
 */
@Configurable
public class ConverterService {

    /** The model and the protocol the wand runs with. The protocol is optional. */
    public record ModelSelection(AI ai, Protocol protocol) {
    }

    @Autowired
    private ManuscriptService manuscriptService;

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
    private TokenizerService tokenizerService;

    @Autowired
    private InferenceServices inferenceServices;

    private final Gson gson = new Gson();

    // --- what the book keeps ---

    /**
     * The data of the book as it is stored now - the story view keeps the book it was opened with, the data may have been
     * saved since.
     */
    public ConverterData load(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        JsonObject attrs = (current != null ? current : manuscript).getAttributes();
        ConverterData data = null;
        if (attrs != null && attrs.has(ConverterData.KEY)) {
            data = gson.fromJson(attrs.get(ConverterData.KEY), ConverterData.class);
        }
        return data != null ? data : new ConverterData();
    }

    public void save(Manuscript manuscript, ConverterData data) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        if (current == null) {
            current = manuscript;
        }
        JsonObject attrs = current.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            current.setAttributes(attrs);
        }
        attrs.add(ConverterData.KEY, gson.toJsonTree(data));
        manuscriptService.save(current);
    }

    // --- the chat ---

    /**
     * Reads an uploaded SillyTavern chat ({@code .jsonl}) into the data of the book, replacing the chat and the turn
     * details, which describe the chat. The branches of the chat are not followed: every message is the swipe that was
     * shown; hidden (system) messages and empty ones are left out.
     *
     * @return the number of messages
     * @throws IllegalArgumentException when the file is not a SillyTavern chat or has no messages
     */
    public int importChat(ConverterData data, byte[] file, String fileName) {
        String text = new String(file, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        List<ChatLine> lines = toLines(SillyTavernChats.parse(text, fileName));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("The chat has no messages");
        }
        data.setFileName(StringUtils.defaultIfBlank(fileName, "chat.jsonl"));
        data.setChat(lines);
        data.setTurns(new ArrayList<>());
        return lines.size();
    }

    /**
     * The messages of the chat that have text, the swipe that was shown.
     */
    public static List<ChatLine> toLines(SillyTavernChats.Chat chat) {
        List<ChatLine> lines = new ArrayList<>();
        for (SillyTavernChats.Message message : chat.messages()) {
            String text = message.shown().text();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            String name = message.name() != null ? message.name()
                    : message.user() ? "User" : StringUtils.defaultIfBlank(chat.characterName(), "Character");
            lines.add(new ChatLine(name, message.user(), text.strip()));
        }
        return lines;
    }

    /**
     * The messages {@code from} to {@code to} (from 1, inclusive) as the prompt shows them, each under a heading with its
     * number and the name of the speaker.
     */
    public static String chatText(List<ChatLine> chat, int from, int to) {
        StringBuilder text = new StringBuilder();
        for (int i = Math.max(1, from); i <= Math.min(to, chat.size()); i++) {
            ChatLine line = chat.get(i - 1);
            text.append("--- Message ").append(i).append(" (").append(line.getName()).append(") ---\n")
                    .append(line.getText().strip()).append("\n\n");
        }
        return text.toString().stripTrailing();
    }

    // --- the model ---

    /**
     * The model and protocol the wand starts with: the ones chosen before, else those of the book, else the defaults of the
     * user.
     *
     * @return null when there is none
     */
    public ModelSelection resolveModel(Manuscript manuscript, ConverterData data) throws Exception {
        AI ai = data.getAiId() != null ? aiService.find(data.getAiId()) : null;
        if (ai != null && !ai.isDeleted()) {
            Protocol protocol = data.getProtocolId() != null ? protocolService.find(data.getProtocolId()) : null;
            return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
        }
        Manuscript current = manuscriptService.find(manuscript);
        Manuscript book = current != null ? current : manuscript;
        ai = book.getAi();
        if (ai != null && !ai.isDeleted()) {
            Protocol protocol = book.getProtocol();
            return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
        }
        return null;
    }

    /** The approximate number of tokens of the text, by the tokenizer of the model when there is one. */
    public long countTokens(AI ai, String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        try {
            InferenceService service = ai != null ? inferenceServices.forAI(ai) : null;
            return service != null ? service.countTokensApprox(text) : tokenizerService.countTokensApprox(text);
        } catch (Exception e) {
            // a rough estimate does for a counter
            return text.length() / 4;
        }
    }

    // --- the prompt ---

    /** The template the prompt starts with: the user's or {@link Defaults#PROMPT}. */
    public String promptTemplate(ConverterData data) {
        return StringUtils.defaultIfBlank(data.getPrompt(), Defaults.PROMPT);
    }

    /**
     * The prompt for the messages {@code from} to {@code to}: the template with the lore of the book and the messages put
     * in.
     *
     * @throws Exception when the template is not valid
     */
    public String buildPrompt(Manuscript manuscript, ConverterData data, String template, int from, int to) throws Exception {
        ConverterTemplateData templateData = new ConverterTemplateData();
        templateData.setBackgroundLore(buildBackgroundLore(manuscript));
        templateData.setChat(chatText(data.getChat(), from, to));
        templateData.setFirstMessage(from);
        templateData.setLastMessage(to);
        return templateService.processTemplate(template, "stConverter", templateData);
    }

    /** The prompt as the system message, with a short user turn some providers require. */
    public List<LLMChatMessage> toPayload(String systemPrompt) {
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, systemPrompt));
        payload.add(LLMChatMessage.of(LLMRole.USER, "[ Start ]"));
        return payload;
    }

    /** The JSON schema the model is asked to answer in. */
    public JsonNode schema() {
        return StructuredOutputs.schemaOf(GeneratedTurns.class);
    }

    /**
     * The entries of the book's lorebook and of its sub lorebooks (deleted ones are skipped) as the story would see them,
     * sorted by their order. Which entries are used does not depend on their enabled flags, tags or filtering.
     */
    public String buildBackgroundLore(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        if (current == null) {
            current = manuscript;
        }
        Lorebook root = current.getLorebook() != null ? lorebookService.find(current.getLorebook()) : null;
        if (root == null) {
            return "(no lorebook)";
        }

        List<LorebookEntry> entries = new ArrayList<>();
        Set<Long> processed = new HashSet<>();
        Queue<Lorebook> toProcess = new ArrayDeque<>();
        toProcess.add(root);
        while (!toProcess.isEmpty()) {
            Lorebook lorebook = toProcess.remove();
            if (lorebook.isDeleted() || !processed.add(lorebook.getId())) {
                continue;
            }
            lorebookEntryService.getEntriesForLorebook(lorebook).stream()
                    .filter(entry -> !entry.isDeleted()).forEach(entries::add);
            toProcess.addAll(lorebookService.getSubbooks(lorebook));
        }
        // a stable sort
        entries.sort(Comparator.comparingInt(LorebookEntry::getOrder));

        // one template data for all entries, like the generation does: variables set in an entry are visible in the next ones
        LorebookTemplateData templateData = new LorebookTemplateData();
        StringBuilder lore = new StringBuilder();
        for (LorebookEntry entry : entries) {
            String payload = render(entry, templateData);
            if (StringUtils.isNotBlank(payload)) {
                lore.append(payload.trim()).append("\n\n");
            }
        }
        return lore.length() == 0 ? "(the lorebook is empty)" : lore.toString().stripTrailing();
    }

    private String render(LorebookEntry entry, LorebookTemplateData templateData) {
        String payload = entry.getPayload();
        if (StringUtils.isBlank(payload)) {
            return payload;
        }
        try {
            return templateService.processTemplate(payload, "lorebookEntry", templateData);
        } catch (Exception e) {
            // a broken entry must not break the wand, use it as it is (the same as the story generation does)
            STConverterLog.get().warn("Failed to process template of lorebook entry '{}': {}", entry.getName(), e.getMessage());
            return payload;
        }
    }

    // --- the answer ---

    /**
     * Checks the complete answer of the model and reads it.
     *
     * @return the turns in the order of the chat, the numbers of the messages fixed to the range
     * @throws IllegalStateException when the answer does not match the schema or has no usable turn
     */
    public List<TurnItem> readAnswer(String answer, int from, int to) throws Exception {
        SchemaCheck check = StructuredOutputs.validate(schema(), answer);
        if (!check.valid()) {
            throw new IllegalStateException("The model's answer does not match what was asked for: " + String.join("; ", check.errors()));
        }
        List<TurnItem> turns = toTurns(StructuredOutputs.parse(answer, GeneratedTurns.class), from, to);
        if (turns.isEmpty()) {
            throw new IllegalStateException("The model's answer has no turn for the messages " + from + " to " + to);
        }
        return turns;
    }

    /**
     * The turns of the answer as turn items: the numbers of the messages are put in the range {@code from} to {@code to}, a
     * turn that is wholly outside of it or is the wrong way round is left out, and the turns are in the order of the chat.
     */
    public static List<TurnItem> toTurns(GeneratedTurns generated, int from, int to) {
        List<TurnItem> turns = new ArrayList<>();
        for (GeneratedTurn turn : generated.turns()) {
            int first = Math.max(from, turn.firstMessage());
            int last = Math.min(to, turn.lastMessage());
            if (first > last) {
                continue;
            }
            turns.add(new TurnItem(first, last, turn.sceneSetting().strip(), turn.povCharacter().strip(),
                    turn.presentCharacters().strip(), turn.instructions().strip()));
        }
        turns.sort(Comparator.comparingInt(TurnItem::getFirstMessage).thenComparingInt(TurnItem::getLastMessage));
        return turns;
    }

    // --- turn boundaries ---

    /**
     * Moves the range to the boundaries of the turns. A turn ends with a message of the character (the messages of the user
     * before it are its instructions), so the end of the range snaps back to the last message of the character at or before
     * it, and the start back to the first message after the one before. The end of the chat is a boundary too. When nothing
     * is left between them (a range inside the instructions of a turn) the end goes forward to the end of that turn.
     *
     * @param from the first message of the range, from 1
     * @param to   the last message of the range, from 1
     * @return {first, last}, from 1
     */
    public static int[] alignRange(List<ChatLine> chat, int from, int to) {
        int size = chat.size();
        if (size == 0) {
            return new int[]{1, 1};
        }
        int first = alignStart(chat, Math.max(1, Math.min(from, size)));
        int last = alignEnd(chat, Math.max(first, Math.min(to, size)));
        if (last < first) {
            last = turnEnd(chat, Math.max(1, Math.min(to, size)));
        }
        return new int[]{first, last};
    }

    /** The first message of the turn the message {@code number} (from 1) is in. */
    static int alignStart(List<ChatLine> chat, int number) {
        int start = number;
        while (start > 1 && chat.get(start - 2).isUser()) {
            start--;
        }
        return start;
    }

    /**
     * The last message of the character at or before the message {@code number} (from 1); the end of the chat stays; 0 when
     * there is none.
     */
    static int alignEnd(List<ChatLine> chat, int number) {
        if (number >= chat.size()) {
            return chat.size();
        }
        int end = number;
        while (end > 0 && chat.get(end - 1).isUser()) {
            end--;
        }
        return end;
    }

    /** The first message of the character at or after the message {@code number} (from 1); the end of the chat when none. */
    static int turnEnd(List<ChatLine> chat, int number) {
        int end = number;
        while (end < chat.size() && chat.get(end - 1).isUser()) {
            end++;
        }
        return end;
    }

    /**
     * Splits the messages {@code from} to {@code to} (from 1) into blocks of about {@code size} messages for the auto mode,
     * every block starts after the one before. With {@code align} a block ends at the end of a turn (the last message of the
     * character within the size, or when the turn is longer than that, the end of the turn), so no turn is cut in two.
     *
     * @return {first, last} of every block, from 1, never empty for a valid range
     */
    public static List<int[]> splitBlocks(List<ChatLine> chat, int from, int to, int size, boolean align) {
        List<int[]> blocks = new ArrayList<>();
        int last = Math.min(to, chat.size());
        int step = Math.max(1, size);
        int start = Math.max(1, from);
        while (start <= last) {
            int end = Math.min(last, start + step - 1);
            if (align && end < last) {
                int aligned = alignEnd(chat, end);
                end = aligned >= start ? aligned : Math.min(last, turnEnd(chat, end));
            }
            blocks.add(new int[]{start, end});
            start = end + 1;
        }
        return blocks;
    }

    /**
     * The next row that is not checked, going down from the row after {@code after} and starting again from the top when
     * there is none below.
     *
     * @param after   the number (from 1) of the row the author was at, 0 for none
     * @param checked whether the row with this index (from 0) is checked
     * @return the index (from 0) of the row, -1 when all are checked
     */
    public static int nextUnchecked(int size, int after, java.util.function.IntPredicate checked) {
        for (int step = 0; step < size; step++) {
            int index = (Math.max(0, after) + step) % size;
            if (!checked.test(index)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Puts the new turns into the turn details: the turns the author has not reviewed that cover a message of the range
     * {@code from} to {@code to} are replaced by the new ones, the reviewed ones stay (the author has written them already),
     * and all are in the order of the chat.
     *
     * @return how many turns were replaced
     */
    public static int mergeTurns(List<TurnItem> existing, List<TurnItem> added, int from, int to) {
        int before = existing.size();
        existing.removeIf(turn -> !turn.isReviewed() && turn.getFirstMessage() <= to && turn.getLastMessage() >= from);
        int replaced = before - existing.size();
        existing.addAll(added);
        existing.sort(Comparator.comparingInt(TurnItem::getFirstMessage).thenComparingInt(TurnItem::getLastMessage));
        return replaced;
    }
}
