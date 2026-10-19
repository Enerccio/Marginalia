package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * {@code /list-branches [offset [limit]]}: the branches of the book, numbered, with where they part from the others.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ListBranchesCommand extends ManuscriptReadSupport {

    static final String NAME = "list-branches";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_BRANCHES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_BRANCHES, llmDescription = L.DESC_COMMAND_LIST_BRANCHES_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_BRANCHES_RETURN))
    public void listBranches(CommandContext context) throws Exception {
        listBranches(context, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_BRANCHES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_BRANCHES, llmDescription = L.DESC_COMMAND_LIST_BRANCHES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_BRANCHES_RETURN))
    public void listBranchesOffset(CommandContext context) throws Exception {
        listBranches(context, context.get("offset", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_BRANCHES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_BRANCHES, llmDescription = L.DESC_COMMAND_LIST_BRANCHES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "offset", type = Integer.class, description = L.DESC_COMMAND_OFFSET_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_BRANCHES_RETURN))
    public void listBranchesLimit(CommandContext context) throws Exception {
        listBranches(context, context.get("offset", Integer.class), context.get("limit", Integer.class));
    }

    private void listBranches(CommandContext context, Integer offset, Integer limit) throws Exception {
        Book book = new Book(context);
        StoryTree tree = book.tree();
        int from = Math.min(Math.max(offset == null ? 0 : offset, 0), tree.leaves.size());
        int to = Math.min(tree.leaves.size(), from + limit(limit));

        JsonArray branches = new JsonArray();
        for (int i = from; i < to; i++) {
            ChatMessage leaf = tree.leaves.get(i);
            List<ChatMessage> path = tree.pathTo(leaf);
            JsonObject row = new JsonObject();
            row.addProperty("branch", i + 1);
            row.addProperty("handle", leaf.getUuid());
            row.addProperty("active", leaf.getId().equals(tree.activeLeafId));
            row.addProperty("parts", path.size());
            row.addProperty("words", path.stream().mapToInt(ChatMessage::getWordCount).sum());
            // the last part that other branches share: the parts after it are the own parts of the branch
            int fork = 0;
            for (int p = 0; p < path.size() - 1; p++) {
                if (tree.children.get(path.get(p).getId()).size() > 1) {
                    fork = p + 1;
                }
            }
            row.addProperty("forkPosition", fork);
            if (fork > 0) {
                row.addProperty("forkHandle", path.get(fork - 1).getUuid());
            }
            row.addProperty("created", leaf.getCreation() == null ? null : leaf.getCreation().toInstant().toString());
            row.addProperty("endPreview", preview(leaf.getResponse(), PREVIEW_LENGTH));
            branches.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("total", tree.leaves.size());
        result.addProperty("offset", from);
        result.add("branches", branches);
        answer(context, result);
    }
}
