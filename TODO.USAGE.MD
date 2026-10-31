# Writing statistics (usage per book per day) — design notes and plan

What happened to a book, day by day: the tokens spent on it, the words written (by the author and by the model) and removed, the
parts made and deleted, the edits, and the **time**: how long the book was open, how long the author was editing, how long the
models generated for it and how long an agent worked on it. And the **size of the book**, in tokens and words, whole and of the
current branch, as it was at the end of each day, so that two days can be compared and the growth (or shrinkage) of a book read off.
Counts are rows per book and day; times are recorded intervals; sizes are a snapshot per book and day. What
the agents do is recorded apart (`agent` = true), against the original book although they work on a hidden copy; the display adds
the author's and the agents' side up, or shows one of them. The author gets a *Statistics* window for a book (a day-by-day grid,
totals, charts of the words, the time and the tokens per day) and one across all books (*Global statistics*: today, yesterday, the
last 7 or 30 days, a range of dates, with a breakdown by book). This file collects the decisions, the open questions and the order
of work. **Steps 1 (the data layer), 2 (words, parts, edits), 3 (deletes), 4 (tokens), 5 (agents), 6 (time), 7 (size), 8 (book data and clones), 9 (the interface) and 10 (the backfill) are built**, the user manual follows. Done items are marked ✅.

Not to be confused with `Usage` (`usages`, *Token usage per user*, `UsageDialog`): that one counts what a *user* used of an *AI*
(for the limits of shared models) and has no idea which book the tokens went to. This one is per *book* and day.

## Two kinds of numbers: work and time

- **Work** (tokens, words, parts, edits, deletes) adds up: five agents write five agents' worth of words.
- **Time** is the clock and never adds up things that ran together. It is **recorded per book as it happened** and **combined only
  when it is shown**:
  - five agents running for 24 hours on five books are recorded as 24 h on each of the five books;
  - the global display shows **24 h** of agent time (the stretch of the clock during which any agent was running), and 24 h on
    each book's own line;
  - three agents run one after another for 8 h each are recorded as 8 h on each book and shown as 24 h in total, and 8 h on each
    line.

  So the recording keeps the *intervals* (when, for how long), not a per-day number, because a number per book and day cannot be
  combined into a union afterwards and intervals can. The union is a display rule (`TimeUnion`, see *Time*), used wherever a time
  is shown for more than one book, for both sides at once (*All* = the author's and the agents' side) or for a folder.

## Data model

Three entities, all `OwnedEntity` of the owner of the book, with no foreign key to the book (see *The book is gone, the history
stays*), Flyway V23, `persistence.xml`, beans in `services-config.xml`.

**`ManuscriptStatistic`** (table `manuscript_statistics`): the counts, **two rows per book and day at most**, the author's
(`agent` = false) and the agents' (`agent` = true), see *Agents*. The counters are **real columns**, not extended attributes: they
are added up in SQL (`UPDATE ... SET x = x + :n`) so that parallel generations and an agent do not lose counts, the same way
`Usage` does it (`UsageRepository#add`, a lock around the first insert of a row), and the grid orders and sums them in SQL.

| Column | Meaning |
|---|---|
| `bookUuid` | the book, by its stable `uuid`, **not a foreign key**: the rows outlive the book |
| `bookName` | the name of the book (a copy, kept current while the book exists, the last one after it is gone) |
| `folderPath` | the path of the folder of the book (`FolderService.getPath`, folder names cannot contain `/`), empty for a book in no folder; kept current the same way |
| `day` | `date`, see *Day boundary* |
| `agent` | false: what the author did (with the models they asked); true: what the agents did on the book |
| `prefillTokens`, `generatedTokens`, `requests` | what the model calls made for this book that day cost (as in `Usage`) |
| `wordsWritten` | words added to the text by hand (positive change of the word count of a part by a source that is not a model; in the agent row, by the agent's own commands) |
| `wordsGenerated` | words added by a model (the version has a `RevisionSource.isModel()` source) |
| `wordsRemoved` | words taken away: the negative changes of the edits and the words of the deleted parts |
| `partsCreated` | parts that got their first text that day |
| `partsDeleted` | parts deleted (a part with its descendants counts each of them) |
| `edits` | new versions of a part that already had a text (author edit, redraft, regenerate, replace, command, agent merge, restore) |
| `reconstructed` | true for a day rebuilt from the history of the parts instead of counted live, see *Backfill* |
| `lastChange` | timestamp of the last count, for the "last active" column |

Unique index `(bookUuid, day, agent)`; a second index on `(userId, day)` for the global statistics. No row exists for a day without
activity.

**`ActivityInterval`** (table `activity_intervals`): the time, **one row per stretch of time** during which a kind of activity went
on in a book.

| Column | Meaning |
|---|---|
| `bookUuid`, `bookName`, `folderPath` | as above |
| `kind` | `OPEN` (the book was open), `EDIT` (the author was typing in a part), `GENERATE` (a model was generating for the book), `RUN` (an agent was working on the book) |
| `agent` | false for `OPEN` and `EDIT`; for `GENERATE` and `RUN` whether the agents or the author caused it |
| `day` | the day the stretch is in; a stretch is **cut at midnight** (the row is closed at 00:00:00.000 and a new one opened), so every row is inside one day |
| `startTime`, `endTime` | timestamps with milliseconds; `endTime` is moved forward while the stretch is open |

Indexes `(userId, day)` and `(bookUuid, day)`. A stretch is one row, however many things overlapped inside it (the recording merges
the overlaps of the *same* kind, book and side, see *Time*), so the table grows with the number of stretches, not of requests. The
length of a row is `endTime − startTime`, in milliseconds; there is no counter column to keep in step.

**`ManuscriptSize`** (table `manuscript_sizes`): how big the book is, **one row per book and day** (unique `(bookUuid, day)`, index
`(userId, day)`), the latest value of the day. Not split by side: the size is a property of the book, and the original book
changes only when the author's text, a restore or the merge of an agent changes it.

| Column | Meaning |
|---|---|
| `bookUuid`, `bookName`, `folderPath` | as above |
| `day` | `date` |
| `bookWords`, `bookTokens` | the size of the **whole book**: every part that is not deleted, all branches and alternatives included (`ChatMessageService.getTotalWordCount` / `getTotalTokenCount`, the numbers the book already shows) |
| `branchWords`, `branchTokens` | the size of the **current branch**: the story from the first part to the active leaf (`getBranchWordCount` / `getBranchTokenCount`) |
| `leafUuid` | the active leaf the branch values were measured on, so a day on which the author switched to another branch can be told from a day on which the branch grew |
| `baseline` | true for a row that only says how big the book was when tracking began (see below), written only for a book with nothing to rebuild the history from; the change that day is unknown |
| `reconstructed` | true for a day rebuilt from the history of the parts, see *Backfill* |
| `lastChange` | timestamp of the last update of the row |

**Words are the main measure of growth, tokens the second one** (the book is measured in both, every time, and both are kept).
Words are what an author counts and compares; they come from `countWords` and do not depend on a model or a tokenizer, so they
are the same whatever model the book uses. Tokens are **never recounted**: a part keeps the token count it was written with, so
changing the model or the tokenizer changes nothing in the rows already written, and the parts added afterwards are counted with
the new tokenizer, which is correct (they cost what the new model counts). The token totals of a book that used several models are
therefore a mix, and may jump when the model is changed; that is expected, the author knows they changed it, and the words are
the stable figure to compare. Every place
that shows a size or a growth shows the words first and the tokens next to them, and the one that has to choose (a chart, the
order of a list) takes **words by default**, with a switch to tokens.

A row is **updated every time the size changes**, and a day without a change has no row (the size of that day is the last row
before it, carried forward). The growth of a day is `end of the day − end of the day before`, where the day before is the latest
row that is earlier; for a book that was created under tracking and has no earlier row, the earlier size is 0, so the first day of
a book is all growth. For a `baseline` row there is no growth to show. The comparison between any two days is the same
subtraction (the size at the end of each), and a range's growth is `size at the end of the range − size before it`.

## The book is gone, the history stays

The statistics are the author's record of their work, not a part of the book. So:

- **No cleanup reference to the book.** The rows hold the `uuid` of the book as a plain column, so neither the trash nor the purge
  of the book touches them. A book in the trash keeps counting in the totals and in the per-book list; a purged book stays in the
  history under its last name and path. Both are listed with the **deleted** flag (the trash is a deleted book until it is
  restored). "Deleted" is a `LEFT JOIN` on `manuscripts.uuid` (no row = purged, `is_deleted` = in the trash), done by the per-book
  query, never stored. A book restored from the trash is the same `uuid` and carries on.
- **The name and the folder path are copied into every row of all three tables** (`bookName`, `folderPath`) so that a purged book can
  still be shown and so the per-book and per-folder sums need no join. They are one value per book, kept equal in all its rows:
  - **rename** of the book updates all its rows: `ManuscriptServiceImpl.save`, which sees the name change (it loads the stored
    book), calls `ManuscriptStatisticService.refreshBook(book)`, one SQL update per table `WHERE bookUuid = :uuid`;
  - **move** (`ManuscriptService.moveToFolder`, a bulk update that does not save the book, so it calls the refresh itself, the
    *Organise* dialog and the mass change go through it) updates the path of the moved books;
  - **rename or move of a folder, and the delete of a folder that moves its content up**, change the path of every book beneath
    it (nested ones too): `FolderService` calls `refreshFolder(folder)` after the change, which recomputes the path of the books
    beneath from `FolderService.getPaths()` and updates the rows book by book (a few hundred books at most);
  - a safety net: `count` and the tracker compare the name and path of the book they write for with the cached values of the
    last row they touched and correct the rows when they differ (a save path that was missed does not leave a wrong name for good);
  - `deleteWithContent` of a folder and the delete of a book change nothing: the rows keep the last name and path, which is the
    point.
- **Clearing is the user's own act.** *Clear statistics* (a book, a deleted book, or all of it) is a hard delete of the rows of
  all three tables; the list of deleted books has a *Forget* action for exactly that.

## Where it is counted

The point of the design: **no new calls spread over the code base**, every number comes from a place that already is the single way
something is done.

- **Words, parts, edits** come from `MessageRevisionService.record` ("the single way a version is made"). A version has its
  `wordCount`; the change is `new.wordCount − previous.wordCount`, and the previous version is already loaded to compare the text.
  No previous version → a part was created (`partsCreated`, the words go to `wordsWritten` or `wordsGenerated` by the source of
  the version). A previous version → `edits + 1`, a positive change goes to `wordsWritten` / `wordsGenerated`, a negative one to
  `wordsRemoved`. `ensureCurrent` records the unknown writer's version of an unrecorded text; that is a repair, not an edit, so it
  does not count (a `UNRECORDED` / repair flag in the call, the same way `RevisionOrigin.UNRECORDED` works). Because blank text
  records nothing, an empty part and a generation that was stopped before any text are not counted as parts.
- **Deletes** come from `ChatMessageServiceImpl.delete` (soft; `findDescendantIds` is already called there, so the count is its
  size + 1) and `deleteNodeAndMigrateChildren` (one part). The words of the deleted parts are the sum of their newest versions'
  `wordCount` (one query on `message_revisions`, not a load of the text). A **hard delete of a part that is in the trash is not
  counted again**, and restoring from the trash does not subtract (the statistics are a log of what was done, not a balance).
- **Tokens** are the hard one: `OpenAICompatibleInferenceService.recordUsage` knows the AI and the user but not the book, and the
  callers are many (the generation `InferenceStep`, summaries, impersonate, annotate, the agents, plugins). Proposed: a
  *usage context*, a request attribute next to the ones `InRequestScope` already carries to the worker threads
  (`UsageContext.manuscriptId`), set by the generation pipeline once (the controller knows the book) and by the other callers that
  have one (`SummaryServiceImpl`, `ImpersonateServiceImpl`, `AnnotateServiceImpl`, the agent run), and read in
  `UsageServiceImpl.record`, which then adds to the `Usage` of the user *and*, when the context has a book, to the row of the book.
  A call without the context counts for the AI only, as now. Plugins get the same through `UsageContext.with(book, runnable)` /
  an `ExtensionAI`-independent hook in `UsageService`. The alternative (the callbacks carry `RoundUsage` already; every caller would
  report it itself) puts the code in ten places and loses the count of a stopped call, so it is not taken.
- **Generation time** is not a number added at the end of a round but a stretch of the clock: the inference service opens a
  registration of the `ActivityTracker` (see *Time*) for the book of the `UsageContext` when it sends a round (a call with several
  rounds, the function calls, registers each round on its own: the time spent in the functions between the rounds is not the
  model's) and closes it in a `finally` (done, error, stop, cancel), so a stopped call counts until the stop, as it counts its
  tokens. A call without a book in its context registers nothing.
- **Size** is not summed from the changes but measured: after a change the book's sizes are asked of the same functions that
  show them (a `SUM` over the parts of the book, and the branch of the active leaf), and the row of today is set to the result.
  **The measurement never calls a tokenizer:** it adds up what each part has saved (its word count and its token count, the latter
  counted with the tokenizer that was last used on that part), so nothing is recounted. A delete takes away exactly what the part
  had saved, a restore from the trash gives the same back, and a model change touches no stored number.
  `ManuscriptSizeService.touch(book)` only marks the book as changed; a coalescing worker (2 seconds after the last `touch`, so a
  burst of edits or the streaming end of a generation is one measurement, and at once when the book is closed or the server shuts
  down) loads the book by id, measures, and upserts the row. No session is needed (the owner is the owner of the book). The
  `touch` calls are in the places where the size can change, each of which already is the single way of its change:
  - the text of a part: `MessageRevisionService.record` (also for the repair `ensureCurrent` and the merge: **the "do not count"
    flag is about the counts, not the size**, an agent merge and a restore do change the size of the original);
  - the parts of the book: `ChatMessageServiceImpl.delete`, `deleteNodeAndMigrateChildren`, the restore from the trash
    (`TrashService`), `createPart`, `insertPart`, `addPart`, `mergeIntoPrevious`;
  - **the current branch**: switching the active leaf (`swipeTo`, `swipeAndSwitch`, `branchAndSwitch`, a new part that becomes the
    leaf): the whole book does not change but the branch does;
  - the whole book at once: `restoreBackup`, the archive import, the SillyTavern import, the cleanup;
  - **opening the book** (the `OPEN` registration of the tracker, below) and closing it: a safety net that makes sure there is a
    row for today and corrects a size that drifted (a path that was missed);
  - the book is **born** (new book, duplicate, branch into a new book, `cloneFromBackup`, import): `touch(book, born)` writes its
    first row, not a `baseline`, so the growth from nothing is the first day's.
  The working copy of an agent is not measured (it is not the book; its size reaches the original at the merge, which `touch`es the
  original). A book that has no row at all when it is first touched (a book that was there before the statistics were) gets a
  `baseline` row; the upgrade rebuilds the history of every book (see *Backfill*), and a `baseline` row is left only for a book it
  could not rebuild.
- **Not counted:** the changes of the book's settings, the lorebook, the summaries' text (only the model calls for them are
  tokens), the annotations. The text of a part that is a *branch* of another (`DUPLICATE` / `BRANCH`) is a copy, not writing: the
  version of a duplicate does not count, a *swipe* (a new generation of a sibling) does, it is a real generation.

## Time

### Recording

- **`ActivityTracker`** is a new bean (`services-config.xml`, one daemon `ScheduledExecutorService` with a `@PreDestroy`
  shutdown) and the **only writer of `ActivityInterval`**. Code that has something going on takes a registration
  (`AutoCloseable`) and closes it; the tracker turns the registrations into rows.
- **A key** is a kind, a book (its `uuid`) and the `agent` flag. The registrations of one key that are open together are **one
  stretch**: the first one inserts a row (`startTime` = now), the tick and the close of the last one move `endTime` forward, and
  the row is finished when the set is empty. Two requests at once, two browser tabs, two editors, sub-agents that ask the model
  together: one row, whatever their order and overlap. This is the only merging done when recording: **different books, and
  different kinds, are different rows that overlap as they happened**.
- **The tick** is every **60 seconds** (a constant of the bean, injected so a test can change it): it moves the `endTime` of every
  open stretch to now (one `UPDATE` per open key), cuts a stretch that crossed midnight (closes it at 00:00:00.000, opens the next
  day's at the same instant), drops the registrations older than their cap (see below) and makes the `RUN` registrations (below).
  A server that dies loses at most one interval: the row keeps the last `endTime` it was given and nothing is open after a start.
  A gap longer than two ticks (a suspended laptop) closes the stretch at the last tick instead of bridging it.
- **A registration that never closes** (a leak, a request that hangs) must not count for ever: each has an age cap (the request
  timeout of the AI for a generation, an hour for the others) after which the tick drops it and logs it.
- **Linger.** `GENERATE` keeps a stretch open for 2 seconds after its last registration closed (the next round of an agent
  usually follows at once), so a busy agent is a few long rows and not thousands of short ones; the row ends at the last close, and
  a gap of up to 2 seconds bridged by a new request counts as generation. The other kinds have no linger. The constant is the
  only inexactness of the recording, and is there for the size of the table.
- **No session.** The tick and the closes run on threads without a user, so the owner of a row is the owner of the book, read from
  it, never `currentUser`.

### What is recorded

- **`OPEN` and `EDIT` (the author's side).** `ActivityTracker.open(book)` returns a registration the book window takes when its
  story tab is attached and closes when it is detached; the registration has `editing(boolean)` for the part editor (the edit
  toggle of the story card). The key is the book, not the registration: the same book in two browser tabs is one `OPEN` stretch,
  and editing two parts at once is one `EDIT` stretch.
- **What "open" means.** The story editor of the book is attached (its story tab is the one tab that cannot be minimized, `TabDock.setMinimizable`, so an attached editor is on the screen and there is no "open in the background" to decide) **and the browser page is visible**: a small script in
  `JavascriptLibrary` reports `visibilitychange` (and nothing else) to the registration, so a book left open in a hidden tab
  overnight does not count. A heartbeat is not needed, the UI that stops answering is closed by `SessionManager` and the detach
  closes the registration. `OPEN` has no idle rule, so a long read of a part counts.
- **Editing time and the idle cut-off.** `EDIT` is recorded only while the author is *typing*: a part is in the editor, the page
  is visible **and the last keystroke was less than 5 minutes ago** (the thinking pause after a key counts, a night with the
  editor open does not). The window is a constant (`ActivityTracker.IDLE_CUTOFF`, 5 minutes), not a setting. It is measured on the
  client from the last `keydown` in the editor (the mouse and scrolling do not count, reading is *open* time, not *editing* time):
  the script keeps the time of the last key and tells the registration only about **changes of the state** (typing → idle after
  the cut-off, idle → typing on a key), not about every key, so a fast typist sends nothing. The registration is "in the set" of the
  key only while it is typing, so the stretch closes when the author goes idle. The server never trusts the cut-off alone: a
  registration that stays "typing" without a state change for longer than the cut-off plus one tick (a lost message, a frozen
  tab) is treated as idle by the tick.
- **Not a change of the `@Extendable` classes:** the registration is made by one small helper component the story windows add to
  themselves in their attach listener (and the phone twin), so no method of an `@Extendable` class changes and
  `PublicAPISignatureTest` has nothing new to say; the edit toggle is the only call into an existing method.
- **`GENERATE` (both sides).** A registration for every round that is on the wire, see *Where it is counted*; the key is the book
  and the side (an agent's working copy is the agent side of the original, see *Agents*).
- **`RUN` (the agents' side).** Not registered by code but by the tick, which asks in one SQL query for the runs in the status
  `RUNNING` (not `PAUSED`, not waiting for the author) and keeps one stretch per original book that has one. A run counts as
  working while its status says so, whatever the number of its sub-agent threads.

### Showing time: the union

`TimeUnion` is the one place that combines intervals, and everything that shows a time goes through it.

- **Input:** the intervals of a filter (the user, a range of days, the kinds, the side or both, a book or a set of books or a
  folder), read by one query on `(userId, day)` that returns only `bookUuid, kind, agent, day, startTime, endTime`.
- **Union:** sort by `startTime`, sweep, merge the overlapping and touching ones, add up the lengths. Rows are few (stretches, not
  requests), so it is done in Java, and it is exact to the millisecond.
- **Where it is used.** The time of *one book and one kind* is the sum of its rows (they cannot overlap: one key is one stretch at
  a time), but it is run through the same union so nobody has to rely on that. *Combined* times are always unions:
  - the **totals line** of the global statistics: the union over all the books of the range (five agents × 24 h = 24 h);
  - the **per-day grid and chart** of the global view: the union per day over the books;
  - a **side switch** *All* over both sides: the author's and the agents' intervals together (the author writes in book A while an
    agent runs on book B: the generation of both is one stretch if they overlap), *Me* and *Agents* each their own;
  - a **folder** (`sumByFolder`): the union over its books;
  - the **per-book line** is that book's own time: the lines of the list are therefore **not parts of the total** and the time
    columns have no *Share* column; a note under the list says the books' times overlap and the total is the real time.
- **Kinds are not added to each other** either: `EDIT` is inside `OPEN` in time and a generation happens while the book is open, so
  the four times are four numbers, never a total of them (the chart draws them side by side, not stacked).
- **Examples that are tests:** 5 books × `RUN` 24 h at the same time → 24 h in total, 24 h per book; 3 books × `RUN` 8 h one after
  another → 24 h in total, 8 h per book; two overlapping stretches of 10 s offset by 4 s → 14 s; a stretch of the author and one of
  an agent that overlap, *All* → their union, *Me* and *Agents* → each its own.

## Agents

An agent works on a hidden **working copy** of the book (`Manuscript.agentic`, `AgentRun.workingCopy`) and its changes come back
only through the merge the author reviews. The statistics must go to the book the author knows, so:

- `ManuscriptStatisticService.count` and the tracker take the book of the change and resolve it: a book that is `agentic` is the
  working copy of one run (`AgentRunService`, a lookup by working copy, cached for the call), the run's agent has the original
  (`Agent.manuscript`), and the count or the interval is added to the **original with `agent` = true**. A copy that belongs to no
  run (a copy in the middle of being made or dropped) is not counted. Everything counted for the book (words, parts, deletes,
  edits, tokens, generation time) follows this one rule, so `MessageRevisionService.record`, `delete` and the usage context need no
  special cases. The tokens of the run are in `AgentRun.prefillTokens` / `generatedTokens` already; the statistics add them per
  day and book, the run's own total is not touched.
- **The merge is not counted.** The versions an agent merge writes into the original (`AGENT_MERGE`) are the work that was already
  counted live on the copy. The merge passes the "do not count" flag of `RevisionOrigin` (the same one as the repair), so nothing is
  counted twice. For the same reason **making the copy** (the copy of the parts and of their versions), restoring a checkpoint into
  a new copy and the hard delete of the copy when the run ends are not counted: they do not go through the counted paths
  (`record`, `delete`) or are flagged; a test per case.
- **Discarded work is counted too.** The statistics are a log of what was done and what it cost, not of what ended in the book; an
  agent run that is discarded still spent the tokens and the time. The agent side is therefore "what the agents did", not "what the
  agents contributed", and the dialog says so.
- **The two sides of a book are never written at the same time by two actors.** A book has at most one agent and an agent has one
  unfinished run (`AgentRunService.findUnfinished`), and while the run holds the lock (`checkNotLocked`,
  `ManuscriptLockedException`) the book cannot be opened for writing. So there is no moment in which the author and an agent both
  change the book, and what follows from it is used instead of being guarded against:
  - the copy → run → original lookup has exactly one answer (a working copy belongs to one run, a book to one agent), so it
    can be cached for the call and never has to choose;
  - a book has one `RUN` stretch at a time (one run), so the tick needs no de-duplication across runs;
  - the author's side of a book does not change while a run is unfinished (apart from `OPEN`, if a locked book may be opened to
    read; `EDIT` and the word, part, edit and delete counts cannot move), and the agent's side does not change when the run is
    over and the review is open (the merge is the author's, counted nowhere, see above), so the "merge is not counted" rule
    never has an author edit mixed into it;
  - the merge, which writes into the original, runs with the book still locked until it is finished.

  This does **not** remove the concurrency inside one side: the threads (sub-agents) of one run all add to the agent row of the
  counts, the author can generate in two branches or have the book in two browser tabs, and the tick runs while a request adds
  tokens. That is why the counters stay SQL increments (`x = x + :n`) with the first insert of a row guarded by the lock plus
  the unique index `(bookUuid, day, agent)` and a retry of the update, and why the intervals have one writer, the tracker.
- The display adds the two sides by default (the book's whole day; the times through `TimeUnion`), with a switch *All / Me /
  Agents*, and shows the agents' `RUN` next to the author's `OPEN` and `EDIT`.

## Decisions

- **Day boundary.** The day of the server's time zone (`ZoneId.systemDefault()`): Marginalia is self-hosted and the desktop app
  is the same machine, so the zone of the process is the one the author lives in. A per-user zone setting is not worth a setting
  now; the columns are a `date` and exact timestamps, so a zone can be added later without a migration of the data. A stretch of
  time is cut exactly at the midnight of that zone.
- **Counters are a log, not a balance.** Nothing is decremented when a deleted part comes back or a version is rolled back; the
  rollback is an edit with a negative change of the words. "Words in the book now" is still `getTotalWordCount`.
- **No story text in the tables.** Numbers, times, and the name and the folder path of the book (which the book list shows
  anyway); the production log rule (no story content) is not touched. Both are things the author named themselves, so a statistic
  that outlives its book can carry them; the name is not written to the log.
- **A clone starts clear.** A duplicate of a book, a book made from a backup (`cloneFromBackup`, the *Duplicate* and the *Branch into
  a new book* both end there), an import and an agent's working copy are books with a **new `uuid`**, so they have no rows of
  any of the tables and get none of the source's: nothing is copied, nothing is shared (the clone's own first size row is written
  when it is made, see *Size*). What the copying itself does is not counted
  either: the parts of a clone are `DUPLICATE` versions (not counted, see *Where it is counted*), and `cloneFromBackup` runs with
  the "do not count" flag so the words and parts of a copy are not booked as written that day. The first row of the clone is its
  first real activity.
- **The backup does not carry the statistics.** They are not in the backup JSON, so a backup is not a way to move them between
  books and a clone made from it is clear. `restoreBackup` (the restore *into the same book*) replaces the content of the book
  with the backup's: it is not counted either (flag) and it leaves the history of the book as it is, a restore is not "work that day".
- **A shared AI used by somebody else.** The rows belong to the owner of the *book*; the `Usage` of the other user keeps counting
  the AI side. Books are not shared between users, so the case is the same user in practice.
- **Failure to count never fails the work.** Like `UsageService.record`: the count is wrapped, logged on failure and swallowed; the
  version, the delete or the generation is not rolled back for a statistic. The writes of the words / parts / edits are in the
  transaction of the change only when it is the same call (`record` runs in it), else after it.

## Global statistics

The same rows read across the books: no new table, just queries over `(userId, day)` with the dates.

- **Ranges** (`StatisticRange`, one enum shared by the book window and the global one, resolved to `from` / `to` dates in the
  server's zone, both ends included): *Today*, *Yesterday*, *Last 7 days*, *Last 30 days* (rolling, today is the last day), *This
  month*, *All time* and a *custom* `from` – `to` pair from two date pickers. A range is two dates, so the service takes
  `(LocalDate from, LocalDate to)`; the presets live in the UI only.
- **Growth.** `growth(from, to)` per book is the size at the end of `to` (the latest `ManuscriptSize` row at or before it) minus
  the size before `from` (the latest row before it; none = 0 for a book born inside the range, unknown for a baseline), in words and
  tokens, for the whole book and for the current branch (a note when the branch was another at the two ends). The total is the sum
  over the books. A deleted book keeps its last size (rows are not removed, nothing is set to zero), so after its deletion it
  has no growth, and before it it did; the *deleted* flag is on its line as everywhere. Two queries do it: the latest row per book
  at or before a date (a correlated `MAX(day)`), twice.
- **Queries** in `ManuscriptStatisticService`, filtered by the user, the dates and optionally the side: the counts by SQL `SUM`
  and `GROUP BY` (`sumByDay(from, to, side)` for the grid and the chart, `sumByBook(from, to, side)` for the list, `sumByFolder`),
  the times by `TimeUnion` over the intervals of the same filter. `total(from, to, side)` is the totals line, counts and times.
  The book's own window is the same queries with a book id.
- **Which books.** All of them, **also the deleted ones**: the totals of a range are what the author did in it, whatever became
  of the books later (*The book is gone, the history stays*). `sumByBook` groups by `bookUuid` and shows the name and the folder
  path of the row, with the **deleted** flag from the join; the same book is one line even after a rename, because the rows were
  renamed with it. The grid can filter the deleted ones out. A `sumByFolder` (group by `folderPath`) is the same query on the other
  column, for "how much did I write in *Sci-fi* this month" (a prefix match includes the subfolders; its times are the union over
  the books of the folder). Read access is `findAllForUser` style: only the user's rows, never an id taken from the interface.
- **The breakdown by book is part of the global view, not an extra.** For the chosen range the dialog shows the totals line, and
  under it **one line per book** (`sumByBook`) with every column of the book's window (tokens, words, parts, edits, deletes, the
  four times), ordered by any column (words by default) and with a *Share* column (the part of the range's total) for the counts
  only: the times of the books are each the book's own and overlap (*Time*, *Showing time*). A book that is **deleted** (in the
  trash or purged) is listed under the **name** its rows keep, with a **deleted** flag (a badge in the line, a column on the
  phone); the flag is the join result (`manuscripts.uuid` missing or `is_deleted`), so a book restored from the trash loses it.
  The per-day grid and the charts are on further tabs, with the same *All / Me / Agents* switch and range. A click on a line opens
  the book's own statistics window when the book exists, nothing for a deleted one.
- **Same name, same path.** Books may share a name in one folder (legal: only folders are unique among their siblings), and a
  deleted book keeps its name, so two lines can look alike. Where the list has two or more lines with the same `bookName` **and**
  the same `folderPath`, each of them gets a small tag with the first 8 characters of the book's `uuid` (a badge next to the name,
  the whole `uuid` in its tooltip so it can be matched with the book's *About* page, an export or a log). The test is made on the
  lines of the result (the list the dialog shows, not the whole table), so a name that is unique in the range has no tag, and
  one that becomes a duplicate when a range widens gets it. The tag is only a display rule: `sumByBook` always returns the
  `bookUuid`, and the grouping is by it, so the duplicates are never merged. Same in the phone twin.
- **For agents and the command bar:** `get-statistics` (range preset or dates, `by` = `day` or `book`), read only, next to
  `get-book-statistics`.

## Interface

- **Statistics** in the book window (menu of the book next to *Backups* / *Token usage*): a `StatisticsDialog` (`@Extendable`, so
  `@PublicAPI` on its methods and `PublicAPISignatureTest`) with a grid by day (newest first; tokens sent / generated, words
  written / generated / removed, parts, edits, deletes, times open / editing / generating / agent running, shown as `h:mm`, and the
  **size** of the book at the end of the day (whole and current branch, words and tokens) with the **growth** against the day
  before, coloured by the sign through a shared CSS class), a
  switch *All / Me / Agents*, a totals line, a range (the presets of `StatisticRange` and a custom one) and the charts. Phone
  twin `PhoneStatisticsDialog`.
- **Charts** use the `ChartJs` widget that the token dialogs (`TokenUsageDialog`, `BranchTokensDialog`, `AgentContextView`) use: a
  Chart.js configuration built as a `Map`, turned into JSON with Gson and given to `setChartConfig`, `setHeight` for the size
  (smaller on the phone). No new library, no new `@NpmPackage`, no CSS: `chart-connector.js` already takes the text and grid
  colors from the Lumo variables, so the charts follow the light and dark theme. The series colors are constants of the
  dialog, as in `BranchTokensDialog`. A shared `StatisticCharts` helper builds the configs for both dialogs and both twins from
  the rows the queries return, so they cannot drift. Three charts on tabs of the dialog, each over the days of the range:
  - **Words per day:** stacked bars of *written*, *generated* and *removed* (the last one drawn negative, below the axis);
  - **Time per day:** **grouped** (not stacked) bars of open, editing, generating and agent running in hours, because the kinds
    overlap in time and are not parts of a whole. A function cannot be put in a JSON configuration, so the values are converted
    to hours in Java and the axis is titled with the unit; the exact `h:mm` is in the grid. The values are the unions of
    *Showing time*, so in the global dialog a day with five agents running all day is 24 h, not 120;
  - **Tokens per day:** stacked bars of sent and generated;
  - **Size of the book:** lines of the whole book and of the current branch in words (tokens on a switch), one point per day with
    the last known value carried over the days without a row (a step line), so the slope between any two days is the growth;
    a day on which the branch changed is marked on the line; below it the growth per day as bars (negative when the book shrank).
    In the global dialog the lines are the sum of the books' sizes and the growth bars their sum, and the breakdown by book has a
    growth column;
  - and, in the global dialog, **by book:** a horizontal bar of the books of the range by the chosen column (words by default, time, tokens or
    growth in words or tokens, which can be negative), the top 10 and the rest as *Other*, deleted books named and flagged as in the list. For a time column the bars are
    each book's own time, with no "Other" total and a title saying they overlap.
  The days without a row are not in the query result: the helper fills the range with a zero for each of them so that the axis is
  honest. A range that is longer than 90 days is grouped by week (over a year, by month) so the bars stay readable; the grid keeps
  the days. The *All / Me / Agents* switch and the range change the data of the charts and the grid together.
- **Global statistics**: a `GlobalStatisticsDialog` for the whole workspace, opened next to the *Token usage* of the models; the
  same grid and charts as the book window (`StatisticCharts`), with the range chosen on top.
- A command for agents and the command bar: `get-book-statistics` (range, returns the rows), read only, next to `get-book-info`.
- Texts: new keys in `L`, `LocalizationBundleEN`, `JP` and the Chinese bundle, grouped by prefix, reusing `LABEL_TOKENS_SENT`,
  `LABEL_TOKENS_GENERATED`, `LABEL_REQUESTS`, `LABEL_CLOSE`; new ones for the times and for the *All / Me / Agents* switch.

## Backfill

Everything but the time can be rebuilt for the days before the statistics existed, from data the application already keeps: every
part has its `creation`, every version of its text (`message_revisions`, since 1.6.0) has `creation`, `source`, `is_agent`,
`wordCount` and `tokenCount`, and a deleted part keeps its `modification`. It is not exact and does not pretend to be, but it is
much better than an empty chart that starts on the day of the upgrade. One app migration, `StatisticsBackfillMigration` (app
version bump, written like `PartRevisionMigration`: a batch read per book in one session, the rows written in another
transaction, retried on a lock; it runs once and writes nothing for a book that has rows already).

- **What is rebuilt**
  - **Parts created:** per day, from `ChatMessage.creation` of the parts of the book (swipes included, the parts in the trash
    included, the purged ones are gone). The author's side, unless the first version of the part is a generated one: then it is a
    generation, which the live counting also counts as a part created.
  - **Edits and words:** from the versions of each part in order: the first version is the part created (its `wordCount` is
    `wordsWritten` or `wordsGenerated` by the source, `isModel()`), every later version is an edit whose word change is the
    difference of the `wordCount` of the version and the one before it (positive to written / generated, negative to
    `wordsRemoved`). The same rules as the live counting in `MessageRevisionService.record`, so the two give the same numbers for
    the same history. A version of an agent (`is_agent`) goes to the agents' side, and `AGENT_MERGE` too (the live counting does
    not count a merge, but the work that preceded it is lost, the merge is the only trace of it). A `DUPLICATE` version and a repair
    (`PLUGIN` without a detail) are not counted, as live.
  - **Deletes:** a part in the trash gives `partsDeleted` on the day of its `modification` (the day it was deleted, as far as it
    is known) and its words to `wordsRemoved`. A part that was purged is gone and is not found.
  - **Tokens:** estimated, since the real counts of the calls were never kept: the tokens *generated* from the `tokenCount` of the
    generated versions, the tokens *sent* from the prompt tokens the part has saved (`promptTokens`). The reasoning, the summaries and
    the other calls are not in them, so the numbers are a floor, and are shown as an estimate.
  - **Size:** the size of the book at the end of each day, from the versions: for a day, the sum over the parts that existed and
    were not deleted that day of the `wordCount` / `tokenCount` of the newest version made by then. Done for the whole book; for
    the **current branch** the branch is the one of today's active leaf (the old active leaves were not kept), measured the same
    way, so the line shows how the branch the author is on now grew. `leafUuid` is that leaf. A book that has no versions at all
    (nothing to rebuild from) gets one `baseline` row of today instead.
  - **Days worked on** (for the calendar widget): they come out of the above, since every day that has a part created or a version
    has a row: the creation and the modification of the parts and the dates of the versions reveal when the book was worked on.
    Before 1.6.0 there are no versions, only the one `modification` of a part: such a part marks its creation day and its last
    modification day, and the days between are not known.
- **What is not rebuilt: the time** (`OPEN`, `EDIT`, `GENERATE`, `RUN`). There are no intervals for the old days, and the display
  must not show them as zeros: the day is **unknown**, shown as a dash in the grid and left out of the time chart. The date from which
  the time is recorded is kept (`statisticsSince`, an extended attribute of `UserSetting`, set by the migration the first time), and
  every day before it is a day without time; the dialog says so in a line under the grid.
- **Marked as reconstructed.** The rows have a `reconstructed` flag (a column of `ManuscriptStatistic` and of `ManuscriptSize`); the
  grid marks them (an icon with a tooltip: *rebuilt from the history of the parts, an estimate*), the totals say how many of the
  days are reconstructed, and the token columns of those days carry the "estimate" mark. A live count that falls on a day with a
  reconstructed row **replaces** it (the migration runs at the start of the new version, so a day is either rebuilt or live, and
  the day of the upgrade is live from then on and not rebuilt).
- **Not rebuilt for a book that was purged**: its parts are gone, and so is anything to build from.
- **Names and paths** are the books' current ones.

## Order of work

1. ✅ **Data layer:** `ManuscriptStatistic`, `ActivityInterval` and `ManuscriptSize`, V23, their repositories (`JpaManuscriptStatisticRepository`: `add`
   as one SQL update that returns the rows changed, `findRange`, the sums; `JpaActivityIntervalRepository`: insert, move the end,
   find by filter, delete by book), `ManuscriptStatisticService` (`count(book, delta)`, `range`, the sums), wiring,
   `persistence.xml`. No `@CleanupReference` to the book. `ManuscriptStatisticCrudTest` (`OwnedCrudContract`) and a concurrency test
   (many threads adding to the same day lose nothing); a test that the trash, the purge and the cleanup of a book leave the rows
   and a test of the per-book list for a book in the trash and a purged one.
2. ✅ **Words, parts, edits** in `MessageRevisionService.record` (+ the repair flag), with the tests of every `RevisionSource`, a
   blank text, the same text twice, `ensureCurrent`.
   Done differently from the first draft: a `counted` flag on `RevisionData` / `RevisionOrigin` (`uncounted()`) instead of a `UNRECORDED`-like constant; `recordFirst` and the agent merge are uncounted; `MERGE` is an edit with no words (step 3: the merged-in part's delete must not count its words as removed again); `rollBackTo` counts its own edit; nothing is counted while the book's history is off; the hidden working copy of an agent is skipped until step 5; archive / SillyTavern imports still count as written words (step 8 decides).
3. ✅ **Deletes** in `ChatMessageServiceImpl` (soft, hard, node-and-migrate), with the words of the deleted subtree.
   Words of the deleted parts are their `wordCount` columns (`measureLive`), not a query on `message_revisions`, so a part without versions counts too. Only a soft delete counts; the merge counts the part but not its words; `deleteNodeAndMigrateChildren(…, counted)` for the agent merge.
4. ✅ **Tokens:** `UsageContext`, set in the generation pipeline and the other callers, read in `UsageServiceImpl.record`; the
   plugins' entry point. Tests with `MockLLMServer` (generate, regenerate, summary, a stopped generation, a call without a book).
   Done as a thread-local scope like `UnmeteredScope` / `InferenceSession` (`UsageContext.open(book)` around the call that starts a request, read once by the inference service in that thread and kept in its `Request`) rather than a request attribute, so the later rounds and threads of a request keep the book; set by `InferenceStep`, summaries, impersonate, redraft, annotate (all stages) and the agent runner (the working copy; skipped by `count` until step 5); the `anthropic` and `google-genai` plugins reopen it around `UsageService.record`. The plugins that ask a model about a book open the scope too (later): reviewer, sidequery (the answer and the tab name), arc-outline (the generator dialogs) and manga-generator (the section card). The arc outline always has a book (its dialogs and the commands pass it: `ArcOutlineService.generate(model, book, ...)`); the lore generator has one when it is used for a book: its commands run in the open book, and the dialog gets the book from the Lorebook tab of the book (a decorator on `ManuscriptLorebookPart.create` stores `ManuscriptDialog.refreshManuscript` on the lorebook view), while the lorebooks of the workspace have none (`LoreGeneratorService.complete(model, book, payload)`, `book` may be null). Not done: the SillyTavern converter (it works on a file, not a book).
5. ✅ **Agents:** the copy → original → `agent` resolution in `ManuscriptStatisticService.count`, the "do not count" flag for the merge,
   the copy, the checkpoint restore and the drop of the copy; tests that run an agent on a book and check that the author's side is
   untouched (also: a write to a locked book is refused and counts nothing), the agent's has the work, the merge adds nothing, a
   discarded run keeps its row, and two runs cannot exist for a book.
   `AgentRunService.findOriginalOfCopy` (a query on the run's `workingCopy`) resolves a copy in `ManuscriptStatisticService.count`, which replaced the temporary skip of step 2; the flags for the merge were done in steps 2 and 3 (versions and deletes), making the copy, restoring a checkpoint and dropping the copy needed none (backup restore versions and hard deletes are uncounted already), each has a test in `AgentStatisticsTest`, with a real run through the runner for the tokens.
6. ✅ **Time:** `ActivityTracker` (the keys and the stretches, the registrations of `OPEN`, `EDIT`, `GENERATE`, the `RUN` query, the
   60 s tick with the extension of the open stretches, the cut at midnight, the caps and the linger, the registration of a round in
   the inference service), the visibility and keystroke script in `JavascriptLibrary` (state changes only, the 5 minute cut-off), the
   helper in the story windows (desktop and phone) and the edit toggle; `TimeUnion` and the queries that use it. The tick takes its
   clock and interval from the bean so a test can drive it without waiting a minute. Tests: registrations of one key become one
   stretch (overlapping, back to back inside the linger, apart); two books at the same time are two rows that overlap; **the union
   examples of *Showing time*** (5 × 24 h, 3 × 8 h in sequence, partial overlaps, *All* over both sides); a stretch across
   midnight is two rows; a registration that is never closed is dropped by its cap; a hidden page does not count; editing stops
   counting 5 minutes after the last key and starts again on a key; a lost "idle" message is covered by the tick; a closed
   registration ends its row at the close and not at the next tick; a stopped agent run closes its `RUN`.
   Done differently from the first draft: the registrations are notes in memory and the tracker's own thread (one daemon `ScheduledExecutorService`, `init-method` / `destroy-method`) is the only one that touches the database, so a caller that holds a transaction never waits for SQLite; `flush()` writes now (tests, shutdown), `tick()` is public and the clock, the tick, the linger and a `manual` mode (no thread) are set by the tests (`ActivityTrackerTest`, `TimeUnionTest`, `GenerationTimeTest`). The `GENERATE` registration is made around each request in `OpenAICompatibleInferenceService` (`stream`: from before the request is sent to the one call of the usage recorder that every end goes through; `infer`: around the call; `round`: around the stream only, not around what the caller does with the answer), only for a metered request with a book. The `RUN` stretch starts at the tick that finds the run `RUNNING` (`AgentRunService.findRunningBookIds`) and ends at the tick that no longer does, so it is exact to a tick. The cap of a registration applies to `GENERATE` (one hour); an `OPEN` registration is held by the attached panel (the detach closes it) and `EDIT` by the typing state, whose fallback is the cut-off plus a tick, ended at the cut-off. The browser tells `marginalia-typing` true again at most once a minute while the keys go on (the server must not mistake a long run of typing for a lost message), `marginalia-visibility` on every change. The `anthropic` and `google-genai` plugins register `GENERATE` on each request path (`generating(run)`, nothing without a tracker, so their tests need no Spring), and the plugins that open `UsageContext` for a book (see step 4) give their requests to its generation time through the inference service. Not done: `ActivityIntervalService.times` / `timesByDay` / `timesByBook` are the unions, the per folder union comes with the folder statistics of step 9 (`ActivityRow` has no folder path).
7. ✅ **Size:** `ManuscriptSize`, `ManuscriptSizeService` (`touch`, the coalescing worker, `sizeAt`, `growth`), the calls in the places of
   *Size* and the `OPEN` safety net, the `baseline` rule for a book without history (the history of the others is the migration of step 10).
   Tests: edit, delete, restore from the trash, a new part, a branch switch (the branch changes, the whole does not), a merge of
   an agent (the original grows, the copy is not measured), a burst of edits is one measurement, a change of the model or the tokenizer
   leaves the stored tokens and the earlier rows alone, a book born today (growth from 0), a baseline row (no growth), two days compared with a day without a row between
   them, a deleted book keeps its size, no session on the worker thread.
   Done as designed, with these details: `touch(book)` / `touch(id)` / `touch(book, born)` only note the id in a map; one daemon thread (`size-measure`, `destroy-method="stop"`) measures what is due, 2 s after the last touch (`touchNow` at once, `flush()` for the shutdown and tests; `setManual`, `setDelayMillis`, `setClock` for tests). The measurement loads the book by id (not agentic, not in the trash), adds up `ChatMessageService.getTotalWordCount` / `getTotalTokenCount` / `getActiveBranchSizes` (columns of the parts, no tokenizer), and `set` writes the row of today only when something differs. The decisions of the first row: a book born under tracking (`touch(book, true)` from the first `ManuscriptServiceImpl.save` and `cloneFromBackup`) has a normal first row, any other book without a row gets a `baseline` row, and a baseline row stays baseline for the rest of its day (the change of that day is unknown). The touches are in the choke points instead of every caller: `ChatMessageServiceImpl.save` and `delete` (create, insert, add, merge, the text of a part, the soft and hard delete), `ManuscriptServiceImpl.save` (the active leaf: swipes, branches, a deleted active part; a new book) and the end of `cloneFromBackup` / `restoreBackup`, `MessageRevisionServiceImpl.append` (before the history-off exit: the size is not the work), the restore and the permanent delete of `CleanupServiceImpl` (they are bulk updates that no service hears), and the tracker (`open` touches, the close of the window measures at once). `ManuscriptSizeService.growth(book, from, to)` is the subtraction of the plan (`SizeGrowth`, `known` false for a day on which the book was first seen). The archive and SillyTavern imports need nothing of their own, they make parts through `ChatMessageService`.
8. ✅ **Book data and clones:** `refreshBook` / `refreshFolder` and their calls (`ManuscriptServiceImpl.save` on a name change,
   `moveToFolder`, the rename, move and delete of a folder, the safety net), on all three tables, with tests for every one (rename a
   book, move it, rename and move a folder with books two levels down, delete a folder, the *Organise* dialog's move, a stale copy
   saved with the old name); the clones: duplicate, branch into a new book, `cloneFromBackup`, import, the agent copy have no rows
   and book nothing; `restoreBackup` counts nothing and leaves the rows; the backup JSON has no statistics.
   Done as designed, with these differences: the three `refreshBook` updates are guarded (`WHERE ... AND (bookName <> :name OR folderPath <> :path)`), so a book that is saved again and again rewrites nothing; `ManuscriptStatisticService.refreshBook(book)` is called by every `ManuscriptServiceImpl.save` of a book that exists (a rename, or a folder set by save), `refreshBooks(books, folder)` by `moveToFolder` (a bulk update, the books in the caller's hand may have the old folder), and `refreshFolders()` by the rename, move, details rename, delete of a folder and by a move of the Organise dialog (`FolderService.transfer`): not `refreshFolder(folder)` of the plan, because the paths of the books that moved up or merged are easier to read all at once than to follow, so it takes every book of the user that is not in the trash from `ManuscriptRepository.findPlacements` and `FolderRepository.findPlacements`, which are scalar queries and see what a bulk update did even when the session holds the old entities. A book in the trash (and so everything `deleteWithContent` takes) is not in them and keeps its last name and path. The safety net is `noteBook(book, path)`, called by `count`, `ActivityIntervalService.start` and `ManuscriptSizeService.set`: when the name or path differs from what the last writer told, it asks the stored book, and corrects the rows only if the stored book differs too (a writer with an old copy of the book changes nothing). **Imports count nothing**: the archive and SillyTavern imports now make their versions with `.uncounted()` (the first draft counted them as written words, but the text was written elsewhere); the clones (duplicate, branch into a new book, `cloneFromBackup`) and `restoreBackup` already counted nothing (`DUPLICATE`, the uncounted restore versions), and the backup JSON has no statistics; each has a test in `BookDataStatisticsTest`.
9. ✅ **Interface:** `StatisticRange`, `StatisticCharts` (with a unit test of the configs: the filled zero days, the weekly grouping, the
   negative removed words, grouped time bars, the top 10 and *Other*), `StatisticsDialog` + `GlobalStatisticsDialog` and the phone
   twins, the menu items, the commands, localization (EN, JP, ZH), `BundleCompletenessTest`.
   Done as designed, with these differences: `StatisticRange` (in `domain.collections`, with `Span` and `parse` for the commands) and `SizeSeries` (the size on each day and the growth per book, pure) are in the domain, and a new `StatisticReportService.report(filter)` assembles counts, times and sizes into one `StatisticReport` that the windows and the commands both format; one `StatisticsView` (`ui.dialogs.statistics`) is the content of all four windows (`StatisticsDialog`, `GlobalStatisticsDialog` and the phone twins `PhoneStatisticsDialog`, `PhoneGlobalStatisticsDialog`, which only differ by `compact`: fewer columns, smaller charts); the menu item is *Statistics* in the settings menu of the story (`StoryEditor.openStatisticsDialog`), and the global window is a *Statistics* button in the header of the list of books (`ManuscriptPart`, `PhoneManuscriptPart`) instead of next to the token usage of the models, because a click on a book of its list opens the book, which only the list of books can do. The commands are `get-book-statistics [range] [side]` and `get-statistics [range] [by] [side]` (range as a name, a day or `from..to`, instead of the two parameters of the first draft). The Chinese texts are in the `chineselocalization` plugin. Not done here (step 10): the dash for the days before the time was recorded (`statisticsSince`) and the marks of reconstructed days. Tests: `StatisticRangeTest`, `SizeSeriesTest`, `StatisticChartsTest` (the filled zero days, the weekly and monthly grouping, the negative removed words, the grouped time bars, the top 10 and *Other*), `StatisticReportServiceTest`, `StatisticsCommandsTest`, `StatisticsViewTest`; the charts and the tabs were only built in tests, never looked at in a browser.
10. ✅ **Backfill** (see the section *Backfill* below): an app migration, `StatisticsBackfillMigration`, like `PartRevisionMigration`,
    that reconstructs the history of the counts and of the sizes from the parts and their versions, and marks it as reconstructed.
    Tests per rule of the section, with the fixtures of the `PartRevisionMigrationTest` kind.
    Done as designed, with these differences: the rules are a class without a database, `StatisticsReconstruction` (13 tests, one per rule: edits and removals, generated words with tokens and prompt, the day of the prompt, copies / snapshots / imports / repairs, the agents' side and the merge, deletes, a book in the trash, a part without versions, the live days, branch and size, no row on a day without a change), and `StatisticsBackfillMigration` (app 8 → 9, `appVersion` 9) only reads and writes (`StatisticsBackfillMigrationTest`: rebuilt rows and their flags, folder path, once only, a book with rows left alone, the days of the upgrade not rebuilt, the trash, a delete, a live size row after a rebuilt one is not a baseline, `statisticsSince`, the report). A part without versions (history off) is rebuilt from the part itself; the tokens sent are the prompt tokens the part saved, put on the day of its last generation (not each regeneration); a book without parts or text gets no row (not the baseline row of today: the first live measurement makes it); the live count does not replace a rebuilt row (the migration never writes the day of the upgrade, so they never meet); a part of a book in the trash is not counted as deleted one by one, and the days of its size are rebuilt. `UserSetting.statisticsSince` is a string (ISO date), set to the day of the first stretch of the user or today; `StatisticReport.timeSince` / `DayLine.reconstructed` carry both to the window (a dash for a day without time, the time chart leaves it out, `~` on a rebuilt day and on its tokens, two notes under the totals) and the commands (`timeRecordedSince`, `rebuiltDays`, `rebuilt` on a day).
11. **Docs:** user page in `manual/user` (with a picture: add it to `TODO.IMAGES.md` and the screenshot flow), the developer page
    (generation of the context, where the counts come from, the intervals and the union; front matter `covers` / `verified`,
    `tools/check_dev_docs.py`, stamp), `CHANGELOG.MD` under `[Unreleased]` (*Added*, the migration number V23, the new
    `UsageContext` / `UsageService` API for plugins), `tools/retype_build.sh`, and the regenerated `docs/` committed.

## Open questions

- **Size of `activity_intervals`.** One row per stretch: a few dozen a day for the author, and for an agent that runs for hours
  one `RUN` row and a few hundred `GENERATE` rows (thanks to the linger) a day. A year is in the tens of thousands of rows, and the
  "All time" union is a sweep over them; if that ever gets slow, the old days can be compacted (merge the touching rows of a key,
  lossless) or cached per month. Not needed for the first version.
- ✅ **Goals, streaks and a calendar widget (done as a last tab *Calendar* of the statistics windows: `WritingActivity` / `WritingGoal`, `StatisticReportService.activity`, `ActivityCalendarView`, the goals in `UserPart`; the goal and the goal streak are for all books only, the shade of a book goes by its busiest day).** Nothing in the data model is needed for it, it is
  all read from the rows that exist: a widget (in the book window and in the global view) that marks every **day the book was
  worked on**, like a contribution calendar (a grid of days, shade by words written or time spent editing, the last activity
  time as the tooltip from `lastChange` / the end of the last interval), the current and the longest **streak** of days in a row,
  and a **daily goal** (words, or editing time) drawn as a line or a ring, set in the user settings (an extended attribute of
  `UserSetting`, no migration). A day "worked on" would be one with words written, an edit, a part created, or `EDIT` time on the
  author's side; the agents' side and opening the book do not make a day (a rule of the widget, one place to change). Needs only
  the day queries of the *Global statistics* and a new component with CSS classes, so it can be added at any time after the
  first version without touching what is stored.
- **Retention and privacy (later, not in the first version).** The size of the data is not a concern (a count row is some 150
  bytes a day, an interval or a size row some 100: megabytes over years), **privacy is**: the tables keep when the author worked,
  for how long, and the name and folder path of every book, also of the books that were deleted. What exists in the design is the
  user's own *Clear statistics* and *Forget* (a book, a deleted book, all). What is left for a longer item of the TODO, to be
  settled when this lands: an administrator action to clear the history of a user, or of all users (the admin *Cleanup* purges
  soft-deleted objects and deliberately does not touch these tables, so it needs an action of its own); a setting to keep only the
  last N months, which the user or the administrator sets; a setting to switch recording off (per user, and for the whole
  instance); whether a purged book should lose its name in the rows (keep only the `uuid` and numbers, show *Deleted book*) instead
  of keeping the name for the list; and whether the statistics should be in the database backup and its restore (they are, as part of
  the database; the per-book backup does not carry them).
