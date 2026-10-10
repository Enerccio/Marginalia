---
label: Home
icon: home
order: 1000
---

# Marginalia

Marginalia is a self-hosted writing tool for long-form fiction co-written with large language models. You write the
instructions for each part of the story, the model writes the prose, and Marginalia keeps everything around it in
order: the branching story, the world lore, the summaries that keep long books within the model's context, and the
prompt settings that shape the style.

It runs in your browser - as a desktop app on your own computer, or as a small server for you and your friends - and
works with any OpenAI-compatible API: OpenAI, OpenRouter, LiteLLM, llama.cpp, LM Studio, Ollama, vLLM, and others.

![The story editor](images/hero-story-editor.png)

[!button variant="primary" text="Get started"](user/getting-started/index.md)
[!button text="Download"](https://github.com/Enerccio/Marginalia/releases)
[!button text="Source on GitHub"](https://github.com/Enerccio/Marginalia)

## What it does

- **Books with branching stories** – regenerate or swipe a part, branch from any point, move around the story tree,
  edit the generated text. See [Books](user/books/index.md).
- **Lorebooks** – world and character notes put into the prompt only when they matter: by book tags, text or regex
  filters, nested lorebooks. SillyTavern world info can be imported. See [Lorebooks](user/lorebooks/index.md).
- **Templates and macros** - prompts are Handlebars templates with SillyTavern-compatible macros, so imported lore
  keeps working. See [Templates & macros](user/templates/index.md).
- **Summaries** – older parts are condensed so long books fit into the context window. See
  [Summaries](user/books/summaries.md).
- **Any OpenAI-compatible model** - including reasoning models, with per-model limits and switchable generation
  settings. See [Inference providers](user/inference-providers.md) and [Protocols](user/protocols.md).
- **Backups** - per-book backups with restore and copy, and full database backups on a schedule. See
  [Book backups](user/books/backups.md) and [Database backups](user/administration/database-backups.md).
- **Multiple users** – each with their own books, lore, and models; administrators manage users, backups, and cleanup.
  See [Administration](user/administration/index.md).
- **Published books** – a read-only reading view you can share. See [Publishing & viewer](user/books/publishing.md).
- **Extensions** - plugins add features at runtime: chapter markers, lorebook history, AI reviews, side questions to
  the model. See [Extensions](user/extensions/index.md).

## Which guide do you need?

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[User guide](user/index.md)**           | You want to install Marginalia, run it for others, or write with it. Start with [Getting started](user/getting-started/index.md). |
| **[Developer guide](developer/index.md)** | You want to build Marginalia from source, change it, write an extension, or understand how it works inside.                       |

## Quick links

| I want to...                   |                                                          |
|--------------------------------|----------------------------------------------------------|
| Install it on my computer      | [Desktop app](user/getting-started/desktop-app.md)       |
| Run it on a server             | [Server (Docker)](user/getting-started/docker-server.md) |
| Connect a model                | [Inference providers](user/inference-providers.md)       |
| Write the first part of a book | [Quick start](user/getting-started/quick-start.md)       |
| Use my SillyTavern lorebooks   | [Import & export](user/lorebooks/import-export.md)       |
| Fix a problem                  | [Troubleshooting & FAQ](user/troubleshooting.md)         |
| Write an extension             | [Plugin development](developer/plugins/index.md)         |
| Report a bug or contribute     | [Contributing](developer/contributing.md)                |

## About

Marginalia is open source under the [MIT License](https://github.com/Enerccio/Marginalia/blob/master/LICENSE).
Pictures used in the screenshots of this manual are credited on the [Credits](credits.md) page.
Releases are on [GitHub](https://github.com/Enerccio/Marginalia/releases); bugs and ideas go to the
[issue tracker](https://github.com/Enerccio/Marginalia/issues).
