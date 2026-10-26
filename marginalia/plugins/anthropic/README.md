# Anthropic Claude

Anthropic's Claude models as an inference provider, over the [Anthropic Java SDK](https://github.com/anthropics/anthropic-sdk-java)
instead of an OpenAI-compatible endpoint.

**Your story text goes to Anthropic.** Every request of an AI that uses this provider (the prompt with the story, the lorebook
entries and the summaries, the answers of the model, the files and pictures an agent or a command sends) is sent to Anthropic's
servers (`api.anthropic.com`, or the proxy you set), and Anthropic's terms and privacy policy for the API decide what happens to
it there (retention, safeguards that may decline a request). The other providers send to the server you chose, which may be your
own. Do not use this provider for text you may not send to Anthropic. On a server shared with other users, an administrator who
shares an AI of this provider sends the story text of everybody who uses it to Anthropic.

Once the plugin is loaded, the **Type** list of the inference provider dialog offers **Extension**, and the **Provider**
list of the *Per Type Settings* tab **Anthropic Claude**. The settings are the API key of the
[Claude Console](https://platform.claude.com) (stored encrypted), the model (with a **Refresh** that lists the models the key can
use), an optional URL for a proxy, the timeout and the retries. The reasoning settings of the dialog are used: with reasoning on,
the summaries of the thinking arrive as the reasoning of the story part. Claude 4.6 and later think adaptively and the
**Reasoning Effort** is the effort of the model (low, medium, high); the earlier models think with a budget of tokens (1 024 /
8 192 / 24 576). With reasoning off nothing is sent, and the models that cannot think without it (Opus 5.5) think as they do by
default, without showing it.

Streaming, stopping, JSON schemas (`infer`), functions the model can call, pictures, PDF and text files as attachments, limits of
shared AIs and usage counting work as with the OpenAI-compatible provider. Usage is what Anthropic reports (the tokens read from
and written to the cache count as prompt); the tokens of the texts shown in the story are counted by Anthropic's `count_tokens`
and fall back to the local estimate. Not sent: temperature, top p and the penalties of the protocol, which the current API
refuses (the response size is used). Not supported: Amazon Bedrock, Google Vertex AI and Microsoft Foundry (only API keys of the
Claude API), prompt caching, the server-side tools of Anthropic (web search...).

The AIs of the provider stay in the database when the plugin is unloaded and work again when it is loaded; while it is not
loaded, generating with them says to load the extension.

## For developers

The plugin follows `google-genai` (see *Inference providers from a plugin* in the developer manual): `AnthropicFactory` is
registered with `InferenceServices` on load and unregistered on unload, `AnthropicForm` is the settings tab of the AI dialog,
`AnthropicInferenceService` the service. The SDK and the libraries only it needs are embedded in the JAR (`Embed-Dependency`,
about 36 MB, most of it the SDK's model classes); what the application ships (OkHttp, Jackson, Kotlin, the JSON schema generator)
is excluded and used from the application, which the bundle sees first through boot delegation.

Three things worth knowing:

- Every request is streamed, also the ones whose answer is handed over whole; a stream is stopped by closing it, which the
  SDK turns into cancelling the HTTP call.
- A signed thinking block must go back with the tool calls of its answer. Inside a request the service sends the whole
  answer back; the transcript of an agent keeps only the calls, so the blocks travel in the signature of the first call
  (`AnthropicMessages.encodeThinking`).
- The conversation has to start and end with a user message (models after Claude 4.5 refuse a prefilled answer). The payload
  of the story always does; any other is refused with an `InferenceException` (bad request) instead of being changed.

```sh
mvn package        # target/anthropic-1.5.0.jar, runs the unit tests and the tests against a fake Messages API
```
