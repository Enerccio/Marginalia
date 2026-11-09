package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.generation.CommandMacros;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code {{command::name::argument}}}: a template runs a read only command made for macros, lazily, and the answer is kept
 * with the part the generation continues from until a regenerate or a swipe.
 */
class CommandMacroTest extends GenerationTestBase {

    private static final AtomicInteger PROBE_RUNS = new AtomicInteger();
    private static final AtomicInteger WRITER_RUNS = new AtomicInteger();
    private static final AtomicInteger FAILING_RUNS = new AtomicInteger();

    @Autowired
    private CommandService commandService;

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * Counts its runs; the answer says which run it was and the book it ran in.
     */
    public static class Suite {

        @Command(commandName = "macro-probe", humanNameText = "Probe", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true, availableInMacros = true)
        public void probe(CommandContext context) throws Exception {
            String book = context.getScope().getManuscript().getName();
            context.setReturn("probe#" + PROBE_RUNS.incrementAndGet() + ":" + context.getPositional() + ":" + book);
        }

        @Command(commandName = "macro-async", humanNameText = "Async probe", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true, availableInMacros = true, async = true)
        public void async(CommandContext context) {
            new Thread(() -> context.setReturn("late answer")).start();
        }

        @Command(commandName = "macro-failing", humanNameText = "Failing", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true, availableInMacros = true)
        public void failing(CommandContext context) {
            FAILING_RUNS.incrementAndGet();
            throw new IllegalStateException("broken");
        }

        @Command(commandName = "macro-writer", humanNameText = "Writer", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                readOnly = true)
        public void writer(CommandContext context) {
            WRITER_RUNS.incrementAndGet();
            context.setReturn("not for macros");
        }
    }

    public static class NotReadOnly {

        @Command(commandName = "macro-not-read-only", humanNameText = "Changes", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                availableInMacros = true, signature = @CommandParameterSignature)
        public void changes(CommandContext context) {
            context.setReturn("changed");
        }
    }

    private final Suite suite = new Suite();

    @BeforeEach
    void setUp() {
        PROBE_RUNS.set(0);
        WRITER_RUNS.set(0);
        FAILING_RUNS.set(0);
        commandService.registerCommands(suite);
    }

    @AfterEach
    void tearDown() {
        commandService.unregisterCommands(suite);
    }

    private Manuscript book(String template) throws Exception {
        Manuscript manuscript = newManuscript();
        manuscript.setTemplate(template);
        return manuscriptService.save(manuscript);
    }

    private String systemPrompt() {
        return String.join("\n", llm.lastCompletionRequest().getMessages("system"));
    }

    private ChatMessage generate(Manuscript manuscript, GenerationRequest request) throws Exception {
        llm.enqueue(MockLLMResponse.text("part"));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getSimpleErrors()).isEmpty();
        return chatMessageService.find(run.getMessage().getId());
    }

    private void conditional(Lorebook lorebook, String name, String condition) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload("<" + name + ">");
        entry.setActivationCondition(condition);
        lorebookEntryService.save(entry);
    }

    @Test
    void theConditionOfALoreEntryRunsOnlyCommandsMadeForMacros() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("world");
        lorebook = lorebookService.save(lorebook);
        conditional(lorebook, "read", "{{command::macro-probe::x}}");
        conditional(lorebook, "write", "{{command::macro-writer}}");
        Manuscript manuscript = book("{{backgroundLore}}");
        manuscript.setLorebook(lorebook);
        manuscript = manuscriptService.save(manuscript);

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(systemPrompt()).contains("<read>").doesNotContain("<write>");
        assertThat(PROBE_RUNS).hasValue(1);
        assertThat(WRITER_RUNS).hasValue(0);
    }

    @Test
    void theAnswerOfTheCommandIsInThePrompt() throws Exception {
        Manuscript manuscript = book("Before {{command::macro-probe::a::b}} after");

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(systemPrompt()).contains("Before probe#1:[a, b]:" + manuscript.getName() + " after");
    }

    @Test
    void theCommandRunsOncePerGenerationWhateverTheNumberOfRenders() throws Exception {
        Manuscript manuscript = book("{{command::macro-probe::x}} / {{command::macro-probe::x}}");

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(PROBE_RUNS).hasValue(1);
        assertThat(systemPrompt()).contains("probe#1:[x]:" + manuscript.getName() + " / probe#1:[x]:" + manuscript.getName());
    }

    @Test
    void differentArgumentsAreDifferentCalls() throws Exception {
        Manuscript manuscript = book("{{command::macro-probe::x}} {{command::macro-probe::y}}");

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(PROBE_RUNS).hasValue(2);
    }

    @Test
    void aCommandInAnUntakenBranchIsNotRun() throws Exception {
        Manuscript manuscript = book("{{#if false}}{{command::macro-probe::x}}{{/if}}nothing");

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(PROBE_RUNS).hasValue(0);
    }

    @Test
    void anAsyncCommandIsWaitedFor() throws Exception {
        Manuscript manuscript = book("[{{command::macro-async}}]");

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(systemPrompt()).contains("[late answer]");
    }

    @Test
    void aCommandNotMadeForMacrosIsNotRun() throws Exception {
        Manuscript manuscript = book("[{{command::macro-writer}}] [{{command::no-such-command}}]");

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(WRITER_RUNS).hasValue(0);
        assertThat(systemPrompt()).contains("[Error] [Error]");
    }

    @Test
    void aFailingCommandRendersEmptyAndIsNotKept() throws Exception {
        Manuscript manuscript = book("[{{command::macro-failing}}]");
        ChatMessage first = generate(manuscript, GenerationRequest.newMessage());

        assertThat(systemPrompt()).contains("[]");
        generate(manuscript, GenerationRequest.newMessage());

        // the second generation tried again, nothing was kept with the first part
        assertThat(FAILING_RUNS.get()).isGreaterThanOrEqualTo(2);
        assertThat(chatMessageService.find(first.getId()).getAttributes().has(CommandMacros.ATTRIBUTE_KEY)).isFalse();
    }

    @Test
    void theAnswerIsKeptWithThePartTheGenerationContinuesFrom() throws Exception {
        Manuscript manuscript = book("{{command::macro-probe::x}}");
        ChatMessage first = generate(manuscript, GenerationRequest.newMessage());
        // nothing to keep it with before the first part exists
        assertThat(chatMessageService.find(first.getId()).getAttributes().has(CommandMacros.ATTRIBUTE_KEY)).isFalse();

        generate(manuscript, GenerationRequest.newMessage());

        assertThat(chatMessageService.find(first.getId()).getAttributes().getAsJsonObject(CommandMacros.ATTRIBUTE_KEY).entrySet())
                .hasSize(1);
    }

    @Test
    void aLaterGenerationFromTheSamePartReadsTheKeptAnswer() throws Exception {
        Manuscript manuscript = book("unused");
        ChatMessage part = generate(manuscript, GenerationRequest.newMessage());
        Manuscript current = manuscriptService.find(manuscript.getId());

        CommandMacros first = new CommandMacros(commandService, commandScopes, chatMessageService, current,
                chatMessageService.find(part.getId()), false);
        String answer = first.run("macro-probe", List.of("x"));
        // another generation: new object, the part is read again
        CommandMacros second = new CommandMacros(commandService, commandScopes, chatMessageService, current,
                chatMessageService.find(part.getId()), false);

        assertThat(second.run("macro-probe", List.of("x"))).isEqualTo(answer);
        assertThat(PROBE_RUNS).hasValue(1);
    }

    @Test
    void aRegenerateRunsTheCommandAgainAndReplacesTheKeptAnswer() throws Exception {
        Manuscript manuscript = book("{{command::macro-probe::x}}");
        generate(manuscript, GenerationRequest.newMessage());
        ChatMessage second = generate(manuscript, GenerationRequest.newMessage());
        assertThat(PROBE_RUNS).hasValue(2);

        generate(manuscript, GenerationRequest.regenerate(second));

        assertThat(PROBE_RUNS).hasValue(3);
        assertThat(systemPrompt()).contains("probe#3:");
    }

    @Test
    void aSwipeRunsTheCommandAgain() throws Exception {
        Manuscript manuscript = book("{{command::macro-probe::x}}");
        generate(manuscript, GenerationRequest.newMessage());
        ChatMessage second = generate(manuscript, GenerationRequest.newMessage());

        generate(manuscript, GenerationRequest.newSwipe(second));

        assertThat(PROBE_RUNS).hasValue(3);
        assertThat(systemPrompt()).contains("probe#3:");
    }

    @Test
    void aRefreshDropsTheAnswersOfCallsThatWereNotMade() throws Exception {
        Manuscript manuscript = book("unused");
        ChatMessage part = generate(manuscript, GenerationRequest.newMessage());
        Manuscript current = manuscriptService.find(manuscript.getId());
        new CommandMacros(commandService, commandScopes, chatMessageService, current, chatMessageService.find(part.getId()), false)
                .run("macro-probe", List.of("old"));

        new CommandMacros(commandService, commandScopes, chatMessageService, current, chatMessageService.find(part.getId()), true)
                .run("macro-probe", List.of("new"));

        assertThat(chatMessageService.find(part.getId()).getAttributes().getAsJsonObject(CommandMacros.ATTRIBUTE_KEY).entrySet())
                .hasSize(1);
    }

    @Test
    void aCommandAvailableInMacrosHasToBeReadOnly() {
        NotReadOnly bad = new NotReadOnly();

        assertThatThrownBy(() -> commandService.registerCommands(bad))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("readOnly");
        assertThat(commandService.getCommands("macro-not-read-only")).isEmpty();
    }
}
