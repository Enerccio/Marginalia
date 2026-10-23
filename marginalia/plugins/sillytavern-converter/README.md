# SillyTavern Converter

Rewrite a SillyTavern roleplay chat as a story, turn by turn. Load the chat into the book, read it in a table, let the
model split it into *turn details* (scene setting, POV character, present characters, instructions) from the chat and the
lore of the book, and put each of them into the new turn form of the story with a click.

## Usage

The sidebar of the story editor (desktop layout) gets an **ST Converter** tab. The text is small and the tab scrolls when the
sidebar is low. At the top of the tab a **Convert to turn details** section, closed until you open it (the chat and the
turn details get the room), holds:

- the **model and protocol** the wand runs with (the model of the book by default; the choice is kept with the book),
- the **range** of the chat the wand works on, **From** and **To**, with the number of tokens the request takes (the
  messages of the range, and the template and lore, which are counted once),
- the **wand** (*Convert range*) and a pen button that edits its prompt.

Under that, two tabs:

- **Chat** - upload a `.jsonl` chat exported by SillyTavern (a chat without branches; of the swipes of a message the
  shown one is read, hidden system messages are left out). It is shown as a table: a checkbox, the number and the message with
  the name of the speaker in bold above it. Tick the messages you have gone through while you rewrite them; the double arrow above the table goes
  to the next unchecked message (the turn details have one too). **Click a message** to start a range,
  **Shift+click** another to extend it up to there; the range is highlighted. The button with the cross removes the chat
  (and the turn details) from the book.
- **Turn details** - what the wand prepared, one row per turn with the messages it covers and one element with the scene
  setting, POV, present characters and instructions, each under a bold line with its name, also with a checkbox for the turns you have used. **Click a turn** to open the new turn form
  of the story and copy all four fields into it, then generate as usual.

The wand sends the chat of the range together with the lore of the book (all non-deleted entries of the lorebook and its
sub lorebooks, rendered like the story would) and asks for a list of turns that cover the range. The turns replace the
turns of the earlier runs that touch the range, except those you ticked as reviewed.

## Storage

Everything is stored with the book (its attributes), so it is in the book backups and copies: the chat, the ticks, the
turn details, the model and the prompt. The chat is kept as read (who said what), not as the file.
