---
label: Création des lorebooks
order: 100
---

# Création des lorebooks

<a id="the-lorebook-editor"></a>

## L'éditeur de lorebooks

Ouvrez l'onglet **Lorebooks**. L'éditeur affiche un lorebook à la fois ; choisissez-le dans le champ **Lorebook** en haut.
Les lorebooks appartiennent à votre compte : les autres utilisateurs ne les voient pas, sauf si vous en [partagez](#sharing-a-lorebook) un.

La barre d'outils comporte deux rangées, qui passent sur plusieurs lignes dans une fenêtre étroite : la première pour les lorebooks (de **Lorebook** à **Tag lorebooks**), la seconde pour les entrées du lorebook sélectionné (de **Add Entry** à **Refresh**).

| Bouton | |
|---|---|
| **Lorebook** | Sélectionne le lorebook à modifier. |
| **Only root lorebooks** | N'affiche que les lorebooks qui ne sont pas le sous-lorebook d'un autre (le lorebook sélectionné reste dans la liste). |
| **Add Lorebook** | Crée un nouveau lorebook nommé *New Lorebook* et le sélectionne. |
| Clone (icône de deux feuilles) | Fait une copie du lorebook sélectionné : demande le nom (celui de l'original suivi de *(Copy)*) et sélectionne la copie. La copie a les mêmes entrées (avec leurs réglages, tags et tags négatifs), les mêmes tags, la même description et les mêmes données d'extensions. Ses sous-lorebooks ne sont pas copiés : la copie utilise les mêmes. L'original n'est pas modifié. |
| **Delete Lorebook** | Supprime le lorebook sélectionné (après confirmation). |
| Import (icône de fichier) | **Import Marginalia Lorebook**, **Import from SillyTavern**, **Import from archive** (une archive zip de fichiers `.md`) ou **Import from other user** (un lorebook qu'un autre utilisateur [partage](#sharing-a-lorebook)), voir [Import et export](import-export.md). |
| Export (icône de fichier) | Télécharge le lorebook avec ses sous-lorebooks, voir [Import et export](import-export.md#exporting). |
| Tags (icône d'étiquettes) | **Tag lorebooks** : met une étiquette sur plusieurs lorebooks à la fois, voir [plus bas](#tagging-many-at-once). Non affiché pour un lorebook qui est ouvert dans un livre. |
| Description (icône de lignes, la dernière de la première rangée) | **Edit description** : ouvre une fenêtre pour écrire de quoi parle le lorebook, voir [plus bas](#description). |
| **Add Entry** | Ajoute une entrée au lorebook, voir [Entrées et activation](entries.md). |
| **Full-screen editor** | Ouvre les entrées du lorebook dans une fenêtre plein écran à onglets, voir [Édition en plein écran](#editing-in-full-screen). Uniquement sur ordinateur. |
| **Search and replace** | Remplace un texte dans le nom, le texte et le commentaire des entrées du lorebook, voir [Rechercher et remplacer](entries.md#search-and-replace). |
| **Mass edit** | Ouvre toutes les entrées du lorebook et de ses sous-lorebooks dans un seul tableau, voir [Édition en masse](#mass-edit). |
| Tags (icône d'étiquettes, à droite de **Search and replace**, deuxième rangée) | **Tag entries** : met une étiquette sur plusieurs entrées du lorebook sélectionné à la fois, voir [plus bas](#tagging-many-at-once). |
| **Preview activation** | Montre quelles entrées un tour avec un texte que vous saisissez utiliserait, et pourquoi, voir [Aperçu de l'activation](entries.md#preview-activation). |
| **Refresh** | Recharge les entrées. |
| Champ de recherche | N'affiche que les entrées qui contiennent le texte, voir [Rechercher et supprimer beaucoup d'entrées](entries.md#searching-and-deleting-many-entries). |
| **Delete selected** | Apparaît lorsque des entrées sont sélectionnées dans la liste ; les supprime après confirmation. |

Sous la barre d'outils se trouvent les réglages du lorebook sélectionné :

| Champ | |
|---|---|
| **Name** | Le nom du lorebook, affiché lorsque vous le choisissez pour un livre ou comme sous-lorebook. |
| **Sub Lorebooks** | D'autres lorebooks dont les entrées sont incluses avec celui-ci, voir [plus bas](#sub-lorebooks). Cliquez sur le nom d'un sous-lorebook sélectionné pour l'ouvrir. |
| **Tags** | Tags de tout le lorebook ; ils comptent comme tags de chacune de ses entrées, voir [Tags](entries.md#tags). |
| **Scan depth** | `0` à `5` : nombre de fois où le contenu des entrées utilisées est recherché pour les filtres des autres entrées, voir [Entrées amenées par d'autres entrées](entries.md#entries-brought-in-by-other-entries). Ce réglage vaut pour les entrées du lorebook qu'un livre utilise et pour celles de ses sous-lorebooks, sauf si un sous-lorebook a sa propre profondeur (ligne suivante). |
| **Scan depth as a sub lorebook** | Vide, ou `0` à `5` : la profondeur de recherche des entrées de ce lorebook lorsqu'il est le sous-lorebook de celui qu'utilise un livre. Vide signifie la profondeur de recherche de ce lorebook, `0` signifie que les entrées de ce lorebook ne sont jamais trouvées par le contenu des autres. |
| **Enabled** | Lorsque la case est décochée, le lorebook n'apporte rien : ni ses entrées, ni celles de ses sous-lorebooks. |
| **Share with other users** | Lorsque la case est cochée, les autres utilisateurs peuvent consulter le lorebook et importer une copie, voir [plus bas](#sharing-a-lorebook). Désactivé pour un nouveau lorebook. |

Sous les réglages se trouve la [description](#description) du lorebook, lorsqu'il en a une.

Le reste de l'éditeur est la liste des [entrées](entries.md). Toutes les modifications sont enregistrées au fur et à mesure.

### Description

La description tient en quelques mots (ou quelques lignes) sur le lorebook : quel monde il décrit, à quoi il sert, quel livre l'utilise. Cliquez sur l'icône de lignes dans la première rangée de la barre d'outils, écrivez le texte dans la fenêtre et confirmez ; un texte vide supprime la description. Un lorebook qui a une description l'affiche sur une ligne sous ses réglages, tronquée par des points de suspension lorsqu'elle ne tient pas ; placez le pointeur de la souris sur la ligne pour la lire en entier. La description est copiée avec le lorebook (clone, export, import, sauvegardes) et c'est le texte que les autres utilisateurs voient à côté de son nom lorsque vous le partagez.

![La fenêtre de la description d'un lorebook](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Partager un lorebook

Cochez **Share with other users** et chaque autre utilisateur de cette installation voit le lorebook dans **Import from other user** (le bouton d'import de la première rangée de la barre d'outils), avec son nom, son auteur (votre nom complet, ou votre identifiant de connexion si vous n'en avez pas) et sa description. Ils peuvent ouvrir une ligne pour lire les entrées (et celles des sous-lorebooks) et importer une copie. Décochez la case et le lorebook disparaît de leur liste ; les copies qu'ils ont déjà faites leur appartiennent toujours.

* Le partage ne permet que la lecture. Personne d'autre ne peut modifier, supprimer ni utiliser votre lorebook dans un livre ; votre lorebook reste le vôtre et vous le voyez comme avant.
* Un import crée **de nouveaux lorebooks à l'autre utilisateur** : le lorebook et ses sous-lorebooks **partagés**, à n'importe quelle profondeur, avec leurs entrées, tags et description, liés comme les vôtres le sont. Les copies sont privées (non partagées) : ce que vous faites ensuite avec votre lorebook ne les atteint pas, et ce qu'elles deviennent ne vous atteint pas non plus.
* **Seuls les sous-lorebooks qui sont eux-mêmes partagés sont affichés et copiés.** Un sous-lorebook qui n'est pas partagé est laissé de côté avec tout ce qui se trouve en dessous, y compris un lorebook partagé situé plus bas. Lorsque vous cochez **Share with other users** sur un lorebook qui a des sous-lorebooks non partagés, une question vous demande s'il faut les partager aussi : **Yes** partage le lorebook et tous ses sous-lorebooks (à n'importe quelle profondeur), **No** ne partage que le lorebook, **Cancel** ne partage rien et laisse la case décochée. Un sous-lorebook ajouté plus tard n'est pas partagé tant que vous ne le partagez pas.
* Décocher la case arrête le partage de ce lorebook seulement ; ses sous-lorebooks restent partagés (et apparaissent dans la liste des autres de leur propre chef) tant que vous ne les décochez pas aussi.
* Les lorebooks supprimés, les copies de travail des agents et vos propres lorebooks ne sont pas listés, et rien n'est listé pour un utilisateur qui a été supprimé.

Sous les réglages, l'estimation des tokens des entrées activées du lorebook est affichée. Le lien **Count with subbooks** à côté ajoute les tokens des entrées activées du lorebook et de tous ses sous-lorebooks, à n'importe quelle profondeur, et affiche le total à côté. Comme lorsqu'une histoire est écrite, un sous-lorebook désactivé est laissé de côté avec les lorebooks qui se trouvent sous lui, et un lorebook qui est sous-lorebook à plusieurs endroits n'est compté qu'une fois.
Le total est un comptage du moment où vous cliquez : si vous modifiez une entrée, il disparaît ; cliquez à nouveau pour recompter.

Un lorebook d'un livre sur lequel travaille un agent est affiché en lecture seule (voir [Agents](../agents.md)), sans **Add Entry**, **Search and replace**, **Mass edit**, **Delete Lorebook** ni les réglages ; **Add Lorebook**, l'import, l'export et **Refresh** restent disponibles, pour que vous puissiez toujours commencer un autre lorebook.
<a id="mass-edit"></a>

## Édition en masse

**Mass edit** ouvre une fenêtre qui affiche toutes les entrées du lorebook sélectionné **et de ses sous-lorebooks** (quelle que soit leur profondeur) dans un seul tableau, classées selon le **Order** des entrées (les entrées de même ordre sont classées par lorebook, puis par nom). Cette fonction sert à mettre de l'ordre dans un gros lorebook :

- Modifier la case *enabled*, le **Entry Name**, le **Order**, les **Tags**, le **Filtering Mode**, le **Filtering**, le **Insertion Mode**, les **Negative Tags**, le **Activation Context** avec ses **Parts**, la plage de parties (**From message** et **To message** ; vide : pas de limite) et la **Note** d'une entrée directement dans le tableau, comme dans l'éditeur. Chaque entrée occupe une ligne. Le tableau défile vers le bas (et sur le côté lorsque la fenêtre est étroite). La ligne se déplace à sa nouvelle place lorsque vous changez l'ordre. Le texte d'une entrée est raccourci dans le tableau ; ouvrez l'entrée dans le lorebook pour lire ou modifier l'intégralité du texte.
- La zone **Lorebook** d'une ligne indique le lorebook auquel appartient l'entrée. Choisissez un autre lorebook (le lorebook lui-même ou l'un de ses sous-lorebooks) pour y déplacer l'entrée.
- Pour déplacer plusieurs entrées, cochez-les (la case de l'en-tête coche toutes les lignes), choisissez **Move to lorebook** en bas de la fenêtre, puis appuyez sur **Move checked**.
- Pour supprimer plusieurs entrées, cochez-les et appuyez sur **Delete checked** ; une confirmation vous est demandée une seule fois, puis les entrées sont déplacées vers la [corbeille](../trash.md).
- Un lorebook sur lequel travaille un agent est verrouillé : ses entrées sont grisées et ne peuvent être ni modifiées ni déplacées, et rien ne peut y être déplacé. Ce contrôle se fait pour chaque lorebook, de sorte qu'un lorebook peut être libre alors qu'un de ses sous-lorebooks est verrouillé (parce qu'il est utilisé par un autre livre). **Refresh** relit la fenêtre pour vérifier si un verrou a été levé.

Le lorebook dans l'éditeur est actualisé lorsque la fenêtre est fermée après une modification.

![Édition en masse d'un lorebook et de ses sous-lorebooks](../../images/lorebook-mass-edit.png)

---
<a id="editing-in-full-screen"></a>

## Édition en plein écran

Le **Mode plein écran** (deuxième ligne de la barre d'outils) ouvre le lorebook sélectionné dans une fenêtre qui occupe tout l'écran, pour travailler sur de longues entrées. Le bouton d'agrandissement de la ligne d'une entrée ouvre la fenêtre sur cette entrée. L'onglet *Lorebook* de la fenêtre d'un livre a le même bouton, pour le lorebook affiché dans cet onglet. La fenêtre porte le nom du lorebook. Elle fonctionne par onglets, comme ceux de l'[éditeur de l'histoire](../books/story-editor.md) : faites glisser un onglet pour le placer à côté d'un autre ou en dessous, et faites glisser les séparateurs entre eux.

![L'éditeur de lorebook en plein écran : la liste des entrées et deux entrées ouvertes dans des onglets](../../images/lorebook-fullscreen.png)

- L'onglet **Entries** liste les entrées, avec la zone de recherche et les filtres par étiquette et par statut de la page du lorebook, ainsi que **Add Entry**, **Mass edit**, **Tag entries**, **Preview activation** et **Refresh** (la modification en masse et les étiquettes montrent aussi leurs changements dans les onglets ouverts). Avec **Include sub lorebooks**, il liste aussi les entrées des [sous-lorebooks](#sub-lorebooks), chacune accompagnée du nom de son lorebook ; **Add Entry** ajoute alors l'entrée au lorebook choisi à côté, et l'onglet d'une telle entrée indique son lorebook. Cet onglet ne peut pas être fermé. Chaque ligne a trois boutons :
  - **Open** (icône de stylo, ou double-clic sur la ligne) affiche l'entrée dans l'onglet d'entrée que vous avez utilisé en dernier ou, s'il n'y en a pas, ouvre un onglet dans une colonne à droite de la liste ;
  - **Open in new tab** (icône de flèche) ajoute un onglet pour l'entrée, à côté de l'onglet d'entrée que vous avez utilisé en dernier ;
  - **Delete** (icône de corbeille) supprime l'entrée après confirmation, et ferme son onglet.

  Une entrée n'est ouverte dans un seul onglet au maximum : l'ouvrir à nouveau affiche l'onglet qu'elle possède déjà.
- Un **onglet d'entrée** comporte, de haut en bas : le **Name**, l'**Order** et **Enabled** ; **Settings**, un panneau repliable, avec les autres réglages de l'entrée (mode et filtre de filtrage, mode d'insertion, contexte et parties d'activation, étiquettes et étiquettes négatives, plage de parties, cases de récursion et condition, voir [Entrées et activation](entries.md)) ; puis le **content** dans un éditeur Markdown (chaque onglet garde son mode d'affichage : le texte seul, le texte et l'aperçu, ou l'aperçu seul, y compris lorsque la fenêtre est rouverte), et la **note** à côté lorsque **Show note** est coché. L'œil ouvre l'[aperçu d'activation](entries.md#preview-activation) pour cette entrée. La ligne du bas affiche le nombre estimé de jetons du contenu et, en rouge, ce qui ne va pas dans son modèle (il est tout de même enregistré). Fermez un onglet avec son **×**.
- **Images dans les entrées.** Collez une image dans le contenu, déposez-en une dessus ou choisissez-en une avec le bouton image de la barre d'outils (PNG, JPEG, GIF ou WebP, jusqu'à 10 Mo). Elle est stockée avec vos fichiers (l'onglet *Resources*) et le contenu reçoit une ligne comme `![Map](resource:…)` à l'emplacement du curseur ; l'aperçu affiche l'image, et le texte entre crochets est sa légende. Supprimez la ligne pour retirer l'image de l'entrée. Le modèle lit l'entrée sans ces lignes. Les images elles-mêmes ne sont envoyées que lorsque les réglages du modèle indiquent *Multimodal (accepts images)* (voir [Fournisseurs d'inférence](../inference-providers.md)). Les images voyagent avec le lorebook : un clone, une copie d'un lorebook partagé, un fichier d'export de lorebook et une sauvegarde de livre avec ses fichiers les emportent, et la copie reçoit des images qui lui sont propres. Les exports SillyTavern et les exports de livres les omettent. L'onglet *Lorebook* de l'histoire les affiche sous le texte de l'entrée. Les images ne sont pas encore prises en compte dans les estimations de jetons.
- **Passer d'une entrée à l'autre.** Ctrl+clic (Cmd+clic sur Mac) sur le nom ou un mot-clé d'une autre entrée, dans le contenu, dans le texte ou dans l'aperçu, ouvre cette entrée dans l'onglet que vous avez utilisé en dernier ; avec Maj également, elle s'ouvre dans un nouvel onglet. La ligne du bas liste aussi, après **Mentioned in:**, les entrées dont le contenu cite celle-ci ; cliquez sur l'une d'elles pour l'ouvrir, Maj+clic pour un nouvel onglet. Une entrée est désignée par son nom et par les mots de son filtre (chaque mot-clé, tout le texte d'un filtre *Text*, ce que correspond une expression régulière), en mots entiers, sans tenir compte de la casse. Cela est proche de ce que suit la [récursion](entries.md), sans être identique : la récursion trouve aussi un mot-clé à l'intérieur d'un mot plus long, exige tous les mots-clés d'un filtre *AND*, et vérifie les étiquettes et la condition.
- L'éditeur Markdown dispose d'une barre d'outils (gras, italique, barré, une ligne, titres, citation, code, bloc de code, tableau et les trois types de listes) et, à droite, des vues *edit*, *edit with preview* (par défaut) et *preview*. Les raccourcis habituels fonctionnent (Ctrl+B, Ctrl+I...) ; Tab passe au champ suivant au lieu d'insérer une tabulation. Ce que vous tapez est enregistré lorsque vous cessez de taper un instant, et lorsque vous quittez l'éditeur.
- L'en-tête affiche le nombre estimé de jetons des entrées activées, **Count with subbooks** et **Search and replace**, qui applique le remplacement aussi dans les onglets ouverts.

Toutes les modifications sont enregistrées au fur et à mesure, donc **Close** ne fait rien perdre. La fenêtre garde en mémoire la façon dont vous l'avez laissée, pour chaque lorebook : la prochaine fois qu'elle s'ouvre avec les mêmes entrées dans les mêmes onglets, disposés de la même manière (les entrées supprimées depuis en sont exclues). La page du lorebook affiche de nouveau les entrées lorsque la fenêtre est fermée.

**Modifications faites ailleurs.** Une modification dans un onglet est enregistrée dans l'entrée telle qu'elle est stockée à cet instant. Ce qui a été changé ailleurs (la page du lorebook, la modification en masse, une autre fenêtre) dans les *autres* champs de l'entrée est donc conservé, et l'onglet l'affiche. Si le **content** ou la **note** a été modifié ailleurs depuis que l'onglet l'affiche, votre texte n'est pas enregistré par-dessus : la ligne du bas l'indique, avec **Keep my text** (enregistrer le vôtre malgré tout) et **Load the saved text** (abandonner le vôtre). Une entrée supprimée ailleurs ferme son onglet.
La fenêtre suit aussi ce qui est enregistré pendant qu'elle est ouverte : lorsqu'un agent ou une autre fenêtre (un autre onglet du navigateur, l'onglet *Lorebook* de l'histoire) enregistre ou supprime une entrée, la liste est rechargée, et l'onglet de cette entrée affiche son état actuel (l'onglet d'une entrée supprimée se ferme). Un onglet dont le texte attend **Keep my text** ou **Load the saved text** reste tel quel jusqu'à votre réponse. Une modification enregistrée dans la même seconde où vous tapez peut encore se croiser : la ligne du bas pose alors la question comme ci-dessus.

**Agents.** La fenêtre n'est pas proposée pendant qu'un agent travaille sur un livre qui utilise le lorebook. Si une exécution démarre alors que la fenêtre est ouverte, la fenêtre passe en lecture seule à votre prochaine modification ou lorsque vous changez d'onglet : une barre indique le nom de l'agent (et précise si votre dernière modification n'a pas pu être enregistrée). Mettez l'exécution en pause ou arrêtez-la, puis cliquez sur **Check again** : les entrées sont chargées telles que l'exécution les a laissées, et vous pouvez de nouveau modifier.

Les extensions [Lore Generator](../extensions/lore-generator.md) et [Lorebook VCS](../extensions/lorebook-vcs.md) fonctionnent aussi ici : la baguette se trouve dans les lignes de la liste et dans la ligne du nom d'un onglet d'entrée, le panneau des révisions est au-dessus de la liste, et la barre de révision d'une entrée se trouve dans son onglet.

<a id="tagging-many-at-once"></a>

## Étiqueter plusieurs éléments à la fois

Les deux boutons d'étiquettes de la barre d'outils ouvrent une seule fenêtre qui ajoute une étiquette à plusieurs éléments, ou la retire de ceux-ci : **Tag lorebooks** liste vos lorebooks, **Tag entries** liste les entrées du lorebook sélectionné. Choisissez l'**Tag** (ou tapez une nouvelle étiquette puis appuyez sur Enter) ; la liste affiche chaque élément avec ses étiquettes, et les éléments qui portent l'étiquette choisie apparaissent en premier, avec l'étiquette mise en évidence. Les boutons **+** et **-** d'une ligne ajoutent ou retirent l'étiquette pour cet élément ; cochez plusieurs éléments (la case de l'en-tête coche tous les éléments) et utilisez **Apply the tag to the checked** ou **Remove the tag from the checked** en bas pour agir sur tous. Un élément sur lequel travaille un agent (un lorebook d'un livre avec une exécution en cours, et donc toutes ses entrées) est grisé et ne peut pas être modifié tant que l'exécution n'est pas mise en pause ou arrêtée. La fenêtre étiquette aussi les [livres](../books/managing-books.md#tagging-many-books-at-once).
Les étiquettes qu'elle ajoute aux entrées sont les étiquettes ordinaires, voir [Étiquettes](entries.md#tags) ; les étiquettes négatives se définissent sur l'entrée elle-même.

![Étiquetage des entrées d'un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Utiliser un lorebook dans un livre

Ouvrez le livre et son onglet **Lorebook**, puis choisissez le lorebook dans le champ **Lorebook**. L'éditeur situé plus bas affiche le lorebook, de sorte que vous pouvez ajouter et modifier des entrées pendant que vous écrivez. Videz le champ pour écrire sans lorebook.

Un livre utilise un seul lorebook. Pour en utiliser plusieurs, créez un lorebook qui les inclut comme sous-lorebooks, et utilisez celui-là.

Un même lorebook peut être utilisé par autant de livres que vous le souhaitez. Les modifications qui lui sont apportées affectent tous ces livres à partir de la prochaine partie générée ; les parties déjà écrites conservent le lore avec lequel elles ont été écrites.
<a id="sub-lorebooks"></a>

## Sous-lorebooks

**Sous-lorebooks** inclut les entrées d'autres lorebooks dans celui-ci. Une configuration typique :

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- Un sous-lorebook peut avoir ses propres sous-lorebooks ; tous les niveaux sont inclus.
- Un lorebook peut être le sous-lorebook de plusieurs lorebooks. Lorsqu'il est atteint plusieurs fois, ses entrées ne sont utilisées qu'une seule fois.
- Un lorebook ne peut pas s'inclure lui-même, directement ou par l'intermédiaire d'autres lorebooks : un tel choix est refusé avec le message *Cannot select sub lorebook: cycle detected.*
- Un sous-lorebook désactivé est exclu, ainsi que tout ce qu'il inclut.

![Un lorebook avec deux sous-lorebooks : les jetons avec et sans eux](../../images/lorebook-sub-lorebooks.png)

Les entrées de tous les lorebooks inclus sont regroupées et triées selon leur [ordre](entries.md#order-and-insertion), quel que soit le lorebook d'origine.

<a id="disabling-and-deleting"></a>

## Désactivation et suppression

Décochez **Enabled** pour désactiver temporairement un lorebook, par exemple pour voir comment un livre se lit sans lui. Les livres continuent de l'utiliser, et il refonctionne lorsque vous le cochez à nouveau.

**Delete Lorebook** retire le lorebook de vos listes. Les livres qui l'utilisaient ne reçoivent plus ses entrées, et les lorebooks qui l'incluaient comme sous-lorebook l'ignorent. Les données restent dans la base de données jusqu'à ce qu'un administrateur exécute le [Nettoyage](../administration/cleanup.md). D'ici là, vous pouvez restaurer le lorebook depuis la [Corbeille](../trash.md). Les copies présentes dans les [sauvegardes des livres](../books/backups.md) et dans les fichiers exportés ne sont pas touchées.
