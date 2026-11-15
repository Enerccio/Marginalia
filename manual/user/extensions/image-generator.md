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
| GPU lock | The [GPU lock](../administration/gpus.md) of the GPU the server runs on, when it shares it with the language model or other servers; see [One GPU](#one-gpu-for-the-model-and-the-images). Shown only when the administrator set up GPU locks. |

![A backend in the settings](../../images/plugin-imagegenerator-settings.png)

**Test connection** checks the address and the key.

The panel also has two texts that a book can change:

- **Style** - put in front of every prompt: the look of the pictures, for example *watercolor, soft light*. The prompt writer is
  told it too, so the prompt does not describe another style.
- **Negative prompt** - what the pictures must not show. A turbo model that runs without guidance (CFG 1) ignores it. The negative
  prompt a backend had before is taken over here the first time you open the panel.

What you set in the panel is your default. A book (its *Prompts* tab) or a [folder](../books/folders.md) can set its own under
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

With one small GPU, the language model and the image model cannot be loaded together. The administrator sets this up once, for
every model and server on that GPU, under [Admin → GPUs](../administration/gpus.md): a **GPU lock** for the language model and one for
the image server, each with the requests that load it and free the GPU of it. Pick the image server's lock in **GPU lock** of the
backend (and the language model's lock in its [AI settings](../inference-providers.md)).

Every image then takes its turn on the GPU:

- When the GPU is loaded with the image server already, the image starts at once; nothing is unloaded between rerolls.
- When it holds the language model (or another server), the image waits until the model has answered, the model's lock frees the GPU,
  and the image server's lock loads it.
- Nothing is held between the images: closing or folding the window, or leaving it open, keeps no GPU. The next request to the
  language model frees the GPU of the image server by itself, and the image server stays loaded until then.

The window shows *Generating the image* while it waits for its turn.

**Requests before and after each image.** A backend can also have its own two lists of requests, made inside the lock around every
image: **Before each image** and **After each image**. Freeing the GPU belongs to the GPU lock; use these for anything else the
server needs around an image. Each list is made one request after the other (**Add a request** adds one, the bin removes one). Each
request has an address, a method (`POST` by default), headers (`Name: value`, one per line), a body, a **timeout** (empty: 60 seconds,
at most 600) and **Wait afterwards** (seconds to pause after the answer). The headers are stored encrypted, like the api key.

When a request of the *before* list fails, the rest is not made and the image stops (the message says which request failed: *Request 2
of 3: ...*). The *after* list is made **completely** whatever happens to one of its requests, and a failure is written to the log of
the extension; the image is kept. A backend saved with the single request of an earlier version shows it as the first request of the
list.

Backends saved before GPU locks existed lose their **GPU (name)** and **Release after idle** settings: pick a GPU lock instead, and move
requests that freed the GPU (unloading the language model, `POST <address>/free` of ComfyUI) into the steps of the locks.

## What is not done

- Backends are per user, not per book.
