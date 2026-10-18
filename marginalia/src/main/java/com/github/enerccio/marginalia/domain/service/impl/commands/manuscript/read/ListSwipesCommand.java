package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /list-swipes part [branch]}: the alternatives of a part, with the branch that shows each of them.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ListSwipesCommand extends ManuscriptReadSupport {

    static final String NAME = "list-swipes";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_SWIPES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_SWIPES, llmDescription = L.DESC_COMMAND_LIST_SWIPES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_SWIPES_RETURN))
    public void listSwipes(CommandContext context) throws Exception {
        listSwipes(context, context.get("part", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_LIST_SWIPES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_LIST_SWIPES, llmDescription = L.DESC_COMMAND_LIST_SWIPES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_LIST_SWIPES_RETURN))
    public void listSwipesBranch(CommandContext context) throws Exception {
        listSwipes(context, context.get("part", Integer.class), context.get("branch", Integer.class));
    }

    private void listSwipes(CommandContext context, Integer position, Integer branch) throws Exception {
        Book book = new Book();
        List<ChatMessage> path = book.path(branch);
        ChatMessage message = book.part(path, position);
        StoryTree tree = book.tree();

        List<ChatMessage> swipes = new ArrayList<>(chatMessageService.getSwipesForMessage(message));
        swipes.removeIf(ChatMessage::isDeleted);
        swipes.sort(StoryTree.CREATED);
        JsonArray list = new JsonArray();
        int number = 0;
        for (ChatMessage swipe : swipes) {
            number++;
            JsonObject row = new JsonObject();
            row.addProperty("swipe", number);
            row.addProperty("shown", swipe.getId().equals(message.getId()));
            row.addProperty("words", swipe.getWordCount());
            row.addProperty("edited", swipe.isEdited());
            row.addProperty("preview", preview(swipe.getResponse(), PREVIEW_LENGTH));
            // the branch that shows the swipe, to read it with get-part
            ChatMessage known = tree.byId.get(swipe.getId());
            if (known != null) {
                row.addProperty("branch", tree.branchOf(known));
            }
            list.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("position", position);
        result.add("swipes", list);
        answer(context, result);
    }
}
