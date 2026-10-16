---
label: Sharing data between extensions
order: 64
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/IPCService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/IPCServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/OsgiService.java
  - marginalia/plugins/arc-outline
  - marginalia/plugins/sidequery
  - marginalia/plugins/lore-generator/src/main/java/com/github/enerccio/marginalia/extensions/loregenerator/names
  - marginalia/plugins/name-generator/src/main/java/com/github/enerccio/marginalia/extensions/namegenerator/NameGeneratorExtension.java
  - marginalia/plugins/name-provider/src/main/java/com/github/enerccio/marginalia/extensions/nameprovider/NameProviderExtension.java
verified: 0b626835
---

# Sharing data between extensions

Extensions are separate bundles that should not import each other's classes (one can be unloaded and installed again
while another keeps running). There are two ways to work together anyway:

| You want | Use |
|---|---|
| to offer **text about a book or a story part** to whoever wants it (and to use what others offer), without knowing who they are | [`IPCService`](#ipcservice-text-for-whoever-asks) |
| to use **one particular extension** and what it can do (functions the model can call, objects, lists), if it is installed | [a direct call](#calling-another-extension-directly) |

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

## Calling another extension directly

When an extension needs more than text from another one, it calls the other extension's own methods. The
**Lore Generator** and the **Arc Outline** do this to get names from the **Name Generator** and the **Name Provider**: they
let the model call their functions (`list_name_categories`, `generate_names` and `list_real_name_lists`,
`pick_real_names`), restricted to the categories the user checked.

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
  load it with *its* class loader (`extension.getClass().getClassLoader().loadClass(name)`). The Lore Generator does that
  for `NameGeneratorFunctions` and `NameProviderFunctions`: it wraps the real implementation in a `java.lang.reflect.Proxy`
  of that interface that only lets the checked categories through, and hands the proxy to
  `FunctionRegistry.bind(type, proxy)` to get the `FunctionSet` for the model. The sets of several extensions are put
  together with `FunctionSet.and` (the names of the functions must differ, which is why the provider's are
  `list_real_name_lists` and `pick_real_names`), so a request has the functions of both, of one or of none.
- **Look it up on every call.** Keep no reference to the extension, its classes or its `Method` objects: they would
  keep an unloaded extension alive. A missing extension is a normal answer (`null`), not an error: the dialogs of the
  generators ask when they open and hide the *Name tools* button when there is none. A screen that stays open can follow
  the loading and unloading with `OsgiService.addExtensionListener` (the listener is held weakly, keep a reference to it).
- **One class touches the other extensions** (`NameToolsBridge`, with the class names and the method names as
  constants), so that a change on the other side breaks one place and the reflection is not spread over the code.
  Report a failed call with an exception that says what is missing; the caller decides what to show.
- **Selections and settings are yours**: the Lore Generator keeps the category names the user checked in its own
  settings and passes them to the bridge each time; the bridge leaves out a category that the other extension no longer
  has. When one bridge serves several extensions, prefix what is saved with its source (`Generator: `, `Provider: `)
  so that equal titles can not be confused, and read what was saved before the prefix existed as the first source
  (`NameToolsBridge.normalize`). The caller finds out which functions it got with `FunctionSet.contains` (to add the hint
  about them to the prompt), no flags have to be carried around.

### Offering something to be called

An extension that wants to be called gives its `MarginaliaExtension` class a few `public` methods that use only JDK and
application types (the Name Generator and the Name Provider have `categories()`, `newFunctions()` and `functions()`). There is no compiler
between the two, so treat the class name, the method names and their signatures as a contract: keep them stable, say so
in the README of the plugin, and when you must change them, change the callers (here: the two bridges) in the same commit.
The offering extension does not need to know who calls it.

### Which one?

| | `IPCService` | Direct call |
|---|---|---|
| Caller knows | the kind of data (about a book / a story part) | the other extension and its methods |
| Several providers | yes, listed in the order they registered | one |
| Types | text | anything the JDK and the application have |
| The user chooses | what to include (the *Side Query* popover) | usually nothing, the feature simply appears (the *Name tools* button) |
| When it is missing | its entry is not in the list | the lookup returns `null` |

The two bridges in the repository are `marginalia/plugins/lore-generator/.../names/NameToolsBridge.java` and the copy
in `marginalia/plugins/arc-outline/.../names/NameToolsBridge.java` (plugins are separate bundles, so the copy is not
shared).

## In the bundled extensions

- **Arc Outline** offers its outline as two sources about a book, so the user chooses how much the caller gets:
  `arc-outline` / `outline` (the arcs with their states, descriptions and chapters) and `arc-outline` /
  `outline-with-notes` (the same with the note of every arc and chapter).
- **Side Query** lists all providers: the button with the plug icon above a tab opens a popover with a checkbox for each.
  Checked book sources are added to the system message after the lorebook; checked story part sources are asked for
  each story part in the *Chat Logs* range and put after the text of the part. The checked sources are stored in the
  options of the tab, a source that is gone is skipped until it comes back.
