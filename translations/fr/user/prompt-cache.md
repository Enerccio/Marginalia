---
label: Cache de prompt et identifiant de session
order: 975
icon: database
---

# Cache de prompt et identifiant de session

Les serveurs de modèles conservent le prompt qu'ils viennent de lire (le *cache de prompt*), afin que la requête suivante qui commence par le même texte n'ait pas à le relire. Pour une longue histoire, c'est la différence entre attendre quelques secondes et attendre plusieurs minutes. Un cache n'aide que dans la mesure où le nouveau prompt commence comme l'ancien (voir [Pourquoi un cache peut ne pas aider](#why-a-cache-may-not-help)). Marginalia peut aussi indiquer au serveur **de quel type de travail relève une requête**, afin qu'un serveur, ou un proxy placé devant lui, puisse garder un cache pour chaque type.

<a id="what-is-sent"></a>

## Ce qui est envoyé

Chaque requête que Marginalia effectue pour un type de travail porte l'en-tête HTTP

```
X-Session-ID: 3f9a0c1d5b7e4a2c8d6f1e0b9a7c5d3e
```

La valeur est la même pour un même utilisateur faisant un même type de travail, quel que soit le livre concerné, et elle diffère selon les utilisateurs et selon les types :

| Type | Requêtes |
|---|---|
| génération | Une nouvelle partie, *Régénérer* et *Variante* : leurs prompts partagent l'histoire, donc partagent la valeur. |
| impersonate | *Impersonate*. |
| annotation | *Annoter avec l'IA* et l'annotation automatique ; le second modèle du mode en deux étapes a une valeur qui lui est propre. |
| réécriture | *Réécrire* et la réécriture de l'annotation automatique. |
| résumé | Les résumés. |
| agent | Les requêtes d'un [agent](agents.md) ; chaque exécution a sa propre valeur, puisqu'elle fait croître sa propre conversation. |
| aide | L'assistant d'aide. |
| plugins | Chaque plugin a la sienne : *Reviewer*, *Side Query* (ses titres d'onglet sont différents), *Lore Generator*, *Arc Outline*, *Converter* et *Manga Generator*. |

Les requêtes qui n'appartiennent à aucun type (lister les modèles, tester la connexion, compter les tokens) n'envoient pas d'en-tête.

La valeur est un condensé à clé calculé à partir de l'utilisateur et du type (la clé provient de `secret.key` dans le [dossier de données](administration/index.md#the-data-folder)). Elle ne révèle ni l'utilisateur ni le livre, personne ne peut deviner à qui elle appartient, et une autre installation donne une autre valeur pour le même utilisateur. Il y a une valeur par utilisateur et par type, non par livre, si bien qu'un serveur n'a jamais à garder plus de caches que cela.

Tous les fournisseurs l'envoient : ceux compatibles OpenAI, ainsi que les fournisseurs [Anthropic Claude](extensions/anthropic.md) et [Google Gemini](extensions/google-genai.md). Un serveur qui n'utilise pas l'en-tête l'ignore. Il n'existe aucun réglage pour le désactiver.

<a id="why-a-cache-may-not-help"></a>

## Pourquoi un cache peut ne pas aider

Un serveur réutilise la partie du cache où le nouveau prompt est identique à l'ancien **dès le tout premier token** ; la première différence met fin à la réutilisation, et tout ce qui suit est relu, même si une bonne partie est inchangée. Regardez la ligne de `llama-server` affichée au début d'une requête :

```
selected slot by LCP similarity, sim_best = 0.163, f_keep = 0.151
```

`f_keep` est la part du prompt en cache que la nouvelle requête conserve. Proche de 1, le cache est utilisé ; ici, seuls 15 % le sont, donc le prompt diffère du prompt en cache dès le début, que le cache ait été restauré depuis le disque ou qu'il n'ait jamais quitté l'emplacement.

Les modèles à **fenêtre glissante** (Gemma, par exemple) et les modèles hybrides ne peuvent pas retirer la fin de leur cache pour repartir du milieu. D'après le code source de `llama.cpp`, `llama-server` ne peut reprendre à partir d'un prompt en cache que lorsque le nouveau prompt le prolonge exactement, ou lorsqu'un **point de contrôle de contexte** existe à l'endroit où les deux divergent ou avant. Il ne crée de points de contrôle qu'au début des messages de l'utilisateur (à au moins 8192 tokens d'intervalle) et peu avant la fin du prompt, et les garde dans le fichier de slot (les lignes INFO `appended N context checkpoint(s)` et `restored N context checkpoint(s)` indiquent quand cela se produit). Sans point de contrôle utilisable, il relit tout le prompt, quel que soit le `f_keep`, et ne le signale qu'au niveau trace (`-lv 4` : `forcing full prompt re-processing due to lack of cache data`). C'est pourquoi l'histoire d'une génération, d'une annotation et d'une réécriture est envoyée comme une chaîne de messages, chaque partie étant une réponse du modèle après un message de l'utilisateur : le dernier message de l'utilisateur reçoit un point de contrôle, c'est là que la requête suivante commence à différer, et une nouvelle partie n'ajoute que des messages à la fin. Pour lire un pré-remplissage à froid dans le journal : la première ligne `prompt processing, n_tokens = 4096` apparaît ici après environ 7 s ; si un préfixe avait été réutilisé, elle apparaîtrait au bout d'une ou deux secondes.

Le [prompt](books/prompts.md#how-the-prompt-is-put-together) commence par le **message système**, et l'histoire vient après ; ce qui se trouve dans le message système doit donc rester le même d'une requête à l'autre. Dans le modèle maître par défaut, le message système contient, dans cet ordre, le bloc de lore, les résumés et les notes de l'auteur (annotations). Lorsque l'un d'eux change entre deux requêtes, ce qui est courant (le nouveau texte active d'autres entrées de lore, un résumé est ajouté, une note est ajoutée ou résolue), le cache n'est valable que jusqu'à ce point, et toute l'histoire qui le suit est relue. Les parties les plus anciennes sortent du prompt en premier lorsque l'histoire ne tient plus, ce qui décale le début de l'histoire d'une partie à chaque tour. Ce qui aide :

- Donnez aux entrées qui changent d'une partie à l'autre le mode d'insertion **Avant le prompt utilisateur** ([Ordre et insertion](lorebooks/entries.md#order-and-insertion)) : elles vont à la fin du prompt, après l'histoire, et ne perturbent pas le cache. Gardez dans le bloc de lore ce qui est toujours vrai.
- Retirez du modèle maître les blocs que vous n'utilisez pas (les notes de l'auteur, par exemple, si vous ne donnez pas de notes au modèle), afin que le message système reste le même.
- Ouvrez **Comparer le prompt** dans le menu d'une partie pour voir où le cache s'arrête : il compare le prompt de la partie à celui d'une partie antérieure (la plus proche par défaut ; la zone liste les parties qui la précèdent et ses variantes) et marque le premier message qui diffère. Voir [Comparer le prompt](#compare-prompt).

<a id="compare-prompt"></a>

### Comparer le prompt

**Comparer le prompt** dans le menu d'une partie (ordinateur et téléphone) répond à la question *pourquoi le cache n'a-t-il pas été utilisé pour cette partie ?* Il liste les messages des deux prompts (le message système, puis un message de l'utilisateur et une réponse du modèle par partie, puis le message de l'utilisateur du tour) avec un statut pour chacun : identique, modifié, ajouté ou supprimé. Le premier message qui diffère est mis en évidence, et ceux qui suivent sont marqués *Relu*, car un serveur relit tout ce qui suit la première différence. Au-dessus du tableau :

- l'endroit de la première différence (message et caractère) et la part du prompt identique depuis le début, en caractères et environ en tokens ;
- une lecture de la différence : le **message système** diffère (le jailbreak, le modèle, le lore, les résumés ou les notes ont changé entre les deux requêtes : la cause habituelle), une partie **au milieu de l'histoire** diffère (elle a été modifiée, régénérée ou remplacée par un résumé), le **début de l'histoire a bougé** (les parties les plus anciennes ont été retirées ou résumées), ou la différence ne porte que sur le **dernier message du prompt précédent**, le message de l'utilisateur de ce tour, que l'histoire ne répète pas. Ce dernier cas est le plus qu'une partie suivante ou une variante puisse réutiliser ; si le serveur n'a tout de même pas utilisé son cache, le prompt n'en est pas la cause (vérifiez le slot, le modèle et l'identifiant de session).

Sélectionner un message affiche les deux textes côte à côte, à partir juste avant la première différence. Seuls les messages sont comparés : le modèle, les outils de la requête et le slot du serveur ne font pas partie du prompt enregistré. La partie doit avoir un prompt enregistré (les parties saisies par l'auteur et les parties générées avant que les prompts ne soient conservés n'en ont pas).

Les types de requêtes ci-dessous comptent pour ce qui est conservé lorsque vous passez de l'un à l'autre ; le prompt détermine la part qui est utilisée.
<a id="using-it-one-slot-on-disk-with-llamacpp"></a>

## Utilisation : un seul slot sur disque avec llama.cpp

La valeur de cette approche est pour vous si le serveur manque de mémoire pour les caches. Un `llama-server` local avec assez de mémoire pour plusieurs slots peut garder un cache pour chaque type de travail et n'a besoin de rien d'autre. Avec la mémoire pour **un seul** slot, chaque autre type de travail efface le cache de l'histoire : l'annotation d'une partie, la réécriture qui suit et la génération suivante relisent leurs prompts à chaque fois.

La solution consiste à garder le cache de chaque type sur disque et à le replacer dans l'unique slot lorsqu'une requête de ce type arrive. `llama-server` peut enregistrer un slot dans un fichier et le restaurer ; un proxy placé devant lui reconnaît le type de la requête grâce à l'en-tête. L'exemple ci-dessous est un crochet pour [LiteLLM](https://docs.litellm.ai/).

**Prérequis :** un `llama-server` daté du 2026-10-08 ou plus récent (llama.cpp PR #26004, *preserve context checkpoints across slot save/restore*). Une version plus ancienne écrit et restaure la mémoire du slot mais pas ses [points de contrôle du contexte](#why-a-cache-may-not-help) ; un modèle à fenêtre glissante (Gemma, par exemple) ou un modèle hybride relit alors tout le prompt après chaque restauration, même si une grande partie n'a pas changé. Vérifiez la version avec `llama-server --version` ; une image de conteneur avec une étiquette mobile (`full`, `server`...) n'est pas mise à jour tant que vous ne la tirez pas.

Lancez `llama-server` avec un seul slot et un dossier pour les fichiers :

```sh
llama-server -m model.gguf -c 65536 -np 1 --slot-save-path /var/cache/llama-slots
```

Enregistrez ce fichier sous le nom `slot_cache.py`, à côté de la configuration de LiteLLM :

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

et activez-le dans `config.yaml` de LiteLLM : le callback, et `slot_cache` dans le `model_info` de la route qui mène au `llama-server`. Une seule entrée générique suffit, aucun modèle n'a besoin d'être listé dans LiteLLM (le serveur les connaît, et le client nomme celui qu'il veut) :

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

Le crochet n'a pas besoin d'adresse propre : il utilise le `api_base` et l'`api_key` de la route vers laquelle va la requête (les slots sont à la racine du serveur, donc un `/v1` final est retiré). Seules les routes avec `slot_cache` sont concernées ; une requête pour un autre modèle, un modèle cloud par exemple, passe sans changement même si elle porte l'en-tête. Plusieurs serveurs fonctionnent côte à côte, chacun avec son propre slot. Avec le générique, la route est le modèle que le client a demandé, donc les fichiers sont rangés par modèle (`qwen3-32b`), et vous pouvez toujours lister par leur nom certains modèles à côté, s'ils ont besoin d'autres réglages. LiteLLM lit la partie d'un nom de modèle avant le `/` comme le fournisseur ; pour les noms qui en contiennent un (`org/model`, comme dans un preset du routeur), utilisez donc la route `model_name: "openai/*"` et donnez à Marginalia le modèle sous la forme `openai/org/model` : le crochet retire le `openai/`, et les fichiers comme les appels de slot utilisent le nom que le serveur connaît. Les routes et leur `model_info` viennent de `model_list` : un proxy lancé sans elle (seulement `litellm --model ...`) n'en a aucun, et le crochet n'y fait rien. L'unique entrée générique en tient lieu.

**`llama-server` en mode routeur** (plusieurs modèles derrière une même adresse, chargés à la demande) fonctionne : chaque modèle est une instance à part, avec ses propres slots. Le crochet garde donc l'état par serveur, par modèle et par slot, et nomme le modèle dans le corps de ses appels de slot (le nom utilisé par le client, sans le `openai/` de LiteLLM ; le réglage `model` en donne un autre, pour une route dont le nom n'est pas celui du modèle). Il faut un llama.cpp de février 2026 ou plus récent, qui transmet `/slots` à l'instance (avant, il répondait `400 Invalid action`).

Quand le routeur ne peut garder que certains de ses modèles (`--models-max`, 4 par défaut), en demander un autre le fait décharger le modèle utilisé il y a le plus longtemps, et le slot de ce modèle, avec le cache qu'il contenait, disparaît. Le crochet suit le même ordre (réglez `models_max` dans `slot_cache`, ou `LLAMA_MODELS_MAX`, au même nombre que `--models-max` ; 0 pour aucune limite) et enregistre ce slot **avant** la requête qui fait décharger le modèle par le routeur. Pour cela, le proxy doit être le seul client du routeur : une requête qui l'atteint autrement n'est pas vue. Un enregistrement ne charge jamais un modèle que le routeur a déchargé. Voici ce qu'il fait : le cache d'une session est écrit sur disque **lorsque le slot est nécessaire à une autre** (une requête d'un autre type, un autre modèle, ou le déchargement du modèle par le routeur), et non après chaque requête ; une écriture de 3 Go après chaque génération ralentirait seulement le serveur. Ainsi, un fichier apparaît après le premier changement, par exemple génération, annotation, génération : l'annotation enregistre d'abord la génération. Avec `save_idle_seconds` dans `slot_cache` (ou `LLAMA_SAVE_IDLE_SECONDS`), le cache est aussi écrit lorsqu'aucune requête n'est arrivée depuis ce délai, si bien que le dossier le contient peu après que vous avez cessé de travailler ; une requête relance l'attente, et un cache déjà présent sur disque n'est pas réécrit au changement suivant. Le crochet n'envoie un enregistrement, une restauration ou un effacement que lorsque le slot est inactif (il interroge `GET /slots`, `is_processing` ; sans ce point d'accès, il envoie immédiatement) : `llama-server` met en attente un tel appel pendant qu'une requête tourne, et ne libère qu'un appel en attente à chaque fin de requête, si bien qu'une requête d'un autre client arrivée derrière pourrait attendre indéfiniment. Le serveur ne fait rien d'autre pendant qu'un enregistrement écrit.

Les détails : lorsqu'une requête arrive d'une autre session que celle qui est dans le slot, le crochet enregistre le slot sous le nom de la session qui s'y trouvait, restaure le fichier de la nouvelle (ou vide le slot s'il n'existe pas encore de fichier), puis laisse la requête suivre son cours. Les fichiers sont tenus **par route** (le `model_name`) : un état enregistré ne convient qu'au modèle qui l'a écrit, donc le fichier porte le nom de la route et de la session, et deux routes ne partagent jamais un fichier, même si elles utilisent le même serveur et le même dossier. Le serveur ne relit alors que ce qui diffère du prompt restauré. Un cache qui ne peut pas être restauré, un fichier disparu ou un serveur qui ne répond pas signifient seulement que le prompt est relu ; la requête elle-même n'échoue jamais à cause du crochet.

À savoir :

- **Taille.** Un fichier contient la mémoire du slot **et ses points de contrôle du contexte**, il pèse donc les deux ensemble : pour une longue histoire et un grand modèle, de quelques centaines de mégaoctets à plusieurs gigaoctets. Un exemple mesuré : un modèle de 31 B avec un prompt de 18 000 tokens a produit des fichiers de 1,3 à 11 Go, le plus gros avec environ 18 points de contrôle d'environ 450 Mo chacun, en plus d'environ 1,5 Go de cache. Un serveur garde jusqu'à 32 points de contrôle par défaut, et chaque échange écrit et lit tous ceux-ci. `--ctx-checkpoints N` de `llama-server` limite ce nombre, ce qui réduit la taille des fichiers et raccourcit les échanges, mais un point de contrôle n'aide que lorsque le prompt suivant diffère pour la première fois du prompt en cache à ce point ou juste après. Une nouvelle partie ou un swipe diffère au début du dernier message utilisateur (le point de contrôle le plus récent), alors qu'une annotation ou une réécriture peut changer une partie située plusieurs parties en arrière ; gardez donc autant de points de contrôle que l'histoire remonte en arrière (regardez les lignes `prompt:` du crochet, elles indiquent où chaque requête diffère). Il y a un fichier par route, par utilisateur et par type de travail, et chacun est réécrit sur place : le dossier reste donc aussi petit que cela.
- **Vitesse.** Vérifiez qu'un échange est plus rapide que la relecture du prompt (le crochet journalise la taille et la durée de chaque enregistrement et restauration). L'exemple ci-dessus, sur un disque NVMe avec une AMD Radeon AI PRO R9700 : un enregistrement de 11 Go a pris 6 s, et la restauration d'un fichier de 7,6 Go pour 20 000 tokens 8 s, soit environ 14 s pour un échange, alors que relire un prompt de cette longueur prend environ 45 s à 440 tokens par seconde, et le préremplissage croît avec le prompt. Sans points de contrôle, les mêmes fichiers faisaient environ 1 Go et un échange durait 5 s.
- **Mémoire.** Les points de contrôle du slot sont conservés dans la **mémoire système** du serveur (et non dans la mémoire du GPU), jusqu'à `--ctx-checkpoints` d'entre eux (32 par défaut, de quelques centaines de mégaoctets chacun pour un grand modèle), et un enregistrement ou une restauration lit ou écrit tous ces points. `llama-server` conserve aussi, dans un cache mémoire propre (`--cache-ram`, 8 GiB par défaut), les prompts des slots qu'il a abandonnés ; les fichiers du crochet remplissent le même rôle, si bien que ce réglage peut être mis à 0 sur une machine à court de mémoire. Lorsque la machine commence à utiliser son swap, les enregistrements et restaurations deviennent plusieurs fois plus lents, car les points de contrôle doivent d'abord être relus depuis le swap : regardez `free -h` avant de regarder le disque.
- **Limites.** Sans limite, le dossier grossit avec le nombre de routes, d'utilisateurs et de types de travail. Réglez `max_files` (un nombre de fichiers) et/ou `max_gib` (leur taille totale) dans le `slot_cache` d'une route (ou `LLAMA_CACHE_MAX_FILES` et `LLAMA_CACHE_MAX_GIB` dans l'environnement du proxy, pour toutes les routes) : après chaque enregistrement, le crochet supprime les caches **de cette route** qui ont été **utilisés** il y a le plus longtemps. Restaurer un cache compte comme l'utiliser. Il ne supprime jamais le cache qu'il vient d'enregistrer ni celui qu'il est sur le point de restaurer (ainsi, une route dont deux sessions alternent garde au moins deux fichiers, quelle que soit la limite), et il ne touche qu'aux fichiers de la route : les autres routes et les autres fichiers du dossier sont en sécurité. Un cache unique plus gros que `max_gib` est conservé (avec un avertissement dans le journal). La suppression demande le dossier : réglez `save_path` (ou `LLAMA_SLOT_SAVE_PATH`) sur le dossier de `--slot-save-path` tel que le voit le proxy ; avec `llama-server` dans un conteneur, c'est un volume que les deux conteneurs montent. Sans cela, rien n'est supprimé. Un cache supprimé signifie seulement que sa session relit son prompt une fois. Les limites sont facultatives : une tâche cron sur l'hôte convient aussi si vous n'avez pas besoin de plafonner la taille, par exemple `find /srv/ai/slots -name '*.bin' -mtime +2 -delete` (un cache en cours d'utilisation est réenregistré à chaque fois que sa session est écartée, son âge repart donc de zéro). Les fichiers supprimés de l'extérieur ne posent aucun problème au crochet ; un fichier absent est simplement un échec de cache.
- **Après avoir changé le modèle d'une route**, supprimez ses fichiers dans le dossier (leurs noms commencent par celui de la route) : un état enregistré avec d'autres poids ne convient pas au nouveau modèle.
- **Chaque changement coûte un enregistrement et une restauration.** Avec l'[annotation automatique](books/annotations.md) activée, une partie est suivie d'une annotation et d'une réécriture, si bien qu'un tour peut provoquer trois ou quatre échanges. Mesurez un tour avant de vous y fier.
- **Un redémarrage du proxy** oublie quelle session est dans le slot ; la première requête qui suit restaure son fichier par-dessus le slot, et ce que le slot contenait est perdu.
- **Un modèle que le routeur a déchargé sans que le crochet le sache** (un mauvais `models_max`, un autre client) laisse un slot vide alors que le crochet croit encore que la dernière session y est. Une requête pour la même session relit simplement son prompt ; une requête pour une autre session enregistre d'abord le slot vide par-dessus le fichier de cette session, si bien que son cache est perdu aussi. Dans les deux cas, cela ne coûte qu'un préremplissage.
- **`llama-server` ne journalise pas un simple enregistrement ou une simple restauration** (il journalise les points de contrôle du contexte qu'il ajoute ou restaure, et les erreurs). Ce qu'il a fait figure dans sa réponse, et le journal du proxy (avec `LITELLM_LOG=INFO`) le contient, avec une ligne pour chaque appel que fait le crochet, par exemple `slot_cache: https://… restore <file> (openai/model): ok (21920 tokens, 2580 MB, 3.1 s)`, `… save <file> …: ok (…)` ou `… restore … miss (400)` ; le « miss » de la première requête d'une session est normal (llama-server l'écrit ainsi : « Unable to restore slot, no available space in KV cache or invalid slot save file »). Les vrais problèmes sont signalés par des avertissements : un certificat qui ne correspond pas à l'adresse, une clé d'API refusée, un serveur qui ne répond pas.
- **Où le prompt diffère.** Pour chaque requête après la première d'une session, le journal contient une ligne qui indique à quel point le prompt est identique au dernier de cette session, par exemple `slot_cache: <route>_<session> prompt: 14 messages, the last one had 12: the first 11 are the same, message 12 (user) is the same for its first 2150 of 9031 characters`. Un serveur ne peut réutiliser son cache que jusque-là (un message représente environ trois ou quatre caractères par token) ; c'est donc la ligne à lire lorsqu'une deuxième requête relit presque tout : le message qu'elle nomme est celui dont le début a changé. La ligne compare aussi le dernier message des deux prompts, qui porte ce qui change (le lore, la partie, la consigne) : lorsque l'histoire a grandi d'une partie, l'ancien dernier message n'est plus à la même place, et le nombre de caractères que les deux derniers messages ont en commun indique à quel point le lore, par exemple, est identique dans les deux.
- **Si rien ne se passe**, regardez le journal du proxy (`docker logs litellm-proxy 2>&1 | grep slot_cache`) : lorsque le crochet fonctionne, il y a une ligne pour chaque appel, et une requête de Marginalia pour une route sans `slot_cache` est signalée une fois (`not used for <route>: no model_info.slot_cache`). Aucune ligne `slot_cache` du tout signifie que le crochet n'a pas été exécuté : le conteneur contient encore un ancien fichier ou une ancienne configuration. Un fichier unique monté dans un conteneur (`./slot_cache.py:/app/slot_cache.py`) continue d'afficher la version qu'il avait à la création du conteneur si un éditeur a remplacé le fichier ; `docker compose up -d --force-recreate` prend la nouvelle version (un simple redémarrage ne suffit pas). `docker exec litellm-proxy grep -c save_idle_seconds /app/slot_cache.py` affiche 0 pour un fichier trop ancien.
- **Dans Docker**, le proxy et `llama-server` sont deux conteneurs ; le dossier de `--slot-save-path` est donc un répertoire de l'hôte que les deux montent (le proxy doit y écrire : il supprime les anciens fichiers et renouvelle l'âge des fichiers utilisés), le fichier du crochet va à côté de la configuration de LiteLLM, et `api_base` est une adresse que le proxy peut joindre, avec le nom d'hôte du certificat lorsque le serveur utilise TLS :

  ```yaml
  # llama-server                              # litellm
  volumes:                                    volumes:
    - /srv/ai/slots:/slots                      - ./litellm_config.yml:/app/config.yml:ro
  command: >                                    - ./slot_cache.py:/app/slot_cache.py:ro
    ... -np 1 --models-max 1                    - /srv/ai/slots:/slots
    --slot-save-path /slots
  ```

  avec `save_path: /slots` et `models_max: 1` dans le `slot_cache` de la route.
- Le crochet utilise `CustomLogger.async_pre_call_deployment_hook`, que LiteLLM appelle après avoir choisi le déploiement et avant d'envoyer la requête (la version 1.106.5 a été vérifiée avec un vrai routeur et un serveur de remplacement) ; vérifiez-le avec la version que vous utilisez. Un proxy qui change de modèle derrière une même adresse en redémarrant un seul serveur (llama-swap) n'est pas pris en charge : les appels de slot atteindraient le modèle chargé, quel qu'il soit. Rien de cela n'a été exécuté sur un vrai routeur `llama-server` ; les tests utilisent un serveur de remplacement qui vérifie les appels que fait le crochet.
<a id="freeing-the-gpu-for-another-model"></a>

## Libérer le GPU pour un autre modèle

Un GPU qui héberge le modèle de langage n'a pas de place pour un modèle d'image ou de synthèse vocale à côté. Avec `llama-server` en mode routeur, un second
hook peut décharger les modèles sur demande : un plugin qui a besoin du GPU (le [Générateur d'images](extensions/image-generator.md#one-gpu-for-the-model-and-the-images),
l'Audiobook) le demande par une requête au proxy, avec la même clé que pour le reste :

```sh
curl http://litellm:4000/v1/chat/completions \
  -H "Authorization: Bearer $LITELLM_KEY" \
  -H "X-Llama-Unload: all" \
  -H "Content-Type: application/json" \
  -d '{"model": "qwen3-32b", "messages": [{"role": "user", "content": "unload"}]}'
```

Une requête avec l'en-tête `X-Llama-Unload` reçoit sa réponse du hook lui-même (HTTP 200, le texte `unloaded: <names>`) et n'atteint jamais
un modèle. Le hook liste les modèles du routeur (`GET /models`), décharge ceux qui sont chargés (`POST /models/unload` avec le nom) et
attend que le routeur indique qu'aucun n'est plus en mémoire (`loaded`, `loading` et `unloading` occupent la mémoire), pendant au plus
`LLAMA_UNLOAD_WAIT_SECONDS` (120). Si le serveur est injoignable, répond par une erreur, ou si les modèles sont toujours là après l'attente, la réponse est une
erreur (HTTP 502), afin qu'un plugin qui envoie cela comme requête *before* ne démarre pas l'autre modèle sur un GPU plein. Le modèle revient
tout seul : la requête ordinaire suivante fait charger le modèle par le routeur, si bien que la requête *after* d'un plugin peut être vide pour le modèle de langage. Le nom du modèle
dans le corps n'a qu'à être accepté par une route (avec la route `openai/*`, il commence par `openai/`, par exemple `openai/unload`) ; c'est l'en-tête qui compte.
Un nom qu'aucune route n'accepte reçoit un 400 *après* que les modèles ont été déchargés, parce que le hook s'exécute avant que LiteLLM examine le nom. Le déchargement concerne chaque serveur dont la route a `unload`, pas un seul modèle.

Le hook est un script à part, `gpu_unload.py`, à côté de `slot_cache.py` et chaîné avec lui. Ils sont indépendants : `gpu_unload.py` fonctionne
sans le hook de cache. Quand les deux sont chaînés, `gpu_unload.py` trouve le hook de cache parmi les callbacks de LiteLLM et appelle sa
`release_server`, qui **sauvegarde d'abord le cache de chaque slot puis oublie ce qu'il contenait**, afin que la requête suivante restaure dans le modèle rechargé le cache de sa session. (Sans cet appel, le hook de cache croirait toujours que la session est dans le slot et ne restaurerait rien : tout le prompt
serait relu après chaque échange de modèle.)

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

Dans Docker, le fichier doit se trouver dans le conteneur comme `slot_cache.py` : ajoutez `- ./gpu_unload.py:/app/gpu_unload.py:ro` aux `volumes` du proxy,
à côté de la ligne de `slot_cache.py`. Un fichier absent apparaît au démarrage sous la forme `ImportError: Could not import proxy_handler_instance from gpu_unload`
(la vraie raison, `FileNotFoundError` ou une erreur du script lui-même, figure quelques lignes plus haut dans le journal).

Chaînez les deux dans `litellm_config.yml` et marquez la route avec `unload` :

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

Dans le Générateur d'images (ou tout plugin qui a les deux requêtes), réglez la requête *before* sur `POST` vers l'adresse ci-dessus avec les en-têtes
`Authorization: Bearer <key>` et `X-Llama-Unload: all` et le corps de l'exemple ; les en-têtes sont stockés chiffrés.

Le serveur de l'autre modèle peut être piloté par le même proxy. LiteLLM relaie un chemin vers une autre adresse
(`general_settings: pass_through_endpoints`, `path`, `target` et `include_subpath: true` ; la clé du proxy est vérifiée, et
l'en-tête `Authorization` du client n'est **pas** transmis, si bien que la cible a son propre secret dans `headers`). Un petit
service à côté du serveur de synthèse vocale ou d'images, qui arrête et démarre son conteneur, est une telle cible : [`tools/gpu-control`](https://github.com/Enerccio/Marginalia/tree/master/tools/gpu-control)
(`POST /<container>/start` répond quand le service qu'il contient est prêt, `stop` quand il s'est arrêté ; son README contient l'exemple compose et la route LiteLLM
pour le serveur de synthèse vocale, `/v1/audio/speech`).

```yaml
general_settings:
  pass_through_endpoints:
    - path: "/gpu"
      target: "http://gpu-control:8000"
      include_subpath: true
      headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
```

Ce qui a été vérifié : LiteLLM 1.108.3 avec un routeur factice qui indique `loaded`, `unloading` et `unloaded` (l'attente retenait la réponse jusqu'à ce que l'état soit `unloaded`, un serveur arrêté donnait 502, avec les deux hooks chaînés le slot était sauvegardé avant le déchargement et restauré par la requête suivante, avec seulement
`gpu_unload.py` rien d'autre n'était appelé, et le pass-through atteignait sa cible, ajoutait sa propre valeur `headers` à la place d'une envoyée par le client, et refusait une requête sans la clé). Il a aussi été exécuté contre un vrai
routeur `llama-server` derrière LiteLLM (un modèle 31B) : la requête a renvoyé `unloaded: <model>` après 1 à 2 secondes, le routeur a journalisé
`stopping model instance` et `exited with status 0` (le processus a disparu, la mémoire est libre), une seconde requête sans rien de chargé
a renvoyé `unloaded: nothing` immédiatement, et la requête ordinaire suivante a pris environ une minute parce que le routeur a rechargé le modèle. Si votre
`GET /models` nomme les états autrement, modifiez `IN_MEMORY` dans le script.
