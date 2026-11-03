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
      Fiora has never met or heard of anyone else in this story and does not know anything about them.

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
- **When it appears:** not as a pop-up in the middle of reading. The tab shows a badge "N changes to review"; the dialog
  opens from it, or by itself when the next generation needs the state (processing is just in time, see
  [Finality of the run](#finality-of-the-run)): the generation waits in the listener until the dialog is closed (a
  listener may call `chain.next()` later from another thread) and then goes on with the applied state. Cancel in that
  dialog lets the generation continue with the state as it was.
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

- **N is configurable**: a default in the settings of the auto mode (small, e.g. 1-3, for keeping up with the story as
  it is written; larger for a catch-up) and **per run**: "Process next N parts" in the tab, the `parts` argument of
  `knowledge-process` / `knowledge-reprocess`, and the same value for the runs started by a reprocess.
- A catch-up is a series of runs of N parts until the cursor reaches the last settled part. **Guided**: one dialog per
  run (the next run starts when it is applied; Cancel stops the series). **Automatic**: the runs follow each other
  without asking and the log is shown at the end.
- A bigger N is cheaper (fewer requests) but gives a longer answer and worse review; the tab shows the estimate (parts,
  tokens, requests) before a long series starts.
- Progress and cancel for a series; one series per book at a time.

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
    field, a hand-written `Mara; Fiora`). Without the argument it uses the present characters of the turn being
    generated, if the command can see it. Like the note, the macro prints the block whether or not **Insert into
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

## Milestone 1 - state, cursor, "has not met" (no LLM)

- [ ] State model in `Manuscript.attributes` (Gson, like `AuthorsNoteData`): tracked characters (name, aliases, initial
      state), start point, cursor, facts (character, text, knows / does not know, source message uuid or none for
      manual ones), "has met" pairs, auto-mode flag, processed list (info), injection settings.
- [ ] Tracked characters: list, add / edit / delete dialog (name, aliases, initial state as free-text knows / does not
      know items, each with an optional *about* name), in the story sidebar tab.
- [ ] State per item (active / old, with the message that made it old), manual retract / restore, the Old knowledge
      list per character with copy, edit, restore and delete; see [Old knowledge](#old-knowledge). (Retraction by the
      model is Milestone 2; until then a story where tracked characters meet people needs hand edits.)
- [ ] Optional pass without a model (the "has met" names only): tracked character A has met B when both were present (POV or present characters) in a
      part; B is any name, tracked or not. Read POV / present characters of each message from the cursor on. Writes into
      the same state.
- [ ] Name matching of the POV / present fields against the tracked names and aliases, see
      [Present characters](#present-characters).
- [ ] Rewind: reprocess from a message, the branch change rewind and the drop on a regenerate; see
      [Edited, redrafted and regenerated parts](#edited-redrafted-and-regenerated-parts).
- [ ] "Changed since processed" hint on the processed list (hash of the text), with **Reprocess from here**; no
      automatic reprocess for an edit or a redraft.
- [ ] Block builder: closed-world block for the tracked characters among the POV and the present characters: initial
      state + learned facts + the people met (names), then "knows nothing else, has never met or heard of anyone not
      listed" (collapse long lists, cap the length; wording tested against the models in use).
- [ ] Insertion in the payload with depth, role, in-prompt toggle; token reservation; count with the generation's model.
- [ ] Commands, see below. Macro through `knowledge-tracker-block` with the character list as the argument (the arguments
      arrive as a list split on `::`; the command joins all of them with a newline, so a `::` in the list is harmless;
      a nested `{{presentCharacters}}` is a property lookup evaluated as an argument of the command before it runs, read in
      `MacroTranslator.argExpr`/`subExpr`; not run yet, add a test in the style of `CommandMacroTest` that proves it).
- [ ] Settings of the book (see [Settings of the book](#settings-of-the-book)), saved in the state; the tab shows
      them, the toggle for the automatic processing among them.
- [ ] Story sidebar tab: auto mode on/off, start point, cursor and processed list, who has met whom, "reprocess from
      here" on a message, progress while processing.
- [ ] Localization class (`KnowledgeTrackerLocalization extends PluginLocalizationBundle`, ja and zh), user manual page
      in `manual/user/extensions`, CHANGELOG entry.

## Milestone 2 - LLM extraction (what was learned, hearsay)

- [ ] Retraction and additions by the model in one answer per tracked character: the active items numbered in the
      request, the numbers to retract and the facts learned in the answer (JSON), each with its source message.
- [ ] Processing step per batch of settled messages: the messages + POV + present characters + the list of tracked
      characters with their current knowledge in (the model may report only on tracked characters), JSON facts out ("Mara learned: Tomas is a smuggler, told by Ilse"), each with its source
      message. Updates the state instead of regenerating it.
- [ ] **Extraction prompt content:** one request per run holds, in this order: the instructions (retract / add rules,
      JSON format), the **lorebook**, the tracked characters with their numbered active items, and the **messages** of
      the run (text + POV + present characters + number of each). The lorebook is there so the model knows who and what
      the names are (aliases, relations) and does not mistake an unnamed "the courier" for a stranger; it is *world*
      knowledge, and the instructions say that it is **not** what the characters know: a fact is added to a character
      only if the messages show that character perceiving it.
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
- [ ] Choose the model for the extraction (a cheaper one; a separate model matters on a single-slot local server, it
      evicts the prompt cache otherwise).
- [ ] Review dialog: the table of changes per tracked character (apply checkbox, retract / add, editable text,
      evidence with the message), Apply / Cancel, the badge on the tab, review vs auto-apply setting with a read-only
      log and per-row revert; see [The review dialog](#the-review-dialog).
- [ ] The generation waits for the dialog when it needs the processed state; Cancel lets it go on with the old state;
      no user interface = auto-apply or skip.
- [ ] Runs: N parts per run as a setting of the auto mode and per run (tab, `parts` argument), series of runs for a
      catch-up, estimate before a long series, progress and cancel; see [Runs and parts per run](#runs-and-parts-per-run).
- [ ] "Ask before applying" checkbox (guided / automatic), per book and for one run.
- [ ] List of what each character knows, editable: delete / edit a learned fact, add one by hand (manual facts and the
      initial state have no source message and survive a reprocess). Review before a batch is applied: see the review dialog.
- [ ] Private moments (thought, whisper, eavesdropping): the model decides who actually perceived it; the present
      characters are only the prior.

## Commands

For the command bar and for agents (registered with `commandService.registerCommands`, like
`CharacterTrackerCommands`):

- [ ] `knowledge-status` - auto mode, start point, cursor, number of unprocessed settled messages, hints for changed
      messages. Reads only.
- [ ] `knowledge-process` - process the settled messages after the cursor now (`parts`: how many per run; `ask`:
      override of the checkbox, for an agent false returns or applies the changes).
- [ ] `apply-knowledge-changes` - apply a set of changes returned by `knowledge-process` (for agents).
- [ ] `knowledge-reprocess` (`parts`, `ask` as above) - from a given message (addressed the way the other message commands address a message,
      see `ManuscriptReadSupport`); `DESTRUCTIVE`-ish: it drops facts, so it asks the user.
- [ ] `knowledge-set-start` - set the start point.
- [ ] `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character`
      (name, aliases, initial state; addressed by name, like the character-tracker commands).
- [ ] `list-knowledge` / `get-knowledge` (per character), `add-knowledge`, `delete-knowledge`.
- [ ] `knowledge-tracker-block` (argument: the character list, optional) - the block as text, for
      `{{command::knowledge-tracker-block::<characters>}}`; marked for macros.

## Open questions

- Default N for keeping up with the story (1 part? a few?) and the upper limit; and should a run never cross a point
  where the active branch is not settled? (It does not by the finality rule.)

- Is dropping facts by their source message acceptable as the rewind, given that the processed list is info only? (The
  source tag on a fact is needed for it; the alternative is to wipe the whole state and reprocess from the start point.)
- Rewind on a branch change drops the facts of the other branch; keep a state per branch instead (heavier)?
- A regenerate drops the state of the part only after the new text exists. Between the start of the regenerate and its
  end the old state is still injected for the prompt of the regenerate itself (which is right: the state before the part
  is what that prompt needs; the state *of* the part has to be excluded from it). Check that the block for a
  regenerate is built from the state up to the part before the regenerated one.
- Does a tracked character start at the message where it is introduced (nothing is known before it, so the
  processor skips earlier messages for it), or is it tracked from the start point like everyone? Probably an optional
  "introduced in message" next to the initial state.
- Offer to import the names from character-tracker (if installed) when creating tracked characters, or leave it out?
- The closed-world sentence is what stops the omniscience; does the model respect it for characters that are in the
  lorebook / story text but not on the list? To be tested with real models (the user reports that models usually
  respect an explicit "doesn't know X"; the general form is untested).
- Will the model retract reliably ("has not met Thomas" after they meet) and not over-retract or over-add? Needs a set
  of test scenes with the models in use; if it does not work well enough, add the presence shortcut (optional *about*
  name on an item).
- Does a retracted item leave a trace in the block ("Fiora has met Thomas") or is the learned fact enough? Probably the
  learned fact only.
- Knowledge that is not about a person (a place, a secret, an event): the same closed-world rule for everything, or
  only for people? Only people is the original problem and a shorter block.
- Hearsay and rumours vs. certain knowledge: one level ("knows") or two ("believes" / "knows")?
- Latency of processing just in time with automatic annotation on: acceptable, or ask for the core signal?
- Extract from a **partial** text of a stopped generation? (Current idea: yes, it is the story as it stands.)
