package com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read;

import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Reason;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.JavaTokkitStrategy;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.domain.templates.UserPromptData;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@code /preview-lore-activation text [limit]}, {@code /preview-lore-activation part [branch [limit]]}: which entries of
 * the lorebooks the generation would use for a text, or for a turn like the one that made a part, and why - the rules of
 * the generation step ({@link LorebookActivationService}) - and the lore text and its tokens that would reach the prompt.
 * Changes nothing; the answer is JSON text, see {@link LorebookReadSupport}.
 * <p>
 * With a part the text the filters are looked for in is the user prompt of the book rendered from the turn input stored
 * with that part (instructions, scene, point of view, who is present); the rest of the story is not part of the template
 * context, so a payload that uses it renders without. With a text, that text is used as it is.
 */
public class PreviewLoreActivationCommand extends LorebookReadSupport {

    private static final Logger log = LoggerFactory.getLogger(PreviewLoreActivationCommand.class);

    static final String NAME = "preview-lore-activation";

    @Autowired
    private LorebookActivationService activationService;

    @Autowired
    private TemplateService templateService;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_PREVIEW_LORE_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION, llmDescription = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_PREVIEW_TEXT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_RETURN))
    public void previewText(CommandContext context) throws Exception {
        preview(context, context.get("text", String.class), null, null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_PREVIEW_LORE_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION, llmDescription = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, description = L.DESC_COMMAND_PREVIEW_TEXT_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_RETURN))
    public void previewTextLimit(CommandContext context) throws Exception {
        preview(context, context.get("text", String.class), null, null, context.get("limit", Integer.class));
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_PREVIEW_LORE_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION, llmDescription = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_RETURN))
    public void previewPart(CommandContext context) throws Exception {
        preview(context, null, context.get("part", Integer.class), null, null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_PREVIEW_LORE_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION, llmDescription = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_RETURN))
    public void previewPartBranch(CommandContext context) throws Exception {
        preview(context, null, context.get("part", Integer.class), context.get("branch", Integer.class), null);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_PREVIEW_LORE_ACTIVATION,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION, llmDescription = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = Integer.class, description = L.DESC_COMMAND_PART_PARAM),
                    @CommandParameter(name = "branch", type = Integer.class, description = L.DESC_COMMAND_BRANCH_PARAM),
                    @CommandParameter(name = "limit", type = Integer.class, description = L.DESC_COMMAND_LIMIT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_PREVIEW_LORE_ACTIVATION_RETURN))
    public void previewPartLimit(CommandContext context) throws Exception {
        preview(context, null, context.get("part", Integer.class), context.get("branch", Integer.class), context.get("limit", Integer.class));
    }

    private void preview(CommandContext context, String text, Integer position, Integer branch, Integer limit) throws Exception {
        Book book = new Book();
        Manuscript manuscript = book.manuscript;
        Lore lore = new Lore(book);
        Lorebook root = lore.book(null);

        ChatMessage part = position == null ? null : book.part(book.path(branch), position);
        TurnInput input = part == null ? null : new TurnInput(part.getSceneSetting(), part.getPovCharacter(),
                part.getPresentCharacters(), part.getInstructions());
        TemplateContext templateContext = templateContext(manuscript, input, part);
        String matched = part == null ? StringUtils.defaultString(text) : userPrompt(manuscript, input, templateContext);

        List<String> bookTags = tagRelationService.getTagsForObject(manuscript).stream().map(Tag::getValue).toList();
        List<Lorebook> books = activationService.collectLorebooks(root);
        Map<Long, Lorebook> bookOf = new HashMap<>();
        for (Lorebook l : books) {
            for (LorebookEntry entry : l.getCachedEntries()) {
                bookOf.put(entry.getId(), l);
            }
        }

        LorebookTemplateData data = new LorebookTemplateData();
        if (input != null) {
            data.setPovCharacter(input.povCharacter());
            data.setSceneSetting(input.sceneSetting());
            data.setPresentCharacters(input.presentCharacters());
            data.setInstructions(input.instructions());
        }
        data.setNarrativePov(templateContext.getNarrativePov());
        data.setNarrativeTense(templateContext.getNarrativeTense());
        data.setStyle(templateContext.getStyle());
        data.setTemplateContext(templateContext);

        int max = limit(limit);
        JsonArray activated = new JsonArray();
        JsonArray skipped = new JsonArray();
        int activatedCount = 0;
        int skippedCount = 0;
        Map<Reason, Integer> byReason = new EnumMap<>(Reason.class);
        StringBuilder loreBlock = new StringBuilder();
        StringBuilder userBlock = new StringBuilder();
        for (LorebookEntry entry : activationService.orderedEntries(books)) {
            Verdict verdict = activationService.evaluate(entry, bookTags, matched);
            Lorebook owner = bookOf.get(entry.getId());
            JsonObject row = new JsonObject();
            row.addProperty("lorebook", lore.number(owner));
            row.addProperty("entry", numberOf(lore, owner, entry));
            row.addProperty("name", entry.getName());
            row.addProperty("reason", verdict.reason().name());
            if (verdict.detail() != null) {
                row.addProperty("detail", verdict.detail());
            }
            if (verdict.activated()) {
                String payload = activationService.renderPayload(entry, data);
                if (StringUtils.isNotBlank(payload)) {
                    StringBuilder block = entry.getInsertionMode() == InsertionMode.IN_LORE_BLOCK ? loreBlock : userBlock;
                    block.append(payload).append("\n\n");
                }
                row.addProperty("order", entry.getOrder());
                row.addProperty("insertion", entry.getInsertionMode().name());
                if (++activatedCount <= max) {
                    activated.add(row);
                }
            } else {
                byReason.merge(verdict.reason(), 1, Integer::sum);
                if (++skippedCount <= max) {
                    skipped.add(row);
                }
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("source", part == null ? "text" : "part");
        if (part != null) {
            result.addProperty("part", position);
        }
        result.addProperty("matchedTextLength", matched.length());
        result.addProperty("activatedCount", activatedCount);
        result.add("activated", activated);
        result.addProperty("notActivatedCount", skippedCount);
        JsonObject reasons = new JsonObject();
        byReason.forEach((reason, count) -> reasons.addProperty(reason.name(), count));
        result.add("notActivatedByReason", reasons);
        result.add("notActivated", skipped);
        String lorePart = loreBlock.toString().trim();
        String userPart = userBlock.toString().trim();
        result.addProperty("loreBlock", lorePart);
        result.addProperty("loreBlockTokens", tokens(manuscript, lorePart));
        result.addProperty("userPromptLore", userPart);
        result.addProperty("userPromptLoreTokens", tokens(manuscript, userPart));
        answer(context, result);
    }

    /**
     * The number of the entry in its lorebook, the one {@code list-lore-entries} gives.
     */
    private int numberOf(Lore lore, Lorebook owner, LorebookEntry entry) throws Exception {
        List<LorebookEntry> all = lore.entries(owner);
        for (int i = 0; i < all.size(); i++) {
            if (Objects.equals(all.get(i).getId(), entry.getId())) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * What the templates of a turn can read, as far as it can be known without the generation: the book, the settings
     * of the narration, the turn and the variables of the book and of the branch up to the part.
     */
    private TemplateContext templateContext(Manuscript manuscript, TurnInput input, ChatMessage part) throws Exception {
        TemplateContext context = new TemplateContext();
        if (input != null) {
            context.setPovCharacter(input.povCharacter());
            context.setPresentCharacters(input.presentCharacters());
            context.setSceneSetting(input.sceneSetting());
            context.setInstructions(input.instructions());
        }
        context.setNarrativePov(manuscriptService.getPov(manuscript));
        context.setNarrativeTense(manuscriptService.getTense(manuscript));
        context.setStyle(manuscriptService.getStyle(manuscript));
        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));
        context.setGenerationType("normal");
        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());
        if (part != null) {
            context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, part.getAttributes());
        }
        return context;
    }

    /**
     * The user prompt of the book for the turn, which is what the filters of the entries are looked for in. A template
     * that fails to render is replaced by the plain fields of the turn.
     */
    private String userPrompt(Manuscript manuscript, TurnInput input, TemplateContext context) throws Exception {
        UserPromptData data = new UserPromptData();
        data.setInstructions(input.instructions());
        data.setPovCharacter(input.povCharacter());
        data.setPresentCharacters(input.presentCharacters());
        data.setSceneSetting(input.sceneSetting());
        data.setTemplateContext(context);
        try {
            String rendered = templateService.processTemplate(manuscriptService.getUserPrompt(manuscript), "userPrompt", data);
            return rendered == null ? "" : rendered;
        } catch (Exception e) {
            log.debug("The user prompt of the book did not render: {}", e.getMessage());
            return Arrays.asList(input.instructions(), input.sceneSetting(), input.povCharacter(), input.presentCharacters())
                    .stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
        }
    }

    private long tokens(Manuscript manuscript, String text) throws Exception {
        if (text.isEmpty()) {
            return 0;
        }
        AI ai = manuscript.getAi();
        InferenceService inferenceService = ai == null ? null : inferenceServices.forAI(ai);
        return inferenceService == null ? new JavaTokkitStrategy().countTokens(null, text) : inferenceService.countTokens(text);
    }
}
