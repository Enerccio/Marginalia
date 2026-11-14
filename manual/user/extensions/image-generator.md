---
label: Image Generator
order: 13
---

# Image Generator

Makes an image for a part of the story. The model writes the image prompt from the part, you edit it, an image backend draws
the picture, you reroll until it fits and accept it - it becomes an image of the part, like one you add by hand with *Images* in
the part menu (it is shown in the story, and in the exports).

The extension does not make images itself: it talks to a **ComfyUI** server or to a server with the **OpenAI images API** (also
spoken by stable-diffusion.cpp, LiteLLM and many hosted services).

## Adding a backend

*Settings → Extension Settings → Image Generator Settings* lists the backends. Add one (+), fill it in and save the *Settings* page.

| Setting | What it does |
|---|---|
| Name, Type | The name shown in the window. **ComfyUI** or **OpenAI images API**. |
| Address | The base address of the server, for example `http://localhost:8188` for ComfyUI. |
| Api key | Sent as a bearer token to the image server. It is stored encrypted and not shown again; type a new one to replace it, or **Remove the key**. |
| Key of the requests around the images | Sent as `Authorization: Bearer` with every request [before and after the images](#one-gpu-for-the-model-and-the-images) that has no `Authorization` header of its own, for example the key of a LiteLLM proxy. Stored encrypted. Empty: the api key is used. |
| Model | OpenAI images API only: the `model` of the request. |
| Workflow | ComfyUI only: a workflow saved in ComfyUI with **Save (API Format)**. Write `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` and `{{height}}` where they go, and `{{lora}}` / `{{lora_strength}}` in a LoRA loader to [choose the LoRA in the window](#choosing-a-lora). |
| The server is on this machine or on my network | Allows addresses of the local network. Leave it off for a server in the cloud: it then cannot be made to call your own machine. |
| Width, Height | What the window starts with (you can change them there for one image). |
| Prompt form | How the language model writes the prompt for this backend, to suit its image model: **Tags** (short comma separated phrases, for SDXL, Pony, Illustrious), **Descriptive sentences** (a paragraph, for Flux, Krea, Qwen-Image, Z-Image) or **Free text**, your own instruction for the prompt writer (it starts from the one that was chosen). A backend made before this setting existed writes tags, or uses the instruction you had set then as its free text. The built-in instructions are in your [prompt language](../account.md#prompt-language) and ask for the image prompt in English, which image models read best. |
| Timeout | How long to wait for one image. |
| Release after idle | See [One GPU](#one-gpu-for-the-model-and-the-images). |

![A backend in the settings](../../images/plugin-imagegenerator-settings.png)

**Test connection** checks the address and the key.

The panel also has two texts that a book can change:

- **Style** - put in front of every prompt: the look of the pictures, for example *watercolor, soft light*. The prompt writer is
  told it too, so the prompt does not describe another style.
- **Negative prompt** - what the pictures must not show. A turbo model that runs without guidance (CFG 1) ignores it. The negative
  prompt a backend had before is taken over here the first time you open the panel.

What you set in the panel is your default. A book (its *Prompts* tab) or a [folder](../folders.md) can set its own under
*Extension Settings → Image Generator*; a book uses its own, else the nearest folder's, else yours.

## Making an image

![Generate image in the menu of a part](../../images/plugin-imagegenerator-menu.png)

1. Choose **Generate image** in the menu of a part. Nothing is sent to a model yet: first fill in what the prompt writer should
   follow, then press **Write the prompt with the model** (or <kbd>Ctrl+Enter</kbd> in *Details*). The model of the window (the
   book's by default, the last choice is remembered) writes the prompt of the part; you can stop it with **Abort**, change the
   prompt, write it yourself, or write it again. These apply only to writing the prompt, not to the image itself:
   - **Send lorebook entries** - the entries of the book's lorebook (and its sub lorebooks) go to the model as in a generation, so
     it knows how the characters and places look. An entry is sent when its filters are found in the part: its text, instructions,
     scene setting, point of view and characters present, or in your *Details*. Entries that are always on come too. Only as many
     fit as the context of the model allows (a notice tells how many were left out). The choice is remembered.
   - **Details** - what the picture must show or how it must look (*at night, close up on her face, the red dress*); the prompt
     writer follows it. It is for this window only.
2. Choose the backend and press **Generate**. The image is made from the **Prompt** with the **Style** in front, and the
   **Negative prompt**; both start with the book's (or your default) and a change is for this window only (the style is also told
   to the prompt writer). The size starts with the backend's and can be changed for this window.
3. The image appears. **Reroll** makes another one with a new seed, **Discard** drops it, **Accept** adds it to the part and closes
   the window. **Abort** stops an image that is being made (ComfyUI is told to interrupt the job).

![The window with the prompt written by the model](../../images/plugin-imagegenerator-prompt.png)

![The image, with Accept, Discard and Reroll](../../images/plugin-imagegenerator.png)

The open part shows the new image under its text at once.

The window remembers what you used for each part: the details, style, negative prompt, prompt and size are kept with the part when the
prompt has been written and when you generate, and the next time you open the window on that part it starts with them. A part you never
made an image for starts with the book's style and negative prompt.

## Choosing a LoRA

A LoRA is a small add-on to the image model, usually for a style. With ComfyUI you can choose it in the window instead of saving the
workflow again for each one:

1. In the exported workflow, write `{{lora}}` as the `lora_name` of the LoRA loader (*Load LoRA*, `LoraLoaderModelOnly`, or
   `LoraLoader`), and `{{lora_strength}}` as its strength (`strength_model`; `strength_clip` too for `LoraLoader`).
2. The window then shows **LoRA** with the LoRA files of the server (its `models/loras` folder) and **LoRA strength**. You can
   also type the name of a file the list does not show.
3. **None** (an empty LoRA) takes the LoRA loaders out of the workflow, so the image is made without them.

The LoRA and the strength you generate with are remembered for the backend and are there the next time. So is the list: a server
that shares the GPU is often not running when the window opens (the requests before the first image start it), so the window offers
the LoRAs the server listed last and asks again after the first image. Only when it has never been able to ask does a notice say that
the LoRAs could not be read; you can still type the name of a file.

## One GPU for the model and the images

With one small GPU, the language model and the image model cannot be loaded together. The extension does not manage the memory;
each backend can have two lists of requests that you set up:

- **Before the first image** - made once, when the window starts to use the backend. For example a request to your proxy that
  unloads the language model and loads the image model.
- **After the last image** - made when you accept, discard or close the window, or when **Release after idle** seconds passed since
  the last image (so a forgotten window does not keep the GPU; 0 turns it off). For ComfyUI this can be `POST <address>/free` with the
  body `{"unload_models": true, "free_memory": true}`; for a proxy a request that loads the language model again.

With `llama-server` in router mode behind LiteLLM, the *before* request can unload the language model through the proxy: see
[Freeing the GPU for another model](../prompt-cache.md#freeing-the-gpu-for-another-model).

Writing the prompt again after an image (**Write the prompt with the model**) first lets the image backend go: the *after*
requests are made and the window waits for them, so the language model finds the GPU free. The next **Generate** makes the *before*
requests again.

Both moments take a **list of requests**, made one after the other (**Add a request** adds one, the bin removes one). A swap often needs
several: unload the language model, then start the image server, and after the images stop it. Each request has an address, a method
(`POST` by default), headers (`Name: value`, one per line), a body, a **timeout** (empty: 60 seconds, at most 600; give a server that
loads a big model more) and **Wait afterwards** (seconds to pause after the answer, for a server that is still busy after it said yes).
The headers are stored encrypted, like the api key (a request often carries `Authorization: Bearer ...`). The backend stays held while
you reroll, so only the first image pays for the swap. If a proxy in front of the models swaps them by the model name, leave both empty.

When a request of the *before* list fails, the rest is not made and the image stops (the message says which request failed: *Request 2
of 3: ...*), so that nothing is loaded on a GPU that is still full. The *after* list is made **completely** whatever happens to one of
its requests (every server must be stopped), and the first failure is shown as a warning. A backend saved with the single request of an
earlier version shows it as the first request of the list.

**Several people on one GPU.** Everyone whose backend has the same server address (`http://host:port`) goes through one gate: the
*before* request is made for the first window that starts and the *after* request when the last one is done, so closing one window
does not unload the model under the image of another, and the images are made one at a time (a second window waits its turn).
The requests are those of the user who comes first and last.

A backend can also name the GPU it runs on (**GPU (name)**). Backends that name the same GPU wait for each other even when their addresses differ, and that includes the backends of the [Audiobook](audiobook.md) (a speech server and an image server on one card): a user whose backend needs another model waits until the others are done and then runs their own requests, instead of joining a load that is not theirs. Leave it empty and only requests to the same address wait for each other.

## What is not done

- Backends are per user, not per book.
