---
label: Résumés
order: 60
---

# Résumés

Un modèle ne peut lire qu'une quantité limitée de texte à la fois (son contexte). Lorsqu'un livre dépasse cette limite, Marginalia
laisse de côté les parties les plus anciennes, et le modèle oublie ce qui s'y est passé. Les **résumés** conservent cet
historique dans le prompt, sous forme condensée.

<a id="how-summaries-work"></a>

## Fonctionnement des résumés

Un résumé appartient à une partie et couvre **cette partie et toutes les parties qui la précèdent**, jusqu'à la partie précédente qui possède un résumé :

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
            [S1]            [S2]
└──── S1 ────┘└──── S2 ──────┘ sent in full ...
```

Ici, *S1* (sur la partie n° 4) couvre les parties n° 1 à 4, et *S2* (sur la partie n° 8) couvre les parties n° 5 à 8.

Lors de la génération de la partie suivante :

- tous les résumés de la branche active sont insérés dans le modèle principal, du plus ancien au plus récent, dans le bloc
  `{%{{{summaries}}}%}` (*Summary of story so far* dans le modèle par défaut) ;
- les parties couvertes par des résumés ne sont **plus** envoyées ;
- les parties situées après le résumé le plus récent (n° 9 et n° 10 ci-dessus) sont envoyées en entier, dans la limite de ce qui tient.

Les résumés ne sont créés que sur demande ; Marginalia ne résume pas de lui-même.

<a id="creating-a-summary"></a>

## Créer un résumé

1. Choisissez la partie qui termine une section de l'histoire que vous voulez condenser, par exemple la dernière partie d'un chapitre.
2. Dans son menu (☰), choisissez **Générer un résumé**.
3. La boîte de dialogue du résumé affiche le résumé au fur et à mesure que le modèle l'écrit (ainsi que son raisonnement, pour les modèles de raisonnement). **Arrêter** interrompt la génération et supprime le résumé. La boîte de dialogue ne bloque pas l'histoire : **Fermer** pendant que le modèle écrit la réduit dans le [panneau des exécutions](story-editor.md#the-panel-of-the-runs), et le résumé continue de se générer (la partie ne peut pas être supprimée ni fusionnée entre-temps) ; rouvrez-la depuis ce panneau pour le lire.
4. Une fois la génération terminée, fermez la boîte de dialogue avec **Quitter**.

![Un résumé en cours de génération](../../images/summary-dialog.png)

Le menu de la partie propose désormais **Voir le résumé**, qui rouvre le résumé, et **Supprimer le résumé**, qui le supprime. Pour modifier un résumé, éditez-le dans la [vue d'ensemble des résumés](#the-summaries-overview), ou supprimez-le et régénérez-le,
éventuellement avec un autre [prompt de résumé](#the-summary-prompt).

Le résumé est écrit par le modèle du livre, avec le [protocole](../protocols.md) du livre, et doit tenir dans la
[limite de contexte](../protocols.md#limits) avec le texte des parties qu'il couvre. Pour résumer une longue section avec un petit modèle, procédez par étapes plus courtes.
<a id="the-summaries-overview"></a>

## La vue d'ensemble des résumés

Le bouton **Résumés** (l'icône de liste, à gauche du menu des réglages ⚙) dans la barre inférieure de l'éditeur d'histoire ouvre la vue d'ensemble de tous les résumés de la branche active. Survolez le bouton pour voir combien de jetons les résumés utilisés occupent dans le prompt.

![Le bouton Résumés avec les jetons des résumés utilisés](../../images/summaries-menu-button.png)

La fenêtre liste les résumés que la prochaine génération enverra, du plus ancien au plus récent, et la somme de leurs jetons en haut.

![La vue d'ensemble des résumés](../../images/summaries-dialog.png)

| Colonne | |
|---|---|
| **ID** | Identifiant en base de données du résumé. Un `-` marque un résumé [remplacé](#meta-summaries) par un méta-résumé. |
| **ID du message** | Identifiant en base de données de la partie à laquelle appartient le résumé (le même numéro que celui que l'plan de l'éditeur d'histoire affiche comme *DB ID*). |
| **Ordre** | Position de cette partie dans la branche, `1` est la première partie. |
| **Contenu** | Le raisonnement du modèle (replié, s'il s'agissait d'un modèle de raisonnement) et le texte du résumé, dans une zone défilante de la même hauteur pour chaque résumé. |
| **Jetons** | Taille du texte du résumé. |
| **Sélectionner** | Coche le résumé pour un [méta-résumé](#creating-a-meta-summary). |
| icône de corbeille | Supprime le résumé. |

Les résumés fusionnés dans un méta-résumé sont affichés sous celui-ci, voir [plus bas](#meta-summaries). Ils n'ont ni case **Sélectionner**, ni icône de corbeille, ni crayon : seuls les résumés utilisés peuvent être modifiés.

La barre de menus au-dessus de la grille comporte **Rechercher et remplacer** (voir [plus bas](#search-and-replace-in-the-summaries)), **Créer des résumés...** (voir [plus bas](#create-summaries)), **Refaire tous les résumés** (voir [plus bas](#resummarize-all-summaries)), **Exporter les résumés** et **Importer des résumés** (voir [Exporter et importer](#export-and-import-of-the-summaries)) ; des [extensions](../extensions/index.md) peuvent y ajouter des outils.

<a id="editing-a-summary"></a>

### Modifier un résumé

Cliquez sur le crayon à droite du contenu d'un résumé. Le texte devient une zone de saisie de la même taille. Cliquez sur la coche verte pour enregistrer la modification, ou sur l'icône de corbeille rouge pour la jeter. Le nombre de jetons est recompté après l'enregistrement.

![Modifier un résumé](../../images/summary-edit.png)

Modifier un résumé change ce que le modèle reçoit, rien d'autre : le résumé reste valide. Seul le texte peut être modifié, pas le raisonnement.

<a id="search-and-replace-in-the-summaries"></a>

### Rechercher et remplacer dans les résumés

**Rechercher et remplacer** dans la barre de menus de la vue d'ensemble modifie un nom ou une expression dans les résumés uniquement, par exemple lorsqu'un personnage a été renommé, et laisse l'histoire intacte. C'est la [recherche et remplacement de l'histoire](story-editor.md#search-and-replace) avec les mêmes champs et options, à la différence près qu'il n'y a pas de **Rechercher dans les résumés** (les résumés sont tout ce qu'il examine). Le tableau liste chaque résumé comportant une correspondance, et les correspondances qu'il contient.

Il examine aussi les résumés qu'un méta-résumé remplace et le résumé qu'un méta-résumé a remplacé (ils sont affichés sous le méta-résumé et reviennent lorsqu'on le [défait](#unwinding-a-meta-summary)) : une correspondance dans ceux-ci est listée avec le méta-résumé qui les contient.

Le texte des résumés fusionnés fait partie de ce qui rend un méta-résumé valide ; le remplacement recalcule donc la valeur de contrôle de ces méta-résumés, qui restent valides. **Revalider tous les résumés** (activé par défaut) le fait pour chaque résumé du livre, y compris ceux que le remplacement n'a pas touchés et ceux qui étaient déjà périmés, voir [la recherche dans l'histoire](story-editor.md#search-and-replace). Désactivé, un méta-résumé sur une branche non parcourue (**Rechercher dans toutes les branches** désactivé) deviendrait périmé, et la fenêtre indique combien il y en a et demande s'il faut continuer.

<a id="create-summaries"></a>

### Créer des résumés

**Créer des résumés...** (également dans le menu des réglages ⚙ de l'histoire, **Analyse**, et proposé après un [import](story-editor.md#after-an-import)) écrit en une fois les résumés d'une histoire longue, par exemple d'un livre importé : il découpe l'histoire en blocs et fait un résumé pour chacun, l'un après l'autre, du plus ancien au plus récent. Un résumé couvre les parties jusqu'à la partie suivant le résumé qui le précède, comme le fait le résumé d'une partie unique ; les blocs se suivent donc sans trou.

- **Où il commence.** Seules les parties **après le résumé le plus récent** reçoivent des résumés (toute l'histoire s'il n'y en a aucun). Les résumés existants sont ignorés : ils ne sont ni réécrits, ni modifiés, et ne décident pas où finissent les nouveaux blocs. Pour réécrire les plus anciens, utilisez [Refaire tous les résumés](#resummarize-all-summaries).
- **Découper l'histoire par** indique où se termine un bloc :
  - **Nombre de jetons** : après la partie qui amène le bloc à **Jetons par résumé** (la coupe est arrondie à la fin de cette partie ; un bloc a au moins ce nombre de jetons, autant de parties qu'il en faut).
  - **Chapitres** : avant une partie qui commence un chapitre, de sorte que le résumé va jusqu'à la partie qui le précède. Un chapitre est une partie qui commence par un titre (le niveau de titre le moins profond par lequel une partie commence : `#` dans un livre fait par l'[import](managing-books.md#importing-a-finished-manuscript)). **Minimum de jetons avant qu'un chapitre ne termine un résumé** évite que les chapitres courts aient leur propre résumé : lorsque le bloc en cours a moins de jetons, le chapitre qui commence ne le termine pas et il est résumé avec le suivant.
  - **Nombre de parties** : après chaque série de **Parties par résumé** parties.
- **Jusqu'à la partie** arrête la plage à une partie (vide signifie la fin de l'histoire). **Résumer aussi le reste à la fin** donne un résumé aux parties qui suivent la dernière coupe, qui sont moins nombreuses qu'un bloc (avec des chapitres seulement lorsqu'elles atteignent le minimum de jetons) ; désactivez-le pour laisser sans résumé les parties les plus récentes, comme elles le sont pendant que vous écrivez.
- Un bloc ne devient jamais plus long que ce que le modèle peut lire (80 % du contexte, sans la réponse) : il se termine avant la partie qui le dépasserait. Une partie seule plus longue que cela échoue avec l'erreur habituelle.

![Créer des résumés : par chapitres, avec un minimum de 500 jetons](../../images/summaries-create.png)

Une ligne sous les options indique combien de résumés seront écrits, pour combien de parties et de jetons. **Démarrer** ouvre la fenêtre avec le numéro du résumé en cours d'écriture et son texte au fur et à mesure. La fenêtre se ferme lorsque le dernier est écrit.

![La fenêtre pendant l'écriture des résumés](../../images/summaries-create-progress.png)

Chaque résumé est enregistré dès qu'il est écrit : **Arrêter**, ou une requête échouée, conserve les résumés qui le précèdent, et **Créer des résumés** reprend à partir de là la fois suivante (il commence après le résumé le plus récent). Le livre ne doit pas être verrouillé par un [agent](../agents.md#the-book-is-locked) ; chaque résumé est une requête au modèle du livre.

<a id="resummarize-all-summaries"></a>

### Refaire tous les résumés

Après avoir modifié le prompt de résumé (ou le prompt de méta-résumé), les résumés que vous avez déjà suivent toujours l'ancien. **Refaire tous les résumés** les réécrit tous avec le modèle du livre, afin qu'ils se ressemblent tous. Il demande d'abord confirmation et indique combien de résumés il va écrire : une requête chacun.

![La question avant de réécrire les résumés](../../images/summaries-resummarize-confirm.png)

- Il commence par le résumé le **plus ancien** et va jusqu'au plus récent.
- Un **méta-résumé** est écrit après les résumés qu'il remplace (ceux qui sont affichés sous lui, les méta-résumés internes avant les externes), à partir de leurs textes **nouveaux**, de sorte qu'un méta-résumé n'est jamais fait d'un texte ancien.
- Chaque résumé garde sa place sur sa partie. La valeur de contrôle de chacun est recalculée à partir de l'histoire telle qu'elle est maintenant, de sorte qu'un résumé **périmé** est valide ensuite (le modèle a lu le texte actuel des parties).
- La fenêtre affiche le numéro du résumé en cours d'écriture et le texte au fur et à mesure. Les nouveaux textes ne sont enregistrés qu'une fois le **dernier** terminé : si vous appuyez sur **Arrêter**, ou si une requête échoue (le prompt ne tient pas dans le contexte, le modèle ne répond rien), chaque résumé reste tel qu'il était.

![La fenêtre pendant l'écriture des résumés](../../images/summaries-resummarize.png)

Les résumés des autres branches ne sont pas modifiés. Assurez-vous que le modèle n'est pas occupé et que le livre n'est pas travaillé par un agent. Les modifications que vous avez faites à la main dans les textes sont remplacées ; exportez d'abord les résumés si vous voulez les conserver.

<a id="export-and-import-of-the-summaries"></a>

### Exporter et importer les résumés

**Exporter les résumés** télécharge les résumés utilisés sur la branche active dans un seul fichier texte (`<book>-summaries.md`), du plus ancien au plus récent, chacun sous une ligne comme `<!-- summary part="..." order="3" -->`. Modifiez les textes dans n'importe quel éditeur, gardez les lignes d'en-tête, et réimportez le fichier avec **Importer des résumés** : choisissez le fichier, le tableau indique ce qui arriverait à chaque résumé, et rien n'est écrit tant que vous n'appuyez pas sur **Importer**.

| Statut | |
|---|---|
| **Va changer** | La partie désignée par l'en-tête a un résumé utilisé et son texte diffère. |
| **Va changer (par position)** | Voir ci-dessous. |
| **Inchangé** | Le texte est celui que le résumé possède. |
| **Partie introuvable** | La branche n'a aucun résumé utilisé pour cet en-tête. |
| **Texte vide** | Le texte sous l'en-tête est vide ; un import ne vide jamais un résumé. |

![La fenêtre d'import avec un fichier modifié](../../images/summaries-import.png)

Un en-tête désigne la partie par son identifiant, de sorte qu'un fichier rapporte les résumés au livre dont il provient. Pour déplacer les résumés vers un autre livre (une copie a des parties aux identifiants différents), cochez **Associer par position** : un résumé dont la partie ne figure pas dans cette branche va au résumé de la partie située à la même position (le `order` de l'en-tête, la racine vaut 1). Vérifiez d'abord le tableau : une position ne convient que si les deux histoires ont la même forme.

Le fichier contient les résumés utilisés par la génération, et un méta-résumé en fait partie. Les résumés qu'un méta-résumé remplace n'y figurent pas et ne sont jamais modifiés : leur texte fait partie de ce qui rend le méta-résumé valide (voir [Méta-résumés](#meta-summaries)), donc une modification de ceux-ci le rendrait périmé. Un import ne modifie que le texte et le nombre de jetons d'un résumé, comme le fait le crayon ; aucun résumé ne devient donc périmé par lui. Il est refusé pendant qu'un agent travaille sur le livre.

<a id="deleting-a-summary"></a>

### Supprimer un résumé

L'icône de corbeille d'un résumé fait la même chose que **Supprimer le résumé** dans le menu de la partie. Pour un méta-résumé, elle demande ce qu'il faut faire, voir [Défaire un méta-résumé](#unwinding-a-meta-summary).
<a id="meta-summaries"></a>

## Résumés méta

Les résumés restent dans le prompt à titre définitif, de sorte qu'un long livre remplit le contexte de ceux-ci. Un **méta-résumé** condense
plusieurs résumés en un seul : c'est un résumé de résumés.

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
        [S1]        [S2]        [S3]
        └─────────── M ─────────────┘
```

Ici *M* fusionne *S1*, *S2* et *S3*. Il appartient à la partie du résumé fusionné le plus récent (#9 ci-dessus, qui contenait *S3*)
et remplace ce résumé sur la partie. À partir de là :

- seul *M* entre dans le bloc `{%{{{summaries}}}%}`, à la place de *S1*, *S2* et *S3* ;
- *S1* et *S2* restent sur leurs parties, mais ne sont pas envoyés. Ils sont affichés sous *M* dans la vue d'ensemble ;
- *S3*, le résumé que *M* a remplacé, est conservé à l'intérieur de *M*, affiché sous lui avec un badge **Remplacé**.

Les parties couvertes par les résumés fusionnés ne sont toujours pas envoyées en entier ; un méta-résumé ne change pas ce que couvrent les résumés, seulement la longueur du texte qui les représente.

<a id="creating-a-meta-summary"></a>

### Créer un méta-résumé

1. Ouvrez la [vue d'ensemble des résumés](#the-summaries-overview).
2. Cochez la case **Sélectionner** des résumés à fusionner. Deux au minimum. Vous ne cochez que les résumés en usage (ceux du niveau supérieur de la grille).
3. Cliquez sur **Créer un méta-résumé**.

Marginalia fusionne tout, du résumé coché le plus récent au plus ancien, y compris les résumés intermédiaires qui n'étaient pas cochés. La boîte de dialogue du méta-résumé fonctionne comme celle du résumé : elle affiche le texte au fur et à mesure que le modèle l'écrit, et **Arrêter** annule la génération sans rien modifier. Quand elle se termine, fermez la boîte de dialogue avec **Quitter** ; la vue d'ensemble affiche alors le résultat.

![Résumés cochés et méta-résumé en cours de génération](../../images/meta-summary-create.png)

Un méta-résumé peut aussi fusionner des méta-résumés : cochez-les comme n'importe quel autre résumé. Les résumés fusionnés sont affichés sous le nouveau, avec leurs propres résumés fusionnés sous eux.

Les résumés à fusionner et la requête adressée au modèle doivent tenir dans la [limite de contexte](../protocols.md#limits) ; avec un petit modèle, fusionnez donc quelques résumés à la fois.

<a id="unwinding-a-meta-summary"></a>

### Dérouler un méta-résumé

Supprimer un méta-résumé (l'icône de corbeille dans la vue d'ensemble, ou **Supprimer le résumé** dans le menu de la partie) pose une question :

| Bouton | |
|---|---|
| **Dérouler** | Supprime le méta-résumé et remet sur la partie le résumé qu'il remplaçait. Les résumés fusionnés dans un méta-résumé composé de méta-résumés reviennent un niveau à la fois, dans l'ordre inverse de leur création. |
| **Supprimer** | Supprime le méta-résumé et laisse la partie sans résumé. Les résumés qu'il fusionnait redeviennent en usage (mais celui qu'il remplaçait a disparu). |
| **Annuler** | Ne fait rien. |

Après un déroulement, les résumés fusionnés sont de nouveau en usage et la vue d'ensemble les montre au niveau supérieur.

![Suppression d'un méta-résumé](../../images/meta-summary-delete.png)

<a id="the-meta-summary-prompt"></a>

### Le prompt du méta-résumé

La requête pour un méta-résumé est le **Prompt de méta-résumé par défaut** du livre (dans l'onglet [Prompts](prompts.md), ou le modèle par défaut dans *Paramètres → Modèles*). Elle reçoit deux variables :

| Variable | |
|---|---|
| `{%{{{summaryBlocks}}}%}` | Les résumés à fusionner, du plus ancien au plus récent, séparés par une ligne vide. |
| `{%{{{backgroundLore}}}%}` | Les entrées du lorebook actives au moment de l'écriture de la partie couverte par le méta-résumé. |

Le prompt intégré demande d'enchaîner les événements en entrées plus courtes — environ moitié moins longues — sans perdre les noms, objets, accords ni fils narratifs non résolus, et sans rien ajouter de nouveau.

<a id="when-summaries-become-outdated"></a>

## Quand les résumés deviennent obsolètes

Un résumé n'est valide que pour le texte exact dont il a été tiré. Lorsque vous [modifiez](story-editor.md#editing-the-text) ou [supprimez](story-editor.md#deleting-a-part) une partie couverte par un résumé, celui-ci ne correspond plus.

Les résumés obsolètes ne sont pas envoyés au modèle. Marginalia le vérifie lorsqu'il génère la partie suivante et demande *Les résumés des messages d'identifiants : [...] sont obsolètes ... Poursuivre la génération et les supprimer ?* Répondez **Oui** pour les supprimer et générer ; les parties qu'ils couvraient sont alors envoyées en entier (dans la limite de ce qui tient) jusqu'à ce que vous les résumiez de nouveau. Répondez **Non** pour arrêter : rien n'est supprimé, et vous pouvez d'abord traiter les résumés.

Dans la [vue d'ensemble des résumés](#the-summaries-overview), un résumé obsolète a un **fond rouge** et une étiquette **Obsolète**, et ses jetons ne sont pas comptés dans la somme en haut. S'il dit encore la bonne chose (vous avez corrigé une coquille, par exemple), appuyez sur le bouton **coche** de sa ligne pour le **revalider**, ou choisissez **Revalider tous les résumés** dans la barre de menus pour revalider d'un coup tous les obsolètes. Sinon, supprimez-le ou générez-le de nouveau. Un méta-résumé est vérifié comme n'importe quel résumé ; les résumés qu'il fusionne en font partie et ne deviennent jamais obsolètes par eux-mêmes. La revalidation est refusée pendant qu'un agent travaille sur le livre.

Lorsque vous enregistrez la [modification d'une partie](story-editor.md#editing-the-text) qui rendrait des résumés obsolètes, Marginalia vous prévient d'abord et vous laisse revenir à la modification, **Enregistrer quand même**, ou enregistrer et revalider les résumés.

![La question posée lorsqu'un enregistrement rend des résumés obsolètes](../../images/summaries-save-warning.png)

Après **Enregistrer quand même**, la vue d'ensemble des résumés affiche le résumé comme obsolète (en rouge, avec l'étiquette **Obsolète**, et non compté dans la somme en haut) :

![Un résumé obsolète dans la vue d'ensemble](../../images/summaries-outdated.png)

Une [recherche et remplacement](story-editor.md#search-and-replace) dans l'histoire conserve valides les résumés qu'elle couvre et prévient ceux qui deviendraient obsolètes. La [recherche et remplacement des résumés](#search-and-replace-in-the-summaries) garde les méta-résumés valides.

Un méta-résumé est obsolète lorsqu'une partie qu'il couvre est modifiée ou supprimée, mais aussi lorsqu'un résumé qu'il fusionne est supprimé. (C'est pourquoi les résumés fusionnés ne peuvent pas être modifiés un à un : le changement rendrait le méta-résumé obsolète. La [recherche et remplacement](#search-and-replace-in-the-summaries) recalcule la valeur de contrôle du méta-résumé.) Supprimer un méta-résumé obsolète revient à le dérouler : le résumé qu'il remplaçait revient, et il est lui aussi vérifié. S'il est obsolète également, il est supprimé de la même manière.

Lorsque vous [créez une branche](branches-and-story-tree.md#branching-from-an-earlier-part) à partir d'une partie qui a un résumé, la nouvelle branche reçoit une copie du résumé. Un méta-résumé copié conserve ce qu'il remplaçait, de sorte qu'il peut être déroulé dans la nouvelle branche.

<a id="the-summary-prompt"></a>

## Le prompt de résumé

La requête pour un résumé est le **Prompt de résumé par défaut** du livre (dans l'onglet [Prompts](prompts.md), ou le modèle par défaut dans *Paramètres → Modèles*). Elle reçoit deux variables :

| Variable | |
|---|---|
| `{%{{{text}}}%}` | Le texte des parties à résumer, du plus ancien au plus récent. |
| `{%{{{backgroundLore}}}%}` | Les entrées du lorebook actives au moment de l'écriture de la partie résumée. |

Le prompt intégré demande un registre compact d'événements, regroupés par lieu : sous un en-tête `[LOCATION / SCENE: Name]`, un événement par ligne, sous la forme *qui fait quoi -> ce qui en découle* (l'intention seulement lorsque le texte la montre), sans titres, numéros ni identifiants pour les événements, et sans l'ambiance. Il est destiné au modèle, pas à un lecteur, et il fait environ un quart de la taille du texte qu'il remplace. Le prompt de méta-résumé attend le même format et fusionne les lignes voisines en chaînes de causes à effets. Remplacez-le si vous préférez un autre format ou une autre longueur ; un résumé produit avant la modification du prompt conserve son ancienne forme jusqu'à ce que vous le refassiez.

<a id="tips"></a>

## Conseils

- Résumez aux coupures naturelles : la fin d'un chapitre ou d'une scène.
- Lorsque les résumés commencent à occuper trop de contexte (l'info-bulle du bouton **Résumés** indique la part), fusionnez les plus anciens en un méta-résumé. Répétez plus tard avec le méta-résumé et les résumés qui suivent.
- Les résumés restent dans le prompt à titre définitif. Avec beaucoup de résumés, le prompt peut à lui seul remplir le contexte, et la génération s'arrête avec *La limite de contexte est insuffisante.* Raccourcissez alors les résumés (un prompt de résumé plus concis), fusionnez-les en [méta-résumés](#meta-summaries) ou augmentez la limite de contexte.
- Gardez les faits qui ne doivent jamais être oubliés (noms, relations, règles du monde) dans le [lorebook](../lorebooks/index.md) plutôt que de compter sur les résumés.
- Vérifiez ce que le modèle a reçu avec **Afficher le prompt** sur la partie suivante.
