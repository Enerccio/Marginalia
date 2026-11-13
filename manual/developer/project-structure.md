---
label: Project structure
order: 970
verified: 3cbdfc044
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
│   ├── libs/                code that plugins share, one Maven project each (`translator-common` and `http-hooks`, embedded in the bundles that use them)
│   └── plugins/             bundled extensions, one Maven project each
├── manual/                  sources of this manual (Retype, Markdown)
│   └── images/              screenshots used by the manual and the READMEs
├── docs/                    generated manual (tools/retype_build.sh), ignored by git - a local copy for reading
├── screenshots/             scripted environment that takes the pictures of the manual (see its SCREENSHOTS.md)
├── tools/                   check_dev_docs.py (stale developer pages), annotate_ui_maps.py (labels of the UI map figures), manual_i18n.py (translates the user manual with Claude Code, see [Translating the manual](translating-the-manual.md)), retype_build.sh (builds the manual into docs/ with the version in the label and footer), plugin_api_diff.py (the plugin API changes between two git revisions), LocalLlm.java (a prompt on the dev AI), workset_run.sh (the dataset harness), gpu-control/ (the controller that starts and stops the containers that share one GPU), update-watcher/ (the host side of Admin → Update: watch.sh runs rebuild.sh when the application asks, install.sh makes it systemd units)
├── .github/workflows/
│   ├── desktop.yml          CI: tests, desktop builds for 5 platforms, release upload
│   ├── manual.yml           CI: builds the manual and publishes it to GitHub Pages
│   └── server.yml           CI: WAR, plugin JARs, Docker image (ghcr.io), release upload
├── pom.xml                  parent POM: the version of the application and the plugins, lists them as modules
├── Dockerfile               two-stage build: Maven builder → Jetty 12 image
├── docker-compose.yml       the server setup (port 8080, ./data volume, JVM options)
├── rebuild.sh, rebuild.bat  update a Docker server: pull, build the plugins and the image, copy the plugins to data/extensions, restart
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
├── src/main/resources/           Spring XML, Flyway migrations, logback, Vaadin build info
├── src/main/resources-release/   WAR-only resources: the release logback.xml, the mark of a woven build
├── src/main/resources-raw/       copied into WEB-INF/classes without filtering (binary files: the font of the PDF export); also a resource folder of the classes
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
| `Defaults.java`, `DefaultsJA.java`, `DefaultsFR.java`, `DefaultsES.java`, `DefaultsDE.java`, `DefaultsIT.java`, `DefaultsKO.java` | The built-in prompts (master template, POV, tense, style, the user, summary, redraft, annotate and impersonate prompts, the texts of the agents) as an object per prompt language, used when neither the book, its folders nor the user's settings override them; `Localization.getDefaults()` picks the one of the user's prompt language, see [Templating](templating.md#the-built-in-prompts-and-their-languages). |
| `Constants.java` | Application constants: `DEAD_SESSION_CHECK_TIMEOUT` (seconds without a heartbeat before `SessionManager` closes a UI, default 120). |
| `UIConstants.java`, `CssStyles.java` | Column widths and dialog offsets; CSS class names defined in `shared-styles.css`. |
| `SaneSQLiteDialect.java` | Hibernate's community SQLite dialect without generated `CHECK` constraints. |

### `bound` - startup and sessions

| File | |
|---|---|
| `ApplicationInitializer.java` | Runs registered `Migration`s when the stored `AppSettings` versions are behind (`bound/migration`: `EncryptApiKeysMigration`, `FulltextMigration`, `PartRevisionMigration`, `VersionHistoryPermissionMigration`, `StatisticsBackfillMigration`, `OriginalResponseMigration`, `PermissionsMigration`, see [Database & migrations](database.md)). |
| `Migration.java` | Interface of data migrations in Java (schema migrations are Flyway SQL). |
| `SessionManager.java` | The `sessionManager` bean: registry of all HTTP sessions, binds the logged-in user to them, runs code in other users' sessions (`runForUsers`) and closes dead UIs and sessions on its watcher thread. See [Sessions](architecture.md#sessions). |
| `SessionTrackingListener.java` | `HttpSessionListener` registered by `WebappApplicationInitializer`; forwards session creation and destruction to `SessionManager`. |
| `SessionInformation.java` | What `SessionManager` knows about one session: HTTP and Vaadin session, user, main UI, captured request attributes, close listeners. |
| `SessionCloseListener.java`, `RunInSession.java` | Callbacks: another session was closed; code to run for a session in `runForUsers`. |
| `ApplicationPoint.java` | The `applicationPoint` bean: application-wide maps of open UIs to their `Workspace` (only `Main`) and to a copy of their logged-in user. `Main` and `Viewer` `register` the UI after login; the entries are removed when the UI detaches. |
| `UiProblem.java`, `ProblemLog.java` | A problem the user was shown (kind, message, detail, with `clean` that makes the text safe for a model) and the last 20 of a session. Written by `utils/UiProblems`, read by the help assistant. |
| `AgentRequestAttributes.java`, `AgentSession.java`, `AgenticSessionPoint.java` | What makes the worker thread of an agent run look like the owner's session: the request attributes that resolve the `user` bean and the session scoped beans, the session object they stand on, and the `SessionPoint` of a run (the language of the run, the open book is the working copy), see [Agents](agents.md#the-request-context-of-a-run). |
| `SessionPoint.java` | Session-scoped bean; holds the `Language` of the session's UI (set from the user's settings when the workspace opens, read by `LocalizationImpl`) the conversation with the help assistant (`getHelpChat()`) and the problems the user was shown (`getProblems()`, a `ProblemLog`, see [What went wrong](ui.md#what-went-wrong-the-problem-log)). |

### `ui` - user interface

| Package | Files |
|---|---|
| `ui` | `Restorable` (`storeState()` / `restoreState(JsonObject)`, implemented by what has state worth remembering) and `RestorableState` (its defensive readers and `put`), see [User interface](ui.md#remembering-where-the-user-was); `JavascriptLibrary` and the other helpers of the package. `LLMInteract` (a part of the UI describes itself to the help assistant's model: name, Markdown state, children, and the harmless moves it offers), `LLMInteraction` (one such move) and `LLMSnapshot` (the tree of what the model reads, with its paths), `LLMFormProblems` (what is wrong with the fields of a form, never their values), see [User interface](ui.md#letting-the-help-assistant-see-the-ui). |
| `ui.main` | `WebappApplicationInitializer` (creates the Spring context, registers the session listener), `MarginaliaServlet` (the `VaadinServlet`), `AppShellConfig` (push, theme, global CSS), `LoginCheckRoute` (login overlay, saved logins, first-start dialog), `Main` (route `/`), `Viewer` (route `/view`, published books), `Reset` (route `/reset`: forgets the saved workspace state and opens an empty workspace, for a workspace that freezes the browser), `JobsView` (route `/jobs`: the jobs that run now, for administrators). |
| `ui.workspace` | `Workspace` (interface of the workspace shown after login), `DesktopWorkspace` (its tabbed implementation with footer buttons), `WorkspaceComponent` (interface of its tabs), `PartIcon` (the line drawing in front of the name of a tab) and `WorkspaceAutosave` (stores the state of the workspace in `UserSetting.workspaceState`). `WorkspaceLLMSupport` (what the workspaces and the table sections tell the help assistant: the sections by their labels, the moves to open them, the state of a table with an add button). |
| `ui.workspace.phone`, `ui.workspace.phone.parts` | `PhoneWorkspace` (the workspace for phones: an `AppLayout` with a drawer instead of the left-hand tabs) `PhoneManuscriptPart` (single column book list), `PhoneLorebookPart` (lorebook view in one column), `PhoneProtocolPart` / `PhoneAIPart` (two column protocol and model lists) `PhoneSettingsPart` (`UserPart` with a stacked header) and `PhoneTrashPart` (trash with `PhoneTrashGrid`). |
| `ui.dialogs.phone`, `ui.dialogs.phone.manuscript` | `PhoneManuscriptDialog` (the book dialog for phones: full screen, one scrolling tab row), `PhoneSummariesDialog` (`SummariesDialog` with a wide content column) and its tabs `PhoneManuscriptInfoPart`, `PhoneManuscriptPromptPart`, `PhoneManuscriptLorebookPart` (with `PhoneLorebookView`), `PhoneManuscriptStoryPart` (one column, outline in the drawer of an `AppLayout`, message cards with a header line of icons), `PhoneManuscriptTreePart`, `PhoneManuscriptBackupPart`. |
| `ui.menu` | The menus of the story editor built from one registry: `StoryMenuRegistry` (Spring bean), `StoryMenuItem`, `StoryMenuKeys`, `MenuKind`, `MenuDefinition` / `MenuNode` / `BuiltMenu` / `MenuItemContent`, `MenuGroup`, `MenuPath`, `MenuPosition`, see [User interface](ui.md#menus-of-the-story-editor). |
| `ui.shortcuts` | The keyboard shortcuts of the story editor: `ShortcutRegistry`, `ShortcutAction` / `ShortcutActions`, `KeyCombo`, `KeyBindings`, `KeyBindingsEditor`, see [User interface](ui.md#keyboard-shortcuts). |
| `ui.workspace.parts` | One class per workspace tab: `ManuscriptPart` (Books), `LorebookPart`, `UserPart` (Settings), `ProtocolPart`, `AIPart` (Inference Providers), `ResourcesPart` (Resources: lazy `TreeGrid`, a group row per `clazz` + `objectId` from `ResourceService.findGroupPageForUser`, its files from `findPageOfGroupForUser`), `TrashPart` (Trash: the `TrashGrid` widget), `AdminPart`, plus `NewBookAction` (the *Add Book* flow shared by both book lists). |
| `ui.workspace.parts.admin` | Admin panels with their own logic: `DatabaseBackupPanel`, `CleanupPanel`, `ResourceGarbagePanel` (the *Resource Files* tab: scan and delete the files nothing refers to), `HelpAssistantPanel` (the settings of the help assistant), `StatisticsPanel` (the *Statistics* tab: the switch and the retention of the [writing statistics](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize) for everybody, clearing one user or all), `JobsPanel` (the *Jobs* tab: the log of the jobs that ended), `UpdatePanel` (the *Update* tab: the status of the update watcher of a Docker server and the button that asks it to update); `UpdatePanel` (*Update*: the status of the update watcher of a Docker host and the button that asks it for an update) |
| `ui.jobs` | The lists of the [job log](domain-model.md#the-job-log-job-jobcall): `ActiveJobsGrid` (what runs now, on the page `/jobs`), `RunsGrid` (the runs of the registry below it, with Stop and End), `JobHistoryGrid` (the jobs that ended with their calls, filtered, paged and deleted by hand, in the *Jobs* tab of the Admin page), `JobFormat` (times, durations and statuses as both show them), `PolledRows` (the rows of a grid refreshed by a poll: the browser is sent a change only when the rows show something new). |
| `ui.dialogs.permissions` | `PermissionsDialog` (the permissions of a user, opened from the key button of Admin → Users), `PermissionUIFactory` / `PermissionUIFactoryImpl` and `PermissionUIComponent` (the settings of a permission type, `HelperPermissionComponent` for `HELPER`), see [Permissions dialog](ui.md#permissions-dialog). |
| `ui.dialogs.agent` | The screens of [agents](agents.md#the-screens): `AgentConsoleDialog` and its tabs, `AgentDialog` and its editors, `AgentMergeDialog` / `JsonMergeDialog` (the review of a result), the dialogs of a run (`AgentApprovalDialog`, `AgentStopDialog`, `AgentResumeDialog`, `AgentCloneDialog`). |
| `ui.dialogs.search` | The search and replace windows: `SearchReplaceDialog` (the part both share) with `ManuscriptSearchReplaceDialog` (story) and `LorebookSearchReplaceDialog` (entries), see [Books and the story](services.md#books-and-the-story). |
| `ui.exports` | `ExportSettingsEditor`, the *Exports* tab of the settings: the style and the templates of each export format of the user, see [UI](ui.md). |
| `ui.notifications` | `NotificationSettingsEditor`, the *Notifications* tab of the settings; the browser side is `DesktopNotifier` (`ui.widgets`), see [Desktop notifications](plugins/notifications.md). |
| `ui.dialogs` | Entity dialogs (`AIDialog`, `ProtocolDialog`, `LorebookDialog`, `UserDialog`), `LorebookEditorDialog` (the full-screen lorebook editor: the entries in tabs of a `TabDock`), the limits of a shared AI (`AILimitsDialog`, `AILimitEditDialog`; `AILimitInfoDialog` is the user's own view), `UsageDialog` (every user's usage), `ManuscriptDialog` (the book window), `LorebookImportDialog` (resolving lorebooks during restores), `PromptDialog` (shows the prompt a part was generated from), `ExtensionLogDialog` (the log of an extension, opened from the Admin page), `UsageDialog` (what every user used of every AI, opened by administrators from the Inference Providers tab) and generic dialogs: `ConfirmDialog`, `TextInputDialog`, `ListSelectDialog`, `ErrorDialog`, `ProgressBarDialog`. `ExportDialog` exports the story of a book. `ManuscriptCloneDialog` (clone a book), `ArchiveUploadDialog` / `ArchiveEntryNamesDialog` / `ArchiveImportRunner` (import of `.md` archives), `MultiTagDialog` (a tag on many books, lorebooks or entries), `MassChangeModelProtocolDialog` (a model and a protocol on many books), `MassChangeLanguageDialog`, `MassRenameDialog` (a language on many books), `LorebookMassEditDialog` (all entries of a lorebook in one table), `SharedImportDialog` (*Import from other user*: what other users share, to import a copy), `DescriptionDialog` (the description of a lorebook), `LoreUsageDialog` / `BookLoreUsageDialog` (the lore usage of a part and of a book), `LorebookActivationPreviewDialog` (which entries a text would use and why), `CommandProgressDialog` (the Stop window of a running command). Threading helpers `ThreadAccessDialog` and `UIPushGuard`; `BookWindows` (one window per book: the hold of a book window, *open it here and close it there*, `openHere` for plugins) |
| `ui.dialogs.bookimport` | The one import window of the Books tab and of the story menu: `BookImportDialog` (a source selector, the file upload, the name, the options and the place), `BookImportSource` (what a source reads, says and offers; `ArchiveSource`, `DocumentSource` for Word, LibreOffice, RTF, Markdown, text, EPUB, Obsidian and Scrivener, `ChatSource` for SillyTavern and LM Studio chats), `InsertionPicker` (at the end, before or after a part of the active branch, only for a book that exists), `DocumentImportRunner` (progress with Stop), and what is offered afterwards: `AfterImportDialog` (*Import finished*) and `LoreNotesAgent` (the agent that takes the lore notes), see [UI](ui.md). `CreateSummariesDialog` is in `ui.dialogs.manuscript`. |
| `ui.dialogs.statistics` | The content of the four statistics windows (`StatisticsDialog`, `GlobalStatisticsDialog` and the phone twins in `ui.dialogs` / `ui.dialogs.phone`): `StatisticsView` (the period, the totals and the tabs), `StatisticCharts` (the Chart.js configurations), `ActivityCalendarView` and `ActivityCalendarGrid` (the calendar of the days worked on, its layout is pure); the window also has the buttons that clear the statistics (see Privacy in the [domain model](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize)). See [User interface](ui.md). |
| `ui.dialogs.story` | What the desktop and the phone story tab share: `StoryHost` (implemented by both book dialogs), `StoryEditor` (the interface of the story tab, desktop and phone, that the menu callbacks get), `StoryCard` / `StoryCards`, `StoryDockLayout` (the layouts of the desktop dock), `StoryBranchSearch` (the search of the branch), `StoryAnnotations` / `CardAnnotations` / `AnnotationsPanel` / `AnnotationView` / `AnnotationDialog` / `AnnotateDialog` (the notes), `RedraftDialog`, `ImpersonateDialog` (draft the instructions), `PartMergeDialog`, `OutdatedSummariesDialog` (the question when an edit makes summaries outdated), `AutoAnnotate` (the automatic annotation and redraft of a generated part), `StoryGenerationListener`, `NewTurnForm`, `TurnDetailsForm`, `MessageMetrics`, `StoryBackup`, `StorySupport`, `StoryActivity` (the time the book is open and edited, for the statistics), and the lazy loading of long stories: `LazyBranch`, `StoryLazyLoader`, `ReaderLazyLoader`; `AutoAnnotateSteps` (the automatic annotation and redraft as steps of what follows a part, without their windows) |
| `ui.dialogs.manuscript` | The tabs of the book window, each a `ManuscriptDialogPart`: `ManuscriptStoryPart` (story editor - the largest UI class), `ManuscriptTreePart`, `ManuscriptInfoPart` (About), `ManuscriptPromptPart`, `ManuscriptLorebookPart`, `ManuscriptBackupPart`; and the summary dialogs: `SummariesDialog` (overview of the summaries of the branch, opened from the story editor), `SummaryDialog` (one summary or meta summary, generating or viewing) and `SummaryRemoval` (asking before a summary is removed); and `ImagesDialog` (add, caption and remove the images of a part, opened from its card); `GenerationHold` (what a generation of the book holds now, given to every tab of the window) |
| `ui.components` | Larger reusable components: `TagsView` (the Tags tab of the settings: tags with their usage, rename, merge, delete), `LorebookView` (the lorebook editor, used in the Lorebooks tab and in books), `PhoneLorebookView` (its phone version in one column), `LorebookPreview` (the read only Lorebook tab of the desktop story editor), `LorebookEntryEditor` (an entry in a tab of the full-screen lorebook editor), `LorebookTokens` (the token estimates of a lorebook, shared by the view and the editor), `InvalidExtensionsWarning` (the red bar of the workspace about invalid extensions), `TreantTree` (story tree drawn with treant.js), `MessageImages` (the images of a part under its text - in the story editor, the Viewer, the image dialog and the Resources tab preview; the files are read when the browser asks for them), `ThreadCopyRequestAttributes` (carries the session to worker threads). `HelpAssistantWidget` (the robot button and chat panel `Main` adds after the workspace; it answers the model's requests to read and move through the page on the page's thread)., `BuildInfoFooter` (the version text in the corner), `CommandBar` (the command line, see [Commands](commands/index.md)); `RunCorner` (the panel of the runs, on every page), `PageRuns` (the model windows of a page) and `RunsOnPage` (listeners of the runs that run in the page's request scope), `LoreChangesOnPage` (the same for `LoreChanges`) |
| `ui.widgets` | Small widgets: `ChartJs` (a Chart.js chart drawn from a JSON configuration, see [UI](ui.md)), `GraphView` (a Cytoscape.js graph, see [Custom components](custom-components.md)), `MarkdownEditor` (a React Markdown editor, see [Custom components](custom-components.md#markdowneditor)), `HTabSheet` (tab sheet with the tabs on the left and room for custom content, used by the workspace), `TabDock` (columns of tab sheets whose tabs can be dragged apart and together, the left bar of the story editor), `BackendTableProvider*` / `BackendTableItem` (lazy grids backed by a repository query), `TagMultiComboBox`, `LazyContextMenu` (a context menu filled when it opens), `LazyLoadIndicator`, `WordDiffView` (the word by word differences of the review), `TurnRow` (a row of a list of turn details with the buttons to move, merge, edit and delete it, used by the Planner and by the Converter, see [Plugin UI](plugins/ui-extensions.md)), `TrashGrid` (restoring deleted objects and deleting parts and entries for good, used by `TrashPart` and `CleanupPanel`), `PhoneTrashGrid` (its phone layout, used by `PhoneTrashPart`), `TemplateHints` (hint popovers of prompt fields), `TextAreaPopoverComponent` / `TextFieldPopOverComponent`, `HtmlText`, `ScrollPanel`, `ModelProtocolPicker` (a model and a protocol to choose from, for dialogs that run the model with a choice of their own), `ToolSelectionButton` (choosing the tools the model may use, with `ui.dialogs.ToolSelectionDialog`), `ResizableTextArea` (`install(loc, area, fixedHeight)` adds a corner icon that switches a text area between a fixed height and growing with its content; used by the prompt, description and lorebook fields), `Notification`, `PermissiveLoginOverlay` (allows empty passwords). `MergeTurnsDialog` (makes one turn of two: the Planner for two scenes, plugins that keep turn details); `FloatingWindow` (a model window that is not modal and folds into the panel of the runs) and `WindowRun` (one request of a window as a run), both for plugins too |

### `domain.model` - entities

| File | |
|---|---|
| `BaseEntity`, `OwnedEntity`, `ExtendableEntity` | Mapped superclasses: id, uuid, soft delete, timestamps; owner; JSON `attributes` for extensions. |
| `Setting` | Base of settings, single table `settings` with a `DTYPE` discriminator. |
| `impl/Manuscript` | A book. |
| `impl/Folder` | A folder of books, nested through `parent`; its settings are inherited by the books below. See [Folders](folders.md). |
| `impl/ChatMessage` | A story part - a node of the story tree. |
| `impl/Summary` | A summary of the story up to a part, or a meta summary of summaries. |
| `impl/ManuscriptStatistic`, `impl/ActivityInterval`, `impl/ManuscriptSize` | The writing statistics of a book (counts per day, stretches of time, the size at the end of a day); the book is a plain `bookUuid`, `converter/LocalDateConverter` stores the day. See [Domain model](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize). |
| `impl/Lorebook`, `impl/LorebookEntry` | Lorebooks and their entries. |
| `impl/AI`, `impl/OpenAICompatible`, `impl/ExtensionAI` | Inference providers (`AI` is the base; `OpenAICompatible` talks to an OpenAI-compatible API, `ExtensionAI` is provided by an extension). |
| `impl/Protocol`, `impl/ChatCompletionProtocol` | Protocols (generation settings). |
| `impl/Tag`, `impl/TagRelation` | Tags and their assignment to objects (by class and id, optionally as a negative tag). |
| `impl/Resource` | Uploaded files stored by hash, used for the image attachments of a part (`ChatMessage.getImages()`); `clazz` + `objectId` loosely note what uses it. Listed in the Resources tab (`ResourcesPart`). |
| `impl/MessageRevision` | A version of the text of a part (the table `message_revisions`), see [Version history](version-history.md). |
| `impl/Usage` | What a user used of an AI (prefill and generated tokens, requests, and the tokens since the last reset of their limit); an `OwnedEntity`, one per AI and user, so a shared AI has one for everybody who uses it. |
| `impl/Job`, `impl/JobCall` | The job log: a unit of work that talked to an external AI and the calls it made (tables `jobs`, `job_calls`), written when the job ends, for the administrator. See [The job log](domain-model.md#the-job-log-job-jobcall). |
| `impl/settings/AppSettings`, `impl/settings/UserSetting` | Installation-wide settings (versions, database backup schedule) and per-user settings. |

`User` and `Permission` are in `domain.security.model`, `PermissionType` in `domain.security.permissions`. Every entity must also be listed in `src/main/webapp/config/persistence.xml`.
See [Domain model](domain-model.md).

### `domain.repository` - data access

One interface and one JPA implementation (`impl/Jpa*Repository`) per entity, built on `BaseRepository` →
`OwnedRepository` → `ExtendableRepository` (and `JpaBaseRepository` → ...). The beans are defined in
`services-config.xml` with the shared `EntityManager`. `impl/StaleSaves` is the detector of saves from a copy older than the
row (logged in production, a failure in the tests, `-Dtest.staleSaves`); `JpaBaseRepository` also asks the runs of the registry
before a delete. `FolderRepository` / `JpaFolderRepository` come with `FolderScope` (the folder clause of the list queries) and `FolderModification` (the *Last modified* of the folders). `ManuscriptStatisticRepository`, `ActivityIntervalRepository` and `ManuscriptSizeRepository` (the statistics) share the queries of `impl/StatisticQueries` (the filter of a `StatisticFilter` as a `where`).

### `domain.components` - plain data and helpers

Small classes that are neither entities nor services, shared by services, generation steps and the UI. No Spring beans,
no state besides their fields.

| Class | |
|---|---|
| `TurnInput` | The four fields of the instruction panel (scene, POV character, present characters, instructions). |
| `TokenLimits` | Static helpers for the context, response and prompt budget of a generation (the smaller of protocol and provider limit). |
| `AILimit`, `AILimits`, `LimitStatus` | The limit of one user on a shared AI (strategy, reset, token limits, hard limit of the generated tokens), the JSON in `AI.limits` that holds them by user uuid, and where a user stands (limit, tokens used in the period, next reset; `block(...)` says what stops a request). See [Limits](domain-model.md#limits-of-the-users-of-a-shared-ai). |
| `PromptComparison` | Compares two stored built prompts (JSON payloads) like a prefix cache: per-message status, the first difference (message, character), the shared share of the new prompt and a `Verdict` (system message changed, story shifted, last message only...). Pure, no services; used by `PromptComparisonDialog` (*Compare Prompt* in the part menu). |
| `RevisionRow`, `RevisionData`, `RevisionOrigin`, `RevisionClearing` | The [version history](version-history.md): a version without its text (what the list reads), the whole data of a version to record, who is writing a text into a part (source, detail, agent, reasoning), what *Clear history* deletes. |
| `PromptTokenBreakdown` | Raw token counts of one built prompt: the budget, every section, what was cut and the tokens of each payload message. Filled by the generation steps, stored with the part (`ChatMessage.getTokenBreakdown()`), shown by `TokenUsageDialog`. |
| `LoreUsage`, `LoreUsageReport`, `PartLoreUsage` | The lore entries a generation used (`MessagePrompt.loreUsage`: entry uuid, tokens, pass of the scan depth), and the lore usage reports built from them (a book over a branch or all branches and a range of parts; the entries of one part). |
| `SummaryNode` | A summary and the summaries it merges, for the summaries overview. |
| `SummaryPlanner` | Where the summaries of a stretch of the story go (*Create summaries*): by tokens, chapters or parts, pure, see [Services](services.md). |
| `CronSchedule` | Cron expression parsing and next-run computation, used by the database backup schedule. |
| `StatisticCounts`, `StatisticFilter`, `StatisticReport`, `DayStatistic`, `BookStatistic`, `FolderStatistic`, `ActivityRow`, `ActivityTimes`, `TimeUnion`, `SizeGrowth`, `SizeSeries`, `PartTotals`, `BookPlacement`, `FolderPlacement` | The [writing statistics](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize): what is counted, what a read asks for and gets back, the union of the stretches of time and the size and its growth, all pure. |
| `WritingActivity`, `WritingGoal` | The days a book was worked on, the streaks and the daily goal, read from the same rows for the calendar of the statistics; pure. |

### `domain.service` - business logic

Interfaces in `domain.service`, implementations in `domain.service.impl`:

| Service | Responsible for |
|---|---|
| `BaseService`, `OwnedService`, `ExtendableService` | Generic CRUD, soft delete, lookups restricted to the current user. |
| `ManuscriptService`, `ChatMessageService`, `SummaryService` | Books, the story tree (add, branch, swipe, delete a part and re-attach its children, word and token counts), summaries. |
| `MessageRevisionService`, `EffectiveHistoryLimit` | The versions of the text of a part and the limit of the history, see [Version history](version-history.md). |
| `FolderService`, `PromptField`, `EffectiveSetting`, `FolderBookLockedException` | The folders and the settings the books inherit from them, see [Folders](folders.md). `BackupFolders` and `BackupInheritance` (`impl`) are the folder in a backup and the inherited settings in an export; `HelpBookData` with `manual/BookHelpTools` the books tools of the help assistant. |
| `ManuscriptStatisticService`, `ActivityIntervalService`, `ManuscriptSizeService`, `ActivityTracker`, `StatisticReportService`, `StatisticsPolicyService`, `UsageContext` | The writing statistics: the counts, the time and the size of a book per day, the one writer of the stretches of time, the report for the windows and the commands, the privacy switches and the pruning of old days, and the book a model request is made for. See [Services](services.md). |
| `LoreUsageService` | The lore usage reports of a part and of a book, from the records of `MessagePromptService`. |
| `LorebookService`, `LorebookEntryService` | Lorebooks, entries, sharing with other users (`findAllShared`, `importShared`, `share`), import and export (incl. SillyTavern through `SillyTavernEntryConverter`). |
| `AIService`, `ProtocolService` | Inference providers and protocols. |
| `TagService`, `TagRelationService`, `SettingService`, `ResourceService` | Tags, settings, files. |
| `AILimitService` | The per-user limits of a shared AI: the owning administrator sets them, `check` refuses a request of a user who is over their limit (called by `OpenAICompatibleInferenceService.stream`), `getStatus` is for the user's own view. |
| `UsageService` | Tokens a user used with an AI, counted by `OpenAICompatibleInferenceService` (`Usage`, one per AI and user); `findAllUsages()` lists everybody's for administrators. |
| `JobService`, `service.job`, `service.impl.job` | The job log. `service.job`: `Jobs` (the static facade that opens and ends jobs and calls and holds the running ones), `ActiveJob`, `FinishedJob`, `FinishedCall`, `JobFilter`, `JobStatus`, `JobCallType`; `JobService` / `JobServiceImpl` write ended jobs in the background and read and purge the log for administrators; `service.impl.job.JobRecorder` is the `InferenceListener` that records every request to a model as a call. See [Services](services.md). |
| `StoryGenerationService` | The generation engine (`StoryGenerationServiceImpl.GenerationEngine`). `impl/generation/CommandMacros` runs the {%{`{{command::...}}`}%} macros of a generation and keeps their answers with the part (see [Templating & macros](templating.md#macros-that-run-commands)). |
| `InferenceServices`, `InferenceService`, `InferenceSession`, `InferenceException`, `InferenceErrors` | Choosing and calling the model API; the kind of work a request belongs to (header `X-Session-ID`); classified failures and their localized texts. |
| `FunctionRegistry` | The Java interfaces the model can call as functions, compiled once (see [Function calls](services.md#function-calls)). |
| `TokenizerService` | Token counting. |
| `ManualService` | The user manual packed into the WAR (`classpath:/manual/`, its translations `classpath:/manual-translations/<language>/`), parsed once and searched in memory (BM25) for the help assistant, which reads it in the language of the user. |
| `HelpAssistantService`, `UnmeteredScope` | The help assistant: its settings (administrators), the AI it uses, who pays (`UnmeteredScope` is read by the inference services), `ask` with the manual tools and the screen. |
| `TemplateService` | Rendering Handlebars templates with macros. |
| `BackupService`, `DatabaseBackupService` | Book backups (restore, clone, export); database backups and their schedule. |
| `LoreChanges` | The static notice of the lorebook entries written, told after the commit (see [Services](services.md)). |
| `LoreImages`, `LoreMentions` | Static helpers of the lorebook entry content: `LoreImages` finds, strips, replaces and writes the image markup (`![caption](resource:<uuid>)`) of an entry; `LoreMentions` finds where a text names entries (the links between entries of the editor, *Mentioned in*). See [Services](services.md) and [UI](ui.md). |
| `service.run` (`Runs`), `AIGate`, `GenerationView` | The registry of the runs of the books: what runs, who started it, its Stop, what it holds, its live text; the refusals with the name of a run (see [Services](services.md#the-runs-and-what-they-hold)). `AIGate` (`AIGateImpl`) is the queue of the connections of an AI. `GenerationView` is what the story shows of a generation a command started (the command bar's `progressInStory` commands). |
| `UpdateService` | Admin → Update: reads the status the update watcher of a Docker host writes and asks it for an update (files in `<data>/update`). |
| `CleanupService`, `TrashService` | Purging soft-deleted data that is no longer referenced; listing, restoring and (parts, entries) permanently deleting it (one bean, `CleanupServiceImpl`, shares the reference model). |
| `ToolService` | Suites of tools (functions, items to check, commands) that extensions offer to the model, and the user's `ToolSelection` turned into functions and hints (in memory, removed with the bundle). |
| `IPCService` | Providers of text about a book or a story part that extensions register for each other (in memory, removed with the bundle). |
| `OsgiService`, `ExtensionService` | Loading, stopping and starting extensions (the `.STOPPED` flag), the logger of an extension (`createLogger`, implemented by the package-private `ExtensionLogger`) and its log file; `@Extendable` decorators. |

Also here: `CancellationToken`, `GenerationListener` (UI
callbacks of a generation) and `search/` (`Sorter`, `ManuscriptFilterValues` for the book lists; `FulltextQuery`, `FulltextHit` and `LikePatterns` for the full-text search and `LIKE` patterns).

| Package | |
|---|---|
| `service.impl.generation` | Engine interfaces and data: `GenerationStep`, `GenerationStepBase`, `GenerationStepType`, `GenerationController`, `Events`, `GenerationEvent`, `GenerationRequest` / `GenerationRequestType`, `GenerationProperties`. |
| `service.impl.generation.impl` | The eight steps, `PrepareForGenerationStep` ... `CleanupStep`. |
| `service.impl` (generation) | `AfterPartPhase`: what follows a generated part, the stages of `generation/AfterPart` (the binding API of the plugins, see [After the part](plugins/after-part.md)) run as one phase on the server. |
| `service.impl.generation.dto` | `LLMChatMessage` (role, text and optional `LLMAttachment`s: images, audio, files; the OpenAI compatible provider sends them as content parts of a user message, audio only as wav/mp3; `LLMToolCall`s of an assistant message and the `toolCallId` of a `TOOL` result), `LLMRole`, `PrePromptData` (the prompts collected for one generation). |
| `service.function` | `FunctionDefinition`, `FunctionSet`, `FunctionCompiler` (JSON schema of the parameters, validation of the asynchronous method shape, argument parsing, method handles), `FunctionContinuation` (how a function answers), `FunctionCallException`; the annotations `@LLMFunction` / `@LLMParam` are in `domain.traits`. |
| `service.manual` | The manual and the help assistant: `ManualParser`, `ManualIndex`, `ManualPage` / `ManualSection` / `ManualHit`, the functions `ManualTools`, `HelpTools`, `InteractiveHelpTools` and `AdminHelpTools` with their implementations, `UiStateSource` (how the model reads and moves through the screen and reads the problems), `AdminDataSource` (where it reads the data of the Admin page; implemented by `HelpAdminData` in `domain.service.impl`), `HelpPrompts` (the default prompts and the rules added for each set of tools). |
| `service.impl.inference` | `OpenAICompatibleInferenceService` (openai-java client, streaming, reasoning, timeout and retries, mapping of API failures to `InferenceException`); `GatedInferenceService` (around every provider: the queue of the AI's connections, the live text of the run a request is bound to) and `ObservedInferenceService`. |
| `service.structured` | `StructuredOutputs` (JSON schema of a class from `@LLMSchema` / `@LLMField`, validation and parsing of a model's answer), `SchemaCheck`, `StructuredOutputException`; used by `InferenceService.infer`. `PartialJson` reads the unfinished JSON of a streamed answer into a Jackson tree (for showing a `stream(..., schema, ...)` answer while it arrives). |
| `service.agent`, `service.agent.jsonmerge` | Everything agents need that is not a service of its own: `CommandFunctions` (the commands as functions of the model), the virtual file system (`VirtualFileSystem`, `VirtualPath`, the mounts, `WorkspaceLimits`, `ZipExtractor`), the tools of the model (`RunTools`, `AgentFileTools`, `CheckpointTools`, `SubAgentTools`, `ImageTools`, `SyncTools`), checkpoints, the merge of a result (`MergePlan`, `MergeDiff`) and the JSON merge of extension data, `AgentDiagnostics`, `HandleTranslator`. See [Agents](agents.md). |
| `service.command` | The model of [commands](commands/index.md): `CommandDefinition`, `CommandContext`, `CommandScope`, `CommandRun`, `CommandLine`, `CommandHelp`, `CommandText`, the parameter and return definitions. |
| `service.impl.commands` (+ `lorebook`, `manuscript` with `read` / `write`) | The commands of the application: read commands (`ListPartsCommand`, `GetPartCommand`, `SearchLoreCommand`...), write commands (`PartEditCommands`, `LoreEntryWriteCommands`, `StoryGenerationCommands`...), `SearchReplace*Command`, `BackupCommands`, with their support classes. |
| `service.impl.commands.manuscript.read` (statistics) | `GetBookStatisticsCommand`, `GetStatisticsCommand` and `StatisticJson` (how a report becomes JSON): the commands `get-book-statistics` and `get-statistics`. |
| `service.search.replace` | `TextSearch` / `EntityTextSearch` (finding and replacing text, plain or by regular expression), `TextHit`, `TaskProgress`; used by `ManuscriptReplaceService` and `LorebookReplaceService`. |
| `service.impl.inference.tokenizer` | `TokenizerStrategy` and its implementations: OpenAI SDK, llama.cpp, LiteLLM, LiteLLM Anthropic, JTokkit. |

See [Services](services.md) and [Generation pipeline](generation-pipeline.md).

### `domain.templates` - prompts

| File | |
|---|---|
| `TemplateData` | Base of all template data; implements the SillyTavern-compatible macros. |
| `MasterTemplateData`, `UserPromptData`, `LorebookTemplateData`, `SummaryTemplateData`, `MetaSummaryTemplateData`, `RedraftTemplateData` | The variables available in each kind of template (listed in the UI hints). |
| `ExportHeaderTemplateData` | The variables (`title`, `author`) of the title page template of story exports. |
| `TemplateContext` | What macros are evaluated against: POV character, model, story so far, variables. One per generation. |
| `TemplateVariables` | Storage of `setvar` / `getvar` variables (local per branch, global per book). |
| `MomentFormat` | moment.js date formats → `DateTimeFormatter` for `{{datetimeformat}}`. |
| `macros/MacroTranslator` | Rewrites SillyTavern macro syntax into Handlebars. |
| `macros/MacroHelpers`, `macros/Macros` | Handlebars helpers for the macros; registry of supported and ignored macros. |
| `CommandMacroRunner` | What the {%{`{{command::...}}`}%} macro calls to run a command (set in the `TemplateContext` by the generation, implemented by `CommandMacros`). |

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
`model/Permission` with `permissions/PermissionType` (what an administrator allowed a user to do), `repository/PermissionRepository` (+ `impl/JpaPermissionRepository`) and `service/PermissionService` (+ `impl/PermissionServiceImpl`, see [Permissions](services.md#permissions)),
`AdminGuard` (`requireAdmin()`, used by the administrator-only services, see [Services](services.md#administration)) and
`PersistedLoginInfo` (a saved login; the user's saved logins are stored as delimited text in `User.savedLogins`).

### `domain.traits`, `domain.collections`, `domain.listener`

| Package | |
|---|---|
| `domain.traits` | Annotations: `@CommonTx`, `@CommonTxReadOnly`, `@NoTx` (transactions), `@Extendable` (instrumented UI classes) with `@PublicAPI`, `@APIReturn`, `@APIParameter`, `@APILocals` (the signature of each of their methods that plugins see), `@LLMSchema` / `@LLMField` (describe a class the model answers with, see `StructuredOutputs`), `@ExtendedAttribute` (fields stored in `attributes`), `@Fulltextable` (fields whose text goes into `_fulltext`), `@CleanupReference(s)` (how references behave during cleanup), `@LocalizedTemplateDescription` (texts of template hints), `@SupportedAI` (provider type of an inference service). `@Command` with `CommandDataSeverity` (what a command does to existing data, see [Commands](commands/index.md#data-severity)). |
| `domain.collections` | Enums: `AIType`, `ProtocolType`, `ReasoningEffort`, `LimitStrategy`, `ResetStrategy`, `FilteringMode`, `InsertionMode`, `ActivationContext`, `SummaryType`, `RevisionSource`, `RevisionDetail` (the sources and the named details of a version of a part), `ActivityKind`, `ActivityMetric`, `StatisticRange`, `StatisticSide` (the [writing statistics](domain-model.md#writing-statistics-manuscriptstatistic-activityinterval-manuscriptsize)). |
| `domain.listener` | `ExtendableEntityListener` - serializes `attributes` and `@ExtendedAttribute` fields to `extendedContent` and back, and collects the `@Fulltextable` fields into `_fulltext`. |

### Extensions, localization, helpers

| Package | |
|---|---|
| `extensions` | `MarginaliaExtension` - the service interface every extension bundle registers; `ExtensionHasCss` and `ExtensionHasLocalization` - optional interfaces of an extension (its styles, its languages); `ExtensionAIFactory` and `ExtensionAIForm` - an inference provider brought by an extension and its settings form; `InheritedSettingRegistry` (Spring bean), `InheritedSetting` and `InheritedSettingValues` - the settings of an extension that books and folders inherit (the core registers the templates of the exporters there too, `ExportInheritedSettings`), see [Inherited settings](plugins/inherited-settings.md); `SharedGate` - the gates of a GPU that several extensions share, which the plugins reach through `GpuGate` of the `http-hooks` library. |
| `instruct` | `BuildTimeWeaver` (puts the hooks into the `@Extendable` classes at build time and marks them `@Woven`), `RuntimeInstrumentationInitializer` (installs the ByteBuddy agent for unwoven classes in development runs), `ExtendableWeaving` (what both instrument), `ExtendableMethodVisitor` (the bytecode rewrite), `ExtensionServiceHolder` (static access to `ExtensionService` from instrumented code); `instruct.verify`: `ExtensionVerifier` (checks an extension's decorators against the application before it is started, `ExtensionService.verifyExtension`), `ContextInterpreter` and `ContextValue` (its bytecode data-flow analysis), `ExtensionVerification` (the result). |
| `loc` | `L` (all text keys), `Localization` (the service interface) / `LocalizationImpl` (picks the bundle of the session's language), `Language`, `LocalizationBundle` (texts and formats of one language) / `LocalizationBundleBase` / `LocalizationBundleEN` / `LocalizationBundleJP` / `LocalizationBundleFR` / `LocalizationBundleES` / `LocalizationBundleDE` / `LocalizationBundleKO` / `LocalizationBundleIT`, `NaturalOrderComparator`. |
| `concurrent` | `AsyncRunnableWrapper` (keeps the submitting stack trace for errors on worker threads), `ThrowingRunnable`. |
| `utils.docimport` | Reading a finished manuscript: `DocumentFormat` (by the extension of the file; `DOCX`, `ODT`, `RTF`, `MARKDOWN`, `TEXT`, `EPUB`, the zips of an Obsidian vault and of a Scrivener project) with a reader for each into a `TextDocument` of `TextBlock`s (a heading level and a text), and `PartSplitter`, which cuts the document into the texts of the parts by the chapters and a size, see [Services](services.md). |
| `utils` | `AITypes` (the type of an AI as the lists show it), `UiProblems` (writes down what the user was shown that went wrong: the session's problem log and the last three failures in the browser's session storage, for the help assistant), `UIUtils` (error dialogs, validation messages, layout and grid helpers, dialog positioning, cookies, `clientAddress`), `ClientAddressResolver` (the client address of a request: `X-Forwarded-For` only from the `trustedProxies`, see [Login throttling](services.md#login-throttling)), `ReflectUtils` (cached reflection), `GeneratedTextHighlighter` (word diff of a part against its original text, `<mark>` around the shared words), `ThreadUtils` (`executeInThread` - runs a task on a fresh thread without the caller's thread locals and waits for it)., `BuildInfo` (version, commit and build time read from `git.properties`, see [Building](building.md)), `CommandUtils` (what commands read from the session), `MarkdownArchive` (reads the `.md` files of a zip), `TextDiff` / `WordDiff` (differences of two texts), `TimeSafeText` / `RegexTimeoutException` (regular expressions that stop after a time limit, see [Regular expressions](../user/regular-expressions.md)). `WordCounter` (the word count of every part: a run of letters and digits is a word, every Han, hiragana or katakana character is one too), `TurnDetailsMerge` (joins the turn details of two turns that become one, what `MergeTurnsDialog` suggests). |
| `com.github.enerccio.tools` | `Pair`, `Pointer` (small holders used across the code) and `GenerateFlywayDiff` (development tool, see [Database schema changes](building.md#database-schema-changes)). |

## Resources and configuration

| Path | |
|---|---|
| `src/main/resources/META-INF/spring/application-config.xml` | Root of the Spring configuration; imports the three files below. |
| `.../spring/container-config.xml` | Localization, configuration, `applicationPoint`, `sessionManager`, session beans, application initializer. |
| `.../spring/datasources-config.xml` | Data source, Flyway, JPA, transaction manager. |
| `.../spring/services-config.xml` | Repositories, services, generation steps, extensions. |
| `src/main/resources/migration/V<n>__<name>.sql` | Flyway migrations - currently `V1__initial` to `V25__lorebook_protocol_sharing`. |
| `src/main/resources/logback.xml` | Logging for development (IDE, `mvn jetty:run`) - `DEBUG`. |
| `src/main/resources-release/logback.xml` | Logging packaged into the WAR instead - `INFO` (see [Packaging & releases](packaging.md#the-war)). |
| `src/main/resources-release/META-INF/build-info/woven.properties` | Only in the WAR: says that the build wove the `@Extendable` classes, so no agent is installed at start (see [`@Extendable`](plugins/extendable.md)). |
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
| `chart-connector.js` | Draws Chart.js charts for the `ChartJs` widget (npm package `chart.js`, declared with `@NpmPackage` on `ChartJs`). |
| `graph-connector.js` | Draws Cytoscape.js graphs for the `GraphView` widget (npm package `cytoscape`, declared with `@NpmPackage` on `GraphView`). |
| `markdown-editor.tsx` | The React Markdown editor of the `MarkdownEditor` widget (npm packages `@uiw/react-md-editor` and `rehype-sanitize`, declared with `@NpmPackage` on `MarkdownEditor`): our toolbar with localized tooltips, the debounced value, the mode reported to the server. |
| `treant-connector.js` | Connects `TreantTree` to the treant.js library (npm packages `treant-js` and `raphael` are declared with `@NpmPackage` on `TreantTree`). |

Generated and ignored by Git: `src/main/frontend/generated/`, `src/main/frontend/index.html`, the development bundle
in `src/main/bundles/`, `node_modules/`, `package.json` and the Vite configuration.

## Desktop app (`src/desktop/`)

| Path | |
|---|---|
| `java/.../desktop/DesktopLauncher.java` | The launcher: options, Jetty base in `~/.marginalia/desktop`, starts Jetty in-process, browser, tray icon (`marginalia.png`, loaded from the launcher jar), single instance. JDK only. |
| `java/.../desktop/BundledPlugins.java` | Copies the plugin JARs of a full build (`plugins/` next to `server/`) into the extensions folder, once. JDK only. |
| `installer.xml` | Ant targets that make the installers (`plain`, `full`) with jpackage, called from the `desktop` profile. |
| `icons/marginalia.{icns,ico,png}` | The desktop app icon: `--icon` of jpackage (per OS) and the tray icon. Made from `src/main/webapp/icons/icon.svg`. |
| `bin/marginalia`, `bin/marginalia.bat` | Start scripts of the portable folder (bundled runtime + JVM options + `MARGINALIA_JAVA_OPTS`). |

Built only by the `desktop` profile. See [Packaging & releases](packaging.md).

## Tests (`src/test/`)

Test classes mirror the areas they cover (paths relative to `src/test/java/com/github/enerccio/marginalia/`):

| Package | |
|---|---|
| `test` | Test infrastructure: `MarginaliaTestBase` (Spring context, temp data folder, login helpers), `GenerationTestBase` / `GenerationRun` (running real generations), `InferenceCollector`, `ExpectedLog`, `TestContextPostProcessor`, `TestUserService` (cheap password hashing). |
| `test.llm` | `MockLLMServer` - a fake OpenAI-compatible server with programmable scenarios. |
| `crud` | CRUD tests for every entity, built on `OwnedCrudContract` / `ExtendableCrudContract`. |
| `generation` | Generation requests and lorebook activation; the macros of the story (`ClosestMacroTest`: `closest-message`, `closest-summary`, `current-message`; `CommandMacroTest`: {%{`{{command::...}}`}%} and the kept answers). |
| `templates`, `domain.templates` | Template rendering, macros, default templates. |
| `backup`, `cleanup`, `lorebook`, `db` | Book and database backups, cleanup planner, lorebook import/export, Flyway migrations. |
| `instruct` | Bytecode instrumentation (with fixture classes in `instruct.fixture`), the extension verifier and the check that every method of an `@Extendable` class declares its `@PublicAPI` (`PublicAPISignatureTest`). |
| `jobs` | The job log: `JobsTest` (jobs and calls without the application) and `JobServiceTest` (the log is written, read and purged by an administrator only, requests to a model are calls). |
| `domain.agents`, `ui.dialogs.agent`, `ui.workspace.parts` | Agents: services, runner, tools, checkpoints, merge, console and review dialogs (`AgentRunnerTest`, `AgentMergeTest`, `AgentConsoleDialogTest`...), see [Testing](testing.md). |
| `statistics`, `ui.dialogs.statistics`, `crud` | The writing statistics: counting, the time, the size, the report, the backfill, the calendar and the windows (`StatisticReportServiceTest`, `StatisticsPolicyTest`, `ActivityTrackerTest`, `WritingActivityTest`, `StatisticsViewTest`...), see [Testing](testing.md). |
| `command` | The command service, the command line and help, commands as functions, the read and write commands of the application (`*CommandsTest`). |
| `domain`, `domain.service` | Entity listener, token limits, cron expressions, SillyTavern conversion. |
| `manual`, `ui` (`LLMSnapshotTest`, `WorkspaceLLMSupportTest`, `WorkspacePartsLLMTest`), `ui.components`, `bound` / `utils` (`ProblemLogTest`, `UiProblemsTest`), `domain` (`HelpAssistantServiceTest`, `HelpAdminDataTest`) | The manual parser and index, the help assistant (who pays, prompts, the functions, interactions), what the UI tells it and the widget that answers it. |

`src/test/resources/META-INF/spring/test-application-config.xml` is the test variant of the Spring configuration and
`macro-test.json` a lorebook fixture. See [Testing](testing.md).

## Plugins (`marginalia/plugins/`)

Each plugin is a Maven project with `bundle` packaging and the same layout:

```
plugins/<name>/
├── pom.xml          depends on io.github.enerccio:marginalia:1.8.0:classes (provided)
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
| Anthropic Claude | `extensions.anthropic` | Activator and extension, `AnthropicFactory` (the `ExtensionAIFactory`), the settings form, the inference service over the Anthropic Java SDK (embedded in the JAR) and helpers for the messages, the errors and the settings; has unit tests and a test against a fake Messages API. |
| Arc Outline | `extensions.arcoutline` | Model (arcs, chapters, the in-memory `ArcOutline`, settings, the schema of a generated arc and `GeneratedMappedStoryArc` for the import of a story, whose chapters say the message they begin in; `OutlineNode.part` links an arc or chapter to a part of the active branch), service (outline, prompt building, `mapPart`), a sidebar tab, dialogs (arc, the generators of arcs and chapters on a common `GeneratorDialog`, `StoryImportDialog`, `PlotWizardDialog`), the plot wizard (`WizardChat` with its `WizardMessage`s and `WizardCall`s and the `WizardOptions` of the chats of a book, kept in the attributes of the book; `WizardContext` builds the request of a message: what the model sees, earlier tool calls with their results, the summary of a long chat; `PlotWizardTemplateData` for its prompt), a settings form, `LegacyNameTools` (reads the name categories saved before the tool dialog) and `arcoutline.css`. |
| Audiobook | `extensions.audiobook` | `AudiobookExtension` (menu items, the settings panel, the viewer's player and the export decorators), `label` (`PieceSplitter` splits the text in code, `LabelPrompt` is the instruction template and `PieceLabeler` / `LabelService` ask the model for the speaker and the delivery of each piece, `ScriptFromLabels` makes the script), `script` (the entries of a part script, the spoken form, the cache keys), `queue` (`ConversionQueue`, a worker thread with the request context of the user, `AudioStore`, `LabelTrace`, what the model said while it labelled a part, shown live by the Convert window), `service` (`AudiobookService`, the Chatterbox backend, `RecoveringBackend` that starts a speech server again when it is gone or every few pieces and makes the piece again, the batch with the requests around it, `ResourceAudioStore` that keeps the audio as resources of the part), `audio` (the joiners for mp3 and wav, ID3 tags, `Ffmpeg` for m4b and the levelling of the loudness), `studio`, `export` (mp3 with chapters, m4b, a zip), `commands`, `model` (backends, actors, the cast, the settings) and `ui` (the settings form, the convert dialog, the studio, the player). Embeds the `http-hooks` library. |
| Author's Note | `extensions.authorsnote` | Model, service, a sidebar panel, a generation listener and a macro command (`authors-note`). |
| Beta Readers | `extensions.betareaders` | Model (`BetaChat` with its `BetaMessage`s and `ChatPersona`s, the `Persona` library and the `Medium`s, the settings and the data a book keeps, the kinds of a message: said, scene, presence), service (`BetaReadersService` with the store, `ChatRunner` that asks the model, `PromptBuilder` for the system prompt and the story it sends, `PartsTracker` for what the personas have read, `ReplyReader` / `BetaSchemas` / `TextFormat` for the answer in a schema or as text, `PersonaWriter`, `BetaReadersInherited` for the settings a book and a folder inherit), UI (the tab `BetaReadersView`, `ChatPanel` and its message cards, the dialogs of a new chat, of its settings, of the persona and medium libraries and of who is present). |
| Chapter Marker | `extensions.chaptermarking` | Activator and extension only - the smallest example. |
| Character Tracker | `extensions.charactertracker` | Model (characters, relationships, the in-memory `CharacterBook`), service, a sidebar tab, dialogs (character, import from the lorebook, relationship map on `GraphView`) and `charactertracker.css`. |
| Gemma 4 tools fix | `extensions.gemma4toolsfix` | Activator and extension only; registers a `ToolRoleResolver`. |
| Google Gemini | `extensions.googlegenai` | Activator and extension, `GoogleGenAiFactory` (the `ExtensionAIFactory`), the settings form, the inference service over the Google Gen AI SDK (embedded in the JAR) and helpers for the messages, the errors and the settings; has unit tests. |
| Image Generator | `extensions.imagegenerator` | Model (`ImageSettings`, `BackendConfig` with its `HttpHook`s), service (`ImageBackend` with `ComfyUiBackend` and `OpenAiImagesBackend`, `WorkflowTemplate`, `ImageSession` that holds a backend between images, `HookRunner`, `UrlGuard`, `ImageGeneratorService`), the window (`ImageGeneratorDialog`), `ImageSettingsForm` (the panel of the extension settings) and `image-generator.css`; has unit tests with fake ComfyUI and OpenAI servers. |
| Instruction Exporter | `extensions.instructionexporter` | Activator and extension only. |
| Knowledge Tracker | `extensions.knowledgetracker` | Model (`KnowledgeState` in the book's attributes: tracked characters, items, people met, the cursor and the processed list), `scene` (`PresentCharacters`, the lenient reading of the present characters field, and `SeparatorText`), `block` (`KnowledgeBlockBuilder` and `PayloadInsertion`), `process` (`BranchProcessor`, the cursor and the "has met" pass over the active branch; pure, no Spring), `extract` (the request to the model and its numbering, the checking of the answer into a change set, applying reviewed changes, `RunCommit`, the log of automatic changes with its revert, and the tidy pass: `TidyPrompt`, `TidyAnswer`, `TidyChangeSetBuilder`, `TidyTrigger`; pure, no Spring), services (state, prompt, processing, `ExtractionService` that asks the model, `ExtractionRun` / `ExtractionRuns`, one worker-thread run per book), a non-modal draggable progress panel and the review dialog, `KnowledgeTrackerCommands` (including a macro command and commands that ask, which are `async`), a sidebar tab and two dialogs, the mass changes window (`MassChangesDialog`) and the export and import as JSON (`KnowledgeTransfer`). The extension also listens to `BEFORE_PREPARE_CONTENT`, `AFTER_PREPARE_PAYLOAD` and `AFTER_CLEANUP`. |
| Prompt Inspector | `extensions.promptinspector` | Activator, extension (the menu item, the `BEFORE_INFERENCE` listener), the dialog, `GenerationQuestion` (asks through `accessUI` and releases the generation exactly once; tested with a fake view) and localization. |
| Book Description Generator | `extensions.bookdescription` | Model (the settings, `OutputFormat`, the answer in a schema, the template data of the two prompts), service (`BookDescriptionService` with the notes pipeline, `StoryPlanner` for the pieces of a range and their division, `AnswerParser` for JSON or Markdown answers, `BookDescriptionInherited` for the two prompts a book and a folder inherit), UI (`BookDescriptionDialog`, the settings form). |
| Lore Generator | `extensions.loregenerator` | Model (the prompts), service (lore selection, prompts), a dialog split into `SavePromptAsDialog`, `AttachmentPanel` and `GenerationPane`, a settings form, and `LegacyNameTools` (reads the name categories and hints saved before the tool dialog). |
| Lorebook VCS | `extensions.lorebookvcs` | Model, service, two UI panels. |
| Manga Generator | `extensions.mangagenerator` | Model (mangas, sections, the user's defaults), service (requests: story text, activated lore fitted to the context, previous script; streaming), the window (`MangaDialog`), a card per section, `MangaSettingsForm` (the panel of the extension settings), `TextEditDialog` and `manga.css`; has unit tests. |
| Name generator | `extensions.namegenerator` | The extension, the `@LLMFunction` interface and its implementation, `NameGeneratorToolProvider` (the suite for `ToolService`), `NameTables` (the chains and the sampling; a chain is packed in arrays, `NameTables.Chain`, and read token by token, the tables of all the categories are 12 MB in memory). `src/build` holds the Java programs of the build: importers of the sources of names, the tuner and the trainer, see its README. |
| Name provider | `extensions.nameprovider` | The extension, the `@LLMFunction` interface and its implementation, `NameProviderToolProvider` (the suite for `ToolService`), `NameLists` (the lists of real names and the random choice), `NameEntry` (a pair of the romanized and the original name). `src/build/ImportPopularNames.java` writes `name-lists.json` from the popular names by country dataset, see its README. |
| Place provider | `extensions.placeprovider` | The extension, the `@LLMFunction` interface and its implementation, `PlaceIndex` (the places read from `places.json`: search, nearby, distance), the records of the answers, `PlaceProviderToolProvider` (the suite for `ToolService`) and `PlaceProviderCommands`. `src/build` holds the two Java programs that download Natural Earth and reduce it to `places.json` (profile `import-places`), see its README. |
| Reviewer | `extensions.reviewer` | Model (`ReviewerProfile` with a stable uuid), service, dialogs, a settings form and `ManuscriptProfileSelect`, the profile picker of a book's info tab. |
| Side Query | `extensions.sidequery` | Model, service, a chat panel and a settings form; `SideQueryLog` holds its logger for classes the extension class does not create. |
| Conlang Translator | `extensions.conlangtranslator` | Model (`Language` with its rule sections, categories, registers, the lexicon of `LexiconEntry`s and the `NameCategory`s offered to the Name generator, the settings of a book and of a user), `store` (`ConlangStore`, the library of languages in the user settings), `diff` (`LanguageDiff`, `ChangeSet`, `Conflict`: the comparison and merge of two versions of a language), `designer` (the AI designer: prompts, the answer, `Proposal` and its builder), `translate` (prompt, schema and checks of a translation, the quick translation), `service` (translation runs, the designer, the library, `NameGeneratorBridge`, `LanguageAccess` for agent runs), `commands`, `text` (`ConlangKind`, its passage kind) and `ui` (the editor with the lexicon and names tabs, the designer and its wizard, the review of changes, the book view). Shares [`translator-common`](plugins/libraries/translator-common.md). |
| Real Language Translator | `extensions.reallangtranslator` | Model (`RealLanguage` with its `Script`, the `Presets`, the glossary of `GlossaryEntry`s merged from the language, the folders and the book by `Glossary`, the extraction prompts and the settings of a book and of a user), `translate` (prompt, schema and checks of a translation, `ScriptCheck`, `TermExtraction`, the quick translation), `service` (translation and glossary services, the word extraction and its runs, the library, `RealInherited`), `commands`, `ui` (the editor, the book view with the glossary and extraction panels) and `ReallangKind`. Shares [`translator-common`](plugins/libraries/translator-common.md). |
| Prompt History | `extensions.prompthistory` | Activator, extension (an `InferenceListener` and a panel in the user settings), `PromptHistory` (where the histories are: the session's plugin data, the latest runs of agents), `HistoryStore` (the last requests of one session), `Packing` (a request as gzipped JSON) and `PromptHistoryDialog`. |
| Converter (`sillytavern-converter`) | `extensions.stconverter` | Model (the chat, the turn items, the schema of the answer, and `BookSource`, the parts of a book to redraft), service (storage in the attributes of the book, the prompt, the pure functions that read and merge the answer, `BookSourceReader` that strips a backup or a book to a `BookSource` resource), `ConverterView` (the tab, its turn details are `TurnRow`s that can be moved) and its CSS; `STConverterLog` holds the logger. |
| Story Notes (`story-notes`) | `extensions.storynotes` | Activator, extension (the sidebar tab and the commands), model (`NoteBook` of `NotePage`s, `StoryNotesData` kept in the attributes of the book), `StoryNotesService` (read-change-store `update` under a lock), `StoryNotesCommands` (the commands agents and the plot wizard of Arc Outline use), `StoryNotesView` (the tab), `NotePageDialog`, `StoryNotesScripts` and `StoryNotesStyles` with `storynotes.css`. |
| Read Aloud (`tts-reader`) | `extensions.ttsreader` | Activator, extension (one decorator of `Viewer.renderManuscriptReadingView`), the control component `TtsControl`, `tts-reader.js` (the reading, in the browser) and `tts-reader.css`. |
| Chinese localization | `extensions.chineselocalization` | Activator, extension and one `LocalizationBundle` - a language, no UI code. |

`plugins/Dockerfile` and `plugins/docker-compose.yml` build all of them in a container (JARs end up in `plugins/build/`,
ignored by Git), see [Packaging](packaging.md#building-the-plugins-in-docker).

See [Plugin development](plugins/index.md).

## Build output (`marginalia/target/`)

| Path | |
|---|---|
| `marginalia-1.8.0.war`, `marginalia-1.8.0/` | The WAR and its exploded form. |
| `marginalia-1.8.0-classes.jar` | Classes for plugin builds. |
| `classes/`, `test-classes/` | Woven classes. |
| `dev-bundle/`, `sw.ts`, `vaadin-dev-server-settings.json` | Vaadin frontend build files. |
| `surefire-reports/`, `test-home/` | Test reports and test data folders. |
| `desktop/` | Desktop distribution (with `-Pdesktop`). |

## Where to find...

| I want to change... | Look at |
|---|---|
| A text in the UI | `loc/L.java` (key) and the text in every bundle: `loc/LocalizationBundleEN.java` (required), `LocalizationBundleJP.java`, `LocalizationBundleFR.java`, `LocalizationBundleES.java`, `LocalizationBundleDE.java`, `LocalizationBundleKO.java`, `LocalizationBundleIT.java` |
| The default prompts | `Defaults.java` (and its translations `DefaultsJA`, `DefaultsFR`, `DefaultsES`, `DefaultsDE`, `DefaultsIT`, `DefaultsKO`; the Chinese one is in the `chineselocalization` plugin) |
| What is sent to the model | `domain/service/impl/generation/impl/` (the steps), `PreparePayloadStep` for the final messages |
| How lore is activated | `ProcessLorebookStep`, `LorebookServiceImpl` |
| A macro | `domain/templates/TemplateData.java` (logic), `macros/Macros.java` (registry), `macros/MacroTranslator.java` (syntax) |
| The story editor | `ui/dialogs/manuscript/ManuscriptStoryPart.java` |
| Summaries and meta summaries | `SummaryServiceImpl` (chain, creation, unwinding, export and import), `utils/SummaryTransfer.java` (the text file), `ui/dialogs/manuscript/SummariesDialog.java`, `SummariesImportDialog.java`, `SummaryDialog.java` |
| The story tree operations | `ChatMessageServiceImpl` (branch, swipe, delete), `ManuscriptTreePart` (UI) |
| Folders, or where a setting of a book comes from | [Folders](folders.md), `ManuscriptService.resolve`, `FolderServiceImpl`, `ui/dialogs/OrganiseDialog.java` |
| An entity or the schema | the entity in `domain/model/impl/`, a new Flyway migration, `persistence.xml` - see [Database & migrations](database.md) |
| A new service | interface in `domain/service/`, implementation in `impl/`, a bean in `services-config.xml` |
| The help assistant | `HelpAssistantServiceImpl`, `HelpPrompts` (prompts), `ui/components/HelpAssistantWidget.java`, `ui/LLMInteract.java` (what a part of the UI says and offers), `utils/UiProblems.java` and `bound/ProblemLog.java` (what went wrong), `domain/service/impl/HelpAdminData.java` (the data of the Admin page), `ui/workspace/parts/admin/HelpAssistantPanel.java` (settings) |
| What an extension can hook into | classes annotated `@Extendable` in `ui/` |
| Global styles | `src/main/frontend/styles/shared-styles.css` (the book style of the text is code now, `PartStyle`, not a stylesheet) |
| Login and saved logins | `ui/main/LoginCheckRoute.java`, `UserServiceImpl` |
| Backups | `BackupServiceImpl` (books), `DatabaseBackupServiceImpl` (database), `Configuration` (pending restore, copy before a migration) |
| The desktop app | `src/desktop/java/.../DesktopLauncher.java`, the `desktop` profile in `pom.xml` |
