---
label: Setting up the speech server
order: 28
---

# Setting up the speech server for the Audiobook

The [Audiobook](audiobook.md) extension needs a **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, MIT licence) that it can reach over HTTP. This page is the server side. What is on it was tried on one machine (an AMD Radeon AI PRO R9700
with 32 GB, LiteLLM in front of everything, `llama-server` in router mode); your hardware and proxy may need other values.

## Which setup is yours?

| Your situation | What to do |
|---|---|
| The speech server runs on **another machine**, on the CPU, or on a card with room for it **next to the language model** | Run the server, put its address into a backend, **leave the requests before and after a batch empty**. Nothing else is needed. |
| The speech server and the language model **share one card** and do not fit together | You need the requests before and after a batch: **before** frees the card and starts the speech server, **after** stops it. The rest of this page. |
| The language model is a cloud model | Nothing to free: run the speech server where you like and leave the requests empty. |

So: **yes, pre and post requests are needed only when the two models take turns on one GPU.** They are what makes the turns: the plugin makes
them once around a *batch* (everything you started with one click), not around every part.

## 1. The speech server

Run Chatterbox-TTS-Server in a container (or from source) so that `GET /api/model-info` answers. Things that matter:

- **The model.** Turbo (`chatterbox-turbo`) is the default of the extension and the best sounding in the tests; the multilingual model is needed only for text in another language than the book's. The model is set in the server's `config.yaml`
  (or `POST /save_settings` and `POST /restart_server`). The *Speech model* of the backend must be the model the server has loaded after the requests before a batch.
- **Japanese needs `pykakasi` in the image.** Without it the server logs *pykakasi not available - Japanese text processing skipped* and reads kanji wrongly. Add `RUN python3 -m pip install --no-cache-dir pykakasi` to its Dockerfile (before `COPY . .`). The same silent skip exists for Hebrew, Russian and Chinese packages.
- **Voices** are files in the server's `reference_audio/` folder (mount it, so they survive). The extension uploads the clips of your actors itself (`POST /upload_reference`) before a batch, under a name made from the hash of the clip. The server has no delete call: clips pile up there.
- The server has **no authentication**. Do not publish its port; put a proxy in front of it if it is not on a private network.
- `restart: "no"` for a container that something else starts on demand. `restart: unless-stopped` starts it at boot and takes the card for nothing.

In the backend (*Settings → Extension Settings → Audiobook Settings*) put its address, tick *The server is on this machine or on my network* if it is, press **Test connection**: *the server answers, the model is loaded* is what you want.
The extension speaks to `/tts` (the full set of parameters: voice, language, exaggeration, CFG weight, temperature, speed, seed) and `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. If a proxy is between you and the server, it has to pass all of these.

## Higgs Audio v3 in ComfyUI

A backend with the speech model **Higgs Audio v3 in ComfyUI** talks to a [ComfyUI](https://github.com/comfyanonymous/ComfyUI) with the nodes of the
[TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite), not to Chatterbox-TTS-Server. Its address is the one of ComfyUI (for example
`http://gpu-server:8188`). It was set up on the same AMD R9700 in the `yanwk/comfyui-boot:rocm` image.

1. **Install the suite** into the `custom_nodes` folder of ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). Its Python packages go into the
   container. To keep them when the container is recreated, give pip a folder on a volume: add `PIP_USER=true` to the environment of the container and mount
   `./storage/local:/root/.local`.
2. **Never let pip replace the torch of the image** (a CUDA build would replace the ROCm one). Write the installed versions into a file and install with it as a
   constraint:
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   Do not run the suite's `install.py` as a whole: it may reinstall torch. `librosa` is not in its `requirements.txt` and the suite does not load without it.
3. **The model**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (about 8 GB), or let the
   suite download it the first time. It needs about 8 GB of the graphics card.
4. **Test connection** in the backend says *the server answers* with the model of the node when ComfyUI and the node are there.

What the extension does with it: it uploads the clips of the cast into the input folder of ComfyUI (`POST /upload/image`, under a name made from the clip), lists
the voices of the suite and the clips with `GET /object_info`, posts a workflow of its own to `/prompt` for every piece, waits on `/history` and fetches the audio
from `/view`: with ffmpeg on the Marginalia side as flac, which it trims after the last word, fades out and stores as mp3 or wav; without ffmpeg as the mp3 of ComfyUI, untrimmed (wav storage then does not work). **After every piece it calls `POST /free` with
`{"free_memory": true}`**: on the R9700 the third piece in a row on the loaded model crashed the graphics card (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI exits);
emptying the memory between pieces keeps the model loaded and avoids it, for about 3 seconds a piece. ComfyUI is started and stopped around a batch with the
same kind of requests as the speech server ([the requests](#the-requests)).

The **Fish Audio S2 Pro** engine of the suite did not work on that card: its audio came out as invalid numbers (NaN). The Higgs Audio v3 weights are under a
research and non-commercial licence.

## One GPU for the model and the speech

A card that holds the language model has no room for the speech model. The plan is the same for any proxy:

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**The order is not negotiable.** Starting the speech container while the language model holds the card filled the host's memory in the test
(30 GB of RAM, the swap, then the OOM killer took other services): the unload **must come first**, and the first failing request ends the
list, so nothing starts on a full card. The requests *after* a batch all run, whatever happens to one of them.

Why *stop* the container instead of unloading the model inside the server: an unload inside the server left about 1 GB of the card taken after
many model swaps; stopping the container frees everything.

### The requests

Each request of the lists is a method, an address, headers, a body and a wait afterwards (seconds). Do not write the key into the headers (the form shows headers as typed): put it into **Key of the requests around a batch**, or leave that empty and the *Api key* of the backend is sent. It goes out as `Authorization: Bearer` with every request below that has none of its own. With a LiteLLM
in front of everything and one key, the lists are:

| | Method and address | Headers | Body | Does |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Unloads the language model; answered by the hook itself, takes 1 to 2 s. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | Starts the speech container and answers when the server is ready (about 13 s). |
| Before 3 (only if the server starts without a model) | `POST http://litellm:4000/gpu/tts/load` | | | Loads the model (8 to 14 s, 50 s the first time it is downloaded). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Stops the container (under a second). |

For a **Higgs** backend *Before 2* is `POST http://litellm:4000/gpu/comfy/start` (it answers when ComfyUI is up, about 15 s; the model is loaded with the first
piece), there is no *Before 3*, and *After 1* is `POST http://litellm:4000/gpu/comfy/stop`. The two pass-throughs are in the README of `tools/gpu-control`.

The language model comes back by itself: the next ordinary request makes the router load it (about a minute for a big model; with the
[prompt cache hook](../prompt-cache.md) the cache of your session is restored), so no request after the batch is needed for it.

Without LiteLLM the same requests go to whatever starts and stops your containers; the only rule is the order above.

### What answers those addresses

- **`X-Llama-Unload`** is answered by the hook `gpu_unload.py` of LiteLLM, described with its code in
  [Freeing the GPU for another model](../prompt-cache.md#freeing-the-gpu-for-another-model) (it works without the cache hook). With a route `openai/*` the model name in the body must start with `openai/`: another name is answered 400 *after* the models were unloaded.
- **`/gpu/tts/start`, `/stop`, `/load`** are LiteLLM **pass-through endpoints** to the small controller `tools/gpu-control` of the repository, which starts and stops containers by name. Its
  README has the compose file with the **Docker socket proxy** (so the controller can start and stop containers and do nothing else; the socket itself is root on the host) and the settings that are easy to get wrong: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

  ```yaml
  general_settings:
    pass_through_endpoints:
      - path: "/gpu/tts/start"
        target: "http://gpu-control:8000/chatterbox/start"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/stop"
        target: "http://gpu-control:8000/chatterbox/stop"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/load"
        target: "http://chatterbox:8004/restart_server"
  ```

  (the container is named `chatterbox` and `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` tells the controller what to wait for; the controller's token stays in its `.env` and in LiteLLM's environment, Marginalia only ever has the key of LiteLLM).
- **All the containers and LiteLLM share a Docker network** (`docker network create ai`, once). Put `networks: [default, ai]` and the external network `ai` into each compose file: a `docker network connect` done by hand is lost when a container is recreated.

## Checking it

1. `curl http://<speech server>/api/model-info` answers (`loaded` true after a start).
2. *Test connection* in the backend says the model is loaded. If the requests before a batch are the only thing that starts the server, test it with the lists in place: *the server answers, no model is loaded* is the answer of a stopped-and-started one that still has to load.
3. Convert one short part in **Convert to audio**. The window shows the pieces being made; afterwards the speech container should be stopped and `nvidia-smi` / `nvtop` / `rocm-smi` show the card free again.
4. If the first call after a model load takes long, that is the engine warming up (once, 28 s in the first run, not seen again).

## Troubleshooting

| Symptom | Cause |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | The server started empty (a start wrapper that unloads the model once): add the request **Before 3**. |
| The first pieces are made, then the server says *Model not loaded* and its log shows `Request received for /api/unload` | Something other than Marginalia unloads the model (the extension never calls `/api/unload`): typically a startup wrapper in the container that waits for the server to answer and then unloads the model "to keep boot cheap". Its wait can end only after the first piece, because the single-threaded server was busy, and the unload then lands in the middle of the batch (a `urllib` timeout traceback in the container log is that wrapper waiting). The pattern to look for in the compose file is a `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Replace it with `command: ["python3", "server.py"]`: a start alone gives a loaded Turbo, and the unload only ever served to keep the card free after a boot, which a container that the controller starts and stops does not need. To tell who sent it, read the log of the extension (*Admin → Extensions → Log*): it lists every request before and after a batch with its address and the time it was answered, and when the pieces start and end. An unload that comes while the log says the batch is running, or from an address that is not in the list, is not the extension's. |
| The batch stops in the middle (the log of the extension says *Cannot reach*, or the server's log just ends) and a restart by hand works | The server died, typically killed by the system for taking all the memory (`dmesg \| grep -i oom` on the host: a `python3` process of many GB). The extension then restarts it itself (the requests after the batch, then those before it) and makes the piece again once; when the server cannot be brought back, or is gone again at once, the rest of the batch fails at once and the log says why. To keep a leaking server from getting that far set **Restart the server after (pieces)** in the backend (for example 100), and a `mem_limit` on the container so that the system kills only it. |
| The conversion fails at once with the answer of the proxy | The first request before the batch failed (a wrong key, the unload route not found); the list stops there on purpose. |
| The host runs out of memory when the batch starts | The language model was still on the card: the unload request is missing or is not first. |
| Japanese is read as nonsense | `pykakasi` is missing in the image of the server; the backend must also be the multilingual one for that piece. |
| The upload of a clip fails behind a proxy | The proxy has to pass a multipart body (`/upload_reference`) unchanged. Not tested through LiteLLM pass-through; use a voice file of the server (`predefined:Name.wav`) until it works. |
| Another user's batch unloads the model under yours | Batches on one server address share a gate inside Marginalia, and a batch for another speech model waits until the others are done. A speech server and, say, an image server on one card need the same name in **GPU (name)** on both backends to wait for each other (the Image Generator's too). Two Marginalia installations on one card do not share a gate. |
