# Beta Readers

Personas you define and a chat about the book with them.

A persona can be written by the model from a few words (*Write with the model* in its window).

See the [user manual](../../../manual/user/extensions/beta-readers.md). What is not done (generating a persona from a short description, sending
the lorebook) is in [TODO.md](TODO.md).

## Settings

*Settings → Extension Settings → Beta Readers* has:

- the **prompt of a chat with one persona** and the **prompt of a group chat** (empty: the built-in prompt; `{{medium}}` is the text
  of the medium),
- the **scene** (where and how the conversation takes place; empty: an online chat; sent in the system prompt of every chat, after the prompt or where it has `{{scene}}`),
- the **medium** (what the text stands for: a book, a movie, a manga...), the **model** and the **protocol** of the chats,
- the **persona library** (a name, an icon and a description; it starts with six personas) and the **mediums** (a name and the part of
  the prompt that tells the personas how to react), each edited in a window of its own.

The prompts, the medium, the model and the protocol are also *Extension Settings* in the *Prompts* tab of a book and in the settings of
a folder: a book uses its own, else the nearest folder's, else the ones here, else the built-in ones (the model and the protocol of the
book).

## The chats

The tab **Beta Readers** in the sidebar of the story has a tab for every open chat of the book (`+` starts one, the menu renames, closes and
deletes a chat, opens the closed ones again and edits the personas of this book). A chat is either a chat with **one persona** or a **group**
(which may start with one persona); the kind is chosen when it is started. A chat may choose its own medium, model, protocol and reply
format; what it leaves empty it inherits.

A reply is a turn: the thinking of the model, when it sends one (closed, above the messages), and the messages of the personas and the narrator, shown when they are
finished. The personas present are described at the end of your last message and are not stored, so adding or removing a persona does not
change the history. **Include the latest parts** sends the story: everything the chat has not read yet the first time, then the parts that
are new or changed. A message says which parts it sent, and the box under the text shows how many tokens the context uses.

You can edit and delete your messages and every message of a turn, **Regenerate** the last turn, and in a group **Bring in a persona** (with a
note for the group) or **Send away** one; the history keeps a line for each. **Update from the library** takes the current description and icon
of a persona into the chat's copy. **Chat settings** changes the medium, model, protocol and reply format of a chat; a changed medium leaves a
line in the history.

**How much of the story the personas read** is set per chat (when it is started and in *Chat settings*): **Personas read up to part** (the spoiler
limit), **Start from part** and **Use the summaries of the story** (the first message sends the summaries and then the parts after the newest
of them, as far as they fit the context). A line under the text box says which parts the personas have read.

## How it works

The plugin decorates `renderStoryContent` of `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` and adds the tab to their `leftBar`; the
settings panel is added by decorating `refresh` and `save` of `UserPart`, and the prompts, the medium, the model and the protocol are
registered as inherited settings (`BetaReadersInherited`). The logic is in plain classes with unit tests: `PromptBuilder` (the request),
`BetaSchemas` (the JSON schema of a reply), `ReplyReader` (reads a reply while it streams), `PartsTracker` (what was sent and what is new),
`ChatRunner` (asks any `InferenceService`) and `ChatEdits`; `BetaReadersService` is the part that needs Spring. The styles are in
`betareaders.css` (`ExtensionHasCss`).

## Storage

The persona library, the mediums and the settings are in the attributes of the `UserSetting` (under `BetaReadersSettings.KEY`), the personas of a book
and its chats in the attributes of the `Manuscript`, both as JSON (`BetaReadersStore`), so they are in the backups.
