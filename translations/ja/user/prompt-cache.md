---
label: プロンプトキャッシュとセッションID
order: 975
icon: database
---

# プロンプトキャッシュとセッションID

モデルサーバーは、読み込んだばかりのプロンプト（*プロンプトキャッシュ*）を保持します。これにより、同じ文章で始まる次の要求では、そのプロンプトを読み直さずに済みます。長い物語では、数秒で済むか数分待つかの違いになります。キャッシュは、新しいプロンプトが古いものと同じ始まり方をしている範囲でしか効きません（[キャッシュが効かない理由](#why-a-cache-may-not-help)を参照）。Marginalia は、要求が **どの種類の作業か** をサーバーに伝えることもできます。そうすると、サーバー（またはその前段に置かれたプロキシ）は種類ごとにキャッシュを保持できます。

<a id="what-is-sent"></a>

## 送信される内容

Marginalia が作業の種類ごとに行うすべての要求には、次のHTTPヘッダーが付きます。

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

値は、同じユーザーが同じ種類の作業を行う場合、どの作品についてかに関係なく同じです。値はユーザーごと、種類ごとに異なります。

| 種類 | 要求 |
|---|---|
| 生成 | 新しいパート、*Regenerate*、*Swipe*。これらのプロンプトは物語を共有するため、値も共有します。 |
| 代理入力 | *Impersonate*。 |
| 注釈 | *Annotate with AI* と自動注釈。2段階モードの2つ目のモデルは、独自の値を持ちます。 |
| 書き直し | *Redraft* と、自動注釈の書き直し。 |
| 要約 | 要約。 |
| エージェント | [エージェント](agents.md)の要求。実行ごとに独自の会話を積み上げるため、それぞれ独自の値を持ちます。 |
| ヘルプ | ヘルプアシスタント。 |
| プラグイン | 各プラグインがそれぞれ独自の値を持ちます：*Reviewer*、*Side Query*（タブのタイトルは別のもの）、*Lore Generator*、*Arc Outline*、*Converter*、*Manga Generator*。 |

種類に属さない要求（モデル一覧の取得、接続テスト、トークン数の計算）では、ヘッダーは送られません。

値は、ユーザーと種類から作られたキー付きハッシュです（キーは[データフォルダ](administration/index.md#the-data-folder)内の `secret.key` から得られます）。ユーザーも作品も明らかにせず、誰に属するかを特定することはできません。別のインストールでは、同じユーザーでも別の値になります。値はユーザーと種類の組み合わせごとに1つで、作品ごとではありません。そのため、サーバーがそれ以上のキャッシュを保持する必要はありません。

すべてのプロバイダーがこのヘッダーを送ります。OpenAI互換のプロバイダーに加え、[Anthropic Claude](extensions/anthropic.md) と [Google Gemini](extensions/google-genai.md) のプロバイダーも含みます。ヘッダーを使わないサーバーは、これを無視します。オフにする設定はありません。

<a id="why-a-cache-may-not-help"></a>

## キャッシュが効かない理由

サーバーは、新しいプロンプトが古いものと **最初のトークンから** 同じである部分のキャッシュを再利用します。最初の違いでそこで止まり、それ以降は、変わっていない部分がどれだけあっても読み直されます。要求の開始時に出力される `llama-server` 行を見てください。

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` は、新しい要求が保持しているキャッシュ済みプロンプトの割合です。1 に近ければキャッシュが使われています。ここでは 15 % しか使われていないので、プロンプトは最初のほうで既にキャッシュと違っています。キャッシュがディスクから復元されたものか、スロットに残っていたものかは関係ありません。

**スライディングウィンドウ**を持つモデル（例えば Gemma）やハイブリッドモデルは、キャッシュの末尾を捨てて途中から続けることができません。`llama.cpp` のソースによれば、`llama-server` は、新しいプロンプトがキャッシュ済みプロンプトを正確に延長している場合、または違いが生じる箇所より前に **コンテキストチェックポイント** が存在する場合にしか、キャッシュ済みプロンプトから続行できません。チェックポイントは、ユーザーメッセージの先頭（少なくとも8192トークンおき）と、プロンプトの末尾近くにのみ作られ、スロットファイルに保存されます（`appended N context checkpoint(s)` と `restored N context checkpoint(s)` のINFO行が作成時を示します）。使えるチェックポイントがなければ、`f_keep` の値にかかわらずプロンプト全体を読み直し、その旨はトレースレベルでのみ報告されます（`-lv 4`：`forcing full prompt re-processing due to lack of cache data`）。

そのため、生成、注釈、書き直しの物語は、メッセージの連なりとして送られます。各パートは、ユーザーのメッセージの後にモデルの応答が続く形になります。最後のユーザーメッセージがチェックポイントになり、次の要求はそこから異なり始めます。新しいパートは、末尾にメッセージを追加するだけです。ログからコールドプリフィルを読み取る場合、最初の `prompt processing, n_tokens = 4096` 行はここでは約7秒後に出ます。接頭辞が再利用されていれば、1、2秒後に出ていたはずです。

[プロンプト](books/prompts.md#how-the-prompt-is-put-together)は **システムメッセージ** から始まり、その後に物語が続きます。そのため、システムメッセージの内容は要求ごとに同じでなければなりません。デフォルトのマスターテンプレートでは、システムメッセージには、この順でロアブロック、要約、作者のメモ（注釈）が入ります。これらのいずれかが2回の要求の間で変わると（新しいテキストが別のロア項目を有効にする、要約が追加される、メモが追加または解決されるなど、よくあることです）、キャッシュはその地点までしか有効でなく、その後ろの物語全体が読み直されます。物語が収まらなくなると、最も古いパートから外れていくため、ターンごとに物語の開始位置が1パートずつずれます。有効な対策は次のとおりです。

- パートごとに変わる項目には、挿入モードを **Before User Prompt** にしてください（[順序と挿入](lorebooks/entries.md#order-and-insertion)）。これらは物語の後ろ、プロンプトの末尾に置かれ、キャッシュを乱しません。ロアブロックには、常に真である内容だけを残してください。
- 使わないブロックはマスターテンプレートから外してください（例えば、モデルにメモを与えないなら作者のメモ）。そうすれば、システムメッセージは変わりません。
- パートのメニューから **Compare Prompt** を開くと、キャッシュがどこで終わっているかが分かります。このパートのプロンプトを、より前のパートのプロンプトと比べ、最初に違うメッセージに印を付けます。比較対象はデフォルトでは最も近いパートです。ボックスには、それより前のパートとそのスワイプが並びます。[Compare Prompt](#compare-prompt) を参照してください。

### Compare Prompt

パートのメニュー（デスクトップとスマートフォン）にある **Compare Prompt** は、「このパートでキャッシュが使われなかったのはなぜか」に答えます。二つのプロンプトのメッセージ（システムメッセージ、その後にパートごとのユーザーメッセージとモデルの応答、そしてそのターンのユーザーメッセージ）を、それぞれ同じ、変更、追加、削除のいずれかの状態とともに表に並べます。最初に違うメッセージは強調表示され、それ以降のメッセージには *Read again* と印が付きます。サーバーは最初の違いから後をすべて読み直すためです。表の上には次の情報が表示されます。

- 最初の違いがどこにあるか（メッセージと文字位置）、そして先頭から同じ部分がプロンプト全体のどれだけを占めるか（文字数と、おおよそのトークン数）。
- 違いの解釈。**システムメッセージ**が違う（ジェイルブレイク、テンプレート、ロア、要約、メモのいずれかが二つの要求の間で変わった。よくある原因です）、**物語の途中のパート**が違う（編集、再生成、要約への置き換えがされた）、**物語の開始位置がずれた**（最も古いパートが除外されたか要約された）、または違いが **前のプロンプトの最後のメッセージ**、つまりそのターンのユーザーメッセージだけである（物語はこれを繰り返さない）。最後のケースは、次のパートやスワイプが再利用できる最大の範囲です。それでもサーバーがキャッシュを使わなかった場合、原因はプロンプトではありません（スロット、モデル、セッションIDを確認してください）。

メッセージを選ぶと、最初の違いの少し手前から、二つのテキストを並べて表示します。比較されるのはメッセージだけです。モデル、要求のツール、サーバーのスロットは、保存されたプロンプトには含まれません。パートには保存されたプロンプトが必要です（作者が手で入力したパートや、プロンプトを保存する前に生成されたパートには、保存されたプロンプトがありません）。

以下の要求の種類は、種類を切り替えたときに何が保持されるかに関わります。どれだけ使われるかはプロンプトが決めます。
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 使い方: llama.cpp でスロット1つをディスクと併用する

サーバーのキャッシュ用メモリが足りないときは、この方法が役に立ちます。ローカルの`llama-server`に複数スロット分のメモリがあれば、作業の種類ごとにキャッシュを保持できるので、それ以上の設定は必要ありません。スロットが**1つ**しかない場合は、別の種類の作業が入るたびにストーリーのキャッシュが捨てられます。パートへの注釈、その後の書き直し、次の生成では、そのたびにプロンプトを読み直すことになります。

対策は、作業の種類ごとのキャッシュをディスクに保存しておき、その種類のリクエストが来たときに1つのスロットへ読み込み直すことです。`llama-server` はスロットをファイルに保存して復元できます。その前段に置いたプロキシが、ヘッダーからリクエストの種類を判断します。以下の例は [LiteLLM](https://docs.litellm.ai/) 用のフックです。

**前提条件:** `llama-server` の 2026-10-08 以降のビルドであること（llama.cpp PR #26004、*preserve context checkpoints across slot save/restore*）。それより古いビルドは、スロットのメモリは保存・復元しますが、[コンテキストのチェックポイント](#why-a-cache-may-not-help)は保存しません。そのため、スライディングウィンドウを使うモデル（例：Gemma）やハイブリッドモデルでは、復元のたびにプロンプト全体を読み直すことになり、変わっていない部分の量は関係ありません。ビルドは `llama-server --version` で確認してください。移動するタグを持つコンテナイメージ（`full`、`server`...）は、pull するまで更新されません。

`llama-server` を、1スロットとファイル用フォルダーを指定して起動します:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

これを LiteLLM の設定ファイルと同じ場所に `slot_cache.py` という名前で保存します:

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

そして LiteLLM の `config.yaml` でオンにします。コールバックを追加し、さらに `slot_cache` を、`llama-server` に向かうルートの `model_info` に設定します。ワイルドカードのエントリーが1つあれば十分で、LiteLLM にモデルを個別に列挙する必要はありません（サーバーがモデルを把握しており、クライアントが使いたいモデルを指定します）:

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

フックには独自のアドレスは不要です。リクエストの送信先ルートの `api_base` と `api_key` を使います（スロットのパスはサーバーのルートにあるため、末尾の `/v1` は取り除かれます）。`slot_cache` を持つルートだけが対象になります。ヘッダーがあっても、他のモデル（例えばクラウド上のモデル）へのリクエストはそのまま通ります。複数のサーバーを並べて使うこともでき、それぞれが独自のスロットを持ちます。ワイルドカードを使う場合、ルートはクライアントが指定したモデルになるため、ファイルはモデルごとに保存されます（`qwen3-32b`）。別の設定が必要なモデルは、ワイルドカードの隣に名前で列挙することもできます。LiteLLM はモデル名の `/` より前の部分をプロバイダーとして扱うため、そのような名前（`org/model` のようなルーターのプリセットの場合）では、ルート `model_name: "openai/*"` を使い、Marginalia にはモデルを `openai/org/model` として渡してください。フックは `openai/` を取り除き、ファイルとスロットの呼び出しには、サーバーが把握している名前を使います。ルートとその `model_info` は `model_list` から取得されます。`litellm --model ...` だけで起動したプロキシには、どちらもないため、フックは何もしません。ワイルドカードのエントリー1つが、その代わりになります。

**ルーターモード**（1つのアドレスの背後に多数のモデルがあり、必要に応じて読み込まれる場合）の `llama-server` も動作します。各モデルは独自のスロットを持つ独立したインスタンスなので、フックはサーバー、モデル、スロットごとに状態を保持し、スロット呼び出しのボディにモデル名を含めます（クライアントが使った名前で、LiteLLM の `openai/` は除きます。`model` の設定を使うと、ルート名がモデル名と異なる場合に別の名前を指定できます）。これには2026年2月以降の llama.cpp が必要です。それより前のバージョンは `/slots` をインスタンスに転送せず、`400 Invalid action` と応答していました。

ルーターが一部のモデルしか保持できない場合（既定は `--models-max` 件）、別のモデルを要求すると、最も長く使われていなかったモデルがアンロードされ、そのモデルのスロットとその中のキャッシュは失われます。フックも同じ順序に従い（`models_max` を `slot_cache` で、または `LLAMA_MODELS_MAX` を `--models-max` と同じ数値に設定します。0 は無制限です）、ルーターがアンロードする原因となるリクエストの**前に**、そのスロットを保存します。これが機能するには、プロキシがルーターの唯一のクライアントである必要があります。別経路で届いたリクエストは検知されません。保存によって、ルーターがアンロードしたモデルが読み込まれることはありません。

動作としては、セッションのキャッシュは、スロットが別のセッションに必要になったとき（別の種類のリクエスト、別のモデル、またはルーターによるモデルのアンロード）に、ディスクへ書き出されます。リクエストごとに書き出すわけではありません。生成のたびに3GBを書き込むと、サーバーが遅くなるだけです。そのため、例えば生成、注釈、生成という順序の場合、最初の切り替え後にファイルが現れます。注釈の前に生成が保存されるためです。`save_idle_seconds` を `slot_cache`（または `LLAMA_SAVE_IDLE_SECONDS`）に設定すると、その時間リクエストが来なければキャッシュも書き出されるため、作業を止めた後すぐにフォルダーに保存されます。リクエストが来ると待機時間は最初からやり直されます。すでにディスク上にあるキャッシュは、次の切り替えで再び書き込まれません。

フックは、スロットがアイドル状態のときにだけ保存・復元・削除の呼び出しを送ります（`GET /slots`、`is_processing` のエンドポイントで確認します。このエンドポイントがない場合は、すぐに送信されます）。`llama-server` は、リクエストの実行中にそのような呼び出しを保留にし、リクエストが終わるたびに保留中の呼び出しを1件だけ解放します。そのため、保留中の呼び出しの後ろに到着した別のクライアントのリクエストは、そこで無期限に待たされる可能性があります。保存の書き込み中、サーバーは他の処理を行いません。

詳細: 別のセッションからリクエストが届き、スロットにあるセッションと異なる場合、フックはスロットを、そこにあったセッションの名前で保存し、新しいセッションのファイルを復元します（ファイルがまだない場合はスロットを空にします）。そのうえでリクエストを処理させます。ファイルは**ルートごと**に保持されます（`model_name` を参照）。保存された状態は、それを書き込んだモデルにのみ適合するため、ファイル名にはルートとセッションが含まれ、同じサーバーとフォルダーを使っていても、2つのルートが同じファイルを共有することはありません。サーバーは、復元したプロンプトと異なる部分だけを読みます。復元できないキャッシュ、消えてしまったファイル、応答しないサーバーがあっても、プロンプトを読み直すだけです。リクエスト自体がフックのせいで失敗することはありません。

注意事項:

- **サイズ。** ファイルにはスロットのメモリと、その**コンテキストのチェックポイント**の両方が含まれるため、両者を合わせた大きさになります。長いストーリーや大きなモデルでは、数百メガバイトから数ギガバイトになります。実測例: 1万8000トークンのプロンプトを持つ31Bモデルでのファイルサイズは1.3GBから11GBで、最大のものは約1.5GBのキャッシュに加え、約450MBのチェックポイントが約18個含まれていました。サーバーは既定で最大32個のチェックポイントを保持し、切り替えのたびにそのすべてを書き込み、読み込みます。`--ctx-checkpoints N` の `llama-server` で上限を設定すると、ファイルが小さくなり切り替えが短くなりますが、チェックポイントが役立つのは、次のプロンプトが、キャッシュされたものと最初に異なる位置がそのチェックポイントの位置か、その直後である場合だけです。新しいパートや書き直しは、最後のユーザーメッセージの先頭（最新のチェックポイント）で異なりますが、注釈や書き直しは数パート前の部分を変えることがあります。そのため、ストーリーが遡る分だけチェックポイントを保持してください（フックの `prompt:` の行を見ると、各リクエストがどこで異なるかがわかります）。ルート、ユーザー、種類ごとにファイルは1つずつで、その場で上書きされるため、フォルダーはこの大きさに収まります。
- **速度。** 切り替えが、プロンプトを読み直すより速いかを確認してください（フックは保存と復元のたびにサイズと時間を記録します）。上の例は、NVMe ディスクと AMD Radeon AI PRO R9700 を使ったものです。11GBの保存は6秒、2万トークンの7.6GBの復元は8秒で、切り替えは約14秒かかりました。一方、同じ長さのプロンプトを440トークン/秒で読み直すと約45秒かかり、プロンプト処理は長さに応じて増えます。チェックポイントなしでは、同じファイルは約1GBで、切り替えは5秒でした。
- **メモリ。** スロットのチェックポイントは、サーバーの**システムメモリ**に保持されます（GPUのメモリではありません）。最大 `--ctx-checkpoints` 個まで（既定は32個、大きなモデルでは1個数百メガバイト）で、保存や復元のたびにそのすべてを読み書きします。`llama-server` も、手放したスロットのプロンプトを独自のメモリキャッシュ（`--cache-ram`、既定は8GiB）に保持します。フックのファイルも同じ役割を果たすため、メモリが不足しているマシンでは 0 に設定できます。マシンがスワップを始めると、チェックポイントを先にスワップから読み戻す必要があるため、保存と復元が数倍遅くなります。ディスクを見る前に、`free -h` を確認してください。
- **制限。** 制限を設けないと、フォルダーはルート、ユーザー、種類の数に応じて大きくなります。ルートの `slot_cache` で `max_files`（ファイル数）や `max_gib`（合計サイズ）を設定してください（すべてのルートに対しては、プロキシの環境変数で `LLAMA_CACHE_MAX_FILES` と `LLAMA_CACHE_MAX_GIB` を設定します）。すると、フックは保存のたびに、**そのルート**のキャッシュのうち最も長く**使われていなかった**ものを削除します。キャッシュの復元も使用とみなされます。フックは、直前に保存したキャッシュと、これから復元するキャッシュを決して削除しません（そのため、2つのセッションを交互に使うルートは、制限に関係なく少なくとも2つのファイルを保持します）。削除対象はルートのファイルだけなので、他のルートやフォルダー内の他のファイルは安全です。`max_gib` より大きい単一のキャッシュは残されます（ログに警告が出ます）。削除にはフォルダーが必要です。`save_path`（または `LLAMA_SLOT_SAVE_PATH`）に、プロキシから見た `--slot-save-path` フォルダーを設定してください。コンテナでは、`llama-server` を使い、両方のコンテナがマウントするボリュームにします。これがないと何も削除されません。削除されたキャッシュは、そのセッションがプロンプトを1度だけ読み直すことを意味します。制限は任意です。サイズの上限が不要なら、ホスト上の cron ジョブでも同じことができます（例: `find /srv/ai/slots -name '*.bin' -mtime +2 -delete`）。使用中のキャッシュは、そのセッションがスワップアウトされるたびに保存し直されるため、その古さは毎回リセットされます。フックの外から削除されたファイルはフックに影響しません。欠けているファイルは、単にキャッシュミスになります。
- **ルートのモデルを変更した後** は、フォルダー内のそのファイル（名前はルートで始まるもの）を削除してください。別の重みで保存された状態は、新しいモデルには適合しません。
- **切り替えのたびに、保存と復元の両方が発生します。** [自動注釈](books/annotations.md)をオンにしていると、パートの後に注釈と書き直しが続くため、1ターンで3〜4回切り替わることがあります。頼る前に、1ターンの時間を測ってください。
- **プロキシを再起動すると**、どのセッションがスロットにあるかが忘れられます。再起動後の最初のリクエストは、そのファイルをスロットに上書き復元します。スロットにあったものは失われます。
- **フックが知らないうちにルーターがモデルをアンロードした場合**（誤った `models_max`、別のクライアント）、スロットは空なのに、フックは最後のセッションがまだ入っていると考えます。同じセッションのリクエストは、単にプロンプトを読み直します。別のセッションのリクエストは、まず空のスロットをそのセッションのファイルに上書き保存するため、そのキャッシュも失われます。どちらの場合も、プリフィルのコストがかかるだけです。
- `llama-server` は単純な保存や復元をログに記録しません（追加または復元したコンテキストのチェックポイントと、エラーのみを記録します）。実行結果は応答に含まれ、プロキシのログ（`LITELLM_LOG=INFO` を付けて）にもあります。フックが行うすべての呼び出しごとに行が出ます（例: `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`、`… save <file> …: ok (…)`、`… restore … miss (400)`）。セッションの最初のリクエストのミスは正常です（llama-server は "Unable to restore slot, no available space in KV cache or invalid slot save file" と記録します）。本当の問題は警告として出ます。アドレスと一致しない証明書、拒否された API キー、応答しないサーバーなどです。
- **プロンプトの違い。** セッションの2回目以降のリクエストごとに、ログには、そのプロンプトが同じセッションの前回のプロンプトとどこまで同じかを示す行があります（例: `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`）。サーバーがキャッシュを再利用できるのは、そこまでです（メッセージ1つはおよそ3〜4文字で1トークンになります）。2回目のリクエストがほぼすべてを読み直している場合は、この行を確認してください。行が指すメッセージが、開始位置の変わったメッセージです。この行は、2つのプロンプトの最後のメッセージも比較します。そこには変わるもの（設定、パート、タスク）が入っています。ストーリーがパート1つ分伸びた場合、古い最後のメッセージはもう同じ位置にありません。その場合、2つの最後のメッセージが共有する文字数が、例えば設定などがどの程度同じかを示します。
- **何も起きない場合**は、プロキシのログ（`docker logs litellm-proxy 2>&1 | grep slot_cache`）を確認してください。フックが動いていれば、呼び出しごとに行があります。`slot_cache` がないルートへの Marginalia のリクエストは、一度だけ（`not used for <route>: no model_info.slot_cache`）報告されます。`slot_cache` の行がまったくない場合、フックは実行されていません。コンテナに古いファイルか設定が残っています。エディタがファイルを置き換えた後も、コンテナにマウントされた単一のファイル（`./slot_cache.py:/app/slot_cache.py`）は、コンテナ作成時の内容を表示し続けます。新しいファイルを読み込むには `docker compose up -d
  --force-recreate` を使ってください（単なる再起動では読み込まれません）。`docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` は古すぎるファイルに対して 0 を出力します。
- **Docker では**、プロキシと `llama-server` は2つのコンテナになるため、`--slot-save-path` のフォルダーはホスト上のディレクトリとなり、両方のコンテナがマウントします（プロキシはそこに書き込む必要があります。古いファイルを削除し、使用中のファイルの経過時間を更新するためです）。フックのファイルは LiteLLM の設定と同じ場所に置きます。`api_base` には、プロキシが到達できるアドレスを指定します。サーバーが TLS を使う場合は、証明書のホスト名も指定します:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  ルートの `slot_cache` で `save_path: /slots` と `models_max: 1` を設定します。
- フックは `CustomLogger.async_pre_call_deployment_hook` を使います。これは LiteLLM がデプロイメントを選んだ後、リクエストを送る前に呼び出します（バージョン 1.106.5 は実際のルーターとスタンドインのサーバーで確認しました）。お使いのバージョンで確認してください。単一のサーバーを再起動してモデルを切り替えるプロキシ（llama-swap など）はサポートされていません。スロット呼び出しが、読み込まれているモデルに届いてしまうためです。以上はいずれも、実際の `llama-server` ルーターでは実行していません。テストはフックが行う呼び出しを検証するスタンドインを使っています。
<a id="freeing-the-gpu-for-another-model"></a>

## 別のモデルのために GPU を空ける

言語モデルを載せた GPU には、その隣に画像モデルや音声モデルを置く余裕がありません。`llama-server` をルーターモードで使うと、要求に応じてモデルをアンロードする 2 つ目のフックを設定できます。[GPU ロック](administration/gpus.md)（画像または音声サーバーのロック。そのサーバーを起動する前に実行されます）のステップが、それ以外のすべてに使うのと同じキーで、プロキシへのリクエストによってこれを要求します:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

ヘッダー `X-Llama-Unload` を持つリクエストは、フック自身が応答し（HTTP 200、本文は `unloaded: <names>`）、どのモデルにも届きません。フックはルーターのモデル一覧を取得し（`GET /models`）、読み込まれているものをアンロードし（名前を付けて `POST /models/unload`）、ルーターがどのモデルもメモリ上にないと報告するまで待ちます（メモリを占有しているのは `loaded`、`loading`、`unloading` です）。待ち時間は最大 `LLAMA_UNLOAD_WAIT_SECONDS`（120）です。サーバーに接続できない場合、エラーを返す場合、または待機後もモデルが残っている場合は、応答はエラー（HTTP 502）になります。そのため、最初の取得ステップがこのリクエストであるロックは、満杯の GPU 上で別のモデルを起動しません。モデルは自動的に戻ります。次の通常のリクエストでルーターがそれを読み込み直すため、プラグインの *after* リクエストは言語モデルについては空になることがあります。本文のモデル名は、いずれかのルートが受け付けるものであれば十分です（ルート `openai/*` では `openai/` で始まる名前、例えば `openai/unload`）。重要なのはヘッダーです。どのルートも受け付けない名前の場合、フックは LiteLLM が名前を確認する前に動作するため、モデルをアンロードした *後* に 400 で応答します。アンロードは、1 つのモデルではなく、`unload` を持つルートのあるすべてのサーバーに対して行われます。

フック自体は独立したスクリプト `gpu_unload.py` で、`slot_cache.py` の隣に置き、それと連鎖させます。両者は互いに独立しており、`gpu_unload.py` はキャッシュフックなしでも動作します。両方を連鎖させると、`gpu_unload.py` は LiteLLM のコールバックの中からキャッシュフックを見つけ、その `release_server` を呼び出します。これは **まず全スロットのキャッシュを保存し、そこにあったものを忘れさせます**。そのため次のリクエストは、再読み込みされたモデルにそのセッションのキャッシュを復元できます。（この呼び出しがないと、キャッシュフックはセッションがまだスロットにあると思い込み、何も復元しません。その結果、入れ替えのたびにプロンプト全体が読み直されることになります。）

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

Docker では、ファイルはコンテナ内で `slot_cache.py` と同じように配置されている必要があります。`- ./gpu_unload.py:/app/gpu_unload.py:ro` をプロキシの `volumes` に、`slot_cache.py` の行の隣に追加してください。ファイルが存在しない場合、起動時には `ImportError: Could not import proxy_handler_instance from gpu_unload` と表示されます（実際の理由は `FileNotFoundError` か、スクリプト自体のエラーで、ログではその数行上に出ています）。

2 つを `litellm_config.yml` で連鎖させ、ルートに `unload` の印を付けます:

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

上のアドレスへ `POST` で送信する [GPU ステップ](administration/gpus.md) とし、ヘッダー `Authorization: Bearer <key>` と `X-Llama-Unload: all` と例の本文を付けます（ヘッダーは暗号化されて保存されます）。そしてそれを画像・音声サーバーのロックの取得ステップの先頭に置きます。

もう一方のモデルのサーバーも、同じプロキシ経由で制御できます。LiteLLM はパスを別のアドレスへ中継します（`general_settings: pass_through_endpoints`、`path`、`target`、`include_subpath: true`）。プロキシのキーは検査され、クライアントの `Authorization` ヘッダーは**渡されません**。そのため転送先は `headers` に独自のシークレットを持ちます。音声または画像サーバーの隣で、そのコンテナを停止・起動する小さなサービスが、そのような転送先の一例です：[`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)（`POST /<container>/start` はサービスの準備ができた時に、`stop` は終了した時に応答します。その README には compose の例と、音声サーバー用の LiteLLM ルート（`/v1/audio/speech`）があります）。また、このサービスはルーターのモデルを、指定された**もの以外**をアンロードすることもできます（`POST /llama/unload` と `{"keep": ["my-model"]}` を使用）。これにより、Marginalia と同じルーターを使う別のプログラムが、互いを知らなくても、それぞれ自分のモデルだけを保持できます。この呼び出しは、フックのようにスロットのキャッシュを保存はしません。

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

確認した内容: LiteLLM 1.108.3 を、`loaded`、`unloading`、`unloaded` を報告する代替ルーターとともに実行しました。待機処理は、状態が `unloaded` になるまで応答を保留しました。停止しているサーバーは 502 を返しました。両方のフックを連鎖させた場合は、アンロードの前にスロットが保存され、次のリクエストで復元されました。`gpu_unload.py` だけの場合は、他に何も呼び出されませんでした。パススルーは転送先に到達し、クライアントから送られた値の代わりに独自の `headers` の値を追加し、キーのないリクエストを拒否しました。また、LiteLLM の後ろにある実際の `llama-server` ルーター（31B モデル 1 つ）でも実行しました。リクエストは 1〜2 秒後に `unloaded: <model>` を返し、ルーターのログには `stopping model instance` と `exited with status 0` が記録されました（プロセスが終了したため、メモリは解放されています）。何も読み込まれていない状態での 2 回目のリクエストは、すぐに `unloaded: nothing` を返しました。次の通常のリクエストは、ルーターがモデルを読み込み直したため、約 1 分かかりました。お使いの `GET /models` が状態を別の名前で表している場合は、スクリプト内の `IN_MEMORY` を変更してください。
