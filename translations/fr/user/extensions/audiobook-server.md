---
label: Mise en place du serveur de synthèse vocale
order: 28
---

# Mise en place du serveur de synthèse vocale pour l'Audiobook

L'extension [Audiobook](audiobook.md) a besoin d'un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licence MIT) qu'elle peut joindre en HTTP. Cette page traite de la partie serveur. Ce qui est décrit ici a été testé sur une seule machine (une AMD Radeon AI PRO R9700
avec 32 Go, LiteLLM devant tout le reste, `llama-server` en mode routeur) ; votre matériel et votre proxy peuvent demander d'autres valeurs.

<a id="which-setup-is-yours"></a>

## Quelle est votre configuration ?

| Votre situation | Quoi faire |
|---|---|
| Le serveur de synthèse vocale tourne sur **une autre machine**, sur le CPU, ou sur une carte qui a la place de le loger **à côté du modèle de langage** | Lancez le serveur, indiquez son adresse dans un backend, **laissez vides les requêtes avant et après un lot**. Rien d'autre n'est nécessaire. |
| Le serveur de synthèse vocale et le modèle de langage **partagent une seule carte** et ne tiennent pas ensemble | Il vous faut les requêtes avant et après un lot : **avant** libère la carte et démarre le serveur de synthèse vocale, **après** l'arrête. Le reste de cette page. |
| Le modèle de langage est un modèle dans le cloud | Rien à libérer : lancez le serveur de synthèse vocale où vous voulez et laissez les requêtes vides. |

En résumé : **les requêtes avant et après ne sont nécessaires que lorsque les deux modèles se relaient sur un même GPU.** Elles servent à organiser ce relais : l'extension les exécute une fois autour d'un *lot* (tout ce que vous avez lancé d'un seul clic), et non autour de chaque partie.

<a id="1-the-speech-server"></a>

## 1. Le serveur de synthèse vocale

Lancez Chatterbox-TTS-Server dans un conteneur (ou depuis les sources) de façon que `GET /api/model-info` réponde. Points importants :

- **Le modèle.** Turbo (`chatterbox-turbo`) est la valeur par défaut de l'extension et celui qui sonne le mieux lors des tests ; le modèle multilingue n'est nécessaire que pour un texte dans une autre langue que celle du livre. Le modèle se règle dans le `config.yaml` du serveur
  (ou via `POST /save_settings` et `POST /restart_server`). Le *Speech model* du backend doit être le modèle que le serveur a chargé après les requêtes avant un lot.
- **Le japonais exige `pykakasi` dans l'image.** Sans lui, le serveur journalise *pykakasi not available - Japanese text processing skipped* et lit mal les kanjis. Ajoutez `RUN python3 -m pip install --no-cache-dir pykakasi` à son Dockerfile (avant `COPY . .`). Le même saut silencieux existe pour l'hébreu, le russe et le chinois.
- **Les voix** sont des fichiers du dossier `reference_audio/` du serveur (montez-le en volume, pour qu'ils survivent). L'extension envoie elle-même les extraits de vos acteurs (`POST /upload_reference`) avant un lot, sous un nom construit à partir du hash de l'extrait. Le serveur n'a pas d'appel de suppression : les extraits s'accumulent.
- Le serveur n'a **aucune authentification**. Ne publiez pas son port ; placez un proxy devant lui s'il n'est pas sur un réseau privé.
- `restart: "no"` pour un conteneur que quelque chose d'autre démarre à la demande. `restart: unless-stopped` le démarre au boot et monopolise la carte pour rien.

Dans le backend (*Settings → Extension Settings → Audiobook Settings*), indiquez son adresse, cochez *The server is on this machine or on my network* si c'est le cas, puis appuyez sur **Test connection** : c'est *the server answers, the model is loaded* que vous voulez voir.
L'extension parle à `/tts` (l'ensemble des paramètres : voix, langue, exaggeration, CFG weight, temperature, speed, seed) ainsi qu'à `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files` et `/upload_reference`. Si un proxy se trouve entre vous et le serveur, il doit laisser passer toutes ces routes.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## Un seul GPU pour le modèle de langage et la synthèse vocale

Une carte qui porte le modèle de langage n'a pas de place pour le modèle de synthèse vocale. Le plan est le même quel que soit le proxy :

```
avant un lot :   1. décharger le modèle de langage
                 2. démarrer le serveur de synthèse vocale (et attendre qu'il soit prêt ; charger son modèle s'il démarre vide)
la synthèse :    toutes les pièces de toutes les parties du lot
après un lot :   3. arrêter le serveur de synthèse vocale (le modèle de langage se recharge tout seul à la requête suivante)
```

**L'ordre n'est pas négociable.** Démarrer le conteneur de synthèse pendant que le modèle de langage occupe la carte a saturé la mémoire de l'hôte lors du test
(30 Go de RAM, le swap, puis l'OOM killer a emporté d'autres services) : le déchargement **doit passer en premier**, et la première requête qui échoue arrête la liste, si bien que rien ne démarre sur une carte pleine. Les requêtes *après* un lot sont toutes exécutées, quoi qu'il arrive à l'une d'elles.

Pourquoi *arrêter* le conteneur plutôt que décharger le modèle à l'intérieur du serveur : un déchargement interne au serveur laissait environ 1 Go de la carte occupé après de nombreux changements de modèle ; arrêter le conteneur libère tout.

<a id="the-requests"></a>

### Les requêtes

Chaque requête des listes est une méthode, une adresse, des en-têtes, un corps et une attente après coup (en secondes). N'écrivez pas la clé dans les en-têtes (le formulaire affiche les en-têtes tels qu'ils sont saisis) : placez-la dans **Key of the requests around a batch**, ou laissez ce champ vide et la *Api key* du backend est envoyée. Elle part sous la forme `Authorization: Bearer` avec chaque requête ci-dessous qui n'a pas la sienne. Avec un LiteLLM
devant tout le reste et une seule clé, les listes sont les suivantes :

| | Méthode et adresse | En-têtes | Corps | Rôle |
|---|---|---|---|---|
| Avant 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Décharge le modèle de langage ; répondue par le crochet lui-même, prend 1 à 2 s. |
| Avant 2 | `POST http://litellm:4000/gpu/tts/start` | | | Démarre le conteneur de synthèse vocale et répond quand le serveur est prêt (environ 13 s). |
| Avant 3 (seulement si le serveur démarre sans modèle) | `POST http://litellm:4000/gpu/tts/load` | | | Charge le modèle (8 à 14 s, 50 s la première fois, le temps du téléchargement). |
| Après 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Arrête le conteneur (moins d'une seconde). |

Le modèle de langage revient tout seul : la requête ordinaire suivante fait charger le modèle par le routeur (environ une minute pour un gros modèle ; avec le [crochet de cache de prompts](../prompt-cache.md), le cache de votre session est restauré), donc aucune requête après le lot n'est nécessaire pour lui.

Sans LiteLLM, les mêmes requêtes vont vers ce qui démarre et arrête vos conteneurs ; la seule règle à respecter est l'ordre indiqué plus haut.

<a id="what-answers-those-addresses"></a>

### Ce qui répond à ces adresses

- **`X-Llama-Unload`** est pris en charge par le crochet `gpu_unload.py` de LiteLLM, décrit avec son code dans
  [Libérer le GPU pour un autre modèle](../prompt-cache.md#freeing-the-gpu-for-another-model) (il fonctionne sans le crochet de cache). Avec une route `openai/*`, le nom du modèle dans le corps doit commencer par `openai/` : un autre nom reçoit une 400 *après* que les modèles ont été déchargés.
- **`/gpu/tts/start`, `/stop` et `/load`** sont des **points de terminaison pass-through** de LiteLLM vers le petit contrôleur `tools/gpu-control` du dépôt, qui démarre et arrête les conteneurs par leur nom. Son
  README contient le fichier compose avec le **proxy de socket Docker** (ainsi le contrôleur peut démarrer et arrêter des conteneurs, et rien d'autre ; la socket elle-même donne les droits root sur l'hôte) ainsi que les réglages faciles à mal configurer : `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. Dans LiteLLM :

  ```yaml
  general_settings:
    pass_through_endpoints:
      - path: "/gpu/tts/start"
        target: "http://gpu-control:8000/chatterbox/start"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/stop"
        target: "http://gpu-control:8000/chatterbox/stop"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/load"
        target: "http://chatterbox:8004/restart_server"
  ```

  (le conteneur s'appelle `chatterbox`, et `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indique au contrôleur ce qu'il doit attendre ; le jeton du contrôleur reste dans son `.env` et dans l'environnement de LiteLLM, Marginalia ne possède que la clé de LiteLLM).
- **Tous les conteneurs et LiteLLM partagent un réseau Docker** (`docker network create ai`, une seule fois). Ajoutez `networks: [default, ai]` et le réseau externe `ai` dans chaque fichier compose : un `docker network connect` fait à la main se perd lorsqu'un conteneur est recréé.

<a id="checking-it"></a>

## Vérification

1. `curl http://<serveur de synthèse vocale>/api/model-info` répond (`loaded` vaut true après un démarrage).
2. *Test connection* dans le backend indique que le modèle est chargé. Si les requêtes avant un lot sont la seule chose qui démarre le serveur, testez-le avec les listes en place : *the server answers, no model is loaded* est la réponse d'un serveur arrêté puis redémarré qui doit encore charger son modèle.
3. Convertissez une partie courte dans **Convert to audio**. La fenêtre affiche les morceaux en cours de fabrication ; ensuite, le conteneur de synthèse vocale doit être arrêté, et `nvidia-smi`, `nvtop` ou `rocm-smi` doivent montrer la carte de nouveau libre.
4. Si le premier appel après le chargement d'un modèle est long, c'est le moteur qui se réchauffe (une fois, 28 s lors du premier essai, et plus jamais revu).
<a id="troubleshooting"></a>

## Dépannage

| Symptôme | Cause |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Le serveur a démarré vide (un script de démarrage qui décharge le modèle une fois) : ajoutez la requête **Before 3**. |
| Les premiers morceaux sont produits, puis le serveur répond *Model not loaded* et son journal affiche `Request received for /api/unload` | Quelqu'un d'autre que Marginalia décharge le modèle (l'extension n'appelle jamais `/api/unload`) : en général un script de démarrage dans le conteneur qui attend que le serveur réponde puis décharge le modèle « pour garder un démarrage léger ». Son attente ne peut se terminer qu'après le premier morceau, car le serveur mono-thread était occupé, et le déchargement tombe alors au milieu du lot (une trace d'expiration `urllib` dans le journal du conteneur signale cette attente). Le motif à chercher dans le fichier compose est une `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Remplacez-la par `command: ["python3", "server.py"]` : un simple démarrage donne un Turbo chargé, et le déchargement ne servait qu'à libérer la carte après un démarrage, ce dont un conteneur que le contrôleur démarre et arrête n'a pas besoin. Pour savoir qui l'a envoyé, lisez le journal de l'extension (*Admin → Extensions → Log*) : il liste chaque requête avant et après un lot avec son adresse et l'heure de sa réponse, ainsi que le début et la fin des morceaux. Un déchargement qui arrive pendant que le journal indique que le lot tourne, ou depuis une adresse absente de la liste, ne vient pas de l'extension. |
| Le lot s'arrête au milieu (le journal de l'extension indique *Cannot reach*, ou le journal du serveur s'interrompt simplement) et un redémarrage à la main fonctionne | Le serveur est mort, généralement tué par le système parce qu'il a pris toute la mémoire (`dmesg \| grep -i oom` sur l'hôte : un processus `python3` de plusieurs Go). L'extension le redémarre alors elle-même (les requêtes après le lot, puis celles avant) et refait le morceau une seule fois ; si le serveur ne peut pas être relancé, ou disparaît de nouveau aussitôt, le reste du lot échoue immédiatement et le journal indique pourquoi. Pour éviter qu'un serveur qui fuit n'en arrive là, réglez **Restart the server after (pieces)** dans le backend (par exemple 100), ainsi qu'un `mem_limit` sur le conteneur pour que le système ne tue que lui. |
| La conversion échoue aussitôt avec la réponse du proxy | La première requête avant le lot a échoué (mauvaise clé, route de déchargement introuvable) ; la liste s'arrête là volontairement. |
| L'hôte manque de mémoire au début du lot | Le modèle de langue était encore sur la carte : la requête de déchargement est absente ou n'est pas la première. |
| Le japonais est lu comme du charabia | `pykakasi` est absent de l'image du serveur ; le backend doit aussi être la version multilingue pour ce morceau. |
| L'envoi d'un clip échoue derrière un proxy | Le proxy doit transmettre un corps multipart (`/upload_reference`) sans le modifier. Non testé avec le pass-through de LiteLLM ; utilisez un fichier de voix du serveur (`predefined:Name.wav`) en attendant que cela fonctionne. |
| Le lot d'un autre utilisateur décharge le modèle sous le vôtre | Les lots sur une même adresse de serveur partagent un verrou interne à Marginalia, et un lot pour un autre modèle de synthèse attend que les autres soient terminés. Un serveur de synthèse et, par exemple, un serveur d'images sur une même carte ont besoin du même nom dans **GPU (name)** sur les deux backends pour s'attendre mutuellement (le Générateur d'images aussi). Deux installations de Marginalia sur une même carte ne partagent pas de verrou. |
