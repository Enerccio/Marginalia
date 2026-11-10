---
label: Text a model reads
order: 67
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/extensions/PartTextRegistry.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/components/ModelText.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/StoryAround.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/RedraftServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/AnnotateServiceImpl.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/domain/service/impl/ChatMessageServiceImpl.java
verified: 5b5451f5d
---

# The text of a part as a model reads it

The text of a part is `ChatMessage.getResponse()`. An extension may want to **store the story in one form and give the model another**:
the Conlang Translator keeps the speech of the characters in a constructed language and lets the model read the English it was made from;
a redaction plugin could hide names. `PartTextRegistry` is the one place where that is said. What the *export* holds is its own registry, [The text of an export](export-text.md).

## Who asks

Everything that gives the text of a part **to a model** takes it from the registry (`@Autowired PartTextRegistry`):

| Call | Returns |
|---|---|
| `forModel(part)` / `forModel(parts)` | the text the model reads; the batch is in the order given |
| `modelText(part)` / `modelTexts(parts)` | a `ModelText`: `text()`, `stored()`, `changed()` and the offset maps |
| `isActive()` | whether any transformer is registered (a place that would pay for the map can skip it) |

In the application: the prompt of a generation (`PrepareContentStep`, `GenerationStepBase`: the story, `{{current-message}}`,
`closest-message`), the lorebook scan (`LorebookActivationService.recentParts`), the story around a part (`StoryAround`: annotate, redraft,
impersonate) and the input of the summarizer (`SummaryServiceImpl`). The code that works on the **stored** text keeps `getResponse()`: the
editor, the history, the offsets of the annotations, the **hash of a summary** (`SummaryService.BlockTexts.CURRENT`), the search and replace, the
export, the backups and, for now, the read and write commands of an agent. `StoredTextReadersTest` lists those files in
`src/test/resources/stored-text-readers.txt`; a file that starts reading `getResponse()` fails the test until someone decides which kind it is,
and the answer for a file that talks to a model is the registry.

Token counts: `ChatMessage.getTokenCount()` is what the context budget uses, so a recount after an edit counts **what the model reads**
(`ChatMessageService.replaceText` takes it as `modelText`); word counts stay on the stored text.

## Registering a transformer

```java
@Autowired
private PartTextRegistry partTexts;

public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    partTexts.register(bundle.getSymbolicName(), new PartTextRegistry.Transformer() {
        @Override
        public void prepare(Collection<ChatMessage> parts) {
            // called once with every part about to be read: load the data of the whole branch here, not per part
        }

        @Override
        public List<ModelText.Substitution> substitutions(ChatMessage part) {
            // ranges of part.getResponse() and what the model reads in their place; empty for none
            return List.of(new ModelText.Substitution(start, end, "I see the enemy."));
        }
    });
}

public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
    partTexts.unregisterAll(b.getSymbolicName());
}
```

- Substitutions are ranges of the **stored** text. Several transformers are merged: a range that overlaps an earlier one (registration order) is
  ignored; ranges outside the text are ignored.
- A transformer that throws does not break the request: it is logged and the part is read as stored.
- The registry is one bean for the whole JVM: **tests that register a transformer must unregister it** (`@AfterEach`).

## The offset map

Two features talk to a model in the offsets of the stored text, so `ModelText` maps both ways:

- **Annotate:** the model quotes the text it was shown; `AnnotateServiceImpl` finds the quote in `text()` and turns its range into the stored
  range with `storedRange(start, end)`. A quote the model took from inside a replaced passage becomes the whole passage.
- **Redraft of a selection:** the author selects in the stored text. `RedraftService.widen(part, passage)` grows the selection to whole replaced
  passages (the dialog calls it), and the prompt shows the model that range through `modelRange`.

A range that touches a replaced passage grows to the whole passage on the other side (the inside of a replaced passage has no counterpart); text
outside the replaced ranges maps one to one; a range that starts at a place where one text has a passage and the other has none (a deletion)
begins with what follows it.

## Putting the other form into the story

`ChatMessageService.replaceText(manuscript, part, text, modelText, generatedText, origin)` is the edit for a text that says the same in other
words: the tokens are those of `modelText`, `generatedText` becomes `originalResponse` (the authorship of a version with the detail `TRANSLATION` is worked out against it: what differs from it is what
the author changed, see [Who wrote the words](../version-history.md#who-wrote-the-words)), the summaries that were valid are revalidated (the outdated stay outdated), the history gets a version, and
the annotations move (`AnnotationService.shift`). Use `RevisionOrigin.automatic(RevisionDetail.TRANSLATION, false)` or your own origin.
