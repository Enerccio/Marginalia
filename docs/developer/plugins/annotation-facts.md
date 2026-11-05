# Facts for the annotation

When a model [annotates a part](../../user/books/annotations.md#letting-a-model-annotate) it reviews the part against the story before
it, the story after it and the lore. An extension that **knows more about the story** than those texts say (what each character knows, a
timeline, an inventory) can hand that knowledge to the reviewer, so it can mark a continuity error that no text shows: a character who
uses a name they never heard. `AnnotationFactsRegistry` is the one place where that is said. The facts only reach the *annotate* prompt;
generation, redraft and the other prompts are not touched (the Knowledge Tracker has its own way into the prompt of a generation, see
its page).

## Registering a provider

```java
@Autowired
private AnnotationFactsRegistry annotationFacts;

public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
    annotationFacts.register(bundle.getSymbolicName(), (book, part, branch) ->
            List.of(new AnnotationFactsRegistry.Section("What the guards have been told:", "The gate closes at dusk.")));
}

public void onExtensionUnload(Bundle b, OsgiService parentService, ExtensionService extensionService) {
    annotationFacts.unregisterAll(b.getSymbolicName());
}
```

| Part of the call | |
|---|---|
| `book`, `part` | The book and the part that is reviewed. |
| `branch` | The parts from the start of the story to `part`, in order, `part` last: the branch the author reads, not the active leaf of the book. |
| Result | `Section(title, text)` blocks, or an empty list / `null`. A block with an empty text is dropped; the title may be blank. Several providers are joined in registration order. |

`AnnotateServiceImpl.createPayload` calls `facts(book, part, branch)` for every annotation (also the batch, the automatic annotation and
the command `/generate-annotation`) and sets the result as the variable `facts` of the annotate prompt. It is rendered before the token
estimate, so the facts count in the context budget like the lore: a part that no longer fits with them fails with the usual *context
insufficient*.

## What a provider must get right

- **Answer for the story at the part, not for the end of the book.** The model reviews an early part; a fact that becomes true ten parts
  later would make a correct passage look wrong. Say what is established *before* the part, and what the part itself brings
  **separately** (so the first half of the part is not checked against what the second half teaches).
- **Say what the facts are not.** The default prompt tells the model that a fact that is not listed is not a fact and that the records are
  silent about everything else. Do not write "nobody else knows", unless your data is complete.
- **Be cheap and safe.** The call is made on the thread that starts the annotation, inside its read-only transaction. A provider that
  throws is logged and left out (`AnnotationFactsRegistry` catches it), the annotation goes on.
- Return **nothing** when you have nothing for the part; the block and the directive of the prompt are printed only when `facts` is not
  empty.
- The registry is one bean for the whole JVM: **tests that register a provider must unregister it** (`@AfterEach`).

## The prompt

`AnnotateTemplateData.facts` is the variable. The built-in annotate prompt prints it between `=== ESTABLISHED FACTS START ===` and
`=== ESTABLISHED FACTS END ===`, and the system prompt has the fifth directive (*Established facts*: a passage that contradicts a fact or
makes a character act on what they cannot know is a continuity error; name the fact; do not flag what the records are silent about). A
prompt the author wrote has to use `{{facts}}` itself, as for `annotations` or `outputFormat`.

## The Knowledge Tracker

The [Knowledge Tracker](../../user/extensions/knowledge-tracker.md) is the first provider (`KnowledgeFactsProvider`, switched by the book
setting `factsForAnnotation`, on by default). Its state is the state of the *end* of the processed story, not a series of snapshots, so
`KnowledgeFactsBuilder` rebuilds the state at the reviewed part from the messages the items remember:

| Item | At the start of the reviewed part |
|---|---|
| Initial or manual | A fact. |
| Learned in an earlier part (position on the branch before the part) | A fact. |
| Learned in the part | Listed apart: *learns in this part*. |
| Learned later, or in a part that is not on this branch | Not a fact. |
| Retracted by a later part (or one off the branch) | Still a fact. |
| Retracted by the part | Listed apart: *until this part, which changes it*. |
| Retracted by an earlier part, retracted by hand, tidied, declined | Not a fact. |

The people met are read the same way (`MetPerson.sourceMessage`). The characters described are the tracked ones that are in the *Present
Characters* field of the part, are its point of view, or are named in its text (whole word, any case, aliases too; names in Chinese and
Japanese are found inside the text). When a part before the reviewed one was never processed, the facts end with a line that says so,
because what the characters learned there is missing.
