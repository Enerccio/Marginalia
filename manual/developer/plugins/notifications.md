---
label: Desktop notifications
order: 70
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/NotificationService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/NotificationServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/model/impl/settings/NotificationSettings.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/DesktopNotifier.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/notifications/NotificationSettingsEditor.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/JavascriptLibrary.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/OsgiServiceImpl.java
verified: d163441bd
---

# Desktop notifications

A user who starts a long run of a model can ask the browser to tell them when it is over (*Settings → Notifications*, see the
[user manual](../../user/account.md#notifications)). A plugin that runs a model (a review, a generator, a translation) takes part in two
steps: it **registers a source**, so the run is in the user's list, and it **reports the end of a run**.

## Registering a source

```java
@Autowired
private NotificationService notificationService;

// onExtensionLoad
notificationService.register(bundle.getSymbolicName(),
        new NotificationService.Source("reviewer.review", () -> T.localized(REVIEW_THE_BOOK), false));
```

| Argument | |
|---|---|
| `owner` | The symbolic name of the bundle. The application removes everything a bundle registered when it is unloaded (`OsgiServiceImpl`), you need not unregister; `unregister(id)` is there for a source that goes away while the plugin stays. |
| `id` | Stable and unique. The convention is the key of the plugin (the `KEY` constant of its data, `com.github.enerccio.marginalia.extensions.reviewer`) plus the run: `KEY + ".review"`; keep it in a `public static final String NOTIFY_...` of the extension class. It is what the settings of the users store, so do not change it between versions. |
| `name` | A `Supplier` asked every time the list is shown, so it follows the language of the session: `() -> T.localized(NAME)` (see [Plugin localization](ui-extensions.md)), never a text looked up once. A supplier that throws is replaced by the id. |
| `hasAutomatic` | `true` when the run also starts by itself, without the user opening a dialog. The list then has two entries, *Name* and *Name (automatic)*, that the user chooses apart. |

What a user chose stays in their settings when the plugin is not loaded and applies again when it is back; saving the tab does not drop it.

## Reporting the end of a run

```java
new DesktopNotifier(ui).finished("reviewer.review", false, book.getName());   // ended well
new DesktopNotifier(ui).failed("reviewer.review", false, book.getName());     // ended with an error
```

- Call it where the UI is changed (inside `ui.access`): `DesktopNotifier` is `@Configurable` and reads the settings of the current user.
- Do not call it for a run the user stopped.
- `automatic` says the run started by itself. Only the entry of that kind decides: a user who checked *Name* is not told about the automatic
  run, a user who checked *Name (automatic)* is not told about the one they opened. A source registered with `hasAutomatic = false` ignores the flag.
- `detail` is a second line (the title of the book), `null` for none.
- It never throws. Nothing is shown when notifications are off, the entry is not chosen, the source is not registered, the browser does not
  allow them, or the user is looking at the page.

## How it works

`NotificationService.prepare(source, automatic, outcome, detail)` returns the `Notice` (title, body, tag) or `null`; the body is *"&lt;name&gt; finished"* /
*"&lt;name&gt; failed"* in the user's language. The tag is the key of the entry, so two notices of the same entry replace each other in the browser.
`DesktopNotifier` sends the notice with `JavascriptLibrary.showDesktopNotification`, which shows it only when the browser's
`Notification.permission` is `granted`, the page is hidden or has no focus, and the page is a secure context (HTTPS or `localhost`); on
browsers where `new Notification` is refused (phones) it goes through the service worker.

The choice is stored in `NotificationSettings`, a `Setting` row per user (`@DiscriminatorValue`, listed in `persistence.xml`, no migration) with the
extended attributes `enabled` and `chosen` (the keys of the entries: the source id, and `id.auto` for the automatic run).

The permission belongs to the browser and is not stored. The check box of the tab is the switch of the user (`NotificationSettings.enabled`, what
`prepare` checks) and only enables the buttons; the button **Register this browser** is watched by `JavascriptLibrary.watchNotificationPermission`, which
asks the browser from the click itself (browsers refuse the request otherwise) and sends the answer back as the event `marginalia-notification-permission`;
`NotificationSettingsEditor` shows the state and says why when the click did not get the permission. A browser that was not registered shows nothing, whatever
the user chose.

## The sources of the application

| Id | Run | Automatic run |
|---|---|---|
| `generation` | The next part of the story (`StoryGenerationListener`, desktop and phone) | |
| `annotate` | `AnnotateDialog` | the annotation of `AutoAnnotate` |
| `redraft` | `RedraftDialog` | the redraft that follows the automatic annotation |
| `impersonate` | `ImpersonateDialog` (Draft Instructions) | |
| `summary` | `SummaryDialog` | |
| `resummarize` | `ResummarizeDialog` | |
| `help-assistant` | `HelpAssistantWidget` | |

## The sources of the plugins

The ids are the key of the plugin plus the suffix. The name in the list starts with the plugin (*Reviewer: generate a review*).

| Plugin | Suffix | Run | Automatic run |
|---|---|---|---|
| Arc Outline | `.arc`, `.chapters`, `.import` | the three generators (`GeneratorDialog`, which asks its subclass for `notificationSource()`) | |
| Arc Outline | `.wizard` | an answer of the plot wizard | |
| Beta Readers | `.chat` | a reply in a chat (`ChatPanel`) | |
| Beta Readers | `.persona` | *Write with the model* in the persona dialog | |
| Book Description Generator | `.generate` | the description (the key is the package of the plugin, its settings key ends in `.settings`) | |
| Image Generator | `.prompt`, `.image` | the model writes the prompt; the backend makes the image | |
| Knowledge Tracker | `.extract` | a run the user starts in the tab (told when it waits for the review, is done or failed) | the runs the extension starts by itself while a part is generated are not told |
| Lore Generator | `.generate`, `.compact` | the generation and the compaction of the dialog | |
| Manga Generator | `.script`, `.tags` | one section | the continuous mode, told when it ran out of work or a step failed |
| Reviewer | `.review` | a review of a part | |
| Side Query | `.query` | an answer of the tab | |
| SillyTavern Converter | `.convert` | one request, or the whole automatic conversion | |

The two translators (Conlang and Real language) run from commands and the command bar, which have no source.
