package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.TextDiff;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * {@code /compare-parts a b [branchA [branchB]]}: the sentences that differ between two parts (two swipes, for one), {@code b} 0 compares part {@code a} with the text the model produced first.
 * Changes nothing; the answer is JSON text, see {@link ManuscriptReadSupport}.
 */
public class ComparePartsCommand extends ManuscriptReadSupport {

    static final String NAME = "compare-parts";

    /** The most changes that are shown, and the length of their text. */
    static final int MAX_DIFF_LINES = 200;
    static final int MAX_DIFF_LINE_LENGTH = 300;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_COMPARE_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_COMPARE_PARTS, llmDescription = L.DESC_COMMAND_COMPARE_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "a", type = Integer.class, description = L.DESC_COMMAND_PART_A_PARAM),
                    @CommandParameter(name = "b", type = Integer.class, description = L.DESC_COMMAND_PART_B_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_COMPARE_PARTS_RETURN))
    public void compareParts(CommandContext context) throws Exception {
        comparePartsImpl(context, context.get("a", Integer.class), context.get("b", Integer.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_COMPARE_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_COMPARE_PARTS, llmDescription = L.DESC_COMMAND_COMPARE_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "a", type = Integer.class, description = L.DESC_COMMAND_PART_A_PARAM),
                    @CommandParameter(name = "b", type = Integer.class, description = L.DESC_COMMAND_PART_B_PARAM),
                    @CommandParameter(name = "branchA", type = Integer.class, description = L.DESC_COMMAND_BRANCH_A_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_COMPARE_PARTS_RETURN))
    public void comparePartsBranchA(CommandContext context) throws Exception {
        comparePartsImpl(context, context.get("a", Integer.class), context.get("b", Integer.class), context.get("branchA", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_COMPARE_PARTS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_COMPARE_PARTS, llmDescription = L.DESC_COMMAND_COMPARE_PARTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "a", type = Integer.class, description = L.DESC_COMMAND_PART_A_PARAM),
                    @CommandParameter(name = "b", type = Integer.class, description = L.DESC_COMMAND_PART_B_PARAM),
                    @CommandParameter(name = "branchA", type = Integer.class, description = L.DESC_COMMAND_BRANCH_A_PARAM),
                    @CommandParameter(name = "branchB", type = Integer.class, description = L.DESC_COMMAND_BRANCH_B_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_COMPARE_PARTS_RETURN))
    public void comparePartsBranchB(CommandContext context) throws Exception {
        comparePartsImpl(context, context.get("a", Integer.class), context.get("b", Integer.class), context.get("branchA", Integer.class), context.get("branchB", Integer.class));
    }

    private void comparePartsImpl(CommandContext context, Integer a, Integer b, Integer branchA, Integer branchB)
            throws Exception {
        Book book = new Book(context);
        ChatMessage first = book.part(book.path(branchA), a);
        // part 0 stands for the text the model produced first, before the author edited it
        boolean original = b != null && b == 0;
        ChatMessage second = original ? null : book.part(book.path(branchB == null ? branchA : branchB), b);
        if (original && first.getOriginalResponse() == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NO_ORIGINAL_RESPONSE));
        }
        String oldText = original ? first.getOriginalResponse() : first.getResponse();
        String newText = original ? first.getResponse() : second.getResponse();

        TextDiff.Result diff = TextDiff.diff(oldText, newText);
        if (diff == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_TOO_LARGE_TO_COMPARE));
        }
        JsonObject result = new JsonObject();
        result.addProperty("a", a);
        result.addProperty("b", b);
        result.addProperty("identical", diff.identical());
        result.addProperty("removed", diff.changes().stream().filter(c -> !c.added()).count());
        result.addProperty("added", diff.changes().stream().filter(TextDiff.Change::added).count());
        result.addProperty("sentencesA", diff.oldUnits());
        result.addProperty("sentencesB", diff.newUnits());
        JsonArray lines = new JsonArray();
        for (TextDiff.Change change : diff.changes()) {
            if (lines.size() >= MAX_DIFF_LINES) {
                break;
            }
            JsonObject line = new JsonObject();
            line.addProperty("change", change.added() ? "+" : "-");
            line.addProperty("sentence", change.index());
            line.addProperty("text", preview(change.text(), MAX_DIFF_LINE_LENGTH));
            lines.add(line);
        }
        result.addProperty("truncated", diff.changes().size() > lines.size());
        result.add("changes", lines);
        answer(context, result);
    }
}
