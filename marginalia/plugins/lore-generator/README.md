# Lore Generator

Generates the content of a lore entry from the existing lore. Works in the lorebook editor of the desktop UI (not on the phone).

![The lore generator](../../../manual/images/plugin-loregenerator.png)

Every row of the lorebook has a wand button that opens the Lore Generator dialog for that entry (a warning is shown when the entry already has content).

- **Lore**: *Current Lorebook* (only the lorebook itself, its subbooks are ignored) or *All Lore* (the lorebook together with its subbooks).
- **Entries**: *Only up to position (n)* (n is the order of the entry; entries with a higher order are left out, ties are broken by creation) or *All entries*. The entry being generated is never included.
  Enabled flags, tags, filtering and insertion modes are ignored, only the content of the entries is used.
- **Generation prompt**: prompts are stored in the user settings; Save, Save as and Delete manage them. Deleting the last one recreates `DEFAULT`. The prompt used last is selected again when the dialog opens.
- **Generation**: the system prompt is `[ Existing Lore ]` with the selected entries followed by the prompt from the text area.
- **Tools**: the common `ToolSelectionButton`, shown while a suite of tools for the model is installed (the [Name generator](../name-generator/README.md), the [Name provider](../name-provider/README.md), the [Place provider](../place-provider/README.md)). It opens the `ToolSelectionDialog` (a section for each suite with a filter of its own, the hint of the suite editable there) and keeps a `ToolSelection`, saved in the user settings (`LoreGeneratorData.tools`) and with the entry. `LoreGeneratorService.tools` turns it into the functions and the hints (`ToolService.resolve`); the hints are added to the end of the generation prompt, the functions go to the generation, not to the compaction. What older versions saved (the name categories with the prefix of their source, the two hints) is read once by `LegacyNameTools`.
- **Attachments**: images, audio (wav, mp3) and other files can be uploaded under the prompt (up to 20 MB each); they are sent with the generation request, not with the compaction. Each one has a delete button, disabled while an inference runs. Whether they are used depends on the model.
- **Compaction**: the same lore followed by the compaction prompt (Settings → Extensions → Lore Generator, the built-in one when blank) in which `{{compactionData}}` is replaced by the generated text.
- **Remembered per entry**: *Copy to Lore Entry* also stores the options of the generation in the attributes of the entry: the prompt (and the name of the saved prompt it came from), the lore and entries choices and the checked tools. The next time the generator opens for that entry it restores them, ahead of the last used prompt and the name categories of the user settings.
- **Copy to Lore Entry**: the compaction result becomes the content of the entry (a top-level code block around it is removed), the generation result goes to its note. Both are replaced.

The model and the protocol start as the default ones from the user settings; the two lists under the header of the dialog change them for this generation (nothing is saved). They are disabled while the model runs.

### Commands

`LoreGeneratorCommands` registers `/list-lore-templates [template]`, `/generate-lore-entry [prompt [template [lorebook [entry [order [scope [limit]]]]]]]` and `/compact-lore-entry text [lorebook [entry [order [scope [limit]]]]]` for the command bar and for agents. All three are `NON_DESTRUCTIVE`: like the dialog they store nothing, the generated and the compacted (unfenced) text are the result, and the caller puts it into an entry. They extend `LorebookReadSupport` of the application, so the lorebook and the entry are numbers like in `/list-lorebooks` and `/list-lore-entries`. The entry gives the place of the text in the lore (`limit`); an entry that does not exist yet is stood for by its `order` (after all the others when omitted). `/generate-lore-entry` always starts from a saved generation prompt (the `template`, else the one used last, `none` for none) and appends the `prompt` of the caller under `### Task for this entry`, since a short prompt alone gives a poor entry; the descriptions tell an agent what the prompt must contain. The commands are `async`, the request is cancelled when the run is stopped. The model is the user's default model and protocol, with no name tools: an agent gets names from the commands of the Name generator and the Name provider and writes them into the prompt.

### Screenshots

![The compaction of a generated entry](../../../manual/images/plugin-loregenerator-compaction.png)

![The Lore Generator settings](../../../manual/images/plugin-loregenerator-settings.png)
