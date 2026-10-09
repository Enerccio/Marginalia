---
label: Generation events
order: 65
---

# Generation events

`@Extendable` hooks change what the user sees; **generation events** change what the model sees. A plugin registers
listeners with `StoryGenerationService.addEventListener(...)` and the generation calls them at fixed points of the
pipeline - with the lore, the summaries, the prompt data, the payload and the streamed answer at hand, all of which
the listener may read and replace.

The full list of events, what each one makes available and the rules for listeners are in
[Generation pipeline → Events for extensions](../generation-pipeline.md#events-for-extensions). This page shows how
to use them on the bundled
[Author's Note](https://github.com/Enerccio/Marginalia/tree/master/marginalia/plugins/authorsnote) plugin, which
inserts a per-book note into the prompt of every generation.

## Author's Note

The plugin has three parts:

| Part | Class | Mechanism |
|---|---|---|
| The note, stored with the book | `AuthorsNoteData` in `Manuscript.attributes`, under the package name | [Extended attributes](extended-attributes.md) |
| An *Author's Note* tab next to the story outline | `AuthorsNoteView`, added on leave of `ManuscriptStoryPart.renderStoryContent` | [@Extendable hooks](extendable.md), [A tab next to the story](ui-extensions.md#a-tab-next-to-the-story) |
| Inserting the note into the prompt | a listener on `Events.AFTER_PREPARE_PAYLOAD` | generation events (this page) |

The data is a plain class serialized with Gson:

```java
public class AuthorsNoteData {
    public static final String KEY = "com.github.enerccio.marginalia.extensions.authorsnote";

    private boolean enabled = false;        // insert the note at all
    private String note = "";               // sent to the model
    private String privateNote = "";        // for the author only, never sent
    private int depth = 1;                  // number of messages after the note
    private LLMRole role = LLMRole.SYSTEM;  // role of the inserted message
    ...
}
```

### Registering the listener

The listener is registered in `onExtensionLoad`, next to the decorator, and kept so it can be removed on unload.
`StoryGenerationService` is injected into the `@Configurable` extension:

```java
@Configurable
public class AuthorsNoteExtension implements MarginaliaExtension {

    @Autowired
    private StoryGenerationService storyGenerationService;

    private Registration payloadRegistration;

    @Override
    public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
        ...
        // the payload is complete after PREPARE_PAYLOAD, what the listener sets is sent to the model
        payloadRegistration = storyGenerationService.addEventListener(Events.AFTER_PREPARE_PAYLOAD,
                this::insertAuthorsNote);
    }

    @Override
    public void onExtensionUnload(Bundle b, OsgiServiceImpl osgiService, ExtensionService extensionService) {
        ...
        if (payloadRegistration != null) {
            payloadRegistration.unregister();
            payloadRegistration = null;
        }
    }
}
```

### Choosing the event

The note has to end up among the chat messages sent to the model, so the plugin listens to `AFTER_PREPARE_PAYLOAD`:
at that point `PreparePayloadStep` has built the final list of `LLMChatMessage`s and whatever
`event.setPayload(...)` sets is what inference sends. The payload looks like this:

| # | Role | Content |
|---|---|---|
| 0 | `SYSTEM` | jailbreak + system prompt (template, lore, summaries) |
| 1, 2 | `USER`, `ASSISTANT` | `[ Generate story. ]`, story part 1 |
| … | `USER`, `ASSISTANT` | `[ Generate more story. ]`, story part *n* |
| last | `USER` | the rendered user prompt with the instructions for this turn |

Other events would be the wrong place: before `AFTER_PREPARE_PAYLOAD` there is no payload (a listener could only
change the system prompt or the story text in `GenerationProperties`), after `BEFORE_INFERENCE` the request is
already on its way.

### Changing the payload

```java
private void insertAuthorsNote(GenerationControllerEvent event, EventChain chain) {
    try {
        // listeners are global - every generation of every user comes here, the note is per book
        Manuscript manuscript = event.getManuscript();
        if (manuscript != null) {
            AuthorsNoteData data = authorsNoteService.loadCurrent(manuscript);
            event.setPayload(authorsNoteService.insertNote(event.getPayload(), data));
        }
    } catch (Exception e) {
        // generate without the note rather than fail the generation
        log.warn("Failed to insert the author's note: {}", e.getMessage(), e);
    } finally {
        chain.next();
    }
}
```

and in `AuthorsNoteService`:

```java
public List<LLMChatMessage> insertNote(List<LLMChatMessage> payload, AuthorsNoteData data) {
    if (payload == null || !data.isEnabled() || StringUtils.isBlank(data.getNote())) {
        return payload;
    }
    int min = !payload.isEmpty() && payload.getFirst().getRole() == LLMRole.SYSTEM ? 1 : 0;
    int index = Math.clamp(payload.size() - Math.max(0, data.getDepth()), min, payload.size());

    List<LLMChatMessage> result = new ArrayList<>(payload);
    result.add(index, LLMChatMessage.of(data.getRole(), data.getNote().trim()));
    return result;
}
```

Points worth copying:

- **`chain.next()` in `finally`.** The generation waits until every listener calls it; a listener that throws before
  calling it would leave the generation hanging.
- **Listeners are global.** The same listener gets the events of every generation of every user. The plugin acts only
  on data of the book being generated (`event.getManuscript()`), which belongs to the user who started it.
- **Reload what the user may have changed.** `event.getManuscript()` is the book as it was when the generation started.
  The note is edited in the sidebar and saved right away, so `loadCurrent` reads it again with
  `ManuscriptService.find(...)` - services work in listeners, they run in the user's request scope.
- **Replace, don't mutate.** The listener builds a new list and calls `event.setPayload(...)`. Another listener may
  have set an immutable list, and the new list makes it obvious what the plugin sends.
- **Don't break the generation.** A failure is logged and the story is generated without the note.
- **Keep the system prompt first.** Some providers accept a system message only at the start, so the depth is clamped
  to keep the note after it.

!!!warning Token budget
The context budget is computed in step 4 (`PrepareContentStep`), before the payload exists: it doesn't know about
messages a listener adds later. The budget keeps a reserve of a few hundred tokens, a short note fits into it; a plugin that adds a lot of text
should make room for it earlier, for example by dropping story parts from `MANUSCRIPT_CHRONICLE` in
`AFTER_MANUSCRIPT_CONCATENATION`.
!!!

### Other things listeners can do

| Goal | Event | How |
|---|---|---|
| Add text to the system prompt | `AFTER_PREPARE_CONTENT` | change `PrePromptData.systemPrompt` |
| Hide or add lore | `PROCESS_ACTIVATED_ENTRIES` | modify the `ACTIVATED_LOREBOOK_ENTRIES` list |
| Shorten the story sent to the model | `AFTER_MANUSCRIPT_CONCATENATION` | replace `MANUSCRIPT_CHRONICLE` |
| Change the instructions of the turn | `AFTER_PREPARE_CONSTANT_DATA` | change the processed user prompt in `PrePromptData` |
| Post-process the answer as it streams | `CHUNK_RECEIVED` | replace the `CHUNK` property |
| Act on the finished part | `AFTER_INFERENCE` | read `event.getMessage()` |

`GenerationProperties` documents each property and when it is available.
