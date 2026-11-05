# Real Language Translator

<img src="icon.svg" alt="" width="64" align="right">

Lets the characters of a story speak a **real** language (Japanese for a story set in Japan): you write the dialogue in English and the plugin has a model translate the direct speech
into authentic Japanese (kana and kanji, or romaji) while the story keeps the English behind it. It is the sibling of the
[Conlang Translator](../conlang-translator/README.md) and does the same work for a language the model already knows: no lexicon, no designer, the rules are a short note on the script
and the politeness. What the two share is the library [`translator-common`](../../libs/translator-common) (the data of a translated speech, the reverse to the English every model reads,
the text of the reader and of the export, the run loops, the speakers), embedded in both bundles.

**State: a skeleton.** The bundle loads, registers its passage kind (`reallang-translator.speech`, a mark with a dotted green line, the click shows the English) and the transformer that
makes every model read the English of such a speech, and has its CSS, localization class and icon. The translating itself, the language settings, the commands (`reallang-*`) and the
second pass are the steps of [TODO.md](TODO.md); the plan and the decisions are in the *Real languages* section of the
[Conlang Translator's TODO](../conlang-translator/TODO.md).

Build (the same source tree as the application): `mvn install -DskipTests` in `marginalia/`, `mvn install -DskipTests` in `marginalia/libs/translator-common`, then `mvn package` here, and
load the JAR in *Admin → Extensions*.
