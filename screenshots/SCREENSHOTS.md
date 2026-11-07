# Screenshots of the manual

This folder is a **scripted environment that takes every picture of the manual** (`manual/images/`) from the same
dataset: its own Marginalia installation with its own data folder, a fake inference server instead of a model (so no API
key and no network), and headless Google Chrome driven by Playwright. Nothing here touches `~/.marginalia`.

The text is written for an agent (or a person) who has to retake pictures: what exists, how to start it, how to get back to
an earlier state, and what each script does. Read [Rules of the game](#rules-of-the-game) before you change a script.

```
screenshots/
  SCREENSHOTS.md      this file
  package.json        playwright-core (the browser is the installed Google Chrome, nothing is downloaded)
  bin/                shell scripts: env.sh, start-app.sh, stop-app.sh, start-llm.sh, stop-llm.sh,
                      snapshot.sh, restore.sh, refresh-plugins.sh, rebuild.sh (+ chain.txt), shoot-all.sh, publish.sh,
                      make-broken-plugin.py, list-images.py, seed-statistics.py, seed-conlang.py, shoot-conlang.sh
  flows/              the Playwright scripts (NN-name.mjs = a step of the setup, shoot-*.mjs = only pictures)
  lib/                shot.mjs (browser, login, dialogs, snap helpers), story.mjs (story editor helpers)
  llm/                mock-llm.mjs (fake OpenAI compatible server) and rules.mjs (what it answers)
  dataset/            the content: story.mjs, lorebook.mjs, characters.mjs, plugins.mjs, stchat.mjs (a SillyTavern chat)
  assets/             cover.jpg (the painting, taken out of the test fixture) and portrait.jpg (a crop of it), made by bin/fetch-assets.sh (public domain, credited in manual/credits.md; not in Git)
  steps/              step backups: <name>.tar.gz of the whole data folder after each step   (not in Git)
  final/              the final state: marginalia.sqlite, the data folder as a tarball          (not in Git)
  .data/              the data folder, used as -Duser.home, so the data is in .data/.marginalia (not in Git)
  .run/               pid files, logs of the app and of the fake model, request log (not in Git)
  out/                the pictures that were taken, before they are copied to the manual   (not in Git)
```

## Requirements

* the WAR built from the **same commit** as the plugins: `mvn package -DskipTests` in the repository root builds the
  application and all plugins (`marginalia/target/marginalia-1.7.0.war`, `marginalia/plugins/*/target/*-1.7.0.jar`). Use
  `mvn clean package -DskipTests -pl <plugin folders>` for the plugins when they were built before: an incremental build
  can leave a plugin without the AspectJ weaving (its `@Autowired` fields are then `null` and its dialogs fail with a
  `NullPointerException` in the log);
* Node.js 20+ (`npm install` in this folder, once), Google Chrome in `/Applications`, JDK 25 + Maven, `lsof`, `curl`;
* for `export-result.png`: `pdftoppm` (`brew install poppler`) and ImageMagick (`magick`);
* for the annotated UI maps: Pillow (`bin/shoot-all.sh` installs it into `.run/pil`).

Ports: the application runs on **8099** and the fake model on **8098** (`bin/env.sh`), so a development instance on 8080 is
not in the way. The application is started with `mvn jetty:run-war` of the built WAR (production mode, own `-Duser.home`,
and the user's `~/.m2/settings.xml` is passed with `-s`, because the real home folder is hidden from Maven).

## Accounts, providers and the dataset

| What | Value |
|---|---|
| Administrator | `admin` / `marginalia` (full name *Administrator*) |
| Regular user | `jana` / `marginalia` (*Jana K.*), has the shared provider, a token limit and the permission *Create own models* (the Add button of her Inference Providers tab) |
| Providers (admin) | **Local LLM** (shared, `http://localhost:8098/v1`, model `marginalia-demo-12b`), **Local Reasoner** (reasoning, `marginalia-demo-reasoner`), **Featherless.ai** (a hosted service, only listed) |
| Protocols | *Chat Completion* and *Creative Protocol*; defaults in *Settings*: Local LLM + Chat Completion, *No Backups* |
| Lorebook | *Unregistered - the valley of the Lyrasil* (31 entries, the lorebook of the dataset book of the tests, `dataset/unregistered-lorebook.json`), imported through `dataset/lorebook.mjs`; *Phrasebook* is created and deleted for the trash |
| Book | *Unregistered*, tags `portal fantasy`, `language barrier`, `slow burn` |
| Story | 5 parts on the main branch (+ a second version of #2 made with Swipe, + a branch from #4 written by Local Reasoner), summaries on #3 and #5 with a meta summary, a picture on #5, an edited sentence in #5 |
| Second book | *Night Shift* of `jana` (two parts, so the usage and limit screens have numbers) |

The key of the providers is the placeholder `demo-key`. The OpenAI client refuses an empty key (TODO.BUGS.md BUG-62, the
manual says to enter anything). The fake model ignores it.

The story is the first five parts of the dataset book *Unregistered* of the tests (`marginalia/src/test/resources/workset`, see its README):
`dataset/from-fixture.py` takes the parts, the lorebook, the book settings of the translators and two parts with translated speech from the fixture into
`dataset/*.json` (they are in Git, so the pictures do not depend on the test resources). What the fake model "writes" besides that (the second version
of part 2, the branch of the reasoning model, the summaries, the plugin answers, the book of `jana`) is in `dataset/`; the only third party
material is the painting in `assets/` (public domain, taken out of the fixture by `bin/fetch-assets.sh`).

### The fake model

`llm/mock-llm.mjs` (no dependencies) answers `GET /v1/models` and `POST /v1/chat/completions` (streaming SSE with usage,
reasoning in `reasoning_content`, or plain JSON). **What** it answers comes from `llm/rules.mjs`: the first rule whose
`when(ctx)` is true. The turns of the story are recognized by the text of their instructions, summaries and meta summaries by
their system prompt, the side query, the review, the lore generation and compaction the same way. The server restarts
itself when `llm/` or `dataset/` change (`node --watch`).

Control endpoints: `POST /_queue {"reply": "...", "reasoning": "...", "status": 401, "message": "...", "toolCalls": [{"name": "...", "arguments": {...}}]}` (a one-shot answer
that wins over the rules, used for `t2b` and for the error picture), `POST /_mode`, `GET /_log` (the last requests, also all in
`.run/llm-requests.jsonl`), `POST /_reset`. When a new kind of request shows up, look at the log, then add a rule.

## Commands

```sh
cd screenshots && npm install                     # once
bin/rebuild.sh                                    # the whole setup from the empty installation (about 30 minutes)
bin/rebuild.sh 13-db-backup                       # restore that step and run the rest of the chain
bin/shoot-all.sh                                  # all pictures that the chain does not take itself
bin/publish.sh --dry ; bin/publish.sh             # compare / copy out/*.png to manual/images/
bin/shoot-some.sh 13-db-backup shoot-styles.mjs      # restore a state and run only those flows (a fixed or a new one)
bin/restore.sh 13-db-backup                       # go back to a step (stops the app, replaces the data folder, starts the app)
bin/snapshot.sh <name>                            # save the current data folder as steps/<name>.tar.gz (stops and starts the app)
bin/refresh-plugins.sh                            # swap the plugin JARs of the data folder for the freshly built ones (app stopped: restore.sh <step> --no-start first)
bin/start-app.sh | bin/stop-app.sh | bin/start-llm.sh | bin/stop-llm.sh
node flows/shoot-book.mjs --only turn-details     # one part of a shooting script
python3 bin/list-images.py                        # the table of all pictures below
```

The step snapshots keep the plugin JARs of the day the step was made, which are older than the build after any plugin change
(the plugin pictures then show old screens). `bin/shoot-all.sh` (after every restore) and `bin/rebuild.sh` (after the first one)
therefore run `bin/refresh-plugins.sh`, which replaces them by `marginalia/plugins/*/target/<name>-<version>.jar`; build the plugins first.

`bin/snapshot.sh` and `bin/restore.sh` stop the application first, because SQLite keeps a write-ahead log: a copy of the folder
of a running application is not consistent.

Every flow starts a fresh browser, logs in and works through the real UI. Pictures are written to `out/<name>.png`; names
starting with `_` are scratch files. The version text in the bottom left corner (`BuildInfoFooter`) is hidden in all pictures. `SHOT_TOASTS=1` shows the toasts (they are hidden in all pictures, except the ones that
are about a toast).

## The setup chain (`bin/chain.txt`)

Each step is a flow plus the snapshot taken after it. `bin/rebuild.sh <step>` restores a step and runs everything after it.
A flow may also take pictures on the way (names in the last column).

| Step after | Flow | What it does | Pictures |
|---|---|---|---|
| `00-empty` | (a fresh installation) | | |
| `01-admin` | `00-first-start` | creates the administrator | `first-start`, `login` |
| `02-extensions` | `01-extensions` | installs the 15 plugin JARs (all but Arc Outline, Google Gemini and Anthropic Claude) through *Admin → Extensions* | |
| `03-providers` | `02-providers` | the three providers (fill *General Settings* first, see the quirks) | |
| `04-protocols` | `03-protocols-settings` | two protocols and the default settings | |
| `05-lorebook` | `04-lorebook` | imports the lorebook file | |
| `06-story` | `05-book` | creates the book, tags, lorebook, then writes parts 1-5 (+ Swipe) with the fake model | |
| `06e-edit` | `08-edit` | the user edits one sentence of the last part | |
| `06a-backup` | `backup-a` | first backup of the book (no pictures yet) | |
| `07-summaries` | `06-summaries` | summaries of #3 and #5, then a meta summary | `summary-dialog`, `summaries-dialog`, `meta-summary-*`, `summary-edit` |
| `08-images` | `07-images` | uploads the photograph, caption, a second unused upload (Resources) | `images-dialog` |
| `09r-reasoning` | `reasoning` | a branch from #4 written by Local Reasoner, then back to the main branch | |
| `09a-backup` | `backup-b` | per-book backup strategy and a second backup (with the picture) | |
| `10-users` | `09-users` | user `jana` (and her permission *Create own models*, given in her permissions window), her protocol and defaults, her book *Night Shift* | |
| `11-limits` | `10-limits` | a token limit for her, usage, her info dialog, a second part by her | `provider-limit*`, `provider-usage`, `provider-shared-user` |
| `12-trash` | `trash` | imports *Bestiary* and deletes it, creates and deletes *Scratch book* | |
| `13-db-backup` | `db-backup` | a manual database backup (**the state of all core pictures**) | |
| `14-characters` | `plugin-characters` | the cast of the Character Tracker | |
| `15-relationships` | `plugin-relationships` | the relationships between them | |
| `16-authorsnote` | `plugin-authorsnote` | the Author's Note of the book | `plugin-authorsnote` |
| `17-sidequery` | `plugin-sidequery` | two questions in Side Query | `plugin-sidequery` |
| `18-reviewer` | `plugin-reviewer` | a review of the last part | `plugin-reviewer*` |
| `19-lorebookvcs` | `plugin-lorebookvcs` | a second revision of the entry of Mizuki | `plugin-lorebookvcs` |
| `20-loregenerator` | `plugin-loregenerator` | a new lore entry made with Lore Generator (generation, compaction, the Tools window) | `plugin-loregenerator*`, `tools-dialog`, `tools-hint` |
| `21-chaptermarker` | `plugin-chaptermarker` | chapter headings in parts 1 and 4 (**the state of the plugin pictures**) | `plugin-chaptermarker` |
| `22-arcoutline` | `plugin-arcoutline` | installs the Arc Outline JAR (unless it is there), the first arc imported from the five parts of the story with the fake model, a second arc with chapters by hand, a third arc and its chapters made with the fake model (`arcOutline` in `dataset/plugins.mjs`) | `plugin-arcoutline-tab`, `plugin-arcoutline-menu`, `plugin-arcoutline-note`, `plugin-arcoutline-import`, `plugin-arcoutline-generate-arc`, `plugin-arcoutline-generate-chapters`, `plugin-arcoutline-generated` |
| `23-betareaders` | `plugin-betareaders` | installs the Beta Readers JAR, its settings and persona library, a group chat with two personas (the conversation, the reasoning, the actions of a message, edit/delete/regenerate, personas joining and leaving, the chat settings with another medium and a scene, a scene sent with **Send as scene**, the spoiler limit, the menu of the chats, the personas of the book, delete); the personas answer from the rule `beta-readers` of `llm/rules.mjs` | `plugin-betareaders-*` (settings, library, write-persona, tab, new-chat, chat, actions, joined, bring-in, chat-settings, scene, spoiler, menu) |

`99-final` (made by hand after `21-chaptermarker`, so without the Arc Outline of `22-arcoutline`; see [The final database](#the-final-database)) is the state at the end of the older chain.
The core pictures are taken on `13-db-backup`, **before** the plugin data (the chapter headings, the Author's Note, the
characters... would show in the outline, the prompt and the token numbers of the core pictures).

## Taking the pictures (`bin/shoot-all.sh`)

| Flow | State | Pictures |
|---|---|---|
| `shoot-admin` | `13-db-backup` | the admin tabs; the extension log (stops and starts one extension) |
| `shoot-workspace` | `13-db-backup` | the workspace tabs, protocols, providers (edit dialog), settings (all tabs, merge of tags), lorebook, resources, trash, plugin settings |
| `shoot-book` | `13-db-backup` | the book window: About, Prompts, the story (hero, turn details, prompt, token usage, highlight), Branch view, Backups |
| `shoot-book-extra` | `13-db-backup` | the reasoning of a part, the backup dialogs, export dialogs (PDF), import of a backup as a new book |
| `shoot-lorebook-tab` | `13-db-backup` | the read only *Lorebook* tab of the story editor with two entries opened: `story-lorebook-tab` |
| `shoot-phone` | `13-db-backup` | the phone layout (Chrome as a phone: 402x874, touch, mobile user agent), the search and replace windows of the story and of the lorebook, and the reading view of the viewer (`viewer-reading`; its list is taken by `shoot-folders`) |
| `shoot-extra --only <part>` | a **copy** of `13-db-backup`, discarded | `language` (selector, Japanese), `quick-start` (a new book, an error of the model), `context-warning`, `verification` (a patched plugin, a stopped one) |
| `shoot-plugin-characters` | `21-chaptermarker` | Character Tracker: list, dialog, the question when a changed dialog is cancelled, map (2x pixel ratio), import dialog |
| `shoot-plugin-knowledgetracker` | `99-final` (or any step from `02-extensions`) **with the plugin JAR copied in** | Knowledge Tracker: the Knowledge tab with a tracked character, her items, the old knowledge and the processing panel: `plugin-knowledgetracker`, `plugin-knowledgetracker-processing`. The steps hold no `knowledge-tracker` JAR (`refresh-plugins.sh` only swaps the ones that are there): stop the application, copy `marginalia/plugins/knowledge-tracker/target/knowledge-tracker-1.7.0.jar` into `.data/.marginalia/extensions/`, start it. A rebuild of the chain installs it (`01-extensions`) |
| `shoot-plugin-knowledgetracker-review` | the same, **and the fake model running** (`bin/start-llm.sh`) | Knowledge Tracker reading with the model: the progress panel and the review of the changes: `plugin-knowledgetracker-progress`, `plugin-knowledgetracker-review`. The flow resets the fake model and queues its answer, so the proposal is always the same |
| `shoot-commands` | a **copy** of `13-db-backup`, discarded | the command line of the story editor: the list of commands, the help of one, the result of `/take-backup` (which adds a backup to the book, hence the copy), (the Stop window, `command-running`, is not retaken: `/hello-world-async` is gone), the question of `/search-replace-parts` (answered with No): `command-list`, `command-help`, `command-result`, `command-running`, `command-confirm` |
| `shoot-search-replace` | a **copy** of `13-db-backup`, discarded | the settings menu with *Search and replace*, the window of the story (empty, with a row opened, the warning about summaries that would become outdated) and of a lorebook; nothing is replaced, but the workspace is left on the Lorebooks tab (the phone flow needs it on Books): `story-settings-menu`, `search-replace-dialog`, `search-replace-found`, `search-replace-warning`, `lorebook-search-replace` |
| `shoot-help` | a **copy** of `13-db-backup`, discarded | the help assistant (nobody has the permission in the other pictures, so the robot button is not in them): its Admin tab with the model chosen, then the *Help assistant* permission of the administrator with *Allow navigating the interface* (the assistant moves through the interface only for a user whose permission allows it), the chat panel with an answer queued in the fake model, then a second question for which the fake model calls `get_ui_state` and `interact_with_ui` (queued `toolCalls`) so that the assistant really opens the Admin page and a tab, then a third one over the *New Inference Provider* dialog (the assistant stays usable above a dialog): the fake model reads the form with `get_ui_state` and explains the empty required field: `admin-help-assistant`, `help-assistant`, `help-assistant-navigate`, `help-assistant-form` |
| `shoot-permissions` | a **copy** of `13-db-backup`, discarded | the permissions window of `jana` (key button of *Admin → Users*; the application gave her *Create own models* when it migrated the old data): the list of the permissions to add, the *Help assistant* permission added with its settings unfolded, the *Agents* one too (the window and its table are made taller for that picture), saved and folded: `permissions-add`, `permissions-settings`, `permissions-settings-agents`, `permissions-dialog` |
| `shoot-books-extra` | a **copy** of `13-db-backup`, discarded | a book imported from a zip of `.md` files (`archive-upload`), *Tag books*, a lorebook with two subbooks imported from `dataset/lorebook.mjs` (`companion`): *Count with subbooks*, *Mass edit*, *Tag entries*, and the entries imported from a zip (the zip files are made by the flow in `out/_archive/`): `archive-upload`, `multi-tag-books`, `lorebook-sub-lorebooks`, `lorebook-mass-edit`, `multi-tag-entries`, `archive-lorebook-upload`, `archive-entry-names` |
| `shoot-folders` | a **copy** of `13-db-backup`, discarded | the folders of 1.6.0, made through the real UI: *Novels / Portal fantasy* and *Short stories*, a second book (*The Lantern Road*, a favorite), the story book moved into *Harry Potter*, the style set in the settings of *Fan fiction*; then the list, the mass changes (the rename numbers the books of all folders), the Organise window, the *Prompts* tab that inherits the style, the minimized tabs and the mass delete of the story, the help assistant with the check box for the books (the fake model calls `search_books`), the phone list and the viewer with its folders (a second browser, phone profile; the viewer is opened into a book and back, and visited again, to check that it keeps the folder in its cookie): `viewer-list`, `viewer-folder`, `books-folders`, `folder-settings`, `mass-change-model-protocol`, `mass-change-language`, `mass-rename`, `organise-dialog`, `book-prompts-inherited`, `story-minimized-tabs`, `story-mass-delete`, `help-assistant-books`, `phone-folders` |
| `shoot-history` | a **copy** of `13-db-backup`, discarded | the version history of a part: the last part is changed twice by the author and redrafted (so it has versions of three kinds), the menu of the part with the *History* item, the history window with a version named (*Label…*), *Clear history*, the *Include versions* check box of the branch view with a part found only through an old version, the settings menu of the story, and the phone layout (a second browser): `history-part-menu`, `history-dialog`, `history-clear`, `history-search`, `history-menu`, `phone-history` |
| `shoot-statistics` | a **copy** of `13-db-backup`, discarded, with a year of statistics written by `bin/seed-statistics.py` (the application is stopped for it; the dataset has only a day or two, a calendar of that is empty) | the daily goals of the *Settings* tab (500 words, 30 minutes), the statistics window of the book (settings menu of the story → *Statistics*: the days, the time, the calendar), of all books (the *Statistics* button of the list of books: with the goal bars) and the phone layout: `settings-daily-goal`, `statistics-book`, `statistics-time`, `statistics-calendar`, `statistics-global`, `statistics-global-calendar`, `phone-statistics` |
| `plugin-conlang` (run by `bin/shoot-conlang.sh`, which restores `23-betareaders`, copies in the plugin's JAR, starts and stops the application once for its migrations, writes the language and a translated part with `bin/seed-conlang.py` and runs the flow) | the language *Vrakht* of `dataset/conlang-vrakht.json` (the one of the dataset book of the tests) and a changed copy, the book's choice of it and its speakers (`dataset/conlang-book.json`), one more part after the last one with two speeches in Vrakht (`dataset/conlang-part.json`, made by `dataset/from-fixture.py`), the book published | the panel and the editor of a language (its *Names* tab with the preview of twenty names; `bin/seed-conlang.py` gives the language the categories *People* and *Places*), the review of the differences, the designer's menu and wizard, the fields of a book (with the understanding of two characters that do not speak the language), the story with the English behind a speech, the window that edits the source of a speech, the search and replace over the sources, the range window, the export dialog and the viewer: `plugin-conlang-settings`, `-editor`, `-rules`, `-lexicon`, `-names`, `-review`, `-designer-menu`, `-wizard`, `-book`, `-tab`, `-story`, `-source`, `-search-replace`, `-range`, `-export`, `-viewer` |
| `plugin-reallang` (run by `bin/shoot-reallang.sh`, the same way with `bin/seed-reallang.py`) | the languages *Japanese* and *Czech* (written into the library of the administrator), the book chooses Japanese and has its speakers (`dataset/reallang-book.json`), one more part with four speeches in Japanese (`dataset/reallang-part.json`) | the panel (and the library of extraction prompts further down) and the editor of a language (also its end, with the glossary of the language), the fields of a book, the *Real language* tab with the speakers and the understanding of the characters, the word extraction (the fake model lists terms: queued answers; the terms land in the glossary as proposals), the glossary of the book and its entry window, the story with the English behind a speech, the window that edits the source, the search and replace over the sources, the range window, the export dialog: `plugin-reallang-settings`, `-prompts`, `-editor`, `-editor-glossary`, `-book`, `-tab`, `-extraction`, `-glossary`, `-glossary-entry`, `-source`, `-search-replace`, `-story`, `-range`, `-export` |
| `shoot-new` | a **copy** of `13-db-backup`, discarded | the screens of 1.7.0 that have no flow of their own: installs Book Description Generator, Prompt Inspector and Prompt History, makes a description (queued answer), the Lore Usage window, a generation that the inspector stops before it is sent and sends (so the history has a request), the description window of a lorebook, a part edited under a summary (the question *Back to editing / Save anyway / Save and revalidate summaries*, and the overview with the outdated summary), then the second round of 1.7.0: *Resummarize all summaries* (the question, then the window at work: three queued answers with a delay of 3 s each), the Planner tab of the story editor (four scenes added through the real form, the first one ticked, a click on a scene that fills the new turn form), the import from LM Studio (a conversation file that the flow writes into `out/_lmstudio/`) and the Condition of a lorebook entry with the preview that says *condition is false* (a taller window, 1200 px, so the entry shows down to its Condition): `plugin-book-description`, `lore-usage`, `plugin-prompt-inspector`, `plugin-prompt-history`, `lorebook-description`, `summaries-save-warning`, `summaries-outdated`, `summaries-resummarize-confirm`, `summaries-resummarize`, `planner-scene-dialog`, `planner-tab`, `planner-new-turn`, `lmstudio-import`, `lorebook-condition`, `lorebook-condition-preview`. A section can be run alone on a fresh copy with `SHOT_ONLY=planner bin/shoot-some.sh 13-db-backup shoot-new.mjs` |
| `plugin-arcoutline-wizard` | a **copy** of `22-arcoutline`, discarded; the fake model runs and every answer is queued | the plot wizard of the Arc Outline (**Develop the plot**): the menu of the wand with its three items (this retakes `plugin-arcoutline-menu`), a conversation in the mode *Explore directions* with the options beside it, the planner tool group ticked so that the model (queued `toolCalls`) lists the planner and adds two scenes, and the summary of the chat with the question what it should keep: `plugin-arcoutline-menu`, `plugin-arcoutline-wizard`, `plugin-arcoutline-wizard-tools`, `plugin-arcoutline-wizard-summarize`, `plugin-arcoutline-wizard-summary` |
| `shoot-agents` | a **copy** of `22-arcoutline`, discarded | the Agents tab and what is behind it. The administrator gets three concurrent runs (*Agents* permission), the book is cloned three times (`book-clone`) and four agents are made: *Continuity editor* (a conversation in the console: it asks what to do, works, reports three findings, edits parts, a lorebook entry and an arc of the outline, asks a *Reader* sub-agent, finishes; the author compacts it, finishes it, and changes the same sentence and the same arc meanwhile, so that the review has conflicts and a JSON merge), *Lore keeper* (asks a question with `ask_user`, then is paused), *Style editor* (calls `delete-part`, **Ask first**: waits for the approval) and *Drafter* (the fake model is slow: it works while `agents-tab` is taken). Every answer of the fake model is queued (`toolCalls` of `/_queue`, see `lib/agents.mjs`) before the step that needs it. The handles the agent names (`h:m5`, `h:e9`) are the numbers the run gives the things in the order it first lists them (`list-parts` and `list-lore-entries` first). The window of the approval only comes up when the console is open while the agent works (opened after, it lies under the console), and it can not be closed without a decision, so the flow reloads the page. Phone pictures with a second browser. Pictures: `agent-dialog`, `agent-dialog-prompts`, `agent-models`, `agent-commands`, `agent-start-menu`, `agent-console-start`, `agent-console-run`, `agent-console-report`, `agent-console-plan`, `agent-console-findings`, `agent-console-command`, `agent-console-question`, `agent-console-workspace`, `agent-checkpoints`, `agent-context`, `agent-book-copy`, `agent-diagnostics`, `agent-runs`, `agent-subagents`, `agent-approval`, `agent-resume`, `agent-stop`, `agents-tab`, `agent-locked-book`, `agent-locked-lorebook`, `agent-clone`, `admin-agents`, `agent-review`, `agent-review-conflict`, `agent-review-differences`, `agent-review-json`, `agent-json-merge`, `phone-agents`, `phone-agent-console`, `book-clone` |
| `shoot-styles` | a **copy** of `13-db-backup`, discarded | the text styles and the export settings of 1.6.0: the *Styles* tab with the horror style of the styling guide imported from `manual/files/horror-book.style.json` (and saved, so it is in the menus), its *Custom CSS* tab, the *Exports* tab with the HTML section opened, the *Appearance → Change Styles For Text* menu and the story in the horror style, the menu of a part with its *Inspect* sub menu, the *Images* window with the picture in the middle and the text flowing around it, the *Preview as in the viewer* (the book is put back to the *Normal* style first: a style with a *Text width* keeps the text in a narrow column and a picture on the side does not wrap around it), and the menu of the styles of the reader: `settings-styles`, `settings-styles-css`, `settings-exports`, `story-style-menu`, `story-style-horror`, `story-part-menu`, `images-position`, `images-preview`, `viewer-style-menu` |
| `plugin-imagegenerator` | a **copy** of `13-db-backup`, discarded | installs the Image Generator JAR, adds a backend of the type *OpenAI images API* pointing to the fake server (`/v1/images/generations` answers with `dataset/generated-image.png`, made by `dataset/make-generated-image.py`; the prompt writer is the rule `image-prompt` of `llm/rules.mjs`), makes an image of the last part and accepts it: `plugin-imagegenerator-settings`, `plugin-imagegenerator-menu`, `plugin-imagegenerator-prompt`, `plugin-imagegenerator` |
| `plugin-googlegenai` | a **copy** of `13-db-backup`, discarded | installs the Google Gemini JAR, adds a provider of the type *Extension* (nothing is sent to Google): `plugin-googlegenai-type`, `plugin-googlegenai`, `plugin-googlegenai-list` |
| `plugin-stconverter` | a **copy** of `13-db-backup`, discarded | uploads the chat of `dataset/stchat.mjs` to the Converter tab (the first upload of it, the chat), chooses the whole range and converts it (the turn details are queued in the fake model): `plugin-stconverter-chat`, `plugin-stconverter-turns` |
| `plugin-manga` | a **copy** of `13-db-backup`, discarded | the Manga Generator window: a manga, a section, the script and the tags (queued in the fake model): `plugin-manga`, `plugin-manga-tags` |
| `plugin-anthropic` | a **copy** of `13-db-backup`, discarded | installs the Anthropic Claude JAR (36 MB), adds a provider of the type *Extension* with a placeholder key (nothing is sent to Anthropic): `plugin-anthropic`, `plugin-anthropic-list` |
| `shoot-features` | a **copy** of `13-db-backup`, discarded | the screens of 1.5.0: annotations (the window, the right click menu of the text, the popover of a mark, the margin, the tab), the model that annotates, one part and a range of parts (answers queued in the fake model), redraft, merge of two parts, branch tokens, drafting the instructions; then the workspace: keyboard shortcuts, advanced search of the books, import from SillyTavern (`dataset/stchat.mjs` makes the chat), preview of the activation of a lorebook, resource files, the light theme |
| `shoot-invalid-extensions` | a **copy** of `13-db-backup`, discarded | the Author's Note JAR is replaced by the broken one (`bin/make-broken-plugin.py`) and its `.valid` report deleted before the start (the block in `bin/shoot-all.sh`): the red bar, `invalid-extensions-warning` |
| `shoot-tabs` | a **copy** of `22-arcoutline`, discarded | drags *Side Query* under the other tabs and *Characters* into a column of its own (the arrangement is saved with the book): `story-editor-tabs` |
| `plugin-instructionexporter`, `plugin-chinese`, `shoot-export` | `21-chaptermarker` | the menu item, the UI in Chinese (the language is reset), `export-result` (PDF page 1 and 2 with `pdftoppm` + `magick`) |

The dev UI maps are not pictures of a flow: `dev-ui-map-*-not-annotated.png` are plain screenshots, and
`tools/annotate_ui_maps.py` draws the labels on a copy (`out/maps/`); coordinates are at the top of that script.

## Pictures

(Generated by `python3 bin/list-images.py`; the pictures that are not in the table are not retaken here: `desktop-tray.png`
(the tray icon of the desktop app), `dev-intellij-tomcat.png` (IntelliJ), `dev-plugin-bookmarks.png` (the example plugin of
the developer guide, it is not in the repository).)

@@IMAGES@@

## Rules of the game

1. **One state per picture.** A flow that changes data must be a step of the chain (and then a snapshot follows). A flow that
   only looks may be run on any state, but the pictures of a page must come from the state the table names.
2. **Never use the real data.** All data is in `.data/`; the scripts use port 8099. Do not point them to `~/.marginalia`.
3. **After a flow fails**, the data may be half changed: `bin/restore.sh <step>` and run it again. `_fail*.png` in `out/` shows
   the page at the failure.
4. **Fields of Vaadin.** `fill()` of `lib/shot.mjs` sends a `change` event and types a password field key by key (a password
   field only synchronizes from real key presses). Combo boxes are picked with `pick()` (it clicks the option); Vaadin
   `Select` with `pickSelect()`. Dialogs are `vaadin-dialog[opened]`; the book window is a dialog too, so `snapDialog()` takes
   the n-th smallest one (0 = the one on top, -1 = the book window).
5. **Coordinates** (the + of the story editor at 1857,822; the sidebar divider at 405,488; the pen of a row at 1804, the
   left side menu...) are for 1920x932. Layout changes of the application show as flows that click on the wrong thing:
   look at the `_fail*.png`.
6. After a flow that was changed: run it from the step before it (`bin/rebuild.sh <step before>`), look at the pictures with
   your own eyes, then `bin/publish.sh`, `retype build` (see CLAUDE.md), update `TODO.IMAGES.md` and the plugin READMEs.

## Quirks of the application met on the way (all in `TODO.BUGS.md`)

* **BUG-61** a new provider dialog forgets the API key when you return to *General Settings* after filling *Per Type Settings*
  (the flow fills *General Settings* first);
* **BUG-62** an empty API key is refused (the flows use `demo-key`);
* **BUG-63** every login of a regular user shows 2-3 *Application Error* dialogs (`dismissLoginError()` closes them);
* **BUG-64** *Refresh* in *Edit Inference Provider* empties the *Model* field (the flow picks the model again).

Things that changed in 1.7.0 and that the flows know: a save of a part under a summary asks first (`partAction(..., 'Save')` of `lib/story.mjs` answers *Save and revalidate summaries*), the list of books has the menus **Manage** and **Import** (`bookListMenu` of `lib/shot.mjs`), the default summary prompt is a *compact event ledger* (the rule `summary` of `llm/rules.mjs`), the new entry of a lorebook is put among the others by its order (the flow of Lore Generator scrolls to it).

## The final database

After the last step the data folder is also saved as `steps/99-final.tar.gz` and the SQLite file as
`final/marginalia.sqlite` (and `final/data-folder.tar.gz` with the book backups, pictures, extensions and `secret.key` that the
database needs). To look at it: `bin/restore.sh 99-final`, then open `http://localhost:8099` as `admin` / `marginalia`.
