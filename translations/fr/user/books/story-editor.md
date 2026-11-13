---
label: Éditeur d'histoire
order: 90
---

# Éditeur d'histoire

L'onglet **Histoire** d'un livre est l'endroit où vous écrivez. Il affiche les parties de la [branche active](branches-and-story-tree.md)
du début jusqu'à la fin.

![L'éditeur d'histoire](../../images/hero-story-editor.png)

Cette page décrit la disposition sur ordinateur. Sur téléphone, l'histoire tient dans une seule colonne et le plan se trouve dans un tiroir,
voir [Utiliser Marginalia sur un téléphone](../phone.md#the-story).

| Zone | |
|---|---|
| **Plan** (à gauche) | Une entrée par partie, `#1`, `#2`... avec l'identifiant de la partie dans la base de données. Cliquez sur une entrée pour faire défiler jusqu'à la partie (les parties qui la précèdent sont chargées d'abord si elle ne l'est pas encore). L'en-tête indique combien de parties compte la branche. Une partie qui possède un [résumé](summaries.md) affiche une petite icône de document après son entrée. L'extension [Chapter Marker](../extensions/chapter-marker.md) affiche ici les titres des chapitres. |
| **Lorebook** (à gauche, à côté du Plan) | Un aperçu du lore du livre, avec une zone de recherche et une modification rapide du texte d'une entrée, voir [L'onglet lorebook](#the-lorebook-tab). |
| **Annotations** (à gauche, à côté du Plan) | Les [notes](annotations.md) sur des passages de l'histoire, pour l'ensemble du livre. |
| **Planificateur** (à gauche, à côté du Plan) | Les scènes que vous prévoyez d'écrire, une liste que vous remplissez à la main, voir [L'onglet planificateur](#the-planner-tab). |
| **Histoire** (au centre) | Le texte de chaque partie, avec son menu (☰) et ses métadonnées à droite, et en dessous la barre inférieure. |
| **Barre inférieure** | Le bouton **Résumés** (icône de liste) et le menu des paramètres (⚙) à gauche, la ligne de commande, et le bouton **+** à droite. |

Voilà à quoi ressemble un nouveau livre. Tous ces éléments sont des onglets que vous pouvez disposer comme vous le souhaitez, l'histoire comprise,
voir [Disposer les onglets](#arranging-the-tabs). La séparation entre la barre latérale et l'histoire peut être déplacée, et Marginalia
retient où vous l'avez laissée (enregistrée avec le livre) et y revient lorsque vous rouvrez le livre.

L'histoire s'ouvre toujours en bas, à la partie la plus récente. Un livre long n'est pas affiché d'un bloc : seules les dernières parties sont
chargées, et les précédentes s'ajoutent au-dessus à mesure que vous remontez, sans déplacer la partie que vous lisez. Les parties à l'autre
extrémité sont retirées à nouveau, de sorte que pas plus que le nombre défini dans le paramètre **Parties chargées à la fois dans l'histoire** de
votre [compte](../account.md) ne reste chargé (vide les charge toutes). Si vous faites défiler plus vite que les parties n'arrivent, une marque
*Chargement des parties précédentes* (ou *suivantes*) au bord indique qu'il y en a davantage. Cliquer sur une partie dans le plan, ou en trouver une
avec la recherche, charge les parties autour d'elle, et commencer une nouvelle partie vous ramène à la fin de l'histoire.

<a id="searching-the-story"></a>

### Rechercher dans l'histoire

Au-dessus du plan se trouve un champ de recherche portant sur le texte de la **branche sur laquelle vous êtes** (la recherche de l'arbre de l'histoire
parcourt toutes les branches). Comme un livre long n'est pas chargé d'un bloc, la recherche du navigateur (Ctrl+F) ne peut pas trouver de texte
dans les parties non chargées ; celle-ci le peut.

- Tapez ce que vous cherchez ; la recherche démarre lorsque vous cessez de taper. Plusieurs mots doivent tous se trouver dans la même partie, une expression
  se met entre guillemets doubles (`"vieille clé"`) et `*` remplace n'importe quels caractères (`phare*`). La casse n'a pas d'importance.
- Chaque endroit où un mot apparaît est une correspondance, et elles sont numérotées sur toute la branche. Sous le champ, vous voyez
  *Correspondance 3 sur 27, dans la partie #5* et une barre indiquant où vous en êtes. L'histoire défile jusqu'à la correspondance et charge les parties autour
  lorsqu'elles ne sont pas chargées.
- Les boutons **haut** et **bas** vont à la correspondance précédente et à la suivante (après la dernière, on revient à la première).
  **Entrée** dans le champ fait la même chose que le bouton bas, **Maj+Entrée** que le bouton haut.
- Toutes les correspondances dans les parties chargées sont surlignées dans le texte, la correspondance où vous vous trouvez dans une couleur plus vive. Videz
  le champ pour retirer le surlignage.

Sur téléphone, la recherche se trouve dans le tiroir du plan ; le tiroir se ferme lorsqu'il affiche une correspondance, rouvrez-le pour la suivante.

<a id="the-lorebook-tab"></a>

### L'onglet lorebook

L'onglet **Lorebook** de la barre latérale affiche le lore du lorebook du livre sans quitter l'histoire, de sorte que vous n'avez pas à changer d'onglet pour vérifier
ce que dit une entrée, ou pour corriger un mot. Il est relu chaque fois que vous revenez à l'onglet *Histoire*, de sorte que les modifications faites dans l'onglet
*Lorebook* du livre apparaissent aussitôt.

- La **zone de recherche** en haut n'affiche que les entrées qui contiennent le texte dans leur nom, leur contenu, leur note ou leur filtre (la casse n'a pas d'importance).
- Le **crayon** d'une entrée ouvre une petite fenêtre avec son nom, son activation, le filtre et la manière dont elle est recherchée, le contenu
  et la note ; **Enregistrer** les écrit (les étiquettes, l'ordre et les réglages d'activation se modifient dans l'onglet *Lorebook* du livre). Rien
  ne peut être modifié pendant qu'un agent travaille sur le lorebook.
- Le **bouton d'agrandissement** (⤢) ouvre l'[éditeur de lorebook en plein écran](../lorebooks/lorebooks.md#editing-in-full-screen) (ordinateur uniquement) : celui
  à côté de la zone de recherche sur le lorebook, celui d'une entrée sur cette entrée, celui d'un sous-lorebook (avec *Inclure les sous-lorebooks*) sur
  ce lorebook. L'onglet est rechargé lorsque vous fermez l'éditeur.

- Chaque entrée est une ligne avec son nom et le crayon ; la flèche l'ouvre pour afficher son **contenu** (rendu en Markdown) et le **texte qui l'active** (le
  *Texte/motif de filtrage*, avec une note lorsqu'il s'agit d'une expression régulière).
- **Inclure les sous-lorebooks** affiche le lorebook et chacun de ses sous-lorebooks (et leurs propres sous-lorebooks, quelle que soit la profondeur) en lignes distinctes, avec leurs entrées
  dessous ; cliquez sur la flèche d'un lorebook pour masquer ses entrées. Sans cette option, seules les entrées du lorebook du livre sont listées.
- Les entrées et les lorebooks désactivés (ils ne sont pas utilisés pour l'histoire) sont affichés en grisé.
- Un livre sans lorebook affiche une note à la place.

![L'onglet Lorebook avec deux entrées ouvertes](../../images/story-lorebook-tab.png)

Comme tout onglet de l'éditeur, il peut être glissé dans un panneau à part, voir ci-dessous.
<a id="the-planner-tab"></a>

### L'onglet Planificateur

L'onglet **Planificateur** est une liste des scènes que vous voulez voir traverser l'histoire, dans l'ordre que vous souhaitez. Chaque scène correspond aux détails d'un tour qui l'écrit : un **titre** pour la liste, le **cadre de la scène**, le **personnage POV**, les **personnages présents** et les **instructions**, soit les quatre mêmes champs que le formulaire du nouveau tour.

![L'onglet Planificateur avec quatre scènes, la première déjà écrite](../../images/planner-tab.png)

- **Ajouter une scène** ouvre un formulaire pour une nouvelle scène ; elle est ajoutée à la fin de la liste. Le crayon modifie une scène, les flèches la déplacent vers le haut ou vers le bas, la corbeille la supprime.
- Le **+** de la ligne d'une scène **insère une nouvelle scène** juste en dessous, entre cette scène et la suivante.
- Le bouton de fusion d'une scène la **fusionne avec la suivante** : un formulaire s'ouvre avec les détails des deux scènes déjà réunis (les deux titres, les cadres de scène et les instructions l'un sous l'autre, les personnages présents des deux, le personnage POV de la première ; lorsque la seconde scène en a un autre, le formulaire l'indique). Modifiez ce qui ne convient pas et appuyez sur **Fusionner** (ou <kbd>Ctrl/Cmd+Entrée</kbd>) : les deux scènes ne forment plus qu'une, à la place de la première. L'opération n'est effectuée que si les deux scènes ont été écrites.
- **Importer depuis la branche** ajoute une scène pour chaque partie de la branche sur laquelle se trouve le livre (de la première partie jusqu'à la plus récente) qui possède des détails de tour : son cadre de scène, son personnage POV, ses personnages présents et ses instructions, la première ligne des instructions servant de titre. Les parties sans détails de tour sont laissées de côté, et les scènes sont ajoutées après celles que vous avez déjà, non écrites. Utilisez-le pour planifier la réécriture d'une histoire que vous avez écrite : importez, fusionnez les tours qui vont ensemble, insérez ce qui manque, puis écrivez les scènes sur une nouvelle branche ou dans une copie du livre.
- La case devant une scène indique qu'elle est **terminée** (écrite) : le titre est barré et la scène est estompée. Rien ne le vérifie à votre place, cochez-la lorsque vous avez écrit la scène. La ligne au-dessus de la liste compte combien de scènes sont terminées.
- **Cliquez sur une scène** pour placer ses détails dans le **formulaire du nouveau tour** (le formulaire s'ouvre, avec le cadre de scène, le POV, les personnages présents et les instructions de la scène ; ce qui s'y trouvait est remplacé), puis appuyez sur **Générer** lorsque vous en êtes satisfait. Sur téléphone, le tiroir se referme d'abord.
- Les scènes sont enregistrées avec le livre, elles figurent donc dans ses sauvegardes et ses copies. Un modèle peut lui aussi ajouter des scènes : l'[assistant de intrigue](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de l'extension Arc Outline et un [agent](../agents.md) peuvent lire et modifier le planificateur grâce aux [commandes](../commands/index.md#the-planner) ; la liste affiche immédiatement ce qu'ils ont ajouté.

![Le formulaire d'une nouvelle scène](../../images/planner-scene-dialog.png)

![Un clic sur une scène remplit le formulaire du nouveau tour avec ses détails](../../images/planner-new-turn.png)

Comme chaque onglet de l'éditeur, il peut être détaché dans un panneau qui lui est propre, voir ci-dessous.

<a id="arranging-the-tabs"></a>

### Organiser les onglets

L'éditeur est un ensemble d'onglets : l'**Histoire** elle-même (les parties et la barre inférieure qui se trouve sous elles), le **Plan**, la **Bibliothèque** (lorebook), et les onglets que les [extensions](../extensions/index.md) ajoutent (Personnages, Arcs, Side Query...). Faites glisser un onglet par son en-tête pour le réorganiser (ordinateur uniquement ; le téléphone conserve une seule colonne, avec le plan dans un tiroir). Un nouveau livre affiche le Plan et la Bibliothèque dans un panneau à gauche, et l'Histoire dans une colonne à part à droite ; vous pouvez placer l'Histoire où vous voulez, par exemple au centre, avec le plan d'un côté et la bibliothèque de l'autre, ou au-dessus du plan :

- Déposez-le sur l'une des bandes en pointillés qui apparaissent au-dessus, entre et sous les panneaux d'une colonne pour le **détacher** dans un panneau qui lui est propre, dans cette colonne ; les panneaux d'une colonne sont empilés les uns sous les autres.
- Déposez-le sur l'une des bandes verticales situées à gauche, à droite ou entre les colonnes pour le **détacher sur le côté** : il obtient une colonne à part, à côté des autres. Élargissez l'éditeur avec les séparateurs pour faire de la place pour d'autres colonnes.

Les panneaux et les colonnes sont séparés par des séparateurs : faites-les glisser pour donner à chaque panneau la place dont il a besoin. Les tailles sont enregistrées avec la disposition.

![La barre latérale avec Personnages dans une colonne à part et Side Query dessous](../../images/story-editor-tabs.png)

- Déposez-le sur l'en-tête d'un autre onglet ou sur le corps d'un autre panneau pour **rejoindre** ce panneau (déposer sur un onglet le place avant cet onglet, déposer sur le corps le place en dernier). Un panneau qui perd son dernier onglet disparaît, et de même une colonne sans panneau.

La disposition et les tailles sont enregistrées avec le livre : elle est donc la même la prochaine fois que vous l'ouvrez, et dans chaque navigateur ; l'onglet affiché par chaque panneau est mémorisé avec le reste de votre espace de travail. Un onglet d'extension qui n'est pas chargé conserve sa place, pour le moment où l'extension sera de nouveau disponible.

L'histoire est toujours présente : son onglet ne peut pas être fermé, et un onglet qui se trouve dans un panneau avec d'autres onglets est masqué tant qu'un autre est sélectionné. Tout ce qui affiche une partie ramène l'histoire au premier plan : cliquer sur une entrée du plan, un résultat de la recherche, choisir une branche dans l'[arbre de l'histoire](branches-and-story-tree.md), ou lancer une génération sélectionne d'abord l'onglet **Histoire**. Pendant qu'une génération prépare son prompt (généralement quelques secondes), les autres onglets sont désactivés ; une fois que le modèle écrit, ils sont de nouveau utilisables, voir [plus bas](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Réduire les onglets

Chaque onglet, sauf l'histoire, possède un petit bouton **⌄** (chevron) à côté de son nom. Il **réduit** l'onglet : celui-ci quitte son panneau (un panneau dont tous les onglets sont réduits disparaît pour le moment) et une icône ronde le représentant, un cercle coloré contenant un dessin (une liste pour le plan, un livre pour la bibliothèque, une bulle de dialogue pour les annotations, une loupe pour Side Query, et ainsi de suite ; un onglet d'extension qui n'a pas de dessin reçoit une ou deux lettres), apparaît dans une barre sur le bord de l'éditeur. Survolez l'icône pour voir de quel onglet il s'agit. Rien n'est fermé, l'onglet conserve ce qu'il affichait. Cliquez sur l'icône et l'onglet revient dans le panneau, à la place qu'il occupait parmi les onglets, et s'affiche.

La barre est présente tant qu'un onglet est réduit. Le bouton situé en haut de la barre (les flèches circulaires) envoie la barre au côté suivant de l'éditeur, dans le sens des aiguilles d'une montre : gauche, haut, droite, bas. L'histoire ne peut pas être réduite, il y a donc toujours quelque chose à l'écran.

![Onglets réduits dans la barre sur le bord de l'éditeur](../../images/story-minimized-tabs.png)

Les icônes sont dans l'ordre alphabétique des noms des onglets, tant que vous ne le changez pas : faites glisser une icône sur une autre et elle prend la place de celle-ci. Les onglets réduits, la position de la barre et l'ordre des icônes sont enregistrés avec le livre, avec la disposition et les tailles.

<a id="the-default-arrangement"></a>

### La disposition par défaut

Un nouveau livre s'ouvre avec la disposition standard (le plan, la bibliothèque et les annotations à gauche, l'histoire à droite). Pour faire de votre propre disposition la disposition standard, réglez les onglets comme vous le souhaitez (panneaux, tailles, onglets réduits, côté de la barre), puis choisissez **Enregistrer la disposition comme ma disposition par défaut** dans le menu des paramètres (⚙) de l'histoire. Chaque livre qui n'a pas de disposition propre (un livre que vous n'avez pas réorganisé) s'ouvre alors avec celle-ci ; un livre que vous avez réorganisé conserve sa disposition. La disposition par défaut est conservée dans vos paramètres utilisateur.

Pour revenir à la disposition par défaut, choisissez **Rétablir la disposition par défaut** dans le menu des paramètres (⚙) de l'histoire et confirmez. Il s'agit de celle que vous avez enregistrée, ou de la disposition standard si vous n'en avez enregistré aucune. Cela ne fait que replacer les onglets (et oublie les tailles et ce qui est réduit) : vos textes, l'onglet affiché par chaque panneau et les onglets des extensions restent en place, ces derniers dans le premier panneau.
<a id="writing-the-next-part"></a>

## Rédiger la partie suivante

1. Cliquez sur **+** dans le coin inférieur droit. Le panneau *Instructions du nouveau tour* s'ouvre.
2. Remplissez les champs. Tous sont facultatifs, mais le modèle a besoin d'au moins quelques indications :

   | Champ | |
   |---|---|
   | **Cadre de la scène** | Où et quand la partie se déroule, par exemple *Le phare, une nuit d'orage à la fin de l'automne*. |
   | **Personnage du point de vue** | Le personnage dont le point de vue sert à écrire la partie. |
   | **Personnages présents** | Qui est dans la scène, et dans quel état. |
   | **Instructions** | Ce qui doit se passer dans cette partie. |

   *Cadre de la scène*, *Personnage du point de vue* et *Personnages présents* sont remplis à partir de la dernière partie : vous ne modifiez
   que ce qui change. *Instructions* est vide à chaque fois.
3. Cliquez sur **Générer**.

![Le panneau Instructions du nouveau tour](../../images/new-turn-instructions.png)

La nouvelle partie apparaît à la fin et se remplit au fur et à mesure que le modèle l'écrit. Pour les [modèles de raisonnement](../inference-providers.md#reasoning-models),
le raisonnement apparaît d'abord sous **Voir le raisonnement**.

Pendant qu'une partie est générée, le bouton **+** se transforme en bouton rouge **Arrêter**. Arrêter conserve le texte écrit
jusque-là comme partie.

<a id="while-a-part-is-generated"></a>

### Pendant la génération d'une partie

Pendant les premières secondes, le temps que le prompt soit construit à partir du livre, toute la fenêtre du livre attend. Dès que le modèle
commence à écrire, seules la nouvelle partie et les parties qui la précèdent sont retenues :

- **La nouvelle partie** ne peut pas être modifiée tant qu'elle n'est pas terminée : ni édition, ni menu.
- **Les parties qui la précèdent** restent en place : vous pouvez les éditer, les annoter et les rédiger à nouveau, mais pas les supprimer, les fusionner ou les
  embrancher, et l'histoire reste sur sa branche (pas de swipe, pas de changement de branche). Une modification d'une partie antérieure, ou du lore, n'atteint pas la
  partie en cours d'écriture : son prompt a déjà été construit. Elle compte pour la suite.
- **Tout le reste est libre** : le lore, les réglages et le prompt du livre, les onglets de la barre de gauche (requête latérale, plan d'arc, notes...),
  la barre de commandes. Une requête au modèle attend une connexion libre du modèle si la génération utilise la seule disponible.
- **En attente** : un second tour, la recherche et remplacement, un import dans le livre et la suppression en masse. Les onglets de l'arbre et des sauvegardes attendent aussi.

Ce que la fenêtre grise est aussi refusé lorsqu'une commande, un agent ou une extension le demande : Marginalia indique ce qui bloque
(*Ceci est en cours d'utilisation : …*).

**Quitter le livre arrête la génération.** Le bouton de sortie demande confirmation ; *Oui* équivaut à **Arrêter** : ce qui est arrivé est conservé. Fermer
l'onglet du navigateur fait de même, et le navigateur demande confirmation avant de fermer ou de recharger l'onglet pendant qu'une partie est générée. Une courte
coupure du réseau, ou un ordinateur portable fermé une minute, ne constitue pas une sortie : la page revient et affiche ce qui est arrivé entre-temps.

Lorsque le modèle renvoie une erreur (serveur injoignable, clé API incorrecte, contexte trop long...), l'erreur est affichée.
Si aucun texte n'est encore arrivé, l'histoire reste telle qu'elle était : une nouvelle partie ou un swipe est retiré, et une partie régénérée retrouve
son texte précédent. Le texte arrivé avant l'erreur est conservé, comme avec **Arrêter**. Corrigez le problème, puis générez à nouveau.

La manière dont les champs entrent dans le prompt est définie par le [Prompt utilisateur](prompts.md#user-prompt) du livre. Avec le prompt utilisateur
par défaut, le modèle les reçoit sous les noms *Current Location & Time*, *Point of View Character*,
*Characters Currently Present / State* et *Plot Specifications for the Next Part*.

!!!
Le texte est écrit en Markdown : `*italique*`, `**gras**`, `# Titre` et ainsi de suite sont affichés mis en forme. Une partie avec un titre `#`
est un chapitre pour l'extension [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Rédiger les instructions

Vous ne savez pas ce qui doit se passer ensuite ? **Rédiger des instructions**, sous le champ *Instructions* du panneau, laisse le modèle écrire
les instructions à votre place, comme vous le feriez. Une fenêtre s'ouvre avec :

| | |
|---|---|
| **Orientation** | De quoi la partie doit parler : une idée vague, une direction (« elle le confronte au sujet de la lettre »), ou rien du tout, et le modèle poursuit l'histoire là où elle mène. Au départ, il contient ce que vous avez déjà saisi dans *Instructions*. |
| **Plan de l'histoire** | Seulement lorsqu'une source en fournit un. Le [Plan d'arc](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) le fait, avec les données du chapitre en cours. En lecture seule ; le modèle le suit. |

**Rédiger des instructions** lance le modèle, et la réponse s'affiche en continu dans **Instructions rédigées**, que vous pouvez modifier ou vider
avant de continuer ; **Rédiger à nouveau** tente une nouvelle fois, **Arrêter** conserve ce qui est arrivé. Rien ne change tant que vous ne cliquez pas sur
**Utiliser comme instructions** : le texte remplace alors les *Instructions* du panneau (les autres champs ne sont pas touchés) et le panneau s'ouvre
à nouveau, prêt pour **Générer**.

Le modèle reçoit l'histoire jusqu'ici (la partie ancienne sous forme de [résumés](summaries.md), comme pour une génération), le cadre de la scène, le point de vue et
les personnages présents qui figurent dans le panneau, le lore de la dernière partie, vos dernières instructions en guise d'exemples de votre manière d'écrire, votre
orientation et le plan. Il ne voit pas les entrées de votre lorebook qu'un nouveau tour activerait. La requête est le
[prompt d'incarnation](prompts.md#impersonate-prompt). Le modèle est celui du livre ; rédiger des instructions consomme des jetons comme toute requête.

![Rédaction des instructions du tour suivant](../../images/impersonate-dialog.png)

La même chose est faite par la commande [`/impersonate`](../commands/index.md), qui renvoie le texte. Les [agents](../agents.md) n'y ont pas accès : ils écrivent
l'histoire avec leurs propres instructions.

<a id="the-settings-menu"></a>

## Le menu des réglages

Le menu des réglages (⚙) dans la barre inférieure regroupe ce qui agit sur toute l'histoire. **Rechercher et remplacer** est en haut ; le reste se trouve dans des sous-menus
(sur téléphone aussi, sauf les éléments réservés au bureau) :

| Sous-menu | Éléments |
|---|---|
| **Apparence** | **Changer les styles du texte**, **Surligner le texte généré** et **Annotations dans la marge** (bureau uniquement, le dernier) |
| **Analyse** | **Annoter une plage de parties avec l'IA**, **Créer des résumés**, **Prendre des notes de lore avec un agent**, **Annotation automatique**, **Jetons de la branche**, **Utilisation du lore**, **Statistiques** |
| **Import et export** | **Importer**, **Exporter l'histoire** (et les éléments des extensions qui importent ou exportent, comme **Exporter les instructions**) |
| **Nettoyage** | **Effacer l'historique du livre**, **Vider la corbeille de l'historique**, **Suppression en masse** |
| **Disposition** (bureau uniquement) | **Enregistrer la disposition comme ma disposition par défaut**, **Rétablir la disposition par défaut** |

Les éléments apportés par les [extensions](../extensions/index.md) se trouvent dans le sous-menu qui leur correspond, ou à la fin du menu. Les raccourcis des éléments sont dans
*Paramètres → Raccourcis clavier*, où un élément est désigné avec son sous-menu (*Analyse › Statistiques*). Les sections ci-dessous indiquent où se trouve chaque élément.
<a id="the-part-menu"></a>

## Le menu d'une partie

Chaque partie possède un menu (☰) dans son coin supérieur droit. **Modifier**, **Abandonner les modifications**, **Régénérer**, **Variante**, **Réécrire**, **Historique** et **Images** se trouvent dans le menu lui-même, **Supprimer** en est le dernier élément, et le reste se trouve dans quatre sous-menus : **Annotations** (*Annoter la partie*, *Annoter avec l'IA*), **Résumé** (*Générer un résumé* / *Voir le résumé*, *Supprimer le résumé*), **Inspecter** (*Afficher le prompt*, *Comparer le prompt*, *Utilisation des jetons*, *Utilisation du lore*) et **Branches et fusion** (*Créer une branche*, *Fusionner avec la partie précédente*). Un sous-menu sans rien pour la partie n'est pas affiché. Le Relecteur ajoute son sous-menu **Examiner** après *Résumé*. Les éléments :

| Élément | |
|---|---|
| **Modifier** | Rend le texte modifiable. **Enregistrer** (le même élément de menu) l'enregistre. Lancer une génération ou quitter l'onglet enregistre aussi les modifications en cours. |
| **Abandonner les modifications** | Uniquement pendant la modification du texte : ferme le champ sans enregistrer, la partie conserve le texte de son dernier enregistrement. Si vous avez modifié le texte, on vous le demande d'abord. |
| **Régénérer** | Réécrit la dernière partie en remplaçant son texte. Uniquement sur la dernière partie. |
| **Variante** | Écrit une autre version de la dernière partie et conserve l'ancienne comme branche séparée. Uniquement sur la dernière partie. Voir [Branches et arbre de l'histoire](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Réécrire** | Demande au modèle de réécrire cette partie à partir de vos instructions, sur n'importe quelle partie, voir [plus bas](#redrafting-a-part). Pas pendant la modification du texte. |
| **Historique (n)** | Les [versions du texte](version-history.md) de la partie, avec leur nombre : les comparer, en remettre une, en nommer une. Pas pendant la modification du texte. |
| **Annoter la partie** | Crée une [note](annotations.md#making-an-annotation) sur la partie entière. Pour annoter des mots, sélectionnez-les et faites un clic droit (**Annoter la sélection**). |
| **Annoter avec l'IA** | Permet à un modèle [d'examiner la partie](annotations.md#letting-a-model-annotate) et de proposer des notes. Pas pendant la modification du texte. Pour plusieurs parties à la fois, utilisez **Annoter une plage de parties avec l'IA** dans le menu des paramètres (⚙), et pour chaque nouvelle partie **Annotation automatique** (voir [Annotations](annotations.md#annotating-every-new-part-automatically)). |
| **Créer une branche** | Démarre une nouvelle branche à partir de cette partie, voir [Branches et arbre de l'histoire](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Fusionner avec la partie précédente** | Ajoute le texte de cette partie à la partie qui la précède, voir [plus bas](#merging-a-part-into-the-previous-one). Pas sur la première partie. |
| **Afficher le prompt** | Affiche le prompt exact envoyé au modèle pour cette partie. |
| **Utilisation des jetons** | Montre la façon dont le contexte du modèle a été utilisé pour cette partie : un graphique des sections du prompt (instructions du modèle, lore, résumés, histoire jusqu'à présent, vos instructions, place réservée à la réponse) et la taille de chaque message envoyé au modèle. Si une partie de l'histoire n'a pas tenu, il indique combien de parties ont été laissées de côté. Les parties écrites avant que cela soit enregistré n'ont pas de graphique. |
| **Utilisation du lore** | Liste les entrées du [lorebook](../lorebooks/index.md) qui sont entrées dans le prompt de cette partie, avec les jetons que chacune a coûtés et ce qui l'a trouvée (le prompt, ou le contenu d'une autre entrée lorsque le lorebook explore plus en profondeur). Les parties saisies à la main, écrites avant que cela soit enregistré, ou écrites pour un livre sans lorebook indiquent qu'aucune information n'a été enregistrée. Pour l'ensemble du livre, utilisez **Utilisation du lore** dans le menu des paramètres (⚙), voir [plus bas](#which-lore-entries-were-used). |
| **Images** | Joint des images à la partie, voir [Images](#images). |
| **Générer un résumé** / **Voir le résumé** | Résume l'histoire jusqu'à cette partie, ou affiche le résumé, voir [Résumés](summaries.md). |
| **Supprimer le résumé** | Supprime le résumé de cette partie. Pour un [méta-résumé](summaries.md#meta-summaries), il demande s'il faut le défaire ou le supprimer complètement. |
| **Supprimer** | Supprime la partie, voir [plus bas](#deleting-a-part). |

*Régénérer* et *Variante* utilisent les détails du tour de la dernière partie (voir [plus bas](#turn-details)). Pour changer ce qui
doit se passer, modifiez-les d'abord.

![Le menu d'une partie, avec le sous-menu Inspecter ouvert](../../images/story-part-menu.png)

![Utilisation des jetons d'une partie](../../images/token-usage.png)

<a id="part-metadata"></a>

## Métadonnées de la partie

À côté de chaque partie :

| | |
|---|---|
| **Ordre n \| ID BD : m** | La position de la partie dans la branche, et son ID dans la base de données. |
| **Modèle** | Le fournisseur d'inférence qui a écrit la partie. |
| **Jetons : x (y)** | `x` jetons de texte dans la partie, `y` jetons dans le prompt qui l'a produite. |
| **Mots** | Mots dans la partie. |
| **Détails du tour** | Les instructions à partir desquelles la partie a été écrite. |

<a id="turn-details"></a>

### Détails du tour

**Détails du tour** ouvre le *Cadre de la scène*, le *Personnage du point de vue*, les *Personnages présents* et les *Instructions* à partir desquels la partie a été écrite. Pour la dernière partie, ils peuvent être modifiés (les changements sont enregistrés immédiatement) et sont utilisés par *Régénérer* et *Variante*. Pour les parties antérieures, ils sont en lecture seule.

![Détails du tour d'une partie](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Modifier le texte

Le texte généré vous appartient : vous pouvez le changer. Choisissez **Modifier** dans le menu de la partie, modifiez le texte, puis choisissez **Enregistrer**. Les parties modifiées sont envoyées au modèle telles que vous les avez modifiées.

Modifier une partie couverte par un [résumé](summaries.md) rend ce résumé périmé ; Marginalia le remarque à la génération suivante et supprime le résumé. Pour que cela n'arrive pas par surprise, **Enregistrer** vérifie d'abord le nouveau texte : lorsqu'il rendrait des résumés périmés, une fenêtre indique combien, et propose **Retour à l'édition** (rien n'est enregistré, l'éditeur reste ouvert avec votre texte), **Enregistrer quand même** (les résumés deviennent périmés) et **Enregistrer et revalider les résumés** (le texte est enregistré et les résumés restent valides, pour une modification qui ne change pas ce qu'ils disent, comme une coquille). La vérification se fait pour **Enregistrer** (le menu, le raccourci et le bouton de l'éditeur) ; les enregistrements automatiques, par exemple lorsque vous lancez une génération avec l'éditeur ouvert, ne demandent rien.

<a id="keyboard-shortcuts"></a>

## Raccourcis clavier

Sur un ordinateur (la [mise en page téléphone](../phone.md) n'en a pas), la boucle d'écriture fonctionne au clavier :

| Touche | |
|---|---|
| `+` | Ouvre le panneau *Instructions du nouveau tour* avec le curseur dans *Instructions*. |
| `Ctrl+Entrée` | Lance la génération depuis le panneau ouvert. `Échap` le ferme ; le clavier revient à l'histoire. |
| `Ctrl+.` | Arrête la génération en cours. |
| `s` / `r` | **Variante** / **Régénérer** la dernière partie. |
| `e` | Ouvre l'éditeur de la partie la plus visible à l'écran (la dernière partie s'il n'y en a pas). |
| `Ctrl+S` | Enregistre la partie en cours de modification (aussi lorsque le curseur est dans son texte). |
| `/` | Place le curseur dans la barre de commandes (l'onglet histoire est d'abord affiché). |
| `Ctrl+Maj+F` | Ouvre l'onglet *Plan* et place le curseur dans la recherche de la branche (`Entrée` / `Maj+Entrée` vont à la correspondance suivante / précédente). |
| `Ctrl+Maj+H` | Ouvre la fenêtre *Rechercher et remplacer* du livre. |
| `a` | **Annoter la sélection** : crée une note sur le texte sélectionné dans une partie. |
| `Alt+PageDown` / `Alt+PageUp` | Affiche l'onglet suivant / précédent, en parcourant les onglets de toutes les feuilles dans l'ordre de la mise en page, puis recommence. |

Une touche sans `Ctrl`, `Alt` ou `Meta` (comme `+`, `s`, `r`, `e`, `a`, `/`) ne fonctionne que lorsqu'aucun champ de texte n'a le focus, afin de ne jamais gêner la saisie. Les raccourcis fonctionnent dans l'onglet histoire du livre ouvert et non lorsqu'une autre fenêtre est ouverte par-dessus. Les boutons affichent leur touche dans l'info-bulle, et le menu de la dernière partie l'affiche après *Modifier*, *Régénérer* et *Variante*. Sur un Mac, `Cmd` fonctionne là où une touche indique `Ctrl` (sauf si vous avez attribué cette touche `Cmd` à une autre action). Vous pouvez modifier chaque touche dans [Paramètres → Raccourcis clavier](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Supprimer une partie

**Supprimer** retire la partie de l'histoire. Les parties qui la suivaient restent et continuent à partir de la partie qui la précède, donc
supprimer une partie au milieu de l'histoire la retire simplement. Supprimer la dernière partie fait de la partie qui la précède
la nouvelle fin de la branche.

<a id="deleting-many-parts-at-once"></a>

### Supprimer plusieurs parties à la fois

Le menu des paramètres (⚙) contient **Suppression en masse**. L'activer place une case à cocher à gauche de chaque partie et affiche
sous l'histoire une barre indiquant le nombre de parties cochées ainsi qu'un bouton **Supprimer**. Cochez les parties que vous voulez
retirer (les cases restent cochées pendant le défilement, car l'histoire ne charge qu'une fenêtre de parties) ; **Maj + clic** sur une case
coche (ou décoche) toutes les parties situées entre elle et celle sur laquelle vous aviez cliqué précédemment ; une partie cochée est teintée
en rouge. Appuyez ensuite sur **Supprimer** : après une confirmation, les parties sont supprimées une à une derrière une barre de progression
qu'il est impossible d'interrompre. Elles sont supprimées comme une seule partie l'est : les parties qui suivaient une partie supprimée
continuent à partir de la partie qui la précède, et les parties supprimées vont dans la corbeille. Ensuite, l'histoire est de nouveau affichée et la
suppression en masse est désactivée. **Annuler** dans la barre, ou choisir à nouveau **Suppression en masse**, la désactive sans rien supprimer. Lancer une
génération la désactive également. Une partie cochée sur laquelle une fenêtre ou une traduction travaille reste dans l'histoire, et Marginalia indique ce qui
la retient.

![Suppression en masse avec quelques parties cochées](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Refaire un passage

**Régénérer** et **Variante** ne travaillent que sur la dernière partie et l'écrivent entièrement à nouveau. **Réécrire** travaille sur n'importe quelle partie, même une ancienne au milieu de l'histoire, et conserve ce que vous ne demandez pas de changer. Choisissez **Réécrire** dans le menu de la partie :

1. Écrivez ce qui doit changer dans **Instructions pour la réécriture** : *ajoute plus de dialogue entre les deux*, *rallonge le combat*, *supprime l'explication de la carte*, *garde la fin telle quelle*. Des instructions vides réécrivent la partie avec les mêmes événements.
2. Choisissez **Longueur** si la partie doit devenir **Plus court** ou **Plus long** (la valeur par défaut est **Même longueur**) : une phrase qui le demande est ajoutée après vos instructions (voir [Prompts de réécriture plus courte et plus longue](prompts.md#redraft-shorter-and-longer)). Choisissez **Réécrire**. Le texte s'affiche au fur et à mesure dans **Texte réécrit** ; **Arrêter** l'interrompt. Choisissez **Réécrire à nouveau** pour un autre essai, avec d'autres instructions si vous le souhaitez. **Différences** montre ce qui a changé par rapport à la partie, mot à mot. Lorsque la partie comporte des [annotations](annotations.md) ouvertes, **Traiter les annotations ouvertes de la partie** (coché d'emblée) les transmet aussi au modèle, et **Appliquer** les marque comme résolues.
3. Le texte réécrit est un champ que vous pouvez encore modifier (son icône dans le coin le fait grandir pour tenir tout le texte), et **Différences** suit vos modifications. **Appliquer** remplace le texte de la partie par lui ; **Fermer** le jette. Rien dans le livre ne change avant **Appliquer**.

![La fenêtre de réécriture : les instructions, le nouveau texte et les différences](../../images/redraft-dialog.png)

Le modèle reçoit les [prompts de réécriture](prompts.md#redraft-prompt) : la partie, vos instructions, l'histoire qui précède la partie (l'histoire plus ancienne sous forme de [résumés](summaries.md) lorsqu'ils sont valides, le reste sous forme de texte, la partie la plus récente en premier lorsque tout ne tient pas), autant de l'histoire qui suit la partie qu'il en tient (jusqu'à un quart de la place disponible), le lore que les lorebooks donnent à cette partie maintenant (recalculé pour le tour de la partie, de sorte qu'une modification d'une entrée parvient à la réécriture ; le lore avec lequel la partie a été écrite n'est utilisé que si le livre n'a pas de lorebook), le point de vue, le temps et le style du livre, ainsi que le cadre de la scène, le personnage au point de vue, les personnages présents et les instructions du tour dont est issue la partie. L'histoire qui suit la partie est là pour que le nouveau texte mène toujours vers elle : les événements dont dépendent les parties suivantes doivent rester.

La partie conserve sa place, son résumé, ses images et son mode de génération ; le texte, ses mots et ses jetons sont remplacés, et la partie est comptée comme modifiée. Appliquer la réécriture est une modification comme **Enregistrer** dans l'éditeur : les [résumés](summaries.md) qui décrivent l'ancien texte deviennent donc périmés, et Marginalia indique combien. Un résumé porté par la partie elle-même en fait partie.

Lorsqu'un résumé décrit la partie (la partie en a un, ou une partie plus récente de la branche en a un), la fenêtre affiche la case **Revalider les résumés que cette réécriture rend périmés**, décochée d'emblée. Cochez-la lorsque les résumés disent toujours la bonne chose (la réécriture a changé la formulation, pas ce qui se passe) : **Appliquer** recalcule alors leur empreinte et ils restent valides. Laissez-la décochée lorsque les événements ont changé, et refaites les résumés.

La fenêtre ne bloque pas l'histoire, voir [Travailler à côté des fenêtres](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Travailler à côté des fenêtres de réécriture et d'annotation

Sur un ordinateur, les fenêtres **Réécrire** et **Annoter avec l'IA** se placent à droite de l'histoire et la laissent utilisable : vous pouvez lire, faire défiler et modifier l'histoire pendant que le modèle écrit. Faites glisser une fenêtre par son titre pour la déplacer, ou par son bord pour la redimensionner. La flèche (⌄) dans le titre replie une fenêtre dans le [panneau des exécutions](#the-panel-of-the-runs) en bas à gauche de la page : le modèle continue, **Ouvrir** (ou un clic sur l'entrée) rouvre la fenêtre, et **Arrêter** arrête le modèle (une fois terminé, **Fermer** ferme la fenêtre). Sur un téléphone, les fenêtres recouvrent toujours l'histoire, mais elles se replient de la même manière.

![La fenêtre de réécriture à côté de l'histoire pendant que le modèle écrit, et l'exécution dans le panneau en bas à gauche](../../images/redraft-beside-story.png)

**Fermer et Arrêter sont deux choses différentes.** **Arrêter** de la fenêtre arrête le modèle, et la fenêtre reste avec ce qui est arrivé. **Fermer** pendant que le modèle travaille n'arrête pas celui-ci : la fenêtre se replie dans le panneau et le modèle continue dans sa file ; sa réponse est dans la fenêtre lorsque vous la rouvrez. Fermez une fenêtre dont le modèle a fini, ou **Fermer** la dans le panneau, et elle disparaît ; fermer le livre ou l'onglet du navigateur arrête aussi une fenêtre repliée. Il en va de même pour les autres fenêtres qui attendent un modèle : un **résumé** (et *Créer des résumés*, *Réécrire tous les résumés*), le [Reviewer](../extensions/reviewer.md), la description du livre, les générateurs de lore et de plan, les générateurs d'images et de manga, et le rédacteur de personnages des Lecteurs bêta. Les fenêtres *Studio* et *Convertir* du livre audio ne bloquent pas non plus la page ; les fermer n'a jamais arrêté la conversion.

Tant qu'une fenêtre est ouverte (ou repliée) sur une partie, celle-ci affiche ce qu'elle fait à côté de son menu (*Réécriture · Le livre*), et **Régénérer**, **Variante**, **Réécrire**, **Annoter avec l'IA**, **Fusionner avec la précédente** et **Supprimer** sont désactivés pour cette partie, afin que la fenêtre ne perde pas sa partie. Modifier la partie reste possible. Fermer le livre ferme ses fenêtres et arrête leurs modèles. Une partie sur laquelle un [traducteur](../extensions/real-language-translator.md) travaille affiche *Traduction des répliques de la partie...* de la même façon, et deux traducteurs ne travaillent jamais sur une même partie en même temps : le second indique que la partie est utilisée.

- Lorsque vous **Appliquez** une réécriture de la partie entière et que la partie a été modifiée après l'ouverture de la fenêtre (vous l'avez éditée entre-temps), Marginalia demande d'abord confirmation : **Différences** compare alors la partie telle qu'elle est maintenant avec la réécriture, pour que vous voyiez ce qu'appliquer remplacerait ; choisissez **Appliquer quand même** ou **Annuler**. Une réécriture qui [s'applique d'elle-même](annotations.md#annotating-every-new-part-automatically) ne le fait pas lorsque la partie a été modifiée : la fenêtre attend alors votre décision.
- Si la partie a été supprimée avant que vous n'**Appliquiez** la réécriture ou n'ajoutiez les annotations, la fenêtre le signale, n'écrit rien et se ferme.

<a id="the-panel-of-the-runs"></a>

### Le panneau des exécutions

Tout ce qui travaille sur vos livres avec un modèle en ce moment est listé en bas à gauche de chaque page : une génération, une réécriture ou une annotation, une traduction par le traducteur [Real Language](../extensions/real-language-translator.md) ou [Conlang](../extensions/conlang-translator.md), une réécriture des résumés, la lecture de l'histoire par le Knowledge Tracker, une conversion [Livre audio](../extensions/audiobook.md), une commande de la barre de commandes qui prend du temps (*Rédiger des instructions*, un résumé... : sa fenêtre se place sur le côté et se replie comme les autres). Les fenêtres dans lesquelles vous attendez une réponse y figurent aussi tant que le modèle répond : **Imiter**, l'assistant d'aide, une traduction rapide, le concepteur de conlang, une conversation de Side Query, les Lecteurs bêta ou l'assistant d'intrigue. Il liste vos exécutions de **toutes vos pages ouvertes** (un autre onglet, le téléphone), et pas seulement celles lancées sur cette page. Rien en cours, rien d'affiché.

![Le panneau des exécutions ouvert : la réécriture repliée dedans, avec la fin du texte du modèle, Ouvrir et Arrêter](../../images/runs-panel.png)

- **Fermé**, il se présente comme une petite barre : la première exécution, combien d'autres (*Traduction des répliques de la partie... · 2 de plus*) et une barre mobile tant qu'un modèle travaille. Cliquez dessus pour ouvrir la liste ; elle s'ouvre toute seule lorsque vous repliez une fenêtre dedans.
- **Ouvert**, chaque exécution indique ce qu'elle fait, le livre, sa durée, ce qu'elle attend et la fin de ce que le modèle écrit :
  - *En attente du modèle : 2 requêtes avant celle-ci* lorsque le modèle est occupé par d'autres requêtes (voir *Connexions* dans [Fournisseurs d'inférence](../inference-providers.md)) ;
  - *Le modèle réfléchit...* ou *Le modèle écrit...*, avec la fin de sa réflexion ou de sa réponse en dessous ;
  - *Vous attend* lorsque l'exécution attend une décision de votre part (une réécriture à appliquer, une proposition du Knowledge Tracker à examiner, la question de l'Inspecteur de prompt avant une génération, une commande de l'assistant d'intrigue à autoriser).
- **Arrêter** arrête l'exécution, où qu'elle ait été lancée. **Ouvrir**, ou un clic sur l'entrée, réaffiche une fenêtre repliée (uniquement sur la page où elle a été ouverte) ou l'exécution elle-même ; le bouton est **Examiner** lorsque l'exécution attend votre décision. Une exécution du Knowledge Tracker ouvre son livre sur cette page (lorsqu'aucun autre livre n'est ouvert ici ; sinon, il vous est demandé de fermer celui-ci d'abord), passe à l'onglet Connaissances et affiche la revue. **Fermer** ferme définitivement une fenêtre (et arrête son modèle).
- Sur un ordinateur, vous pouvez déplacer le panneau par sa poignée (⋮ à sa gauche) ; il reste où vous le laissez dans ce navigateur. Dans la moitié inférieure de la page, il s'agrandit vers le haut lorsque vous l'ouvrez, comme dans son coin, et il reste toujours à l'intérieur de la page. Lorsqu'il se trouve à l'endroit du bouton rond de l'[assistant d'aide](../administration/help-assistant.md) (sur un livre, les deux sont dans le coin inférieur gauche), le bouton s'écarte et revient lorsque la place est libre.

Lorsqu'une exécution que vous avez lancée se termine après la fermeture de sa page, une autre de vos pages ouvertes envoie sa [notification](../account.md#notifications) (pour les exécutions qui en ont une, comme la réécriture des résumés).

<a id="redrafting-only-a-passage"></a>

### Réécrire seulement un passage

Pour réécrire quelques phrases et laisser le reste de la partie intact, sélectionnez-les dans le texte de la partie puis faites un **clic droit** (un appui prolongé sur un téléphone) : le menu propose **Réécrire la sélection**. (Le menu ☰ de la partie n'est pas proposé pour cela, car son ouverture peut faire perdre la sélection dans certains navigateurs.) La fenêtre fonctionne comme ci-dessus avec deux différences : elle affiche le **Texte sélectionné**, et la réécriture ne consiste qu'en le texte qui remplace la sélection. Le modèle reçoit la partie entière avec la sélection marquée, ainsi que l'histoire et le lore qui l'entourent, pour que le nouveau passage se raccorde aux phrases qui précèdent et suivent. **Différences** compare la sélection avec le texte réécrit ; **Appliquer** place le texte réécrit à la place de la sélection et conserve le reste de la partie, y compris l'espace ou le saut de ligne aux bords de la sélection. Lorsque des [annotations](annotations.md) sont ouvertes, la case à cocher ne propose que celles dont le passage chevauche la sélection. Si la partie a été modifiée après que vous avez sélectionné le texte (dans une autre fenêtre), **Appliquer** refuse et vous devez resélectionner. Pendant que la partie est en cours d'édition, c'est la sélection dans l'éditeur qui est utilisée, et la modification est enregistrée au préalable.

<a id="what-follows-a-generated-part"></a>

### Ce qui suit une partie générée

Lorsqu'une partie que vous avez générée (depuis la page ou la barre de commandes) est terminée, le livre peut en faire plus de lui-même, dans cet ordre :

1. **Annoter** : l'[annotation automatique](annotations.md#annotating-every-new-part-automatically) examine la partie.
2. **Réécrire** : la partie est réécrite pour tenir compte des remarques.
3. **Traduire** : les traducteurs [Real language](../extensions/real-language-translator.md) et [Conlang](../extensions/conlang-translator.md) traduisent ses répliques.
4. **Connaissances** : le [Knowledge Tracker](../extensions/knowledge-tracker.md) la lit.
5. **Audio** : le [Livre audio](../extensions/audiobook.md) en fait un enregistrement sonore.

Seules les étapes que le livre a activées s'exécutent. Elles tournent sur le serveur, de sorte que la page peut être fermée, et elles forment une seule entrée dans le [panneau des exécutions](#the-panel-of-the-runs) : *Après la partie 14 : Annoter ✓ · Réécrire … · Traduire*, avec le texte du modèle et un seul **Arrêter**. Ce qui modifie le texte passe en premier, pour que le suivi et l'audio reçoivent le texte final. Une étape qui attend votre décision (des remarques ou une réécriture à choisir) ouvre sa fenêtre, et l'entrée propose **Examiner** pour l'ouvrir dans une autre page.

- **Arrêter**, la fermeture d'une fenêtre qui attend votre décision, ou l'échec d'une étape y met fin : ce qui a été fait reste, le reste n'est pas exécuté, et un message indique ce qui reste à faire à la main (chaque étape a sa propre action sur la partie).
- Pendant que la **réécriture** tourne, la partie suivante ne peut pas être générée (on vous indique par quelle exécution) ; pendant que les autres étapes tournent, elle le peut.
- La partie ne peut pas être supprimée tant que son texte n'est pas définitif ; la générer à nouveau met fin à ce qui suivait sa version précédente.
- Un [agent](../agents.md) exécute ces étapes par ses propres commandes : rien ne suit une partie générée par un agent.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionner une partie avec la partie précédente

Un modèle qui s'est arrêté au milieu d'une phrase (la limite de réponse, une connexion coupée) ne peut pas être prié de
« continuer » le même texte de façon fiable. Écrivez plutôt la partie suivante (*continue là où la dernière partie s'est
arrêtée*) et joignez les deux avec **Fusionner avec la partie précédente** dans le menu de la seconde. Une revue s'ouvre :

- **Joindre les textes avec** : une ligne vide, un saut de ligne, une espace ou rien. Ce choix est présélectionné d'après la fin du texte précédent : un paragraphe après un texte qui se termine par une phrase, une espace après un texte coupé.
- La **mise en scène**, le **personnage POV**, les **personnages présents** et les **instructions** des deux tours, réunis dans des champs que vous pouvez modifier. Ce que les deux tours ont en commun au début n'est gardé qu'une fois (« Écris le duel. Reste bref » et « Écris le duel. Ajoute de la pluie » donnent la première ligne, puis « Ajoute de la pluie » sur la ligne suivante).
- **Données** : ce que les deux parties conservent, valeur par valeur (par exemple les [variables](../templates/macros.md) de l'histoire et les données des extensions), avec l'indication de savoir si la valeur est la même, différente ou présente sur une seule des parties. La partie fusionnée garde la valeur de la **partie la plus récente** (c'est elle qui représente l'état actuel de l'histoire), sauf si vous choisissez pour elle la valeur de la partie précédente, ou si vous la retirez. Les valeurs identiques sont masquées, sauf si vous les demandez.

![La revue avant qu'une partie soit fusionnée avec la partie précédente](../../images/part-merge-dialog.png)

La partie précédente conserve sa place, son résumé et la façon dont elle a été générée (prompt, modèle, durées) ; elle reçoit le texte joint, le raisonnement joint et les images, et ses mots et jetons sont recomptés. La partie fusionnée est supprimée (elle se trouve dans la [corbeille](../trash.md)) et ce qui la suivait se poursuit à partir de la partie précédente. Les résumés qui couvrent les deux parties deviennent obsolètes, comme après une modification. L'[historique](version-history.md#backups-copies-and-the-trash) de la partie fusionnée est supprimé, et la partie conservée a le texte joint comme version **Fusionnée**. Une partie ne peut pas être fusionnée si c'est la première partie du livre, ou si la partie qui la précède a d'autres suites (variantes de la partie) : supprimez-les ou transformez-les d'abord en branche.

## Images

Une partie peut contenir des images : une carte, le portrait d'un personnage, une illustration. Choisissez **Images** dans le menu de la partie pour ouvrir la liste des images de la partie.

![La boîte de dialogue Images d'une partie](../../images/images-dialog.png)

- **Ajouter une image** (ou déposer un fichier dessus) envoie une image PNG, JPEG, GIF ou WebP d'au plus 10 Mo. Les images WebP sont converties en PNG lors de l'envoi, car les fichiers Word et PDF ne peuvent pas contenir de WebP.
- **Légende** s'affiche sous l'image. Elle peut être vide.
- **Position dans la partie** est une grille 3x3. La colonne est le côté du texte (gauche, centre, droite), la ligne est l'emplacement dans celui-ci : le haut se place avant le texte, le milieu au milieu du texte, le bas après le texte. Cliquez sur une case pour la choisir.
- **Le texte entoure l'image** permet au texte de contourner l'image au lieu de se placer au-dessus et au-dessous d'elle. À gauche ou à droite, le texte court à côté de l'image. Dans la colonne du milieu, le texte se trouve des deux côtés : quelques blocs du texte (environ 400 caractères, deux blocs au minimum) à cette ligne deviennent deux colonnes, la première partie à gauche de l'image et le reste à droite, si bien que le texte se lit dans la colonne de gauche puis continue dans celle de droite. Deux images au même endroit se placent l'une au-dessus de l'autre, entre les colonnes. Quand le texte n'a pas la place pour deux colonnes (un seul paragraphe, ou des blocs déjà pris par la bande au-dessus), l'image devient un bloc à part, centré.
- **Largeur** est la largeur de l'image en pourcentage de la largeur du texte. **Automatique** conserve la taille de l'image (jamais plus large que le texte ; une image avec du texte des deux côtés fait 30 %). Une image que le texte contourne fait au plus 60 %, et une image avec du texte des deux côtés au plus 40 %, afin que le texte garde un peu de place. La hauteur suit les proportions de l'image.
- Le bouton corbeille retire l'image de la partie.

![La position d'une image : au milieu, le texte la contournant](../../images/images-position.png)

**Aperçu comme dans le lecteur** (un bouton de la boîte de dialogue, et la même entrée dans le menu de la partie lorsque celle-ci contient des images) affiche la partie comme la [visionneuse](publishing.md) la montre, avec le style de texte du livre : les images là où vous les avez placées, et un bouton pour un écran large ou étroit (téléphone). Il utilise le texte de l'éditeur tel qu'il est maintenant, enregistré ou non.

![L'aperçu : le texte en deux colonnes avec l'image entre elles](../../images/images-preview.png)

L'éditeur de l'histoire n'utilise pas la position : il affiche les images sous le texte de la partie, sous forme de liste. La position est utilisée par la visionneuse et par les [exports](export.md#images).

L'endroit du texte où se place une image ne dépend pas de nombres enregistrés avec elle : le texte est coupé entre ses blocs (paragraphes, listes, citations et blocs de code ne sont jamais coupés), si bien que les images restent à leur place lorsque vous modifiez le texte. Le milieu est l'endroit entre blocs le plus proche du milieu du texte ; une image que le texte contourne en bas est placée avant le dernier bloc, afin qu'au moins ce bloc coure à côté d'elle. Les images au même endroit gardent l'ordre de la liste ; celles qui se suivent au même endroit (et ne laissent pas le texte les contourner) se rangent côte à côte lorsqu'elles tiennent. Le PDF et Word font de même pour les images qui ont une largeur.

Les images ne font **pas partie du texte**. Le modèle ne les voit jamais, elles ne figurent pas dans le prompt, elles ne comptent pas comme jetons et elles ne modifient pas les [résumés](summaries.md). Ce ne sont pas non plus du Markdown : un lien `![](...)` écrit dans le texte n'est pas une image de la partie, utilisez plutôt **Images**.

Bon à savoir :

- **Régénérer** conserve les images de la partie, même lorsque la génération échoue. **Variante** écrit une nouvelle partie (une nouvelle version à côté de l'ancienne), qui démarre sans images. **Créer une branche** copie les images dans la nouvelle partie.
- Les images appartiennent à votre compte. Dans la [visionneuse](publishing.md), les lecteurs d'un livre publié les voient eux aussi, sauf si vous décochez **Montrer les images aux lecteurs** dans l'onglet *À propos* du livre.
- Une [sauvegarde](backups.md) mémorise les images qu'a une partie, mais pas les fichiers d'image. Restaurer une sauvegarde dans un autre compte ou une autre installation donne des parties sans leurs images.
- Retirer une image d'une partie ne fait que la détacher de la partie. Le fichier reste et figure toujours dans [Ressources](../resources.md), où vous pouvez le télécharger, le remplacer ou le supprimer. La supprimer là-bas la retire de toutes les parties qui l'utilisent. Le fichier lui-même reste dans le dossier de données.

<a id="summaries"></a>

## Résumés

Le bouton **Résumés** de la barre du bas (à gauche du menu des réglages ⚙) ouvre la vue d'ensemble des résumés de la branche, où ils peuvent être modifiés, supprimés et fusionnés en méta-résumés. L'infobulle du bouton indique combien de jetons les résumés utilisés occupent dans le prompt. Voir [Résumés](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Quelle part du contexte prend l'histoire

Le menu des réglages (⚙) contient **Jetons de la branche**. Il montre, avant toute génération, la part du prompt que prend l'histoire de la branche active : une barre pour l'histoire **sans résumés** (toutes les parties) et **avec résumés** (les résumés et les parties postérieures au plus récent), ainsi qu'un tableau des parties, des jetons et de la part du **budget du prompt** (le contexte du modèle du livre moins la place réservée à la réponse). Seuls les résumés qui décrivent encore l'histoire comptent ; ceux qui ne correspondent plus sont nommés, et ils sont écartés lorsqu'une partie est générée. La lore, le modèle système et les instructions du tour prennent de la place sur ce même budget. Sans modèle ayant une taille de contexte, le tableau n'affiche pas de parts.

![Jetons de la branche](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Quelles entrées du lore ont été utilisées

**Utilisation du lore** dans le menu des paramètres (⚙, **Analyse**) indique quelles entrées du lorebook du livre ont été utilisées par les générations, et lesquelles ne l'ont jamais été,
afin de pouvoir élaguer un lorebook selon ce qu'il fait réellement et non au hasard. Chaque génération enregistre les entrées qui sont entrées dans son prompt
(ainsi que les tokens de chacune) ; la fenêtre additionne ces données sur l'ensemble des parties :

- **Parties** choisit ce qui est compté : la **Branche active**, ou **Toutes les branches**, c'est-à-dire chaque branche et chaque variante du livre.
- **Depuis la partie** et **Jusqu'à la partie** limitent la plage, numérotée comme dans le plan (la première partie d'une branche est la 1 ; dans toutes les branches, le numéro correspond à la profondeur de la partie dans l'arbre) ; laissez-les vides pour le début et la fin.
- **Afficher** conserve les entrées qui étaient **Jamais utilisée**, **Parfois utilisée** ou **Toujours utilisée** dans les parties comptées.

![La fenêtre Utilisation du lore](../../images/lore-usage.png)

Le tableau comporte une ligne pour chaque entrée des lorebooks du livre, la plus utilisée en premier : son lorebook, si elle a été jamais, parfois ou toujours
utilisée, dans combien de parties (parmi les parties qui ont un enregistrement, avec le pourcentage), les tokens qu'elle a coûtés dans l'ensemble de ces parties et par utilisation, ainsi que la première et la
dernière partie qui l'ont utilisée. Cliquez sur un en-tête pour trier. Une entrée qui a été utilisée auparavant mais qui n'est plus dans les lorebooks du livre (supprimée, déplacée, ou
dont le lorebook est désactivé ou n'appartient plus au livre) reste dans la liste, marquée *n'est plus dans le lorebook* ; une entrée supprimée n'a plus de nom.

Seules les parties qui ont un enregistrement sont comptées : une partie saisie à la main, écrite avant que Marginalia ne conserve cet enregistrement, ou écrite alors que le livre n'avait aucun lorebook n'en a pas, et la fenêtre indique combien il y en a. Les entrées *Toujours utilisée* peuvent être des entrées constantes ; les entrées *Jamais utilisée* sont des candidates à la suppression ou à un meilleur filtre. [**Aperçu de l'activation**](../lorebooks/entries.md#preview-activation) dans l'éditeur de lorebook montre pourquoi une entrée ne se déclenche pas actuellement.

<a id="text-styles"></a>

## Styles de texte

Le menu des paramètres (⚙) dans la barre du bas contient **Apparence → Changer les styles du texte**, un menu avec une entrée par style, triées par nom.
Pour savoir comment créer un style, voir le [guide de mise en forme](styling-guide.md).
Un style détermine l'apparence du texte des parties : police, taille, couleurs, alignement, titres... L'application fournit deux
styles qui ne peuvent pas être modifiés : **Normal** (l'apparence de l'application) et **Book** (un style de lecture proche du livre, celui qu'utilise la [visionneuse](publishing.md)). Vous pouvez en créer d'autres dans [Paramètres → Styles](../account.md#styles).

- La première entrée, **Default (...)**, suit votre *style par défaut* (celui marqué dans *Paramètres → Styles*, Normal si vous n'en avez choisi aucun). Un nouveau livre n'a pas de style propre et affiche celui-ci.
- Choisir une autre entrée enregistre le style **dans le livre**, ce qui remplace le style par défaut pour ce seul livre ; l'entrée cochée est celle que le livre utilise actuellement. **Default** annule ce choix.
- Un style que vous supprimez revient à votre style par défaut dans les livres qui l'utilisaient.

![Apparence, Changer les styles du texte : les styles au choix](../../images/story-style-menu.png)

Dans le style **Book**, les paragraphes sont justifiés et les mots longs sont coupés automatiquement, selon le dictionnaire de la
**Langue** du livre (définie dans l'onglet [À propos](managing-books.md#the-about-tab)). La coupure des mots est faite par le navigateur ;
pour quelques langues rares, il n'existe pas de dictionnaire et le texte n'est tout simplement pas coupé.

Les livres qui avaient l'ancien interrupteur *Changer les styles du texte* activé continuent d'afficher le style Book tant que vous ne choisissez pas une autre entrée.

<a id="highlighting-generated-text"></a>

## Surligner le texte généré

Le menu des paramètres (⚙) comporte aussi l'option cochable **Surligner le texte généré**. Lorsqu'elle est activée, les mots écrits par un modèle sont surlignés dans chaque partie, afin que ce que vous avez ajouté ou réécrit se distingue. Marginalia suit le texte à travers les [versions de la partie](version-history.md) : chaque version est superposée à la précédente, et les mots qu'elles ont en commun gardent leur auteur. Ainsi, une phrase que vous avez tapée dans un texte généré reste la vôtre, quel que soit le nombre de fois où le modèle réécrit le reste. Une partie que vous n'avez jamais modifiée est entièrement surlignée ; une partie que vous avez écrite vous-même n'a rien de surligné. Ce qu'un agent a écrit compte comme celui du modèle. L'option n'est pas enregistrée : elle se désactive à nouveau lorsque la page est rouverte.

Une partie dont [l'historique est désactivé](version-history.md) n'a pas de versions à suivre : elle est comparée au texte que le modèle a généré en premier. Le surlignage fonctionne aussi sur le [téléphone](../phone.md).

Le même comptage donne le ratio : l'onglet **Infos** du livre contient **Paternité de la branche** avec un bouton **Calculer**, qui indique quel pourcentage des mots de la branche active vous avez écrits et combien ont été générés par un modèle, et [l'export](export.md) peut terminer le fichier par une déclaration à ce sujet.

![Texte généré surligné et menu des paramètres](../../images/highlight-generated.png)

<a id="commands"></a>

## Commandes

Juste à côté du menu situé sous le récit se trouve une **ligne de commande** : tapez `/` pour voir les commandes, choisissez-en une et appuyez sur **Enter** (ou sur le bouton de lecture) pour l'exécuter. Ce que renvoie une commande s'affiche dans une petite fenêtre au-dessus de la ligne. Voir [Commandes](../commands/index.md) pour savoir comment écrire les arguments, ce qu'affiche l'aide et comment arrêter une commande qui prend du temps. La ligne de commande n'est pour l'instant disponible que dans l'onglet récit de la disposition pour ordinateur.
<a id="search-and-replace"></a>

## Recherche et remplacement

Le menu des réglages (⚙) propose aussi **Effacer l'historique du livre** et **Vider la corbeille de l'historique**, voir [Historique des versions](version-history.md#deleting-versions).

Le menu des réglages (⚙) propose aussi **Rechercher et remplacer**, qui modifie un mot, un nom ou une expression dans toute
l'histoire en une seule fois. La fenêtre comporte un champ **Rechercher** et un champ **Remplacer par**, ainsi que ces options :

![Le menu des réglages avec Rechercher et remplacer](../../images/story-settings-menu.png)

![La fenêtre Rechercher et remplacer avec une recherche saisie](../../images/search-replace-dialog.png)

| Option | Par défaut | Signification |
|---|---|---|
| **Rechercher par expression régulière** | désactivée | Le texte recherché est une [expression régulière](../regular-expressions.md) et le remplacement peut utiliser ses groupes (`$1`). Sinon, le texte est recherché tel qu'il est saisi. |
| **Respecter la casse** | activée | Un texte n'est trouvé que dans la casse saisie (*John* ne trouve pas *john*). Désactivée : la casse n'a pas d'importance. Verrouillée quand **Rechercher par expression régulière** est activée. |
| **Rechercher dans les résumés** | activée | Les [résumés](summaries.md) sont aussi recherchés et modifiés. |
| **Rechercher dans toutes les branches** | désactivée | Toutes les [branches](branches-and-story-tree.md) sont recherchées, sinon seulement la branche active. |
| **Revalider tous les résumés** | activée | Après le remplacement, chaque [résumé](summaries.md) du livre est de nouveau valide, voir ci-dessous. |

Avec une expression régulière, la casse est toujours prise telle qu'elle est saisie ; commencez-la par `(?i)` pour l'ignorer. Appuyez sur **Rechercher**
(le bouton à droite des champs ; sur le téléphone, sous eux). La barre de progression indique l'avancement et **Arrêter**
à côté l'interrompt. Une recherche ne modifie rien.

Le tableau liste chaque partie de l'histoire et chaque résumé qui contient une correspondance, avec leur type et leur identifiant. Ouvrez une ligne pour voir les
correspondances qu'elle contient : le texte autour de la correspondance tel qu'il est, et tel qu'il sera après le remplacement. Une correspondance ou un remplacement trop long pour être affiché est écrit *[texte recherché]* ou *[texte de remplacement]*.

![Les correspondances d'une recherche, la première partie ouverte](../../images/search-replace-found.png)

Appuyer sur **Entrée** dans le champ **Rechercher** ou **Remplacer par** a le même effet que **Rechercher**.

Une fois la recherche terminée, les champs et les options sont verrouillés et le bouton **Remplacer** est activé. **Rechercher à nouveau** oublie
ce qui a été trouvé et déverrouille la fenêtre, pour que vous puissiez modifier la recherche. **Remplacer** modifie l'histoire : la fenêtre est entièrement gelée
(même **Arrêter** ne fonctionne pas) jusqu'à la fin, puis elle est de nouveau vide. Les parties remplacées sont marquées comme
modifiées, comme si vous les aviez modifiées vous-même.

Un remplacement dans le texte qu'un [résumé](summaries.md#when-summaries-become-outdated) décrit rendrait ce résumé périmé.
Avec **Revalider tous les résumés** activée, Marginalia recalcule la valeur de contrôle de chaque résumé du livre (ceux de
toutes les branches, ceux que le remplacement n'a pas touchés, et ceux qui étaient déjà périmés), de sorte qu'ils soient tous valides
ensuite. Cela rend aussi valide un résumé périmé sans que son texte soit relu ; utilisez donc cette option quand vous savez que les résumés disent toujours la bonne chose.

Avec l'option désactivée, seuls les résumés couverts par la recherche sont mis à jour. Ceux qu'elle ne couvre pas (résumés des
branches non recherchées, qui partagent leurs parties antérieures avec la branche active ; ou tous les résumés quand ils ne sont pas
recherchés) deviennent périmés. S'il y en a, Marginalia indique combien et demande s'il faut continuer. Un
résumé déjà périmé le reste.

![L'avertissement sur les résumés qui deviendraient périmés](../../images/search-replace-warning.png)

Dans la [disposition téléphone](../phone.md), la même fenêtre place les champs et les boutons les uns sous les autres, et le tableau a une
colonne qui affiche le type et l'identifiant, et sous eux la correspondance et le remplacement.

La [vue d'ensemble des résumés](summaries.md#search-and-replace-in-the-summaries) propose la même fenêtre, pour les résumés seulement.

La même recherche et le même remplacement peuvent être lancés depuis la [ligne de commande](../commands/index.md#commands-that-come-with-marginalia)
avec `/search-replace-parts` ; la commande demande confirmation avant de remplacer.

<a id="importing-into-the-book"></a>

## Importer dans le livre

Le menu des réglages (⚙) propose **Importer** (sous **Import et export**). C'est la fenêtre de l'onglet Livres qui crée un nouveau livre à partir d'un fichier (voir [Gérer les livres](managing-books.md#creating-a-book)), pour le livre dans lequel vous vous trouvez : les parties sont alors **ajoutées à l'histoire**. La **Source** est l'une des suivantes : l'archive zip de fichiers `.md`, un document (Word, LibreOffice, RTF, Markdown, texte brut, EPUB), le zip d'un coffre Obsidian ou le zip d'un projet Scrivener ; chacune propose le même texte et les mêmes options que là-bas (le nombre de mots par partie et le niveau de titre des chapitres), et une ligne indique ce que l'import va produire. Les conversations de SillyTavern et de LM Studio ne sont pas proposées : ce sont un arbre de branches, qui n'a pas sa place au milieu d'une histoire.

**Placer les parties** indique où elles vont :

- **À la fin de l'histoire** : après la dernière partie de la branche active (la première partie d'un livre vide).
- **Avant une partie** ou **Après une partie** : choisissez la partie dans la liste (les numéros sont les emplacements dans la branche active, avec le début du texte). Les parties sont insérées entre deux parties : tout ce qui suivait l'emplacement, toutes ses branches, suit les nouvelles parties, et aucune partie n'est modifiée ni générée à nouveau. La fin de l'histoire reste où elle était (l'histoire compte désormais autant de parties en plus). Les résumés qui couvrent cet emplacement deviennent périmés, car ils décrivent l'histoire sans les nouvelles parties : la fenêtre des [résumés](summaries.md) les affiche et les refait.

![La fenêtre d'import d'un livre](../images/book-import-into.png)

Les parties d'une archive sont les fichiers dans l'ordre de leurs noms, les nombres à l'intérieur des noms étant comparés comme des nombres (`2.md` avant `10.md`), y compris dans les dossiers de l'archive. Les dossiers, les fichiers cachés (`.nom`), le dossier `__MACOSX` et les fichiers qui ne sont pas `.md` sont ignorés. Le fichier est le texte de la partie tel quel ; il n'est pas envoyé au modèle ni modifié, et toutes les parties comptent comme écrites par vous.

L'import affiche une barre de progression avec le nombre de parties traitées. **Arrêter** met fin à l'import après la partie en cours d'enregistrement : les parties déjà ajoutées restent dans l'histoire (l'import n'est pas annulé), et un message indique combien ont été importées.

<a id="after-an-import"></a>

### Après un import

Quand les parties sont dans le livre, une fenêtre **Import terminé** propose les deux opérations qui facilitent l'écriture d'une longue histoire (un livre qui vient d'être importé comme nouveau est ouvert en premier, et la fenêtre s'affiche par-dessus). Elles sont aussi dans le menu des réglages (⚙, **Analyse**), où vous pouvez les faire plus tard ou les refaire :

- **Créer les résumés...** écrit les résumés de ce qui a été importé, voir [Créer des résumés](summaries.md#create-summaries). Faites-le d'abord : un agent verrouille le livre pendant qu'il travaille.
- **Prendre des notes de lore avec un agent...** lance un [agent](../agents.md) qui lit l'histoire et écrit les personnages, lieux, objets et termes qu'il trouve sous forme d'entrées du lorebook du livre, voir [Notes de lore](../agents.md#lore-notes-of-an-imported-book).

**Pas maintenant** ferme la fenêtre sans faire ni l'un ni l'autre.

![La fenêtre après un import](../../images/import-finished.png)

Le même import dans un lorebook est décrit dans [Import et export](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exporter l'histoire

Le menu des réglages (⚙) propose aussi **Exporter l'histoire**, qui enregistre la branche active dans un fichier texte, Markdown, HTML, Word, PDF ou EPUB. Voir [Exporter un livre](export.md).

<a id="what-the-model-gets"></a>

## Ce que reçoit le modèle

Pour chaque nouvelle partie, Marginalia envoie :

1. Le **modèle maître** comme message système : rôle, entrées du [lorebook](../lorebooks/index.md), point de vue,
   temps verbal, style et [résumés](summaries.md). Le prompt de contournement du
   [fournisseur d'inférence](../inference-providers.md#general-settings), s'il est activé, passe en premier.
2. Les parties précédentes de la branche active, comme les propres réponses antérieures du modèle, autant qu'il en tient dans la
   [limite de contexte](../protocols.md#limits), la plus récente en premier. Les parties plus anciennes qui ne tiennent pas sont laissées de côté (sauf si un
   [résumé](summaries.md) les couvre).
3. Le **prompt utilisateur** avec les instructions pour la nouvelle partie.

*Afficher le prompt* sur n'importe quelle partie montre exactement ce qui a été envoyé. Voir [Modèles de prompt du livre](prompts.md) pour les modèles.
