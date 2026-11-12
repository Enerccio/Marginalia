# Concurrency of long operations — analysis and plan

The analysis for the item **101** of `TODO.md` (*Concurrency of long operations*; it sits between 86 and 87 there, out of order).
What the item asks for: one registry of the running operations per book and part, conflicting operations refused or queued with a
message that names the run, a run that survives the closed page, and a non-modal indicator with Stop, so that the strict modal of the
translators becomes a choice. This file says what exists today (core and every plugin), what is missing, a design, and an order of
work. Steps 1 to 8 of the order of work are built (step 3 for the two windows of the core; step 8 for kind A, kind B was
not needed so far).

Read from the code at commit `f2f64a955`. Points marked **(check)** were not read to the end and must be confirmed before they are
relied on.

## Where the work stopped (2026-11-12)

To pick up on another machine; delete this section when it is done.

1. **Full suite.** The last full run (repo root, `mvn clean test -Dvaadin.skip=true -Dbuild.dir=/tmp/marginalia-build`) passed all
   4,888 tests of the app; the plugins' tests were still running when the work stopped (their changed ones had passed one by one).
   Run it once more from the root and look at the plugins. The two runs before failed on tests that disturbed each other in one
   JVM, fixed in the same commit as this note: the current `UI` is cleared after every test (`MarginaliaTestBase`), the agent tests
   wait for their workers (`AgentTestBase`), `quietBackgroundWriters()` for the migration tests, and the size worker and the activity
   tracker no longer write from a run they scheduled before they were set manual. If another test fails only in the full run, look
   for a background writer first (a thread dump at the failure shows it).
2. **The browser check** below (section *The browser check*): the setup commands, then the list. Fix what it finds.
3. **Developer docs:** `python3 tools/check_dev_docs.py` flags 33 pages (most from the earlier steps); review them against the code
   and stamp each with `--stamp` in a commit of its own.
4. Optional: a code review of the commits of this work (`097b30d14` onwards), mainly the fold / detach / close paths of
   `FloatingWindow` and the plugins' `onDetach`.
5. Then the open items of the steps below (Audiobook batches as runs, Review / finished runs in the panel, Side Query's tab name...).

## Summary

- The pieces exist, but each of them is local: a flag of one page, a set inside one plugin, a log for the administrator. Nothing on
  the server knows "this book has a run", except the lock of the agents.
- The Knowledge Tracker already has nearly the whole wanted behaviour (a run object that outlives the page, one per book, a
  non-modal floating panel with the live reasoning, Cancel, a review that waits). It is the model to lift into the core.
- The lock of the agents (`AgentRunService.checkNotLocked`, called from 14 services) shows where a conflicting change must be
  refused. Locks become general: one check at the lowest level of every write (the base `save` / `delete`), for every owned
  object, with the agent lock folded in. They are not stored: a lock is derived from a run that is alive, so it cannot outlive
  it, and the forced unlock is ending the run (design 3a).
- The largest part of the work is not the registry (small) but (a) the operations whose answer today lives only in an open window
  (annotate, redraft, summary, review, generators), and (b) the story generation, whose listener is the page.
- Many of the modal windows only read the story until the author accepts their answer (annotate, redraft, review, the generators).
  They can simply stop being modal, long before any registry exists: see *The shortcut* (design 5a). It is the cheapest visible
  step.
- What follows a generated part (annotate, redraft, translate, knowledge, audio) is driven by the page and by a decorator on a UI
  method, without order, and only for generations started on the page. A second loop in the core with stages that plugins bind
  to, sequential or parallel, replaces it: design 5b.
- The connections of an AI should be the queue of every request, by default and apart from everything else here (design 7). The
  queue exists (`AIGate`) but only agents use it; one wrapper covers every provider. With it the freeze of a generation can let
  go of the tabs that only need a connection, and a parallel stage simply uses the connections there are.
- A book is open for editing in one window only, per user across sessions, refused by the same lock as an agent's book, with
  *open it here and close it there* as the way out of a dead page (design 3b). It removes the two-windows cases from everything
  else here.
- A second, quieter risk shows up once runs really work side by side: whole-row saves of a book or a part from a copy read
  earlier. It has to be dealt with first.

## What exists today

### Core

| Thing | Where | What it does | What it lacks |
|---|---|---|---|
| Freeze of the book window | `ManuscriptDialog.freeze` / `updateFreezeStatus`, `ManuscriptStoryPart.setFrozen`, the phone twins | During a story generation: exit button, the tabs of the window, the left bar except the story, the menu and the command bar are disabled, the cards are frozen, the action button becomes Stop (`activeGenerationToken`). Not a modal, a flag of that page. | Known to that one page only. The same book in a second browser tab, on the phone, or through a command is not frozen; `generateNextTurn` checks only the agent lock, so two generations on one book are possible. |
| Modal LLM windows | `SummaryDialog`, `CreateSummariesDialog`, `ResummarizeDialog`, `AnnotateDialog`, `RedraftDialog`, `ImpersonateDialog` (all `ModalityMode.STRICT`, each with its own `CancellationToken`) | The window is the lock and the progress. Closing it cancels the request (`AnnotateDialog`, `RedraftDialog`, `ImpersonateDialog` do so in their opened-change listener). | The answer exists only in the window: accept or lose it. The run dies with the page. |
| Automatic annotation | `AutoAnnotate` → `AnnotateDialog.openAuto` → `RedraftDialog.openAuto` → `finished(part)` | Chain after a generated part, through the same modal windows. `finished` is the hook four plugins decorate. | Bound to the page that generated; no state on the server. |
| Summary batches | `SummaryServiceImpl.createSummaries` / `resummarize` (virtual threads `summaries`, `resummarize`), `ResummaryRun` keeps an async job | Run on the server and write as they go. | The callback is the window; no "one per book" check apart from the agent lock. |
| Commands | `CommandServiceImpl` (pool `command-N`), `CommandProgressDialog` (strict modal, Stop), `StoryGenerationCommands` | A command can generate, summarise, annotate. | Same: no registry, a modal as lock. |
| Agent lock | `AgentRunService.checkNotLocked(book / lorebook)`, `ManuscriptLockedException`, `LorebookLockedException`, `FolderBookLockedException`, `AgentLockCheck` (UI) | Persistent (the status of the `AgentRun`). Checked in `ChatMessageServiceImpl`, `ManuscriptServiceImpl`, `ManuscriptReplaceServiceImpl`, `SummaryServiceImpl`, `RedraftServiceImpl`, `ImpersonateServiceImpl`, `StoryGenerationServiceImpl`, `LorebookServiceImpl`, `LorebookEntryServiceImpl`, `FolderServiceImpl`. The message names the agent. | Whole book only, agents only. |
| Job log | `Jobs` (static), `JobServiceImpl`, `JobRecorder`, `ActiveJobsGrid` (`/jobs`), `JobsPanel` | Every request to a model is a call of a job (kind, label, user, backend, model, times). `Jobs.active()` is in memory. | Metadata only and administrator only: no book, no part, no Stop, no text. Written at the end. |
| Which book a request is for | `UsageContext.open(book)` (thread local, read by the inference service) | Most callers open it. | Not opened by: the help assistant, `manga-generator`, `image-generator`, `sillytavern-converter`, the label model of `audiobook`. |
| Cap of requests at one AI | `AIGate` (`AI.maxConcurrentRequests`) | A FIFO of permits per AI. | Agents only. "Not applied to what the user generates by hand" is written into its contract. |
| One GPU for several extensions | `SharedGate` (core) / `GpuGate` (`libs/http-hooks`) | Modes, before/after hooks, `exclusive` in order, cancellable wait. | Speech and image backends only, not the language model. |
| Notifications | `NotificationService`, `DesktopNotifier(ui)` | The end of a run as a browser notification, per source, automatic or not. | Needs the `UI` of the page that started the run. |
| Push helpers | `UIPushGuard`, `ThreadAccessDialog`, `ProgressBarDialog`, `UIUtils` | Safe `ui.access` + push. | The throttling of live text is written three times in plugins (`RunWindow` 150 ms, the Knowledge Tracker's `UiAccess.coalesced`, the Audiobook), none in the core. |
| A fixed corner widget | `HelpAssistantWidget` (`CssStyles.HELP_ROOT`, a button fixed bottom right, a panel above it) | Shows that a fixed, non-modal widget in the shell works, also over dialogs. | - |

A worker keeps working when its page or session is gone: `ThreadCopyRequestAttributes` copies the session attributes
(`manual/developer/architecture.md`, *Threads and push*). So "survives the closed page" is a question of where the result goes,
not of the thread.

### Libraries

- **`translator-common`**: `PartRuns<R>` (one run per part, a `Set` of part uuids), `RangeRuns` (one range run per book, a `Set` of
  book uuids), `AsyncRuns`, `RunProgress` (bound to the worker thread, `ModelClient.ask` reports chunks and takes the stop),
  `RunWindow` (the strict modal with reasoning, answer and Stop; throttled), `RangeDialog`, `RangeLoop`.
  **The sets are fields of an object each plugin creates, and the library is a copy in each bundle.** So the Conlang Translator and
  the Real Language Translator do not see each other's runs, a part run does not see a range run of the same book (two different
  sets), and the core sees neither. `RunWindow` is used only by the Real Language Translator (three places); the Conlang Translator's
  runs have no window at all.
- **`http-hooks`**: `GpuGate`, `JobCalls.track` (a call of the job log).

### Plugins

| Plugin | Long operation | Thread | Window | Stop | Writes | Guard |
|---|---|---|---|---|---|---|
| `reallang-translator` | Translate a part / selection / speech; automatic after a part (`AutoAnnotate.finished`); range; word extraction (`ExtractionRuns`) | `PartRuns`, `RangeRuns` virtual threads | `RunWindow` strict modal; `RangeDialog`; `ExtractionPanel` | yes | the text of the part; glossary in the book | one per part, one range per book, `CHANGED` when the part differs afterwards |
| `conlang-translator` | Same shape, plus design / quick translate / import words (`AsyncRuns`) | same | no window for a part; `RangeDialog` | range only | text of the part; language data | same, its own sets |
| `knowledge-tracker` | Extraction series, tidy pass; automatic after a part | `ExtractionRun` virtual thread | **`ProgressPanel`: not modal, draggable, live reasoning and answer, log, Cancel, Review** | yes | book attributes | **`ExtractionRuns`: one per book, the latest kept for a tab opened later; a review waits for the user, the request scope is replaced by the one of the reviewing request** |
| `audiobook` | Conversion of parts to audio; automatic after a part; label model | `ConversionQueue` (one worker per user), `AudiobookService` | `StudioDialog`, `ConvertDialog` | per job, all | audio store, part attributes | **a real queue per user: jobs, cancel, retry, listeners, a job in the log**; GPU through `GpuGate` |
| `image-generator` | Prompt by the model, image by the backend | single thread executor of the dialog, idle scheduler | `ImageGeneratorDialog` (detach listener) | yes | part / book attributes | `GpuGate` |
| `manga-generator` | Script and tags per section, images | inference callbacks | `MangaDialog`, `MangaSectionCard` (`activeToken`) | yes | book attributes | none |
| `reviewer` | Review of a part / book | inference callback | `ReviewDialog` | yes | part and book attributes | none |
| `sidequery` | Chat beside the story, naming of a tab | inference callbacks | a tab of the story window (not a dialog) | yes | book attributes | none |
| `beta-readers` | Persona chats (`ChatRunner`) | inference callbacks | a view of the story window | yes | book attributes | none |
| `lore-generator` | Generate lore entries, commands | inference callback | `LoreGeneratorDialog` | yes | lorebook entries (on accept) | none |
| `arc-outline` | Generators, plot wizard, story import | inference callbacks | `GeneratorDialog` subclasses, `PlotWizardDialog`, `StoryImportDialog` | yes | book attributes | none |
| `book-description-generator` | Description of the book | its own `Thread` | `BookDescriptionDialog` | yes | book | none |
| `sillytavern-converter` | Conversion of a chat by the model | inference callback | `ConverterView` | yes | book attributes, lorebook | none |
| `character-tracker`, `authorsnote`, `story-notes`, `prompt-inspector`, `prompt-history` | No run of their own: they listen to generation events (`BEFORE_PREPARE_CONTENT`, `AFTER_PREPARE_PAYLOAD`, `BEFORE_INFERENCE`) | generation worker | `PromptInspectorDialog` (modal, a question inside a generation) | - | book attributes (`story-notes` under its own lock per book) | - |
| `anthropic`, `google-genai` | Providers: every request above goes through them | `CompletableFuture.runAsync` | - | token | usage | - |
| `chaptermarker`, `lorebookvcs`, `instructionexporter`, `name-*`, `place-provider`, `gemma4toolsfix`, `tts-reader`, `chineselocalization` | Nothing long | - | - | - | - | - |

A plain Vaadin `Dialog` is modal unless `setModal(false)` is called; only the Knowledge Tracker's panel and three popovers do.
Each plugin dialog is meant to stop its request when it is closed (the author; not read for every one, a dialog that does
not is a bug to fix on the way): `LoreGeneratorDialog`,
`GeneratorDialog`, `PlotWizardDialog`, `BookDescriptionDialog` and `ImageGeneratorDialog` have a listener for it.

**After every generated part** up to four plugins start a run of their own from the same hook (`AutoAnnotate.finished`: the two
translators, the Knowledge Tracker, the Audiobook), after the core's own annotation and redraft. Each starts its thread at once and
knows nothing of the others: on a model with one slot they queue up inside the server, with the user's next generation behind
them, and nothing on the page says so (except the one strict modal).

## What is missing

1. **No server-side knowledge of a run** (book, parts, who, what, how to stop it). The freeze, the windows and the sets are all
   local. Consequences today:
   - a second generation on the same book from another tab, the phone or a command;
   - delete, regenerate, merge or edit of a part a translation writes into (caught afterwards as `CHANGED`, the request is wasted);
   - delete or restore of a book, an import into it, a search and replace, a start of an agent, a cleanup purge while a run works;
   - a part run and a range run of the same plugin on the same part; the two translators on the same part (both rewrite its text);
   - an extension unloaded while its run works.
2. **No shared indicator.** Each run has its own window or none. A user cannot see "what is the model doing now" in one place.
3. **A result that only a window holds.** Annotate, redraft, summary of one part, impersonate, review, the generators: close the
   page and the answer is gone. Only the Knowledge Tracker lets a proposal wait.
4. **The story generation is tied to its page.** `StoryGenerationListener` creates and fills the card; the token is a field of the
   tab; the freeze is the lock. The pipeline itself saves the part as it goes (`InferenceStep`), so the text survives; what happens
   to the listener's `ui.access` calls after the page is gone, and whether `CLEANUP` still runs, must be read **(check)**.
5. **No queue for the language model.** `AIGate` is for agents. A one-slot server orders the requests itself, invisibly, and a
   long automatic run can stand in front of the generation the user is waiting for. And the other way round: a model with
   several connections is not used, because the page is frozen during a generation, so a side query or the arc generator cannot
   be started beside it (design 7).
6. **Whole-row saves.** Plugin data lives in `Manuscript.attributes` / `ChatMessage.attributes`, inside `extendedContent`. A save
   writes the whole JSON. There is no `@Version`. `ManuscriptService.update(book, change)` (read fresh and save in one write
   transaction) exists but has about twenty callers; most plugins do `find` + change + `save` under a lock of their own (two
   transactions, and the lock is the plugin's, not shared), and some core code saves a copy it has held for a while (for example
   `CleanupStep` saves the engine's `Manuscript`; the book window saves its own). Today the modals keep such writes mostly apart.
   **Every modal that goes away widens this window**: a run of plugin A can write back plugin B's data as it was a minute ago.
   `ChatMessage` has no `update` at all, and the translators and the redraft both write the text of a part.

## Design

### 1. Make the writes safe first

- `ChatMessageService.update(part, change)` like `ManuscriptService.update`; the same for `Lorebook` / `LorebookEntry` if a run
  writes them.
- Move the plugin stores (`StoryNotesService.update`, the Knowledge Tracker's, the Character Tracker's, Side Query, Arc Outline,
  Beta Readers, Reviewer, Manga, Author's Note, the translators) onto it, so the read-change-save is one write transaction (the
  write lock of SQLite is taken at its start, see `database.md`). Their own per-book locks can then go.
- Core code that saves a held copy while a run may be working (the generation steps, the book window's save, the mass dialogs)
  must change only its own fields through `update`. List them by searching `manuscriptService.save(` and `chatMessageService.save(`
  (about 30 files in the core, every plugin store).
- **No version column** (proposed, to be confirmed). A version counter on `Manuscript` and `ChatMessage` would make a stale
  save fail instead of overwriting, but it is the wrong tool here: it needs the only migration of this plan, and it does not
  repair anything, it turns every save that was not moved to `update` into an error in front of the author ("the book was
  changed meanwhile") in a place that has no way to recover. What removes the stale saves is the three things above it:
  `update` for every change of a field or a plugin's data, one window per book (3b), and the hold on a part while a run writes
  it (3a) followed by the page reading that part again when the run is done.
- **A detector instead, for development and the tests.** Every entity has `modification` (`BaseEntity`). The base `save` can
  compare the `modification` of the copy it is given with the stored one and, when the copy is older, log a warning with the
  entity and the caller: a stale save, somebody has to use `update` or read again first. No column, no migration, no failure
  for the user; in the tests it can be an error, so a new stale save does not get in unnoticed.

### 2. The registry of runs (core)

A singleton bean, in memory, in the application (not in a library: a library is a copy per bundle, the reason `SharedGate` lives in
the core). Proposed name `RunRegistry` in `domain.service.run`, with a static-free API a plugin gets by `@Autowired`.

A **run** has:

- identity: id, the user, the source (the id used for notifications, e.g. `reviewer.review`), a label supplier (localized when
  shown, never cached), `automatic`, the bundle that owns it (so an unload can stop its runs, as `NotificationService` does);
- **what it holds**: the book (uuid), and a scope: the whole book, a set of parts, "the end of the active branch" (a generation),
  or nothing (a chat beside the story); and a mode: *writes* or *only reads*;
- the AI it asks (for the queue, step 5);
- state: `QUEUED`, `WAITING_FOR_MODEL`, `RUNNING`, `WAITING_FOR_USER` (a review or an answer to accept), `DONE`, `FAILED`,
  `STOPPED`; started and ended times; a progress (n of m) when it has one;
- live text: the end of the reasoning and of the answer (capped, as the Knowledge Tracker's 6000 characters), fed by a listener;
- `stop()`; and for a finished run an optional *open* action (show the result, go to the part).

API sketch: `Run start(RunRequest)` (throws `RunConflictException` naming the run that is in the way, or returns a queued run when
the request allows waiting), `run.progress()` to bind to the worker thread (`RunProgress` of `translator-common` moves to the core
almost unchanged; `ModelClient.ask` and the core inference callbacks report to it), `run.finish(...)`, `List<Run> of(user)`,
`List<Run> of(book)`, `check(book, parts, operation)`, listeners per user.

**Relation to the job log.** One thing, two views: `start` opens the `Jobs.Handle` (so the job gets the book and the run id, and the
log needs no second bookkeeping), the registry is what the *user* sees while it runs, the log is what the *administrator* reads
afterwards. `/jobs` can show the book and offer Stop through the registry.

**Finished runs** stay in the list of their user until dismissed or for a while (the Knowledge Tracker keeps the latest per book).
In memory: a restart of the server loses the running and the waiting ones. That is the present behaviour of every run; a queue that
survives a restart is item 95 and builds on this.

### 3. Conflicts

The check is one general guard at the lowest level of every write, with the agent lock folded into it (see 3a), so a new
service method or a plugin cannot forget it. The exception names the run and is shown like `ManuscriptLockedException`
(with the same UI helper as `AgentLockCheck`), ideally with a *Stop it* button.

| Operation | Against a run that holds | Result |
|---|---|---|
| Start a generation | a generation on the book | refused |
| Start a generation | a run that writes parts of the active branch | refused, or queued behind it (setting of the run) |
| Regenerate / delete / merge / split / edit a part | a run that writes that part | refused, names the run |
| Edit a part | a run that only reads it | allowed; the run sees `CHANGED` afterwards as now |
| A range run | parts held by another writing run | refused for those parts or queued |
| Delete / restore / import into / replace in a book, start an agent, purge | any run of the book | refused |
| Unload an extension | its runs | stopped first (ask) |
| Two read-only runs (review, side query, description) | each other | allowed; they meet only at the model |

"Queued" needs the run to be describable as data that can wait (the wording of item 95). The translators' part runs, the range runs,
the summaries, the Knowledge Tracker and the Audiobook can; the windows that collect input and then show the answer cannot until
step 5 below.

The checks are by part uuid, so `PartRuns` / `RangeRuns` keep their API and ask the registry instead of their own set. That alone
fixes the two-translators and the part-versus-range cases.

### 3a. Where the locks live: general, at the lowest level, in memory (decided)

The question: should a lock be a general thing of every object (`OwnedEntity`), enforced at the lowest level, in the database,
with a forced unlock for one that is stuck?

**General and at the lowest level: yes.** Today the agent lock is checked by hand in each service method that changes something
(14 services), and a method that forgets it is not protected (the docs say so: "a change that a service does not route through
these methods is not locked"). The registry check of design 3 would inherit that weakness. One check in the base of every write
does not: `BaseServiceImpl` / `OwnedServiceImpl` `save`, `saveWithoutEvent`, `saveWithoutModification` and `delete` (and the
repository under them) are where every owned entity is written, plugins included, so "is this object held by somebody else?" is
asked there once, for all 24 owned entity classes, and a new service or plugin cannot forget it.

**In the database: no, and not as a field of the entity in any case.**

- *A column on the entity* (`lockedBy`, `lockedAt` on `OwnedEntity`) is the one variant that does not work here. A save writes
  the whole row from whatever copy the caller holds: a copy read before the lock was taken writes "not locked" back, a copy
  read during the lock writes it back after it was released. It is the stale-save problem of design 1 applied to the lock
  itself. It would also be a migration over every owned table.
- *A table of locks* (`locks`: type and id of the object, the run that holds it, the mode, since when) avoids that. But what
  would it store? Every holder is a thread of this one process (SQLite, one instance). After a restart every holder is dead and
  every stored lock is stale, so the first thing the application would do with the table is empty it. Persistence buys nothing
  and is exactly what makes a lock able to be **stuck**: a row that outlived the work it belonged to.
- So: **store the runs, derive the locks.** A lock is not a thing of its own but a statement about a run that is alive: "run R
  holds object X in mode M". That is already how the agents work (no lock is stored anywhere; `findLockingRun` asks which runs
  have a locking status), and it is the one case where the answer comes from the database, because an agent run really does
  survive a restart. The runs of this document live in memory, so their holds live in the registry; if runs become persistent
  one day (the queue of item 95), their holds follow from their rows the same way, with no lock table.

One service answers the question for both sources: `LockService.holder(entity, operation)` asks the registry and
`AgentRunService`, and the base `save` / `delete` call it. `ManuscriptLockedException` and its siblings become one exception that
names the holder (an agent or a run).

**Stuck locks, and the forced unlock.** A hold cannot be stuck apart from its run; a stuck hold is a stuck run, which is a
thing one can see and end:

- the holds are released in the `finally` of the run, with the connection and the job;
- the owner ends a run in the corner panel (Stop), the administrator ends any run in `/jobs` (*End*, new): ending a run releases
  what it held even when its thread does not answer (the thread is interrupted; if it writes later, it is no longer the holder
  and is refused like anybody else);
- the sweeper that ends a job without a call for `Jobs.IDLE_LIMIT` (30 minutes) ends its run with it;
- unloading an extension ends its runs; a restart ends all of them;
- for an agent the forced unlock exists: pause or stop the run.

That is the forced unlock, attached to the thing that is actually stuck. A separate *unlock this book* button would only be
needed for a lock that can exist without a run, which this design does not have.

What the general check needs, to be general:

- **Modes, not a flag.** From the table of design 6: *content* (the object is being written: nobody else saves or deletes it),
  *structure* (it must stay where and what it is: no delete, move, merge, split, but its text and its attributes may be
  saved), *read* (nothing is refused; a delete ends the run). The base `save` refuses against *content*, the base `delete`
  against *content* and *structure*.
- **Containers.** A hold on a book in mode *structure* must also stop the delete of the book's parts on the held branch, a hold
  on a lorebook covers its entries (as the agent lock walks the lorebook tree today). The check walks up: an entity tells its
  containers (a part: its book; an entry: its lorebook), default none.
- **The holder writes through its own hold.** The generation saves the part it holds. The check compares with the work on the
  current thread: the same thread scope that carries the connection of the AI (design 7), the job and the usage context. One
  scope, "the work this thread belongs to", instead of four thread locals.
- **What the lowest level cannot see.** It knows that an object is saved, not why. Operations that are more than a save (merge
  and split of parts, a move in the tree, the change of the active leaf, restore of a backup, import, search and replace, purge)
  keep an explicit check in their service, now one line against the same `LockService`. And the 58 bulk statements in the
  repositories (`executeUpdate`: folder moves, `lastOpened`, passages and revisions deleted with their part, statistics, the job
  log) pass under any check in `save`: each has to be looked at once; most touch tables nobody locks.
- **Reads are never refused**, and the interface asks the same service to show a held object as held (as `MassRenameDialog`
  greys out a book an agent works on, and `AgentLockCheck` says who holds it).

Cost compared with design 3 as first written: less, not more. The checks move from many service methods into one place, and
the agent lock is folded into it instead of living beside it. The careful part is the agent lock's existing behaviour (which
operations it refuses on a book, a lorebook and a folder), which must come out the same; its tests are the safety net.

### 3b. One window per book

**Proposed (by the author, and it fits): a book can be open for editing in one window only**, for the same user across all
their sessions: a second browser tab, a second browser, the phone. Opening it a second time is refused the way an
agent-locked book is refused today, by the same lock.

Why it fits:

- **The place exists.** Every screen that opens a book asks `AgentLockCheck.refuses(manuscript)` first (`ManuscriptPart`,
  `PhoneManuscriptPart`, `OrganiseDialog`; the windows themselves are made in four places, two of them the reopening after a
  restored backup). The same call asks the general lock service of 3a: the book is held by an agent, **or by a window**.
- **It is the same kind of holder.** The open window is one more holder in the registry, not a run: who (the user's page: which
  browser or device, since when), what (the book), a mode of its own, *open*. It is taken when the window is attached and given
  back when it is closed or its page is detached, the lifecycle `StoryActivity` already follows to tell the `ActivityTracker`
  that a book is on the screen (`BookPresence`, for the time of the writing statistics). Whether the window's hold is taken in
  that same place or beside it is a detail of the implementation.
- **A book is then held by one thing at a time:** a window or an agent. The agent already excludes the window; now the window
  excludes a second window, and starting an agent on a book that is open is refused or asks to close it **(check)** what
  starting an agent does today while the book is open.

What it takes off this plan:

- **Two windows saving the same book.** Each window keeps its own copy of the book and of the parts on its cards and saves from
  it. Two of them overwrite each other today, with no run involved at all: the most ordinary form of the stale save of design 1.
  With one window it cannot happen from the interface.
- **A second generation from another tab or the phone**, and Stop "from any page": there is no other page of that book.
- **The other pages of a book following the hold** (design 6a, 6b): the hold is shown in the one window that has the book.
  The per-user corner panel stays, because two tabs with two *different* books are still allowed and share the model.
- **Leaving and closing** (6d) are about one window, with no question which of several pages owns the generation.

What it does not take off:

- Writes that do not come from the book window: commands run elsewhere, an agent, the runs of plugins, the lorebook editor of
  the workspace (a lorebook can belong to several books), the mass dialogs (rename, move, delete, model and protocol, language,
  tags), the trash and the purge, a restore or an import from the list of books. The lowest-level check of 3a stays what
  protects the data; the window is only its most common holder. Against a hold in mode *open* the base check refuses what would
  pull the book from under the window (delete, restore, import into it, move to the trash) and lets saves through; the mass
  dialogs show such a book as held, as they show an agent's.
- The published reader (`Viewer`) and everything read-only: not a window in this sense, never refused.

**The stuck case is real here, and the forced unlock is "open it here".** A window is held by a page, and a page can die
without saying so (a crashed browser, a phone out of battery, a laptop that went to sleep): the book then stays held until the
page is detached, which can take the heartbeat timeout (five minutes or more). Waiting that long to open one's own book is not
acceptable, so the refusal offers the way out:

> *This book is open in another window (Firefox, since 14:02). Open it here and close it there?*

- Yes closes the other window through its own page (`ui.access`; `SessionManager` and `ApplicationPoint` already find the pages
  of a user): what is in an open editor there is saved first (the window's autosave), a generation running there is stopped as
  for any leaving (6d), and the question says so when one is running. The other page shows why its window closed.
- If that page does not answer (it is detached, or the access fails), the hold is simply dropped: a page that cannot be
  reached cannot write.
- No administrator is needed and there is no time limit to tune: the owner is the only one who can hold their own book.

Details to settle when it is built:

- **Restored tabs.** The workspace opens the book that was open last (`ManuscriptPart.restoreState` → `openManuscript`, behind
  the same `AgentLockCheck`). So a login on the phone while the desktop has the book open runs into the check by itself: there
  it must not offer to take the book over, it leaves the book closed and says where it is open.
- **The same page twice.** The check is by page, so a window reopened in the same page (after a restored backup,
  `ManuscriptBackupPart`) replaces its own hold.
- **Public API.** `AgentLockCheck` is `@Extendable` with `refuses(Manuscript)` since 1.4.0; the method stays and asks more (its
  locals change, so `PublicAPISignatureTest`), the message becomes general (*The book is held by …*), the agent's own text
  kept.
- **Lorebooks, folders.** The agent lock also covers the lorebooks of the book and blocks changes of its folder. The window
  does not: a lore edit beside an open book is wanted (and by design 6 even beside a running generation).

### 4. The panel in the corner

The indicator, as proposed: a small non-modal panel fixed in a corner of the shell, showing the current run with what the model is
thinking and a Stop button.

- **Where** (decided): in the shell (`Main`), like `HelpAssistantWidget`, so it is there on every screen and over dialogs. It
  starts **bottom left** (the help button owns the bottom right) and is **draggable**; where the author left it is remembered
  for the browser. On the phone a bar above the bottom navigation, not draggable.
- **Collapsed**: a chip with a spinner, the label of the run that has the model now and a count ("Translating part 14 · 2 waiting").
  Hidden when the user has no run.
- **Expanded**: the current run (book, what, since when, progress), the end of the reasoning and of the answer in two boxes that
  scroll to the bottom (exactly `RunWindow`'s content), **Stop**; below it the waiting runs in order (each with Stop / remove), and
  the finished ones that wait for the user (**Review**, **Show**, dismiss). *Go to the book* opens the story at the part.
- **Per user, not per page**: the panel of every open page of the user shows the same list (the registry tells its listeners;
  each panel updates through `ui.access` + `UIPushGuard`, throttled; one shared throttle helper in the core replaces the three
  copies). A page opened later shows the runs that are already going.
- **The notification at the end** is sent by the panel of a live page of the user (`ApplicationPoint` knows the open UIs), so it no
  longer needs the page that started the run.
- **No blocking variant** (decided): the strict modal of `RunWindow` goes away and gets no setting. The progress of a
  translation (reasoning, answer, Stop) is read in the panel entry, not modal, like any other run.
- What it replaces: `RunWindow` (removed; its content is the panel entry), the Knowledge Tracker's `ProgressPanel` (its log and Review move
  into the panel entry; or it stays as the *open* action of the run), the progress half of `RangeDialog` and `CreateSummariesDialog`
  / `ResummarizeDialog` once the run started.
- CSS in `shared-styles.css` + `CssStyles` (used by the core), scripts (scroll to bottom) in `JavascriptLibrary`, texts in `L` and
  the three bundles, `@Extendable` + `@PublicAPI` on the panel so plugins can add an action to an entry.

### 5. Runs whose answer is in a window

For each window decide one of:

- **A. Stays a modal conversation** (the user waits for the answer and reads it): impersonate, the prompt inspector's question,
  quick translate, the help assistant, the chats (Side Query, Beta Readers, plot wizard). They register a *read-only* run so that
  the panel and the model queue know about them; closing still stops them. Cheap.
- **B. Becomes a run with a result that waits**: annotate (manual and automatic), redraft, a summary, a review, the lore and
  outline generators, the book description. (Before this, see 5a: the same windows made non-modal give most of it for far less.)
  The window collects the input and starts the run; the answer is kept by the run
  (`WAITING_FOR_USER`) and the same window opens on it from the panel (**Review**). This is the Knowledge Tracker's `REVIEW` phase,
  made general. Each one is a refactor of a dialog that is `@Extendable` (public API: `PublicAPISignatureTest`, the plugins that
  decorate them).
- **C. Already writes as it goes**: translators, range runs, summary batches, the Knowledge Tracker in automatic mode, the
  Audiobook queue. Only the registration and the panel.

The automatic chain after a part (`AutoAnnotate` → annotate → redraft → `finished` → four plugins) becomes a sequence of runs of
kind B/C that the registry orders, instead of windows opened one after another by the page that generated. `finished` must keep
firing for the plugins that decorate it (it is public API since 1.7.0), or get a registry event as its successor with the decorator
kept for a version.

### 5a. The shortcut: the same windows, not modal

Kind B is the expensive way to free the page. Most of the benefit comes much cheaper: **leave the run in its window and make the
window non-modal**, so the author reads, edits and scrolls the story while the model annotates or redrafts. The run still lives
with the page (close the book and it stops, as now); what goes away is only the curtain.

Why it works here:

- The book window (`ManuscriptDialog`) is itself a strict modal `Dialog`. Vaadin makes a dialog opened over a modal one its child
  (*Server-Side Modality*, Vaadin 25): a modeless child is usable together with its parent, and closing the parent closes it, which
  fires the opened-change listener that already cancels the request. The Knowledge Tracker's `ProgressPanel` runs this way today.
- `AnnotateDialog` and `RedraftDialog` are not `@Extendable` and no plugin decorates them: no public API changes.
- **Annotate reads only** until *Add the selected annotations*, and a note is anchored by looking its quote up in the text as it
  is then (`AnnotateServiceImpl.annotateQuote`, `QuoteFinder`), so an edit made meanwhile costs at most a note whose quote is no
  longer found. A note may exist without the text it was about, and without its part (the author): nothing to guard.
- **Redraft reads only** until *Apply*. A redraft of a *passage* already refuses when the stored text is not the one the passage
  was selected in (`MSG_REDRAFT_SELECTION_CHANGED`). A redraft of the *whole part* has **no such check**: non-modal, it would
  replace what the author typed meanwhile (the part history keeps the old text, but silently). It needs the same comparison, with
  a choice (*the part changed: apply anyway / show the differences / discard*).

What has to be added, per window:

1. `setModal(false)`, draggable, resizable, a sensible start position (not over the middle of the story), and a **minimise**: the
   window folds into the corner panel (design 4) as an entry with the live text and Stop, and opens again from there. This is where
   the two ideas meet: the corner panel is the place the non-modal windows go when they are out of the way. Until the registry
   exists the panel can be fed by the page's own windows.
2. **The part is marked busy on its card** (a spinner and the name of the run) instead of the whole page being blocked. Actions
   that would pull the part from under the run are disabled on that card or ask first: regenerate, delete, merge, a second redraft
   of the same part. Editing stays allowed (the checks above cover it), except for a redraft that applies by itself (the automatic
   chain): there the editor of that part is held closed, or the automatic apply turns into "wait for the author" when the text
   changed.
3. **The generation and the windows**: starting a generation with such a window open on a part of the active branch is allowed
   for annotate (read only) and asks for a redraft; the freeze must not disable the Stop and Apply of an open window. On a model
   with one slot the two requests simply queue at the server until design 7 orders them.
4. A part that is gone when the answer arrives (deleted, regenerated): the window says so and closes without writing.
5. Keys: `DialogShortcuts` / `DialogFocus` work inside the window as before; check that the story's own shortcuts (new turn, stop)
   do the right thing while a window has the focus, and that <kbd>Esc</kbd> does not close a running window by accident
   (`setCloseOnEsc(false)` is set already).
6. **Phone**: a floating window makes no sense on a phone screen. There the window stays as it is and gets only the minimise (a bar
   above the navigation).

Which windows can go this way:

| Window | Until the user accepts it | Verdict |
|---|---|---|
| `AnnotateDialog` (one part, a range, automatic) | reads | yes, first |
| `RedraftDialog` (manual, automatic) | reads | yes, with the changed-text check for a whole part |
| `SummaryDialog` (one summary) | reads; the summary is stored when the model is done and was not stopped | yes; the parts it covers must not be deleted or merged meanwhile |
| `CreateSummariesDialog`, `ResummarizeDialog` | writes summaries as it goes | yes; deleting or merging the parts it covers must be refused meanwhile |
| `ImpersonateDialog` | reads, the answer goes into the new turn form | can stay modal: short, and the user waits for it |
| `reviewer` `ReviewDialog`, `book-description-generator`, `lore-generator`, `arc-outline` generators | read (lore: entries on accept) | yes; plain `Dialog`s, each plugin its own change |
| `image-generator` `ImageGeneratorDialog`, `manga-generator` `MangaDialog`, `audiobook` `StudioDialog` / `ConvertDialog` | their own data | yes; they are workbenches the author keeps open |
| `RunWindow`, `RangeDialog` (translators) | **write the text of the part** | yes, but these are the ones that need the busy mark and the refusals for real: a translation and an edit of the same part can not both win |
| `CommandProgressDialog` | whatever the command does | no: a command can do anything, it stays the lock |
| `prompt-inspector` | a question inside a generation | no |

What this shortcut does not give: a run that survives the closed book or the closed browser, the refusal of a second generation
from another tab or the phone, and anything across plugins (the two translators on one part). Those still need the registry. But
they are the rarer cases; the everyday complaint, "the page is blocked while the model thinks", is answered by this step.

### 5b. A second loop: what happens after the part

The generation loop ends with the stored part (`AFTER_CLEANUP`). What follows today is not a loop but a chain the *page* drives:

```
StoryGenerationListener.onComplete        (ui.access, the page that generated)
  -> Callbacks.partCompleted              (the desktop or the phone story tab)
    -> new AutoAnnotate(...).run          AnnotateDialog.openAuto -> RedraftDialog.openAuto  (strict modals)
      -> AutoAnnotate.finished(part)      an empty method, the hook
        -> 4 decorators                   conlang, reallang, knowledge tracker, audiobook
```

What is wrong with it, beyond the modals:

- **It runs only for a generation started on the page.** A generation by a command (`StoryGenerationCommands`) or by an agent never
  reaches `partCompleted`: no annotation, no translation, no knowledge, no audio for that part.
- **The plugins bind to a method of a UI class** and read its private fields by name (`manuscript`, `onChanged`) through
  `getReflectiveFieldValue`. `@PublicAPI` does not cover field names.
- **No order and no waiting.** The four decorators run in the order the extensions were loaded, and each only *starts* its run
  and returns (`runs.start(...)`, `queue.submit(...)`). So the translation that rewrites the speeches of the part, the Knowledge
  Tracker that reads the part and the Audiobook that speaks it all start together. What should happen (the author): the tracker
  reads the **untranslated** text, the audiobook speaks the **translated** one. The tracker gets that by construction: it reads
  through `PartTextRegistry`, where the translators turn their passages back into what the model reads, whenever it runs. The
  Audiobook does not use the registry, it reads the text as stored, so today it speaks whatever is there when its turn in the
  queue comes: translated or not is a matter of timing. With stages it is right by order: `MEDIA` comes after `TRANSFORM`.
- **Nothing can be stopped or seen as a whole**, and a closed window in the middle (`finished` "is not called when the author
  closes a window of the run") silently drops everything behind it.

**The proposal: a post-generation loop in the core**, started by the engine after `AFTER_CLEANUP` of a generation that completed
(not stopped, not failed), on the server, with no page needed. Plugins and the core's own processes **bind to a stage**.

*Stages*, in a fixed order, named after what they do to the part, so that what changes the text comes before what reads it:

| Stage | For | Bound today (as the hook) |
|---|---|---|
| `REVIEW` | notes on the new text | the core's automatic annotation |
| `REWRITE` | the text is written again | the core's automatic redraft |
| `TRANSFORM` | passages of the final text are replaced | the two translators |
| `EXTRACT` | data is read out of the final text | the Knowledge Tracker (and what comes: 91 *lore that keeps up*, a summary when a chapter closes) |
| `MEDIA` | what is made from the final text | the Audiobook (images later) |

*A binding*: the owner (the bundle, removed on unload like a notification source), a stable id and a name supplier (the same pair
as a `NotificationService.Source`, so one id serves both), the stage, the **mode**, and the handler. The handler is plain blocking
code on a worker thread with the request scope of the user: `void run(PostPart context) throws Exception`. The context gives the
book and the part **as stored when the handler starts**, the request type (new part or regenerated), a progress to report the
model's text to (`RunProgress`), `isStopped()`, `changed()` (the page should show the part again), and `skip(reason)`. A handler
that has nothing to do for this book (its automatic switch is off) returns at once; the switches stay where they are, in each
plugin's settings of the book.

*Modes*:

- **sequential**: runs alone, in the order of the bindings of the stage; the next one sees what it left. For everything that
  changes the text of the part, and for a step that depends on the one before it (the redraft on the notes).
- **parallel**: the parallel bindings of a stage start together, after its sequential ones, and the stage ends when all are done.
  For what reads the part and writes its own data. A parallel binding must not change the text of the part (enforced: its context
  has no write access to the part's text, or the save refuses it).

**Parallel on a model with one connection is sequential by itself**: the handlers do start together, but every request takes a
permit of its AI at the model gate (design 7), and an AI with `maxConcurrentRequests = 1` gives one at a time, in order. No special
case in the loop: two handlers that ask two different AIs, or one that asks the speech server, really run side by side; two that
ask the same one-slot model take turns. The loop only has to state the AI of a waiting handler so the panel can say "waiting for
the model". (An AI with no cap set is treated as unlimited today; a local server whose cap nobody typed in will get the requests
together and queue them itself. Reading the number from the server is item 94.)

*Rules of the loop*:

- One loop per part, a run of the registry that holds the part. **What it does to the next generation** (decided): a generation
  is refused while the text the model would read is being written again, and only then. So:
  - `REWRITE` (the redraft, automatic or by hand) **blocks** a new generation on that branch until it is applied or dropped.
    Blocked means refused with the name of the run, not queued behind it.
  - `REVIEW` (the annotation) **does not block**. A generation started while the notes are being made gets the part as it is,
    without what the notes would have led to; the redraft that follows the notes then changes a part that already has a
    successor. That is the author's doing (a mistake or an intention), as with any edit of an older part.
  - `TRANSFORM` (the translations) **does not block**: the model reads the part through `PartTextRegistry`, which gives it the
    untranslated text whether the translation is done, running or not started.
  - `EXTRACT` and `MEDIA` do not block.
  The part itself stays held for writing from `REVIEW` to `TRANSFORM` (no edit, delete or regenerate of it by somebody else).
  At the queue of the model the author's generation goes before the loop's requests.
- **Stop ends the loop. There is no resume and no "skip this step"** (decided). The author stops because something is wrong
  with the part (the redraft is not what was wanted), so what the later steps would have worked on is about to change anyway;
  a loop that picks up at "the last step" would have to know whether the text it stopped on is the one the author kept, fixed by
  hand, redrafted again or regenerated. The rule is therefore the present one, made explicit (`AutoAnnotate.finished` "is not
  called when the author closes a window of the run"): after a Stop the part is in the author's hands and the rest is done by
  hand.
  - What was applied before the Stop stays (notes that were added, a redraft that was applied, a translation that was written).
    The step that was running applies nothing (a redraft that is stopped leaves the text as it was; what arrived stays in its
    window, where the author can still edit and apply it, as now).
  - The steps that did not run are simply not run. The finished entry of the panel names them (*stopped at redraft; not run:
    translate, knowledge, audio*), so the author knows what is left. That list is information, not a button.
  - **Every binding must therefore have a manual action for one part**, or the author cannot finish by hand. They exist:
    *Annotate with AI* and *Redraft* on the card, the translators' actions on a part and their range window, the Knowledge
    Tracker's own run (it reads on from its cursor, so the part is picked up by its next run in any case), the Audiobook's
    studio. A new binding without a manual twin is refused in review.
  - Closing the window of a step that waits for the author without applying (a redraft shown for a decision) is the same Stop.
  - Regenerating or deleting the part ends the loop too (the `AFTER_CLEANUP` listeners `dropRegenerated` already take back what
    was made).
- **A failure is a Stop** (decided): a step that fails, sequential or parallel, ends the loop where it is, exactly as if the
  author had stopped it. The other steps of a parallel stage that are running are stopped with it; what was applied stays;
  the entry in the panel shows the error and the steps that did not run, and the rest is done by hand.
- **Modal or not.** The loop itself is never a modal: it is the entry in the panel, and the page stays usable under the rules of
  what holds the part. The author who wants to watch opens the step that is running from the entry and gets the same content as
  that step's own window (for the redraft: the text streaming in, the differences, the original with the notes), not modal, with
  the one Stop. No step opens a modal. On the phone the entry is the bar above the navigation and the step opens full screen.
- **In the panel** the loop is one entry: *After part 14: annotate ✓ · redraft … · translate · knowledge · audio*, with the live
  text of the step that has the model and one Stop.
- **Where it starts**: after a completed generation of the author, from the page or from the command bar (the second is new:
  today a generation by a command gets none of the automatic steps; assumed wanted, say if not). **Not for an agent** (decided):
  an agent changes parts by its own commands and does the steps the same way, when it judges them due, so the loop never runs
  for a generation of an agent run, and a binding needs a command for one part besides its action in the interface (annotate,
  redraft, the translators, the Knowledge Tracker and the Audiobook have commands). It is **not** started by hand either: with
  no resume, doing a step by hand means that step's own action, and a range stays with the range windows of the plugins.

*What it costs*:

- The core's annotation and redraft must run without their windows: `AnnotateService` and `RedraftService` do the work already and
  the windows only show it, so this is the kind B refactor for exactly these two, and it is needed for nothing else first.
- `AutoAnnotate.finished` is public API since 1.7.0. The four built-in plugins move to bindings (and stop reading private fields);
  the method is still called for a version, on the page if it is there, when the text-changing stages are over, and is marked as
  replaced in `extendable.md`.
- A new page in the developer manual (binding to a stage), `generation-pipeline.md` gets the second loop, the user manual a section
  on what happens after a part and in which order.
- It needs the registry (design 2) and the queue of the connections (design 7) to be worth anything; without them it is the present chain with
  an order.

#### Variant: the stages as steps of the existing engine

Instead of a loop of its own, the engine of the generation (`StoryGenerationServiceImpl.GenerationEngine`) can go on after
`CLEANUP`: new `GenerationStepType`s behind it (`REVIEW`, `REWRITE`, `TRANSFORM`, `EXTRACT`, `MEDIA`), each a `GenerationStep` bean
in `services-config.xml` that emits its `BEFORE_` / `AFTER_` events, and the plugins bind with what they use already,
`storyGenerationService.addEventListener(Events.X, listener)`.

What speaks for it:

- **The binding API exists and the plugins know it**: `GenerationEvent.onEvent(event, chain)`, where the listener holds the chain
  until it calls `chain.next()`. That *is* a sequential binding (the Prompt Inspector holds a generation open this way), with
  observers (`addEventObserver`) for the panel, and `generation-events.md` already documents it.
- **The handler has the generation in its hands**: the controller with the payload that was sent, the AI and protocol, the turn
  input, the request type, the `RUN_ID` and the `properties` the earlier listeners left. A translator or the tracker can use what
  the model was given instead of building it again.
- One engine, one timeline: a generation and what follows it are one thing in the log and in the panel.

What the engine assumes today and what would have to change:

1. **`CLEANUP` is the end and the place every stop and error jumps to** (`executeNextStep`: cancelled or failed → `jumpTo(CLEANUP)`),
   and `CleanupStep` on a cancel takes the new part away again. A Stop pressed while the translation runs must never get there.
   The engine needs a **second phase with its own cancellation token**: after `AFTER_CLEANUP` of a completed generation the first
   token is spent, a stop or a failure in the second phase ends that phase (or skips the step) and touches nothing of the part.
2. **The page is released in the middle.** `CleanupStep` calls `uiListener.onComplete`, which unfreezes the page and clears its
   token. That must stay exactly there: the second phase runs with the page free, and its Stop is the registry's, not the page's.
   The steps of the second phase must not use `getUIListener()` / `accessUI` as the first phase does, because the page may be
   gone; what they show goes through the registry.
3. **Only sequential.** Listeners of an event run one after another in the order they were registered (the order the extensions
   were loaded). A parallel binding is new: a registration with a mode, `addEventListener(event, listener, PARALLEL)`, for which
   the engine starts all of them and continues when each has called `next()`; and an order that does not depend on the loading
   (a priority, or simply the stages: few listeners share one).
4. **`AFTER_CLEANUP` is documented as the last event of every generation** and three plugins use it to take back what a
   regenerated part had (`dropRegenerated`). It stays where it is, for every outcome; the new events come after it and only for a
   completed generation. The wording of the docs changes, the behaviour of existing listeners does not.
5. **Without a generation there is no engine.** Running the steps by hand on a part or a range, or after an import, needs an entry
   that builds an engine for an existing part and `jumpTo`s the first step of the second phase, with no payload and no turn input:
   the handlers must cope with a controller that has only the book and the part.
6. The engine keeps the payload and its copy of the book alive for as long as the second phase takes (minutes, with audio longer),
   and that copy of the book goes stale while other things write: every step of the second phase reads the book and the part fresh.

Compared:

| | A loop of its own | Steps of the engine |
|---|---|---|
| Binding API | new (`register(stage, mode, handler)`), blocking handler | the existing events, `chain.next()` |
| Sequential | by the loop | there already |
| Parallel | by the loop | to be added to the event dispatch |
| Stop and failure | its own from the start | a second phase and token to be cut into the engine |
| Context of the generation (payload, properties) | not there unless handed over | there |
| By hand on a part or a range, after an import | natural | needs an engine without a generation |
| Risk for the generation itself | none, it is not touched | the engine's stop and error paths change |
| New things for a plugin author to learn | one small API | two events and a mode |

**Suggested: the engine, in two phases.** The plugins keep one way to bind (events), and the handlers get the generation's
context, which is worth more than the independence of a separate loop. The condition is point 1: the second phase is a clearly
separate part of the engine (its own token, its own error path, never `CLEANUP`), and the stages, the modes and the rules of the
loop are the ones described above. Point 5 was the argument for a loop of its own; with the decision that a stopped loop is not
resumed and the steps are not run by hand as a loop, it falls away: there is always a generation in front of the second phase.
Stop also becomes simple in the engine: cancel the token of the second phase, let the running step end without applying, emit
nothing more.

This also answers how the automatic chain is ordered (it was an open question): by stage, and inside a stage by binding; the
author switches steps on and off, not their order.

### 6. The story generation: blocking by part and by stage

**Decided:** the page is not blocked as a whole for as long as a generation runs. What is blocked follows from **which part is
being written** and **which stage** the generation is in. While the model is answering, the author can edit the lore, redraft or
annotate another part, ask a side query, as long as the AI has a connection for it (otherwise that request waits in the queue of
design 7, behind the generation). The same holds for a generation the author starts from the command bar.

Today the freeze is one boolean from the click to the end: `ManuscriptDialog.updateFreezeStatus` disables the exit button and the
tabs of the window and calls `setFrozen(frozen)` on every part of the window (story, lorebook, tree, prompt, backup, info, each
with its own implementation, desktop and phone); the story tab disables its left bar, menu, command bar and every card
(`StoryCards.setFrozen`). A generation from the command bar is blocked in another way again: `CommandProgressDialog`, a strict
modal over everything, and the new part is not shown streaming in.

What the stages need:

| Stage | What happens | Held | Free |
|---|---|---|---|
| **Prepare** (`PREPARE_GENERATION` .. `PREPARE_PAYLOAD`, with the listeners on its events: the trackers' blocks, the Prompt Inspector's question before the inference) | The prompt is built from the book, the lore, the summaries, the parts of the branch and the plugins' data. Seconds, unless the Prompt Inspector holds it open. | Everything, as today: the prompt should be built from one state of the book, and it is too short to be worth a finer rule. | - |
| **New part** (`GENERATE_NEW_MESSAGE`) | The part is created (or the old version kept for a regenerate), the active leaf moves. | as Prepare | - |
| **Inference** (`INFERENCE`) | The prompt is sent; only the new part is written, chunk by chunk. | The part that is written: edit, delete, regenerate, redraft, annotate, translate, merge, split. The shape of its branch: delete / merge / split / move of a part it descends from, a change of the active leaf (swipe, switch of branch). A second generation in the book. What replaces the book: delete, restore of a backup, import into it, start of an agent. | **The lore** (entries, activation settings). **Every other part**: edit its text, annotate, redraft, a summary. The prompt, the info and the settings of the book. The read-only tabs and the plugins' tabs (side query, arc outline, notes, trackers, beta readers). Exit from the book: asks and stops the generation (6d). |
| **Cleanup** (`CLEANUP`) | The part and the book are saved, a stopped part is taken away. Short. | as Inference | as Inference |
| **Second phase, `REVIEW` .. `TRANSFORM`** (design 5b) | Notes, redraft, translation of the new part. | The new part, for writing. The next generation on its branch **only during `REWRITE`** (the redraft). | everything else, as during Inference; a new generation during the annotation and the translation |
| **Second phase, `EXTRACT`, `MEDIA`** | The final text is read. | Delete / regenerate of the part ends the loop; nothing is refused. | everything, the next generation too |

**Changing what a later step will read is allowed, and is the author's business** (decided). The prompt of the generation is
fixed once the inference starts, so an edit of the lore or of an earlier part during the inference does not reach *this* part,
only what comes after. A later step (the annotation, the redraft, the tracker) reads the book, the lore and the parts **as they
are when that step starts**, never a copy taken when the generation began: so an old part changed meanwhile may give the analysis
a state the generated text did not know, and equally the author can quickly correct a lore entry before the analysis runs. No
check, no warning; each step of the second phase reads fresh (which the stale-copy point of design 5b asks for anyway).

How to build it:

- **6a. The hold instead of the boolean.** The generation publishes what it holds: the book, the part (and, for a regenerate, the
  version it replaces), the stage. The parts of the window get that instead of `setFrozen(true)`: a new method beside the old one
  (`setFrozen(boolean)` stays, it is `@PublicAPI` on `@Extendable` classes, and means "Prepare": everything). Each
  implementation decides from the stage: the lorebook, prompt and info parts are free from Inference on; the tree part shows but
  does not change the active leaf; the backup part stays held (a backup made now would contain half a part, a restore replaces
  the book). In the story, only the card of the held part is frozen; the cards it descends from lose their structural actions;
  the menu and the command bar stay enabled with the items that conflict disabled (new turn, mass delete, search and replace
  over the book, range runs that include the part). The action button stays Stop.
- **6b. The server refuses what the page should not have offered.** The page's disabling is a courtesy; the same table is the
  conflict table of design 3, checked at the lowest level (3a), so a command, an agent or a plugin
  cannot do what the first page greys out. Without the registry the finer blocking would rest on the page alone, which is why
  this comes after it.
- **6c. A command of the author blocks the same way.** `next` / regenerate from the command bar (also from a template or a batch
  of commands) is the same run with the same hold: the story tab shows the part streaming in and blocks by part and stage, and
  the second phase follows. The strict `CommandProgressDialog` is not shown for these commands (it stays for the others); the
  command still waits for the generation before the next command of a batch runs, and Stop is the generation's Stop. The page
  that ran the command has to become the listener of the generation: today the command has a listener of its own that only
  collects the result. The two already exclude each other in one direction (the freeze disables the command bar while the
  story generates); the other direction rests on the strict modal of the running command, which goes away for these commands,
  so the hold of the generation has to cover it (new turn and a second command refused while the command generates). A generation by an *agent* is
  not touched: it runs on the working copy, with the agent lock.
- **6d. Leaving the book, and a tab that is closed.**
  - **Leaving the book** (decided): the exit button is not disabled any more; pressed while a generation or a text-changing
    stage of the second phase runs, it says that this stops the generation and asks. Yes is the Stop the author knows: what
    arrived is kept as after any Stop, the second phase does not run. The same question for anything else in the application
    that takes the book off the screen (logout, opening another book in its place).
  - **Prepare** is held as a whole (decided), see the table.
  - **A closed tab: what happens today.** Nothing decides it; how it ends depends on the stage and on the next use of the page
    (the author's observation, and what the code says). `isDead()` of the generation is only its own token, so the request goes
    on. The next chunk is stored, then the page listener's `ui.access` meets a page that is gone; the exception lands in
    `invokeContinuation`, which calls the page listener again (`onError`) before it jumps to `CLEANUP`, so the cleanup may never
    run and the engine may be left with the request open and the part half written. A stage that does not touch the page gets
    through. A test that closes the UI in each stage belongs to the change, to pin down the new, deliberate path.
  - **A closed tab** (decided): **the same as leaving, it stops.** The story tab cancels the generation when its page is detached
    (a detach listener on the tab, or the registry when it sees the last page of the run go), through the normal Stop path:
    what arrived is kept, cleanup runs, the connection is given back, no second phase. Reasons:
    - one rule for every way of leaving, and the one already chosen for the exit button and for a stopped loop: the rest is
      done by hand;
    - a generation nobody watches cannot be stopped (until the panel of step 6 exists there is not even a place to see it),
      and on a model with one connection it would hold that connection for minutes;
    - the second phase may apply a redraft by itself: that should not happen to a part nobody has seen.
  - **The warning.** A page cannot ask its own question when the tab is closed; the browser only allows its generic *Leave
    site?* prompt (`beforeunload`). The page asks for it while a generation or a text-changing stage runs, and only then (the
    script in `JavascriptLibrary`). It also covers a reload and a typed address.
  - **What is not a closed tab.** A page is detached when the browser says it goes away, or when its heartbeats stop for the
    timeout of `SessionManager` (never under three heartbeat intervals, five minutes by default). A short loss of the network, a
    laptop lid closed for a minute, a phone that locks its screen for a moment come back to the same page: the generation is
    still there and the page shows what arrived meanwhile. So the rule costs a generation only when the page is really gone.
    **(check)** on a phone: whether switching to another app or the browser discarding a background tab reports the page as
    gone at once; if it does, the phone needs the timeout and not the browser's word.
  - **The other choice**, if losing the rest of a part to a closed tab turns out to hurt: let the *inference* finish and store
    the part, run **no** second phase, and show it as an ordinary finished part when the book is opened again. It needs the page
    listener to be optional in the engine and, to be stoppable, the panel. Not proposed now: it is the first piece of "a run
    that survives its page" and brings its questions with it.

### 7. One model, many runs

**The connections of an AI are a queue of their own, for every request, and the base of the rest.** It has nothing to do with the
loop after a part or with the registry: each AI has as many connections as its setting says, every request to it takes one and
gives it back, and what finds none waits in order. Everything above then needs no rule of its own about the model: a parallel
stage uses as many connections as there are, a side query runs beside a generation when a second connection is free and waits
when there is one, and a one-slot model serialises everything by itself.

Why it is needed (the case that shows it): with a model that has two or more connections, the author still cannot ask a side
query or use the arc generator while a part is generated. The model is not what stops them. Both are tabs of the left bar of the
story (`leftBar.add("sidequery", ...)`, `leftBar.add("arc-outline", ...)`), and the freeze disables every tab but the story
(`ManuscriptStoryPart.setFrozen`: `leftBar.setTabsEnabled(!frozen, STORY_TAB)`), together with the menu and the command bar. The
freeze blocks the page because nothing else orders the requests; once the connections do, the freeze only has to protect the
part that is being written.

What exists: `AIGate` / `AIGateImpl` is exactly this queue (by the uuid of the AI, so shared by all its users; first come first
served; a cancelled wait leaves the queue; the cap is read at every request), but only two callers take a place: the runner of
the agents (`AgentRunnerImpl`) and the commands an agent runs (`ManuscriptWriteSupport`).

What to do:

- **One place for every provider.** `InferenceServicesImpl.forAI` already wraps the service of any AI, the core's and an
  extension's, in `ObservedInferenceService` for the listeners. The gate goes into the same wrapper: `stream` / `infer` / `round`
  take a place before the request is sent and give it back when the callback ends (completion, cancel, error). No inference
  service and no plugin changes; `anthropic` and `google-genai` are covered. Counting tokens, listing models and the connection
  test take no place.
- **A request inside a request** happens, and on one GPU (confirmed by the author): an agent calls a tool, the tool asks the
  model. It works today because a place is one **request in flight**, not a conversation: the agent takes its place for one
  round and has given it back when the tool runs, and the command the tool runs takes a place of its own
  (`ManuscriptWriteSupport`, only for an agent run; it shows `MSG_COMMAND_WAITING_FOR_MODEL` while it waits, a text to reuse).
  Between two rounds of a tool loop the server is idle, so this is also what the server really does. The general gate keeps
  that meaning: **the place is given back while a tool is executed** and taken again for the next round, at the back of the
  queue like any request. In the core service that is one acquire per round. For the
  providers of extensions, which run their tool loop inside `stream`, the wrapper does it from outside: it wraps the functions
  it hands on, gives the place back when one is called and takes it again when it returns. So no provider changes, nothing
  waits for itself, and the two existing callers (the runner of the agents, the agents' commands) use the same gate instead of
  their own calls.
- **Waiting is not a timeout.** The time in the queue must not run against the timeout of the request, and Stop while waiting
  leaves the queue (the `CompletableFuture` of `acquire` does that already).
- **Order: a plain queue** (decided): first come first served per AI, one entry per request, no classes and no priority. It
  gives the turns by itself, because a job asks for its next request only when the one before is done: the next part of a
  range, the next round of a tool loop and the next step of the loop after a part each go to the back, so whoever waits gets
  the model in between, and a generation waits at most one request of each job that is running. That jobs interleave on one
  slot is not the queue's problem: a server with one GPU is meant to keep the prompt cache per conversation (the session header
  `X-Session-ID` and the slot kept on disk, *Prompt cache and session id* in the user manual), so a change of conversation
  costs a swap and not a reread.
- **Shared AIs.** The queue is per AI, across users. A user sees "waiting for the model, 2 before you", not whose requests they
  are. Between users it is the same plain queue; a per-user share can come later if needed.
- **The number: one connection unless the AI says more** (decided). `AI.maxConcurrentRequests` exists (field *Concurrent
  requests of agents* in the `AIDialog`): the label and the help become "connections", for everything, and an AI without a
  number has **1**, not "unlimited" as now. This changes existing AIs: a cloud model that ran several requests side by side
  (agents, parallel runs) is serialised until its owner raises the number, so it goes into the changelog and the dialog shows
  the 1 as the value, not as an empty field. Reading the number from the server stays item 94.
- **What it shows.** The state "waiting for the model" with the position, in the window that asked and, later, in the corner
  panel. Before the registry exists the wrapper can already tell the callback (a new default method of the callback, or a
  listener of `InferenceSession`), so each window can say "waiting" instead of looking stuck.

What follows from it at once, without the registry:

- **The freeze can let go of what only needs a connection**: the tabs of the left bar (side query, the arc outline, the notes,
  the trackers' views, beta readers), and with them the lore and the other parts. What stays held is the part that is being
  written and the shape of its branch. The whole rule, by part and by stage, is design 6.
- It depends on **safe writes** (design 1), and this is where that risk stops being theoretical: a side query saved while a part
  is generated writes the book's attributes, and the generation saves the book too. Every save on the generation's path must go
  through `update` first.
- The loop after a part (5b) needs no logic for "parallel becomes sequential", and the agents stop being a special case.

## Work per plugin

| Plugin | Work |
|---|---|
| `translator-common` | `PartRuns` / `RangeRuns` ask the registry; `RunProgress` comes from the core; `RunWindow` is removed, its content is the panel entry; `RangeDialog` starts a run and may be closed. |
| `reallang-translator`, `conlang-translator` | No modal for a translation any more; the automatic run after a part as a registry run; writes of the part through `ChatMessageService.update`; extraction and design runs registered. |
| `knowledge-tracker` | `ExtractionRuns` on top of the registry (it is the prototype: mostly deleting code); `ProgressPanel` becomes the panel entry; the store on `update`. |
| `audiobook` | The queue stays (it batches by speech model); each batch is a registry run so it is in the panel and conflicts with deleting its parts; the label model's requests get `UsageContext`. |
| `image-generator`, `manga-generator` | Runs registered (read-only on the story, write on their own data); `UsageContext`; stores on `update`. Images wait at `GpuGate` already: show that wait as `WAITING_FOR_MODEL`. |
| `reviewer`, `lore-generator`, `arc-outline`, `book-description-generator` | Kind B: the result waits. One dialog each; `arc-outline` has several generators on one base class (`GeneratorDialog`), so one change. |
| `sidequery`, `beta-readers`, `sillytavern-converter` | Kind A: register, nothing else. Stores on `update`. |
| `character-tracker`, `authorsnote`, `story-notes` | Stores on `update` only. |
| `prompt-inspector` | Its question holds a generation open: the run shows `WAITING_FOR_USER`. |
| `anthropic`, `google-genai` | Nothing, or the gate call (see 7). |
| the rest | Nothing. |

All built-in plugins are built from the same tree, so they move with the core. A third-party plugin that does not register keeps
working as today (its own window); it only is not seen by the registry.

## Order of work

1. ✅ **Safe writes** (design 1). Done: `ChatMessageService.update(part, PartChange)`; the stores of the built-in plugins (Arc
   Outline, Author's Note, Audiobook, Beta Readers, Character Tracker, Image Generator, Knowledge Tracker, Manga Generator,
   Reviewer, Side Query, SillyTavern Converter, Story Notes; the Story Notes lock is gone) and the core writers that saved a held
   copy (`deleteNodeAndMigrateChildren`, the mass model/protocol and language dialogs, the lore notes agent, the lorebook
   commands, `UserServiceImpl.changePassword`) on `update`; no version column; the detector `StaleSaves` in
   `JpaBaseRepository.write` (logs in production, fails the tests: `-Dtest.staleSaves`). It compares with the modification the
   last save of the row wrote, kept in memory, not with the stored one: a bulk touch (a saved part touches its book) is not a
   stale save. The full suite has no stale save left. Not done: the book window's `save()` and the other
   `refreshManuscript()` + change + `save()` places of its parts (they read fresh first, the gap is the user's click, not a
   run; they become `update` with the holds of step 4/5), `Lorebook` / `LorebookEntry` `update` (no run writes them yet), and
   the generation's own saves of the part it holds (the hold of step 4 protects them; a plugin that writes the part during the
   inference is reported by the detector).
2. ✅ **The connections of an AI as the queue of every request** (design 7). Done: `GatedInferenceService` in `forAI` around
   every provider (outside `ObservedInferenceService`); a plain queue per AI (`AIGateImpl`, the places are handed out outside
   its lock); one connection when the AI has no number (`AI.getConnections()`, the field *Connections*, required, shows 1);
   the place is given back before the caller's terminal callback and while a function the model called runs
   (`FunctionSet.around`); the methods never block: with no free place they return a token and send later from a virtual
   thread with the caller's request scope, `UsageContext`, `UnmeteredScope`, `InferenceSession` and job; `onWaiting(ahead)` on
   the callbacks (`0` when sent), shown by the story page (one notice) and the command bar; the agent runner and the agents'
   commands no longer take places themselves (the agent's pause still ends a wait for a connection at once, through
   `onWaiting`). `InferenceService.unwrap` (the provider plugins needed it already: `forAI` returns a wrapper). Not done:
   "waiting" in the other windows (annotate, redraft, summaries, the plugins' windows): they get `onWaiting` on their
   callbacks but show nothing yet; that is the corner panel of step 3/6. The freeze of the page is unchanged (step 5).
3. ✅ **Annotate and redraft not modal** (design 5a): the two windows, the changed-text check of a whole-part redraft, the busy mark
   on the card, the minimise into a first version of the corner panel (fed by the page). Done for `AnnotateDialog` (one part,
   a range, automatic) and `RedraftDialog` (whole part, passage, automatic): `FloatingWindow` (modeless, draggable, resizable,
   at the right on the desktop; strict on the phone; fold into the corner and back; stopped with the book window, also folded),
   `PageRuns` (the windows of the page, with their part ids) and `RunCorner` (bottom left in `Main`, *Open* / *Stop* / *Close*,
   above the strict book window like the help widget); the card badge and `StoryCard.getBusyWith()` turning off regenerate,
   swipe, redraft, annotate, merge, delete and redraft selection (the one public API: a default method and two `@PublicAPI`
   methods per card); the changed-text check of a whole-part redraft (*Apply anyway* / *Cancel*, the differences against the
   part as it is now; an automatic apply waits); a part deleted meanwhile is not written. Not done: the corner is not
   draggable and does not remember its place, it shows no live text (the window has it); a *merge* of the part after the busy
   one into it is not off (the deleted-part check covers what it can); the generation is not asked about an open redraft of
   the last part (point 3 of 5a; the changed-text check catches a *Continue* that changed it); the mass delete does not skip
   busy parts (same check). Then the other read-only windows of the table in 5a, core and plugins, one at a time (small
   each): `SummaryDialog`, `CreateSummariesDialog` / `ResummarizeDialog`, the plugins' windows.
4. ✅ **The registry + the general lock check** (design 2, 3, 3a): the guard in the base `save` / `delete` with modes and
   containers, the agent lock folded into it, the explicit checks of the compound operations, the bulk statements looked at;
   **one window per book** as the first holder (design 3b: the check when a book is opened, *open it here and close it there*);
   then the holds **for what already runs on the server** (translators through the library, Knowledge Tracker, summary
   batches, Audiobook batches, and the generation, which registers what it holds). Medium. From here on a second generation and a delete under a run are
   refused with a name; the busy mark of step 3 is read from the registry.
   Done: `Runs` (`domain.service.run`), **static** like `Jobs` and not a bean (the library is a copy per bundle without
   injection, and the base repository needs it); modes `WRITE`, `STRUCTURE`, `READ`, `OPEN`; operations `DELETE`, `REPLACE`,
   `GENERATE`, `OPEN`, `WRITE`; `startUnless` checks and starts in one step; `Runs.HeldException` extends
   `ManuscriptLockedException` and names the run (`ERROR_HELD_BY_RUN`). The guard is in `JpaBaseRepository.delete` and in a
   save of an entity marked deleted (the lowest level); the container is the book (a book is refused by any run of it, except
   the `OPEN` hold of the current page). Explicit checks: `ManuscriptServiceImpl.delete` and `restoreBackup`, `generateNextTurn`.
   Holders: the generation (its book `READ`, its new part `WRITE`, given back when `CLEANUP` starts), the window of a book
   (`BookWindows`, desktop and phone, *Open here*, the restored workspace never takes over, the statistics now ask the agent
   lock too), the redraft and annotation windows (`STRUCTURE`), `PartRuns` and `RangeRuns` of `translator-common` (`WRITE`;
   the word extraction `READ`), the summary batches and the resummary (`READ`); the cards read `Runs.busyWith`
   (`RunsOnPage.listen`); the mass delete skips held parts. Not done:
   - **saves are not refused**: a `WRITE` hold refuses a delete and a second writer, not a save by someone else, because the
     holder's own saves come from many threads (the inference callbacks) and telling them apart needs the thread scope of the
     work (3a, *the holder writes through its own hold*); with step 5;
   - **the agent lock is not folded in**: it stays a status in the database, asked by the services as before, and by
     `BookWindows.open` through `AgentLockCheck`; a check per write would be a query per write;
   - the Knowledge Tracker and the Audiobook are not registered (they only read the parts; their place is the panel, step 6);
   - starting an agent on a book open in a window is not refused (a scheduled agent would fail while the author has the book
     open; what it does today stays, 3b *(check)*); the imports into a book (`insertParts`, `importTree`) and search and replace
     do not check the runs; the bulk statements were not looked at one by one (the deletes of a book's parts go with the
     book, which is checked);
   - a run whose work never reaches its end (a step of the generation that never calls `next`) holds until a restart; there is
     no sweeper yet (with `/jobs`, step 6).
5. ✅ **Blocking by part and stage** (design 6a to 6c): the hold instead of the boolean in every part of the book window, desktop
   and phone; the conflict table in the services; the author's `next` / regenerate commands blocked the same way. Medium to
   large (many small places, the public API of the window's parts). This is where a side query, a lore edit or a redraft of
   another part beside a running generation becomes possible. With it (design 6d): leaving the book asks and stops, a closed
   tab stops through the normal Stop path, the browser's prompt while a generation runs.
   Done: the generation's run has a **stage** (`Runs.Stage` `PREPARE` / `INFERENCE`); when the `INFERENCE` step starts it holds the
   branch above its part in `STRUCTURE` and moves on. New operations `EDIT` (the text of a part by hand: refused by a generation's
   `WRITE`; a translation's is not), `MOVE` (merged away, a part inserted after it: refused by `WRITE` and `STRUCTURE`), `SWITCH`
   (the active leaf, refused during the inference). Checked in `ChatMessageServiceImpl.storeResponse` (so `updateResponse`,
   `storeResponse`, `replaceText`), `mergeIntoPrevious`, `insertPart` (so an import of parts before or after a part; `importTree` is not checked) and
   `ManuscriptServiceImpl.save` (a leaf that is not the written part: swipe, branch, a stale copy). The windows follow the
   registry (`RunsOnPage.listen`), whoever started the generation: `GenerationHold` to every tab (`setHold`, default
   `setFrozen(hold != null)`); info, prompt and lorebook free from the inference on, tree and backup held; the story: only the
   written card frozen, the others *kept in place* (`StoryCard.setKeptInPlace`: no delete, merge, branch), menu, command bar and
   left bar on, search and replace / import / mass delete off, the round button Stop (also for a generation the tab did not start).
   6d: the exit button asks and stops (`MSG_LEAVE_STOPS_GENERATION`), a window that leaves its page stops the generation the page
   started (`BookWindows.stopGenerationOf`, on detach: closed tab, heartbeat timeout), `JavascriptLibrary.warnOnLeave` while a
   generation runs. A logout needs no question: its button is in the workspace, behind the strict book window, so the book is
   left by its exit button first; a logout from another page (*log out everywhere*) detaches the page, which stops the
   generation as a closed tab does.
   6c, built afterwards: `@Command(progressInStory = true)` on `next` / `swipe` / `regenerate`; the story tab gives its command
   bar a `GenerationView` (`CommandBar.setGenerationView`), the bar runs the line in `CommandScopes.session(view)` and opens no
   `CommandProgressDialog` for these commands; the command asks the view for a listener (`show(book, request)`: the story gets
   ready on its page and freezes as for the round button) and tells it everything besides its own (the question is asked by the
   story); the run belongs to the bar's page (`GenerationOptions.withPage`), so a closed tab stops it; a refused start tells
   the view `onCancelled(null)`. The round button stops it, the command then ends with a `CancellationException` shown as
   *stopped*. While one runs the bar refuses a second (`ERROR_COMMAND_STILL_RUNNING`); other commands run. The command still
   holds its per-book gate, so a command from elsewhere waits as before. Test: `GenerationCommandsTest`. Not done:
   - **(check)** 6c in a real browser: the part streaming in, the round button's Stop and the result popover at the end;
   - between the start of the command and the start of its generation (the gate, a regenerate's question) the window is not
     held: a round-button generation started then wins, and the command is refused with the name of that run;

   - **saves are still not refused** (a plain `save` of a held part by someone else); the thread scope is still missing, and
     every by-hand path goes through a checked method now, so it has not been needed;
   - a stale copy of the book saved during the inference is refused (`SWITCH`), not repaired: the window's tabs read the book
     fresh before they save, so it should not happen from the interface;
   - the cleanup is held as the prepare (the run ends when `CLEANUP` starts, the window holds the whole book until the listener's
     `onComplete`): a fraction of a second;
   - the stage moves when the `INFERENCE` step starts, before its `BEFORE_INFERENCE` event: while the Prompt Inspector asks its
     question the window is free already (the table above puts that question in *Prepare*); the prompt is built by then, so
     nothing the author changes reaches it;
   - **(check)** on a phone, whether a browser in the background reports the page as gone at once (6d); not tested on a device.
6. ✅ **The panel on the registry**: per user instead of per page, Stop and the live text for every run, the queue of each model;
   notifications from the panel; `/jobs` shows the book. Medium. The strict modal of the Real Language Translator becomes a
   setting here (it writes the text of a part, so it waits for the refusals of step 4).
   Done: the strict modal is **removed, with no setting** (as design 4 and the decisions say; the line above was older). Each run has
   its text (`Runs.Text`: the end of the reasoning and of the answer, the place in the queue, the requests), fed by
   `GatedInferenceService` for every request made on a thread bound to the run (`run.bind()`, `Runs.current()`; the callbacks run
   with it bound, so the next request of a chain reports to it too); bound: the generation's steps, the summary batches and the
   resummary (which got their Stop through `setOnStop`), `PartRuns` / `RangeRuns`, the annotate and redraft windows when they start
   a request (`FloatingWindow.bind`), the Knowledge Tracker's `ExtractionRun` (now a run
   of the registry, `setWaitingForUser` while a proposal waits). `RunCorner` lists the runs of the user (`Runs.ofUser`, owner from the
   book or `Jobs.currentOwner()`) and the windows of the page: chip with the first run and the count, a list with book, state
   (waiting N before / thinking / writing / waits for you / stopping), time, the end of the text (throttled, `RunsOnPage.listenText`),
   Open / Stop / Close; draggable by a grip, the place in `localStorage`; a bar on the phone. A stop from another page reaches a
   window in its own page (`ui.access`). The end: `end(Outcome)`, `Builder.notifies`, the first live page of the user sends the
   notification when the run's page is gone (`claimNotice`); used by the resummary. `/jobs`: `RunsGrid` with user, book, label,
   source, state, since, holds, Stop and End (the sweeper by hand). `RunWindow` and its texts and styles are gone from
   `translator-common`; `new RunProgress()`. Not done:
   - **the Audiobook's batches** are not runs of the registry yet (the queue batches by speech model; each batch should be a run that
     holds its parts READ and binds its worker); so a conversion is not in the panel;
   - the panel has no **Review** / *go to the book* actions for a run that waits for the user (the Knowledge Tracker's proposal is
     reviewed in its tab), and no list of the finished runs: an entry goes when its run ends;
   - only the resummary declares a notification: the other runs that end after their page is gone (a translation, the tracker)
     have no source of their own, or one that is only for runs the user started (the tracker's);
   - `KnowledgeTracker`'s `ProgressPanel` stays (the panel shows the run as well);
   - the jobs of `/jobs` still do not name the book (the runs below them do); a job knows only the book id of its usage;
   - **(check)** the drag in a real browser with a book window open (the panel is in the top layer then).
7. ✅ **The loop after the part** (design 5b): stages and bindings in the core, annotation and redraft without their windows, the
   four plugins moved from the `AutoAnnotate.finished` decorator to bindings, one entry in the panel. Medium to large. For the
   author's generations, from the page or the command bar; not for agent runs.
   Done: `AfterPart` (stages, modes, `Binding`, a **blocking** `Handler` with `appliesTo`, `Context`: the book and part as stored,
   the generation's request / AI / protocol / input / properties, `onStop`, `await`, `changed`, `skip`, `waitForAuthor`, `stop`,
   `accessUI`), `StoryGenerationService.addAfterPartBinding`. `AfterPartPhase` is started by the engine after `AFTER_CLEANUP` of a
   completed generation (`startAfterPart`), as a phase of its own (own run `Runs.AFTER_PART`, own stop and end, never `CLEANUP`); one
   run holds the part `STRUCTURE` until the end of `TRANSFORM`, then `READ`; `setBlocksGeneration` during `REWRITE`; parallel steps
   on the engine's executor; a failure is a Stop; the "stopped at / failed at, not run: ..." message goes to `onWarning`; a
   regeneration of the part stops it; `GenerationOptions.afterPart` (`withoutAfterPart()` for an agent's command). `Runs`: child
   runs (`setAdopts`, `getParent`, `partOf`; text and queue forwarded, Stop passed on), `blocksGeneration`, `setOnOpen` (the
   panel's Review), `relabel`. `AutoAnnotateSteps` (the annotation in REVIEW, the redraft in REWRITE, without windows; a result to
   decide opens `AnnotateDialog.openResult` / `RedraftDialog.openResult` in the generating page and from Review, closing = Stop;
   an applied redraft resolves the notes it was given; a part edited meanwhile waits for the author). The tabs call
   `AutoAnnotate.finished` from `onPartFinal` (deprecated, with `run`). Plugins: both translators TRANSFORM sequential
   (`PartRuns.run`, `TranslationRuns.translateNow`), Knowledge Tracker EXTRACT parallel (waits for its `ExtractionRun`), Audiobook
   MEDIA parallel (waits for its job, Stop cancels it). Tests: `AfterPartTest`, `AutoAnnotateStepsTest`. Decided while building:
   - the binding API is a blocking handler, not the events with `chain.next()` the design suggested: parallel steps and the names
     in the panel needed a registration of their own anyway, and a blocking handler is simpler for a plugin;
   - the redraft blocks a new generation of the **book**, not only of the branch;
   - a model that finds no notes goes on (before, nothing followed: the translation and the audio were skipped);
   - an answer of the annotation that cannot be read is a failure (before, the window stayed open with it).
   Not done:
   - **(check)** in a real browser: the result windows opening in the generating page and from Review in another page;
   - the author's generation does not go before the phase's requests at the queue of the model (first come first served);
   - a parallel step is not prevented from writing the text of the part;
   - the redraft by hand does not block the next generation;
   - no list of finished phases in the panel (the message of a stopped one is a notification of the generating page only).
8. ✅ **Kind A registrations** (cheap, one by one). **Kind B** (the result waits without its window) for the remaining windows only
   where a closed page really loses something worth keeping. Each dialog is its own change, and after steps 3 and 7 it may turn
   out that few are needed.
   Done (kind A): `WindowRun` (`ui.widgets`, for plugins too): the request of a window as a run of the registry while the model
   answers, no holds, `quiet()`, the window's own stop run in its page (`start(stop)` / `start(stop, request)`, `start(null)` for no
   Stop), `finished()` from the end callbacks, ended with the window (closed or detached); `AsyncRuns` of `translator-common` binds
   `Runs.current()` to its worker. Registered: `ImpersonateDialog`, `HelpAssistantWidget` (through `start(stop, request)`, so
   `send()` keeps its `@PublicAPI`), the quick translation of both translators (no Stop: it can not be stopped), the Conlang
   designer's busy window (its Stop is *stop waiting*), Side Query's answer, the Beta Readers chat, the plot wizard. A question
   inside a run marks it `setWaitingForUser`: the Prompt Inspector's question on the generation's run (`Runs.current()` on the
   generation's thread), the plot wizard's approval of a command. Test: `WindowRunTest`. Decided:
   - **no kind B**: after steps 3 and 7 nothing a closed page loses is worth a result that waits without its window. The windows
     that write go on on the server already (translations, summary batches, the loop after the part); the read-only ones lose one
     answer the author asked for and can ask again. Take one up when an author misses it, one dialog at a time.
   Not done:
   - the other requests of these plugins are not registered: Side Query's tab name (the generators and the persona writer are,
     through their `FloatingWindow`);
   - built afterwards: the remaining windows of 5a are not modal (`FloatingWindow`, now public in `ui.widgets`): `SummaryDialog`
     (the part held `READ`; the summary stored with `ChatMessageService.update`), `CreateSummariesDialog` / `ResummarizeDialog`
     (the entry of the batch's own run, `follow("summaries")`), Reviewer, book description, lore generator, Arc Outline's
     `GeneratorDialog`, image and manga generators, the Beta Readers `PersonaDialog`; the audiobook `StudioDialog` /
     `ConvertDialog` only `MODELESS` (their queue never stopped with the window). **Close and Stop are apart** in all of them
     (also annotate and redraft): a close while the model works folds the window, the request goes on in the queue; *Close* in the
     panel (`closeForGood`) and the book window's detach stop it. A click on an entry of the panel opens its window or its Review.
     A window without parts is a run only while its model works. Test: `FloatingWindowTest`. Not done: **(check)** in a real
     browser (a folded dialog is detached: the plugins' `onDetach` now ask `isMinimised()`); the conlang designer, the translators'
     `RangeDialog` (already modeless with Close and Stop apart) and the kind-A chats are unchanged. A folded window ends with its page
     too (the `UI`'s detach), also when no book window owns it;
   - **(check)** in a real browser: the entries of these windows in the panel of another page, and the Stop from there.

Steps 1 to 3 are small and already help: the model's connections are used in order, and an annotation or a redraft no longer
blocks the writing. Steps 4 to 6 give what the item's text asks for (the registry, the refusals, the indicator, the modal as a
choice) and free the page during a generation. Step 7 puts everything that follows a generation in one ordered, visible place.
Step 8 is what "a run that survives the closed page" means for the windows that hold their answer; it can be taken one window
at a time or left out. The generation itself does not survive its page: leaving stops it.

## The browser check

The "(check)" notes of the steps above, as one session in a real browser. Tick a line when it works, or note what happened.

**Setup** (the screenshot environment, its own data and ports, nothing touches `~/.marginalia`):

```sh
mvn package -DskipTests                         # repo root, into target/ (the IDE closed: it shares target/); the WAR and every plugin
screenshots/bin/restore.sh 99-final --no-start  # the dataset: the book "Unregistered", lore, summaries, all plugins
screenshots/bin/refresh-plugins.sh              # the plugin JARs of this commit
screenshots/bin/start-llm.sh && screenshots/bin/start-app.sh
curl -s -X POST localhost:8098/_pace -d '{"delayMs":3000,"chunkDelayMs":250}'   # a slow model: about 3 s queued, then word by word
```

Then <http://localhost:8099>, `admin` / `marginalia`; a second tab (or a private window as `jana`) for the "other page" lines.
`curl -X POST localhost:8098/_pace -d '{}'` gives the normal pace back; `screenshots/bin/stop-app.sh` and `stop-llm.sh` at the end.

**The folding windows** (step 8, built afterwards; each: start the model, *Close* while it writes, see the entry in the panel,
click the entry, the window is back with the text going on; then *Stop* in the window: it stays with what arrived; then *Close*
it in the panel of a folded one: the model stops):

- [ ] annotate a part, and a range of parts; redraft a part, and a selected passage
- [ ] a summary of a part, and a meta summary; *Create summaries* and *Rewrite all summaries* (one entry each, not two)
- [ ] Reviewer; book description; lore generator; an Arc Outline generator
- [ ] image generator (the prompt being written, then the picture; *Accept* of a folded one); manga generator (continuous)
- [ ] Beta Readers persona writer; Audiobook Studio and Convert are only not modal (the page usable beside them)
- [ ] the plugin windows while folded really go on: their text is complete when opened again (a folded dialog is detached;
      the plugins' `onDetach` ask `isMinimised()`)
- [ ] a folded window and the exit button of the book window: the window ends, its model stops
- [ ] a folded window and a closed tab, without a book window (the lore generator from a lorebook, or *Create summaries* after
      an import) and with one: the run leaves the panel of the second tab, its model stops
- [ ] the windows stand at the right, can be moved and resized; on a narrow window (phone width) they are modal and full

**The panel of the runs** (step 6):

- [ ] the entries of these windows in the panel of the other tab, with the text going on; *Stop* from there stops the
      window's model in the first tab
- [ ] drag the panel by its grip with a book window open (the panel is in the top layer then); its place is kept on reload
- [ ] the model's queue: two requests at once (a summary and a redraft) show "waiting, 1 before"

**The generation** (steps 5 and 6c):

- [ ] while a part is generated: lore, prompt and info tabs usable, an earlier part can be annotated and redrafted, the new
      part and the ones before it can not be deleted or merged
- [ ] the exit button asks and stops; a reload asks (the browser's own question)
- [ ] `/next` in the command bar: the part streams into the story, the round button stops it, the result popover at the end;
      a second `/next` while it runs is refused, the text stays in the field

**What follows the part** (step 7, with automatic annotation on in the book):

- [ ] after a generated part the annotation and the redraft run as one entry; a result to decide opens its window in the
      generating page, and from *Review* in the panel of the other tab

**Not on this list:** a phone in the background (6d) needs a real device.

## Open questions

Decided so far, in the sections named: locks general, at the lowest level, in memory (3a); one window per book (3b); annotate
and redraft not modal (5a); the loop after the part as a second phase of the engine, its stages, Stop and failure end it, no
resume, not for agents, also for the author's commands, only the redraft blocks the next generation (5b); blocking by part and
stage, Prepare held whole, leaving and a closed tab stop the generation (6); the connections as the queue of every request,
one by default (7); a conflicting action by hand is refused, not queued.

Also decided: a plain queue per AI, first come first served (the prompt cache of a one-GPU server makes interleaving cheap);
no modal for a translation, its progress is in the panel; the panel starts bottom left and is draggable; for a shared AI it
says "waiting, 2 before you", without names.

Still open:

1. ~~**Versions** (design 1)~~: none, the stale-save detector instead (built with step 1).

Still to find out, by a test when the part is built: what a phone reports when its browser goes to the background (6d), and what
starting an agent does to a book that is open (3b).

## With every step

- **Tests**: the registry and the conflict table as plain unit tests; `MarginaliaTestBase` tests for the guard in the services
  (next to the agent lock tests); `MockLLMServer` held open to keep a run alive while a conflicting call is made; two threads
  writing two plugins' data into one book for step 1 (`ConcurrentTransactionsTest` is the model); `PublicAPISignatureTest` and
  `BundleCompletenessTest` whenever a dialog or a text changes.
- **Docs**: `manual/developer/architecture.md` (*Threads and push*), `services.md` (`Jobs`, `AIGate`, the new registry),
  `generation-pipeline.md`, `plugins/libraries/translator-common.md`, `plugins/notifications.md`, a new plugin page on registering
  a run, `plugins/extended-attributes.md` (use `update`); the user manual for the panel and the settings;
  `python3 tools/check_dev_docs.py`.
- **Changelog**: the panel and the refusals under *Added* / *Changed*; the API plugins see (`RunRegistry`, `ChatMessageService.update`,
  `RunProgress` moved, `AIGate` contract, any changed `@PublicAPI`) under *Changed*; the migration number if the version column is taken.
