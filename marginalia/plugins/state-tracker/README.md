# State Tracker

Keeps the current state of the characters you choose (clothes, what they carry, the weapon at hand, injuries, mood, or your own
categories) and puts the state of the characters of the scene into the prompt, so the model keeps to it: the State tab of the
story sidebar. The block goes into the prompt like the Author's Note and/or by the
`{{command::state-tracker-block::<characters>}}` macro.

- **Categories** (`model.Category`, kinds single / list / description, an instruction for the model, strict, "none" when empty):
  the user's catalog in the user settings (`CategoryCatalog`, built-in defaults with fixed ids until the user stores theirs,
  the State Tracker section of Settings → Extension Settings, `StateSettingsForm`), and the book's copies in its state
  (`StateBook.categories`, linked by `sourceId`; categories of the book only have none). *Categories of this book*
  (`BookCategoriesDialog`): add from my categories, new for this book, update from my categories, save to my categories.
- **Tracked characters** (`TrackedCharacter`: name, aliases, the ids of the tracked categories, the values as `StateItem`s, current or
  history with the reason, locked).
- **The block** (`StateBlockBuilder`), inserted with `PayloadInsertion` at `AFTER_PREPARE_PAYLOAD`, tokens reserved at
  `BEFORE_PREPARE_CONTENT`; **commands** (`StateTrackerCommands`).
- Shares [`story-tracker-common`](../../libs/story-tracker-common/README.md) with the Knowledge Tracker (the lenient reading of the
  present characters field, the payload insertion, the UI access helper).

- **Following the story** (`extract`, `service`): the extraction (`StatePrompt`, `StateAnswer`, `StateChangeBuilder`) and the tidy
  (`TidyPrompt`, `TidyAnswer`, `TidyChangeBuilder`: drop, update, merge of the unlocked current values of the characters present in
  the last K parts), both applied by `StateApplier` (values ended by the tidy go to the history as `TIDIED`, recorded on the last
  processed part so that a reprocess takes them back); `StateExtractionService`, `StateSteps` (the runs of the library).

- **The log** of what is applied without a review (`StateLogEntry` in `StateBook.changeLog`, `StateLog` with the revert), over the
  library's `storytracker.log` and shown by its `ChangeLogPanel`.

- **The story clock** (`StoryClock`, `ClockMark`, `StoryTime`, `TimeOfDay`; `ClockBuilder`, `ClockChange`; `ClockDialog`, `ClockText`):
  the model reports what each part says about time in the same request, the code counts; fixed points by hand; the time in the block,
  the story day on the values and in the tidy.

Done: Milestones 1 to 5 (the state, the extraction, the tidy, the log with revert, the story clock). What is left is in [TODO.md](TODO.md).

User manual: `manual/user/extensions/state-tracker.md`.
