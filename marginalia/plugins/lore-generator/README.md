# Lore Generator

Generates the content of a lore entry from the existing lore. Works in the lorebook editor of the desktop UI (not on the phone).

Every row of the lorebook has a wand button that opens the Lore Generator dialog for that entry (a warning is shown when the entry already has content).

- **Lore**: *Current Lorebook* (only the lorebook itself, its subbooks are ignored) or *All Lore* (the lorebook together with its subbooks).
- **Entries**: *Only up to position (n)* (n is the order of the entry; entries with a higher order are left out, ties are broken by creation) or *All entries*. The entry being generated is never included.
  Enabled flags, tags, filtering and insertion modes are ignored, only the content of the entries is used.
- **Generation prompt**: prompts are stored in the user settings; Save, Save as and Delete manage them. Deleting the last one recreates `DEFAULT`.
- **Generation**: the system prompt is `[ Existing Lore ]` with the selected entries followed by the prompt from the text area.
- **Attachments**: images, audio (wav, mp3) and other files can be uploaded under the prompt (up to 20 MB each); they are sent with the generation request, not with the compaction. Each one has a delete button, disabled while an inference runs. Whether they are used depends on the model.
- **Compaction**: the same lore followed by the compaction prompt (Settings → Extensions → Lore Generator, the built-in one when blank) in which `{{compactionData}}` is replaced by the generated text.
- **Copy to Lore Entry**: the compaction result becomes the content of the entry (a top-level code block around it is removed), the generation result goes to its note. Both are replaced.

The model and the protocol are the default ones from the user settings.
