package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptReplaceService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch.InvalidSearchException;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
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
    static final String HUMAN_NAME = "Search and replace in the story";
    static final String DESCRIPTION = "Searches the parts of the story (and their summaries) of the open book and, "
            + "after you confirm, replaces the matches. Options after the replacement, in this order: regex (false), "
            + "case sensitive (true), search in summaries (true), search in all branches (false), revalidate all "
            + "summaries (true).";
    static final String LLM_DESCRIPTION = "Search and replace in the story text of the book the user has open: the "
            + "parts of the story and optionally their summaries, in the active branch or in all of them. Changes the "
            + "book, but the user is asked to confirm the number of matches first and the change can't be undone; "
            + "answering no changes nothing. Fails when no book is open. Asynchronous. Optional booleans, positional "
            + "and in this order: regex, caseSensitive, summaries, allBranches, revalidateSummaries.";
    static final String RETURN_DESCRIPTION = "What was done: how many matches in how many parts were replaced, or "
            + "that nothing was found or the user did not confirm";

    private static final String SEARCH = "The text to find (a regular expression with regex)";
    private static final String REPLACE = "The text to put in its place (may be empty; with regex it can use $1 groups)";
    private static final String REGEX = "Search is a regular expression, false when omitted";
    private static final String CASE_SENSITIVE = "Match the case of the letters, true when omitted; has no effect with regex";
    private static final String SUMMARIES = "Search in the summaries too, true when omitted";
    private static final String ALL_BRANCHES = "Search in all the branches, not only the active one; false when omitted";
    private static final String REVALIDATE = "Work out the hash of all the summaries again so none becomes outdated; true when omitted";

    @Autowired
    private ManuscriptReplaceService replaceService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    @Command(commandName = NAME, humanName = HUMAN_NAME, async = true, description = DESCRIPTION,
            llmDescription = LLM_DESCRIPTION,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = SEARCH),
                    @CommandParameter(name = "replace", type = String.class, description = REPLACE)}),
            returns = @CommandReturn(name = "message", type = String.class, description = RETURN_DESCRIPTION))
    public void replace(CommandContext context) throws Exception {
        run(context, false, true, true, false, true);
    }

    @Command(commandName = NAME, humanName = HUMAN_NAME, async = true, description = DESCRIPTION,
            llmDescription = LLM_DESCRIPTION,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = SEARCH),
                    @CommandParameter(name = "replace", type = String.class, description = REPLACE),
                    @CommandParameter(name = "regex", type = Boolean.class, description = REGEX)}),
            returns = @CommandReturn(name = "message", type = String.class, description = RETURN_DESCRIPTION))
    public void replaceRegex(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), true, true, false, true);
    }

    @Command(commandName = NAME, humanName = HUMAN_NAME, async = true, description = DESCRIPTION,
            llmDescription = LLM_DESCRIPTION,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = SEARCH),
                    @CommandParameter(name = "replace", type = String.class, description = REPLACE),
                    @CommandParameter(name = "regex", type = Boolean.class, description = REGEX),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = CASE_SENSITIVE)}),
            returns = @CommandReturn(name = "message", type = String.class, description = RETURN_DESCRIPTION))
    public void replaceCase(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class), true, false, true);
    }

    @Command(commandName = NAME, humanName = HUMAN_NAME, async = true, description = DESCRIPTION,
            llmDescription = LLM_DESCRIPTION,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = SEARCH),
                    @CommandParameter(name = "replace", type = String.class, description = REPLACE),
                    @CommandParameter(name = "regex", type = Boolean.class, description = REGEX),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = CASE_SENSITIVE),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = SUMMARIES)}),
            returns = @CommandReturn(name = "message", type = String.class, description = RETURN_DESCRIPTION))
    public void replaceSummaries(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), false, true);
    }

    @Command(commandName = NAME, humanName = HUMAN_NAME, async = true, description = DESCRIPTION,
            llmDescription = LLM_DESCRIPTION,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = SEARCH),
                    @CommandParameter(name = "replace", type = String.class, description = REPLACE),
                    @CommandParameter(name = "regex", type = Boolean.class, description = REGEX),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = CASE_SENSITIVE),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = SUMMARIES),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = ALL_BRANCHES)}),
            returns = @CommandReturn(name = "message", type = String.class, description = RETURN_DESCRIPTION))
    public void replaceBranches(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class), true);
    }

    @Command(commandName = NAME, humanName = HUMAN_NAME, async = true, description = DESCRIPTION,
            llmDescription = LLM_DESCRIPTION,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "search", type = String.class, description = SEARCH),
                    @CommandParameter(name = "replace", type = String.class, description = REPLACE),
                    @CommandParameter(name = "regex", type = Boolean.class, description = REGEX),
                    @CommandParameter(name = "caseSensitive", type = Boolean.class, description = CASE_SENSITIVE),
                    @CommandParameter(name = "summaries", type = Boolean.class, description = SUMMARIES),
                    @CommandParameter(name = "allBranches", type = Boolean.class, description = ALL_BRANCHES),
                    @CommandParameter(name = "revalidateSummaries", type = Boolean.class, description = REVALIDATE)}),
            returns = @CommandReturn(name = "message", type = String.class, description = RETURN_DESCRIPTION))
    public void replaceAll(CommandContext context) throws Exception {
        run(context, context.get("regex", Boolean.class), context.get("caseSensitive", Boolean.class),
                context.get("summaries", Boolean.class), context.get("allBranches", Boolean.class),
                context.get("revalidateSummaries", Boolean.class));
    }

    private void run(CommandContext context, boolean regex, boolean caseSensitive, boolean summaries,
                     boolean allBranches, boolean revalidateSummaries) throws Exception {
        Manuscript open = CommandUtils.getCurrentManuscript();
        // the window's copy can be older than the stored book
        Manuscript manuscript = open == null ? null : manuscriptService.findForUser(open.getUuid());
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

        // there is no preview, so the user decides on the numbers
        context.setStatus(null);
        context.setSubStatus(null);
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
     * Asks the user whether to replace. A hook for tests, which have no browser to ask in.
     */
    protected boolean confirm(CommandContext context, String question) throws Exception {
        return CommandUtils.confirm(context, question);
    }

}
