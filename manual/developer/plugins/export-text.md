---
label: The text of an export
order: 68
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/extensions/ExportTextRegistry.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/dialogs/ExportDialog.java
  - marginalia/src/test/java/com/github/enerccio/marginalia/extensions/ExportTextRegistryTest.java
verified: 271b964f0
---

# The text of an export

The text of a part is `ChatMessage.getResponse()`. An extension that keeps the story in one form may want the **export** to say it in another: the Conlang
Translator stores the speech of the characters in an invented language, and lets the author export the English instead, or either with the other in
footnotes. `ExportTextRegistry` is the one place where that is said. It is the export's counterpart of [`PartTextRegistry`](part-text.md) (which is what a
*model* reads); the two are independent, an extension may use one, the other, or both.

## Why a registry

The first version of the Conlang Translator decorated `ExportDialog.export` and replaced the `messages` argument with copies of the parts. That works for
one plugin and breaks for two: an edit is a **range of the stored text**, and the second plugin that changes the parts reads its ranges against text the first
one already changed (its passages then point at the wrong characters, and, worse, two plugins number their footnotes from `[^1]`). With the registry every
contributor answers for the stored text, the registry puts all the answers together once, and the footnotes of the whole export are numbered in one sequence.
A book can then hold passages of several plugins (an invented language and a real one) and export all of them.

## Registering a contributor

```java
@Autowired
private ExportTextRegistry exportTexts;

public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    exportTexts.register(bundle.getSymbolicName(), new ExportTextRegistry.Contributor() {
        @Override
        public void prepare(Collection<ChatMessage> parts, ExportTextRegistry.Session session) {
            // called once with every part of the export: load the data of the whole export here, not per part
        }

        @Override
        public List<ExportTextRegistry.Edit> edits(ChatMessage part, ExportTextRegistry.Session session) {
            // ranges of part.getResponse() and what the export has in their place; empty for none
            return List.of(ExportTextRegistry.Edit.withNote(start, end, "I see the enemy.", "Mara: Da ja-se vek."));
        }
    });
}

public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
    exportTexts.unregisterAll(b.getSymbolicName());
}
```

An `Edit` is `(start, end, replacement, note, markAt)`; build it with `Edit.replace(start, end, text)` or `Edit.withNote(start, end, text, note[, markAt])`:

| Field | Meaning |
|---|---|
| `start`, `end` | The range of the **stored** text that is replaced (`end` exclusive; `start == end` inserts). |
| `replacement` | What the export has in its place; empty removes the range. |
| `note` | The text of a footnote, `null` for none. The registry puts it on one line (a line break would end the definition). |
| `markAt` | Where the footnote mark goes in the stored text, `end` or later: the text between `end` and `markAt` is kept and the mark follows it. It puts the mark behind the closing quotation mark of a speech instead of into it. Ignored without a note. |

The footnotes are Markdown footnotes (`[^3]` in the text, `[^3]: the note` after the part): every exporter parses them (HTML and EPUB link them, plain text
writes them as endnotes after the part, Word and PDF as footnotes at the foot of the page, see [Footnotes of the export](../../user/books/export.md#footnotes)).

## What the registry does

`apply(parts, session)` is called by the export dialog (`ExportDialog.applyExportTexts`, after the parts of the branch were collected and before the exporter
runs):

- every contributor is `prepare`d with all the parts, then asked for the edits of each part, in registration order;
- the edits of a part are sorted by their start; **an edit that overlaps an earlier one is dropped** (of two that start together, the one registered first wins),
  and so is one outside the text, so a contributor that answers with a stale range damages nothing;
- a part with edits is a **copy** (`ChatMessage.copyOf`) with the new text; the stored part is never changed. A part with no edit is returned as it is;
- footnotes are numbered `[^1]`, `[^2]`... over the **whole export**, in the order of the parts, and each part ends with the definitions of its own notes;
- a contributor that throws is logged and left out, the others work.

With none registered the parts are returned as they are (`isActive()` says whether any is).

## The session and the choices of the author

`Session` tells the contributor what the export is about: `source()` is the export dialog and `manuscript()` the book. An extension that lets the author choose
what the export holds adds a field to the dialog (a decorator of `ExportDialog.createExtensionFields` that adds a component to the list it returns), keeps it in a
map **keyed by the dialog** (`WeakHashMap`), and reads it in `edits` through `session.source()`. A contributor that finds no field for its dialog changes nothing.
Registered once for the whole JVM: the registry is one bean, **tests that register a contributor must unregister it**.

## A plugin passage that is no longer what it was made for

A contributor that works from [anchored passages](anchored-passages.md) must use a passage only when the text at its range is still what the plugin wrote
there (the Conlang Translator keeps a hash of it, `Span.isEdited`). It is the check that makes the author's edits safe, and the one that lets a passage whose
range another plugin's edit shifted drop out quietly. The library [`translator-common`](libraries/translator-common.md) does it for you
(`SpeechExportContributor`).

## Tests

`ExportTextRegistryTest` (plain unit tests, no Spring): the replacement, the mark behind the closing quote, overlaps, two contributors on the same stored text with
footnotes numbered across parts, a failing contributor, `prepare`. `StoredTextReadersTest` lists `ExportTextRegistry.java` among the files that read the stored
text (`stored-text-readers.txt`).
