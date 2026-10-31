# Version history

Every change of the text of a part ([`ChatMessage`](domain-model.md)) leaves a **version** behind: a `MessageRevision` with the
text, who wrote it and when. This page follows the feature through the layers: the data, the service and the places that
record, the limit and the pruning, restoring, search, backups and copies, the migrations and the windows. The user's view is
[Version history](../user/books/version-history.md).

## The data

`MessageRevision` (`domain/model/impl`, table `message_revisions`, Flyway `V22__message_revisions.sql`) is an
`ExtendableEntity`, so it has an owner, a `uuid`, a soft delete (`is_deleted`) and the extended content.

| Field | Where | |
|---|---|---|
| `message` / `messageId` | column `message_id`, `@CleanupReference(OWNED_BY)` | The part. Nullable, the cleanup nulls it before it purges the part. `messageId` is the same column read-only, for code with no session to load the part. |
| `source` | column, `RevisionSource` | `GENERATE`, `REGENERATE`, `REDRAFT`, `HUMAN`, `PLUGIN`, `AUTOMATIC`, `MERGE`, `SPLIT` (reserved, nothing splits a part yet), `DUPLICATE`, `RESTORE`, `SNAPSHOT`. `isModel()` says whether the reasoning is kept. |
| `agent` | column `is_agent` | An agent did it, whatever the source. |
| `sourceDetail` | column | A short word: "model · protocol" of a generated version, the name of an extension. What the application itself names is a `RevisionDetail` stored as `#NAME` (`RevisionDetail.parse` reads it back) and shown through `Localization.getRevisionDetail`, so it is in the language of the reader; anything else is free text shown as it is. |
| `label` | column | A version with a label is a saved version, exempt from pruning. |
| `wordCount`, `tokenCount` | columns | |
| `response`, `responseReasoning`, `restoredFrom` | `@ExtendedAttribute` | The text and the reasoning of the text (gzipped with the rest of the extended content); the uuid of the version a `RESTORE` is a copy of. `response` is `@Fulltextable`, the reasoning is not. |

- **A row is a whole version, the current one too.** The newest version that is not in the trash mirrors the text of the part. The
  part keeps its `response` (the generation pipeline and every reader use it as before), so there is one more copy of the
  current text; that is what keeps every reader of a part unchanged.
- **No ordinal.** The versions of a part are ordered by `creation`, then `id`. Nothing is renumbered when a version is pruned,
  purged or taken out of the trash, and "newer than this version" is `creation > c OR (creation = c AND id > i)`.
- **The reasoning is kept only for the versions a model wrote** (`RevisionSource.isModel()`) and for a `RESTORE` copy of one.
- **A list never reads the text.** An `ExtendableEntity` inflates its content when it is loaded, so the history list uses a
  projection query (`RevisionRow`: everything but the text and the reasoning). The text of a version is loaded by id when it
  is shown, compared, restored or searched. The badge of the part menu is one `count ... group by message_id`
  (`MessageRevisionService.countFor`) for the parts of the branch.
- The trash of the history is the plain soft delete of `OwnedService.delete`: a deleted version still counts toward the limit
  until it is purged, it shows in the user's Trash as the type *Version of a part* (`ENUM_DOMAIN_MESSAGE_REVISION`), and the
  Cleanup purges it (`MessageRevision` is in `CleanupServiceImpl.OWNED_ON_DELETE`, so a purged part takes its versions along).
- Extended attributes are saved only through the service's `save`: every write goes through `MessageRevisionService`.

`persistence.xml` lists the entity, `services-config.xml` the repository and the service. The indexes are
`(message_id, is_deleted, creation)` and `(userId, is_deleted)`.

## Recording

`MessageRevisionService.record(part, source, detail, agent)` and `record(part, RevisionData)` are the **single write path** (the
second takes the whole version: a redraft has a reasoning of its own while the part keeps the old one). It appends the text as
the next version, does nothing when the text is the one of the newest version or empty, counts the words and tokens, resolves
the limit of the book (nothing is recorded when the limit is 0), saves and prunes. It is called **in the transaction of the
change, after the part is saved**; callers do not call it themselves, they go through `ChatMessageService`, whose write
methods take a `RevisionOrigin` (source, detail, agent, reasoning; `AUTHOR` is the default of the overloads without one,
`UNRECORDED` is for the code that records the version itself, `automatic`, `human` and `model` build the common ones):
`updateResponse`, `storeResponse`, `createPart`, `insertPart`, `addPart`. The service first calls `ensureCurrent(part)` on the
state the part had, then changes it, then records.

| Site | Source |
|---|---|
| a generation completes (`CleanupStep`), also the text that arrived before a stop or an error | `GENERATE` or `REGENERATE`, "model · protocol", the reasoning of the part. A generation that produced nothing records nothing. |
| the regenerate step | `ensureCurrent` before it clears the part |
| `RedraftServiceImpl` (dialog and command) | `REDRAFT` with the reasoning the redraft got |
| the editor, `storeResponse` | `HUMAN` |
| manuscript commands (`ManuscriptWriteSupport`), `AgentMergeServiceImpl`, Replace | `AUTOMATIC` with `RevisionDetail.COMMAND`, `AGENT_MERGE`, `REPLACE` (agent flag set for the first two) |
| archive and SillyTavern import | `HUMAN` with `ARCHIVE_IMPORT` / `SILLYTAVERN_IMPORT`, `GENERATE` for a chat that has a model |
| a branch (`ChatMessageService` copy) | `DUPLICATE` with `BRANCH` |
| `mergeIntoPrevious` | `MERGE` on the surviving part; the merged part's history goes with it |
| restore, snapshot | `RESTORE`, `SNAPSHOT` |
| streaming chunks | not recorded, only the completed text |

`ensureCurrent(part)` records the text as `PLUGIN` when the newest version differs from it: an extension that wrote
`setResponse` + `save` directly, or a part from before the feature. So the history never silently lies; the dialog calls it
when it opens, and every service write calls it before it replaces the text.

## The writing statistics

The same write path is where the **words, parts and edits** of the [writing statistics](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize)
are counted (`append` calls `ManuscriptStatisticService.count` for the book of the part, in the same transaction; the arithmetic is
`StatisticCounts.ofVersion(source, previousWords, words)`). The previous version is the newest one that is not in the trash:
none means the part got its first text (a part created, its words written by a person or generated by a model, by
`RevisionSource.isModel()`); otherwise it is an edit, and the change of the word count is written / generated when positive and
removed when negative. `DUPLICATE` (a copy) and `SNAPSHOT` (the same text) are not counted, `MERGE` is an edit that adds no words
(the words were written before). `rollBackTo` makes no version, so it counts its own edit. A version is **not counted** when its
`RevisionData` / `RevisionOrigin` is `uncounted()`: the repair of `ensureCurrent` (whose words are counted again from the text, a
writer that does not record leaves a stale count on the part), `recordFirst` (the migration and the restore of a backup) and
`MERGE_ORIGIN` of the agent merge (the work was counted when it was done). Nothing is counted while the history of the book is
switched off (limit 0, no version is made, there is nothing to compare with).

**Agents.** The hidden working copy of an agent is not a book of the author. `ManuscriptStatisticService.count` resolves it: a book that is
`agentic` is looked up in the run that has it as its working copy (`AgentRunService.findOriginalOfCopy`, no session needed) and the
count goes to the **original book of the agent with `agent` = true**; a copy that no run has (it is being made or dropped) is not counted.
So the words, parts, deletes, edits and tokens of everything done on the copy (`record`, `delete`, the `UsageContext` of the run) need no
special case. Not counted: making the copy and restoring a checkpoint into a new copy (they restore from a backup, whose versions
are uncounted), dropping a copy (a delete for good), and the merge into the book (`MERGE_ORIGIN` and the `counted = false` delete are
uncounted, the work was counted live). A run that is discarded keeps its row: the agents' side is what the agents did and spent,
not what ended in the book. A book has at most one unfinished run and is locked while it holds the book (`checkNotLocked`), so the
author's side of the book and the agents' side are never written by two actors; within the agents' side the counters are SQL
increments.

The **deletes** are counted in `ChatMessageServiceImpl.delete` (also reached by `deleteNodeAndMigrateChildren`): a soft delete
counts the parts it takes (the part and its descendants that were not deleted yet) as `partsDeleted` and their `wordCount`s as
`wordsRemoved` (`ChatMessageRepository.measureLive`, one query). Deleting for good (the trash, a generation that got nothing, a
copy that is dropped), restoring from the trash and deleting a part that is deleted already count nothing. The delete of a merged-in
part (`mergeIntoPrevious`) counts the part only, its words live on in the previous one; `deleteNodeAndMigrateChildren(…, counted =
false)` keeps a delete out of the statistics, which the agent merge uses (the delete was counted when the agent did it).

The same place also tells the **size** of the book that its text may have changed (`ManuscriptSizeService.touch`, before the exit for a book whose history is off: the size is not the work, so the repair of a version and the merge of an agent, which count nothing, change it too).

## The limit and the pruning

`ManuscriptService.resolveHistoryLimit(book)` returns an `EffectiveHistoryLimit(value, origin, folder)`: the **strictest** (smallest)
value of four levels, each an `@ExtendedAttribute historyMaxVersions`:

1. the permission `VERSION_HISTORY` of the book's owner (`Permission`): a **missing row means granted with 50**
   (`EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS`), a row that is not granted means 0, a granted row without a number no limit;
2. the user settings (`UserSetting`);
3. the folders from the book's folder up to the root, farthest first (`FolderService.findChain`, by id, usable on a worker thread);
4. the book (`Manuscript`).

A nearer level that sets the same value takes the origin (so the dialog says "limited by the folder"). Negative or null is "not set",
0 is "no history". `resolveInheritedHistoryLimit(folder)` and `resolvePermissionHistoryLimit()` give what the folder dialog and
the user settings show as the inherited value (for the current user: the folder a window holds is a lazy proxy without a session, only its
id is read); `HistoryLimitField` is the field of the four settings windows (placeholder and
helper text from the origin).

`prune(part, limit)` runs after every `record`: it brings the part down to the limit, **deleting for good first the oldest version in
the trash, then the oldest one that is not**. The newest version is never removed, and the **exempt** ones are not counted: those
with a label and the first `GENERATE` version of the part. Lowering a limit purges nothing by itself; the next recorded
version prunes down to it. `clear(part|book, keepSnapshots)` and `previewClear` (what `ClearHistoryDialog` shows) delete everything but
the newest version, and the exempt ones when `keepSnapshots`; `emptyTrash` deletes the trash. They work on the parts of the
current user only (`IllegalArgumentException` for another user's book).

The limit of a book is also a setting of the book for the code that changes settings: `ManuscriptService.BookSettings.historyMaxVersions`
(null leaves it, a negative number takes it away) is what the agent command `set-book-history-limit` calls (`updateSettings`; the
result says the book's own limit, the one that counts and the level that sets it) and what the merge of an agent's work applies:
`historyMaxVersions` is one of `MergeDiff.BOOK_FIELDS` (a book field of the review, `AgentMergeServiceImpl.applyBook`, no number = the agent took the limit away), so the limit
the agent set in the copy reaches the book.

A permission is added by `VersionHistoryPermissionMigration` (app 7 → 8) for every user who has none: granted, 50.

## Restoring

Both are in `MessageRevisionService`, one transaction, and return a `PartEdit` like a `ChatMessageService` edit does (so the
caller can report the summaries the change made outdated):

- `restoreOnTop(book, part, version)` appends a `RESTORE` version, a copy of the text (and the reasoning) with `restoredFrom`,
  and sets the text of the part;
- `rollBackTo(book, part, version)` sets the text of the part to the version and soft deletes the versions newer than it
  (`softDeleteNewerThan`); they can be taken out of the trash (`undelete`).

`snapshot(book, part, label)` saves the current text as a `SNAPSHOT` with a label whether or not the newest version has the same
text; `label(version, label)` sets, changes or removes the label of any version. `snapshot` throws `IllegalStateException` for a
book that keeps no history.

## Search

The main searches read `messages._fulltext` and never see the versions. *Include versions* is a separate path:

- `MessageRevisionRepository.findFulltexts(book, likePatterns)` and `findFulltextHits(bookIds, likePatterns)` query the same
  `FulltextQuery.likePatterns()` over `message_revisions._fulltext`: the versions not in the trash of the parts that are not deleted.
- `MessageRevisionService.searchFulltext` gives one `FulltextHit` per part, its newest matching version
  (`FulltextHit.version()` is the uuid, plus `versionTime()` and `versionSource()`); `findPartsWithText` gives the parts per book.
- `ChatMessageService.searchFulltext(book, query, includeVersions)` and `findManuscriptsWithText(ids, query, allBranches,
  includeVersions)` merge them with the text hits: **the text of a part wins**, the versions add the parts that don't have the
  words any more, and the active branch rule of the library search applies to them too.
- The windows: `ManuscriptSearchCriteria.includeVersions` and the check box of `ManuscriptSearchDialog`; the check box of
  `ManuscriptTreePart` / `PhoneManuscriptTreePart`, whose version hit has a second link with the goto id
  `history:<part id>:<version uuid>` that opens `PartHistoryDialog.open(..., versionUuid)`; the agent command `search-parts`
  (`includeVersions`, the hit has a `version` object).
- Replace and `find-occurrences` never look at versions. Every version is full-text indexed, so a long history costs its plain
  text once more in the database (accepted; the limit bounds it).

## Backups and copies

A backup keeps the history only when asked: `ManuscriptService.createBackup(book, includeHistory)` and
`BackupService.takeBackup(book, includeHistory)` (the overloads without the flag, and so the automatic backups, the backup before an
agent merge and the agents' working copies, write none). `MessageRevisionService.marshalForParts` puts `revisions` under each
message: `uuid, source, agent, detail, label, created (ms), deleted, text, reasoning, restoredFrom, wordCount, tokenCount`,
oldest first, the trash too.

Restoring goes through `ManuscriptServiceImpl.restoreMessages(..., MissingHistory)`, which also reads the parts of the backup
back without events:

- a message with `revisions`: `restoreForPart` writes the versions with new uuids and the original times (`restoredFrom` is
  mapped to the new uuids), prunes to the limit of the book they come to (nothing is written when it keeps none) and calls
  `ensureCurrent`;
- a message without: by `MissingHistory`: `FIRST_VERSION` (restore, import) calls `recordFirst(part, null)`, which makes what the
  migration made (`GENERATE` with the reasoning and "model · protocol", plus `HUMAN` if the text was edited since, or `HUMAN`),
  `DUPLICATE` (`BackupServiceImpl.cloneManuscript`, a duplicate book) records a `DUPLICATE` version, `NONE`
  (`cloneFromBackup` without the argument, the working copy of an agent) records nothing until the part is changed.

The part is rebuilt from the extended content, so `restoreMessages` calls `ExtendableEntityListener.deserialize` on it before
the versions are made. Published pages, the exports and the database backup (the SQLite file) do not change.

## The migrations

`PartRevisionMigration` (app 6 → 7) makes the first versions of the parts that exist: a part with `modelUsed` gets a `GENERATE`
version of `originalResponse` (with the reasoning and "model · protocol") and a `HUMAN` one of the text it has when that is
another, any other part a `HUMAN` one; deleted parts too; a part that has versions or no text gets none, so it can be run
again. It reads in batches of 50 in one session and writes the versions of a part in another (the id generator updates its
sequence table on a second connection, and SQLite refuses the write of a transaction that already has a read snapshot).
`VersionHistoryPermissionMigration` is 7 → 8, see above. Both are registered in `container-config.xml` (`appVersion` is 9 since the [statistics backfill](database.md)).

## The windows

- `PartHistoryDialog` is one class for the desktop and the phone (`UIUtils.isPhone()`, `UIUtils.fitPhone`): a grid of
  `RevisionRow`s (a card column on a phone), the selected version shown as text or as a word diff (`WordDiffView`) against
  the previous version or the current text, the reasoning in a `Details`, and the actions *Restore on top*, *Roll back to here*
  (with a confirmation that counts the versions that go to the trash), *Label…*, *Delete* / *Take out of trash* /
  *Delete permanently*, *Copy text*, *Save version*, *Show deleted versions*, *Empty trash*, *Clear history* (`ClearHistoryDialog`
  with the preview and the check box that keeps saved versions). The `onChanged` callback gets the part after a restore or a
  roll back so the card can draw it and report the outdated summaries. Its package-private fields and `create(...)` are for
  the tests.
- `StoryCard.openHistory()` and `getHistoryCount()` are implemented by the cards of `ManuscriptStoryPart` and
  `PhoneManuscriptStoryPart`; `StoryMenuRegistry` has the card item `History (n)` (label with the count, hidden while the part is
  edited or not saved) and, in the settings menu, `BookHistoryActions` (*Clear history of the book*, *Empty trash of the history*).
- The limit is edited with `HistoryLimitField` in `ManuscriptInfoPart` / `PhoneManuscriptInfoPart`, `FolderDialog` (through
  `FolderService.Details.historyMaxVersions`) and `UserPart`; the administrator's ceiling with
  `VersionHistoryPermissionComponent` (`PermissionUIFactoryImpl`), where a permission that is added starts at 50 and the
  number is disabled while the permission is not granted.

## For extensions

- An extension that writes the text of a part should call `ChatMessageService.storeResponse(book, part, text, origin)` (or the
  other overloads) with `new RevisionOrigin(RevisionSource.PLUGIN, "<extension name>", false, null)`. One that writes with
  `setResponse` + `save` is recorded as an unknown writer before the next change.
- `@Extendable` classes gained methods (`StoryCard.openHistory`, `getHistoryCount`, the tree parts' `showInHistory` and
  `versionSnippet`); `FolderService.Details` has a fifth component (the four-argument constructor clears the folder's limit).
- A plugin that saves a backup uses `BackupService.takeBackup(book, true)` to take the history with it.

## Tests

`MessageRevisionCrudTest` (`crud`), `MessageRevisionServiceTest`, `PartHistoryRecordingTest`, `PartHistoryActionsTest`,
`PartHistoryLimitTest`, `PartHistoryBackupTest`, `VersionSearchTest` and `PartRevisionMigrationTest` (`revision`),
`VersionHistoryPermissionMigrationTest` (`db`), `RevisionLocalizationTest` (`loc`), and in `ui`: `PartHistoryDialogTest`,
`HistoryLimitSettingsTest`, `BranchSearchTest`, `ManuscriptSearchDialogTest`, `StoryMenuRegistryTest`.
