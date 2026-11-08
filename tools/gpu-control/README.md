# gpu-control

A small service (Python standard library only) that starts and stops containers on request, so that the models that share one GPU can take turns:
a speech server (Chatterbox) or an image server (ComfyUI) is **stopped** while the language model is loaded, and **started** (and waited for) when a
plugin needs it. Stopping a container frees its memory completely, which an "unload" call of the server inside does not always do. Used through
LiteLLM, so that one key is all a plugin needs: see *Freeing the GPU for another model* in `manual/user/prompt-cache.md`.

| Call | Does |
|---|---|
| `POST /<name>/start` | starts the container and answers 200 when the service in it is ready (504 after `START_TIMEOUT`) |
| `POST /<name>/stop` | stops it and answers 200 when it has exited |
| `GET /<name>/status` | `{"name", "status", "ready"}` |

Every call needs the header `X-Control-Token`. Only the containers of `GPU_CONTROL_CONTAINERS` can be touched (`name` or `name=http://host:port/health`, the
address that answers 200 when the service is ready; without one the container's own health check is used, else a running container is ready).
`gpu_control.py` documents the environment. Tests (a stand-in for the Docker daemon): `cd tools/gpu-control && python3 -m unittest test_gpu_control`.

## Safety

The Docker socket is **root on the host**. Keep this service on the internal network of the compose project (no `ports:`), give it a long random
`GPU_CONTROL_TOKEN`, and name only the containers it may touch. Better than mounting the socket into it: a socket proxy that allows only what is needed.

## Running it (compose)

`docker-compose.yml` here runs the controller and a **socket proxy** (`tecnativa/docker-socket-proxy`) that lets it do only what it needs: start and stop containers
by name (`POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`) and nothing else. The settings are not obvious, from the proxy's `haproxy.cfg`: a POST is refused unless `POST=1` (so
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
