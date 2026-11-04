# Conlang Translator

Lets the characters of a story speak a constructed language. You describe the language (rules and a lexicon, written with the model's help), and the plugin has the model
write the direct speech of the characters who speak it in that language, once a part is final or on request. The text of the story holds the conlang; the English it was
translated from is kept as plugin data and is what every model reads (generation, summaries, annotations, other plugins), through the core's `PartTextRegistry`. A click on a
translated speech shows the English, in the story and in the viewer, which a book can set to show the conlang only or the English only; the exports can print either with the
other in footnotes.

User manual: [Conlang Translator](../../../manual/user/extensions/conlang-translator.md). What is done, what is not, the decisions and the trials on real models: [TODO.md](TODO.md).

Build (the same source tree as the application): `mvn install -DskipTests` in `marginalia/`, then `mvn package` here, and load the JAR in *Admin → Extensions*.

Packages: `model` (language, lexicon, spans), `store` (the library in the user settings, the settings of a book), `translate` (the request, the answer, the checks, the reverse),
`text` (the transformer for the models, the viewer's and the export's text), `diff` (the change set of two languages), `designer` (the model's proposals), `service`
(translation, range runs, designer, library), `commands` (the `conlang-*` commands, thin wrappers of the services), `ui` (screens). The logic is pure and tested without a UI; `screenshots/bin/shoot-conlang.sh` takes the pictures of the manual.
