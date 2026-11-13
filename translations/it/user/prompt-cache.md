---
label: Cache del prompt e ID di sessione
order: 975
icon: database
---

# Cache del prompt e ID di sessione

I server dei modelli conservano il prompt appena letto (la *cache del prompt*), così che la richiesta successiva che inizia con lo stesso testo non debba leggerlo di nuovo. Per una storia lunga la differenza è tra attendere pochi secondi e attendere minuti. Una cache aiuta solo finché il nuovo prompt inizia come quello vecchio (vedi [Perché una cache può non aiutare](#why-a-cache-may-not-help)). Marginalia può anche comunicare al server **che tipo di lavoro è una richiesta**, così che un server, o un proxy davanti a esso, possa mantenere una cache per ciascun tipo.

<a id="what-is-sent"></a>

## Cosa viene inviato

Ogni richiesta che Marginalia fa per un tipo di lavoro porta l'header HTTP

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

Il valore è lo stesso per lo stesso utente che svolge lo stesso tipo di lavoro, qualunque sia il libro di cui si tratta, e cambia tra utenti e tra tipi:

| Tipo | Richieste |
|---|---|
| generazione | Una nuova parte, *Rigenera* e *Swipe*: i loro prompt condividono la storia, quindi condividono il valore. |
| impersonificazione | *Impersonate*. |
| annotazione | *Annota con l'IA* e l'annotazione automatica; il secondo modello della modalità a due fasi ha un valore proprio. |
| riscrittura | *Riscrittura* e la riscrittura dell'annotazione automatica. |
| riassunto | I riassunti. |
| agente | Le richieste di un [agente](agents.md); ogni esecuzione ha un valore proprio, perché fa crescere una propria conversazione. |
| aiuto | L'assistente di aiuto. |
| plugin | Ciascun plugin ha il suo: *Reviewer*, *Side Query* (le sue schede hanno altri titoli), *Lore Generator*, *Arc Outline*, *Converter* e *Manga Generator*. |

Le richieste che non appartengono a nessun tipo (elencare i modelli, verificare la connessione, contare i token) non inviano l'header.

Il valore è un hash con chiave dell'utente e del tipo (la chiave viene da `secret.key` nella [cartella dei dati](administration/index.md#the-data-folder)). Non rivela né l'utente né il libro, nessuno può risalire a chi appartiene, e un'altra installazione dà un valore diverso per lo stesso utente. C'è un valore per utente e tipo, non uno per libro, quindi un server non deve mai mantenere più cache di quelle.

Tutti i provider lo inviano: quelli compatibili con OpenAI, e i provider [Anthropic Claude](extensions/anthropic.md) e [Google Gemini](extensions/google-genai.md). Un server che non usa l'header lo ignora. Non esiste un'impostazione per disattivarlo.

<a id="why-a-cache-may-not-help"></a>

## Perché una cache può non aiutare

Un server riutilizza la parte della cache in cui il nuovo prompt è uguale al vecchio **fin dal primo token**; la prima differenza interrompe il riuso, e tutto ciò che viene dopo viene letto di nuovo, anche se gran parte è invariata. Guarda la riga di `llama-server` stampata all'avvio di una richiesta:

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` è la quota del prompt in cache che la nuova richiesta conserva. Vicino a 1 la cache viene usata; qui ne viene usato solo il 15 %, quindi il prompt differisce da quello in cache già all'inizio, che la cache sia stata ripristinata dal disco o non abbia mai lasciato lo slot.

I modelli con una **finestra scorrevole** (Gemma, per esempio) e i modelli ibridi non possono eliminare la fine della cache e ripartire dal mezzo. Stando al sorgente di `llama.cpp`, `llama-server` può continuare da un prompt in cache solo quando il nuovo prompt ne estende esattamente l'inizio, oppure quando esiste un **checkpoint di contesto** nel punto in cui i due divergono o prima. Ne crea solo all'inizio dei messaggi dell'utente (distanziati di almeno 8192 token) e poco prima della fine del prompt, e li conserva nel file dello slot (le righe INFO `appended N context checkpoint(s)` e `restored N context checkpoint(s)` indicano quando lo fa). Senza un checkpoint utilizzabile rilegge l'intero prompt, indipendentemente da `f_keep`, e lo segnala solo al livello trace (`-lv 4`: `forcing full prompt re-processing due to lack of cache data`). Per questo la storia di una generazione, di un'annotazione e di una riscrittura viene inviata come una catena di messaggi, ciascuna parte come risposta del modello dopo un messaggio dell'utente: l'ultimo messaggio dell'utente riceve un checkpoint, ed è lì che la richiesta successiva comincia a differire, e una nuova parte aggiunge solo messaggi in fondo. Per leggere un prefill a freddo dal log: la prima riga `prompt processing, n_tokens = 4096` arriva dopo circa 7 s qui; se un prefisso fosse stato riutilizzato, arriverebbe dopo uno o due secondi.

Il [prompt](books/prompts.md#how-the-prompt-is-put-together) inizia con il **messaggio di sistema**, e la storia viene dopo, quindi ciò che sta nel messaggio di sistema deve restare uguale da una richiesta alla successiva. Nel template principale predefinito il messaggio di sistema contiene, in quest'ordine, il blocco della lore, i riassunti e le note dell'autore (annotazioni). Quando uno di questi cambia tra due richieste, cosa comune (il nuovo testo attiva altre voci della lore, si aggiunge un riassunto, si aggiunge o si risolve una nota), la cache vale solo fino a quel punto e tutta la storia che ne segue viene letta di nuovo. Le parti più vecchie escono per prime dal prompt quando la storia non ci sta più, il che sposta l'inizio della storia di una parte a ogni turno. Ciò che aiuta:

- Assegna alle voci che cambiano da una parte all'altra la modalità di inserimento **Prima del prompt utente** ([Ordine e inserimento](lorebooks/entries.md#order-and-insertion)): vanno in fondo al prompt, dopo la storia, e non disturbano la cache. Lascia nel blocco della lore ciò che è sempre vero.
- Escludi dal template principale i blocchi che non usi (per esempio le note dell'autore, se non dai note al modello), così che il messaggio di sistema resti uguale.
- Apri **Confronta il prompt** nel menu di una parte per vedere dove termina la cache: confronta il prompt della parte con quello di una parte precedente (per impostazione predefinita la più vicina; il riquadro elenca le parti che la precedono e i suoi swipe) e segna il primo messaggio che differisce. Vedi [Confronta il prompt](#compare-prompt).

<a id="compare-prompt"></a>

### Confronta il prompt

**Confronta il prompt** nel menu di una parte (desktop e telefono) risponde alla domanda *perché la cache non è stata usata per questa parte?* Elenca i messaggi dei due prompt (il messaggio di sistema, poi un messaggio utente e una risposta del modello per ogni parte, poi il messaggio utente del turno) con uno stato per ciascuno: uguale, modificato, aggiunto o eliminato. Il primo messaggio che differisce viene evidenziato e quelli successivi sono segnati come *Rileggi*, perché un server rilegge tutto ciò che viene dopo la prima differenza. Sopra la tabella:

- dove si trova la prima differenza (messaggio e carattere) e quanto del prompt, in caratteri e approssimativamente in token, è uguale dall'inizio;
- una lettura della differenza: il **messaggio di sistema** differisce (il jailbreak, il template, la lore, i riassunti o le note sono cambiati tra le due richieste: la causa più comune), una parte **nel mezzo della storia** differisce (è stata modificata, rigenerata o sostituita da un riassunto), l'**inizio della storia si è spostato** (le parti più vecchie sono state escluse o riassunte), oppure la differenza riguarda solo l'**ultimo messaggio del prompt precedente**, il messaggio utente di quel turno, che la storia non ripete. Quest'ultimo caso è il massimo che una parte successiva o uno swipe possono riutilizzare; se il server non ha comunque usato la sua cache, la causa non è il prompt (controlla lo slot, il modello e l'ID di sessione).

Selezionando un messaggio si vedono i due testi affiancati, a partire poco prima della prima differenza. Vengono confrontati solo i messaggi: il modello, gli strumenti della richiesta e lo slot del server non fanno parte del prompt memorizzato. La parte deve avere un prompt memorizzato (le parti scritte dall'autore e quelle generate prima che i prompt venissero conservati non ne hanno).

I tipi di richiesta qui sotto contano per ciò che viene conservato quando passi dall'uno all'altro; il prompt decide quanta parte di esso viene riutilizzata.
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## Uso: uno slot su disco con llama.cpp

Il valore è per te se il server finisce la memoria per le cache. Un `llama-server` locale con memoria sufficiente per più slot
può mantenere una cache per ogni tipo di lavoro e non richiede altro. Con memoria per **un solo** slot, ogni altro tipo di
lavoro getta via la cache della storia: l'annotazione di una parte, la riscrittura successiva e la generazione seguente
rileggono ogni volta i loro prompt.

La soluzione è tenere su disco la cache di ogni tipo e caricarla nell'unico slot quando arriva una richiesta di quel tipo.
`llama-server` può salvare uno slot in un file e ripristinarlo; un proxy davanti al server riconosce il tipo di una
richiesta dall'intestazione. L'esempio seguente è un hook per [LiteLLM](https://docs.litellm.ai/).

**Requisito:** un `llama-server` del 2026-10-08 o successivo (llama.cpp PR #26004, *preserve context checkpoints across
slot save/restore*). Uno più vecchio scrive e ripristina la memoria dello slot ma non i suoi [checkpoint di
contesto](#why-a-cache-may-not-help), quindi un modello con finestra scorrevole (Gemma, per esempio) o un modello ibrido
rilegge l'intero prompt dopo ogni ripristino, per quanto di esso sia invariato. Verifica la build con `llama-server --version`;
un'immagine di container con un tag mobile (`full`, `server`...) non viene aggiornata finché non la scarichi di nuovo.

Avvia `llama-server` con uno slot e una cartella per i file:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

Salva questo file come `slot_cache.py` accanto alla configurazione di LiteLLM:

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

e attivalo nel `config.yaml` di LiteLLM: il callback, e `slot_cache` nel `model_info` della route che va al
`llama-server`. Basta una voce con wildcard, senza elencare nessun modello in LiteLLM (il server li conosce e il client
indica quello che vuole):

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

L'hook non ha un indirizzo proprio: usa `api_base` e `api_key` della route verso cui va la richiesta (gli slot si
trovano alla radice del server, quindi un `/v1` finale viene tolto). Vengono toccate solo le route con `slot_cache`; una
richiesta per un altro modello, per esempio uno in cloud, passa invariata anche se porta l'intestazione. Più server
lavorano affiancati, ciascuno con il proprio slot. Con la wildcard la route è il modello richiesto dal client, quindi i file
sono tenuti per modello (`qwen3-32b`), e puoi comunque elencare per nome alcuni modelli accanto ad essa se hanno bisogno di
altre impostazioni. LiteLLM considera provider la parte del nome di un modello prima di una `/`, quindi per i nomi che ne
contengono una (`org/model`, come in un preset del router) usa la route `model_name: "openai/*"` e indica a Marginalia il
modello come `openai/org/model`: l'hook rimuove `openai/`, e i file e le chiamate agli slot usano il nome che il server
conosce. Le route e il loro `model_info` provengono da `model_list`: un proxy avviato senza (solo con `litellm --model ...`)
non ne ha, e lì l'hook non fa nulla. L'unica voce con wildcard è la sostituzione.

**`llama-server` in modalità router** (molti modelli dietro un solo indirizzo, caricati a richiesta) funziona: ogni modello
è un'istanza a sé con i propri slot, quindi l'hook tiene lo stato per server, modello e slot, e indica il modello nel corpo
delle sue chiamate agli slot (il nome usato dal client, senza `openai/` di LiteLLM; l'impostazione `model` ne indica un
altro, per una route il cui nome non è quello del modello). Serve un llama.cpp di febbraio 2026 o successivo, che inoltra
`/slots` all'istanza (prima rispondeva `400 Invalid action`).

Quando il router può tenere caricati solo alcuni dei suoi modelli (`--models-max`, 4 per impostazione predefinita),
chiederne un altro lo fa scaricare il modello usato da più tempo, e lo slot di quel modello, con la cache dentro, va perso.
L'hook segue lo stesso ordine (imposta `models_max` in `slot_cache`, oppure `LLAMA_MODELS_MAX`, con lo stesso numero di
`--models-max`; 0 per nessun limite) e salva quello slot **prima** della richiesta che fa scaricare il modello al router.
Per questo il proxy deve essere l'unico client del router: una richiesta che lo raggiunge in altro modo non viene vista.
Un salvataggio non carica mai un modello che il router ha scaricato. Come funziona: la cache di una sessione viene scritta
su disco **quando lo slot serve a un'altra** (una richiesta di un altro tipo, un altro modello, o il router che scarica il
modello), non dopo ogni richiesta, perché una scrittura di 3 GB dopo ogni generazione rallenterebbe solo il server. Quindi
un file compare dopo il primo cambio, per esempio generazione, annotazione, generazione: l'annotazione salva per prima la
generazione. Con `save_idle_seconds` in `slot_cache` (o `LLAMA_SAVE_IDLE_SECONDS`) la cache viene scritta anche quando non
arriva nessuna richiesta per quel tempo, così la cartella l'ha poco dopo che smetti di lavorare; una richiesta ricomincia
l'attesa, e una cache già presente su disco non viene riscritta al cambio successivo. L'hook invia un salvataggio, un
ripristino o una cancellazione solo quando lo slot è libero (chiede `GET /slots`, `is_processing`; senza quell'endpoint
invia subito): `llama-server` tiene in sospeso una chiamata del genere mentre una richiesta è in corso e ne rilascia una
sola ogni volta che una richiesta finisce, quindi una richiesta di un altro client arrivata dietro di essa potrebbe
attendere lì per sempre. Durante la scrittura di un salvataggio il server non fa altro.

I dettagli: quando arriva una richiesta da una sessione diversa da quella nello slot, l'hook salva lo slot con il nome
della sessione che vi si trovava, ripristina il file della nuova (o svuota lo slot se non c'è ancora un file) e lascia
proseguire la richiesta. I file sono tenuti **per route** (il `model_name`): uno stato salvato si adatta solo al modello che
lo ha scritto, quindi il file prende il nome dalla route e dalla sessione, e due route non ne condividono mai uno, anche se
usano lo stesso server e la stessa cartella. Il server poi legge solo ciò che differisce dal prompt ripristinato. Una cache
che non si può ripristinare, un file che non c'è più o un server che non risponde fanno solo rileggere il prompt; la
richiesta in sé non fallisce mai per colpa dell'hook.

Da sapere:

- **Dimensione.** Un file contiene la memoria dello slot **e i suoi checkpoint di contesto**, quindi occupa quanto entrambi
  insieme: per una storia lunga e un modello grande, da centinaia di megabyte a diversi gigabyte. Un esempio misurato: un
  modello da 31 B con un prompt di 18.000 token ha prodotto file da 1,3 a 11 GB, il più grande con circa 18 checkpoint da
  circa 450 MB ciascuno oltre a circa 1,5 GB di cache. Un server tiene di default fino a 32 checkpoint, e ogni cambio
  scrive e legge tutti. `--ctx-checkpoints N` di `llama-server` ne limita il numero, il che rende i file più piccoli e i
  cambi più brevi, ma un checkpoint serve solo quando il prompt successivo differisce per la prima volta da quello in cache
  in corrispondenza di esso o subito dopo. Una nuova parte o uno swipe differiscono all'inizio dell'ultimo messaggio
  dell'utente (il checkpoint più recente), mentre un'annotazione o una riscrittura può cambiare una parte di qualche
  capitolo indietro, quindi tieni tanti checkpoint quanti ne richiede la storia (guarda le righe `prompt:` dell'hook, dicono
  dove ogni richiesta differisce). C'è un file per route, utente e tipo, e ciascuno viene sovrascritto sul posto, quindi la
  cartella resta di questa dimensione.
- **Velocità.** Verifica che un cambio sia più veloce che rileggere il prompt (l'hook registra dimensione e tempo di ogni
  salvataggio e ripristino). L'esempio sopra, su un disco NVMe con una AMD Radeon AI PRO R9700: il salvataggio di 11 GB ha
  richiesto 6 s e il ripristino di 7,6 GB per 20.000 token 8 s, circa 14 s per un cambio, mentre rileggere un prompt di
  quella lunghezza richiede circa 45 s a 440 token al secondo, e il prefill cresce con il prompt. Senza checkpoint gli
  stessi file pesavano circa 1 GB e un cambio durava 5 s.
- **Memoria.** I checkpoint dello slot sono tenuti nella **memoria di sistema** del server (non in quella della GPU), fino a
  `--ctx-checkpoints` di essi (32 per impostazione predefinita, centinaia di megabyte ciascuno per un modello grande), e un
  salvataggio o un ripristino legge o scrive tutti. `llama-server` tiene inoltre i prompt degli slot che ha abbandonato in
  una propria cache in memoria (`--cache-ram`, 8 GiB per impostazione predefinita); i file dell'hook svolgono lo stesso
  compito, quindi può essere impostato a 0 su una macchina con poca memoria. Quando la macchina inizia a usare la swap,
  salvataggi e ripristini diventano diverse volte più lenti, perché i checkpoint vanno prima riletti dalla swap: guarda
  `free -h` prima di guardare il disco.
- **Limiti.** Senza un limite la cartella cresce con il numero di route, utenti e tipi. Imposta `max_files` (un numero di
  file) e/o `max_gib` (la loro dimensione totale) nello `slot_cache` di una route (oppure `LLAMA_CACHE_MAX_FILES` e
  `LLAMA_CACHE_MAX_GIB` nell'ambiente del proxy, per tutte le route) e l'hook cancella dopo ogni salvataggio le cache **di
  quella route** che sono state **usate** da più tempo: anche ripristinare una cache conta come usarla. Non cancella mai la
  cache che ha appena salvato né quella che sta per ripristinare (quindi una route con due sessioni a turno conserva almeno
  due file, qualunque sia il limite), e tocca solo i file della route, perciò le altre route e gli altri file nella cartella
  sono al sicuro. Una singola cache più grande di `max_gib` resta (con un avviso nel log).
  Per cancellare serve la cartella: imposta `save_path` (o `LLAMA_SLOT_SAVE_PATH`) alla cartella di `--slot-save-path` come
  la vede il proxy; con `llama-server` in un container è un volume che entrambi i container montano. Senza di essa non si
  cancella nulla. Una cache cancellata fa solo sì che la sua sessione rilegga il prompt una volta. I limiti sono facoltativi:
  un cron job sull'host fa lo stesso lavoro se non ti serve un tetto alla dimensione, per esempio
  `find /srv/ai/slots -name '*.bin' -mtime +2 -delete` (una cache in uso viene salvata di nuovo ogni volta che la sua
  sessione viene sostituita, quindi la sua età ricomincia a contare da capo). I file cancellati dall'esterno non danno
  problemi all'hook; uno mancante è semplicemente un miss.
- **Dopo che cambi il modello di una route**, cancella i suoi file nella cartella (i nomi iniziano con la route): uno stato
  salvato con altri pesi non si adatta al nuovo modello.
- **Ogni cambio costa un salvataggio e un ripristino.** Con l'[annotazione automatica](books/annotations.md) attiva, a una
  parte seguono un'annotazione e una riscrittura, quindi un turno può cambiare slot tre o quattro volte. Misura un turno
  prima di farci affidamento.
- **Un riavvio del proxy** dimentica quale sessione è nello slot; la prima richiesta dopo di esso ripristina sopra lo slot
  il suo file, e quello che lo slot conteneva va perso.
- **Un modello che il router ha scaricato senza che l'hook lo sappia** (un `models_max` sbagliato, un altro client) lascia lo
  slot vuoto mentre l'hook crede ancora che vi sia l'ultima sessione. Una richiesta per la stessa sessione rilegge
  semplicemente il prompt; una richiesta per un'altra sessione salva prima lo slot vuoto sopra il file di quella sessione,
  quindi anche la sua cache va persa. In entrambi i casi si paga solo un prefill.
- **`llama-server` non registra un salvataggio o un ripristino semplice** (registra i checkpoint di contesto che aggiunge o
  ripristina, e gli errori). Ciò che ha fatto è nella sua risposta, e il log del proxy (con `LITELLM_LOG=INFO`) lo riporta,
  con una riga per ogni chiamata che l'hook fa, per esempio `slot_cache: https://… restore <file> (openai/model): ok (21920
  tokens, 2580 MB, 3.1 s)`, `… save <file> …: ok (…)` oppure `… restore … miss (400)`; il miss della prima richiesta di una
  sessione è normale (llama-server lo registra come "Unable to restore slot, no available space in KV cache or invalid slot
  save file"). I problemi reali sono gli avvisi: un certificato che non corrisponde all'indirizzo, una api key rifiutata, un
  server che non risponde.
- **Dove il prompt differisce.** Per ogni richiesta successiva alla prima di una sessione, il log ha una riga che dice quanto il
  prompt è uguale all'ultimo di quella sessione, per esempio `slot_cache: <route>_<session> prompt: 14 messages, the last one
  had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`. Un server può
  riusare la sua cache solo fino a quel punto (un messaggio vale circa tre o quattro caratteri per token), quindi è la riga da
  leggere quando una seconda richiesta rilegge quasi tutto: il messaggio che nomina è quello il cui inizio è cambiato. La riga
  confronta anche l'ultimo messaggio dei due prompt, che contiene ciò che cambia (il lore, la parte, il compito): quando la
  storia è cresciuta di una parte, il vecchio ultimo messaggio non è più nello stesso punto, e il numero di caratteri che i
  due ultimi messaggi hanno in comune dice quanto, per esempio, il lore è uguale nei due.
- **Se non succede nulla**, guarda il log del proxy (`docker logs litellm-proxy 2>&1 | grep slot_cache`): con l'hook in
  funzione c'è una riga per ogni chiamata, e una richiesta di Marginalia per una route senza `slot_cache` viene segnalata una
  volta sola (`not used for <route>: no model_info.slot_cache`). Se non c'è alcuna riga `slot_cache`, l'hook non è stato
  eseguito: il container ha ancora un file o una configurazione più vecchi. Un singolo file montato in un container
  (`./slot_cache.py:/app/slot_cache.py`) continua a mostrare la versione che aveva quando il container è stato creato se un
  editor ha sostituito il file; `docker compose up -d --force-recreate` prende quello nuovo (un semplice riavvio no).
  `docker exec litellm-proxy grep -c save_idle_seconds /app/slot_cache.py` stampa 0 per un file troppo vecchio.
- **In Docker** il proxy e `llama-server` sono due container, quindi la cartella di `--slot-save-path` è una directory
  dell'host che entrambi montano (il proxy deve poterci scrivere: cancella i file vecchi e rinnova l'età di quelli usati);
  il file dell'hook va accanto alla configurazione di LiteLLM, e `api_base` è un indirizzo che il proxy può raggiungere, con
  il nome host del certificato quando il server usa TLS:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  con `save_path: /slots` e `models_max: 1` nello `slot_cache` della route.
- L'hook usa `CustomLogger.async_pre_call_deployment_hook`, che LiteLLM chiama dopo aver scelto il deployment e prima di
  inviare la richiesta (la versione 1.106.5 è stata verificata con un router reale e un server di prova); controllalo con la
  versione che usi. Un proxy che cambia modello dietro un unico indirizzo riavviando un solo server (llama-swap) non è
  supportato: le chiamate agli slot raggiungerebbero qualunque modello sia caricato. Nulla di tutto questo è stato eseguito
  contro un vero router `llama-server`; i test usano un server di prova che controlla le chiamate fatte dall'hook.
<a id="freeing-the-gpu-for-another-model"></a>

## Liberare la GPU per un altro modello

Una GPU che ospita il modello linguistico non ha spazio per un modello di immagini o di sintesi vocale accanto. Con `llama-server` in modalità router, un secondo hook può scaricare i modelli su richiesta, quindi un plugin che ha bisogno della GPU (il [Generatore di immagini](extensions/image-generator.md#one-gpu-for-the-model-and-the-images), l'Audiolibro) la richiede con una richiesta al proxy, con la stessa chiave che usa per tutto il resto:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

Una richiesta con l'intestazione `X-Llama-Unload` riceve risposta dall'hook stesso (HTTP 200, il testo `unloaded: <names>`) e non arriva mai a un modello. L'hook elenca i modelli del router (`GET /models`), scarica quelli caricati (`POST /models/unload` con il nome) e attende finché il router non segnala che nessuno è più in memoria (`loaded`, `loading` e `unloading` occupano la memoria), per al massimo `LLAMA_UNLOAD_WAIT_SECONDS` (120). Se il server non è raggiungibile, risponde con un errore o i modelli sono ancora presenti dopo l'attesa, la risposta è un errore (HTTP 502), così un plugin che invia questa richiesta come richiesta *before* non avvia l'altro modello su una GPU piena. Il modello torna da solo: la richiesta ordinaria successiva fa caricare il modello al router, quindi la richiesta *after* di un plugin può essere vuota per il modello linguistico. Il nome del modello nel corpo deve solo essere uno che una route accetta (con la route `openai/*` inizia con `openai/`, per esempio `openai/unload`); conta l'intestazione. Un nome che nessuna route accetta riceve un 400 *dopo* che i modelli sono stati scaricati, perché l'hook viene eseguito prima che LiteLLM controlli il nome. Lo scaricamento va a ogni server la cui route ha `unload`, non a un solo modello.

L'hook è uno script a sé, `gpu_unload.py`, accanto a `slot_cache.py` e concatenato con esso. Sono indipendenti: `gpu_unload.py` funziona anche senza l'hook della cache. Quando sono concatenati entrambi, `gpu_unload.py` trova l'hook della cache tra i callback di LiteLLM e chiama il suo `release_server`, che **salva prima la cache di ogni slot e ne dimentica il contenuto**, così che la richiesta successiva ripristini nel modello ricaricato la cache della sua sessione. (Senza quella chiamata, l'hook della cache crederebbe ancora che la sessione sia nello slot e non ripristinerebbe nulla: l'intero prompt verrebbe letto di nuovo dopo ogni scambio.)

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

In Docker il file deve essere nel container come lo è `slot_cache.py`: aggiungi `- ./gpu_unload.py:/app/gpu_unload.py:ro` ai `volumes` del proxy, accanto alla riga di `slot_cache.py`. Un file che non c'è si mostra all'avvio come `ImportError: Could not import proxy_handler_instance from gpu_unload` (il motivo reale, `FileNotFoundError` o un errore dello script stesso, si trova poche righe sopra nel log).

Concatena i due in `litellm_config.yml` e contrassegna la route con `unload`:

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

Nel Generatore di immagini (o in qualsiasi plugin che ha le due richieste) imposta la richiesta *before* come `POST` all'indirizzo sopra, con le intestazioni `Authorization: Bearer <key>` e `X-Llama-Unload: all` e il corpo dell'esempio; le intestazioni sono memorizzate cifrate.

Il server dell'altro modello può essere controllato attraverso lo stesso proxy. LiteLLM inoltra un percorso a un altro indirizzo (`general_settings: pass_through_endpoints`, `path`, `target` e `include_subpath: true`; la chiave del proxy viene verificata, e l'intestazione `Authorization` del client **non** viene inoltrata, quindi il target ha il proprio segreto in `headers`). Un piccolo servizio accanto al server di sintesi vocale o di immagini che ferma e avvia il suo container è un target del genere: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control) (`POST /<container>/start` risponde quando il servizio al suo interno è pronto, `stop` quando è terminato; il suo README contiene l'esempio di compose e la route LiteLLM per il server di sintesi vocale, `/v1/audio/speech`).

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

Cosa è stato verificato: LiteLLM 1.108.3 con un router di prova che riporta `loaded`, `unloading` e `unloaded` (l'attesa tratteneva la risposta finché lo stato non era `unloaded`, un server non raggiungibile dava 502, con entrambi gli hook concatenati lo slot veniva salvato prima dello scaricamento e ripristinato dalla richiesta successiva, con solo `gpu_unload.py` non veniva chiamato nient'altro, e il pass-through raggiungeva il suo target, aggiungeva il proprio valore di `headers` al posto di uno inviato dal client, e rifiutava una richiesta senza la chiave). È stato eseguito anche contro un vero router `llama-server` dietro LiteLLM (un modello da 31B): la richiesta ha restituito `unloaded: <model>` dopo da 1 a 2 secondi, il router ha registrato `stopping model instance` e `exited with status 0` (il processo è terminato, quindi la memoria è libera), una seconda richiesta con nulla caricato ha restituito `unloaded: nothing` immediatamente, e la richiesta ordinaria successiva ha impiegato circa un minuto perché il router ha ricaricato il modello. Se il tuo `GET /models` usa nomi diversi per gli stati, modifica `IN_MEMORY` nello script.
