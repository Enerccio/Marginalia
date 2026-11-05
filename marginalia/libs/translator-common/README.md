# translator-common

What the [Conlang Translator](../../plugins/conlang-translator/README.md) and the [Real Language Translator](../../plugins/reallang-translator/README.md) share. A plain Maven jar that each plugin
**embeds** in its bundle (`<Embed-Dependency>translator-common;inline=true</Embed-Dependency>`); it is not an extension and is never put into the extensions folder. Plugins do not see each
other's classes, so this is shared source and not shared runtime: nothing in it may be state that two plugins would have to share (each copy has its own statics), and what has to differ is
a parameter (the passage kind id, the mark class, the name of the requests, the texts of the export field).

Packages: `model` (the data of a translated speech `Span`, the speakers, the reader and export modes), `translate` (`Reverse`: the translated text back to the English the models read;
`TranslationPlan`; `SelectionRange`), `text` (the transformer for `PartTextRegistry`, the text of the export and of the reader, the passage kind), `service` (the run loops, the model client, the
access to the passages, `TranslationWriter` that writes a translation into a part and takes it out, `TranslatedPassages` for the viewer), `ui` (the popover over a speech, the reader's part, the speakers' editor, the list editor, the export field; `CommonStyles` with `translatorcommon.css`, and
`TranslatorCommonLocalization` with the Japanese and Chinese texts; the range window `RangeDialog`). A passage is used only when the text at its range still has the hash it was made for (`Span.isEdited`): that contract
is what lets a second plugin's passages drop out quietly where the first one changed the text, and it has a test.

Build: `mvn install -DskipTests` here (a plugin needs it in the local repository first). `install-plugins.sh`, `marginalia/plugins/Dockerfile` and the workflows do it.
