---
label: Livre audio
order: 27
---

# Livre audio

Transforme un livre en livre audio, partie par partie, à l'aide d'un serveur de synthèse vocale : une voix pour le narrateur et une voix pour chaque personnage, conservées avec les parties du livre et lues dans le lecteur. Vous pouvez la laisser tourner (le modèle indique qui parle et comment, le serveur de synthèse lit) pour obtenir un premier jet, ou travailler les parties une à une dans le **Audiobook studio**, en examinant exactement ce qui est envoyé au serveur de synthèse et en le modifiant à la main, puis exporter le résultat en fichiers mp3 avec des marques de chapitre.

L'extension ne produit pas elle-même la parole : elle communique avec un **Chatterbox-TTS-Server** que vous faites tourner (voir [Installer le serveur de synthèse](audiobook-server.md)). Le texte du livre n'est jamais modifié et n'est envoyé qu'aux serveurs que vous avez indiqués ; l'audio est stocké dans votre propre installation, sous forme de ressources liées aux parties.

<a id="what-you-need"></a>

## Ce dont vous avez besoin

- Un serveur de synthèse vocale et, s'il partage la carte graphique avec votre modèle de langage, les requêtes qui libèrent de la place pour lui puis la arrêtent à nouveau ([Installer le serveur de synthèse](audiobook-server.md)).
- Des voix : un court **extrait de référence** (12 à 30 secondes d'une seule personne, sans musique) pour chaque voix que vous voulez cloner, ou l'un des fichiers de voix fournis par le serveur. Un narrateur a besoin d'une voix de lecture calme et régulière.
- Pour un livre à plusieurs voix, un modèle de langage capable d'étiqueter le texte. Par défaut, c'est le modèle du livre.
- **ffmpeg** est facultatif. Sans lui, tout fonctionne en mp3. Avec lui sur le serveur (il est présent dans l'image Docker ; sur un ordinateur de bureau, il est trouvé dans le `PATH` ou dans les dossiers habituels comme `/opt/homebrew/bin` ; la variable `MARGINALIA_FFMPEG` indique son emplacement lorsqu'il se trouve ailleurs), l'export obtient le format **m4b**, et un administrateur peut stocker l'audio au format wav.

<a id="the-settings"></a>

## Les paramètres

*Paramètres → Extension Settings → Audiobook Settings* est le panneau de votre compte ; enregistrez la page *Paramètres* après l'avoir modifié.

![Le panneau avec un backend et les acteurs](../../images/plugin-audiobook-settings.png)

### Backends

Un backend est un serveur de synthèse vocale et le modèle avec lequel il parle. Ajoutez-en un avec **+**. **Duplicate** copie le backend sélectionné (adresse, clés, verrou GPU, voix, valeurs par défaut et requêtes avant et après un lot) sous un nouveau nom : c'est le moyen rapide d'obtenir un backend pour un autre modèle sur la même machine, par exemple Higgs à côté de Chatterbox, où vous changez ensuite le modèle, l'adresse et les requêtes de démarrage et d'arrêt.

| Paramètre | Ce qu'il fait |
|---|---|
| Name | Le nom affiché dans la fenêtre de conversion. |
| Address | L'adresse de base du serveur de synthèse, ou de ce qui le transmet, par exemple `http://localhost:8004`. |
| Api key | Envoyée comme jeton bearer si le serveur (ou votre proxy) en demande un. Elle est stockée chiffrée et n'est plus affichée ; saisissez une nouvelle clé pour la remplacer, ou utilisez **Remove the key**. |
| Speech model | **Chatterbox Turbo** (en anglais, avec des balises sonores comme `[sigh]`, le plus rapide et le modèle par défaut), **Chatterbox** (en anglais, avec une *exaggeration* de l'intonation), **Chatterbox Multilingual** (23 langues) ou **Higgs Audio v3 in ComfyUI** (de nombreuses langues, voix qui suivent l'humeur d'une réplique, et sons tels que des pleurs, un souffle ou un gémissement ; voir [plus bas](#higgs-audio-v3-emotions-and-sounds)). Le [verrou GPU](../administration/gpus.md) du backend (ou les requêtes avant un lot) doit laisser ce modèle chargé. Un livre qui a besoin d'un autre modèle utilise un autre backend. |
| GPU lock | Le [verrou GPU](../administration/gpus.md) du GPU sur lequel tourne le serveur, lorsqu'il le partage avec le modèle de langage ou d'autres serveurs (y compris l'[Image Generator](image-generator.md)) : chaque lot et chaque échantillon attend son tour sur le GPU, et le verrou charge d'abord le serveur de synthèse. Affiché seulement si l'administrateur a configuré les verrous GPU. |
| The server is on this machine or on my network | Autorise les adresses du réseau local. Laissez-le désactivé pour un serveur dans le cloud : il ne peut alors pas être amené à appeler votre propre machine. |
| Timeout of a request | Le délai d'attente d'un morceau de parole. |
| Restart the server after (pieces) | Pour un serveur de synthèse qui fuit en mémoire et qui est tué par le système lors d'un long lot : après ce nombre de morceaux, les requêtes qui suivent le lot puis celles qui le précèdent sont envoyées à nouveau au milieu du lot (arrêt, démarrage, chargement), et le travail se poursuit avec un serveur neuf. 0 (la valeur par défaut) ne le fait jamais. Un serveur qui plante est redémarré quel que soit ce réglage, voir [Dépannage](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | La clé que les requêtes avant et après un lot transmettent sous la forme `Authorization: Bearer`. Elle est stockée chiffrée et n'est plus affichée (**Remove the key** l'efface), si bien qu'elle n'a pas besoin d'être écrite dans les en-têtes de chaque requête, où elle serait lisible. Vide : la *Api key* ci-dessus est utilisée, ce que vous souhaitez lorsqu'un proxy comme LiteLLM sert tout. Une requête qui a son propre en-tête `Authorization` le conserve. |
| Narrator voice | Le narrateur d'un livre sans distribution. Un fichier de voix du serveur s'écrit `predefined:Henry.wav`. |
| Precision | Higgs uniquement : la précision dans laquelle le modèle calcule. **fp32** (la valeur par défaut) est le choix sûr : sur une carte AMD, **bf16** a transformé des morceaux en charabia, en effets sonores et en voix erronées. bf16 nécessite environ moitié moins de mémoire graphique (environ 8 Go au lieu de 16) et est plus rapide là où il fonctionne. |
| Send the text of the clip to Higgs | Higgs uniquement, expérimental : le [texte de l'extrait](#voices-and-clips) l'accompagne. Laissez-le désactivé : lors des tests, cela a modifié la voix. |
| Check the length of the pieces | Activé par défaut. Un morceau bien plus long que son texte (4 s plus 0,6 s par mot : le modèle a continué à parler, ou a produit un son au lieu des mots) est refait avec la graine suivante, jusqu'à **Nouvelles tentatives** fois ; la première prise de la bonne longueur est conservée, sinon la plus courte. |
| Check the words with speech recognition | Désactivé par défaut ; nécessite la [reconnaissance vocale](#speech-recognition). Après chaque lot, chaque morceau est transcrit et comparé à son texte. Un morceau qui s'écarte de plus de **Max. word errors (%)** de ses mots est refait avec la graine suivante et vérifié à nouveau, jusqu'à **Nouvelles tentatives** tours ; la prise comportant le moins de mots erronés est conservée. La conversion indique combien de morceaux ont été refaits et combien diffèrent encore. |
| Defaults of a segment | Température, poids CFG, vitesse et taille des fragments avec lesquels un morceau démarre (le studio peut les modifier pour un seul morceau). Higgs n'utilise que la température. |
| Before a batch, After a batch | Requêtes envoyées autour de chaque lot de parties (et de chaque échantillon), à l'intérieur du verrou GPU. Libérer la carte graphique et démarrer le serveur de synthèse est le rôle du verrou GPU ; voir [Un GPU pour le modèle et la synthèse](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vide : ne fait rien. |

**Test connection** fait ce que fait un lot : il prend le verrou GPU (ce qui démarre un serveur de synthèse arrêté), envoie les requêtes qui précèdent un lot, demande au serveur s'il est là et s'il a un modèle chargé, puis envoie les requêtes qui suivent le lot. Sans verrou ni requêtes, il se contente de poser la question.

Un livre qui contient des passages dans une autre écriture (du japonais dans un livre en anglais) les envoie au modèle multilingue, si vous avez un backend pour lui, avec leur langue ; ils restent sur le modèle du livre si vous n'en avez pas. Un livre Higgs les conserve : Higgs les lit lui-même, dans la même voix.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3 : émotions et sons

Avec un backend **Higgs Audio v3 in ComfyUI**, les étiquettes du modèle de langage sont entendues. L'adresse du backend est celle de votre ComfyUI
(avec les nœuds TTS Audio Suite, voir [Mise en place du serveur de parole](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **diction** d'un passage de parole est jouée : triste, en colère, joyeuse, amusée, tendue, effrayée, surprise, tendre, sarcastique, suppliante, désespérée,
  dégoûtée, excitée, un chuchotement ou un cri. Une diction forte (intensité 0,8 et plus) est plus expressive, une diction faible (en dessous de 0,2) est lue sans émotion.
  Le narrateur est lu sans émotion.
- Le modèle de langage peut mettre davantage de **sons** dans la parole que pour Chatterbox Turbo : rire, petit rire, gloussement, soupir, halètement, toux, raclement de gorge,
  reniflement, éternuement, gémissement, pleur, sanglot, cri, souffle court, gémissement de plaisir, fredonnement et chut.
- Le programme lit aussi le **récit autour de chaque passage de parole** (en anglais) et donne à la parole le son qu'il nomme, lorsque le modèle n'en a mis aucun :
  *"You look funny," she giggled.* reçoit un gloussement, *She burst out laughing. "Careful!"* un rire, *a soft moan escaped her lips. "Yes..."* un gémissement,
  *panting* ou *out of breath* un souffle court, *cried quietly* ou *burst into tears* des pleurs. Il ignore ce qui ne ressemble qu'à un son : *"Stop!" she cried.*
  (une exclamation), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, ainsi qu'un son que le récit
  attribue à un autre personnage nommé. *"You look funny," he mocked her.* reçoit une diction sarcastique ou amusée et aucun son. Le récit lui-même est lu tel qu'il est écrit.
- **Le narrateur est toujours lu sans émotion** : pas de sons ni d'humeur, y compris pour un passage que le modèle a laissé au narrateur.
- Higgs n'a pas besoin d'un extrait par humeur : un seul extrait d'une voix suffit. Il reprend l'enregistrement de l'extrait, donc un extrait propre compte davantage qu'avec Chatterbox
  ([Un bon extrait](#a-good-clip)).
- Chaque passage est produit par ComfyUI, et sa mémoire est vidée après chacun (quelques secondes par passage), ce qui évite que certaines cartes graphiques plantent.
- Higgs continue souvent après le dernier mot d'un passage (un souffle, le début d'un autre son), et l'extrait s'arrête au milieu de ce son, ce qu'on entend comme un bref
  « whoosh » avant la pause. Avec **ffmpeg**, l'extension coupe cette queue et fait disparaître chaque passage en fondu ; sans elle, les passages restent tels que Higgs les a produits.
- Le modèle Higgs Audio v3 est soumis à une **licence de recherche et non commerciale** : un livre audio que vous vendez nécessite une licence de Boson AI.

<a id="speech-recognition"></a>

### Reconnaissance vocale

La **reconnaissance vocale** transcrit ce que dit un passage de parole. Elle vérifie les passages d'un lot (lorsqu'un backend a **Check the words with speech
recognition**) et reconnaît le [texte d'un extrait vocal](#voices-and-clips). Choisissez où elle s'exécute :

| Réglage | Ce qu'il fait |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)** : le modèle de parole Granite de TTS Audio Suite dans votre ComfyUI (l'adresse est celle de ComfyUI). **OpenAI-compatible (whisper)** : tout serveur disposant de `/v1/audio/transcriptions`, comme un serveur whisper (faster-whisper, speaches) ou l'API OpenAI. |
| Address, Api key | L'adresse de ComfyUI, ou la base de l'API (avec ou sans `/v1`) ; la clé est stockée chiffrée. Adresse vide : pas de reconnaissance vocale. |
| GPU lock | Le [verrou GPU](../administration/gpus.md) que prend la reconnaissance. Sur une seule carte avec Higgs en fp32, donnez-lui un verrou à part dont les étapes redémarrent ComfyUI : ComfyUI garde le modèle de parole dans la mémoire du serveur, et charger la reconnaissance à côté a provoqué un manque de mémoire sur le serveur. |
| Model of the recognition | Vide : `granite-speech-4.1-2b` pour ComfyUI, `whisper-1` pour le serveur compatible OpenAI. |
| Timeout, The server is on this machine or on my network | Comme pour un backend. |

**Test connection** vérifie que ComfyUI dispose des nœuds, ou que le serveur répond. Les passages d'un lot sont vérifiés après le lot, lorsque le serveur de parole est rendu,
si bien que la reconnaissance et le modèle de parole n'ont jamais besoin de la carte en même temps.

<a id="shared-speech-servers"></a>

### Serveurs de parole partagés

Sur une installation à plusieurs utilisateurs, l'administrateur peut garder des serveurs de parole pour tout le monde, de sorte qu'une machine GPU est configurée une seule fois et que personne n'a besoin d'une adresse ou d'une clé. L'administrateur le fait dans le même panneau (les dernières sections, visibles seulement par les administrateurs) : **Shared speech servers** ouvre une fenêtre avec les mêmes champs qu'un backend personnel, plus la liste de ceux qui peuvent l'utiliser : **all users**, ou les utilisateurs choisis dans la liste. Un interrupteur, **Users may add speech servers of their own**, décide si les backends personnels restent autorisés.

Ce que vous obtenez :

- **Backend in use** en haut du panneau liste les serveurs partagés que vous pouvez utiliser (marqués *(shared)*) et les vôtres. La fenêtre *Convert to audio* et le studio utilisent celui que vous choisissez ; les commandes aussi.
- Vous ne voyez que le nom d'un serveur partagé, jamais son adresse, ses clés ni ses requêtes. Les lots de tous les utilisateurs sur une même adresse de serveur attendent leur tour, si bien que deux utilisateurs ne peuvent pas démarrer deux fois le même GPU.
- Lorsque l'administrateur vous retire un serveur, une tâche qui le désigne encore est refusée ; choisissez-en un autre.
- Lorsque l'administrateur désactive vos propres serveurs, le panneau l'indique. Ceux que vous avez ajoutés sont conservés et sont de nouveau utilisés si l'interrupteur est réactivé.
- La *Narrator voice* d'un serveur partagé doit être un fichier de voix du serveur (`predefined:Henry.wav`) : un extrait de référence est une ressource d'un seul utilisateur, et les autres ne peuvent pas le lire. Les voix de vos acteurs fonctionnent comme d'habitude.

<a id="actors"></a>

### Acteurs

Un **acteur** est une voix à vous : un extrait de référence (l'une de vos [ressources](../resources.md) audio, ou un nouvel extrait téléversé ici) ou un
fichier de voix du serveur, avec des voix propres pour une **humeur** (un chuchotement, un cri) ou une **langue** (un extrait qui parle
japonais). La langue l'emporte sur l'humeur, et sans voix pour elles, la voix de l'acteur lit. Vos acteurs sont conservés dans
votre compte ; tout livre peut attribuer un acteur à son narrateur et à ses personnages.

**Aperçu** à côté d'une voix (dans l'acteur, les réglages et la distribution) joue quelques phrases dites avec elle (une courte, dans la langue de la voix pour une langue) ; si le GPU tient déjà autre chose, le verrou GPU charge d'abord le serveur de parole. La flèche à côté propose d'autres échantillons : **with sounds** (un soupir, un rire, un halètement) pour les modèles qui les prononcent (Chatterbox Turbo, Higgs Audio v3), et pour Higgs chaque **mood** de l'étiquetage (sad, whispered, sarcastic...). Les acteurs sont listés par leur nom.

Chaque échantillon ne prend le GPU que pour lui-même. Le GPU reste chargé avec le serveur de parole après un échantillon, si bien que les échantillons suivants arrivent aussitôt, jusqu'à ce qu'une requête du modèle de langage (ou d'un autre serveur) ait besoin du GPU. Ouvrir **Voices and clips** charge déjà le serveur de parole, de sorte que son premier échantillon n'attend pas.

![Un acteur avec une voix pour une humeur](../../images/plugin-audiobook-actor.png)
<a id="voices-and-clips"></a>

### Voix et extraits

**Voix et extraits** (à côté des acteurs) est une fenêtre unique pour vos acteurs et les extraits dont leurs voix sont clonées :

- **Acteurs** : chacun avec sa voix, le nombre d'autres voix (humeurs, langues) qu'il possède et la qualité la plus faible de ses extraits ; ajoutez, modifiez ou supprimez-en un.
- **Extraits** : chacun avec sa durée, sa **qualité** et l'endroit où il est utilisé (acteurs, avec l'humeur ou la langue, et les narrateurs de vos moteurs).
  Sélectionnez un extrait pour lire ce qui ne va pas, le **lire** avec **Lire**, le **remplacer** avec **Remplacer...**, le **nettoyer avec ffmpeg** avec **Nettoyer avec ffmpeg** (avec ffmpeg) ou le supprimer (seulement lorsque rien ne l'utilise).
- **Remplacer...** met un autre extrait, l'un des vôtres ou un fichier que vous envoyez, à la place de l'ancien, partout où il est utilisé. Les parties sont refaites avec la nouvelle voix ;
  l'ancien extrait reste jusqu'à ce que vous le supprimiez.
- **Texte de l'extrait** : les mots prononcés dans l'extrait sélectionné. Tapez-les, ou appuyez sur **Reconnaître** pour que la [reconnaissance vocale](#speech-recognition) les écrive, corrigez-les puis **Enregistrer**. La fenêtre de l'acteur affiche le même champ pour l'extrait de la voix de l'acteur. Une copie nettoyée et un remplacement reprennent ce texte. Higgs ne le reçoit que lorsque le moteur le transmet (désactivé par défaut).
- **Nettoyer avec ffmpeg** crée une copie nettoyée à côté de l'extrait (moins de souffle et de ronflement continus), mesurée à nouveau, pour l'écouter et la comparer ; remplacez l'extrait par elle si elle sonne mieux.
  Elle ne supprime ni la musique, ni les effets, ni les autres voix.

<a id="a-good-clip"></a>

#### Un bon extrait

Un modèle de voix copie l'**enregistrement**, pas seulement la voix : la pièce, un souffle, la musique sous une scène de film reviennent dans chaque phrase, souvent sous la forme d'un léger « chuintement » sous les mots que nul filtre du résultat ne supprime. Donc :

- Utilisez un enregistrement propre d'une seule personne avec de vraies pauses (un échantillon d'acteur de voix, une lecture faite chez soi dans une pièce calme), de 12 à 30 secondes. Un extrait découpé dans un film ou une série est le pire cas.
- Les marques de qualité sont mesurées sur l'extrait : **Propre** (les pauses sont au moins 45 dB sous la parole), **Correct** (35 à 45 dB, ou plus long que 30 s, ce que Chatterbox coupe), **Mauvais** (son de fond, sous 35 dB ; déformé ; plus court que 8 s). Un extrait qui n'est pas propre est signalé lorsque vous l'ajoutez.
- Pour un meilleur résultat, nettoyez un extrait à la main : la réduction de bruit d'[Audacity](https://www.audacityteam.org/) avec un profil de bruit pris dans une pause de l'extrait, ou un suppresseur de voix comme [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) pour un extrait avec de la musique. Le nettoyage ffmpeg
  ne réduit qu'un bruit continu : sur un extrait de film, il a gagné environ 3 dB lors du test et l'extrait est resté *Mauvais*.

<a id="converting-new-parts-by-itself"></a>

### Convertir les nouvelles parties automatiquement

*Convertir les nouvelles parties en audio automatiquement* est la valeur par défaut pour tous vos livres ; voir [Conversion automatique](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## La distribution et la prononciation d'un livre

Les deux sont conservées avec le livre (elles voyagent dans sa sauvegarde) et se modifient depuis la fenêtre **Convertir en audio**.

**Distribution** indique qui lit : un acteur pour le **narrateur**, un pour les **autres personnages** (quiconque le modèle nomme sans que la distribution le prévoie ; vide : le narrateur lit) et un pour chaque **personnage**. Un personnage a un nom et d'autres noms que le texte peut utiliser pour lui (*Tom* pour *Tomáš Novák*), séparés par des virgules ; une réplique est attribuée au personnage lorsque son nom, l'un des autres noms, ou un mot de son nom qu'aucun autre personnage ne partage, correspond, sans tenir compte de la casse ni des accents. **Ajouter les personnages du livre** remplit la liste à partir des personnages du point de vue et des personnages présents dans les parties.

![La distribution d'un livre](../../images/plugin-audiobook-cast.png)

**Prononciation** est la façon dont le texte est écrit *pour la voix* ; le livre lui-même n'est jamais modifié. Le **dictionnaire** remplace des mots entiers (la casse compte) par ce qui doit être dit pour eux : une graphie phonétique pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Pour un livre en anglais, les règles lisent aussi à voix haute d'autres éléments : nombres, ordinaux, années, heures, sommes d'argent et abréviations en mots, un titre qui n'est qu'un nombre comme *Chapter four. The Arch* (le mot placé avant le nombre est le vôtre), les marques markdown comme `*` sont supprimées, et une rupture de scène (`***`, `---`) n'est pas lue mais constitue une pause plus longue. Désactivez les règles pour un livre dans une autre langue ; le dictionnaire s'applique toujours. **Essayer un texte** montre comment un passage sera lu. Un passage dont le texte prononcé change est refait, les autres sont conservés.

![La prononciation d'un livre](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Conversion d'un livre

*Menu Outils de l'éditeur de récit (le bouton à engrenage) → **Convertir en audio***.

![La boîte de dialogue avant le lancement](../../images/plugin-audiobook-convert.png)

1. Choisissez le **backend** et appuyez sur **Charger** : les parties contenant du texte de la branche active sont listées (la première est la 1) avec ce que chacune possède :
   *Non générée*, *Étiquetée* (les étiquettes du modèle sont enregistrées, pas encore d'audio), *Générée* (avec un bouton de lecture et un téléchargement) ou
   *Générée (le texte a changé)* (l'audio a été produit à partir d'un texte plus ancien et sera refait) ; *(final)* est ajouté à une partie que le studio marque comme finale.
2. Laissez **Étiqueter les parties avec un modèle de langage** désactivé pour la version simple : le narrateur lit chaque paragraphe. Activez-le pour que le
   modèle décide qui parle dans chaque passage de dialogue, comment (calme, chuchoté, crié...) et où placer une balise sonore, et le casting fournit
   les voix. Le modèle du livre est choisi ; vous pouvez en prendre un autre si vous le souhaitez. Les parties sont d'abord étiquetées, puis lues à voix haute. Une partie qui a déjà des étiquettes pour son texte est lue avec elles
   même lorsque la case est décochée (le modèle n'est pas sollicité). Relancer un livre étiqueté sans cocher la case ne transforme donc pas ses dialogues en simple narration ; **Supprimer les étiquettes** permet de demander une narration simple ou de nouvelles étiquettes.
3. Cochez les parties (la case de l'en-tête les coche toutes) et **planifiez** ce qu'il faut en faire ; la colonne **Plan** l'affiche, et rien n'est encore exécuté :
   **Générer** (produire ce qui manque ; une partie à jour n'est pas refaite), **Régénérer** (supprimer l'audio, supprimer les étiquettes, puis générer),
   **Supprimer l'audio** (les morceaux et l'audio assemblé de la partie ; les étiquettes restent), **Supprimer les étiquettes** (le modèle sera à nouveau sollicité la prochaine fois que la partie sera produite),
   **Effacer le plan**. Les étapes s'additionnent pour une même partie (Supprimer l'audio et Supprimer les étiquettes sans Générer ne font que nettoyer) ; une partie marquée finale est exclue de tous les plans.
4. **Démarrer** exécute le plan. Lorsqu'il supprime de l'audio ou des étiquettes, il demande d'abord confirmation. Ce qui doit être généré est placé dans votre file d'attente, qui continue de tourner lorsque vous fermez la fenêtre
   et lorsque vous quittez la page ; une partie qui attend ou s'exécute dans la file est exclue d'un plan. L'état, les morceaux déjà produits et le message de la tâche sont affichés sur la ligne de la partie.
   Pendant que le modèle étiquette une partie, sa ligne affiche *Étiquetage des morceaux 17-32 sur 120* (le modèle est sollicité pour environ 16 morceaux à la fois) ; **cliquez sur l'état** d'une partie étiquetée pour lire, pour chaque requête, le
   raisonnement du modèle et sa réponse, au fur et à mesure de leur arrivée. Ils sont conservés en mémoire avec la tâche, jusqu'à ce que les tâches terminées soient effacées (*Effacer les terminées*) ou que l'application redémarre.

![La file d'attente : les morceaux déjà produits, et un téléchargement lorsqu'une partie est terminée](../../images/plugin-audiobook-queue.png)

Comment une partie est produite, pour savoir à quoi s'attendre :

- Le texte est **découpé par le programme**, jamais par le modèle, en morceaux de narration et en morceaux de dialogue direct (entre guillemets doubles droits ou courbes, ou entre crochets japonais ; un titre est de la narration). Le modèle se contente d'*étiqueter* les morceaux numérotés. Un texte avec des balises sonores n'est utilisé que s'il s'agit du morceau avec les balises ajoutées et que rien d'autre n'a changé, ainsi le modèle ne peut jamais modifier votre livre.
- **Qui parle dans un morceau est décidé par le modèle pour les dialogues ; la narration est celle du narrateur.** Une lettre, un mot, un message ou les pensées d'un personnage n'ont pas de guillemets, donc le
  programme les considère comme de la narration. Un tel morceau n'est lu par le personnage (la voix du casting) que lorsqu'il **commence en italique** (`*...*` ou `_..._`) et que le modèle nomme le personnage, d'après le contexte (une signature, « l'écriture ne laissait aucun doute »). La narration à la troisième personne, même lorsqu'elle est racontée du point de vue d'un personnage (« Elle le regarda traverser la pièce »), est toujours celle du narrateur : le programme ignore le personnage que le modèle attribue à un morceau sans italique. Les italiques d'insistance, de sons ou de mots étrangers relèvent aussi de la narration. Les mots restent toujours les vôtres.
- **L'instruction donnée au modèle est un modèle que vous pouvez modifier.** *Paramètres → Paramètres des extensions → Paramètres du livre audio → Prompt d'étiquetage* est votre valeur par défaut ; un livre (onglet *Prompts* →
  *Paramètres des extensions* → *Livre audio*) ou un dossier peut avoir la sienne. Vide signifie celle du dossier parent, puis la vôtre, puis celle intégrée. Il est rédigé comme les autres
  [prompts](../books/prompts.md) : les variables sont `{{characters}}`, `{{deliveries}}`, `{{tags}}` et `{{wordsPerTag}}`, et la fenêtre des indications propose **Insérer le modèle par défaut**. Une instruction modifiée fait que les étiquettes des parties seront à nouveau demandées la prochaine fois qu'elles seront produites.
- **Le volume sonore des parties est uniformisé** lorsque le programme ffmpeg est installé (le même que celui qui produit le `.m4b`) : l'audio assemblé de chaque partie est ramené à un même niveau sonore (EBU R128, linéaire, pour que la dynamique soit conservée ; **-18 LUFS** sauf si vous le modifiez dans *Paramètres → Paramètres des extensions → Paramètres du livre audio*, où vous pouvez aussi le désactiver). Les morceaux
  sont conservés tels que le serveur les a produits ; seule la partie assemblée est uniformisée, afin qu'un livre audio ne change pas de niveau d'une partie à l'autre. **Les parties produites auparavant** ne sont pas touchées tant
  que vous ne les convertissez pas à nouveau : cela les réassemble à partir des morceaux stockés et les uniformise, sans rien demander au serveur de synthèse vocale (ni décharger le modèle de langage). Désactiver l'uniformisation laisse telles quelles les parties déjà uniformisées. Si ffmpeg échoue pour une partie, elle conserve son audio tel quel et le résultat l'indique.
- Toutes les parties d'un même clic sont produites en **un seul lot par modèle vocal** : les requêtes précédant le lot s'exécutent une fois, puis tous les morceaux sont lus, puis les requêtes suivant le lot s'exécutent une fois. C'est ce qui évite à la carte graphique de changer de modèle à chaque partie.
- Chaque morceau est **conservé selon son contenu** (texte, voix, modèle, langue, paramètres, graine). Une partie déjà à jour ne coûte
  rien, un paragraphe modifié est le seul morceau refait, et une partie en échec conserve les morceaux déjà produits lorsque vous appuyez sur **Réessayer**.
- Les morceaux sont assemblés avec des pauses qui dépendent de leur place (après un titre, entre les paragraphes, avant *dit-il*) en un fichier mp3 par partie, conservé avec la partie.
- Pendant qu'une génération lancée par vous s'exécute, la synthèse vocale attend, afin que le modèle de langage ne soit pas déchargé sous elle.
- Une partie que vous marquez **finale** dans le studio est laissée intacte, et la boîte de dialogue indique combien il y en a.
- **Le livre audio complet** assemble toutes les parties ayant un audio à jour en un seul fichier mp3, avec un repère de chapitre pour chaque partie, et affiche un lien de téléchargement sous la liste (les parties exclues sont nommées). Le même fichier est le format d'export *un mp3 avec repères de chapitres*.
- **Tout arrêter** annule les parties en attente et interrompt immédiatement celle en cours : la requête au modèle de langage (l'étiquetage) ou au serveur de synthèse vocale (le morceau en cours de lecture) est abandonnée, sans être attendue. Un lot arrêté avant de commencer n'envoie pas les requêtes qui le précèdent, si bien que le modèle de langage reste chargé. **Télécharger** fournit le mp3 d'une partie terminée.
<a id="the-audiobook-studio"></a>

## Le studio de livre audio

*Menu Tools → **Audiobook studio*** (la voix de la pièce sélectionnée, avec un aperçu, est à côté de son locuteur) est l'endroit où le livre audio final est fabriqué, une partie à la fois : vous voyez le script, le modifiez, écoutez une pièce et réalisez la partie.

![Le studio avec une réplique sélectionnée](../../images/plugin-audiobook-studio.png)

- Pendant que la partie est annotée ou fabriquée, une barre indique qu'un travail est en cours, et les boutons qui lanceraient d'autres travaux attendent.
- **Annoter** crée le script de la partie (avec le modèle de langage si vous le demandez, sinon avec le narrateur) et affiche les pièces dans un tableau : **speaker**, **delivery**, **intensity**, spoken text et une marque pour une pièce que vous avez modifiée. Pendant son exécution, un bouton **Arrêter** apparaît à côté et met fin à la requête au modèle de langage.
- Sélectionnez une pièce pour modifier son **speaker**, son **spoken text**, sa **delivery**, son **intensity** (l'exagération de la voix, pour les modèles qui en ont une ; sur Higgs, la **delivery** et l'**intensity** sont les marques comme `<|emotion:sadness|>` au début du spoken text, qui les suivent quand vous les modifiez), sa **language**, sa **seed**, sa **pause after**. **Appliquer** garde la modification et **verrouille** la pièce : une nouvelle annotation laisse une pièce verrouillée telle quelle.
  **Unlock** la rend à l'annotation. *The request the speech server gets* affiche la requête exacte.
- **Hear** fait l'audio d'une pièce (cette pièce seulement ; le serveur de parole est démarré pour elle). **Another take** la refait avec une nouvelle seed, et les **takes** sont conservés pour que vous choisissiez celui que vous aimez (*Use this take and hear it*). Une bonne voix dépend souvent de la seed.
- **Make the part from this script** assemble les pièces telles quelles, sans nouvelle annotation.
- Le **status** de la partie est *draft*, *reviewed* ou *final*. Une partie *final* n'est plus annotée ni convertie à nouveau, et l'export peut se limiter aux parties *final*.

<a id="exporting"></a>

## Exportation

Le livre audio est un format de l'*Exporter l'histoire* du livre : **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** et, lorsque ffmpeg est installé, **Audiobook (one m4b with chapters)**. Ils sont faits de l'audio des parties à jour : une partie sans audio, ou dont l'audio est plus ancien que son texte ou que son script dans le studio, est laissée de côté, et la fenêtre indique lesquelles. Les fichiers mp3 portent des balises ID3 (le titre du livre, le chapitre, la piste), et le fichier unique a des marques de chapitre que des lecteurs comme VLC et la plupart des applications de livres audio affichent ; le zip contient une liste de lecture. La case **Audiobook: only the parts marked final** limite l'export aux parties que vous avez terminées dans le studio.

Le **m4b** est le format qu'Apple Books et la plupart des applications de livres audio sur téléphone attendent : audio AAC (64 kbit/s, suffisant pour la parole) dans un conteneur MP4, avec les chapitres, le titre, l'auteur et le genre. C'est ffmpeg qui le fabrique, donc ce format n'est proposé que sur un serveur qui a ffmpeg. L'audio stocké en mp3 est encodé une seconde fois pour cela ; l'audio stocké en wav n'est encodé qu'une fois.

<a id="how-the-audio-is-stored"></a>

### Comment l'audio est stocké

Un administrateur le choisit pour toute l'installation dans *Settings → Extension Settings → Audiobook Settings* (la dernière section, visible seulement par les administrateurs) :

- **mp3** (par défaut) : léger, assemblé sans décodeur, ffmpeg n'est pas nécessaire. Le m4b est une seconde étape avec perte.
- **wav** : sans perte, environ dix fois plus lourd (une heure de parole pèse environ 170 Mo), nécessite ffmpeg. Les pièces sont demandées au serveur de parole en wav et assemblées en wav ; le navigateur les lit telles quelles ; les exports mp3 et zip sont encodés par ffmpeg (96 kbit/s) et le m4b n'est encodé qu'une fois.

Le choix s'applique à ce qui est fait à partir de maintenant. Les parties faites avant gardent leur audio et sont reconnues telles quelles ; une partie des deux types (une pièce verrouillée du studio, d'avant) est assemblée en wav, et un export de parties des deux types fonctionne, mais nécessite ffmpeg.

Un export du livre entier est fait à partir des fichiers stockés des parties, dans des fichiers temporaires du serveur, puis téléchargé depuis là ; sa durée n'est donc pas limitée par la mémoire de l'application, mais le dossier temporaire a besoin de place pour environ le double de l'audio du livre (avec wav, environ 350 Mo par heure) pendant sa fabrication.

![Le dialogue d'export avec les formats de livre audio](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversion automatique

Un livre peut produire l'audio d'une nouvelle partie de lui-même, dès que la partie est *final* : après l'annotation automatique, la réécriture et les traductions si le livre en a, comme dernière étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part) (dans le panneau des exécutions, jusqu'à ce que l'audio soit fait ; son **Arrêter** retire la partie de la file). Elle est **désactivée** par défaut. Choisissez-la dans l'onglet *Prompts* du livre sous les paramètres d'extension, pour un dossier de livres, ou pour tous vos livres dans les paramètres du livre audio ; le réglage le plus proche l'emporte :

- **Non**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices** ; un livre sans modèle reçoit la narration simple, et vous en êtes informé.

![Le réglage dans un livre](../../images/plugin-audiobook-auto.png)

La partie entre dans votre file, avec la distribution et la prononciation du livre. Un message vous indique qu'elle est en file, un autre que son audio est prêt ou a échoué ; ils n'apparaissent que sur la page qui a lancé la génération, et la tâche se trouve de toute façon dans la fenêtre *Convert to audio*.

<a id="listening"></a>

## Écoute

Un livre publié (ou le lecteur) qui a des parties avec audio reçoit une barre de lecture : lecture, pause, arrêt, vitesse, et la partie entendue est marquée dans le texte. Elle démarre à partir de la sélection ou de la première partie visible, joue les parties dans l'ordre et charge la suivante pendant que la courante joue ; les commandes multimédia de l'appareil connaissent le livre. L'audio fait à partir d'un texte plus ancien d'une partie n'est pas proposé. L'audio est servi au propriétaire du livre et, tant que le livre est publié, à toute personne pouvant l'ouvrir ; un livre retiré de la publication cesse d'être servi immédiatement (à la partie suivante que charge le lecteur).

![Le lecteur dans la liseuse](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Une partie sans audio

Le lecteur s'arrête à une partie sans audio, pour qu'aucune partie de l'histoire ne soit sautée, et le signale. Lorsque l'extension [Read Aloud](tts-reader.md) est installée, la barre affiche aussi un bouton haut-parleur, **Read this part (and the next ones without audio) with the voice of the browser**. Rien n'est lu avec une voix de machine sans que vous ne l'appuyiez. Il lit alors cette partie et les parties suivantes sans audio avec la voix de votre appareil, et lorsqu'il atteint une partie avec audio, le lecteur reprend la main. Les parties sans audio suivantes dans la même écoute sont lues de la même façon, sans nouvelle demande ; **Arrêter** la fait taire, et relancer le lecteur redemande. La voix, la vitesse et la hauteur sont celles de Read Aloud.
<a id="commands"></a>

## Commandes

Les mêmes choses que les [commandes](../commands/index.md) (la ligne de commande de l'éditeur de récit, la macro `command` d'un modèle, un agent). Les parties sont numérotées comme les numérote la boîte de dialogue Convert, ou désignées par leur identifiant.

| Commande | Ce qu'elle fait |
|---|---|
| `/audiobook-status [from [to]]` | Le serveur de synthèse vocale utilisé, la manière dont le livre se convertit tout seul, le nombre de parties qui ont un audio (aucune, prête, plus ancienne que le texte, plus ancienne que le script), l'état de chaque partie listée dans le studio, et la file d'attente. Lente seulement lorsque le livre est long. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Met les parties dans la file d'attente (le livre entier sans argument, une seule partie lorsque seul `from` est donné). `labelling` `true` utilise le modèle du livre ; `what` est `audio` ou `script` (seulement le script pour le studio, rien n'est prononcé). Avec `wait` `true`, la commande reste en place jusqu'à la fin des tâches, affiche leur avancement et indique comment chacune s'est terminée ; l'arrêter annule les tâches qui n'ont pas encore commencé. |
| `/audiobook-stop` | Annule les parties en attente ; celle en cours s'arrête après son morceau. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` ou `final` dans le studio ; la partie a besoin d'un script. |
| `/audiobook-cast` | Le narrateur, la voix de secours et les personnages de la distribution avec leurs acteurs. |

![Le résultat d'une commande](../../images/plugin-audiobook-command.png)

L'audio est produit pour **votre** livre, et non pour la copie de travail cachée d'un [agent](../agents.md) : un agent peut lire la distribution, mais reçoit un refus des autres commandes, qui indiquent que vous devez les lancer vous-même. Lancez-les depuis la ligne de commande, ou confiez à un agent la préparation du livre (annoter, relire) et convertissez-le vous-même.

## Notifications

Lorsqu'un lot de conversions est terminé, le navigateur peut vous prévenir ([notifications](../account.md#notifications), pour que vous puissiez quitter l'onglet pendant que le serveur de synthèse vocale travaille). Dans la liste des notifications de la page *Paramètres*, il y a deux lignes : **Convert to audio** (les lots que vous lancez dans la boîte de dialogue ou avec la commande) et **Convert to audio (automatic)** (une nouvelle partie transformée en audio automatiquement). Un lot que vous avez arrêté ne notifie pas ; un lot contenant une partie en échec signale qu'il a échoué.

![La liste des notifications](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Ce qu'il ne fait pas (encore)

- Mp3 et wav uniquement (pas d'opus) ; le volume sonore des morceaux est celui que donnent les voix.
- Pas d'effets sonores ni d'ambiance.
- Deux moteurs de synthèse vocale, Chatterbox et Higgs Audio v3 (dans ComfyUI).
- L'essai d'une voix en dehors d'un segment.
- La distribution ne fait pas partie de la sauvegarde des *acteurs* d'un livre : un livre restauré conserve sa distribution, mais désigne des acteurs qui doivent exister dans votre compte.
