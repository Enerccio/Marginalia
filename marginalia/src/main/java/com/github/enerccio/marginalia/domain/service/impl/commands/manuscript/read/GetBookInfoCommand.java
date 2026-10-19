package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * {@code /get-book-info}: the name, the settings, the model, the totals and the tags of the book.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class GetBookInfoCommand extends ManuscriptReadSupport {

    static final String NAME = "get-book-info";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_GET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_GET_BOOK_INFO, llmDescription = L.DESC_COMMAND_GET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_GET_BOOK_INFO_RETURN))
    public void getBookInfo(CommandContext context) throws Exception {
        bookInfo(context);
    }

    private void bookInfo(CommandContext context) throws Exception {
        Book book = new Book(context);
        Manuscript manuscript = book.manuscript;
        JsonObject result = new JsonObject();
        result.addProperty("name", manuscript.getName());
        result.addProperty("description", manuscript.getDescription());
        result.addProperty("language", manuscript.getLanguage().name());
        result.addProperty("pov", manuscriptService.getPov(manuscript));
        result.addProperty("tense", manuscriptService.getTense(manuscript));
        result.addProperty("style", manuscriptService.getStyle(manuscript));
        result.addProperty("customTemplate", manuscript.getTemplate() != null && !manuscript.getTemplate().isBlank());
        AI ai = manuscript.getAi();
        result.addProperty("ai", ai == null ? null : ai.getName());
        result.addProperty("protocol", manuscript.getProtocol() == null ? null : manuscript.getProtocol().getName());

        ChatMessage leaf = manuscript.getActiveLeaf();
        JsonObject branch = new JsonObject();
        branch.addProperty("parts", chatMessageService.getBranchFromLeaf(leaf).size());
        branch.addProperty("words", chatMessageService.getBranchWordCount(leaf));
        branch.addProperty("tokens", chatMessageService.getBranchTokenCount(leaf));
        result.add("activeBranch", branch);
        JsonObject all = new JsonObject();
        all.addProperty("parts", chatMessageService.getAllMessages(manuscript).size());
        all.addProperty("words", chatMessageService.getTotalWordCount(manuscript));
        all.addProperty("tokens", chatMessageService.getTotalTokenCount(manuscript));
        result.add("allBranches", all);

        JsonArray tags = new JsonArray();
        for (Tag tag : tagRelationService.getTagsForObject(manuscript)) {
            tags.add(tag.getValue());
        }
        result.add("tags", tags);
        var strategy = manuscriptService.getBackupStrategy(manuscript);
        result.addProperty("backupStrategy", strategy == null ? null : strategy.name());
        result.addProperty("backupStrategyValue", manuscriptService.getBackupStrategyValue(manuscript));
        answer(context, result);
    }
}
