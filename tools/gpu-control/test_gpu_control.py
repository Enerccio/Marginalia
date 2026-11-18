"""Tests of gpu_control.py against a stand-in for the Docker daemon (a Unix socket) and a stand-in for the service in the container.

    python3 -m unittest tools/gpu-control/test_gpu_control.py
"""
import json
import os
import socketserver
import tempfile
import threading
import time
import unittest
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer, ThreadingHTTPServer

os.environ["GPU_CONTROL_TOKEN"] = "secret"
os.environ["START_TIMEOUT"] = "5"
import gpu_control  # noqa: E402


class FakeDocker(socketserver.ThreadingMixIn, socketserver.UnixStreamServer):
    daemon_threads = True

    def __init__(self, path):
        self.containers = {"chatterbox": {"status": "exited", "ready_at": 0.0}}
        self.calls = []
        self.inspect_forbidden = False
        super().__init__(path, FakeDockerHandler)


class FakeDockerHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.0"

    def log_message(self, *a):
        pass

    def answer(self, code, body=None):
        data = json.dumps(body).encode() if body is not None else b""
        self.send_response(code)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        self.server.calls.append(("GET", self.path))
        if self.server.inspect_forbidden:
            return self.answer(403, {"message": "forbidden"})
        name = self.path.split("/")[2]
        c = self.server.containers.get(name)
        if c is None:
            return self.answer(404, {"message": "No such container"})
        self.answer(200, {"State": {"Status": c["status"]}})

    def do_POST(self):
        self.server.calls.append(("POST", self.path))
        name, action = self.path.split("?")[0].split("/")[2:4]
        c = self.server.containers.get(name)
        if c is None:
            return self.answer(404, {"message": "No such container"})
        if action == "start":
            if c["status"] == "running":
                return self.answer(304)
            c["status"], c["ready_at"] = "running", time.monotonic() + 0.5   # the service needs a moment
            return self.answer(204)
        if action == "restart":
            c["status"], c["ready_at"], c["hung"] = "running", time.monotonic() + 0.3, False
            return self.answer(204)
        if action == "stop":
            if c["status"] != "running":
                return self.answer(304)
            c["status"] = "exited"
            return self.answer(204)
        self.answer(404)


class ServiceHandler(BaseHTTPRequestHandler):
    """The health url of the service in the container: 200 once the container says it is ready."""
    docker = None

    def log_message(self, *a):
        pass

    def do_GET(self):
        c = self.docker.containers["chatterbox"]
        ok = c["status"] == "running" and time.monotonic() >= c["ready_at"] and not c.get("hung")
        self.send_response(200 if ok else 503)
        self.send_header("Content-Length", "0")
        self.end_headers()


class FakeRouter(ThreadingHTTPServer):
    """A llama-server in router mode: GET /models, POST /models/unload (the model leaves memory after a moment)."""

    def __init__(self):
        self.models = {}
        self.unloads = []
        self.refuse = False
        self.lock = threading.Lock()
        super().__init__(("127.0.0.1", 0), FakeRouterHandler)

    def state(self, name):
        with self.lock:
            m = self.models[name]
            if m["status"] == "unloading" and time.monotonic() >= m["gone_at"]:
                m["status"] = "unloaded"
            return m["status"]


class FakeRouterHandler(BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

    def answer(self, code, body):
        data = json.dumps(body).encode()
        self.send_response(code)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        if self.path != "/models":
            return self.answer(404, {})
        self.answer(200, {"data": [{"id": n, "status": {"value": self.server.state(n)}} for n in list(self.server.models)]})

    def do_POST(self):
        body = json.loads(self.rfile.read(int(self.headers["Content-Length"])))
        name = body["model"]
        self.server.unloads.append((name, self.headers.get("Authorization")))
        if self.server.refuse:
            return self.answer(500, {"error": "no"})
        if self.server.state(name) not in ("loaded", "loading"):
            return self.answer(400, {"error": "model is not loaded"})
        with self.server.lock:
            self.server.models[name].update(status="unloading", gone_at=time.monotonic() + 0.3)
        self.answer(200, {"success": True})


class GpuControlTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.dir = tempfile.mkdtemp()
        cls.docker = FakeDocker(os.path.join(cls.dir, "docker.sock"))
        threading.Thread(target=cls.docker.serve_forever, daemon=True).start()
        ServiceHandler.docker = cls.docker
        cls.service = ThreadingHTTPServer(("127.0.0.1", 0), ServiceHandler)
        threading.Thread(target=cls.service.serve_forever, daemon=True).start()
        gpu_control.DOCKER_SOCKET = os.path.join(cls.dir, "docker.sock")
        os.environ["GPU_CONTROL_CONTAINERS"] = f"chatterbox=http://127.0.0.1:{cls.service.server_port}/health"
        cls.router = FakeRouter()
        threading.Thread(target=cls.router.serve_forever, daemon=True).start()
        os.environ["GPU_CONTROL_LLAMA"] = f"http://127.0.0.1:{cls.router.server_port}/v1"
        cls.control = ThreadingHTTPServer(("127.0.0.1", 0), gpu_control.Handler)
        threading.Thread(target=cls.control.serve_forever, daemon=True).start()
        cls.base = f"http://127.0.0.1:{cls.control.server_port}"

    @classmethod
    def tearDownClass(cls):
        for server in (cls.control, cls.service, cls.docker, cls.router):
            server.shutdown()

    def call(self, method, path, token="secret", body=None):
        headers = {"X-Control-Token": token} if token else {}
        data = json.dumps(body).encode() if body is not None else None
        request = urllib.request.Request(self.base + path, data=data, method=method, headers=headers)
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                return response.status, json.loads(response.read())
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read())

    def setUp(self):
        self.docker.containers["chatterbox"].update(status="exited", ready_at=0.0, hung=False)
        self.docker.calls.clear()
        self.docker.inspect_forbidden = False
        self.router.models = {"big-32b": {"status": "loaded"}, "joi-4b": {"status": "loaded"},
                              "embed": {"status": "unloaded"}, "other": {"status": "loading"}}
        self.router.unloads.clear()
        self.router.refuse = False
        os.environ.pop("GPU_CONTROL_LLAMA_KEY", None)

    def test_start_waits_until_the_service_answers(self):
        started = time.monotonic()
        code, body = self.call("POST", "/chatterbox/start")
        self.assertEqual(200, code)
        self.assertTrue(body["ready"])
        self.assertGreaterEqual(time.monotonic() - started, 0.4)      # not when the container started, when the service was ready

    def test_start_of_a_running_container_is_fine(self):
        self.call("POST", "/chatterbox/start")
        code, _ = self.call("POST", "/chatterbox/start")
        self.assertEqual(200, code)

    def test_stop_returns_when_it_has_exited(self):
        self.call("POST", "/chatterbox/start")
        code, body = self.call("POST", "/chatterbox/stop")
        self.assertEqual((200, "exited"), (code, body["status"]))
        self.assertEqual("exited", self.docker.containers["chatterbox"]["status"])
        self.assertEqual(200, self.call("POST", "/chatterbox/stop")[0])        # stopping a stopped one is fine

    def test_status(self):
        self.assertEqual((200, False), (lambda r: (r[0], r[1]["ready"]))(self.call("GET", "/chatterbox/status")))
        self.call("POST", "/chatterbox/start")
        self.assertTrue(self.call("GET", "/chatterbox/status")[1]["ready"])

    def test_a_bad_token_is_refused_and_nothing_is_called(self):
        self.assertEqual(401, self.call("POST", "/chatterbox/stop", token="nope")[0])
        self.assertEqual(401, self.call("POST", "/chatterbox/stop", token=None)[0])
        self.assertEqual([], self.docker.calls)

    def test_only_the_listed_containers(self):
        self.assertEqual(404, self.call("POST", "/postgres/stop")[0])
        self.assertEqual(404, self.call("POST", "/..%2Fetc/stop")[0])
        self.assertEqual([], self.docker.calls)

    def test_a_start_that_never_gets_ready_times_out(self):
        self.docker.containers["chatterbox"]["status"] = "exited"
        old = gpu_control.START_TIMEOUT
        gpu_control.START_TIMEOUT = 1
        try:
            self.docker.containers["chatterbox"]["ready_at"] = 0.0
            original = gpu_control.answers
            gpu_control.answers = lambda url: False
            code, _ = self.call("POST", "/chatterbox/start")
        finally:
            gpu_control.START_TIMEOUT = old
            gpu_control.answers = original
        self.assertEqual(504, code)

    def test_a_proxy_that_does_not_show_containers_still_starts_and_stops(self):
        self.docker.inspect_forbidden = True               # the safe socket proxy: POST start / stop allowed, GET /containers/... denied
        started = time.monotonic()
        code, body = self.call("POST", "/chatterbox/start")
        self.assertEqual(200, code)
        self.assertGreaterEqual(time.monotonic() - started, 0.4)      # the health address of the service decided
        self.assertEqual("running", self.docker.containers["chatterbox"]["status"])
        self.assertEqual(200, self.call("POST", "/chatterbox/stop")[0])
        self.assertEqual("exited", self.docker.containers["chatterbox"]["status"])

    def test_docker_down_is_a_502(self):
        old = gpu_control.DOCKER_SOCKET
        gpu_control.DOCKER_SOCKET = os.path.join(self.dir, "missing.sock")
        try:
            self.assertEqual(502, self.call("POST", "/chatterbox/stop")[0])
        finally:
            gpu_control.DOCKER_SOCKET = old

    def posts(self):
        return [path.split("/")[3].split("?")[0] for method, path in self.docker.calls if method == "POST"]

    def test_ensure_does_nothing_when_the_service_answers(self):
        self.call("POST", "/chatterbox/start")
        self.docker.calls.clear()
        code, body = self.call("POST", "/chatterbox/ensure")
        self.assertEqual((200, "none"), (code, body["action"]))
        self.assertEqual([], self.posts())                               # no docker call at all

    def test_ensure_starts_a_stopped_container_and_waits_for_it(self):
        started = time.monotonic()
        code, body = self.call("POST", "/chatterbox/ensure")
        self.assertEqual((200, "started", True), (code, body["action"], body["ready"]))
        self.assertGreaterEqual(time.monotonic() - started, 0.4)
        self.assertEqual(["start"], self.posts())

    def test_ensure_restarts_a_running_container_whose_service_is_dead(self):
        self.call("POST", "/chatterbox/start")
        self.docker.containers["chatterbox"]["hung"] = True
        self.docker.calls.clear()
        old = gpu_control.ENSURE_GRACE
        gpu_control.ENSURE_GRACE = 0.3
        try:
            code, body = self.call("POST", "/chatterbox/ensure")
        finally:
            gpu_control.ENSURE_GRACE = old
        self.assertEqual((200, "restarted"), (code, body["action"]))
        self.assertEqual(["start", "restart"], self.posts())

    def test_ensure_through_a_proxy_without_inspect(self):
        self.docker.inspect_forbidden = True
        code, body = self.call("POST", "/chatterbox/ensure")
        self.assertEqual((200, "started"), (code, body["action"]))
        self.docker.calls.clear()
        self.assertEqual("none", self.call("POST", "/chatterbox/ensure")[1]["action"])
        self.assertEqual([], self.posts())

    def test_llama_unload_keeps_the_named_models_and_waits_for_the_memory(self):
        started = time.monotonic()
        code, body = self.call("POST", "/llama/unload", body={"keep": ["joi-4b"]})
        self.assertEqual(200, code)
        self.assertEqual(["big-32b", "other"], sorted(body["unloaded"]))
        self.assertEqual(["joi-4b"], body["kept"])
        self.assertGreaterEqual(time.monotonic() - started, 0.25)      # answered once the router said they left memory
        self.assertEqual("unloaded", self.router.state("big-32b"))
        self.assertEqual("loaded", self.router.state("joi-4b"))
        self.assertNotIn("embed", [n for n, _ in self.router.unloads])  # not in memory: no call

    def test_llama_unload_before_every_job_is_cheap_when_only_the_kept_model_is_loaded(self):
        self.router.models = {"big-32b": {"status": "unloaded"}, "joi-4b": {"status": "loaded"}}
        started = time.monotonic()
        code, body = self.call("POST", "/llama/unload", body={"keep": ["joi-4b"]})
        self.assertEqual((200, [], ["joi-4b"]), (code, body["unloaded"], body["kept"]))
        self.assertLess(time.monotonic() - started, 0.25)          # no unload, no wait
        self.assertEqual([], self.router.unloads)
        self.assertEqual("loaded", self.router.state("joi-4b"))

    def test_llama_keep_in_the_query_and_several_names(self):
        code, body = self.call("POST", "/llama/unload?keep=joi-4b,big-32b")
        self.assertEqual((200, ["other"]), (code, body["unloaded"]))
        self.assertEqual(["big-32b", "joi-4b"], sorted(body["kept"]))

    def test_llama_unload_without_keep_unloads_everything(self):
        code, body = self.call("POST", "/llama/unload")
        self.assertEqual((200, ["big-32b", "joi-4b", "other"]), (code, sorted(body["unloaded"])))
        self.assertEqual([], body["kept"])

    def test_llama_unload_sends_the_key(self):
        os.environ["GPU_CONTROL_LLAMA_KEY"] = "llama-key"
        self.call("POST", "/llama/unload", body={"keep": ["joi-4b"]})
        self.assertEqual({"Bearer llama-key"}, {auth for _, auth in self.router.unloads})

    def test_llama_bad_keep_is_a_400_and_nothing_is_unloaded(self):
        self.assertEqual(400, self.call("POST", "/llama/unload", body={"keep": [1]})[0])
        self.assertEqual([], self.router.unloads)

    def test_llama_a_refused_unload_is_a_502(self):
        self.router.refuse = True
        self.assertEqual(502, self.call("POST", "/llama/unload", body={"keep": ["joi-4b"]})[0])

    def test_llama_memory_that_is_not_freed_is_a_504(self):
        old = gpu_control.UNLOAD_TIMEOUT
        gpu_control.UNLOAD_TIMEOUT = 0.5
        self.router.models["big-32b"] = {"status": "unloading", "gone_at": time.monotonic() + 60}
        try:
            self.assertEqual(504, self.call("POST", "/llama/unload", body={"keep": ["joi-4b"]})[0])
        finally:
            gpu_control.UNLOAD_TIMEOUT = old

    def test_llama_models_and_the_token(self):
        code, body = self.call("GET", "/llama/models")
        self.assertEqual(200, code)
        self.assertEqual("loaded", {m["id"]: m["status"] for m in body["models"]}["joi-4b"])
        self.assertEqual(401, self.call("POST", "/llama/unload", token="nope")[0])
        self.assertEqual([], self.router.unloads)


if __name__ == "__main__":
    unittest.main()
