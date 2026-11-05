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
7. **Several languages in one book (2026-11-05):** the book names a language first (the inherited setting) and has a list of other languages (`BookSettings.otherLanguages`, each a `BookLanguage` with its own
   speakers); one pass per language (`TranslationService.translate(book, part, languageId)`), the speeches of the other languages carried through the write (`TranslationWriter.carried`), the
   languages whose speakers are not in the part skipped (`Speakers.mayBeInvolved`), a language choice in the menus (`LanguageChoiceDialog`), the range window and the commands (`language`,
   `reallang-add-book-language`, `-remove-book-language`), a *Speakers* tab for each language and the *Languages* tab. Not run on the real model yet.
8. 57 unit tests (script check, prompt, answer, checker, store, presets, book settings, commands, texts).

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

- **Language cues in the narration (idea, from the dataset build, 2026-11-05).** The English source has to say which language a speech is in ("she said in Japanese") because that is what the
  classifier reads; after the translation the cue stays in the narration next to the translated speech, which reads redundant or odd (`"..." she said in Japanese`, with the speech in Japanese).
  Options: (1) the source names the language only when it changes (done in the dataset's *Style* entry; the classifier then has to carry the language of a conversation from `THE STORY BEFORE` and
  the speakers, to check on the real model: lines with no cue); (2) the translation removes the cue phrase it was told about (the model returns the span of the cue, the writer cuts it from the
  narration; the English source keeps it); (3) the author edits it out by hand. **Decision so far (author, 2026-11-05): (1) is enough for now; whether the plugin should cut the cue itself ((2)) is left for later,
  after the translation pass has been seen on real parts.** (2) needs the cue in the answer and a rule for what may be cut.

## Next
- **A glossary of names and special words (idea, from the dataset build, 2026-11-05).** The language has no place for how a name of the story is written in it, so the model chooses again in every part:
  in run 4 of the dataset harness a foreign name was echoed once as `サエレン。` and once left in Latin letters, and `Tomáš` can become `トマーシュ`, `トマシュ` or stay as it is. A section next to the
  rules (politeness, names, other) that the book fills in, with entries of its own: **(1)** *name -> written form* (`Saellen` -> `サエレン`, `Silvakhor` -> `シルヴァコール`; for a language with other scripts
  also the reading or romanization, the family-name order, the honorific the character gets); **(2)** *special readings*: a **gikun / ateji** list (a word written with kanji that are read in another
  way, `魔女` read `ウィッチ`, `異世界` read `イセカイ`), with the reading for the furigana line that the plugin has as the *reading* field; **(3)** *words left as they are* (a title, a place, a
  made-up term of the story that is not translated). Used like the conlang lexicon is: the entries that matter for the part (the names it has) go into the system message, so a server that caches the prompt
  reads it once; the answer checker flags a name written another way than the glossary says (as `wrongScript` is flagged); a name the model had to choose is **proposed** as a new entry to accept
  (like the `newWords` of the Conlang Translator), so the choice is made once and then kept. **Where it lives (decided with the author, 2026-11-05): at every level, combined.** The glossary exists **per language** (the library of the user: terms of the language itself, the
  honorific habits, common words), **per folder** (a folder that holds a series of books defines the names and terms the books share; folders nest, so the chain is walked: top level, the outer folder, the
  inner folder) and **per book** (the story's own, the one most used). The effective glossary is the **union** of the levels, the nearest level wins for the same source word (a book can change how a
  folder writes a name, and can switch an entry off). Not the usual inherited setting: `resolveExtension` returns the one value of the nearest level, so the plugin reads the **own** value of each level
  (the language, each `Folder` up the `getParent()` chain, the book) and merges them itself; the dialogs of the book and the folder show the entries of their own level and, greyed, the ones that come from above
  with the level they come from, as the inherited fields do. A name the model had to choose is proposed for the **book** level (the author moves it up to the folder or the language when it is common).
  An alternative or complement to look at: a **lorebook entry** the plugin reads (by a tag), so one place holds the names for the writer's model and for the translator. The same is useful for Czech and any language with its own
  spelling of foreign names (Tomáš written `Tomas` by the model in a mutter, `Saellen` declined in Czech).
- **Relationship fields on the notes of the characters** (age, rank, closeness) instead of free text in the voice.
- **Several languages: try it on the real model** (a part with Japanese and Korean speakers: two requests, the second leaves the first's speeches alone; the overlap drops when both languages claim a
  speaker). A character who speaks two languages in one scene is left to the text of the part. The menus ask with a dialog (a choice of languages), not an entry for each language.
- **A second pass** as an action on a part or a range (not a stage of every translation: it doubles the requests of a slow model): the model rewrites each speech for naturalness, given the voice and
  the relationship; optionally a back-translation check that flags drift.
- **Furigana** (ruby) in HTML and EPUB; the reading is a line in the popover only.
- **A run copy of the language for agent runs** (the Conlang Translator has one for a language with a lexicon). A language here is a few lines of rules, so an agent run changes the library directly now.
- **A translator that reads the other plugin's translation as English:** the model of this plugin sees the other plugin's speech in the other language (only its own passages are reversed).
- A real-model check of the script check on romaji with macrons, on mixed scripts and on names.
