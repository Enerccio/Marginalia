# Prompt History

Keeps the last requests sent to the models in your session, with the answers and the reasoning, so you can see what the
model was really asked.

Adds a panel **Prompt history** to *Settings → Extension Settings*. Its button **Show prompt history** opens a dialog with
the last 20 requests of your session, newest first: the **date**, the **model** (the name of the AI and the model id) and
the **type of request** of each - `generation` for a new part, a swipe or a regeneration, `annotate`, `summary`,
`impersonate`, `redraft`, `agent`, `help`, or the name of a plugin's own kind of work - with its status and duration.
Choose a request to read, in tabs, **Request** (every message that was sent, under its role), **Reasoning** (what the model
thought, when the provider sent it) and **Response** (the answer, the functions the model called, the error of a failed
request). *Refresh* reloads the list, *Clear* empties it.

## Usage

Nothing to configure. Install the plugin (see [Marginalia plugins](../README.md)) and open the dialog from the settings.

The same dialog is opened for an [agent](../../../manual/user/agents.md) by the **Prompt history** button in the footer of
its console, with the requests of the run that is shown.

Notes:

- A request is listed as soon as it is sent, marked *Running*, and its answer and reasoning grow in the dialog as the
  model writes them.
- Every kind of request is kept, generations too (the [Prompt Inspector](../prompt-inspector/README.md) is the way to
  *stop* a generation and look at its prompt first). The point is to see what a plugin or a command really sent.
- The history is in memory, in your session, and packed (gzip) when a request ends, unpacked only when the dialog shows it.
  It is gone when you log out or the server restarts, and nothing is saved to the database. Each browser session has its
  own list of the 20 newest requests.
- Requests of agents are not in the list of the session: an agent works in a session of its own, made up again at every
  turn of the run, so they are kept per run (20 per run, for the 10 latest runs) and shown by the console of the run.
- Everything is shown as it was sent, story text included: do not open the dialog on a shared screen.

## For developers

The plugin installs an `InferenceListener` with `InferenceSession.addListener`: `onRequest` is called on the thread that
starts the request, where the session of the user can be reached, and returns the `Observation` that gets the pieces of
the answer (`onChunk`) and the end (`onFinish`) on whatever thread the provider works on. The history of a session is in
`SessionPoint.getPluginData(key, supplier)`, the free per-session map for extensions; the dialog is told about changes
through the store and updates itself with `ui.access`. The button of the console is added by a decorator on
`AgentConsoleDialog.create`. See [Services](../../../manual/developer/services.md) (`InferenceSession`) and
[Plugin development](../../../manual/developer/plugins/index.md).
