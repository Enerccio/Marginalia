# Prompt Inspector

Shows the prompt of a generation before it is sent to the model, and asks whether to send it.

Adds **Inspect prompt before sending** to the settings (⚙) menu of the story editor, in the *Analysis* group. It is a check
box item: while it is ticked, every generation of yours (a new part, a swipe, a regeneration) stops just before the
request, after every other extension has finished changing the prompt, and opens a dialog **Send this prompt to the model?** with the
exact messages that would be sent - the system message, the story parts as the model sees them, and the instructions of
the turn - each under its role, in a scrollable, resizable text. **Send** sends the request, **Cancel** stops the generation;
the part that was started for it is removed again, as when a generation fails before any text arrives.

## Usage

Nothing to configure. Install the plugin (see [Marginalia plugins](../README.md)), open a book and tick the item in the
settings menu. The switch is a setting of your user, shared by all your books and sessions; other users are not asked.

Notes:

- The prompt is built first, as for any generation, and you are asked as the last step before the request. If a summary
  became invalid, the usual question about continuing without it comes before the prompt is shown. No request is made to
  the model before you answer; *Cancel* only removes the part that was started.
- Only generations started from the story editor are held: an agent or a command has no page to show the prompt on, and its
  generations go on unasked.
- The text can be selected and copied. If you close the page while the dialog is open, that is a *Cancel*: the generation is
  stopped and nothing is sent, as soon as the server notices that the page is gone (that can take a while). *Stop* in
  another window of the same book does the same at once.

## For developers

The plugin listens to `Events.BEFORE_INFERENCE` and opens its dialog through `GenerationControllerEvent.accessUI`, the call that
runs an action bound to the UI that started the generation (`ui.access` and a push). See [Generation events](../../../manual/developer/plugins/generation-events.md).

`GenerationQuestion` is the part that can be reused: it asks through `accessUI`, releases the generation exactly once by
whatever answers first (*Send*, *Cancel*, a stop from elsewhere, the dialog going away), and has tests with a fake dialog and
page (`GenerationQuestionTest`; `mvn test` in this folder after `mvn install` of the application). The walkthrough is in
[Plugin development](../../../manual/developer/plugins/index.md#asking-the-user-in-the-middle-of-a-generation).
