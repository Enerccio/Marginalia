# Side Query

A chat with the model next to your story - ask about the plot, check continuity, brainstorm names or the next
chapter, without adding anything to the book.

![Side query panel](../../../manual/images/plugin-sidequery.png)

## Usage

The story view gets a *Side Query* tab in its left sidebar (next to the outline), with chat tabs. In each tab:

- type a question and **SEND**,
- **REGENERATE** drops the last answer and asks again,
- **UNDO** removes the last message - if it was your question, it goes back into the input box to edit,
- messages can be edited and copied,
- choose what the model gets to see:
  - *Lorebook* - the book's lore,
  - *Chat Logs* - story parts, from part N to part M (counted from 0),
- the token counter shows how big the request is,
- frequently used questions can be saved and picked from a list.

Open several tabs for separate conversations; tabs can be renamed.

## Settings

Side query profiles are configured on the *Settings* page:

- *Initial System Query* - the system prompt,
- *Instructions Before User Input* - text added at the end of the system prompt, after the context,
- the model and protocol to use - by default the book's own,
- *Enable AI Tab Naming* - not implemented yet.

## Storage

Conversations are stored with the book, so they are included in book backups and copies. Profiles and saved
queries are stored in your user settings (included in database backups).
