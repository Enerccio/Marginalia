---
label: Annotations
order: 95
---

# Annotations

Une **annotation** est une note pour vous sur un passage d'une partie : *scène faible*, *la chronologie est fausse*, *à revoir*. Elle fonctionne comme un commentaire dans un traitement de texte : le texte de l'histoire reste propre, le passage est seulement marqué, et la note vit à côté. Les notes peuvent aussi venir d'un modèle ou d'une [extension](../extensions/index.md) qui vérifie l'histoire.

Une annotation est **destinée à l'auteur**. Elle n'est pas envoyée au modèle sauf si vous le demandez pour cette note, voir [Donner une note au modèle](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Ce qu'est une annotation

| | |
|---|---|
| **Passage** | Les mots auxquels la note se rapporte, ou la **partie entière**. |
| **Note** | Ce que vous voulez retenir. |
| **Type** | Un mot facultatif que vous choisissez (*continuité*, *style*, *rythme*...). |
| **Source** | Qui l'a écrite : vous, un modèle (*IA*), un *agent* ou une *extension*. La marque dans le texte a une couleur différente pour chacun. |
| **Statut** | *Ouverte*, *Résolue*, *Écartée* ou *Orpheline*. Seules les notes ouvertes sont marquées dans le texte. |

Une partie peut avoir autant d'annotations que vous voulez, y compris sur des passages qui se chevauchent.

<a id="making-an-annotation"></a>

## Créer une annotation

**Sur un passage.** Sélectionnez les mots dans le texte de la partie (ils peuvent avoir une mise en forme, la sélection n'a pas besoin d'être exacte), puis faites un **clic droit** (un appui prolongé sur téléphone) et choisissez **Annotate selection**, ou appuyez sur sa [touche](story-editor.md#keyboard-shortcuts) (`a`). La fenêtre affiche le passage ; rédigez la note, un type facultatif, et indiquez si le modèle reçoit la note, puis **Save**. Le menu (☰) de la partie n'a pas cet élément : l'ouvrir fait perdre la sélection. Le menu du clic droit contient **Annotate selection**, [**Redraft selection**](story-editor.md#redrafting-only-a-passage) et **Copy**, et remplace celui du navigateur sur le texte d'une partie ; il contient donc aussi **Copy**. Pendant que la partie est en cours de modification, c'est la sélection dans l'éditeur qui est utilisée : la modification est d'abord enregistrée.

**Sur la partie entière.** Choisissez **Annotate part** dans le menu de la partie.

**Avec un modèle.** Choisissez **Annotate with AI**, voir [plus bas](#letting-a-model-annotate).

**Depuis la ligne de commande** (et pour les extensions) : [`/create-annotation`](../commands/index.md#annotations).

![Le menu du clic droit sur le texte d'une partie](../../images/story-text-menu.png)

![La fenêtre d'une nouvelle annotation](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Lire les annotations

Un passage avec une note ouverte est souligné d'un **trait ondulé**, de la couleur de son auteur (bleu pour vous, orange pour un modèle, vert pour un agent, rouge pour une extension). Une note à l'intérieur d'une autre est soulignée plus bas, pour que les deux restent visibles.

![La note d'un passage souligné, ouverte à la marque](../../images/annotation-popover.png)

- **Cliquez sur le texte souligné** pour voir les notes qui couvrent ce point (toutes, là où les passages se chevauchent). Elles s'ouvrent à côté du texte ; sur téléphone, touchez le passage.
- Une **bulle avec un nombre** dans l'en-tête d'une partie compte les notes de la partie qui demandent un regard. Cliquez dessus pour toutes les voir, y compris celles qui concernent la partie entière et celles qui ont perdu leur passage.
- Dans la **marge** (le menu à engrenages de l'histoire, **Annotations in the margin** ; ordinateur uniquement, et mémorisé pour vous), les notes de chaque partie se placent en colonne à gauche de son texte, dans l'ordre de leurs passages, au lieu de la fenêtre flottante. La note sous le pointeur et son passage souligné s'allument ensemble, et un clic sur un passage fait apparaître sa note. Ce sont les mêmes notes (résoudre, modifier...) ; les notes résolues et écartées n'y apparaissent pas, puisqu'elles ne sont pas soulignées.
- L'onglet **Annotations** liste les notes de tout le livre, regroupées par partie dans l'ordre de l'histoire, avec un filtre (*Need a look*, *Closed*, *All*). **Show in story** (ou le numéro de la partie) vous amène à la partie.

![Les notes dans la marge d'une partie](../../images/annotations-margin.png)

![L'onglet Annotations](../../images/annotations-tab.png)

Chaque note affiche son auteur, son type, son statut, le passage, la note et ces actions :

| | |
|---|---|
| ✓ **Resolve** | Le problème est traité. La note quitte le texte et reste dans la liste. |
| ✗ **Dismiss** | La note ne mérite pas qu'on s'en occupe. Idem. |
| ↶ **Reopen** | Rouvre une note fermée. |
| ✎ **Edit** | Modifie la note et le type. |
| 🗑 **Delete** | Supprime la note ; elle peut être restaurée depuis la [corbeille](../trash.md). |
| **Give to the model with the part** | Voir plus bas. |
| **Attach to selection** | Pour une note sans passage : sélectionnez du texte dans la partie, puis cliquez dessus. |

<a id="annotations-follow-the-text"></a>

## Les annotations suivent le texte

Vous n'avez pas à vous soucier de l'endroit où se trouve une note quand vous modifiez. Marginalia garde le passage de chaque note avec les mots auxquels elle se rapportait :

- le texte ajouté ou supprimé **avant** le passage le déplace ;
- une modification **à l'intérieur** du passage le rallonge ou le raccourcit ;
- les mots que vous tapez **juste au début ou à la fin** du passage restent en dehors de lui ;
- corriger une faute dans un passage d'un seul mot conserve la note sur ce mot.

Lorsque le passage est **supprimé** ou réécrit de façon à ne plus pouvoir être retrouvé, la note devient **Orpheline**. Elle est conservée : rattachez-la à un autre passage (**Attach to selection**), écartez-la ou supprimez-la. Si vous rétablissez les mots (annuler), la note retrouve son passage.

Lorsqu'une partie est **générée à nouveau**, le texte est neuf : les notes retrouvent alors leurs passages grâce aux mots qu'elles citaient ; celles dont les mots ont disparu deviennent orphelines. Quand deux parties sont [fusionnées](story-editor.md#merging-a-part-into-the-previous-one), les notes de la seconde passent dans la partie fusionnée. Une **branche** d'une partie copie ses notes. Les notes font partie des [sauvegardes](backups.md), des copies d'un livre et des exports d'une sauvegarde.
<a id="letting-a-model-annotate"></a>

## Laisser un modèle annoter

**Annotate with AI** dans le menu d'une partie demande à un modèle de la relire. Vous indiquez ce qu'il faut chercher (*signaler les erreurs de continuité*, *dialogue faible*, *contredit le lore* ; vide, il utilise ce que le livre, son dossier ou vos réglages [définissent une fois pour toutes](prompts.md#annotate-prompt), sinon la tâche du prompt), choisissez le modèle, puis **Annotate**.

Le modèle lit la partie comme le fait la [réécriture](story-editor.md#redrafting-a-part) : la partie, l'histoire qui la précède (l'histoire plus ancienne sous forme de [résumés](summaries.md)), autant de l'histoire qui suit que possible, le lore et le style du livre, et les notes que la partie a déjà afin de ne pas les répéter. Il ne donne jamais de positions : il **cite** le passage, et Marginalia retrouve la citation dans le texte. Une citation introuvable n'est pas perdue : la note est proposée comme note sur la partie entière, marquée *not found in the text*.

**Rien n'est ajouté au livre tant que vous n'avez pas accepté.** Les notes proposées par le modèle sont listées, chacune avec une case à cocher (toutes cochées). Décochez celles que vous ne voulez pas et choisissez **Add the selected annotations**.

Sur un ordinateur, la fenêtre ne bloque pas l'histoire : elle peut être repliée dans le coin de la page pendant que le modèle travaille, voir [Travailler à côté de la fenêtre](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Option | |
|---|---|
| **Model** | Le modèle qui relit. Vide, c'est le **Default model for annotating** de vos [réglages](../account.md#general-settings), sinon le modèle du livre. Relire est une analyse : ce n'est donc souvent pas le modèle qui écrit la prose. |
| **Answer format** | *Automatic* (structuré, quand le modèle peut répondre en JSON), *Structured (JSON)*, *Text blocks* (pour les modèles qui ne respectent pas un format) ou *Free critique, then extraction* (le modèle écrit une critique libre, puis un second modèle, généralement moins coûteux, la transforme en notes : choisissez-le sous **Model that extracts the notes**). |

![Les notes qu'un modèle propose pour une partie](../../images/annotate-ai-proposals.png)

**Une plage de parties (lot).** **Annotate with AI** dans le menu d'une partie ne relit que cette partie. Pour en relire plusieurs, ouvrez le menu des réglages (⚙) de l'histoire et choisissez **Annotate a range of parts with AI** (sur téléphone aussi). C'est la même fenêtre, avec **From part** et **To part** à choisir (les deux sont obligatoires, **Annotate** attend qu'ils soient renseignés ; peu importe lequel est le plus ancien ; seules les parties dotées d'un texte sont listées). **Annotate** relit ensuite les parties de la plage l'une après l'autre, avec les mêmes consignes, le même format de réponse et le même modèle, chacune lue avec l'histoire qui l'entoure comme une partie isolée. Les propositions sont listées sous le numéro de leur partie, toutes cochées, et **Add the selected annotations** ajoute chacune à sa propre partie. **Stop** arrête la partie en cours et n'en lance plus aucune ; ce qui a été trouvé jusque-là reste dans la liste. Une partie dont la réponse ne peut pas être lue, ou qui ne tient pas dans le contexte du modèle, est ignorée (la fenêtre indique laquelle) et l'exécution continue ; un échec du modèle met fin à l'exécution. Chaque partie est une requête au modèle : une longue plage coûte donc autant de requêtes (et de tokens).

![Annotation d'une plage de parties : les propositions sont listées sous le numéro de leur partie](../../images/annotate-ai-batch.png)

**Faits venus des extensions.** Une [extension](../extensions/index.md) qui en sait plus sur l'histoire que son texte peut le transmettre au relecteur, qui marque alors les passages qui les contredisent comme erreurs de continuité. Le [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know) le fait : il indique au modèle ce que chaque personnage suivi de la partie savait au début de celle-ci, si bien qu'un personnage qui utilise un nom qu'il n'a jamais entendu est signalé. Les faits figurent dans le [prompt d'annotation](prompts.md#annotate-prompt) intégré (un prompt qui vous est propre doit utiliser `{%{{{facts}}}%}`) ; sans une telle extension, le prompt reste tel quel.

Si une réponse ne peut pas être lue, Marginalia demande une fois de plus ; si elle reste illisible, la réponse du modèle est affichée afin que le travail ne soit pas perdu. Le prompt est le [prompt d'annotation](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Annoter automatiquement chaque nouvelle partie

Le menu des réglages (⚙) de l'histoire contient **Automatic annotation** (sur téléphone aussi ; une coche indique qu'il est activé). Il fait relire chaque partie juste après sa génération, et la réécrire en option. Les réglages sont enregistrés avec le livre :

| Option | |
|---|---|
| **Model**, **Protocol** | Le modèle qui relit. Vide, c'est le modèle du livre (et non le **Default model for annotating** de vos réglages, avec lequel démarre la fenêtre que vous ouvrez vous-même). |
| **What to look for** | Les consignes, comme dans **Annotate with AI**. Vide, il utilise ce que le livre hérite (voir [Annotate prompt](prompts.md#annotate-prompt)), sinon la tâche du prompt ; si vous démarrez avec ce champ vide, il affiche ensuite le texte utilisé. |
| **Annotate every generated part automatically** | L'active. Une fois une partie générée (une nouvelle partie, un swipe ou une régénération ; pas une partie arrêtée ou en échec), le modèle la relit **aussitôt**, sans fenêtre : le [panneau des exécutions](story-editor.md#the-panel-of-the-runs) la montre comme une étape de [ce qui suit la partie](story-editor.md#what-follows-a-generated-part). |
| **Add the annotations without asking** | Quand le modèle a fini, les notes qu'il a trouvées sont ajoutées. Sans cette option, la fenêtre d'annotation s'ouvre avec les propositions (dans la page où vous avez généré ; depuis **Review** dans le panneau, sur n'importe quelle page) et attend que vous les choisissiez, comme lorsque vous la lancez vous-même. |
| **Then redraft the part to address the annotations** | Uniquement avec l'option ci-dessus. Une fois les notes ajoutées, le modèle [réécrit](story-editor.md#redrafting-a-part) la partie, avec les annotations ouvertes de la partie et sans consignes propres. Il écrit avec le modèle du livre et ne touche pas aux résumés : la partie est nouvelle, aucun résumé ne la décrit. Un modèle qui n'a rien trouvé n'a rien à réécrire. |
| **Apply the redraft without asking** | Uniquement avec la réécriture. Quand le modèle a fini, la réécriture remplace le texte de la partie et les notes traitées sont marquées comme résolues. Sans cette option, ou si vous avez modifié la partie pendant que le modèle écrivait, la fenêtre de réécriture s'ouvre avec le résultat et attend votre décision. |

La fenêtre de réécriture qui attend a deux onglets : **Redrafted text** (le texte, son raisonnement et les différences avec la partie) et **Original text with annotations** (la partie telle qu'elle était, avec les passages des notes ouvertes marqués comme dans l'histoire, et les notes en dessous ; cliquez sur une marque pour aller à sa note, ou pointez l'une des deux pour les illuminer toutes deux). Consultez l'original pour voir ce que l'on a demandé au modèle de traiter, modifiez le texte ou appliquez-le.

![Les réglages de l'annotation automatique](../../images/annotate-auto.png)

**Fermer une fenêtre qui attend, ou Stop dans le panneau, met fin à ce qui suit la partie.** Rien d'autre n'est ajouté ni appliqué, et les étapes suivantes (une traduction, l'audio) ne sont pas lancées ; le message du panneau les nomme, afin que vous puissiez les faire à la main. Ce qui a déjà été ajouté ou appliqué reste. Pendant que la réécriture tourne, une nouvelle partie ne peut pas être générée (elle continuerait à partir de l'ancien texte) ; pendant que les notes sont rédigées, elle le peut.

Chaque partie générée coûte au moins une requête de plus au modèle (deux avec la réécriture). Si vous générez avec un serveur local qui ne garde qu'un seul prompt dans son cache, la relecture remplace l'histoire dans ce cache et la génération suivante doit relire toute l'histoire : choisissez un autre **Model** pour la relecture, ou un second serveur.
<a id="watching-for-phrases"></a>

## Surveiller les expressions

Une **liste de surveillance des expressions** (*phrase watch list*) désigne les mots que vous ne voulez pas voir dans la prose : clichés (*a shiver ran down her spine*, *tapestry*), tics d'un modèle, nom que vous avez abandonné. Une partie est contrôlée pour ces expressions **sans modèle**, et chaque endroit où une expression est trouvée devient une annotation de ce passage (sa source est *Phrase watch list*, et l'entrée correspondante y est indiquée). Ensuite, c'est une note comme les autres : laissez la [réécriture](#redrafting-to-address-the-notes) s'en charger, ou utilisez **Dismiss** pour un résultat qui convient (dans un dialogue, par exemple).

![La liste de surveillance des expressions d'un livre](../../images/phrase-watch-dialog.png)

Les expressions ne sont pas interdites dans la sortie du modèle. Une interdiction n'empêcherait une expression qu'après son apparition et pousserait le modèle vers le synonyme le plus proche ; une note sur le texte terminé permet de réécrire le passage dans son ensemble.

**Où se trouvent les listes.** Vos paramètres ont un onglet **Phrase watch list** avec votre liste, que tous les livres utilisent. Un [dossier](folders.md#folder-settings) ajoute une liste pour les livres qu'il contient et pour les dossiers situés en dessous, et un livre ajoute la sienne, dans **Phrase watch list** du menu des paramètres (⚙) de l'histoire. Les listes s'additionnent. Une entrée portant la même expression qu'une entrée héritée la remplace (pour lui donner un autre modèle de note, par exemple), et une entrée marquée **Off here** la retire pour ce dossier ou ce livre ; les expressions héritées sont listées sous les vôtres, chacune avec **Switch off here**.

**Comment une expression est trouvée.** Sans tenir compte de la casse, et seulement comme mots entiers (*tapestry* n'est pas trouvé dans *tapestries*) ; tout espace ou saut de ligne correspond à n'importe quel autre, et les guillemets et apostrophes droits et courbes correspondent entre eux. Cochez **Regular expression** pour écrire à la place une [expression régulière](../regular-expressions.md), également sans tenir compte de la casse. Une expression inutilisable est refusée au moment de l'enregistrement. Au plus 100 notes sont créées dans une même partie.

**Modèles d'annotation.** Le texte d'une note est un **annotation template** de vos paramètres (le même onglet) : un nom, la **Note**, où `{%{{{match}}}%}` représente les mots trouvés et `{%{{{phrase}}}%}` l'entrée de la liste, un **Type** (la couleur de la marque) et **Give to the model with the part** (voir [Donner une note au modèle](#giving-a-note-to-the-model)). Chaque entrée d'une liste en choisit un ; une entrée sans modèle reçoit une note intégrée (*Watched phrase: "…"*), sans type, et non transmise au modèle.

**Quand les parties sont contrôlées.** Dans **Phrase watch list** du livre :

| Option | |
|---|---|
| **Annotate the watched phrases in every generated part** | Une fois une partie générée, elle est contrôlée juste après l'[annotation automatique](#annotating-every-new-part-automatically) (une étape de [ce qui suit la partie](story-editor.md#what-follows-a-generated-part), que l'annotation automatique soit activée ou non). Lorsque le livre réécrit aussi automatiquement (**Add the annotations without asking** et **Then redraft the part…** de l'annotation automatique), la réécriture traite également ces notes ; une réécriture appliquée sans demande de confirmation est contrôlée à nouveau, et si elle a réintroduit une expression, elle est réécrite de nouveau, trois fois au maximum (les résultats de la dernière passe restent des notes pour vous). Les parties écrites par un [agent](../agents.md) sont également contrôlées. |
| **Save and scan the story** | Enregistre la liste et contrôle dès maintenant les parties de la branche active, pour un livre déjà écrit : toutes, ou celles allant de **From part** à **To part** (les numéros des parties de la branche). La commande `/scan-phrases [from to]` fait la même chose. |

Un emplacement qui porte déjà une note de la liste pour la même expression n'en reçoit pas une seconde, y compris si vous l'avez rejetée, afin que l'histoire puisse être contrôlée à nouveau après une modification. Une note résolue ne compte pas : si l'expression est toujours présente (une réécriture l'a réintroduite), elle reçoit une nouvelle note. Les modèles d'annotation appartiennent à vos paramètres : un livre exporté avec ce qu'il hérite emporte les expressions avec lui, et sur une autre installation, leurs notes sont celles par défaut.

<a id="giving-a-note-to-the-model"></a>

## Donner une note au modèle

Cochez **Give to the model with the part** sur une note, et elle devient aussi une note pour le modèle : les notes des parties qui figurent dans le prompt entrent dans le modèle maître sous la forme `{%{{{annotations}}}%}` (le modèle intégré contient un bloc *Author's notes on the story* ; un modèle personnalisé doit utiliser la variable). Seules les notes **ouvertes** portant cette case sont transmises, et uniquement pour les parties qui tiennent dans le prompt. Utilisez-la pour ce que le modèle doit garder en tête pendant la suite (*ce personnage cache la lettre*), et non pour des remarques de relecture.

<a id="redrafting-to-address-the-notes"></a>

## Réécrire pour traiter les notes

Lorsqu'une partie a des notes ouvertes, la fenêtre de [réécriture](story-editor.md#redrafting-a-part) présente une case **Deal with the open annotations of the part**. Si elle est cochée, le modèle reçoit les notes (le passage et la note) avec vos instructions et réécrit la partie pour les traiter ; lorsque vous appliquez la réécriture avec **Apply**, ces notes sont marquées comme résolues. Dans la [réécriture d'une sélection](story-editor.md#redrafting-only-a-passage), seules les notes dont le passage chevauche la sélection sont transmises.

<a id="commands"></a>

## Commandes

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`, `/resolve-annotation`, `/reattach-annotation` et `/delete-annotation` permettent de travailler avec les notes depuis la ligne de commande. Les extensions peuvent utiliser les mêmes commandes. [`/generate-annotation <part>`](../commands/index.md#annotations) est **Annotate with AI** sous forme de commande : le modèle relit la partie avec le modèle du livre (un agent utilise son propre modèle lorsque *Write the book with the model of the agent* est activé ; `profile` choisit un autre profil de modèle de l'agent), avec les instructions, le format de réponse et le prompt du livre, et chaque note trouvée est ajoutée immédiatement, puisqu'il n'y a personne pour les choisir. Il s'agit d'une seule requête au modèle : elle s'exécute seule et consomme des jetons.

<a id="annotations-and-agents"></a>

## Annotations et agents

Un [agent](../agents.md) lit et écrit les annotations avec les mêmes commandes : il peut annoter un livre entier (*continuity*, *style*...), résoudre les notes qu'il a traitées, les modifier ou les supprimer. Il travaille sur une copie du livre, donc rien n'atteint vos notes tant que vous n'avez pas [examiné le résultat](../agents.md#reviewing-the-result) :

- Une note créée par l'agent apparaît sur une ligne **Annotation - Added** ; une note qu'il a résolue, rejetée ou réécrite est **Changed** (avec la note, le type, le statut et *give to the model* comme champs, chacun avec sa propre case **Take this change**) ; une note qu'il a supprimée est **Deleted**. Les notes d'une partie supprimée par l'agent disparaissent avec la partie et n'ont pas de ligne.
- La ligne indique dans quelle partie se trouve la note (`3: ` et le début de la note) ; **Show in the copy** ouvre la partie dans la copie de l'agent. Une note sur une partie écrite par l'agent nécessite cette partie : cocher la note coche aussi la partie.
- **Only annotations** au-dessus de la liste ne coche que les notes. Le passage d'une note retenue reste le même lorsque le texte de la partie est identique ; lorsque vous retenez le nouveau texte de la partie proposé par l'agent, ou si votre propre texte diffère, la note retrouve ses mots dans le texte de votre livre et, si elle n'y parvient pas, devient une note portant sur la partie entière.
- Une note que vous avez écrite pendant la pause de l'agent est intégrée à sa copie lorsqu'il reprend (et une note que vous avez résolue y est résolue aussi), afin qu'il en ait connaissance ; une note que vous et l'agent avez tous deux modifiée constitue un conflit comme tout autre.
