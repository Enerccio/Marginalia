# TODO - Beta Readers

The plugin is finished apart from what is listed as open below (steps 1-9 are done): the settings, the libraries, the chats, the conversation and
how much of the story the personas read, with the user manual page and its pictures.

## Goal

The user defines personas (beta readers: a picky editor, a fan of the villain, a genre skeptic...) and chats with them
about the book.

## Decided

- A persona is only a name, an icon and a description (no model of its own, no settings). The model is a property of the chat.
- Two kinds of chat, chosen when the chat is started and never changed:
  - **1v1 chat** with one persona, uses the single-persona prompt;
  - **group chat** with one or more personas, uses the group prompt. A group chat may start with a single persona, so others can
    be brought in later. Starting a group chat with one persona is a different thing from a 1v1 chat.
- Both prompts and the medium are inherited settings (book, folder, user level in the plugin's settings panel), see
  *Plugin development → Inherited settings*.
- Several chats per book, each in its own tab.
- Which parts of the story a chat has seen is stored in the chat, and a message can send the parts that are new (see *New parts*).
- Output of a chat: both JSON (a schema) and free text with `**Name:**` prefixes are supported, an option per chat; JSON is the
  default when the protocol supports structured outputs.
- **The reply is a list of messages.** One reply of the model is one *turn*: its reasoning and then a list of messages that the
  personas (and the narrator) say in turn, so one reasoning covers several messages that the user sees as separate ones.
- **The personas are not in the system prompt.** The roster (the names and descriptions of the personas present) is added to the
  last user message when the request is built, not saved in the chat and added again with every new message. The system prompt
  (the prompt and the medium) and the history never change, so the cache of the prefix stays; adding or removing a persona only
  changes the roster. A persona that joins or leaves is still a saved line in the history ("Mara joins") so the model sees when.
- **Reasoning** is a small field of the schema, first, before the messages (the model writes a short reasoning, then the messages).
  The chat shows it as collapsed details above the messages of the turn, like the reasoning elsewhere in the application. A model
  with native reasoning shows that in the same place and the field is left out of its schema.
- **A chat keeps a copy of every persona it uses** (name, icon, description at the moment the persona was added), so editing or
  deleting the persona in the library or the book does not change the chats. A book-local persona wins over a global one when the
  persona is picked.
- **The model and the protocol** of a chat: the choice of the chat tab itself (highest priority, optional), else the settings
  chain (book, then the folders above it, then the user level of the plugin), else the AI and protocol of the book.
- **A 1v1 chat has the same structure as a group chat** (the same schema, the same reply of messages, the description of its one
  persona in the last user message); only the prompt differs, and there is no joining or leaving.
- While streaming, the messages that are not finished are hidden and the broken JSON is tolerated (`PartialJson`, as the
  generators of Arc Outline do); a message is shown when it is complete.
- The limit on the messages of a reply is `maxItems` in the schema, and enforced when reading the reply too (the extra ones are
  dropped), because a model may not honor it.

## Plan: the steps (one commit each, the plugin builds and loads after every one)

Build before every step: `mvn install -DskipTests` in `marginalia/` (the plugin compiles against the installed app), then
`mvn -o -q package` in this folder; run only the tests of the step. The package is `extensions.betareaders` with `model`,
`service`, `ui` and `commands` below it, as in the other plugins. Tick the boxes of the lists below as the steps are done.

**Step 1 - the model and the storage of personas and mediums (done).**
- `model`: `Persona` (uuid, name, icon as base64 PNG like `StoryCharacter.icon`, description), `Medium` (id, name, prompt fragment),
  `BetaReadersSettings` (the two prompts, the id of the default medium, the library of personas, the library of mediums, the
  uuids of the default AI and protocol), `BookPersonas` (the personas of one book).
- `BetaReadersService`: load / save the settings in `UserSetting.getAttributes()` and the book personas in the attributes of the
  book (`settingService.save`, `manuscriptService.save`, a key of the plugin); the built-in starter personas (Athena, Vesper,
  Ember and a few more), the built-in mediums and the default texts of the two prompts; `PersonaIcons.createIcon(bytes)` (a copy of
  the Character Tracker's: a PNG at most 200 pixels) and, for a persona without an icon, the circle with the first letters of its
  name that the tabs of a `TabDock` have (`TabDockIcons`, the color follows the uuid).
- `personasFor(manuscript)`: the library and the book's, the book's wins by name.
- Tests: the round trip of the settings and the book personas, the defaults when nothing is stored, the book winning by name.

**Step 2 - the settings panel and the inherited settings (done).**
- `BetaReadersExtension` decorates `UserPart.refresh` / `save` (the panel *Beta Readers* in *Extension Settings*, as the Reviewer
  does) with `ui/BetaReadersSettingsForm`: the two prompts, the default medium, the AI and the protocol, and the buttons that
  open the editors of the persona library and of the mediums.
- `ui/PersonaDialog` (name, icon upload and remove, description), `ui/PersonaLibraryDialog` (list, add, edit, copy, delete,
  reorder, *Add the starter personas*), `ui/MediumLibraryDialog`.
- Register the inherited settings in `onExtensionLoad` (single prompt, group prompt, medium, AI, protocol; `userValue` from the
  settings, `fallback` the built-in text) and `unregisterAll` in `onExtensionUnload`.
- `BetaReadersLocalization` (`T.localized`, ja / zh entries) from here on, for every text added.
- Tests: the registration (kinds, user level, fallbacks, the choices), every text having its ja / zh entry, the library windows
  without a browser (copy, move, starters, the editors and their name checks). The chain itself is the core's
  (`ExtensionSettingsInheritanceTest`); a deleted AI counting as not set is the lookup of step 6.
- Not tried in a running application yet: the panel in *Settings → Extension Settings* and the section in the dialogs of a book and
  a folder (load the JAR and look, before step 3 builds on it).

**Step 3 - the chat model and the prompt, no UI, no model call (done).**
- `model`: `BetaChat` (uuid, title, `ChatKind` 1v1 or group, `ReplyFormat` auto / JSON / text, the personas as `ChatPersona` copies with a
  `present` flag so a persona that left keeps its copy, the medium, the AI and protocol of the tab, the messages), `BetaMessage` (kind
  USER / ASSISTANT / JOIN / LEAVE; user: the text, the "new stuff" block as it was sent, the `SentPart`s; assistant: the reasoning and the
  list of `BetaSpoken` with their `BetaPart`s of type action / speech, or the raw text of a reply that could not be read). The chats are
  in `BookData`, there is no separate `BetaChats`.
- `Reply` (record, `@LLMSchema`) is the schema of one turn and `BetaSchemas.replySchema(speakers, maxMessages, withReasoning)` builds it per
  request: the `speaker` enum is the personas present plus `Narrator`, `maxItems`, the `reasoning` field only for a model without native
  reasoning, no `allOf` around the items, the part types in lower case.
- `PromptBuilder.build(chat, renderedSystemPrompt, options)`: the system message is the prompt and "How to answer" (JSON or text), the
  history (replies sent back as the JSON or the text they were, the join / leave lines and the next user message as one message), the
  roster added to the last user message only (a message of its own when the history ends with a reply), and the token limit (the oldest
  messages are dropped, the last one stays). `TextFormat` renders messages as `**Name:** *action* speech`; reading it is step 5.
  `BetaReadersService.renderPrompt(template, medium)` renders the prompt as a template (`BetaReadersTemplateData`: `{{medium}}`).
- Tests (`PromptBuilderTest`, `BetaSchemasTest`, `TextFormatTest`, the chat round trip in `BetaReadersStoreTest`): the roster only in the
  last message and not stored, the prefix not changing when a persona joins, joining and leaving lines, 1v1, JSON and text history, the
  schema validating a good answer and refusing a speaker that is not present, too many messages and a reasoning it does not have, the limit.

**Step 4 - which parts are sent (done).**
- `PartsTracker` (pure, works on `StoryPart(id, number, text)`): what a chat has sent is replayed from its messages (`sentBy`), so deleting
  or editing a message gives its parts back; `status` (new, changed, gone, first) for the checkbox; `plan` for what a message sends: the
  context of the first message (the summaries, then the parts after the newest of them, the newest that fit the token budget) or the
  "new stuff" (new parts, changed ones marked, a note for parts that are gone after a branch switch). Everything the message considered is
  stored as sent, also what the summaries stand for and what did not fit (the plan says how many were left out), so it is not sent later out
  of order; the gone parts are stored with the message (`BetaMessage.goneParts`) and forgotten. `SentPart` has the number for the line
  "parts 41-43 sent" (`ranges`). A switch back to the other branch sends its parts as changed. The spoiler limit is a parameter already.
- `BetaReadersService.loadStory(book, maxNumber, withSummaries)` reads the active branch and gathers the valid summaries as a generation does.
- Tests (`PartsTrackerTest`): new, changed, nothing new after sending, a deleted message giving its parts back, a branch switch and back,
  the spoiler limit, the summaries and the budget, ranges. `loadStory` is not covered (it needs the application).

**Step 5 - reading a reply (done).**
- `ReplyReader.read(text, done, speakers, maxMessages)` returns a `Reading` (reasoning, messages, raw text, how many were dropped;
  `toMessage()` makes the line of the history). The format is judged from the text: JSON when it starts with a brace or a bracket (after
  an optional markdown fence), else free text.
- JSON through `PartialJson`; while the reply streams only the finished messages are returned, found by scanning for the closing braces
  of the objects of the `messages` array (`messageEnds`), so a message appears when it is closed, not when the next one starts. A reply
  that is broken after some messages keeps them; a reply that is cut for good keeps the message that was being written. The reasoning
  shows as it is written. The speaker is taken in the chat's spelling, an unknown one is kept as written; parts of an unknown type are
  speech, empty parts and messages are skipped. A bare array of messages is read too.
- Free text: a message starts with `**Name:**` (or `**Name**:`) of a speaker that is present or the narrator, so a bold word in a speech does
  not start one; `*...*` is an action, the rest speech; text before the first speaker is the narrator's; the last message is hidden until
  the reply is done.
- The messages over `maxMessages` are dropped (also while streaming). A reply with no messages is the raw text once it is done, and is not
  shown while it streams.
- Tests (`ReplyReaderTest`): a reply cut at every position (never fewer messages, never changed ones), broken JSON, an unknown speaker,
  too many messages, the markdown block, bare array, free text and what is neither.

**Step 6 - asking the model (done).**
- `ChatRunner.run(inference, protocol, request, listener)` (pure, takes any `InferenceService`): builds the prompt and, for JSON, the schema, streams
  with `stream(payload, protocol, schema, ...)`, tells the listener what `ReplyReader` has read after every chunk, and ends exactly once with an
  `Outcome` (the reply for the history, stopped, failed, the prompt with how many messages were trimmed, JSON or not). A stopped reply keeps what was
  read, a failed one is not kept, a model that wrote nothing is an `EmptyReplyException`. The reasoning chunks of a model that reasons on its own show
  where the field would be, unless the reply has the field. `ReplyFormat.AUTO` is JSON and falls back to text when the service takes no schema,
  `JSON` does not fall back, `TEXT` asks without a schema.
- `ChatEdits` (pure): edit a message of the user (the parts it sent stay), delete a message, delete one message of a turn (the turn keeps the rest,
  a turn left empty goes), take the last reply back, `join` / `leave` (the saved lines; group chats only, the last persona can't leave, a persona that
  left comes back with its copy). The prompt now merges replies that follow each other after a delete and drops a history that begins with a reply.
- `ModelChoice` (pure): the first candidate that exists and is not deleted.
- `BetaReadersService`: `resolveModel(book, chat)` (the tab, the chain, the book), `systemPrompt(book, chat)` (the prompt of the kind from the chain and the
  medium of the chat or the chain's), `getChats` / `saveChat` / `deleteChat`, `ask(book, chat, text, includeLatest, listener)` (builds the parts with
  the room the prompt leaves, stores the message of the user, asks, stores the reply before the listener hears of it), `regenerate(book, chat, listener)`
  (also the retry after a failure). The number of messages of a turn follows the kind and the size of the group (`BetaReadersDefaults.maxMessages`).
- Tests (`ChatRunnerTest` with a fake stream, `ChatEditsTest`, `ModelChoiceTest`): the schema and the prompt of the request, reading while it streams, the
  reasoning, text and the fall back, stopped / failed / empty, the limits, one end per run, edits, deletes, joining and leaving, the merged turns.
  Not covered (they need the application): the methods of the service above. The reply is stored from the thread of the inference service inside the
  request of the user that asked (`InRequestScope`), as the Anthropic plugin does for the usage; check it when the chat tab exists (7b).

**Step 7 - the chat tab, in three commits.**
- 7a (done): `BetaReadersView` is the tab in the left bar of the story (`renderStoryContent` of `ManuscriptStoryPart` and of
  `PhoneManuscriptStoryPart`, as Arc Outline does): a tab per open chat, `+` for a new chat, a menu (new, rename, close, delete, *Personas of this
  book* with the `PersonaLibraryDialog`, *Closed chats* to open one again). A closed chat is kept (`BetaChat.closed`), the open tab is remembered
  in the book (`BookData.activeChat`). `NewChatDialog` (kind, title, personas from the library and the book, medium, model, protocol and reply
  format; what is left empty is inherited and the field says from what). `ChatPanel` shows what the chat is made of (kind, medium, model, the
  personas with icons, the ones who left greyed); the conversation is 7b. Tests: `NewChatDialogTest`. Tried in the screenshot environment
  (`screenshots/flows/plugin-betareaders.mjs`, on a copy of `22-arcoutline`: the tab, the dialog, the menu, rename, close and reopen, the personas
  of the book, delete, the chats kept after a reload; the phone drawer by hand). The menu is a button with a context menu (a menu bar moved its
  one item into its overflow), the options of the dialog are folded away.
- 7b (done): `ChatPanel` is the conversation: the header (kind, medium, model, the personas in a closed section), the messages
  (`BetaMessageCard`: your message with *Parts 41-43 sent*, a line for a persona that joined or left, a `Turn` with the reasoning closed above
  the messages, an icon and the name of each speaker, the narrator without an icon, an action in italics, a reply that could not be read as
  written), and the composer (the text, *Ctrl+Enter*, *Include the latest parts* with the number of new parts or *Include the story (n
  parts)* for the first message, a button that checks again, *Send* that becomes *Stop*, and the size of the context). The turn that
  streams is redrawn at most every 100 ms, only finished messages show; the tabs are locked while a reply is written. When the reply ends
  (on the thread of the inference service) the panel refreshes inside the request of the user (`InRequestScope`), as the service does when it
  stores the reply: tried in the screenshot environment, this is what showed that a refresh without it fails. `BetaReadersService.ask` /
  `regenerate` return `Asked` (the message and the token), `partsStatus`, `contextUsage` and `NoModelException`. The `CHANGELOG.MD` entry
  is written. The fake model of the screenshots answers the personas (`betaReaders` in `dataset/plugins.mjs`, `betaRule`); the flow
  `plugin-betareaders.mjs` sends three messages and checks the reply, the parts sent, the context counter and that the conversation is kept.
  Tests: `BetaMessageCardTest`. Not done: the wait for the first token has no indicator.
- 7c (done): the cards have buttons (`BetaMessageCard.Actions`): edit and delete on your message, delete on every message of a turn and on a line
  (joined, left, medium), *Regenerate* under the last turn, *Ask again* under a question without a reply (not after a joined / left / medium
  line); the buttons are hidden while a reply is written. The personas section of the panel (it scrolls) has *Update from the library* (the
  description and icon of the copy, the name stays; `BetaReadersService.refreshPersona` takes the persona by uuid, else by name), *Send away* and
  *Bring in a persona* (`PresenceDialog`: which persona, and a note for the group). `ChatEdits.changeMedium` and the new line kind `MEDIUM`
  (the prompt says "[From now on the text is read as: ...]"); *Chat settings* in the menu of the tab (`ChatSettingsDialog`, the fields shared with
  `NewChatDialog` in `ChatOptionsForm`). The messages of a chat are a `CopyOnWriteArrayList`, so the panel can draw them while a reply is added on
  another thread. Tests: `ChatEditsTest`, `BetaMessageCardTest`, `PresenceDialogTest`; the flow `plugin-betareaders.mjs` edits, deletes,
  regenerates, brings a persona in, sends one away, updates one, changes the medium and checks the request the model got (the join and leave lines,
  the roster of the personas that are there).

**Step 8 - the spoiler limit and the context options (done).** Per chat (`BetaChat.spoilerLimit`, `fromPart`, `summaries`; the defaults are the whole story with
summaries, a chat stored before has them): *Personas read up to part*, *Start from part* and *Use the summaries of the story*, in `ChatOptionsForm`
(the new chat window, folded with the medium and the model, and *Chat settings*; a first part after the last one is refused). `PartsTracker.plan` and
`status` take the first part (`minNumber`) as well as the limit; the summaries are used only when the story starts at its beginning, and they are
gathered from the story up to the limit. A limit lowered under what was sent does not unsend it. The panel says how far the personas may read
(*Reads up to part 3 · from part 2*) and which parts they have read (`PartsTracker.read`). The panel was made tighter for the phone (compact padding,
a shorter text box, the status on one line); looked at in the drawer. Tests: `PartsTrackerTest`, `BetaReadersStoreTest`, `NewChatDialogTest`,
`PresenceDialogTest`; the flow checks that a chat limited to part 3 from part 2 sends parts 2-3 only and, with the limit raised, the new parts 4-5.
Not done: the lorebook is not sent (summaries and parts only); "the part the user is reading" is not a way to set the limit.

**Step 9 - finish (done).** README, the user manual page `manual/user/extensions/beta-readers.md` (+ the screenshots, `screenshots/flows`), the
entry in the plugin tables, the `CHANGELOG.MD` entry (*Added: the plugin Beta Readers*, written at 7b, when the plugin first does
something users see), the dev notes if something new was learned, `tools/retype_build.sh`, ja / zh localization checked, the checklists
below ticked.

## Settings

- [x] User settings panel of the plugin (*Settings → Extension Settings*): the single-persona prompt, the group prompt, the default
  medium, the persona library.
- [x] Inherited settings (`InheritedSettingRegistry`): single-persona prompt, group prompt, medium (a choice), the AI and the
  protocol (two choices listing the AIs and the protocols of the user, stored by uuid; an AI or protocol that is gone counts as not
  set and the book's is used); `userValue` from the settings panel, `fallback` the built-in default.
- [x] Medium: what the text stands for (book, movie, TV series, manga/comic, audio drama, stage play, game / visual novel...). Each
  medium has a name and a prompt fragment that goes into both prompts (`{{medium}}`); a small library with defaults that the user
  can edit and extend. A chat can override it; changing it in a chat leaves a line in the history ("now read as: manga").

## Personas

- [x] Persona: name, icon, description (the name and the title, like "Athena - The Analytical Archivist", are part of the name and
  the description). Without an icon: a circle with the first letters of the name, as the `TabDock` tabs have.
- [x] A chat stores a copy of its personas; *Update from the library* in the chat (optional) takes the current definition.
- [x] Storage of the library (user settings) and of book-local personas (attributes of the book).
- [x] Editor: create, edit, copy, delete, reorder personas.
- [x] Ready-made starter personas the user can copy (the library starts with six; *Add the starter personas* adds the missing ones).
- [ ] Generate a persona from a short description with the model.
- [x] Global per user (done, the library) + create persona in book (the same window with the book as its list, step 7a)
- [x] Small icon (saved as same as character-tracker; uploading it is the persona dialog of step 2)

## Chat

(The logic of most of these is done in steps 3-6; an item is ticked when a user can do it, which is the UI of step 7.)

- [x] Chat tabs: new chat (1v1 with one persona, or group chat with chosen personas), rename, close and delete; stored with the book.
- [x] Per tab: the AI and the protocol to use instead of the inherited ones (empty: inherited, shown as the placeholder with where it
  comes from, like the fields of the settings dialogs); changing it takes effect with the next message. (Choosing it when the chat is
  started is done in 7a; changing it afterwards, in 7c.)
- [x] Chat panel with streaming answers and a stop button.
- [x] **Regenerate the last reply** (button): drops the last turn and asks again; the roster is built again and the parts sent by the
  message before it stay sent.
- [x] **Edit a user message** in the history, and **delete messages** (a user message, or one message of a turn: the turn keeps the
  rest and is sent back to the model as the JSON without the deleted one). What was sent with a deleted or edited message is
  recomputed from the messages that are left (see *New parts*).
- [x] The reasoning of a turn as collapsed details above its messages; the messages of the turn below it, each with the icon and
  the name of its speaker, the narrator without an icon.
- [x] Choose the personas that take part; address one by name; let the others answer in turn.
- [x] What the personas know about the book: lorebook, summaries, a range of parts, the part the user is reading.
- [x] Spoiler limit: a persona reads only up to a chosen part.
- [x] History of the conversations, saved with the book; clear and delete.
- [x] Token budget of the context, shown to the user.
- [x] Option to use json schema or freefall
- [x] Json schema output. Preliminary structure (the `speaker` is restricted to the names present plus `Narrator`, built per request):
  `{"reasoning": "...", "messages": [{"speaker": "Athena", "parts": [{"type": "action" | "speech", "text": "..."}]}, ...]}`
  - the narrator describes the scene, the personas speak and act (body expressions), the parts of a message are interleaved.
- [x] Reading a reply while it streams (`PartialJson`): show the messages that are finished, hide the one being written, ignore the
  rest of a broken reply but keep what was read; a reply that is not JSON at all is shown as text.
- [x] The roster in the last user message (not saved): names and descriptions of the personas present, and who may speak.
- [x] Prompt should encourage interaction between personas
- [x] A group chat is an interleaved chat between the defined personas, a 1v1 chat is a chat with the one persona (the 1v1 prompt).
- [x] If the user adds / removes a persona (group chat only), it is done via a special button interaction that prompts the user for a
  "prompt" and inserts an interleaved user message telling the model that the persona joined / left (a saved line of the history);
  the persona is added to (or removed from) the roster of the last user message, and the chat stores a copy of it.

## New parts

- [x] Each user message stores which parts it sent (the ids of the parts and a hash of their text); what a chat has sent so far
  is the union over its messages, so deleting, editing or regenerating a message gives the parts back.
- [x] The message box has a checkbox **Include the latest parts**; it shows how many parts are new (added, or changed since they
  were sent). When checked, the message sends them as "new stuff" (the new text, marked as new, before the user's message; a
  changed part says that it changed) and the chat stores them as sent. Unchecked: nothing is added, what was sent stays in the
  history and is not marked.
- [x] The first message of a chat has nothing sent yet: with the box checked it sends the context (summaries, then parts after the
  newest summary, within the token budget, like the Reviewer's *With summaries*), up to the spoiler limit.
- [x] Switching the branch: compare with the common part of both branches; parts of the other branch count as changed.
- [x] A line in the chat where parts were sent ("parts 41-43 sent"), so it is visible what the personas have read.
- [x] If the history and the new parts do not fit the token limit, the context is built again (summaries and the latest parts) and the
  history is trimmed.

## Plugin basics

- [x] Localization (`BetaReadersLocalization`, ja/zh entries).
- [x] README, user manual page (`manual/user/extensions/beta-readers.md`, the pictures `plugin-betareaders-*` from the flow `plugin-betareaders.mjs`, chain step `23-betareaders`).
- [x] Entry in the plugin list of `marginalia/plugins/README.md`, `CHANGELOG.MD` entry when it does something.
- [x] Tests for the service (prompt building, storage of personas, the new-parts diff).
