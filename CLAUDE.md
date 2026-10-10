# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Marginalia: a self-hosted tool for co-writing long fiction with OpenAI-compatible LLMs (branching story tree, lorebooks, summaries, Handlebars/SillyTavern-macro prompts). One Java 25 web app (Vaadin Flow UI, Spring XML without Spring Boot, Hibernate + SQLite + Flyway), packaged as a single WAR. Runs in Docker (Jetty) or as a desktop app (`marginalia/src/desktop`, `DesktopLauncher`, JDK-only, never in the WAR). The developer docs in `manual/developer/*.md` are detailed and authoritative (architecture, building, testing, generation pipeline, plugins).

## Commands

All from `marginalia/` (the Maven project; needs JDK 25 + Maven 3.9, build from a git clone — the build reads git info):

```sh
mvn package                  # build + tests; production WAR in target/marginalia-1.0.0.war
mvn package -DskipTests
mvn install -DskipTests      # also installs the classes JAR into ~/.m2 (required before building plugins)
mvn test -Dtest=LorebookActivationTest        # one class
mvn test -Dtest='MacroRenderingTest#name'     # one method
mvn test -Dtest='Backup*Test'                 # pattern
export MAVEN_OPTS="-XX:+EnableDynamicAgentLoading -Djdk.attach.allowAttachSelf=true -Duser.home=/path/to/dev-home"
mvn -DskipTests jetty:run    # dev mode at http://localhost:8080/
docker compose up --build    # from repo root; data in ./data
```

Use a separate `-Duser.home` for dev runs: data lives in `~/.marginalia` (also the desktop app's folder). Note `user.home` also moves Maven's repo; add `-Dmaven.repo.local=$HOME/.m2/repository`.

Plugins (`marginalia/plugins/{chaptermarker,lorebookvcs,reviewer,sidequery,authorsnote}`) are separate Maven projects (OSGi bundles): after `mvn install` of the app, run `mvn package` in the plugin dir, then load the JAR in Admin → Extensions. They must be built from the same source tree as the app.

There is no lint step. First build is slow (Vaadin downloads Node.js and npm packages).

## Architecture (big picture)

- **No REST API / separate frontend.** Vaadin components live server-side and call Spring services directly. UI code is under `ui.*`, services in `domain.service(.impl)`, JPA repositories in `domain.repository`, entities in `domain.model`, all in `com.github.enerccio.marginalia`.
- **Spring wiring is explicit XML** (`src/main/resources/META-INF/spring/*.xml`, `webapp/config/`). A new service/repository needs a bean in `services-config.xml` — component scan does not pick up service impls. A new entity must be added to `webapp/config/persistence.xml` and needs a Flyway migration.
- **UI classes are created with `new`**, annotated `@Configurable`, and get `@Autowired` fields via **compile-time AspectJ weaving** (`aspectj-maven-plugin`/`ajc`). Plain javac (e.g. an IDE not set to Ajc) compiles fine but every injected field is `null` at runtime. Keep the `-parameters` and `preserveAllLocals` compiler options: extensions read method args/locals by name.
- **Transactions** come from Spring CGLIB proxies via `@CommonTx` / `@CommonTxReadOnly` / `@NoTx` — self-calls within a service are not transactional.
- **Ownership:** every entity belongs to a user (`OwnedEntity`); use `findAllForUser()` / `findForUser(uuid)` from `OwnedService`, never trust raw ids. Deletes are soft (`deleted=true`); admin Cleanup purges following `@CleanupReference`. Entities have `id` plus a stable `uuid` used in URLs/backups/exports.
- **Schema:** Flyway migrations in `src/main/resources/migration/V<n>__*.sql`; Hibernate only validates (`hbm2ddl=validate`), so entity changes without a migration break startup.
- **New entity fields:** prefer `@ExtendedAttribute` (`domain.traits`; a `@Transient` field serialized into the `extendedContent` JSON by `ExtendableEntityListener`) over a real column. No Flyway migration needed, and it travels with backups. Use a real column only when the app must query, filter, sort, index or join on it in SQL (relations, `name`, `enabled`, `ordinal`, `published`, counts...). Changes to extended attributes are persisted only via the service's `save` (the fields are invisible to Hibernate dirty checking).
- **Extension data:** `ExtendableEntity.attributes` is a dynamic `JsonObject` that is itself part of the extended attributes (stored as `attributes_*` keys in `extendedContent`). It differs from `@ExtendedAttribute` fields in being dynamic: extensions can't add fields to existing classes, so they keep their own data under their own key (`getAttributes().add("myplugin", ...)`) with no schema change, and it travels with backups/exports.
- **Generation pipeline** (`domain.service.impl.generation`): `generateNextTurn` runs a fixed sequence of steps (prepare → constants → lorebook → content → payload → new message → inference → cleanup) on a worker thread, emitting `Events` that extensions can listen to/modify. Worker threads have no HTTP request: request attributes are copied via `ThreadCopyRequestAttributes` so the session-scoped `user` bean resolves. UI updates from other threads must go through `ui.access(...)` (+ `UIPushGuard`).
- **Extensions:** OSGi (Felix) bundles loaded from `~/.marginalia/extensions` at runtime. UI classes marked `@Extendable` are instrumented by a ByteBuddy agent (`instruct` package, installed at startup) so plugins can register decorators before/after any method via `ExtensionService.registerDecorator`.
- **Localization:** UI strings are keys in the `L` enum (`loc/L.java`) with English text in `LocalizationEN.loadMessages()`. Both files are grouped by key prefix (`LABEL_`, `ENUM_`, `ERROR_`, `MSG_`, `HELP_`, `DESC_`) under `// --- PREFIX ---` headers. Keys name the English text, not the screen, so reuse an existing key (e.g. `LABEL_CLOSE`) instead of adding a per-screen duplicate (before adding a key, grep `LocalizationEN` for the same English text). Add new keys at the end of their prefix group in **both** files — never at the end of the file.
- **Prompts** are Handlebars templates; SillyTavern macros are translated by `MacroTranslator` (`domain.templates`).

## Testing notes

Tests (`src/test/java`) boot the real Spring XML config (minus OSGi/instrumentation) on SQLite in `target/test-home/ctx-*`, with `MarginaliaTestBase` (`loginAs`, `createUser`, `createAI` helpers) and `MockLLMServer`, a fake OpenAI-compatible server programmed per test. Generation tests extend `GenerationTestBase`; entity CRUD uses `OwnedCrudContract`/`ExtendableCrudContract`. No network or API key needed.

## Other

- When you implement a feature or fix, update the docs that describe it: user docs in `manual/user`, developer docs in `manual/developer`. Developer pages carry `verified` (commit last checked) and `covers` (code described) in their front matter; after a code change run `python3 tools/check_dev_docs.py` (`-v` names modified files) and update the pages it flags. Once a page matches the code, stamp it with `--stamp <page.md>` in a commit of its own (the stamp is a commit that already exists).
- `manual/` is the source of the published manual (Retype; `retype.yml`): `manual/user`, `manual/developer`, screenshots in `manual/images`. `docs/` is its generated output — never edit or read it; after changing anything in `manual/`, run `retype build` in the root folder.
- `TODO.md`, `TODO.BUGS.md` track planned work.
- Logging: `src/main/resources/log4j.properties` (DEBUG, dev) is replaced by `resources-release/log4j.properties` (INFO) in the packaged WAR so no story content is logged.
