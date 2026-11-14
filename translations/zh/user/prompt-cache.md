---
label: 提示词缓存与会话 ID
order: 975
icon: database
---

# 提示词缓存与会话 ID

模型服务器会保留刚刚读取过的提示词（即*提示词缓存*），这样下一个以相同文本开头的请求就不必再读一遍。对于长篇故事，这意味着等待几秒与等待几分钟的区别。缓存只在新提示词与旧提示词开头相同的范围内有效（参见[缓存为何可能不起作用](#why-a-cache-may-not-help)）。Marginalia 还可以告诉服务器**一个请求属于哪类工作**，这样服务器或其前面的代理就能为每类工作分别保留一份缓存。

<a id="what-is-sent"></a>

## 发送的内容

Marginalia 为每类工作发出的每个请求都带有以下 HTTP 请求头：

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

同一用户做同一类工作时，无论是哪本书，该值都相同；不同用户之间、不同类工作之间，该值则不同：

| 类型 | 请求 |
|---|---|
| 生成 | 新的部分、*Regenerate* 和 *Swipe*：它们的提示词共享同一个故事，因此共享同一个值。 |
| 冒充 | *Impersonate*。 |
| 标注 | *Annotate with AI* 和自动标注；两阶段模式下的第二个模型有自己的值。 |
| 重写 | *Redraft* 以及自动标注的重写。 |
| 摘要 | 摘要。 |
| 智能体 | [智能体](agents.md)的请求；每次运行都有自己的值，因为它会积累自己的对话。 |
| 帮助 | 帮助助手。 |
| 插件 | 每个插件都有自己的值：*Reviewer*、*Side Query*（其标签页标题不同）、*Lore Generator*、*Arc Outline*、*Converter* 和 *Manga Generator*。 |

不属于任何类型的请求（列出模型、测试连接、统计 token）不发送该请求头。

该值是对用户与类型取的带密钥哈希（密钥来自[数据文件夹](administration/index.md#the-data-folder)中的 `secret.key`）。它既不暴露用户，也不暴露书籍，无法由此推断出它属于谁；另一个安装实例对同一用户会得出不同的值。每个用户、每类工作只有一个值，而不是每本书一个，因此服务器需要保留的缓存数量不会超过这个上限。

所有提供方都会发送该请求头：兼容 OpenAI 的提供方，以及 [Anthropic Claude](extensions/anthropic.md) 和 [Google Gemini](extensions/google-genai.md) 提供方。不使用该请求头的服务器会忽略它。目前没有开关可以关闭它。

<a id="why-a-cache-may-not-help"></a>

## 缓存为何可能不起作用

服务器只会复用缓存中与新提示词**从第一个 token 起**完全相同的那一部分；第一处差异之后的内容都会被重新读取，无论其中有多少内容未变。请看请求开始时打印的 `llama-server` 行：

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` 是新请求保留的已缓存提示词所占的比例。接近 1 时缓存被使用；此处只有 15%，说明提示词在靠前的位置就与缓存不同，无论缓存是从磁盘恢复的，还是一直保留在槽位中。

采用**滑动窗口**的模型（例如 Gemma）以及混合模型不能丢弃缓存的末尾后从中间继续。根据 `llama.cpp` 的源代码，`llama-server` 只有在新提示词完整地延伸了已缓存的提示词，或者在两者出现差异的位置之前（含该位置）存在一个**上下文检查点**时，才能从已缓存的提示词继续。它只在用户消息的开头（相隔至少 8192 个 token）以及提示词末尾附近设置检查点，并将其保存在槽位文件中（INFO 日志行 `appended N context checkpoint(s)` 和 `restored N context checkpoint(s)` 会说明何时发生）。若没有可用的检查点，它会重新读取整个提示词，无论 `f_keep` 怎么说，并且只在 trace 级别报告这一点（`-lv 4`：`forcing full prompt re-processing due to lack of cache data`）。因此，生成、标注和重写的故事会被发送为一连串消息，每个部分都是用户消息之后模型的一次回答：用户的最后一条消息会设置检查点，下一个请求正是从这里开始出现差异；而新的部分只会在末尾追加消息。从日志读取冷预填充（prefill）时间：这里第一条 `prompt processing, n_tokens = 4096` 行大约在 7 秒后出现；如果复用了前缀，它会在一两秒后出现。

[提示词](books/prompts.md#how-the-prompt-is-put-together)以**系统消息**开头，故事位于其后，因此系统消息中的内容必须在两次请求之间保持不变。在默认的主模板中，系统消息依次包含以下内容：设定块、摘要和作者注释（标注）。当其中任何一项在两次请求之间发生变化时（这很常见：新文本激活了其他设定条目，添加了摘要，或新增、解决了注释），缓存只在该处之前有效，其后的整个故事都要重新读取。当故事超出容量时，最旧的部分会先从提示词中移除，这使得每一轮故事的起点都会后移一个部分。以下做法有助于解决这一问题：

- 将那些随部分变化的条目的插入方式设为 **Before User Prompt**（参见[顺序与插入](lorebooks/entries.md#order-and-insertion)）：它们会被放到提示词的末尾，位于故事之后，不会干扰缓存。始终为真的内容保留在设定块中。
- 将你不使用的模块从主模板中去掉（例如，如果你不向模型提供注释，就去掉作者注释），使系统消息保持不变。
- 打开某个部分菜单中的 **Compare Prompt**，查看缓存在哪里结束：它会将该部分的提示词与某个更早部分的提示词进行比较（默认是最近的那个；列表框会列出它之前的部分及其 swipe 版本），并标出第一条不同的消息。参见 [Compare Prompt](#compare-prompt)。

### Compare Prompt

某个部分菜单中的 **Compare Prompt**（桌面端和手机端）用于回答*为什么这一部分没有用上缓存？*它会列出两份提示词的消息（先是系统消息，然后是每个部分的一条用户消息和一条模型回答，最后是本轮的用户消息），并为每条消息标注状态：相同、已修改、已新增或已删除。第一条不同的消息会被高亮显示，之后的消息标记为*重新读取*，因为服务器会从第一处差异起重新读取所有内容。表格上方显示：

- 第一处差异的位置（消息及字符），以及从开头起有多少提示词相同（以字符数计，并大致以 token 数计）；
- 对差异的解释：**系统消息**不同（越狱提示、模板、设定、摘要或注释在两次请求之间发生了变化：这是最常见的原因）；**故事中间**的某个部分不同（它被编辑、重新生成或被摘要替换）；**故事的起点后移了**（最旧的部分被去掉或被摘要）；或者差异仅出现在**较早提示词的最后一条消息**上，即该轮的用户消息，而故事并不会重复这条消息。这最后一种情况是下一个部分或 swipe 能够复用的最大范围；如果服务器仍然没有使用缓存，则原因不在提示词（请检查槽位、模型和会话 ID）。

选中某条消息会并排显示两段文本，从第一处差异之前一点开始。比较的只有消息：模型、请求中的工具以及服务器槽位都不属于所存储的提示词。该部分需要有存储的提示词（由作者手动输入的部分，以及在保存提示词之前生成的部分，都没有提示词）。

下面的请求类型关系到切换时保留什么；提示词决定其中有多少会被用上。
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 使用方式：llama.cpp 单槽位加磁盘缓存

如果服务器的内存不够存放缓存，这一节就是为你准备的。本地的 `llama-server` 如果有足够内存容纳多个槽位，就能为每类工作各保留一份缓存，无需额外设置。只有**一个**槽位时，每切换一类工作，之前的缓存都会被丢弃：故事的缓存、某个部分的注释、之后的重写稿和下一次生成，每次都要重新读取它们的提示词。

解决办法是把每类工作的缓存都保存在磁盘上，遇到该类请求时再把它换入唯一的槽位。`llama-server` 可以把槽位保存为文件并再恢复；前面放一个代理，它通过请求头判断请求属于哪一类。下面的示例是 [LiteLLM](https://docs.litellm.ai/) 的一个钩子。

**要求：** `llama-server` 需为 2026-10-08 或之后的版本（llama.cpp PR #26004，*preserve context checkpoints across slot save/restore*）。更早的版本只写入和恢复槽位的内存，而不包括其 [上下文检查点](#why-a-cache-may-not-help)，因此带滑动窗口的模型（例如 Gemma）或混合模型，每次恢复后都要把整个提示词重新读一遍，即使其中大部分没有变化。用 `llama-server --version` 检查构建版本；使用浮动标签的容器镜像（`full`、`server`……）在你重新拉取之前不会更新。

以一个槽位和一个用于存放文件的文件夹启动 `llama-server`：

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

把下面的内容保存为 LiteLLM 配置旁边的 `slot_cache.py`：

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

然后在 LiteLLM 的 `config.yaml` 中启用它：在回调中加入它，并在通往 `llama-server` 的路由的 `model_info` 中设置 `slot_cache`。一条通配符条目就够了，无需在 LiteLLM 中列出任何模型（服务器知道有哪些模型，客户端指定它要的那一个）：

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

钩子本身不需要任何地址：它使用请求所走路由的 `api_base` 和 `api_key`（槽位位于服务器的根路径下，因此末尾的 `/v1` 会被去掉）。只有带有 `slot_cache` 的路由会被处理；请求其他模型（例如云端模型）时即使带有该请求头，也会直接透传。多个服务器可以并存，每个服务器有自己的槽位。使用通配符时，路由对应的就是客户端请求的模型，因此文件按模型（`qwen3-32b`）分别保存；如果某些模型需要不同的设置，你仍可以在通配符旁边按名称列出它们。LiteLLM 会把模型名中第一个 `/` 之前的部分当作提供方，因此对于名称中包含该符号的模型（`org/model`，例如路由器预设中的模型），请使用路由 `model_name: "openai/*"`，并在 Marginalia 中把模型填为 `openai/org/model`：钩子会去掉 `openai/`，文件和槽位调用使用服务器所知道的名称。路由及其 `model_info` 来自 `model_list`；如果代理启动时没有配置文件（只有 `litellm --model ...`），就没有路由，钩子在那里不做任何事。一条通配符条目就是这种替代方案。

**`llama-server` 路由器模式**（一个地址后面有多个模型，按需加载）也可以使用：每个模型都是拥有自己槽位的独立实例，因此钩子按服务器、模型和槽位保存状态，并在槽位调用的请求体中写明模型（即客户端使用的名称，去掉 LiteLLM 的 `openai/`；设置 `model` 可以指定另一个名称，适用于路由名称不是模型名的情况）。它需要 2026 年 2 月或之后的 llama.cpp，该版本会把 `/slots` 转发给实例（之前它只回复 `400 Invalid action`）。

当路由器只能同时保留部分模型时（`--models-max`，默认为 4），请求另一个模型会使其卸载最久未使用的模型，而该模型的槽位及其中的缓存也随之消失。钩子遵循同样的顺序（在 `slot_cache` 中设置 `models_max`，或 `LLAMA_MODELS_MAX`，使其与 `--models-max` 相同；0 表示不限制），并且在发出那个会让路由器卸载它的请求**之前**保存该槽位。为此，代理必须是路由器的唯一客户端：以其他方式到达路由器的请求不会被看到。保存操作绝不会加载路由器已卸载的模型。

它的工作方式是：某个会话的缓存只在**该槽位需要给另一个会话使用时**才写入磁盘（另一类请求、另一个模型，或路由器卸载模型），而不是每次请求后都写。每次生成后写入 3 GB 只会拖慢服务器。因此，在第一次切换之后才会出现文件，例如生成、注释、再生成：注释操作会先保存生成的缓存。在 `save_idle_seconds` 中设置（或 `LLAMA_SAVE_IDLE_SECONDS` 中的 `slot_cache`）后，如果一段时间没有请求，缓存也会被写入，因此你停止工作后不久文件夹中就会有它；新的请求会重新开始等待，而已经在磁盘上的缓存在下一次切换时不会被再次写入。钩子只在槽位空闲时发送保存、恢复或清除调用（它会询问 `GET /slots`、`is_processing`；如果没有该端点，则立即发送）：`llama-server` 会在请求运行期间暂停此类调用，并且只在每个请求结束时释放一个被暂停的调用，因此排在它后面到达的其他客户端请求可能会永远等待。保存写入期间，服务器不做任何其他事情。

细节如下：当请求来自与槽位中不同的会话时，钩子会以槽位中原会话的名称保存该槽位，恢复新会话的文件（如果还没有文件，则清空槽位），然后放行请求。文件**按路由**保存（`model_name`）：保存的状态只适用于写入它的模型，因此文件以路由和会话命名，两个路由即使使用同一服务器和文件夹，也不会共用文件。随后服务器只读取与已恢复提示词不同的部分。无法恢复的缓存、已经不存在的文件，或者无响应的服务器，都只意味着需要重新读取提示词；钩子永远不会因此导致请求失败。

注意事项：

- **大小。** 一个文件包含槽位的内存**及其上下文检查点**，因此大小是两者之和：对于长篇故事和大型模型，可能从几百兆字节到好几吉字节。实测示例：一个 31 B 模型、提示词为 18,000 个 token 时，文件大小在 1.3 到 11 GB 之间，最大的那个包含约 18 个检查点（每个约 450 MB），另外还有约 1.5 GB 的缓存。服务器默认最多保留 32 个检查点，每次切换都要写入和读取全部检查点。`--ctx-checkpoints N` 中的 `llama-server` 可以限制其数量，这会让文件变小、切换变快，但检查点只有在下一个提示词与缓存中的提示词首次不同的位置恰好在它处或之后时才有用。新的部分或重新生成的候选内容在最后一条用户消息的开头处开始不同（即最新的检查点），而注释或重写稿则可能改动前面好几个部分，因此请保留与故事回溯范围相当数量的检查点（查看钩子的 `prompt:` 日志行，它们会说明每个请求在哪里不同）。每个路由、每个用户、每类工作只有一个文件，且原地覆盖，因此文件夹会保持这么小。
- **速度。** 请确认切换比重新读取提示词更快（钩子会记录每次保存和恢复的大小与耗时）。上面的示例运行在 NVMe 磁盘和 AMD Radeon AI PRO R9700 上：保存 11 GB 用了 6 秒，恢复 7.6 GB（对应 20,000 个 token）用了 8 秒，一次切换约 14 秒；而以每秒 440 个 token 的速度重新读取同样长度的提示词约需 45 秒，并且预填充耗时会随提示词增长。没有检查点时，同样的文件约为 1 GB，一次切换约 5 秒。
- **内存。** 槽位的检查点保存在服务器的**系统内存**中（而不是 GPU 的内存），最多 `--ctx-checkpoints` 个（默认 32 个，大型模型每个数百兆字节），保存或恢复时都会读写全部检查点。`llama-server` 还会在自己的内存缓存（`--cache-ram`，默认 8 GiB）中保留它放弃的槽位的提示词；钩子的文件起的是同样的作用，因此在内存紧张的机器上可以将其设为 0。当机器开始使用交换分区时，保存和恢复会慢好几倍，因为检查点必须先从交换分区读回：在查看磁盘之前，先看 `free -h`。
- **限制。** 如果不设限制，文件夹会随路由、用户和工作类别的数量而增长。在路由的 `slot_cache` 中设置 `max_files`（文件数量）和/或 `max_gib`（文件总大小）（或在代理的环境中设置 `LLAMA_CACHE_MAX_FILES` 和 `LLAMA_CACHE_MAX_GIB`，对所有路由生效），钩子就会在每次保存后删除**该路由**中**最久未被使用**的缓存：恢复缓存也算作使用。它永远不会删除刚刚保存的缓存或即将恢复的缓存（因此，一个路由若轮流使用两个会话，无论限制为多少，至少会保留两个文件），并且只处理该路由的文件，其他路由和文件夹中的其他文件都是安全的。单个缓存如果大于 `max_gib`，会被保留（并在日志中给出警告）。删除操作需要文件夹：将 `save_path`（或 `LLAMA_SLOT_SAVE_PATH`）设为代理所看到的 `--slot-save-path` 文件夹；在容器中使用 `llama-server` 时，它应是两个容器都挂载的卷。没有设置时，什么都不会被删除。被删除的缓存只意味着其会话需要重新读取一次提示词。这些限制都是可选的：如果你不需要限制大小，主机上的定时任务同样可以完成，例如 `find /srv/ai/slots -name '*.bin' -mtime +2 -delete`（正在使用的缓存每次被换出时都会重新保存，因此其使用时间会重新计算）。从外部删除的文件对钩子无害；缺失的文件只是一次未命中。
- **更改路由的模型之后**，请删除文件夹中该路由的文件（文件名以路由名开头）：其他权重保存的状态与新模型不匹配。
- **每次切换都要付出一次保存和一次恢复的代价。** 开启[自动注释](books/annotations.md)后，一个部分之后会跟着一次注释和一次重写，因此一轮操作可能切换三到四次。依赖它之前，请先测量一轮操作的耗时。
- **代理重启**后，钩子会忘记哪个会话在槽位中；重启后的第一个请求会把对应的文件恢复到槽位上，槽位原有的内容随之丢失。
- **如果路由器在钩子不知情的情况下卸载了某个模型**（`models_max` 配置错误，或其他客户端），那么槽位是空的，而钩子仍以为最后一个会话在其中。对同一会话的请求只需重新读取提示词；对另一会话的请求则会先把空槽位保存到该会话的文件上，因此它的缓存也会丢失。两种情况都只会增加一次预填充的开销。
- `llama-server` **不记录**普通的保存或恢复操作（它只记录追加或恢复的上下文检查点，以及错误）。实际发生的情况可以在其返回结果中看到，代理的日志（开启 `LITELLM_LOG=INFO`）中也有记录，钩子每发起一次调用都会有一行，例如 `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`, `… save <file> …: ok (…)` or `… restore … miss (400)`；会话第一个请求出现的未命中是正常的（llama-server 会记录 "Unable to restore slot, no available space in KV cache or invalid slot save file"）。真正的问题会以警告形式出现：证书与地址不匹配、API 密钥被拒绝、服务器无响应。
- **提示词在哪里不同。** 对于一个会话中第一个之后的每个请求，日志都会有一行，说明该提示词与同一会话的上一个提示词相同到什么程度，例如 `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`。服务器只能复用到这个位置为止的缓存（一条消息大约相当于三到四个字符的 token），因此当第二个请求几乎要重新读取全部内容时，就应看这一行：它指出的那条消息就是开头发生变化的消息。这一行还会比较两个提示词的最后一条消息，其中包含发生变化的内容（设定、部分、任务）：当故事增加了一个部分时，旧的最后一条消息不再位于原来的位置，而两条最后消息共有的字符数，就说明例如设定在两次请求中有多少是相同的。
- **如果什么都没有发生**，请查看代理的日志（`docker logs litellm-proxy 2>&1 | grep slot_cache`）：钩子工作时，每次调用都会有一行；Marginalia 对于没有 `slot_cache` 的路由所发出的请求，只会报告一次（`not used for <route>: no model_info.slot_cache`）。如果根本没有 `slot_cache` 行，说明钩子没有运行：容器中仍然是旧的文件或旧的配置。如果编辑器替换了挂载进容器的单个文件（`./slot_cache.py:/app/slot_cache.py`），容器仍会显示创建时的版本。`docker compose up -d
  --force-recreate` picks the new one up (a plain restart does not). `docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` 会输出 0，说明文件过旧。
- **在 Docker 中**，代理和 `llama-server` 是两个容器，因此 `--slot-save-path` 的文件夹应是两个容器都挂载的宿主机目录（代理需要对它有写入权限：它会删除旧文件，并更新被使用文件的时间）；钩子文件放在 LiteLLM 配置旁边；`api_base` 是代理能够访问的地址，如果服务器使用 TLS，则需使用证书中的主机名：

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  并在路由的 `slot_cache` 中设置 `save_path: /slots` 和 `models_max: 1`。
- 钩子使用 `CustomLogger.async_pre_call_deployment_hook`，LiteLLM 会在选定部署之后、发送请求之前调用它（1.106.5 版本已在真实的路由器和替代服务器上测试过）；请对照你所用的版本进行检查。通过重启单个服务器（llama-swap）在同一地址后切换模型的代理不受支持：槽位调用会到达当前加载的任意模型。以上内容都没有在真实的 `llama-server` 路由器上运行过；测试使用的是替代服务器，它检查钩子发出的调用。
<a id="freeing-the-gpu-for-another-model"></a>

## 为另一个模型释放 GPU

装有语言模型的 GPU 没有多余的显存再放下图像模型或语音模型。在路由器模式下使用 `llama-server` 时，第二个钩子可以按请求卸载模型，因此需要 GPU 的插件（[Image Generator](extensions/image-generator.md#one-gpu-for-the-model-and-the-images)、有声书）会向代理发送一个请求来申请，使用它处理其他所有请求时所用的同一个密钥：

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

请求带有请求头 `X-Llama-Unload` 时，由钩子本身应答（HTTP 200，文本为 `unloaded: <names>`），不会到达任何模型。钩子会列出路由器中的模型（`GET /models`），卸载其中已加载的模型（`POST /models/unload` 加上名称），并等待，直到路由器报告内存中已没有任何模型（`loaded`、`loading` 和 `unloading` 表示占用内存的状态），最多等待 `LLAMA_UNLOAD_WAIT_SECONDS`（120）秒。如果无法连接服务器、服务器返回错误，或等待结束后模型仍在内存中，则返回错误（HTTP 502）。因此，插件将此请求作为 *before* 请求发送时，不会在显存已满的 GPU 上启动另一个模型。模型会自动恢复：下一个普通请求会让路由器重新加载它，所以插件的 *after* 请求对语言模型可以为空。请求体中的模型名只需是某个路由接受的名称（使用路由 `openai/*` 时，名称以 `openai/` 开头，例如 `openai/unload`）；真正起作用的是请求头。不被任何路由接受的名称，会在模型卸载之后以 400 *after* 应答，因为钩子先于 LiteLLM 检查名称运行。卸载会发送到所有路由含有 `unload` 的服务器，而不是仅一个模型。

钩子是一个独立的脚本 `gpu_unload.py`，与 `slot_cache.py` 放在一起并与之串联。两者相互独立：`gpu_unload.py` 无需缓存钩子也能工作。两者串联时，`gpu_unload.py` 会在 LiteLLM 的回调中找到缓存钩子，并调用其 `release_server`，该方法**先保存每个槽位的缓存，并清空其中原有的内容**，这样下一个请求便会把其会话的缓存恢复到重新加载的模型中。（如果没有这一调用，缓存钩子仍会认为会话在槽位中，因而不会恢复任何内容：每次切换之后，整个提示词都会被重新读取。）

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

在 Docker 中，该文件必须像 `slot_cache.py` 一样位于容器内：在代理的 `volumes` 中、紧挨着 `slot_cache.py` 那一行，添加 `- ./gpu_unload.py:/app/gpu_unload.py:ro`。若文件不存在，启动时会显示为 `ImportError: Could not import proxy_handler_instance from gpu_unload`（真正的原因，即 `FileNotFoundError` 或脚本本身的错误，会在日志中它上方几行显示）。

在 `litellm_config.yml` 中串联这两个钩子，并用 `unload` 标记路由：

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

在 Image Generator（或任何带有这两个请求的插件）中，将 *before* 请求的方法设为 `POST`，地址同上，并带上请求头 `Authorization: Bearer <key>` 和 `X-Llama-Unload: all` 以及示例中的请求体；请求头以加密方式保存。

另一个模型的服务器也可以通过同一个代理进行控制。LiteLLM 会把某个路径转发到另一个地址（`general_settings: pass_through_endpoints`、`path`、`target` 和 `include_subpath: true`；会检查代理的密钥，而客户端的 `Authorization` 请求头**不会**被转发，因此目标服务有其自己的密钥，位于 `headers` 中）。一个与语音或图像服务器并排运行、可停止和启动其容器的小型服务就是这样的目标：[`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)（`POST /<container>/start` 在其中的服务就绪时应答，`stop` 在其退出时应答；其 README 中有 compose 示例，以及语音服务器的 LiteLLM 路由，`/v1/audio/speech`）。

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

检测内容：LiteLLM 1.108.3，搭配一个模拟路由器，它报告 `loaded`、`unloading` 和 `unloaded`（等待会一直持有应答，直到状态变为 `unloaded`；服务器宕机时返回 502；两个钩子串联时，槽位在卸载前被保存，并由下一个请求恢复；仅使用 `gpu_unload.py` 时，不会调用其他任何东西；直通功能到达了其目标，并用自身的 `headers` 值替换了客户端发送的值，且拒绝了不带密钥的请求）。还在 LiteLLM 后面的真实 `llama-server` 路由器（一个 31B 模型）上运行过：请求在 1 到 2 秒后返回 `unloaded: <model>`，路由器记录了 `stopping model instance` 和 `exited with status 0`（进程已退出，因此内存已释放），在没有加载任何模型时的第二个请求立即返回 `unloaded: nothing`，而下一个普通请求耗时约一分钟，因为路由器重新加载了模型。如果你的 `GET /models` 对状态的命名不同，请在脚本中修改 `IN_MEMORY`。
