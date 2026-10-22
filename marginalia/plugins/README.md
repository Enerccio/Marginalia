# Marginalia plugins

Optional extensions for Marginalia. Each plugin is an OSGi bundle (a JAR) that Marginalia loads at runtime, without
a restart.

| Plugin | What it adds |
|---|---|
| [Arc Outline](arc-outline/README.md) | The arcs of the book and their chapters, each planned, current or finished, in the story sidebar; the model can write an arc from the lore. |
| [Author's Note](authorsnote/README.md) | A per-book note inserted into the prompt of every generation, at a chosen depth and role. |
| [Chapter Marker](chaptermarker/README.md) | Story parts starting a chapter (a Markdown `#` heading) are shown as chapters in the story sidebar. |
| [Character Tracker](character-tracker/README.md) | The characters of the book with their relationships, and a map of the relationships, in the story sidebar. |
| [Gemma 4 tools fix](gemma4toolsfix/README.md) | Sends the results of function calls to Gemma 4 models with the role `tool_responses`. |
| [Google Gemini](google-genai/README.md) | Google's Gemini models as an inference provider (type *Extension*), over the Google Gen AI Java SDK: thinking summaries as reasoning, safety filters, exact usage. |
| [Instruction Exporter](instructionexporter/README.md) | "Export instructions" in the story settings menu: copies the instructions, scene settings and characters of the active branch to the clipboard. |
| [Lore Generator](lore-generator/README.md) | Writes the content of a lorebook entry with the model from the existing lore (generation, compaction, attachments), from a button in the lorebook editor. |
| [Lorebook VCS](lorebookvcs/README.md) | Revision history for lorebook entries: snapshots, browsing and restoring old versions, export/import. |
| [Name generator](name-generator/README.md) | Functions for the model to list name categories (about 1270: names of the world, places, deities, drugs...) and generate names from Markov chains; used by the Lore Generator and Arc Outline. |
| [Name provider](name-provider/README.md) | Functions for the model to list lists of real names (forenames and surnames of about 100 countries) and pick names from them, each as a pair of the romanized and the original name; used by the Lore Generator and Arc Outline. |
| [Place provider](place-provider/README.md) | Functions and commands to look up real places (Natural Earth, about 18 000 notable ones): a search by name, type and country, the places around a point and the distance of two places. |
| [Reviewer](reviewer/README.md) | AI-written reviews of a story part, with configurable reviewer profiles. |
| [Side Query](sidequery/README.md) | A chat panel next to the story for asking the model questions about the book, its lore and recent parts. |
| [Chinese localization](chineselocalization/README.md) | Simplified Chinese as a language of the user interface; the example for adding a language. |

## Installing

1. Get the plugin JAR (`<plugin>/target/<plugin>-<version>.jar`, see *Building*).
2. In Marginalia, as an administrator, open *Admin → Extensions* and use *Load Extension (.jar)*.

The JAR is stored in `~/.marginalia/extensions` and loaded on every start. JARs can also be copied into that folder
directly - they are picked up on the next start. Load a new build the same way to update a plugin, uninstall it from
the same page.

Plugins are built for one Marginalia version: they extend parts of the user interface, so a plugin built for a
different version may stop working. Use plugins built from the same source tree as the application.

## Building

Plugins compile against the Marginalia application, which has to be installed in the local Maven repository first:

```sh
mvn -N install                   # in the repository root: installs the parent POM (holds the version)

cd marginalia
mvn install -DskipTests          # installs the application (and its classes JAR used by plugins)

cd plugins/chaptermarker          # or arc-outline, authorsnote, character-tracker, gemma4toolsfix, google-genai, instructionexporter, lore-generator, lorebookvcs, name-generator, name-provider, place-provider, reviewer, sidequery, chineselocalization
mvn package                       # target/chaptermarker-1.4.0.jar
```

`mvn package` in the repository root builds the application and all plugins in one go. Requirements are the same as for
the application: JDK 25+ and Maven 3.9+.

### Building in Docker

Without a local JDK and Maven, `docker-compose.yml` in this folder builds the application and all plugins in a
container and copies the JARs into `build/` (ignored by Git):

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

Run both from the repository root. `rebuild.sh` / `rebuild.bat` in the root use this to update a Docker server and
copy the JARs into `data/extensions`.

## Writing a plugin

A plugin is a Maven project with `bundle` packaging (`maven-bundle-plugin`) that depends on
`io.github.enerccio:marginalia` (classifier `classes`, scope `provided`). Its bundle activator registers a
`MarginaliaExtension` service; Marginalia calls `onExtensionLoad` / `onExtensionUnload` with the `ExtensionService`.

Through `ExtensionService.registerDecorator(decorator, className, methodName)` a plugin runs code before and after
any method of a UI class annotated `@Extendable`, with access to the method's arguments and local variables -
that's how these plugins add menu items, panels and settings. Through
`StoryGenerationService.addEventListener(event, listener)` a plugin takes part in generation - it can change the
lore, the prompt and the answer; [Author's Note](authorsnote) inserts its note into the payload this way. Plugin data is usually kept in the `attributes` of
existing entities (book, story part, lorebook, user settings), so it's included in backups and exports.

The plugins here are complete examples, [Chapter Marker](chaptermarker) is the smallest one.
