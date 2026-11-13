---
label: Libraries
icon: package
order: 58
covers:
  - marginalia/libs
  - pom.xml
  - install-plugins.sh
  - marginalia/plugins/Dockerfile
  - .github/workflows/server.yml
  - .github/workflows/desktop.yml
verified: 8a278a19f
---

# Libraries for plugins

Two plugins that do part of the same work should not copy each other. A **library** is the code they share, in `marginalia/libs/<name>`: a plain Maven jar (not a bundle)
that each plugin **embeds** in its own bundle.

| Library | What it is | For |
|---|---|---|
| [`http-hooks`](http-hooks.md) | The HTTP call with a size cap and no redirects, the guard for private addresses, the request that frees or takes the GPU (headers stored protected) and the gate that lets several users share one GPU. The JDK only. | The Image Generator, the Audiobook, and any plugin that calls a server of the user. |
| [`translator-common`](translator-common.md) | The data of a translated speech, the reverse to the text a model reads, the text of the export and of the reader, the run loops, the speakers and the screens built on them. | The Conlang Translator, the Real Language Translator, and anybody who writes a plugin that keeps a story in one language and lets the models read another. |

## Why embedded, and what that means

Plugins are OSGi bundles and **do not see each other's classes** ([Class loading](../overview.md#class-loading)): a bundle that imported the packages of another
would be wired to one revision of it, and after the other bundle was unloaded and installed again it would keep the old classes (and the JVM remembers a class
reference that failed to resolve). A library is therefore not an extension and is not wired to anything. It is **compiled into every plugin that uses it**
(`Embed-Dependency` with `inline=true`: the classes land in the root of the bundle JAR, next to the plugin's own), so each bundle has **its own copy**, loaded by its
own class loader. The consequences are the rules of a library:

- **Shared source, not shared runtime.** `static` fields exist once per plugin. A library holds no state that two plugins would have to share, and no registry both
  would have to see; what the plugins have to agree on goes through the application (the core registries, attributes, the services), as for any two plugins
  ([Sharing data between extensions](../ipc.md)).
- **What has to differ is a parameter, not a constant.** A library that registers a passage kind takes its id; one that puts a CSS class on a mark takes the class;
  the texts a plugin shows (an export field's labels) are given by the plugin. Two plugins that both use the library then never collide in the application.
- **Objects do not cross.** The `Span` of one plugin is not an instance of the `Span` of the other (different class loaders, same name). They exchange data (the JSON
  in the attributes of a passage), never objects.
- **The library may use everything the application has** (`provided`, as a plugin does, see [the `pom.xml` of a plugin](../overview.md#pomxml)) and must be woven by
  AspectJ if it has `@Configurable` classes (its POM has the same `aspectj-maven-plugin` as a plugin's; the plugin only embeds the woven classes).
- **A library is never loaded as an extension.** It is kept out of `marginalia/plugins/` on purpose: the install script, the plugin `Dockerfile` and the workflows
  collect `plugins/*/target/*.jar` into the extensions folder.

## Using a library in a plugin

1. The library is a module of the root `pom.xml` (`<module>marginalia/libs/translator-common</module>`, before the plugins that use it), and a **dependency** of the
   plugin, with the version of the project:

   ```xml
   <dependency>
       <groupId>com.github.enerccio.marginalia.extensions</groupId>
       <artifactId>translator-common</artifactId>
       <version>${project.version}</version>
   </dependency>
   ```

2. The bundle plugin embeds it (everything else of the plugin is `provided`, so only the library is embedded):

   ```xml
   <Embed-Dependency>translator-common;inline=true</Embed-Dependency>
   ```

   Keep the negated `Import-Package` entry of every plugin (`!com.github.enerccio.marginalia.extensions.<yourplugin>.*`): the library's packages are private to the
   bundle, they are not exported.
3. Build the library **before** the plugin: `mvn install -DskipTests` in `marginalia/libs/translator-common` puts it in the local Maven repository, where a plugin built on
   its own (`mvn package` in its folder) finds it. `mvn package` / `mvn install` in the repository root builds in the right order by itself.
   `install-plugins.sh`, `marginalia/plugins/Dockerfile` and the workflows `server.yml` and `desktop.yml` have a step for it: a library you add needs no change in them
   when it lives in `marginalia/libs/`.
4. The CSS and the texts of a library's screens are in the library (`translatorcommon.css`, `TranslatorCommonLocalization`). The plugin's `ExtensionHasCss` returns the
   library's CSS together with its own (`CommonStyles.css() + "\n" + MyStyles.css()`); the same rules from two plugins are harmless.

## Writing a library

Start from the POM of `translator-common` (a plain `jar`, the `provided` dependencies of a plugin, the aspect plugin). Put what is **pure** in it first (data, text
functions, loops) and test it without the application; add the screens that are the same in both plugins last. Document each package in a `package-info.java` and
each public class with what it is for, what it needs from the plugin and what it must not hold: the next plugin author reads that, not your plugin.
