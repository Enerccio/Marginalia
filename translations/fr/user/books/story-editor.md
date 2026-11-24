---
label: Éditeur de l'histoire
order: 90
---

# Éditeur de l'histoire

L'onglet **Story** d'un livre est l'endroit où vous écrivez. Il affiche les parties de la [branche active](branches-and-story-tree.md)
depuis le début jusqu'à la fin.

![L'éditeur de l'histoire](../../images/hero-story-editor.png)

Cette page décrit la disposition sur ordinateur. Sur un téléphone, l'histoire tient en une seule colonne et le plan se
trouve dans un tiroir, voir [Utiliser Marginalia sur téléphone](../phone.md#the-story).

| Zone | |
|---|---|
| **Outline** (à gauche) | Une entrée par partie, `#1`, `#2`... avec l'identifiant de la partie dans la base. Cliquez sur une entrée pour faire défiler jusqu'à la partie (les parties qui la précèdent sont chargées d'abord si elle ne l'est pas encore). L'en-tête indique le nombre de parties de la branche. Une partie qui a un [résumé](summaries.md) porte une petite icône de document après son entrée. L'extension [Chapter Marker](../extensions/chapter-marker.md) affiche ici les titres de chapitres. |
| **Lorebook** (à gauche, à côté de l'Outline) | Un aperçu du savoir de l'univers du livre, avec une zone de recherche et une modification rapide du texte d'une entrée, voir [L'onglet Lorebook](#the-lorebook-tab). |
| **Annotations** (à gauche, à côté de l'Outline) | Les [notes](annotations.md) sur des passages de l'histoire, pour l'ensemble du livre. |
| **Planner** (à gauche, à côté de l'Outline) | Les scènes que vous prévoyez d'écrire, une liste que vous remplissez à la main, voir [L'onglet Planner](#the-planner-tab). |
| **Story** (au centre) | Le texte de chaque partie, avec son menu (☰) et ses métadonnées à droite, et sous elle la barre du bas. |
| **Barre du bas** | Le bouton **Summaries** (icône de liste) et le menu des paramètres (⚙) à gauche, la ligne de commande, et le bouton **+** à droite. |

Voici à quoi ressemble un nouveau livre. Tout cela consiste en des onglets que vous pouvez disposer comme vous le
souhaitez, l'histoire comprise, voir [Disposer les onglets](#arranging-the-tabs). La séparation entre la barre latérale et l'histoire
peut être déplacée au glisser, et Marginalia garde en mémoire l'endroit où vous l'avez laissée (elle est enregistrée avec
le livre) et y revient lorsque vous rouvrez le livre.

L'histoire s'ouvre toujours en bas, à la partie la plus récente. Un long livre n'est pas affiché d'un seul coup : seules les
dernières parties sont chargées, et les précédentes s'ajoutent au-dessus à mesure que vous remontez, sans déplacer la
partie que vous lisez. Les parties situées à l'autre extrémité sont à nouveau retirées, si bien que pas plus que le nombre
fixé par le paramètre **Parts loaded at once in the story** de votre [compte](../account.md) ne reste chargé (une valeur vide les
charge toutes). Si vous faites défiler plus vite que les parties n'arrivent, une marque *Loading earlier parts* (ou
*later parts*) au bord indique qu'il en reste. Cliquer sur une partie dans le plan, ou en trouver une avec la recherche,
charge les parties qui l'entourent, et commencer une nouvelle partie vous ramène à la fin de l'histoire.

<a id="searching-the-story"></a>

### Rechercher dans l'histoire

Au-dessus du plan se trouve un champ de recherche portant sur le texte de la **branche sur laquelle vous êtes** (la
recherche dans l'arbre de l'histoire parcourt toutes les branches). Comme un long livre n'est pas chargé d'un seul coup,
la recherche du navigateur (Ctrl+F) ne peut pas trouver de texte dans les parties non chargées ; celle-ci le peut.

- Tapez ce que vous cherchez ; la recherche démarre dès que vous arrêtez de taper. Plusieurs mots doivent tous se trouver
  dans la même partie ; une expression va entre guillemets doubles (`"old key"`) et `*` représente n'importe quels caractères
  (`light*house`). La casse n'a pas d'importance.
- Chaque endroit où figure un mot est une occurrence, et elles sont numérotées sur l'ensemble de la branche. Sous le champ,
  vous voyez *Match 3 of 27, in part #5* et une barre indiquant où vous en êtes. L'histoire défile jusqu'à l'occurrence et
  charge les parties qui l'entourent si elles ne le sont pas.
- Les boutons **up** et **down** vont à l'occurrence précédente et à la suivante (après la dernière, on revient à la
  première). **Enter** dans le champ fait la même chose que le bouton down, **Shift+Enter** que le bouton up.
- Toutes les occurrences présentes dans les parties chargées sont surlignées dans le texte, celle où vous vous trouvez
  l'étant dans une couleur plus vive. Effacez le champ pour supprimer le surlignage.

Sur téléphone, la recherche se trouve dans le tiroir du plan ; le tiroir se referme lorsqu'il affiche une occurrence, il
faut le rouvrir pour passer à la suivante.

<a id="the-lorebook-tab"></a>

### L'onglet Lorebook

L'onglet **Lorebook** de la barre latérale affiche le savoir du lorebook du livre sans quitter l'histoire, si bien que vous
n'avez pas à changer d'onglet pour vérifier ce qu'une entrée dit ou pour corriger un mot qu'elle contient. Il est relu à
chaque fois que vous revenez à l'onglet *Story*, de sorte que les modifications faites dans l'onglet *Lorebook* du livre
apparaissent aussitôt.

- La **zone de recherche** en haut n'affiche que les entrées dont le texte figure dans leur nom, leur contenu, leur note ou
  leur filtre (la casse n'a pas d'importance).
- Le **crayon** d'une entrée ouvre une petite fenêtre avec son nom, son état d'utilisation, le filtre et la manière dont
  elle est recherchée, le contenu et la note ; **Save** les enregistre (les étiquettes, l'ordre et les réglages d'activation
  se modifient dans l'onglet *Lorebook* du livre). Rien ne peut être modifié pendant qu'un agent travaille sur le lorebook.
- Le **bouton d'agrandissement** (⤢) ouvre l'[éditeur de lorebook en plein écran](../lorebooks/lorebooks.md#editing-in-full-screen) (sur ordinateur uniquement) : celui
  situé à côté de la zone de recherche pour le lorebook, celui d'une entrée pour cette entrée, celui d'un sous-lorebook (avec
  *Include sub lorebooks*) pour ce lorebook. L'onglet est à nouveau chargé lorsque vous fermez l'éditeur.

- Chaque entrée est une ligne avec son nom et le crayon ; la flèche l'ouvre pour afficher son **contenu** (rendu en Markdown)
  et le **texte qui l'active** (le *Filtering Text/Pattern*, avec une note lorsqu'il s'agit d'une expression régulière).
- **Include sub lorebooks** affiche le lorebook et chacun de ses sous-lorebooks (et les leurs, quelle que soit la profondeur)
  sous forme de lignes distinctes, avec leurs entrées en dessous ; cliquez sur la flèche d'un lorebook pour masquer ses
  entrées. Sans cette option, seules les entrées du lorebook du livre sont listées.
- Les entrées et les lorebooks désactivés (ils ne sont pas utilisés pour l'histoire) apparaissent en grisé.
- Un livre sans lorebook affiche une note à la place.

![L'onglet Lorebook avec deux entrées ouvertes](../../images/story-lorebook-tab.png)

Comme tout onglet de l'éditeur, il peut être sorti dans un panneau à part, par glisser-déposer, voir ci-dessous.
<a id="the-planner-tab"></a>

### L'onglet Planner

L'onglet **Planner** est une liste des scènes que vous voulez voir traverser l'histoire, dans l'ordre souhaité. Chaque scène correspond aux détails d'un tour qui l'écrit : un **title** pour la liste, le **scene setting** (le cadre de la scène), le **POV character** (le personnage du point de vue), les **present characters** (les personnages présents) et les **instructions**, soit les quatre champs du formulaire de nouveau tour.

![The Planner tab with four scenes, the first one written](../../images/planner-tab.png)

- **Add scene** ouvre un formulaire pour une nouvelle scène ; elle est ajoutée à la fin de la liste. Le crayon modifie une scène, les flèches la déplacent vers le haut ou vers le bas, la corbeille la supprime.
- Le **+** de la ligne d'une scène **insère une nouvelle scène** juste en dessous, entre cette scène et la suivante.
- Le bouton de fusion d'une scène **la fusionne avec la suivante** : un formulaire s'ouvre avec les détails des deux scènes déjà réunis (les deux titres, les cadres de scène et les instructions l'un sous l'autre, les personnages présents des deux, le personnage POV de la première ; lorsque la seconde a un autre personnage POV, le formulaire l'indique). Modifiez ce qui ne convient pas, puis appuyez sur **Merge** (ou <kbd>Ctrl/Cmd+Enter</kbd>) : les deux scènes ne forment plus qu'une, à la place de la première. L'opération n'est effectuée que si les deux scènes sont cochées.
- **Import from the branch** ajoute une scène pour chaque partie de la branche sur laquelle se trouve le livre (de la première partie à la plus récente) qui contient des détails de tour : son cadre de scène, son personnage POV, ses personnages présents et ses instructions, la première ligne des instructions servant de titre. Les parties sans détails de tour sont laissées de côté, et les scènes sont ajoutées après celles que vous avez déjà, non cochées. Servez-vous-en pour planifier la réécriture d'une histoire que vous avez déjà écrite : importez, fusionnez les tours qui vont ensemble, insérez ce qui manque, puis écrivez les scènes sur une nouvelle branche ou dans une copie du livre.
- La case devant une scène indique qu'elle est **done** (écrite) : le titre est barré et la scène est estompée. Rien ne le vérifie à votre place ; cochez-la lorsque vous avez écrit la scène. La ligne au-dessus de la liste compte le nombre de scènes terminées.
- **Cliquez sur une scène** pour placer ses détails dans le **new turn form** (le formulaire s'ouvre avec le cadre de scène, le POV, les personnages présents et les instructions de la scène ; ce qui s'y trouvait est remplacé), puis appuyez sur **Generate** lorsque vous en êtes satisfait. Sur téléphone, le tiroir se referme d'abord.

Les scènes sont enregistrées avec le livre, elles figurent donc dans ses sauvegardes et ses copies. Un modèle peut aussi ajouter des scènes : le [plot wizard](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de l'extension Arc Outline et un [agent](../agents.md) peuvent lire et modifier le planner au moyen de [commandes](../commands/index.md#the-planner) ; la liste affiche immédiatement ce qu'ils ont ajouté.

![The form for a new scene](../../images/planner-scene-dialog.png)

![A click on a scene fills the new turn form with its details](../../images/planner-new-turn.png)

Comme chaque onglet de l'éditeur, il peut être glissé dans un panneau à part, voir ci-dessous.

<a id="arranging-the-tabs"></a>

### Organiser les onglets

L'éditeur est un ensemble d'onglets : la **Story** elle-même (les parties et la barre inférieure qui les suit), l'**Outline**, le **Lorebook**, ainsi que les onglets que les [extensions](../extensions/index.md) ajoutent (Characters, Arcs, Side Query...). Faites glisser un onglet par son en-tête pour les réorganiser (sur ordinateur uniquement ; le téléphone conserve une seule colonne, avec le plan dans un tiroir). Un nouveau livre affiche l'Outline et le Lorebook dans un panneau à gauche, et la Story dans une colonne à part à droite ; vous pouvez placer la Story où vous voulez, par exemple au centre avec le plan d'un côté et le lore de l'autre, ou au-dessus du plan :

- Déposez-le sur l'une des bandes en pointillés qui apparaissent au-dessus, entre et sous les panneaux d'une colonne pour le **détacher** dans un panneau à part, dans cette colonne ; les panneaux d'une colonne sont empilés les uns sous les autres.
- Déposez-le sur l'une des bandes verticales à gauche, à droite ou entre les colonnes pour le **détacher sur le côté** : il obtient sa propre colonne, à côté des autres. Élargissez l'éditeur avec les séparateurs pour faire de la place à d'autres colonnes.

Les panneaux et les colonnes sont séparés par des séparateurs : faites-les glisser pour donner à chaque panneau la place dont il a besoin. Les dimensions sont enregistrées avec la disposition.

![The sidebar with Characters in a column of its own and Side Query under it](../../images/story-editor-tabs.png)

- Déposez-le sur l'en-tête d'un onglet ou sur le corps d'un autre panneau pour **rejoindre** ce panneau (déposer sur un onglet le place avant celui-ci, déposer sur le corps le place en dernier). Un panneau qui perd son dernier onglet disparaît, et il en va de même pour une colonne sans panneau.

La disposition et les dimensions sont enregistrées avec le livre : elles sont donc identiques à la prochaine ouverture et dans chaque navigateur, de même que l'onglet affiché par chaque panneau. Un onglet d'extension qui n'est pas chargé conserve sa place jusqu'à ce que l'extension revienne.

La story est toujours présente quelque part : son onglet ne peut pas être fermé, et un onglet qui partage un panneau avec d'autres est masqué lorsqu'un autre est sélectionné. Tout ce qui affiche un passage ramène la story au premier plan : cliquer sur une entrée du plan, un résultat de recherche, choisir une branche dans l'[arbre de l'histoire](branches-and-story-tree.md), ou lancer une génération sélectionne d'abord l'onglet **Story**. Pendant qu'une génération prépare son prompt (généralement quelques secondes), les autres onglets sont désactivés ; une fois que le modèle écrit, ils redeviennent utilisables, voir [ci-dessous](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Réduire les onglets

Chaque onglet, sauf la story, possède un petit bouton **⌄** (chevron) à côté de son nom. Il **réduit** l'onglet : celui-ci quitte son panneau (un panneau dont tous les onglets sont réduits disparaît provisoirement) et une icône ronde le représentant apparaît dans une barre sur le bord de l'éditeur : un cercle coloré avec un dessin (une liste pour le plan, un livre pour le lorebook, une bulle de dialogue pour les annotations, une loupe pour Side Query, et ainsi de suite ; un onglet d'extension sans dessin reçoit une ou deux lettres). Survolez l'icône pour voir de quel onglet il s'agit. Rien n'est fermé : l'onglet conserve ce qu'il affichait. Cliquez sur l'icône, l'onglet revient dans son panneau, à la place qu'il occupait parmi les onglets, et il est affiché.

La barre est présente tant qu'un onglet est réduit. Le bouton situé en haut (les flèches circulaires) envoie la barre au côté suivant de l'éditeur, dans le sens des aiguilles d'une montre : gauche, haut, droite, bas. La story ne peut pas être réduite : il y a donc toujours quelque chose à l'écran.

![Minimized tabs in the bar at the edge of the editor](../../images/story-minimized-tabs.png)

Les icônes sont classées par ordre alphabétique des noms des onglets jusqu'à ce que vous le modifiiez : faites glisser une icône sur une autre et elle prend la place de celle-ci. Les onglets réduits, la position de la barre et l'ordre des icônes sont enregistrés avec le livre, en même temps que la disposition et les dimensions.

<a id="the-default-arrangement"></a>

### La disposition par défaut

Un nouveau livre s'ouvre avec la disposition standard (le plan, le lorebook et les annotations à gauche, la story à droite). Pour faire de votre propre disposition la disposition standard, réglez les onglets comme vous le souhaitez (panneaux, dimensions, onglets réduits, côté de la barre), puis choisissez **Save layout as my default** dans le menu des réglages (⚙) de la story. Chaque livre qui n'a pas de disposition propre (un livre que vous n'avez pas réorganisé) s'ouvrira alors avec celle-ci ; un livre que vous avez réorganisé conserve la sienne. La valeur par défaut est conservée dans vos réglages utilisateur.

Pour revenir à la disposition par défaut, choisissez **Restore default layout** dans le menu des réglages (⚙) de la story, puis confirmez. Il s'agit de celle que vous avez enregistrée, ou de la disposition standard si vous n'en avez enregistré aucune. Cela ne fait que replacer les onglets (et oublie les dimensions et les onglets réduits) : vos textes, l'onglet affiché par chaque panneau et les onglets d'extensions restent en place, ces derniers dans le premier panneau.
<a id="writing-the-next-part"></a>

## Rédiger la partie suivante

1. Cliquez sur **+** en bas à droite. Le panneau *New Turn Instructions* s'ouvre.
2. Remplissez les champs. Ils sont tous facultatifs, mais le modèle a besoin d'au moins quelques indications :

   | Champ | |
   |---|---|
   | **Scene Setting** | Où et quand la partie se déroule, par exemple *The lighthouse, a stormy night in late autumn*. |
   | **POV Character** | Le point de vue narratif de la partie. |
   | **Present Characters** | Qui est dans la scène, et dans quel état. |
   | **Instructions** | Ce qui doit se passer dans cette partie. |

   *Scene Setting*, *POV Character* et *Present Characters* sont remplis à partir de la dernière partie : vous ne modifiez que ce qui change.
   *Instructions* est vide à chaque fois.
3. Cliquez sur **Generate**.

![Le panneau New Turn Instructions](../../images/new-turn-instructions.png)

La nouvelle partie apparaît à la fin et se remplit au fur et à mesure que le modèle l'écrit. Pour les [modèles de raisonnement](../inference-providers.md#reasoning-models),
le raisonnement apparaît d'abord sous **View Reasoning**.

Pendant la génération d'une partie, le bouton **+** devient un bouton rouge **Stop**. Arrêter conserve le texte écrit jusqu'alors comme partie.

<a id="while-a-part-is-generated"></a>

### Pendant la génération d'une partie

Pendant les premières secondes, le temps que le prompt soit assemblé à partir du livre, toute la fenêtre du livre attend. Dès que le modèle commence à écrire, seuls la nouvelle partie et les parties qui la précèdent sont bloquées :

- **La nouvelle partie** ne peut pas être modifiée tant qu'elle n'est pas terminée : ni édition, ni menu.
- **Les parties qui la précèdent** restent en place : vous pouvez les modifier, les annoter et les réécrire, mais pas les supprimer, les fusionner ou les embrancher, et l'histoire reste sur sa branche (pas de swipe, pas de changement de branche). Une modification d'une partie antérieure, ou du lore, n'atteint pas la partie en cours d'écriture : son prompt a déjà été assemblé. Elle compte pour la suite.
- **Tout le reste reste disponible** : le lore, les paramètres et le prompt du livre, les onglets de la barre de gauche (side query, arc outline, notes...), la barre de commandes. Une requête au modèle attend une connexion libre du modèle si la génération utilise la seule disponible.
- **En attente** : une deuxième partie, la recherche et remplacement, un import dans le livre et la suppression en masse. Les onglets de l'arbre et des sauvegardes attendent aussi.

Ce que la fenêtre grise est aussi refusé lorsqu'une commande, un agent ou une extension le demande : Marginalia indique ce qui fait obstacle
(*This is in use: …*).

**Quitter le livre arrête la génération.** Le bouton de sortie demande d'abord confirmation ; *Yes* a le même effet que **Stop** : ce qui est arrivé est conservé. Fermer l'onglet du navigateur fait de même, et le navigateur demande confirmation avant de fermer ou de recharger l'onglet pendant la génération d'une partie. Une courte coupure du réseau, ou un ordinateur portable fermé pendant une minute, ne compte pas comme une sortie : la page revient et affiche ce qui est arrivé entre-temps.

Lorsque le modèle renvoie une erreur (serveur injoignable, mauvaise clé API, contexte trop long...), l'erreur s'affiche. Si aucun texte n'est encore arrivé, l'histoire reste telle qu'elle était : une nouvelle partie ou un swipe est supprimé, et une partie régénérée retrouve son texte précédent. Le texte arrivé avant l'erreur est conservé, comme avec **Stop**. Corrigez le problème, puis générez à nouveau.

La manière dont les champs entrent dans le prompt est définie par le [User Prompt](prompts.md#user-prompt) du livre. Avec le user prompt par défaut, le modèle les reçoit sous les noms *Current Location & Time*, *Point of View Character*, *Characters Currently Present / State* et *Plot Specifications for the Next Part*.

!!!
Le texte est écrit en Markdown : `*italics*`, `**bold**`, `# Heading` et ainsi de suite sont affichés mis en forme. Une partie avec un titre `#` est un chapitre pour l'extension [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Rédiger les instructions

Vous ne savez pas ce qui doit se passer ensuite ? **Draft Instructions**, sous le champ *Instructions* du panneau, laisse le modèle rédiger les instructions à votre place, comme vous le feriez. Une fenêtre s'ouvre avec :

| | |
|---|---|
| **Guidance** | Ce que la partie doit traiter : une idée vague, une direction (« elle le confronte au sujet de la lettre »), ou rien du tout, et le modèle poursuit l'histoire là où elle mène. Il commence avec ce que vous avez déjà saisi dans *Instructions*. |
| **Plan of the Story** | Seulement lorsqu'un élément en fournit un. L'[Arc Outline](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) le fait, avec les données du chapitre en cours. Lecture seule ; le modèle le suit. |

**Draft Instructions** lance le modèle et diffuse la réponse dans **Drafted Instructions**, que vous pouvez modifier ou vider avant de continuer ; **Draft Again** tente une nouvelle fois, **Stop** conserve ce qui est arrivé. Rien n'est modifié tant que vous ne cliquez pas sur **Use as Instructions** : le texte remplace alors les *Instructions* du panneau (les autres champs ne sont pas touchés) et le panneau se rouvre, prêt pour **Generate**.

Le modèle reçoit l'histoire jusqu'ici (la partie plus ancienne sous forme de [résumés](summaries.md), comme lors d'une génération), le cadre de la scène, le POV et les personnages présents qui figurent dans le panneau, le lore de la dernière partie, vos dernières instructions comme exemples de votre manière d'écrire, votre guidance et le plan. Il ne voit pas les entrées de votre lorebook qu'une nouvelle partie activerait. La requête est le [prompt d'impersonation](prompts.md#impersonate-prompt). Le modèle est celui du livre ; la rédaction consomme des tokens comme toute requête.

![Rédaction des instructions de la partie suivante](../../images/impersonate-dialog.png)

La même chose est la commande [`/impersonate`](../commands/index.md), qui renvoie le texte. Les [agents](../agents.md) ne l'ont pas : ils écrivent l'histoire avec leurs propres instructions.

<a id="the-settings-menu"></a>

## Le menu des paramètres

Le menu des paramètres (⚙) dans la barre du bas regroupe ce qui agit sur toute l'histoire. **Search and replace** est en haut ; le reste se trouve dans des sous-menus (y compris sur téléphone, sauf les éléments réservés au bureau) :

| Sub menu | Items |
|---|---|
| **Appearance** | **Change Styles For Text**, **Highlight Generated Text** et **Annotations in the margin** (bureau uniquement, pour le dernier) |
| **Analysis** | **Annotate a range of parts with AI**, **Create summaries**, **Take lore notes with an agent**, **Automatic annotation**, **Phrase watch list**, **Branch Tokens**, **Lore Usage**, **Statistics** |
| **Import & export** | **Import**, **Export story** (et les éléments des extensions qui importent ou exportent, comme **Export instructions**) |
| **Cleanup** | **Clear history of the book**, **Empty trash of the history**, **Mass delete** |
| **Layout** (bureau uniquement) | **Save layout as my default**, **Restore default layout** |

Les éléments apportés par les [extensions](../extensions/index.md) se trouvent dans le sous-menu qui leur correspond, ou à la fin du menu. Les raccourcis des éléments sont dans *Settings → Keyboard shortcuts*, où un élément est nommé avec son sous-menu (*Analysis › Statistics*). Les sections ci-dessous indiquent où se trouve chaque élément.
<a id="the-part-menu"></a>

## Le menu d'une partie

Chaque partie a un menu (☰) dans son coin supérieur droit. **Edit**, **Discard Changes**, **Regenerate**, **Swipe**, **Redraft**, **History** et **Images** se trouvent directement dans le menu, **Delete** est son dernier élément, et le reste se trouve dans quatre sous-menus : **Annotations** (*Annotate part*, *Annotate with AI*), **Summary** (*Generate summary* / *View summary*, *Delete summary*), **Inspect** (*Show Prompt*, *Compare Prompt*, *Token Usage*, *Lore Usage*) et **Branch & merge** (*Branch Story*, *Merge into previous part*). Un sous-menu qui ne contient rien pour la partie n'est pas affiché. Le Reviewer ajoute son sous-menu **Review** après *Summary*. Les éléments :

| Élément | |
|---|---|
| **Edit** | Rend le texte modifiable. **Save** (le même élément de menu) l'enregistre. Lancer une génération ou quitter l'onglet enregistre aussi les modifications en cours. |
| **Discard Changes** | Uniquement pendant la modification du texte : ferme le champ sans enregistrer, la partie conserve le texte de sa dernière sauvegarde. Si vous avez modifié le texte, on vous le demande d'abord. |
| **Regenerate** | Réécrit la dernière partie, en remplaçant son texte. Seulement sur la dernière partie. |
| **Swipe** | Écrit une autre version de la dernière partie et conserve l'ancienne comme branche séparée. Seulement sur la dernière partie. Voir [Branches et arbre de l'histoire](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Redraft** | Permet au modèle de réécrire cette partie à partir de vos instructions, sur n'importe quelle partie, voir [ci-dessous](#redrafting-a-part). Pas pendant la modification du texte. |
| **History (n)** | Les [versions du texte](version-history.md) de la partie, avec le nombre de versions : les comparer, en remettre une en place, en nommer une. Pas pendant la modification du texte. |
| **Annotate part** | Ajoute une [note](annotations.md#making-an-annotation) sur la partie entière. Pour annoter des mots, sélectionnez-les puis faites un clic droit (**Annotate selection**). |
| **Annotate with AI** | Permet à un modèle de [relire la partie](annotations.md#letting-a-model-annotate) et de proposer des notes. Pas pendant la modification du texte. Pour plusieurs parties à la fois, utilisez **Annotate a range of parts with AI** dans le menu des paramètres (⚙), et pour chaque nouvelle partie **Automatic annotation** (voir [Annotations](annotations.md#annotating-every-new-part-automatically)). |
| **Branch Story** | Démarre une nouvelle branche à partir de cette partie, voir [Branches et arbre de l'histoire](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Merge into previous part** | Ajoute le texte de cette partie à la partie qui la précède, voir [ci-dessous](#merging-a-part-into-the-previous-one). Pas sur la première partie. |
| **Show Prompt** | Affiche le prompt exact envoyé au modèle pour cette partie. |
| **Token Usage** | Montre comment le contexte du modèle a été utilisé pour cette partie : un graphique des sections du prompt (instructions du modèle, lore, résumés, histoire jusqu'ici, vos instructions, place réservée à la réponse) et la taille de chaque message envoyé au modèle. Si une partie de l'histoire n'a pas pu tenir, il indique combien de parties ont été laissées de côté. Les parties écrites avant que cette information soit enregistrée n'ont pas de graphique. |
| **Lore Usage** | Liste les entrées du [lorebook](../lorebooks/index.md) qui ont été intégrées au prompt de cette partie, avec les tokens que chacune a coûtés et ce qui l'a trouvée (le prompt, ou le contenu d'une autre entrée lorsque le lorebook explore plus en profondeur). Les parties saisies à la main, écrites avant que cette information soit enregistrée, ou écrites pour un livre sans lorebook indiquent qu'aucune donnée n'a été enregistrée. Pour l'ensemble du livre, utilisez **Lore Usage** dans le menu des paramètres (⚙), voir [ci-dessous](#which-lore-entries-were-used). |
| **Images** | Attache des images à la partie, voir [Images](#images). |
| **Generate summary** / **View summary** | Résume l'histoire jusqu'à cette partie, ou affiche le résumé, voir [Résumés](summaries.md). |
| **Delete summary** | Supprime le résumé de cette partie. Pour un [méta-résumé](summaries.md#meta-summaries), on vous demande s'il faut le défaire ou le supprimer complètement. |
| **Delete** | Supprime la partie, voir [ci-dessous](#deleting-a-part). |

*Regenerate* et *Swipe* utilisent les détails du tour de la dernière partie (voir [ci-dessous](#turn-details)). Pour modifier ce qui doit se passer, modifiez-les d'abord.

![Le menu d'une partie, avec le sous-menu Inspect ouvert](../../images/story-part-menu.png)

![Token Usage d'une partie](../../images/token-usage.png)

<a id="part-metadata"></a>

## Métadonnées de la partie

À côté de chaque partie :

| | |
|---|---|
| **Order #n \| DB ID: m** | La position de la partie dans la branche, et son identifiant dans la base de données. |
| **Model** | Le fournisseur d'inférence qui a écrit la partie. |
| **Tokens: x (y)** | `x` tokens de texte dans la partie, `y` tokens dans le prompt qui l'a produite. |
| **Words** | Nombre de mots dans la partie. |
| **Turn Details** | Les instructions à partir desquelles la partie a été écrite. |

<a id="turn-details"></a>

### Détails du tour

**Turn Details** ouvre le *Scene Setting*, le *POV Character*, les *Present Characters* et les *Instructions* à partir desquels la partie a été écrite. Pour la dernière partie, ils peuvent être modifiés (les changements sont enregistrés immédiatement) et sont utilisés par *Regenerate* et *Swipe*. Pour les parties précédentes, ils sont en lecture seule.

![Détails du tour d'une partie](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Modifier le texte

Le texte généré vous appartient : vous pouvez le modifier. Choisissez **Edit** dans le menu de la partie, changez le texte, puis choisissez **Save**. Les parties modifiées sont envoyées au modèle telles que vous les avez éditées.

Modifier une partie couverte par un [résumé](summaries.md) rend ce résumé obsolète ; Marginalia le remarque à la génération suivante et supprime le résumé. Pour éviter que cela n'arrive par surprise, **Save** vérifie d'abord le nouveau texte : si celui-ci rendrait des résumés obsolètes, une fenêtre indique combien, et propose **Back to editing** (rien n'est enregistré, l'éditeur reste ouvert avec votre texte), **Save anyway** (les résumés deviennent obsolètes) et **Save and revalidate summaries** (le texte est enregistré et les résumés restent valides, pour une modification qui ne change pas ce qu'ils disent, comme une coquille). La vérification se fait pour **Save** (le menu, le raccourci et le bouton de l'éditeur) ; les enregistrements automatiques, par exemple lorsque vous lancez une génération avec l'éditeur ouvert, ne demandent rien.

<a id="keyboard-shortcuts"></a>

## Raccourcis clavier

Sur un ordinateur (la [disposition pour téléphone](../phone.md) n'en a pas), la boucle d'écriture fonctionne au clavier :

| Touche | |
|---|---|
| `+` | Ouvre le panneau *New Turn Instructions* avec le curseur dans *Instructions*. |
| `Ctrl+Enter` | Génère à partir du panneau ouvert. `Esc` le ferme ; le clavier revient à l'histoire. |
| `Ctrl+.` | Arrête la génération en cours. |
| `s` / `r` | **Swipe** / **Regenerate** la dernière partie. |
| `e` | Ouvre l'éditeur de la partie la plus visible à l'écran (la dernière partie si aucune ne l'est). |
| `Ctrl+S` | Enregistre la partie en cours de modification (également lorsque le curseur est dans son texte). |
| `/` | Place le curseur dans la barre de commandes (l'onglet de l'histoire s'affiche d'abord). |
| `Ctrl+Shift+F` | Ouvre l'onglet *Outline* et place le curseur dans la recherche de la branche (`Enter` / `Shift+Enter` vont à la correspondance suivante / précédente). |
| `Ctrl+Shift+H` | Ouvre la fenêtre *Search and replace* du livre. |
| `a` | **Annotate selection** : ajoute une note sur le texte sélectionné dans une partie. |
| `Alt+PageDown` / `Alt+PageUp` | Affiche l'onglet suivant / précédent, en parcourant les onglets de toutes les feuilles dans l'ordre de la disposition, puis recommence. |

Une touche sans `Ctrl`, `Alt` ou `Meta` (comme `+`, `s`, `r`, `e`, `a`, `/`) ne fonctionne que lorsqu'aucun champ de texte n'a le focus, afin de ne jamais gêner la frappe. Les raccourcis fonctionnent dans l'onglet de l'histoire du livre ouvert, et non lorsqu'une autre fenêtre est ouverte par-dessus. Les boutons affichent leur touche dans l'info-bulle, et le menu de la dernière partie l'affiche après *Edit*, *Regenerate* et *Swipe*. Sur un Mac, `Cmd` fonctionne là où une touche indique `Ctrl` (sauf si vous avez attribué cette touche `Cmd` à une autre action). Vous pouvez modifier chaque touche dans [Settings → Keyboard shortcuts](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Supprimer une partie

**Delete** retire la partie de l'histoire. Les parties qui la suivaient restent et continuent à partir de la partie qui la précède. Supprimer une partie au milieu de l'histoire la retire donc simplement du récit. Supprimer la dernière partie fait de la partie précédente la nouvelle fin de la branche.

<a id="deleting-many-parts-at-once"></a>

### Supprimer plusieurs parties à la fois

Le menu des paramètres (⚙) contient **Mass delete**. L'activer place une case à cocher à gauche de chaque partie et affiche, sous l'histoire, une barre indiquant le nombre de parties cochées ainsi qu'un bouton **Delete**. Cochez les parties à supprimer (les coches sont conservées pendant le défilement, car l'histoire ne charge qu'une fenêtre de parties). **Maj + clic** sur une case à cocher coche (ou décoche) toutes les parties situées entre celle-ci et la dernière case sur laquelle vous avez cliqué. Une partie cochée apparaît teintée en rouge. Appuyez ensuite sur **Delete** : après une confirmation, les parties sont supprimées une à une, derrière une barre de progression qu'il est impossible d'interrompre. Chacune est supprimée comme une partie seule : les parties qui suivaient une partie supprimée continuent à partir de la partie qui la précède, et les parties supprimées vont à la corbeille. Ensuite, l'histoire est de nouveau affichée et la suppression multiple est désactivée. **Cancel**, dans la barre, ou un nouveau choix de **Mass delete**, la désactive sans rien supprimer. Lancer une génération la désactive également. Une partie cochée sur laquelle travaille une fenêtre ou une traduction reste dans l'histoire, et Marginalia indique quelle fenêtre ou quelle traduction en est responsable.

![Suppression multiple avec quelques parties cochées](../../images/story-mass-delete.png)

<a id="redrafting-a-part"></a>

## Réécrire une partie

**Regenerate** et **Swipe** ne travaillent que sur la dernière partie et la réécrivent entièrement. **Redraft** travaille sur n'importe quelle partie, même une ancienne au milieu de l'histoire, et conserve ce que vous ne demandez pas de modifier. Choisissez **Redraft** dans le menu de la partie :

1. Écrivez ce qui doit changer dans **Instructions for the redraft** : *ajouter davantage de dialogue entre les deux*, *rallonger le combat*, *supprimer l'explication de la carte*, *garder la fin telle quelle*. Des instructions vides réécrivent la partie avec les mêmes événements.
2. Choisissez **Length** si la partie doit devenir **Shorter** ou **Longer** (la valeur par défaut est **Same length**) : une phrase qui le demande est ajoutée après vos instructions (voir [Redraft Shorter Prompt and Redraft Longer Prompt](prompts.md#redraft-shorter-and-longer)). Choisissez **Redraft**. Le texte s'affiche au fur et à mesure dans **Redrafted text** ; **Stop** l'interrompt plus tôt. Choisissez **Redraft again** pour un nouvel essai, avec d'autres instructions si vous le souhaitez. **Differences** montre ce qui a changé par rapport à la partie, mot à mot. Lorsque la partie comporte des [annotations](annotations.md) ouvertes, **Deal with the open annotations of the part** (coché par défaut) les transmet également au modèle, et **Apply** les marque comme résolues.
3. Le texte réécrit est un champ que vous pouvez encore modifier (son icône dans le coin le fait grandir pour tenir tout le texte), et **Differences** suit vos modifications. **Apply** remplace le texte de la partie par ce texte ; **Close** le jette. Rien ne change dans le livre avant **Apply**.

![La fenêtre de réécriture : les instructions, le nouveau texte et les différences](../../images/redraft-dialog.png)

Le modèle reçoit les [invites de réécriture](prompts.md#redraft-prompt) : la partie, vos instructions, l'histoire qui précède la partie (l'histoire plus ancienne sous forme de [résumés](summaries.md) lorsque des résumés valides existent, le reste sous forme de texte, la partie la plus récente en premier lorsque tout ne tient pas), autant de l'histoire qui suit la partie qu'il en tient (jusqu'à un quart de la place disponible), le contexte que les livres de référence fournissent à cette partie à présent (recalculé pour le tour de la partie, de sorte qu'une modification d'une entrée parvient à la réécriture ; le contexte avec lequel la partie a été écrite n'est utilisé que si le livre ne comporte aucun livre de référence), le point de vue, le temps verbal et le style du livre, ainsi que le cadre de la scène, le personnage au point de vue, les personnages présents et les instructions du tour dont provient la partie. L'histoire qui suit la partie est là pour que le nouveau texte mène toujours à la suite : les événements dont dépendent les parties suivantes doivent être conservés.

La partie garde sa place, son résumé, ses images et la manière dont elle a été générée ; le texte, ses mots et ses jetons sont remplacés, et la partie est considérée comme modifiée. Appliquer une réécriture est une modification, comme **Save** dans l'éditeur : les [résumés](summaries.md) qui décrivent l'ancien texte deviennent donc périmés, et Marginalia indique combien. Un résumé porté par la partie elle-même en fait partie.

Lorsqu'un résumé décrit la partie (la partie en a un, ou une partie plus récente de la branche en a un), la fenêtre affiche la case **Revalidate the summaries this redraft makes outdated**, décochée par défaut. Cochez-la si les résumés disent toujours la bonne chose (la réécriture a modifié la formulation, pas ce qui se passe) : **Apply** recalcule alors leur empreinte et ils restent valides. Laissez-la décochée si les événements ont changé, et rédigez de nouveau les résumés.

La fenêtre ne bloque pas l'histoire, voir [Travailler à côté de la fenêtre](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Travailler à côté des fenêtres de réécriture et d'annotation

Sur un ordinateur, les fenêtres **Redraft** et **Annotate with AI** s'affichent à droite de l'histoire et la laissent utilisable : vous pouvez lire, faire défiler et modifier l'histoire pendant que le modèle écrit. Faites glisser une fenêtre par son titre pour la déplacer, ou par son bord pour la redimensionner. La flèche (⌄) du titre replie une fenêtre dans le [panneau des exécutions](#the-panel-of-the-runs) en bas à gauche de la page : le modèle continue, **Open** (ou un clic sur l'entrée) fait revenir la fenêtre, et **Stop** arrête le modèle (une fois celui-ci terminé, **Close** ferme la fenêtre). Sur un téléphone, les fenêtres recouvrent toujours l'histoire, mais elles se replient de la même manière.

![La fenêtre de réécriture à côté de l'histoire pendant que le modèle écrit, et l'exécution dans le panneau en bas à gauche](../../images/redraft-beside-story.png)

**Close et Stop sont deux choses différentes.** Le **Stop** de la fenêtre arrête le modèle, et la fenêtre reste avec ce qui est arrivé. **Close** pendant que le modèle travaille n'arrête pas celui-ci : la fenêtre se replie dans le panneau et le modèle poursuit sa file d'attente ; sa réponse est dans la fenêtre lorsque vous la rouvrez. Fermer une fenêtre dont le modèle a terminé, ou **Close** dans le panneau, la supprime ; fermer le livre ou l'onglet du navigateur arrête aussi une fenêtre repliée. Il en va de même pour les autres fenêtres qui attendent un modèle : un **résumé** (ainsi que *Create summaries* et *Rewrite all summaries*), le [Reviewer](../extensions/reviewer.md), la description du livre, les générateurs de contexte et de plan, les générateurs d'images et de manga, et le rédacteur de personnages de Beta Readers. Les fenêtres *Studio* et *Convert* du livre audio ne bloquent pas non plus la page ; les fermer n'a jamais arrêté la conversion.

Pendant qu'une fenêtre est ouverte (ou repliée) sur une partie, celle-ci affiche ce qu'elle fait à côté de son menu (*Redrafting · The book*), et **Regenerate**, **Swipe**, **Redraft**, **Annotate with AI**, **Merge into previous** et **Delete** sont désactivés pour cette partie, afin que la fenêtre ne perde pas sa partie. Modifier la partie reste possible. Fermer le livre ferme ses fenêtres et arrête leurs modèles. Une partie sur laquelle un [traducteur](../extensions/real-language-translator.md) travaille affiche *Translating the speech of the part...* de la même manière, et deux traducteurs ne travaillent jamais sur une même partie en même temps : le second indique que la partie est déjà utilisée.

- Lorsque vous **Apply** une réécriture de la partie entière et que la partie a été modifiée après l'ouverture de la fenêtre (vous l'avez éditée entre-temps), Marginalia demande d'abord confirmation : **Differences** compare alors la partie telle qu'elle est maintenant avec la réécriture, pour que vous voyiez ce que l'application remplacerait ; choisissez **Apply anyway** ou **Cancel**. Une réécriture qui [s'applique d'elle-même](annotations.md#annotating-every-new-part-automatically) ne le fait pas lorsque la partie a été modifiée : la fenêtre attend alors votre décision.
- Lorsque la partie a été supprimée avant que vous ne **Apply** la réécriture ou n'ajoutiez les annotations, la fenêtre le signale, n'écrit rien et se ferme.
<a id="the-panel-of-the-runs"></a>

### Le panneau des exécutions

Tout ce qui travaille actuellement sur vos livres avec un modèle est listé en bas à gauche de chaque page : une génération, une réécriture ou une annotation, une traduction par le [Real Language](../extensions/real-language-translator.md) ou le [Conlang](../extensions/conlang-translator.md), une réécriture des résumés, le Knowledge Tracker qui lit l'histoire, une conversion [Audiobook](../extensions/audiobook.md), une commande de la barre de commandes qui prend du temps (*Draft instructions*, un résumé... : sa fenêtre reste sur le côté et se replie comme les autres), un [agent](../agents.md) au travail. Les fenêtres dans lesquelles vous attendez une réponse y figurent aussi, pendant que le modèle répond : **Impersonate**, l'assistant d'aide, une traduction rapide, le concepteur Conlang, une discussion de Side Query (et le nommage de son onglet), Beta Readers ou le plot wizard. Le panneau liste vos exécutions de **toutes vos pages ouvertes** (un autre onglet, le téléphone), et pas seulement celles lancées sur cette page. Rien ne tourne, rien ne s'affiche.

Le fenêtre du Knowledge et du State Tracker est aussi une entrée du panneau : fermée pendant que le modèle travaille, elle se replie dedans.

![Le panneau des exécutions ouvert : la réécriture repliée dedans, avec la fin du texte du modèle, Open et Stop](../../images/runs-panel.png)

- **Fermé**, il se présente comme une petite barre : la première exécution, le nombre d'autres (*Translating the speech of the part... · 2 more*) et une barre mobile pendant qu'un modèle travaille. Cliquez dessus pour ouvrir la liste ; elle s'ouvre toute seule quand vous repliez une fenêtre dedans.
- **Ouvert**, chaque exécution montre ce qu'elle fait, le livre, sa durée, ce qu'elle attend et la fin de ce que le modèle écrit :
  - *Waiting for the model: 2 requests before this one* quand le modèle est occupé par d'autres requêtes (voir *Connections* dans [Fournisseurs d'inférence](../inference-providers.md)) ;
  - *The model is thinking...* ou *The model is writing...*, avec la fin de sa réflexion ou de sa réponse en dessous ;
  - *Waits for you* quand l'exécution attend une décision de votre part (une réécriture à appliquer, une proposition du Knowledge Tracker à examiner, la question du Prompt Inspector avant une génération, une commande du plot wizard à autoriser).
- **Stop** arrête l'exécution, où qu'elle ait été lancée. **Open**, ou un clic sur l'entrée, réaffiche une fenêtre repliée (seulement sur la page où elle a été ouverte) ou l'exécution elle-même ; le bouton est **Review** quand l'exécution attend votre décision. Une exécution du Knowledge Tracker ouvre son livre sur cette page (quand aucun autre livre n'est ouvert ici ; sinon, on vous demande d'abord de fermer celui-là), passe à l'onglet Knowledge et montre la revue. Le **Stop** d'un agent le met en pause. **Close** ferme une fenêtre définitivement (et arrête son modèle).
- **Ce qui est terminé** reste dans la liste pendant deux heures, sous ce qui tourne, avec le résultat (*Completed*, *Failed*) et la durée : une traduction, des résumés, ce qui a suivi une partie, la session d'un agent, un import, un travail qui a continué après que vous avez quitté sa page. **Close** le retire de la liste. Une génération de l'histoire et les exécutions que vous avez arrêtées ne sont pas conservées.
- Sur un ordinateur, vous pouvez déplacer le panneau par sa poignée (⋮ à sa gauche) ; il reste à l'endroit où vous le laissez, dans ce navigateur. Dans la moitié basse de la page, il monte quand vous l'ouvrez, comme dans son coin, et il reste toujours dans la page. Quand il se trouve à l'endroit du bouton rond de l'[assistant d'aide](../administration/help-assistant.md) (sur un livre, les deux sont dans le coin inférieur gauche), le bouton s'écarte et revient quand la place est libre.

Quand une exécution lancée se termine après la fermeture de sa page, une autre page ouverte de votre compte envoie sa [notification](../account.md#notifications) (pour les exécutions qui en ont une, comme la réécriture des résumés).

**Ce qui est refusé pendant qu'une exécution tourne.** La partie suivante n'est pas générée tant qu'une fenêtre de [réécriture](#redrafting-a-part) du livre est ouverte (fermez-la, ou appliquez-la, d'abord) ou qu'une commande de la barre de commandes est sur le point de générer. Une partie n'est pas fusionnée dans la partie précédente tant qu'une fenêtre ou une traduction travaille sur celle-ci, et la recherche et le remplacement laissent de côté la partie qu'une génération est en train d'écrire (elle est comptée comme ignorée). Un [agent](../agents.md) ne démarre pas sur un livre ouvert dans une fenêtre ou sur lequel quelque chose d'autre travaille. Quand plusieurs exécutions attendent un même modèle, votre prochaine génération passe avant l'annotation, la traduction ou l'audio de la partie précédente.

<a id="redrafting-only-a-passage"></a>

### Réécrire seulement un passage

Pour réécrire quelques phrases en laissant le reste de la partie intact, sélectionnez-les dans le texte de la partie et faites un **clic droit** (un appui long sur un téléphone) : le menu propose **Redraft selection**. (Le menu ☰ de la partie n'est pas proposé pour cela, car l'ouvrir peut faire perdre la sélection dans certains navigateurs.) La fenêtre fonctionne comme décrit plus haut, avec deux différences : elle affiche le **Selected text**, et la réécriture ne concerne que le texte qui remplace la sélection. Le modèle reçoit la partie entière avec la sélection marquée, ainsi que l'histoire et le lore autour d'elle, pour que le nouveau passage se raccorde aux phrases qui le précèdent et le suivent. **Differences** compare la sélection avec le texte réécrit ; **Apply** met le texte réécrit à la place de la sélection et conserve le reste de la partie, y compris l'espace ou le saut de ligne aux bords de la sélection. Quand des [annotations](annotations.md) sont ouvertes, la case à cocher ne propose que celles dont le passage chevauche la sélection. Si la partie a été modifiée après votre sélection du texte (dans une autre fenêtre), **Apply** refuse et vous devez resélectionner le passage. Pendant que la partie est en cours d'édition, c'est la sélection de l'éditeur qui est utilisée, et la modification est d'abord enregistrée.

<a id="what-follows-a-generated-part"></a>

### Ce qui suit une partie générée

Quand une partie que vous avez générée (depuis la page ou la barre de commandes) est complète, le livre peut faire lui-même d'autres opérations sur elle, dans cet ordre :

1. **Annotate** : l'[annotation automatique](annotations.md#annotating-every-new-part-automatically) examine la partie.
2. **Mark phrases** : la [liste de surveillance des phrases](annotations.md#watching-for-phrases) annote les phrases qu'elle trouve (sans modèle).
3. **Redraft** : la partie est réécrite pour tenir compte des notes.
4. **Translate** : les traducteurs [Real language](../extensions/real-language-translator.md) et [Conlang](../extensions/conlang-translator.md) traduisent ses répliques.
5. **Knowledge** : le [Knowledge Tracker](../extensions/knowledge-tracker.md) la lit.
6. **Audio** : l'[Audiobook](../extensions/audiobook.md) en fait un audio.

Seules les étapes activées pour le livre s'exécutent. Elles tournent sur le serveur, la page peut donc être fermée, et elles forment une seule entrée du [panneau des exécutions](#the-panel-of-the-runs) : *After part 14: Annotate ✓ · Redraft … · Translate*, avec le texte du modèle et un seul **Stop**. Ce qui modifie le texte passe en premier, pour que le tracker et l'audio reçoivent le texte final. Une étape qui attend votre décision (des notes ou une réécriture à choisir) ouvre sa fenêtre, et l'entrée propose **Review** pour l'ouvrir dans une autre page.

- **Stop**, la fermeture d'une fenêtre qui attend votre décision, ou une étape en échec met fin à la suite : ce qui a été fait reste, le reste n'est pas exécuté, et un message indique ce qui reste à faire à la main (chaque étape a sa propre action sur la partie).
- Pendant que la **réécriture** tourne, la partie suivante ne peut pas être générée (on vous indique quelle exécution en est la cause) ; pendant que les autres étapes tournent, elle le peut.
- La partie ne peut pas être supprimée tant que son texte n'est pas définitif ; la générer à nouveau met fin à ce qui suivait sa version précédente.
- Un [agent](../agents.md) exécute ces étapes par ses propres commandes : rien ne suit une partie qu'un agent a générée.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionner une partie dans la précédente

Un modèle qui s'est arrêté au milieu d'une phrase (limite de réponse, connexion coupée) ne peut pas être fiablement invité à « continuer » le même texte. Écrivez plutôt la partie suivante (*continue à partir de l'endroit où la dernière partie s'est arrêtée*), puis réunissez les deux parties avec **Merge into previous part** dans le menu de la seconde. Une revue s'ouvre :

- **Join the texts with** : une ligne vide, un saut de ligne, un espace ou rien. La valeur est présélectionnée d'après la fin du texte précédent : un paragraphe après un texte qui se termine par une phrase, un espace après un texte coupé en plein milieu.
- La **scene setting**, le **POV character**, les **present characters** et les **instructions** des deux tours, combinés dans des champs modifiables. Ce que les deux tours ont en commun au début n'est gardé qu'une fois (« Write the duel. Keep it short » et « Write the duel. Add rain » donnent le premier, puis « Add rain » sur la ligne suivante).
- **Data** : ce que les deux parties conservent, valeur par valeur (par exemple les [variables](../templates/macros.md) de l'histoire et les données des extensions), avec l'indication de la valeur identique, différente ou présente seulement sur l'une des parties. La partie fusionnée garde la valeur de la **partie la plus récente** (c'est elle qui décrit l'état actuel de l'histoire), sauf si vous choisissez la valeur de la partie précédente, ou si vous laissez la valeur de côté. Les valeurs identiques sont masquées, sauf si vous les demandez.

![La revue avant qu'une partie soit fusionnée dans la précédente](../../images/part-merge-dialog.png)

La partie précédente garde sa place, son résumé et la façon dont elle a été générée (prompt, modèle, durées). Elle reçoit le texte réuni, le raisonnement réuni et les images, et ses mots et ses jetons sont recomptés. La partie que vous avez fusionnée est supprimée (elle se trouve dans la [corbeille](../trash.md)), et ce qui la suivait se poursuit à partir de la partie précédente. Les résumés qui couvrent les deux parties deviennent périmés, comme après une modification. L'[historique](version-history.md#backups-copies-and-the-trash) de la partie fusionnée est supprimé, et la partie conservée reçoit le texte réuni comme version **Merged**. Une partie ne peut pas être fusionnée lorsqu'elle est la première partie du livre, ou lorsque la partie qui la précède a d'autres suites (variantes de la partie) : supprimez-les ou transformez-les d'abord en branche.

## Images

Une partie peut avoir des images : une carte, le portrait d'un personnage, une illustration. Choisissez **Images** dans le menu de la partie pour ouvrir la liste des images de la partie.

![La fenêtre Images d'une partie](../../images/images-dialog.png)

- **Add image** (ou déposez un fichier dessus) importe une image PNG, JPEG, GIF ou WebP d'au plus 10 Mo. Les images WebP sont converties en PNG à l'import, car les fichiers Word et PDF ne peuvent pas contenir de WebP.
- **Caption** s'affiche sous l'image. Il peut être vide.
- **Position in the part** est une grille de 3 × 3. La colonne est le côté du texte (gauche, centre, droite), la ligne est la place dans ce côté : le haut est avant le texte, le milieu est au milieu du texte, le bas est après le texte. Cliquez sur une case pour la choisir.
- **Text flows around the image** laisse le texte s'écouler autour de l'image au lieu de la placer au-dessus et au-dessous. À gauche ou à droite, le texte longe l'image. Dans la colonne du centre, le texte est des deux côtés : quelques blocs du texte (environ 400 caractères, deux blocs au minimum) à cette ligne deviennent deux colonnes, la première partie à gauche de l'image et le reste à droite, de sorte que le texte se lit en descendant la colonne de gauche, puis continue dans celle de droite. Deux images au même endroit se placent l'une au-dessus de l'autre, entre les colonnes. Lorsque le texte n'a pas la place de deux colonnes (un seul paragraphe, ou des blocs déjà pris par la bande du dessus), l'image est un bloc à part, centré.
- **Width** est la largeur de l'image en pourcentage de la largeur du texte. **Automatic** garde la taille de l'image (jamais plus large que le texte ; une image avec du texte des deux côtés fait 30 %). Une image que le texte longe fait au plus 60 %, et une image avec du texte des deux côtés au plus 40 %, pour que le texte garde de la place. La hauteur suit les proportions de l'image.
- Le bouton de corbeille retire l'image de la partie.

![La position d'une image : au milieu, avec le texte qui s'écoule autour d'elle](../../images/images-position.png)

**Preview as in the viewer** (un bouton de la fenêtre, et la même entrée dans le menu de la partie lorsque la partie a des images) affiche la partie telle que la [visionneuse](publishing.md) la montre, avec le style de texte du livre : les images là où vous les avez placées, et un interrupteur pour un écran large ou étroit (téléphone). Il utilise le texte tel qu'il est dans l'éditeur à l'instant, enregistré ou non.

![L'aperçu : le texte en deux colonnes avec l'image entre elles](../../images/images-preview.png)

L'éditeur de l'histoire n'utilise pas la position : il affiche les images sous le texte de la partie, sous forme de liste. La position sert à la visionneuse et aux [exports](export.md#images).

L'endroit du texte où se place une image ne dépend pas de nombres enregistrés avec elle : le texte est découpé entre ses blocs (paragraphes, listes, citations et blocs de code ne sont jamais coupés), de sorte que les images restent là où elles doivent être lorsque vous modifiez le texte. Le milieu est l'emplacement entre deux blocs le plus proche du milieu du texte ; une image que le texte longe en bas est placée avant le dernier bloc, afin qu'au moins ce bloc longe l'image. Les images placées au même endroit gardent l'ordre de la liste ; celles qui sont voisines au même endroit (et qui ne laissent pas le texte s'écouler autour d'elles) se mettent en rangée lorsqu'elles tiennent. Le PDF et Word font cela pour les images qui ont une largeur.

Les images ne font **pas partie du texte**. Le modèle ne les voit jamais, elles ne sont pas dans le prompt, elles ne comptent pas comme jetons et elles ne modifient pas les [résumés](summaries.md). Ce ne sont pas non plus du Markdown : un lien `![](...)` écrit dans le texte n'est pas une image de la partie ; utilisez plutôt **Images**.

À savoir :

- **Regenerate** garde les images de la partie, même lorsque la génération échoue. **Swipe** écrit une nouvelle partie (une nouvelle version à côté de l'ancienne), qui commence sans images. **Branch Story** copie les images dans la nouvelle partie.
- Les images appartiennent à votre compte. Dans la [visionneuse](publishing.md), les lecteurs d'un livre publié les voient aussi, sauf si vous décochez **Show the images to readers** dans l'onglet *About* du livre.
- Une [sauvegarde](backups.md) se souvient des images qu'a une partie, mais pas des fichiers d'image. Restaurer une sauvegarde dans un autre compte ou une autre installation donne des parties sans leurs images.
- Retirer une image d'une partie ne fait que la détacher de cette partie. Le fichier reste et figure toujours dans les [Ressources](../resources.md), où vous pouvez le télécharger, le remplacer ou le supprimer. Le supprimer là-bas le retire de chaque partie qui l'utilise. Le fichier lui-même reste dans le dossier de données.

<a id="summaries"></a>

## Résumés

Le bouton **Summaries** de la barre du bas (à gauche du menu des réglages ⚙) ouvre la vue d'ensemble des résumés de la branche, où ils peuvent être modifiés, supprimés et fusionnés en méta-résumés. L'info-bulle du bouton indique le nombre de jetons que prennent les résumés utilisés dans le prompt. Voir [Résumés](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Quelle part du contexte occupe l'histoire

Le menu des réglages (⚙) contient **Branch Tokens**. Il montre, avant toute génération, ce que l'histoire de la branche active occupe dans le prompt : une barre pour l'histoire **sans résumés** (toutes les parties) et **avec résumés** (les résumés et les parties qui suivent la plus récente), ainsi qu'un tableau des parties, de leurs jetons et de leur part du **budget du prompt** (le contexte du modèle du livre, moins la place réservée à la réponse). Seuls les résumés qui décrivent encore l'histoire sont comptés ; ceux qui ne correspondent plus sont nommés, et ils sont écartés lorsqu'une partie est générée. L'univers (lore), le modèle système et les instructions du tour prennent aussi de la place dans ce même budget. Sans modèle ayant une taille de contexte, le tableau n'affiche pas de parts.

![Branch Tokens](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Quelles entrées du lorebook ont été utilisées

**Lore Usage** dans le menu des réglages (⚙, **Analysis**) indique quelles entrées du lorebook du livre ont été utilisées par les générations, et lesquelles ne l'ont jamais été. Vous pouvez ainsi élaguer un lorebook d'après ce qu'il fait réellement, et non par supposition. Chaque génération enregistre les entrées qui sont entrées dans son prompt (ainsi que les tokens de chacune), et la fenêtre additionne ces données sur les parties :

- **Parts** choisit ce qui est compté : la **Active branch**, ou **All branches**, c'est-à-dire toutes les branches et toutes les variantes du livre.
- **From part** et **To part** limitent la plage, numérotée comme dans le plan (la première partie d'une branche est 1 ; dans toutes les branches, le numéro correspond à la profondeur de la partie dans l'arbre). Laissez-les vides pour le début et la fin.
- **Show** garde les entrées qui étaient **Never used**, **Sometimes used** ou **Always used** dans les parties comptées.

![La fenêtre Lore Usage](../../images/lore-usage.png)

Le tableau a une ligne pour chaque entrée des lorebooks du livre, la plus utilisée en premier : son lorebook, si elle a été jamais, parfois ou toujours utilisée, dans combien de parties (parmi celles qui ont un enregistrement, avec le pourcentage), les tokens qu'elle a coûtés dans toutes ces parties et par utilisation, puis la première et la dernière partie qui l'ont utilisée. Cliquez sur un en-tête pour trier. Une entrée qui a été utilisée auparavant, mais qui ne figure plus dans les lorebooks du livre (supprimée, déplacée, ou dont le lorebook est désactivé ou n'est plus celui du livre), reste dans la liste, marquée *not in the lorebook now* ; une entrée supprimée n'a plus de nom.

Seules les parties qui ont un enregistrement sont comptées. Une partie tapée à la main, écrite avant que Marginalia ne conserve cet enregistrement, ou écrite quand le livre n'avait pas de lorebook, n'en a pas, et la fenêtre indique combien il y en a. Les entrées *Always used* pourraient être des entrées constantes. Les entrées *Never used* sont candidates à la suppression ou à un meilleur filtre. [**Preview activation**](../lorebooks/entries.md#preview-activation) dans l'éditeur de lorebook montre pourquoi une entrée ne se déclenche pas maintenant.

<a id="text-styles"></a>

## Styles de texte

Le menu des réglages (⚙) dans la barre du bas contient **Appearance → Change Styles For Text**, un menu avec une entrée par style, triées par nom. Pour savoir comment créer un style, voir le [guide de mise en forme](styling-guide.md). Un style détermine l'apparence du texte des parties : police, taille, couleurs, alignement, titres... L'application fournit deux styles qui ne peuvent pas être modifiés : **Normal** (l'apparence de l'application) et **Book** (un style de lecture proche d'un livre, celui qu'utilise la [visionneuse](publishing.md)). Vous pouvez créer les vôtres dans [Settings → Styles](../account.md#styles).

- La première entrée, **Default (...)**, suit votre *style par défaut* (celui marqué dans *Settings → Styles*, Normal si vous n'en avez pas choisi). Un nouveau livre n'a pas de style propre et l'affiche.
- Choisir une autre entrée enregistre le style **dans le livre**, ce qui remplace le style par défaut pour ce livre seulement. L'entrée cochée est celle que le livre utilise actuellement. **Default** annule ce choix.
- Un style supprimé revient à votre style par défaut dans les livres qui l'utilisaient.

![Appearance, Change Styles For Text : les styles au choix](../../images/story-style-menu.png)

Dans le style **Book**, les paragraphes sont justifiés et les mots longs sont coupés automatiquement, selon le dictionnaire de la **Language** du livre (réglée dans l'[onglet About](managing-books.md#the-about-tab)). La coupure des mots est faite par le navigateur. Pour quelques langues rares, il n'existe pas de dictionnaire, et le texte n'est tout simplement pas coupé.

Les livres qui avaient l'ancien interrupteur *Change Styles For Text* activé continuent d'afficher le style Book jusqu'à ce que vous choisissiez une autre entrée.

<a id="highlighting-generated-text"></a>

## Mise en évidence du texte généré

Le menu des réglages (⚙) propose aussi l'option à cocher **Highlight Generated Text**. Lorsqu'elle est active, les mots écrits par un modèle sont mis en évidence dans chaque partie, pour que ce que vous avez ajouté ou réécrit ressorte. Marginalia suit le texte à travers les [versions de la partie](version-history.md) : chaque version est superposée à la précédente, et les mots qu'elles ont en commun gardent leur auteur. Ainsi, une phrase que vous avez tapée dans un texte généré reste à vous, peu importe le nombre de fois où le modèle réécrit le reste. Une partie que vous n'avez jamais modifiée est entièrement mise en évidence ; une partie que vous avez écrite vous-même n'a rien de mis en évidence. Ce qu'a écrit un agent est compté comme celui du modèle. L'option n'est pas enregistrée : elle est de nouveau désactivée lorsque la page est rouverte.

Une partie dont l'[historique est désactivé](version-history.md) n'a pas de versions à suivre : elle est comparée au texte que le modèle a généré en premier. La mise en évidence fonctionne aussi sur le [téléphone](../phone.md).

Le même décompte donne la proportion : l'onglet **Info** du livre contient **Authorship of the branch**, avec un bouton **Calculate**. Il indique quel pourcentage des mots de la branche active vous avez écrits, et quel pourcentage a été généré par un modèle. [L'export](export.md) peut aussi terminer le fichier par une déclaration à ce sujet.

![Texte généré mis en évidence et menu des réglages](../../images/highlight-generated.png)

<a id="commands"></a>

## Commandes

À côté du menu, sous l'histoire, se trouve une **command line** : tapez `/` pour voir les commandes, choisissez-en une et appuyez sur **Enter** (ou sur le bouton de lecture) pour l'exécuter. Ce que renvoie une commande s'affiche dans une petite fenêtre au-dessus de la ligne. Voir [Commandes](../commands/index.md) pour savoir comment écrire les arguments, ce qu'affiche l'aide et comment arrêter une commande qui prend du temps. Pour l'instant, la ligne de commande n'est disponible que dans l'onglet histoire de la disposition pour ordinateur.
<a id="search-and-replace"></a>

## Rechercher et remplacer

Le menu des réglages (⚙) contient aussi **Clear history of the book** et **Empty trash of the history**, voir [Historique des versions](version-history.md#deleting-versions).

Le menu des réglages (⚙) contient aussi **Search and replace**, qui remplace un mot, un nom ou une expression dans toute l'histoire en une seule fois. La fenêtre comprend les champs **Search for** et **Replace with**, ainsi que ces options :

![Le menu des réglages avec Search and replace](../../images/story-settings-menu.png)

![La fenêtre Search and replace avec une recherche saisie](../../images/search-replace-dialog.png)

| Option | Par défaut | Signification |
|---|---|---|
| **Search by regex** | désactivée | Le texte recherché est une [expression régulière](../regular-expressions.md) et le remplacement peut utiliser ses groupes (`$1`). Sinon, le texte est recherché tel qu'il est saisi. |
| **Case sensitive** | activée | Un texte n'est trouvé que dans la casse saisie (*John* ne trouve pas *john*). Désactivée : la casse n'a pas d'importance. Verrouillée tant que **Search by regex** est activée. |
| **Search in summaries** | activée | Les [résumés](summaries.md) sont aussi recherchés et modifiés. |
| **Search in all branches** | désactivée | Toutes les [branches](branches-and-story-tree.md) sont recherchées, sinon seulement la branche active. |
| **Revalidate all summaries** | activée | Après le remplacement, chaque [résumé](summaries.md) du livre est de nouveau valide, voir plus bas. |

Avec une expression régulière, la casse est toujours prise telle qu'elle est saisie ; commencez-la par `(?i)` pour l'ignorer. Appuyez sur **Search** (le bouton à droite des champs ; sur téléphone, en dessous d'eux). La barre de progression indique l'avancement, et **Stop** à côté l'interrompt. Une recherche ne modifie rien.

Le tableau liste chaque partie de l'histoire et chaque résumé qui contient une occurrence, avec leur type et leur identifiant. Ouvrez une ligne pour voir les occurrences qu'elle contient : le texte autour de l'occurrence tel qu'il est, et tel qu'il sera après le remplacement. Une occurrence ou un remplacement trop long pour être affiché est écrit *[searched text]* ou *[replace text]*.

![Les occurrences d'une recherche, la première partie ouverte](../../images/search-replace-found.png)

Appuyer sur **Enter** dans le champ **Search for** ou **Replace with** a le même effet que **Search**.

Une fois la recherche terminée, les champs et options sont verrouillés et le bouton **Replace** est activé. **Search again** oublie ce qui a été trouvé et déverrouille la fenêtre, pour que vous puissiez modifier la recherche. **Replace** modifie l'histoire : la fenêtre est entièrement gelée (même **Stop** ne fonctionne pas) jusqu'à la fin de l'opération, puis elle est de nouveau vide. Les parties remplacées sont marquées comme modifiées, comme si vous les aviez éditées vous-même.

Un remplacement dans le texte qu'un [résumé](summaries.md#when-summaries-become-outdated) décrit rendrait ce résumé périmé. Avec **Revalidate all summaries** activée, Marginalia recalcule la valeur de contrôle de chaque résumé du livre (ceux de toutes les branches, ceux que le remplacement n'a pas touchés, et ceux qui étaient déjà périmés), pour qu'ils soient tous valides ensuite. Cela rend aussi valide un résumé périmé sans que son texte soit examiné ; utilisez donc cette option quand vous savez que les résumés disent toujours la bonne chose.

Avec l'option désactivée, seuls les résumés couverts par la recherche sont mis à jour. Ceux qu'elle ne couvre pas (résumés de branches non recherchées, qui partagent leurs parties antérieures avec la branche active ; ou tous les résumés, si les résumés ne sont pas recherchés) deviennent périmés. S'il en existe, Marginalia indique combien et demande si vous voulez continuer. Un résumé déjà périmé le reste.

![L'avertissement sur les résumés qui deviendraient périmés](../../images/search-replace-warning.png)

Dans la [disposition téléphone](../phone.md), la même fenêtre place les champs et les boutons les uns sous les autres, et le tableau n'a qu'une colonne qui affiche le type et l'identifiant, avec, en dessous, l'occurrence et le remplacement.

La [vue d'ensemble des résumés](summaries.md#search-and-replace-in-the-summaries) propose la même fenêtre, mais pour les résumés seulement.

La même recherche et le même remplacement peuvent être lancés depuis la [ligne de commande](../commands/index.md#commands-that-come-with-marginalia) avec `/search-replace-parts` ; la commande demande confirmation avant de remplacer.

<a id="importing-into-the-book"></a>

## Importer dans le livre

Le menu des réglages (⚙) contient **Import** (sous **Import & export**). C'est la fenêtre de l'onglet Books qui crée un nouveau livre à partir d'un fichier (voir [Gérer les livres](managing-books.md#creating-a-book)), mais ici l'import concerne le livre où vous êtes : les parties sont **ajoutées à l'histoire** à la place. La **Source** peut être l'archive zip de fichiers `.md`, un document (Word, LibreOffice, RTF, Markdown, texte brut, EPUB), le zip d'un coffre Obsidian ou le zip d'un projet Scrivener. Chacune a le même texte et les mêmes options que dans la fenêtre de l'onglet Books (le nombre de mots par partie et le niveau de titre des chapitres), et une ligne indique ce que l'import va produire. Les discussions de SillyTavern et de LM Studio ne sont pas proposées : ce sont des arbres de branches, qui n'ont pas leur place au milieu d'une histoire.

**Put the parts** indique où les parties sont placées :

- **At the end of the story** : après la dernière partie de la branche active (la première partie d'un livre vide).
- **Before a part** ou **After a part** : choisissez la partie dans la liste (les numéros sont les emplacements dans la branche active, avec le début du texte). Les parties sont insérées entre deux parties : tout ce qui suivait l'emplacement, y compris toutes ses branches, suit les nouvelles parties, et aucune partie n'est modifiée ni générée à nouveau. La fin de l'histoire reste à sa place (l'histoire compte autant de parties de plus). Les [résumés](summaries.md) qui couvrent l'emplacement deviennent périmés, car ils décrivent l'histoire sans les nouvelles parties : la fenêtre des résumés les affiche et permet de les refaire.

![La fenêtre d'import d'un livre](../images/book-import-into.png)

Les parties d'une archive sont les fichiers, classés selon leurs noms, les nombres dans les noms étant comparés comme des nombres (`2.md` avant `10.md`), y compris dans les dossiers de l'archive. Les dossiers, les fichiers cachés (`.name`), le dossier `__MACOSX` et les fichiers qui ne sont pas `.md` sont ignorés. Le fichier est le texte de la partie tel quel : il n'est ni envoyé au modèle ni modifié, et toutes les parties comptent comme écrites par vous.

L'import affiche une barre de progression avec le nombre de parties traitées. **Stop** l'arrête après la partie en cours d'enregistrement : les parties déjà ajoutées restent dans l'histoire (l'import n'est pas annulé), et un message indique combien ont été importées.

<a id="after-an-import"></a>

### Après un import

Une fois les parties dans le livre, une fenêtre **Import finished** propose deux actions qui facilitent l'écriture d'une longue histoire (un livre qui vient d'être importé comme nouveau livre est ouvert en premier, et la fenêtre passe devant lui). Ces deux actions sont aussi dans le menu des réglages (⚙, **Analysis**), pour être faites plus tard ou à nouveau :

- **Create summaries...** écrit les résumés de ce qui a été importé, voir [Créer des résumés](summaries.md#create-summaries). Faites-le en premier : un agent verrouille le livre pendant son travail.
- **Take lore notes with an agent...** lance un [agent](../agents.md) qui lit l'histoire et écrit les personnages, lieux, objets et termes qu'il trouve comme entrées du lorebook du livre, voir [Notes de lore](../agents.md#lore-notes-of-an-imported-book).

**Not now** ferme la fenêtre sans rien faire.

![La fenêtre après un import](../../images/import-finished.png)

Le même import dans un lorebook est décrit dans [Import et export](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exporter l'histoire

Le menu des réglages (⚙) contient aussi **Export story**, qui enregistre la branche active dans un fichier texte, Markdown, HTML, Word, PDF ou EPUB. Voir [Exporter un livre](export.md).

<a id="what-the-model-gets"></a>

## Ce que reçoit le modèle

Pour chaque nouvelle partie, Marginalia envoie :

1. Le **master template** comme message système : rôle, entrées du [lorebook](../lorebooks/index.md), point de vue, temps verbal, style et [résumés](summaries.md). Le prompt de contournement du [fournisseur d'inférence](../inference-providers.md#general-settings), s'il est activé, vient en premier.
2. Les parties précédentes de la branche active, comme réponses antérieures du modèle lui-même, autant qu'il en tient dans la [limite de contexte](../protocols.md#limits), les plus récentes d'abord. Les parties plus anciennes qui ne tiennent pas sont laissées de côté (sauf si un [résumé](summaries.md) les couvre).
3. Le **user prompt** avec les consignes pour la nouvelle partie.

*Show Prompt* sur n'importe quelle partie montre exactement ce qui a été envoyé. Voir [Prompts du livre](prompts.md) pour les modèles.
