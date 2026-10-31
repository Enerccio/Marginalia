# Beta Readers

Personas you define and a chat about the book with them.

**Status: under construction.** What exists so far is the configuration; the chat itself is not there yet (the plan is in
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
