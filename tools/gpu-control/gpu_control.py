"""Starts and stops containers on request, so that the models of a GPU can take turns (stdlib only, no dependencies).

    POST /<name>/start     starts the container and answers when it is ready (200), or 504 after START_TIMEOUT seconds
    POST /<name>/stop      stops it and answers when it has exited (200): its memory is free
    POST /<name>/ensure    nothing when the service in it answers; else starts the container, or restarts it when it runs but the service
                           does not answer within ENSURE_GRACE seconds; 200 with "action": "none" | "started" | "restarted" once it is ready
    GET  /<name>/status    {"name": ..., "status": "running" | "exited" | ..., "ready": true | false}
    POST /llama/unload     unloads every model of the llama-server routers of GPU_CONTROL_LLAMA except the ones in `keep`
                           (JSON body {"keep": ["a", "b"]} or ?keep=a,b, none: unload all) and answers when their memory is free
                           (200 {"unloaded": [...], "kept": [...]}), 504 after UNLOAD_TIMEOUT seconds
    GET  /llama/models     {"models": [{"server", "id", "status"}]}: what the routers report
    POST /llama/load       loads the models named in the JSON body {"models": ["a"]} (or ?models=a,b) into the routers that list them and answers
                           when they are loaded (200 {"loaded": [...]}), 400 for a name no router lists, 504 after LOAD_TIMEOUT seconds;
                           it does not unload anything: make room first (unload) or the router may refuse for lack of memory
    POST /<name>/restart   restarts the container and answers when it is ready (200), or 504 after START_TIMEOUT
    POST /llama/unload     also takes {"only": ["a"]} (or ?only=a): unloads just those models, everything else stays as it is
    GET  /overview         containers, llama models and the video memory of the card in one answer (what the web page shows)
    GET  /                 the web page (ui.html: no data in it, it asks for the token and calls the routes above with it)

Only the containers named in GPU_CONTROL_CONTAINERS are touched, and every call needs the header `X-Control-Token`.
`unload` and `models` are not container actions, so a container may still be called `llama`.
The container API is the Docker socket (DOCKER_SOCKET, default /var/run/docker.sock) mounted into this container. That socket is
root on the host: keep this service on the internal network of the compose project, publish its port only on the host itself
(or behind TLS: the web page asks for the token over plain HTTP), and prefer a socket proxy that allows only containers (DOCKER_HOST=http://socket-proxy:2375, see README.md).

Environment:
  GPU_CONTROL_TOKEN       the secret of the callers (required; LiteLLM sends it with `headers` of its pass_through_endpoints entry)
  GPU_CONTROL_CONTAINERS  comma separated, `name` or `name=http://host:port/health`: the address that answers 200 when the
                          service in it is ready (without one, the health check of the container is used, else it is ready
                          when it runs). Give every container one when the socket proxy does not allow inspect (the safe setting).
  GPU_CONTROL_LLAMA       comma separated roots of llama-server instances in router mode (http://llama-server:8080, not /v1)
  GPU_CONTROL_LLAMA_KEY   the --api-key of those servers, if they have one (sent as a bearer token)
  UNLOAD_TIMEOUT          seconds to wait until the unloaded models are out of memory, default 120
  LOAD_TIMEOUT            seconds to wait until a model is loaded, default 300
  GPU_CONTROL_PORT        default 8000
  START_TIMEOUT           seconds to wait for a start, default 180
  STOP_TIMEOUT            seconds Docker waits before it kills the container, default 30
  ENSURE_GRACE            seconds an ensure waits for a running container whose service does not answer before it restarts it, default 30
                          (the socket proxy then needs ALLOW_RESTARTS=1)
"""
import hmac
import http.client
import json
import logging
import os
import re
import glob
import socket
import threading
import time
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

log = logging.getLogger("gpu_control")

TOKEN = os.environ.get("GPU_CONTROL_TOKEN", "")
START_TIMEOUT = float(os.environ.get("START_TIMEOUT", "180"))
STOP_TIMEOUT = int(os.environ.get("STOP_TIMEOUT", "30"))
DOCKER_SOCKET = os.environ.get("DOCKER_SOCKET", "/var/run/docker.sock")
DOCKER_HOST = os.environ.get("DOCKER_HOST", "")          # http://host:port of a socket proxy, instead of the socket
ENSURE_GRACE = float(os.environ.get("ENSURE_GRACE", "30"))
UNLOAD_TIMEOUT = float(os.environ.get("UNLOAD_TIMEOUT", "120"))
LOAD_TIMEOUT = float(os.environ.get("LOAD_TIMEOUT", "300"))
# the states of a model in the router's /models that still hold its memory, and the ones worth an unload call
IN_MEMORY = ("loaded", "loading", "unloading")
TO_UNLOAD = ("loaded", "loading", "unknown")
LLAMA_LOCK = threading.Lock()         # one unload at a time: two callers keeping different models must not interleave
SYSFS_DRM = os.environ.get("SYSFS_DRM", "/sys/class/drm")      # where the amdgpu driver shows the video memory of the cards
UI_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ui.html")
NAME = re.compile(r"^[A-Za-z0-9][A-Za-z0-9_.-]*$")


def containers():
    """{name: health url or None} from GPU_CONTROL_CONTAINERS."""
    found = {}
    for item in os.environ.get("GPU_CONTROL_CONTAINERS", "").split(","):
        name, _, health = item.strip().partition("=")
        if name:
            found[name] = health or None
    return found


class StillInMemory(RuntimeError):
    """The routers still report an unloaded model in memory after UNLOAD_TIMEOUT."""


def in_memory(root, name):
    return any(m["id"] == name and status_of(m) in IN_MEMORY for m in llama_models(root))


def llama_servers():
    """The router roots of GPU_CONTROL_LLAMA (a trailing /v1 is dropped: the router's own endpoints are at the root)."""
    found = []
    for item in os.environ.get("GPU_CONTROL_LLAMA", "").split(","):
        root = item.strip().rstrip("/")
        if root.endswith("/v1"):
            root = root[:-len("/v1")]
        if root and root not in found:
            found.append(root)
    return found


def llama_call(method, url, body=None, timeout=15):
    headers = {"Content-Type": "application/json"}
    key = os.environ.get("GPU_CONTROL_LLAMA_KEY", "")
    if key:
        headers["Authorization"] = "Bearer " + key
    data = json.dumps(body).encode() if body is not None else None
    request = urllib.request.Request(url, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            raw = response.read()
    except urllib.error.HTTPError as e:
        raise RuntimeError(f"{url} answered {e.code}") from e
    try:
        return json.loads(raw) if raw else None
    except ValueError:
        return None


def status_of(model):
    """The state a router reports for a model: `{"status": {"value": "loaded"}}` (or a plain string); "unknown" without one."""
    raw = model.get("status")
    return str(raw.get("value", "unknown") if isinstance(raw, dict) else raw or "unknown")


def llama_models(root):
    body = llama_call("GET", f"{root}/models")
    listed = body.get("data", []) if isinstance(body, dict) else body if isinstance(body, list) else []
    return [m for m in listed if isinstance(m, dict) and m.get("id")]


def llama_unload(keep, only=None):
    """Unloads every model of every router that is in memory and not in `keep` (with `only`: just the ones named there, the rest is
    left alone), then waits until the routers report them out of memory. Returns (unloaded, kept): the names unloaded and the kept ones
    that were in memory."""
    unloaded, kept, waiting = [], [], []
    for root in llama_servers():
        for model in llama_models(root):
            name, state = model["id"], status_of(model)
            if only is not None and name not in only:
                continue
            if name in keep:
                if state in IN_MEMORY:
                    kept.append(name)
                continue
            if state in TO_UNLOAD:
                try:
                    llama_call("POST", f"{root}/models/unload", {"model": name}, timeout=UNLOAD_TIMEOUT)
                except RuntimeError:
                    if in_memory(root, name):      # refused while it holds memory: a real error (else it went away by itself meanwhile)
                        raise
                unloaded.append(name)
            if state in TO_UNLOAD or state == "unloading":
                waiting.append((root, name))
    deadline = time.monotonic() + UNLOAD_TIMEOUT
    while waiting:
        waiting = [(root, name) for root, name in waiting if in_memory(root, name)]
        if not waiting:
            break
        if time.monotonic() > deadline:
            raise StillInMemory(", ".join(name for _, name in waiting) + f" still in memory after {UNLOAD_TIMEOUT:.0f} s")
        time.sleep(1)
    return unloaded, kept


class UnknownModel(ValueError):
    """A model that no router lists."""


def llama_load(names):
    """Loads the named models into the routers that list them and waits until they report `loaded`. Returns the names loaded now
    (already loaded ones are not asked again). A model that goes back to `unloaded` while loading failed to load: RuntimeError."""
    wanted = []
    for root in llama_servers():
        listed = {m["id"]: status_of(m) for m in llama_models(root)}
        wanted += [(root, name, listed[name]) for name in sorted(names) if name in listed]
    missing = names - {name for _, name, _ in wanted}
    if missing:
        raise UnknownModel("no router lists " + ", ".join(sorted(missing)))
    loaded, waiting = [], []
    for root, name, state in wanted:
        if state == "loaded":
            continue
        if state not in ("loading",):
            llama_call("POST", f"{root}/models/load", {"model": name}, timeout=LOAD_TIMEOUT)
        waiting.append((root, name))
        loaded.append(name)
    deadline = time.monotonic() + LOAD_TIMEOUT
    while waiting:
        states = {(root, name): next((status_of(m) for m in llama_models(root) if m["id"] == name), "unknown") for root, name in waiting}
        failed = [name for (root, name), state in states.items() if state in ("unloaded", "unknown", "failed")]
        if failed:
            raise RuntimeError(", ".join(failed) + " did not load")
        waiting = [key for key, state in states.items() if state != "loaded"]
        if not waiting:
            break
        if time.monotonic() > deadline:
            raise StillLoading(", ".join(name for _, name in waiting) + f" not loaded after {LOAD_TIMEOUT:.0f} s")
        time.sleep(1)
    return loaded


class StillLoading(RuntimeError):
    """A model is still loading after LOAD_TIMEOUT."""


def name_list(field, query, body):
    """The names of `field` (`keep` or `only`): ?keep=a,b (repeatable) and/or a JSON body {"keep": [...]} (or a comma separated string).
    None when the field is not there at all."""
    names, present = [], False
    for value in parse_qs(query).get(field, []):
        names += value.split(",")
        present = True
    if body:
        try:
            parsed = json.loads(body)
        except ValueError:
            raise ValueError("the body is not JSON")
        value = parsed.get(field) if isinstance(parsed, dict) else None
        if isinstance(value, str):
            value = value.split(",")
        if value is not None:
            present = True
            if not isinstance(value, list) or not all(isinstance(v, str) for v in value):
                raise ValueError('use {"%s": ["model", ...]}' % field)
            names += value
    return {n.strip() for n in names if n.strip()} if present else None


def keep_list(query, body):
    return name_list("keep", query, body) or set()


def vram():
    """The video memory of the cards the kernel shows in sysfs (the amdgpu driver: mem_info_vram_used / mem_info_vram_total, in bytes),
    [{"card", "used", "total"}]; nothing for a card that does not show it (NVIDIA, or a container without /sys)."""
    found = []
    for path in sorted(glob.glob(os.path.join(SYSFS_DRM, "card[0-9]*", "device"))):
        try:
            with open(os.path.join(path, "mem_info_vram_used")) as f:
                used = int(f.read())
            with open(os.path.join(path, "mem_info_vram_total")) as f:
                total = int(f.read())
        except (OSError, ValueError):
            continue
        found.append({"card": os.path.basename(os.path.dirname(path)), "used": used, "total": total})
    return found


class UnixConnection(http.client.HTTPConnection):
    def __init__(self, path, timeout):
        super().__init__("localhost", timeout=timeout)
        self.path = path

    def connect(self):
        self.sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        self.sock.settimeout(self.timeout)
        self.sock.connect(self.path)


def docker(method, path, timeout=60):
    """One call to the Docker Engine API. Returns (status, parsed json or None)."""
    if DOCKER_HOST:
        url = urlparse(DOCKER_HOST)
        connection = http.client.HTTPConnection(url.hostname, url.port or 2375, timeout=timeout)
    else:
        connection = UnixConnection(DOCKER_SOCKET, timeout)
    try:
        connection.request(method, path)
        response = connection.getresponse()
        body = response.read()
        try:
            return response.status, json.loads(body) if body else None
        except ValueError:
            return response.status, None
    finally:
        connection.close()


def inspect(name):
    status, body = docker("GET", f"/containers/{name}/json")
    if status == 404:
        return None
    if status == 403:
        # a socket proxy without CONTAINERS=1 (the safe setting: that one also lets a caller create containers) does not show the
        # containers: the state is unknown and the health address decides when a container is ready
        return {"status": "unknown", "health": None}
    if status != 200 or not isinstance(body, dict):
        raise RuntimeError(f"docker answered {status} for {name}")
    state = body.get("State") or {}
    health = (state.get("Health") or {}).get("Status")
    return {"status": state.get("Status", "unknown"), "health": health}


def answers(url):
    try:
        with urllib.request.urlopen(url, timeout=5) as response:
            return 200 <= response.status < 300
    except OSError:
        return False


def ready(name, health_url):
    info = inspect(name)
    if info is None or info["status"] not in ("running", "unknown"):
        return False
    if health_url:
        return answers(health_url)
    return info["health"] in (None, "healthy")      # without a health address and without inspect: ready as soon as it was started


def wait_ready(name, health_url, seconds):
    deadline = time.monotonic() + seconds
    while True:
        if ready(name, health_url):
            return True
        if time.monotonic() >= deadline:
            return False
        time.sleep(min(1.0, max(0.05, deadline - time.monotonic())))


def start(name, health_url):
    status, _ = docker("POST", f"/containers/{name}/start")
    if status not in (204, 304):
        raise RuntimeError(f"docker answered {status} to the start of {name}")
    return wait_ready(name, health_url, START_TIMEOUT)


def ensure(name, health_url):
    """Makes sure the service answers: "none" when it already does, "started" for a stopped container, "restarted" for a running one
    whose service did not answer within ENSURE_GRACE (hung, or crashed inside). None when it is not ready after START_TIMEOUT."""
    if ready(name, health_url):
        return "none"
    status, _ = docker("POST", f"/containers/{name}/start")
    if status == 204:
        return "started" if wait_ready(name, health_url, START_TIMEOUT) else None
    if status != 304:
        raise RuntimeError(f"docker answered {status} to the start of {name}")
    # it was running: maybe still starting up, else dead inside
    if wait_ready(name, health_url, ENSURE_GRACE):
        return "none"
    log.warning("%s runs but does not answer after %.0f s: restarting it", name, ENSURE_GRACE)
    status, _ = docker("POST", f"/containers/{name}/restart?t={STOP_TIMEOUT}", timeout=STOP_TIMEOUT + 30)
    if status != 204:
        raise RuntimeError(f"docker answered {status} to the restart of {name}")
    return "restarted" if wait_ready(name, health_url, START_TIMEOUT) else None


def restart(name, health_url):
    status, _ = docker("POST", f"/containers/{name}/restart?t={STOP_TIMEOUT}", timeout=STOP_TIMEOUT + 30)
    if status != 204:
        raise RuntimeError(f"docker answered {status} to the restart of {name}")
    return wait_ready(name, health_url, START_TIMEOUT)


def stop(name):
    status, _ = docker("POST", f"/containers/{name}/stop?t={STOP_TIMEOUT}", timeout=STOP_TIMEOUT + 30)
    if status not in (204, 304):
        raise RuntimeError(f"docker answered {status} to the stop of {name}")


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args):
        log.info("%s %s", self.address_string(), fmt % args)

    def reply(self, code, body):
        data = json.dumps(body).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def page(self):
        try:
            with open(UI_FILE, "rb") as f:
                data = f.read()
        except OSError:
            return self.reply(404, {"error": "ui.html is missing"})
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Frame-Options", "DENY")
        self.send_header("Content-Security-Policy",
                         "default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; connect-src 'self'; base-uri 'none'; form-action 'none'")
        self.end_headers()
        self.wfile.write(data)

    def overview(self):
        """Everything the page shows; a container or router that fails shows its error instead of failing the whole answer."""
        found = []
        for name, health in containers().items():
            try:
                info = inspect(name)
                if info is None:
                    found.append({"name": name, "status": "missing", "ready": False})
                else:
                    found.append({"name": name, "status": info["status"], "ready": ready(name, health)})
            except (OSError, RuntimeError) as e:
                found.append({"name": name, "status": "error", "ready": False, "error": str(e)})
        models, errors = [], []
        for root in llama_servers():
            try:
                models += [{"server": root, "id": m["id"], "status": status_of(m)} for m in llama_models(root)]
            except (OSError, RuntimeError) as e:
                errors.append(f"{root}: {e}")
        return {"containers": found, "llama": {"configured": bool(llama_servers()), "models": models, "errors": errors}, "vram": vram()}

    def handle_any(self, method):
        if method == "GET" and urlparse(self.path).path in ("/", "/ui"):
            return self.page()         # no data in the page: it asks for the token and sends it with every call
        if not TOKEN or not hmac.compare_digest(self.headers.get("X-Control-Token", ""), TOKEN):
            return self.reply(401, {"error": "bad token"})
        url = urlparse(self.path)
        parts = url.path.strip("/").split("/")
        if method == "GET" and parts == ["overview"]:
            return self.reply(200, self.overview())
        if parts[0] == "llama" and len(parts) == 2 and parts[1] in ("unload", "load", "models"):
            return self.handle_llama(method, parts[1], url.query)
        if len(parts) != 2 or not NAME.match(parts[0]):
            return self.reply(404, {"error": "use /<container>/start|ensure|restart|stop|status or /llama/load|unload|models"})
        name, action = parts
        allowed = containers()
        if name not in allowed:
            return self.reply(404, {"error": f"{name} is not one of GPU_CONTROL_CONTAINERS"})
        try:
            if method == "GET" and action == "status":
                info = inspect(name)
                if info is None:
                    return self.reply(404, {"error": f"no container {name}"})
                return self.reply(200, {"name": name, "status": info["status"], "ready": ready(name, allowed[name])})
            if method == "POST" and action == "start":
                if start(name, allowed[name]):
                    return self.reply(200, {"name": name, "status": "running", "ready": True})
                return self.reply(504, {"error": f"{name} is not ready after {START_TIMEOUT:.0f} s"})
            if method == "POST" and action == "ensure":
                done = ensure(name, allowed[name])
                if done is None:
                    return self.reply(504, {"error": f"{name} is not ready after {START_TIMEOUT:.0f} s"})
                return self.reply(200, {"name": name, "status": "running", "ready": True, "action": done})
            if method == "POST" and action == "restart":
                if restart(name, allowed[name]):
                    return self.reply(200, {"name": name, "status": "running", "ready": True})
                return self.reply(504, {"error": f"{name} is not ready after {START_TIMEOUT:.0f} s"})
            if method == "POST" and action == "stop":
                stop(name)
                return self.reply(200, {"name": name, "status": "exited"})
            return self.reply(405, {"error": "unknown action"})
        except (OSError, RuntimeError) as e:
            log.warning("%s %s: %s", action, name, e)
            return self.reply(502, {"error": str(e)})

    def handle_llama(self, method, action, query):
        if not llama_servers():
            return self.reply(404, {"error": "GPU_CONTROL_LLAMA is empty: no llama-server to control"})
        try:
            if method == "GET" and action == "models":
                found = [{"server": root, "id": m["id"], "status": status_of(m)} for root in llama_servers() for m in llama_models(root)]
                return self.reply(200, {"models": found})
            if method == "POST" and action == "load":
                length = int(self.headers.get("Content-Length") or 0)
                raw = self.rfile.read(length) if length else b""
                try:
                    names = name_list("models", query, raw)
                    if not names:
                        raise ValueError('use {"models": ["model", ...]}')
                    with LLAMA_LOCK:
                        loaded = llama_load(names)
                except UnknownModel as e:
                    return self.reply(404, {"error": str(e)})
                except ValueError as e:
                    return self.reply(400, {"error": str(e)})
                log.info("llama load: loaded %s", ", ".join(loaded) or "nothing (already loaded)")
                return self.reply(200, {"loaded": loaded})
            if method == "POST" and action == "unload":
                length = int(self.headers.get("Content-Length") or 0)
                raw = self.rfile.read(length) if length else b""
                try:
                    keep, only = keep_list(query, raw), name_list("only", query, raw)
                except ValueError as e:
                    return self.reply(400, {"error": str(e)})
                with LLAMA_LOCK:
                    unloaded, kept = llama_unload(keep, only)
                log.info("llama unload: unloaded %s, kept %s", ", ".join(unloaded) or "nothing", ", ".join(kept) or "nothing")
                return self.reply(200, {"unloaded": unloaded, "kept": kept})
            return self.reply(405, {"error": "unknown action"})
        except (StillInMemory, StillLoading) as e:
            log.warning("llama %s: %s", action, e)
            return self.reply(504, {"error": str(e)})
        except (OSError, RuntimeError) as e:
            log.warning("llama %s: %s", action, e)
            return self.reply(502, {"error": str(e)})

    def do_POST(self):
        self.handle_any("POST")

    def do_GET(self):
        self.handle_any("GET")


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    if not TOKEN:
        raise SystemExit("GPU_CONTROL_TOKEN is not set")
    if not containers() and not llama_servers():
        raise SystemExit("GPU_CONTROL_CONTAINERS and GPU_CONTROL_LLAMA are empty: nothing to control")
    port = int(os.environ.get("GPU_CONTROL_PORT", "8000"))
    log.info("controls %s on port %d", ", ".join(list(containers()) + llama_servers()), port)
    ThreadingHTTPServer(("0.0.0.0", port), Handler).serve_forever()
