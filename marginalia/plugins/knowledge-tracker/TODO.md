# Knowledge Tracker - TODO

## Goal

Characters must not be omniscient: the model often lets a character know things about characters they never met (their
job, name, appearance) unless the instructions guard against it. The plugin works out what each character of the scene
knows and does not know, and inserts it into the next prompt as an explicit block, for example:

    Fiora: has met Mara. Knows: Mara is a courier. Does not know: Thomas exists, anything about him.

Explicit "does not know" lines are what the model respects; a lorebook entry that mentions the fact does not need
to be changed (no core change is planned).

## Tracked characters

The plugin tracks **only the characters the user creates in it**, never everything it finds in the text:

- A tracked character has a **name** (matched, case-insensitively, against the POV / present characters of the parts
  and, in the extraction, against the text; optional aliases) and an **initial state** the user writes: what the
  character knows and does not know when it enters the book ("Mara: knows the city, knows Fiora is her sister; does not
  know Thomas exists"). A new character can be created when it is introduced, with the initial state of that moment.
- **Initial state is manual**: facts without a source message, in two kinds, *knows* and *does not know* (and
  optionally *has met* / *has not met* another tracked character). They are the base of the state, are never dropped
  by a reprocess and only the user edits them.
- Everything the processor learns from the story is added on top of the initial state, **for tracked characters only**
  (the subjects). The model for the extraction is told the list of tracked characters and their current state and may
  only report what *they* learned; what an untracked character learns is ignored, but untracked people may appear in
  the facts of a tracked one ("Fiora learned that Mara is a courier").
- **Only the subjects are tracked, not the people they know about.** The block is **closed-world** for every tracked
  character that is in the scene: it lists what the character knows (initial state + learned facts + the people it has
  met, by name) and says that the character knows nothing else, in particular has never met or heard of anyone not
  listed and cannot know their name, job or looks:

      Fiora knows: Mara (courier, her friend's sister); the Guild owes her money.
      Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.

  The plugin never has to know Thomas: he is excluded by not being on Fiora's list.
- **The initial state is the main tool.** The user writes it as free-text items, knows or does not know, and an item may
  be about a person who is not tracked: when creating Fiora they write "has not met Thomas", "does not know Thomas is
  her father", whether Thomas is tracked or not. The plugin does not parse the text and does not need a character
  called Thomas. The closed-world sentence is the safety net for what the user did not think of (a setting, on by
  default).
- **Items can become false.** When Fiora later meets Thomas, "has not met Thomas" must stop being injected, without
  being deleted (the user wrote it, and a reprocess from before the meeting must bring it back). An item therefore has
  a state per message: *active* or *retracted from message M*. See [Retracting items](#retracting-items).
- The people a tracked character has met are names, not tracked characters: they come from the POV / present fields of
  the parts (an untracked Mara can still be "met" by tracked Fiora). The characters that are in the current scene are
  met by definition and are listed as present.
- The user can create a tracked character from a name the picker offers (the cast of character-tracker, if installed,
  the POV / present names seen in the book), but the plugin does not depend on character-tracker and has its own list.

### Retracting items

An initial item (or a learned fact) is retracted when the story contradicts it. **The model does it**, in the same
request that extracts what was learned: it reads the messages with the active items (numbered) in front of it and
answers with two lists for each tracked character:

- **retract**: the items the messages contradict ("3: has not met Thomas" - they meet in the market);
- **add**: what the character *learned in these messages*, and only that: what it saw, heard or was told. Fiora meeting
  Thomas adds what she perceives (his name if he gives it, his looks, what he says to her), not what the reader knows
  about him and not what the narration says only the narrator knows. When in doubt, nothing is added.

Retractions and additions are state with a source message, like facts, so a reprocess from M undoes both for M and
later and the original item comes back. Nothing is deleted: the user wrote the items.

- The block shows only active items; retracted ones are [old knowledge](#old-knowledge), shown in the tab with the
  message that retracted them, and the user can restore one or retract one by hand (these hand changes have no source
  message and a reprocess leaves them).
- The prompt for the extraction must say explicitly: retract only when the text shows the contradiction, add only what
  the character perceived, never infer. It is the main thing to test against real models (see the open questions).
- No presence shortcut in the first version: a deterministic "retire an item when the two names are in the same scene"
  needs a subject field on every item and is a possible later optimization, not a requirement. Until the model step
  exists, retraction is by hand (Milestone 1).

### Old knowledge

Every item has one of two states: **active** (sent to the model) or **old** (kept and shown, never sent). Nothing the
plugin or the user ever wrote is thrown away:

- an item **retracted** by a change (model-proposed and applied, or by hand) becomes old, with the message that
  retracted it;
- an **addition the user rejects** in the review dialog (unchecks) is not applied as active but is kept as an old item
  (with its evidence and the message), so the model's idea is not lost;
- a retraction the user rejects simply leaves the item active.

The tab has an **Old knowledge** list per character, greyed, below the active items, **collapsed by default** (the
header shows how many old items there are; the open/closed state is a per-viewer convenience). Each old item can be **copied**
(the text to the clipboard), **edited** and **restored** (becomes active again, from now on), and deleted for good (the
only way to really remove something). Old items that came from processing carry their source message, so a reprocess
from M removes the old items made at M or later together with the active ones; items the user wrote or edited by
hand are not touched.

### The review dialog

What the model proposes is not applied silently: the processing of a batch produces a **set of changes** (a diff against
the state) and the plugin shows them in a dialog, a **table of changes grouped by tracked character**:

| | Column |
|---|---|
| ☑ | Apply this change (all checked by default) |
| Change | *Retract* (the old item struck through) or *Add* |
| Item | The text of the item; editable in place before applying |
| Evidence | A short quote from the message that shows it (the model must give one) and the message it is in; a click shows the message |

Per character: a heading with the name and the number of changes, a "check / uncheck all". Buttons: **Apply** (the checked
rows are applied, the cursor moves over the batch), **Cancel** (nothing is applied, the cursor stays; the same batch
is proposed again next time). A row that is unchecked is not applied; a rejected addition is kept as
[old knowledge](#old-knowledge), a rejected retraction leaves the item as it is. Neither is proposed again for these
messages (until a reprocess). The same table is used for a catch-up over many messages.

- **Ask or not: a checkbox of the auto mode, "Ask before applying"** (per book, and overridable for one run).
  Checked = **guided**: every run ends in the dialog. Unchecked = **purely automatic**: the changes are applied at once
  and only recorded in a log (the same table, read-only, with a "revert" per row). Guided is the default.
- **When it appears: never by itself.** The dialog is never a pop-up in the middle of reading or writing; the user
  opens it with a button of the [progress panel](#the-progress-panel) (and from the "N changes to review" badge of the
  tab). When the next generation needs the state (processing is just in time, see
  [Finality of the run](#finality-of-the-run)), the generation waits in the listener (a listener may call
  `chain.next()` later from another thread) with the panel showing "Waiting for your review" and the button; it goes
  on with the applied state when the dialog is applied, or with the state as it was when it is cancelled (or the panel
  is dismissed, which counts as Cancel).
- **No browser, no dialog:** a generation without a user interface (an agent, a command, a headless run) auto-applies,
  or skips processing; the commands `knowledge-process` / `knowledge-reprocess` give the changes back as JSON for an
  agent to apply with `apply-knowledge-changes`.
- A phone gets the same table in the layout of the phone UI (a list of cards instead of a table).
- The model must return, for each change, the evidence quote and the number of the message; a change without them is
  dropped, so every row can be checked.

### Runs and parts per run

Processing happens in **runs**. A run takes the next **N settled parts** after the cursor (see
[Finality of the run](#finality-of-the-run)) and handles them together in one request (split only when they do not fit
the context of the model), so the model sees a short stretch of the story at once and the review has one table per run.

- **N is configurable**: a default in the settings of the auto mode (default 1 for keeping up with the story as
  it is written, no upper limit; larger for a catch-up) and **per run**: "Process next N parts" in the tab, the `parts` argument of
  `knowledge-process` / `knowledge-reprocess`, and the same value for the runs started by a reprocess.
- A catch-up is a series of runs of N parts until the cursor reaches the last settled part. **Guided**: one dialog per
  run (the next run starts when it is applied; Cancel stops the series). **Automatic**: the runs follow each other
  without asking and the log is shown at the end.
- A bigger N is cheaper (fewer requests) but gives a longer answer and worse review; the tab shows the estimate (parts,
  tokens, requests) before a long series starts.
- Progress and cancel for a series in the [progress panel](#the-progress-panel); one series per book at a time.

## Present characters

The POV and present-characters fields of a part are free text (a text field, the character-tracker picker only appends
`, Name`; the default template labels the field "Characters Currently Present / State", so an entry may carry a note).
The block must contain **only the tracked characters that are in the scene**, so nothing extra goes to the model; the
fields are therefore parsed leniently (one parser, a pure function with its own unit tests):

- Text in parentheses (and brackets) is dropped: `Mara (injured, asleep), Thomas` → `Mara`, `Thomas`. Nested and
  unbalanced parentheses must not break it.
- Split on the **separators**, which are configurable (below). The defaults are `,` `;` newline, `/`, `|` and
  the Japanese / Chinese `、` `，` `；`. Words (` and `, ` & `) are not in the defaults, a name like "Jack and Jill"
  would break; whoever writes the lists that way adds them. Trim, drop empty items and a leading "the".
- **Separators are a setting at two levels:** the book (in the plugin's state, [Settings of the book](#settings-of-the-book))
  and the user (`UserSetting.getAttributes()` under the plugin's key, like the other plugins), the user value is the
  default for books that do not set their own, and the book overrides it. Stored as a list of strings (a literal
  separator or a whole word, matched case-insensitively between spaces); the tab and the user settings show them as a
  comma-free chip list or one per line, with "reset to default". The built-in default is used when neither is set.
- Compare case-insensitively, ignoring accents and extra spaces, against the tracked name and its aliases: whole
  entry equals the name/alias. A trailing note after a colon or dash (`Mara - wounded`) is cut.
- Not a match: a name that is only a substring of a longer entry ("Mara's brother" is not Mara); no fuzzy distance. A
  miss costs one missing block; a false hit sends a character to the model that is not there. Aliases are the way to
  widen the matching.
- A tracked character is **in the scene** when it matches the **present field** of the part the prompt is built for.
  The POV field is not read on its own: the author lists the POV character among the present ones anyway, and not
  reading it keeps one rule. A character that is not present is not listed at all, not even as "does not know".
- An entry that matches no tracked character is just a name met by the tracked ones (see "people met").

### The progress panel

While the plugin works (a run, a series, a reprocess) it shows a **non-modal progress panel**, so the user can keep
reading the story, editing a part or writing the next one while the model works:

- **Not modal, movable:** a small floating panel (no overlay, no focus trap, nothing behind it is blocked) that the user
  can **drag** to another place and collapse to a title bar; its position is a per-viewer convenience. Closing it does
  not stop the work (the work goes on in the background; the badge on the tab still shows the state); a **Cancel**
  button stops it.
- **Content:** what is happening (reading the lorebook, asking the model, parsing), the run it is on ("run 3 of 12",
  the parts), the elapsed time and the estimate of the tokens; for a series also the changes found so far. Errors of
  the model (an unparseable answer, a refused request) are shown here with a retry, never as a pop-up.
- **When it is done**, the panel turns into the result: "N changes found" and a **Review changes** button (no changes:
  "Nothing new" and it closes by itself after a moment). **The review dialog opens only when the user presses that
  button**, never automatically; guided and automatic mode both end in the panel, the automatic one with the log
  ("N changes applied", a link to the log) instead of the button.
- A guided series stops at each run until its review is applied or cancelled, then the panel continues with the next
  run.
- Without a browser (an agent, a command, a headless run) there is no panel, see below.
- The phone UI gets the same as a bar at the bottom, expandable (no dragging there).

## How it works (auto mode)

The plugin is a **processor that follows the story**. It reads the existing messages of the active branch from a
chosen point on, in order, and keeps a **state** of what each character knows. It runs by itself ("auto mode") and the
user can steer it:

- **Start point:** where processing begins. For a new book the start of the book; for an existing one the user picks a
  message ("start from here"), so a long book is not read from the first part. The default offered is "from the
  current end" (only new parts).
- **State:** one state per book, saved with the book (`Manuscript.attributes` under the plugin's key, so it is in
  backups). It holds the tracked characters with their initial state, the facts learned from the story and a
  **cursor** (the last message processed). Processing continues from the cursor.
- **Reprocess from a message:** the user tells the plugin to redo everything from message M on (UI and command). The
  facts that came from M and later are dropped, the cursor goes back to just before M, and processing runs again.
  Needed after the author edits or rewrites an earlier part, or when the extraction was wrong.
- **Processed list (info only):** the plugin may save which messages it processed (message uuid, hash of the text, time)
  and shows it to the user, including a hint "text changed since processed". Nothing is decided from this list; the
  cursor and the facts are what count.
- **Facts remember their source message** (uuid of the message they were learned in). That is not the processed list:
  it is what makes "reprocess from M" and a changed branch cheap (drop the facts whose source is M or after it, or not
  on the active branch), instead of wiping the whole state.
- **Branch changes:** if the cursor message is not on the active branch any more (swipe, regeneration, another
  branch), the plugin rewinds to the last message that is on both (drops the facts from the off-branch messages) and
  goes on from there. Switching back reprocesses; keeping a state per branch is not planned.
- **Injection:** the block (the state of the tracked characters that are present, see
  [Present characters](#present-characters)) reaches the prompt the **same two ways as the Author's Note**, the user
  picks either or both:
  - **Automatic insertion** in the payload (**Insert into prompt**, **Depth**, **Role**; `AFTER_PREPARE_PAYLOAD`,
    tokens reserved in `BEFORE_PREPARE_CONTENT`). The present characters are those of the turn being generated.
  - **Macro** `{{command::knowledge-tracker-block::<characters>}}` in the master template, user prompt or a lorebook
    entry: the block lands wherever the macro is. The command (`availableInMacros`, reads only) takes the **character
    list as its argument**, the raw text of the field, parsed by the same rules, for example
    `{{command::knowledge-tracker-block::{{presentCharacters}}}}`; so a template can pass any list (a different scene
    field, a hand-written `Mara; Fiora`). The argument is required (a command does not see the turn being
    generated). A literal `::` in a hand-written list would split it into several arguments and fail the call: use
    another separator there. The answer of a macro command is kept with the part the new part continues from, keyed by
    the command and its arguments, so a second generation from the same part (not a regenerate or a swipe, which ask
    again) does not see a state edit made between the two. Like the note, the macro prints the block whether or not **Insert into
    prompt** is on, so switch that off when the macro is used, or the model gets it twice; depth and role do not
    apply to the macro. An empty result (nobody tracked is present) prints nothing.
- **Commands** (command bar and agents; agents can drive the plugin by calling these by hand): see
  [Commands](#commands).

## Edited, redrafted and regenerated parts

Three different things can happen to a part the plugin has already processed:

| What happens | What the plugin does |
|---|---|
| The author **edits** it, or **redrafts** it (also the redraft of the automatic annotation, when it comes after the part was processed) | **Nothing automatic.** The part is now *stale*: its text differs from the text that was processed (the processed list keeps a hash, info only) and the tab shows "changed since processed" on it. What the knowledge should be is up to the user: they edit the items by hand, or press **Reprocess from here**. The plugin never reprocesses by itself because of an edit. |
| The author **regenerates** it (the same part, a new text) | The state that this part produced is **dropped** (facts, retractions, old items and rejected additions with this part as their source; the cursor goes back to just before it) and the new text is processed in its place, as if the old one had never been seen. Detected by the regenerate generation of a part that is at or before the cursor (`ORIGINAL_MESSAGE` is set in the events of a regenerate). |
| The author **swipes** or goes to another branch | The new part is a different message: the cursor is no longer on the active branch, so the plugin rewinds to the last message both branches share and continues from there (see [How it works](#how-it-works-auto-mode)). |

- With the finality rule the last part of the branch is normally not processed yet, so a regenerate of the last part
  usually has nothing to drop. It matters when the part was already processed (automatic annotation off, processed
  right after the generation, or processed by hand), and for a regenerate of an older part.
- A regenerate that is stopped or fails leaves the original text (the generation restores it): nothing is dropped
  then. Drop only when the generation produced a new text (`AFTER_CLEANUP`, `OUTCOME` `COMPLETED`, or `CANCELLED` with
  text).
- Items the user wrote or edited by hand are never dropped by this, as with a reprocess.

## Settings of the book

All saved with the book (in the plugin's state), edited in the plugin's tab in the story:

| Setting | Meaning |
|---|---|
| **Process new parts automatically** (auto mode) | On: the plugin processes the parts that were created, by the rule of [Finality of the run](#finality-of-the-run) (in the background right after a generation when it is settled, otherwise just before the next prompt is built). **Off: nothing runs by itself**; the user processes by hand ("Process next N parts" in the tab, `knowledge-process`), or not at all. The state that exists is still injected. |
| **Ask before applying** | Guided (review dialog per run) or purely automatic, see [Runs and parts per run](#runs-and-parts-per-run). |
| **Parts per run** | N, see [Runs and parts per run](#runs-and-parts-per-run). |
| **Start point** | The message processing begins at. |
| **Model** (and protocol) | For the extraction; empty = the book's model. |
| **Lorebook in the extraction** | *Resolve* (default): only the entries the parts of the run activate. *Whole*: the entire lorebook of the book and its sub lorebooks. Or *none*. See [Milestone 2](#milestone-2---llm-extraction-what-was-learned-hearsay). |
| **In prompt**, **Depth**, **Role** | Whether and where the block is inserted (like the Author's Note); the block is also available as the `{{command::knowledge-tracker-block::<characters>}}` macro, independent of this. |
| **Name separators** | How the present-characters field is split, see [Present characters](#present-characters); overrides the user's setting, empty = use it. |
| **Closed-world sentence** | The "knows nothing else" line of the block, on by default. |

The switch for the automatic processing is separate from "In prompt": a book can inject the state and not process
anything (everything by hand), or process and not inject (just to collect the state).

## Finality of the run

The plugin must process the **final** text of a part, never a draft the run is about to replace. `AFTER_CLEANUP` is not
final. The generation of a part is only the first link of a chain:

- With **Automatic annotation** on (`Manuscript.autoAnnotate*`, `ui.dialogs.story.AutoAnnotate`, started from
  `ManuscriptStoryPart.partCompleted` / `PhoneManuscriptStoryPart` in the browser session, after `AFTER_CLEANUP`), the
  annotation window opens, may add notes without asking and, when set, the **redraft** window rewrites the text of the
  part to address them. The windows may wait for the author; closing a window ends the chain and drops what would
  follow. This chain is UI-side and there is no server-side event for "the chain is over".
- A swipe / regeneration runs the whole chain again. The author can also edit the text by hand.
- A stopped generation leaves the partial text in the story; that is what the next prompt sees, so it is the final text
  of that part ("interrupted" counts as finished).
- Agents and commands that make many generations in a row are fine: they can call the plugin's commands by hand, and
  the rule below keeps the automatic mode out of their way.

Rule: **the cursor advances only over parts that are settled.** A part is settled when
- it is not the last part of the branch (something follows it, so nobody can still be redrafting it), or
- a new generation is about to start (starting a turn proves the chain of the last part is finished, closed or
  abandoned), or
- the book has automatic annotation off and the generation ended (`AFTER_CLEANUP` with `OUTCOME` `COMPLETED`, or
  `CANCELLED` with a part that has text).

So the normal path is **just in time, before the prompt is built** (before `BEFORE_PREPARE_CONTENT`): process every
settled part after the cursor, then build the block. It costs latency on the first generation after a part; show it in
the status of the generation, honour cancellation, and process in batches. With auto-annotation off the work happens
right after the generation instead, in the background. Never process while the generation of that part is still
running or while an annotation / redraft window of it is working.

If this latency turns out to hurt with automatic annotation on, ask for a small core signal "the auto-annotation chain
finished" (an `@Extendable` method with `@PublicAPI` or an event); also useful to other plugins. Not before.

One processing run per book at a time (serialize); a generation waits for it, it does not start another.

## Milestone 1 - state, cursor, "has not met" (no LLM): done

Done and checked in the running application (the Knowledge tab, the commands through the command line, a restart of the
application), with unit tests for the logic (`model`, `scene`, `block`, `process`) and a manual page
(`manual/user/extensions/knowledge-tracker.md`):

- [x] State in `Manuscript.attributes` (`KnowledgeState`): settings, tracked characters (name, aliases), items (knows /
      does not know; active or old; origin initial, manual or learned; source message), people met, start point,
      cursor, the processed list with the hash of each text.
- [x] The Knowledge tab: tracked characters, items, retract / restore / edit / delete / copy, the Old knowledge list,
      settings (in prompt, depth, role, closed world line, auto processing, separators of the book and of the user).
- [x] The lenient reading of the present characters field (`PresentCharacters`), separators per book and per user.
- [x] The block (`KnowledgeBlockBuilder`) with the closed world line; insertion into the payload with depth, role and
      reserved tokens; the `knowledge-tracker-block` macro.
- [x] Processing (`BranchProcessor`): the cursor kept in line with the active branch (swipe, other branch, deleted
      part), the start point, the settled-parts rule, the "has met" pass, reprocess from a part, parts whose text
      changed since they were read; a regenerate takes back the state of the replaced part (`AFTER_CLEANUP`);
      automatic processing just before the prompt (`BEFORE_PREPARE_CONTENT`).
- [x] Commands: status, tracked characters, knowledge, process, reprocess, set start, block. A command that asks the user
      must be `async = true` (it waits on a worker thread; synchronous it blocks the UI thread that would show the
      question).
- [x] Localization (English, Japanese, Chinese), user manual, CHANGELOG entry.

Not done from the first plan (not needed for Milestone 1, left for later): a unit test of the macro with the nested
`{{presentCharacters}}` argument (it works in the application: the block command answered with the right text for a list
passed by hand, but a template with the nested macro was not run), collapsing long lists in the block and a cap on its
length, the optional *about* name on an item (only if the model does not retract reliably), per-book processed list
paging in the tab (it shows the latest 30), the phone layout of the tab (not looked at).

## Milestone 2 - LLM extraction (what was learned, hearsay)

**Status:** the first version is built and was run through the real application with a fake model that gave a queued
answer (the Process now button, the non-modal progress panel, the review dialog, Apply, the stored result; the agent
commands) and the prompt was tested against a real model (see [To validate](#to-validate-with-real-models-before-milestone-2-is-built-on-top)).
What is in: `extract` (`ExtractionAnswer`, `ExtractionPrompt`, `ChangeSetBuilder`, `ChangeApplier`, `RunCommit`; pure, 18
tests), `ExtractionService` (model, context fitting, lorebook modes, propose / commit), `ExtractionRun` / `ExtractionRuns`
(a run on a worker thread, one per book), the progress panel and the review dialog, the settings, `knowledge-process` with
`ask`, `apply-knowledge-changes`. A bug found by running it, kept as a test: the cursor has to move *before* the changes are
applied, else its first step takes them back (`RunCommit`).

The **estimate before a long series** is in (`RunEstimate`, `KnowledgeProcessService.estimate`): *Process now* asks for a
confirmation with the parts, requests and story tokens when it would need more than 3 requests.

The **log of automatic runs with a revert per row** is in (`ChangeLog`, `ChangeLogEntry`, the *Applied automatically* section
of the tab; the last 200 changes; an item the user changed since is not touched).

**Run through the application with the real model** (2026-11-03, the user's `gemma-4-31b-it-MM`, the small Draconia story of the
screenshot data): *Process now* over 5 parts took 86 s and proposed two additions with correct quotes from part 5 (review, apply,
log). A generation with **Ask before applying** off took 3 s (it did not wait) and the part it produced was read by the model at its
end (`AutoAnnotate.finished`, a minute later: *Added to Draconia (Part 6)*). Found and fixed by this run: a review that waited
while the browser session ended could not be applied (*Scope 'session' is not active*): the worker now continues in the session of
the request that applies the review. Not tried: the hook with the automatic annotation and redraft switched on (the same
`finished` method ends that chain).

**Backfill from starting information** (2026-11-03, the same model and story): Draconia with two initial items (*Knows: Crabbe and
Goyle are her bodyguards*, *Does not know: Harry has been watching her or knows where she goes at night*), start point at part 2,
one part per run, applied at once. *Process now* asked for confirmation first (*4 parts in 4 requests*), the series took 119 s and
read parts 2-5 (part 1 was never read): at part 5 (Harry says he has seen the map) the model retracted the *does not know* item
and added what she learned; nothing was added for parts where she was not present or learned nothing. Result: 2 active, 1 old,
both changes in the log.

**Growth of the list, the relevance rule and the tidy pass** (2026-11-04). The user saw the review fill with trivia (*the elevator
stalled* for both characters, *she used nya comments*): the extraction prompt said to report everything a character gains, and the
model's own check of its rules found nothing wrong. Rule 7 (only what could matter later: secrets, identities, revealed feelings,
plans, promises, discoveries, what another character does not know; not events all present saw, small talk, mood, setting details;
at most 3 per character, usually 0 or 1) was probed on a scene with one important fact (the user's real model, 4 samples each): the
old prompt gave 4.8 additions per run (Peter above 2 additions in 2 of 4), the new one 1.0, the important fact (she is leaving) was
learned 4/4 in both, no shared event added. Second, the **tidy pass** (`TidyPrompt`, `TidyAnswer`, `TidyChangeSetBuilder`,
`ExtractionService.proposeTidy` / `commitTidy`, a run mode of `ExtractionRun`, the button *Tidy knowledge*, `knowledge-tidy`): one
request per tidy with the items the model learned and the user did not lock (numbered), the others shown for comparison only, and the
last N processed parts for context; the answer drops items or merges items into one (earliest source message, so a reprocess takes it
back); reviewed or logged with a revert. A tidied item is *old knowledge* (`OldReason.TIDIED`), never deleted. **Decisions:** only
`LEARNED` items are candidates (what the user wrote is never touched); the **lock** (`KnowledgeItem.locked`) keeps an item away from the
tidy only, the story can still contradict it (memory loss) while a betrayal stays; no hindsight rule ("not mentioned for N parts") because
a secret must outlive the recent parts; it is a pass the user starts (a button and a command), not part of every run. Regression of rule 7 (same model, 3 samples): the name and job, the letter, the memo plot, the private moments and the
whisper cases all came out as before (the known over-retraction case is unchanged). Tidy through the application with the real model
on seven items (the promise, the Aurors, three trivia, a duplicate, the fog): it dropped the five trivia / duplicate ones with a
reason each and kept the promise and the Aurors (74 s). **Automatic tidy** (`TidyTrigger`, `KnowledgeState.partsSinceTidy`, `ExtractionRun.tidyIfDue`): at the end of a run that is not guided,
when the setting is on and K parts were read since the last tidy (a tidy, even an empty one, starts the count again); applied and
logged; a guided run never starts one. **The model's writing is streamed** to the progress panel (`ExtractionService.Live`,
throttled to 3 updates a second) and every request's reasoning and answer is kept in the run (`Thought`) so that it can be read also
when nothing was proposed (*Last run* button). Checked with the real model (live text grows while it thinks) and the fake one
(reading adds 3, the tidy that follows retracts 1). Not done: a cap on the number of items in the block.

Left from this list: the **generation waiting for the review** (a guided run is started from the tab; before a prompt the
plugin reads with the model only when **Ask before applying** is off, else it only notes who has met whom), the
panel's position as a per-viewer convenience, and the phone layout.

- [x] Retraction and additions by the model in one answer per tracked character: the active items numbered in the
      request, the numbers to retract and the facts learned in the answer (JSON), each with its source message.
- [x] Processing step per batch of settled messages: the messages + POV + present characters + the list of tracked
      characters with their current knowledge in (the model may report only on tracked characters), JSON facts out ("Mara learned: Tomas is a smuggler, told by Ilse"), each with its source
      message. Updates the state instead of regenerating it.
- [x] **Extraction prompt content:** one request per run holds, in this order: the instructions (retract / add rules,
      JSON format), the **lorebook**, the tracked characters with their numbered active items, and the **messages** of
      the run (text + POV + present characters + number of each). The lorebook is there so the model knows who and what
      the names are (aliases, relations) and does not mistake an unnamed "the courier" for a stranger; it is *world*
      knowledge, and the instructions say that it is **not** what the characters know: a fact is added to a character
      only if the messages show that character perceiving it.
      - **Extract only what the messages teach, and omit what the character already has.** The instructions say, in
        these words or close: report only knowledge that the character *gains in these messages* (saw, heard, read,
        was told, worked out), and **leave out**
        - **global knowledge** that a member of that world has anyway (who the king is for John, who is a subject of
          the kingdom; common customs, geography of their home, the language), and
        - **what follows from the character itself** as the lorebook describes it (Lizzy is a succubus, so she knows
          she has a tail and what her kind can do; a character's own history, trade, family, abilities),
        - and anything the tracked state already holds (no duplicates; an item that says the same is not added again).
        Lorebook, tracked characters' initial state and the earlier items are there so the model can tell *new* from
        *already known*; an addition that only restates them is dropped. When in doubt whether the character
        already knew it, nothing is added. A fact that is new to this character *because it was told it* ("the king
        is dead", Peter reads the memo) is added, even if the lorebook has it: the lorebook is what is true, the
        item is that this character now knows it.
      - **Lorebook: resolve or whole** (a setting of the book, **default: resolve**). *Resolve* = only the entries that
        the parts of the run activate, as a generation would (the union over the parts of the run; filters, keys and
        sub lorebooks like `ImageGeneratorService.activatedLore`: `LorebookActivationService.collectLorebooks` /
        `evaluateAll` / `renderPayload`, matched against the text, instructions, scene setting, POV and present
        characters of the part). *Whole* = every entry of the book's lorebook and its sub lorebooks, as
        `ArcOutlineService` sends it. Resolve is cheaper and stays on topic; whole catches the entry no key activated
        (a secret kinship that no part of the run mentions by name) at the price of tokens, and may not fit a small
        context: if it does not, fall back to resolve for that run and say so in the tab; the entries that do not fit
        are counted and reported, as the image generator does.
      - The token count of the lorebook goes into the estimate before a long series and into the split of a run that
        does not fit the context of the model (fewer parts per request, the lorebook is kept).
- [x] Choose the model for the extraction (a cheaper one; a separate model matters on a single-slot local server, it
      evicts the prompt cache otherwise).
- [~] Review dialog: the table of changes per tracked character (apply checkbox, retract / add, editable text,
      evidence with the message), Apply / Cancel, the badge on the tab, review vs auto-apply setting with a read-only
      log and per-row revert; see [The review dialog](#the-review-dialog).
- [~] The generation waits for the dialog when it needs the processed state; Cancel lets it go on with the old state;
      no user interface = auto-apply or skip.
- [~] Runs: N parts per run as a setting of the auto mode and per run (tab, `parts` argument), series of runs for a
      catch-up, estimate before a long series, progress and cancel; see [Runs and parts per run](#runs-and-parts-per-run).
- [x] "Ask before applying" checkbox (guided / automatic), per book and for one run.
- [x] List of what each character knows, editable: delete / edit a learned fact, add one by hand (manual facts and the
      initial state have no source message and survive a reprocess). Review before a batch is applied: see the review dialog.
- [x] Private moments (thought, whisper, eavesdropping): the model decides who actually perceived it; the present
      characters are only the prior.

## Commands

For the command bar and for agents (registered with `commandService.registerCommands`, like
`CharacterTrackerCommands`):

- [x] `knowledge-status` - the settings and the state in short (the cursor, start point and the hints for changed
      messages come with the processing).
- [x] `knowledge-process` - process the settled messages after the cursor now (`parts`: how many per run; `ask`:
      override of the checkbox: `ask` true returns the numbered changes and holds them, `apply-knowledge-changes` applies the ones it is given). Async.
- [x] `apply-knowledge-changes` - apply a set of changes returned by `knowledge-process` (for agents). Milestone 2.
- [x] `knowledge-reprocess` (`parts`, `ask` as above) - from a given message (addressed the way the other message commands address a message,
      see `ManuscriptReadSupport`); `DESTRUCTIVE`-ish: it drops facts, so it asks the user. Needs the rewind wiring.
- [x] `knowledge-set-start` - set the start point (message addressing as above).
- [x] `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character`
      (name, aliases; addressed by name, alias or number, a name or alias already used by another is refused).
- [x] `list-knowledge` (per character: items by number, old ones marked, people met), `add-knowledge`,
      `retract-knowledge`, `restore-knowledge`, `delete-knowledge`. (One list command instead of list + get.)
- [x] `knowledge-tracker-block` (argument: the character list, required) - the block as text, for
      `{{command::knowledge-tracker-block::<characters>}}`; marked for macros.

## Decisions (the former open questions)

- **Parts per run:** default **1**, **no upper limit** (only the context of the model: a run that does not fit is
  split). A run never crosses an unsettled part (the finality rule already guarantees it).
- **Rewind:** dropping facts by their source message is the rewind; the processed list stays info only. The alternative
  (wipe and reprocess from the start point) is only what **Reprocess from the start point** does on request.
- **Branches:** one state per book, no state per branch. A branch change rewinds to the last message both branches share.
- **Regenerate:** the block for the prompt of a regenerate is built from the state up to the part *before* the
  regenerated one (the state of the part itself is excluded). A requirement of the block builder with its own test, not
  a question.
- **Introduced in:** no such field. A tracked character is tracked from the start point; one that has not appeared has
  nothing to learn, and the extraction ignores it. The initial state says what it knows when it enters.
- **Name picker:** offers the POV / present names seen in the book **and** the cast of character-tracker when it is
  installed. Looked up at runtime, no hard dependency (the plugin works without it, the picker only has fewer names).
- **A retracted item leaves no trace in the block;** the learned fact ("Fiora met Thomas, he is a smuggler") is enough.
- **Knowledge is per character, about anything.** An item is always *a character's* knowledge, but its subject is free:
  a person, a place, a secret, a plan, an event ("Peter knows about the plot to kill the duke", learned by reading a
  memo). The extraction adds such items too (what the character perceived, with the memo / message as the evidence).
  The closed-world line covers the **story-specific** knowledge of every kind (people, places, secrets, plans, events),
  not the general knowledge of the world (a character knows what a horse is, the street it lives on, unless the
  initial state says otherwise): "Peter knows nothing else about the people, places, secrets and events of this
  story." Its wording is tested (see below).
- **One level of knowing:** an item is text and nothing more. A rumour is written as a rumour ("Fiora heard that Thomas
  is a smuggler"); there is no knows / believes flag.
- **Partial text of a stopped generation is extracted** (it is the story as it stands).
- **Latency of just-in-time processing with automatic annotation on:** accepted for the first version, with progress in
  the generation status. The core signal "auto-annotation chain finished" is asked for only if it proves too slow (see
  [Finality of the run](#finality-of-the-run)).

## To validate with real models (before Milestone 2 is built on top)

These are tests, not design questions. Write a small set of test scenes and run them with the models in use.

**Results so far** (2026-11-03, the user's `gemma-4-31b-it-MM` behind llama-server, reasoning on by default, about 1000
tokens per answer; 3 to 4 samples per case, so these are signs, not rates; one model, one scene each):

- [x] **Closed-world sentence works.** Scene: Thomas, a stranger who is secretly Fiora's father, comes into the bakery;
      the lore gives his job and says nothing about whether they met. *No block:* 3 to 4 of 4 samples leak (Fiora
      "recognized him immediately", greets him as "Master Wren", the narration calls him "the royal tax assessor" in her
      point of view, she asks whether he is here for "the morning taxes"). *With a block* (explicit "has never met
      Thomas" items; or only the closed-world line with **no** Thomas items; or that without the "is currently with"
      line): 0 of 12 leak his name or job before he introduces himself. So the safety net alone is enough, and the
      "is currently with" line is harmless (no difference between E and F). The wording of the block stays as it is.
- [x] **Scope of the closed-world line is fine.** A walk through her own town with the line kept "the familiar scents of
      the town" (3 of 3); she is not a stranger in Ashby. No need to narrow the line.
- [~] **Extraction prototype works** (the prompt below; 15 requests, about 600 tokens, about 30 s each; every answer was
      valid JSON): *meeting* retracted "has never met Thomas" 3 of 3, added his name, job and looks, never the father
      item and never retracted the "does not know he is her father" item; *memo*: Peter learns the plot 3 of 3, Anna
      (who did not look up) nothing 3 of 3; *private muttering with the tracked character absent*: nothing for her
      3 of 3; *ordinary knowledge*: John (who saw the royal carriage) nothing, Lizzy never "has a tail", the Friday
      closing and the hired hall added 6 of 6. One false addition: "the crowd is feeling nervous hunger" (Lizzy's own
      empathy, rule 2) in 1 of 3 **with** the lore; none without it.
- [x] **Rates for the extraction prompt** (second round, same model, 6 samples per scene, 48 requests, scored by script;
      every answer was valid JSON; about 790 tokens and 39 s per run of one part): *meeting* 6/6 (retracts "has never met",
      adds name and job, no father leak, keeps the father item); *private muttering, tracked character absent* 6/6 nothing;
      *memo* 6/6 (Peter learns the plot, Anna nothing); *letter, whisper, sleeper, absent character* (5 tracked) 6/6 on all
      five checks (Fiora learns the letter and not the whisper, Mara hears the whisper, the sleeper and the absent one learn
      nothing).
- [~] **Over-retraction found, not fixed by the prompt:** in the scene where Fiora is only *told* who Thomas is, the model
      retracted the item "does not know who Thomas Wren is: she has never met or seen him" 6/6. Defensible (half of it is
      contradicted) but it throws away the half that stays true. A rule was added to the prompt (*an item that says more than
      one thing is retracted only when the messages contradict all of it*, now in `ExtractionPrompt`): 5/6 still retract, so
      it is noise, not a fix; the meeting scene (6/6) and the five-character scene (6/6 on all five checks) are unchanged.
      What helps is **one thought per item** (said in the manual) and the review: a retraction is shown and can be unchecked.
      Not tried: a separate pass that asks only about retractions, or a presence shortcut.
- [~] **Lorebook in the extraction** (resolve / whole / none), same scene, 6 samples each: the lorebook did **not** help the
      omission rule and made one thing worse: "the crowd feels nervous hunger" (the empathy of a succubus, which the lore
      describes, perceived in the scene) was added 2/6 with the resolved lore, 3/6 with the whole lorebook, 0/6 without. No
      mode leaked a world secret (the king's illness, the landlord's smuggling): 18/18. All three added the Friday closing 18/18
      and no one learned "the king is Alaric". So: the default stays *resolve* (the lore is there for names, aliases and
      relations, which this scene did not test), *whole* is not worse on secrets, *none* is the cheapest and was the cleanest.
      Left: a scene where the names need the lore (an alias, a title, "the courier").
- [ ] More of the same before relying on it: 8+ samples, a second model, an over-retraction scene (two characters of
      the same name, a scene where they only hear of each other), a long scene with several tracked characters, and the
      real request size (a full lorebook and 3 to 10 parts).

**The tested extraction prompt** (system message; the user message holds `[World]` (the lore, when sent),
`[Tracked characters]` with the numbered active items and whether each is present, and `[Messages]` numbered `#1`,
`#2` ... each with its POV and present characters). Keep it as the starting point of Milestone 2:

```
You keep track of what the characters of a story know. You get the world (lorebook, when given), the TRACKED characters
with the numbered items of what each knows or does not know now, and the new story MESSAGES (numbered). Report, for each
tracked character, only what changed in these messages.

Rules:
1. add: knowledge the character GAINS in these messages: what it saw, heard, read, was told or worked out itself. Not
   what the reader knows, not what only the narrator says, not what other characters know.
2. Do NOT add global knowledge that a member of that world has anyway (who the king is for a subject of the kingdom,
   common customs, the language, the geography of their own home), and nothing that follows from the character itself as
   the world text describes it (its species, body, abilities, history, family, trade).
3. Do NOT add anything the items of the character already say.
4. retract: the number of an item that the messages clearly contradict (for example "has never met X" after they meet).
   Only when the text shows the contradiction. An item saying a character does not know Y is retracted only if the
   character learns Y in these messages.
5. Never infer. When in doubt, report nothing. A character that is not present, or does not see or hear an event
   (private thoughts, whispers, a closed letter), learns nothing from it.
6. Every change needs an evidence quote copied from the messages and the number of the message.

Answer with JSON only, no other text: {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],
"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. A character with no changes has
empty lists.
```

Still open from the list above: the 8+ samples, the lorebook modes and the harder scenes (the last item of the results).
