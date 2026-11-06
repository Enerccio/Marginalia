# Workset builder (the dataset harness)

Builds the large test book described in `TODO.DATASET.MD` (repo root) by running the **real generation loop of the application** over a written skeleton of
the story, with a model behind a bridge. A build tool, not a test: it does nothing unless `-Dworkset.build=true`, and the name `WorksetBuilder` keeps it out of the
normal suite (surefire only picks `*Test`). The result is the backup JSON of the book; tests and screenshots load that file, they never run the harness.

## Running it

From `marginalia/` (the Maven project), build elsewhere than `target/` when the IDE is open:

```sh
mvn test -Dbuild.dir=/tmp/marginalia-build -Dtest=WorksetBuilder -Dworkset.build=true \
         -Dworkset.parts=5 -Dworkset.author=claude -Dworkset.model=haiku -Dworkset.lengthFactor=0.9
```

| Property | Default | |
|---|---|---|
| `workset.build` | - | must be `true`, else the class is skipped |
| `workset.parts` | 5 | how many parts this run adds, then it stops |
| `workset.until` | none | stop after this part number |
| `workset.author` | `fixed` | `fixed` = a fixed text, no model (proves the chain, costs nothing); `claude` = the `claude` command line tool in non-interactive mode |
| `workset.model` | `haiku` | model name given to `claude --model` |
| `workset.steps` | `generate,annotate,redraft` | which steps of the loop run (`generate` only = the old behaviour); add `knowledge` (Knowledge Tracker) and `translate` (the four languages); both need the plugins, see below; `tools/workset_run.sh` runs all five steps |
| `workset.plugins` | by the steps: `knowledge` with the knowledge step, `conlang,reallang` with translate | plugins started by hand, in this order; known: `knowledge`, `reallang`, `conlang`, `arcoutline` (Arc Outline loads but has no step yet) |
| `workset.blockSize` | 25 | the block (act) after which the `blockend` step runs: Arc Outline import, chapter headings `# I. Name` (numbered over the whole book), summaries at the last part of each chapter except very short ones and the last chapter of the block; once per block (`state.json`), also for a full block found at the start of a run |
| `workset.chapters` / `workset.chapterWords` | from the words / 3000 | the exact number of chapters asked for in a block; by default the words of the block divided by `chapterWords` (at least 2). Left to itself the model cut 20 chapters out of 25 parts (about 600 words each) |
| `workset.summaryEvery` | 1 | a summary at the end of every n-th chapter of a block (1 = each chapter, 2 = every second one, about 3000 or 6000 words); never for the last chapter of the block |
| `workset.metaPairsPerBlock` / `workset.contextShare` | 1 / 0.5 | the flow of the summaries: during a block they do not change; at its close every chapter except the last gets a summary, the last chapter stays whole for the next block (the first summary of the next block covers it). From the second close on, one pair of the oldest fresh summaries is merged into a meta summary (summary 1 + 2 -> meta 1: one meta by part 50, two by 75); more pairs only if the summaries alone would take more than `contextShare` of the usable context (max context minus reply). A meta summary is merged into another one only as a last resort (4 of them) |
| `workset.summary.minWords` | 800 | a chapter with fewer words gets no summary |
| `workset.lengthHint` / `workset.lengthHintParts` | false / none | ask for a number of words (pointless in general); `lengthHintParts=19,40` keeps the ask for named parts (tests of the length badge) |
| `workset.translators` | `conlang,reallang` | the order of the two translator plugins in the pass |
| `workset.translate.all` | `false` | the translator plugins asked for **every** part whatever the part says: `conlang`, `conlang,reallang` or `true` (both). By default a language is asked when a character who speaks it is in the scene (the POV character or a present one, by name or alias: `TranslationPass.SPEAKERS`, so Japanese and Czech nearly always because of Tomáš and Mizuki) or when the beat's codes have it (J/Jm Japanese, C Czech, V/v/V+ Vrakht, A Aethel-Vael), which covers speakers the story does not name |
| `workset.translate.allUntil` | 1 | the parts up to this position are translated into all four languages whatever their beats say: a smoke test of every language and of the false positives |
| `workset.lengthHint` | true | add "About N words." to the instructions of a part |
| `workset.lengthFactor` | 0.75 | the asked length times this (the model writes longer than asked); 0.9 gave 450 words for 450 asked |
| `workset.start` | 2026-01-05 | the calendar date of day 1 of the timeline |
| `workset.dir` | `../work/harness` | the work folder (output, database, cache) |
| `workset.skeleton` | `../work/dataset` | the folder with `beats.json` and `lorebook.json` |

`tools/workset_run.sh [-D...]` is the same command with the plugin classes on the classpath and `translate` in the steps (`BUILD_DIR`, `WORKSET_STEPS` override the defaults). Plugins first: `mvn install -DskipTests` in
`marginalia/` and in `marginalia/libs/translator-common`, then `mvn package -DskipTests` in each plugin (their `target/classes` are woven there); one Maven at a time.

## The input: the skeleton

`work/dataset/` (ignored by git, built by `build_skeleton.py` from `skeleton_data.py`): `beats.json` (100 parts: scene, POV, present characters, instructions,
word range, day and time on the timeline, the languages in it; the branch), `lorebook.json` (the lorebook in the application's own file format),
`knowledge_seed.json` (start state and expected items of the Knowledge Tracker, not used by the harness yet), `outline.md` (the same for reading). The checks of a run are in `tools/workset_check.py`.

## The output: everything is under `work/harness/` (ignored by git)

`review/` is **only the report for a human**. The product is `export/book.json`; the rest is state.

| Path | What | Written |
|---|---|---|
| `export/book.json` | **the product**: `createBackup(book, includeHistory = true)` of the book as it is after the run (parts, lorebook, prompts and lore usage of every part, revisions) | every run, overwritten |
| `review/latest.md` | a symbolic link to the review of the newest run (made by the harness at the end of every run) | every run |
| `review/run-NNN.md` | the parts **this run** added, **every step of the loop as a section of its own** (generate: instructions, prompt size, lore, text; annotate: what to look for and each proposed note; redraft: notes given, before/after, the redrafted text), with the call (cached or asked, seconds, tokens) of each. For reading, nothing reads it back | every run, one file per run |
| `usage.jsonl` | the ledger, one line per call of the model: part, author, cached or not, request/answer size, tokens, cost the tool reports, milliseconds. The table printed at the end of a run is made from it, and the run number is the highest in it plus one | appended |
| `answers/<hash>.json` | the **cache**: every answer of the model with the request it answered, named by the SHA-256 of (author id, all messages). A request that is the same is never asked again | when a model answers |
| `prompts/<part>.json` | the messages of the last request of each part (`part-1.json`, `branch-1.json`), to read what the model was sent | overwritten |
| `home/` | the **database** of the application (SQLite, the lorebook, the book, the parts, the user `author`); kept between runs, this is what makes a run continue | by the application |
| `state.json` | only the branch flags (`branchStarted`, `branchDone`); absent until the branch is built | by the harness |
| `cli-cwd/` | an empty folder the `claude` tool runs in, so no CLAUDE.md or settings are found | |

## How a run goes (and why it can be resumed)

1. `TestContextPostProcessor` is given `marginalia.test.fixedHome` = `work/harness/home`, so the Spring context uses that folder instead of a fresh one.
2. The user `author`, the AI, the protocol, the lorebook and the book are **found again by name**, or created the first time. The AI's address is updated: the fake model server has a new port every run.
3. The next part is the **length of the active branch plus one** (the database is the truth, not a counter). A part whose generation failed is removed by the application itself.
4. For each part the clock (`AppClock`, a shared clock the application reads for every stored time) is set to the moment of the part on the timeline, the real `generateNextTurn` runs, the application builds
   the prompt (lorebook, macros, summaries) and sends it to the **fake model server**; the **bridge** (`WorksetBuilder.bridge`) writes the prompt, asks the cache, and on a miss the author, stores the answer,
   moves the clock a little (time to first token, then the time it took) and streams it back. The application saves the part exactly as in a real book.
5. After part 40 the **branch** (3 parts, a second child of part 39) is generated, and the active leaf is set back to the main line.
6. The run stops after `workset.parts` parts, exports, writes the review and prints the usage table.

**To redo parts:** there is no `--redo` yet. Move or delete `work/harness` (the cache goes with it, which is what you want when the prompts changed: a changed prompt misses the cache anyway) and run again; keep an old
folder next to it to compare (`work/harness-run1`...). To keep the cache and redo only the database, delete `home/` and `state.json`. If a run dies **inside the branch**, the next run refuses to go on
(`state.json`); remove the half branch by hand and set `branchStarted` to false.

## The pieces (this package)

| Class | |
|---|---|
| `WorksetBuilder` | the JUnit class: setup, the loop, the branch, the bridge, the review and the usage table |
| `Author` | the interface of the model behind the bridge (messages in, text out, with usage) |
| `FixedAuthor`, `ClaudeCliAuthor` | a fixed text; the `claude` tool (`-p`, `--model`, `--system-prompt` = the system message of the application, `--tools ""`, `--no-session-persistence`, `--setting-sources ""`, JSON output) |
| `AnswerCache`, `Ledger` | the cache by request hash; `usage.jsonl` |
| `Skeleton`, `SteppingClock` | reads `beats.json` and builds the instructions with the length hint; the clock set by hand that `AppClock` delegates to |

## Runbook: continuing the build (after a compaction, a new session, another person)

Everything needed to continue is on disk; nothing depends on a conversation. **The state is the database in `work/harness/home`** (how many parts, which branch), the plan is in
`TODO.DATASET.MD`, the skeleton in `work/dataset/`.

1. **Find out where it is.** `python3 tools/workset_check.py | tail -5` (the number of parts and the problems), `tail -3 work/harness/usage.jsonl` (the last run), the newest file in `work/harness/review/`.
   Old attempts are `work/harness-run1`, `-run2`... (kept to compare, never used by a run).
2. **Read** `TODO.DATASET.MD` (the sections *Harness status*, *The author's workflow at the end of a block*, the decisions at the top), `work/dataset/outline.md` (the story), and the newest review.
3. **Never start a run without the author's go.** A run is 5 to 25 parts (`-Dworkset.parts`); the author reads the review between runs. Memory: one Maven at a time (see `CLAUDE.md`); the run is
   about 15 seconds per part with Haiku, so run it in the background and wait for the notification, do not poll.
4. **The loop for a run:**
   a. run the command above (background), `-Dbuild.dir=/tmp/marginalia-build`, author `claude`, length factor `0.9`;
   b. `python3 tools/workset_check.py <first> <last>`: foreign text, hidden speech ("murmured something"), cue habits, length, words per day;
   c. read `work/harness/review/run-NNN.md` in full (the text is what matters; the script only finds the obvious);
   d. report to the author: what is good, what is wrong (quote the lines), the numbers (words, calls, seconds, tokens; the cost figure of the tool is list price and means nothing on a subscription, what
      counts is the hourly and weekly limit), a recommendation, and **ask for the go**.
5. **When the text is wrong** fix the cause, not the part: the rules are in the lorebook entry *Style* (inserted before the user prompt; `work/dataset/skeleton_data.py`, then
   `python3 work/dataset/build_skeleton.py`), the content in the beat (`skeleton_data.py`, `BEATS`). **Gotcha: the lorebook is imported once.** A resumed run finds it by name in the database and does *not*
   read `lorebook.json` again, so a changed *Style* entry only works after `mv work/harness work/harness-runN` (a fresh database, an empty cache) and a run from part 1. A changed *beat* is read at every run but
   only affects parts not yet built. A part already in the book is not rewritten by a later run.
6. **Rules the story has settled on** (details and reasons in `TODO.DATASET.MD`): all speech is quoted English and the language is named only when it changes ("she said in Japanese"); no foreign word, gloss
   or romaji is ever written by the model (the translator plugins create them afterwards, and only translate quoted speech); thoughts are in English; muttering is quoted; names come from Markov chains
   trained on the lexicons (`work/dataset/names.py`), not from a model; the chapters are numbered I to N over the whole book (not per arc), headings are `# IV. Name`; a summary is made at the end
   of each chapter except a very short one and except the last chapter of the block, which stays whole.
7. **Next, in this order** (the author decides): parts 6 to 25 (the rest of block 1); then the shared way to run plugins in the harness (Arc Outline import, Knowledge Tracker, the translators), the block-end
   step, blocks 2 to 4; the workflow passes; the statistics companion; `LargeWorksetTest`.

## The plugin host and the translation pass (2026-11-05, second session)

`PluginHost` starts plugins without OSGi, as planned: `Class.forName`, `new` (the weaving injects the `@Autowired` fields), then `onExtensionLoad(stub Bundle, stub OsgiService, ExtensionService)`. The stubs
know the symbolic name (`marginalia.workset.<name>`) and make the logger (`extension.<name>`), everything else throws so a plugin that needs more shows up at once. Checked: the Real Language and Conlang
translators load and register their commands, passage kinds, transformers and generation listeners; their `registerDecorator` calls are accepted and inert. The Knowledge Tracker and Arc Outline are in the
host's list but **not run yet**. The classes must be on surefire's classpath (`-Dmaven.test.additionalClasspath=...`, comma separated), they cannot be dependencies of the app (a cycle).

`TranslationPass` is the last step of the loop (`translate`), run through **the plugins' commands** with `CommandScopes.forManuscript(book)` (nothing called directly):
- **Set-up** (every run, idempotent, by name): `conlang-import` of `vrakht.json` and `aethel_vael.json` (made by the plugin's own converters into `work/dataset/` when missing), `conlang-set-book-language Vrakht`,
  `conlang-add-book-language Aethel-Vael`; `reallang-create` Japanese (preset `ja-kana-kanji`) and Czech (preset `other`, native script), `reallang-set-book-language Japanese`, `reallang-add-book-language Czech`.
  Speakers (mode *only*, listed = speaks it): Vrakht = Steing, Paskal, Mangel, Frecht, Kestim, Roid, Saellen (market Vrakht); Aethel-Vael = Saellen, Aelmoria; Japanese = Mizuki, Tomáš; Czech = Tomáš.
  Tomáš and Mizuki learn Vrakht later (parts 26-60); the speaker list is static, so that rests on the cue in the text ("in Vrakht"), which the plugins let override the list.
- **Per part**, after the redraft: one `<plugin>-translate-part` call per language (a language when one of its speakers is in the scene or the beat has it; the first part all four), in the order conlang (Vrakht, Aethel-Vael) then reallang (Japanese, Czech); the review gets a section for each
  (the command's answer, the speeches with their English) and one with the part as stored. The ledger kind is `translate`, the label `part N translate <Language>`.
- A command is found by the **exact set of parameter names**: pass all of them (null/empty for the ones not set), or "No form of command ... takes the arguments".
- Known limit, as in the reallang TODO: the second plugin's model sees the first plugin's translation in the part (a Vrakht line is in the text the Japanese pass reads); a speech claimed by two languages is
  decided by whoever comes first. The next part's prompt is checked to read the English again (the transformers of the plugins).

Checked with the fixed author (3 parts, all four languages, `work/harness-translate-fixed`, 21 calls): every language translates its line, the other three answer `none`, the next prompt has the English only.
**Not tried on a real model**: the real translation prompts (Vrakht with its lexicon, Japanese, Czech) have never run through the bridge with Haiku. Expect `newWords`/`needsRepair` for Vrakht and Aethel-Vael.

## The knowledge step (2026-11-05)

`KnowledgePass`, between the redraft and the translation (the author's order). Through the plugin's commands; the only direct write is the **settings of the book** (the plugin has no command for them, they are
its state in the book's attributes): block in the prompt, `extractWithModel` on, `askBeforeApplying` off (changes applied and logged), `autoProcess` off (the harness calls `knowledge-process` itself, so the
call has its own ledger line and review section instead of hiding inside the next generation). Tracked: Tomáš, Mizuki, Saellen, Aelmoria, Steing, Mangel, Paskal. **The start items** (`role: start` in `knowledge_seed.json`, 11)
are entered by hand with `add-knowledge`; the `expected` items are only printed in the review next to what the model found, never inserted. The branch parts are not read (the state follows the main line).
Checked on Haiku, parts 1-5 (run 3): the block (start items + what the model found) is in the next prompt, English only; part 4 found "Mizuki now knows Tomáš learned Japanese" (the seed expects it) and retracted the
old "does not know".

**Found by the smoke test of all four languages on part 1 (run 2):** the Conlang Translator's prompt only offered "Vrakht" or "none (ordinary English)", so Haiku put the Japanese and Czech mutters into Vrakht ("the only other
tongue") and, since the Japanese pass then read the Vrakht text, the English was lost from the next prompt. Fixed in the plugin (`TranslationPrompt`: "none" for any other language than the one asked, real or invented); run 3 gives
"nothing to translate" for Vrakht and Aethel-Vael on part 1 and an English-only next prompt. A speech that two passes claim is still decided by whoever comes first (the nesting hazard stays possible).

## Where the build stands (2026-11-05)

`work/harness/` holds **run 3**: parts 1-5 with all five steps (generate, annotate, redraft, knowledge, translate) on Haiku, 29 calls, 400 s; the next run continues at part 6. Earlier attempts: `harness-run1..4`, `harness-fixed-loop`,
`harness-run5-annotate`, `harness-translate-fixed`. Changing the *Style* entry or a beat that is already built needs a fresh `home/` (keep `answers/` for the calls whose prompt did not change). Continue with:

```sh
tools/workset_run.sh -Dworkset.parts=20 -Dworkset.author=claude -Dworkset.model=haiku -Dworkset.lengthFactor=0.9 > /tmp/workset-run.log 2>&1
```
(about 25 calls per 5 parts, 6.5 minutes per 5 parts; the Vrakht and Aethel-Vael passes carry the lexicon, ~5k tokens in, and only run on parts whose beat has them; the first part has all four as a smoke test.)
`tools/workset_check.py` reads the stored text, which now has the translations in it: it flags the Japanese as foreign text (to be changed to read the English source).
## Not done yet

**The harness does the whole loop of a part: generate -> annotations -> redraft -> knowledge -> translations** (checked with the fixed author: a `GENERATE` and a `REDRAFT` revision, the annotation `RESOLVED`, the redrafted text in the prompt of the next part). Knowledge and conlang/reallang follow when the plugins can run in the harness; see `TODO.DATASET.MD`, *The author's writing loop, per part*. Parts built before a step existed are rebuilt.

Summaries and the author's block-end step (Arc Outline import, chapter headings, a summary at the end of each chapter), annotation and redraft passes, the plugin passes (Knowledge Tracker while the
story is generated, the Vrakht, Aethel-Vael and Japanese translators), the statistics companion, `--redo`, a second author for the local reasoning model. The plan, the decisions and the
findings are in `TODO.DATASET.MD`.


## The book as a test fixture

`src/test/resources/workset/book.zip` (5.7 MB, the archive of the app with the picture) and `lorebook.json` are the export of the finished dataset (`work/harness/export`), kept whole
(prompts, versions, annotations, plugin data) so that tests have a book of a real size. `LargeWorksetTest` restores it once per class (about 5 s on an M4) and checks it; `ExportRoundTripCheck`
(`-Dworkset.roundtrip=true`) checks a fresh export of the harness the same way and prints the times. To replace the fixture after a new run, copy the two files from `work/harness/export/`.
The lorebook is imported first (a backup only links its lorebook, by a fixed uuid).
