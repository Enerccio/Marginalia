---
label: Entrées et activation
order: 90
---

# Entrées et activation

<a id="the-entry-list"></a>

## La liste des entrées

Chaque ligne de l'éditeur de lore correspond à une entrée :

| Colonne | |
|---|---|
| case à cocher | **Activé**. Les entrées désactivées ne sont jamais utilisées. |
| **Nom de l'entrée** | Votre nom pour l'entrée. Non envoyé au modèle. |
| **Ordre** | Position de l'entrée dans le prompt, voir [Ordre et insertion](#order-and-insertion). Les nouvelles entrées reçoivent `100`. |
| **Mots-clés** | Mots-clés qui activent l'entrée, voir [Mots-clés](#tags). |
| ⌄ | Affiche les détails de l'entrée. |
| agrandir | Ouvre l'entrée dans [l'éditeur plein écran](lorebooks.md#editing-in-full-screen) (ordinateur). |
| corbeille | Supprime l'entrée (après confirmation). |

**Ajouter une entrée** dans la barre d'outils ajoute une entrée d'ordre `100`. Les modifications sont enregistrées au fur et à mesure de la saisie.

<a id="searching-and-deleting-many-entries"></a>

### Chercher et supprimer plusieurs entrées

La zone avec la loupe, à côté des boutons, n'affiche que les entrées dont le nom, le contenu, la note ou le filtre contient le texte (la casse n'a pas d'importance). Videz-la pour revoir toutes les entrées. Elle ne regarde que les entrées du livre de lore sélectionné, pas les sous-livres de lore. Sur l'affichage téléphone, la zone se trouve au-dessus de la liste.

À côté de la zone se trouvent deux autres listes qui limitent l'affichage. **Mots-clés** affiche les entrées qui ont ce mot-clé (elle liste les mots-clés qu'au moins une entrée du livre de lore possède ; les mots-clés *négatifs* ne sont pas pris en compte). **Statut** affiche **Toutes**, **Activé**, **Désactivé** ou **Constantes (sans clés)** : les entrées activées et sans filtre, donc utilisées pour chaque partie. La recherche, le mot-clé et le statut se combinent : une entrée doit correspondre à tous les critères. Sur l'affichage téléphone, les deux listes se trouvent sous la zone de recherche.

La colonne de cases à cocher à gauche sélectionne des entrées (la case de l'en-tête sélectionne toutes celles qui sont affichées ; une recherche suivie de la case de l'en-tête sélectionne donc les correspondances). Quand une sélection existe, **Supprimer la sélection** apparaît dans la barre d'outils avec le nombre d'entrées ; le bouton demande une seule confirmation, puis déplace les entrées vers la [corbeille](../trash.md). La [modification en masse](lorebooks.md#mass-edit) possède aussi un bouton **Supprimer les éléments cochés**. Sur téléphone, le menu **⋮** des entrées contient **Select entries** : des cases apparaissent devant les entrées, et **Supprimer la sélection** apparaît avec le nombre d'entrées cochées (une entrée possède aussi son propre bouton **Delete entry**).


<a id="search-and-replace"></a>

## Rechercher et remplacer

**Rechercher et remplacer** (à droite de **Ajouter une entrée**) modifie un mot, un nom ou une expression dans le nom, le texte et le commentaire de toutes les entrées du livre de lore d'un coup. Il fonctionne comme la [recherche et remplacement de l'histoire](../books/story-editor.md#search-and-replace), sans les options pour les résumés et les branches : **Rechercher** liste les entrées avec des correspondances (le tableau affiche leurs noms) et les correspondances dans celles-ci, en l'état et telles qu'elles seront ; **Remplacer** modifie les entrées. Il possède les options **Rechercher par expression régulière** (voir [Expressions régulières](../regular-expressions.md)) et **Respecter la casse** (activée par défaut ; verrouillée tant que l'expression régulière est active, ce qui permet alors d'utiliser `(?i)` pour ignorer la casse). Les entrées désactivées sont aussi recherchées. Sur téléphone, le bouton (une icône à côté de la case *Activé*) ouvre la même fenêtre, dans la mise en page téléphone.

![La fenêtre Rechercher et remplacer d'un livre de lore, les correspondances d'une entrée ouverte](../../images/lorebook-search-replace.png)

La ligne de commande de l'éditeur de l'histoire possède `/search-replace-lore-entries` pour le livre de lore du livre ouvert, voir [Commandes](../commands/index.md#commands-that-come-with-marginalia).

<a id="entry-details"></a>

## Détails d'une entrée

Le bouton ⌄ ouvre les détails :

![Une entrée avec ses détails](../../images/lorebook-entry.png)

| Champ | |
|---|---|
| **Contenu** | Le texte qui entre dans le prompt. Un [modèle](#templates-in-entries) : il peut utiliser des variables et des macros. |
| **Mode de filtrage** | **Mots-clés (n'importe lequel)**, **Mots-clés (tous)**, **Texte** ou **Expression régulière**, selon la façon dont *Texte/motif de filtrage* est comparé. |
| **Texte/motif de filtrage** | Le filtre, voir [Filtres](#filters). Vide signifie aucun filtre. |
| **Mode d'insertion** | **Dans le bloc de lore** ou **Avant le prompt utilisateur**, voir [Ordre et insertion](#order-and-insertion). |
| **Activation Context** | L'endroit où le filtre est cherché : **Instructions** (la demande pour la partie, valeur par défaut) ou **Instructions et parties de l'histoire**, voir [Chercher dans l'histoire](#looking-into-the-story). |
| **Parties** | Avec *Instructions et parties de l'histoire* : combien des parties les plus récentes de l'histoire sont cherchées avec la demande, au minimum 1. |
| **Depuis le message** | L'entrée n'est utilisée qu'à partir de la partie portant ce numéro, voir [Limiter une entrée à certaines parties](#limiting-an-entry-to-some-parts). Vide : depuis le début. |
| **To message** | L'entrée n'est utilisée que jusqu'à la partie portant ce numéro. Vide : jusqu'à la fin. |
| **Condition** | Un modèle qui doit donner une valeur vraie pour que l'entrée soit utilisée, voir [Une condition pour une entrée](#a-condition-for-an-entry). Vide : aucune condition. |
| **Mots-clés négatifs (exclusion)** | Mots-clés qui désactivent l'entrée, voir [Mots-clés](#tags). |
| **Chercher aussi dans les résumés** | Quand elle est cochée, le filtre est aussi cherché dans les [résumés](../books/summaries.md) de l'histoire jusqu'ici, ceux que la prochaine demande enverrait (un résumé qui ne correspond plus à son histoire est écarté). Nécessite un filtre. |
| **Ne pas chercher d'autres entrées dans son contenu** | Quand elle est cochée, les mots du contenu de cette entrée n'appellent jamais d'autres entrées, voir [Entrées appelées par d'autres entrées](#entries-brought-in-by-other-entries). |
| **Seulement via d'autres entrées** | Quand elle est cochée, l'entrée n'est jamais utilisée à cause de la demande ou de l'histoire elle-même ; seulement lorsque son filtre est trouvé dans le contenu d'une autre entrée utilisée, voir [Entrées appelées par d'autres entrées](#entries-brought-in-by-other-entries). |
| **Note** | Vos notes. Non envoyées au modèle. Les entrées importées de SillyTavern listent ici les réglages qui n'ont pas pu être importés. |

Écrivez le contenu comme vous voulez que le modèle le lise : une description courte et factuelle fonctionne le mieux :

> **Captain Varro** - commandant de la garde du port d'Ostra, la cinquantaine, main gauche marquée d'une cicatrice. Loyal envers le Duc, il se méfie de la guilde des mages.
> Il parle par phrases courtes et sèches.
## Activation

Avant chaque partie générée, Marginalia parcourt les entrées du lorebook du livre et de ses [sous-lorebooks](lorebooks.md#sub-lorebooks). Une entrée est **activée** lorsque :

1. l'entrée est activée, et son lorebook aussi ;
2. la partie en cours d'écriture se trouve dans sa plage de parties, si elle en a une (**Depuis le message**, **Jusqu'au message**) ;
3. aucune de ses **étiquettes négatives** n'est une étiquette du livre ;
4. elle n'a aucune **étiquette**, ou au moins une d'entre elles est une étiquette du livre ;
5. sa **condition**, si elle en a une, est vraie, voir [Une condition pour une entrée](#a-condition-for-an-entry) ;
6. elle n'a pas de **filtre**, ou le filtre correspond aux instructions de la partie (ou, avec **Contexte d'activation** *Instructions et parties de l'histoire*, aux instructions et aux parties les plus récentes de l'histoire).

Toutes les conditions doivent être remplies. Une entrée sans étiquette ni filtre est toujours active.

<a id="tags"></a>

### Étiquettes

Les étiquettes relient les entrées aux **étiquettes du livre** (définies dans l'onglet [À propos](../books/managing-books.md#the-about-tab) du livre).
Utilisez-les pour partager un même lorebook entre des livres qui ont besoin de parties différentes de celui-ci :

| Étiquettes de l'entrée | Étiquettes négatives | Active dans un livre étiqueté... |
|---|---|---|
| - | - | toujours |
| `winter` | - | `winter` |
| `winter`, `book-2` | - | `winter` **ou** `book-2` |
| - | `book-1` | tout sauf `book-1` |
| `winter` | `book-1` | `winter`, sauf s'il est aussi `book-1` |

Les étiquettes doivent correspondre exactement. Une étiquette négative l'emporte toujours sur une étiquette.

Des étiquettes peuvent aussi être données à un lorebook entier (le champ **Étiquettes** du [lorebook](lorebooks.md) ; les lorebooks importés depuis des fichiers peuvent en porter). Elles comptent alors comme étiquettes de chacune de ses entrées.

<a id="filters"></a>

### Filtres

Un filtre n'active l'entrée que lorsque la **demande de la partie** la mentionne. Il est comparé au [prompt utilisateur](../books/prompts.md#user-prompt) de la partie tel qu'il est envoyé — le *Cadre de la scène*, le *Personnage du point de vue*, les *Personnages présents* et les *Instructions* que vous avez saisis, à l'intérieur du texte du modèle de prompt utilisateur.

| Mode | Correspond lorsque |
|---|---|
| **Mots-clés (n'importe lequel)** | Le filtre est une liste de mots-clés séparés par des virgules (`Varro, Ostra`) ; la demande en contient au moins un, sans tenir compte de la casse. Les espaces autour d'un mot-clé sont ignorés. |
| **Mots-clés (tous)** | La même liste, mais la demande doit contenir chaque mot-clé. |
| **Texte** | Le texte apparaît n'importe où dans la demande, sans tenir compte des majuscules et minuscules. `varro` correspond à *Captain Varro*. |
| **Expression régulière** | L'[expression régulière](../regular-expressions.md#in-a-lorebook-entry-filter) correspond n'importe où dans la demande, sans tenir compte de la casse, sur plusieurs lignes. |

Par exemple, l'expression régulière `\b(varro|captain)\b` correspond au mot entier *Varro* ou *Captain*, et
`varro.*(harbor|docks)` ne correspond que lorsque Varro et le port ou les quais sont tous deux mentionnés, dans cet ordre.

!!!warning Par défaut, le filtre regarde la demande, pas l'histoire
Sauf si l'entrée regarde dans l'histoire (voir [Regarder dans l'histoire](#looking-into-the-story)), un filtre vérifie uniquement les instructions de la nouvelle partie, et non le texte des parties précédentes. Une entrée sur Varro est
activée lorsque Varro est mentionné dans la scène, les personnages ou les instructions de la partie que vous écrivez — placez-le dans *Personnages présents* lorsqu'il est dans la scène.

La demande contient aussi le texte du modèle de prompt utilisateur lui-même. Un filtre portant sur un mot que le modèle utilise (celui par défaut mentionne *personnages*, *intrigue*, *ton*...) est toujours actif.
!!!

Une expression régulière invalide ne correspond jamais ; l'entrée reste inactive.

Combinez étiquettes et filtre lorsqu'une entrée ne doit être utilisée que dans certains livres, et là seulement lorsqu'elle est pertinente.

<a id="looking-into-the-story"></a>

### Regarder dans l'histoire

Par défaut, le filtre n'est cherché que dans la demande de la partie ; une entrée sur le dragon est donc utilisée lorsque *vous* mentionnez le dragon. Lorsque l'histoire doit garder l'entrée en jeu — le dragon était dans la dernière partie, votre nouvelle instruction dit seulement « il s'envole » — réglez **Contexte d'activation** de l'entrée sur **Instructions et parties de l'histoire** et **Parties** sur le nombre des parties les plus récentes à rechercher avec la demande (`1` est la partie qui précède celle en cours d'écriture, `2` les deux qui la précèdent...).

- Le filtre est recherché dans le texte de ces parties et dans la demande ensemble ; les mots-clés de **Mots-clés (tous)** peuvent donc être répartis entre elles. La demande compte toujours.
- Seul le texte des parties est recherché, pas leurs résumés, et seulement les parties de la branche que vous écrivez (une variante ou une régénération ne recherche pas la partie qu'elle remplace).
- Une histoire plus courte que **Parties** est recherchée en entier. Marginalia ne lit que le nombre de parties dont les entrées activées ont besoin ; un lorebook sans de telles entrées ne touche donc pas du tout à l'histoire.
- Les entrées amenées par [d'autres entrées](#entries-brought-in-by-other-entries) ne sont pas cherchées dans l'histoire, seulement dans le contenu des entrées utilisées.

<a id="limiting-an-entry-to-some-parts"></a>

### Limiter une entrée à certaines parties

Certains éléments de lore ne sont vrais que pour une partie de l'histoire : le déguisement d'un personnage découvert au chapitre 5, les rumeurs avant le début de la guerre. Remplissez **Depuis le message** et/ou **Jusqu'au message** avec les numéros des parties, tels que le plan de l'histoire les numérote (la première partie est `1` ; les deux numéros sont inclus). L'entrée n'est alors utilisée que lorsque la partie en cours d'écriture a un numéro compris dans la plage, quel que soit son filtre — une entrée sans filtre est une entrée constante, mais seulement à l'intérieur de la plage.

- Un champ vide n'impose aucune limite : seul **Depuis le message** `10` signifie « à partir de la partie 10 », seul **Jusqu'au message** `4` signifie « jusqu'à la partie 4 ».
- La partie en cours d'écriture est celle qui suit la dernière partie de la branche (une histoire de 9 parties écrit la partie `10`). Une variante ou une régénération écrit la partie qu'elle remplace.
- Les parties sans texte comptent aussi, comme dans le plan. Supprimer ou insérer des parties déplace les numéros.
- Si **Depuis le message** est supérieur à **Jusqu'au message**, l'entrée n'est jamais utilisée.
- Une entrée hors de sa plage n'est pas non plus amenée par [d'autres entrées](#entries-brought-in-by-other-entries).
- L'extension [VCS de lorebook](../extensions/index.md) conserve la plage dans les versions d'une entrée ; l'import, l'export et le clonage d'un lorebook conservent aussi la plage.

<a id="a-condition-for-an-entry"></a>

### Une condition pour une entrée

La **Condition** est un modèle, écrit comme le [contenu d'une entrée](#templates-in-entries), que Marginalia calcule avant chaque partie. L'entrée n'est utilisée que lorsque le résultat est **vrai** : tout sauf un résultat vide, `0`, `false`, `no` et `off` (quelle que soit la casse). Elle voit les mêmes choses que le contenu : le personnage du point de vue, les personnages présents, la scène, les instructions du tour, l'histoire jusqu'ici et les variables.

![Une entrée avec une condition : l'entrée n'est utilisée que lorsque Tomáš est le point de vue et que Mizuki est présente](../../images/lorebook-condition.png)

| Condition | L'entrée est utilisée lorsque |
|---|---|
| `{%{{{#if presentCharacters}}yes{{/if}}}%}` | le tour a des personnages présents |
| `{%{{{eval::povCharacter == "Fiora"}}}%}` | le point de vue est Fiora (voir [Expressions](../templates/macros.md#expressions)) |
| `{%{{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}}%}` | Fiora est le point de vue et Mara est présente |
| `{%{{{getvar::revealed}}}%}` | la variable `revealed` a une valeur (définie par une partie précédente, voir [Référence des macros](../templates/macros.md#variables)) |
| `{%{{{command::authors-note}}}%}` | la [commande](../commands/index.md) répond quelque chose (seule une commande conçue pour les macros peut être utilisée) |

Fonctionnement :

- Elle est vérifiée après l'interrupteur d'activation, la plage de parties et les étiquettes, et **avant le filtre** : une entrée dont la condition est fausse n'est pas utilisée, même si son filtre correspond, et elle n'est pas non plus amenée par [d'autres entrées](#entries-brought-in-by-other-entries).
- Une condition qui **ne peut pas être décidée** compte comme fausse, et l'[aperçu](#preview-activation) indique pourquoi : ce n'est pas un modèle valide, elle utilise un nom qui n'existe pas (une faute de frappe), une commande qui n'existe pas ou qui n'est pas conçue pour les macros, ou le temps est écoulé. Le champ devient rouge pour les deux premiers cas pendant la saisie.
- Elle ne fait que lire. Un `{%{{{setvar}}}%}` dans une condition ne change rien, ni pour le prompt ni pour les autres entrées, et les commandes sont celles qui ne font que lire.
- Elle est calculée une fois pour chaque entrée et chaque partie, de sorte que `{%{{{random::yes::no}}}%}` ne change pas d'avis entre la première passe et les passes de la profondeur d'analyse.
- L'[aperçu](#preview-activation) signale les entrées dont la condition est fausse : *Non utilisée : la condition est fausse*.

  ![L'aperçu de l'activation : l'entrée de Mizuki n'est pas utilisée car sa condition est fausse](../../images/lorebook-condition-preview.png)
- Toutes les conditions d'une partie partagent **60 secondes**. Lorsqu'elles sont épuisées, les conditions restantes comptent comme fausses (*condition failed*, délai dépassé). Une condition en cours d'exécution n'est pas arrêtée ; le temps est vérifié avant la suivante.
- L'import et l'export d'un lorebook, le clonage et l'extension VCS de lorebook conservent la condition.

<a id="entries-brought-in-by-other-entries"></a>

### Entrées amenées par d'autres entrées

Normalement, seule la demande de la partie active les entrées. Avec une **profondeur d'analyse** (le champ du [lorebook](lorebooks.md#the-lorebook-editor) ; `0`, la valeur par défaut, désactive cette fonction), le contenu des entrées activées est cherché pour trouver les filtres des entrées qui ne l'étaient pas, comme s'il faisait partie de la demande. Une entrée dont le filtre y est trouvé est activée aussi. C'est la **passe 1** ; le contenu des entrées qu'elle a activées est cherché à la passe 2, et ainsi de suite, autant de passes que l'indique la profondeur d'analyse (5 au maximum).

Par exemple, avec une profondeur d'analyse de `1`, une entrée *Captain Varro* (filtre `varro`) est activée par la demande, et son contenu dit « ...sert le duc d'Ostra ». Une entrée *Ostra* (filtre `ostra`) est alors activée aussi, bien que la demande ne mentionne jamais la ville. Avec une profondeur d'analyse de `2`, l'entrée *Ostra* amènerait à son tour une entrée sur le port qu'elle mentionne.

- Seuls les **filtres** comptent : une entrée déjà utilisée parce qu'elle n'a pas de filtre n'a besoin de rien, et une entrée désactivée, ou exclue par ses étiquettes, n'est pas amenée.
- Le contenu cherché est le texte de l'entrée tel qu'il est écrit ; les modèles qu'il contient ne sont pas exécutés.
- La **profondeur d'analyse** du lorebook que **le livre utilise** compte pour ses propres entrées et pour les entrées de ses sous-lorebooks. Un sous-lorebook peut avoir une profondeur propre (**Profondeur d'analyse comme sous-lorebook** dans ses réglages ; vide : celle du lorebook que le livre utilise) : ses entrées ne sont alors trouvées par le contenu des autres que dans les passes jusqu'à cette profondeur, `0` jamais. Le nombre de passes effectuées est celui de la plus grande profondeur. Le champ d'un sous-lorebook ne compte que tant qu'il est un sous-lorebook ; lorsqu'un livre l'utilise directement, son **Profondeur d'analyse** normale compte.
- Une entrée avec **Ne pas chercher d'autres entrées dans son contenu** cochée peut être activée, mais elle n'en amène aucune.
- Une entrée avec **Seulement via d'autres entrées** cochée n'est jamais trouvée par la demande : elle n'est utilisée que lorsque le contenu d'une entrée utilisée contient son filtre. Sans profondeur d'analyse, ou sans filtre, elle n'est jamais utilisée ; l'[aperçu](#preview-activation) indique *Non utilisée : utilisée seulement via le contenu d'autres entrées*.
- L'ordre dans le prompt ne dépend pas de la façon dont une entrée a été trouvée, voir [Ordre et insertion](#order-and-insertion).
- Les extensions qui modifient les entrées utilisées (voir [Extensions](../extensions/index.md)) sont consultées aussi sur les entrées amenées par d'autres.
<a id="preview-activation"></a>

## Aperçu de l'activation

**Aperçu de l'activation** (le bouton à l'œil dans la barre d'outils de l'éditeur de lorebook, et sur téléphone à côté du bouton de recherche et de remplacement) répond à la question « quelles entrées seraient utilisées pour ce texte ? » sans rien générer. Saisissez le texte de la demande dans **Texte du tour** (la scène, les personnages et les instructions, tels que vous les saisiriez) et le tableau liste chaque entrée du lorebook et de ses sous-lorebooks, dans l'ordre où elles sont utilisées :

- une coche verte pour les entrées qui seraient utilisées, et une marque grise pour les autres ;
- le lorebook de l'entrée, son **Ordre**, la **Passe** (vide pour les entrées que le texte a activées, `1`, `2`... pour les entrées appelées par [d'autres entrées](#entries-brought-in-by-other-entries)) ;
- la **Raison** : aucun filtre, filtre trouvé dans le texte, filtre trouvé dans le contenu d'une autre entrée (avec le filtre et l'entrée), ou pourquoi non : désactivée, la partie est hors de sa plage, un mot-clé négatif du livre, le livre n'a pas le mot-clé de l'entrée, la condition est fausse ou ne peut pas être tranchée, le filtre n'a pas été trouvé.

Les mots-clés comptent aussi : choisissez un livre dans **Mots-clés du livre** pour utiliser ses mots-clés (sans livre, aucun mot-clé ne compte). Ce livre fournit aussi les parties de son histoire pour les entrées qui [consultent l'histoire](#looking-into-the-story) (sans livre, elles ne cherchent que dans le texte) et le numéro de la prochaine partie pour les entrées [limitées à certaines parties](#limiting-an-entry-to-some-parts) (sans livre, leur plage n'est pas vérifiée). Le texte que vous saisissez sert d'instructions du tour pour les [conditions](#a-condition-for-an-entry) ; avec un livre, ses variables et son point de vue, les personnages présents et la scène de sa dernière partie sont utilisés aussi (les commandes d'une condition ne sont pas exécutées dans l'aperçu). Sous le texte, une ligne indique combien d'entrées seraient utilisées et environ combien de jetons de lore cela représente. La profondeur d'analyse du lorebook est utilisée.

Dans l'[éditeur plein écran](lorebooks.md#editing-in-full-screen), l'œil se trouve aussi sur la ligne du nom de chaque onglet d'entrée : l'aperçu s'ouvre pour cette entrée, sa ligne est sélectionnée, et une ligne au-dessus du tableau indique si elle serait utilisée et pourquoi, ce qui change à mesure que vous tapez. Modifiez le filtre dans l'onglet, puis appuyez sur **Actualiser** dans l'aperçu pour voir l'effet.

![Aperçu de l'activation d'un lorebook](../../images/lorebook-preview-activation.png)

L'aperçu applique uniquement les règles de Marginalia : le texte est celui que vous saisissez (et non le modèle de prompt utilisateur qui l'entoure), et ce qu'une [extension](../extensions/index.md) modifie pendant la génération d'une partie n'en fait pas partie.

<a id="order-and-insertion"></a>

## Ordre et insertion

Les entrées activées sont placées dans le prompt, triées par **Ordre**, du plus petit au plus grand. Les entrées de même ordre gardent l'ordre dans lequel elles ont été créées. Les entrées de tous les lorebooks inclus sont triées ensemble.

**Mode d'insertion** détermine où va une entrée :

| Mode | Emplacement |
|---|---|
| **Dans le bloc de lore** | Dans le bloc `{%{{{backgroundLore}}}%}` du [modèle principal](../books/prompts.md#master-template) - *World & lore context* dans le modèle par défaut, dans le message système. À utiliser pour les connaissances de fond. |
| **Avant le prompt utilisateur** | Juste avant la demande de la partie, à la fin du prompt. Le modèle y prête le plus d'attention. À utiliser avec parcimonie, pour ce qui doit influencer cette partie précise (l'état actuel d'un personnage, une règle pour la scène). |

Les entrées sont séparées par une ligne vide. Le lore compte dans la [limite de contexte](../protocols.md#limits) ; avec beaucoup d'entrées longues, moins de l'histoire tient dans le prompt.

<a id="templates-in-entries"></a>

## Modèles dans les entrées

Le contenu d'une entrée est un modèle, comme les [prompts du livre](../books/prompts.md#templates). Il peut utiliser :

| Variable | |
|---|---|
| `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` | Les instructions de la partie. |
| `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}` | Les réglages narratifs du livre. |

et toutes les [macros](../templates/macros.md). Par exemple, une entrée qui s'adapte au personnage du point de vue :

{%{
```handlebars
Varro {{#povCharacter}}is seen through the eyes of {{.}}: {{/povCharacter}}a stern, scarred officer.
```
}%}

Toutes les entrées activées d'une partie partagent leurs variables dans l'ordre d'activation : une entrée peut poser une valeur via `setvar`, que les entrées suivantes dans l'ordre (et le modèle principal) lisent avec `getvar`.

Une erreur de syntaxe dans le contenu est affichée sous le champ. L'entrée est tout de même enregistrée ; tant qu'elle n'est pas corrigée, son contenu est utilisé comme texte brut, sans traitement du modèle.

<a id="tips"></a>

## Conseils

- Gardez les entrées courtes et factuelles : le modèle n'a pas besoin de prose, il a besoin de faits.
- Une entrée par personnage, lieu ou concept, pour que chacune puisse être activée séparément.
- Utilisez des entrées toujours actives pour le cœur du monde, des filtres pour le casting et les lieux, et des mots-clés pour partager un lorebook entre plusieurs livres.
- Vérifiez ce qui a été activé avec **Afficher le prompt** sur une partie générée, ou essayez un texte avant d'écrire avec l'[Aperçu de l'activation](#preview-activation).
- Utilisez la profondeur d'analyse avec parcimonie : une seule passe suffit pour la plupart des livres, et chaque passe peut faire entrer plus de lore que vous ne le voulez.
