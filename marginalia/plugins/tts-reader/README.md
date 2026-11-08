# Read Aloud

Reads a published book aloud in the viewer, with the voice of your device.

## Usage

The reader (the *Viewer* page of a book) gets a small control next to the style button, top right:

- **Play** reads from the part that is on screen, or from the part where your text selection starts; the next parts are
  loaded as the reading goes on, and the part that is read is highlighted and scrolled into view,
- **Pause** / **Stop**,
- the **gear** opens the voice (the voices of the browser, those of the language of the book first; the default is the
  best voice for that language), the speed and the pitch.

The speech is the one of the browser (`speechSynthesis`): nothing is sent to a server, nothing is stored on one. The
choices are kept in the browser's storage, so they belong to the device. What is read is the text on the page, so the
translated speech of the Conlang and Real Language translators is read the way the book shows it.

Limits: the voices and their quality depend on the device and browser; pause may not work on every phone (the reading
then stops where it was). The control is hidden when the browser can not speak.

## Building

See the [plugins README](../README.md#building-plugins).
