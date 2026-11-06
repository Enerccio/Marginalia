# Example book

*Unregistered* is a finished book to open, read and take apart: about 100 parts (some 99,000 words) of one story, with everything the features of Marginalia leave behind. It is a good way to see what a long book looks like in the
[story editor](../books/story-editor.md), the [summaries](../books/summaries.md), the [annotations](../books/annotations.md) and the [statistics](../books/statistics.md), or to try the extensions on something that is already there.

> A Czech cellar guide who learned Japanese from novels and a Japanese nurse on holiday pass through a bricked archway in a Prague cellar and wake in a river valley held by a Vrakht-speaking empire.
> Neither knows the language, and a life-tax assessor has found two people who are not in his ledger.

## What is in it

| | |
|---|---|
| **The story** | 100 parts on the main line and a side branch of 3 parts, in 32 chapters (the headings are in the text). |
| **Summaries** | One for each chapter of any length (the last chapter of each 25 parts is left whole, as the model reads it raw) and the meta summaries that merge the oldest ones, in the compact format of the default [summary prompt](../books/summaries.md). |
| **Annotations** | About 450 review notes (continuity, style, lore, point of view, pacing) that the book's own *Annotate with AI* proposed and the redraft acted on; every part also has its versions (360 in all, see [version history](../books/version-history.md)). |
| **Lorebook** | 31 entries: the world, the places, the people and the languages. |
| **Prompts** | Each part keeps the prompt it was written from and the lore that was activated: **Show Prompt**, **Compare Prompt** and **Lore Usage** in the menu of the part ([Book prompts](../books/prompts.md)). |
| **Knowledge Tracker** | Seven tracked characters and what each of them knows and does not know, part by part ([Knowledge Tracker](../extensions/knowledge-tracker.md)). |
| **Translated speech** | About 1,250 translated passages: the speech of the Dominion in **Vrakht** and of the valley in **Aethel-Vael** ([Conlang Translator](../extensions/conlang-translator.md)), and **Japanese** and **Czech** ([Real Language Translator](../extensions/real-language-translator.md)). |
| **Chapters** | The outline of the story, made by [Arc Outline](../extensions/arc-outline.md) from the text. |
| **A picture** | One illustration, in part 31. |

The data of the four extensions is in the book, but you see it only with the extensions loaded; without them it is kept and ignored. The book has no model or protocol: after the import, choose yours in the
book's settings.

## Download

The files are attached to the release [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1):

| File | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip) (5.7 MB) | The book: the backup with its picture. |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | The lorebook of the book, on its own. The book file carries it too, you only need this one to look at the lorebook without the book. |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | The constructed language of the Dominion, for the Conlang Translator. |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | The constructed language of the valley, for the Conlang Translator. |

## Importing it

1. **The languages** (if you have the Conlang Translator): in **Settings → Extension Settings → Conlang Translator** click **Import** and choose `vrakht.json`, then do it again for `aethel-vael.json`
   ([Conlang Translator](../extensions/conlang-translator.md#languages)). Do it before the book, so that the book's language settings have something to point at.
2. **The book:** in the *Settings* tab of the book window click **Import Backup as New Book**, enter a name, and upload `unregistered-book.zip`
   ([how a backup is imported](../books/backups.md#importing-a-backup-as-a-new-book)). When Marginalia asks about the lorebook, choose **Import**: it is in the file.
3. Open the new book, and in its settings choose your model and protocol.

The translated passages are stored in the book, so the Vrakht, Aethel-Vael, Japanese and Czech speech shows as it was translated; to translate more with the Real Language Translator you create the languages *Japanese* and *Czech*
there (the plugin offers them as presets), because the plugin does not export its languages with a book.

## Where it comes from and the licence

The story, the lorebook and the two languages are original. The outline, the lorebook, the languages and the plan of the parts were written by the author of Marginalia; the text of the parts was **written by an AI model**
(Claude Haiku) through Marginalia's own generation loop: generate, annotate, redraft, track the knowledge, translate, summarise. It is a demonstration of the program, not a published novel, and it has the quirks of a model's prose.

- The text, the lorebook and the two languages are dedicated to the public domain under [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/): use them for anything.
- The picture is *Interior of a Cottage in Scheveningen* (1887) by Willem Adriaan Alexander Liernur, in the public domain, from [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art).
