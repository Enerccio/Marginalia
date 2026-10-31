# TODO - Beta Readers

The plugin so far has the settings panel and the inherited settings, the persona and medium libraries and the model of the chats and
the prompt (steps 1-3); there is no chat yet.

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

**Step 4 - which parts are sent.**
- `PartsTracker`: the parts of the active branch with a hash of their text, what the messages of the chat have sent, the difference
  (added, changed, gone after a switch of the branch), the context of the first message (the summaries and then the parts after
  the newest summary within the budget, up to the spoiler limit, like the Reviewer's *With summaries*), the text of the "new stuff".
- Tests: new parts, a changed part, a deleted message giving its parts back, a branch switch, the spoiler limit, the first message.

**Step 5 - reading a reply.**
- `ReplyReader`: JSON through `PartialJson` (only the finished messages, the unfinished one hidden, a broken reply keeps what was read,
  the messages over the limit dropped, the reasoning), the free text with `**Name:**` prefixes, a reply that is neither shown as text.
- Tests: a reply cut at every position, broken JSON, an unknown speaker, too many messages, the free text.

**Step 6 - asking the model.**
- `BetaReadersService.ask(chat, message, includeLatest, listener)`: finds the AI and the protocol (the tab, the chain, the book), builds the
  request with the schema or without, streams through the inference service as Side Query does, gives the listener the finished messages
  and the reasoning, can be stopped, stores the reply; `regenerate(chat)`, `edit`, `delete`, `addPersona` / `removePersona` (the saved
  lines of the history).
- Tests with a fake stream: the reply stored, a stop in the middle, regenerate, the delete of one message of a turn, the join line.

**Step 7 - the chat tab, in three commits.**
- 7a: the tab in the story sidebar (`renderStoryContent` of `ManuscriptStoryPart` and of the phone twin, a `TabDock`, as Side Query
  and the Author's Note do) with the tabs of the chats, *New chat* (`ui/NewChatDialog`: 1v1 or group, the personas, the medium, the
  AI and the protocol), rename, close, delete; the personas of the book (`PersonaLibraryDialog` with the book as its scope).
- 7b: the messages (`ui/BetaMessageCard`: the icon and name of the speaker, the narrator without an icon, the reasoning as collapsed
  details above the turn), the composer (the text, *Include the latest parts* with the count of new parts, send / stop), streaming
  with the unfinished message hidden, the token counter, the line where parts were sent.
- 7c: regenerate the last reply, edit a user message, delete messages, add / remove a persona (the dialog that asks for the
  "prompt" and inserts the line), *Update from the library*.

**Step 8 - the spoiler limit and the context options.** The limit (a part of the active branch) in the tab, the range of parts of the
first message, the display of what the personas have read; the phone layout checked.

**Step 9 - finish.** README, the user manual page `manual/user/extensions/beta-readers.md` (+ the screenshots, `screenshots/flows`), the
entry in the plugin tables, the `CHANGELOG.MD` entry (*Added: the plugin Beta Readers*, written at 7b, when the plugin first does
something users see), the dev notes if something new was learned, `tools/retype_build.sh`, ja / zh localization checked, the checklists
below ticked.

## Settings

- [x] User settings panel of the plugin (*Settings → Extension Settings*): the single-persona prompt, the group prompt, the default
  medium, the persona library.
- [x] Inherited settings (`InheritedSettingRegistry`): single-persona prompt, group prompt, medium (a choice), the AI and the
  protocol (two choices listing the AIs and the protocols of the user, stored by uuid; an AI or protocol that is gone counts as not
  set and the book's is used); `userValue` from the settings panel, `fallback` the built-in default.
- [ ] Medium: what the text stands for (book, movie, TV series, manga/comic, audio drama, stage play, game / visual novel...). Each
  medium has a name and a prompt fragment that goes into both prompts (`{{medium}}`); a small library with defaults that the user
  can edit and extend. A chat can override it; changing it in a chat leaves a line in the history ("now read as: manga").

## Personas

- [x] Persona: name, icon, description (the name and the title, like "Athena - The Analytical Archivist", are part of the name and
  the description). Without an icon: a circle with the first letters of the name, as the `TabDock` tabs have.
- [ ] A chat stores a copy of its personas; *Update from the library* in the chat (optional) takes the current definition.
- [x] Storage of the library (user settings) and of book-local personas (attributes of the book).
- [x] Editor: create, edit, copy, delete, reorder personas.
- [x] Ready-made starter personas the user can copy (the library starts with six; *Add the starter personas* adds the missing ones).
- [ ] Generate a persona from a short description with the model.
- [ ] Global per user (done, the library) + create persona in book (the same window with the book as its list, step 7a)
- [x] Small icon (saved as same as character-tracker; uploading it is the persona dialog of step 2)

## Chat

- [ ] Chat tabs: new chat (1v1 with one persona, or group chat with chosen personas), rename, close and delete; stored with the book.
- [ ] Per tab: the AI and the protocol to use instead of the inherited ones (empty: inherited, shown as the placeholder with where it
  comes from, like the fields of the settings dialogs); changing it takes effect with the next message.
- [ ] Chat panel with streaming answers and a stop button.
- [ ] **Regenerate the last reply** (button): drops the last turn and asks again; the roster is built again and the parts sent by the
  message before it stay sent.
- [ ] **Edit a user message** in the history, and **delete messages** (a user message, or one message of a turn: the turn keeps the
  rest and is sent back to the model as the JSON without the deleted one). What was sent with a deleted or edited message is
  recomputed from the messages that are left (see *New parts*).
- [ ] The reasoning of a turn as collapsed details above its messages; the messages of the turn below it, each with the icon and
  the name of its speaker, the narrator without an icon.
- [ ] Choose the personas that take part; address one by name; let the others answer in turn.
- [ ] What the personas know about the book: lorebook, summaries, a range of parts, the part the user is reading.
- [ ] Spoiler limit: a persona reads only up to a chosen part.
- [ ] History of the conversations, saved with the book; clear and delete.
- [ ] Token budget of the context, shown to the user.
- [ ] Option to use json schema or freefall
- [x] Json schema output. Preliminary structure (the `speaker` is restricted to the names present plus `Narrator`, built per request):
  `{"reasoning": "...", "messages": [{"speaker": "Athena", "parts": [{"type": "action" | "speech", "text": "..."}]}, ...]}`
  - the narrator describes the scene, the personas speak and act (body expressions), the parts of a message are interleaved.
- [ ] Reading a reply while it streams (`PartialJson`): show the messages that are finished, hide the one being written, ignore the
  rest of a broken reply but keep what was read; a reply that is not JSON at all is shown as text.
- [x] The roster in the last user message (not saved): names and descriptions of the personas present, and who may speak.
- [ ] Prompt should encourage interaction between personas
- [ ] A group chat is an interleaved chat between the defined personas, a 1v1 chat is a chat with the one persona (the 1v1 prompt).
- [ ] If the user adds / removes a persona (group chat only), it is done via a special button interaction that prompts the user for a
  "prompt" and inserts an interleaved user message telling the model that the persona joined / left (a saved line of the history);
  the persona is added to (or removed from) the roster of the last user message, and the chat stores a copy of it.

## New parts

- [ ] Each user message stores which parts it sent (the ids of the parts and a hash of their text); what a chat has sent so far
  is the union over its messages, so deleting, editing or regenerating a message gives the parts back.
- [ ] The message box has a checkbox **Include the latest parts**; it shows how many parts are new (added, or changed since they
  were sent). When checked, the message sends them as "new stuff" (the new text, marked as new, before the user's message; a
  changed part says that it changed) and the chat stores them as sent. Unchecked: nothing is added, what was sent stays in the
  history and is not marked.
- [ ] The first message of a chat has nothing sent yet: with the box checked it sends the context (summaries, then parts after the
  newest summary, within the token budget, like the Reviewer's *With summaries*), up to the spoiler limit.
- [ ] Switching the branch: compare with the common part of both branches; parts of the other branch count as changed.
- [ ] A line in the chat where parts were sent ("parts 41-43 sent"), so it is visible what the personas have read.
- [ ] If the history and the new parts do not fit the token limit, the context is built again (summaries and the latest parts) and the
  history is trimmed.

## Plugin basics

- [ ] Localization (`BetaReadersLocalization`, ja/zh entries).
- [ ] README, user manual page (`manual/user/extensions/beta-readers.md`), developer notes if needed.
- [ ] Entry in the plugin list of `marginalia/plugins/README.md`, `CHANGELOG.MD` entry when it does something.
- [ ] Tests for the service (prompt building, storage of personas, the new-parts diff).
