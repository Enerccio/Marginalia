# Marginalia - known bugs

Found while writing the manual. Each entry has a **severity**, **where** to look, **steps** to reproduce and what is
**expected**. Reference them as `BUG-<n>`. Strike an entry through (`~~...~~`) when bug is fixed.

Severity: **high** - data loss or a broken feature, **medium** - wrong result with a workaround, **low** - cosmetic or
an edge case.

---

~~## BUG-1 - Book prompts: Summary Prompt is not loaded and gets erased (high)~~

**Where:** `ui/dialogs/manuscript/ManuscriptPromptPart.java`, `load()`.

`load()` sets the values of all prompt fields except *Default Summary Prompt* (`summaryField.setValue(...)` is
missing). The summary field also gets no placeholder and no template hints popover (the Settings tab has both, with
`SummaryTemplateData`). `autosave()` saves all fields, so the first edit of any prompt field writes the empty summary
prompt over the stored one.

**Steps:**
1. Open a book → *Prompts*, enter a *Default Summary Prompt*. Close the book.
2. Open it again → *Prompts*: the field is empty (the value is still stored).
3. Change *Tense*. Close and reopen the book: the summary prompt is gone for good; summaries use the default from
   Settings.

**Expected:** the field shows the stored value; editing another field leaves it unchanged. It has the placeholder
(default from Settings) and the hints popover like the other fields.

---

~~## BUG-2 - Branch Story drops the part's template variables and extension data (medium)~~

**Where:** `ChatMessage.loadFrom()` used by `ChatMessageServiceImpl.branch()`.

`loadFrom()` copies the text and turn details, but not `attributes` (the extended content). Local template variables
(`setvar` and friends, stored per part and read from the active leaf in `GenerationStepBase`) are therefore lost in the
new branch, together with any data extensions keep on the part. The `edited` flag is not copied either.

**Steps:**
1. Use a user prompt that sets a local variable, e.g. `{{setvar::mood::grim}}`, and generate a part.
2. On that part choose *Branch Story* and generate the next part with a prompt that prints `{{getvar::mood}}`.
3. *Show Prompt*: the variable is empty.

**Expected:** the branched copy keeps the part's attributes (variables, extension data) and the `edited` flag.

---

~~## BUG-3 - Editing a part doesn't update its word count (medium)~~

**Where:** `ManuscriptStoryPart.ChatMessageCard.autosaveAndSwapToMarkdown()`.

Saving an edited part recounts its tokens but not its words (`setWordCount` is only called in `InferenceStep`). The
part's *Words* and the word counts on the book's *About* tab keep the generated value.

**Steps:** generate a part, *Edit* it, delete half of the text, *Save*. *Words* on the part and *Total Word Count* on
*About* don't change.

**Expected:** words are recounted (`chatMessageService.countWords`) together with the tokens.

---

~~## BUG-4 - Editing a part fails when the book has no model (low)~~

**Where:** `ManuscriptStoryPart.ChatMessageCard.autosaveAndSwapToMarkdown()`, `InferenceServicesImpl.forAI()`.

The token count after an edit uses `inferenceServices.forAI(currentManuscript.getAi())`; with no model selected on
*About*, `forAI(null)` throws a `NullPointerException` and the edit is not saved. The same happens on leaving the
*Story* tab or starting a generation while a part is in edit mode.

**Steps:** clear *Model* on *About*, go to *Story*, *Edit* a part, change it, *Save* → *Internal Server Error*, the
change is lost.

**Expected:** the edit is saved; tokens are counted with the approximate tokenizer (`countTokensApprox`) when the book
has no model.

---

~~## BUG-5 - Summary of a book without model, or one that doesn't fit, shows an internal error (low)~~

**Where:** `SummaryServiceImpl.createSummary()`, `ManuscriptStoryPart.SummaryDialog.startGeneration()`.

*Generate summary* on a book without a model fails with a `NullPointerException` (`forAI(null)`). When the parts
to summarize don't fit into the context, `SummaryContextInsufficient` is thrown. Both end in the generic
*Internal Server Error* dialog, and the summary dialog stays open with the title *Summary Error*.

**Expected:** a plain message like the story generation gives (*Book is missing model.*,
*Contextual limit not sufficient.* with the required and available tokens from `SummaryContextInsufficient`).

---

~~## BUG-6 - The book list isn't refreshed after closing a book (low)~~

**Where:** `ui/dialogs/ManuscriptDialog.java`.

`ManuscriptPart` passes `setOnClose(this::refreshGrid)`, but `ManuscriptDialog` never runs `onClose` (compare
`LorebookDialog`, which runs it from an opened-change listener). A renamed book, new tags or the new *Last Modified*
show in *Books* only after switching tabs or clicking refresh. The restore in `ManuscriptBackupPart.performRestore()`
passes the callback on, so it is lost there as well.

**Expected:** closing the book refreshes the list.

---

~~## BUG-7 - No way to get the reader link of a published book (medium)~~

**Where:** `ManuscriptInfoPart` (*Published* checkbox), `ui/main/Viewer.java`.

*Published* says "Published books can be read by other users through the reader link", but the link
(`/view/<book uuid>`) is not shown anywhere, and the book uuid isn't visible in the UI. The reader's list at `/view`
shows only the user's own books, so other users can't find a published book either.

**Expected:** the *About* tab shows the reader link (with a copy button) when the book is published; optionally the
`/view` list also offers published books of other users.

---

~~## BUG-8 - Book, lorebook and entry names can be cleared (low)~~

**Where:** `ManuscriptInfoPart.autosave()`, `LorebookView` (lorebook *Name* field and the *Entry Name* column).

The name fields are saved on every change, also when empty. The book, lorebook or entry then has a blank name in
lists, pick-lists and the dialog title (an empty lorebook can't be told apart from others in the *Lorebook* and
*Sub Lorebooks* selections).

**Expected:** an empty name is not saved (field marked required, previous name kept).

---

~~## BUG-9 - A failed generation leaves an empty part; a failed Regenerate erases the text (high)~~

**Where:** `GenerateNewMessageStep`, `InferenceStep.onError()`, `CleanupStep`.

`GenerateNewMessageStep` creates (or, for *Regenerate*, clears and saves) the part and sets the state to
`PARTIAL_SUCCESS` before the request is sent. When the request fails (model server down, wrong API key, rate limit,
context too long...), `InferenceStep.onError()` jumps to cleanup with that state, and `CleanupStep` keeps the part as
a partial result:

- **new part / swipe:** an empty part stays at the end of the branch (a failed swipe also hides the previous
  version, which is only reachable through the branch view);
- **Regenerate:** the part was cleared before the request, so its original text, reasoning and counts are lost.

The `NOT_SUCCESSFUL` branch of `CleanupStep`, which removes a new part and returns to the previous swipe, is never
reached after a part was created.

**Steps:** stop the model server (or set a wrong API key on the provider) and generate a part → the error is shown and
an empty part is added. *Regenerate* on the last part instead → the part is empty.

**Expected:** an error before any text arrived removes the new part / swipe (as the `NOT_SUCCESSFUL` branch does) and
leaves a regenerated part with its previous content (keep a copy before clearing, or clear only when the first chunk
arrives). An error in the middle of the stream may keep the partial text, like *Stop*.

---

~~## BUG-10 - The book's Lorebook tab shows the full lorebook editor instead of the book's lorebook (medium)~~

**Where:** `ui/dialogs/manuscript/ManuscriptLorebookPart.java` (`new LorebookView(null)`), `LorebookView` constructor.

The comment says the view is pinned ("hides internal lorebook dropdown and create button"), but `pinnedLorebook` is
only set when the constructor gets a non-null lorebook, so the book tab gets an unpinned view. It shows a second
*Lorebook* selector, *Add Lorebook* and the import button next to the book's own *Lorebook* field. Picking a
lorebook in the inner selector, adding or importing one switches the editor to a lorebook that isn't the book's, while
the book keeps its own. After *Delete Lorebook* the editor jumps to the first lorebook of the user, and the deleted
lorebook stays selected in the book's field (generation then skips it silently).

**Steps:** open a book → *Lorebook*, pick lorebook A in the book's field, then pick B in the inner selector → the
entries of B are edited, the book still uses A.

**Expected:** the editor in the book tab always shows the book's lorebook (pinned: no inner selector, no add/import),
and deleting it clears the book's lorebook.

---

~~## BUG-11 - Lorebook tags can't be seen or edited, but limit activation (medium)~~

**Where:** `LorebookServiceImpl.fillEntries()`, `unmarshalContent()`, `LorebookView`.

A lorebook's own tags are added to the tags of each of its entries during activation, so a tagged lorebook only
contributes to books with one of those tags. Lorebook tags are only created by imports (Marginalia lorebook export,
book backups), and nothing in the UI shows or changes them. An imported lorebook can therefore stay silent in a book
for no visible reason.

**Steps:** export a lorebook whose JSON has `"tags": ["fantasy"]` at the lorebook level (or edit an export to add it),
import it, use it in a book without the tag → none of its entries activate; the lorebook editor shows no tags.

**Expected:** a *Tags* field for the lorebook in the lorebook editor (next to *Sub Lorebooks*), with a hint that
the tags apply to all entries.

---

~~## BUG-12 - New lorebook entries are named "New Lorebook" (low)~~

**Where:** `LorebookView.createNewEntry()` uses `L.LABEL_NEW_LOREBOOK`.

**Expected:** a label of its own, e.g. "New Entry".

---

~~## BUG-13 - SillyTavern import accepts any JSON and reports errors as invalid Marginalia files (low)~~

**Where:** `LorebookServiceImpl.importFromSillytavern()`, `LorebookView.openImportDialog()`.

A JSON file without `entries` (e.g. a Marginalia lorebook export, a character card) is imported as an empty
lorebook without a warning. Real errors in either import are shown as *File is not a valid Marginalia lorebook
export.*, also for *Import from SillyTavern*.

**Expected:** a file without `entries` is rejected; the message names the format that was expected
(SillyTavern world info or Marginalia lorebook).


---

~~## BUG-14 - Swipe continues the story instead of writing another version of the last part (high)~~

**Where:** `GenerationRequest.newSwipe()`, `PrepareContentStep`, `GenerationStepBase.createTemplateContext()`.

`GenerationRequest.newSwipe()` sets `requestType = GenerationRequestType.NEW_MESSAGE` instead of `SWIPE`. A swipe is
therefore an ordinary new message: the new part is added as a **child** of the part that should be replaced, with the
same turn details, and the story editor shows it as an extra part at the end. The `SWIPE` branches of
`GenerateNewMessageStep`, `CleanupStep` and the UI, and the `swipe` value of `{{lastGenerationType}}`, are never
reached.

Fixing the type alone is not enough: the swiped part is the active leaf, and only `REGENERATE` removes the last part
of the branch when the prompt and the template context are built. A real swipe would still send the version it
replaces as the previous part, show it in `{{lastMessage}}`, and load the local variables that version set (so
`{{.turn++}}` counts the replaced part too).

**Steps (verified with a test against the mock LLM):** generate parts "FIRST", "SECOND", then *Swipe* "SECOND" →
the new part's parent is "SECOND" (not "FIRST"), the request contains "SECOND" as the last assistant message,
`{{lastMessage}}` renders "SECOND", a `{{.n++}}` counter is one too high.

**Expected:** `newSwipe` uses `SWIPE`; for `SWIPE`, like for `REGENERATE`, the swiped part is left out of the story
sent to the model and of the template context (`storyMessages`, `lastInstructions`, `lastMessageTime`, local
variables come from its parent). Add a generation test for swipes.

---

~~## BUG-15 - Typos in templates are sent to the model as "Error" without a warning (low)~~

**Where:** `TemplateServiceImpl.isValidTemplate()`, `MacroHelpers` (`MISSING_VALUE = "Error"`).

An unknown variable or argument-less macro (`{{instruction}}` instead of `{{instructions}}`, `{{povCharachter}}`)
renders as the word `Error`. Validation only compiles the template, so such templates are saved without a warning,
and the model silently gets "Error" in the prompt.

**Expected:** validation (book prompts, Settings, lorebook entries) reports unknown names, e.g. as a warning that
still allows saving; the hints popover could also list the variables available in every template (`manuscriptName`,
`manuscriptDescription`, the turn fields), which work but aren't shown.

---

~~## BUG-16 - POV, Tense and Style are validated as templates in Settings, but macros in them don't work (low)~~

**Where:** `UserPart.save()` (validates `defaultPov`, `defaultTense`, `defaultStyle` with `isValidTemplate`),
`MasterTemplateData` / `LorebookTemplateData`.

Settings validates the default *Point of View*, *Tense* and *Style* as templates, which suggests macros can be used in
them. Their values are inserted into the master template (and lorebook entries) as plain values, so a macro such as
`{{user}}` in *Style* reaches the model literally. Validation also blocks saving a style that merely contains
unbalanced braces. The book *Prompts* tab doesn't validate these fields at all, so the two places behave differently.

**Expected:** either render POV, Tense and Style as templates (in the shared context, before the master template) or
treat them as plain text everywhere and drop the validation.

---

~~## BUG-17 - Reviewer: the review dialog saves an old copy of the part, reverting edits (high)~~

**Where:** `plugins/reviewer` - `ReviewerExtension` (menu items capture `message` when the card's menu is built),
`ReviewDialog.persistData()` → `ReviewerService.saveReviewData()`.

The *Review* menu items capture the card's `ChatMessage` instance when the card is created. The card later replaces
its own `message` (every time its menu opens, `refreshSummaryMenuItems()` reloads it; editing saves a new instance),
but the review dialog keeps working with the captured one. `persistData()` saves that whole stale entity - on opening
the dialog, on paging, editing and after every generated review. Its `extendedContent` and columns overwrite the
current state of the part: an edit made after the card was rendered is reverted, a summary created since is unlinked,
and local template variables or other extension data written since are lost.

**Steps:** open a book, open the menu of a part, *Edit* its text and *Save*, then *Review → View / Generate Review* →
after the review is generated, close and reopen the book: the part has its old text.

**Expected:** the dialog reloads the part (`chatMessageService.find`) before reading and writing review data, and only
changes the review attribute (e.g. a service method that loads, sets the attribute and saves).

---

~~## BUG-18 - Lorebook VCS: revision history is lost when several entries are open (high)~~

**Where:** `plugins/lorebookvcs` - `LoreEntryRevisionPanel.initData()` / `syncCurrentRevisionFromEntry()` /
`onDetach()`, `Lix buorebookVCSService.saveVCSData()`.

Each revision panel loads the history of the **whole lorebook** (`vcsData`) once, when the entry details are opened,
and every action saves that whole object back. With the details of two entries open, the panels hold separate copies:
a snapshot created in entry A is overwritten by the next save from entry B - including B's automatic save when its
details are closed (`onDetach`).

**Steps:** open the details of entries A and B. In A click **+** (2 revisions). Close B's details. Reopen A: it has one
revision again.

**Expected:** every save re-reads the stored history and changes only the entry's own part
(`getVCSData` → update entry → save), or the history is stored per entry.

---

~~## BUG-19 - Lorebook VCS: "Lorebook Revisions" panel works on the wrong lorebook (medium)~~

**Where:** `plugins/lorebookvcs` - `LorebookVCSExtension` (decorator on `LorebookView.create`),
`LorebookVCSGlobalPanel`.

The panel is created once, in `LorebookView.create()`, for the lorebook selected at that moment, and keeps it. After
selecting another lorebook in the editor, export and import still use the first one (an import overwrites its history).
When the user has no lorebook when the editor is created (new account), the panel never appears until the page is
reloaded.

**Expected:** the panel follows the selected lorebook (read `lorebookView.getCurrentLorebook()` when used, or hook
`updateSelectedLorebook`).

---

~~## BUG-20 - Lorebook VCS: history import problems (medium)~~

**Where:** `plugins/lorebookvcs` - `LorebookVCSService.parseSillyTavernVCSJson()`, `LorebookVCSGlobalPanel`.

- **SillyTavern import turns keys into tags.** `parseSTRevision` puts the entry's `key` and `keysecondary` into the
  revision's *tags*. Switching to such a revision makes the trigger words tags of the entry, so it only activates in
  books tagged with them - i.e. practically never. Keys should become the filter, as in the core SillyTavern import
  (`SillyTavernEntryConverter.filter`); filter and insertion mode are currently taken from the current entry instead.
- **History can't be moved to a copy of a lorebook.** Revisions are keyed by entry uuid. Importing a lorebook
  (export file or book backup) creates entries with new uuids, so an exported history imported onto the copy matches
  nothing and shows no revisions - although the README recommends the history export for moving it.
- **Import replaces the whole history** of the lorebook without saying so.
- **Errors crash:** `UIUtils.internalServerError(null, e)` dereferences the null `Localization`, so a broken import file
  ends in a `NullPointerException` instead of an error dialog.

**Expected:** keys → filter; entries matched by uuid, then by name/order; a confirmation that existing history is
replaced; the injected `loc` passed to `internalServerError`.

---

~~## BUG-21 - Side Query: lorebook context, tab naming and chat log range (low)~~

**Where:** `plugins/sidequery` - `SideQueryService.buildPromptPayload()`, `SideQueryTabContent`, `SideQuerySettingsForm`.

- **Lorebook** sends every enabled entry of the book's lorebook, but only of the root lorebook (sub lorebooks are
  ignored), also when the lorebook is disabled, and with the content unrendered (macros such as `{{user}}` reach the
  model literally).
- **Enable AI Tab Naming** is saved in the profile but nothing reads it: tabs are always named *Tab N*.
- **Chat Logs** *from/to* are 0-based indexes into the branch (default 0-5 = parts #1-#6), while the outline numbers
  parts from 1, and the prompt labels them *Message #1*... There is no hint in the UI.
- **Instructions Before User Input** are appended to the system message, not put before the user's question as the
  label (and the README) say.
- Saving / deleting saved queries calls `UIUtils.internalServerError(null, e)` - a `NullPointerException` on error.

**Expected:** include sub lorebooks, skip disabled ones and render entries like the generation does; implement or
remove AI tab naming; 1-based range matching the outline; instructions inserted before the last user message (or the
label changed); pass `loc`.

---

~~## BUG-22 - Settings: the Default Summary Prompt is never saved (medium)~~

**Where:** `ui/workspace/parts/UserPart.java` - `refresh()` and `save()`.

*Settings → Templates → Default Summary Prompt* is shown (with placeholder and hints), but `refresh()` doesn't load
`userSetting.getDefaultSummaryPrompt()` into it and `save()` doesn't call `setDefaultSummaryPrompt(...)` (nor validate
it). Whatever is entered is dropped on *Save*; summaries always use the book's summary prompt or the built-in default.
Together with BUG-1 there is currently no reliable way to change the summary prompt.

**Steps:** enter a summary prompt in Settings, *Save*, switch to another tab and back → the field is empty; a summary
uses the built-in prompt (*Show* the request in the mock LLM or check the summary's style).

**Expected:** loaded, validated and saved like the other templates.

---

~~## BUG-23 - Password-less accounts are allowed silently, also on servers (high, security)~~

**Where:** `UserDialog.save()`, `UserServiceImpl.authenticate()`, `PermissiveLoginOverlay`.

Accounts without a password are a deliberate feature: `UserDialog.save()` accepts two empty password fields,
`authenticate()` accepts an empty password when no hash is stored, and `PermissiveLoginOverlay` disables the login
form's required check. It's convenient for the single-user desktop app, but nothing limits or explains it: the
first-start dialog (also on a Docker server reachable from the network) and *Admin → Users* accept an empty password
without a warning - the fields are even marked required. An administrator account created that way can be opened by
anyone who knows or guesses the user name.

**Steps:** on an empty installation, enter a user name in the first-start dialog, leave both passwords empty, *OK* →
log in with that name and an empty password.

**Expected:** allow password-less accounts only where it is safe (desktop app / listening on `127.0.0.1`) or behind an
explicit setting; otherwise require a password. At least warn when an account is saved without one, and drop the
*required* marker if empty passwords stay allowed.

---

~~## BUG-24 - Change Password doesn't ask for the current password or end other sessions (medium, security)~~

**Where:** `UserDialog` (self edit from the workspace footer), `UserServiceImpl.changePassword()`.

- Changing one's own password doesn't require the current password, so anyone with access to an open session can take
  over the account.
- `changePassword()` keeps all saved logins (*Save login* cookies, valid for 30 days): their secrets don't depend on
  the password. Changing the password after a device was lost or the password leaked doesn't log that device out.

**Expected:** a *Current password* field for self edits; `changePassword()` clears the user's saved logins (except,
optionally, the current device).

---

~~## BUG-25 - Settings: unsaved changes are dropped silently (low)~~

**Where:** `UserPart.onTabSwitched()` → `refresh()`, `UserPart.openImportBackupAsNewDialog()`.

- Every switch to the *Settings* tab reloads it from the database, and leaving it doesn't warn about unsaved changes:
  edit a template, look something up on the *Books* tab, come back → the edit is gone. Extension settings forms are
  refreshed the same way.
- *Import Backup as New Book*: when the file is uploaded before *New Book Name* is filled in, the upload is discarded
  with *Cannot save form.* and has to be repeated.

**Expected:** keep unsaved values (or ask before discarding them); in the import dialog, keep the uploaded file until a
name is entered, or ask for the name after the upload.

---

~~## BUG-26 - Deleting a user leaves all their data in place for good (medium)~~

**Where:** `UserServiceImpl.deleteUser()`, `CleanupServiceImpl` (`OwnedEntity.owner` is a `STRONG` reference).

`deleteUser()` only soft-deletes the user (and frees the login). Their books, lorebooks, tags, inference providers
(with API keys), protocols and settings stay live. Nobody can see or delete them any more - all lists are per owner -
and *Cleanup* can't remove them: the live data blocks the deleted user (`deletedUserOwningDataIsBlocked`), and the
data itself isn't deleted, so nothing ever becomes purgeable. The only way to get rid of it is to edit the database.

**Steps:** create a user with a book, delete the user, run *Cleanup → Analyze* → the user is *Deleted objects still in
use*, referenced by the book; the book never shows up as deleted.

**Expected:** deleting a user offers to delete their data as well (soft-delete everything they own, so Cleanup can
purge it), or Cleanup treats data owned by a deleted user as deleted.

---

~~## BUG-27 - Extensions: updating, failed loads and uploads (medium)~~

**Where:** `AdminPart.createExtensionsTab()`, `OsgiServiceImpl.installPackage()` / `getBundles()`.

- **Updating an extension doesn't work while running.** Uploading a new version under the same file name overwrites
  the JAR, but `installBundle` with the same location returns the bundle that is already installed - the old code keeps
  running until the next start. Under a different file name, both versions are installed and active (duplicate menu
  items, two settings panels).
- **Extensions that fail to start are invisible.** The list only shows bundles that registered a
  `MarginaliaExtension` service; a JAR that fails (wrong version, missing dependency) is not listed, so it can't be
  unloaded from the UI and is retried on every start. The upload only shows *Failed to load extension: ...* once.
- **Unload** has no confirmation.
- The JAR is stored under the file name sent by the browser (`new File(extensionsPath, metadata.fileName())`) without
  sanitizing it.

**Expected:** an upload of a bundle whose symbolic name is already installed replaces it (uninstall + install); bundles
that failed are listed with their state and can be removed; confirmation before unloading; file names sanitized like
database backup uploads.

---

~~## BUG-28 - Database backups: the schedule can't be switched off without a valid cron expression (low)~~

**Where:** `DatabaseBackupPanel.saveSchedule()`, `DatabaseBackupServiceImpl.updateSchedule()`.

`saveSchedule()` requires `previewSchedule()` to succeed and `updateSchedule()` always parses the expression, also when
*Create backups automatically* is unchecked. With an empty or broken expression, the schedule can't be saved as off.

**Expected:** the expression is only required when scheduled backups are enabled. A non-empty expression is always
validated and kept while the schedule is off, so it can be turned on again later.

---

~~## BUG-29 - API keys are stored in plain text (low, security)~~

**Where:** `OpenAICompatible.apiKey` (plain `@Lob` column).

API keys of all users are stored unencrypted in the database, and so in every database backup an administrator can
download. Anyone with a copy of `marginalia.sqlite` or a backup has all keys. The UI hides keys from other users, but
an administrator can read them from a backup.

**Expected:** encrypt keys at rest (e.g. with a key kept outside the database, in the data folder or an environment
variable), or at least document it next to the database backups.

---

~~## BUG-30 - Production logs at DEBUG level, including story content (low)~~

**Where:** `marginalia/src/main/resources/log4j.properties`.

The shipped configuration sets `com.github.enerccio`, `org.apache.catalina` and `org.springframework.web` to `DEBUG`.
Every generation logs each pipeline step, and `ProcessLorebookStep` logs the activated lore (`Final lorebook content:
...`) - users' story material ends up in the server log (`docker compose logs`, `desktop/logs/marginalia.log`), and the
logs grow quickly. The line `logger.com.vaadin=DEBUG` lacks the `log4j.` prefix and has no effect.

**Expected:** `INFO` for production (DEBUG for development only, e.g. via a system property or a separate file), no
user content above DEBUG.

---

~~## BUG-31 - `mvn jetty:run` can't start the application (medium)~~

**Where:** `marginalia/pom.xml`, `jetty-maven-plugin` 11.0.7.

The POM still declares the Jetty 11 Maven plugin. Jetty 11 implements Servlet 5 (Jakarta EE 9), while Spring 7 and
Vaadin 25 need Servlet 6.1, and its annotation scanner (ASM) can't read the Java 21+ class files of the libraries.

**Steps:**
1. `cd marginalia && mvn -DskipTests jetty:run`
2. The log fills with `Error scanning entry com/vaadin/flow/component/react/ReactAdapterComponent.class ...
   Caused by: java.lang.IllegalArgumentException: Unsupported class file major version 65`; the web application
   doesn't start (Jetty itself listens, every request fails).

**Expected:** a working one-command development run - replace it with the Jetty 12 EE10 plugin
(`org.eclipse.jetty.ee10:jetty-ee10-maven-plugin`, with the JVM options the agent needs), or remove the plugin.

---

~~## BUG-32 - A plain `mvn package` builds a development-mode WAR (low)~~

**Where:** `marginalia/pom.xml` (`vaadin-maven-plugin`, `maven-war-plugin`), `README.md` → *Build from source*.

Only the Dockerfile (copies `flow-build-info.json.PRODUCTION`) and the `desktop` profile (swaps the file) produce a
production WAR. The WAR from `mvn package`, which the README presents as the build result, contains a
`flow-build-info.json` with `"productionMode": false` and absolute paths of the build machine (`npmFolder`,
`frontendFolder`). Deployed to another server it runs in Vaadin development mode (dev tools, slower, more memory).
A `flow-build-info.json` left in `src/main/resources` (ignored by Git) silently overrides the generated one in every
later build.

**Steps:**
1. `mvn package -DskipTests`
2. `unzip -p target/marginalia-1.0.0.war WEB-INF/classes/META-INF/VAADIN/config/flow-build-info.json`
3. `"productionMode": false`, paths from the build machine.

**Expected:** a `production` profile (or production by default and a `dev` profile) instead of copying files around;
the README names the right command for a deployable WAR.

---

~~## BUG-33 - Extensions: Felix's bundle cache in the extensions folder is never cleaned (low)~~

**Where:** `OsgiServiceImpl.start()`.

Felix's storage is the `extensions` folder itself (`FRAMEWORK_STORAGE = extensionPath`), so `bundleN/` folders with
copies of the JARs and `cache.lock` sit next to the extension JARs. The clean-up doesn't work:
`FRAMEWORK_STORAGE_CLEAN` is set to `"true"`, but Felix only knows `"onFirstInit"`, and the folder deleted before
start is `org.eclipse.osgi` (Equinox's cache, not Felix's). Felix therefore restores the bundles installed in the
previous run from its cache, and `start()` starts all of them:

- An extension JAR deleted from the folder while Marginalia is stopped still loads from the cached copy.
- A JAR replaced with a newer version while stopped keeps running the old version: `installBundle` with an
  already-cached location returns the cached bundle. The manual says JARs copied into the folder are picked up on the
  next start.

**Steps** (derived from the code and the cache contents in `~/.marginalia/extensions`, not run end to end):
1. Load an extension in *Admin → Extensions*, stop Marginalia.
2. Delete its JAR from `~/.marginalia/extensions` (the `bundleN/` folders stay), start Marginalia.
3. The extension is still active and listed.

**Expected:** a separate storage folder (e.g. `extensions/.cache` or a temp folder) cleaned on every start
(`onFirstInit`), so the JARs in the folder are the single source of truth.

---

~~## BUG-34 - Application migrations are never run (low)~~

**Where:** `bound/ApplicationInitializer.initializeAndMigrateAppDB()`.

The second check compares the wrong variable: `if (dbVer < appVersion)` instead of `if (appVer < appVersion)`. Once
the database version has caught up, `APP` migrations are skipped even when `AppSettings.appVersion` is behind. No
migrations are registered yet (both versions are hard-coded to `1` in `container-config.xml`), so it has no effect
today, but the first application migration will silently not run.

**Expected:** `appVer < appVersion`, and a test with a registered migration.

---

~~## BUG-35 - Save login cookies are readable by scripts (low, security)~~

**Where:** `LoginCheckRoute.addRememberMeCookies()`.

The *Save login* cookies, including the plain secret (`..._rememberMe_secret`), are created with `setSecure(true)`
but without `HttpOnly` and without `SameSite`. Any script running in the page (an extension's frontend code, any
future XSS) can read the secret and log in as the user for 30 days, also after
the session ends.

**Expected:** `HttpOnly` and `SameSite=Lax` (or `Strict`) on all remember-me cookies.

---

~~## BUG-36 - The Docker build context includes the server's data folder (low)~~

**Where:** `.dockerignore`.

`.dockerignore` only excludes `marginalia/target/` and `.idea/`. `./data` - the data folder of the Docker server,
with the database, all book and database backups and extension JARs - sits next to the `Dockerfile` and is sent to
the Docker daemon as part of the build context on every `docker compose up -d --build` (the documented update
command). The image doesn't copy it, but builds get slower as the data grows, and users' data is handed to the
builder (a remote builder, if one is configured). `docs/`, `manual/` and `marginalia/plugins/*/target/` are sent too.

**Steps:**
1. Run the Docker server for a while (or put a large file into `./data`).
2. `docker compose build` - *transferring context* includes the whole `./data` folder.

**Expected:** `data/`, `docs/`, `manual/`, `marginalia/plugins/*/target/` and other build output excluded in
`.dockerignore`.

---

~~## BUG-37 - Tag relation indexes are on the wrong columns (low)~~

**Where:** `TagRelation` (`@Table(indexes = ...)`), `migration/V1__initial.sql` (table `t2e`).

The two indexes of `t2e` are `(id, clazz)` and `(id, clazz, negative)`. `id` is the primary key, so they never help.
All lookups filter by `objectId` + `clazz` (+ `negative`) - the tags of a book, lorebook or entry,
`JpaTagRelationRepository.findTagsForObject` - or by `tag_id` + `clazz` (`findObjectIdsForTag`), and there is no
index on either. Every lookup scans the whole table. `LorebookService.fillEntries` runs two such queries per entry on
every generation, so large lorebooks get slower as the number of tagged objects grows.

**Expected:** indexes on `(objectId, clazz, negative)` and `(tag_id, clazz)` (entity annotation + a Flyway migration
that drops the old ones).

---

~~## BUG-38 - Dates in extended attributes are stored in the server's local time zone (low)~~

**Where:** `ExtendableEntityListener.AttributesAccessor` (`SimpleDateFormat("yyyy.MM.dd'Z'HH:mm:ss.SSS")`).

`Date` extended attributes are written and parsed with the JVM's default time zone; the `'Z'` in the pattern is a
literal and suggests UTC, but no time zone is set. The same database read by a JVM in another time zone - a database
backup of the Docker server (UTC) restored in the desktop app, a server whose `TZ` changes, a book backup imported on
another installation - shifts all these dates by the offset. During the DST change the repeated hour is ambiguous.
Affected: `AppSettings.lastScheduledBackup` / `backupScheduleChanged` (missed scheduled backups are caught up or skipped
wrongly), the timing fields of parts (`request`, `ttft`, `reasoningEnd`) and dates kept by extensions.

**Expected:** write UTC (set the formatter's time zone, keep reading old values as before or with a marker), or store
epoch milliseconds.

---

~~## BUG-39 - Restoring an uploaded SQLite file that isn't a Marginalia database stops Marginalia from starting (medium)~~

**Where:** `DatabaseBackupServiceImpl.importBackup()` / `scheduleRestore()`, `Configuration.applyPendingRestore()`.

*Upload Backup* only checks the 16-byte SQLite header. Any SQLite database (another application's, a corrupted or
truncated copy) is accepted and can be scheduled for restore. On the next start it is swapped in before anything
checks it; Flyway baselines it at V1 (`baselineOnMigrate`) because it has no history table and fails on V2
(`ALTER TABLE messages ...` - no such table), so the Spring context fails and no page is served. The UI can't help any
more; the admin has to stop the server and move `db-backups/pre-restore-*-marginalia.sqlite` back by hand (only
documented in the developer guide). A Marginalia backup from a newer version than the running code has a similar
risk.

**Steps** (derived from the code, not run end to end):
1. *Admin → Database Backups → Upload Backup* with any non-empty SQLite file that isn't a Marginalia database.
2. *Restore on restart*, restart Marginalia.
3. Startup fails with a Flyway migration error.

**Expected:** check the upload (or the backup before scheduling the restore): `flyway_schema_history` present and not
newer than the running code, the core tables present, `PRAGMA integrity_check`. When the restored database fails to
migrate on start, put the previous database back automatically and log why.

---

~~## BUG-40 - Every streamed chunk rewrites the whole part and the book (low)~~

**Where:** `InferenceStep` (`CHUNK_RECEIVED` / `REASONING_CHUNK_RECEIVED` handlers), `SummaryServiceImpl.createSummary()`.

For every chunk the model streams (often a single token), the generation saves the part *and* the book:
`chatMessageService.save(chatMessage)` and `manuscriptService.save(manuscript)` - two write transactions, each
serializing the entity's whole extended content. The part's JSON includes `builtPrompt`, the complete prompt sent to the
model (often tens of kilobytes), so a 1,000-token answer writes the prompt to the database about a thousand times;
the work grows with the square of the answer length. Summaries do the same (`self.save(summary)` per chunk). On SQLite
every one of these writes takes the single write lock, slowing down other users' saves on a server, and grows the
WAL file. Saving the book on every chunk also writes the generation's copy of the book back each time - a change to
the same book made meanwhile in another browser tab (e.g. its prompts) is overwritten.

**Expected:** keep the text in memory while streaming and save at most every few hundred milliseconds (and on
completion / cancel / error); don't save the book during streaming, or only the fields that changed.

---

~~## BUG-41 - Tokenizer strategy cache can't be invalidated and may stick to the fallback (low)~~

**Where:** `TokenizerServiceImpl`.

The working tokenizer strategy is cached per provider under the key `<ai id>` (`getCacheKey`), but `invalidateCache`
removes `"ai_id_" + id` - a key that never exists - and nothing calls `invalidateCache` anyway. The cache is only
dropped when the cached strategy throws. JTokkit is the last candidate and never fails, so if a provider's server was
unreachable or returned errors on the first count (e.g. the local llama.cpp server wasn't started yet), JTokkit is
cached for that provider until Marginalia restarts, and token counts stay approximate after the server is back. The
same happens after changing a provider's URL or model to one whose server supports a better tokenizer.

**Expected:** one key format; `AIDialog` (or `AIService.save`) calls `invalidateCache` when a provider is saved; the
local JTokkit fallback is not cached (or only for a short time).

---

~~## BUG-42 - The prompt can exceed the model's context by one part (medium)~~

**Where:** `PrepareContentStep` (loop that fills `MANUSCRIPT_CHRONICLE`).

Earlier parts are added newest first while `tokens <= limit - 256`, but the check happens *before* a part is added:
once the running total is just under the threshold, the next part is added whatever its size. The final prompt can
therefore be larger than `limit` (context minus response tokens) by up to one part (+100). With parts of a few
hundred to a few thousand tokens this happens regularly once a book is longer than the context. Depending on the
provider the request then fails with a context-length error, or the server silently
truncates the prompt or the answer.

**Steps** (derived from the code): context 4096, response 1024 → `limit` 3072, threshold `limit - 256` = 2816. Base
prompt 1700 tokens, parts of 900 tokens (counted as 1000). 1700 ≤ 2816 → add a part (2700); 2700 ≤ 2816 → add another
(3700). The prompt is ~3700 tokens for a limit of 3072.

**Expected:** add a part only when `tokens + part + 100 <= limit - 256` (and keep counting with the same estimate the
check uses).

---

~~## BUG-43 - Stop doesn't close the connection to the model (medium)~~

**Where:** `InferenceStep` (`onChunk`), `OpenAICompatibleInferenceService.OpenAIInferenceAsyncController`.

`InferenceStep` ignores the `CancellationToken` returned by `InferenceService.stream()` and never calls
`terminateInference()`. When the user presses *Stop* while the stream waits for the next chunk (most of the time), the
next chunk arrives, `onChunk` sees the generation's token cancelled, jumps to `CLEANUP` and returns without calling
`continueInference()` - the controller is never asked for another chunk, so it never notices the cancellation and never
closes the HTTP response. The model keeps generating until it reaches its limit (and paid APIs bill for it), and the
connection stays open until the server finishes or a timeout closes it. The same happens when the generation thread
is interrupted.

**Steps** (derived from the code): generate a long part with a local llama.cpp server, press *Stop* after a few
words → the server log shows the request running to `max_tokens`.

**Expected:** on cancellation `InferenceStep` calls `inferenceController.terminateInference()` (or cancels the stream's
token, linked to the generation's token).

---

~~## BUG-44 - Model streams block the common ForkJoinPool (low)~~

**Where:** `OpenAICompatibleInferenceService.stream()` / `OpenAIInferenceAsyncController.continueInference()`.

Starting the request (`createStreaming`) and reading every chunk (`iterator.next()`, a blocking network read) run in
`CompletableFuture.runAsync(...)` without an executor, i.e. on the JVM-wide common `ForkJoinPool`. Its size is the number
of CPUs minus one (at least 1). On a small server (1-2 CPUs, a typical Docker host) one stream waiting for its next
token - or for the first token of a reasoning model, which can take minutes - occupies the only thread: other users'
generations and summaries can't start or read their chunks until it returns, and any other code using the common pool
(parallel streams, other `CompletableFuture`s) is blocked too.

**Expected:** a dedicated (cached or bounded) executor for inference I/O, like the generation thread pool.

---

~~## BUG-45 - The summary prompt is rendered twice with the same variables (low)~~

**Where:** `SummaryServiceImpl.createSummaryPayload()`.

The summary prompt is rendered once without the text (to check that the prompt fits) and once with it, both times
with the same `SummaryTemplateData` and template context. Macros with side effects run twice: `{{.n++}}` /
`{{incvar::n}}` / `{{addvar}}` in the summary prompt produce a value one step too far, and a `{{setvar}}` before a
`{{getvar}}` of the same name sees its own earlier value. The generation does the same estimate on a fork of the
context (`PrepareContentStep`, `TemplateContext.fork()`); the summary doesn't.

**Steps:** set a book's summary prompt to `Count: {{.n++}}` and generate a summary → the request contains `Count: 1`
instead of `Count: 0` (variables of a part start from its stored values, so the exact numbers depend on the branch).

**Expected:** render the estimate with `templateContext.fork()`, like the master template.

---

~~## BUG-46 - Deleting the last part makes the whole story disappear (high)~~

**Where:** `ManuscriptStoryPart.ChatMessageCard.createMenuItems()` (*Delete*), `ChatMessageServiceImpl.deleteNodeAndMigrateChildren()`.

`deleteNodeAndMigrateChildren(message, currentManuscript, false)` moves the book's active leaf to the deleted part's
parent, but only on the `Manuscript` object passed in - the story editor's detached copy, which is never saved. The
editor then reloads the book (`parent.refreshManuscript()`), whose `activeLeaf` in the database still points to the
soft-deleted part. `getBranchFromLeaf` starts from a deleted row and returns nothing, so the story tab is empty - all
parts seem to be gone (they are still in the database). The next *Generate* uses the deleted part as parent
(`find(id)` ignores `deleted`): the new part continues a branch that can't be read, and the model gets no story at
all. The existing test (`ChatMessageCrudTest.deleteNodeMovesChildrenToParent`) only checks the in-memory object.

**Steps (verified with a test against the real services):** a book with parts "A" → "B" (B active), delete "B" the way
the editor does → reloaded book: active leaf = "B", `deleted=true`, branch length 0.

**Expected:** `deleteNodeAndMigrateChildren` saves the book when it changes the active leaf (or the editor saves it);
a test that reloads the book from the database.

---

~~## BUG-47 - One broken extension stops all extensions after it from loading (medium)~~

**Where:** `OsgiServiceImpl.start()`, `startBundleInternal()`.

`start()` installs every JAR in `extensions/` and then starts the bundles in one loop, without catching anything per
bundle. A JAR that can't be installed (not a bundle, damaged, or a second copy of an installed plugin under another
file name - Felix refuses a duplicate symbolic name and version) aborts the install loop, so no extension loads at
all. A bundle whose activator or `onExtensionLoad` throws aborts the start loop, so the bundles after it are not
started. The exception ends in `onApplicationEvent`, which only logs it; observers never get `afterServiceStarted`.
The bundled plugins make this more likely: their `onExtensionLoad` reports errors with
`UIUtils.internalServerError(loc, e)`, which opens a dialog and can't work at startup (there is no UI).

**Steps:**
1. Copy `chaptermarker-1.0.0.jar` in `~/.marginalia/extensions` to `chaptermarker-copy.jar` (or put any non-JAR file
   named `x.jar` there).
2. Restart Marginalia → *Admin → Extensions*: none (or only some) of the extensions are listed and active; the log
   shows a `BundleException`.

**Expected:** each JAR is installed and started on its own; a failing one is logged (with its file name) and skipped,
the others load.

---

~~## BUG-48 - Uploading a new build of an installed extension doesn't update it and loads it twice (medium)~~

**Where:** `AdminPart.createExtensionsTab()` (upload handler), `OsgiServiceImpl.installPackage()`.

The upload is written over the JAR with the same file name and passed to `context.installBundle("file:" + path)`.
OSGi returns the **already installed** bundle for a location that is installed, so the old code keeps running, and
`startBundleInternal` calls `onExtensionLoad` a second time on the same extension object. The plugin registers its
decorators again (it overwrites the fields holding the first ones, so *Unload* removes only the second set and the
first keeps running until restart), and `componentCallbacks` for the extension is replaced, losing the callbacks
bound so far. The new JAR takes effect only after a restart. With a different file name, the install fails with a
duplicate-bundle error, but the uploaded JAR stays in `extensions/` and triggers BUG-47 on the next start.

**Steps:**
1. Load `reviewer-1.0.0.jar` in *Admin → Extensions*.
2. Change a label in the Reviewer plugin, rebuild, load the new `reviewer-1.0.0.jar` again: success message, but
   the old label is still shown. *Unload* it and open a book: the *Review* menu item is still added.

**Expected:** an upload of an installed bundle (same location or same symbolic name) updates it: unload the old one
(`stopBundleInternal`), `bundle.update()` or uninstall + install, load the new one. A failed install deletes the
uploaded file.

---

~~## BUG-49 - Lorebook VCS: the Lorebook Revisions panel works on the wrong lorebook (high)~~

**Where:** `plugins/lorebookvcs/.../LorebookVCSExtension.java` (decorator of `LorebookView.create`),
`LorebookVCSGlobalPanel`.

The panel is created once per `LorebookView`, in `create()`, with the lorebook that is current at that moment, and
keeps it. In the *Lorebooks* tab that's the first lorebook (by name); choosing another lorebook in the combo box
doesn't touch the panel. Export then exports the first lorebook's history, and both imports write into the first
lorebook - replacing its whole history. The SillyTavern import has no check at all; the plugin's own import warns
about a UUID mismatch, but names the wrong lorebook as "active".

**Steps:**
1. Two lorebooks *A* and *B* with Lorebook VCS installed, open the *Lorebooks* tab (A is selected).
2. Select *B*, use *Lorebook Revisions → Import SillyTavern* with a history file.
3. Select *A*: its entries show the imported revisions, A's own history is gone; B has none.

**Expected:** the panel acts on `lorebookView.getCurrentLorebook()` at the time of the action (or is rebuilt when the
lorebook changes).

Duplicate of BUG-19, fixed with it.

---

~~## BUG-50 - Lorebook VCS and Side Query: errors end in a NullPointerException (low)~~

**Where:** `LorebookVCSGlobalPanel` (fixed with BUG-20), `SideQueryTabContent`, `SideQueryView` - six calls of
`UIUtils.internalServerError(null, e)`.

`internalServerError` starts with `loc.getValue(...)`, so with `null` it throws a `NullPointerException` instead of
logging the error and showing the error dialog. The original exception is lost from the log.

**Steps:** in Lorebook VCS, *Import* a file that is valid JSON but not a history (e.g. `[]`) or make the save fail →
the log shows an NPE from `UIUtils.internalServerError`, no error dialog.

**Expected:** pass the injected `Localization`.

---

~~## BUG-51 - The OSGi framework is never stopped (low)~~

**Where:** `OsgiServiceImpl` - `stop()` and `onExit()` have no callers, the bean has no destroy method.

When the web application stops, Felix keeps running: bundles are not stopped, `onExtensionUnload` and the activators'
`stop` are not called, and on a servlet container that redeploys the WAR without restarting the JVM (Tomcat, Jetty
hot deploy) the old framework, its threads and the old application class loader stay in memory while the new
deployment starts a second framework on the same `extensions/org.eclipse.osgi` storage (which `start()` deletes).

**Steps:** deploy the WAR to Tomcat, redeploy it from the manager, take a heap dump (or watch the threads): two Felix
frameworks (`FelixDispatchQueue`, `FelixFrameworkWiring` threads...).

**Expected:** stop the framework when the root context closes (`ContextClosedEvent` or `destroy-method="onExit"`).

---

~~## BUG-52 - Lorebook VCS: its panels stay after the extension is unloaded (low)~~

**Where:** `LorebookVCSExtension.onExtensionUnload()`.

Unload only unregisters the decorators. The *Lorebook Revisions* panel and the revision bars in open lorebook views
stay, and keep working - they still create, switch and import revisions - with the classes of an uninstalled bundle,
until the view is rebuilt. Reviewer and Side Query remove their components on unload.

**Steps:** open the *Lorebooks* tab, *Admin → Extensions → Unload* Lorebook VCS, go back: the panel and the revision
bars are still there and work.

**Expected:** track the added panels (or `bindAttachableComponent`) and remove them on unload.

---

~~## BUG-53 - Unloading an extension changes other users' screens without their session lock (low)~~

**Where:** `ReviewerExtension.onExtensionUnload()`, `SideQueryExtension.onExtensionUnload()`,
`OsgiServiceImpl.stopBundleInternal()` (bound component callbacks).

Unload runs on the administrator's request thread. Reviewer and Side Query then remove menu items, tabs and panels
from components of **every** open UI, of every user, directly - without `ui.access(...)`. Vaadin components may only be
changed while their session is locked: the changes race with that user's own requests, can fail with an exception
from Vaadin's state tree, and are not pushed (the other user sees them only on their next interaction).

**Steps:** user 1 opens a book; the administrator, in another browser, unloads Reviewer → in user 1's window the
*Review* menu item is still there until user 1 clicks something (and the log may show errors from the state tree).

**Expected:** each change made through `component.getUI().ifPresent(ui -> ui.access(...))`; document the same for
`bindAttachableComponent` callbacks.

---

~~## BUG-54 - `bindAttachableComponent` forgets the callback when a component is moved, and keeps unloaded extensions (low)~~

**Where:** `OsgiServiceImpl.bindAttachableComponent()`, `stopBundleInternal()`.

The callback is removed on the component's first detach and never added back on a later attach, so a component
that is detached and attached again (moved to another layout, a tab sheet that detaches hidden tabs, a dialog
reopened) is not cleaned up on unload. `stopBundleInternal` runs the callbacks but leaves the extension's entry in
`componentCallbacks`, so every unloaded extension object - and through it its bundle class loader - stays referenced
until restart. Calling `bindAttachableComponent` for an extension that isn't loaded throws a
`NullPointerException`. (No bundled plugin uses the method yet.)

**Expected:** re-register the callback on attach (or bind for the component's lifetime, not its first attachment),
remove the extension's entry after running its callbacks, and fail with a clear message for an unknown extension.

---

~~## BUG-55 - Extensions that failed to start are not listed and can't be unloaded (low)~~

**Where:** `OsgiServiceImpl.getBundles()`, used by *Admin → Extensions*.

The list contains only bundles that registered a `MarginaliaExtension` service. A bundle that is installed but
failed to start (an exception in its activator, BUG-47) or isn't a Marginalia extension at all is not shown, so the
administrator doesn't see it and can't unload it - the JAR has to be deleted from `extensions/` by hand.

**Steps:** put a plugin whose activator throws into `extensions/`, restart → it's not in the list; the only trace is
in the log.

**Expected:** list every installed bundle with its state (*INSTALLED*, *RESOLVED*, *ACTIVE*...) and allow unloading
any of them.

---

~~## BUG-56 - `mvn jetty:run` still can't start: the Jetty plugin can't be resolved (low)~~

**Where:** `marginalia/pom.xml`, `jetty-ee11-maven-plugin` 12.1.14 and its `<dependencies>`.

The fix of BUG-31 switched to the Jetty 12 plugin, but kept two plugin dependencies, `org.eclipse.jetty:jetty-server`
and `org.eclipse.jetty:jetty-servlet` 12.1.14. `jetty-servlet` doesn't exist in Jetty 12 (the servlet support moved
to the environment modules, `org.eclipse.jetty.ee11:jetty-ee11-servlet`), so Maven can't resolve the plugin.

**Steps:** `cd marginalia && mvn -DskipTests jetty:run` → `Plugin org.eclipse.jetty.ee11:jetty-ee11-maven-plugin:12.1.14
or one of its dependencies could not be resolved: Could not find artifact org.eclipse.jetty:jetty-servlet:jar:12.1.14`.

**Expected:** the plugin starts the application; drop the extra dependencies (the plugin brings its own) and document
`mvn jetty:run` in *Building from source* (with the JVM options from `MAVEN_OPTS`).

---

~~## BUG-57 - The desktop app logs at DEBUG level, including story content (low)~~

**Where:** `marginalia/pom.xml`, `desktop` profile; `src/main/resources/log4j.properties`.

The fix of BUG-30 swaps `log4j.properties` for `log4j.properties.RELEASE` only in the `Dockerfile`. The `desktop`
profile packages the development `log4j.properties`, so the desktop app logs
`com.github.enerccio`, `org.apache.catalina` and `org.springframework.web` at `DEBUG` - every generation writes the
pipeline steps and the activated lore (`Final lorebook content: ...`) into `~/.marginalia/desktop/logs/`.

**Steps:** `mvn package -Pdesktop -DskipTests`, start the app, generate a part, open the log in
`~/.marginalia/desktop/logs/`: DEBUG lines with the lore text.

**Expected:** the desktop WAR contains the release logging configuration, like the Docker image (ideally decided by the
build, not by copying files in the `Dockerfile`).

---

~~## BUG-58 - Release archives are named after the POM version, not the tag (low)~~

**Where:** `.github/workflows/desktop.yml`, `marginalia/pom.xml` (`<version>1.0.0</version>`, `desktop.appVersion`).

The `release` job attaches the desktop archives to the release of any `v*` tag, but the archive names
(`marginalia-<version>-<os>-<arch>...`) and the version of the native app (`jpackage --app-version`) come from the
POM. Nothing checks that they match: pushing `v1.1.0` without changing the POM publishes `marginalia-1.0.0-*`
archives, and the app reports 1.0.0.

**Steps:** push a tag `v1.0.1` on a commit with POM version `1.0.0` → the release *v1.0.1* contains
`marginalia-1.0.0-*.tar.gz` / `.zip`.

**Expected:** the workflow fails when the tag doesn't match the POM version (or sets the version from the tag,
`mvn versions:set -DnewVersion=${GITHUB_REF_NAME#v}`).

---

~~## BUG-59 - Docker on Linux: Marginalia can't write to a `./data` folder created by Docker (medium)~~

**Where:** `docker-compose.yml` (`./data:/var/marginalia/.marginalia`), `Dockerfile` (runs as user `jetty`).

`./data` is ignored by Git, so on a fresh clone it doesn't exist. `docker compose up` creates the missing bind-mount
folder on the host as `root` with mode 755. The container runs as the unprivileged `jetty` user, and the bind mount
hides the folder prepared (and `chown`ed) in the image, so Marginalia can't create its database in
`/var/marginalia/.marginalia`. Docker Desktop (macOS, Windows) maps permissions differently and is not affected; the
`Dockerfile` also creates an unused `/var/marginalia/data`.

**Steps:** on a Linux host with Docker Engine: fresh clone, `docker compose up` → the log shows the database / folder
can't be created (permission denied), the application doesn't start.

**Expected:** the documented install works as is - e.g. an entrypoint that fixes the ownership of the data folder
before dropping to `jetty`, a named volume instead of a bind mount, or `user:` / instructions to create `./data` with
the right owner (`mkdir data && sudo chown <uid>:<gid> data` with the ids of `jetty` in the image, `docker compose run --rm marginalia id`) in *Server (Docker)*.

---

~~## BUG-60 - The Docker image runs Marginalia in Jetty's Jakarta EE 10 environment (low)~~

**Where:** `Dockerfile` (`--add-modules=server,http,ee10-deploy,ee10-websocket-jakarta,ee10-webapp,ee10-jsp`).

Marginalia is compiled against Servlet 6.1 (`jakarta.servlet-api` 6.1.0, Jakarta EE 11), and the desktop launcher
deploys it with Jetty's `ee11-*` modules. The Docker image (and the *Local Jetty* instructions in the developer guide,
which copy it) deploy the same WAR in the `ee10` environment, which implements Servlet 6.0. It starts, but the two
distributions run on different servlet implementations, and anything that uses a Servlet 6.1 API fails only in
Docker.

**Expected:** the image uses the same `ee11-*` modules as the desktop app (`ee11-deploy, ee11-webapp,
ee11-websocket-jakarta, ee11-jsp`); update *Building from source → Local Jetty* together with it.
