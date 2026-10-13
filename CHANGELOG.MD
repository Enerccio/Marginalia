# Changelog

All notable changes to Marginalia are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Plugin: **Name generator** — the model can call `list_name_categories` and `generate_names` to get names from Markov
  chains instead of inventing them. About 1200 categories, trained at build time from the Onomastikon, fantasygen,
  Corpora and markov-namegen-lib (the build clones them, only the probabilities are committed). The length of the chain
  and the usual lengths of the names are set per category in `src/build/chains.json`, written by the tuning step
  (`mvn exec:exec@tune-chains`).
- Lore Generator: **Name tools** gives the model the functions of the Name generator for the categories you check, in a
  lazy table with a filter (the checked categories stay in it). The checked categories are remembered per entry with
  the other options of the generation. The default hint lets the model modify or remix the names it gets.

### Changed

- Lore Generator: the dialog is split into smaller classes (`NameToolsDialog`, `SavePromptAsDialog`,
  `AttachmentPanel`, `GenerationPane`).

### Fixed

- Lore Generator: the Name generator is found by reflection and not imported, so it works when it is installed after the
  Lore Generator, and again after it was unloaded and installed again.

## [1.1.0] - 2026-10-12

No database migration: a 1.0.0 database opens unchanged.

### Added

- Phone layout below 768 px: a phone workspace with a drawer, a phone book window, a phone story editor and phone
  versions of the book, lorebook, provider, protocol, settings and trash pages. The layout is chosen at page load.
  Not available on the phone: administration pages, the full lorebook editor and *Resources* (see the user manual).
- Per-user UI language: a language selector in the footer (only installed languages are listed), stored in the user
  settings and applied to all open sessions of the user. Japanese is built in.
- Extensions can add languages (`ExtensionHasLocalization`). The new Chinese Localization plugin is the worked example.
- Token breakdown: the tokens of every message of the final prompt are recorded with each generated part (parts from
  before have none) and shown in new Token Usage views with charts.
- Message attachments (images, audio, other files) can be passed to the model by the inference services. Used by the
  Lore Generator plugin; whether the model uses them depends on the model.
- Extension management: an extension can be stopped and started again without uninstalling it (`<jar>.STOPPED` flag
  file), and each extension has its own log file, rotated at 2 MB, shown in Admin → Extensions → Log. Plugins create
  their logger with `OsgiService.createLogger(bundle)`.
- Extensions can ship their own CSS (`ExtensionHasCss`), removed again when the extension is unloaded.
- Plugin: **Lore Generator** — generates the content of a lore entry from the existing lore, with saved prompts,
  attachments and a compaction step.
- Plugin: **Instruction Exporter** — copies the scene setting, POV character and present characters of the active
  branch to the clipboard.
- Plugin: **Chinese Localization** — Chinese UI texts as an extension.
- Root `pom.xml` as the parent of the application and all plugins: the version is defined in one place and
  `mvn package` in the root builds everything.
- `plugins/Dockerfile`, `plugins/docker-compose.yml` and `rebuild.sh` / `rebuild.bat` to build the plugins in a
  container and restart the stack with them.
- Documentation: user pages for the phone layout and the new plugins; developer pages for the new extension
  interfaces, token breakdown, message attachments and the parent POM.

### Changed

- Only the newest version of an extension is loaded at startup; older versions are moved to a separate folder.
- Story redraws keep the open tab sheet and the open reasonings.
- UI texts that were hard-coded (tree node cards, bundle states, error dialog button, prompt format list, login
  description, `ERROR` fallbacks) are localization keys.
- Inline styles were replaced by CSS classes (`shared-styles.css`, `CssStyles`) and all browser-side JavaScript moved to
  `ui.JavascriptLibrary`.
- The bundled plugins use the extension logger instead of `LoggerFactory`.
- Plugins built for 1.0.0 must be rebuilt from the 1.1.0 source tree; the plugin API is not frozen and the extension
  verifier shows plugins that no longer match as *Invalid*.

### Fixed

- A page reload logged out every user who had not chosen *Save login*; a user who is already logged in this session is
  now kept.
- `SessionManager` kept the session-scoped `user` proxy, so user filters running on another thread failed with
  `ScopeNotActiveException`; it now stores a plain copy taken at login.
- The plugin Docker image no longer runs the Vaadin frontend build.
- Summaries: the inference now starts after the transaction that creates the summary has committed. A fast answer
  could be saved before the new summary row was visible, which failed with `OptimisticLockException` and left the
  summary request unanswered (seen as intermittent `MetaSummaryTest` timeouts in CI).

### Known issues

- The Japanese and Chinese texts are machine translations that a native speaker should review.
- The phone layout does not follow a rotation, and `PhoneManuscriptStoryPart` duplicates much of
  `ManuscriptStoryPart`.
- Plugins' own texts, fixed words in exported files and default object names created by services are English only.
- A restored story part does not get its former children back.

## [1.0.0] - 2026-10-10

Initial release.

- Branching story tree (regenerate, swipe, branch), lorebooks with tag, text and regex activation and nested lorebooks,
  summaries and meta summaries, Handlebars and SillyTavern-macro prompts, SillyTavern import.
- Search across all branches, story export (TXT, Markdown, HTML, DOCX, PDF, EPUB), images on story parts.
- OpenAI-compatible providers with timeout, retries, classified errors and a connection test.
- Per-book backups, scheduled database backups with staged restore, a copy of the database before a migration, trash
  with restore.
- Login throttling per user and per address, encrypted API keys, server-side admin checks.
- OSGi extension system with verification of bundles, and the Author's Note, Chapter Marker, Lorebook VCS, Reviewer and
  Side Query plugins.
- Docker image (amd64, arm64), WAR and desktop archives for five targets.

[Unreleased]: https://github.com/Enerccio/Marginalia/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/Enerccio/Marginalia/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Enerccio/Marginalia/releases/tag/v1.0.0
