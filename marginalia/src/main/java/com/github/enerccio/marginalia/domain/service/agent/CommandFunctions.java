package com.github.enerccio.marginalia.domain.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandException;
import com.github.enerccio.marginalia.domain.service.command.CommandParameterDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException.Kind;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.loc.L;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The commands an agent may call, as functions of the model: one function per command, with the parameters of all its
 * forms (the overloads) as one schema.
 * <ul>
 * <li><b>Name</b>: a command name that is a valid function name is the name of the function. Any other ({@code .} is legal
 * in a command, not in a function) is written with '_' for each character that is not allowed and a suffix of six
 * hexadecimal digits of the hash of the command name, cut so that the whole is at most 64 characters. The rule depends on
 * the name only, never on what else is registered or in which order, so a name in a stored transcript means the same
 * command after a restart.</li>
 * <li><b>Parameters</b>: the union of the parameters of the forms, in the order of the ladder. A parameter is required
 * when every form with a declared signature has it and none declares it
 * {@link com.github.enerccio.marginalia.domain.traits.CommandParameter#optional() optional}. A call goes to the form with
 * the fewest parameters that has all the arguments the model passed; the parameters of that form it left out are null
 * (or the declared default), so the model never has to know the ladder. A command without a declared signature takes
 * any arguments.</li>
 * <li><b>Result</b>: text as it is, anything else as JSON, nothing as "OK"; longer than {@link #MAX_RESULT_LENGTH} it is
 * cut with a note. A failure is the message of what went wrong (the inference service writes it to the model as
 * {@code Error: <message>}).</li>
 * <li><b>Files</b>: a text argument that is exactly {@code @file:/path} is replaced, before the command runs, by the text of
 * that file of the agent's own file system (when the caller gave a reader), so a long text goes through the model once, in
 * the file, and the call stays short. The call keeps the reference, so what a file held is read when the call is made.</li>
 * <li><b>Results</b>: the same for {@code @result} (the newest result of the run) and {@code @result:3} (the result #3,
 * see {@link CommandResults}): the whole text of that result, before it was cut for the model, so the text of one command
 * goes into the next without passing through the model.</li>
 * <li><b>Images</b>: a parameter of the type {@code LLMAttachment[]} (the images a command sends to its model) is
 * offered to the model as a list of paths of image files of its workspace, which are read, scaled down and passed to the
 * command as attachments, when the caller gave a reader of images; without one the parameter does not exist for the model.
 * It is the model of the command that sees the images, so an agent whose own model can't see images may pass them too.</li>
 * <li><b>Policy</b>: only the allowed commands exist for the model. A destructive one follows the
 * {@link DestructiveCommandMode} of the agent: {@code DENY} leaves it out, {@code ASK} waits for the {@link Approval}
 * (refused when there is none), {@code AUTO} runs it.</li>
 * </ul>
 * The commands run as whoever the calling thread is: the functions are called with the request context of the run
 * ({@link com.github.enerccio.marginalia.bound.AgentRequestAttributes}), which the command service hands on to the
 * commands that run on a worker thread.
 */
public final class CommandFunctions {

    private static final Logger log = LoggerFactory.getLogger(CommandFunctions.class);

    /** The longest result given to the model, in characters. */
    public static final int MAX_RESULT_LENGTH = 20_000;

    /** The most images the model passes to one call. */
    public static final int MAX_IMAGES = 8;

    /** A text argument that starts with this is a reference to a file, see the class description. */
    public static final String FILE_PREFIX = "@file:";

    private static final int MAX_NAME_LENGTH = 64;
    private static final int SUFFIX_LENGTH = 6;
    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * The decision of the user about a destructive command, asked when the agent is in the {@code ASK} mode.
     */
    @FunctionalInterface
    public interface Approval {

        /**
         * @param commandName the command the agent wants to run
         * @param arguments   what it passes, by parameter name
         * @return whether the user agrees; completes when they decide (the run waits for it)
         */
        CompletableFuture<Boolean> approve(String commandName, Map<String, Object> arguments);

    }

    private CommandFunctions() {
    }

    /**
     * The name of the function of a command, see the class description.
     */
    public static String functionName(String commandName) {
        String clean = commandName.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (clean.equals(commandName) && commandName.length() <= MAX_NAME_LENGTH) {
            return commandName;
        }
        String suffix = hash(commandName);
        String base = clean.length() > MAX_NAME_LENGTH - SUFFIX_LENGTH - 1 ? clean.substring(0, MAX_NAME_LENGTH - SUFFIX_LENGTH - 1) : clean;
        return base + "_" + suffix;
    }

    private static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, SUFFIX_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The commands the agent is allowed, as functions; its destructive mode decides about the destructive ones.
     *
     * @param approval who decides in the {@code ASK} mode, null when nobody can
     */
    public static FunctionSet forAgent(CommandService commands, Agent agent, Function<L, String> lookup, Approval approval) {
        return forAgent(commands, agent, lookup, approval, null);
    }

    /**
     * As {@link #forAgent(CommandService, Agent, Function, Approval)}, with the files of the agent: a text argument
     * {@code @file:/path} is the contents of that file.
     *
     * @param files reads the text of a file of the agent by its path and fails with the reason (the message goes to the
     *              model); null when the agent has no files, the arguments are then passed as they are
     */
    public static FunctionSet forAgent(CommandService commands, Agent agent, Function<L, String> lookup, Approval approval,
                                       Function<String, String> files) {
        return forAgent(commands, agent, lookup, approval, files, null);
    }

    /**
     * As {@link #forAgent(CommandService, Agent, Function, Approval, Function)}, and every result a command returns is
     * kept whole in {@code commandResults} (the model gets it cut when it is long).
     *
     * @param commandResults where the result of each call is put, null to keep none
     */
    public static FunctionSet forAgent(CommandService commands, Agent agent, Function<L, String> lookup, Approval approval,
                                       Function<String, String> files, CommandResults commandResults) {
        return forAgent(commands, agent, lookup, approval, files, commandResults, null);
    }

    /**
     * As {@link #forAgent(CommandService, Agent, Function, Approval, Function, CommandResults)}, and the commands that
     * take images get them from the files of the agent.
     *
     * @param images reads an image file of the agent by its path as the attachment of a message and fails with the reason
     *               (the message goes to the model); null when the agent has no files, the commands then have no
     *               image parameters for the model
     */
    public static FunctionSet forAgent(CommandService commands, Agent agent, Function<L, String> lookup, Approval approval,
                                       Function<String, String> files, CommandResults commandResults,
                                       Function<String, LLMAttachment> images) {
        return forAgent(commands, agent, lookup, approval, files, commandResults, images, null);
    }

    /**
     * As {@link #forAgent(CommandService, Agent, Function, Approval, Function, CommandResults, Function)}, and the commands
     * run in the scope of the cage and the agent works with the handles of the cage instead of the uuids.
     *
     * @param cage the scope of the commands and the handles; null runs them in the scope of the session, with the uuids
     */
    public static FunctionSet forAgent(CommandService commands, Agent agent, Function<L, String> lookup, Approval approval,
                                       Function<String, String> files, CommandResults commandResults,
                                       Function<String, LLMAttachment> images, CommandCage cage) {
        return create(commands, agent.getAllowedCommands(), agent.getDestructiveMode(), lookup, approval, files, commandResults, images, cage);
    }

    /**
     * @param allowed  names of the commands to offer; those that are not registered are skipped
     * @param mode     what happens with a destructive command
     * @param lookup   the texts of the commands (the descriptions are for the model, so in the language of the run)
     * @param approval who decides in the {@code ASK} mode, null when nobody can
     */
    public static FunctionSet create(CommandService commands, Collection<String> allowed, DestructiveCommandMode mode,
                                     Function<L, String> lookup, Approval approval) {
        return create(commands, allowed, mode, lookup, approval, null);
    }

    /**
     * @param files reads the text of a file by its path, null when there are no files; see
     *              {@link #forAgent(CommandService, Agent, Function, Approval, Function)}
     */
    public static FunctionSet create(CommandService commands, Collection<String> allowed, DestructiveCommandMode mode,
                                     Function<L, String> lookup, Approval approval, Function<String, String> files) {
        return create(commands, allowed, mode, lookup, approval, files, null);
    }

    /**
     * @param commandResults where the result of each call is put, null to keep none; see
     *                   {@link #forAgent(CommandService, Agent, Function, Approval, Function, CommandResults)}
     */
    public static FunctionSet create(CommandService commands, Collection<String> allowed, DestructiveCommandMode mode,
                                     Function<L, String> lookup, Approval approval, Function<String, String> files,
                                     CommandResults commandResults) {
        return create(commands, allowed, mode, lookup, approval, files, commandResults, null);
    }

    /**
     * @param images reads an image file by its path, null when there are no files; see
     *               {@link #forAgent(CommandService, Agent, Function, Approval, Function, CommandResults, Function)}
     */
    public static FunctionSet create(CommandService commands, Collection<String> allowed, DestructiveCommandMode mode,
                                     Function<L, String> lookup, Approval approval, Function<String, String> files,
                                     CommandResults commandResults, Function<String, LLMAttachment> images) {
        return create(commands, allowed, mode, lookup, approval, files, commandResults, images, null);
    }

    /**
     * @param cage the scope the commands run in and the handles of the agent, see
     *             {@link #forAgent(CommandService, Agent, Function, Approval, Function, CommandResults, Function, CommandCage)}
     */
    public static FunctionSet create(CommandService commands, Collection<String> allowed, DestructiveCommandMode mode,
                                     Function<L, String> lookup, Approval approval, Function<String, String> files,
                                     CommandResults commandResults, Function<String, LLMAttachment> images, CommandCage cage) {
        Set<String> names = new HashSet<>(allowed);
        Map<String, List<CommandDefinition>> byName = new TreeMap<>();
        for (CommandDefinition definition : commands.getCommands()) {
            if (definition.availableForAgent() && names.contains(definition.commandName())) {
                byName.computeIfAbsent(definition.commandName(), _ -> new ArrayList<>()).add(definition);
            }
        }

        Map<String, FunctionDefinition> functions = new LinkedHashMap<>();
        for (Map.Entry<String, List<CommandDefinition>> entry : byName.entrySet()) {
            List<CommandDefinition> forms = entry.getValue();
            boolean destructive = forms.getFirst().dataSeverity().isDestructive();
            if (destructive && mode == DestructiveCommandMode.DENY) {
                continue;
            }
            String functionName = functionName(entry.getKey());
            FunctionDefinition definition = FunctionDefinition.dynamic(functionName, describe(forms, lookup), schema(forms, lookup, files != null, commandResults != null, images != null),
                    (continuation, arguments) -> call(commands, entry.getKey(), destructive ? mode : DestructiveCommandMode.AUTO,
                            approval, files, commandResults, images, cage, continuation, arguments));
            if (functions.putIfAbsent(functionName, definition) != null) {
                // two names with the same hash, which is not going to happen; the first (by name) wins
                log.warn("Command {} has the function name {} of another command, it is not offered", entry.getKey(), functionName);
            }
        }
        return FunctionSet.bind(functions.values(), null);
    }

    private static String describe(List<CommandDefinition> forms, Function<L, String> lookup) {
        CommandDefinition first = forms.getFirst();
        String text = first.llmDescription().get(lookup);
        if (text.isBlank()) {
            text = first.description().get(lookup);
        }
        if (text.isBlank()) {
            text = first.humanName().get(lookup);
        }
        String returns = first.returns().isVoid() ? "" : first.returns().description().get(lookup);
        return returns.isBlank() ? text : text + "\nReturns: " + returns;
    }

    // --- the schema ---

    private static List<CommandDefinition> declared(List<CommandDefinition> forms) {
        return forms.stream().filter(form -> !form.anyArguments()).toList();
    }

    /** The parameters of all the forms by name, in the order they first appear. */
    private static Map<String, List<CommandParameterDefinition>> parametersOf(List<CommandDefinition> forms) {
        Map<String, List<CommandParameterDefinition>> parameters = new LinkedHashMap<>();
        for (CommandDefinition form : declared(forms)) {
            for (CommandParameterDefinition parameter : form.parameters()) {
                parameters.computeIfAbsent(parameter.name(), _ -> new ArrayList<>()).add(parameter);
            }
        }
        return parameters;
    }

    private static Set<String> requiredOf(List<CommandDefinition> forms) {
        List<CommandDefinition> declared = declared(forms);
        Set<String> required = new LinkedHashSet<>();
        parametersOf(forms).forEach((name, parameters) -> {
            if (parameters.size() == declared.size() && parameters.stream().noneMatch(CommandParameterDefinition::optional)) {
                required.add(name);
            }
        });
        return required;
    }

    private static ObjectNode schema(List<CommandDefinition> forms, Function<L, String> lookup, boolean files, boolean results,
                                     boolean images) {
        ObjectNode properties = JSON.createObjectNode();
        parametersOf(forms).forEach((name, parameters) -> {
            if (isImages(parameters)) {
                if (images) {
                    properties.set(name, imagesSchema(parameters, lookup));
                }
                return;
            }
            properties.set(name, parameterSchema(parameters, lookup, files, results));
        });
        ObjectNode root = JSON.createObjectNode();
        root.put("type", "object");
        root.set("properties", properties);
        ArrayNode required = JSON.createArrayNode();
        requiredOf(forms).stream().filter(name -> images || !isImages(parametersOf(forms).get(name))).forEach(required::add);
        if (!required.isEmpty()) {
            root.set("required", required);
        }
        root.put("additionalProperties", forms.stream().anyMatch(CommandDefinition::anyArguments));
        return root;
    }

    private static ObjectNode parameterSchema(List<CommandParameterDefinition> forms, Function<L, String> lookup, boolean files,
                                                boolean results) {
        CommandParameterDefinition first = forms.getFirst();
        // forms that disagree about the type take anything
        Class<?> type = forms.stream().allMatch(p -> p.type() == first.type()) ? first.type() : Object.class;
        ObjectNode schema = typeSchema(type);
        forms.stream().map(p -> p.description().get(lookup)).filter(text -> !text.isBlank()).findFirst()
                .ifPresent(text -> schema.put("description", text));
        List<String> allowed = forms.stream().map(CommandParameterDefinition::allowed).filter(a -> !a.isEmpty()).findFirst().orElse(List.of());
        if ((files || results) && allowed.isEmpty() && isText(forms)) {
            String text = schema.path("description").asText("");
            schema.put("description", (text.isEmpty() ? "" : text + " ") + (files ? Defaults.AGENT_FILE_ARGUMENT_HINT : "")
                    + (files && results ? " " : "") + (results ? Defaults.AGENT_RESULT_ARGUMENT_HINT : ""));
        }
        if (!allowed.isEmpty()) {
            ArrayNode values = JSON.createArrayNode();
            allowed.forEach(value -> values.add(valueNode(type, value)));
            schema.set("enum", values);
        }
        forms.stream().map(CommandParameterDefinition::defaultValue).filter(value -> !value.isEmpty()).findFirst()
                .ifPresent(value -> schema.set("default", valueNode(type, value)));
        return schema;
    }

    /** Whether the parameter takes images (in every form that has it), which the model names by paths of files. */
    private static boolean isImages(List<CommandParameterDefinition> forms) {
        return forms.stream().allMatch(p -> p.type() == LLMAttachment[].class);
    }

    private static ObjectNode imagesSchema(List<CommandParameterDefinition> forms, Function<L, String> lookup) {
        ObjectNode schema = JSON.createObjectNode();
        schema.put("type", "array");
        schema.set("items", JSON.createObjectNode().put("type", "string"));
        String text = forms.stream().map(p -> p.description().get(lookup)).filter(t -> !t.isBlank()).findFirst().orElse("");
        schema.put("description", (text.isEmpty() ? "" : text + " ") + Defaults.AGENT_IMAGES_ARGUMENT_HINT);
        return schema;
    }

    /** Whether the parameter is a text in every form that has it. */
    private static boolean isText(List<CommandParameterDefinition> forms) {
        return forms.stream().allMatch(p -> CharSequence.class.isAssignableFrom(boxed(p.type())));
    }

    private static ObjectNode typeSchema(Class<?> type) {
        ObjectNode schema = JSON.createObjectNode();
        Class<?> boxed = boxed(type);
        if (boxed == Object.class) {
            // any value
            return schema;
        }
        if (CharSequence.class.isAssignableFrom(boxed)) {
            schema.put("type", "string");
        } else if (boxed == Character.class) {
            schema.put("type", "string");
            schema.put("minLength", 1);
            schema.put("maxLength", 1);
        } else if (boxed == Boolean.class) {
            schema.put("type", "boolean");
        } else if (boxed == Integer.class || boxed == Long.class || boxed == Short.class || boxed == Byte.class
                || boxed == BigInteger.class) {
            schema.put("type", "integer");
        } else if (Number.class.isAssignableFrom(boxed)) {
            schema.put("type", "number");
        } else if (boxed.isEnum()) {
            schema.put("type", "string");
            ArrayNode values = JSON.createArrayNode();
            for (Object constant : boxed.getEnumConstants()) {
                values.add(((Enum<?>) constant).name());
            }
            schema.set("enum", values);
        } else if (Collection.class.isAssignableFrom(boxed) || boxed.isArray()) {
            schema.put("type", "array");
        } else if (Map.class.isAssignableFrom(boxed)) {
            schema.put("type", "object");
        }
        return schema;
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "boolean" -> Boolean.class;
            case "char" -> Character.class;
            default -> type;
        };
    }

    /** The text of a default or an allowed value as the JSON of the type, text when it does not fit. */
    private static JsonNode valueNode(Class<?> type, String text) {
        Class<?> boxed = boxed(type);
        try {
            if (boxed == Integer.class || boxed == Long.class || boxed == Short.class || boxed == Byte.class) {
                return JSON.getNodeFactory().numberNode(Long.parseLong(text.trim()));
            }
            if (boxed == Double.class || boxed == Float.class || boxed == BigDecimal.class) {
                return JSON.getNodeFactory().numberNode(Double.parseDouble(text.trim()));
            }
        } catch (NumberFormatException e) {
            // the author wrote a text for a number, the model gets it as written
        }
        if (boxed == Boolean.class && (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false"))) {
            return JSON.getNodeFactory().booleanNode(Boolean.parseBoolean(text));
        }
        return JSON.getNodeFactory().textNode(text);
    }

    // --- the call ---

    private static void call(CommandService commands, String commandName, DestructiveCommandMode mode, Approval approval,
                             Function<String, String> files, CommandResults commandResults, Function<String, LLMAttachment> images,
                             CommandCage cage, FunctionContinuation<Object> continuation, Map<String, Object> passed) {
        try {
            List<CommandDefinition> forms = commands.getCommands(commandName);
            if (forms.isEmpty()) {
                throw new FunctionCallException(Kind.FAILED, "The command " + commandName + " is not available now");
            }
            Map<String, Object> arguments = new LinkedHashMap<>();
            passed.forEach((name, value) -> {
                if (value != null) {
                    arguments.put(name, value);
                }
            });
            Map<String, String> references = readReferences(forms, arguments, files, commandResults);
            references.putAll(readImages(forms, arguments, images));
            Map<String, Object> complete = complete(forms, arguments);

            if (mode == DestructiveCommandMode.DENY) {
                throw new FunctionCallException(Kind.FAILED, "The command " + commandName + " changes or removes data and is not allowed");
            }
            if (mode == DestructiveCommandMode.ASK) {
                if (approval == null) {
                    throw new FunctionCallException(Kind.FAILED, "The command " + commandName
                            + " changes or removes data and needs the approval of the user, which can not be asked now");
                }
                approval.approve(commandName, complete).whenComplete((approved, error) -> {
                    if (error != null) {
                        continuation.fail(new FunctionCallException(Kind.FAILED, messageOf(error), error));
                    } else if (!Boolean.TRUE.equals(approved)) {
                        continuation.fail(new FunctionCallException(Kind.FAILED, "The user did not approve the command " + commandName));
                    } else {
                        run(commands, commandName, complete, references, commandResults, cage, continuation);
                    }
                });
                return;
            }
            run(commands, commandName, complete, references, commandResults, cage, continuation);
        } catch (RuntimeException e) {
            continuation.fail(e);
        }
    }

    /**
     * Puts the text in the place of a text argument that is {@code @file:/path} (the contents of that file) or
     * {@code @result} / {@code @result:3} (a result of an earlier call, whole). Anything else, an argument that is not a
     * text parameter too, is left as the model wrote it.
     *
     * @param files          null when the agent has no files, {@code @file:} is then not a reference
     * @param commandResults null when no results are kept, {@code @result} is then not a reference
     * @return the references that were replaced, by the name of the argument
     * @throws FunctionCallException with the reason when a file can't be read or the result is not kept, the model
     *                               corrects the call
     */
    private static Map<String, String> readReferences(List<CommandDefinition> forms, Map<String, Object> arguments,
                                                      Function<String, String> files, CommandResults commandResults) {
        Map<String, String> replaced = new LinkedHashMap<>();
        if (files == null && commandResults == null) {
            return replaced;
        }
        Map<String, List<CommandParameterDefinition>> parameters = parametersOf(forms);
        for (Map.Entry<String, Object> argument : arguments.entrySet()) {
            if (!(argument.getValue() instanceof String text)) {
                continue;
            }
            boolean file = files != null && text.startsWith(FILE_PREFIX);
            boolean result = commandResults != null && CommandResults.isReference(text);
            if (!file && !result) {
                continue;
            }
            List<CommandParameterDefinition> declared = parameters.get(argument.getKey());
            if (declared == null || !isText(declared)) {
                continue;
            }
            try {
                argument.setValue(file ? files.apply(text.substring(FILE_PREFIX.length()).strip()) : commandResults.resolve(text).text());
                replaced.put(argument.getKey(), text.strip());
            } catch (RuntimeException e) {
                throw new FunctionCallException(Kind.FAILED, "The argument " + argument.getKey() + ": " + messageOf(e), e);
            }
        }
        return replaced;
    }

    /**
     * Puts the images in the place of an argument that names image files of the agent: a path, or a list of paths, becomes
     * the attachments (at most {@link #MAX_IMAGES}). An argument of a parameter that does not take images is left alone.
     *
     * @param images null when the agent has no files, the parameter is then not there and the argument fails
     * @return the paths that were replaced, by the name of the argument
     * @throws FunctionCallException with the reason when a path is not an image of the workspace
     */
    private static Map<String, String> readImages(List<CommandDefinition> forms, Map<String, Object> arguments,
                                                  Function<String, LLMAttachment> images) {
        Map<String, String> replaced = new LinkedHashMap<>();
        Map<String, List<CommandParameterDefinition>> parameters = parametersOf(forms);
        for (Map.Entry<String, Object> argument : arguments.entrySet()) {
            List<CommandParameterDefinition> declared = parameters.get(argument.getKey());
            if (declared == null || !isImages(declared)) {
                continue;
            }
            if (images == null) {
                throw new FunctionCallException(Kind.INVALID_ARGUMENTS, "The parameter " + argument.getKey() + " is not available");
            }
            List<String> paths = new ArrayList<>();
            if (argument.getValue() instanceof Collection<?> list) {
                list.forEach(item -> paths.add(String.valueOf(item).strip()));
            } else if (argument.getValue() instanceof String text) {
                paths.add(text.strip());
            } else {
                throw new FunctionCallException(Kind.INVALID_ARGUMENTS, "The argument " + argument.getKey()
                        + " is a list of paths of image files of your workspace");
            }
            if (paths.size() > MAX_IMAGES) {
                throw new FunctionCallException(Kind.INVALID_ARGUMENTS, "The argument " + argument.getKey() + " has "
                        + paths.size() + " images, the most it takes is " + MAX_IMAGES);
            }
            List<LLMAttachment> attachments = new ArrayList<>();
            for (String path : paths) {
                try {
                    attachments.add(images.apply(path));
                } catch (RuntimeException e) {
                    throw new FunctionCallException(Kind.FAILED, "The argument " + argument.getKey() + ": " + messageOf(e), e);
                }
            }
            argument.setValue(attachments.toArray(new LLMAttachment[0]));
            replaced.put(argument.getKey(), String.join(", ", paths));
        }
        return replaced;
    }

    /**
     * The arguments for the form that fits what the model passed: the one with the fewest parameters that has them all,
     * the other parameters of it null or their default.
     */
    private static Map<String, Object> complete(List<CommandDefinition> forms, Map<String, Object> arguments) {
        List<CommandDefinition> declared = declared(forms);
        if (declared.isEmpty()) {
            return arguments;
        }
        for (String name : requiredOf(forms)) {
            if (!arguments.containsKey(name)) {
                throw new FunctionCallException(Kind.INVALID_ARGUMENTS, "Missing required parameter '" + name + "'");
            }
        }
        CommandDefinition best = null;
        for (CommandDefinition form : declared) {
            Set<String> names = new HashSet<>();
            form.parameters().forEach(p -> names.add(p.name()));
            if (names.containsAll(arguments.keySet()) && (best == null || form.parameters().size() < best.parameters().size())) {
                best = form;
            }
        }
        if (best == null) {
            if (forms.stream().anyMatch(CommandDefinition::anyArguments)) {
                return arguments;
            }
            Set<String> known = parametersOf(forms).keySet();
            Set<String> unknown = new LinkedHashSet<>(arguments.keySet());
            unknown.removeAll(known);
            throw new FunctionCallException(Kind.INVALID_ARGUMENTS, unknown.isEmpty()
                    ? "No form of the command takes the parameters " + arguments.keySet() + " together"
                    : "Unknown parameter(s) " + unknown + ", the parameters are " + known);
        }
        Map<String, Object> complete = new LinkedHashMap<>();
        for (CommandParameterDefinition parameter : best.parameters()) {
            Object value = arguments.get(parameter.name());
            if (value == null && !parameter.defaultValue().isEmpty()) {
                value = parameter.defaultValue();
            }
            if (value != null && !parameter.allowed().isEmpty() && !parameter.allowed().contains(String.valueOf(value))) {
                throw new FunctionCallException(Kind.INVALID_ARGUMENTS, "Parameter '" + parameter.name() + "' must be one of "
                        + parameter.allowed());
            }
            complete.put(parameter.name(), value);
        }
        return complete;
    }

    @SuppressWarnings("unchecked")
    private static void run(CommandService commands, String commandName, Map<String, Object> arguments, Map<String, String> references,
                            CommandResults commandResults, CommandCage cage, FunctionContinuation<Object> continuation) {
        // the agent knows the items by handles, the command by uuids; the user was shown the handles
        CommandRun run = cage == null ? commands.execute(commandName, arguments)
                : commands.execute(commandName, (Map<String, Object>) cage.handles().fromHandles(arguments), false, cage.scope());
        run.whenComplete((value, error) -> {
            if (error != null) {
                continuation.fail(new FunctionCallException(Kind.FAILED, messageOf(error), error));
                return;
            }
            try {
                String text = FunctionDefinition.render(value);
                if (cage != null) {
                    text = cage.handles().toHandles(text);
                }
                if (commandResults != null) {
                    commandResults.add(functionName(commandName), describeArguments(arguments, references), text);
                }
                continuation.complete(bound(text));
            } catch (RuntimeException e) {
                continuation.fail(e);
            }
        });
    }

    /** The arguments of a call as one short line for the list of the results: {@code name=value, ...}, cut. */
    public static String describeArguments(Map<String, Object> arguments) {
        return describeArguments(arguments, Map.of());
    }

    /** As {@link #describeArguments(Map)}, an argument that was a reference shows the reference, not the text it stood for. */
    private static String describeArguments(Map<String, Object> arguments, Map<String, String> references) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, Object> argument : arguments.entrySet()) {
            if (argument.getValue() == null) {
                continue;
            }
            String value = references.getOrDefault(argument.getKey(), String.valueOf(argument.getValue())).replaceAll("\\s+", " ").strip();
            if (value.length() > 40) {
                value = value.substring(0, 40) + "…";
            }
            text.append(text.isEmpty() ? "" : ", ").append(argument.getKey()).append('=').append(value);
        }
        return text.length() <= CommandResults.MAX_ARGUMENTS ? text.toString()
                : text.substring(0, CommandResults.MAX_ARGUMENTS) + "…";
    }

    /** What went wrong, from the cause of the command for the model: no class names, no "Command x failed". */
    public static String messageOf(Throwable error) {
        Throwable cause = error;
        while ((cause instanceof CommandException || cause instanceof java.util.concurrent.CompletionException
                || cause instanceof java.util.concurrent.ExecutionException) && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (cause instanceof java.util.concurrent.CancellationException) {
            return "The command was stopped";
        }
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            return cause.getClass().getSimpleName();
        }
        return message;
    }

    /** The result cut at the end of a line within {@link #MAX_RESULT_LENGTH}, with a note of how much there was. */
    public static String bound(String text) {
        if (text.length() <= MAX_RESULT_LENGTH) {
            return text;
        }
        int cut = text.lastIndexOf('\n', MAX_RESULT_LENGTH);
        if (cut < MAX_RESULT_LENGTH / 2) {
            cut = MAX_RESULT_LENGTH;
        }
        return text.substring(0, cut) + "\n[cut: " + cut + " of " + text.length()
                + " characters; ask for less (a smaller page, a narrower search)]";
    }

}
