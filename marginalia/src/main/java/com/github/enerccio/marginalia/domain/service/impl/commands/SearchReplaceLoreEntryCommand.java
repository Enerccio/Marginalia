package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch.InvalidSearchException;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.CommandUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.concurrent.CancellationException;

/**
 * {@code /search-replace-lore-entries search replace [regex [caseSensitive]]}: the search and replace dialog of the
 * lorebook as a command, for the lorebook of the book that is open (the entries' name, text and comment). The options
 * that are left out have the defaults of the dialog: no regex, case sensitive. Like
 * {@link SearchReplaceChatMessageCommand} it is {@code async}, asks the user to confirm the number of matches (there is
 * no preview), can't be stopped while it replaces and works only with a book window open. Fails when the book has no
 * lorebook.
 */
@Configurable
public class SearchReplaceLoreEntryCommand {

    static final String NAME = "search-replace-lore-entries";

    @Autowired
    private LorebookReplaceService replaceService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private Localization loc;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_LOREBOOK, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK_RETURN))
    public void replace(CommandContext context) throws Exception {
        run(context, false, true);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_LOREBOOK, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK_RETURN))
    public void replaceRegex(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), true);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_LOREBOOK, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_LOREBOOK_RETURN))
    public void replaceCase(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class));
    }

    private void run(CommandContext context, boolean regex, boolean caseSensitive) throws Exception {
        Manuscript manuscript = context.getScope().getManuscript();
        if (manuscript == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
        }
        Lorebook lorebook = manuscript.getLorebook() == null ? null
                : lorebookService.findForUser(manuscript.getLorebook().getUuid());
        if (lorebook == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_LOREBOOK));
        }
        String search = context.get("search", String.class);
        String replacement = context.get("replace", String.class);
        if (search == null || search.isEmpty()) {
            throw new IllegalArgumentException(loc.getValue(L.ERROR_SEARCH_TEXT_EMPTY));
        }
        if (replacement == null) {
            replacement = "";
        }

        context.setStatus(loc.getValue(L.MSG_COMMAND_SEARCHING));
        LorebookReplaceService.Plan plan;
        try {
            plan = replaceService.plan(lorebook, new LorebookReplaceService.Options(search, replacement, regex,
                    caseSensitive), SearchReplaceCommands.progress(context));
        } catch (CancellationException e) {
            return;
        } catch (InvalidSearchException e) {
            throw new IllegalArgumentException(String.format(loc.getValue(e.getKind() == InvalidSearchException.Kind.SEARCH
                    ? L.ERROR_INVALID_REGEX : L.ERROR_INVALID_REPLACEMENT), e.getMessage()), e);
        }
        if (context.isTerminated()) {
            return;
        }
        if (plan.items().isEmpty()) {
            context.setReturn(loc.getValue(L.MSG_SEARCH_NOTHING_FOUND));
            return;
        }

        // there is no preview, so the user decides on the numbers
        context.setStatus(null);
        context.setSubStatus(null);
        String question = String.format(loc.getValue(L.MSG_COMMAND_REPLACE_CONFIRM), plan.hitCount(),
                plan.items().size(), SearchReplaceCommands.shorten(search), SearchReplaceCommands.shorten(replacement));
        if (!confirm(context, question)) {
            if (!context.isTerminated()) {
                context.setReturn(loc.getValue(L.MSG_COMMAND_REPLACE_CANCELLED));
            }
            return;
        }

        // one transaction that is finished or rolled back as a whole, the user can't stop it half way
        context.setUninterruptable(true);
        context.setStatus(loc.getValue(L.MSG_COMMAND_REPLACING));
        LorebookReplaceService.Result result = replaceService.apply(lorebook, plan, SearchReplaceCommands.NO_STOP);
        CommandUtils.reloadManuscriptWindow();

        String message = String.format(loc.getValue(L.MSG_REPLACE_DONE), result.hits(), result.changed());
        context.setReturn(result.skipped() == 0 ? message
                : message + "\n" + String.format(loc.getValue(L.MSG_REPLACE_SKIPPED), result.skipped()));
    }

    /**
     * Asks the user whether to replace. A hook for tests, which have no browser to ask in.
     */
    protected boolean confirm(CommandContext context, String question) throws Exception {
        return context.confirm(question);
    }

}
