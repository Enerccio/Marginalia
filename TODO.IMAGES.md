# Screenshots still to be made

Almost all pictures of the manual are taken by the scripted environment in [`screenshots/`](screenshots/SCREENSHOTS.md):
one dataset (the Harry Potter book), 1920x932 dark theme, all plugins installed, the same footer with the Language
selector everywhere. Retake a picture there (`bin/rebuild.sh`, `bin/shoot-all.sh`, `bin/publish.sh`) instead of by hand,
so that all of them keep matching. The table of the pictures, with the flow that takes each one, is in
[`screenshots/SCREENSHOTS.md`](screenshots/SCREENSHOTS.md#pictures).

Screenshots that show the photograph from Wikimedia Commons (*Harry Potter.jpg*, CC BY-SA 4.0) must stay listed on
`manual/credits.md`. If another third-party picture is used, add it there too (author, source, license, changes).

## 1.6.0

The screens of 1.6.0 come from the flow `shoot-folders` (see [`SCREENSHOTS.md`](screenshots/SCREENSHOTS.md)): `books-folders`, `folder-settings`,
`organise-dialog`, `mass-change-model-protocol`, `mass-change-language`, `book-prompts-inherited`, `story-minimized-tabs`,
`story-mass-delete`, `help-assistant-books`, `phone-folders`; the pictures that show the changed screens (the book list, the tab
buttons of the story editor, the Backups tab and the help panel) were retaken with `bin/shoot-all.sh`. Not taken: the question of a name
clash in *Organise* (rename / skip / merge), the question when a folder with content is deleted, the progress bar of a copy.

## Taken for 1.5.0, not yet (optional)

Every picture of the manual was retaken for 1.5.0 (the footer got the theme button, the story editor is a dock with the story
as a tab, the menus of the story show the keys of their items, the lists got clone buttons, the lorebook editor got its new
toolbar). The screens that are new in 1.5.0 are in the manual too: the annotations (`annotation-dialog`, `annotation-popover`,
`annotations-margin`, `annotations-tab`, `annotate-ai-proposals`, `annotate-ai-batch`, `annotate-auto`), `redraft-dialog`, `part-merge-dialog`, `branch-tokens`,
`impersonate-dialog`, `story-text-menu`, `settings-shortcuts`, `theme-light`, `book-search-advanced`, `sillytavern-import`,
`lorebook-preview-activation`, `admin-resource-files`, and the plugins Anthropic Claude (`plugin-anthropic`,
`plugin-anthropic-list`), SillyTavern Converter (`plugin-stconverter-chat`, `plugin-stconverter-turns`) and Manga Generator
(`plugin-manga`, `plugin-manga-tags`); the flows are `shoot-features`, `plugin-anthropic`, `plugin-stconverter` and `plugin-manga`
(see [`SCREENSHOTS.md`](screenshots/SCREENSHOTS.md)). Not taken:

| Picture | Used in | Why |
|---|---|---|
| `agent-webhook-test.png` | `user/account.md`, `user/agents.md#when-you-are-not-there` | The message to a URL in *Settings* with **Send a test message** (a fake server would have to answer the call). Still missing from 1.4.0. |
| `archive-progress.png` | `user/books/story-editor.md#importing-text-files` | The progress bar of the import with **Stop**: the import of three files is over before a picture can be taken (a zip of a few hundred files would do). Still missing from 1.4.0. |
| `story-loading-parts.png` | `user/books/story-editor.md` | The *Loading earlier parts* mark at the top of a long story (needs a longer story or a low *Parts loaded at once*). Still missing from 1.4.0. |
| a *Merge JSON data* picture with a decision made | `user/agents.md#data-that-extensions-keep-json` | `agent-json-merge.png` shows the tree opened to the conflict; one with *Use the agent's* chosen and the *Text* tab is missing. Still missing from 1.4.0. |
| the window of **Redraft selection** | `user/books/story-editor.md#redrafting-only-a-passage` | The same as `redraft-dialog` with the *Selected text* field on top; `shoot-features` has the right click menu (`story-text-menu`) but does not choose **Redraft selection** yet. |
| an *Annotation - Added* line in the review of an agent | `user/books/annotations.md#annotations-and-agents` | `shoot-agents` does not make the agent write annotations. |
| the part menu (☰) with the keys of its items | `user/books/story-editor.md#the-part-menu`, `user/books/story-editor.md#keyboard-shortcuts` | Only the settings menu has a picture (`story-settings-menu`); the menu of a part with `e`, `r`, `s` next to its items would show the keys of the part. |
| **Auto mode** of the SillyTavern Converter (the line with *Messages per batch*) | `user/extensions/sillytavern-converter.md#auto-mode` | The flow converts one range with the wand only. |
| the **Override** menu and the request window (the eye) of a section of the Manga Generator | `user/extensions/manga-generator.md#sections` | Not in `plugin-manga`. |
| the new features on a phone (the annotations, the redraft, the merge) | `user/phone.md` | `shoot-phone` has not been extended; the phone pictures show the screens of 1.4.0 as they look now. |

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
* The Name provider and the Place provider have no screen of their own (their tools are in the *Tools* window, `tools-dialog`); a picture of the Place provider as a *Use this suite* section is not taken.
* The login card shows the version and the commit under the button; `00-first-start` hides that line (`login.png` is the card of the empty installation).
