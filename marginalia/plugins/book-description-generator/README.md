# Book Description Generator

Lets the model write the name and the description of a book from its story. The button is on the **About** tab of the book
window; the author picks a range of the messages, the model reads it and the result can be copied to the book.

## Usage

The About tab of the book window (desktop and phone) gets a **Generate name and description** button at its end. It opens a
dialog:

- *Model* and *Protocols* - what the generation runs with. It starts with the model and protocol of the book, else the default
  ones of the user. The choice is for this generation only.
- *From message* / *To message* - the range of the story, in the numbers of the messages with text of the active branch of the book
  (the beginning of the message is shown under the field). It starts with the whole story.
- *Use summaries* - a [summary](../../../manual/user/books/summaries.md) of the story stands in for the messages it covers when all
  of them are in the range, which makes a long range cheaper. A summary that reaches outside of the range is not used (its
  messages are), and neither is one that does not describe the story any more. Remembered for the next time.
- *Answer format* - how the model is asked to write: *Automatic* (JSON in a schema when the model's service can take one, Markdown
  when it can not), *JSON*, *JSON without a schema* (the prompt describes the object; for a server that can't take a schema together
  with the thinking of the model) or *Markdown* (the name as the first heading, the description below it). Whatever the answer
  turns out to be, JSON and Markdown are both read. Remembered for the next time.
- A counter shows how many tokens the range is with the prompt, how many the model takes, and whether the story is sent as it is or
  has to be made into notes first.

**Generate** starts the model (**Stop** ends it). The name and the description appear on the right, each with a **Copy to book**
button that puts it into the field of the About tab and saves the book. If the answer can't be read, the text the model wrote is
shown instead.

### A story that is too long

When the range with the prompt is more than the model takes (its context minus the room for the reply), the story is divided into
parts that fit, in order. The **notes prompt** runs on each part and writes notes on it (they are shown under *Notes* while they
come); then the **description prompt** gets the notes instead of the story. Notes that are still too long for the model are
divided and noted again, up to four rounds. A message that is larger than a part is cut at a paragraph, a line or a sentence. When the
context size of the model is not known the story is always sent as it is.

## Prompts

Both prompts are *inherited settings*: a book uses its own, else the nearest folder above it that sets one, else the user's, else
the built-in text.

- *Description prompt* - writes the name and the description. Variables: `{{story}}`, `{{fromNotes}}` (the story is the notes),
  `{{bookName}}`, `{{bookDescription}}`.
- *Notes prompt* - writes the notes of one part of a long story. Variables: `{{story}}`, `{{part}}`, `{{parts}}`, `{{bookName}}`,
  `{{bookDescription}}`.

They are set in the *Prompts* tab of the book (section *Extension Settings*), in the dialog of a folder, and at the user level in
*Settings → Extension Settings → Book Description Generator*. The instructions for the format of the answer are added after the
description prompt by the plugin, so the prompt only says what to write.

## Storage

The prompts of the user, and the *Use summaries* and *Answer format* choices, are in the user settings (included in database
backups). The prompts of a book or a folder are stored with it. Nothing is stored about the generated name and description until
they are copied; then they are the name and the description of the book.
