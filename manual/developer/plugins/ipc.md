---
label: Sharing data between extensions
order: 64
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/IPCService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/IPCServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/ToolService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/tool
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/ToolServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/OsgiService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/OsgiServiceImpl.java
  - marginalia/src/main/resources/META-INF/spring/services-config.xml
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/ToolSelectionDialog.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/ToolSelectionButton.java
  - marginalia/plugins/arc-outline
  - marginalia/plugins/sidequery
  - marginalia/plugins/lore-generator
  - marginalia/plugins/name-generator/src/main/java/com/github/enerccio/marginalia/extensions/namegenerator/NameGeneratorExtension.java
  - marginalia/plugins/name-generator/src/main/java/com/github/enerccio/marginalia/extensions/namegenerator/NameGeneratorToolProvider.java
  - marginalia/plugins/name-provider/src/main/java/com/github/enerccio/marginalia/extensions/nameprovider/NameProviderExtension.java
  - marginalia/plugins/name-provider/src/main/java/com/github/enerccio/marginalia/extensions/nameprovider/NameProviderToolProvider.java
  - marginalia/plugins/place-provider/src/main/java/com/github/enerccio/marginalia/extensions/placeprovider/PlaceProviderToolProvider.java
  - marginalia/plugins/place-provider/src/main/java/com/github/enerccio/marginalia/extensions/placeprovider/PlaceProviderExtension.java
verified: 1e3193fc
---

# Sharing data between extensions

Extensions are separate bundles that should not import each other's classes (one can be unloaded and installed again
while another keeps running). There are three ways to work together anyway:

| You want | Use |
|---|---|
| to offer **text about a book or a story part** to whoever wants it (and to use what others offer), without knowing who they are | [`IPCService`](#ipcservice-text-for-whoever-asks) |
| to offer **tools for the model** (functions it can call, and the same as commands) that the user chooses in a **Tools** dialog | [`ToolService`](#toolservice-tools-for-the-model) |
| to use **one particular extension** and what it can do (objects, lists), if it is installed | [a direct call](#calling-another-extension-directly) |

## IPCService: text for whoever asks

`IPCService` lets extensions offer data to each other as **text**, through callbacks registered by a key. The
application can use it too.

There are two kinds of providers:

| Kind | Callback | Asked with |
|---|---|---|
| About a book | `ManuscriptPluginData.getDataFor(Manuscript, Bundle caller, String callerIdentifier)` | `getManuscriptData(...)` |
| About a story part | `ChatMessagePluginData.getDataFor(ChatMessage, Bundle caller, String callerIdentifier)` | `getChatMessageData(...)` |

A callback returns the text, or `null` / blank when it has nothing. It may throw: the exception reaches the caller.

### Offering data

Register in `onExtensionLoad`, with `@Autowired IPCService` in your `@Configurable` extension class:

```java
ipcService.registerManuscriptProvider(
        bundle.getSymbolicName(),   // bundleIdentifier: who offers it
        "outline",                  // providerKey: stable, callers remember it
        "Arc outline",              // providerName: what the user sees
        (manuscript, caller, callerIdentifier) -> service.outlineText(manuscript));
```

- Use the **symbolic name of your bundle** as the identifier. When an extension is unloaded the application removes
  all providers of `bundle.getSymbolicName()`. A provider registered under another identifier must be removed with
  `unregisterAll(identifier)` in `onExtensionUnload`.
- Registering a key again replaces the provider and keeps its place in the order.
- The key is part of what other extensions store (Side Query remembers the sources the user checked), so don't rename it.

### Asking for data

```java
Map<String, Map<String, String>> providers = ipcService.getManuscriptProviders();
// bundle identifier -> (provider key -> provider name), both in the order of registration; a copy

String text = ipcService.getManuscriptData("arc-outline", "outline", manuscript,
        bundle /* your own bundle, null for the application */,
        MyExtension.class.getName() /* unique identifier of the caller */);
```

`getChatMessageProviders()` and `getChatMessageData(...)` are the same for story parts. Asking for a provider that
is not there (any more) returns `null` instead of throwing, since extensions come and go at any time; list the
providers again when you show them to the user, and call them on the thread you are on (the callback runs there, so
the session and transaction are yours).

The callback gets the **caller's bundle and identifier**, so a provider can decide what to give to whom. Nothing is
checked by the service itself: a provider gives the data of the book it is handed, so it must only be handed books the
caller may read.

## ToolService: tools for the model

`ToolService` lets an extension offer a **suite of tools** - functions the model can call - to every dialog that runs the
model with tools (the generators of the Lore Generator and of the Arc Outline), and the same suite as commands for the
command bar and the agents. Neither side imports the other and nothing is found by reflection: the types that cross are the
ones of the application (`FunctionSet`, `ToolSelection`).

### Offering a suite

Implement `ToolProvider` and register it in `onExtensionLoad`, with `@Autowired ToolService` in your `@Configurable` extension class:

```java
toolService.registerProvider(
        bundle.getSymbolicName(),   // bundleIdentifier: who offers it
        "names",                    // providerKey: stable, callers save it with the choice of the user
        "Name provider",            // the human name, the title of the section in the dialog
        new NameProviderToolProvider(lists, registry));
```

```java
public interface ToolProvider {
    default List<ToolItem> items() { return List.of(); }                 // what the user can check; none: on or off
    FunctionSet functions(Collection<String> items, ToolRequest request) throws Exception;
    default String defaultHint() { return null; }                        // added to the prompt when the model gets the suite
    default String description() { return null; }                        // shown above the items
    default Object commands() { return null; }                           // @Command suite, the same tools for the command bar and agents
}
```

- **Items** restrict the suite: the Name generator has one for each category of names. `functions` gets the ids that are checked
  (and still offered by `items()`) and returns functions that know only those - the provider does the restricting with its own
  types, usually a small class that implements its `@LLMFunction` interface around the real implementation
  (`NameProviderToolProvider.Restricted`). A suite without items is on or off; `items` is then empty.
  `items()` is asked on every use, it may change while the application runs. An item id is saved by the user's choice: keep
  it stable.
- **`ToolItem(id, label)`**: the id is what is saved, the label what the user sees.
- **`defaultHint`** is a short text for the model, "when and how to call these": the caller adds the hint of each suite the model gets
  to its prompt. The user can write their own in the dialog; theirs replaces the default.
- **`commands()`** is a suite of `@Command` methods ([Commands](../commands/writing-commands.md)). The service registers it
  with `CommandService` together with the provider and removes it with it, so the extension does not call
  `registerCommands` / `unregisterCommands` itself, and the command bar, the agents and the dialogs with tools have one source
  and one human name.
- The provider is identified by the **symbolic name of the bundle** and the key. When an extension is unloaded the application
  removes all providers of that bundle and their commands (`OsgiServiceImpl`); registering a key again replaces the provider and
  keeps its place in the order. A function name must not be used by two suites a user can check together: `resolve` fails
  with the names of both providers.

### Asking for tools

A dialog needs three things, and `ToolSelectionButton` does the first two:

```java
ToolSelectionButton tools = new ToolSelectionButton();           // hides itself while no suite is installed
tools.setValue(settings.getTools());                              // a ToolSelection, null for none
tools.addValueChangeListener(settings::setTools);                 // save it, it is plain JSON (Gson)
...
ToolSet set = toolService.resolve(tools.getValue(), new ToolRequest(bundle, MyService.class.getName()));
inference.stream(payload, protocol, set.functions(), callback);   // null when the user chose nothing
prompt += "\n\n" + set.hint();                                    // the hints of the suites the model got
```

- `ToolSelection` is what the user chose: for each provider (`bundle/key`) the checked item ids, `enabled` for a suite without
  items, and the user's hint. Keep it in your settings (the Lore Generator also keeps it with each lore entry it generated). A provider that is
  not installed (any more) or an item that is gone is ignored by `resolve`; a provider that throws is logged and left out.
- `ToolSet` holds the combined `FunctionSet` (null when nothing is on) and the hints; `ToolSet.NONE` for a dialog that gives
  the model no tools.
- The button opens `ToolSelectionDialog`: one section for each suite with a filter field of its own, see [UI](../ui.md). Call
  `refresh()` when a dialog that stays open may have lost or gained a suite (it also refreshes when it is attached).

### Reading what an older version saved

The Lore Generator and the Arc Outline used to save the name categories with the prefix of their source (`Generator: `,
`Provider: `, none meant the generator) and two hints, and found the name extensions by reflection. Each of them has a small
`LegacyNameTools` that turns those into a `ToolSelection` for the suites `name-generator/names` and `name-provider/names`,
once, when the settings are read (`migrateLegacyNameTools`); what is saved next has only the new form. Do the same when you
change what your settings keep.

## Calling another extension directly

When an extension needs more than text from another one and [`ToolService`](#toolservice-tools-for-the-model) is not what it
wants, it calls the other extension's own methods. (The Lore Generator and the Arc Outline did this for the name extensions
before `ToolService` existed; the technique is the same for anything else.)

### Do not import the other extension

The obvious way - depend on the other plugin in the `pom.xml` and import its packages - is the wrong one. A bundle that
imports the packages of another bundle is tied to the classes of that bundle: when the other extension is unloaded
(Admin → Extensions, an update, a restart of the framework) and loaded again, the importing bundle keeps the old
classes, and an extension installed *after* yours is not seen at all, its classes are other ones. So the bundle does
not import them (`pom.xml` has no dependency on the other plugin), and nothing of it appears in the `Import-Package`
of the manifest. The other extension is **found at run time and used by reflection**.

### Finding and calling it

`OsgiService.getLoadedExtensions()` lists the extensions that finished loading (`LoadedExtension(bundle, extension)`),
so the other extension is found by the name of its class:

```java
private static MarginaliaExtension find(OsgiService osgiService) {
    for (OsgiService.LoadedExtension loaded : osgiService.getLoadedExtensions()) {
        if (loaded.extension().getClass().getName()
                .equals("com.github.enerccio.marginalia.extensions.namegenerator.NameGeneratorExtension")) {
            return loaded.extension();
        }
    }
    return null;                                   // not installed, stopped or not loaded yet
}

@SuppressWarnings("unchecked")
public static List<String> categories(OsgiService osgiService) {
    MarginaliaExtension extension = find(osgiService);
    if (extension == null) {
        return null;
    }
    try {
        return (List<String>) extension.getClass().getMethod("categories").invoke(extension);
    } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("The name generator has no categories()", e);
    }
}
```

The rules the bundled code follows:

- **Only types both sides know cross the border**: the JDK and the application (`List<String>`, `FunctionSet`,
  `Manuscript`...). Your bundle can't name a class of the other one. When you need the other extension's own interface,
  load it with *its* class loader (`extension.getClass().getClassLoader().loadClass(name)`) and, if you must implement it,
  wrap the real object in a `java.lang.reflect.Proxy` of that interface.
- **Look it up on every call.** Keep no reference to the extension, its classes or its `Method` objects: they would
  keep an unloaded extension alive. A missing extension is a normal answer (`null`), not an error: a dialog asks when it opens and
  hides the feature when there is none. A screen that stays open can follow
  the loading and unloading with `OsgiService.addExtensionListener` (the listener is held weakly, keep a reference to it).
- **One class touches the other extension** (with the class names and the method names as constants), so that a change on
  the other side breaks one place and the reflection is not spread over the code. Report a failed call with an exception
  that says what is missing; the caller decides what to show.
- **Selections and settings are yours**: keep what the user chose in your own settings and pass it each time; leave out
  what the other extension no longer has.

### Offering something to be called

An extension that wants to be called gives its `MarginaliaExtension` class a few `public` methods that use only JDK and
application types. There is no compiler between the two, so treat the class name, the method names and their signatures as a
contract: keep them stable, say so in the README of the plugin, and when you must change them, change the callers in the same
commit. The offering extension does not need to know who calls it. (The Name generator and the Name provider had `categories()`,
`newFunctions()` and `functions()` for the bridges; they are gone, their suites are offered through `ToolService`.)

### Which one?

| | `IPCService` | `ToolService` | Direct call |
|---|---|---|---|
| Caller knows | the kind of data (about a book / a story part) | that it wants tools for the model | the other extension and its methods |
| Several providers | yes, listed in the order they registered | yes, in sections of one dialog | one |
| Types | text | `FunctionSet`, `ToolSelection` | anything the JDK and the application have |
| The user chooses | what to include (the *Side Query* popover) | suites and their items (the **Tools** dialog) | usually nothing, the feature simply appears |
| When it is missing | its entry is not in the list | it is not in the dialog, ignored by `resolve` | the lookup returns `null` |

## In the bundled extensions

- **Arc Outline** offers its outline as two sources about a book, so the user chooses how much the caller gets:
  `arc-outline` / `outline` (the arcs with their states, descriptions and chapters) and `arc-outline` /
  `outline-with-notes` (the same with the note of every arc and chapter).
- **Side Query** lists all providers: the button with the plug icon above a tab opens a popover with a checkbox for each.
  Checked book sources are added to the system message after the lorebook; checked story part sources are asked for
  each story part in the *Chat Logs* range and put after the text of the part. The checked sources are stored in the
  options of the tab, a source that is gone is skipped until it comes back.
