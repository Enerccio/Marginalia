# Anchored passages

An extension that needs to attach something to a **passage of the text** of a part (the Conlang Translator keeps the English source of a
translated speech; a glossary plugin could keep a hint) does not have to follow the text itself. The core already does that for the
annotations of the author, and since 1.7.0 it does it for any kind of passage: the offsets move through the author's edits, the passage is found
again by its quote when the part is generated again, goes with a merge, is copied to a branch, goes to the trash and to backups.

A passage of an extension is a row of `AnchoredPassage` (the table of the annotations, see [the model](../domain-model.md#anchoredpassage)) with a
`passageKind`, the id of your kind, and your data in `getAttributes()`:

```java
PassageService passages;   // @Autowired
AnchoredPassage passage = passages.createFromQuote(book, part, "my-plugin.gloss", quote, before, after);
passage.getAttributes().addProperty("source", "Hello there");   // your data, under your own keys
passages.save(passage);
```

- `create(book, part, kind, start, end)` (offsets of the stored text, white space at the edges is left out) or `createFromQuote` (a quote that is not
  found makes a passage of the whole part with `isNotLocated()`); `listForPart` / `listForManuscript` (a kind, or `null` for all the kinds of the
  extensions); `reattach`, `remove`.
- The range is `getStart()` / `getEnd()` of the **stored** text of the part, which is also what the author edits; the text a model reads may be
  another one, see [The text a model reads](part-text.md) (`ModelText` maps the ranges).
- `listForParts(partIds, kind)` reads the passages of many parts with one query and no manuscript, for code that holds the parts already (the text a model
  reads: a story of hundreds of parts must not cost a query per part).
- A passage whose text is gone is `isOrphaned()` with no range; it is found again if the text comes back.
- Passages are **not** annotations: the badge of the part, the annotations tab and the margin list only the editorial notes, and the commands of
  the annotations see only those. Nothing to resolve or dismiss.

## The kind

Register a `PassageKind` in `PassageKinds` when the extension loads and unregister it when it unloads. The passages of a kind that is not
registered stay in the data and are not shown.

| Method | |
|---|---|
| `id()` | The id stored with every passage; start it with the name of the extension. |
| `markClass(passage)` | CSS classes of the mark over the passage in the story card (desktop and phone) and in the redraft dialog; `null` is no mark. Your stylesheet comes with `ExtensionHasCss`, or use the shared `annotation-mark` classes. |
| `markAttributes(passage)` | Further attributes of the mark as HTML (` data-x="y"`, values escaped). The core adds `data-annotation="<uuid>"`, which is how the click finds the passage. |
| `onClick(passage, anchor)` | The author clicked the mark; open a popover or a dialog from `anchor`. |
| `shownInViewer()`, `shownInExports()` | Whether the viewer of a published book and the exports get the passages of the kind. They only ask the kind: the core draws nothing of its own there, the extension's hooks do: for the exports the extension registers a contributor in the [`ExportTextRegistry`](export-text.md); the viewer click is the extension's decorator of `Viewer.createPart` (the [translator library](libraries/translator-common.md) has one). |

`StoryAnnotations` (the annotations a story tab keeps) loads the passages of the registered kinds once for the book, only while some kind is registered,
and puts their marks into the text together with the marks of the annotations.

Tests: `PassageServiceTest`, `StoryAnnotationsTest`, `FlywayMigrationTest.upgradesV25AnnotationsThroughTheV26TableRebuild`.
