---
label: プロンプトキャッシュとセッション ID
order: 975
icon: database
---

# プロンプトキャッシュとセッション ID

モデルサーバーは、読み込んだばかりのプロンプト（*プロンプトキャッシュ*）を保持しておき、同じ文章で始まる次のリクエストで、それを改めて読み込まずに済むようにします。長い物語では、数秒で済むか数分待つことになるかの差になります。キャッシュが役立つのは、新しいプロンプトが古いものと同じ始まり方をしている範囲に限られます（[キャッシュが役立たない理由](#why-a-cache-may-not-help)を参照）。Marginalia は、リクエストが**どの種類の作業か**もサーバーに伝えられるため、サーバー、またはその前段のプロキシーは種類ごとにキャッシュを保持できます。

<a id="what-is-sent"></a>

## 送信される内容

Marginalia が作業の種類ごとに行うすべてのリクエストには、次の HTTP ヘッダーが付きます。

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

値は、同じユーザーが同じ種類の作業を行う場合、どの本についてのものであっても同じになります。ユーザーや種類が異なれば値も異なります。

| 種類 | リクエスト |
|---|---|
| 生成 | 新しいパート、*再生成* と *スワイプ*。プロンプトは物語を共有するため、値も共有します。 |
| impersonate | *Impersonate*。 |
| 注釈 | *AI で注釈を付ける* と自動注釈。二段階モードの二つ目のモデルは、独自の値を持ちます。 |
| 書き直し | *書き直し* と自動注釈の書き直し。 |
| 要約 | 要約。 |
| エージェント | [エージェント](agents.md)のリクエスト。実行ごとに、育っていく独自の会話を持つため、それぞれ独自の値を持ちます。 |
| ヘルプ | ヘルプアシスタント。 |
| プラグイン | 各プラグインが独自の値を持ちます。*Reviewer*、*Side Query*（タブのタイトルは別のものです）、*Lore Generator*、*Arc Outline*、*Converter*、*Manga Generator*。 |

どの種類にも属さないリクエスト（モデルの一覧取得、接続テスト、トークン数のカウント）にはヘッダーは付きません。

値は、ユーザーと種類から作られるキー付きハッシュです（キーは[データフォルダー](administration/index.md#the-data-folder)内の `secret.key` から取得されます）。ユーザーも本も特定できず、誰のものかを突き止めることもできません。別のインストール環境では、同じユーザーであっても別の値になります。値は本ごとではなくユーザーと種類ごとに一つだけなので、サーバーが保持すべきキャッシュの数もそれ以上にはなりません。

OpenAI 互換のプロバイダーのほか、[Anthropic Claude](extensions/anthropic.md)と[Google Gemini](extensions/google-genai.md)のプロバイダーもすべてこのヘッダーを送ります。ヘッダーを使わないサーバーはこれを無視します。送信をオフにする設定はありません。

<a id="why-a-cache-may-not-help"></a>

## キャッシュが役立たない理由

サーバーがキャッシュを再利用できるのは、新しいプロンプトが古いものと**最初のトークンから**同じである部分に限られます。最初の違いで再利用は終わり、その後の部分は、変わっていなくても改めて読み込まれます。リクエストの開始時に出力される `llama-server` の行を見てください。

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` は、新しいリクエストがキャッシュ済みプロンプトのうち保持している割合です。1 に近ければキャッシュが使われています。ここでは 15 % しか保持されていないため、キャッシュがディスクから復元されたものであっても、スロットに残ったままのものであっても、プロンプトは早い段階で異なっています。

**スライディングウィンドウ**のモデル（例えば Gemma）やハイブリッドモデルは、キャッシュの末尾を捨てて途中から続けることができません。`llama.cpp` のソースによると、`llama-server` が保存済みプロンプトから続けられるのは、新しいプロンプトが保存済みのものを正確に延長している場合か、二つが異なる位置以前に**コンテキストチェックポイント**が存在する場合に限られます。チェックポイントは、ユーザーメッセージの始まりの位置（8192 トークン以上の間隔）と、プロンプト末尾の少し前にだけ作られ、スロットファイルに保存されます（いつ作られたかは INFO 行の `appended N context checkpoint(s)` と `restored N context checkpoint(s)` が示します）。使えるチェックポイントがなければ、`f_keep` の指定にかかわらずプロンプト全体を改めて読み込みます。そのことが報告されるのはトレースレベルだけです（`-lv 4`: `forcing full prompt re-processing due to lack of cache data`）。そのため、生成、注釈、書き直しの物語は、一つのパートごとにユーザーメッセージに続くモデルの応答として、メッセージの連鎖として送られます。最後のユーザーメッセージがチェックポイントになり、次のリクエストはそこから異なり始めます。新しいパートはメッセージを末尾に追加するだけです。ログからコールドのプリフィルを読み取るには、ここでは最初の `prompt processing, n_tokens = 4096` の行が約 7 秒後に出ています。接頭辞が再利用されていれば、1、2 秒後に出ているはずです。

[プロンプト](books/prompts.md#how-the-prompt-is-put-together)は**システムメッセージ**で始まり、その後に物語が続くため、システムメッセージの内容はリクエストごとに同じでなければなりません。デフォルトのマスターテンプレートでは、システムメッセージには、この順で、設定集ブロック、要約、作者メモ（注釈）が入ります。これらのいずれかが二つのリクエスト間で変わると（新しい文章が別の設定集の項目を有効にする、要約が追加される、メモが追加または解決されるなど、よくあることです）、キャッシュは変更箇所までしか有効にならず、その後ろの物語全体が改めて読み込まれます。物語がプロンプトに収まりきらなくなると、最も古いパートから外れていくため、ターンごとに物語の始まりが一つのパート分ずれます。効果があるのは次のことです。

- パートごとに変わる項目には挿入モードの**ユーザープロンプトの前**を設定します（[順序と挿入](lorebooks/entries.md#order-and-insertion)）。これらは物語の後ろ、プロンプトの末尾に置かれ、キャッシュを乱しません。常に真である内容だけを設定集ブロックに残します。
- 使わないブロックはマスターテンプレートから外します（例えば、モデルにメモを渡さないなら作者メモ）。そうすればシステムメッセージは変わりません。
- パートのメニューで**プロンプトを比較**を開くと、キャッシュがどこで終わっているかが分かります。このパートのプロンプトを、以前のパートのものと比較し（既定では直前のパート。ボックスには、それより前のパートとそのスワイプが並びます）、最初に異なるメッセージに印を付けます。[プロンプトを比較](#compare-prompt)を参照してください。

<a id="compare-prompt"></a>

### プロンプトを比較

パートのメニュー（デスクトップとスマートフォン）にある**プロンプトを比較**は、「なぜこのパートではキャッシュが使われなかったのか」に答えます。二つのプロンプトのメッセージ（システムメッセージ、続いてパートごとのユーザーメッセージとモデルの応答、最後にそのターンのユーザーメッセージ）を、それぞれ同じ、変更、追加、削除のいずれかの状態とともに一覧にします。最初に異なるメッセージは強調表示され、その後ろのメッセージには、サーバーがその後すべてを改めて読み込むため、*再読み込み* の印が付きます。表の上には次の情報が表示されます。

- 最初の違いがある位置（メッセージと文字）と、先頭から同じである部分の量（文字数と、おおよそのトークン数）。
- 違いの解釈。**システムメッセージ**が異なる（ジェイルブレイク、テンプレート、設定集、要約、メモが二つのリクエストの間で変わった。よくある原因です）、**物語の途中**のパートが異なる（編集、再生成、または要約に置き換えられた）、**物語の始まりがずれた**（最も古いパートが外された、または要約された）、あるいは違いが**前のプロンプトの最後のメッセージ**だけである（そのターンのユーザーメッセージで、物語には繰り返されない）。最後のケースは、次のパートやスワイプが再利用できる最大の量です。それでもサーバーがキャッシュを使わなかった場合、原因はプロンプトではありません（スロット、モデル、セッション ID を確認してください）。

メッセージを選択すると、最初の違いの少し前から、二つのテキストが並んで表示されます。比較の対象はメッセージだけです。モデル、リクエストのツール、サーバーのスロットは、保存されたプロンプトには含まれません。パートには保存済みのプロンプトが必要です（作者が手で入力したパートや、プロンプトを保存する前に生成されたパートには、プロンプトがありません）。

以下の種類のリクエストは、切り替えたときに何が保持されるかに関わるため重要です。その中身はプロンプトによって、どれだけ使われるかが決まります。
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 使い方：llama.cpp で一つのスロットをディスクと併用する

この方法が役立つのは、サーバーのキャッシュ用メモリが足りない場合です。十分なメモリがあり複数のスロットを持てるローカルの`llama-server`なら、作業の種類ごとにキャッシュを保持でき、ほかに設定は要りません。メモリが**スロット一つ分**しかないと、別の種類の作業が来るたびに物語のキャッシュが捨てられます。部分の注釈、その後の書き直し、次の生成は、毎回プロンプトを読み直すことになります。

解決策は、作業の種類ごとのキャッシュをディスクに保存しておき、その種類の要求が届いたときに一つのスロットへ入れ替えることです。`llama-server`はスロットをファイルに保存して復元できます。その前段に置いたプロキシが、ヘッダーで要求の種類を判別します。以下の例は [LiteLLM](https://docs.litellm.ai/) 用のフックです。

**必要条件:** 2026-10-08 以降の`llama-server`であること（llama.cpp PR #26004、*preserve context checkpoints across slot save/restore*）。それより古いものはスロットのメモリを保存・復元しますが、[コンテキストチェックポイント](#why-a-cache-may-not-help)は保存しません。そのため、スライディングウィンドウを持つモデル（例えば Gemma）やハイブリッドモデルでは、復元のたびに変化のない部分も含めてプロンプト全体を読み直すことになります。ビルドは`llama-server --version`で確認してください。移動するタグを持つコンテナイメージ（`full`、`server`...）は、pull するまで更新されません。

一つのスロットとファイル用のフォルダで`llama-server`を起動します:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

LiteLLM の設定ファイルの隣に`slot_cache.py`という名前で保存します:

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

LiteLLM の`config.yaml`でフックを有効にします。コールバックを設定し、`llama-server`へ向かうルートの`model_info`に`slot_cache`を指定してください。ワイルドカードのエントリーを一つ置けば十分で、LiteLLM にモデルを一覧する必要はありません（サーバーがモデルを知っており、クライアントが使いたいモデルを指定します）:

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

フック自身はアドレスを持ちません。要求が向かうルートの`api_base`と`api_key`を使います（スロットはサーバーのルートにあるため、末尾の`/v1`は取り除かれます）。`slot_cache`を持つルートだけが対象です。ヘッダーを付けていても、他のモデル（例えばクラウドのモデル）への要求は素通りします。複数のサーバーを並べて動かすことができ、それぞれが独自のスロットを持ちます。ワイルドカードを使うと、ルートはクライアントが要求したモデルになるため、ファイルはモデルごとに（`qwen3-32b`）保存されます。一部のモデルに別の設定が必要な場合は、ワイルドカードの横にその名前を登録することもできます。LiteLLM はモデル名の最初の`/`より前をプロバイダーとして読むため、その文字を含む名前（ルーターのプリセットのような`org/model`）では、ルートに`model_name: "openai/*"`を使い、Marginalia にはモデルを`openai/org/model`として指定します。フックは`openai/`を取り除き、ファイル名とスロットの呼び出しではサーバーが知っている名前を使います。ルートとその`model_info`は`model_list`から取得されます。`litellm --model ...`だけで起動したプロキシには、ルートも設定もないため、フックは何もしません。ワイルドカードのエントリーがその代わりになります。

**`llama-server`（ルーターモード）**では、一つのアドレスの背後に多数のモデルがあり、必要に応じて読み込まれる構成でも動作します。各モデルは独自のスロットを持つ個別のインスタンスなので、フックはサーバー・モデル・スロットごとに状態を保持し、スロットの呼び出しの本文にモデル名を記します。これはクライアントが使った名前から LiteLLM の`openai/`を除いたものです。`model`の設定で別の名前を指定することもできます（ルート名がモデル名と異なる場合に使います）。これには2026年2月以降の llama.cpp が必要です。このバージョンは`/slots`をインスタンスへ転送します（それ以前は`400 Invalid action`と応答していました）。

ルーターが一部のモデルだけを保持できる場合（既定では`--models-max`）、別のモデルを要求すると、最も長く使われていなかったモデルがアンロードされ、そのスロットとキャッシュも失われます。フックも同じ順序に従い、ルーターがアンロードする要求の前にそのスロットを**保存します**。`slot_cache`で`models_max`を、または`LLAMA_MODELS_MAX`を、`--models-max`と同じ数値に設定してください（0 は無制限）。そのためには、プロキシがルーターの唯一のクライアントである必要があります。別の経路で届いた要求は認識されません。保存によって、ルーターがアンロードしたモデルが読み込み直されることはありません。

保存の動作は次のとおりです。セッションのキャッシュは、そのスロットが別の用途に必要になったとき（別の種類の要求、別のモデル、ルーターによるアンロード）にディスクへ書き込まれます。要求ごとではありません。生成のたびに 3GB を書き込むとサーバーが遅くなるだけだからです。そのため、例えば生成、注釈、生成の順に進むと、最初の切り替えの後にファイルが現れます。注釈が先に生成を保存するためです。`save_idle_seconds`を`slot_cache`で（または`LLAMA_SAVE_IDLE_SECONDS`で）設定すると、しばらく要求がないときにもキャッシュが書き込まれるので、作業を止めた後すぐにフォルダにキャッシュが現れます。要求があると待機は最初からやり直され、ディスク上にすでにあるキャッシュは次の切り替えで再度書き込まれません。フックは、スロットがアイドル状態のときにだけ保存・復元・消去の呼び出しを送ります（`GET /slots`、`is_processing`に問い合わせます。そのエンドポイントがない場合は即座に送ります）。`llama-server`は、要求の実行中にそのような呼び出しを保留し、要求が終わるたびに保留された呼び出しを一つだけ解放します。そのため、後ろに届いた別クライアントの要求が、永遠に待たされる可能性があります。保存の書き込み中、サーバーは他の処理を行いません。

詳細は次のとおりです。要求がスロットにあるセッションとは別のセッションから届くと、フックはスロットの内容をそのセッション名で保存し、新しいセッションのファイルを復元します（ファイルがなければスロットを空にします）。その後、要求を続行させます。ファイルは**ルートごと**（`model_name`ごと）に保持されます。保存された状態は、それを書き込んだモデルにしか合いません。そのため、ファイル名にはルートとセッションが含まれ、同じサーバーとフォルダを使っていても、二つのルートがファイルを共有することはありません。サーバーは、復元されたプロンプトと異なる部分だけを読みます。復元できないキャッシュ、消えてしまったファイル、応答しないサーバーは、プロンプトを読み直す原因になるだけです。フックのせいで要求が失敗することはありません。

注意点:

- **サイズ。** ファイルはスロットのメモリと**そのコンテキストチェックポイント**の両方を含むため、その合計の大きさになります。長い物語と大きなモデルでは、数百 MB から数 GB になります。計測例として、18,000 トークンのプロンプトを持つ 31B モデルでは、1.3 GB から 11 GB のファイルができました。最も大きいものには約 450 MB のチェックポイントが約 18 個あり、それに加えてキャッシュ本体が約 1.5 GB ありました。サーバーは既定で最大 32 個のチェックポイントを保持し、切り替えのたびにそのすべてを書き込み・読み込みします。`--ctx-checkpoints N`で`llama-server`を指定すると上限をかけられます。これによりファイルは小さくなり、切り替えも短くなります。ただし、チェックポイントが役立つのは、次のプロンプトが保存済みのものと最初に異なる位置が、その直後である場合だけです。新しい部分やスワイプは、最後のユーザーメッセージの先頭（最新のチェックポイント）で異なります。一方、注釈や書き直しは、何部分も前の部分を変えることがあります。そのため、物語が遡る分だけチェックポイントを保持してください。フックのログに`prompt:`の行があり、各要求がどこで異なるかが分かります。ルート・ユーザー・種類ごとにファイルは一つで、その場で上書きされるため、フォルダはそれだけの大きさに収まります。
- **速度。** フックは保存と復元ごとにサイズと時間を記録するので、切り替えがプロンプトの読み直しより速いか確認してください。上の例は、NVMe ディスクと AMD Radeon AI PRO R9700 での計測です。11 GB の保存に 6 秒、20,000 トークン分の 7.6 GB の復元に 8 秒で、切り替えは合計約 14 秒でした。一方、その長さのプロンプトを読み直すと、毎秒 440 トークンで約 45 秒かかります。プリフィルはプロンプトが長くなるほど増えます。チェックポイントなしでは、同じファイルは約 1 GB で、切り替えは 5 秒でした。
- **メモリ。** スロットのチェックポイントはサーバーの**システムメモリ**に保持されます（GPU のメモリではありません）。最大`--ctx-checkpoints`個（既定は 32 個。大きなモデルでは各数百 MB）で、保存や復元のたびにそのすべてを読み書きします。`llama-server`も、手放したスロットのプロンプトを独自のメモリキャッシュ（既定 8 GiB、`--cache-ram`）に保持します。フックのファイルも同じ役割を果たすので、メモリが不足しているマシンでは 0 に設定できます。マシンがスワップを始めると、保存と復元は数倍遅くなります。チェックポイントを先にスワップから読み戻す必要があるためです。ディスクを調べる前に、`free -h`を確認してください。
- **上限。** 上限を設けないと、フォルダはルート・ユーザー・種類の数に応じて大きくなります。ルートの`slot_cache`で`max_files`（ファイル数）や`max_gib`（合計サイズ）を設定すると、フックは保存のたびに、**そのルートの**キャッシュのうち最も長く**使われていない**ものを削除します。（すべてのルートに対しては、プロキシの環境変数`LLAMA_CACHE_MAX_FILES`と`LLAMA_CACHE_MAX_GIB`で設定します。）キャッシュの復元も使用とみなされます。フックは、保存したばかりのキャッシュや復元しようとしているキャッシュは決して削除しません。そのため、一つのルートで二つのセッションを交互に使う場合は、上限に関係なく少なくとも二つのファイルが残ります。削除するのはそのルートのファイルだけなので、他のルートやフォルダ内の他のファイルは安全です。`max_gib`より大きい単一のキャッシュは残ります（ログに警告が出ます）。削除にはフォルダが必要です。プロキシから見た`--slot-save-path`フォルダのパスを`save_path`（または`LLAMA_SLOT_SAVE_PATH`）に設定してください。コンテナで動かす場合は、`llama-server`で両方のコンテナがマウントするボリュームを指定します。設定しないと何も削除されません。削除されたキャッシュは、そのセッションが一度だけプロンプトを読み直す原因になるだけです。上限は任意です。サイズに上限が要らない場合は、ホスト上の cron ジョブでも同じことができます（例えば`find /srv/ai/slots -name '*.bin' -mtime +2 -delete`）。使用中のキャッシュは、そのセッションがスワップアウトされるたびに保存し直されるため、その経過時間は改めてゼロから数えられます。外部から削除されたファイルはフックにとって無害です。見つからないファイルは、単なるミスになります。
- **ルートのモデルを変更した後は**、フォルダ内のそのルートのファイル（名前がルートで始まるもの）を削除してください。別の重みで保存された状態は、新しいモデルには合いません。
- **切り替えのたびに保存と復元が発生します。** [自動注釈](books/annotations.md)が有効だと、部分の後に注釈と書き直しが続くため、一回の応答で三、四回切り替わることがあります。頼りにする前に、一回の応答を計測してください。
- **プロキシを再起動すると**、どのセッションがスロットにあるかを忘れます。再起動後の最初の要求は、そのセッションのファイルをスロットに復元し、スロットにあったものは失われます。
- **フックが知らないうちにルーターがモデルをアンロードした場合**（`models_max`の誤り、別のクライアントなど）、スロットは空なのに、フックはまだ最後のセッションが入っていると考えます。同じセッションの要求は、単にプロンプトを読み直します。別のセッションの要求は、まず空のスロットをそのセッションのファイルに上書き保存するため、そのキャッシュも失われます。どちらもプリフィルのコストがかかるだけです。
- **`llama-server`は単純な保存や復元を記録しません**（追加または復元したコンテキストチェックポイントとエラーは記録します）。実行内容は応答に出ます。`LITELLM_LOG=INFO`を付けたプロキシのログには、フックが行うすべての呼び出しの行があります（例えば`slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`、`… save <file> …: ok (…)`、`… restore … miss (400)`）。セッションの最初の要求でミスが起きるのは正常です（llama-server は "Unable to restore slot, no available space in KV cache or invalid slot save file" と記録します）。本当の問題は警告として出ます。アドレスと一致しない証明書、拒否された API キー、応答しないサーバーなどです。
- **プロンプトが異なる箇所。** あるセッションの二回目以降の各要求について、ログには、前回の要求とプロンプトがどこまで同じかを示す行があります（例：`slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`）。サーバーは同じ部分までしかキャッシュを再利用できません（おおむね 1 トークンが 3〜4 文字です）。そのため、二回目の要求がほぼ全部を読み直すときは、この行を読んでください。行が示すメッセージが、先頭が変わったメッセージです。この行は、二つのプロンプトの最後のメッセージも比べます。そこには変化する部分（設定、部分、タスク）が入っています。物語に部分が一つ増えると、古い最後のメッセージは同じ位置にはなくなります。二つの最後のメッセージが共有する文字数は、例えば設定がどれだけ同じかを示します。
- **何も起きないとき**は、プロキシのログ（`docker logs litellm-proxy 2>&1 | grep slot_cache`）を確認してください。フックが動いていれば、すべての呼び出しに行があります。`slot_cache`のないルートへの Marginalia の要求は、一度だけ報告されます（`not used for <route>: no model_info.slot_cache`）。`slot_cache`の行がまったくない場合、フックは実行されていません。コンテナ内に古いファイルか設定が残っています。コンテナに単一のファイルをマウントしている場合（`./slot_cache.py:/app/slot_cache.py`）、エディタがファイルを置き換えると、コンテナ作成時のバージョンのまま表示され続けます。`docker compose up -d
  --force-recreate`で新しいファイルが読み込まれます（単なる再起動では読み込まれません）。`docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py`は、古すぎるファイルに対して 0 を出力します。
- **Docker の場合**、プロキシと`llama-server`は二つのコンテナなので、`--slot-save-path`のフォルダはホスト上のディレクトリになり、両方のコンテナがマウントします（プロキシはそこに書き込む必要があります。古いファイルを削除し、使用されたファイルの経過時間を更新するためです）。フックのファイルは LiteLLM の設定の隣に置きます。`api_base`は、プロキシが到達できるアドレスです。サーバーが TLS を使う場合は、証明書のホスト名を指定します:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  ルートの`slot_cache`に`save_path: /slots`と`models_max: 1`を設定します。
- フックは`CustomLogger.async_pre_call_deployment_hook`を使います。これは LiteLLM がデプロイメントを選んだ後、要求を送る前に呼び出されます（バージョン 1.106.5 は、実際のルーターと代替サーバーで確認されています）。実行中のバージョンで確認してください。単一のサーバーを再起動してモデルを切り替えることで、一つのアドレスの背後でモデルを入れ替えるプロキシ（llama-swap）はサポートされていません。スロットの呼び出しが、読み込まれている任意のモデルに届いてしまうためです。これらはいずれも、実際の`llama-server`ルーターでは実行されていません。テストは、フックが行う呼び出しを確認する代替サーバーを使っています。
<a id="freeing-the-gpu-for-another-model"></a>

## 別のモデルのために GPU を空ける

言語モデルを載せている GPU には、その隣に画像モデルや音声モデルを置く余裕がありません。`llama-server` をルーターモードで使う場合、2 つ目のフックがリクエストに応じてモデルをアンロードできます。[GPU ロック](administration/gpus.md)（画像または音声サーバーを起動する前のそのサーバーのロック）のステップが、他のすべてに使うものと同じキーでプロキシにリクエストを送り、これを依頼します:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

`X-Llama-Unload` ヘッダーを付けたリクエストは、モデルには届かず、フック自身が応答します（HTTP 200、テキスト `unloaded: <names>`）。フックはルーターのモデル一覧（`GET /models`）を取得し、読み込まれているモデルを名前を指定して（`POST /models/unload`）アンロードしたうえで、ルーターがメモリ上に何も残っていないと報告するまで待ちます（メモリを保持しているのは `loaded`、`loading`、`unloading`）。待ち時間は最大 `LLAMA_UNLOAD_WAIT_SECONDS`（120）秒です。サーバーに接続できない、エラーを返す、または待機後もモデルが残っている場合は、応答はエラー（HTTP 502）になります。そのため、最初の取得ステップがこのリクエストであるロックは、満杯の GPU 上で別のモデルを起動することはありません。モデルは自動的に戻ります。次の通常のリクエストでルーターがモデルを読み込み直すため、言語モデルについては、プラグインの *after* リクエストが空になることがあります。本文中のモデル名は、いずれかのルートが受け付ける名前であれば足ります（`openai/*` のルートでは `openai/` で始まる名前、例えば `openai/unload`）。重要なのはヘッダーのほうです。どのルートも受け付けない名前の場合、フックは LiteLLM がその名前を確認する前に動くため、モデルをアンロードした *後* に 400 で応答します。アンロードは 1 つのモデルではなく、`unload` を持つルートのすべてのサーバーに対して行われます。

フックは独立したスクリプト `gpu_unload.py` で、`slot_cache.py` の隣に置き、それと連結します。両者は独立しています。`gpu_unload.py` はキャッシュフックなしでも動作します。両方を連結すると、`gpu_unload.py` は LiteLLM のコールバックの中からキャッシュフックを見つけ、その `release_server` を呼び出します。この呼び出しは、**まず各スロットのキャッシュを保存し、中身を忘れます**。そのため次のリクエストは、再読み込みされたモデルに、そのセッションのキャッシュを復元できます。（この呼び出しがないと、キャッシュフックはセッションがまだスロットに入っていると思い込み、何も復元しません。その結果、入れ替えのたびにプロンプト全体が読み直されてしまいます。）

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

Docker では、ファイルは `slot_cache.py` と同じようにコンテナ内に置かれている必要があります。`- ./gpu_unload.py:/app/gpu_unload.py:ro` を、プロキシの `volumes` に、`slot_cache.py` の行の隣に追加してください。ファイルが存在しない場合、起動時には `ImportError: Could not import proxy_handler_instance from gpu_unload` と表示されます（本当の原因は `FileNotFoundError` か、スクリプト自体のエラーで、ログではその数行上に出ています）。

この 2 つを `litellm_config.yml` で連結し、ルートには `unload` を付けてください:

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

上記のアドレスに、ヘッダー `Authorization: Bearer <key>` と `X-Llama-Unload: all`、および例の本文を付けて `POST` を送る [GPU ステップ](administration/gpus.md)にしてください（ヘッダーは暗号化して保存されます）。それを画像サーバーと音声サーバーのロックの取得ステップの先頭に置いてください。

別のモデルのサーバーも、同じプロキシ経由で制御できます。LiteLLM はパスを別のアドレスへ中継します（`general_settings: pass_through_endpoints`、`path`、`target`、`include_subpath: true`。プロキシのキーは確認され、クライアントの `Authorization` ヘッダーは**転送されません**。そのため、転送先は `headers` に独自のシークレットを持ちます）。音声サーバーや画像サーバーの隣で、そのコンテナを停止・起動する小さなサービスが、このような転送先になります: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)（`POST /<container>/start` はサービスの準備ができたときに、`stop` は終了したときに応答します。その README には compose の例と、音声サーバー用の LiteLLM ルート（`/v1/audio/speech`）が書かれています）。

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

確認した内容: LiteLLM 1.108.3 を、`loaded`、`unloading`、`unloaded` を報告する代わりのルーター（スタンドイン）と組み合わせて確認しました。待機中は状態が `unloaded` になるまで応答を保留し、停止中のサーバーは 502 を返しました。両方のフックを連結した場合は、アンロードの前にスロットが保存され、次のリクエストで復元されました。`gpu_unload.py` だけの場合は、ほかに何も呼ばれませんでした。パススルーは転送先に届き、クライアントから送られた値の代わりに独自の `headers` の値を追加し、キーのないリクエストを拒否しました。また、LiteLLM の背後にある実際の `llama-server` ルーター（31B モデル 1 つ）でも実行しました。リクエストは 1〜2 秒後に `unloaded: <model>` を返し、ルーターは `stopping model instance` と `exited with status 0` をログに記録しました（プロセスが終了したため、メモリは解放されています）。何も読み込まれていない状態での 2 つ目のリクエストは即座に `unloaded: nothing` を返し、次の通常のリクエストは、ルーターがモデルを読み込み直すため約 1 分かかりました。お使いの `GET /models` が状態を別の名前で表す場合は、スクリプト内の `IN_MEMORY` を変更してください。
