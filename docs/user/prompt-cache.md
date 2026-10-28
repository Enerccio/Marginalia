# Prompt cache and session id

Model servers keep the prompt they have just read (the *prompt cache*), so that the next request that starts with the
same text doesn't have to read it again. For a long story that is the difference between waiting a few seconds and
waiting minutes. Marginalia builds its prompts so that this works (the story first, the new part last), and it can also
tell the server **what kind of work a request is**, so that a server, or a proxy in front of it, can keep a cache for each
kind.

## What is sent

Every request Marginalia makes for a kind of work carries the HTTP header

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

The value is the same for the same user doing the same kind of work, whatever book it is about, and it differs between
users and between kinds:

| Kind | Requests |
|---|---|
| generation | A new part, *Regenerate* and *Swipe*: their prompts share the story, so they share the value. |
| impersonate | *Impersonate*. |
| annotate | *Annotate with AI* and automatic annotation; the second model of the two stage mode has a value of its own. |
| redraft | *Redraft* and the redraft of automatic annotation. |
| summary | Summaries. |
| agent | The requests of an [agent](agents.md); every run has a value of its own, as it grows its own conversation. |
| help | The help assistant. |
| plugins | Each plugin has its own: *Reviewer*, *Side Query* (its tab titles have another), *Lore Generator*, *Arc Outline*, *SillyTavern Converter* and *Manga Generator*. |

Requests that belong to no kind (listing the models, testing the connection, counting tokens) send no header.

The value is a keyed hash of the user and the kind (the key comes from `secret.key` in the
[data folder](administration/index.md#the-data-folder)). It reveals neither the user nor the book, nobody can work out
who it belongs to, and another installation gives another value for the same user. There is one value per user and kind,
not one per book, so a server never has to keep more caches than that.

All providers send it: OpenAI-compatible ones, and the [Anthropic Claude](extensions/anthropic.md) and
[Google Gemini](extensions/google-genai.md) providers. A server that doesn't use the header ignores it. There is no
setting to switch it off.

## Using it: one slot on disk with llama.cpp

The value is for you if the server runs out of memory for caches. A local `llama-server` with enough memory for several
slots can keep a cache for each kind of work and needs nothing more. With memory for **one** slot, every other kind of
work throws the cache of the story away: the annotation of a part, the redraft after it and the next generation read
their prompts again each time.

The way out is to keep the cache of every kind on disk and swap it into the one slot when a request of that kind
arrives. `llama-server` can save a slot to a file and restore it; a proxy in front of it knows which kind a request is
from the header. The example below is a hook for [LiteLLM](https://docs.litellm.ai/).

Start `llama-server` with one slot and a folder for the files:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

Save this as `slot_cache.py` next to the LiteLLM configuration:

```python
"""LiteLLM hook: keeps the prompt cache of llama-server on disk, one file per X-Session-ID.

llama-server runs with ONE slot (-np 1) and --slot-save-path. Marginalia sends the header X-Session-ID, a hash of the
user and the kind of work (generation, annotate, redraft...). When a request for another session arrives, the hook saves
the slot under the name of the session that was in it and restores the file of the new one (or empties the slot when
there is none yet), so every kind of work finds its own cache and only one is in memory.

A saved state fits only the model that wrote it, so the files are kept per route: the name is the route (the model_name
of the LiteLLM config) and the session, and the limits below count the files of one route. Routes never share a file,
also when they point at the same server or share a folder. After you change the model behind a route, delete its files.

The server is the one the request is going to (its api_base and api_key in the LiteLLM config); only deployments that
ask for it are touched, with `model_info` in config.yaml. One wildcard entry covers a llama-server in router mode, no
model has to be listed (the route is then the model the client asked for):

    model_list:
      - model_name: "*"
        litellm_params: {model: "openai/*", api_base: "http://llama-server:8080/v1", api_key: none}
        model_info:
          slot_cache: true              # or the settings below, each one optional:
          # slot_cache: {slot: 0, save_path: /var/cache/llama-slots, max_files: 8, max_gib: 20, model: qwen}

In router mode every model is an instance of its own with its own slots: the slot calls name the model (the model of the
request without the `openai/` of LiteLLM, or `model` of the settings), so the state is kept per server, model and slot.
This needs a llama.cpp that routes /slots (PR 19854, 2026-02).

  slot        the slot to use, default 0 (environment LLAMA_SLOT)
  model       the model the router knows, when it is not the one in the request
  models_max  the --models-max of the router, default 4 (environment LLAMA_MODELS_MAX), 0 for no limit. The router
              unloads the model used longest ago when a request needs more than that; its slot is gone with it. The hook
              follows the same order and saves that slot BEFORE the request that makes the router unload it. Requests
              that reach the router without this proxy are not seen, so it must be the only client of the router.
  save_path   the folder of --slot-save-path as THIS process sees it; without it the files are never deleted
              (environment LLAMA_SLOT_SAVE_PATH)
  max_files   keep at most this many saved caches of the route, default no limit (environment LLAMA_CACHE_MAX_FILES)
  max_gib     keep at most this many GiB of saved caches of the route, default no limit (environment LLAMA_CACHE_MAX_GIB)
When a limit is passed, the caches that were used the longest ago are deleted first.
"""
import asyncio
import collections
import hashlib
import logging
import os
import re

import httpx
from litellm.integrations.custom_logger import CustomLogger

log = logging.getLogger("slot_cache")

SLOT = int(os.environ.get("LLAMA_SLOT", "0"))
SAVE_PATH = os.environ.get("LLAMA_SLOT_SAVE_PATH")
MAX_FILES = int(os.environ.get("LLAMA_CACHE_MAX_FILES", "0"))
MAX_GIB = float(os.environ.get("LLAMA_CACHE_MAX_GIB", "0"))
# --models-max of llama-server in router mode (its default is 4, 0 is no limit)
MODELS_MAX = int(os.environ.get("LLAMA_MODELS_MAX", "4"))
# the header is client input and becomes a file name: only what Marginalia sends is accepted
SESSION_ID = re.compile(r"^[0-9a-f]{32}$")


def route_tag(route):
    """The part of a file name that is the route: readable, safe as a file name, and told apart by a hash of the name."""
    return re.sub(r"[^A-Za-z0-9_-]", "_", route)[:40] + "-" + hashlib.sha256(route.encode()).hexdigest()[:8]


class CacheFolder:
    """The saved caches of one route in the folder of llama-server; deletes the least recently used ones beyond the
    limits (0 is no limit). The files of other routes are not touched and not counted."""

    def __init__(self, path, max_files=0, max_bytes=0, tag=""):
        self.path = path
        self.max_files = max_files
        self.max_bytes = max_bytes
        self.files = re.compile("^" + re.escape(tag) + r"_[0-9a-f]{32}\.bin$")

    def touch(self, session):
        """A cache that was restored was just used: it is the last one to go."""
        if self.path:
            try:
                os.utime(os.path.join(self.path, session + ".bin"))
            except OSError:
                pass

    def evict(self, keep=()):
        """Deletes the oldest caches until the limits hold, never those of the sessions in `keep`."""
        if not self.path or not (self.max_files or self.max_bytes):
            return []
        try:
            entries = []
            for name in os.listdir(self.path):
                if self.files.match(name):
                    stat = os.stat(os.path.join(self.path, name))
                    entries.append((stat.st_mtime, name, stat.st_size))
        except OSError as e:
            log.warning("Cannot list %s: %s", self.path, e)
            return []
        entries.sort()
        count = len(entries)
        size = sum(entry[2] for entry in entries)
        removed = []
        for _, name, length in entries:
            if (not self.max_files or count <= self.max_files) and (not self.max_bytes or size <= self.max_bytes):
                break
            if name[:-len(".bin")] in keep:
                continue
            try:
                os.remove(os.path.join(self.path, name))
            except OSError as e:
                log.warning("Cannot delete %s: %s", name, e)
                continue
            count -= 1
            size -= length
            removed.append(name)
        if self.max_bytes and size > self.max_bytes:
            log.warning("The cache of one session alone is over the limit of %d bytes", self.max_bytes)
        return removed


class SlotSwitcher:
    """One slot of one llama-server and the cache (route and session) that is in it."""

    def __init__(self, base_url, slot=SLOT, client=None, model=None):
        self.base_url = base_url.rstrip("/")
        self.slot = slot
        # a router sends the call to the instance of this model (the body is read by the router)
        self.model = model
        # a swap waits for the slot to be idle, which can be as long as a generation takes
        self.client = client or httpx.AsyncClient(timeout=None)
        self.headers = {}
        self.current = None
        self.folder = None
        self._lock = asyncio.Lock()

    async def _action(self, action, key=None):
        body = {}
        if self.model:
            body["model"] = self.model
        if key:
            body["filename"] = key + ".bin"
        # nothing to save in a model the router has unloaded: it must not be loaded for that
        query = f"action={action}" + ("&autoload=false" if action == "save" else "")
        try:
            response = await self.client.post(f"{self.base_url}/slots/{self.slot}?{query}", json=body or None,
                                              headers=self.headers)
            if response.status_code in (401, 403):
                log.warning("%s %s: the server refused the api key (%d)", self.base_url, action, response.status_code)
            return response.is_success
        except httpx.HTTPError as e:
            # a cache that is not there is only slower, never an error of the request
            log.warning("%s %s: %s", self.base_url, action, e)
            return False

    async def switch(self, key, api_key=None, folder=None):
        """Puts the cache `key` (the file name without .bin) in the slot; `folder` has the limits of its route."""
        folder = folder or CacheFolder(SAVE_PATH, MAX_FILES, int(MAX_GIB * 1024 ** 3))
        async with self._lock:
            if key == self.current:
                return
            # the key of the deployment that asks now (a placeholder is not a key)
            self.headers = {"Authorization": "Bearer " + api_key} if api_key and api_key != "none" else {}
            self.folder = folder
            if self.current is not None and await self._action("save", self.current):
                # not the cache that was just saved, and not the one that is about to be restored
                await asyncio.to_thread(folder.evict, {self.current, key})
            if await self._action("restore", key):
                await asyncio.to_thread(folder.touch, key)
            else:
                await self._action("erase")
            self.current = key

    async def release(self, api_key=None):
        """The model is about to be unloaded by the router: its slot goes, so the cache in it is saved first."""
        async with self._lock:
            if self.current is None:
                return
            self.headers = {"Authorization": "Bearer " + api_key} if api_key and api_key != "none" else {}
            if await self._action("save", self.current) and self.folder:
                await asyncio.to_thread(self.folder.evict, {self.current})
            self.current = None


def upstream_model(model):
    """The name the server knows the model by: LiteLLM's name is `openai/<name>`."""
    return model[len("openai/"):] if model and model.startswith("openai/") else model


def server_root(api_base):
    """The slots are at the root of llama-server, the API address of the model ends in /v1."""
    base = api_base.rstrip("/")
    return base[:-len("/v1")] if base.endswith("/v1") else base


class SlotCacheHook(CustomLogger):
    def __init__(self):
        super().__init__()
        self.switchers = {}
        self.loaded = {}            # per server: the models the router has loaded, the one used longest ago first
        self.client = httpx.AsyncClient(timeout=None)

    async def make_room(self, root, model, models_max, api_key):
        """Follows the models the router has loaded; the ones it unloads for `model` get their slot saved first."""
        if not models_max or not model:
            return
        loaded = self.loaded.setdefault(root, collections.OrderedDict())
        loaded[model] = None
        loaded.move_to_end(model)
        while len(loaded) > models_max:
            gone, _ = loaded.popitem(last=False)
            for (r, m, _slot), switcher in list(self.switchers.items()):
                if r == root and m == gone:
                    await switcher.release(api_key)

    # runs after the router chose the deployment (so its api_base is known) and before the request is sent
    async def async_pre_call_deployment_hook(self, kwargs, call_type):
        headers = (kwargs.get("proxy_server_request") or {}).get("headers") or {}
        session = headers.get("x-session-id")
        options = (kwargs.get("model_info") or {}).get("slot_cache")
        api_base = kwargs.get("api_base")
        # the route the client asked for; without one the files of the models could not be told apart
        route = (kwargs.get("metadata") or {}).get("model_group") or kwargs.get("model")
        if not (session and SESSION_ID.match(session) and options and api_base and route):
            return None
        options = options if isinstance(options, dict) else {}
        tag = route_tag(route)
        folder = CacheFolder(options.get("save_path", SAVE_PATH), int(options.get("max_files", MAX_FILES)),
                             int(float(options.get("max_gib", MAX_GIB)) * 1024 ** 3), tag)
        slot = int(options.get("slot", SLOT))
        model = options.get("model") or upstream_model(kwargs.get("model"))
        where = (server_root(api_base), model, slot)
        await self.make_room(where[0], model, int(options.get("models_max", MODELS_MAX)), kwargs.get("api_key"))
        if where not in self.switchers:
            self.switchers[where] = SlotSwitcher(where[0], slot, self.client, model)
        await self.switchers[where].switch(f"{tag}_{session}", kwargs.get("api_key"), folder)
        return None


proxy_handler_instance = SlotCacheHook()
```

and switch it on in the LiteLLM `config.yaml`: the callback, and `slot_cache` in the `model_info` of the route that goes
to the `llama-server`. One wildcard entry is enough, no model has to be listed in LiteLLM (the server knows them, and
the client names the one it wants):

```yaml
model_list:
  - model_name: "*"
    litellm_params:
      model: "openai/*"
      api_base: http://llama-server:8080/v1
      api_key: none
    model_info:
      slot_cache: true
      # or, each one optional:
      # slot_cache: {slot: 0, save_path: /var/cache/llama-slots, max_files: 8, max_gib: 20, models_max: 1}

litellm_settings:
  callbacks: slot_cache.proxy_handler_instance
```

The hook needs no address of its own: it uses the `api_base` and `api_key` of the route the request goes to (the slots
are at the root of the server, so a trailing `/v1` is dropped). Only the routes with `slot_cache` are touched; a request
for any other model, a cloud one for instance, passes through even though it carries the header. Several servers
work side by side, each with its own slot. With the wildcard the route is the model the client asked for, so the files are
kept per model (`qwen3-32b`), and you can still list some models by name next to it if they need other settings. LiteLLM reads the part of a model
name before a `/` as the provider, so for names that contain one (`org/model`, as in a router preset) use the route
`model_name: "openai/*"` and give Marginalia the model as `openai/org/model`: the hook removes the `openai/`, and the
files and the slot calls use the name the server knows. The
routes and their `model_info` come from the `model_list`: a proxy started without one (only `litellm --model ...`) has
neither, and the hook does nothing there. The one wildcard entry is the replacement.

**`llama-server` in router mode** (many models behind one address, loaded on demand) works: every model is an instance
of its own with its own slots, so the hook keeps the state per server, model and slot, and names the model in the body
of its slot calls (the name the client used, without LiteLLM's `openai/`; the setting `model` gives another one, for a
route whose name is not the model). It needs a llama.cpp from February 2026 or later, which forwards `/slots` to the
instance (before it answered `400 Invalid action`).

When the router may hold only some of its models (`--models-max`, 4 by default), asking for another one makes it
unload the model used longest ago, and the slot of that model, with the cache in it, is gone. The hook follows the same
order (set `models_max` in `slot_cache`, or `LLAMA_MODELS_MAX`, to the same number as `--models-max`; 0 for no limit)
and saves that slot **before** the request that makes the router unload it. For that the proxy must be the only client
of the router: a request that reaches it another way is not seen. A save never loads a model the router has unloaded.
What it does: when a request arrives from another session than the one in the slot, the hook saves the slot under the
name of the session that was in it, restores the file of the new one (or empties the slot when there is no file yet),
and lets the request go on. The files are kept **per route** (the `model_name`): a saved state fits only the model that
wrote it, so the file is named after the route and the session, and two routes never share one, also when they use the
same server and folder. The server then reads only what differs from the restored prompt. A cache that can't be
restored, a file that is gone or a server that doesn't answer only means that the prompt is read again; the request
itself never fails because of the hook.

Things to know:

- **Size.** A file is as big as the memory of the slot holds: for a long story and a large model hundreds of megabytes
  to several gigabytes. There is one file per route, user and kind, and each is overwritten in place, so the folder stays as
  small as that. Check that restoring one is faster than reading the prompt (a fast SSD usually is, by a lot, for a
  long story: a few seconds against a minute of prefill).
- **Limits.** Without a limit the folder grows with the number of routes, users and kinds. Set `max_files` (a number of
  files) and/or `max_gib` (their total size) in the `slot_cache` of a route (or `LLAMA_CACHE_MAX_FILES` and
  `LLAMA_CACHE_MAX_GIB` in the environment of the proxy for all routes) and the hook deletes the caches **of that route**
  that were **used** the longest ago after each save: restoring a cache counts as using it. It never deletes the cache
  it has just saved or the one it is about to restore (so a route with two sessions in turn keeps at least two files,
  whatever the limit), and it only touches the files of the route, so other routes and other files in the folder are safe. A single cache that is bigger than `max_gib` stays (with a warning in the log).
  Deleting needs the folder: set `save_path` (or `LLAMA_SLOT_SAVE_PATH`) to the `--slot-save-path` folder as the proxy
  sees it; with `llama-server` in a container, that is a volume both containers mount. Without it nothing is deleted. A
  deleted cache only means that its session reads its prompt again once. The limits are optional: a cron job on the host
  does as well when you do not need a cap on the size, for example `find /srv/ai/slots -name '*.bin' -mtime +2 -delete`
  (a cache that is in use is saved again whenever its session is swapped out, so its age keeps starting over). Files
  deleted from outside are harmless to the hook; a missing one is a miss.
- **After you change the model of a route**, delete its files in the folder (their names start with the route): a state
  saved by other weights does not fit the new model.
- **Every switch costs a save and a restore.** With [automatic annotation](books/annotations.md) on, a part is followed by an
  annotation and a redraft, so one turn can swap three or four times. Measure a turn before you rely on it.
- **A restart of the proxy** forgets which session is in the slot; the first request after it restores its file over the
  slot, and what the slot held is lost.
- **A model the router unloaded without the hook knowing** (a wrong `models_max`, another client) has an empty slot
  while the hook still thinks the last session is in it. A request for the same session simply reads its prompt again;
  a request for another one saves the empty slot over that session's file first, so its cache is lost too. Both only
  cost a prefill.
- **Failures show in the log of the proxy** as warnings from `slot_cache` (a certificate that does not match the address,
  a refused api key, a server that does not answer); a missing file is not one, it is the normal first request.
- **In Docker** the proxy and `llama-server` are two containers, so the folder of `--slot-save-path` is a directory of the
  host that both mount (the proxy needs to write there: it deletes old files and renews the age of the used ones), the
  hook file goes next to the LiteLLM config, and `api_base` is an address the proxy can reach, with the host name of the
  certificate when the server uses TLS:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  with `save_path: /slots` and `models_max: 1` in the `slot_cache` of the route.
- The hook uses `CustomLogger.async_pre_call_deployment_hook`, which LiteLLM calls after it chose the deployment and
  before it sends the request (version 1.106.5 was checked with a real router and a stand-in server); check it against
  the version you run. A proxy that swaps models behind one address by restarting a single server (llama-swap) is not
  supported: the slot calls would reach whichever model is loaded. None of this was run against a real `llama-server`
  router; the tests use a stand-in that checks the calls the hook makes.
