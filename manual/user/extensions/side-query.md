---
label: Side Query
order: 70
---

# Side Query

A chat with the model next to your story: ask about the plot, check continuity, brainstorm names or the next chapter -
without adding anything to the book.

![Side Query next to the story](../../images/plugin-sidequery.png)

## The Side Query tab

The left sidebar of the [story editor](../books/story-editor.md) gets a **Side Query** tab next to *Outline*. Drag the
divider to give it more room.

The tab holds conversations, each in its own tab: **+** opens a new one (*Tab 2*, *Tab 3*...), the pen renames a tab,
**×** closes it (the last tab can't be closed). Conversations belong to the book and are kept until you close their tab.

## Asking

Type a question into the box at the bottom and click **SEND**. The answer appears as it is written; **STOP** (in place
of *SEND*) interrupts it.

| Button | |
|---|---|
| **SEND** | Sends the question. |
| **UNDO** | Removes the last message. If it was your question, it goes back into the box to be edited. |
| **REGENERATE** | Removes the last answer and asks again. |

Every message has buttons to **move** it up or down, **edit** it, **copy** it to the clipboard, and **exclude** it from
the context (or include it again). Excluded messages stay in the conversation but are not sent to the model - useful
to drop a wrong answer without deleting it.

## What the model sees

The options above the conversation choose what is sent with your questions:

| Option | |
|---|---|
| **Lorebook** | The entries of the book's lorebook. |
| **Chat Logs** *from* … *to* | The story parts of the active branch in that range. |
| **Tokens** | The approximate size of the request. |

!!!warning Chat log numbers start at 0
The *from* and *to* numbers count parts from **0**: `0` to `5` are the parts `#1` to `#6` of the outline.
!!!

!!!
*Lorebook* sends all enabled entries of the book's lorebook as they are written - not only those that would be
activated, but without the entries of sub lorebooks, and without processing [macros](../templates/macros.md) in them.
!!!

The request is built like this:

1. one system message: the profile's *Initial System Query*, the lorebook entries (*Lorebook Context*), the story parts
   (*Chat History Context*, labelled *Message #1*, *#2*...), and the profile's *Instructions Before User Input*;
2. the messages of the conversation that are included, as a chat.

The model and protocol come from the profile (see below), by default those of the book. A cheap, fast model is usually
enough for questions about the story.

## Saved queries

Questions you ask often can be saved:

- **Save prompt** (disk icon) saves the text in the box under the name selected in the list, or asks for a name;
- **Save prompt as...** saves it under a new name;
- pick a name in the list to put the saved text into the box;
- the trash button deletes the selected saved query.

Saved queries belong to your account and are available in every book.

Some useful ones:

- *List every named character that appeared so far, with one line about each.*
- *Which threads of the plot are still open?*
- *Suggest five names for a harbor town in this world.*

## Profiles

Side Query settings are in **Settings → Extension Settings → SideQuery Settings**, organized in **profiles** like the
[Reviewer](reviewer.md#profiles) ones. The selected profile is the one used:

| Field | |
|---|---|
| **Profile**, **New**, **Rename**, **Delete** | Choose and manage profiles. *_Default* can't be renamed or deleted. |
| **SideQuery Model**, **SideQuery Protocol** | The [inference provider](../inference-providers.md) and [protocol](../protocols.md) to use. Empty: the book's. |
| **Initial System Query** | The start of the system message, e.g. the role of the assistant. |
| **Instructions Before User Input** | Text added at the end of the system message, after the context - rules for the answers, e.g. *Answer briefly. Quote the story when you can.* |
| **Enable AI Tab Naming** | Not implemented yet - tabs are always named *Tab N*. |

Click **Save** at the top of the *Settings* page to keep the changes.

## Storage

Conversations are stored with the book, so they are part of [book backups](../books/backups.md) and copies. Profiles
and saved queries are stored in your user settings.
