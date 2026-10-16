# Google Gemini

Google's Gemini models as an inference provider, over the [Google Gen AI Java SDK](https://github.com/googleapis/java-genai)
instead of an OpenAI-compatible endpoint.

Once the plugin is loaded, the **Type** list of the inference provider dialog offers **Extension**, and the **Provider**
list of the *Per Type Settings* tab **Google Gemini**. The settings are the API key of [Google AI Studio](https://aistudio.google.com/apikey)
(stored encrypted), the model (with a **Refresh** that lists the models the key can use), an optional URL for a proxy, the
timeout, the retries and a switch for the adjustable safety filters (`BLOCK_NONE`). The reasoning settings of the dialog are
used: the summaries of the thinking arrive as the reasoning of the story part, the effort is a thinking level for `gemini-3*`
models and a token budget (1 024 / 8 192 / 24 576) for the others.

Streaming, stopping, JSON schemas (`infer`), functions the model can call, attachments, limits of shared AIs and usage
counting work as with the OpenAI-compatible provider. Usage is what Google reports; the counts shown in the story are the
approximate ones of the application. Vertex AI is not supported, only API keys.

The AIs of the provider stay in the database when the plugin is unloaded and work again when it is loaded; while it is not
loaded, generating with them says to load the extension.

## For developers

The plugin is the example of `ExtensionAIFactory` (see *Inference providers from a plugin* in the developer manual):
`GoogleGenAiFactory` is registered with `InferenceServices` on load and unregistered on unload, `GoogleGenAiForm` is the
settings tab of the AI dialog, `GoogleGenAiInferenceService` the service. The SDK and the libraries only it needs are embedded
in the JAR (`Embed-Dependency`, about 14 MB); what the application ships (OkHttp, Jackson, Kotlin, Gson) is excluded and
used from the application, which the bundle sees first through boot delegation.

```sh
mvn package        # target/google-genai-1.3.0.jar, runs the unit tests of the messages, errors and settings
```
