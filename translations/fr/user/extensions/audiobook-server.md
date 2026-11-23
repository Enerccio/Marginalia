---
label: Mise en place du serveur de synthèse vocale
order: 28
---

# Mise en place du serveur de synthèse vocale pour le livre audio

L'extension [Livre audio](audiobook.md) a besoin d'un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licence MIT) qu'elle peut joindre en HTTP. Cette page traite du côté serveur. Ce qui est décrit ici a été essayé sur une seule machine (une AMD Radeon AI PRO R9700
avec 32 Go, LiteLLM devant tout, `llama-server` en mode routeur) ; votre matériel et votre proxy peuvent demander d'autres valeurs.

<a id="which-setup-is-yours"></a>

## Quelle est votre configuration ?

| Votre situation | Que faire |
|---|---|
| Le serveur de synthèse vocale tourne sur **une autre machine**, sur le CPU, ou sur une carte qui a la place de le loger **à côté du modèle de langage** | Lancer le serveur, indiquer son adresse dans un backend, **ne choisir aucun verrou GPU**. Rien d'autre n'est nécessaire. |
| Le serveur de synthèse vocale et le modèle de langage **partagent une carte** et ne tiennent pas ensemble | Il vous faut un [verrou GPU](../administration/gpus.md) : ses étapes **acquire** libèrent la carte et démarrent le serveur de synthèse vocale, ses étapes **release** l'arrêtent. Le reste de cette page. |
| Le modèle de langage est un modèle dans le cloud | Rien à libérer : lancer le serveur de synthèse vocale là où vous voulez et ne choisir aucun verrou GPU. |

Donc : **les requêtes pre et post ne sont nécessaires que lorsque les deux modèles se relaient sur une même carte GPU.** Ce sont elles qui organisent ce relais : l'extension les envoie une seule fois autour d'un *lot* (tout ce que vous avez lancé d'un clic), et non autour de chaque partie.

<a id="1-the-speech-server"></a>

## 1. Le serveur de synthèse vocale

Lancez Chatterbox-TTS-Server dans un conteneur (ou depuis les sources) de façon à ce que `GET /api/model-info` réponde. Points importants :

- **Le modèle.** Turbo (`chatterbox-turbo`) est la valeur par défaut de l'extension et celle qui sonne le mieux dans les essais ; le modèle multilingue n'est nécessaire que pour un texte dans une autre langue que celle du livre. Le modèle se règle dans `config.yaml` du serveur
  (ou `POST /save_settings` et `POST /restart_server`). Le *Speech model* du backend doit être le modèle que le serveur a chargé après les étapes acquire de son verrou GPU.
- **Le japonais exige `pykakasi` dans l'image.** Sans lui, le serveur journalise *pykakasi not available - Japanese text processing skipped* et lit mal les kanjis. Ajoutez `RUN python3 -m pip install --no-cache-dir pykakasi` à son Dockerfile (avant `COPY . .`). Le même saut silencieux existe pour les paquets hébreu, russe et chinois.
- **Les voix** sont des fichiers dans le dossier `reference_audio/` du serveur (montez-le en volume pour qu'elles survivent). L'extension envoie elle-même les extraits de vos comédiens (`POST /upload_reference`) avant un lot, sous un nom construit à partir du hachage de l'extrait. Le serveur n'a pas d'appel de suppression : les extraits s'accumulent.
- Le serveur n'a **aucune authentification**. Ne publiez pas son port ; placez un proxy devant lui s'il n'est pas sur un réseau privé.
- `restart: "no"` pour un conteneur que quelqu'un d'autre démarre à la demande. `restart: unless-stopped` le démarre au démarrage de la machine et monopolise la carte pour rien.

Dans le backend (*Settings → Extension Settings → Audiobook Settings*), indiquez son adresse, cochez *The server is on this machine or on my network* si c'est le cas, puis appuyez sur **Test connection** : *the server answers, the model is loaded* est le résultat attendu.
L'extension parle à `/tts` (l'ensemble complet des paramètres : voix, langue, exagération, poids CFG, température, vitesse, graine) ainsi qu'à `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Si un proxy se trouve entre vous et le serveur, il doit transmettre tous ces appels.

<a id="higgs-audio-v3-in-comfyui"></a>

## Higgs Audio v3 dans ComfyUI

Un backend avec le modèle de synthèse vocale **Higgs Audio v3 in ComfyUI** s'adresse à un [ComfyUI](https://github.com/comfyanonymous/ComfyUI) équipé des nœuds de la
[TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite), et non à Chatterbox-TTS-Server. Son adresse est celle de ComfyUI (par exemple
`http://gpu-server:8188`). Il a été installé sur la même AMD R9700, dans l'image `yanwk/comfyui-boot:rocm`.

1. **Installez la suite** dans le dossier `custom_nodes` de ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). Ses paquets Python vont dans le
   conteneur. Pour les conserver lorsque le conteneur est recréé, donnez à pip un dossier sur un volume : ajoutez `PIP_USER=true` à l'environnement du conteneur et montez
   `./storage/local:/root/.local`.
2. **Ne laissez jamais pip remplacer le torch de l'image** (une version CUDA remplacerait celle ROCm). Écrivez les versions installées dans un fichier et installez avec lui comme contrainte :
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   N'exécutez pas `install.py` de la suite en entier : il pourrait réinstaller torch. `librosa` ne figure pas dans son `requirements.txt` et la suite ne se charge pas sans lui.
3. **Le modèle** : `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (environ 8 Go), ou laissez la suite le télécharger la première fois. Il demande environ 8 Go de la carte graphique.
4. **Test connection** dans le backend affiche *the server answers* avec le modèle du nœud lorsque ComfyUI et le nœud sont là.

Ce que fait l'extension avec lui : elle envoie les extraits des comédiens dans le dossier d'entrée de ComfyUI (`POST /upload/image`, sous un nom construit à partir de l'extrait), liste les voix de la suite et les extraits avec `GET /object_info`, poste un workflow qui lui est propre vers `/prompt` pour chaque morceau, attend sur `/history` et récupère l'audio depuis `/view` : avec ffmpeg côté Marginalia, en flac, qu'elle découpe après le dernier mot, fait fondre en fin et enregistre en mp3 ou wav ; sans ffmpeg, en mp3 de ComfyUI, sans découpe (l'enregistrement en wav ne fonctionne alors pas). **Après chaque morceau, elle appelle `POST /free` avec `{"free_memory": true}`** : sur la R9700, le troisième morceau d'affilée sur le modèle chargé a fait planter la carte graphique (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI se termine) ; vider la mémoire entre les morceaux garde le modèle chargé et évite le problème, pour environ 3 secondes par morceau. ComfyUI est démarré et arrêté autour d'un lot avec le même type de requêtes que le serveur de synthèse vocale ([les requêtes](#the-requests)).

**Utilisez fp32 sur une carte AMD.** Avec la précision `auto` (bf16 sur la R9700 avec ROCm 7.2), beaucoup de morceaux sont sortis en charabia, en effets sonores (une réplique sur des coups est devenue onze coups frappés) ou dans une mauvaise voix, et changer la graine ne faisait que modifier la chance. Les mêmes morceaux en fp32 étaient parfaits mot pour mot. La **Precision** du backend vaut fp32 par défaut ; elle demande environ 16 Go de la carte. La reconnaissance vocale Granite de la suite a le même problème en bf16 (charabia dès la deuxième reconnaissance), donc l'extension la fait tourner aussi en float32.

**Le nettoyage des extraits** ([Nettoyage des extraits](audiobook.md#cleanup-of-clips)) utilise deux nœuds supplémentaires de la même suite, `VocalRemovalNode` (modèles de séparation) et `VoiceFixerNode`, avec `LoadAudio` et `SaveAudio` de ComfyUI ; rien d'autre n'est à installer. La suite télécharge un modèle la première fois qu'il sert (les modèles MelBand Roformer font environ 900 Mo chacun). Sur la R9700, les modèles MelBand (voix, débruitage) et le voice fixer ont fonctionné, environ 25 secondes pour un extrait de 16 secondes. Les modèles UVR non : `UVR-DeNoise` n'a pas pu se télécharger (l'adresse de la suite répondait 404) et `UVR-DeEcho-DeReverb` a échoué avec `module 'samplerate' has no attribute 'resample'` (le paquet Python `samplerate` du conteneur n'est pas celui attendu par la suite). Les nettoyages par défaut n'utilisent que les modèles qui ont fonctionné.

Le moteur **Fish Audio S2 Pro** de la suite n'a pas fonctionné sur cette carte : son audio sortait sous forme de nombres invalides (NaN). Les poids de Higgs Audio v3 sont sous une licence de recherche et non commerciale.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## Un seul GPU pour le modèle et la parole

Une carte qui porte le modèle de langage n'a pas la place pour le modèle de parole. L'administrateur configure l'alternance une seule fois dans
[Admin → GPUs](../administration/gpus.md), avec un GPU et un verrou pour chaque élément pouvant être chargé dessus, puis choisit le verrou dans l'IA du
modèle de langage et dans le backend de parole. Le plan est le même pour tout proxy :

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

Un lot (ou un échantillon) du backend de parole exécute les étapes d'acquisition du verrou de parole lorsque le GPU porte le modèle de langage, puis
toutes les pièces ; le GPU reste chargé avec le serveur de parole après cela. La requête suivante vers le modèle de langage attend la fin du lot,
exécute l'étape de libération du verrou de parole (arrêt du serveur) et poursuit. Plusieurs lots et échantillons à la suite ne paient l'alternance
qu'une fois.

**L'ordre n'est pas négociable.** Démarrer le conteneur de parole pendant que le modèle de langage occupe la carte a rempli la mémoire de l'hôte lors
du test (30 Go de RAM, le swap, puis le tueur OOM a fait tomber d'autres services) : le déchargement **doit passer en premier**. La première étape
d'acquisition en échec met fin à la liste, donc rien ne démarre sur une carte pleine ; et lorsqu'une étape de libération échoue, le verrou suivant
n'est pas chargé du tout (le GPU peut encore être plein) et le job échoue avec l'erreur. Les étapes de libération s'exécutent toutes, quoi qu'il
arrive à l'une d'entre elles.

**Higgs, la reconnaissance vocale et le nettoyage des clips sur un même ComfyUI** ont chacun leur propre verrou, les trois avec les mêmes étapes
(décharger le modèle de langage, démarrer ComfyUI ; libération : arrêter ComfyUI). ComfyUI garde en mémoire du serveur un modèle qu'il a déchargé
(Higgs en fp32 représente environ 16 Go), et charger la reconnaissance vocale à côté a provoqué un manque de mémoire du serveur lors du test ; avec
trois verrous, chaque changement redémarre ComfyUI, de sorte qu'un seul des trois est chargé à la fois.

Pourquoi *arrêter* le conteneur plutôt que décharger le modèle à l'intérieur du serveur : un déchargement à l'intérieur du serveur laissait environ
1 Go de la carte occupé après de nombreux changements de modèle ; arrêter le conteneur libère tout.

<a id="the-steps"></a>

### Les étapes

Chaque étape est une méthode, une adresse, des en-têtes, un corps, un délai d'attente et une pause après (en secondes) ; les en-têtes sont stockés
chiffrés, de sorte que la clé du proxy peut y être écrite (`Authorization: Bearer ...`). Avec un LiteLLM devant tout le reste, les étapes du verrou de parole sont (*Avant 1*
à *3* sont ses étapes d'acquisition, *Après 1* son étape de libération) :

| | Méthode et adresse | En-têtes | Corps | Rôle |
|---|---|---|---|---|
| Avant 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Décharge le modèle de langage ; répondu par le hook lui-même, prend 1 à 2 s. |
| Avant 2 | `POST http://litellm:4000/gpu/tts/start` | | | Démarre le conteneur de parole et répond lorsque le serveur est prêt (environ 13 s). |
| Avant 3 (seulement si le serveur démarre sans modèle) | `POST http://litellm:4000/gpu/tts/load` | | | Charge le modèle (8 à 14 s, 50 s la première fois, le temps du téléchargement). |
| Après 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Arrête le conteneur (moins d'une seconde). |

Pour un backend **Higgs**, *Avant 2* est `POST http://litellm:4000/gpu/comfy/start` (il répond lorsque ComfyUI est prêt, environ 15 s ; le modèle est chargé avec la première pièce),
il n'y a pas d'*Avant 3*, et *Après 1* est `POST http://litellm:4000/gpu/comfy/stop`. Les deux pass-through sont décrits dans le README de `tools/gpu-control`.

Le modèle de langage revient de lui-même : la requête ordinaire suivante fait charger le modèle par le routeur (environ une minute pour un gros
modèle ; avec le [hook de cache de prompt](../prompt-cache.md), le cache de votre session est restauré), donc aucune requête après le lot n'est nécessaire pour lui.

Sans LiteLLM, les mêmes requêtes vont vers ce qui démarre et arrête vos conteneurs ; la seule règle est l'ordre ci-dessus.

<a id="what-answers-those-addresses"></a>

### Ce qui répond à ces adresses

- **`X-Llama-Unload`** est répondu par le hook `gpu_unload.py` de LiteLLM, décrit avec son code dans
  [Libérer le GPU pour un autre modèle](../prompt-cache.md#freeing-the-gpu-for-another-model) (il fonctionne sans le hook de cache). Avec une route `openai/*`, le nom du modèle dans le corps doit commencer
  par `openai/` : un autre nom reçoit une réponse 400 *après* que les modèles ont été déchargés.
- **`/gpu/tts/start`, `/stop`, `/load`** sont des **endpoints pass-through** de LiteLLM vers le petit contrôleur `tools/gpu-control` du dépôt, qui démarre et arrête des
  conteneurs par nom. Son README contient le fichier compose avec le **proxy de socket Docker** (ainsi le contrôleur peut démarrer et arrêter des
  conteneurs et rien d'autre ; le socket lui-même est root sur l'hôte) et les réglages faciles à mal configurer : `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`,
  **`CONTAINERS=0`**. Dans LiteLLM :

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

  (le conteneur s'appelle `chatterbox` et `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indique au contrôleur ce qu'il doit attendre ; le jeton du contrôleur reste dans son `.env` et dans
  l'environnement de LiteLLM, Marginalia ne possède que la clé de LiteLLM).
- **Tous les conteneurs et LiteLLM partagent un réseau Docker** (`docker network create ai`, une seule fois). Mettez `networks: [default, ai]` et le réseau externe `ai` dans chaque
  fichier compose : un `docker network connect` fait à la main est perdu lorsqu'un conteneur est recréé.

<a id="checking-it"></a>

## Vérification

1. `curl http://<speech server>/api/model-info` répond (`loaded` vrai après un démarrage).
2. *Test connection* dans le backend indique que le modèle est chargé. Si le verrou GPU est le seul à démarrer le serveur, testez-le avec le verrou
   choisi : *le serveur répond, aucun modèle n'est chargé* est la réponse d'un serveur arrêté puis redémarré qui doit encore charger son modèle.
3. Convertissez une courte partie dans **Convert to audio**. La fenêtre affiche les pièces en cours de création ; ensuite le conteneur de parole
   doit être arrêté et `nvidia-smi` / `nvtop` / `rocm-smi` doivent montrer la carte de nouveau libre.
4. Si le premier appel après le chargement d'un modèle est long, c'est le moteur qui se réchauffe (une fois, 28 s lors du premier essai, et plus
   jamais revu ensuite).
<a id="troubleshooting"></a>

## Dépannage

| Symptôme | Cause |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Le serveur a démarré vide (un script de démarrage qui décharge le modèle une seule fois) : ajoutez l'étape **Before 3** aux étapes d'acquisition du verrou. |
| Les premiers morceaux sont produits, puis le serveur répond *Model not loaded* et son journal affiche `Request received for /api/unload` | Quelque chose d'autre que Marginalia décharge le modèle (l'extension n'appelle jamais `/api/unload`) : typiquement un script de démarrage dans le conteneur qui attend que le serveur réponde, puis décharge le modèle « pour garder un démarrage léger ». Son attente ne peut se terminer qu'après le premier morceau, parce que le serveur mono-thread était occupé, et le déchargement tombe alors au milieu du lot (une trace d'erreur de délai `urllib` dans le journal du conteneur correspond à l'attente de ce script). Le motif à chercher dans le fichier compose est un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Remplacez-le par `command: ["python3", "server.py"]` : un simple démarrage donne un Turbo chargé, et le déchargement ne servait qu'à libérer la carte après un démarrage, ce dont un conteneur que le contrôleur démarre et arrête n'a pas besoin. Pour savoir qui l'a envoyé, lisez le journal de l'extension (*Admin → Extensions → Log*) : il liste chaque requête avant et après un lot, avec son adresse et l'heure de sa réponse, ainsi que le début et la fin des morceaux ; les étapes du verrou GPU figurent dans le journal de l'application. Un déchargement qui arrive pendant que le journal indique que le lot tourne, ou qui vient d'une adresse absente de la liste, n'est pas celui de l'extension. |
| Le lot s'arrête au milieu (le journal de l'extension indique *Cannot reach*, ou le journal du serveur s'interrompt simplement) et un redémarrage à la main fonctionne | Le serveur est mort, généralement tué par le système parce qu'il a pris toute la mémoire (`dmesg \| grep -i oom` sur l'hôte : un processus `python3` de plusieurs Go). L'extension le redémarre alors elle-même (les requêtes après le lot, les étapes de libération et d'acquisition du verrou GPU, puis les requêtes avant celui-ci) et refait le morceau une seule fois ; si le serveur ne peut pas être relancé, ou meurt de nouveau aussitôt, le reste du lot échoue immédiatement et le journal indique pourquoi. Pour éviter qu'un serveur qui fuit n'en arrive là, réglez **Restart the server after (pieces)** dans le backend (par exemple 100), et ajoutez un `mem_limit` au conteneur pour que le système ne tue que lui. |
| La conversion échoue aussitôt avec la réponse du proxy | La première étape d'acquisition du verrou GPU a échoué (mauvaise clé, route de déchargement introuvable) ; la liste s'arrête là volontairement. |
| L'hôte manque de mémoire au démarrage du lot | Le modèle de langage était encore sur la carte : l'étape de déchargement manque dans le verrou de la voix ou n'est pas la première, ou l'IA du modèle de langage n'a pas de verrou sur le même GPU. |
| Le japonais est lu comme du charabia | `pykakasi` manque dans l'image du serveur ; le backend doit aussi être la version multilingue pour ce morceau. |
| L'envoi d'un clip échoue derrière un proxy | Le proxy doit transmettre un corps multipart (`/upload_reference`) sans le modifier. Non testé via le pass-through de LiteLLM ; utilisez un fichier de voix du serveur (`predefined:Name.wav`) en attendant que cela fonctionne. |
| Le lot d'un autre utilisateur décharge le modèle sous le vôtre | Chaque backend et chaque IA sur une même carte doit nommer un verrou du même [GPU](../administration/gpus.md) : seulement alors ils se relaient. Un backend sans verrou n'attend personne. Deux installations de Marginalia sur une même carte ne partagent pas de GPU. |
