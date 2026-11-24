---
label: Prompt-Cache und Sitzungs-ID
order: 975
icon: database
---

# Prompt-Cache und Sitzungs-ID

Modellserver behalten den Prompt, den sie gerade gelesen haben (den *Prompt-Cache*), damit die nächste Anfrage, die mit demselben Text beginnt, ihn nicht noch einmal lesen muss. Bei einer langen Geschichte ist das der Unterschied zwischen wenigen Sekunden und minutenlangem Warten. Ein Cache hilft nur, soweit der neue Prompt so beginnt wie der alte (siehe [Warum ein Cache nicht helfen kann](#why-a-cache-may-not-help)). Marginalia kann dem Server außerdem mitteilen, **um welche Art von Arbeit es sich bei einer Anfrage handelt**, damit ein Server oder ein Proxy davor für jede Art einen eigenen Cache führen kann.

<a id="what-is-sent"></a>

## Was gesendet wird

Jede Anfrage, die Marginalia für eine Art von Arbeit stellt, trägt den HTTP-Header

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

Der Wert ist für denselben Benutzer und dieselbe Art von Arbeit gleich, unabhängig davon, um welches Buch es geht. Er unterscheidet sich zwischen Benutzern und zwischen Arten.

| Art | Anfragen |
|---|---|
| Generierung | Ein neuer Teil, *Regenerate* und *Swipe*: Ihre Prompts teilen sich die Geschichte und damit den Wert. |
| Impersonate | *Impersonate*. |
| Annotation | *Annotate with AI* und automatische Annotation; das zweite Modell des zweistufigen Modus hat einen eigenen Wert. |
| Redraft | *Redraft* und der Redraft der automatischen Annotation. |
| Zusammenfassung | Zusammenfassungen. |
| Agent | Die Anfragen eines [Agenten](agents.md); jeder Lauf hat einen eigenen Wert, da er seine eigene Konversation aufbaut. |
| Hilfe | Der Hilfe-Assistent. |
| Plugins | Jedes Plugin hat seinen eigenen: *Reviewer*, *Side Query* (dessen Tab-Titel ein anderer ist), *Lore Generator*, *Arc Outline*, *Converter* und *Manga Generator*. |

Anfragen, die zu keiner Art gehören (das Auflisten der Modelle, das Testen der Verbindung, das Zählen von Tokens), senden keinen Header.

Der Wert ist ein Hash mit Schlüssel aus Benutzer und Art (der Schlüssel stammt aus `secret.key` im [Datenordner](administration/index.md#the-data-folder)). Er verrät weder den Benutzer noch das Buch, niemand kann daraus ermitteln, wem er gehört, und eine andere Installation ergibt für denselben Benutzer einen anderen Wert. Es gibt einen Wert pro Benutzer und Art, nicht einen pro Buch, sodass ein Server nie mehr Caches vorhalten muss als das.

Alle Provider senden ihn: die OpenAI-kompatiblen sowie die Provider für [Anthropic Claude](extensions/anthropic.md) und [Google Gemini](extensions/google-genai.md). Ein Server, der den Header nicht verwendet, ignoriert ihn. Es gibt keine Einstellung, um ihn abzuschalten.

<a id="why-a-cache-may-not-help"></a>

## Warum ein Cache nicht helfen kann

Ein Server verwendet den Teil des Caches wieder, in dem der neue Prompt **vom allerersten Token an** mit dem alten übereinstimmt. Der erste Unterschied beendet die Übereinstimmung, und alles danach wird neu gelesen, egal wie viel davon unverändert ist. Schau dir die Zeile `llama-server` an, die ausgegeben wird, wenn eine Anfrage beginnt:

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` ist der Anteil des gecachten Prompts, den die neue Anfrage beibehält. Liegt er nahe bei 1, wird der Cache genutzt. Hier sind es nur 15 %, also unterscheidet sich der Prompt früh vom gecachten, egal ob der Cache von der Festplatte wiederhergestellt wurde oder nie aus dem Slot entfernt wurde.

Modelle mit einem **gleitenden Fenster** (Gemma zum Beispiel) und hybride Modelle können das Ende ihres Caches nicht verwerfen und vom mittleren Stand aus weitermachen. Laut dem Quelltext von `llama.cpp` kann `llama-server` nur von einem gecachten Prompt aus fortfahren, wenn der neue Prompt ihn genau erweitert oder wenn ein **Kontext-Checkpoint** an oder vor der Stelle existiert, an der sich beide unterscheiden. Checkpoints legt er nur am Anfang von Benutzernachrichten an (mindestens 8192 Tokens auseinander) sowie kurz vor dem Ende des Prompts, und er bewahrt sie in der Slot-Datei auf (die INFO-Zeilen `appended N context checkpoint(s)` und `restored N context checkpoint(s)` zeigen, wann das geschieht). Ohne einen brauchbaren Checkpoint liest er den ganzen Prompt neu, unabhängig davon, was `f_keep` sagt, und meldet das nur auf Trace-Ebene (`-lv 4`: `forcing full prompt re-processing due to lack of cache data`). Deshalb wird die Geschichte bei einer Generierung, einer Annotation und einem Redraft als Kette von Nachrichten gesendet, bei der jeder Teil eine Antwort des Modells auf eine Nachricht des Benutzers ist: Die letzte Nachricht des Benutzers wird als Checkpoint gesichert, und genau dort beginnt die nächste Anfrage sich zu unterscheiden. Ein neuer Teil fügt nur Nachrichten am Ende hinzu. Am Log lässt sich ein kalter Prefill so ablesen: Die erste Zeile `prompt processing, n_tokens = 4096` kommt hier nach etwa 7 s; hätte ein Präfix wiederverwendet werden können, käme sie nach einer oder zwei Sekunden.

Der [Prompt](books/prompts.md#how-the-prompt-is-put-together) beginnt mit der **Systemnachricht**, und die Geschichte folgt danach. Deshalb muss der Inhalt der Systemnachricht von einer Anfrage zur nächsten gleich bleiben. In der Standardvorlage des Hauptmusters enthält die Systemnachricht in dieser Reihenfolge den Lore-Block, die Zusammenfassungen und die Autorennotizen (Annotationen). Ändert sich eines davon zwischen zwei Anfragen, was üblich ist (der neue Text aktiviert andere Lore-Einträge, eine Zusammenfassung kommt hinzu, eine Notiz wird hinzugefügt oder erledigt), ist der Cache nur bis zu dieser Stelle gültig, und die ganze dahinterliegende Geschichte wird neu gelesen. Die ältesten Teile verlassen den Prompt zuerst, wenn die Geschichte nicht mehr hineinpasst. Dadurch verschiebt sich bei jeder Runde der Anfang der Geschichte um einen Teil. Hilfreich ist Folgendes:

- Gib den Einträgen, die sich von Teil zu Teil ändern, den Einfügemodus **Before User Prompt** ([Reihenfolge und Einfügen](lorebooks/entries.md#order-and-insertion)). Sie wandern ans Ende des Prompts, hinter die Geschichte, und stören den Cache nicht. Behalte im Lore-Block, was immer gilt.
- Lass die Blöcke, die du nicht brauchst, aus der Hauptvorlage weg (zum Beispiel die Autorennotizen, wenn du dem Modell keine Notizen gibst), damit die Systemnachricht gleich bleibt.
- Öffne **Compare Prompt** im Menü eines Teils, um zu sehen, wo der Cache endet. Der Vergleich stellt den Prompt des Teils dem eines früheren Teils gegenüber (standardmäßig dem nächstgelegenen; das Feld listet die Teile davor und deren Swipes auf) und markiert die erste Nachricht, die sich unterscheidet. Siehe [Compare Prompt](#compare-prompt).

### Compare Prompt

**Compare Prompt** im Menü eines Teils (Desktop und Telefon) beantwortet die Frage *Warum wurde der Cache für diesen Teil nicht verwendet?* Es listet die Nachrichten der beiden Prompts auf (die Systemnachricht, danach je eine Benutzernachricht und eine Modellantwort pro Teil, dann die Benutzernachricht des Zuges) mit einem Status für jede: gleich, geändert, hinzugefügt oder gelöscht. Die erste Nachricht, die sich unterscheidet, wird hervorgehoben, und die danach folgenden werden als *Read again* markiert, weil ein Server alles nach dem ersten Unterschied neu liest. Oberhalb der Tabelle steht:

- wo der erste Unterschied liegt (Nachricht und Zeichen) und wie viel des Prompts vom Anfang an gleich ist, in Zeichen und ungefähr in Tokens;
- eine Deutung des Unterschieds: Die **Systemnachricht** unterscheidet sich (Jailbreak, Vorlage, Lore, Zusammenfassungen oder Notizen haben sich zwischen den beiden Anfragen geändert; das ist die übliche Ursache), ein Teil **in der Mitte der Geschichte** unterscheidet sich (er wurde bearbeitet, neu generiert oder durch eine Zusammenfassung ersetzt), der **Anfang der Geschichte hat sich verschoben** (die ältesten Teile wurden weggelassen oder zusammengefasst), oder der Unterschied betrifft nur die **letzte Nachricht des früheren Prompts**, die Benutzernachricht dieses Zuges, die die Geschichte nicht wiederholt. Der letzte Fall ist das Höchste, was ein nächster Teil oder ein Swipe wiederverwenden kann. Nutzt der Server seinen Cache trotzdem nicht, liegt es nicht am Prompt (prüfe Slot, Modell und Sitzungs-ID).

Wählst du eine Nachricht aus, werden die beiden Texte nebeneinander ab kurz vor dem ersten Unterschied angezeigt. Verglichen werden nur die Nachrichten: Das Modell, die Werkzeuge der Anfrage und der Serverslot sind nicht Teil des gespeicherten Prompts. Der Teil braucht einen gespeicherten Prompt (Teile, die der Autor selbst getippt hat, und Teile, die vor dem Speichern der Prompts erzeugt wurden, haben keinen).

Die folgenden Anfragearten sind wichtig dafür, was beim Wechseln zwischen ihnen erhalten bleibt. Der Prompt entscheidet, wie viel davon genutzt wird.
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## Verwendung: ein Slot auf der Festplatte mit llama.cpp

Dies ist für Sie gedacht, wenn dem Server der Speicher für Caches ausgeht. Ein lokaler `llama-server` mit genug Speicher für mehrere Slots kann für jede Art von Arbeit einen Cache vorhalten und braucht nichts weiter. Mit Speicher für **einen** Slot verwirft jede andere Art von Arbeit den Cache der Geschichte: die Annotation eines Teils, die Neufassung danach und die nächste Generierung lesen ihre Prompts jedes Mal erneut ein.

Der Ausweg ist, den Cache jeder Art auf der Festplatte zu halten und ihn in den einen Slot einzutauschen, sobald eine Anfrage dieser Art eintrifft. `llama-server` kann einen Slot in eine Datei speichern und wiederherstellen; ein Proxy davor erkennt am Header, aus welcher Art eine Anfrage stammt. Das Beispiel unten ist ein Hook für [LiteLLM](https://docs.litellm.ai/).

**Voraussetzung:** ein `llama-server` vom 2026-10-08 oder neuer (llama.cpp PR #26004, *preserve context checkpoints across slot save/restore*). Ein älterer schreibt und stellt den Speicher des Slots wieder her, aber nicht seine [Kontext-Checkpoints](#why-a-cache-may-not-help). Daher liest ein Modell mit gleitendem Fenster (zum Beispiel Gemma) oder ein Hybridmodell nach jedem Wiederherstellen den gesamten Prompt neu, egal wie viel davon unverändert ist. Prüfen Sie den Build mit `llama-server --version`; ein Container-Image mit einem wandernden Tag (`full`, `server` ...) wird erst aktualisiert, wenn Sie es neu ziehen.

Starten Sie `llama-server` mit einem Slot und einem Ordner für die Dateien:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

Speichern Sie dies als `slot_cache.py` neben der LiteLLM-Konfiguration:

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

und schalten Sie es in der LiteLLM-`config.yaml` ein: den Callback und `slot_cache` in der `model_info` der Route, die zum `llama-server` führt. Ein Wildcard-Eintrag genügt; kein Modell muss in LiteLLM aufgeführt werden (der Server kennt sie, und der Client nennt das gewünschte):

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

Der Hook braucht keine eigene Adresse: Er verwendet die `api_base` und `api_key` der Route, zu der die Anfrage geht (die Slots liegen an der Wurzel des Servers, daher wird ein nachgestelltes `/v1` entfernt). Nur die Routen mit `slot_cache` werden berührt; eine Anfrage für ein anderes Modell, etwa ein Cloud-Modell, geht durch, auch wenn sie den Header trägt. Mehrere Server arbeiten nebeneinander, jeder mit seinem eigenen Slot. Mit dem Wildcard-Eintrag ist die Route das Modell, das der Client angefordert hat, daher werden die Dateien pro Modell gehalten (`qwen3-32b`). Daneben können Sie trotzdem einzelne Modelle namentlich aufführen, wenn sie andere Einstellungen brauchen. LiteLLM liest den Teil eines Modellnamens vor einem `/` als Provider. Für Namen, die einen solchen enthalten (`org/model`, wie bei einem Router-Preset), verwenden Sie daher die Route `model_name: "openai/*"` und übergeben Sie Marginalia das Modell als `openai/org/model`: Der Hook entfernt das `openai/`, und die Dateien sowie die Slot-Aufrufe verwenden den Namen, den der Server kennt. Die Routen und ihre `model_info` stammen aus der `model_list`: Ein Proxy, der ohne sie gestartet wird (nur `litellm --model ...`), hat keine davon, und der Hook tut dort nichts. Der eine Wildcard-Eintrag ist der Ersatz.

**`llama-server` im Router-Modus** (viele Modelle hinter einer Adresse, bei Bedarf geladen) funktioniert: Jedes Modell ist eine eigene Instanz mit eigenen Slots, daher hält der Hook den Zustand pro Server, Modell und Slot und nennt das Modell im Body seiner Slot-Aufrufe (der Name, den der Client verwendet hat, ohne LiteLLMs `openai/`; die Einstellung `model` gibt einen anderen an, für eine Route, deren Name nicht das Modell ist). Es braucht ein llama.cpp vom Februar 2026 oder neuer, das `/slots` an die Instanz weiterleitet (vorher antwortete es mit `400 Invalid action`).

Wenn der Router nur einen Teil seiner Modelle halten darf (`--models-max`, standardmäßig 4), entlädt er bei der Anfrage nach einem anderen das zuletzt vor längerer Zeit genutzte Modell, und der Slot dieses Modells mit dem darin liegenden Cache ist verloren. Der Hook folgt derselben Reihenfolge (setzen Sie `models_max` in `slot_cache` oder `LLAMA_MODELS_MAX` auf dieselbe Zahl wie `--models-max`; 0 bedeutet keine Grenze) und speichert diesen Slot **bevor** die Anfrage läuft, die den Router zum Entladen bringt. Dafür muss der Proxy der einzige Client des Routers sein: Eine Anfrage, die auf anderem Weg bei ihm ankommt, wird nicht gesehen. Ein Speichervorgang lädt nie ein Modell, das der Router entladen hat. Was er tut: Der Cache einer Sitzung wird auf die Festplatte geschrieben, **wenn der Slot für eine andere gebraucht wird** (eine Anfrage einer anderen Art, eines anderen Modells oder der Router entlädt das Modell), nicht nach jeder Anfrage; ein Schreibvorgang von 3 GB nach jeder Generierung würde den Server nur ausbremsen. Daher erscheint eine Datei nach dem ersten Wechsel, zum Beispiel bei Generieren, Annotieren, Generieren: Die Annotation speichert zuerst die Generierung. Mit `save_idle_seconds` in der `slot_cache` (oder `LLAMA_SAVE_IDLE_SECONDS`) wird der Cache außerdem geschrieben, wenn so lange keine Anfrage kam, sodass der Ordner ihn bald nach Ihrer Arbeitspause hat. Eine Anfrage beginnt das Warten von vorn, und ein Cache, der schon auf der Festplatte liegt, wird beim nächsten Wechsel nicht erneut geschrieben. Der Hook sendet ein Speichern, Wiederherstellen oder Löschen nur, wenn der Slot untätig ist (er fragt `GET /slots`, `is_processing` ab; ohne diesen Endpunkt sendet er sofort): `llama-server` hält einen solchen Aufruf zurück, während eine Anfrage läuft, und gibt bei jedem Ende einer Anfrage nur einen zurückgehaltenen Aufruf frei. So könnte eine Anfrage eines anderen Clients, die dahinter eingetroffen ist, dort dauerhaft warten. Der Server tut während eines Schreibvorgangs nichts anderes.

Die Einzelheiten: Kommt eine Anfrage aus einer anderen Sitzung als der im Slot, speichert der Hook den Slot unter dem Namen der Sitzung, die darin war, stellt die Datei der neuen wieder her (oder leert den Slot, wenn es noch keine Datei gibt) und lässt die Anfrage weiterlaufen. Die Dateien werden **pro Route** gehalten (`model_name`): Ein gespeicherter Zustand passt nur zu dem Modell, das ihn geschrieben hat. Daher wird die Datei nach Route und Sitzung benannt, und zwei Routen teilen nie eine Datei, auch wenn sie denselben Server und Ordner nutzen. Der Server liest dann nur, was sich vom wiederhergestellten Prompt unterscheidet. Ein Cache, der sich nicht wiederherstellen lässt, eine Datei, die fehlt, oder ein Server, der nicht antwortet, führt nur dazu, dass der Prompt erneut gelesen wird; die Anfrage selbst scheitert nie wegen des Hooks.

Das sollten Sie wissen:

- **Größe.** Eine Datei enthält den Speicher des Slots **und seine Kontext-Checkpoints**, sie ist also so groß wie beides zusammen: bei einer langen Geschichte und einem großen Modell hunderte Megabyte bis mehrere Gigabyte. Ein gemessenes Beispiel: Ein 31-B-Modell mit einem Prompt von 18.000 Token erzeugte Dateien von 1,3 bis 11 GB, die größte mit etwa 18 Checkpoints zu je rund 450 MB neben etwa 1,5 GB Cache. Ein Server hält standardmäßig bis zu 32 Checkpoints, und jeder Tausch schreibt und liest alle davon. `--ctx-checkpoints N` von `llama-server` begrenzt das, was die Dateien kleiner und die Tauschvorgänge kürzer macht. Ein Checkpoint hilft aber nur, wenn sich der nächste Prompt erstmals am oder kurz nach ihm vom gespeicherten unterscheidet. Ein neuer Teil oder ein Swipe unterscheidet sich am Anfang der letzten Benutzernachricht (dem neuesten Checkpoint), während eine Annotation oder eine Neufassung einen Teil mehrere Teile zurück ändern kann. Behalten Sie daher so viele Checkpoints, wie die Geschichte zurückreicht (sehen Sie sich die `prompt:`-Zeilen des Hooks an, sie zeigen, wo sich jede Anfrage unterscheidet). Es gibt eine Datei pro Route, Benutzer und Art, und jede wird an Ort und Stelle überschrieben, daher bleibt der Ordner so klein.
- **Geschwindigkeit.** Prüfen Sie, dass ein Tausch schneller ist als das erneute Lesen des Prompts (der Hook protokolliert Größe und Zeit jedes Speicherns und Wiederherstellens). Das Beispiel oben, auf einer NVMe-Festplatte mit einer AMD Radeon AI PRO R9700: Ein Speichern von 11 GB dauerte 6 s, und ein Wiederherstellen von 7,6 GB für 20.000 Token 8 s, also etwa 14 s pro Tausch. Das erneute Lesen eines Prompts dieser Länge bei 440 Token pro Sekunde dauert dagegen etwa 45 s; ein Prefill wächst mit dem Prompt. Ohne Checkpoints waren dieselben Dateien etwa 1 GB groß, und ein Tausch dauerte 5 s.
- **Speicher.** Die Checkpoints des Slots werden im **Systemspeicher** des Servers gehalten (nicht im Speicher der GPU), bis zu `--ctx-checkpoints` davon (standardmäßig 32, bei einem großen Modell je hunderte Megabyte), und ein Speichern oder Wiederherstellen liest oder schreibt sie alle. `llama-server` hält außerdem die Prompts der Slots, die es aufgegeben hat, in einem eigenen Speicher-Cache (`--cache-ram`, standardmäßig 8 GiB). Die Dateien des Hooks übernehmen dieselbe Aufgabe, daher kann er auf einem Rechner mit knappem Speicher auf 0 gesetzt werden. Beginnt der Rechner auszulagern, werden Speichern und Wiederherstellen um ein Mehrfaches langsamer, weil die Checkpoints zuerst aus dem Auslagerungsspeicher zurückgelesen werden müssen. Sehen Sie sich deshalb `free -h` an, bevor Sie auf die Festplatte schauen.
- **Grenzen.** Ohne Grenze wächst der Ordner mit der Zahl der Routen, Benutzer und Arten. Setzen Sie `max_files` (eine Anzahl von Dateien) und/oder `max_gib` (ihre Gesamtgröße) in der `slot_cache` einer Route (oder `LLAMA_CACHE_MAX_FILES` und `LLAMA_CACHE_MAX_GIB` in der Umgebung des Proxys für alle Routen). Dann löscht der Hook nach jedem Speichern die Caches **dieser Route**, die **zuletzt vor längerer Zeit verwendet** wurden. Das Wiederherstellen eines Caches zählt als Verwendung. Er löscht nie den Cache, den er gerade gespeichert hat, oder den, den er gleich wiederherstellen wird (so behält eine Route mit zwei Sitzungen im Wechsel mindestens zwei Dateien, unabhängig vom Limit). Er berührt nur die Dateien der Route, daher sind andere Routen und andere Dateien im Ordner sicher. Ein einzelner Cache, der größer als `max_gib` ist, bleibt erhalten (mit einer Warnung im Protokoll). Zum Löschen braucht der Hook den Ordner: Setzen Sie `save_path` (oder `LLAMA_SLOT_SAVE_PATH`) auf den Ordner `--slot-save-path`, so wie der Proxy ihn sieht; mit `llama-server` in einem Container ist das ein Volume, das beide Container einbinden. Ohne ihn wird nichts gelöscht. Ein gelöschter Cache bedeutet nur, dass seine Sitzung den Prompt einmal erneut liest. Die Grenzen sind optional: Ein Cron-Job auf dem Host tut dasselbe, wenn Sie keine Obergrenze für die Größe brauchen, zum Beispiel `find /srv/ai/slots -name '*.bin' -mtime +2 -delete` (ein Cache, der in Gebrauch ist, wird jedes Mal neu gespeichert, wenn seine Sitzung ausgelagert wird, daher beginnt sein Alter immer wieder von vorn). Von außen gelöschte Dateien sind für den Hook unschädlich; eine fehlende Datei zählt als Cache-Fehlzugriff.
- **Nach einem Modellwechsel einer Route** löschen Sie deren Dateien im Ordner (ihre Namen beginnen mit der Route): Ein Zustand, der mit anderen Gewichten gespeichert wurde, passt nicht zum neuen Modell.
- **Jeder Wechsel kostet ein Speichern und ein Wiederherstellen.** Ist [automatische Annotation](books/annotations.md) eingeschaltet, folgen auf einen Teil eine Annotation und eine Neufassung, sodass ein Durchgang drei- oder viermal tauschen kann. Messen Sie einen Durchgang, bevor Sie sich darauf verlassen.
- **Ein Neustart des Proxys** vergisst, welche Sitzung im Slot liegt. Die erste Anfrage danach stellt deren Datei über den Slot wieder her, und was der Slot enthielt, ist verloren.
- **Ein Modell, das der Router entlädt, ohne dass der Hook es merkt** (ein falsches `models_max`, ein anderer Client), hat einen leeren Slot, während der Hook noch glaubt, die letzte Sitzung liege darin. Eine Anfrage für dieselbe Sitzung liest ihren Prompt einfach erneut. Eine Anfrage für eine andere speichert zuerst den leeren Slot über die Datei jener Sitzung, daher ist deren Cache ebenfalls verloren. Beides kostet nur einen Prefill.
- **`llama-server` protokolliert ein einfaches Speichern oder Wiederherstellen nicht** (es protokolliert die Kontext-Checkpoints, die es anhängt oder wiederherstellt, und Fehler). Was es getan hat, steht in seiner Antwort. Das Protokoll des Proxys (mit `LITELLM_LOG=INFO`) enthält es ebenfalls, mit einer Zeile für jeden Aufruf, den der Hook macht, zum Beispiel `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`, `… save <file> …: ok (…)` oder `… restore … miss (400)`. Der Fehlzugriff bei der ersten Anfrage einer Sitzung ist normal (llama-server protokolliert ihn als "Unable to restore slot, no available space in KV cache or invalid slot save file"). Echte Probleme sind Warnungen: ein Zertifikat, das nicht zur Adresse passt, ein abgelehnter API-Schlüssel, ein Server, der nicht antwortet.
- **Wo sich der Prompt unterscheidet.** Für jede Anfrage nach der ersten einer Sitzung enthält das Protokoll eine Zeile, die angibt, wie weit der Prompt dem letzten dieser Sitzung gleicht, zum Beispiel `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`. Ein Server kann seinen Cache nur bis dahin wiederverwenden (eine Nachricht ist etwa drei bis vier Zeichen pro Token lang). Diese Zeile sollten Sie daher lesen, wenn eine zweite Anfrage fast alles erneut liest: Die Nachricht, die sie nennt, ist diejenige, deren Anfang sich geändert hat. Die Zeile vergleicht außerdem die letzte Nachricht der beiden Prompts, die enthält, was sich ändert (das Lore, den Teil, die Aufgabe). Wenn die Geschichte um einen Teil gewachsen ist, steht die alte letzte Nachricht nicht mehr an derselben Stelle. Die Zahl der Zeichen, die beide letzten Nachrichten gemeinsam haben, zeigt, wie weit etwa das Lore in beiden gleich ist.
- **Wenn nichts passiert**, sehen Sie sich das Protokoll des Proxys an (`docker logs litellm-proxy 2>&1 | grep slot_cache`). Ist der Hook aktiv, gibt es eine Zeile für jeden Aufruf, und eine Anfrage von Marginalia für eine Route ohne `slot_cache` wird einmal gemeldet (`not used for <route>: no model_info.slot_cache`). Fehlt jede `slot_cache`-Zeile, lief der Hook nicht: Der Container hat noch eine ältere Datei oder Konfiguration. Eine einzelne Datei, die in einen Container eingebunden ist (`./slot_cache.py:/app/slot_cache.py`), zeigt weiterhin die Version, die sie beim Anlegen des Containers hatte, wenn ein Editor die Datei ersetzt hat. `docker compose up -d
  --force-recreate` übernimmt die neue Version (ein einfacher Neustart nicht). `docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` gibt für eine zu alte Datei 0 aus.
- **In Docker** sind der Proxy und `llama-server` zwei Container. Daher ist der Ordner von `--slot-save-path` ein Verzeichnis des Hosts, das beide einbinden (der Proxy muss dort schreiben können: Er löscht alte Dateien und erneuert das Alter der verwendeten). Die Hook-Datei kommt neben die LiteLLM-Konfiguration, und `api_base` ist eine Adresse, die der Proxy erreichen kann, mit dem Hostnamen des Zertifikats, wenn der Server TLS verwendet:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  mit `save_path: /slots` und `models_max: 1` in der `slot_cache` der Route.
- Der Hook verwendet `CustomLogger.async_pre_call_deployment_hook`, das LiteLLM aufruft, nachdem es das Deployment gewählt hat und bevor es die Anfrage sendet (Version 1.106.5 wurde mit einem echten Router und einem Ersatzserver geprüft). Prüfen Sie es anhand der Version, die Sie betreiben. Ein Proxy, der Modelle hinter einer Adresse tauscht, indem er einen einzelnen Server neu startet (llama-swap), wird nicht unterstützt: Die Slot-Aufrufe würden bei dem Modell landen, das gerade geladen ist. Nichts davon wurde gegen einen echten `llama-server`-Router ausgeführt. Die Tests verwenden einen Ersatzserver, der die Aufrufe des Hooks prüft.
<a id="freeing-the-gpu-for-another-model"></a>

## GPU für ein anderes Modell freigeben

Eine GPU, die das Sprachmodell hält, hat neben sich keinen Platz für ein Bild- oder Sprachmodell. Mit `llama-server` im Router-Modus kann ein zweiter Hook die Modelle auf Anfrage entladen: Ein Schritt einer [GPU-Sperre](administration/gpus.md) (der Sperre des Bild- oder Sprachservers, bevor dieser seinen Server startet) fordert dies mit einer Anfrage an den Proxy an, mit demselben Schlüssel, den er auch für alles andere verwendet:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

Eine Anfrage mit dem Header `X-Llama-Unload` beantwortet der Hook selbst (HTTP 200, der Text `unloaded: <names>`), und sie erreicht nie ein Modell. Der Hook listet die Modelle des Routers auf (`GET /models`), entlädt die geladenen (`POST /models/unload` mit dem Namen) und wartet, bis der Router meldet, dass keines mehr im Speicher ist (`loaded`, `loading` und `unloading` halten den Speicher fest), höchstens `LLAMA_UNLOAD_WAIT_SECONDS` (120). Wenn der Server nicht erreichbar ist, mit einem Fehler antwortet oder die Modelle nach dem Warten noch geladen sind, lautet die Antwort Fehler (HTTP 502). So startet eine Sperre, deren erster Acquire-Schritt diese Anfrage ist, das andere Modell nicht auf einer vollen GPU. Das Modell kommt von selbst zurück: Die nächste gewöhnliche Anfrage lässt den Router es wieder laden, daher kann die *After*-Anfrage eines Plugins für das Sprachmodell leer sein.

Der Modellname im Body muss nur einer sein, den eine Route annimmt (mit der Route `openai/*` beginnt er mit `openai/`, zum Beispiel `openai/unload`); entscheidend ist der Header. Ein Name, den keine Route annimmt, wird mit einem 400 beantwortet, *nachdem* die Modelle entladen wurden, weil der Hook läuft, bevor LiteLLM den Namen prüft. Das Entladen betrifft jeden Server, dessen Route `unload` hat, nicht nur ein Modell.

Der Hook ist ein eigenes Skript, `gpu_unload.py`, neben `slot_cache.py` und mit ihm verkettet. Die beiden sind voneinander unabhängig: `gpu_unload.py` funktioniert ohne den Cache-Hook. Sind beide verkettet, findet `gpu_unload.py` den Cache-Hook unter den Callbacks von LiteLLM und ruft dessen `release_server` auf, das **zuerst den Cache jedes Slots speichert und vergisst, was darin war**. So stellt die nächste Anfrage den Cache ihrer Sitzung im neu geladenen Modell wieder her. (Ohne diesen Aufruf würde der Cache-Hook glauben, die Sitzung sei noch im Slot, und nichts wiederherstellen: Der gesamte Prompt würde nach jedem Wechsel erneut gelesen.)

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

In Docker muss die Datei im Container liegen, so wie `slot_cache.py` es ist: Ergänzen Sie `- ./gpu_unload.py:/app/gpu_unload.py:ro` unter `volumes` des Proxys, neben der Zeile von `slot_cache.py`. Eine Datei, die fehlt, zeigt sich beim Start als `ImportError: Could not import proxy_handler_instance from gpu_unload` (der eigentliche Grund, `FileNotFoundError` oder ein Fehler des Skripts selbst, steht einige Zeilen darüber im Log).

Verketten Sie die beiden in `litellm_config.yml` und markieren Sie die Route mit `unload`:

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

Machen Sie daraus einen [GPU-Schritt](administration/gpus.md), der die oben genannte Adresse per `POST` aufruft, mit den Headern `Authorization: Bearer <key>` und `X-Llama-Unload: all` sowie dem Body des Beispiels (die Header werden verschlüsselt gespeichert), und setzen Sie ihn an den Anfang der Acquire-Schritte der Sperren des Bild- und Sprachservers.

Der Server des anderen Modells lässt sich über denselben Proxy steuern. LiteLLM leitet einen Pfad an eine andere Adresse weiter (`general_settings: pass_through_endpoints`, `path`, `target` und `include_subpath: true`; der Schlüssel des Proxys wird geprüft, und der Header `Authorization` des Clients wird **nicht** weitergereicht, sodass das Ziel ein eigenes Geheimnis in `headers` hat). Ein kleiner Dienst neben dem Sprach- oder Bildserver, der dessen Container anhält und wieder startet, ist so ein Ziel: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control) (`POST /<container>/start` antwortet, wenn der Dienst darin bereit ist, `stop`, wenn er beendet wurde; sein README enthält das Compose-Beispiel und die LiteLLM-Route für den Sprachserver, `/v1/audio/speech`). Er kann auch die Modelle eines Routers entladen, **außer** den genannten (`POST /llama/unload` mit `{"keep": ["my-model"]}`). So behalten Marginalia und ein anderes Programm am selben Router jeweils nur ihr eigenes Modell, ohne voneinander zu wissen. Dieser Aufruf speichert die Slot-Caches allerdings nicht, wie es der Hook tut.

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

Geprüft wurde: LiteLLM 1.108.3 mit einem Stellvertreter-Router, der `loaded`, `unloading` und `unloaded` meldet (das Warten hielt die Antwort, bis der Zustand `unloaded` war; ein Server, der nicht lief, ergab 502; mit beiden verketteten Hooks wurde der Slot vor dem Entladen gespeichert und von der nächsten Anfrage wiederhergestellt; mit nur `gpu_unload.py` wurde sonst nichts aufgerufen; und der Durchreicher erreichte sein Ziel, setzte an Stelle eines vom Client gesendeten Wertes seinen eigenen `headers`-Wert ein und wies eine Anfrage ohne Schlüssel ab). Außerdem wurde er gegen einen echten `llama-server`-Router hinter LiteLLM getestet (ein 31B-Modell): Die Anfrage kam nach 1 bis 2 Sekunden mit `unloaded: <model>` zurück, der Router protokollierte `stopping model instance` und `exited with status 0` (der Prozess ist beendet, der Speicher also frei), eine zweite Anfrage bei nichts Geladenem kam sofort mit `unloaded: nothing` zurück, und die nächste gewöhnliche Anfrage dauerte etwa eine Minute, weil der Router das Modell erneut lud. Benennt Ihr `GET /models` die Zustände anders, ändern Sie `IN_MEMORY` im Skript.
