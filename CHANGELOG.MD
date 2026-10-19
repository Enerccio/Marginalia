# Changelog

All notable changes to Marginalia are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Commands.** New `CommandService` (`registerCommands`, `unregisterCommands` for a whole suite, `unregisterCommand` by name, `getCommands`, `getCommand`, `execute`): a command suite is an object with public `void` methods taking a `CommandContext` and annotated with the new `@Command(commandName, humanName, description, llmDescription, async)` (the texts are `L` localization keys, with a `...Text` variant with a plain string for extensions: `humanNameText`, `descriptionText`, `llmDescriptionText`, also in `@CommandParameter` and `@CommandReturn`; the human name needs one of its two, the other texts are optional, and giving both of a pair is refused when the suite is registered). The arguments of a run are read with `CommandContext.get(...)`, the result is set with `setReturn(...)` (before the method returns when `async = false`; an `async = true` command runs on a worker thread and answers whenever it likes) and `execute` returns it in a `CompletableFuture`. Plugins register their suites in `onExtensionLoad` and unregister them in `onExtensionUnload`. A suite must be stateless: it reads the current state from the services and from the `SessionPoint`, which now also holds the user's `Workspace` (`getWorkspace()`, set when the page logs in). New `CommandUtils` (`getWorkspace()`, `getManuscriptSection()`, `getManuscriptWindow()`, `getCurrentManuscript()`, `getCurrentAI()`; `null` when there is nothing open) reads it for commands. Commands can be overloaded (methods with the same `commandName` and different `@CommandParameterSignature`, dispatched by the number and types of the arguments; no signature means any arguments, read as a list of objects with `CommandContext.getPositional()`), declare their `@CommandReturn`, and a run can be stopped: `CommandContext.isTerminated()`, cancelling the future from `execute`, `executePositional` or `executeLine`. The application registers its own commands when the service starts: `/hello-world [message]` and `/hello-world-async [message]` (the same after 10 seconds, to try an asynchronous command). The story tab of the book window has a **command bar** next to the menu: type `/` for the commands, a list filters them, a help shows the description and the next argument (arguments are separated by spaces, `\ ` and `\\` escape a space and a backslash, or are wrapped in double quotes, `\"` being a quote inside them); the help and the result are popovers above the bar (they cover the story and move nothing), an asynchronous command blocks the UI with a window that has a Stop button. The bar is a separate component (`CommandBar`) that sends results to any `CommandResultTarget`. The manual has a new **Commands** section in the user guide and in the developer guide (the model, writing a command suite, the command bar).
 The desktop and phone UI classes got common interfaces: `ManuscriptWindow` (implemented by `ManuscriptDialog` and `PhoneManuscriptDialog`) and `ManuscriptSection` (implemented by `ManuscriptPart` and `PhoneManuscriptPart`, `getOpenWindow()`), and `Workspace` gained `getManuscriptSection()` and `getOpenManuscriptWindow()` (plugin API from 1.4.0 for `DesktopWorkspace`, `PhoneWorkspace`, `ManuscriptPart` and `PhoneManuscriptPart`).
- **Command status.** `CommandContext.setStatus(String)` and `setSubStatus(String)`: two lines a command shows while it runs, the main line and a smaller detail under it, in the progress window of the command bar (a blank line is hidden). `CommandService.execute`, `executePositional` and `executeLine` now return a `CommandRun`, a `CompletableFuture<Object>` that also holds the lines (`getStatus()`, `getSubStatus()`, `addStatusListener(...)`).
- **`/search-replace-parts`** and **`/search-replace-lore-entries`** run the search and replace of the story and of the lorebook of the open book from the command bar (`search replace` and then the options `regex`, `caseSensitive`, `summaries`, `allBranches`, `revalidateSummaries`, with the defaults of the windows). They are asynchronous, ask a yes/no question with the number of matches before they replace (there is no preview) and fail without an open book. Six new localization keys (`ERROR_SEARCH_TEXT_EMPTY`, `ERROR_COMMAND_NO_LOREBOOK`, `MSG_COMMAND_SEARCHING`, `MSG_COMMAND_REPLACING`, `MSG_COMMAND_REPLACE_CONFIRM`, `MSG_COMMAND_REPLACE_CANCELLED`).
- **Uninterruptable commands.** `CommandContext.setUninterruptable(boolean)` and `isUninterruptable()`: a command marks the part of its run that can't be abandoned; `CommandRun.cancel` does nothing and the Stop button of the progress window is disabled meanwhile (`CommandRun.isUninterruptable()`, `addUninterruptableListener`). `CommandService.execute`, `executePositional` and `executeLine` got a form with a last argument `boolean uninterruptable` that starts the run so and keeps it so, for a command that starts another asynchronous one. `CommandUtils.confirm(context, question)` asks the user from a worker thread, `ManuscriptWindow.reload()` (new, in both book windows) shows the book again after a change made behind its back.
- 28 new localization keys (`LABEL_COMMAND_*`, `DESC_COMMAND_*`) in the English, Japanese and Chinese bundles for the texts of the commands the application registers. `CommandHelp.matchingNames` and `CommandHelp.of` take a function that localizes the keys; `CommandDefinition`, `CommandParameterDefinition` and `CommandReturnDefinition` hold their texts as the new `CommandText`.
- **`/take-backup`** takes a backup of the open book, as the **Take backup** button of the Backups tab does, and shows its progress in the window. Four new localization keys (`ERROR_COMMAND_NO_BOOK_OPEN`, `MSG_COMMAND_BACKUP_TAKING`, `MSG_COMMAND_BACKUP_WRITING`, `MSG_COMMAND_BACKUP_TAKEN`) in the English, Japanese and Chinese bundles.
- **Search and replace** in the story (settings menu ⚙ of the story tab, desktop and phone): changes a text in the parts of the story and, optionally, in their summaries, in the active branch or in all branches, with an optional regular expression. The window lists the found parts and summaries and opens each to the matches with the text before and after, as it is and as it will be; the search runs on a worker thread with a progress bar and a Stop button, nothing changes until **Replace**. The hashes of the summaries the search covers are recomputed so they stay valid; the summaries it does not cover (other branches that share the changed parts, or all of them when summaries are not searched) become outdated and the user is asked before that happens. The option **Revalidate all summaries** (on by default) instead recomputes the hash of every summary of the book, touched or not and outdated or not, so none becomes outdated. Both windows have a **Case sensitive** option (on by default, locked while **Search by regex** is on, a regex can use `(?i)`); `TextSearch.of` and the `Options` of `ManuscriptReplaceService` and `LorebookReplaceService` got a `caseSensitive` parameter. The window is `SearchReplaceDialog` (domain agnostic base, usable for lorebooks later) and `ManuscriptSearchReplaceDialog`, the work is done by the new `ManuscriptReplaceService`, `EntityTextSearch` and `TextSearch`. Plugin API from 1.4.0: new methods `createSearchReplaceMenuItem` and `openSearchReplaceDialog` of `ManuscriptStoryPart` and `PhoneManuscriptStoryPart` (the first one adds an item to the settings menu, `cogs`); `SummaryService.collectFirstBlock` and `BlockTexts`; `ChatMessageService.findFulltextsContaining` and `findSummaryFulltextsContaining`. 23 new localization keys in the English, Japanese and Chinese bundles, new `search-hit-*` styles. Enter in the search or replace field starts the search.
- **Search and replace in lorebook entries.** A **Search and replace** button after **Add Entry** in the lorebook editor (an icon in the phone view) opens the same window as the story's, for the name, text and comment of the entries of the lorebook (regex option only); the table shows the entry names. New `LorebookReplaceService` (`plan`, `apply`), `LorebookEntryService.findFulltextsContaining`, `LorebookSearchReplaceDialog`; plugin API from 1.4.0: new `openSearchReplaceDialog` of `LorebookView` and `PhoneLorebookView`. `SearchReplaceDialog` got `getIdColumnHeader()`.
- **Command data severity.** New attribute `@Command.dataSeverity` of the enum `CommandDataSeverity` (`DESTRUCTIVE`, `NON_DESTRUCTIVE`): whether the command changes or removes data that exists. It defaults to `DESTRUCTIVE`, the safe assumption; a command that only reads or adds data says `NON_DESTRUCTIVE`. The forms of one command must share it, `CommandDefinition` got `dataSeverity()` (a new record component, after `async`). It is information for the user, who sees a red **Destructive** or green **Non-destructive** label with a tooltip in the command list and in the help of the command bar, and for agents that will run commands (the service does not enforce it). `/search-replace-parts` and `/search-replace-lore-entries` are destructive, `/take-backup` and `/hello-world*` are not. Four new localization keys (`LABEL_COMMAND_DESTRUCTIVE`, `LABEL_COMMAND_NON_DESTRUCTIVE`, `HELP_COMMAND_DESTRUCTIVE`, `HELP_COMMAND_NON_DESTRUCTIVE`) in the English, Japanese and Chinese bundles, new `command-severity*` styles.
- **Regular expressions page** in the user guide: the building blocks, typical replacements in a story, groups in the replacement, lorebook entry filters and use in the commands; the Search and replace pages link to it.
- **Version footer.** The version of the build and the short Git commit id (`Marginalia 1.4.0 (93ec118)`) are shown in a tiny text in the bottom left corner of the workspace (tooltip: full commit id, commit and build time) and under the login form. A run that does not come from the packaged WAR (`mvn jetty:run`, IDE) shows `dev` as the version. New `utils.BuildInfo`, `ui.components.BuildInfoFooter`, `build-info-footer` style. The published manual shows the version too (label next to the title and footer): build it with the new `tools/retype_build.sh`.
- **Permissions.** New entity `Permission` (`domain.security.permissions`, table `permissions`, migration `V15`; an `ExtendableEntity` with the `PermissionType` stored by name, the holder `permissionHolder` (a `User`) and `granted`) with `PermissionRepository` and `PermissionService`: `checkPermissions(user, type)` (false when the user has no permission of the type or it is not granted), `getPermission(user, type)`, `getPermissions(user)` and `setGranted(user, type, granted)`. A permission is found by its holder whoever created it (the owner is the administrator who gave it) and the user is loaded again, as the one in the session is a proxy; `save`, `saveWithoutEvent`, `delete` and `setGranted` are for administrators (`SecurityException`). `PermissionType`: `HELPER`, `CREATE_AI`.
- **Permissions dialog.** A key button in the user list (Admin → Users) opens `PermissionsDialog` (`ui.dialogs.permissions`), a modal dialog with a grid of the permissions of the user: the localized name, a *Granted* checkbox and a delete button (hard delete), **Add permission** with a combo box of the types the user has none of, **Save** (off until something changes) and **Exit** (asks when there are unsaved changes). The settings of a permission type are edited in its detail row, which an arrow in front of the name folds (folded at first; fixed column widths like other tables): `PermissionUIFactory` / `PermissionUIFactoryImpl` return a `PermissionUIComponent` or `null`, `HelperPermissionComponent` for `HELPER`. After a save the pages of the edited user are reloaded (`SessionManager.reloadUser`), unless it is the current user. New `Localization.getPermissionType(PermissionType)`, `AdminPart.openPermissionsDialog` (plugin API from 1.4.0, the users column of the grid is wider), a `fold-spacer` style, seven localization keys (`LABEL_PERMISSIONS`, `LABEL_PERMISSION`, `LABEL_GRANTED`, `LABEL_ADD_PERMISSION`, `ENUM_PERMISSION_HELPER`, `ENUM_PERMISSION_CREATE_AI`, `MSG_UNSAVED_CHANGES_EXIT`) in the English, Japanese and Chinese bundles.
- **Generation options.** New `StoryGenerationService.generateNextTurn(manuscript, input, request, listener, GenerationOptions)` (the four argument form delegates with `GenerationOptions.defaults()`): `GenerationOptions(ai, protocol, context)` makes a generation use another AI / protocol than the book's, and a `RequestAttributes` context for a caller without an HTTP request (the call no longer throws "Called out of ui!" but needs a request or a context). The model is kept in the generation, not written to the book. Preparation for running agents.
- **Regular expressions of users and models are time limited.** New `utils.TimeSafeText` (a `CharSequence` whose `charAt` throws `RegexTimeoutException` after a deadline, so a pattern that backtracks is stopped instead of holding its thread; `TimeSafeText.matcher(pattern, text, deadline)`, `view()` for the places that take a `Function<String, CharSequence>`) with 30 seconds as the limit, changeable with the system property `marginalia.regexTimeoutMillis`. `/search-lore` and `/find-occurrences` have 30 seconds for the whole search; `TextSearch` (search and replace in the story and the lorebooks, so also `/search-replace-parts` and `/search-replace-lore-entries`) gives each text 30 seconds and then fails with `RegexTimeoutException` (plugins that call `TextSearch` can see it). The file tools of an agent keep their own limit of three seconds. New localization key `ERROR_REGEX_TIMEOUT`.
- **Commands that read the lorebooks.** Four `NON_DESTRUCTIVE` commands, one class each in `domain.service.impl.commands.lorebook.read`: `/list-lorebooks` (the root lorebook of the open book and the tree of its subbooks, numbered, with entry counts, tags and subbooks), `/list-lore-entries [lorebook] [filter] [offset] [limit]` (settings and a preview of each entry; the filter matches the name or a tag), `/get-lore-entry <entry> [lorebook] [offset] [length]` (settings, keys, tags, negative tags, the comment and a slice of the text) and `/search-lore <query> [regex] [limit]` (name, text and comment of the entries of all the lorebooks of the book, with snippets). Like the commands that read the story they answer JSON text, are paged and bounded and give out no ids: a lorebook is addressed by its number from `/list-lorebooks` (the root is 1), an entry by its number in the lorebook. Lorebooks the book does not use are not reachable. Entries with the same order are now listed by creation (`findByLorebookOrdered`), so the numbers are stable. `ManuscriptReadSupport` and its `Book` have `protected`/`public` members now (the new package extends it). 22 new localization keys (`LABEL_COMMAND_*`, `DESC_COMMAND_*`, `ERROR_COMMAND_NO_SUCH_LOREBOOK`, `ERROR_COMMAND_NO_SUCH_LORE_ENTRY`) in the English, Japanese and Chinese bundles.
- **Commands that read the book.** Fifteen `NON_DESTRUCTIVE` commands, one class each in `domain.service.impl.commands.manuscript.read`: `/get-book-info`, `/list-parts`, `/get-part`, `/list-swipes`, `/list-branches`, `/get-part-metrics`, `/show-prompt`, `/search-parts`, `/find-occurrences`, `/compare-parts`, `/count-tokens`, `/list-summaries`, `/get-summary`, `/check-summaries` and `/get-template-variables`. They answer JSON text, lists are paged and texts come in slices. A part is addressed by its position on a branch (the first part is 1) and a branch by its number from `/list-branches`; no id is given out or accepted. New `utils.TextDiff` (sentence diff). 83 new localization keys (`LABEL_COMMAND_*`, `DESC_COMMAND_*`, `ERROR_COMMAND_*`) in the English, Japanese and Chinese bundles.
- **Command results an agent can keep.** The new file tools `list_command_results` and `save_command_result(path, id?, append?)` let an agent save the whole result of a command call to a file of its workspace without writing it out again (a long generated text, a result that was cut), and look at an older one later. The last 10 results of the run are kept whole with a number each (new `CommandResults`, filled by `CommandFunctions.forAgent`/`create`, which got a form with a `CommandResults` argument; `AgentVirtualFileSystemService.getFileTools` too). The note the runner adds to the system prompt says so and asks the agent not to write out text it already has.
- **Commands of the Lore Generator and the name extensions** for the command bar and for agents. Lore Generator: `/list-lore-templates [template]`, `/generate-lore-entry [prompt [template [lorebook [entry [order [scope [limit]]]]]]]` and `/compact-lore-entry text [lorebook [entry [order [scope [limit]]]]]`. Like the dialog they store nothing and only read (`NON_DESTRUCTIVE`): they return the generated and the compacted text (without its code block) and the caller puts it into an entry. The lorebook and the entry are numbers like in `/list-lore-entries`, the entry (or, for one that does not exist yet, the `order` it will get) decides which lore the model sees; the model is the default model of the user. Name generator: `/list-name-categories` and `/generate-names category count [min_length [max_length]]`. Name provider: `/list-real-name-lists` and `/pick-real-names list count`. They answer JSON. `/generate-lore-entry` starts from a saved generation prompt (the `template` argument, else the one used last, `none` for no template) and adds the `prompt` of the caller after it under *Task for this entry*. The model of `/generate-lore-entry` gets no name tools; a caller gets names with the name commands and writes them into the prompt. `LorebookReadSupport.Lore` (constructor, `number`, `book`, `entries`, `entry`) is `public` so that a plugin can address lorebooks and entries the same way.
- **`AgenticSessionPoint`.** A `SessionPoint` for a session without a browser: it holds the book directly (`getCurrentManuscript()`, `setManuscript`), and has no workspace and no book window, so the commands work for an agent as for the user. Preparation for running agents.

- **Agents: entities and services.** New entities `Agent` (the definition of an LLM that works on one manuscript: name, `enabled`, the manuscript as a real column, extended attributes for the model profiles `AgentModelProfile` (AI + protocol per role, the first is the main one), system prompt, instructions, command allowlist, `DestructiveCommandMode`, max rounds, token budget, context cap) and `AgentRun` (one execution: `AgentRunStatus`, tokens and rounds added up in SQL, the working copy, `startedAt` / `finishedAt`, `statusReason`, `language`, `uuidPairs`), migration `V16`. New `AgentService` (a manuscript has one agent, which is not changed; the manuscript must be the user's own; limits of the configuration; `getGrant`, `resolveProfile`, `cloneAgent`) and `AgentRunService` (`start`: one unfinished run per agent and the concurrent runs of the user are limited; `transition` along the allowed changes of `AgentRunStatus`; `addUsage`; `getTokenBudget`; `recoverInterrupted` for a start of the server; deleting a run discards it and its working copy). A soft deleted manuscript takes its agent and their runs with it (plugin visible: `ManuscriptService.delete`); agents and runs are in the trash and in the cleanup. The runner that executes a started run is described below. New permission type `AGENTS` (`Permission.agentsMaxConcurrentRuns`, `agentsRunTokenBudget`), localization keys `ENUM_PERMISSION_AGENTS`, `ENUM_DOMAIN_AGENT`, `ENUM_DOMAIN_AGENT_RUN` in the English, Japanese and Chinese bundles.
- **Agents: tab, dialogs and the lock of the book.** Users who may use agents (administrators, or the granted permission `AGENTS`) have a new **Agents** tab (`AgentPart`, desktop workspace only; the phone layout has none yet): a table of the agents with their ID (uuid), name and the state of their run, and the buttons **Start** / **Resume**, **Pause**, **Stop** (asks whether the result is kept or discarded), **Edit**, **Clone** and **Delete**. **New agent** and **Edit** open `AgentDialog`: name, enabled, the book (only the books that have no agent; not changed afterwards), the main model (AI and protocol), what happens with destructive commands, the limits of a run (rounds, tokens, context), the system prompt (a new agent gets `Defaults.DEFAULT_AGENT_PROMPT`, **Restore default** puts it back), the instructions and the allowed commands: a table with a check box per command and a selection that ticks **All**, **None** or **All non-destructive** at once (`AgentCommandsEditor`). **Clone** (`AgentCloneDialog`) copies an agent with its files to another book. The administrator limits how many agents a user has, clones included: the permission `AGENTS` has a new setting **Most agents** (`Permission.agentsMaxAgents`, five when not set), and a settings component for the three limits of the permission in the permissions dialog (`AgentsPermissionComponent`). `AgentGrant` got a fourth component `maxAgents` and `AgentService` the new `canCreate()` (plugin visible: `AgentService.save` of a new agent and `cloneAgent` refuse with an `IllegalStateException` above the limit).
- **Agents: the book is locked while a run works.** While a run of the agent of a book works or waits for the user (`RUNNING`, `IDLE`, `WAITING_APPROVAL`, `PAUSING`, `STOPPING`, see `AgentRunStatus.holdsLock()`), every service that changes the book refuses with the new `ManuscriptLockedException`: `ManuscriptService` (`save`, `delete`, `restoreBackup`), `ChatMessageService` (`save`, so also `createRoot`, `addChild`, `branch` and the swipes that save the book; `delete`), `SummaryService` (`createSummary`, `createMetaSummary`, `updateSummaryText`, `attachMetaSummary`, `removeSummary`), `ManuscriptReplaceService.apply` and `StoryGenerationService.generateNextTurn`. Reading is not locked. A paused, stopped or finished run releases the lock; the lorebooks are not locked. New `AgentRunService.findLockingRun(manuscript)` and `checkNotLocked(manuscript)`. The screens check too: the book list does not open a locked book (it says which agent holds it) and any other `ManuscriptLockedException` that reaches `UIUtils.internalServerError` is shown as a notice, not as an error window. Plugins that change a book of a user who uses agents should be ready for the exception.
- **Agents: pause, stop and resume.** New `AgentRunService.pause`, `stop(run, keep)` and `resume` (the buttons of the tab): a run that waits for the user is paused at once, a working one goes to `PAUSING` / `STOPPING` and the runner finishes it; when no runner is busy with the run (`workerStarted` / `workerFinished` are for the runner to call, in memory only) the service finishes the request itself, so a run never hangs in `PAUSING`. Stopping and discarding drops the working copy. Resuming checks what a start checks (permission, enabled agent, usable model, a free concurrent run).
- **Agents: the request context of a run.** New `AgentRequestAttributes` (package `bound`, a `RequestAttributes` that is not a servlet request) and the record `AgentSession(userUuid, language)` (what a run keeps of its session, `AgentSession.of(run)`), and `AgentRunService.openSession(run)` that makes the context from the database: the `user` bean is the owner of the run, the `sessionPoint` an `AgenticSessionPoint` with the language of the run, so the services, `Localization` and the commands work on the threads of a run as they do for the user. **Plugin visible:** `ThreadCopyRequestAttributes.create()` now returns `RequestAttributes` (it returned `ThreadCopyRequestAttributes`; the copy of a browser session as before, the attributes of an agent run as they are, `null` without a context), so a plugin that stored the result in a `ThreadCopyRequestAttributes` variable has to use `RequestAttributes` (`google-genai` is changed). Preparation for running agents.
- **Agents: commands as the functions of an agent.** New `CommandFunctions` (package `domain.service.agent`): `CommandFunctions.forAgent(commandService, agent, lookup, approval)` (or `create(...)` with a list of names and a mode) turns the commands an agent may call into a `FunctionSet`, one function per command with the parameters of all its forms in one schema, so a model that leaves out a middle option no longer fails on the ladder of overloads: the call goes to the smallest form that has every parameter it passed, the others are null or their default. A command name that is a valid function name is the function name, any other (a `.`, more than 64 characters) is written with `_` and six hexadecimal digits of its hash, so a name in a stored transcript means the same command after a restart. Results are cut at 20 000 characters with a note, a failure reaches the model as `Error: <message>`, a destructive command follows the destructive mode of the agent (`DENY` leaves it out, `ASK` waits for the decision of the user and is refused without one, `AUTO` runs it). **Plugin visible:** `@CommandParameter` has the optional attributes `optional`, `defaultValue` and `allowed`, `CommandParameterDefinition` the same as record components (the three-argument constructor stays), and `FunctionDefinition.dynamic(...)` makes a function without a Java method (`FunctionDefinition.render` is public). A parameter is required for an agent when every form has it and none says `optional`. The runner calls it.
- 57 new localization keys (`LABEL_AGENT*`, `ENUM_AGENT_RUN_*`, `ENUM_DESTRUCTIVE_MODE_*`, `ENUM_COMMAND_PRESET_*`, `ERROR_BOOK_LOCKED`, `HELP_AGENT*` and others) in the English, Japanese and Chinese bundles; `Localization.getAgentRunStatus` and `getDestructiveCommandMode` (plugin API: a class that implements `Localization` needs them). `DesktopWorkspace.create` has the new locals `agentsTabHeader` and `agentsTab`.

- **Agentic books.** `Manuscript` and `Lorebook` have the flag `agentic` (default false; columns added in the migration `V16`): the working copy of an agent, never listed with the books of the user. The lists of the UI (the Manuscripts tab on desktop and phone, the viewer, the lorebook lists and pickers of the Lorebooks tab and of the book dialogs, the book pickers of the agent dialogs) show only the other books. Plugin visible: the old methods (`findAllForUser`, `searchManuscripts`) are unchanged and return both kinds; new `ManuscriptService.findAllRegularForUser`, `findAllAgenticForUser`, `searchRegularManuscripts`, `searchAgenticManuscripts` and `LorebookService.findAllRegularForUser`, `findAllAgenticForUser`. A plugin that lists the books of the user for the user should call the regular ones.
- **Agents: the runner.** **Start** and **Resume** in the Agents tab now run the agent: the new `AgentRunner` (`domain.service`) takes a `RUNNING` run up in a background worker, in the request context of `openSession`, and owns the loop, one round at a time. The tools of a round are the commands the agent may call (`CommandFunctions.forAgent`), `AgentFileTools` and the new run tools `finish` (the report, the run is `DONE`), `ask_user` (the run is `IDLE` with the question as its reason), `update_plan` and `get_run_status` (`RunTools`). A reply without a call ends the turn in `IDLE`. The system prompt is made from the agent at every request (its prompt, the instructions, a note on how the run works), so a change of the instructions applies on a resume. Every round is saved as rows of the new `AgentMessage` (`AgentMessageService`: `append`, `transcript`, `count`; table `agent_messages` in the migration `V16`): the assistant message before its calls run, the results after, so a run interrupted in between gets an "outcome unknown" result for the calls on its next work. The runner enforces the rounds, the token budget and the context cap (the model's `maxContext` or the agent's and profile's lower caps; older rounds are not summarized yet): at a limit the run is paused with the reason; a model that can't be asked pauses the run too, only a failure of the runner is `FAILED`. A pause or stop is made at the next stable point, between rounds (`AgentRunService.getStopTarget` and `endStop` carry the choice of **Stop** to the worker). `AgentRunner.send(run, text)` writes the author's message to an `IDLE` run (the console does it). `recoverInterrupted` is now called when the server starts. Plugin visible: `InferenceService.round(payload, protocol, functions, callback)` asks the model once and reports the text and the calls it wants without calling them (default: `UnsupportedOperationException`; implemented by `OpenAICompatibleInferenceService`, so an agent whose model comes from an extension that does not implement it is paused with the reason), with `InferenceRoundCallback` and `RoundUsage`; `AgentRunService` has `getStopTarget` and `endStop`. Not done: the approval of `ASK` commands (they are refused), "Pause now", the summaries of older rounds, a per-AI limit of concurrent requests.
- **Agents: the working copy of a run.** Every run works on a hidden copy of the book, not on the book: `AgentRunService.start` calls the new `ManuscriptService.createAgenticCopy(original)` (the messages with their summaries and the active leaf, tags and settings of the book through the backup JSON, and its own copy of the lorebook tree through the new `LorebookService.copyLorebooks(lorebooks)`, all marked `agentic`, never published, with new uuids and nothing linked to the lorebooks of the user) and keeps it as `AgentRun.workingCopy` (now an eager relation: the runner uses the book on its own threads). `AgentRunService.openSession` refuses a run that has no copy (`IllegalStateException`), so an agent can never get the book of the user. The copy is deleted for good, with its messages, summaries, tags and lorebooks, and the run lets go of it, when the run gets to `DONE`, `FAILED`, `STOPPED_DISCARDED` or `APPLIED` (also when a run is deleted): new `ManuscriptService.deleteAgenticCopy`, `LorebookService.deleteAgenticCopy` and `ChatMessageService.hardDeleteForManuscript` (they refuse anything that is not a working copy), new `AgentRunRepository.updateWorkingCopy` and `ChatMessageRepository.getAllMessagesIncludingDeleted`. A paused or kept run keeps the copy; a resumed run that has none gets one. A new run makes a new copy. The book of the user is still locked while the run works. The lorebook import no longer matches by name against the working copies of agents. Not done: the uuid pairs and the merge of the copy back.
- **Agents: approval of commands.** An agent in the `ASK` mode no longer has its destructive commands refused: `CommandFunctions` gets an `Approval` from the runner, the run goes to `WAITING_APPROVAL` (with the command as its reason) and waits for the user. New `AgentApprovalService` (`request`, `pending`, `decide`, `rejectAll`, `hasPending`; in memory, a restart pauses the run as it does for any working one; `AgentApprovalServiceImpl` is a bean in `services-config.xml`): the console polls it and pops up the new `AgentApprovalDialog` (the command, its arguments, **Approve** / **Deny**, can't be dismissed without a decision). A pause, stop, discard, finish or failure of the run refuses what waits (`AgentRunServiceImpl.transition`), so the calls in the air end. What follows an approval runs in the context of the run, never in the session of the browser that approved. 4 new `LABEL_*` and 1 `MSG_AGENT_APPROVAL_QUESTION` localization keys in the English, Japanese and Chinese bundles.
- **Agents: the user is told when an agent needs them.** New column `agent_runs.attention` (in `V16`, `AgentRun.isAttention()`), set with every change of the status by `AgentRunStatus.needsAttention(reason)` (the agent waits for a message or asks a question, asks for an approval, finished, failed, or was paused with a reason of its own: a limit, a model that can't be asked, a restart), cleared by the new `AgentRunService.acknowledge(run)` (the console does it when it shows the run) or when the run goes on; `AgentRunService.findNeedingAttention()` lists them. The state is stored, so it is there when the user comes back; for the screens that are open now the new `AgentAttentionService` (`subscribe(userId, listener)`, `publish(run)`, `Notice`) pushes a change after the transaction commits, and every Agents tab (every browser window of the user) shows a count on its header (`AgentPart.getAttentionBadge()`), highlights the **Console** button of the agent, and pops up a notice with an **Open** button. `AgentConsoleDialog` acknowledges what it shows. 1 `LABEL_OPEN` and 5 `MSG_AGENT_NOTICE_*` localization keys.
- **Fixed: the usage of the last request of an agent run could be saved without an owner.** The counting of the tokens is asynchronous and could run after the run closed its `AgentRequestAttributes`, which cleared the session and left an empty user; the attributes now keep the user and the session point when they are closed, and `UsageService.add` refuses to save a usage when there is no user.
- **Agents: Start is a menu.** The **Start** button of the Agents tab opens a menu (a `ContextMenu`, `AgentPart.getLastStartMenu()` for the tests): **Start immediately** works as Start did, **Start in console** starts the run, opens its console and gives the agent another first message (`Defaults.AGENT_CONSOLE_START_MESSAGE`) that tells it to call `ask_user` at once and ask the author what they want, instead of working on its instructions. **Resume** of a paused run is the button as before. Plugin visible: `AgentRunner` has the new `run(run, firstMessage)` (for a `RUNNING` run with an empty transcript); a class that implements `AgentRunner` has to add it. The Agents tab does not show the notice of an agent whose console is open. 2 new `LABEL_START_*` localization keys.
- **Fixed: the Agents tab and the Start menu.** The table did not change when a stop or a pause was finished by the worker after the button was pressed (it waits for the round in the air), so **Start** never came back after **Stop and discard**: `AgentAttentionService.publish` now hears every change of a run (`Notice.attention` says whether it needs the user), and every open Agents tab reads its table again. The Start menu is a `MenuBar` item with a submenu, not a `ContextMenu` on a button, which never opened in a row of the table (its target had no parent yet). The console no longer shows a run that was stopped and discarded.
- **Fixed: a change of a run crashed the Agents tab, and the console read bottom up.** `AgentAttentionService` called the screens on the thread that committed the change, inside the commit with its connection already closed, and Vaadin runs the access to a UI at once on the calling thread, so the tab's first read of the database failed with "Connection handle already closed" (an error window whenever a run changed). The listeners are now called on a thread of the service (`AgentAttentionServiceImpl`, one thread, so in the order of the changes). In the console the question or the report of the agent was above the transcript and the box that answers it below; the order is now the state, the transcript, the question or report, the box. The name of who spoke and the text are blocks on lines of their own (they ran together).
- **The agent console shows Markdown.** What the agent and you say, and the question or report under the transcript, are rendered as Markdown (lists, headings, emphasis, code), like the help assistant. The arguments and results of a command stay plain monospace text.
- **Agent runs started in the console are conversations.** `AgentRun.interactive` (an extended attribute, set by `AgentRunService.start(agent, true)`; **Start in console** sets it, **Start immediately** does not). When such an agent calls `finish`, the report goes to you like a question and the run waits for your next message (`IDLE`) instead of ending; a new **Finish** button in the console (`AgentRunService.finish`) ends it (`DONE`). The agent is told so in its prompt. An autonomous run still ends with `finish`.
- **Fixed: a long report hid the steps of the agent in the console.** The report (or question) sat under the transcript and, when long, took all the height of the window and squeezed the transcript to a thin line, so a finished run showed only its report. The report or question is now the last bubble of the transcript, under the steps that led to it, and the whole transcript scrolls between the state line and the message box, which stay in place.
- **The agent console has a Runs tab: the history of the runs of the agent.** Every run, latest first (stopped and discarded ones too), with its date, status, rounds, tokens and result. **View** opens a run in the Run tab with all its steps and its report, **Download** gives its transcript as a Markdown file (`AgentTranscriptExport`), and **Delete** moves it to the trash (`AgentRunService.delete`; a run that is going on can't be deleted, stop it first). Until now a run could be deleted only together with its agent.
- **The console shows the current context of the run, and the application log tells what the agents do.** Next to the rounds and tokens the console shows the context of the last request against its cap (the lowest limit of the agent, the model profile and the model: `AgentRunService.getContextCap`), so a run that is near a pause for a full context can be seen coming; it is kept on the answer of the model (`AgentMessage.contextTokens`, an extended attribute, no migration). The log (`INFO`) has the run started, the model and limits when it works, every round (tokens, context, calls), every call (name, time, result length), every status change with its reason, stops, deletes, messages of the user (the length) and approvals. What the agent or you write, the arguments and the results are never logged.
- **Ctrl+Enter (Cmd+Enter on a Mac) sends the message in the agent console.**
- **The reasoning of an agent is kept and shown.** When the provider sends the reasoning of a round (`reasoning_content` or `reasoning` in the answer) it is stored beside the answer (`AgentMessage.reasoning`, an extended attribute, not a part of the transcript the model is sent) and shown in the console as a folded **Reasoning** block before the answer; the Markdown download of a run has it too. Plugin API: `InferenceService.InferenceRoundCallback` has a default method `onRound(text, reasoning, calls, usage)` that forwards to the old `onRound(text, calls, usage)`, so providers need no change; the OpenAI-compatible service calls the new one. A model that writes its reasoning into the text as `<think>…</think>` is not split out (yet).
- **An agent can pass a file as the text of an argument.** A text argument of a command that is exactly `@file:/path` is replaced, before the command runs, by the contents of that file of the agent's workspace (`CommandFunctions`, a new optional file reader of `forAgent` / `create`), so a long text is written once, checked and passed without the model writing it again in the call; the stored call keeps the short reference. The description of every text parameter and the note of the runner say so. A file that can't be read is an error naming the argument. In the `ASK` mode the approval window shows the text that will run. The `content` of the file tools `write_file` and `append_file` takes the same reference, so a file can be copied or added to another (`/book.md` built from `/parts/one.md`, `/parts/two.md`...) without the text passing through the model; the content is read before the target is changed.
- **More file tools for agents.** `list_files` and `grep_files` take a name pattern (`glob`: `*.md`, `chapters/*.md`, `**/draft-*`, `{md,txt}`; the case does not matter), `copy_file` copies a file or a folder with everything in it to a free name (all or nothing, within the limits of the workspace), and `merge_files` joins several files into one, in the order given, with a delimiter, without the text passing through the model (the last name of a path may be a pattern such as `/chapters/*.md`, the target left out so the merge can be repeated).
- **The agent is told how much it can read and write.** The system message of a run ends with the limits of its model (the context, and the longest answer: the lower of the protocol's reply tokens and the AI's max completion tokens), with the advice to write long texts in parts (`write_file`, then `append_file`), and `get_run_status` reports the longest answer too. Before, a model with a reply limit of 8000 tokens tried to write a file of 100 000 tokens in one call, which was cut off.
- **Agents: the console.** New button **Console** in the Agents tab opens `AgentConsoleDialog` for the latest run of the agent: the transcript (what the user and the agent said, each call folded with its arguments and result), the state, rounds and tokens of the run (with the token budget), the question of an `IDLE` run or the report of a `DONE` one, a box that sends the answer through `AgentRunner.send`, **Pause**, **Resume** and **Stop**, and a second tab, `AgentWorkspaceBrowser`, with the workspace of the agent: browse the folders, read a text file, download or delete a file, clear the workspace (not while the agent works). The console reads the run again every two seconds while it is open. 9 new `LABEL_*` and 7 new `MSG_AGENT_*` localization keys in the English, Japanese and Chinese bundles; the transcript uses the styles of the help assistant.
- **Agents: virtual file system and file tools.** New `AgentVirtualFileSystemService` (`open(run)`, `openWorkspace(agent)`, `getFileTools`, `getUsage`, `clearWorkspace`, `copyWorkspace`, `deleteWorkspace`, `deleteRunFolder`) and the package `domain.service.agent`: `VirtualPath` (absolute, NFC, `..` stops at the root, no empty or `.` parts, no control characters, drive letters, UNC or Windows device names, limits of length and depth), `VirtualFileSystem` over `VirtualMount`s (the workspace of the agent is the root, `/checkpoints` a read only list; symbolic links are never followed, names that differ only in case are one name, writes are atomic and under size, file size and file count limits `WorkspaceLimits`) and `RunFolder` (the layout `agent-runs/<run uuid>` and `agent-workspaces/<agent uuid>` in the application folder). The workspace belongs to the agent and stays between its runs; cloning an agent copies it, deleting the agent for good or purging it in the cleanup deletes it, and so for the folder of a run. New functions for the model, `AgentFileTools`: `list_files`, `read_file` (line ranges), `write_file`, `append_file`, `replace_in_file` (the text must be there exactly once), `regex_replace_in_file`, `grep_files`, `move_file` (the target must not exist), `delete_file`; results are bounded, errors name virtual paths only, regular expressions are stopped after three seconds. Plugin visible: `TextSearch.apply` and `TextSearch.matches` have an overload taking the view of the text the regular expression reads.

### Changed

- **Schema validation per table.** `persistence.xml` sets `hibernate.hbm2ddl.jdbc_metadata_extraction_strategy` to `individually`: the SQLite driver answers the columns of all tables with one compound SELECT, which SQLite refuses above 500 columns in total (the schema was at 490 before `V16`, the application did not start with more).
- **`SessionPoint` and `CommandUtils` (plugin API from 1.4.0).** `SessionPoint` got `getManuscriptSection()`, `getManuscriptWindow()`, `getCurrentManuscript()` and `getCurrentAI()` (the lookup in the workspace moved there from `CommandUtils`, whose methods now delegate to them). `CommandUtils.getSessionPoint()` also answers for request attributes that are not a servlet request when they hold an `AgenticSessionPoint`.

- **Generation controller (plugin API from 1.4.0).** `GenerationController` and `GenerationControllerEvent` have `getAi()` and `getProtocol()`, the model the generation really uses; read it there instead of `event.getManuscript().getAi()` / `getProtocol()`, which is the book's and may differ (the pipeline no longer reloads them into the book object). `GenerationController.getRequestAttributes()` returns `RequestAttributes` (was `ThreadCopyRequestAttributes`) and `ThreadCopyRequestAttributes.InRequestScope` takes any `RequestAttributes`. The `authorsnote` plugin counts its note with the model of the generation (`AuthorsNoteService.countTokens(AI, data)`).
- The command bar empties its field when a command starts.
- **Focus in dialogs.** Dialogs put the cursor into the field they are about when they open (name of an AI, a model protocol, a user, a new manuscript from a backup, a profile or a tab; the file name of an export; the search text of search and replace; the text of the input and selection dialogs; and the same in the arc-outline, character-tracker, lore-generator, reviewer and sidequery plugins). The new-turn popover of the story tab (desktop and phone) puts it into the instructions. New `DialogFocus.onOpen(dialog, field)` helper (`ui.widgets`) and `NewTurnForm.focusInstructions()` (plugin API from 1.4.0).
- **Two application switches became permissions.** `AppSettings.userAisAllowed` (users can create their own AIs) is now the permission `CREATE_AI` and `AppSettings.helpEnabled` (the help assistant) the permission `HELPER`, per user; administrators always have both. App migration 4 → 5 (`PermissionsMigration`) gives a permission to every existing user when its switch was on, new users have none. Removed: the checkbox *Users can create their own models* of the Users tab, the checkbox *Enabled* of the Help assistant tab, `AIService.isUserCreationAllowed` / `setUserCreationAllowed`, the component `userAisCheckbox` of `AdminPart.createUsersTab` (plugin API), the first component `enabled` of `HelpAssistantService.Settings` and its constructors. `HelpAssistantService.isAvailable` is per user now. The fields of `AppSettings` stay (deprecated), only for the migration.
- **Help assistant: per user settings.** *Apply limits* and *Allow navigating the interface* moved from the Help assistant tab to the `HELPER` permission of each user (extended attributes `Permission.helperApplyLimits` and `helperInteractions`), the migration copies the old application settings into the new `HELPER` permissions. `HelpAssistantService.Settings` is now `(aiUuid, adminPrompt, userPrompt, adminData)`. An administrator without the permission is answered with both off.
- **Agent console, Workspace tab.** Smaller text, compact rows, less space between the parts and no padding around the tab (the Run and Runs tabs got a small padding of their own), so a folder shows many files instead of one or two; the file list and the viewer have a fixed height (the viewer scrolls) and the viewer shows the first 2,000 characters of a file with a note of how long it is (it was 100,000), the download has the rest (new `workspace-browser` style; the text of `MSG_AGENT_FILE_CUT` takes the two numbers).

### Fixed

- A book created with **Add Book** is now remembered by the workspace autosave like a book opened from the list (the list did not know about its window).

### Known issues

- New users have neither `CREATE_AI` nor `HELPER`, an administrator gives them in the permissions of the user.

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

[Unreleased]: https://github.com/Enerccio/Marginalia/compare/v1.3.0...HEAD
[1.3.0]: https://github.com/Enerccio/Marginalia/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/Enerccio/Marginalia/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Enerccio/Marginalia/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Enerccio/Marginalia/releases/tag/v1.0.0
