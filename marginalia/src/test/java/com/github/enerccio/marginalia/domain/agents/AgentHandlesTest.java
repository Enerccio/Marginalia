package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.agent.CommandCage;
import com.github.enerccio.marginalia.domain.service.agent.CommandFunctions;
import com.github.enerccio.marginalia.domain.service.agent.HandleTranslator;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The agent knows the items of its working copy by handles, never by uuids: the handles are given to the items as they
 * appear in the results of the commands, are the same every time, and are turned back into uuids in the arguments, where
 * only the items of the copy can be reached.
 */
class AgentHandlesTest extends AgentTestBase {

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private CommandService commandService;

    @Autowired
    private AgentRunPairService pairService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    private Manuscript book;
    private ChatMessage first, second;
    private Lorebook root;
    private LorebookEntry entry;
    private Agent agent;
    private AgentRun run;
    private CommandScope scope;
    private HandleTranslator handles;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        root = new Lorebook();
        root.setName(uniqueName("root"));
        root.setSubbooks(new ArrayList<>());
        root = lorebookService.save(root);
        entry = new LorebookEntry();
        entry.setLorebook(root);
        entry.setName("Harry");
        entry = entryService.save(entry);
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);
        first = message(null, "Once upon a time");
        second = message(first, "there was a dragon");
        agent = agentService.save(newAgent(book));
        run = agentRunService.find(agentRunService.start(agent).getId());
        scope = commandScopes.forRun(run);
        handles = pairService.translator(run, scope);
    }

    private ChatMessage message(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private ChatMessage copied(String text) throws Exception {
        return chatMessageService.getAllMessages(scope.getManuscript()).stream()
                .filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    @Test
    void anItemOfTheCopyGetsAHandleOfItsKind() throws Exception {
        ChatMessage part = copied("Once upon a time");
        Lorebook lorebook = scope.getLorebook();
        LorebookEntry copiedEntry = entryService.getEntriesForLorebook(lorebook).getFirst();

        String text = handles.toHandles("part " + part.getUuid() + " in " + lorebook.getUuid() + " with " + copiedEntry.getUuid());

        assertThat(text).isEqualTo("part h:m1 in h:l1 with h:e1");
        assertThat(pairService.findHandle(run, part.getUuid())).contains("h:m1");
        assertThat(pairService.findCopyUuid(run, "h:m1")).contains(part.getUuid());
    }

    @Test
    void theHandlesAreTheSameEveryTimeAndCountPerKind() throws Exception {
        ChatMessage one = copied("Once upon a time");
        ChatMessage two = copied("there was a dragon");

        assertThat(handles.toHandles(one.getUuid())).isEqualTo("h:m1");
        assertThat(handles.toHandles(two.getUuid())).isEqualTo("h:m2");
        assertThat(handles.toHandles(one.getUuid() + " " + two.getUuid() + " " + one.getUuid())).isEqualTo("h:m1 h:m2 h:m1");

        // another translator of the run (after a pause) gives the same handles and goes on counting
        HandleTranslator again = pairService.translator(run, scope);
        assertThat(again.toHandles(two.getUuid())).isEqualTo("h:m2");
        ChatMessage third = chatMessageService.addChild(two, new ChatMessage());
        assertThat(again.toHandles(third.getUuid())).isEqualTo("h:m3");
    }

    @Test
    void handlesGoBackToTheUuidsInTheArguments() throws Exception {
        ChatMessage one = copied("Once upon a time");
        handles.toHandles(one.getUuid());

        assertThat(handles.fromHandles("h:m1")).isEqualTo(one.getUuid());
        assertThat(handles.fromHandles("compare h:m1 with itself, h:m1.")).isEqualTo("compare " + one.getUuid() + " with itself, " + one.getUuid() + ".");

        Map<String, Object> arguments = new LinkedHashMap<>();
        arguments.put("part", "h:m1");
        arguments.put("parts", new ArrayList<>(List.of("h:m1", "plain")));
        arguments.put("nested", Map.of("part", "h:m1"));
        arguments.put("count", 3);
        arguments.put("none", null);
        @SuppressWarnings("unchecked")
        Map<String, Object> translated = (Map<String, Object>) handles.fromHandles(arguments);
        assertThat(translated).containsEntry("part", one.getUuid()).containsEntry("count", 3).containsEntry("none", null);
        assertThat(translated.get("parts")).isEqualTo(List.of(one.getUuid(), "plain"));
        assertThat(translated.get("nested")).isEqualTo(Map.of("part", one.getUuid()));
    }

    @Test
    void aHandleTheRunDoesNotHaveIsLeftAsItIs() throws Exception {
        assertThat(handles.fromHandles("h:m99")).isEqualTo("h:m99");
        assertThat(handles.fromHandles("no handles here, only h: and h:x1")).isEqualTo("no handles here, only h: and h:x1");
        assertThat(handles.fromHandles(42)).isEqualTo(42);
        assertThat(handles.fromHandles(null)).isNull();
    }

    @Test
    void aUuidOfTheOriginalOrAnythingElseIsNotMadeAHandle() throws Exception {
        String text = "original " + first.getUuid() + ", the book " + book.getUuid() + ", nothing 123e4567-e89b-12d3-a456-426614174000";

        assertThat(handles.toHandles(text)).isEqualTo(text);
        assertThat(pairService.findHandle(run, first.getUuid())).isEmpty();
    }

    @Test
    void anItemTheAgentMadeGetsAHandleAndNoOriginal() throws Exception {
        ChatMessage made = chatMessageService.addChild(copied("there was a dragon"), new ChatMessage());

        String handle = handles.toHandles(made.getUuid());

        assertThat(handle).startsWith("h:m");
        assertThat(pairService.findByCopy(run, made.getUuid())).get().satisfies(pair -> {
            assertThat(pair.kind()).isEqualTo(AgentRunPairKind.MESSAGE);
            assertThat(pair.originalUuid()).isNull();
        });
        assertThat(handles.fromHandles(handle)).isEqualTo(made.getUuid());
    }

    @Test
    void aHandleIsOfThePairThatAlreadyExisted() throws Exception {
        ChatMessage part = copied("Once upon a time");
        UuidPair pair = pairService.findByCopy(run, part.getUuid()).orElseThrow();
        assertThat(pair.originalUuid()).isEqualTo(first.getUuid());

        handles.toHandles(part.getUuid());

        UuidPair after = pairService.findByCopy(run, part.getUuid()).orElseThrow();
        assertThat(after.originalUuid()).as("the same pair, now with its handle").isEqualTo(pair.originalUuid());
        assertThat(after.handle()).isNotNull();
        assertThat(pairService.count(run)).as("no second row").isEqualTo(pairService.getAll(run).size());
    }

    @Test
    void handlesOfOneRunMeanNothingInAnother() throws Exception {
        ChatMessage part = copied("Once upon a time");
        handles.toHandles(part.getUuid());
        agentRunService.transition(run, com.github.enerccio.marginalia.domain.collections.AgentRunStatus.FAILED, "failed");
        AgentRun next = agentRunService.find(agentRunService.start(agent).getId());
        HandleTranslator other = pairService.translator(next, commandScopes.forRun(next));

        assertThat(other.fromHandles("h:m1")).as("h:m1 of the first run").isEqualTo("h:m1");
        assertThat(pairService.findCopyUuid(next, "h:m1")).isEmpty();
    }

    // ---------------------------------------------------------------------------------- through the commands

    public static class PartCommands {
        @Command(commandName = "peek-part", humanNameText = "Peek part", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature, returns = @CommandReturn(name = "part", type = String.class, descriptionText = "x"))
        public void peek(CommandContext context) throws Exception {
            Manuscript copy = context.getScope().getManuscript();
            context.setReturn("{\"part\":\"" + copy.getActiveLeaf().getUuid() + "\",\"book\":\"" + copy.getUuid() + "\"}");
        }

        @Command(commandName = "read-part", humanNameText = "Read part", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature(@CommandParameter(name = "part", type = String.class, descriptionText = "x")),
                returns = @CommandReturn(name = "text", type = String.class, descriptionText = "x"))
        public void read(CommandContext context) throws Exception {
            context.setReturn(context.getScope().resolveMessage(context.get("part", String.class)).getResponse());
        }
    }

    private final Object parts = new PartCommands();

    private FunctionSet functions() {
        commandService.registerCommands(parts);
        return CommandFunctions.create(commandService, List.of("peek-part", "read-part"), DestructiveCommandMode.AUTO,
                l -> l.name(), null, null, null, null, new CommandCage(scope, handles));
    }

    @AfterEach
    void unregister() {
        commandService.unregisterCommands(parts);
    }

    private static String call(FunctionSet set, String command, String arguments) throws Exception {
        return set.call(CommandFunctions.functionName(command), arguments, Runnable::run, () -> false).get(5, TimeUnit.SECONDS);
    }

    @Test
    void theModelNeverSeesAUuidAndCanUseAHandle() throws Exception {
        // the active leaf of the copy is the part it works on
        Manuscript copy = scope.getManuscript();
        copy.setActiveLeaf(copied("there was a dragon"));
        manuscriptService.save(copy);
        FunctionSet functions = functions();

        String peeked = call(functions, "peek-part", "{}");

        assertThat(peeked).contains("\"part\":\"h:m1\"");
        assertThat(peeked).doesNotContain(copied("there was a dragon").getUuid());
        assertThat(peeked).as("the uuid of the copy of the book is no item of the book").contains("\"book\":\"" + copy.getUuid() + "\"");
        assertThat(call(functions, "read-part", "{\"part\":\"h:m1\"}")).isEqualTo("there was a dragon");
    }

    @Test
    void theCommandsThatWriteTakeHandlesAndOnlyChangeTheCopy() throws Exception {
        Manuscript copy = scope.getManuscript();
        copy.setActiveLeaf(copied("there was a dragon"));
        manuscriptService.save(copy);
        FunctionSet functions = CommandFunctions.create(commandService, List.of("list-parts", "edit-part", "create-part", "delete-part"),
                DestructiveCommandMode.AUTO, l -> l.name(), null, null, null, null, new CommandCage(scope, handles));

        String listed = call(functions, "list-parts", "{}");
        assertThat(listed).contains("\"handle\":\"h:m1\"").contains("\"handle\":\"h:m2\"");
        assertThat(listed).doesNotContain(copied("Once upon a time").getUuid());

        call(functions, "edit-part", "{\"part\":\"h:m1\",\"old\":\"Once\",\"new\":\"Twice\"}");
        assertThat(copied("Twice upon a time")).isNotNull();
        assertThat(chatMessageService.find(first.getId()).getResponse()).as("the original").isEqualTo("Once upon a time");

        String created = call(functions, "create-part", "{\"text\":\"The end.\"}");
        assertThat(created).contains("\"handle\":\"h:m3\"").contains("\"position\":3");
        assertThat(copied("The end.").getParent().getId()).isEqualTo(copied("there was a dragon").getId());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> call(functions, "edit-part", "{\"part\":\"3\",\"old\":\"The\",\"new\":\"A\"}"))
                .hasMessageContaining("handle");
        call(functions, "delete-part", "{\"part\":\"h:m3\"}");
        assertThat(chatMessageService.find(copied("there was a dragon").getId()).isDeleted()).isFalse();
    }

    @Test
    void aHandleOfThePartOfTheOriginalOrAnUuidReachesNothing() throws Exception {
        FunctionSet functions = functions();

        // an uuid is no handle: it is passed as it is, and the cage refuses it as the part of the original is not in the copy
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> call(functions, "read-part", "{\"part\":\"" + first.getUuid() + "\"}"))
                .hasMessageContaining("The part does not exist");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> call(functions, "read-part", "{\"part\":\"h:m77\"}"))
                .hasMessageContaining("The part does not exist");
    }
}
