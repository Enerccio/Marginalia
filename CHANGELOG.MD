# Changelog

All notable changes to Marginalia are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.3.0] - 2026-10-16

One database migration (`V14`): a copy of the database is saved before it runs, and 1.2.0 cannot open a migrated database.

### Added

- **Help assistant.** A round robot button in the bottom right corner (desktop and phone) opens a chat that answers questions
  about Marginalia from its manual: the model searches and reads the manual pages with two functions and names the pages it used. The
  panel says which AI answers and warns when the user's limit applies; the conversation is kept in the session
  (`SessionPoint.getHelpChat()`). The model can also read what the user is doing (the open section, tab or dialog and its
  title, never story text) with a function `get_ui_state(path, deep)`; by default it gets only what is on the screen, `deep` also
  expands the tabs that are not open. There are two default prompts, for administrators and for other users, editable (with **Reset to
  default**) in the new tab. With **Allow navigating the interface** (new setting `helpInteractions`, off by default) the model can also
  move through the UI for the user with a function `interact_with_ui(path, interactions)`: open a section, select a tab, nothing that
  changes anything. UI elements offer such moves by overriding the new `LLMInteract.getInteraction()` (a map of keys to `LLMInteraction`);
  `applyInteraction(key)` / `applyInteraction(List)` run them in order and return the new snapshot; `LLMSnapshot` gained `interactions` and
  `element`; `UiStateSource` gained `interact` and a `withInteractions` flag on `read`; new `InteractiveHelpTools`; implemented by both workspaces,
  `AdminPart` and both story editor dialogs (these overrides carry `@PublicAPI`; `DesktopWorkspace` got a private `openAdmin()`).
  Interactions also select a tab of the Settings (`UserPart`, so the phone settings too) and press the button that opens the dialog
  to add a book, a model or a protocol (`open-dialog:<button text>`, `LLMInteract.dialogButtons`; not while a dialog is open): those
  lists and the settings now describe themselves (`getLLMState`, `getInteraction` with `@PublicAPI`; `WorkspaceLLMSupport.describeTable`).
  **When something goes wrong** the model can read it: a function `read_last_errors(count)` returns the last errors, failed requests
  to a provider (`InferenceException`s, also a generation's), warnings and validation messages the user was shown (every `ui.widgets.Notification`
  error and warning, `UIUtils.showError` / `internalServerError` / `inferenceError` / `showValidationErrors`), newest first. They are
  kept in the new `ProblemLog` of the session (`SessionPoint.getProblems()`, the last 20, written by the new `utils.UiProblems`), the
  unexpected ones also in the `sessionStorage` of the browser tab (the last three, they survive a reload); keys, tokens and passwords are
  removed from the text and it is cut at 400 characters. New `GenerationListener.onInferenceError` (a default method calling
  `onSimpleError`; `StoryGenerationListener` overrides it, with `@PublicAPI`). **Forms:** `get_ui_state` lists under *Problems with the form*
  the fields that are invalid (with the message they show) or required and empty, never their values (new
  `LLMInteract.getLLMProblems()`, default `LLMFormProblems.scan`; `AIDialog` adds its cross field rules, with `@PublicAPI`;
  `LLMSnapshot` gained `problems`; `UiStateSource` gained `readProblems`). **Manual links:** the manual tools print the address of the
  page on the published manual (`https://enerccio.github.io/Marginalia/user/...`, `ManualPage.url()`), the assistant cites pages as links
  and they open in a new tab. **The assistant is usable above a dialog** (a book is one; before, the dialog blocked the button and the
  panel): in the top layer it moves to the bottom left while a dialog is open. **Administration data:** with **Allow reading
  administration data** (new setting `helpAdminData`, off by default) an administrator's assistant gets `get_admin_data(topic)`
  (extensions with their state, user counts, database backups, what a cleanup would remove, the application) and
  `read_extension_log(extension, lines)` (new `AdminHelpTools`, `AdminDataSource`; never for other users, read only). A new **Help assistant** tab on the Admin page lets an administrator turn the
  assistant on or off and choose one of their own models for
  it; **Apply limits** (shared models only) makes the questions count against each user's limit and usage like any other
  request to the model, otherwise (or for a model that is not shared) the owner of the model pays and nothing is counted for the
  user. The user manual (`manual/user`, Markdown, no pictures) is now packed into the WAR
  as `classpath:/manual/` and searched in memory by the new `ManualService` (`ManualPage`, `ManualSection`, `ManualHit`);
  new `HelpAssistantService` (`ask`, `getInfo`), new `ManualTools`, `HelpTools`, `UiStateSource`, `HelpPrompts`, new `ui.LLMInteract` and `ui.LLMSnapshot` (implemented by `Workspace`,
  `WorkspaceComponent`, `ManuscriptDialogPart`, `PhoneManuscriptDialogPart`, every dialog, `Main` and several components; extensions can
  implement it on their own components, see the developer manual, *UI*; the overrides in the extendable classes carry `@PublicAPI`), new component `ui.components.HelpAssistantWidget` (added to `Main`), new application settings `helpEnabled`, `helpAiUuid`, `helpApplyLimits`, `helpInteractions`, `helpAdminData`, `helpPromptAdmin`, `helpPromptUser`
  (extended attributes, no migration), new texts in all language bundles (the Chinese localization plugin too).
  **For plugins:** new `UnmeteredScope`; an inference service (an extension AI) must skip `AILimitService.check` and
  `UsageService.record` for a request started while `UnmeteredScope.isActive()`, see the developer manual,
  *AI providers*. The `google-genai` plugin does it. Both Dockerfiles now copy `manual/user` into the build.
- **Red warning about invalid extensions.** A red bar at the top of the workspace (desktop; below the title bar on phones) shows when an
  installed extension is invalid, so it is not found out only on the Admin page or by a missing function. An administrator sees
  which extensions on hover, other users are told to tell the administrator. Extensions the administrator stopped are not counted. New
  `OsgiService.getInvalidExtensions()`, new component `ui.components.InvalidExtensionsWarning`, new texts in all language
  bundles (the Chinese localization plugin too).
- **Lorebook preview in the desktop story editor.** A new read only *Lorebook* tab in the left sidebar (next to the
  Outline, a `TabDock` tab with the id `lorebook`, so it can be detached too) shows the entries of the book's lorebook in a
  tree grid; an entry opens to its content (rendered as Markdown) and the text or pattern that activates it. *Include sub lorebooks* lists the
  lorebook and all sub lorebooks below it as top level rows with their entries under them, without it only the entries are
  listed. It is read again whenever the Story tab is entered; nothing is saved from it. New component
  `ui.components.LorebookPreview`, new `ManuscriptStoryPart.createLorebookTab` and `LOREBOOK_TAB` (plugins must not give a
  tab of theirs the ids `chapters` or `lorebook`). New text in all language bundles (the Chinese localization plugin too).
- **Detachable sidebar tabs in the desktop story editor.** The tabs of the left sidebar (Outline and the tabs of extensions)
  can be dragged out into panels of their own, in the same column under each other or in a column of their own next to the
  others, and dragged back onto another panel (on a tab to put it before that tab, on the body to put it last). While a tab
  is dragged, dashed strips between the panels and tall strips between the columns take a drop to detach it. The panels and
  the columns are separated by splitters (`SplitLayout`) the user can drag, their sizes are saved in the layout too. The arrangement is saved with the book, in the attributes of the manuscript under
  `$desktopManuscriptStoryPartTabsConfig` as `{"columns":[{"size":..,"groups":[{"size":..,"tabs":[...]}]}]}` (no migration; it travels with backups), and a tab of an extension that is not loaded
  keeps its place. The selected tab of every panel is part of the workspace autosave (`Restorable`, key `tabs` of the story
  part). The position of the divider between the sidebar and the story is saved with the book too
  (attribute `$desktopManuscriptStoryPartSplitPosition`, new `ManuscriptStoryPart` methods `applySplitPosition` and `saveSplitPosition`).
  New reusable widget `ui.widgets.TabDock`. API changes plugins can see (since 1.3.0): the field `leftBar` of
  `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` is a `TabDock` instead of a `VTabSheet` (`add(id, caption, content)`,
  `add(caption, content)`, `remove(content)`; extensions that read it as `VTabSheet` must be updated (the extension verification refuses them) - Arc Outline, Character
  Tracker, Side Query and Author's Note were, and give their tabs the ids `arc-outline`, `character-tracker`, `sidequery` and `authors-note`); new
  `ManuscriptStoryPart` methods `applyTabsConfig` and `saveTabsConfig`, `restoreState` has the new local `tabs`. New text in
  all language bundles (the Chinese localization plugin too).
- **Inference providers from extensions.** An extension registers an `ExtensionAIFactory`
  (`InferenceServices.registerExtensionFactory` / `unregisterExtensionFactory`, `getExtensionFactory(id)`,
  `getExtensionFactories()`) that creates the `InferenceService` and the settings form (`ExtensionAIForm`) of its AIs. New
  AI type `AIType.EXTENSION` and entity `ExtensionAI` (`ais_extension`, one column `extensionId`, **migration V14**); the
  settings of the provider are in `ExtensionAI.getSettings()` (the dynamic attributes of the AI), secrets are encrypted with
  the new `InferenceServices.protectSecret` / `revealSecret`. In the AI dialog the type *Extension* (offered while some
  extension provides AIs) shows a *Provider* selector, the form of the provider and *Test Connection*; the type of an existing
  AI can no longer be changed. While the extension is not loaded `InferenceServices.forAI` returns `null`, the new
  `forAIOrThrow` throws `InferenceException` of the new type `EXTENSION_UNAVAILABLE` (the generation steps and summaries use it
  and the user is told to load the extension), the AI list shows the provider name (`AITypes.label`) and the dialog opens such
  an AI with a notice and keeps the settings of the extension as they are. API changes plugins can see (since 1.3.0): the
  `AIDialog` methods `createFields`, `updateDynamicFields`, `save` and `populateFields` have new locals, new `AIDialog` methods
  `addExtensionSettings`, `isExtensionMissing`, `extensionAiFor`, `currentExtensionAI`, `testExtensionConnection`. New texts
  in all language bundles (the Chinese localization plugin too).
- New plugin **Google Gemini** (`marginalia/plugins/google-genai`): Google's Gemini models as an inference provider over the
  Google Gen AI Java SDK, which is embedded in the plugin JAR only (about 14 MB, nothing is added to the application). API key
  (encrypted), model list, optional proxy URL, timeout, retries, safety filters off, the thinking summaries as reasoning
  (thinking level for Gemini 3, token budget for the others), streaming with prompt cancellation, JSON schema, function calls
  (with the thought signatures Gemini 3 needs), attachments, usage and limits like the OpenAI-compatible provider.
- New plugin **Arc Outline** (`marginalia/plugins/arc-outline`): an *Arcs* tab in the story sidebar with the arcs of the
  book and their chapters. Arcs and chapters have a title, description, note and a state (planned, current, finished),
  are reordered with buttons (a chapter moved past the end of its arc goes into the neighbouring arc) and finishing an
  arc finishes its chapters. The wand opens a dialog that asks the model for a new arc (a name, a terse description and the details in the note) from
  a prompt template (`{{backgroundLore}}`, `{{existingArcs}}`, `{{specification}}`, editable in *Settings → Extension
  Settings → Arc Outline*), with the reasoning and the arc shown while they stream in; the model can get names from the
  Name Generator like in the Lore Generator. The wand of an arc does the same for its chapters (prompt variables
  `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}`, `{{chapterSpecification}}`),
  with a choice of how much of the story the model sees, the number of chapters in words or as an exact number (with
  a field for each chapter) and the new chapters shown under the arc as they stream in. Both ask for a terse description and put the details into
  the note. Generated arcs and chapters
  keep the reasoning and the answer of the model (`generationReasoning`, `generationResult`), shown in a popover.
  Stored in the book's attributes, no migration.
- Arc Outline: the wand next to *Add new arc* is a menu with *Generate an arc* and the new *Import from story*, which
  asks the model to summarize a range of the messages of the active branch of the book as an arc with chapters
  (optional exact number of chapters, state of the imported nodes, a counter with the approximate tokens of the range
  and of the whole prompt next to what the model takes). New prompt `Defaults.IMPORT_PROMPT` with the variables
  `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}`, `{{numberOfChapters}}`, editable in *Settings → Extension
  Settings → Arc Outline* (*Story import prompt*). Chapters with a note have a chevron that shows the note under the
  description in the list, without the dialog.
- `ModelProtocolPicker` (`ui.widgets`): a model and a protocol to choose from, for dialogs that run the model with a
  choice of their own. The Lore Generator dialog has one (it starts with the default model of the user settings), so
  has the Arc Outline generator (it starts with the model and protocol of the book).
- `IPCService`: extensions (and the application) offer text about a book or a story part to each other without
  importing each other: `registerManuscriptProvider` / `registerChatMessageProvider(bundleIdentifier, providerKey,
  providerName, callback)` with `ManuscriptPluginData` / `ChatMessagePluginData.getDataFor(entity, callerBundle,
  callerIdentifier)`, `getManuscriptProviders` / `getChatMessageProviders` (bundle → key → name, in registration order)
  and `getManuscriptData` / `getChatMessageData`. The providers of a bundle are removed when its extension unloads.
- Side Query: a plug button above a tab lists the sources of data other extensions offer; checked sources about the book
  are sent with the lore, sources about story parts are added after each included part. The Arc Outline offers its
  outline as two sources: the terse one (descriptions) and the full one (with the notes).
- `PartialJson.parse` (`domain.service.structured`): reads the unfinished JSON of a streamed answer (a markdown block
  around it is ignored) into a Jackson tree, to show what has arrived so far. New dependency
  `io.github.funnysaltyfish:partial-json-parser-jvm` (brings `kotlin-stdlib`).

- Workspace autosave: Marginalia remembers where the user was and restores it after the login - the selected tab, the
  filters of the book list, the lorebook and the open entries of the lorebook tab, the open book with its tab (and
  the open entries of its lorebook tab) and the new turn form of the story tab (scene, POV character, present
  characters, instructions). Both the desktop and the phone layout. Stored in the new `UserSetting.workspaceState`
  extended attribute (no migration). What was deleted meanwhile is skipped, a deleted book is not opened.
- `Restorable` (`ui`): `storeState()` / `restoreState(JsonObject)` for the UI classes that have state worth
  remembering, with the helpers in `RestorableState`. API changes plugins can see (since 1.3.0): `Workspace`
  extends `Restorable` and has `autosave()` and `restoreAutosaved()`; `WorkspaceComponent`, `ManuscriptDialogPart` and
  `PhoneManuscriptDialogPart` extend `Restorable` with default methods that store nothing, so existing parts compile
  unchanged; `ManuscriptDialog`/`PhoneManuscriptDialog` have `setOnStateChange` and `stateChanged()` (a plugin that
  adds a tab calls it), the lorebook views `setOnStateChange`, `NewTurnForm` `setOnChange`.
- New plugin **Name provider** (`marginalia/plugins/name-provider`): the model gets real names instead of generated ones.
  `list_real_name_lists` lists about 180 lists, `<Country> - Surnames` (75 countries) and `<Country> - Forenames` (106), and
  `pick_real_names` picks up to 100 of a list at random as pairs `{romanized, original}` (`Satō` / `佐藤`). The data is the
  popular names by country dataset (CC0), imported at build time into `name-lists.json` (committed; without network the
  committed file is used), the country codes are expanded to the English names of the countries.
- Name generator: 75 new categories `<Country> - Surnames` trained on the romanized surnames of the same dataset (new build
  step `import-popular-names`, `src/build/ImportPopularNames.java`), the tables and `chains.json` are extended, the
  existing ones are unchanged.

### Changed

- Character Tracker: the *Add relationship* button is above the fields of a new relationship, and closing the character dialog
  (Cancel, Esc) with unsaved changes asks for confirmation (Yes/No).
- The opened details of lorebook entries stay open when the entries are reloaded (refresh button, switching to the tab).
- Lore Generator and Arc Outline: the *Name tools* table lists the categories of the Name generator and of the Name provider
  together, with the prefix of the source (`Generator: `, `Provider: `), and the model gets the tools of the extensions that have a
  checked category: both, one or none. Each kind has its own hint in the settings (*Name generator hint*, the former *Name
  tools hint*, and the new *Name provider hint*, which tells the model to pick a name from the returned ones and keep it as
  it is). Categories saved before count as Generator ones. `NameGeneratorBridge` is now `NameToolsBridge`; the service
  methods `buildGenerationPrompt`, `buildPrompt` and `buildChapterPrompt` take the `FunctionSet` instead of a flag.

### Fixed

- Reviewer: the review dialog no longer saves an older copy of the part (reverting an edit or unlinking a summary made after the book was opened); it reloads the part before it reads or writes the reviews.
- Side Query: the sidebar tab no longer shows a second scrollbar around the message list (only the messages scroll), and the options bar wraps in a narrow sidebar.
- Saving the settings no longer writes back a copy of the user setting loaded when the tab was opened, which could undo
  a language chosen in the meantime.

### Known issues

## [1.2.0] - 2026-10-14

Four database migrations (`V10` to `V13`): a copy of the database is saved before they run, and 1.1.0 cannot open
a migrated database.

### Added

- `InferenceService.infer`: asks the model for the whole answer at once (no streaming, still asynchronous). With a JSON
  schema the provider is asked to follow it and the callback learns whether the answer is valid (`SchemaCheck`); with a
  Java class the answer is read into an object. The schema of a class is built from its fields and can be refined
  with the new annotations `@LLMSchema` and `@LLMField` (`StructuredOutputs`). The model can call functions during `infer` too. `InferenceService.stream` can pass a response schema to the provider (not validated). New dependency `json-schema-validator`.
- Settings → **Tags**: a table of all your tags with the number of objects related to each. A tag can be renamed or
  deleted (its relations are deleted with it). Renaming a tag to the name of another tag asks whether to merge them:
  yes moves the relations to the existing tag and deletes the renamed one, no leaves the tag unchanged
  (`TagService.findUsageForUser/findRenameConflict/rename/merge`, `TagRelationService.moveRelations/countRelationsByTag`;
  `TextInputDialog.Builder.initialValue`).
- Admin → Extensions: a **#** button next to the version of each extension shows the SHA-256 hash of its JAR on hover
  and copies it to the clipboard on click (`OsgiService.getSha256`).
- Shared inference providers: an administrator can check **Shared with all users** in a provider's dialog. Every user
  can then use it in their books, but not see its address or API key, change it or delete it. The rules are enforced in
  `AIService`, not only hidden in the UI (migration `V10`). A setting **Users can create their own models** (above the
  user list in the administration) lets administrators restrict users to the shared providers.
- Token usage per provider: the Inference Providers tab shows the tokens sent and generated with each provider, per user
  (a shared provider counts separately for each user); administrators get a **Usage per user** table with the tokens,
  requests and time of the last request of every user and provider (migration `V11`). The numbers are what the service
  reports (Marginalia asks for `stream_options.include_usage`) or a local estimate, and, unless an administrator limits a user (see below), nothing is limited by them.
- Limits of shared providers: the administrator who owns a shared provider sets, per user, a limit - unlimited, a token
  limit (tokens sent, generated and in total, each optional, reset daily, weekly or monthly) or not available - in the
  new **Limits** dialog of the Inference Providers tab. A request of a user who is over the limit or has the provider
  not available fails before anything is sent (a prompt that would go over the sent or total limit is refused, only the generated tokens can go over, unless the administrator makes that limit **hard**: then a request whose reserved
  response (the maximum response size) would not fit is refused too;
  `InferenceException` types `NOT_AVAILABLE` and `LIMIT_REACHED`, so
  generations, summaries and extensions all stop). Users see their own limit and what they used with the **info**
  button of a shared provider. The limits are stored in the provider (extended attribute `AI.limits`, a JSON object
  keyed by the uuid of the user, `AILimits`); the tokens used in the current period are two new columns of the `Usage`
  (migration `V12`), reset lazily when the user's last use is from an earlier period. New `AILimitService`; plugins
  that decorate `PhoneAIPart.createActions` see the new `infoButton` and `limitsButton`.
- LLM function calling: the model can call Java methods of an interface marked `@LLMFunction`
  (`FunctionRegistry`, `FunctionSet`, `FunctionContinuation`; asynchronous, JSON schema generated from the parameter
  types, up to 10 rounds per request with the last one forced to answer). `OpenAICompatibleInferenceService` runs the
  calls; extensions use it through `InferenceService.stream(payload, protocol, functions, callback)`.
- `InferenceServices.addToolRoleResolver` / `removeToolRoleResolver`: an extension can change the role the results of
  function calls are sent with.
- New widget `GraphView` (`ui.widgets`, `@PublicAPI` from 1.2.0): a graph drawn with Cytoscape.js (npm package
  `cytoscape`, `graph-connector.js`, `JavascriptLibrary.renderGraph`) - nodes with colors and images, labelled edges
  with arrows, nodes arranged in rings around a center node, re-centered by a double click. Plugins can use it.
- Plugin: **Character Tracker** — a *Characters* tab in the story sidebar: the characters of the book (name, tagline,
  note, icon scaled to at most 200 x 200 pixels, color) with relationships between them (directed or unidirectional,
  with a name and a tagline), a relationship map (double-click a character to arrange the map around it), and an import of lorebook entries (of the book's
  lorebook and its sub lorebooks) as characters. Stored in the attributes of the book. No
  changes to the application.
- Plugin: **Gemma 4 Tools Fix** — sends the results of function calls to Gemma 4 models with the role
  `tool_responses` instead of `tool`.
- Plugin: **Name generator** — the model can call `list_name_categories` and `generate_names` to get names from Markov
  chains instead of inventing them. About 1200 categories, trained at build time from the Onomastikon, fantasygen,
  Corpora and markov-namegen-lib (the build clones them, only the probabilities are committed). The length of the chain
  and the usual lengths of the names are set per category in `src/build/chains.json`, written by the tuning step
  (`mvn exec:exec@tune-chains`).
- Lore Generator: **Name tools** gives the model the functions of the Name generator for the categories you check, in a
  lazy table with a filter (the checked categories stay in it). The checked categories are remembered per entry with
  the other options of the generation. The default hint lets the model modify or remix the names it gets.
- Highlight of generated text: the settings menu (⚙) of the story has the checkable **Highlight Generated Text**. While
  it is on, the text of every part that is still the one the LLM wrote is highlighted, what the author added or
  rewrote is not (a word-by-word diff of the part against its new extended attribute `ChatMessage.originalResponse`,
  which generation fills and editing leaves alone). The switch is not saved. Existing parts get their current text as
  the original by the app migration 3 → 4 (`OriginalResponseMigration`, deleted parts too; no SQL migration), so what
  was edited before cannot be told apart. Only the desktop story tab has it. Plugins: `ChatMessage.originalResponse`,
  new `@Extendable` methods `ManuscriptStoryPart.createHighlightGeneratedMenuItem`, `setShowGeneratedDiff` and
  `ChatMessageCard.renderResponse` (`@PublicAPI` from 1.2.0).
- Reviewer plugin: a book can have its own reviewer profile. The info tab of the book (desktop and phone) has a
  **Reviewer Profile** combo box under the protocol; the chosen profile takes priority over the one selected in the
  settings (cleared: the selected one is used). The choice is stored in the book's attributes by the uuid of the profile,
  which profiles now have (profiles saved before get one the first time they are loaded), so renaming a profile keeps it.
  **Advanced Options** has a **Prompt from Profile** combo box that fills the prompt override with the review prompt of
  the chosen profile (only the prompt, not its model, protocol or pre-prompt).

### Changed

- A protocol can lower the context and response limits of a provider, but no longer raise them above the provider's.
- The story editors share their code: `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` use the new classes of
  `ui.dialogs.story` (`StoryCards`, `StorySupport`, `StoryBackup`, `StoryGenerationListener`, `NewTurnForm`,
  `TurnDetailsForm`, `MessageMetrics`) and are about 300 lines shorter each. Plugins that decorate methods of these two
  classes should be checked against the new code.
- The phone workspace classes and more of the desktop UI classes are annotated with `@PublicAPI(from = "1.1.0")`
  and `PublicAPISignatureTest` checks the annotations against the compiled classes; the phone twins of the book tabs
  are listed as extension points in the developer manual.
- Vaadin is built with `optimizeBundle=false`.
- Lore Generator: the dialog is split into smaller classes (`NameToolsDialog`, `SavePromptAsDialog`,
  `AttachmentPanel`, `GenerationPane`).
- Deleting takes what belongs to the deleted object with it: a book its story parts, their summaries and its tags, a
  story part its summary and the parts below it, a lorebook its entries and their tags, an inference provider its usage
  numbers. The Trash restores them together with the object, and the cleanup keeps the contents
  of a deleted object it can't remove yet, so a restorable lorebook doesn't lose its entries. Services: `delete(x,
  false)` of `ManuscriptService`, `ChatMessageService`, `LorebookService`, `LorebookEntryService` and `AIService`
  cascades; new `TagRelationService.deleteForObject`, `ChatMessageService.softDeleteForManuscript`,
  `SummaryService.softDeleteForManuscript` / `softDeleteForMessages`. The messages and summaries of a book are soft
  deleted with one bulk update, so extensions get no save event for them.
- Trash: **Delete Permanently** removes deleted story parts and lorebook entries (with their summaries and tags) from
  the database right away, for users and administrators; other objects still wait for the cleanup, so nobody removes a
  whole book by accident. The parts below a removed part move to its parent. New `TrashService.deletePermanently` /
  `getPermanentlyDeletableTypes`; `TrashGrid` and `PhoneTrashGrid` get `createDeleteButton`, `deleteSelected` and the
  field `deleteButton` (`@PublicAPI` from 1.2.0).
- `@ExtendedAttribute(inject = true)` now works for any Java type, not only `JsonObject`: other types are converted
  to and from JSON with Gson (objects are stored flat as `<injectPrefix>_<key>`, lists and scalars under `injectPrefix`).

### Fixed

- Inference providers: the API key typed into a new provider is no longer lost when you visit *General Settings* before
  **OK** (the dialog read the enabled state of a field in a hidden tab). **Refresh** keeps the selected model. An empty
  API key works for local servers: a placeholder (`none`) is sent instead of failing in the client library.
- Regular users no longer get an *Application Error* dialog after every login (the admin part is refreshed only for
  administrators).
- Admin → Cleanup: purging an AI that has usage rows no longer fails (`usages.ai_id` is nullable, migration `V13`).
- Lore Generator: the Name generator is found by reflection and not imported, so it works when it is installed after the
  Lore Generator, and again after it was unloaded and installed again.
- The error dialog showed its message as HTML, so markup in an error message (provider replies, user input echoed in an
  exception) was rendered. It is shown as plain text now, line breaks kept.

## [1.1.0] - 2026-10-12

No database migration: a 1.0.0 database opens unchanged.

### Added

- Phone layout below 768 px: a phone workspace with a drawer, a phone book window, a phone story editor and phone
  versions of the book, lorebook, provider, protocol, settings and trash pages. The layout is chosen at page load.
  Not available on the phone: administration pages, the full lorebook editor and *Resources* (see the user manual).
- Per-user UI language: a language selector in the footer (only installed languages are listed), stored in the user
  settings and applied to all open sessions of the user. Japanese is built in.
- Extensions can add languages (`ExtensionHasLocalization`). The new Chinese Localization plugin is the worked example.
- Token breakdown: the tokens of every message of the final prompt are recorded with each generated part (parts from
  before have none) and shown in new Token Usage views with charts.
- Message attachments (images, audio, other files) can be passed to the model by the inference services. Used by the
  Lore Generator plugin; whether the model uses them depends on the model.
- Extension management: an extension can be stopped and started again without uninstalling it (`<jar>.STOPPED` flag
  file), and each extension has its own log file, rotated at 2 MB, shown in Admin → Extensions → Log. Plugins create
  their logger with `OsgiService.createLogger(bundle)`.
- Extensions can ship their own CSS (`ExtensionHasCss`), removed again when the extension is unloaded.
- Plugin: **Lore Generator** — generates the content of a lore entry from the existing lore, with saved prompts,
  attachments and a compaction step.
- Plugin: **Instruction Exporter** — copies the scene setting, POV character and present characters of the active
  branch to the clipboard.
- Plugin: **Chinese Localization** — Chinese UI texts as an extension.
- Root `pom.xml` as the parent of the application and all plugins: the version is defined in one place and
  `mvn package` in the root builds everything.
- `plugins/Dockerfile`, `plugins/docker-compose.yml` and `rebuild.sh` / `rebuild.bat` to build the plugins in a
  container and restart the stack with them.
- Documentation: user pages for the phone layout and the new plugins; developer pages for the new extension
  interfaces, token breakdown, message attachments and the parent POM.

### Changed

- Only the newest version of an extension is loaded at startup; older versions are moved to a separate folder.
- Story redraws keep the open tab sheet and the open reasonings.
- UI texts that were hard-coded (tree node cards, bundle states, error dialog button, prompt format list, login
  description, `ERROR` fallbacks) are localization keys.
- Inline styles were replaced by CSS classes (`shared-styles.css`, `CssStyles`) and all browser-side JavaScript moved to
  `ui.JavascriptLibrary`.
- The bundled plugins use the extension logger instead of `LoggerFactory`.
- Plugins built for 1.0.0 must be rebuilt from the 1.1.0 source tree; the plugin API is not frozen and the extension
  verifier shows plugins that no longer match as *Invalid*.

### Fixed

- A page reload logged out every user who had not chosen *Save login*; a user who is already logged in this session is
  now kept.
- `SessionManager` kept the session-scoped `user` proxy, so user filters running on another thread failed with
  `ScopeNotActiveException`; it now stores a plain copy taken at login.
- The plugin Docker image no longer runs the Vaadin frontend build.
- Summaries: the inference now starts after the transaction that creates the summary has committed. A fast answer
  could be saved before the new summary row was visible, which failed with `OptimisticLockException` and left the
  summary request unanswered (seen as intermittent `MetaSummaryTest` timeouts in CI).

### Known issues

- The Japanese and Chinese texts are machine translations that a native speaker should review.
- The phone layout does not follow a rotation, and `PhoneManuscriptStoryPart` duplicates much of
  `ManuscriptStoryPart`.
- Plugins' own texts, fixed words in exported files and default object names created by services are English only.
- A restored story part does not get its former children back.

## [1.0.0] - 2026-10-10

Initial release.

- Branching story tree (regenerate, swipe, branch), lorebooks with tag, text and regex activation and nested lorebooks,
  summaries and meta summaries, Handlebars and SillyTavern-macro prompts, SillyTavern import.
- Search across all branches, story export (TXT, Markdown, HTML, DOCX, PDF, EPUB), images on story parts.
- OpenAI-compatible providers with timeout, retries, classified errors and a connection test.
- Per-book backups, scheduled database backups with staged restore, a copy of the database before a migration, trash
  with restore.
- Login throttling per user and per address, encrypted API keys, server-side admin checks.
- OSGi extension system with verification of bundles, and the Author's Note, Chapter Marker, Lorebook VCS, Reviewer and
  Side Query plugins.
- Docker image (amd64, arm64), WAR and desktop archives for five targets.

[Unreleased]: https://github.com/Enerccio/Marginalia/compare/v1.3.0...HEAD
[1.3.0]: https://github.com/Enerccio/Marginalia/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/Enerccio/Marginalia/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Enerccio/Marginalia/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Enerccio/Marginalia/releases/tag/v1.0.0
