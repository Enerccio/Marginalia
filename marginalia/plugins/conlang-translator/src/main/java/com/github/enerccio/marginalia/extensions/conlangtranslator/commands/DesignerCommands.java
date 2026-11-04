package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerAnswer;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerPrompts;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.DesignerStage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.designer.Proposal;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.Change;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RuleSection;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.RunLanguage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.DesignerService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.HeldProposals;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.TranslationPrompt.Request;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The designer as commands: the model proposes words, a section of the rules, a rewrite, a stage of a new language, or answers a question; a review
 * and the sample sentences are read only. A proposal is a set of changes against the language ({@link ChangeSet}). With {@code ask true} the command
 * returns the changes as a numbered list and holds them, {@code conlang-apply-changes} applies the numbers the caller chooses; without it the
 * proposal is applied at once (what the designer window does when the author clicks <i>Apply</i> on all). Inside an agent run the changes go into the
 * run's copy of the language. The commands that take over what a run left in a book are here too.
 */
public class DesignerCommands extends ConlangCommandSupport {

    public static final String DESIGN_WORDS = "conlang-design-words";
    public static final String DESIGN_RULES = "conlang-design-rules";
    public static final String REWRITE_RULES = "conlang-rewrite-rules";
    public static final String DESIGN_STAGE = "conlang-design-stage";
    public static final String ASK_DESIGNER = "conlang-ask-designer";
    public static final String REVIEW = "conlang-review";
    public static final String TRY_SENTENCES = "conlang-try-sentences";
    public static final String APPLY_CHANGES = "conlang-apply-changes";
    public static final String PENDING_CHANGES = "conlang-pending-changes";
    public static final String TAKE_PENDING = "conlang-take-pending-changes";
    public static final String DISCARD_PENDING = "conlang-discard-pending-changes";

    private final DesignerService designer;

    public DesignerCommands(DesignerService designer) {
        this.designer = designer;
    }

    // ---- proposals

    @Command(commandName = DESIGN_WORDS, linkedCommand = APPLY_CHANGES, humanNameText = "Design words", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "The designer invents new words for a language.",
            llmDescriptionText = "Asks the designer model for count new words (for a category, or any), each with a meaning and a rationale (the roots and the rule), consistent with the sounds, the spelling and the words that exist. "
                    + "A category that does not exist yet is made. Without ask the words are added to the lexicon as new; with ask true you get the numbered changes and conlang-apply-changes adds the ones you choose.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "category", type = String.class, optional = true, descriptionText = "The category of the words (its name; a new name makes one); any when left out"),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, defaultValue = "10", min = "1", max = "60", descriptionText = "How many words"),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was added, or the numbered changes"))
    public void designWords(CommandContext context) throws Exception {
        Language language = language(context);
        Integer count = context.get("count", Integer.class);
        Manuscript book = scopeBook(context);
        Request request = DesignerPrompts.words(language, context.get("category", String.class), count == null ? 10 : count,
                StringUtils.defaultString(context.get("note", String.class)), designer.lore(language));
        propose(context, book, language, designer.propose(language, request));
    }

    @Command(commandName = DESIGN_RULES, linkedCommand = APPLY_CHANGES, humanNameText = "Design a rules section", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "The designer writes a new section of the rules, and the words it needs.",
            llmDescriptionText = "Asks the designer model for a new rules section about the topic (a numbering system, colors, politeness, a kinship system...) and the words the section needs. "
                    + "Without ask it is added; with ask true you get the numbered changes and conlang-apply-changes adds the ones you choose.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "topic", type = String.class, descriptionText = "What the section is about, in your own words"),
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was added, or the numbered changes"))
    public void designRules(CommandContext context) throws Exception {
        Language language = language(context);
        Request request = DesignerPrompts.section(language, required(context.get("topic", String.class), "the topic"), designer.lore(language));
        propose(context, scopeBook(context), language, designer.propose(language, request));
    }

    @Command(commandName = REWRITE_RULES, linkedCommand = APPLY_CHANGES, humanNameText = "Rewrite a rules section", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "The designer rewrites a section of the rules as asked.",
            llmDescriptionText = "Asks the designer model to rewrite a rules section as the note says; it keeps what is right. Without ask the section is replaced (the old text is lost: read it first with conlang-get-rules "
                    + "if you may need it); with ask true you get the change and conlang-apply-changes applies it.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "section", type = String.class, descriptionText = PARAM_SECTION),
                    @CommandParameter(name = "note", type = String.class, descriptionText = "What to change"),
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was changed, or the numbered changes"))
    public void rewriteRules(CommandContext context) throws Exception {
        Language language = language(context);
        RuleSection section = section(language, context.get("section", String.class));
        Request request = DesignerPrompts.rewrite(language, section, required(context.get("note", String.class), "what to change"));
        propose(context, scopeBook(context), language, designer.propose(language, request));
    }

    @Command(commandName = DESIGN_STAGE, linkedCommand = APPLY_CHANGES, humanNameText = "Design a stage of a language", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "The designer proposes one stage of a new language: sounds, writing, grammar or word formation.",
            llmDescriptionText = "Makes a new language one stage at a time: sounds, writing, grammar, word-formation, each a rules section the designer proposes from the brief and the earlier sections. "
                    + "Make the language with conlang-create, then call the stages in this order, each time with the brief (what the language is for: the people, their world, inspirations). "
                    + "Without ask the section is added; with ask true you get the numbered change first. Call again with a note to refine the section the stage made.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "stage", type = String.class, allowed = {"sounds", "writing", "grammar", "word-formation"}, descriptionText = "The stage"),
                    @CommandParameter(name = "brief", type = String.class, optional = true, descriptionText = "What the language is for: the people, their world, inspirations"),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was added, or the numbered changes"))
    public void designStage(CommandContext context) throws Exception {
        Language language = language(context);
        DesignerStage stage = stage(context.get("stage", String.class));
        String previous = language.getRules().stream().filter(section -> section.getTitle().equalsIgnoreCase(stage.title())).map(RuleSection::getText)
                .findFirst().orElse("");
        Request request = DesignerPrompts.stage(language, stage, StringUtils.defaultString(context.get("brief", String.class)), previous,
                StringUtils.defaultString(context.get("note", String.class)), designer.lore(language));
        propose(context, scopeBook(context), language, designer.propose(language, request));
    }

    private static DesignerStage stage(String text) {
        String wanted = StringUtils.defaultString(text).strip().toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
        return switch (wanted) {
            case "sounds" -> DesignerStage.SOUNDS;
            case "writing" -> DesignerStage.WRITING;
            case "grammar" -> DesignerStage.GRAMMAR;
            case "word-formation", "wordformation" -> DesignerStage.WORD_FORMATION;
            default -> throw new IllegalArgumentException("The stage is sounds, writing, grammar or word-formation, not " + text);
        };
    }

    @Command(commandName = ASK_DESIGNER, linkedCommand = APPLY_CHANGES, humanNameText = "Ask the designer", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Asks the designer a question about the language; it may offer changes.",
            llmDescriptionText = "Asks the designer model a free question about the language (\"which words are missing for 'my brother died at the gate'?\"). Returns its answer. When the answer comes with "
                    + "rules or words to add, they are held as numbered changes that conlang-apply-changes applies; nothing is written by this command.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "question", type = String.class, descriptionText = "The question")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the answer and the changes offered"))
    public void askDesigner(CommandContext context) throws Exception {
        Language language = language(context);
        Request request = DesignerPrompts.ask(language, required(context.get("question", String.class), "the question"), List.of(), designer.lore(language));
        DesignerAnswer answer = designer.answer(request);
        JsonObject result = new JsonObject();
        result.addProperty("answer", answer.message());
        if (answer.hasChanges()) {
            Proposal proposal = designer.proposal(language, answer);
            hold(context, scopeBook(context), language, proposal);
            result.add("changes", changeRows(proposal.set()));
            result.addProperty("hint", "conlang-apply-changes applies them (pass the numbers to apply only some)");
        }
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = REVIEW, linkedCommand = ASK_DESIGNER, humanNameText = "Review a language", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The designer reviews the rules and the lexicon for contradictions and gaps. Reads only.",
            llmDescriptionText = "Asks the designer model to review the whole language: contradictions between sections, things the rules promise and do not define, words that break the sound system or the spelling, "
                    + "two words for one meaning, gaps a translator will hit. Returns findings (severity, where, problem, fix). Changes nothing: fix with the update commands. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the findings"))
    public void review(CommandContext context) throws Exception {
        DesignerAnswer answer = designer.answer(DesignerPrompts.review(language(context)));
        JsonObject result = new JsonObject();
        result.addProperty("summary", answer.message());
        JsonArray findings = new JsonArray();
        for (DesignerAnswer.Finding finding : answer.findings()) {
            JsonObject row = new JsonObject();
            row.addProperty("severity", StringUtils.defaultString(finding.severity()));
            row.addProperty("where", StringUtils.defaultString(finding.where()));
            row.addProperty("problem", StringUtils.defaultString(finding.problem()));
            row.addProperty("fix", StringUtils.defaultString(finding.fix()));
            findings.add(row);
        }
        result.add("findings", findings);
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = TRY_SENTENCES, linkedCommand = APPLY_CHANGES, humanNameText = "Try sentences", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The designer translates sample sentences with the new language, to see how it works. Reads only.",
            llmDescriptionText = "Asks the designer model to translate the sentences (one per line) with the language, to test it. Returns the translations with notes on the hard choices. Words the lexicon lacks "
                    + "that the sentences needed are held as numbered changes for conlang-apply-changes; nothing is written by this command.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "sentences", type = String.class, descriptionText = "The English sentences, one per line")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the translations"))
    public void trySentences(CommandContext context) throws Exception {
        Language language = language(context);
        List<String> sentences = Arrays.stream(required(context.get("sentences", String.class), "the sentences").split("\\R")).filter(line -> !line.isBlank()).toList();
        DesignerAnswer answer = designer.answer(DesignerPrompts.samples(language, sentences));
        JsonObject result = new JsonObject();
        result.addProperty("message", answer.message());
        JsonArray samples = new JsonArray();
        for (DesignerAnswer.Sample sample : answer.samples()) {
            JsonObject row = new JsonObject();
            row.addProperty("english", StringUtils.defaultString(sample.source()));
            row.addProperty("translation", StringUtils.defaultString(sample.text()));
            row.addProperty("note", StringUtils.defaultString(sample.note()));
            samples.add(row);
        }
        result.add("samples", samples);
        if (!answer.words().isEmpty()) {
            Proposal proposal = designer.proposal(language, answer);
            hold(context, scopeBook(context), language, proposal);
            result.add("newWords", changeRows(proposal.set()));
        }
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = APPLY_CHANGES, linkedCommand = PENDING_CHANGES, humanNameText = "Apply the designer's changes", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Applies the changes that a designer command (ask true) held.",
            llmDescriptionText = "Applies the changes held by the last designer command for the language that was called with ask true (or by conlang-ask-designer, conlang-try-sentences): all of them, or the numbers you give "
                    + "separated by commas (ranges as 2-4). A change that does not fit the language any more (it changed meanwhile) is skipped and counted. Returns what was applied.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "changes", type = String.class, optional = true, descriptionText = "The numbers of the changes to apply, separated by commas; all of them when left out")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was applied"))
    public void applyChanges(CommandContext context) throws Exception {
        Language language = language(context);
        Manuscript book = scopeBook(context);
        Proposal proposal = HeldProposals.take(place(book), language.getId());
        if (proposal == null) {
            throw new IllegalStateException("No changes are held for " + language.getName() + "; call a designer command with ask true first");
        }
        Set<Integer> numbers = numbers(context.get("changes", String.class), proposal.set().changes().size());
        if (numbers != null) {
            proposal.set().pickNone();
            List<Change> changes = proposal.set().changes();
            for (int number : numbers) {
                proposal.set().pick(changes.get(number - 1), true);
            }
        }
        context.setReturn(GSON.toJson(applied(book, language, proposal)));
    }

    // ---- what a run left in a book

    @Command(commandName = PENDING_CHANGES, linkedCommand = TAKE_PENDING, humanNameText = "Language changes left by a run", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The changes to languages that an agent run left in the book, not taken over into the library yet. Reads only.",
            llmDescriptionText = "A run works on a copy of the languages it changes. When its result is applied to the book the copy stays in the book until the user takes it over. This lists, for each such language, the "
                    + "changes the run made as numbered changes, compared with the library as it is now (what the user changed meanwhile is a conflict). conlang-take-pending-changes takes them over. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the pending languages"))
    public void pending(CommandContext context) throws Exception {
        Manuscript book = scopeBook(context);
        if (book == null) {
            throw new IllegalStateException("No book is open");
        }
        JsonArray list = new JsonArray();
        for (Map.Entry<String, RunLanguage> entry : languages.run(book).entrySet()) {
            JsonObject row = new JsonObject();
            row.addProperty("language", entry.getValue().getOurs().getName());
            ChangeSet set = languages.changes(book, entry.getKey());
            if (set == null) {
                row.addProperty("new", true);
                row.addProperty("words", entry.getValue().getOurs().getLexicon().size());
            } else {
                row.add("changes", changeRows(set));
            }
            list.add(row);
        }
        JsonObject result = new JsonObject();
        result.add("languages", list);
        answer(context, result);
    }

    @Command(commandName = TAKE_PENDING, linkedCommand = PENDING_CHANGES, humanNameText = "Take over the changes of a run", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Takes the changes an agent run made to a language into the library.",
            llmDescriptionText = "Writes the changes a run left in the book into the language of the library: all of them that are not in conflict, or the numbers from conlang-pending-changes. A language the library lacks is added whole. "
                    + "Asks the user. Not available inside an agent run.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "changes", type = String.class, optional = true, descriptionText = "The numbers of the changes to take, separated by commas; all that are not in conflict when left out")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was taken"))
    public void takePending(CommandContext context) throws Exception {
        Manuscript book = scopeBook(context);
        if (book == null || languages.inRun(book)) {
            throw new IllegalStateException("The changes of a run are taken over from the book, outside a run");
        }
        String id = pendingId(book, context.get("language", String.class));
        ChangeSet set = languages.changes(book, id);
        Set<Integer> numbers = set == null ? null : numbers(context.get("changes", String.class), set.changes().size());
        if (!context.confirm("Take the changes of the run to the language into your library?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was taken");
            }
            return;
        }
        if (set != null) {
            if (numbers == null) {
                set.pickClean();
            } else {
                set.pickNone();
                for (int number : numbers) {
                    set.pick(set.changes().get(number - 1), true);
                }
            }
        }
        LanguageDiff.Result result = languages.takeOver(book, id, set);
        JsonObject row = new JsonObject();
        row.addProperty("applied", result.applied().size());
        row.addProperty("skippedChangedMeanwhile", result.stale().size());
        row.addProperty("skippedNeedsAnother", result.unmet().size());
        context.setReturn(GSON.toJson(row));
    }

    @Command(commandName = DISCARD_PENDING, linkedCommand = PENDING_CHANGES, humanNameText = "Discard the changes of a run", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Forgets the changes an agent run made to a language.",
            llmDescriptionText = "Drops the run's version of the language from the book without taking anything into the library. Destructive, asks the user. Not available inside an agent run.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was dropped"))
    public void discardPending(CommandContext context) throws Exception {
        Manuscript book = scopeBook(context);
        if (book == null || languages.inRun(book)) {
            throw new IllegalStateException("The changes of a run are discarded from the book, outside a run");
        }
        String id = pendingId(book, context.get("language", String.class));
        if (!context.confirm("Forget the changes of the run to the language?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was dropped");
            }
            return;
        }
        languages.forget(book, id);
        context.setReturn("Dropped the changes of the run");
    }

    private String pendingId(Manuscript book, String reference) throws Exception {
        Map<String, RunLanguage> run = languages.run(book);
        if (run.isEmpty()) {
            throw new IllegalStateException("The book holds no changes of a run");
        }
        String wanted = StringUtils.defaultString(reference).strip();
        for (Map.Entry<String, RunLanguage> entry : run.entrySet()) {
            if (entry.getKey().equals(wanted) || entry.getValue().getOurs().getName().strip().equalsIgnoreCase(wanted)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("The run left no changes of " + wanted + ". It changed: "
                + String.join(", ", run.values().stream().map(language -> language.getOurs().getName()).toList()));
    }

    // ---- helpers

    private static String place(Manuscript book) {
        return book == null ? "user" : book.getUuid();
    }

    private void hold(CommandContext context, Manuscript book, Language language, Proposal proposal) {
        HeldProposals.hold(place(book), language.getId(), proposal);
    }

    /**
     * Applies the proposal at once, or holds it and returns the numbered changes when the command was called with {@code ask true}.
     */
    private void propose(CommandContext context, Manuscript book, Language language, Proposal proposal) throws Exception {
        JsonObject result;
        if (Boolean.TRUE.equals(context.get("ask", Boolean.class))) {
            hold(context, book, language, proposal);
            result = new JsonObject();
            if (!proposal.message().isBlank()) {
                result.addProperty("message", proposal.message());
            }
            result.add("changes", changeRows(proposal.set()));
            if (!proposal.dropped().isEmpty()) {
                result.add("leftOut", array(proposal.dropped()));
            }
            result.addProperty("hint", "conlang-apply-changes applies them (pass the numbers to apply only some)");
        } else {
            result = applied(book, language, proposal);
        }
        context.setReturn(GSON.toJson(result));
    }

    private JsonObject applied(Manuscript book, Language language, Proposal proposal) throws Exception {
        LanguageDiff.Result[] done = new LanguageDiff.Result[1];
        languages.update(book, language.getId(), stored -> done[0] = LanguageDiff.apply(stored, proposal.set()));
        JsonObject result = new JsonObject();
        if (!proposal.message().isBlank()) {
            result.addProperty("message", proposal.message());
        }
        result.addProperty("applied", done[0].applied().size());
        if (!done[0].stale().isEmpty()) {
            result.addProperty("skippedChangedMeanwhile", done[0].stale().size());
        }
        if (!done[0].unmet().isEmpty()) {
            result.addProperty("skippedNeedsAnother", done[0].unmet().size());
        }
        if (!proposal.dropped().isEmpty()) {
            result.add("leftOut", array(proposal.dropped()));
        }
        Language now = languages.find(book, language.getId());
        if (now != null) {
            result.add("language", languageRow(now));
        }
        return result;
    }
}
