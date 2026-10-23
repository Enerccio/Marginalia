---
label: SillyTavern Converter
order: 70
---

# SillyTavern Converter

Rewrite a [SillyTavern](https://github.com/SillyTavern/SillyTavern) roleplay chat as a story, turn by turn: load the
chat into a book, let the model split it into *turn details* using the chat and the lore of the book, and put each turn
into the new turn form of the story with one click.

To make a book out of a chat directly (every message, or every user and assistant pair, a part), see [Importing from
SillyTavern](../books/managing-books.md#importing-from-sillytavern). The converter is for the other way: you keep the
chat next to you and write the story again, with the model, from instructions made of the chat.

!!!
SillyTavern Converter is not available in the [phone layout](../phone.md#extensions).
!!!

## The ST Converter tab

The left sidebar of the [story editor](../books/story-editor.md) gets an **ST Converter** tab (small text, and the tab scrolls
when the sidebar is low). Above its two lists is the section **Convert to turn details**, closed until you open it so that
the lists get the room. It holds:

| | |
|---|---|
| **Model** and **Protocol** | What the wand runs with. They start with the model and protocol of the book; the choice is kept with the book. |
| **From**, **To** | The range of messages the wand works on. Type the numbers, click in the chat, or use **All**. |
| Token count | How many tokens the request takes: the messages of the range plus the prompt and the lore (counted once). Use it to keep the range within the context of the model. |
| **Convert range** | The wand: asks the model for the turn details of the range. It becomes **Stop** while the model works. |
| Pen button | Edits the prompt of the wand (variables `{{backgroundLore}}`, `{{chat}}`, `{{firstMessage}}`, `{{lastMessage}}`), with a **Default** button. |

## The chat

Upload the `.jsonl` file of a chat (in SillyTavern it is in `data/<user>/chats/<character>/`, or export it from the
chat). The chat must have no branches: of every message the swipe that was shown is read, and hidden system messages and
empty ones are left out. If your chat has branches, import the one you want to rewrite first (see
[Importing from SillyTavern](../books/managing-books.md#importing-from-sillytavern) for joining branches into a tree).

The **Chat** tab is a table of the messages: a checkbox, the number and the message, with the name of who said it in bold above the
text (the messages of the user are marked with `»`).

- Tick the checkbox of a message you have gone through, so you can see what is left. The button with the double arrow
  above the table scrolls to the next unchecked message (after the one you ticked or clicked last, starting again from the
  top at the end).
- **Click** a message to make it the range, **Shift+click** another to extend the range up to it. The range is
  highlighted and the token count follows.
- The button with the cross above the table removes the chat and the turn details from the book (after asking).

## Turn details

Use the wand and the **Turn details** tab opens with what the model prepared, in the order of the chat. A row is a turn:
the messages it is made from and, in one element, the scene setting, the POV character, the characters who are present and
the instructions, each under a bold line with its name.
The model is told to cover every message of the range exactly once; numbers outside of the range are cut to it.

- **Click a row** to open the *new turn* form of the story and copy all four fields of the turn into it. Change what you
  want and generate. (A row's checkbox and delete button do not open the form.)
- Tick the checkbox of a turn you have used; the double arrow goes to the next unchecked turn.
- Running the wand again for a range **replaces** the earlier turns that touch the range, except the ticked ones.

## Storage

The chat, the ticks, the turn details, the model and the prompt are stored with the book (in its attributes), so they are
in [book backups](../books/backups.md) and copies.
