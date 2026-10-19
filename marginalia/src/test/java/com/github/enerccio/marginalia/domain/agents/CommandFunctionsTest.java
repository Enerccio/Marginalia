package com.github.enerccio.marginalia.domain.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.service.agent.CommandFunctions;
import com.github.enerccio.marginalia.domain.service.agent.CommandResults;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The commands an agent may call as functions: the names, the schema made from the forms of a command, the call that
 * picks the form, the bounded result, the errors for the model and the policy for the destructive commands.
 */
class CommandFunctionsTest {

    enum Field {RESPONSE, SCENE}

    private final CommandServiceImpl commands = new CommandServiceImpl();

    @AfterEach
    void tearDown() {
        commands.destroy();
    }

    /** The ladder of overloads of the manuscript commands: each form adds a parameter. */
    public static class Ladder {
        @Command(commandName = "list-things", humanNameText = "List things", descriptionText = "For the user",
                llmDescriptionText = "Lists the things", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature,
                returns = @CommandReturn(name = "things", type = String.class, descriptionText = "JSON of the page"))
        public void none(CommandContext context) {
            context.setReturn("things branch=null offset=null limit=null");
        }

        @Command(commandName = "list-things", humanNameText = "List things", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature(@CommandParameter(name = "branch", type = Integer.class,
                        descriptionText = "Number of the branch")))
        public void branch(CommandContext context) {
            context.setReturn("things branch=" + context.get("branch") + " offset=null limit=null");
        }

        @Command(commandName = "list-things", humanNameText = "List things", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature({
                        @CommandParameter(name = "branch", type = Integer.class),
                        @CommandParameter(name = "offset", type = Integer.class, defaultValue = "0")}))
        public void offset(CommandContext context) {
            context.setReturn("things branch=" + context.get("branch") + " offset=" + context.get("offset") + " limit=null");
        }

        @Command(commandName = "list-things", humanNameText = "List things", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature({
                        @CommandParameter(name = "branch", type = Integer.class),
                        @CommandParameter(name = "offset", type = Integer.class, defaultValue = "0"),
                        @CommandParameter(name = "limit", type = Integer.class)}))
        public void limit(CommandContext context) {
            context.setReturn("things branch=" + context.get("branch") + " offset=" + context.get("offset")
                    + " limit=" + context.get("limit"));
        }
    }

    public static class Others {
        @Command(commandName = "get.part", humanNameText = "Get part", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature({
                        @CommandParameter(name = "part", type = Integer.class),
                        @CommandParameter(name = "field", type = Field.class, optional = true),
                        @CommandParameter(name = "mode", type = String.class, optional = true, allowed = {"short", "long"},
                                defaultValue = "short")}))
        public void get(CommandContext context) {
            context.setReturn("part " + context.get("part") + " " + context.get("field") + " " + context.get("mode"));
        }

        @Command(commandName = "long", humanNameText = "Long", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature)
        public void text(CommandContext context) {
            context.setReturn(("line of the text\n").repeat(5_000));
        }

        @Command(commandName = "data", humanNameText = "Data", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature)
        public void data(CommandContext context) {
            context.setReturn(Map.of("count", 3));
        }

        @Command(commandName = "nothing", humanNameText = "Nothing", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature)
        public void nothing(CommandContext context) {
            context.setReturn(null);
        }

        @Command(commandName = "boom", humanNameText = "Boom", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                signature = @CommandParameterSignature)
        public void boom(CommandContext context) {
            throw new IllegalStateException("The book is locked by the user");
        }

        @Command(commandName = "anything", humanNameText = "Anything", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE)
        public void anything(CommandContext context) {
            context.setReturn("any " + context.getArguments());
        }
    }

    public static class Destructive {
        final AtomicInteger runs = new AtomicInteger();

        @Command(commandName = "wipe", humanNameText = "Wipe", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
                signature = @CommandParameterSignature(@CommandParameter(name = "what", type = String.class)))
        public void wipe(CommandContext context) {
            runs.incrementAndGet();
            context.setReturn("wiped " + context.get("what"));
        }
    }

    private FunctionSet functions(DestructiveCommandMode mode, CommandFunctions.Approval approval, String... names) {
        return CommandFunctions.create(commands, List.of(names), mode, l -> l.name(), approval);
    }

    private FunctionSet functions(String... names) {
        return functions(DestructiveCommandMode.ASK, null, names);
    }

    private static String call(FunctionSet set, String name, String arguments) throws Exception {
        return set.call(name, arguments, Runnable::run, () -> false).get(5, TimeUnit.SECONDS);
    }

    private static FunctionCallException failure(FunctionSet set, String name, String arguments) {
        CompletableFuture<String> result = set.call(name, arguments, Runnable::run, () -> false);
        assertThatThrownBy(() -> result.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(FunctionCallException.class);
        try {
            result.get();
        } catch (ExecutionException e) {
            return (FunctionCallException) e.getCause();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException("did not fail");
    }

    private static FunctionDefinition definition(FunctionSet set, String name) {
        return set.getDefinitions().stream().filter(d -> d.getName().equals(name)).findFirst().orElseThrow();
    }

    // --- names ---

    @Test
    void aValidCommandNameIsTheFunctionName() {
        assertThat(CommandFunctions.functionName("list-parts")).isEqualTo("list-parts");
        assertThat(CommandFunctions.functionName("take_backup")).isEqualTo("take_backup");
    }

    @Test
    void aNameWithADotIsWrittenWithAHashOfTheCommandName() {
        String name = CommandFunctions.functionName("foo.bar");

        assertThat(name).matches("foo_bar_[0-9a-f]{6}");
        assertThat(CommandFunctions.functionName("foo.bar")).as("stable").isEqualTo(name);
        assertThat(CommandFunctions.functionName("foo_bar")).as("not the command foo_bar").isEqualTo("foo_bar").isNotEqualTo(name);
        assertThat(CommandFunctions.functionName("foo.baz")).isNotEqualTo(name);
    }

    @Test
    void aLongNameIsCutToSixtyFourCharacters() {
        String command = "a".repeat(70);
        String name = CommandFunctions.functionName(command);

        assertThat(name).hasSize(64).startsWith("aaaa").matches("a+_[0-9a-f]{6}");
        assertThat(CommandFunctions.functionName("a".repeat(71))).isNotEqualTo(name);
        assertThat(CommandFunctions.functionName("b".repeat(64))).as("64 characters fit").hasSize(64).doesNotContain("_");
    }

    @Test
    void theNameDoesNotDependOnWhatElseIsRegistered() {
        commands.registerCommands(new Others());
        String alone = definition(functions("get.part"), CommandFunctions.functionName("get.part")).getName();

        commands.registerCommands(new Ladder());
        assertThat(definition(functions("get.part", "list-things"), alone).getName()).isEqualTo(alone);
    }

    // --- the offered set ---

    @Test
    void onlyTheAllowedCommandsAreOffered() {
        commands.registerCommands(new Ladder());
        commands.registerCommands(new Others());

        FunctionSet set = functions("list-things", "data", "not-registered");

        assertThat(set.getDefinitions()).extracting(FunctionDefinition::getName).containsExactlyInAnyOrder("list-things", "data");
        assertThat(functions().isEmpty()).isTrue();
    }

    @Test
    void theFormsOfACommandAreOneFunction() {
        commands.registerCommands(new Ladder());

        FunctionSet set = functions("list-things");

        assertThat(set.getDefinitions()).hasSize(1);
        FunctionDefinition definition = set.getDefinitions().getFirst();
        assertThat(definition.getDescription()).isEqualTo("Lists the things\nReturns: JSON of the page");
        JsonNode schema = definition.getParametersSchema();
        assertThat(schema.get("type").asText()).isEqualTo("object");
        assertThat(schema.get("properties").fieldNames()).toIterable().containsExactly("branch", "offset", "limit");
        assertThat(schema.get("properties").get("branch").get("type").asText()).isEqualTo("integer");
        assertThat(schema.get("properties").get("branch").get("description").asText()).isEqualTo("Number of the branch");
        assertThat(schema.get("properties").get("offset").get("default").asInt()).as("a number, not a text").isZero();
        assertThat(schema.get("additionalProperties").asBoolean()).isFalse();
    }

    @Test
    void aParameterIsRequiredWhenEveryFormHasIt() {
        commands.registerCommands(new Ladder());
        commands.registerCommands(new Others());

        // the ladder starts with the form without parameters: nothing is required
        assertThat(functions("list-things").getDefinitions().getFirst().getParametersSchema().has("required")).isFalse();

        JsonNode schema = definition(functions("get.part"), CommandFunctions.functionName("get.part")).getParametersSchema();
        assertThat(schema.get("required")).hasSize(1);
        assertThat(schema.get("required").get(0).asText()).isEqualTo("part");
    }

    @Test
    void enumsAndAllowedValuesAreInTheSchema() {
        commands.registerCommands(new Others());

        JsonNode properties = definition(functions("get.part"), CommandFunctions.functionName("get.part"))
                .getParametersSchema().get("properties");

        assertThat(properties.get("field").get("type").asText()).isEqualTo("string");
        assertThat(properties.get("field").get("enum")).extracting(JsonNode::asText).containsExactly("RESPONSE", "SCENE");
        assertThat(properties.get("mode").get("enum")).extracting(JsonNode::asText).containsExactly("short", "long");
        assertThat(properties.get("mode").get("default").asText()).isEqualTo("short");
    }

    @Test
    void aCommandWithoutASignatureTakesAnyParameters() throws Exception {
        commands.registerCommands(new Others());
        FunctionSet set = functions("anything");

        assertThat(set.getDefinitions().getFirst().getParametersSchema().get("additionalProperties").asBoolean()).isTrue();
        assertThat(call(set, "anything", "{\"x\":1,\"y\":\"two\"}")).isEqualTo("any {x=1, y=two}");
    }

    // --- the call ---

    @Test
    void theCallGoesToTheSmallestFormThatHasWhatWasPassed() throws Exception {
        commands.registerCommands(new Ladder());
        FunctionSet set = functions("list-things");

        assertThat(call(set, "list-things", "{}")).isEqualTo("things branch=null offset=null limit=null");
        assertThat(call(set, "list-things", "{\"branch\":2}")).isEqualTo("things branch=2 offset=null limit=null");
        // no form has offset alone: the one with branch and offset is called, branch is null
        assertThat(call(set, "list-things", "{\"offset\":5}")).isEqualTo("things branch=null offset=5 limit=null");
        // a middle option left out does not break the call
        assertThat(call(set, "list-things", "{\"branch\":1,\"limit\":10}")).isEqualTo("things branch=1 offset=0 limit=10");
    }

    @Test
    void aNullOrAnEmptyArgumentIsNotPassed() throws Exception {
        commands.registerCommands(new Ladder());
        FunctionSet set = functions("list-things");

        assertThat(call(set, "list-things", "{\"branch\":null,\"limit\":null}")).isEqualTo("things branch=null offset=null limit=null");
        assertThat(call(set, "list-things", "")).isEqualTo("things branch=null offset=null limit=null");
    }

    @Test
    void textForANumberIsConverted() throws Exception {
        commands.registerCommands(new Ladder());

        assertThat(call(functions("list-things"), "list-things", "{\"branch\":\"3\"}")).isEqualTo("things branch=3 offset=null limit=null");
    }

    @Test
    void theDefaultIsUsedForAParameterTheModelLeftOut() throws Exception {
        commands.registerCommands(new Others());
        String name = CommandFunctions.functionName("get.part");

        assertThat(call(functions("get.part"), name, "{\"part\":4}")).isEqualTo("part 4 null short");
        assertThat(call(functions("get.part"), name, "{\"part\":4,\"field\":\"scene\",\"mode\":\"long\"}")).isEqualTo("part 4 SCENE long");
    }

    @Test
    void aMissingRequiredParameterIsAnErrorForTheModel() {
        commands.registerCommands(new Others());
        String name = CommandFunctions.functionName("get.part");

        FunctionCallException error = failure(functions("get.part"), name, "{\"mode\":\"long\"}");

        assertThat(error.getKind()).isEqualTo(FunctionCallException.Kind.INVALID_ARGUMENTS);
        assertThat(error.getMessage()).isEqualTo("Missing required parameter 'part'");
    }

    @Test
    void aValueThatIsNotAllowedIsAnErrorForTheModel() {
        commands.registerCommands(new Others());
        String name = CommandFunctions.functionName("get.part");

        assertThat(failure(functions("get.part"), name, "{\"part\":1,\"mode\":\"huge\"}").getMessage())
                .isEqualTo("Parameter 'mode' must be one of [short, long]");
    }

    @Test
    void anUnknownParameterIsAnErrorThatListsTheKnownOnes() {
        commands.registerCommands(new Ladder());

        assertThat(failure(functions("list-things"), "list-things", "{\"page\":2}").getMessage())
                .isEqualTo("Unknown parameter(s) [page], the parameters are [branch, offset, limit]");
    }

    @Test
    void aValueOfAWrongTypeIsAnErrorFromTheCommand() {
        commands.registerCommands(new Ladder());

        assertThat(failure(functions("list-things"), "list-things", "{\"branch\":\"first\"}").getMessage())
                .contains("No form of command list-things");
    }

    @Test
    void whenTheCommandFailsTheModelGetsItsMessage() {
        commands.registerCommands(new Others());

        FunctionCallException error = failure(functions("boom"), "boom", "{}");

        assertThat(error.getMessage()).isEqualTo("The book is locked by the user");
    }

    @Test
    void aCommandThatIsGoneIsAnError() {
        commands.registerCommands(new Others());
        FunctionSet set = functions("data");
        commands.unregisterCommand("data");

        assertThat(failure(set, "data", "{}").getMessage()).isEqualTo("The command data is not available now");
    }

    // --- the result ---

    @Test
    void textIsGivenAsItIsAndAnythingElseAsJson() throws Exception {
        commands.registerCommands(new Others());
        FunctionSet set = functions("data", "nothing");

        assertThat(call(set, "data", "{}")).isEqualTo("{\"count\":3}");
        assertThat(call(set, "nothing", "{}")).isEqualTo("OK");
    }

    @Test
    void aLongResultIsCutAtALineWithANote() throws Exception {
        commands.registerCommands(new Others());

        String result = call(functions("long"), "long", "{}");

        assertThat(result.length()).isLessThan(CommandFunctions.MAX_RESULT_LENGTH + 200);
        assertThat(result).contains("line of the text\n[cut: ").contains(" of 85000 characters");
        String kept = result.substring(0, result.indexOf("[cut:"));
        assertThat(kept).endsWith("\n");
        assertThat(kept.lines()).allMatch("line of the text"::equals);
    }

    // --- the results kept for the model ---

    @Test
    void theResultsAreKeptWholeWithTheCallThatMadeThem() throws Exception {
        commands.registerCommands(new Ladder());
        commands.registerCommands(new Others());
        CommandResults results = new CommandResults();
        FunctionSet set = CommandFunctions.create(commands, List.of("list-things", "long", "data"), DestructiveCommandMode.AUTO,
                l -> l.name(), null, null, results);

        assertThat(results.last()).isNull();
        call(set, "list-things", "{\"branch\":2,\"offset\":5}");
        String shown = call(set, "long", "{}");
        call(set, "data", "{}");

        assertThat(shown).contains("[cut: ");
        assertThat(results.list()).extracting(CommandResults.Entry::id).containsExactly(1, 2, 3);
        assertThat(results.get(1).function()).isEqualTo("list-things");
        assertThat(results.get(1).arguments()).isEqualTo("branch=2, offset=5");
        assertThat(results.get(1).text()).isEqualTo("things branch=2 offset=5 limit=null");
        assertThat(results.get(2).text()).as("whole, not cut").hasSize(5_000 * "line of the text\n".length());
        assertThat(results.last().function()).isEqualTo("data");
        assertThat(results.last().text()).contains("\"count\"");
    }

    @Test
    void aFailedCallKeepsNothingAndOnlyTheLastTenAreKept() throws Exception {
        commands.registerCommands(new Ladder());
        commands.registerCommands(new Others());
        CommandResults results = new CommandResults();
        FunctionSet set = CommandFunctions.create(commands, List.of("list-things", "data"), DestructiveCommandMode.AUTO,
                l -> l.name(), null, null, results);

        failure(set, "list-things", "{\"branch\":\"many\"}");
        assertThat(results.list()).isEmpty();
        for (int i = 0; i < 13; i++) {
            call(set, "data", "{}");
        }

        assertThat(results.list()).hasSize(CommandResults.KEEP);
        assertThat(results.list().getFirst().id()).isEqualTo(4);
        assertThat(results.get(3)).as("older than the last ten").isNull();
        assertThat(results.last().id()).isEqualTo(13);
    }

    @Test
    void theArgumentsOfAResultAreShortened() {
        String argument = "x".repeat(500) + " and\nmore";
        String described = CommandFunctions.describeArguments(new java.util.LinkedHashMap<>(Map.of("text", argument)));

        assertThat(described).startsWith("text=" + "x".repeat(40) + "…").hasSize(46).doesNotContain("\n");
        assertThat(CommandFunctions.describeArguments(new java.util.LinkedHashMap<>())).isEmpty();
    }

    // --- the policy ---

    @Test
    void aDestructiveCommandIsLeftOutWhenDenied() {
        commands.registerCommands(new Destructive());
        commands.registerCommands(new Others());

        FunctionSet set = functions(DestructiveCommandMode.DENY, null, "wipe", "data");

        assertThat(set.getDefinitions()).extracting(FunctionDefinition::getName).containsExactly("data");
    }

    @Test
    void aDestructiveCommandRunsWhenAuto() throws Exception {
        Destructive suite = new Destructive();
        commands.registerCommands(suite);

        assertThat(call(functions(DestructiveCommandMode.AUTO, null, "wipe"), "wipe", "{\"what\":\"all\"}")).isEqualTo("wiped all");
        assertThat(suite.runs).hasValue(1);
    }

    @Test
    void askWithoutAnApprovalRefusesTheDestructiveCommand() {
        Destructive suite = new Destructive();
        commands.registerCommands(suite);

        FunctionCallException error = failure(functions(DestructiveCommandMode.ASK, null, "wipe"), "wipe", "{\"what\":\"all\"}");

        assertThat(error.getMessage()).contains("needs the approval of the user");
        assertThat(suite.runs).hasValue(0);
    }

    @Test
    void askWaitsForTheDecisionOfTheUser() throws Exception {
        Destructive suite = new Destructive();
        commands.registerCommands(suite);
        List<String> asked = new ArrayList<>();
        CompletableFuture<Boolean> decision = new CompletableFuture<>();
        FunctionSet set = functions(DestructiveCommandMode.ASK, (name, arguments) -> {
            asked.add(name + arguments);
            return decision;
        }, "wipe");

        CompletableFuture<String> result = set.call("wipe", "{\"what\":\"all\"}", Runnable::run, () -> false);

        assertThat(result).isNotDone();
        assertThat(asked).containsExactly("wipe{what=all}");
        assertThat(suite.runs).hasValue(0);
        decision.complete(true);
        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("wiped all");
        assertThat(suite.runs).hasValue(1);
    }

    @Test
    void aDeniedApprovalIsAnErrorForTheModel() {
        Destructive suite = new Destructive();
        commands.registerCommands(suite);
        FunctionSet set = functions(DestructiveCommandMode.ASK, (name, arguments) -> CompletableFuture.completedFuture(false), "wipe");

        FunctionCallException error = failure(set, "wipe", "{\"what\":\"all\"}");

        assertThat(error.getMessage()).isEqualTo("The user did not approve the command wipe");
        assertThat(suite.runs).hasValue(0);
    }

    @Test
    void aNonDestructiveCommandNeedsNoApproval() throws Exception {
        commands.registerCommands(new Others());

        assertThat(call(functions(DestructiveCommandMode.ASK, null, "data"), "data", "{}")).isEqualTo("{\"count\":3}");
    }

    // --- files as arguments ---

    private FunctionSet withFiles(DestructiveCommandMode mode, CommandFunctions.Approval approval,
                                  java.util.function.Function<String, String> files, String... names) {
        return CommandFunctions.create(commands, List.of(names), mode, l -> l.name(), approval, files);
    }

    private static final java.util.function.Function<String, String> FILES = path -> {
        if (path.equals("/notes/draft.md")) {
            return "the long draft\nof two lines";
        }
        throw new com.github.enerccio.marginalia.domain.service.agent.VirtualFileException("There is no file " + path);
    };

    @Test
    void aTextArgumentThatIsAFileReferenceIsTheContentsOfTheFile() throws Exception {
        commands.registerCommands(new Destructive());
        FunctionSet set = withFiles(DestructiveCommandMode.AUTO, null, FILES, "wipe");

        assertThat(call(set, "wipe", "{\"what\":\"@file:/notes/draft.md\"}")).isEqualTo("wiped the long draft\nof two lines");
        // anything else is the text it is
        assertThat(call(set, "wipe", "{\"what\":\"read @file:/notes/draft.md\"}")).isEqualTo("wiped read @file:/notes/draft.md");
        assertThat(call(set, "wipe", "{\"what\":\"plain\"}")).isEqualTo("wiped plain");
    }

    @Test
    void aFileThatCanNotBeReadIsAnErrorNamingTheArgument() {
        commands.registerCommands(new Destructive());
        FunctionSet set = withFiles(DestructiveCommandMode.AUTO, null, FILES, "wipe");

        FunctionCallException error = failure(set, "wipe", "{\"what\":\"@file:/missing.md\"}");

        assertThat(error.getMessage()).isEqualTo("The argument what: There is no file /missing.md");
    }

    @Test
    void withoutFilesTheReferenceIsJustText() throws Exception {
        commands.registerCommands(new Destructive());
        FunctionSet set = functions(DestructiveCommandMode.AUTO, null, "wipe");

        assertThat(call(set, "wipe", "{\"what\":\"@file:/notes/draft.md\"}")).isEqualTo("wiped @file:/notes/draft.md");
        assertThat(definition(set, "wipe").getParametersSchema().toString()).doesNotContain("@file:");
    }

    @Test
    void theApprovalSeesTheTextThatWillRun() throws Exception {
        commands.registerCommands(new Destructive());
        java.util.concurrent.atomic.AtomicReference<Map<String, Object>> seen = new java.util.concurrent.atomic.AtomicReference<>();
        FunctionSet set = withFiles(DestructiveCommandMode.ASK, (name, arguments) -> {
            seen.set(arguments);
            return CompletableFuture.completedFuture(true);
        }, FILES, "wipe");

        call(set, "wipe", "{\"what\":\"@file:/notes/draft.md\"}");

        assertThat(seen.get()).containsEntry("what", "the long draft\nof two lines");
    }

    @Test
    void theTextParametersSayThatAFileCanBePassedButTheOthersDoNot() {
        commands.registerCommands(new Others());
        FunctionSet set = withFiles(DestructiveCommandMode.AUTO, null, FILES, "get.part");
        String schema = definition(set, CommandFunctions.functionName("get.part")).getParametersSchema().toString();

        // the part is a number and the mode one of a few values: neither takes a file
        assertThat(schema).doesNotContain("@file:");
        commands.registerCommands(new Destructive());
        assertThat(definition(withFiles(DestructiveCommandMode.AUTO, null, FILES, "wipe"), "wipe").getParametersSchema().toString())
                .contains("@file:/path");
    }

    // --- the results as arguments ---

    private FunctionSet withResults(CommandResults results, Function<String, String> files) {
        if (commands.getCommands("wipe").isEmpty()) {
            commands.registerCommands(new Ladder());
            commands.registerCommands(new Others());
            commands.registerCommands(new Destructive());
        }
        return CommandFunctions.create(commands, List.of("list-things", "long", "wipe"), DestructiveCommandMode.AUTO, l -> l.name(), null,
                files, results);
    }

    @Test
    void aTextArgumentThatIsAResultReferenceIsTheWholeResult() throws Exception {
        CommandResults results = new CommandResults();
        FunctionSet set = withResults(results, null);

        call(set, "list-things", "{\"branch\":2}");
        call(set, "long", "{}");
        call(set, "list-things", "{\"branch\":3}");

        assertThat(call(set, "wipe", "{\"what\":\"@result\"}")).isEqualTo("wiped things branch=3 offset=null limit=null");
        assertThat(call(set, "wipe", "{\"what\":\"@result:#1\"}")).isEqualTo("wiped things branch=2 offset=null limit=null");
        assertThat(call(set, "wipe", "{\"what\":\"@result:2\"}")).contains("[cut: ");
        assertThat(results.last().text()).as("whole, not cut").hasSize("wiped ".length() + 5_000 * "line of the text\n".length());
        // anything else is the text it is
        assertThat(call(set, "wipe", "{\"what\":\"see @result:1\"}")).isEqualTo("wiped see @result:1");
        // the list shows the reference, not the text
        assertThat(results.last().arguments()).isEqualTo("what=see @result:1");
        assertThat(results.get(5).arguments()).isEqualTo("what=@result:#1");
    }

    @Test
    void aResultThatIsNotThereIsAnErrorNamingTheArgument() throws Exception {
        CommandResults results = new CommandResults();
        FunctionSet set = withResults(results, null);

        assertThat(failure(set, "wipe", "{\"what\":\"@result\"}").getMessage())
                .isEqualTo("The argument what: There is no result to use yet: call a command first");
        call(set, "list-things", "{\"branch\":2}");
        assertThat(failure(set, "wipe", "{\"what\":\"@result:7\"}").getMessage())
                .isEqualTo("The argument what: There is no result #7, list_command_results shows the ones that are kept");
        assertThat(failure(set, "wipe", "{\"what\":\"@result:last\"}").getMessage()).contains("is not a reference to a result");
    }

    @Test
    void withoutKeptResultsTheReferenceIsJustText() throws Exception {
        commands.registerCommands(new Destructive());
        FunctionSet set = functions(DestructiveCommandMode.AUTO, null, "wipe");

        assertThat(call(set, "wipe", "{\"what\":\"@result\"}")).isEqualTo("wiped @result");
        assertThat(definition(set, "wipe").getParametersSchema().toString()).doesNotContain("@result");
        assertThat(definition(withResults(new CommandResults(), null), "wipe").getParametersSchema().toString()).contains("@result:3")
                .doesNotContain("@file:");
        assertThat(definition(withResults(new CommandResults(), FILES), "wipe").getParametersSchema().toString())
                .contains("@file:/path").contains("@result:3");
    }
}
