---
label: Extensions
order: 930
icon: plug
---

# Extensions

Extensions add features to Marginalia without changing the application itself. Fifteen extensions come with Marginalia:

| Extension | What it adds |
|---|---|
| [Arc Outline](arc-outline.md) | The arcs of the book and their chapters, with their state, in the sidebar; the model can write an arc for you. |
| [Author's Note](authors-note.md) | A note to the model, inserted into every generation of the book. |
| [Chapter Marker](chapter-marker.md) | Chapter titles in the story outline, from Markdown headings in the parts. |
| [Character Tracker](character-tracker.md) | The characters of the book with their relationships, and a map of them. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Makes function calling work with Gemma 4 models whose API wants another role for tool results. |
| [Google Gemini](google-genai.md) | Google's Gemini models as an inference provider, over Google's own API, with the thinking summaries shown as reasoning. |
| [Instruction Exporter](instruction-exporter.md) | Copies the instructions, scene settings and characters of the story to the clipboard. |
| [Lore Generator](lore-generator.md) | Writes the content of a lorebook entry with the model, from the lore you already have. |
| [Lorebook VCS](lorebook-vcs.md) | Revision history for lorebook entries. |
| [Tools for the model](tools.md) | The **Tools** button of the generators: which tool suites (names, places) the model may use, and which of their categories. |
| [Name generator](name-generator.md) | Names for the model to use instead of inventing them: about 1270 lists of names, as Markov chains. |
| [Name provider](name-provider.md) | Real names for the model to pick from: forenames and surnames of about 100 countries, with the original spelling. |
| [Place provider](place-provider.md) | Real places for the model to look up: cities, seas, bays, rivers, mountains and more with coordinates and countries, what is around a place and the distance of two. |
| [Reviewer](reviewer.md) | Reviews of story parts written by the model - reader reactions, critique. |
| [Side Query](side-query.md) | A chat with the model about your book, next to the story. |
| [SillyTavern Converter](sillytavern-converter.md) | A SillyTavern chat next to the story and turn details the model prepares from it, to rewrite the chat as a story. |
| [Chinese localization](chinese-localization.md) | Simplified Chinese as a language of the user interface. |

## Installing

Extensions are installed by an **administrator** for the whole Marginalia installation, in *Admin → Extensions*:
they are JAR files, uploaded with **Load Extension (.jar)**. See [Managing extensions](../administration/extensions.md)
for the details.

Once installed, an extension is available to every user. An extension loaded while Marginalia runs shows up in
windows opened afterwards: close and reopen a book (or reload the page) to see it.

!!!warning Use matching versions
Extensions hook into the user interface of one Marginalia version. An extension built for a different version may stop
working or break parts of the application. Use the extensions that come with your version of Marginalia.
!!!

The fifteen extensions above are part of the Marginalia source code (`marginalia/plugins`) and are built separately;
see the [plugins README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

## Extensions on a phone

The [phone layout](../phone.md) has its own classes, so an extension has to support it. Arc Outline, Author's Note, Chapter Marker,
Character Tracker, Instruction Exporter and Reviewer do; Side Query, Lore Generator and Lorebook VCS add nothing on a phone (see [Using Marginalia on a phone](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix and Google Gemini have no screens of their own (Google Gemini only adds a provider to the model dialog, which a phone has as well), and Chinese localization is in the language selector of the phone menu. Extensions are installed from the Admin page,
which a phone doesn't have.

## Extension settings

Extensions with settings add a section to **Settings → Extension Settings** (the third tab of the *Settings* page).
These settings belong to your account, like the rest of *Settings*, and are saved with its **Save** button.

## Where extension data is kept

Extensions keep their data together with the data it belongs to - reviews with the story part, conversations with the
book, revisions with the lorebook - so it is included in [database backups](../administration/database-backups.md).
Whether it is also part of [book backups](../books/backups.md) and exports is described on each extension's page.

Uninstalling an extension doesn't delete its data: installing it again brings it back.
