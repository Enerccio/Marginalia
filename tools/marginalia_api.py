#!/usr/bin/env python3
"""
Command line client of the Marginalia HTTP API (/api/v1, see manual/developer/api.md). Python standard library only.

The server and the key come from the environment, so the key is never on a command line or in the shell history:

    export MARGINALIA_URL=https://marginalia.example.org      # without /api/v1
    export MARGINALIA_API_KEY_FILE=~/.marginalia-api-key       # a file with the key (or MARGINALIA_API_KEY=mrg_...)

    python3 tools/marginalia_api.py version                    # version and commit of the running application
    python3 tools/marginalia_api.py can-hot-install            # can plugins built from this checkout go in without a full update?
    python3 tools/marginalia_api.py install marginalia/plugins/sidequery/target/sidequery-*.jar [more.jar ...]
    python3 tools/marginalia_api.py install --plugins          # every built plugin JAR (marginalia/plugins/*/target/*.jar)
    python3 tools/marginalia_api.py extensions                 # what is installed
    python3 tools/marginalia_api.py stop|start|uninstall|log sidequery.jar
    python3 tools/marginalia_api.py update-status | update | update-log
    python3 tools/marginalia_api.py books | commands
    python3 tools/marginalia_api.py run get-book-info --book <uuid> [--args '{"name": "value"}'] [--confirm] [--wait 60]
    python3 tools/marginalia_api.py run-status <run id> | cancel <run id>

can-hot-install compares the commit the server runs with HEAD of this checkout: when nothing the application is built from
(marginalia/src/main, marginalia/pom.xml, the root pom.xml) changed in between, plugins built here fit the running application
and can be installed without updating it. `install` checks that first and refuses otherwise (--force skips the check; the
server verifies every JAR anyway and rolls back one that does not fit). Uncommitted changes of the application are reported too.
The key needs the scopes of what it is used for: install / extensions / stop / start / uninstall / log need *Manage the
extensions*, update-status / update / update-log *Update the installation*, books / commands / run *Read* (or *Write*).
Exit code: 0 ok, 1 the call failed or a full update is needed, 2 a usage or configuration error.
"""
import argparse
import glob
import json
import os
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request

APP_PATHS = ["marginalia/src/main", "marginalia/pom.xml", "pom.xml"]
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def fail(message, code=2):
    print(message, file=sys.stderr)
    sys.exit(code)


def config():
    url = os.environ.get("MARGINALIA_URL")
    if not url:
        fail("Set MARGINALIA_URL (the address of the application, without /api/v1)")
    key = os.environ.get("MARGINALIA_API_KEY")
    key_file = os.environ.get("MARGINALIA_API_KEY_FILE")
    if not key and key_file:
        with open(os.path.expanduser(key_file), encoding="utf-8") as f:
            key = f.read().strip()
    if not key:
        fail("Set MARGINALIA_API_KEY_FILE (a file with the key) or MARGINALIA_API_KEY")
    return url.rstrip("/") + "/api/v1", key


def call(method, path, body=None, content_type="application/json", raw=False, timeout=600):
    base, key = config()
    data = None
    if body is not None:
        data = body if isinstance(body, bytes) else json.dumps(body).encode("utf-8")
    request = urllib.request.Request(base + path, data=data, method=method)
    request.add_header("Authorization", "Bearer " + key)
    request.add_header("User-Agent", "marginalia-api-client")
    if data is not None:
        request.add_header("Content-Type", content_type)
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            status, payload = response.status, response.read()
    except urllib.error.HTTPError as e:
        status, payload = e.code, e.read()
    except urllib.error.URLError as e:
        fail("Cannot reach %s: %s" % (base, e.reason), 1)
    text = payload.decode("utf-8", errors="replace")
    if raw:
        return status, text
    try:
        return status, json.loads(text) if text else None
    except json.JSONDecodeError:
        return status, text


def show(status, value):
    if isinstance(value, str):
        print(value)
    else:
        print(json.dumps(value, indent=2, ensure_ascii=False))
    if status >= 400:
        sys.exit(1)


def git(*args):
    return subprocess.run(["git", "-C", REPO, *args], capture_output=True, text=True)


def hot_install_check():
    """(ok, message): whether plugins built from this checkout fit the running application."""
    status, version = call("GET", "/version")
    if status != 200:
        return False, "The server answered %s: %s" % (status, version)
    running = version.get("commitFull")
    if not running:
        return False, "The server does not know its commit (a development build?), a full update is the safe way"
    if git("cat-file", "-e", running + "^{commit}").returncode != 0:
        return False, "The running commit %s is not in this checkout: git fetch first" % running[:12]
    changed = git("diff", "--name-only", running, "HEAD", "--", *APP_PATHS)
    if changed.returncode != 0:
        return False, "git diff failed: " + changed.stderr.strip()
    files = [f for f in changed.stdout.splitlines() if f]
    head = git("rev-parse", "HEAD").stdout.strip()
    lines = ["Server runs %s (%s), this checkout is at %s" % (version.get("version"), running[:12], head[:12])]
    dirty = [f for f in git("status", "--porcelain", "--", *APP_PATHS).stdout.splitlines() if f]
    if dirty:
        lines.append("Uncommitted changes of the application in this checkout:")
        lines += ["  " + f for f in dirty]
    if files:
        lines.append("The application changed since the running commit (%d files), a full update is needed:" % len(files))
        lines += ["  " + f for f in files[:40]]
        if len(files) > 40:
            lines.append("  ... and %d more" % (len(files) - 40))
        lines.append("(python3 tools/plugin_api_diff.py %s HEAD lists what plugins can see of it)" % running[:12])
        return False, "\n".join(lines)
    if dirty:
        return False, "\n".join(lines + ["Commit or stash them, plugins built now would not match the server"])
    lines.append("The application is the same: plugins built from this checkout can be installed without a full update")
    return True, "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Marginalia HTTP API client", formatter_class=argparse.RawDescriptionHelpFormatter,
                                     epilog=__doc__.split("\n\n", 1)[1])
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("version")
    sub.add_parser("me")
    sub.add_parser("can-hot-install")
    install = sub.add_parser("install")
    install.add_argument("jars", nargs="*")
    install.add_argument("--plugins", action="store_true", help="every marginalia/plugins/*/target/*.jar")
    install.add_argument("--force", action="store_true", help="skip the check that no full update is needed")
    sub.add_parser("extensions")
    for name in ("stop", "start", "uninstall", "log"):
        sub.add_parser(name).add_argument("jar", help="the JAR name of the extension, sidequery.jar")
    sub.add_parser("update-status")
    sub.add_parser("update")
    sub.add_parser("update-log")
    sub.add_parser("books")
    sub.add_parser("commands")
    run = sub.add_parser("run")
    run.add_argument("name")
    run.add_argument("--book")
    run.add_argument("--args", help="JSON: an object (by name) or an array (by position)")
    run.add_argument("--confirm", action="store_true")
    run.add_argument("--wait", type=int, default=30)
    sub.add_parser("run-status").add_argument("run")
    sub.add_parser("cancel").add_argument("run")
    args = parser.parse_args()

    if args.command == "version":
        show(*call("GET", "/version"))
    elif args.command == "me":
        show(*call("GET", "/me"))
    elif args.command == "can-hot-install":
        ok, message = hot_install_check()
        print(message)
        sys.exit(0 if ok else 1)
    elif args.command == "install":
        jars = list(args.jars)
        if args.plugins:
            jars += sorted(j for j in glob.glob(os.path.join(REPO, "marginalia/plugins/*/target/*.jar"))
                           if not j.endswith(("-sources.jar", "-javadoc.jar")))
        if not jars:
            fail("No JAR given")
        if not args.force:
            ok, message = hot_install_check()
            print(message)
            if not ok:
                fail("Not installed (--force installs anyway)", 1)
        failed = False
        for jar in jars:
            name = os.path.basename(jar)
            with open(jar, "rb") as f:
                data = f.read()
            status, value = call("PUT", "/admin/extensions/" + urllib.parse.quote(name), data, "application/java-archive")
            if status == 200:
                print("installed %s: %s %s, %s" % (name, value.get("symbolicName"), value.get("version"), value.get("state")))
            else:
                failed = True
                print("FAILED %s (%s): %s" % (name, status, value.get("error") if isinstance(value, dict) else value), file=sys.stderr)
                if isinstance(value, dict) and value.get("report"):
                    print(value["report"], file=sys.stderr)
        sys.exit(1 if failed else 0)
    elif args.command == "extensions":
        status, value = call("GET", "/admin/extensions")
        if status != 200:
            show(status, value)
        for e in value:
            flags = [e["state"]] + (["stopped"] if e.get("stopped") else []) + (["INVALID"] if e.get("valid") is False else [])
            print("%-40s %-12s %s" % (e["jar"], e.get("version") or "", ", ".join(flags)))
    elif args.command in ("stop", "start"):
        show(*call("POST", "/admin/extensions/%s/%s" % (urllib.parse.quote(args.jar), args.command)))
    elif args.command == "uninstall":
        show(*call("DELETE", "/admin/extensions/" + urllib.parse.quote(args.jar)))
    elif args.command == "log":
        show(*call("GET", "/admin/extensions/%s/log" % urllib.parse.quote(args.jar), raw=True))
    elif args.command == "update-status":
        show(*call("GET", "/admin/update"))
    elif args.command == "update":
        show(*call("POST", "/admin/update"))
    elif args.command == "update-log":
        show(*call("GET", "/admin/update/log", raw=True))
    elif args.command == "books":
        show(*call("GET", "/books"))
    elif args.command == "commands":
        show(*call("GET", "/commands"))
    elif args.command == "run":
        body = {"confirm": args.confirm, "wait": args.wait}
        if args.args:
            body["arguments"] = json.loads(args.args)
        path = ("/books/%s" % urllib.parse.quote(args.book) if args.book else "") + "/commands/" + urllib.parse.quote(args.name)
        show(*call("POST", path, body))
    elif args.command == "run-status":
        show(*call("GET", "/runs/" + urllib.parse.quote(args.run)))
    elif args.command == "cancel":
        show(*call("DELETE", "/runs/" + urllib.parse.quote(args.run)))


if __name__ == "__main__":
    main()
