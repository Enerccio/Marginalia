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
                      snapshot.sh, restore.sh, rebuild.sh (+ chain.txt), shoot-all.sh, publish.sh,
                      make-broken-plugin.py, list-images.py
  flows/              the Playwright scripts (NN-name.mjs = a step of the setup, shoot-*.mjs = only pictures)
  lib/                shot.mjs (browser, login, dialogs, snap helpers), story.mjs (story editor helpers)
  llm/                mock-llm.mjs (fake OpenAI compatible server) and rules.mjs (what it answers)
  dataset/            the content: story.mjs, lorebook.mjs, characters.mjs, plugins.mjs
  assets/             Harry_Potter.jpg, downloaded by bin/fetch-assets.sh (Wikimedia Commons, CC BY-SA 4.0, credited in manual/credits.md; not in Git)
  steps/              step backups: <name>.tar.gz of the whole data folder after each step   (not in Git)
  final/              the final state: marginalia.sqlite, the data folder as a tarball          (not in Git)
  .data/              the data folder, used as -Duser.home, so the data is in .data/.marginalia (not in Git)
  .run/               pid files, logs of the app and of the fake model, request log (not in Git)
  out/                the pictures that were taken, before they are copied to the manual   (not in Git)
```

## Requirements

* the WAR built from the **same commit** as the plugins: `mvn package -DskipTests` in the repository root builds the
  application and all plugins (`marginalia/target/marginalia-1.3.0.war`, `marginalia/plugins/*/target/*-1.3.0.jar`). Use
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
| Regular user | `ginny` / `marginalia` (*Ginny W.*), has the shared provider and a token limit |
| Providers (admin) | **Local LLM** (shared, `http://localhost:8098/v1`, model `marginalia-demo-12b`), **Local Reasoner** (reasoning, `marginalia-demo-reasoner`), **Featherless.ai** (a hosted service, only listed) |
| Protocols | *Chat Completion* and *Creative Protocol*; defaults in *Settings*: Local LLM + Chat Completion, *No Backups* |
| Lorebook | *Harry Potter - Half-Blood Prince* (11 entries, tag `genderbend`), imported from `dataset/lorebook.mjs`; *Bestiary* is created and deleted for the trash |
| Book | *Harry Potter and Half Blood Prince - FDM*, tags `genderbend`, `harry potter`, `f/m` |
| Story | 5 parts on the main branch (+ a second version of #2 made with Swipe, + a branch from #4 written by Local Reasoner), summaries on #3 and #5 with a meta summary, a picture on #5, an edited sentence in #5 |
| Second book | *Quidditch Tryouts* of `ginny` (two parts, so the usage and limit screens have numbers) |

The key of the providers is the placeholder `demo-key`. The OpenAI client refuses an empty key (TODO.BUGS.md BUG-62, the
manual says to enter anything). The fake model ignores it.

The text of the story, the lore and the other things the model "writes" is original (family friendly) and lives in
`dataset/`; the only third party material is the photograph in `assets/`.

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
bin/restore.sh 13-db-backup                       # go back to a step (stops the app, replaces the data folder, starts the app)
bin/snapshot.sh <name>                            # save the current data folder as steps/<name>.tar.gz (stops and starts the app)
bin/start-app.sh | bin/stop-app.sh | bin/start-llm.sh | bin/stop-llm.sh
node flows/shoot-book.mjs --only turn-details     # one part of a shooting script
python3 bin/list-images.py                        # the table of all pictures below
```

`bin/snapshot.sh` and `bin/restore.sh` stop the application first, because SQLite keeps a write-ahead log: a copy of the folder
of a running application is not consistent.

Every flow starts a fresh browser, logs in and works through the real UI. Pictures are written to `out/<name>.png`; names
starting with `_` are scratch files. `SHOT_TOASTS=1` shows the toasts (they are hidden in all pictures, except the ones that
are about a toast).

## The setup chain (`bin/chain.txt`)

Each step is a flow plus the snapshot taken after it. `bin/rebuild.sh <step>` restores a step and runs everything after it.
A flow may also take pictures on the way (names in the last column).

| Step after | Flow | What it does | Pictures |
|---|---|---|---|
| `00-empty` | (a fresh installation) | | |
| `01-admin` | `00-first-start` | creates the administrator | `first-start`, `login` |
| `02-extensions` | `01-extensions` | installs the 12 plugin JARs (all but Arc Outline and Google Gemini) through *Admin → Extensions* | |
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
| `10-users` | `09-users` | user `ginny`, her protocol and defaults, her book *Quidditch Tryouts* | |
| `11-limits` | `10-limits` | a token limit for her, usage, her info dialog, a second part by her | `provider-limit*`, `provider-usage`, `provider-shared-user` |
| `12-trash` | `trash` | imports *Bestiary* and deletes it, creates and deletes *Scratch book* | |
| `13-db-backup` | `db-backup` | a manual database backup (**the state of all core pictures**) | |
| `14-characters` | `plugin-characters` | the cast of the Character Tracker | |
| `15-relationships` | `plugin-relationships` | the relationships between them | |
| `16-authorsnote` | `plugin-authorsnote` | the Author's Note of the book | `plugin-authorsnote` |
| `17-sidequery` | `plugin-sidequery` | two questions in Side Query | `plugin-sidequery` |
| `18-reviewer` | `plugin-reviewer` | a review of the last part | `plugin-reviewer*` |
| `19-lorebookvcs` | `plugin-lorebookvcs` | a second revision of the entry of Draconia | `plugin-lorebookvcs` |
| `20-loregenerator` | `plugin-loregenerator` | a new lore entry made with Lore Generator (generation, compaction, name tools) | `plugin-loregenerator*` |
| `21-chaptermarker` | `plugin-chaptermarker` | chapter headings in parts 1 and 4 (**the state of the plugin pictures**) | `plugin-chaptermarker` |
| `22-arcoutline` | `plugin-arcoutline` | installs the Arc Outline JAR (unless it is there), the first arc imported from the five parts of the story with the fake model, a second arc with chapters by hand, a third arc and its chapters made with the fake model (`arcOutline` in `dataset/plugins.mjs`) | `plugin-arcoutline-tab`, `plugin-arcoutline-menu`, `plugin-arcoutline-note`, `plugin-arcoutline-import`, `plugin-arcoutline-generate-arc`, `plugin-arcoutline-generate-chapters`, `plugin-arcoutline-generated` |

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
| `shoot-phone` | `13-db-backup` | the phone layout (Chrome as a phone: 402x874, touch, mobile user agent) and the viewer |
| `shoot-extra --only <part>` | a **copy** of `13-db-backup`, discarded | `language` (selector, Japanese), `quick-start` (a new book, an error of the model), `context-warning`, `verification` (a patched plugin, a stopped one) |
| `shoot-plugin-characters` | `21-chaptermarker` | Character Tracker: list, dialog, the question when a changed dialog is cancelled, map (2x pixel ratio), import dialog |
| `shoot-help` | a **copy** of `13-db-backup`, discarded | the help assistant (it is off in all other pictures, so the robot button is not in them): its Admin tab with the model chosen, the chat panel with an answer queued in the fake model, then a second question for which the fake model calls `get_ui_state` and `interact_with_ui` (queued `toolCalls`) so that the assistant really opens the Admin page and a tab: `admin-help-assistant`, `help-assistant`, `help-assistant-navigate` |
| `plugin-googlegenai` | a **copy** of `13-db-backup`, discarded | installs the Google Gemini JAR, adds a provider of the type *Extension* (nothing is sent to Google): `plugin-googlegenai-type`, `plugin-googlegenai`, `plugin-googlegenai-list` |
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

## The final database

After the last step the data folder is also saved as `steps/99-final.tar.gz` and the SQLite file as
`final/marginalia.sqlite` (and `final/data-folder.tar.gz` with the book backups, pictures, extensions and `secret.key` that the
database needs). To look at it: `bin/restore.sh 99-final`, then open `http://localhost:8099` as `admin` / `marginalia`.
