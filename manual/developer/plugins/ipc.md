---
label: Sharing data between extensions
order: 64
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/IPCService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/IPCServiceImpl.java
---

# Sharing data between extensions

Extensions are separate bundles that should not import each other's classes (one can be unloaded and installed again
while another keeps running). `IPCService` lets them offer data to each other anyway, as **text**, through callbacks
registered by a key. The application can use it too.

There are two kinds of providers:

| Kind | Callback | Asked with |
|---|---|---|
| About a book | `ManuscriptPluginData.getDataFor(Manuscript, Bundle caller, String callerIdentifier)` | `getManuscriptData(...)` |
| About a story part | `ChatMessagePluginData.getDataFor(ChatMessage, Bundle caller, String callerIdentifier)` | `getChatMessageData(...)` |

A callback returns the text, or `null` / blank when it has nothing. It may throw: the exception reaches the caller.

## Offering data

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
- The key is part of what other extensions store (a caller that lets the user choose sources remembers the key), so don't rename it.

## Asking for data

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
