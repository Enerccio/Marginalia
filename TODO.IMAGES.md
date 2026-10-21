# Screenshots still to be made

Almost all pictures of the manual are taken by the scripted environment in [`screenshots/`](screenshots/SCREENSHOTS.md):
one dataset (the Harry Potter book), 1920x932 dark theme, all plugins installed, the same footer with the Language
selector everywhere. Retake a picture there (`bin/rebuild.sh`, `bin/shoot-all.sh`, `bin/publish.sh`) instead of by hand,
so that all of them keep matching. The table of the pictures, with the flow that takes each one, is in
[`screenshots/SCREENSHOTS.md`](screenshots/SCREENSHOTS.md#pictures).

Screenshots that show the photograph from Wikimedia Commons (*Harry Potter.jpg*, CC BY-SA 4.0) must stay listed on
`manual/credits.md`. If another third-party picture is used, add it there too (author, source, license, changes).

## Changes since 1.3.0 (1.4.0): new and retake

The manual of 1.4.0 describes screens that have no picture yet (agents above all: `user/agents.md` has none) and several
old pictures show screens that changed. The names of the new pictures are proposals (`<name>.png` in `manual/images/`,
taken by a flow in `screenshots/flows/`; each needs its row in `SCREENSHOTS.md` and a reference in the page named in
*Used in*). Most of them need data the dataset does not have yet (see [What the dataset needs](#what-the-dataset-needs)).

### Retake (the screen changed)

| File | Used in | What changed |
|---|---|---|
| `admin-users.png`, `admin-database-backups.png`, `admin-cleanup.png`, `admin-trash.png`, `admin-help-assistant.png`, `admin-extensions.png` | `user/administration/*` | The Admin page has a new **Agents** tab (between *Help assistant* and *Extensions*): the tab strip differs in all of them. |
| `permissions-add.png`, `permissions-settings.png`, `permissions-dialog.png` | `user/administration/permissions.md` | The third permission, **Agents**, is in the list to add; its settings (*Most agents*, concurrent runs, token budget, working copies, file and workspace sizes, checkpoints, requests at once) have to be shown unfolded in `permissions-settings.png`. |
| `inference-provider.png`, `inference-provider-general.png`, `inference-provider-2.png`, `plugin-googlegenai.png`, `plugin-googlegenai-general.png` | `user/inference-providers.md`, `user/extensions/google-genai.md` | The model dialog has the new field **Concurrent requests of agents** (General Settings, with the *Max Context Size* group). |
| `settings-defaults.png` | `user/account.md` | New field **Parts loaded at once in the story** in *General Settings*. |
| `settings-templates.png` | `user/account.md` | New fields for users who may use agents: **System prompt of agents**, **Compaction prompt of agents**, **Instructions of agents (agents.md)**, and the message to a URL (**Message to a URL when an agent needs you**, format, *Also when the application is open*, **Send a test message**). |
| `books.png` | `user/books/managing-books.md` | The toolbar of the list has **Tag books** and **Import from archive**, every row a **Clone** button (and a book an agent works on is grayed out). |
| `phone-book-list.png` | `user/phone.md` | The same on the phone: **Clone** in the card of a book and **Import from archive**. |
| `lorebook.png`, `lorebook-entry.png` | `user/lorebooks/lorebooks.md`, `user/lorebooks/entries.md` | New buttons **Mass edit**, **Tag lorebooks**, **Tag entries**, **Import from archive**, the check boxes **Count with subbooks** and **Only root lorebooks**; the entry filtering modes **Keywords (any)** and **Keywords (all)**. |
| `story-lorebook-tab.png` | `user/books/story-editor.md` | The Lorebook tab of the book window has the **Shown lorebook** selector. |
| `story-settings-menu.png` | `user/books/story-editor.md` | The settings menu (⚙) has **Import from archive** between *Search and replace* and the export. |
| `command-list.png`, `command-help.png` | `user/commands/index.md` | About forty more commands (read and write commands of the book and the lorebooks); the help of a command shows the range, default and allowed values of its parameters. |
| `phone-workspace-drawer.png` | `user/phone.md`, `user/agents.md#on-a-phone` | **Agents** in the drawer (for a user who may use agents) and the button with the count of agents that wait for you in the top bar. |
| `help-assistant-form.png` | `user/administration/help-assistant.md` | Referenced by the page since this change; the workspace behind it lacks the Agents tab (see the next point). |
| `dev-ui-map-workspace.png` (+ `-not-annotated`), `dev-ui-map-book.png` (+ `-not-annotated`) | `developer/ui.md` | A new tab (`AgentPart`) in the workspace, the command bar and the new items of the settings menu in the book window; `tools/annotate_ui_maps.py` needs the new labels (`AgentPart`, `CommandBar`). |

Every picture of the whole workspace (1920x932; the tab list on the left is visible or dimmed behind a dialog) now shows one more
tab, **Agents**, because the administrator of the dataset may use agents. They are only out of date in that detail and a full
run of the chain retakes them: `workspace.png`, `books.png`, `lorebook.png`, `settings-extensions.png`, `settings-templates.png`,
`protocols-list.png`, `resources-tab.png`, `resources-replace.png`, `trash-tab.png`, `trash-restore-blocked.png`,
`book-about.png`, `book-backups.png`, `book-prompts.png`, `backup-export-images.png`, `hero-story-editor.png`,
`highlight-generated.png`, `part-with-image.png`, `quick-start-first-part.png`, `story-tree.png`, `story-tree-search.png`,
`story-editor-tabs.png`, `search-replace-warning.png`, `extension-verification.png`, `invalid-extensions-warning.png`,
`help-assistant-navigate.png`, `ui-japanese.png`, `plugin-arcoutline-tab.png`, `plugin-authorsnote.png`, `plugin-chaptermarker.png`,
`plugin-charactertracker-tab.png`, `plugin-chineselocalization.png`, `plugin-loregenerator-settings.png`,
`plugin-reviewer-settings.png`, `plugin-sidequery.png`, `plugin-sidequery-settings.png`. Not affected: the dialog crops, the
login and first start pictures, the viewer, and the phone pictures other than the drawer. (The version in the bottom left
corner is hidden by the scripts.)

### New pictures

Agents, `user/agents.md` (the heading in the page in parentheses):

| File | What it shows |
|---|---|
| `agents-tab.png` | The **Agents** tab with a few agents in different states (idle, working, paused, one that waits for you with the count next to the tab name) and their buttons (*The table*). |
| `agent-start-menu.png` | The **Start** menu: *Start immediately* / *Start in console* (*A run*). |
| `agent-dialog.png` | *New agent*: book, main model and protocol, prompt and instructions with the **Insert Default Template** info popover, the limits (*Creating and editing an agent*). |
| `agent-commands.png` | The **Selection** of allowed commands: the roles (Auditor, Lore keeper, Editor, Drafter), the grid with severity, batch policy and the tokens each definition adds (*Allowed commands*). |
| `agent-models.png` | **Models for sub-agents**: the table and the window of one model profile (*Models for sub-agents*). |
| `agent-console-run.png` | The console, **Run** tab, while an agent works: the state line with the context against its cap, the transcript with a folded *Reasoning*, calls with their results, the **Plan**, the message box with **Send now** (*The console*). |
| `agent-console-question.png` | The bubble of a question of the agent (`ask_user`) or its report, with the box to answer (*The console*). |
| `agent-console-command.png` | A command of the author in the command line of the console (`/compact`, an allowed command) and its result (*Running a command yourself*). |
| `agent-console-workspace.png` | The **Workspace** tab: folders, files, **Upload file**, the quota (*Unpacking a zip file*). |
| `agent-checkpoints.png` | The **Checkpoints** tab with the automatic and the saved ones, the quota and the **Rewind** button (*Checkpoints*). |
| `agent-context.png` | The **Context** tab with its three charts (*The context*). |
| `agent-diagnostics.png` | The **Diagnostics** tab (*Diagnostics*). |
| `agent-subagents.png` | The **Sub-agents** tab with the conversation of one (*Sub-agents*). |
| `agent-book-copy.png` | The **Book copy** tab (*The copy of the book*). |
| `agent-runs.png` | The **Runs** tab: history, view, download, delete (*A run*). |
| `agent-approval.png` | The window that asks for approval of a destructive command (*Asking you*). |
| `agent-resume.png`, `agent-stop.png` | **Resume** with the *Note for the agent*, and the question to keep or discard the result (*Resuming a run*, *A run*). |
| `agent-review.png` | **Review changes**: the list of what the agent changed with the boxes, the choices *all* / *none* / *only new parts* / *without conflicts*, the detail of a line (*Reviewing the result*). |
| `agent-review-differences.png` | A conflict in the review: **Use the agent's** / the book's / the start's, the field by field edit and the word by word **Differences** (*Reviewing the result*). |
| `agent-json-merge.png` | `JsonMergeDialog`, the data of an extension (an arc outline) with the three versions side by side (*Data that extensions keep (JSON)*). |
| `agent-clone.png` | **Clone** an agent to another book (*Cloning*). |
| `agent-locked-book.png` | The book list with a book an agent works on grayed out and the lorebook view read only with the message naming the agent (*The book is locked*). |
| `phone-agents.png`, `phone-agent-console.png` | The Agents tab as cards on a phone, and the console with its icon tabs (*On a phone*). |
| `admin-agents.png` | *Admin → Agents*: the default system prompt and compaction prompt, the switch for the messages to a URL (`user/administration/index.md`). |
| `agent-webhook-test.png` | Optional: the message to a URL (ntfy) in *Settings*, with **Send a test message** (`user/account.md`, *Settings*). |

Books, lorebooks and the rest:

| File | Used in | What it shows |
|---|---|---|
| `book-clone.png` | `user/books/managing-books.md#cloning-a-book` | The clone window: the name and **Only current branch**. |
| `multi-tag-books.png` | `user/books/managing-books.md#tagging-many-books-at-once` | *Tag books*: the tag picker, the rows with chips, apply and remove, a locked book grayed out. |
| `multi-tag-entries.png` | `user/lorebooks/lorebooks.md#tagging-many-at-once` | The same window for the entries of a lorebook. |
| `lorebook-mass-edit.png` | `user/lorebooks/lorebooks.md#mass-edit` | The table of all entries of a lorebook and its sub lorebooks, with *Move checked*. (Needs sub lorebooks in the dataset.) |
| `lorebook-sub-lorebooks.png` | `user/lorebooks/lorebooks.md#sub-lorebooks` | **Count with subbooks** on, **Only root lorebooks** and the tokens column (the old idea of a picture with *Include sub lorebooks* on). |
| `archive-upload.png`, `archive-entry-names.png` | `user/lorebooks/import-export.md#importing-an-archive-of-text-files`, `user/books/story-editor.md`, `user/books/managing-books.md` | The upload window for a zip of `.md` files, and the table of the names of the entries before the import. |
| `archive-progress.png` | Optional, the same pages | The progress bar with **Stop**. |
| `story-loading-parts.png` | `user/books/story-editor.md` | Optional: the *Loading earlier parts* mark at the top of a long story (set *Parts loaded at once* low, or use a longer story). |

### What the dataset needs

* A **user who may use agents**: the permission `AGENTS` for `ginny` (and the administrator has it), with her own agent for the
  book *Quidditch Tryouts* so a second user can show the limits.
* **Agent runs** made with the fake model (`llm/rules.mjs`): its answers have to be tool calls (`toolCalls` of `/_queue`, as in
  `shoot-help`): read commands, `update_plan`, `write_file`, an edit of a part, `report`, `ask_user`, `spawn_agent`, a
  destructive command for the approval window. One agent **paused**, one **finished with a result to review** (changed parts, a
  new lorebook entry, a changed arc outline for the JSON merge, a conflict), one **waiting for you**, one that **works** while the
  picture is taken (the fake model has to be slowed down for that).
* **Sub lorebooks** (a lorebook with two sub lorebooks), for *Mass edit*, *Count with subbooks*, *Only root lorebooks*, the
  *Shown lorebook* selector and the tag windows.
* A zip file of `.md` files (`assets/`, original text) for the archive pictures.
* New flows in `flows/` and rows in `SCREENSHOTS.md`: `agents` (a step of the chain that creates the agents and the runs,
  before `13-db-backup` if the core pictures are to show the Agents tab with data), `shoot-agents`, `shoot-agents-phone`,
  `shoot-books-extra` (clone, tags, archive) and additions to `shoot-workspace` / `shoot-lorebook-tab`.

## Not taken by the scripts

These pictures cannot be made in a browser or are not pictures of this application's own screens. They are old (not
retaken); retake them by hand when the screen they show changes.

| File | Used in | Why |
|---|---|---|
| `desktop-tray.png` | `user/getting-started/desktop-app.md` | The tray icon of the desktop application (a native macOS menu bar). |
| `dev-intellij-tomcat.png` | `developer/building.md` | The run configuration of IntelliJ IDEA. |
| `dev-plugin-bookmarks.png` | `developer/plugins/example.md` | The *Bookmarks* example plugin of the developer guide: the plugin is not in the repository, it is built step by step in the page. Needs the bottom bar with the Summaries button and the part menu with *Images* and *Token Usage*. |
| `command-running.png` | `user/commands/index.md` | The Stop window of a running command. It was taken with `/hello-world-async`, which is a fixture of the tests now; the picture is the old one. Retake it with a command that takes time (the `shoot-commands` flow has a note). |
| `dev-ui-map-phone.png` (+ `-not-annotated`) | `developer/ui.md` | Optional: the phone twin of the class maps (workspace drawer, book window, story part). `tools/annotate_ui_maps.py` has no layout for it yet. |

## Ideas

* The `gemma4toolsfix` plugin has no screen (nothing to show but its row in *Admin → Extensions*).
* A picture of the *Lorebook Revisions* row (export / import of the history) of the Lorebook VCS plugin.
* A picture of the Lorebook tab with *Include sub lorebooks* on (the dataset has no sub lorebooks).
