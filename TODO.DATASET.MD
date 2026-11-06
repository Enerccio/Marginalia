# TODO.DATASET - a large test workset (book backup JSON)

## Goal

One complete, realistic **workset** for tests: a book with a lorebook, about 100 parts on the main branch, one small
branch, and summaries, all shaped like what the application itself produces when a model writes with the default
prompts. It is loaded from a **book backup JSON** (the format of `ManuscriptService.createBackup`) with
`BackupService.restoreAsNewManuscript`, so it goes through the real import path and also works through the UI backup import.

The same dataset is meant to be reused later for the **screenshots** of the manual (`screenshots/`, which today has its
own small Harry Potter dataset in `screenshots/dataset/*.mjs`). That is a later step, but it shapes the data now, see
[Screenshot reuse](#screenshot-reuse).

Decisions (made with the user):

- Format: book backup JSON, kept in `marginalia/src/test/resources/`.
- "Characters" = lorebook entries only (no Character Tracker plugin data).
- World: a **new original world**, decided: the isekai story **"Unregistered"** (a Czech cellar guide, Tomáš, who learned Japanese from novels, and a
  Japanese nurse, Mizuki, fall through a bricked archway in Prague into the Sennet valley, a province of the Vrakht-speaking Dominion; neither knows
  Vrakht). English is the narration, Japanese is their private language (real-language translator), Czech is his muttering (a second real language),
  Vrakht is the world (conlang translator), Aethel-Vael is the tongue of the valley people (the Lyrshara) (conlang, `marginalia/plugins/conlang-translator/aethel-vael.md`, a second conlang in the same book), Mizuki's broken English is left untranslated. Ending: they are registered under a Lyrshara ferrywoman's line,
  the way home opens once and is not used. The skeleton (bible, 31 lorebook entries, 100 beats with timeline, a 3-part branch) is in
  `work/dataset/outline.md` (+ `beats.json`, `lorebook.json`, built by `build_skeleton.py`; `work/` is excluded locally in `.git/info/exclude`; when the answers are to be committed, only the part meant for git moves out).
  **Names** come from Markov chains trained on the lexicon of each language (`work/dataset/names.py`; the author picked from the candidates, a model is
  bad at naming): Vrakht Steing (assessor), Paskal (conscript), Mangel (lieutenant), Frecht (commander), Kestim (inspector), Roid (surgeon), Fort
  Stein-krog; Aethel-Vael Saellen (ferrywoman), Aelmoria (hedge-witch), the river Lyrasil, the town Silvakhor, Kaelthal (the hollow). Tomáš and Mizuki keep
  their real names. The people are the Lyrshara (chain-free: lyra + shara, "water-companions"); their language is Aethel-Vael. The Dominion names, functional compounds of Vrakht roots: the valley Zint-karn (tax-land), the river Vat-veg (water-road), the town Zorg-ort (provisions-place), the people skag-folk (enemy-people).
- The data is produced by **running the application's real generation loop** (prompts built and saved by the app, summaries,
  annotations, redraft history and statistics written by the app's services); the story text is answered by Haiku through a
  bridge, the reasoning parts by the local reasoning model. Only the skeleton (bible, lorebook, beats, timeline) is written
  by hand. See [Pipeline](#pipeline-run-the-real-generation-loop-decided).

## What the file has to contain (from `createBackup`, `Defaults`, `screenshots/dataset`)

Verify every key against the code before writing the generator (`ManuscriptServiceImpl.createBackup`,
`LorebookServiceImpl.marshalLorebooks` / `marshalEntry`, `ChatMessage`, `Summary`); this list is from reading it once.

- Book: `uuid`, `name`, `description`, `published`, `tags`, `activeLeafUuid` (the leaf of the main branch), `extendedContent`
  (`pov`, `tense`, `style`, optionally the prompt templates; leave the templates out so the defaults from `Defaults` apply).
- Lorebook: `lorebook {uuid, name}` and `lorebooks[]` (flat list, root first; `uuid`, `name`, `enabled`, `scanDepth`,
  `description`, `tags`, `subbooks`, `entries[]`). Entry: `name`, `payload`, `comment`, `enabled`, `order`, `filtering`,
  `filteringMode`, `insertionMode`, `tags`, `negativeTags` (see `screenshots/dataset/lorebook.mjs`).
- Parts: flat `messages[]`, each with `uuid`, `parentUuid`, `tokenCount`, `wordCount`, `edited` and `extendedContent`
  (`instructions`, `sceneSetting`, `povCharacter`, `presentCharacters`, `response`, `modelUsed`, ...).
- Summaries: `summary {uuid, extendedContent {summary, summaryType, summaryMessageHash, ...}}` on the parts that have one,
  plus a few meta summaries, so the summary levels and the "summaries of the story so far" block are exercised.
- Branch: a second child of one early-middle part (parentUuid = that part), 2 to 3 parts long, not the active leaf.

## Content plan

Lorebook (about 25 to 35 entries): setting (always on), style (`BEFORE_USER_PROMPT`), 6 to 8 characters with name
keywords, places, factions, a few items and rules, 2 to 3 entries with `filteringMode` other than TEXT (regex / tags if the
code offers them), one disabled entry, one entry restricted by `fromMessage`/`toMessage`. Aim to exercise activation, not
just count.

Story: a story bible (setting, cast, tone, 3 to 4 arcs over 100 parts) and a **one-line beat per part**. Each part gets
what an author types into the New Turn panel: scene, POV character, characters present, 1 to 3 sentences of
instructions. Vary the POV, the location, who is present, and the part length (about 250 to 700 words), so the data is
not uniform. The text must follow the default prompt constraints (third person limited, past tense, no new named
characters beyond the bible, no headings or meta text, only story text).

## Pipeline: run the real generation loop (decided)

The dataset is **not assembled from text, it is produced by the application**. A build harness runs the real services (the
same `generateNextTurn`, summary, annotation and redraft code the UI calls) against a **bridge** that answers with a real
model instead of a fake one, and the dataset is the backup of the book that came out. So the stored prompts, lore usage,
token counts, hashes, revisions and statistics are written by the application itself, not imitated by a script. The prompt that
the author model (Haiku) gets is the prompt the application built; there is no second, hand-made prompt, and it is saved with
the part exactly as in a real book (`MessagePrompt`: `builtPrompt`, `backgroundLore`, `loreUsage`; in the backup under the
part's `extendedContent` keys `builtPrompt` / `backgroundLore` / `loreUsage`).

**The pieces**

- **Harness** (a build tool, not a routine test: a JUnit class that only runs with a flag, e.g.
  `mvn test -Dtest=WorksetBuilder -Dworkset.build=true`, or a `main` in the test sources; extends `GenerationTestBase`, own test home).
  It creates the user, the AI and protocol (pointing at the bridge), imports the lorebook, creates the book with its settings
  (default templates, the defaults of `Defaults` stay as they are), then walks the skeleton.
- **Bridge** = a `MockLLMScenario.respondWith(request -> ...)` responder (it exists in `MockLLMServer`). It receives the complete
  request the pipeline built, writes it to `work/prompts/NNN.json`, asks the author model, returns the answer (and the
  reasoning, where there is one, so the pipeline stores `responseReasoning` itself; check that `MockLLMResponse` can carry it).
  Author models behind the bridge:
  - **Haiku through the Claude Code CLI** (`claude` is installed here): one non-interactive call per request, system prompt
    and user message as the application built them, no tools, one turn. Needs no API key. Check the flags that replace the
    CLI's own system prompt and switch tools off, or the answers will be shaped by it.
  - **The local reasoning model** (`tools/LocalLlm.java --reasoning`) for the 10 to 15 parts and 2 to 3 summaries that must have
    reasoning; its thinking comes back through the bridge into the part. Never fake reasoning with Haiku.
  - Alternative if the user has an API key: register Haiku as a real AI through the `anthropic` plugin and let the application
    call it itself (most authentic, and extended thinking would give real reasoning); not needed for the first version.
- **Per part:** the harness fills the New Turn fields from the skeleton (scene, POV, present characters, instructions) and calls
  `generateNextTurn`; the pipeline runs its steps (prepare -> constants -> lorebook -> content -> payload -> new message ->
  inference -> cleanup) and saves the part with its prompt. Summaries and the meta summaries are made by the application's
  summary service through the same bridge (so `summaryMessageHash`, types and chaining are the application's). The branch is a
  second `generateNextTurn` from an earlier part, a swipe is a regenerate (`REGENERATE` revision).
- **Workflow passes** run the application's own services as well: auto-annotate and redraft (`REDRAFT` revisions, annotations of
  source `AI`), simulated author annotations with `AnnotationService`, and so on (see the workflow section). The plugins'
  passes need the plugins loaded, which the Spring-only test context does not do: see the open questions.
- **Time (shared clock).** Today only `ManuscriptStatisticServiceImpl`, `ManuscriptSizeServiceImpl` and `ActivityTrackerImpl`
  have a settable clock (each its own); everything else reads the real time with `new Date()` / `System.currentTimeMillis()`
  (`JpaBaseRepository` for `creation`/`modification`, the bulk `now` updates of the repositories, `request` / `ttft` /
  `reasoningEnd` in `GenerateNewMessageStep` and `InferenceStep`, usage and AI-limit periods, summaries). Plan: one shared
  `Clock` (default = system clock, production unchanged) that those three and these places read; a small, separate change of
  about 15 production files, done before the harness. The harness sets the day of the timeline before each part, and the
  bridge advances the clock by realistic amounts (time to first token about 1 s, then the generation time), so the
  `GENERATE` intervals and `ttft` are plausible. Not a SQL pass afterwards: `request` and friends live in the
  `extendedContent` blob, saving again moves `modification`, and usage periods would be missed.
- **Dates in the companion.** `createBackup` writes no `creation` / `modification` of parts (only what is in the blob), so after
  a restore they are the restore time. The companion therefore has two exports from the harness database: the three
  statistics tables, and the **dates per uuid** of the parts, summaries, annotations and revisions (check what a restore keeps
  of revisions and annotations). The loader re-applies them after the restore, and for the screenshots shifts everything by
  (today - last day of the timeline).
- **Cache and rebuild:** every model answer is stored by the hash of the request it answered (`work/answers/`). A rerun
  replays the answers and calls no model, so the same JSON can be rebuilt offline and byte for byte; when a prompt template
  or the lorebook changes, only the requests that changed miss the cache. Commit the answers (compressed): they are the
  source of the text, the JSON is derived from them.
- **Sequential, not parallel.** Each part's prompt contains the story so far (summaries and recent parts), so part N waits for
  part N-1; about 100 parts at 30 to 90 s each plus the summaries is one to three hours in total, spread over the gradual runs
  below (the local-model parts one at a time as well). What can run in parallel is only what does not feed the story: drafting the
  lorebook entries, the Knowledge Tracker extractions of finished parts.
- **Size.** Stored prompts carry the context, 30 to 60 KB each, so the fixture with prompts is several MB; keep it
  compressed in `src/test/resources` and decide whether `BackupService` can read it as is or a test helper unpacks it.
  The example book package leaves the prompts out.

**Gradual runs and usage tracking (decided with the user).** The generation is not one long run. The harness works in **runs
of 5 parts** and then stops and waits for the user's evaluation before the next run, because the Haiku usage is to be tracked
and, if it is too high, the author model is switched to the local model (slower, no limit):

- **Resumable.** The harness database and the answer cache persist between runs (`work/`); a run continues from the next
  part of the skeleton, with the clock where the last run left it. `--parts 5` (default 5) is how many parts to do, `--until N`
  and `--stop-after-summaries` are the other stops. Nothing is regenerated unless the user asks (`--redo N`, which drops that
  part and everything after it, or deletes only the cache entries whose request changed).
- **Usage ledger** (`work/usage.jsonl`, one line per model call): time, part/step (part, summary, meta summary, annotate, redraft,
  tracker...), which model, request size (characters and tokens as the application counts them), answer size, the
  usage the author model reports (input / output tokens, cached tokens, cost if the CLI prints it, duration; check what
  `claude -p --output-format json` returns), and whether the answer came from the cache. After every run the harness prints a
  table: this run, cumulative, average per part, and the **trend of the input size** (the prompt grows with the story, so the cost per part
  rises; the summaries are what keep it bounded), plus a projection to 100 parts and to the whole workflow.
- **Pluggable author per kind of call.** The bridge chooses the model by a setting per step (`part=haiku`, `summary=haiku`,
  `reasoning-parts=local`...), changeable between runs, so moving only the expensive kinds (long context parts) to the local
  model, or everything, needs no code change. A part written by another model gets its own `modelUsed`.
- **Evaluation stop.** After each run the harness writes `work/review/run-N.md`: the 5 new parts (text, the instructions they
  were given, the prompt size, the lore that was activated, the summary) so the user can judge quality, continuity and
  usage together. The first run is the dry run of 3 to 5 parts described below; if the style or the prompts need to change, the
  affected parts are re-run and the cache entries of changed requests are dropped.
- **Budget guard.** A configured ceiling (tokens or calls) per run: the harness stops cleanly when it would be crossed, before
  the next call, and does not start another run by itself.

**Steps**

1. **Skeleton (me):** story bible, lorebook entries, the 100 beats with their New Turn fields **and their day/time on the
   timeline** (see [Timeline](#timeline-a-few-months-of-history-for-the-writing-statistics)), written to a scratch folder as a
   reviewable `outline.md` plus the machine-readable list the harness reads. Nothing is generated before the user has
   approved the story.
2. **Harness** that runs the loop with a fake responder first (fixed text) to prove the whole chain, the dates and the export.
3. **Bridge to Haiku** and the first run of 5 parts; the user evaluates quality and usage, the skeleton and prompts are fixed,
   the cache of what changed is dropped.
4. **Runs of 5 parts**, each followed by the user's evaluation, until the 100 parts are done (the model of any kind of call
   can be switched to the local one between runs); then the workflow passes, the summaries and the plugin passes, in the order of
   the workflow section, also in short runs.
5. **Export:** `createBackup(includeHistory = true)` of the book, the statistics tables, the language file; then the
   [checks](#checks-on-the-data-itself) and `LargeWorksetTest` (restore the file as a new book, assert 100+ parts on the main
   branch, the branch, the lorebook entry count, the stored prompts and lore usage, summaries not stale, and that a backup of
   the restored book matches the file in structure). Also open it once in the UI.

## Checks on the data itself

- JSON parses, uuids are unique, every `parentUuid` exists, exactly one root, the active leaf is reachable.
- No part is empty, no part still contains the template markers (`{{`), headings, "Here is the next part" type remarks,
  or text of the prompt; length spread is as planned; names used appear in the bible or the lorebook (a quick script check).
- Re-run only the batches that fail; the others are kept.

## Workflow passes on some parts (annotations, redraft, Knowledge Tracker, conlang)

Some parts are not "just written": they went through the app's workflows, so the fixture holds what those leave behind.
Every pass is run by the application's own services in the harness (see the [pipeline](#pipeline-run-the-real-generation-loop-decided)),
so the prompts are the app's (`Defaults.DEFAULT_ANNOTATE_*`, `DEFAULT_REDRAFT_*`, the plugins' prompts) and the author model
(Haiku, or the local model for the few reasoning ones) only answers through the bridge; the author's side of the cycle
(accepting, rejecting, editing, dismissing) is simulated by the harness. Nothing here needs a running model at test time. The Character Tracker stays out (earlier decision); the **Knowledge Tracker** and the **conlang
translator** are different plugins and are in. Fixture and tests that use these parts need those two plugins loaded
(the plugin tests / the screenshot environment), so keep their data in clearly named sections that the core tests ignore:
plugin data in the book's `attributes` is just JSON for the core app, it restores and travels with backups without the plugin.

1. **Annotation cycle (simulated), about 8 to 10 parts.** Annotations (`Annotation`, in the backup as `messages[].annotations`):
   range + quote of a passage, `text`, `type`, `label`, `sendToPrompt`, `source` (`AUTHOR` / `AI` / `AGENT`) and `status`
   (`OPEN`, `RESOLVED`, `DISMISSED`, `ORPHANED`). Mix them: author notes, AI notes, some open, some resolved (and the text
   changed accordingly), one dismissed, one orphaned (its quote no longer in the text), a few with `sendToPrompt` so the
   next part's prompt has the "AUTHOR'S NOTES" block. Where an open note has `sendToPrompt`, the **next part is written
   with that note in its prompt** (Haiku gets the note, as the app would send it), so the story visibly reacts to it.
2. **Annotate and redraft (the auto-annotate workflow), about 5 to 6 parts, with history.** For each: version 1 = the part as
   written (also `originalResponse`), then the model's annotation pass (`source` `AI`, from `DEFAULT_ANNOTATE_PROMPT` with the
   `DEFAULT_ANNOTATE_SYSTEM_PROMPT`), then the redraft (`DEFAULT_REDRAFT_PROMPT` + the accepted annotations) giving the final
   `response`. The history is `MessageRevision` rows (the backup carries them only with `includeHistory`, so the fixture is
   built with it): `GENERATE` -> `REDRAFT` (-> a `HUMAN` edit on one of them -> `AUTOMATIC`/`RESTORE` on another), each with
   `source`, `sourceDetail`, `label`, `wordCount`, `tokenCount`, `response` and its date from the timeline, so "History" and the
   version comparison have something to show. Book settings for it: `autoAnnotate`, `autoAnnotateInstructions`,
   `autoAnnotateRedraft`, `autoAnnotateAccept(Redraft)` on, and `historyMaxVersions` set. Include one part where the redraft was
   rejected (annotations stay open, no REDRAFT revision) and one where the author edited the redraft.
3. **Knowledge Tracker pass saved.** *For this story the tracker has real work (secrets and language):* the tracked characters are Tomáš, Mizuki, Saellen,
   Aelmoria, Steing, Mangel and Paskal; what a character does **not** know is the point: the pair do not understand Vrakht until part 26-60, and not
   Aethel-Vael at all; Tomáš does not know the reed-door (part 41 tells Mizuki), Steing's false columns are known to the Lyrshara and to Steing, not yet
   to the pair (part 49 / 61) or to Mangel (part 72); who has met the inspector (part 76 on). Items: the tally tokens, the phone, the notebook. The
   cursor stays at part 60. The idea of a character learning a language is also an open item of the conlang plugin (its `TODO.md`, *Later / ideas*).
   **The language levels are not lorebook entries any more** (they were three `BEFORE_USER_PROMPT` entries limited by part number, the author's remark:
   that is what the tracker is for). The start state and the changes the story should produce (one fact per entry, knows or does not know, plus who has met whom; 51 items: the 11 of part 1 are the **start state, entered by hand**, the rest is what the model's extraction **should** produce, a check to review, never inserted) are in `work/dataset/knowledge_seed.json`. **Consequence:**
   the tracker has to run **while the story is generated** (extract after each part, so the next prompt carries "does not understand Vrakht" and
   then "basic Vrakht"), not only afterwards for the saved pass. That is the open question *Plugin passes in the harness* (below), now blocking and
   adding one extraction call per part; if it cannot be solved, the three entries are kept in the skeleton as `FALLBACK_LANGUAGE_ENTRIES` and go back
   into the lorebook. The range-limited entries that stay (`fromMessage`/`toMessage`: the Prague cellar, the phone and watch, the tally token) still
   exercise that feature.
   Original description of the pass follows. The plugin keeps its state in the book's attributes under
   `com.github.enerccio.marginalia.extensions.knowledgetracker` (`KnowledgeState`: `settings`, `characters` (tracked characters
   with aliases, items, met people, per-part changes), `startMessage`, `cursor`, `processed` (part uuid + hash of its text),
   `partsSinceTidy`, `changeLog`). The fixture has the tracker run over the first **N parts (say 60 of 100)**: the cast from the
   lorebook, what each character knows/has/met after those parts, a `cursor` at part N, `processed` with the **real hashes of the
   final texts** (computed by the assembly script, the same way the plugin does it, or the tracker shows them as changed), one tidy
   already done, a change-log with a few automatic changes. The extraction text is written by Haiku from the plugin's own
   extraction prompts over the finished parts (batch by batch, like the tracker does: earlier parts as context), and the
   assembly script checks that every character/place named exists in the lorebook and that no change refers to a part after the cursor.
   Parts after the cursor are "not processed yet", so a test or screenshot can run the next step.
4. **Conlang pass (Vrakht).** `marginalia/plugins/conlang-translator/vrakht.md` holds the language (grammar, particles; spelled
   *Vrakht* in the repo; use that spelling and tell me if "vrakth" was meant to be a different language). Plan: the story has a
   people who speak it (an orc-like or otherwise harsh culture in the original world; **this ties the world to the language, so it
   is a constraint for the user's story idea**; decided: the whole world speaks it, so **about 89 of the 100 parts** have Vrakht speech, all translated - a
   long pass with the local model, run in short batches), translated by
   the plugin's own flow (rules from `vrakht.md` as the language `rules`, `audience`, the glossary built up by the designer),
   with the plugin data for them. Where the plugin keeps it (to confirm in `store/`, `service/BookConlang`, `ConlangStore`): the
   **language definition lives in the user's library** (`UserSetting.attributes`), **not in the book**, and the book/part carry the
   assignment and the saved run (`book.attributes`, `ConlangStore.readRun`). So the fixture needs a **language file** (the
   plugin's own import format, `"format": 1`) next to the book backup, and the loader imports it before the book. **It is not
   written by hand: `marginalia/plugins/conlang-translator/tools/vrakht_to_json.py` already converts `vrakht.md` into that JSON**
   (`python3 tools/vrakht_to_json.py -o vrakht.json`); the assembly script runs it (or the generated file is committed with the
   fixture and a test checks it still equals the converter's output). The Vrakht parts are produced with the plugin's own
   translation flow over that imported language (not a separate prompt of mine), and the saved run in the book comes from it. Check the translated parts: Vrakht words come from the rules/glossary only (a script
   check against the glossary), names are kept, and a back-translation or review pass is saved for 2 to 3 of them.
5. **Order of work for these parts:** write the part (Haiku/local) -> annotate -> redraft -> (conlang translate) -> summarize
   the **final** text -> Knowledge Tracker extraction over the final texts. The summaries and hashes must be made from the final
   texts, never from version 1. The assembly script checks all of this ordering and the hashes.

Validation additions: the restored book shows the annotations in their statuses, `includeHistory` revisions are restored and
dated in order, the Knowledge Tracker state loads with the plugin (cursor, processed hashes match the texts), and the
Vrakht parts translate/validate against the imported language. If a plugin changes its data format, `--stamp` aside, the
loader test is what fails first: keep it.

## Example book (downloadable package, the story itself)

A lighter output of the same build, for new users: the story as an example book to import, not the test data around it.

- **Package:** one download (a zip, or two files side by side) with the book backup JSON, `vrakht.json` (the language, from
  `vrakht_to_json.py`, see the conlang pass) and a short README (how to import: Settings -> Extension Settings -> Conlang
  Translator -> Import for the language first, then Book -> Import backup; which plugins show which parts of it) plus the licence.
- **Contents:** the finished text, lorebook, summaries, the branch, annotations and the redraft history (they show the features);
  the Knowledge Tracker and conlang data stay in (inert JSON without the plugins). **Left out:** the statistics companion,
  stored prompts, and the links to providers/protocols (a fresh install has none and the import must work without an AI).
- **Licence and origin:** the text is original and written by AI models; the book `description` and the README say so and give
  the licence: **CC0 1.0** (decided). Only possible because the world is original; the package includes the CC0 notice
  and the README says the text may be used for anything. Check that nothing in it is third-party (no borrowed names, lore or
  text), and that `vrakht.md`, the language it converts, is the user's own work and covered by the same dedication.
  **The picture** (part 31, `work/dataset/cover.jpg`, attached by the harness) is not covered by it: *Interior of a Cottage in Scheveningen* (1887) by
  Willem Adriaan Alexander Liernur, public domain, from Rawpixel (https://www.rawpixel.com/image/13744617/image-face-person-art); the caption of the
  image says so and the README of the package must repeat it.
- **Where it lives:** first as a download in the manual (next to the pages; `tools/retype_build.sh`, and `docs/` is committed
  with every `manual/` change); a "Add example book" button in the app (bundled resource, 3 languages, docs, changelog) only later.
- **Test:** the package imports into a fresh installation with no AI configured, with and without the two plugins, and the
  imported book opens with the expected number of parts, the branch and the lorebook entries.

## Timeline (a few months of history, for the writing statistics)

The statistics (`ManuscriptStatistic`, `ActivityInterval`, `ManuscriptSize`; `manual/developer/domain-model.md`, "Writing
statistics") are core, not a plugin. **A backup carries no statistics** (and `restoreBackup`, imports and clones count nothing),
so the book JSON alone gives one day of history. The dataset therefore has two parts that share one **timeline**:

1. **Timeline in the book JSON.** The skeleton gives every part a day and a time, spread over about **3 to 4 months**
   (e.g. 14 weeks): irregular, not one part per day. Some days have 3 to 5 parts, some weeks none (a gap that breaks a
   streak), a few weekend binges, one long pause. Put the time into what the backup keeps (`request` / `ttft` /
   `reasoningEnd` in the part's `extendedContent`, summary dates if any) and, if `createBackup` can carry them
   (`includeHistory` revisions with their own dates), a few edited parts whose edits fall on later days than the creation.
   **Check first** whether `restoreAsNewManuscript` can keep `creation` / `modification` of the parts; if it cannot, the dates
   live in the companion below and in `request`, and tests that need `creation` order rely on the order of insertion.
2. **Statistics companion** (a second file, `workset-statistics.json`). **It is not computed by a script: the harness runs
   the real loop with the clock of `ManuscriptStatisticServiceImpl` (and the activity tracker) set to each timeline day, so the
   application writes the rows itself**, and the companion is an export of the three tables of the harness database:
   the nine counters of `StatisticCounts` per day and side, `ManuscriptSize` rows, and `ActivityInterval` rows (`OPEN`, `EDIT`,
   `GENERATE`, with start/end times inside the day; the harness opens and closes the activity around each part and adds the
   editing stretches of the annotate/redraft/edit passes). Because the code made them, they **add up to the book** by
   construction; a script check still verifies it (words per day = words of the parts of the day, parts created = parts of
   the day, the size after the last day = the real size of the restored book, the deleted/removed counters = the few deleted
   parts and the swipe/branch), because a wrong clock setting or a missed `touch` would show up there.
   - **Tests:** a helper loads the companion through the services with the clock of `ManuscriptStatisticServiceImpl`
     set to each day (the way the statistics tests do), or inserts rows directly; no `Thread.sleep`, no real time.
   - **Screenshots:** a loader like `screenshots/bin/seed-statistics.py` writes the rows with SQL into the screenshot database
     after the import (run with the app stopped), and the days are counted from "today" so the calendar ends on the
     present; the companion stores day offsets from the last day, not absolute dates, for that reason (tests can fix "today").
3. **Shape it for the windows:** the current streak and the longest streak (different), at least one day that reaches a daily
   goal of 500 words and one that does not, a busy day and a quiet one, a month with a clear trend (so the chart is not flat),
   and the earliest weeks marked `reconstructed` with no time (like an installation upgraded in the middle of the period)
   in one variant of the loader, to exercise the `~` marks and the dash for the days before `statisticsSince`.

Checks added for it: the sum rules above, no day in the future, every `ActivityInterval` inside one day and `start < end`,
and a test that the statistics report for the restored book + companion shows the expected days, totals and streaks.

## Screenshot reuse

Not part of the first delivery, but keep these in mind so the file does not have to be redone:

- **One source of truth.** The screenshot flows import the same backup JSON (Book -> Import backup in the UI, or
  `restoreAsNewManuscript`) instead of typing turns into the New Turn panel; the fake model (`screenshots/llm/rules.mjs`)
  still answers the few turns that are generated live. The flows that today use `dataset/story.mjs` keep working until
  they are moved over one by one; do not delete the old dataset in this task.
- **Text must be publishable.** Original and family friendly (the screenshots go into the manual and `docs/`), no real
  people, no fan-fiction names. The user's choice of an original world already fits this.
- **Screenshot-friendly shape.** Book name and description that look good in the list, a few tags on the book and lorebook
  entries, entries with different insertion modes and keywords, summaries on some parts, a meta summary, a second version
  of one part (Swipe) and the small branch, one edited part. Parts of mixed length so the tree and the text look natural.
- **Stable uuids and dates.** Derived from the part number, with fixed creation dates (spread over some days), so the pictures
  do not change between rebuilds of the file.
- If a screenshot flow needs data the test fixture does not (images, a second user's book, statistics), it is added by the
  flow on top of the import, not baked into the shared file.

## The author's writing loop, per part (decided with the user, 2026-11-05)

The real loop of the author, for **every** part, in this order: **generate -> run the annotations -> redraft with the annotations -> knowledge (Knowledge Tracker) -> conlang processing
(Conlang Translator and Real Language Translator)**. The harness must do the same, in the same order, part by part, because the next part is written from the *result* of the loop: the redrafted text, the author's notes of the
annotations and the knowledge block are in its prompt, and the stored prompts, the revisions (`GENERATE` then `REDRAFT` for every part) and the annotations come out as in a real book. So the
earlier idea of "annotate and redraft about 5 to 6 chosen parts, translate afterwards in batches" is replaced: all parts go through the loop, a few with a different outcome for variety (a redraft
rejected, an annotation dismissed or edited by the author, a swipe), listed in the skeleton's `WORKFLOW`.

- **Generate:** done (`WorksetBuilder`). **Annotate and redraft: done in the harness** (2026-11-05, checked with the fixed author), with the author's own *What to look for* (POV drift, continuity errors, style errors) as the book's annotation instructions.
- **Annotate and redraft** are core services; the order is driven by the dialogs of the story view (`AutoAnnotate`, `AnnotateDialog`, `RedraftDialog`), which cannot run in the harness, so the harness calls
  what they call: `AnnotateService.annotate` + `accept`, `AnnotationService.setStatus`, `RedraftService.redraft`, with the book settings of the author (`autoAnnotate`, `autoAnnotateRedraft`,
  `autoAnnotateAccept...`). Doable without plugins. The bridge must tell the kinds of request apart (part, annotate, redraft) for the ledger.
- **Knowledge, conlang and reallang** are plugins: they need the shared way to run plugins in the harness (below). Calls per part: about 5 to 7 (generate, annotate, redraft, extraction, one to three
  translations), so 500 to 700 calls for the book, of the order of 2 to 3 hours of Haiku in total, spread over several sessions because of the hourly limit of the plan.
- Parts 1 to 5 built so far are generate-only and will be rebuilt once the loop exists.

## The author's workflow at the end of a block (decided with the user, 2026-11-05)

The summaries and chapters are not made "every few parts": they follow how the author writes. A **block is an act (25 parts)**; after each block (parts 25, 50, 75, 100):

1. **Arc Outline, *Import from story*** over the block's parts (the plugin's own flow, defaults, chapters linked to the parts they begin in). The **arc name is ignored**, only the chapter
   names are used. The arc and its chapters stay in the outline as *Finished*, with the request and reasoning the plugin stores.
2. **Chapter headings in the text:** each chapter's first part gets a heading `# <roman numeral>. <name>` as its first line. The numbering is **continuous over the book (I to N), not per
   arc**. The Chapter Marker plugin then shows a table of contents. The heading is an edit by the author: the part is marked **edited** and its history gets a `HUMAN` revision (this
   also provides the "edited parts" the workflow list asks for).
3. **Summaries:** a summary at the **last part of each chapter**, except a very short chapter (no summary), and **not for the last chapter of the block**: it is kept whole, so the next block
   starts with real messages in the prompt. The summaries are made from the final text (after the headings).
4. The next block is written with those summaries in the prompt. After part 100 the book ends with its last chapter whole, like a book in the middle of writing.

This replaces "meta summaries after 25/50/75" and the idea of a summary stage every few parts. Meta summaries stay optional for later. The block-end step needs the Arc Outline plugin in the
harness, with the Knowledge Tracker and the two translators: one shared way to run plugins in the harness (decided: no OSGi; the plugins' compiled classes on the harness classpath and `onExtensionLoad` called by hand, then drive the steps through their commands; see the harness README) is the next piece of work after the first real run. Timeline: the block-end step is done on the day after the last part of the block.

## Harness status (2026-11-05)

How the harness is organised, how to run it, what each file in `work/harness/` is: `marginalia/src/test/java/com/github/enerccio/marginalia/workset/README.md`.

`marginalia/src/test/java/com/github/enerccio/marginalia/workset/` (`WorksetBuilder` and its parts) is written and runs: `mvn test -Dtest=WorksetBuilder
-Dworkset.build=true [-Dworkset.parts=5] [-Dworkset.until=N] [-Dworkset.author=fixed|claude]` (work folder `work/harness`, skeleton `work/dataset`).
Done and checked with the fixed-text author: the real generation loop over the skeleton, a **resumable** build (the database stays in `work/harness/home`, the user, AI,
protocol, lorebook and book are found again, the next part is the length of the active branch plus one; a second run continued at part 6), the answer cache by request
hash, `prompts/`, `usage.jsonl`, `review/run-N.md`, `export/book.json`, the timeline through the shared `AppClock` (request and ttft carry the day of the part), the branch
(48 messages after 45 parts, one part with two children, the active branch intact). **Not done:** the `claude` author (written, never run), summaries and meta summaries,
annotation / redraft passes (done later), the plugin passes (Knowledge Tracker; the translators are done, see below), the statistics companion.
Style findings of the first real runs (Haiku through the `claude` CLI, 3 redos of parts 1-5, kept in `work/harness-run1`, `-run2`): left to itself the model wrote Japanese in romaji with an italic gloss,
a Czech thought in italics, indirect speech ("murmured something in Japanese", which no translator can pick up) and "he said in English" after every line; the *Style* lorebook entry (before the
user prompt) now says: all speech in plain English in quotation marks, never a foreign word or gloss, thoughts in English, muttering is quoted speech, and **the language is named only when it
changes**. Decided with the author: the cue stays in the narration after the translation (the plugin may cut it later, see the Real Language Translator's `TODO.md`, *Language cues in the
narration*). The asked length is multiplied by `workset.lengthFactor` (0.75 gave 311-430 words for ~450 asked; 0.9 for the next runs).
Found on the way: a **lorebook with tags, or an entry with a tag, is not activated** unless the book carries the tag, and then nothing of the lorebook reached the prompt; the
skeleton has no tags on the lorebook or its entries for now (when tags are wanted for the screenshots, the book gets the same tags).

**Plugin host, knowledge step and translation pass (2026-11-05, done; Haiku run 3, parts 1-5):** `PluginHost` loads plugins from the classpath and calls `onExtensionLoad` by hand (stub bundle and OSGi service); `TranslationPass` sets up
the four languages (Vrakht and Aethel-Vael by import into the Conlang Translator, Japanese and Czech in the Real Language Translator, with speakers) and translates each part through the plugins' commands after
the redraft, only the languages the beat has. Checked with the fixed author and on Haiku (Japanese, Czech and the knowledge step over parts 1-5; Vrakht and Aethel-Vael only as a false-positive test on part 1, which found and fixed a prompt bug in the Conlang Translator; no real Vrakht or Aethel-Vael translation has been seen yet). The knowledge step is written too (`KnowledgePass`; checked on Haiku over parts 1-5, run 3: start items entered by hand, the block in the next prompt). Arc Outline loads in the host, its block-end step is not written.
Details and commands: the harness README, `tools/workset_run.sh`.

## Open questions

- ~~What the story is about~~ Decided (see Decisions). Step 1 (skeleton) is written; it waits for the user's read of `outline.md`.
- **Language set-up of the book:** Japanese = only Mizuki and Tomáš; Czech = only Tomáš; Vrakht and Aethel-Vael through the conlang plugin (19 parts have Aethel-Vael speech, all of them also Vrakht). `tools/aethel_vael_to_json.py` had a missing comma (line 25, fixed); it now gives 247 words, 5 rule sections, 16 categories and warns that `Vaistas` (line 208) disagrees with the rule `vaisthas`. The language file has exactly one vulgar entry (`Tits`, the slang-register example, line 193) besides plain body and action words; decided: the file and the dataset's copy keep it unchanged (it is the register example of the plugin; the story simply does not use it). Not tried on a real model:
  a speaker who uses broken English inside a Japanese scene (the prompt classifies English as `none` and lets the text override; parts 1, 2, 3, 12, 17
  are the test), and the known limit that the Japanese pass sees the other plugin's Vrakht (see the reallang TODO).
- **Plugin passes in the harness.** The Knowledge Tracker and conlang flows are plugins (OSGi), and the Spring-only test
  context loads no extensions. Options: (a) run those passes in the screenshot environment (the full application with the
  plugins, pointed at the same bridge) and merge the result into the book; (b) call the plugins' services directly from a
  test in the plugin's own module (they are plain classes with Spring services); (c) load the plugin classes into the harness
  without OSGi. Decide after reading how the plugins' tests set themselves up (`KnowledgeStateService`, `BookConlang`).
- **Author model through the bridge.** Confirm the `claude` CLI can be run non-interactively with its own system prompt
  replaced, no tools and no extra context (so the answer is shaped only by the application's prompt), and that the cost/limits of
  about 150 calls (parts, summaries, passes) are acceptable. If not, fall back to the Haiku subagents driven through the
  prompt/answer files of the bridge (slower, one agent per request).
- **Entity dates.** Covered by the shared clock and the dates in the companion (see the pipeline); what remains to check is
  what `restoreAsNewManuscript` keeps of the dates of revisions and annotations, and that the activity tracker's own tick thread
  works with a manually driven clock.
- **Size and format of the committed fixture** (stored prompts make it several MB): compressed resource, and whether the
  restore path accepts it directly; plus the committed model answers (`work/answers/`) that make the build repeatable.
  Default: commit both, because tests must run offline.
- Do we also want a second, smaller variant (20 parts) for slow tests? Cheap to cut from the same data.

## Follow-ups when done

- Mention the dataset in `manual/developer/testing.md` (how to load it, how to rebuild it).
- `python3 tools/check_dev_docs.py` after the doc change; no `CHANGELOG.MD` entry (internal test data).
