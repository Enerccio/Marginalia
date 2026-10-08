# Reviewer

Lets the model review what you've written - get reader reactions, critique or anything else you prompt it for, right
next to the story.

![Message review](../../../docs/images/plugin-reviewer.png)

## Usage

Every story part gets a **Review** item in its menu:

- **View / Generate Review** opens the review dialog. *Generate* writes a new review of the part (it can be stopped
  while generating); a part can have several reviews and you can page through them.
- **Advanced Options** changes how this part is reviewed:
  - *Prompt Override* - a different review prompt for this part,
  - *Use standard prompt info* - include the book's usual prompt information (style, POV, scene...),
  - *Include Lorebook* - include the activated lore,
  - *Token Limit* - maximum length of the review.
- **Delete Review** removes the reviews of the part.

## Settings

Reviewer profiles are configured on the *Settings* page. A profile has:

- *Review Pre-Prompt (System)* - the system prompt,
- *Review Post-Prompt (User)* - what to ask for; the default simulates readers' comments on a story forum,
- the model and protocol to use - by default the book's own.

Create several profiles (a strict editor, enthusiastic readers...) and pick the default one.

## Storage

Reviews are stored with the story part, so they are included in book backups and copies. Profiles are stored in
your user settings (included in database backups).
