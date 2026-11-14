---
label: Éditeur d'histoire
order: 90
---

# Éditeur d'histoire

L'onglet **Histoire** d'un livre est l'endroit où vous écrivez. Il affiche les parties de la [branche active](branches-and-story-tree.md)
du début à la fin.

![L'éditeur d'histoire](../../images/hero-story-editor.png)

Cette page décrit la disposition sur ordinateur. Sur téléphone, l'histoire tient dans une seule colonne et le plan est dans un tiroir, voir
[Utiliser Marginalia sur un téléphone](../phone.md#the-story).

| Zone | |
|---|---|
| **Plan** (à gauche) | Une entrée par partie, `#1`, `#2`... avec l'identifiant de la partie dans la base. Cliquez sur une entrée pour faire défiler jusqu'à la partie (les parties qui la précèdent sont chargées d'abord si elle ne l'est pas encore). L'en-tête indique combien de parties compte la branche. Une partie qui a un [résumé](summaries.md) porte une petite icône de document après son entrée. L'extension [Chapter Marker](../extensions/chapter-marker.md) affiche ici les titres de chapitres. |
| **Lorebook** (à gauche, à côté du Plan) | Une vue du savoir du livre, avec une zone de recherche et une modification rapide du texte d'une entrée, voir [L'onglet lorebook](#the-lorebook-tab). |
| **Annotations** (à gauche, à côté du Plan) | Les [notes](annotations.md) sur des passages de l'histoire, pour l'ensemble du livre. |
| **Planificateur** (à gauche, à côté du Plan) | Les scènes que vous prévoyez d'écrire, une liste que vous remplissez à la main, voir [L'onglet planificateur](#the-planner-tab). |
| **Histoire** (au centre) | Le texte de chaque partie, avec son menu (☰) et ses métadonnées à droite, et en dessous la barre du bas. |
| **Barre du bas** | Le bouton **Résumés** (icône de liste) et le menu des paramètres (⚙) à gauche, la ligne de commande, et le bouton **+** à droite. |

Voici à quoi ressemble un nouveau livre. Tous ces éléments sont des onglets que vous pouvez disposer comme vous le souhaitez, l'histoire comprise, voir
[Disposer les onglets](#arranging-the-tabs). Le séparateur entre la barre latérale et l'histoire peut être déplacé, et Marginalia
retient où vous l'avez laissé (enregistré avec le livre) et y revient à l'ouverture du livre suivante.

L'histoire s'ouvre toujours en bas, à la partie la plus récente. Un livre long n'est pas affiché entièrement d'un coup : seules les dernières parties sont
chargées, et les précédentes s'ajoutent au-dessus à mesure que vous remontez, sans déplacer la partie que vous lisez. Les parties à l'autre
extrémité sont retirées, de sorte que pas plus que le nombre fixé par le paramètre **Parties chargées à la fois dans l'histoire** de votre
[compte](../account.md) ne reste chargé (vide les charge toutes). Si vous faites défiler plus vite que les parties n'arrivent, une marque *Chargement des parties
précédentes* (ou *suivantes*) au bord indique qu'il en reste. Cliquer sur une partie du plan, ou en trouver une avec la recherche, charge les parties
qui l'entourent, et commencer une nouvelle partie vous ramène à la fin de l'histoire.

<a id="searching-the-story"></a>

### Rechercher dans l'histoire

Au-dessus du plan se trouve un champ de recherche pour le texte de la **branche sur laquelle vous êtes** (la recherche de l'arbre de l'histoire
parcourt toutes les branches). Comme un livre long n'est pas chargé d'un coup, la recherche du navigateur (Ctrl+F) ne peut pas trouver de texte
dans les parties non chargées ; celle-ci, si.

- Tapez ce que vous cherchez ; la recherche démarre lorsque vous cessez de taper. Plusieurs mots doivent tous être dans la même partie, une expression
  se met entre guillemets doubles (`"old key"`) et `*` représente n'importe quels caractères (`light*house`). La casse n'a pas d'importance.
- Chaque endroit où un mot se trouve est une correspondance, et elles sont numérotées sur l'ensemble de la branche. Sous le champ, vous voyez
  *Correspondance 3 sur 27, dans la partie n° 5* et une barre indiquant où vous en êtes. L'histoire défile jusqu'à la correspondance et charge les parties
  qui l'entourent lorsqu'elles ne sont pas chargées.
- Les boutons **haut** et **bas** vont à la correspondance précédente et à la suivante (après la dernière, on revient à la première).
  **Entrée** dans le champ fait la même chose que le bouton bas, **Maj+Entrée** comme le bouton haut.
- Toutes les correspondances des parties chargées sont surlignées dans le texte, celle où vous vous trouvez dans une couleur plus forte. Effacez
  le champ pour retirer le surlignage.

Sur téléphone, la recherche se trouve dans le tiroir du plan ; le tiroir se ferme lorsqu'il affiche une correspondance, rouvrez-le pour la suivante.

<a id="the-lorebook-tab"></a>

### L'onglet lorebook

L'onglet **Lorebook** de la barre latérale affiche le savoir du lorebook du livre sans quitter l'histoire, de sorte que vous n'avez pas à changer d'onglet
pour vérifier ce que dit une entrée, ou pour corriger un mot dedans. Il est relu chaque fois que vous revenez à l'onglet *Histoire*, de sorte que
les modifications faites dans l'onglet *Lorebook* du livre apparaissent aussitôt.

- La **zone de recherche** en haut n'affiche que les entrées dont le texte figure dans leur nom, leur contenu, leur note ou leur filtre (la casse n'a pas d'importance).
- Le **crayon** d'une entrée ouvre une petite fenêtre avec son nom, son activation, le filtre et la manière dont elle est recherchée, le contenu
  et la note ; **Enregistrer** les écrit (les étiquettes, l'ordre et les réglages d'activation se modifient dans l'onglet *Lorebook* du livre). Rien
  ne peut être modifié pendant qu'un agent travaille sur le lorebook.
- Le **bouton d'agrandissement** (⤢) ouvre l'[éditeur de lorebook en plein écran](../lorebooks/lorebooks.md#editing-in-full-screen) (ordinateur uniquement) : celui situé à côté de la zone de recherche
  du lorebook, celui d'une entrée sur cette entrée, celui d'un sous-lorebook (avec *Inclure les sous-lorebooks*) sur ce lorebook. L'onglet est rechargé
  lorsque vous fermez l'éditeur.

- Chaque entrée est une ligne avec son nom et le crayon ; la flèche l'ouvre pour montrer son **contenu** (rendu en Markdown) et le **texte qui l'active** (le
  *Texte/motif de filtrage*, avec une note lorsqu'il s'agit d'une expression régulière).
- **Inclure les sous-lorebooks** affiche le lorebook et chacun de ses sous-lorebooks (et leurs sous-lorebooks, quelle que soit leur profondeur) comme des lignes
  distinctes, avec leurs entrées dessous ; cliquez sur la flèche d'un lorebook pour masquer ses entrées. Sans cette option, seules les entrées du lorebook du livre sont listées.
- Les entrées et les lorebooks désactivés (ils ne sont pas utilisés pour l'histoire) sont affichés en grisé.
- Un livre sans lorebook affiche une note à la place.

![L'onglet Lorebook avec deux entrées ouvertes](../../images/story-lorebook-tab.png)

Comme tous les onglets de l'éditeur, il peut être sorti dans un panneau à part, voir ci-dessous.
<a id="the-planner-tab"></a>

### Le planificateur

L'onglet **Planificateur** est une liste des scènes que vous voulez voir traversées par l'histoire, dans l'ordre souhaité. Chaque scène correspond aux détails d'un tour qui l'écrit : un **titre** pour la liste, le **cadre de la scène**, le **personnage POV**, les **personnages présents** et les **instructions**, soit les quatre mêmes champs que le formulaire de nouveau tour.

![L'onglet Planificateur avec quatre scènes, la première déjà écrite](../../images/planner-tab.png)

- **Ajouter une scène** ouvre un formulaire pour une nouvelle scène ; elle est placée à la fin de la liste. Le crayon modifie une scène, les flèches la déplacent vers le haut ou vers le bas, la corbeille la supprime.
- Le **+** de la ligne d'une scène **insère une nouvelle scène** juste en dessous, entre cette scène et la suivante.
- Le bouton de fusion d'une scène la **fusionne avec la suivante** : un formulaire s'ouvre avec les détails des deux scènes déjà réunis (les deux titres, les cadres de scène et les instructions l'un sous l'autre, les personnages présents des deux, le personnage POV de la première ; lorsque la seconde scène a un autre personnage POV, le formulaire l'indique). Modifiez ce qui ne convient pas et appuyez sur **Fusionner** (ou <kbd>Ctrl/Cmd+Entrée</kbd>) : les deux scènes ne forment plus qu'une, à la place de la première. L'opération n'est effectuée que si les deux scènes ont été écrites.
- **Importer depuis la branche** ajoute une scène pour chaque partie de la branche sur laquelle se trouve le livre (de la première partie jusqu'à la plus récente) qui possède des détails de tour : son cadre de scène, son personnage POV, ses personnages présents et ses instructions, la première ligne des instructions servant de titre. Les parties sans détails de tour sont ignorées, et les scènes sont ajoutées après celles que vous avez déjà, non cochées. Utilisez cette fonction pour planifier la réécriture d'une histoire que vous avez écrite : importez, fusionnez les tours qui vont ensemble, insérez ce qui manque, puis écrivez les scènes sur une nouvelle branche ou dans une copie du livre.
- La case devant une scène indique qu'elle est **terminée** (écrite) : le titre est barré et la scène est estompée. Rien ne le vérifie à votre place : cochez-la lorsque vous avez écrit la scène. La ligne au-dessus de la liste compte combien de scènes sont terminées.
- **Cliquez sur une scène** pour placer ses détails dans le **formulaire de nouveau tour** (le formulaire s'ouvre, avec le cadre de scène, le POV, les personnages présents et les instructions de la scène ; ce qui s'y trouvait est remplacé), puis appuyez sur **Générer** lorsque vous êtes satisfait. Sur téléphone, le tiroir se referme d'abord.
- Les scènes sont enregistrées avec le livre, elles figurent donc dans ses sauvegardes et ses copies. Un modèle peut aussi ajouter des scènes : l'[assistant de plan](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de l'extension Arc Outline et un [agent](../agents.md) peuvent lire et modifier le planificateur grâce aux [commandes](../commands/index.md#the-planner) ; la liste affiche immédiatement ce qu'ils ont ajouté.

![Le formulaire d'une nouvelle scène](../../images/planner-scene-dialog.png)

![Un clic sur une scène remplit le formulaire de nouveau tour avec ses détails](../../images/planner-new-turn.png)

Comme chaque onglet de l'éditeur, il peut être détaché dans un panneau à part, voir ci-dessous.

<a id="arranging-the-tabs"></a>

### Disposer les onglets

L'éditeur est un ensemble d'onglets : l'**Histoire** elle-même (les parties et la barre inférieure qui les suit), le **Plan**, la **Lorebook**, et les onglets que les [extensions](../extensions/index.md) ajoutent (Personnages, Arcs, Side Query...). Faites glisser un onglet par son en-tête pour les réorganiser (sur ordinateur uniquement ; le téléphone garde une seule colonne, avec le plan dans un tiroir). Un nouveau livre place le Plan et la Lorebook dans un panneau à gauche, et l'Histoire dans une colonne à elle seule à droite ; vous pouvez placer l'Histoire où vous voulez, par exemple au milieu, avec le plan d'un côté et la lore de l'autre, ou au-dessus du plan :

- Déposez-le sur l'une des bandes en pointillés qui apparaissent au-dessus, entre et sous les panneaux d'une colonne pour le **détacher** dans un panneau à part, dans cette colonne ; les panneaux d'une colonne sont empilés les uns sous les autres.
- Déposez-le sur l'une des bandes verticales à gauche, à droite ou entre les colonnes pour le **détacher sur le côté** : il obtient une colonne à lui seul, à côté des autres. Élargissez l'éditeur avec les séparateurs pour faire de la place à d'autres colonnes.

Les panneaux et les colonnes sont séparés par des séparateurs : faites-les glisser pour donner à chaque panneau la place dont il a besoin. Les tailles sont enregistrées avec la disposition.

![La barre latérale avec Personnages dans une colonne à part et Side Query dessous](../../images/story-editor-tabs.png)

- Déposez-le sur l'en-tête d'un onglet ou sur le corps d'un autre panneau pour **rejoindre** ce panneau (déposé sur un onglet, il se place avant cet onglet ; déposé sur le corps, il se place en dernier). Un panneau qui perd son dernier onglet disparaît, de même qu'une colonne sans panneau.

La disposition et les tailles sont enregistrées avec le livre : elle est donc la même à la prochaine ouverture et dans chaque navigateur ; l'onglet affiché par chaque panneau est mémorisé avec le reste de votre espace de travail. Un onglet d'extension qui n'est pas chargé conserve sa place, pour le moment où l'extension sera de nouveau disponible.

L'histoire est toujours quelque part : son onglet ne peut pas être fermé, et un onglet qui partage un panneau avec d'autres onglets est masqué lorsqu'un autre est sélectionné. Tout ce qui affiche une partie ramène l'histoire à l'écran : cliquer sur une entrée du plan, un résultat de la recherche, choisir une branche dans l'[arbre de l'histoire](branches-and-story-tree.md), ou lancer une génération sélectionne d'abord l'onglet **Histoire**. Pendant qu'une génération prépare son prompt (généralement quelques secondes), les autres onglets sont désactivés ; une fois que le modèle écrit, ils redeviennent utilisables, voir [plus bas](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Réduire les onglets

Chaque onglet, sauf l'histoire, possède un petit bouton **⌄** (chevron) à côté de son nom. Il **réduit** l'onglet : celui-ci quitte son panneau (un panneau dont tous les onglets sont réduits disparaît pour l'instant) et une icône ronde le représentant apparaît dans une barre au bord de l'éditeur : un disque coloré avec un dessin (une liste pour le plan, un livre pour la lorebook, une bulle pour les annotations, une loupe pour Side Query, et ainsi de suite ; un onglet d'extension qui n'a pas de dessin reçoit une ou deux lettres). Survolez l'icône pour voir de quel onglet il s'agit. Rien n'est fermé : l'onglet conserve ce qu'il affichait. Cliquez sur l'icône, et l'onglet revient dans son panneau, à la place qu'il occupait parmi les onglets, et s'affiche.

La barre reste là tant qu'un onglet est réduit. Le bouton en haut de la barre (les flèches circulaires) envoie la barre vers le côté suivant de l'éditeur, dans le sens des aiguilles d'une montre : gauche, haut, droite, bas. L'histoire ne peut pas être réduite, il reste donc toujours quelque chose à l'écran.

![Onglets réduits dans la barre au bord de l'éditeur](../../images/story-minimized-tabs.png)

Les icônes sont classées par ordre alphabétique des noms des onglets jusqu'à ce que vous le modifiiez : faites glisser une icône sur une autre et elle prend la place de celle-ci. Les onglets réduits, la position de la barre et l'ordre des icônes sont enregistrés avec le livre, ainsi que la disposition et les tailles.

<a id="the-default-arrangement"></a>

### La disposition par défaut

Un nouveau livre s'ouvre avec la disposition standard (le plan, la lorebook et les annotations à gauche, l'histoire à droite). Pour faire de votre propre disposition la disposition standard, réglez les onglets comme vous le souhaitez (panneaux, tailles, onglets réduits, côté de la barre), puis choisissez **Enregistrer la disposition comme ma disposition par défaut** dans le menu des réglages (⚙) de l'histoire. Chaque livre qui n'a pas de disposition propre (un livre que vous n'avez pas réorganisé) s'ouvre alors avec celle-ci ; un livre que vous avez disposé conserve sa disposition. La disposition par défaut est conservée dans vos réglages utilisateur.

Pour revenir à la disposition par défaut, choisissez **Rétablir la disposition par défaut** dans le menu des réglages (⚙) de l'histoire, puis confirmez. Il s'agit de celle que vous avez enregistrée, ou de la disposition standard si vous n'en avez enregistré aucune. Cela ne fait que replacer les onglets (et oublie les tailles et ce qui est réduit) : vos textes, l'onglet affiché par chaque panneau et les onglets d'extensions restent en place, ces derniers dans le premier panneau.
<a id="writing-the-next-part"></a>

## Rédiger la partie suivante

1. Cliquez sur **+** dans le coin inférieur droit. Le panneau *Instructions du nouveau tour* s'ouvre.
2. Remplissez les champs. Ils sont tous facultatifs, mais le modèle a besoin d'au moins quelques indications :

   | Champ | |
   |---|---|
   | **Cadre de la scène** | Où et quand la partie se déroule, par exemple *Le phare, une nuit d'orage à la fin de l'automne*. |
   | **Personnage du point de vue** | Le point de vue à partir duquel la partie est écrite. |
   | **Personnages présents** | Qui est dans la scène, et dans quel état. |
   | **Instructions** | Ce qui doit se passer dans cette partie. |

   *Cadre de la scène*, *Personnage du point de vue* et *Personnages présents* sont repris de la dernière partie, vous ne modifiez donc que
   ce qui est différent. *Instructions* est vide à chaque fois.
3. Cliquez sur **Générer**.

![Le panneau Instructions du nouveau tour](../../images/new-turn-instructions.png)

La nouvelle partie apparaît à la fin et se remplit au fur et à mesure que le modèle l'écrit. Pour les [modèles de raisonnement](../inference-providers.md#reasoning-models),
le raisonnement apparaît d'abord sous **Voir le raisonnement**.

Pendant qu'une partie est générée, le bouton **+** devient un bouton rouge **Arrêter**. Arrêter conserve le texte écrit jusque-là
comme partie.

<a id="while-a-part-is-generated"></a>

### Pendant qu'une partie est générée

Pendant les premières secondes, le temps que l'invite soit composée à partir du livre, toute la fenêtre du livre attend. Dès que le modèle
commence à écrire, seules la nouvelle partie et les parties qui la précèdent restent bloquées :

- **La nouvelle partie** ne peut pas être modifiée tant qu'elle n'est pas terminée : ni édition, ni menu.
- **Les parties qui la précèdent** restent en place : vous pouvez les modifier, les annoter et les rédiger à nouveau, mais pas les supprimer, les fusionner ou
  les embrancher, et l'histoire reste sur sa branche (pas de swipe, pas de changement de branche). Une modification d'une partie antérieure, ou du lore, n'atteint pas la partie
  en cours d'écriture : son invite a déjà été composée. Elle compte pour la suite.
- **Tout le reste est libre** : le lore, les réglages et l'invite du livre, les onglets de la barre de gauche (requête latérale, plan de l'arc, notes...),
  la barre de commandes. Une requête au modèle attend une connexion libre du modèle si la génération en occupe la seule.
- **En attente** : un deuxième tour, la recherche et le remplacement, un import dans le livre et la suppression en masse. Les onglets de l'arbre et de sauvegarde attendent aussi.

Ce que la fenêtre grise est aussi refusé lorsqu'une commande, un agent ou une extension le demande : Marginalia indique ce qui fait obstacle
(*Ceci est en cours d'utilisation : …*).

**Quitter le livre arrête la génération.** Le bouton de sortie demande d'abord confirmation ; *Oui* équivaut à **Arrêter** : ce qui est arrivé est conservé. Fermer
l'onglet du navigateur fait de même, et le navigateur demande confirmation avant de fermer ou de recharger l'onglet pendant qu'une partie est générée. Une courte
perte du réseau, ou un ordinateur portable fermé une minute, ne compte pas comme une sortie : la page revient et affiche ce qui est arrivé entre-temps.

Lorsque le modèle renvoie une erreur (serveur injoignable, clé d'API incorrecte, contexte trop long...), l'erreur est affichée.
Si aucun texte n'est encore arrivé, l'histoire reste telle qu'elle était : une nouvelle partie ou un swipe est retiré, et une partie régénérée retrouve
son texte précédent. Le texte déjà arrivé avant l'erreur est conservé, comme avec **Arrêter**. Corrigez le problème, puis générez à nouveau.

La façon dont les champs entrent dans l'invite est définie par l'[invite utilisateur](prompts.md#user-prompt) du livre. Avec l'invite utilisateur par défaut, le modèle les reçoit sous
*Lieu et heure actuels*, *Personnage du point de vue*, *Personnages présents / État* et *Spécifications de l'intrigue pour la partie suivante*.

!!!
Le texte est écrit en Markdown : `*italics*`, `**bold**`, `# Heading` et ainsi de suite sont affichés avec leur mise en forme. Une partie avec un titre
`#` est un chapitre pour l'extension [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Rédiger les instructions

Vous ne savez pas ce qui doit se passer ensuite ? **Rédiger des instructions** sous le champ *Instructions* du panneau permet au modèle d'écrire
les instructions à votre place, comme vous le feriez. Une fenêtre s'ouvre avec :

| | |
|---|---|
| **Orientation** | Ce que la partie devrait traiter : une idée approximative, une direction (« elle le confronte au sujet de la lettre »), ou rien du tout, et le modèle poursuit l'histoire là où elle le mène. Il part de ce que vous avez déjà saisi dans *Instructions*. |
| **Plan de l'histoire** | Seulement lorsqu'une source en fournit un. Le [Plan de l'arc](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) en fournit un, avec les données du chapitre en cours. Lecture seule ; le modèle le suit. |

**Rédiger des instructions** lance le modèle et diffuse la réponse dans **Instructions rédigées**, que vous pouvez modifier ou vider
avant de continuer ; **Rédiger à nouveau** tente une nouvelle fois, **Arrêter** conserve ce qui est arrivé. Rien n'est modifié tant que vous ne cliquez pas sur
**Utiliser comme instructions** : le texte remplace alors les *Instructions* du panneau (les autres champs ne sont pas touchés) et le panneau s'ouvre
à nouveau, prêt pour **Générer**.

Le modèle reçoit l'histoire jusqu'ici (la partie ancienne sous forme de [résumés](summaries.md), comme lors d'une génération), le cadre de la scène, le point de vue et les
personnages présents qui figurent dans le panneau, le lore de la dernière partie, vos dernières instructions comme exemples de votre façon d'écrire, votre
orientation et le plan. Il ne voit pas les entrées de votre lorebook qu'un nouveau tour activerait. La requête est l'[invite d'incarnation](prompts.md#impersonate-prompt). Le modèle est
celui du livre ; la rédaction consomme des jetons comme toute requête.

![Rédaction des instructions du tour suivant](../../images/impersonate-dialog.png)

La même chose vaut pour la commande [`/impersonate`](../commands/index.md), qui renvoie le texte. Les [agents](../agents.md) ne l'obtiennent pas : ils écrivent
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
*Paramètres → Raccourcis clavier*, où un élément est nommé avec son sous-menu (*Analyse › Statistiques*). Les sections ci-dessous indiquent où se trouve chaque élément.
<a id="the-part-menu"></a>

## La menu d'une partie

Chaque partie a un menu (☰) dans son coin supérieur droit. **Modifier**, **Abandonner les modifications**, **Régénérer**, **Variante**, **Réécrire**, **Historique** et **Images** se trouvent dans le menu lui-même, **Supprimer** en est le dernier élément, et le reste se trouve dans quatre sous-menus : **Annotations** (*Annoter la partie*, *Annoter avec l'IA*), **Résumé** (*Générer un résumé* / *Voir le résumé*, *Supprimer le résumé*), **Inspecter** (*Afficher le prompt*, *Comparer le prompt*, *Utilisation des jetons*, *Utilisation du lore*) et **Branches et fusion** (*Créer une branche*, *Fusionner avec la partie précédente*). Un sous-menu sans rien pour la partie n'est pas affiché. Le Relecteur ajoute son sous-menu **Examiner** après *Résumé*. Les éléments :

| Élément | |
|---|---|
| **Modifier** | Transforme le texte en champ modifiable. **Enregistrer** (le même élément de menu) l'enregistre. Démarrer une génération ou quitter l'onglet enregistre aussi les modifications en cours. |
| **Abandonner les modifications** | Uniquement pendant la modification du texte : ferme le champ sans enregistrer, la partie conserve le texte de sa dernière sauvegarde. Si vous avez modifié le texte, on vous le demande d'abord. |
| **Régénérer** | Réécrit la dernière partie, en remplaçant son texte. Uniquement sur la dernière partie. |
| **Variante** | Écrit une autre version de la dernière partie et conserve l'ancienne comme branche séparée. Uniquement sur la dernière partie. Voir [Branches et arbre de l'histoire](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Réécrire** | Permet au modèle de réécrire cette partie à partir de vos instructions, sur n'importe quelle partie, voir [plus bas](#redrafting-a-part). Pas pendant la modification du texte. |
| **Historique (n)** | Les [versions du texte](version-history.md) de la partie, avec leur nombre : les comparer, en remettre une en place, en nommer une. Pas pendant la modification du texte. |
| **Annoter la partie** | Ajoute une [note](annotations.md#making-an-annotation) sur la partie entière. Pour annoter des mots, sélectionnez-les et faites un clic droit (**Annoter la sélection**). |
| **Annoter avec l'IA** | Permet à un modèle d'[examiner la partie](annotations.md#letting-a-model-annotate) et de proposer des notes. Pas pendant la modification du texte. Pour plusieurs parties à la fois, utilisez **Annoter une plage de parties avec l'IA** dans le menu des réglages (⚙), et pour chaque nouvelle partie **Annotation automatique** (voir [Annotations](annotations.md#annotating-every-new-part-automatically)). |
| **Créer une branche** | Démarre une nouvelle branche à partir de cette partie, voir [Branches et arbre de l'histoire](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Fusionner avec la partie précédente** | Ajoute le texte de cette partie à la partie qui la précède, voir [plus bas](#merging-a-part-into-the-previous-one). Pas sur la première partie. |
| **Afficher le prompt** | Affiche le prompt exact envoyé au modèle pour cette partie. |
| **Utilisation des jetons** | Montre comment le contexte du modèle a été utilisé pour cette partie : un graphique des sections du prompt (instructions du modèle, lore, résumés, histoire jusqu'ici, vos instructions, place réservée à la réponse) et la taille de chaque message envoyé au modèle. Si une partie de l'histoire n'a pas tenu, il indique combien de parties ont été laissées de côté. Les parties écrites avant que cela soit enregistré n'ont pas de graphique. |
| **Utilisation du lore** | Liste les entrées du [lorebook](../lorebooks/index.md) qui ont été intégrées au prompt de cette partie, avec les jetons que chacune a coûté et ce qui l'a trouvée (le prompt, ou le contenu d'une autre entrée lorsque le lorebook explore plus en profondeur). Les parties saisies à la main, écrites avant que cela soit enregistré, ou écrites pour un livre sans lorebook indiquent que rien n'a été enregistré. Pour l'ensemble du livre, utilisez **Utilisation du lore** dans le menu des réglages (⚙), voir [plus bas](#which-lore-entries-were-used). |
| **Images** | Joint des images à la partie, voir [Images](#images). |
| **Générer un résumé** / **Voir le résumé** | Résume l'histoire jusqu'à cette partie, ou affiche le résumé, voir [Résumés](summaries.md). |
| **Supprimer le résumé** | Supprime le résumé de cette partie. Pour un [méta-résumé](summaries.md#meta-summaries), il demande s'il faut le défaire ou le supprimer complètement. |
| **Supprimer** | Supprime la partie, voir [plus bas](#deleting-a-part). |

*Régénérer* et *Variante* utilisent les détails du tour de la dernière partie (voir [plus bas](#turn-details)). Pour changer ce qui doit
se passer, modifiez-les d'abord.

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

**Détails du tour** ouvre le *Cadre de la scène*, le *Personnage du point de vue*, les *Personnages présents* et les *Instructions* à partir desquels la partie a été
écrite. Pour la dernière partie, ils peuvent être modifiés (les changements sont enregistrés immédiatement) et sont utilisés par *Régénérer* et
*Variante*. Pour les parties précédentes, ils sont en lecture seule.

![Détails du tour d'une partie](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Modifier le texte

Le texte généré vous appartient : vous pouvez le changer. Choisissez **Modifier** dans le menu de la partie, changez le texte, puis choisissez **Enregistrer**. Les parties modifiées sont envoyées au modèle telles que vous les avez modifiées.

Modifier une partie couverte par un [résumé](summaries.md) rend ce résumé périmé ; Marginalia le remarque à la prochaine génération et supprime le résumé. Pour que cela ne se produise pas par surprise, **Enregistrer** vérifie d'abord le nouveau texte : si celui-ci rendrait des résumés périmés, une fenêtre indique combien et propose **Retour à l'édition** (rien n'est enregistré, l'éditeur reste ouvert avec votre texte), **Enregistrer quand même** (les résumés deviennent périmés) et **Enregistrer et revalider les résumés** (le texte est enregistré et les résumés restent valides, pour une modification qui ne change pas ce qu'ils disent, comme une coquille). La vérification se fait pour **Enregistrer** (le menu, le raccourci et le bouton de l'éditeur) ; les enregistrements automatiques, par exemple lorsque vous démarrez une génération alors que l'éditeur est ouvert, ne demandent rien.

<a id="keyboard-shortcuts"></a>

## Raccourcis clavier

Sur un ordinateur (la [disposition téléphone](../phone.md) n'en a pas), la boucle d'écriture fonctionne au clavier :

| Touche | |
|---|---|
| `+` | Ouvre le panneau *Instructions du nouveau tour* avec le curseur dans *Instructions*. |
| `Ctrl+Enter` | Génère à partir du panneau ouvert. `Esc` le ferme ; le clavier revient à l'histoire. |
| `Ctrl+.` | Arrête la génération en cours. |
| `s` / `r` | **Variante** / **Régénérer** la dernière partie. |
| `e` | Ouvre l'éditeur de la partie la plus visible à l'écran (la dernière partie si aucune ne l'est). |
| `Ctrl+S` | Enregistre la partie en cours de modification (aussi lorsque le curseur est dans son texte). |
| `/` | Place le curseur dans la barre de commandes (l'onglet histoire s'affiche d'abord). |
| `Ctrl+Shift+F` | Ouvre l'onglet *Plan* et place le curseur dans la recherche de la branche (`Enter` / `Shift+Enter` vont à la correspondance suivante / précédente). |
| `Ctrl+Shift+H` | Ouvre la fenêtre *Rechercher et remplacer* du livre. |
| `a` | **Annoter la sélection** : ajoute une note sur le texte sélectionné dans une partie. |
| `Alt+PageDown` / `Alt+PageUp` | Affiche l'onglet suivant / précédent, en parcourant les onglets de toutes les feuilles dans l'ordre de la disposition, puis recommence. |

Une touche sans `Ctrl`, `Alt` ou `Meta` (comme `+`, `s`, `r`, `e`, `a`, `/`) ne fonctionne que lorsqu'aucun champ de texte n'a le focus, afin de ne jamais gêner la saisie. Les raccourcis fonctionnent dans l'onglet histoire du livre ouvert et non lorsqu'une autre fenêtre est ouverte par-dessus. Les boutons affichent leur touche dans l'info-bulle, et le menu de la dernière partie l'affiche après *Modifier*, *Régénérer* et *Variante*. Sur un Mac, `Cmd` fonctionne là où une touche indique `Ctrl` (sauf si vous avez attribué cette touche `Cmd` à une autre action). Vous pouvez modifier chaque touche dans [Réglages → Raccourcis clavier](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Supprimer une partie

**Supprimer** retire la partie de l'histoire. Les parties qui la suivaient restent et reprennent à partir de la partie qui la précède, donc
supprimer une partie au milieu de l'histoire la retire simplement. Supprimer la dernière partie fait de la partie qui la précède
la nouvelle fin de la branche.

<a id="deleting-many-parts-at-once"></a>

### Supprimer plusieurs parties à la fois

Le menu des réglages (⚙) contient **Suppression en masse**. L'activer place une case à cocher à gauche de chaque partie et affiche
sous l'histoire une barre avec le nombre de parties cochées et un bouton **Supprimer**. Cochez les parties que vous voulez retirer (les cases restent
cochées pendant le défilement, l'histoire ne charge qu'une fenêtre de parties) ; **Maj + clic** sur une case coche (ou décoche) toutes les parties
situées entre celle-ci et la dernière case sur laquelle vous avez cliqué ; une partie cochée est teintée en rouge. Appuyez ensuite sur **Supprimer** : après une confirmation, les parties sont supprimées
une à une derrière une barre de progression qui ne peut pas être interrompue. Elles sont supprimées comme une seule partie l'est : les parties qui suivaient une partie supprimée
reprennent à partir de la partie qui la précède, et les parties supprimées vont à la corbeille. Ensuite, l'histoire est de nouveau affichée et la suppression
en masse est désactivée. **Annuler** dans la barre, ou choisir de nouveau **Suppression en masse**, la désactive sans rien supprimer. Lancer une génération la désactive aussi. Une partie cochée sur laquelle
une fenêtre ou une traduction travaille reste dans l'histoire, et Marginalia indique ce qui la retient.

![Suppression en masse avec quelques parties cochées](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Refaire un passage

**Régénérer** et **Variante** ne travaillent que sur la dernière partie et l'écrivent entièrement à neuf. **Réécrire** agit sur n'importe quelle partie, y compris une ancienne au milieu de l'histoire, et conserve ce que vous ne demandez pas de modifier. Choisissez **Réécrire** dans le menu de la partie :

1. Écrivez ce qui doit changer dans **Instructions pour la réécriture** : *ajouter plus de dialogue entre les deux*, *rallonger le combat*, *supprimer l'explication de la carte*, *garder la fin telle quelle*. Des instructions vides réécrivent la partie avec les mêmes événements.
2. Choisissez **Longueur** si la partie doit devenir **Plus court** ou **Plus long** (la valeur par défaut est **Même longueur**) : une phrase qui le demande est ajoutée après vos instructions (voir [Redraft Shorter Prompt et Redraft Longer Prompt](prompts.md#redraft-shorter-and-longer)).
   Choisissez **Réécrire**. Le texte s'affiche au fur et à mesure dans **Texte réécrit** ; **Arrêter** le termine plus tôt. Choisissez **Réécrire à nouveau** pour un nouvel essai, avec d'autres instructions si vous le souhaitez. **Différences** montre ce qui a changé par rapport à la partie, mot à mot. Lorsque la partie a des [annotations](annotations.md) ouvertes, **Traiter les annotations ouvertes de la partie** (coché d'emblée) les transmet aussi au modèle, et **Appliquer** les marque comme résolues.
3. Le texte réécrit est un champ que vous pouvez encore modifier (son icône de coin le fait grandir pour tenir tout le texte), et **Différences** suit vos modifications. **Appliquer** remplace le texte de la partie par celui-ci ; **Fermer** le jette. Rien ne change dans le livre avant **Appliquer**.

![La fenêtre de réécriture : les instructions, le nouveau texte et les différences](../../images/redraft-dialog.png)

Le modèle reçoit les [invites de réécriture](prompts.md#redraft-prompt) : la partie, vos instructions, l'histoire qui précède la partie (l'histoire plus ancienne sous forme de [résumés](summaries.md) lorsqu'ils sont valides, le reste en texte, la partie la plus récente en premier lorsque tout ne tient pas), autant de l'histoire qui suit la partie que la place le permet (jusqu'à un quart de l'espace disponible), le savoir que les livres de connaissances donnent à cette partie maintenant (recalculé pour le tour de la partie, de sorte qu'une modification d'une entrée parvienne à la réécriture ; le savoir avec lequel la partie a été écrite n'est utilisé que si le livre n'a pas de livre de connaissances), le point de vue, le temps et le style du livre, ainsi que le cadre de la scène, le personnage au point de vue, les personnages présents et les instructions du tour dont la partie est issue. L'histoire qui suit la partie est là pour que le nouveau texte mène toujours à elle : les événements sur lesquels s'appuient les parties suivantes doivent être conservés.

La partie garde sa place, son résumé, ses images et son mode de génération ; le texte, ses mots et ses jetons sont remplacés et la partie est considérée comme modifiée. Appliquer une réécriture est une modification, comme **Enregistrer** dans l'éditeur ; les [résumés](summaries.md) qui décrivent l'ancien texte deviennent donc périmés, et Marginalia indique combien. Un résumé porté par la partie elle-même en fait partie.

Lorsqu'un résumé décrit la partie (la partie en a un, ou une partie plus récente de la branche en a un), la fenêtre affiche la case **Revalider les résumés que cette réécriture rend périmés**, décochée d'emblée. Cochez-la lorsque les résumés disent toujours la bonne chose (la réécriture a changé la formulation, pas ce qui se passe) : **Appliquer** recalcule alors leur empreinte et ils restent valides. Laissez-la décochée lorsque les événements ont changé, et refaites les résumés.

La fenêtre ne bloque pas l'histoire, voir [Travailler à côté de la fenêtre](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Travailler à côté des fenêtres de réécriture et d'annotation

Sur un ordinateur, les fenêtres **Réécrire** et **Annoter avec l'IA** se placent à droite de l'histoire et la laissent utilisable : vous pouvez lire, faire défiler et modifier l'histoire pendant que le modèle écrit. Faites glisser une fenêtre par son titre pour la déplacer, ou par son bord pour la redimensionner. La flèche (⌄) dans le titre replie une fenêtre dans le [panneau des exécutions](#the-panel-of-the-runs) en bas à gauche de la page : le modèle continue, **Ouvrir** (ou un clic sur l'entrée) rouvre la fenêtre, et **Arrêter** arrête le modèle (une fois terminé, **Fermer** ferme la fenêtre). Sur téléphone, les fenêtres recouvrent encore l'histoire, mais elles se replient de la même façon.

![La fenêtre de réécriture à côté de l'histoire pendant que le modèle écrit, et l'exécution dans le panneau en bas à gauche](../../images/redraft-beside-story.png)

**Fermer et Arrêter sont deux choses différentes.** Le **Arrêter** propre à la fenêtre arrête le modèle, et la fenêtre reste avec ce qui est arrivé. **Fermer** pendant que le modèle travaille ne l'arrête pas : la fenêtre se replie dans le panneau et le modèle poursuit sa file ; sa réponse est dans la fenêtre lorsque vous la rouvrez. Fermez une fenêtre dont le modèle a fini, ou **Fermer**-la dans le panneau, et elle disparaît ; fermer le livre ou l'onglet du navigateur arrête aussi une fenêtre repliée. Il en va de même pour les autres fenêtres qui attendent un modèle : un **résumé** (et *Créer des résumés*, *Réécrire tous les résumés*), le [Relecteur](../extensions/reviewer.md), la description du livre, les générateurs de savoir et de plan, les générateurs d'images et de manga, et le rédacteur de personnages des Lecteurs bêta. Les fenêtres *Studio* et *Convertir* du livre audio ne bloquent pas non plus la page ; les fermer n'a jamais arrêté la conversion.

Tant qu'une fenêtre est ouverte (ou repliée) sur une partie, la partie affiche ce qu'elle fait à côté de son menu (*Réécriture en cours · Le livre*), et **Régénérer**, **Variante**, **Réécrire**, **Annoter avec l'IA**, **Fusionner avec la précédente** et **Supprimer** sont désactivés pour cette partie, afin que la fenêtre ne perde pas sa partie. Modifier la partie reste possible. Fermer le livre ferme ses fenêtres et arrête leurs modèles. Une partie sur laquelle un [traducteur](../extensions/real-language-translator.md) travaille affiche *Traduction des répliques de la partie...* de la même façon, et deux traducteurs ne travaillent jamais sur une même partie en même temps : le second indique que la partie est utilisée.

- Lorsque vous **Appliquez** une réécriture de la partie entière et que la partie a été modifiée après l'ouverture de la fenêtre (vous l'avez éditée entre-temps), Marginalia demande d'abord : **Différences** compare alors la partie telle qu'elle est maintenant avec la réécriture, pour que vous voyiez ce que l'application remplacerait ; choisissez **Appliquer quand même** ou **Annuler**. Une réécriture qui [s'applique d'elle-même](annotations.md#annotating-every-new-part-automatically) ne le fait pas lorsque la partie a été modifiée : la fenêtre attend alors votre décision.
- Lorsque la partie a été supprimée avant que vous **Appliquiez** la réécriture ou ajoutiez les annotations, la fenêtre le signale, n'écrit rien et se ferme.

<a id="the-panel-of-the-runs"></a>

### Le panneau des exécutions

Tout ce qui travaille actuellement sur vos livres avec un modèle est listé en bas à gauche de chaque page : une génération, une réécriture ou une annotation, une traduction par le traducteur [Real Language](../extensions/real-language-translator.md) ou [Conlang](../extensions/conlang-translator.md), une réécriture des résumés, la lecture de l'histoire par le Suivi des connaissances, une conversion [Livre audio](../extensions/audiobook.md), une commande de la barre de commandes qui prend du temps (*Rédiger des instructions*, un résumé... : sa fenêtre se place à côté et se replie comme les autres). Les fenêtres dans lesquelles vous attendez une réponse y figurent aussi pendant que le modèle répond : **Incarner**, l'assistant d'aide, une traduction rapide, le concepteur de langues construites, une discussion de Requête annexe, de Lecteurs bêta ou de l'assistant d'intrigue. Il liste vos exécutions de **toutes vos pages ouvertes** (un autre onglet, le téléphone), et pas seulement celles lancées sur cette page. Rien en cours, rien d'affiché.

![Le panneau des exécutions ouvert : la réécriture repliée dedans, avec la fin du texte du modèle, Ouvrir et Arrêter](../../images/runs-panel.png)

- **Fermé**, il se présente sous la forme d'une petite barre : la première exécution, combien d'autres (*Traduction des répliques de la partie... · 2 de plus*) et une barre mobile pendant qu'un modèle travaille. Cliquez dessus pour ouvrir la liste ; elle s'ouvre toute seule lorsque vous repliez une fenêtre dedans.
- **Ouvert**, chaque exécution indique ce qu'elle fait, le livre, sa durée, ce qu'elle attend et la fin de ce qu'écrit le modèle :
  - *En attente du modèle : 2 requêtes avant celle-ci* lorsque le modèle est occupé par d'autres requêtes (voir *Connexions* dans [Fournisseurs d'inférence](../inference-providers.md)) ;
  - *Le modèle réfléchit...* ou *Le modèle écrit...*, avec la fin de sa réflexion ou de sa réponse en dessous ;
  - *Vous attend* lorsque l'exécution attend une décision de votre part (une réécriture à appliquer, une proposition du Suivi des connaissances à examiner, la question de l'inspecteur d'invite avant une génération, une commande de l'assistant d'intrigue à autoriser).
- **Arrêter** arrête l'exécution, où qu'elle ait été lancée. **Ouvrir**, ou un clic sur l'entrée, réaffiche une fenêtre repliée (uniquement sur la page où elle a été ouverte) ou l'exécution elle-même ; le bouton devient **Examiner** lorsque l'exécution attend votre décision. Une exécution du Suivi des connaissances ouvre son livre sur cette page (lorsqu'aucun autre livre n'est ouvert ici ; sinon, on vous demande d'abord de fermer celui-là), passe à l'onglet Connaissances et affiche la revue. **Fermer** ferme définitivement une fenêtre (et arrête son modèle).
- Sur un ordinateur, vous pouvez déplacer le panneau par sa poignée (⋮ à sa gauche) ; il reste où vous le laissez, dans ce navigateur. Dans la moitié inférieure de la page, il s'élève lorsque vous l'ouvrez, comme dans son coin, et il reste toujours dans la page. Lorsqu'il se trouve à l'endroit du bouton rond de l'[assistant d'aide](../administration/help-assistant.md) (sur un livre, les deux sont dans le coin inférieur gauche), le bouton s'écarte et revient lorsque la place est libre.

Lorsqu'une exécution que vous avez lancée se termine après la fermeture de sa page, une autre de vos pages ouvertes envoie sa [notification](../account.md#notifications) (pour les exécutions qui en ont une, comme la réécriture des résumés).

<a id="redrafting-only-a-passage"></a>

### Réécrire seulement un passage

Pour réécrire quelques phrases et laisser le reste de la partie intact, sélectionnez-les dans le texte de la partie puis faites un **clic droit** (un appui prolongé sur téléphone) : le menu propose **Réécrire la sélection**. (Le menu ☰ de la partie n'est pas proposé pour cela, car l'ouvrir peut faire perdre la sélection dans certains navigateurs.) La fenêtre fonctionne comme ci-dessus avec deux différences : elle affiche le **Texte sélectionné**, et la réécriture ne concerne que le texte qui remplace celui-ci. Le modèle reçoit la partie entière avec la sélection marquée, ainsi que l'histoire et le savoir qui l'entourent, afin que le nouveau passage se raccorde aux phrases qui le précèdent et le suivent. **Différences** compare la sélection avec le texte réécrit ; **Appliquer** place le texte réécrit à la place de la sélection et conserve le reste de la partie, y compris l'espace ou le saut de ligne aux bords de la sélection. Avec des [annotations](annotations.md) ouvertes, la case à cocher ne propose que celles dont le passage chevauche la sélection. Si la partie a été modifiée après que vous avez sélectionné le texte (dans une autre fenêtre), **Appliquer** refuse et vous sélectionnez à nouveau. Pendant que la partie est en cours de modification, c'est la sélection de l'éditeur qui est utilisée, et la modification est d'abord enregistrée.

<a id="what-follows-a-generated-part"></a>

### Ce qui suit une partie générée

Lorsqu'une partie que vous avez générée (depuis la page ou la barre de commandes) est terminée, le livre peut en faire davantage de lui-même, dans cet ordre :

1. **Annoter** : l'[annotation automatique](annotations.md#annotating-every-new-part-automatically) examine la partie.
2. **Réécrire** : la partie est réécrite pour tenir compte des notes.
3. **Traduire** : les traducteurs [Real language](../extensions/real-language-translator.md) et [Conlang](../extensions/conlang-translator.md) traduisent ses répliques.
4. **Savoir** : le [Suivi des connaissances](../extensions/knowledge-tracker.md) la lit.
5. **Audio** : le [Livre audio](../extensions/audiobook.md) en fait un enregistrement sonore.

Seules les étapes que le livre a activées s'exécutent. Elles tournent sur le serveur, de sorte que la page peut être fermée, et elles forment une seule entrée dans le [panneau des exécutions](#the-panel-of-the-runs) : *Après la partie 14 : Annoter ✓ · Réécrire … · Traduire*, avec le texte du modèle et un seul **Arrêter**. Ce qui modifie le texte passe en premier, afin que le suivi et l'audio reçoivent le texte final. Une étape qui attend votre décision (des notes ou une réécriture à choisir) ouvre sa fenêtre, et l'entrée propose **Examiner** pour l'ouvrir dans une autre page.

- **Arrêter**, fermer une fenêtre qui attend votre décision, ou une étape qui échoue met fin à la chaîne : ce qui a été fait reste, la suite n'est pas exécutée, et un message indique ce qui reste à faire à la main (chaque étape a sa propre action sur la partie).
- Pendant que la **réécriture** tourne, la partie suivante ne peut pas être générée (on vous indique quelle exécution le bloque) ; pendant que les autres étapes tournent, elle le peut.
- La partie ne peut pas être supprimée tant que son texte n'est pas définitif ; la générer à nouveau met fin à ce qui suivait sa version précédente.
- Un [agent](../agents.md) effectue ces étapes par ses propres commandes : rien ne suit une partie qu'un agent a générée.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionner une partie avec la précédente

Un modèle qui s'est arrêté au milieu d'une phrase (la limite de réponse, une connexion coupée) ne peut pas être invité de façon fiable à « continuer » le même texte. Écrivez plutôt la partie suivante (*continue from where the last part stopped*) et joignez les deux avec **Fusionner avec la partie précédente** dans le menu de la seconde. Une revue s'ouvre :

- **Joindre les textes avec** : une ligne vide, un saut de ligne, une espace ou rien. Cette option est présélectionnée selon la fin du texte précédent : un paragraphe après un texte qui se terminait par une phrase, une espace après un texte qui a été coupé.
- Le **cadre de la scène**, le **personnage du point de vue**, les **personnages présents** et les **instructions** des deux tours, combinés dans des champs modifiables. Ce que les deux tours ont en commun au début est conservé une seule fois (« Write the duel. Keep it short » et « Write the duel. Add rain » donnent le premier, puis « Add rain » sur la ligne suivante).
- **Données** : ce que les deux parties conservent, valeur par valeur (par exemple les [variables](../templates/macros.md) de l'histoire et les données des extensions), avec l'indication si la valeur est identique, différente ou présente seulement sur l'une des parties. La partie fusionnée conserve la valeur de la **partie la plus récente** (c'est elle qui représente l'état actuel de l'histoire), sauf si vous choisissez pour elle la valeur de la partie précédente, ou si vous la retirez. Les valeurs identiques sont masquées, sauf si vous les demandez.

![La revue avant qu'une partie soit fusionnée avec la précédente](../../images/part-merge-dialog.png)

La partie précédente conserve sa place, son résumé et la manière dont elle a été générée (prompt, modèle, durées) ; elle reçoit le texte joint, le raisonnement joint et les images, et ses mots et jetons sont recomptés. La partie fusionnée est supprimée (elle se trouve dans la [Corbeille](../trash.md)) et ce qui la suivait se poursuit à partir de la partie précédente. Les résumés qui couvrent les deux parties deviennent obsolètes, comme après une modification. L'[historique](version-history.md#backups-copies-and-the-trash) de la partie fusionnée est supprimé, et la partie survivante a le texte joint comme version **Fusionnée**. Une partie ne peut pas être fusionnée lorsqu'elle est la première partie du livre, ou lorsque la partie qui la précède a d'autres suites (variantes de la partie) : supprimez-les ou transformez-les d'abord en branche.

## Images

Une partie peut comporter des images : une carte, le portrait d'un personnage, une illustration. Choisissez **Images** dans le menu de la partie pour ouvrir la liste des images de la partie.

![La boîte de dialogue Images d'une partie](../../images/images-dialog.png)

- **Ajouter une image** (ou déposer un fichier dessus) importe une image PNG, JPEG, GIF ou WebP d'au plus 10 Mo. Les images WebP sont converties en PNG lors de l'import, car les fichiers Word et PDF ne peuvent pas contenir de WebP.
- **Légende** s'affiche sous l'image. Elle peut être vide.
- **Position dans la partie** est une grille 3x3. La colonne indique le côté du texte (gauche, centre, droite), la ligne indique la place dans celui-ci : le haut se situe avant le texte, le milieu au milieu de celui-ci, le bas après le texte. Cliquez sur une case pour la choisir.
- **Le texte entoure l'image** permet au texte de contourner l'image au lieu de la laisser au-dessus et en dessous. À gauche ou à droite, le texte longe l'image. Dans la colonne du milieu, le texte se trouve des deux côtés : quelques blocs de texte (environ 400 caractères, deux blocs au minimum) à cette rangée deviennent deux colonnes, la première partie à gauche de l'image et le reste à droite, de sorte que le texte se lit de haut en bas dans la colonne de gauche puis continue dans celle de droite. Deux images placées au même endroit se suivent l'une au-dessus de l'autre entre les colonnes. Lorsque le texte n'a pas la place pour deux colonnes (un seul paragraphe, ou des blocs déjà pris par la bande ci-dessus), l'image occupe un bloc à elle seule, centrée.
- **Largeur** est la largeur de l'image en pourcentage de la largeur du texte. **Automatique** conserve la taille de l'image (jamais plus large que le texte ; une image avec du texte des deux côtés fait 30 %). Une image que le texte longe fait au plus 60 %, et une image avec du texte des deux côtés au plus 40 %, afin que le texte garde un peu de place.
  La hauteur suit les proportions de l'image.
- Le bouton corbeille retire l'image de la partie.

![La position d'une image : au milieu, le texte la contournant](../../images/images-position.png)

**Aperçu comme dans le lecteur** (un bouton de la boîte de dialogue, et la même entrée dans le menu de la partie lorsque celle-ci comporte des images) affiche la partie comme la [visionneuse](publishing.md) la présente, avec le style de texte du livre : les images là où vous les avez placées, et un bouton pour un écran large ou étroit (téléphone). Il utilise le texte actuel de l'éditeur, enregistré ou non.

![L'aperçu : le texte en deux colonnes avec l'image entre elles](../../images/images-preview.png)

L'éditeur de l'histoire n'utilise pas la position : il affiche les images sous le texte de la partie, sous forme de liste. La position est utilisée par la visionneuse et par les [exports](export.md#images).

L'emplacement d'une image dans le texte ne dépend pas de nombres enregistrés avec elle : le texte est découpé entre ses blocs (paragraphes, listes, citations et blocs de code ne sont jamais coupés), de sorte que les images restent à leur place lorsque vous modifiez le texte. Le milieu est l'endroit entre deux blocs le plus proche du milieu du texte ; une image que le texte contourne en bas est placée avant le dernier bloc, afin qu'au moins ce bloc longe l'image. Les images placées au même endroit gardent l'ordre de la liste ; celles qui se suivent au même endroit (et ne laissent pas le texte les contourner) se placent côte à côte lorsqu'elles tiennent. Le PDF et Word font cela pour les images qui ont une largeur.

Les images ne font **pas partie du texte**. Le modèle ne les voit jamais, elles ne figurent pas dans le prompt, elles ne comptent pas comme jetons et elles ne modifient pas les [résumés](summaries.md). Elles ne sont pas non plus du Markdown : un lien `![](...)` écrit dans le texte n'est pas une image de la partie ; utilisez plutôt **Images**.

À savoir :

- **Régénérer** conserve les images de la partie, même lorsque la génération échoue. **Variante** écrit une nouvelle partie (une nouvelle version à côté de l'ancienne), qui commence sans images. **Créer une branche** copie les images dans la nouvelle partie.
- Les images appartiennent à votre compte. Dans la [visionneuse](publishing.md), les lecteurs d'un livre publié les voient aussi, à moins que vous ne décochiez **Montrer les images aux lecteurs** dans l'onglet *About* du livre.
- Une [sauvegarde](backups.md) mémorise les images que possède une partie, mais pas les fichiers des images. La restauration d'une sauvegarde dans un autre compte ou une autre installation donne des parties sans leurs images.
- Retirer une image d'une partie la détache seulement de celle-ci. Le fichier reste et figure toujours dans les [Ressources](../resources.md), où vous pouvez le télécharger, le remplacer ou le supprimer. Le supprimer là-bas le retire de chaque partie qui l'utilise. Le fichier lui-même reste dans le dossier de données.

<a id="summaries"></a>

## Résumés

Le bouton **Résumés** de la barre inférieure (à gauche du menu des réglages ⚙) ouvre la vue d'ensemble des résumés de la branche, où ils peuvent être modifiés, supprimés et fusionnés en méta-résumés. L'info-bulle du bouton indique combien de jetons les résumés utilisés occupent dans le prompt. Voir [Résumés](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Quelle part du contexte occupe l'histoire

Le menu des réglages (⚙) contient **Jetons de la branche**. Il montre, avant toute génération, ce que l'histoire de la branche active occupe dans le prompt : une barre pour l'histoire **sans résumés** (toutes les parties) et **avec résumés** (les résumés et les parties qui suivent la plus récente), ainsi qu'un tableau avec les parties, les jetons et la part du **budget du prompt** (le contexte du modèle du livre moins la place réservée à la réponse). Seuls les résumés qui décrivent encore l'histoire comptent ; ceux qui ne correspondent plus sont signalés par leur nom et sont abandonnés lorsqu'une partie est générée. Le lore, le modèle de système et les instructions du tour prennent de la place sur ce même budget. Sans modèle dont la taille de contexte est connue, le tableau n'affiche pas de parts.

![Jetons de la branche](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Quelles entrées du lore ont été utilisées

**Utilisation du lore** dans le menu des réglages (⚙, **Analyse**) indique quelles entrées du lorebook du livre ont été utilisées par les générations, et lesquelles ne l'ont jamais été.
Ainsi, un lorebook peut être élagué selon ce qu'il fait réellement, et non au hasard. Chaque génération enregistre les entrées qui ont été intégrées à son prompt
(ainsi que les jetons de chacune) ; la fenêtre additionne ces données sur les parties :

- **Parties** choisit ce qui est compté : la **Branche active**, ou **Toutes les branches**, c'est-à-dire chaque branche et chaque variante du livre.
- **Depuis la partie** et **Jusqu'à la partie** limitent la plage, numérotée comme dans le plan (la première partie d'une branche est la 1, et dans toutes les branches le numéro correspond à la profondeur de la partie dans l'arbre) ; laissez-les vides pour le début et la fin.
- **Afficher** garde les entrées **Jamais utilisée**, **Parfois utilisée** ou **Toujours utilisée** dans les parties comptées.

![La fenêtre Utilisation du lore](../../images/lore-usage.png)

Le tableau comporte une ligne par entrée des lorebooks du livre, les plus utilisées en premier : son lorebook, si elle a été jamais, parfois ou toujours
utilisée, dans combien de parties (parmi les parties ayant un enregistrement, avec le pourcentage), les jetons qu'elle a coûtés dans l'ensemble de ces parties et par utilisation, ainsi que la première et la
dernière partie qui l'ont utilisée. Cliquez sur un en-tête pour trier. Une entrée qui a été utilisée auparavant mais qui n'est plus dans les lorebooks du livre (supprimée, déplacée, ou
dont le lorebook est désactivé ou n'appartient plus au livre) reste dans la liste, marquée *n'est plus dans le lorebook* ; une entrée supprimée n'a plus de nom.

Seules les parties ayant un enregistrement sont comptées : une partie saisie à la main, écrite avant que Marginalia ne conserve cet enregistrement, ou écrite alors que le livre n'avait aucun lorebook n'en a pas, et la fenêtre indique combien il y en a. Les entrées **Toujours utilisée** pourraient être des entrées constantes, tandis que les entrées **Jamais utilisée** sont des candidates à la suppression ou à un meilleur filtre ; [**Aperçu de l'activation**](../lorebooks/entries.md#preview-activation) dans l'éditeur de lorebook montre pourquoi une entrée ne se déclenche pas actuellement.

<a id="text-styles"></a>

## Styles de texte

Le menu des réglages (⚙) dans la barre du bas contient **Apparence → Changer les styles du texte**, un menu avec une entrée par style, triées par nom.
Pour savoir comment créer un style, voir le [guide de mise en forme](styling-guide.md).
Un style détermine l'aspect du texte des parties : police, taille, couleurs, alignement, titres... L'application fournit deux
styles qui ne peuvent pas être modifiés : **Normal** (l'apparence de l'application) et **Book** (un style de lecture proche du livre, celui qu'utilise la [visionneuse](publishing.md)). Vous pouvez créer les vôtres dans [Réglages → Styles](../account.md#styles).

- La première entrée, **Default (...)**, suit votre *style par défaut* (celui marqué dans *Réglages → Styles*, Normal si vous n'en avez choisi aucun). Un nouveau livre n'a pas de style propre et l'affiche.
- Choisir une autre entrée enregistre le style **dans le livre**, ce qui remplace le style par défaut pour ce livre uniquement ; l'entrée cochée est celle que le livre utilise actuellement. **Default** annule ce choix.
- Un style que vous supprimez revient à votre style par défaut dans les livres qui l'utilisaient.

![Apparence, Changer les styles du texte : les styles au choix](../../images/story-style-menu.png)

Dans le style **Book**, les paragraphes sont justifiés et les mots longs sont coupés automatiquement par une césure, à l'aide du dictionnaire de la **Langue** du livre (définie dans l'onglet [À propos](managing-books.md#the-about-tab)). La césure est assurée par le navigateur ;
pour quelques langues rares, il n'existe pas de dictionnaire et le texte n'est tout simplement pas coupé.

Les livres qui avaient l'ancien interrupteur *Changer les styles du texte* activé continuent d'afficher le style Book jusqu'à ce que vous choisissiez une autre entrée.

<a id="highlighting-generated-text"></a>

## Surligner le texte généré

Le menu des réglages (⚙) propose aussi l'option cochable **Surligner le texte généré**. Lorsqu'elle est activée, les mots écrits par un modèle sont surlignés dans chaque partie, afin que ce que vous avez ajouté ou réécrit se remarque. Marginalia suit le texte à travers les [versions de la partie](version-history.md) : chaque version est superposée à la précédente, et les mots qu'elles ont en commun gardent leur auteur. Ainsi, une phrase que vous avez tapée dans un texte généré reste la vôtre, quel que soit le nombre de fois où le modèle réécrit le reste. Une partie que vous n'avez jamais modifiée est surlignée en entier ; une partie que vous avez écrite vous-même n'a rien de surligné. Ce qu'un agent a écrit compte comme celui du modèle. Ce réglage n'est pas enregistré : il est désactivé à nouveau lorsque la page est rouverte.

Une partie dont [l'historique est désactivé](version-history.md) n'a pas de versions à suivre : elle est comparée au texte généré par le modèle en premier lieu. Le surlignage fonctionne aussi sur le [téléphone](../phone.md).

Le même calcul donne le ratio : l'onglet **Infos** du livre contient **Paternité de la branche** avec un bouton **Calculer**, qui indique quel pourcentage des mots de la branche active vous avez écrit et quel pourcentage a été généré par un modèle. [L'export](export.md) peut aussi terminer le fichier par une déclaration à ce sujet.

![Texte généré surligné et menu des réglages](../../images/highlight-generated.png)

<a id="commands"></a>

## Commandes

Sous le récit, à côté du menu, se trouve une **ligne de commande** : tapez `/` pour voir les commandes, choisissez-en une et appuyez sur **Enter** (ou sur le bouton de lecture) pour l'exécuter. Ce que renvoie une commande s'affiche dans une petite fenêtre au-dessus de la ligne. Consultez [Commandes](../commands/index.md) pour savoir comment écrire les arguments, ce qu'affiche l'aide et comment arrêter une commande qui prend du temps. La ligne de commande n'est pour l'instant disponible que dans l'onglet récit de la mise en page ordinateur.
<a id="search-and-replace"></a>

## Recherche et remplacement

Le menu des réglages (⚙) contient aussi **Effacer l'historique du livre** et **Vider la corbeille de l'historique**, voir [Historique des versions](version-history.md#deleting-versions).

Le menu des réglages (⚙) contient aussi **Rechercher et remplacer**, qui modifie un mot, un nom ou une phrase dans toute
l'histoire en une seule fois. La fenêtre comporte un champ **Rechercher** et un champ **Remplacer par**, ainsi que ces options :

![Le menu des réglages avec Rechercher et remplacer](../../images/story-settings-menu.png)

![La fenêtre Rechercher et remplacer avec une recherche saisie](../../images/search-replace-dialog.png)

| Option | Par défaut | Signification |
|---|---|---|
| **Rechercher par expression régulière** | désactivée | Le texte recherché est une [expression régulière](../regular-expressions.md) et le remplacement peut utiliser ses groupes (`$1`). Sinon, le texte est recherché tel qu'il est saisi. |
| **Respecter la casse** | activée | Un texte n'est trouvé que dans la casse saisie (*John* ne trouve pas *john*). Désactivée : la casse n'a pas d'importance. Verrouillée tant que **Rechercher par expression régulière** est activée. |
| **Rechercher dans les résumés** | activée | Les [résumés](summaries.md) sont aussi recherchés et modifiés. |
| **Rechercher dans toutes les branches** | désactivée | Toutes les [branches](branches-and-story-tree.md) sont recherchées, sinon seule la branche active l'est. |
| **Revalider tous les résumés** | activée | Après le remplacement, chaque [résumé](summaries.md) du livre est de nouveau valide, voir plus bas. |

Avec une expression régulière, la casse est toujours prise telle qu'elle est saisie ; commencez-la par `(?i)` pour l'ignorer. Appuyez sur **Rechercher**
(le bouton à droite des champs ; sur le téléphone, sous eux). La barre de progression indique l'avancement et **Arrêter**
à côté l'interrompt. Une recherche ne modifie rien.

Le tableau liste chaque partie de l'histoire et chaque résumé qui contient une correspondance, avec son type et son identifiant. Ouvrez une ligne pour voir les
correspondances qu'elle contient : le texte autour de la correspondance tel qu'il est et tel qu'il sera après le remplacement. Une correspondance ou un remplacement trop long pour être affiché est écrit *[texte recherché]* ou *[texte de remplacement]*.

![Les correspondances d'une recherche, la première partie ouverte](../../images/search-replace-found.png)

Appuyer sur **Entrée** dans le champ **Rechercher** ou **Remplacer par** a le même effet que **Rechercher**.

Une fois la recherche terminée, les champs et les options sont verrouillés et le bouton **Remplacer** est activé. **Rechercher à nouveau** oublie
ce qui a été trouvé et déverrouille la fenêtre, afin que vous puissiez modifier la recherche. **Remplacer** modifie l'histoire : la fenêtre est entièrement figée
(même **Arrêter** ne fonctionne pas) jusqu'à la fin, puis elle est de nouveau vide. Les parties remplacées sont marquées comme modifiées, comme si vous les aviez modifiées vous-même.

Un remplacement dans le texte qu'un [résumé](summaries.md#when-summaries-become-outdated) décrit rendrait le résumé périmé.
Avec **Revalider tous les résumés** activée, Marginalia recalcule la valeur de contrôle de chaque résumé du livre (ceux de
toutes les branches, ceux que le remplacement n'a pas touchés, et ceux qui étaient déjà périmés), de sorte qu'ils sont tous valides
ensuite. Cela rend aussi valide un résumé périmé sans que son texte soit relu ; utilisez-la donc lorsque vous savez que les résumés disent toujours la bonne chose.

Avec l'option désactivée, seuls les résumés couverts par la recherche sont mis à jour. Ceux qu'elle ne couvre pas (résumés de
branches non recherchées, qui partagent leurs parties antérieures avec la branche active, ou tous les résumés lorsque les résumés ne sont pas recherchés) deviennent périmés. S'il en existe, Marginalia indique combien et demande si l'on continue. Un
résumé déjà périmé le reste.

![L'avertissement concernant les résumés qui deviendraient périmés](../../images/search-replace-warning.png)

Dans la [mise en page téléphone](../phone.md), la même fenêtre place les champs et les boutons les uns sous les autres, et le tableau n'a qu'une
colonne qui affiche le type et l'identifiant, puis, dessous, la correspondance et le remplacement.

La [vue d'ensemble des résumés](summaries.md#search-and-replace-in-the-summaries) propose la même fenêtre, pour les résumés seulement.

La même recherche et le même remplacement peuvent être lancés depuis la [ligne de commande](../commands/index.md#commands-that-come-with-marginalia)
avec `/search-replace-parts` ; la commande demande confirmation avant de remplacer.

<a id="importing-into-the-book"></a>

## Importer dans le livre

Le menu des réglages (⚙) contient **Importer** (sous **Import et export**). C'est la fenêtre de l'onglet Livres qui crée un nouveau livre à partir d'un fichier (voir [Gérer les livres](managing-books.md#creating-a-book)), mais ici, pour le livre dans lequel vous vous trouvez : les parties sont **ajoutées à l'histoire**
à la place. La **Source** est l'une des archives zip de fichiers `.md`, un document (Word, LibreOffice, RTF, Markdown, texte brut, EPUB), le zip d'un coffre Obsidian ou le zip d'un projet Scrivener ; chacun a le même texte et les mêmes options que là-bas
(le nombre de mots par partie et le niveau de titre des chapitres), et une ligne indique ce que l'import va produire. Les discussions de SillyTavern
et de LM Studio ne sont pas proposées : ce sont un arbre de branches, qui n'a pas sa place au milieu d'une histoire.

**Placer les parties** indique où elles vont :

- **À la fin de l'histoire** : après la dernière partie de la branche active (la première partie d'un livre vide).
- **Avant une partie** ou **Après une partie** : choisissez la partie dans la liste (les numéros sont les emplacements dans la branche active, avec le
  début du texte). Les parties sont placées entre deux parties : tout ce qui suivait l'emplacement, toutes ses branches, suit les nouvelles
  parties, et aucune partie n'est modifiée ni générée à nouveau. La fin de l'histoire reste où elle était (l'histoire compte d'autant de parties de plus). Les
  [résumés](summaries.md) qui couvrent l'emplacement deviennent périmés, car ils décrivent l'histoire sans les nouvelles parties : la fenêtre des résumés les affiche et les refait.

![La fenêtre d'import d'un livre](../images/book-import-into.png)

Les parties d'une archive sont les fichiers dans l'ordre de leurs noms, les nombres à l'intérieur des noms étant comparés comme des nombres (`2.md`
avant `10.md`), y compris dans les dossiers de l'archive. Les dossiers, les fichiers cachés (`.name`), le dossier `__MACOSX` et les fichiers qui ne sont pas
`.md` sont ignorés. Le fichier est le texte de la partie tel quel ; il n'est ni envoyé au modèle ni modifié, et toutes les parties comptent comme écrites par vous.

L'import affiche une barre de progression avec le nombre de parties traitées. **Arrêter** y met fin après la partie en cours d'enregistrement : les parties déjà ajoutées restent dans l'histoire (l'import n'est pas annulé), et un message indique combien ont été importées.

<a id="after-an-import"></a>

### Après un import

Lorsque les parties sont dans le livre, une fenêtre **Import terminé** propose les deux opérations qui facilitent l'écriture d'une longue histoire (un livre qui vient d'être importé comme nouveau est d'abord ouvert, et la fenêtre apparaît devant lui). Elles sont aussi dans le menu des réglages (⚙, **Analyse**), afin de pouvoir les faire plus tard ou à nouveau :

- **Créer les résumés...** rédige les résumés de ce qui a été importé, voir [Créer des résumés](summaries.md#create-summaries). Faites-le en premier : un agent
  verrouille le livre pendant son travail.
- **Prendre des notes de lore avec un agent...** lance un [agent](../agents.md) qui lit l'histoire et écrit les personnages, lieux, objets et
  termes qu'il trouve comme entrées du lorebook du livre, voir [Notes de lore](../agents.md#lore-notes-of-an-imported-book).

**Pas maintenant** ferme la fenêtre sans rien faire.

![La fenêtre après un import](../../images/import-finished.png)

Le même import dans un lorebook est décrit dans [Import et export](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exporter l'histoire

Le menu des réglages (⚙) contient aussi **Exporter l'histoire**, qui enregistre la branche active dans un fichier texte, Markdown, HTML, Word, PDF ou EPUB.
Voir [Exporter un livre](export.md).

<a id="what-the-model-gets"></a>

## Ce que reçoit le modèle

Pour chaque nouvelle partie, Marginalia envoie :

1. Le **modèle maître** comme message système : rôle, entrées du [lorebook](../lorebooks/index.md), point de vue,
   temps, style et [résumés](summaries.md). Le prompt de contournement du
   [fournisseur d'inférence](../inference-providers.md#general-settings), s'il est activé, passe en premier.
2. Les parties précédentes de la branche active, comme les réponses antérieures du modèle lui-même, autant qu'il en tient dans la
   [limite de contexte](../protocols.md#limits), les plus récentes d'abord. Les parties plus anciennes qui ne tiennent pas sont laissées de côté (sauf si un
   [résumé](summaries.md) les couvre).
3. Le **prompt utilisateur** avec les instructions pour la nouvelle partie.

*Afficher le prompt* sur n'importe quelle partie montre exactement ce qui a été envoyé. Voir [Modèles de prompts du livre](prompts.md) pour les modèles.
