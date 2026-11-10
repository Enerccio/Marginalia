---
label: Inherited settings
order: 66
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/extensions/InheritedSetting.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/extensions/InheritedSettingRegistry.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/extensions/InheritedSettingValues.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/ExtensionSettingsForm.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/FolderDialog.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/manuscript/ManuscriptPromptPart.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/phone/manuscript/PhoneManuscriptPromptPart.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/ManuscriptServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/FolderServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/BackupFolders.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/BackupInheritance.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/OsgiServiceImpl.java
verified: cec4f0cd1
---

# Inherited settings

The text settings of a book (the prompts, the point of view, the style...) are *inherited*: a book uses its own, else the
nearest [folder](../folders.md) above it that sets one, else the user's, else a default. A plugin can give its own
settings the same chain. It registers a setting, the *Prompts* tab of every book and the dialog of every folder get an
**Extension Settings** section (the exporters of the application register their templates there too, see `ExportInheritedSettings`; an accordion, one panel per plugin, the group title is the panel's title) with a field for it, and the plugin asks the service for the value that applies to a book.

The levels of the user are the plugin's business (it has its own panel in the *Settings* screen, see
[Settings panels](ui-extensions.md#settings-panels)), so the chain the application keeps is *book → folders*; the
registration only tells where the user level and the default are, so the dialogs can show what an empty field inherits.

## Registering a setting

The registry is a Spring bean (`InheritedSettingRegistry`), like the story menu registry. Register in `onExtensionLoad`,
unregister in `onExtensionUnload` (the settings of a bundle that is unloaded are removed by its symbolic name anyway).

```java
@Configurable
public class BetaReadersExtension implements MarginaliaExtension {

    @Autowired
    private InheritedSettingRegistry settings;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        settings.register(bundle.getSymbolicName(),
                InheritedSetting.choice("betareaders.medium", () -> T.localized(MEDIUM),
                                () -> mediumService.choices())                 // List<InheritedSetting.Choice>(id, label)
                        .group(() -> T.localized(BETA_READERS))              // the title of the group, once per plugin
                        .help(() -> T.localized(WHAT_THE_TEXT_STANDS))
                        .userValue(() -> userSettings().getMedium())           // the user level of the plugin, null for none
                        .fallback(() -> "book"));                              // when nothing sets it
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
        settings.unregisterAll(b.getSymbolicName());
    }
}
```

| Factory | Editor | Value |
|---|---|---|
| `InheritedSetting.text(key, label)` | a line | the text |
| `InheritedSetting.textArea(key, label)` | several lines (resizable like the prompts) | the text |
| `InheritedSetting.choice(key, label, choices)` | a box with a clear button | the `id` of the picked `Choice` |

A value is always a text. Anything richer (a list, a number) is the plugin's own editor and storage.

- **The key** is what the value is stored under in the attributes of every book and folder, so prefix it with the name of the
  plugin (`betareaders.medium`); the keys of all plugins share one object.
- **The texts are suppliers**, not strings: a language is chosen per session and the dialogs ask each time they are built, so
  use `T.localized(...)` inside the lambda.
- **`userValue`** is what the plugin's own user settings hold (null or blank for nothing); **`fallback`** is the last level and is
  empty when it is not given. The dialogs show them as the placeholder of an empty field with the line *Inherited from your
  settings* or *Built-in default*. Both are called from whatever thread resolves a setting, a generation worker included (it has
  the request attributes of the session, like the application's own services), and an exception in a supplier is logged and
  treated as no value.
- **`variables(Class)`** (a text area that holds a template): the data class the template is rendered with; the hints popover of the field lists its properties as the variables
  the template can use.
- A setting registered later than a dialog was built is not in that dialog; a new dialog has it.

## Reading the value

```java
@Autowired
private ManuscriptService manuscriptService;

EffectiveSetting<String> medium = manuscriptService.resolveExtension(manuscript, "betareaders.medium");
medium.value();     // the text, blank only if the fallback is
medium.origin();    // BOOK, FOLDER, USER or DEFAULT
medium.folder();    // the folder that sets it when the origin is FOLDER
```

`resolveExtension(manuscript, key)` walks the book, then the folders above it, then the user level and the default of the
registration. `resolveExtensionInherited(folder, key)` is what a book filed in that folder would inherit (the top level for
`null`). A key nothing is registered for (the plugin is not loaded) has no user level and an empty default, but the values of the
book and the folders are still found.

Use the same call for a chat tab, a prompt, a command: whatever the plugin does for a book should ask here rather than read the
book's attributes itself, or the folders are skipped.

## Where the values are

`InheritedSettingValues` reads and writes them: an object `inheritedSettings` in the dynamic `attributes` of the `Manuscript` and
the `Folder` with one text per key ([Extended attributes](extended-attributes.md)). A blank text is not stored. There is no schema
change, the values travel with the entity and stay when the plugin is unloaded.

```java
InheritedSettingValues.get(book, "betareaders.medium");          // what the book sets itself, null for nothing
InheritedSettingValues.set(book, "betareaders.medium", "movie");  // a blank value removes it
manuscriptService.save(book);                                     // like any extended attribute, stored by the service's save
```

The resolvers (`ManuscriptServiceImpl`) only read the user's settings with `SettingService.find`: a user who never saved settings has set nothing, and the row is not created inside a read-only transaction.

The dialogs do this themselves; a plugin writes the values only when it sets them without a dialog (a command, an import).
Changing a folder goes through `FolderService.update(folder, new Details(..., extensions))`, which refuses while an agent works on
a book below it, like a change of a prompt.

## What the application does with them

- **Book and phone dialog.** The *Prompts* tab gets the `ExtensionSettingsForm` below the prompts, autosaved on change like them. A text area of the form is a `TextAreaPopoverComponent`: its info icon shows the inherited text (`TemplateHints`) so the author can copy it into the field.
- **Folder dialog.** The same form below the prompts; saved with the *Save* button, in the same call as the prompts.
- **Backups.** The path of folders in a backup of a book carries the folders' values (`extensions` of each level), and a missing folder
  is made again with them. An export that writes what the book inherits writes the values of the nearest folder and the user level of
  every registered setting into the book (the book's own values stay), see `BackupInheritance`.
- **Unload.** `OsgiServiceImpl` unregisters the settings of the bundle, so the sections disappear until it is loaded again.

## Tests

`ExtensionSettingsInheritanceTest` (the chain, the registry, a failing supplier), `BackupExtensionSettingsTest` (the backup and
the export), `FolderDialogTest` and `BookPromptSettingsTest` (the section in the dialogs). Register in `@BeforeEach` and
`unregisterAll` in `@AfterEach`: the registry is a singleton of the test context.
