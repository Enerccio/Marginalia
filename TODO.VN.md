# TODO: shared code for a visual novel project

A possible second project: visual novels written with an LLM. The author defines characters (sprite sheets) and
writes the story beat by beat: for each beat the model answers with a scene as JSON (who stands where, with which
expression, who says what). The finished game is played in a viewer or exported, without a model.

This file is the plan for splitting the codebase so that both applications share what is not specific to prose.
Nothing here is started. It comes from one read of the code (packages, imports, sizes), not from a prototype.

## The idea: three layers

Maven modules inside this repository, **with the package names kept as they are**
(`com.github.enerccio.marginalia...`), so plugins and `@PublicAPI` are not affected by the move. The visual novel is a
second application on the lower two layers, in a repository of its own (see [Repositories](#repositories)).

| Layer | What goes in | State today |
|---|---|---|
| 1. Platform (no story concepts) | See below | Mostly clean |
| 2. Narrative core (shared by both applications) | See below | Needs generalising |
| 3. Novel application (stays Marginalia) | See below | Stays as it is |

### 1. Platform

- Entity base: `BaseEntity` / `OwnedEntity` / `ExtendableEntity`, `domain.traits`, `ExtendableEntityListener`, the base
  repositories and services (`BaseService`, `OwnedService`, `ExtendableService`).
- Users and application: `domain.security`, settings, `Resource`, tags, jobs, cleanup and trash, the database backup,
  updates, notifications.
- LLM: `AI` / `Protocol` / `Usage` and the limits, `InferenceService` and its implementations, the tokenizers,
  `domain.service.structured`, `function`, `tool`, the GPU locks.
- Templates: `domain.templates` and `MacroTranslator`.
- Extensions: OSGi, the `instruct` weaving, extension AIs, plugin localization, the extension log.
- Generic UI: `ui/widgets`, `ui/menu`, `ui/jobs`, `ui/shortcuts`, the AI / protocol / admin dialogs.

The entities, the inference implementations, `structured` / `function` / `tool` / `gpu` / `job` and `templates` import
no story entity today.

### 2. Narrative core

- The branching tree (`ChatMessage`, `activeLeaf`, swipe, branch).
- Lorebooks and their activation.
- Summaries and the summary chain.
- The generation pipeline (the steps and `Events`).
- `MessagePrompt`, folders with inherited settings.

This layer is the real decision. A visual novel is still a long branching story that needs lore and summaries to fit
into the context, so sharing it is better than writing it again. The price: a part is prose today
(`ChatMessage.response`), and the payload of a node has to become pluggable, prose for Marginalia and a scene for the
visual novel.

### 3. Novel application

- Annotations and passages, the version history and authorship, redraft, impersonate, the planner.
- Writing statistics, `export/*`, the SillyTavern import.
- Agents (`domain.service.agent`, 52 files, and `AgentRunnerImpl`, about 3,900 lines), the manuscript commands.
- Most of `ui/dialogs` (108 of 168 files name story entities).

## Repositories

Two repositories, not one and not three.

- **This repository:** the platform, the narrative core and the novel application, as modules. Nothing in it is about
  visual novels.
- **The visual novel repository:** only that application (characters, sprite sheets, the scene schema, the stage, the
  viewer, the Ren'Py export). Marginalia is a git submodule of it, at a pinned commit, and is built from source.

Why:

- **Not one repository:** Marginalia would carry an application that has nothing to do with writing prose.
- **Not three:** the borders of the layers will still move (the payload of a node touches the platform, the core and
  both applications), and a change across three repositories is three commits and two bumps of a pointer. The shared
  layers can get a repository of their own later, when they rarely change together with an application or when
  somebody else builds on them; published artifacts are then a better link than submodules.
- **A submodule and not versioned artifacts:** plugins must be built from the same source tree as the application,
  `PublicAPISignatureTest` checks them against it, the build reads the git information, and nothing is published today
  (the build installs into the local `~/.m2`). A pinned commit built from source fits all of that.

The rule that follows: **Marginalia never needs to know about joins, pins or ending flags.** What both can use sits
in the narrative core (named branches). What only the visual novel needs is built on hooks of the core, the main one
being "resolve the branch of this leaf" as something the application can replace. A feature of the visual novel that
can't be built without editing Marginalia's own code shows a missing extension point of the core; it is not a reason
to put visual novel code there.

The price: a change that crosses the border is two commits and a bump of the submodule, and CI has to check out
recursively. So the visual novel repository is created late (see the order of work), when the core can be built
against.

## What blocks the split now

- [ ] **Vaadin in the domain.** 25 files of `domain` import Vaadin or `ui.*`, among them
  `OpenAICompatibleInferenceService`, `GatedInferenceService`, `OsgiService`, `GenerationStepBase`,
  `GenerationListener`, `Runs`.
- [ ] **DTOs in the wrong package.** `InferenceService` imports `LLMChatMessage` and `LLMToolCall` from
  `impl.generation.dto`; they belong next to the inference API.
- [ ] **Generic services that name story entities:** `SessionPoint`, `CleanupServiceImpl`, `UsageContext` /
  `UsageServiceImpl`, `ResourceServiceImpl`, `TagRelationService`, `IPCService`, `CommandServiceImpl`. Each needs a
  registration point in place of the hard reference.
- [ ] **Story registries in `extensions`:** `AnnotationFactsRegistry`, `ExportTextRegistry`, `PartTextRegistry` move
  to layer 3.
- [ ] **Registries that are one file:** `persistence.xml`, `services-config.xml`, the Flyway migrations, the key
  enums with their bundles (split per layer already: `L` and `M_L`, the VN adds its own enum and `TextBundle`s), `Defaults*`, `CssStyles`. Each needs a form per module.
- [ ] **Tests:** `MarginaliaTestBase` and `MockLLMServer` become a test support module.

## Order of work

1. [ ] Fix the leaks above in place, without changing modules, so the tests keep passing.
2. [ ] Extract the platform module, same packages.
3. [ ] Extract the narrative core, once the needs of the visual novel are concrete enough to shape the payload of a
   node, with the hooks the visual novel needs (the payload of a node, resolving a branch).
4. [ ] Create the visual novel repository with Marginalia as a submodule and start the application there.

## Open questions

- How `ajc` weaving and `BuildTimeWeaver` behave per module was not checked. This is the main technical risk of step 2.
- Vaadin frontend resources (`src/main/frontend`, `shared-styles.css`) served from a module JAR.
- Whether the plugins that are useful to both applications need anything besides the platform (most name story
  classes today).
- One Flyway history per module, or one per application that includes the platform's migrations.

## Later: named branches as choices, export to Ren'Py

The story tree already is the structure of a game with choices: a node with several children is a place where the
story splits. Giving a branch a name turns it into a choice of the player, and an export can write the whole tree as a
Ren'Py script.

- **The name is on the child.** An extended attribute of the part (the text of the choice, for example "Follow her"),
  so no migration, and it travels with backups and branch copies. It belongs to layer 2: Marginalia can use named
  branches too (the tree view, a gamebook or Twine export).
- **The name decides: a named branch is offered, an unnamed one is followed.** A named child is shown to the player
  as a choice, also when it is the only one (a menu of one line, which visual novels use for emphasis). An unnamed
  child is never shown: the story goes on into it by itself. So the author controls menus only by naming, and a plain
  line of parts (nothing named) plays without a stop, as it does today.
- **A swipe is not a choice.** Siblings are also the discarded takes of the same turn, and they are unnamed, so they
  are never offered. Only one of them can be the way on, see the rules below.
- **The export walks the tree, not the active branch.** Every exporter today follows `activeLeaf`; this one starts at
  the root. Each node with choices becomes a `menu:` and each named child a `label` reached by `jump`; the labels are
  made from the uuids of the parts so they stay the same between exports.
- **Scene to script.** A scene of the JSON becomes `scene` (background), `show <character> <expression> at <position>`
  and the dialogue lines; a character becomes a `define ... = Character(...)`.
- **Sprite sheets are cut up.** Ren'Py wants one image per expression (`<character> <expression>.png`), so the export
  cuts the sheets and writes the files with the script into a project folder (`game/script.rpy`, `game/images/`).

### Joining branches (visual novel only)

Without joins every choice doubles the story that has to be written. The tree stays a tree; a join is added on top,
in the visual novel repository (links, pins and the ending flag are extended attributes of the parts and need no
schema of Marginalia; the core only has to let the application resolve a branch its own way):

- **Active and passive parents.** A part keeps its one `parent`: the *active* parent, the one every walk of the tree
  follows today and the one whose side holds the summaries. A join adds *passive* parents: links from the part to the
  last parts of other branches (uuids in an extended attribute). With `A -> B, C` and both continuing in `D`: `D.parent`
  is `B`, `C` is a passive parent of `D`. Everything that does not know about joins (summaries, lore, the story view of
  Marginalia) sees `A B D` and is right, which is the state of the tree today.
- **Pins: sets of visited parts.** A part can carry pin sets (uuids in an extended attribute). A pin names a part
  that must have been visited; inside a set all of them ("and"), between the sets any one ("or"). `F` with the set
  `{C}` is for who came through `C`; `X` with `{A, C}` and `{A1, C1}` is reached by who saw `A` and `C`, and also by
  who saw the unrelated `A1` and `C1`. A pin can name any part above, not only a parent of a join, and there is one
  kind of condition for everything: "through `B` or `C`" at a join of three is `{B}`, `{C}`. A part without pins is
  open to every route that gets to it. With `D -> E, F`: the branch of `E` resolves to `A B D E` (no pins, the active
  parents), the branch of `F` with `{C}` to `A C D F`.
- **The first set is the active one.** As with the parents: the part is written in the context of its first set, and
  the other sets are passive, the part is played on those routes too but was not written for them.
- **Resolving a branch.** Walk up from the leaf with the parts of the active set still to be found (plus the ones of
  the pinned parts passed on the way, so the parts below `F` inherit its route without saying anything); at a part
  with passive parents take the parent that still has them above it, the active one when several or none do. A set
  that no single path from the root can satisfy (two parts on branches that never meet) can't be resolved: refuse it
  when the pin is set. This is one function, and generation (summaries, lore, the variables of `setvar`), the player
  and the export all have to read the branch through it.
- **In the game pins are a condition, not a choice.** `F` can only be reached by a player who visited `C`. The
  export keeps one variable per part that some pin names (`$ seen_C = True` at the label of `C`) and guards the pinned
  children: `F` gets `if seen_C`, `X` gets `if (seen_A and seen_C) or (seen_A1 and seen_C1)`, a child without pins is
  open to all. This is the usual "branch and bottleneck" shape of a visual novel.
- **Conditions without code.** This is the point of the pins: the author never writes a variable or an `if`, only
  says "this part continues the route through `C`", and the compile step makes the variables and the conditions.
  What a player meets at a part follows from the children that are open on their route:
  - named children are open: a menu of them, one or more (`E` and `F` named, `F` pinned to `C`: who came through `B`
    is offered `E`, who came through `C` chooses between `E` and `F`);
  - no named child is open: the story goes on into the open unnamed child, no menu (`E` pinned to `B` and `F` to `C`,
    both unnamed: an earlier choice decides, the player is not asked again);
  - both kinds are open: the menu wins and the unnamed child is not taken. An unnamed child is so the "otherwise" of
    its named siblings: choices that show up only on some routes, and the plain way on for everybody else;
  - several unnamed children are open: only one can be followed. A pinned one goes before one without pins (the more
    exact condition first, the one without pins is the "otherwise"). Among several without pins, which are swipes, to
    decide: the one on the active branch of the book, or the take the author selected last; the compile step names
    the place either way, because the other takes are never played;
  - the pins of the parts on the way are not repeated: a part under `F` is behind `F`, so it needs `C` too.

  The editor should show which routes reach a part, and warn about a part that no route reaches (the only real
  mistake: it was written and nobody can play it).
- **Where a route stops is an ending.** A leaf is an ending, and so is a part whose children are all closed on the
  player's route (at `D` with only `F` pinned to `C`, the story of who came through `B` ends with `D`). Neither is an
  error: a branch that was not continued is one more ending. The compile step lists the endings with the route that
  leads to each, so the author sees the ones they did not mean.
- **The ending flag.** A part can be marked as an ending (an extended attribute, with an optional name that the
  viewer and the export show; `return` in Ren'Py). It tells a meant ending from a branch left unfinished: the list
  above shows the stops *without* the flag as the ones to look at, the flagged ones are done. While the flag is set the
  branch is closed: no generation of a next part, no swipe that continues it, no join out of it, until the author
  unchecks it. Only a leaf can be flagged, and a flagged part can't get children, so the two never
  meet and the flag is always per part. A join that stops for one route only (at `D` with only `F` pinned to `C`, the
  route through `B` stops) is not flagged, it has a child: it stays in the list as a stop without a flag. An author who
  means that ending gives the route its own leaf and flags that: `D` splits into `E`, `F` and `G`, and `G`, pinned to
  what was traversed, is the bad ending (it can be no more than the ending card).
- **Pins cover everything that was traversed,** since what a player did is the parts they visited. Not covered:
  "never visited `Z`" (the ending for who skipped something). A negative pin inside a set would do it, as the negative
  tags of lorebook entries do; decide when a story needs it.

### Writing artifacts and the finished game

Summaries are an artifact of writing: they exist so that a generation fits into the context. The same holds for the
lore, the prompts (`MessagePrompt`), the instructions of a part and the variables of the templates. The finished game,
played in the viewer or exported, needs none of them: only the scenes, the characters with their images, and the shape
of the story (splits with their names, joins, pins).

- **One compiled form for the viewer and the exports.** A step turns the tree into a graph of scenes (nodes, choices,
  conditions from the pins, the assets it uses) with nothing of the writing in it. The viewer plays this graph and the
  Ren'Py export writes it out, so the two can't disagree, and a published game does not carry the prompts or the lore
  of its author.
- **Joins only matter to summaries while writing.** The points about summaries below concern generating on a pinned
  route (`F` and the parts under it), never the player. A route nobody generates on any more needs no valid summary.

### Writing state and gameplay state

Two states that must not be mixed. The stage shows either, through the same call that sets it whole.

| | Writing state | Gameplay state |
|---|---|---|
| Whose | The author's, one per book | A player's, one per playthrough (and per save) |
| Lives in | The entities of the book: the tree, the active leaf, pins and their active set, summaries, lore, prompts | Outside the book: the part the player is at and the line in it, the set of visited parts, the way taken (for going back) |
| Works on | The tree as it is being written, drafts and unfinished branches included | The compiled graph only |
| A part is reached by | One route, the resolved branch (active parents, the active pin set): what a generation has as context | The route the player really took; the visited set answers the pins |
| Changed by | Generating, editing, swiping, naming, pinning | Advancing, choosing, going back, loading a save |
| Needs a model | Yes | No |

- **Gameplay state is small and never written into the book.** Every part has the full state of the stage, so a
  player's state is the current part, the line, the visited set and the way taken. Going back is one step back on the
  way taken, a save is this state written down. Playing changes nothing the author owns: not the active leaf, not a
  part, not a counter.
- **Where it is kept.** For the owner and signed in users an entity of their own (save slots, by the uuid of the
  game); for a reader of a published game without an account the storage of the browser. In an export it is the
  store of Ren'Py and none of our business.
- **Saves against a game that changed.** A save names parts by uuid, and the author can compile again. A save whose
  part is gone falls back to the last part of its way that still exists; its visited set keeps what still exists.
- **Trying it while writing.** "Play from here" in the editor starts a gameplay state from a part: the way taken and
  the visited set are the resolved branch of that part, then the author plays as a player would. It is thrown away
  afterwards, and it reads the tree compiled at that moment, drafts not included.
- **Decided: the game is written by beats, not played as a chat.** The first idea (the engine log above) was like
  SillyTavern: the player types and the model answers with the next scene. Dropped for an author who writes the
  visual novel beat by beat, as a book is written in Marginalia: an instruction for the beat, the model writes the
  scene, the author edits, swipes, branches. A part of the tree is one beat. The player then only plays what was
  written, without a model. Where the log says "the player types the next prompt", read "the author"; its points
  about the stage, the assets and the pipeline hold either way.

To solve:

- **Summaries must not cross a join.** A summary covers its part and the earlier ones back to the previous summary, and
  its hash is made of those texts. A summary on `D` whose block reaches into `B` does not match on the route through
  `C`; the hash check would drop it as outdated there, so nothing wrong is sent, but that route loses the summary.
  Rule to try: a join starts a block, so each parent of a join ends its own (a summary on `B` and one on `C`), and the
  blocks from `D` down are the same on every route.
- **The text of `D` was written after `B`.** It has to read well after `C` too. The author's job, but the generation of
  a join could be given the passive side (its summary) so the model writes a scene that fits both.
- **A part without a pin below a join** was generated with the active route in its context and is played on the
  passive one as well. Accepted (that is what "passive" means), or the editor warns.
- **No cycles:** a passive parent must not be the part itself or a part below it. Check when the link is made.
- **Everything that changes the tree:** deleting a part (its children move to its parent), merging into the previous
  part, branching, the backup and the copies (links are uuids and have to be rewritten like `nextSummaryUuid` is) need
  to keep the links and the pins valid, or drop them.
- **Reading the branch:** it is one recursive SQL query over `parent` today (`findBranchFromLeaf`). Pins are in the
  JSON, so the walk is done piece by piece between joins, or the links become real columns.

Open:

- Choices that only set a variable and stay on the same path (Ren'Py flags and conditions). The template variables of
  a part (`setvar`) follow the branch already and could be exported as `$ name = value`.
- Whether the model proposes the choices (a `choices` list in the scene JSON, each one generated on demand as a named
  child) or only the author names them.

## The engine: notes from an earlier discussion

From a chat log about the engine itself (assets, the generation pipeline, the stage, caching), read against what
this codebase already has. The log assumed a new Spring Boot application; some of its advice does not apply here.

- **Assets with descriptions.** Images are uploaded in the web UI; a button sends a background to a multimodal model
  for a description of a few sentences, stored with the asset, and the story model is given the list of ids with
  descriptions and picks by meaning, not by file name. The same for the expressions of a character. To add: the JSON
  schema of a scene can list the allowed ids as an `enum` (backgrounds, characters, expressions per character), so
  with structured output the model can't name an asset that does not exist. The description call is one more use of
  `InferenceService` (the model has to take images).
- **A pipeline of stages that extensions can block and that can be stopped.** The log ends at a custom interceptor
  chain on virtual threads. The generation pipeline is that already: fixed steps on a worker thread, `Events` that
  extensions listen to and change (a listener may block, it is not the UI thread), `CancellationToken`. So no event
  framework is needed, and not `StructuredTaskScope` either: it is still a preview API in JDK 25 and would put
  `--enable-preview` on the whole build. What the visual novel adds are its own steps and events (scene parsed,
  assets resolved), which is one more reason the pipeline belongs to the shared layer.
- **The UI is locked while a scene is generated, except Stop.** As the story editor does today.
- **The stage is a client side component.** Canvas, transitions and the typewriter text run in the browser; the
  server sends whole commands ("present this scene") and gets events back (presentation finished, the player
  advanced, the player asked to go back). A Lit component with `callJsFunction` and `@ClientCallable`. The rule of
  the main application (scripts only in `JavascriptLibrary`) is about its own UI classes; the stage is a component of
  the other application.
- **The stage keeps no state of its own.** Going back, jumping to a part and loading all use one call that sets the
  whole stage at once without animation (`hydrateState(snapshot, animate)`); the server never sends differences.
  This needs **the full state of the stage for every part**: the background and who stands where, not only what
  changed. With joins it is more than a convenience: if scenes were differences, the stage at `D` would depend on
  the route that led to it. So either the model writes the whole stage in every scene, or a scene is a difference and
  the compile step makes the full state, and then a join has to start with a full state (as `scene` does in Ren'Py).
  To decide with the scene schema; the whole stage every time is simpler and costs a few tokens.
- **Images are loaded by the browser, in parallel, and kept.** Three levels in the log: decoded images in the
  component (`ImageBitmap` in a map), the HTTP cache of the browser, a cache in the server. `Resource` keeps a file
  on disk with a `hash`, not a blob in the database, so the server level is the file cache of the operating system and
  needs no library; what is missing is a URL that contains the hash, so the answer can be cached for good
  (`immutable`). How the resources are served now, and whether a published game can read them without a session, has
  to be checked. The server can tell the stage to preload (`preloadAssets`) as soon as the streamed JSON names a
  background, which `PartialJson` makes possible.

Also from the log, about the product and not the code: a prototype of this is a weekend, a tool other people can use
is ten times that; the hurdle for a user is the assets (hence the generator and the descriptions); and every model
server formats structured output a little differently, which the inference layer here already carries.

## What the visual novel gets from the existing code

- **Scene JSON:** `InferenceService.infer(payload, protocol, Class<T>, ...)` already asks for structured output and
  checks it against the schema; `PartialJson` allows showing a scene while it streams.
- **Sprite sheets:** `Resource` can store them; the `image-generator` plugin with the GPU locks and `http-hooks` can
  generate them.
- **Voices:** the `audiobook` and `tts-reader` plugins.
- **Characters:** a new owned entity with extended attributes, linked to lore entries.

The stage (sprites, positions, the dialogue box) should be a client side component that is given the scene JSON.
Server side Vaadin components suit the editor, not the player.
