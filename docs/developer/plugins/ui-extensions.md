# Extending the UI

Plugins add to the user interface from decorators: when a method that builds part of a screen runs, the plugin's
decorator finds the component it built and adds to it. This page collects the patterns the bundled plugins use. Read
[@Extendable hooks](extendable.md) first, and [User interface](../ui.md) for the conventions of the UI code.

Every screen below exists twice: the desktop class and its phone twin (`ManuscriptStoryPart` and
`PhoneManuscriptStoryPart` and so on), and a decorator only runs for the class it was registered for. Register it a
second time for the phone class to support phones, or leave it out and the extension simply adds nothing there, see
[The phone twins](extendable.md#the-phone-twins).

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

Use the [menu registry](#menu-items-of-the-story-editor) below: an item of the menu of a part (☰ on a card) is a `StoryMenuItem.card(..)`, with its
sub menus, its state worked out each time the menu opens and a row in the key bindings. It works on the desktop and the phone story tab alike.
Reviewer registers its *Review* sub menu this way (`ReviewerExtension`).

The older way still works: decorate `ManuscriptStoryPart$ChatMessageCard.createMenuItems` (called when the menu of the card is opened for the first
time, not when the card is created, after the registry items were added) and add to the `hamburgerMenu` field; the card's `message` field is the part.
Such an item has no shortcut row, no key hint and you have to remove it yourself on unload. To update items each time the menu opens, decorate
`refreshSummaryMenuItems`, which the card runs each time the menu is about to open. It can also run before the menu was ever opened: the items are
not created then, so check what you read from the menu for `null`. The card replaces its `message` with the saved copy whenever the part is
edited, so read the field again in the click listener rather than capturing it when the menu is built.

### The story menu bar

The bottom bar of the story editor has a menu bar with the **Summaries** item and the settings item (⚙). It is built
by small methods of `ManuscriptStoryPart` that get the bar as an argument (`bar`), so decorate one of them on leave:

| Method | Add to |
|---|---|
| `populateMenuBar(bar)` | The bar itself - a new item next to the summaries and the settings item. The method is empty and runs after the application's items were added. |
| `createCogsMenuItem(bar)` | The settings menu: the local variable `cogs` is its `MenuItem`, add to `cogs.getSubMenu()` (an item of the [registry](#menu-items-of-the-story-editor) is better: it gets a key and no cleanup). |

```java
MenuBar bar = context.getMethodArgument("bar", MenuBar.class);
MenuItem cogs = context.getLocalVariable("cogs", MenuItem.class);
cogs.getSubMenu().addItem("My tool", e -> openMyTool(context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class)));
```

The menu bar is built once per story editor, so it needs no marker. Read `currentManuscript` when the item is clicked,
not when it is built - the story editor loads other books into the same instance.

Instruction Exporter is the small example of the settings menu: it adds **Export instructions** to `cogs.getSubMenu()` and
copies the text with `navigator.clipboard.writeText` (plugins keep their own scripts).

### Tools in the summaries overview

`SummariesDialog` (the overview of the summaries, opened from the **Summaries** item) has a menu bar above its
grid, with the item *Search and Replace* of the application in it. Decorate `populateMenuBar(bar)` on leave and add items to the argument `bar`. A new dialog (and menu bar) is
created every time the overview opens, so the decorator runs for each of them. The dialog's fields (`manuscript`,
`treeGrid`) are reachable by reflection; to reload the grid after your tool changed the summaries, close and reopen
the dialog, or call its private `refresh()` with `callReflectiveMethod`.

Changes to summaries should go through `SummaryService` (`updateSummaryText`, `removeSummary`, `createMetaSummary`):
it keeps the token counts, the hashes of the summaries and the meta summaries that merged them consistent. In
particular, don't edit the text of a summary a meta summary stands in for (shown under it in the overview) - its text
is part of the hash of the meta summary and it would be dropped as outdated.

### A tab next to the story

`ManuscriptStoryPart.renderStoryContent()` redraws the message cards and the chapter outline every time the story is
redrawn (opening the book, after a generation, after a delete); the `leftBar` tab sheet with the story outline is created
once, so the selected tab survives a redraw. The method runs many times, so check for your tab first. Side Query adds its
tab on leave:

```java
TabDock leftBar = context.getReflectiveFieldValue(instrumented, "leftBar", TabDock.class);
Manuscript book = context.getReflectiveFieldValue(instrumented, "currentManuscript", Manuscript.class);
if (leftBar != null && book != null && ComponentUtil.getData(leftBar, KEY) == null) {
    SideQueryView view = new SideQueryView(book, sideQueryService);
    leftBar.add("sidequery", "Side Query", view);
    ComponentUtil.setData(leftBar, KEY, view);
}
```

The tab dock (and so the plugin's component) lives as long as the story part, a redraw does not recreate it.

`leftBar` is a `TabDock` (`ui.widgets`; since 1.3.0, before it was a `VTabSheet`): on the desktop the user can drag its tabs
into panels of their own, stacked or side by side, and `add`/`remove` find the tab wherever it is now. Since 1.5.0 the story
itself is a tab of this dock (the id `story`), so the dock is the whole editor and the user may put the story and your tab in
any arrangement; a tab the saved layout does not know still lands in the first sheet, the sidebar of a new book. A tab that
is not the selected one of its sheet is hidden and its content disabled (the sheet does that), so do not expect events from your
component then; `leftBar.select(id)` shows a tab. Give the tab a **stable id** of your own
(`add(id, caption, content)`, not the localized caption): the layout the user made is saved with the book by these ids, and
a tab added later or after the extension was off lands where the user left it. `add(caption, content)` uses the caption as
the id. Since 1.6.0 every tab has a minimize button, and a minimized tab is an icon in the bar of the dock: a circle with one or
two letters, made by `TabDockIcons`. `add(id, caption, letters, content)` gives the letters of your tab (`"SQ"`); without them
they are the initials of the caption. A minimized tab stays in the dock (its content is alive, only not in a sheet);
`leftBar.select(id)` restores it, `isMinimized(id)`, `minimize(id)` and `restore(id)` do what they say, and
`setMinimizable(id, false)` takes the button away from a tab. The ids of the minimized tabs and the side of the bar are in
`getLayout()` (`minimized`, `dock`), so they are saved with the book like the rest of the arrangement. The ids `chapters` (the outline), `lorebook` (the read only lorebook preview, desktop only) and `story` (since 1.5.0) belong to the
application; a tab with the same id would replace them. Remove the tab with `leftBar.remove(view)`. The phone drawer has a `TabDock` too, but with the detaching off.

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

### Columns in an existing grid

A grid the view keeps in a field can get more columns. Lore Generator adds a button to every row of the lorebook
entries after `LorebookView.create()` has built the grid, and keeps the column to remove it on unload:

```java
Grid<LorebookEntry> grid = context.getReflectiveFieldValue(instrumented, "grid", Grid.class);
Grid.Column<LorebookEntry> column = grid.addComponentColumn(entry -> new Button(icon, e -> openDialog(entry)));
ComponentUtil.setData(lorebookView, COLUMN_KEY, column);          // onExtensionUnload: grid.removeColumn(column)
```

The row's item is the entity itself; read the context that is not part of it (the selected lorebook) from the view
when the button is clicked, as above. A dialog the button opens is an ordinary component of the plugin: it can call the
model with `InferenceService.stream` (put `LLMAttachment`s on the user message to send images or files, see
[Services](../services.md)) and must update the UI through `ui.access(...)` and `UIPushGuard`.

### Changing what the application built

The decorator can change any component the method built. Chapter Marker relabels the sidebar button of a part:

```java
ChatMessage msg = context.getMethodArgument("msg", ChatMessage.class);
Button sidebarBtn = context.getLocalVariable("sidebarBtn", Button.class);
sidebarBtn.setText(orderId + ". " + chapterTitle);
sidebarBtn.addClassNames(CssStyles.TEXT_PRIMARY, CssStyles.TEXT_SEMIBOLD);
```

If more plugins change the same component, they run in the order they were loaded and the last one wins - append
or prefix rather than replace where you can.

### Keyboard shortcuts

Shortcuts work in the desktop layout only (the phone twin of the story has none). Register an action with a default key; it is listed in
*Settings → Keyboard shortcuts*, where the user can change or clear the key, and the handler runs when the key is pressed
in the open story tab (not while a generation runs). The handler gets the component of the story tab.

```java
ShortcutRegistry.register(new ShortcutAction("myplugin.summarize",
        loc -> "Summarize the last part",      // the name in the table, in the language of the session
        "Ctrl+Shift+y",                         // canonical key combination (KeyCombo), null for none
        storyTab -> summarizeLastPart(storyTab)));
```

Prefix the id with the plugin name. A key without Ctrl/Alt/Meta fires only when no text field has the focus; avoid keys
the browser keeps (`Ctrl+N/T/W`) and `Alt` with a letter (types a character on a Mac). Unregister in `onExtensionUnload`
(`ShortcutRegistry.unregister(id)`); the stored key of the user stays for when the plugin is back.

### Menu items of the story editor

Three menus of the story editor are filled from the `StoryMenuRegistry` (`ui.menu`): the **settings menu** (⚙ at the bottom, `StoryMenuItem.tools`), the
**menu of a part** (☰ on a card, `StoryMenuItem.card`) and the **right click menu of the text of a part** (long press on a phone,
`StoryMenuItem.text`). Registering an item instead of decorating a method gives you:

- the item on the **desktop and the phone** story tab with one definition (`.phone(false)` for desktop only),
- a row in *Settings → Keyboard shortcuts* (a default key, or none) and the key of the user shown next to the item,
- text, visibility and enabled / ticked state that are **worked out each time the menu opens**, so an item can depend on the part,
- **sub menus** whose items come and go,
- no cleanup: the items are removed with `unregisterAll(owner)` (and a bundle that is unloaded loses the rest by itself).

**1. Get the registry.** The extension class is `@Configurable`, so inject it like any service:

```java
@Configurable
public class MyExtension implements MarginaliaExtension {
    @Autowired
    private StoryMenuRegistry menuRegistry;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        menuRegistry.register(bundle.getSymbolicName(),
                StoryMenuItem.tools("myplugin.export", loc -> "Export my format",
                                story -> exportBook(story.getCurrentManuscript()))
                        .icon(Solid.FILE_EXPORT::create));
    }

    @Override
    public void onExtensionUnload(Bundle bundle, OsgiService osgiService, ExtensionService extensionService) {
        menuRegistry.unregisterAll(bundle.getSymbolicName());
    }
}
```

The first argument is the **owner** (use the bundle's symbolic name); the key of an item (`"myplugin.export"`) identifies it, so prefix it with your plugin's name.
Registering a key again replaces the item. Instruction Exporter is the whole of this example.

**2. Choose the menu and the callback.** What the callback gets depends on the menu:

| Factory | Menu | Callback |
|---|---|---|
| `StoryMenuItem.tools(key, label, action)` | settings menu | `Consumer<StoryEditor>`: `story -> ...` |
| `StoryMenuItem.card(key, label, action)` | menu of a part | `BiConsumer<StoryEditor, StoryCard>`: `(story, card) -> ...` |
| `StoryMenuItem.text(key, label, action)` | right click menu of the text | `BiConsumer<StoryEditor, StoryCard>` |

`label` is a `Function<Localization, String>` (`loc -> "My item"`, or `loc -> loc.getValue(L.X)` for text of the application; a plugin cannot add keys to `L`, use
`Localization.getCurrentLanguage()` for your own table), and the overloads with an `L` key are for the application's own items. The label is also the
name of the action in the key bindings table, so make it say what the action does.

`StoryEditor` is the story tab (the desktop or the phone one): `getCurrentManuscript()` (the book it shows **now**, read it when the action runs, the tab loads other
books into the same instance), `openSearchReplaceDialog()`, `openExportDialog()`, `toggleBookStyles()`... `StoryCard` is the card of the part the menu is of:
`getMessage()` (the `ChatMessage`, the card replaces it with the saved copy, so read it each time), `isEditing()`, `isLast()`, and the actions of the part
(`toggleEdit()`, `swipe()`, `branch()`, `annotatePart()`...). The callback runs on the UI thread of the user, in a request, as any click listener.

**3. Describe when it shows.** All of these are optional and are asked again each time the menu opens (and after an item of the settings menu ran):

```java
StoryMenuItem.card("myplugin.summarize", loc -> "Summarize with notes",
                (story, card) -> summarize(card.getMessage()))
        .icon(VaadinIcon.NOTEBOOK::create)                       // a new Icon per menu
        .defaultKey("Ctrl+Shift+y")                              // KeyCombo text, null (the default) for no key
        .visibleWhen((story, card) -> card.getMessage().getId() != null)
        .enabledWhen((story, card) -> !card.isEditing())
        .contextLabel((loc, story, card) -> card.isLast() ? "Summarize (last part)" : "Summarize")   // the text in the menu
        .phone(false)                                            // not on the phone
```

A tools item can be a check box: `.checkedWhen(story -> story.isShowGeneratedDiff())` (the action switches whatever the predicate reads; a ticked item is
refreshed after the action ran). `.actionId("myplugin.summarize")` gives the item the id of its key binding when it is not `menu.<key>` (keep it when you
rename the item and the user has a key stored), and `.hintAction((story, card) -> id)` shows the key of another action (the *Edit* item shows the key of saving while the text is edited).

**4. Sub menus.** An item that only holds other items has no callback; its items name it with `parent(key)` (any depth). Reviewer:

```java
menuRegistry.register(owner, StoryMenuItem.card("reviewer.review", l -> "Review", null).icon(VaadinIcon.STAR::create));
menuRegistry.register(owner, StoryMenuItem.card("reviewer.review.view", l -> "View / Generate Review", (story, card) -> openReview(card))
        .parent("reviewer.review"));
menuRegistry.register(owner, StoryMenuItem.card("reviewer.review.delete", l -> "Delete Review", (story, card) -> deleteReview(card))
        .enabledWhen((story, card) -> card != null && hasReview(card.getMessage()))      // the state follows the part
        .parent("reviewer.review"));
```

A parent must be registered before its items; an item whose parent is missing (or not shown on the phone) is left out. Items appear in the order of registration, after the
application's.

**5. Keys.** Every item with a callback is an action of *Settings → Keyboard shortcuts* (the id is `menu.<key>` or the `actionId` you gave) from the moment it is registered, with
`defaultKey` or without. A key of a `card` or `text` item acts on the part that is being edited, else the part that holds the text selection, else the last part, and on the last
part when the item is not visible or enabled for the chosen one (the same predicates decide). A `tools` item needs no part. Shortcuts work on the desktop only.
Pick a default that the browser does not keep (`Ctrl+N/T/W`, `Alt` with a letter on a Mac) or none.

**6. What happens on unload and reload.** `unregisterAll(owner)` removes the items from the registry and from the key bindings table. Menus that are open keep the Vaadin items they were
built with, but an unregistered item is retired: it is hidden the next time its menu is refreshed (a part menu when it opens, the settings menu after any of its items ran or the
story tab is entered) and it runs nothing. Registering the same key again (a reloaded extension) replaces the item. New menus, and part menus that are opened for the first
time, have the items registered at that moment; a menu that was built before your extension loaded does not get them until the story is opened again.

The hooks `createCogsMenuItem` and `createMenuItems` stay for plugins that add other things than items (a decorated method can do anything), and what a decorator adds directly is not
managed by the registry. Test your items without a browser as the application does (`StoryMenuRegistryTest`): `registry.definition(kind, phone, bindings).build(new ContextMenu(), loc, story, () -> card)`
returns a `BuiltMenu` whose items (`get(key)`) can be inspected and clicked with stub implementations of `StoryEditor` and `StoryCard`.

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
  components don't work from a plugin. Style with the classes of `CssStyles`
  (`addClassName(CssStyles.TEXT_SECONDARY)`), which the application's `shared-styles.css` defines. A style only your
  plugin uses goes into a CSS file in the bundle's `src/main/resources` that the application puts into the page for
  you ([Styles of a plugin](#styles-of-a-plugin)), with its class names as constants in the plugin. Use `getStyle()` only for a value known at runtime.
  Charts are the exception: the application's `ChartJs` widget (`ui.widgets`) draws any Chart.js configuration given as
  JSON, so a plugin can show a chart without a frontend module of its own.
- **Add localization keys.** `Localization` knows only the application's `L` keys. A plugin translates the texts of
  its own UI with a `PluginLocalizationBundle` ([Texts in several languages](#texts-in-several-languages)). A plugin
  can also add a *language* for the application's texts, see
  [Example plugin → Chinese localization](example.md#walkthrough-chinese-localization).
- **Add routes.** Routes are registered when the servlet starts. Use dialogs and tabs instead.

### Texts in several languages

A plugin can't add keys to the `L` enum, so the texts of its UI are the English sentences themselves.
`PluginLocalizationBundle` (package `extensions`) turns one into the text of the language of the current session
(`Localization.getCurrentLanguage()`): `localized(message)` returns the translation, and the English `message` when the
language has none, so a language or a text that is missing shows English. The bundled plugins have one subclass each and
call it through a static field:

```java
public class InstructionExporterLocalization extends PluginLocalizationBundle {

    public static final InstructionExporterLocalization T = new InstructionExporterLocalization();

    @Override
    protected void loadKeys() {
        ja("Export instructions", "指示をエクスポート");
        zh("Export instructions", "导出指令");
        ja("Failed to export: %s", "エクスポートに失敗しました: %s");
        zh("Failed to export: %s", "导出失败：%s");
    }
}
```

```java
import static ...InstructionExporterLocalization.T;

Notification.show(T.localized("Copied to clipboard"));
Notification.show(T.localized("Failed to export: %s").formatted(e.getMessage()));
```

- `ja` and `zh` are shortcuts for `add(Language, message, translation)`; add other languages the same way (the language
  needs a bundle in `Localization`, see the [Chinese localization](example.md#walkthrough-chinese-localization)).
- A text with values has the `String.format` placeholders in the key (`%s`, `%d`) and the values are put in with
  `formatted(...)`. When a language needs another order, number the placeholders in the key and in the translation
  (`%1$d turns for the messages %2$d-%3$d`). Don't build a sentence from pieces: a language puts the words in another
  order, write one key for each whole sentence (and one for the singular and the plural).
- Look the text up when it is shown, not in a field or a constant: the bundle answers for the language of the session
  that asks, and a text kept in a field stays in the language of whoever created it. A lambda that is called when a
  menu is opened (`l -> T.localized("Export instructions")`) is fine.
- Without a session (startup, a worker thread that got no request attributes) the default language is used, so the
  title that a plugin registers in `onExtensionLoad` (`ToolService.registerProvider(..., "Name provider", ...)`) stays in
  English; the same holds for annotation values (`@CommandParameter(descriptionText = ...)`), which must be constants.
- Texts for the model (prompts, the descriptions of functions and commands, the error messages of commands) stay in
  English; only what the user reads is translated.
- For words the application has a key for (`L.LABEL_CANCEL`) the plugin can use `loc.getValue(...)` instead.

### Telling the help assistant about your UI

The [help assistant](../services.md#the-help-assistant) can read the screen of the user, and, when an administrator allows
it, move through it. It sees only the classes that implement `LLMInteract`
([User interface → Letting the help assistant see the UI](../ui.md#letting-the-help-assistant-see-the-ui)), so a plugin
dialog or panel that does not is not part of what it reads: a dialog of yours is simply absent from the screen.

```java
@Configurable
public class MyPanel extends VerticalLayout implements LLMInteract {
    @Override
    public String getLLMState() {
        return "The panel of My plugin, with the filter **" + filter.getValue() + "**.";   // what the user sees, nothing secret
    }
}
```

- The element is found under the nearest `LLMInteract` above it in the component tree, and only while it is visible (a dialog
  only while it is open). Its path is the names of the elements above it and its own simple class name.
- `getLLMState` is Markdown for the model: say what the user sees and would say themselves (the open tab, a filter, a title). Never
  the text of a story or a lorebook, a key, a password or an id: the state goes to the administrator's AI, which may be an
  outside provider.
- `getInteraction()` offers moves the model may make: a map from a key to an `LLMInteraction(description, action)`. Only
  harmless ones the user could undo with a click (select a tab, open a section), never anything that changes, saves, deletes,
  sends or generates; none while your UI is locked. `LLMInteract.tabs(...)` makes the entries for a row of tabs,
  `LLMInteract.dialogButtons(button...)` the ones that press a button that opens a dialog (offered only while no dialog is open).
- `getLLMProblems()` is what the model reads to explain why a form can not be saved. The default already lists the visible fields of
  your component that are invalid (with the message the field shows) or required and empty, by their labels, never their values;
  override it only for a rule that your save method checks across fields (`LLMInteract.super.getLLMProblems()` plus your lines).
  Give your fields labels and mark the required ones, then the model can name them.
- To add to a class of the application, decorate its `getLLMState` (or `getInteraction`) with `getCurrentReturn` /
  `setCurrentReturn` (see [Changing the return value](extendable.md#changing-the-return-value)); append to the text and put your
  entries into a copy of the map. Only the classes that override those methods can be decorated: both workspaces, `AdminPart` (it exists only for administrators),
  both story editor dialogs, `Main`, `UserPart` and the lists of models, protocols and books (`getLLMState`, `getInteraction`) and
  `AIDialog` (`getLLMProblems`); the other elements use the defaults of the interface.
- What a user was shown that went wrong is recorded for you: your `Notification.error` / `warning` (the application's
  `ui.widgets.Notification`) and `UIUtils.showError` / `showValidationErrors` / `inferenceError` write it to the session's problem
  log, which the model reads with `read_last_errors` ([What went wrong](../ui.md#what-went-wrong-the-problem-log)). Say what is wrong in the message (the model gets the text the user saw).

## Styles of a plugin

A plugin can't add to the application's stylesheet, but it can ship its own CSS. Implement `ExtensionHasCss` on the
class that implements `MarginaliaExtension`:

```java
public class MyExtension implements MarginaliaExtension, ExtensionHasCss {

    @Override
    public String getCss(ApplicationEntryPoint entryPoint) {
        // the card lives in the workspace only
        return entryPoint instanceof Main ? MyStyles.css() : null;
    }
    ...
}
```

`getCss` returns the CSS text for a page, or `null`/blank when the extension has no styles there. The entry point is
the root component of the page: `Main` (the workspace) or `Viewer` (the reader); both implement
`ApplicationEntryPoint`. The text is usually a resource of the bundle, read once and cached:

```java
try (InputStream in = MyStyles.class.getResourceAsStream("/my-plugin.css")) {
    css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
}
```

Use Lumo variables (`var(--lumo-contrast-10pct)`) in the rules and keep the class names as constants next to the
loader (Side Query: `SideQueryStyles` and `sidequery.css`); prefix them with the plugin's name, the page is shared
with the application and other plugins.

A plugin can use the application's components that draw with a JavaScript library (`TreantTree`, `ChartJs`, `GraphView`,
Character Tracker's relationship map is a `GraphView`), but it can't add a library of its own, see
[Custom components](../custom-components.md#components-in-plugins).

**What the application does.** Each entry point owns a `CssRegistry` (created in `onAttach`, released in `onDetach`).
For every UI it

- adds the style of each extension that is loaded already when the entry point is attached (an extension loaded at
  startup, or before the user opened the page),
- adds the style of an extension that is loaded while the page is open (*Admin → Extensions → Load*), so it works
  without a reload,
- removes the style again when the extension is unloaded, replaced by a newer version or the application stops.

The CSS goes into a `<style id="marginalia-extension-css-<extension class name>">` element in the document head; the
element is replaced if it exists, so nothing is added twice. The registry listens to `OsgiService` through an
`ExtensionListener` ([Extension events](overview.md#extension-events)). `getCss` is called once per page and extension
and an exception from it is logged and skips only that extension, but it should not do anything expensive.

!!!warning Do not inject the style yourself
Don't add the `<style>` element from your own components (`executeJs` in a constructor): the style would not be
removed when the extension is unloaded and would be added again for each component. Return it from `getCss`.
!!!

## Errors

Inside click listeners and other event handlers, catch exceptions and report them like the application does:
`UIUtils.internalServerError(loc, e)` logs and shows an error dialog - pass an injected `Localization`. Expected problems go to `Notification`. Failures of the model API (`InferenceException`) are explained by `UIUtils.inferenceError(loc, e)` or, as text, `InferenceErrors.messageOf(loc, e)`. Exceptions in decorators themselves are logged by the
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
    @Override public void onError(Throwable e) { ui.access(() -> { UIUtils.inferenceError(loc, e); UIPushGuard.push(ui); }); }
    @Override public boolean isDead()    { return ui.isClosing(); }   // nobody left to show it to
});
// token.cancel() stops the stream, e.g. when the dialog closes
```

- **Name the kind of work.** Open an `InferenceSession` around the call that starts the request, with a kind named like your
  plugin: `try (InferenceSession _ = InferenceSession.open("my-plugin")) { service.stream(...); }` (a second argument tells
  apart runs that do not share a prompt). The service then sends the header `X-Session-ID` with a hash of the user and
  the kind, which a server or proxy with a prompt cache can use to keep a cache per kind of work; see [Prompt cache and
  session id](../../user/prompt-cache.md). The scope covers only the call itself; a request outside of any scope sends no header.
- **Call `controller.continueInference()`** after each chunk, or the stream stops after the first one.
  `terminateInference()` ends it early.
- `protocol` supplies sampling parameters and the response limit; it may be `null`.
- Fit the prompt into the model's context yourself: `TokenLimits.promptTokens(ai, protocol)` is the room left for the
  prompt, `service.countTokens(text)` (or the cheaper `countTokensApprox`) counts.
- `infer(...)` is the same call without streaming: the callback gets the whole text once (`InferenceResultCallback`). With
  a JSON schema, or a class annotated with `@LLMSchema` / `@LLMField`, the provider is asked to follow it and the
  answer is checked (`SchemaCheck`) or read into an object (`TypedInferenceCallback`); `stream` can pass a schema too,
  but checks nothing. Both can offer functions to the model.
- The stream stops when the token is cancelled or `isDead()` returns `true`, both checked between chunks; the
  callback then gets `onCancel()`.

To change what a *story* generation sends or receives, don't call the model yourself - register a generation listener
like Author's Note does ([Generation events](generation-events.md)).

## Unloading

When a plugin is unloaded, components it added to open screens stay there - with listeners pointing into a bundle
that is gone - unless the plugin removes them. There are two ways.

**Track and remove** - what Reviewer, Side Query, Lore Generator and Lorebook VCS do. Keep the extended components in weak sets (so that closed
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
leftBar.add("sidequery", "Side Query", view);
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
component isn't attached. The bundled Reviewer, Side Query, Lore Generator and Lorebook VCS do it this way.
!!!

Data in attributes stays after unloading, so loading the plugin again picks up where it left off. If your plugin is
uninstalled for good, its data stays in the database (and in backups) - harmless, but say so in its README.
