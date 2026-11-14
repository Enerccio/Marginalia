---
label: プロンプトキャッシュとセッション ID
order: 975
icon: database
---

# プロンプトキャッシュとセッション ID

モデルサーバーは、直前に読み込んだプロンプト（*プロンプトキャッシュ*）を保持しておき、同じテキストで始まる次のリクエストでそれを再び読み込まずに済むようにします。長い物語では、数秒の待ち時間で済むか数分待つことになるかの差になります。キャッシュが効くのは、新しいプロンプトが古いものと同じ先頭から始まっている範囲だけです（[キャッシュが効かない場合](#why-a-cache-may-not-help)を参照）。Marginalia は、リクエストが**どの種類の作業か**をサーバーに伝えることもできます。これにより、サーバー（またはその前段に置かれたプロキシ）が作業の種類ごとにキャッシュを保持できます。

<a id="what-is-sent"></a>

## 送信される内容

Marginalia が作業の種類ごとに行うすべてのリクエストには、次の HTTP ヘッダーが付きます。

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

値は、同じユーザーが同じ種類の作業を行う場合、対象の本が何であっても同じになります。ユーザーや種類が異なれば値も異なります。

| 種類 | リクエスト |
|---|---|
| generation | 新しいパート、*再生成* と *スワイプ*。これらのプロンプトは物語を共有するため、値も共有します。 |
| impersonate | *なりきり*。 |
| annotate | *AI で注釈を付ける* と自動注釈。2 段階モードの 2 つ目のモデルには、独自の値があります。 |
| redraft | *書き直し* と自動注釈の書き直し。 |
| summary | 要約。 |
| agent | [エージェント](agents.md)のリクエスト。実行ごとに会話が増えていくため、実行ごとに独自の値があります。 |
| help | ヘルプアシスタント。 |
| plugins | 各プラグインがそれぞれ独自の値を持ちます。*Reviewer*、*Side Query*（タブのタイトルは別のものになります）、*Lore Generator*、*Arc Outline*、*Converter*、*Manga Generator*。 |

どの種類にも属さないリクエスト（モデル一覧の取得、接続テスト、トークン数のカウント）にはヘッダーは付きません。

値は、ユーザーと種類から作られたキー付きハッシュです（キーは[データフォルダ](administration/index.md#the-data-folder)内の `secret.key` から取得されます）。ユーザーも本も特定できず、誰の値なのかを割り出すこともできません。別のインストール環境では、同じユーザーでも異なる値になります。値は本ごとではなくユーザーと種類ごとに 1 つだけなので、サーバーが保持すべきキャッシュの数もそれ以上に増えることはありません。

すべてのプロバイダーがこのヘッダーを送信します。OpenAI 互換のプロバイダーに加え、[Anthropic Claude](extensions/anthropic.md) と [Google Gemini](extensions/google-genai.md) のプロバイダーも対象です。このヘッダーを使わないサーバーは単に無視します。これをオフにする設定はありません。
<a id="why-a-cache-may-not-help"></a>

## キャッシュが効かない理由

サーバーは、新しいプロンプトと古いプロンプトが**最初のトークンから**同じである部分のキャッシュを再利用します。最初の差異でその再利用は終わり、それ以降は、どれだけ変わっていなくても読み直されます。リクエストの開始時に出力される`llama-server`の行を見てください。

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep`は、新しいリクエストがキャッシュ済みプロンプトのうち保持している割合です。1に近ければキャッシュが使われています。ここでは15 %しか使われていないため、プロンプトは序盤ですでにキャッシュと異なっています。キャッシュがディスクから復元されたものか、スロットから一度も出ていないものかは関係ありません。

**スライディングウィンドウ**を持つモデル(例としてGemma)やハイブリッドモデルは、キャッシュの末尾を捨てて途中から続きを処理することができません。`llama.cpp`のソースによれば、`llama-server`は、新しいプロンプトがキャッシュ済みプロンプトを厳密に延長している場合、または二つのプロンプトが異なる位置と同じか、それより前に**コンテキストのチェックポイント**が存在する場合にしか、キャッシュ済みプロンプトの続きから処理できません。チェックポイントは、ユーザーメッセージの先頭(互いに少なくとも8192トークン離れて)と、プロンプト末尾の少し前にだけ作られ、スロットファイルに保存されます。作られたときはINFO行の`appended N context checkpoint(s)`と`restored N context checkpoint(s)`に表示されます。使えるチェックポイントがない場合は、`f_keep`が何と言っていても、プロンプト全体を読み直します。その事実は、トレースレベルでのみ`-lv 4`:`forcing full prompt re-processing due to lack of cache data`として報告されます。だからこそ、一つの生成、注釈、書き直しのストーリーは、メッセージの連鎖として送られます。各部分は、ユーザーのメッセージに続くモデルの応答として並びます。ユーザーの最後のメッセージがチェックポイントになり、次のリクエストはそこから違い始め、新しい部分はメッセージを末尾に追加するだけです。ログからコールドのプリフィルを読み取るには、最初の`prompt processing, n_tokens = 4096`行がここでは約7秒後に現れるかを見ます。接頭辞が再利用されていれば、1、2秒後に現れるはずです。

[プロンプト](books/prompts.md#how-the-prompt-is-put-together)は**システムメッセージ**で始まり、その後にストーリーが続くため、システムメッセージの内容はリクエストごとに変わらず同じでなければなりません。デフォルトのマスターテンプレートでは、システムメッセージには、この順序で、ロア・ブロック、要約、作者のメモ(注釈)が入ります。これらのいずれかが二つのリクエストの間で変わると(これは普通のことです。新しい文章が別のロアエントリを有効にしたり、要約が追加されたり、メモが追加または解決されたりします)、キャッシュはその位置までしか有効でなく、その後ろにあるストーリー全体が読み直されます。ストーリーが収まらなくなると、最も古い部分からプロンプトを離れるため、毎ターン、ストーリーの開始位置が一つの部分ぶん進みます。効果があるのは次のことです。

- 部分ごとに変わるエントリには、挿入モードとして**ユーザープロンプトの前**を指定してください([順序と挿入](lorebooks/entries.md#order-and-insertion))。それらはプロンプトの末尾、ストーリーの後ろに置かれ、キャッシュを乱しません。常に真である内容だけをロア・ブロックに残してください。
- 使わないブロックは、マスターテンプレートから外してください(たとえば、モデルに作者のメモを与えないなら作者のメモ)。そうすればシステムメッセージは変わりません。
- 部分のメニューで**プロンプトを比較**を開くと、キャッシュがどこで終わっているかがわかります。この機能は、その部分のプロンプトを、より前の部分(既定では最も近いもの。ボックスには、それより前の部分とそのスワイプが並びます)のプロンプトと比べ、最初に異なるメッセージに印を付けます。詳しくは[プロンプトを比較](#compare-prompt)を参照してください。

<a id="compare-prompt"></a>

### プロンプトを比較

部分のメニュー(デスクトップとスマートフォン)にある**プロンプトを比較**は、*なぜこの部分ではキャッシュが使われなかったのか?*に答えます。二つのプロンプトのメッセージ(システムメッセージ、続いて部分ごとのユーザーメッセージとモデルの応答、最後にそのターンのユーザーメッセージ)を並べ、それぞれに、同じ、変更、追加、削除のいずれかの状態を付けて表示します。最初に異なるメッセージは強調表示され、その後ろのものには*再読み込み*と印が付きます。サーバーは最初の差異以降をすべて読み直すからです。表の上には次の情報が出ます。

- 最初の差異がどこにあるか(メッセージと文字位置)、また、冒頭から同じであるプロンプトの量(文字数と、おおよそのトークン数)。
- 差異の読み方。**システムメッセージ**が異なる(ジェイルブレイク、テンプレート、ロア、要約、メモが二つのリクエストの間で変わった。最も多い原因です)、**ストーリーの途中の部分**が異なる(編集、再生成、または要約による置き換えがあった)、**ストーリーの開始位置が移動した**(最も古い部分が除外されたか、要約された)、あるいは差異が**前のプロンプトの最後のメッセージだけ**である(そのターンのユーザーメッセージで、ストーリーはこれを繰り返しません)。最後のケースは、次の部分やスワイプが再利用できる最大のものです。それでもサーバーがキャッシュを使わなかった場合、原因はプロンプトではありません(スロット、モデル、セッションIDを確認してください)。

メッセージを選択すると、最初の差異の少し前から、二つのテキストを並べて表示します。比較されるのはメッセージだけです。モデル、リクエストのツール、サーバーのスロットは、保存されたプロンプトには含まれません。比較するには、部分に保存されたプロンプトが必要です(作者が手で入力した部分や、プロンプトを保存する前に生成された部分にはありません)。

以下のリクエストの種類は、切り替えたときに何が保持されるかに関わります。どれだけ使われるかは、プロンプトが決めます。
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 使い方: llama.cpp とディスク上の 1 スロット

この方法が役に立つのは、サーバーでキャッシュ用のメモリが足りなくなる場合です。複数のスロットに足りるメモリを持つローカルの `llama-server` なら、作業の種類ごとにキャッシュを保持でき、それ以上は何も必要ありません。スロット **1 つ分** のメモリしかないと、別の種類の作業のたびに物語のキャッシュが捨てられます。パートの注釈、その後の書き直し、次の生成は、毎回プロンプトを読み直すことになります。

解決策は、種類ごとのキャッシュをディスクに保存しておき、その種類のリクエストが来たときに 1 つのスロットへ入れ替えることです。`llama-server` はスロットをファイルに保存し、また復元できます。その前段に置かれたプロキシは、ヘッダーからリクエストの種類を判断できます。以下の例は [LiteLLM](https://docs.litellm.ai/) 用のフックです。

**要件:** 2026-10-08 以降の `llama-server` (llama.cpp PR #26004, *preserve context checkpoints across slot save/restore*)。それより古いものはスロットのメモリは書き出し・復元しますが、[コンテキストチェックポイント](#why-a-cache-may-not-help)は対象外です。そのため、スライディングウィンドウを使うモデル (例えば Gemma) やハイブリッドモデルでは、復元のたびに、変化していない部分があってもプロンプト全体を読み直します。`llama-server --version` でビルドを確認してください。移動するタグ (`full`、`server` など) のコンテナイメージは、pull するまで更新されません。

スロット 1 つとファイル用のフォルダーを指定して `llama-server` を起動します:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

次の内容を LiteLLM の設定ファイルと同じ場所に `slot_cache.py` という名前で保存します:

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

そして LiteLLM の `config.yaml` でこれを有効にします。コールバックと、`llama-server` へ向かうルートの `model_info` にある `slot_cache` です。ワイルドカードのエントリーが 1 つあれば十分で、LiteLLM にモデルを個別に列挙する必要はありません (モデルはサーバーが把握しており、クライアントが使いたいモデルを指定します):

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

フックには独自のアドレスは不要です。リクエストの送り先のルートの `api_base` と `api_key` を使います (スロットはサーバーのルートにあるため、末尾の `/v1` は取り除かれます)。`slot_cache` を持つルートだけが対象です。ヘッダーがあっても、それ以外のモデル (例えばクラウドのモデル) へのリクエストはそのまま通過します。複数のサーバーを並べて使うこともでき、それぞれが独自のスロットを持ちます。ワイルドカードを使うと、ルートはクライアントが要求したモデルになるため、ファイルはモデルごとに (`qwen3-32b`) 保存されます。別の設定が必要なモデルがあれば、そのモデルを名前で個別に列挙することもできます。LiteLLM はモデル名の `/` より前の部分をプロバイダーとして扱うため、名前にそれが含まれる場合 (`org/model` のようにルーターのプリセットを使う場合) は、ルートに `model_name: "openai/*"` を使い、Marginalia にはモデルを `openai/org/model` として指定してください。フックは `openai/` を取り除き、ファイル名とスロットへの呼び出しには、サーバーが把握している名前を使います。ルートとその `model_info` は `model_list` から得られます。それを使わずに起動したプロキシ (`litellm --model ...` だけ) には、どちらもありません。そのため、フックは何もしません。ワイルドカードのエントリー 1 つが、その代わりになります。

**`llama-server` をルーターモードで使う場合** (1 つのアドレスの背後に多くのモデルがあり、必要に応じて読み込まれる) も動作します。各モデルは独自のスロットを持つ独立したインスタンスなので、フックは状態をサーバー・モデル・スロットごとに保持し、スロットへの呼び出しの本文にモデル名を記載します (クライアントが使った名前から、LiteLLM の `openai/` を除いたもの。`model` の設定で別の名前を指定できます。ルート名がモデル名と一致しない場合に便利です)。2026 年 2 月以降の llama.cpp が必要です。それはスロットへの呼び出しをインスタンスへ転送します (以前は `400 Invalid action` と応答していました)。`/slots` を使うことで、この転送が可能になります。

ルーターが保持できるモデルが一部だけの場合 (`--models-max`、既定値は 4)、別のモデルを要求すると、最も長く使われていないモデルがアンロードされ、そのモデルのスロットとその中のキャッシュは失われます。フックも同じ順序に従い (`models_max` を `slot_cache` で、または `LLAMA_MODELS_MAX` で、`--models-max` と同じ数に設定します。0 は制限なし)、ルーターにアンロードさせるリクエストの **前に** そのスロットを保存します。そのため、プロキシはルーターへの唯一のクライアントである必要があります。別の経路で届いたリクエストは検知されません。保存によってルーターがアンロードしたモデルが読み込まれることはありません。

動作としては、セッションのキャッシュは **そのスロットが別の用途に必要になったとき** (別の種類の作業、別のモデル、またはルーターによるモデルのアンロード) にディスクへ書き込まれます。リクエストごとには書き込みません。生成のたびに 3 GB を書き込むと、サーバーが遅くなるだけだからです。そのため、最初の切り替え後にファイルが現れます。例えば生成、注釈、生成の順なら、注釈の前に生成の分が保存されます。`save_idle_seconds` を `slot_cache` (または `LLAMA_SAVE_IDLE_SECONDS`) に設定すると、その時間リクエストが来なければキャッシュも書き込まれるため、作業を止めた後すぐにフォルダーに保存されます。リクエストが来ると待機時間は最初から数え直されます。ディスク上にすでにあるキャッシュは、次の切り替え時に再度書き込まれることはありません。

フックは、スロットが空いているときにだけ保存・復元・削除を送ります (`GET /slots`、`is_processing` に問い合わせます。そのエンドポイントがない場合は、すぐに送ります)。`llama-server` は、リクエストの実行中にそのような呼び出しを保留し、リクエストが終わるたびに保留中の呼び出しを 1 つだけ解放します。そのため、保留の後ろに到着した別のクライアントのリクエストは、そこで無期限に待たされる可能性があります。サーバーは、保存の書き込み中は他の処理を行いません。

詳細: スロットにあるセッションとは別のセッションからリクエストが届くと、フックはスロットを、そこにあったセッションの名前で保存し、新しいセッションのファイルを復元し (ファイルがまだない場合はスロットを空にします)、リクエストを続行させます。ファイルは **ルートごと** (`model_name` 単位) に保存されます。保存された状態は、それを書き出したモデルにしか合わないため、ファイル名はルートとセッションにもとづいて付けられ、同じサーバーとフォルダーを使っていても、2 つのルートがファイルを共有することはありません。サーバーは、復元されたプロンプトとの差分だけを読みます。復元できないキャッシュ、消えたファイル、応答しないサーバーがあっても、プロンプトを読み直すだけです。リクエスト自体がフックのせいで失敗することはありません。

知っておくべきこと:

- **サイズ。** ファイルにはスロットのメモリと **そのコンテキストチェックポイント** が入るため、両方を合わせた大きさになります。長い物語と大きなモデルでは、数百メガバイトから数ギガバイトになります。実測の例: 31B のモデルで 18,000 トークンのプロンプトの場合、ファイルは 1.3 から 11 GB になりました。最大のものは、約 1.5 GB のキャッシュに加えて、約 450 MB のチェックポイントが約 18 個ありました。サーバーは既定で最大 32 個のチェックポイントを保持し、切り替えのたびにそのすべてを書き込み、読み込みます。`--ctx-checkpoints N` を `llama-server` に設定すると上限を設けられ、ファイルが小さくなり、切り替えも短くなります。ただしチェックポイントが効くのは、次のプロンプトが、キャッシュされたものと最初に異なる位置がそのチェックポイントの位置またはそのすぐ後にある場合だけです。新しいパートやスワイプは最後のユーザーメッセージの先頭で変わります (これは最新のチェックポイントです)。一方、注釈や書き直しは、数パート前までさかのぼって変えることがあります。そのため、物語が遡る分だけチェックポイントを保持してください (フックの `prompt:` の行を見ると、各リクエストがどこで異なるかがわかります)。ファイルはルート、ユーザー、種類ごとに 1 つずつで、それぞれ同じ場所で上書きされるため、フォルダーはその分だけの大きさに保たれます。
- **速度。** 切り替えがプロンプトの読み直しより速いか確認してください (フックは保存と復元のたびにサイズと時間を記録します)。上記の例は、NVMe ディスクと AMD Radeon AI PRO R9700 の環境のものです。11 GB の保存に 6 秒、20,000 トークン分の 7.6 GB の復元に 8 秒かかり、切り替えは約 14 秒でした。一方、その長さのプロンプトを読み直すと、毎秒 440 トークンで約 45 秒かかります。また、プリフィルはプロンプトが長くなるほど増えます。チェックポイントなしでは、同じファイルは約 1 GB で、切り替えは 5 秒でした。
- **メモリ。** スロットのチェックポイントは、サーバーの **システムメモリ** に保持されます (GPU のメモリではありません)。その数は最大 `--ctx-checkpoints` 個です (既定は 32 個。大きなモデルでは 1 個あたり数百 MB)。保存や復元では、そのすべてを読み書きします。`llama-server` は、手放したスロットのプロンプトを、独自のメモリキャッシュ (`--cache-ram`、既定は 8 GiB) にも保持します。フックのファイルも同じ役割を果たすので、メモリの少ないマシンでは 0 に設定できます。マシンがスワップを始めると、チェックポイントを先にスワップから読み戻す必要があるため、保存と復元が数倍遅くなります。ディスクを見る前に `free -h` を確認してください。
- **上限。** 上限がなければ、フォルダーはルート、ユーザー、種類の数に応じて大きくなります。ルートの `slot_cache` で `max_files` (ファイル数) や `max_gib` (合計サイズ) を設定する (すべてのルートに対してはプロキシの環境変数で `LLAMA_CACHE_MAX_FILES` と `LLAMA_CACHE_MAX_GIB` を設定する) と、フックは保存のたびに、**そのルートの** キャッシュのうち **最も長く使われていない** ものを削除します。キャッシュを復元することも、使ったことに数えられます。直前に保存したキャッシュや、これから復元するキャッシュは削除しません (そのため、交互に使う 2 つのセッションを持つルートは、上限にかかわらず少なくとも 2 つのファイルを保持します)。削除するのはそのルートのファイルだけなので、他のルートやフォルダー内の他のファイルは安全です。`max_gib` より大きい単一のキャッシュは残ります (ログに警告が出ます)。削除にはフォルダーが必要です。プロキシから見た `--slot-save-path` フォルダーを `save_path` (または `LLAMA_SLOT_SAVE_PATH`) に設定してください。コンテナ内で動かす場合は `llama-server` を使い、その場合は両方のコンテナがマウントするボリュームにします。設定がなければ何も削除されません。削除されたキャッシュは、そのセッションが一度だけプロンプトを読み直すことを意味するだけです。上限は任意です。サイズに上限が不要なら、ホストの cron ジョブでも同じことができます (例: `find /srv/ai/slots -name '*.bin' -mtime +2 -delete`)。使用中のキャッシュは、そのセッションがスワップアウトされるたびに保存し直されるため、経過時間は毎回最初から数え直されます。外部から削除されたファイルはフックにとって無害です。存在しないファイルは、単にミスとして扱われます。
- **ルートのモデルを変更した後**は、フォルダー内のそのルートのファイル (名前がルートで始まるもの) を削除してください。別の重みで保存された状態は、新しいモデルには合いません。
- **切り替えのたびに保存と復元の費用がかかります。** [自動注釈](books/annotations.md) を有効にすると、パートの後に注釈と書き直しが続くため、1 ターンで 3 ～ 4 回切り替わることがあります。頼る前に 1 ターンの時間を計測してください。
- **プロキシを再起動すると**、どのセッションがスロットにあったかを忘れます。その後の最初のリクエストは、そのファイルをスロットに上書き復元し、スロットにあったものは失われます。
- **フックが知らないうちにルーターがアンロードしたモデル** (誤った `models_max`、別のクライアント) は、スロットが空なのに、フックは最後のセッションがまだ入っていると考え続けます。同じセッションのリクエストは、単にプロンプトを読み直します。別のセッションのリクエストは、まず空のスロットをそのセッションのファイルに上書き保存するため、そのキャッシュも失われます。どちらの場合もプリフィルの分の費用がかかるだけです。
- **`llama-server` は単純な保存や復元をログに記録しません** (記録するのは、追加または復元したコンテキストチェックポイントと、エラーです)。その処理結果は応答に含まれ、プロキシのログ (`LITELLM_LOG=INFO` を付けた場合) にも記録されます。フックが行う呼び出しごとに 1 行あり、例えば `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`、`… save <file> …: ok (…)`、`… restore … miss (400)` があります。セッションの最初のリクエストでの失敗は正常です (llama-server は "Unable to restore slot, no available space in KV cache or invalid slot save file" とログに出します)。本当の問題は警告として出ます。アドレスと一致しない証明書、拒否された api key、応答しないサーバーなどです。
- **プロンプトがどこで異なるか。** セッションの 2 回目以降のリクエストごとに、ログには、そのプロンプトが同じセッションの前回のプロンプトとどこまで同じかを示す行があります (例: `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`)。サーバーは、そこまでしかキャッシュを再利用できません (おおよそトークン 1 つにつき 3 ～ 4 文字)。そのため、2 回目のリクエストでほぼすべてを再度読み込んでいるときは、この行を見てください。行に示されたメッセージが、先頭から変わったものです。この行は、2 つのプロンプトの最後のメッセージも比較します。最後のメッセージには、変化する部分 (設定資料、パート、タスク) が含まれています。物語にパートが追加された場合、古い最後のメッセージはもう同じ位置にありません。2 つの最後のメッセージが共有する文字数が、例えば設定資料がどこまで両者で同じかを示します。
- **何も起きない場合**は、プロキシのログ (`docker logs litellm-proxy 2>&1 | grep slot_cache`) を確認してください。フックが動いていれば、呼び出しごとに行があります。`slot_cache` のないルートへの Marginalia のリクエストは 1 回だけ報告されます (`not used for <route>: no model_info.slot_cache`)。`slot_cache` の行がまったくない場合、フックが実行されていません。コンテナ内に古いファイルか設定が残っています。コンテナに単一ファイルをマウントしている場合 (`./slot_cache.py:/app/slot_cache.py`)、エディターでファイルを置き換えても、コンテナ作成時のバージョンが表示され続けます。`docker compose up -d
  --force-recreate` で新しいものが読み込まれます (単純な再起動では読み込まれません)。`docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` は、古すぎるファイルに対して 0 を出力します。
- **Docker の場合**、プロキシと `llama-server` は 2 つのコンテナなので、`--slot-save-path` のフォルダーは、両方がマウントするホスト上のディレクトリにします (プロキシはそこへ書き込む必要があります。古いファイルを削除し、使用中のファイルの経過時間を更新するためです)。フックのファイルは LiteLLM の設定の隣に置きます。`api_base` は、プロキシが到達できるアドレスにします。サーバーが TLS を使う場合は、証明書のホスト名を使ってください:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  ルートの `slot_cache` に `save_path: /slots` と `models_max: 1` を指定します。
- フックは `CustomLogger.async_pre_call_deployment_hook` を使います。これは LiteLLM が配置先を選んだ後、リクエストを送る前に呼び出します (バージョン 1.106.5 は、実際のルーターとスタンドインのサーバーで確認しました)。お使いのバージョンで確認してください。1 つのアドレスの背後で、単一のサーバーを再起動してモデルを切り替えるプロキシ (llama-swap) はサポートされていません。スロットへの呼び出しが、そのとき読み込まれているモデルに届いてしまうためです。これらはいずれも、実際の `llama-server` ルーターに対しては実行していません。テストでは、フックが行う呼び出しを確認するスタンドインを使っています。
<a id="freeing-the-gpu-for-another-model"></a>

## 別のモデルのために GPU を空ける

言語モデルを載せた GPU には、その隣に画像モデルや音声モデルを置く余裕がありません。ルーターモードの `llama-server` では、2 つ目のフックがリクエストに応じてモデルをアンロードできます。そのため GPU を必要とするプラグイン（[Image Generator](extensions/image-generator.md#one-gpu-for-the-model-and-the-images)、Audiobook）は、ほかのすべてに使うのと同じキーで、プロキシへのリクエストによってそれを要求します:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

ヘッダー `X-Llama-Unload` を付けたリクエストは、フック自身が応答し（HTTP 200、本文は `unloaded: <names>`）、モデルには届きません。フックはルーターのモデル一覧を取得し（`GET /models`）、読み込まれているものをアンロードして（名前を指定した `POST /models/unload`）、どのモデルもメモリに残っていないとルーターが報告するまで待ちます（メモリを示すのは `loaded`、`loading`、`unloading`）。待つ時間は最長で `LLAMA_UNLOAD_WAIT_SECONDS` (120) です。サーバーに接続できない、エラーを返す、または待機後もモデルが残っている場合、応答はエラー（HTTP 502）になります。そのため、これを *before* リクエストとして送るプラグインは、空いていない GPU 上で別のモデルを起動することはありません。モデルは自動的に戻ります。次の通常のリクエストでルーターがモデルを読み込むため、プラグインの *after* リクエストは言語モデルについては空でかまいません。本文のモデル名は、いずれかのルートが受け付ける名前であればよく（ルート `openai/*` では `openai/` で始まります。例えば `openai/unload`）、重要なのはヘッダーのほうです。どのルートも受け付けない名前の場合、モデルのアンロード後に 400 で応答します。フックは LiteLLM が名前を確認する前に動くためです。アンロードは、ルート `unload` を持つすべてのサーバーに対して行われ、1 つのモデルだけに対して行われるわけではありません。

このフックは独立したスクリプト `gpu_unload.py` で、`slot_cache.py` の隣に置き、それと連鎖させます。両者は独立しており、`gpu_unload.py` はキャッシュフックなしでも動作します。両方を連鎖させた場合、`gpu_unload.py` は LiteLLM のコールバックの中からキャッシュフックを見つけ、その `release_server` を呼び出します。この関数は **まずすべてのスロットのキャッシュを保存し、そこにあった内容を忘れます**。そうすることで、次のリクエストは再読み込みされたモデルに、そのセッションのキャッシュを復元します。（この呼び出しがないと、キャッシュフックはセッションがまだスロットにあると思い込み、何も復元しません。その結果、入れ替えのたびにプロンプト全体が読み直されることになります。）

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

Docker では、ファイルは `slot_cache.py` と同じようにコンテナ内に存在している必要があります。プロキシの `volumes` に `- ./gpu_unload.py:/app/gpu_unload.py:ro` を追加してください。`slot_cache.py` の行の隣に置きます。ファイルが存在しない場合、起動時に `ImportError: Could not import proxy_handler_instance from gpu_unload` と表示されます（本当の理由、つまり `FileNotFoundError` またはスクリプト自体のエラーは、ログではその数行上にあります）。

2 つを `litellm_config.yml` で連鎖させ、ルートに `unload` を付けてください:

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

Image Generator（または 2 つのリクエストを持つ任意のプラグイン）では、*before* リクエストを `POST`、上記のアドレス、ヘッダー `Authorization: Bearer <key>` と `X-Llama-Unload: all`、そして例の本文で設定してください。ヘッダーは暗号化されて保存されます。

もう一方のモデルのサーバーも、同じプロキシを通じて制御できます。LiteLLM は、あるパスを別のアドレスへ中継します（`general_settings: pass_through_endpoints`、`path`、`target`、`include_subpath: true`。プロキシのキーは検査され、クライアントの `Authorization` ヘッダーは **渡されません**。そのため、転送先は独自のシークレットを `headers` に持ちます）。音声サーバーや画像サーバーのそばで、そのコンテナを停止・起動する小さなサービスが、そのような転送先の例です: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)（`POST /<container>/start` は中のサービスの準備ができたときに、`stop` は終了したときに応答します。README には compose の例と、音声サーバー用の LiteLLM ルート（`/v1/audio/speech`）があります）。

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

確認した内容: LiteLLM 1.108.3 と、`loaded`、`unloading`、`unloaded` を報告する代替ルーターを使いました（待機は、状態が `unloaded` になるまで応答を保留し、停止中のサーバーは 502 を返し、両フックを連鎖させた場合はアンロードの前にスロットが保存され、次のリクエストで復元されました。`gpu_unload.py` だけの場合は、ほかに何も呼び出されませんでした。また、パススルーは転送先に届き、クライアントが送った値の代わりに独自の `headers` 値を追加し、キーのないリクエストを拒否しました）。LiteLLM の背後にある実際の `llama-server` ルーター（31B モデルが 1 つ）に対しても実行しました。リクエストは 1〜2 秒後に `unloaded: <model>` を返し、ルーターは `stopping model instance` と `exited with status 0` を記録しました（プロセスは終了しているため、メモリは解放されています）。何も読み込まれていない状態での 2 回目のリクエストは、すぐに `unloaded: nothing` を返しました。次の通常のリクエストは、ルーターがモデルを再び読み込むため、約 1 分かかりました。お使いの `GET /models` が状態を別の名前で示す場合は、スクリプト内の `IN_MEMORY` を変更してください。
