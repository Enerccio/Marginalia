# Story Notes: open

- **Storage off the book row.** The pages live in the `attributes` of the book, which are a part of its gzipped
  `extendedContent`: every load of the book reads and parses all of it, and saving one page writes the whole row (while a
  generation writes the same row). A core "resource" entity (1-N, `extendedContent` only, keyed by owner type, owner uuid and
  plugin key, loaded when asked) would fix it for every plugin, but it has to be taken into backups, copies of a book, the
  working copy of an agent, soft delete and cleanup first; see the item in the main `TODO.md`. All the storage of this plugin is in
  `StoryNotesService.read` / `update`, so a move is a change of those two and a one-time migration of the stored attributes.
  A resource per page would also make a save write only that page.
- The pictures are taken by `screenshots/flows/plugin-storynotes.mjs` (a copy of `23-betareaders`, not a step of the chain).
- Phone: the tab is in the outline drawer like the others; the dialog has not been tried on a small screen.
- Not done on purpose: keyword activation of a page (that is the lorebook), an automatic insertion into the prompt. A page can be put
  into a template with `{{command::note-text::Title}}`.
- Ideas: a page for a story part ("see part 14"), an annotation linking to a page, "summarize this page" and "check the latest part
  against this note" through the reviewer or the side query.
