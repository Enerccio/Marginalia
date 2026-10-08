---
label: Reviewer
order: 80
---

# Reviewer

Lets the model review what you've written: reader reactions, an editor's critique, a continuity check - whatever you
prompt it for. Reviews are kept with the part, next to the story, and never become part of the book.

![A review of a story part](../../images/plugin-reviewer.png)

## Reviewing a part

The menu (☰) of every part in the [story editor](../books/story-editor.md) gets a **Review** submenu:

| Item | |
|---|---|
| **View / Generate Review** | Opens the review dialog. When the part has no review yet, one is generated right away. |
| **Advanced Options** | Opens the [advanced options](#advanced-options), then generates a review with them. |
| **Delete Review** | Removes all reviews of the part. |

In the review dialog:

- **‹** / **›** page through the reviews of the part; **›** on the last review generates a new one;
- **Stop generating** interrupts the review being written;
- click the text of a review to edit it; it is saved when you click outside the field;
- *Thinking Process* shows the reasoning of [reasoning models](../inference-providers.md#reasoning-models);
- **Close** closes the dialog.

!!!warning Reopen the book after editing a part
In the current version, the review dialog can save an older copy of the part: when the part was edited, or got a
[summary](../books/summaries.md), after the book was opened, opening or generating a review brings back the old text
and drops the summary. After editing or summarizing a part, close and reopen the book before reviewing it.
!!!

## What the model gets

By default the review uses the **same prompt the part was written from** (*Use standard prompt info*): the system
prompt with the lore and summaries, the earlier parts, then the part itself, and finally the review prompt. The model
reviews the part with the full context of the story.

The **pre-prompt** of the profile, if set, comes first as a system message.

## Advanced options

| Option | |
|---|---|
| **Prompt Override** | A different review prompt for this review. Starts with the prompt of the profile. |
| **Use standard prompt info** | On: the prompt the part was written from, as above. Off: only the story parts, with the options below. |
| **Include Lorebook** | (off only) Adds the lore that was active when the part was written. |
| **Token Limit** | (off only) How many tokens of story to send: the parts up to and including the reviewed one, newest first, as many as fit. Starts with the room for the prompt of the model. |

With *Use standard prompt info* off, the dialog shows the size of the request (*Total tokens*). **Generate** opens the
review dialog with these options; further reviews generated in that dialog use them too.

## Profiles

Reviewer settings are in **Settings → Extension Settings → Reviewer Settings**. They are organized in **profiles**:

| Field | |
|---|---|
| **Profile** | The profile to edit - and the one used for all reviews. |
| **New**, **Rename**, **Delete** | Manage profiles. The *_Default* profile can't be renamed or deleted. |
| **Reviewer Model** | The [inference provider](../inference-providers.md) that writes reviews. Empty: the book's model. |
| **Reviewer Protocol** | The [protocol](../protocols.md). Empty: the book's protocol. |
| **Review Pre-Prompt (System)** | A system message sent first, e.g. *You are a demanding fiction editor.* |
| **Review Post-Prompt (User)** | The request for the review, sent last. |

Click **Save** at the top of the *Settings* page to keep the changes.

The default post-prompt simulates a discussion on a story forum: a made-up thread with thirty replies from different
readers, positive and negative. Other ideas:

- *List continuity errors in the last part compared to the earlier story. Quote the passages.*
- *As a line editor, point out clichés, repetitions and weak verbs in the last part, with suggestions.*
- *Describe how a first-time reader would feel at the end of this part, and what they expect next.*

Create a profile for each kind of review and select the one you want before reviewing. A cheaper model for reviews
saves cost: set it as the profile's *Reviewer Model*.

## Storage

Reviews are stored with the story part, so they are part of [book backups](../books/backups.md) and copies.
[Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part) doesn't copy them. Profiles are
stored in your user settings.
