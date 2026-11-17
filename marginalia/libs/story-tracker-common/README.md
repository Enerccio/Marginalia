# story-tracker-common

What the plugins that follow the story share: the [Knowledge Tracker](../../plugins/knowledge-tracker/README.md) and the
[State Tracker](../../plugins/state-tracker/README.md). A plain Maven jar that each plugin **embeds** in its bundle
(`<Embed-Dependency>story-tracker-common;inline=true</Embed-Dependency>`); it is not an extension and is never put into the extensions
folder. Plugins do not see each other's classes, so this is shared source and not shared runtime: nothing in it may be state that two
plugins would have to share (each copy has its own statics).

Packages:

- `process`: `Part` (a part of the branch as a tracker sees it), `StoryBranch` (reads the active branch of a book), `TrackerProgress`
  (what a tracker's saved state offers: start point, cursor, processed parts, taking back what came from given parts, the reading of a
  part without a model), `BranchProcessor` (keeps a `TrackerProgress` in line with the branch: swipes, other branches, reprocess,
  settled parts, parts changed since they were read), `ProcessedPart`, `TextHash`, `RunEstimate` (the cost of a series of runs before
  it starts), `PartReader` (what the model may read of earlier parts).
- `scene`: `PresentCharacters` (the lenient reading of the present characters field), `SeparatorText` (the separators as the user
  edits them).
- `characters`: `CharacterTrackerCharacters` (the characters of the Character Tracker read from the book's attributes, for an import).
- `block`: `PayloadInsertion` (a block into the prompt at a depth, like the Author's Note).
- `process` also has `Evidence` (whether a quote of the model is in a part).
- `llm`: `TrackerModels` (which model, the budget of a request), `ModelCall` (one streamed request, the reasoning kept, an optional function
  to read earlier parts), `StoryLore` (the lorebook text of a run), `Live`, `LoreMode`.
- `run`: `TrackerRun` (a series of requests on a worker thread, guided or automatic, with the tracker's `Steps`) and `TrackerRuns` (one run
  per book, a proposal held for an agent).
- `log`: `LogEntry` (a change applied without asking; a tracker extends it) and `TrackerChangeLog` (keep the newest 200, revert once
  through the tracker's `Undo`).
- `ui`: `UiAccess` (changes a component from a worker thread through `ui.access` and a push), `ProgressPanel`, `ReviewDialog`, `ImportCharactersDialog` (the
  import from the Character Tracker, with the tracker's options), `ChangeLogPanel` (the *Applied automatically* list with its revert buttons), their CSS
  (`StoryTrackerStyles`, `storytracker.css`) and texts (`StoryTrackerLocalization`).

Build: `mvn install -DskipTests` here (a plugin needs it in the local repository first). The root `Dockerfile` and the workflows do it
for every folder of `marginalia/libs/`.
