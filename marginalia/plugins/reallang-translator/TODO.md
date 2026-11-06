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
8. **Glossary of names and special words (2026-11-06):** `Glossary` / `GlossaryEntry` (the word, written form or *keep*, reading, note, on/off, proposed), three levels merged (language in the library, folders in their attributes, book in `BookSettings`; the folder's and the book's are kept per language of the book), the nearest level wins and an entry that is off at a nearer level switches the word off; `GlossaryService` (read, write, move up, accept, propose) and `BookReal` for the screens and commands; the request has a `GLOSSARY` block (whole up to 100 entries, else the entries the part has) and asks for `names` (the written forms the model had to choose), `AnswerChecker` flags a speech that writes a name another way (`Problem.GLOSSARY`), the names are added to the book as proposals; the *Glossary* tab for each language of the book (`GlossaryPanel`: add, edit, delete, accept, switch off/on for the book, move up to a folder or the language), the glossary list in the language editor, commands `reallang-glossary*`.
9. 78 unit tests (script check, prompt, answer, checker, glossary, store, presets, book settings, commands, texts).

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

## Real-model trial 2: in the real app, with the glossary (2026-11-06)

The plugin was installed in a copy of the screenshot environment (the *Unregistered* book) whose AI points at the local model of the dev database (`gemma-4-31b-it-MM`, a thinking model, `maxCompletionTokens` 32000), the Prompt History plugin installed to see the requests, driven by Playwright (the scripts are in the session's scratchpad, not kept). Japanese (kana and kanji, reading on) and Czech in one book, speakers Mizuki and Tomáš for Japanese, Tomáš for Czech (only when he mutters).
- **A part with both languages, "All languages" (the part menu opens the *Which language?* dialog):** Japanese pass 251 s and 244 s in the two runs, Czech pass 63 s and about 60 s (completion 2755 to 4202 tokens in trial 1; not read from the history this time). 11 speeches of the *Go Home* chapter in Japanese: the polite *desu/masu* for Tomáš, plain for Mizuki (*分かってたんだ*, *何を？*). The ferry part: 6 Japanese speeches and one Czech (*Tomáši Hájku, ty idiote, slíbil jsi jí krátkou procházku.*: the model declined the name in the vocative, as Czech does); the Japanese pass gave the muttered Czech line `none`, the Czech pass translated it. Both runs, one sample each.
- **The glossary, 2 runs of the same part:** *Tomáš* kept and *Mizuki* -> *美月* (book and language level) were in the system message and used (*美月さん*, *Tomáš* kept); the model **reported two names it had to choose** (*Saellen* -> *サエレン*, *Silvakhor* -> *シルヴァコール*), the plugin added them to the book as proposals ("2 name(s) were added to the glossary of the book to look at"); in the second run both were written exactly as proposed (2 of 2). One more proposal in the second run was **for Czech: *Tomáš Hájek* -> *Tomáši Hájku*, the inflected form**: the prompt now says to report the base (dictionary) form (**not tried again**).
- **Bugs found and fixed:** the combined outcome of a book with several languages dropped the glossary counts (no "names proposed" toast); the *Which language?* dialog showed no radio selected (the *All languages* item was `null`), the choice worked (all languages).
- **The screens:** the *Real language* tab with the speakers of each language, the *Glossary* tab of each language (add, edit dialog, move to the language, switch off and on for the book), the right-click menu of a selection, the phone layout of the story with the marks and of the tab (the glossary grid scrolls sideways), the commands `reallang-glossary`, `-set`, `-switch` from the command bar (it needs all the arguments of the form, `""` for an empty one, like the other commands).
- **Not tried:** the language editor's glossary list in *Settings*, a folder level entry through the *Move to* menu (the book was not in a folder), the commands `-remove`, `-accept`, `-move`, the export and the viewer of a real book, *Translate parts*.

## Not tried
- **Level of understanding (2026-11-06, shared with the Conlang Translator, see its TODO, *The POV character does not understand*):** built in `translator-common` and registered here (facts for the annotation, the message inserted into the prompt, the `reallang-understanding` macro, the column in the *Speakers* tab); unit tests only, not tried in the real app or on the real model.
- **The screens and the app:** the tab, the glossary, the part menu, the selection and the commands were run in the real app (trial 2); the settings panel and the language editor, the range window, the viewer and the export of a real book were not.

- **Language cues in the narration (idea, from the dataset build, 2026-11-05).** The English source has to say which language a speech is in ("she said in Japanese") because that is what the
  classifier reads; after the translation the cue stays in the narration next to the translated speech, which reads redundant or odd (`"..." she said in Japanese`, with the speech in Japanese).
  Options: (1) the source names the language only when it changes (done in the dataset's *Style* entry; the classifier then has to carry the language of a conversation from `THE STORY BEFORE` and
  the speakers, to check on the real model: lines with no cue); (2) the translation removes the cue phrase it was told about (the model returns the span of the cue, the writer cuts it from the
  narration; the English source keeps it); (3) the author edits it out by hand. **Decision so far (author, 2026-11-05): (1) is enough for now; whether the plugin should cut the cue itself ((2)) is left for later,
  after the translation pass has been seen on real parts.** (2) needs the cue in the answer and a rule for what may be cut.

## Next
- **Glossary follow-ups.** (1) The folder level has no dialog of its own: the core's folder dialog has no hook for a plugin's panel, so a folder's entries are made by moving an entry of a book up (the *Move to* menu of the *Glossary* tab) or by `reallang-glossary-set level=folder`. (2) The *Conlang Translator* has a lexicon, not a glossary; the same *names* idea (a name of the story that is not a word of the language) is not there. (3) The relevance of a big glossary is a whole-word match of the English; an inflected English form (`witches`) is not found. (4) A lorebook entry as a source of the names (the alternative noted in the idea) was not looked at. (5) A gikun entry (`witch` -> `魔女` read `ウィッチ`) is only as good as the model's use of the reading; not tried on the real model.
- **Relationship fields on the notes of the characters** (age, rank, closeness) instead of free text in the voice.
- **Several languages: try it on the real model** (a part with Japanese and Korean speakers: two requests, the second leaves the first's speeches alone; the overlap drops when both languages claim a
  speaker). A character who speaks two languages in one scene is left to the text of the part. The menus ask with a dialog (a choice of languages), not an entry for each language.
- **A second pass** as an action on a part or a range (not a stage of every translation: it doubles the requests of a slow model): the model rewrites each speech for naturalness, given the voice and
  the relationship; optionally a back-translation check that flags drift.
- **Furigana** (ruby) in HTML and EPUB; the reading is a line in the popover only.
- **A run copy of the language for agent runs** (the Conlang Translator has one for a language with a lexicon). A language here is a few lines of rules, so an agent run changes the library directly now.
- **A translator that reads the other plugin's translation as English:** the model of this plugin sees the other plugin's speech in the other language (only its own passages are reversed).
- A real-model check of the script check on romaji with macrons, on mixed scripts and on names.
