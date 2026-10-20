---
label: Creating lorebooks
order: 100
---

# Creating lorebooks

## The lorebook editor

Open the **Lorebooks** tab. The editor shows one lorebook at a time; pick it in the **Lorebook** field at the top.
Lorebooks belong to your account: other users don't see them.

The toolbar has two rows, which wrap onto more lines in a narrow window: the first for the lorebooks (**Lorebook** to **Tag lorebooks**), the second for the entries of the selected lorebook (**Add Entry** to **Refresh**).

| Button | |
|---|---|
| **Lorebook** | Selects the lorebook to edit. |
| **Add Lorebook** | Creates a new lorebook named *New Lorebook* and selects it. |
| **Delete Lorebook** | Deletes the selected lorebook (after confirming). |
| Import (file icon) | **Import Marginalia Lorebook** or **Import from SillyTavern**, see [Import & export](import-export.md). |
| Export (file icon) | Downloads the lorebook with its sub lorebooks, see [Import & export](import-export.md#exporting). |
| Tags (tags icon, last of the first row) | **Tag lorebooks**: puts a tag on many lorebooks at once, see [below](#tagging-many-at-once). Not shown for a lorebook that is open in a book. |
| **Add Entry** | Adds an entry to the lorebook, see [Entries & activation](entries.md). |
| **Search and replace** | Changes a text in the name, text and comment of the entries of the lorebook, see [Search and replace](entries.md#search-and-replace). |
| Tags (tags icon, right of **Search and replace**, second row) | **Tag entries**: puts a tag on many entries of the selected lorebook at once, see [below](#tagging-many-at-once). |
| **Refresh** | Reloads the entries. |

Below the toolbar are the settings of the selected lorebook:

| Field | |
|---|---|
| **Name** | The name of the lorebook, shown when you pick it for a book or as a sub lorebook. |
| **Sub Lorebooks** | Other lorebooks whose entries are included with this one, see [below](#sub-lorebooks). |
| **Tags** | Tags of the whole lorebook; they count as tags of each of its entries, see [Tags](entries.md#tags). |
| **Enabled** | When unchecked, the lorebook contributes nothing - neither its entries nor those of its sub lorebooks. |

The rest of the editor is the list of [entries](entries.md). All changes are saved as you make them.

A lorebook of a book an agent works on is shown read only (see [Agents](../agents.md)), without **Add Entry**, **Search and replace**, **Delete Lorebook** and the settings; **Add Lorebook**, import, export and **Refresh** stay, so you can always start another lorebook.

## Tagging many at once

The two tags buttons of the toolbar open one window that puts a tag on many items, or takes it off them: **Tag lorebooks** lists
your lorebooks, **Tag entries** the entries of the selected lorebook. Pick the **Tag** (or type a new one and press Enter);
the list shows every item with its tags, and the items that have the picked tag come first with the tag highlighted. A row's
**+** and **-** buttons apply or remove the tag for that item; tick several items (the box in the header ticks all) and
use **Apply the tag to the checked** or **Remove the tag from the checked** at the bottom to do it for all of them. An item
an agent works on (a lorebook of a book with a running agent, and so all its entries) is grayed out and can not be changed
until the run is paused or stopped. The window also tags [books](../books/managing-books.md#tagging-many-books-at-once).
The tags it puts on entries are the ordinary ones, see [Tags](entries.md#tags); negative tags are set on the entry itself.

## Using a lorebook in a book

Open the book and its **Lorebook** tab, and pick the lorebook in the **Lorebook** field. The editor below shows the
lorebook, so you can add and change entries while you write. Clear the field to write without a lorebook.

A book uses one lorebook. To use several, create a lorebook that includes them as sub lorebooks, and use that one.

The same lorebook can be used by any number of books. Changes to it affect all of them from the next generated part
on; parts that were already written keep the lore they were written with.

## Sub lorebooks

**Sub Lorebooks** includes the entries of other lorebooks in this one. A typical setup:

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- Sub lorebooks can have sub lorebooks of their own; all levels are included.
- A lorebook can be a sub lorebook of several lorebooks. When it is reached more than once, its entries are still used
  only once.
- A lorebook can't include itself, directly or through other lorebooks: such a choice is refused with
  *Cannot select sub lorebook: cycle detected.*
- A disabled sub lorebook is left out together with everything it includes.

The entries of all included lorebooks are put together and sorted by their [order](entries.md#order-and-insertion),
regardless of which lorebook they come from.

## Disabling and deleting

Uncheck **Enabled** to switch a lorebook off for a while, for example to see how a book reads without it. Books keep
using it, and it works again when you check it.

**Delete Lorebook** removes the lorebook from your lists. Books that used it no longer get its entries, and lorebooks
that included it as a sub lorebook skip it. The data stays in the database until an administrator runs
[Cleanup](../administration/cleanup.md), and until then you can restore the lorebook from the [Trash](../trash.md); copies in [book backups](../books/backups.md) and exported files are not
affected.
