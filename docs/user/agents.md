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
| **Status** | What its run is doing: *Running*, *Waiting for you*, *Waiting for approval*, *Pausing*, *Paused*, *Stopping*, *Stopped, result kept*, *Done, waits for the review*. An agent without a run says *Not running*, or *Disabled* when it is switched off. |

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
| Files | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `delete_file` in its own workspace (notes it keeps between runs). |
| `@file:/path` | Not a tool but a way to call one: a text argument of a command that is exactly `@file:/notes/chapter.md` is replaced by the contents of that file of the agent's workspace, so the agent can write a long text to a file once, check it, and pass it to a command (the result shows the short reference in the call, the command gets the text). `write_file` and `append_file` take it as their content too, which copies a file or adds it to another: a long file can be written in parts, each part within the longest answer of the model, and put together this way. |
| `update_plan` | Its to-do list. |
| `get_run_status` | How many rounds and tokens it has used, how much context, and the longest answer it can write. The agent is also told these limits of its model in its prompt, so it writes a long text in parts. |
| `ask_user` | A question for you. The run waits (*Waiting for you*). |
| `finish` | Its report: the run is *Done*. The report is shown as the reason of the status. |

How a run ends a stretch of work:

- **Waiting for you:** the agent answered without a tool call, or asked you something. Answer it in the [console](#the-console).
- **Done:** the agent called `finish` (in a run you started *immediately*). A run you started *in the console* is a conversation: the report of the agent comes to you like a question, the run waits for your next message, and only **Finish** in the console (or **Stop**) ends it.
- **Paused**, with the reason: the limit of rounds, the token budget or the context limit is reached, the model could not be asked (an error of
  the provider, your limit on the AI), or the server was restarted. **Resume** goes on from the last complete round; a call that was in the air when the
  server stopped is reported to the model as "outcome unknown".
- **Failed:** the runner itself broke; the reason says what.

**Pause** and **Stop** take effect between two rounds: the model request and the calls that are going on finish first.

## The console

**Console** opens a window for the latest run of the agent, with three tabs.

**Run** shows the *transcript*: what you told the agent, what it said (both as [Markdown](https://commonmark.org/help/), so lists, headings and emphasis show as such), and every command or file tool it called, folded to one line with the
arguments (click it to see them and the result, shown as plain text). When the model sends its **reasoning** (what it thought before it answered), a folded *Reasoning* block shows it before the answer, and the download has it. Whether you get any depends on the model and the server: it has to report it, and your AI has to have reasoning switched on. The state of the run, the **context** (how many tokens the last request used of the limit of the model, so you see how close the run is to a pause), the rounds and tokens it used (and the token budget, when there
is one) stay above it and the box for your message under it, while the transcript scrolls. The last bubble of the transcript is what the agent waits for:

- when the run is *Waiting for you*, the question of the agent; write the answer in the box and press **Send** (or **Ctrl+Enter**), and the agent goes on;
- when the run is *Done*, the report of the agent.

The buttons at the bottom are **Pause**, **Resume** and **Stop**, which do what they do in the table, and **Finish**, which ends a run that waits for you (its work is over, *Done*).

The window looks at the run every two seconds while it is open; **Refresh** does it at once. The **Run** tab shows the latest run that was not discarded, so a run that finished or failed can still be read there until the agent starts a new run; older runs are in **Runs**.

**Runs** is the history of the agent: every run it had, the latest first, with its date, status, rounds, tokens and result. Use the eye to **open** a
run in the **Run** tab and read its steps and its report again (the run you open stays there until you open another one), the arrow to
**download** its transcript as a Markdown file (what was said, every command with its arguments and result, the report), and the bin to **delete** it.
A deleted run goes to the [trash](trash.md). A run that is going on can't be deleted, stop it first. Runs you stopped and **discarded** are in the list
too, so you can read what they did or delete them; the **Run** tab shows them only when you open them.

**Workspace** shows the files of the agent: its notes and reports, which it keeps between runs. Click a folder to go in (**Up** goes back), click a
text file to read it, use the arrows to download a file or the bin to delete it, and **Clear** deletes everything (stale notes mislead a fresh run).
Files can't be changed while the agent works, pause it first. The workspace has a limit of size, file size and number of files; the line
under the table says how much is used.

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

A paused run keeps its copy and works on it again when you resume it. When the run ends (it is *Done*, *Failed* or you stop it and discard the result)
the copy is deleted for good, and the next run makes a new one from the book as it is then. Nothing brings the changes of the copy back into your
book yet.

## Creating and editing an agent

**New agent** opens the same window as **Edit**:

| Field | |
|---|---|
| **Name** | Required. |
| **Enabled** | A disabled agent can't be started; its runs that exist are not touched. |
| **Book** | The book the agent works on, required. Only books that have no agent are offered, and the book of an agent can't be changed later. |
| **Model**, **Protocols** | The model and the protocol the agent talks with. Both, or neither; an agent without them can't be started. |
| **Destructive commands** | What happens when the agent wants a command that changes or removes data: *Run them*, *Ask first* (a [window](#asking-you) asks you to approve it) or *Refuse them*. |
| **Most rounds**, **Token budget of a run**, **Context limit** | Optional limits of one run. The limit your administrator gave you applies in any case. |
| **System prompt** | What the agent is told to be. A new agent starts with the default one; **Restore default** puts it back. |
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
