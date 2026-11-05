# Real Language Translator - TODO

The plan, the decisions and why it is a plugin of its own (modularity: an installation that does not need it does not load it) are in the *Real languages* section of
`../conlang-translator/TODO.md`. This file tracks the work of this plugin.

## Done
1. **Library** (`marginalia/libs/translator-common`): the pure code of the Conlang Translator that does not depend on a lexicon moved there: `Span`, `Speakers`, `CharacterNote`, `ExportMode`,
   `ReaderMode`, `Reverse`, `TranslationPlan`, `Speech`, `SelectionRange`, `SpeechTransformer`, `ExportText`, `ReaderText`, `SpeechPassageKind`, `RangeLoop`, `RangeRuns`, `PartRuns`, `AsyncRuns`,
   `ModelClient`, `PassageAccess`, `PluginLog`, `SourcePopover`, `ReaderView`, `SpeakersEditor`, `ListEditor`, `EditDialog`, `ExportChoice`, with their own CSS and texts; for this plugin also
   `TranslationWriter` (the write, the selection write and the revert, moved out of the Conlang Translator's service), `RangeDialog`, `TranslatedPassages`, `ModelJson`, `Span.reading`.
2. **Skeleton**: bundle, activator, extension, localization, icon, README, this file, the module in the root POM; the build scripts, the plugins `Dockerfile` and the workflows build the library first.
3. **Export and viewer for a book with both plugins:** `SpeechExportContributor` and the export field here; the viewer draws a part once from the passages of every plugin (`ReaderView`,
   `TranslatedPassages`), so the marks of both are clickable. The reader mode of the plugin that decorates first rules the page (set the same in both).
4. **The language** (`RealLanguage`: script, honorifics, reading, politeness, names, other rules; `Presets` for Japanese in kana and kanji / kana / romaji), kept in the user settings (`RealStore`), the
   panel in the Settings screen (`LanguageSettingsForm`, `LanguageEditor`), the inherited fields (`RealInherited`: language, auto, model, protocol, reader).
5. **The prompt and the checks** (`TranslationPrompt`, `TranslationAnswer`, `AnswerChecker`, `ScriptCheck`, `QuickTranslation`): quote, language, overlap, script; the translation of a part, a
   selection and a text (`TranslationService`), the menus, the automatic run, the range window, the Real language tab (speakers, quick translate).
6. **Commands** `reallang-*` (`ReallangCommands`), a test that every operation of a service has one; the manual page.
7. 52 unit tests (script check, prompt, answer, checker, store, presets, commands, texts).

## Real-model trial 1 (2026-11-05)

The request of `TranslationPrompt` (built by the plugin's own classes, the answer read by `TranslationAnswer` and checked by `AnswerChecker`) on the local model of the dev database (`gemma-4-31b-it-MM`, a thinking model),
structured output on, `max_tokens` 32000, one part of six speeches (a lieutenant, her captain, her little brother, an American reporter who speaks English), notes on the four characters in the request. Samples: **2 of 2 in kana and
kanji, 1 of 1 in romaji, 1 of 1 in kana only** (completion 2755 to 4202 tokens, 142 to 215 s each, prompt about 1060 tokens, the thinking about 7000 to 11000 characters).

- **Form:** 4 of 4 answers were read; every quote was found in the part; no speech was dropped; no answer was in the wrong script (`wrongScript` 0 in all four).
- **Classification:** 4 of 4: the five Japanese speeches were translated and the reporter's English line was left as `none`.
- **Level of speech:** 4 of 4 followed the notes: the lieutenant polite (desu/masu) to her captain, plain and warm to her brother (`ね`, `から`, `よ`), the captain plain and blunt, the boy childlike (`お姉ちゃん`, `もん` / `じゃん`).
  The register was named (`polite` / `plain`) every time.
- **Reading:** given in all three scripts it was asked for (kana and kanji, kana only), empty in romaji as the prompt says; in romaji the macrons were used (`Arigatō`).
- **Seen, not a bug of the plugin, the model's:** the captain became `大佐` (colonel in the navy) in one sample of two, `taichō` in romaji; the brother's name was `ユキ` in one script and `ゆうき` in another (the name rule of the preset
  does not say to keep one form); the kana-only translation put spaces between words (readable, not how a kana-only text is written), and `やくそくな` for a sister to her brother is odd. The rules text could say "ranks as the
  period and the service of the story have them" and "a name is written in one form for the whole book" - not changed before more samples show it matters.
- **Not seen:** a wrong-script answer (to see the report, the check is covered by unit tests only), a speech the model puts in `none` by mistake, a part with no speech, a long part.

## Not tried
- **The screens and the app:** nothing of the plugin was run in the real app: the settings panel, the editor, the tab, the range window, the viewer and the export of a real book, a phone.

## Next
- **Relationship fields on the notes of the characters** (age, rank, closeness) instead of free text in the voice, and a **language per character** (the book's language is the default), so that one
  book can have Japanese and Korean speakers; the request lists the languages of the speakers of the part and the model says which language each speech is in (`Span.language` is there already).
  One pass per language, the menus offer the languages (see *Several languages in one book* in the Conlang Translator's TODO).
- **A second pass** as an action on a part or a range (not a stage of every translation: it doubles the requests of a slow model): the model rewrites each speech for naturalness, given the voice and
  the relationship; optionally a back-translation check that flags drift.
- **Furigana** (ruby) in HTML and EPUB; the reading is a line in the popover only.
- **A run copy of the language for agent runs** (the Conlang Translator has one for a language with a lexicon). A language here is a few lines of rules, so an agent run changes the library directly now.
- **A translator that reads the other plugin's translation as English:** the model of this plugin sees the other plugin's speech in the other language (only its own passages are reversed).
- A real-model check of the script check on romaji with macrons, on mixed scripts and on names.
