# TODO: platform refactor (layer 1 of `TODO.VN.md`)

The plan for splitting the platform out of the application, so that Joi (`../joi/MARGINALIA.md`) and later the visual
novel can build on it. Base: tag `pre-refactor` (`22f248c31` on `dev`). Released as part of **1.9.0**: nobody using the
application or writing a plugin should be able to tell that the code moved.

Scope: the platform module only. The narrative core (layer 2) waits until the visual novel shapes the payload of a node,
as `TODO.VN.md` says; Joi does not need it.

## Rules for the whole refactor

- **Packages do not change.** A class moves to another module, never to another package. Plugins (19 of them import the
  inference DTOs from `impl.generation.dto`, 9 import `ui.components.ThreadCopyRequestAttributes`) and `@PublicAPI`
  stay untouched. A package split over two jars is fine here: the webapp loads both through one class loader (same
  runtime package, package-private access works) and Felix runs with `FRAMEWORK_BOOTDELEGATION=*`. There is no JPMS
  module that would forbid it. So "DTOs in the wrong package" stops being a blocker: they go to the platform module
  in the package they are in.
- **The direction of dependencies is the only thing to fix.** Platform code must not name a story class. Vaadin in the
  platform is allowed: the platform carries the generic UI, so `UI`, `Component` and `Registration` in a platform
  service are not leaks (the "25 files import Vaadin" count of `TODO.VN.md` mixes the two; most of them only import
  `ThreadCopyRequestAttributes`, which is platform code).
- **Tests pass after every step**, one commit per item, the classes touched plus `PublicAPISignatureTest`. The full
  suite at the end of each phase.
- **No behaviour change**, so no changelog entry, except where a plugin-visible signature changes (then `from = 1.9.0`
  and an entry under `Changed`). Developer docs (`manual/developer/architecture*`, building, testing, plugins) follow
  the structure once the module exists.

## Phase 0: a layer map and a check

Before any code moves, make the borders explicit and checkable, so every step shows its progress and nothing slides
back.

- [x] `marginalia/src/test/resources/layers/layers.txt`: every package (or class, where a package is split) mapped to
      `platform` or `app`, longest name wins. Started from the first cut below.
- [x] `LayerDependencyTest` (`architecture` package): reads the compiled classes with ASM and fails when a `platform`
      class names an `app` class, or when a class has no layer. Today's references are in
      `layers/allowed-leaks.txt`, which may only shrink (a fixed one fails until its line is deleted); Phase 1 empties
      it. At the start: 153 references from 33 platform classes. `-Dlayers.update=true` rewrites the list after a change of the map.
- [ ] Once Phase 2 is done the module border itself enforces this; the test stays to keep the split packages honest.

### First cut of the map

Platform (no story concept):

- `domain.traits`, `domain.security`, `domain.templates`, `instruct`, `concurrent`, `api`
- `domain.model`: `BaseEntity`, `OwnedEntity`, `ExtendableEntity`, `Setting` (+ `settings/UserSetting`,
  `AppSettings`, `NotificationSettings`), `converter/*`, `impl/AI`, `Protocol`, `ChatCompletionProtocol`,
  `OpenAICompatible`, `ExtensionAI`, `Usage`, `Tag`, `TagRelation`, `Resource`, `Job`, `JobCall`, `Gpu`, `GpuLock`,
  `GpuStep`
- `domain.service`: `function`, `tool`, `structured`, `gpu`, `job`, `manual`, `run` (`Runs`), `command` (the framework,
  not the manuscript commands), the inference API and `impl.inference`, `impl.generation.dto` (`LLM*`), the base and
  owned/extendable services, AI/protocol/limits/usage, settings, resources, tags, jobs, cleanup, trash, the database
  backup, updates, notifications, OSGi and extension services, tokenizers, `CancellationToken`
- `extensions` minus the story registries
- `loc` (the mechanism, `L` and its bundles; `M_L` and the `MarginaliaLocalizationBundle*` are the app's)
- `ui`: `widgets`, `menu`, `jobs`, `shortcuts`, `styles` (mechanism), the generic part of `ui.components`
  (`ThreadCopyRequestAttributes`, `UIPushGuard`, `RunCorner`, `RunsOnPage`, `PageRuns`, `TagsView`, `CommandBar`),
  the generic dialogs (`ConfirmDialog`, `ErrorDialog`, `TextInputDialog`, `ProgressBarDialog`, `UserDialog`, `AIDialog`,
  `AILimit*Dialog`, `ProtocolDialog`, `Gpu*Dialog`, `ToolSelectionDialog`, `ExtensionLogDialog`), `JavascriptLibrary`
- `bound` minus `migration` (`SessionPoint` after the fix below), `Configuration`, `DatabaseCheck`,
  `ReadWriteRoutingDataSource`, `SaneSQLiteDialect`, `UIConstants`, `CssStyles` (see Phase 2)

App (everything else), notably: story entities, `Defaults*` (story prompts), `bound.migration` (Java migrations of
story data), the book backup and archive import (`BackupService`, `ArchiveImportService`: the database backup is the
platform one), statistics and `ActivityInterval*`, `ImageAttachment`/`ImageAnchor`, folders and their inherited
settings (`EffectiveSetting`, `EffectiveHistoryLimit`, `HistoryLimitField`, `InheritedLanguageCombo`,
`PromptSettingsForm`, `BookLabels`: layer 2 later), `ui.main` (the routes and workspaces), `ui.workspace`, the rest
of `ui.dialogs`, `export`, agents.

## Phase 1: fix the leaks in place

Same module, same packages, ordered so the classes Joi needs first are clean first. Found with `jdeps` on the compiled
classes; each item is a registration point or an interface in place of a hard reference.

1. [x] **Runs.** `Runs` holds `ChatMessage` locks and has `Builder.book(Manuscript)`; `HeldException` extends
       `ManuscriptLockedException`. Holds become typed by entity class without naming one (the app says which classes
       lock as "write parts"), `book(...)` becomes a generic owner object (uuid + label), the exception gets a platform
       base class that `ManuscriptLockedException` extends. Unblocks `JpaBaseRepository`, `GatedInferenceService`,
       `RunsGrid`, `RunCorner`, `RunsOnPage`, `PageRuns`, `WindowRun`, `UIUtils`.
       Done: `RunScope` (set by the app's `BookRunScope`), `Builder.book(OwnedEntity)`, platform `LockedException` as
       the base of `ManuscriptLockedException` and `Runs.HeldException`.
2. [x] **Request copying.** `ThreadCopyRequestAttributes` names `AgentRequestAttributes`: a hook the agent code
       registers. With 1. done, `OpenAICompatibleInferenceService` and `GatedInferenceService` are clean.
       Found clean already (the hit came from stale classes); `DetachedRequestAttributes` / `ApiRequestAttributes`
       still name `AgenticSessionPoint`, which belongs to item 8.
3. [x] **Usage.** `UsageContext.open(Manuscript)` and `UsageServiceImpl` (manuscript statistics): the context carries
       an owner object (kind + uuid), the app adds the book statistics through a listener. This is also Joi's "usage per
       assistant and role".
       Done in a smaller form: `UsageContext.open(BaseEntity)`, `UsageListener` beans (the app's `BookUsageCounter`),
       and `RequestActivity` (what `ActivityTracker` extends) for the time on the wire. The context is still one id;
       a kind (assistant, role) comes with Joi's need.
4. [x] **Generation listener and views in platform signatures.** `GenerationListener` (names `ChatMessage`),
       `GenerationView`, `PromptField`, `PassageKind`, `IPCService`: decide per class; most are app interfaces that
       sit in `domain.service` only by package and simply go to `app` in the map, not leaks to fix.
       Done by the map: none of them is named by a platform class any more.
5. [x] **Commands.** `CommandScope` / `CommandScopesImpl` list the story entities: scopes become registered by the
       app; the framework (`CommandService`, `CommandContext`, `CommandBar`) stays generic. `utils.CommandUtils` goes
       to the app.
       Done: the built-in suites are registered by the app's `BuiltInCommands` bean, not `CommandServiceImpl`. The
       framework sees a `BaseCommandScope` (`isAgent`, `confirm`) and gets the default one from a `CommandScopeSource`;
       `CommandScope` (book, lorebooks, run) is `app` and extends it, commands use `CommandScope.of(context)` (plugin API
       change, the five plugins that read the scope are fixed). Left: `CommandBar` (`setGenerationView`, the hold of the
       book for a generation, `CommandScopes.session(view)`) and `ApiDispatcher`, which go with the UI and API items.
       Then done: `CommandBar` has a `CommandBarHost` (the scope, the hold for a generation; the story's is the app's
       `StoryCommandBarHost`), `ApiDispatcher` has `ApiTargets` (the books, the scope of a command, the session point of
       a call; the app's `BookApiTargets`).
6. [x] **Cleanup and resources.** `CleanupServiceImpl` (1,174 lines, names seven story entities) and
       `JpaResourceRepository` / `ResourceServiceImpl` (resources of a book through `ChatMessage`): story-specific
       cleanup steps and "resources of an owner" queries become contributions the app registers. `@CleanupReference`
       already covers most of the purge; check what is left.
       Done: resources through `SharedResources` (the app's `BookSharedResources`); the trash through `TrashPolicy`
       (the app's `BookTrashPolicy`: the kinds that can be deleted for good and what goes with them, moving the children
       of deleted parts, the size of the books); the uuid scan of the purge has its own pattern.
7. [x] **Tags.** `TagRelationService(Impl)` names `Manuscript`: tagged types registered.
8. [x] **Session.** `SessionPoint` holds the open book, window and section; `ApplicationPoint` / `SessionInformation`
       name `Workspace`. Split into a platform `SessionPoint` (user, language, generic attributes) and an app
       `ManuscriptSessionPoint` (or a typed attribute the app stores in it).
       Done: the bean is the app's `MarginaliaSessionPoint` (open book, window, book list, the workspace as `Workspace`),
       `AgenticSessionPoint` extends it; `SessionPoint` holds a `BaseWorkspace` (platform part of `Workspace`), and
       `ApplicationPoint`, `SessionInformation`, `DetachedRequestAttributes` / `ApiRequestAttributes` use the platform types.
9. [x] **OSGi and menus.** `OsgiServiceImpl` names `StoryMenuRegistry`; `ui.menu` (`BuiltMenu`, `MenuDefinition`,
       `MenuNode`) names `StoryCard` / `StoryEditor` / `StoryMenuItem`: the menu mechanism takes registered menu
       kinds, the story menus are the app's.
       Done: `BuiltMenu`, `MenuDefinition`, `MenuNode` are the story editor's menu and went to `app` in the map; the
       platform keeps the positions, groups and paths. `OsgiServiceImpl` clears the registries of an unloaded bundle
       through the beans that implement `ExtensionRegistrations` (IPC, tools, notifications, story menus, inherited
       settings), so it names none of them.
10. [x] **Extensions registries.** `AnnotationFactsRegistry`, `ExportTextRegistry`, `PartTextRegistry` go to `app`
        (same package, map entry), and whatever registers them in `OsgiServiceImpl` goes through a hook.
        Done: they were `app` in the map already and `OsgiServiceImpl` does not name them (extensions unregister
        themselves); the hook is `ExtensionRegistrations` of item 9.
11. [x] **Localization.** `Localization` / `LocalizationImpl` name `AgentRunStatus`, `Lorebook*` and entity classes
        (labels of story enums and entity kinds): the app registers those label providers.
        Done as a subtype: `MarginaliaLocalization extends Localization` and `MarginaliaLocalizationImpl extends LocalizationImpl`
        (the bean) hold the story enum labels, the story entity kinds and the prompts (`Defaults`); the platform keeps
        the bundles, its own enums, `localizeDomainObject` (filled by the subclass) and the prompt languages (English
        alone, the subclass gives the ones of `Defaults`). Callers that need story labels inject `MarginaliaLocalization`.
        The keys were split later (Phase 2, Localization): `M_L` holds the app's.
12. [x] **Widgets and the rest.** `AuthorshipField`, `FloatingWindow`, `ExtensionSettingsForm`, `ReaderStyleChooser`,
        `PartView`: move to `app` in the map, or (`FloatingWindow`, `ExtensionSettingsForm`) cut out the story part.
        Done: `PermissionUIFactoryImpl` is an app bean, `ApplicationEntryPoint` is platform. `FloatingWindow` and
        `WindowRun` take an `OwnedEntity` and `BaseEntity` parts, the book window is found as a `HostWindow`, so both are
        platform; `ExtensionSettingsForm` (the book and folder settings) is `app` in the map; `UIUtils` shows any
        `LockedException` with its own `describe(loc)`.
13. [x] **Test base.** `MarginaliaTestBase` split into a platform base (`loginAs`, `createUser`, `createAI`,
        `MockLLMServer`, the CRUD contracts) and the app's story helpers on top.
        Done: `PlatformTestBase` (users, permissions, login, AIs, the mock LLM) under `MarginaliaTestBase` (the
        background writers of books and activity); the CRUD contracts extend the platform base. `LayerDependencyTest`
        checks the shared test code too (`layers/test-layers.txt`, no allow list).

Done when the allow list of `LayerDependencyTest` is empty. Progress: 153 references from 33 classes at the start,
133 from 21 after items 1, 2, 3, 7 and parts of 6 and 12 (2026-11-19), 115 from 15 after the settings split and items 9, 10. 70 from 13 after the command framework (item 5). 38 from 11 after localization (item 11). 19 from 7 after cleanup (item 6) and the widgets of item 12, 0 after the session, the command bar and the API (2026-11-19).

Found on the way, to decide:

- [x] **`UserSetting`** holds settings of the application (`WebhookFormat` of agents, `WatchedPhrase`, `AnnotationTemplate`,
  `BackupStrategy`). The platform needs its own user settings and the application's settings on top (extended
  attributes of an app-side part, or a second entity). A design choice, not a quick fix.
  Done: the platform part is the mapped superclass `BaseUserSetting` (language, prompt language, theme, keys, workspace
  state, default AI and protocol), `SettingService.getUserSetting()` returns the configured subclass (`userSettingClass`).
- [x] **`ExtensionHasLocalization.getPromptDefaults()`** returns `Defaults` (the story prompts): the plugin API mixes the
  UI language with the prompts. Splitting it changes the API of the `chineselocalization` plugin; do it with item 11.
  Done: `ExtensionHasLocalizedPrompts` (app), registered by the app's `ExtensionPromptsRegistrar`.
- **Building the WAR with `-Dbuild.dir` outside the project fails** in `build-frontend` (Vite can't resolve
  `@rollup/pluginutils` from `/tmp/...`). Tests are fine there. To build plugins against such a build, a classes JAR of
  `<build.dir>/classes` was installed with `mvn install:install-file -Dclassifier=classes -DpomFile=marginalia/pom.xml`.

## Phase 2: extract the platform module

- [ ] **Layout.** `marginalia/platform` (packaging `jar`, artifact `marginalia-platform`), listed before `marginalia`
      in the root `pom.xml`. The app depends on it. Plugins keep depending on the app's `classes` JAR; check that the
      platform comes in transitively, or add it to the plugin parent.
- [ ] **Weaving (the main risk, try it first with a spike).** The platform has `@Configurable` classes, so it needs
      its own `aspectj-maven-plugin` with `spring-aspects` as aspect library, and its own `BuildTimeWeaver` run in
      `process-classes` on its output folder. The app must not weave the platform again (no `weaveDependencies`).
      Check: injected fields not null in platform widgets, `@Woven` marks in the platform JAR, the ByteBuddy agent of
      dev runs still covering the platform classes, `PublicAPISignatureTest` reading classes from both modules.
- [ ] **Spring XML.** `services-config.xml` (125 beans) split into a platform file shipped in the platform JAR
      (`META-INF/spring/platform/*.xml`) and the app's; `application-config.xml` imports both.
- [ ] **Entities.** `persistence.xml` (40 classes): the app lists its own, a platform `PersistenceUnitPostProcessor`
      adds the platform ones, so Joi does not copy the list. A test checks every `@Entity` on the class path is in
      the unit.
- [ ] **Flyway.** Existing installs have one history (V1 to V31) that created both kinds of tables, and its versions
      and checksums must not change. Proposal: the 31 migrations stay in the app; the platform gets its own location
      and history table (`flyway_schema_history_platform`), starting with a V1 that creates the platform tables with
      `IF NOT EXISTS` (a no-op on Marginalia, the schema for Joi). The platform history runs first, new platform
      schema changes go there. Decide before writing it; test on a copy of a real database and on an empty one.
- [ ] **Styles and frontend.** `CssStyles` is used by 43 classes, platform widgets among them. Either the platform
      ships its part of `shared-styles.css` as a JAR resource (`META-INF/resources/frontend/...`) and the app keeps
      the rest, or the stylesheet stays in the app for now and Joi copies it. Check how Vaadin picks up frontend files
      from a dependency JAR in production mode (`optimizeBundle=false` is set already).
- [x] **Localization.** The keys are split: one enum per layer (`LocalizationKey`), the platform's `L` with the
      `LocalizationBundle*` (the language and its formats), the app's `M_L` with the `MarginaliaLocalizationBundle*`
      (`TextBundle`s registered with `Localization.registerTexts`, by an extension with
      `ExtensionHasLocalization.getTextBundles`); `getValue(LocalizationKey)` looks up the enum of the key. The localized
      texts of commands and template variables are in an annotation per layer (`@MarginaliaCommandTexts`,
      `@MarginaliaTemplateDescription` with `M_L`; `@CommandTexts`, `@LocalizedTemplateDescription` with `L`), found by
      the meta-annotations `@CommandTextSource` / `@TemplateDescriptionSource` (`LocalizedAnnotations`), so no app text
      is left in `L`. The layer-2 core enum (and `TODO.VN.MD`) is one more enum with its bundles and annotations.
- [ ] **Tests.** The platform base from Phase 1 as a `test-jar` of the platform module; the platform's own tests
      move with it (inference, templates, function/tool, gpu, jobs, security, instruct, CRUD of platform entities).
- [ ] **Packaging.** The WAR has the platform JAR in `WEB-INF/lib`; the desktop app and Docker image unchanged in
      behaviour. Check the release workflows and `bin/` scripts that call `mvn install` of the app (they must install
      the platform first, the root build does it).
- [ ] **Docs.** `CLAUDE.md` (commands, module layout), `manual/developer/building.md`, architecture and plugin pages;
      `tools/check_dev_docs.py` `covers` paths that moved.

Done when the WAR, the desktop app and all plugins build from the root, the full suite passes, and a 1.8 database
starts on it without a migration error.

## Not in this refactor

- The narrative core (layer 2) and the visual novel.
- The gaps Joi lists for the platform (thinking per request, parallel tool calls, images for all providers,
  cancelling the HTTP stream, runtime-defined tools, a generic tool loop with approvals). They are improvements of
  the platform, each its own change after Phase 2 (usage per owner comes with Phase 1, item 3).
- The provider plugins as plain modules (Joi's open question): decide when Joi needs them.

## Notes

- The leak list comes from `jdeps -verbose:class` on `target/classes` compiled 2026-11-16, run 2026-11-18; Phase 0
  replaces it with the test.
