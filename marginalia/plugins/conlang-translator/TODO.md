# Conlang Translator - TODO

Status: **stub** (the bundle loads and logs, nothing is hooked). Everything below is a proposal to discuss unless a line says
**Decided**.

## Decisions so far

- **The story holds the conlang, the English source is plugin data, models read the source** (see below).
- **Storage: option C**, a core change: the annotation machinery gets a base class for anchored passages that are not editorial
  notes; the conlang passages are of such a kind (see *The core annotations*, *The class design*).
- **Who speaks: everyone except the listed** (a mode **per book**, this is the default); the text can override per speech. **Decided 2026-11-04: the speakers and the character notes are kept in the book (`BookSettings.speakers`), not in the language** (the same language is spoken by other people in another story); a language, its export/import and its comparison carry none, and the old `speakers` of a stored language are ignored.
- **The story side is the *Conlang* tab of the tab dock** (`ConlangBookView`, added to `ManuscriptStoryPart`/`PhoneManuscriptStoryPart` `renderStoryContent`): *Words* (the lexicon of the book's language, `LexiconTab`, stored in the library on every change by `BookConlang.saveWords`, which keeps the words a run added meanwhile), *Speakers* (`SpeakersEditor`, stored in the book by `BookConlang.saveSpeakers`) and *Quick translate* (`QuickTranslatePanel`, `QuickTranslation` prompt, nothing written; the words it needed are imported as *new* by `BookConlang.importWords`). It replaces the *Look up a word* tool. **Not tried in the real app / on a phone yet; no screenshot.**
- **Translate the selection** (item of the right click menu of the text, `StoryMenuItem.text`; core `StoryCard.withSelection` gives the range): `TranslationService.translateSelection` trims the range (`SelectionRange`), refuses one that overlaps a passage or a part that changed, asks the model with `QuickTranslation` (the story around the sentence and the point of view / present characters as context), replaces the sentence in the stored text with `replaceText` (the model text is unchanged), creates the passage with the English and adds the reported words as *new*. The speaker of such a passage is empty. **Not tried in the real app.**
- **One request** per part that classifies and translates, for now. It is tested on a real model, as the Knowledge Tracker's was,
  with the author's own **test conlang** (to be given), and the results written here; two requests only if that goes badly.
- **A language links an example book and an example lorebook** that the designer reads (set once in the editor).
- **The language editor, the library and the designer live in the settings** (the plugin's panel in *Settings*, a large editor dialog); the book and folder dialogs only **choose** the language through the inheritance chain; the story side has only a thin lookup and *Translate this* (see *Where the pieces live*).
- **Languages are settings**: the language objects (rules + lexicon) live in the settings registry; which language a book uses
  is **selected through the inheritance chain** (book > folders > user), like the other inherited settings. A language can be
  **duplicated** (renamed) into an independent one; a language that is *shared* (inherited, not cloned) is one object, so a book that
  adds words to it or changes it changes it for every book that uses it, which is **a feature** (a series in one world).
- **Words have a category** (a semantic group, one per entry, author-ordered, with an *always send* flag): for organization, for the grouped payload and for sending part of a big lexicon.
- **Rules are a list of named sections** (title, text, whether it goes to the model); the lexicon has **entries with senses** (see *The test conlang: Vrakht*).
- **Rules and lexicon are separate; the model invents words and reports them with a rationale, the author edits them;** a changed
  word is a search and replace (core `ManuscriptReplaceService`).
- **The conlang is the original**; the English is the *source*; no "restore the originals", en masse commands only while the plugin
  is installed; searching English words in the conlang text is not needed.
- **Translate part / retranslate / Translate this** work on the source (see *Translating by hand*).
- **Agent runs (last step, after the commands) work on a duplicate of the language, locked, and the user reviews the difference** (lazy copy on the first write, one `LanguageDiff`/change set shared with the designer, import and duplicate-compare); this needs **new extension hooks in the core** (copy created, command scope with the run, checkpoint, resume sync, merge plan sections, merge apply, run ended, cleanup); see *Agent runs*.
- **Everything is also a command for agents, the designer included, but in the last steps** (nothing is UI-only in the end; one service the UI and the commands share; `ask true` proposals applied with `conlang-apply-changes`; see *Commands for agents*).
- **The designer works in the editor at any time too** (generate words for a category, add a rules section such as a numbering system, review), as proposals the author accepts.
- **The plugin has a conlang designer**: a section where the author designs a language with the model (see *The conlang designer*).
  **Shape: fixed stages as the frame, with a chat inside each stage** to adjust the proposal before it is accepted.
  **Phonology is optionally structured** (a sound inventory and syllable template next to the free text): when present the plugin
  generates candidate words, the model picks and justifies them, and invented words are checked against it; a language without it
  works from the text only.
- **Class design: (i)** a new base class for the anchored passage between `ExtendableEntity` and `Annotation`; `Annotation` stays the
  editorial one. **Table: SINGLE_TABLE + `kind` column** (V26, `status` / `source` made nullable by rebuilding the table and its
  indexes; old rows `EDITORIAL`; old backups without a kind mean editorial).
- **New words: added at once, marked *new*;** the author reviews them in the tab whenever they like.
- **Scope: direct speech only** for now. Thoughts (and anything else) are **opt in by the author editing the prompt template**
  (the request prompt is a template the author can change, an inherited prompt setting like the Beta Readers' prompts), not a
  switch in the plugin. **Lore entries are translated by hand** with *Translate this* ("Mulberry Castle" -> the conlang, then
  written into the entry), see *Conlang names in the source*.
- **Script: romanization only in the first version.** A custom font per language is **a possibility for later** (see *Later*).
- **Exports first: EPUB, DOCX and HTML, the source as the footnote**, plus *conlang only* and *source only*; Markdown, TXT, PDF and
  AO3 follow.
- **The reader's view is the author's decision, per book:** the author chooses what the readers of the book see, with the same
  choices as the export (*conlang with click to reveal the source*, *conlang only*, *source only*, ...) and **can force it**, so
  a reader cannot switch the conlang off. (Decided: the author's own views, the story card and the manuscript dialog they edit
  in, keep a personal switch, since the author must be able to read the source; the forced mode is what the viewer shows to others.)
- **Step 2 is just in time:** it does only what steps 3-8 need (the model-text accessor and its callers, the part-edit path, the annotation
  base class with V26 and the hiding from the lists, the kind registry); the viewer click and the export hook are designed in steps 9 and 10.
- **The core's annotation kind registry is full from the start:** a kind registers its flags (counted in the badge, tab and margin; shown in
  the viewer and the exports) **and its own mark renderer and click behaviour**, so other plugins can add kinds at once.
- **The author's own views keep a personal switch** when the book forces a reader mode; the forced mode is what the viewer shows to others.
- **Summaries read the source and are hashed on the stored text (the conlang):** the summarizer's input goes through the accessor, `BlockTexts.CURRENT` and the hash stay as they are; every translation or retranslate rehashes the summaries that covered the part and were valid (a translation never changes the meaning); the outdated ones stay outdated.
- **Central accessor for the text a model reads, all readers switched** (core services and plugins), with the guard test and the payload scan.

## Goal

The author defines a constructed language (or several). When a part of the story is final, the plugin asks the model which
direct speech is spoken in a conlang and what it is in that language, and **puts the conlang into the story**. The reader
**clicks the speech to reveal the English source**, in the viewer and in the manuscript dialog. The exports print the conlang with
the source as a footnote, or the source only.

## The central idea: the story holds the conlang, the English source is plugin data, the model reads the source

**Decided (the author's direction; it replaces the first idea, "annotate, never rewrite").**

**Terminology.** In a conlang book the **conlang is the original text of the book**; the English is only an artifact of not
making the generating model write the conlang (which would dilute the story). So the English the model writes and reads is
called the **source** (source text, source version), never "the original", in the data, the UI and the docs.

- The **text of the part is changed**: the speech in English becomes the speech in the conlang. The English source is kept
  with the passage, in the plugin's data (see *Storage*).
- Every path that gives the story **to a model** swaps the conlang back to the source, so the model keeps reading and writing
  its own language (the next part, the summaries, the lorebook scan, the reviewers...).
- So the plugin is two functions over the text of a part and its passages: `toModel(text, passages)` (the reverse) and the
  stored text itself (the conlang). Exports and the reader's switch use the same reverse for "source only".

**Why the work is split in two (the author's reason, and the point of the plugin).** The story is written by one request in English, and the
conlang by a **separate** request that has only the language and the part. If the story model had to write the conlang it would carry
the language (Vrakht's spec and lexicon are about 20 KB, about 6 000 tokens, in **every** turn of the story: "10k tokens of waste") and
**divide its attention** between the story and the grammar, and both suffer: the prose gets worse and the conlang is mediocre, a model
doing two jobs does neither well. Split: the story model writes in its best language with the normal prompt and cache, and the translator
spends its whole attention on the rules, on a prefix (rules, lexicon) that is **the same for every part**, so a server that caches the
prompt reads it once. This is also why the translation request is its own, tuned request with its own model setting (a model that
follows rules well, not necessarily the one that writes the best prose).

What this buys, compared with leaving the text alone and swapping at display:
- **Everything shows the conlang by default**: the viewer, the manuscript dialog, the phone, copy / paste, every export,
  without a hook in each renderer. A renderer that does not know the plugin shows the conlang without the click, which is
  a graceful failure.
- **A word is changed with search and replace** (the core's `ManuscriptReplaceService`, see *The lexicon*): the text is the
  data.
- One thing to maintain, the reverse, instead of a swap in every renderer and exporter.

What it costs (the risks, in order of how bad a miss is):
1. **A prompt path that does not swap shows the model the conlang, silently.** The model then answers in the conlang,
   misreads the scene or writes a summary of gibberish. Who gives the text of a part to a model today (found by looking for
   `getResponse()`): in the core `PrepareContentStep` (the generation), `SummaryService`, `LorebookActivationServiceImpl`,
   `StoryAround`, `AnnotateServiceImpl`, `RedraftServiceImpl`, the read commands for agents (`GetPartCommand`, `ListPartsCommand`,
   `ComparePartsCommand`, `CountTokensCommand`...); in the plugins arc-outline, beta-readers, book-description-generator,
   image-generator, knowledge-tracker (3 files), manga-generator, reviewer, sidequery, sillytavern-converter. Too many to
   fix one by one and to keep fixed: a **core accessor "the text of a part as the model reads it"** (plugins register a
   transformer, the plugin's reverse is one) that every one of them calls, and a test that fails when a new reader of
   `getResponse()` is not on the list of the ones that need the source. On the main path a **safety net**: after the payload is
   made (`AFTER_PREPARE_PAYLOAD`) the plugin looks for the conlang text of any passage still in it and swaps it.
2. **Translating is an edit of the text of the part**, with the side effects of an edit: the revision (`MessageRevisionService`,
   `RevisionOrigin`), the annotations moving (`AnnotationService.shift`), `originalResponse` (what the model generated, used to
   highlight the author's edits: the translated speech would look edited) and the **hash of the summaries**, see the next point.
   `ManuscriptReplaceService` already does all of these for a replace; the translation must go through the same path, and run after
   the redraft and the automatic annotation of the part (they read the English).
   **Summaries (decided): they read the source, but their hash is made of the stored text, the conlang.** Two separate things in
   `SummaryService`: the text a summary is *made from* (the input of the summarizer, meta summaries included) goes through the accessor
   and is the **source**; the text its hash is *made of* (`BlockTexts.CURRENT`, `message.getResponse()`) stays the **stored text**, so
   the core's checks of validity, the replace and everything that rehashes stay as they are and no summary code learns about the
   conlang. The consequence is on the edit path: a translation **changes the stored text**, so the hash
   of every summary covering the part changes. **A translation never changes what the text means (decided), so the commit always
   validates the hashes: it rehashes every summary that covers the part and was valid just before it** (`SummaryService.collectFirstBlock`
   / the revalidate calls the replace uses), whatever kind of translation it is: the first one, a **retranslate** after the rules or the
   lexicon changed, the one after a redraft. A summary that was **already outdated stays outdated**: the translation must not make a
   summary valid that was not (after a redraft the summary is outdated because of the redraft, which comes before the translation).
   A consequence: **the translation need not run before the summary is made**; either order ends valid. An edit of the conlang by hand
   outdates the summaries like any edit of the text.
3. **The author edits the conlang text by hand.** The conlang is the original, so this is normal and the author's words win: the
   passage is marked *edited*, its source is stale, and the reverse leaves the text as it is (the model reads the conlang of
   that speech; flagged, and an *Edit source* action re-derives it: the model translates the conlang back, or the author types
   the English).

Not a problem, decided:
- **No "restore the originals" is needed.** The conlang is the original; uninstalling the plugin leaves the book as the author
  wrote it. The history of a part (the revision the translation records) holds the English from before, so a part can go back.
  An **en masse "revert to the source"** (and an en masse translate) exist as commands, but **only while the plugin is installed**.
- **Searching the story for an English word** that is in a dialogue will not find it. Not a big deal: the author searches the
  book they have, which is in the conlang. Searching the sources too is an option for later, never required.
- **English speech written by hand** is translated by the manual run (below).

## The core annotations (the mechanism to build on)

`domain.model.impl.Annotation` (`AnnotationService`, user manual `books/annotations.md`) is a note anchored to a **range of
offsets** of a part's source text. It already does what the spans need and what would be expensive to rebuild:

- **anchoring**: the offsets follow the author's edits (`shift`), are found again by quote and context when the part is generated
  again (`relocate`, `QuoteFinder`), move on a merge (`moveToPrevious`), are copied to a branch (`copyForBranch`), go to the trash
  and to backups (`marshalForManuscript`); a passage that is gone becomes `ORPHANED`;
- `createFromQuote(manuscript, part, quote, before, after, text, source, type, label)`: exactly what the model's answer needs
  (it gives quotes, not offsets); a quote that is not found makes a note of the whole part, flagged `notLocated`;
- an extension is a **source** (`AnnotationSource`), has a free `type` and a `label` (the plugin's name) and, being an
  `ExtendableEntity`, its own data in `getAttributes()` (the source, the language, the speaker, `understood`, `edited`); the passage of the annotation is the conlang text in the story;
- the marks are drawn by `TextMarkup` as inline `<mark class=... data-annotation=...>` elements **inside the `Markdown`
  component**, a click is reported by `JavascriptLibrary.fireAnnotationClickEvent`.

What does not fit, so what a conlang span would break or need:

| Annotations today | A conlang span |
|---|---|
| a note **for the author** to resolve or dismiss, counted by the badge ("needs a look"), listed in the *Annotations* tab and the margin | hundreds of them, nothing to resolve: they would flood all three; an `ORPHANED` one is not a task either |
| the mark is an **underline with a popover** that shows the note | the passage is the conlang text and the popover's note is the English source, which is almost what the plugin wants (without resolve / dismiss) |
| drawn only in the author's story card (`ManuscriptStoryPart`, `PhoneManuscriptStoryPart`) | the **click to reveal** is also wanted in the **viewer** (`PartView` shows `msg.getResponse()` as it is) and in the manuscript dialog |
| "Annotate with AI" / `AutoAnnotate` is a separate workflow of the core (and the *Finality of the run* chain of the Knowledge Tracker) | the plugin is not a reviewer; the name clash is only a naming risk in the docs |

Options:
- **A. Reuse `Annotation` as storage and filter.** The span is an `Annotation` (source extension, `type` "conlang", `label` the
  plugin); a small core change makes the lists skip it (a flag or the type, so that the badge, the tab and the margin ignore it) and
  the viewer and the dialog learn the click. The hard part (anchoring, so that the reverse finds the conlang text) is free.
- **B. Own spans in the attributes of the part** with an own anchoring (`AnnotationAnchors` is the thing to copy: `shift`,
  `relocate`, the branch and merge cases). No core change for storage, but a copy of the most fragile code and every path that
  edits a part (the author's edit, redraft of a passage, merge, branch) must call it.
- **C. A core notion of an annotation *kind*:** a plugin registers a kind (a renderer for the mark, whether the badge and the
  lists count it, whether the viewer and the exports see it). The conlang spans are one kind; other plugins could add theirs
  (glossary hints, footnotes...). The largest change, and the cleanest one if the exports need it anyway.

**Decided: C**, built on the existing entity, with the design below.

### The class design (what the dig found)

Today: one entity `Annotation` on the table `annotations` (`V19__annotations.sql`, **shipped in 1.5.0**, so the table cannot be
edited in place: any change is a **new migration, V26**), the repository / service / commands / backup format / cleanup / UI all
name `Annotation` (46 files in the application, **no plugin** names it, so the plugin-visible API is not affected). The columns
`status` and `source` are `NOT NULL`; the anchoring (part, `startOffset`, `endOffset`, quote, context) is separate from what makes it
editorial (status, source, type, label, text, `sendToPrompt`, `notLocated`).

The shape you suggested: a base class for the **anchored passage** (part, range, quote, context, the anchoring and shifting) and
the editorial notes as a subclass. Two ways to name it:
- **(i) new base class between `ExtendableEntity` and `Annotation`** (e.g. `AnchoredPassage`), `Annotation` stays the editorial
  one: the least churn (the 46 files keep their name), the new base carries the anchoring;
- **(ii) `Annotation` becomes the base** and `EditorialAnnotation` the one with statuses: the cleanest names, but the rename touches
  the 46 files, the service, the repository, the commands and the manual.

How it is stored (Hibernate has `SINGLE_TABLE` as `Setting` and `JOINED` as `AI` / `Protocol` in the code already):
- **SINGLE_TABLE with a discriminator `kind`**: V26 adds `kind` (existing rows `EDITORIAL`) and has to make `status` and `source`
  nullable (SQLite cannot drop `NOT NULL`: the migration rebuilds the table and its two indexes); no data moves between tables, the
  queries by part stay one query. Probably the smallest migration.
- **JOINED**: the passage columns stay in `annotations`, `status` and `source` move to a child table (a data copy and a column
  drop, where an index covers `status`); each lookup of editorial notes joins.
- A plugin **cannot add an entity class** (`persistence.xml` is the core's), so the kind of a plugin is **not a Java subclass**: it is
  a row of the base entity with a `kind` string (here `conlang-translator`) and the plugin's data in the extended attributes.
  The core kind registry (C) says, for a kind: whether the badge, the tab and the margin count it, how its mark is drawn, whether
  the viewer and the exports see it. The editorial subclass is the one core kind.
- The backup JSON (`marshalForManuscript`) gets the `kind`; **old backups have none and mean editorial**. The cleanup
  (`@CleanupReference`), the trash, `copyForBranch`, `shift`, `relocate`, `moveToPrevious` move to the base and work for every kind.
- The service: the anchoring methods work on the base; the editorial ones (`setStatus`, `update`, "needs a look") on the subclass.
  The commands (`/create-annotation` ...) stay editorial.

Chosen: (i) and SINGLE_TABLE. To check when step 2 starts: whether the tests of the migrations run on a copy of a database of the
last release (so V26 is tried on real rows), and what else reads `status` / `source` (the cleanup, the backups, the fulltext).

## The conlang definition: rules and lexicon are separate

A language of the book (a user level library of languages, picked per book, like the personas of Beta Readers?) has two
parts that are written, read and edited differently:

**Rules** (written by the author, free text, what the model reads as the grammar book; the plugin does not parse it):
- phonology (the sounds, what a word may look like), orthography / romanization, stress;
- grammar: word order, cases, particles, plurals, tenses, questions, politeness;
- **word formation**: how new words are made (roots, compounds, affixes, borrowing, how names and loanwords are treated), so a
  made-up word has to follow the rules and the model can say which rule it used;
- register and style notes, some sample sentences; the **speakers** (next section).
- The rules are only edited by the author (the model may suggest a change to them, see *Later*, never makes it).

**Lexicon** (words, **owned by the author but written mostly by the model**):
- The model is told: use the lexicon, and **if the speech needs a word that is not in it, invent one by the rules, and report it**.
  Every reported word is **added to the lexicon at once** (so the next part says the same thing the same way) and is marked
  *new* until the author has looked at it; the author can **change, lock, retire or delete it** at any time.
- An entry: the **word** (romanized, the script form when there is one), its **category** (below), the **gloss** (what it means, in the story's language),
  the part of speech and optional notes, and
  - **rationale**: why the model chose this word (which rule or root, "compound of *ael* 'light' + *vor* 'keeper' by the
    compound rule", a sound it wanted for the mood...). The model must write it, the author reads it to judge the word and can
    edit it (it also goes back into later requests, so the next invention follows the same logic);
  - **origin**: *author* or *model* (+ the part it was invented in, the model and the time); an author's word is never changed
    by the model, an edit of a model's word makes it the author's;
  - **status**: *new* (not looked at), *accepted*, *locked* (the same padlock as the Knowledge Tracker's items: the model may use
    it but never redefine it), *retired* (kept but not offered, for a word that was replaced);
  - **register** and **variants**: see *Registers and synonyms* below;
  - the **spans that use it** (see below).
- **Categories.** A language has a list of **categories** (the author's own, ordered, e.g. *People & Social*, *Military & War*,
  *Kinship*, *Numbers*, *Particles*) and every entry has **one category** (a word is listed once; a free tag for crossing groups
  could come later). A category is a **semantic group**, separate from the entry's *kind* (root, compound, particle, name,
  number) and from the sense's part of speech, which Vrakht blurs on purpose. A category has a name, an optional note ("words of
  the legion and the siege") and a flag **always send**. Uses:
  - **organization**: the lexicon tab groups, filters and collapses by category; the author moves entries between them;
  - **the payload**: the lexicon goes to the model **grouped under its category headings**, as the author already writes it
    (`[Military & War]` then `drak - war / battle / fight`), which is shorter than a flat list (no kind or category on every line)
    and helps the model find a word by the concept it wants;
  - **sending part of a big lexicon**: below a size limit everything is sent. Above it the plugin sends the categories flagged
    *always send* (particles, pronouns, numbers, kinship...) plus the **categories of the words the speech needs**, found by a cheap
    match of the speech's words against the glosses, plus the locked and recently invented words; whole categories rather than
    single words, so the model sees the neighbours of a concept and reuses a word instead of inventing a second one;
  - **new words**: the model must give a category for every word it reports, an existing one or a proposed new one (the author
    sees a proposed category as *new* too, like a new word), so the lexicon stays organized without work;
  - **the designer and bulk vocabulary**: the seed lexicon is made category by category, and "invent the words of this category"
    or "of these meanings, into this category" are actions of a category.
- Sent with the request: the rules first, then the lexicon (as above), then the part and its context, so the prefix stays
  cacheable. The order is stable (the categories in the author's order, the words within a category as the author keeps them, a new
  word at the end of its category), so a new word changes the request only from its place on; for a long run that matters little,
  the prefix is read once per part.
- **Changing a word that is used = search and replace.** The conlang is the text of the story, so the author's edit of a word
  (or its retirement for another) is a replace of the old form with the new one in the parts: the core's
  `ManuscriptReplaceService` (`plan` shows the matches, `apply` replaces, records the revisions, moves the annotations and keeps
  the hashes of the summaries valid; the active branch or all of them), and the plugin updates the conlang strings it holds in its
  own data (the passages follow by the annotations' shift). Whole word by default; **inflection** is the author's to handle
  (the stem alone, or a regex), the preview shows how many matches in how many parts first. **Retranslate** (the model redoes a
  speech with the new lexicon) stays for what a replace cannot fix. Because the text is the data the plugin does not have to
  record which words every speech used; the lexicon entry could still count its uses by a search, and a consistency check
  (a word of the speech that is not in the lexicon) is a later feature.

The model's answer therefore has, besides the translations, a list of `newWords: [{word, gloss, partOfSpeech, rationale,
rule}]`; the checker drops a new word that is already in the lexicon (the
existing entry is used), flags a word that breaks the phonology only when the rules say so in a form it can check (probably
never in the first version: only the model checks), and a new word without a rationale is kept but flagged.

**Registers and synonyms.** A language has **registers**, the author's own ordered list (e.g. *formal*, *neutral*, *soft*, *colloquial*, *vulgar*, *archaic*,
*poetic*), each with a short note; a lexicon entry has **one register** (default *neutral*) and **variants**: links to entries that **mean the same thing**
(`Mela` formal, `Melis` soft, `Lelya` vulgar: one concept, three entries). The editor shows the variants of a concept together, and the payload sends
them **grouped**: `breast: mela (formal), melis (soft), lelya (vulgar)`. The request tells the model to **choose the form by the register that the
speaker and the situation ask for**: a noble addressing a king, a soldier cursing, a lover whispering. This is a different relation from a homophone
(one form, several meanings: *senses*); a word may have both. The model's `newWords` give a register too (and the concept, i.e. the variant it joins, when
it is one). **The voice of a character** is what the model matches to the register: see *Who speaks the conlang*.

Also in a language:
- optional, **later**: a **font** for a script (see *Later*), a **CSS style** of the span (italic, color).
- could be read from a **lorebook entry** instead of a form (a "Language: Sylvari" entry), so the author writes the rules once
  and the model has them for the story too. Open question below.

## Where the pieces live (settings, not the story sidebar)

The inherited-settings mechanism (`InheritedSettingRegistry`, developer manual *Inherited settings*) carries **simple values** (text,
choice...) through book > folders > the plugin's user level, and the user level is the plugin's own panel in the *Settings* screen.
A language is a structured object, so it does not travel as a setting; it is **chosen** by one:

- **The library of languages** (rules sections, categories, lexicon) is the plugin's panel in **Settings → Extension Settings →
  Conlang Translator**, the user level, like the persona library of the Beta Readers: a list with *New*, *Design with AI*, *Copy*,
  *Export / Import*, *Delete*, and a button that opens the **language editor**, a large dialog (maximizable) for the rules, the
  lexicon by category, the *new* words to review and the designer's actions. Everything that defines a language is edited here.
- **Which language a book uses** is an **inherited choice** (`InheritedSetting.choice`, the choices from the library): the book's
  own, else a folder's, else the user's default, so a series in one folder shares a language. The book and the folder dialogs have the
  field under *Extension Settings*, with the other per-book options of the plugin (translate automatically, reader mode, the
  model, the request prompt as inherited text).
- **No book-local languages (decided).** A language is only in the library. A language for one book is a **duplicate with another
  name** (*Duplicate* in the library: an independent copy of the rules, the categories, the lexicon and the links, which then grows
  on its own). A library language that is *selected* by several books is one object: a change, and the words a run adds, reach all of
  them. Consequence: **the backup of a book does not carry the language** (it is the user's library, not the book's). The
  translated text and the sources of the passages are in the book, so reading, the reverse and the exports of the book keep working
  without the language. A restore in another account (or after the language was deleted) has a book whose language field does
  not resolve: translating is off, nothing breaks, the field says the language is missing.
  **The library itself is not lost:** the languages are part of the user's data, which the **database backup** carries.
  *Export / Import* of a language (a document with the rules, categories and lexicon, without the links) is how a language is
  **shared** with another user or moved to another installation.
- **The story side is thin:** the menu actions of a part (*Translate part*, *Retranslate*), the *Translate this* box, the progress of a
  run, and a **read-only lookup** of the words of the book's language (search by word or meaning, so the author can look a word up
  while writing, without opening the settings). Probably a small sidebar tab with just that, or a dialog opened from the story menu;
  not the editor. The *new* words to review show as a badge there that opens the editor.
- **The designer gets its references from the language, not from a picker each time:** a language has **linked references**, an
  **example book** and an **example lorebook** (either, both or none), chosen once in the language editor and kept with the language
  (by their uuids, from the user's own books and lorebooks; the lorebook is an entity of its own, a book only points to it, so it
  can be linked without any book). The designer reads them where it needs the world:
  - the *brief* and *Generate words* take the lorebook (the people, the places, the names that want a word; the entries to use can
    be narrowed, a lorebook with its subbooks and a filter by entry, so a large lore does not fill the request, the same choices as
    the Knowledge Tracker's lorebook mode: all, none, picked entries);
  - the *test* and *Review* take speeches from the example book (a sample of its direct speech, so the author reads the new language on
    their own story);
  - *Add a section with AI* sees both, so a numbering system or a politeness system suits the world.
  A link that no longer resolves (the book or
  lorebook was deleted) is shown as such in the editor and the designer works without it. The links are **not exported** with a
  language and are dropped by a copy into another user's library (the uuids mean nothing there); a copy inside the user's own
  library keeps them.

## The conlang designer

A section of the plugin (a button **Design with AI** in the editor of a language, and for a new language) where the author makes the
language **with the model**, instead of writing the rules by hand. Its output is what the rest of the plugin uses: the **rules**
and a first **lexicon** of the language (see *The conlang definition*); nothing in the designer is needed to translate.

**The flow** (stages, each proposed by the model, edited and accepted by the author before the next; the author can go back, or
skip a stage and write it by hand):
1. **Brief**: what the language is for (the people, their world, their culture), inspirations ("harsh, like Old Norse", "sing-song,
   Polynesian-like"), constraints (no sounds that English readers cannot read, short words, how it should *look* on the page), what
   to avoid. The **lorebook can be given** (the entries about the people and the place; the model reads them, as the Lore
   Generator does) so the language fits the world.
2. **Sound system (phonology)**: the sounds, the syllable shape, stress, what is forbidden.
3. **Writing (orthography / romanization)**: how each sound is written, so that the text is readable and consistent.
4. **Grammar**: word order, cases or particles, plurals, tenses, questions, politeness, how adjectives and possession work.
5. **Word formation**: roots, compounds, affixes, loanwords, **how names are made**; the rules the model cites as the rationale
   of an invented word later.
6. **Seed lexicon** (made category by category): a core list (the basic words: pronouns, numbers, kinship, body, nature, common verbs; a Swadesh-style list of
   100 or 200 meanings), then **the names of the book** (places, peoples, gods, the characters' names from the lorebook and the
   story) so the first translations already agree with the lore; each entry with its **rationale**, as the lexicon wants.
7. **Test**: the model translates sample sentences (the author's, or taken from the story: some speeches of the book) with the new
   language, and the author reads them; what looks wrong sends the author back to a stage ("the plural is ugly", "too many
   vowels").
At the end the **rules** are saved into the language, the words into its lexicon (origin *model*, status *new*), as a normal
language. A language the author already has can be opened in the designer too, to **extend** it (new words for a list of
meanings, a section of grammar the rules do not cover, a missing word-formation rule) or **review** it (contradictions in the
rules, words that break the phonology, gaps).

### The designer inside the editor, at any time

The designer is not only the first-time wizard. The **language editor** (in the settings: *Extension Settings* of the user, the folder
and the book dialogs, wherever the language is edited) has the designer's actions **on every part of the language**, so a language
grows after it exists:
- **On a category**: *Generate words...* ("30 words for the category *Cookware*", with a count, an optional note "kitchen of a
  siege camp, hard words") makes the entries, each with its gloss, rationale and the category, checked against the rules and the
  lexicon (no duplicate: the same meaning twice; the same form for a different meaning is allowed, see *Homophones*; the word generator's candidates when the phonology is structured).
  Creating a category and filling it is one flow: *New category* then *Generate words for it*.
- **On the rules**: *Add a section with AI...* for something that was missed: "a **numbering system** (base-24, digits 0-11, powers
  with names)", "colors", "politeness and attitude prefixes", "how names and loanwords are written", "a phonology and spelling
  section from the words that exist". The model reads the **existing rules and lexicon** so the new section agrees with them and
  proposes a **new rules section** (title, text) plus the words it needs; the author edits and accepts it. Also *Rewrite this
  section...* with a note, and *Review the rules* (contradictions, undefined pieces, the gaps the Vrakht file shows).
- **On an entry**: *More senses*, *Alternatives* (three other words for the same meaning, by the rules), *Explain* (the rationale
  again), *Check against the phonology*.
- **A free box**: *Ask the designer...* with the language as the context ("add a plural for the military words", "why do both `den`
  and `fall` clash"), which answers in a chat and may offer changes to apply.
Every action gives a **proposal**, never a change: a list of entries and / or sections in a window like the Knowledge Tracker's
review (check boxes, editable text, what the model thought), and **Accept** writes them (entries as origin *model*, status *new*,
sections as new rules sections). The request is the designer's, with its own prompt templates and model setting. When the language
is **shared** (inherited by other books), the editor says so before an accept, since the change is for all of them.

**Decided:** stages with a chat inside for a new language; optional structured phonology with a word generator; the actions
above inside the editor at any time. **Ideas still open:**
- **Chat or stages (decided: stages, chat inside).** A **conversation** (the author talks to a designer persona, the model proposes and asks questions, as the Beta
  Readers chat does: streamed replies, reasoning shown) next to / instead of a **fixed sequence of stages** with a proposal each. The
  chat is freer and fits "make it more guttural"; the stages guarantee that nothing is forgotten. Perhaps stages as the frame, the
  chat inside each stage.
- **Structured phonology and a word generator (decided: optional).** The rules are free text, but if the phonology stage also yields a **structured
  sound inventory and syllable template** (consonants, vowels, allowed shapes, banned clusters), the plugin can **generate
  candidate words** itself (like the name-generator plugin's Markov chains, but from the language's own phonotactics), and the model
  *picks and justifies* instead of inventing a string from nothing: more consistent words, fewer ones that break the sound system,
  and a check on the words the model invents later (does the word fit the inventory?). The cost is a second, structured part of the
  rules that the author may edit and the plugin must read. Optional: a language without it works as the text only.
- **Tools for the model.** If the **name-generator** / **name-provider** plugins are installed their functions (name categories,
  real names) can be offered to the designer for inspiration (never a dependency).
- **Registers** (a designer action on a concept): *Add a vulgar variant*, *Add a formal variant* make the variant entry with the register and the link;
  *Review* lists the concepts that have one register only where the language otherwise has several.
- **Bulk vocabulary** (also per category): "invent words for these 50 meanings", "the names of all the places in the lorebook" as an action of the
  lexicon, not only of the designer.
- **Import**: paste an existing description of a language (a wiki page, notes) and have the model sort it into rules and lexicon.
- **Export**: the language (rules, lexicon) as a Markdown / JSON document, to keep outside the book, to share, or to import into
  another book's language (the library's copy / clone already does it inside the application).
- The designer shares the **model / protocol settings** of the plugin (a designer-specific override as the Beta Readers chat has),
  and its prompts are **templates** the author can change, as the translation prompt is.

## Who speaks the conlang

The idea so far: a character speaks the conlang **unless listed as not knowing it**, or the text says otherwise.

- Per language: *Speakers* mode **everyone except** the listed characters (the default, the author's idea) or **only** the
  listed ones; the characters are names with aliases, matched against the POV / present characters of the part (as the
  Knowledge Tracker does, own list, no dependency on Character Tracker, the same picker for names).
- **Character notes**, kept with the speakers list: for each character the **gender** ("Mara: a woman"), and the **voice** ("rough, crude soldier",
  "formal court speech"), and whether they know the language. The request's character list is made from them. Two trial findings need them: the
  **gender** (a gender-marking language gets it wrong when the part only says "her"; the checker did not help) and the **register** (the form of a
  word follows the voice). The notes are written by the author (or proposed by the designer from the lorebook), the plugin has its own list and does
  not depend on another plugin; the Character Tracker's cast could fill it in if it is installed.
- **The text overrides the list**: "she said in Common", "he switched to Sylvari", "the guard could not understand a word"
  are read by the model, which has the lists and the part, and decides per piece of speech. So the model is classifier and
  translator in one request: for every direct speech it answers *no translation* (the story's language) or *language + text*.
- Speech to someone who does not understand it: the translation is still made (the reader gets the flavor); a flag
  `understood: false` on the span could mark it so the reader sees "(not understood)" behind the click, and so a later prompt
  could tell the model that the character does not know what was said (see *Later*).
- What counts as speech: **direct speech** only by default (quotation marks of any style: `" "`, `“ ”`, `« »`, `「 」`, dialogue
  dashes). Options: thoughts, written text (a letter, an inscription), songs and chants, spells.

## When it runs

Like the Knowledge Tracker: on the **final** text of a part, in the background, on a worker thread.

- Reuse its finality rule (the part is settled when something follows it, or a new generation starts, or the book has no
  automatic annotation and the generation ended; with automatic annotation / redraft on, after `AutoAnnotate.finished`).
  See the Knowledge Tracker's TODO, *Finality of the run*: the same chain applies, a draft must not be translated.
- Triggers: automatic after a part (setting, on / off), a **"Translate"** action in the menu of a part, a **range** (all parts
  of a branch that have none, to translate an existing book), and a **retranslate** of one speech or a part.
- A regenerate / swipe makes a new part text in English, which is translated in its place (the passages of the old text go
  with it; the swipes keep their own). **The translation is an edit of the text** (see the risks above): through the path of
  `ManuscriptReplaceService` / the author's edit, with a revision, and before the summary of the part.
- One request per part (several parts when short): the conlang definitions and the lexicon first (the cacheable prefix), the
  part and its context (POV, present characters, a few earlier parts for who is speaking to whom) after.
- Model, protocol and the lorebook use are settings (book > folders > user, **inherited settings** as Beta Readers has), and
  the request is **structured JSON** (a schema: `quote`, `speaker`, `language`, `text`, `newWords[]`), streamed so the
  progress and the reasoning can be shown.
- The answer is **checked**: the `quote` must be found in the part verbatim (a quote that is not there is dropped or searched
  fuzzily, then flagged), the language must be one of the book's, no overlapping spans, nothing outside the quotation marks.

## Translating by hand, redraft and retranslate

All of these work on the **source**: `source = toModel(text of the part)`, so what the model and the plugin handle is always the
English, whatever the part holds now.

- **Translate part** (menu of a part; command `translate-part` for agents): runs the request on the source of the part and
  writes the result as the text. It can take a **new source**: after a **redraft** the part holds the English the redraft wrote
  (the redraft reads the source, so its prompt shows English, and its result is English), and *Translate part* turns that into
  the conlang. The automatic run does the same after a redraft finishes, when the setting is on (after the automatic annotation
  and redraft chain, see *When it runs*).
- **Retranslate** is the same action on a part that is already in the conlang: the source is rebuilt from the passages, so
  after the **rules or the lexicon changed** the author retranslates a part, a range or the book and gets the new conlang.
  A speech whose source did not change **may keep its translation** (a setting, on by default for a range) so a run does not
  rewrite what the author has already read and fixed by hand; *Retranslate all* ignores that.
- **Translate this**: a small text box (a dialog or a panel of the tab): type or paste English, choose the language, get the
  translation, copy it or insert it at the cursor of the part being edited; the new words it needs go to the lexicon like any
  other. For one-off lines and for a part the author wrote by hand, without running a whole part.
- **Redraft of a passage** (the author selects text in the card; `RedraftService.Passage`) selects in the **stored** text, but the
  model works on the source: a selection inside a conlang speech must map to that speech's source (the passage is redrafted as a
  whole, its result is translated and replaces the speech). To check: the offsets of a selection against the passages, and
  what `RedraftService` does with the text it is given (it must call the model accessor, like the other readers).
- The manual runs are the answer to English written by hand and to a changed rule; none of them needs the plugin's data to be
  more than the source of each passage.

## Conlang names in the source

Lore entries are translated by hand (*Translate this*: "Mulberry Castle" gives the conlang name, which the author writes into the
entry). So the **source the model reads can contain conlang words**: a name in the lore, a name the narration uses. This is not a
problem, with these rules:
- The reverse is **per passage** (the recorded speeches), so conlang words that are not in a passage are never touched; a name
  in the narration stays as written, and so does a name inside a speech's source.
- The translation request is told: **words that are already in the conlang, and every word of the lexicon, are kept as they are**;
  names are looked up in the lexicon first (so "Mulberry Castle" in a speech becomes the conlang name of the entry, not a new
  invention).
- **The lexicon is the natural home of the names**: *Translate this* can save its result as a lexicon entry (word, gloss "Mulberry
  Castle", a kind "name") as well as copy it for the lore entry, and the model reports the names it invents like any new word.
- The model that writes the story sees the conlang names in the lore and may use them; that is wanted.
- Two small hazards: a **lorebook keyword** is English ("Mulberry Castle") while the narration may write the conlang name, so
  the author adds the conlang name as a keyword (the lore of the speech activates from the source anyway); and a conlang word that
  is also an English word (a collision) could confuse the reverse's checks, a checker case to test.

## Display

With the conlang in the text every view shows it already. What is left is the **click that reveals the source**, on the
desktop and the phone, and the **reader mode**, a setting of the **book** that the author chooses and may force on the readers of
the viewer: *conlang with click*, *conlang only*, *source only* (the same choices as the export). *Source only* is the reverse applied
to the text of the part before it is shown. The author's own views keep a personal switch (decided).

Places: the story part card (`ManuscriptStoryPart`, `PhoneManuscriptStoryPart`, both `@Extendable`), the **manuscript dialog**,
the **viewer** (`ui.main.Viewer`, the published book, where the reader may not be the author: the data must be saved with the
part, not kept in the session). All render the text with Vaadin's `Markdown` component.

Checked: **inline HTML does pass through `Markdown`**: `TextMarkup` puts `<mark class="..." data-annotation="...">` into it for the
annotations, the search and the highlight of generated text, and the click is a JavaScript listener on the element. So the
passage can be marked `<mark class="conlang" data-source="...">`. To check:
1. In the author's card the annotation marks and their popover (the note = the source) may be enough for the click; the
   marks come from `StoryAnnotations.marks` (`ManuscriptStoryPart` ~2113), the popover from `CardAnnotations`.
2. The **viewer** does `new PartView(msg.getResponse(), ...)` and knows nothing of marks: it needs the marks of the conlang
   passages and a click handler (core change, or a decorator of `createPart` with a `@PublicAPI` that the plugin uses).
3. The manuscript dialog: which of the two paths it uses.
4. The mark is **per passage**, so the source must be at hand in the browser (`data-source`, the text is the author's own,
   no secret) or fetched by the id of the annotation on the click.

## Exports

The text is the conlang already, so *conlang only* needs nothing. **First formats: EPUB, DOCX, HTML**, the source as the footnote; the rest later. Options in the export dialog (a plugin section, or a core
change if the dialog has no way for plugins to add options):

- *Conlang only* (as the text is) / *source only* (the reverse) / *conlang with the source as a footnote* /
  *source with the conlang as a footnote*;
- footnote style per format: Markdown `[^n]`, HTML / EPUB notes (EPUB 3 `epub:type="noteref"`), DOCX real footnotes, PDF,
  TXT (endnotes at the end of the chapter), AO3 HTML; the footnote text could carry the **speaker**;
- optional **glossary** at the end (the lexicon, the words used in the book).

Needs a core extension point: the exporters read the text of a part themselves (`Exporter.export(options, chatMessages, ...)`),
so a **text transform hook before the export** (the plugin returns the text, with footnote markers when asked, and a list of
notes the exporter prints in its own way). The reverse is the same function as the one for the model. The footnote *printing*
is the work, per format.

## Storage

- The **text of the part** is the conlang version (the normal `ChatMessage.response`).
- Per passage (an `Annotation`, option A, or the attributes of the part, option B): the **source**, the language, the
  speaker, `understood`, the model and time, a hash of the conlang text it made (to tell that the author edited it: *edited*).
  The passage's range / quote is the conlang text, so the reverse finds it by the anchoring of the annotations.
- Languages: user settings (library) + a book's selection and overrides in the book's attributes. The lexicon with the language.
- No Flyway migration (extended attributes); the data travels with backups. Uninstalling the plugin leaves the conlang text (the
  book); the sources stay in the data and in the history of the parts.

## The test conlang: Vrakht (`vrakht.md`, what it shows)

The author's LLM-assisted language, kept in this folder as the first test case: a grammar in 8 rules, about 310 words in 14 titled
lists, base-24 numbers, and at the end a compact **LANGUAGE INSTRUCTION** block, the text the author pastes into a prompt today.
Read as a test of the design (a parse of the file; the counts are exact):

- **Rules are several sections, not one text.** The file has a long spec, possessives, numbers, and the compact instruction block.
  So the rules of a language are a list of **named sections** (title, text, *sent to the model* yes / no / only for the designer),
  and the compact block is one of them. The whole file is about 17 KB (about 5 000 tokens): at this size the whole lexicon is simply
  sent, the filtering is for much bigger languages.
- **The lexicon import cannot be a regex.** The `[Category]` headings are the **categories** of the language (14 titled lists, about 40
  headings), and the import should keep them; the lines are `word - gloss`, `word- gloss`, `[Category]` and `[ Category ]`, notes in brackets,
  glosses split by `/`, compounds, conversion tables. A model sorts a pasted description into rules and lexicon (the designer's
  *Import*), a line parser gives the first guess. (Aethel-Vael below has tables of three shapes: the import cannot assume one.)
- **One word, several senses.** Of 314 distinct words, 34 are defined more than once. Most are repeats of the same meaning (`gab`,
  `hab`, `mak`, `slaf` listed twice), but many are **deliberate conversion**, the language has no word classes (`blod` blood /
  lineage / bleed, `brand` fire / burn, `tod` death / kill, `vakt` wall / guard, `drak` war / fight, `pakt` treaty / bind). So a
  lexicon entry has **senses** (a gloss each, a part of speech when it matters). Only a **repeat** (the same meaning listed twice) is a
  fault, to merge. The same form with different meanings is **normal** (see *Homophones* below): `den` is both "because / therefore" (a
  linker) and "think" (a verb); `fall` is the mood prefix *Fall*, a verb "fall" and a noun "defeat"; `hoh` is the prefix *Hoh* and "hollow"
  in `hoh-kop`; `groz` is "goods" and "great"; `ord` is "grammar" and "law"; `stamm` "lineage" and "root"; `zeik` "written sign" and "rune".
- **Productive compounding must be recorded.** Rule 3 says to make complex terms by hyphenating roots (`eiz-blod` veteran,
  `drak-karn` battlefield, `vrak-haus` fortress). The model will do it all the time, so an invented compound is a **new word with
  its parts as the rationale** and goes into the lexicon (kind *compound*), otherwise the next part may build another word for the
  same thing.
- **Parts the model has to guess are findings of the review:** the suffix `-lig` (four words, no rule), `laff` and `zunge` (used in
  `laff-skalf`, `lug-zunge`, never defined), `hoh` = hollow, the six **attitude prefixes** (*Gna, Mok, Hoh, Fall, Droh, Acht*) that
  have no slot in the sentence blueprint and are not in the compact block, so the model will never use them, although they are what
  would carry the tone of a speech; names and proper nouns (no rule), punctuation (`Frag` replaces `?`, nothing about the rest),
  yes / no, here / there, this / that, who / what / why (missing, the first run will show how the model invents them).
- **Writing is inconsistent and nothing says so:** there is **no phonology or spelling section**; the sound /ʃ/ is `sh` (`marsh`, `mosh`)
  and `sch` (`schi`, `schnel`, `scharf`), /x/ is `kh` (`rokh`, `vekh`, `likht`) and `ch` (`acht`, `reich`), doubled letters appear
  without a rule (`magg`, `hakk`, `bett`). The designer's *Review* should find exactly this, and a structured phonology would settle it.
- **The verb unit is written two ways:** the spec says separate tokens ([Tense] + [Negation] + [Adverb] + [Stem]) and every
  example hyphenates (`ja-se`, `ve-deng`), and `ne-krag` is the word "weak" and also "not strongly": parsing the typography of a
  translation is ambiguous, so the checks below must be **lenient about hyphens**.
- **Leftovers of generation:** the headings promise 30 descriptors (27 are there) and 15 numbers and quantifiers (13); the base-24
  system is right (all seven conversions and 24^3 = 13 824 check out). A *Review* that compares what a heading promises with what
  is listed is cheap.
- **Good for the plugin:** Vrakht is rigid enough to be **checked without a model**, which is what the request's checker wants:
  every translated speech must start with a mood particle (`Da`, `Frag`, `Bef`); every object is behind a role particle; `za` never
  follows another role particle; and **every token is in the lexicon, or a hyphenated compound of lexicon words, or reported as a new
  word**. The last check alone catches the model inventing a word and not saying so, and it needs only the lexicon. Checks that
  come from the rules (the first two) are the language's own, so the language may carry a **list of checks** (a later feature: the
  rules stay free text and the author writes the checks, or the designer proposes them); the lexicon check is general.
- **The vocabulary is small and military** (an imperial tongue of about 310 words): ordinary dialogue will need many invented
  words from the first part. That is the right stress test for the lexicon growing, the rationale and the review of *new* words.

## Commands for agents: everything the UI does (decided: nothing is UI-only)

Every action of the plugin, the designer included, is also a **command** (`@Command`, as the Knowledge Tracker's `KnowledgeTrackerCommands`), so
an agent can build, grow, run and check a language and translate a book without a person at the dialogs. How:

- **One service per area, the UI and the commands both call it** (no logic in a dialog or in a command), so they cannot differ. A test
  lists the service operations and the command that wraps each, and fails for an operation without one.
- **Names** carry the prefix `conlang-` (they share one namespace with every command of the application and the other plugins).
- **Same conventions as the Knowledge Tracker:** the reads are `readOnly` (`NON_DESTRUCTIVE`); the writes are `NON_DESTRUCTIVE` or
  `DESTRUCTIVE` by what they lose; a command that asks the user (`context.confirm`) is `async = true`; a delete is `linkedCommand` to
  its list; the commands that call the model run on a **worker thread** and the progress shows in the UI; every command that makes a
  proposal has **`ask true`**, which returns the proposal as a **numbered list** without applying, and `conlang-apply-changes` applies the
  numbers the agent gives (the KT's `knowledge-process ask true` and `apply-knowledge-changes`).
- A language is addressed by **name or uuid**; commands that work on a book take the book as the other commands of the application do
  (the part by number / uuid); to check: how the commands of a plugin get the user-level scope (`CommandScopes`) when they are not about a
  book.

**The commands** (a first list, the names are not final):

| Area | Commands |
|---|---|
| Languages (the library) | `conlang-list`, `conlang-get` (the rules sections, categories, counts), `conlang-create`, `conlang-duplicate`, `conlang-update` (name, notes), `conlang-delete` (asks), `conlang-export`, `conlang-import`, `conlang-set-references` (example book, example lorebook), `conlang-set-book-language` (the inherited choice of a book or a folder, if no core command sets an extension setting) |
| Rules sections | `conlang-list-rules`, `conlang-get-rules`, `conlang-add-rules`, `conlang-update-rules`, `conlang-delete-rules` (asks), `conlang-move-rules`, the *sent to the model* flag as a parameter of update |
| Categories | `conlang-list-categories`, `conlang-add-category`, `conlang-update-category` (name, note, *always send*), `conlang-move-category`, `conlang-delete-category` (asks where its words go) |
| Lexicon | `conlang-list-words` (filters: category, status *new* / locked / retired, text), `conlang-get-word`, `conlang-add-word`, `conlang-update-word` (senses, category, rationale, notes), `conlang-delete-word` (asks), `conlang-lock-word` / `conlang-unlock-word`, `conlang-retire-word` / `conlang-restore-word`, `conlang-accept-words` (marks *new* ones as looked at), `conlang-check-lexicon` (repeats of a meaning, homophones as information only, words not in any category, compounds with unknown parts) |
| Changing a used word | `conlang-replace-word` (`plan` returns the matches and parts; `apply` runs the core replace, asks; both over the active branch or all branches) |
| Translating | `conlang-translate-part` (the part; `source` takes a new English text, after a redraft; `ask true`), `conlang-translate-range`, `conlang-retranslate` (a part or a range, *keep unchanged translations* option, asks for a range), `conlang-translate-text` (the *Translate this* box: text, language, optional *save to the lexicon*, returns the translation and the new words), `conlang-revert-to-source` (a part or a range, asks), `conlang-edit-source` (a passage: new English, retranslated), `conlang-status` (which parts are translated, edited, stale, untranslated; the run in progress), `conlang-check-translation` (the lexicon and the language's checks against a part) |
| Reading | `conlang-get-source` (a part as the model reads it) and `conlang-list-passages` (a part's passages: conlang, source, language, speaker, edited); the **read commands of the application** (`get-part`, `list-parts`...) go through the model accessor (see the central idea), so an agent reading a part gets the **source** unless it asks for the conlang |
| Designer | `conlang-design-words` (language, category, count, note; creates the category when it is new; `ask true`), `conlang-design-rules` (language, the topic or the instruction: a numbering system, colors, politeness...; `ask true`), `conlang-rewrite-rules` (a section and a note), `conlang-review` (the rules and lexicon, a report, read-only), `conlang-word-alternatives`, `conlang-word-senses`, `conlang-ask-designer` (the free question, read-only answer, may offer changes), `conlang-design-stage` (language, stage, brief or note: the stages of a new language one by one, `ask true`), `conlang-apply-changes` (the numbers of a proposal) |
| Macros | read-only, `availableInMacros`: `{{command::conlang-lexicon::<language>::<categories>}}` (the lexicon block as the request sends it), `{{command::conlang-rules::<language>}}`, so the author's own prompts and the other plugins (the Lore Generator, the Arc Outline) can bring the language into a prompt |

Notes:
- **A command is as safe as the UI action:** the destructive ones ask (`confirm`), and in an agent session the approval of the
  application applies (data severity), as for the Knowledge Tracker.
- **A proposal is not a change** for an agent either: `ask true` returns it, `conlang-apply-changes` applies; without `ask true` the
  command applies at once, as `knowledge-process` does when *Ask before applying* is off.
- The **docs**: the commands go into `manual/user/commands/index.md` under the plugin, with the parameters, and into the manual page of
  the plugin; `{{command::...}}` macros into the macro list.

## Agent runs: duplicate, lock and show the difference (the hardest part)

An agent run works on a hidden **working copy** of the book and its lorebooks, and the review is a **three-way merge** (the book as
the run started, the agent's copy, the book now) with checkpoints and a *sync* on resume (developer manual *Agents*, `AgentMergeService`,
`MergeDiff`, `AgentRunPairService`; the extension data of a book, part, lorebook or entry is merged as JSON, place by place). A language is
a **library object of the user**, outside the book and its attributes, so none of this covers it. An agent that builds or changes a
language (new words, a numbering system, a translated book that needs words) must therefore work on a **duplicate** of it, the original
must not change until the user has seen the **difference** and taken over what they want. Found in the core (nothing of it is visible
to a plugin today: no lifecycle event of a run, no extra section in the merge, `CommandScope` gives the model, not the run's copy):

**The design**
- **Copy on first write (lazy).** Most runs never touch a language, so nothing is copied at the start. The first *writing* conlang
  command of a run (`conlang-add-word`, a translation that must invent a word, a designer action applied...) **duplicates the language**
  for the run (a hidden duplicate, not in the library, with the identity map `original uuid <-> duplicate uuid` of its words, categories
  and sections, as the run pairs of the book), takes the **base snapshot**, **locks the original**, and the copy's book from then on
  chooses the duplicate. Reads before that read the original.
- **Lock follows the book's lock (decided with "book release" = the book is editable again).** The core locks the book **while the run
  works** and lets go of it when the run is paused, stopped and kept, or done (you may edit the book then; the copy catches up with
  your changes when the run resumes). The language does the same: **locked (read-only for everything but the run) while the run works,
  released when the book is released**, so a paused or kept run does not hold a language hostage for days, and the user can edit the
  language and the book alike while it waits. What follows is the same as for the book: the **sync on resume** brings the user's changes
  to the language into the duplicate (field by field, what both changed is a conflict that the review shows), and the **review** is the
  three-way (base = the language when the duplicate was made, the run's duplicate, the language now). While the run works, the
  **translation runs of other books** may read the language but cannot add words (they say so; refuse or read-only, to decide), and the
  editor shows *Locked by the run X of the agent Y*.
- **The difference.** One engine, `LanguageDiff(base, ours, theirs)` -> a **change set** of typed operations with field-level before / after
  (add / change / delete of a word, a sense, a category, a rules section, the order, a link), each with the identity that the pairs give.
  The same change set is what the **designer's proposals** are (a proposal is a diff against the current language), what **Import** makes
  against an existing language, what *Duplicate then Compare* gives the user (experiment on a copy, merge back: a feature of its own),
  and what the **agent review** shows. One review window (check boxes, editable text, conflict marks, grouped by category and section),
  one `apply` with the conflict check, as the Knowledge Tracker's `ChangeSet` / `ChangeApplier`.
- **Coupling with the book's merge.** The translated text of the copy uses the new words, so the language part of the review must go
  with the book part: applied together, and when the user takes a part but not a word that it uses, the review **warns** ("3 parts use
  words you did not take: ...", found by the lexicon check over the parts that the run changed; `MergePlan.Item` has `dependsOn`).

**Why it is the hard part**
- **Identity**: duplicate and original must keep the uuid map, or "changed" and "added" cannot be told apart; a word the agent re-adds
  under a new uuid is a conflict with the original's one, found by the word itself, not by the uuid.
- **Three-way merge of structured text**: a rules section edited by both is a text merge (paragraph level, else choose a side), senses
  are nested, order changes (a move of a category) are their own operation, a word deleted by one and changed by the other is a conflict.
- **Size**: a lexicon of thousands of words in a review (grouped by category, only what changed, virtualized).
- **Everything the core does to a copy must also happen to the duplicate:** the **checkpoints** (restore a checkpoint and the duplicate has
  to go back with it), the **sync on resume** (what the user changed in the language while the run was paused: a real three-way sync with
  the base moved forward only for what was taken over, as the core does for the book), **discard**, **failure**, **stop and keep**, and the
  **backup that the core takes before an apply**: the language needs one too (an export of the original, kept).
- Commands of the run must resolve **the language of the copy**, not of the book: the language commands need the run's scope.

**The hooks the core needs to give a plugin** (a registered `ExtensionHasAgentRuns`-style listener, each with a `@PublicAPI` /
event, to be designed in step 2; the list so far):
1. **Copy created** (*the book is cloned for an agent*): the run, the book, its copy and the pairs; for a plugin that copies its own data
   eagerly, or sets up lazy copying. Also **book cloned by the user** (a clone of the book, with a flag for the agent copy).
2. **Command scope with the run**: the commands of a plugin learn that they run in a run (the run, the copy book) and not on the book.
3. **Checkpoint saved / restored**: to snapshot and restore the plugin's own data with the copy's.
4. **Resume sync**: called when the run is synced with the user's changes (a plugin's data may sync, or report a conflict).
5. **Merge difference** (*merge plan*): the plugin returns **extra sections** for the review, in the model the core dialog draws (items
   with kind, change, label, fields with base / agent / now, conflict, editable, `dependsOn`), so the language is reviewed in the same window
   as the book and its parts, picked in the same selection.
6. **Merge apply**: called with the picked items of the plugin's section inside the apply (after the core's backup, in order: the plugin's
   data before the parts that use it); and a hook **before the backup** so a plugin can take its own.
7. **Book locked / book released by a run** (the run takes the lock on the book when it works and lets go of it on pause, stop, finish: the
   moment the book is **editable again**) and **run ended** (applied / discarded / failed): lock the language when the book is locked,
   release it when the book is released, delete the duplicate at the end.
8. **Cleanup**: a duplicate left behind by a run that no longer exists (a crash, a deleted agent) is deleted by the application's cleanup,
   so the plugin's data must be listed in what the cleanup knows.

**Not an agent hook, but found on the way: forced reader mode and the source.** With the reader mode *conlang only* forced on a published
book, the English source must **not be sent to the browser** (`data-source` in the page would give it away). The viewer must build the
page without the sources in that mode, and the *click to reveal* must not exist for it. A release / publish point of a book is where the
mode is fixed; to check what the core has.

**Questions for step 2 / open:** what the translation runs of other books do while the language is locked by a running run (wait, refuse,
or run read-only), whether the language lock is taken at the first write only (lazy, as above) or from the start of the run, and whether the
hooks are one listener interface or several events.

## The second test conlang: Aethel-Vael (`aethel-vael.md`, what it shows)

A different kind of language from Vrakht: **SOV with real morphology** (the plural suffix `-is` with a vowel drop, the genitive `-en`, the verb prefixes
`El-` past, `Va-` future, `Ki-` imperative, negation by the word `Moris` before the verb or adjective, a "because" clause that comes first). Written
as a **lore entry** ("LORE & LANGUAGE PROFILE": a description, rules in prose, tables, an instruction), about 22 KB (about 7 000 tokens), 290 lexicon lines for
251 distinct words, mostly in tables of three shapes (word / meaning, word / meaning / derivation, and verbs with four precomputed forms). Read as a
test of the design (the counts are from a parse of the file):

- **Productive morphology breaks the lexicon check.** Every token that is an inflected form (`thalis`, `elsari`, `aelisen`, `vadran`) is not in the
  lexicon, so "every token is known or reported" would flag most of a translation. The language needs an **affix list the plugin can read**: affix,
  prefix or suffix, function, the vowel rule ("a noun that ends in a vowel drops it before `-is`"); the check strips the affixes to find the stem. Free
  text rules stay what the model reads; the small structured table is for the plugin (the check, and the generator of candidate forms). The designer
  can propose it from the rules, and the *Review* can check that the table agrees with the examples. **Replace a word** in the story (a changed
  stem) must be **affix-aware** too: a whole-word replace of `sari` misses `elsari`, `vasari`, `sarith`.
- **Precomputed forms are derived data.** The verb tables list the root and its three tense forms, which the rule already gives (`El-` + root): the
  import should keep the **root only** and use the rule to **verify** the table; the table has one wrong row (`Run`: `Vaistas`, should be
  `Vaisthas`). A lexicon entry therefore has no form columns.
- **The derivation column is the rationale.** `Khorthal` = "Shield / Protection of stone" (`Khor` + `Thal`), `Sillor` = "Tree-arm" (`Silva` + `Loris`): a
  note and the parts. A lexicon entry has **components** (links to other entries) next to the rationale, so the editor can show **derived words**: a
  change of a root lists the words made from it (*Update derived words*, a designer action that proposes the new forms, a proposal like any other),
  and the *new* words the model reports have their parts linked at once.
- **Homophones again, about a dozen** (same form, different meanings; normal, see *Homophones* below): `Vaela` = "or" (a conjunction) *and* "mouth", `Sael` = "blood" *and* "that",
  `Istha` = "leg" and "move", `Isthas` = "run" and "fast", `Seli` = "sit" and "rub", `Kaelen` = "strange" and "inside", `Sarishara` = "medicine" and
  "friend", `Khorshara` = "basket" and "neighbor", `Thalvhan` = "pan" and "metal", `Vhanen` = "hot" and "red", `Vhanval` = "heat" and "fever"; the
  negation word `Moris` is the word for "shadow" on purpose; the genitive `Aelisen` is also "golden"; about 40 words are only listed twice with the
  same meaning. Same answer as for Vrakht: **senses**; the repeats are merged, the homophones are left alone.
- **Gaps the model has to fill for ordinary speech:** no **numbers**, no **prepositions** or postpositions (to, in, with, from), question words (`Vaelir`
  where, `Vaelisae` which, `Vaelisael` what) but no rule for a question and no who / when / why / how, no adverbs, no possessive pronouns ("my" is not
  `Ae` + `-en` said anywhere), no "if", no yes / no, no names rule, and `Sae` is "this" in the lexicon and "the" in the examples. Expect many *new*
  words from the first part; a good stress test of the reporting and of the review.
- **A sound palette in the instruction:** "if a word is missing, construct a phonetically similar placeholder ... (using sounds like `s, l, v, th, ae, sh,
  ph, r`)": a seed for the optional structured phonology and its word generator that needs no full inventory, a **list of sounds** is enough to start
  (the lexicon also uses `k`, `kh`, `d`, `n`, `m`, `p`, so the palette and the words disagree: a *Review* item).
- **It is written as a lore entry,** so today the **story model reads all of it** whenever the entry is active: the 7 000 tokens a turn that the split
  removes. **Import from a lorebook entry** is how this author comes in (the language option "a lorebook entry as the source"): the plugin reads the
  entry into a language, and says that the entry can be turned off for the story model (or cut down to a few sentences of description).
- **Register variants of one concept (the author's remark: the adult items are there to show slang variants).** The anatomy table is headed
  "Formal & Vulgar Variations" and has `Mela` (breast, formal / anatomical), `Melis` (soft synonym), `Lelya` (vulgar, colloquial); Vrakht has its own
  *Profanities* list and the tone prefixes (`Gna` polite, `Hoh` patronizing). This is the **opposite of a homophone: several forms, one meaning, each with
  a register**. A language therefore has **registers** and a lexicon entry has a **register** and a link to the entries that mean the same: see
  *Registers and synonyms* in the lexicon section. The words themselves are ordinary content for the model, but a model that **softens or refuses**
  crude speech would defeat the register: the trial needs a scene with a **crude speaker** and a **formal one** saying the same thing, to see the model
  pick the form by the voice.

### Homophones are normal (decided after the author's remark)

Real languages have homophones and polysemy (English *bank*, *bear*, *light*), and a conlang that has them is more natural, not wrong; the two test
languages have them on purpose or by economy (`den`, `fall`, `Moris`, `Aelisen`, `Vaela`...). So the plugin **does not treat the same form with
different meanings as a fault**:
- A lexicon entry has several **senses**; adding a word whose form exists adds a sense or a new entry with the same form, **without a warning**.
- Only a **repeat** (the same meaning twice, the same word listed in two places) is reported, as a fault to merge.
- *Review* and `conlang-check-lexicon` list the homophones as **information** (collapsed, off by default, "show homophones"), never as problems; the
  one useful note is a **function word that shares its form with a word of the same grammatical slot**, a hint only.
- The **model** is told in the request that a form may have several senses and must pick the one the context needs; the **checker** (the model
  one) must not flag a homophone. In the trial the checker's false problems did not come from homophones, but its `fix` and `gloss` do read
  the senses: the gloss of `den` is "think" or "because" by position.
- **Where it does matter: changing a word.** A change of one sense's form (`Sael` "blood" to something else) cannot be found by text, `Sael` "that"
  has the same letters. So the **replace of a word** shows its matches in the story as a **list the author ticks** (the core replace already plans
  before it applies: the plan lists the matches, each can be left out), grouped by the sense the model reads for it when that is asked
  (*Classify the matches*: a model request per match, optional), and an **entry knows its senses' forms separately**: a sense may have its own
  form while the others keep theirs (an entry becomes two).
- **The designer** may propose a homophone on purpose ("make the word for X the same as Y, as the language does for ...") and must not
  avoid one that the rules allow.

## Real-model trial of the request (2026-11-04)

Setup: the user's `gemma-4-31b-it-MM` (`openai/unsloth|gemma-4-31b-it-MM`) behind llama-server, **thinking on** (the default), `max_tokens` 24000,
through `tools/LocalLlm.java` (see the root `CLAUDE.md`). Files in `trial/`: the draft prompt `task.txt` (the instructions, then the whole
`vrakht.md` as the language: the system message is about **6 600 tokens**), four scenes `s1`-`s4`, the output schema `schema.json`, `run.sh` (one
request at a time) and `score.py` (parses the answers and checks them; `trial/out/` is not committed). Scenes: **s1** five speeches of two
Vrakht speakers (a question, a command, a threat, a number: "three dozen"), **s2** a captive who does not know Vrakht, a guard who speaks it,
and the commander who speaks to her "in the common tongue", **s3** a healer who needs words the lexicon does not have, **s4** narration with
indirect speech only. The request asks for JSON: `translations[{quote, speaker, language, text, choice}]` and `newWords[{word, gloss, category,
rationale}]`. 3 samples of s1 and s2, 2 of s3, 1 of s4 and 1 of s1 with the strict schema: **10 requests, one model, so signs, not rates.**

**Results**
- **Valid JSON 10 of 10** (the 9 free ones and the strict-schema one); **every `quote` found verbatim in the part**, 10 of 10. The strict
  `response_format` json_schema **works on this server and the model still thinks** (10.9k characters of reasoning), so it can be used.
- **Finding the speech, 10 of 10:** the parts have 5 (s1), 6 (s2), 5 (s3) and 0 (s4: indirect speech only) quotations; s1, s3 and s4 were
  exactly right in every run. In **s2 the thinking runs gave 7 entries, 3 of 3: they split the last quotation** ("Leave her alone, you
  fool. She is worth more alive.") **into its two sentences**, each with its own mood (`Bef`, `Da`) and a verbatim `quote`; the non-thinking run
  kept it as one. Both are usable, but the plugin must accept an entry that is part of a quotation (the passage is the `quote`, found in
  the text, not the whole quotation) and entries that **adjoin**. (My own first scoring wrongly called 7 right and the count I had first
  written, 4, wrong: I had counted the quotations by hand, badly; the script now counts them.)
- **Who speaks, 3 of 3 in s2:** the guard Vrakht, the captive none (twice), the commander's lines "in the common tongue" none (twice, the text
  overrides the default), his lines to the guard Vrakht (twice). No translation text for a "none" speech.
- **Number:** "three dozen" = `karg dutz` (24 + 12), 4 of 4.
- **Used the language well:** `gild-frou` (trade-woman) for a female merchant, `hoh-kop` for "you fool" (from the profanity list), `ek-rokh`
  for "my lord", `liv-lig mar-gut` for "worth more alive", the tone prefixes (`Gna` for the soldier to his commander, `Droh` for the threat,
  `Hoh` for the commander to the guard), the question and command moods.
- **The lexicon check (every token known or reported) flagged nothing in 10 runs:** every word that was not in the lexicon was reported in
  `newWords` with a category and a rationale (the model never invented silently). The check counts a compound of known parts as known, so it
  cannot see a **wrong meaning** (see below).

**Problems found**
- **The same concept got a different new word in each sample** (as the lexicon does not carry it between runs): north `seit-ob` / `nord` /
  `nord-seit`, wait `ne-vakt` (the guard word, "not guard") / `vait` / `halt`, the pass `veg` / `stein-veg`, morning `dag-komm` / `neu-dag`.
  This is what the lexicon is for: a reported word enters the lexicon at once and the next request sees it. **To test next:** the same
  scene with the new words of the first run in the lexicon, do they come back?
- **A word of the lexicon stretched to a wrong meaning:** `vakt` (guard, watch) as "wait" in 2 of 4 samples, found only by reading. A
  rationale does not say it, since no word was invented. For the review of *new*, not for the checker.
- **The tone prefixes have no slot in the blueprint** (rule gap, as found by reading the file) and the model **fills the gap differently
  each time:** `Gna` alone (no mood particle) in 3 samples, `Gna Da ...`, `Droh Da ...`, `Hoh Bef ...` in others, and mid-speech `ab Droh
  ek ve-hab ...`. A language **check** "starts with a mood particle, optionally behind a tone prefix" would flag the first and the designer's
  *Review* should have found the gap before the model had to guess.
- **Starting with a linker:** `den ...` / `Den Bef ...` instead of a mood particle in 3 of 10 (rule 1 says the mood particle is mandatory
  first); a speech begins a new "sentence" with nothing to link to. Same check.
- **Grammar slips:** a role particle without its object (`gold lok, gild-frou`: `lok` dangling) in 1 of 3 samples of s2; **gender**
  `gild-man` for a woman in 1 of 3 (the other two `gild-frou`): the model knows the rule and misses it sometimes, a review item.
- **Cost:** a request with speech takes **2 to 4 minutes** (prompt 6.6k tokens, 2.2k to 4.7k tokens of output, mostly thinking: 5k to 12k characters
  of reasoning, about 18 to 19 tokens a second); a part without speech 16 seconds. In the background after a part this is fine; a **range**
  (a whole book) is hours. See the non-thinking test below.
- **A request does one part:** the 6.6k prompt is read again for every part; the prefix (rules, lexicon) is the same every time, so with the
  server's prompt cache only the first is slow (the first request here took 13 tokens a second, the rest 18-19).

**Non-thinking mode** (`--extra '"chat_template_kwargs":{"enable_thinking":false}'`, which works: `reasoning_chars=0`), one sample each of s1,
s2, s3: **26 to 44 seconds instead of 120 to 250, 450 to 700 tokens instead of 2.2k to 4.7k (about five times faster)**; valid JSON,
verbatim quotes and the finding of the speech as before (s2 as 6 entries); **who speaks again right** in s2. **But the translation is worse:**
- s1: **"three dozen" came out as `drei-dutz`** (three-twelve, 36 in no system the file defines) instead of `karg dutz`, 0 of 1 (the thinking
  runs 4 of 4); **`za` after a role particle twice** (`ge za veg-dor`, `lok za sulk`), which rule 7 forbids in capitals (thinking runs: never);
  and **`nord` used without being reported**, the one **silent invention** of all the runs, which the lexicon check caught.
- s2: `gild-man` for a woman again; s3 acceptable (`stoff` reported, odd compounds: `holt-stoff` "wood-material" for cloth).
So non-thinking is **a fast draft, not a final translation**; the failures were exactly what the checks find (`za` after a role particle, an
unreported word); the wrong number and the gender are not found by a check.

**Two-pass check** (a second request, the checker, reads the translations against the language and the story: a gloss word by word from the
lexicon only, a back-translation, `meaning` same / close / different, `ruleViolations`, `problems`, a `fix`; prompt `trial/verify_task.txt`, run
with `trial/verify.sh`). Tried on 6 outputs that have known errors (the non-thinking ones and two of the thinking ones), the checker without
thinking on all of them and with thinking on two:
- **Non-thinking checker (25 to 48 s a request):** it **finds real rule violations, some that the trial's own script did not**: `za` after a role
  particle (2 of 2), the dangling `lok`, an adverb after its verb (rule 3), an object behind `und` without a role particle, `za` on a subject
  (rule 4); it noticed `vakt` for "wait" ("a stretch, acceptable"). It **missed** the wrong number `drei-dutz` (0 of 1), the wrong gender
  `gild-man` for a woman (**0 of 5** over all the checkers, also with an instruction that asks for it) and the unreported `nord`. **Noisy:** about
  60 % of the entries are marked `close` for fine translations (a "Report" that became "Speak!"), and it gave false problems (`za` before an
  ordinary direct object; `kehr` read as `ke`); its `fix` sometimes kept the error. Only `different`, `ruleViolations` and `problems` are worth
  looking at, never `close`.
- **Thinking checker (114 to 237 s):** on the output with the wrong number it found the **number** (36 is `karg dutz`), **`nord`**, and **both
  `za` violations with no false `za`**; on the output with the wrong gender it passed both entries as `same`. So thinking finds the number and
  the rules and still not the gender.
- **Gender is the translator's job:** with `(woman)` written after Mara in the character list the non-thinking translator still wrote `gild-man`
  3 of 3; the thinking translator wrote `gild-frou` 2 of 3 with no hint. So gender needs the thinking translator, and the checker is no help.
- **`nord` (an unreported word) is found by the free lexicon check**, no model needed; the number could get a check from the number rules of the
  language.

**What the two-pass check is for (proposal):**
1. **Free checks first, always:** quote verbatim, mood particle first (behind an optional tone prefix), the lexicon check, and the language's own
   checks (`za` after a role particle, a role particle without a noun, numbers by the number rules). They are the repair loop's trigger.
2. **The model checker is an action, not a stage of every translation:** *Review translation* (a part, a range, `conlang-check-translation` for
   agents), with thinking on (4 minutes), whose answer is **advice for the author** (the issues, never `close`, with the suggested fix), not an
   automatic rewrite. It is the only way found so far to see a wrong number or a stretched word without reading, and it is wrong often
   enough that it must not change the text by itself.
3. **The translator thinks by default** (*always think*, in the background): it is what gets the number (4 of 4) and mostly the gender right.
   *Draft then repair* (non-thinking, then the failed free checks sent again with thinking) is for a range, an estimate or a quick preview and
   does not fix what no check sees (gender, number without a check).

**What this changes in the plan**
- The prompt works as a **first version of the request** (step 4) with a JSON answer; keep the strict schema as an option (a setting).
- The checker (step 4) checks, per speech: the quote verbatim, the language known or `none`, no text for `none`, **the first token a mood
  particle (behind an optional tone prefix)**, the lexicon check, and, from the language's own list of checks, `za` after a role
  particle, a role particle without a noun; the **words that are new** are reported, never dropped.
- **Draft fast, check, retry the failures with thinking** (an idea from this trial; the two-pass trial above limits it: it repairs only what a check sees): the request first without thinking (30 seconds),
  the checker runs on every speech, and only the speeches that **fail a check** (or the whole part when several do) are sent again with
  thinking on, together with the failed checks ("`za` must not follow `ge`", "`nord` is not in the lexicon: report it"). A setting chooses
  *always think* (best, slow), *draft then repair* (default?) or *never think* (fastest). The checks that cannot be written (numbers, gender)
  are the reason to keep *always think* for a book that matters; the numbers could get a check of their own for a language with a number
  system (a language check: "numbers by the number rules").
- A trial of the **second run** with the lexicon updated is the next test (consistency of words); another with a **bigger part** (20
  speeches) for the time; and the designer's *Review* on `vrakht.md`, which should find the tone-prefix gap and `-lig`.

## Real-model trial 2: Aethel-Vael (2026-11-04)

Same model (`gemma-4-31b-it-MM`), thinking on, `max_tokens` 24000. Prompt `trial/task_ae.txt` (the Vrakht one with two additions: *choose the
form of a word by the register that the speaker's voice asks for*, and the character list carries **gender and voice**; the system message is about
**8 900 tokens**), scenes `a1`-`a4`, `trial/score_ae.py` (with an **affix-aware lexicon check**). **a1** grammar (SOV, the plural, the genitive, the tenses,
`Moris` negation, a "because" clause, a gap: "yesterday"), **a2** a human who does not know the language and a speech "in the common tongue", **a3**
the same concept said by a formal healer and a crude mercenary (breast, fever, wet, skin), **a4** the gaps (where, numbers, "the third house", thanks,
sorry, please, yes). 8 requests: a1 x2, a2 x1, a3 x3, a4 x2. **One model, a few samples: signs, not rates.**

**Results**
- **Valid JSON 8 of 8, every `quote` verbatim, every speech found** (7, 4, 4, 4 per scene). **Who speaks right** in a2 (the human's lines and the elder's "in the
  common tongue" none, the elves' lines translated).
- **Register by voice, 3 of 3:** the formal healer said `mela` (breast, formal) and the crude mercenary `Lelya` / `Lelyis` (vulgar), every time. The
  design (a register on an entry, variants of one concept, the character's voice in the request) works with this model.
- **The grammar was followed:** SOV with the verb last, adjectives after the noun, the plural `-is` with the vowel drop done right (`Mellitis`, `Lelyis`,
  `silvis`), the genitive `-en` (`Aelmoren valen silva vasari`), `El-` / `Va-` / `Ki-`, `Moris` before the verb, the "because" clause first
  (`Moris kael aelva ae aelisis moris viri`); and the lacking "my" was made from the language's own pieces, `Aeen` (`Ae` + `-en`).
- **The affix-aware lexicon check works:** `thalis`, `elsari`, `aelisis`, `vadran`, `Lelyis` were all known, no false alarm. Only two flags in 8 runs, both
  informative: `vaelissaren` (a **rule gap**: the rules say a vowel drops before `-is` but not before the genitive `-en`; the model dropped it,
  the check did not know), and `elaes` (a **prefix on a word the translation had reported as new**: the check must let reported words take affixes
  too, now done in the script).
- **Every new word was reported** with a category, a register and a rationale (4 or 5 in a1 and a4, 1 or 2 in a3); no silent invention. The social
  words of the lexicon were used (`Shalari`, `Aelpha`, `Morsha`, `Vaelir`).

**Problems found**
- **Different new words per sample again:** "arrow" `thaldran` / `Sillithal` / `Silvthal`; "yesterday" `aelisth-mor` / `elvalen`, "tomorrow" `aelisth-val` /
  `vavalen` (a **tense prefix put on a noun**, which the rules do not allow, but a clever way out); the numbers `aenlis`, `valenlis` in one sample and
  `duo`, `tri`, `trisen` in the other; the healer `saelis-sari` / `sari-vaelissari`. Only "yes" (`ael`) was the same twice. As in Vrakht: the lexicon must
  carry the words from one request to the next, and **the numbers need a number system in the language**, not an invention each time.
- **Outside the sound palette:** `duo`, `tri` use `d` and `t` and look Latin; the instruction says the sounds are `s, l, v, th, ae, sh, ph, r`. A
  **structured palette** (a list of sounds) would let a check flag them, and the generator offer words that fit.
- **A rule contradicted:** in one a4 sample the model **invented a copula** `aes` ("to be") "to allow sentence formation", although the language has none
  (its examples are `Sae silva moris aeth`); in the other it wrote `elthali` ("stood") for "was". No check sees it, the rules are prose.
- **Stretches of meaning again:** `elphari` ("touched") for an arrow that "struck", `sarishara` (friend / medicine) for "help", `lyrkhornis` (great water
  bowl) for "bucket" (a compound of known parts, not reported), and **"to the third house" lost its "to"** (the language has no preposition: the model dropped it).
- **Cost:** 124 to 298 seconds a request (2.3k to 5.2k output tokens, 5k to 12k characters of reasoning); the 8.9k-token system message is read
  each time.

**What this changes in the plan**
- The **affix list** (see *The second test conlang*) is confirmed: with it the lexicon check works for a morphological language, **and a reported word must
  be allowed affixes**.
- The **register** design stands as it is; the next test is a *formal* line spoken by the crude character and the reverse, and a character with **no voice
  note**, to see the default.
- The **review of the language** should find: the rule gap of the genitive (does `-en` drop the vowel?), the missing numbers and prepositions, the
  palette that the words break, and the absence of a copula that the model then invents.
- A **language check for "no word outside the palette"** needs the structured palette; the optional structured phonology of the designer gets its
  first use case.

## Real-model trial 3: the wording of step 4 (2026-11-04)

Same model (`gemma-4-31b-it-MM`), thinking on, `max_tokens` 24000. The **system message is made by `TranslationPrompt`** (the new instructions: several entries for one quotation allowed, `understood`, `register`,
`kind`, `variantOf`, "do not stretch a word") with `vrakht.md` as **one rule section** and an empty lexicon (the file mixes grammar and lexicon; the import of a language is the designer's, step 8), so this
tries the wording and the fields, **not the grouped lexicon**. The user messages are the old ones of `trial/s1..s3.txt` (their own CHARACTERS list: the notes of the language, gender and voice, were not
exercised). Four requests: s1, s2, s3 and s2 with the strict `json_schema`. **A few samples of one model: signs, not rates.** Scored with `score.py` (the checks of the first trial).
- **Valid JSON 4 of 4 and every `quote` verbatim in the part (24 of 24)**; the schema run is as good as the free ones. 208 to 242 seconds a request (prompt 6.8k tokens, 3.9k to 4.5k tokens of output, 8k to 12k
  characters of reasoning), as before: the new fields cost nothing noticeable.
- **The splitting is now allowed and used:** s1 gave 7 entries for 5 quotations (the two-sentence ones split), s2 7 for 6; every entry has its own `quote` found in the part, none overlaps, so
  `AnswerChecker` takes them all.
- **Who speaks right again:** the captive and the commander's "common tongue" lines `none` with an empty text (4 of 4 in both s2 runs), the others Vrakht.
- **`understood` works:** in both s2 runs the merchant's question to a woman who does not know Vrakht came back `understood: false` and no other entry did.
- **`register` was used for what Vrakht has instead of registers:** the language has none defined (the field says "see REGISTERS"), and the model put the **attitude prefix** in it (`Gna`, `Droh`, `hoh`, `Fall`)
  rather than leaving it empty: a sign that the register of a language with tone prefixes (Vrakht) wants them as registers, which the designer's *Review* can propose. An unknown register name must not break
  anything: the plugin keeps it as text (`Span.register`).
- **"Three dozen" came out `karg dutz` (24 + 12), right** (s1); `gild-man` for a woman again in the free s2 run, **`gild-frou` in the schema run** (1 of 2): the gender needs the notes of the speakers (the new
  CHARACTERS list) and the thinking translator, as found before; not tried here.
- **New words:** all reported, with a rationale; `kind` filled (`compound` / `root`), `variantOf` empty. **`vakt` was reported as a new word "to wait"** although `vakt` (guard, wall) is in the file: the model
  used the "do not stretch" rule and invented a sense; `AnswerChecker` adds it as a homophone, which is the design (the author sees it in the *new* list), and the stretch is visible there.
- **Still there (checks that are the language's own, not done):** a speech that starts with a tone prefix and no mood particle (`Gna Da ...` is fine, `Fall Da`, `Droh Bef` too: the first word is a prefix, the
  mood comes after), `Den Bef ...` (a linker first, s3). The first-token check of the first trial flags them; `AnswerChecker` has no such check on purpose.

## Real-model trial 4: the designer (2026-11-04)

Local model (see `tools/LocalLlm.java --list`; the AI is the user's, not a fact of the application), one sample per request, 30000 tokens of budget, the requests written by `DesignerPrompts` for the test Vrakht (the whole `vrakht.md` as one rules section and 17 words) in `trial/design/` (`DesignGen.java` writes the requests, `run.sh` sends them; the requests and answers are not committed).
- **All eight answers were the JSON object** (in a code fence; the lenient reader takes it), 8 of 8 parsed. Time and size: *words* 134 s (prompt 6565, completion 2308), *section* 204 s, *review* 166 s, *ask* 104 s, *samples* 113 s, *stage sounds* 75 s, *stage grammar* 99 s; **rewrite 439 s** (prompt 12435, completion 7560): the model gives back the **whole section**, and the test section is the 17856 characters of the file. A rewrite of a section of a normal size is small; one big section is slow and expensive, which is a reason to keep the sections of a language short (the editor says nothing about it yet: see *Open items*).
- **Words** (8 for *Things*, note "kitchen of a siege camp"): sensible, in the style (`kurt` cleaver, `sal-stein` salt, `brand-ort` hearth), but the rationale of a new root was **"New root"** in 5 of 8: the prompt now asks for the sounds chosen and why, or the roots and the rule (**not tried again**).
- **Section** (a numbering system, base 12): the model **replaced** the file's base-24 section (its `replaces` named a title that is not a section of the language, because the file is one section, so the builder made a new section) and gave 15 digit words that look German (`ein`, `drei`): the note asked for something that contradicts the rules and the model chose silently; *message* said so.
- **Review**: found real contradictions in the file's final lexicon block (`es` = "is" against the zero-copula rule, `ek`/`ja` as "you"/"I" against the pronoun section) and the missing rule for the positions; 5 findings with a section and a fix each. The most useful action.
- **Ask**: "which words are missing for *my brother died at the gate*": answered from the lexicon (`ek-`, `man-sib`, `lok`), no invention. **Samples**: three sentences translated with the grammar and two new words proposed (`lin`, `dag-stigg`) that the proposal then flags as new.
- **Stages** (a new language *Skarn* from a brief): sounds and grammar each one section that follows the brief (SOV, cases, `kh`, stress on the first syllable) but **short** (443 and 581 characters): the prompt now asks for 200 to 500 words with examples (**not tried again**). The sound section used `q` although the brief said nothing English readers cannot say; the author edits.

## Step 5 check in the real app (2026-11-04)

The screenshot environment (`13-db-backup`, the V23 database migrated), the plugin JAR copied in, a language and the book's settings written into the data folder with a small script (no UI yet), the
fake model answering from its queue; Playwright through the real UI.
- **Translate the speech** on part 2: the card shows the Vrakht ("Frag ek es hold-gild lok mun, nu Da rota-hab?"), the narration, the quotation marks and the speech "in the common tongue" stay;
  the part is **not flagged edited**, its tokens stay 315 (the English count), the history has a version `AUTOMATIC / TRANSLATION`; two passages (`kind PASSAGE`, `conlang-translator.speech`) with
  the source, the speaker, the hash; `rota` is in the lexicon, **new**, origin model, with its rationale and the category `Things`; after a reload the story has two `mark.conlang-passage`.
- **The model reads English:** *Annotate with AI* on that part sent "How touching. Does the Boy Who Lived..." and neither `hold-gild` nor `Frag ek` (through `StoryAround` and the registry).
- **Automatic:** with `auto` on, a generated part was translated by itself right after the generation and the story showed it (and its mark) without a reload; a **regenerate** gave the new English
  text and soft deleted the passage.
- **A bug the check found:** the story was loaded again in `ui.access`, a thread with no session, and the services failed with `ScopeNotActiveException` ("Scope 'session' is not active"). The callback of
  `TranslationRuns` now runs in the captured request scope.
- **A gotcha of the fake model:** `llm('/_reset')` of `lib/shot.mjs` sends a GET without a body and is answered "unsupported GET /_reset": the queue was never cleared, and the stale replies of the earlier runs
  answered the wrong requests. Call it as `llm('/_reset', {})`.
- **Not checked / left:** the click on a mark (step 9), the phone story, the repair run for the words nobody reported (`needsRepair` is only reported), a part with several speakers' tone prefixes, concurrent runs.

## Step 6 check in the real app (2026-11-04)

Screenshot environment, plugin copied in, no seeded data: everything below was done through the UI.
- The panel opens in *Extension Settings*; a new language gets its name, rules, two categories, a register, two words (one with a sense and a rationale) and a speaker; *Save* of the editor
  then *Save* of the Settings screen; after a reload the library, the word count and the chosen default language are there. The workspace shows *Unsaved changes* while a language is edited and
  not saved, as for any field.
- *Duplicate* (`Vrakht 2`, the words come along), *Export* (the document has the lexicon and no links), *Import* of that document (named `Vrakht 2 2`: a name that is taken gets a number), a
  bad document is refused and the window stays, *Delete* with the question.
- The book dialog (Prompts) and the folder dialog show the four fields under *Extension Settings*; the language field shows the one inherited from the user, and a part translated through
  the menu used that inherited language (nothing was set on the book). The lookup in the story finds `drak` by its meaning `war`.
- Layouts looked at in the screenshots (1920 wide and 900 x 700): the panel, each tab, the word window (it scrolls inside, the buttons stay), the maximized editor, the lookup. Found and fixed:
  the icon of the search field was huge (a Font Awesome icon in a prefix slot: use the Vaadin icon), the meaning column repeated the word, the counts of the category tab did not follow the
  words added in the lexicon tab, the checkbox of the defaults sat beside a helper text.
- A thing that looks like a bug and is not: after *Import* the new language is selected, a click on its row selects it away (Vaadin's toggle), so a script that clicks first finds *Delete* disabled.

## Step 7 check in the real app (2026-11-04)

Screenshot environment, everything through the UI. A language was duplicated, the copy got a new word, a changed rationale, a changed rules section and a deleted word; *Compare* of the original
with the copy listed four changes (the delete not picked), *Apply 3 picked* wrote them and the library showed three words. An *Import* of a document of the same name whose words had other ids
asked *Review the differences / Import as new*, flagged the two words that were here already (*The same word with a shared meaning is here already*) as conflicts, *Select without conflicts*
picked only the new word, and applying it gave three words. Found and fixed: a comparison offered the **name** of the copy as a change (it renamed the original to `Vrakht 2`; the name is not
compared now), *Select without conflicts* picked the deletes of the old words while their replacements stayed unpicked (a comparison with no base never picks a delete now), the *About*
column was cut, and an add showed three boxes per field (an add is now one editable box per field, a kind or a status is read-only and in words).

## Step 9 check in the real app (2026-11-04)

Screenshot environment (`13-db-backup` migrated by one start of the app), the new plugin JAR copied in, **no language**: two passages of the kind written straight into `annotations` (the conlang text put into parts 2 and 3, the English in the `Span`) and the book set to
published; the reader mode set in the book's `inheritedSettings` (the book dialog's field was only looked at); headless Chrome through Playwright, desktop and phone (the scripts are in the scratchpad of the session, not in the repository).
- **The viewer** (`/view/<uuid>`, it has its own login): *click*: one `mark.conlang-passage` with `data-conlang-passage`, the conlang in the text and **none of the English in the page** (innerText and HTML searched); a click (a tap on the phone) opens the popover under the mark with *Said by Draconia*
  and the English. *Conlang only*: no mark, the conlang, no English. *English only*: the English, no conlang. No error or `Mod ... skipped` line in the application log.
- **The author's story card** (`openBook`): one mark in the active branch, the click opens the same popover. **The book dialog** (Prompts, *Extension Settings*) shows *Readers of the published book see* with *The conlang, the English on a click* and its help line.
- **Not covered:** the second passage (*not understood*, on part 3) is on a swipe alternative that is not on the active branch, so it was never shown; the lazy loading of many parts; the dialog's choices *conlang only* / *English only* were set in the database, not through the field; a reader that is not the owner (only `admin` was used).

## Step 10 check in the real app (2026-11-04)

The same environment as the step 9 check, the WAR and the JAR built from the working tree; the passages seeded in the database (one speech on the active branch); Playwright, the dialog of *Export story*.
- The dialog has the third select *Translated speech*; Markdown with each of the four choices: *as it is* has the conlang and no note; *the English only* has the English in the quotes; *conlang, English in footnotes* has `"Frag ek ..."[^1]` and `[^1]: Draconia: "How touching ..."` after the part; the last is the same the other way round.
- HTML has the linked number and the `<section class="footnotes">` with `data-footnotes=""` (the XML-safe form); the EPUB was exported (and, in the unit test, parses as XML); DOCX has `"...rota-hab?"[1]` and `[1] Draconia: "How touching ..."` as the next paragraph; the PDF has them too (`pdftotext`).
- **Not seen:** the field is **absent** for a book without translated speech (only the code does that); the AO3 export in the app; a book where the speech passes through a part that has images between (the images of the book here were all at the end).

## Step 11 check in the real app (2026-11-04)

The same environment; the language *Vrakht* (a stub: id and name) written into the user settings and chosen by the book, the fake model answering one queued JSON for the translation. Playwright through the real UI.
- The dialog counts *5 parts in the range, 4 to do* for the parts that are not translated and *1 to do* for the translated one; **Put the English back** over the book asks, gives the English back in the part, removes the passage (the mark is gone), the history has a version `AUTOMATIC / #TRANSLATION`, the part is
  **not flagged as edited**, the story is drawn again. **Translate the parts that are not translated** over part 3 sent one request and wrote the queued translation: the conlang is in the story with its mark. A second revert then found that part (**found by the dialog reading the parts again: a first version kept the
  flags from the time it opened and said *0 to do***), and *Nothing to do in the range* is shown for an empty choice.
- **Not seen:** a failure of a part, **Stop** during a run (only the loop is unit-tested), a range of many parts (the model was one queued answer), the phone.

## Open items found while building steps 5-8 (2026-11-04)

Things that were seen and not done, which the step lists above do not carry. Each is small; none blocks the plan.
- **Prompts changed and not tried again:** the *new root* rationale (words) and the length of a stage section (wizard) were changed after the trial; run `trial/design/run.sh` again and write the result here.
- **The end-to-end checks are only partly in the repository** (`flows/plugin-conlang.mjs` takes the pictures of the panel, editor, review, designer, book fields, story click, range window, export and viewer; the runs with the fake model of steps 5 and 11, the automatic run, the viewer's three modes and the exports of every format were driven by scripts of the session, not kept). The scripts that drove the real app (Playwright, the fake model's queue: step 5 translation and automatic run, steps 6-8 settings, editor, review, wizard, designer actions, desktop and phone) live in the scratchpad of the session. They should become flows of `screenshots/flows/` (as `plugin-*.mjs`), which also gives the screenshots of step 12. The UI classes (`LanguageSettingsForm`, `LanguageEditorDialog`, `LexiconTab`, `ChangeReviewDialog`, `DesignerWizard`...) and `LanguageLibrary` / `ConlangInherited` / `DesignerService` have **no unit tests** (the model, store, translate, diff and designer packages have 140).
- **The phone, only partly satisfying:** the panel, the editor, the review and the wizard work on a phone, but the lexicon grid shows two or three columns and scrolls sideways, the *Meanings* list of the word window is below the fold, the *Generate words* button of the wizard's lexicon step is below the fold, and the library grid of the panel was not looked at with several languages. The story side on a phone (the *Look up a word* tool, the part menu) was not tried, nor the phone *Manuscript* dialog's fields for a folder or a book (they are the same classes as the desktop's).
- **Translations (ja / zh):** all texts of the plugin have them, written by the model, not read by a native speaker; the stage names, the register of the designer's texts and the legal-sounding ones (conflicts) most of all.
- **The editor says nothing about the size of a section:** a rewrite gives back the whole section (7 minutes for 17856 characters on the local model), so a hint at the rules tab ("keep sections short: one topic each") and a warning on *Rewrite* above a size.
- **Dialogs lose work on Cancel without a question** (the editor, the wizard asks only when it has something): the editor's *Cancel* discards its copy at once.
- **A language deleted or renamed while a book names it:** the book's field shows the id's language as missing (the combo shows nothing); the editor should say which books use the language before a delete (the library does not know the books, the plugin would have to search the attributes of all books).
- **Known, not mine:** `PhoneManuscriptPartFoldersTest` (2 failures) fails on a clean HEAD too, and the `sillytavern-import` step of the screenshot flow `shoot-features` fails on the *comments* button; neither was looked into.
- **Not retested in the real app after the core change of step 2:** the Knowledge Tracker, Reviewer and the other plugins that now read the text through `partTexts.forModel` have their unit tests passing, but were not run with a translated book; the translation of a part that the Knowledge Tracker reads is the one test that matters.

## Step 2 findings (2026-11-04, from reading the core)

**Who reads the text of a part** (`getResponse()` and `::getResponse`, 30 files of the core outside `ui`, 14 in `ui`, 2 exporters, 12 plugin files):
- **Gives it to a model (-> the accessor, the source):** `PrepareContentStep` (the story of the prompt, 3 places), `GenerationStepBase` (`storyMessages`, and
  `currentMessage` of a regenerate), `LorebookActivationServiceImpl` (the scan for keywords), `StoryAround` (the story around a part, for the annotate and
  redraft prompts; also `countTokens` of a part), `AnnotateServiceImpl` (the text and **the quotes the model answers with**), `RedraftServiceImpl` (the text and
  **the selection**), `SummaryServiceImpl` (**line 611 the input of the summarizer and 746 the story of its prompt**), the agent read commands (`GetPartCommand`,
  `ListPartsCommand`, `ListSwipesCommand`, `ListBranchesCommand`, `ComparePartsCommand`, `CountTokensCommand`), and in the plugins arc-outline, beta-readers,
  book-description-generator, image-generator (service), knowledge-tracker (`ExtractionService`), manga-generator, reviewer, sidequery.
- **Stays the stored text (the conlang):** `SummaryService.BlockTexts.CURRENT` and `SummaryServiceImpl:559` and **`:609`** (the hash of the block: the
  summarizer's input and its hash are made on the same line today, `:609` and `:611` must be split), the revisions (`MessageRevisionServiceImpl`,
  `RevisionData`), `ChatMessageServiceImpl` (the edit, the merge), `ManuscriptReplaceServiceImpl`, `AnnotationServiceImpl` (the offsets), the agent copy and
  merge, `JpaChatMessageRepository` (the search), the exporters (their own hook later), the UI of the cards, the history dialog, the viewer, `chaptermarker`,
  `ImageGeneratorDialog`, the comparison of the knowledge tracker (`text.equals(original.getResponse())`).
- **Token counts:** `ChatMessage.getTokenCount()` is made at generation (English) and is what the context budgeting uses (`StoryAround:203`); a recount after an
  edit (`ChatMessageServiceImpl`, `ManuscriptReplaceServiceImpl`) must count the **source**, as that is what the model is sent. Word counts stay on the stored text.

**The accessor must give an offset map, not only a string.** Two features speak to a model in the offsets of the **stored** text: **annotate** (the model answers
with quotes, and `createFromQuote` looks them up in the stored text, where the passages inside a translated speech are different words) and **redraft of a
selection** (the selection is a range of the stored text). So `PartTextService.modelText(part)` returns a `ModelText`: the source `text()`, the stored
`stored()`, and `storedRange(sourceStart, sourceEnd)` / `sourceRange(storedStart, storedEnd)` (a range that touches a substituted passage grows to the whole
passage on the other side); `QuoteFinder` falls back to it when a quote of the model is not found in the stored text. A transformer returns **substitutions**
(`start`, `end` of the stored text and the replacement), all in stored coordinates, merged by the service (an overlap: the first registered wins); a
`prepare(parts)` hook lets it load its data for a whole branch at once (the story of a prompt is hundreds of parts, one query per part would be too slow). With no
transformer registered the accessor returns the stored text, so switching the callers changes nothing for the application as it is.

**The edit path** (`ManuscriptReplaceServiceImpl.apply` is the model): `messageRevisionService.ensureCurrent(message)`; set the text; counts; `setEdited`;
`chatMessageService.save`; `messageRevisionService.record(message, RevisionOrigin.automatic(RevisionDetail.REPLACE, false).data(message))`;
`annotationService.shift(owned, message, before, after)`; the summaries' hashes from its plan (`SummaryService.collectFirstBlock` with a `BlockTexts` that
holds the new text of the part). A new method **`ChatMessageService.replaceText(manuscript, part, newText, origin, options)`** does all of that for a
plugin, with these decisions: a new `RevisionDetail.TRANSLATION`; **`edited` is not set** (a translation is not the author's edit: `edited` and
`originalResponse` drive the highlight of what the author changed); **`originalResponse` gets the same substitutions** where they are found (best effort), so
the highlight of generated text still works on a translated part; the **summaries that were valid before the change are rehashed** (a translation never
changes the meaning), the outdated ones stay outdated; the agent lock is checked (`agentRunService.checkNotLocked`).

**The build:** this machine has JDK 26 (the project says 25), Maven 3.10 and a 25 GB local repository; the IDE is open, so builds go to `-Dbuild.dir=/tmp/marginalia-build`.

**The order of the work in step 2:** 2a the accessor, its transformer registration, its callers in the core, the guard test (a list of the files that read the stored
text; a new reader fails the test) and the split of `SummaryServiceImpl:609/611`; 2b `replaceText` with its tests; 2c the annotation base class, V26 and the kind
registry; 2d the plugin callers. Each part has unit tests, and the application behaves as before until a plugin registers something.

**Step 2 status (2026-11-04):**
- **2a done** (the accessor and its callers): `PartTextRegistry` + `ModelText` (the offset map), the callers of the core switched (generation story and
  `{{current-message}}`, `PrepareContentStep` for the whole branch at once, the lorebook scan, `StoryAround` for the annotate, redraft and impersonate
  prompts, the summarizer's input with its hash left on the stored text, annotate's quotes mapped back to stored offsets, `RedraftService.widen` for a
  selection), the guard test (`StoredTextReadersTest`, `src/test/resources/stored-text-readers.txt`). **The agent read and write commands stay on the stored
  text** (they work on it together: an agent reads a part and then replaces words in it) until step 13.
- **2b done** (the edit path): `ChatMessageService.replaceText` (no `edited`, tokens of the model's text, `originalResponse`, the valid summaries rehashed,
  `RevisionDetail.TRANSLATION` with its label in EN / JP / ZH). A regression the first test run found and fixed: the two-stage annotate prompt had
  printed a `ModelText` object instead of the text.
- Tests: 238 in the classes touched, all pass (`ModelTextTest`, `PartTextRegistryTest`, `StoredTextReadersTest`, `PartTextGenerationTest`, `ReplaceTextTest`,
  `AnnotateServiceTest` with two new tests, and the existing tests of redraft, impersonate, the lorebook, summaries, tokens, history, `PublicAPISignatureTest`,
  `BundleCompletenessTest`). Docs: `manual/developer/plugins/part-text.md` (to be stamped after the commit), `CHANGELOG.MD`.
- **2c done** (2026-11-04, the annotation base class): `AnchoredPassage` (the anchoring: part, range, quote, context, `orphan()` / `unorphan()`, `copy()`)
  is the base of `Annotation`, SINGLE_TABLE on `annotations`, discriminator `kind` (`EDITORIAL` / `PASSAGE`) and a column `passageKind`; V26 rebuilds the table
  (`status` / `source` nullable, tested on a V25 database in `FlywayMigrationTest`). **A plugin's kind is the string `passageKind`, not a Java class**
  (a discriminator value must name a class; `"not null"` cannot be inserted). `PassageService` (create / createFromQuote / list / reattach / remove and the anchoring for
  every kind: shift, relocate, moveToPrevious, copyForBranch, deletes, backup with `kind` / `passageKind`); `AnnotationService` delegates its anchoring to it. The
  kind registry is `PassageKinds` / `PassageKind` (`markClass`, `markAttributes`, `onClick`, `shownInViewer`, `shownInExports`); `StoryAnnotations` draws the marks of
  the registered kinds in the cards (desktop, phone, redraft dialog) and `CardAnnotations` routes the click. **Scope that was cut from the plan:** the flags
  "counted in the badge / tab / margin" are not in the registry: those lists read `Annotation`s only, so a passage is never in them (a kind that wants a list makes its own).
  The trash and the cleanup name the root class (`EntityKey(AnchoredPassage.class, id)`). Docs: `manual/developer/plugins/anchored-passages.md`, changelog.
  Tests: `PassageServiceTest`, `StoryAnnotationsTest`, `AnnotationCleanupTest` (key), the migration test.
- **2d done** (2026-11-04): the plugin callers that send the story to a model take it from `PartTextRegistry.forModel` (reviewer, manga-generator, image-generator,
  arc-outline, sidequery, knowledge-tracker `ExtractionService` and `KnowledgeProcessService` (the text of a `Part`, so the quotes of the answer and the hash of a
  processed part are on what the model read), beta-readers, book-description-generator); chaptermarker, the Image Generator dialog, the converter and the Knowledge
  Tracker's "changed text" comparison stay on the stored text. Plugin tests pass (arc-outline, beta-readers, book-description-generator, image-generator,
  manga-generator, reviewer, sidequery).
- **Checked in the browser** (the screenshot environment, 2026-11-04): the `13-db-backup` data folder (database at V23) migrates to V26 at start (a copy is saved
  first, rows `EDITORIAL`, the three indexes), `shoot-features` takes the annotation pictures (dialog, mark, popover, margin, tab, annotate with the model, redraft,
  merge) with no exception in the log, and Side Query on `16-authorsnote` sends the story through the registry. Not checked in a browser: a mark of a plugin's kind (no
  plugin registers one yet; `StoryAnnotationsTest` covers the marks and the click).
- **Left:** the viewer click and the export hook wait for steps 9 and 10. Next is step 3, the model.

## Plan (tentative, one commit each, the plugin builds and loads after every step)

Build before every step: `mvn install -DskipTests` in `marginalia/`, then `mvn -o -q package` in this folder.

**The first version had no commands and no agent support (decided):** steps 1-12 were the whole plugin for a person using the UI, testable by hand and
with unit tests. **Commands for agents and the agent runs (duplicate, lock, difference) are the last features** (steps 13 and 14). What is kept from the
start is only the shape that makes them cheap later: **one service per area that the UI calls** (no logic in a dialog), and the *text as the model
reads it* accessor; the commands are thin wrappers added at step 13, with the test that lists the service operations and their commands.

1. **Stub** (done): bundle, activator, extension that logs, localization class, README, this file, the module in the root POM.
2. **Core check, just in time** (no plugin code; the viewer click and the export hook wait for steps 9 and 10, the **agent hooks** for step 14).
   Decided: the kind registry is full, the accessor central, the author keeps a switch. To do here:
   - **Annotation base and kinds**: the base class (i), V26 (SINGLE_TABLE, `kind`; check the migration tests run on a last-release
     database, and what else reads `status` / `source`: cleanup, backups, fulltext), the **full kind registry** (flags and the mark
     renderer / click), the lists skipping hidden kinds, `@PublicAPI` where an `@Extendable` class changes.
   - **The accessor**: the table of every reader of a part's text (core and plugins), source or stored for each; the accessor, the
     transformer registration, the callers switched, the guard test, the payload scan.
   - **The edit path**: how a plugin replaces the text of a part with the revision, the annotation shift, the summary hashes and
     `originalResponse` (a new revision type, and what the highlight of the author's edits does with a translation); the **rehash of the
     valid summaries**, always (decided: summaries are made from the source, hashed on the stored text, and a translation never changes the meaning); how a
     selection to redraft maps onto the source.
   - A changelog entry and a developer doc line for every change.
3. **Model** (done 2026-11-04, package `model` and `store`; 29 unit tests): `Language` (rule sections with an `Audience`, categories with *always send*, registers, the lexicon,
   speakers, the links to the example book and lorebook; lookup, `repeats()` (the same form *and* a shared meaning: a homophone is never listed), `variantGroup`, the deletes that
   clean the links, `duplicate` (keeps the links), `forExport` / `forImport` (no links, a new id, a newer format is refused)), `LexiconEntry` (senses, kind, rationale, rule, `Origin`,
   `EntryStatus` NEW / ACCEPTED / LOCKED / RETIRED, register, variants; `editedByAuthor()` makes a model's word the author's), `Speakers` + `CharacterNote` (mode, gender, voice, `speaks(name)` by
   name or alias), `Span` (the data of a passage of the kind `conlang-translator.speech`: language, source, speaker, `understood`, register, a hash of the conlang text to tell an edit, model, time;
   written into the `attributes` of the `AnchoredPassage`). `ConlangStore` keeps the library in the attributes of the user settings (a language that can not be read does not take the library with it)
   and exports / imports a language as a JSON document. **Not in this step:** the plugin does not use any of it yet (the registration of the passage kind is step 5), and the book's choice of a language
   is an inherited setting (step 6). Decisions made here: the variants are a list of ids on each entry (the group is the closure, so a link works from either side); whether a hyphen is a
   part of a word is **an option of the language** (`hyphenStructural`, off by default): off, forms are compared without hyphens (Vrakht writes the same compound with and without
   them), on, a hyphen is part of the form. It applies to the lookup by form and to the repeats; the checker of step 4 reads the same option.
4. **The request and the reverse** (done 2026-11-04, package `translate`, 47 more unit tests; the real-model trial of the new wording is *Real-model trial 3* above):
   - `Reverse.toModel(stored, passages)` turns the translated passages back into their source and returns a `ModelText` (so the core's map works: a quote of the model, a selection
     of the author). A passage is the speech without its quotation marks, so the marks, the markdown and the narration are never touched. An **edited** passage (the hash of its text
     does not match) stays as it is and is reported, one that overlaps an earlier one, has no source or is not in the text is ignored and reported; adjoining passages are both turned back.
     The step 5 registration turns the `AnchoredPassage`s of the kind into `Located` and calls it.
   - `TranslationPrompt.build(language, part, scene, maxLexiconChars)`: the **system message is the same for every part** of a book (instructions, rules of the audience *model*,
     registers, lexicon: tested), the **user message** has the characters of the notes who are in the part (speaks / does not know, gender, voice), the earlier parts, the point of
     view, the present characters and the part. `LexiconText` writes the lexicon under its category headings, **one line for the forms of a concept**
     (`breast: mela (formal), melis (soft), lelya (vulgar)`), no retired words, and cuts a lexicon that is too big down to the always-send categories, the categories of the words the
     part needs (a word of a gloss is in the part) and the categories of the new and locked words.
   - `TranslationAnswer.parse` reads the answer leniently (a code fence, text around it); `TranslationSchema.JSON` is the strict schema (a test keeps it equal to the records). New
     fields against the first trial: `understood`, `register`, and for a word `kind` and `variantOf`.
   - `AnswerChecker.check(part, answer, language)`: the quote must be found (`QuoteFinder`), the language the book's or `none`, no overlap whatever the order, text for `none` ignored,
     an empty text dropped; **the lexicon check** (`LexiconCheck`: the lexicon, compounds of known parts, the affixes of the language, the reported words with affixes, the names of the
     part, the hyphen option) flags the words nobody reported (`needsRepair()`); new words are reported, **never dropped for being new**, one that is in the lexicon with the same meaning is
     left out, a homophone is added, one without a rationale is added and flagged. **Not done:** the language's own checks (a mood particle first, `za` after a role particle: a later
     feature, the language needs a list of checks), the number check, and the vowel drop before an affix (`silvis`: prose in the rules; the check calls it unknown, the trial's `vaelissaren`).
5. **The hook** (done 2026-11-04, checked in the real app, see *Step 5 check* below): the extension registers the passage kind (`ConlangPassageKind`, the dotted mark `mark.conlang-passage`) and
   a `PartTextRegistry` transformer (`ConlangTransformer`, the passages of a whole branch with one query through `PassageService.listForParts`, new in the core); the part menu has **Translate the
   speech**; `TranslationService.translate` is the whole pipeline (the part as the model reads it, `TranslationPrompt`, one streamed request, `AnswerChecker`, `TranslationPlan`, **one edit**
   through `ChatMessageService.replaceText` (version `TRANSLATION`, not flagged as the author's, tokens stay the English ones, `originalResponse` follows when the part was unedited, summaries rehashed),
   a passage per speech with its `Span`, the new words into the lexicon of the library, new); the old passages go with the text they stood in, **the ones the author edited stay**; nothing is written
   when the model finds nothing (so a part is never left with conlang and no source). **Finality** as the Knowledge Tracker: the `AutoAnnotate.finished` decorator starts a worker (virtual thread with
   the session of the user) when the book has `auto` on, and the story is loaded again when it is done; **a regenerate removes the passages** of the part (`AFTER_CLEANUP`, only when it produced
   a text). A language and the settings of a book (`BookSettings`: language, auto, model, protocol, context parts, lexicon limit) are read from the library and the book's attributes; **there is no
   UI for them yet** (step 6), the menu action says "The book has no language" until they exist. The model of the request is the settings', else the book's, else the user's default.
6. **Languages in the settings** (done 2026-11-04 except the parts listed under *Not in this step*; checked in the real app, see *Step 6 check* below): the panel *Conlang Translator* in
   *Settings → Extension Settings* (`LanguageSettingsForm`: the list with New, Edit, Duplicate, Export, Import, Delete, and the user level of what books inherit), the **language editor**
   (`LanguageEditorDialog`: tabs Overview, Rules, Categories, Registers, Lexicon; the Speakers tab moved to the book, see *Decisions*; a copy until *Save*, resizable and draggable, a button maximizes it; the lexicon tab has a
   search, the filters by category and status, edit, accept, *Accept all new*, lock, retire, delete; the word window has the senses, the other forms of the concept, the rationale), the
   **inherited choices** in the book and folder dialogs (`ConlangInherited`: the language, translate by itself, the model, the protocol; the user level is `UserDefaults` in the panel, the
   book's `contextParts` and `maxLexiconChars` stay in the book's own attributes with their defaults and have no field yet), and the thin story side (first a read-only *Look up a word* tool, now the *Conlang* tab of the story, see *Decisions*). The Settings screen stores the library when it is saved (`LanguageLibrary.put` + `ConlangStore.merge`): **a language the
   user did not touch is taken from the library as it is then**, so the words a run added while the screen was open are not lost (tested); a language edited on the screen is the screen's.
   *Duplicate* is the only way to a language for one book (no book-local languages, decided). **Not in this step:** *Replace in the story* and *Retranslate* in the editor and the *Translate
   this* box (they need the en masse machinery of step 11 and the hand translation of the *Translating by hand* section), the badge of the *new* words on the story side, the fields for the
   number of earlier parts and the lexicon size limit, the preview of a bigger lexicon (a language of thousands of words was not tried in the grid).
7. **The change set and the diff** (done 2026-11-04, package `diff`, 31 unit tests, checked in the real app, see *Step 7 check* below): `LanguageDiff.diff(base, incoming, current)` is a **three-way** merge that
   makes a `ChangeSet` of `Change`s (ADD, CHANGE, DELETE, MOVE of the language's own fields, a rules section, a category, a register, a character, a word) with `FieldChange`s (before, after, now,
   the `value` that applying writes); `LanguageDiff.compare(current, other)` is the same with the base = the current language (a copy, an import, no history); `LanguageDiff.apply(current, set)`
   writes the picked changes. Decisions: things are the same **by id** (a copy keeps the ids), a **word that is new but already here under another id** (the same form and a shared meaning) is
   a conflict found by the word itself (a homophone with another meaning is not); a field both changed **differently** is a conflict that **keeps our value** until the user takes theirs or
   writes one; a change in conflict as a whole (changed here and deleted there, deleted here and changed there, a moved section on both sides, the same word) is **not picked by default**;
   senses, the other forms of a word and the aliases of a character are lists merged **as sets** (a sense is the same by its meaning); the order of sections, categories and registers is a MOVE
   (what is not in the longest common run of two orders moved); a new word that uses a new category or register **depends** on its add (`ChangeSet.pick` takes it along, an apply that finds
   it unpicked skips the word and reports it); `apply` is **stale-safe** (a field that is not what the set saw, or an add whose thing appeared meanwhile, is skipped and reported), does adds,
   changes and moves in the order of `Target` (a category before its words) and deletes last, and a delete goes through the language's own (a deleted category leaves its words without one).
   **The name of the language is not compared** (it is what the library knows it by). A comparison with no base never picks a delete by default (`pickAdditions`, also what *Select without
   conflicts* does there). The window is `ChangeReviewDialog` (a list with a check box per change, the filter by kind of thing, *Select all / none / without conflicts*, and for the selected
   change its fields here / in the other language / what will be written, with *Keep mine* and *Take theirs* on a conflict and the text editable for the fields that are text). It is reached from
   the library panel by **Compare** (bring the changes of another language into the selected one: duplicate, experiment, compare, merge back) and by **Import** of a language whose name is in the
   library (*Review the differences* or *Import as new*). What the user takes is written into a copy that replaces the language on the screen, stored when the Settings screen is saved.
   **Not in this step:** a stored base for a copy (*Duplicate then Compare* is two-way: what the original changed since the copy shows as a change to undo, the user does not pick it), the
   dependency of a word on the words it links to, the diff of the order of the lexicon, a text merge of a rules section edited on both sides (a conflict of a field is choose-a-side or write
   the text), a preview of a review of thousands of words (the list is a virtualized grid, not tried that large).

8. **Conlang designer** (done 2026-11-04, packages `designer` (pure, 10 unit tests) and `service` / `ui`; tried on a real model, see *Real-model trial 4* below; checked in the real app on desktop and on a phone):
   - **The request machinery:** `ModelClient` (the one streamed request, shared with the translator), `AsyncRuns` (a job on a worker thread of the session, the result on the page), `DesignerService` (the model of the designer: its own setting, else the translation's, else the user's default; the lorebook of the language as text; `propose`, `answer`, `proposal`),
     `DesignerPrompts` (one system message per action with the language as it is: the rules, registers, categories, lexicon and the lorebook; the answer is one JSON object, `DesignerAnswer`, read leniently).
   - **A proposal is a change set:** `ProposalBuilder` puts the answer into a copy of the language (a section is new or the rewrite of the one of its title; a word needs a form and a meaning and one the language has with a shared meaning is left out and said so; new words are origin *model*, status *new*) and the proposal is `LanguageDiff.compare` of the two, reviewed in the step 7 window (`ChangeReviewDialog`, now with a note for what the model said and what was left out) and applied into the copy the editor works on.
   - **In the editor:** a menu *Design with AI* (*Generate words...* for a category with a count and a note, *Add a section with AI...* and the words it needs, *Rewrite a section...*, *Review the language* (findings as cards), *Ask the designer...* (a conversation; an answer with words or sections has *Review the proposed changes*)), also as buttons on the category and rules tabs. **A new language:** *Design a language* in the library: the wizard (brief, sounds, writing, grammar, word formation, seed lexicon by category with *Add the basic categories*, test with sentences), each stage a section that the model proposes and the author edits, refines with a note and accepts; back and skip work; the language exists only in the window until *Finish*.
   - **The phone:** the Settings screen on a phone is the same `UserPart`, so the panel and every window are the same classes; found on a phone and fixed: the review grid shows the name and a conflict mark only (the rest is in the details, whose two columns stack), the lexicon actions are icons, the findings are cards instead of a table that did not fit.
   **Not in this step:** the designer's **lorebook narrowing** (all entries of the example lorebook up to 20000 characters, no picking), the **sentences from the example book** (the test takes the author's), *More senses / Alternatives / Explain / Check against the phonology* on a word, the structured phonology and the word generator (decided optional, later), the **warning that a language is shared by other books** before an accept, the designer's own **prompt templates** (the prompts are in code), a way to **stop** a request on the server (*Stop waiting* only drops the answer), the vowel and register actions *Add a vulgar variant*, *Import a description*.

9. **Display** (done 2026-11-04, 11 new unit tests, 151 in all; checked in the real app, see *Step 9 check* below):
   - **The author's views** (the story card of the desktop and of the phone, the manuscript dialog: they all draw the marks through `StoryAnnotations`): `ConlangPassageKind.onClick` opens `SourcePopover` at the mark: the English
     source, who said it, *(not understood by the listener)* for `understood: false`, and a red warning when the author edited the conlang text since (`Span.isEdited`). No core change was needed.
   - **The viewer needs no core change either:** the plugin decorates `Viewer.createPart` (`@Extendable`, `@PublicAPI` `msg`), reads the passages of the part with `PassageService.listForParts` (no ownership check, the readers are not the owners), finds the book as the viewer did
     (`manuscriptService.findViewable(manuscriptParam)`, so a book the reader may not see has no mode) and calls `ReaderView.apply`, which `setContent`s the `PartView` the viewer built with `ReaderText.render`.
   - **The reader mode** is the inherited choice `conlangtranslator.reader` (book > folders; **no user level**: the author's defaults must not decide what a reader sees; fallback *click*): `ReaderMode` CLICK (the conlang, a click shows the English), CONLANG (the stored text, nothing else),
     SOURCE (the reverse the models read; an edited speech stays conlang). **In the click mode the page holds only the uuid of the passage** (`data-conlang-passage`); the click event carries it to the server, which builds the popover from the passage it already has: the English is never in the
     page, in any mode but SOURCE. The plugin's own script (`ReaderView.CLICK_SCRIPT`) moves the popover anchor onto the mark; `.conlang-reader-part` makes the part the anchor's host. The viewer got the plugin's CSS (`getCss` for `Viewer`).
   - **Not done:** the *Manuscript* dialog's *preview* (`PartPreviewDialog` builds a `PartView` too and has no marks: it shows the conlang, no click); the author's **personal switch** between the three modes in the story views (decided, not built);
     a **test of `ReaderView` / the decorator** (it needs a UI; only `ReaderText` and `SourcePopover.content` are unit-tested, the real-app check below covers the rest).

10. **Exports** (done 2026-11-04, checked in the real app, see *Step 10 check*; 9 new unit tests in the plugin, 7 in the core):
    - **Core, small and generic:** `ExportDialog.createExtensionFields()` (a seam: an empty list that the constructor adds to the form, `@PublicAPI` from 1.7.0), and **footnotes for every exporter** (`commonmark-ext-footnotes`, a new dependency): the author's own `[^1]` / `[^1]: note` work too. HTML / EPUB / AO3 get linked notes under the part (`wellFormed` for the EPUB's XML,
      AO3 stripped of what its editor cuts), TXT / DOCX / PDF get `[1]` and the notes as `[1] ...` **endnotes after the part's text**, Markdown keeps the syntax. **Not done: real footnotes at the foot of the page** in Word (`XWPFDocument.createFootnote`) and in the PDF.
    - **The plugin:** the field *Translated speech* in the export dialog, **only for a book that has translated passages** (decorator of `createExtensionFields`), with four choices (`ExportMode`): the conlang as it is (default: nothing changes), the English only, the conlang with the English in footnotes, the English with the conlang in footnotes. A decorator of `export` (enter) replaces
      the parts with copies (`ChatMessage.copyOf`) whose text is `ExportText.render` (the footnote numbers go on over the whole export, the mark behind the closing quotation mark, a speech the author edited stays as it is without a note, the note is one line with the speaker in front). The stored parts are never changed.
    - **Not done:** the *glossary* at the end (the words used in the book), the speaker as an option of the note, the choice remembered between exports, a choice of the default per book, the exports of the **SillyTavern chat** and the **Obsidian vault** were not looked at (they read the text of the parts themselves; the Obsidian notes keep Markdown, so the notes should survive, the chat is a model's view and should perhaps get the English).
11. **En masse actions** (done 2026-11-04, checked in the real app, see *Step 11 check*; 7 new unit tests, 167 in all): **Translate parts** in the tools menu of the story opens `RangeDialog`, **non-modal** (the story stays usable; a part edited meanwhile makes that part fail with *The part was changed*, the run goes on) with
    three actions over *From part* / *To part* (the parts that have text, counted from 1 as in the export dialog): *Translate the parts that are not translated*, *Translate the translated parts again* (a retranslate: `TranslationService.translate` reads the English back from the passages), *Put the English back*
    (`TranslationService.revert`: every speech the author did not edit is its source again, its passage is removed, the history keeps the conlang version `TRANSLATION`; a confirmation first). The count *N parts in the range, M to do* follows the choice; the dialog reads the parts again before each run and after it (a run changes which are translated).
    The run (`RangeLoop` pure and tested, `RangeRuns` the worker thread with the user's session) goes **one part after the other**, shows *Part n of m*, **Stop** is looked at between parts (a request to the model can't be taken back), a part that fails is counted and the run goes on, *no language* / *no model* ends it; the summary says parts, speeches, new words,
    edited speeches that stayed, failures, and whether it was stopped; the story is drawn again when something changed (core: `StoryEditor.reloadStory()`, new public method). **Not done:** *keep the translations whose source did not change* for a retranslate (every part of the range is asked again), an estimate of the time and tokens before a run
    (a part is minutes on the local model), a run that survives a page reload (it runs on in the server but the dialog and its Stop are gone), the same buttons on the phone (the item is in the tools menu of both, the dialog was not looked at on a phone), the *Replace in the story* and the *Translate this* box of step 6.
12. **Docs** (done 2026-11-04): the user manual page `manual/user/extensions/conlang-translator.md` (the idea, languages and the editor, the designer, the review, a book's choices, translating, the story, the viewer, the export, the limits) with 11 pictures, the rows of the extension index, the phone table, the developer plugin index and the plugins README, the plugin README (no longer a stub),
    the changelog (one entry, rewritten as the plugin grows, the API lines under *Changed*). **Screenshots are a flow now:** `screenshots/bin/shoot-conlang.sh` (the language *Orrelian* in `dataset/conlang-orrelian.json`, a changed copy for the review, `bin/seed-conlang.py`, `flows/plugin-conlang.mjs`), see `SCREENSHOTS.md`.
    **Localization:** `ConlangTranslatorLocalizationTest` checks that every text has its ja and zh entry, with the same values, and that no translation is left for a gone text; they pass, the translations are the model's and **not read by a native speaker**. **Not done:** the flow does not yet cover the translating itself (the part menu, a run with the fake model: the checks of steps 5 and 11 are in the notes only), nor the phone.
13. **Commands for agents** (done 2026-11-05, 25 new unit tests in all; **not tried in the real app, see *Step 13 / 14 check*** below): package `commands`, five suites registered by the extension, 68 commands, all `conlang-` (the table of *Commands for agents* is the plan,
    this is what exists): `LibraryCommands` (languages, `conlang-set-book-language`, rules, categories, registers), `LexiconCommands` (words, states, `conlang-accept-words`, `conlang-check-lexicon`), `TranslationCommands` (`conlang-status`, translate part / range / sentence / text,
    `conlang-revert-to-source`, `conlang-check-translation`, `conlang-get-source`, `conlang-list-passages`, the speakers, the macros `conlang-lexicon` and `conlang-rules`), `DesignerCommands` (design words / rules / stage, rewrite, ask, review, try sentences, `ask true` + `conlang-apply-changes`
    through `HeldProposals`, and the three commands for what a run left). `ConlangCommandSupport` (a `ManuscriptReadSupport`) finds a language by name or id, a section by title or number, a category by name or number, a word by its form (the id only for homophones). A word an agent adds or
    changes is the model's and *new*, one the author types at the command bar is the author's; a locked word is not changed or deleted. `CommandsTest` registers every suite in a plain `CommandServiceImpl` (the core checks the annotations), checks the conventions (prefix, texts, macros read only,
    the commands that ask or call a model are `async`) and has the table **operation of a service -> its command** that fails for a new public method of a service that is neither a command nor listed as ui-only. **Left out:** `conlang-replace-word` (the *Replace in the story* of step 11 does not exist
    as a service), `ask true` for the translating commands (a translation is undone by `conlang-revert-to-source` and the history of the part), `conlang-edit-source`, `conlang-word-alternatives` / `conlang-word-senses` (the word window's buttons are the designer's `conlang-design-words` and `ask`), and a ladder of
    overloads: an optional parameter is `optional = true` in one form, so an agent may leave it out and the command bar needs every argument.
14. **Agent runs** (done 2026-11-05 in a **simpler way than designed**, see *Agent runs* above and *Step 13 / 14 check*): **no hook of the core was added.** The design (the lazy duplicate, the lock, `ExtensionHasAgentRuns`-style events, an extra section in the merge) is replaced by what the core
    already does for the data of a plugin in a book: `LanguageAccess` is the one place that reads and writes a language; inside an agent run (`Manuscript.isAgentic`) the first change of a language puts a copy of it and the version it came from into the attributes of the **working copy**
    (`RunLanguage`, key `...runLanguages`, an object keyed by the language id), and the run reads and writes that copy from then on. The copy goes with everything the core does to the copy of a book: the checkpoints and their restore (`snapshotAgenticCopy`), the delete of the copy (so no
    cleanup of a left duplicate is needed), and the review, where `attributes_<key>` is a JSON field of the book that the core merges three ways (`JsonMerge`, words matched by their `id`). Applying the run puts the key into the book's attributes; the **Conlang tab** of the book then shows *An agent run
    changed the language X* with **Review** (a `ChangeReviewDialog` over `LanguageDiff.diff(base, run's, library now)`, so the user's changes to the library meanwhile are kept and what both changed is a conflict) and **Drop**; `conlang-pending-changes` / `conlang-take-pending-changes` /
    `conlang-discard-pending-changes` do the same for an agent that is not in a run. `TranslationService` and `BookConlang` read and write through `LanguageAccess`, so the words a translation invents in a run go to the run's copy.
    **What this does not do, against the design:** the language is **not locked** while a run works (the user and other books can change the library; the three-way take-over keeps both), the run does not see the library's changes made after its first write (a **sync on resume** for the language is not done),
    the original book's review shows the language as one JSON field (editable as text) and not as a list of words in the core's window, and the warning *"3 parts use words you did not take"* is not made. These are the follow-ups if the review of a long run turns out to be hard to read.

## Step 13 / 14 check (2026-11-05)

Done: the plugin builds, 192 unit tests pass (`CommandsTest`, `RunLanguageStoreTest` and the 167 before). **Not tried in the real app and not with a real agent run** (the check of the other steps was done in the screenshot environment): that the commands answer as written, that a run's first write puts the copy
into the working copy and that the review of the core lists the `attributes_...runLanguages` field of the book and applies it, that the banner of the Conlang tab shows after the apply, the phone. The assumptions that were read in the code and not run: the working copy keeps the `attributes` of the book
(`createAgenticCopy` via the backup JSON), `ManuscriptService.update` works on an agentic book, the agent's threads have the session of the user (`SettingService.getOrCreate(UserSetting.class)` in a command), the key with dots in its name is a valid `attributes_` field.

## Real languages: the same translation for Japanese and the like (decided 2026-11-05, built the same day in `../reallang-translator`)

The translation of the dialogue does not need an invented language: a story set in Japan can have its characters written in English and the speech translated into authentic Japanese (kana and kanji, or romaji), with the English as the source of
the passage (the click, the footnotes of the export) like now. **Decision: a separate plugin** (working name `real-language-translator`), so that an installation that does not need it does not load it (modularity first; the memory is the classes of one more bundle, which is small).
The conlang plugin stays as it is. A book may still mix an invented language and a real one: each plugin has its own speakers, its own passage kind and its own language setting, and the model path already works (`PartTextRegistry` merges the substitutions of every transformer; the second
plugin reads the English of the first one's speeches). What makes the two coexist is therefore in the **core**, and it is done first (the API is free to change while the version is unreleased):
1. **An export registry in the core (done 2026-11-05)**, like `PartTextRegistry`: a plugin returns substitutions and footnotes for a part as ranges of the stored text, the core applies all of them at once, drops overlaps and numbers the footnotes in one sequence. Today each plugin replaces the `messages` of the export dialog
   in turn with offsets of the stored text, which the second one reads against text the first one changed (the hash check of `ExportText.usable` skips such passages, so nothing is wrong, but they are lost; and two plugins would both start their footnotes at `[^1]`). The conlang export moves to it. Check the viewer
   (`ReaderView`, the marks of the passages) for the same, a passage kind is rendered by its own plugin on the stored text.
2. **`translator-common`**: the pure code both need (the span and passage data, `Reverse`, `TranslationPlan`, `SelectionRange`, `RangeLoop`, `ExportText` / `ReaderText` / `SourcePopover`, the speakers) as a plain Maven module of the root `pom.xml`, **embedded** into each bundle (`Embed-Dependency`; plugins do not see each
   other's classes, so it is shared source and not shared runtime: nothing static in it may be state that the two plugins would have to share). The passage kind id, the attribute keys and the command prefix are parameters, not constants. The contract that a passage is used only when its text still matches
   the hash it was made for stays in the library and has a test.
3. **The plugin**, with the steps below; it has its own settings panel, inherited settings (language, model, auto), commands (`reallang-*`), localization and manual page.

**Done 2026-11-05 (the groundwork):** the code of this plugin that does not depend on a lexicon is the library `marginalia/libs/translator-common` now (see its README: the span and passage data, `Reverse`, `TranslationPlan`, the transformer, the reader's and the export's text, the run loops, the popover, the speakers' editor, the list editor...), embedded in this bundle and in the new
`reallang-translator` (a skeleton with its passage kind, transformer, CSS and icon; its own `TODO.md` has the steps). In this plugin: `ConlangKind.ID` is the passage kind, `TranslationRuns` wraps `PartRuns`, the range run takes its work as a lambda (`rangeWork` of the extension), the export field takes its labels from the plugin; `ReaderMode.CONLANG` / `ExportMode.CONLANG*` are
`TRANSLATED` / `TRANSLATED*` (a book's stored reader mode `conlang` is still read), the mark attribute of the viewer is `data-translated-passage`. The core **export registry** (step 1 above) is done (`ExportTextRegistry`, `ExportDialog.applyExportTexts`; this plugin registers a `SpeechExportContributor` and no longer decorates `ExportDialog.export`; documented in the developer manual). The viewer's marks for a mixed book are **done** (2026-11-05): `ReaderView.apply` draws a part once from the passages of every plugin (`TranslatedPassages`, each with the mark of its kind) and leaves a flag on the part, so the plugin that decorates first does it for both; one reader mode rules the page. Both plugins have an icon (`icon.svg`, `ConlangIcon` / `ReallangIcon`): the
orc with a shouting bubble for the invented language, a person with a kanji in a bubble for the real one.

What a natural language is: a `Language` with a short rules section (the script, how politeness and honorifics are handled, how names are written) and **no lexicon**; the request sends no lexicon and asks for no new words. Steps:
1. **The language of the new plugin** is a rules section (the script, how politeness and honorifics are handled, how names are written) with presets (Japanese: script kana+kanji / kana / romaji, honorifics kept or translated), kept in the user settings like the library of the conlang plugin, under its own key.
2. **The prompt for it** (`natural` package): translate for this speaker and listener in the register their relationship asks for; the checks that need a lexicon (`LexiconCheck`, new words) do not run, the quote check stays, and a **script check** replaces them (kana or kanji where it was asked for, romaji only otherwise).
3. **Several real languages in one book (done, see below):** a language per character (a field of its note, the book's language is the default), the request lists the languages of the speakers of the part, the model says which language each speech is in (`Span.language` is there already). Relationship fields on the note (age, rank, closeness) for politeness.
4. **A second pass** as an action on a part or a range (not a stage of every translation: it doubles the requests of a slow model): the model rewrites each speech for naturalness, given the voice and the relationship; optionally a back-translation check that flags drift.
5. **A reading line** on the passage (`Span.reading`, the romaji of kana or kanji text) in the popover above the English. Furigana (ruby) only in HTML and EPUB, left out of the first version.
6. **Commands and agent runs:** `reallang-*` commands for everything the screens do; the run copy of a language is the one of `LanguageAccess` (it moves into the library of step 2 if it is generic enough).
Quality has no lexicon as a safety net: the second pass, the back-translation and the author are the checks.

### Several languages in one book (conlangs too) - done (2026-11-05)

An epic fantasy has a tongue for the elves, another for the dwarves, a third for the orcs; the plugin and the Real Language Translator (same library) do it the same way. What was built:
- **The book names several languages**, each with **its own speakers**: the first is the inherited setting (`BookSettings.languageId` with `speakers`), the others are `BookSettings.otherLanguages`
  (`BookLanguage`: the id and its `Speakers`). A language added in the **Languages** tab starts in the mode *only the listed* with nobody listed, so two languages do not both claim everybody; the
  first language needs its exceptions listed by the author.
- **One pass per language** (`TranslationService.translate(book, part, languageId)`; null: all languages whose speakers may be in the part, `Speakers.mayBeInvolved`). Each is asked separately: its
  rules and lexicon only, only its speakers' speeches. The passes run one after the other; a pass reads the part as the model reads it (the English of **all** languages) and keeps the
  speeches of the other languages translated: `TranslationWriter.carried` makes them entries of the plan, a speech of the answer that lands on one of them is dropped, and the write makes their passages again with their own `Span`.
- **The passages say which language they are** (`Span.language`): a retranslation of one language and a revert of one (`TranslationWriter.revert(book, part, read, languageId)`) leave the others alone;
  *put the English back* is for one language or all.
- **The range dialog, the automatic run and the commands** take the language (all of the book's by default; `RangeLoop.select` counts "translated in that language").
  Not done: the run of the range does not say the pass (*Part 3 of 40, Elvish*); each part is all of its languages.
- **The menus ask with a dialog** (`LanguageChoiceDialog`) when the book has more than one language: the part menu (a language or *All languages*) and the right click on a selection (one language);
  *Translate parts* has a field. Not one menu entry for each language: the menu registry's items are static and a book's languages come and go. The selection route works for each language on its own.
- **The Conlang tab** has a field for the language the *Words*, *Speakers* and *Quick translate* tabs are about, and the *Languages* tab (`BookLanguagesPanel`). Commands: `language` on the
  translating and speakers commands, `conlang-add-book-language`, `conlang-remove-book-language`.
- **Not tried on the real model**, and the footnotes of an export do not name the language yet (the popover and the footnote say who spoke, not in which language).

## Later / ideas

- **A custom script (a font per language).** The text stays romanized (the stored text, the reverse, the search and the model never
  see the script); the font only changes how the letters are drawn, as most conlang fonts map the Latin letters to glyphs.
  Feasibility, not checked yet: *in the application* it looks cheap (the extension's CSS is put into the pages by `CssRegistry`;
  an `@font-face` with the font as a `data:` URI works but is heavy in every page, so a real file URL is better: where an
  extension may keep a file, to check); *the viewer* of a published book needs the same CSS; *the exports* are the hard part: EPUB
  can embed a font (a file in the zip, an `@font-face`), HTML can inline it, DOCX needs an embedded obfuscated font and PDF a
  font embedded by the PDF library; none of the exporters has a way to add a file today (it would be part of the export hook).
  So: the display first, the HTML / EPUB export next, DOCX / PDF last or never (a note that the font must be installed).

- **The POV character does not understand.** The prompt of the next generation sees the source (English), so the model may let a character
  who does not know Sylvari react to what was said. A block like the Knowledge Tracker's ("Mara does not understand Sylvari;
  she does not know what was said") for the speech with `understood: false`, or telling the Knowledge Tracker.
- **Learning a language.** A character who picks it up (the lexicon words they know) and the translation showing partly.
- **Word-level view:** hover a conlang word for its gloss (the model gives an interlinear gloss per speech).
- **Consistency check** of an existing translation against the lexicon (the same meaning, the same word); a report of the words
  used that are not in the lexicon.
- **The model suggests a change of the rules** (a pattern it had to break, a gap in the grammar) as a note the author accepts or
  not; never applied by itself.
- Text-to-speech / pronunciation (IPA) per speech.
- Several translations of one speech (a dialect, two languages in one scene) and a **retranslate with a note**
  ("shorter", "more formal").

## Open questions

None left from the first round. Still to do, not questions: the **test conlang** (from the author) for the real-model trial of the
request; the **core check** (step 2): the migration V26, the accessor for the text a model reads, how a plugin edits a part's
text, the viewer's click, the export hook; and the points marked *to check* above.
