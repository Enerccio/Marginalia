# Audiobook - TODO

Status: **working, not finished**: steps 1 to 8, 10, 11 and 12 below are built and were checked in the application and on the author's server (queue, player, labelling, cast, studio,
spoken form, automatic conversion, export, commands, manual); missing are the sound effects (9b), other engines (9) and the items listed as *Not done*
under each step. What stands below was tried on the author's own server on 2026-11-08 (an AMD R9700 Pro, LiteLLM in front
of everything); a line says **Decided** when it is, **Not tried** / **Open** when it is not. Numbers are one listener, one machine, few samples.

## Idea

Convert a book into audio part by part with a text-to-speech (TTS) server, a voice for the narrator and one for each character, stored with the parts and played in the
viewer. Two ways of working: an **automatic run** (the LLM labels a part, the TTS speaks it, over parts N to M) that makes a **rough draft**, and the **Audiobook Studio** in
which the final audiobook is made part by part, looking at exactly what is sent to the TTS and changing it by hand. Later: sound effects and ambience. The browser-voice reading is
the separate [`tts-reader`](../tts-reader) plugin (item 36 of the main TODO) and stays the fallback that needs no server.

## Decisions

- **The audio is saved as a `Resource`** (details to check under *Open*). **Decided.**
- **A separate plugin from `tts-reader`** (that one is browser only, no server state). **Decided.**
- **The TTS is a separate server**, not llama-server (mainline llama-server does not serve `/v1/audio/speech`). **Decided.** The engine is **Chatterbox** through
  **devnen/Chatterbox-TTS-Server** (FastAPI, MIT; it installs its engine from the fork `devnen/chatterbox-v2`; the upstream `resemble-ai/chatterbox` is a library, marked retired).
  **Turbo is the default model**; the multilingual model only for a piece in another language (section *Chatterbox*).
- **Everything goes through the LiteLLM of the user, with one key**: the language model, the speech route, and the control calls of the GPU (below). The plugin never talks to
  the TTS server or to ComfyUI directly. **Decided.**
- **The GPU is shared by requests to a URL of the user**, made by the plugin around a batch: the same `HttpHook` / `HookRunner` as the Image Generator, now **lists** of
  requests (done, step 1). **One batch = one swap**: all the LLM calls first (labelling), unload the LLM, all the speech, stop the speech server (and ComfyUI when used), the LLM
  loads again with the next request. **Decided.**
- **Backends are per user, like the AI providers** (a user may point an AI at any address already, the admin can ban the feature), and the administrator can **also share
  backends** that users just pick by name (URL, key and hooks hidden from them, an allowance per user like `AILimit`), with an admin switch "users may add their own backends" (on by
  default, as for AIs). The private-addresses flag stays a setting of the backend: it guards against a hostile *remote server*, not against the user. **Decided.**
- **One lock / queue per GPU, not per user session**: sessions of several users on one server address share a `GpuGate` (before hooks for the first, after hooks for the
  last, work one at a time), so the end of one window does not unload the model under the job of another. **Decided** (done in `http-hooks`).
- **The text of the book is split in code, never by the model**; the LLM only labels pieces (step 6). **The text sent to the TTS is a separate spoken form** (step 8). **The
  narrator is a role of its own**, not a character. **A seed is part of the cache key of a segment.** **Decided.**

## The setup that works (the author's server, all checked through LiteLLM with one key)

```
Marginalia -> LiteLLM (key) -> llama-server router      the language model (slot_cache.py + gpu_unload.py hooks of manual/user/prompt-cache.md)
                            -> chatterbox container     speech: /v1/audio/speech, /tts, voices in reference_audio/
                            -> gpu-control container    starts / stops containers (tools/gpu-control, behind a docker socket proxy)
                                 -> comfyui-amd         sound effects (Stable Audio 3 Small-SFX)
```
All the containers and LiteLLM share the Docker network `ai` (`docker network create ai`, once; **a manual `docker network connect` is lost when a container is recreated**: put
`networks: [default, ai]` and the external `ai` network in each compose file; example files: `tools/gpu-control/docker-compose.yml`, `comfyui.docker-compose.example.yml`).

| What | Request through LiteLLM (`Authorization: Bearer <key>`) | Measured |
|---|---|---|
| Unload the LLM | `POST /v1/chat/completions` with header `X-Llama-Unload: all`, any chat body whose `model` a route accepts (with the route `openai/*` it must start with `openai/`: another name is answered 400 *after* the models were unloaded) | 1 to 2 s, answers 200 when the router reports nothing in memory, 502 when not; the next ordinary request loads the model again, **about 55 to 60 s**, and `slot_cache` restores its prompt cache because `gpu_unload.py` makes it save and forget the slots first |
| Start the speech server | `POST /gpu/tts/start` (pass-through to the controller, `<container>/start`) | 13 s, returns when `/api/model-info` answers; the model of its `config.yaml` (Turbo) is loaded at start |
| Load a model | `POST /gpu/tts/load` (pass-through to `/restart_server`) | 8 to 14 s with the files cached, 47 to 52 s with the first download of a model; needed after an in-server unload, **the server does not reload by itself** (503 "TTS engine model is not currently loaded") |
| Speech | `POST /v1/audio/speech` (route `chatterbox`, `mode: audio_speech`; bytes and content type pass unchanged) or `/tts` for the full parameter set (direct to the server: no route yet) | Turbo about 2.7x faster than real time |
| Stop the speech server | `POST /gpu/tts/stop` | 0.7 s |
| Start / stop ComfyUI | `POST /gpu/comfy/start`, `POST /gpu/comfy/stop` | start 11 to 13 s (returns when `/system_stats` answers); stop 31 to 37 s = the 30 s grace period of `docker stop` (ComfyUI ignores SIGTERM): set `STOP_TIMEOUT=5` in the controller's `.env` or `stop_grace_period: 5s` in the compose file |

**Plugin sequences:** before a batch the LLM unload, then `/gpu/tts/start` (and `/gpu/tts/load` only when the server was left without a model, or for a model that is not the
config's); after it `/gpu/tts/stop`. For sound effects add `/gpu/comfy/start` before and `/gpu/comfy/stop` after (both fit on the card at the same time: below).
`HookRunner.runAll` for the before list (stops at the first failure: the next request must not load a model on a full GPU), `runAllRegardless` for the after list (every container
is stopped whatever happens to one request), a timeout and a wait per request.

**Memory (card 34.2 GB = 31.9 GiB, the LLM about 31):** both containers stopped **57 MiB**; Chatterbox loaded 4.85 GB; ComfyUI idle about 0.8 GiB (more with the SFX models:
3 to 4 GiB in fp16, 9.35 GiB for two models in fp32). **Stopping the containers is the only reset that holds**: Chatterbox "unloaded" in-process (`POST /api/unload`) left 278 MB at first
and about **1 GiB after many model hot-swaps** (nvtop: `server.py` 1029 MiB); ComfyUI's `POST /free` released **nothing** (3.94 GiB before and after, also after a trivial prompt). So
both are stopped as containers between batches; the in-server unload is only for short pauses inside one.

**Gotchas met while setting it up (the how-to must say them):**
- The socket proxy (`tecnativa/docker-socket-proxy`): a POST is refused unless `POST=1` (so `POST=0` with `ALLOW_STOP=1` refuses the stop, the controller got a 403), and `CONTAINERS=1`
  allows **every** `/containers/...` path, create and exec included = root on the host: use `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, `CONTAINERS=0`; the controller then cannot inspect a
  container and waits for the **health address** of each one (`GPU_CONTROL_CONTAINERS=name=http://host:port/health`). The token of the controller lives in its `.env` and in LiteLLM's
  environment (`headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}` in `pass_through_endpoints`; LiteLLM replaces a token a client sends, and does not pass the client's `Authorization` on);
  Marginalia stores only the LiteLLM key (a protected header of the hook).
- `restart: unless-stopped` starts a container at boot and takes the GPU (Chatterbox loads its model at start: 4.8 GB): use `restart: "no"` for what the controller starts. A wrapper
  that unloads the model once after start (to keep boot cheap) races a following `/gpu/tts/load`; without it the start alone gives a loaded Turbo.
- ComfyUI: two `CLI_ARGS` lines in a compose file replace each other (`--listen` is lost): one line.
- Chatterbox needs `pykakasi` in the image for Japanese (below); `requirements-rdna4.txt` does not list it.
- The first call after a model load can be slow once (27.8 s for a short sentence in the first run; not seen again).

## Chatterbox (the speech engine)

- **API shapes.** `/v1/audio/speech` takes `model`, `input`, `voice` (a file name, with the extension: the predefined ones of `GET /v1/audio/voices`, or a file of `reference_audio/`),
  `response_format` (wav, opus, mp3), `speed`, `seed`, `language`; **no `exaggeration`, `cfg_weight` or `temperature`**. `/tts` takes `text`, `voice_mode` (`predefined` with
  `predefined_voice_id`, or `clone` with `reference_audio_filename`), `output_format`, `split_text` + `chunk_size` (50 to 500, default 120; long text is split and joined), `temperature`,
  `exaggeration`, `cfg_weight`, `seed`, `speed_factor`, `language`, `stream`. The answer is mono 24 kHz mp3 at 32 kbps (about 14 MB an hour; a part of a book fits a `Resource`).
  So the backend uses **`/tts`** (the parameters are the point of the studio) and `/v1/audio/speech` only as the simple route. `GET /api/model-info` answers 200 also when nothing is
  loaded (`loaded: false`): it says the server is up, not the model. `POST /save_settings {"model": {"repo_id": "chatterbox" | "chatterbox-turbo" | "chatterbox-multilingual"}}` (persisted in
  `config.yaml`) + `POST /restart_server` swap the model without restarting the container. The server has no authentication and no delete call.
- **Models (one text, two cloned voices, the author's ears).** **Turbo** (350M, English, the sound tags): about 2.7x real time, **found the best sounding**; the original (500M, English,
  `exaggeration` and `cfg_weight`): about real time, 27 to 33 s per 25 s; multilingual (23 languages: ar da de el en es fi fr he hi it ja ko ms nl no pl pt ru sv sw tr zh): about real time.
  The model is a choice of the **backend profile** (one backend per model, each with its own start / load requests; a book picks the backend). **Not tried:** the original with a higher
  `exaggeration` (the server's config has 1.3), other texts and voices.
- **Settings sweep of the original (8 clips):** time does not depend on them; `exaggeration` up makes it a little shorter, `cfg_weight` down longer (the male clip 33% longer at 0.3);
  **a different seed alone changed the length as much as the settings, and the author found the seed 7 clip the most natural**: the seed counts for more than what was swept. So a seed per
  segment is part of its cache key and "another take" (a new seed) is a feature. Reading plain narration is a weak test of the expression settings (to check on dialogue).
- **Sound tags (Turbo only):** `[laugh] [chuckle] [sigh] [gasp] [cough] [clear throat] [sniff] [groan] [shush]` inside the text. `[clear throat]` was well placed and sounded fine.
- **Mood.** Nothing reads the mood of the story; the delivery comes from the reference clip and, on the original, `exaggeration`. A line labelled `whisper` (intensity 0.2, so `exaggeration` 0.5)
  came out "not a whisper, not normal talk, in the middle". So **a mood needs its own reference clip** (a whispered clip of the voice, or of any whisperer; probably also `shout`):
  the cast is voice x mood. Not tried yet.
- **A heading `I. The Bricked Arch` was read wrongly** (the roman numeral): solved by the spoken form (step 8).
- **Japanese in an English book.** Only the multilingual model can read it (`language: ja`). `……息をしてる。` came out as "aa atari yo": the multilingual tokenizer converts kanji to
  hiragana only with **`pykakasi`**, which the image of the server did not have (the log says "pykakasi not available - Japanese text processing skipped", the kanji then go on unchanged;
  the same silent skip exists for Hebrew `dicta_onnx`, Russian `russian_text_stresser`, Chinese `spacy_pkuseg`). The durations showed it before the ears did (kanji 3.0 to 4.6 s for a one
  second sentence, hiragana 1.2 s). Fix: `RUN python3 -m pip install --no-cache-dir pykakasi` in `Dockerfile.rdna4` before `COPY . .` (cached layers stay). After it all five variants (also the
  leading `……`) say "iki o shite iru" correctly; the `……` is not voiced. A dictionary reading of an ambiguous kanji can still be wrong: the labelling may give the kana reading later.
  The voice stayed "jarring": a clone of an English reading carries the accent into Japanese, so **a cast entry may need a voice per language** (a Japanese clip for the Japanese pieces).
- **Mixed languages are rendered grouped by model** (each piece is its own file, the order does not matter) and joined in the order of the chapter: one model switch per model per batch, not one
  per change of language. Swap time (8 to 14 s) does not matter for a batch.
- **Scene 1 (the first chapter of the test book, 496 words, 18 pieces):** the author's cast (a male clone for one character, a female clone for the other, a separate narrator clip) rendered
  on Turbo, the Japanese line on the multilingual model; the assembly: **every piece normalised to the same loudness** (`loudnorm` I=-20, a 10 ms fade-in; the narrator clip was far louder than the
  others) and pauses by position (1.0 s after the heading, 0.6 s between paragraphs, 0.12 s from a line of speech into its "he said", 0.25 s otherwise), 64 kbps mp3, 172 s, -20.4 LUFS.
  Turbo for 17 pieces took 58 s, original 173 s, multilingual 197 s. The cast was a good test: the author called the cloned voices convincing.

## Voices (reference clips)

- A reference voice is a file in `reference_audio/` of the TTS server (bind mounted, so it survives), uploaded once with `POST /upload_reference` (multipart `files`, wav or mp3,
  `max_reference_duration_sec` 30) and named in a request; a request cannot carry the audio. So **once per server per clip**, not per book, and the TTS server holds only a copy: the
  master is a `Resource` of the cast. Before a batch the plugin lists the files (`GET /get_reference_files`) and uploads the missing ones under a name made from the hash of the bytes (the
  same bytes, the same name; users cannot collide). **Open:** the upload through LiteLLM (does `pass_through_endpoints` forward a multipart body?), no delete call (clips pile up on the server),
  every user of the server can list and use every clip.
- **A good clip** (what worked): 12 to 30 s of one speaker, no music, cut where a pause is (a clip that starts on a hard consonant or ends mid-word is audible in the clone), mono 24 kHz;
  the loudness of the clips differs (-20 to -29 dB mean), the pieces are normalised after synthesis. A **narrator needs a clip of a reading voice**, steady and calm (15 s worked).
- **Noise in a clip:** a high-pass and `afftdn` removed nothing measurable of soft footsteps (the sub-80 Hz band was already at -48 dB; steps are intermittent, not a steady hiss): cut around
  them, or try a speech denoiser (RNNoise, `arnndn`: needs a model file). The author judged the cleaned clip good enough to clone.
- **The narrator is a role of its own** with its own voice and settings, plus a voice for a speaker the labelling cannot resolve.

## The language model: labelling (step 6)

- **Do not ask the model to copy the text.** Asked to split a 470 word chapter into segments word for word, it dropped narration between two speeches in 3 of 3 tries (thinking off). So the
  part is **split in code** into pieces of narration and pieces of direct speech (between the quotation marks; a markdown heading is narration, the `#` removed) and the model only **labels** the
  numbered pieces: `speaker` (narration is always the narrator), `delivery` (neutral, calm, tense, sad, angry, joyful, amused, whisper, shout), `intensity` 0 to 1, and for a *short* piece of
  speech `tagged_text`: the piece word for word with sound tags inserted where the context clearly calls for them (at most one per 80 words of the chapter). A tagged text that is not the original plus
  tags is dropped (the original is used), so the book can never be changed by the model.
- **Tried (`gemma-4-31b-it-MM` through LiteLLM, thinking off, 18 pieces of which 7 speech, 1 sample):** 1019 completion tokens, 110 s (60 s of it the reload); all seven speeches attributed
  correctly (also a vocative "Tomáš," and the reply, from the narration around them); the Japanese line `whisper` 0.2; one tag, `[clear throat]` after the narration "He cleared his throat". An
  earlier passage (2 speakers, 95 words): the text preserved exactly, a whispered threat 0.6 to 0.7, a shout 0.8, a single `[sigh]` after "Jonas sighed", no tag in narration, no `[laugh]` for a character
  who "was already laughing" (careful, not wrong); with thinking 11 segments, 3169 tokens, 213 s; with `chat_template_kwargs.enable_thinking=false` 8 segments, 449 tokens, 23.6 s, the same result:
  **thinking off** is the setting. Not tried: a scene with three speakers, a long monologue, first person narration, a thousand-word part.
- **Delivery and tags are engine specific:** the plugin maps what the labelling says to what the engine has (tags on Turbo only; `exaggeration` / `cfg_weight` only on `/tts`; a mood clip) and drops the rest.
  A piece in another script (Japanese in an English book) gets its language and so its model.
- Own `InferenceSession` kind for the cache swap; the conlang translator already finds who speaks a line (look at reusing it).

## Sound effects (ComfyUI, Stable Audio 3 Small-SFX)

- **Files:** `Comfy-Org/stable-audio-3` (not gated): `checkpoints/stable_audio_3_small_sfx.safetensors` and `text_encoders/t5gemma_b_b_ul2.safetensors` (`hf download Comfy-Org/stable-audio-3
  checkpoints/stable_audio_3_small_sfx.safetensors text_encoders/t5gemma_b_b_ul2.safetensors --local-dir .` in the models folder). ComfyUI 0.34.0 loads it as `StableAudio3` + `SAT5GemmaModel` +
  `SA3AudioVAE`; its template list (`/api/workflow_templates`) showed no audio entry. **Stable Audio Open 1.0 was dropped**: its Stability repo is gated (`auto`, accept on the website; it has `model.safetensors`, not the file
  the ComfyUI docs name, the repack is `Comfy-Org/stable-audio-open-1.0_repackaged`), the docs' encoder repo `Comfy-Org/t5-base` does not exist, and it gave constant (NaN) or cracked output.
- **The graph that works** (taken from the official blueprint `blueprints/audio_generation_stable_audio_3_medium.json` of `Comfy-Org/workflow_templates`, read with `gh api`), API format:
  `CheckpointLoaderSimple` -> `CLIPLoader` (`t5gemma_b_b_ul2.safetensors`, type `stable_audio`) -> `CLIPTextEncode` (the prompt) and `CLIPTextEncode` (empty, the negative) -> `EmptyLatentAudio`
  (`seconds`, `batch_size` 1) -> `KSampler` (positive and negative straight from the encoders, **steps 8, cfg 1.0, sampler `lcm`, scheduler `simple`**, denoise 1) -> `VAEDecodeAudio` ->
  `SaveAudio` (flac) or `SaveAudioMP3`. **No `ConditioningStableAudio`** (that node belongs to Open 1.0). 10 s of audio take about 1 s. The result is in the `audio` key of the outputs of
  `GET /history/<id>` (the Image Generator's ComfyUI backend reads `images` only), fetched from `/view`. Output is 44.1 kHz stereo and quiet (peaks 0.1 to 0.6): normalise it.
- **How the wrong recipe was found:** the same prompts with `euler` / `dpmpp_3m_sde_gpu`, 20 to 50 steps, cfg 3 to 5 and the Open 1.0 conditioning gave hiss, "crunch", clipping and constant NaN
  output; fp32 (`--force-fp32`) did not cure it; the output of a repeated identical job is **cached by ComfyUI** (0.5 s, so it is no test of repeatability); inside one run the sampler and the
  decoder are exactly repeatable. A spectral check (centroid, flatness) separated two kinds of defect, not good from bad. Only the official graph gave sane levels.
- **The author's ears:** the bolt, the boots and the wind **sound correct**. Drips: the ambience prompt sparkles (8 steps) or is "crunchy" (16); with cfg 1.5 and a negative prompt the dripping is
  right but the peaks distort; "slow water drips echoing in a stone cave" buzzes, or gives one drop and then silence; **"a single water drop falling into a puddle, close up, dry, no reverb"
  is the best** (a tiny buzz at 8 steps, good at 16 but a bit artificial, "it repeats").
- **Lessons for the studio:** (1) describe the **single sound, dry**, not an "ambience" or an echo; (2) `lcm` / `simple`, 8 to 16 steps, cfg 1 (the negative prompt does nothing at cfg 1); (3) a
  **repeating sound** (drips, footsteps) is better built from *one-shots placed by the mixer at irregular intervals* than asked from the model in one long clip: a cue is a `one-shot` (generate a short clip,
  trim the silence, place it) or a `bed` (a long texture at a low level, only for textures such as wind and rain), and the LLM that proposes the cues says which.
- **Not tried:** removing `--force-fp32` (added before the right graph was found: the tiny buzz may or may not come from it), a negative prompt with cfg between 1 and 1.5, the Medium model,
  other wording, mixing a bed under speech.

## A conlang

The Conlang Translator keeps the speech of the story in the invented language and the English as its source. A speech engine knows no invented language: Chatterbox takes text plus a language
id and normalises it per language; there is no phoneme input. **Tried** (the multilingual model, a cloned voice, seed 42; the Vrakht of `screenshots/dataset/conlang-vrakht.json` with a male voice,
Aethel-Vael of `marginalia/plugins/conlang-translator/aethel-vael.md` with a female one; five short sentences each): the raw text as English, as German (Vrakht looks Germanic) or Italian (Aethel-Vael looks
Latin), and respelled by the LLM for Finnish, Italian and Spanish. The respellings were small and regular (`za` -> `sa`, `k` -> `c`, `j` -> `y`, `ei` / `ae` -> `ai`), and where the spelling is already regular they
add little. **The author's verdict: "they all sort of work"** (it is a conlang), so **the plugin should let the user audition the candidate languages for a conlang and pick the best** (a per-language setting
"read as: ..." with a preview of sample sentences in each candidate, in the studio). The respelling is only as good as the description of the sounds it starts from, and neither language has a phonology
section (the lexicon has a `script` field per word, no pronunciation): a `pronunciation` field on the entries, written once by the LLM and editable, or a phonology section, is the place for it; the
spoken-form step substitutes it. Engines that take phonemes (Piper, Kokoro through espeak-ng) could be exact but cannot clone voices and sound worse; training a voice on a conlang is not realistic. The
audiobook can also read the English source of a speech (dubbing) as a per-language choice. **Not tried:** a pronunciation field, other candidate languages (Turkish, Japanese kana).

## Open

- **The audio as a `Resource` (done):** `ResourceAudioStore` stores every piece and every joined part as a `Resource` linked to its part; the index (cache key -> resource uuid) is in the part's `attributes` under the plugin's key, so the audio travels with the book; a replaced audio is soft deleted (the files stay until the *Resource Files* garbage collection); wav is stored as well as mp3. The readers of a published book are served by `ResourceService.readForReader` (the book is viewable by the user at every request, the resource is the owner's and linked to a part of that book; the content type is told from the bytes). Still to check by hand: how the Resources tab shows a non-image file and how a backup of a long audiobook behaves (size).
- **Gates (done):** the gates live in the application (`SharedGate`), `GpuGate.forBackend(gpu, url)` of `http-hooks` is the way to them, so the Audiobook and the Image Generator on one GPU find the same gate when their backends name the same **GPU (name)** (default: the address). A *mode* (`BackendConfig.gateMode()`: address + speech model) makes a batch for another model wait until the holders are gone and run its own before requests; the line is fair. Tested with fakes (`SharedGateTest`, `GpuGateTest`, `SpeechBatchTest`, `ImageSessionTest`); not on a real GPU. Not done: the gate does not know the language model that the before requests unload (a plugin that does not use `GpuGate` can still race it).
- The ComfyUI backend of the Image Generator reads `images` only; sound needs `audio`.
- Upload of a reference clip through LiteLLM (multipart through `pass_through_endpoints`: untested), cleanup of clips on the server.
- The LLM labelling on more passages; the mood clips (whisper); a Japanese reference clip for a character; the original model with a higher `exaggeration`; `--force-fp32` off for the SFX.
- Joining: the pieces are joined in Java (byte-concatenated mp3, `WavJoiner`), pauses by position. **Loudness (done 2026-11-09):** when ffmpeg is there the joined part is levelled (`Ffmpeg.normalizeLoudness`, two-pass `loudnorm`, linear, -18 LUFS default, user setting) and the loudness is kept with the part (`attributes.audiobook.part.loudness`); a part without it, or with another one, is joined again from its stored pieces when converted (no speech server); the first run on the author's server measured -26 LUFS before and -18.4 after. Not done: the level of the voices inside a part (a cloned voice louder than the narrator), a fade between pieces, the export joining parts of different levels made without ffmpeg.
- Consent for a cloned voice of a real person is left to the user.

## Steps

1. ✅ **Shared HTTP library** `marginalia/libs/http-hooks` (`Http` with a size cap and no redirects, `UrlGuard`, `HttpHook` with protected headers, a timeout and a wait per request, `HookRunner.runAll` /
   `runAllRegardless`, `GpuGate`, `BackendException`), embedded by the Image Generator, whose hooks are lists now. The guard also refuses 100.64.0.0/10, IPv6 unique-local and the IPv4 inside an IPv6 address; the host
   is resolved by the guard and again by the client (the JVM's 30 s lookup cache makes them equal; a JVM without that cache would not).
2. ✅/◐ **Settings.** *User* (*Settings → Extension Settings → Audiobook Settings*, done): own backends (address, key protected with `InferenceServices.protectSecret`, the private-addresses switch,
   the lists of requests before / after a batch as `HookListEditor` of `http-hooks` (shared with the Image Generator), the model of the profile, the timeout, the format, the defaults of a segment:
   temperature, `cfg_weight`, speed, chunk size), the backend picked, a *Test connection* button (`GET /api/model-info`: the server is there, with or without a model). The form was checked by
   unit tests of the model and of the check, **not in a running application**. *Admin side (done):* **shared backends** (`model/SharedBackend`, `SharedSettings`, kept in the settings of the installation under `audiobook.shared`; `ui/SharedBackendsDialog` opened from the admin-only section of the panel, the fields are the `ui/BackendEditor` that the user's own backends use too). Each shared backend is for all users or for the users picked by uuid (a plain access list; no token or time allowance, since a speech request has no cost to count yet), and a switch says whether users may keep their own (the own ones stay stored and are ignored while it is off). `AudiobookService.backends()` / `findBackend` / `selectedBackend()` are the only lookups (shared ones the user may use first, then their own), used by the Convert dialog, the studio, the commands and the queue, so an access that is taken away stops a job that names the backend. Users never get the address, keys or requests of a shared backend into their settings or form. Unit tests of the access rules (`SharedSettingsTest`); **not tried in a running application**. Open: the gate keyed by address needs the model (see *Open*), the narrator voice of a shared backend can only be a voice of the server.
3. ✅/◐ **The job queue** (`queue/ConversionQueue`, one per user, one worker thread with the request context of the user): jobs are *waiting*, *running*, *done*, *failed*, *cancelled*; every job that waits is
   taken as **one run**, which makes the pieces that are not stored, **one batch per speech model** (`SpeechBatch` with the requests of the backend that has the model), then joins each part. Cached by content
   (`ScriptEntry.key()` = SHA-256 of text, voice, model, language, exaggeration, cfg, temperature, speed, seed, chunk size, format; `PartScript.joinKey()` = the pieces and pauses in order): a part whose script is
   the stored one costs no batch at all, a changed paragraph is the only piece made again, a retry keeps what was made, the pieces of an older text are dropped when the new part is stored. Cancel (one job or
   all: a running job stops after its piece, the requests after the batch still run), retry, clear, a **pause while a generation of the same user runs** (`GenerationTracker`: observers of
   `BEFORE_PREPARE_GENERATION` / `AFTER_CLEANUP`, per owner of the book). Audio is stored by `ResourceAudioStore`: resources linked to the part, the index (piece key -> resource uuid, the joined part) in the
   part's attributes under `audiobook`, written once per job (`flush`). **Joined without ffmpeg** (the image has none and Java cannot decode or encode mp3): `audio/Mp3Joiner` cuts the ID3 tags and the
   Xing/Info frame and puts the frames after each other, a pause is silent frames made from the header of the first frame (verified with ffmpeg on pieces of the real server: decodes without a warning,
   the length is the sum, the pauses are digital silence).
   **Checked on the real server** (2026-11-08, Turbo, the queue with `ChatterboxBackend` and a map as the store): two parts in one run (a predefined voice, a cloned clip), 8.0 s and 6.7 s of mp3 that
   ffmpeg decodes without a warning; the same parts again: *up to date*, **0 batches**; one word changed in a paragraph: one batch, one piece spoken, the part joined again. It found that parts submitted one
   after the other in a loop become **two runs** (the worker took the first before the second was in, so an extra swap of the language model): `ConversionQueue.submitAll` puts them in together (tested).
   **Checked in the application** (the production WAR, Admin -> Extensions upload, a fake speech server on this machine): the panel in *Settings -> Extension Settings* (add a backend, *Test connection*
   says "the server answers, the model is loaded", save), the item *Convert to audio* in the tools menu (the cogs button of the story tab), the dialog (five parts of the example book -> 63 pieces, **one**
   `model-info` check = one batch), all jobs *Done* with progress and a *Download* of a valid mp3 (24 kHz mono, 8.9 s for nine pieces and eight pauses), the Resources page shows the audio linked to the
   parts, and **after a restart of the application every part says *Up to date* and the server gets no call at all** (the index in the part's attributes and the resources survived).
   **Findings:** every piece is a `Resource`, so a book makes thousands of small rows in the user's *Resources* tab (63 for five short parts): keep pieces somewhere else (a folder of the plugin, or a
   single resource per part with an index of the byte ranges) before this is used on a whole book; opening a book crashed at HEAD (`AuthorshipField` was `@Configurable` without
   `preConstruction = true` and used `loc` in its constructor: fixed, not part of the plugin).
   **Not done / not checked:** `GenerationTracker` and the pause while a generation runs (no generation ran during the test), two users on one GPU, loudness of the pieces (needs a decoder: the narrator clip
   was far louder than the others, so voices of different level stay different; ideas: ffmpeg when it is on the path, or the `global_gain` of the frames), cross-fade; a save of the part from the queue can
   race an edit of the same part (it saves the whole part, once per job); the audio only plays through the download link (the player is step 5); the narrator voice is a text field
   (`predefined:Henry.wav`, `clip:<resource uuid>`) until the cast (step 7) exists; the plain script is one voice, paragraph by paragraph, no spoken form yet (step 8: numerals, markdown marks such as `*`
   and footnote marks are read as they are). Only mp3 (the format select is gone: wav and opus cannot be joined).
4. ✅/◐ **The backend interface** (`SpeechBackend`) and the first implementation (`ChatterboxBackend`): `/tts` with the whole set of parameters (`SpeechRequest` = one entry of the script: voice mode,
   voice, language, exaggeration, cfg weight, temperature, speed, seed, format, chunk size; the language goes to the multilingual model only), `prepare()` (the server is up *and has a model loaded*:
   a missing load request is told as that, the 503 as well), `referenceFiles()`, `predefinedVoices()`, `ensureReference(bytes, ext)` (a multipart upload under `m-<24 hex of the SHA-256>.<ext>`,
   nothing sent when the list already has the name). `SpeechBatch` / `AudiobookService.runBatch` run a batch: before requests (stop at the first failure) -> `prepare` -> the work -> after requests (all
   of them, the first failure reported next to the result), through a `GpuGate` of the server address. Tested against a fake server (12 + 6 tests) **and on the real server** (2026-11-08, directly at port 8004, Turbo): `prepare`, 28 predefined voices (`/get_predefined_voices` entries are
   file names), the list of clips, the upload (once, the second call sent nothing), a predefined and a cloned piece (different audio, 2.4 s each, mp3 24 kHz mono). What the check found: **(1)** Java's
   `HttpClient` offers `Upgrade: h2c` on a plain connection and an ASGI server (uvicorn/httptools: Chatterbox-TTS-Server, LiteLLM) then takes the body for upgrade data and the app sees an empty
   request (422 "files field required"): `Http` in `http-hooks` is HTTP/1.1 only now (a test checks the header; it affected every POST with a body to such a server). **(2)** A bare container start with
   the author's startup wrapper (it unloads the model once the server answers) leaves the server without a model: `prepare()` said so; the batch needs the load request (`POST /gpu/tts/load`) after the start.
   **(3)** Starting the TTS container while the language model holds the GPU filled the host's RAM (30 GB, swap nearly full) and the OOM killer took Elasticsearch, a Jetty and the TTS process: **the
   unload must come first, and never a start on a loaded GPU**; the before list of a backend must begin with the unload request (a default to offer in the form). *Not done:* the upload through LiteLLM (a multipart pass-through), a cancel inside a running `/tts` call (it ends with the call), the cross-plugin gate.
5. ✅/◐ **The player in the viewer** (`AudioPlayer` + `audiobook-player.js`, added by a decorator of `Viewer.renderManuscriptReadingView`, only for a book that has parts with audio): the page is given the
   address of the audio of each part that has some (a `StreamResource` per part of this page, read for the user who asks, like the images of the parts; `PlayableParts.of(branch)`: the joined audio exists
   **and was made from the text the part has now**, audio of an older text is not offered). The script fetches a part into memory (a blob: plays also in browsers that refuse a stream without range requests),
   plays the parts in order, marks the one that is heard (`audiobook-heard`), keeps only it and the next one in memory and fetches the next while the first plays, waits for the lazy loader of the reader when
   the loaded parts end, starts from the selection or the first part in view, speed 0.75 to 2 kept in the browser, **Media Session** (title of the book, the number of the part, play / pause / stop /
   next / previous part of the device). Checked in a browser (headless Chrome against the real app, a fake speech server, parts 1 to 3 converted of five): plays, the mark follows, pause and resume at the
   same point, the next part follows by itself, speed applies at once and is remembered, a selection decides where it starts, stop clears everything, the media session has the book, and **a part without audio
   stops the player with a notice** ("This part has no audio (yet)") instead of skipping a part of the story. The bar sits left of the read-aloud bar of `tts-reader` and does not overlap it when it grows.
   **Published books (done):** the audio is served to the owner and, while the book is published, to every user who can open it (core `ResourceService.readForReader`, tested in `ResourceReaderAccessTest`; not tried in the running application with a second user). **Browser voice at a gap (done):** at a part without audio the player stops with its notice and fires `marginalia-audio-gap` (`detail.messageId`, `detail.reader`); `tts-reader` answers by putting `detail.reader` (`readFrom(element, until, done)`, `stop()`) into the event, and the player then shows a button (`audiobook-offer`). Decided: it is **offered, never started unasked**; once accepted in a listening run, later gaps of that run are read without asking again (reset by pressing play anew). The browser voice reads the part and the ones after it until the first part that has audio, then `done(part)` hands back to the player; Stop silences both. Checked in headless Chrome with fake audio and a fake speech engine (gap, no Read Aloud: no button, two gaps accepted once, stop); not with a real voice. **Not done:** Safari and phones are not tried; no seek bar, no remembered position, no sleep timer; the bar is not shown in the phone layout
   specially (it is the same fixed box).
6. ✅/◐ **Labelling** of a part (`label/`): `PieceSplitter` splits the text **in code** into pieces of narration and speech (straight and curly quotes, the Japanese corner brackets 「」 and 『』, headings, an unclosed quote stays narration; the words are
   never changed, tested as `normalize(pieces) == normalize(text)`); `LabelPrompt` + the typed schema `LabelAnswer` / `PieceLabel` (`@LLMSchema`, so the application builds the schema and checks the answer);
   `PieceLabeler` asks in **windows of 16 pieces with 4 pieces of context** and, when an answer does not match the schema (a cut-off JSON), **again in halves** down to 4 pieces; `ScriptFromLabels` makes the
   script and is where the book is protected: a tagged text is taken only when it is the piece plus **allowed tags** (anything else, a changed word, an unknown bracket: the original), tags only for **Turbo**,
   intensity -> `exaggeration` only for the models that have one (a constant for Turbo, so the cache does not miss for nothing), a missing or odd label = the narrator, neutral; pauses by position (heading 1.0,
   paragraph 0.6, speech into "he said" 0.12, else 0.25); a piece in another script goes to the multilingual model when the user has a backend for it, else it stays. Labels are **cached in the part's attributes**
   (`audiobook.labels`, key = hash of text + characters + prompt version; incomplete sets are not stored). `LlmLabelModel` is the model call (`InferenceSession` kind `audiobook`). **In the dialog:** *Label the
   parts with a language model* + a model picker (the book's by default); the names of the characters in the instruction are the point of view and the present characters of the parts up to this one; the
   cast (step 7) gives each speaker a voice.
   **Checked on the real model** (`gemma-4-31b-it-MM` through LiteLLM, the exact request the application sends, 17 pieces of the example chapter, 2 characters): all seven speeches attributed correctly (the
   vocative "Tomáš," and the replies too), the whispered Japanese line `WHISPER`, `[clear throat]` after "He cleared his throat", no tag in narration. **With thinking on and the 2048 tokens of the protocol the
   answer is cut off often** (16 pieces: cut off; 8: cut off; 4: fit, 955 to 1743 tokens, the thinking is 2 to 7k characters, the same request fit by 91 tokens once and failed once): the halving made it 6 calls
   instead of 1 and it worked; **use a protocol with thinking off or a bigger limit** for the labelling (earlier: 449 tokens, 24 s for 8 segments with `enable_thinking=false`). The model copies the text
   into `tagged_text` for most speech pieces when told to "leave it empty"; the new wording ("never copy the text of a piece that has no tag") was not measured because the next run was cut off
   (copies are harmless: no tag = the original is read).
   **Checked in the application** (fake model server): the schema went out as `response_format` `json_schema` with the delivery names and `tagged_text`, the characters from the parts, a valid tag was
   kept (`[sigh] Tomáš,`), an altered text was thrown away, the second run and a run after a restart made **no model call and no speech call** (labels and audio cached).
   **Not done:** `delivery` and `intensity` are not kept in the script entries (only what they turn into) - the studio (7b) needs them in the entry; the delivery of a *mood* needs its own
   clip; the instruction is English and the attribution pattern for the pauses is English; no spoken form (step 8); no cancel of a labelling call that runs; a part of a thousand words, a scene with three speakers,
   first person narration and a conversation across windows are not tried on the real model.
7. ✅/◐ **Actors and the cast** (two levels): an **actor** (`model/Actor`, in the user's audiobook settings, `AudiobookSettings.actors`, stored at once by `ui/ActorDialog`) is a reusable *voice*: a name, a voice and
   voices for a **mood** (`mood:WHISPER`) or a **language** (`lang:ja`) (`Role`, `ui/RoleEditor`). The **cast** (`model/Cast`, kept **in the book**: its attributes `audiobook.cast`, so it travels with the book;
   `ui/CastDialog` from *Convert to audio → Cast*) says who plays whom: the actor of the **narrator**, of a speaker the cast does not know (the **fallback**), and of each **character** (name, other names, actor).
   (First built as named casts in the settings picked per book/folder by an inherited setting; replaced: the characters are the book's, the voices the user's.) `Cast.voice(speaker, mood, language, actors, backendVoice)`
   picks: the language of the piece before its mood before the voice of the actor; a character without an actor (or whose actor was deleted) is read by the fallback, then the narrator, then the voice of the backend
   (the narrator voice of the backend stays as that last resort); a name is found by name or alias ignoring case and accents, else as **one word of exactly one name** ("Tomáš" for "Tomáš Novák", not when two
   Tomášes are in the cast); a broken voice text is skipped. A voice is the text form of a `VoiceRef`; `VoiceField` offers the audio resources of the user (not the plugin's own `audiobook-*` pieces), an **Add a clip**
   button uploads one as a resource, and a typed `Henry.wav` is a voice of the server (it is usually stopped between batches, so its voices are not listed). The cast dialog has *Add the characters of the book* (point of
   view and present characters, skipping a name the cast already resolves) and *New actor* (also next to each character). The cast is read **again when a job runs** (a change made while it waits is heard), its names go first
   into the characters of the labelling instruction, and only the pieces whose voice changed are spoken again (the voice is in the key of a piece). `ScriptFromLabels` asks a
   `VoiceChooser(speaker, delivery, language)`. Tests: `CastTest` (resolution, deleted actor, JSON), the chooser test in `ScriptFromLabelsTest`.
   **Checked in the application** (fake servers): four actors made in the settings (one with a mood voice), the cast of the book (narrator, fallback, a character picked, one left without an actor), the characters added from
   the book: Tomáš (calm) read by the calm clip of his actor, Mizuki (no actor) by the fallback, the narrator's pieces cached. **Found by the test:** the label generator of a `Select` with an empty choice is called with
   `null` (it crashed the whole settings panel).
   **Not done:** a cast that is shared by the books of a series (it is per book; a *copy the cast of another book* button, or a cast on the folder, is the way); the actors are not in the backup of a book (the cast keeps their
   ids, the actors are the user's, like the clips); the **audition** of a voice (a short sample in the dialog: needs the server up, so it waits for the studio, 7b); the voices of the server are typed, not listed; suggestions from
   the lorebook entry of a character; a language list beyond what the script detection yields (ja, zh, ko, ru, el, he, ar, hi: Latin-script languages such as German need a language setting of the book, step 8); a book
   converted without labelling has the narrator only; no check that a clip is a good clip (length, noise); the admin side of step 2.
7b. ✅/◐ **The Audiobook Studio** (*tools menu → Audiobook studio*, `ui/StudioDialog`, `studio/`): the automatic run makes a draft, the final audiobook is made part by part.
   - **The script of a part** is `StudioScript` = the segments + the hash of the text + the status, kept in the part (`audiobook.studio`, `PartStudioStore`), so it travels with the book; a `StudioSegment` = the
     `ScriptEntry` (everything the server is told, as before: speaker, voice, model, language, spoken text, exaggeration, cfg, temperature, speed, seed, pause) + the **words of the book** (`source`), the delivery and
     intensity of the labelling, **locked**, and the **takes** (seeds tried). `StudioScript.toPartScript()` is the input of the renderer; the audio of a segment is still cached by the hash of its entry.
   - **Annotate** (`ConversionJob.Mode.SCRIPT_ONLY`, through the queue so the language model is handled like the rest): the script is made fresh (labels of the model, or the plain narration) and **merged** with
     the stored one (`StudioScript.merge`): a **locked** segment (changed by hand: any edit, a chosen take, a pause) is kept with its text and takes, matched by the words of the book in order, everything else is the new
     annotation; the status stays when the text is the same, else draft. Every run of *Convert to audio* does this too (the stored script is what is spoken), so the edits of the studio survive a batch.
   - **Hear** (`Mode.SEGMENTS_ONLY`): only the pieces that are not made are spoken, nothing is joined, the audio is kept with the part; a piece that is there is played at once. **Another take** (a new random seed; the
     earlier takes and their audio stay: `PartScript.keepKeys`, so the join does not delete them), **choose a take**, **lower the intensity** (exaggeration down for the models that have one; on Turbo, which ignores it,
     a new take), **more pause** (+0.3 s), **unlock**. The **raw request** of the segment is shown as JSON (a clip by its name).
   - **Editing:** every number, the model, the language, the voice (a changed *delivery* asks the cast for the voice of that mood unless the voice was set in the same go; a changed *intensity* moves the exaggeration).
   - **Status** draft -> reviewed -> final: *Convert to audio* and *Annotate* leave a **final** part alone (and say how many). *Make the part from this script* (`render`) speaks the stored script as it is
     (refuses when the text changed since: annotate again).
   - **Cost:** a hearing or a part is one job, so one swap of the language model per run, and everything is cached by content.
   **Checked in the application** (fake servers): annotate (labelled), edit a segment (locked), hear, another take (new seed spoken once, two takes kept), more pause (0.6 -> 0.9), lower the intensity, make the part (nothing else
   spoken again), status final disables *Annotate*, a second annotation keeps the locked segment with its takes. Tests: `StudioScriptTest`, the job modes in `ConversionQueueTest`.
   **Not done:** **no audition of a voice outside a segment** and no compare-side-by-side of takes (a take is chosen and heard one at a time); a hearing needs the speech server up, so it runs the *before* requests of the
   backend (the language model is unloaded) and waits behind a running job; the **selection** of several segments or the whole part as one hearing (hear a segment, or make the part); undo of an edit (*unlock* only
   gives the annotation's version back after the next annotation); a segment cannot be split, merged, added or deleted by hand (the annotation decides the pieces); intensity of a locked segment is not changed
   by a new cast (the voice is part of what was locked); the studio uses the selected backend only; takes are never deleted until the part is made again; the export of only the *final* parts is step 11; the player in
   the reader plays whatever is stored, final or not.
8. ✅/◐ **The spoken form** (`script/SpokenForm`, `NumberWords`; settings `model/SpokenSettings` kept **with the book**, `audiobook.spoken`; `ui/SpokenFormDialog`, the *Pronunciation* button of *Convert to audio* and
   the studio): what the voice is given is spelled apart from the words of the book (`StudioSegment.source` stays the book's, so a locked segment is still found; the studio shows *In the book: ...* under the
   spoken text). The rules, in this order: a **scene break** (`***`, `* * *`, `---`, dashes) is nothing: no segment, the pause before it is 1.2 s (none at the end of the part); links and markdown marks (`*`, `_`,
   backticks) go, a snake_case word stays; the labelling's **sound tags** are left alone; the **dictionary** of the book replaces whole words, case sensitive, longest first, and what it puts in is not touched
   by the rules after it (held back as placeholders in private use characters, which no rule takes for a number); then, for a piece in Latin script and with the **English rules on** (off for a book in another
   language: only the marks and the dictionary): a **numbered heading** ("IV. The Arch" -> "Chapter four. The Arch", "Chapter IV" -> "Chapter four.", "3. Home"; the word is a setting of the book; a lone letter
   that could be a word ("I remember", "C is for cat"), a number with no mark ("3 Men in a Boat"), a numeral over 100 ("MIX") or a malformed one ("CIVIC") is not a chapter; roman numerals in the body are
   left alone), **abbreviations** (Mr. Mrs. Ms. Dr. Prof. Jr. Sr. Capt. Col. Gen. Lt. Sgt. Mt. vs., etc., e.g., i.e.), **numbers** (currency $ £ €, times 9:05, ranges 10-20 with an en dash, percents, ordinals
   1st, decimals, thousands separators, a **year** only after in/since/from/until/by/of/year/during/before/after/circa, leading zeros and over 15 digits digit by digit, else a plain cardinal).
   A piece in another script (kana, Cyrillic...) gets no English rules. The **dictionary is also how a Japanese reading is fixed** (the word -> kana) and how a conlang word is respelled. The settings dialog has a
   live *Try a text*. The settings are read **when a job runs** (a change is heard on the next annotation or batch: only pieces whose spoken text changed are made again). Tests: `SpokenFormTest` (every rule and
   the cases it must leave alone), the scene breaks in `ScriptTest` and `ScriptFromLabelsTest`.
   **Checked in the application** (fake servers): the preview of the dialog, a dictionary word (Mizuki -> Mee-zoo-kee) used by the annotation, the *In the book* note, and the batch speaking only the one piece that
   has the word.
   **Not done:** the labelling does **not** return a spoken form (the rules and the dictionary do it in code, which cannot change the words; a model that proposes pronunciations for the odd words of a book,
   checked to be the same word, is a later idea); the rules are **English only** (numbers in Czech, Japanese counters, kana readings by a dictionary of the language: none); roman numerals and dates in the body, "St."
   (saint or street), "No. 5", a number next to a unit ("5 km"), phone numbers, and "$5 million" are read plainly or wrongly; the dictionary is per book (no user-level or folder-level one, no import from the
   conlang translator plugin, whose words could fill it); no check that a respelling is read as meant (hear it in the studio).
9. **Engines with their own API** (a tagged multi-speaker script like Dia's `[S1]` / `[S2]`) as more backends, or a generic HTTP template backend (the same gap the Image Generator has).
9b. **Sound effects and ambience** (after the speech works): cues next to the segments of a part, proposed by the LLM from the text with a position and a kind (`one-shot` or `bed`); each cue a prompt for the
   sound backend (ComfyUI with the graph above, another backend, cached by prompt and seed; the prompt describes a single sound, dry) or a file of the user's own library; the mix with ffmpeg (levels, ducking
   under speech, fades, one-shots placed at irregular intervals, a bed under a stretch of segments).
10. ✅/◐ **Automatic conversion** of a new part when it is final, **off by default**: the hook is the one the conlang translator uses, `AutoAnnotate.finished` (called when nothing more will happen to a generated
   part by itself: right away when the book does not annotate its parts, else after the notes and the redraft), decorated after the method. The choice is the **inherited setting `audiobook.auto`** (book -> nearest
   folder -> the user's *Convert new parts to audio by itself* in the audiobook settings -> **no**): *No*, *Yes: the narrator reads the part* (plain script) or *Yes: labelled by the model of the book, the cast gives
   the voices* (the AI and the protocol of the book; a book without a model gets the plain narration and a warning). `AudiobookService.convertFinishedPart` finds the part on the active branch, uses the speech server
   the user has picked, and goes through `convert`, so everything else holds: the cast and the spoken form of the book, the queue waits while a generation of the user runs, a part marked final in the studio is left
   alone, the audio is cached by content. The author gets a toast when the part is queued and when its audio is ready or failed (in the page that started it, if it is still open; the job is in *Convert to audio* in any
   case); a failure never reaches the story. Tests: `AutoModeTest` (the ids, anything unknown is *no*, the user level, old settings).
   **Checked in the application** (fake servers, a generation through the story editor): by default nothing happens; with *the narrator reads* the new part was queued, spoken and done, with the toasts; with *labelled* the
   model was asked once to label it before the speech.
   **Not done:** the **model of the labelling cannot be set apart from the book's** (and a thinking model needs the protocol of the book to leave room, see step 6); the automatic run does not wait for a
   part the author edits afterwards (the next *Convert to audio* makes it again, a changed text makes its audio stale); **a regenerated or edited part** is not converted again by itself (only a new generated one);
   a part that is made by a command or an agent gets no toast and, if the page is closed, no notice at all; the setting of a folder is read through the book (the usual chain), nothing converts a whole book
   by itself; no limit on how much a book may queue.
11. ✅/◐ **Export as an audiobook** (`export/AudiobookExporter`, `AudiobookFiles`; `audio/Id3Tag`, `Mp3Joiner.joinMarked`): two **formats of the export of the book**, registered with `ExporterService`, so the export dialog
   gives the title, the author, the file name and the range of parts: **Audiobook (one mp3 per chapter, in a zip)** (`01 - Title.mp3`... each tagged ID3v2.3: title, track `n/total`, book, author, genre *Audiobook*;
   a `playlist.m3u8`) and **Audiobook (one mp3 with chapter marks)** (the parts joined with 1.5 s between them, the chapters in the tag as `CHAP` frames with a `CTOC` table of contents, UTF-16 titles; the
   start of each chapter comes from the frames, `Mp3Joiner.joinMarked`). A chapter is called by the heading of its part (`# Title`), else "Part n". It runs in the request context of the user and converts nothing:
   a part with **no audio, audio made from an older text, or audio older than the script of the studio** (a segment was changed and the part not made again) is left out, and the author is told which
   (a warning, also in the log); nothing at all to export closes the progress window with a warning (the export dialog's own failure path is an error page: the exporter stops the progress window quietly, through the
   protected `manualCancel` of `ProgressBarDialog` by reflection, and falls back to throwing when that is not possible). **Only the parts marked final**: a check box added to the export dialog by a decorator of
   `createExtensionFields` (shown for the audiobook formats only), applied by a decorator of `export` that replaces its `messages` argument. Tests: `AudiobookExportTest`, `Id3TagTest`, the start times in `Mp3JoinerTest`.
   **Checked in the application** (fake servers): the two formats from the export dialog (a valid ID3v2.3 file, MPEG layer III, a chapter 0 - 14.4 s; the zip with its tagged file and playlist), the parts without
   audio left out with their numbers, *only final* with one part final (one chapter) and with none (a warning, no error page).
   **m4b and wav storage (added later):** the optional program **ffmpeg** (`audio/Ffmpeg`: `MARGINALIA_FFMPEG`, the `PATH`, the usual folders; the format is registered only when it is found, at load) makes the m4b from the joined audio (AAC 64k, `-brand "M4B "`, the chapters through an FFMETADATA file, `faststart`; checked: `ftyp M4B `, a `chpl` atom, the titles). The administrator's storage setting (`AppSettings` attributes, key `audiobook`, `storage`; `AdminGuard`, a section in the settings form seen only by administrators) makes `BackendConfig.withFormat("wav")` for the scripts built from then on; the format is in the key of a piece, so a changed storage makes new pieces; `WavJoiner` joins wav (PCM, silence by zeros, streamed headers read), `AudioKind` finds the kind from the first bytes (the browser gets the right type), a book of both kinds is joined as wav (mp3 decoded by ffmpeg). Exports: wav parts are encoded by ffmpeg for the mp3 and the zip (96k), once for the m4b; mp3 parts are encoded a second time for the m4b. The Docker image has ffmpeg (`--build-arg WITH_FFMPEG=false` leaves it out). Not run against the real speech server: that `/tts` with `output_format: wav` returns a 16 bit PCM wav is taken from the documentation of Chatterbox-TTS-Server; a float wav would be joined too (tag 3), an 8 bit one is refused.
   **Not done:** a cover picture in the m4b; the "whole audiobook" button of the convert window is mp3 only; the one mp3 file
   works in VLC, Audiobookshelf and players that read ID3 chapters, not in all; no cover picture; the file name of the one file is the name of the book; no loudness levelling of the export itself (the parts are levelled when they are joined, see *Loudness*; the loudness between the voices inside a part is not, see the
   findings), no Xing header in the one file (a player estimates the length from the bitrate, which is constant); the part number in "Part n" and in the report is the place in the export, not in the book;
   a part list with more than a few hundred megabytes is held in memory.
12. ✅/◐ **Commands, notifications, the manual and the server how-to.** *Commands* (`commands/AudiobookCommands`): `audiobook-status`, `-convert` (from, to, labelling, what = audio | script, wait: an async command with progress that
    cancels its jobs when stopped), `-stop`, `-set-part-status`, `-cast`; the forms are a ladder (the command line binds the exact number of arguments). An agent's working copy has no audio and its changes are
    thrown away, so an agent may only read the cast (the rest refuse it, saying the user has to run them). Checked in the application at the command line (status, convert with wait, final parts left alone, a range outside the book, stop).
    *Notifications*: the source `audiobook.convert` (with its automatic entry) in the notification list; `ui/BatchNotifier` tells when all the jobs of a click, a command or an automatic conversion are over (a stopped batch says nothing);
    `AudiobookService.audioState` (none / ready / older than the text / older than the script) is what the status and the export share. *Manual*: `manual/user/extensions/audiobook.md` and `audiobook-server.md` (which setup
    needs the requests before and after a batch, the requests with LiteLLM, the controller, the gotchas), 12 pictures by `screenshots/flows/plugin-audiobook.mjs` (the fake model answers the speech server and the labelling).
    **Found by the author's first real run and fixed:** the annotation could be started again and again with no sign of work (the studio now shows a bar and holds the buttons while a job of the part waits or runs);
    the cast window did not scroll; the test of the connection failed for a server that is stopped (it now makes the requests before and after a batch around the question, like a batch does); the actors were not sorted;
    the *request the server gets* showed `<♪ name>` for a clip (it shows the name the clip has on the server now); a **voice preview** (button in the voice field and the cast) speaks a sentence in the voice (in the language
    of a voice for a language, with the multilingual backend when there is one); the **key of the requests around a batch** is a field of its own (encrypted, never shown; empty: the api key) so the Bearer is not typed into the headers.
    **Not done:** the plugin unit tests do not cover the commands (they need the application's services; they were run by hand); the preview, the test with requests and the key were not run against a real server
    by me (the author's run showed *Model not loaded* after the first piece: the server log has `/api/unload`, which Marginalia never sends: a startup wrapper of the container unloads the model once the server answers and its wait
    ended after the first request: the how-to says so); a `/v1/audio/speech` mode for a LiteLLM route that carries the speech without the pass-throughs of `/tts` (no exaggeration, no upload of clips); the sound effects (step 9b).
    (LiteLLM routes and pass-throughs, the two hook scripts, the controller and its socket proxy and the Chatterbox container with `pykakasi` are in the how-to; ComfyUI with the graph waits for the sound effects.)

## Notes

- The scripts of these experiments are in [`experiments/`](experiments/README.md) (cleaned up, addresses from the environment): the pipeline, the assembly, the settings sweep, the conlang audition, the ComfyUI graph. They are not part of the plugin.
- Engines seen in a quick search, not tried: Dia TTS Server (two speakers per script, English only, GPU), VibeVoice (the TTS code was pulled by Microsoft, research-only licence), Qwen3-TTS, Pocket TTS (CPU, cloning,
  six languages), Kokoro / Piper (fixed voices, CPU), OpenSpeakers (AGPL). Sound: Woosh (Sony AI, non-commercial), MOSS-SoundEffect (Apache-2.0), TangoFlux (non-commercial), AudioLDM2 and MMAudio (unknown).
  Check each one's state and licence before building on it.
