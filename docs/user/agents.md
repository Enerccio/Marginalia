# Agents

An **agent** is a model that works on one of your books on its own, for a long time, calling the [commands](commands/index.md)
you allow it as its tools: it can read the story, check it against the lorebook and report to you. The **Agents** tab is
where you create them, set what each may do, start and stop their runs, talk to them in the [console](#the-console) and clone them.

!!!warning What this version can do
**Start** now runs the agent: it works in the background, one round at a time, calling the commands you allowed and its own file tools, until it
finishes, asks you something, reaches a limit or you pause or stop it (see [A run](#a-run)). It works on a hidden [copy](#the-working-copy) of the
book, so your book is never touched; the book itself is [locked](#the-book-is-locked) while it works. Bringing the changes of the agent back
into your book (the review) is not in this version yet, so use it to read the book and the lorebooks and to keep notes in its files.
!!!

The tab is there only if an administrator gave you the **Agents** [permission](administration/permissions.md) (administrators always
have it). It is not in the phone layout yet.

## The table

| Column | |
|---|---|
| **Agent ID** | The identifier of the agent. |
| **Name** | The name you gave it. |
| **Status** | What its run is doing: *Running*, *Waiting for you*, *Waiting for approval*, *Pausing*, *Paused*, *Stopping*, *Stopped, result kept*, *Done, waits for the review*, *Applied*. An agent without a run says *Not running*, or *Disabled* when it is switched off. |

The buttons at the end of a row depend on the run:

| Button | |
|---|---|
| **Start** | Opens a menu. **Start immediately**: the agent works on its task (its instructions) on its own and you hear of it when it asks or finishes. **Start in console**: opens the [console](#the-console) and tells the agent not to work yet but to ask you at once what you want it to do; the run is then a [conversation](#a-run) that goes on until you press **Finish**. |
| **Resume** | Goes on with a paused or stopped run (shown instead of **Start**). |
| **Pause** | Pauses a run that works or waits for you. |
| **Console** | Opens the [console](#the-console) of the agent. |
| **Stop** | Asks whether to **keep the result** (the run can be resumed later) or **discard** it. |
| **Edit** | Opens the agent. |
| **Clone** | Copies the agent, see [Cloning](#cloning). |
| **Delete** | Moves the agent, with its runs, to the [trash](trash.md). |

Starting is refused, with the reason, when the agent is disabled, has no model, or you have all the runs you may work at once.

An agent has at most one run that is not over, and a book has at most one agent.

## A run

A run is the job of the agent. It works in rounds: the model is asked once, it answers with text and/or calls to its tools, the results go back, and so on.
Every round is saved, so a run can be paused and go on later, even after the server was restarted. The agent has the commands you allowed and
these of its own:

| Tool | |
|---|---|
| Files | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `copy_file`, `merge_files`, `delete_file` in its own workspace (notes it keeps between runs). Next to the workspace it sees the read-only file `/AGENTS.md`, your instructions as the run started with them, which it can read again whenever it likes and never change. `list_files` and `grep_files` take a name pattern (`glob`, for example `*.md` or `chapters/*.md`), `copy_file` copies a folder with everything in it (a quick way to keep a version of the notes), `merge_files` joins several files into one without the text passing through the model (the last name of a path may be a pattern such as `/chapters/*.md`). `list_command_results` and `save_command_result` keep the long result of a command without the agent writing it out again: the last ten results are kept whole (the agent sees a long one cut), and one is saved to a file by its number, the newest when none is given; it then passes the file on as `@file:/path`. |
| `@file:/path` | Not a tool but a way to call one: a text argument of a command that is exactly `@file:/notes/chapter.md` is replaced by the contents of that file of the agent's workspace, so the agent can write a long text to a file once, check it, and pass it to a command (the result shows the short reference in the call, the command gets the text). `write_file` and `append_file` take it as their content too, which copies a file or adds it to another: a long file can be written in parts, each part within the longest answer of the model, and put together this way. |
| `@result` | Like `@file:`, for the result of a command: a text argument that is exactly `@result` is replaced by the whole result of the last command the agent called, `@result:3` by the result with that number (`list_command_results` shows the numbers). A generated text goes from one command to the next, or into `write_file` and `append_file`, without the agent writing it out. |
| `view_image` | Looks at an image file of its workspace (PNG, JPEG, GIF or WebP): the picture is shown to the model in the next message. Only an agent whose model is marked as seeing images (**The model sees images** in the agent) has it. A large image is scaled down before it is sent, a GIF shows its first frame, and only the newest four images stay in front of the model (an older one becomes a note, and the agent can look again). The images come from you (**Upload file**, or [attached to a message](#the-console)); the agent can't make one. |
| Images for commands | A command that sends pictures to a model of its own, such as [`generate-lore-entry`](extensions/lore-generator.md#commands), takes an `images` parameter: a list of paths of image files of the workspace (at most 8). It is the model of the command, not the agent's, that has to see them, so the agent can pass images even when its own model does not see images. |
| `update_plan` | Its to-do list. |
| `get_run_status` | How many rounds and tokens it has used, how much context, and the longest answer it can write. The agent is also told these limits of its model in its prompt, so it writes a long text in parts. |
| `checkpoint`, `checkpoint_list`, `checkpoint_restore`, `checkpoint_delete` | Its [checkpoints](#checkpoints): saves a restore point of the working copy and its workspace (with a label), lists them, goes back to one (when the round ends) and deletes one it does not need. |
| `get_sync_report` | Only after a resume that took over changes of yours: lists again what you changed in the book while the run was paused and what of it came into the agent's copy, see [Resuming a run](#resuming-a-run). |
| `request_compaction` | Compacts its own context: when the round ends it writes the summary of its earlier work, as with `/compact`. It is told to save its notes to a file first. See [Context filling up](#running-a-command-yourself). |
| `ask_user` | A question for you. The run waits (*Waiting for you*). |
| `finish` | Its report: the run is *Done*. The report is shown as the reason of the status. |

How a run ends a stretch of work:

- **Waiting for you:** the agent answered without a tool call, or asked you something. Answer it in the [console](#the-console).
- **Done:** the agent called `finish` (in a run you started *immediately*). A run you started *in the console* is a conversation: the report of the agent comes to you like a question, the run waits for your next message, and only **Finish** in the console (or **Stop**) ends it. The result of a *Done* run waits for your [review](#reviewing-the-result).
- **Paused**, with the reason: the limit of rounds, the token budget or the context limit is reached, the model could not be asked (an error of
  the provider, your limit on the AI), or the server was restarted. **Resume** goes on from the last complete round; a call that was in the air when the
  server stopped is reported to the model as "outcome unknown".
- **Failed:** the runner itself broke; the reason says what.

**Pause** and **Stop** take effect between two rounds: the model request and the calls that are going on finish first.

## The console

**Console** opens a window for the latest run of the agent, with four tabs: **Run**, **Runs**, **Workspace** and **Checkpoints**.

**Run** shows the *transcript*: what you told the agent, what it said (both as [Markdown](https://commonmark.org/help/), so lists, headings and emphasis show as such), and every command or file tool it called, folded to one line with the
arguments (click it to see them and the result, shown as plain text). When the model sends its **reasoning** (what it thought before it answered), a folded *Reasoning* block shows it before the answer, and the download has it. Whether you get any depends on the model and the server: it has to report it, and your AI has to have reasoning switched on. The state of the run, the **context** (how many tokens the last request used of the limit of the model, so you see how close the run is to a pause), the rounds and tokens it used (and the token budget, when there
is one) stay above it and the box for your message under it, while the transcript scrolls. What the agent waits for is in the transcript where it was written, as a bubble of its own that stays there when the run goes on:

- a **Question of the agent**, when the run is *Waiting for you*; write the answer in the box and press **Send** (or **Ctrl+Enter**), and the agent goes on;
- a **Report of the agent**, when it called `finish`.

In a conversation you can also write while the agent **works**. The message is **queued**: it appears as a dashed bubble at the end of the transcript and the agent
reads it at the end of its current turn (instead of stopping to wait for you); several queued messages go together as one. When it should not wait, press the
small **Send now** (lightning) button on the bubble: the agent drops the round it is in (the request to the model is canceled, a command that waited for your approval
is refused, calls that were not made are marked as skipped) and reads your message next. Commands that already ran stay done. Queued messages are lost when the server restarts, and
a run that is not a conversation (started *immediately*) does not take messages while it works.

### Running a command yourself

Under the transcript is the command line of the application (the one of the story tab: type `/`, the list and the help of the command appear above it) with what you can run on the agent: `/compact`, `/clear-workspace` and the
[commands the agent may call](#allowed-commands) that only read or add data (a command that changes or removes data is not offered, it would ask you to confirm
in a window of the book, which a run does not have). You run the command, not the agent: it is told, in a message of yours, that you ran it and what it answered
(and that what it read earlier may be out of date if the command changed the working copy), so it never believes it made a call it did not. It works as a message:
where the run waits (*Waiting for you*) or is *Paused* the command runs at once and the run stays as it is; where a [conversation](#the-console) works it is
queued, a dashed bubble marked *Command*, and runs at the end of the agent's turn, where the agent would have waited (the agent does not go on because of it).
The small **Run now** button on the bubble drops the round the agent is in, runs the command and lets the agent go on from there. A command that takes long
holds the box: wait for it before you write to the agent.

**`/compact [what to stress]`** frees the context. The agent is asked to read everything so far and write a summary (what you asked, what is done, what it found,
its notes and its plan, what it was about to do); that summary replaces everything before it in what the model reads. In the transcript you still see all of it,
and the summary as a folded block, **Summary of the earlier work**; the export keeps it too. The text after the command, if any, is what the summary should stress.
The request is the agent's own and its tokens count against the run. The question the agent is asked is its **Compaction prompt**: the one of the agent, else your
default in [Settings](account.md#templates), else the one of the administrator, else the built-in one. If the context is already so full that the request does
not fit, the oldest messages are left out of it (the summary says how many). Do it before the context is full, the usage line shows how close it is. Right after the compaction the context shown is an estimate of what the next request will hold (the summary, the system prompt and the definitions of the tools); the first answer of the model after it replaces it with the real number.

**Context filling up.** Once the context of the model passes 75% of its cap the agent gets a notice after every round (a message of the run, you see it in the
transcript): how full it is, and that it should save what it must not lose to a file of its workspace, update its plan and call `request_compaction`. Above 90% the notice says that the run
stops when the context is full. The compaction the agent asks for is the same as yours (its **Compaction prompt**, see [Creating and editing an agent](#creating-and-editing-an-agent), its tokens count against the run); if it fails, nothing is changed and the agent is told.
The notices need a context cap: the lowest of the cap of the agent and the limits of its model.

**`/clear-workspace [what to keep]`** deletes the files and all the folders of the [workspace](#the-working-copy) of the run (after you confirm, as with the button of the workspace). Name what
to keep after it, separated by spaces: files, folders (with everything in them) or patterns such as `*.md` or `chapters/*.md`, a path with spaces in double quotes
(`/clear-workspace /notes.md "/my drafts" *.png`). The folders above a kept file stay. If something named is not there nothing is deleted. `/AGENTS.md` and the checkpoints are not
part of the workspace and are never touched. The agent is told what was deleted and what was kept. In the transcript a command you ran is a bubble with the command and what it answered.

When the model of the agent sees images (see [Creating and editing an agent](#creating-and-editing-an-agent)), **Attach image** is next to **Send**: pick a PNG, JPEG, GIF or WebP file, or paste a picture from the clipboard
into the message box (**Ctrl+V**), and it waits above the box (the cross takes it back) until you send. Up to eight images go with a message; the text may be empty.
The images are saved in the **uploads** folder of the workspace (a name that is taken gets a number: `map-2.png`), within the limits and rules of the workspace,
and the model is shown them with your message. Delete them in the **Workspace** tab when you no longer need them.

The buttons at the bottom are **Pause**, **Resume** and **Stop**, which do what they do in the table, and **Finish**, which ends a run that waits for you (its work is over, *Done*).

The window looks at the run every two seconds while it is open; **Refresh** does it at once. The **Run** tab shows the latest run that was not discarded, so a run that finished or failed can still be read there until the agent starts a new run; older runs are in **Runs**.

**Runs** is the history of the agent: every run it had, the latest first, with its date, status, rounds, tokens and result. Use the eye to **open** a
run in the **Run** tab and read its steps and its report again (the run you open stays there until you open another one), the arrow to
**download** its transcript as a Markdown file (what was said, every command with its arguments and result, the report), and the bin to **delete** it.
A deleted run goes to the [trash](trash.md). A run that is going on can't be deleted, stop it first. Runs you stopped and **discarded** are in the list
too, so you can read what they did or delete them; the **Run** tab shows them only when you open them.

**Workspace** shows the files of the agent: its notes and reports, which it keeps between runs. Click a folder to go in (**Up** goes back), click a
text file to read it, use the arrows to download a file or the bin to delete it, and **Clear** deletes everything (stale notes mislead a fresh run).
**New folder** creates a folder in the open one and **Upload file** puts a file from your computer there, so you can hand the agent
a brief, a style guide or a draft. The same rules hold as for the agent's own files: names can't have `\ < > : " | ? *`, a trailing dot
or space, or be too long or too deep, the limits of file size and number of files apply, and an uploaded file must be text (UTF-8),
because the agent can only read text, or an image (PNG, JPEG, GIF or WebP, recognised by what the file contains, not by its name;
up to 5 MB, and it counts against the total and the number of files like any file). A file is never replaced by an upload: delete the old one first.
Files can't be changed while the agent works, pause it first. The workspace has a limit of size, file size and number of files, which your administrator can set in your [permission](administration/permissions.md#settings-of-a-permission); the line
under the table says how much is used. The name `AGENTS.md` at the top is taken by your instructions, which the agent sees there (the file can't be written or deleted).

## Asking you

When the agent needs you, you are told wherever you are in the app:

- a **number on the Agents tab** counts the agents that wait for you, and the **Console** button of such an agent is highlighted;
- a **notice** pops up with an **Open** button that opens the console of that agent.

The agent needs you when it *asks you something or waits for your next message*, asks you to **approve a command**, *finished*, *failed*, or
was *paused by itself* (a limit is reached, the model could not be asked, the server was restarted). A pause you asked for does not count.

Agents work while you are away, so this does not depend on the app being open: the number is there when you log in again, and every browser
window you have open gets the notice. Opening the console of the agent clears it (the other windows update).

When the agent is set to **Ask first** and wants a command that changes or removes data, the run stops in *Waiting for approval* and a window
pops up over the console with the command and its arguments. **Approve** lets it run, **Deny** tells the agent that you refused and it goes on.
Pausing or stopping the run refuses the command too. The command still works on the [copy](#the-working-copy) of the book. If the console is
closed, the window is there as soon as you open it.

## The working copy

Every run works on a **copy of your book** and of its lorebooks that the app makes when the run starts: the messages, summaries, tags and settings
as they are at that moment, and the lorebook with its sub-lorebooks. The copy is hidden: it is not in the lists of books and lorebooks. Whatever the
agent does, it does to the copy; your book stays as it was.

A paused run keeps its copy and works on it again when you resume it. A run that is *Done*, or that you stopped and kept, keeps it until you
[take over its result or discard it](#reviewing-the-result). When the run is *Failed*, you discard its result, or its changes are applied, the copy is deleted for good, and the next run
makes a new one from the book as it is then.

## Resuming a run

A paused or kept run does not hold the lock on your book, so you can edit the book meanwhile. When you **Resume** the run, its working copy catches up with what you did:

- What you changed and the agent did not (a part, a lorebook entry, a setting, a tag...) comes into the copy. A part you added comes in too and the agent can build on it; what you deleted is deleted from the copy.
- What you and the agent both changed to different texts **stays as the agent has it**: nothing the agent did is lost. It is a [conflict](#reviewing-the-result) that you resolve when you review the result, with both texts in front of you.
- What you changed and the agent deleted stays deleted, and what you deleted and the agent changed stays in the copy; the review shows both.

Nothing needs a click: the sync is part of resuming, and it takes a moment (a few seconds when you changed hundreds of items). The state of the copy before it is saved as an automatic [checkpoint](#checkpoints). The agent is told what happened, by the names it knows
the items by, in a message when it goes on ("While the run was paused the user changed the book..."), and can ask again with `get_sync_report`. To tell it something more, write to the run as usual.

## Reviewing the result

When a run is *Done* (or you stopped it with **Stop and keep the result**) the agent row and the console have a **Review changes** button. It opens a window with
everything the agent changed, compared with your book **as it is now**, one line each:

- **Book:** its name, description, language, point of view, tense, style and templates, its tags, and the data an extension keeps in the book (an arc outline, the character tracker).
- **Part:** a part whose text, instructions, scene setting, point-of-view character, present characters or summary the agent changed, a part it wrote, a part it deleted.
- **Lorebook** and **Entry:** a lorebook or an entry the agent made, changed (also moved, renamed, or with other tags, or other subbooks) or deleted.

Tick the lines you want, then **Apply the picked changes**. The buttons above the list tick **all** of them, **none**, **only new parts** (the new branch the agent wrote, nothing else) or everything
**without conflicts**. A line that needs another (a new part needs the part it follows if that is new too) brings it along when you tick it, and loses its followers when you untick it. Click a line to see it
on the right: the text of your book now and the text that will be written, **which you can edit** before you apply. If you write nothing, the agent's text is applied.

Your book may have changed since the run started (you can edit it as soon as the run is not working). What only you changed is not in the list and stays. What both of you changed to the same thing is no
change. What both changed to **different** texts is a **conflict**: it starts unticked, and when you tick it the text you see on the right replaces the text of the book. **Use the book's** puts the book's text
into the box (the line then changes nothing for that field), **Use the agent's** puts the agent's back; or write the result yourself. A line that can't be taken over (the part it follows, or the lorebook it
is in, was deleted from your book) says so and can't be ticked.

A lorebook that other books use is marked **Shared**: the change goes to the lorebook itself, so the other books get it too. It starts unticked.

Applying is done to the book **in place**: the parts keep their addresses, other books keep their links to a lorebook, and what the agent deleted goes to the trash. A **backup of the book as it was** is taken first
(it is among the backups of the book, with its lorebooks), and the changes are made all together or not at all. **Show the branch the agent ended on** also switches the book to the branch the agent was on, when it is
among what you applied. The summaries of parts that were edited are outdated as after an edit by hand; the agent's own summaries are taken over as they are.

When the changes are applied the run is *Applied* and its copy and checkpoints are deleted. **Discard the result** throws the copy away and leaves the book as it is. Until you do one of the two the agent has an unfinished run and
can't start another.

## Checkpoints

A **checkpoint** is a restore point of the working copy of the run (the parts, summaries, lorebooks and the data of the extensions, with the handles the agent was given) and of the
workspace of the agent, so that a job that went wrong can be undone: the agent goes back with `checkpoint_restore`, or you do it in the **Checkpoints** tab of the console. Who makes them:

- **Start of the run**, made when the run begins. It is never rolled out.
- **Automatic**, made by the run before a round in which the agent calls something that may change the book or the workspace (a command that is not a plain read, or a file tool that writes), and only when something
  changed since the last checkpoint. The last five are kept; the oldest goes when a new one is made.
- **Made on purpose**, by the agent with `checkpoint` (it is told to make one before a risky step) or by you with **Save a checkpoint**. They stay until deleted.

All the checkpoints of a run share a **quota** that the administrator sets in the [permission](administration/permissions.md#settings-of-a-permission) (100 MB when empty). Part of it is kept for the automatic ones, so that the
agent can't use it all up; a checkpoint made on purpose that does not fit is **refused**, nothing is deleted behind the back of whoever asked. The agent then gets the list and deletes what it does not need
(not the checkpoint of the start, and not the newest one); you can delete any in the tab. Each is an archive (a ZIP of the book as JSON, the pairs and the workspace files) you can **download**.

**Restore** makes the working copy again from the checkpoint: it is a new copy, the old one is deleted, the workspace is the saved one (files written after it are gone) and the handles of the agent for the items
that were there still work. The state before is saved as an automatic checkpoint first, if there is room, so a restore can be taken back. A restore takes about as long as the start of a run (half a minute for a book
of two thousand parts). You can restore while the agent does not work; the agent restores itself between two rounds and is told what came back. The checkpoints go when the run lets go of its copy.

## Creating and editing an agent

**New agent** opens the same window as **Edit**:

| Field | |
|---|---|
| **Name** | Required. |
| **Enabled** | A disabled agent can't be started; its runs that exist are not touched. |
| **Book** | The book the agent works on, required. Only books that have no agent are offered, and the book of an agent can't be changed later. |
| **Model**, **Protocols** | The model and the protocol the agent talks with. Both, or neither; an agent without them can't be started. |
| **The model sees images** | Tick it when the model takes images (the API of the model does not say, so you do). The agent then has `view_image` and you can attach images to your messages; without it it never gets a picture. |
| **Destructive commands** | What happens when the agent wants a command that changes or removes data: *Run them*, *Ask first* (a [window](#asking-you) asks you to approve it) or *Refuse them*. |
| **Most rounds**, **Token budget of a run**, **Context limit** | Optional limits of one run. The limit your administrator gave you applies in any case. |
| **System prompt** | What the agent is told to be. Leave it empty and the agent follows the default - yours from [Settings](account.md#templates), else your administrator's, else the built-in one - which is shown in grey. The info icon has **Insert Default Template**, which fills the default in so you can edit a copy of it; clear the field again to follow the default. |
| **Compaction prompt** | What the agent is asked when you [`/compact`](#running-a-command-yourself) it: to write the summary that replaces its earlier work. Empty follows the default, like the system prompt, with the same info icon. |
| **Instructions (agents.md)** | Your instructions to the agent, added to its prompt in every run. The agent can read them, never change them. |
| **Allowed commands** | See below. |

### Allowed commands

The agent is offered only the commands you tick. The table lists every command with its effect (*Destructive* changes or removes
data, *Non-destructive* only reads or adds). The **Selection** box ticks many at once: **All**, **None** or **All non-destructive**
(the default for a new agent); ticking a command yourself makes the selection *Custom*. Commands that an extension brings stay allowed
when the extension is not loaded, you just don't see them.

## Cloning

**Clone** asks for a name and a book that has no agent, and copies the definition of the agent and its files (not its runs) to a new
agent of that book. You can have as many agents, clones included, as your administrator allows (five unless they set another number);
at the limit **New agent** and **Clone** are off and say why.

## The book is locked

While a run of an agent works or waits for you, **its book is locked**: you can read it, but nothing changes it, so the agent never
works on text that moves under it. The book list does not open a locked book, it tells you which agent holds it, and
generating, editing, search and replace, summaries and deleting the book are refused. **Pause** or **Stop** the run to edit the book again;
a paused, stopped or finished run does not lock it.

The **lorebooks of the book** are locked with it: the lorebook the book uses and its sub-lorebooks. On the Lorebooks tab such a lorebook is
shown the way it is on a phone, as plain text and without any editing, with a note that says which agent holds it; **Refresh** shows it for
editing again after you paused or stopped the run. Other lorebooks are not affected.
