---
label: Livre audio
order: 27
---

# Livre audio

Transforme un livre en livre audio, partie par partie, avec un serveur de synthèse vocale : une voix pour le narrateur et une pour chaque personnage, conservées avec les parties du livre et lues dans le lecteur. Vous pouvez le laisser tourner (le modèle indique qui parle et comment, le serveur de synthèse lit) pour un brouillon rapide, ou travailler les parties une à une dans le **Audiobook studio**, en regardant exactement ce qui est envoyé au serveur de synthèse et en le modifiant à la main, puis exporter le résultat en fichiers mp3 avec des marques de chapitre.

L'extension ne produit pas la voix elle-même : elle communique avec un **Chatterbox-TTS-Server** que vous faites tourner (voir [Mise en place du serveur de synthèse vocale](audiobook-server.md)). Le texte du livre n'est jamais modifié et n'est envoyé à personne d'autre que les serveurs que vous avez indiqués ; l'audio est stocké dans votre propre installation, sous forme de ressources liées aux parties.

<a id="what-you-need"></a>

## De quoi vous avez besoin

- Un serveur de synthèse vocale et, s'il partage la carte graphique avec votre modèle de langage, les requêtes qui libèrent de la place pour lui et l'arrêtent ensuite ([Mise en place du serveur de synthèse vocale](audiobook-server.md)).
- Des voix : un court **extrait de référence** (de 12 à 30 secondes d'une seule personne, sans musique) pour chaque voix que vous voulez cloner, ou l'un des fichiers de voix fournis par le serveur. Un narrateur a besoin d'une voix de lecture calme et régulière.
- Pour un livre à plusieurs voix, un modèle de langage capable d'étiqueter le texte. Par défaut, c'est le modèle du livre.
- **ffmpeg** est facultatif. Sans lui, tout fonctionne en mp3. Avec lui sur le serveur (il est présent dans l'image Docker ; sur un ordinateur de bureau, il est trouvé dans le `PATH` ou dans les dossiers habituels tels que `/opt/homebrew/bin` ; la variable `MARGINALIA_FFMPEG` indique son emplacement lorsqu'il est ailleurs), l'export obtient le format **m4b**, et un administrateur peut stocker l'audio en wav.

<a id="the-settings"></a>

## Les paramètres

*Paramètres → Extension Settings → Audiobook Settings* est le panneau de votre compte ; enregistrez la page *Paramètres* après l'avoir modifié.

![Le panneau avec un backend et les acteurs](../../images/plugin-audiobook-settings.png)

### Backends

Un backend est un serveur de synthèse vocale et le modèle avec lequel il parle. Ajoutez-en un avec **+**. **Duplicate** copie le backend sélectionné (adresse, clés, verrou GPU, voix, valeurs par défaut et requêtes avant et après un lot) sous un nouveau nom : c'est le moyen rapide d'obtenir un backend d'un autre modèle sur la même machine, par exemple Higgs à côté de Chatterbox, où vous changez ensuite le modèle, l'adresse et les requêtes de démarrage et d'arrêt.

| Réglage | Ce que cela fait |
|---|---|
| Name | Le nom affiché dans la fenêtre Convert. |
| Address | L'adresse de base du serveur de synthèse vocale, ou de ce qui le relaie, par exemple `http://localhost:8004`. |
| Api key | Envoyée comme jeton bearer si le serveur (ou votre proxy) en demande un. Elle est stockée chiffrée et n'est plus affichée ; saisissez-en une nouvelle pour la remplacer, ou utilisez **Remove the key**. |
| Speech model | **Chatterbox Turbo** (anglais, avec des balises sonores telles que `[sigh]`, le plus rapide et celui par défaut), **Chatterbox** (anglais, avec une *exaggeration* de l'interprétation), **Chatterbox Multilingual** (23 langues) ou **Higgs Audio v3 in ComfyUI** (de nombreuses langues, voix qui adaptent l'humeur d'une réplique, et sons tels que pleurs, halètements ou gémissements ; voir [ci-dessous](#higgs-audio-v3-emotions-and-sounds)). Le [verrou GPU](../administration/gpus.md) du backend (ou les requêtes avant un lot) doit maintenir ce modèle chargé. Un livre qui a besoin d'un autre modèle utilise un autre backend. |
| GPU lock | Le [verrou GPU](../administration/gpus.md) du GPU sur lequel tourne le serveur, lorsqu'il le partage avec le modèle de langage ou d'autres serveurs (y compris le [Générateur d'images](image-generator.md)) : chaque lot et chaque échantillon attend son tour sur le GPU, et le verrou charge d'abord le serveur de synthèse vocale. Affiché uniquement si l'administrateur a configuré des verrous GPU. |
| The server is on this machine or on my network | Autorise les adresses du réseau local. Laissez-le désactivé pour un serveur dans le cloud : il ne peut alors pas être amené à appeler votre propre machine. |
| Timeout of a request | Le délai d'attente d'un morceau de synthèse vocale. |
| Restart the server after (pieces) | Pour un serveur de synthèse qui fuit en mémoire et est tué par le système lors d'un long lot : après ce nombre de morceaux, les requêtes qui suivent le lot puis celles qui le précèdent sont à nouveau envoyées au milieu du lot (arrêt, démarrage, chargement), et le travail continue avec un serveur neuf. 0 (la valeur par défaut) ne le fait jamais. Un serveur qui plante est redémarré quelle que soit cette valeur, voir [Dépannage](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | La clé que les requêtes avant et après un lot transmettent sous la forme `Authorization: Bearer`. Elle est stockée chiffrée et n'est plus affichée (**Remove the key** la supprime), afin de ne pas avoir à être écrite dans les en-têtes de chaque requête, où elle serait lisible. Vide : c'est la *Api key* ci-dessus qui est utilisée, ce que vous voulez lorsqu'un seul proxy, comme LiteLLM, sert tout. Une requête qui possède déjà son propre en-tête `Authorization` le conserve. |
| Narrator voice | Le narrateur d'un livre sans distribution. Un fichier de voix du serveur s'écrit `predefined:Henry.wav`. |
| Precision | Propre à Higgs : la précision avec laquelle le modèle calcule. **fp32** (la valeur par défaut) est le choix sûr : sur une carte AMD, **bf16** a transformé des morceaux en charabia, en effets sonores et en voix erronées. bf16 demande environ la moitié de la mémoire graphique (environ 8 Go au lieu de 16) et est plus rapide là où il fonctionne. |
| Send the text of the clip to Higgs | Propre à Higgs, expérimental : le [texte de l'extrait](#voices-and-clips) est envoyé avec lui. Laissez-le désactivé : lors des tests, il a modifié la voix. |
| Check the length of the pieces | Activé par défaut. Un morceau bien plus long que son texte (4 s plus 0,6 s par mot : le modèle a continué à parler, ou a produit un son à la place des mots) est refait avec la graine suivante, jusqu'à **Nouvelles tentatives** fois ; la première prise de la bonne durée est conservée, sinon la plus courte. |
| Check the words with speech recognition | Désactivé par défaut ; nécessite la [reconnaissance vocale](#speech-recognition). Après chaque lot, chaque morceau est transcrit et comparé à son texte. Un morceau qui s'écarte de plus de **Max. word errors (%)** de ses mots est refait avec la graine suivante et vérifié à nouveau, jusqu'à **Nouvelles tentatives** essais ; la prise comportant le moins de mots erronés est conservée. La conversion indique combien de morceaux ont été refaits et combien diffèrent encore. |
| Defaults of a segment | Température, poids CFG, vitesse et taille de segment avec lesquels un morceau démarre (le studio peut les modifier pour un seul morceau). Higgs n'utilise que la température. |
| Before a batch, After a batch | Requêtes envoyées autour de chaque lot de parties (et de chaque échantillon), à l'intérieur du verrou GPU. Libérer la carte graphique et démarrer le serveur de synthèse relève du verrou GPU ; voir [Un seul GPU pour le modèle et la synthèse](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vide : ne fait rien. |

**Test connection** fait ce que fait un lot : il prend le verrou GPU (ce qui démarre un serveur de synthèse arrêté), envoie les requêtes avant un lot, demande au serveur s'il est là et s'il a un modèle chargé, puis envoie les requêtes après le lot. Sans verrou ni requêtes, il se contente de demander.

Un livre qui comporte des morceaux dans une autre écriture (du japonais dans un livre en anglais) les envoie au modèle multilingue, si vous avez un backend pour lui, avec leur langue ; ils restent sur le modèle du livre si vous n'en avez pas. Un livre Higgs les conserve : Higgs les lit lui-même, dans la même voix.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3 : émotions et sons

Avec un backend **Higgs Audio v3 dans ComfyUI**, les étiquettes du modèle de langage sont entendues. L'adresse du backend est celle de votre ComfyUI
(avec les nœuds TTS Audio Suite, voir [Configurer le serveur de voix](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **diction** d'un passage de parole est parlée : triste, en colère, joyeuse, amusée, tendue, effrayée, surprise, tendre, sarcastique, suppliante, désespérée,
  dégoûtée, excitée, un chuchotement ou un cri. Une diction forte (intensité 0,8 et plus) est plus expressive, une diction faible (en dessous de 0,2) est lue sobrement.
  Le narrateur est lu sobrement.
- Le modèle de langage peut mettre davantage de **sons** dans la parole que pour Chatterbox Turbo : rire, petit rire, gloussement, soupir, halètement, toux, raclement de gorge,
  reniflement, éternuement, gémissement, pleurs, sanglot, cri, respiration haletante, plainte, fredonnement et chut.
- Le programme lit aussi la **narration autour de chaque passage de parole** (en anglais) et donne à la parole le son qu'elle nomme, lorsque le modèle n'en a mis aucun :
  *"You look funny," she giggled.* reçoit un gloussement, *She burst out laughing. "Careful!"* un rire, *a soft moan escaped her lips. "Yes..."* un gémissement,
  *panting* ou *out of breath* une respiration haletante, *cried quietly* ou *burst into tears* des pleurs. Il laisse de côté ce qui n'a que l'apparence d'un son : *"Stop!" she cried.*
  (une exclamation), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, ainsi qu'un son que la narration
  attribue à un autre personnage nommé. *"You look funny," he mocked her.* reçoit une diction sarcastique ou amusée et aucun son. La narration elle-même est lue telle qu'écrite.
- **Le narrateur est toujours lu sobrement** : ni sons ni humeur, y compris pour un passage que le modèle a laissé au narrateur.
- Higgs n'a pas besoin d'un extrait par humeur : un seul extrait d'une voix suffit. Il reproduit l'enregistrement de l'extrait, donc un extrait propre compte davantage qu'avec Chatterbox
  ([Un bon extrait](#a-good-clip)).
- Chaque passage est produit par ComfyUI, dont la mémoire est vidée après lui (quelques secondes par passage), ce qui évite que certaines cartes graphiques plantent.
- Higgs continue souvent après le dernier mot d'un passage (un souffle, le début d'un autre son), et l'extrait s'arrête au milieu de ce son, ce qui s'entend comme un bref « whoosh » avant la pause. Avec l'extension **ffmpeg**, la fin superflue est coupée et chaque passage est atténué en fondu ; sans elle, les passages restent tels que Higgs les a produits.
- Le modèle Higgs Audio v3 est sous une **licence de recherche et non commerciale** : un livre audio que vous vendez nécessite une licence de Boson AI.

<a id="speech-recognition"></a>

### Reconnaissance vocale

La **reconnaissance vocale** transcrit ce que dit un passage de parole. Elle vérifie les passages d'un lot (lorsqu'un backend a **Check the words with speech
recognition** activé) et reconnaît le [texte d'un extrait vocal](#voices-and-clips). Choisissez l'endroit où elle s'exécute :

| Réglage | Ce qu'il fait |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)** : le modèle de parole Granite de la TTS Audio Suite dans votre ComfyUI (l'adresse est celle de ComfyUI). **OpenAI-compatible (whisper)** : tout serveur doté de `/v1/audio/transcriptions`, comme un serveur whisper (faster-whisper, speaches) ou l'API OpenAI. |
| Address, Api key | L'adresse de ComfyUI, ou la base de l'API (avec ou sans `/v1`) ; la clé est stockée chiffrée. Adresse vide : pas de reconnaissance vocale. |
| GPU lock | Le [verrou GPU](../administration/gpus.md) que prend la reconnaissance. Sur une seule carte avec Higgs en fp32, donnez-lui un verrou à part dont les étapes redémarrent ComfyUI : ComfyUI garde le modèle de parole en mémoire du serveur, et charger la reconnaissance à côté a fait manquer de mémoire au serveur. |
| Model of the recognition | Vide : `granite-speech-4.1-2b` pour ComfyUI, `whisper-1` pour le serveur compatible OpenAI. |
| Timeout, The server is on this machine or on my network | Comme pour un backend. |

**Test connection** vérifie que ComfyUI dispose des nœuds, ou que le serveur répond. Les passages d'un lot sont vérifiés après le lot, lorsque le serveur de voix est rendu,
de sorte que la reconnaissance et le modèle de parole n'ont jamais besoin de la carte en même temps.

<a id="cleanup-of-clips"></a>

### Nettoyage des extraits

**Cleanup of clips** nettoie un extrait de référence sur un ComfyUI avec la [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (la même
que pour les œuvres Higgs). Le résultat est une nouvelle **version** de l'extrait dans [Voix et extraits](#voices-and-clips) ; l'extrait lui-même reste.

| Réglage | Ce qu'il fait |
|---|---|
| Address, Api key | L'adresse de ComfyUI ; la clé est stockée chiffrée. Adresse vide : pas de nettoyage. |
| GPU lock | Le [verrou GPU](../administration/gpus.md) que prend le nettoyage. Donnez-lui un verrou à part, et non celui de la parole ou de la reconnaissance vocale, dont les étapes redémarrent ComfyUI, afin que les modèles de nettoyage ne soient jamais chargés à côté du modèle de parole. |
| Timeout, The server is on this machine or on my network | Comme pour un backend (le délai concerne un seul nettoyage, 10 minutes par défaut : ComfyUI télécharge un modèle la première fois qu'il est utilisé). |
| Cleanup | Les nettoyages (préréglages) proposés dans Voix et extraits, par nom. Chacun est une liste d'étapes exécutées l'une après l'autre. **Default cleanups** rétablit ceux qui sont fournis avec. |

Une étape est l'une des suivantes :

- **Separation model** : un modèle de la suite qui sépare le son en deux, la partie *extracted* et la partie *remaining*, et en garde une. Quelle partie contient la voix dépend du modèle : un modèle de voix seule (`MelBandRoformer`) extrait la voix, un modèle de débruitage extrait le bruit, donc gardez la partie
  *remaining*. **Aggressiveness** (0 à 20) détermine la quantité retirée.
- **Voice fixer** : restaure une voix dégradée (un téléphone, un vieil enregistrement). Il modifie davantage le son de la voix qu'une séparation.
- **ffmpeg (here)** : le filtre de **Clean with ffmpeg**, exécuté par l'application entre les étapes sur ComfyUI.

Les nettoyages fournis sont : *Voice from music* (la voix extraite d'un film ou d'une chanson), *Remove noise*, *Restore the voice* et *Film or game clip* (la
voix extraite de la musique, puis le bruit retiré). **Test connection** vérifie que ComfyUI dispose des nœuds et remplit la liste des modèles avec ceux de votre
serveur. Les modèles sont téléchargés par ComfyUI la première fois qu'un nettoyage les utilise.

<a id="shared-speech-servers"></a>

### Serveurs de voix partagés

Sur une installation à plusieurs utilisateurs, l'administrateur peut tenir des serveurs de voix pour tout le monde, afin qu'une machine avec GPU soit configurée une seule fois et que personne n'ait besoin d'une adresse ou d'une clé. L'administrateur le fait dans le même panneau (les dernières sections, visibles par les seuls administrateurs) : **Shared speech servers** ouvre une fenêtre avec les mêmes champs qu'un backend personnel, ainsi que la liste de ceux qui peuvent l'utiliser : **all users**, ou les utilisateurs choisis dans la liste. Un interrupteur, **Users may add speech servers of their own**, décide si les backends personnels restent autorisés.

Ce que vous obtenez :

- **Backend in use** en haut du panneau liste les serveurs partagés que vous pouvez utiliser (marqués *(shared)*) et les vôtres. La fenêtre *Convert to audio* et le studio utilisent celui que vous choisissez ; les commandes l'utilisent aussi.
- Vous ne voyez que le nom d'un serveur partagé, jamais son adresse, ses clés ni ses requêtes. Les lots de tous les utilisateurs sur une même adresse de serveur attendent leur tour, de sorte que deux utilisateurs ne peuvent pas démarrer la même carte GPU en même temps.
- Lorsque l'administrateur vous retire un serveur, un travail qui le désigne encore est refusé ; choisissez-en un autre.
- Lorsque l'administrateur désactive vos propres serveurs, le panneau le signale. Ceux que vous avez ajoutés sont conservés et redeviennent utilisables si l'interrupteur est remis en marche.
- La *Narrator voice* d'un serveur partagé doit être un fichier de voix du serveur (`predefined:Henry.wav`) : un extrait de référence est une ressource d'un seul utilisateur, et les autres ne peuvent pas le lire. Les voix de vos acteurs fonctionnent comme d'habitude.
<a id="actors"></a>

### Acteurs

Un **acteur** est une de vos voix : un extrait de référence (l'un de vos [ressources](../resources.md) audio, ou un nouvel extrait envoyé ici), ou un
fichier de voix du serveur, avec des voix à lui pour une **humeur** (un chuchotement, un cri) ou une **langue** (un extrait qui parle
japonais). La langue l'emporte sur l'humeur, et sans voix pour elles, c'est la voix de l'acteur qui lit. Vos acteurs sont conservés dans
votre compte ; n'importe quel livre peut donner un acteur à son narrateur et à ses personnages.

**Aperçu** à côté d'une voix (dans l'acteur, les réglages et la distribution) lit avec elle quelques phrases (une courte, dans la langue de la voix pour une langue) et la fait jouer ; quand le GPU occupe autre chose, le verrou du GPU charge d'abord le serveur de synthèse vocale. La flèche à côté propose d'autres échantillons : **avec des sons** (un soupir, un rire, un halètement) pour les modèles qui les prononcent (Chatterbox Turbo, Higgs Audio v3), et pour Higgs chaque **humeur** de l'étiquetage (triste, chuchoté, sarcastique...). Les acteurs sont listés par nom.

Chaque échantillon ne prend le GPU que pour lui-même. Le GPU reste chargé avec le serveur de synthèse vocale après un échantillon, de sorte que les suivants viennent tout de suite, jusqu'à ce qu'une requête du modèle de langue (ou d'un autre serveur) ait besoin du GPU. Ouvrir **Voix et extraits** charge déjà le serveur de synthèse vocale, de sorte que son premier échantillon n'attend pas.

![Un acteur avec une voix pour une humeur](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Voix et extraits

**Voix et extraits** (à côté des acteurs) est une seule fenêtre pour vos acteurs et les extraits dont leurs voix sont clonées. Elle a trois onglets, côte à côte au départ : **Acteurs**, **Extraits** et **Extrait**. Faites glisser un onglet pour le placer à côté, sous ou avec un autre ; la fenêtre se rouvre comme vous l'avez laissée.

- **Acteurs** : chacun avec sa voix, le nombre d'autres voix (humeurs, langues) qu'il a et la pire qualité de ses extraits ; ajouter, modifier ou supprimer un acteur.
- **Extraits** : vos extraits dans des **dossiers** (par exemple un par personnage), avec les **versions** faites à partir d'un extrait placées dessous, chacune avec sa durée, sa **qualité** et l'endroit où elle sert (acteurs, avec l'humeur ou la langue, et les narrateurs de vos moteurs). **Ajouter un extrait** en envoie un.
- **Extrait** : l'extrait sélectionné.
  - **Nom** et **Dossier** : renommer l'extrait (le fichier garde son nom) et le ranger dans un dossier, un nouveau en tapant son nom. Une version reste dans le dossier de l'extrait dont elle est issue, et indique à partir de quoi et comment elle a été faite.
  - **Lire** l'extrait, lire ce qui ne va pas avec lui, **Remplacer...** ou **Supprimer** : **Supprimer** n'est possible que si rien ne l'utilise ; ses versions remontent alors à l'extrait dont elles sont issues.
  - **Nettoyer avec ffmpeg** fait une version où le souffle et le bourdonnement constants sont réduits. Cela ne retire ni la musique, ni les effets, ni les autres voix.
  - **Nettoyer avec l'IA** fait une version avec le [nettoyage](#cleanup-of-clips) choisi sur ComfyUI. Il tourne en arrière-plan : vous pouvez continuer à travailler, et un message indique quand la version est prête.
  - **Utiliser cette version** place une version à l'endroit où l'extrait dont elle est issue est utilisé. Les parties sont refaites avec la nouvelle voix, et l'ancien extrait reste jusqu'à ce que vous le supprimiez.
  - **Texte de l'extrait** : les mots prononcés dans l'extrait. Tapez-les, ou appuyez sur **Reconnaître** pour que la [reconnaissance vocale](#speech-recognition) les transcrive, corrigez-les et **Enregistrer**. La fenêtre de l'acteur montre le même champ pour l'extrait de la voix de l'acteur. Une version et un remplacement reprennent le texte. Higgs ne le reçoit que si le moteur l'envoie (désactivé par défaut).
- **Remplacer...** place un autre extrait, l'un des vôtres ou un que vous envoyez, partout où l'extrait est utilisé.

Les sélecteurs d'une voix (l'acteur, le narrateur d'un moteur) affichent un extrait sous la forme *dossier / extrait › version*.

<a id="a-good-clip"></a>

#### Un bon extrait

Un modèle de voix copie l'**enregistrement**, pas seulement la voix : la pièce, un souffle, la musique sous une scène de film reviennent dans chaque réplique, souvent sous la forme d'un léger « whoosh » sous les mots que nul filtre du résultat ne supprime. Donc :

- Utilisez un enregistrement propre d'un seul locuteur avec de vraies pauses (un échantillon d'acteur de doublage, une lecture faite chez soi dans une pièce calme), de 12 à 30 secondes. Un extrait découpé dans un film ou une série est le pire cas.
- Les repères de qualité sont mesurés sur l'extrait : **Propre** (les pauses sont au moins 45 dB sous la parole), **Correct** (35 à 45 dB, ou plus long que 30 s, que Chatterbox coupe), **Mauvais** (bruit de fond, sous 35 dB ; déformé ; plus court que 8 s). Un extrait qui n'est pas propre est signalé lorsque vous l'ajoutez.
- Nettoyez un extrait avec l'IA ([Nettoyage des extraits](#cleanup-of-clips)) : lors du test, le nettoyage *Extrait de film ou de jeu* a fait passer un extrait avec souffle, bourdonnement et écho de *Mauvais* à *Propre* (les pauses sont passées de 38 à 89 dB sous la parole). Écoutez la version avant de l'utiliser : un modèle de séparation peut aussi retirer une respiration ou la fin d'un mot doux. Le nettoyage ffmpeg ne réduit qu'un bruit constant : sur un extrait de film, il a gagné environ 3 dB et l'extrait est resté *Mauvais*.
- Sans ComfyUI, nettoyez un extrait à la main : la réduction de bruit d'[Audacity](https://www.audacityteam.org/) avec un profil de bruit pris dans une pause de l'extrait, ou un suppresseur de voix comme [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) pour un extrait avec de la musique.

<a id="converting-new-parts-by-itself"></a>

### Convertir les nouvelles parties d'elles-mêmes

*Convertir les nouvelles parties en audio d'elles-mêmes* est le réglage par défaut pour tous vos livres ; voir [Conversion automatique](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## La distribution et la prononciation d'un livre

Les deux sont conservées avec le livre (elles voyagent dans sa sauvegarde) et se modifient depuis la fenêtre **Convertir en audio**.

**Distribution** indique qui lit : un acteur pour le **narrateur**, un pour les **autres locuteurs** (quiconque le modèle nomme et que la distribution n'a pas ; vide : le narrateur lit) et un pour chaque **personnage**. Un personnage a un nom et d'autres noms que le texte peut utiliser pour lui (*Tom* pour *Tomáš Novák*), séparés par des virgules ; une réplique est attribuée au personnage lorsque son nom, l'un des autres noms, ou un mot du nom qu'aucun autre personnage ne partage, correspond, sans tenir compte de la casse ni des accents. **Ajouter les personnages du livre** remplit la liste à partir du point de vue et des personnages présents dans les parties.

![La distribution d'un livre](../../images/plugin-audiobook-cast.png)

**Prononciation** est la façon dont le texte est écrit *pour la voix* ; le livre lui-même n'est jamais modifié. Le **dictionnaire** remplace des mots entiers (la casse compte) par ce qui doit être dit pour eux : une réécriture phonétique pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Pour un livre en anglais, les règles lisent aussi à voix haute les autres éléments : nombres, ordinaux, années, heures, sommes d'argent et abréviations en mots, un titre qui n'est qu'un nombre comme *Chapter four. The Arch* (le mot placé avant le nombre est le vôtre), les marques markdown telles que `*` sont retirées, et un changement de scène (`***`, `---`) n'est pas lu mais constitue une pause plus longue. Désactivez les règles pour un livre dans une autre langue ; le dictionnaire s'applique toujours. **Essayer un texte** montre comment un passage sera lu. Un passage dont le texte prononcé change est refait, les autres sont conservés.

![La prononciation d'un livre](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertir un livre

*Menu Tools de l'éditeur de récit (le bouton roue dentée) → **Convert to audio***.

![La boîte de dialogue avant le démarrage](../../images/plugin-audiobook-convert.png)

1. Choisissez le **backend** et appuyez sur **Load** : les parties contenant du texte de la branche active sont listées (la première est la 1) avec l'état de chacune :
   *Non générée*, *Étiquetée* (les étiquettes du modèle sont enregistrées, pas encore d'audio), *Générée* (avec un bouton de lecture et un téléchargement) ou
   *Générée (le texte a changé)* (l'audio a été fait à partir d'un texte plus ancien et sera refait) ; *(final)* s'ajoute à une partie que le studio marque comme finale.
2. Laissez **Label the parts with a language model** décoché pour la version simple : le narrateur lit chaque paragraphe. Cochez-la pour que le modèle décide qui parle dans chaque réplique, de quelle façon (calme, chuchotée, criée...) et où placer une balise sonore, et le casting fournit les voix. Le modèle du livre est choisi ; vous pouvez en choisir un autre si vous le souhaitez. Les parties sont d'abord étiquetées, puis lues. Une partie qui a déjà des étiquettes pour son texte est lue avec elles même lorsque la case est décochée (le modèle n'est pas sollicité). Relancer un livre étiqueté sans la case ne transforme donc pas ses dialogues en simple narration ; **Remove labels** permet de demander une narration simple ou de nouvelles étiquettes.
3. Cochez les parties (la case de l'en-tête les coche toutes) et **plan** ce qu'il faut en faire ; la colonne **Plan** l'affiche, et rien n'est encore fait :
   **Générer** (produit ce qui manque ; une partie à jour n'est pas refaite), **Régénérer** (supprime l'audio, retire les étiquettes, puis génère), **Delete audio** (les morceaux et l'audio assemblé de la partie ; les étiquettes restent), **Remove labels** (le modèle est à nouveau sollicité la prochaine fois que la partie est produite), **Clear the plan**. Les étapes s'additionnent sur une partie (Delete audio et Remove labels sans Générer ne font que nettoyer) ; une partie marquée finale est exclue de tous les plans.
4. **Démarrer** exécute le plan. Quand il retire de l'audio ou des étiquettes, il demande d'abord confirmation. Ce qui doit être généré est placé dans votre file d'attente, qui continue lorsque vous fermez la fenêtre et lorsque vous quittez la page ; une partie qui attend ou s'exécute dans la file est exclue d'un plan. L'état, les morceaux déjà produits et le message de la tâche s'affichent sur la ligne de la partie.
   Pendant que le modèle étiquette une partie, sa ligne affiche *Étiquetage des morceaux 17-32 sur 120* (le modèle est sollicité pour 16 morceaux à la fois) ; **cliquez sur l'état** d'une partie étiquetée pour lire, pour chaque requête, le raisonnement du modèle et sa réponse, au fur et à mesure qu'ils arrivent. Ils sont conservés en mémoire avec la tâche, jusqu'à ce que les tâches terminées soient effacées (*Clear finished*) ou que l'application redémarre.

![La file d'attente : morceaux déjà produits, et un téléchargement lorsqu'une partie est terminée](../../images/plugin-audiobook-queue.png)

Comment une partie est produite, pour que vous sachiez à quoi vous attendre :

- Le texte est **découpé par le programme**, jamais par le modèle, en morceaux de narration et en morceaux de discours direct (entre guillemets droits ou courbes, ou crochets japonais ; un titre est de la narration). Le modèle se contente d'*étiqueter* les morceaux numérotés. Un texte avec balises sonores n'est utilisé que s'il s'agit du morceau avec les balises ajoutées et que rien d'autre n'a été modifié, ainsi le modèle ne peut jamais changer votre livre.
- **Qui parle dans un morceau relève du choix du modèle pour le discours ; la narration relève du narrateur.** Une lettre, un mot, un message ou les pensées d'un personnage n'ont pas de guillemets, donc le programme les appelle narration. Un tel morceau n'est lu par le personnage (la voix du casting) que lorsqu'il **commence en italique** (`*...*` ou `_..._`) et que le modèle nomme le personnage d'après le contexte (une signature, « l'écriture ne laissait aucun doute »). La narration à la troisième personne, même lorsqu'elle est vue du point de vue d'un personnage (« Elle regarda Percy traverser la pièce »), est toujours celle du narrateur : le programme ignore le personnage que le modèle attribue à un morceau sans italique. Les italiques pour l'insistance, les sons ou les mots étrangers sont aussi de la narration. Les mots restent toujours les vôtres.
- **L'instruction donnée au modèle est un modèle que vous pouvez modifier.** *Settings → Paramètres des extensions → Audiobook Settings → Labelling prompt* est votre valeur par défaut ; un livre (onglet *Prompts* → *Paramètres des extensions* → *Audiobook*) ou un dossier peut avoir la sienne. Vide signifie celle du dossier parent, puis la vôtre, puis celle intégrée. Elle est écrite comme les autres [prompts](../books/prompts.md) : les variables sont `{{characters}}`, `{{deliveries}}`, `{{tags}}` et `{{wordsPerTag}}`, et la fenêtre d'aide contient **Insérer le modèle par défaut**. Une instruction modifiée fait redemander les étiquettes des parties à produire la prochaine fois qu'elles seront faites.
- **Le volume des parties est égalisé** lorsque le programme ffmpeg est installé (le même que celui qui produit le `.m4b`) : l'audio assemblé de chaque partie est ramené à un même niveau sonore (EBU R128, linéaire, pour que la dynamique soit conservée ; **-18 LUFS** sauf si vous le modifiez dans *Settings → Paramètres des extensions → Audiobook Settings*, où vous pouvez aussi le désactiver). Les morceaux sont conservés tels que le serveur les a produits ; seule la partie assemblée est égalisée, afin qu'un livre audio ne change pas brusquement de volume d'une partie à l'autre. **Les parties produites auparavant** ne sont pas touchées tant que vous ne les convertissez pas à nouveau : cela les réassemble à partir des morceaux stockés et les égalise, sans rien demander au serveur de synthèse vocale (ni décharger le modèle de langage). Désactiver l'égalisation laisse les parties déjà égalisées telles quelles. Si ffmpeg échoue pour une partie, celle-ci conserve son audio d'origine et le résultat l'indique.
- Toutes les parties d'un même clic sont produites en **un seul lot par modèle de synthèse vocale** : les requêtes avant le lot sont exécutées une fois, puis tous les morceaux sont lus, puis les requêtes après le lot sont exécutées une fois. C'est ce qui évite que la carte graphique change de modèle à chaque partie.
- Chaque morceau est **conservé selon son contenu** (texte, voix, modèle, langue, réglages, graine). Une partie déjà à jour ne coûte rien, un paragraphe modifié est le seul morceau refait, et une partie en échec conserve les morceaux déjà produits lorsque vous appuyez sur **Retry**.
- Les morceaux sont assemblés avec des pauses qui dépendent de leur place (après un titre, entre les paragraphes, avant *dit-il*) en un seul mp3 par partie, conservé avec la partie.
- Pendant qu'une génération de la vôtre s'exécute, la synthèse attend, afin que le modèle de langage ne soit pas déchargé pendant ce temps.
- Une partie que vous avez marquée **finale** dans le studio est laissée intacte, et la boîte de dialogue indique combien elles sont.
- **Le livre audio entier** assemble toutes les parties dont l'audio est à jour en un seul mp3 avec une marque de chapitre pour chaque partie, et affiche un lien de téléchargement sous la liste (les parties exclues sont nommées). Le même fichier correspond au format d'export *one mp3 with chapter marks*.
- **Stop all** annule les parties en attente et arrête immédiatement celle en cours : la requête au modèle de langage (l'étiquetage) ou au serveur de synthèse (le morceau en cours de lecture) est abandonnée, sans attendre sa réponse. Un lot arrêté avant de commencer n'envoie pas les requêtes qui le précèdent, si bien que le modèle de langage reste chargé. **Télécharger** donne le mp3 d'une partie terminée.
<a id="the-audiobook-studio"></a>

## Le studio du livre audio

*Menu Tools → **Audiobook studio*** (la voix du passage sélectionné, avec un aperçu, est à côté de son speaker) sert à fabriquer le livre audio définitif, partie par partie : vous voyez le script, le modifiez, écoutez un passage et fabriquez la partie.

![Le studio avec une réplique sélectionnée](../../images/plugin-audiobook-studio.png)

- Pendant qu'une partie est annotée ou fabriquée, une barre indique qu'un traitement est en cours, et les boutons qui lanceraient d'autres traitements attendent.
- **Annoter** crée le script de la partie (avec le modèle de langue si vous le demandez, sinon avec le narrateur) et affiche les passages dans un tableau : speaker, delivery, intensity, texte prononcé et une marque pour un passage que vous avez modifié. Pendant ce traitement, un bouton **Arrêter** apparaît à côté et met fin à la requête adressée au modèle de langue.
- Sélectionnez un passage pour modifier son **speaker**, le **texte prononcé**, la **delivery**, l'**intensity** (l'exagération de la voix, pour les modèles qui la proposent ; sur Higgs, la delivery et l'intensity sont les marques comme `<|emotion:sadness|>` au début du texte prononcé, qui les suivent lorsque vous les modifiez), la **language**, le **seed**, la **pause after**. **Appliquer** garde la modification et **verrouille** le passage : une nouvelle annotation laisse un passage verrouillé tel quel.
  **Unlock** le rend à l'annotation. *The request the speech server gets* montre la requête exacte.
- **Hear** crée l'audio d'un passage (seulement ce passage ; le serveur de synthèse vocale est démarré pour lui) ; **Another take** le refait avec un nouveau seed, et les **takes** sont conservés pour que vous choisissiez celui que vous préférez (*Use this take and hear it*). Une bonne voix dépend souvent du seed.
- **Make the part from this script** assemble les passages tels quels, sans nouvelle annotation.
- Le **status** de la partie est *draft*, *reviewed* ou *final*. Une partie *final* n'est ni annotée ni convertie à nouveau, et l'export peut être limité aux parties *final*.

<a id="exporting"></a>

## Exporter

Le livre audio est un format de l'option *Exporter l'histoire* du livre : **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** et, lorsque ffmpeg est installé, **Audiobook (one m4b with chapters)**. Ils sont constitués à partir de l'audio des parties à jour : une partie sans audio, ou dont l'audio est plus ancien que son texte ou que son script dans le studio, est laissée de côté, et la fenêtre indique laquelle. Les fichiers mp3 portent des balises ID3 (le titre du livre, le chapitre, la piste), et le fichier unique contient des repères de chapitres que des lecteurs comme VLC et la plupart des applications de livres audio affichent ; le zip contient une playlist. La case **Audiobook: only the parts marked final** limite l'export aux parties que vous avez terminées dans le studio.

Le **m4b** est le format que Apple Books et la plupart des applications de livres audio sur téléphone attendent : audio AAC (64 kbit/s, suffisant pour la parole) dans un conteneur MP4 avec les chapitres, le titre, l'auteur et le genre. C'est ffmpeg qui le produit, donc ce format n'est proposé que sur un serveur doté de ffmpeg. L'audio stocké en mp3 est encodé une seconde fois pour lui ; l'audio stocké en wav n'est encodé qu'une fois.

<a id="how-the-audio-is-stored"></a>

### Comment l'audio est stocké

Un administrateur le choisit pour toute l'installation dans *Settings → Extension Settings → Audiobook Settings* (la dernière section, visible seulement par les administrateurs) :

- **mp3** (par défaut) : léger, assemblé sans décodeur, pas besoin de ffmpeg. Le m4b est une seconde étape avec perte.
- **wav** : sans perte, environ dix fois plus volumineux (une heure de parole pèse environ 170 Mo), nécessite ffmpeg. Les passages sont demandés au serveur de synthèse vocale en wav et assemblés en wav ; le navigateur les lit tels quels ; les exports mp3 et zip sont encodés par ffmpeg (96 kbit/s) et le m4b n'est encodé qu'une fois.

Le choix s'applique à ce qui est fabriqué à partir de ce moment. Les parties fabriquées auparavant gardent leur audio et sont prises telles quelles ; une partie de l'un et l'autre type (un passage verrouillé du studio d'avant) est assemblée en wav, et un export regroupant des parties des deux types fonctionne, mais nécessite ffmpeg.

Un export de tout le livre est constitué à partir des fichiers stockés des parties, dans des fichiers temporaires du serveur, puis téléchargé depuis là ; sa durée n'est donc pas limitée par la mémoire de l'application, mais le dossier temporaire doit avoir de la place pour environ le double de l'audio du livre (en wav, environ 350 Mo par heure) pendant sa constitution.

![La fenêtre d'export avec les formats de livre audio](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversion automatique

Un livre peut créer lui-même l'audio d'une nouvelle partie, dès que la partie est *final* : après l'annotation automatique, la reformulation et les traductions si le livre en a, comme dernière étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part) (dans le panneau des traitements jusqu'à la fin de la création de l'audio ; son bouton **Arrêter** retire la partie de la file). Elle est **désactivée** par défaut. Choisissez-la dans l'onglet *Prompts* du livre, sous les paramètres d'extension, pour un dossier de livres, ou pour tous vos livres dans les paramètres du livre audio ; c'est le réglage le plus proche qui s'applique :

- **Non**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices** ; un livre sans modèle reçoit la narration simple, et vous en êtes informé.

![Le réglage dans un livre](../../images/plugin-audiobook-auto.png)

La partie entre dans votre file, avec la distribution et la prononciation du livre. Une notification vous indique qu'elle est en file, puis une autre que son audio est prêt ou a échoué ; elles n'apparaissent que sur la page qui a lancé la génération, mais le travail figure dans la fenêtre *Convert to audio* dans tous les cas.

<a id="listening"></a>

## Écoute

Un livre publié (ou la liseuse) qui contient des parties avec audio affiche une barre de lecture : lecture, pause, arrêt, vitesse, la partie entendue étant marquée dans le texte. La lecture part de la sélection ou de la première partie visible, joue les parties dans l'ordre et charge la suivante pendant que la courante joue ; les commandes multimédia de l'appareil connaissent le livre. L'audio fabriqué à partir d'une version plus ancienne du texte d'une partie n'est pas proposé. L'audio est servi au propriétaire du livre et, tant que le livre est publié, à toute personne qui peut l'ouvrir ; un livre retiré de la publication cesse d'être servi immédiatement (à la partie suivante, lorsque le lecteur la charge).

![Le lecteur dans la liseuse](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Une partie sans audio

Le lecteur s'arrête à une partie sans audio, pour qu'une partie de l'histoire ne soit pas sautée, et l'indique. Lorsque l'extension [Read Aloud](tts-reader.md) est installée, la barre affiche aussi un bouton haut-parleur, **Read this part (and the next ones without audio) with the voice of the browser**. Rien n'est lu avec une voix de machine tant que vous ne l'appuyez pas. Il lit alors cette partie et les parties suivantes sans audio avec la voix de votre appareil, et lorsqu'il atteint une partie avec audio, le lecteur reprend la main. Les parties sans audio qui suivent, dans la même écoute, sont lues de la même façon sans nouvelle demande ; **Arrêter** la fait taire, et relancer le lecteur demande à nouveau. La voix, la vitesse et la hauteur sont celles de Read Aloud.
<a id="commands"></a>

## Commandes

Les mêmes actions que les [commandes](../commands/index.md) (la ligne de commande de l'éditeur de récit, la macro `command` d'un modèle, un agent). Les parties sont numérotées comme les numérote la boîte de dialogue Convert, ou désignées par leur identifiant.

| Commande | Ce qu'elle fait |
|---|---|
| `/audiobook-status [from [to]]` | Le serveur de synthèse vocale utilisé, la manière dont le livre se convertit tout seul, le nombre de parties qui ont un audio (aucun, prêt, plus ancien que le texte, plus ancien que le script), l'état de chaque partie listée dans le studio, et la file d'attente. Lent seulement quand le livre est long. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Met les parties en file d'attente (le livre entier sans argument, une seule partie quand seul `from` est donné). `labelling` `true` utilise le modèle du livre ; `what` est `audio` ou `script` (seul le script pour le studio est produit, rien n'est prononcé). Avec `wait` `true`, la commande reste active jusqu'à la fin des tâches, affiche leur avancement et indique comment chacune s'est terminée ; l'arrêter annule les tâches qui n'ont pas commencé. |
| `/audiobook-stop` | Annule les parties en attente ; celle en cours s'arrête après son morceau. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` ou `final` dans le studio ; la partie a besoin d'un script. |
| `/audiobook-cast` | Le narrateur, la voix de repli et les personnages de la distribution avec leurs acteurs. |

![Le résultat d'une commande](../../images/plugin-audiobook-command.png)

L'audio est produit pour **votre** livre, et non pour la copie de travail cachée d'un [agent](../agents.md) : un agent peut lire la distribution, mais reçoit un refus des autres commandes, qui indique que vous devez les lancer vous-même. Lancez-les depuis la ligne de commande, ou confiez à un agent la préparation du livre (annotation, relecture) puis convertissez-le vous-même.

## Notifications

Une fois un lot de conversions terminé, le navigateur peut vous le signaler ([notifications](../account.md#notifications), pour que vous puissiez quitter l'onglet pendant que le serveur de synthèse vocale travaille). Dans la liste des notifications de la page *Paramètres*, il y a deux lignes : **Convert to audio** (les lots que vous lancez dans la boîte de dialogue ou avec la commande) et **Convert to audio (automatic)** (une nouvelle partie transformée en audio automatiquement). Un lot que vous avez arrêté ne notifie pas ; un lot contenant une partie en échec indique qu'il a échoué.

![La liste des notifications](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Ce qu'il ne fait pas (encore)

- MP3 et WAV uniquement (pas d'opus) ; le volume sonore des morceaux est celui que fournissent les voix.
- Pas d'effets sonores ni d'ambiance.
- Deux moteurs de synthèse vocale, Chatterbox et Higgs Audio v3 (dans ComfyUI).
- L'écoute d'une voix en dehors d'un segment.
- La distribution n'est pas incluse dans la sauvegarde d'un livre, pas plus que vos *acteurs* : un livre restauré garde sa distribution, mais nomme des acteurs qui doivent exister dans votre compte.
