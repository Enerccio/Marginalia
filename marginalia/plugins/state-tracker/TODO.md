# State Tracker - TODO

## Goal

The model forgets what a character wears, carries and holds: the sword lost in chapter 3 is drawn again in chapter 5,
the cloak soaked in the lake is dry in the next scene. The plugin keeps the **current state** of the characters the
user tracks, in **categories** the user defines (clothes, inventory, equipped weapon, injuries, mood...), follows the
story with the model to update it and puts the state of the characters in the scene into the prompt:

    Current state:
    Fiora - Wearing: torn green cloak, leather boots. Weapon: none. Carrying: rope, lantern.
    Mara - Wearing: guild courier uniform. Injuries: sprained left wrist.

It is the [Knowledge Tracker](../knowledge-tracker/TODO.md) applied to a different question: not "what has the character
learned" (a growing set of facts) but "what is true of the character now" (slots whose values the story replaces). The
machinery that follows the story is the same and is shared through a library (see
[Milestone 0](#milestone-0---the-shared-library)); this file describes only what differs.

Out of scope: numbers and arithmetic (HP, gold, stats). The model is unreliable at them and they serve role play more
than book writing; that would be another plugin.

## Categories

A category is what can be tracked about a character:

| Field | Meaning |
|---|---|
| **Name** | Shown in the tab and in the block ("Wearing", "Weapon", "Carrying") |
| **Kind** | How its value changes, see below |
| **Instruction** | For the model: what belongs in the category and what does not ("physical items carried on the body or in bags, not money, not what is worn"). The quality of the extraction depends on it; editable, shown in the category dialog |
| **Strict** | On: change it only when the text says so. Off: also obvious consequences (dove into the lake, so the clothes are wet). To be settled by the tests with the real model, see [To validate](#to-validate-with-real-models) |

Kinds:

| Kind | Example | Value | Changes the model can propose |
|---|---|---|---|
| **Single** | Equipped weapon, location, mount | one short value or nothing | *set* (replaces), *clear* |
| **List** | Inventory, injuries, active spells | a list of entries | *add* an entry, *remove* an entry |
| **Description** | Clothes, appearance, mood | one free text | *rewrite* |

### Where categories are defined

- **The user's categories** live in the user settings (`UserSetting.getAttributes()` under the plugin's key, like the
  separators of the Knowledge Tracker), edited in the plugin's user settings form. They are the catalog the user
  reuses across books. The plugin offers a few defaults on first use (clothes, inventory, equipped, injuries, mood),
  localized like the Knowledge Tracker's defaults.
- **Book-only categories** are created in the book's tab and exist in that book only (a magic system's attunement, a
  ship's cargo). A book-only category can be **saved to my categories** later.
- **The book keeps a copy** of every category it uses (name, kind, instruction, strict), with the uuid of the user
  category it came from. So the book is complete on its own: a backup, an export or another user opening it sees the
  same categories, and deleting a user category does not break any book. When the user's category changed after it was
  copied, the tab shows "differs from your category" with **Update from my categories** (nothing is updated silently:
  a changed instruction changes what the model does).

### Assigning categories to characters

- A tracked character (name, aliases, as in the Knowledge Tracker) gets the **categories tracked for it**: picked in the
  character dialog from the user's categories and the book's own. Only these are extracted and shown for it (the horse
  tracks injuries only).
- A **state item** is what the Knowledge Tracker's item is, with the category combo in place of *knows / does not
  know*: the item dialog has a combo of the character's categories and a text. For a *single* or *description*
  category the character has at most one active item (a new one replaces the old one, which becomes history); for a
  *list* category one item per entry.
- The **initial state** is what the user writes when they add the character (Fiora: wearing a green cloak; carrying
  rope, lantern). Unlike in the Knowledge Tracker, it is only a starting value: the story changes it, so extraction and
  the tidy may replace or remove it like any other value. The **lock** of an item is what protects it (see
  [The tidy](#the-tidy-transient-states)).
- Removing a category from a character keeps its items as history; adding it back brings nothing back by itself.

## State and history

- Every item has a source message (the message that set it, empty for what the user wrote), so reprocess from M, a
  regenerate and a branch change take back what M and later did, exactly as in the Knowledge Tracker.
- A replaced or removed value is not deleted: it becomes **history** (the Knowledge Tracker's *old knowledge*) with the
  message that ended it. The tab shows the history per category, collapsed: *Wearing: green cloak (from start) → nothing,
  bathing (msg 40) → borrowed robe (msg 44)*. Items in history can be copied, edited, restored, deleted for good.
- Characters that are not in the scene are not read: their state stays as it was until they appear again (absent for
  twenty chapters, still carrying the rope). The tidy is what ages it, if the user wants it to.

## Extraction

The same runs as the Knowledge Tracker (N settled parts per run, the cursor, guided or automatic, the review dialog, the
progress panel, the commands for agents). The request gets the present tracked characters, for each its categories
(name, kind, instruction, strict) with the current values numbered, and the parts of the run. The answer is a list of
changes per character and category:

- *set* / *clear* (single), *add* / *remove N* (list), *rewrite* (description);
- each with the **evidence** quote and the message number; a change without them is dropped;
- a category that does not change is not mentioned. Not changing is the default: the prompt says so first.

The review table is the Knowledge Tracker's, grouped by character, with the category and an **old → new** column
("sword → axe", "+ lantern", "- rope"); the new value is editable before applying.

Prompt rules to try (see [To validate](#to-validate-with-real-models)):

1. Report only changes of the listed categories of the listed characters.
2. Strict categories: only what the text states. Not strict: also the direct physical consequence of what the text
   states (wet after the lake, the sheathed sword is no longer in hand), never a guess about what happened off scene.
3. An action changes the state when it is done, not when it is planned or offered ("I will give you the key" is no
   change; handing it over is a *remove* for one and an *add* for the other).
4. Prefer short values in the words of the text.

## The tidy (transient states)

Some states fade without the text saying so (mood, out of breath, wet clothes, a bruise). There is no expiry timer:
the **tidy** handles them, as in the Knowledge Tracker, and it runs **regularly by default** (the automatic tidy every K
parts, K a setting of the book; and a button and a command for a tidy by hand).

- One request per character that was present in the last K parts: its unlocked items of all categories, numbered, the
  category definitions and the last K parts.
- The model answers per item: **keep**, **drop** (no longer true or no longer relevant: the anger of five scenes ago,
  "carrying a cup" long after the meal), **update** (the state moved on and the parts show it: the mood improved), or
  **merge** list entries (two entries for the same thing).
- What it drops or replaces goes to history (reason *tidied*), never deleted, reviewed or logged with a revert like any
  change.
- Locked items are skipped (the scar, the family sword: true until the story itself changes them; extraction can still
  change a locked item, as in the Knowledge Tracker).
- Characters absent in the last K parts are not tidied (nothing to judge from).

## Block

- Only the present tracked characters (the lenient reading of the present characters field, shared), only their
  categories with a value: `Name - Category: value. Category: entry, entry.`; an emptied single category prints `none`
  when the user wants it (a setting of the category, "show when empty", for the lost-weapon case), otherwise it is left
  out.
- Inserted like the Author's Note (in prompt, depth, role, reserved tokens) and/or by the macro
  `{{command::state-tracker-block::<characters>}}`, the same rules as the Knowledge Tracker's macro.

## Settings of the book

The Knowledge Tracker's (auto processing, ask before applying, parts per run, start point, model, lorebook in the
extraction, in prompt / depth / role, separators), plus the book-only categories and **Automatic tidy** (on by default)
with **every K parts**.

## Milestone 0 - the shared library

**First step done** (2026-11-16): `marginalia/libs/story-tracker-common` (package `extensions.storytracker`), embedded in the Knowledge
Tracker, with its tests (all 117 of the Knowledge Tracker pass unchanged):

- `process`: `Part`, `TextHash`, `RunEstimate`, `ProcessedPart`, `PartReader`, `StoryBranch` (reads the active branch), `TrackerProgress`
  (start point, cursor, processed parts, `rewind`, `read` = what a tracker notes without a model) and `BranchProcessor` over it.
  `KnowledgeState` implements `TrackerProgress`; its `read` is the "has met" pass.
- `scene`: `PresentCharacters`, `SeparatorText`. `block`: `PayloadInsertion`. `ui`: `UiAccess`.
- Developer manual: `manual/developer/plugins/libraries/story-tracker-common.md`.

**Second step done with Milestone 2** (2026-11-16): `llm` (`TrackerModels`, `ModelCall` with the optional reading of earlier parts,
`StoryLore`, `Live`, `LoreMode`), `run` (`TrackerRun` with the plugin's `Steps`, `TrackerRuns`), `ui` (`ProgressPanel` with the plugin's
texts, `ReviewDialog` with the plugin's rows, `StoryTrackerStyles` / `storytracker.css`, `StoryTrackerLocalization`), `process.Evidence`.
The Knowledge Tracker uses them (`KnowledgeSteps`, `KnowledgeRunScreens`; its `ExtractionRun(s)`, `ProgressPanel` and `ReviewDialog` are
gone); its tests pass and its review flow of the screenshots runs to the end. Not moved: `CharacterTrackerImport` (the State Tracker
has no import yet).

Also later, when both are installed: one request that does both extractions, to halve the waiting on a one-slot
server. The library should not make that impossible; not part of the first version.

## Milestones

1. **State without the model** (done 2026-11-16; tests of the model, the block and the localization; layout checked in the running
   application with a flow of the screenshot tooling; the user-level separators of the Knowledge Tracker are not here, only the book's): categories (user and book-only, the book's copies), tracked characters with their
   categories, items with the category combo, history, lock, the block and the macro, the tab, the commands (status,
   characters, state, block). Unit tests, manual page, CHANGELOG.
2. **Extraction** (done 2026-11-16): `extract` (`StatePrompt`: the present tracked characters with their categories, kind, strict and
   instruction, the current values numbered through the request; `StateAnswer`; `StateChangeBuilder`: set / clear for single and
   description, add / remove for lists, a value it has already, a remove of another character's or category's value, a message that
   does not exist and a change without a quote are dropped, a quote not found in the part is flagged; `StateApplier`, which records the
   part on the value it sets and on the value it ends), `StateBook` is the `TrackerProgress` (a rewind deletes what was read in the
   dropped parts and makes current again what they ended; a value the user corrects is the user's), `StateProcessService`,
   `StateExtractionService`, `StateSteps`, `StateRunScreens`, the Processing panel of the tab (process now / next part, start point,
   processed parts with Reprocess from here, the run and its review, a held proposal of a command), the settings (automatic,
   ask before applying, parts per run, lorebook, model, protocol), the after-part step and the reading before the prompt (only when
   the book applies without asking), the regenerate drop, the commands `state-process`, `apply-state-changes`, `state-reprocess`,
   `state-set-start`. Tested by `ExtractionTest` (9) and in the running application with the fake model (the review, an unchecked
   change, a quote that is not in the part, the part markers). Not done: no log with revert of automatic changes (the history shows the part),
   no desktop notification when a run of the tab ends, the model does not read earlier parts.
3. **Tidy** (done 2026-11-16): `TidyPrompt` (the tracked characters present in the last K processed parts, their unlocked current values
   numbered, the locked ones shown as "(stays)", the K parts), `TidyAnswer` (drop / update / merge per character), `TidyChangeBuilder`
   (drop = clear or remove, update = set or replace, merge = replace of the first entry and remove of the others; a value of another
   character, named twice, locked, an update to the same text, a merge of fewer than two entries or across categories, no reason: dropped),
   `StateApplier` ends the values as `TIDIED` on the last processed part (a reprocess from it brings them back; a value locked or ended
   meanwhile is skipped). **Tidy the state** in the Processing panel, the settings *Tidy automatically* (on) and *Tidy after this many
   parts* (K = 10, also the parts the tidy reads), the command `state-tidy` (with `ask`, applied by `apply-state-changes`). Tested by
   `TidyTest` (8). Differs from the plan above: **one request** for all the present characters, not one per character (one wait on a
   one-slot server; the parts are the same for all); the automatic tidy runs, as in the Knowledge Tracker, only at the end of a run that
   applies **without asking** (a guided run never forces a second review), so with *Ask before applying* on (the default) the tidy is by
   hand. Not done: not checked in the running application yet.
4. **Log with revert** (done 2026-11-16): what is applied without a review (`logged`: runs with *Ask before applying* off, the automatic
   tidy, the commands without `ask`) goes to `StateBook.changeLog` (`StateLogEntry`: action, category, the value made and the value
   ended, the part, tidy or not) through the shared `storytracker.log` (`TrackerChangeLog`, also used by the Knowledge Tracker now) and is
   shown by the shared `ChangeLogPanel`. `StateLog.revert` is an unchecked change after the fact: the value made is deleted, the ended
   one restored, only while both are as the run left it (and a single category has no other value meanwhile). Tested by `StateLogTest`
   (6), the library's `TrackerChangeLogTest` (3). Checked in the running application with the flow `shoot-plugin-statetracker`
   (the log, the tidy review, the history, a revert). A value the tidy writes is marked `StateItem.fromTidy` and shown *by the
   tidy, part N* (an edit makes it the user's).
5. **Story clock** (done 2026-11-16): `model.StoryClock` in `StateBook` (on/off, the anchor, the time at the start point, a
   `ClockMark` per processed part: advance / day / date / flashback read by the model, *set* = a fixed point of the user), folded
   in the order of the processed parts into a `StoryTime` (day, `TimeOfDay`, date, approximate); `StateBook.rewind` drops the
   model's marks of the dropped parts, fixed points stay. The request: `[Clock]` section and the clock rules in
   `StateDefaults.extractionSystem(true)` (the tested merged wording plus the rule "*day* only when counted from the anchor"; since
   milestone 6 a separate `clockNote()` added after the extraction prompt),
   `StatePrompt.Request.clock` (a request is never empty while the clock is on); `StateAnswer.clock` (`ClockReport`),
   `ClockBuilder` (checks, `commit`), `ClockChange` rows in the review (group *Story time*), `Proposal.clock`. An advance with only
   a time of day is the same day when that time is later, else the next day (the code decides). Values remember
   `StateItem.storyDay`; the tidy request has the clock and *(set on day N, M days ago)*, its prompt a rule about it. The block
   starts with *Story time: ...*. UI: the settings, the story time line and the time of every processed part with a clock button
   (`ClockDialog`, the fixed point), `ClockText`. Command `set-story-time`; `state-status` shows the time; `apply-state-changes`
   numbers the clock entries after the changes. Tested by `StoryClockTest` (7), `ClockTest` (5), and in the running application
   with the flow `shoot-plugin-statetracker` (the review with the clock, the times of the parts, a fixed point kept by a
   reprocess). Not done: no fantasy calendar (a date is Gregorian), one timeline (two POV threads at different times are not
   handled), the clock is not in the change log (a mark is changed on its part).

6. **Prompts as settings, in every prompt language** (done 2026-11-17): `StateInherited` registers the *State extraction prompt* and
   the *State tidy prompt* as inherited settings (book, folder, the user level in the State Tracker section of the settings through
   `StateService.getUserPrompt`/`putUserPrompt`, then the built-in text); `StateExtractionService` resolves them per book.
   `StateDefaults` became `StateTrackerDefaults` (instance methods, `T.defaults()` picks the prompt language) with `JA`, `ZH`, `FR`,
   `ES`, `DE`, `IT` and `KO` subclasses; JSON keys and values, the kinds of category, `strict`, `(stays)`, the times of day and the
   headings of the user message stay English (`StateTrackerDefaultsTranslationTest`). The clock rules are no longer spliced into the
   extraction prompt (that could not work on a prompt of the user): `clockNote()` is added after whichever extraction prompt is used,
   with its own answer line ("put the clock into the same JSON answer, as the list `clock` next to `characters`"). Tested on the local
   model (Gemma 4 31B, `--max-tokens 32000`, 3 samples each, English): **A** (bread eaten, lantern handed over, a flashback, three days
   later, by nightfall) clock 3 of 3 (none, advance 1 morning, flashback, advance 3 night; once `nightfall`, which `TimeOfDay.parse`
   takes as evening), state 3 of 3 (bread and lantern removed from Fiora, lantern added to Mara, mood *relieved*/*cheerful*, nothing
   from the flashback); **C** (siege, 1453-04-06: all night, the twentieth of April, an hour later) 3 of 3 none / date 1453-04-20 /
   advance 0, the crossbow replaced by the spear; **D** (no tracked character: two days, the following morning, by evening) 3 of 3
   advance 2 / advance 1 morning / advance 0 evening, `characters` empty. Prompt 1160-1400 tokens, answer 467-3248 tokens, 24-166 s.
   The translations were not tried against a model.

## Milestone 5 - the story clock

Numbers were left out because the model is bad at keeping sums over many parts. Time is the exception worth having ("day 14
after the awakening", the date of a siege, how long ago a wound was taken), and it can avoid the problem: **the model only
reports what each part says about time, the code does the counting.**

- **One clock per book**, not per character (time is shared). It is a day counter from an **anchor** the user names
  ("the awakening" = day 1), the time of day (dawn, morning, noon, afternoon, evening, night), and optionally a calendar date
  (Gregorian, computed with `java.time`; a fantasy calendar is a later step, or a free label).
- **Per part** the model reports one of: *none* (the default), *advance* (days, 0 for the same day, the time of day, *approximate*
  for "weeks passed"), *day* (a stated story day), *date* (a stated date), *flashback* (the clock does not move). Each with a quote,
  checked by `Evidence` like the other changes.
- **Stored per processed part**: the time at a part is the time at the part before plus what the part says, so a reprocess, a
  regenerate or a branch change need no history (recomputed from the parts). A stated day or date overrides an approximate
  estimate before it (in the test the model estimated "weeks" as 14 days and then read "the fortieth day": the clock must take 40,
  not 20 + 14 + ...).
- **Read in the same request** as the state changes (one more field of the answer, no extra wait on a one-slot server). The user
  can set the clock of a part by hand (a fixed point, like a stated day).
- **The block**: `Story time: day 14 after the awakening, evening (20 April 1453).`
- **With the values**: a value keeps the story day it was set on, so the block and the tidy can say *a cup of tea (set 9 days
  ago)*, *sprained wrist (since day 4)*. The tidy then judges fading from the time that passed, not from the last parts only.
- Still open: two POV threads at different times (left out of the first version), a story that jumps back for good (a prologue
  set later): a part can be marked *not on the main timeline* by hand.

Prompt test (2026-11-16, the local model, Gemma 4 31B, `--max-tokens 24000`), the clock alone (not yet merged with the
extraction prompt), 3 samples of each of three scenes written for the test:

- **A** (day 3, evening): a camp at night (none), "The next morning" (advance 1, morning), a childhood memory (flashback), "Three
  days later" (advance 3), "By nightfall" (advance 0, night). **3 of 3** exactly so.
- **B** (day 20): a walk (none), "The weeks in the mountain village blurred together" (advance 14, approximate), a dream of the
  platform in Prague (flashback), "It is the fortieth day since you woke" (day 40). **3 of 3** exactly so.
- **C** (day 1 of a siege, 1453-04-06): "The guns fired all night" (none: it was night already), "For a week ... On the twentieth
  of April" (date 1453-04-20: the end of the message, not the week), "An hour later" (advance 0), "Do you remember the spring
  fair?" (none: a remembered question is not a flashback). **3 of 3** exactly so.
- 27 of 27 quotes word for word. Prompt 728-786 tokens, answer 830-2131 tokens with 2100-6900 characters of reasoning, 42-108 s.
- **Merged into the extraction prompt** (2026-11-16, same model, `--max-tokens 32000`, 3 samples of four scenes): the built-in
  extraction prompt with a clock section and the answer `{"clock":[...], "characters":[...]}`, plus the rule "nothing in a
  flashback changes the current state".
  - **A** (scene A with state: the bread eaten, the lantern handed to Mara, relief at the inn; the flashback has apples and a
    wooden sword): clock 3 of 3 as alone (once "three days later" marked approximate, harmless); state 3 of 3: bread removed,
    lantern removed from Fiora and added to Mara, mood *relieved*; nothing of the flashback added.
  - **B** (scene B with the trainers worn out and felt boots and a sheepskin coat given, a dream of a buzzing phone, a cup of tea
    handed over): clock 3 of 3 (the weeks 14 or 20 days, approximate, then day 40); Wearing set 3 of 3 (*city jacket, jeans, felt
    boots, sheepskin coat*: the jacket kept, the text does not say it went); the dreamed phone never added; the cup of tea never
    added to Carrying (0 of 3: a cup held is not "carried in bags", defensible).
  - **C** (Kessa, "Day 2 in Kessa": "the next two days", "On the third morning", Fiora sells her rope that evening): rope removed 3 of
    3. "On the third morning" was *day 3* in 2 of 3 (the scene is ambiguous: the third morning of the haggling or of Kessa) and once
    an advance **without days**, only the time of day.
  - **D** (C without a tracked character present): clock 3 of 3 (advance 2, advance 1 morning, advance 0 evening), `characters` empty.
  - The merge does not cost the clock accuracy, nor the state; the requests get longer: prompt 1128-1426 tokens, answer 654-3541
    tokens with 1300-11300 characters of reasoning, 34-181 s (the state alone was 52-160 s).
  - For the build: (1) *day* only when the text counts from the anchor ("the fortieth day since you woke"); an ordinal of something
    else ("the third morning" of a stay) is an advance: a prompt rule. (2) An advance without days but with a time of day is the
    same day when that time comes later than the clock's, else the next day: the code decides, the model is not asked again.
    (3) The parts where no tracked character is present must still be sent (only for the clock), so the request is never skipped
    while the clock is on.
- Still to try: a long scene that crosses midnight, "the following week", a date without a year.

The prompt of the test:

```
You keep track of the time of a story. You get where the story stands now (the CLOCK: the story day, counted from an anchor event, the time of day, and the calendar date when the story has one) and the new story MESSAGES (numbered). For each message, report how the time of the story moves in it. Do not count: report only what the message says, the program adds it up.

Kinds:
- none: the message does not move the time, or says nothing about it. This is the default.
- advance: time passes. "days" is how many days pass (0 when it stays the same day: later that day, an hour later), "timeOfDay" where the message ends (dawn, morning, noon, afternoon, evening, night) when it says so.
- day: the message states the story day itself ("on the fifth day after the awakening"): "day" is that number.
- date: the message states a calendar date: "date" as YYYY-MM-DD, with the year of the clock when the message gives none.
- flashback: the message is set in the past (a memory, a dream of the past, a story told about earlier events); the clock does not move.

Rules:
1. Report only what the message states or makes plain ("the next morning" is one day, morning; "that night" is the same day, night; "three days later" is three days). Never guess time that the text does not show: a scene that simply continues is none.
2. Vague spans ("weeks passed", "a few days later", "some time later") are an advance with your best number of days and "approximate": true.
3. The time of day alone ("by evening they reached the river") is an advance of 0 days with the time of day.
4. A message that goes back to the present after a flashback continues from the clock as it was before the flashback.
5. Every report other than none needs an evidence quote copied word for word from the message.

Answer with JSON only, no other text: {"messages":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}]}. One entry per message, in order; leave out the fields a kind does not use.
```

## To validate with real models

Tried on the local model (`tools/LocalLlm.java`, the AI *Local LLM* of the dev database, a Gemma 4 31B model at the time;
`--max-tokens 24000`), 3 samples per case, three scenes written for the test (not from a book):

- **A, the river** (Fiora: wearing, weapon at hand *short sword, drawn*, carrying rope and lantern; Mara: no weapon, a letter; she
  slides the sword into its scabbard, hands the lantern to Mara, falls into the river, Mara loses the letter, draws a knife from her
  boot, two promises "tomorrow I'll find you a blade / give you my gloves"). First prompt: 3 of 3 samples: the cloak wet, the lantern
  removed from Fiora and added to Mara, the letter removed, Mara's weapon *thin knife*; the promises ignored 3 of 3; the sword set from
  *drawn* to *short sword* (sheathed, still at hand: a fair update). One quote of 9 was paraphrased (*"The letter... It's gone."*):
  the check flags it. Prompt 971 tokens, answer 1000-1600 tokens with 2200-4200 characters of reasoning, 52-83 s.
- **B, nothing happens** (a fire, talk of tomorrow, a promise to give the rope "in the morning", Mara sets the lantern beside her).
  First prompt: 2 of 3 removed the lantern from Mara because she set it down. Rule added (*putting a thing down for a moment, using
  it or showing it is not losing it*): 3 of 3 empty. Mood *tired and irritated* kept 3 of 3 (the text says it again).
- **C, off page** (they reach the inn and sit in borrowed shirts and trousers, a cut is bound, Mara laughs). 3 of 3: both wear the
  borrowed clothes. Mood: first prompt *smiling* / *laughing* 3 of 3 (an action, not a mood); rule 5 added (*a state, not an action
  (cheerful, not laughing)*): *cheerful* 2 of 3, *smiling / laughing* 1 of 3. The bound cut was left as it was 6 of 6 (strict; a
  defensible choice). The second prompt made the model think longer here (up to 15 000 characters, 235 s for one sample).
- **A with the second prompt** (the built-in one, `StateDefaults`): 3 of 3 the same six changes as before, every quote word for
  word, the promises ignored. Prompt 1025 tokens, 5200-9900 characters of reasoning, 92-160 s.

- **Tidy, the road** (built-in tidy prompt, `StateDefaults.tidySystem`; three messages: three days on the road with the cloak drying,
  Fiora and Mara make up and laugh, they cross the ford; values: a soaked cloak, *rope* and *coil of rope*, lantern, *a cup of tea*,
  a locked scar, a sprained wrist, Fiora *furious with Mara*, Mara *out of breath*, Mara's knife). 3 of 3: the cup dropped, the cloak
  updated to *dry and stiff with mud*, the two ropes merged, Mara's *out of breath* dropped; the lantern, the knife, the sprained wrist
  (the text says it still aches) and the locked scar kept. The anger: 2 of 3 updated to *lighter* (the word of the story, but not a
  full value), 1 of 3 dropped. Prompt 896 tokens, answer 824-868 tokens with 1800-2050 characters of reasoning, 96-170 s.

Still to try:

- strict vs not strict on the lake case, the sheathed sword case, the "I will give you the key" case;
- the handover (one *remove*, one *add*) in one answer;
- a scene where nothing changes: the answer must be empty;
- the tidy on a mood that was not mentioned for K parts (the road case had the mood shown changing); an update value like *lighter*
  may need a rule "a whole value (lighter in spirit)".

## Decisions

- Categories in the user settings plus book-only ones; the book keeps copies (backups and exports complete).
- Three kinds: single, list, description. No numbers.
- Transient states through the regular (automatic) tidy, not a timer or a flag.
- The machinery shared with the Knowledge Tracker through a library in `marginalia/libs`.
- The name (*State Tracker*, folder `state-tracker`) is a working name.
