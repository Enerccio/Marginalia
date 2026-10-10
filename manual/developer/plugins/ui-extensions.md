---
label: Extending the UI
order: 70
verified: 7e15794
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui
  - marginalia/src/main/java/com/github/enerccio/marginalia/instruct
  - marginalia/plugins
---

# Extending the UI

Plugins add to the user interface from decorators: when a method that builds part of a screen runs, the plugin's
decorator finds the component it built and adds to it. This page collects the patterns the bundled plugins use. Read
[@Extendable hooks](extendable.md) first, and [User interface](../ui.md) for the conventions of the UI code.

## The basic pattern

```java
@Override
public void onMethodLeave(Object instrumented, ExtendableMethodContext context, Throwable throwing) throws Exception {
    if (throwing != null) {
        return;                                                   // the method failed, leave it alone
    }
    ContextMenu menu = context.getReflectiveFieldValue(instrumented, "hamburgerMenu", ContextMenu.class);
    if (menu == null || ComponentUtil.getData(menu, MY_ITEM_KEY) != null) {
        return;                                                   // nothing to extend, or already extended
    }
    MenuItem item = menu.addItem("My action", event -> myAction(...));
    ComponentUtil.setData(menu, MY_ITEM_KEY, item);               // mark it, keep a handle for unloading
    trackedMenus.add(menu);                                       // remember it for onExtensionUnload
}
```

1. **Find the component** - a field, a local variable or an argument of the decorated method.
2. **Add only once.** Many methods run more than once for the same component (`refresh()`, `UserPart.refresh`...),
   so mark the component with `ComponentUtil.setData(component, key, value)` and check the mark first. Prefix the
   keys with your plugin's package (`KEY + ".menuItem"`, see [Extended attributes](extended-attributes.md)) - the
   components are shared with the application and every other plugin.
3. **Remember what you added**, so that `onExtensionUnload` can remove it ([Unloading](#unloading)).

## Recipes

### Menu items on a story part

Decorate `ManuscriptStoryPart$ChatMessageCard.createMenuItems` (called by the card's constructor) and add to the
`hamburgerMenu` field. The card's `message` field is the part. Reviewer adds a sub-menu:

```java
MenuItem reviewMenuItem = hamburgerMenu.addItem("Review");
reviewMenuItem.addComponentAsFirst(VaadinIcon.STAR.create());
reviewMenuItem.getSubMenu().addItem("View / Generate Review", e -> openReviewDialog(message));
```

To update items each time the menu opens, decorate `refreshSummaryMenuItems`, which the card runs from the menu's
opened listener.

The card replaces its `message` with the saved copy whenever the part is edited, so read the field again in the click
listener (`context.getReflectiveFieldValue(card, "message", ...)`) rather than capturing it when the menu is built.

### The story menu bar

The bottom bar of the story editor has a menu bar with the **Summaries** item and the settings item (⚙). It is built
by small methods of `ManuscriptStoryPart` that get the bar as an argument (`bar`), so decorate one of them on leave:

| Method | Add to |
|---|---|
| `populateMenuBar(bar)` | The bar itself - a new item next to the summaries and the settings item. The method is empty and runs after the application's items were added. |
| `createCogsMenuItem(bar)` | The settings menu: the local variable `cogs` is its `MenuItem`, add to `cogs.getSubMenu()`. |

```java
MenuBar bar = context.getMethodArgument("bar", MenuBar.class);
MenuItem cogs = context.getLocalVariable("cogs", MenuItem.class);
cogs.getSubMenu().addItem("My tool", e -> openMyTool(context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class)));
```

The menu bar is built once per story editor, so it needs no marker. Read `currentManuscript` when the item is clicked,
not when it is built - the story editor loads other books into the same instance.

### Tools in the summaries overview

`SummariesDialog` (the overview of the summaries, opened from the **Summaries** item) has an empty menu bar above its
grid. Decorate `populateMenuBar(bar)` on leave and add items to the argument `bar`. A new dialog (and menu bar) is
created every time the overview opens, so the decorator runs for each of them. The dialog's fields (`manuscript`,
`treeGrid`) are reachable by reflection; to reload the grid after your tool changed the summaries, close and reopen
the dialog, or call its private `refresh()` with `callReflectiveMethod`.

Changes to summaries should go through `SummaryService` (`updateSummaryText`, `removeSummary`, `createMetaSummary`):
it keeps the token counts, the hashes of the summaries and the meta summaries that merged them consistent. In
particular, don't edit the text of a summary a meta summary stands in for (shown under it in the overview) - its text
is part of the hash of the meta summary and it would be dropped as outdated.

### A tab next to the story

`ManuscriptStoryPart.renderStoryContent()` rebuilds the whole story view - and the `leftBar` tab sheet with the story
outline - every time the story is redrawn (opening the book, after a generation, after a delete). Side Query adds its
tab on leave:

```java
VTabSheet leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", VTabSheet.class);
Manuscript book = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
if (leftBar != null && book != null && ComponentUtil.getData(leftBar, KEY) == null) {
    SideQueryView view = new SideQueryView(book, sideQueryService);
    leftBar.add("Side Query", view);
    ComponentUtil.setData(leftBar, KEY, view);
}
```

Because the tab sheet is new on every redraw, so is the plugin's component - keep state that must survive a redraw
in the plugin's data, not in the component.

### Panels in an existing layout

Find the layout and insert at a position relative to a known child. Lorebook VCS puts its panel above the lorebook
header that `LorebookView.create()` builds:

```java
HorizontalLayout header = context.getLocalVariable("lorebookHeaderLayout", HorizontalLayout.class);
int index = lorebookView.indexOf(header);
lorebookView.addComponentAtIndex(Math.max(index, 0), panel);
```

A component created once for a view must not hold on to things that change in it: `LorebookView` stays the same
when the user picks another lorebook, so a panel bound to the lorebook it was created with ends up acting on the
wrong one. Look the current value up when it's needed (`lorebookView.getCurrentLorebook()`), or hook the
method that switches it.

### Changing what the application built

The decorator can change any component the method built. Chapter Marker relabels the sidebar button of a part:

```java
ChatMessage msg = context.getMethodArgument("msg", ChatMessage.class);
Button sidebarBtn = context.getLocalVariable("sidebarBtn", Button.class);
sidebarBtn.setText(orderId + ". " + chapterTitle);
sidebarBtn.getStyle().set("color", "var(--lumo-primary-color)");
```

If more plugins change the same component, they run in the order they were loaded and the last one wins - append
or prefix rather than replace where you can.

### Settings panels

The *Settings* screen (`UserPart`) has an *Extension Settings* tab with an `Accordion` in the field
`extensionSettings`, empty unless plugins add to it. The bundled plugins use two decorators:

```java
// UserPart.refresh, on leave: add the panel once, refresh it on later calls
Accordion accordion = context.getReflectiveFieldValue(instrumented, "extensionSettings", Accordion.class);
AccordionPanel panel = (AccordionPanel) ComponentUtil.getData(accordion, SETTINGS_PANEL_KEY);
if (panel == null) {
    panel = accordion.add("Reviewer Settings", new ReviewerSettingsForm(reviewerService));
    ComponentUtil.setData(accordion, SETTINGS_PANEL_KEY, panel);
} else {
    ((ReviewerSettingsForm) panel.getContent().findFirst().orElseThrow()).refresh();
}

// UserPart.save, on ENTER: put the form's values into the user's settings object...
UserSetting userSetting = context.getReflectiveFieldValue(instrumented, "userSetting", UserSetting.class);
reviewerService.saveSettings(form.save(), userSetting);     // writes into userSetting.getAttributes()
// ...which the body of save() then stores with settingService.save(userSetting)
```

Changes in the panel's fields are tracked like the application's own: any value change outside of
`UserPart.refresh` (and its decorators) locks the workspace navigation until the user saves or discards. Fill the form
in the `refresh` decorator, not later (e.g. from a background thread), or the loaded values count as changes.

Saving on enter lets the application's own `settingService.save(userSetting)` store the plugin's values too. Note that
`save()` returns early when the application's own fields don't validate - then the plugin's values are not stored
either.

### Dialogs and own components

Plugin dialogs and components are ordinary Vaadin classes in the bundle. Annotate them `@Configurable` to inject
services. Follow the application's conventions ([User interface → Conventions](../ui.md#conventions)).

What plugins can't do:

- **Add frontend resources.** The browser side of Marginalia is one bundle built with the WAR. A plugin can use every
  Vaadin component and add-on the application already uses (Vaadin core components, the FontAwesome icons,
  Viritin's `VTabSheet`...), but `@CssImport`, `@JsModule`, `@NpmPackage` and add-ons with their own web
  components don't work from a plugin. Style with `getStyle()` and Lumo CSS variables
  (`var(--lumo-primary-color)`), or with `executeJs` if you must.
- **Add localization keys.** `Localization` knows only the application's `L` keys. The bundled plugins use English
  strings; a plugin that wants translations has to bring its own (a `ResourceBundle` in the bundle, chosen by
  `UI.getCurrent().getLocale()`).
- **Add routes.** Routes are registered when the servlet starts. Use dialogs and tabs instead.

## Errors

Inside click listeners and other event handlers, catch exceptions and report them like the application does:
`UIUtils.internalServerError(loc, e)` logs and shows an error dialog - pass an injected `Localization`. Expected problems go to `Notification`. Exceptions in decorators themselves are logged by the
`ExtensionService` and don't need handling unless you want to show something.

## Threads

Decorators run on the thread that called the decorated method. For UI methods that's a request thread holding the
Vaadin session lock, so decorators can change components directly and services see the current user.

Anything slow - calling the model, large imports - belongs on another thread, with the same rules as in the
application ([Background work and push](../ui.md#background-work-and-push)): capture `UI.getCurrent()` on the UI
thread, change components only inside `ui.access(...)`, push with `UIPushGuard.push(ui)`, and wrap service calls that
need the current user in the request scope.

### Calling the model

`InferenceServices.forAI(ai)` returns the client for a provider; `stream(...)` sends a list of messages and calls back
as the answer arrives, on another thread. Reviewer's review dialog, shortened:

```java
UI ui = UI.getCurrent();
InferenceService service = inferenceServices.forAI(ai);
List<LLMChatMessage> payload = List.of(
        LLMChatMessage.of(LLMRole.SYSTEM, "You are a literary critic."),
        LLMChatMessage.of(LLMRole.USER, "Review this:\n\n" + message.getResponse()));

CancellationToken token = service.stream(payload, protocol, new InferenceService.InferenceAsyncCallback() {
    private final StringBuilder text = new StringBuilder();

    @Override
    public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType type, String chunk) {
        ui.access(() -> {
            if (type == InferenceService.ChunkType.RESPONSE) {
                text.append(chunk);
                reviewText.setText(text.toString());
            }
            UIPushGuard.push(ui);
            controller.continueInference();          // ask for the next chunk - the stream is pulled
        });
    }

    @Override public void onCompletion() { ui.access(() -> { save(text.toString()); UIPushGuard.push(ui); }); }
    @Override public void onCancel()     { ui.access(() -> UIPushGuard.push(ui)); }
    @Override public void onError(Throwable e) { ui.access(() -> { showError(e); UIPushGuard.push(ui); }); }
    @Override public boolean isDead()    { return ui.isClosing(); }   // nobody left to show it to
});
// token.cancel() stops the stream, e.g. when the dialog closes
```

- **Call `controller.continueInference()`** after each chunk, or the stream stops after the first one.
  `terminateInference()` ends it early.
- `protocol` supplies sampling parameters and the response limit; it may be `null`.
- Fit the prompt into the model's context yourself: `TokenLimits.promptTokens(ai, protocol)` is the room left for the
  prompt, `service.countTokens(text)` (or the cheaper `countTokensApprox`) counts.
- The stream stops when the token is cancelled or `isDead()` returns `true`, both checked between chunks; the
  callback then gets `onCancel()`.

To change what a *story* generation sends or receives, don't call the model yourself - register a generation listener
like Author's Note does ([Generation events](generation-events.md)).

## Unloading

When a plugin is unloaded, components it added to open screens stay there - with listeners pointing into a bundle
that is gone - unless the plugin removes them. There are two ways.

**Track and remove** - what Reviewer, Side Query and Lorebook VCS do. Keep the extended components in weak sets (so that closed
screens can be garbage collected) and undo the changes in `onExtensionUnload`:

```java
private final Set<ContextMenu> menus = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

@Override
public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
    extensionService.unregisterDecorator(menuDecorator);
    synchronized (menus) {
        for (ContextMenu menu : menus) {
            menu.getUI().ifPresent(ui -> ui.access(() -> {
                MenuItem item = (MenuItem) ComponentUtil.getData(menu, MY_ITEM_KEY);
                if (item != null) {
                    menu.remove(item);
                    ComponentUtil.setData(menu, MY_ITEM_KEY, null);
                }
            }));
        }
        menus.clear();
    }
}
```

**Bind a callback** - `OsgiService.bindAttachableComponent(component, callback, extension)` registers `callback` to
run when the extension is unloaded, for as long as `component` is attached (a component that is detached and attached
again - moved, in a tab sheet, in a reopened dialog - stays bound):

```java
SideQueryView view = new SideQueryView(book, service);
leftBar.add("Side Query", view);
osgiService.bindAttachableComponent(view, () -> leftBar.remove(view), this);
```

(`osgiService` is the one passed to `onExtensionLoad`, or injected.) The callbacks run before `onExtensionUnload`,
each inside `ui.access(...)` of its component's UI, so they can change the component directly. A component that is
detached when the extension is unloaded runs its callback when it is attached again. Calling
`bindAttachableComponent` for an extension that isn't loaded throws an `IllegalStateException`.

!!!warning Other users' sessions
`onExtensionUnload` runs on the request thread of the administrator who clicked *Unload*, but the components belong
to every user's open screens. Change them inside `component.getUI().ifPresent(ui -> ui.access(...))`, as above, or
with `UIUtils.accessComponent(component, () -> ...)`, which does the same and runs the change directly when the
component isn't attached. The bundled Reviewer, Side Query and Lorebook VCS do it this way.
!!!

Data in attributes stays after unloading, so loading the plugin again picks up where it left off. If your plugin is
uninstalled for good, its data stays in the database (and in backups) - harmless, but say so in its README.
