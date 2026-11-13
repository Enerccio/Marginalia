---
label: プロンプトキャッシュとセッション ID
order: 975
icon: database
---

# プロンプトキャッシュとセッション ID

モデルサーバーは直前に読み込んだプロンプト（*プロンプトキャッシュ*）を保持しているため、同じ文章で始まる次のリクエストでは、それを再び読み込む必要がありません。長い物語では、これは数秒で済むか数分待つかの差になります。キャッシュが効くのは、新しいプロンプトが古いものと同じように始まっている範囲だけです（[キャッシュが効かない理由](#why-a-cache-may-not-help)を参照）。Marginalia は、リクエストが**どの種類の作業か**をサーバーに伝えることもできます。そうすればサーバー、またはその前段のプロキシは、種類ごとにキャッシュを保持できます。

<a id="what-is-sent"></a>

## 送信される内容

Marginalia が作業の種類ごとに行うすべてのリクエストには、次の HTTP ヘッダーが付きます。

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

この値は、同じユーザーが同じ種類の作業を行う限り、どの本についてであっても同じです。ユーザーや種類が異なれば値も異なります。

| 種類 | リクエスト |
|---|---|
| generation | 新しいパート、*再生成*、*スワイプ*。プロンプトは物語を共有するため、値も共有します。 |
| impersonate | *なりきり*。 |
| annotate | *AI で注釈を付ける*と自動注釈。二段階モードの二つ目のモデルは、独自の値を持ちます。 |
| redraft | *書き直し*と、自動注釈の書き直し。 |
| summary | 要約。 |
| agent | [エージェント](agents.md)のリクエスト。実行ごとに、独自の会話を積み上げるため、それぞれ独自の値を持ちます。 |
| help | ヘルプアシスタント。 |
| plugins | プラグインごとに独自の値があります。*Reviewer*、*Side Query*（タブのタイトルは別です）、*Lore Generator*、*Arc Outline*、*Converter*、*Manga Generator*。 |

どの種類にも属さないリクエスト（モデル一覧の取得、接続テスト、トークン数の計算）にはヘッダーを付けません。

この値は、ユーザーと種類から作られるキー付きハッシュです（キーは[データフォルダー](administration/index.md#the-data-folder)の `secret.key` から取得されます）。ユーザーも本も明かさず、誰のものかを突き止めることもできません。また、同じユーザーでも別のインストール環境では別の値になります。値はユーザーと種類ごとに一つで、本ごとではありません。そのため、サーバーがそれ以上のキャッシュを保持する必要はありません。

すべてのプロバイダーがこのヘッダーを送ります。OpenAI 互換のプロバイダー、そして [Anthropic Claude](extensions/anthropic.md) と [Google Gemini](extensions/google-genai.md) のプロバイダーです。このヘッダーを使わないサーバーは、それを無視します。オフにする設定はありません。

<a id="why-a-cache-may-not-help"></a>

## キャッシュが効かない理由

サーバーは、新しいプロンプトが古いものと同じである部分のキャッシュを、**最初のトークンから**再利用します。最初の違いでそこで止まり、それ以降はたとえ大部分が変わっていなくても、もう一度読み込まれます。リクエストの開始時に出力される `llama-server` の行を見てください。

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` は、新しいリクエストが保持するキャッシュ済みプロンプトの割合です。1 に近ければキャッシュが使われます。ここでは 15 % しか使われていないため、キャッシュがディスクから復元されたものであっても、スロットに残ったままのものであっても、プロンプトは早い段階で古いものと異なっています。

**スライディングウィンドウ**を持つモデル（例えば Gemma）やハイブリッドモデルは、キャッシュの末尾を捨てて途中から続けることができません。`llama.cpp` のソースによれば、`llama-server` がキャッシュ済みプロンプトから続けられるのは、新しいプロンプトがそれを正確に延長している場合か、二つが異なる位置の時点かそれより前に**コンテキストチェックポイント**が存在する場合に限られます。チェックポイントは、ユーザーメッセージの先頭（少なくとも 8192 トークン間隔）と、プロンプトの末尾の少し前にだけ作られ、スロットファイルに保存されます（`appended N context checkpoint(s)` と `restored N context checkpoint(s)` という INFO 行で作成と復元がわかります）。使えるチェックポイントがなければ、`f_keep` の値にかかわらずプロンプト全体を読み直し、それはトレースレベル（`-lv 4`: `forcing full prompt re-processing due to lack of cache data`）でのみ報告されます。そのため、生成、注釈、書き直しの物語は、それぞれのパートがユーザーのメッセージに続くモデルの応答として、メッセージの連鎖で送られます。最後のユーザーメッセージがチェックポイントになり、次のリクエストはそこから異なり始めます。そして新しいパートはメッセージを末尾に追加するだけです。ログから冷えた prefill を読み取るには、最初の `prompt processing, n_tokens = 4096` の行が、ここでは約 7 秒後に出力されます。プレフィックスが再利用されていれば、それは一、二秒後に出力されるはずです。

[プロンプト](books/prompts.md#how-the-prompt-is-put-together)は**システムメッセージ**で始まり、その後に物語が続きます。そのため、システムメッセージの内容はリクエストごとに同じである必要があります。既定のマスターテンプレートでは、システムメッセージにはこの順序で、ロアブロック、要約、作者のメモ（注釈）が入ります。それらのいずれかが二つのリクエストの間で変わると（通常は起こります。新しい文章が別のロアエントリーを有効にしたり、要約が追加されたり、メモが追加または解決されたりします）、キャッシュはその位置までしか有効でなくなり、その後ろの物語全体が読み直されます。物語が収まりきらなくなると、最も古いパートから先にプロンプトから外れるため、ターンごとに物語の開始位置が一つのパート分ずつずれます。役に立つのは次のとおりです。

- パートごとに変わるエントリーには、挿入モードとして **ユーザープロンプトの前** を設定します（[順序と挿入](lorebooks/entries.md#order-and-insertion)）。これらは物語の後ろのプロンプトの末尾に置かれ、キャッシュを乱しません。常に真である内容だけをロアブロックに残します。
- 使わないブロックはマスターテンプレートから外します（例えば、モデルにメモを渡さない場合の作者のメモ）。そうすればシステムメッセージは変わりません。
- パートのメニューで **プロンプトを比較** を開くと、キャッシュがどこで終わっているかがわかります。このパートのプロンプトを、より前のパートのプロンプト（既定では最も近いもの。ボックスには、その前のパートとスワイプが一覧表示されます）と比べ、最初に異なるメッセージを示します。[プロンプトを比較](#compare-prompt)を参照してください。

<a id="compare-prompt"></a>

### プロンプトを比較

パートのメニュー（デスクトップとスマートフォン）の **プロンプトを比較** は、*なぜこのパートではキャッシュが使われなかったのか？* に答えます。二つのプロンプトのメッセージ（システムメッセージ、次にパートごとのユーザーメッセージとモデルの応答、最後にそのターンのユーザーメッセージ）を、それぞれ状態付きで一覧表示します。状態は、同じ、変更、追加、削除のいずれかです。最初に異なるメッセージは強調表示され、その後ろのメッセージには *再読み込み* と表示されます。サーバーは最初の違いから後ろをすべて読み直すためです。表の上には次の情報があります。

- 最初の違いがどこにあるか（メッセージと文字の位置）、そして先頭から同じである範囲の大きさ（文字数と、おおよそのトークン数）。
- 違いの解説。**システムメッセージ**が異なる（脱獄、テンプレート、ロア、要約、メモのいずれかが二つのリクエストの間で変わった。通常の原因です）。物語の**途中のパート**が異なる（編集、再生成、または要約に置き換えられた）。**物語の開始位置がずれた**（最も古いパートが外されたか、要約された）。あるいは違いが**前のプロンプトの最後のメッセージだけ**である（そのターンのユーザーメッセージで、物語は繰り返さない）。最後のケースでは、次のパートやスワイプがキャッシュを再利用できる範囲が最も多くなります。それでもサーバーがキャッシュを使わなかった場合、原因はプロンプトではありません（スロット、モデル、セッション ID を確認してください）。

メッセージを選ぶと、最初の違いの少し手前から、二つのテキストが並べて表示されます。比較されるのはメッセージだけです。モデル、リクエストのツール、サーバーのスロットは、保存されたプロンプトには含まれません。パートには保存されたプロンプトが必要です（作者が入力したパートや、プロンプトを保存する前に生成されたパートには、保存されたプロンプトがありません）。

次の種類のリクエストは、種類を切り替えたときに何が保持されるかに関係します。そのうちどれだけが使われるかは、プロンプトが決めます。
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 使い方：llama.cpp でディスク上の 1 スロットを使う

この仕組みが役立つのは、サーバーのキャッシュ用メモリが足りない場合です。十分なメモリを持つローカルの `llama-server` は、スロットを複数用意して作業の種類ごとにキャッシュを保持できるので、それ以上の設定は必要ありません。メモリが **1 スロット分** しかないと、ほかの種類の作業が来るたびに物語のキャッシュが捨てられてしまいます。パートの注釈、その後の書き直し、次の生成は、毎回プロンプトを読み直すことになります。

解決策は、作業の種類ごとのキャッシュをディスクに保存しておき、その種類の要求が来たときに 1 スロットへ入れ替えることです。`llama-server` はスロットをファイルに保存して復元できます。その前に置いたプロキシが、ヘッダーから要求の種類を判断します。以下の例は [LiteLLM](https://docs.litellm.ai/) 用のフックです。

**要件:** `llama-server` は 2026-10-08 以降のビルドであること（llama.cpp PR #26004、*preserve context checkpoints across slot save/restore*）。それより古いものはスロットのメモリを書き出し・復元しますが、[コンテキストのチェックポイント](#why-a-cache-may-not-help)は扱いません。そのため、スライディングウィンドウを使うモデル（例: Gemma）やハイブリッドモデルは、復元のたびにプロンプト全体を読み直し、変わっていない部分がどれだけあってもお構いなしになります。`llama-server --version` でビルドを確認してください。`full` や `server` のような移動するタグのコンテナイメージは、pull するまで更新されません。

スロットを 1 つ、ファイル用のフォルダーを指定して `llama-server` を起動します:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

次の内容を LiteLLM の設定ファイルと同じ場所に `slot_cache.py` として保存します:

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

LiteLLM の `config.yaml` で有効にします。コールバックと、`llama-server` に向かうルートの `model_info` にある `slot_cache` です。ワイルドカードの項目 1 つで十分で、LiteLLM にモデルを列挙する必要はありません（モデルはサーバーが知っており、クライアントが使いたいものを指定します）:

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

フック自身はアドレスを持ちません。要求が向かうルートの `api_base` と `api_key` を使います（スロットはサーバーのルートにあるため、末尾の `/v1` は取り除かれます）。`slot_cache` を持つルートだけが対象です。それ以外のモデル（例えばクラウドのモデル）への要求は、ヘッダーが付いていても素通しされます。複数のサーバーを並べて使うこともでき、それぞれが独自のスロットを持ちます。ワイルドカードを使うと、ルートはクライアントが要求したモデルになるので、ファイルはモデルごと（`qwen3-32b` など）に保存されます。別の設定が必要なモデルは、ワイルドカードの隣に名前を指定して個別に列挙することもできます。LiteLLM はモデル名の `/` より前の部分をプロバイダー名として扱うため、`/` を含む名前（ルーターのプリセットのような `org/model`）の場合は、ルートを `model_name: "openai/*"` とし、Marginalia にはモデルを `openai/org/model` として指定します。フックが `openai/` を取り除き、ファイル名とスロットへの呼び出しにはサーバーが知っている名前が使われます。ルートとその `model_info` は `model_list` から読み込まれます。`model_list` なしで起動したプロキシ（`litellm --model ...` だけの場合）にはどちらもなく、フックは何もしません。ワイルドカードの項目 1 つがその代わりになります。

**ルーターモードの `llama-server`**（1 つのアドレスの裏に多数のモデルがあり、必要に応じて読み込まれる構成）でも動作します。各モデルは独自のスロットを持つ個別のインスタンスなので、フックはサーバー・モデル・スロットごとに状態を保持し、スロットへの呼び出しの本文にモデル名を指定します（クライアントが使った名前から LiteLLM の `openai/` を除いたもの。設定 `model` を指定すると別の名前になります。ルートの名前がモデルと一致しない場合に使います）。2026 年 2 月以降の llama.cpp が必要です。それ以前は `/slots` が `400 Invalid action` を返していましたが、新しいものはインスタンスに転送します。

ルーターが保持できるモデルが一部だけの場合（`--models-max`、既定は 4）、別のモデルを要求すると、最も長く使われていなかったモデルがアンロードされ、そのスロットとその中のキャッシュは失われます。フックは同じ順序で追跡します（`slot_cache` の `models_max` または `LLAMA_MODELS_MAX` を `--models-max` と同じ数に設定してください。0 は無制限）。そして、ルーターにアンロードさせる要求の**前に**、そのスロットを保存します。そのためには、プロキシがルーターの唯一のクライアントである必要があります。別の経路で届いた要求は見えません。保存によって、ルーターがアンロードしたモデルが読み込み直されることはありません。動作は次のとおりです: セッションのキャッシュは、**スロットが別のものに必要になったとき**（別の種類の作業、別のモデル、またはルーターによるモデルのアンロード）にディスクへ書き出されます。毎回の要求ごとではありません。生成のたびに 3 GB を書き込むと、サーバーの動作が遅くなるだけだからです。たとえば、生成→注釈→生成の順なら、ファイルは最初の切り替えの後に現れます。注釈の前に生成が保存されるわけです。`slot_cache` の `save_idle_seconds`（または `LLAMA_SAVE_IDLE_SECONDS`）を設定すると、その時間だけ要求がなかった場合にもキャッシュが書き出されるため、作業を止めた後すぐにフォルダーに保存されます。要求があると待機は最初から数え直されます。すでにディスクにあるキャッシュは、次の切り替え時に再び書き出されません。フックは、スロットが空いているときにだけ保存・復元・消去を送ります（`GET /slots` の `is_processing` を確認します。そのエンドポイントがなければすぐに送ります）。`llama-server` は要求の実行中にそのような呼び出しを保留にし、要求が終わるたびに保留中の呼び出しを 1 つだけ解放するため、保留の後ろに来た別のクライアントの要求は、いつまでも待たされる可能性があるからです。保存の書き込み中、サーバーはほかの処理を行いません。

詳細: 別のセッションの要求が届くと、フックはスロットの内容をそのセッションの名前で保存し、新しいセッションのファイルを復元します（ファイルがなければスロットを空にします）。そのうえで要求を通します。ファイルは**ルートごと**（`model_name`）に保存されます。保存された状態は、それを書き出したモデルにしか合わないため、ファイル名にはルートとセッションが使われ、同じサーバーやフォルダーを使っていても、2 つのルートがファイルを共有することはありません。その後サーバーは、復元されたプロンプトと異なる部分だけを読みます。復元できないキャッシュ、消えたファイル、応答しないサーバーは、プロンプトを読み直すだけの結果になります。フックが原因で要求が失敗することはありません。

知っておくべきこと:

- **サイズ。** ファイルはスロットのメモリと**そのコンテキストのチェックポイント**を含むため、両方を合わせた大きさになります。長い物語と大きなモデルでは、数百メガバイトから数ギガバイトに達します。実測例: 31B のモデルでプロンプトが 18,000 トークンの場合、ファイルは 1.3 GB から 11 GB になりました。最大のものには、約 450 MB のチェックポイントが約 18 個と、約 1.5 GB のキャッシュが含まれていました。サーバーは既定で最大 32 個のチェックポイントを保持し、切り替えのたびにすべてを書き込み・読み込みます。`llama-server` の `--ctx-checkpoints N` で上限を設定すると、ファイルと切り替えは小さくなりますが、チェックポイントが役立つのは、次のプロンプトが、キャッシュされたものと最初に異なる位置がそのチェックポイントの位置か、そのすぐ後である場合だけです。新しいパートや選び直し（swipe）は最後のユーザーメッセージの先頭（最新のチェックポイント）で異なりますが、注釈や書き直しは数パート前の部分を変えることがあります。そのため、物語が遡る範囲に応じたチェックポイントを保持してください（フックの `prompt:` の行を見ると、各要求がどこで異なるかがわかります）。ファイルはルート・ユーザー・種類ごとに 1 つで、それぞれ同じ場所に上書きされるため、フォルダーはこの大きさに保たれます。
- **速度。** 切り替えがプロンプトの読み直しより速いか確認してください（フックは、保存と復元のすべてについて、サイズと時間を記録します）。上の例は、NVMe ディスクと AMD Radeon AI PRO R9700 での測定です。11 GB の保存は 6 秒、20,000 トークン分の 7.6 GB の復元は 8 秒、切り替えはおよそ 14 秒でした。一方、同じ長さのプロンプトを読み直すと、毎秒 440 トークンでおよそ 45 秒かかります。プリフィルはプロンプトが長くなるほど増えます。チェックポイントなしでは、同じファイルは約 1 GB で、切り替えは 5 秒でした。
- **メモリ。** スロットのチェックポイントはサーバーの**システムメモリ**（GPU のメモリではない）に保持されます。その数は `--ctx-checkpoints` まで（既定は 32 個。大きなモデルでは各数百 MB）で、保存や復元のたびに全部を読み書きします。`llama-server` は、手放したスロットのプロンプトを独自のメモリキャッシュ（`--cache-ram`、既定は 8 GiB）にも保持します。フックのファイルが同じ役割を果たすため、メモリに余裕がないマシンでは 0 に設定できます。マシンがスワップを始めると、チェックポイントを先にスワップから読み戻す必要があるため、保存と復元は数倍遅くなります。ディスクを見る前に `free -h` を確認してください。
- **上限。** 上限を設けないと、フォルダーはルート、ユーザー、種類の数に応じて大きくなります。ルートの `slot_cache` に `max_files`（ファイル数）や `max_gib`（合計サイズ）を設定してください（またはプロキシの環境変数 `LLAMA_CACHE_MAX_FILES` と `LLAMA_CACHE_MAX_GIB` で、すべてのルートに設定できます）。すると、保存のたびにフックは、そのルートのキャッシュのうち**最も長く使われていないもの**を削除します。キャッシュを復元することも「使用」に数えられます。フックは、いま保存したキャッシュや、これから復元するキャッシュは削除しません（そのため、1 つのルートで 2 つのセッションを交互に使う場合は、上限に関係なく少なくとも 2 つのファイルが残ります）。また、そのルートのファイルにしか触れないため、ほかのルートやフォルダー内のほかのファイルは安全です。`max_gib` より大きい単独のキャッシュは残ります（ログに警告が出ます）。削除にはフォルダーへのアクセスが必要です。`save_path`（または `LLAMA_SLOT_SAVE_PATH`）に、プロキシから見た `--slot-save-path` のフォルダーを設定してください。`llama-server` をコンテナで動かす場合は、両方のコンテナがマウントするボリュームになります。設定がなければ何も削除されません。削除されたキャッシュは、そのセッションが一度だけプロンプトを読み直す原因になるだけです。上限は任意です。サイズに上限を設けたくない場合は、ホストの cron ジョブでも十分です。たとえば `find /srv/ai/slots -name '*.bin' -mtime +2 -delete` のようにします（使用中のキャッシュは、そのセッションが切り替えられて外されるたびに保存し直されるため、経過時間は改めて数え直されます）。フックの外から削除されたファイルは問題ありません。欠けているファイルは、単にキャッシュがないことになります。
- **ルートのモデルを変更した後**は、そのルートのファイルをフォルダーから削除してください（名前はルートで始まります）。別の重みで保存された状態は、新しいモデルには合いません。
- **切り替えのたびに保存と復元が発生します。** [自動注釈](books/annotations.md)を有効にしていると、パートの後に注釈と書き直しが続くため、1 ターンで 3〜4 回切り替わることがあります。頼る前に、1 ターンの時間を測ってください。
- **プロキシの再起動。** プロキシを再起動すると、どのセッションがスロットにあるかを忘れます。再起動後の最初の要求は、そのファイルをスロットに復元し、スロットの内容は失われます。
- **フックの知らないうちにルーターがアンロードしたモデル。**（`models_max` の誤設定や別のクライアントによる場合）スロットは空なのに、フックは最後のセッションがまだ入っていると考えます。同じセッションの要求は単にプロンプトを読み直します。別のセッションの要求では、先に空のスロットをそのセッションのファイルに保存してしまうため、そのキャッシュも失われます。どちらの場合も、失われるのは前処理（プリフィル）の時間だけです。
- **`llama-server` は、単純な保存や復元をログに記録しません。**（追加または復元するコンテキストのチェックポイントと、エラーだけを記録します。）何をしたかは応答に含まれており、プロキシのログ（`LITELLM_LOG=INFO` 指定時）にはフックが行うすべての呼び出しの行があります。例: `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB, 3.1 s)`、`… save <file> …: ok (…)`、`… restore … miss (400)`。セッションの最初の要求での miss は正常です（`llama-server` は "Unable to restore slot, no available space in KV cache or invalid slot save file" と記録します）。本当に問題があるのは警告です。アドレスと合わない証明書、拒否された API キー、応答しないサーバーなどです。
- **プロンプトが異なる箇所。** セッションの 2 回目以降のすべての要求について、ログには、そのセッションの前回のプロンプトとどこまで同じかを示す行があります。例: `slot_cache: <route>_<session> prompt: 14 messages, the last one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`。サーバーはそこまでしかキャッシュを再利用できません（メッセージはおよそ 3〜4 文字で 1 トークンです）。そのため、2 回目の要求がほぼすべてを読み直す場合は、この行を確認してください。示されたメッセージの先頭が変わったものです。この行は、2 つのプロンプトの最後のメッセージも比較します。最後のメッセージには、変わる部分（設定、パート、指示）が入っています。物語にパートが追加されると、古い最後のメッセージは同じ位置にはなくなります。2 つの最後のメッセージが共有する文字数は、たとえば設定（ロア）がどこまで同じかを示します。
- **何も起きない場合は**、プロキシのログを確認してください（`docker logs litellm-proxy 2>&1 | grep slot_cache`）。フックが動いていれば、呼び出しごとに行が出ます。`slot_cache` を持たないルートへの Marginalia の要求は、一度だけ `not used for <route>: no model_info.slot_cache` と報告されます。`slot_cache` の行がまったくない場合は、フックが動いていません。コンテナにまだ古いファイルか設定があるということです。エディターがファイルを置き換えた後も、単独のファイルをコンテナにマウントした場合（`./slot_cache.py:/app/slot_cache.py`）はコンテナ作成時の版が表示され続けます。`docker compose up -d --force-recreate` で新しいものが読み込まれます（単なる再起動では読み込まれません）。`docker exec litellm-proxy grep -c save_idle_seconds /app/slot_cache.py` が 0 を出力したら、ファイルは古すぎます。
- **Docker では**、プロキシと `llama-server` は 2 つのコンテナです。そのため、`--slot-save-path` のフォルダーはホストのディレクトリで、両方がマウントします（プロキシは古いファイルを削除し、使用されたファイルの経過時間を更新するため、そこへの書き込みが必要です）。フックのファイルは LiteLLM の設定ファイルの隣に置きます。`api_base` は、プロキシが到達できるアドレスにし、サーバーが TLS を使う場合は証明書のホスト名を使います。

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  ルートの `slot_cache` には `save_path: /slots` と `models_max: 1` を設定します。
- フックは `CustomLogger.async_pre_call_deployment_hook` を使います。これは LiteLLM がデプロイメントを選んだ後、要求を送る前に呼び出すものです（バージョン 1.106.5 は、実際のルーターとスタンドインのサーバーで確認しました）。お使いのバージョンで確認してください。モデルを 1 つのアドレスの裏に置き、単一のサーバーを再起動して切り替えるプロキシ（llama-swap）はサポートしていません。スロットへの呼び出しは、読み込まれているどのモデルに届くか分からないためです。これらのいずれも、実際の `llama-server` のルーターでは実行していません。テストは、フックが行う呼び出しを確認するスタンドインを使っています。
<a id="freeing-the-gpu-for-another-model"></a>

## GPUをほかのモデルに解放する

言語モデルを載せたGPUには、その隣に画像モデルや音声モデルを置く余裕がありません。`llama-server` をルーターモードで動かしている場合、もう一つのフックがリクエストに応じてモデルをアンロードできます。そこで、GPUを必要とするプラグイン([Image Generator](extensions/image-generator.md#one-gpu-for-the-model-and-the-images)、Audiobook)は、ほかのすべての処理と同じキーを使ってプロキシにリクエストを送り、GPUの解放を依頼します。

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

ヘッダー `X-Llama-Unload` を付けたリクエストには、フック自身が応答します(HTTP 200、本文は `unloaded: <names>`)。モデルには届きません。フックはルーターのモデル一覧(`GET /models`)を取得し、読み込まれているモデルを `POST /models/unload` で名前を指定してアンロードします。そのあと、ルーターがどのモデルもメモリにないと報告するまで待ちます(`loaded`、`loading`、`unloading` の状態はメモリを占有しています)。待つのは最長で `LLAMA_UNLOAD_WAIT_SECONDS`(120)秒です。サーバーに接続できない場合、エラーを返した場合、あるいは待ったあともモデルがメモリに残っている場合は、応答はエラー(HTTP 502)になります。そのため、プラグインがこれを *before* リクエストとして送った場合、満杯のGPUに別のモデルを読み込むことはありません。モデルは自動的に戻ります。次の通常のリクエストで、ルーターがモデルを読み込み直すからです。したがって、プラグインの *after* リクエストは言語モデルに対しては空でかまいません。本文のモデル名は、いずれかのルートが受け付ける名前であれば十分です(ルートが `openai/*` の場合は `openai/` で始まる名前、例えば `openai/unload`)。重要なのはヘッダーのほうです。どのルートも受け付けない名前の場合、フックはLiteLLMがその名前を確認する前に動作するため、モデルをアンロードしたあとで400が返ります。アンロードは1つのモデルだけでなく、`unload` を持つルートのあるすべてのサーバーに送られます。

フックは独立したスクリプト `gpu_unload.py` で、`slot_cache.py` と並べて、連結して使います。両者は互いに独立しています。`gpu_unload.py` はキャッシュフックなしでも動作します。両方を連結した場合、`gpu_unload.py` はLiteLLMのコールバックの中からキャッシュフックを見つけ、その `release_server` を呼び出します。これは、**まず各スロットのキャッシュをすべて保存し、中身を忘れる**処理です。そのため、次のリクエストは、再読み込みされたモデルにセッションのキャッシュを復元できます。(この呼び出しがないと、キャッシュフックはセッションがまだスロットにあると考え、何も復元しません。その結果、モデルを切り替えるたびにプロンプト全体を読み直すことになります。)

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

Dockerでは、ファイルを `slot_cache.py` と同じようにコンテナに入れてください。プロキシの `volumes` に、`slot_cache.py` の行の隣に `- ./gpu_unload.py:/app/gpu_unload.py:ro` を追加します。ファイルがないと、起動時に `ImportError: Could not import proxy_handler_instance from gpu_unload` と表示されます。本当の原因(`FileNotFoundError` やスクリプト自身のエラー)は、ログのその数行上に出ています。

2つのフックを `litellm_config.yml` で連結し、ルートに `unload` を付けます。

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

Image Generator(または、2つのリクエストを持つ任意のプラグイン)では、*before* リクエストを、上のアドレスへの `POST` に設定します。ヘッダーは `Authorization: Bearer <key>` と `X-Llama-Unload: all`、本文は例のものを使います。ヘッダーは暗号化して保存されます。

もう一方のモデルのサーバーも、同じプロキシ経由で操作できます。LiteLLMは、パスを別のアドレスへ中継できます(`general_settings: pass_through_endpoints`、`path`、`target`、`include_subpath: true`)。プロキシのキーは検査されますが、クライアントの `Authorization` ヘッダーは**転送されません**。そのため、転送先は `headers` に独自のシークレットを持ちます。音声サーバーや画像サーバーの隣で、コンテナを停止・起動する小さなサービスが、そのような転送先になります。それが [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control) です(`POST /<container>/start` は、その中のサービスが準備できると応答し、`stop` は終了すると応答します。そのREADMEには、composeの例と、音声サーバー向けのLiteLLMルート `/v1/audio/speech` が載っています)。

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

確認した内容は次のとおりです。LiteLLM 1.108.3で、`loaded`、`unloading`、`unloaded` を報告する代替のルーターを使って検証しました。待機は、状態が `unloaded` になるまで応答を保留しました。停止中のサーバーは502を返しました。2つのフックを連結した場合、アンロードの前にスロットが保存され、次のリクエストで復元されました。`gpu_unload.py` だけの場合は、ほかに何も呼ばれませんでした。転送では、目的のサーバーに届き、クライアントが送ったヘッダーの代わりに `headers` の値が使われ、キーのないリクエストは拒否されました。さらに、LiteLLMの背後で実際の `llama-server` ルーター(31Bモデル1つ)を使って実行しました。リクエストは1~2秒後に `unloaded: <model>` を返しました。ルーターのログには `stopping model instance` と `exited with status 0` が出ました(プロセスは終了したので、メモリは解放されています)。何も読み込まれていない状態での2回目のリクエストは、すぐに `unloaded: nothing` を返しました。次の通常のリクエストは、ルーターがモデルを読み込み直すため、約1分かかりました。お使いの `GET /models` の状態名が異なる場合は、スクリプトの `IN_MEMORY` を変更してください。
