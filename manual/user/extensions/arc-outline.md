---
label: Arc Outline
order: 106
---

# Arc Outline

The plan of your book in the sidebar: arcs, each split into chapters, with the state of every one of them, and a model
that can write arcs and chapters for you. The outline is not part of the story prompt; the generators and the
[Side Query](side-query.md) can use it when you ask them to.

## The Arcs tab

The left sidebar of the [story editor](../books/story-editor.md) gets an **Arcs** tab next to *Outline*. **Add new
arc** opens the dialog of an arc:

| Field | |
|---|---|
| Title | Required. |
| Description | What happens in it. |
| Note (private) | Notes for yourself. |
| State | **Planned**, **Current** or **Finished**. |

Chapters have the same fields. The list shows every arc as a card with its chapters indented below it. The color of a
card follows its state: grey for planned, blue for the current one, green with a dimmed title for finished ones. The
buttons of a card:

| Button | |
|---|---|
| calendar / play / check | The state; opens a menu to change it. |
| chevron (arcs) | Hides or shows the chapters of the arc. |
| arrows | Move the arc or chapter up or down. |
| plus (arcs) | Adds a chapter at the end of the arc. |
| wand (arcs) | Asks the model to write chapters for the arc, see [below](#generating-chapters). |
| pen | Opens it in the dialog. |
| trash | Deletes it (an arc with all its chapters), after a confirmation. |
| robot (generated arcs and chapters) | Shows what the model wrote, see [below](#generating-an-arc). |

![The Arcs tab: a finished, a current and a planned arc](../../images/plugin-arcoutline-tab.png)

### Moving chapters between arcs

A chapter moved up from the first place of its arc goes to the end of the previous arc; one moved down from the last
place goes to the start of the next arc. Keep pressing the arrow to take a chapter as far as you need.

### Finishing an arc

Marking an arc **Finished** marks all its chapters **Finished** too. Changing an arc to another state leaves its
chapters as they are.

The outline is saved with the book as soon as you change it, so it is included in its backups.

## The outline as a source for other extensions

The outline is offered to other extensions as a source of data about the book: in [Side Query](side-query.md) check
**Arc outline** in the plug popover to send the arcs, their states, descriptions and chapters with your questions. There
are two versions: **Arc outline** is the terse one (descriptions only), **Arc outline (with notes)** adds the note of every
arc and chapter under its description, which is where the details of generated arcs and chapters are. Your private notes
are sent only when you check the second one.

## Generating an arc

The wand button next to **Add new arc** asks the model to write an arc for you. The dialog has two halves.

On the left:

| | |
|---|---|
| Prompt | The instructions for the model, with three variables: `{{backgroundLore}}` (the entries of the book's lorebook and its sub lorebooks), `{{existingArcs}}` (your arcs and chapters, with their states and descriptions, but not your private notes) and `{{specification}}`. It starts as the prompt from *Settings → Extension Settings → Arc Outline* (the default prompt when you have not changed it); what you edit here applies to this generation only. |
| Specification | What you want from the arc: the theme, the characters, where it should lead. Can be empty. |
| Name tools | Only when the [Name generator](name-generator.md) is loaded: the categories of names the model may use instead of inventing them, the same as in the [Lore Generator](lore-generator.md). |
| Model, Protocols | Start as the model and protocol of the book (your default ones from *Settings* when the book has none). Change them to use another model for this generation only; the choice is not saved. |
| Generate | Starts the model. While it runs the button is a red **Stop** and everything else in the dialog is disabled. |

On the right you see the **reasoning** of the model as it is written and, as soon as the answer begins, the **arc** it
proposes - a name, a terse description and a longer note - shown like an arc of the outline (the note under its
description) and growing as the answer streams in. The description is what the outline shows, so the model is asked to
keep it to a couple of sentences and to put the details into the note, which ends up in the **Note** of the saved arc.
When the
model is done, its answer is checked; if it is not a valid arc (for example the note is missing), the dialog says
so and the arc can't be saved.

![Generating an arc](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** adds the arc at the end of the outline. To try again, press **Generate** once more, with the same or a
changed prompt or specification; the previous answer is replaced.

## Generating chapters

The wand button on an arc asks the model to divide that arc into chapters. The dialog is like the one for arcs, with
these choices on the left:

| | |
|---|---|
| What the model knows of the story | **Whole story so far**: the arcs up to this one, with all their chapters. **Arc summaries only**: the same arcs without chapters. **Arc summaries and finished chapters**: the same arcs with only the chapters you marked *Finished*. Arcs after this one are never sent. |
| What you want from the chapters in general | Pace, tone, what must happen. Can be empty. |
| Number of chapters | Write it in words ("about eight") or choose an exact number. A number adds a text area for each chapter where you can say what it should contain; all of them are optional. Using one of the two clears the other. With an exact number, an answer with a different number of chapters is rejected. |
| Prompt | The instructions for the model, with the variables `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` and `{{chapterSpecification}}`. It starts as the **Chapter generation prompt** of *Settings → Extension Settings → Arc Outline*. |

On the right you see the reasoning, then the arc with the chapters it already has, and the new chapters as the model
writes them. **Save as chapters** adds them at the end of the arc, each with a terse description and the details in its note (like arcs). Every new chapter keeps the reasoning and the answer
of the model, and has its own robot button.

![Generating the chapters of an arc](../../images/plugin-arcoutline-generate-chapters.png)

An arc or chapter written by the model keeps its **reasoning** and the **answer** it came from. The robot button on it
opens them in a small popover, out of the way.

![What the model wrote for an arc](../../images/plugin-arcoutline-generated.png)
