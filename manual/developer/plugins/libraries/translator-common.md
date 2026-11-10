---
label: translator-common
order: 50
covers:
  - marginalia/libs/translator-common
  - marginalia/plugins/reallang-translator/src/main/java
verified: cec4f0cd1
---

# translator-common

`marginalia/libs/translator-common` is what the **Conlang Translator** and the **Real Language Translator** share, written so that a third plugin can use it: any plugin that
keeps a story in one language and lets the models, the exports and the readers deal with another. Read [Libraries for plugins](index.md) first for how a library is embedded
and what that implies (a copy per plugin, no shared state, parameters instead of constants).

## The idea

A translator plugin stores the **translated** text in the story and keeps the text the model wrote it from, the **source**, beside it:

```mermaid
flowchart LR
    subgraph Story["a part"]
        T["stored text<br/>… “Da ja-se vek.” …"]
        P["AnchoredPassage<br/>(range of the speech)<br/>attributes: Span<br/>source, speaker, hash"]
    end
    T --- P
    P -->|Reverse| M["what a model reads<br/>… “I see the enemy.” …"]
    P -->|SpeechExportContributor| E["an export<br/>translation / source / footnotes"]
    P -->|ReaderText + ReaderView| R["the reader of a published book<br/>click shows the source"]
    P -->|SourcePopover| A["the author's click"]
```

Everything that gives the story to a model reads the source (`PartTextRegistry`, [The text a model reads](../part-text.md)), the export is decided by the author in the
dialog ([The text of an export](../export-text.md)), and a click shows the source. The **translation itself** (the prompt, the answer, what is acceptable) is the plugin's: that is
where an invented language (a lexicon, new words) and a real one (a script, politeness) differ, so it is not in the library.

## What is in it

| Package | Class | What it is for |
|---|---|---|
| `model` | `Span` | What a plugin keeps for one translated passage: the **source**, the speaker, the language, whether the listener understands it, the register, a **reading** (the romaji of a Japanese text, empty for none), the model and time, and a **hash of the translated text** the plugin wrote. `Span.read(attributes)` / `span.write(attributes)` store it as JSON in the attributes of the passage. `isEdited(currentText)` is true when the author changed the translation since: the source is then stale. |
| | `Speakers`, `CharacterNote`, `SpeakersMode` | Who speaks the language in a book (everyone except the listed characters, or only the listed ones), with the aliases, gender and voice of each character. `speaks(name)`, `find(name)`; `mayBeInvolved(names)` says whether a part with these characters may hold speech in the language (`Speakers.namesOf(pointOfView, present)` makes the names), which decides whether a language of a book is asked for. |
| | `UnderstandingLevel`, `UnderstandingSettings` | How much of a language a character follows (`CharacterNote.getLevel()`: `NONE`, `FEW_WORDS`, `FOLLOWS_MOST`, `FLUENT`; null = not said), static for the book, with `key()` / `parse(key)` for the commands; and how a plugin inserts the block into the prompt of the story (`inPrompt`, `depth`), kept in the plugin's book settings and edited in the `SpeakersEditor`. |
| | `BookLanguage`, `LanguageRef` | A language a book has besides the one it names first: its id in the plugin's library and **its own** `Speakers` (a new one starts in the mode `ONLY` with nobody listed); `LanguageRef(id, name)` is how the screens list the languages of a book. |
| | `ReaderMode` | What the reader of a published book sees: `CLICK` (the translation, the source on a click), `TRANSLATED`, `SOURCE`. `id()` / `of(id)` for the setting. |
| | `ExportMode` | What an export holds: `TRANSLATED` (as stored), `SOURCE`, `TRANSLATED_NOTES`, `SOURCE_NOTES` (the other text in footnotes). |
| `translate` | `Reverse` | `Reverse.toModel(stored, located)` turns the translated passages of a stored text back into their source and returns the `ModelText` map, the substitutions, the passages the author edited and the ones that could not be used. |
| | `TranslationPlan` | `TranslationPlan.make(source, accepted)`: the new text of a part with every accepted speech replaced, and the `Planned` passages in it (range, translation, source, speech). An `Accepted` may **carry** the `Span` of an earlier translation (a speech of another language of the book that stays as it is): it is in the new text and `Planned.carried()` gives its data back. `translatedCount()` counts the speeches of this run only. Pure. |
| | `Speech` | One direct speech as the model answered for it (`quote`, `speaker`, `language`, `text`, `understood`, `register`). A convenient shape for the answer; a plugin may use its own and make a `Speech` of it. |
| | `ModelJson` | `ModelJson.object(raw)`: the JSON object in what a model answered (a code fence or a word before it is ignored); null when there is none. |
| | `SelectionRange` | `trim(text, start, end)`: the range the user selected without its white space. |
| `text` | `UnderstandingBlock` | The text that tells the models who does not follow which language: `build(entries, characters)` from the levels of the `Speakers` of the languages of a book (`Entry(languageName, speakers)`); only a level that says something is written; the optional `characters` keeps the names and aliases. Pure. |
| | `SpeechTransformer` | A `PartTextRegistry.Transformer`: `new SpeechTransformer(passageAccess)`. |
| | `SpeechExportContributor`, `ExportText` | An `ExportTextRegistry.Contributor`: `new SpeechExportContributor(passageAccess, session -> mode)`. `ExportText.edits(mode, stored, located)` is the pure part. |
| | `ReaderText` | The markdown of a part for the reader: `render(mode, stored, passages, markClass)` (or a function that gives each passage the mark of its kind); the marks carry `data-translated-passage`. |
| | `SpeechPassageKind` | The `PassageKind` a plugin registers: `new SpeechPassageKind(id, markClass)`; the click opens `SourcePopover`; a search and replace of the book that changes words inside the speech makes the hash again (`textReplaced`), so the speech is not taken for an edited one. |
| `service` | `PassageAccess` | `new PassageAccess(kind)`: the translated passages of parts of **your** kind, located (`of(parts)`, `passagesOf(part)`, `locate(passage)`). It is the `Passages` source of the transformer. |
| | `TranslationWriter` | `new TranslationWriter(kindId)`: **writes** a `TranslationPlan` into a part (`write`), one selected sentence (`writeSelection`) and takes a translation out again (`revert`), with a lock per book, a refusal of a part that changed meanwhile (`ChangedMeanwhile`) and the rule that a passage the author edited stays. Also `readOf(part)` and `overlaps(part, start, end)`. For a book with several languages: `carried(part, read, languageId)` (the speeches of the **other** languages, as `Accepted` entries at their place in the English) with `overlapsAny(carried, start, end)`, and `revert(book, part, read, languageId)` that puts back one language. `editSource(passageUuid, source)` sets the English of one speech by hand and takes the text it has now as the one it belongs to (a speech marked as edited is turned back again); `replaceSource(passageUuid, expected, source)` writes a source a search and replace made, only if it is still `expected` (`SourceReplace` plans and applies it over the active branch). |
| | `UnderstandingPrompt`, `PayloadBlock` | `new UnderstandingPrompt(source, propertyKey, log)` gives the block to the story model (`register(storyGenerationService)`: the tokens are reserved in `BEFORE_PREPARE_CONTENT`, the message inserted in `AFTER_PREPARE_PAYLOAD` at the depth of the book's `UnderstandingSettings`) and, as `factsProvider()`, to the annotation reviewer (`AnnotationFactsRegistry`); `block(book, characters)` is what the plugin's macro command returns. The plugin's `Source` says where the entries and the settings are; `propertyKey` must be the plugin's own. Call `unregister()` and `annotationFacts.unregisterAll(owner)` on unload. `PayloadBlock.insert(payload, text, depth, role)` is the insertion. |
| | `ModelClient` | `new ModelClient(name).choose(aiUuid, protocolUuid, book)` finds the model (yours, else the book's, else the user's defaults), `ask(model, book, system, user)` waits for the whole answer (reasoning chunks are left out). `ModelSelection` is the AI and protocol. |
| | `RangeLoop`, `RangeRuns` | An action over a range of parts, one at a time: `RangeLoop.select` / `run` (pure, with a stop and a summary), `RangeRuns.start` on a worker thread of the session, telling the page the progress. The work is a lambda that gets the language to work on (null: all of the book's); a `RangePart` knows the languages it is translated in. |
| | `PartRuns<R>`, `AsyncRuns` | One job on a worker thread with the user's request scope, the result handed to the page. One run per part. |
| | `TranslatedPassages` | `TranslatedPassages.of(passages, kinds, partId)`: the passages of a part of **every** registered kind that carry a `Span`, whichever plugin made them; `markClass(kinds, passage, fallback)`. For the viewer. |
| | `PluginLog` | The extension's logger for the code that has none (`PluginLog.set(log)` in `onExtensionLoad`). **Per copy**: set it in every plugin. |
| `ui` | `SourcePopover` | The popover over a translated speech: the source, who said it, "not understood", "edited after the translation", and (only where `SpeechPassageKind.onClick` opens it, the author's views, not the viewer) the *Edit the source* button. |
| | `ReaderView` | `ReaderView.apply(view, part, mode, passages, markClass)` in a decorator of `Viewer.createPart`; with a function in place of the class for passages of several kinds. A part is changed once. |
| | `SourceEditDialog` | The window the popover's *Edit the source* button opens: the translation (read only) and the English source in a text area; saving calls `TranslationWriter.editSource`. |
| | `SourceSearchReplaceDialog` | A subclass of the application's `SearchReplaceDialog` over the sources of the translated speeches of the active branch (`SourceReplace`); `SourceSearchReplaceDialog.button(book, kindId)` is the button a plugin puts into its tab of the story. |
| | `RangeDialog` | The window of the actions on a range of parts (translate the new ones, translate again, put the source back), with a progress and a stop: `new RangeDialog(book, languages, loader, rangeRuns, reload)`; with more than one `LanguageRef` it has a field for the language. |
| | `LanguageChoiceDialog`, `BookLanguagesPanel` | `LanguageChoiceDialog.ask(languages, withAll, chosen)` asks which language of the book an action is for (it just runs with one); `BookLanguagesPanel` adds the other languages of a book from the plugin's library and takes them away. |
| | `SpeakersEditor`, `ListEditor<T>`, `EditDialog` | The editor of the speakers; the list editor it is built on (a grid with add, edit, delete, move) and the small dialog with a validation. |
| | `ExportChoice` | `ExportChoice.field(label, labels)`: the select of the export dialog, with the plugin's own texts. |
| | `CommonStyles`, `TranslatorCommonLocalization` | The CSS classes and `translatorcommon.css`; the texts with their Japanese and Chinese translations (`TC.localized(...)`). |

## Building a translator plugin on it

`marginalia/plugins/reallang-translator` is the smaller example (no lexicon, no designer); the Conlang Translator is the larger one. The steps:

**1. The identity of your plugin.** The id of your passage kind and the CSS class of the mark are yours, unique among plugins:

```java
public final class MyKind {
    public static final String ID = "my-translator.speech";   // the id of the kind
    public static final String MARK = "my-passage";           // the CSS class of the mark
}
```

and the rule for the mark in your own CSS (`mark.my-passage { border-bottom: 1px dotted ...; cursor: help; }`).

**2. Register in `onExtensionLoad`:**

```java
PluginLog.set(log);
PassageAccess passages = new PassageAccess(MyKind.ID);

passageKinds.register(new SpeechPassageKind(MyKind.ID, MyKind.MARK));                    // the mark and the click
partTexts.register(owner, new SpeechTransformer(passages));                                // the models read the source
exportTexts.register(owner, new SpeechExportContributor(passages, session -> chosenMode(session)));  // the export
```

`passageKinds` is the core's `PassageKinds`, `partTexts` the `PartTextRegistry`, `exportTexts` the `ExportTextRegistry`; unregister all three in `onExtensionUnload`. `chosenMode`
reads the field you added to the export dialog (below); return `null` or `ExportMode.TRANSLATED` for no change.

**3. The export field.** Decorate `ExportDialog.createExtensionFields` on leave, add `ExportChoice.field(label, mode -> text)` to the list it returns and keep it in a
`WeakHashMap` keyed by the dialog (`instrumented`); `chosenMode(session)` is `map.get(session.source()).getValue()`. Add the field only for a book that has your passages.

**4. The viewer.** Decorate `Viewer.createPart` on leave: when the part has passages of your kind (`passageService.listForParts(ids, MyKind.ID)`), call
`ReaderView.apply(view, part, mode, TranslatedPassages.of(passageService, passageKinds, partId), p -> TranslatedPassages.markClass(passageKinds, p, MyKind.MARK))`; the `mode` is
`ReaderMode.of(...)` of the book's setting for the readers. Passing the passages of every plugin is what makes a book that holds two kinds draw right (see below).

**5. CSS and texts.** `ExtensionHasCss.getCss` returns `CommonStyles.css() + "\n" + MyStyles.css()` for `Main` and `Viewer`. Your own texts are your own
`PluginLocalizationBundle`; the library's screens have theirs.

**6. Translating a part** (yours; the library gives the pieces). The shape both plugins follow:

```java
TranslationWriter writer = new TranslationWriter(MyKind.ID);
Reverse.Result read = writer.readOf(part);                               // the English of a part that is translated already
String source = read.text();                                              // what you send to the model
String raw = new ModelClient("my-translator").ask(model, book, system, user);
// parse the answer (ModelJson.object finds the JSON in it), check it (the quote is in `source`, the language is right...), then
List<TranslationPlan.Accepted> accepted = ...;                            // (speech, start, end) of the ones you accept
TranslationPlan plan = TranslationPlan.make(source, accepted);
writer.write(book, part, plan, read, languageId, modelName,
        (span, planned) -> span.setReading(readingOf(planned.speech())));  // what only you know goes on the Span
```

`write` replaces the text of the part with a version in the history (`ChatMessageService.replaceText`: a text that says what the old one said, [The text a model
reads](../part-text.md#putting-the-other-form-into-the-story)), removes the old passages of the part except the ones the author edited, and makes a passage of every planned speech;
it throws `TranslationWriter.ChangedMeanwhile` when the part is not what you read. `writeSelection` does the same for one sentence the author selected, `revert` puts the source back.
Run it on a worker thread: `PartRuns` for a part, `RangeRuns` (with `RangeLoop`) for a range, `new RangeDialog(...)` for the window of a range.

**7. Who speaks.** Keep a `Speakers` in the attributes of the book (it is Gson-friendly) and give the author `new SpeakersEditor(speakers, onChange)`; put the notes of the
characters in your prompt.

**8. More than one language in a book** (both plugins do it; it needs nothing from the core). The book keeps the language it names first (the inherited setting) with `Speakers`, and a list of
`BookLanguage` for the others. A translation is **one pass per language**: the rules, the lexicon and the speakers of one language, and only its speeches. The passes run one after the other, and a pass must
leave the speeches of the other languages translated, which `write` does when the plan holds them:

```java
Reverse.Result read = writer.readOf(part);                                   // the English of ALL the languages
List<TranslationPlan.Accepted> carried = writer.carried(part, read, languageId);   // the speeches of the other languages
// ... ask the model for this language; drop the accepted speeches that TranslationWriter.overlapsAny(carried, start, end)
accepted.addAll(carried);
TranslationPlan plan = TranslationPlan.make(read.text(), accepted);
if (plan.translatedCount() == 0) { /* nothing was translated in this pass */ }
writer.write(book, part, plan, read, languageId, modelName, extra);          // the carried passages are made again with their own Span
```

The passages tell their language (`Span.language`), so a retranslation or a revert of one language (`writer.revert(book, part, read, languageId)`) leaves the others alone, and
`Speakers.mayBeInvolved(Speakers.namesOf(pov, present))` spares the request for a language none of whose speakers is in the part. The menus ask the language with `LanguageChoiceDialog`, the range
dialog has a field for it, and the commands take an optional `language`.

## The contract a passage depends on

A passage is used **only when the text at its range still has the hash it was made for** (`Span.isEdited`). Every consumer in the library checks it (the reverse, the export,
the popover). It is what keeps the author's edits safe (an edited speech has no trustworthy source and is left as it is) and what lets the passage of another plugin, whose
range an earlier edit shifted, drop out instead of corrupting text. If you write a consumer of your own passages, apply the same check; `Reverse`, `ExportText` and
`SourcePopover` have tests that pin it.

## Two plugins in one book

Each plugin has its own passage kind, so the passages never mix. Models read the source of both (`PartTextRegistry` merges the substitutions of every transformer on
the stored text); the export puts both plugins' edits together once ([The text of an export](../export-text.md)). The published book's viewer draws a part **once**, from the
passages of every plugin (`TranslatedPassages`), each with the mark of its own kind: the first plugin whose decorator reaches the part does it for both (`ReaderView` leaves a flag on
the part, a string key, so the copy of the class in the other bundle sees it) and the second finds it done. The consequence for a book with both is that **one reader mode rules
the page** (the one of the plugin that decorates first); the author sets the same in both. A click finds its passage in the list that was drawn, whichever plugin it is.

A translator does not read the translated speech of the *other* plugin as its source: the stored text it sends to its model has the other plugin's translation in it (only its own
passages are reversed), so the model sees that speech in the other language and, being told to translate the English, leaves it alone.

## Tests

The library has plain unit tests (`mvn test` in `marginalia/libs/translator-common`): `ReverseTest`, `TranslationPlanTest`, `SelectionRangeTest`, `SpanTest`, `SpeakersTest`,
`SpeechTransformerTest`, `ExportTextTest` (through the core registry), `ReaderTextTest`, `RangeLoopTest`, `CarriedSpeechesTest` (the speeches of the other languages kept through a write and a revert), `TranslationWriter` has no other unit test here (it needs the services; the plugins' tests of the translation go through it), `ModelJsonTest`, `SourcePopoverTest` (a Vaadin component built without a browser: the
library's test dependencies include the servlet API for that) and `TranslatorCommonLocalizationTest` (every text has its Japanese and Chinese translation, with the same
`%s` / `%d` placeholders). A plugin that adds texts to its own bundle has its own such test (`ConlangTranslatorLocalizationTest` is the template).
