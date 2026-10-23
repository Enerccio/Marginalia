package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequestType;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code /next [scene [pov [present [instructions [after [profile]]]]]]}, {@code /swipe [part [scene [pov [present [instructions
 * [profile]]]]]]}, {@code /regenerate [part ...]}: the commands that have the model write the story, the way the buttons of the story
 * do. They are {@code async}, slow and cost tokens. One generation of a book runs at a time, because each moves the active end of
 * the book: the others wait for their turn. Swipe and regenerate work on the last part of a branch (the end of the active branch
 * when none is given), which they make the active end first. For an agent {@code profile} names one of the model profiles of its
 * definition to write with.
 */
@Configurable
public class StoryGenerationCommands extends ManuscriptWriteSupport {

    static final String NEXT = "next";
    static final String SWIPE = "swipe";
    static final String REGENERATE = "regenerate";

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void next(CommandContext context) throws Exception {
        next(context, null, null, null, null, null, null);
    }

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void nextScene(CommandContext context) throws Exception {
        next(context, context.get("scene", String.class), null, null, null, null, null);
    }

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void nextPov(CommandContext context) throws Exception {
        next(context, context.get("scene", String.class), context.get("pov", String.class), null, null, null, null);
    }

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void nextPresent(CommandContext context) throws Exception {
        next(context, context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), null, null, null);
    }

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void nextInstructions(CommandContext context) throws Exception {
        next(context, context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), null, null);
    }

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "after", type = String.class, description = L.DESC_COMMAND_AFTER_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void nextAfter(CommandContext context) throws Exception {
        next(context, context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), ref(context, "after"), null);
    }

    @Command(commandName = NEXT, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_NEXT, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_NEXT, llmDescription = L.DESC_COMMAND_NEXT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "after", type = String.class, description = L.DESC_COMMAND_AFTER_PART_PARAM),
                    @CommandParameter(name = "profile", type = String.class, description = L.DESC_COMMAND_GEN_PROFILE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_NEXT_RETURN))
    public void nextProfile(CommandContext context) throws Exception {
        next(context, context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), ref(context, "after"), ref(context, "profile"));
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipe(CommandContext context) throws Exception {
        swipe(context, null, null, null, null, null, null);
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipePart(CommandContext context) throws Exception {
        swipe(context, ref(context, "part"), null, null, null, null, null);
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipeScene(CommandContext context) throws Exception {
        swipe(context, ref(context, "part"), context.get("scene", String.class), null, null, null, null);
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipePov(CommandContext context) throws Exception {
        swipe(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), null, null, null);
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipePresent(CommandContext context) throws Exception {
        swipe(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), null, null);
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipeInstructions(CommandContext context) throws Exception {
        swipe(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), null);
    }

    @Command(commandName = SWIPE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_SWIPE, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SWIPE, llmDescription = L.DESC_COMMAND_SWIPE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "profile", type = String.class, description = L.DESC_COMMAND_GEN_PROFILE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SWIPE_RETURN))
    public void swipeProfile(CommandContext context) throws Exception {
        swipe(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), ref(context, "profile"));
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regenerate(CommandContext context) throws Exception {
        regenerate(context, null, null, null, null, null, null);
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regeneratePart(CommandContext context) throws Exception {
        regenerate(context, ref(context, "part"), null, null, null, null, null);
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regenerateScene(CommandContext context) throws Exception {
        regenerate(context, ref(context, "part"), context.get("scene", String.class), null, null, null, null);
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regeneratePov(CommandContext context) throws Exception {
        regenerate(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), null, null, null);
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regeneratePresent(CommandContext context) throws Exception {
        regenerate(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), null, null);
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regenerateInstructions(CommandContext context) throws Exception {
        regenerate(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), null);
    }

    @Command(commandName = REGENERATE, batch = BatchPolicy.SINGLE, humanName = L.LABEL_COMMAND_REGENERATE, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_REGENERATE, llmDescription = L.DESC_COMMAND_REGENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, description = L.DESC_COMMAND_GEN_PART_PARAM),
                    @CommandParameter(name = "scene", type = String.class, description = L.DESC_COMMAND_GEN_SCENE_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_GEN_POV_PARAM),
                    @CommandParameter(name = "present", type = String.class, description = L.DESC_COMMAND_GEN_PRESENT_PARAM),
                    @CommandParameter(name = "instructions", type = String.class, description = L.DESC_COMMAND_GEN_INSTRUCTIONS_PARAM),
                    @CommandParameter(name = "profile", type = String.class, description = L.DESC_COMMAND_GEN_PROFILE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_REGENERATE_RETURN))
    public void regenerateProfile(CommandContext context) throws Exception {
        regenerate(context, ref(context, "part"), context.get("scene", String.class), context.get("pov", String.class), context.get("present", String.class), context.get("instructions", String.class), ref(context, "profile"));
    }

    /** One generation of a book at a time: each moves the active end of the book. */
    private static final Map<Long, ReentrantLock> GATES = new ConcurrentHashMap<>();

    @Autowired
    private StoryGenerationService storyGenerationService;

    private void next(CommandContext context, String scene, String pov, String present, String instructions, String after,
                      String profile) throws Exception {
        generate(context, GenerationRequestType.NEW_MESSAGE, null, after, scene, pov, present, instructions, profile);
    }

    private void swipe(CommandContext context, String part, String scene, String pov, String present, String instructions,
                       String profile) throws Exception {
        generate(context, GenerationRequestType.SWIPE, part, null, scene, pov, present, instructions, profile);
    }

    private void regenerate(CommandContext context, String part, String scene, String pov, String present, String instructions,
                            String profile) throws Exception {
        generate(context, GenerationRequestType.REGENERATE, part, null, scene, pov, present, instructions, profile);
    }

    private void generate(CommandContext context, GenerationRequestType type, String partHandle, String after, String scene,
                          String pov, String present, String instructions, String profile) throws Exception {
        Book book = new Book(context);
        GenerationOptions options = options(context, profile);
        ReentrantLock gate = GATES.computeIfAbsent(book.manuscript.getId(), _ -> new ReentrantLock());
        gate.lockInterruptibly();
        try {
            // the book as it is when it is this one's turn
            book = new Book(context);
            ChatMessage target = null;
            if (type == GenerationRequestType.NEW_MESSAGE) {
                if (after != null) {
                    activate(book, part(book, after));
                }
            } else {
                target = partHandle != null ? part(book, partHandle) : book.manuscript.getActiveLeaf();
                if (target == null) {
                    throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_STORY));
                }
                if (!book.childrenOf(target).isEmpty()) {
                    throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_PART_NOT_LAST));
                }
                if (type == GenerationRequestType.REGENERATE && !confirmed(context,
                        String.format(loc.getValue(L.MSG_COMMAND_REGENERATE_CONFIRM), book.locate(target.getUuid(), null).position()))) {
                    return;
                }
                activate(book, target);
            }
            TurnInput input = type == GenerationRequestType.NEW_MESSAGE
                    ? new TurnInput(text(scene), text(pov), text(present), text(instructions))
                    : new TurnInput(scene != null ? scene : text(target.getSceneSetting()), pov != null ? pov : text(target.getPovCharacter()),
                    present != null ? present : text(target.getPresentCharacters()),
                    instructions != null ? instructions : text(target.getInstructions()));
            GenerationRequest request = switch (type) {
                case NEW_MESSAGE -> GenerationRequest.newMessage();
                case SWIPE -> GenerationRequest.newSwipe(target);
                case REGENERATE -> GenerationRequest.regenerate(target);
            };

            context.setStatus(loc.getValue(L.MSG_COMMAND_GENERATING));
            CompletableFuture<ChatMessage> done = new CompletableFuture<>();
            List<String> warnings = new CopyOnWriteArrayList<>();
            Manuscript fresh = context.getScope().getManuscript();
            ChatMessage created;
            try (AIGate.Permit _ = placeAt(context, options.ai() != null ? options.ai() : fresh.getAi())) {
                CancellationToken token = storyGenerationService.generateNextTurn(fresh, input, request,
                        listener(context, done, warnings), options);
                created = await(context, done, token);
            }
            if (created == null) {
                return;
            }
            Book.Located at = new Book(context).locate(created.getUuid(), null);
            JsonObject result = describe(created, at.position());
            result.addProperty("parts", at.path().size());
            result.addProperty("model", created.getModelUsed());
            if (!warnings.isEmpty()) {
                JsonArray list = new JsonArray();
                warnings.forEach(list::add);
                result.add("warnings", list);
            }
            changed();
            answer(context, result);
        } finally {
            gate.unlock();
        }
    }

    /**
     * Makes the part the end of the active branch, which swipe and regenerate need and a continuation starts from.
     */
    private void activate(Book book, ChatMessage message) throws Exception {
        if (book.manuscript.getActiveLeaf() == null || !book.manuscript.getActiveLeaf().getId().equals(message.getId())) {
            book.manuscript.setActiveLeaf(message);
            manuscriptService.save(book.manuscript);
        }
    }

    private static String text(String text) {
        return text == null ? "" : text;
    }

    /**
     * Collects what the generation says. A question (the summaries that are outdated are left out of the prompt if it
     * goes on) is passed on to whoever called the command.
     */
    private GenerationListener listener(CommandContext context, CompletableFuture<ChatMessage> done, List<String> warnings) {
        return new GenerationListener() {
            @Override
            public void onNodeCreated(ChatMessage message) {
            }

            @Override
            public void onReasoningChunk(String chunk, ChatMessage message) {
            }

            @Override
            public void onResponseChunk(String chunk, ChatMessage message) {
            }

            @Override
            public void onMetricsUpdated(ChatMessage message) {
            }

            @Override
            public void onComplete(ChatMessage message) {
                done.complete(message);
            }

            @Override
            public void onCancelled(ChatMessage partialMessage) {
                done.completeExceptionally(new CancellationException(loc.getValue(L.ERROR_COMMAND_GENERATION_CANCELLED)));
            }

            @Override
            public void onError(Throwable cause) {
                done.completeExceptionally(cause);
            }

            @Override
            public void onSimpleError(String error) {
                done.completeExceptionally(new IllegalStateException(error));
            }

            @Override
            public void onWarning(String warning) {
                warnings.add(warning);
            }

            @Override
            public void askQuestion(String question, Runnable yes, Runnable no) {
                CompletableFuture.runAsync(() -> {
                    boolean answer = false;
                    try {
                        answer = context.confirm(question);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (RuntimeException e) {
                        // not asked, so no
                    }
                    if (answer) {
                        warnings.add(question);
                    }
                    (answer ? yes : no).run();
                });
            }
        };
    }
}
