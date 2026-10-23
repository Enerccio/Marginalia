---
label: Annotations
order: 95
---

# Annotations

An **annotation** is a note for you about a passage of a part: *weak scene*, *timeline is wrong*, *revisit*. It works like
a comment in a word processor: the text of the story stays clean, the passage is only marked, and the note lives next to
it. Notes can also come from a model or from an [extension](../extensions/index.md) that checks the story.

An annotation is **for the author**. It is not sent to the model unless you say so for that note, see
[Giving a note to the model](#giving-a-note-to-the-model).

## What an annotation is

| | |
|---|---|
| **Passage** | The words the note is about, or the **whole part**. |
| **Note** | What you want to remember. |
| **Type** | An optional word you choose (*continuity*, *style*, *pacing*...). |
| **Source** | Who wrote it: you, a model (*AI*), an *agent* or an *extension*. The mark in the text has a different colour for each. |
| **Status** | *Open*, *Resolved*, *Dismissed* or *Orphaned*. Only open notes are marked in the text. |

A part can have any number of annotations, also on overlapping passages.

## Making an annotation

**On a passage.** Select the words in the text of the part (they can have formatting, the selection does not have to be
exact), then choose **Annotate selection** in the menu (☰) of the part. The window shows the passage; write the note, an
optional type, and whether the model gets the note, and **Save**. Selecting and then opening the menu (☰) of the part can lose the selection in some browsers; the
**right click** (a long press on a phone) on the selected text opens a menu with **Annotate selection** and **Copy** that keeps it. The menu replaces
the one of the browser on the text of a part, so it has **Copy** too. While the part is being edited, the selection in the
editor is used: the edit is saved first.

**On the whole part.** Choose **Annotate part** in the menu of the part.

**With a model.** Choose **Annotate with AI**, see [below](#letting-a-model-annotate).

**From the command line** (and for extensions): [`/create-annotation`](../commands/index.md#annotations).

## Reading the annotations

A passage with an open note is underlined with a **wavy line**, coloured by who wrote the note (blue you, orange a
model, green an agent, red an extension). A note inside another one is underlined lower, so both can be seen.

- **Click the underlined text** to see the notes that cover that point (all of them where passages overlap). They open
  next to the text; on a phone tap the passage.
- A **speech bubble with a number** in the header of a part counts the notes of the part that need a look. Click it to
  see them all, also the ones about the whole part and the ones that lost their passage.
- The **Annotations** tab lists the notes of the whole book, grouped by part in the order of the story, with a filter
  (*Need a look*, *Closed*, *All*). **Show in story** (or the number of the part) takes you to the part.

Each note shows who wrote it, its type, its status, the passage, the note and these actions:

| | |
|---|---|
| ✓ **Resolve** | The problem is dealt with. The note leaves the text and stays in the list. |
| ✗ **Dismiss** | The note is not worth dealing with. Same. |
| ↶ **Reopen** | Brings a closed note back. |
| ✎ **Edit** | Changes the note and the type. |
| 🗑 **Delete** | Deletes the note; it can be restored from the [trash](../trash.md). |
| **Give to the model with the part** | See below. |
| **Attach to selection** | For a note without a passage: select text in the part, then click it. |

## Annotations follow the text

You do not have to care where a note is when you edit. Marginalia keeps the passage of each note with the words it
was about:

- text added or removed **before** the passage moves it;
- an edit **inside** the passage makes it longer or shorter;
- words you type **right at the start or end** of the passage are outside of it;
- fixing a typo in a one word passage keeps the note on that word.

When the passage is **deleted** or rewritten so that it can't be found, the note becomes **Orphaned**. It is kept: attach
it to another passage (**Attach to selection**), dismiss it, or delete it. If you bring the words back (undo), the note
finds its passage again.

When a part is **generated again** the text is new, so the notes find their passages again by the words they quoted;
the ones whose words are gone are orphaned. When two parts are [merged](story-editor.md#merging-a-part-into-the-previous-one), the notes of the later one move to
the merged part. A **branch** of a part copies its notes. Notes are part of [backups](backups.md), copies of a book and
exports of a backup.

## Letting a model annotate

**Annotate with AI** in the menu of a part asks a model to review it. You say what to look for (*flag continuity errors*,
*weak dialogue*, *contradicts the lore*; empty uses the task of the prompt), choose the model, and **Annotate**.

The model reads the part like the [redraft](story-editor.md#redrafting-a-part) does: the part, the story before it (older
story as [summaries](summaries.md)), as much of the story after it as fits, the lore and the style of the book, and the
notes the part already has so it does not repeat them. It never gives positions: it **quotes** the passage, and Marginalia
finds the quote in the text. A quote that is not found is not lost: the note is proposed as a note about the whole part,
marked *not found in the text*.

**Nothing is added to the book until you accept.** The notes the model proposes are listed with a check box each (all
checked); uncheck the ones you do not want and choose **Add the selected annotations**.

| Option | |
|---|---|
| **Model** | The model that reviews. Empty is the **Default model for annotating** from your [settings](../account.md#general-settings), else the model of the book. Reviewing is analysis, so this is often not the model that writes the prose. |
| **Answer format** | *Automatic* (structured, when the model can answer in JSON), *Structured (JSON)*, *Text blocks* (for models that do not keep to a format) or *Free critique, then extraction* (the model writes a free review, a second model, usually a cheaper one, turns it into notes: choose it under **Model that extracts the notes**). |

If an answer can't be read, Marginalia asks once more; if it still can't be read, the model's answer is shown so the work
is not lost. The prompt is the [annotate prompt](prompts.md#annotate-prompt).

## Giving a note to the model

Check **Give to the model with the part** on a note and it becomes a note to the model too: the notes of the parts that are
in the prompt go into the master template as `{%{{{annotations}}}%}` (the built-in template has a block *Author's notes on the
story*; a template of your own has to use the variable). Only **open** notes with this check are given, and only for the
parts that fit into the prompt. Use it for what the model should keep in mind while it continues (*this character is hiding
the letter*), not for editing remarks.

## Redrafting to address the notes

When a part has open notes, the [redraft](story-editor.md#redrafting-a-part) window has a check box **Deal with the open
annotations of the part**. Checked, the model gets the notes (the passage and the note) together with your instructions and
rewrites the part to deal with them; when you **Apply** the redraft, those notes are marked resolved.

## Commands

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`,
`/resolve-annotation`, `/reattach-annotation` and `/delete-annotation` work with the notes from the command line.
Extensions can use the same commands. An [agent](../agents.md) can read annotations but **cannot make or change them yet**
(its changes go to a working copy of the book, which does not take annotations back).
