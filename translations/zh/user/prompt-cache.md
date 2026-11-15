---
label: 提示缓存与会话 ID
order: 975
icon: database
---

# 提示缓存与会话 ID

模型服务器会保留它刚读过的提示（即*提示缓存*），这样下一个以相同文本开头的请求就不必重新读取。对于长篇故事来说，这意味着等待几秒还是几分钟。缓存只在新提示与旧提示的开头一致的范围内有效（参见[为什么缓存可能不起作用](#why-a-cache-may-not-help)）。Marginalia 还可以告诉服务器**请求属于哪种工作**，这样服务器或其前面的代理就能为每种工作保留一份缓存。

<a id="what-is-sent"></a>

## 发送的内容

Marginalia 为某种工作发出的每个请求都会带上这个 HTTP 头：

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

同一用户做同一种工作时，无论是哪本书，该值都相同；不同用户之间、不同种类之间，值则不同：

| 类型 | 请求 |
|---|---|
| generation | 新的部分、*Regenerate* 和 *Swipe*：它们的提示共享同一个故事，因此共享同一个值。 |
| impersonate | *Impersonate*。 |
| annotate | *Annotate with AI* 以及自动批注；“两阶段”模式中的第二个模型有自己的值。 |
| redraft | *Redraft* 以及自动批注的重写。 |
| summary | 摘要。 |
| agent | [代理](agents.md)的请求；每次运行都有自己的值，因为它会积累自己的对话。 |
| help | 帮助助手。 |
| plugins | 每个插件都有自己的值：*Reviewer*、*Side Query*（其标签页标题不同）、*Lore Generator*、*Arc Outline*、*Converter* 和 *Manga Generator*。 |

不属于任何类型的请求（列出模型、测试连接、统计 token）不发送该头。

该值是用户和类型的带密钥哈希（密钥来自[数据文件夹](administration/index.md#the-data-folder)中的 `secret.key`）。它既不泄露用户，也不泄露书籍，任何人都无法据此推断其归属；另一个安装实例对同一用户会得到不同的值。每个用户、每种类型只有一个值，而不是每本书一个，因此服务器需要保留的缓存数量永远不会超过这个数。

所有提供方都会发送该头：兼容 OpenAI 的提供方，以及 [Anthropic Claude](extensions/anthropic.md) 和 [Google Gemini](extensions/google-genai.md) 提供方。不使用该头的服务器会忽略它。没有设置可以关闭它。

<a id="why-a-cache-may-not-help"></a>

## 为什么缓存可能不起作用

服务器只会复用缓存中新提示与旧提示**从第一个 token 起**都相同的那一部分；第一处差异就会中断复用，此后的内容都要重新读取，即使其中大部分没有变化。请看请求开始时打印的 `llama-server` 行：

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` 是新请求保留的缓存提示所占的比例。接近 1 时说明缓存被使用；这里只有 15%，说明提示在前段就与缓存中的不同，无论缓存是从磁盘恢复的，还是一直留在槽位中。

具有**滑动窗口**的模型（例如 Gemma）以及混合模型，无法截掉缓存的末尾后从中间继续。据 `llama.cpp` 源码所述，只有当新提示完全扩展了缓存提示，或者在两者出现差异的位置或之前存在**上下文检查点**时，`llama-server` 才能从缓存提示继续。它只会在用户消息的开头设置检查点（相隔至少 8192 个 token），并在提示末尾附近设置检查点，且保存在槽位文件中（INFO 日志行 `appended N context checkpoint(s)` 和 `restored N context checkpoint(s)` 会说明何时设置）。没有可用的检查点时，无论 `f_keep` 显示什么，它都会重新读取整个提示，并且只在 trace 级别报告这一点（`-lv 4`：`forcing full prompt re-processing due to lack of cache data`）。这就是为什么生成新部分、批注和重写时，故事会被作为一连串消息发送：每一部分都是在用户消息之后的模型回答。最后一条用户消息会设置检查点，这正是下一个请求开始出现差异的地方；而新的部分只会在末尾追加消息。从日志中读取冷预填充：这里第一条 `prompt processing, n_tokens = 4096` 行大约在 7 秒后出现；如果复用了前缀，则会在一两秒后出现。

[提示](books/prompts.md#how-the-prompt-is-put-together)从**系统消息**开始，故事位于其后，因此系统消息中的内容必须在两次请求之间保持不变。在默认的主模板中，系统消息依次包含：设定块、摘要以及作者的注释（批注）。当其中任何一项在两次请求之间发生变化时（这很常见：新文本激活了其他设定条目、添加了摘要、添加或解决了注释），缓存只在变化之前有效，其后的整个故事都要重新读取。当故事超出容量时，最早的部分会先从提示中移除，这使得每一轮故事的起点都会后移一个部分。以下做法有助于改善：

- 将每个部分都会变化的条目设为插入方式 **Before User Prompt**（[顺序与插入](lorebooks/entries.md#order-and-insertion)）：它们会被放到提示的末尾、故事之后，不会干扰缓存。把始终为真的内容保留在设定块中。
- 不要在主模板中保留你不使用的块（例如，如果你不向模型提供注释，就去掉作者注释），使系统消息保持不变。
- 打开某个部分菜单中的 **Compare Prompt**，查看缓存在哪里结束：它会将该部分的提示与较早部分的提示进行比较（默认是最近的一个；列表会显示它之前的部分及其 swipe），并标出第一条不同的消息。参见 [Compare Prompt](#compare-prompt)。

### Compare Prompt

某个部分（桌面端和手机端）菜单中的 **Compare Prompt** 用于回答*为什么这个部分没有使用缓存？*它会列出两个提示的消息（先是系统消息，然后是每个部分对应的一条用户消息和一条模型回答，最后是本轮的用户消息），并为每条消息标注状态：相同、已更改、已添加或已删除。第一条不同的消息会被高亮显示，其后的消息会标记为*Read again*，因为服务器会在第一处差异之后重新读取所有内容。表格上方显示：

- 第一处差异的位置（消息和字符），以及从开头起相同的提示量（以字符数计，并大致折算为 token 数）；
- 对差异的说明：**系统消息**不同（越狱提示、模板、设定、摘要或注释在两次请求之间发生了变化：这是常见原因），**故事中间**的某个部分不同（它被编辑、重新生成或被摘要替代），**故事的起点后移了**（最早的部分被省略或被摘要），或者差异仅在于**较早提示的最后一条消息**，即该轮的用户消息（故事不会重复这条消息）。最后一种情况是下一个部分或 swipe 所能复用的最大部分；如果服务器仍然没有使用其缓存，那么原因不在提示（请检查槽位、模型和会话 ID）。

选中某条消息会并排显示两段文本，从第一处差异之前开始。只比较消息本身：模型、请求的工具以及服务器槽位都不属于所存储的提示。该部分需要有已存储的提示（由作者手动输入的部分，以及在保存提示功能推出之前生成的部分，都没有存储的提示）。

下列请求类型会影响你在它们之间切换时保留的内容；提示决定了其中有多少会被使用。
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 使用方法：llama.cpp 与磁盘上的单个槽位

如果服务器的内存不足以容纳缓存，本节对你有用。本地 `llama-server` 如果内存足以容纳多个槽位，就能为每类工作各保留一份缓存，无需其他操作。若内存只够一个**槽位**，每类其他工作都会清掉故事的缓存：对某一段的注释、之后的重写稿以及接下来的生成，每次都要重新读取各自的提示词。

解决办法是把每类工作的缓存都保存在磁盘上，当该类请求到达时再换入唯一的槽位。`llama-server` 可以把槽位保存到文件并再恢复；其前面的代理通过请求头判断请求属于哪一类。下面的示例是 [LiteLLM](https://docs.litellm.ai/) 的一个钩子。

**要求：** 2026-10-08 或之后的 `llama-server`（llama.cpp PR #26004，*preserve context checkpoints across slot save/restore*）。更早的版本只写入和恢复槽位的内存，而不包括其[上下文检查点](#why-a-cache-may-not-help)，因此使用滑动窗口的模型（例如 Gemma）或混合模型，在每次恢复后都要重新读取整个提示词，无论其中有多少内容没有变化。用 `llama-server --version` 检查构建版本；使用可变标签的容器镜像（`full`、`server`……）在你拉取之前不会更新。

使用一个槽位并指定一个存放文件的文件夹来启动 `llama-server`：

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

将以下内容保存为 LiteLLM 配置旁边的 `slot_cache.py`：

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
  save_idle_seconds
              also save the cache of the session in the slot when no request came for this long (default 0: only
              when the slot is needed for another session or model; environment LLAMA_SAVE_IDLE_SECONDS). A save
              is sent when the slot is idle, and the server does nothing else while it writes.
  models_max  the --models-max of the router, default 4 (environment LLAMA_MODELS_MAX), 0 for no limit. The router
              unloads the model used longest ago when a request needs more than that; its slot is gone with it. The hook
              follows the same order and saves that slot BEFORE the request that makes the router unload it. Requests
              that reach the router without this proxy are not seen, so it must be the only client of the router.
  save_path   the folder of --slot-save-path as THIS process sees it; without it the files are never deleted
              (environment LLAMA_SLOT_SAVE_PATH)
  max_files   keep at most this many saved caches of the route, default no limit (environment LLAMA_CACHE_MAX_FILES)
  max_gib     keep at most this many GiB of saved caches of the route, default no limit (environment LLAMA_CACHE_MAX_GIB)
When a limit is passed, the caches that were used the longest ago are deleted first.

A save, restore or erase is only sent when the slot is idle (GET /slots, is_processing): llama-server parks such a call
while a request runs, and only one parked call is released each time a request ends, so a request of another client that
arrived behind it could wait there indefinitely. Without /slots the calls are sent at once.
"""
import asyncio
import collections
import hashlib
import logging
import os
import re

import httpx
from litellm.integrations.custom_logger import CustomLogger

try:
    # the logger of the proxy: its INFO lines show with LITELLM_LOG=INFO
    from litellm._logging import verbose_proxy_logger as log
except ImportError:
    log = logging.getLogger("slot_cache")

SLOT = int(os.environ.get("LLAMA_SLOT", "0"))
SAVE_PATH = os.environ.get("LLAMA_SLOT_SAVE_PATH")
MAX_FILES = int(os.environ.get("LLAMA_CACHE_MAX_FILES", "0"))
MAX_GIB = float(os.environ.get("LLAMA_CACHE_MAX_GIB", "0"))
# --models-max of llama-server in router mode (its default is 4, 0 is no limit)
MODELS_MAX = int(os.environ.get("LLAMA_MODELS_MAX", "4"))
SAVE_IDLE_SECONDS = float(os.environ.get("LLAMA_SAVE_IDLE_SECONDS", "0"))
IDLE_POLL_SECONDS = 1.0         # how often a busy slot is looked at ...
IDLE_WAIT_MAX_SECONDS = 3600    # ... and for how long at most
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

    def has(self, session):
        return bool(self.path) and os.path.exists(os.path.join(self.path, session + ".bin"))

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
        self.version = 0            # counts the requests: what the slot holds changed since the last save
        self.saved_version = 0
        self._idle = None
        self._lock = asyncio.Lock()

    async def _busy(self):
        """Whether the slot is processing a request. False when the server does not tell."""
        try:
            response = await self.client.get(f"{self.base_url}/slots", headers=self.headers, timeout=15,
                                             params={"model": self.model} if self.model else None)
            slots = response.json() if response.is_success else None
            return isinstance(slots, list) and any(
                slot.get("is_processing") for slot in slots
                if isinstance(slot, dict) and slot.get("id", self.slot) == self.slot)
        except (httpx.HTTPError, ValueError):
            return False

    async def _wait_idle(self):
        deadline = asyncio.get_running_loop().time() + IDLE_WAIT_MAX_SECONDS
        while await self._busy() and asyncio.get_running_loop().time() < deadline:
            await asyncio.sleep(IDLE_POLL_SECONDS)

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
                log.warning("slot_cache: %s %s: the server refused the api key (%d)", self.base_url, action,
                            response.status_code)
            detail = ""
            if response.is_success:
                try:
                    detail = describe(response.json())
                except ValueError:
                    pass
            log.info("slot_cache: %s %s %s (%s%s): %s", self.base_url, action, key or "", self.model or "-",
                     "" if self.slot == 0 else f", slot {self.slot}",
                     "ok" + detail if response.is_success else
                     (f"miss ({response.status_code})" if action == "restore" else f"failed ({response.status_code})"))
            return response.is_success
        except httpx.HTTPError as e:
            # a cache that is not there is only slower, never an error of the request
            log.warning("slot_cache: %s %s: %s", self.base_url, action, e)
            return False

    async def switch(self, key, api_key=None, folder=None, idle=SAVE_IDLE_SECONDS):
        """Puts the cache `key` (the file name without .bin) in the slot; `folder` has the limits of its route.
        With `idle` the cache is saved when no request came for that many seconds."""
        folder = folder or CacheFolder(SAVE_PATH, MAX_FILES, int(MAX_GIB * 1024 ** 3))
        await self._switch(key, api_key, folder)
        self.version += 1
        if idle:
            if self._idle and not self._idle.done():
                self._idle.cancel()
            self._idle = asyncio.create_task(self._save_when_idle(idle))

    async def _save_when_idle(self, seconds):
        await asyncio.sleep(seconds)
        version = self.version
        await self._wait_idle()         # a new request cancels this task, also while it waits
        async with self._lock:
            if self.current is None or version == self.saved_version:
                return
            if await self._action("save", self.current):
                self.saved_version = version
                if self.folder:
                    await asyncio.to_thread(self.folder.evict, {self.current})

    async def _switch(self, key, api_key, folder):
        async with self._lock:
            if key == self.current:
                return
            # the key of the deployment that asks now (a placeholder is not a key)
            self.headers = {"Authorization": "Bearer " + api_key} if api_key and api_key != "none" else {}
            self.folder = folder
            await self._wait_idle()
            # a cache that was saved after its last request (the idle save) is on disk already
            unchanged = self.saved_version == self.version and self.current is not None and folder.has(self.current)
            if self.current is not None and (unchanged or await self._action("save", self.current)):
                # not the cache that was just saved, and not the one that is about to be restored
                await asyncio.to_thread(folder.evict, {self.current, key})
                self.saved_version = self.version
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
            await self._wait_idle()
            if await self._action("save", self.current) and self.folder:
                await asyncio.to_thread(self.folder.evict, {self.current})
            if self._idle and not self._idle.done():
                self._idle.cancel()
            self.current = None


def describe(body):
    """What llama-server says about a slot call that worked: it logs nothing itself, the numbers are in the answer."""
    if not isinstance(body, dict):
        return ""
    timings = body.get("timings") if isinstance(body.get("timings"), dict) else {}
    try:
        if "n_restored" in body:
            return f" ({body['n_restored']} tokens, {body.get('n_read', 0) / 1e6:.0f} MB, {timings.get('restore_ms', 0) / 1000:.1f} s)"
        if "n_saved" in body:
            return f" ({body['n_saved']} tokens, {body.get('n_written', 0) / 1e6:.0f} MB, {timings.get('save_ms', 0) / 1000:.1f} s)"
        if "n_erased" in body:
            return f" ({body['n_erased']} tokens dropped)"
    except (TypeError, ValueError):
        pass
    return ""


def text_of(message):
    """The text of a chat message: a string, or the text parts of a list."""
    content = message.get("content") if isinstance(message, dict) else None
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return "".join(part.get("text", "") for part in content if isinstance(part, dict))
    return ""


def compare(previous, messages):
    """Says where a prompt first differs from the last one of its session: a server can reuse its cache up to there, and not
    after it, however much of the rest is the same. `previous` and `messages` are lists of (role, text). The last message is
    compared with the last one of the other prompt as well: it holds what changes, and when a story grew by a message it is not
    at the same place."""
    shared = 0
    while shared < min(len(previous), len(messages)) and previous[shared] == messages[shared]:
        shared += 1
    if shared == len(previous) == len(messages):
        return f"the same as the last one ({len(messages)} messages)"
    if shared == len(previous) or shared == len(messages):
        said = (f"{len(messages)} messages, the last one had {len(previous)}: the first {shared} are the same, "
                f"{'new messages follow' if shared == len(previous) else 'the end is cut'}")
    else:
        role, text = messages[shared]
        same = len(os.path.commonprefix([previous[shared][1], text]))
        said = (f"{len(messages)} messages, the last one had {len(previous)}: the first {shared} are the same, message {shared + 1} "
                f"({role}) is the same for its first {same} of {len(text)} characters (the last one: {len(previous[shared][1])})")
    last_now, last_before = messages[-1][1], previous[-1][1]
    return (said + f"; the last message is the same as the last one's last message for its first "
            f"{len(os.path.commonprefix([last_before, last_now]))} of {len(last_now)} characters (the last one: {len(last_before)})")


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
        self.skipped = set()        # the routes that were reported as not used
        self.last = {}              # the messages of the last prompt of each session, to say where the next one differs
        self.client = httpx.AsyncClient(timeout=None)

    def explain(self, key, messages):
        if not isinstance(messages, list):
            return
        current = [(m.get("role"), text_of(m)) for m in messages if isinstance(m, dict)]
        previous = self.last.get(key)
        self.last[key] = current
        if previous is not None:
            log.info("slot_cache: %s prompt: %s", key, compare(previous, current))

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

    async def release_server(self, root, api_key=None):
        """For another hook that is about to unload the models of the server at `root` (see gpu_unload.py): saves the cache
        of every slot kept for it and forgets what was in them, so the next request restores its cache into the reloaded model."""
        for (r, _model, _slot), switcher in list(self.switchers.items()):
            if r == root:
                await switcher.release(api_key)
        self.loaded.pop(root, None)

    # runs after the router chose the deployment (so its api_base is known) and before the request is sent
    async def async_pre_call_deployment_hook(self, kwargs, call_type):
        headers = (kwargs.get("proxy_server_request") or {}).get("headers") or {}
        session = headers.get("x-session-id")
        options = (kwargs.get("model_info") or {}).get("slot_cache")
        api_base = kwargs.get("api_base")
        # the route the client asked for; without one the files of the models could not be told apart
        route = (kwargs.get("metadata") or {}).get("model_group") or kwargs.get("model")
        if not (session and SESSION_ID.match(session)):
            return None
        if not (options and api_base and route):
            # a request of Marginalia that is passed through: say why, once per route
            why = "no model_info.slot_cache" if not options else "no api_base" if not api_base else "no route"
            if (route, why) not in self.skipped:
                self.skipped.add((route, why))
                log.info("slot_cache: not used for %s: %s", route, why)
            return None
        options = options if isinstance(options, dict) else {}
        tag = route_tag(route)
        folder = CacheFolder(options.get("save_path", SAVE_PATH), int(options.get("max_files", MAX_FILES)),
                             int(float(options.get("max_gib", MAX_GIB)) * 1024 ** 3), tag)
        slot = int(options.get("slot", SLOT))
        model = options.get("model") or upstream_model(kwargs.get("model"))
        where = (server_root(api_base), model, slot)
        self.explain(f"{tag}_{session}", kwargs.get("messages"))
        await self.make_room(where[0], model, int(options.get("models_max", MODELS_MAX)), kwargs.get("api_key"))
        if where not in self.switchers:
            self.switchers[where] = SlotSwitcher(where[0], slot, self.client, model)
        await self.switchers[where].switch(f"{tag}_{session}", kwargs.get("api_key"), folder,
                                           float(options.get("save_idle_seconds", SAVE_IDLE_SECONDS)))
        return None


proxy_handler_instance = SlotCacheHook()
```

然后在 LiteLLM 的 `config.yaml` 中启用它：在该处设置回调，并在通往 `llama-server` 的路由的 `model_info` 中设置 `slot_cache`。一个通配符条目就足够了，无需在 LiteLLM 中列出任何模型（服务器知道它们，客户端指定所需的模型）：

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
      # slot_cache: {slot: 0, save_path: /var/cache/llama-slots, max_files: 8, max_gib: 20, models_max: 1, save_idle_seconds: 120}

litellm_settings:
  callbacks: slot_cache.proxy_handler_instance
```

钩子不需要自己的地址：它使用请求所发往路由的 `api_base` 和 `api_key`（槽位位于服务器根路径下，因此末尾的 `/v1` 会被去掉）。只有带有 `slot_cache` 的路由会被处理；请求其他模型（例如云端模型）时，即使带有该请求头，也会直接透传。多个服务器可以并行工作，各自拥有自己的槽位。使用通配符时，路由即为客户端所请求的模型，因此文件按模型分别保存（`qwen3-32b`）；如果某些模型需要其他设置，仍可在其旁边按名称单独列出。LiteLLM 将模型名称中 `/` 之前的部分视为提供方，因此对于包含该字符的名称（例如路由器预设中的名称 `org/model`），请使用路由 `model_name: "openai/*"`，并在 Marginalia 中将模型设为 `openai/org/model`：钩子会去掉 `openai/`，而文件和槽位调用使用服务器所识别的名称。路由及其 `model_info` 来自 `model_list`：若代理启动时未指定（仅 `litellm --model ...`），则两者都没有，钩子在此时不做任何操作。单个通配符条目即可替代它们。

**`llama-server` 在路由器模式下**可以工作（多个模型位于同一地址之后，按需加载）：每个模型都是拥有自己槽位的独立实例，因此钩子按服务器、模型和槽位保存状态，并在其槽位调用的请求体中写明模型名（即客户端使用的名称，去掉 LiteLLM 的 `openai/`；设置 `model` 可指定另一个名称，适用于路由名称与模型不同的情况）。它需要 2026 年 2 月或之后的 llama.cpp，该版本会将 `/slots` 转发到实例（之前则会回复 `400 Invalid action`）。

当路由器只能同时保留部分模型时（`--models-max`，默认为 4），请求另一个模型会使其卸载最久未使用的模型，而该模型的槽位及其中的缓存随之消失。钩子遵循相同的顺序（在 `slot_cache` 中设置 `models_max`，或设置 `LLAMA_MODELS_MAX`，使其与 `--models-max` 的数值相同；0 表示不限制），并在使路由器卸载该模型的请求**之前**保存该槽位。为此，代理必须是路由器的唯一客户端：以其他方式到达路由器的请求不会被察觉。保存操作绝不会加载路由器已卸载的模型。

工作方式如下：会话的缓存只在**槽位被另一个会话需要时**才写入磁盘（另一类请求、另一个模型，或路由器卸载该模型），而不是每次请求之后都写入：每次生成后写入 3 GB 只会拖慢服务器。因此，第一次切换之后才会出现文件，例如生成、注释、生成这一顺序中，注释会先保存生成的缓存。若在 `slot_cache` 中设置 `save_idle_seconds`（或 `LLAMA_SAVE_IDLE_SECONDS`），则在这段时间内没有请求时，缓存也会被写入，因此你停止工作后不久文件夹中就会有它；新的请求会重新开始等待，而磁盘上已有的缓存在下一次切换时不会再次写入。只有当槽位空闲时，钩子才会发送保存、恢复或清除请求（它会询问 `GET /slots`、`is_processing`；若没有该端点，则立即发送）：`llama-server` 会在请求运行期间暂存此类调用，且每当一个请求结束时只释放一个被暂存的调用，因此排在其后到达的其他客户端请求可能会永远等待在那里。服务器在保存写入期间不做任何其他工作。

细节如下：当请求来自与槽位中不同的会话时，钩子会以原先在槽位中的会话名称保存该槽位，恢复新会话的文件（若尚无文件则清空槽位），然后让请求继续进行。文件按**路由**（`model_name`）保存：已保存的状态只适用于写入它的模型，因此文件以路由和会话命名，即使两个路由使用同一服务器和文件夹，也绝不共享同一文件。随后服务器只读取与恢复的提示词不同的部分。无法恢复的缓存、已不存在的文件或无响应的服务器，都只意味着需要重新读取提示词；请求本身绝不会因钩子而失败。

注意事项：

- **大小。** 文件保存的是槽位的内存**及其上下文检查点**，因此文件大小是两者之和：对于较长的故事和大型模型，文件大小为数百 MB 到数 GB。一个实测示例：31 B 模型、提示词为 18,000 个 token 时，生成的文件大小为 1.3 到 11 GB，其中最大的一个包含约 18 个检查点（每个约 450 MB），另外还有约 1.5 GB 的缓存。服务器默认最多保留 32 个检查点，每次切换都会写入和读取全部检查点。`--ctx-checkpoints N` 的 `llama-server` 可以限制这一点，使文件更小、切换更快，但检查点只有在下一个提示词与缓存的提示词首次不同的位置恰好在其处或紧随其后时才有帮助。新的一段或重新生成（swipe）会在最后一条用户消息（即最新的检查点）的开头出现差异，而注释或重写稿可能会改动前面好几段的内容，因此应保留与故事回溯范围相当数量的检查点（查看钩子日志中的 `prompt:` 行，它们会说明每个请求在哪里出现差异）。每个路由、用户和类型各对应一个文件，且每次都原地覆盖，因此文件夹只会占用这么大的空间。
- **速度。** 请确认一次切换比重新读取提示词更快（钩子会记录每次保存和恢复的大小和耗时）。上面的示例是在 NVMe 磁盘和 AMD Radeon AI PRO R9700 上测得的：保存 11 GB 耗时 6 秒，恢复 7.6 GB（对应 20,000 个 token）耗时 8 秒，一次切换约 14 秒；而以每秒 440 个 token 的速度重新读取该长度的提示词约需 45 秒，且预填充时间会随提示词增长。没有检查点时，同样的文件约为 1 GB，一次切换耗时 5 秒。
- **内存。** 槽位的检查点保存在服务器的**系统内存**中（而非 GPU 内存），最多 `--ctx-checkpoints` 个（默认 32 个，大型模型每个数百 MB），保存或恢复时会读取或写入全部检查点。`llama-server` 还会在其自身的内存缓存（`--cache-ram`，默认 8 GiB）中保留它已放弃的槽位的提示词；钩子的文件起的是同样的作用，因此在内存不足的机器上可以将其设为 0。当机器开始使用交换分区时，保存和恢复会慢好几倍，因为检查点必须先从交换分区读回：在检查磁盘之前，先查看 `free -h`。
- **限制。** 若不设限制，文件夹会随路由、用户和类型的数量而增长。在某个路由的 `slot_cache` 中设置 `max_files`（文件数量）和/或 `max_gib`（文件总大小）（或者在代理的环境变量中设置 `LLAMA_CACHE_MAX_FILES` 和 `LLAMA_CACHE_MAX_GIB`，对所有路由生效），钩子就会在每次保存后删除**该路由**中**最久未被使用**的缓存：恢复某个缓存也算作使用。它绝不会删除刚刚保存的缓存或即将恢复的缓存（因此，若一个路由中两个会话轮流使用，则无论限制为多少，至少会保留两个文件），且只会处理该路由的文件，因此其他路由及文件夹中的其他文件是安全的。单个大于 `max_gib` 的缓存会被保留（日志中会给出警告）。删除需要文件夹：将 `save_path`（或 `LLAMA_SLOT_SAVE_PATH`）设为代理所见的 `--slot-save-path` 文件夹；若在容器中，则 `llama-server` 必须是两个容器都挂载的卷。若未设置，则不会删除任何内容。被删除的缓存只意味着其会话需要再读取一次提示词。这些限制是可选的：若你不需要对大小设上限，主机上的 cron 任务同样可以实现，例如 `find /srv/ai/slots -name '*.bin' -mtime +2 -delete`（正在使用的缓存每次被换出时都会重新保存，因此其时间会不断重新开始计算）。从外部删除的文件对钩子无害；缺少的文件只会被视为未命中。
- **更换路由的模型之后**，请删除文件夹中该路由的文件（其文件名以路由名开头）：由其他权重保存的状态不适用于新模型。
- **每次切换都要付出一次保存和一次恢复的代价。** 开启[自动注释](books/annotations.md)后，一段之后会接着进行注释和重写，因此一轮操作可能切换三到四次。在依赖它之前，请先测量一轮的耗时。
- **重启代理**会忘记槽位中是哪个会话；重启后的第一个请求会将其文件恢复覆盖到槽位上，而槽位中原有的内容会丢失。
- **路由器在钩子不知情的情况下卸载了某个模型**（`models_max` 设置有误，或有其他客户端介入），此时槽位为空，而钩子仍认为最后一个会话在其中。对同一会话的请求只需重新读取提示词；对另一个会话的请求则会先用空槽位覆盖该会话的文件，因此其缓存也会丢失。两种情况都只是多一次预填充的代价。
- **`llama-server` 不记录普通的保存或恢复**（它记录所追加或恢复的上下文检查点，以及错误）。其操作结果在其回答中，代理的日志（启用 `LITELLM_LOG=INFO` 时）中也有记录，钩子发出的每个调用都有一行，例如 `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`、`… save <file> …: ok (…)` 或 `… restore … miss (400)`；会话第一个请求未命中属正常情况（llama-server 会记录为 "Unable to restore slot, no available space in KV cache or invalid slot save file"）。真正的问题以警告形式出现：证书与地址不匹配、API 密钥被拒绝，或服务器无响应。
- **提示词在哪里不同。** 对于会话中第一个之后的每个请求，日志中都有一行说明该提示词与该会话上一个提示词相同的范围，例如 `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`。服务器只能复用到该处的缓存（一条消息大约为每个 token 三到四个字符），因此当第二个请求几乎又要重新读取全部内容时，应查看这一行：其所指出的消息即为开头发生变化的那一条。该行还会比较两个提示词的最后一条消息，其中包含发生变化的内容（设定、这一段、任务）：当故事增加了一段时，旧的最后一条消息就不再位于原来的位置，而两条最后消息所共有的字符数，则表示例如设定在两次之间相同的程度。
- **如果没有任何反应**，请查看代理的日志（`docker logs litellm-proxy 2>&1 | grep slot_cache`）：钩子工作时，每个调用都会有一行记录；对于没有 `slot_cache` 的路由，Marginalia 的请求只会报告一次（`not used for <route>: no model_info.slot_cache`）。完全没有 `slot_cache` 行，说明钩子没有运行：容器中仍是旧的文件或配置。若编辑器替换了挂载到容器中的单个文件（`./slot_cache.py:/app/slot_cache.py`），容器会继续显示其创建时的版本；`docker compose up -d
  --force-recreate` 会读取新文件（普通重启则不会）。对于过旧的文件，`docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` 会输出 0。
- **在 Docker 中**，代理和 `llama-server` 是两个容器，因此 `--slot-save-path` 的文件夹是两个容器都挂载的主机目录（代理需要对其有写入权限：它会删除旧文件，并更新正在使用的文件的时间）；钩子文件放在 LiteLLM 配置旁边；`api_base` 是代理可以访问的地址，当服务器使用 TLS 时，应包含证书中的主机名：

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  在路由的 `slot_cache` 中设置 `save_path: /slots` 和 `models_max: 1`。
- 钩子使用 `CustomLogger.async_pre_call_deployment_hook`，LiteLLM 在选定部署之后、发送请求之前调用它（1.106.5 版已用真实路由器和替身服务器测试）；请对照你所运行的版本进行检查。通过重启单个服务器（llama-swap）在同一地址后切换模型的代理不受支持：槽位调用会到达当前已加载的任意模型。以上内容均未针对真实的 `llama-server` 路由器运行过；测试使用的是一个替身，用来检查钩子发出的调用。
<a id="freeing-the-gpu-for-another-model"></a>

## 释放 GPU 给另一个模型

装着语言模型的 GPU 没有空间同时容纳图像模型或语音模型。在路由模式下使用 `llama-server` 时，可以再加一个钩子，按请求卸载模型：[GPU 锁](administration/gpus.md)（图像或语音服务器在启动自身之前所用的锁）的某个步骤会向代理发送一个请求来要求卸载，使用它处理其他所有请求时所用的同一个密钥：

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

带有请求头 `X-Llama-Unload` 的请求由钩子自己应答（HTTP 200，文本为 `unloaded: <names>`），永远不会到达模型。钩子会列出路由器中的模型（`GET /models`），卸载其中已加载的模型（`POST /models/unload` 加上名称），并等待路由器报告内存中已不再有任何模型（`loaded`、`loading` 和 `unloading` 保存内存状态），最多等待 `LLAMA_UNLOAD_WAIT_SECONDS`（120）。如果无法连接服务器、服务器返回错误，或等待之后模型仍然存在，则应答为错误（HTTP 502）。这样，若某个锁的第一个获取步骤就是这个请求，就不会在已占满的 GPU 上启动另一个模型。模型会自行恢复：下一个普通请求会让路由器重新加载它，因此语言模型的插件 *after* 请求可以为空。请求体中的模型名称只需是某条路由能接受的名称（例如使用路由 `openai/*` 时，名称以 `openai/` 开头，如 `openai/unload`）；真正起作用的是请求头。若名称不被任何路由接受，则会在模型已被卸载*之后*返回 400，因为钩子在 LiteLLM 检查名称之前运行。卸载会发送到每个路由中带有 `unload` 的服务器，而不只是某一个模型。

该钩子是一个独立的脚本 `gpu_unload.py`，与 `slot_cache.py` 并列且与之串接。两者互不依赖：`gpu_unload.py` 无需缓存钩子也能工作。两者都串接时，`gpu_unload.py` 会在 LiteLLM 的回调中找到缓存钩子，并调用它的 `release_server`，该函数**会先保存每个 slot 的缓存，并忘记其中原有的内容**，这样下一个请求便能将其会话的缓存恢复到重新加载的模型中。（若没有这一调用，缓存钩子仍会认为会话在 slot 中，因而不会恢复任何内容：每次切换后整个提示词都会被重新读取。）

```python
"""LiteLLM hook: frees the GPU on request, so that another model (images, speech) can use it.

A request with the header `X-Llama-Unload: all` (any chat completion body; the model only has to be a route) is answered
by this hook with HTTP 200 and never reaches a model. The hook unloads every model that a `llama-server` in router mode
reports as loaded and returns when the router says none is in memory any more (at most LLAMA_UNLOAD_WAIT_SECONDS, default
120; then the answer is an error, so that what follows does not start on a full GPU). The next ordinary request makes the
router load the model again.

It is independent of slot_cache.py. When that hook is chained too, this one asks it to save the caches of the slots first and
to forget them (its `release_server`), so the cache of a session is restored into the reloaded model; without it nothing is saved.

Switch it on for the servers to be unloaded with `unload` in the `model_info` of their route (one wildcard entry covers a
router), and chain it with the other hooks:

    model_list:
      - model_name: "*"
        litellm_params: {model: "openai/*", api_base: "http://llama-server:8080/v1", api_key: none}
        model_info:
          unload: true
    litellm_settings:
      callbacks: [slot_cache.proxy_handler_instance, gpu_unload.proxy_handler_instance]
"""
import asyncio
import logging
import os

import httpx
import litellm
from litellm.integrations.custom_logger import CustomLogger

try:
    from litellm._logging import verbose_proxy_logger as log
except ImportError:
    log = logging.getLogger("gpu_unload")

UNLOAD_HEADER = "x-llama-unload"
WAIT_SECONDS = float(os.environ.get("LLAMA_UNLOAD_WAIT_SECONDS", "120"))
# the states of a model in the router's /models that still hold its memory
IN_MEMORY = ("loaded", "loading", "unloading")


def server_root(api_base):
    """The router's own endpoints are at the root of llama-server, the API address of the model ends in /v1."""
    base = api_base.rstrip("/")
    return base[:-len("/v1")] if base.endswith("/v1") else base


def status_of(model):
    """The state a router reports for a model: `{"status": {"value": "loaded"}}` (or a plain string); "unknown" without one."""
    raw = model.get("status") if isinstance(model, dict) else None
    return str(raw.get("value", "unknown") if isinstance(raw, dict) else raw or "unknown")


def servers():
    """(server root, api key) of every deployment of the config that has `unload` in its model_info, once per server."""
    try:
        from litellm.proxy.proxy_server import llm_router
    except ImportError:
        return {}
    found = {}
    for deployment in (llm_router.model_list if llm_router else []):
        params = deployment.get("litellm_params") or {}
        if (deployment.get("model_info") or {}).get("unload") and params.get("api_base"):
            key = params.get("api_key")
            if isinstance(key, str) and key.startswith("os.environ/"):
                key = os.environ.get(key[len("os.environ/"):])
            found.setdefault(server_root(params["api_base"]), key)
    return found


class GpuUnloadHook(CustomLogger):
    def __init__(self):
        super().__init__()
        self.client = httpx.AsyncClient(timeout=None)

    async def models(self, root, headers):
        response = await self.client.get(f"{root}/models", headers=headers, timeout=15)
        response.raise_for_status()
        body = response.json()
        listed = body.get("data", []) if isinstance(body, dict) else body
        return [m for m in listed if isinstance(m, dict) and m.get("id")]

    async def release_caches(self, root, api_key):
        """Lets the hooks that keep a cache of the slots (slot_cache.py) save it: they are found among the callbacks of LiteLLM."""
        for callback in litellm.callbacks:
            release = getattr(callback, "release_server", None)
            if callable(release):
                await release(root, api_key)

    async def unload_all(self, root, api_key):
        """Unloads every model of the router at `root` that is in memory and waits for the memory. Returns their names."""
        headers = {"Authorization": "Bearer " + api_key} if api_key and api_key != "none" else {}
        await self.release_caches(root, api_key)
        names = [m["id"] for m in await self.models(root, headers) if status_of(m) in ("loaded", "loading", "unknown")]
        for name in names:
            response = await self.client.post(f"{root}/models/unload", json={"model": name}, headers=headers, timeout=WAIT_SECONDS)
            response.raise_for_status()
        deadline = asyncio.get_running_loop().time() + WAIT_SECONDS
        while any(status_of(m) in IN_MEMORY for m in await self.models(root, headers)):
            if asyncio.get_running_loop().time() > deadline:
                raise RuntimeError(f"{root}: still in memory after {WAIT_SECONDS:.0f} s")
            await asyncio.sleep(1)
        return names

    # runs before the router looks at the request
    async def async_pre_call_hook(self, user_api_key_dict, cache, data, call_type):
        headers = (data.get("proxy_server_request") or {}).get("headers") or {}
        if not any(str(name).lower() == UNLOAD_HEADER for name in headers):
            return None
        from fastapi import HTTPException
        found = servers()
        if not found:
            raise HTTPException(status_code=404, detail="gpu_unload: no deployment with model_info.unload")
        unloaded = []
        try:
            for root, key in found.items():
                unloaded += await self.unload_all(root, key)
        except Exception as e:
            log.warning("gpu_unload: failed: %s", e)
            raise HTTPException(status_code=502, detail=f"gpu_unload: {e}")
        log.info("gpu_unload: unloaded %s", ", ".join(unloaded) or "nothing")
        # a mock answer: LiteLLM answers the request itself (HTTP 200) and never calls a model, which would load one again
        data["mock_response"] = "unloaded: " + (", ".join(unloaded) or "nothing")
        return data


proxy_handler_instance = GpuUnloadHook()
```

在 Docker 中，该文件必须像 `slot_cache.py` 那样放在容器内：在代理的 `volumes` 中添加 `- ./gpu_unload.py:/app/gpu_unload.py:ro`，紧挨着 `slot_cache.py` 那一行。若文件不存在，启动时会显示为 `ImportError: Could not import proxy_handler_instance from gpu_unload`（真正的原因，即 `FileNotFoundError` 或脚本本身的错误，会在日志中位于其上方几行处）。

在 `litellm_config.yml` 中将两者串接，并用 `unload` 标记路由：

```yaml
model_list:
  - model_name: "*"
    litellm_params: {model: "openai/*", api_base: "http://llama-server:8080/v1", api_key: none}
    model_info:
      slot_cache: true
      unload: true

litellm_settings:
  callbacks: [slot_cache.proxy_handler_instance, gpu_unload.proxy_handler_instance]
```

将其设为一个 [GPU 步骤](administration/gpus.md)，使用请求头 `Authorization: Bearer <key>` 和 `X-Llama-Unload: all` 以及示例中的请求体，向上述地址发送 `POST` 请求（请求头以加密方式存储），并把它放在图像和语音服务器的锁的获取步骤中的第一位。

另一个模型的服务器也可以通过同一个代理控制。LiteLLM 会将某个路径转发到另一个地址（`general_settings: pass_through_endpoints`、`path`、`target` 和 `include_subpath: true`；会检查代理的密钥，而客户端的 `Authorization` 请求头**不会**被转发，因此目标服务有其自己在 `headers` 中的密钥）。一个位于语音或图像服务器旁、用于停止和启动其容器的小型服务就是这样的目标：[`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)（服务就绪时其中的 `POST /<container>/start` 会应答，退出后则为 `stop`；其 README 中包含 compose 示例以及语音服务器的 LiteLLM 路由，`/v1/audio/speech`）。

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

检查内容：LiteLLM 1.108.3 配合一个模拟路由器，该路由器报告 `loaded`、`unloading` 和 `unloaded`（等待会一直保持应答，直到状态变为 `unloaded`；服务器宕机时返回 502；两个钩子都串接时，slot 在卸载前被保存，并由下一个请求恢复；只有 `gpu_unload.py` 时，不会调用其他任何东西；透传能到达其目标，并以其自身的 `headers` 值替换客户端发送的值；没有密钥的请求会被拒绝）。还在 LiteLLM 后面的真实 `llama-server` 路由器（一个 31B 模型）上运行过：请求在 1 到 2 秒后返回 `unloaded: <model>`，路由器记录了 `stopping model instance` 和 `exited with status 0`（进程已退出，因此内存已释放）；在没有任何模型加载时发送的第二个请求立即返回 `unloaded: nothing`；而下一个普通请求用了大约一分钟，因为路由器重新加载了模型。如果你的 `GET /models` 对状态的命名不同，请在脚本中修改 `IN_MEMORY`。
