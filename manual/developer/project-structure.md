---
label: Project structure
order: 970
verified: 21e02c73
covers:
  - pom.xml
  - marginalia/src
  - marginalia/plugins
  - rebuild.sh
  - rebuild.bat
  - marginalia/pom.xml
  - tools
---

# Project structure

A tour of the repository: what each folder and package contains and where to look when you want to change
something. [Architecture](architecture.md) explains how the parts work together; this page is the map.

## Repository root

```
Marginalia/
├── marginalia/              the application - a Maven project (see below)
│   └── plugins/             bundled extensions, one Maven project each
├── manual/                  sources of this manual (Retype, Markdown)
│   └── images/              screenshots used by the manual and the READMEs
├── docs/                    generated manual, served by GitHub Pages - don't edit by hand
├── screenshots/             scripted environment that takes the pictures of the manual (see its SCREENSHOTS.md)
├── tools/                   check_dev_docs.py (stale developer pages), annotate_ui_maps.py (labels of the UI map figures)
├── .github/workflows/
│   ├── desktop.yml          CI: tests, desktop builds for 5 platforms, release upload
│   └── server.yml           CI: WAR, plugin JARs, Docker image (ghcr.io), release upload
├── pom.xml                  parent POM: the version of the application and the plugins, lists them as modules
├── Dockerfile               two-stage build: Maven builder → Jetty 12 image
├── docker-compose.yml       the server setup (port 8080, ./data volume, JVM options)
├── rebuild.sh, rebuild.bat  update a Docker server: pull, build the plugins, copy them to data/extensions, restart
├── retype.yml               manual configuration (input manual/, output docs/)
├── README.md, LICENSE
├── CHANGELOG.MD             user- and plugin-visible changes, per release
├── TODO.md                  release plan
├── TODO.BUGS.md             known bugs
└── TODO.IMAGES.md           screenshots not taken by the scripts
```

| Path | Notes |
|---|---|
| `data/` | Created by `docker compose`: the data folder of the Docker server. Ignored by Git. |
| `.gitattributes` | Keeps line endings of the desktop launch scripts and of `rebuild.sh` / `rebuild.bat` (`LF` for the shell scripts, `CRLF` for the batch files). |
| `.dockerignore` | Excludes `marginalia/target/`, `marginalia/node_modules/`, the plugin `target/` and `build/` folders and `.idea/` from the Docker build context. |

## The application (`marginalia/`)

```
marginalia/
├── pom.xml                       dependencies, build plugins, the desktop profile
├── src/main/java/                application code
├── src/main/resources/           Spring XML, Flyway migrations, log4j, Vaadin build info
├── src/main/resources-release/   WAR-only resources: the release log4j.properties
├── src/main/resources-raw/       copied into WEB-INF/classes without filtering (empty)
├── src/main/webapp/config/       configuration.properties, persistence.xml
├── src/main/frontend/            CSS and JavaScript for the Vaadin frontend
├── src/desktop/                  desktop launcher and launch scripts (not part of the WAR)
├── src/test/                     tests
└── plugins/                      bundled extensions
```

The root `pom.xml` is the parent POM and lists the application and every plugin as modules, so `mvn package` there builds
all of them; each plugin is also a Maven project that is built on its own. See [Building from source](building.md).

## Java packages

Application code lives in `com.github.enerccio.marginalia` (paths below are relative to
`src/main/java/com/github/enerccio/marginalia/`), plus a few helpers in `com.github.enerccio.tools`.

### Top level

| File | |
|---|---|
| `Configuration.java` | The `configuration` bean: data folder and its subfolders, database URL, staged database restore, the copy saved before a migration, saved login settings. |
| `Defaults.java` | Built-in prompt defaults: master template, POV, tense, style, user prompt and summary prompt - used when neither the book nor the user's settings override them - and the template of the title page of story exports. |
| `Constants.java` | Application constants: `DEAD_SESSION_CHECK_TIMEOUT` (seconds without a heartbeat before `SessionManager` closes a UI, default 120). |
| `UIConstants.java`, `CssStyles.java` | Column widths and dialog offsets; CSS class names defined in `shared-styles.css`. |
| `SaneSQLiteDialect.java` | Hibernate's community SQLite dialect without generated `CHECK` constraints. |

### `bound` - startup and sessions

| File | |
|---|---|
| `ApplicationInitializer.java` | Runs registered `Migration`s when the stored `AppSettings` versions are behind (`bound/migration`: `EncryptApiKeysMigration`, `FulltextMigration`, `OriginalResponseMigration`, see [Database & migrations](database.md)). |
| `Migration.java` | Interface of data migrations in Java (schema migrations are Flyway SQL). |
| `SessionManager.java` | The `sessionManager` bean: registry of all HTTP sessions, binds the logged-in user to them, runs code in other users' sessions (`runForUsers`) and closes dead UIs and sessions on its watcher thread. See [Sessions](architecture.md#sessions). |
| `SessionTrackingListener.java` | `HttpSessionListener` registered by `WebappApplicationInitializer`; forwards session creation and destruction to `SessionManager`. |
| `SessionInformation.java` | What `SessionManager` knows about one session: HTTP and Vaadin session, user, main UI, captured request attributes, close listeners. |
| `SessionCloseListener.java`, `RunInSession.java` | Callbacks: another session was closed; code to run for a session in `runForUsers`. |
| `ApplicationPoint.java` | The `applicationPoint` bean: application-wide maps of open UIs to their `Workspace` (only `Main`) and to a copy of their logged-in user. `Main` and `Viewer` `register` the UI after login; the entries are removed when the UI detaches. |
| `SessionPoint.java` | Session-scoped bean; holds the `Language` of the session's UI (set from the user's settings when the workspace opens, read by `LocalizationImpl`) and the conversation with the help assistant (`getHelpChat()`). |

### `ui` - user interface

| Package | Files |
|---|---|
| `ui` | `Restorable` (`storeState()` / `restoreState(JsonObject)`, implemented by what has state worth remembering) and `RestorableState` (its defensive readers and `put`), see [User interface](ui.md#remembering-where-the-user-was); `JavascriptLibrary` and the other helpers of the package. `LLMInteract` (a part of the UI describes itself to the help assistant's model: name, Markdown state, children, and the harmless moves it offers), `LLMInteraction` (one such move) and `LLMSnapshot` (the tree of what the model reads, with its paths), see [User interface](ui.md#letting-the-help-assistant-see-the-ui). |
| `ui.main` | `WebappApplicationInitializer` (creates the Spring context, registers the session listener), `MarginaliaServlet` (the `VaadinServlet`), `AppShellConfig` (push, theme, global CSS), `LoginCheckRoute` (login overlay, saved logins, first-start dialog), `Main` (route `/`), `Viewer` (route `/view`, published books). |
| `ui.workspace` | `Workspace` (interface of the workspace shown after login), `DesktopWorkspace` (its tabbed implementation with footer buttons), `WorkspaceComponent` (interface of its tabs) and `WorkspaceAutosave` (stores the state of the workspace in `UserSetting.workspaceState`). `WorkspaceLLMSupport` (what the workspaces tell the help assistant: the sections by their labels and the moves to open them). |
| `ui.workspace.phone`, `ui.workspace.phone.parts` | `PhoneWorkspace` (the workspace for phones: an `AppLayout` with a drawer instead of the left-hand tabs) `PhoneManuscriptPart` (single column book list), `PhoneLorebookPart` (reduced lorebook view), `PhoneProtocolPart` / `PhoneAIPart` (two column protocol and model lists) `PhoneSettingsPart` (`UserPart` with a stacked header) and `PhoneTrashPart` (trash with `PhoneTrashGrid`). |
| `ui.dialogs.phone`, `ui.dialogs.phone.manuscript` | `PhoneManuscriptDialog` (the book dialog for phones: full screen, one scrolling tab row), `PhoneSummariesDialog` (`SummariesDialog` with a wide content column) and its tabs `PhoneManuscriptInfoPart`, `PhoneManuscriptPromptPart`, `PhoneManuscriptLorebookPart` (with `PhoneLorebookView`), `PhoneManuscriptStoryPart` (one column, outline in the drawer of an `AppLayout`, message cards with a header line of icons), `PhoneManuscriptTreePart`, `PhoneManuscriptBackupPart`. |
| `ui.workspace.parts` | One class per workspace tab: `ManuscriptPart` (Books), `LorebookPart`, `UserPart` (Settings), `ProtocolPart`, `AIPart` (Inference Providers), `ResourcesPart` (Resources: lazy `Grid` over `ResourceService.findPageForUser`), `TrashPart` (Trash: the `TrashGrid` widget), `AdminPart`, plus `NewBookAction` (the *Add Book* flow shared by both book lists). |
| `ui.workspace.parts.admin` | Admin panels with their own logic: `DatabaseBackupPanel`, `CleanupPanel`, `HelpAssistantPanel` (the settings of the help assistant). |
| `ui.dialogs` | Entity dialogs (`AIDialog`, `ProtocolDialog`, `LorebookDialog`, `UserDialog`), the limits of a shared AI (`AILimitsDialog`, `AILimitEditDialog`; `AILimitInfoDialog` is the user's own view), `UsageDialog` (every user's usage), `ManuscriptDialog` (the book window), `LorebookImportDialog` (resolving lorebooks during restores), `PromptDialog` (shows the prompt a part was generated from), `ExtensionLogDialog` (the log of an extension, opened from the Admin page), `UsageDialog` (what every user used of every AI, opened by administrators from the Inference Providers tab) and generic dialogs: `ConfirmDialog`, `TextInputDialog`, `ListSelectDialog`, `ErrorDialog`, `ProgressBarDialog`. `ExportDialog` exports the story of a book. Threading helpers `ThreadAccessDialog` and `UIPushGuard`. |
| `ui.dialogs.story` | What the desktop and the phone story tab share: `StoryHost` (implemented by both book dialogs), `StoryCard` / `StoryCards`, `StoryGenerationListener`, `NewTurnForm`, `TurnDetailsForm`, `MessageMetrics`, `StoryBackup`, `StorySupport`. |
| `ui.dialogs.manuscript` | The tabs of the book window, each a `ManuscriptDialogPart`: `ManuscriptStoryPart` (story editor - the largest UI class), `ManuscriptTreePart`, `ManuscriptInfoPart` (About), `ManuscriptPromptPart`, `ManuscriptLorebookPart`, `ManuscriptBackupPart`; and the summary dialogs: `SummariesDialog` (overview of the summaries of the branch, opened from the story editor), `SummaryDialog` (one summary or meta summary, generating or viewing) and `SummaryRemoval` (asking before a summary is removed); and `ImagesDialog` (add, caption and remove the images of a part, opened from its card). |
| `ui.components` | Larger reusable components: `TagsView` (the Tags tab of the settings: tags with their usage, rename, merge, delete), `LorebookView` (the lorebook editor, used in the Lorebooks tab and in books), `PhoneLorebookView` (its reduced phone version), `LorebookPreview` (the read only Lorebook tab of the desktop story editor), `InvalidExtensionsWarning` (the red bar of the workspace about invalid extensions), `TreantTree` (story tree drawn with treant.js), `MessageImages` (the images of a part under its text - in the story editor, the Viewer, the image dialog and the Resources tab preview; the files are read when the browser asks for them), `ThreadCopyRequestAttributes` (carries the session to worker threads). `HelpAssistantWidget` (the robot button and chat panel `Main` adds after the workspace; it answers the model's requests to read and move through the page on the page's thread). |
| `ui.widgets` | Small widgets: `ChartJs` (a Chart.js chart drawn from a JSON configuration, see [UI](ui.md)), `GraphView` (a Cytoscape.js graph, see [Custom components](custom-components.md)), `HTabSheet` (tab sheet with the tabs on the left and room for custom content, used by the workspace), `TabDock` (columns of tab sheets whose tabs can be dragged apart and together, the left bar of the story editor), `BackendTableProvider*` / `BackendTableItem` (lazy grids backed by a repository query), `TagMultiComboBox`, `TrashGrid` (restoring deleted objects and deleting parts and entries for good, used by `TrashPart` and `CleanupPanel`), `PhoneTrashGrid` (its phone layout, used by `PhoneTrashPart`), `TemplateHints` (hint popovers of prompt fields), `TextAreaPopoverComponent` / `TextFieldPopOverComponent`, `HtmlText`, `ScrollPanel`, `ModelProtocolPicker` (a model and a protocol to choose from, for dialogs that run the model with a choice of their own), `ResizableTextArea` (`install(loc, area, fixedHeight)` adds a corner icon that switches a text area between a fixed height and growing with its content; used by the prompt, description and lorebook fields), `Notification`, `PermissiveLoginOverlay` (allows empty passwords). |

### `domain.model` - entities

| File | |
|---|---|
| `BaseEntity`, `OwnedEntity`, `ExtendableEntity` | Mapped superclasses: id, uuid, soft delete, timestamps; owner; JSON `attributes` for extensions. |
| `Setting` | Base of settings, single table `settings` with a `DTYPE` discriminator. |
| `impl/Manuscript` | A book. |
| `impl/ChatMessage` | A story part - a node of the story tree. |
| `impl/Summary` | A summary of the story up to a part, or a meta summary of summaries. |
| `impl/Lorebook`, `impl/LorebookEntry` | Lorebooks and their entries. |
| `impl/AI`, `impl/OpenAICompatible`, `impl/ExtensionAI` | Inference providers (`AI` is the base; `OpenAICompatible` talks to an OpenAI-compatible API, `ExtensionAI` is provided by an extension). |
| `impl/Protocol`, `impl/ChatCompletionProtocol` | Protocols (generation settings). |
| `impl/Tag`, `impl/TagRelation` | Tags and their assignment to objects (by class and id, optionally as a negative tag). |
| `impl/Resource` | Uploaded files stored by hash, used for the image attachments of a part (`ChatMessage.getImages()`); `clazz` + `objectId` loosely note what uses it. Listed in the Resources tab (`ResourcesPart`). |
| `impl/Usage` | What a user used of an AI (prefill and generated tokens, requests, and the tokens since the last reset of their limit); an `OwnedEntity`, one per AI and user, so a shared AI has one for everybody who uses it. |
| `impl/settings/AppSettings`, `impl/settings/UserSetting` | Installation-wide settings (versions, database backup schedule) and per-user settings. |

`User` is in `domain.security.model`. Every entity must also be listed in `src/main/webapp/config/persistence.xml`.
See [Domain model](domain-model.md).

### `domain.repository` - data access

One interface and one JPA implementation (`impl/Jpa*Repository`) per entity, built on `BaseRepository` →
`OwnedRepository` → `ExtendableRepository` (and `JpaBaseRepository` → ...). The beans are defined in
`services-config.xml` with the shared `EntityManager`.

### `domain.components` - plain data and helpers

Small classes that are neither entities nor services, shared by services, generation steps and the UI. No Spring beans,
no state besides their fields.

| Class | |
|---|---|
| `TurnInput` | The four fields of the instruction panel (scene, POV character, present characters, instructions). |
| `TokenLimits` | Static helpers for the context, response and prompt budget of a generation (the smaller of protocol and provider limit). |
| `AILimit`, `AILimits`, `LimitStatus` | The limit of one user on a shared AI (strategy, reset, token limits, hard limit of the generated tokens), the JSON in `AI.limits` that holds them by user uuid, and where a user stands (limit, tokens used in the period, next reset; `block(...)` says what stops a request). See [Limits](domain-model.md#limits-of-the-users-of-a-shared-ai). |
| `PromptTokenBreakdown` | Raw token counts of one built prompt: the budget, every section, what was cut and the tokens of each payload message. Filled by the generation steps, stored with the part (`ChatMessage.getTokenBreakdown()`), shown by `TokenUsageDialog`. |
| `SummaryNode` | A summary and the summaries it merges, for the summaries overview. |
| `CronSchedule` | Cron expression parsing and next-run computation, used by the database backup schedule. |

### `domain.service` - business logic

Interfaces in `domain.service`, implementations in `domain.service.impl`:

| Service | Responsible for |
|---|---|
| `BaseService`, `OwnedService`, `ExtendableService` | Generic CRUD, soft delete, lookups restricted to the current user. |
| `ManuscriptService`, `ChatMessageService`, `SummaryService` | Books, the story tree (add, branch, swipe, delete a part and re-attach its children, word and token counts), summaries. |
| `LorebookService`, `LorebookEntryService` | Lorebooks, entries, import and export (incl. SillyTavern through `SillyTavernEntryConverter`). |
| `AIService`, `ProtocolService` | Inference providers and protocols. |
| `TagService`, `TagRelationService`, `SettingService`, `ResourceService` | Tags, settings, files. |
| `AILimitService` | The per-user limits of a shared AI: the owning administrator sets them, `check` refuses a request of a user who is over their limit (called by `OpenAICompatibleInferenceService.stream`), `getStatus` is for the user's own view. |
| `UsageService` | Tokens a user used with an AI, counted by `OpenAICompatibleInferenceService` (`Usage`, one per AI and user); `findAllUsages()` lists everybody's for administrators. |
| `StoryGenerationService` | The generation engine (`StoryGenerationServiceImpl.GenerationEngine`). |
| `InferenceServices`, `InferenceService`, `InferenceException`, `InferenceErrors` | Choosing and calling the model API; classified failures and their localized texts. |
| `FunctionRegistry` | The Java interfaces the model can call as functions, compiled once (see [Function calls](services.md#function-calls)). |
| `TokenizerService` | Token counting. |
| `ManualService` | The user manual packed into the WAR (`classpath:/manual/`), parsed once and searched in memory (BM25) for the help assistant. |
| `HelpAssistantService`, `UnmeteredScope` | The help assistant: its settings (administrators), the AI it uses, who pays (`UnmeteredScope` is read by the inference services), `ask` with the manual tools and the screen. |
| `TemplateService` | Rendering Handlebars templates with macros. |
| `BackupService`, `DatabaseBackupService` | Book backups (restore, clone, export); database backups and their schedule. |
| `CleanupService`, `TrashService` | Purging soft-deleted data that is no longer referenced; listing, restoring and (parts, entries) permanently deleting it (one bean, `CleanupServiceImpl`, shares the reference model). |
| `IPCService` | Providers of text about a book or a story part that extensions register for each other (in memory, removed with the bundle). |
| `OsgiService`, `ExtensionService` | Loading, stopping and starting extensions (the `.STOPPED` flag), the logger of an extension (`createLogger`, implemented by the package-private `ExtensionLogger`) and its log file; `@Extendable` decorators. |

Also here: `CancellationToken`, `GenerationListener` (UI
callbacks of a generation) and `search/` (`Sorter`, `ManuscriptFilterValues` for the book lists; `FulltextQuery`, `FulltextHit` and `LikePatterns` for the full-text search and `LIKE` patterns).

| Package | |
|---|---|
| `service.impl.generation` | Engine interfaces and data: `GenerationStep`, `GenerationStepBase`, `GenerationStepType`, `GenerationController`, `Events`, `GenerationEvent`, `GenerationRequest` / `GenerationRequestType`, `GenerationProperties`. |
| `service.impl.generation.impl` | The eight steps, `PrepareForGenerationStep` ... `CleanupStep`. |
| `service.impl.generation.dto` | `LLMChatMessage` (role, text and optional `LLMAttachment`s: images, audio, files; the OpenAI compatible provider sends them as content parts of a user message, audio only as wav/mp3; `LLMToolCall`s of an assistant message and the `toolCallId` of a `TOOL` result), `LLMRole`, `PrePromptData` (the prompts collected for one generation). |
| `service.function` | `FunctionDefinition`, `FunctionSet`, `FunctionCompiler` (JSON schema of the parameters, validation of the asynchronous method shape, argument parsing, method handles), `FunctionContinuation` (how a function answers), `FunctionCallException`; the annotations `@LLMFunction` / `@LLMParam` are in `domain.traits`. |
| `service.manual` | The manual and the help assistant: `ManualParser`, `ManualIndex`, `ManualPage` / `ManualSection` / `ManualHit`, the functions `ManualTools`, `HelpTools` and `InteractiveHelpTools` with their implementations, `UiStateSource` (how the model reads and moves through the screen), `HelpPrompts` (the default prompts). |
| `service.impl.inference` | `OpenAICompatibleInferenceService` (openai-java client, streaming, reasoning, timeout and retries, mapping of API failures to `InferenceException`). |
| `service.structured` | `StructuredOutputs` (JSON schema of a class from `@LLMSchema` / `@LLMField`, validation and parsing of a model's answer), `SchemaCheck`, `StructuredOutputException`; used by `InferenceService.infer`. `PartialJson` reads the unfinished JSON of a streamed answer into a Jackson tree (for showing a `stream(..., schema, ...)` answer while it arrives). |
| `service.impl.inference.tokenizer` | `TokenizerStrategy` and its implementations: OpenAI SDK, llama.cpp, LiteLLM, LiteLLM Anthropic, JTokkit. |

See [Services](services.md) and [Generation pipeline](generation-pipeline.md).

### `domain.templates` - prompts

| File | |
|---|---|
| `TemplateData` | Base of all template data; implements the SillyTavern-compatible macros. |
| `MasterTemplateData`, `UserPromptData`, `LorebookTemplateData`, `SummaryTemplateData`, `MetaSummaryTemplateData` | The variables available in each kind of template (listed in the UI hints). |
| `ExportHeaderTemplateData` | The variables (`title`, `author`) of the title page template of story exports. |
| `TemplateContext` | What macros are evaluated against: POV character, model, story so far, variables. One per generation. |
| `TemplateVariables` | Storage of `setvar` / `getvar` variables (local per branch, global per book). |
| `MomentFormat` | moment.js date formats → `DateTimeFormatter` for `{{datetimeformat}}`. |
| `macros/MacroTranslator` | Rewrites SillyTavern macro syntax into Handlebars. |
| `macros/MacroHelpers`, `macros/Macros` | Handlebars helpers for the macros; registry of supported and ignored macros. |

See [Templating & macros](templating.md).

### `export` - story export

| File | |
|---|---|
| `ExporterBase` | Common part of the exporters: title page, progress, Markdown parsing, `toHtml` and the `walk` over Markdown blocks. |
| `TxtExporter`, `MarkdownExporter`, `HtmlExporter`, `DocxExporter`, `PdfExporter`, `EpubExporter` | The built-in formats, registered by `ExporterServiceImpl`. |

See [Story export](services.md#story-export).

### `domain.security`

`model/User`, `repository/UserRepository` (+ `impl/JpaUserRepository`), `service/UserService` (+
`impl/UserServiceImpl`: authentication, password hashes, saved logins, protection of the last administrator),
`AdminGuard` (`requireAdmin()`, used by the administrator-only services, see [Services](services.md#administration)) and
`PersistedLoginInfo` (a saved login; the user's saved logins are stored as delimited text in `User.savedLogins`).

### `domain.traits`, `domain.collections`, `domain.listener`

| Package | |
|---|---|
| `domain.traits` | Annotations: `@CommonTx`, `@CommonTxReadOnly`, `@NoTx` (transactions), `@Extendable` (instrumented UI classes) with `@PublicAPI`, `@APIReturn`, `@APIParameter`, `@APILocals` (the signature of each of their methods that plugins see), `@LLMSchema` / `@LLMField` (describe a class the model answers with, see `StructuredOutputs`), `@ExtendedAttribute` (fields stored in `attributes`), `@Fulltextable` (fields whose text goes into `_fulltext`), `@CleanupReference(s)` (how references behave during cleanup), `@LocalizedTemplateDescription` (texts of template hints), `@SupportedAI` (provider type of an inference service). |
| `domain.collections` | Enums: `AIType`, `ProtocolType`, `ReasoningEffort`, `LimitStrategy`, `ResetStrategy`, `FilteringMode`, `InsertionMode`, `SummaryType`. |
| `domain.listener` | `ExtendableEntityListener` - serializes `attributes` and `@ExtendedAttribute` fields to `extendedContent` and back, and collects the `@Fulltextable` fields into `_fulltext`. |

### Extensions, localization, helpers

| Package | |
|---|---|
| `extensions` | `MarginaliaExtension` - the service interface every extension bundle registers; `ExtensionHasCss` and `ExtensionHasLocalization` - optional interfaces of an extension (its styles, its languages); `ExtensionAIFactory` and `ExtensionAIForm` - an inference provider brought by an extension and its settings form. |
| `instruct` | `RuntimeInstrumentationInitializer` (installs the ByteBuddy agent), `ExtendableMethodVisitor` (the bytecode rewrite), `ExtensionServiceHolder` (static access to `ExtensionService` from instrumented code); `instruct.verify`: `ExtensionVerifier` (checks an extension's decorators against the application before it is started, `ExtensionService.verifyExtension`), `ContextInterpreter` and `ContextValue` (its bytecode data-flow analysis), `ExtensionVerification` (the result). |
| `loc` | `L` (all text keys), `Localization` (the service interface) / `LocalizationImpl` (picks the bundle of the session's language), `Language`, `LocalizationBundle` (texts and formats of one language) / `LocalizationBundleBase` / `LocalizationBundleEN` / `LocalizationBundleJP`, `NaturalOrderComparator`. |
| `concurrent` | `AsyncRunnableWrapper` (keeps the submitting stack trace for errors on worker threads), `ThrowingRunnable`. |
| `utils` | `AITypes` (the type of an AI as the lists show it), `UIUtils` (error dialogs, validation messages, layout and grid helpers, dialog positioning, cookies, `clientAddress`), `ClientAddressResolver` (the client address of a request: `X-Forwarded-For` only from the `trustedProxies`, see [Login throttling](services.md#login-throttling)), `ReflectUtils` (cached reflection), `GeneratedTextHighlighter` (word diff of a part against its original text, `<mark>` around the shared words), `ThreadUtils` (`executeInThread` - runs a task on a fresh thread without the caller's thread locals and waits for it). |
| `com.github.enerccio.tools` | `Pair`, `Pointer` (small holders used across the code) and `GenerateFlywayDiff` (development tool, see [Database schema changes](building.md#database-schema-changes)). |

## Resources and configuration

| Path | |
|---|---|
| `src/main/resources/META-INF/spring/application-config.xml` | Root of the Spring configuration; imports the three files below. |
| `.../spring/container-config.xml` | Localization, configuration, `applicationPoint`, `sessionManager`, session beans, application initializer. |
| `.../spring/datasources-config.xml` | Data source, Flyway, JPA, transaction manager. |
| `.../spring/services-config.xml` | Repositories, services, generation steps, extensions. |
| `src/main/resources/migration/V<n>__<name>.sql` | Flyway migrations - currently `V1__initial` to `V13__usage_ai_nullable`. |
| `src/main/resources/log4j.properties` | Logging for development (IDE, `mvn jetty:run`) - `DEBUG`. |
| `src/main/resources-release/log4j.properties` | Logging packaged into the WAR instead - `INFO` (see [Packaging & releases](packaging.md#the-war)). |
| `src/main/resources/META-INF/build-info/build-info.properties` | Version and build time, filtered by Maven. |
| `../manual/user/**/*.md` (outside `marginalia/`) | The user manual, copied by the `<resources>` of the POM to `classpath:/manual/` for the help assistant, see [Packaging & releases](packaging.md). |
| `src/main/resources/META-INF/VAADIN/config/flow-build-info.json.DEVELOPMENT` | Template of a development build info for IDE runs; never packaged into the WAR (see [Development and production mode](building.md#development-and-production-mode)). |
| `src/main/webapp/config/configuration.properties` | Application settings (saved logins). |
| `src/main/webapp/config/persistence.xml` | The persistence unit: list of entity classes, dialect, `hbm2ddl.auto=validate`. |

## Frontend (`src/main/frontend/`)

There is no hand-written frontend application - Vaadin generates it. The only sources are:

| File | |
|---|---|
| `styles/shared-styles.css` | Global CSS, imported by `AppShellConfig`: spacing helpers, utility classes, story text and Markdown wrapping, story tree. |
| `styles/reading-mode-styles.css` | The book style of the reader and editor (`.manuscript-styles .chat-message-markdown`), imported separately by `AppShellConfig`. |
| `chart-connector.js` | Draws Chart.js charts for the `ChartJs` widget (npm package `chart.js`, declared with `@NpmPackage` on `ChartJs`). |
| `graph-connector.js` | Draws Cytoscape.js graphs for the `GraphView` widget (npm package `cytoscape`, declared with `@NpmPackage` on `GraphView`). |
| `treant-connector.js` | Connects `TreantTree` to the treant.js library (npm packages `treant-js` and `raphael` are declared with `@NpmPackage` on `TreantTree`). |

Generated and ignored by Git: `src/main/frontend/generated/`, `src/main/frontend/index.html`, the development bundle
in `src/main/bundles/`, `node_modules/`, `package.json` and the Vite configuration.

## Desktop app (`src/desktop/`)

| Path | |
|---|---|
| `java/.../desktop/DesktopLauncher.java` | The launcher: options, Jetty base in `~/.marginalia/desktop`, starts Jetty in-process, browser, tray icon, single instance. JDK only. |
| `bin/marginalia`, `bin/marginalia.bat` | Start scripts of the portable folder (bundled runtime + JVM options + `MARGINALIA_JAVA_OPTS`). |

Built only by the `desktop` profile. See [Packaging & releases](packaging.md).

## Tests (`src/test/`)

Test classes mirror the areas they cover (paths relative to `src/test/java/com/github/enerccio/marginalia/`):

| Package | |
|---|---|
| `test` | Test infrastructure: `MarginaliaTestBase` (Spring context, temp data folder, login helpers), `GenerationTestBase` / `GenerationRun` (running real generations), `InferenceCollector`, `ExpectedLog`, `TestContextPostProcessor`. |
| `test.llm` | `MockLLMServer` - a fake OpenAI-compatible server with programmable scenarios. |
| `crud` | CRUD tests for every entity, built on `OwnedCrudContract` / `ExtendableCrudContract`. |
| `generation` | Generation requests and lorebook activation. |
| `templates`, `domain.templates` | Template rendering, macros, default templates. |
| `backup`, `cleanup`, `lorebook`, `db` | Book and database backups, cleanup planner, lorebook import/export, Flyway migrations. |
| `instruct` | Bytecode instrumentation (with fixture classes in `instruct.fixture`), the extension verifier and the check that every method of an `@Extendable` class declares its `@PublicAPI` (`PublicAPISignatureTest`). |
| `domain`, `domain.service` | Entity listener, token limits, cron expressions, SillyTavern conversion. |
| `manual`, `ui` (`LLMSnapshotTest`, `WorkspaceLLMSupportTest`), `ui.components`, `domain` (`HelpAssistantServiceTest`) | The manual parser and index, the help assistant (who pays, prompts, the functions, interactions), what the UI tells it and the widget that answers it. |

`src/test/resources/META-INF/spring/test-application-config.xml` is the test variant of the Spring configuration and
`macro-test.json` a lorebook fixture. See [Testing](testing.md).

## Plugins (`marginalia/plugins/`)

Each plugin is a Maven project with `bundle` packaging and the same layout:

```
plugins/<name>/
├── pom.xml          depends on io.github.enerccio:marginalia:1.3.0:classes (provided)
├── README.md        user documentation
└── src/main/java/com/github/enerccio/marginalia/extensions/<name>/
    ├── <Name>Activator.java   OSGi BundleActivator - registers the extension service
    ├── <Name>Extension.java   MarginaliaExtension - registers decorators on load, removes them on unload
    ├── model/                 data kept in entity attributes
    ├── service/               plugin logic
    └── ui/                    components added to the UI
```

| Plugin | Package | Size |
|---|---|---|
| Arc Outline | `extensions.arcoutline` | Model (arcs, chapters, the in-memory `ArcOutline`, settings, the schema of a generated arc), service (outline, prompt building), a sidebar tab, dialogs (arc, the generators of arcs and chapters on a common `GeneratorDialog`, name tools), a settings form, the name generator bridge and `arcoutline.css`. |
| Author's Note | `extensions.authorsnote` | Model, service, a sidebar panel and a generation listener. |
| Chapter Marker | `extensions.chaptermarking` | Activator and extension only - the smallest example. |
| Character Tracker | `extensions.charactertracker` | Model (characters, relationships, the in-memory `CharacterBook`), service, a sidebar tab, dialogs (character, import from the lorebook, relationship map on `GraphView`) and `charactertracker.css`. |
| Gemma 4 tools fix | `extensions.gemma4toolsfix` | Activator and extension only; registers a `ToolRoleResolver`. |
| Google Gemini | `extensions.googlegenai` | Activator and extension, `GoogleGenAiFactory` (the `ExtensionAIFactory`), the settings form, the inference service over the Google Gen AI SDK (embedded in the JAR) and helpers for the messages, the errors and the settings; has unit tests. |
| Instruction Exporter | `extensions.instructionexporter` | Activator and extension only. |
| Lore Generator | `extensions.loregenerator` | Model (the prompts), service (lore selection, prompts), a dialog split into `NameToolsDialog`, `SavePromptAsDialog`, `AttachmentPanel` and `GenerationPane`, a settings form, and `NameToolsBridge` (the Name generator and the Name provider by reflection). |
| Lorebook VCS | `extensions.lorebookvcs` | Model, service, two UI panels. |
| Name generator | `extensions.namegenerator` | The extension, the `@LLMFunction` interface and its implementation, `NameTables` (the chains and the sampling). `src/build` holds the Java programs of the build: importers of the sources of names, the tuner and the trainer, see its README. |
| Name provider | `extensions.nameprovider` | The extension, the `@LLMFunction` interface and its implementation, `NameLists` (the lists of real names and the random choice), `NameEntry` (a pair of the romanized and the original name). `src/build/ImportPopularNames.java` writes `name-lists.json` from the popular names by country dataset, see its README. |
| Reviewer | `extensions.reviewer` | Model (`ReviewerProfile` with a stable uuid), service, dialogs, a settings form and `ManuscriptProfileSelect`, the profile picker of a book's info tab. |
| Side Query | `extensions.sidequery` | Model, service, a chat panel and a settings form; `SideQueryLog` holds its logger for classes the extension class does not create. |
| Chinese localization | `extensions.chineselocalization` | Activator, extension and one `LocalizationBundle` - a language, no UI code. |

`plugins/Dockerfile` and `plugins/docker-compose.yml` build all of them in a container (JARs end up in `plugins/build/`,
ignored by Git), see [Packaging](packaging.md#building-the-plugins-in-docker).

See [Plugin development](plugins/index.md).

## Build output (`marginalia/target/`)

| Path | |
|---|---|
| `marginalia-1.3.0.war`, `marginalia-1.3.0/` | The WAR and its exploded form. |
| `marginalia-1.3.0-classes.jar` | Classes for plugin builds. |
| `classes/`, `test-classes/` | Woven classes. |
| `dev-bundle/`, `sw.ts`, `vaadin-dev-server-settings.json` | Vaadin frontend build files. |
| `surefire-reports/`, `test-home/` | Test reports and test data folders. |
| `desktop/` | Desktop distribution (with `-Pdesktop`). |

## Where to find...

| I want to change... | Look at |
|---|---|
| A text in the UI | `loc/L.java` (key) and the text in every bundle: `loc/LocalizationBundleEN.java` (required), `LocalizationBundleJP.java` |
| The default prompts | `Defaults.java` |
| What is sent to the model | `domain/service/impl/generation/impl/` (the steps), `PreparePayloadStep` for the final messages |
| How lore is activated | `ProcessLorebookStep`, `LorebookServiceImpl` |
| A macro | `domain/templates/TemplateData.java` (logic), `macros/Macros.java` (registry), `macros/MacroTranslator.java` (syntax) |
| The story editor | `ui/dialogs/manuscript/ManuscriptStoryPart.java` |
| Summaries and meta summaries | `SummaryServiceImpl` (chain, creation, unwinding), `ui/dialogs/manuscript/SummariesDialog.java`, `SummaryDialog.java` |
| The story tree operations | `ChatMessageServiceImpl` (branch, swipe, delete), `ManuscriptTreePart` (UI) |
| An entity or the schema | the entity in `domain/model/impl/`, a new Flyway migration, `persistence.xml` - see [Database & migrations](database.md) |
| A new service | interface in `domain/service/`, implementation in `impl/`, a bean in `services-config.xml` |
| The help assistant | `HelpAssistantServiceImpl`, `HelpPrompts` (prompts), `ui/components/HelpAssistantWidget.java`, `ui/LLMInteract.java` (what a part of the UI says and offers), `ui/workspace/parts/admin/HelpAssistantPanel.java` (settings) |
| What an extension can hook into | classes annotated `@Extendable` in `ui/` |
| Global styles | `src/main/frontend/styles/shared-styles.css`, `reading-mode-styles.css` |
| Login and saved logins | `ui/main/LoginCheckRoute.java`, `UserServiceImpl` |
| Backups | `BackupServiceImpl` (books), `DatabaseBackupServiceImpl` (database), `Configuration` (pending restore, copy before a migration) |
| The desktop app | `src/desktop/java/.../DesktopLauncher.java`, the `desktop` profile in `pom.xml` |
