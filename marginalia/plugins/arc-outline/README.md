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
| arrows | Move the arc or chapter up or down. |
| plus (arc) | Adds a chapter at the end of the arc. |
| wand (arc) | Generates chapters for the arc with the model, see below. |
| pen | Edits it in the dialog. |
| trash | Deletes it (an arc with its chapters), after a confirmation. |

A chapter moved up from the first place of its arc goes to the end of the previous arc, one moved down from the last
place to the start of the next arc, so chapters can be moved between arcs.

Marking an arc *Finished* marks all its chapters *Finished*. Other states don't change the chapters.

Changes are saved at once.

## The outline as a source

The plugin registers the outline with the `IPCService` as two sources of data about a book,
so other extensions can use it: `outline` ("Arc outline") has the arcs with their states, descriptions and chapters (the
terse version), `outline-with-notes` ("Arc outline (with notes)") has the same with the note of every arc and chapter
under its description (the full version). Nothing is sent unless the other extension is told to ask for it: the Side
Query can send either with a question (plug button → *Arc outline* or *Arc outline (with notes)*).

## Generating an arc

The wand next to *Add new arc* opens a dialog that asks the model for a new arc. On the left:

- *Prompt* - a template filled from the settings (*Settings → Extension Settings → Arc Outline*; the default is in
  `Defaults.GENERATION_PROMPT`). Its variables are `{{backgroundLore}}` (the entries of the book's lorebook and its sub
  lorebooks), `{{existingArcs}}` (the arcs and chapters of the outline with their states and descriptions, without
  the private notes) and `{{specification}}` (the text below). Edits here apply to this generation only.
- *Specification* - what you want from the arc.
- *Name tools* - shown when the Name Generator plugin is loaded: the categories the model may ask names of, as in the
  Lore Generator. The hint about the tools (editable in the settings) is added to the end of the prompt.
- *Generate*, which becomes a red *Stop* while the model runs. Everything else is disabled meanwhile.

On the right the reasoning of the model streams in as Markdown, and once the answer starts the arc shows as it would
in the outline (without chapters), filled in as the JSON streams. The model is asked for `{name, description, note}` with a
JSON schema: the `description` is terse (it is what the outline shows) and the full version goes to the `note`, which
the preview shows under the description; when it finishes, the answer is validated against the schema and only a valid one can be saved. *Save as
arc* adds it at the end of the outline and closes the dialog; *Generate* again tries once more, with the same or an
edited prompt and specification. The *Model* and *Protocols* lists at the top of the left side start as the model and protocol of the book (the default ones from the user settings when the book has no model) and can be changed for this generation; nothing is saved.

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
- Model, protocol and name tools work as for arcs.

On the right the arc is shown with its chapters, and the chapters the model writes are added below them as the JSON
streams in. The answer is `{chapters: [{name, description, note}, ...]}` (terse description, full version in the note, like for
arcs); when it is valid, *Save as chapters* adds them to
the **end of the arc**, each with the full reasoning and JSON of the request (the robot button on a chapter shows them).

An arc made this way has a robot button (so has a generated chapter) that opens a popover with the reasoning and the JSON the model wrote.

## How it works

The plugin decorates `renderStoryContent` of `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` and adds the tab to
their `leftBar`. The outline (`ArcOutline`) is loaded into memory when the tab is created and the whole outline is
stored after every change. The styles are in `arcoutline.css`, which the application puts into the page while the
extension is loaded (`ExtensionHasCss`).

## Storage

The arcs, with their chapters inside, are stored with the book (`com.github.enerccio.marginalia.extensions.arcoutline`
in its attributes: `arcs`), so they are included in book backups and copies. Every arc and chapter has a stable `uuid`;
a generated arc or chapter also has `generationReasoning` and `generationResult` (the raw reasoning and JSON of the
model). The prompts, the name tools hint and the name categories are in the user settings
(`com.github.enerccio.marginalia.extensions.arcoutline.settings` in their attributes).
