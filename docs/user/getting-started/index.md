# Getting started

Marginalia runs in your browser. You can install it in two ways:

| | [Desktop app](desktop-app.md) | [Server (Docker)](docker-server.md) |
|---|---|---|
| Best for | One person on one computer | Several users, or access from other devices |
| Installation | Download and run, nothing else to install | Docker and Docker Compose |
| Address | `http://127.0.0.1:8765` (your computer only) | Port `8080`, normally behind a reverse proxy with HTTPS |
| Data | `~/.marginalia` | `./data` next to `docker-compose.yml` |

Both versions are the same application with the same features: books, lorebooks, users, backups and extensions.
To write from a phone, run the server version and open it in the phone's browser: see
[Using Marginalia on a phone](../phone.md).

Marginalia has no model of its own. It needs an **OpenAI-compatible API** to write text: a cloud service (OpenAI,
OpenRouter...) or a model running on your own computer (llama.cpp, LM Studio, Ollama, vLLM...). Have the API address
and an API key ready (a local server that doesn't need one works without a key, see [Adding a provider](../inference-providers.md#adding-a-provider)).

## Steps

1. Install Marginalia: [desktop app](desktop-app.md) or [server](docker-server.md).
2. [Create the administrator account](first-start.md) on the first start.
3. Follow the [quick start](quick-start.md): connect a model, create a protocol and write the first part of a book.
