package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
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
 * {@code /search-replace-parts search replace [regex [caseSensitive [summaries [allBranches [revalidateSummaries]]]]]}:
 * the search and replace dialog of the story as a command, for the book that is open. The options that are left out
 * have the defaults of the dialog: no regex, case sensitive, summaries searched, only the active branch, summaries
 * revalidated. The options are positional, so to set one the ones before it are given too.
 * <p>
 * It is {@code async} and it asks first: the command bar shows no preview of the matches, so after the search the user
 * gets a yes/no question with the number of matches (and the warning about the summaries that would become outdated).
 * Only an answer of yes replaces; the replace itself can't be stopped, see {@link CommandContext#setUninterruptable}.
 * It works only with a book window open.
 */
@Configurable
public class SearchReplaceChatMessageCommand {

    static final String NAME = "search-replace-parts";

    @Autowired
    private ManuscriptReplaceService replaceService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replace(CommandContext context) throws Exception {
        run(context, false, true, true, false, true, null, false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceRegex(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), true, true, false, true, null, false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceCase(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class), true, false, true, null, false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_SUMMARIES_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceSummaries(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), false, true, null, false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_SUMMARIES_PARAM),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = L.DESC_COMMAND_ALL_BRANCHES_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceBranches(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class), true, null, false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_SUMMARIES_PARAM),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = L.DESC_COMMAND_ALL_BRANCHES_PARAM),
                    @CommandParameter(name = "revalidateSummaries", type = Boolean.class, description = L.DESC_COMMAND_REVALIDATE_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceAll(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class),
                context.get("revalidateSummaries", Boolean.class), null, false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_SUMMARIES_PARAM),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = L.DESC_COMMAND_ALL_BRANCHES_PARAM),
                    @CommandParameter(name = "revalidateSummaries", type = Boolean.class, description = L.DESC_COMMAND_REVALIDATE_PARAM),
                    @CommandParameter(name = "maxHits", type = Integer.class, description = L.DESC_COMMAND_MAX_HITS_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceMaxHits(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class),
                context.get("revalidateSummaries", Boolean.class), context.get("maxHits", Integer.class), false);
    }

    @Command(commandName = NAME, humanName = L.LABEL_COMMAND_SEARCH_REPLACE_STORY, async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            description = L.DESC_COMMAND_SEARCH_REPLACE_STORY, llmDescription = L.DESC_COMMAND_SEARCH_REPLACE_STORY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = L.DESC_COMMAND_SEARCH_PARAM),
                    @CommandParameter(name = "replace", type = String.class, description = L.DESC_COMMAND_REPLACE_PARAM),
                    @CommandParameter(name = "regex", type = Boolean.class, description = L.DESC_COMMAND_REGEX_PARAM),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = L.DESC_COMMAND_CASE_SENSITIVE_PARAM),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = L.DESC_COMMAND_SUMMARIES_PARAM),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = L.DESC_COMMAND_ALL_BRANCHES_PARAM),
                    @CommandParameter(name = "revalidateSummaries", type = Boolean.class, description = L.DESC_COMMAND_REVALIDATE_PARAM),
                    @CommandParameter(name = "maxHits", type = Integer.class, description = L.DESC_COMMAND_MAX_HITS_PARAM),
                    @CommandParameter(name = "dryRun", type = Boolean.class, description = L.DESC_COMMAND_DRY_RUN_PARAM)}),
            returns = @CommandReturn(name = "message", type = String.class, description = L.DESC_COMMAND_SEARCH_REPLACE_STORY_RETURN))
    public void replaceDryRun(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class),
                context.get("revalidateSummaries", Boolean.class), context.get("maxHits", Integer.class),
                context.get("dryRun", Boolean.class));
    }

    private void run(CommandContext context, boolean regex, boolean caseSensitive, boolean summaries,
                     boolean allBranches, boolean revalidateSummaries, Integer maxHits, boolean dryRun) throws Exception {
        Manuscript manuscript = context.getScope().getManuscript();
        if (manuscript == null) {
            throw new IllegalStateException(loc.getValue(L.ERROR_COMMAND_NO_BOOK_OPEN));
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
        ManuscriptReplaceService.Plan plan;
        try {
            plan = replaceService.plan(manuscript, new ManuscriptReplaceService.Options(search, replacement, regex,
                    caseSensitive, summaries, allBranches, revalidateSummaries), SearchReplaceCommands.progress(context));
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

        context.setStatus(null);
        context.setSubStatus(null);
        if (dryRun) {
            // only the numbers and a few matches, to decide whether the replace is wanted
            String report = String.format(loc.getValue(L.MSG_COMMAND_REPLACE_DRY_RUN), plan.hitCount(), plan.items().size());
            if (plan.outdatedSummaries() > 0) {
                report += "\n" + String.format(loc.getValue(L.MSG_REPLACE_OUTDATED_SUMMARIES), plan.outdatedSummaries());
            }
            context.setReturn(report + SearchReplaceCommands.samples(plan.items().stream().flatMap(item -> item.hits().stream()).toList()));
            return;
        }
        if (maxHits != null && plan.hitCount() > maxHits) {
            throw new IllegalArgumentException(String.format(loc.getValue(L.ERROR_COMMAND_TOO_MANY_HITS), plan.hitCount(), maxHits));
        }
        // there is no preview, so the user decides on the numbers
        String question = String.format(loc.getValue(L.MSG_COMMAND_REPLACE_CONFIRM), plan.hitCount(),
                plan.items().size(), SearchReplaceCommands.shorten(search), SearchReplaceCommands.shorten(replacement));
        if (plan.outdatedSummaries() > 0) {
            question += "\n\n" + String.format(loc.getValue(L.MSG_REPLACE_OUTDATED_SUMMARIES), plan.outdatedSummaries());
        }
        if (!confirm(context, question)) {
            if (!context.isTerminated()) {
                context.setReturn(loc.getValue(L.MSG_COMMAND_REPLACE_CANCELLED));
            }
            return;
        }

        // one transaction that is finished or rolled back as a whole, the user can't stop it half way
        context.setUninterruptable(true);
        context.setStatus(loc.getValue(L.MSG_COMMAND_REPLACING));
        ManuscriptReplaceService.Result result = replaceService.apply(manuscript, plan, SearchReplaceCommands.NO_STOP);
        CommandUtils.reloadManuscriptWindow();

        String message = String.format(loc.getValue(L.MSG_REPLACE_DONE), result.hits(), result.changed());
        context.setReturn(result.skipped() == 0 ? message
                : message + "\n" + String.format(loc.getValue(L.MSG_REPLACE_SKIPPED), result.skipped()));
    }

    /**
     * Asks whoever called the command whether to replace. A hook for tests, which have no browser to ask in.
     */
    protected boolean confirm(CommandContext context, String question) throws Exception {
        return context.confirm(question);
    }

}
