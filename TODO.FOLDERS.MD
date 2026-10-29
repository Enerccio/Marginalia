# Folders — design notes and plan

Groups of books ("folders", nested) whose prompt settings the books inside inherit: a *Sci-fi* folder decides the tone and
style guidelines, a *Sci-fi / Expanse* subfolder adds to them, a book overrides what it needs. Nothing is implemented yet; this file
collects the ideas and the decisions before the work starts. All of it ships in **1.6.0**, built as a series of commits
(one per step of *Order of work*, each leaving the suite green).

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
  pane there. The search takes a folder scope (+ subfolders).
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

1. Entity `Folder` (`OwnedEntity`/`ExtendableEntity`: `name`, `parent`, the prompt fields as `@ExtendedAttribute`s),
   `persistence.xml`, Flyway migration (new `folders` table + `manuscripts.folder_id`; the newest is V20, reuse it if it is
   not committed), repository, `FolderService`(+impl) with the bean in `services-config.xml`.
2. The resolver (see above) and the getters through it. Load the user's folders once per generation and walk the chain in
   memory; a book has a handful of ancestors at most.
3. `Manuscript.folder` and `ManuscriptFilterValues.folder` (+ "include subfolders"?), `ManuscriptRepository` predicate,
   `ManuscriptService.move(...)`, `FolderService.move/rename/delete` with the rules above.
   The branch counts of `ChatMessageServiceImpl` load the whole branch of a book per call (`getBranchFromLeaf`): fine for
   one book, too slow for a page of rows of long books. Add one repository query (recursive CTE up from `activeLeaf`, sum of
   `wordCount` and `tokenCount`, for a list of manuscript ids) and use it for the list; the folder counts come from one
   grouped query plus the folder tree in memory.
4. Soft delete and `@CleanupReference` (admin Cleanup), ownership through `OwnedService`.
5. Backup / restore: folders go with the books (`BackupServiceImpl`); restore resolves the folder like the AI and the
   lorebook (same uuid, else same name path, else none). A cloned book stays in the folder of the original.
6. Code that reads the raw field instead of the resolved getter: `BookSettingsCommands` (`pov`, `tense`, `style`),
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

- Resolver: every field through book / folder / grandparent / `UserSetting` / `Defaults`, blank vs set, a folder
  changed after the book was created.
- Folder rules: unique names, cycle rejected, delete moves children, another user's folder is not found.
- Generation test (`GenerationTestBase`): the style of a folder reaches the prompt the mock server receives, and a book
  override wins.
- Backup round trip with folders; `FlywayMigrationTest` for the migration; `BundleCompletenessTest`.
- An `OwnedCrudContract` test for the entity.

## Docs

- `manual/user`: a page on folders and inheritance (with a picture; add it to `TODO.IMAGES.md` and the screenshot flow),
  the books page, the book settings page.
- `manual/developer`: generation pipeline / settings resolution, data model; stamp the pages that
  `tools/check_dev_docs.py` flags. Then `tools/retype_build.sh`.
- `CHANGELOG.MD` under `[Unreleased]`: Added (folders), Changed (settings resolve through folders), the migration number
  and the `@PublicAPI` changes plugins can see.

## Order of work

1. Entity, migration, `Manuscript.folder`, resolver refactor with tests. No UI, no behaviour change while there are no
   folders (the refactor alone should keep the whole existing suite green).
2. List navigation, create / rename / move / delete, combo labels, phone UI.
3. Folder settings dialog and the "inherited from" display.
4. Backup, agents, export, assistant commands, docs.
