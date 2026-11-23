# gpu-control

A small service (Python standard library only) that starts and stops containers on request, so that the models that share one GPU can take turns:
a speech server (Chatterbox) or an image server (ComfyUI) is **stopped** while the language model is loaded, and **started** (and waited for) when a
plugin needs it. Stopping a container frees its memory completely, which an "unload" call of the server inside does not always do. Used through
LiteLLM, so that one key is all a plugin needs: see *Freeing the GPU for another model* in `manual/user/prompt-cache.md`.

| Call | Does |
|---|---|
| `POST /<name>/start` | starts the container and answers 200 when the service in it is ready (504 after `START_TIMEOUT`) |
| `POST /<name>/ensure` | nothing when the service in it answers (no Docker call); else starts the container, or **restarts** it when it runs but the service does not answer within `ENSURE_GRACE` (30 s), then waits as `start` does; 200 with `"action": "none" \| "started" \| "restarted"` |
| `POST /<name>/stop` | stops it and answers 200 when it has exited |
| `GET /<name>/status` | `{"name", "status", "ready"}` |
| `POST /llama/unload` | unloads every model of the `llama-server` routers of `GPU_CONTROL_LLAMA` **except** the ones in `keep`, answers 200 `{"unloaded": [...], "kept": [...]}` once the router reports them out of memory (504 after `UNLOAD_TIMEOUT`, 502 when a router refuses or can't be reached) |
| `GET /llama/models` | `{"models": [{"server", "id", "status"}]}`, what the routers report |
| `POST /<name>/restart` | restarts the container, 200 when the service is ready again |
| `POST /llama/load` with `{"models": ["a"]}` | loads the named models and answers 200 `{"loaded": [...]}` once the router reports them loaded (404 for a name no router lists, 504 after `LOAD_TIMEOUT`, 300 s); it unloads nothing to make room |
| `POST /llama/unload` with `{"only": ["a"]}` | unloads just the named models and leaves every other one as it is |
| `GET /overview` | the containers, the llama models and the video memory in one answer |
| `GET /` | the [web page](#web-page) (needs no token itself) |

Every call needs the header `X-Control-Token`. Only the containers of `GPU_CONTROL_CONTAINERS` can be touched (`name` or `name=http://host:port/health`, the
address that answers 200 when the service is ready; without one the container's own health check is used, else a running container is ready).
`gpu_control.py` documents the environment. Tests (a stand-in for the Docker daemon): `cd tools/gpu-control && python3 -m unittest test_gpu_control`.

## Web page

`GET /` is a page for doing by hand what the calls above do: **containers** (state, ready; Start, Ensure, Restart, Stop), the **models** of the llama-server
routers (state; Load and Unload of one model, Unload all) and the **video memory** of the card, refreshed every 5 seconds. It is one static file (`ui.html`, no
dependencies, no data in it): it asks for `GPU_CONTROL_TOKEN`, keeps it in the tab's `sessionStorage` (gone when the tab is closed or on *Sign out*) and
sends it as `X-Control-Token` with every call, so the page itself is the only thing reachable without it. Stop, and Unload all, ask for confirmation.

The video memory is read from sysfs (`/sys/class/drm/card*/device/mem_info_vram_used`, the amdgpu driver, which a container sees without any setting);
a card that does not show it there (NVIDIA) is left out, the rest of the page works the same.

To reach it over the network the compose file publishes port 8000 on `GPU_CONTROL_BIND` (default `127.0.0.1`: from another machine use
`ssh -L 8000:localhost:8000 server` and open `http://localhost:8000/`; `0.0.0.0` or a LAN address publishes it to the network). The service speaks plain
HTTP, so on a network you do not trust the token travels in the clear: put it behind a reverse proxy with TLS, or keep the tunnel. Whoever has the token
controls the containers in `GPU_CONTROL_CONTAINERS`, nothing else (see *Safety*).

## Unloading the other models of a llama-server router

`POST /llama/unload` with the body `{"keep": ["joi-4b"]}` (or `?keep=joi-4b,other`; without `keep` every model goes) lets programs that share one
`llama-server` in router mode make room for their own model **without knowing about each other**: each one names only the models it wants to keep.
Marginalia's GPU lock of the language model keeps its big model; another program (an assistant that runs a small model next to its speech model) keeps
the small one. Each caller makes the call **before every job**, since something else may have taken the GPU meanwhile: when only its own models are in
memory the call unloads nothing, makes one `GET /models` and answers at once, so its model stays loaded. Only the models in memory (`loaded`, `loading`) are unloaded; a kept model that is not loaded is not loaded either (the
router loads it with the next request to it). Unloads run one at a time, so two callers keeping different models don't interleave. Set
`GPU_CONTROL_LLAMA` to the root of the router (`http://llama-server:8080`, a trailing `/v1` is dropped; several comma separated) and, if it has an
`--api-key`, `GPU_CONTROL_LLAMA_KEY`. The names are the ids of the router's `GET /models` (`GET /llama/models` lists them).

The call goes to the router directly, not through LiteLLM's `gpu_unload.py` hook: the slot caches of `slot_cache.py` are **not** saved first, so the
next request of an unloaded model reads its prompt again. Use the hook (`X-Llama-Unload: all`) where everything should go and the cache matters.

Through LiteLLM (the body is passed on):

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu/llama/unload"
      target: "http://gpu-control:8000/llama/unload"
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```
```sh
curl -X POST http://litellm:4000/gpu/llama/unload -H "Authorization: Bearer $LITELLM_KEY" \
  -H "Content-Type: application/json" -d '{"keep": ["joi-4b"]}'
```

In a Marginalia GPU step the body can take the model of the AI the job is for: `{"keep": ["{{model | strip:openai/}}"]}` (the AI's model goes
through LiteLLM as `openai/<name>`, the router knows only `<name>`; see *Variables in the steps* of the GPU page of the manual).

## Making sure a server is alive

`start` is already safe to repeat (a running container is fine), but it calls Docker every time and does not notice a container whose service died inside.
`POST /<name>/ensure` checks the health address first and answers at once when the service answers; a step that runs before every job (a GPU lock's
*Steps before every job*) costs one health check. A running container whose service does not answer gets `ENSURE_GRACE` seconds (it may still be starting)
and is then restarted, which the socket proxy allows only with `ALLOW_RESTARTS=1` (it also allows kill, not create or exec).
The LiteLLM pass-through is one more entry like the others (`/gpu/comfy/ensure` → `http://gpu-control:8000/comfyui-amd/ensure`).

## Safety

The Docker socket is **root on the host**. Keep this service on the internal network of the compose project (the compose file publishes only the web page, on the host itself unless you set `GPU_CONTROL_BIND`), give it a long random
`GPU_CONTROL_TOKEN`, and name only the containers it may touch. Better than mounting the socket into it: a socket proxy that allows only what is needed.

## Running it (compose)

`docker-compose.yml` here runs the controller and a **socket proxy** (`tecnativa/docker-socket-proxy`) that lets it do only what it needs: start, stop and restart containers
by name (`POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, `ALLOW_RESTARTS=1`) and nothing else. The settings are not obvious, from the proxy's `haproxy.cfg`: a POST is refused unless `POST=1` (so
`POST=0` with `ALLOW_STOP=1` refuses the stop: the first version of this file did, and the controller got a 403), and `CONTAINERS=1` would allow **every** `/containers/...`
path, create and exec included, which is root on the host, so it stays `0`. The controller therefore cannot inspect a container: it waits for the **health address** of the
service (`GPU_CONTROL_CONTAINERS`, `name=address`) after a start, and `status` reports the state as unknown. Nothing is published on the host. Checked against a stand-in
Docker daemon (`test_gpu_control.py`, including one that refuses inspect); the first version of the compose file was run on the author's server (that is where the 403 came from), this one is not yet confirmed there. Check that the proxy refuses the dangerous
calls (this should print `403`):

```sh
docker run --rm --network gpu-control_control curlimages/curl -s -o /dev/null -w '%{http_code}\n' -X POST http://docker-socket-proxy:2375/containers/create
```

```sh
docker network create ai                  # once: shared by LiteLLM, ComfyUI, the TTS server and the controller
cp .env.example .env                      # then set GPU_CONTROL_TOKEN (a long random string) and the containers
docker compose up -d --build
```

What the other containers need so that the controller and LiteLLM can reach them by name (their own compose files):

```yaml
# ComfyUI (container_name comfyui-amd already): join the network, and merge the two CLI_ARGS (the later one replaces the earlier); a complete file: comfyui.docker-compose.example.yml
services:
  comfyui:
    networks: [default, ai]
    environment:
      - CLI_ARGS=--listen 0.0.0.0 --gpu-only
networks:
  ai:
    external: true
```
```yaml
# the Chatterbox TTS server (devnen/Chatterbox-TTS-Server): a container_name and the network
services:
  chatterbox-tts-server:
    container_name: chatterbox
    networks: [default, ai]
networks:
  ai:
    external: true
```
LiteLLM joins the same network (`networks: [default, ai]` in its compose file) and relays the controller (its key is checked, the controller's token is added by LiteLLM):

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu/comfy/start"
      target: "http://gpu-control:8000/comfyui-amd/start"
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
    - path: "/gpu/comfy/stop"
      target: "http://gpu-control:8000/comfyui-amd/stop"
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```
(the same two for `chatterbox` if you want to stop that container too instead of `/api/unload`). Why stop ComfyUI at all, when `/free` exists: an **idle ComfyUI holds about 1.7 GiB** of the card
(measured with `/system_stats`), which a language model of about 31 GiB cannot share; `docker stop` frees all of it, and the controller waits for the container to be ready again on start.

A plugin then calls `POST http://litellm:4000/gpu/comfy/start` before generating sound effects and `.../stop` after, with the key of the proxy; the speech goes to
`http://litellm:4000/v1/audio/speech` (model `chatterbox`, `voice`, `response_format`: the route passes the body and the binary answer through unchanged, checked with a stand-in server).
