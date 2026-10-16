# Character Tracker

A list of the characters of the book, with the relationships between them and a map of those relationships, in the
story sidebar.

![The Characters tab](../../../manual/images/plugin-charactertracker-tab.png)

## Usage

The story view gets a *Characters* tab in its left sidebar (next to the outline). It has:

- **Add new character** - a dialog with *Name*, *Tagline*, *Note*, *Icon* and *Color*. The same dialog edits a
  character (the pen). An uploaded icon is scaled down to at most 200 x 200 pixels (the proportions are kept, smaller
  images stay as they are) and stored in the book as a base64 PNG.
- The wand menu next to it, with **Import lore entries as characters**: a dialog lists the entries of the book's
  lorebook and of its sub lorebooks, sorted by their order, each with a checkbox and an (i) icon that shows the entry's content in a popover (the checkbox in the header checks all); *Import*
  creates a character with the name of every checked entry.
- **Relationship map** - all relationships as a tree, see below.
- The list of the characters, ordered by name. Each shows its name and, in small letters (two lines at most), its
  tagline, on the color of the character. The chevron expands the character to show its icon and note, the pen edits
  it, the trash deletes it with all its relationships.

### Relationships

The dialog of an existing character lists the relationships it is part of: those it points to (`→`), those that point
to it (`←`) and the unidirectional ones (`↔`), each with a name and a tagline, and has a trash button to delete one.
**Add relationship** searches for the other character, takes the direction (this → other, other → this, both ways), a
name and a tagline. Relationships are saved at once; the fields of the character itself when you press *Save*. A new
character can get relationships after it has been saved.

### Relationship map

A graph (Cytoscape.js, the application's `GraphView`): a node for every character that has a relationship, with its
icon, name and tagline on its color, and an arrow for every relationship, with its name and tagline (an arrow at both
ends when it is unidirectional). The characters are arranged in rings around the character with the most
relationships. **Double-click a character** to arrange the map around it. Drag to move characters, scroll to zoom.
Characters without relationships are not in the map.

### Screenshots

![A character with its relationships](../../../manual/images/plugin-charactertracker-dialog.png)

![Cancelling a character with unsaved changes](../../../manual/images/plugin-charactertracker-unsaved.png)

![The relationship map](../../../manual/images/plugin-charactertracker-map.png)

![Importing characters from the lorebook](../../../manual/images/plugin-charactertracker-import.png)

## How it works

The plugin decorates `renderStoryContent` of `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` and adds the tab to
their `leftBar`. The characters are loaded into memory (`CharacterBook`) when the tab is created and the whole book of
characters is stored after every change.

## Storage

Characters and relationships are stored with the book (`com.github.enerccio.marginalia.extensions.charactertracker` in
its attributes: `characters` and `relations`), so they are included in book backups and copies. A relationship refers
to its characters by `sourceUuid` and `targetUuid`.
