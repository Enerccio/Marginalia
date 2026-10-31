# Beta Readers

Personas you define and a chat about the book with them.

**Status: under construction.** The configuration and the conversation work (the tab, the chats, the replies as they stream, the story sent
as new parts); editing and deleting messages, regenerating a reply and bringing personas into a group are still to come (the plan is in
[TODO.md](TODO.md)).

## Settings

*Settings → Extension Settings → Beta Readers* has:

- the **prompt of a chat with one persona** and the **prompt of a group chat** (empty: the built-in prompt; `{{medium}}` is the text
  of the medium),
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

A reply is a turn: a short reasoning (closed, above the messages) and the messages of the personas and the narrator, shown when they are
finished. The personas present are described at the end of your last message and are not stored, so adding or removing a persona does not
change the history. **Include the latest parts** sends the story: everything the chat has not read yet the first time, then the parts that
are new or changed. A message says which parts it sent, and the box under the text shows how many tokens the context uses.
