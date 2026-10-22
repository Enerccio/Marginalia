# Changelog

All notable changes to Marginalia are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Merge into previous part** (menu of a part, desktop and phone): appends the text of a part to the part before it and removes the part, for the text a model stopped in the middle of. A review (`PartMergeDialog`) shows how the texts are joined (empty line, line break, space, nothing; preselected from the end of the text), the scene setting, POV, present characters and instructions of both turns combined with the start they share kept once (editable), and the data kept on the parts (`attributes`, variables included) value by value with the choice of the later part's value (default), the previous part's value or none. The previous part keeps its summary and generation record, gets the joined text, original text (for *Highlight Generated Text*), reasoning and pictures; what followed the merged part follows it. Refused for the first part and for a part with swipes. New `ChatMessageService.previewMergeIntoPrevious` / `mergeIntoPrevious`, `PartMerge`; new `@Extendable` method `mergeIntoPrevious` of the part cards (`@PublicAPI` from 1.5.0).

## [1.4.0] - 2026-10-22

Everything below is new since 1.3.0. Migrations `V15` to `V18`; plugins have to be rebuilt with the application (`lore-generator`, `arc-outline` and `character-tracker` use the new command and agent API).

### Added

#### Agents

**Agents are experimental in 1.4.0** (the user manual says so): they were built in a few days and have had little use with real models.

An agent is an LLM that works on one book on its own (or with you in a console) through the commands of the application and a workspace of files. It never touches the book directly: it works on a hidden copy and you review the result.

- **Definition, runs and the Agents tab.** Entities `Agent` (the model profiles `AgentModelProfile`: AI + protocol + role, the first is the main one; system prompt, instructions, command allowlist, mode for destructive commands, limits of rounds, tokens and context) and `AgentRun` (status, tokens and rounds added up in SQL, language), `AgentMessage` (the transcript, with the reasoning of the model when the provider sends it), services `AgentService` / `AgentRunService` / `AgentMessageService`, migration `V16` (also the `agentic` flag of `Manuscript` and `Lorebook`, which keeps working copies out of every list of books; new `findAllRegular…` / `findAllAgentic…` methods, the old ones return both). A book has one agent. Users who may use agents (administrators, or the permission `AGENTS`) have the **Agents** tab (desktop and phone: a card per agent, the count of agents that wait for you in the tab and in the top bar, a swipe between the parts of the drawer): **Start** (a menu: *Start immediately* or *Start in console*, which makes the agent ask the author first), **Resume**, **Pause**, **Pause now**, **Stop** (keep or discard the result), **Edit**, **Clone** (to another book, with the workspace) and **Delete**. States and transitions are enforced by `AgentRunService` (`pause`, `stop`, `resume`, `finish`, `recoverInterrupted` at server start; a run never hangs in `PAUSING`).
- **The book is locked while a run works** (`ManuscriptLockedException` from every service that changes the book, `AgentRunService.findLockingRun` / `checkNotLocked`; the book list says which agent holds it). A lorebook of the book and its subbooks is locked the same way (`LorebookLockedException`, the lorebook views are read only and say which agent holds them; `LorebookEntryService.move` refuses a locked target too). A paused, stopped or finished run releases the lock; a paused run does not lock your book, you can edit it meanwhile.
- **Working copy and merge back.** Every run starts from a copy of the book (messages, summaries, tags, settings, its own copy of the lorebook tree; `ManuscriptService.createAgenticCopy`, `LorebookService.copyLorebooks`), recorded with the pairs original ↔ copy (table `agent_run_pairs`, `V17`) and a base snapshot of the original in `agent-runs/<run>/base/`. `openSession` refuses a run without a copy, so an agent can never get your book. **Review changes** (Agents tab and console) lists everything the agent changed against the book *as it is now* and *as the run started*: settings, tags, extension data, parts (also **Follows**, the place in the story), summaries, lorebooks and entries; you tick what to take (all, none, only new parts, without conflicts), see the word-by-word **Differences**, edit the text, take a line field by field, and apply: the book is changed in place after a backup, all or nothing (parts keep their addresses and lorebooks their links, deleted things go to the trash). **Discard the result** drops the copy. The data of extensions in `attributes` (of the book, parts, lorebooks and entries, also the template variables of a part) is merged place by place as a JSON tree (`domain.service.agent.jsonmerge`, `MergePlan.Type.JSON`) in `JsonMergeDialog` (versions side by side, Use agent's / book's / start's, Leave out, Put together, Split / Match for lists, a Text tab, Start over). The copy is deleted when the run is applied, discarded, failed or deleted. On **Resume** what you changed meanwhile comes into the copy (`AgentMergeService.sync`) except what the agent changed too, which stays a conflict for the review; the agent is told and has `get_sync_report`. A **Book copy** tab of the console shows the copy to read, and **Show in the copy** in the review jumps to a part or entry.
- **The runner.** `AgentRunner` works a run in a background worker in the request context of the run (`AgentRequestAttributes`, `AgentSession`; the `user` bean is the owner and the session point an `AgenticSessionPoint`), one round at a time: the system message is made at every request (prompt, instructions, a note on how the run works, the limits of the model), the answer is streamed (the model timeout is the wait for the next chunk), the tools of a round are the allowed commands, the file tools and the run tools `finish`, `ask_user`, `update_plan`, `get_run_status`, `request_compaction`, `checkpoint*`, `get_sync_report`, `view_image`, `search_manual`, `read_manual_page`. An answer without a call ends the turn (`IDLE`), a run started in the console is a conversation (`finish` reports and waits for you; **Finish** ends it). Rounds, token budget and context cap are enforced (at a limit the run pauses with the reason). At most 32 calls of one answer are made, a repeated read-only call is made once, and an error ends the answer (the calls after it are not made and the agent reads the error first). A call whose arguments were cut off is sent back as `{}` and the agent is told to write long text in smaller steps (asked again up to three times), the settings of the model are read again before each request. A command the author queued, **Approve** / **Deny** (`AgentApprovalService`, `AgentApprovalDialog`, destructive commands in the `ASK` mode) and the notices (`AgentAttentionService`, a stored `attention` flag) tell you when an agent needs you. The log tells what the agents do (never what they write).
- **Commands as functions** (`CommandFunctions`). One function per command with the parameters of all its forms in one schema (a command name that is not a valid function name gets `_` and six hex digits of its hash), results cut at 20 000 characters, errors as `Error: <message>`, parameters with `optional`, `defaultValue`, `allowed`, `min` and `max` (a number out of range is refused, not clamped), `LLMAttachment[]` parameters offered as paths of workspace images (the Lore Generator's `generate-lore-entry` takes `images`). The agent never sees a uuid: it works with **handles** (`h:m17`, stored in the pairs). `@Command(availableForAgent = false)` hides a command from agents (`/take-backup`), `@Command(readOnly = true)` marks the ones that change nothing (they need no checkpoint), `@Command(batch = BatchPolicy.BATCHABLE | SINGLE)` marks one that is made alone in its answer (the commands that call a model). **Calls in one answer** of the agent: all together, together except the ones marked alone (default), or one call per answer.
- **Roles of commands.** The **Selection** of allowed commands has *All*, *None*, *All non-destructive* and the roles **Auditor** (reads), **Lore keeper** (also writes lorebooks), **Editor** (also revises parts, summaries, book information) and **Drafter** (also adds parts and lets the model write); none has a command that removes data. The table shows the batch policy and the tokens each definition adds to every request.
- **Files, workspace and zip files.** `AgentVirtualFileSystemService` and `domain.service.agent` (`VirtualPath`, `VirtualFileSystem` over mounts, `RunFolder`: `agent-runs/<run>` and `agent-workspaces/<agent>`): the workspace belongs to the agent and stays between runs; limits for names, depth, file size, number of files and total size (`WorkspaceLimits`); writes are atomic, links never followed. Tools: `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files` (name patterns with `glob`), `copy_file`, `merge_files`, `move_file`, `delete_file`, `unzip_file` and `view_image`. `replace_in_file` and `regex_replace_in_file` also take a folder (`glob`, `max_files`, `dry_run`: a dry run unless told otherwise, all or nothing). A text argument that is exactly `@file:/path` or `@result` / `@result:3` is replaced before the command runs by the file or by the whole result of an earlier call, so long text is not written twice (`list_command_results`, `save_command_result`; the last 10 results are kept); a handle parameter takes the `handle` of such a result. `read_file` reminds the model of `@file:` when it reads a whole file. `/AGENTS.md` shows the instructions as a read only file. Zip files (up to 10 MB) are uploaded as they are and unpacked by `ZipExtractor` with the checks against traversal and zip bombs; images are recognised by their bytes, scaled to 1568 px and only the newest four stay in front of the model (a model profile has the flag **The model sees images**; **Attach image** and Ctrl+V in the console). The workspace tab can create folders and upload text files and images under the same limits.
- **Checkpoints, rewind.** Restore points of the copy and the workspace (a ZIP in the run folder): one at the start, an automatic one before each round that may change something (the last five kept) and the ones made on purpose (`checkpoint`, `checkpoint_list`, `checkpoint_restore`, `checkpoint_delete`, the **Checkpoints** tab); they share a quota (**Checkpoints of a run (MB)**). **Rewind** (to a checkpoint, or **Rewind** on a message of yours, which goes back to the newest checkpoint before it) also takes the conversation back: what came after is greyed and the agent no longer reads it. `CheckpointService.diff` / the tool `checkpoint_diff` show what changed since a checkpoint.
- **Sub-agents.** **Most sub-agents** (0 to 10) lets the agent hand a job to a thread of the run with a conversation of its own (`spawn_agent`, `await_agent`, `agent_status`, `cancel_agent`; a sub-agent can not start sub-agents, finish the run, ask you or make checkpoints). It reads the whole workspace but writes only in `/tmp/<handle>/` (its answer is `result.md` there). It can run on **another model of the agent** (`spawn_agent(profile)`; the profile has a description, can limit the commands or have none; the table **Models for sub-agents**; *Sub-agents may run on the main model*). Its conversation is stored with the run (`V18`, `agent_messages.thread`), a pause pauses the sub-agents and a resume continues them without starting anything twice. Everything counts in the one budget of the run. The **Sub-agents** tab shows each conversation. `get_run_status` lists them.
- **Context and compaction.** Warnings at 75 % and 90 % of the context, `request_compaction`, an **automatic compaction** (**Compact at**, 90 % by default, zero never) and `/compact [focus]` write a summary of the earlier work that replaces it in the request (the transcript keeps everything, folded). The **compaction prompt** and the **system prompt** follow a chain: the agent's own, else the user's (Settings > Templates), else the administrator's (Admin > Agents), else the built-in default; the **instructions** (agents.md) follow the user's default too; the fields show the default as a placeholder with **Insert Default Template**. **Write the book with the model of the agent** (on by default) decides whether the copy uses the agent's model or the book's.
- **Console** (`AgentConsoleDialog`; made for a thumb on a phone). Tabs **Run** (Markdown transcript with the time of each message, folded reasoning, calls with arguments and results; the question or report is a bubble), **Runs** (history; view, Markdown download, delete), **Workspace**, **Checkpoints**, **Context** (what the next request is made of, by kind, by part and at every answer, with the exact text and a Markdown download), **Book copy**, **Sub-agents** and **Diagnostics** (calls by tool, errors, repeated calls, long arguments, biggest results, slowest rounds, context peak, compactions, notices, sub-agents; Markdown, JSON; also for old runs). Also the state line with the context against its cap, **Reply limit**, **Plan** (the steps, with their state) and **Findings**, the command line of the application (the author's commands: `/compact`, `/clear-workspace`, and the allowed commands that change nothing), Ctrl+Enter to send. **Messages while the agent works** are queued (read by the next request; **Send now** drops the round in the air, a trash button takes one back; commands wait for the end of the turn), **Resume** takes an optional *Note for the agent*. Every answer is stored with what the request reported (`AgentMessage.telemetry`: time to the first word, why it ended, cache and reasoning tokens, calls against calls made, wait for a place).
- **`report`, findings.** `report(text, severity?)` appends a note, warning or problem to the findings of the run (200 at most), kept in `findings.json` of the run folder, listed in the review and in the Markdown file of the run. Not for sub-agents.
- **Limits and permission.** The permission `AGENTS` has: **Most agents** (5), concurrent runs (1), run token budget, **Working copies**, **Largest file**, **Workspace size**, **Files in a workspace**, **Checkpoints of a run**, **Requests to the model at once (per run)**; an administrator needs no granted permission but the settings of their own permission apply. A model has **Concurrent requests of agents** (`AIGate`, a queue per model). The tokens of a run, the context cap (lowest of the agent, the profile and the model) and the reply limit are enforced.
- **A message to a URL** when you are away (Settings > General; ntfy, Gotify or Discord; the administrator can turn it off, Admin > Agents). Only `http(s)`, no redirects, the URL is never logged. It needs the permission `NOTIFICATIONS` (see Permissions) and calls only public addresses unless its sub-permission is given.
- **Resilience.** The messages and commands you queued, the plan, the results of the commands and the call being made are kept in files of the run; stopping the server pauses the working runs first; after the restart a call that had not begun is made again, one that only reads is made again, and only a call that had begun to change something is reported as interrupted. Stopping or pausing at once cancels the command in the air (`CommandContext.isTerminated()`, `FunctionContinuation.onCancel`). A paused approval stays open and comes again.
- **Manual.** The developer guide has the page *Agents*, the user guide the agent pages.

#### Commands

- **Command service and command bar.** `CommandService` (`registerCommands`, `unregisterCommands`, `getCommands`, `execute` / `executePositional` / `executeLine` returning a `CommandRun`): a suite is a stateless object with public `void` methods taking a `CommandContext` and annotated `@Command(commandName, humanName, description, llmDescription, async, dataSeverity, readOnly, availableForAgent, batch)` with `@CommandParameter` / `@CommandReturn` / `@CommandParameterSignature` (overloads, positional arguments); the texts are `L` keys or plain strings for extensions. The context offers `get`, `setReturn`, `setStatus` / `setSubStatus`, `setUninterruptable`, `isTerminated`, `confirm(question)` and `getScope()` (`CommandScope`: the book the command works on, the open book for the user, the working copy for an agent, with `getModel()`). `CommandDataSeverity` (`DESTRUCTIVE` default, `NON_DESTRUCTIVE`) is shown as a label and drives the destructive mode of agents. The story tab has a **command bar** (type `/`; list, help, quoting; results and help are popovers; an asynchronous command shows a window with **Stop**). `SessionPoint` holds the `Workspace` and answers `getManuscriptSection()`, `getManuscriptWindow()`, `getCurrentManuscript()`, `getCurrentAI()` (`CommandUtils` delegates); `ManuscriptWindow` and `ManuscriptSection` are common interfaces of the desktop and phone classes.
- **Read commands** (all `NON_DESTRUCTIVE`, answer JSON, paged, with handles): `/get-book-info`, `/list-parts`, `/get-part`, `/list-swipes`, `/list-branches`, `/get-part-metrics`, `/show-prompt`, `/search-parts`, `/find-occurrences`, `/compare-parts`, `/count-tokens`, `/list-summaries`, `/get-summary`, `/check-summaries`, `/get-template-variables`, `/list-lorebooks`, `/list-lore-entries`, `/get-lore-entry`, `/search-lore`, `/preview-lore-activation` (which entries a text would use and why; the rules moved to `LorebookActivationService`). Parameters declare their range and defaults (`limit` 50, 1 to 200...).
- **Write commands.** Manuscript: `/edit-part`, `/replace-part-text`, `/append-to-part`, `/set-part-fields`, `/create-part`, `/insert-part <text> <part> [before]` (everything that followed the place, all branches, follows the new part, so the other parts keep their uuids, summaries and data; in the review the inserted part is an added part and the others get the field **Follows**), `/branch-part`, `/delete-part`, `/switch-branch`, `/set-summary`, `/remove-summary`, `/revalidate-summaries`, `/set-book-info`, `/set-book-prompts`, `/set-book-tags`, and with the model (asynchronous, one generation of a book at a time) `/next`, `/swipe`, `/regenerate`, `/regenerate-summary`, `/create-meta-summary`. Lorebook: `/create-lore-entry`, `/update-lore-entry`, `/delete-lore-entry`, `/move-lore-entry`, `/set-lore-entry-tags`, `/create-lorebook` (both make the root lorebook for a book that has none), `/set-lorebook`, `/add-subbook`, `/remove-subbook`, `/delete-lorebook`. A lorebook that an agent deleted (it is only marked as deleted, with its entries and tags) or took out of its parent can still be named by its handle, and `/add-subbook` with it brings it back (`LorebookService.restore`, through the trash; `CommandScope.resolveUnusedLorebook`, only a lorebook of the working copy that the run has a handle for). Adding and changing is `NON_DESTRUCTIVE`, removing is `DESTRUCTIVE` and asks. Editing services the screens and the commands share: `ChatMessageService.updateResponse` / `branchAndSwitch` / `swipeAndSwitch` / `createPart` / `insertPart`, `LorebookService.addSubbook` / `removeSubbook` / `setSubbooks` / `createSubbook`, `LorebookEntryService.create` / `update` / `validate` / `deleteEntry` / `move`, `SummaryService.revalidate`, `ManuscriptService.updateSettings`.
- **Other commands.** `/search-replace-parts`, `/search-replace-lore-entries`, `/take-backup`; Lore Generator `/list-lore-templates`, `/generate-lore-entry`, `/compact-lore-entry`; Name generator `/list-name-categories [filter]` (over a thousand categories: the filter is the start of the name, the answer has a category on each line), `/generate-names`; Name provider `/list-real-name-lists`, `/pick-real-names`; Arc Outline `/get-arc-outline`, `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc`, the same for chapters, `/generate-arc`, `/generate-outline-chapters`, `/import-arc-from-story`; Character Tracker `/list-characters`, `/get-character`, `/create-character`, `/update-character`, `/delete-character` and the relations. The plugins' commands take the model from `CommandScope.getModel()`, so in a run they use the model of the agent or the book (the Lore Generator used the default model of the user before).

#### Books, story and lorebooks

- **Lazy loading of the parts.** The setting **Parts loaded at once in the story** (`UserSetting.storyPartsLimit`, empty loads everything) keeps a sliding window of parts in the story (desktop and phone) and the viewer: it opens with the last parts, adds earlier ones above and drops the far ones (`LazyBranch`, `StoryLazyLoader`, `ReaderLazyLoader`). The viewer opens where you stopped reading (kept in the browser only). The outline and the branch search move the window to the part. **`/reset`** forgets the saved workspace state and opens an empty workspace, for a workspace that freezes the browser.
- **Search and replace** in the story (⚙ menu, desktop and phone) and in lorebook entries: the active branch or all branches, summaries optionally, optional regular expression and **Case sensitive**; the window lists the matches with the text before and after, runs on a worker thread with **Stop**, nothing changes until **Replace**; summaries are revalidated (**Revalidate all summaries**, on by default) or the user is asked before they become outdated (`ManuscriptReplaceService`, `LorebookReplaceService`, `TextSearch`, `EntityTextSearch`, `SearchReplaceDialog`). Regular expressions of users and models are stopped after 30 seconds (`TimeSafeText`, `marginalia.regexTimeoutMillis`, `RegexTimeoutException`). A **Regular expressions** page in the user guide.
- **Import from an archive.** Every `.md` file of a zip becomes a part added to the active branch (⚙ menu of the story), a new book (next to **Add Book**) or a lorebook entry (a new lorebook, disabled entries; names from `names.txt`, the first heading or the file name, edited in a table first). Progress bar with **Stop**; not one transaction. `ArchiveImportService`, `MarkdownArchive`.
- **Books: clone** (the clone button of the book list; **Only current branch**; own copies of the parts, summaries and pictures, the same lorebook) via `BackupService.cloneManuscript`; **Tag books**, **Tag lorebooks** and **Tag entries** put or take one tag on many items at once (`MultiTagDialog`, `TagRelationService.getTagsForObjects`); the Lorebook tab of the book window has **Shown lorebook** to look at any subbook without binding it.
- **Lorebooks.** **Mass edit** (all entries of the lorebook and its subbooks in one table, move to another lorebook, `LorebookMassEditDialog`, `LorebookService.getLorebookTree`); **Count with subbooks**; **Only root lorebooks** and a click on a sub-lorebook name opens it; the filtering modes **Keywords (any)** and **Keywords (all)** (`FilteringMode.KEYWORDS_OR`, `KEYWORDS_AND`, first in the enum).
- **Generation options.** `StoryGenerationService.generateNextTurn(…, GenerationOptions)` lets a generation use another AI / protocol than the book's and works without an HTTP request.

#### Permissions

- **Permissions** (`V15`): the entity `Permission` (`PermissionType` `HELPER`, `CREATE_AI`, `AGENTS`; settings as extended attributes) with `PermissionService`, and the key button in Admin > Users that opens `PermissionsDialog` (granted check boxes, a detail row with the settings of each type, `PermissionUIFactory`). Two application switches became permissions: *Users can create their own models* is `CREATE_AI` and the help assistant is `HELPER`, per user (administrators always have both); *Apply limits* and *Allow navigating the interface* of the help assistant moved into `HELPER`. The migration (app migration 4 → 5, `PermissionsMigration`) grants them to existing users when the switch was on. `AIService.isUserCreationAllowed` / `setUserCreationAllowed` and the first component of `HelpAssistantService.Settings` are removed, the fields of `AppSettings` stay deprecated.
- **Notifications permission** (`PermissionType.NOTIFICATIONS`, no migration: a permission is a row, the setting an extended attribute). The server sends the message to the webhook of a user only when the user is an administrator or has the permission (the settings show the fields only to them, not only to users of agents). Its sub-permission **Allow addresses on this machine and in private networks** (`Permission.notificationsAllowLocal`, in the dialog only while the permission is granted) decides where the message may go: without it the host must resolve to public addresses only, so `localhost`, private and link-local networks (the metadata service of a cloud host) and similar are refused, for the delivery, the test message and the saving of the address (`AgentWebhookService.check(user, url)`, `mayNotify`, `mayUseLocalAddresses`; `AgentWebhookServiceImpl.isLocal`). Administrators need neither. This closes the finding that every user of agents could make the server call any address and read its status in the test message. `PermissionUIComponent.onGrantedChanged` is new, `NotificationsPermissionComponent` is `@Extendable`. DNS rebinding is not covered (the name is resolved again by the HTTP client).

#### Plugins and extensions

- **`@Extendable` and `@PublicAPI`.** Sixty more UI classes can be decorated (the agent screens, the search and replace and permission dialogs, the generic dialogs, `CommandBar`, `LorebookMassEditDialog`, `MultiTagDialog`, the archive import, `Reset`...), and every instrumented method carries `@PublicAPI(from = "1.4.0", …)` checked by `PublicAPISignatureTest`. New methods of `DesktopWorkspace`, `PhoneWorkspace`, `ManuscriptPart`, `PhoneManuscriptPart`, `ManuscriptStoryPart` and the lorebook views (menu items and dialogs of the new features) are plugin visible. The Google Gen AI plugin implements `InferenceService.round` for agents and keeps the thought signature of a call (`LLMToolCall.signature`; not checked against the live API with a long run).
- **API for extensions.** `InferenceService.round(payload, protocol, functions, callback)` (one request of an agent; `InferenceRoundCallback`, `RoundUsage` with `RoundStats`), `CommandService` and the annotations above, `CommandScope`, `FunctionDefinition.dynamic`, `FunctionContinuation.onCancel`, `Localization.getValue(Language, L)`, `ManuscriptService.createAgenticCopy` returning `AgenticCopy`, `AgentGrant`, `MergePlan.Type.JSON`, `ThreadCopyRequestAttributes.create()` returning `RequestAttributes`, `GenerationController` / `GenerationControllerEvent.getAi()` and `getProtocol()` (the model the generation really uses; the pipeline no longer reloads them into the book), `DialogFocus.onOpen`, `UIUtils.isPhone()`.
- **`ToolService`: tools for the model from any extension** (API for plugins, `domain.service.tool`). An extension registers a `ToolProvider` under its bundle, a key and a human name (`registerProvider`): `items()` (what the user can check, none = on or off), `functions(items, request)` (the `FunctionSet` limited to the checked items, done by the provider with its own types), `defaultHint()`, `description()` and `commands()`, a `@Command` suite that the service registers with `CommandService` and removes with the provider, so one registration gives the functions to the dialogs with tools and the commands to the command bar and the agents. `getProviders()` lists them, `resolve(ToolSelection, ToolRequest)` gives the `ToolSet` (functions put together, the hints with the user's replacing the default one; a gone provider or item is ignored, a failing provider is logged and left out, two providers with the same function name fail naming both). `OsgiServiceImpl` removes the providers of an unloaded bundle. No migration. Replaces finding the name extensions by reflection.
- **The common tools dialog.** `ToolSelectionButton` (`ui.widgets`; shows `Tools (n)`, hides itself while no suite is installed) opens `ToolSelectionDialog` (`ui.dialogs`): a section for each suite under its human name, each with a filter field of its own (checked items stay in the table; *Check shown* / *Uncheck shown*), a check box for a suite without items, and an editor for the hint of the suite. Keys `LABEL_TOOLS*`, `MSG_TOOLS_*` in the English, Japanese and Chinese bundles. Doc: *Tools for the model* in the user manual.
- New plugin **Place provider** (`marginalia/plugins/place-provider`): the model, agents and the command line look up real places instead of inventing them. Functions `list_place_types`, `search_places` (name, type and country; `inlets` of `Canada`), `places_near` and `distance_between`, and the commands `/list-place-types`, `/search-places`, `/places-near`, `/distance-between` (all non-destructive). The data is Natural Earth 1:10m (public domain), about 18 000 notable places reduced at build time into `places.json` (committed, 2.3 MB): the steps run only with `mvn package -Pimport-places` (like `train-names` of the Name generator), a normal build does not touch the file. Registered as a tool suite (on or off) with the commands, so the Lore Generator and the Arc Outline offer it to the model.

#### Other

- **Version footer.** The version and short commit id (`Marginalia 1.4.0 (93ec118)`) in the bottom left corner of the workspace and under the login form (`dev` outside the packaged WAR); the published manual shows them too (`tools/retype_build.sh`).
- **Dialogs focus their first field** when they open (also in the arc-outline, character-tracker, lore-generator, reviewer and sidequery plugins).
- **Localization.** About 800 new keys in the English, Japanese and Chinese bundles.

### Changed

- **Opening a long story is faster.** The part cards build their *Turn Details* popover and the editing text area only when first needed, and the menu of a card is a `LazyContextMenu` filled when it opens. Plugin visible: `createMenuItems()` of the story cards no longer creates the `ContextMenu` (new `createHamburgerMenu()`, `prepareMenu()`; a decorator's items appear once the user opens the menu; `refreshSummaryMenuItems` can run before `createMenuItems`); the phone `createInfoPopover` content moved to `createDetails` and the desktop card got one.
- **The story always opens at the bottom.** The saved scroll position (`ChatMessage.scrollPosition`, now ignored) and its scripts (`StorySupport.trackScrollPosition`, `setupScrollListener`, `restoreScrollPosition`, `JavascriptLibrary.restoreScrollPosition` / `fireDebouncedScrollEvent` / `getScrollTop`) are gone; deleting a part redraws the story at the bottom. `renderStoryContent`, `Viewer.renderManuscriptReadingView` and `StoryGenerationListener` changed (rebuild plugins that read them).
- **Backups, exports and copies of lorebooks carry the `attributes`** of lorebooks and entries (`LorebookService.marshalLorebooks`); before, the data of extensions was lost.
- **SQLite:** read-write transactions begin with the write lock (`transaction_mode=IMMEDIATE`, `ReadWriteRoutingDataSource`), so writing threads wait for each other instead of failing with `database is locked`; schema validation runs per table (`hibernate.hbm2ddl.jdbc_metadata_extraction_strategy=individually`) because the driver can not read more than 500 columns at once.
- **Docker: the heap is 1 GB (`-Xmx1g`)**; 512 MB was no longer enough (`OutOfMemoryError` when the Name generator loaded its tables). Change an existing `docker-compose.yml` by hand.
- **Name generator:** building the plugin no longer retrains `name-tables.json`; use `mvn package -Ptrain-names`.
- **Lore Generator and Arc Outline: the *Name tools* button is the common *Tools* button.** It offers every installed suite (Name generator, Name provider, Place provider), not only the name extensions; the model gets the suites that have something checked. The hints are edited in the tool dialog for each suite, the two hint fields (*Name generator hint*, *Name provider hint*) are gone from the extension settings. The categories checked and the hints written before are read once into the new choice (`LegacyNameTools`) and saved in the new form (`tools` in the settings and in the options kept with a generated lore entry). `NameToolsBridge`, `NameToolsDialog` and the `Defaults.NAME_*_PROMPT` constants of both plugins are removed (the default hints are in the suites). Plugin visible: the services' `nameCategories()` / `nameFunctions(...)` are `tools(ToolSelection)`; `buildGenerationPrompt` / `buildPrompt` / `buildChapterPrompt` take a `ToolSet`; the protected `GeneratorDialog.supportsNameTools()` is `supportsTools()` and `buildPrompt` takes a `ToolSet`.
- **Name generator and Name provider** register their suites with `ToolService` (key `names`; items = the categories) and no longer register their commands themselves. Their extension classes lose `categories()`, `newFunctions()` and `functions()`.

### Fixed

- **A regular user got a blank page or an *Application Error* after the login:** the desktop workspace built the admin part (whose panels ask for backups and refuse anybody but an administrator) for every user. It is now created only for administrators (`DesktopWorkspace`; the `user` local of `create()` moved up in the `@PublicAPI`).
- **Text areas on a phone** (`ResizableTextArea`) are as high as set again: the helper text under a field, long on a narrow screen, took most of the height, so a long prompt showed one or two lines.
- **A story of hundreds of parts froze the browser when it opened** (see the lazy parts and the lazy popovers above).
- **A book created with Add Book** is remembered by the workspace autosave like a book opened from the list.
- **Lorebook VCS:** switching, adding or deleting a revision of an entry no longer closes the opened entries.
- **Login with *Save login* ticked failed for a login with a space** (also a comma, a quote or a non-ASCII letter): the login was written into the cookie as it is and Tomcat refused the cookie (`An invalid character [32] was present in the Cookie value`). The login is URL-encoded in the cookie now (`LoginCheckRoute`); a cookie saved before is read as before.

### Known issues

- New users have no permission (`CREATE_AI`, `HELPER`, `AGENTS`, `NOTIFICATIONS`), an administrator gives them in the permissions of the user.

## [1.3.0] - 2026-10-16

One database migration (`V14`): a copy of the database is saved before it runs, and 1.2.0 cannot open a migrated database.

### Added

- **Help assistant.** A round robot button in the bottom right corner (desktop and phone) opens a chat that answers questions
  about Marginalia from its manual: the model searches and reads the manual pages with two functions and names the pages it used. The
  panel says which AI answers and warns when the user's limit applies; the conversation is kept in the session
  (`SessionPoint.getHelpChat()`). The model can also read what the user is doing (the open section, tab or dialog and its
  title, never story text) with a function `get_ui_state(path, deep)`; by default it gets only what is on the screen, `deep` also
  expands the tabs that are not open. There are two default prompts, for administrators and for other users, editable (with **Reset to
  default**) in the new tab. With **Allow navigating the interface** (new setting `helpInteractions`, off by default) the model can also
  move through the UI for the user with a function `interact_with_ui(path, interactions)`: open a section, select a tab, nothing that
  changes anything. UI elements offer such moves by overriding the new `LLMInteract.getInteraction()` (a map of keys to `LLMInteraction`);
  `applyInteraction(key)` / `applyInteraction(List)` run them in order and return the new snapshot; `LLMSnapshot` gained `interactions` and
  `element`; `UiStateSource` gained `interact` and a `withInteractions` flag on `read`; new `InteractiveHelpTools`; implemented by both workspaces,
  `AdminPart` and both story editor dialogs (these overrides carry `@PublicAPI`; `DesktopWorkspace` got a private `openAdmin()`).
  Interactions also select a tab of the Settings (`UserPart`, so the phone settings too) and press the button that opens the dialog
  to add a book, a model or a protocol (`open-dialog:<button text>`, `LLMInteract.dialogButtons`; not while a dialog is open): those
  lists and the settings now describe themselves (`getLLMState`, `getInteraction` with `@PublicAPI`; `WorkspaceLLMSupport.describeTable`).
  **When something goes wrong** the model can read it: a function `read_last_errors(count)` returns the last errors, failed requests
  to a provider (`InferenceException`s, also a generation's), warnings and validation messages the user was shown (every `ui.widgets.Notification`
  error and warning, `UIUtils.showError` / `internalServerError` / `inferenceError` / `showValidationErrors`), newest first. They are
  kept in the new `ProblemLog` of the session (`SessionPoint.getProblems()`, the last 20, written by the new `utils.UiProblems`), the
  unexpected ones also in the `sessionStorage` of the browser tab (the last three, they survive a reload); keys, tokens and passwords are
  removed from the text and it is cut at 400 characters. New `GenerationListener.onInferenceError` (a default method calling
  `onSimpleError`; `StoryGenerationListener` overrides it, with `@PublicAPI`). **Forms:** `get_ui_state` lists under *Problems with the form*
  the fields that are invalid (with the message they show) or required and empty, never their values (new
  `LLMInteract.getLLMProblems()`, default `LLMFormProblems.scan`; `AIDialog` adds its cross field rules, with `@PublicAPI`;
  `LLMSnapshot` gained `problems`; `UiStateSource` gained `readProblems`). **Manual links:** the manual tools print the address of the
  page on the published manual (`https://enerccio.github.io/Marginalia/user/...`, `ManualPage.url()`), the assistant cites pages as links
  and they open in a new tab. **The assistant is usable above a dialog** (a book is one; before, the dialog blocked the button and the
  panel): in the top layer it moves to the bottom left while a dialog is open. **Administration data:** with **Allow reading
  administration data** (new setting `helpAdminData`, off by default) an administrator's assistant gets `get_admin_data(topic)`
  (extensions with their state, user counts, database backups, what a cleanup would remove, the application) and
  `read_extension_log(extension, lines)` (new `AdminHelpTools`, `AdminDataSource`; never for other users, read only). A new **Help assistant** tab on the Admin page lets an administrator turn the
  assistant on or off and choose one of their own models for
  it; **Apply limits** (shared models only) makes the questions count against each user's limit and usage like any other
  request to the model, otherwise (or for a model that is not shared) the owner of the model pays and nothing is counted for the
  user. The user manual (`manual/user`, Markdown, no pictures) is now packed into the WAR
  as `classpath:/manual/` and searched in memory by the new `ManualService` (`ManualPage`, `ManualSection`, `ManualHit`);
  new `HelpAssistantService` (`ask`, `getInfo`), new `ManualTools`, `HelpTools`, `UiStateSource`, `HelpPrompts`, new `ui.LLMInteract` and `ui.LLMSnapshot` (implemented by `Workspace`,
  `WorkspaceComponent`, `ManuscriptDialogPart`, `PhoneManuscriptDialogPart`, every dialog, `Main` and several components; extensions can
  implement it on their own components, see the developer manual, *UI*; the overrides in the extendable classes carry `@PublicAPI`), new component `ui.components.HelpAssistantWidget` (added to `Main`), new application settings `helpEnabled`, `helpAiUuid`, `helpApplyLimits`, `helpInteractions`, `helpAdminData`, `helpPromptAdmin`, `helpPromptUser`
  (extended attributes, no migration), new texts in all language bundles (the Chinese localization plugin too).
  **For plugins:** new `UnmeteredScope`; an inference service (an extension AI) must skip `AILimitService.check` and
  `UsageService.record` for a request started while `UnmeteredScope.isActive()`, see the developer manual,
  *AI providers*. The `google-genai` plugin does it. Both Dockerfiles now copy `manual/user` into the build.
- **Red warning about invalid extensions.** A red bar at the top of the workspace (desktop; below the title bar on phones) shows when an
  installed extension is invalid, so it is not found out only on the Admin page or by a missing function. An administrator sees
  which extensions on hover, other users are told to tell the administrator. Extensions the administrator stopped are not counted. New
  `OsgiService.getInvalidExtensions()`, new component `ui.components.InvalidExtensionsWarning`, new texts in all language
  bundles (the Chinese localization plugin too).
- **Lorebook preview in the desktop story editor.** A new read only *Lorebook* tab in the left sidebar (next to the
  Outline, a `TabDock` tab with the id `lorebook`, so it can be detached too) shows the entries of the book's lorebook in a
  tree grid; an entry opens to its content (rendered as Markdown) and the text or pattern that activates it. *Include sub lorebooks* lists the
  lorebook and all sub lorebooks below it as top level rows with their entries under them, without it only the entries are
  listed. It is read again whenever the Story tab is entered; nothing is saved from it. New component
  `ui.components.LorebookPreview`, new `ManuscriptStoryPart.createLorebookTab` and `LOREBOOK_TAB` (plugins must not give a
  tab of theirs the ids `chapters` or `lorebook`). New text in all language bundles (the Chinese localization plugin too).
- **Detachable sidebar tabs in the desktop story editor.** The tabs of the left sidebar (Outline and the tabs of extensions)
  can be dragged out into panels of their own, in the same column under each other or in a column of their own next to the
  others, and dragged back onto another panel (on a tab to put it before that tab, on the body to put it last). While a tab
  is dragged, dashed strips between the panels and tall strips between the columns take a drop to detach it. The panels and
  the columns are separated by splitters (`SplitLayout`) the user can drag, their sizes are saved in the layout too. The arrangement is saved with the book, in the attributes of the manuscript under
  `$desktopManuscriptStoryPartTabsConfig` as `{"columns":[{"size":..,"groups":[{"size":..,"tabs":[...]}]}]}` (no migration; it travels with backups), and a tab of an extension that is not loaded
  keeps its place. The selected tab of every panel is part of the workspace autosave (`Restorable`, key `tabs` of the story
  part). The position of the divider between the sidebar and the story is saved with the book too
  (attribute `$desktopManuscriptStoryPartSplitPosition`, new `ManuscriptStoryPart` methods `applySplitPosition` and `saveSplitPosition`).
  New reusable widget `ui.widgets.TabDock`. API changes plugins can see (since 1.3.0): the field `leftBar` of
  `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` is a `TabDock` instead of a `VTabSheet` (`add(id, caption, content)`,
  `add(caption, content)`, `remove(content)`; extensions that read it as `VTabSheet` must be updated (the extension verification refuses them) - Arc Outline, Character
  Tracker, Side Query and Author's Note were, and give their tabs the ids `arc-outline`, `character-tracker`, `sidequery` and `authors-note`); new
  `ManuscriptStoryPart` methods `applyTabsConfig` and `saveTabsConfig`, `restoreState` has the new local `tabs`. New text in
  all language bundles (the Chinese localization plugin too).
- **Inference providers from extensions.** An extension registers an `ExtensionAIFactory`
  (`InferenceServices.registerExtensionFactory` / `unregisterExtensionFactory`, `getExtensionFactory(id)`,
  `getExtensionFactories()`) that creates the `InferenceService` and the settings form (`ExtensionAIForm`) of its AIs. New
  AI type `AIType.EXTENSION` and entity `ExtensionAI` (`ais_extension`, one column `extensionId`, **migration V14**); the
  settings of the provider are in `ExtensionAI.getSettings()` (the dynamic attributes of the AI), secrets are encrypted with
  the new `InferenceServices.protectSecret` / `revealSecret`. In the AI dialog the type *Extension* (offered while some
  extension provides AIs) shows a *Provider* selector, the form of the provider and *Test Connection*; the type of an existing
  AI can no longer be changed. While the extension is not loaded `InferenceServices.forAI` returns `null`, the new
  `forAIOrThrow` throws `InferenceException` of the new type `EXTENSION_UNAVAILABLE` (the generation steps and summaries use it
  and the user is told to load the extension), the AI list shows the provider name (`AITypes.label`) and the dialog opens such
  an AI with a notice and keeps the settings of the extension as they are. API changes plugins can see (since 1.3.0): the
  `AIDialog` methods `createFields`, `updateDynamicFields`, `save` and `populateFields` have new locals, new `AIDialog` methods
  `addExtensionSettings`, `isExtensionMissing`, `extensionAiFor`, `currentExtensionAI`, `testExtensionConnection`. New texts
  in all language bundles (the Chinese localization plugin too).
- New plugin **Google Gemini** (`marginalia/plugins/google-genai`): Google's Gemini models as an inference provider over the
  Google Gen AI Java SDK, which is embedded in the plugin JAR only (about 14 MB, nothing is added to the application). API key
  (encrypted), model list, optional proxy URL, timeout, retries, safety filters off, the thinking summaries as reasoning
  (thinking level for Gemini 3, token budget for the others), streaming with prompt cancellation, JSON schema, function calls
  (with the thought signatures Gemini 3 needs), attachments, usage and limits like the OpenAI-compatible provider.
- New plugin **Arc Outline** (`marginalia/plugins/arc-outline`): an *Arcs* tab in the story sidebar with the arcs of the
  book and their chapters. Arcs and chapters have a title, description, note and a state (planned, current, finished),
  are reordered with buttons (a chapter moved past the end of its arc goes into the neighbouring arc) and finishing an
  arc finishes its chapters. The wand opens a dialog that asks the model for a new arc (a name, a terse description and the details in the note) from
  a prompt template (`{{backgroundLore}}`, `{{existingArcs}}`, `{{specification}}`, editable in *Settings → Extension
  Settings → Arc Outline*), with the reasoning and the arc shown while they stream in; the model can get names from the
  Name Generator like in the Lore Generator. The wand of an arc does the same for its chapters (prompt variables
  `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}`, `{{chapterSpecification}}`),
  with a choice of how much of the story the model sees, the number of chapters in words or as an exact number (with
  a field for each chapter) and the new chapters shown under the arc as they stream in. Both ask for a terse description and put the details into
  the note. Generated arcs and chapters
  keep the reasoning and the answer of the model (`generationReasoning`, `generationResult`), shown in a popover.
  Stored in the book's attributes, no migration.
- Arc Outline: the wand next to *Add new arc* is a menu with *Generate an arc* and the new *Import from story*, which
  asks the model to summarize a range of the messages of the active branch of the book as an arc with chapters
  (optional exact number of chapters, state of the imported nodes, a counter with the approximate tokens of the range
  and of the whole prompt next to what the model takes). New prompt `Defaults.IMPORT_PROMPT` with the variables
  `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}`, `{{numberOfChapters}}`, editable in *Settings → Extension
  Settings → Arc Outline* (*Story import prompt*). Chapters with a note have a chevron that shows the note under the
  description in the list, without the dialog.
- `ModelProtocolPicker` (`ui.widgets`): a model and a protocol to choose from, for dialogs that run the model with a
  choice of their own. The Lore Generator dialog has one (it starts with the default model of the user settings), so
  has the Arc Outline generator (it starts with the model and protocol of the book).
- `IPCService`: extensions (and the application) offer text about a book or a story part to each other without
  importing each other: `registerManuscriptProvider` / `registerChatMessageProvider(bundleIdentifier, providerKey,
  providerName, callback)` with `ManuscriptPluginData` / `ChatMessagePluginData.getDataFor(entity, callerBundle,
  callerIdentifier)`, `getManuscriptProviders` / `getChatMessageProviders` (bundle → key → name, in registration order)
  and `getManuscriptData` / `getChatMessageData`. The providers of a bundle are removed when its extension unloads.
- Side Query: a plug button above a tab lists the sources of data other extensions offer; checked sources about the book
  are sent with the lore, sources about story parts are added after each included part. The Arc Outline offers its
  outline as two sources: the terse one (descriptions) and the full one (with the notes).
- `PartialJson.parse` (`domain.service.structured`): reads the unfinished JSON of a streamed answer (a markdown block
  around it is ignored) into a Jackson tree, to show what has arrived so far. New dependency
  `io.github.funnysaltyfish:partial-json-parser-jvm` (brings `kotlin-stdlib`).

- Workspace autosave: Marginalia remembers where the user was and restores it after the login - the selected tab, the
  filters of the book list, the lorebook and the open entries of the lorebook tab, the open book with its tab (and
  the open entries of its lorebook tab) and the new turn form of the story tab (scene, POV character, present
  characters, instructions). Both the desktop and the phone layout. Stored in the new `UserSetting.workspaceState`
  extended attribute (no migration). What was deleted meanwhile is skipped, a deleted book is not opened.
- `Restorable` (`ui`): `storeState()` / `restoreState(JsonObject)` for the UI classes that have state worth
  remembering, with the helpers in `RestorableState`. API changes plugins can see (since 1.3.0): `Workspace`
  extends `Restorable` and has `autosave()` and `restoreAutosaved()`; `WorkspaceComponent`, `ManuscriptDialogPart` and
  `PhoneManuscriptDialogPart` extend `Restorable` with default methods that store nothing, so existing parts compile
  unchanged; `ManuscriptDialog`/`PhoneManuscriptDialog` have `setOnStateChange` and `stateChanged()` (a plugin that
  adds a tab calls it), the lorebook views `setOnStateChange`, `NewTurnForm` `setOnChange`.
- New plugin **Name provider** (`marginalia/plugins/name-provider`): the model gets real names instead of generated ones.
  `list_real_name_lists` lists about 180 lists, `<Country> - Surnames` (75 countries) and `<Country> - Forenames` (106), and
  `pick_real_names` picks up to 100 of a list at random as pairs `{romanized, original}` (`Satō` / `佐藤`). The data is the
  popular names by country dataset (CC0), imported at build time into `name-lists.json` (committed; without network the
  committed file is used), the country codes are expanded to the English names of the countries.
- Name generator: 75 new categories `<Country> - Surnames` trained on the romanized surnames of the same dataset (new build
  step `import-popular-names`, `src/build/ImportPopularNames.java`), the tables and `chains.json` are extended, the
  existing ones are unchanged.

### Changed

- Character Tracker: the *Add relationship* button is above the fields of a new relationship, and closing the character dialog
  (Cancel, Esc) with unsaved changes asks for confirmation (Yes/No).
- The opened details of lorebook entries stay open when the entries are reloaded (refresh button, switching to the tab).
- Lore Generator and Arc Outline: the *Name tools* table lists the categories of the Name generator and of the Name provider
  together, with the prefix of the source (`Generator: `, `Provider: `), and the model gets the tools of the extensions that have a
  checked category: both, one or none. Each kind has its own hint in the settings (*Name generator hint*, the former *Name
  tools hint*, and the new *Name provider hint*, which tells the model to pick a name from the returned ones and keep it as
  it is). Categories saved before count as Generator ones. `NameGeneratorBridge` is now `NameToolsBridge`; the service
  methods `buildGenerationPrompt`, `buildPrompt` and `buildChapterPrompt` take the `FunctionSet` instead of a flag.

### Fixed

- Reviewer: the review dialog no longer saves an older copy of the part (reverting an edit or unlinking a summary made after the book was opened); it reloads the part before it reads or writes the reviews.
- Side Query: the sidebar tab no longer shows a second scrollbar around the message list (only the messages scroll), and the options bar wraps in a narrow sidebar.
- Saving the settings no longer writes back a copy of the user setting loaded when the tab was opened, which could undo
  a language chosen in the meantime.

### Known issues

## [1.2.0] - 2026-10-14

Four database migrations (`V10` to `V13`): a copy of the database is saved before they run, and 1.1.0 cannot open
a migrated database.

### Added

- `InferenceService.infer`: asks the model for the whole answer at once (no streaming, still asynchronous). With a JSON
  schema the provider is asked to follow it and the callback learns whether the answer is valid (`SchemaCheck`); with a
  Java class the answer is read into an object. The schema of a class is built from its fields and can be refined
  with the new annotations `@LLMSchema` and `@LLMField` (`StructuredOutputs`). The model can call functions during `infer` too. `InferenceService.stream` can pass a response schema to the provider (not validated). New dependency `json-schema-validator`.
- Settings → **Tags**: a table of all your tags with the number of objects related to each. A tag can be renamed or
  deleted (its relations are deleted with it). Renaming a tag to the name of another tag asks whether to merge them:
  yes moves the relations to the existing tag and deletes the renamed one, no leaves the tag unchanged
  (`TagService.findUsageForUser/findRenameConflict/rename/merge`, `TagRelationService.moveRelations/countRelationsByTag`;
  `TextInputDialog.Builder.initialValue`).
- Admin → Extensions: a **#** button next to the version of each extension shows the SHA-256 hash of its JAR on hover
  and copies it to the clipboard on click (`OsgiService.getSha256`).
- Shared inference providers: an administrator can check **Shared with all users** in a provider's dialog. Every user
  can then use it in their books, but not see its address or API key, change it or delete it. The rules are enforced in
  `AIService`, not only hidden in the UI (migration `V10`). A setting **Users can create their own models** (above the
  user list in the administration) lets administrators restrict users to the shared providers.
- Token usage per provider: the Inference Providers tab shows the tokens sent and generated with each provider, per user
  (a shared provider counts separately for each user); administrators get a **Usage per user** table with the tokens,
  requests and time of the last request of every user and provider (migration `V11`). The numbers are what the service
  reports (Marginalia asks for `stream_options.include_usage`) or a local estimate, and, unless an administrator limits a user (see below), nothing is limited by them.
- Limits of shared providers: the administrator who owns a shared provider sets, per user, a limit - unlimited, a token
  limit (tokens sent, generated and in total, each optional, reset daily, weekly or monthly) or not available - in the
  new **Limits** dialog of the Inference Providers tab. A request of a user who is over the limit or has the provider
  not available fails before anything is sent (a prompt that would go over the sent or total limit is refused, only the generated tokens can go over, unless the administrator makes that limit **hard**: then a request whose reserved
  response (the maximum response size) would not fit is refused too;
  `InferenceException` types `NOT_AVAILABLE` and `LIMIT_REACHED`, so
  generations, summaries and extensions all stop). Users see their own limit and what they used with the **info**
  button of a shared provider. The limits are stored in the provider (extended attribute `AI.limits`, a JSON object
  keyed by the uuid of the user, `AILimits`); the tokens used in the current period are two new columns of the `Usage`
  (migration `V12`), reset lazily when the user's last use is from an earlier period. New `AILimitService`; plugins
  that decorate `PhoneAIPart.createActions` see the new `infoButton` and `limitsButton`.
- LLM function calling: the model can call Java methods of an interface marked `@LLMFunction`
  (`FunctionRegistry`, `FunctionSet`, `FunctionContinuation`; asynchronous, JSON schema generated from the parameter
  types, up to 10 rounds per request with the last one forced to answer). `OpenAICompatibleInferenceService` runs the
  calls; extensions use it through `InferenceService.stream(payload, protocol, functions, callback)`.
- `InferenceServices.addToolRoleResolver` / `removeToolRoleResolver`: an extension can change the role the results of
  function calls are sent with.
- New widget `GraphView` (`ui.widgets`, `@PublicAPI` from 1.2.0): a graph drawn with Cytoscape.js (npm package
  `cytoscape`, `graph-connector.js`, `JavascriptLibrary.renderGraph`) - nodes with colors and images, labelled edges
  with arrows, nodes arranged in rings around a center node, re-centered by a double click. Plugins can use it.
- Plugin: **Character Tracker** — a *Characters* tab in the story sidebar: the characters of the book (name, tagline,
  note, icon scaled to at most 200 x 200 pixels, color) with relationships between them (directed or unidirectional,
  with a name and a tagline), a relationship map (double-click a character to arrange the map around it), and an import of lorebook entries (of the book's
  lorebook and its sub lorebooks) as characters. Stored in the attributes of the book. No
  changes to the application.
- Plugin: **Gemma 4 Tools Fix** — sends the results of function calls to Gemma 4 models with the role
  `tool_responses` instead of `tool`.
- Plugin: **Name generator** — the model can call `list_name_categories` and `generate_names` to get names from Markov
  chains instead of inventing them. About 1200 categories, trained at build time from the Onomastikon, fantasygen,
  Corpora and markov-namegen-lib (the build clones them, only the probabilities are committed). The length of the chain
  and the usual lengths of the names are set per category in `src/build/chains.json`, written by the tuning step
  (`mvn exec:exec@tune-chains`).
- Lore Generator: **Name tools** gives the model the functions of the Name generator for the categories you check, in a
  lazy table with a filter (the checked categories stay in it). The checked categories are remembered per entry with
  the other options of the generation. The default hint lets the model modify or remix the names it gets.
- Highlight of generated text: the settings menu (⚙) of the story has the checkable **Highlight Generated Text**. While
  it is on, the text of every part that is still the one the LLM wrote is highlighted, what the author added or
  rewrote is not (a word-by-word diff of the part against its new extended attribute `ChatMessage.originalResponse`,
  which generation fills and editing leaves alone). The switch is not saved. Existing parts get their current text as
  the original by the app migration 3 → 4 (`OriginalResponseMigration`, deleted parts too; no SQL migration), so what
  was edited before cannot be told apart. Only the desktop story tab has it. Plugins: `ChatMessage.originalResponse`,
  new `@Extendable` methods `ManuscriptStoryPart.createHighlightGeneratedMenuItem`, `setShowGeneratedDiff` and
  `ChatMessageCard.renderResponse` (`@PublicAPI` from 1.2.0).
- Reviewer plugin: a book can have its own reviewer profile. The info tab of the book (desktop and phone) has a
  **Reviewer Profile** combo box under the protocol; the chosen profile takes priority over the one selected in the
  settings (cleared: the selected one is used). The choice is stored in the book's attributes by the uuid of the profile,
  which profiles now have (profiles saved before get one the first time they are loaded), so renaming a profile keeps it.
  **Advanced Options** has a **Prompt from Profile** combo box that fills the prompt override with the review prompt of
  the chosen profile (only the prompt, not its model, protocol or pre-prompt).

### Changed

- A protocol can lower the context and response limits of a provider, but no longer raise them above the provider's.
- The story editors share their code: `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` use the new classes of
  `ui.dialogs.story` (`StoryCards`, `StorySupport`, `StoryBackup`, `StoryGenerationListener`, `NewTurnForm`,
  `TurnDetailsForm`, `MessageMetrics`) and are about 300 lines shorter each. Plugins that decorate methods of these two
  classes should be checked against the new code.
- The phone workspace classes and more of the desktop UI classes are annotated with `@PublicAPI(from = "1.1.0")`
  and `PublicAPISignatureTest` checks the annotations against the compiled classes; the phone twins of the book tabs
  are listed as extension points in the developer manual.
- Vaadin is built with `optimizeBundle=false`.
- Lore Generator: the dialog is split into smaller classes (`NameToolsDialog`, `SavePromptAsDialog`,
  `AttachmentPanel`, `GenerationPane`).
- Deleting takes what belongs to the deleted object with it: a book its story parts, their summaries and its tags, a
  story part its summary and the parts below it, a lorebook its entries and their tags, an inference provider its usage
  numbers. The Trash restores them together with the object, and the cleanup keeps the contents
  of a deleted object it can't remove yet, so a restorable lorebook doesn't lose its entries. Services: `delete(x,
  false)` of `ManuscriptService`, `ChatMessageService`, `LorebookService`, `LorebookEntryService` and `AIService`
  cascades; new `TagRelationService.deleteForObject`, `ChatMessageService.softDeleteForManuscript`,
  `SummaryService.softDeleteForManuscript` / `softDeleteForMessages`. The messages and summaries of a book are soft
  deleted with one bulk update, so extensions get no save event for them.
- Trash: **Delete Permanently** removes deleted story parts and lorebook entries (with their summaries and tags) from
  the database right away, for users and administrators; other objects still wait for the cleanup, so nobody removes a
  whole book by accident. The parts below a removed part move to its parent. New `TrashService.deletePermanently` /
  `getPermanentlyDeletableTypes`; `TrashGrid` and `PhoneTrashGrid` get `createDeleteButton`, `deleteSelected` and the
  field `deleteButton` (`@PublicAPI` from 1.2.0).
- `@ExtendedAttribute(inject = true)` now works for any Java type, not only `JsonObject`: other types are converted
  to and from JSON with Gson (objects are stored flat as `<injectPrefix>_<key>`, lists and scalars under `injectPrefix`).

### Fixed

- Inference providers: the API key typed into a new provider is no longer lost when you visit *General Settings* before
  **OK** (the dialog read the enabled state of a field in a hidden tab). **Refresh** keeps the selected model. An empty
  API key works for local servers: a placeholder (`none`) is sent instead of failing in the client library.
- Regular users no longer get an *Application Error* dialog after every login (the admin part is refreshed only for
  administrators).
- Admin → Cleanup: purging an AI that has usage rows no longer fails (`usages.ai_id` is nullable, migration `V13`).
- Lore Generator: the Name generator is found by reflection and not imported, so it works when it is installed after the
  Lore Generator, and again after it was unloaded and installed again.
- The error dialog showed its message as HTML, so markup in an error message (provider replies, user input echoed in an
  exception) was rendered. It is shown as plain text now, line breaks kept.

## [1.1.0] - 2026-10-12

No database migration: a 1.0.0 database opens unchanged.

### Added

- Phone layout below 768 px: a phone workspace with a drawer, a phone book window, a phone story editor and phone
  versions of the book, lorebook, provider, protocol, settings and trash pages. The layout is chosen at page load.
  Not available on the phone: administration pages, the full lorebook editor and *Resources* (see the user manual).
- Per-user UI language: a language selector in the footer (only installed languages are listed), stored in the user
  settings and applied to all open sessions of the user. Japanese is built in.
- Extensions can add languages (`ExtensionHasLocalization`). The new Chinese Localization plugin is the worked example.
- Token breakdown: the tokens of every message of the final prompt are recorded with each generated part (parts from
  before have none) and shown in new Token Usage views with charts.
- Message attachments (images, audio, other files) can be passed to the model by the inference services. Used by the
  Lore Generator plugin; whether the model uses them depends on the model.
- Extension management: an extension can be stopped and started again without uninstalling it (`<jar>.STOPPED` flag
  file), and each extension has its own log file, rotated at 2 MB, shown in Admin → Extensions → Log. Plugins create
  their logger with `OsgiService.createLogger(bundle)`.
- Extensions can ship their own CSS (`ExtensionHasCss`), removed again when the extension is unloaded.
- Plugin: **Lore Generator** — generates the content of a lore entry from the existing lore, with saved prompts,
  attachments and a compaction step.
- Plugin: **Instruction Exporter** — copies the scene setting, POV character and present characters of the active
  branch to the clipboard.
- Plugin: **Chinese Localization** — Chinese UI texts as an extension.
- Root `pom.xml` as the parent of the application and all plugins: the version is defined in one place and
  `mvn package` in the root builds everything.
- `plugins/Dockerfile`, `plugins/docker-compose.yml` and `rebuild.sh` / `rebuild.bat` to build the plugins in a
  container and restart the stack with them.
- Documentation: user pages for the phone layout and the new plugins; developer pages for the new extension
  interfaces, token breakdown, message attachments and the parent POM.

### Changed

- Only the newest version of an extension is loaded at startup; older versions are moved to a separate folder.
- Story redraws keep the open tab sheet and the open reasonings.
- UI texts that were hard-coded (tree node cards, bundle states, error dialog button, prompt format list, login
  description, `ERROR` fallbacks) are localization keys.
- Inline styles were replaced by CSS classes (`shared-styles.css`, `CssStyles`) and all browser-side JavaScript moved to
  `ui.JavascriptLibrary`.
- The bundled plugins use the extension logger instead of `LoggerFactory`.
- Plugins built for 1.0.0 must be rebuilt from the 1.1.0 source tree; the plugin API is not frozen and the extension
  verifier shows plugins that no longer match as *Invalid*.

### Fixed

- A page reload logged out every user who had not chosen *Save login*; a user who is already logged in this session is
  now kept.
- `SessionManager` kept the session-scoped `user` proxy, so user filters running on another thread failed with
  `ScopeNotActiveException`; it now stores a plain copy taken at login.
- The plugin Docker image no longer runs the Vaadin frontend build.
- Summaries: the inference now starts after the transaction that creates the summary has committed. A fast answer
  could be saved before the new summary row was visible, which failed with `OptimisticLockException` and left the
  summary request unanswered (seen as intermittent `MetaSummaryTest` timeouts in CI).

### Known issues

- The Japanese and Chinese texts are machine translations that a native speaker should review.
- The phone layout does not follow a rotation, and `PhoneManuscriptStoryPart` duplicates much of
  `ManuscriptStoryPart`.
- Plugins' own texts, fixed words in exported files and default object names created by services are English only.
- A restored story part does not get its former children back.

## [1.0.0] - 2026-10-10

Initial release.

- Branching story tree (regenerate, swipe, branch), lorebooks with tag, text and regex activation and nested lorebooks,
  summaries and meta summaries, Handlebars and SillyTavern-macro prompts, SillyTavern import.
- Search across all branches, story export (TXT, Markdown, HTML, DOCX, PDF, EPUB), images on story parts.
- OpenAI-compatible providers with timeout, retries, classified errors and a connection test.
- Per-book backups, scheduled database backups with staged restore, a copy of the database before a migration, trash
  with restore.
- Login throttling per user and per address, encrypted API keys, server-side admin checks.
- OSGi extension system with verification of bundles, and the Author's Note, Chapter Marker, Lorebook VCS, Reviewer and
  Side Query plugins.
- Docker image (amd64, arm64), WAR and desktop archives for five targets.

[Unreleased]: https://github.com/Enerccio/Marginalia/compare/v1.4.0...HEAD
[1.4.0]: https://github.com/Enerccio/Marginalia/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/Enerccio/Marginalia/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/Enerccio/Marginalia/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Enerccio/Marginalia/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Enerccio/Marginalia/releases/tag/v1.0.0
