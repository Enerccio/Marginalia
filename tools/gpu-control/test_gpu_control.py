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
        ok = self.docker.containers["chatterbox"]["status"] == "running" and time.monotonic() >= self.docker.containers["chatterbox"]["ready_at"]
        self.send_response(200 if ok else 503)
        self.send_header("Content-Length", "0")
        self.end_headers()


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
        cls.control = ThreadingHTTPServer(("127.0.0.1", 0), gpu_control.Handler)
        threading.Thread(target=cls.control.serve_forever, daemon=True).start()
        cls.base = f"http://127.0.0.1:{cls.control.server_port}"

    @classmethod
    def tearDownClass(cls):
        for server in (cls.control, cls.service, cls.docker):
            server.shutdown()

    def call(self, method, path, token="secret"):
        request = urllib.request.Request(self.base + path, method=method, headers={"X-Control-Token": token} if token else {})
        try:
            with urllib.request.urlopen(request, timeout=20) as response:
                return response.status, json.loads(response.read())
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read())

    def setUp(self):
        self.docker.containers["chatterbox"].update(status="exited", ready_at=0.0)
        self.docker.calls.clear()

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

    def test_docker_down_is_a_502(self):
        old = gpu_control.DOCKER_SOCKET
        gpu_control.DOCKER_SOCKET = os.path.join(self.dir, "missing.sock")
        try:
            self.assertEqual(502, self.call("POST", "/chatterbox/stop")[0])
        finally:
            gpu_control.DOCKER_SOCKET = old


if __name__ == "__main__":
    unittest.main()
