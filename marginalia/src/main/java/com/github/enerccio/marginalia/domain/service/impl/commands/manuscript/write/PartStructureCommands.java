package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.M_L;
import com.google.gson.JsonObject;
import java.util.Set;

/**
 * {@code /create-part text [parent [instructions [scene [pov [present]]]]]}, {@code /branch-part part}, {@code /delete-part part},
 * {@code /switch-branch part}, {@code /insert-part text part [before]}: the commands that add, insert, copy and delete the parts of the
 * story and choose the branch. A part is given by
 * its handle, see {@link ManuscriptWriteSupport}.
 */
public class PartStructureCommands extends ManuscriptWriteSupport {

    static final String CREATE_PART = "create-part";
    static final String INSERT_PART = "insert-part";
    static final String BRANCH_PART = "branch-part";
    static final String DELETE_PART = "delete-part";
    static final String SWITCH_BRANCH = "switch-branch";

    @Command(commandName = CREATE_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_CREATE_PART,
            description = M_L.DESC_COMMAND_CREATE_PART,
            llmDescription = M_L.DESC_COMMAND_CREATE_PART_LLM,
            parameters = @MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
            returns = M_L.DESC_COMMAND_CREATE_PART_RETURN)
    public void createPart(CommandContext context) throws Exception {
        createPart(context, context.get("text", String.class), null, null, null, null, null);
    }

    @Command(commandName = CREATE_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "parent", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_CREATE_PART,
            description = M_L.DESC_COMMAND_CREATE_PART,
            llmDescription = M_L.DESC_COMMAND_CREATE_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "parent", description = M_L.DESC_COMMAND_PARENT_PARAM)},
            returns = M_L.DESC_COMMAND_CREATE_PART_RETURN)
    public void createPartParent(CommandContext context) throws Exception {
        createPart(context, context.get("text", String.class), ref(context, "parent"), null, null, null, null);
    }

    @Command(commandName = CREATE_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "parent", type = String.class),
                    @CommandParameter(name = "instructions", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_CREATE_PART,
            description = M_L.DESC_COMMAND_CREATE_PART,
            llmDescription = M_L.DESC_COMMAND_CREATE_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "parent", description = M_L.DESC_COMMAND_PARENT_PARAM),
                    @MarginaliaParameterText(name = "instructions", description = M_L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM)},
            returns = M_L.DESC_COMMAND_CREATE_PART_RETURN)
    public void createPartInstructions(CommandContext context) throws Exception {
        createPart(context, context.get("text", String.class), ref(context, "parent"), context.get("instructions", String.class), null, null, null);
    }

    @Command(commandName = CREATE_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "parent", type = String.class),
                    @CommandParameter(name = "instructions", type = String.class),
                    @CommandParameter(name = "scene", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_CREATE_PART,
            description = M_L.DESC_COMMAND_CREATE_PART,
            llmDescription = M_L.DESC_COMMAND_CREATE_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "parent", description = M_L.DESC_COMMAND_PARENT_PARAM),
                    @MarginaliaParameterText(name = "instructions", description = M_L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM),
                    @MarginaliaParameterText(name = "scene", description = M_L.DESC_COMMAND_TURN_SCENE_PARAM)},
            returns = M_L.DESC_COMMAND_CREATE_PART_RETURN)
    public void createPartScene(CommandContext context) throws Exception {
        createPart(context, context.get("text", String.class), ref(context, "parent"), context.get("instructions", String.class), context.get("scene", String.class), null, null);
    }

    @Command(commandName = CREATE_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "parent", type = String.class),
                    @CommandParameter(name = "instructions", type = String.class),
                    @CommandParameter(name = "scene", type = String.class),
                    @CommandParameter(name = "pov", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_CREATE_PART,
            description = M_L.DESC_COMMAND_CREATE_PART,
            llmDescription = M_L.DESC_COMMAND_CREATE_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "parent", description = M_L.DESC_COMMAND_PARENT_PARAM),
                    @MarginaliaParameterText(name = "instructions", description = M_L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM),
                    @MarginaliaParameterText(name = "scene", description = M_L.DESC_COMMAND_TURN_SCENE_PARAM),
                    @MarginaliaParameterText(name = "pov", description = M_L.DESC_COMMAND_TURN_POV_PARAM)},
            returns = M_L.DESC_COMMAND_CREATE_PART_RETURN)
    public void createPartPov(CommandContext context) throws Exception {
        createPart(context, context.get("text", String.class), ref(context, "parent"), context.get("instructions", String.class), context.get("scene", String.class), context.get("pov", String.class), null);
    }

    @Command(commandName = CREATE_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "parent", type = String.class),
                    @CommandParameter(name = "instructions", type = String.class),
                    @CommandParameter(name = "scene", type = String.class),
                    @CommandParameter(name = "pov", type = String.class),
                    @CommandParameter(name = "present", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_CREATE_PART,
            description = M_L.DESC_COMMAND_CREATE_PART,
            llmDescription = M_L.DESC_COMMAND_CREATE_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "parent", description = M_L.DESC_COMMAND_PARENT_PARAM),
                    @MarginaliaParameterText(name = "instructions", description = M_L.DESC_COMMAND_TURN_INSTRUCTIONS_PARAM),
                    @MarginaliaParameterText(name = "scene", description = M_L.DESC_COMMAND_TURN_SCENE_PARAM),
                    @MarginaliaParameterText(name = "pov", description = M_L.DESC_COMMAND_TURN_POV_PARAM),
                    @MarginaliaParameterText(name = "present", description = M_L.DESC_COMMAND_TURN_PRESENT_PARAM)},
            returns = M_L.DESC_COMMAND_CREATE_PART_RETURN)
    public void createPartPresent(CommandContext context) throws Exception {
        createPart(context, context.get("text", String.class), ref(context, "parent"), context.get("instructions", String.class), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class));
    }

    @Command(commandName = INSERT_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_INSERT_PART,
            description = M_L.DESC_COMMAND_INSERT_PART,
            llmDescription = M_L.DESC_COMMAND_INSERT_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_INSERT_ANCHOR_PARAM)},
            returns = M_L.DESC_COMMAND_INSERT_PART_RETURN)
    public void insertPart(CommandContext context) throws Exception {
        insertPart(context, context.get("text", String.class), ref(context, "part"), false);
    }

    @Command(commandName = INSERT_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class),
                    @CommandParameter(name = "part", type = String.class),
                    @CommandParameter(name = "before", type = Boolean.class, optional = true, defaultValue = "false")}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_INSERT_PART,
            description = M_L.DESC_COMMAND_INSERT_PART,
            llmDescription = M_L.DESC_COMMAND_INSERT_PART_LLM,
            parameters = {@MarginaliaParameterText(name = "text", description = M_L.DESC_COMMAND_NEW_TEXT_PARAM),
                    @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_INSERT_ANCHOR_PARAM),
                    @MarginaliaParameterText(name = "before", description = M_L.DESC_COMMAND_INSERT_BEFORE_PARAM)},
            returns = M_L.DESC_COMMAND_INSERT_PART_RETURN)
    public void insertPartBefore(CommandContext context) throws Exception {
        insertPart(context, context.get("text", String.class), ref(context, "part"), Boolean.TRUE.equals(context.get("before", Boolean.class)));
    }

    @Command(commandName = BRANCH_PART,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_BRANCH_PART,
            description = M_L.DESC_COMMAND_BRANCH_PART,
            llmDescription = M_L.DESC_COMMAND_BRANCH_PART_LLM,
            parameters = @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_HANDLE_PARAM),
            returns = M_L.DESC_COMMAND_BRANCH_PART_RETURN)
    public void branchPart(CommandContext context) throws Exception {
        branchPart(context, ref(context, "part"));
    }

    @Command(commandName = DELETE_PART,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_DELETE_PART,
            description = M_L.DESC_COMMAND_DELETE_PART,
            llmDescription = M_L.DESC_COMMAND_DELETE_PART_LLM,
            parameters = @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_HANDLE_PARAM),
            returns = M_L.DESC_COMMAND_DELETE_PART_RETURN)
    public void deletePart(CommandContext context) throws Exception {
        deletePart(context, ref(context, "part"));
    }

    @Command(commandName = SWITCH_BRANCH,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class)}),
            returns = @CommandReturn(name = "result", type = String.class))
    @MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_SWITCH_BRANCH,
            description = M_L.DESC_COMMAND_SWITCH_BRANCH,
            llmDescription = M_L.DESC_COMMAND_SWITCH_BRANCH_LLM,
            parameters = @MarginaliaParameterText(name = "part", description = M_L.DESC_COMMAND_PART_HANDLE_PARAM),
            returns = M_L.DESC_COMMAND_SWITCH_BRANCH_RETURN)
    public void switchBranch(CommandContext context) throws Exception {
        switchBranch(context, ref(context, "part"));
    }

    private void createPart(CommandContext context, String text, String parentHandle, String instructions, String scene,
                            String pov, String present) throws Exception {
        Book book = new Book(context);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(loc.getValue(M_L.ERROR_COMMAND_PART_TEXT_EMPTY));
        }
        ChatMessage parent = parentHandle != null ? part(book, parentHandle) : book.manuscript.getActiveLeaf();
        TurnInput input = instructions == null && scene == null && pov == null && present == null ? null
                : new TurnInput(scene, pov, present, instructions);
        ChatMessage created = chatMessageService.createPart(book.manuscript, parent, text, input, RevisionOrigin.automatic(RevisionDetail.COMMAND, true));
        answerWith(context, created, null);
    }

    /**
     * Inserts the part into the story next to the part {@code handle}: after it, or before it (after the part above it, the first
     * part of the story when it has none). What followed the place follows the new part, nothing is deleted or made again.
     */
    private void insertPart(CommandContext context, String text, String handle, boolean before) throws Exception {
        Book book = new Book(context);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(loc.getValue(M_L.ERROR_COMMAND_PART_TEXT_EMPTY));
        }
        ChatMessage anchor = part(book, handle);
        Book.Located at = book.locate(handle, null);
        Set<Long> outdated = outdatedHeads(at.path());
        ChatMessage created = chatMessageService.insertPart(book.manuscript, before ? anchor.getParent() : anchor, text, null,
                RevisionOrigin.automatic(RevisionDetail.COMMAND, true));

        Book fresh = new Book(context);
        Book.Located now = fresh.locate(created.getUuid(), null);
        JsonObject result = describe(created, now.position());
        result.addProperty("parts", now.path().size());
        result.addProperty("continuedBy", fresh.childrenOf(created).size());
        result.addProperty("insertedBefore", before ? handle : null);
        reportOutdated(result, now.path(), outdated);
        changed();
        answer(context, result);
    }

    private void branchPart(CommandContext context, String handle) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        ChatMessage copy = chatMessageService.branchAndSwitch(book.manuscript, message).getActiveLeaf();
        answerWith(context, copy, handle);
    }

    private void deletePart(CommandContext context, String handle) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        Book.Located at = book.locate(handle, null);
        int followers = book.childrenOf(message).size();
        if (!confirmed(context, String.format(loc.getValue(M_L.MSG_COMMAND_DELETE_PART_CONFIRM), at.position(), followers))) {
            return;
        }
        Set<Long> before = outdatedHeads(at.path());
        ChatMessage end = at.path().getLast();
        ChatMessage parent = message.getParent();
        Manuscript saved = chatMessageService.deleteNodeAndMigrateChildren(message, book.manuscript, false);

        Book fresh = new Book(context);
        JsonObject result = new JsonObject();
        result.addProperty("deleted", handle);
        result.addProperty("position", at.position());
        result.addProperty("followingParts", followers);
        ChatMessage leaf = end.getId().equals(message.getId()) ? parent : end;
        if (leaf != null) {
            Book.Located now = fresh.locate(leaf.getUuid(), null);
            reportOutdated(result, now.path(), before);
        }
        if (saved != null && saved.getActiveLeaf() != null) {
            result.addProperty("activePart", saved.getActiveLeaf().getUuid());
        }
        changed();
        answer(context, result);
    }

    private void switchBranch(CommandContext context, String handle) throws Exception {
        Book book = new Book(context);
        ChatMessage message = part(book, handle);
        ChatMessage leaf = chatMessageService.swipeAndSwitch(book.manuscript, message).getActiveLeaf();
        Book fresh = new Book(context);
        Book.Located at = fresh.locate(handle, null);
        JsonObject result = describe(at.message(), at.position());
        result.addProperty("branchEnd", leaf.getUuid());
        result.addProperty("parts", at.path().size());
        changed();
        answer(context, result);
    }

    /**
     * Tells about a part that was just made and is the end of the active branch now.
     */
    private void answerWith(CommandContext context, ChatMessage created, String copiedFrom) throws Exception {
        Book.Located at = new Book(context).locate(created.getUuid(), null);
        JsonObject result = describe(created, at.position());
        result.addProperty("parts", at.path().size());
        if (copiedFrom != null) {
            result.addProperty("copiedFrom", copiedFrom);
        }
        changed();
        answer(context, result);
    }
}
