---
label: Beta Readers
order: 107
---

# Beta Readers

Readers you invent, and a chat with them about your book. You describe a **persona** (a name, an icon and a few lines about how the person
reads and what they care about), and the model plays them: it reacts to the parts of your story as that reader would, alone or in a group
that argues with each other. Nothing the readers say is part of the book.

![A group chat with two personas](../../images/plugin-betareaders-chat.png)

## The Beta Readers tab

The left sidebar of the [story editor](../books/story-editor.md) gets a **Beta Readers** tab, next to *Outline*. It has a tab for every
open chat of the book. The **+** starts a chat, and the menu (⋮) next to it has:

| Item | |
|---|---|
| New chat | The same as **+**. |
| Rename chat | Changes the title of the chat. |
| Close chat | Closes the tab; the chat is kept and is in *Closed chats*. |
| Chat settings | Changes the [options of the chat](#the-options-of-a-chat). |
| Delete chat | Deletes the chat and its history, after a confirmation. |
| Personas of this book | The personas that belong to this book only, see [below](#personas). |
| Closed chats | Opens a closed chat again. |

The chats are saved with the book, so they are in its backups.

![The tab, before the first chat](../../images/plugin-betareaders-tab.png)

![The menu of the chats](../../images/plugin-betareaders-menu.png)

## Personas

A persona has a **name**, a **description** (what the model plays: how the persona reads, what they like and what annoys them) and an
optional **icon**, an image scaled down to a small size. Without an icon the persona is a circle with the first letters of the name.

The personas of your account are the **persona library** in *Settings → Extension Settings → Beta Readers*, started with six readers: Athena,
Vesper, Ember, Quill, Nox and Sable. **Edit personas** opens the library in a window where you can add, edit, copy, delete and reorder
personas, upload their icons and **Add the starter personas** the library is missing. The changes are applied with **Save**, and **Cancel**
throws them away.

![The persona library](../../images/plugin-betareaders-library.png)

A book can have personas of its own: *Personas of this book* in the menu of the chats opens the same window with the personas that only this
book offers. They are marked *(this book)* when you choose them for a chat.

A chat keeps a **copy** of its personas, so a change in the library does not change a chat that has begun; the copy is changed when you ask
(see [the group](#a-group)).

## Starting a chat

**+** opens the window of a new chat.

![The window of a new chat](../../images/plugin-betareaders-new-chat.png)

| Field | |
|---|---|
| Kind | **One persona** or **A group**. It can't be changed afterwards. |
| Title | Empty: the names of the personas. |
| Persona(s) | One persona for a chat with one; for a group, at least one (a group can start with one persona and others join later). |

The folded part, **Story, medium, model and format**, holds the [options of a chat](#the-options-of-a-chat). Every one of them can stay
empty, and then it is inherited.

A chat with **one persona** is a conversation between you and that persona. A **group** is a conversation of all the personas present with you
and with each other: one reply can have several of them talking, one after another, and they may disagree, tease each other and build on each
other's points. A message in the reply can also be a narration (*Narrator*): a short description of what the persona does.

## Talking

Write into the box at the bottom and press **Send**; the answer appears as it is written, and **Stop** (the button is *Stop* while the model
answers) cuts it short. One reply is one turn of the model: a short **Reasoning** (closed; models that think on their own show their own
thinking instead and no extra reasoning is asked) and the messages of the personas, each with what they do and what they say.

The personas of the group are told who is present at the end of your last message only (nothing about it is saved), so adding or removing a
persona never changes the earlier history.

### Sending the story

**Include the latest parts** sends the story with your message. The first time it brings everything the chat may read (see
[below](#how-much-of-the-story-the-personas-read)): the summaries of the story and the parts after them, as far as they fit the context of the
model. Afterwards it sends only what is **new or changed** since the last time, marked in the request as new, so the personas can notice the
change. The label of the box says what would be sent (*Include the story (5 parts)*, *Include the latest parts (2 new)*, *none new*, *the story
changed*), and the button next to it checks again after you wrote something. A message of yours says which parts it sent (*Parts 1-5 sent*).

Under the box are what the personas have read (*The personas have read: parts 1-5*) and the size of the context (*Context: 2,266 of 30,720
tokens*). When the history does not fit, the oldest messages are left out of the request, not deleted, and a notice says how many.

### Changing the conversation

| Button | |
|---|---|
| pen (your message) | Edits your message; what it sent stays sent. |
| trash | Deletes your message, or one message of a reply (the rest of the turn stays, and the model sees the turn without it). |
| Regenerate | Drops the last reply and asks again. |

When a reply failed or there is none, the panel says so and offers **Ask again**.

![The actions of a message](../../images/plugin-betareaders-actions.png)

### A group

*Personas (n)* above the conversation lists who is present. In a group you can:

- **Bring in a persona** from the library or the book, with an optional note that tells the group why they came ("Mara has read the whole
  book"). The history gets a line (*Mara joined the conversation.*), so the model knows when.
- **Send away** a persona (the arrow button of their row), again with a note. They stay in the list, greyed and marked *(left)*. The last
  persona can't leave.
- **Update from the library**: takes the current description and icon of the persona from the library (or the book) into the chat.

![A persona that joined the group](../../images/plugin-betareaders-joined.png)

![Bringing a persona in](../../images/plugin-betareaders-bring-in.png)

## The options of a chat

The window of a new chat and **Chat settings** have the same options; every one can be left empty and then the chat inherits it.

| Option | |
|---|---|
| Medium | What the text stands for: a book, a movie, a TV series, a manga or comic, an audio drama, a stage play, a game or visual novel. The personas react as the audience of that medium would. Changing it in a running chat leaves a line in the history (*From now on the text is read as: ...*). |
| Model | The AI that answers. |
| Protocol | The [protocol](../protocols.md) used with it. |
| Reply format | **Automatic** (JSON when the model can follow a schema, free text otherwise), **JSON** or **Free text**. |
| Personas read up to part | The [spoiler limit](#how-much-of-the-story-the-personas-read). |
| Start from part | The part where the story the personas get begins. |
| Use the summaries of the story | See below. |

![The settings of a chat](../../images/plugin-betareaders-chat-settings.png)

### How much of the story the personas read

The parts are numbered from 1 as in the *Outline* tab of the story.

- **Personas read up to part** is the spoiler limit: the personas never get a part after it, and the summaries are collected only from the
  story up to it. Raise it when the story moves on, and the next message offers the parts that were held back as new ones.
- **Start from part** leaves the earlier parts out, not even as summaries. Use it to let the personas read only one chapter.
- **Use the summaries of the story** (on by default) lets the first message start with the summaries and then the parts after the newest of them.
  Summaries apply only when the story starts at its beginning.

A first part after the last one is refused. Lowering the limit under what was already sent does not unsend it.

![A chat that reads parts 2 to 3 only](../../images/plugin-betareaders-spoiler.png)

The lorebook is not sent to the personas: they only know what the story tells them.

## Settings

*Settings → Extension Settings → Beta Readers* is the place of your defaults:

![The settings of the plugin](../../images/plugin-betareaders-settings.png)

| Setting | |
|---|---|
| Medium | The medium of the chats. |
| Model of the chats | The AI. Empty: the model of the book. |
| Protocol of the chats | Empty: the protocol of the book. |
| Prompt of a chat with one persona | The system prompt of a one-to-one chat. Empty: the built-in prompt. |
| Prompt of a group chat | The system prompt of a group chat. Empty: the built-in prompt. |
| Persona library | **Edit personas**, see [above](#personas). |
| Mediums | **Edit mediums**: each medium has a name and the prompt that tells the personas how to react to it. **Restore the built-in mediums** adds the ones that are missing. |

`{{medium}}` in a prompt is replaced by the text of the medium. The settings are saved with the **Save** button of the *Settings* page.

The medium, the model, the protocol and the two prompts are also [inherited settings](../books/prompts.md#inherited-settings): the *Prompts*
tab of a book and the settings of a [folder](../books/folders.md) have them in the **Extension Settings** section. A book uses its own
value, otherwise the nearest folder's, otherwise the one from *Settings*, otherwise the built-in one; the model and the protocol of the book
are the last resort. A chat overrides the medium, the model and the protocol for itself in its [options](#the-options-of-a-chat).

## On a phone

Works. The **Beta Readers** tab is in the outline drawer of the story, and the chats are the same as on the desktop; the room for the
conversation is small, so landscape is easier. See [Using Marginalia on a phone](../phone.md#extensions).

## Storage

The personas and the chats of a book are saved in the attributes of the book (so with its backups), the personas of your account and the
settings in your user settings. The model is asked with the usual limits of the AI, and the tokens count as used by the book.
