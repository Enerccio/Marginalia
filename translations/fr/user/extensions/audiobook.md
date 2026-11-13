---
label: Livre audio
order: 27
---

# Livre audio

Transforme un livre en livre audio, partie par partie, grâce à un serveur de synthèse vocale : une voix pour le narrateur et une pour chaque personnage, conservées avec les parties du livre et lues dans le lecteur. Vous pouvez laisser l'extension tourner (le modèle indique qui parle et comment, le serveur de synthèse vocale lit) pour une ébauche rapide, ou travailler sur les parties une à une dans le **studio du livre audio**, en regardant exactement ce qui est envoyé au serveur de synthèse vocale et en le modifiant à la main, puis exporter le résultat en fichiers mp3 avec des marques de chapitres.

L'extension ne produit pas elle-même la parole : elle communique avec un **Chatterbox-TTS-Server** que vous faites tourner (voir [Installer le serveur de synthèse vocale](audiobook-server.md)). Le texte du livre n'est jamais modifié et n'est jamais envoyé à d'autres que les serveurs que vous avez indiqués ; l'audio est stocké dans votre propre installation, sous forme de ressources liées aux parties.

<a id="what-you-need"></a>

## De quoi avez-vous besoin

- Un serveur de synthèse vocale et, s'il partage la carte graphique avec votre modèle de langage, les requêtes qui libèrent de la place pour lui puis l'arrêtent à nouveau ([Installer le serveur de synthèse vocale](audiobook-server.md)).
- Des voix : un court **extrait de référence** (12 à 30 secondes d'une seule personne, sans musique) pour chaque voix que vous voulez cloner, ou l'un des fichiers de voix que le serveur propose. Un narrateur a besoin d'une voix de lecture calme et régulière.
- Pour un livre à plusieurs voix, un modèle de langage capable d'étiqueter le texte. C'est par défaut le modèle du livre.
- **ffmpeg** est facultatif. Sans lui, tout fonctionne en mp3. Avec lui sur le serveur (il est présent dans l'image Docker, et sur un ordinateur de bureau il est trouvé dans le `PATH` ou dans les dossiers habituels comme `/opt/homebrew/bin` ; la variable `MARGINALIA_FFMPEG` indique son emplacement lorsqu'il se trouve ailleurs), l'export obtient le format **m4b**, et un administrateur peut stocker l'audio en wav.

<a id="the-settings"></a>

## Les paramètres

*Paramètres → Paramètres de l'extension → Paramètres du livre audio* est le panneau de votre compte ; enregistrez la page *Paramètres* après l'avoir modifié.

![Le panneau avec un backend et les acteurs](../../images/plugin-audiobook-settings.png)

### Backends

Un backend est un serveur de synthèse vocale et le modèle avec lequel il parle. Ajoutez-en un avec **+**.

| Paramètre | Ce qu'il fait |
|---|---|
| Nom | Le nom affiché dans la fenêtre de conversion. |
| Adresse | L'adresse de base du serveur de synthèse vocale, ou de ce qui le relaie, par exemple `http://localhost:8004`. |
| Clé d'API | Envoyée comme jeton bearer si le serveur (ou votre proxy) en demande une. Elle est stockée chiffrée et n'est plus affichée ; saisissez-en une nouvelle pour la remplacer, ou utilisez **Supprimer la clé**. |
| Modèle de synthèse | **Chatterbox Turbo** (anglais, avec des balises sonores comme `[sigh]`, le plus rapide et par défaut), **Chatterbox** (anglais, avec une *exagération* de l'intonation) ou **Chatterbox Multilingual** (23 langues). Les requêtes avant un lot doivent laisser ce modèle chargé. Un livre qui a besoin d'un autre modèle utilise un autre backend. |
| GPU (nom) | Facultatif. Les serveurs qui partagent un même GPU, y compris ceux du [Générateur d'images](image-generator.md), reçoivent ici le même nom : leurs travaux attendent alors les uns les autres, et un backend pour un autre modèle attend que les autres aient fini. Vide : seuls les lots vers cette adresse attendent les uns les autres. |
| Le serveur est sur cette machine ou sur mon réseau | Autorise les adresses du réseau local. Laissez-le désactivé pour un serveur dans le cloud : il ne peut alors pas être amené à appeler votre propre machine. |
| Délai d'expiration d'une requête | Le temps d'attente pour un morceau de parole. |
| Redémarrer le serveur après (morceaux) | Pour un serveur de synthèse vocale qui fuit en mémoire et est tué par le système lors d'un long lot : après ce nombre de morceaux, les requêtes qui suivent le lot puis celles qui le précèdent sont refaites au milieu du lot (arrêt, démarrage, chargement), et le travail continue avec un serveur neuf. 0 (la valeur par défaut) ne le fait jamais. Un serveur qui tombe en panne est redémarré quoi qu'indique ce réglage, voir [Dépannage](audiobook-server.md#troubleshooting). |
| Clé des requêtes autour d'un lot | La clé que les requêtes avant et après un lot transmettent en tant que `Authorization: Bearer`. Elle est stockée chiffrée et n'est plus affichée (**Supprimer la clé** l'efface), afin de ne pas avoir à être écrite dans les en-têtes de chaque requête, où elle serait lisible. Vide : la *Clé d'API* ci-dessus est utilisée, ce qui est voulu lorsqu'un proxy tel que LiteLLM dessert tout. Une requête qui possède déjà son propre en-tête `Authorization` le conserve. |
| Voix du narrateur | Le narrateur d'un livre qui n'a pas de distribution. Un fichier de voix du serveur s'écrit `predefined:Henry.wav`. |
| Valeurs par défaut d'un segment | Température, poids CFG, vitesse et taille des morceaux avec lesquels un morceau démarre (le studio peut les modifier pour un seul morceau). |
| Avant un lot, Après un lot | Les requêtes faites autour d'un lot de parties : *avant* libère la carte graphique et démarre le serveur de synthèse vocale, *après* l'arrête. Voir [Un seul GPU pour le modèle et la parole](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vide : ne fait rien. |

**Tester la connexion** fait ce que fait un lot : il envoie les requêtes avant un lot (de sorte qu'un serveur de synthèse vocale arrêté soit démarré), demande au serveur s'il est là et s'il a un modèle chargé, puis envoie les requêtes après le lot. Sans requêtes, il se contente de demander.

Un livre qui contient des morceaux dans une autre écriture (du japonais dans un livre en anglais) les envoie au modèle multilingue, si vous avez un backend pour lui, avec leur langue ; ils restent sur le modèle du livre si vous n'en avez pas.

<a id="shared-speech-servers"></a>

### Serveurs de synthèse vocale partagés

Sur une installation à plusieurs utilisateurs, l'administrateur peut tenir des serveurs de synthèse vocale pour tout le monde, de sorte qu'une machine dotée d'un GPU soit configurée une seule fois et que personne n'ait besoin d'une adresse ni d'une clé. L'administrateur le fait dans le même panneau (les dernières sections, visibles seulement par les administrateurs) : **Serveurs de synthèse vocale partagés** ouvre une fenêtre avec les mêmes champs qu'un backend à vous, ainsi que la liste de ceux qui peuvent l'utiliser : **tous les utilisateurs**, ou les utilisateurs choisis dans la liste. Un interrupteur là, **Les utilisateurs peuvent ajouter leurs propres serveurs de synthèse vocale**, décide si les backends ci-dessous restent autorisés.

Ce que vous obtenez :

- **Backend utilisé** en haut du panneau liste les serveurs partagés que vous pouvez utiliser (marqués *(partagé)*) et les vôtres. La fenêtre *Convertir en audio* et le studio utilisent celui que vous choisissez ; les commandes l'utilisent aussi.
- Vous ne voyez que le nom d'un serveur partagé, jamais son adresse, ses clés ou ses requêtes. Les lots de tous les utilisateurs sur une même adresse de serveur attendent les uns les autres, de sorte que deux utilisateurs ne puissent pas démarrer deux fois le même GPU.
- Lorsque l'administrateur vous retire un serveur, un travail qui le désigne encore est refusé ; choisissez-en un autre.
- Lorsque l'administrateur désactive vos propres serveurs, le panneau l'indique. Ceux que vous avez ajoutés sont conservés et réutilisés si l'interrupteur est de nouveau activé.
- La *Voix du narrateur* d'un serveur partagé doit être un fichier de voix du serveur (`predefined:Henry.wav`) : un extrait de référence est une ressource d'un seul utilisateur et ne peut pas être lu par les autres. Les voix de vos acteurs fonctionnent comme d'habitude.

<a id="actors"></a>

### Acteurs

Un **acteur** est une voix à vous : un extrait de référence (l'une de vos [ressources](../resources.md) audio, ou un nouveau téléversé ici) ou un fichier de voix du serveur, avec des voix propres pour une **humeur** (un chuchotement, un cri) ou une **langue** (un extrait qui parle japonais). La langue l'emporte sur l'humeur, et sans voix pour ces cas, c'est la voix de l'acteur qui lit. Vos acteurs sont conservés dans votre compte ; n'importe quel livre peut attribuer un acteur à son narrateur et à ses personnages.

**Aperçu** à côté d'une voix (dans l'acteur, les paramètres et la distribution) fait prononcer une courte phrase avec elle (dans la langue de la voix pour une langue) et la joue, en démarrant le serveur de synthèse vocale si les requêtes avant un lot le font. Les acteurs sont listés par leur nom.

![Un acteur avec une voix pour une humeur](../../images/plugin-audiobook-actor.png)

<a id="converting-new-parts-by-itself"></a>

### Convertir les nouvelles parties automatiquement

*Convertir automatiquement les nouvelles parties en audio* est le réglage par défaut de tous vos livres ; voir [Conversion automatique](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Le casting et la prononciation d'un livre

Les deux sont conservés avec le livre (ils voyagent dans sa sauvegarde) et se modifient depuis la boîte de dialogue **Convertir en audio**.

**Casting** indique qui lit : un acteur pour le **narrateur**, un pour les **autres personnages parlants** (quiconque le modèle nomme et que le casting n'a pas) — vide : le narrateur lit — et un pour chaque **personnage**. Un personnage a un nom et d'autres noms que le texte peut utiliser pour lui (*Tom* pour *Tomáš Novák*), séparés par des virgules ; une réplique est attribuée au personnage lorsque son nom, l'un des autres noms, ou un mot du nom qu'aucun autre personnage ne partage, correspond, sans tenir compte de la casse ni des accents. **Ajouter les personnages du livre** remplit la liste à partir des personnages du point de vue et des personnages présents dans les parties.

![Le casting d'un livre](../../images/plugin-audiobook-cast.png)

**Prononciation** indique comment le texte est écrit *pour la voix* ; le livre lui-même n'est jamais modifié. Le **dictionnaire** remplace des mots entiers (la casse compte) par ce qui doit être dit à leur place : une graphie phonétique pour un nom ou un mot inventé, ou une lecture en kana pour le japonais. Pour un livre en anglais, les règles lisent aussi à voix haute les autres éléments : nombres, ordinaux, années, heures, sommes d'argent et abréviations, écrits en toutes lettres ; un titre qui n'est qu'un nombre, par exemple *Chapter four. The Arch* (le mot qui précède le nombre est de votre choix) ; les marques markdown telles que `*` sont supprimées ; et une rupture de scène (`***`, `---`) n'est pas lue mais produit une pause plus longue. Désactivez les règles pour un livre dans une autre langue ; le dictionnaire reste appliqué. **Essayer un texte** montre comment un passage sera lu. Un passage dont le texte parlé change est refait, les autres sont conservés.

![La prononciation d'un livre](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Conversion d'un livre

*Menu Outils de l'éditeur de l'histoire (le bouton en forme de rouage) → **Convertir en audio***.

![La boîte de dialogue avant le démarrage](../../images/plugin-audiobook-convert.png)

1. Choisissez le **moteur** et appuyez sur **Charger** : les parties contenant du texte de la branche active sont listées (la première est la 1) avec leur état :
   *Non générée*, *Étiquetée* (les étiquettes du modèle sont enregistrées, pas encore d'audio), *Générée* (avec un bouton de lecture et un téléchargement) ou
   *Générée (le texte a changé)* (l'audio a été fait à partir d'un texte plus ancien et sera refait) ; *(final)* est ajouté à une partie que le studio marque comme finale.
2. Laissez **Étiqueter les parties avec un modèle de langage** désactivé pour la version simple : le narrateur lit chaque paragraphe. Activez-le pour que le modèle décide qui parle à chaque réplique, de quelle manière (calme, chuchotée, criée...) et où placer une balise sonore, et pour que le casting fournisse les voix. Le modèle du livre est choisi ; vous pouvez en choisir un autre si vous le souhaitez. Les parties sont d'abord étiquetées, puis lues. Une partie qui a déjà des étiquettes pour son texte est lue avec celles-ci même si la case est décochée (le modèle n'est pas sollicité) ; relancer un livre étiqueté sans la case ne transforme donc pas ses dialogues en simple narration. **Supprimer les étiquettes** permet de demander une lecture simple ou de nouvelles étiquettes.
3. Cochez les parties (la case de l'en-tête les coche toutes) et **planifiez** ce qu'il faut faire : la colonne **Plan** l'affiche, mais rien n'est encore fait :
   **Générer** (produire ce qui manque ; une partie à jour n'est pas refaite), **Régénérer** (supprimer l'audio, supprimer les étiquettes, générer),
   **Supprimer l'audio** (les morceaux et l'audio assemblé de la partie ; les étiquettes restent), **Supprimer les étiquettes** (le modèle sera de nouveau sollicité la prochaine fois que la partie sera produite), **Effacer le plan**. Les étapes s'additionnent sur une partie (Supprimer l'audio et Supprimer les étiquettes sans Générer ne font que nettoyer) ; une partie marquée finale est exclue de tous les plans.
4. **Démarrer** exécute le plan. Lorsqu'il retire de l'audio ou des étiquettes, il demande d'abord confirmation. Ce qui doit être généré est placé dans votre file d'attente, qui continue de tourner lorsque vous fermez la fenêtre et lorsque vous quittez la page ; une partie qui attend ou s'exécute dans la file est exclue d'un plan. L'état, les morceaux déjà produits et le message de la tâche sont affichés sur la ligne de la partie.
   Pendant que le modèle étiquette une partie, sa ligne affiche *Étiquetage des morceaux 17-32 sur 120* (le modèle est sollicité pour 16 morceaux à la fois) ; **cliquez sur l'état** d'une partie étiquetée pour lire, pour chaque requête, le raisonnement du modèle et sa réponse, au fur et à mesure qu'ils arrivent. Ils sont conservés en mémoire avec la tâche, jusqu'à ce que les tâches terminées soient effacées (*Effacer les terminées*) ou que l'application redémarre.

![La file d'attente : morceaux déjà produits, et un téléchargement lorsqu'une partie est terminée](../../images/plugin-audiobook-queue.png)

Comment une partie est produite, pour savoir à quoi vous attendre :

- Le texte est **découpé par le programme**, jamais par le modèle, en morceaux de narration et en morceaux de discours direct (entre guillemets doubles droits ou courbes, ou entre crochets japonais ; un titre est de la narration). Le modèle se contente d'*étiqueter* les morceaux numérotés. Un texte avec des balises sonores n'est utilisé que lorsqu'il s'agit du morceau avec les balises ajoutées et que rien d'autre n'a changé, de sorte que le modèle ne puisse jamais modifier votre livre.
- **Qui parle dans un morceau est décidé par le modèle pour le discours ; la narration revient au narrateur.** Une lettre, une note, un message ou les pensées d'un personnage n'ont pas de guillemets : le programme les considère donc comme de la narration. Un tel morceau n'est lu par le personnage (la voix du casting) que lorsqu'il **commence en italique** (`*...*` ou `_..._`) et que le modèle nomme le personnage, d'après le contexte (une signature, « l'écriture était sans équivoque »). La narration à la troisième personne, même lorsqu'elle est racontée du point de vue d'un personnage (« Elle le regarda traverser la pièce »), est toujours celle du narrateur : le programme ignore le personnage que le modèle attribue à un morceau sans italique. Les italiques d'insistance, de sons ou de mots étrangers sont aussi de la narration. Les mots restent toujours les vôtres.
- **La consigne donnée au modèle est un modèle que vous pouvez modifier.** *Paramètres → Paramètres des extensions → Paramètres de l'audiobook → Consigne d'étiquetage* est votre valeur par défaut ; un livre (onglet *Prompts* → *Paramètres des extensions* → *Audiobook*) ou un dossier peut avoir la sienne. Vide signifie celle du dossier parent, puis la vôtre, puis celle intégrée. Il est rédigé comme les autres [prompts](../books/prompts.md) : les variables sont `{{characters}}`, `{{deliveries}}`, `{{tags}}` et `{{wordsPerTag}}`, et la fenêtre d'aide propose **Insérer le modèle par défaut**. Une consigne modifiée fait que les étiquettes des parties à traiter seront de nouveau demandées la prochaine fois qu'elles seront produites.
- **Le volume sonore des parties est uniformisé** lorsque le programme ffmpeg est installé (le même que celui qui produit le `.m4b`) : l'audio assemblé de chaque partie est ramené à un même niveau de sonie (EBU R128, linéaire, pour que la dynamique soit conservée ; **-18 LUFS** sauf si vous le modifiez dans *Paramètres → Paramètres des extensions → Paramètres de l'audiobook*, où vous pouvez aussi le désactiver). Les morceaux sont conservés tels que le serveur les a produits : seule la partie assemblée est uniformisée, afin qu'un audiobook ne change pas de niveau d'une partie à l'autre. **Les parties produites auparavant** ne sont pas modifiées tant que vous ne les convertissez pas à nouveau : cela les réassemble à partir des morceaux stockés et les uniformise, sans rien demander au serveur de synthèse vocale (ni décharger le modèle de langage). Désactiver l'uniformisation laisse les parties déjà uniformisées telles quelles. Si ffmpeg échoue pour une partie, elle conserve son audio tel quel et le résultat l'indique.
- Toutes les parties d'un même clic sont produites en **un seul lot par modèle vocal** : les requêtes précédant le lot sont exécutées une fois, puis tous les morceaux sont lus, puis les requêtes suivant le lot sont exécutées une fois. C'est ce qui évite que la carte graphique change de modèle à chaque partie.
- Chaque morceau est **conservé selon son contenu** (texte, voix, modèle, langue, réglages, graine). Une partie déjà à jour ne coûte rien, un paragraphe modifié est le seul morceau refait, et une partie en échec conserve les morceaux déjà produits lorsque vous appuyez sur **Réessayer**.
- Les morceaux sont assemblés avec des pauses qui dépendent de leur place (après un titre, entre les paragraphes, avant *dit-il*) en un mp3 par partie, conservé avec la partie.
- Pendant qu'une génération de votre part s'exécute, la synthèse vocale attend, afin que le modèle de langage ne soit pas déchargé sous elle.
- Une partie que vous avez marquée **finale** dans le studio est laissée intacte, et la boîte de dialogue indique combien il y en a.
- **L'audiobook complet** assemble chaque partie disposant d'un audio à jour en un seul mp3 avec une marque de chapitre pour chaque partie, et affiche un lien de téléchargement sous la liste (les parties exclues sont nommées). Le même fichier correspond au format d'export *un mp3 avec marques de chapitres*.
- **Tout arrêter** annule les parties en attente et interrompt immédiatement celle en cours : la requête au modèle de langage (l'étiquetage) ou au serveur de synthèse vocale (le morceau en cours de lecture) est abandonnée, sans attendre. Un lot arrêté avant de commencer n'envoie pas les requêtes qui le précèdent, de sorte que le modèle de langage reste chargé. **Télécharger** fournit le mp3 d'une partie terminée.
<a id="the-audiobook-studio"></a>

## Le studio de livre audio

*Menu Outils → **Studio de livre audio*** (la voix du passage sélectionné, avec un aperçu, se trouve à côté de son haut-parleur) est l'endroit où le livre audio final est fabriqué, une partie à la fois : vous voyez le script, le modifiez, écoutez un passage et fabriquez la partie.

![Le studio avec une réplique sélectionnée](../../images/plugin-audiobook-studio.png)

- Pendant qu'une partie est annotée ou fabriquée, une barre indique qu'un traitement est en cours, et les boutons qui lanceraient d'autres traitements attendent.
- **Annoter** produit le script de la partie (avec le modèle de langage si vous le demandez, sinon avec le narrateur) et affiche les passages dans un tableau : locuteur, mode de diction, intensité, texte prononcé et une marque pour un passage que vous avez modifié. Pendant son exécution, un bouton **Arrêter** apparaît à côté et met fin à la requête adressée au modèle de langage.
- Sélectionnez un passage pour modifier son **locuteur**, le **texte prononcé**, le **mode de diction**, l'**intensité** (l'exagération de la voix, pour les modèles qui en disposent), la **langue**, la **graine**, la **pause après** celui-ci. **Appliquer** conserve la modification et **verrouille** le passage : une nouvelle annotation laisse un passage verrouillé tel quel. **Déverrouiller** le rend à l'annotation. *La requête reçue par le serveur de synthèse vocale* affiche la requête exacte.
- **Écouter** produit l'audio d'un passage (uniquement ce passage ; le serveur de synthèse vocale est lancé pour lui), et **Une autre prise** le refait avec une nouvelle graine. Les **prises** sont conservées, de sorte que vous pouvez choisir celle qui vous plaît (*Utiliser cette prise et l'écouter*). Une bonne voix dépend souvent de la graine.
- **Fabriquer la partie à partir de ce script** assemble les passages tels quels, sans nouvelle annotation.
- Le **statut** de la partie est *brouillon*, *relue* ou *finale*. Une partie finale n'est ni annotée ni reconvertie à nouveau, et l'export peut être limité aux parties finales.

<a id="exporting"></a>

## Exportation

Le livre audio est un format de **Exporter l'histoire** du livre : **Livre audio (un mp3 par chapitre, dans un zip)**, **Livre audio (un mp3 avec repères de chapitres)** et, lorsque ffmpeg est installé, **Livre audio (un m4b avec chapitres)**. Ils sont composés de l'audio des parties à jour : une partie sans audio, ou un audio plus ancien que son texte ou que son script dans le studio, est laissée de côté, et la fenêtre l'indique. Les fichiers mp3 portent des étiquettes ID3 (le titre du livre, le chapitre, la piste) ; le fichier unique contient des repères de chapitres que des lecteurs comme VLC et la plupart des applications de livres audio affichent ; le zip contient une liste de lecture. La case **Livre audio : seulement les parties marquées finales** limite l'export aux parties que vous avez terminées dans le studio.

Le **m4b** est le format qu'Apple Livres et la plupart des applications de livres audio sur téléphone attendent : audio AAC (64 kbit/s, suffisant pour la parole) dans un conteneur MP4 avec les chapitres, le titre, l'auteur et le genre. C'est ffmpeg qui le produit ; le format n'est donc proposé que sur un serveur disposant de ffmpeg. L'audio stocké en mp3 est encodé une seconde fois pour celui-ci ; l'audio stocké en wav n'est encodé qu'une fois.

<a id="how-the-audio-is-stored"></a>

### Comment l'audio est stocké

Un administrateur le choisit pour l'ensemble de l'installation dans *Paramètres → Paramètres des extensions → Paramètres du livre audio* (la dernière section, visible seulement par les administrateurs) :

- **mp3** (par défaut) : léger, assemblé sans décodeur, sans besoin de ffmpeg. Le m4b est une seconde étape avec pertes.
- **wav** : sans perte, environ dix fois plus volumineux (une heure de parole représente environ 170 Mo), nécessite ffmpeg. Les passages sont demandés au serveur de synthèse vocale au format wav et assemblés en wav ; le navigateur les lit tels quels ; les exports mp3 et zip sont encodés par ffmpeg (96 kbit/s) et le m4b n'est encodé qu'une fois.

Ce choix s'applique à ce qui est produit à partir de ce moment. Les parties produites auparavant conservent leur audio et sont interprétées telles quelles ; une partie des deux types (un passage verrouillé du studio, antérieur à ce changement) est assemblée en wav, et un export de parties des deux types fonctionne, mais nécessite ffmpeg.

![La fenêtre d'export avec les formats de livre audio](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversion automatique

Un livre peut produire lui-même l'audio d'une nouvelle partie, dès que la partie est finale : après l'annotation automatique, la reformulation et les traductions si le livre en possède, comme dernière étape de [ce qui suit une partie générée](../books/story-editor.md#what-follows-a-generated-part) (dans le panneau des traitements, jusqu'à la production de l'audio ; son bouton **Arrêter** retire la partie de la file d'attente). Elle est **désactivée** par défaut. Choisissez-la dans l'onglet *Prompts* du livre, sous les paramètres d'extension, pour un dossier de livres, ou pour tous vos livres dans les paramètres du livre audio ; c'est le réglage le plus proche qui s'applique :

- **Non**,
- **Oui : le narrateur lit la partie**,
- **Oui : étiquetée par le modèle du livre, la distribution donne les voix** ; un livre sans modèle obtient la narration simple, et vous en êtes averti.

![Le réglage dans un livre](../../images/plugin-audiobook-auto.png)

La partie entre dans votre file d'attente, avec la distribution et la prononciation du livre. Une notification vous indique qu'elle est en file d'attente, une autre que son audio est prêt ou a échoué ; elles n'apparaissent que dans la page qui a lancé la génération, et le travail figure de toute façon dans la fenêtre *Convertir en audio*.

<a id="listening"></a>

## Écoute

Un livre publié (ou le lecteur) qui possède des parties avec audio affiche une barre de lecteur : lecture, pause, arrêt, vitesse, et la partie entendue est marquée dans le texte. Elle commence à partir de la sélection ou de la première partie visible, joue les parties dans l'ordre et charge la suivante pendant que la courante joue ; les commandes multimédias de l'appareil connaissent le livre. L'audio produit à partir d'un ancien texte d'une partie n'est pas proposé. L'audio est servi au propriétaire du livre et, tant que le livre est publié, à toute personne qui peut l'ouvrir ; un livre retiré de la publication cesse d'être servi immédiatement (à la prochaine partie, lorsque le lecteur se charge).

![Le lecteur dans la liseuse](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Une partie sans audio

Le lecteur s'arrête sur une partie sans audio, afin qu'aucune partie de l'histoire ne soit sautée, et l'indique. Lorsque l'extension [Lecture à voix haute](tts-reader.md) est installée, la barre affiche aussi un bouton haut-parleur, **Lire cette partie (et les suivantes sans audio) avec la voix du navigateur**. Rien n'est lu avec une voix de synthèse sans que vous ne l'appuyiez. Elle lit alors cette partie et les parties suivantes qui n'ont pas d'audio, avec la voix de votre appareil ; lorsqu'elle atteint une partie qui possède un audio, le lecteur reprend la main. Les parties suivantes sans audio, dans la même écoute, sont lues de la même façon sans nouvelle demande ; **Arrêter** la fait taire, et relancer le lecteur redemande. La voix, la vitesse et la hauteur sont celles de Lecture à voix haute.

<a id="commands"></a>

## Commandes

Les mêmes actions que [les commandes](../commands/index.md) (la ligne de commande de l'éditeur d'histoire, la macro `command` d'un modèle, un agent). Les parties sont numérotées comme la fenêtre de conversion les numérote, ou désignées par leur identifiant.

| Commande | Ce qu'elle fait |
|---|---|
| `/audiobook-status [from [to]]` | Le serveur de synthèse vocale utilisé, la façon dont le livre se convertit automatiquement, combien de parties ont un audio (aucun, prêt, plus ancien que le texte, plus ancien que le script), le statut de chaque partie listée dans le studio, et la file d'attente. Lent seulement lorsque le livre est long. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Place les parties dans la file d'attente (le livre entier sans argument, une seule partie lorsque seul `from` est donné). `labelling` `true` utilise le modèle du livre ; `what` vaut `audio` ou `script` (seulement le script pour le studio, rien n'est prononcé). Avec `wait` `true`, la commande reste active jusqu'à la fin des travaux, indique leur avancement et précise comment chacun s'est terminé ; l'arrêter annule les travaux qui n'ont pas commencé. |
| `/audiobook-stop` | Annule les parties en attente ; la partie en cours s'arrête après son passage. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` ou `final` dans le studio ; la partie doit avoir un script. |
| `/audiobook-cast` | Le narrateur, le recours et les personnages de la distribution avec leurs interprètes. |

![Le résultat d'une commande](../../images/plugin-audiobook-command.png)

L'audio est produit pour **votre** livre, et non pour la copie de travail cachée d'un [agent](../agents.md) : un agent peut lire la distribution, et reçoit un refus des autres commandes, lui indiquant que vous devez les lancer vous-même. Lancez-les depuis la ligne de commande, ou confiez à un agent la tâche de préparer le livre (annoter, relire) et convertissez-le vous-même.
## Notifications

Quand un lot de conversions est terminé, le navigateur peut vous en informer ([notifications](../account.md#notifications), afin que vous puissiez quitter l'onglet pendant que le serveur de synthèse vocale travaille). Dans la liste des notifications de la page *Paramètres*, il y a deux lignes : **Convert to audio** (les lots que vous lancez depuis la boîte de dialogue ou avec la commande) et **Convert to audio (automatic)** (une nouvelle partie qui a été transformée en audio automatiquement). Un lot que vous avez arrêté ne déclenche pas de notification, un lot dont une partie a échoué l'indique.

![La liste des notifications](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Ce qu'il ne fait pas (encore)

- Mp3 et wav uniquement (pas d'opus) ; le volume sonore des morceaux dépend de ce que produisent les voix.
- Pas d'effets sonores ni d'ambiance.
- Un seul moteur de synthèse vocale, Chatterbox. Les autres moteurs sont la prochaine étape.
- L'écoute d'une voix en dehors d'un segment.
- La distribution n'est pas incluse dans la sauvegarde d'un livre de vos *acteurs* : un livre restauré conserve sa distribution, mais elle nomme des acteurs qui doivent exister dans votre compte.
