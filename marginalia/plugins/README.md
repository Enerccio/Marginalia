# Marginalia plugins

Optional extensions for Marginalia. Each plugin is an OSGi bundle (a JAR) that Marginalia loads at runtime, without
a restart.

| Plugin | What it adds |
|---|---|
| [Chapter Marker](chaptermarker/README.md) | Story parts starting a chapter (a Markdown `#` heading) are shown as chapters in the story sidebar. |
| [Lorebook VCS](lorebookvcs/README.md) | Revision history for lorebook entries: snapshots, browsing and restoring old versions, export/import. |
| [Reviewer](reviewer/README.md) | AI-written reviews of a story part, with configurable reviewer profiles. |
| [Side Query](sidequery/README.md) | A chat panel next to the story for asking the model questions about the book, its lore and recent parts. |

## Installing

1. Get the plugin JAR (`<plugin>/target/<plugin>-<version>.jar`, see *Building*).
2. In Marginalia, as an administrator, open *Admin → Extensions* and use *Load Extension (.jar)*.

The JAR is stored in `~/.marginalia/extensions` and loaded on every start. JARs can also be copied into that folder
directly - they are picked up on the next start. Uninstall a plugin from the same page.

Plugins are built for one Marginalia version: they extend parts of the user interface, so a plugin built for a
different version may stop working. Use plugins built from the same source tree as the application.

## Building

Plugins compile against the Marginalia application, which has to be installed in the local Maven repository first:

```sh
cd marginalia
mvn install -DskipTests          # installs the application (and its classes JAR used by plugins)

cd plugins/chaptermarker          # or lorebookvcs, reviewer, sidequery
mvn package                       # target/chaptermarker-1.0.0.jar
```

Requirements are the same as for the application: JDK 25+ and Maven 3.9+.

## Writing a plugin

A plugin is a Maven project with `bundle` packaging (`maven-bundle-plugin`) that depends on
`io.github.enerccio:marginalia` (classifier `classes`, scope `provided`). Its bundle activator registers a
`MarginaliaExtension` service; Marginalia calls `onExtensionLoad` / `onExtensionUnload` with the `ExtensionService`.

Through `ExtensionService.registerDecorator(decorator, className, methodName)` a plugin runs code before and after
any method of a UI class annotated `@Extendable`, with access to the method's arguments and local variables -
that's how these plugins add menu items, panels and settings. Plugin data is usually kept in the `attributes` of
existing entities (book, story part, lorebook, user settings), so it's included in backups and exports.

The four plugins here are complete examples, [Chapter Marker](chaptermarker) is the smallest one.
