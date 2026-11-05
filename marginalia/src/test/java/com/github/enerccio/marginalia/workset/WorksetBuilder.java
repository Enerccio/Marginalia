package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.TestContextPostProcessor;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.github.enerccio.marginalia.utils.AppClock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Builds the dataset (see {@code TODO.DATASET.MD}): runs the real generation loop of the application over the skeleton of the story, a part at a
 * time, with a model behind a bridge, and exports the book. <b>A build tool, not a test:</b> it does nothing unless {@code -Dworkset.build=true},
 * and its name keeps it out of the normal suite.
 * <pre>
 *   mvn test -Dtest=WorksetBuilder -Dworkset.build=true [-Dworkset.parts=5] [-Dworkset.until=40] [-Dworkset.author=fixed|claude]
 * </pre>
 * <b>Resumable.</b> The database of the application lives in {@code &lt;dir&gt;/home} and stays between runs; the user, the AI, the protocol, the lorebook
 * and the book are found again by name; the next part is the length of the active branch plus one. Every answer of the model is stored by the hash
 * of its request ({@code answers/}), so a part whose request did not change is never asked again, and a run that died in the middle continues
 * where it stopped (a part that failed is removed by the application itself). A run does {@code workset.parts} parts (5) and stops, to be
 * looked at: {@code review/run-N.md}, {@code usage.jsonl}, {@code export/book.json}.
 * <p>
 * Properties: {@code workset.dir} (work folder, default {@code ../work/harness}), {@code workset.skeleton} (folder of {@code beats.json} and
 * {@code lorebook.json}, default {@code ../work/dataset}), {@code workset.parts}, {@code workset.until} (stop after this part number),
 * {@code workset.author} ({@code fixed}: a fixed text, no model; {@code claude}: the {@code claude} tool, default {@code fixed}),
 * {@code workset.model} (default haiku), {@code workset.start} (date of day 1, default 2026-01-05), {@code workset.lengthHint} (default true), {@code workset.lengthFactor} (the asked length times this, default 0.75: Haiku writes longer than asked).
 */
@EnabledIfSystemProperty(named = "workset.build", matches = "true")
class WorksetBuilder extends MarginaliaTestBase {

    private static final String USER = "author";
    private static final String AI_NAME = "Workset author";
    private static final String PROTOCOL_NAME = "Workset protocol";
    private static final String LOREBOOK_NAME = "Unregistered - the valley";
    /** The author's "What to look for" of the annotations (verbatim). */
    private static final String ANNOTATE_INSTRUCTIONS = """
            1. POV Drift - Always keep POV character as defined (if multiple characters are defined then it can switch between them)
            2. Continuity errors - compare with lorebook and previous messages even if instructions for the turn have mistake against the lore
            3. Style error - Compare text with style guidelines to find weak cliches and bad style choices""";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static {
        if ("true".equals(System.getProperty("workset.build")) && System.getProperty(TestContextPostProcessor.FIXED_HOME_PROPERTY) == null) {
            System.setProperty(TestContextPostProcessor.FIXED_HOME_PROPERTY,
                    Path.of(System.getProperty("workset.dir", "../work/harness"), "home").toAbsolutePath().normalize().toString());
        }
    }

    @Autowired
    private StoryGenerationService storyGenerationService;
    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ProtocolService protocolService;
    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private MessagePromptService messagePromptService;
    @Autowired
    private AnnotateService annotateService;
    @Autowired
    private RedraftService redraftService;
    @Autowired
    private AnnotationService annotationService;
    @Autowired
    private CommandService commandService;
    @Autowired
    private CommandScopes commandScopes;
    @Autowired
    private ExtensionService extensionService;

    private Path dir;
    private Skeleton skeletonForReview;
    private Ledger ledger;
    private AnswerCache cache;
    private Author author;
    private SteppingClock clock;
    private int run;
    private String current = "";
    private volatile String bridgeError;
    private volatile String kind = "part";
    private volatile Ledger.Line lastLine;
    /** The review: for each part (by label) the steps the loop did on it, in order, each as a block of markdown. */
    private final java.util.Map<String, List<String>> audit = new java.util.LinkedHashMap<>();
    private Set<String> steps = Set.of("generate");
    /** The plugins started by hand (see {@link PluginHost}) and the translators among them, in the order the pass runs them. */
    private PluginHost plugins;
    private List<String> translators = List.of();
    private TranslationPass translation;
    private KnowledgePass knowledge;
    private BlockEnd blockEnd;
    private long summaryBudget;
    private Set<String> translateAll = Set.of();
    /** The parts up to this position are translated into every language, whatever their beats say (a smoke test of all four languages). */
    private int translateAllUntil = 1;

    @BeforeEach
    void keepClockHonest() {
        AppClock.reset();
    }

    @AfterEach
    void resetClock() {
        AppClock.reset();
        if (plugins != null) {
            plugins.unload();
        }
    }

    @Test
    void build() throws Exception {
        dir = Path.of(System.getProperty("workset.dir", "../work/harness")).toAbsolutePath().normalize();
        Path skeletonDir = Path.of(System.getProperty("workset.skeleton", "../work/dataset")).toAbsolutePath().normalize();
        int partsThisRun = Integer.getInteger("workset.parts", 5);
        int until = Integer.getInteger("workset.until", Integer.MAX_VALUE);
        // asking the model for a number of words is pointless (it writes what the scene needs); it stays only for the parts that test the length badge
        boolean lengthHint = "true".equals(System.getProperty("workset.lengthHint"));
        Set<Integer> lengthHintParts = new java.util.HashSet<>();
        for (String number : System.getProperty("workset.lengthHintParts", "").split(",")) {
            if (!number.isBlank()) {
                lengthHintParts.add(Integer.parseInt(number.strip()));
            }
        }
        double lengthFactor = Double.parseDouble(System.getProperty("workset.lengthFactor", "0.75"));
        LocalDate start = LocalDate.parse(System.getProperty("workset.start", "2026-01-05"));
        steps = Set.of(System.getProperty("workset.steps", "generate,annotate,redraft").split(","));
        // the translator plugins asked for every part whatever the beat says: "false" (default: gated by the beat codes), "conlang", "conlang,reallang"/"true" (all)
        String ungated = System.getProperty("workset.translate.all", "false").strip();
        translateAllUntil = Integer.getInteger("workset.translate.allUntil", 1);
        translateAll = ungated.equals("true") ? Set.of("conlang", "reallang") : ungated.equals("false") ? Set.of() : Set.of(ungated.split("\\s*,\\s*"));
        // the plugins: by default the translators when the loop translates, none otherwise (the knowledge tracker is opt-in: it needs its own set-up)
        String defaultPlugins = (steps.contains("knowledge") ? "knowledge," : "") + (steps.contains("translate") ? "conlang,reallang," : "") + (steps.contains("blockend") ? "arcoutline" : "");
        List<String> wantedPlugins = List.of(System.getProperty("workset.plugins", defaultPlugins).split(",")).stream()
                .map(String::strip).filter(name -> !name.isEmpty()).toList();
        int blockSize = Integer.getInteger("workset.blockSize", 25);
        int summaryMinWords = Integer.getInteger("workset.summary.minWords", 800);

        Files.createDirectories(dir);
        Skeleton skeleton = Skeleton.load(skeletonDir.resolve("beats.json"));
        skeletonForReview = skeleton;
        ledger = new Ledger(dir.resolve("usage.jsonl"));
        cache = new AnswerCache(dir.resolve("answers"));
        run = ledger.nextRun();
        String authorKind = System.getProperty("workset.author", "fixed");
        author = switch (authorKind) {
            case "fixed" -> new FixedAuthor();
            case "claude" -> new ClaudeCliAuthor(System.getProperty("workset.model", "haiku"), dir.resolve("cli-cwd"), 20);
            default -> throw new IllegalArgumentException("workset.author is fixed or claude, not " + authorKind);
        };

        // ---- the setup, found again by name when a run before made it
        User user = userService.findByName(USER);
        if (user == null) {
            user = createUser(USER, DEFAULT_PASSWORD, false);
        }
        loginAs(user);
        llm.respondWith(this::bridge);
        clock = new SteppingClock(skeleton.beats.get(0).at(start));
        AppClock.set(clock);

        OpenAICompatible ai = findAi();
        if (ai == null) {
            ai = new OpenAICompatible();
            ai.setAiType(com.github.enerccio.marginalia.domain.collections.AIType.OPEN_AI_COMPATIBLE);
            ai.setName(AI_NAME);
            ai.setApiKey("workset");
            ai.setModel(com.github.enerccio.marginalia.test.llm.MockLLMScenario.DEFAULT_MODEL);
            ai.setModelName(com.github.enerccio.marginalia.test.llm.MockLLMScenario.DEFAULT_MODEL);
            ai.setNeedsJailbreak(false);
            ai.setEnabledReasoning(false);
            ai.setMaxContext(200000);
            ai.setMaxCompletionTokens(4000);
        }
        ai.setUri(llm.baseUrl()); // the port of the fake model server is new in every run
        ai = (OpenAICompatible) aiService.save(ai);

        Protocol protocol = protocolService.findAllForUser().stream().filter(p -> PROTOCOL_NAME.equals(p.getName())).findFirst().orElse(null);
        if (protocol == null) {
            ChatCompletionProtocol p = new ChatCompletionProtocol();
            p.setName(PROTOCOL_NAME);
            p.setProtocolType(ProtocolType.CHAT_COMPLETION);
            p.setMaxTokens(200000);
            p.setReplyTokens(4000);
            protocol = protocolService.save(p);
        }

        Lorebook lorebook = lorebookService.findAllForUser().stream().filter(l -> LOREBOOK_NAME.equals(l.getName())).findFirst().orElse(null);
        if (lorebook == null) {
            lorebook = lorebookService.importLorebook(Files.readString(skeletonDir.resolve("lorebook.json"), StandardCharsets.UTF_8), LOREBOOK_NAME);
        }

        Manuscript book = manuscriptService.findAllForUser().stream().filter(m -> skeleton.title.equals(m.getName())).findFirst().orElse(null);
        if (book == null) {
            Manuscript m = new Manuscript(); // the default prompt templates stay as the application has them
            m.setName(skeleton.title);
            m.setDescription(skeleton.description);
            m.setAi(ai);
            m.setProtocol(protocol);
            m.setLorebook(lorebook);
            m.setPov("third person limited");
            m.setTense("past");
            // the author's settings: every generated part is annotated, the notes are added without asking, the part is redrafted to address them and the redraft is applied
            m.setAutoAnnotate(true);
            m.setAutoAnnotateInstructions(ANNOTATE_INSTRUCTIONS);
            m.setAutoAnnotateAccept(true);
            m.setAutoAnnotateRedraft(true);
            m.setAutoAnnotateAcceptRedraft(true);
            book = manuscriptService.save(m);
        } else {
            Manuscript existing = book;
            OpenAICompatible theAi = ai;
            Protocol theProtocol = protocol;
            book = manuscriptService.update(existing, b -> {
                b.setAi(theAi);
                b.setProtocol(theProtocol);
            });
        }
        long bookId = book.getId();

        // ---- the plugins: started before the first part, so their listeners of the generation events are there for every prompt
        if (!wantedPlugins.isEmpty()) {
            plugins = new PluginHost(extensionService);
            plugins.load(wantedPlugins);
        }
        if (steps.contains("knowledge")) {
            if (plugins == null || !plugins.isLoaded("knowledge")) {
                throw new IllegalStateException("workset.steps has knowledge, but the knowledge plugin is not loaded (workset.plugins=" + wantedPlugins + ")");
            }
            knowledge = new KnowledgePass(commandService, commandScopes, manuscriptService);
            System.out.print("[workset] knowledge set-up:\n" + knowledge.setup(manuscriptService.find(bookId), skeletonDir.resolve("knowledge_seed.json")).indent(2));
        }
        if (steps.contains("translate")) {
            List<String> order = List.of(System.getProperty("workset.translators", "conlang,reallang").split(",")).stream().map(String::strip)
                    .filter(name -> !name.isEmpty() && plugins != null && plugins.isLoaded(name)).toList();
            if (order.isEmpty()) {
                throw new IllegalStateException("workset.steps has translate, but no translator plugin is loaded (workset.plugins=" + wantedPlugins + ")");
            }
            translators = order;
            translation = new TranslationPass(commandService, commandScopes);
            String report = translation.setup(manuscriptService.find(bookId), translators, skeletonDir,
                    Path.of("plugins/conlang-translator/tools").toAbsolutePath().normalize());
            System.out.print("[workset] translation set-up:\n" + report.indent(2));
        }

        if (steps.contains("blockend")) {
            if (plugins == null || !plugins.isLoaded("arcoutline")) {
                throw new IllegalStateException("workset.steps has blockend, but the arcoutline plugin is not loaded (workset.plugins=" + wantedPlugins + ")");
            }
            // the summaries may take this share of the context the book's AI has left after the reply; meta summaries are made only above it (0 = never)
            double contextShare = Double.parseDouble(System.getProperty("workset.contextShare", "0.5"));
            summaryBudget = (long) (contextShare * (ai.getMaxContext() - ai.getMaxCompletionTokens()));
            System.out.println("[workset] summaries may take " + summaryBudget + " tokens before meta summaries are made (" + contextShare + " of the usable context)");
            blockEnd = new BlockEnd(new TranslationPass(commandService, commandScopes), manuscriptService, chatMessageService, summaryMinWords, Integer.getInteger("workset.chapters"), Integer.getInteger("workset.chapterWords", 3000), summaryBudget, Integer.getInteger("workset.summaryEvery", 1), Integer.getInteger("workset.metaPairsPerBlock", 1));
            blockEnd.forgetLastAnswerWith(() -> {
                try {
                    cache.forgetLast();
                } catch (java.io.IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        }

        // ---- the parts
        State state = State.load(dir.resolve("state.json"));
        if (state.branchStarted && !state.branchDone) {
            throw new IllegalStateException("A run stopped in the middle of the branch (state.json): look at the book, remove the half branch by hand "
                    + "and set branchStarted to false before building on");
        }
        List<Integer> doneThisRun = new ArrayList<>();
        int done = chain(manuscriptService.find(bookId)).size();
        System.out.println("[workset] run " + run + ": the book has " + done + " parts, author " + author.id() + ", " + partsThisRun + " parts this run");
        // a block that is full but was not closed (the run before stopped at its last part, or the step was added later) is closed first
        endBlockIfDue(state, bookId, done, blockSize, skeleton, start);
        while (doneThisRun.size() < partsThisRun && done < skeleton.beats.size() && done < until) {
            Skeleton.Beat beat = skeleton.beats.get(done);
            clock.set(beat.at(start));
            generate(bookId, "part " + beat.n(), new TurnInput(beat.scene(), beat.pov(), beat.present(),
                    Skeleton.instructions(beat.instructions(), beat.words(), lengthHint || lengthHintParts.contains(beat.n()), lengthFactor)), clock);
            afterGenerate(bookId, "part " + beat.n(), beat.languages(), beat.present(), beat.pov());
            done++;
            doneThisRun.add(beat.n());
            endBlockIfDue(state, bookId, done, blockSize, skeleton, start);
            if (skeleton.branch != null && done == skeleton.branch.fromPart() + 1 && !state.branchDone) {
                buildBranch(skeleton, bookId, state, start, lengthHint, lengthFactor);
            }
        }

        // ---- what a run leaves behind
        Manuscript finished = manuscriptService.find(bookId);
        Files.createDirectories(dir.resolve("export"));
        Files.writeString(dir.resolve("export/book.json"), GSON.toJson(manuscriptService.createBackup(finished, true)), StandardCharsets.UTF_8);
        writeReview(doneThisRun);
        printTable(done);
        System.out.println("[workset] the book has " + done + " of " + skeleton.beats.size() + " parts; export: " + dir.resolve("export/book.json"));
    }

    // -------------------------------------------------------------------------------------------------------- generation

    private void audit(String label, String heading, String body) {
        Ledger.Line l = lastLine;
        String call = l == null ? "" : String.format(Locale.ROOT, "_call: %s, %s, %.1f s, %d tokens in, %d out_\n\n", l.author(),
                l.cached() ? "from the cache" : "asked", l.millis() / 1000.0, l.inputTokens(), l.outputTokens());
        audit.computeIfAbsent(label, k -> new ArrayList<>()).add("### " + heading + "\n\n" + call + body + "\n");
    }

    private static String words(String text) {
        return text == null || text.isBlank() ? "0 words" : text.strip().split("\\s+").length + " words";
    }

    private void generate(long bookId, String label, TurnInput input, SteppingClock clock) throws Exception {
        current = label;
        kind = "part";
        bridgeError = null;
        GenerationRun generation = new GenerationRun().answering(true);
        storyGenerationService.generateNextTurn(manuscriptService.find(bookId), input, GenerationRequest.newMessage(), generation);
        GenerationRun.Outcome outcome = generation.await(Duration.ofMinutes(25));
        if (outcome != GenerationRun.Outcome.COMPLETED || !generation.getErrors().isEmpty() || !generation.getSimpleErrors().isEmpty()) {
            throw new IllegalStateException("The generation of " + label + " did not complete: " + outcome + " errors=" + generation.getErrors()
                    + " simple=" + generation.getSimpleErrors() + (bridgeError != null ? " bridge=" + bridgeError : ""));
        }
        // the model "took" time to write it
        clock.advance(Duration.ofSeconds(Math.max(5, generation.getResponse().length() / 4 / 30)));
        System.out.println("[workset] " + label + " done: " + generation.getResponse().split("\\s+").length + " words");
        ChatMessage created = manuscriptService.find(bookId).getActiveLeaf();
        MessagePrompt prompt = messagePromptService.findFor(created);
        String lore = prompt == null || prompt.getLoreUsageJson() == null ? "-" : prompt.getLoreUsageJson();
        audit(label, "1. Generate", "Instructions given: " + input.instructions() + "\n\nScene: " + input.sceneSetting() + " · POV: " + input.povCharacter()
                + " · present: " + input.presentCharacters() + "\n\nPrompt: " + (prompt == null || prompt.getBuiltPrompt() == null ? 0 : prompt.getBuiltPrompt().length())
                + " characters, lore entries activated: " + (lore.split("\"entry\"").length - 1) + "\n\nText (" + words(created.getResponse()) + "):\n\n" + created.getResponse() + "\n");
    }

    private void buildBranch(Skeleton skeleton, long bookId, State state, LocalDate start, boolean lengthHint, double lengthFactor) throws Exception {
        state.branchStarted = true;
        state.save();
        Manuscript book = manuscriptService.find(bookId);
        ChatMessage mainLeaf = book.getActiveLeaf();
        List<ChatMessage> chain = chain(book);
        ChatMessage from = chain.get(skeleton.branch.fromPart() - 1);
        Instant base = skeleton.beats.get(skeleton.branch.fromPart()).at(start); // the time of the part this branch is the other child of
        int i = 0;
        for (Skeleton.BranchPart part : skeleton.branch.parts()) {
            clock.set(base.plusSeconds(600L * (i + 1)));
            if (i == 0) {
                ChatMessage parent = from;
                manuscriptService.update(manuscriptService.find(bookId), b -> b.setActiveLeaf(parent));
            }
            int[] words = part.length().equals("S") ? new int[]{250, 350} : part.length().equals("L") ? new int[]{550, 700} : new int[]{400, 500};
            generate(bookId, "branch " + (i + 1), new TurnInput(part.scene(), part.pov(), part.present(),
                    Skeleton.instructions(part.instructions(), words, lengthHint, lengthFactor)), clock);
            afterGenerate(bookId, "branch " + (i + 1), part.languages(), part.present(), part.pov());
            i++;
        }
        manuscriptService.update(manuscriptService.find(bookId), b -> b.setActiveLeaf(mainLeaf));
        state.branchDone = true;
        state.save();
    }

    /** The parts of the active branch, oldest first. */
    private List<ChatMessage> chain(Manuscript book) throws Exception {
        List<ChatMessage> parts = new ArrayList<>();
        ChatMessage part = book.getActiveLeaf();
        while (part != null) {
            parts.add(part);
            part = chatMessageService.getParent(part);
        }
        Collections.reverse(parts);
        return parts;
    }

    private OpenAICompatible findAi() throws Exception {
        return aiService.findAllForUser().stream().filter(a -> AI_NAME.equals(a.getName())).map(a -> (OpenAICompatible) a).findFirst().orElse(null);
    }

    // -------------------------------------------------------------------------------------------------------- the rest of the author's loop

    /**
     * What the author does after a part is generated: the annotations (the model reads the part and proposes notes, they are added), then the redraft that addresses them
     * (the part is written again, the redraft is applied, the notes are resolved). The order of the dialogs of the story view ({@code AutoAnnotate}, {@code AnnotateDialog},
     * {@code RedraftDialog}), which cannot run here, done with the services they call. Knowledge and the translators follow when the plugins can run in the harness.
     */
    private void afterGenerate(long bookId, String label, List<String> languageCodes, String present, String pov) throws Exception {
        ChatMessage generated = manuscriptService.find(bookId).getActiveLeaf();
        if (steps.contains("annotate")) {
            clock.advance(Duration.ofMinutes(2)); // the author reads the part
            int notes = annotate(bookId, label, generated);
            if (notes > 0 && steps.contains("redraft")) {
                clock.advance(Duration.ofMinutes(1));
                redraft(bookId, label, generated);
            }
        }
        // the order of the author's loop: knowledge, then the translations. The branch is not read: the state follows the main line of the story
        if (steps.contains("knowledge") && label.startsWith("part ")) {
            clock.advance(Duration.ofMinutes(1));
            knowledge(bookId, label);
        }
        if (steps.contains("translate")) {
            clock.advance(Duration.ofMinutes(1));
            translate(bookId, label, languageCodes, present, pov);
        }
    }

    /** Closes the block that ends at part {@code done}, once (the blocks closed are in {@code state.json}), on the day after its last part. */
    private void endBlockIfDue(State state, long bookId, int done, int blockSize, Skeleton skeleton, LocalDate start) throws Exception {
        if (blockEnd == null || done == 0 || done % blockSize != 0) {
            return;
        }
        if (state.blocksDone.contains(done / blockSize)) {
            return;
        }
        clock.set(skeleton.beats.get(done - 1).at(start).plus(Duration.ofHours(24)).atZone(java.time.ZoneOffset.UTC).withHour(10).withMinute(0).toInstant());
        blockEnd(bookId, done / blockSize, done - blockSize + 1, done);
        state.blocksDone.add(done / blockSize);
        state.save();
    }

    /**
     * The end of a block of 25 parts: the outline of the block (Arc Outline), the chapter headings in the text and the summaries (see {@link BlockEnd}). The calls of the
     * model it makes (the import of the arc, the summaries) are labelled in the ledger as {@code block N ...}.
     */
    private void blockEnd(long bookId, int block, int from, int to) throws Exception {
        String label = "block " + block;
        current = label;
        kind = "blockend";
        bridgeError = null;
        lastLine = null;
        String report = blockEnd.run(manuscriptService.find(bookId), bookId, label, from, to, (name, type) -> {
            current = name;
            kind = type;
            bridgeError = null;
            lastLine = null;
        }, () -> {
            try {
                return chain(manuscriptService.find(bookId));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, position -> {
            int total = 0;
            for (String plugin : translators) {
                try {
                    total += translation.speechCount(manuscriptService.find(bookId), plugin, position);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            return total;
        });
        audit(label, "Block end (parts " + from + " to " + to + ")", report);
        System.out.println("[workset] " + label + ": end of the block done (parts " + from + "-" + to + ")");
    }

    /**
     * The Knowledge Tracker reads the part just finished (the model finds what the characters learned, the changes are applied and logged), so the block in the
     * prompt of the next part has it. The items the seed expects at this part are put next to the answer, for the author to compare; they are never inserted.
     */
    private void knowledge(long bookId, String label) throws Exception {
        current = label + " knowledge";
        kind = "knowledge";
        bridgeError = null;
        lastLine = null;
        String answer = knowledge.process(manuscriptService.find(bookId));
        clock.advance(Duration.ofSeconds(20));
        int number = Integer.parseInt(label.substring(5));
        audit(label, "3b. Knowledge", "Answer of the command:\n\n```\n" + answer + "\n```\n\nThe seed expects at this part (never inserted, to compare):\n\n"
                + knowledge.expectedAt(number) + "\n\n" + knowledge.state(manuscriptService.find(bookId)));
        System.out.println("[workset] " + label + ": knowledge " + answer.replaceAll("\\s+", " "));
    }

    /**
     * The translation pass for the part just finished: one request per language it has (the codes of its beat), through the commands of the plugins. The English stays
     * behind every translated speech as a passage, and the next part is written from the English again (the plugins' transformers), so this changes what the book
     * shows and exports, not what the next prompt says.
     */
    private void translate(long bookId, String label, List<String> languageCodes, String present, String pov) throws Exception {
        int position = chain(manuscriptService.find(bookId)).size();
        List<TranslationPass.Lang> languages = TranslationPass.languagesFor(translators, languageCodes, present, pov,
                position <= translateAllUntil ? Set.of("conlang", "reallang") : translateAll);
        if (languages.isEmpty()) {
            audit(label, "4. Translate", "No speaker of a language in this part (present: " + present + ", codes " + languageCodes + "): no request.");
            return;
        }
        for (TranslationPass.Lang language : languages) {
            current = label + " translate " + language.name();
            kind = "translate";
            bridgeError = null;
            lastLine = null;
            String answer = translation.translate(manuscriptService.find(bookId), language, position);
            clock.advance(Duration.ofSeconds(20));
            String speeches = translation.passages(manuscriptService.find(bookId), language, position);
            audit(label, "4. Translate - " + language.name() + " (" + language.plugin() + ")", "Answer of the command:\n\n```\n" + answer + "\n```\n\nSpeeches of the part in "
                    + language.name() + " now:\n\n" + speeches);
            System.out.println("[workset] " + label + ": " + language.name() + " " + answer.replaceAll("\\s+", " "));
        }
        ChatMessage finalPart = chatMessageService.find(manuscriptService.find(bookId).getActiveLeaf());
        audit(label, "4. Translate - the part as stored now (with the translations in it)", words(finalPart.getResponse()) + ":\n\n" + finalPart.getResponse() + "\n");
    }

    private int annotate(long bookId, String label, ChatMessage generated) throws Exception {
        current = label + " annotate";
        kind = "annotate";
        bridgeError = null;
        Manuscript book = manuscriptService.find(bookId);
        ChatMessage part = chatMessageService.find(generated);
        CompletableFuture<List<AnnotateService.Proposal>> done = new CompletableFuture<>();
        var token = annotateService.annotate(book, part, AnnotateService.Request.of(book.getAutoAnnotateInstructions()), new AnnotateService.Callback() {
            public void onProgress(String reasoning, String text) {
            }

            public void onFinished(List<AnnotateService.Proposal> proposals, String raw) {
                done.complete(proposals);
            }

            public void onUnreadable(String raw, String reason) {
                System.out.println("[workset] " + label + ": the annotation could not be read (" + reason + "), no notes");
                done.complete(List.of());
            }

            public void onTerminated(String raw) {
                done.completeExceptionally(new IllegalStateException("annotation stopped"));
            }

            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        if (token == null) {
            throw new IllegalStateException("the annotation of " + label + " could not start");
        }
        List<AnnotateService.Proposal> proposals = done.get(25, TimeUnit.MINUTES);
        clock.advance(Duration.ofSeconds(20));
        if (!proposals.isEmpty()) {
            annotateService.accept(manuscriptService.find(bookId), chatMessageService.find(generated), proposals, book.getAi().getName());
        }
        StringBuilder block = new StringBuilder("What to look for (the book's annotation instructions):\n\n```\n" + book.getAutoAnnotateInstructions() + "\n```\n\n");
        if (proposals.isEmpty()) {
            block.append("The model proposed no notes.\n");
        } else {
            block.append("The model proposed ").append(proposals.size()).append(proposals.size() == 1 ? " note" : " notes").append(", all added as annotations (the book accepts without asking):\n\n");
            for (AnnotateService.Proposal p : proposals) {
                block.append("- [").append(p.type() == null ? "note" : p.type()).append("] ").append(p.located() ? "" : "(passage not found, note on the whole part) ")
                        .append('"').append(p.quote()).append("\" -> ").append(p.note()).append('\n');
            }
        }
        audit(label, "2. Annotate", block.toString());
        System.out.println("[workset] " + label + ": " + proposals.size() + " notes");
        return proposals.size();
    }

    private void redraft(long bookId, String label, ChatMessage generated) throws Exception {
        current = label + " redraft";
        kind = "redraft";
        bridgeError = null;
        Manuscript book = manuscriptService.find(bookId);
        ChatMessage part = chatMessageService.find(generated);
        List<Annotation> addressed = annotationService.listForPart(book, part).stream().filter(a -> a.getStatus() == AnnotationStatus.OPEN).toList();
        CompletableFuture<String[]> done = new CompletableFuture<>();
        var token = redraftService.redraft(book, part, "", null, GenerationOptions.defaults(), true, null, new RedraftService.Callback() {
            public void onProgress(String reasoning, String text) {
            }

            public void onFinished(String reasoning, String text) {
                done.complete(new String[]{reasoning, text});
            }

            public void onTerminated(String reasoning, String text) {
                done.completeExceptionally(new IllegalStateException("redraft stopped"));
            }

            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        if (token == null) {
            throw new IllegalStateException("the redraft of " + label + " could not start");
        }
        String[] result = done.get(25, TimeUnit.MINUTES);
        clock.advance(Duration.ofSeconds(Math.max(5, result[1].length() / 4 / 30)));
        Manuscript current = manuscriptService.find(bookId);
        String modelAndProtocol = current.getAi().getName() + " · " + current.getProtocol().getName();
        chatMessageService.updateResponse(current, chatMessageService.find(generated), result[1],
                RevisionOrigin.model(RevisionSource.REDRAFT, modelAndProtocol, false, result[0]));
        for (Annotation annotation : addressed) {
            annotationService.setStatus(current, annotation, AnnotationStatus.RESOLVED);
        }
        List<String> before = java.util.Arrays.stream(part.getResponse().split("\\n\\s*\\n")).map(String::strip).toList();
        List<String> after = java.util.Arrays.stream(result[1].split("\\n\\s*\\n")).map(String::strip).toList();
        long changed = after.stream().filter(p -> !before.contains(p)).count();
        StringBuilder block = new StringBuilder("Notes given to the model (" + addressed.size() + ", now resolved):\n\n");
        for (Annotation annotation : addressed) {
            block.append("- [").append(annotation.getType() == null ? "note" : annotation.getType()).append("] ").append(annotation.getText()).append('\n');
        }
        block.append("\nBefore: ").append(words(part.getResponse())).append(" · after: ").append(words(result[1])).append(" · paragraphs changed: ").append(changed)
                .append(" of ").append(after.size()).append(" (the revision history of the part now has GENERATE and REDRAFT)\n");
        if (result[0] != null && !result[0].isBlank()) {
            block.append("\nReasoning:\n\n").append(result[0]).append('\n');
        }
        block.append("\nRedrafted text (this is the text of the part from now on):\n\n").append(result[1]).append('\n');
        audit(label, "3. Redraft", block.toString());
        System.out.println("[workset] " + label + ": redrafted, " + addressed.size() + " notes resolved");
    }

    // -------------------------------------------------------------------------------------------------------- the bridge

    /** What the fake model server answers a completion request with: the author's answer, from the cache when the same request was answered before. */
    private MockLLMResponse bridge(MockLLMRequest request) {
        try {
            List<Author.Message> messages = request.getMessages().stream().map(m -> new Author.Message(m.role(), m.content())).toList();
            Path prompts = Files.createDirectories(dir.resolve("prompts"));
            String key = AnswerCache.key(author.id(), messages);
            Files.writeString(prompts.resolve(current.replace(' ', '-') + ".json"), GSON.toJson(messages), StandardCharsets.UTF_8);
            Optional<Author.Answer> cached = cache.find(key);
            Author.Answer answer = cached.isPresent() ? cached.get() : author.answer(messages);
            if (cached.isEmpty()) {
                cache.store(key, author.id(), messages, answer);
            }
            long requestChars = messages.stream().mapToLong(m -> m.content().length()).sum();
            Ledger.Line line = new Ledger.Line(Instant.now().toString(), run, current, kind, author.id(), cached.isPresent(), requestChars,
                    answer.text().length(), answer.inputTokens(), answer.outputTokens(), answer.cachedTokens(), answer.costUsd(), answer.millis());
            ledger.add(line);
            lastLine = line;
            clock.advance(Duration.ofMillis(1200)); // time to first token
            MockLLMResponse response = MockLLMResponse.split(answer.text(), 120);
            if (answer.reasoning() != null && !answer.reasoning().isBlank()) {
                response = response.withReasoning(answer.reasoning());
            }
            return response.usage(Math.max(answer.inputTokens(), requestChars / 4), Math.max(answer.outputTokens(), answer.text().length() / 4));
        } catch (Exception e) {
            bridgeError = e.toString();
            e.printStackTrace();
            return MockLLMResponse.error(500, "bridge: " + e);
        }
    }

    // -------------------------------------------------------------------------------------------------------- reports

    /** The review of the run: for every part the steps of the loop, each separately, so each can be checked. */
    private void writeReview(List<Integer> numbers) throws Exception {
        StringBuilder out = new StringBuilder("# Run " + run + " - parts " + numbers + "\n\nEvery step of the author's loop is a section of its own: generate, annotate, redraft"
                + ", translate when the plugins are loaded (knowledge follows). `usage.jsonl` has the calls, `prompts/` the last request of each part.\n\n");
        for (var entry : audit.entrySet()) {
            String label = entry.getKey();
            out.append("## ").append(label.substring(0, 1).toUpperCase(Locale.ROOT)).append(label.substring(1));
            if (label.startsWith("part ") && skeletonForReview != null) {
                Skeleton.Beat beat = skeletonForReview.beats.get(Integer.parseInt(label.substring(5)) - 1);
                out.append(" - day ").append(beat.day()).append(' ').append(beat.time()).append(" - ").append(beat.pov()).append(", ").append(beat.scene())
                        .append(" [").append(String.join(" ", beat.languages())).append("], asked ").append(beat.words()[0]).append('-').append(beat.words()[1]).append(" words");
            }
            out.append("\n\n");
            entry.getValue().forEach(block -> out.append(block).append('\n'));
        }
        Files.createDirectories(dir.resolve("review"));
        String name = "run-" + String.format(Locale.ROOT, "%03d", run) + ".md";
        Files.writeString(dir.resolve("review").resolve(name), out.toString(), StandardCharsets.UTF_8);
        // review/latest.md always points at the newest run, so there is no need to look for the number
        Path latest = dir.resolve("review/latest.md");
        Files.deleteIfExists(latest);
        Files.createSymbolicLink(latest, Path.of(name));
    }

    private void printTable(int partsDone) throws Exception {
        List<Ledger.Line> all = ledger.read();
        List<Ledger.Line> mine = all.stream().filter(l -> l.run() == run).toList();
        System.out.println("[workset] usage            calls  cached   input   output  cached-in   cost$   seconds");
        System.out.println(row("this run", mine));
        System.out.println(row("cumulative", all));
        if (!mine.isEmpty() && partsDone > 0) {
            long avg = all.stream().filter(l -> !l.cached()).mapToLong(Ledger.Line::inputTokens).sum() / Math.max(1, all.stream().filter(l -> !l.cached()).count());
            System.out.println("[workset] average input per real call: " + avg + " tokens; the prompt grows with the story, the summaries are what bound it");
        }
    }

    private static String row(String name, List<Ledger.Line> lines) {
        return String.format(Locale.ROOT, "[workset] %-14s %5d %7d %7d %8d %10d %7.2f %9d", name, lines.size(), lines.stream().filter(Ledger.Line::cached).count(),
                lines.stream().mapToLong(Ledger.Line::inputTokens).sum(), lines.stream().mapToLong(Ledger.Line::outputTokens).sum(),
                lines.stream().mapToLong(Ledger.Line::cachedTokens).sum(), lines.stream().mapToDouble(Ledger.Line::costUsd).sum(),
                lines.stream().mapToLong(Ledger.Line::millis).sum() / 1000);
    }

    /** What the database does not say; saved after every change. */
    static class State {
        boolean branchStarted;
        boolean branchDone;
        /** The blocks (1, 2...) whose end step is done: it is not repeated. */
        java.util.Set<Integer> blocksDone = new java.util.TreeSet<>();
        /** The blocks whose meta summaries were made (or found not needed). */
        java.util.Set<Integer> metaDone = new java.util.TreeSet<>();
        private transient Path file;

        static State load(Path file) throws Exception {
            State s = Files.exists(file) ? GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), State.class) : new State();
            s.file = file;
            return s;
        }

        void save() throws Exception {
            Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
        }
    }
}
