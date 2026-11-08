"""Starts and stops containers on request, so that the models of a GPU can take turns (stdlib only, no dependencies).

    POST /<name>/start     starts the container and answers when it is ready (200), or 504 after START_TIMEOUT seconds
    POST /<name>/stop      stops it and answers when it has exited (200): its memory is free
    GET  /<name>/status    {"name": ..., "status": "running" | "exited" | ..., "ready": true | false}

Only the containers named in GPU_CONTROL_CONTAINERS are touched, and every call needs the header `X-Control-Token`.
The container API is the Docker socket (DOCKER_SOCKET, default /var/run/docker.sock) mounted into this container. That socket is
root on the host: keep this service on the internal network of the compose project, never publish its port, and prefer
a socket proxy that allows only containers (DOCKER_HOST=http://socket-proxy:2375, see README.md).

Environment:
  GPU_CONTROL_TOKEN       the secret of the callers (required; LiteLLM sends it with `headers` of its pass_through_endpoints entry)
  GPU_CONTROL_CONTAINERS  comma separated, `name` or `name=http://host:port/health`: the address that answers 200 when the
                          service in it is ready (without one, the health check of the container is used, else it is ready
                          when it runs)
  GPU_CONTROL_PORT        default 8000
  START_TIMEOUT           seconds to wait for a start, default 180
  STOP_TIMEOUT            seconds Docker waits before it kills the container, default 30
"""
import hmac
import http.client
import json
import logging
import os
import re
import socket
import time
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse

log = logging.getLogger("gpu_control")

TOKEN = os.environ.get("GPU_CONTROL_TOKEN", "")
START_TIMEOUT = float(os.environ.get("START_TIMEOUT", "180"))
STOP_TIMEOUT = int(os.environ.get("STOP_TIMEOUT", "30"))
DOCKER_SOCKET = os.environ.get("DOCKER_SOCKET", "/var/run/docker.sock")
DOCKER_HOST = os.environ.get("DOCKER_HOST", "")          # http://host:port of a socket proxy, instead of the socket
NAME = re.compile(r"^[A-Za-z0-9][A-Za-z0-9_.-]*$")


def containers():
    """{name: health url or None} from GPU_CONTROL_CONTAINERS."""
    found = {}
    for item in os.environ.get("GPU_CONTROL_CONTAINERS", "").split(","):
        name, _, health = item.strip().partition("=")
        if name:
            found[name] = health or None
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
    if info is None or info["status"] != "running":
        return False
    if health_url:
        return answers(health_url)
    return info["health"] in (None, "healthy")


def start(name, health_url):
    status, _ = docker("POST", f"/containers/{name}/start")
    if status not in (204, 304):
        raise RuntimeError(f"docker answered {status} to the start of {name}")
    deadline = time.monotonic() + START_TIMEOUT
    while time.monotonic() < deadline:
        if ready(name, health_url):
            return True
        time.sleep(1)
    return False


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

    def handle_any(self, method):
        if not TOKEN or not hmac.compare_digest(self.headers.get("X-Control-Token", ""), TOKEN):
            return self.reply(401, {"error": "bad token"})
        parts = urlparse(self.path).path.strip("/").split("/")
        if len(parts) != 2 or not NAME.match(parts[0]):
            return self.reply(404, {"error": "use /<container>/start|stop|status"})
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
            if method == "POST" and action == "stop":
                stop(name)
                return self.reply(200, {"name": name, "status": "exited"})
            return self.reply(405, {"error": "unknown action"})
        except (OSError, RuntimeError) as e:
            log.warning("%s %s: %s", action, name, e)
            return self.reply(502, {"error": str(e)})

    def do_POST(self):
        self.handle_any("POST")

    def do_GET(self):
        self.handle_any("GET")


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    if not TOKEN:
        raise SystemExit("GPU_CONTROL_TOKEN is not set")
    if not containers():
        raise SystemExit("GPU_CONTROL_CONTAINERS is empty: nothing to control")
    port = int(os.environ.get("GPU_CONTROL_PORT", "8000"))
    log.info("controls %s on port %d", ", ".join(containers()), port)
    ThreadingHTTPServer(("0.0.0.0", port), Handler).serve_forever()
