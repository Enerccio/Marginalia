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
     arm64/x64 (tests once, smoke test per platform, archives attached to `v*` releases). Still missing: signing /
     notarization (macOS Gatekeeper, Windows SmartScreen), an app icon, installers (dmg/msi/deb), and the release
     version is the pom version, not the tag.

9. **WAR size / dependency hygiene.** The WAR was ~200 MB. `tika-parsers-standard-package` + `tika-async-cli`
   were unused and are removed (the WAR is ~185 MB; the export tests declare PDFBox themselves). `Resource` is now used for the image attachments of parts (story editor, exports) and listed in the Resources tab. Logging is on
   `slf4j-log4j12` + `reload4j` (log4j 1.x API) — move to logback or log4j2. Remove what's unused, or finish it (see 21). Still open: the logging move.

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

15. **Merge into previous part** (instead of a "continue" request type) — chat completion has no reliable "continue":
    prefilling a trailing assistant message is ignored or rejected by many OpenAI-compatible backends and reasoning
    models, and tends to repeat the text. A new turn ("continue from where the last part stopped") already works;
    what is missing is joining the result. Add a part action that appends the selected part's text to the part before
    it, recounts words/tokens and removes the emptied part. Decide: which part's `attributes` (variables, extension
    data) win, what happens if the previous part has other children (branches), and invalidating a summary that
    covered either part. Optional helper: pre-fill the next turn's instructions with a "continue" text when the last
    part ended on a length cutoff. Separately: "impersonate/draft instructions for me".
16. ✅ **Search inside a book** — the box above the branch tree searches the messages of all branches (`_fulltext`,
    `FulltextQuery`) and jumps to a found part. Missing: global search across books.

17. **Duplicate actions** — duplicate book (settings only / with story), lorebook, protocol, provider.
    Most of it exists via backup clone and lorebook export/import.

18. **More provider and protocol types.** Only `AIType.OPEN_AI_COMPATIBLE` and `ProtocolType.CHAT_COMPLETION` exist.
    - Text completion protocol (llama.cpp / KoboldCpp / Ollama raw prompt with instruct templates).
    - Native Anthropic / Gemini clients (tokenizer strategies for LiteLLM-Anthropic already exist).
    - 🧩 Each new provider could be a plugin if `InferenceServices` gets a registration API like decorators have.

19. ✅ **Context / token budget view.** `PromptTokenBreakdown` (`domain.components`) records the raw tokens of every
    prompt section (jailbreak, template, lore, summaries, story, user lore, user prompt, reserved), the budget, the
    story parts that were cut and the tokens of each payload message; it is stored with the part
    (`ChatMessage.tokenBreakdown`, `null` for older parts) and *Token Usage* in the part menu shows it with Chart.js
    (`ChartJs` widget, `TokenUsageDialog`). Not done: a view of the whole book's budget before generating.

20. **Lorebook improvements.**
    - ✅ Edit lorebook-level tags in `LorebookView` (generation reads them in `fillEntries`, but there's no UI).
    - Entry search/filter and bulk enable/disable/delete in the entry grid.
    - SillyTavern **export** (import exists).
    - Recursive activation / scan depth (entries activating other entries) and activation preview
      ("which entries would fire for this prompt").
    - ✅ A read only *Lorebook* tab in the story editor (desktop) shows the lorebook of the story without leaving it:
      entries with their activation text and Markdown content, optionally with the sub lorebooks (`LorebookPreview`).
      Not done: editing from there, a phone twin, search in it.

21. **Character / persona management.** POV and present characters are free text per turn. A character card
    entity (name, description, avatar via the `Resource` entity (`ResourceService.uploadImage`), linked lorebook entries) with
    pick-lists in the turn editor. SillyTavern character card import. 🧩 possible extension.
    - ✅ Characters per book (name, tagline, note, icon scaled to 200 x 200, colour), relationships between them and a
      relationship map, in the story sidebar; characters can be created from lorebook entries. The **Character
      Tracker** plugin (`marginalia/plugins/character-tracker`) keeps them in the book's attributes, not in an entity.
    - Link characters to lorebook entries (the import only copies the names).
    - Pick-lists of the characters for the POV and present characters of a turn.
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
    - plugins still have English-only texts of their own (they can't add keys to the `L` enum; a plugin can use
      `Localization.getCurrentLanguage()` for its own tables);
    - hard-coded text outside the UI classes: default names like "Imported Lorebook" and "Entry" in services, the
      "Contents" title and other fixed words of the exported files (`ExporterBase`), exception messages of services
      (developer-facing, except the ones the UI shows);
    - the manual and the login page are English only (the login page uses the default language, the user's language is
      known only after login);
    - dates use one pattern per language (no per-user format), the date format of the book language is separate.

24a. **Generation events for extensions** (groundwork for a 🧩 *Generation Inspector* plugin: per-message timeline,
    lorebook activation with skip reasons, context budget, exact payload).
    - ✅ Done: listeners get the generation context through `GenerationControllerEvent` (`getProperties()`,
      `getProperty(key)`, `getRequest()`, `getInput()`, `get/setPrePromptData()`, `get/setPayload()`). Property keys
      are public in `GenerationProperties`, which documents each key's type and the first event it's available in.
    - Lorebook entry skip reasons are only logged in `ProcessLorebookStep`; store them as a property.
    - Terminal events for failed and cancelled generations (currently only success emits `AFTER_INFERENCE`).
    - Lightweight read-only observers (synchronous, no executor hop) for hot events like `CHUNK_RECEIVED`.
    - Generation run id on the event to correlate data from concurrent generations.

24. **Extension management polish.** ✅ A JAR is verified before it is installed and again after an update, and an
    extension built for another core version is refused (`ExtensionVerifier`, *Verification* column). Extensions can be stopped and
    started without uninstalling (`.STOPPED` flag file next to the JAR) and log into their own file, shown in the *Log*
    dialog (`OsgiService.createLogger`, `ExtensionLogDialog`). A red bar at the top of the workspace tells everybody (the
    administrator which ones, with a tooltip) that installed extensions are invalid and not running
    (`InvalidExtensionsWarning`, `OsgiService.getInvalidExtensions`); it is built at login, so it does not appear for an
    extension that turns invalid while the page is open, and it does not link to *Admin → Extensions* yet. Still missing:
    show bundle errors and dependencies. The plugin API is not frozen in 1.0.

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
27. **Keyboard shortcuts** in the story editor (generate, stop, swipe, regenerate, edit), so most of the writing loop
    works with the keyboard and tabs. First step: a shortcut on the open story tab that opens the new-turn popover
    (`NewTurnForm.focusInstructions` already puts the cursor into the instructions), Ctrl+Enter in the popover to
    generate and Esc to close it, the focus going back to the story afterwards. Single keys such as `+` fire only when
    no text field (or other input) has the focus, the ones that would collide with typing need a modifier. Check the
    browser and OS collisions (Ctrl+N/T/W are the browser's, Option+letter types characters on a Mac) in a real browser
    before choosing the defaults, show the key in the tooltip of the button it triggers, and make the shortcuts active
    only for the open story tab. The command bar (`/`), search and replace and next/previous tab are the next
    candidates; the phone layout needs none.
    **Key binding dialog.** The defaults are not final, so the user changes them in a dialog (user settings): a table of
    actions with their current key, a field that records the next key combination pressed, a reset to the default and a
    warning when two actions get the same key or the key is one the browser keeps. The bindings are a per-user setting
    (an `@ExtendedAttribute` of `UserSetting`, travels with backups); plugins register their actions with a default key
    so they show up in the same dialog.
28. ✅ **Mobile layout** — below 768 px (decided once, at page load) the app uses `PhoneWorkspace`: a drawer instead of
    the tab bar and phone twins of the parts (`workspace/phone/parts`) and of the book window
    (`PhoneManuscriptDialog`, tabs in `dialogs/phone/manuscript`), documented in `manual/user/phone.md`. Not done: the
    layout does not switch on resize/rotation without a reload; the lorebook editor is reduced (entry content only);
    *Resources* and the provider/protocol dialogs are the desktop ones; no admin pages on a phone; an extension must
    register its decorators for the phone twins too (`PhoneManuscriptStoryPart`, see `plugins/extendable.md`).
    The story twins share their helpers in `ui.dialogs.story` (generation listener, cards registry, new turn form,
    backup...); the other twins (tree, info, prompt, backup, lorebook parts) still duplicate the desktop classes: move
    shared code out when either is touched next.
29. **Theming** — light/dark toggle per user, editor font/size settings.
30. **Import from other tools** — SillyTavern chats → book with branches, NovelAI/KoboldAI stories. 🧩
31. **Image generation / illustrations** for scenes (would use `Resource`; attaching images by hand is done, see
    *Images* in the part menu - generation, sending images to vision models and showing them to the readers of a
    published book in the Viewer are not; the Viewer deliberately shows them to the owner only). Files of deleted
    resources and of replaced content stay in the data folder: a garbage collection for them is still to be done. 🧩
32. **Text-to-speech read-aloud** in the Viewer. 🧩
33. **Health and metrics endpoint.** ✅ The Dockerfile has a `HEALTHCHECK` (bash opens a socket to port 8080, a 2xx/3xx
    answer is healthy). ✅ Tokens per provider and user are counted since 1.2.0 (`Usage`, Inference Providers tab and
    *Usage per user* in the administration). Missing: a dedicated health endpoint and other metrics (tokens per
    book, requests per time).
34. **Configuration via environment variables** — today it's `configuration.properties` plus
    `-Duser.home`. Document the keys and allow env overrides (data dir, default language, cookie TTL).

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
37. 🧩 **Continuity checker.** After a part is accepted, a side query compares it with the activated lore and the
    summaries and lists contradictions (eye colour, who knows what, dead characters talking). Findings are annotations
    on the part, not edits. The `reviewer` plugin is the natural base.
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
44. 🧩 **Inline rewrite.** Select a passage in a part and give an instruction (shorter, darker, show don't tell);
    only the selection is sent (with its surrounding paragraph), the answer is a diff to accept or reject. The
    existing flow regenerates whole parts.
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
    Groundwork exists (`PromptTokenBreakdown`, 24a); this is the "why did the model suddenly forget X" tool.
56. **Cache-friendly prompt ordering.** Providers and llama.cpp reuse the longest identical prompt prefix. Lore
    that changes every turn near the top breaks the cache. Show in *Token Usage* where the prefix first differs from
    the previous turn's payload, and add an ordering mode that keeps the stable parts first and the volatile lore
    last. Real money/latency win on long books.
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
