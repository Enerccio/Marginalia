# Real Language Translator

<img src="icon.svg" alt="" width="64" align="right">

Lets the characters of a story speak a **real** language (Japanese for a story set in Japan): you write the dialogue in English and the plugin has a model translate the direct speech
into authentic Japanese (kana and kanji, kana only, or romaji) while the story keeps the English behind it. It is the sibling of the
[Conlang Translator](../conlang-translator/README.md) and does the same work for a language the model already knows: no lexicon, no designer, the rules are a short note on the script,
the honorifics, the politeness and the names. What the two share is the library [`translator-common`](../../libs/translator-common) (the data of a translated speech, the reverse to the English
every model reads, the text of the reader and of the export, the writer of a translation into a part, the run loops, the speakers, the range window), embedded in both bundles.

**State: works; the request and the glossary were tried on a real model in the real app (see the TODO).** It registers its passage kind (`reallang-translator.speech`, a mark with a dotted green line, the click shows the reading and the English) and the
transformer that makes every model read the English of such a speech; has the languages in *Settings → Extension Settings* (presets for Japanese), the inherited fields of books and folders
(language, translate by itself, model, protocol, what readers see), the *Real language* tab of the story (speakers, other languages of the book, quick translate), the menu items (part, selection, range), the viewer, the
export field and contributor, and the commands (`reallang-*`). The translation is one request per language of the book for a part ([`TranslationPrompt`](src/main/java/com/github/enerccio/marginalia/extensions/reallangtranslator/translate/TranslationPrompt.java)),
checked for the quote, the script and the glossary of names ([`Glossary`](src/main/java/com/github/enerccio/marginalia/extensions/reallangtranslator/model/Glossary.java): the language, the folders and the book, merged; a name the model had to choose is proposed to the book). What is left is in [TODO.md](TODO.md); the plan and the decisions are in the *Real languages* section of the
[Conlang Translator's TODO](../conlang-translator/TODO.md). The user's page is [Real Language Translator](../../../manual/user/extensions/real-language-translator.md).

Build (the same source tree as the application): `mvn install -DskipTests` in `marginalia/`, `mvn install -DskipTests` in `marginalia/libs/translator-common`, then `mvn package` here, and
load the JAR in *Admin → Extensions*.
