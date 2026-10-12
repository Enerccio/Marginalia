# Instruction Exporter

Copies the instructions of the story - with the scene setting, POV character and present characters - to the clipboard.

Adds **Export instructions** to the settings (⚙) menu of the story editor. It goes over the parts of the active branch,
from the first one to the current leaf and copies the fields each part was generated from as one text:

```
Scene Setting:
A harbour town at dawn
POV Character:
Mara
Present Characters:
Mara, the harbourmaster
Instructions:
Mara arrives and is questioned.

Scene Setting:
...
```

Every filled field is a `Field:` line followed by its contents; the parts are separated by a blank line, in story order.
Fields that are empty are left out, and so are parts with no filled field.

## Usage

Nothing to configure. Install the plugin (see [Marginalia plugins](../README.md)), open a book and choose
*Export instructions* in the settings menu. The text is on the clipboard, paste it anywhere.

The browser only allows writing to the clipboard on a secure page: `https://` or `localhost`.
