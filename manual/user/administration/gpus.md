---
label: GPUs
order: 84
---

# GPUs

A graphics card often holds one model at a time: the language model, or the image model, or the speech model. The **GPUs** tab of the
Admin page lets the models and servers on one card take turns on it, so that an image or an audiobook does not start while the language
model fills the card, and the language model comes back when it is needed.

You set it up once, as the administrator; every user then picks a lock for their AI or their backends.

## What it is made of

| | |
|---|---|
| **GPU** | One card (or any machine that holds one model at a time). Only its name. |
| **GPU lock** | One way to use a GPU: what is loaded on it. For example *Language model*, *ComfyUI* and *Speech* on the same GPU. A lock has two ordered lists of steps: the **acquire steps** load it (start a container, load a model, unload whatever must go first), the **release steps** free the card of it (stop the container, unload the model). **Jobs at once** limits how many jobs of the lock run together (an image server that makes one image at a time: 1; 0 for no limit). |
| **GPU step** | One HTTP request: an address, a method, headers (`Name: value`, one per line, stored encrypted, so a bearer key can be written there), a body, a timeout (empty: 60 seconds) and a wait after the answer. A step can be used by several locks, for example one *unload the language model* step that both the image and the speech lock run first. **Test** makes the request at once. |

Pick the lock in:

- the [settings of an AI](../inference-providers.md) (**GPU lock** in the general settings), for the language model;
- a backend of the [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- a backend of the [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

The **GPU lock** box is shown only when at least one lock exists.

## How the turns work

- Every request to a model, every image and every audiobook batch or sample takes the lock of its AI or backend **for its own length**.
- When the GPU is already loaded with that lock, the job starts at once and no step runs.
- When it is loaded with another lock, the job waits until the jobs of that lock are done. Then the **release steps** of that lock run (all
  of them, also when one fails), then the **acquire steps** of the new lock (in order; the first failure stops them and the job fails with
  the message of the step).
- When a release step fails, the new lock is not loaded at all (the card may still be full): the job fails with the error and the GPU stays
  with the lock it had. The next job of another lock tries to free it again.
- After a job the GPU **stays loaded** with its lock. Nothing is unloaded on a timer: an image window that is left open, folded or closed
  keeps no GPU, and the image model stays loaded until the language model (or another lock) needs the card.
- Jobs wait in the order they came. Once a job waits for another lock, new jobs of the loaded lock wait behind it, so a busy language
  model cannot keep an image waiting for ever.
- An AI or backend without a lock, or with a lock that was deleted, does not wait for anything.

What a GPU holds is known only while the application runs: after a restart nothing is known to be loaded, and the first job runs only its
acquire steps.

## The tab

The tab lists the GPUs with the lock each is loaded with now (*Unknown* when nothing ran yet or it was unloaded) and how many jobs run
and wait on it, then the locks and the steps, each with buttons to add, edit and delete. **Refresh** reads what the GPUs hold again.

**Unload now** frees a GPU by hand, for example to use the card for something outside Marginalia: it waits for the running jobs, runs the
release steps of the loaded lock and leaves the GPU unknown (also when a step failed; the message says which). The next job loads its lock
again.

Deleting a GPU deletes its locks; deleting a step takes it out of the locks that run it. AIs and backends that named a deleted lock run
without one.

## An example: one card, three models

With `llama-server` behind LiteLLM and a [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) style
proxy that starts and stops containers:

| Step | Request |
|---|---|
| Unload the language model | `POST http://litellm:4000/v1/chat/completions` with the header `X-Llama-Unload: all` (see [Freeing the GPU for another model](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Start speech / Stop speech | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Lock | Acquire steps | Release steps | Jobs at once |
|---|---|---|---|
| Language model | (none: the router loads it with the next request) | (none) | 0 |
| ComfyUI | Unload the language model, Start ComfyUI | Stop ComfyUI | 1 |
| Speech | Unload the language model, Start speech | Stop speech | 1 |

The AI of the language model picks *Language model*, the image backend *ComfyUI*, the speech backend *Speech*. A batch of speech after a
chat runs *Unload the language model* and *Start speech*; the next chat message runs *Stop speech* first.
