# xAI Grok

xAI's Grok models as an inference provider, over the xAI REST API (chat completions) instead of the generic OpenAI-compatible
provider. Nothing is embedded in the plugin: it uses the HTTP client and the JSON library the application already has.

**Your story text goes to xAI.** Every request of an AI that uses this provider (the prompt with the story, the lorebook
entries and the summaries, the answers of the model, the pictures an agent or a command sends) is sent to xAI's servers
(`api.x.ai`, or the proxy you set), and xAI's terms and privacy policy for the API decide what happens to it there. The other
providers send to the server you chose, which may be your own. Do not use this provider for text you may not send to xAI. On a
server shared with other users, an administrator who shares an AI of this provider sends the story text of everybody who uses it
to xAI.

Once the plugin is loaded, the **Type** list of the inference provider dialog offers **Extension**, and the **Provider** list of
the *Per Type Settings* tab **xAI Grok**. The settings are the API key of the [xAI Console](https://console.x.ai) (stored
encrypted), the model (with a **Refresh** that lists the models the key can use), an optional URL for a proxy (default
`https://api.x.ai/v1`), the timeout, the retries and whether the reasoning effort is sent.

With reasoning on, the reasoning the model shows (`reasoning_content`, as `grok-3-mini` does) arrives as the reasoning of the
story part. The **Reasoning Effort** is sent as `reasoning_effort` only to `grok-3-mini` (`low` for low, `high` otherwise), as the
other Grok models refuse the parameter; **Send the reasoning effort** can force it (*Always*, with the effort named as it is) or
turn it off (*Never*).

Streaming, stopping, JSON schemas (`infer`), functions the model can call, pictures (JPEG, PNG) and text files as attachments,
limits of shared AIs and usage counting work as with the OpenAI-compatible provider. Usage is what xAI reports: the reasoning
tokens, which xAI counts apart from the completion tokens and bills as generated ones, are added to the generated tokens. The
tokens of the texts shown in the story are counted by xAI's `tokenize-text` endpoint and fall back to the local estimate.
Not supported: the server-side tools of xAI (live search, code execution), image and video generation.
