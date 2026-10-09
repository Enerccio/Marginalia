# Author's Note

A note to the model that is sent with every generation of the book: the tone you want right now, what the current
chapter is about, a reminder the model keeps forgetting. Unlike the instructions of a turn, the note stays until you
change it.

## The Author's Note tab

The left sidebar of the [story editor](../books/story-editor.md) gets an **Author's Note** tab next to *Outline*.
Changes are saved to the book as you type and used by the next generation.

| Field | |
|---|---|
| **Insert into prompt** | Turns the note on or off without deleting it. |
| **Insertion depth** | Where the note goes, counted in messages from the end of the prompt: `0` - after the instructions of the turn (the very last message), `1` - right before them, `2` - before the last story part, and so on. A large depth puts the note right after the system prompt. |
| **Role** | Whether the note is sent as a *System*, *User* or *Assistant* message. |
| **Author's Note** | The text sent to the model. An empty note is not sent. |
| **Author's Note (private)** | Notes for yourself - plans, reminders. Never sent to the model. |

!!!
The closer the note is to the end of the prompt, the stronger its effect. Depth `1` with role *System* is a good start.
The note is not processed as a [template](../templates/macros.md): macros are sent as they are written.
!!!

## How the prompt looks

A generation sends the system prompt first, then the story as a chat - each part as an answer of the model to
*Generate story* - and the instructions of the turn last. With depth `1` the note is inserted right before the
instructions:

1. system prompt (template, lore, summaries)
2. story parts…
3. **the Author's Note**
4. the instructions of the turn

The note is not counted when Marginalia decides how much of the story fits into the context: keep it short.

## Storage

The note is stored with the book, so it is part of [book backups](../books/backups.md) and copies.
