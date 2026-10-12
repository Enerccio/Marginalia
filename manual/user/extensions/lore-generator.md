---
label: Lore Generator
order: 85
---

# Lore Generator

Lets the model write the content of a [lorebook entry](../lorebooks/entries.md) for you. It reads the lore you already
have, so the new entry fits the world, and compacts the result into the short, dense form lore entries work best in.

!!!
Lore Generator is not available in the [phone layout](../phone.md#extensions).
!!!

## Opening the generator

With the extension installed, every row of the lorebook editor gets a wand button (**Generate lore entry**). It opens
the generator for that entry. If the entry already has content, the dialog says so at the top: copying the result to
the entry replaces the content.

The generator uses your **Default Model** and **Default Protocol** from *Settings*, see
[Account & settings](../account.md). Without a default model it only shows a warning.

## What the model sees

The two choices at the top of the dialog decide which lore goes into the request:

| Choice | |
|---|---|
| **Current Lorebook** (default) / **All Lore** | Only the entries of the lorebook the entry is in, together with its [sub lorebooks](../lorebooks/lorebooks.md); or the entries of all your lorebooks. |
| **Only up to position (n)** (default) / **All entries** | *n* is the order of the entry. Only the entries that come before it are shown (a lower order, or the same order and created earlier), so the model builds on what already exists and doesn't get lore that was written after the entry. *All entries* shows everything. |

The entry you are generating is never included, with or without content. Whether other entries or lorebooks are
enabled, their tags and filters and their insertion mode are ignored: what you pick here is what is sent, in the
order the entries are inserted into a prompt. The templates of the entries are processed as usual.

## Prompts

The generation prompt is what you ask the model to write. The prompts are listed in the box next to the choices, in the
same top row; the text of the selected one is in the text area on the left, and you can change it for a single
generation without saving. The two halves of the dialog are separated by a splitter you can drag.

| Button | |
|---|---|
| **Save** | Saves the text under the selected name. |
| **Save as** | Asks for a name and saves the text as a new prompt (or replaces one of that name, after asking). |
| **Delete** | Deletes the selected prompt. When the last one is deleted, the built-in `DEFAULT` prompt comes back. |

The built-in prompt has placeholders in brackets (the subject, the details to include, the length, the structure):
fill them in before you run it. Prompts belong to your account.

## Generation and compaction

The right side has two tabs.

**Generation** - **Run Generation** sends the lore followed by the generation prompt. The answer appears as it is
written, with the model's reasoning (when it gives any) in a *Thinking Process* section above it. **Stop** (in place of
*Run Generation*) ends it early; what was written so far is kept.

**Compaction** - available once there is a generation. **Run Compaction** sends the same lore followed by the
compaction prompt, with the generated text in it, and the model condenses it into a compact entry. The compaction
prompt is set in *Settings → Extension Settings → Lore Generator*; when it is empty, the built-in one is used (it
strips the prose and keeps the facts). Running the generation again discards the compaction.

While the model is working, all controls are frozen except **Stop**; you can't close the dialog either.

### Attachments

Under the prompt you can add files for the model to look at: reference images, a map, a character sheet. Drop them on
the upload area, choose them, or paste them into the prompt with Ctrl+V (a screenshot or a copied file becomes an
attachment; pasted text is inserted as usual). Up to 20 MB each. They are listed with their name, type and size, and the trash
button removes one. They are sent with the generation, not the compaction, and you can add and delete them only while
no generation runs.

Whether the model can use them depends on the model and the [inference provider](../inference-providers.md): images
are sent as images, audio only as WAV or MP3, other files as files. When the provider refuses an attachment, you get a
message and nothing is generated.

## Copying the result

**Copy to Lore Entry** is available when the compaction has a result. The compacted text becomes the **content** of
the entry, and the generated text before the compaction goes into its **note**. Both are replaced. If the compaction
is one code block (the built-in prompt asks for that, to make the text easy to copy), the code block around it is
removed. The lorebook is refreshed, and the dialog stays open; **Exit** closes it.

## Storage

The prompts and the compaction prompt are stored with your settings, so [database backups](../administration/database-backups.md)
include them. Nothing is stored with the lorebook; the generated text exists in the dialog only until you copy it.
