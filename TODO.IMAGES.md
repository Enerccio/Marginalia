# Screenshots still to be made

The manual links these files from `manual/`; save them in `manual/images/` (dark theme, like the existing ones) and
run `retype build` in the repository root afterwards. Delete a line when its image exists.

Screenshots that show the photograph from Wikimedia Commons (*Harry Potter.jpg*, CC BY-SA 4.0) must stay listed on
`manual/credits.md`. If another third-party picture is used, add it there too (author, source, license, changes).

Tips for the session: one dataset (the Harry Potter book), 1920x932 dark theme, all plugins installed (Author's Note,
Character Tracker, Side Query, Lore Generator, Name generator...), an administrator login for the admin screens, and the
footer with the Language selector visible. Retake the screens marked "footer" in the same session.

## New

The page already describes the feature, only the picture is missing. The file names are proposals.

### Core

| File | Used in | Should show |
|---|---|---|
| `settings-tags.png` | `user/account.md` §Tags | Settings → Tags: the table with the object counts, rename and delete buttons |
| `tag-merge.png` | `user/account.md` §Tags | The confirmation that asks whether to merge when a tag is renamed to an existing name |
| `provider-shared-user.png` | `user/inference-providers.md` §Shared providers | The Inference Providers tab of a regular user: a shared provider as a read-only row, the info button |
| `provider-limit-info.png` | `user/inference-providers.md` §Limits of shared providers | The info dialog ("Your limits": limit, used, next reset) |
| `provider-limits.png` | `user/inference-providers.md` §Limits of shared providers | The Limits dialog of an administrator and the Edit limit dialog (one image or two) |
| `provider-usage.png` | `user/inference-providers.md` §Usage | The Usage per user dialog of an administrator |
| `token-usage.png` | `user/books/story-editor.md` (part menu, Token Usage) | The Token Usage dialog: chart of the prompt sections and the list of messages |
| `highlight-generated.png` | `user/books/story-editor.md` §Highlighting generated text | A story with the highlight on (a part with edits in it) and the ⚙ menu with the checked item |
| `language-selector.png` | `user/account.md` §Language | The footer with the Language selector open |
| `ui-japanese.png` | `user/account.md` §Language | The workspace or the story editor in Japanese |
| `extension-log.png` | `user/administration/extensions.md` §The extension log | The Log dialog of an extension |
| `extension-verification.png` | `user/administration/extensions.md` §When an extension fails | The verification report of an extension that does not fit this version, and a stopped extension (Start button) |
| `trash-delete-permanently.png` | `user/trash.md` §Deleting for good | The confirmation of Delete Permanently, and the message when something is blocked |
| `error-dialog.png` | `user/inference-providers.md` §Error messages | An inference error dialog (e.g. a timeout or a limit reached) |
| `context-warning.png` | `user/inference-providers.md` §Warning before sending | The warning when the prompt does not fit the context |
| `settings-extensions.png` | `user/account.md` §Extension Settings | Settings → Extension Settings with the Lore Generator settings |

### Plugins (no picture on their page yet)

| File | Used in | Should show |
|---|---|---|
| `plugin-charactertracker-tab.png` | `user/extensions/character-tracker.md` | The Characters tab in the story sidebar |
| `plugin-charactertracker-dialog.png` | `user/extensions/character-tracker.md` | The character dialog with relationships |
| `plugin-charactertracker-map.png` | `user/extensions/character-tracker.md` | The relationship map (also fits `developer/custom-components.md`, GraphView) |
| `plugin-loregenerator.png` | `user/extensions/lore-generator.md` | The generator dialog (prompt, generation and compaction panes) |
| `plugin-loregenerator-names.png` | `user/extensions/lore-generator.md` §Name tools | The Name tools dialog with the filtered category table |
| `plugin-authorsnote.png` | `user/extensions/authors-note.md` | The Author's Note tab |
| `plugin-instructionexporter.png` | `user/extensions/instruction-exporter.md` | The Export instructions entry in the ⚙ menu |
| `plugin-chineselocalization.png` | `user/extensions/chinese-localization.md` | The UI in Chinese |

### Optional

| File | Used in | Should show |
|---|---|---|
| `phone-providers.png` | `user/phone.md` | The phone provider list with the info button of a shared provider |
| `phone-trash.png` | `user/phone.md` | The phone trash tab |
| `export-result.png` | `user/books/export.md` §The title page, §Table of contents | A rendered EPUB or PDF: title page and table of contents |
| `dev-ui-map-phone.png` (+ `-not-annotated`) | `developer/ui.md` | The phone twin of the class maps (workspace drawer, book window, story part), drawn with `tools/annotate_ui_maps.py` |

## To retake

The screen changed after the picture was made.

| File | Why |
|---|---|
| `workspace.png` | The footer has a Language selector above Change Password (used on `user/index`, `first-start`, `developer/architecture`, `contributing`, `ui`) |
| `dev-ui-map-workspace-not-annotated.png`, `dev-ui-map-workspace.png` | Same footer change; retake the plain screenshot, then `annotate_ui_maps.py workspace` (the "Workspace" label may move, the footer got a row taller) |
| `settings-defaults.png`, `settings-templates.png` | The tab bar has a new Tags tab |
| `inference-providers-list.png` | New columns Shared, Tokens sent, Tokens generated; new buttons Usage per user, info and limits |
| `inference-provider.png`, `inference-provider-general.png` | An administrator sees the new Shared with all users checkbox under Type; retake as an administrator |
| `admin-extensions.png` | New # (SHA-256), verification, Start/Stop and Log controls; shows only the `bookmarks` example plugin, install the real ones |
| `admin-users.png` | New Users can create their own models checkbox above the list |
| `trash-tab.png`, `admin-trash.png`, `trash-restore-blocked.png` | New Delete Permanently button next to Restore Selected |
| `lorebook-entry.png` | No `~N Tokens` line and no expand toggle on Content (`lorebook.png` has both) |
| `plugin-lorebookvcs.png` | No token counts and no expand toggles |
| `phone-workspace-drawer.png` | The drawer has the language selector |

Smaller differences, retake when convenient:

| File | Why |
|---|---|
| `quick-start-first-part.png`, `plugin-chaptermarker.png`, `plugin-sidequery.png` | The story tab shows only the gears button; there is a Summaries button next to it now (see `hero-story-editor.png`) |
| `dev-plugin-bookmarks.png` | Same bottom bar; the part menu looks like it lacks the Images and Token Usage items (check against the current menu) |
| `book-prompts.png`, `new-turn-instructions.png`, `turn-details.png` | No expand toggles on the text areas (`new-turn-instructions.png` can be cropped from `hero-story-editor.png`) |
| `books.png`, `protocols-list.png`, `resources-tab.png`, `resources-replace.png`, `admin-cleanup.png`, `admin-database-backups.png` | Only the footer is old (no Language selector), retake in the session |

The class maps `dev-ui-map-book.png` and `dev-ui-map-workspace.png` are drawn with `tools/annotate_ui_maps.py` from a
plain screenshot (`dev-ui-map-<name>-not-annotated.png`), see [Contributing](manual/developer/contributing.md).
