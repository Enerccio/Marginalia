# Image Generator

Makes an image for a part of the story. The model writes the image prompt from the part, you edit it, a backend draws the
picture, you reroll until it fits and accept it - it becomes an image of the part, like one added by hand with *Images* in the
part menu.

## Usage

1. Add a backend in *Settings → Extension Settings → Image Generator Settings* (see below) and save the settings.
2. Choose **Generate image** in the menu of a part. Nothing runs when the window opens: **Write the prompt with the model** has the
   model of the window (by default the book's) write the prompt of the part; change it as you like, or write it yourself. **Send lorebook entries**
   sends the entries the part activates (filters matched against its text, instructions, scene setting, point of view, characters
   present and your details; as many as fit the context), **Details** is what you want in the picture.
3. **Generate** makes the image from the prompt with the **Style** in front and the **Negative prompt**, **Reroll** another one (a new
   seed). **Accept** adds the image to the part, **Discard** drops it. Writing the prompt again after an image first ends the image
   session (its *after* requests, so a shared GPU is free for the language model).

**Style** and **Negative prompt** are inherited settings (`imagegenerator.style`, `imagegenerator.negativePrompt`): a book or a folder
sets them in *Prompts → Extension Settings*, else the user's panel, else none. The window starts with the book's value and can change it
for itself. The instruction of the prompt writer belongs to the backend, its **Prompt form** (`PromptForm`): *Tags*, *Descriptive
sentences* (both built in) or *Free text*; a backend saved before the choice keeps the user's old instruction (`BackendConfig.instructionFor`).

## Backends

| Type | What it talks to |
|---|---|
| **ComfyUI** | A ComfyUI server. Paste a workflow saved with *Save (API Format)* and write `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` and `{{height}}` where they go (`"seed": {{seed}}` and `"seed": "{{seed}}"` both work). The workflow is posted to `/prompt`, `/history/<id>` is polled and the image is fetched from `/view`. **Abort** interrupts the job. `{{lora}}` (the file) and `{{lora_strength}}` (a number) in a LoRA loader make the LoRA selectable in the window: the list is `GET /models/loras` (remembered per backend as `knownLoras`, offered while the server is not running and read again after an image), the choice is remembered per backend, and with none chosen every node whose input is exactly `{{lora}}` is taken out of the graph (its users get its `model` input for output 0, its `clip` input for output 1). |
| **OpenAI images API** | `POST <address>/v1/images/generations` (an address that ends with `/v1` is used as it is). Answers with `b64_json` or an `url` are both accepted. Also served by stable-diffusion.cpp, LiteLLM and many hosted services; the api key is sent as a bearer token. |

The api key and the headers of the hooks are stored encrypted. A backend marked **The server is on this machine or on my network**
may use local addresses; a backend that is not refuses them (and so do its hooks and image links): loopback, private, link-local,
shared (100.64.0.0/10) and unique-local IPv6 addresses. Redirects are never followed and an answer is read only up to a limit.
The HTTP code (the call, the guard, the hooks) is the library [`http-hooks`](../../libs/http-hooks/README.md).

## One GPU for the model and the images

With one small GPU the language model and the image model cannot be loaded together. The backend names a **GPU lock** of the application
(`GpuService`, set up by the administrator in Admin → GPUs; `GpuLockSelect` in the settings): every image takes the lock for its own length
(`ImageJob`), and the lock's steps free the GPU of the language model and load the image server when the GPU holds something else. Nothing
is held between images: a window that is closed, folded or left open keeps no GPU, and the GPU stays loaded with the image server until a
job of another lock needs it.

A backend can also have its own requests around each image, inside the lock: **Before each image** (a list made in order, the first failure
stops the image) and **After each image** (all of them are made, a failure is logged). Every request without an `Authorization` header of
its own gets `Authorization: Bearer` with the backend's **Key of the requests around the images** (empty: the api key; `HookKeys.withBearer`
of `http-hooks`, shared with the Audiobook). The window keeps what it used for a part (details, style, negative prompt, prompt, size) in the
part's attributes under the plugin's key (`PartImageValues`) and opens with it again.

## Not done

Backends are per user, not per book.
