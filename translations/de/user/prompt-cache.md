---
label: Prompt-Cache und Sitzungs-ID
order: 975
icon: database
---

# Prompt-Cache und Sitzungs-ID

Modellserver behalten den Prompt, den sie gerade gelesen haben (den *Prompt-Cache*), damit die nächste Anfrage, die mit demselben Text beginnt, ihn nicht erneut lesen muss. Bei einer langen Geschichte ist das der Unterschied zwischen wenigen Sekunden und mehreren Minuten Wartezeit. Ein Cache hilft nur, soweit der neue Prompt so beginnt wie der alte (siehe [Warum ein Cache nicht helfen kann](#why-a-cache-may-not-help)). Marginalia kann dem Server außerdem mitteilen, **um welche Art von Arbeit es sich bei einer Anfrage handelt**, damit ein Server oder ein Proxy davor für jede Art einen eigenen Cache führen kann.

<a id="what-is-sent"></a>

## Was gesendet wird

Jede Anfrage, die Marginalia für eine Art von Arbeit stellt, trägt den HTTP-Header

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

Der Wert ist für denselben Benutzer und dieselbe Art von Arbeit gleich, unabhängig davon, um welches Buch es geht, und er unterscheidet sich zwischen Benutzern und zwischen Arten:

| Art | Anfragen |
|---|---|
| Generierung | Ein neuer Teil, *Neu generieren* und *Variante*: Ihre Prompts teilen die Geschichte, daher teilen sie den Wert. |
| Impersonate | *Impersonate*. |
| Kommentierung | *Mit KI kommentieren* und automatische Kommentierung; das zweite Modell des Zweistufenmodus hat einen eigenen Wert. |
| Überarbeitung | *Überarbeiten* und die Überarbeitung bei automatischer Kommentierung. |
| Zusammenfassung | Zusammenfassungen. |
| Agent | Die Anfragen eines [Agenten](agents.md); jeder Lauf hat einen eigenen Wert, da er seine eigene Unterhaltung aufbaut. |
| Hilfe | Der Hilfe-Assistent. |
| Plugins | Jedes Plugin hat seinen eigenen: *Reviewer*, *Side Query* (dessen Tab-Titel einen anderen haben), *Lore Generator*, *Arc Outline*, *Converter* und *Manga Generator*. |

Anfragen, die zu keiner Art gehören (Auflisten der Modelle, Testen der Verbindung, Zählen der Token), senden keinen Header.

Der Wert ist ein Hash mit Schlüssel über Benutzer und Art (der Schlüssel stammt aus `secret.key` im [Datenordner](administration/index.md#the-data-folder)). Er verrät weder den Benutzer noch das Buch, niemand kann daraus ermitteln, wem er gehört, und eine andere Installation ergibt für denselben Benutzer einen anderen Wert. Es gibt einen Wert pro Benutzer und Art, nicht einen pro Buch, sodass ein Server nie mehr Caches vorhalten muss als das.

Alle Anbieter senden ihn: OpenAI-kompatible sowie die Anbieter [Anthropic Claude](extensions/anthropic.md) und [Google Gemini](extensions/google-genai.md). Ein Server, der den Header nicht nutzt, ignoriert ihn. Es gibt keine Einstellung, um ihn abzuschalten.

<a id="why-a-cache-may-not-help"></a>

## Warum ein Cache nicht helfen kann

Ein Server verwendet den Teil des Caches wieder, in dem der neue Prompt **ab dem allerersten Token** mit dem alten übereinstimmt; der erste Unterschied beendet ihn, und alles danach wird erneut gelesen, egal wie viel davon unverändert ist. Schau dir die Zeile `llama-server` an, die beim Start einer Anfrage ausgegeben wird:

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` ist der Anteil des zwischengespeicherten Prompts, den die neue Anfrage beibehält. Nahe bei 1 wird der Cache genutzt; hier sind es nur 15 %, der Prompt weicht also früh vom zwischengespeicherten ab, unabhängig davon, ob der Cache von der Festplatte wiederhergestellt wurde oder nie den Slot verlassen hat.

Modelle mit einem **gleitenden Fenster** (z. B. Gemma) und hybride Modelle können das Ende ihres Caches nicht verwerfen und aus der Mitte weitermachen. Nach dem Quelltext von `llama.cpp` kann `llama-server` nur dann von einem zwischengespeicherten Prompt aus weitermachen, wenn der neue Prompt ihn genau fortsetzt oder wenn ein **Kontext-Checkpoint** an oder vor der Stelle existiert, an der sich die beiden unterscheiden. Checkpoints legt er nur am Anfang von Benutzernachrichten (mindestens 8192 Token auseinander) und kurz vor dem Ende des Prompts an und behält sie in der Slot-Datei (INFO-Zeilen `appended N context checkpoint(s)` und `restored N context checkpoint(s)` zeigen an, wann das geschieht). Ohne brauchbaren Checkpoint liest er den gesamten Prompt erneut, was auch immer `f_keep` sagt, und meldet das nur auf der Trace-Stufe (`-lv 4`: `forcing full prompt re-processing due to lack of cache data`). Deshalb wird die Geschichte bei einer Generierung, einer Kommentierung und einer Überarbeitung als Kette von Nachrichten gesendet, wobei jeder Teil als Antwort des Modells auf eine Nachricht des Benutzers folgt: Die letzte Nachricht des Benutzers erhält einen Checkpoint, und dort beginnt die nächste Anfrage sich zu unterscheiden; ein neuer Teil fügt nur Nachrichten am Ende hinzu. Am Log erkennt man einen kalten Prefill daran: Die erste `prompt processing, n_tokens = 4096`-Zeile erscheint hier nach etwa 7 s; wäre ein Präfix wiederverwendet worden, käme sie nach ein oder zwei Sekunden.

Der [Prompt](books/prompts.md#how-the-prompt-is-put-together) beginnt mit der **Systemnachricht**, und danach folgt die Geschichte; daher muss der Inhalt der Systemnachricht von einer Anfrage zur nächsten gleich bleiben. In der standardmäßigen Hauptvorlage enthält die Systemnachricht in dieser Reihenfolge den Lore-Block, die Zusammenfassungen und die Autorennotizen (Kommentierungen). Ändert sich einer davon zwischen zwei Anfragen, was üblich ist (der neue Text aktiviert andere Lore-Einträge, eine Zusammenfassung kommt hinzu, eine Notiz wird hinzugefügt oder erledigt), ist der Cache nur bis zu dieser Stelle gültig, und die ganze Geschichte dahinter wird erneut gelesen. Die ältesten Teile verlassen den Prompt zuerst, wenn die Geschichte nicht mehr hineinpasst; dadurch verschiebt sich der Anfang der Geschichte bei jedem Zug um einen Teil. Hilfreich ist Folgendes:

- Gib den Einträgen, die sich von Teil zu Teil ändern, den Einfügemodus **Vor dem Benutzerprompt** ([Reihenfolge und Einfügung](lorebooks/entries.md#order-and-insertion)): Sie kommen ans Ende des Prompts, hinter die Geschichte, und stören den Cache nicht. Behalte im Lore-Block, was immer gilt.
- Lass die Blöcke, die du nicht brauchst, aus der Hauptvorlage weg (zum Beispiel die Autorennotizen, wenn du dem Modell keine Notizen gibst), damit die Systemnachricht gleich bleibt.
- Öffne **Prompts vergleichen** im Menü eines Teils, um zu sehen, wo der Cache endet: Es vergleicht den Prompt des Teils mit dem eines früheren Teils (standardmäßig mit dem nächstliegenden; das Feld listet die Teile davor und deren Varianten auf) und markiert die erste Nachricht, die sich unterscheidet. Siehe [Prompts vergleichen](#compare-prompt).

<a id="compare-prompt"></a>

### Prompts vergleichen

**Prompts vergleichen** im Menü eines Teils (Desktop und Telefon) beantwortet die Frage *Warum wurde der Cache für diesen Teil nicht genutzt?* Es listet die Nachrichten der beiden Prompts auf (die Systemnachricht, danach je Teil eine Benutzernachricht und eine Modellantwort, zuletzt die Benutzernachricht des Zugs) mit einem Status für jede: gleich, geändert, hinzugefügt oder gelöscht. Die erste Nachricht, die sich unterscheidet, ist hervorgehoben, und die danach folgenden sind als *Erneut gelesen* markiert, weil ein Server alles nach dem ersten Unterschied erneut liest. Über der Tabelle:

- wo der erste Unterschied liegt (Nachricht und Zeichen) und wie viel des Prompts vom Anfang an gleich ist, in Zeichen und ungefähr in Token;
- eine Deutung des Unterschieds: Die **Systemnachricht** unterscheidet sich (Jailbreak, Vorlage, Lore, Zusammenfassungen oder Notizen haben sich zwischen den beiden Anfragen geändert: die übliche Ursache), ein Teil **in der Mitte der Geschichte** unterscheidet sich (er wurde bearbeitet, neu generiert oder durch eine Zusammenfassung ersetzt), der **Anfang der Geschichte hat sich verschoben** (die ältesten Teile wurden weggelassen oder zusammengefasst), oder der Unterschied betrifft nur die **letzte Nachricht des früheren Prompts**, die Benutzernachricht dieses Zugs, die die Geschichte nicht wiederholt. Dieser letzte Fall ist das Höchste, was ein nächster Teil oder eine Variante wiederverwenden kann; wenn der Server seinen Cache trotzdem nicht nutzt, liegt es nicht am Prompt (prüfe den Slot, das Modell und die Sitzungs-ID).

Wird eine Nachricht ausgewählt, erscheinen die beiden Texte nebeneinander, beginnend kurz vor dem ersten Unterschied. Verglichen werden nur die Nachrichten: Das Modell, die Werkzeuge der Anfrage und der Serverslot gehören nicht zum gespeicherten Prompt. Der Teil braucht einen gespeicherten Prompt (Teile, die der Autor selbst getippt hat, und Teile, die erzeugt wurden, bevor Prompts gespeichert wurden, haben keinen).

Die folgenden Anfragearten sind wichtig für das, was erhalten bleibt, wenn du zwischen ihnen wechselst; der Prompt bestimmt, wie viel davon genutzt wird.
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## Verwendung: ein Slot auf der Festplatte mit llama.cpp

Das ist etwas für Sie, wenn dem Server der Speicher für Caches ausgeht. Ein lokaler `llama-server` mit genug Speicher für mehrere Slots kann für jede Art von Arbeit einen eigenen Cache halten und braucht nichts weiter. Mit Speicher für **einen** Slot wirft jede andere Art von Arbeit den Cache der Geschichte weg: Die Annotation eines Teils, die anschließende Neufassung und die nächste Generierung lesen ihre Prompts jedes Mal erneut ein.

Der Ausweg: Den Cache jeder Art auf der Festplatte behalten und ihn in den einen Slot einlagern, sobald eine Anfrage dieser Art eintrifft. `llama-server` kann einen Slot in eine Datei speichern und wiederherstellen. Ein vorgeschalteter Proxy erkennt am Header, zu welcher Art eine Anfrage gehört. Das folgende Beispiel ist ein Hook für [LiteLLM](https://docs.litellm.ai/).

**Voraussetzung:** ein `llama-server` vom 2026-10-08 oder neuer (llama.cpp PR #26004, *preserve context checkpoints across slot save/restore*). Ein älterer schreibt und liest zwar den Speicher des Slots, aber nicht seine [Kontextcheckpoints](#why-a-cache-may-not-help). Ein Modell mit gleitendem Fenster (zum Beispiel Gemma) oder ein Hybridmodell liest dann nach jedem Wiederherstellen den ganzen Prompt erneut ein, auch wenn davon nur wenig verändert ist. Prüfen Sie den Build mit `llama-server --version`. Ein Container-Image mit einem wandernden Tag (`full`, `server` ...) wird erst aktualisiert, wenn Sie es neu ziehen.

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

und schalten Sie es in der LiteLLM-`config.yaml` ein: den Callback sowie `slot_cache` in der `model_info` der Route, die zum `llama-server` führt. Ein Wildcard-Eintrag genügt. Kein Modell muss in LiteLLM aufgelistet werden, denn der Server kennt sie, und der Client nennt das gewünschte Modell:

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

Der Hook braucht keine eigene Adresse. Er verwendet die `api_base` und `api_key` der Route, zu der die Anfrage geht (die Slots liegen an der Wurzel des Servers, daher wird ein nachgestelltes `/v1` entfernt). Nur Routen mit `slot_cache` werden angefasst. Eine Anfrage für ein anderes Modell, etwa ein Cloud-Modell, geht durch, auch wenn sie den Header trägt. Mehrere Server laufen nebeneinander, jeder mit seinem eigenen Slot. Mit dem Wildcard ist die Route das Modell, das der Client angefragt hat, daher werden die Dateien pro Modell gehalten (`qwen3-32b`). Sie können daneben trotzdem einige Modelle namentlich auflisten, wenn diese andere Einstellungen brauchen. LiteLLM liest den Teil eines Modellnamens vor einem `/` als Provider. Bei Namen, die ein solches Zeichen enthalten (`org/model`, wie in einer Router-Voreinstellung), verwenden Sie daher die Route `model_name: "openai/*"` und geben Marginalia das Modell als `openai/org/model` an: Der Hook entfernt das `openai/`, und die Dateien sowie die Slot-Aufrufe verwenden den Namen, den der Server kennt. Die Routen und ihre `model_info` stammen aus der `model_list`. Ein Proxy, der ohne sie gestartet wurde (nur mit `litellm --model ...`), hat weder das eine noch das andere, und der Hook tut dort nichts. Der eine Wildcard-Eintrag ist der Ersatz.

**`llama-server` im Router-Modus** (viele Modelle hinter einer Adresse, bei Bedarf geladen) funktioniert: Jedes Modell ist eine eigene Instanz mit eigenen Slots. Der Hook hält den Zustand daher pro Server, Modell und Slot und nennt das Modell im Body seiner Slot-Aufrufe (der Name, den der Client verwendet, ohne LiteLLMs `openai/`; die Einstellung `model` setzt einen anderen Namen, für eine Route, deren Name nicht das Modell ist). Dafür braucht es ein llama.cpp vom Februar 2026 oder neuer, das `/slots` an die Instanz weiterleitet (vorher wurde `400 Invalid action` beantwortet).

Wenn der Router nur einige seiner Modelle halten darf (`--models-max`, standardmäßig 4), entlädt er bei der Anfrage nach einem anderen Modell das Modell, das am längsten nicht benutzt wurde. Dessen Slot mit dem Cache darin ist dann weg. Der Hook hält sich an dieselbe Reihenfolge (setzen Sie `models_max` in `slot_cache` oder `LLAMA_MODELS_MAX` auf dieselbe Zahl wie `--models-max`; 0 bedeutet kein Limit). Er speichert diesen Slot **bevor** die Anfrage kommt, die den Router zum Entladen zwingt. Dafür muss der Proxy der einzige Client des Routers sein, denn eine Anfrage, die auf anderem Weg ankommt, wird nicht gesehen. Ein Speichervorgang lädt nie ein Modell, das der Router entladen hat.

Konkret: Der Cache einer Sitzung wird auf die Festplatte geschrieben, **wenn der Slot für eine andere Sitzung gebraucht wird** (eine Anfrage anderer Art, eines anderen Modells oder das Entladen durch den Router), nicht nach jeder Anfrage. Ein 3-GB-Schreibvorgang nach jeder Generierung würde den Server nur verlangsamen. Eine Datei entsteht daher erst nach dem ersten Wechsel, zum Beispiel bei Generieren, Annotieren, Generieren: Die Annotation speichert zuerst die Generierung. Mit `save_idle_seconds` in der `slot_cache` (oder `LLAMA_SAVE_IDLE_SECONDS`) wird der Cache auch dann geschrieben, wenn so lange keine Anfrage kam. Der Ordner enthält ihn daher bald, nachdem Sie aufgehört haben zu arbeiten. Eine Anfrage beginnt die Wartezeit von vorn. Ein Cache, der schon auf der Festplatte liegt, wird beim nächsten Wechsel nicht erneut geschrieben. Der Hook sendet ein Speichern, Wiederherstellen oder Löschen nur, wenn der Slot untätig ist (er fragt `GET /slots` und `is_processing` ab; ohne diesen Endpunkt sendet er sofort). `llama-server` hält einen solchen Aufruf zurück, solange eine Anfrage läuft, und gibt bei jedem Ende einer Anfrage nur einen zurückgehaltenen Aufruf frei. So könnte eine Anfrage eines anderen Clients, die dahinter eintraf, dauerhaft warten. Der Server tut während des Schreibens nichts anderes.

Die Details: Trifft eine Anfrage aus einer anderen Sitzung ein als der, die im Slot ist, speichert der Hook den Slot unter dem Namen der Sitzung, die darin war. Dann stellt er die Datei der neuen Sitzung wieder her (oder leert den Slot, wenn es noch keine Datei gibt) und lässt die Anfrage weiterlaufen. Die Dateien werden **pro Route** gehalten (`model_name`). Ein gespeicherter Zustand passt nur zu dem Modell, das ihn geschrieben hat. Daher wird die Datei nach Route und Sitzung benannt, und zwei Routen teilen nie eine Datei, auch wenn sie denselben Server und Ordner verwenden. Der Server liest danach nur, was vom wiederhergestellten Prompt abweicht. Ein Cache, der nicht wiederhergestellt werden kann, eine fehlende Datei oder ein nicht antwortender Server bedeuten nur, dass der Prompt erneut gelesen wird. Die Anfrage selbst schlägt wegen des Hooks nie fehl.

Dinge, die Sie wissen sollten:

- **Größe.** Eine Datei enthält den Speicher des Slots **und seine Kontextcheckpoints**, sie ist also so groß wie beides zusammen: bei einer langen Geschichte und einem großen Modell hunderte Megabyte bis mehrere Gigabyte. Ein gemessenes Beispiel: Ein 31-B-Modell mit einem Prompt von 18.000 Tokens erzeugte Dateien von 1,3 bis 11 GB. Die größte enthielt etwa 18 Checkpoints zu je rund 450 MB, dazu etwa 1,5 GB Cache. Ein Server behält standardmäßig bis zu 32 Checkpoints, und jeder Wechsel schreibt und liest alle davon. `--ctx-checkpoints N` von `llama-server` begrenzt das. Das macht die Dateien kleiner und die Wechsel kürzer, aber ein Checkpoint hilft nur, wenn sich der nächste Prompt erst an ihm oder kurz danach vom gespeicherten unterscheidet. Ein neuer Teil oder ein Swipe weicht am Anfang der letzten Nutzernachricht ab (dem neuesten Checkpoint). Eine Annotation oder Neufassung kann dagegen einen Teil mehrere Teile zurück ändern. Behalten Sie daher so viele Checkpoints, wie die Geschichte zurückreicht. Die `prompt:`-Zeilen des Hooks zeigen, wo jede Anfrage abweicht. Es gibt eine Datei pro Route, Nutzer und Art, und jede wird an Ort und Stelle überschrieben. Der Ordner bleibt daher so klein.
- **Geschwindigkeit.** Prüfen Sie, dass ein Wechsel schneller ist als das erneute Lesen des Prompts (der Hook protokolliert Größe und Dauer jedes Speicherns und Wiederherstellens). Das obige Beispiel, auf einer NVMe-Festplatte mit einer AMD Radeon AI PRO R9700: Ein Speichern von 11 GB dauerte 6 s, ein Wiederherstellen von 7,6 GB für 20.000 Tokens 8 s, also etwa 14 s für einen Wechsel. Den Prompt dieser Länge erneut zu lesen dauert dagegen rund 45 s bei 440 Tokens pro Sekunde, und das Vorfüllen wächst mit dem Prompt. Ohne Checkpoints waren dieselben Dateien etwa 1 GB groß, und ein Wechsel dauerte 5 s.
- **Speicher.** Die Checkpoints des Slots liegen im **Systemspeicher** des Servers (nicht im Speicher der GPU), bis zu `--ctx-checkpoints` davon (standardmäßig 32, bei einem großen Modell je hunderte Megabyte). Ein Speichern oder Wiederherstellen liest oder schreibt alle davon. `llama-server` hält außerdem die Prompts der Slots, die es aufgegeben hat, in einem eigenen Speicher-Cache (`--cache-ram`, standardmäßig 8 GiB). Die Dateien des Hooks erledigen dieselbe Aufgabe. Deshalb kann er auf einem Rechner mit knappem Speicher auf 0 gesetzt werden. Wenn der Rechner anfängt zu swappen, werden Speichern und Wiederherstellen um ein Vielfaches langsamer, weil die Checkpoints zuerst aus dem Swap zurückgelesen werden. Prüfen Sie daher zuerst `free -h`, bevor Sie die Festplatte untersuchen.
- **Grenzen.** Ohne Grenze wächst der Ordner mit der Zahl der Routen, Nutzer und Arten. Setzen Sie `max_files` (eine Anzahl von Dateien) und/oder `max_gib` (ihre Gesamtgröße) in der `slot_cache` einer Route (oder `LLAMA_CACHE_MAX_FILES` und `LLAMA_CACHE_MAX_GIB` in der Umgebung des Proxys für alle Routen). Dann löscht der Hook nach jedem Speichern die Caches **dieser Route**, die am längsten nicht **benutzt** wurden. Ein Wiederherstellen zählt als Benutzen. Er löscht nie den Cache, den er gerade gespeichert hat, oder den, den er gleich wiederherstellt. Eine Route mit zwei Sitzungen im Wechsel behält daher mindestens zwei Dateien, unabhängig vom Limit. Er berührt nur die Dateien der Route, andere Routen und andere Dateien im Ordner sind also sicher. Ein einzelner Cache, der größer als `max_gib` ist, bleibt erhalten (mit einer Warnung im Protokoll). Zum Löschen braucht der Hook den Ordner: Setzen Sie `save_path` (oder `LLAMA_SLOT_SAVE_PATH`) auf den `--slot-save-path`-Ordner, so wie der Proxy ihn sieht. Mit `llama-server` in einem Container ist das ein Volume, das beide Container einbinden. Ohne diese Angabe wird nichts gelöscht. Ein gelöschter Cache bedeutet nur, dass seine Sitzung den Prompt einmal erneut liest. Die Limits sind optional. Ein Cronjob auf dem Host tut dasselbe, wenn Sie keine Obergrenze für die Größe brauchen, zum Beispiel `find /srv/ai/slots -name '*.bin' -mtime +2 -delete` (ein Cache, der in Gebrauch ist, wird jedes Mal neu gespeichert, wenn seine Sitzung ausgelagert wird, daher beginnt sein Alter von vorn). Dateien, die von außen gelöscht werden, sind für den Hook unschädlich. Eine fehlende Datei ist ein Fehlversuch.
- **Nach einer Änderung des Modells einer Route** löschen Sie deren Dateien im Ordner (ihre Namen beginnen mit der Route). Ein Zustand, der mit anderen Gewichten gespeichert wurde, passt nicht zum neuen Modell.
- **Jeder Wechsel kostet ein Speichern und ein Wiederherstellen.** Ist [automatische Annotation](books/annotations.md) eingeschaltet, folgen auf einen Teil eine Annotation und eine Neufassung. Ein Durchlauf kann dann drei- oder viermal wechseln. Messen Sie einen Durchlauf, bevor Sie sich darauf verlassen.
- **Ein Neustart des Proxys** vergisst, welche Sitzung im Slot ist. Die erste Anfrage danach stellt die Datei ihrer Sitzung über den Slot wieder her, und was der Slot vorher enthielt, geht verloren.
- **Ein Modell, das der Router entladen hat, ohne dass der Hook es weiß** (ein falsches `models_max`, ein anderer Client), hat einen leeren Slot, während der Hook noch glaubt, dass die letzte Sitzung darin ist. Eine Anfrage derselben Sitzung liest ihren Prompt einfach erneut. Eine Anfrage einer anderen Sitzung speichert zuerst den leeren Slot über die Datei dieser Sitzung, daher geht auch deren Cache verloren. Beides kostet nur ein Vorfüllen.
- `llama-server` protokolliert ein einfaches Speichern oder Wiederherstellen nicht. Es protokolliert die Kontextcheckpoints, die es anhängt oder wiederherstellt, und Fehler. Was es getan hat, steht in seiner Antwort, und das Protokoll des Proxys (mit `LITELLM_LOG=INFO`) enthält es, mit einer Zeile für jeden Aufruf des Hooks, zum Beispiel `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`, `… save <file> …: ok (…)` oder `… restore … miss (400)`. Der Fehlversuch bei der ersten Anfrage einer Sitzung ist normal (llama-server protokolliert ihn als "Unable to restore slot, no available space in KV cache or invalid slot save file"). Echte Probleme sind Warnungen: ein Zertifikat, das nicht zur Adresse passt, ein abgelehnter API-Schlüssel, ein Server, der nicht antwortet.
- **Wo der Prompt abweicht.** Für jede Anfrage nach der ersten einer Sitzung enthält das Protokoll eine Zeile, die angibt, wie weit der Prompt mit dem letzten dieser Sitzung übereinstimmt, zum Beispiel `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`. Ein Server kann seinen Cache nur bis dorthin wiederverwenden (eine Nachricht hat etwa drei bis vier Zeichen pro Token). Diese Zeile lesen Sie also, wenn eine zweite Anfrage fast alles erneut liest. Die genannte Nachricht ist diejenige, deren Anfang sich geändert hat. Die Zeile vergleicht außerdem die letzte Nachricht der beiden Prompts. Sie enthält, was sich ändert (das Lore, den Teil, die Aufgabe). Wenn die Geschichte um einen Teil gewachsen ist, steht die alte letzte Nachricht nicht mehr an derselben Stelle. Die Zahl der Zeichen, die die beiden letzten Nachrichten gemeinsam haben, zeigt dann, wie weit zum Beispiel das Lore in beiden gleich ist.
- **Wenn nichts geschieht**, sehen Sie im Protokoll des Proxys nach (`docker logs litellm-proxy 2>&1 | grep slot_cache`). Wenn der Hook arbeitet, gibt es eine Zeile für jeden Aufruf. Eine Anfrage von Marginalia für eine Route ohne `slot_cache` wird einmal gemeldet (`not used for <route>: no model_info.slot_cache`). Fehlt jede `slot_cache`-Zeile, lief der Hook nicht. Dann hat der Container noch eine ältere Datei oder Konfiguration. Eine einzelne Datei, die in einen Container eingebunden ist (`./slot_cache.py:/app/slot_cache.py`), zeigt weiter die Version, die beim Erstellen des Containers vorlag, wenn ein Editor die Datei ersetzt hat. `docker compose up -d
  --force-recreate` übernimmt die neue (ein einfacher Neustart nicht). `docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` gibt 0 für eine Datei aus, die zu alt ist.
- **In Docker** sind der Proxy und `llama-server` zwei Container. Der Ordner von `--slot-save-path` ist daher ein Verzeichnis des Hosts, das beide einbinden. Der Proxy muss dort schreiben können, denn er löscht alte Dateien und erneuert das Alter der benutzten. Die Hook-Datei kommt neben die LiteLLM-Konfiguration. `api_base` ist eine Adresse, die der Proxy erreichen kann, mit dem Hostnamen des Zertifikats, wenn der Server TLS verwendet:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  mit `save_path: /slots` und `models_max: 1` in der `slot_cache` der Route.
- Der Hook verwendet `CustomLogger.async_pre_call_deployment_hook`, das LiteLLM aufruft, nachdem es das Deployment gewählt hat, und bevor es die Anfrage sendet. Version 1.106.5 wurde mit einem echten Router und einem Ersatzserver geprüft. Prüfen Sie das für die Version, die Sie betreiben. Ein Proxy, der Modelle hinter einer Adresse wechselt, indem er einen einzelnen Server neu startet (llama-swap), wird nicht unterstützt. Die Slot-Aufrufe würden dann beim jeweils geladenen Modell landen. Nichts davon wurde gegen einen echten `llama-server`-Router ausgeführt. Die Tests verwenden einen Ersatzserver, der die Aufrufe des Hooks prüft.
<a id="freeing-the-gpu-for-another-model"></a>

## Die GPU für ein anderes Modell freigeben

Eine GPU, auf der das Sprachmodell liegt, hat daneben keinen Platz für ein Bild- oder Sprachmodell. Mit `llama-server` im Router-Modus kann ein zweiter Hook die Modelle auf Anfrage entladen: Ein Schritt eines [GPU-Locks](administration/gpus.md) (des Locks des Bild- oder Sprachservers, bevor dieser seinen Server startet) fordert das mit einer Anfrage an den Proxy an, mit demselben Schlüssel, den er für alles andere verwendet:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

Eine Anfrage mit dem Header `X-Llama-Unload` beantwortet der Hook selbst (HTTP 200, der Text `unloaded: <names>`) und sie erreicht nie ein Modell. Der Hook listet die Modelle des Routers auf (`GET /models`), entlädt die geladenen (`POST /models/unload` mit dem Namen) und wartet, bis der Router meldet, dass keines mehr im Speicher ist (`loaded`, `loading` und `unloading` bedeuten, dass der Speicher belegt ist), höchstens `LLAMA_UNLOAD_WAIT_SECONDS` (120). Ist der Server nicht erreichbar, antwortet er mit einem Fehler, oder sind die Modelle nach dem Warten noch da, lautet die Antwort Fehler (HTTP 502). So startet ein Lock, dessen erster Acquire-Schritt diese Anfrage ist, das andere Modell nicht auf einer vollen GPU. Das Modell kommt von selbst zurück: Die nächste normale Anfrage lässt den Router es wieder laden, sodass die *after*-Anfrage eines Plugins für das Sprachmodell leer sein kann. Der Modellname im Body muss nur einer sein, den eine Route akzeptiert (bei der Route `openai/*` beginnt er mit `openai/`, zum Beispiel `openai/unload`). Entscheidend ist der Header. Ein Name, den keine Route akzeptiert, wird mit einem 400 beantwortet, *nachdem* die Modelle entladen wurden, weil der Hook vor der Namensprüfung von LiteLLM läuft. Das Entladen betrifft jeden Server, dessen Route `unload` hat, und nicht nur ein Modell.

Der Hook ist ein eigenes Skript, `gpu_unload.py`, neben `slot_cache.py` und mit ihm verkettet. Die beiden sind unabhängig: `gpu_unload.py` funktioniert auch ohne den Cache-Hook. Wenn beide verkettet sind, findet `gpu_unload.py` den Cache-Hook unter den Callbacks von LiteLLM und ruft dessen `release_server` auf. Das **speichert zuerst den Cache jedes Slots und vergisst, was darin war**, damit die nächste Anfrage den Cache ihrer Sitzung in das neu geladene Modell zurückholt. (Ohne diesen Aufruf würde der Cache-Hook immer noch glauben, die Sitzung sei im Slot, und nichts wiederherstellen. Der ganze Prompt würde nach jedem Wechsel erneut gelesen.)

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

In Docker muss die Datei im Container liegen, so wie `slot_cache.py`: Füge `- ./gpu_unload.py:/app/gpu_unload.py:ro` zu den `volumes` des Proxys hinzu, neben die Zeile von `slot_cache.py`. Eine fehlende Datei erscheint beim Start als `ImportError: Could not import proxy_handler_instance from gpu_unload` (der eigentliche Grund, `FileNotFoundError` oder ein Fehler des Skripts selbst, steht einige Zeilen darüber im Log).

Verkette die beiden in `litellm_config.yml` und markiere die Route mit `unload`:

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

Mache daraus einen [GPU-Schritt](administration/gpus.md), der eine `POST`-Anfrage an die Adresse oben schickt, mit den Headern `Authorization: Bearer <key>` und `X-Llama-Unload: all` und dem Body aus dem Beispiel (die Header werden verschlüsselt gespeichert). Stelle ihn an den Anfang der Acquire-Schritte der Locks des Bild- und Sprachservers.

Der Server des anderen Modells lässt sich über denselben Proxy steuern. LiteLLM leitet einen Pfad an eine andere Adresse weiter (`general_settings: pass_through_endpoints`, `path`, `target` und `include_subpath: true`). Der Schlüssel des Proxys wird geprüft, und der Header `Authorization` des Clients wird **nicht** weitergereicht, daher hat das Ziel ein eigenes Geheimnis in `headers`. Ein kleiner Dienst neben dem Sprach- oder Bildserver, der dessen Container stoppt und startet, ist so ein Ziel: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control) (`POST /<container>/start` antwortet, wenn der Dienst darin bereit ist, `stop`, wenn er beendet wurde. Seine README enthält das Compose-Beispiel und die LiteLLM-Route für den Sprachserver, `/v1/audio/speech`).

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

Geprüft wurde Folgendes: LiteLLM 1.108.3 mit einem Ersatz-Router, der `loaded`, `unloading` und `unloaded` meldet. Das Warten hielt die Antwort, bis der Zustand `unloaded` war. Ein nicht erreichbarer Server ergab 502. Mit beiden verketteten Hooks wurde der Slot vor dem Entladen gespeichert und von der nächsten Anfrage wiederhergestellt. Mit nur `gpu_unload.py` wurde sonst nichts aufgerufen. Die Weiterleitung erreichte ihr Ziel, setzte an Stelle eines vom Client gesendeten Werts seinen eigenen `headers`-Wert und wies eine Anfrage ohne Schlüssel ab. Außerdem wurde es gegen einen echten `llama-server`-Router hinter LiteLLM getestet (ein 31B-Modell). Die Anfrage kam nach 1 bis 2 Sekunden mit `unloaded: <model>` zurück, der Router protokollierte `stopping model instance` und `exited with status 0` (der Prozess ist weg, also ist der Speicher frei). Eine zweite Anfrage, bei der nichts geladen war, kam sofort mit `unloaded: nothing` zurück. Die nächste normale Anfrage dauerte etwa eine Minute, weil der Router das Modell erneut lud. Wenn dein `GET /models` die Zustände anders benennt, ändere `IN_MEMORY` im Skript.
