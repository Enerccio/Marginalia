# Anthropic Claude

Adds Anthropic's Claude models as an [inference provider](../inference-providers.md), over Anthropic's own API instead of
an OpenAI-compatible one.

Claude is also reachable through aggregators such as OpenRouter with the normal *OpenAI Compatible* provider. This extension
talks to the Claude API directly (the Anthropic SDK), which gives the things a compatible endpoint doesn't: the **summaries
of the model's thinking** shown as reasoning, the effort of Claude 4.6 and later models, and the exact token counts Anthropic
reports and counts.

!!!warning Your story text goes to Anthropic
Every request of an AI that uses this provider - the prompt with the story, the lorebook entries and the summaries, the
answers of the model, the files and pictures an agent or a command sends - is sent to Anthropic's servers
(`api.anthropic.com`, or the proxy you set). Anthropic's terms and privacy policy for the API decide what happens to it there
(retention, safeguards that can decline a request). The other providers send to the server you chose, which may be your own.
Do not use this provider for text you may not send to Anthropic. On a server shared with other users, an administrator who
shares an AI of this provider sends the story text of everybody who uses it to Anthropic.
!!!

## Installing

Load the extension in *Admin → Extensions*, as described in [Managing extensions](../administration/extensions.md).
The JAR is large (about 36 MB): it contains Anthropic's SDK and the libraries only the SDK needs. Nothing else in
Marginalia uses them.

Once it is loaded, the **Type** list of the inference provider dialog has a new entry, **Extension**, and the **Provider**
list at the top of the *Per Type Settings* tab offers **Anthropic Claude** (it is chosen for you while it is the only one).
Without a loaded extension that provides AIs, **Extension** is not offered.

## Adding a Claude provider

1. Get an API key in the [Claude Console](https://platform.claude.com).
2. On the **Inference Providers** tab click **Add Inference Provider**, enter a **Name**, choose the type **Extension** and
   the provider **Anthropic Claude**.
3. Fill in the *General Settings* (context size, response size, [reasoning](#reasoning)) and the settings of the
   provider on the *Per Type Settings* tab, then click **Test Connection**.

| Field | |
|---|---|
| **Api Key** | The key of the Claude Console. It is stored encrypted and not shown again; when you edit the provider, click **Reset** to enter another one. |
| **Model** | The model to use, e.g. `claude-opus-5-5` or `claude-sonnet-5-5`. **Refresh** lists the models your key can use; you can type a name that is not in the list. |
| **URL** | Optional. Empty uses Anthropic's API (`api.anthropic.com`), set it to send the requests through a proxy. |
| **Timeout (seconds)** | How long to wait for Anthropic to answer, and for more text once it started writing. The default is 300. |
| **Retries** | How often a request is repeated when Anthropic answers with a rate limit (429) or a server error (5xx, also 529 *overloaded*), or cannot be reached. The default is 2, 0 turns retrying off. |

**Test Connection** lists the models and asks the chosen one for a few tokens, with the values in the dialog (before you
save them).

## Reasoning

The reasoning settings are those of the general settings of the provider, like for any provider:

- With **Enabled Reasoning** off nothing about thinking is sent. The models that think by default (Opus 5.5 can't be told
  not to) do, and their thinking is not shown; the others do not think.
- With it on, the model sends summaries of its thinking and they are shown as the reasoning of the story part. For Claude 4.6
  and later the **Reasoning Effort** is the effort of the model (low, medium, high) with adaptive thinking: the model decides
  how much to think. For the earlier models (Claude 4.5 and before, Haiku 4.5 among them) it is a budget of 1 024, 8 192 or
  24 576 thinking tokens. **None** is the same as off.

Thinking tokens count as generated tokens, and they are taken from the response size you set: if the model uses it all for
thinking, the story part comes back empty and the error says the response size was used up. Raise **Max Response Tokens**
or lower the effort. Claude needs a response size on every request: when neither the provider nor the protocol sets one,
16 000 is used.

## What works

Everything a story needs works as with an OpenAI-compatible provider: streaming, stopping a generation, summaries,
[extensions that call the model](index.md), functions the model can call (the
[Name generator](name-generator.md) offers some), and pictures (JPEG, PNG, GIF, WebP), PDF and text files attached to a
message. The tokens a user uses are counted in the **Tokens sent** and **Tokens generated** columns from the numbers
Anthropic reports (tokens read from the cache and written to it count as sent), and the
[limits](../inference-providers.md#limits-of-shared-providers) of a shared provider apply.

The token counts shown in the story (the size of a part, the prompt) are counted by Anthropic (the `count_tokens` request of
the model), once for each text: the answers are remembered. When Anthropic cannot be asked (no key, no network, an error),
Marginalia estimates the count itself for a minute and then tries again; a text that is saved or edited while another save is
going on is estimated too.

Two things differ from other providers:

- **Temperature, top p and the penalties of the protocol are not sent.** The current Claude API refuses them; the model
  samples with its own settings. The response size of the protocol is used.
- A conversation has to start and end with a message of the user, and Claude does not continue an answer. The prompt of a
  book always does; a request from an extension that does not is refused with a "bad request" error.

## When the extension is not loaded

The providers you created stay in the list when the extension is unloaded, with the type *Extension: anthropic*, and
work again when the extension is loaded. While it is not loaded, a book that uses such a provider can't generate: the error
tells you to load the extension or to choose another provider. The provider can still be opened and its general settings
changed; the Claude settings are only shown when the extension is loaded, and they are not touched.

## Not supported

- **Amazon Bedrock, Google Vertex AI and Microsoft Foundry.** Only API keys of the Claude API are supported.
- Prompt caching and the tools that run on Anthropic's servers (web search, code execution).
