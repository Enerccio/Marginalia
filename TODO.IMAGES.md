# Screenshots still to be made

Almost all pictures of the manual are taken by the scripted environment in [`screenshots/`](screenshots/SCREENSHOTS.md):
one dataset (the Harry Potter book), 1920x932 dark theme, all plugins installed, the same footer with the Language
selector everywhere. Retake a picture there (`bin/rebuild.sh`, `bin/shoot-all.sh`, `bin/publish.sh`) instead of by hand,
so that all of them keep matching. The table of the pictures, with the flow that takes each one, is in
[`screenshots/SCREENSHOTS.md`](screenshots/SCREENSHOTS.md#pictures).

Screenshots that show the photograph from Wikimedia Commons (*Harry Potter.jpg*, CC BY-SA 4.0) must stay listed on
`manual/credits.md`. If another third-party picture is used, add it there too (author, source, license, changes).

## Taken for 1.4.0, not yet (optional)

All the pictures of the screens that changed since 1.3.0 are retaken, and the new ones of the agents, the book clone, the
multi-tag windows, the lorebook with subbooks (Count with subbooks, Mass edit) and the archive import are in the manual
(flows `shoot-agents` and `shoot-books-extra`, see [`SCREENSHOTS.md`](screenshots/SCREENSHOTS.md)). Not taken:

| Picture | Used in | Why |
|---|---|---|
| `agent-webhook-test.png` | `user/account.md`, `user/agents.md#when-you-are-not-there` | The message to a URL in *Settings* with **Send a test message** (a fake server would have to answer the call). |
| `archive-progress.png` | `user/books/story-editor.md#importing-text-files` | The progress bar of the import with **Stop**: the import of three files is over before a picture can be taken (a zip of a few hundred files would do). |
| `story-loading-parts.png` | `user/books/story-editor.md` | The *Loading earlier parts* mark at the top of a long story (needs a longer story or a low *Parts loaded at once*). |
| a *Merge JSON data* picture with a decision made | `user/agents.md#data-that-extensions-keep-json` | `agent-json-merge.png` shows the tree opened to the conflict; one with *Use the agent's* chosen and the *Text* tab is missing. |

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
* A picture of the Lorebook tab of the story editor with *Include sub lorebooks* on (the dataset has no sub lorebooks; `shoot-books-extra` has a lorebook with two, but the book window is not opened there).
