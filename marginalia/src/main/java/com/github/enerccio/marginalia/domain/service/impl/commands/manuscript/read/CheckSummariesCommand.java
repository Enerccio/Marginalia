package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * {@code /check-summaries [branch]}: the summaries that are outdated and the edited parts that made them so.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class CheckSummariesCommand extends ManuscriptReadSupport {

    static final String NAME = "check-summaries";

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_CHECK_SUMMARIES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CHECK_SUMMARIES, llmDescription = L.DESC_COMMAND_CHECK_SUMMARIES_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CHECK_SUMMARIES_RETURN))
    public void checkSummaries(CommandContext context) throws Exception {
        checkSummaries(context, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_CHECK_SUMMARIES,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_CHECK_SUMMARIES, llmDescription = L.DESC_COMMAND_CHECK_SUMMARIES_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_CHECK_SUMMARIES_RETURN))
    public void checkSummariesBranch(CommandContext context) throws Exception {
        checkSummaries(context, context.get("branch", Integer.class));
    }

    private void checkSummaries(CommandContext context, Integer branch) throws Exception {
        Book book = new Book();
        List<ChatMessage> path = book.path(branch);
        List<ChatMessage> newestFirst = new ArrayList<>(path);
        Collections.reverse(newestFirst);
        Map<Long, Integer> positions = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            positions.put(path.get(i).getId(), i + 1);
        }

        // oldest first, a block covers the parts after the head of the older block up to its own head
        List<SummaryBlock> blocks = new ArrayList<>(summaryService.collectBlocks(newestFirst));
        Collections.reverse(blocks);
        JsonArray outdated = new JsonArray();
        int previous = 0;
        for (SummaryBlock block : blocks) {
            int head = positions.get(block.head().getId());
            if (!block.isValid()) {
                JsonObject row = new JsonObject();
                row.addProperty("position", head);
                row.addProperty("covers", (previous + 1) + "-" + head);
                JsonArray edited = new JsonArray();
                for (int i = previous; i < head; i++) {
                    if (path.get(i).isEdited()) {
                        edited.add(i + 1);
                    }
                }
                row.add("editedParts", edited);
                outdated.add(row);
            }
            previous = head;
        }
        JsonObject result = new JsonObject();
        result.addProperty("summaries", blocks.size());
        result.addProperty("outdated", outdated.size());
        result.add("outdatedSummaries", outdated);
        answer(context, result);
    }
}
