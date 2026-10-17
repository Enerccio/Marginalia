package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.*;
import com.github.enerccio.marginalia.domain.service.impl.commands.BackupCommands;
import com.github.enerccio.marginalia.domain.service.impl.commands.HelloWorldCommands;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class CommandServiceImpl implements CommandService, InitializingBean, DisposableBean {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_.-]+");

    /** Cost of a call that fits a command that takes any arguments: it loses to every declared signature that fits. */
    private static final int ANY_ARGUMENTS_COST = 1000;

    private record Registered(CommandDefinition definition, Method method) {

    }

    /** A call matched to an overload, with the arguments converted. */
    private record Binding(Registered registered, Map<String, Object> arguments, List<Object> positional, int cost) {

    }

    // name -> the overloads of the command in the order they are tried; all of them belong to one suite
    private final Map<String, List<Registered>> commands = new LinkedHashMap<>();

    private final AtomicInteger threadCounter = new AtomicInteger();
    private final ExecutorService workers = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "command-" + threadCounter.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public void afterPropertiesSet() {
        registerCommands(new HelloWorldCommands());
        registerCommands(new BackupCommands());
    }

    @Override
    public List<CommandDefinition> registerCommands(Object suite) {
        if (suite == null) {
            throw new IllegalArgumentException("No command suite to register");
        }
        Class<?> type = suite.getClass();
        Map<String, List<Registered>> found = new TreeMap<>();
        for (Method method : commandMethods(type)) {
            Registered registered = new Registered(definitionOf(method, suite), method);
            found.computeIfAbsent(registered.definition().commandName(), _ -> new ArrayList<>()).add(registered);
        }
        if (found.isEmpty()) {
            throw new IllegalArgumentException(type.getName() + " has no method annotated with @Command");
        }
        for (Map.Entry<String, List<Registered>> entry : found.entrySet()) {
            validateOverloads(entry.getKey(), entry.getValue(), type);
            entry.getValue().sort(OVERLOAD_ORDER);
        }

        synchronized (commands) {
            for (String name : found.keySet()) {
                List<Registered> existing = commands.get(name);
                if (existing != null && existing.getFirst().definition().instance() != suite) {
                    throw new IllegalArgumentException("Command " + name + " is registered already by "
                            + existing.getFirst().definition().instance().getClass().getName());
                }
            }
            found.forEach(commands::put);
        }
        return found.values().stream().flatMap(List::stream).map(Registered::definition).toList();
    }

    private static final Comparator<Registered> OVERLOAD_ORDER = Comparator
            .comparing((Registered r) -> r.definition().anyArguments())
            .thenComparingInt(r -> r.definition().parameters().size())
            .thenComparing(r -> r.method().getName())
            .thenComparing(r -> r.definition().parameters().stream().map(p -> p.type().getName()).toList().toString());

    private static CommandDefinition definitionOf(Method method, Object suite) {
        Command meta = method.getAnnotation(Command.class);
        String name = meta.commandName();
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid command name '" + name + "' of " + describe(method)
                    + ", use letters, digits, '_', '-' and '.'");
        }
        if (method.getReturnType() != void.class || method.getParameterCount() != 1
                || method.getParameterTypes()[0] != CommandContext.class) {
            throw new IllegalArgumentException(describe(method) + " must be void name(CommandContext)");
        }

        CommandParameterSignature signature = meta.signature();
        if (signature.any() && signature.value().length > 0) {
            throw new IllegalArgumentException(describe(method) + " declares parameters and also takes any arguments");
        }
        List<CommandParameterDefinition> parameters = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (CommandParameter parameter : signature.value()) {
            if (parameter.name().isBlank() || !names.add(parameter.name())) {
                throw new IllegalArgumentException(describe(method) + " has a parameter without a name or a name used twice: '"
                        + parameter.name() + "'");
            }
            parameters.add(new CommandParameterDefinition(parameter.name(), parameter.type(), parameter.description()));
        }
        CommandReturn returns = meta.returns();
        try {
            // a public method of a class that is not public (a nested class of a plugin) is not callable otherwise
            method.setAccessible(true);
        } catch (RuntimeException ignored) {
            // inaccessible module, the call fails with a clear message
        }
        return new CommandDefinition(name, meta.humanName(), meta.description(), meta.llmDescription(), meta.async(),
                suite, signature.any(), List.copyOf(parameters),
                new CommandReturnDefinition(returns.name(), returns.type(), returns.description()));
    }

    private static void validateOverloads(String name, List<Registered> overloads, Class<?> type) {
        Set<List<Object>> signatures = new HashSet<>();
        for (Registered registered : overloads) {
            CommandDefinition definition = registered.definition();
            if (!definition.humanName().equals(overloads.getFirst().definition().humanName())) {
                // the human name is the one of the command, not of a form of it
                throw new IllegalArgumentException("The overloads of command " + name + " in " + type.getName()
                        + " have different human names");
            }
            List<Object> key = definition.anyArguments() ? List.of("any")
                    : List.copyOf(definition.parameters().stream().map(CommandParameterDefinition::type).toList());
            if (!signatures.add(key)) {
                throw new IllegalArgumentException("Command " + name + " is declared twice with the same signature in "
                        + type.getName());
            }
        }
    }

    /** Annotated methods of the class and its superclasses; they must be public, or they would be silently skipped. */
    private static List<Method> commandMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (method.isSynthetic() || method.isBridge() || !method.isAnnotationPresent(Command.class)) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException(describe(method) + " must be public");
                }
                methods.add(method);
            }
        }
        return methods;
    }

    private static String describe(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    @Override
    public void unregisterCommands(Object suite) {
        synchronized (commands) {
            commands.values().removeIf(overloads -> overloads.getFirst().definition().instance() == suite);
        }
    }

    @Override
    public boolean unregisterCommand(String commandName) {
        synchronized (commands) {
            return commands.remove(commandName) != null;
        }
    }

    @Override
    public List<CommandDefinition> getCommands() {
        synchronized (commands) {
            return commands.values().stream().flatMap(List::stream).map(Registered::definition).toList();
        }
    }

    @Override
    public List<CommandDefinition> getCommands(String commandName) {
        return overloadsOf(commandName).stream().map(Registered::definition).toList();
    }

    private List<Registered> overloadsOf(String commandName) {
        synchronized (commands) {
            List<Registered> overloads = commands.get(commandName);
            return overloads == null ? List.of() : List.copyOf(overloads);
        }
    }

    @Override
    public CommandRun execute(String commandName, Map<String, Object> arguments) {
        List<Registered> overloads = overloadsOf(commandName);
        if (overloads.isEmpty()) {
            return noSuchCommand(commandName);
        }
        Map<String, Object> named = arguments == null ? Map.of() : arguments;
        Binding best = null;
        for (Registered registered : overloads) {
            Binding binding = bindNamed(registered, named);
            if (binding != null && (best == null || binding.cost() < best.cost())) {
                best = binding;
            }
        }
        if (best == null) {
            return failedRun(new IllegalArgumentException(
                    "No form of command " + commandName + " takes the arguments " + named.keySet()));
        }
        return run(commandName, best);
    }

    @Override
    public CommandRun executePositional(String commandName, List<Object> arguments) {
        List<Registered> overloads = overloadsOf(commandName);
        if (overloads.isEmpty()) {
            return noSuchCommand(commandName);
        }
        List<Object> positional = arguments == null ? List.of() : arguments;
        Binding best = null;
        for (Registered registered : overloads) {
            Binding binding = bindPositional(registered, positional);
            if (binding != null && (best == null || binding.cost() < best.cost())) {
                best = binding;
            }
        }
        if (best == null) {
            return failedRun(new IllegalArgumentException(
                    "No form of command " + commandName + " takes " + positional.size() + " such argument(s)"));
        }
        return run(commandName, best);
    }

    @Override
    public CommandRun executeLine(String line) {
        CommandLine parsed = CommandLine.parse(line);
        if (!parsed.command()) {
            return failedRun(new IllegalArgumentException("A command starts with a slash: " + line));
        }
        return executePositional(parsed.name(), new ArrayList<>(parsed.arguments()));
    }

    private static CommandRun failedRun(Throwable error) {
        CommandRunImpl run = new CommandRunImpl();
        run.completeExceptionally(error);
        return run;
    }

    private static CommandRun noSuchCommand(String commandName) {
        return failedRun(new IllegalArgumentException("No command " + commandName));
    }

    private static Binding bindNamed(Registered registered, Map<String, Object> named) {
        CommandDefinition definition = registered.definition();
        if (definition.anyArguments()) {
            return new Binding(registered, named, List.of(), ANY_ARGUMENTS_COST);
        }
        Set<String> declared = new HashSet<>();
        definition.parameters().forEach(p -> declared.add(p.name()));
        if (!declared.equals(named.keySet())) {
            return null;
        }
        Map<String, Object> converted = new LinkedHashMap<>();
        List<Object> positional = new ArrayList<>();
        int cost = 0;
        for (CommandParameterDefinition parameter : definition.parameters()) {
            CommandValues.Converted value = CommandValues.convert(named.get(parameter.name()), parameter.type());
            if (value == null) {
                return null;
            }
            converted.put(parameter.name(), value.value());
            positional.add(value.value());
            cost += value.cost();
        }
        return new Binding(registered, converted, positional, cost);
    }

    private static Binding bindPositional(Registered registered, List<Object> arguments) {
        CommandDefinition definition = registered.definition();
        if (definition.anyArguments()) {
            return new Binding(registered, Map.of(), arguments, ANY_ARGUMENTS_COST);
        }
        if (definition.parameters().size() != arguments.size()) {
            return null;
        }
        Map<String, Object> converted = new LinkedHashMap<>();
        List<Object> positional = new ArrayList<>();
        int cost = 0;
        for (int i = 0; i < arguments.size(); i++) {
            CommandParameterDefinition parameter = definition.parameters().get(i);
            CommandValues.Converted value = CommandValues.convert(arguments.get(i), parameter.type());
            if (value == null) {
                return null;
            }
            converted.put(parameter.name(), value.value());
            positional.add(value.value());
            cost += value.cost();
        }
        return new Binding(registered, converted, positional, cost);
    }

    private CommandRun run(String commandName, Binding binding) {
        Registered registered = binding.registered();
        CommandRunImpl result = new CommandRunImpl();
        CommandContextImpl context = new CommandContextImpl(commandName, binding.arguments(), binding.positional(),
                registered.definition().async(), result);
        if (!registered.definition().async()) {
            invoke(registered, context);
            context.finish();
            return result;
        }

        ThreadCopyRequestAttributes attributes = ThreadCopyRequestAttributes.create();
        try {
            Future<?> worker = workers.submit(() -> {
                if (result.isDone()) {
                    // stopped before it started
                    return;
                }
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    invoke(registered, context);
                }
            });
            result.whenComplete((_, _) -> {
                if (result.isCancelled()) {
                    worker.cancel(true);
                }
            });
        } catch (RuntimeException e) {
            context.fail(e);
        }
        return result;
    }

    private static void invoke(Registered registered, CommandContextImpl context) {
        try {
            registered.method().invoke(registered.definition().instance(), context);
        } catch (InvocationTargetException e) {
            context.fail(e.getCause() == null ? e : e.getCause());
        } catch (Throwable t) {
            context.fail(t);
        }
    }

    @Override
    public void destroy() {
        workers.shutdownNow();
    }

}
