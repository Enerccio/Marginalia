package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.write;

import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * {@code /create-lore-entry name payload [lorebook [comment [enabled [order [keys [filteringMode [insertionMode [tags]]]]]]]]]},
 * {@code /update-lore-entry entry [name [payload ...]]}, {@code /delete-lore-entry entry}, {@code /move-lore-entry entry lorebook},
 * {@code /set-lore-entry-tags entry [add [remove [addNegative [removeNegative]]]]},
 * {@code /set-lore-entry-activation entry [preventRecursion [recursionOnly [activationContext [activationParts [activationSummaries [fromMessage [toMessage [condition]]]]]]]]}: the commands that change the entries of the lorebooks
 * of the book. Entries and lorebooks are given by their handles, see {@link LorebookWriteSupport}.
 */
public class LoreEntryWriteCommands extends LorebookWriteSupport {

    static final String CREATE_LORE_ENTRY = "create-lore-entry";
    static final String UPDATE_LORE_ENTRY = "update-lore-entry";
    static final String DELETE_LORE_ENTRY = "delete-lore-entry";
    static final String MOVE_LORE_ENTRY = "move-lore-entry";
    static final String SET_LORE_ENTRY_TAGS = "set-lore-entry-tags";
    static final String SET_LORE_ENTRY_ACTIVATION = "set-lore-entry-activation";

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntry(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), null, null, null, null, null, null, null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryLorebook(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), null, null, null, null, null, null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryComment(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), null, null, null, null, null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, defaultValue = "true", description = L.DESC_COMMAND_LORE_ENABLED_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryEnabled(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), context.get("enabled", Boolean.class), null, null, null, null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, defaultValue = "true", description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryOrder(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), null, null, null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, defaultValue = "true", description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryKeys(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), null, null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, defaultValue = "true", description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM),
                    @CommandParameter(name = "filteringMode", type = String.class, description = L.DESC_COMMAND_LORE_FILTERING_MODE_PARAM, allowed = {"KEYWORDS_OR", "KEYWORDS_AND", "TEXT", "REGEX"})}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryFilteringMode(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), ref(context, "filteringMode"), null, null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, defaultValue = "true", description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM),
                    @CommandParameter(name = "filteringMode", type = String.class, description = L.DESC_COMMAND_LORE_FILTERING_MODE_PARAM, allowed = {"KEYWORDS_OR", "KEYWORDS_AND", "TEXT", "REGEX"}),
                    @CommandParameter(name = "insertionMode", type = String.class, description = L.DESC_COMMAND_LORE_INSERTION_MODE_PARAM, allowed = {"IN_LORE_BLOCK", "BEFORE_USER_PROMPT"})}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryInsertionMode(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), ref(context, "filteringMode"), ref(context, "insertionMode"), null);
    }

    @Command(commandName = CREATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_CREATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_CREATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_TARGET_LOREBOOK_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, defaultValue = "true", description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM),
                    @CommandParameter(name = "filteringMode", type = String.class, description = L.DESC_COMMAND_LORE_FILTERING_MODE_PARAM, allowed = {"KEYWORDS_OR", "KEYWORDS_AND", "TEXT", "REGEX"}),
                    @CommandParameter(name = "insertionMode", type = String.class, description = L.DESC_COMMAND_LORE_INSERTION_MODE_PARAM, allowed = {"IN_LORE_BLOCK", "BEFORE_USER_PROMPT"}),
                    @CommandParameter(name = "tags", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LORE_ENTRY_RETURN))
    public void createLoreEntryTags(CommandContext context) throws Exception {
        createLoreEntry(context, context.get("name", String.class), context.get("payload", String.class), ref(context, "lorebook"), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), ref(context, "filteringMode"), ref(context, "insertionMode"), context.get("tags", String.class));
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntry(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), null, null, null, null, null, null, null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryName(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), null, null, null, null, null, null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryPayload(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), null, null, null, null, null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryComment(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), context.get("comment", String.class), null, null, null, null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, description = L.DESC_COMMAND_LORE_ENABLED_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryEnabled(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), context.get("comment", String.class), context.get("enabled", Boolean.class), null, null, null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryOrder(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), null, null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryKeys(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), null, null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM),
                    @CommandParameter(name = "filteringMode", type = String.class, description = L.DESC_COMMAND_LORE_FILTERING_MODE_PARAM, allowed = {"KEYWORDS_OR", "KEYWORDS_AND", "TEXT", "REGEX"})}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryFilteringMode(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), ref(context, "filteringMode"), null);
    }

    @Command(commandName = UPDATE_LORE_ENTRY, humanName = L.LABEL_COMMAND_UPDATE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_UPDATE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_UPDATE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LORE_NAME_PARAM),
                    @CommandParameter(name = "payload", type = String.class, description = L.DESC_COMMAND_LORE_PAYLOAD_PARAM),
                    @CommandParameter(name = "comment", type = String.class, description = L.DESC_COMMAND_LORE_COMMENT_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, description = L.DESC_COMMAND_LORE_ENABLED_PARAM),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, description = L.DESC_COMMAND_LORE_ORDER_PARAM),
                    @CommandParameter(name = "keys", type = String.class, description = L.DESC_COMMAND_LORE_KEYS_PARAM),
                    @CommandParameter(name = "filteringMode", type = String.class, description = L.DESC_COMMAND_LORE_FILTERING_MODE_PARAM, allowed = {"KEYWORDS_OR", "KEYWORDS_AND", "TEXT", "REGEX"}),
                    @CommandParameter(name = "insertionMode", type = String.class, description = L.DESC_COMMAND_LORE_INSERTION_MODE_PARAM, allowed = {"IN_LORE_BLOCK", "BEFORE_USER_PROMPT"})}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_UPDATE_LORE_ENTRY_RETURN))
    public void updateLoreEntryInsertionMode(CommandContext context) throws Exception {
        updateLoreEntry(context, ref(context, "entry"), context.get("name", String.class), context.get("payload", String.class), context.get("comment", String.class), context.get("enabled", Boolean.class), context.get("order", Integer.class), context.get("keys", String.class), ref(context, "filteringMode"), ref(context, "insertionMode"));
    }

    @Command(commandName = DELETE_LORE_ENTRY, humanName = L.LABEL_COMMAND_DELETE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_DELETE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_DELETE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_DELETE_LORE_ENTRY_RETURN))
    public void deleteLoreEntry(CommandContext context) throws Exception {
        deleteLoreEntry(context, ref(context, "entry"));
    }

    @Command(commandName = MOVE_LORE_ENTRY, humanName = L.LABEL_COMMAND_MOVE_LORE_ENTRY,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_MOVE_LORE_ENTRY, llmDescription = L.DESC_COMMAND_MOVE_LORE_ENTRY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_MOVE_LORE_ENTRY_RETURN))
    public void moveLoreEntry(CommandContext context) throws Exception {
        moveLoreEntry(context, ref(context, "entry"), ref(context, "lorebook"));
    }

    @Command(commandName = SET_LORE_ENTRY_TAGS, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_RETURN))
    public void setLoreEntryTags(CommandContext context) throws Exception {
        setLoreEntryTags(context, ref(context, "entry"), null, null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_TAGS, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "add", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_ADD_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_RETURN))
    public void setLoreEntryTagsAdd(CommandContext context) throws Exception {
        setLoreEntryTags(context, ref(context, "entry"), context.get("add", String.class), null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_TAGS, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "add", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_ADD_PARAM),
                    @CommandParameter(name = "remove", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_REMOVE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_RETURN))
    public void setLoreEntryTagsRemove(CommandContext context) throws Exception {
        setLoreEntryTags(context, ref(context, "entry"), context.get("add", String.class), context.get("remove", String.class), null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_TAGS, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "add", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_ADD_PARAM),
                    @CommandParameter(name = "remove", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_REMOVE_PARAM),
                    @CommandParameter(name = "addNegative", type = String.class, description = L.DESC_COMMAND_LORE_NEGATIVE_ADD_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_RETURN))
    public void setLoreEntryTagsAddNegative(CommandContext context) throws Exception {
        setLoreEntryTags(context, ref(context, "entry"), context.get("add", String.class), context.get("remove", String.class), context.get("addNegative", String.class), null);
    }

    @Command(commandName = SET_LORE_ENTRY_TAGS, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "add", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_ADD_PARAM),
                    @CommandParameter(name = "remove", type = String.class, description = L.DESC_COMMAND_LORE_TAGS_REMOVE_PARAM),
                    @CommandParameter(name = "addNegative", type = String.class, description = L.DESC_COMMAND_LORE_NEGATIVE_ADD_PARAM),
                    @CommandParameter(name = "removeNegative", type = String.class, description = L.DESC_COMMAND_LORE_NEGATIVE_REMOVE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_TAGS_RETURN))
    public void setLoreEntryTagsRemoveNegative(CommandContext context) throws Exception {
        setLoreEntryTags(context, ref(context, "entry"), context.get("add", String.class), context.get("remove", String.class), context.get("addNegative", String.class), context.get("removeNegative", String.class));
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationRecursion(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), null, null, null, null, null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationRecursionOnly(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), null, null, null, null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM),
                    @CommandParameter(name = "activationContext", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_CONTEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationContext(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), context.get("activationContext", String.class), null, null, null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM),
                    @CommandParameter(name = "activationContext", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_CONTEXT_PARAM),
                    @CommandParameter(name = "activationParts", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_PARTS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationParts(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), context.get("activationContext", String.class), context.get("activationParts", String.class), null, null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM),
                    @CommandParameter(name = "activationContext", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_CONTEXT_PARAM),
                    @CommandParameter(name = "activationParts", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_PARTS_PARAM),
                    @CommandParameter(name = "activationSummaries", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_SUMMARIES_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationSummaries(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), context.get("activationContext", String.class), context.get("activationParts", String.class), context.get("activationSummaries", String.class), null, null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM),
                    @CommandParameter(name = "activationContext", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_CONTEXT_PARAM),
                    @CommandParameter(name = "activationParts", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_PARTS_PARAM),
                    @CommandParameter(name = "activationSummaries", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_SUMMARIES_PARAM),
                    @CommandParameter(name = "fromMessage", type = String.class, description = L.DESC_COMMAND_LORE_FROM_MESSAGE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationFrom(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), context.get("activationContext", String.class), context.get("activationParts", String.class), context.get("activationSummaries", String.class), context.get("fromMessage", String.class), null, null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM),
                    @CommandParameter(name = "activationContext", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_CONTEXT_PARAM),
                    @CommandParameter(name = "activationParts", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_PARTS_PARAM),
                    @CommandParameter(name = "activationSummaries", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_SUMMARIES_PARAM),
                    @CommandParameter(name = "fromMessage", type = String.class, description = L.DESC_COMMAND_LORE_FROM_MESSAGE_PARAM),
                    @CommandParameter(name = "toMessage", type = String.class, description = L.DESC_COMMAND_LORE_TO_MESSAGE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationTo(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), context.get("activationContext", String.class), context.get("activationParts", String.class), context.get("activationSummaries", String.class), context.get("fromMessage", String.class), context.get("toMessage", String.class), null);
    }

    @Command(commandName = SET_LORE_ENTRY_ACTIVATION, humanName = L.LABEL_COMMAND_SET_LORE_ENTRY_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION, llmDescription = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "entry", type = String.class, description = L.DESC_COMMAND_LORE_ENTRY_HANDLE_PARAM),
                    @CommandParameter(name = "preventRecursion", type = String.class, description = L.DESC_COMMAND_LORE_PREVENT_RECURSION_PARAM),
                    @CommandParameter(name = "recursionOnly", type = String.class, description = L.DESC_COMMAND_LORE_RECURSION_ONLY_PARAM),
                    @CommandParameter(name = "activationContext", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_CONTEXT_PARAM),
                    @CommandParameter(name = "activationParts", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_PARTS_PARAM),
                    @CommandParameter(name = "activationSummaries", type = String.class, description = L.DESC_COMMAND_LORE_ACTIVATION_SUMMARIES_PARAM),
                    @CommandParameter(name = "fromMessage", type = String.class, description = L.DESC_COMMAND_LORE_FROM_MESSAGE_PARAM),
                    @CommandParameter(name = "toMessage", type = String.class, description = L.DESC_COMMAND_LORE_TO_MESSAGE_PARAM),
                    @CommandParameter(name = "condition", type = String.class, description = L.DESC_COMMAND_LORE_CONDITION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LORE_ENTRY_ACTIVATION_RETURN))
    public void setLoreEntryActivationCondition(CommandContext context) throws Exception {
        setLoreEntryActivation(context, ref(context, "entry"), context.get("preventRecursion", String.class), context.get("recursionOnly", String.class), context.get("activationContext", String.class), context.get("activationParts", String.class), context.get("activationSummaries", String.class), context.get("fromMessage", String.class), context.get("toMessage", String.class), context.get("condition", String.class));
    }

    private void setLoreEntryActivation(CommandContext context, String handle, String preventRecursion, String recursionOnly,
                                        String activationContext, String activationParts, String activationSummaries, String fromMessage,
                                        String toMessage, String condition) throws Exception {
        Lore lore = new Lore(new Book(context));
        LorebookEntry entry = entry(lore, handle).entry();
        boolean changed = false;
        if (given(preventRecursion)) {
            entry.setPreventRecursion(flag(preventRecursion));
            changed = true;
        }
        if (given(recursionOnly)) {
            entry.setRecursionOnly(flag(recursionOnly));
            changed = true;
        }
        if (given(activationSummaries)) {
            entry.setActivationSummaries(flag(activationSummaries));
            changed = true;
        }
        if (given(activationContext)) {
            entry.setActivationContext(mode(ActivationContext.class, activationContext));
            changed = true;
        }
        if (given(activationParts)) {
            entry.setActivationParts(number(activationParts));
            changed = true;
        }
        if (given(fromMessage)) {
            // 0 (or less) removes the limit, the setter turns it into null
            entry.setFromMessage(number(fromMessage));
            changed = true;
        }
        if (given(toMessage)) {
            entry.setToMessage(number(toMessage));
            changed = true;
        }
        if (condition != null) {
            // the one parameter where empty text is a value: it removes the condition
            entry.setActivationCondition(condition);
            changed = true;
        }
        if (!changed) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        if (entry.getFromMessage() != null && entry.getToMessage() != null && entry.getFromMessage() > entry.getToMessage()) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_LORE_RANGE_EMPTY),
                    entry.getFromMessage(), entry.getToMessage()));
        }
        LorebookEntryService.EntrySaved saved = lorebookEntryService.update(entry);
        JsonObject result = new JsonObject();
        result.addProperty("handle", handle);
        result.addProperty("name", entry.getName());
        addActivation(result, entry);
        result.add("problems", problems(saved.problems()));
        changed();
        answer(context, result);
    }

    private boolean flag(String text) {
        return switch (text.strip().toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN_MODE), text, List.of("true", "false")));
        };
    }

    private static boolean given(String value) {
        return value != null && !value.isBlank();
    }

    private int number(String text) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN_MODE), text, "0, 1, 2, ..."));
        }
    }

    private void createLoreEntry(CommandContext context, String name, String payload, String lorebook, String comment,
                                 Boolean enabled, Integer order, String keys, String filteringMode, String insertionMode,
                                 String tags) throws Exception {
        Book book = new Book(context);
        Lore lore = lorebook == null ? loreToAddTo(book) : new Lore(book);
        Lorebook target = lorebook == null ? lore.book(null) : book(lore, lorebook);
        LorebookEntry fields = new LorebookEntry();
        fields.setName(name);
        fields.setPayload(payload == null ? "" : payload);
        // models write keys as a list ("Name, Lastname"), so that is the default of an entry they create
        fields.setFilteringMode(FilteringMode.KEYWORDS_OR);
        fill(fields, comment, enabled, order, keys, filteringMode, insertionMode);
        LorebookEntryService.EntrySaved saved = lorebookEntryService.create(target, fields);
        applyTags(saved.entry(), tags, null, null, null);
        answer(context, saved);
    }

    private void updateLoreEntry(CommandContext context, String handle, String name, String payload, String comment,
                                 Boolean enabled, Integer order, String keys, String filteringMode, String insertionMode) throws Exception {
        Lore lore = new Lore(new Book(context));
        LorebookEntry entry = entry(lore, handle).entry();
        if (name == null && payload == null && comment == null && enabled == null && order == null && keys == null
                && filteringMode == null && insertionMode == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        if (name != null) {
            entry.setName(name);
        }
        if (payload != null) {
            entry.setPayload(payload);
        }
        fill(entry, comment, enabled, order, keys, filteringMode, insertionMode);
        answer(context, lorebookEntryService.update(entry));
    }

    private void deleteLoreEntry(CommandContext context, String handle) throws Exception {
        Lore lore = new Lore(new Book(context));
        Lore.Found found = entry(lore, handle);
        if (!confirmed(context, String.format(loc.getValue(L.MSG_COMMAND_DELETE_LORE_ENTRY_CONFIRM), found.entry().getName()))) {
            return;
        }
        lorebookEntryService.deleteEntry(found.book(), found.entry());
        JsonObject result = new JsonObject();
        result.addProperty("deleted", handle);
        result.addProperty("name", found.entry().getName());
        changed();
        answer(context, result);
    }

    private void moveLoreEntry(CommandContext context, String handle, String lorebook) throws Exception {
        Lore lore = new Lore(new Book(context));
        LorebookEntry entry = entry(lore, handle).entry();
        Lorebook target = book(lore, lorebook);
        answer(context, new LorebookEntryService.EntrySaved(lorebookEntryService.move(entry, target), java.util.List.of()));
    }

    private void setLoreEntryTags(CommandContext context, String handle, String add, String remove, String addNegative,
                                  String removeNegative) throws Exception {
        Lore lore = new Lore(new Book(context));
        LorebookEntry entry = entry(lore, handle).entry();
        if (add == null && remove == null && addNegative == null && removeNegative == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        applyTags(entry, add, remove, addNegative, removeNegative);
        JsonObject result = new JsonObject();
        result.addProperty("handle", handle);
        result.add("tags", names(tagRelationService.getTagsForObject(entry)));
        result.add("negativeTags", names(tagRelationService.getTagsForObject(entry, true)));
        changed();
        answer(context, result);
    }

    private void fill(LorebookEntry entry, String comment, Boolean enabled, Integer order, String keys, String filteringMode,
                      String insertionMode) {
        if (comment != null) {
            entry.setComment(comment);
        }
        if (enabled != null) {
            entry.setEnabled(enabled);
        }
        if (order != null) {
            entry.setOrder(order);
        }
        if (keys != null) {
            entry.setFiltering(keys);
        }
        if (filteringMode != null) {
            entry.setFilteringMode(mode(FilteringMode.class, filteringMode));
        }
        if (insertionMode != null) {
            entry.setInsertionMode(mode(InsertionMode.class, insertionMode));
        }
    }

    private <E extends Enum<E>> E mode(Class<E> type, String text) {
        for (E value : type.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(text.strip())) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN_MODE), text,
                java.util.Arrays.stream(type.getEnumConstants()).map(Enum::name).toList()));
    }

    private void applyTags(LorebookEntry entry, String add, String remove, String addNegative, String removeNegative) throws Exception {
        retag(entry, remove, false, false);
        retag(entry, removeNegative, true, false);
        retag(entry, add, false, true);
        retag(entry, addNegative, true, true);
    }

    private void retag(LorebookEntry entry, String values, boolean negative, boolean add) throws Exception {
        List<Tag> current = tagRelationService.getTagsForObject(entry, negative);
        for (String value : list(values)) {
            Tag existing = current.stream().filter(tag -> tag.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
            if (add && existing == null) {
                tagRelationService.createRelation(tagService.getOrCreateForUser(value), entry, negative);
            } else if (!add && existing != null) {
                tagRelationService.removeRelation(existing, entry, negative);
            }
        }
    }

    /**
     * Tells about the entry that was saved: where it is, its handle and the problems of its fields.
     */
    private void answer(CommandContext context, LorebookEntryService.EntrySaved saved) throws Exception {
        Lore lore = new Lore(new Book(context));
        Lore.Found found = lore.find(null, saved.entry().getUuid());
        JsonObject result = new JsonObject();
        result.addProperty("handle", found.entry().getUuid());
        result.addProperty("lorebook", lore.number(found.book()));
        result.addProperty("entry", found.number());
        result.addProperty("name", found.entry().getName());
        result.add("problems", problems(saved.problems()));
        changed();
        answer(context, result);
    }
}
