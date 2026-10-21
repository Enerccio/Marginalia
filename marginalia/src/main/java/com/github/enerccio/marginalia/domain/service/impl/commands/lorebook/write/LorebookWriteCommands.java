package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.write;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonObject;

/**
 * {@code /create-lorebook name [parent]}, {@code /set-lorebook lorebook [name [enabled]]}, {@code /add-subbook lorebook subbook},
 * {@code /remove-subbook lorebook subbook}, {@code /delete-lorebook lorebook}: the commands that change the lorebooks of the book and how
 * they contain each other. Lorebooks are given by their handles, see {@link LorebookWriteSupport}.
 */
public class LorebookWriteCommands extends LorebookWriteSupport {

    static final String CREATE_LOREBOOK = "create-lorebook";
    static final String SET_LOREBOOK = "set-lorebook";
    static final String ADD_SUBBOOK = "add-subbook";
    static final String REMOVE_SUBBOOK = "remove-subbook";
    static final String DELETE_LOREBOOK = "delete-lorebook";

    @Command(commandName = CREATE_LOREBOOK, humanName = L.LABEL_COMMAND_CREATE_LOREBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LOREBOOK, llmDescription = L.DESC_COMMAND_CREATE_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LOREBOOK_NAME_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LOREBOOK_RETURN))
    public void createLorebook(CommandContext context) throws Exception {
        createLorebook(context, context.get("name", String.class), null);
    }

    @Command(commandName = CREATE_LOREBOOK, humanName = L.LABEL_COMMAND_CREATE_LOREBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CREATE_LOREBOOK, llmDescription = L.DESC_COMMAND_CREATE_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LOREBOOK_NAME_PARAM),
                    @CommandParameter(name = "parent", type = String.class, description = L.DESC_COMMAND_PARENT_LOREBOOK_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CREATE_LOREBOOK_RETURN))
    public void createLorebookParent(CommandContext context) throws Exception {
        createLorebook(context, context.get("name", String.class), ref(context, "parent"));
    }

    @Command(commandName = SET_LOREBOOK, humanName = L.LABEL_COMMAND_SET_LOREBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LOREBOOK, llmDescription = L.DESC_COMMAND_SET_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LOREBOOK_RETURN))
    public void setLorebook(CommandContext context) throws Exception {
        setLorebook(context, ref(context, "lorebook"), null, null);
    }

    @Command(commandName = SET_LOREBOOK, humanName = L.LABEL_COMMAND_SET_LOREBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LOREBOOK, llmDescription = L.DESC_COMMAND_SET_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LOREBOOK_NAME_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LOREBOOK_RETURN))
    public void setLorebookName(CommandContext context) throws Exception {
        setLorebook(context, ref(context, "lorebook"), context.get("name", String.class), null);
    }

    @Command(commandName = SET_LOREBOOK, humanName = L.LABEL_COMMAND_SET_LOREBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_LOREBOOK, llmDescription = L.DESC_COMMAND_SET_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM),
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_LOREBOOK_NAME_PARAM),
                    @CommandParameter(name = "enabled", type = Boolean.class, optional = true, description = L.DESC_COMMAND_LOREBOOK_ENABLED_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_LOREBOOK_RETURN))
    public void setLorebookEnabled(CommandContext context) throws Exception {
        setLorebook(context, ref(context, "lorebook"), context.get("name", String.class), context.get("enabled", Boolean.class));
    }

    @Command(commandName = ADD_SUBBOOK, humanName = L.LABEL_COMMAND_ADD_SUBBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_ADD_SUBBOOK, llmDescription = L.DESC_COMMAND_ADD_SUBBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM),
                    @CommandParameter(name = "subbook", type = String.class, description = L.DESC_COMMAND_SUBBOOK_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_ADD_SUBBOOK_RETURN))
    public void addSubbook(CommandContext context) throws Exception {
        addSubbook(context, ref(context, "lorebook"), ref(context, "subbook"));
    }

    @Command(commandName = REMOVE_SUBBOOK, humanName = L.LABEL_COMMAND_REMOVE_SUBBOOK,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_REMOVE_SUBBOOK, llmDescription = L.DESC_COMMAND_REMOVE_SUBBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM),
                    @CommandParameter(name = "subbook", type = String.class, description = L.DESC_COMMAND_SUBBOOK_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REMOVE_SUBBOOK_RETURN))
    public void removeSubbook(CommandContext context) throws Exception {
        removeSubbook(context, ref(context, "lorebook"), ref(context, "subbook"));
    }

    @Command(commandName = DELETE_LOREBOOK, humanName = L.LABEL_COMMAND_DELETE_LOREBOOK,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_DELETE_LOREBOOK, llmDescription = L.DESC_COMMAND_DELETE_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "lorebook", type = String.class, description = L.DESC_COMMAND_LOREBOOK_HANDLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_DELETE_LOREBOOK_RETURN))
    public void deleteLorebook(CommandContext context) throws Exception {
        deleteLorebook(context, ref(context, "lorebook"));
    }

    private void createLorebook(CommandContext context, String name, String parent) throws Exception {
        Lore lore = loreToAddTo(new Book(context));
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        Lorebook created = lorebookService.createSubbook(parent == null ? lore.book(null) : book(lore, parent), name.strip());
        answerBook(context, created);
    }

    private void setLorebook(CommandContext context, String handle, String name, Boolean enabled) throws Exception {
        Lore lore = new Lore(new Book(context));
        Lorebook book = book(lore, handle);
        if (name == null && enabled == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        if (name != null && !name.isBlank()) {
            book.setName(name.strip());
        }
        if (enabled != null) {
            book.setEnabled(enabled);
        }
        answerBook(context, lorebookService.save(book));
    }

    private void addSubbook(CommandContext context, String handle, String subbook) throws Exception {
        Lore lore = new Lore(new Book(context));
        Lorebook parent = book(lore, handle);
        // a lorebook that was deleted or taken out of its parent can still be named, and is brought back by being added again
        Lorebook sub = lore.bookOrUnused(subbook);
        if (sub.isDeleted()) {
            sub = lorebookService.restore(sub);
        }
        answerBook(context, lorebookService.addSubbook(parent, sub));
    }

    private void removeSubbook(CommandContext context, String handle, String subbook) throws Exception {
        Lore lore = new Lore(new Book(context));
        answerBook(context, lorebookService.removeSubbook(book(lore, handle), book(lore, subbook)));
    }

    private void deleteLorebook(CommandContext context, String handle) throws Exception {
        Lore lore = new Lore(new Book(context));
        Lorebook book = book(lore, handle);
        if (book.getId().equals(lore.book(null).getId())) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_CANNOT_DELETE_ROOT_LOREBOOK));
        }
        int entries = lore.entries(book).size();
        if (!confirmed(context, String.format(loc.getValue(L.MSG_COMMAND_DELETE_LOREBOOK_CONFIRM), book.getName(), entries))) {
            return;
        }
        for (Lorebook parent : lore.books) {
            if (lorebookService.getSubbooks(parent).stream().anyMatch(sub -> sub.getId().equals(book.getId()))) {
                lorebookService.removeSubbook(parent, book);
            }
        }
        lorebookService.delete(book, false);
        JsonObject result = new JsonObject();
        result.addProperty("deleted", handle);
        result.addProperty("name", book.getName());
        result.addProperty("entries", entries);
        changed();
        answer(context, result);
    }

    /**
     * Tells about a lorebook: its handle, where it is, and its subbooks.
     */
    private void answerBook(CommandContext context, Lorebook changed) throws Exception {
        Lore lore = new Lore(new Book(context));
        JsonObject result = new JsonObject();
        result.addProperty("handle", changed.getUuid());
        Lorebook book = lore.books.stream().filter(b -> b.getId().equals(changed.getId())).findFirst().orElse(null);
        if (book != null) {
            result.addProperty("lorebook", lore.number(book));
            result.add("subbooks", lore.subbooks(book));
        }
        result.addProperty("name", changed.getName());
        result.addProperty("enabled", changed.isEnabled());
        changed();
        answer(context, result);
    }
}
