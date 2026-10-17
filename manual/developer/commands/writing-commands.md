---
label: Writing commands
order: 90
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/traits
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/commands
  - marginalia/src/main/java/com/github/enerccio/marginalia/utils/CommandUtils.java
  - marginalia/src/test/java/com/github/enerccio/marginalia/command
---

# Writing commands

This page shows how to add commands, in the application and in a plugin. Read [Commands](index.md) first for the model.

## A suite

A suite is a plain class (no interface, no base class) with public methods annotated with `@Command`:

```java
public class DiceCommands {

    @Command(commandName = "roll", humanName = "Roll a die",
            description = "Rolls a die with the given number of sides (6 when omitted).",
            llmDescription = "Returns a random integer from 1 to the number of sides. No side effects.",
            signature = @CommandParameterSignature(
                    @CommandParameter(name = "sides", type = Integer.class, description = "Number of sides")),
            returns = @CommandReturn(name = "result", type = Integer.class, description = "The number rolled"))
    public void roll(CommandContext context) {
        int sides = context.get("sides", Integer.class);
        context.setReturn(ThreadLocalRandom.current().nextInt(sides) + 1);
    }

    @Command(commandName = "roll", humanName = "Roll a die",
            description = "Rolls a die with the given number of sides (6 when omitted).",
            llmDescription = "Returns a random integer from 1 to the number of sides. No side effects.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = Integer.class, description = "The number rolled"))
    public void rollSix(CommandContext context) {
        context.setReturn(ThreadLocalRandom.current().nextInt(6) + 1);
    }
}
```

`/roll` and `/roll 20` now work. The two methods are two forms of the command `roll`: the same name and human name, a
different signature. Typing `/roll 20` is text; because the parameter is `Integer.class` the text is converted, and
`/roll many` fails with a notification instead of calling the method.

Rules to remember:

- The method is `public void` and takes only the `CommandContext`. Anything else makes `registerCommands` throw.
- Fill in `description` (for the user, shown in the list and the help) and `llmDescription` (for an agent: what it does,
  when to use it, whether it has side effects).
- Declare `returns`. The help shows it. `void.class` is the default.
- No signature means **any arguments**: the objects are in `context.getPositional()`. Use it for commands like echo, or when
  the arguments are truly free-form. A declared signature that fits always wins over the any-form.
- An argument of type `Object.class` is "any" and also accepts a list of objects, passed as one argument.
- The suite is **stateless**. Do not keep the book, the user or the result in a field. See [Statelessness](index.md#statelessness).

## Reading the current state

```java
@Command(commandName = "book-title", humanName = "Title of the open book", returns = @CommandReturn(name = "title", type = String.class))
public void title(CommandContext context) {
    Manuscript book = CommandUtils.getCurrentManuscript();
    if (book == null) {
        context.fail(new IllegalStateException("No book is open"));
        return;
    }
    context.setReturn(book.getName());
}
```

`CommandUtils` returns `null` when nothing is open (no workspace yet, no book). The same code works in the desktop and in
the phone layout, because `getManuscriptWindow()` returns the `ManuscriptWindow` interface. Use the services for data;
they check ownership of the current user.

## Failing

A synchronous command can throw; the caller gets a `CommandException` with the exception as its cause. `fail` does the same
and is the way for code on another thread. A message intended for the user goes into the exception message – the command
bar shows it in a notification.

## Asynchronous commands

Set `async = true` when the command takes time or waits for something. Such a command:

- runs on a worker thread, never blocks the UI,
- may answer later, from any thread, with `setReturn` (or `fail`),
- **must check `context.isTerminated()`** in its loops and return at once when it is true, because the user can press
  **Stop** in the progress window.

```java
@Command(commandName = "slow", humanName = "Slow job", async = true)
public void slow(CommandContext context) {
    for (int i = 0; i < 100; i++) {
        if (context.isTerminated() || Thread.currentThread().isInterrupted()) {
            return;
        }
        doOneStep(i);
    }
    context.setReturn("done");
}
```

Tell the user what is going on with `context.setStatus("Taking a backup of Dune")` (the main line) and
`context.setSubStatus("Chapter 3 of 12")` (the detail under it); they appear in the progress window. Set them as the work
advances, `null` clears a line.

Blocking calls (`Thread.sleep`, a network call) are interrupted when the run is cancelled; let the `InterruptedException`
end the command or set the interrupt flag again and return. A command that is stopped should not call `setReturn`; if it
does, the call is ignored.

## Registering

### The application

Add the suite in `CommandServiceImpl.afterPropertiesSet`:

```java
registerCommands(new HelloWorldCommands());
```

The application's suites are plain objects, not Spring beans, and are created with `new`; one that needs services is
`@Configurable` and gets them `@Autowired`, like the UI classes (`BackupCommands`). Keep the logic in services and
make the command a thin adapter that reads the current state through `CommandUtils`.

### A plugin

The plugin gets the `CommandService` injected into its (`@Configurable`) extension and registers the suite in
`onExtensionLoad`:

```java
@Configurable
public class DiceExtension implements MarginaliaExtension {

    @Autowired
    private CommandService commandService;

    private final DiceCommands commands = new DiceCommands();

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        commandService.registerCommands(commands);
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        commandService.unregisterCommands(commands);
    }
}
```

Keep the suite instance in a field: `unregisterCommands` takes the registered object. Unregistering in
`onExtensionUnload` is required – the service does not track bundles, and a command left behind would keep the class
loader of the unloaded bundle alive. `registerCommands` throws when a name is taken, so give plugin commands a prefix
(`dice-roll`) unless the name is meant to be shared.

## Running a command from code

```java
CommandRun result = commandService.executeLine("/roll 20");   // a CompletableFuture<Object>; also getStatus()
Object value = result.get();                         // blocks; fine for a synchronous command
commandService.execute("roll", Map.of("sides", 20)); // by name
commandService.executePositional("roll", List.of(20));
```

Do not block the UI thread on an `async` command: use `whenComplete` and `ui.access(...)`. To stop it, cancel the future.

## Testing

Commands are plain classes, so they are tested without the application: create a `CommandServiceImpl`, register the
suite and run it (see `CommandServiceTest` and `CommandDispatchTest` in `src/test/.../command`).

```java
CommandServiceImpl commands = new CommandServiceImpl();
commands.registerCommands(new DiceCommands());
assertThat(commands.executeLine("/roll 1").get()).isEqualTo(1);
commands.destroy();   // stops the worker pool
```

Test the dispatch (every form), the failure of a bad argument, and for an `async` command that cancelling the future ends
the work: assert `isTerminated()` becomes true and the worker returns. `CommandUtils` returns `null` outside a session, so
a test of a command that reads the workspace needs the request attributes (see `CommandUtilsTest`).

## Checklist

- [ ] Each form has `description`, `llmDescription`, `signature`, `returns`.
- [ ] The suite has no state of a run or a user in fields.
- [ ] `async` commands look at `isTerminated()`.
- [ ] A plugin unregisters its suites in `onExtensionUnload`.
- [ ] The user guide or the plugin's documentation lists the command and its arguments.
- [ ] `CHANGELOG.MD` mentions new commands.
