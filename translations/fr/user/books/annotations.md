---
label: Annotations
order: 95
---

# Annotations

Une **annotation** est une note pour vous à propos d'un passage d'une partie : *scène faible*, *la chronologie est fausse*, *à revoir*. Elle fonctionne comme un
commentaire dans un traitement de texte : le texte de l'histoire reste propre, le passage est seulement marqué, et la note vit à côté
de lui. Les notes peuvent aussi provenir d'un modèle ou d'une [extension](../extensions/index.md) qui vérifie l'histoire.

Une annotation est **destinée à l'auteur**. Elle n'est pas envoyée au modèle, sauf si vous le demandez pour cette note, voir
[Transmettre une note au modèle](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Ce qu'est une annotation

| | |
|---|---|
| **Passage** | Les mots auxquels la note se rapporte, ou la **partie entière**. |
| **Note** | Ce que vous voulez retenir. |
| **Type** | Un mot facultatif que vous choisissez (*continuité*, *style*, *rythme*...). |
| **Source** | Qui l'a écrite : vous, un modèle (*IA*), un *agent* ou une *extension*. La marque dans le texte a une couleur différente pour chacun. |
| **Statut** | *Ouverte*, *Résolue*, *Écartée* ou *Orpheline*. Seules les notes ouvertes sont marquées dans le texte. |

Une partie peut avoir un nombre quelconque d'annotations, y compris sur des passages qui se chevauchent.

<a id="making-an-annotation"></a>

## Créer une annotation

**Sur un passage.** Sélectionnez les mots dans le texte de la partie (ils peuvent avoir une mise en forme, la sélection n'a pas besoin d'être exacte), puis **faites un clic droit** dessus (un appui long sur un téléphone) et choisissez **Annoter la sélection**, ou appuyez sur sa [touche](story-editor.md#keyboard-shortcuts) (`a`). La fenêtre affiche le passage ; rédigez la note, un
type facultatif, et indiquez si le modèle reçoit la note, puis **Enregistrer**. Le menu (☰) de la partie ne contient pas cet élément : l'ouvrir supprime la sélection. Le menu du clic droit contient **Annoter la sélection**, [**Réécrire la sélection**](story-editor.md#redrafting-only-a-passage) et **Copier**, et remplace
celui du navigateur sur le texte d'une partie ; il comporte donc aussi **Copier**. Pendant que la partie est en cours de modification, c'est la sélection dans
l'éditeur qui est utilisée : la modification est d'abord enregistrée.

**Sur la partie entière.** Choisissez **Annoter la partie** dans le menu de la partie.

**Avec un modèle.** Choisissez **Annoter avec l'IA**, voir [ci-dessous](#letting-a-model-annotate).

**Depuis la ligne de commande** (et pour les extensions) : [`/create-annotation`](../commands/index.md#annotations).

![Le menu du clic droit sur le texte d'une partie](../../images/story-text-menu.png)

![La fenêtre d'une nouvelle annotation](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Lire les annotations

Un passage contenant une note ouverte est souligné d'un **trait ondulé**, coloré selon l'auteur de la note (bleu pour vous, orange pour un
modèle, vert pour un agent, rouge pour une extension). Une note située à l'intérieur d'une autre est soulignée plus bas, afin que les deux soient visibles.

![La note d'un passage souligné, ouverte à la marque](../../images/annotation-popover.png)

- **Cliquez sur le texte souligné** pour voir les notes qui couvrent ce point (toutes, lorsque des passages se chevauchent). Elles s'ouvrent à côté du texte ; sur un téléphone, touchez le passage.
- Une **bulle avec un nombre** dans l'en-tête d'une partie compte les notes de la partie qui demandent un examen. Cliquez dessus pour
  toutes les voir, y compris celles qui concernent la partie entière et celles dont le passage a disparu.
- Dans la **marge** (le menu des rouages de l'histoire, **Annotations dans la marge** ; ordinateur uniquement, et mémorisé pour vous) les notes de chaque partie se placent en colonne à gauche de son texte, dans
  l'ordre de leurs passages, au lieu de s'afficher dans la fenêtre contextuelle. La note sous le pointeur et son passage souligné s'illuminent ensemble, et un clic sur un
  passage amène sa note à l'écran. Ce sont les mêmes notes (résoudre, modifier...) ; les notes résolues et écartées n'y figurent pas, car elles ne sont pas soulignées.
- L'onglet **Annotations** liste les notes de tout le livre, regroupées par partie dans l'ordre de l'histoire, avec un filtre
  (*À examiner*, *Fermées*, *Toutes*). **Afficher dans l'histoire** (ou le numéro de la partie) vous amène à la partie.

![Les notes dans la marge d'une partie](../../images/annotations-margin.png)

![L'onglet Annotations](../../images/annotations-tab.png)

Chaque note indique qui l'a écrite, son type, son statut, le passage, la note et ces actions :

| | |
|---|---|
| ✓ **Résoudre** | Le problème est traité. La note quitte le texte et reste dans la liste. |
| ✗ **Écarter** | La note ne vaut pas la peine d'être traitée. Idem. |
| ↶ **Rouvrir** | Rétablit une note fermée. |
| ✎ **Modifier** | Modifie la note et le type. |
| 🗑 **Supprimer** | Supprime la note ; elle peut être restaurée depuis la [corbeille](../trash.md). |
| **Transmettre au modèle avec la partie** | Voir ci-dessous. |
| **Rattacher à la sélection** | Pour une note sans passage : sélectionnez du texte dans la partie, puis cliquez dessus. |

<a id="annotations-follow-the-text"></a>

## Les annotations suivent le texte

Vous n'avez pas à vous soucier de l'emplacement d'une note lorsque vous modifiez. Marginalia conserve le passage de chaque note avec les mots auxquels il se rapportait :

- le texte ajouté ou supprimé **avant** le passage le déplace ;
- une modification **à l'intérieur** du passage le rend plus long ou plus court ;
- les mots que vous tapez **juste au début ou à la fin** du passage sont en dehors de celui-ci ;
- corriger une faute de frappe dans un passage d'un seul mot conserve la note sur ce mot.

Lorsque le passage est **supprimé** ou réécrit de façon qu'on ne puisse plus le retrouver, la note devient **Orpheline**. Elle est conservée : rattachez-la
à un autre passage (**Rattacher à la sélection**), écartez-la ou supprimez-la. Si vous rétablissez les mots (annuler), la note retrouve
son passage.

Quand une partie est **générée à nouveau**, le texte est neuf, donc les notes retrouvent leurs passages grâce aux mots qu'elles citaient ; celles dont les mots ont disparu deviennent orphelines. Lorsque deux parties sont [fusionnées](story-editor.md#merging-a-part-into-the-previous-one), les notes de la partie suivante passent à
la partie fusionnée. Une **branche** d'une partie copie ses notes. Les notes font partie des [sauvegardes](backups.md), des copies d'un livre et
des exports d'une sauvegarde.
<a id="letting-a-model-annotate"></a>

## Faire annoter par un modèle

**Annoter avec l'IA** dans le menu d'une partie demande à un modèle d'examiner celle-ci. Vous indiquez ce qu'il faut chercher (*signaler les erreurs de continuité*,
*dialogues faibles*, *contredit le lore* ; laissé vide, on utilise ce que le livre, son dossier ou vos réglages ont
[défini une fois](prompts.md#annotate-prompt), sinon la tâche du prompt), vous choisissez le modèle, puis **Annoter**.

Le modèle lit la partie comme le fait la [réécriture](story-editor.md#redrafting-a-part) : la partie, l'histoire qui la précède (l'histoire plus ancienne
sous forme de [résumés](summaries.md)), autant de l'histoire qui suit que possible, le lore et le style du livre, ainsi que les
notes que la partie possède déjà, afin de ne pas les répéter. Il ne donne jamais de positions : il **cite** le passage, et Marginalia
retrouve la citation dans le texte. Une citation introuvable n'est pas perdue : la note est proposée comme note sur la partie entière,
marquée *introuvable dans le texte*.

**Rien n'est ajouté au livre avant que vous n'acceptiez.** Les notes proposées par le modèle sont listées, chacune avec une case à cocher (toutes
cochées) ; décochez celles que vous ne voulez pas et choisissez **Ajouter les annotations sélectionnées**.

Sur ordinateur, la fenêtre ne bloque pas l'histoire, et elle peut être repliée dans le coin de la page pendant que le modèle travaille, voir
[Travailler à côté des fenêtres](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Option | |
|---|---|
| **Modèle** | Le modèle qui examine. Vide : le **Modèle par défaut pour l'annotation** de vos [réglages](../account.md#general-settings), sinon le modèle du livre. Examiner est une analyse, c'est pourquoi ce n'est souvent pas le modèle qui écrit la prose. |
| **Format de la réponse** | *Automatique* (structuré, lorsque le modèle sait répondre en JSON), *Structuré (JSON)*, *Blocs de texte* (pour les modèles qui ne respectent pas un format) ou *Critique libre, puis extraction* (le modèle rédige une critique libre, un second modèle, en général moins coûteux, la transforme en notes : choisissez-le sous **Modèle qui extrait les notes**). |

![Les notes qu'un modèle propose pour une partie](../../images/annotate-ai-proposals.png)

**Une plage de parties (lot).** **Annoter avec l'IA** dans le menu d'une partie n'examine que cette partie. Pour en examiner plusieurs, ouvrez le menu
des réglages (⚙) de l'histoire et choisissez **Annoter une plage de parties avec l'IA** (également sur téléphone). C'est la même fenêtre avec
**Depuis la partie** et **Jusqu'à la partie** à choisir (les deux sont obligatoires, **Annoter** attend qu'ils soient renseignés ; peu importe
laquelle est la première ; seules les parties ayant un texte sont listées). **Annoter** examine ensuite les parties de la plage l'une après l'autre, avec les mêmes instructions, le même format de réponse et le même modèle, chacune lue avec l'histoire qui l'entoure comme une partie isolée. Les propositions sont listées sous le numéro de leur partie, toutes cochées, et **Ajouter les annotations sélectionnées** ajoute chacune à sa propre partie. **Arrêter** interrompt la partie en cours et n'en lance aucune autre ; ce qui a été trouvé jusque-là reste dans la liste. Une partie dont la réponse ne peut pas être lue, ou qui ne tient pas dans le contexte du modèle, est ignorée (la fenêtre l'indique) et le lot continue ; une panne du modèle met fin au lot. Chaque partie est une requête au modèle : une plage longue coûte donc autant de requêtes (et de jetons).

![Annoter une plage de parties : les propositions sont listées sous le numéro de leur partie](../../images/annotate-ai-batch.png)

**Faits venant des extensions.** Une [extension](../extensions/index.md) qui connaît sur l'histoire plus que ce que dit son texte peut transmettre
ces informations à l'examinateur, qui marque alors comme erreurs de continuité les passages qui les contredisent. [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know)
le fait : il indique au modèle ce que chaque personnage suivi de la partie savait au début de celle-ci, si bien qu'un personnage qui emploie un nom qu'il n'a jamais
entendu est noté. Les faits figurent dans le [prompt d'annotation](prompts.md#annotate-prompt) intégré (un prompt à vous doit utiliser
`{%{{{facts}}}%}`) ; sans une telle extension, le prompt reste tel qu'il était.

Si une réponse ne peut pas être lue, Marginalia demande une nouvelle fois ; si elle reste illisible, la réponse du modèle est affichée afin que le travail
ne soit pas perdu. Le prompt utilisé est le [prompt d'annotation](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Annoter automatiquement chaque nouvelle partie

Le menu des réglages (⚙) de l'histoire contient **Annotation automatique** (également sur téléphone ; une coche indique qu'elle est activée). Le livre
examine chaque partie juste après sa génération, et peut optionnellement la réécrire. Les réglages sont enregistrés avec le livre :

| Option | |
|---|---|
| **Modèle**, **Protocole** | Le modèle qui examine. Vide : le modèle du livre (et non le **Modèle par défaut pour l'annotation** de vos réglages, dont part la fenêtre que vous ouvrez vous-même). |
| **Ce qu'il faut chercher** | Les instructions, comme dans **Annoter avec l'IA**. Vide : on utilise ce que le livre hérite (voir [Prompt d'annotation](prompts.md#annotate-prompt)), sinon la tâche du prompt ; si vous démarrez avec ce champ vide, il affiche alors le texte utilisé. |
| **Annoter automatiquement chaque partie générée** | Active la fonction. Une fois une partie générée (une nouvelle partie, un swipe ou une régénération ; pas une partie arrêtée ou en échec), le modèle l'examine **aussitôt**, sans fenêtre ; le [panneau des exécutions](story-editor.md#the-panel-of-the-runs) la montre comme une étape de [ce qui suit la partie](story-editor.md#what-follows-a-generated-part). |
| **Ajouter les annotations sans demander** | Quand le modèle a fini, les notes qu'il a trouvées sont ajoutées. Sans cette option, la fenêtre d'annotation s'ouvre avec les propositions (dans la page où vous avez généré ; depuis **Examiner** dans le panneau, sur n'importe quelle page) et attend que vous les choisissiez, comme lorsque vous la lancez vous-même. |
| **Puis réécrire la partie pour traiter les annotations** | Seulement avec l'option ci-dessus. Une fois des notes ajoutées, le modèle [réécrit](story-editor.md#redrafting-a-part) la partie, avec les annotations ouvertes de la partie et sans instructions propres. Il écrit avec le modèle du livre et ne touche pas aux résumés : la partie est nouvelle, aucun résumé ne la décrit. Un modèle qui n'a rien trouvé n'a rien à réécrire. |
| **Appliquer la réécriture sans demander** | Seulement avec la réécriture. Quand le modèle a fini, la réécriture remplace le texte de la partie et les notes qu'elle traite sont marquées comme résolues. Sans cette option, ou si vous avez modifié la partie pendant que le modèle écrivait, la fenêtre de réécriture s'ouvre avec le résultat et attend votre choix. |

La fenêtre de réécriture qui attend a deux onglets : **Texte réécrit** (le texte, son raisonnement et les différences avec la partie) et **Texte original avec annotations** (la partie telle qu'elle était, avec les passages des notes ouvertes marqués comme dans l'histoire, et les notes en dessous ; cliquez sur une marque pour aller à sa note, ou survolez l'une ou l'autre pour les mettre en évidence toutes les deux). Regardez l'original pour voir ce que le modèle devait traiter, modifiez le texte ou appliquez-le.

![Les réglages de l'annotation automatique](../../images/annotate-auto.png)

**Fermer une fenêtre qui attend, ou Arrêter dans le panneau, met fin à ce qui suit la partie.** Rien d'autre n'est ajouté ni appliqué, et
les étapes suivantes (une traduction, l'audio) ne sont pas lancées ; le message du panneau les nomme, afin que vous puissiez les faire à la main. Ce qui a déjà été
ajouté ou appliqué reste. Pendant que la réécriture tourne, une nouvelle partie ne peut pas être générée (elle continuerait à partir de l'ancien texte) ; pendant que les notes
sont produites, elle le peut.

Chaque partie générée coûte au moins une requête de plus au modèle (deux avec la réécriture). Si vous générez avec un serveur local qui ne garde qu'un prompt dans son cache,
l'examen remplace l'histoire dans ce cache, et la génération suivante doit relire l'histoire : choisissez un autre **Modèle** pour l'examen, ou
un second serveur.

<a id="giving-a-note-to-the-model"></a>

## Donner une note au modèle

Cochez **Transmettre au modèle avec la partie** sur une note et elle devient aussi une note pour le modèle : les notes des parties qui figurent dans le
prompt entrent dans le modèle maître sous `{%{{{annotations}}}%}` (le modèle intégré a un bloc *Notes de l'auteur sur
l'histoire* ; un modèle à vous doit utiliser la variable). Seules les notes **ouvertes** portant cette case sont transmises, et seulement pour les
parties qui tiennent dans le prompt. Servez-vous-en pour ce que le modèle doit garder en tête en poursuivant (*ce personnage cache
la lettre*), et non pour des remarques de relecture.

<a id="redrafting-to-address-the-notes"></a>

## Réécrire pour traiter les notes

Lorsqu'une partie a des notes ouvertes, la fenêtre de [réécriture](story-editor.md#redrafting-a-part) comporte une case **Traiter les annotations
ouvertes de la partie**. Cochée, le modèle reçoit les notes (le passage et la note) avec vos instructions et
réécrit la partie pour les traiter ; quand vous **Appliquez** la réécriture, ces notes sont marquées comme résolues. Dans la
[réécriture d'une sélection](story-editor.md#redrafting-only-a-passage), seules les notes dont le passage chevauche la sélection sont
transmises.
<a id="commands"></a>

## Commandes

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`,
`/resolve-annotation`, `/reattach-annotation` et `/delete-annotation` permettent de gérer les notes depuis la ligne de commande.
Les extensions peuvent utiliser les mêmes commandes. [`/generate-annotation <part>`](../commands/index.md#annotations) est **Annoter avec l'IA** sous forme de commande : le modèle examine la partie avec le modèle du livre
(un agent utilise son propre modèle lorsque *Écrire le livre avec le modèle de l'agent* est activé ; `profile` choisit un autre profil de modèle de l'agent), avec les instructions, le format de réponse et le prompt du livre, et chaque note trouvée est ajoutée immédiatement,
puisqu'il n'y a personne pour les choisir. Il s'agit d'une seule requête au modèle, elle s'exécute donc seule et consomme des tokens.

<a id="annotations-and-agents"></a>

## Annotations et agents

Un [agent](../agents.md) lit et écrit les annotations avec les mêmes commandes : il peut annoter un livre entier (*continuité*, *style*...), résoudre les notes qu'il a traitées, les modifier
ou les supprimer. Il travaille sur une copie du livre, donc rien n'atteint vos notes tant que vous n'avez pas [examiné le résultat](../agents.md#reviewing-the-result) :

- Une note créée par l'agent apparaît sur une ligne **Annotation - Added** ; une note qu'il a résolue, rejetée ou réécrite est **Modifié** (avec la note, le type, le statut et *give to the model* comme champs, chacun avec sa propre case **Prendre cette modification**) ; une note qu'il a supprimée est **Supprimés**. Les notes d'une partie supprimée par l'agent suivent la partie et n'ont pas de lignes.
- La ligne indique dans quelle partie se trouve la note (`3: ` et le début de la note) ; **Afficher dans la copie** ouvre la partie dans la copie de l'agent. Une note sur une partie écrite par l'agent nécessite cette partie : cocher la note coche aussi la partie.
- **Seulement les annotations** au-dessus de la liste ne coche que les notes. Le passage d'une note prise reste le même lorsque le texte de la partie est identique ; lorsque vous prenez le nouveau texte de la partie de l'agent, ou que votre texte diffère, la note retrouve ses mots dans le texte de votre livre et, lorsque ce n'est pas possible, devient une note sur la partie entière.
- Une note que vous avez écrite pendant que l'agent était en pause arrive dans sa copie quand il reprend (et une note que vous avez résolue y est résolue), afin qu'il en tienne compte ; une note que vous et l'agent avez tous deux modifiée constitue un conflit comme les autres.
