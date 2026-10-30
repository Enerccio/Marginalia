# Part revisions (version history of a part) — design notes and plan

Every version of the text of a part is kept: who wrote it (the author, a model, a plugin, an agent), when, and for a generated
text the reasoning that came with it. The author can look at the versions, compare any two, go back to one, label the ones
worth keeping and prune the rest. This file collects the decisions and the order of work. Nothing is built yet.

## Status

- ✅ **Step 1:** `MessageRevision` (extends `ExtendableEntity`, `response` fulltext), `RevisionSource`, `RevisionRow`, Flyway V22,
  `MessageRevisionRepository` / `JpaMessageRevisionRepository` (latest, list rows by projection, counts, soft and
  hard delete by manuscript / parts), `MessageRevisionService` (`record`, `findLatest`, `list`, `countFor`, deletes), wiring in
  `services-config.xml` and `persistence.xml`, the cleanup (`OWNED_ON_DELETE`) and the delete / restore paths of
  `ChatMessageServiceImpl`; `MessageRevisionCrudTest`, `MessageRevisionServiceTest`. `record` does not prune yet, and nothing
  calls it yet (step 2). The trash of the history is the plain soft delete of `OwnedService.delete`.

## Decisions (settled in discussion)

- **New entity `MessageRevision`**, apart from `ChatMessage`. Not an extended attribute of the part (the parts of a branch
  are loaded together and a story view keeps them; a history grows without bound; it is the problem V20 solved for the
  prompt) and not `message_prompts` (that entity promises *at most one per part* with a fixed shape; a history is appended,
  restored and pruned).
- **A row is a version, not a diff and not the replaced text.** Row 1 is the first text of the part, every change adds the
  *new* text as the next row, so the newest live row mirrors the part. This costs one duplicate of the current text and buys
  correct attribution: the source, the model and the reasoning always describe the text on the row. A diff is computed when
  two rows are shown.
- **Every save records a row** (no burst window). The editor autosaves on blur / tab swap (`autosaveAndSwapToMarkdown`), not
  per keystroke, and the text is small; the retention rules bound the growth. No row when the text did not change.
- **Manual snapshots with a label**, and any existing row can be given a label (*pinned*).
- **The text lives in the extended content** of the row (`MessageRevision extends ExtendableEntity`), which is gzipped by
  `ExtendableEntity` already; no separate blob helper as in `MessagePrompt`. Lists must not read it, see *Reading*.
- **Reasoning is kept only for versions a model wrote** (GENERATE, REGENERATE, REDRAFT) and for a RESTORE row that copies one;
  never for HUMAN, PLUGIN or AUTOMATIC rows (those do not use reasoning).
- **Retention:** 50 versions per part by default; manual pruning; automatic pruning set on the book, its folders, the user and the user's
  permission (always the strictest value wins); a quick *Clear history*; soft deleted versions count toward the cap until purged.
- **Initial generation is revision 1**, existing books get their rows from an app migration.
- **Copying:** a backup restore as copy keeps the history (with an *include history* option when making the backup);
  *duplicate book* starts clean.

## Model

```
message_revisions (MessageRevision extends ExtendableEntity)
  message_id    bigint        FK ChatMessage, nullable (the cleanup nulls it before it purges the part)
  source        varchar(20)   enum, see below
  isAgent       boolean       an agent / agent command did it (also set on AUTOMATIC and HUMAN-like rows an agent made)
  sourceDetail  varchar(255)  nullable, short: "model · protocol" (GENERATE/REGENERATE/REDRAFT), plugin name (PLUGIN),
                              command / agent name (AUTOMATIC, isAgent), "the version of <date and time>" (RESTORE)
  label         varchar(255)  nullable; non-null = pinned snapshot, exempt from automatic pruning
  wordCount     int
  tokenCount    bigint
  is_deleted    boolean       (OwnedEntity) soft delete = the version is in the trash; counts toward the cap until purged
  _fulltext     clob          the plain text of `response` for the search (ExtendableEntity)
  extendedContent             @ExtendedAttribute String response, @ExtendedAttribute String responseReasoning (gzipped)
  index ix_message_revisions_message (message_id, is_deleted, creation)
  index ix_message_revisions_user    (userId, is_deleted)
```

`source`:

| Source | Meaning | Reasoning kept |
|---|---|---|
| `GENERATE` | the first generation of the part | yes |
| `REGENERATE` | the part regenerated in place (`GenerationRequest.regenerate`) | yes |
| `REDRAFT` | `RedraftServiceImpl` rewrote (a passage of) the text | yes |
| `HUMAN` | the author typed or edited it | no |
| `PLUGIN` | a plugin wrote it; `sourceDetail` = plugin name | no |
| `AUTOMATIC` | a mass action: Replace, manuscript commands | no |
| `MERGE` | the part is the result of merging the next part into it; `sourceDetail` = the merged part's text start | no |
| `SPLIT` | the part was cut in two (the part that keeps the first half) | no |
| `DUPLICATE` | the part was made by *duplicate book*; `sourceDetail` = "copied from <book>" | no |
| `RESTORE` | a version put back on top (see *Restore*); `sourceDetail` = which version | copied from the restored row |
| `SNAPSHOT` | *Save version…* with a label: a copy of the current text | copied from the current version |

`isAgent` is a flag, not more sources, so an agent that edited (HUMAN-like) or ran Replace (AUTOMATIC) is captured without
doubling the enum.

- **No ordinal.** The versions of a part are ordered by `creation`, then `id` (two made in the same millisecond; the ids come
  from one sequence and grow). Nothing is numbered or renumbered, so a pruned, purged or *undeleted* version is simply in its
  place by time, and *Roll back to here* means "newer than this one" (`creation`, `id` after the chosen row). A version is named
  in the interface by its time (and its label), not by a number.
- Real columns for what the app queries, sorts or shows without opening the text: `message_id`, `source`,
  `isAgent`, `sourceDetail`, `label`, the counts, `is_deleted`. The text and the reasoning are extended attributes (no
  migration to add more; plugins can keep their own data in `attributes`, which travels with backups).
- `ChatMessage` keeps `response`, `responseReasoning` and `originalResponse` as they are (`originalResponse` is the baseline
  of `GeneratedTextHighlighter`; it stays).
- Flyway **V22** (V21 is the folders migration, already committed): table + `_SEQ`, entity in `webapp/config/persistence.xml`,
  `MessageRevisionRepository` + `JpaMessageRevisionRepository`, bean in `services-config.xml`.
- `@CleanupReference(Policy.OWNED_BY)` on `message`; deleted with the part (soft and hard), like `MessagePrompt`.
- `response` is `@Fulltextable` (the row gets the `_fulltext` column of `ExtendableEntity`, in the V22 table), the reasoning is
  not. The main search never sees revisions, see *Search*.
- Extended attributes are saved only through the service's `save` (no dirty checking): all writes go through the service.

## Reading

An `ExtendableEntity` inflates and parses its content when it is loaded, so a list of 50 versions would read 50 texts. The
history list uses a **projection query** (`select r.id, r.source, … from MessageRevision r where …`, like
`ChatMessageRepository` does for the fulltext hits, "the rest of the message, including `extendedContent`, is not read"); the
text of a version is loaded by id when it is shown, compared or restored. The count for the badge in the part toolbar is a
single `count … group by message_id` for all parts of the branch.

## Service

`MessageRevisionService extends OwnedService<MessageRevision, MessageRevisionRepository>`:

- `record(ChatMessage part, Source source, String detail, boolean agent)` — the **single write path**: appends the current text
  of the part (and, for the model sources, its reasoning) as the next row; does nothing when the text equals the newest live
  row; counts the words / tokens; then applies the automatic pruning (below). Called by `ChatMessageService` after the part
  is saved, in the same transaction.
- `ensureCurrent(part)` — if the newest live row differs from `part.getResponse()` (a plugin wrote `setResponse` + `save`
  directly, or a part from before the feature that the migration did not reach), appends the text as `PLUGIN` (unknown
  writer) so the history never silently lies. The dialog calls it when it opens.
- `search(manuscript, FulltextQuery)` and `searchBooks(manuscriptIds, FulltextQuery)` for *Include versions* (see *Search*).
- `List<RevisionRow> list(part, boolean includeTrash)` (projection), `Map<Long, Long> countFor(parts)`, `MessageRevision get(id)`.
- `snapshot(part, label)`, `label(revision, label)` (set / change / remove the label of any row).
- `restoreOnTop(manuscript, part, revision)` and `rollBackTo(manuscript, part, revision)` — see *Restore*.
- `delete(revision)` (to the trash), `undelete(revision)`, `purge(revision / part / book)` (hard delete; owner, or admin through
  Cleanup), `emptyTrash(part / book)`.
- `clear(part)` / `clear(book)` — *Clear history*, see *Pruning*.
- `copy(from, to)` for backup restore as copy; `softDeleteForManuscript`, `softDeleteForParts`, `hardDeleteForParts`, wired
  into `CleanupServiceImpl` and the part delete paths next to the `MessagePromptService` calls.

## Where it is recorded

Same transaction as the change, after `save`, by `record`.

| Site | Source |
|---|---|
| first generation completes (`InferenceStep` / `GenerateNewMessageStep` finish) | `GENERATE` (+ reasoning, model · protocol) |
| regenerate completes (`StoryGenerationListener` branch `REGENERATE`) | `REGENERATE` (+ reasoning, model · protocol) |
| redraft completes (`RedraftServiceImpl.onCompletion`) | `REDRAFT` (+ reasoning, model · protocol) |
| `ChatMessageServiceImpl.storeResponse` from the editor | `HUMAN` |
| `storeResponse` from `AgentMergeServiceImpl` and the manuscript commands (`ManuscriptWriteSupport.write`) | `AUTOMATIC`, `isAgent = true` |
| `ManuscriptReplaceServiceImpl` (mass Replace) | `AUTOMATIC` |
| `mergeIntoPrevious` | `MERGE` on the surviving part; the merged part's own history is deleted with it |
| split of a part | `SPLIT` on the part that is cut (the new part gets row 1 as `SPLIT` too) |
| a plugin through `storeResponse` with a plugin argument (see *Plugins*) | `PLUGIN` |
| restore, snapshot | `RESTORE`, `SNAPSHOT` |
| streaming chunks (`InferenceStep` appends to `response`) | not recorded: only the completed text is |
| new parts: swipes, impersonate, `addPart`, SillyTavern import | row 1 is written when the part is created (`HUMAN`, `GENERATE` for generated ones) |
| duplicate book | row 1 of each copied part, `DUPLICATE` |

Generation writes row 1 / `REGENERATE` / `REDRAFT` on completion. A cancelled or failed generation that left some generated
text records that text as the version it is (same rule as the part: whatever is stored is a version); one that produced
nothing records nothing, and the part keeps its previous version.

## Search

- **The main search ignores versions.** Every existing search (the library search over books, the in-book search, Replace,
  the manuscript search commands) keeps looking at the current `response` of the parts only: they query `messages._fulltext`,
  and revisions are in another table, so this is the default without any filter.
- **Include versions** is a separate, off by default option next to those searches:
  - library search (`findFulltextHits`): a book matches also when a live version of one of its parts matches;
  - in-book search (`findFulltexts`): a part matches also through a version; the result row says *found in the version of <date> (HUMAN,
    date)* and opens the history dialog on that version, with the hit highlighted; a part that matches both ways shows the
    current text first;
  - manuscript search commands (agent): a boolean `includeVersions`, default `false`; hits carry the id and the time of the version.
- Needs the mirror of the two repository queries on `MessageRevisionRepository` (`findFulltexts(manuscript, likePatterns)`,
  `findFulltextHits(manuscriptIds, likePatterns)`), joining `message_id` → part → manuscript, only live rows
  (`is_deleted = false`) of non-deleted parts, same `FulltextQuery.likePatterns()`. Trashed versions are not searched.
- **Replace never touches versions** (a version is history, not text of the story), with or without the option; the Replace
  preview does not offer it.
- **Cost:** `_fulltext` is a plain clob beside the gzipped content, so every row costs its plain text once more in the
  database; with a high or no limit that is the main storage price of the history (accepted). The migration fills
  `_fulltext` through the entity (the listener does it) or writes it itself if it uses bulk updates.

## Restore

Two ways, both in the history dialog, chosen per action:

1. **Restore on top** — a new `RESTORE` row is appended, a copy of the chosen version (text, and the reasoning it had), the
   part gets that text through `ChatMessageService.storeResponse` (token / word counts, annotation shift, `edited`, summary
   staleness come with it). Nothing is rewritten, the intermediate history stays.
2. **Roll back to here** — the chosen version becomes the current one: the part gets its text, and every row *newer* than it is
   soft deleted (into the trash, restorable). No new row. Meant for "I went the wrong way, forget everything after this".
   Asks for a confirmation that names how many versions go to the trash.

## Pruning

- **Cap** per part: the number of rows including soft deleted ones, until they are purged. Exempt from automatic pruning and
  from being the thing that is removed: the **first (GENERATE) version** and every **labeled** row. Exempt rows are not counted
  against the cap, so a part of ten snapshots and a cap of 20 still has room for 20 ordinary versions.
- **When a version arrives and the part is over the cap**: purge (hard delete) the oldest row in the trash, else the oldest live
  row that is not exempt, repeat until under the cap. So the cap is a real storage bound.
- **Settings** (all the same shape: max versions per part, 0 = no history at all, empty = not set = no limit from this level).
  Four levels, **the strictest (smallest) set value wins** — nearer does not override farther, so a book cannot raise what a
  folder, the user or the administrator limited:
  1. **book:** `historyMaxVersions` extended attribute of `Manuscript` (book settings);
  2. **folders:** the same attribute on `Folder`; every folder on the chain from the book's folder to the root counts (the
     chain is read by `FolderService.findChain`, by id, usable on generation threads);
  3. **user:** the default in the user settings (`UserSetting`);
  4. **permissions:** a new `PermissionType.VERSION_HISTORY` with the extended attribute `historyMaxVersions` on `Permission`
     (like `AGENTS` has its limits), set by the administrator per user. This is the admin ceiling, and it **carries the default
     of 50**: the app migration creates the permission for **every existing user, administrators included**, granted, with
     `historyMaxVersions = 50`; the administrator can change the number, clear it (= no limit from this level, i.e. unlimited
     unless a lower level limits) or take the permission away (not granted = 0, no history for that user). Unlike the other
     permissions a **missing row means granted with 50** (new users get no permission rows when they are created, so this is
     what makes the feature work for them), the permissions dialog shows the default for a user without a row. Administrators
     are not exempt from their own setting but may change it.
  - Effective value = the minimum of the set values; `ManuscriptService.resolveHistoryLimit(book)` returns it with the level it
    came from (`EffectiveHistoryLimit(value, origin BOOK / FOLDER / USER / PERMISSION, folder)`, same shape as
    `EffectiveSetting`), so the dialogs can say *limited to 20 by the folder "Sci-fi"*. It is read from the database per
    record (cheap: the book, a chain of folders, one user setting, one permission); cache per request if it shows.
  - Lowering a limit does not purge anything by itself; the next recorded version prunes down to the new value (exempt rows
    excepted). Raising it changes nothing until more versions come.
- **Manual:** *Delete version* (to the trash), *Empty trash* (part / book), *Purge* (a row), *Undelete*.
- **Clear history** (the quick prune): per part and per book, keeps the **current version** (the newest live row); it asks first
  and tells the number of rows and the freed size. Cleared rows are purged, not trashed. Labeled rows and the first version go
  too unless the dialog's *keep snapshots* box (default **on**) is kept — shout if you want them to go always.
- Admin → Cleanup purges the trash of every user like any other soft deleted entity.

## Existing data (migration)

App migration `PartRevisionMigration` (app version 6 → 7; `container-config.xml` `appVersion` 7), written like
`OriginalResponseMigration` (walks by id, one transaction per batch, idempotent: parts that already have a row are skipped):

- Part with a `modelUsed`: row 1 `GENERATE` (text = `originalResponse`, reasoning = `responseReasoning`, detail = model ·
  protocol); when `response` differs from it (the part was edited), row 2 `HUMAN` with `response`.
- Part without a `modelUsed` (typed by the author, imported): row 1 `HUMAN` with `response`.
- Note `OriginalResponseMigration` counted *all* old text as generated, so `originalResponse` of old parts is no proof that a
  model wrote it; `modelUsed` is the signal.
- Deleted parts too (they can be restored). `modification` of the part is not touched. Parts with a blank `response` get no row.
- Storage: every old part gets one text copy (gzipped), roughly the size of the story again; the migration logs the rows made.
  The migration respects the limits (a user whose effective limit is 0 gets no rows; the permissions are created before it
  runs, and an old part gets two rows at most, well under the default cap).

## UI

- A **History** action in the part menu (desktop `ManuscriptStoryPart`, phone `PhoneManuscriptStoryPart`) with the count badge.
- `PartHistoryDialog`: a list of versions, newest first (source with the agent mark, detail, time, words / tokens, label,
  trash rows greyed with a *Show deleted* toggle); the selected one shown as text and as a **diff** against the current text or
  the previous version (reuse the diff of `PromptComparison` / `GeneratedTextHighlighter`); the reasoning of a generated
  version in a collapsible block.
- Actions: *Restore on top*, *Roll back to here*, *Save version…* (label), *Label / Unlabel*, *Delete*, *Undelete*, *Copy text*,
  *Clear history*, *Empty trash*. A book-level *Clear history* / *Empty trash* in the book menu.
- An **Include versions** checkbox (off) beside the library search, the in-book search and the agent search presets; its state
  is not remembered between sessions (a search that surprises with old text is worse than one that must be asked twice).
- Settings: the cap in the book settings, the folder settings, the user settings (each shows the effective value and the level
  that sets it) and, for the administrator, in the user's permissions (`VERSION_HISTORY`).
- `L` keys under the right prefix groups in `L` and **every** bundle (EN, JP, Chinese plugin bundle); grep `LocalizationBundleEN`
  before adding (`LABEL_CLOSE`, `LABEL_DELETE`, …). CSS classes in `shared-styles.css` + `CssStyles`, scripts in
  `JavascriptLibrary`, no inline styles. The phone layout gets the dialog as a full-screen page without hover-only controls.

## Copy, backup, export

- **Backup of a book** (`ManuscriptServiceImpl.createBackup/restore`): an option *include history*; the rows go under each
  message (`revisions`: `[{source, isAgent, detail, label, created, deleted, text, reasoning}]`). Restore **as copy**
  and the full restore keep them; a backup without the key restores with no history (the migration-style row 1 is made from
  the part). *Duplicate book* does not copy them: row 1 of each copied part is made from the copied text as `DUPLICATE`.
- Published pages, the viewer and the exports (`ExportDialog`): never include revisions. SillyTavern export / import: no.
- Database backups (the SQLite file): nothing to do.

## Plugins

- `MessageRevisionService` is a service; the dialog gets `@Extendable` only if a plugin needs it (then `@PublicAPI`,
  `PublicAPISignatureTest`, `manual/developer/plugins/extendable.md`).
- Plugins that write a part's text (`reviewer`, `sidequery`, `character-tracker`, `lore-generator`, `name-generator`,
  `manga-generator`, `sillytavern-converter`, …: grep `setResponse`, `storeResponse`) should write through
  `ChatMessageService.storeResponse(..., Source.PLUGIN, "plugin name")`. Direct `setResponse` + `save` is caught by
  `ensureCurrent` (recorded as an anonymous `PLUGIN` version when the dialog opens), so nothing breaks, but the attribution is
  lost. Audit them when the service exists. `CHANGELOG.MD` mentions it (plugin-visible).
- No event on record / restore in the first version.

## Tests

- `MessageRevisionCrudTest` via `ExtendableCrudContract` (identity, update, soft / hard delete, owner isolation, extended
  attributes `response` / `responseReasoning`).
- `MessageRevisionServiceTest`: `record` per source, no row for unchanged text, reasoning only for model sources; the order is `creation`, then `id` (twenty
  versions in a row keep it), and an undeleted version is back in its place; restore on top and roll back (the newer rows trashed, the part text, annotations, `edited`); label /
  unlabel; exemption of the first and labeled rows; cap with trash counted; purge order (trash first, then oldest live);
  resolver (strictest of book / folder chain / user / permission, 0 and empty, an ungranted permission, origin reported, a
  folder can limit but a book cannot raise it); clear (part, book, keep snapshots); another user's rows invisible;
  copy; delete with the part; cleanup purge (extend `CleanupServiceTest`); `ensureCurrent` after a direct `setResponse`.
- Search: a word only in an old version is not found by the main search and Replace, is found with *Include versions* in the
  library search, the in-book search and the command (with the id and the time); trashed and purged versions are not found; a deleted
  part's versions are not found; the reasoning is not searched.
- Generation tests (`GenerationTestBase`): generate → row 1 with reasoning and model; regenerate → `REGENERATE`; redraft →
  `REDRAFT`; a cancelled generation.
- `PartRevisionMigrationTest`: old part with `modelUsed` (edited and not), typed part, blank part, deleted part, idempotent rerun.
- Backup round trip with and without history; `FlywayMigrationTest` runs V22; `BundleCompletenessTest` for the new keys;
  `PublicAPISignatureTest` if an `@Extendable` class changes.

## Open questions

None open at the moment.

Settled: *Clear history* keeps labeled snapshots and the first version by default (a box removes them); the limit is stored on
the book, the folders, the user settings and the `VERSION_HISTORY` permission, the strictest wins, the default is 50 per part,
set by the permission (created by the migration for every user, administrators too; a missing row also means 50); sharing of
books is not planned (it can be added later, copying the author from the owner in a migration); every version is fulltext indexed (the plain clob doubles the text per version; the cap bounds it, revisit if the
database size shows it); duplicate book → `DUPLICATE` source; a cancelled generation records the text when some was generated; a merge removes
the history of the merged part and shows as `MERGE` on the surviving one.

## Order of work

1. ✅ Entity, V22, repository, service core (`record`, `list` projection, `get`, `countFor`, deletes), wiring, `persistence.xml`,
   cleanup integration, `MessageRevisionCrudTest`, `MessageRevisionServiceTest` (record / delete / copy).
2. Hook `record` into all the sites of *Where it is recorded*; generation tests.
3. `PartRevisionMigration` (app 6 → 7) + test.
4. Restore (both ways), snapshot / label, trash, `ensureCurrent`.
5. Pruning: `PermissionType.VERSION_HISTORY` (+ the migration creating it with 50 for every user), the resolver, book / folder / user / permission settings, cap enforcement, Clear history, Empty trash.
6. History dialog (desktop), part menu item and badge, settings UI, localization, styles; then the phone layout.
7. Search: `_fulltext` on the row, repository queries, *Include versions* in the three searches, tests. (Can move earlier, it only
   needs step 1.)
8. Backup / restore of the history (option), `AgentCopyServiceImpl` / duplicate book behaviour.
9. Plugin audit, docs (`manual/user` page for the history, `manual/developer` page for the entity / service with `covers`,
   `python3 tools/check_dev_docs.py`, stamp in its own commit), `tools/retype_build.sh` and regenerate `docs/` (committed),
   `CHANGELOG.MD` under `[Unreleased]` (Added: part history, migration V22 and the app migration; Changed: plugins that write
   text should use `storeResponse` with a source).
