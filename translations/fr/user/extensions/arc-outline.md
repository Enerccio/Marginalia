---
label: Plan en arcs
order: 24
---

# Plan en arcs

Le plan de votre livre dans la barre latérale : des arcs, chacun découpé en chapitres, avec l'état de chacun d'eux, et un modèle
capable d'écrire des arcs et des chapitres pour vous. Le plan ne fait pas partie du prompt de l'histoire ; les générateurs et le
[Side Query](side-query.md) peuvent l'utiliser lorsque vous le leur demandez.

<a id="the-arcs-tab"></a>

## L'onglet Arcs

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **Arcs**, à côté de *Outline*. **Add new
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
| calendrier / lecture / coche | L'état ; ouvre un menu pour le modifier. |
| chevron (arcs) | Masque ou affiche les chapitres de l'arc. |
| chevron (chapitres avec une note) | Affiche ou masque la note du chapitre sous sa description, pour la lire sans ouvrir la boîte de dialogue. |
| flèches | Déplacent l'arc ou le chapitre vers le haut ou vers le bas. |
| plus (arcs) | Ajoute un chapitre à la fin de l'arc. |
| baguette (arcs) | Demande au modèle d'écrire des chapitres pour l'arc, voir [ci-dessous](#generating-chapters). |
| punaise | Lie (épingle) l'arc ou le chapitre à la partie la plus visible de l'histoire, voir [ci-dessous](#linking-to-the-story). |
| cible (arcs et chapitres avec une partie liée) | Fait défiler l'histoire jusqu'à la partie à laquelle il est lié. |
| crayon | Ouvre l'élément dans la boîte de dialogue. |
| corbeille | Supprime l'élément (un arc avec tous ses chapitres), après confirmation. |
| robot (arcs et chapitres générés) | Affiche ce que le modèle a écrit, voir [ci-dessous](#generating-an-arc). |

En haut, à côté de la baguette, se trouve un crayon avec une plume : il rédige les instructions du tour suivant à partir du chapitre en cours, voir
[ci-dessous](#drafting-the-instructions-for-the-current-chapter).

![L'onglet Arcs : un arc terminé, un arc en cours et un arc planifié](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Lier au récit

Un arc ou un chapitre peut être **lié à une partie de l'histoire**, pour passer du plan au passage correspondant dans le texte. La boîte de dialogue contient
le champ **Linked part** : la place de la partie dans la branche active, numérotée à partir de 1 comme l'affiche l'onglet *Outline* (`#12`).
Saisissez-la, ou appuyez sur le bouton **link** à côté pour prendre la partie qui est la **plus visible** dans l'histoire à ce moment-là (faites d'abord défiler
jusqu'à l'endroit voulu, puis ouvrez la boîte de dialogue) ; le bouton **target** fait défiler l'histoire jusqu'à la partie indiquée dans le champ. Un arc ou un chapitre lié
affiche aussi le bouton cible sur sa carte. La **punaise** d'une carte fait la même chose que le bouton link, sans boîte de dialogue : faites défiler
l'histoire jusqu'au début du chapitre, puis appuyez dessus. Le lien désigne une place dans la branche ; il pointe donc vers une autre partie lorsque des parties
qui le précèdent sont supprimées ou insérées. Modifiez-le alors. Un import depuis l'histoire lie les chapitres à votre place, voir [ci-dessous](#importing-an-arc-from-the-story).

<a id="chapter-headings-in-the-text"></a>

### Titres de chapitre dans le texte

**Chapter headings in the text**, dans le menu de la baguette en haut, écrit un titre au début de la partie à laquelle chaque chapitre est
[lié](#linking-to-the-story), par exemple `# Chapter 4: The Storm` ; ainsi le texte de l'histoire porte ses chapitres (l'extension Chapter Marker et les exports les reprennent). La boîte de dialogue
liste les titres qu'elle écrirait, avec leur partie, au fur et à mesure que vous modifiez les options :

| Option | |
|---|---|
| Arcs get headings too | Désactivée : seuls les chapitres reçoivent un titre (`#`) et les arcs sont ignorés. Activée : chaque arc reçoit un titre (`#`) et ses chapitres un niveau en dessous (`##`). Un arc qui n'est pas lié lui-même va à la partie de son premier chapitre lié. |
| Prefix of the arcs / of the chapters | Le mot placé avant le numéro, *Part* et *Chapter* par défaut. Vide donne `4. The Storm`. |
| Numbers | Arabes (1, 2, 3), romains (I, II, III) ou aucun. Les chapitres sont numérotés selon leur place dans le plan, liés ou non ; ainsi un chapitre pas encore écrit garde son numéro libre. |
| Number the chapters from 1 in every arc | Sinon, les chapitres sont numérotés à travers tout le livre. |
| Put the titles into the headings | Désactivée, on obtient `Chapter 4` seul. |
| Remove every other heading of the story first | Chaque ligne de titre de la branche active est supprimée avant l'écriture des nouveaux titres, utile lorsque des chapitres ont été liés ailleurs depuis la dernière exécution. |
| Keep the summaries valid | Activée par défaut. Un titre ne change pas ce qui se passe : les résumés qui décrivaient l'histoire avant la modification redeviennent donc valides (les résumés déjà périmés restent périmés). |

**Write the headings** les insère ; les titres qu'une partie commence déjà par sont remplacés, si bien qu'une nouvelle exécution après une modification du
plan ne les duplique pas. **Remove all headings** (après confirmation) supprime toutes les lignes de titre du texte de la branche active. Chaque partie modifiée conserve son
ancien texte dans son historique. Un chapitre terminé ou en cours sans lien, et un lien situé après la fin de la branche active, ne reçoivent pas de titre et sont signalés. Les options sont mémorisées.

<a id="the-note-of-a-chapter"></a>

### La note d'un chapitre

Un chapitre qui possède une note affiche un chevron à droite. Il ouvre la note sous la description, dans la liste, pour que vous puissiez lire les détails d'un chapitre (ceux que le modèle a rédigés, par exemple) sans ouvrir la boîte de dialogue ; appuyez à nouveau pour la masquer. Les notes ouvertes ne sont pas sauvegardées.

![Un chapitre avec sa note affichée](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Déplacer des chapitres d'un arc à l'autre

Un chapitre déplacé vers le haut depuis la première place de son arc passe à la fin de l'arc précédent ; un chapitre déplacé vers le bas depuis la dernière place passe
au début de l'arc suivant. Continuez à appuyer sur la flèche pour déplacer un chapitre aussi loin que nécessaire.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Rédiger les instructions du chapitre en cours

La plume à côté de *Add new arc* ouvre la fenêtre qui permet au modèle de
[rédiger les instructions](../books/story-editor.md#drafting-the-instructions) du tour suivant, avec les données du chapitre où se trouve l'histoire comme **Plan of the Story**. Le chapitre en cours est le premier chapitre marqué **Current** ; à défaut, le premier qui n'est pas **Finished** ; un arc marqué comme en cours sans chapitre encore écrit est utilisé seul. Le modèle reçoit cet arc et ce chapitre avec leurs descriptions et leurs notes, ainsi que le titre et la description (mais pas la note) du chapitre suivant, avec la consigne que l'histoire ne doit pas encore y parvenir. La fenêtre part de ce que vous avez saisi dans *Instructions* du panneau du nouveau tour, comme guide ; **Use as
Instructions** place le brouillon dans ce panneau et l'ouvre. Sans chapitre en cours, le bouton l'indique.

Ces mêmes données sont une source pour le [Side Query](side-query.md), voir [ci-dessous](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Terminer un arc

Marquer un arc **Finished** marque aussi tous ses chapitres **Finished**. Passer un arc dans un autre état laisse ses
chapitres tels quels.

Le plan est enregistré avec le livre dès que vous le modifiez ; il est donc inclus dans ses sauvegardes.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## Le plan comme source pour d'autres extensions

Le plan est proposé aux autres extensions comme source de données sur le livre : dans [Side Query](side-query.md), cochez
**Arc outline** dans le menu des plugs pour envoyer avec vos questions les arcs, leurs états, leurs descriptions et leurs chapitres. Il existe deux versions : **Arc outline** est la version concise (descriptions seulement), **Arc outline (with notes)** ajoute sous la description de chaque arc et de chaque chapitre sa note, qui contient les détails des arcs et chapitres générés. Vos notes privées ne sont envoyées que si vous cochez la seconde. Une troisième source, **Arc outline: current chapter**, n'envoie que l'arc et le chapitre où se trouve l'histoire (voir [Rédiger les instructions du chapitre en cours](#drafting-the-instructions-for-the-current-chapter)).
<a id="generating-an-arc"></a>

## Générer un arc

Le bouton baguette à côté de **Add new arc** ouvre un menu à trois entrées : **Generate an arc** demande au modèle d'écrire un nouvel arc, **Import from story** résume ce que vous avez déjà écrit, voir [ci-dessous](#importing-an-arc-from-the-story), et **Develop the plot** est une conversation avec le modèle sur la direction que prend l'histoire, voir [ci-dessous](#developing-the-plot-in-a-conversation).

![Le menu de la baguette](../../images/plugin-arcoutline-menu.png)

 La boîte de dialogue de **Generate an arc** comporte deux parties.

À gauche :

| | |
|---|---|
| Prompt | Les instructions destinées au modèle, avec trois variables : `{{backgroundLore}}` (les entrées du lorebook du livre et de ses sous-lorebooks), `{{existingArcs}}` (vos arcs et chapitres, avec leurs états et descriptions, mais sans vos notes privées) et `{{specification}}`. Il part du prompt de *Settings → Extension Settings → Arc Outline* (le prompt par défaut si vous ne l'avez pas modifié) ; ce que vous modifiez ici ne s'applique qu'à cette génération. |
| Specification | Ce que vous attendez de l'arc : le thème, les personnages, vers où il doit mener. Peut rester vide. |
| Tools | Seulement lorsqu'une suite d'[outils pour le modèle](tools.md) est installée (noms, lieux) : les suites, et les catégories de ces suites, que le modèle peut utiliser au lieu d'inventer des éléments, comme dans le [Lore Generator](lore-generator.md). |
| Model, Protocols | Reprennent le modèle et le protocole du livre (vos valeurs par défaut dans *Settings* lorsque le livre n'en a pas). Modifiez-les pour utiliser un autre modèle pour cette seule génération ; ce choix n'est pas enregistré. |
| Generate | Lance le modèle. Pendant son exécution, le bouton devient un **Stop** rouge et tout le reste de la boîte de dialogue est désactivé. |

À droite, vous voyez le **raisonnement** du modèle au fur et à mesure qu'il s'écrit et, dès que la réponse commence, l'**arc** proposé (un nom, une description concise et une note plus longue), présenté comme un arc du plan (la note sous sa description) et qui s'enrichit à mesure que la réponse arrive. La description est ce que le plan affiche : le modèle est donc invité à la limiter à deux phrases et à placer les détails dans la note, qui se retrouve dans la **Note** de l'arc enregistré. Lorsque le modèle a fini, sa réponse est vérifiée ; si ce n'est pas un arc valide (par exemple si la note manque), la boîte de dialogue le signale et l'arc ne peut pas être enregistré.

![Génération d'un arc](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** ajoute l'arc à la fin du plan. Pour recommencer, appuyez à nouveau sur **Generate**, avec le même prompt ou la même spécification, ou modifiés ; la réponse précédente est remplacée.

<a id="importing-an-arc-from-the-story"></a>

## Importer un arc depuis l'histoire

**Import from story** dans le menu de la baguette demande au modèle de lire une partie de votre histoire et de la transcrire en un seul arc avec ses chapitres, afin que vous puissiez intégrer au plan un livre déjà écrit. Le modèle lit toujours la [branche active](../books/branches-and-story-tree.md) du livre, telle qu'elle est au moment où vous ouvrez la boîte de dialogue. La boîte de dialogue ressemble à celle des arcs, avec ces choix à gauche :

| | |
|---|---|
| Du message, Au message | La plage de l'histoire à envoyer. Les messages de la branche qui contiennent du texte sont numérotés à partir de 1 ; au départ, toute la branche est choisie. Sous chaque champ, vous voyez le début du message qu'il désigne. |
| Nombre de chapitres (facultatif) | Choisissez un nombre pour obtenir exactement ce nombre de chapitres (une réponse avec un autre nombre est rejetée) ; laissez vide pour que le modèle découpe l'histoire là où elle se découpe naturellement. |
| Lier les chapitres aux parties de l'histoire | Activé par défaut. Les messages sont numérotés à partir de 1 dans la plage envoyée, et le modèle indique dans quel message commence chaque chapitre ; le chapitre est [lié](#linking-to-the-story) à cette partie de l'histoire (numérotée sur l'ensemble de la branche, donc exacte quelle que soit la plage), et l'arc à la partie où commence son premier chapitre. Une réponse qui nomme un message non envoyé laisse le chapitre non lié. |
| État de l'arc importé et de ses chapitres | **Finished** par défaut, car l'histoire est écrite ; choisissez **Current** ou **Planned** si vous en avez besoin. |
| Compteur de tokens | Le nombre de messages de la plage et leur nombre approximatif de tokens, ainsi que le nombre approximatif de tokens de l'ensemble du prompt (instructions, lore, plan et histoire), à côté de ce que prend le modèle. Il devient un avertissement lorsque le prompt ne tient pas ; choisissez alors moins de messages. Il suit la plage, le nombre de chapitres, le prompt et le modèle. |
| Prompt | Les instructions destinées au modèle, avec les variables `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (les messages de la plage) et `{{numberOfChapters}}`. Il part du **Story import prompt** de *Settings → Extension Settings → Arc Outline*. |

![Importation d'un arc depuis l'histoire](../../images/plugin-arcoutline-import.png)

Ici, il n'y a pas d'outils : les noms se trouvent dans l'histoire. À droite, vous voyez le raisonnement, puis l'arc et ses chapitres au fur et à mesure que le modèle les écrit. **Save as arc** ajoute l'arc avec ses chapitres à la fin du plan ; chacun d'eux conserve le raisonnement et la réponse du modèle et possède son propre bouton robot. Une histoire longue s'importe mieux en plusieurs étapes, un arc pour chaque plage.
<a id="developing-the-plot-in-a-conversation"></a>

## Développer l'intrigue dans une conversation

**Develop the plot** dans le menu de la baguette ouvre une conversation avec le modèle sur l'intrigue. Parler ne change rien dans le livre : le modèle voit ce que vous choisissez de lui envoyer, répond, et lorsque vous vous êtes mis d'accord sur quelque chose, vous le transmettez au générateur d'arcs en un clic.

À gauche, vous choisissez ce que le modèle voit et comment il vous aide (les choix sont mémorisés pour la prochaine fois). À droite se trouve la conversation :

![L'assistant d'intrigue : les options à gauche, la conversation à droite](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Model, Protocols | Commencent comme le modèle et le protocole du livre. Les changer ne s'applique qu'à cette conversation. |
| How the model helps | **Free talk** (vous menez), **Explore directions** (plusieurs façons distinctes dont l'histoire peut continuer), **Find the problems** (fils jamais résolus, promesses que personne ne tient, contradictions avec le lorebook, rythme) ou **Expand an idea** (votre idée développée en prémisse, tournants, une fin et des chapitres qui s'enchaînent après vos arcs). |
| Send the lorebook | Les entrées du lorebook du livre et de ses sous-lorebooks. |
| Send the outline | **Not sent**, **Descriptions only** ou **With the notes** (vos notes privées aussi). |
| Send the notes | Seulement lorsque [Story Notes](story-notes.md) est installé : **Not sent**, **Titles and first lines** (par défaut) ou **Full text** de vos notes, sans les pages que vous avez masquées aux assistants. |
| Send the summaries of the story | Les résumés remplacent l'histoire jusqu'au plus récent d'entre eux, comme le fait une génération. |
| Send the latest parts of the story | Les parties après le résumé le plus récent (toutes lorsque les résumés sont désactivés), les plus récentes qui tiennent dans **Tokens of the story**. Chaque partie est montrée au modèle sous son numéro dans le plan de l'histoire. |
| Tokens of the story | Le plus grand nombre de tokens des parties de l'histoire. Vide : un tiers de ce que prend le modèle. |
| Prompt | Les instructions pour le modèle, avec les variables `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` et `{{mode}}`. Le changement est mémorisé. |

Sous les options, une ligne indique combien de résumés et de parties de l'histoire reçoit le modèle, et combien de tokens la requête compte sur ce que prend le modèle. Elle devient un avertissement lorsque cela ne tient pas : envoyez moins ou [résumez la conversation](#chats-branches-and-summaries). Le livre est relu à chaque message, donc un changement du plan ou de l'histoire est vu par la réponse suivante.

À droite se trouve la conversation. Tapez un message et appuyez sur **Send** (c'est **Stop** pendant que le modèle écrit ; ce qui est arrivé est conservé). Chaque message a des boutons : copier, **branch from here**, supprimer, et sur une réponse du modèle aussi une baguette, **Generate an arc from this message**, qui ouvre le générateur d'arcs avec le message comme spécification. Si la discussion a été longue, demandez d'abord au modèle de résumer ce que vous avez décidé, sous forme de spécification.

<a id="letting-the-model-change-the-book"></a>

### Laisser le modèle modifier le livre

Sous les options se trouve **What the model may do**. Avec aucune de ces cases cochée, la conversation n'est qu'un échange. Cochez un groupe et le modèle reçoit des outils pour celui-ci, et peut les utiliser lorsque vous le demandez ou que vous êtes d'accord ; il est invité à regarder avant d'ajouter, à préférer ajouter plutôt que remplacer, et à dire ce qu'il a fait :

| Group | The model can |
|---|---|
| **Planner** | lister les scènes du [planner](../books/story-editor.md#the-planner-tab), les ajouter, les modifier, les déplacer, les supprimer et les marquer comme faites. |
| **Lorebook** | chercher et lire le lorebook, créer des entrées et modifier les entrées existantes (pas les supprimer). |
| **Outline** | lire le plan, ajouter des arcs et des chapitres, les modifier et les déplacer (pas les supprimer). |
| **Notes** (lorsque [Story Notes](story-notes.md) est installé) | chercher et lire vos notes et suivre leurs liens, ajouter une page ou ajouter à une page lorsque vous lui demandez de garder quelque chose, et modifier le texte d'une page (pas supprimer une page). Les pages que vous avez masquées aux assistants restent inaccessibles. |
| **Characters** (lorsque le [Character Tracker](character-tracker.md) est installé) | lire les personnages du livre et leurs relations, ajouter des personnages et des relations, les modifier (nom, accroche, note, couleur) et les supprimer. Seule la suppression est demandée avant d'avoir lieu, une modification ne l'est pas : vous la voyez dans la conversation et dans l'onglet Characters. Le modèle peut désigner un personnage par son numéro dans la liste ; la question et la conversation affichent à côté le nom du personnage. |
| **Generators** | appeler les générateurs d'arcs et de chapitres et, lorsque le [Lore Generator](lore-generator.md) est installé, générer une entrée de lorebook. Ils renvoient un brouillon et n'enregistrent rien, sauf si le modèle en reçoit l'ordre ; il vous montre le brouillon et l'enregistre lorsque vous êtes d'accord. |
| **Tools** (bouton, lorsqu'une [suite d'outils](tools.md) est installée) | les outils de noms (noms, lieux), comme dans les générateurs, pour qu'il puisse vous proposer de vrais noms. |

- **Ask me before it changes or deletes something** (activé par défaut) : la modification du texte de ce qui existe (une entrée de lorebook, une scène, un arc) et la suppression vous sont montrées d'abord, avec ce que le modèle veut transmettre, et le modèle attend **Allow** ou **Refuse**. L'ajout d'une scène, d'une entrée ou d'un arc n'est pas demandé : vous le voyez dans la conversation et dans le planner, le lorebook ou le plan, et vous pouvez le supprimer.
- Chaque outil appelé par le modèle est listé sous sa réponse (une ligne avec le nom et une marque : ✓ réussi, ✗ échoué ; ouvrez-la pour voir ce qui a été transmis et ce qui est revenu). Les messages suivants portent les appels faits par le modèle et ce qu'ils ont renvoyé (un long résultat est coupé), pour qu'il sache ce qu'il a déjà fait et ne refasse pas la même entrée ; ils comptent dans la limite de tokens de la conversation.
- Ce que coûtent les outils : leurs descriptions font partie de chaque requête, donc un modèle à court de contexte dispose de moins de place. Seuls les modèles capables d'appeler des fonctions fonctionnent avec eux ; la boîte de dialogue le signale lorsqu'un modèle ne le peut pas.

![Le modèle a ajouté deux scènes au planner ; ses appels sont listés sous la réponse](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Conversations, branches et résumés

Un livre peut avoir autant de conversations que vous voulez. Choisissez-en une dans la liste au-dessus des messages : **+** en démarre une nouvelle, le crayon la renomme (elle porte le nom de votre premier message jusque-là) et la poubelle la supprime. Elles sont stockées avec le livre.

- **Branch** copie la conversation telle quelle dans une nouvelle conversation ; le bouton de branche d'un message la copie jusqu'à ce message inclus, pour reprendre l'échange autrement à partir de là. Par la suite, la copie n'a plus rien à voir avec l'original.
- **Summarize the chat** demande d'abord ce que le résumé doit garder ou souligner (facultatif, par exemple « garder le retournement sur le traître et la fin que nous avons rejetée » ; vide : le modèle décide), puis demande au modèle un résumé de la conversation (ce que vous avez décidé, ce que vous avez rejeté, ce qui reste ouvert, les noms qui ont été fixés) et l'ajoute comme message. Dès lors, le modèle reçoit ce résumé et les messages qui le suivent, **et non** les messages qui le précèdent, lesquels restent à l'écran, estompés. Utilisez-le lorsque la conversation est devenue trop longue pour le modèle, ou pour repartir d'un état propre sans perdre les décisions. Si vous résumez à nouveau plus tard, le nouveau résumé couvre l'ancien. Faites d'abord une branche lorsque vous voulez garder toute la conversation pour le modèle.

![La question sur ce que le résumé doit garder](../../images/plugin-arcoutline-wizard-summarize.png)

![La conversation après le résumé : les messages au-dessus restent à l'écran, estompés](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Génération des chapitres

Le bouton baguette d'un arc demande au modèle de diviser cet arc en chapitres. La boîte de dialogue ressemble à celle des arcs, avec ces choix à gauche :

| | |
|---|---|
| Ce que le modèle connaît de l'histoire | **Whole story so far** : les arcs précédant celui-ci, avec tous leurs chapitres. **Arc summaries only** : les mêmes arcs sans chapitres. **Arc summaries and finished chapters** : les mêmes arcs, avec seulement les chapitres que vous avez marqués *Finished*. Les arcs suivant celui-ci ne sont jamais envoyés. |
| Ce que vous voulez des chapitres en général | Rythme, ton, ce qui doit se passer. Peut être vide. |
| Nombre de chapitres | Écrivez-le en toutes lettres (« environ huit ») ou choisissez un nombre exact. Un nombre ajoute une zone de texte pour chaque chapitre, où vous pouvez indiquer ce qu'il doit contenir ; elles sont toutes facultatives. Utiliser l'un des deux efface l'autre. Avec un nombre exact, une réponse comportant un nombre de chapitres différent est rejetée. |
| Prompt | Les instructions pour le modèle, avec les variables `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` et `{{chapterSpecification}}`. Il reprend le **Chapter generation prompt** de *Settings → Extension Settings → Arc Outline*. |

À droite, vous voyez le raisonnement, puis l'arc avec les chapitres qu'il contient déjà, et les nouveaux chapitres au fur et à mesure que le modèle les écrit. **Save as chapters** les ajoute à la fin de l'arc, chacun avec une description succincte et les détails dans sa note (comme pour les arcs). Chaque nouveau chapitre conserve le raisonnement et la réponse du modèle, et possède son propre bouton robot.

![Génération des chapitres d'un arc](../../images/plugin-arcoutline-generate-chapters.png)

Un arc ou un chapitre écrit par le modèle conserve son **reasoning** et la **answer** d'où il provient. Le bouton robot placé dessus ouvre ceux-ci dans une petite fenêtre contextuelle, à l'écart.

![Ce que le modèle a écrit pour un arc](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Commandes

Le plan et ses générateurs sont aussi des [commandes](../commands/index.md), pour la ligne de commande et pour les [agents](../agents.md). Elles travaillent sur le livre ouvert (pour un agent : sur sa copie de travail), et un onglet Arcs ouvert affiche la modification immédiatement. Les arcs et les chapitres sont désignés par leur **numéro**, tel que le plan le montre : le premier arc est `1`, le premier chapitre d'un arc est `1` (les numéros changent quand on ajoute, supprime ou déplace un élément qui les précède). Une commande qui modifie un arc ou un chapitre ne prend que les champs que vous lui donnez ; un texte vide efface une description ou une note.

| Commande | Ce qu'elle fait |
|---|---|
| `/get-arc-outline [arc [notes]]` | Les arcs avec leurs chapitres, leurs états et leurs descriptions, en JSON. Les notes privées seulement avec `notes` `true` ; `arc` renvoie ce seul arc. Lecture seule. |
| `/add-arc title [description [note [state [position]]]]` | Ajoute un arc, à la fin ou à `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Modifie un arc. `state` est `PLANNED`, `CURRENT` ou `FINISHED` (terminer un arc termine ses chapitres). |
| `/move-arc arc position` | Déplace un arc à la place `position` (1 est le premier). |
| `/delete-arc arc` | Supprime un arc avec ses chapitres, après confirmation. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Ajoute un chapitre à un arc. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Modifie un chapitre. |
| `/move-outline-chapter arc chapter position [to_arc]` | Déplace un chapitre à l'intérieur de son arc, ou dans l'arc `to_arc`. |
| `/delete-outline-chapter arc chapter` | Supprime un chapitre, après confirmation. |
| `/generate-arc [specification [save]]` | Demande au modèle un nouvel arc, comme le fait *Generate an arc*. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Demande au modèle les chapitres d'un arc, comme le fait la baguette de l'arc. `count` est un nombre exact de chapitres, `context` est `EVERYTHING` (la valeur par défaut), `ARCS_ONLY` ou `ARCS_AND_FINISHED`, selon le choix *What the model knows of the story*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Demande au modèle de résumer les messages `from` à `to` de la branche active (numérotés parmi les messages contenant du texte, comme dans *Import from story*) sous forme d'arc avec ses chapitres. `state` (par défaut `FINISHED`) est l'état de l'arc enregistré et de ses chapitres. Avec `mapParts` `true`, chaque chapitre est lié au message où il commence (sa *Linked part*, comme le fait la case *Map parts* de la boîte de dialogue), et la réponse contient la partie de chaque chapitre. |

Les trois commandes de génération utilisent les mêmes prompts que les boîtes de dialogue (vos *Extension Settings → Arc Outline*, ou les valeurs par défaut), le modèle et le protocole du livre (votre modèle par défaut si le livre n'en a pas) et aucun outil : un appelant qui veut des noms les obtient avec les commandes du [générateur de noms](name-generator.md) ou du [fournisseur de noms](name-provider.md), et les écrit dans la spécification. Comme les boîtes de dialogue, elles n'enregistrent que sur demande : avec `save` `false` (la valeur par défaut), elles renvoient l'arc ou les chapitres en JSON et ne stockent rien ; avec `save` `true`, elles les ajoutent au plan (un arc à la fin, des chapitres à la fin de l'arc) et la réponse contient leurs numéros. Une histoire trop longue pour le modèle est refusée par `/import-arc-from-story` : choisissez moins de messages. Les commandes de génération prennent du temps et s'arrêtent quand l'exécution est interrompue.

Ajouter et modifier ne sont pas destructifs ; seules les deux commandes `delete` le sont, et un agent peut être configuré pour vous demander confirmation.
