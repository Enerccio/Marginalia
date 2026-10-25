# Arc Outline

The plan of the book in the story sidebar: arcs, each split into chapters, with the state of every one of them, and a
model that can write arcs and chapters for you.

![The Arcs tab](../../../manual/images/plugin-arcoutline-tab.png)

## Usage

The story view gets an *Arcs* tab in its left sidebar (next to the outline). **Add new arc** opens a dialog with:

- *Title* (required),
- *Description* - what happens in it,
- *Note (private)* - notes for yourself,
- *State* - *Planned*, *Current* or *Finished*.

Every arc is a card with its chapters indented below it, colored by its state: grey for planned, blue for the current
one, green for finished ones (their title is dimmed). The buttons:

| Button | |
|---|---|
| state icon (calendar, play, check) | Opens a menu to change the state. |
| chevron (arc) | Hides or shows the chapters of the arc. |
| chevron (chapter) | Shows or hides the note of the chapter under its description, so it can be read without the dialog. Only chapters that have a note. |
| arrows | Move the arc or chapter up or down. |
| plus (arc) | Adds a chapter at the end of the arc. |
| wand (arc) | Generates chapters for the arc with the model, see below. |
| pen | Edits it in the dialog. |
| trash | Deletes it (an arc with its chapters), after a confirmation. |

![A chapter with its note shown](../../../manual/images/plugin-arcoutline-note.png)

The pen with a feather next to the wand drafts the instructions of the next turn from the current chapter: the model of the story
editor writes them from the data of that chapter (the first one marked *Current*, else the first one not *Finished*, with its arc and the
title and description of the chapter after it), and **Use as Instructions** puts them into the new turn panel. The same data is the
source `current-chapter` ("Arc outline: current chapter") that other extensions, like the Side Query, can ask for.

A chapter moved up from the first place of its arc goes to the end of the previous arc, one moved down from the last
place to the start of the next arc, so chapters can be moved between arcs.

Marking an arc *Finished* marks all its chapters *Finished*. Other states don't change the chapters.

Changes are saved at once.

## The outline as a source

The plugin registers the outline with the `IPCService` as three sources of data about a book,
so other extensions can use it: `outline` ("Arc outline") has the arcs with their states, descriptions and chapters (the
terse version), `outline-with-notes` ("Arc outline (with notes)") has the same with the note of every arc and chapter
under its description (the full version), `current-chapter` ("Arc outline: current chapter") has only the arc and the chapter the story
is at, with the title and description of the chapter after it. Nothing is sent unless the other extension is told to ask for it: the Side
Query can send any of them with a question (plug button → *Arc outline*, *Arc outline (with notes)* or *Arc outline: current chapter*).

## Generating an arc

The wand next to *Add new arc* opens a menu: *Generate an arc* and *Import from story* (see below). ![The menu of the wand](../../../manual/images/plugin-arcoutline-menu.png)

*Generate an arc* opens a dialog that asks the model for a new arc. On the left:

- *Prompt* - a template filled from the settings (*Settings → Extension Settings → Arc Outline*; the default is in
  `Defaults.GENERATION_PROMPT`). Its variables are `{{backgroundLore}}` (the entries of the book's lorebook and its sub
  lorebooks), `{{existingArcs}}` (the arcs and chapters of the outline with their states and descriptions, without
  the private notes) and `{{specification}}` (the text below). Edits here apply to this generation only.
- *Specification* - what you want from the arc.
- *Tools* - the common `ToolSelectionButton`, shown while a suite of tools for the model is installed (names, places): which
  suites and which of their categories the model may use, as in the Lore Generator. The hints of the suites (editable in the
  tool dialog) are added to the end of the prompt. Not in the story import, the names are in the story.
- *Generate*, which becomes a red *Stop* while the model runs. Everything else is disabled meanwhile.

On the right the reasoning of the model streams in as Markdown, and once the answer starts the arc shows as it would
in the outline (without chapters), filled in as the JSON streams. The model is asked for `{name, description, note}` with a
JSON schema: the `description` is terse (it is what the outline shows) and the full version goes to the `note`, which
the preview shows under the description; when it finishes, the answer is validated against the schema and only a valid one can be saved. *Save as
arc* adds it at the end of the outline and closes the dialog; *Generate* again tries once more, with the same or an
edited prompt and specification. The *Model* and *Protocols* lists at the top of the left side start as the model and protocol of the book (the default ones from the user settings when the book has no model) and can be changed for this generation; nothing is saved.

## Importing an arc from the story

*Import from story* in the menu of the wand summarizes the written story as an arc with chapters. It always reads the
**active branch** of the book (as it is when the dialog opens); only messages that have text are counted, from 1. On the
left, besides the model, protocol and prompt:

- *From message* / *To message* - the range to send (the whole branch at first); under each field the beginning of the
  message it points to.
- *Number of chapters (optional)* - empty: the model divides the story where it divides naturally; a number (1 to 40) is
  put into the JSON schema, so an answer with another number of chapters is not valid.
- *State of the imported arc and its chapters* - *Finished* at first, as the story is written already.
- A counter: the messages of the range and their approximate tokens, and the approximate tokens of the whole prompt
  (template, lore, outline and story) next to what the model takes (context minus the room for the reply). It is
  highlighted when the prompt does not fit. It follows the range, the number of chapters, the prompt and the model.
- *Prompt* - `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (the messages, each under a `--- Message N ---`
  heading) and `{{numberOfChapters}}`. It starts as the *Story import prompt* of the settings (default
  `Defaults.IMPORT_PROMPT`). There are no tools, the names are in the story.

![Importing an arc from the story](../../../manual/images/plugin-arcoutline-import.png)

The answer is `{name, description, note, chapters: [{name, description, note}, ...]}`; the arc and its chapters are
shown on the right as they stream in. *Save as arc* adds the arc with all its chapters at the end of the outline, each
with the reasoning and JSON of the request.

## Generating chapters

The wand on an arc opens the same kind of dialog for the chapters of that arc. On the left:

- *What the model knows of the story* - how much of the outline is sent; the arcs after this one never are:
  - *Whole story so far* - the arcs up to and including this one, each with all its chapters,
  - *Arc summaries only* - the arcs up to and including this one, without chapters,
  - *Arc summaries and finished chapters* - the same arcs with only their chapters that are *Finished*.
- *What you want from the chapters in general* - free text.
- *Number of chapters* - either in words ("about eight", "as many as the arc needs") or an exact number. Choosing a
  number (1 to 40) adds a text area for each chapter, to say what that chapter should contain (all optional); the two
  ways exclude each other, using one clears the other. An exact number is also put into the JSON schema, so an answer
  with another number of chapters is not valid.
- *Prompt* - a template with `{{backgroundLore}}`, `{{outline}}` (the arc being worked on is marked in it),
  `{{numberOfChapters}}`, `{{generalSpecification}}` and `{{chapterSpecification}}` (one string, a numbered list of what
  you wrote for each chapter). It starts as the chapter prompt of the settings (default `Defaults.CHAPTER_GENERATION_PROMPT`).
- Model, protocol and tools work as for arcs.

On the right the arc is shown with its chapters, and the chapters the model writes are added below them as the JSON
streams in. The answer is `{chapters: [{name, description, note}, ...]}` (terse description, full version in the note, like for
arcs); when it is valid, *Save as chapters* adds them to
the **end of the arc**, each with the full reasoning and JSON of the request (the robot button on a chapter shows them).

An arc made this way has a robot button (so has a generated chapter) that opens a popover with the reasoning and the JSON the model wrote.

## Commands

`ArcOutlineCommands` and `ArcGenerationCommands` register the outline and its generators for the command bar and for agents:
`/get-arc-outline`, `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc`, `/add-outline-chapter`, `/update-outline-chapter`,
`/move-outline-chapter`, `/delete-outline-chapter` and `/generate-arc`, `/generate-outline-chapters`, `/import-arc-from-story`
(see the user manual for the arguments). They work on `CommandContext.getScope().getManuscript()` (the open book, or the
working copy of an agent), address arcs and chapters by their numbers (no uuid leaves the plugin) and store the outline with
`ArcOutlineService.save`, then `ArcOutlineService.changed(manuscript)` tells the open `ArcOutlineView` of the book to read the
outline again (`onChange`; the view keeps the outline in memory and would overwrite the change otherwise). Updates take the
fields they are given, deleting is the only `DESTRUCTIVE` kind and asks `context.confirm`. The generators build the prompt
with the same `ArcOutlineService.build*Prompt` methods and schemas as the dialogs and ask the model with `ArcOutlineService.generate`
(a blocking `infer` that checks the answer against the schema); with `save` false they only return the result.

## How it works

The plugin decorates `renderStoryContent` of `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` and adds the tab to
their `leftBar`. The outline (`ArcOutline`) is loaded into memory when the tab is created and the whole outline is
stored after every change. The styles are in `arcoutline.css`, which the application puts into the page while the
extension is loaded (`ExtensionHasCss`).

## Storage

The arcs, with their chapters inside, are stored with the book (`com.github.enerccio.marginalia.extensions.arcoutline`
in its attributes: `arcs`), so they are included in book backups and copies. Every arc and chapter has a stable `uuid`;
a generated arc or chapter also has `generationReasoning` and `generationResult` (the raw reasoning and JSON of the
model). The prompts and the chosen tools (`tools`) are in the user settings
(`com.github.enerccio.marginalia.extensions.arcoutline.settings` in their attributes).
