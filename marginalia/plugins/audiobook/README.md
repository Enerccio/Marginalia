# Audiobook

Turns a book into an audiobook, part by part, with a text-to-speech server and a voice for the narrator and each
character.

**Status: work in progress.** So far: the settings (the speech servers and the requests made around a batch, *Settings → Extension Settings → Audiobook Settings*) and *Convert to audio* in the tools menu of the story editor (a queue: the narrator reads the chosen parts, one mp3 per part, cached by content). A player in the reader (headphones button) plays the converted parts; the parts can be labelled by a language model first (who speaks, how, sound tags). The studio (tools menu) shows the script of a part segment by segment: edit, hear, take again, make the part, mark it final. A new part can be made into audio by itself when it is final (off by default). The export of the book has two audiobook formats (a zip with a file per chapter, one mp3 with chapter marks). The text is spelled for the voice (numbers, abbreviations, numbered headings, a dictionary of the book), the book itself is not changed. The cast of a book gives the narrator and each character an actor (a voice of yours, also per mood and per language); see [TODO.md](TODO.md) for the plan and for what was found out on a real server (`experiments/` has the scripts). For reading
aloud with the voice of your device, without a server, use [Read Aloud](../tts-reader/README.md).

## Building

See the [plugins README](../README.md#building-plugins).
