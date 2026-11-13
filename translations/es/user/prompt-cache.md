---
label: Caché de prompts e identificador de sesión
order: 975
icon: database
---

# Caché de prompts e identificador de sesión

Los servidores de modelos conservan el prompt que acaban de leer (la *caché de prompts*), para que la siguiente petición que empiece con el mismo texto no tenga que leerlo de nuevo. En una historia larga, esa diferencia va de esperar unos segundos a esperar minutos. Una caché solo ayuda mientras el nuevo prompt empiece igual que el anterior (véase [Por qué una caché puede no ayudar](#why-a-cache-may-not-help)). Marginalia también puede indicar al servidor **qué tipo de trabajo es una petición**, de modo que un servidor, o un proxy delante de él, pueda mantener una caché para cada tipo.

<a id="what-is-sent"></a>

## Qué se envía

Cada petición que Marginalia hace para un tipo de trabajo lleva la cabecera HTTP

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

El valor es el mismo para el mismo usuario haciendo el mismo tipo de trabajo, sea cual sea el libro, y es distinto entre usuarios y entre tipos:

| Tipo | Peticiones |
|---|---|
| generación | Una parte nueva, *Regenerar* y *Variante*: sus prompts comparten la historia, así que comparten el valor. |
| impersonate | *Impersonate*. |
| anotación | *Anotar con IA* y la anotación automática; el segundo modelo del modo de dos etapas tiene un valor propio. |
| reescritura | *Reescribir* y la reescritura de la anotación automática. |
| resumen | Resúmenes. |
| agente | Las peticiones de un [agente](agents.md); cada ejecución tiene un valor propio, porque hace crecer su propia conversación. |
| ayuda | El asistente de ayuda. |
| plugins | Cada plugin tiene el suyo: *Reviewer*, *Side Query* (sus títulos de pestaña tienen otro), *Lore Generator*, *Arc Outline*, *Converter* y *Manga Generator*. |

Las peticiones que no pertenecen a ningún tipo (listar los modelos, probar la conexión, contar tokens) no envían la cabecera.

El valor es un hash con clave del usuario y del tipo (la clave viene de `secret.key` en la [carpeta de datos](administration/index.md#the-data-folder)). No revela ni al usuario ni al libro, nadie puede deducir a quién pertenece, y otra instalación da un valor distinto para el mismo usuario. Hay un valor por usuario y tipo, no uno por libro, así que un servidor nunca necesita mantener más cachés que esas.

Todos los proveedores la envían: los compatibles con OpenAI, y los proveedores [Anthropic Claude](extensions/anthropic.md) y [Google Gemini](extensions/google-genai.md). Un servidor que no use la cabecera la ignora. No hay ningún ajuste para desactivarla.

<a id="why-a-cache-may-not-help"></a>

## Por qué una caché puede no ayudar

Un servidor reutiliza la parte de la caché en la que el nuevo prompt es igual al anterior **desde el primer token**; la primera diferencia pone fin a ese tramo, y todo lo que viene después se vuelve a leer, aunque buena parte no haya cambiado. Mire la línea de `llama-server` que aparece al empezar una petición:

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` es la proporción del prompt en caché que la nueva petición conserva. Cerca de 1, la caché se usa; aquí solo se usa el 15 %, así que el prompt difiere del almacenado desde muy pronto, tanto si la caché se restauró desde disco como si nunca salió del slot.

Los modelos con **ventana deslizante** (Gemma, por ejemplo) y los modelos híbridos no pueden eliminar el final de su caché y continuar desde la mitad. Según el código fuente de `llama.cpp`, `llama-server` solo puede continuar desde un prompt en caché cuando el nuevo prompt lo extiende exactamente, o cuando existe un **punto de control de contexto** en el lugar donde ambos difieren o antes. Crea puntos de control solo al inicio de los mensajes del usuario (separados al menos 8192 tokens) y poco antes del final del prompt, y los guarda en el archivo del slot (las líneas INFO `appended N context checkpoint(s)` y `restored N context checkpoint(s)` indican cuándo lo hace). Sin un punto de control utilizable, vuelve a leer el prompt completo, sin importar lo que diga `f_keep`, y solo lo informa en el nivel de traza (`-lv 4`: `forcing full prompt re-processing due to lack of cache data`). Por eso la historia de una generación, de una anotación y de una reescritura se envía como una cadena de mensajes, cada parte como una respuesta del modelo tras un mensaje del usuario: el último mensaje del usuario tiene un punto de control, que es donde la siguiente petición empieza a diferir, y una parte nueva solo añade mensajes al final. Para leer un prefill en frío en el registro: la primera línea `prompt processing, n_tokens = 4096` aparece aquí tras unos 7 s; si se hubiera reutilizado un prefijo, aparecería tras uno o dos segundos.

El [prompt](books/prompts.md#how-the-prompt-is-put-together) empieza por el **mensaje de sistema**, y la historia va después, así que lo que haya en el mensaje de sistema debe mantenerse igual de una petición a otra. En la plantilla maestra predeterminada, el mensaje de sistema contiene, en este orden, el bloque de lore, los resúmenes y las notas del autor (anotaciones). Cuando cualquiera de ellos cambia entre dos peticiones, lo cual es habitual (el texto nuevo activa otras entradas de lore, se añade un resumen, se añade o se resuelve una nota), la caché solo es válida hasta ese punto y toda la historia que hay detrás se vuelve a leer. Las partes más antiguas salen primero del prompt cuando la historia ya no cabe, lo que desplaza el inicio de la historia una parte en cada turno. Lo que ayuda:

- Dé a las entradas que cambian de una parte a otra el modo de inserción **Antes del prompt de usuario** ([Orden e inserción](lorebooks/entries.md#order-and-insertion)): se colocan al final del prompt, detrás de la historia, y no alteran la caché. Deje en el bloque de lore lo que siempre es cierto.
- Saque de la plantilla maestra los bloques que no use (las notas del autor, por ejemplo, si no da notas al modelo), para que el mensaje de sistema no cambie.
- Abra **Comparar prompt** en el menú de una parte para ver dónde termina la caché: compara el prompt de la parte con el de una parte anterior (por defecto, la más cercana; el cuadro enumera las partes anteriores a ella y sus variantes) y marca el primer mensaje que difiere. Véase [Comparar prompt](#compare-prompt).

<a id="compare-prompt"></a>

### Comparar prompt

**Comparar prompt** en el menú de una parte (escritorio y teléfono) responde a la pregunta *¿por qué no se usó la caché para esta parte?* Enumera los mensajes de los dos prompts (el mensaje de sistema, luego un mensaje del usuario y una respuesta del modelo por cada parte, y después el mensaje del usuario del turno), con un estado para cada uno: igual, cambiado, añadido o eliminado. El primer mensaje que difiere aparece resaltado, y los siguientes se marcan como *Leído de nuevo*, porque un servidor vuelve a leer todo lo que va después de la primera diferencia. Encima de la tabla:

- dónde está la primera diferencia (mensaje y carácter) y qué parte del prompt es igual desde el principio, en caracteres y aproximadamente en tokens;
- una interpretación de la diferencia: el **mensaje de sistema** difiere (el jailbreak, la plantilla, el lore, los resúmenes o las notas cambiaron entre las dos peticiones: la causa habitual), una parte **en medio de la historia** difiere (se editó, se regeneró o se reemplazó por un resumen), el **inicio de la historia se movió** (las partes más antiguas se dejaron fuera o se resumieron), o la diferencia es solo el **último mensaje del prompt anterior**, el mensaje del usuario de ese turno, que la historia no repite. Este último caso es lo máximo que puede reutilizar una parte siguiente o una variante; si el servidor aun así no usó su caché, la causa no es el prompt (revise el slot, el modelo y el identificador de sesión).

Al seleccionar un mensaje se muestran los dos textos uno al lado del otro, desde poco antes de la primera diferencia. Solo se comparan los mensajes: el modelo, las herramientas de la petición y el slot del servidor no forman parte del prompt guardado. La parte necesita un prompt guardado (las partes escritas por el autor y las generadas antes de que se guardaran los prompts no tienen).

Los tipos de petición que se indican a continuación importan para lo que se conserva al cambiar entre ellos; el prompt decide cuánto de ello se usa.
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## Uso: una sola ranura en disco con llama.cpp

El valor es para ti si el servidor se queda sin memoria para las cachés. Un `llama-server` local con memoria suficiente para varias
ranuras puede mantener una caché para cada tipo de trabajo y no necesita nada más. Con memoria para **una** ranura, cualquier
otro tipo de trabajo tira la caché de la historia: la anotación de una parte, la reescritura posterior y la siguiente generación
vuelven a leer sus prompts cada vez.

La salida consiste en guardar la caché de cada tipo en disco y cargarla en la única ranura cuando llega una petición de ese tipo.
`llama-server` puede guardar una ranura en un archivo y restaurarla; un proxy delante de él sabe de qué tipo es una petición por la
cabecera. El ejemplo de abajo es un enganche para [LiteLLM](https://docs.litellm.ai/).

**Requisito:** un `llama-server` de 2026-10-08 o posterior (llama.cpp PR #26004, *preserve context checkpoints across
slot save/restore*). Uno anterior escribe y restaura la memoria de la ranura pero no sus [puntos de control de contexto](#why-a-cache-may-not-help), así que un modelo con ventana deslizante (Gemma, por ejemplo) o un modelo híbrido vuelve a leer el prompt entero después de cada restauración, por mucho que no haya cambiado. Comprueba la versión con `llama-server --version`;
una imagen de contenedor con una etiqueta móvil (`full`, `server`...) no se actualiza hasta que la descargas.

Arranca `llama-server` con una ranura y una carpeta para los archivos:

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

Guarda esto como `slot_cache.py` junto a la configuración de LiteLLM:

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

y actívalo en el `config.yaml` de LiteLLM: el callback, y `slot_cache` en el `model_info` de la ruta que va al
`llama-server`. Una entrada comodín basta; no hace falta listar ningún modelo en LiteLLM (el servidor los conoce y
el cliente nombra el que quiere):

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

El enganche no necesita dirección propia: usa el `api_base` y el `api_key` de la ruta a la que va la petición (las
ranuras están en la raíz del servidor, así que se quita un `/v1` final). Solo se tocan las rutas con `slot_cache`; una petición
para cualquier otro modelo, uno en la nube por ejemplo, pasa sin cambios aunque lleve la cabecera. Varios servidores
funcionan a la vez, cada uno con su propia ranura. Con el comodín, la ruta es el modelo que pidió el cliente, así que los archivos se
guardan por modelo (`qwen3-32b`), y aun así puedes listar algunos modelos por nombre junto a él si necesitan otros ajustes. LiteLLM toma la parte de un nombre de modelo anterior a un `/` como proveedor, así que para nombres que contienen uno (`org/model`, como en un preset del router) usa la ruta
`model_name: "openai/*"` y da a Marginalia el modelo como `openai/org/model`: el enganche quita el `openai/`, y los
archivos y las llamadas a las ranuras usan el nombre que conoce el servidor. Las
rutas y su `model_info` salen de `model_list`: un proxy arrancado sin una (solo con `litellm --model ...`) no tiene
ninguna, y el enganche no hace nada allí. La entrada comodín única es el reemplazo.

**`llama-server` en modo router** (muchos modelos tras una dirección, cargados bajo demanda) funciona: cada modelo es una instancia
propia con sus propias ranuras, así que el enganche guarda el estado por servidor, modelo y ranura, y nombra el modelo en el cuerpo
de sus llamadas a las ranuras (el nombre que usó el cliente, sin el `openai/` de LiteLLM; el ajuste `model` da otro, para una
ruta cuyo nombre no es el modelo). Necesita un llama.cpp de febrero de 2026 o posterior, que reenvía `/slots` a la
instancia (antes respondía `400 Invalid action`).

Cuando el router puede tener cargados solo algunos de sus modelos (`--models-max`, 4 por defecto), pedir otro hace que
descargue el modelo usado hace más tiempo, y la ranura de ese modelo, con la caché dentro, desaparece. El enganche sigue el mismo
orden (pon `models_max` en `slot_cache`, o `LLAMA_MODELS_MAX`, con el mismo número que `--models-max`; 0 para sin límite)
y guarda esa ranura **antes** de la petición que hace que el router la descargue. Para eso el proxy debe ser el único cliente
del router: una petición que llegue por otro camino no se ve. Un guardado nunca carga un modelo que el router ha descargado.
Lo que hace: la caché de una historia se escribe en disco **cuando la ranura hace falta para otra** (una petición de
otro tipo, otro modelo, o el router descargando el modelo), no después de cada petición: escribir 3 GB tras cada
generación solo ralentizaría el servidor. Así, un archivo aparece tras el primer cambio; por ejemplo, generar, anotar,
generar: la anotación guarda primero la generación. Con `save_idle_seconds` en `slot_cache` (o
`LLAMA_SAVE_IDLE_SECONDS`) la caché también se escribe cuando no llega ninguna petición durante ese tiempo, así la carpeta la
tiene poco después de que dejes de trabajar; una petición reinicia la espera, y una caché que ya está en disco no se vuelve a
escribir en el siguiente cambio. El enganche envía un guardado, una restauración o un borrado solo cuando la ranura está libre (consulta
`GET /slots`, `is_processing`; sin ese endpoint lo envía de inmediato): `llama-server` retiene una llamada de ese tipo mientras
corre una petición y libera solo una retenida cada vez que termina una petición, así que una petición de otro cliente que llegó
detrás podría esperar allí para siempre. El servidor no hace nada más mientras escribe un guardado.

Los detalles: cuando llega una petición de otra historia distinta a la que está en la ranura, el enganche guarda la ranura con el
nombre de la historia que estaba dentro, restaura el archivo de la nueva (o vacía la ranura si todavía no hay archivo),
y deja seguir la petición. Los archivos se guardan **por ruta** (el `model_name`): un estado guardado solo encaja con el modelo que
lo escribió, así que el archivo lleva el nombre de la ruta y de la historia, y dos rutas nunca comparten uno, aunque usen el
mismo servidor y carpeta. El servidor luego solo lee lo que difiere del prompt restaurado. Una caché que no se puede
restaurar, un archivo que ya no está o un servidor que no responde solo significa que el prompt se vuelve a leer; la petición
en sí nunca falla por culpa del enganche.

Cosas que conviene saber:

- **Tamaño.** Un archivo contiene la memoria de la ranura **y sus puntos de control de contexto**, así que ocupa lo que ambos
  juntos: para una historia larga y un modelo grande, cientos de megabytes a varios gigabytes. Un ejemplo medido: un modelo de 31 B con un
  prompt de 18.000 tokens generó archivos de 1,3 a 11 GB, el mayor con unos 18 puntos de control de unos 450 MB cada uno además de
  unos 1,5 GB de caché. Un servidor guarda hasta 32 puntos de control por defecto, y cada cambio escribe y lee todos. 
  `--ctx-checkpoints N` de `llama-server` lo limita, lo que hace los archivos más pequeños y los cambios más cortos, pero un punto de control
  ayuda solo cuando el siguiente prompt difiere por primera vez de la caché en él o justo después. Una parte nueva o un swipe difieren
  al inicio del último mensaje del usuario (el punto de control más reciente), mientras que una anotación o una reescritura puede
  cambiar una parte varias partes atrás, así que conserva tantos puntos de control como alcance la historia (mira las líneas `prompt:`
  del enganche, indican dónde difiere cada petición). Hay un archivo por ruta, usuario y tipo, y cada uno se sobrescribe en su sitio,
  así que la carpeta se queda así de pequeña.
- **Velocidad.** Comprueba que un cambio es más rápido que leer el prompt (el enganche registra el tamaño y el tiempo de cada guardado
  y restauración). El ejemplo de arriba, en un disco NVMe con una AMD Radeon AI PRO R9700: guardar 11 GB tardó 6 s y restaurar
  7,6 GB para 20.000 tokens, 8 s; unos 14 s por cambio, mientras que volver a leer un prompt de esa longitud tarda unos 45 s
  a 440 tokens por segundo, y el prefill crece con el prompt. Sin puntos de control, esos mismos archivos ocupaban unos 1 GB y
  un cambio tardaba 5 s.
- **Memoria.** Los puntos de control de la ranura se guardan en la **memoria del sistema** del servidor (no en la memoria de la GPU),
  hasta `--ctx-checkpoints` de ellos (32 por defecto, cientos de megabytes cada uno para un modelo grande), y un guardado o una
  restauración lee o escribe todos. `llama-server` también guarda los prompts de las ranuras que ha dejado en una caché de memoria
  propia (`--cache-ram`, 8 GiB por defecto); los archivos del enganche hacen el mismo trabajo, así que puede ponerse a 0 en una máquina
  con poca memoria. Cuando la máquina empieza a usar la swap, los guardados y restauraciones se vuelven varias veces más lentos, porque
  los puntos de control hay que leerlos primero desde la swap: mira `free -h` antes que el disco.
- **Límites.** Sin límite, la carpeta crece con el número de rutas, usuarios y tipos. Pon `max_files` (un número de
  archivos) y/o `max_gib` (su tamaño total) en el `slot_cache` de una ruta (o `LLAMA_CACHE_MAX_FILES` y
  `LLAMA_CACHE_MAX_GIB` en el entorno del proxy para todas las rutas) y el enganche borra, tras cada guardado, las cachés **de esa ruta**
  que hace más tiempo que se **usaron**: restaurar una caché cuenta como usarla. Nunca borra la caché que acaba de guardar ni la que
  está a punto de restaurar (así que una ruta con dos historias alternas conserva al menos dos archivos, pase lo que pase con el límite),
  y solo toca los archivos de la ruta, así que las otras rutas y los demás archivos de la carpeta están a salvo. Una sola caché mayor que
  `max_gib` se conserva (con un aviso en el registro).
  Borrar necesita la carpeta: pon `save_path` (o `LLAMA_SLOT_SAVE_PATH`) en la carpeta de `--slot-save-path` tal como la ve el proxy;
  con `llama-server` en un contenedor, es un volumen que montan ambos contenedores. Sin eso no se borra nada. Una caché borrada solo
  significa que su historia vuelve a leer su prompt una vez. Los límites son opcionales: un trabajo cron en el host
  hace lo mismo si no necesitas un tope de tamaño, por ejemplo `find /srv/ai/slots -name '*.bin' -mtime +2 -delete`
  (una caché en uso se vuelve a guardar cada vez que su historia sale de la ranura, así que su antigüedad empieza de nuevo). Los archivos
  borrados desde fuera no afectan al enganche; uno que falte es un fallo de caché.
- **Tras cambiar el modelo de una ruta**, borra sus archivos de la carpeta (sus nombres empiezan por la ruta): un estado guardado
  con otros pesos no sirve para el modelo nuevo.
- **Cada cambio cuesta un guardado y una restauración.** Con la [anotación automática](books/annotations.md) activada, a una parte le siguen una
  anotación y una reescritura, así que un turno puede cambiar de ranura tres o cuatro veces. Mide un turno antes de confiar en él.
- **Un reinicio del proxy** olvida qué historia está en la ranura: la primera petición después restaura su archivo sobre la ranura,
  y lo que tenía la ranura se pierde.
- **Un modelo que el router descargó sin que el enganche lo supiera** (un `models_max` incorrecto, otro cliente) deja la ranura
  vacía mientras el enganche sigue creyendo que la última historia está dentro. Una petición de la misma historia simplemente vuelve
  a leer su prompt; una petición de otra historia guarda antes la ranura vacía sobre el archivo de esa historia, así que también se pierde
  su caché. Ambos casos solo cuestan un prefill.
- **`llama-server` no registra un guardado o una restauración simples** (sí registra los puntos de control de contexto que añade o
  restaura, y los errores). Lo que hizo está en su respuesta, y el registro del proxy (con `LITELLM_LOG=INFO`) lo tiene, con una línea por
  cada llamada que hace el enganche, por ejemplo `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB,
  3.1 s)`, `… save <file> …: ok (…)` o `… restore … miss (400)`; el fallo de la
  primera petición de una historia es normal (llama-server lo registra como "Unable to restore slot, no available space in KV
  cache or invalid slot save file"). Los problemas reales son avisos: un certificado que no coincide con la dirección, una clave de API
  rechazada, un servidor que no responde.
- **Dónde difiere el prompt.** Para cada petición posterior a la primera de una historia, el registro tiene una línea que dice hasta dónde
  es igual el prompt al último de esa historia, por ejemplo `slot_cache: <route>_<session> prompt: 14 messages, the last
  one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`. Un servidor solo puede
  reutilizar su caché hasta ahí (un mensaje son unos tres o cuatro caracteres por token), así que esta es la línea que leer cuando una segunda
  petición vuelve a leer casi todo: el mensaje que nombra es aquel cuyo inicio cambió. La línea también compara el último mensaje de
  los dos prompts, que contiene lo que cambia (el lore, la parte, la tarea): cuando la historia creció en una parte, el antiguo último
  mensaje ya no está en el mismo lugar, y el número de caracteres que comparten los dos últimos mensajes indica hasta qué punto el lore, por ejemplo, es igual en ambos.
- **Si no pasa nada**, mira el registro del proxy (`docker logs litellm-proxy 2>&1 | grep slot_cache`): con el enganche en marcha hay una
  línea por cada llamada, y una petición de Marginalia para una ruta sin `slot_cache` se informa una vez
  (`not used for <route>: no model_info.slot_cache`). Si no hay ninguna línea `slot_cache`, el enganche no se ejecutó: el contenedor
  todavía tiene un archivo o una configuración antiguos. Un único archivo montado en un contenedor (`./slot_cache.py:/app/slot_cache.py`)
  sigue mostrando la versión que tenía cuando se creó el contenedor si un editor reemplazó el archivo; `docker compose up -d
  --force-recreate` carga el nuevo (un simple reinicio no). `docker exec litellm-proxy grep -c save_idle_seconds
  /app/slot_cache.py` imprime 0 para un archivo demasiado antiguo.
- **En Docker** el proxy y `llama-server` son dos contenedores, así que la carpeta de `--slot-save-path` es un directorio del
  host que ambos montan (el proxy necesita escribir allí: borra los archivos viejos y renueva la antigüedad de los usados), el
  archivo del enganche va junto a la configuración de LiteLLM, y `api_base` es una dirección que el proxy puede alcanzar, con el nombre de host
  del certificado cuando el servidor usa TLS:

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  con `save_path: /slots` y `models_max: 1` en el `slot_cache` de la ruta.
- El enganche usa `CustomLogger.async_pre_call_deployment_hook`, que LiteLLM llama después de elegir el despliegue y
  antes de enviar la petición (la versión 1.106.5 se comprobó con un router real y un servidor de sustitución); compruébalo con la
  versión que ejecutas. Un proxy que cambia de modelo tras una misma dirección reiniciando un único servidor (llama-swap) no
  está soportado: las llamadas a las ranuras llegarían al modelo que esté cargado. Nada de esto se ejecutó contra un router `llama-server`
  real; las pruebas usan un sustituto que comprueba las llamadas que hace el enganche.
<a id="freeing-the-gpu-for-another-model"></a>

## Liberar la GPU para otro modelo

Una GPU que tiene cargado el modelo de lenguaje no tiene espacio para un modelo de imagen o de voz junto a él. Con `llama-server` en modo router, un segundo hook puede descargar los modelos cuando se le pide. Así, un plugin que necesita la GPU (el [Generador de imágenes](extensions/image-generator.md#one-gpu-for-the-model-and-the-images), el Audiobook) la pide con una petición al proxy, usando la misma clave que emplea para todo lo demás:

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

Una petición con la cabecera `X-Llama-Unload` la responde el propio hook (HTTP 200, con el texto `unloaded: <names>`) y nunca llega a un modelo. El hook lista los modelos del router (`GET /models`), descarga los que están cargados (`POST /models/unload` con el nombre) y espera hasta que el router informa de que ninguno ocupa ya memoria (`loaded`, `loading` y `unloading` retienen la memoria), como máximo `LLAMA_UNLOAD_WAIT_SECONDS` (120). Si no se puede contactar con el servidor, si responde con un error, o si los modelos siguen ahí tras la espera, la respuesta es un error (HTTP 502). Así, un plugin que envía esto como su petición *antes* no arranca el otro modelo en una GPU llena. El modelo vuelve solo: la siguiente petición normal hace que el router lo cargue, por lo que la petición *después* de un plugin puede estar vacía para el modelo de lenguaje. El nombre del modelo en el cuerpo solo tiene que ser uno que acepte alguna ruta (con la ruta `openai/*` empieza por `openai/`, por ejemplo `openai/unload`); lo que cuenta es la cabecera. Un nombre que ninguna ruta acepta recibe un 400 *después* de que los modelos se hayan descargado, porque el hook se ejecuta antes de que LiteLLM mire el nombre. La descarga llega a todos los servidores cuya ruta tenga `unload`, no a un solo modelo.

El hook es un script propio, `gpu_unload.py`, junto a `slot_cache.py` y encadenado con él. Son independientes: `gpu_unload.py` funciona sin el hook de caché. Cuando están encadenados los dos, `gpu_unload.py` encuentra el hook de caché entre los callbacks de LiteLLM y llama a su `release_server`, que **guarda primero la caché de cada slot y olvida lo que había en ella**, de modo que la siguiente petición restaure en el modelo recargado la caché de su sesión. (Sin esa llamada, el hook de caché seguiría creyendo que la sesión está en el slot y no restauraría nada: todo el prompt se volvería a leer tras cada cambio.)

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

En Docker el archivo debe estar en el contenedor igual que `slot_cache.py`: añade `- ./gpu_unload.py:/app/gpu_unload.py:ro` a los `volumes` del proxy, junto a la línea de `slot_cache.py`. Un archivo que no está ahí aparece al arrancar como `ImportError: Could not import proxy_handler_instance from gpu_unload` (la causa real, `FileNotFoundError` o un error del propio script, está unas líneas más arriba en el log).

Encadena los dos en `litellm_config.yml` y marca la ruta con `unload`:

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

En el Generador de imágenes (o en cualquier plugin que tenga las dos peticiones), configura la petición *antes* como `POST` a la dirección de arriba, con las cabeceras `Authorization: Bearer <key>` y `X-Llama-Unload: all`, y el cuerpo del ejemplo. Las cabeceras se guardan cifradas.

El servidor del otro modelo también se puede controlar a través del mismo proxy. LiteLLM retransmite una ruta a otra dirección (`general_settings: pass_through_endpoints`, `path`, `target` e `include_subpath: true`; la clave del proxy se comprueba, y la cabecera `Authorization` del cliente **no** se reenvía, así que el destino tiene su propio secreto en `headers`). Un pequeño servicio junto al servidor de voz o de imagen que detiene e inicia su contenedor es un destino de este tipo: [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control) (`POST /<container>/start` responde cuando el servicio que contiene está listo, y `stop` cuando ha terminado; su README tiene el ejemplo de compose y la ruta de LiteLLM para el servidor de voz, `/v1/audio/speech`).

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

Lo que se ha comprobado: LiteLLM 1.108.3 con un router de sustitución que informa de `loaded`, `unloading` y `unloaded` (la espera retuvo la respuesta hasta que el estado era `unloaded`; un servidor caído dio 502; con los dos hooks encadenados, el slot se guardó antes de la descarga y la siguiente petición lo restauró; con solo `gpu_unload.py` no se llamó a nada más; y el pass-through llegó a su destino, añadió su propio valor de `headers` en lugar del enviado por el cliente y rechazó una petición sin la clave). También se ejecutó contra un router `llama-server` real tras LiteLLM (un modelo de 31B): la petición devolvió `unloaded: <model>` tras 1 a 2 segundos; el router registró `stopping model instance` y `exited with status 0` (el proceso ya no existe, así que la memoria está libre); una segunda petición, sin nada cargado, devolvió `unloaded: nothing` al instante; y la siguiente petición normal tardó alrededor de un minuto porque el router volvió a cargar el modelo. Si tu `GET /models` nombra los estados de otra forma, cambia `IN_MEMORY` en el script.
