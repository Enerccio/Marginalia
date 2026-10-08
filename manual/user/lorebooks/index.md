---
label: Lorebooks
order: 950
icon: archive
---

# Lorebooks

A lorebook holds what the model must know about your world: characters, places, history, rules, terminology. Each
piece of knowledge is an **entry**. Before every generated part, Marginalia decides which entries are relevant and puts
them into the prompt - so the model knows who Captain Varro is when he walks into the scene, without every entry
taking up space in every prompt.

![The lorebook editor](../../images/lorebook.png)

## How it fits together

```
Book ──uses──▶ Lorebook "Saga" ──sub lorebooks──▶ "Characters", "Places", "Magic"
                   │                                   │
                 entries                             entries
```

- A **book** uses one lorebook (on its *Lorebook* tab).
- A lorebook can include other lorebooks as **sub lorebooks**. Their entries take part too, so you can keep shared
  material (a world used by several books, a cast of characters) in its own lorebook and include it where needed.
- Each **entry** has the text that goes into the prompt, and conditions that decide when it is used.

## When an entry is used

For each generated part, an entry is **activated** when all of these are true:

1. the entry and its lorebook are enabled;
2. its **tags** fit the book's tags (if it has any);
3. its **filter** matches the instructions for the part (if it has one).

Activated entries are inserted in their **order** - into the lore block of the system prompt, or right before the
request for the part. See [Entries & activation](entries.md).

Entries without tags and without a filter are always active: use them for what the model must always know.

## Lorebooks and other features

- Lorebook entries can use [templates and macros](../templates/index.md) - SillyTavern world info keeps working.
- [SillyTavern world info](import-export.md#importing-sillytavern-world-info) can be imported.
- [Book backups](../books/backups.md) contain the book's lorebook with its sub lorebooks.
- The [Lorebook VCS](../extensions/lorebook-vcs.md) extension keeps a revision history of entries.

## In this section

- [Creating lorebooks](lorebooks.md) - the lorebook editor, sub lorebooks, using a lorebook in a book
- [Entries & activation](entries.md) - entry fields, tags, filters, order and insertion
- [Import & export](import-export.md) - Marginalia lorebook files and SillyTavern world info
