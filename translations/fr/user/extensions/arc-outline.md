---
label: Plan des arcs
order: 24
---

# Plan des arcs

Le plan de votre livre dans la barre latérale : des arcs, chacun découpé en chapitres, avec l'état de chacun d'eux, et un modèle
capable d'écrire des arcs et des chapitres à votre place. Le plan ne fait pas partie du prompt de l'histoire ; les générateurs et le
[Side Query](side-query.md) peuvent l'utiliser lorsque vous le leur demandez.

<a id="the-arcs-tab"></a>

## L'onglet Arcs

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **Arcs** à côté de *Plan*. **Add new
arc** ouvre la boîte de dialogue d'un arc :

| Champ | |
|---|---|
| Title | Obligatoire. |
| Description | Ce qui se passe dedans. |
| Note (private) | Notes pour vous-même. |
| State | **Planned**, **Current** ou **Finished**. |
| Linked part | La partie de l'histoire à laquelle l'arc ou le chapitre appartient, voir [ci-dessous](#linking-to-the-story). |

Les chapitres ont les mêmes champs. La liste affiche chaque arc sous forme de carte, avec ses chapitres en retrait en dessous. La couleur
d'une carte suit son état : gris pour planifié, bleu pour l'arc en cours, vert avec un titre atténué pour les arcs terminés. Les boutons
d'une carte :

| Bouton | |
|---|---|
| calendar / play / check | L'état ; ouvre un menu pour le changer. |
| chevron (arcs) | Masque ou affiche les chapitres de l'arc. |
| chevron (chapitres avec une note) | Affiche ou masque la note du chapitre sous sa description, pour la lire sans ouvrir la boîte de dialogue. |
| arrows | Déplace l'arc ou le chapitre vers le haut ou vers le bas. |
| plus (arcs) | Ajoute un chapitre à la fin de l'arc. |
| wand (arcs) | Demande au modèle d'écrire des chapitres pour l'arc, voir [ci-dessous](#generating-chapters). |
| target (arcs et chapitres avec une partie liée) | Fait défiler l'histoire jusqu'à la partie à laquelle il est lié. |
| pen | Ouvre l'élément dans la boîte de dialogue. |
| trash | Supprime l'élément (un arc avec tous ses chapitres), après confirmation. |
| robot (arcs et chapitres générés) | Affiche ce que le modèle a écrit, voir [ci-dessous](#generating-an-arc). |

À côté de la baguette, en haut, se trouve un stylo avec une plume : il rédige les instructions du prochain tour à partir du chapitre en cours, voir
[ci-dessous](#drafting-the-instructions-for-the-current-chapter).

![L'onglet Arcs : un arc terminé, un arc en cours et un arc planifié](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Lier à l'histoire

Un arc ou un chapitre peut être **lié à une partie de l'histoire**, pour passer du plan à l'endroit du texte. La boîte de dialogue contient
le champ **Linked part** : la place de la partie dans la branche active, numérotée à partir de 1 comme l'affiche l'onglet *Plan* (`#12`).
Saisissez-la, ou appuyez sur le bouton **link** à côté pour prendre la partie qui est la **plus visible** dans l'histoire à ce moment
(faites défiler jusqu'à l'endroit voulu, puis ouvrez la boîte de dialogue) ; le bouton **target** fait défiler l'histoire jusqu'à la partie
indiquée dans le champ. Un arc ou un chapitre lié affiche aussi le bouton target sur sa carte. Le lien est la place dans la branche, il
pointe donc vers une autre partie lorsque des parties qui la précèdent sont supprimées ou insérées ; modifiez-le alors. Un import depuis
l'histoire lie les chapitres à votre place, voir [ci-dessous](#importing-an-arc-from-the-story).

<a id="the-note-of-a-chapter"></a>

### La note d'un chapitre

Un chapitre qui a une note affiche un chevron à droite. Il ouvre la note sous la description, dans la liste, pour que vous puissiez
lire les détails d'un chapitre (ceux écrits par le modèle, par exemple) sans ouvrir la boîte de dialogue ; appuyez à nouveau pour la
masquer. Les notes ouvertes ne sont pas enregistrées.

![Un chapitre avec sa note affichée](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Déplacer des chapitres d'un arc à l'autre

Un chapitre déplacé vers le haut depuis la première place de son arc passe à la fin de l'arc précédent ; un chapitre déplacé vers le bas
depuis la dernière place passe au début de l'arc suivant. Continuez à appuyer sur la flèche pour déplacer un chapitre aussi loin que nécessaire.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Rédiger les instructions du chapitre en cours

Le stylo avec une plume, à côté de *Add new arc*, ouvre la fenêtre qui permet au modèle de
[rédiger les instructions](../books/story-editor.md#drafting-the-instructions) du prochain tour, avec les données du chapitre où en est
l'histoire comme **Plan de l'histoire**. Le chapitre en cours est le premier chapitre marqué **Current** ; s'il n'y en a pas, le premier
qui n'est pas **Finished** ; un arc marqué en cours qui n'a pas encore de chapitres est seul. Le modèle reçoit cet arc et ce chapitre avec
leurs descriptions et leurs notes, ainsi que le titre et la description (pas la note) du chapitre suivant, avec l'avertissement que l'histoire
ne doit pas encore y arriver. La fenêtre part de ce que vous avez saisi dans *Instructions* du panneau du nouveau tour, comme guide ;
**Use as Instructions** place le brouillon dans ce panneau et l'ouvre. Sans chapitre en cours, le bouton l'indique.

Ces mêmes données servent de source au [Side Query](side-query.md), voir [ci-dessous](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Terminer un arc

Marquer un arc **Finished** marque aussi tous ses chapitres **Finished**. Passer un arc à un autre état laisse ses
chapitres tels quels.

Le plan est enregistré avec le livre dès que vous le modifiez, il est donc inclus dans ses sauvegardes.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## Le plan comme source pour d'autres extensions

Le plan est proposé à d'autres extensions comme source de données sur le livre : dans le [Side Query](side-query.md), cochez
**Arc outline** dans le popover des plugins pour envoyer les arcs, leurs états, descriptions et chapitres avec vos questions. Il existe
deux versions : **Arc outline** est la version concise (descriptions seulement), **Arc outline (with notes)** ajoute la note de chaque
arc et de chaque chapitre sous sa description, c'est là que se trouvent les détails des arcs et chapitres générés. Vos notes privées ne
sont envoyées que si vous cochez la seconde. Une troisième source, **Arc outline: current chapter**, n'envoie que l'arc et le chapitre
où en est l'histoire (voir [Rédiger les instructions du chapitre en cours](#drafting-the-instructions-for-the-current-chapter)).

<a id="generating-an-arc"></a>

## Générer un arc

Le bouton baguette à côté de **Add new arc** ouvre un menu à trois entrées : **Generate an arc** demande au modèle d'écrire un nouvel arc, **Import from story** résume ce que vous avez déjà écrit, voir [ci-dessous](#importing-an-arc-from-the-story), et **Develop the plot** est une conversation avec le modèle sur la direction que prend l'histoire, voir [ci-dessous](#developing-the-plot-in-a-conversation).

![Le menu de la baguette](../../images/plugin-arcoutline-menu.png)

 La boîte de dialogue de **Generate an arc** comporte deux moitiés.

À gauche :

| | |
|---|---|
| Prompt | Les instructions pour le modèle, avec trois variables : `{{backgroundLore}}` (les entrées du lorebook du livre et de ses sous-lorebooks), `{{existingArcs}}` (vos arcs et chapitres, avec leurs états et descriptions, mais sans vos notes privées) et `{{specification}}`. Il part du prompt de *Settings → Extension Settings → Arc Outline* (le prompt par défaut si vous ne l'avez pas modifié) ; ce que vous éditez ici ne s'applique qu'à cette génération. |
| Specification | Ce que vous attendez de l'arc : le thème, les personnages, vers où il doit mener. Peut être vide. |
| Tools | Seulement lorsqu'une suite d'[outils pour le modèle](tools.md) est installée (noms, lieux) : les suites, et leurs catégories, que le modèle peut utiliser au lieu d'inventer des choses, comme dans le [Lore Generator](lore-generator.md). |
| Model, Protocols | Reprennent le modèle et le protocole du livre (vos valeurs par défaut de *Settings* lorsque le livre n'en a pas). Changez-les pour utiliser un autre modèle pour cette seule génération ; le choix n'est pas enregistré. |
| Generate | Lance le modèle. Pendant son exécution, le bouton devient un **Stop** rouge et tout le reste de la boîte de dialogue est désactivé. |

À droite, vous voyez le **raisonnement** du modèle au fur et à mesure qu'il s'écrit et, dès que la réponse commence, l'**arc** proposé — un nom,
une description concise et une note plus longue — présenté comme un arc du plan (la note sous sa description) et qui grandit à mesure que la
réponse arrive. La description est ce que affiche le plan, c'est pourquoi le modèle est invité à la limiter à quelques phrases et à mettre
les détails dans la note, qui se retrouve dans la **Note** de l'arc enregistré. Lorsque le modèle a fini, sa réponse est vérifiée ; si ce
n'est pas un arc valide (par exemple si la note manque), la boîte de dialogue le signale et l'arc ne peut pas être enregistré.

![Génération d'un arc](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** ajoute l'arc à la fin du plan. Pour recommencer, appuyez une nouvelle fois sur **Generate**, avec le même prompt ou la même spécification, ou modifiés ; la réponse précédente est remplacée.
<a id="importing-an-arc-from-the-story"></a>

## Importer un arc depuis l'histoire

**Importer depuis l'histoire** dans le menu de la baguette demande au modèle de lire une partie de votre histoire et de
la transcrire en un arc avec ses chapitres, afin de faire entrer dans le plan un livre que vous avez déjà écrit. Le
modèle lit toujours la [branche active](../books/branches-and-story-tree.md) du livre, telle qu'elle est au moment où
vous ouvrez la boîte de dialogue. Celle-ci ressemble à celle des arcs, avec les choix suivants à gauche :

| | |
|---|---|
| From message, To message | L'étendue de l'histoire à envoyer. Les messages de la branche qui ont du texte sont numérotés à partir de 1 ; au départ, toute la branche est sélectionnée. Sous chaque champ, vous voyez le début du message auquel il renvoie. |
| Number of chapters (optional) | Choisissez un nombre pour obtenir exactement ce nombre de chapitres (une réponse avec un autre nombre est rejetée). Laissez le champ vide pour que le modèle divise l'histoire là où elle se divise naturellement. |
| Link the chapters to the parts of the story | Activé au départ. Les messages sont numérotés à partir de 1 dans l'étendue que vous envoyez ; le modèle indique dans quel message commence chaque chapitre. Le chapitre est [lié](#linking-to-the-story) à ce passage de l'histoire (numéroté dans toute la branche, donc juste quelle que soit l'étendue), et l'arc au passage où commence son premier chapitre. Une réponse qui cite un message non envoyé laisse le chapitre non lié. |
| State of the imported arc and its chapters | **Terminé** au départ, car l'histoire est écrite ; choisissez **Actuelle** ou **Planifié** si vous en avez besoin. |
| Token counter | Le nombre de messages de l'étendue et leur nombre approximatif de tokens, ainsi que le nombre approximatif de tokens de l'ensemble de l'invite (instructions, lore, plan et histoire), à côté de ce que prend le modèle. Un avertissement apparaît lorsque l'invite ne tient pas ; choisissez alors moins de messages. Il suit l'étendue, le nombre de chapitres, l'invite et le modèle. |
| Prompt | Les instructions pour le modèle, avec les variables `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (les messages de l'étendue) et `{{numberOfChapters}}`. Elle part de la **Story import prompt** de *Settings → Extension Settings → Arc Outline*. |

![Importer un arc depuis l'histoire](../../images/plugin-arcoutline-import.png)

Il n'y a pas d'outils ici, les noms se trouvent dans l'histoire. À droite, vous voyez le raisonnement, puis l'arc et ses
chapitres tels que le modèle les écrit. **Save as arc** ajoute l'arc avec ses chapitres à la fin du plan ; chacun d'eux
conserve le raisonnement et la réponse du modèle et possède son propre bouton robot. Une longue histoire se règle mieux
en plusieurs étapes, un arc pour chaque étendue.
<a id="developing-the-plot-in-a-conversation"></a>

## Développer l'intrigue dans une conversation

**Développer l'intrigue** dans le menu de la baguette ouvre une conversation avec le modèle au sujet de l'intrigue. Rien dans le livre ne change en discutant : le modèle voit ce que vous choisissez de lui envoyer, répond, et une fois que vous vous êtes mis d'accord sur un point, vous le transmettez au générateur d'arcs en un clic.

À gauche, vous choisissez ce que le modèle voit et la manière dont il vous aide (les choix sont mémorisés pour la prochaine fois) ; à droite se trouve la conversation :

![L'assistant d'intrigue : les options à gauche, la conversation à droite](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Modèle, Protocoles | Commencent avec le modèle et le protocole du livre. Les modifier ne s'applique qu'à cette conversation. |
| Comment le modèle vous aide | **Discussion libre** (vous menez), **Explorer des directions** (plusieurs façons distinctes dont l'histoire peut se poursuivre), **Repérer les problèmes** (fils jamais résolus, promesses non tenues, contradictions avec l'univers, rythme) ou **Développer une idée** (votre idée transformée en prémisse, tournants, une fin et des chapitres qui s'enchaînent après vos arcs). |
| Envoyer le lorebook | Les entrées du lorebook du livre et de ses sous-lorebooks. |
| Envoyer le plan | **Non envoyé**, **Descriptions seulement**, ou **Avec les notes** (vos notes privées comprises). |
| Envoyer les notes | Uniquement si [Notes de l'histoire](story-notes.md) est installé : **Non envoyées**, **Titres et premières lignes** (par défaut) ou **Texte intégral** de vos notes, sans les pages que vous avez masquées aux assistants. |
| Envoyer les résumés de l'histoire | Les résumés remplacent l'histoire jusqu'au plus récent d'entre eux, comme le fait une génération. |
| Envoyer les dernières parties de l'histoire | Les parties après le résumé le plus récent (toutes, si les résumés sont désactivés), les plus récentes qui tiennent dans **Jetons de l'histoire**. Chaque partie est présentée au modèle sous son numéro dans le plan de l'histoire. |
| Jetons de l'histoire | Le nombre maximal de jetons des parties de l'histoire. Vide : un tiers de ce que prend le modèle. |
| Prompt | Les instructions donnée au modèle, avec les variables `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` et `{{mode}}`. La modification est mémorisée. |

Sous les options, une ligne indique combien de résumés et de parties de l'histoire le modèle reçoit, et combien de jetons la requête compte sur ce que le modèle peut prendre. Elle devient un avertissement lorsque cela ne tient pas ; envoyez moins, ou [résumez la conversation](#chats-branches-and-summaries). Le livre est relu à chaque message : une modification du plan ou de l'histoire est donc prise en compte dès la réponse suivante.

À droite se trouve la conversation. Saisissez un message et appuyez sur **Envoyer** (le bouton devient **Arrêter** pendant que le modèle écrit ; ce qui est déjà arrivé est conservé). Chaque message possède des boutons : copier, **Brancher à partir d'ici**, supprimer, et, sur une réponse du modèle, aussi une baguette, **Générer un arc à partir de ce message**, qui ouvre le générateur d'arcs avec le message comme spécification. Si la discussion a été longue, demandez d'abord au modèle de résumer ce que vous avez décidé sous forme de spécification.

<a id="letting-the-model-change-the-book"></a>

### Laisser le modèle modifier le livre

Sous les options se trouve **Ce que le modèle peut faire**. Tant qu'aucune de ces cases n'est cochée, la conversation reste un simple échange. Cochez un groupe et le modèle reçoit des outils pour s'en servir, qu'il peut utiliser lorsque vous le demandez ou lorsque vous vous êtes mis d'accord ; il est invité à regarder avant d'ajouter, à préférer l'ajout au remplacement, et à dire ce qu'il a fait :

| Groupe | Le modèle peut |
|---|---|
| **Planificateur** | lister les scènes du [planificateur](../books/story-editor.md#the-planner-tab), en ajouter, en modifier, les déplacer, les supprimer et les marquer comme faites. |
| **Lorebook** | chercher et lire l'univers, créer des entrées et modifier les entrées existantes (sans les supprimer). |
| **Plan** | lire le plan, ajouter des arcs et des chapitres, les modifier et les déplacer (sans les supprimer). |
| **Notes** (lorsque [Notes de l'histoire](story-notes.md) est installé) | chercher et lire vos notes et suivre leurs liens, ajouter une page ou compléter une page lorsque vous lui demandez de retenir quelque chose, et modifier le texte d'une page (sans supprimer de page). Les pages que vous avez masquées aux assistants restent inaccessibles. |
| **Générateurs** | appeler le générateur d'arcs et de chapitres et, lorsque le [Générateur d'univers](lore-generator.md) est installé, générer une entrée d'univers. Ils renvoient un brouillon et ne sauvegardent rien, sauf si le modèle en reçoit l'ordre ; il vous montre le brouillon et le sauvegarde lorsque vous êtes d'accord. |
| **Outils** (bouton, lorsqu'une [suite d'outils](tools.md) est installée) | les outils de noms (noms, lieux), comme pour les générateurs, afin qu'il puisse vous proposer de vrais noms. |

- **Me demander avant de modifier ou de supprimer quelque chose** (activé par défaut) : toute modification du texte de ce qui existe (une entrée d'univers, une scène, un arc) et toute suppression vous sont présentées d'abord, avec ce que le modèle veut transmettre, et le modèle attend **Autoriser** ou **Refuser**. L'ajout d'une scène, d'une entrée ou d'un arc n'est pas soumis à cette demande : vous le voyez dans la conversation et dans le planificateur, le lorebook ou le plan, et pouvez le supprimer.
- Chaque outil appelé par le modèle est listé sous sa réponse (une ligne avec le nom et une marque : ✓ réussi, ✗ échoué ; ouvrez-la pour voir ce qui a été transmis et ce qui est revenu). Les messages suivants contiennent les appels que le modèle a faits et ce qu'ils ont renvoyé (un résultat long est coupé), afin qu'il sache ce qu'il a déjà fait et ne crée pas deux fois la même entrée ; ces éléments comptent dans la limite de jetons de la conversation.
- Coût des outils : leurs descriptions font partie de chaque requête, donc un modèle à court de contexte dispose de moins de place. Seuls les modèles capables d'appeler des fonctions fonctionnent avec cette option ; la fenêtre le signale lorsqu'un modèle ne le peut pas.

![Le modèle a ajouté deux scènes au planificateur ; ses appels sont listés sous la réponse](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Conversations, branches et résumés

Un livre peut contenir un nombre quelconque de conversations ; choisissez-en une dans la liste au-dessus des messages, **+** en démarre une nouvelle, le crayon la renomme (elle porte le nom de votre premier message jusque-là) et la corbeille la supprime. Elles sont enregistrées avec le livre.

- **Brancher** copie la conversation telle quelle dans une nouvelle conversation ; le bouton de branche d'un message la copie jusqu'à ce message inclus, pour reprendre l'échange autrement à partir de là. La copie n'a plus aucun lien avec l'original.
- **Résumer la conversation** demande d'abord ce que le résumé doit garder ou mettre en avant (facultatif, par exemple « garder le retournement sur le traître et la fin que nous avons rejetée » ; vide : le modèle décide), puis demande au modèle un résumé de la conversation (ce que vous avez décidé, ce que vous avez rejeté, ce qui reste ouvert, les noms fixés) et l'ajoute comme message. À partir de là, le modèle reçoit ce résumé et les messages qui le suivent, **et non** les messages qui le précèdent, lesquels restent à l'écran, estompés. Utilisez-le lorsque la conversation est devenue trop longue pour le modèle, ou pour repartir d'une base propre sans perdre les décisions. Résumez à nouveau plus tard : le nouveau résumé couvre l'ancien. Branchez d'abord si vous voulez que le modèle conserve la conversation entière.

![La question : que doit garder le résumé](../../images/plugin-arcoutline-wizard-summarize.png)

![La conversation après le résumé : les messages qui le précèdent restent à l'écran, estompés](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Génération de chapitres

Le bouton baguette d'un arc demande au modèle de découper cet arc en chapitres. La boîte de dialogue ressemble à celle des arcs, avec
ces choix à gauche :

| | |
|---|---|
| Ce que le modèle connaît de l'histoire | **Histoire entière jusqu'à présent** : les arcs jusqu'à celui-ci, avec tous leurs chapitres. **Résumés d'arcs seulement** : les mêmes arcs sans chapitres. **Résumés d'arcs et chapitres terminés** : les mêmes arcs avec seulement les chapitres que vous avez marqués *Terminé*. Les arcs après celui-ci ne sont jamais envoyés. |
| Ce que vous attendez des chapitres en général | Rythme, ton, ce qui doit se passer. Peut être vide. |
| Nombre de chapitres | Écrivez-le en toutes lettres (« environ huit ») ou choisissez un nombre exact. Un nombre ajoute une zone de texte pour chaque chapitre, où vous pouvez indiquer ce qu'il doit contenir ; toutes sont facultatives. Utiliser l'un des deux efface l'autre. Avec un nombre exact, une réponse comportant un nombre de chapitres différent est rejetée. |
| Consigne | Les instructions données au modèle, avec les variables `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` et `{{chapterSpecification}}`. Elle commence par la **Consigne de génération de chapitres** de *Paramètres → Paramètres d'extension → Arc Outline*. |

À droite, vous voyez le raisonnement, puis l'arc avec les chapitres qu'il possède déjà, et les nouveaux chapitres au fur et à mesure que le modèle les écrit. **Enregistrer comme chapitres** les ajoute à la fin de l'arc, chacun avec une description succincte et les détails dans sa note (comme pour les arcs). Chaque nouveau chapitre conserve le raisonnement et la réponse du modèle, et possède son propre bouton robot.

![Génération des chapitres d'un arc](../../images/plugin-arcoutline-generate-chapters.png)

Un arc ou un chapitre écrit par le modèle conserve son **raisonnement** et la **réponse** dont il est issu. Le bouton robot
placé dessus ouvre ces éléments dans une petite fenêtre flottante, en dehors du chemin.

![Ce que le modèle a écrit pour un arc](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Commandes

Le plan et ses générateurs sont aussi des [commandes](../commands/index.md), pour la ligne de commande et pour les
[agents](../agents.md). Elles travaillent sur le livre ouvert (pour un agent : sur sa copie de travail), et un onglet Arcs ouvert
affiche le changement immédiatement. Les arcs et les chapitres sont désignés par leur **numéro**, tel que le plan les affiche : le premier arc est `1`, le premier
chapitre d'un arc est `1` (les numéros changent lorsqu'on ajoute, supprime ou déplace quelque chose avant eux). Une commande qui
modifie un arc ou un chapitre ne prend que les champs que vous lui donnez ; un texte vide efface une description ou une note.

| Commande | Ce qu'elle fait |
|---|---|
| `/get-arc-outline [arc [notes]]` | Les arcs avec leurs chapitres, états et descriptions au format JSON. Les notes privées seulement avec `notes` `true` ; `arc` ne renvoie que cet arc. Lecture seule. |
| `/add-arc title [description [note [state [position]]]]` | Ajoute un arc, à la fin ou à `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Modifie un arc. `state` vaut `PLANNED`, `CURRENT` ou `FINISHED` (terminer un arc termine ses chapitres). |
| `/move-arc arc position` | Déplace un arc à la place `position` (1 est le premier). |
| `/delete-arc arc` | Supprime un arc avec ses chapitres, après confirmation. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Ajoute un chapitre à un arc. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Modifie un chapitre. |
| `/move-outline-chapter arc chapter position [to_arc]` | Déplace un chapitre à l'intérieur de son arc, ou dans l'arc `to_arc`. |
| `/delete-outline-chapter arc chapter` | Supprime un chapitre, après confirmation. |
| `/generate-arc [specification [save]]` | Demande au modèle un nouvel arc, comme le fait *Générer un arc*. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Demande au modèle les chapitres d'un arc, comme le fait la baguette de l'arc. `count` est un nombre exact de chapitres, `context` vaut `EVERYTHING` (la valeur par défaut), `ARCS_ONLY` ou `ARCS_AND_FINISHED`, comme le choix *Ce que le modèle connaît de l'histoire*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Demande au modèle de résumer les messages de `from` à `to` de la branche active (numérotés parmi les messages contenant du texte, comme dans *Importer depuis l'histoire*) sous forme d'un arc avec chapitres. `state` (par défaut `FINISHED`) est l'état de l'arc enregistré et de ses chapitres. Avec `mapParts` `true`, chaque chapitre est lié au message où il commence (sa *Partie liée*, comme le fait la case *Associer les parties* de la boîte de dialogue), et la réponse donne la partie de chaque chapitre. |

Les trois commandes de génération utilisent les mêmes consignes que les boîtes de dialogue (vos *Paramètres d'extension → Arc Outline*, ou les valeurs par défaut), le modèle et le protocole du livre (votre modèle par défaut lorsque le livre n'en a pas) et aucun outil : un appelant qui veut des noms les obtient avec les commandes du [Générateur de noms](name-generator.md) ou du [Fournisseur de noms](name-provider.md) et les écrit dans la consigne. Comme les boîtes de dialogue, elles n'enregistrent que sur demande : avec `save` `false` (la valeur par défaut), elles renvoient l'arc ou les chapitres au format JSON sans rien stocker ; avec `save` `true`, elles les ajoutent au plan (un arc à la fin, les chapitres à la fin de l'arc) et la réponse indique leurs numéros. Une histoire trop longue pour le modèle est refusée par `/import-arc-from-story` : choisissez moins de messages. Les commandes de génération prennent du temps, et s'arrêtent lorsque l'exécution est arrêtée.

Ajouter et modifier ne sont pas destructifs ; seules les deux commandes `delete` le sont, et un agent peut être configuré pour vous demander confirmation avant de les exécuter.
