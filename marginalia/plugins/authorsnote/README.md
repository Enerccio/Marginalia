# Author's Note

A note to the model, sent with every generation of the book - the tone you want now, what the current chapter is
about, a reminder the model keeps forgetting.

## Usage

The story view gets an *Author's Note* tab in its left sidebar (next to the outline):

- *Insert into prompt* - turns the note on or off,
- *Insertion depth* - number of prompt messages after the note: `0` puts it after the instructions of the turn (the
  very end of the prompt), `1` right before them, and so on; it always stays after the system prompt,
- *Role* - the note is sent as a system, user or assistant message,
- *Author's Note* - the text sent to the model,
- *Author's Note (private)* - notes for yourself, never sent to the model.

Changes are saved as you type and used by the next generation.

## How it works

The plugin listens to the `AFTER_PREPARE_PAYLOAD` generation event, reads the note from the book and inserts it into
the payload sent to the model. It is the example for generation events in the developer manual
(*Plugin development → Generation events*).

## Storage

The note is stored with the book (`com.github.enerccio.marginalia.extensions.authorsnote` in its attributes), so it
is included in book backups and copies.
