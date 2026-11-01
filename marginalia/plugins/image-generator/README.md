# Image Generator

Makes an image for a part of the story. The model writes the image prompt from the part, you edit it, a backend draws the
picture, you reroll until it fits and accept it - it becomes an image of the part, like one added by hand with *Images* in the
part menu.

## Usage

1. Add a backend in *Settings → Extension Settings → Image Generator Settings* (see below) and save the settings.
2. Choose **Generate image** in the menu of a part. The model of the window (by default the book's) writes the prompt of the part;
   change it as you like, or write it yourself. **Write the prompt with the model** writes it again. **Send lorebook entries**
   sends the entries the part activates (filters matched against its text, instructions, scene setting, point of view, characters
   present and your details; as many as fit the context), **Details** is what you want in the picture.
3. **Generate** makes the image, **Reroll** another one (a new seed). **Accept** adds the image to the part, **Discard** drops it.

## Backends

| Type | What it talks to |
|---|---|
| **ComfyUI** | A ComfyUI server. Paste a workflow saved with *Save (API Format)* and write `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` and `{{height}}` where they go (`"seed": {{seed}}` and `"seed": "{{seed}}"` both work). The workflow is posted to `/prompt`, `/history/<id>` is polled and the image is fetched from `/view`. **Abort** interrupts the job. |
| **OpenAI images API** | `POST <address>/v1/images/generations` (an address that ends with `/v1` is used as it is). Answers with `b64_json` or an `url` are both accepted. Also served by stable-diffusion.cpp, LiteLLM and many hosted services; the api key is sent as a bearer token. |

The api key is stored encrypted. A backend marked **The server is on this machine or on my network** may use local addresses;
a backend that is not refuses them (and so do its hooks and image links).

## One GPU for the model and the images

With one small GPU the language model and the image model cannot be loaded together. The plugin does not manage the memory, it
makes requests you configure around the images of a window:

- **Before the first image** - a request made once when the window starts working with the backend (for example a call to your
  proxy that unloads the language model and loads the image model).
- **After the last image** - a request made when you accept, discard, close the window, or when **Release after idle** seconds
  passed since the last image (so a forgotten window doesn't keep the GPU). The ComfyUI example: `POST <address>/free` with the body
  `{"unload_models": true, "free_memory": true}`.

The backend stays held while you reroll, so only the first image pays for the swap. If a proxy in front of the models swaps them
by the model name, leave both empty.

## Not done

Backends are per user, not per book.
