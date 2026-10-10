---
label: Project structure
order: 970
verified: e5a49b7
covers:
  - marginalia/src
  - marginalia/plugins
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
├── .github/workflows/
│   ├── desktop.yml          CI: tests, desktop builds for 5 platforms, release upload
│   └── server.yml           CI: WAR, plugin JARs, Docker image (ghcr.io), release upload
├── Dockerfile               two-stage build: Maven builder → Jetty 12 image
├── docker-compose.yml       the server setup (port 8080, ./data volume, JVM options)
├── retype.yml               manual configuration (input manual/, output docs/)
├── README.md, LICENSE
├── TODO.md                  release plan
└── TODO.IMAGES.md           screenshots still to be made
```

| Path | Notes |
|---|---|
| `data/` | Created by `docker compose`: the data folder of the Docker server. Ignored by Git. |
| `.gitattributes` | Keeps line endings of the desktop launch scripts (`LF` for the shell script, `CRLF` for the batch file). |
| `.dockerignore` | Excludes `marginalia/target/` and `.idea/` from the Docker build context. |

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

There is one Maven module; `plugins/` are separate projects that are built on their own (there is no aggregator
POM yet). See [Building from source](building.md).

## Java packages

Application code lives in `com.github.enerccio.marginalia` (paths below are relative to
`src/main/java/com/github/enerccio/marginalia/`), plus a few helpers in `com.github.enerccio.tools`.

### Top level

| File | |
|---|---|
| `Configuration.java` | The `configuration` bean: data folder and its subfolders, database URL, staged database restore, saved login settings. |
| `Defaults.java` | Built-in prompt defaults: master template, POV, tense, style, user prompt and summary prompt - used when neither the book nor the user's settings override them - and the template of the title page of story exports. |
| `Constants.java` | Application constants: `DEAD_SESSION_CHECK_TIMEOUT` (seconds without a heartbeat before `SessionManager` closes a UI, default 120). |
| `UIConstants.java`, `SharedStyles.java` | Column widths and dialog offsets; CSS class names defined in `shared-styles.css`. |
| `SaneSQLiteDialect.java` | Hibernate's community SQLite dialect without generated `CHECK` constraints. |

### `bound` - startup and sessions

| File | |
|---|---|
| `ApplicationInitializer.java` | Runs registered `Migration`s when the stored `AppSettings` versions are behind (none are registered yet). |
| `Migration.java` | Interface of data migrations in Java (schema migrations are Flyway SQL). |
| `SessionManager.java` | The `sessionManager` bean: registry of all HTTP sessions, binds the logged-in user to them, runs code in other users' sessions (`runForUsers`) and closes dead UIs and sessions on its watcher thread. See [Sessions](architecture.md#sessions). |
| `SessionTrackingListener.java` | `HttpSessionListener` registered by `WebappApplicationInitializer`; forwards session creation and destruction to `SessionManager`. |
| `SessionInformation.java` | What `SessionManager` knows about one session: HTTP and Vaadin session, user, main UI, captured request attributes, close listeners. |
| `SessionCloseListener.java`, `RunInSession.java` | Callbacks: another session was closed; code to run for a session in `runForUsers`. |
| `ApplicationPoint.java` | The `applicationPoint` bean: application-wide maps of open UIs to their `Workspace` (only `Main`) and to a copy of their logged-in user. `Main` and `Viewer` `register` the UI after login; the entries are removed when the UI detaches. |
| `SessionPoint.java` | Empty session-scoped bean. |

### `ui` - user interface

| Package | Files |
|---|---|
| `ui.main` | `WebappApplicationInitializer` (creates the Spring context, registers the session listener), `MarginaliaServlet` (the `VaadinServlet`), `AppShellConfig` (push, theme, global CSS), `LoginCheckRoute` (login overlay, saved logins, first-start dialog), `Main` (route `/`), `Viewer` (route `/view`, published books). |
| `ui.workspace` | `Workspace` (the tabbed workspace after login, footer buttons) and `WorkspaceComponent` (interface of its tabs). |
| `ui.workspace.parts` | One class per workspace tab: `ManuscriptPart` (Books), `LorebookPart`, `UserPart` (Settings), `ProtocolPart`, `AIPart` (Inference Providers), `ResourcesPart` (Resources: lazy `Grid` over `ResourceService.findPageForUser`), `TrashPart` (Trash: the `TrashGrid` widget), `AdminPart`. |
| `ui.workspace.parts.admin` | Admin panels with their own logic: `DatabaseBackupPanel`, `CleanupPanel`. |
| `ui.dialogs` | Entity dialogs (`AIDialog`, `ProtocolDialog`, `LorebookDialog`, `UserDialog`), `ManuscriptDialog` (the book window), `LorebookImportDialog` (resolving lorebooks during restores), `PromptDialog` (shows the prompt a part was generated from) and generic dialogs: `ConfirmDialog`, `TextInputDialog`, `ListSelectDialog`, `ErrorDialog`, `ProgressBarDialog`. `ExportDialog` exports the story of a book. Threading helpers `ThreadAccessDialog` and `UIPushGuard`. |
| `ui.dialogs.manuscript` | The tabs of the book window, each a `ManuscriptDialogPart`: `ManuscriptStoryPart` (story editor - the largest UI class), `ManuscriptTreePart`, `ManuscriptInfoPart` (About), `ManuscriptPromptPart`, `ManuscriptLorebookPart`, `ManuscriptBackupPart`; and the summary dialogs: `SummariesDialog` (overview of the summaries of the branch, opened from the story editor), `SummaryDialog` (one summary or meta summary, generating or viewing) and `SummaryRemoval` (asking before a summary is removed); and `ImagesDialog` (add, caption and remove the images of a part, opened from its card). |
| `ui.components` | Larger reusable components: `LorebookView` (the lorebook editor, used in the Lorebooks tab and in books), `TreantTree` (story tree drawn with treant.js), `MessageImages` (the images of a part under its text - in the story editor, the Viewer, the image dialog and the Resources tab preview; the files are read when the browser asks for them), `ThreadCopyRequestAttributes` (carries the session to worker threads). |
| `ui.widgets` | Small widgets: `HTabSheet` (tab sheet with the tabs on the left and room for custom content, used by the workspace), `BackendTableProvider*` / `BackendTableItem` (lazy grids backed by a repository query), `TagMultiComboBox`, `TrashGrid` (restoring deleted objects, used by `TrashPart` and `CleanupPanel`), `TemplateHints` (hint popovers of prompt fields), `TextAreaPopoverComponent` / `TextFieldPopOverComponent`, `HtmlText`, `ScrollPanel`, `ResizableTextArea` (`install(loc, area, fixedHeight)` adds a corner icon that switches a text area between a fixed height and growing with its content; used by the prompt, description and lorebook fields), `Notification`, `PermissiveLoginOverlay` (allows empty passwords). |

### `domain.model` - entities

| File | |
|---|---|
| `BaseEntity`, `OwnedEntity`, `ExtendableEntity` | Mapped superclasses: id, uuid, soft delete, timestamps; owner; JSON `attributes` for extensions. |
| `Setting` | Base of settings, single table `settings` with a `DTYPE` discriminator. |
| `impl/Manuscript` | A book. |
| `impl/ChatMessage` | A story part - a node of the story tree. |
| `impl/Summary` | A summary of the story up to a part, or a meta summary of summaries. |
| `impl/Lorebook`, `impl/LorebookEntry` | Lorebooks and their entries. |
| `impl/AI`, `impl/OpenAICompatible` | Inference providers (`AI` is the base, `OpenAICompatible` the only type). |
| `impl/Protocol`, `impl/ChatCompletionProtocol` | Protocols (generation settings). |
| `impl/Tag`, `impl/TagRelation` | Tags and their assignment to objects (by class and id, optionally as a negative tag). |
| `impl/Resource` | Uploaded files stored by hash, used for the image attachments of a part (`ChatMessage.getImages()`); `clazz` + `objectId` loosely note what uses it. Listed in the Resources tab (`ResourcesPart`). |
| `impl/settings/AppSettings`, `impl/settings/UserSetting` | Installation-wide settings (versions, database backup schedule) and per-user settings. |

`User` is in `domain.security.model`. Every entity must also be listed in `src/main/webapp/config/persistence.xml`.
See [Domain model](domain-model.md).

### `domain.repository` - data access

One interface and one JPA implementation (`impl/Jpa*Repository`) per entity, built on `BaseRepository` →
`OwnedRepository` → `ExtendableRepository` (and `JpaBaseRepository` → ...). The beans are defined in
`services-config.xml` with the shared `EntityManager`.

### `domain.service` - business logic

Interfaces in `domain.service`, implementations in `domain.service.impl`:

| Service | Responsible for |
|---|---|
| `BaseService`, `OwnedService`, `ExtendableService` | Generic CRUD, soft delete, lookups restricted to the current user. |
| `ManuscriptService`, `ChatMessageService`, `SummaryService` | Books, the story tree (add, branch, swipe, delete a part and re-attach its children, word and token counts), summaries. |
| `LorebookService`, `LorebookEntryService` | Lorebooks, entries, import and export (incl. SillyTavern through `SillyTavernEntryConverter`). |
| `AIService`, `ProtocolService` | Inference providers and protocols. |
| `TagService`, `TagRelationService`, `SettingService`, `ResourceService` | Tags, settings, files. |
| `StoryGenerationService` | The generation engine (`StoryGenerationServiceImpl.GenerationEngine`). |
| `InferenceServices`, `InferenceService`, `InferenceException`, `InferenceErrors` | Choosing and calling the model API; classified failures and their localized texts. |
| `TokenizerService`, `TokenLimits` | Token counting and the context / reply limits of a generation. |
| `TemplateService` | Rendering Handlebars templates with macros. |
| `BackupService`, `DatabaseBackupService`, `CronSchedule` | Book backups (restore, clone, export); database backups and their schedule. |
| `CleanupService`, `TrashService` | Purging soft-deleted data that is no longer referenced; listing and restoring it (one bean, `CleanupServiceImpl`, shares the reference model). |
| `OsgiService`, `ExtensionService` | Loading extensions; `@Extendable` decorators. |

Also here: `TurnInput` (the four fields of the instruction panel), `SummaryNode` (a summary and the summaries it merges, for
the summaries overview), `CancellationToken`, `GenerationListener` (UI
callbacks of a generation) and `search/` (`Sorter`, `ManuscriptFilterValues` for the book lists).

| Package | |
|---|---|
| `service.impl.generation` | Engine interfaces and data: `GenerationStep`, `GenerationStepBase`, `GenerationStepType`, `GenerationController`, `Events`, `GenerationEvent`, `GenerationRequest` / `GenerationRequestType`, `GenerationProperties`. |
| `service.impl.generation.impl` | The eight steps, `PrepareForGenerationStep` ... `CleanupStep`. |
| `service.impl.generation.dto` | `LLMChatMessage`, `LLMRole`, `PrePromptData` (the prompts collected for one generation). |
| `service.impl.inference` | `OpenAICompatibleInferenceService` (openai-java client, streaming, reasoning, timeout and retries, mapping of API failures to `InferenceException`). |
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
| `domain.traits` | Annotations: `@CommonTx`, `@CommonTxReadOnly`, `@NoTx` (transactions), `@Extendable` (instrumented UI classes), `@ExtendedAttribute` (fields stored in `attributes`), `@CleanupReference(s)` (how references behave during cleanup), `@LocalizedTemplateDescription` (texts of template hints), `@SupportedAI` (provider type of an inference service). |
| `domain.collections` | Enums: `AIType`, `ProtocolType`, `ReasoningEffort`, `FilteringMode`, `InsertionMode`, `SummaryType`. |
| `domain.listener` | `ExtendableEntityListener` - serializes `attributes` and `@ExtendedAttribute` fields to `extendedContent` and back. |

### Extensions, localization, helpers

| Package | |
|---|---|
| `extensions` | `MarginaliaExtension` - the service interface every extension bundle registers. |
| `instruct` | `RuntimeInstrumentationInitializer` (installs the ByteBuddy agent), `ExtendableMethodVisitor` (the bytecode rewrite), `ExtensionServiceHolder` (static access to `ExtensionService` from instrumented code). |
| `loc` | `L` (all text keys), `Localization` / `LocalizationBase` / `LocalizationEN`, `NaturalOrderComparator`. |
| `concurrent` | `AsyncRunnableWrapper` (keeps the submitting stack trace for errors on worker threads), `ThrowingRunnable`. |
| `utils` | `UIUtils` (error dialogs, validation messages, layout and grid helpers, dialog positioning, cookies, `clientAddress`), `ClientAddressResolver` (the client address of a request: `X-Forwarded-For` only from the `trustedProxies`, see [Login throttling](services.md#login-throttling)), `ReflectUtils` (cached reflection), `ThreadUtils` (`executeInThread` - runs a task on a fresh thread without the caller's thread locals and waits for it). |
| `com.github.enerccio.tools` | `Pair`, `Pointer` (small holders used across the code) and `GenerateFlywayDiff` (development tool, see [Database schema changes](building.md#database-schema-changes)). |

## Resources and configuration

| Path | |
|---|---|
| `src/main/resources/META-INF/spring/application-config.xml` | Root of the Spring configuration; imports the three files below. |
| `.../spring/container-config.xml` | Localization, configuration, `applicationPoint`, `sessionManager`, session beans, application initializer. |
| `.../spring/datasources-config.xml` | Data source, Flyway, JPA, transaction manager. |
| `.../spring/services-config.xml` | Repositories, services, generation steps, extensions. |
| `src/main/resources/migration/V<n>__<name>.sql` | Flyway migrations - currently `V1__initial` to `V8__resource_link`. |
| `src/main/resources/log4j.properties` | Logging for development (IDE, `mvn jetty:run`) - `DEBUG`. |
| `src/main/resources-release/log4j.properties` | Logging packaged into the WAR instead - `INFO` (see [Packaging & releases](packaging.md#the-war)). |
| `src/main/resources/META-INF/build-info/build-info.properties` | Version and build time, filtered by Maven. |
| `src/main/resources/META-INF/VAADIN/config/flow-build-info.json.DEVELOPMENT` | Template of a development build info for IDE runs; never packaged into the WAR (see [Development and production mode](building.md#development-and-production-mode)). |
| `src/main/webapp/config/configuration.properties` | Application settings (`localization`, saved logins). |
| `src/main/webapp/config/persistence.xml` | The persistence unit: list of entity classes, dialect, `hbm2ddl.auto=validate`. |

## Frontend (`src/main/frontend/`)

There is no hand-written frontend application - Vaadin generates it. The only sources are:

| File | |
|---|---|
| `styles/shared-styles.css` | Global CSS, imported by `AppShellConfig`: spacing helpers, story text and Markdown styles, story tree. |
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
| `instruct` | Bytecode instrumentation (with fixture classes in `instruct.fixture`). |
| `domain`, `domain.service` | Entity listener, token limits, cron expressions, SillyTavern conversion. |

`src/test/resources/META-INF/spring/test-application-config.xml` is the test variant of the Spring configuration and
`macro-test.json` a lorebook fixture. See [Testing](testing.md).

## Plugins (`marginalia/plugins/`)

Each plugin is a Maven project with `bundle` packaging and the same layout:

```
plugins/<name>/
├── pom.xml          depends on io.github.enerccio:marginalia:1.0.0:classes (provided)
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
| Author's Note | `extensions.authorsnote` | Model, service, a sidebar panel and a generation listener. |
| Chapter Marker | `extensions.chaptermarking` | Activator and extension only - the smallest example. |
| Lorebook VCS | `extensions.lorebookvcs` | Model, service, two UI panels. |
| Reviewer | `extensions.reviewer` | Model, service, dialogs and a settings form. |
| Side Query | `extensions.sidequery` | Model, service, a chat panel and a settings form. |

See [Plugin development](plugins/index.md).

## Build output (`marginalia/target/`)

| Path | |
|---|---|
| `marginalia-1.0.0.war`, `marginalia-1.0.0/` | The WAR and its exploded form. |
| `marginalia-1.0.0-classes.jar` | Classes for plugin builds. |
| `classes/`, `test-classes/` | Woven classes. |
| `dev-bundle/`, `sw.ts`, `vaadin-dev-server-settings.json` | Vaadin frontend build files. |
| `surefire-reports/`, `test-home/` | Test reports and test data folders. |
| `desktop/` | Desktop distribution (with `-Pdesktop`). |

## Where to find...

| I want to change... | Look at |
|---|---|
| A text in the UI | `loc/L.java` (key) and `loc/LocalizationEN.java` (text) |
| The default prompts | `Defaults.java` |
| What is sent to the model | `domain/service/impl/generation/impl/` (the steps), `PreparePayloadStep` for the final messages |
| How lore is activated | `ProcessLorebookStep`, `LorebookServiceImpl` |
| A macro | `domain/templates/TemplateData.java` (logic), `macros/Macros.java` (registry), `macros/MacroTranslator.java` (syntax) |
| The story editor | `ui/dialogs/manuscript/ManuscriptStoryPart.java` |
| Summaries and meta summaries | `SummaryServiceImpl` (chain, creation, unwinding), `ui/dialogs/manuscript/SummariesDialog.java`, `SummaryDialog.java` |
| The story tree operations | `ChatMessageServiceImpl` (branch, swipe, delete), `ManuscriptTreePart` (UI) |
| An entity or the schema | the entity in `domain/model/impl/`, a new Flyway migration, `persistence.xml` - see [Database & migrations](database.md) |
| A new service | interface in `domain/service/`, implementation in `impl/`, a bean in `services-config.xml` |
| What an extension can hook into | classes annotated `@Extendable` in `ui/` |
| Global styles | `src/main/frontend/styles/shared-styles.css` |
| Login and saved logins | `ui/main/LoginCheckRoute.java`, `UserServiceImpl` |
| Backups | `BackupServiceImpl` (books), `DatabaseBackupServiceImpl` (database), `Configuration` (pending restore) |
| The desktop app | `src/desktop/java/.../DesktopLauncher.java`, the `desktop` profile in `pom.xml` |
