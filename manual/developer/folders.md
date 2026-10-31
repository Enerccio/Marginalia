---
label: Folders
order: 955
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/model/impl/Folder.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/repository/FolderRepository.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/repository/impl/FolderModification.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/repository/impl/FolderScope.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/repository/impl/JpaFolderRepository.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/FolderService.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/FolderBookLockedException.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/EffectiveSetting.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/PromptField.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/FolderServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/BackupFolders.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/BackupInheritance.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/HelpBookData.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/FolderActions.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/FolderDialog.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/OrganiseDialog.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/PromptSettingsForm.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/InheritedLanguageCombo.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/FolderBookProvider.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/main/Viewer.java
verified: b102fd45a
---

# Folders

Folders put books into a tree and let the books in a tree **inherit settings**. This page follows the feature through the
layers: the entity, how a setting is resolved, the services, the lists and windows, and the places that had to learn about
folders (backups, agents, the help assistant, cleanup). The user's view is in [Folders](../user/books/folders.md).

## The data

`Folder` (`domain/model/impl`, table `folders`, an `ExtendableEntity`, so it has an owner, a `uuid`, soft delete and the
extended content) is:

| Field | Where | |
|---|---|---|
| `name` | column | Unique among the (not deleted) folders of one parent of one user, compared case-insensitively; 1-255 characters, no `/`. The rules are checked by every `save` of `FolderServiceImpl`, not by a constraint (the soft delete and the case rule do not fit SQLite's). |
| `parent` | column `parent_id`, `@CleanupReference(OWNED_BY)` | Null is the top level. A folder purged by [Cleanup](../user/administration/cleanup.md) takes the folders below it with it. |
| `description`, `language`, `template`, `pov`, `tense`, `style` and the prompts | `@ExtendedAttribute` | The settings of the books below. Every one is nullable and **null means inherit**: there is no default on a folder. |

`Manuscript.folder` is a nullable `ManyToOne` column `folder_id` (null is the top level), annotated `@CleanupReference(WEAK)`: a
purge of the folder unlinks the books that are still alive instead of deleting them. The migration is `V21__folders.sql`
(table, sequence, `manuscripts.folder_id` and the indexes `(parent_id, is_deleted)`, `(userId, is_deleted)` and
`(folder_id, is_deleted)`). `persistence.xml` lists the entity, `services-config.xml` the repository and the service.

The limit of the [version history](version-history.md#the-limit-and-the-pruning) is a setting of the folder too, `historyMaxVersions` (an `Integer`, null is not set), carried by `FolderService.Details` (its four-argument constructor leaves it null, which clears it) and copied by `copyTree`.

The settings are extended attributes and not columns because nothing queries them in SQL; the price is the usual one, they
are persisted only by `FolderService.save`/`update`. The name is a column because the lists sort and filter on it.

Two more extended attributes of `UserSetting` serve the feature: `favoriteBooks` (the uuids of the favorite books) and
`helpAssistantBooks` (the opt-in of the help assistant). `UserSetting.defaultStoryLayout` is not about folders but arrived with
the same work (see [the story layout](ui.md)).

## Resolving a setting

A book's setting is the first non-blank of: **the book, the folders above it (nearest first), the `UserSetting`, the built-in
default** (`Defaults`). The code is in three places:

- `PromptField` (`domain/service`) is an enum of the twelve text settings (`MASTER_TEMPLATE`, `POV`, `TENSE`, `STYLE` and the
  eight prompts). It holds the getters of each level (`ofBook`, `ofFolder`, `ofUser`), the setters (`setOfBook`, `setOfFolder`,
  which store a blank as null) and the built-in `fallback()`. A new inheritable setting is a new constant here plus the field on
  `Manuscript` and `Folder`, and nothing else in the resolver.
- `EffectiveSetting<T>(value, origin, folder)` is what the resolver returns: the value, where it is set (`Origin` is `BOOK`,
  `FOLDER`, `USER` or `DEFAULT`) and the folder when it is `FOLDER`. `isCustom()` is true for the book and the folders.
- `ManuscriptService.resolve(book, field)` and `resolveLanguage(book)` do the lookup; `resolveInherited(folder, field)` and
  `resolveInheritedLanguage(folder)` start from a folder (what an empty field would inherit, for the placeholders of the
  dialogs). The old getters (`getPov`, `getStyle`, `getTemplate`...) keep their signatures and return `resolve(...).value()`; they
  were twelve copies of the same lookup before. `getLanguage(book)` is the resolved language, `Manuscript.getLanguage()`
  is still the book's own or English, and `Manuscript.getOwnLanguage()` is null when the book sets none.

`FolderService.findChain(folder)` reads the folder and its ancestors by id (folder first, stopping at a deleted one), with no
current user, so a generation can resolve from a worker thread. The lookups are done when needed and nothing is copied into
the book, so moving a book or editing a folder changes the next generation. A resolve of all twelve settings and the language
costs about 1 ms with no folder and 3 ms through ten folders (measured on the test database), a generation does a handful of
them before an LLM call, so there is **no cache**; add one per generation only if the chain gets much longer or the lookups end
up in a loop over many books.

Everything that used the book's fields directly now goes through the resolver: the generation steps, the summaries, redraft,
annotate and impersonate, the story export (language), the reader, the assistant commands (`get-book-info` and the book
settings commands report the effective values) and the help assistant.

## The services

`FolderService` (`ExtendableService<Folder, FolderRepository>`, see [Services](services.md)):

| Method | |
|---|---|
| `create`, `rename`, `move`, `delete(folder, hard)`, `deleteWithContent` | Delete moves the books and folders directly in the folder to its parent (a clash of names stops it before anything moves); `deleteWithContent` soft-deletes them too. |
| `update(folder, Details)` | The folder dialog: name, description, language and the prompts of a `Map<PromptField, String>`. It writes to the **stored** folder (what the dialog does not set is kept, not the stale copy of the caller) and refuses a change of a setting while an agent works on a book below (`FolderBookLockedException`); the name and the description are free. |
| `findChildren`, `findChild`, `search`, `resolveScope` | Lists. `search` takes the same `ManuscriptFilterValues` as the book search (`inFolder`, the name, the tags), sorters on `name`, `creation`, `modification`. `resolveScope` turns "the folder, or everything below it" into the ids the repositories filter by (`FolderScope` adds the clause to the books' query and to the folders'). |
| `getAncestors`, `getPath`, `getPaths`, `findChain` | Paths are the names joined by `/` (`SEPARATOR`); `getPaths` builds all of them from the tree in memory, for the pickers and the *Folder* column. |
| `describeContent`, `countBooksByFolder` | The numbers for the delete question and the list. The counts are of the folder and everything below it, and leave out the working copies of agents. |
| `findClashes`, `transfer(Transfer, step)` | Copy and move of books and folders, see below. |

Errors: `IllegalArgumentException` for what can never be right (a blank name, a folder that is not the user's),
`IllegalStateException` for what is wrong now (the name is taken, a folder into itself), `ManuscriptLockedException` /
`FolderBookLockedException` for the agent lock. `ManuscriptService.moveToFolder(books, folder)` moves books, `save` checks that the folder
of a book is its owner's, and `findRegularInFolder`, `getFavorites` and `setFavorite` serve the lists.

### Transfers

`transfer` takes a `Transfer(books, folders, target, copy, onlyCurrentBranch, choices)` and checks **everything first**: the target
and every item belong to the user, a folder does not go into itself or below itself, every clash has a `Choice` (`RENAME`,
`SKIP` or `MERGE`, keyed by the id of the folder; `findClashes` tells which need one), and no book (also in a moved or copied
folder) is locked by an agent. Then:

- a **move** is one transaction;
- a **copy** has none. It can take long and the database has one writer, so each folder made and each book cloned
  (`BackupService.cloneManuscript(..., folder)`) is a step of its own, and the `step` callback is called after each (the
  progress bar of `OrganiseDialog`). A failure leaves the copies already made, each complete. This is the pattern of the other long
  tasks: transactions per step, never around the lot.

The result is an `Outcome(books, folders, skipped)`.

### The modification time

`Folder.modification` follows the books below it. `JpaManuscriptRepository.changed(book)` (the hook of
`JpaBaseRepository.write`, which the repositories override to tell the owner of an entity; `JpaChatMessageRepository.touchBook` does
it for the parts) calls `FolderModification.touch`, which walks the parent chain and sets the time with one `UPDATE ... WHERE id IN`.
It skips agentic working copies. `saveWithoutModification`, opening a book and moving a book change nothing; the active-leaf
change (`swipeAndSwitch`, `branchAndSwitch`) saves the book as it did, so it counts.

### Stale copies

The dialogs keep entities for as long as they are open, and a generation held its book from start to finish, so saving such a copy
wrote old values over a setting changed in the meantime (or undid a move to a folder). The writers that change one thing now call
`ManuscriptService.update(book, BookChange)`, which loads the stored book and applies the change to it. Use it instead of `save`
for a change that is yours alone.

## The lists and windows

All the code is in `ui` (the classes are `@Configurable`, the windows `@Extendable` with `@PublicAPI`, see
[Extendable classes](plugins/extendable.md)):

| Class | |
|---|---|
| `ManuscriptPart`, `PhoneManuscriptPart` | The lists of books. The grid holds `FolderBookRow`s (a folder or a book with its tags, counts and path) from `FolderBookProvider`, a lazy provider: the folders come first, the books page by page from their ids, and what is dear (tags, branch sizes, book counts) is read for one page at a time with one query each. The current folder and *Include subfolders* are in `storeState` / `restoreState` (`folder` is the uuid). The virtual **Favorites** folder is not an entity: the provider adds a first row made by `FolderBookRow.favorites(label, count)` at the top level, and the list of its books is the books of the favorite uuids (`UserSetting.favoriteBooks`). |
| `Viewer` | The reading route's list (`/view`) is the phone list without the buttons: the same `FolderBookProvider` and rows, breadcrumb, *Include subfolders* and favorites; a tap opens a folder or navigates to the book. Where it was left is **not** in the session or the workspace state but in the browser cookie `marginalia_viewer_folder` (the folder's uuid, `favorites`, or empty for the top level), written by `JavascriptLibrary.rememberViewerFolder` on every move and read back by `readViewerFolder` when the list is built; `restoreLocation` maps a gone or foreign uuid (`findForUser`) to the top level. The cookie is read in the browser, not from the request, because a push transport does not resend cookies after the handshake. The grid stays empty until the cookie arrives, so the list does not flash the top level first. |
| `FolderActions` | New, rename, delete and move to a folder, shared by both lists and Organise: it asks what the rules need and reports what the service refused. |
| `FolderDialog` | The settings of a folder. Unlike the book's dialog it saves with a button (a change may be refused). |
| `PromptSettingsForm` | The editors of the twelve text settings, shared by the book's *Prompts* tab (desktop and phone) and the folder dialog. It takes the values and the folder the empty ones inherit from (`load`), gives the values back (`values`), shows the inherited value as the placeholder and the origin as the line under the field (`LABEL_INHERITED_FROM_FOLDER`, `_FROM_USER`, `_INHERITED_DEFAULT`) and reports a change only when every template is valid. |
| `InheritedLanguageCombo` | The language box where empty means inherit; the same placeholder and origin line. |
| `OrganiseDialog`, `FolderClashDialog` | The two-pane window (the entries are marked with `MassDeleteBox`, the check box of the story's mass delete, whose one click event carries Shift for a range; the list reads itself again when the window closes) and the question of a name clash. The keys of the buttons are `ShortcutActions.ORGANISE_*` actions, registered in `ShortcutRegistry` and caught in the browser by `JavascriptLibrary.fireOrganiseKeys`; the navigation keys are fixed. |
| `MassChangeModelProtocolDialog`, `MassChangeLanguageDialog`, `MassRenameDialog`, `MultiTagDialog` | Take a list of books (or a folder, `MultiTagDialog.Kind.FOLDERS`). |
| `BookLabels` | How a book is named in the combo boxes: `Folder / Sub / Name`. Use it for any new picker of books. |

Plugins that name a changed method or variable must follow `@PublicAPI`, see [Extendable classes](plugins/extendable.md).

## Other places that know about folders

- **Backups.** `BackupServiceImpl.takeBackup` writes the optional key `folder` (the folder's uuid and the `path` from the top, each
  with name, uuid, description, language and text settings) from `findChain`; `BackupFolders.resolve` finds the folder by uuid, else
  by the name path (creating the missing levels with the saved settings), else returns null (the top level, also on any failure).
  `createBackup`, which the agents' snapshots and `cloneManuscript` share, is unchanged and writes no folder.
  `exportBackup(backup, withImages, includeInherited, progress)` rewrites the stored file on its way out through
  `BackupInheritance`: it drops `folder` and, with the option, writes the values the book inherits (from the backup's own folder
  path, then the user settings, never the built-in defaults) as the book's own. The old three-argument overload exports with the
  option on.
- **Agents.** The working copy (`agentic = true`) used to have no folder and so ignored the folders' settings. It is put in the
  folder of the original: `createAgenticCopy`, `replaceWorkingCopy` (restore of a checkpoint) and `AgentMergeServiceImpl.sync`
  call `ManuscriptService.setAgenticCopyFolder`. A merge is a diff of JSON without a folder, so it never moves the book. The lock is
  `AgentRunService.checkNotLocked`, applied by the folder service to every book below a folder that would change
  (`FolderServiceImpl.checkNoLockedBook`); a paused or stopped run does not lock.
- **Help assistant.** `HelpAssistantService.isBooksAllowed` / `setBooksAllowed` keep the opt-in. When it is on, `ask` adds
  `HelpPrompts.BOOKS` and binds `BookHelpTools` (`list_folder`, `search_books`, `get_book_settings`) to `BookHelpToolsImpl` over
  `HelpBookData` (a `BookDataSource`), which runs each call in the request of the asking user, because the functions run on worker
  threads. The data is limited (100 entries, 20 matches, shortened descriptions), the text of a story is never returned and the
  prompts are reported only as custom or built-in. See [the help assistant](services.md#the-help-assistant).
- **Statistics.** The rows of the [writing statistics](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize) hold the folder path of their book, so `FolderServiceImpl` calls `ManuscriptStatisticService.refreshFolders()` after a rename, a move, a delete (the content moves up) and every transfer of the Organise window; it reads the placements from `ManuscriptRepository.findPlacements` and `FolderRepository.findPlacements`.
- **Cleanup.** `Manuscript.folder` is `WEAK` and `Folder.parent` is `OWNED_BY`, so a purge of a folder unlinks live books and
  purges the folders below it. `CleanupServiceTest` has the cases.
- **Trash.** Only books appear in the Trash; a deleted folder does not (its books do, when deleted with it).

## Adding to the feature

- **A new inheritable setting:** field on `Manuscript` and `Folder` (extended attributes), a constant of `PromptField`, a row in
  `PromptSettingsForm`, the setter in `FolderService.Details` handling, the text in `L` and the bundles. The backup and the export
  walk `PromptField.values()`, so they pick it up.
- **A new list or picker of books:** use `BookLabels` for the names and `FolderService.getPaths` for the paths.
- **A new action in Organise:** a constant in `ShortcutActions`, an entry in `ShortcutRegistry`, a button in the row of
  `OrganiseDialog` and the key handling in `JavascriptLibrary.fireOrganiseKeys`.
- **Anything that changes what a book inherits** (moving a folder, a setting) must go through `FolderService` so the agent lock is checked.

## Tests

`FolderCrudTest` (the `OwnedCrudContract`), `FolderServiceTest`, `FolderTransferTest`, `FolderListTest`, `FolderSettingsTest`,
`SettingsInheritanceTest`, `ModificationTest` (the `modification` of books and folders), `FolderSettingsGenerationTest` (the style of a
folder reaches the request the mock server receives), `BackupFoldersTest`, `BackupExportInheritanceTest`, `AgentFoldersTest`,
`HelpAssistantBooksTest`, `CleanupServiceTest` (purge cases), and the UI tests `FolderDialogTest`, `OrganiseDialogTest`,
`OrganiseDialogFlowTest`, `BookPromptSettingsTest`, `MassChange*DialogTest`, `ManuscriptPartFoldersTest`,
`PhoneManuscriptPartFoldersTest`, `ViewerFoldersTest` (the viewer's list, with a UI that records the cookie writes), `BookLabelsTest`. See [Testing](testing.md).
