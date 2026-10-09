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
   so mark the component with `ComponentUtil.setData(component, key, value)` and check the mark first. Use keys
   prefixed with your plugin's name.
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
wrong one (BUG-49). Look the current value up when it's needed (`lorebookView.getCurrentLorebook()`), or hook the
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
`UIUtils.internalServerError(loc, e)` logs and shows an error dialog - pass an injected `Localization`, not `null`
(BUG-50). Expected problems go to `Notification`. Exceptions in decorators themselves are logged by the
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
([Generation pipeline → Events for extensions](../generation-pipeline.md#events-for-extensions)).

## Unloading

When a plugin is unloaded, components it added to open screens stay there - with listeners pointing into a bundle
that is gone - unless the plugin removes them. There are two ways.

**Track and remove** - what Reviewer and Side Query do. Keep the extended components in weak sets (so that closed
screens can be garbage collected) and undo the changes in `onExtensionUnload`:

```java
private final Set<ContextMenu> menus = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

@Override
public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
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
run when the extension is unloaded, and forgets it when `component` is detached:

```java
SideQueryView view = new SideQueryView(book, service);
leftBar.add("Side Query", view);
osgiService.bindAttachableComponent(view,
        () -> view.getUI().ifPresent(ui -> ui.access(() -> leftBar.remove(view))), this);
```

(`osgiService` is the one passed to `onExtensionLoad`, or injected.) The callbacks run before `onExtensionUnload`. A
component that is detached and attached again loses its callback (BUG-54), so use this for components that are
attached once.

!!!warning Other users' sessions
`onExtensionUnload` and the bound callbacks run on the request thread of the administrator who clicked *Unload*, but
the components belong to every user's open screens. Change them inside `component.getUI().ifPresent(ui ->
ui.access(...))`, as above - the bundled Reviewer and Side Query change them directly (BUG-53).
!!!

Data in attributes stays after unloading, so loading the plugin again picks up where it left off. If your plugin is
uninstalled for good, its data stays in the database (and in backups) - harmless, but say so in its README.
