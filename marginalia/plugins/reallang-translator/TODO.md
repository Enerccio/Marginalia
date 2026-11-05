# Real Language Translator - TODO

The plan, the decisions and why it is a plugin of its own (modularity: an installation that does not need it does not load it) are in the *Real languages* section of
`../conlang-translator/TODO.md`. This file tracks the work of this plugin.

## Done
1. **Library** (`marginalia/libs/translator-common`): the pure code of the Conlang Translator that does not depend on a lexicon moved there, 59 tests: `Span`, `Speakers`, `CharacterNote`, `ExportMode`,
   `ReaderMode`, `Reverse`, `TranslationPlan`, `Speech`, `SelectionRange`, `SpeechTransformer`, `ExportText`, `ReaderText`, `SpeechPassageKind`, `RangeLoop`, `RangeRuns`, `PartRuns`, `AsyncRuns`,
   `ModelClient`, `PassageAccess`, `PluginLog`, `SourcePopover`, `ReaderView`, `SpeakersEditor`, `ListEditor`, `EditDialog`, `ExportChoice`, with their own CSS and texts
   (`TranslatorCommonLocalization`, `CommonStyles`). The passage kind id, the mark class and the names of the requests are parameters. Embedded in each bundle (`Embed-Dependency ...;inline=true`).
2. **Skeleton**: bundle, activator, extension (passage kind, transformer, CSS), localization class, `ReallangIcon`, `icon.svg`, README, this file, the module in the root POM; the build scripts,
   the plugins `Dockerfile` and the workflows build the library first.

## Next
3. The export of a book that holds both kinds of passages works (the core `ExportTextRegistry`, done); register a `SpeechExportContributor` and the export field here. **Still open:** the viewer's marks for a mixed book (`ReaderView.apply` rewrites the content of a part, the last plugin wins).
4. The **language** (rules section with presets, the script, honorifics), kept in the user settings under its own key, a panel in the Settings screen, inherited settings (language, model, auto).
5. The **prompt** and the checks (quote, script), the translation of a part, a range and a selection, the menus.
6. Several languages in one book, a language per character, relationship fields on the speakers' notes.
7. The second pass, the back-translation check, the reading line in the popover.
8. Commands (`reallang-*`), the agent run copy, the manual page.
