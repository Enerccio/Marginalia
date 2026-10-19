package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The scope a command runs in is the cage of an agent: the working copy of its run and the lorebooks of the copy, nothing
 * else can be reached by a uuid, and no question is asked of anybody.
 */
class CommandScopeTest extends AgentTestBase {

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private CommandService commandService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    private Manuscript book;
    private Lorebook root, sub, unrelated;
    private LorebookEntry rootEntry, subEntry;
    private ChatMessage first, second;
    private Agent agent;

    private final Object suite = new WhereAmI();

    /** Says which book a command was run on. */
    public static class WhereAmI {
        @Command(commandName = "where-am-i", humanNameText = "Where am I", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature, returns = @CommandReturn(name = "name", type = String.class, descriptionText = "x"))
        public void where(CommandContext context) throws Exception {
            context.setReturn(context.getScope().getManuscript().getUuid());
        }

        @Command(commandName = "ask-me", humanNameText = "Ask me", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature, returns = @CommandReturn(name = "answer", type = Boolean.class, descriptionText = "x"))
        public void ask(CommandContext context) throws Exception {
            context.setReturn(context.confirm("Really?"));
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        sub = lorebook("sub");
        subEntry = entry(sub, "sub entry");
        root = lorebook("root");
        rootEntry = entry(root, "root entry");
        root.setSubbooks(new ArrayList<>(List.of(sub)));
        root = lorebookService.save(root);
        unrelated = lorebook("unrelated");

        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);
        first = message(book, null, "Once upon a time");
        second = message(book, first, "there was a dragon");
        agent = agentService.save(newAgent(book));
        commandService.registerCommands(suite);
    }

    @AfterEach
    void tearDown() {
        commandService.unregisterCommands(suite);
    }

    private ChatMessage message(Manuscript in, ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(in, message) : chatMessageService.addChild(parent, message);
    }

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName(name));
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return entryService.save(entry);
    }

    private CommandScope scopeOfNewRun() throws Exception {
        return commandScopes.forRun(agentRunService.start(agent));
    }

    private ChatMessage copied(CommandScope scope, String text) throws Exception {
        return chatMessageService.getAllMessages(scope.getManuscript()).stream()
                .filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    @Test
    void theModelOfTheScopeOfARunIsTheModelOfTheAgentOrTheOneOfTheBook() throws Exception {
        OpenAICompatible bookAi = createAI();
        Protocol bookProtocol = protocol();
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setAi(bookAi);
        stored.setProtocol(bookProtocol);
        manuscriptService.save(stored);
        var main = agentService.resolveProfile(agent.getMainProfile());

        CommandScope own = scopeOfNewRun();
        assertThat(own.getModel().ai().getUuid()).as("the default: the model of the agent").isEqualTo(main.ai().getUuid());
        assertThat(own.getModel().protocol().getUuid()).isEqualTo(main.protocol().getUuid());
        assertThat(commandScopes.session().getModel()).as("the user at the command bar has their own defaults").isNull();
    }

    @Test
    void anAgentThatDoesNotUseItsModelHasTheModelOfTheBookInItsScope() throws Exception {
        OpenAICompatible bookAi = createAI();
        Protocol bookProtocol = protocol();
        Manuscript stored = manuscriptService.find(book.getId());
        stored.setAi(bookAi);
        stored.setProtocol(bookProtocol);
        manuscriptService.save(stored);
        agent.setUseAgentModel(false);
        agent = agentService.save(agent);

        CommandScope scope = scopeOfNewRun();

        assertThat(scope.getModel().ai().getUuid()).isEqualTo(bookAi.getUuid());
        assertThat(scope.getModel().protocol().getUuid()).isEqualTo(bookProtocol.getUuid());
    }

    @Test
    void withoutAModelOfTheBookTheScopeNamesNoneAndTheCommandUsesItsOwnDefault() throws Exception {
        agent.setUseAgentModel(false);
        agent = agentService.save(agent);

        assertThat(scopeOfNewRun().getModel()).isNull();
    }

    @Test
    void theScopeOfARunIsItsWorkingCopy() throws Exception {
        AgentRun run = agentRunService.start(agent);
        CommandScope scope = commandScopes.forRun(run);

        assertThat(scope.isAgent()).isTrue();
        assertThat(scope.getRun().getId()).isEqualTo(run.getId());
        Manuscript copy = scope.getManuscript();
        assertThat(copy.isAgentic()).isTrue();
        assertThat(copy.getId()).isNotEqualTo(book.getId());
        assertThat(copy.getId()).isEqualTo(agentRunService.find(run.getId()).getWorkingCopy().getId());
        assertThat(scope.getLorebook().isAgentic()).isTrue();
        assertThat(scope.getLorebook().getId()).isNotEqualTo(root.getId());
    }

    @Test
    void theScopeHasNoBookWhenTheRunLetGoOfItsCopy() throws Exception {
        AgentRun run = agentRunService.start(agent);
        CommandScope scope = commandScopes.forRun(run);

        agentRunService.transition(run, AgentRunStatus.FAILED, "broke");

        assertThat(scope.getManuscript()).isNull();
        assertThat(scope.getLorebook()).isNull();
        assertThatThrownBy(() -> scope.resolveMessage(first.getUuid())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyPartsOfTheCopyAreFound() throws Exception {
        CommandScope scope = scopeOfNewRun();
        ChatMessage inCopy = copied(scope, "there was a dragon");

        assertThat(scope.resolveMessage(inCopy.getUuid()).getId()).isEqualTo(inCopy.getId());
        // the original, another book, nonsense
        assertThatThrownBy(() -> scope.resolveMessage(second.getUuid())).isInstanceOf(IllegalArgumentException.class);
        Manuscript other = manuscript();
        ChatMessage foreign = message(other, null, "elsewhere");
        assertThatThrownBy(() -> scope.resolveMessage(foreign.getUuid())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scope.resolveMessage("not a uuid")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scope.resolveMessage(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aDeletedPartIsNotFound() throws Exception {
        CommandScope scope = scopeOfNewRun();
        ChatMessage inCopy = copied(scope, "there was a dragon");

        chatMessageService.delete(inCopy, false);

        assertThatThrownBy(() -> scope.resolveMessage(inCopy.getUuid())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void onlyLorebooksAndEntriesOfTheCopyAreFound() throws Exception {
        CommandScope scope = scopeOfNewRun();
        Lorebook copiedRoot = scope.getLorebook();
        Lorebook copiedSub = lorebookService.getSubbooks(copiedRoot).getFirst();
        LorebookEntry copiedEntry = entryService.getEntriesForLorebook(copiedSub).getFirst();

        assertThat(scope.resolveLorebook(copiedRoot.getUuid()).getId()).isEqualTo(copiedRoot.getId());
        assertThat(scope.resolveLorebook(copiedSub.getUuid()).getId()).as("a subbook, however deep").isEqualTo(copiedSub.getId());
        assertThat(scope.resolveEntry(copiedEntry.getUuid()).getId()).isEqualTo(copiedEntry.getId());

        for (Lorebook outside : List.of(root, sub, unrelated)) {
            assertThatThrownBy(() -> scope.resolveLorebook(outside.getUuid())).isInstanceOf(IllegalArgumentException.class);
        }
        for (LorebookEntry outside : List.of(rootEntry, subEntry)) {
            assertThatThrownBy(() -> scope.resolveEntry(outside.getUuid())).isInstanceOf(IllegalArgumentException.class);
        }
        // a lorebook of the copy that the copy does not use any more is outside too
        Lorebook stray = new Lorebook();
        stray.setName("stray");
        stray.setAgentic(true);
        Lorebook saved = lorebookService.save(stray);
        assertThatThrownBy(() -> scope.resolveLorebook(saved.getUuid())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAgentIsNotAskedAgain() throws Exception {
        CommandScope scope = scopeOfNewRun();
        com.github.enerccio.marginalia.domain.service.command.CommandRun run = commandService.execute("ask-me", Map.of(), false, scope);

        assertThat(run.get(5, TimeUnit.SECONDS)).isEqualTo(true);
    }

    @Test
    void aCommandRunsOnTheBookOfTheScopeWhateverTheSessionHas() throws Exception {
        AgentRun run = agentRunService.start(agent);
        CommandScope scope = commandScopes.forRun(run);

        Object answer = commandService.execute("where-am-i", Map.of(), false, scope).get(5, TimeUnit.SECONDS);

        assertThat(answer).isEqualTo(agentRunService.find(run.getId()).getWorkingCopy().getUuid());
        assertThat(answer).isNotEqualTo(book.getUuid());
    }

    @Test
    void aRunThatDoesNotExistHasNoScope() {
        assertThatThrownBy(() -> commandScopes.forRun(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commandScopes.forRun(new AgentRun())).isInstanceOf(IllegalArgumentException.class);
    }
}
