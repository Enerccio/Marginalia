# Gemma 4 tools fix

Sends the results of function calls to Gemma 4 models with the role `tool_responses`.

Some (old) APIs of Gemma 4 expect that role for the messages with the result of a function call instead of the `tool` of the
OpenAI chat API. When the model of the AI contains `gemma4` or `gemma-4` (any case), the extension changes the role of
every tool message in the requests to that AI. Requests to other models are not touched.

A port of the SillyTavern extension
[SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

## You probably do not need it

With a current Gemma 4 chat template the plugin is not needed, and it makes the prompt worse. Google's template handles
tool results sent with the role `tool` (the role Marginalia uses) since its fix of 2026-04-10; only the release template
of 2026-04-02 did not, and turned such a message into a stray turn, so the model could invent the result. No version of
the template knows a message whose *role* is `tool_responses`: with a current template the plugin renders an extra
`<|turn>tool_responses` turn after the call. llama.cpp also drops the old `tool_responses` field of the assistant
message, so on llama.cpp `tool` is the only working route.

A GGUF keeps the template it was converted with, so an old Gemma 4 GGUF may still need the plugin. To check, render a
call and its result through llama.cpp's `/apply-template` and look for `<|tool_response>` inside the model turn: if it
is there, the template handles `tool` and the plugin should not be loaded.

For models that write a call as text instead of making it, see the *Function call rule* of the model settings instead.

## Usage

Nothing to configure. Install the plugin (see [Marginalia plugins](../README.md)); it applies to the next request.

## For developers

The plugin is the smallest user of `InferenceServices.addToolRoleResolver`: a `ToolRoleResolver` answers the role for
an AI (or `null` to leave it), and is removed again when the extension is unloaded. Any other model that wants a
different name for tool messages can be handled the same way.
