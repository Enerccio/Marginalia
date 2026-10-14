# Services

The service layer holds Marginalia's logic: everything the UI does to data goes through a service, and services are
the main API extensions use. This page describes how services and repositories are built, how transactions and the
current user work, and what each service is for. The generation engine has its own page,
[Generation pipeline](generation-pipeline.md).

## Layers

```mermaid
flowchart LR
    UI["UI / extensions<br/>(@Configurable, @Autowired)"] --> S["Service interface<br/>domain/service"]
    S -.-> P["Spring CGLIB proxy<br/>transactions"]
    P --> I["Service implementation<br/>domain/service/impl"]
    I --> R["Repository<br/>domain/repository/impl/Jpa*"]
    R --> EM["EntityManager<br/>(shared, transaction-bound)"]
    I --> O["other services"]
```

- **Interfaces** live in `domain/service/` (and `domain/security/service/`), implementations in `.../impl/`. Code
  always depends on the interface.
- **Beans** are declared in `META-INF/spring/services-config.xml`. Each entity service gets its repository injected
  through the `repository` property; everything else is `@Autowired` inside the implementation.
- **Repositories** (`domain/repository/impl/Jpa*Repository`) contain the JPQL and native SQL. They are called only by
  their service.

## The generic service hierarchy

Entity services extend a generic base that matches the entity's base class:

| Service base | For | Adds |
|---|---|---|
| `BaseService` / `BaseServiceImpl` | `BaseEntity` (`User`) | `find(id)`, `find(entity)` (reload), `find(uuid)` → id, `findAll()`, `findAllIds()`, `save`, `saveWithoutEvent`, `delete(entity, hard)`, `evict` |
| `OwnedService` / `OwnedServiceImpl` | `OwnedEntity` (`Resource`) | Owner-aware finders: `findAllForUser()`, `findAllIdsForUser()`, `findForUser(uuid)`, `findAll(user)`; `save` sets the owner of new entities to the current user. |
| `ExtendableService` / `ExtendableServiceImpl` | `ExtendableEntity` (everything else) | Nothing new - the repository serializes [extended attributes](domain-model.md#extendableentity---extended-attributes) on `save`. |

The same three levels exist for repositories: `JpaBaseRepository` → `JpaOwnedRepository` →
`JpaExtendableRepository`. A repository only has to name its entity class (`getEntityClass()`) to get the generic
queries; it can override `hydrate(entity)` to post-process loaded entities.

Things to know about the generic methods:

- **`find(id)` doesn't check owner or `deleted`.** Use it for ids that come from the user's own data (a book's
  `ai`, a part's `parent`). For anything that comes from outside - a URL, an uploaded file, a backup - use
  `findForUser(uuid)`, which returns `null` for other users' and deleted entities.
- **`find(entity)` reloads.** Entities held by the UI are detached and may be stale; services usually start with
  `Manuscript m = find(manuscript)` before changing it.
- **`findAll*` skip deleted rows**, `find(id)` doesn't.
- **`save` returns the managed copy** (`merge`). Keep using the returned object.
- **`save` vs. `saveWithoutEvent`** - `save` serializes the extended attributes into `extendedContent`;
  `saveWithoutEvent` stores `extendedContent` as it is (used when it was copied from a backup). Both keep the `_fulltext` column
  in step with the `@Fulltextable` fields, see [Full-text column](domain-model.md#full-text-column).
- **`delete(entity, false)`** soft deletes (and, in the services of entities that own other rows - manuscript, message,
  lorebook, entry, AI, tag - the owned rows with it), **`delete(entity, true)`** removes the row. See
  [Soft delete](domain-model.md#baseentity---identity-and-soft-delete).

## Transactions

Transactions are declared with three meta-annotations from `domain/traits/` and applied by Spring's
`tx:annotation-driven` with **class-based (CGLIB) proxies** around each service bean (`proxy-target-class="true"`):

| Annotation | Meaning | Typical use |
|---|---|---|
| `@CommonTx` | `@Transactional("common")`, read/write | Methods that change data. |
| `@CommonTxReadOnly` | Read-only transaction | Finders, searches, reports. |
| `@NoTx` | `Propagation.NOT_SUPPORTED` - suspends any transaction | Methods that start background work or do I/O that must not run in a transaction: `generateNextTurn`, database backups (`VACUUM INTO`), file operations. |

The rules that follow from proxies:

- **Only calls through a bean reference are transactional.** A service calling its own method (`this.save(...)`)
  bypasses the proxy, so that method's annotation has no effect. When a service needs its own transactional method
  from a callback, it injects itself - `SummaryServiceImpl` has `@Autowired private SummaryService self` and calls
  `self.save(summary)` from the streaming callback.
- **Transactions are short.** SQLite allows one writer at a time (see
  [SQLite limitations](database.md#sqlite-limitations)); a long write transaction blocks every other user's writes.
  Never call a model, wait for the UI or sleep inside a transaction. Streaming code saves through the service for each
  change, so each save is its own transaction.
- **A transaction ends with the outermost annotated call.** Entities returned to the UI are detached. Lazy
  associations (`ChatMessage.parent`, `ChatMessage.parentScript`, `LorebookEntry.lorebook`) can't be navigated after
  that - reload them through the service (`chatMessageService.getParent(node)`, `find(...)`).
- Methods without an annotation run in the caller's transaction, or without one.

## The current user

Services are singletons, but the user differs per browser session. The session-scoped bean `user`
(`domain.security.model.User` behind a scoped proxy, defined in `container-config.xml`) is injected into services as

```java
@Autowired
protected User currentUser;
```

Each call on `currentUser` is resolved against the HTTP session of the current thread. It holds a copy of the
logged-in user's `id`, `login` and `fullName` - load the entity (`userService.find(currentUser.getId())`) for
anything else, such as `isAdmin()`.

Services that depend on the current user:

- `OwnedServiceImpl` - `findAllForUser`, `findForUser`, and the owner of new entities,
- `SettingService.getOrCreate(UserSetting.class)` - the user's settings,
- `ManuscriptService` - searches, `findViewable`, `markOpened`, and the prompt defaults (`getMasterTemplate`... fall
  back to the *current* user's `UserSetting`),
- administrator checks: `AdminGuard.requireAdmin()` (see [Administration](#administration)).

**On background threads** there is no request, so resolving `currentUser` fails with *No thread-bound request
found*. Capture the request attributes on the UI thread and enter them on the worker:

```java
ThreadCopyRequestAttributes attributes = ThreadCopyRequestAttributes.create();   // on the UI thread

executor.submit(() -> {
    try (InRequestScope _ = new InRequestScope(attributes)) {                    // on the worker
        lorebookService.findAllForUser();                                         // current user resolves
    }
});
```

The generation engine, `SummaryServiceImpl` and `ThreadAccessDialog` do this. Scheduled jobs (database backups) run
without any user and only use installation-wide data (`getOrCreateApp`).

## Services by area

### Books and the story

| Service | Responsible for |
|---|---|
| `ManuscriptService` | Books: search and sort (`searchManuscripts` → ids), `findViewable(uuid)` (own or published, for the viewer), `markOpened`, the effective prompts of a book (`getMasterTemplate`, `getPov`, `getTense`, `getStyle`, `getUserPrompt`, `getSummaryPrompt`, `getMetaSummaryPrompt`: book value → user default → `Defaults`), the effective backup strategy, and the JSON form of a book used by backups (`createBackup`, `cloneFromBackup`, `restoreBackup`); `delete(book, false)` also soft deletes its parts, their summaries and its tags. |
| `ChatMessageService` | The story tree: `createRoot`, `addChild`, `getBranchFromLeaf`, `getSwipesForMessage` (siblings), `swipeTo` (sets the book's active leaf to the deepest last descendant - the caller saves the book), `branch` (copy as sibling), `deleteNodeAndMigrateChildren`, word and token sums, `countWords`. `delete(part, false)` soft deletes the part with its summary and everything below it, `softDeleteForManuscript(book)` all parts and summaries of a book with one bulk update (the book's `delete` uses it). `searchFulltext(book, query)` finds parts of all branches by their `_fulltext` (see [Full-text search](domain-model.md#full-text-search)); `findLeafFor(book, part)` picks the end of the branch that shows a part. |
| `SummaryService` | `createSummary(book, part, callback)` streams a summary from the model into a new `Summary` and attaches it to the part when finished; returns a `CancellationToken`. The streaming starts after the transaction that inserts the `Summary` commits (`TransactionSynchronization.afterCommit`), because the callback thread saves the row as soon as the model answers. `createMetaSummary(book, from, to, callback)` does the same for a meta summary: it merges the summaries in use between the parts `from` (newest) and `to` (oldest, rounded to the end of its block), puts the result on `from` (`attachMetaSummary`) and returns `null` when there is nothing to merge. `removeSummary(part, unwind)` deletes the summary of a part, restoring the one a meta summary replaced when `unwind` is set. `updateSummaryText` edits a text and counts its tokens again. `collectBlocks(branch)` splits the branch into the summary blocks in use (with their hashes, `SummaryBlock.isValid()`), `collectTree(...)` returns the same as a tree of `SummaryNode`s with the summaries a meta summary stands in for as children (the overview uses it). `copySummary` for branching. `softDeleteForManuscript` / `softDeleteForMessages` soft delete summaries in bulk for the delete of a book or parts. See [the summary chain](domain-model.md#the-summary-chain). |
| `StoryGenerationService` | Generating parts, see [Generation pipeline](generation-pipeline.md). |
| `ExporterService` | Registry of story exporters (`registerExporter`, `unregisterExporter`, `getExporters`). The built-in `TxtExporter`, `MarkdownExporter`, `HtmlExporter`, `DocxExporter`, `PdfExporter` and `EpubExporter` (package `export`, all extend `ExporterBase`) are registered in `afterPropertiesSet`; an extension can register its own `Exporter` and it shows in the export dialog. See [Story export](#story-export). |
| `BackupService` | Book backups as JSON files in `data/<login>/backups/manuscripts/<book id>/`: `takeBackup`, `getBackups`, `importBackup`, `applyBackup` (restore, optionally messages only), `cloneBackup` / `restoreAsNewManuscript`, `analyzeLorebooks` + `LorebookDecision`s for lorebooks found in the backup, `exportBackup` (see below). Import and restore read a `File` (the UI uploads to a temp file, the `byte[]` overloads are for small payloads and tests). |

**Images in backups.** A stored backup is JSON and never contains image files, only the `imageAttachments` of the
parts (resource uuids) in their `extendedContent`. `ManuscriptBackup.imageCount` is a header field (read by
`parseMetadataOnly` without loading the backup) so the UI knows whether to ask. `BackupImages` reads and rewrites the
attachments inside the backup JSON (`collect`, `remap` where returning null drops the image).
`exportBackup(backup, withImages, onImage)` writes to a temp file and returns a `BackupExport`; with images it is a ZIP:
`backup.json` (the stored file, streamed), `resources.json` (`{"version":1,"resources":[{uuid, entry, name, mimeType, hash,
size}]}`) and `resources/<n>.<ext>`, one image at a time with `Files.copy`, so nothing but the JSON tree is in memory.
Import, restore and `analyzeLorebooks` detect a ZIP by its first bytes (`openSource`) and read entries only by the names
the manifest lists under `resources/` (no extraction to disk). Each image is read bounded by `MAX_IMAGE_BYTES` and goes
through `ResourceService.uploadImage`, so it is validated again and the hash dedupe reuses an existing file.
Resources are not shared by books, so: `importBackup` keeps a uuid the user already has with the same hash and otherwise
adds the image (`adopting`), `restoreAsNewManuscript` and `cloneBackup(..., withImages)` make a new `Resource` for every
use (from the archive, or `ResourceService.copy` of the user's own resource, which points to the same file); images that
can't be found are dropped from the parts. After every restore `linkImages` notes the new parts in the resources whose
link is empty or stale. The ZIP is only a transfer format, a stored backup stays a JSON file.

The automatic book backups (`BackupStrategy.AFTER_N_MESSAGES` / `AFTER_N_MINUTES`) are triggered by the story editor
after a part is generated (`ManuscriptStoryPart`), not by a service - code that generates parts without the editor
doesn't take them.

### Story export

`ExportDialog` (opened from the settings menu of `ManuscriptStoryPart`) collects the active branch in one
`ProgressBarDialog` (indeterminate), cuts the chosen range and exports it in a second one whose total is the number of
messages, then shows the download link. The exporter gets `ExportOptions` (title, author, title page, language), the
messages and the progress dialog, on which it calls `updateProgress()` for every message.

`ExporterBase` does the common work: it renders the title page from `Defaults.DEFAULT_EXPORT_HEADER_TEMPLATE`
(Handlebars, data `ExportHeaderTemplateData`), skips parts without text, reports progress and feeds the Markdown of
each part to an `ExportWriter` that the exporter creates per export - exporters are shared between users, so any state
must live in the writer. A part whose text has a Markdown heading is a chapter (`chapterTitle`, the same rule as the
Chapter Marker plugin: the first `#` line); before the first part the writer gets the list of all chapters in
`contents(...)` and every chapter start in `message(markdown, chapter)`, so it can put an anchor (`Chapter.anchor()`)
on the chapter and link to it from the table of contents (HTML, EPUB navigation, PDF, DOCX; TXT and Markdown ignore it).
The PDF exporter keeps the export and renders it again until the pages shown in the contents match the pages the
chapters landed on; DOCX uses `PAGEREF` fields and sets `updateFields`, so Word fills them in after asking.
Images attached to a part (`ChatMessage.getImages()`) are loaded once per part (`ResourceService.findImage` +
`getResourceData`, so only the exporting user's images; a missing one is skipped with a warning) and handed to the writer
in `images(List<ExportImage>)` right after `message(...)`. A part with images but no text is exported with an empty text.
HTML embeds them as data URIs, EPUB as files in `OEBPS/images/` with manifest items, DOCX with POI pictures (scaled to
fit the page), PDF with OpenPDF images, TXT as `[Image: caption]`, Markdown as `![caption](data:...)` plus an italic caption line. `figure(src, caption)` renders the XHTML figure.
`MarkdownExporter` is the only one that does not parse: the Markdown of each part is written as it is (raw HTML included, since the output is
source and not a rendered format), as blocks separated by an empty line.
It also provides `toHtml` (CommonMark, raw HTML escaped, XHTML safe) and `walk`, which turns
Markdown into paragraphs, headings, code and rules for formats built from styled text (`BlockSink`; used by TXT, DOCX
and PDF). Libraries: CommonMark (parsing), Apache POI (DOCX), OpenPDF with the Liberation fonts (PDF); EPUB is written
with `java.util.zip`.

### Lorebooks and tags

| Service | Responsible for |
|---|---|
| `LorebookService` | `fillEntries` (loads entries with their effective tags for generation and the editor), `getSubbooks`, `delete(book, false)` also soft deletes the entries and tags of the lorebook, export / import in Marginalia's format (`exportLorebook`, `importLorebook`, `marshalLorebooks`, `analyzeImport`, `importLorebooks`), SillyTavern import (`importFromSillytavern`, via `SillyTavernEntryConverter`). |
| `LorebookEntryService` | `getEntriesForLorebook`; `delete(entry, false)` also deletes the tags of the entry. |
| `TagService` | The user's tags: `searchTagsForUser`, `countTagsForUser` (for lazy combo boxes), `getOrCreateForUser(value)`, and for the Tags tab of the settings `findUsageForUser` (every tag with the number of its relations, `TagUsage`), `findRenameConflict`, `rename` (refuses a value another tag already has) and `merge(source, target)` (moves the relations of `source` to `target`, then deletes `source`). The tags are looked up by uuid among the current user's, so a foreign tag is ignored. |
| `TagRelationService` | Attaching tags to any entity (`createRelation`, `removeRelation`, with `negative` for negative tags) and querying both ways (`getTagsForObject`, `getObjectIdsForTag`, `getObjectsForTag`); `deleteForTag` and `deleteForObject` delete the relations of a deleted tag or tagged object; `moveRelations(from, to)` re-points the relations of a tag to another one (a relation the object already has with `to` is deleted instead) and `countRelationsByTag` counts the relations of each tag of the user. |

### Models

| Service | Responsible for |
|---|---|
| `AIService` | CRUD of inference providers with rules in the service: the user's AIs are their own and the shared ones (`AI.shared`); `save` / `delete` accept only the owner (`SecurityException` otherwise) and `shared = true` only from an administrator (`AdminGuard`); a new AI needs `canCreate()` (administrators, or anybody while `AppSettings.userAisAllowed`, set by `setUserCreationAllowed`, administrators only). `delete(ai, false)` also soft deletes the usages of the AI. `isOwnedByCurrentUser(ai)` and `isAdministrator()` are for the UI; the latter reads the flag from the database, because the session's `User` bean is filled with id, login and name only, so `currentUser.isAdmin()` is always false in UI code. |
| `ProtocolService` | Plain CRUD of protocols (not shared). |
| `UsageService` | `record(ai, prefill, generated)` adds a request to the current user's `Usage` of the AI (never throws, see [Usage](domain-model.md#usage-tokens-used-with-an-ai)); `findForUser(ai)`, `findAllForAI(ai)` (owner of the AI or administrator), `findAllUsages()` (administrator). Called by `OpenAICompatibleInferenceService`, not by the callers of `stream`. |
| `AILimitService` | Limits of the users of a shared AI, see [Limits](domain-model.md#limits-of-the-users-of-a-shared-ai). `findLimits`, `setLimit` (shared AIs only, validated by `AILimit.validate()`), `removeLimit`: the administrator who owns the AI only. `getLimit(ai)` / `getStatus(ai)` (`LimitStatus`: limit, tokens used in the period, next reset) are for the current user; the session's `User` has no uuid, so the service loads it. `check(ai, promptTokens, reservedTokens)` (the prompt must fit into the sent and total limits; the generated tokens only count once reached, or with a hard limit together with the reserved response) throws `InferenceException` `NOT_AVAILABLE` or `LIMIT_REACHED`; `OpenAICompatibleInferenceService.stream` calls it first, so every request is covered. |
| `InferenceServices` | `forAI(ai)` returns the `InferenceService` for a provider. Implementations are listed in `services-config.xml` (`inferenceProviders`), each annotated `@SupportedAI(AIType...)` with a constructor taking the `AI`; the instance is cached on the (transient) `AI.inferenceService` field. `getFunctionRegistry()` is the [`FunctionRegistry`](#function-calls); `addToolRoleResolver` / `removeToolRoleResolver` let an extension change the role the results of function calls are sent with (default `tool`, a Gemma 4 API wants `tool_responses`; `resolveToolRole(ai)` asks the resolvers in the order they were added, the first non-blank answer wins) - the plugin must remove its resolver on unload. |
| `InferenceService` | One provider: `getModels()`, `testConnection()` (model list plus a one-token completion, used by the "Test Connection" button of `AIDialog`), `countTokens` / `countTokensApprox`, `stream(payload, protocol, callback)`. Failures of the provider are `InferenceException` (classified by `Type`: authentication, model/URL not found, context overflow, rate limit, server, timeout, connection...; `OpenAICompatibleInferenceService.translateException`), also passed to `callback.onError`; `InferenceErrors.describe(loc, throwable)` / `messageOf` give the localized text for the UI. `OpenAICompatible` has the extended attributes `requestTimeoutSeconds` and `maxRetries`, applied to the client (read timeout, retries with backoff for 408/409/429/5xx and connection errors). A user message of the payload (`LLMChatMessage`) can carry `LLMAttachment`s (images as data or URL, WAV/MP3 audio, files), which `OpenAICompatibleInferenceService` sends as content parts after the text; an attachment it cannot send (audio in another format, audio or a file given only by URL) throws `IllegalArgumentException` from `stream`, attachments of other roles are not sent. Streaming is asynchronous; the callback gets chunks (`REASONING` / `RESPONSE`) and must call `controller.continueInference()` to receive the next one. `stream(payload, protocol, functions, callback)` also lets the model call functions, see [Function calls](#function-calls). `infer(payload, protocol, callback)` is the non-streaming variant: still asynchronous, but `InferenceResultCallback.onResult(text, check)` gets the whole answer (reasoning is dropped); limits and usage work as for `stream`. `infer(payload, protocol, schema, callback)` also sends the JSON schema as `response_format` (not strict, an additional parameter `response_format` wins) and validates the answer (markdown fences removed) with `StructuredOutputs.validate`: `SchemaCheck` says `checked` / `valid` / `errors`, an invalid answer is still delivered. `infer(payload, protocol, SomeClass.class, callback)` builds the schema from the class (`StructuredOutputs.schemaOf`: fields and Jackson annotations, plus `@LLMSchema` on the class and `@LLMField` on fields for description, `required`, minimum/maximum, length and item limits; unannotated fields are required, no additional properties) and calls `TypedInferenceCallback.onResult(object, text)`, or `onError` with a `StructuredOutputException` (holds the answer) when it does not match. `stream(payload, protocol, schema, functions, callback)` only passes the schema to the provider as `response_format`; the chunks are delivered untouched and nothing is validated (the caller does it, `StructuredOutputs.validate`). Every variant has an overload taking a `FunctionSet` before the callback: the model may call functions, run by the same machinery as in `stream` (rounds, `MAX_FUNCTION_ROUNDS`, `onFunctionCall` reported on the callback), and the callback gets one result, that of the last round. Only `OpenAICompatibleInferenceService` implements `infer` (default: `UnsupportedOperationException`). Implemented by `OpenAICompatibleInferenceService`. |
| `TokenizerService` | Token counting per provider: tries the `TokenizerStrategy` candidates in order and caches the first that works, per provider id, until it fails or the provider is saved (`AIDialog` calls `invalidateCache`). The local JTokkit fallback is not cached, so a provider whose server was unreachable is probed again on the next count. `countTokensApprox` always uses JTokkit locally. |
| `TokenLimits` | Static helpers (in `domain.components`) for the context, response and prompt budget of a generation: the smaller of the protocol's and the provider's limit when both are set, the one that is set otherwise. |
| `TemplateService` | `processTemplate(template, name, data)` renders a Handlebars template with macros; `isValidTemplate` for the UI. See [Templating & macros](templating.md). |

### Function calls

The model can call Java methods (tool / function calling). The functions are the methods of an interface marked
`@LLMFunction(name, description)`; their parameters may carry `@LLMParam(name, description, required)` (without it the
parameter is required and named as in the source, which needs the `-parameters` compiler option the project already
uses). The parameter types are anything Jackson reads: primitives, strings, enums, collections, maps, records and beans
(fields described with `@JsonPropertyDescription`).

Functions are **asynchronous**. Every `@LLMFunction` method returns `void` and takes a `FunctionContinuation<R>` as its
first parameter (hidden from the model, not in the schema); `FunctionRegistry.definitionsOf` throws
`IllegalArgumentException` for a method that returns a value or does not have the continuation first (or marks it with
`@LLMParam`). The method is called on a worker thread, in the request scope of the caller, and may hand the work to any
other thread; it finishes the call with `done.complete(result)` or `done.fail(message | throwable)`, whenever and from
wherever. The first finish wins. `done.isCancelled()` tells that the request is over. An exception thrown by the method
itself is a failed call.

```java
interface Lore {
    @LLMFunction(description = "Finds lorebook entries by name")
    void find(FunctionContinuation<List<String>> done, @LLMParam(description = "Part of the name") String name);
}

FunctionSet functions = inferenceServices.getFunctionRegistry().bind(Lore.class, new LoreImpl(user));
inferenceService.stream(payload, protocol, functions, callback);
```

| Type | What it does |
|---|---|
| `FunctionRegistry` (`inferenceServices.getFunctionRegistry()`) | `definitionsOf(type)` compiles the interface once - JSON schema of every method's parameters (victools `jsonschema-generator`, Jackson module, every parameter schema inlined, recursive types moved to the `$defs` of the function schema), the Jackson types the arguments are parsed into and a method handle - and caches it in a `ClassValue`, so it lives as long as the interface class (an unloaded extension releases its entries). `bind(type, implementation)` is cheap and makes a `FunctionSet`. An interface without `@LLMFunction` methods, with a name used twice, a name providers do not accept (`[a-zA-Z0-9_-]{1,64}`), a static method or an unnamed parameter is an `IllegalArgumentException`. |
| `FunctionSet` | The functions of one request, bound to their implementations; `and(other)` combines sets of several interfaces. `call(name, argumentsJson, executor, cancelled)` returns a `CompletableFuture<String>`: on the executor it parses the JSON object the model wrote into the parameters (unknown properties ignored, a missing optional one is null / 0 / false, a missing required one is an error), calls the method with a `FunctionContinuation` and completes the future when the method answers, with the result rendered: `null` as `OK`, a string as it is, anything else as JSON. Every failure is the `FunctionCallException` the future completes exceptionally with. |
| `FunctionContinuation<T>` | What a function answers with: `complete(T)`, `fail(String)`, `fail(Throwable)`, `isCancelled()`. |
| `FunctionCallException` | A call that could not be carried out; `Kind` is `UNKNOWN_FUNCTION`, `INVALID_ARGUMENTS` or `FAILED` (the method threw). Its message is written for the model. |

`OpenAICompatibleInferenceService` offers the functions as `tools`, collects the calls from the stream (they arrive in
pieces), and when the answer ends with calls it runs them as a chain of futures: one after the other, each started on a worker thread (with the request attributes of the caller), the next only after the previous one answered through its continuation, and nothing blocks while a function waits. When the last has answered the next request is sent. Cancelling the token (or a dead callback) fails the pending call and ends the request with `onCancel`. `onFunctionCall` is called on the thread the function answered on. 
appends an assistant message with the calls (`LLMChatMessage.ofToolCalls`, `LLMToolCall`) and a `LLMRole.TOOL` message
per result (`ofToolResult`) to its own copy of the payload and sends the request again. The callback sees one stream of
chunks and one completion (the text before a call and the answer after it are both `RESPONSE` chunks), and
`InferenceAsyncCallback.onFunctionCall(call, result, failed)` reports each call. A failing call is not an error of the
request: the model gets `Error: <message>` as the result and can correct itself. After `MAX_FUNCTION_ROUNDS` (10) rounds
the request is sent with `tool_choice: none`, so the stream ends with an answer. A cancelled token or a dead callback
stops before the next call. Every request of the loop is counted in the `Usage` separately. A payload that already holds
tool calls and results (`LLMChatMessage` with `toolCalls` / `toolCallId`) is sent as it is. The generation pipeline does
not offer functions yet; they are for services and extensions that call `stream` themselves. A service that cannot call
functions rejects a non-empty `FunctionSet` with `UnsupportedOperationException` (the default of the interface).

### Users and settings

| Service | Responsible for |
|---|---|
| `UserService` | Users: `authenticate` (PBKDF2-HMAC-SHA256 hashes, constant-time compare and the same amount of work for an unknown user, a missing password or a locked account; the first 10 consecutive failures are free, then the account is locked for 1 s, 2 s, 4 s... up to 15 minutes, tracked in `User.failedLogins` / `lockedUntil`; a locked account refuses even the right password; `authenticate(username, password, clientAddress)` also throttles per client address, in memory only: the first 30 consecutive failures from an address are free, then it is blocked with the same 1 s ... 15 minutes back-off for every user name, blocked attempts are not counted against the user, a successful login does not clear the address (otherwise any account holder could reset the count between batches of guesses), entries are forgotten after an hour without failures, the table keeps at most 10,000 addresses (least recently used dropped first) and a restart clears everything; every failure and the moment of a block are logged; see [Login throttling](#login-throttling)), `unlock` (an administrator's *Unlock account* checkbox in `UserDialog`, shown while the user has failures on record: forgets the failures and the lock), `changePassword` (also resets the lock, drops all saved logins and invalidates the user's other open sessions through `SessionManager.runForUsers` - the caller's own session is kept; the current password is checked by `UserDialog` for self edits), `clearPassword` (removes the password, resets the lock, removes saved logins and invalidates the user's sessions except the caller's, an administrator's reset), `isLoginAvailable`, `isLastAdmin` (the last administrator can't be removed or demoted), `deleteUser` (soft delete, frees the login by renaming it to `<login>#<uuid>`, drops saved logins, renames the data folder to `<login>-deleted`; owned data is purged with the user by cleanup), saved logins (`generateNewPersistentInfo`; `authenticateFromCookie` hashes the secret once and compares it with every saved login in constant time, a null user takes the same path; a matched login is rotated). |
| `SettingService` | `getOrCreate(UserSetting.class)` for the current user, `getOrCreate(cls, user)`, `getOrCreateApp(AppSettings.class)` for the installation. |
| `ResourceService` | Files stored by hash in the user's `resources` (or `images`) folder. `uploadImage` stores an attachment (format detected from the content, WebP converted to PNG, 10 MB and 50 megapixel limits), `findImage(uuid)` finds the current user's image. `link` / `unlink` / `describeLink` keep the loose `clazz` + `objectId` note (class name and id of the object using the resource, like `TagRelation`; not a cleanup reference, `describeLink` only checks that the entity exists and is not deleted). `findPageForUser` / `countForUser` serve the lazy table of the Resources tab, `replace` writes a new file and points the resource at it, `softDelete(uuids)` marks resources of the current user deleted (cleanup purges them; files are never deleted). |

### Login throttling

Failed logins are limited twice. Per user (`User.failedLogins`, `lockedUntil`, in the database) and per client address
(`UserServiceImpl.addressFailures`, in memory). Both use the same back-off, 1 s, 2 s, 4 s ... up to 15 minutes, after
10 (user) or 30 (address) consecutive failures; the address limit is higher because several users can share an address.
A failure counts against the address whatever the user name was, so a password spray over many accounts is stopped
even though no single account fails often.

The address comes from `UIUtils.clientAddress(Configuration)` (`LoginCheckRoute.clientAddress()` in the login routes),
which asks `ClientAddressResolver`:

- The address of the connection is used by default. `X-Forwarded-For` is **ignored**, because any client can send it.
- When `trustedProxies` (`configuration.properties`, or `-DtrustedProxies=...`) lists the connecting peer - addresses or
  CIDR ranges - the header is read from the right and the first address that is not a trusted proxy is the client.
  Entries left of it were sent by the client and are never used. The private network is not trusted by itself.
- Host names are never resolved and a bad `trustedProxies` entry stops the start.

Without a proxy nothing needs to be configured. Behind one, list the proxy, otherwise all users share its address
and 30 failures from anyone block everybody for a while.

### Administration

The services behind the Administration screen check on the server that the caller is an administrator, so they stay
safe if something other than that screen (an API, an extension) ever reaches them. They call
`AdminGuard.requireAdmin()` (bean in `services-config.xml`), which loads the current user from the database - the
session copy of `admin` is not trusted, a user demoted or deleted mid-session loses access at once - and throws
`SecurityException` for nobody, a deleted user or a non-administrator. Guarded: every `DatabaseBackupService` method
except `createScheduledBackup` and `getNextScheduledBackup`, `CleanupService.getReferenceModel/analyze/purge`,
`OsgiService.installPackage/uninstallPackage`, and `UserService.deleteUser/clearPassword/unlock`. Code that runs
without a request (the backup schedule, loading extensions on start, `registerContributor`) is not guarded, and
inside these services it calls private unguarded variants (`listBackups`, `removeBackup`). A new administrator-only
service method calls `adminGuard.requireAdmin()` first.

| Service | Responsible for |
|---|---|
| `DatabaseBackupService` | Database backups with `VACUUM INTO`, upload, staged restore, the cron schedule (`CronSchedule`, Spring `ThreadPoolTaskScheduler`) and its rotation. See [Database backups and restores](database.md#database-backups-and-restores). |
| `CleanupService` | `analyze()` builds the plan of what can be purged (from the Hibernate metamodel and `@CleanupReference`s), `purge()` executes it. Extensions with their own references register a `CleanupContributor` (`collectReferences` to block purges, `beforePurge` to clean up). See [Cleanup references](domain-model.md#cleanup-references). |
| `TrashService` | The trash: lists soft-deleted objects (`find`/`count` with a `TrashFilter` of owner and type), shows their extended content as JSON and restores them. Implemented by `CleanupServiceImpl` (same bean as `CleanupService`) because it needs the cleanup reference model. Not administrator-only: a user is limited to their own objects, an administrator (`AdminGuard.isAdmin()`) sees and restores everything, and objects of others raise `SecurityException`. `deletePermanently` removes deleted story parts and lorebook entries for good (children of a removed part move to its parent). `restore` also restores the deleted objects the selection owns (messages of a book...) and is all or nothing: an object that depends on a deleted object outside the selection (`STRONG` and `OWNED_BY` references it holds, and the `OWNS` reference of the object that owns it) blocks the whole restore, see `RestoreResult.getBlocked()`. See [Trash](domain-model.md#trash). |
| `OsgiService`, `ExtensionService` | Loading extensions and the `@Extendable` decorators (which read arguments and locals and can replace the return value of the decorated method), see [Plugin development](plugins/index.md). `OsgiService` also stops and starts extensions without uninstalling them (`stopExtension` / `startExtension` / `isStopped`, the `<jar name>.STOPPED` flag next to the JAR), gives the SHA-256 of an extension's JAR (`getSha256`, cached until the file changes) and gives each extension its own logger: `createLogger(bundle)` returns an SLF4J logger (implemented by `ExtensionLogger`) that appends to `<jar name>.log` in the extensions folder (moved to `.log.old` over 2 MB) and chains each line to the application log (logger `extension.<name>`); `readLog` / `clearLog` serve the Admin page. See [Logging](plugins/overview.md#logging). |

## Asynchronous operations

Long operations - generating a part, a summary, streaming from a model - return immediately and report through a
callback:

| Operation | Returns | Callback |
|---|---|---|
| `StoryGenerationService.generateNextTurn` | `CancellationToken` | `GenerationListener` |
| `SummaryService.createSummary`, `createMetaSummary` | `CancellationToken` | `SummaryService.AsyncCallback` |
| `InferenceService.stream` | `CancellationToken` | `InferenceAsyncCallback` |
| `InferenceService.infer` | `CancellationToken` | `InferenceResultCallback` / `TypedInferenceCallback` |

`CancellationToken.cancel()` stops the operation (the inference callback's `isDead()` reports it to the stream).
Callbacks run on worker threads: UI code in them must use `ui.access(...)` (see
[Threads and push](architecture.md#threads-and-push)), and service calls need the request scope shown above.

## Writing a new service

1. Interface in `domain/service/`; for an entity extend `ExtendableService<Entity, EntityRepository>`.
2. Implementation in `domain/service/impl/` extending `ExtendableServiceImpl<...>`; repository extending
   `JpaExtendableRepository<Entity>` with `getEntityClass()`.
3. Beans in `services-config.xml`:

   ```xml
   <bean id="bookmarkRepository" class="com.github.enerccio.marginalia.domain.repository.impl.JpaBookmarkRepository">
       <property name="entityManager" ref="em"/>
   </bean>

   <bean class="com.github.enerccio.marginalia.domain.service.impl.BookmarkServiceImpl">
       <property name="repository" ref="bookmarkRepository" />
   </bean>
   ```

4. Annotate every public method: `@CommonTx`, `@CommonTxReadOnly` or `@NoTx`.
5. Use `currentUser` for ownership, `findForUser(uuid)` for anything coming from outside, and call
   `AdminGuard.requireAdmin()` for administrator operations - hiding a button in the UI is not a permission check.
6. Keep the class non-final with public methods (CGLIB proxies subclass it).
7. Test it on top of `MarginaliaTestBase` (see [Testing](testing.md)).

Extensions can't add beans to the application context. They keep their logic in their own classes annotated
`@Configurable` (plugins are compiled with the AspectJ compiler too), which get the application's services
`@Autowired` - for example the Reviewer's `ReviewerService`. Transactions then come from the application services they
call. See
[Plugin development](plugins/index.md).
