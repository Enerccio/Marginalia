# Branches & story tree

Marginalia never makes you throw a version away. Every alternative you try is kept as a **branch** of the story, and
you can go back to any of them.

## How the story tree works

Each part continues the part before it. When a part has more than one continuation, the story splits: each path from
the first part to an end is a branch.

```
#1 ─ #2 ─ #3 ─ #4 ─ #5          ← branch A
           └─ #4' ─ #5' ─ #6'   ← branch B (active)
```

The **active branch** is the one the [story editor](story-editor.md) shows and continues. Only its parts are sent to
the model; the other branches don't influence the story.

There are three ways to get a new branch:

| | What happens | The old version |
|---|---|---|
| **Swipe** (last part) | The model writes another version of the last part. *(Currently broken, see below.)* | Stays as a separate branch. |
| **Branch Story** (any part) | The story continues from this part on a new branch. | The parts after it stay on the old branch. |
| **Regenerate** (last part) | The model writes the last part again, in place. | Replaced - no branch is created. |

*Swipe* and *Regenerate* are in the menu of the last part only; *Branch Story* is in the menu of every part.

## Trying another version of the last part

When you don't like the last part, choose **Swipe** in its menu. The model writes the part again from the same
[turn details](story-editor.md#turn-details), and the new version becomes the end of the active branch. The previous
version is kept as its own branch; you can switch back to it in the [Branch view](#the-branch-view).

!!!warning Swipe doesn't work yet
In the current version *Swipe* does not write another version: it adds a **new part after** the last one, written
from the same turn details. Until this is fixed, get another version like this:

1. Choose **Branch Story** in the menu of the last part. The part is copied to a new branch, which becomes active.
2. Choose **Regenerate** on the copy (adjust its **Turn Details** first if you want).

The original version stays on the old branch, reachable in the [Branch view](#the-branch-view).
!!!

To change what should happen before trying again, open **Turn Details** of the last part and edit the instructions.

Use **Regenerate** alone when you don't need the old version: it overwrites the part.

## Branching from an earlier part

To take the story in a different direction from an earlier point, open the menu of that part and choose
**Branch Story**. Marginalia copies the part to a new branch and makes it the end of the active branch, so the
story editor now ends with that part. Continue with **+** as usual.

The parts that came after it are not lost: they stay on the old branch.

[Summaries](summaries.md) of the copied part are copied with it.

## The Branch view

The **Branch view** tab shows the whole story tree. Each part is a card with its date, scene setting, characters
present and instructions; hover over a card to see all its details.

![The story tree](../../images/story-tree.png)

- The parts of the **active branch** are highlighted, and its last part is marked.
- **Click the last part of another branch** to make that branch active. The book switches to the *Story* tab and
  shows it. Only the ends of branches can be clicked; to continue from a part in the middle, use
  [Branch Story](#branching-from-an-earlier-part).
- Long stretches without any branching (more than 20 parts) are collapsed into a block showing the first ten and the
  last ten parts. Click the block to show all parts.

## Deleting parts and branches

**Delete** in the menu of a part removes just that part. The parts after it move up and continue from the part before
it. To remove a whole branch, delete its parts from the end.

The word and token counts on the *About* tab show both the whole book (all branches) and the active branch.
