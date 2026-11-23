---
label: 프롬프트 캐시와 세션 ID
order: 975
icon: database
---

# 프롬프트 캐시와 세션 ID

모델 서버는 방금 읽은 프롬프트(*프롬프트 캐시*)를 보관해 두어, 같은 텍스트로 시작하는 다음 요청이 그 부분을 다시 읽지 않도록 합니다. 긴 이야기에서는 이것이 몇 초 기다리는 것과 몇 분 기다리는 것의 차이입니다. 캐시는 새 프롬프트가 예전 프롬프트와 시작 부분이 같은 만큼만 도움이 됩니다([캐시가 도움이 되지 않을 수 있는 이유](#why-a-cache-may-not-help) 참조). Marginalia는 또한 요청이 **어떤 종류의 작업인지**를 서버에 알릴 수 있어, 서버나 그 앞의 프록시가 종류별로 캐시를 따로 보관할 수 있습니다.

<a id="what-is-sent"></a>

## 전송되는 정보

Marginalia가 어떤 종류의 작업을 위해 보내는 모든 요청에는 다음 HTTP 헤더가 포함됩니다.

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

값은 같은 사용자가 같은 종류의 작업을 할 때 어떤 책에 대한 작업인지와 관계없이 동일하며, 사용자와 종류가 다르면 값도 다릅니다.

| 종류 | 요청 |
|---|---|
| generation | 새 부분, *다시 생성*, *스와이프*: 프롬프트가 이야기를 공유하므로 값도 공유합니다. |
| impersonate | *대신 쓰기*. |
| annotate | *AI로 주석 달기*와 자동 주석; 두 단계 모드의 두 번째 모델은 고유한 값을 가집니다. |
| redraft | *재작성*과 자동 주석의 재작성. |
| summary | 요약. |
| agent | [에이전트](agents.md)의 요청; 실행마다 자체 대화가 늘어나므로 각각 고유한 값을 가집니다. |
| help | 도움말 어시스턴트. |
| plugins | 플러그인마다 고유한 값을 가집니다: *Reviewer*, *Side Query*(탭 제목은 다름), *Lore Generator*, *Arc Outline*, *Converter*, *Manga Generator*. |

어떤 종류에도 속하지 않는 요청(모델 목록 조회, 연결 테스트, 토큰 계산)에는 헤더가 전송되지 않습니다.

값은 사용자와 종류로 만든 키 기반 해시입니다(키는 [데이터 폴더](administration/index.md#the-data-folder)의 `secret.key`에서 가져옵니다). 사용자도 책도 드러나지 않으며, 누가 이 값의 주인인지 알아낼 수 없고, 다른 설치본에서는 같은 사용자라도 다른 값이 나옵니다. 값은 책마다가 아니라 사용자와 종류마다 하나이므로, 서버가 그보다 많은 캐시를 유지할 필요는 없습니다.

모든 제공자가 이 값을 보냅니다. OpenAI 호환 제공자, [Anthropic Claude](extensions/anthropic.md) 및 [Google Gemini](extensions/google-genai.md) 제공자 모두 해당합니다. 이 헤더를 사용하지 않는 서버는 이를 무시합니다. 이를 끌 수 있는 설정은 없습니다.

<a id="why-a-cache-may-not-help"></a>

## 캐시가 도움이 되지 않을 수 있는 이유

서버는 새 프롬프트가 예전 프롬프트와 **맨 처음 토큰부터** 같은 부분의 캐시를 재사용합니다. 처음 달라지는 지점에서 재사용이 끝나고, 그 뒤의 내용은 그대로여도 모두 다시 읽습니다. 요청이 시작될 때 출력되는 `llama-server` 줄을 보십시오.

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep`는 새 요청이 캐시된 프롬프트에서 유지하는 비율입니다. 1에 가까우면 캐시가 사용됩니다. 여기서는 15%만 사용되므로, 캐시가 디스크에서 복원되었든 슬롯에 그대로 남아 있었든, 프롬프트가 앞부분에서 이미 달라진 것입니다.

**슬라이딩 윈도우**를 가진 모델(예: Gemma)과 하이브리드 모델은 캐시의 끝부분을 버리고 중간에서 이어갈 수 없습니다. `llama.cpp` 소스에 따르면, `llama-server`는 새 프롬프트가 캐시된 프롬프트를 정확히 확장하는 경우이거나, 두 프롬프트가 달라지는 지점 이전이나 그 지점에 **컨텍스트 체크포인트**가 있는 경우에만 캐시된 프롬프트에서 이어갈 수 있습니다. 체크포인트는 사용자 메시지의 시작 부분(최소 8192 토큰 간격)과 프롬프트 끝 직전에만 만들어지며, 슬롯 파일에 보관됩니다(INFO 줄 `appended N context checkpoint(s)`와 `restored N context checkpoint(s)`이 그 시점을 알려 줍니다). 사용 가능한 체크포인트가 없으면, `f_keep`가 무엇이라고 하든 프롬프트 전체를 다시 읽으며, 이는 trace 수준에서만 보고됩니다(`-lv 4`: `forcing full prompt re-processing due to lack of cache data`). 그래서 생성, 주석, 재작성의 이야기는 메시지의 사슬로 보내집니다. 각 부분은 사용자 메시지 뒤에 오는 모델의 답변입니다. 마지막 사용자 메시지가 체크포인트가 되고, 바로 거기서 다음 요청이 달라지기 시작하며, 새 부분은 끝에 메시지만 추가합니다. 로그에서 차가운 프리필을 읽을 때, 첫 `prompt processing, n_tokens = 4096` 줄은 여기서 약 7초 뒤에 나옵니다. 접두사가 재사용되었다면 1~2초 뒤에 나왔을 것입니다.

[프롬프트](books/prompts.md#how-the-prompt-is-put-together)는 **시스템 메시지**로 시작하고 이야기는 그 뒤에 오므로, 시스템 메시지의 내용은 요청마다 같아야 합니다. 기본 마스터 템플릿에서 시스템 메시지는 다음 순서로 로어 블록, 요약, 작가 메모(주석)를 담습니다. 이 중 하나라도 두 요청 사이에 바뀌면(흔히 있는 일입니다. 새 텍스트가 다른 로어 항목을 활성화하거나, 요약이 추가되거나, 메모가 추가 또는 해결되는 경우) 캐시는 그 지점까지만 유효하며, 그 뒤의 이야기 전체를 다시 읽습니다. 이야기가 프롬프트에 더 들어가지 않으면 가장 오래된 부분부터 빠지므로, 매 턴마다 이야기의 시작 부분이 한 부분씩 밀려납니다. 도움이 되는 방법은 다음과 같습니다.

- 부분마다 바뀌는 항목에는 삽입 방식을 **사용자 프롬프트 앞**으로 설정하십시오([순서와 삽입](lorebooks/entries.md#order-and-insertion)). 그러면 항목이 이야기 뒤, 프롬프트 끝으로 가서 캐시를 흔들지 않습니다. 언제나 참인 내용만 로어 블록에 두십시오.
- 사용하지 않는 블록은 마스터 템플릿에서 빼 두십시오(예: 모델에게 메모를 주지 않는다면 작가 메모). 그래야 시스템 메시지가 같은 상태로 유지됩니다.
- 부분의 메뉴에서 **프롬프트 비교**를 열어 캐시가 어디서 끝나는지 확인하십시오. 이 기능은 해당 부분의 프롬프트를 이전 부분의 프롬프트와 비교하고(기본값은 가장 가까운 부분이며, 상자에는 그 앞의 부분과 스와이프가 나열됩니다) 처음 달라지는 메시지를 표시합니다. [프롬프트 비교](#compare-prompt)를 참조하십시오.

<a id="compare-prompt"></a>

### 프롬프트 비교

부분의 메뉴(데스크톱과 휴대폰)에 있는 **프롬프트 비교**는 *이 부분에 캐시가 사용되지 않은 이유는 무엇인가?*라는 질문에 답합니다. 두 프롬프트의 메시지(시스템 메시지, 그다음 부분마다 사용자 메시지와 모델 답변, 그리고 그 턴의 사용자 메시지)를 나열하며, 각 메시지에는 상태가 표시됩니다. 같음, 변경됨, 추가됨, 삭제됨 중 하나입니다. 처음 달라지는 메시지는 강조되고, 그 뒤의 메시지는 서버가 처음 차이 이후를 모두 다시 읽기 때문에 *다시 읽기*로 표시됩니다. 표 위에는 다음 정보가 나옵니다.

- 처음 차이가 어디인지(메시지와 글자 위치)와 프롬프트의 어느 정도가 처음부터 같은지(글자 수와 대략적인 토큰 수로 표시).
- 차이에 대한 해석: **시스템 메시지**가 다르다(탈옥 프롬프트, 템플릿, 로어, 요약 또는 메모가 두 요청 사이에 바뀌었음: 보통의 원인), **이야기 중간의** 부분이 다르다(편집, 재생성 또는 요약으로 대체됨), **이야기의 시작 부분이 밀렸다**(가장 오래된 부분이 빠지거나 요약됨), 또는 차이가 **이전 프롬프트의 마지막 메시지뿐**이다. 이 메시지는 그 턴의 사용자 메시지로, 이야기에는 반복되지 않습니다. 마지막 경우가 다음 부분이나 스와이프가 재사용할 수 있는 최대치입니다. 그래도 서버가 캐시를 사용하지 않았다면 원인은 프롬프트가 아닙니다(슬롯, 모델, 세션 ID를 확인하십시오).

메시지를 선택하면 처음 차이 직전부터 두 텍스트가 나란히 표시됩니다. 메시지만 비교되며, 모델, 요청의 도구, 서버 슬롯은 저장된 프롬프트에 포함되지 않습니다. 부분에는 저장된 프롬프트가 있어야 합니다(작가가 직접 입력한 부분과 프롬프트를 저장하기 전에 생성된 부분에는 없습니다).

아래의 요청 종류는 종류를 전환할 때 무엇이 보관되는지와 관련이 있습니다. 어떤 프롬프트를 사용하는지에 따라 그중 얼마나 쓰이는지가 결정됩니다.
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## 사용법: llama.cpp에서 디스크를 활용해 슬롯 하나로 운영하기

이 방법은 서버의 캐시 메모리가 부족할 때 필요합니다. 로컬 `llama-server`에 여러 슬롯을 둘 만큼 메모리가 있다면, 작업 종류마다 캐시를 하나씩 유지할 수 있으므로 따로 설정할 것이 없습니다. 슬롯을 **하나**만 둘 만큼 메모리가 있다면, 다른 종류의 작업이 올 때마다 이야기의 캐시가 버려집니다. 즉 파트의 주석 달기, 그 뒤의 재작성, 다음 생성 작업이 매번 프롬프트를 처음부터 다시 읽어야 합니다.

해결책은 작업 종류별 캐시를 모두 디스크에 보관해 두었다가, 그 종류의 요청이 도착하면 해당 캐시를 슬롯 하나에 불러오는 것입니다. `llama-server`는 슬롯을 파일에 저장하고 복원할 수 있으며, 앞단의 프록시는 헤더를 보고 요청이 어떤 종류인지 알 수 있습니다. 아래 예시는 [LiteLLM](https://docs.litellm.ai/)에 붙이는 훅입니다.

**요구 사항:** 2026-10-08 이후에 빌드된 `llama-server`(llama.cpp PR #26004, *preserve context checkpoints across slot save/restore*)이어야 합니다. 이전 버전은 슬롯의 메모리는 저장하고 복원하지만 [컨텍스트 체크포인트](#why-a-cache-may-not-help)는 저장하지 않으므로, 슬라이딩 윈도우 모델(예: Gemma)이나 하이브리드 모델은 복원할 때마다 바뀌지 않은 부분까지 포함해 프롬프트 전체를 다시 읽습니다. `llama-server --version`로 빌드 버전을 확인하세요. 움직이는 태그(`full`, `server` 등)를 쓰는 컨테이너 이미지는 직접 pull하기 전까지 갱신되지 않습니다.

슬롯 하나와 파일을 저장할 폴더를 지정해 `llama-server`를 시작합니다:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

이 내용을 LiteLLM 설정 파일 옆에 `slot_cache.py`라는 이름으로 저장합니다:

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

그리고 LiteLLM `config.yaml`에서 이를 켭니다. 콜백을 등록하고, `llama-server`로 가는 라우트의 `model_info`에 `slot_cache`를 설정합니다. 와일드카드 항목 하나면 충분하며, LiteLLM에 모델을 하나하나 나열할 필요는 없습니다(모델 목록은 서버가 알고 있고, 클라이언트가 원하는 모델을 지정합니다):

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

훅은 자체 주소가 필요 없습니다. 요청이 가는 라우트의 `api_base`와 `api_key`를 사용합니다(슬롯은 서버의 루트에 있으므로 끝에 붙은 `/v1`는 제거됩니다). `slot_cache`가 붙은 라우트만 처리됩니다. 다른 모델(예: 클라우드 모델)에 대한 요청은 헤더가 있어도 그대로 통과합니다. 여러 서버를 나란히 운용할 수 있으며, 각 서버는 자체 슬롯을 갖습니다. 와일드카드를 쓰면 라우트는 클라이언트가 요청한 모델이 되므로, 파일은 모델별로(`qwen3-32b`) 보관됩니다. 다른 설정이 필요한 모델은 와일드카드 옆에 이름으로 따로 나열할 수 있습니다.

LiteLLM은 모델 이름에서 `/` 앞부분을 제공자로 해석합니다. 따라서 그런 기호를 포함한 이름(예: 라우터 프리셋의 `org/model`)에는 라우트 `model_name: "openai/*"`를 사용하고, Marginalia에는 모델을 `openai/org/model`로 지정하세요. 훅이 `openai/`를 제거하며, 파일과 슬롯 호출에는 서버가 아는 이름이 사용됩니다. 라우트와 그 `model_info`는 `model_list`에서 가져옵니다. 설정 파일 없이(`litellm --model ...`만으로) 시작한 프록시에는 둘 다 없으며, 이 경우 훅은 아무 작업도 하지 않습니다. 와일드카드 항목 하나가 그 대체 수단입니다.

**`llama-server` 라우터 모드**(한 주소 뒤에 여러 모델이 있고 필요할 때 적재되는 구성)에서도 동작합니다. 모델마다 자체 슬롯을 가진 독립된 인스턴스가 있으므로, 훅은 서버, 모델, 슬롯별로 상태를 관리하고 슬롯 호출의 본문에 모델 이름을 넣습니다(클라이언트가 사용한 이름이며, LiteLLM의 `openai/` 접두사는 뺍니다. 라우트 이름이 모델 이름과 다른 경우에는 `model` 설정으로 다른 이름을 지정할 수 있습니다). 이를 위해서는 2026년 2월 이후의 llama.cpp가 필요합니다. 이 버전부터 `/slots`를 인스턴스로 전달하며, 이전 버전은 `400 Invalid action`로 응답했습니다.

라우터가 모델 중 일부만 적재할 수 있을 때(`--models-max`, 기본값 4), 다른 모델을 요청하면 라우터는 가장 오래전에 쓰인 모델을 내리고, 그 모델의 슬롯과 그 안의 캐시는 사라집니다. 훅은 같은 순서를 따릅니다. `models_max`를 `slot_cache`(또는 `LLAMA_MODELS_MAX`)에서 `--models-max`와 같은 값으로 설정하세요(0은 제한 없음). 그리고 라우터가 그 모델을 내리게 만드는 요청 **이전에** 해당 슬롯을 저장합니다. 이를 위해 프록시가 라우터의 유일한 클라이언트여야 합니다. 다른 경로로 라우터에 도달한 요청은 훅이 알 수 없습니다. 저장 작업이 라우터가 내린 모델을 다시 적재하는 일은 없습니다.

동작 방식은 다음과 같습니다. 세션의 캐시는 **그 슬롯이 다른 세션에 필요해질 때**(다른 종류의 요청, 다른 모델, 또는 라우터가 모델을 내릴 때) 디스크에 기록되며, 요청마다 기록하지는 않습니다. 생성 후마다 3 GB를 쓰면 서버만 느려지기 때문입니다. 따라서 파일은 첫 전환 이후에 생깁니다. 예를 들어 생성, 주석, 생성 순서라면 주석 작업이 먼저 생성 작업의 캐시를 저장합니다. `save_idle_seconds`를 `slot_cache`(또는 `LLAMA_SAVE_IDLE_SECONDS`)에 설정하면, 그 시간 동안 요청이 없을 때도 캐시가 기록되므로 작업을 멈춘 뒤 곧 폴더에 파일이 생깁니다. 요청이 오면 대기 시간은 다시 시작되며, 이미 디스크에 있는 캐시는 다음 전환 때 다시 기록되지 않습니다. 훅은 슬롯이 유휴 상태일 때만 저장, 복원, 삭제 호출을 보냅니다(`GET /slots`, `is_processing`를 확인하며, 해당 엔드포인트가 없으면 즉시 보냅니다). `llama-server`는 요청이 실행되는 동안 이런 호출을 보류하고, 요청이 끝날 때마다 보류된 호출 하나만 풀어 줍니다. 따라서 그 뒤에 도착한 다른 클라이언트의 요청이 그 자리에서 영원히 기다릴 수 있습니다. 저장이 기록되는 동안 서버는 다른 일을 하지 않습니다.

세부 사항은 이렇습니다. 슬롯에 있던 세션과 다른 세션의 요청이 들어오면, 훅은 슬롯의 내용을 그 세션 이름으로 저장하고, 새 세션의 파일을 복원한 뒤(파일이 아직 없으면 슬롯을 비운 뒤) 요청을 계속 진행시킵니다. 파일은 **라우트별로** 보관됩니다(`model_name`). 저장된 상태는 그것을 만든 모델에만 맞으므로, 파일 이름에는 라우트와 세션이 들어가며, 같은 서버와 폴더를 쓰더라도 두 라우트가 파일을 공유하는 일은 없습니다. 서버는 복원된 프롬프트와 다른 부분만 읽습니다. 복원할 수 없는 캐시, 사라진 파일, 응답하지 않는 서버는 프롬프트를 다시 읽게 만들 뿐이며, 요청 자체가 훅 때문에 실패하는 일은 없습니다.

알아두어야 할 점:

- **크기.** 파일에는 슬롯의 메모리와 **그 컨텍스트 체크포인트**가 함께 담기므로, 두 가지를 합친 만큼 커집니다. 긴 이야기와 큰 모델이라면 수백 MB에서 수 GB에 이릅니다. 측정 예: 프롬프트가 18,000 토큰인 31B 모델은 1.3~11 GB 파일을 만들었고, 가장 큰 파일은 약 1.5 GB의 캐시 외에 약 450 MB짜리 체크포인트를 약 18개 담고 있었습니다. 서버는 기본적으로 체크포인트를 최대 32개까지 보관하며, 전환할 때마다 이들을 모두 쓰고 읽습니다. `--ctx-checkpoints N`을 `llama-server`로 설정하면 이 수를 제한할 수 있습니다. 파일이 작아지고 전환이 짧아지지만, 체크포인트는 다음 프롬프트가 캐시와 처음 달라지는 지점이 그 체크포인트이거나 그 바로 뒤일 때만 도움이 됩니다. 새 파트나 스와이프는 마지막 사용자 메시지의 시작 부분(가장 최근 체크포인트)에서 달라지는 반면, 주석이나 재작성은 여러 파트 앞의 내용을 바꿀 수 있습니다. 따라서 이야기가 거슬러 올라가는 만큼 체크포인트를 충분히 유지하세요(훅의 `prompt:` 줄을 보면 각 요청이 어디서 달라지는지 알 수 있습니다). 파일은 라우트, 사용자, 종류마다 하나씩이며 제자리에서 덮어쓰이므로, 폴더는 그만큼만 작게 유지됩니다.
- **속도.** 전환이 프롬프트를 읽는 것보다 빠른지 확인하세요(훅은 저장과 복원마다 크기와 시간을 기록합니다). 위 예시는 NVMe 디스크와 AMD Radeon AI PRO R9700 환경의 측정입니다. 11 GB 저장에 6초, 20,000 토큰에 해당하는 7.6 GB 복원에 8초가 걸려 전환에 약 14초가 소요되었습니다. 반면 같은 길이의 프롬프트를 440 토큰/초로 다시 읽으면 약 45초가 걸리며, 프리필 시간은 프롬프트가 길어질수록 늘어납니다. 체크포인트가 없으면 같은 파일은 약 1 GB였고 전환은 5초가 걸렸습니다.
- **메모리.** 슬롯의 체크포인트는 서버의 **시스템 메모리**(GPU 메모리가 아님)에 보관되며, 최대 `--ctx-checkpoints`개까지 보관할 수 있습니다(기본 32개, 큰 모델에서는 각각 수백 MB). 저장이나 복원은 이들을 모두 읽거나 씁니다. `llama-server`는 자신이 내보낸 슬롯의 프롬프트를 별도의 메모리 캐시(`--cache-ram`, 기본 8 GiB)에도 보관합니다. 훅의 파일이 같은 역할을 하므로, 메모리가 부족한 기기에서는 이 값을 0으로 설정할 수 있습니다. 기기가 스왑을 쓰기 시작하면, 체크포인트를 먼저 스왑에서 다시 읽어야 하므로 저장과 복원이 여러 배 느려집니다. 디스크를 보기 전에 `free -h`을 먼저 확인하세요.
- **제한.** 제한이 없으면 폴더는 라우트, 사용자, 종류의 수에 따라 계속 커집니다. 라우트의 `slot_cache`에서 `max_files`(파일 개수)이나 `max_gib`(전체 크기)을 설정하세요(모든 라우트에 적용하려면 프록시 환경에서 `LLAMA_CACHE_MAX_FILES`와 `LLAMA_CACHE_MAX_GIB`을 설정합니다). 그러면 훅은 저장할 때마다 그 라우트의 캐시 중 **가장 오래전에 사용된** 것을 삭제합니다. 캐시를 복원하는 것도 사용으로 칩니다. 훅은 방금 저장한 캐시나 곧 복원할 캐시는 절대 삭제하지 않습니다(따라서 한 라우트에서 두 세션이 번갈아 쓰이면 제한과 관계없이 최소 파일 두 개가 남습니다). 또한 해당 라우트의 파일만 건드리므로, 다른 라우트나 폴더의 다른 파일은 안전합니다. `max_gib`보다 큰 캐시 하나는 그대로 남습니다(로그에 경고가 기록됩니다). 삭제하려면 폴더 정보가 필요합니다. 프록시에서 보이는 `--slot-save-path` 폴더를 `save_path`(또는 `LLAMA_SLOT_SAVE_PATH`)로 설정하세요. 컨테이너에서 `llama-server`를 쓴다면 두 컨테이너가 모두 마운트하는 볼륨이어야 합니다. 이 설정이 없으면 아무것도 삭제되지 않습니다. 삭제된 캐시는 해당 세션이 프롬프트를 한 번 다시 읽는다는 뜻일 뿐입니다. 제한은 선택 사항입니다. 크기 상한이 필요 없다면 호스트의 cron 작업으로도 같은 효과를 낼 수 있습니다. 예를 들어 `find /srv/ai/slots -name '*.bin' -mtime +2 -delete` 같은 방식입니다(사용 중인 캐시는 세션이 전환되어 빠질 때마다 다시 저장되므로, 그 나이는 계속 새로 시작됩니다). 훅은 외부에서 삭제된 파일에 영향을 받지 않으며, 없는 파일은 캐시 미스로 처리됩니다.
- **라우트의 모델을 바꾼 뒤에는** 폴더에서 그 라우트의 파일(이름이 라우트로 시작하는 파일)을 삭제하세요. 다른 가중치로 저장된 상태는 새 모델에 맞지 않습니다.
- **전환마다 저장과 복원의 비용이 듭니다.** [자동 주석](books/annotations.md)이 켜져 있으면 파트 뒤에 주석과 재작성이 이어지므로, 한 턴에 세네 번 전환될 수 있습니다. 의존하기 전에 턴 단위로 측정해 보세요.
- **프록시를 재시작하면** 슬롯에 어떤 세션이 있었는지 잊어버립니다. 재시작 후 첫 요청은 그 세션의 파일을 슬롯 위에 복원하며, 슬롯에 있던 내용은 사라집니다.
- **훅이 모르는 사이에 라우터가 내린 모델**(잘못된 `models_max`, 다른 클라이언트)은 슬롯이 비어 있는데도 훅은 마지막 세션이 여전히 들어 있다고 판단합니다. 같은 세션의 요청은 단순히 프롬프트를 다시 읽습니다. 다른 세션의 요청이면 비어 있는 슬롯을 그 세션의 파일 위에 먼저 저장하므로, 그 세션의 캐시도 사라집니다. 두 경우 모두 프리필 비용만 발생합니다.
- **`llama-server`는 단순한 저장이나 복원은 기록하지 않습니다**(추가하거나 복원한 컨텍스트 체크포인트와 오류만 기록합니다). 실제 동작은 응답에 담겨 있으며, 프록시의 로그(`LITELLM_LOG=INFO` 설정 시)에도 남습니다. 훅이 보내는 호출마다 줄이 기록되며, 예를 들어 `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`, `… save <file> …: ok (…)`, `… restore … miss (400)` 같은 줄이 있습니다. 세션의 첫 요청에서 캐시 미스가 나는 것은 정상입니다(llama-server는 "Unable to restore slot, no available space in KV cache or invalid slot save file"라고 기록합니다). 실제 문제는 경고로 표시됩니다. 주소와 맞지 않는 인증서, 거부된 API 키, 응답하지 않는 서버 등이 그렇습니다.
- **프롬프트가 달라지는 지점.** 세션의 첫 요청 이후 모든 요청마다 로그에는 그 세션의 직전 프롬프트와 얼마나 같은지를 알려 주는 줄이 있습니다(예: `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`). 서버는 그 지점까지만 캐시를 재사용할 수 있으므로(메시지 하나는 토큰당 약 3~4자), 두 번째 요청이 거의 전부 다시 읽는 것처럼 보일 때 이 줄을 보세요. 줄에 명시된 메시지가 시작 부분이 바뀐 메시지입니다. 이 줄은 또 두 프롬프트의 마지막 메시지도 비교하는데, 여기에는 바뀌는 내용(설정 정보, 파트, 작업 지시)이 들어 있습니다. 이야기에 파트가 추가되면 이전의 마지막 메시지는 더 이상 같은 자리에 있지 않으며, 두 마지막 메시지가 공유하는 글자 수가 곧, 예컨대 설정 정보가 두 프롬프트에서 얼마나 같은지를 보여 줍니다.
- **아무 일도 일어나지 않으면** 프록시의 로그(`docker logs litellm-proxy 2>&1 | grep slot_cache`)를 확인하세요. 훅이 작동 중이라면 호출마다 줄이 있고, `slot_cache`가 없는 라우트에 대한 Marginalia의 요청은 한 번 보고됩니다(`not used for <route>: no model_info.slot_cache`). 훅이 실행되지 않았다면 `slot_cache` 줄이 전혀 없습니다. 이때는 컨테이너에 여전히 이전 파일이나 설정이 있는 것입니다. 컨테이너에 단일 파일로 마운트된 경우(`./slot_cache.py:/app/slot_cache.py`), 편집기가 파일을 교체하면 컨테이너가 생성될 때의 버전이 계속 보입니다. `docker compose up -d
  --force-recreate`를 실행하면 새 파일을 읽어 들입니다(단순 재시작으로는 안 됩니다). 너무 오래된 파일이면 `docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py`는 0을 출력합니다.
- **Docker에서는** 프록시와 `llama-server`가 별도의 두 컨테이너이므로, `--slot-save-path`의 폴더는 두 컨테이너가 모두 마운트하는 호스트의 디렉터리여야 합니다(프록시가 여기에 써야 하므로, 오래된 파일을 지우고 사용된 파일의 나이를 갱신합니다). 훅 파일은 LiteLLM 설정 옆에 두며, `api_base`는 프록시가 접근할 수 있는 주소여야 합니다. 서버가 TLS를 쓴다면 인증서의 호스트 이름도 함께 지정하세요:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  그리고 라우트의 `slot_cache`에 `save_path: /slots`와 `models_max: 1`를 설정합니다.
- 훅은 `CustomLogger.async_pre_call_deployment_hook`를 사용합니다. LiteLLM은 배포를 선택한 뒤 요청을 보내기 전에 이를 호출합니다(버전 1.106.5는 실제 라우터와 대역 서버로 확인했습니다). 실행 중인 버전에서 확인하세요. 한 주소 뒤에서 단일 서버를 재시작하며 모델을 바꾸는 프록시(llama-swap)는 지원되지 않습니다. 슬롯 호출이 적재된 모델이 무엇이든 그쪽으로 가기 때문입니다. 이 모든 것은 실제 `llama-server` 라우터에서 실행된 적이 없으며, 테스트는 훅이 보내는 호출을 검사하는 대역 서버를 사용했습니다.
<a id="freeing-the-gpu-for-another-model"></a>

## GPU 해제: 다른 모델을 위해 GPU 비우기

언어 모델이 GPU를 차지하고 있으면, 그 옆에 이미지 모델이나 음성 모델을 올릴 공간이 없습니다. `llama-server`를 라우터 모드로 쓰면, 두 번째 훅이 요청에 따라 모델을 언로드할 수 있습니다. [GPU 잠금](administration/gpus.md)(이미지 또는 음성 서버의 잠금이며, 해당 서버를 시작하기 전에 실행됨)의 한 단계가 같은 키를 사용하여 프록시에 요청을 보내 이를 요청합니다:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

`X-Llama-Unload` 헤더가 있는 요청은 훅 자체가 응답하며(HTTP 200, 텍스트 `unloaded: <names>`), 어떤 모델에도 전달되지 않습니다. 훅은 라우터의 모델 목록을 가져오고(`GET /models`), 메모리에 올라 있는 모델을 언로드하며(이름을 넣은 `POST /models/unload`), 라우터가 메모리에 모델이 더 이상 없다고 보고할 때까지 기다립니다(`loaded`, `loading`, `unloading`가 메모리 상태를 나타냄). 대기 시간은 최대 `LLAMA_UNLOAD_WAIT_SECONDS`(120)초입니다. 서버에 연결할 수 없거나, 오류가 응답되거나, 대기 후에도 모델이 남아 있으면 응답은 오류(HTTP 502)가 됩니다. 따라서 이 요청이 첫 번째 획득 단계인 잠금은 가득 찬 GPU에 다른 모델을 시작하지 않습니다.

모델은 자동으로 되돌아옵니다. 다음 일반 요청이 라우터로 하여금 모델을 다시 불러오게 하므로, 언어 모델에 대해서는 플러그인의 *after* 요청이 비어 있을 수 있습니다. 본문의 모델 이름은 라우트가 허용하는 이름이기만 하면 됩니다(라우트 `openai/*`와 함께라면 `openai/`로 시작하는 이름, 예: `openai/unload`). 실제로 중요한 것은 헤더입니다. 어떤 라우트도 허용하지 않는 이름은, 훅이 LiteLLM이 이름을 확인하기 전에 실행되므로, 모델이 언로드된 *후에* 400으로 응답됩니다. 언로드는 한 모델이 아니라 `unload` 라우트를 가진 모든 서버에 전달됩니다.

이 훅은 별도의 스크립트 `gpu_unload.py`이며, `slot_cache.py` 옆에 있고 그와 연결됩니다. 두 훅은 서로 독립적입니다. `gpu_unload.py`는 캐시 훅 없이도 동작합니다. 둘이 함께 연결되면, `gpu_unload.py`는 LiteLLM의 콜백 중에서 캐시 훅을 찾아 그 `release_server`를 호출합니다. 이 함수는 **먼저 모든 슬롯의 캐시를 저장하고 그 안에 있던 내용을 잊어버립니다**. 그래서 다음 요청이 다시 로드된 모델에 세션의 캐시를 복원합니다. (이 호출이 없으면 캐시 훅은 세션이 아직 슬롯에 있다고 믿어 아무것도 복원하지 않으며, 모델을 교체할 때마다 전체 프롬프트를 다시 읽게 됩니다.)

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

Docker에서는 파일이 `slot_cache.py`처럼 컨테이너 안에 있어야 합니다. 이를 위해 프록시의 `volumes`에 `- ./gpu_unload.py:/app/gpu_unload.py:ro`를 추가하고, `slot_cache.py` 줄 옆에 둡니다. 시작할 때 파일이 없으면 `ImportError: Could not import proxy_handler_instance from gpu_unload`로 표시됩니다(실제 원인, `FileNotFoundError` 또는 스크립트 자체의 오류는 로그에서 그 몇 줄 위에 있습니다).

두 훅을 `litellm_config.yml`에서 연결하고 라우트에 `unload`를 표시합니다:

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

`POST` 위의 주소로 `Authorization: Bearer <key>` 및 `X-Llama-Unload: all` 헤더와 예시의 본문을 보내는 [GPU 단계](administration/gpus.md)를 만들고(헤더는 암호화되어 저장됨), 이를 이미지 및 음성 서버 잠금의 획득 단계 맨 앞에 두십시오.

다른 모델의 서버도 같은 프록시를 통해 제어할 수 있습니다. LiteLLM은 경로를 다른 주소로 중계합니다(`general_settings: pass_through_endpoints`, `path`, `target`, `include_subpath: true`. 프록시의 키는 검사되며, 클라이언트의 `Authorization` 헤더는 **전달되지 않습니다**. 따라서 대상은 `headers`에 자신만의 비밀 값을 가집니다). 음성 또는 이미지 서버 옆에서 컨테이너를 멈추고 시작하는 작은 서비스가 이런 대상의 예입니다: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)(`POST /<container>/start`은 그 안의 서비스가 준비되었을 때, `stop`은 종료되었을 때 응답합니다. README에는 compose 예시와 음성 서버용 LiteLLM 라우트가 있습니다, `/v1/audio/speech`).

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

확인한 내용: LiteLLM 1.108.3을 `loaded`, `unloading`, `unloaded`를 보고하는 대역 라우터와 함께 실행했습니다(대기 중에는 상태가 `unloaded`가 될 때까지 응답을 보류함, 다운된 서버는 502를 반환함, 두 훅을 연결했을 때 언로드 전에 슬롯이 저장되고 다음 요청에서 복원됨, `gpu_unload.py`만 있을 때는 다른 것이 호출되지 않음, 전달 경로는 대상에 도달했고 클라이언트가 보낸 값 대신 자체 `headers` 값을 추가했으며 키가 없는 요청은 거부함). 또한 LiteLLM 뒤의 실제 `llama-server` 라우터(31B 모델 하나)에서도 실행했습니다. 요청은 1~2초 후 `unloaded: <model>`를 반환했고, 라우터는 `stopping model instance`와 `exited with status 0`를 기록했습니다(프로세스가 사라져 메모리가 비었음). 아무것도 로드되지 않은 상태에서 두 번째 요청은 즉시 `unloaded: nothing`를 반환했으며, 다음 일반 요청은 라우터가 모델을 다시 로드하느라 약 1분이 걸렸습니다. 사용 중인 `GET /models`가 상태를 다르게 명명한다면, 스크립트의 `IN_MEMORY`를 바꾸십시오.
