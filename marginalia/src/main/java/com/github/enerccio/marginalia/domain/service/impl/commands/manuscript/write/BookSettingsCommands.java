package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.write;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

/**
 * {@code /set-book-info [name [description [pov [tense [style [language]]]]]]}, {@code /set-book-prompts [template [userPrompt
 * [summaryPrompt [metaSummaryPrompt]]]]}, {@code /set-book-tags [add [remove]]}: the commands that change the settings of the book. The
 * prompts are the only one that is destructive: a bad template spoils every later generation.
 */
public class BookSettingsCommands extends ManuscriptWriteSupport {

    static final String SET_BOOK_INFO = "set-book-info";
    static final String SET_BOOK_PROMPTS = "set-book-prompts";
    static final String SET_BOOK_TAGS = "set-book-tags";

    @Command(commandName = SET_BOOK_INFO, humanName = L.LABEL_COMMAND_SET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_INFO, llmDescription = L.DESC_COMMAND_SET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_BOOK_NAME_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_INFO_RETURN))
    public void setBookInfo(CommandContext context) throws Exception {
        setBookInfo(context, context.get("name", String.class), null, null, null, null, null);
    }

    @Command(commandName = SET_BOOK_INFO, humanName = L.LABEL_COMMAND_SET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_INFO, llmDescription = L.DESC_COMMAND_SET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_BOOK_NAME_PARAM),
                    @CommandParameter(name = "description", type = String.class, description = L.DESC_COMMAND_BOOK_DESCRIPTION_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_INFO_RETURN))
    public void setBookInfoDescription(CommandContext context) throws Exception {
        setBookInfo(context, context.get("name", String.class), context.get("description", String.class), null, null, null, null);
    }

    @Command(commandName = SET_BOOK_INFO, humanName = L.LABEL_COMMAND_SET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_INFO, llmDescription = L.DESC_COMMAND_SET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_BOOK_NAME_PARAM),
                    @CommandParameter(name = "description", type = String.class, description = L.DESC_COMMAND_BOOK_DESCRIPTION_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_BOOK_POV_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_INFO_RETURN))
    public void setBookInfoPov(CommandContext context) throws Exception {
        setBookInfo(context, context.get("name", String.class), context.get("description", String.class), context.get("pov", String.class), null, null, null);
    }

    @Command(commandName = SET_BOOK_INFO, humanName = L.LABEL_COMMAND_SET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_INFO, llmDescription = L.DESC_COMMAND_SET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_BOOK_NAME_PARAM),
                    @CommandParameter(name = "description", type = String.class, description = L.DESC_COMMAND_BOOK_DESCRIPTION_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_BOOK_POV_PARAM),
                    @CommandParameter(name = "tense", type = String.class, description = L.DESC_COMMAND_BOOK_TENSE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_INFO_RETURN))
    public void setBookInfoTense(CommandContext context) throws Exception {
        setBookInfo(context, context.get("name", String.class), context.get("description", String.class), context.get("pov", String.class), context.get("tense", String.class), null, null);
    }

    @Command(commandName = SET_BOOK_INFO, humanName = L.LABEL_COMMAND_SET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_INFO, llmDescription = L.DESC_COMMAND_SET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_BOOK_NAME_PARAM),
                    @CommandParameter(name = "description", type = String.class, description = L.DESC_COMMAND_BOOK_DESCRIPTION_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_BOOK_POV_PARAM),
                    @CommandParameter(name = "tense", type = String.class, description = L.DESC_COMMAND_BOOK_TENSE_PARAM),
                    @CommandParameter(name = "style", type = String.class, description = L.DESC_COMMAND_BOOK_STYLE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_INFO_RETURN))
    public void setBookInfoStyle(CommandContext context) throws Exception {
        setBookInfo(context, context.get("name", String.class), context.get("description", String.class), context.get("pov", String.class), context.get("tense", String.class), context.get("style", String.class), null);
    }

    @Command(commandName = SET_BOOK_INFO, humanName = L.LABEL_COMMAND_SET_BOOK_INFO,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_INFO, llmDescription = L.DESC_COMMAND_SET_BOOK_INFO_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, description = L.DESC_COMMAND_BOOK_NAME_PARAM),
                    @CommandParameter(name = "description", type = String.class, description = L.DESC_COMMAND_BOOK_DESCRIPTION_PARAM),
                    @CommandParameter(name = "pov", type = String.class, description = L.DESC_COMMAND_BOOK_POV_PARAM),
                    @CommandParameter(name = "tense", type = String.class, description = L.DESC_COMMAND_BOOK_TENSE_PARAM),
                    @CommandParameter(name = "style", type = String.class, description = L.DESC_COMMAND_BOOK_STYLE_PARAM),
                    @CommandParameter(name = "language", type = String.class, description = L.DESC_COMMAND_BOOK_LANGUAGE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_INFO_RETURN))
    public void setBookInfoLanguage(CommandContext context) throws Exception {
        setBookInfo(context, context.get("name", String.class), context.get("description", String.class), context.get("pov", String.class), context.get("tense", String.class), context.get("style", String.class), ref(context, "language"));
    }

    @Command(commandName = SET_BOOK_PROMPTS, humanName = L.LABEL_COMMAND_SET_BOOK_PROMPTS,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_PROMPTS, llmDescription = L.DESC_COMMAND_SET_BOOK_PROMPTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "template", type = String.class, description = L.DESC_COMMAND_BOOK_TEMPLATE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_PROMPTS_RETURN))
    public void setBookPrompts(CommandContext context) throws Exception {
        setBookPrompts(context, context.get("template", String.class), null, null, null);
    }

    @Command(commandName = SET_BOOK_PROMPTS, humanName = L.LABEL_COMMAND_SET_BOOK_PROMPTS,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_PROMPTS, llmDescription = L.DESC_COMMAND_SET_BOOK_PROMPTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "template", type = String.class, description = L.DESC_COMMAND_BOOK_TEMPLATE_PARAM),
                    @CommandParameter(name = "userPrompt", type = String.class, description = L.DESC_COMMAND_BOOK_USER_PROMPT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_PROMPTS_RETURN))
    public void setBookPromptsUserPrompt(CommandContext context) throws Exception {
        setBookPrompts(context, context.get("template", String.class), context.get("userPrompt", String.class), null, null);
    }

    @Command(commandName = SET_BOOK_PROMPTS, humanName = L.LABEL_COMMAND_SET_BOOK_PROMPTS,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_PROMPTS, llmDescription = L.DESC_COMMAND_SET_BOOK_PROMPTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "template", type = String.class, description = L.DESC_COMMAND_BOOK_TEMPLATE_PARAM),
                    @CommandParameter(name = "userPrompt", type = String.class, description = L.DESC_COMMAND_BOOK_USER_PROMPT_PARAM),
                    @CommandParameter(name = "summaryPrompt", type = String.class, description = L.DESC_COMMAND_BOOK_SUMMARY_PROMPT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_PROMPTS_RETURN))
    public void setBookPromptsSummaryPrompt(CommandContext context) throws Exception {
        setBookPrompts(context, context.get("template", String.class), context.get("userPrompt", String.class), context.get("summaryPrompt", String.class), null);
    }

    @Command(commandName = SET_BOOK_PROMPTS, humanName = L.LABEL_COMMAND_SET_BOOK_PROMPTS,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_PROMPTS, llmDescription = L.DESC_COMMAND_SET_BOOK_PROMPTS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "template", type = String.class, description = L.DESC_COMMAND_BOOK_TEMPLATE_PARAM),
                    @CommandParameter(name = "userPrompt", type = String.class, description = L.DESC_COMMAND_BOOK_USER_PROMPT_PARAM),
                    @CommandParameter(name = "summaryPrompt", type = String.class, description = L.DESC_COMMAND_BOOK_SUMMARY_PROMPT_PARAM),
                    @CommandParameter(name = "metaSummaryPrompt", type = String.class, description = L.DESC_COMMAND_BOOK_META_SUMMARY_PROMPT_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_PROMPTS_RETURN))
    public void setBookPromptsMetaSummaryPrompt(CommandContext context) throws Exception {
        setBookPrompts(context, context.get("template", String.class), context.get("userPrompt", String.class), context.get("summaryPrompt", String.class), context.get("metaSummaryPrompt", String.class));
    }

    @Command(commandName = SET_BOOK_TAGS, humanName = L.LABEL_COMMAND_SET_BOOK_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_TAGS, llmDescription = L.DESC_COMMAND_SET_BOOK_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "add", type = String.class, description = L.DESC_COMMAND_TAGS_ADD_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_TAGS_RETURN))
    public void setBookTags(CommandContext context) throws Exception {
        setBookTags(context, context.get("add", String.class), null);
    }

    @Command(commandName = SET_BOOK_TAGS, humanName = L.LABEL_COMMAND_SET_BOOK_TAGS,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_SET_BOOK_TAGS, llmDescription = L.DESC_COMMAND_SET_BOOK_TAGS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "add", type = String.class, description = L.DESC_COMMAND_TAGS_ADD_PARAM),
                    @CommandParameter(name = "remove", type = String.class, description = L.DESC_COMMAND_TAGS_REMOVE_PARAM)}),
            returns = @CommandReturn(name = "result", type = String.class, description = L.DESC_COMMAND_SET_BOOK_TAGS_RETURN))
    public void setBookTagsRemove(CommandContext context) throws Exception {
        setBookTags(context, context.get("add", String.class), context.get("remove", String.class));
    }

    private void setBookInfo(CommandContext context, String name, String description, String pov, String tense, String style,
                             String language) throws Exception {
        Book book = new Book(context);
        ManuscriptService.BookSettings changes = new ManuscriptService.BookSettings().name(name).description(description)
                .pov(pov).tense(tense).style(style).language(language == null ? null : language(language));
        if (name == null && description == null && pov == null && tense == null && style == null && language == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        ManuscriptService.SettingsSaved saved = manuscriptService.updateSettings(book.manuscript, changes);
        Manuscript manuscript = saved.manuscript();
        JsonObject result = new JsonObject();
        result.addProperty("name", manuscript.getName());
        result.addProperty("description", manuscript.getDescription());
        result.addProperty("language", manuscriptService.getLanguage(manuscript).getCode());
        result.addProperty("pov", manuscriptService.getPov(manuscript));
        result.addProperty("tense", manuscriptService.getTense(manuscript));
        result.addProperty("style", manuscriptService.getStyle(manuscript));
        changed();
        answer(context, result);
    }

    private void setBookPrompts(CommandContext context, String template, String userPrompt, String summaryPrompt,
                                String metaSummaryPrompt) throws Exception {
        Book book = new Book(context);
        if (template == null && userPrompt == null && summaryPrompt == null && metaSummaryPrompt == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        if (!confirmed(context, loc.getValue(L.MSG_COMMAND_SET_PROMPTS_CONFIRM))) {
            return;
        }
        ManuscriptService.SettingsSaved saved = manuscriptService.updateSettings(book.manuscript,
                new ManuscriptService.BookSettings().masterTemplate(template).userPrompt(userPrompt)
                        .summaryPrompt(summaryPrompt).metaSummaryPrompt(metaSummaryPrompt));
        JsonObject result = new JsonObject();
        JsonArray changed = new JsonArray();
        if (template != null) {
            changed.add("template");
        }
        if (userPrompt != null) {
            changed.add("userPrompt");
        }
        if (summaryPrompt != null) {
            changed.add("summaryPrompt");
        }
        if (metaSummaryPrompt != null) {
            changed.add("metaSummaryPrompt");
        }
        result.add("changed", changed);
        JsonArray warnings = new JsonArray();
        saved.warnings().forEach(warnings::add);
        result.add("warnings", warnings);
        changed();
        answer(context, result);
    }

    private void setBookTags(CommandContext context, String add, String remove) throws Exception {
        Book book = new Book(context);
        if (add == null && remove == null) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_COMMAND_NOTHING_TO_CHANGE));
        }
        Manuscript manuscript = book.manuscript;
        List<Tag> current = tagRelationService.getTagsForObject(manuscript);
        for (String value : list(remove)) {
            for (Tag tag : current) {
                if (tag.getValue().equalsIgnoreCase(value)) {
                    tagRelationService.removeRelation(tag, manuscript);
                }
            }
        }
        for (String value : list(add)) {
            if (current.stream().noneMatch(tag -> tag.getValue().equalsIgnoreCase(value))) {
                tagRelationService.createRelation(tagService.getOrCreateForUser(value), manuscript);
            }
        }
        JsonObject result = new JsonObject();
        JsonArray tags = new JsonArray();
        tagRelationService.getTagsForObject(manuscript).forEach(tag -> tags.add(tag.getValue()));
        result.add("tags", tags);
        changed();
        answer(context, result);
    }

    private Language language(String text) {
        String wanted = text.strip();
        return Arrays.stream(Language.values())
                .filter(l -> l.getCode().equalsIgnoreCase(wanted) || l.getDisplayName().equalsIgnoreCase(wanted) || l.name().equalsIgnoreCase(wanted))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN_LANGUAGE), text)));
    }
}
