---
label: Gemma 4 tools fix
order: 15
---

# Gemma 4 tools fix

Makes function calling work with Gemma 4 models whose API expects another role for the results of the calls.

!!!warning Not needed with a current chat template
Load it only for an old Gemma 4 model. Google's chat template of Gemma 4 reads the results with the role `tool`, the
one Marginalia sends, since April 2026 (every later GGUF has it). With such a template the extension makes the prompt
worse: the result arrives as an extra turn the template does not know. llama.cpp also ignores the old form, so there the
role `tool` is the only one that works.

An old GGUF keeps the template it was made with. To check yours, render a function call and its result with
llama.cpp's `/apply-template` and look for `<|tool_response>` in the model turn: if it is there, do not load the
extension. A model that writes its calls as text instead of making them is helped by the *Function call rule* of the
[model settings](../inference-providers.md) instead.
!!!

## How it works

When the model calls a function, Marginalia sends the result back to it in a message with the role `tool`. Some
servers that run Gemma 4 expect the role `tool_responses` instead, and answer an error or ignore the result otherwise.

The extension changes the role for such models: when the *Model* of an [inference provider](../inference-providers.md) contains `gemma4` or
`gemma-4` (in any letter case), the results of function calls to it are sent as `tool_responses`. Requests to other
models are not touched. It is a port of the SillyTavern extension
[SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

## Usage

There is nothing to configure. Load the extension in *Admin → Extensions*; it applies to the next request.
