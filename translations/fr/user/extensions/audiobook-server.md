---
label: Mise en place du serveur de voix
order: 28
---

# Mise en place du serveur de voix pour l'Audiobook

L'extension [Audiobook](audiobook.md) a besoin d'un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licence MIT) qu'elle peut joindre en HTTP. Cette page traite de la partie serveur. Ce qui est décrit ici a été testé sur une seule machine (une AMD Radeon AI PRO R9700
de 32 Go, LiteLLM devant tout le reste, `llama-server` en mode routeur) ; votre matériel et votre proxy peuvent demander d'autres valeurs.

<a id="which-setup-is-yours"></a>

## Quelle est votre configuration ?

| Votre situation | Que faire |
|---|---|
| Le serveur de voix tourne sur **une autre machine**, sur le CPU, ou sur une carte qui a la place de l'accueillir **à côté du modèle de langage** | Lancez le serveur, indiquez son adresse dans un backend et **ne choisissez aucun verrou GPU**. Rien d'autre n'est nécessaire. |
| Le serveur de voix et le modèle de langage **partagent une même carte** et ne tiennent pas ensemble | Il vous faut un [verrou GPU](../administration/gpus.md) : ses étapes **acquire** libèrent la carte et démarrent le serveur de voix, ses étapes **release** l'arrêtent. Le reste de cette page vous concerne. |
| Le modèle de langage est un modèle cloud | Rien à libérer : lancez le serveur de voix où vous voulez et ne choisissez aucun verrou GPU. |

Donc : **les requêtes *pre* et *post* ne sont nécessaires que lorsque les deux modèles se relaient sur un même GPU.** Ce sont elles qui assurent ce relais : le plugin les envoie une fois pour toutes autour d'un *lot* (tout ce que vous lancez d'un clic), et non autour de chaque partie.

<a id="1-the-speech-server"></a>

## 1. Le serveur de voix

Lancez Chatterbox-TTS-Server dans un conteneur (ou à partir des sources) de façon que `GET /api/model-info` réponde. Points importants :

- **Le modèle.** Turbo (`chatterbox-turbo`) est la valeur par défaut de l'extension et celle qui sonne le mieux lors des tests ; le modèle multilingue n'est nécessaire que pour un texte dans une autre langue que celle du livre. Le modèle se règle dans le `config.yaml` du serveur
  (ou `POST /save_settings` et `POST /restart_server`). Le *Speech model* du backend doit être le modèle que le serveur a chargé après les étapes acquire de son verrou GPU.
- **Le japonais nécessite `pykakasi` dans l'image.** Sans lui, le serveur journalise *pykakasi not available - Japanese text processing skipped* et lit mal les kanjis. Ajoutez `RUN python3 -m pip install --no-cache-dir pykakasi` à son Dockerfile (avant `COPY . .`). La même omission silencieuse existe pour les paquets de l'hébreu, du russe et du chinois.
- **Les voix** sont des fichiers dans le dossier `reference_audio/` du serveur (montez-le en volume, pour qu'elles survivent). L'extension envoie elle-même les clips de vos acteurs (`POST /upload_reference`) avant un lot, sous un nom construit à partir du hash du clip. Le serveur n'a pas d'appel de suppression : les clips s'accumulent.
- Le serveur n'a **aucune authentification**. Ne publiez pas son port ; placez un proxy devant lui s'il n'est pas sur un réseau privé.
- `restart: "no"` pour un conteneur que quelque chose d'autre lance à la demande. `restart: unless-stopped` le démarre au boot et réserve la carte pour rien.

Dans le backend (*Settings → Extension Settings → Audiobook Settings*), indiquez son adresse, cochez *The server is on this machine or on my network* si c'est le cas, puis cliquez sur **Test connection** : c'est *the server answers, the model is loaded* que vous voulez voir.
L'extension parle à `/tts` (l'ensemble des paramètres : voix, langue, exaggeration, CFG weight, temperature, speed, seed) ainsi qu'à `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Si un proxy se trouve entre vous et le serveur, il doit transmettre tous ces appels.

<a id="higgs-audio-v3-in-comfyui"></a>

## Higgs Audio v3 dans ComfyUI

Un backend dont le modèle de voix est **Higgs Audio v3 in ComfyUI** ne parle pas à Chatterbox-TTS-Server, mais à un [ComfyUI](https://github.com/comfyanonymous/ComfyUI) équipé des nœuds de la
[TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite). Son adresse est celle de ComfyUI (par exemple
`http://gpu-server:8188`). Il a été installé sur la même AMD R9700, dans l'image `yanwk/comfyui-boot:rocm`.

1. **Installez la suite** dans le dossier `custom_nodes` de ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). Ses paquets Python vont dans le
   conteneur. Pour les conserver lorsque le conteneur est recréé, donnez à pip un dossier sur un volume : ajoutez `PIP_USER=true` à l'environnement du conteneur et montez
   `./storage/local:/root/.local`.
2. **Ne laissez jamais pip remplacer le torch de l'image** (une version CUDA remplacerait celle de ROCm). Écrivez les versions installées dans un fichier et installez avec celui-ci comme contrainte :
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   N'exécutez pas en bloc le `install.py` de la suite : il risque de réinstaller torch. `librosa` ne figure pas dans son `requirements.txt`, et la suite ne se charge pas sans lui.
3. **Le modèle** : `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (environ 8 Go), ou laissez
   la suite le télécharger la première fois. Il occupe environ 8 Go de la carte graphique.
4. **Test connection** dans le backend affiche *the server answers* avec le modèle du nœud lorsque ComfyUI et le nœud sont présents.

Ce que fait l'extension avec lui : elle envoie les clips du casting dans le dossier d'entrée de ComfyUI (`POST /upload/image`, sous un nom construit à partir du clip), liste
les voix de la suite et les clips avec `GET /object_info`, poste un workflow qui lui est propre vers `/prompt` pour chaque partie, attend sur `/history` et récupère l'audio
depuis `/view` : avec ffmpeg côté Marginalia, en flac, qu'elle découpe après le dernier mot, fait fondre en fin et stocke en mp3 ou en wav ; sans ffmpeg, en mp3 tel que fourni par ComfyUI, sans découpage (le stockage en wav ne fonctionne alors pas). **Après chaque partie, elle appelle `POST /free` avec
`{"free_memory": true}`** : sur la R9700, la troisième partie d'affilée sur le modèle chargé a fait planter la carte graphique (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI se ferme) ;
vider la mémoire entre les parties garde le modèle chargé et évite le problème, au prix d'environ 3 secondes par partie. ComfyUI est démarré et arrêté autour d'un lot avec le même type de requêtes que le serveur de voix ([les requêtes](#the-requests)).

**Utilisez fp32 sur une carte AMD.** Avec la précision `auto` (bf16 sur la R9700 avec ROCm 7.2), beaucoup de parties sortaient en charabia, en effets sonores (une phrase sur un coup de marteau devenait onze coups frappés) ou dans une mauvaise voix, et changer la seed ne faisait que modifier la chance. Les mêmes parties en fp32 étaient parfaites, mot pour mot. La **Precision** du backend vaut fp32 par défaut ; elle occupe environ 16 Go de la carte. La reconnaissance vocale Granite de la suite a le même problème en bf16 (charabia dès la deuxième reconnaissance), donc l'extension l'exécute aussi en float32.

Le moteur **Fish Audio S2 Pro** de la suite ne fonctionnait pas sur cette carte : son audio sortait sous forme de nombres invalides (NaN). Les poids de Higgs Audio v3 sont sous une licence de recherche et non commerciale.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## Un seul GPU pour le modèle et la parole

Une carte qui porte le modèle de langue n'a pas de place pour le modèle de parole. L'administrateur configure l'échange une seule fois dans [Admin → GPUs](../administration/gpus.md), avec un GPU et un verrou pour chaque élément pouvant être chargé dessus, puis choisit le verrou dans l'IA du modèle de langue et dans le backend de parole. Le plan est le même pour tout proxy :

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

Un lot (ou un échantillon) du backend de parole exécute les étapes d'acquisition du verrou de parole lorsque le GPU tient le modèle de langue, puis toutes les pièces ; le GPU reste chargé avec le serveur de parole après. La requête suivante vers le modèle de langue attend le lot, exécute l'étape de libération du verrou de parole (arrêt du serveur) puis poursuit. Plusieurs lots et échantillons à la suite ne paient l'échange qu'une fois.

**L'ordre n'est pas négociable.** Démarrer le conteneur de parole pendant que le modèle de langue occupe la carte a rempli la mémoire de l'hôte lors du test (30 Go de RAM, le swap, puis l'OOM killer a tué d'autres services) : le déchargement **doit venir en premier**. La première étape d'acquisition en échec met fin à la liste, donc rien ne démarre sur une carte pleine ; et lorsqu'une étape de libération échoue, le verrou suivant n'est pas du tout chargé (le GPU peut encore être plein) et le job échoue avec l'erreur. Les étapes de libération s'exécutent toutes, quoi qu'il arrive à l'une d'entre elles.

Pourquoi *arrêter* le conteneur plutôt que décharger le modèle à l'intérieur du serveur : un déchargement interne au serveur laissait environ 1 Go de la carte occupé après de nombreux changements de modèle ; arrêter le conteneur libère tout.

<a id="the-steps"></a>

### Les étapes

Chaque étape est une méthode, une adresse, des en-têtes, un corps, un délai d'attente et une attente après (en secondes) ; les en-têtes sont stockés chiffrés, de sorte que la clé du proxy peut y être inscrite (`Authorization: Bearer ...`). Avec un LiteLLM devant tout le reste, les étapes du verrou de parole sont (*Before 1* à *3* sont ses étapes d'acquisition, *After 1* son étape de libération) :

| | Méthode et adresse | En-têtes | Corps | Rôle |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Décharge le modèle de langue ; répondu par le hook lui-même, prend 1 à 2 s. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | Démarre le conteneur de parole et répond lorsque le serveur est prêt (environ 13 s). |
| Before 3 (seulement si le serveur démarre sans modèle) | `POST http://litellm:4000/gpu/tts/load` | | | Charge le modèle (8 à 14 s, 50 s la première fois, le temps du téléchargement). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Arrête le conteneur (moins d'une seconde). |

Pour un backend **Higgs**, *Before 2* est `POST http://litellm:4000/gpu/comfy/start` (il répond quand ComfyUI est lancé, environ 15 s ; le modèle est chargé avec la première pièce), il n'y a pas de *Before 3*, et *After 1* est `POST http://litellm:4000/gpu/comfy/stop`. Les deux pass-through sont décrits dans le README de `tools/gpu-control`.

Le modèle de langue revient tout seul : la requête ordinaire suivante fait charger le modèle par le routeur (environ une minute pour un gros modèle ; avec le [hook de cache de prompt](../prompt-cache.md), le cache de votre session est restauré), donc aucune requête après le lot n'est nécessaire pour lui.

Sans LiteLLM, les mêmes requêtes vont à ce qui démarre et arrête vos conteneurs ; la seule règle est l'ordre ci-dessus.

<a id="what-answers-those-addresses"></a>

### Ce qui répond à ces adresses

- **`X-Llama-Unload`** est répondu par le hook `gpu_unload.py` de LiteLLM, décrit avec son code dans [Libérer le GPU pour un autre modèle](../prompt-cache.md#freeing-the-gpu-for-another-model) (il fonctionne sans le hook de cache). Avec une route `openai/*`, le nom du modèle dans le corps doit commencer par `openai/` : un autre nom reçoit une 400 *après* que les modèles ont été déchargés.
- **`/gpu/tts/start`, `/stop`, `/load`** sont des **endpoints pass-through** de LiteLLM vers le petit contrôleur `tools/gpu-control` du dépôt, qui démarre et arrête des conteneurs par leur nom. Son README contient le fichier compose avec le **proxy de socket Docker** (ainsi le contrôleur peut démarrer et arrêter des conteneurs, et rien d'autre ; le socket lui-même est root sur l'hôte) et les réglages faciles à mal configurer : `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. Dans LiteLLM :

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

  (le conteneur s'appelle `chatterbox` et `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indique au contrôleur ce qu'il doit attendre ; le jeton du contrôleur reste dans son `.env` et dans l'environnement de LiteLLM, Marginalia ne possède que la clé de LiteLLM).
- **Tous les conteneurs et LiteLLM partagent un réseau Docker** (`docker network create ai`, une seule fois). Placez `networks: [default, ai]` et le réseau externe `ai` dans chaque fichier compose : un `docker network connect` fait à la main est perdu lorsqu'un conteneur est recréé.

<a id="checking-it"></a>

## Vérifier

1. `curl http://<speech server>/api/model-info` répond (`loaded` vaut vrai après un démarrage).
2. *Test connection* dans le backend indique que le modèle est chargé. Si le verrou GPU est la seule chose qui démarre le serveur, testez-le avec le verrou sélectionné : *le serveur répond, aucun modèle n'est chargé* est la réponse d'un serveur arrêté puis redémarré qui doit encore charger son modèle.
3. Convertissez une courte partie avec **Convert to audio**. La fenêtre affiche les pièces en cours de fabrication ; ensuite le conteneur de parole doit être arrêté et `nvidia-smi` / `nvtop` / `rocm-smi` doivent montrer à nouveau la carte libre.
4. Si le premier appel après un chargement de modèle prend du temps, c'est le moteur qui se réchauffe (une seule fois, 28 s au premier lancement, pas revu ensuite).
<a id="troubleshooting"></a>

## Dépannage

| Symptôme | Cause |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Le serveur a démarré vide (un script de démarrage décharge le modèle une fois) : ajoutez l'étape **Before 3** aux étapes d'acquisition du verrou. |
| Les premiers morceaux sont produits, puis le serveur répond *Model not loaded* et son journal affiche `Request received for /api/unload` | Ce n'est pas Marginalia qui décharge le modèle (l'extension n'appelle jamais `/api/unload`) : c'est généralement un script de démarrage dans le conteneur qui attend que le serveur réponde, puis décharge le modèle « pour que le démarrage reste rapide ». Son attente ne peut se terminer qu'après le premier morceau, parce que le serveur mono-thread était occupé, et le déchargement tombe alors au milieu du lot (une trace d'erreur `urllib` dans le journal du conteneur correspond à cette attente du script). Le motif à chercher dans le fichier compose est un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Remplacez-le par `command: ["python3", "server.py"]` : un simple démarrage donne un Turbo chargé, et le déchargement ne servait qu'à libérer la carte après un démarrage, ce dont un conteneur que le contrôleur démarre et arrête n'a pas besoin. Pour savoir qui a envoyé la requête, lisez le journal de l'extension (*Admin → Extensions → Log*) : il liste chaque requête avant et après un lot avec son adresse et l'heure de sa réponse, ainsi que le début et la fin des morceaux ; les étapes du verrou GPU figurent dans le journal de l'application. Un déchargement qui survient alors que le journal indique que le lot est en cours, ou qui vient d'une adresse absente de la liste, ne vient pas de l'extension. |
| Le lot s'arrête en plein milieu (le journal de l'extension indique *Cannot reach*, ou le journal du serveur s'arrête simplement) et un redémarrage manuel fonctionne | Le serveur est mort, généralement tué par le système pour avoir pris toute la mémoire (`dmesg \| grep -i oom` sur l'hôte : un processus `python3` de plusieurs Go). L'extension le redémarre alors elle-même (les requêtes après le lot, les étapes de libération puis d'acquisition du verrou GPU, puis les requêtes avant celui-ci) et refait le morceau une seule fois ; si le serveur ne peut pas être relancé, ou s'il retombe aussitôt, le reste du lot échoue immédiatement et le journal indique pourquoi. Pour éviter qu'un serveur qui fuit n'en arrive là, réglez **Restart the server after (pieces)** dans le backend (par exemple 100), et une option `mem_limit` sur le conteneur pour que le système ne tue que lui. |
| La conversion échoue aussitôt avec la réponse du proxy | La première étape d'acquisition du verrou GPU a échoué (clé erronée, route de déchargement introuvable) ; la liste s'arrête volontairement à cet endroit. |
| L'hôte manque de mémoire au démarrage du lot | Le modèle de langage était encore sur la carte : l'étape de déchargement manque dans le verrou de la synthèse vocale ou n'est pas la première, ou l'IA du modèle de langage n'a pas de verrou sur le même GPU. |
| Le japonais est lu comme du charabia | `pykakasi` manque dans l'image du serveur ; le backend doit aussi être la version multilingue pour ce morceau. |
| L'envoi d'un clip échoue derrière un proxy | Le proxy doit transmettre un corps multipart (`/upload_reference`) sans le modifier. Non testé via le pass-through de LiteLLM ; utilisez un fichier de voix du serveur (`predefined:Name.wav`) tant que cela ne fonctionne pas. |
| Le lot d'un autre utilisateur décharge le modèle sous le vôtre | Chaque backend et chaque IA sur une même carte doit nommer un verrou du même [GPU](../administration/gpus.md) : c'est seulement dans ce cas qu'ils se relaient. Un backend sans verrou n'attend personne. Deux installations de Marginalia sur une même carte ne partagent pas de GPU. |
