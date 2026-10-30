# Folders — design notes and plan

Groups of books ("folders", nested) whose prompt settings the books inside inherit: a *Sci-fi* folder decides the tone and
style guidelines, a *Sci-fi / Expanse* subfolder adds to them, a book overrides what it needs. This file collects the ideas, the decisions and the progress. All of it ships in **1.6.0**,
built as a series of commits (one per step of *Order of work*, each leaving the suite green). Done items are marked ✅.

## Status

- ✅ **Modification** (see below): a book's `modification` follows its parts, `saveWithoutModification` exists.
- ✅ **Data layer** (step 1 without the resolver): `Folder` entity, `Manuscript.folder`, migration V21, `FolderRepository`,
  `FolderService` (create / rename / move / path / ancestors / counts / delete moving the content up / `deleteWithContent`),
  `ManuscriptService.moveToFolder`, wiring, `FolderCrudTest` + `FolderServiceTest`. Decided while writing it:
  - every `FolderService.save` runs the rules (name trimmed, not blank, at most 255, no `/`, unique among the siblings
    case insensitively, the parent is the user's own, no cycle); a save of a folder of another user is *not* rejected (the
    `OwnedCrudContract` expects the owner to be kept), only the parent is checked;
  - `Manuscript.folder` is `@CleanupReference(WEAK)` (a purge of a folder never purges books), `Folder.parent` is `OWNED_BY`;
  - `delete` (move up) fails without moving anything when a folder inside has the name of a folder in the parent; the dialog has
    to offer the merge / rename for that (the *rename / skip / merge* of the two-pane dialog);
  - move and delete of a folder are blocked while an agent works on a book beneath it (`checkNotLocked`), rename is not;
  - `ManuscriptService.moveToFolder` is a bulk update (the book is not saved, `modification` stays), and
    `ManuscriptService.save` rejects a folder that is not the owner's.
- ✅ **Stale copies** (decided: no protected `folder`; the save sites refresh the book and write only what they touch). Audit:
  the book window parts, the mass dialogs, `StoryBackup`, the agent merge and the plugins already did. The generation steps
  (`GenerateNewMessageStep`, `InferenceStep`), `AutoAnnotateDialog` and `StoryGenerationCommands.activate` held the book for a
  long time; they now use `ManuscriptService.update(book, change)`, which loads by id (no session needed: the callbacks of a
  generation run on threads without one) and saves only the change.
- ✅ **Resolver** (step 1 complete): `PromptField` (the 12 text settings with the getter of the book, of a folder, of the user
  settings and the default), `ManuscriptService.resolve(book, field)` → `EffectiveSetting(value, origin BOOK/FOLDER/USER/DEFAULT,
  folder)`, `resolveLanguage` / `getLanguage`, `FolderService.findChain` (by id, no current user: generation threads have no
  session). The 12 getters are one-liners. `get-book-info` and the book settings commands report effective values.
  `SettingsInheritanceTest` (every field, nearest wins, blank skipped, book wins, live change, deleted folder, language) and
  `FolderSettingsGenerationTest` (the folder style reaches the system prompt the mock model gets).
  - Not done on purpose, for the UI steps: the 5 UI places that read `manuscript.getLanguage()` for the `lang` attribute and the
    export (`ExportDialog`, both story parts, `Viewer`) still use the book's own language (English when none); they switch to
    `getLanguage(book)` with the language combo that gets an *inherit* entry (the form writes English when empty now).
    `MassChangeLanguageDialog` shows the book's own too.
  - The chain is read from the database per lookup (a generation asks ~12 times, a few queries each); if it shows, cache the
    chain per generation in the controller.
- ✅ **Step 2, the list** (desktop): breadcrumb, folder rows above the books, *include subfolders*, *Folder* column, counts (folders: books beneath;
  books: words / tokens of the active branch, one recursive-CTE query per page), new / rename / delete folder (move up or delete all with a
  second confirmation), *Move to folder* picker for a book, new book / imports in the current folder, clone next to its original, folder
  tags (*Tag folders*), the mass dialogs and the tag dialog scoped to the current folder (+ checkbox), favorites (star, virtual folder, stored
  in `UserSetting.favoriteBooks`), `BookLabels` for the three book combos, state in `storeState` / `restoreState`. Tests: `FolderListTest`,
  `ManuscriptPartFoldersTest`, `BookLabelsTest`, `MultiTagDialogTest`.
- ✅ `FolderService.transfer` / `findClashes` (copy and move of books and folders, rename / skip / merge, checks before anything changes, locked
  books refuse): `FolderTransferTest`.
- 🟡 **Organise dialog**: structure only (two panes, loading, Left/Right, Enter, Backspace, Insert, double click). Substeps, each a commit:
  - ✅ **O1 Keys and marks.** The keys are caught in the browser (`JavascriptLibrary.fireOrganiseKeys`, capture phase, `organise-key`
    event), so Left / Right switch the pane instead of moving between the grid's cells, and Enter opens the folder (like a double click)
    instead of toggling the checkbox cell. Up / Down are the grid's own, from row to row, the row under the cursor is highlighted
    (`ORGANISE_CURSOR_ROW`). Space / Insert mark, Shift+Up / Down mark the row they leave, the entry above (`..`) is never marked.
    `OrganiseDialogTest` covers the keys through `press(key)`. `Entry` has identity `equals` / `hashCode` (a lazy parent proxy broke the
    hash). Still open from the old O1: the pre-selection of the main list's selected row (the main grid has no multi-select, `selection()` =
    marked entries or else the cursor row comes with O2) and the cursor staying on the folder we came from after Backspace.
  - ✅ **O2 Copy / move (F5 / F6).** Build `FolderService.Transfer` from the active pane's selection and the other pane's folder. Flow: refuse
    when source and target are the same folder → `findClashes` → one dialog per clash (*rename / skip / merge*, *apply to all*) → for a
    copy with books ask *only current branch* (default on) once → `transfer` → reload both panes, notify with `Outcome`. A locked book or
    folder (`AgentLockCheck`) shows the error naming the book. Buttons show their keys.
    Done: `OrganiseDialog.transfer` (row of buttons showing their keys; the keys are the actions `organise.copy` / `organise.move` of `ShortcutRegistry`, default `c` / `m`, rebindable in the key bindings, caught in `fireOrganiseKeys`; O3 adds its buttons the same way), `FolderClashDialog` (rename with a
    free `name (2)` suggestion / skip / merge, *apply to all* gives the other renames a free name of their own), the copy confirmation carries *only current
    branch*; `selection()` = marked entries or else the cursor row (never `..`). A locked book names the agent holding it (the message of
    `ManuscriptLockedException` / `AgentLockCheck`), not the book. After a transfer both panes reload and keep their cursor row.
  - ✅ **O3 New folder / rename / delete.** Reuse `FolderActions.create/rename/delete` on the focused pane's folder / entry,
    refresh the pane (and the other when it showed the changed folder).
    Done: actions `organise.newFolder` (`n`), `organise.rename` (`Shift+r`, plain `r` is the story's regenerate), `organise.delete` (`Delete`) in the button row and the key bindings. They work on the
    entry under the cursor (not on the marks); a book is renamed through `ManuscriptService.update` and deleted after a confirmation, both after the `AgentLockCheck`. Both panes load
    again and the cursor of the active one stays on the made / renamed entry (or on its row number after a delete).
  - ✅ **O4 Mass dialogs from the dialog.** Buttons for model / protocol, language and tag books, scoped to the selection or to the folder of the
    focused pane (+ *include subfolders*). Needs the scope parameter on the three dialogs (see list: scoped to the current folder already; add
    the explicit-selection variant).
    Done: buttons / actions `organise.modelProtocol` (`p`), `organise.language` (`l`), `organise.tagBooks` (`t`); scope = the books marked in the active pane (the cursor row doesn't count), otherwise the folder of
    the active pane. The three dialogs got a constructor with a list of books (listed again by uuid, deleted ones dropped; no *Include subfolders* then). Tagging folders is not offered here.
  - ✅ **O5 Advanced search.** Button opens `ManuscriptSearchDialog` with a folder scope (+ subfolders); a hit has *show in folder* (moves the
    focused pane there and marks the book). Scope parameter on the search first.
    Done: action `organise.search` (`f`); `ManuscriptSearchDialog(folder, onOpen, onShow)` limits the results to the folder (+ *Include subfolders*) by listing the books with `findRegularInFolder`, the
    result button *Show in folder* closes the search and `Pane.show(book)` goes to the book's folder, marks it and puts the cursor on it. No open button there (O6 covers the list).
  - ✅ **O6 Show in list.** Button closes the dialog and opens the focused pane's folder in the main `ManuscriptPart` (callback, no
    cross-class reach into the part).
    Done: action `organise.showInList` (`o`, label *Show folder in the list*); `OrganiseDialog.setOnShowInList(Consumer<Folder>)`, `ManuscriptPart` passes `openFolder`.
  - ✅ **O7 Persistence and entry point.** Last used target remembered (`storeState`); *Organise* header button opens left = current folder,
    right = its parent or the last target.
    Done: `OrganiseDialog.setOnTarget` reports the folder of the right pane when the window closes; `ManuscriptPart` keeps it (`organiseTarget`, state key `organiseTarget`, `top` for the top level) and uses it for the
    right pane unless it is the current folder or gone (then the parent).
  - ✅ **O8 `OrganiseDialogTest`.** Copy, move, clash resolutions (rename / skip / merge, apply to all), locked book, new / rename / delete,
    selection from keys where the Vaadin testing setup allows it, otherwise through the dialog's methods.
    Done: `OrganiseDialogTest` (structure, keys, marks, transfers, new / rename / delete, mass changes, search, show in list, target) and `OrganiseDialogFlowTest` (the questions: clash rename / skip / merge, *apply to all*,
    cancel, the go-on of a copy with the current-branch box, a locked book). A dialog opened in a test is not in the UI, so the window keeps its last question (`lastQuestion()`) and the test presses its buttons. The
    name and confirm dialogs of `FolderActions` and the progress bar of a copy are not driven (they are the list's, or need a worker thread).
- ✅ **Phone list** (`PhoneManuscriptPart`). Substeps:
  - ✅ **P1 Navigation.** Breadcrumb, folder rows above the books (same `FolderService` queries as the desktop list), counts, *include subfolders*
    toggle with the *Folder* column, `storeState` / `restoreState` of the current folder.
    Done: breadcrumb (`updateBreadcrumb`), *Include subfolders*, the folder rows above the books in `FolderBookProvider` (the provider of the desktop list; a folder card shows its book count, tapping it goes in), the folder in `storeState` / `restoreState`.
  - ✅ **P2 Filters.** Name / tag filter on books and folders, scoped as on the desktop.
    Done: the name / tag filters are scoped to the current folder (+ subfolders) like the desktop ones; the list sorter buttons order the folders and books alike.
  - ✅ **P3 Quick move.** Row action opening a folder picker (`ManuscriptService.moveToFolder`); no folder editing, no two-pane dialog.
    Done: a book card has a *Move to folder* button (`FolderActions.moveBooks`, after the `AgentLockCheck`). A folder card has no buttons. The `ListSelectDialog` of the picker had a fixed 200px height that clipped its buttons: fixed (height follows the content).
  - ✅ **P4 Favorites.** Star toggle in the row, the virtual *Favorites* folder at the top level.
    Done: a star button on the book card, the virtual *Favorites* folder row at the top level, which disables *Add book* while open.
  - ✅ **P5 Start in the current folder.** New book / imports on the phone use the current folder.
    Done: *Add book* and both imports use `NewBookAction(opener, currentFolder)`.
  - ✅ **P6 Tests.** Extend `FolderListTest` or add a phone twin; `PublicAPISignatureTest` for the touched `@Extendable` classes.
    Done: `PhoneManuscriptPartFoldersTest` (12 tests: structure, tap in / breadcrumb out, subfolders, filters scoped, state, gone folder, buttons of a book card, favorites, move); `PublicAPISignatureTest` passes. The new book itself and the move picker dialog are not driven (dialogs of a test are not in the UI); the phone layout is not seen in a browser.
- ✅ **Viewer** (`/view`). The list is the phone list without buttons (`FolderBookProvider`: folders, Favorites, breadcrumb, *Include subfolders*, filters in the folder). The place it was left at is kept in the browser cookie `marginalia_viewer_folder` (written / read by `JavascriptLibrary.rememberViewerFolder` / `readViewerFolder`), not in the session: the way back from a book, a reload and the next visit open the same folder; a gone or foreign folder is the top level. Done: `ViewerFoldersTest` (7), `PublicAPISignatureTest`; the flow `shoot-folders` (section `viewer`) goes into a book and back and visits again; pictures `viewer-list`, `viewer-folder`.
- ✅ **Step 3, folder settings and "inherited from"**:
  - ✅ **S1 Reusable settings form.** Extract the field editors of `ManuscriptPromptPart` / `ManuscriptInfoPart` (template, pov, tense, style, the
    prompts, language) into a form usable for a `Manuscript` and a `Folder`, each field with the inherited value as placeholder and a label
    *inherited from X* (`ManuscriptService.resolve` → `EffectiveSetting.origin/folder`).
    Done: `PromptSettingsForm` (ui.widgets) holds the 12 text settings (`PromptField`), the same for a book and a folder, with the placeholder from the new `ManuscriptService.resolveInherited(folder, field)` and the line *Inherited from ...* under an empty field; the validation of the templates moved into it. The language is not in it: it comes with S4 (the combo is in `ManuscriptInfoPart`).
  - ✅ **S2 Folder dialog.** Name, description, tags (`MultiTagDialog.Kind.FOLDERS`) and the S1 form; opened from the list (row button) and from the
    two-pane dialog. Blocked with the book named when a book beneath is locked; rename is not.
    Done: `FolderDialog` (saved with a button through `FolderService.update`), opened by the cog button of a folder row of the list and by the *Folder settings* button / key `s` of the Organise window (`ORGANISE_FOLDER_SETTINGS`). The tags are the `TagMultiComboBox` of the book window (saved as picked). A locked book below refuses a changed setting with `FolderBookLockedException` (the message names the book); a name or description change goes through. Desktop only, the phone edits no folders.
  - ✅ **S3 Book dialog.** Same placeholders and origin labels in the book's settings (desktop and phone twins).
    Done: `ManuscriptPromptPart` and `PhoneManuscriptPromptPart` use the form; the folder is the one of the book. Tests: `BookPromptSettingsTest`, `FolderDialogTest`, `FolderSettingsTest`. Not seen in a browser; the folder window's layout on a phone is not driven (a phone has no folder rows to open it from).
  - ✅ **S4 Language.** *Inherit* entry in the language combo (empty = inherit), the 5 UI places (`ExportDialog`, both story parts, `Viewer`,
    `MassChangeLanguageDialog`) switch to `getLanguage(book)`.
    Done: `InheritedLanguageCombo` (a clear button is the *inherit* choice, the placeholder and the line under it say what is inherited and from where) in both info tabs and in the folder window (`FolderService.Details.language`; a changed language is refused while an agent works on a book below, like the prompts); the five places use `ManuscriptService.getLanguage(book)`; a book without a language no longer turns into English when the info tab saves.
  - ✅ **S5 Tests and plugin check.** Dialog test (set, clear → inherit, origin label), `PublicAPISignatureTest`, grep the plugins for the old
    getters and `ManuscriptPart` locals.
    Done: `FolderDialogTest`, `BookPromptSettingsTest` (set, clear → inherit, the origin line, desktop and phone), `FolderSettingsTest`, `MassChangeLanguageDialogTest` (a book shows the language of its folder); `PublicAPISignatureTest` passes; the plugins read none of the changed getters or locals (the reviewer reads `protocolCombo` of the info tabs, which is unchanged). The export and the reader are not driven (the language code goes into the file / the page). Full suite run once: green except the key-binding uniqueness test, which found `s` taken by a story menu action, so the key is `i`.
- ✅ **Step 4, the rest**:
  - ✅ **R1 Folder modification bump.** `JpaManuscriptRepository.changed` and `touchBook` bump the ancestor folders (`WHERE id IN (:ancestorIds)`);
    decide `saveWithoutModification` for `swipeAndSwitch` / `branchAndSwitch` / active leaf. Test in `ModificationTest`.
    Done: `FolderModification` (repository.impl) walks the chain of the folder and sets `modification` with one bulk update (`WHERE id IN :ids`); `JpaManuscriptRepository.changed` calls it with the folder of the book, `JpaChatMessageRepository.touchBook` with the folder read from the book. Decision: `swipeAndSwitch` / `branchAndSwitch` / the active leaf keep saving the book with its modification (they did), so they bump the folders too; `saveWithoutModification`, opening a book and moving it do not. A move does not touch the folders it leaves and enters. Tests in `ModificationTest` (6 new).
  - ✅ **R2 Branch counts.** One repository query (recursive CTE from `activeLeaf`, sum of `wordCount` / `tokenCount`) if the per-page query
    of the list is not already it; check it on a long book.
    Done: nothing to build, `ChatMessageRepository.sumActiveBranches` (used by `getActiveBranchSizes`, one query per page) already is that CTE. Checked on a chain of 1500 parts (`FolderListTest.theBranchSizeOfALongBookIsOneQuery`); only its correctness, no timing.
  - ✅ **R3 Backup / restore.** Folders go with the books in `BackupServiceImpl`; restore resolves by uuid, else by name path (created with the
    backup's folder settings), else top level; a clone stays in the folder of the original. Round trip test. Also the folder-specific manuscript
    copies already in the backup logic: check them against this.
    Done: `takeBackup` writes the optional key `folder` (uuid of the folder, `path` = the folders top first with name, uuid, description, language, text settings) from `FolderService.findChain`; `createFromBackup` (restore as new, clone of a backup) resolves it in `BackupFolders.resolve` (uuid, else name path with missing levels made through `create` + `update`, else top level; a failure to make a folder leaves the book at the top level) and moves the new book. `applyBackup` leaves the book where it is. `createBackup` itself is unchanged, so the agent snapshots and `cloneManuscript` carry no folder (the clone is put into the folder of the original or the given one by `cloneManuscript(..., folder)`, which was checked and now has a test). Tests: `BackupFoldersTest` (12). Folder tags are not in the backup.
  - ✅ **R4 Agents.** `AgentCopyServiceImpl` working copy keeps the folder pointer (or resolves against the original); `AgentMergeServiceImpl` never
    changes the original's folder. Tests with the agent test base; this also covers *move / delete blocked by an agent's book*.
    Done: the working copy had no folder, so it resolved its settings without the folders (a bug); it is now put into the folder of the book: `createAgenticCopy` (start, resume of a run without a copy), `AgentRunServiceImpl.replaceWorkingCopy` (checkpoint restore) and `AgentMergeServiceImpl.sync` (the book was moved while paused) call the new `ManuscriptService.setAgenticCopyFolder`. A copy is not content of the folder: `FolderModification` and `JpaManuscriptRepository.changed` skip it (it was already left out of the counts and the lock check). `AgentCopyServiceImpl` only reads the copy, nothing to change; a merge is a JSON diff that has no folder, so it never moves the book. Tests: `AgentFoldersTest` (9: copy in the folder, settings through it, top level, not content, locks of move / delete / settings but not rename, paused lets go, follows a move, merge keeps the move, replace from a snapshot).
  - ✅ **R5 Export / import of one book.** *Include inherited settings* checkbox (default on) in `ExportDialog`, written as the book's own values;
    the folder pointer is never exported; archive import starts in the current folder.
    Done: the checkbox is not in `ExportDialog` (it exports the text of the branch, which has no settings) but in the **Backups** tab, desktop and phone, where a book is exported with its settings: `BackupService.exportBackup(backup, withImages, includeInherited, progress)` rewrites the stored backup on its way out (`BackupInheritance`: drops the `folder` key, and with the option writes the language / template / pov / tense / style / prompts the book does not set itself from the folders of the backup, nearest first, then the user settings; built-in defaults stay unset). The stored backup keeps the folder (R3). Archive import (zip of `.md`) already starts in the current folder (P5); the import of a backup file in the user part has no current folder, the book goes to the top level. Tests: `BackupExportInheritanceTest` (7).
  - ✅ **R6 Assistant.** Read-only commands: list / navigate folders, search books, effective settings with source. Opt-in checkbox in the
    popover (`WorkspaceLLMSupport`, `LLMInteract`, `UserSetting`) with the notice that titles and descriptions are sent.
    Done: the "assistant" is the help assistant (`HelpAssistantService`), not the agents. Checkbox in its panel (`HelpAssistantWidget`), kept in `UserSetting.helpAssistantBooks`; when on, `ask` adds `BookHelpTools` (`list_folder`, `search_books`, `get_book_settings`) over `HelpBookData`, run in the request of the user like the admin tools, plus the `HelpPrompts.BOOKS` paragraph. The model gets folder names, titles, descriptions and the short settings (pov, tense, style, language) with their origin, prompts only as custom / built-in with their origin, and a search says where words were found, never the text. Tests: `HelpAssistantBooksTest` (7), a checkbox test in `HelpAssistantWidgetTest`. Not seen in a browser.
  - ✅ **R7 Cleanup.** `CleanupServiceTest` fixture: purge of a folder with books beneath it.
    Done: three tests in `CleanupServiceTest`: a folder deleted with its content takes its subfolders, books, stories and tag links (the tag stays); a folder deleted with the content moved up is purged alone; a live book in a purged folder (made by hand, the services never do it) is only unlinked (`Manuscript.folder` is `WEAK`) and the folders below go with the purged one (`Folder.parent` is `OWNED_BY`).
  - ✅ **R8 Chain cache.** Only if profiling shows the per-lookup chain reads; cache per generation in the controller.
    Done: measured, not built. Resolving all 12 settings and the language of a book takes 0.8 ms with no folder, 1.5 ms through 3 folders, 2.0 ms through 6 and 3.4 ms through 10 (warm, SQLite, tests); a generation does a handful of lookups before an LLM call that takes seconds, so the chain reads cost nothing worth a cache (which would also have to be invalidated by every folder edit). Look again only if folders get much deeper or the lookups end up in a loop over many books.
- ⬜ **Docs and release notes** (last, once the UI is stable):
  - ✅ **D1 User manual.** Folders and inheritance page, books page, book settings page; picture added to `TODO.IMAGES.md` and the screenshot flow.
    Done: new `manual/user/books/folders.md` (moving around, folder settings and where a book's settings come from, language, delete, Organise, copying and name clashes, mass changes, favorites, tags); changed `managing-books.md`, `prompts.md` (*Inherited settings*), `backups.md`, `story-editor.md` (minimize, default layout, mass delete), `agents.md`, `phone.md`, `administration/help-assistant.md` (*Your books*), `account.md`, `books/index.md`, `index.md`. Pictures: the flow `screenshots/flows/shoot-folders.mjs`, the rest retaken with `bin/shoot-all.sh`.
  - ✅ **D2 Developer manual.** Settings resolution, data model, services; `python3 tools/check_dev_docs.py -v`, update the flagged pages, then
    `--stamp <page.md>` each in a commit of its own.
  - ✅ **D3 Changelog.** `CHANGELOG.MD`: Changed (settings resolve through folders), the `@PublicAPI` changes plugins can see.
  - ✅ **D4 Site.** `tools/retype_build.sh`, commit `docs/` with the manual change.
  - ✅ **D5 Final check.** Full suite once (foreground, `mvn test 2>&1 | tail -40`), `BundleCompletenessTest` for EN / JP / CN keys. Done: 3614 tests, one failure (`ManualServiceTest.linksBetweenPagesPointToExistingPages`: a link of the Converter page whose text wrapped over two lines is not rewritten by `ManualParser`, which works line by line); the text is on one line now and the class passes. `BundleCompletenessTest` passed.

## Decided

- **A `Folder` entity, not a path string on the book.** A string would do for plain organising, but folders carry settings
  (prompts, style...), so they need an identity (uuid for `storeState` and backups), empty folders, a cheap rename and a
  place for the settings. Tags marked as folders (the SillyTavern way) were rejected: a book has many tags, so its
  folder, its place in a tree and the inheritance would all be ambiguous; tags are flat and shared with other object types.
- **`Manuscript.folder` is a real column** (nullable `ManyToOne`, null = top level), because the list filters on it in SQL
  (`ManuscriptRepository.searchManuscripts`).
- **Inheritance is live**, resolved when it is needed (generation, summary, redraft...), not copied into the book when it is
  created. Moving a book to another folder changes how it behaves.
- **Blank means inherit**, the same rule the book already has towards `UserSetting`.
- **Nearest wins**: book, then its folder, then the parents up to the top, then `UserSetting`, then `Defaults`. A value replaces
  the inherited one, it is not appended.
- **Which fields a folder sets:** the text/prompt fields only (template, pov, tense, style, the prompts, language). No AI /
  protocol, no lorebook (the mass change dialogs cover the model; a series lorebook is a separate design).
- **Combining: replace, nearest wins.** No append; `{{inherited}}` stays a possible later addition.
- **Deleting a folder offers two options, default *move everything up to the parent*:** the other is *delete everything
  nested* (soft delete of the books and subfolders), behind a second, explicit confirmation that names the number of books
  and folders. Books an agent works on (`AgentLockCheck`) block the delete-all.
- **Folders have tags** (tags are generic over `BaseEntity` + class in `TagRelationService`, so a `Folder` needs a
  `MultiTagDialog.Kind`, no schema change for the relation). See the open question on whether they nest.
- **Tags do not nest:** a book matches a tag filter only through its own tags; folders have their own.
- **Folders and books share one grid.** Folders are a separate query (not part of the id based book provider), always
  listed on top, books below; the row knows its type and the UI renders icon, name, tags, dates and the buttons by it.
  Filters (name, tag) apply to both queries. A change of a book bumps `modification` of all its ancestor
  folders, so the *Last modified* column of a folder is meaningful and sorts like the books. **The bump becomes automatic**
  (see *Modification*).
- **Total-Commander-style two-pane dialog** for moving and copying books and folders (copy a folder = deep copy of the
  tree with its books; move a folder into another = reparent). It replaces separate *clone folder* and *move folder*
  actions; single-book *Move to folder* may still get a quick picker.
- **Copying a book** asks what *Clone book* asks (only the current branch, default on), once per operation.
- **Names:** only folder names are unique per parent (case insensitive, no `/`); book names may repeat, so books never
  clash. A clash of folders in a copy/move asks *rename / skip / merge* with *apply to all*; merging puts the books and
  subfolders of the source into the existing folder (subfolders of the same name merge recursively). Books are never
  merged or overwritten.
- **Books an agent works on (`AgentLockCheck`) block everything:** copy, move and delete of the book, and of any folder
  that contains one (the book may inherit settings from the folder, so even a move changes what the agent sees).
- **Locked books and folder edits:** the settings of a folder cannot be edited while a book beneath it is locked (the dialog
  names the book); a rename is allowed.
- **Single book export** asks *include inherited settings* (checkbox, default on: writes the resolved values as the book's own
  into the archive). The folder pointer itself is never exported.
- **Phone:** navigate (breadcrumb, folders), filter, open books, a quick picker to move one book. No folder editing, folder
  settings, two-pane dialog or mass actions on the phone.
- **Book combos:** the item label stays a single string `Folder / Sub / Name` (top-level books just `Name`), which is what the
  closed field shows and what typing filters on; the dropdown items use a renderer with two lines (small path above, larger
  name below). One shared label generator + renderer for all book combos.
- **The two-pane dialog also has** *new folder*, *rename*, *delete* (F7 / F2 / F8 style, on the focused pane) and buttons that open
  the mass change dialogs (model/protocol, language) and the mass tag dialog for the selection or the folder of the
  focused pane. All three therefore get a scope (a folder + *include subfolders*, or an explicit selection); `MultiTagDialog`
  (`Kind.BOOKS`) is rewritten the same way and gets a folder kind. The two-pane dialog has the advanced book search too
  (`ManuscriptSearchDialog`, with the search in the parts of the story); a hit has *show in folder*, which moves the focused
  pane there. The search takes a folder scope (+ subfolders). Also have a button to show folder in view that will close the dialog
  and open the folder in the main View.
- **Keyboard in the two-pane dialog (decided: yes, if it works):** Left / Right switch the active pane, Up / Down move the
  cursor in it, Enter opens the folder under the cursor, Backspace goes up one level, Space / Insert (and Shift+arrows)
  mark entries, F5 copy, F6 move, F7 new folder, F8 delete, F2 rename, Esc closes. Shown on the buttons like the menus of the story
  show their keys. (Up / Down "move in" was read as moving the cursor; Enter goes into a folder. Say so if it was meant
  differently.) Keys must not fire while a text field (rename, filter) has focus. Phone has no dialog, so no keys there.
- **Columns:** a folder row shows the **total number of books beneath it** (all nested levels, agent working copies not
  counted); a book row shows the **word count and tokens of its active branch** (`ChatMessageService.getBranchWordCount` /
  `getBranchTokenCount`, summed from `ChatMessage.wordCount/tokenCount`). Both are computed lazily for the visible rows
  only, like the tags column. Not sortable in v1 (they are not columns of the manuscript).
- **No drag and drop in v1.**
- **Restore / clone of a backup:** the folder is resolved like the AI and the lorebook: same uuid, else the same path by
  names (created if missing, with the folder settings from the backup), else the top level.
- **Book assistant (LLM commands):** read-only folder support: list and navigate folders, search books (the
  fulltext search of name / description / parts, so *"where is my book about true lovers?"* works), and report the
  *effective* settings with their source (`BookSettingsCommands`). Folder navigation and search are an opt-in checkbox in the popover that opens the
  assistant, remembered in the workspace settings so it is not set every time (the model then sees titles and descriptions of
  all books; the manual says so). Find that popover and the workspace setting first (`WorkspaceLLMSupport`, `LLMInteract`,
  `UserSetting`). No create/move/edit of
  folders by the LLM in v1.
- **A folder has a description** (`@ExtendedAttribute` + `@Fulltextable`, like `Manuscript.description`), shown in its dialog and
  searchable (also by the assistant when it is opted in).
- **Folder rows follow the grid's active sorters** (name / created / modified), always above the books.
- **Entry point:** header button *Organise* opens the two-pane dialog, left pane on the current folder, right pane on its parent
  (or the last used target). The main grid has **no multi-select**: selection lives in the dialog (the grid's selected row, if
  any, is pre-selected in the left pane).
- **The list has the *include subfolders* checkbox** (already above) for finding a book quickly in a deep tree: checked, the
  list shows the books *and* the folders of the whole subtree below the current folder (the top level: everything), each
  with its path in a *Folder* column, and the name / tag filters work on all of them. Unchecked: the current folder only.
- **Favorites.** A book can be marked as a favorite (a star button in its row). The marks are kept in the **workspace
  state** (uuids of the books, in the user settings with the rest of it, so they follow the user to every device and browser);
  a uuid of a book that is gone is dropped on restore. While at least one favorite exists, the top level shows a virtual
  folder **Favorites** (star icon, count) above the real folders. Opening it lists the favorite books flat, with their path
  in the *Folder* column; the filters and sorters work in it, new book / new folder do not (nothing is created there). It
  is a view, not a folder: it has no entity, no settings, it is not in the two-pane dialog tree and nothing inherits from
  it. The phone shows it too and can toggle the star.
- **Include-subfolders state is per list**, kept in the workspace state like the other filters; each mass dialog has its
  own checkbox, default off.
- **Search / filter:** a toggle *include subfolders* next to the filters (default off). Without the toggle the list is the
  current folder only; with it, the books of the whole subtree (at the top level: every book) with a *Folder* column.
- **Mass change dialogs (model/protocol, language, tag books) list only the books of the current folder**, with the same
  *include subfolders* checkbox (at the top level with it checked: all books). They now take `findAllRegularForUser()`.

## Where the inheritance goes

`ManuscriptServiceImpl.getMasterTemplate/getPov/getTense/getStyle/getUserPrompt/getSummaryPrompt/getMetaSummaryPrompt/
getRedraftSystemPrompt/getRedraftPrompt/getAnnotateSystemPrompt/getAnnotatePrompt/getImpersonatePrompt` already resolve
book → `UserSetting` → `Defaults`, each as its own copy of the same method. The folder chain goes between the book and
`UserSetting`. Do not add a folder step to 12 copies: first replace them with one resolver (an enum of the fields, with the
getter on `Manuscript`, `Folder` and `UserSetting` for each) and make the getters one-liners.

## Modification

**Done (before the folders):** a book's `modification` follows its parts by itself. `JpaBaseRepository.changed` is called after a
save (not after `saveWithoutModification`) and before a hard delete; `JpaChatMessageRepository` overrides it and sets the
`modification` of the part's book with a bulk update (no dirty state, no extended content touched). The bulk statements of that
repository (re-parenting, soft delete by ids) do the same. `saveWithoutModification` (repository + services) saves an entity
as it is. Summaries, annotations, prompts of a part and opening a book are auxiliary and do **not** count: only parts change a
book. Test: `crud/ModificationTest`.

**Still to do with the folders:** the same hook one level up. A change of a book (its parts, and a save of the book itself) bumps
the chain of its folders: `JpaManuscriptRepository.changed` and the bulk update in `touchBook` take the ancestor ids
(`WHERE id IN (:ancestorIds)`, ancestors from the folder tree in memory or a recursive CTE). Candidates for
`saveWithoutModification` (navigation, not content): `ChatMessageServiceImpl.swipeAndSwitch` / `branchAndSwitch`, the active leaf
set by the tree and story views. Decide per caller when the folder bump exists.

## Open questions

- **Which fields can a folder set?** First version: the text fields above plus `language`, `pov`, `tense`, `style`,
  `template`. Not AI / protocol (a model is a per-book decision, mass change exists for it). Lorebook is a relation and a
  series wide "world" lorebook probably wants several lorebooks active at once, so later and separately.
- **Append instead of replace?** A folder style plus a small addition in the book. Not in the first version; if wanted
  later, an explicit marker in the text (a macro such as `{{inherited}}`) is better than a flag per field.
- **Show where a value comes from.** The settings form of a book shows the effective value as a placeholder and says
  "inherited from *Sci-fi*". Without it nobody can tell why a book behaves the way it does. Needs the resolver to return
  the source as well as the value.
- **Is the top level a folder?** Probably not: null folder = top level, whose "folder" settings are `UserSetting`.
- **Names:** unique per parent (case insensitive), no `/` in a name (it is the separator when a path is shown).
- **Depth limit?** Probably none, but moving a folder into its own subtree is rejected.
- **Sorting:** folders by name; books keep the existing sorters. Manual order of folders is not needed.
- **Agents:** a working copy of a book (`agentic = true`) must keep the folder pointer or resolve against the original, so an
  agent sees the style the author sees. Check `AgentCopyServiceImpl` and `AgentMergeServiceImpl`.
- **Archive export / import of one book** loses what the book inherited. Either write the resolved values into the export
  or document it. Do not export the folder pointer itself (where a book is filed is a local choice).

## Backend

1. ✅ Entity `Folder` (`ExtendableEntity`: `name`, `parent`, `description`, the prompt fields as `@ExtendedAttribute`s),
   `persistence.xml`, Flyway migration V21 (`folders` table + `manuscripts.folder_id`), repository, `FolderService`(+impl) with
   the beans in `services-config.xml`.
2. ✅ The resolver (see above) and the getters through it. ⬜ Cache the chain per generation if it shows (a handful of ancestors
   at most).
3. ✅ `Manuscript.folder`, `ManuscriptService.moveToFolder`, `FolderService.move/rename/delete` with the rules above.
   ⬜ `ManuscriptFilterValues.folder` (+ "include subfolders") and the `ManuscriptRepository.searchManuscripts` predicate (needs the
   list, step 2); the folder search with its own sorters (the folder rows of the grid), the favorites view.
   The branch counts of `ChatMessageServiceImpl` load the whole branch of a book per call (`getBranchFromLeaf`): fine for
   one book, too slow for a page of rows of long books. Add one repository query (recursive CTE up from `activeLeaf`, sum of
   `wordCount` and `tokenCount`, for a list of manuscript ids) and use it for the list; the folder counts come from one
   grouped query plus the folder tree in memory.
4. ✅ Soft delete and `@CleanupReference` (admin Cleanup), ownership through `OwnedService`. ⬜ Check `CleanupServiceTest`
   fixtures once folders have books (a purge of a folder with books beneath it).
5. Backup / restore: folders go with the books (`BackupServiceImpl`); restore resolves the folder like the AI and the
   lorebook (same uuid, else same name path, else none). A cloned book stays in the folder of the original.
6. ✅ (assistant commands) / ⬜ (UI: language, see Status) Code that reads the raw field instead of the resolved getter: `BookSettingsCommands` (`pov`, `tense`, `style`),
   `GetBookInfoCommand` (`customTemplate`) and whatever `grep -rn "manuscript\.get(Pov|Style|...)"` finds in `ui.*` and in
   `plugins/*`. The tools the LLM uses must report the effective value.
7. Commands for the book assistant (`domain/service/impl/commands/...`): list / navigate folders, search books
   (opt-in), effective settings with their source. Read-only (see Decided).

## UI

1. `ManuscriptPart` (and its twin `PhoneManuscriptPart`): breadcrumb of the current folder, the direct subfolders as
   buttons above the grid (no fake rows in the id-based provider, it has multi-sort), the grid shows the books of the current
   folder. While the name or tag filter is set, search in all folders and show a *Folder* column. The current folder goes
   into `storeState` / `restoreState`.
2. Create / rename / delete folder; *Move to folder* for one book (row button) and for many (a mass dialog next to
   *Tag books* and *Mass changes*, pick an existing folder or type a path).
3. The folder settings dialog. The settings form of the book (`ManuscriptPromptPart`, `ManuscriptInfoPart` in
   `ui/dialogs/manuscript`) should be reusable for a folder, with the "inherited from" placeholders in both.
4. Book combos (`AgentDialog`, `AgentCloneDialog`, `LorebookActivationPreviewDialog`): label as `Folder / Name`. Make one
   shared label generator.
5. New book, clone, archive and SillyTavern import start in the current folder.
6. `@Extendable` classes touched: run `PublicAPISignatureTest`, set `from` to the current version, check the plugins that
   name `ManuscriptPart` or its locals. Localization: new keys at the end of their prefix group in `L` and **all**
   bundles (EN, JP, CN); reuse `LABEL_NAME`, `LABEL_CLOSE`... Styling only through `CssStyles` + `shared-styles.css`.
   JavaScript only through `JavascriptLibrary`.

## Tests

- ✅ Resolver: every field through book / folder / grandparent / `UserSetting` / `Defaults`, blank vs set, a folder
  changed after the book was created.
- ✅ Folder rules: unique names, cycle rejected, delete moves children, another user's folder is not found
  (`FolderServiceTest`), `update` keeps the folder and the settings changed since the copy was loaded.
  ⬜ Move / delete blocked by a book an agent works on (needs the agent test base).
- ✅ Generation test (`GenerationTestBase`): the style of a folder reaches the prompt the mock server receives, and a book
  override wins.
- ⬜ Backup round trip with folders; ✅ `FlywayMigrationTest` runs the migration (passes with V21); `BundleCompletenessTest`
  when strings are added.
- ✅ An `OwnedCrudContract` test for the entity (`FolderCrudTest`).

## Docs

- `manual/user`: a page on folders and inheritance (with a picture; add it to `TODO.IMAGES.md` and the screenshot flow),
  the books page, the book settings page.
- `manual/developer`: generation pipeline / settings resolution, data model; stamp the pages that
  `tools/check_dev_docs.py` flags. Then `tools/retype_build.sh`.
- `CHANGELOG.MD` under `[Unreleased]`: ✅ Added (folders, data layer, V21), ✅ Changed (*Last modified* follows the parts),
  ✅ Fixed (stale copies); ⬜ Changed (settings resolve through folders) and the `@PublicAPI` changes plugins can see, as they come.

## Order of work

1. ✅ Entity, migration, `Manuscript.folder`, services, resolver and tests. No UI, no behaviour change
   while there are no folders (the refactor alone should keep the whole existing suite green).
2. List navigation, create / rename / move / delete, combo labels, phone UI.
3. Folder settings dialog and the "inherited from" display.
4. Backup, agents, export, assistant commands, docs.
