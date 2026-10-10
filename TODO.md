# Marginalia — TODO for first release

Ordered from most to least important. Each item says **why** it matters and **where** to look.
Items marked 🧩 are good candidates for an extension (OSGi plugin) instead of core.

---

## P0 — Must fix before release (security / data loss)

1. ~~**Viewer exposes other users' books.**~~ ✅ Done — `/view/<uuid>` accepts only uuid and resolves through
   `ManuscriptService.findViewable` (owner or `published`, never deleted). Anything else shows a generic "Book not
   found". Opening no longer re-saves: the UI calls `markOpened` (sets `lastOpened` with a bulk update, owner only)
   before navigating, and the reader list sorts by last opened.

2. ~~**Ownership is not enforced in the service layer.**~~ ✅ Done — `OwnedService.findForUser(uuid)` returns
   only non-deleted entities owned by the current user (others are treated as not found → `null`). Backup
   restore/clone and lorebook import analysis use it; the story tree resolves a clicked node only among messages of
   the open manuscript. The Viewer keeps `findViewable` (owner or published).
   Remaining by-id lookups follow references inside the user's own data. `UserSetting` ids and plugin settings are
   only written from the user's own pick-lists and are never exported, so they need no change.

3. ✅ **No automated tests.** `src/test` doesn't exist. Minimum set:
   - ~~template/macro rendering (`TemplateServiceImpl`, `domain/templates/macros`)~~ ✅ `templates/*Test` (incl. the `macro-test.json` lorebook fixture) and `TemplateHelpersTest`
   - ~~lorebook activation (tags, negative tags, filtering modes, insertion modes, sub-lorebooks, disabled/deleted)~~ ✅ `generation/LorebookActivationTest` (real generations via `GenerationTestBase`)
   - ~~backup round trip incl. lorebook resolution (same uuid / same name / not found)~~ ✅ `backup/*Test` (copy, full and messages-only restore, AI/protocol/lorebook resolution, backup files)
   - ~~lorebook export/import (v1 + v2), SillyTavern import~~ ✅ `lorebook/LorebookImportExportTest`
   - ~~cleanup planner (`CleanupServiceImpl`) on a fixture DB: blocked, cascaded, cycles, weak references~~ ✅ `cleanup/CleanupServiceTest`
   - ~~Flyway migrations on an empty DB and on a V1 DB, plus `hbm2ddl=validate`~~ ✅ `FlywayMigrationTest`
   - ~~user service: auth, remember-me cookies, deleted users can't log in, last admin protection~~ ✅ `UserCrudTest`
   - ✅ `@Extendable` instrumentation: `instruct/ExtendableMethodVisitorTest` (ASM + JVM verification, schema kept for
     retransformation, behaviour equal to the original, hooks/arguments/locals) and `RuntimeInstrumentationTest` (real agent)
   - ✅ CRUD for every entity (`crud/*CrudTest` via `OwnedCrudContract`/`ExtendableCrudContract`: identity, update,
     soft/hard delete, owner isolation, `@ExtendedAttribute` fields and `attributes` persistence) and
     `ExtendableEntityListenerTest` for the extended-content serializer
   ✅ Test base done: `MarginaliaTestBase` (production Spring XML minus OSGi/instrumentation, SQLite in
   `target/test-home/ctx-*`, mock request/session per test, `loginAs`/`createUser`/`createAI` helpers) and
   `MockLLMServer` — an OpenAI-compatible fake whose responses are programmed per test; the path segment before `/v1`
   selects the test's scenario (`llm.baseUrl()`).

4. ~~**Scheduled database backups.**~~ ✅ Cron schedule + "keep last N" rotation of scheduled backups in
   `AppSettings`, edited in Admin → Database Backups; cron job restarted on change, missed run caught up after start
   (`DatabaseBackupServiceImpl`).

5. ~~**Login brute-force protection.**~~ ✅ `UserServiceImpl.authenticate` does constant-work hashing (also for unknown
   users / missing passwords) and counts consecutive failures on `User` (`failedLogins`, `lockedUntil`): 10 free
   attempts, then 1s, 2s, 4s ... capped at 15 min; reset on success or password change/clear. Failures are logged.
   Per-IP throttling is in memory only (`UserServiceImpl`, 30 free failures per address, same back-off, logged; a
   restart clears it; the address comes from `UIUtils.clientAddress()`, honouring `X-Forwarded-For` only from a
   loopback/private peer).

---

## P1 — Needed for a usable public release

6. ✅ **README + user documentation** (`docs/user` is empty).
   - ✅ README (description, features, desktop/Docker/source installation, first start, data location, extensions,
     license) and plugin READMEs in `marginalia/plugins`. Still missing: the screenshots listed in `TODO.IMAGES.md`,
     upgrade notes.
   - ✅User guide: books, story editor (generate / regenerate / swipe / branches / summaries / chapters),
     protocols vs. inference providers, lorebooks (tags, filtering, insertion modes, sub-lorebooks), macro
     reference (the `DESC_MACRO_*` texts already exist and can be used to generate it), backups, admin pages.
   - ✅ Deployment: reverse proxy + HTTPS (the app sets remember-me cookies, so it must not be exposed over plain
     HTTP), memory settings, where plugins go.

7. ✅ **Developer documentation.**
   - ✅ Architecture overview: Spring XML wiring, `@Configurable` UI, `@Extendable` + bytecode instrumentation,
     generation pipeline steps (`StoryGenerationServiceImpl.installedSteps`), extended attributes, localization.
   - ✅ **Plugin authoring guide**: bundle layout, `MarginaliaExtension`, `ExtensionService.registerDecorator`,
     `CleanupService.registerContributor`, the context-variable API, and the 4 existing plugins as examples.
   - ✅ How to add an entity (persistence.xml, Flyway migration, `@CleanupReference`, backup/export impact).

8. **Build & CI.**
   - ✅ No CI. Add a GitHub Actions workflow: build core + all plugins, run tests, build the Docker image.
   - Parent/aggregator POM: plugins are separate projects depending on `marginalia:1.0.0` and are **not built or
     shipped by the Dockerfile**. Make a multi-module build, and decide whether bundled plugins ship preinstalled.
   - ✅ `target/` folders are not in `.gitignore` (root and `plugins/*/target`).
   - ✅ Versioning: everything is `1.0.0`. `AppSettings.appVersion/dbVersion` and `ApplicationInitializer` are hard-coded `1`.
     Use the Maven version + git info (git-commit-id plugin is already present) and show it in the admin page.
   - ✅ Release artifacts: WAR + plugin JARs attached to a GitHub release and the Docker image (amd64 + arm64) pushed to
     `ghcr.io` by `.github/workflows/server.yml` (the desktop archives come from `desktop.yml`).
   - ✅ Desktop distribution: `mvn package -Pdesktop` (jlink runtime + jetty-home + launcher with tray icon, jpackage
     app image, archives). Built by `.github/workflows/desktop.yml` on Linux x64/arm64, Windows x64 and macOS
     arm64/x64 (tests once, smoke test per platform, archives attached to `v*` releases). Still missing: signing /
     notarization (macOS Gatekeeper, Windows SmartScreen), an app icon, installers (dmg/msi/deb), and the release
     version is the pom version, not the tag.

9. **WAR size / dependency hygiene.** The WAR is ~200 MB. `tika-parsers-standard-package` + `tika-async-cli`
   look unused (no references in code). `Resource` is now used for the image attachments of parts (story editor, exports) and listed in the Resources tab. Logging is on
   `slf4j-log4j12` + `reload4j` (log4j 1.x API) — move to logback or log4j2. Remove what's unused, or finish it (see 21).

10. ✅ **Story export.** The active branch (range of messages) is exported from the settings menu of the story editor
    as TXT / Markdown / HTML / DOCX / PDF / EPUB, optionally with a title page (`ExporterService`, `ExportDialog`). Missing:
    export of a chosen leaf (other than the active one), chapter headings from `chaptermarker`, an editable title
    page template.

11. **Inference robustness** (`OpenAICompatibleInferenceService`).
    - Configurable timeouts and retry with backoff for 429/5xx.
    - Human-readable error mapping (bad key, wrong URL, model not found, context overflow) instead of a generic
      internal error.
    - "Test connection" button in `AIDialog` (list models + 1-token completion).
    - Context overflow handling: warn before sending when estimated prompt > `maxContext`.

12. **Trash / restore UI.** Everything is soft deleted, but users can't undo a delete. Add a "Recently deleted"
    view per user (books, lorebooks, entries, providers, protocols) with restore. Show its contents in the cleanup
    page so admins know what a purge removes.

13. ✅ ~~**Deleted users' data policy.** Cleanup never purges a deleted user while they own data (`owner` is STRONG).
    Decide: admin action "delete user and all their data" (mark owned data deleted, then cleanup), or
    transfer ownership to another user. Also user data folders (`~/.marginalia/data/<login>`) are keyed by
    login, which is renamed on delete, so the folder is orphaned.~~ `owner` is `OWNED_BY` (BUG-26), the data
    folder is renamed to `<login>-deleted`.

14. ~~**Admin-only operations should be checked server side.** `AdminPart` is only hidden in the UI. Services like
    `UserService.deleteUser`, `DatabaseBackupService`, `CleanupService`, `OsgiService.installPackage` should
    check `currentUser.isAdmin()` themselves. Installing an OSGi bundle runs arbitrary code, so this is the most
    sensitive one.~~ Not required since server code checks it before calling the methods and extensions run arbitrary code anyways

---

## P2 — Expected features (feature parity with similar tools)

15. **Merge into previous part** (instead of a "continue" request type) — chat completion has no reliable "continue":
    prefilling a trailing assistant message is ignored or rejected by many OpenAI-compatible backends and reasoning
    models, and tends to repeat the text. A new turn ("continue from where the last part stopped") already works;
    what is missing is joining the result. Add a part action that appends the selected part's text to the part before
    it, recounts words/tokens and removes the emptied part. Decide: which part's `attributes` (variables, extension
    data) win, what happens if the previous part has other children (branches), and invalidating a summary that
    covered either part. Optional helper: pre-fill the next turn's instructions with a "continue" text when the last
    part ended on a length cutoff. Separately: "impersonate/draft instructions for me".
yeah
16. **Search inside a book** — full text search across the messages of a book (active branch and all branches),
    jump to the message. Global search across books is a nice extra.

17. **Duplicate actions** — duplicate book (settings only / with story), lorebook, protocol, provider.
    Most of it exists via backup clone and lorebook export/import.

18. **More provider and protocol types.** Only `AIType.OPEN_AI_COMPATIBLE` and `ProtocolType.CHAT_COMPLETION` exist.
    - Text completion protocol (llama.cpp / KoboldCpp / Ollama raw prompt with instruct templates).
    - Native Anthropic / Gemini clients (tokenizer strategies for LiteLLM-Anthropic already exist).
    - 🧩 Each new provider could be a plugin if `InferenceServices` gets a registration API like decorators have.

19. **Context / token budget view.** Show per-section token usage of the last prompt (system, lore, summaries,
    history, instructions) and what was cut. The data is already computed in the generation steps.

20. **Lorebook improvements.**
    - ✅ Edit lorebook-level tags in `LorebookView` (generation reads them in `fillEntries`, but there's no UI).
    - Entry search/filter and bulk enable/disable/delete in the entry grid.
    - SillyTavern **export** (import exists).
    - Recursive activation / scan depth (entries activating other entries) and activation preview
      ("which entries would fire for this prompt").

21. **Character / persona management.** POV and present characters are free text per turn. A character card
    entity (name, description, avatar via the `Resource` entity (`ResourceService.uploadImage`), linked lorebook entries) with
    pick-lists in the turn editor. SillyTavern character card import. 🧩 possible extension.

22. **Sharing between users.** Everything is private. Read-only sharing of lorebooks/protocols, or an
    admin-managed "global" library, plus public read-only links for the Viewer (after fixing item 1).

23. **Localization.** Only `LocalizationEN` exists, and the language is chosen by `configuration.properties`
    for the whole instance. Allow a per-user language setting, and make the `L` key system plugin-friendly
    (plugins currently can't add keys to the enum). Some strings are still hard-coded (e.g. bundle states in
    `AdminPart.formatBundleState`, default names like "Imported Lorebook", "Entry" in services).

24a. **Generation events for extensions** (groundwork for a 🧩 *Generation Inspector* plugin: per-message timeline,
    lorebook activation with skip reasons, context budget, exact payload).
    - ✅ Done: listeners get the generation context through `GenerationControllerEvent` (`getProperties()`,
      `getProperty(key)`, `getRequest()`, `getInput()`, `get/setPrePromptData()`, `get/setPayload()`). Property keys
      are public in `GenerationProperties`, which documents each key's type and the first event it's available in.
    - Lorebook entry skip reasons are only logged in `ProcessLorebookStep`; store them as a property.
    - Terminal events for failed and cancelled generations (currently only success emits `AFTER_INFERENCE`).
    - Lightweight read-only observers (synchronous, no executor hop) for hot events like `CHUNK_RECEIVED`.
    - Generation run id on the event to correlate data from concurrent generations.

24. **Extension management polish.** Enable/disable without uninstalling, show bundle errors and dependencies,
    validate a JAR before installing, reject plugins built for an incompatible core version.

24b. **Summaries overview tools** 🧩 — the *Summaries* dialog (`SummariesDialog`, `@Extendable`) has an empty menu bar
    for these:
    - Export and import of the summaries of a branch (e.g. to edit them in an external editor or move them to another
      book).
    - Search and replace in the summaries (names that changed, fixing a term in all of them at once).
    Editing a summary a meta summary stands in for would invalidate the meta summary (its text is part of the hash);
    a replace tool must update the hashes of the meta summaries it touches (or edit only the summaries in use).

---

## P3 — Nice to have

25. **Writing statistics** — words per day/session, per-book totals, goals. 🧩
26. **Undo for edits** — message edits overwrite text in place; keep per-message edit history (branching
    covers generations but not manual edits). 🧩 (similar to `lorebookvcs`)
27. **Keyboard shortcuts** in the story editor (generate, stop, swipe, regenerate, edit).
28. **Mobile layout** — fixed widths (`setWidth("280px")`, 900px dialogs, 260px tab bar) don't fit phones.
29. **Theming** — light/dark toggle per user, editor font/size settings.
30. **Import from other tools** — SillyTavern chats → book with branches, NovelAI/KoboldAI stories. 🧩
31. **Image generation / illustrations** for scenes (would use `Resource`; attaching images by hand is done, see
    *Images* in the part menu - generation, sending images to vision models and showing them to the readers of a
    published book in the Viewer are not; the Viewer deliberately shows them to the owner only). Files of deleted
    resources and of replaced content stay in the data folder: a garbage collection for them is still to be done. 🧩
32. **Text-to-speech read-aloud** in the Viewer. 🧩
33. **Health and metrics endpoint** for Docker (`HEALTHCHECK`), plus basic usage metrics (tokens per provider). The
    compose file has no healthcheck.
34. **Configuration via environment variables** — today it's `configuration.properties` plus
    `-Duser.home`. Document the keys and allow env overrides (data dir, localization, cookie TTL).

---

## Housekeeping found while analyzing

- `Workspace.refresh()` has an empty `internalEvent` block.
- `LorebookVCSExtension.onExtensionLoad` has a `refreshCallback` that computes a value and throws it away.