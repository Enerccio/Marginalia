# Lorebook VCS

Revision history for lorebook entries - keep earlier versions of your lore and go back to them.

A revision is a snapshot of the whole entry: name, text, comment, enabled flag, order, filter and filtering mode,
insertion mode, tags and negative tags.

![Entry revisions](../../../manual/images/plugin-lorebookvcs.png)

## Usage

**Entry revisions** - the detail of every lorebook entry gets a revision bar. Revisions work like slots: the entry
always shows one of them, and edits you make to the entry belong to that revision.

- **+** saves the entry as it is now as a new revision,
- **‹ ›** switch to the previous / next revision - your current edits are kept in the revision you leave, and the
  other one is loaded into the entry (that's what generation uses from then on),
- **−** deletes the current revision and switches to the previous one (the last remaining revision can't be deleted),
- the bar shows the revision number and when it was modified.

**Lorebook revisions** - the lorebook toolbar gets a *Lorebook Revisions* panel to:

- export the history of all entries of the selected lorebook as JSON,
- import a history exported by this plugin (entries are matched by identity, then by name and order, so it also
  attaches to an imported copy of the lorebook),
- import revision history from SillyTavern (entries are matched by their order in the lorebook, trigger keys become
  the entry's filter).

An import replaces the lorebook's history and asks for confirmation when there is one.

## Storage

The history is stored in the database with the lorebook, so it's included in database backups. It's not part of
lorebook exports or of the lorebooks inside book backups - use the history export to move it.
