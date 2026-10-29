# Marginalia — TODO

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
   restart clears it; the address comes from `UIUtils.clientAddress()` / `ClientAddressResolver`, which believes
   `X-Forwarded-For` only from the proxies named in `-DtrustedProxies`).

---

## P1 — Needed for a usable public release

6. ✅ **README + user documentation** (`manual/user`).
   - ✅ README (description, features, desktop/Docker/source installation, first start, data location, extensions,
     license) and plugin READMEs in `marginalia/plugins`. Upgrade notes are in the README; no
     screenshots are outstanding (`TODO.IMAGES.md`).
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
   - ✅ Parent/aggregator POM: plugins are separate projects depending on `marginalia:1.2.0` and are **not built or
     shipped by the Dockerfile**. Make a multi-module build, and decide whether bundled plugins ship preinstalled.
     Partly eased: `marginalia/plugins/Dockerfile` + `docker-compose.yml` build all plugins in a container (JARs in
     `marginalia/plugins/build`), and `rebuild.sh` / `rebuild.bat` pull, rebuild the plugins into `data/extensions`
     and restart the compose stack. The root `pom.xml` is the aggregator and parent POM (since 1.1.0); still no
     preinstalled plugins in the image.
   - ✅ `target/` folders are not in `.gitignore` (root and `plugins/*/target`).
   - ✅ Versioning: everything is `1.2.0`. `AppSettings.appVersion/dbVersion` and `ApplicationInitializer` are hard-coded `1`.
     Use the Maven version + git info (git-commit-id plugin is already present) and show it in the admin page.
   - ✅ Release artifacts: WAR + plugin JARs attached to a GitHub release and the Docker image (amd64 + arm64) pushed to
     `ghcr.io` by `.github/workflows/server.yml` (the desktop archives come from `desktop.yml`).
   - ✅ Desktop distribution: `mvn package -Pdesktop` (jlink runtime + jetty-home + launcher with tray icon, jpackage
     app image, archives). Built by `.github/workflows/desktop.yml` on Linux x64/arm64, Windows x64 and macOS
     arm64/x64 (tests once, smoke test per platform, archives attached to `v*` releases). Signing / notarization
     (macOS Gatekeeper, Windows SmartScreen) is deliberately not planned: it costs money and nobody pays for it, so
     users get the unsigned archives and the manual explains the warnings. Still missing: an app icon, installers
     (dmg/msi/deb), and the release version is the pom version, not the tag.

9. **WAR size / dependency hygiene.** The WAR was ~200 MB. `tika-parsers-standard-package` + `tika-async-cli`
   were unused and are removed (the WAR is ~185 MB; the export tests declare PDFBox themselves). `Resource` is now used for the image attachments of parts (story editor, exports) and listed in the Resources tab. Logging moved from `slf4j-log4j12` + `reload4j` to logback (`log4j-to-slf4j` for the `log4j-api` of POI). ✅ Done.

10. ✅ **Story export.** The active branch (range of messages) is exported from the settings menu of the story editor
    as TXT / Markdown / HTML / DOCX / PDF / EPUB, optionally with a title page (`ExporterService`, `ExportDialog`). Missing:
    export of a chosen leaf (other than the active one), chapter headings from `chaptermarker`, an editable title
    page template.

11. ✅ **Inference robustness** (`OpenAICompatibleInferenceService`).
    - ✅ Configurable timeout and retries (extended attributes of `OpenAICompatible`, edited in `AIDialog`); the client
      retries 429/5xx/connection errors with backoff.
    - ✅ Human-readable error mapping (`InferenceException`, `InferenceErrors`): bad key, wrong URL, model not found,
      context overflow, rate limit, timeout... shown as a notification; used by the story editor, summaries and the
      reviewer / sidequery plugins.
    - ✅ "Test Connection" button in `AIDialog` (list models + 1-token completion).
    - ✅ Warning before sending when the prompt plus the response limit exceed the context (`onWarning`).

12. ✅ ~~**Trash / restore UI.** Everything is soft deleted, but users can't undo a delete. Add a "Recently deleted"
    view per user (books, lorebooks, entries, providers, protocols) with restore. Show its contents in the cleanup
    page so admins know what a purge removes.~~ The *Trash* tab (`TrashPart`, widget `TrashGrid`, service
    `TrashService` implemented by `CleanupServiceImpl`): users restore their own deleted objects, administrators see
    all users' (filter by user) and restore any. Filter by type (`Localization.localizeDomainObject`), a link shows the
    extended content as JSON. Restore is refused while a deleted parent (owner, book, lorebook, strong reference) is not
    restored with it. The same widget is in *Admin → Cleanup*. Not done: a restored story part does not get its
    former children back (they were moved to its parent when it was deleted). Since 1.2.0 deleting an object takes what
    belongs to it along (book -> parts, summaries, tags; lorebook -> entries...) and the Trash restores them together,
    and **Delete Permanently** removes deleted story parts and lorebook entries right away (`TrashService.deletePermanently`).

13. ✅ ~~**Deleted users' data policy.** Cleanup never purges a deleted user while they own data (`owner` is STRONG).
    Decide: admin action "delete user and all their data" (mark owned data deleted, then cleanup), or
    transfer ownership to another user. Also user data folders (`~/.marginalia/data/<login>`) are keyed by
    login, which is renamed on delete, so the folder is orphaned.~~ `owner` is `OWNED_BY` (BUG-26), the data
    folder is renamed to `<login>-deleted`.

14. ~~**Admin-only operations should be checked server side.** `AdminPart` is only hidden in the UI. Services like
    `UserService.deleteUser`, `DatabaseBackupService`, `CleanupService`, `OsgiService.installPackage` should
    check `currentUser.isAdmin()` themselves. Installing an OSGi bundle runs arbitrary code, so this is the most
    sensitive one.~~ ✅ Done — `AdminGuard.requireAdmin()` is called in `UserServiceImpl` (admin methods), `DatabaseBackupServiceImpl`,
    `CleanupServiceImpl` and `OsgiServiceImpl`; tests call them as a non-admin.

---

## P2 — Expected features (feature parity with similar tools)

15. ✅ **Merge into previous part** (instead of a "continue" request type) — chat completion has no reliable "continue":
    prefilling a trailing assistant message is ignored or rejected by many OpenAI-compatible backends and reasoning
    models, and tends to repeat the text. A new turn ("continue from where the last part stopped") already works;
    what is missing is joining the result. Add a part action that appends the selected part's text to the part before
    it, recounts words/tokens and removes the emptied part. Decide: which part's `attributes` (variables, extension
    data) win, what happens if the previous part has other children (branches), and invalidating a summary that
    covered either part. Optional helper: pre-fill the next turn's instructions with a "continue" text when the last
    part ended on a length cutoff. Separately: "impersonate/draft instructions for me".
    Done: *Merge into previous part* in the part menu (`ChatMessageService.mergeIntoPrevious`, review in `PartMergeDialog`: join of the
    texts, prompt fields combined without their shared start, `attributes` value by value with the choice which wins; refused for a
    first part and for a part with swipes). A pre-filled "continue" instruction after a length cutoff is deliberately not planned:
    the author cuts the last unfinished sentence and writes a new turn that follows from the unfinished event.
    "Impersonate/draft instructions for me" is done in 1.5.0: **Draft Instructions** in the new turn panel (`ImpersonateDialog`,
    `ImpersonateService`, the *Impersonate Prompt* template, the `/impersonate` command that agents do not get) and, in the Arc Outline
    plugin, the same with the data of the current chapter as its plan. Not done: an `/impersonate-chapter` command in the plugin.
16. ~~**Search inside a book**~~ ✅ Done — the box above the branch tree searches the messages of all branches (`_fulltext`,
    `FulltextQuery`) and jumps to a found part; the outline of the story tab searches the active branch by occurrence
    (previous/next, progress, highlighted matches; `StoryBranchSearch`); the book list has an advanced search (`ManuscriptSearchDialog`:
    name, tags to include/exclude with all/any, created/modified range, number of parts, text in the active branch or in all
    branches, results with name and tags). Global search across books' contents in one list (text hits per part) was left out.

17. ~~**Duplicate actions**~~ ✅ Done — duplicate book (`ManuscriptCloneDialog`, `BackupService.cloneManuscript`, optionally only
    the current branch), lorebook (`LorebookService.cloneLorebook`: entries, tags, attributes, subbooks stay linked),
    protocol (`ProtocolService.cloneProtocol`) and provider (`AIService.cloneAI`: API key / extension settings and
    attributes, not shared, only the owner), each with a name dialog (`CloneNameDialog`).

18. **More provider types.** `AIType.OPEN_AI_COMPATIBLE` plus the extension AIs (`AIType.EXTENSION`, `ExtensionAIFactory`,
    see `manual/developer/plugins/ai-providers.md`); the only protocol type is `ProtocolType.CHAT_COMPLETION`.
    - ✅ Native Gemini client: the `google-genai` plugin (Google Gen AI SDK embedded in the plugin only).
    - ✅ Native Anthropic client: the `anthropic` plugin (Anthropic Java SDK embedded in the plugin only, same shape as `google-genai`).
    - ~~Text completion protocol (llama.cpp / KoboldCpp raw prompt with instruct templates)~~ Scratched, not planned.
      Marginalia is structured (turns with instructions, lorebook, summaries, templates) and relies on lore injection,
      which a raw text prompt cannot use well; the one place plain completion could work, inline editing, would be
      spoiled by the lore as well. The hosted APIs have dropped raw completion, and the prompt pipeline is built around
      chat messages. Extra body parameters (samplers) on the chat protocol cover the real need more cheaply.

19. ✅ **Context / token budget view.** `PromptTokenBreakdown` (`domain.components`) records the raw tokens of every
    prompt section (jailbreak, template, lore, summaries, story, user lore, user prompt, reserved), the budget, the
    story parts that were cut and the tokens of each payload message; it is stored with the part
    (`ChatMessage.tokenBreakdown`, `null` for older parts) and *Token Usage* in the part menu shows it with Chart.js
    (`ChartJs` widget, `TokenUsageDialog`). *Branch Tokens* in the settings menu of the story shows the budget of the whole active
    branch before generating, with and without summaries (`ChatMessageService.getBranchTokens`, `BranchTokensDialog`).

20. ✅ **Lorebook improvements.**
    - ✅ Edit lorebook-level tags in `LorebookView` (generation reads them in `fillEntries`, but there's no UI).
    - Bulk edit: ✅ *Mass edit* (`LorebookMassEditDialog`: all entries of the lorebook and its subbooks in one table, move
      to another lorebook) and *Tag entries* (`MultiTagDialog`). ✅ entry search (name, content, note, filter) and bulk delete (selection column, *Delete selected*; desktop) in the entry grid. Not done: a filter
      by tag or by status, bulk delete in the mass edit and on the phone.
    - ✅ SillyTavern **export** (`LorebookService.exportToSillytavern`: sub lorebooks merged, filters as keys, tags as the character filter).
    - ✅ Recursive activation / scan depth (`Lorebook.scanDepth`, 0-5, the raw content of used entries is searched for the
      filters of the others; `LorebookEntry.preventRecursion`) and ✅ activation preview (`LorebookActivationPreviewDialog`,
      `LorebookActivationService.evaluateAll`). Not done: a "only by recursion" entry flag, the flags in the agent commands
      (`/create-lore-entry`), a scan depth per lorebook of the tree.
    - ✅ Activation context (`LorebookEntry.activationContext`: `INSTRUCTIONS`, the default, or `INCLUDE_PARTS` with
      `activationParts` ≥ 1): the filter is also looked for in the newest N parts of the branch; the generation step reads
      only as many parts as the enabled entries need (`partsNeeded`, `recentParts`). Not done: the context in the agent
      commands (`/create-lore-entry`, `/update-lore-entry`), searching summaries or the notes of the parts, a context in the
      mass edit.
    - ✅ A read only *Lorebook* tab in the story editor (desktop) shows the lorebook of the story without leaving it:
      entries with their activation text and Markdown content, optionally with the sub lorebooks (`LorebookPreview`).
      Not done: editing from there, a phone twin, search in it.

21. **Character / persona management.** POV and present characters are free text per turn. A character card
    entity (name, description, avatar via the `Resource` entity (`ResourceService.uploadImage`), linked lorebook entries) with
    pick-lists in the turn editor. SillyTavern character card import. 🧩 possible extension.
    - ✅ Characters per book (name, tagline, note, icon scaled to 200 x 200, colour), relationships between them and a
      relationship map, in the story sidebar; characters can be created from lorebook entries. The **Character
      Tracker** plugin (`marginalia/plugins/character-tracker`) keeps them in the book's attributes, not in an entity.
    - ✅ The Character Tracker can insert the characters and their relationships into the prompt (*In prompt*, depth and role, like the Author's Note).
    - Link characters to lorebook entries (the import only copies the names).
    - ✅ Pick-lists of the characters for the POV and present characters of a turn: the Character Tracker puts a button
      at the end of both fields (`TurnCharacterPicker`) that copies a name into the field; the fields stay free text.
    - SillyTavern character card import.

22. **Sharing between users.** Everything is private except inference providers: ✅ since 1.2.0 an administrator can
    share a provider with all users (read-only for them, the key stays hidden), with per-user token limits and usage
    numbers (`AIService`, `AILimitService`, `UsageService`, migrations `V10`-`V12`). Still open: read-only sharing of
    lorebooks/protocols, or an admin-managed "global" library, plus public read-only links for the Viewer (after fixing
    item 1).

23. ✅ **Localization.** Per-user UI language: `Localization` (`LocalizationImpl`) holds one `LocalizationBundle`
    per `Language` and reads the language of the session from `SessionPoint`; it is set from `UserSetting.language`
    when the workspace opens and changed with the `LanguageSelector` in the workspace footer (saves, then reloads every
    page of the user). Bundles: `LocalizationBundleEN` (default, must be complete), `LocalizationBundleJP`, and
    extensions can add languages (`ExtensionHasLocalization`; example plugin `chineselocalization`). A bundle may lack
    keys (the English text is shown). The hard-coded UI strings found in a sweep (tree node cards, bundle states,
    error dialog, prompt format list, login description) are now keys. Not done:
    - the Japanese and Chinese texts are machine translations nobody who speaks the language has reviewed;
    - ✅ the plugins' own UI texts are translated into Japanese and Chinese with `PluginLocalizationBundle` (the English
      sentence is the key, a `...Localization` class in each plugin); not translated: the descriptions of commands and
      functions (annotation constants), the names plugins register on load (tool suites, Side Query sources);
    - hard-coded text outside the UI classes: default names like "Imported Lorebook" and "Entry" in services, the
      "Contents" title and other fixed words of the exported files (`ExporterBase`), exception messages of services
      (developer-facing, except the ones the UI shows);
    - the manual and the login page are English only (the login page uses the default language, the user's language is
      known only after login);
    - dates use one pattern per language (no per-user format), the date format of the book language is separate.

24a. ✅ **Generation events for extensions** (groundwork for a 🧩 *Generation Inspector* plugin: per-message timeline,
    lorebook activation with skip reasons, context budget, exact payload).
    - ✅ Done: listeners get the generation context through `GenerationControllerEvent` (`getProperties()`,
      `getProperty(key)`, `getRequest()`, `getInput()`, `get/setPrePromptData()`, `get/setPayload()`). Property keys
      are public in `GenerationProperties`, which documents each key's type and the first event it's available in.
    - ✅ Lorebook entry verdicts: `LOREBOOK_VERDICT` (per entry, a listener can replace it and the pipeline reads it back) and
      `LOREBOOK_VERDICTS` (the final record, `LorebookEntryVerdict`: entry + `Verdict`).
    - ✅ Terminal event `AFTER_CLEANUP` for every generation (also stopped and failed ones), with `OUTCOME` and `FAILURE`.
    - ✅ Lightweight read-only observers (synchronous, no executor hop): `StoryGenerationService.addEventObserver`, `GenerationObserver`.
    - ✅ Generation run id: `GenerationProperties.RUN_ID`.

24. **Extension management polish.** ✅ A JAR is verified before it is installed and again after an update, and an
    extension built for another core version is refused (`ExtensionVerifier`, *Verification* column). Extensions can be stopped and
    started without uninstalling (`.STOPPED` flag file next to the JAR) and log into their own file, shown in the *Log*
    dialog (`OsgiService.createLogger`, `ExtensionLogDialog`). A red bar at the top of the workspace tells everybody (the
    administrator which ones, with a tooltip) that installed extensions are invalid and not running
    (`InvalidExtensionsWarning`, `OsgiService.getInvalidExtensions`); it is built at login, so it does not appear for an
    extension that turns invalid while the page is open, and it does not link to *Admin → Extensions* yet. Still missing:
    show bundle errors and dependencies. The plugin API is not frozen in 1.0.

24b. **Summaries overview tools** — the *Summaries* dialog (`SummariesDialog`, `@Extendable`) has a menu bar:
    - ✅ Search and replace in the summaries (*Search and Replace*, `ManuscriptSearchReplaceDialog` with `summariesOnly`,
      `ManuscriptReplaceService.Options.summariesOnly`): the summaries only, also the ones inside a meta summary, the hashes
      of the meta summaries are worked out again (and, with *Revalidate all summaries*, those of every summary of the book).
    - Not done 🧩: export and import of the summaries of a branch (e.g. to edit them in an external editor or move them to
      another book). Editing a summary a meta summary stands in for would invalidate the meta summary (its text is part of
      the hash); an import must update the hashes of the meta summaries it touches (as the replace does).

---

## P3 — Nice to have

70. ✅ **Icons for tabs, minimizing** — every tab but the story has a minimize button, a minimized tab is a round icon (circle with one or two letters, SVG) in a bar that the user moves to any side of the editor; click restores it where it was. Minimized tabs and the side are saved with the book; *Save layout as my default* (settings menu of the story) stores the arrangement in the user settings for books that have none of their own.
71. *Redraft book plugin -> loads book from copy or backup, stores only instructions and part content, allow user to check which has been done and copy instructions*
25. **Writing statistics** — words per day/session, per-book totals, goals. 🧩
26. **Undo for edits** — (*Discard Changes* in the part menu drops an edit that is not saved yet.) message edits overwrite text in place; keep per-message edit history (branching
    covers generations but not manual edits). 🧩 (similar to `lorebookvcs`)
27. ✅ **Keyboard shortcuts** in the story editor, desktop layout only (no phone twin). `+` opens the new turn popover (cursor in the instructions), `Ctrl+Enter` generates, Esc closes it
    and the focus goes back to the story, `Ctrl+.` stops, `s` swipes, `r` regenerates, `e` edits the last part, `Ctrl+S`
    saves the edited part; a key without Ctrl/Alt/Meta fires only when no input has the focus, the shortcuts fire only in the
    visible story tab with no dialog above it, the round button and the generate button show the key in their tooltip. The
    user changes the keys in *Settings → Keyboard shortcuts* (`KeyBindingsEditor`: table, record, reset, clear, warnings for
    duplicates, browser keys and Alt+letter; `UserSetting.keyBindings`), plugins register their actions with
    `ShortcutRegistry` (`ui.shortcuts`). The first defaults were tried in real browsers; also bound: the command bar (`/`), the search of the branch
    (`Ctrl+Shift+F`), the search and replace window (`Ctrl+Shift+H`), *Annotate selection* (`a`) and the next / previous tab (`Alt+PageDown` / `Alt+PageUp`), and the menus of the story (settings, part, text) show the
    keys, built from the `StoryMenuRegistry` where every item is a shortcut action. Done: trying those six keys in Chrome, Firefox and Safari (`Ctrl+Shift+F`, `Ctrl+Shift+H`, `Alt+PageDown`, and the `a` over a text
    selection are the ones to check); the phone layout needs none.
28. ✅ **Mobile layout** — below 768 px (decided once, at page load) the app uses `PhoneWorkspace`: a drawer instead of
    the tab bar and phone twins of the parts (`workspace/phone/parts`) and of the book window
    (`PhoneManuscriptDialog`, tabs in `dialogs/phone/manuscript`), documented in `manual/user/phone.md`. Not done: the
    layout does not switch on resize/rotation without a reload; the lorebook editor is reduced (entry content only);
    *Resources* and the provider/protocol dialogs are the desktop ones; no admin pages on a phone; an extension must
    register its decorators for the phone twins too (`PhoneManuscriptStoryPart`, see `plugins/extendable.md`).
    The story twins share their helpers in `ui.dialogs.story` (generation listener, cards registry, new turn form,
    backup...); the other twins (tree, info, prompt, backup, lorebook parts) still duplicate the desktop classes: move
    shared code out when either is touched next.
29. **Theming** — ✅ light/dark toggle per user (`ThemeSwitch` next to the language selector, `UserSetting.themeMode`, Vaadin
    `Page.setColorScheme`). Still open: editor font/size settings; open pages of the user's other sessions change on reload only.
30. **Import from other tools** — NovelAI/KoboldAI stories still to do. 🧩
    - ✅ SillyTavern chats → book (*Import from SillyTavern* in the book list, `SillyTavernImportService`, `utils.SillyTavernChats`):
      several `.jsonl` files are joined into one tree by their common beginning (branches, checkpoints), swipes become sibling
      parts, the checkbox merges a user message and the answer after it into one part (instructions + text).
      Group chats (same `.jsonl` format) import, but a part does not keep who spoke (the converter plugin does show the names).
      Not done: appending to an existing book, SillyTavern's other chat formats (Kobold, CAI, Risu, Agnai, Ooba), the character card and the lorebook of the chat, importing the `extra`
      data of other SillyTavern extensions.
    - ✅ **SillyTavern Converter** plugin (`marginalia/plugins/sillytavern-converter`): a chat kept with the book to rewrite by hand,
      checkboxes for what was gone through, and turn details prepared by the model for a range (copied into the new turn form
      with a click). Not done: a phone twin, editing the turn details in the tab, the converter wand as a command for the
      command bar and agents, running the wand on a long chat in chunks automatically, an automated test of the tab (only the
      logic has unit tests, the UI was checked by hand).
31. **Image generation / illustrations** for scenes (would use `Resource`; attaching images by hand is done, see
    *Images* in the part menu - generation, sending images to vision models and showing them to the readers of a
    published book in the Viewer are not; the Viewer deliberately shows them to the owner only). Files of deleted
    resources and of replaced content stay in the data folder until the administrator removes them in *Admin → Resource Files*
    (garbage collection: scan, ZIP download, delete; done). 🧩
32. **Text-to-speech read-aloud** in the Viewer. 🧩
33. **Health and metrics endpoint.** ✅ `GET /health` (`HealthServlet`, no login): 200 when the context runs and the database
    answers, else 503; the Dockerfile `HEALTHCHECK` uses it. ✅ Tokens per provider and user are counted since 1.2.0 (`Usage`,
    Inference Providers tab and *Usage per user* in the administration). Missing: other metrics (tokens per book,
    requests per time, a Prometheus endpoint).
34. ✅ **Configuration via environment variables** — `MARGINALIA_DATA`, `MARGINALIA_DEFAULT_LANGUAGE`,
    `MARGINALIA_ALLOW_PERSISTENT_LOGIN`, `MARGINALIA_PERSISTENT_LOGIN_TTL`, `MARGINALIA_TRUSTED_PROXIES` (or the same as
    `-Dmarginalia.*`), over `configuration.properties`; documented in the Docker server page and `architecture.md`. Not done:
    the other `-D` options and the extensions folder (`MARGINALIA_EXTENSIONS` only exists in `install-plugins.sh`).

---

## P4 — Ideas (niche, unplanned)

Not committed work: things that fit the architecture and would set Marginalia apart from a generic chat front end.
Most are 🧩 plugin-sized; the ones that need a core hook say so.

### Long-form continuity

35. 🧩 **Plot-thread ledger ("Chekhov's gun" tracker).** A per-book list of open promises (a locked door, a debt, an
    unanswered letter) with the part that opened it. Threads untouched for N parts are injected into the prompt as a
    reminder; closing one is a button or a side-query verdict ("did this part resolve thread X?"). Stored in the book's
    `attributes`, shown next to the story tree.
36. 🧩 **Story state panel (in-world clock, place, inventory).** `setvar` already keeps local variables per part. Add a
    structured, editable panel over them (date/time, location, who is present, injuries, inventory) that a side query
    updates after each part, plus a timeline view across the branch. Catches "it was night, now it is noon" and items
    that vanish.
37. ✅ **Continuity checker.** Done by the annotations (68): *Annotate with AI* (menu of a part), the batch over a range of parts and
    `/generate-annotation` check a part against the story around it, the lore and the existing notes with an instruction like
    "flag continuity errors", and the findings are notes, not edits (*Redraft* then fixes the accepted ones). Since 1.5.0 the
    *Automatic annotation* item of the tools menu (`AutoAnnotateDialog`, `AutoAnnotate`) does it after every completed generation, per
    book: the model (none is the one of the book), the instructions, on / off, adding the notes without asking, redrafting the part to
    address them and applying that redraft without asking; the windows open and start by themselves, closing one ends the run. The
    check is a separate request, so it evicts the prompt cache only when it shares a server slot with the generation (local llama.cpp
    with one slot): choose another model for it. Not done: a continuity preset for the instructions (free text now), a run after a
    generation made by a command or an agent, a check of the windows on a phone. Not verified: whether the annotate context includes
    the summaries.
38. **Time-scoped lorebook entries.** An entry valid from part X to part Y of a branch (the character is alive until
    chapter 12, the secret is public after the reveal), or with variants per story phase. Core: an activation
    condition that reads the branch position. Avoids the usual "disable the entry by hand and forget".
39. **Knowledge-aware lore ("who knows this").** Entries tagged with the characters that know them; the entry fires
    only when the POV character is among them (the turn already has POV and present characters). Prevents the narrator
    leaking secrets the POV character cannot know.
40. 🧩 **Beat sheet / outline mode.** The author writes the upcoming beats as a list; each turn gets the next open beat
    as an instruction, and a side query ticks it off when the generated part covered it (or flags drift). Beats are
    reorderable, and the unfinished ones survive branching.

### Prose quality

41. **Repetition and cliché radar.** Detect repeated n-grams across the last N parts of the branch (and a user
    blacklist: "shivers down her spine", "a mix of X and Y") and highlight them in the editor. Optionally put the
    offenders into an "avoid" block of the next prompt, or regenerate once automatically. Per-book and per-user lists.
42. 🧩 **Swipe feedback loop.** Record why a swipe was rejected (one-click tags: too long, out of character, purple,
    repeats) and which one was kept. The tags build the "avoid" block of the next prompt, and the accept rate per
    protocol/template/model (sampler values are on the protocol) answers "is this prompt actually better".
43. 🧩 **Style fingerprint.** Compute simple metrics for the passages the author wrote or edited (sentence length,
    dialogue ratio, adverb density, paragraph length) and compare them with the generated parts; show the drift and
    offer to draft the book's *Style* text from the author's own writing.
44. ✅ **Inline rewrite.** Select a passage in a part and give an instruction (shorter, darker, show don't tell);
    the answer is a diff to accept or reject. Done in 1.5.0 as *Redraft selection* in the right click menu of the text
    (`RedraftService.Passage`, `RedraftDialog`): the model gets the whole part with the passage marked plus the story and
    lore around it (not only the paragraph), writes the replacement alone, and the notes whose passage overlaps the
    selection can be given to it. Not done: the `/redraft-part` command and the agent tools take no passage, and the
    menu ☰ of the part does not offer it (it loses the selection). The right click menu was verified in real browsers.
45. **Authorship provenance.** ✅ Partly (1.2.0): `ChatMessage.originalResponse` keeps what the model wrote and *Highlight
    Generated Text* shows a word diff of a part against it (desktop story tab only). Open: the ratio per book, the
    phone and exports. Goal: track which spans of a part are human (typed or edited) and which are model output; show
    the ratio per book, optionally colour the text, and offer it as an export option (some publishers and
    contests ask for it). `ChatMessage.edited` is a single flag today.

### Story tree and editing

46. **Branch tools.** Diff two leaves (shared prefix collapsed, divergent tails side by side), colour/label/bookmark
    branches, mark one path as canon, collapse abandoned swipes, and "prune dead branches" (soft-delete a subtree after
    a preview of what the trash will contain). Fits next to the `TreantTree` tree view.
47. **Rewrite from here.** Place the cursor inside a part, cut the rest and continue as a new turn from that point;
    the cut tail stays as a sibling branch. A cheaper cousin of item 15 that does not need joining logic.
48. **Side-by-side model comparison.** Fan the same turn out to 2-3 providers/protocols in parallel and show the
    results as swipes in columns; the pick is recorded (feeds 42). Needs the generation engine to run several children
    of one prompt without re-running the lorebook step.
49. 🧩 **Autopilot batch.** "Write the next N parts with these instructions" unattended, with stop conditions (word
    or token budget, a side-query judge says the scene ended, an error), written to a new branch so the main one stays
    clean; the result is reviewed and kept or pruned. Needs terminal events for failed/cancelled generations (24a).
68. ✅ **Annotations (notes anchored to a passage of a part).** Done in 1.5.0 (see the CHANGELOG), also the *margin notes* mode and the annotations
    of agents (review, sync, handles); open: aligning each margin note with the line of its passage (client-side layout, now the notes are stacked in text
    order), (*Annotate selection* has a shortcut, `a`). The marks, the popover, the right click menu, the margin and the selection were verified in
    real browsers (the scripts of `JavascriptLibrary` are not unit tested). Notes for the author ("weak scene", "timeline wrong",
    "revisit"), like comments in Word, plus findings of AI and agents. Core feature (anchoring, backups and the editor
    need hooks); checkers stay plugins (37 posts its findings as annotations, 44 can read them). Decided design:
    - **Entity** `Annotation` (a real table + Flyway migration, the sidebar queries by book / status / part): part,
      `start`, `end` (offsets in the *source* text of `ChatMessage.response`), the quoted text and a little context
      before and after, text, status (open / resolved / dismissed / orphaned), source (author, AI, agent, plugin),
      type/colour, and a *send to prompt* flag (off by default, annotations are author-facing). Owned, soft deleted,
      `@CleanupReference(OWNED_BY)` the part, in backups and exports. Overlapping annotations are separate rows.
    - **Anchoring, not inline markers.** The text stays clean (markers in the stored text would leak into prompts,
      summaries, exports, search and plugins). On every save of a part the offsets are shifted through a diff of the old
      and the new text, like Word's range markers: an edit inside the range grows or shrinks it, before it moves it,
      text typed exactly at a boundary lands outside, a range that is deleted completely becomes `orphaned` (kept, the
      author can re-attach or dismiss it). After a wholesale replace (the diff means nothing) the quote is searched
      with its context, fuzzy; no match = orphaned. The same quote finder serves the AI annotate (below).
    - **Where the re-anchoring goes** (the code was read, not changed). `ChatMessageServiceImpl.storeResponse` is the
      choke point of the author's edits (`updateResponse`, the redraft dialog, `PartEditCommands`, `RedraftCommands`,
      the agent merge all end there). Not through it, to handle each: `mergeIntoPrevious` (sets the response itself: the
      annotations of the later part move to the earlier one, offsets + length of the earlier text + the join),
      `AgentMergeServiceImpl` (`message.setResponse` next to its own `storeResponse`), the search-and-replace service
      over a book (`EntityTextSearch` writes `@Fulltextable` fields; verify whether it calls `storeResponse`), a
      regeneration of the part (`restartStreaming` clears the text, then the stream fills it: the quote search after
      the generation), and `branch()` (copies the part: copy the annotations; a swipe is a new text, no copy). Put the
      shifting in one place (an `AnnotationAnchors` helper called from `storeResponse`) and cover every path above with
      a test. `utils.TextDiff` is sentence-level and has no offsets, no use for this; `GeneratedTextHighlighter`
      already has an offset-preserving word tokenizer + LCS (`matchGenerated`): extract that into a utility that maps an
      old offset to the new one, and share it.
    - **Showing them.** The part is a `Markdown` component fed a markdown *source* string that is pre-processed with
      inline `<mark class>` (generated-text diff in `GeneratedTextHighlighter`, search hits in `StoryBranchSearch.mark`),
      so source offsets fit the rendering. The three layers cannot be chained (annotation offsets would be shifted by
      the HTML of the earlier layer, and the generated-text diff would see the tags): collect all ranges in raw offsets
      and emit once (a small `MarkupInserter`, with the per-line wrapping and the markdown line-prefix rule of
      `GeneratedTextHighlighter.appendRun`). Spike first: whether the `Markdown` sanitizer keeps `data-*` attributes
      (else a class per annotation) and a click on a mark (script in `JavascriptLibrary`, an event to the server, like
      `fireChipClickEvent`). A gutter badge with the count per part, a side panel listing the open annotations of the
      book (click = jump to the part), and *Annotate selection*.
    - **Display modes.** Default: the text keeps its full width, the annotated range is only marked and the note
      opens in a **popover** on click (tap on the phone). The mark is a wavy underline (a block would clash with the
      background of the generated-text `<mark>`; a block is only the hover / active state), coloured by type or source;
      overlapping ranges differ in colour or thickness. Resolved annotations leave the text and stay in the panel
      (a *show resolved* switch), orphaned and part-level ones have no range: the badge and the panel only. A click on an overlap lists every annotation that covers the
      point. Optional: **notes in the left margin**, next to the text, switched like *Show book styles*
      (`Manuscript.showBookStyles` adds `CssStyles.MARKDOWN_MANUSCRIPT_STYLES` to `mainLayout` in `applyBookStyles`):
      a toggle in the story settings menu that picks a class; decide whether it is per book (like that flag) or per
      user (it is a reading preference, closer to the theme switch). The phone has no room for a margin: popover only.
      Both modes show the same `AnnotationView` component (text, source, status, resolve / edit / delete, *send to
      prompt*, *redraft with this*), which the side panel lists too. Notes: the marks are HTML inside the `Markdown`
      content, not Vaadin components, and a `Popover` needs a component as its target: one popover per card whose
      target is a small anchor element moved to the rectangle of the clicked mark (`JavascriptLibrary.getPositionAndSize`
      is the precedent) - part of the spike. The margin is a real layout change, not only CSS: first version a column
      per card with the notes of the part in text order, hovering a note or its mark lights up the other; aligning each
      note with the line of its mark (client-side layout, stacked down on overlaps, redone on resize and re-render) later.
      A count badge in the header bar of each card shows in both modes and takes no width from the text.
    - **Creating from a selection.** In the editor (`TextArea`) `selectionStart/End` are exact source offsets. In the
      rendered view the selection is rendered text: send the selected text and the text around it from the DOM and find
      it in the source with the quote finder (ambiguous = ask to select more or use the editor). The desktop story tab
      (`ManuscriptStoryPart`) and the phone one (`PhoneManuscriptStoryPart`) each have their own card: both, with the
      `@PublicAPI` annotations (`PublicAPISignatureTest`).
    - **Commands.** A suite `annotation-create / -list / -update / -resolve / -delete` in
      `commands/manuscript/write` (and a read one): the command bar, agents (tools by the `@Command` metadata) and plugins
      get the API for free. They save directly, no review (the caller is the reviewer; an agent can annotate a whole book
      alone, an alternative to `report`).
    - **AI annotate** (`AnnotateService`, shaped like `RedraftService`). Input: the part, an instruction ("flag
      continuity errors", "weak dialogue", "contradicts the lore"), optionally an AI and a protocol (`GenerationOptions`,
      as redraft); the context is the one of the redraft (story before, summaries, story after, lore) and an
      `annotatePrompt` template with the usual fallback (book, Settings → Templates, built-in). Own default AI +
      protocol in the user's settings (annotating is analysis, usually not the prose model), overridable per run.
      Nothing is stored: the proposals go to a **review list** (accept / reject each) and only the accepted ones are
      saved (UI only). The model never gives offsets (it cannot count): it *quotes* the passage, and the quote finder
      anchors it; a quote that is not found becomes a part-level annotation marked *could not locate*, not dropped.
      Output modes, chosen per call: (1) structured, `InferenceService.infer(..., Class<T>)` with a schema and
      `SchemaCheck` when the AI supports it; (2) delimited text (`> quoted passage` + the note) with a lenient parser for
      prose models; (3) two-stage: the prose model writes a free critique, a second (cheaper) AI + protocol extracts the
      annotations. Default: (1) when the AI supports it, else (2). One retry on a parse or schema failure, then the raw
      answer is shown so the work is not lost. Scope: one part; a branch-wide run is a loop (the plugins).
    - **Redraft link.** A `{{annotations}}` variable (the open annotations of the part: quote + note) in the redraft
      prompt, so *redraft to address the open annotations* is one step; resolved on accept.
    - **Build order:** (1) entity, migration, service, backup/export, re-anchoring + tests (the risky part); (2) the
      command suite; (3) UI: highlight, gutter, panel, create from selection (after the spike); (4) `AnnotateService`,
      prompt, default AI setting, review dialog; (5) redraft variable, localization EN/JP/ZH, manual (user + developer),
      CHANGELOG.
    - **Depends on item 69** (the story as a dock tab): the annotation panel is then one more dock tab the user places
      beside the story, which may make the margin mode unnecessary. Do 69 first.
69. ✅ **The story as a tab of the dock (free layout of the desktop story editor).** Done in 1.5.0 (see the CHANGELOG);
    open: the per-user default layout below. Today `ManuscriptStoryPart` has a
    fixed `SplitLayout`: the `TabDock` (`leftBar`) on the left (20 %, `$desktopManuscriptStoryPartSplitPosition`) and the
    story pane (card `ScrollPanel` + bottom controls) on the right, the one thing the user cannot move. Make the story
    pane a tab of the dock, so the whole screen is the user's layout (story in the middle with the outline and the
    lore around it, story above a lorebook, a narrow story on a wide monitor), one layout model and one stored config.
    - **Tab.** Id `story` (a constant like `CHAPTERS_TAB` / `LOREBOOK_TAB`), content = the whole pane (`centerLayout`:
      the cards *and* the bottom controls: menu bar, command bar and its result, the action button; the new-turn popover
      follows its button). It cannot be closed or lost; the layout code always puts it somewhere. A story alone in its
      column keeps a thin tab strip (the drag handle), one row of height, for consistency with the other tabs.
    - **Migration.** `TabDock.setLayout` puts a tab the layout does not know into the first sheet, which would stack
      the story behind the outline of every existing book. On load, a stored layout without `story` is turned into
      the old arrangement: its columns scaled to the old splitter share (`SPLIT_POSITION_ATTRIBUTE`, default 20 %), plus
      a story column with the rest; then the split-position attribute is no longer written (read once, removed with the
      next save). `TabDockTest.storyPartKeepsTheSplitPositionInTheManuscript` is rewritten for this. A book with no
      stored layout gets the default layout: the old arrangement (outline and lorebook sheets 20 %, story 80 %).
    - **Restore default layout.** A menu item in the options (cogs) menu of the story editor (`createCogsMenuItem`,
      desktop only) resets the dock to the default layout after a confirmation. It resets the layout only: the
      selected tabs, the contents and the plugin tabs stay (those are placed by the same rule as a new tab).
    - **Layout per user (optional, can come after the first version).** The layout is stored per book, and once it is
      the whole screen, rearranging it for every new book is tedious: a per-user default layout (a user setting) used
      for books that have none, with *Use this layout for all books* in the options menu. *Restore default layout*
      then restores that default (and the built-in one when there is none).
    - **Careful about.** (1) A story tab stacked behind another tab is `display:none` and measures as zero: the lazy
      loader (`StoryLazyLoader`), the stay-at-bottom logic while streaming (`scrollToBottomIfAtBottom`), `watchEdge`,
      `rememberReaderPosition` and the scroll-to-part code assume a visible, measurable panel; test a generation that
      streams into a hidden story and the return to it. (2) The things attached to `mainLayout` move to the new root:
      the shortcut events (`installShortcuts`, `fireShortcutEvents`), the `MARKDOWN_MANUSCRIPT_STYLES` class
      (`applyBookStyles`) and the `lang` attribute (`applyLanguage`). (3) Plugins (`character-tracker`, `arc-outline`,
      ...) read the field `leftBar` by reflection and add their tabs to it: keep the field name (`@PublicAPI` does not
      cover field names) or rename it together with the plugins. (4) A minimum width for the story cell, so a splitter
      cannot squeeze it to nothing. (5) `@PublicAPI` of the changed `ManuscriptStoryPart` / `TabDock` methods
      (`PublicAPISignatureTest`), the user manual page of the story editor and `manual/developer/ui.md` (the `TabDock`
      section), CHANGELOG (*Changed*: the layout of the story editor, with the migration). The phone editor
      (`TabDock(false)`) is not touched.

### Lorebooks and retrieval

50. **Semantic lore activation.** Keyword/tag activation misses paraphrases. Embed entries (and, optionally, older
    story parts that fell out of context and have no summary) through an OpenAI-compatible `/embeddings` endpoint and
    activate by similarity next to the existing modes; show the score in the activation preview (20). Storage: a blob
    column or `sqlite-vec`; re-embed on change (hash of the content, like the meta summaries).
51. **Lore usage report.** Per entry: how often it fired in this book, last activation, tokens it cost, and the entries
    that never fire or always fire (dead weight). Needs the skip reasons from 24a. Turns lorebook pruning from guesswork
    into a list.
52. 🧩 **Lore suggestions from the story.** Scan the branch for proper nouns and recurring noun phrases with no entry and
    propose stubs; a click hands one to the Lore Generator. Also the reverse: entries whose name never appears in the
    book.
53. 🧩 **Relationship graph.** Draw the lorebook as a graph (entries as nodes, mentions/links as edges, tag colours) to
    see orphans and hubs; clicking opens the entry.
54. 🧩 **Markdown vault mirror.** Two-way sync of a lorebook (and optionally chapters) with a folder of Markdown files with
    front matter (name, tags, mode), so it can live in Obsidian or git. Complements `lorebookvcs`, which versions inside
    the database.

### Prompt engineering

55. **Prompt dry run and diff.** Build the exact payload of the next turn without sending it, and diff it against the
    payload of any earlier part (what changed: which entries joined, which part was cut, which summary swapped).
    Groundwork exists (`PromptTokenBreakdown`, 24a); this is the "why did the model suddenly forget X" tool. It should
    also show where the payload first differs from the previous turn's (providers and llama.cpp reuse the longest
    identical prefix) and why: the active lore set changed, the context window slid, a summary or a note changed.
56. ~~**Cache-friendly prompt ordering.**~~ Scratched, not planned. A prefix cache miss is inherent to lore chosen per
    turn: the set of active entries follows the instructions of the turn, and the sliding context window moves the start
    of the story as well. The ordering choice already exists per entry (`BEFORE_USER_PROMPT` puts the lore after the
    story and leaves the prefix alone, `IN_LORE_BLOCK` puts it in the system message); the diagnostic is part of 55.
57. **Template playground.** Render a template against a chosen part with the context variables listed, errors with
    line numbers, and macro autocomplete (the `DESC_MACRO_*` texts exist). Optional saved cases that run as tests
    when the template is edited.
58. **Per-turn sampler override.** Quick preset switch for one turn (dialogue: higher temperature; action: lower)
    without editing the protocol, remembered as a per-book list of named presets.
59. **Cost and budget ledger.** Partly done in 1.2.0: tokens per provider and user, and per-user limits (unlimited / token
    limit / not available, daily, weekly or monthly reset) on shared providers. Open: price per million tokens on the
    provider, tokens per book/month, a soft cap for the own providers that warns or blocks, and the cost of the current
    book in its info tab.

### Reading, publishing, exporting

60. **Serial publishing.** Drip-publish a book to the Viewer on a schedule (one chapter a day), an Atom feed per
    published book, and reading position per reader. Uses the cron setup of the scheduled backups.
61. **Export targets.** AO3-ready HTML (work skin), Markdown with front matter for static site generators, a plain
    chat-log export for SillyTavern, and an Obsidian-style vault with the lore. Item 10 covers the missing pieces of
    the current exporters.
62. 🧩 **Translate a branch.** Translate the active branch into another language into a new book, with the lorebook
    names as a glossary so character and place names stay consistent. The book language is already a separate setting.
63. **Dictation in the turn editor.** The browser's speech recognition filling the instruction box (phone and
    desktop), the input side of item 32.

### Operations and integration

64. 🧩 **Notifications when a generation ends.** Slow local models take minutes: webhook (ntfy, Discord, Pushover) and, in
    the desktop app, a tray notification when a generation finishes, fails or a batch (49) is done.
65. 🧩 **MCP server.** Expose lore search, summaries and the active branch read-only (write behind a per-user token) to
    other tools such as an editor agent or a coding assistant, without adding a REST API to the core.
66. **Off-site backup target.** Scheduled database backups can only stay in the data folder. Add an upload step (S3,
    WebDAV, or a post-backup shell hook) with optional encryption, and a restore-test button that opens the newest
    backup in a scratch database and reports row counts.

### Development

67. ~~**Test speed on other machines (the Mac).**~~ ✅ Measured 2026-10-21 on the Apple M4 (10 cores, 16 GB, JDK 26):
    2:20 min on one JVM, 1:27 min on the default 3 JVMs, 1.8 GB peak for the 3 JVMs, all 2,558 tests passed (so no test
    counts on an empty database or on the order of the classes there). The row is in `manual/developer/testing.md`.
    Still unmeasured: `-Dtest.forks=2` or `0.25C` on a laptop (not needed with 1.8 GB), caching the encoding that
    `JavaTokkitStrategy` loads for every test (about 8% of the samples of one profile) and the cost of a context rebuilt
    by `@DirtiesContext` in a warm JVM.
