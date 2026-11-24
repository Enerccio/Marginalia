---
label: Livre audio
order: 27
---

# Livre audio

Transforme un livre en livre audio, partie par partie, avec un serveur de synthèse vocale : une voix pour le narrateur et une pour chaque personnage, conservées avec les parties du livre et lues dans le lecteur. Vous pouvez laisser l'extension tourner (le modèle indique qui parle et comment, le serveur de synthèse lit) pour obtenir un brouillon rapide, ou travailler les parties une à une dans le **Audiobook studio**, en voyant exactement ce qui est envoyé au serveur de synthèse et en le modifiant à la main, puis exporter le résultat en fichiers mp3 avec des marques de chapitre.

L'extension ne produit pas la voix elle-même : elle communique avec un **Chatterbox-TTS-Server** que vous faites tourner (voir [Mise en place du serveur de synthèse](audiobook-server.md)). Le texte du livre n'est jamais modifié et n'est envoyé qu'aux serveurs que vous avez indiqués ; l'audio est stocké dans votre propre installation, sous forme de ressources liées aux parties.

<a id="what-you-need"></a>

## Ce qu'il vous faut

- Un serveur de synthèse vocale et, s'il partage la carte graphique avec votre modèle de langage, les requêtes qui libèrent de la place pour lui puis l'arrêtent ([Mise en place du serveur de synthèse](audiobook-server.md)).
- Des voix : un court **extrait de référence** (12 à 30 secondes d'une seule personne, sans musique) pour chaque voix à cloner, ou l'un des fichiers de voix du serveur. Un narrateur a besoin d'une voix de lecture calme et régulière.
- Pour un livre à plusieurs voix, un modèle de langage capable d'étiqueter le texte. Par défaut, c'est le modèle du livre.
- **ffmpeg** est facultatif. Sans lui, tout fonctionne en mp3. Avec lui sur le serveur (il est présent dans l'image Docker ; sur un ordinateur de bureau, il est trouvé dans `PATH` ou dans des dossiers usuels comme `/opt/homebrew/bin` ; la variable `MARGINALIA_FFMPEG` le désigne lorsqu'il se trouve ailleurs), l'export obtient le format **m4b**, et un administrateur peut stocker l'audio en wav.

<a id="the-settings"></a>

## Les réglages

*Settings → Extension Settings → Audiobook Settings* est le panneau de votre compte ; enregistrez la page *Settings* après l'avoir modifié.

![Le panneau avec un backend et les acteurs](../../images/plugin-audiobook-settings.png)

### Backends

Un backend est un serveur de synthèse et le modèle avec lequel il parle. Ajoutez-en un avec **+**. **Duplicate** copie le backend sélectionné (adresse, clés, verrou GPU, voix, réglages par défaut et requêtes avant et après un lot) sous un nouveau nom : c'est le moyen rapide d'obtenir un backend pour un autre modèle sur la même machine, par exemple Higgs à côté de Chatterbox, où vous changez ensuite le modèle, l'adresse et les requêtes de démarrage et d'arrêt.

| Réglage | Ce qu'il fait |
|---|---|
| Nom | Le nom affiché dans la boîte de dialogue Convert. |
| Adresse | L'adresse de base du serveur de synthèse, ou de ce qui le relaie, par exemple `http://localhost:8004`. |
| Clé API | Envoyée comme jeton bearer si le serveur (ou votre proxy) en exige un. Elle est stockée chiffrée et n'est plus affichée ; saisissez-en une nouvelle pour la remplacer, ou utilisez **Remove the key**. |
| Modèle de synthèse | **Chatterbox Turbo** (anglais, avec des balises sonores comme `[sigh]`, le plus rapide et le réglage par défaut), **Chatterbox** (anglais, avec une *exaggeration* de l'interprétation), **Chatterbox Multilingual** (23 langues) ou **Higgs Audio v3 in ComfyUI** (de nombreuses langues, voix qui suivent l'humeur d'une réplique et sons comme les pleurs, le souffle court ou un gémissement ; voir [ci-dessous](#higgs-audio-v3-emotions-and-sounds)). Le [verrou GPU](../administration/gpus.md) du backend (ou les requêtes avant un lot) doit garder ce modèle chargé. Un livre qui a besoin d'un autre modèle utilise un autre backend. |
| Verrou GPU | Le [verrou GPU](../administration/gpus.md) du GPU sur lequel tourne le serveur, lorsqu'il le partage avec le modèle de langage ou d'autres serveurs (celui du [Générateur d'images](image-generator.md) aussi) : chaque lot et chaque échantillon attend son tour sur le GPU, et le verrou charge d'abord le serveur de synthèse. Affiché seulement si l'administrateur a configuré les verrous GPU. |
| Le serveur est sur cette machine ou sur mon réseau | Autorise les adresses du réseau local. Laissez-le désactivé pour un serveur dans le cloud : il ne peut alors pas être amené à appeler votre propre machine. |
| Délai d'une requête | Durée d'attente d'une requête pour un morceau de parole. |
| Redémarrer le serveur après (morceaux) | Pour un serveur de synthèse qui fuit de la mémoire et qui est tué par le système lors d'un long lot : après ce nombre de morceaux, les requêtes après le lot puis celles d'avant sont refaites au milieu du lot (arrêt, démarrage, chargement), et le travail continue avec un serveur neuf. 0 (la valeur par défaut) ne le fait jamais. Un serveur qui s'arrête est redémarré quelle que soit cette valeur, voir [Dépannage](audiobook-server.md#troubleshooting). |
| Clé des requêtes autour d'un lot | La clé que les requêtes avant et après un lot transmettent comme `Authorization: Bearer`. Elle est stockée chiffrée et n'est plus affichée (**Remove the key** la supprime), afin de ne pas avoir à l'écrire dans les en-têtes de chaque requête, où elle serait lisible. Vide : la *Api key* ci-dessus est utilisée, ce qui est voulu lorsqu'un proxy comme LiteLLM sert tout. Une requête qui a son propre en-tête `Authorization` le conserve. |
| Voix du narrateur | Le narrateur d'un livre sans distribution. Un fichier de voix du serveur s'écrit `predefined:Henry.wav`. |
| Précision | Higgs seulement : la précision avec laquelle le modèle calcule. **fp32** (par défaut) est le choix sûr : sur une carte AMD, **bf16** a transformé des morceaux en charabia, en effets sonores et en mauvaises voix. bf16 demande environ la moitié de la mémoire graphique (environ 8 Go au lieu de 16) et il est plus rapide là où il fonctionne. |
| Envoyer le texte du clip à Higgs | Higgs seulement, expérimental : le [texte du clip](#voices-and-clips) l'accompagne. Laissez-le désactivé : dans les tests, cela a modifié la voix. |
| Vérifier la durée des morceaux | Activé par défaut. Un morceau beaucoup plus long que son texte (4 s plus 0,6 s par mot : le modèle a continué à parler, ou a produit un son à la place des mots) est refait avec la graine suivante, jusqu'à **Retries** fois ; la première prise de la bonne durée est conservée, sinon la plus courte. |
| Vérifier les mots avec la reconnaissance vocale | Désactivé par défaut ; nécessite la [reconnaissance vocale](#speech-recognition). Après chaque lot, chaque morceau est transcrit et comparé à son texte. Un morceau qui s'écarte de plus de **Max. word errors (%)** de ses mots est refait avec la graine suivante et vérifié à nouveau, jusqu'à **Retries** tours ; la prise comportant le moins de mots faux est conservée. La conversion indique combien de morceaux ont été refaits et combien diffèrent encore. |
| Réglages par défaut d'un segment | Température, poids CFG, vitesse et taille de bloc avec lesquels un morceau démarre (le studio peut les modifier pour un seul morceau). Higgs n'utilise que la température. |
| Avant un lot, Après un lot | Requêtes faites autour de chaque lot de parties (et de chaque échantillon), à l'intérieur du verrou GPU. Libérer la carte graphique et démarrer le serveur de synthèse relèvent du verrou GPU ; voir [Un seul GPU pour le modèle et la parole](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vide : ne fait rien. |

**Test connection** fait ce que fait un lot : il prend le verrou GPU (si bien qu'un serveur de synthèse arrêté est démarré), envoie les requêtes avant un lot, demande au serveur s'il est là et s'il a un modèle chargé, puis envoie les requêtes après le lot. Sans verrou ni requêtes, il se contente de demander.

Un livre qui contient des morceaux dans une autre écriture (du japonais dans un livre en anglais) les envoie au modèle multilingue, si vous avez un backend pour lui, avec leur langue ; ils restent sur le modèle du livre si vous n'en avez pas. Un livre Higgs les garde : Higgs les lit lui-même, dans la même voix.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3 : émotions et sons

Avec un backend **Higgs Audio v3 dans ComfyUI**, les étiquettes du modèle de langage sont entendues. L'adresse du backend est celle de votre ComfyUI
(avec les nœuds TTS Audio Suite, voir [Configurer le serveur de synthèse vocale](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **diction** d'un passage de parole est jouée : triste, en colère, joyeuse, amusée, tendue, effrayée, surprise, tendre, sarcastique, suppliante, désespérée,
  dégoûtée, excitée, un murmure ou un cri. Une diction forte (intensité 0,8 et plus) est plus expressive, une diction faible (en dessous de 0,2) est lue
  sobrement. Le narrateur est lu sobrement.
- Le modèle de langage peut placer davantage de **sons** dans la parole que pour Chatterbox Turbo : rire, gloussement, kikirire, soupir, halètement, toux, raclement de gorge,
  reniflement, éternuement, gémissement, pleurs, sanglot, cri, respiration haletante, plainte, fredonnement et chut.
- Le programme lit aussi la **narration autour de chaque passage de parole** (en anglais) et donne à la parole le son qu'elle nomme, lorsque le modèle n'en a mis aucun :
  *« You look funny, » she giggled.* reçoit un gloussement, *She burst out laughing. « Careful! »* un rire, *a soft moan escaped her lips. « Yes... »* un gémissement,
  *panting* ou *out of breath* une respiration haletante, *cried quietly* ou *burst into tears* des pleurs. Il laisse de côté ce qui ne ressemble qu'à un son : *« Stop! » she cried.*
  (une exclamation), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, et un son que la narration
  attribue à un autre personnage nommé. *« You look funny, » he mocked her.* reçoit une diction sarcastique ou amusée et aucun son. La narration elle-même est lue telle qu'écrite.
- **Le narrateur est toujours lu sobrement** : aucun son et aucune humeur, y compris pour un passage que le modèle a laissé au narrateur.
- Higgs n'a pas besoin d'un extrait par humeur : un seul extrait d'une voix suffit. Il copie l'enregistrement de l'extrait, donc un extrait propre compte davantage
  qu'avec Chatterbox ([Un bon extrait](#a-good-clip)).
- Chaque passage est produit par ComfyUI, puis sa mémoire est vidée (quelques secondes par passage), ce qui évite que certaines cartes graphiques ne plantent.
- Higgs continue souvent après le dernier mot d'un passage (une respiration, le début d'un autre son), et l'extrait se termine au milieu de cela, ce qui s'entend comme un bref
  « whoosh » avant la pause. Avec **ffmpeg**, l'extension coupe cette queue et fait un fondu de sortie sur chaque passage ; sans lui, les passages restent tels que Higgs les a produits.
- Le modèle Higgs Audio v3 est sous une **licence de recherche et non commerciale** : un livre audio que vous vendez nécessite une licence de Boson AI.

<a id="speech-recognition"></a>

### Reconnaissance vocale

La **reconnaissance vocale** écrit ce que dit un passage de parole. Elle vérifie les passages d'un lot (lorsqu'un backend a **Vérifier les mots par reconnaissance vocale**) et reconnaît le [texte d'un extrait vocal](#voices-and-clips). Choisissez où elle s'exécute :

| Réglage | Ce qu'il fait |
|---|---|
| Reconnaissance vocale | **ComfyUI (Granite ASR)** : le modèle de parole Granite de la TTS Audio Suite dans votre ComfyUI (l'adresse est celle de ComfyUI). **Compatible OpenAI (whisper)** : tout serveur avec `/v1/audio/transcriptions`, comme un serveur whisper (faster-whisper, speaches) ou l'API OpenAI. |
| Adresse, Clé API | L'adresse de ComfyUI, ou la base de l'API (avec ou sans `/v1`) ; la clé est stockée chiffrée. Adresse vide : pas de reconnaissance vocale. |
| Verrou GPU | Le [verrou GPU](../administration/gpus.md) que prend la reconnaissance. Sur une seule carte avec Higgs en fp32, donnez-lui un verrou à part dont les étapes redémarrent ComfyUI : ComfyUI garde le modèle de parole dans la mémoire du serveur, et charger la reconnaissance à côté a fait manquer de mémoire au serveur. |
| Modèle de la reconnaissance | Vide : `granite-speech-4.1-2b` pour ComfyUI, `whisper-1` pour le serveur compatible OpenAI. |
| Délai d'attente, Le serveur est sur cette machine ou sur mon réseau | Comme pour un backend. |

**Tester la connexion** vérifie que ComfyUI a les nœuds, ou que le serveur répond. Les passages d'un lot sont vérifiés après le lot, lorsque le serveur de synthèse vocale est rendu, de sorte que la reconnaissance et le modèle de parole n'ont jamais besoin de la carte en même temps.

<a id="cleanup-of-clips"></a>

### Nettoyage des extraits

**Nettoyage des extraits** nettoie un extrait de référence sur un ComfyUI avec la [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (la même
que pour les œuvres Higgs). Le résultat est une nouvelle **version** de l'extrait dans [Voix et extraits](#voices-and-clips) ; l'extrait lui-même reste en place.

| Réglage | Ce qu'il fait |
|---|---|
| Adresse, Clé API | L'adresse de ComfyUI ; la clé est stockée chiffrée. Adresse vide : pas de nettoyage. |
| Verrou GPU | Le [verrou GPU](../administration/gpus.md) que prend le nettoyage. Donnez-lui un verrou à part, et non celui de la synthèse ou de la reconnaissance vocale, dont les étapes redémarrent ComfyUI, afin que les modèles de nettoyage ne soient jamais chargés à côté du modèle de parole. |
| Délai d'attente, Le serveur est sur cette machine ou sur mon réseau | Comme pour un backend (le délai est celui d'un nettoyage, 10 minutes par défaut : ComfyUI télécharge un modèle la première fois qu'il est utilisé). |
| Nettoyage | Les nettoyages (préréglages) à choisir dans Voix et extraits, par nom. Chacun est une liste d'étapes exécutées l'une après l'autre. **Nettoyages par défaut** rétablit ceux qui sont fournis. |

Une étape est l'une des suivantes :

- **Modèle de séparation** : un modèle de la suite qui sépare l'audio en deux, la partie *extraite* et la partie *restante*, et en garde une. La partie qui contient la voix dépend du modèle : un modèle de voix (`MelBandRoformer`) extrait la voix, un modèle de débruitage ou de désréverbération extrait le bruit ou la pièce, donc gardez la partie restante. **Agressivité** (0 à 20) décide de la quantité retirée.
- **Restauration vocale** : restaure une voix dégradée (un téléphone, un vieil enregistrement). Elle modifie davantage le son de la voix qu'une séparation.
- **ffmpeg (ici)** : le filtre de **Nettoyer avec ffmpeg**, exécuté par l'application entre les étapes sur ComfyUI.
- **Normaliser le volume (ici)** : élève (ou baisse) le niveau de l'extrait jusqu'à la sonorité que vous donnez (-18 LUFS, comme pour le livre audio, par défaut), avec
  ffmpeg dans l'application. Seul le niveau change, les crêtes restent sous l'écrêtage. Placez-la en dernier, après les étapes qui retirent quelque chose (elles
  rendent la voix plus faible). Un nettoyage composé de cette seule étape (et du filtre ffmpeg) n'utilise ni ComfyUI ni son verrou GPU.

Les nettoyages fournis sont : *Voix sans musique* (la voix d'un film ou d'une chanson), *Retirer le bruit*, *Retirer l'écho* (la pièce et l'écho),
*Relever une voix faible* (la seule étape de volume), *Restaurer la voix* et *Extrait de film ou de jeu* (la voix extraite de la musique, puis la pièce, puis le bruit : retirer d'abord le bruit faisait ressortir la pièce
d'un extrait de film). *Retirer l'écho* demande une correction dans certaines configurations de ComfyUI, voir [le serveur de synthèse vocale](audiobook-server.md#higgs-audio-v3-in-comfyui). **Tester la connexion** vérifie que ComfyUI a les nœuds et remplit la liste des modèles avec ceux de votre
serveur. Les modèles sont téléchargés par ComfyUI la première fois qu'un nettoyage les utilise.

<a id="shared-speech-servers"></a>

### Serveurs de synthèse vocale partagés

Sur une installation à plusieurs utilisateurs, l'administrateur peut tenir des serveurs de synthèse vocale pour tout le monde, afin qu'une seule machine avec GPU soit configurée une fois et que personne n'ait besoin d'une adresse ou d'une clé. L'administrateur le fait dans le même panneau (les dernières sections, visibles seulement par les administrateurs) : **Serveurs de synthèse vocale partagés** ouvre une fenêtre avec les mêmes champs qu'un backend personnel, ainsi que la liste de ceux qui peuvent l'utiliser : **tous les utilisateurs**, ou les utilisateurs choisis dans la liste. Un interrupteur là-dessus, **Les utilisateurs peuvent ajouter leurs propres serveurs de synthèse vocale**, décide si les backends ci-dessous restent autorisés.

Ce que vous obtenez :

- **Backend utilisé** en haut du panneau liste les serveurs partagés que vous pouvez utiliser (marqués *(partagé)*) et les vôtres. La fenêtre *Convertir en audio* et le studio utilisent celui que vous choisissez ; les commandes l'utilisent aussi.
- Vous ne voyez que le nom d'un serveur partagé, jamais son adresse, ses clés ni ses requêtes. Les lots de tous les utilisateurs sur une même adresse de serveur attendent leur tour, de sorte que deux utilisateurs ne peuvent pas démarrer deux fois le même GPU.
- Lorsque l'administrateur vous retire un serveur, une tâche qui le désigne encore est refusée ; choisissez-en un autre.
- Lorsque l'administrateur désactive vos propres serveurs, le panneau l'indique. Ceux que vous avez ajoutés sont conservés et sont de nouveau utilisés si l'interrupteur est remis en marche.
- La *voix du narrateur* d'un serveur partagé doit être un fichier de voix du serveur (`predefined:Henry.wav`) : un extrait de référence est une ressource d'un seul utilisateur et ne peut pas être lu par les autres. Les voix de vos acteurs fonctionnent comme d'habitude.
<a id="actors"></a>

### Acteurs

Un **acteur** est une voix à vous : un extrait de référence (l'une de vos [ressources](../resources.md) audio, ou un nouvel extrait téléversé ici) ou un fichier de voix du serveur, avec des voix propres pour une **humeur** (un chuchotement, un cri) ou une **langue** (un extrait qui parle japonais). La langue l'emporte sur l'humeur, et sans voix pour l'une ou l'autre, c'est la voix de l'acteur qui est lue. Vos acteurs sont conservés dans votre compte ; tout livre peut attribuer un acteur à son narrateur et à ses personnages.

Un acteur peut avoir une **accroche**, une courte note à vous : le personnage qu'il incarne, un livre, *profonde et ancienne*, toute étiquette qui vous aide à choisir la bonne. Les sélecteurs d'un acteur (les paramètres, la distribution) l'affichent sous le nom, et *Voices and clips* sous le nom dans la liste des acteurs. Dans la distribution, tapez dans la case d'un acteur pour le retrouver par une partie de son nom ou de son accroche.

**Preview** à côté d'une voix (dans l'acteur, les paramètres et la distribution) joue quelques phrases prononcées avec elle (une courte dans la langue de la voix, pour une langue). Lorsque le GPU détient autre chose, le verrou GPU charge d'abord le serveur de synthèse vocale. La flèche à côté propose d'autres échantillons : **with sounds** (un soupir, un rire, un halètement) pour les modèles qui les prononcent (Chatterbox Turbo, Higgs Audio v3), et pour Higgs chaque **humeur** de l'étiquetage (triste, chuchoté, sarcastique...). Lorsque la voix est un extrait, le bouton avec le fichier audio joue l'**extrait de référence** lui-même tel qu'il est stocké, aussitôt (sans serveur de synthèse ni GPU). Les acteurs sont listés par nom.

Chaque échantillon ne prend le GPU que pour lui-même. Le GPU reste chargé avec le serveur de synthèse vocale après un échantillon, de sorte que les suivants arrivent aussitôt, jusqu'à ce qu'une requête du modèle de langage (ou d'un autre serveur) ait besoin du GPU.

![Un acteur avec une voix pour une humeur](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Voix et clips

**Voices and clips** (à côté des acteurs) est une seule fenêtre pour vos acteurs et les extraits dont leurs voix sont clonées. Elle a trois onglets, côte à côte au début : **Actors**, **Clips** et **Clip**. Faites glisser un onglet pour le placer à côté, sous ou avec un autre ; la fenêtre se rouvre telle que vous l'avez laissée.

- **Actors** : chacun avec sa voix, le nombre d'autres voix (humeurs, langues) qu'il possède et la pire qualité de ses extraits ; ajoutez, modifiez ou supprimez-en un. Le viseur d'un acteur dont la voix est un extrait sélectionne cet extrait dans **Clips**, en ouvrant les dossiers au-dessus.
- **Clips** : vos extraits dans des **dossiers** (par exemple un par personnage), avec les **versions** faites à partir d'un extrait sous lui, chacune avec sa durée, sa **qualité** et son lieu d'utilisation (acteurs, avec l'humeur ou la langue, et narrateurs de vos backends). **Add a clip** en téléverse un. Les dossiers démarrent fermés ; ce que vous ouvrez reste ouvert tant que la fenêtre est ouverte, même après une modification d'un extrait.
  - Cochez des extraits (les dossiers n'ont pas de case) pour travailler sur plusieurs à la fois. **Merge** crée un nouvel extrait à partir des extraits cochés, l'un après l'autre, dans l'ordre de la liste, sans pause entre eux : une référence plus longue à partir de prises courtes d'une voix. Il porte leurs noms, va dans leur dossier s'ils en partagent un, et reçoit leurs textes lorsque chacun en a un ; les extraits cochés restent. Les extraits wav d'un même format sont joints tels quels ; les extraits mp3 ou les wav de formats différents nécessitent ffmpeg. **Delete checked** supprime les extraits cochés après confirmation ; un extrait encore utilisé reste, et le message le nomme.
- **Clip** : l'extrait sélectionné.
  - **Name** et **Folder** : renommez l'extrait (le fichier garde son nom) et placez-le dans un dossier, un nouveau en tapant son nom. Une version reste dans le dossier de l'extrait dont elle est issue, et indique à partir de quoi et comment elle a été faite.
  - Sa **waveform** : cliquez pour placer la tête de lecture puis **Play**, ou glissez dessus pour sélectionner une plage (les champs **From** et **To** suivent, au centième de seconde, et la sélection les suit). La loupe zoome sur la sélection pour une limite précise ; l'autre bouton montre à nouveau l'extrait entier. **Play the selection** joue la plage, **Play around the cut** joue une seconde et demie de chaque côté de la coupe, sans elle : le rendu de l'extrait lorsque la plage est retirée.
  - **Download** (nommé d'après l'extrait et sa version), lire ce qui ne va pas, **Replace...** ou **Delete** (seulement lorsque rien ne l'utilise ; ses versions remontent vers l'extrait dont elles sont issues).
  - **Clean with ffmpeg** crée une version avec moins de souffle et de ronflement constants. Elle ne supprime ni la musique, ni les effets, ni les autres voix.
  - **Clean with AI** crée une version avec le [nettoyage](#cleanup-of-clips) choisi sur ComfyUI. Il tourne en arrière-plan : vous pouvez continuer à travailler, et un message indique quand la version est prête.
  - **Trim** crée une version avec seulement la plage sélectionnée (**Keep this part**) ou sans elle (**Cut this part out**), pour un son qu'aucun nettoyage ne supprime : un animal, une toux, une porte. Les jonctions sont en fondu pour ne pas cliquer. Un extrait wav se découpe sans ffmpeg, un mp3 en a besoin.
  - **Use this version** place une version partout où l'extrait dont elle est issue est utilisé. Les parties sont refaites avec la nouvelle voix, et l'ancien extrait reste jusqu'à ce que vous le supprimiez.
  - **Try the voice** : tapez un texte puis **Speak** : le backend utilisé le prononce avec cet extrait exactement tel que tapé (sans étiquetage, sans sons ajoutés), avec le **Seed** que vous donnez (vide : 42, celui des conversions), et le joue. Le ⓘ liste les balises que lit le modèle du backend, dans sa propre syntaxe (`[cough]` pour Chatterbox Turbo, `<|sfx:cough|>` ou `<|emotion:sadness|>` pour Higgs) ; un clic en insère une dans le texte. Il prend le verrou GPU du backend et apparaît dans les tâches comme une conversion. Le nettoyage et la reconnaissance d'un extrait sont aussi des tâches.
  - **Text of the clip** : les mots prononcés dans l'extrait. Tapez-les, ou appuyez sur **Recognize** pour que la [reconnaissance vocale](#speech-recognition) les transcrive, puis corrigez-les et **Save**. La boîte de dialogue de l'acteur affiche le même champ pour l'extrait de la voix de l'acteur. Une version et un remplacement reprennent le texte. Higgs ne le reçoit que lorsque le backend l'envoie (désactivé par défaut).
- **Replace...** place un autre extrait, l'un des vôtres ou un que vous téléversez ici, partout où l'extrait est utilisé.

Les sélecteurs d'une voix (l'acteur, le narrateur d'un backend) affichent un extrait sous la forme *dossier / extrait › version*.

<a id="a-good-clip"></a>

#### Un bon extrait

Un modèle de voix copie l'**enregistrement**, pas seulement la voix : la pièce, un souffle, la musique sous une scène de film reviennent dans chaque réplique, souvent comme un « whoosh » doux sous les mots, qu'aucun filtre du résultat ne supprime. Donc :

- Utilisez un enregistrement propre d'un seul locuteur, avec de vraies pauses (un échantillon d'acteur de voix, une lecture faite chez soi dans une pièce calme), de 12 à 30 secondes. Un extrait tiré d'un film ou d'une série est le pire cas.
- Les marques de qualité sont mesurées sur l'extrait : **Clean** (les pauses sont d'au moins 45 dB sous la parole), **Fair** (35 à 45 dB, ou plus long que 30 s, que Chatterbox coupe), **Poor** (son de fond, sous 35 dB ; déformé ; plus court que 8 s). Un extrait qui n'est pas propre est signalé lorsque vous l'ajoutez.
- Un modèle de séparation garde ce qui ressemble à une voix : le cri d'une chouette ou le chant d'un oiseau restent (lors du test avec plusieurs modèles). Retirez un tel son avec **Trim**.
- Nettoyez un extrait avec l'IA ([Cleanup of clips](#cleanup-of-clips)) : lors du test, le nettoyage *Film or game clip* a fait passer un extrait avec souffle, ronflement et écho de *Poor* à *Clean* (les pauses sont passées de 38 à 89 dB sous la parole). Écoutez la version avant de l'utiliser : un modèle de séparation peut aussi retirer une respiration ou la fin d'un mot doux. Le nettoyage ffmpeg n'atténue qu'un bruit constant : sur un extrait de film, il a gagné environ 3 dB et l'extrait est resté *Poor*.
- Sans ComfyUI, nettoyez un extrait à la main : la réduction de bruit d'[Audacity](https://www.audacityteam.org/) avec un profil de bruit pris sur une pause de l'extrait, ou un suppresseur de voix comme [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) pour un extrait avec de la musique.

<a id="converting-new-parts-by-itself"></a>

### Convertir les nouvelles parties d'elle-même

*Convert new parts to audio by itself* est la valeur par défaut pour tous vos livres ; voir [Conversion automatique](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## La distribution et la prononciation d'un livre

Les deux sont conservées avec le livre (elles voyagent dans sa sauvegarde) et se modifient depuis la boîte de dialogue **Convert to audio**.

**Cast** indique qui lit : un acteur pour le **narrator**, un pour **other speakers** (quiconque le modèle nomme et que la distribution ne contient pas ; vide : le narrateur lit) et un pour chaque **personnage**. Un personnage a un nom et d'autres noms que le texte peut employer pour lui (*Tom* pour *Tomáš Novák*), séparés par des virgules ; une réplique est attribuée au personnage lorsque son nom, l'un des autres noms, ou un mot du nom qu'aucun autre personnage ne partage, correspond, sans tenir compte de la casse ni des accents. **Add the characters of the book** remplit la liste à partir des personnages à point de vue et des personnages présents dans les parties.

![La distribution d'un livre](../../images/plugin-audiobook-cast.png)

Les **locuteurs sans nom** (le garde, l'aubergiste, la fille à la fenêtre) peuvent chacun recevoir une voix propre à partir d'un ou deux enregistrements. Donnez à la distribution des acteurs de base : **Other men**, **Other women** et, si vous le souhaitez, **Children** (vide : la voix de femme, plus aiguë). L'étiquetage indique alors aussi le genre et l'âge de chaque locuteur que la distribution ne nomme pas, et chacun parle avec une *variation* de l'extrait de base de sa catégorie : un peu plus aiguë ou plus grave, d'autres formants, plus rapide ou plus lente, plus sombre ou plus claire. Un adolescent reçoit la voix d'homme, une adolescente et un enfant celle de femme, un locuteur âgé une voix plus grave et plus lente. La variation découle du nom du locuteur et du livre, donc le même locuteur sonne de la même façon dans chaque chapitre et à chaque exécution. Laissez les trois vides et chaque locuteur sans nom est lu par **Other speakers**, comme auparavant.

Les locuteurs rencontrés jusqu'ici sont listés sous les bases, avec le genre et l'âge qui leur ont été attribués en premier (une partie ultérieure ne les modifie pas). Vous pouvez corriger le genre ou l'âge, écouter le locuteur, lui donner **another variation** (le dé), ou le retirer de la liste. Un locuteur modifié est refait la prochaine fois que les parties sont converties.

Une variation nécessite **ffmpeg** avec le filtre *rubberband* sur le serveur (les paquets Debian et Ubuntu, que l'image Docker installe, sont en général compilés avec lui ; `ffmpeg -filters | grep rubberband` indique si le vôtre l'est). Chaque variation est créée une seule fois, à sa première utilisation, et conservée sous forme de copie cachée de son extrait, de sorte qu'elle voyage avec vos sauvegardes. Sans ffmpeg, la distribution le signale : les locuteurs sans nom partagent la voix de base de leur catégorie, et les variations déjà créées restent utilisées. Une variation ne modifie qu'un extrait de vos fichiers ; un acteur dont la voix est une voix du serveur de synthèse est utilisé tel quel.

**Pronunciation** désigne la manière dont le texte est écrit *pour la voix* ; le livre lui-même n'est jamais modifié. Le **dictionary** remplace des mots entiers (la casse compte) par ce qui doit être dit à leur place : une graphie phonétique pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Pour un livre en anglais, les règles font aussi lire à voix haute d'autres éléments : nombres, ordinaux, années, heures, sommes d'argent et abréviations en toutes lettres, un titre qui n'est qu'un nombre comme *Chapitre quatre. The Arch* (le mot qui précède le nombre vous appartient), les marques markdown telles que `*` sont supprimées, et une rupture de scène (`***`, `---`) n'est pas lue mais produit une pause plus longue. Désactivez les règles pour un livre dans une autre langue ; le dictionnaire s'applique toujours. **Try a text** montre comment un passage sera lu. Un passage dont le texte parlé change est refait, les autres sont conservés.

![La prononciation d'un livre](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertir un livre

*Menu Outils de l'éditeur de récit (le bouton aux engrenages) → **Convert to audio***.

![La boîte de dialogue avant le démarrage](../../images/plugin-audiobook-convert.png)

1. Choisissez le **backend** et appuyez sur **Load** : les parties au texte de la branche active sont listées (la première est la 1), avec leur état :
   *Not generated*, *Labelled* (les étiquettes du modèle sont conservées, pas encore d'audio), *Generated* (avec un bouton de lecture et un téléchargement) ou
   *Generated (the text changed)* (l'audio a été créé à partir d'un texte plus ancien et sera refait) ; *(final)* s'ajoute à une partie que le studio marque comme finale.
2. Laissez **Label the parts with a language model** désactivé pour la version simple : le narrateur lit chaque paragraphe. Activez-le pour que le modèle décide qui parle dans chaque passage de dialogue, comment (calme, chuchoté, crié...) et où placer une balise sonore, et pour que le casting attribue les voix. Le modèle du livre est choisi par défaut ; vous pouvez en prendre un autre. Les parties sont d'abord étiquetées, puis lues. Une partie qui a déjà des étiquettes pour son texte est lue avec elles même lorsque la case est décochée (le modèle n'est pas interrogé). Relancer un livre étiqueté sans cocher la case ne transforme donc pas ses dialogues en simple narration. **Remove labels** est la façon de demander une narration simple ou de nouvelles étiquettes.
3. Cochez les parties (la case de l'en-tête les coche toutes) et **plan** ce qu'il faut en faire. La colonne **Plan** l'affiche, mais rien n'est encore fait :
   **Generate** (créer ce qui manque ; une partie à jour n'est pas recréée), **Regenerate** (supprimer l'audio, retirer les étiquettes, puis générer),
   **Delete audio** (les morceaux et l'audio joint de la partie ; les étiquettes sont conservées), **Remove labels** (le modèle est interrogé à nouveau la prochaine fois que la partie est créée), **Clear the plan**.
   Les étapes s'additionnent sur une partie (Delete audio et Remove labels sans Generate ne font que nettoyer) ; une partie marquée comme finale est exclue de tous les plans.
4. **Start** exécute le plan. Lorsqu'il retire de l'audio ou des étiquettes, il demande d'abord confirmation. Ce qui doit être généré est placé dans votre file d'attente, qui continue de tourner lorsque vous fermez la fenêtre ou quittez la page ; une partie qui attend ou s'exécute dans la file est exclue d'un plan. L'état, les morceaux déjà produits et le message de la tâche s'affichent sur la ligne de la partie.
   Pendant que le modèle étiquette une partie, sa ligne affiche *Labelling pieces 17-32 of 120* (le modèle est interrogé par blocs de 16 morceaux). **Cliquez sur l'état** d'une partie déjà étiquetée pour lire, pour chaque requête, le raisonnement du modèle et sa réponse, au fur et à mesure qu'ils arrivent. Lorsque l'étiquetage de la partie se termine (y compris en cas d'échec ou d'arrêt), tout est conservé avec la partie, chaque exécution venant après les précédentes ; relisez-le ou supprimez-le avec **Reasoning** dans le studio. Les nouvelles étiquettes et le nouvel audio n'y touchent pas, et il figure dans les sauvegardes et les exports du livre avec l'audio.

![La file d'attente : morceaux déjà produits, et un téléchargement lorsqu'une partie est terminée](../../images/plugin-audiobook-queue.png)

Voici comment une partie est créée, pour savoir à quoi vous attendre :

- Le texte est **découpé par le programme**, jamais par le modèle, en morceaux de narration et en morceaux de discours direct (entre guillemets droits ou courbes, ou entre crochets japonais ; un titre est de la narration). Le modèle se contente d'*étiqueter* les morceaux numérotés. Un texte avec des balises sonores n'est utilisé que s'il s'agit du morceau avec les balises ajoutées et que rien d'autre n'a changé, pour que le modèle ne puisse jamais modifier votre livre.
- **Qui parle dans un morceau relève du choix du modèle pour le discours ; la narration relève du narrateur.** Une lettre, un mot, un message ou les pensées d'un personnage n'ont pas de guillemets, donc le programme les considère comme de la narration. Un tel morceau n'est lu par le personnage (la voix du casting) que s'il **commence en italique** (`*...*` ou `_..._`) et que le modèle nomme le personnage à partir du contexte (une signature, « l'écriture ne laissait aucun doute »). La narration à la troisième personne, y compris lorsqu'elle est racontée du point de vue d'un personnage (« Elle le regarda traverser la pièce »), reste toujours celle du narrateur : le programme ignore le personnage que le modèle attribue à un morceau sans italique. Les italiques d'insistance, de sons ou de mots étrangers relèvent eux aussi de la narration. Les mots restent toujours les vôtres.
- **L'instruction donnée au modèle est un modèle que vous pouvez modifier.** *Settings → Extension Settings → Audiobook Settings → Labelling prompt* constitue votre valeur par défaut ; un livre (onglet *Prompts* → *Extension Settings* → *Audiobook*) ou un dossier peut avoir la sienne. Vide, elle reprend celle du dossier parent, puis la vôtre, puis celle intégrée au programme. Elle s'écrit comme les autres [invites](../books/prompts.md) : les variables sont `{{characters}}`, `{{deliveries}}`, `{{tags}}` et `{{wordsPerTag}}`, et la fenêtre d'aide propose **Insert Default Template**. Une instruction modifiée fait redemander les étiquettes des parties à créer la prochaine fois qu'elles seront produites.
- **Le volume des parties est égalisé** lorsque le programme ffmpeg est installé (le même qui produit le `.m4b`) : l'audio joint de chaque partie est ramené à un même niveau sonore (EBU R128, linéaire, donc la dynamique est préservée ; **-18 LUFS** sauf si vous le modifiez dans *Settings → Extension Settings → Audiobook Settings*, où vous pouvez aussi désactiver cette option). Les morceaux restent tels que le serveur les a produits, seule la partie jointe est égalisée, afin qu'un livre audio ne change pas de niveau d'une partie à l'autre. Les **parties créées auparavant** ne sont pas modifiées tant que vous ne les convertissez pas à nouveau : cela les réassemble à partir des morceaux stockés et les égalise, sans rien demander au serveur de synthèse vocale (ni décharger le modèle de langage). Désactiver l'égalisation laisse telles quelles les parties déjà égalisées. Si ffmpeg échoue pour une partie, celle-ci conserve son audio d'origine et le résultat l'indique.
- Toutes les parties d'un même clic sont créées en **un seul lot par modèle vocal** : les requêtes précédant le lot sont exécutées une fois, puis tous les morceaux sont lus, puis les requêtes suivant le lot sont exécutées une fois. C'est ce qui évite à la carte graphique de changer de modèle pour chaque partie.
- Chaque morceau est **conservé selon son contenu** (texte, voix, modèle, langue, réglages, graine). Une partie déjà à jour ne coûte rien, un paragraphe modifié est le seul morceau refait, et une partie en échec conserve les morceaux déjà produits lorsque vous appuyez sur **Retry**.
- Les morceaux sont assemblés avec des pauses qui dépendent de leur position (après un titre, entre les paragraphes, avant *he said*) en un seul fichier mp3 par partie, conservé avec la partie.
- Pendant qu'une génération lancée par vous s'exécute, la synthèse vocale attend, afin que le modèle de langage ne soit pas déchargé pendant ce temps.
- Une partie que vous avez marquée **final** dans le studio est laissée intacte, et la boîte de dialogue indique combien il y en a.
- **Le livre audio complet** assemble en un seul mp3 toutes les parties dont l'audio est à jour, avec une marque de chapitre pour chaque partie, et affiche un lien de téléchargement sous la liste (les parties exclues sont nommées). Le même fichier correspond au format d'export *one mp3 with chapter marks*.
- **Stop all** annule les parties en attente et arrête immédiatement celle en cours : la requête au modèle de langage (l'étiquetage) ou au serveur de synthèse vocale (le morceau en cours de lecture) est abandonnée sans attendre sa réponse. Un lot arrêté avant de commencer n'envoie pas les requêtes qui le précèdent, si bien que le modèle de langage reste chargé. Une partie dont tous les morceaux ont été produits avant l'arrêt reçoit tout de même son audio (elle est seulement assemblée) ; les autres conservent les morceaux qu'elles ont, et la prochaine exécution ne crée que ceux qui manquent. **Download** donne le mp3 d'une partie terminée.
<a id="the-audiobook-studio"></a>

## La studio de livre audio

*Menu Outils → **Audiobook studio*** (la voix de la pièce sélectionnée, avec un aperçu, se trouve à côté de son haut-parleur) est l'endroit où le livre audio final est fabriqué, une partie à la fois : vous voyez le script, le modifiez, écoutez une pièce et produisez la partie.

![La studio avec une réplique sélectionnée](../../images/plugin-audiobook-studio.png)

- Pendant que la partie est annotée ou fabriquée, une barre indique qu'une opération est en cours, et les boutons qui lanceraient d'autres traitements restent en attente.
- **Annotate** produit le script de la partie (avec le modèle de langage si vous le demandez, sinon avec le narrateur) et affiche les pièces dans un tableau : locuteur, diction, intensité, texte énoncé et une marque pour une pièce que vous avez modifiée. Pendant son exécution, un bouton **Stop** apparaît à côté et met fin à la requête adressée au modèle de langage.
- **Reasoning** affiche ce que le modèle de langage a répondu à chaque fois qu'il a étiqueté la partie (la date de l'exécution et les pièces de chaque requête, son raisonnement et sa réponse), afin que vous puissiez voir pourquoi une ligne a été attribuée à un locuteur ou a reçu une diction donnée. **Delete the reasoning** supprime ces informations ; les étiquettes et l'audio restent.
- Sélectionnez une pièce pour modifier son **speaker**, son **spoken text**, sa **delivery**, son **intensity** (l'exagération de la voix, pour les modèles qui en disposent ; sur Higgs, la diction et l'intensité sont les marques telles que `<|emotion:sadness|>` au début du texte énoncé, qui les suivent lorsque vous les modifiez), sa **language**, son **seed**, sa **pause after**. **Apply** conserve la modification et **verrouille** la pièce : une nouvelle annotation laisse une pièce verrouillée telle quelle.
  **Unlock** la rend à l'annotation. *The request the speech server gets* affiche la requête exacte.
- **Hear** produit l'audio d'une pièce (cette pièce seulement ; le serveur de synthèse vocale est démarré pour elle) ; **Another take** la refait avec un nouveau seed, et les **takes** sont conservées pour que vous puissiez choisir celle qui vous plaît (*Use this take and hear it*). Une bonne voix dépend souvent du seed.
- **Make the part from this script** assemble les pièces telles quelles, sans nouvelle annotation.
- Le **status** de la partie est *draft*, *reviewed* ou *final*. Une partie finale n'est ni annotée ni convertie de nouveau, et l'export peut se limiter aux parties finales.

<a id="exporting"></a>

## Exportation

Le livre audio est un format de l'*Export story* du livre : **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** et, lorsque ffmpeg est installé, **Audiobook (one m4b with chapters)**. Ils sont constitués de l'audio des parties à jour : une partie sans audio, ou dont l'audio est plus ancien que son texte ou que son script dans la studio, est omise, et la fenêtre indique laquelle. Les fichiers mp3 contiennent des balises ID3 (le titre du livre, le chapitre, la piste) ; le fichier unique comporte des marques de chapitre que des lecteurs comme VLC et la plupart des applications de livres audio affichent ; le zip contient une liste de lecture. La case **Audiobook: only the parts marked final** limite l'export aux parties que vous avez terminées dans la studio.

Le **m4b** est le format qu'Apple Livres et la plupart des applications de livres audio sur téléphone attendent : audio AAC (64 kbit/s, suffisant pour la parole) dans un conteneur MP4 avec les chapitres, le titre, l'auteur et le genre. C'est ffmpeg qui le produit, si bien que ce format n'est proposé que sur un serveur disposant de ffmpeg. L'audio stocké en mp3 est encodé une seconde fois pour cela ; l'audio stocké en wav n'est encodé qu'une fois.

<a id="how-the-audio-is-stored"></a>

### Comment l'audio est stocké

Un administrateur le choisit pour toute l'installation dans *Settings → Extension Settings → Audiobook Settings* (la dernière section, visible uniquement par les administrateurs) :

- **mp3** (par défaut) : léger, assemblé sans décodeur, sans ffmpeg. Le m4b constitue une seconde étape avec perte.
- **wav** : sans perte, environ dix fois plus volumineux (une heure de parole représente environ 170 Mo), nécessite ffmpeg. Les pièces sont demandées au serveur de synthèse vocale au format wav et assemblées en wav ; le navigateur les lit telles quelles ; les exports mp3 et zip sont encodés par ffmpeg (96 kbit/s) et le m4b n'est encodé qu'une fois.

Ce choix s'applique à ce qui est produit à partir de maintenant. Les parties produites auparavant conservent leur audio et sont traitées telles quelles ; une partie de chaque type (une pièce verrouillée de la studio antérieure) est assemblée en wav, et un export regroupant les deux types fonctionne, mais nécessite ffmpeg.

Un export de l'ensemble du livre est produit à partir des fichiers enregistrés des parties, dans des fichiers temporaires du serveur, puis téléchargé depuis celui-ci ; sa durée n'est donc pas limitée par la mémoire de l'application, mais le dossier temporaire doit avoir de la place pour environ le double de l'audio du livre (en wav, environ 350 Mo par heure) pendant sa production.

![La fenêtre d'export avec les formats de livre audio](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversion automatique

Un livre peut produire lui-même l'audio d'une nouvelle partie, dès que la partie est finale : après l'annotation automatique, la réécriture et les traductions si le livre en possède, comme dernière étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part) (dans le panneau des exécutions, jusqu'à ce que l'audio soit produit ; son **Stop** retire la partie de la file d'attente). Elle est **désactivée** par défaut. Choisissez-la dans l'onglet *Prompts* du livre, sous les réglages d'extension, pour un dossier de livres, ou pour tous vos livres dans les réglages de livre audio ; c'est le réglage le plus proche qui s'applique :

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices** ; un livre sans modèle obtient la narration simple, et vous en êtes averti.

![Le réglage dans un livre](../../images/plugin-audiobook-auto.png)

La partie rejoint votre file d'attente, avec la distribution et la prononciation du livre. Un avis vous indique qu'elle est en file d'attente, un autre que son audio est prêt ou a échoué ; ils n'apparaissent que sur la page qui a lancé la génération, et la tâche figure de toute façon dans la fenêtre *Convert to audio*.

<a id="listening"></a>

## Écoute

Un livre publié (ou le lecteur) qui comporte des parties avec audio affiche une barre de lecture : lecture, pause, arrêt, vitesse, la partie entendue étant marquée dans le texte. Elle démarre à partir de la sélection ou de la première partie visible, lit les parties dans l'ordre et charge la suivante pendant que la courante est lue ; les commandes multimédias de l'appareil connaissent le livre. L'audio produit à partir d'un ancien texte d'une partie n'est pas proposé. L'audio est servi au propriétaire du livre et, tant que le livre est publié, à toute personne pouvant l'ouvrir ; un livre retiré de la publication cesse d'être servi immédiatement (à la partie suivante, lorsque le lecteur la charge).

![Le lecteur dans la liseuse](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Une partie sans audio

Le lecteur s'arrête à une partie qui n'a pas d'audio, afin qu'aucune partie de l'histoire ne soit sautée, et l'indique. Lorsque l'extension [Read Aloud](tts-reader.md) est installée, la barre affiche aussi un bouton haut-parleur, **Read this part (and the next ones without audio) with the voice of the browser**. Rien n'est lu avec une voix synthétique sans que vous appuyiez dessus. Elle lit alors cette partie et les suivantes qui n'ont pas d'audio avec la voix de votre appareil, et lorsqu'elle atteint une partie qui a de l'audio, le lecteur reprend la main. Les parties suivantes sans audio dans la même écoute sont lues de la même façon sans nouvelle demande ; **Stop** la réduit au silence, et relancer le lecteur demande à nouveau. La voix, la vitesse et la hauteur sont celles de Read Aloud.
<a id="commands"></a>

## Commandes

Les mêmes choses que les [commandes](../commands/index.md) (la ligne de commande de l'éditeur de récit, la macro `command` d'un modèle, un agent). Les parties sont numérotées comme les numérote la boîte de dialogue de conversion, ou désignées par leur identifiant.

| Commande | Ce qu'elle fait |
|---|---|
| `/audiobook-status [from [to]]` | Le serveur de synthèse vocale utilisé, la façon dont le livre se convertit tout seul, combien de parties ont un audio (aucune, prête, plus ancienne que le texte, plus ancienne que le script), l'état de chaque partie listée dans le studio, et la file d'attente. Lent seulement quand le livre est long. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Met les parties dans la file d'attente (le livre entier sans argument, une seule partie quand seul `from` est donné). `labelling` `true` utilise le modèle du livre ; `what` est `audio` ou `script` (seulement le script pour le studio, rien n'est prononcé). Avec `wait` `true`, la commande reste active jusqu'à la fin des tâches, indique leur avancement et précise comment chacune s'est terminée ; l'arrêter annule les tâches qui n'ont pas commencé. |
| `/audiobook-stop` | Annule les parties en attente ; la partie en cours s'arrête après son morceau. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` ou `final` dans le studio ; la partie a besoin d'un script. |
| `/audiobook-cast` | Le narrateur, le recours et les personnages de la distribution avec leurs acteurs, les acteurs de base des locuteurs sans nom et les locuteurs sans nom rencontrés jusqu'ici (genre, âge, variation). |

![Le résultat d'une commande](../../images/plugin-audiobook-command.png)

L'audio est produit pour **votre** livre, et non pour la copie de travail cachée d'un [agent](../agents.md) : un agent peut lire la distribution, et reçoit un refus des autres commandes, lui indiquant que
vous devez les lancer vous-même. Lancez-les depuis la ligne de commande, ou confiez à un agent la préparation du livre (annotation, relecture) et convertissez-le vous-même.

## Notifications

Quand un lot de conversions est terminé, le navigateur peut vous prévenir ([notifications](../account.md#notifications), pour que vous puissiez quitter l'onglet pendant que le serveur de synthèse vocale travaille). Dans la liste des notifications de la page *Paramètres*, il y a deux lignes : **Convertir en audio** (les lots que vous lancez depuis la boîte de dialogue ou avec la commande) et **Convertir en audio (automatique)** (une nouvelle partie qui a été transformée en audio par elle-même). Un lot que vous avez arrêté ne notifie pas ; un lot contenant une partie en échec l'indique.

![La liste des notifications](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Ce qu'il ne fait pas (encore)

- Mp3 et wav seulement (pas d'opus) ; le volume sonore des morceaux est celui que fournissent les voix.
- Pas d'effets sonores ni d'ambiance.
- Deux moteurs de synthèse vocale, Chatterbox et Higgs Audio v3 (dans ComfyUI).
- L'écoute d'une voix en dehors d'un segment.
- La distribution ne fait pas partie de la sauvegarde d'un livre à propos de vos *acteurs* : un livre restauré conserve sa distribution, mais nomme des acteurs qui doivent exister dans votre compte.
