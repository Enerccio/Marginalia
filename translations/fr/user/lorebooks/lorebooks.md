---
label: Création des lorebooks
order: 100
---

# Création des lorebooks

<a id="the-lorebook-editor"></a>

## L'éditeur de lorebooks

Ouvrez l'onglet **Lorebooks**. L'éditeur affiche un seul lorebook à la fois ; choisissez-le dans le champ **Lorebook** en haut.
Les lorebooks appartiennent à votre compte : les autres utilisateurs ne les voient pas, sauf si vous en [partagez](#sharing-a-lorebook) un.

La barre d'outils comporte deux rangées, qui passent sur plusieurs lignes dans une fenêtre étroite : la première pour les lorebooks (de **Lorebook** à **Tag lorebooks**), la seconde pour les entrées du lorebook sélectionné (de **Add Entry** à **Refresh**).

| Bouton | |
|---|---|
| **Lorebook** | Sélectionne le lorebook à modifier. |
| **Only root lorebooks** | N'affiche que les lorebooks qui ne sont le sous-lorebook d'aucun autre (le lorebook sélectionné reste dans la liste). |
| **Add Lorebook** | Crée un nouveau lorebook nommé *New Lorebook* et le sélectionne. |
| Clone (icône de deux feuilles) | Fait une copie du lorebook sélectionné : demande le nom (celui de l'original suivi de *(Copy)*) et sélectionne la copie. La copie a les mêmes entrées (avec leurs réglages, mots-clés et mots-clés négatifs), les mêmes mots-clés, la même description et les données des extensions. Ses sous-lorebooks ne sont pas copiés : la copie utilise les mêmes. L'original n'est pas modifié. |
| **Delete Lorebook** | Supprime le lorebook sélectionné (après confirmation). |
| Import (icône de fichier) | **Import Marginalia Lorebook**, **Import from SillyTavern**, **Import from archive** (un zip de fichiers `.md`) ou **Import from other user** (un lorebook qu'un autre utilisateur [partage](#sharing-a-lorebook)), voir [Import & export](import-export.md). |
| Export (icône de fichier) | Télécharge le lorebook avec ses sous-lorebooks, voir [Import & export](import-export.md#exporting). |
| Tags (icône d'étiquettes) | **Tag lorebooks** : met une étiquette sur plusieurs lorebooks à la fois, voir [plus bas](#tagging-many-at-once). Non affiché pour un lorebook ouvert dans un livre. |
| Description (icône de lignes, dernière de la première rangée) | **Edit description** : ouvre une fenêtre pour écrire de quoi parle le lorebook, voir [plus bas](#description). |
| **Add Entry** | Ajoute une entrée au lorebook, voir [Entrées et activation](entries.md). |
| **Full-screen editor** | Ouvre les entrées du lorebook dans une fenêtre plein écran à onglets, voir [Édition en plein écran](#editing-in-full-screen). Ordinateur uniquement. |
| **Search and replace** | Remplace un texte dans le nom, le texte et le commentaire des entrées du lorebook, voir [Rechercher et remplacer](entries.md#search-and-replace). |
| **Mass edit** | Ouvre toutes les entrées du lorebook et de ses sous-lorebooks dans un seul tableau, voir [Modification en masse](#mass-edit). |
| Tags (icône d'étiquettes, à droite de **Search and replace**, deuxième rangée) | **Tag entries** : met une étiquette sur plusieurs entrées du lorebook sélectionné à la fois, voir [plus bas](#tagging-many-at-once). |
| **Preview activation** | Montre quelles entrées un tour utiliserait avec un texte que vous saisissez, et pourquoi, voir [Aperçu de l'activation](entries.md#preview-activation). |
| **Refresh** | Recharge les entrées. |
| Champ de recherche | N'affiche que les entrées qui contiennent le texte, voir [Rechercher et supprimer plusieurs entrées](entries.md#searching-and-deleting-many-entries). |
| **Delete selected** | Apparaît quand des entrées sont sélectionnées dans la liste ; les supprime après confirmation. |

Sous la barre d'outils se trouvent les réglages du lorebook sélectionné :

| Champ | |
|---|---|
| **Name** | Le nom du lorebook, affiché quand vous le choisissez pour un livre ou comme sous-lorebook. |
| **Sub Lorebooks** | D'autres lorebooks dont les entrées sont incluses avec celui-ci, voir [plus bas](#sub-lorebooks). Cliquez sur le nom d'un sous-lorebook sélectionné pour l'ouvrir. |
| **Tags** | Mots-clés de tout le lorebook ; ils comptent comme mots-clés de chacune de ses entrées, voir [Mots-clés](entries.md#tags). |
| **Scan depth** | `0` à `5` : nombre de fois où le contenu des entrées utilisées est cherché pour les filtres des autres entrées, voir [Entrées amenées par d'autres entrées](entries.md#entries-brought-in-by-other-entries). Il compte pour les entrées du lorebook qu'un livre utilise et pour celles de ses sous-lorebooks, sauf si un sous-lorebook a sa propre profondeur (ligne suivante). |
| **Scan depth as a sub lorebook** | Vide, ou `0` à `5` : la profondeur d'analyse des entrées de ce lorebook lorsqu'il est un sous-lorebook de celui qu'utilise un livre. Vide signifie la profondeur d'analyse de ce lorebook-là, `0` que les entrées de ce lorebook ne sont jamais trouvées par le contenu des autres. |
| **Enabled** | Quand la case est décochée, le lorebook n'apporte rien : ni ses entrées, ni celles de ses sous-lorebooks. |
| **Share with other users** | Quand la case est cochée, les autres utilisateurs peuvent consulter le lorebook et en importer une copie, voir [plus bas](#sharing-a-lorebook). Désactivé pour un nouveau lorebook. |

Sous les réglages se trouve la [description](#description) du lorebook, lorsqu'il en a une.

Le reste de l'éditeur est la liste des [entrées](entries.md). Toutes les modifications sont enregistrées au fur et à mesure.

### Description

La description est quelques mots (ou quelques lignes) sur le lorebook : quel monde il décrit, à quoi il sert, quel livre l'utilise. Cliquez sur l'icône de lignes dans la première rangée de la barre d'outils, écrivez dans la fenêtre et confirmez ; un texte vide la supprime. Un lorebook qui a une description l'affiche sur une ligne sous ses réglages, coupée par des points de suspension là où elle ne tient pas ; posez le pointeur de la souris sur la ligne pour lire le tout. La description est copiée avec le lorebook (clone, export, import, sauvegardes) et c'est le texte que les autres utilisateurs voient à côté de son nom lorsque vous le partagez.

![La fenêtre de description d'un lorebook](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Partager un lorebook

Cochez **Share with other users** et chaque autre utilisateur de cette installation voit le lorebook dans **Import from other user** (le bouton d'importation dans la première rangée de la barre d'outils), avec son nom, son auteur (votre nom complet, ou votre identifiant de connexion si vous n'en avez pas) et sa description. Ils peuvent ouvrir une ligne pour lire les entrées (et les entrées des sous-lorebooks) et en importer une copie. Décochez la case et le lorebook disparaît de leur liste ; les copies qu'ils ont déjà faites leur restent.

* Le partage est en lecture seule. Personne d'autre ne peut modifier, supprimer ou utiliser votre lorebook dans un livre ; votre lorebook reste le vôtre et vous le voyez comme avant.
* Une importation crée de **nouveaux lorebooks à l'autre utilisateur** : le lorebook et ses sous-lorebooks **partagés**, à n'importe quelle profondeur, avec leurs entrées, mots-clés et description, reliés comme les vôtres le sont. Les copies sont privées (non partagées) ; ce que vous faites ensuite avec votre lorebook ne les atteint pas, et ce qu'elles deviennent ne vous atteint pas non plus.
* **Seuls les sous-lorebooks qui sont eux-mêmes partagés sont affichés et copiés.** Un sous-lorebook non partagé est laissé de côté avec tout ce qui se trouve en dessous, y compris un lorebook partagé situé plus bas. Quand vous cochez **Share with other users** sur un lorebook qui a des sous-lorebooks non partagés, une question demande s'il faut les partager aussi : **Yes** partage le lorebook et tous ses sous-lorebooks (à n'importe quelle profondeur), **No** ne partage que le lorebook, **Cancel** ne partage rien et laisse la case décochée. Un sous-lorebook ajouté plus tard n'est pas partagé tant que vous ne le partagez pas.
* Décocher la case arrête le partage de ce lorebook seulement ; ses sous-lorebooks restent partagés (et apparaissent dans la liste des autres de leur côté) tant que vous ne les décochez pas aussi.
* Les lorebooks supprimés, les copies de travail des agents et vos propres lorebooks ne sont pas listés, et rien n'est listé pour un utilisateur qui a été supprimé.

Sous les réglages, l'estimation des jetons des entrées activées du lorebook est affichée. Le lien **Count with subbooks** à côté ajoute les jetons des entrées activées du lorebook et de tous ses sous-lorebooks, à n'importe quelle profondeur, et affiche le total à côté. Comme lorsqu'une histoire est écrite, un sous-lorebook désactivé est laissé de côté avec les lorebooks situés en dessous, et un lorebook qui est sous-lorebook à plusieurs endroits n'est compté qu'une fois.
Le total est un décompte au moment où vous cliquez : si vous modifiez une entrée, il disparaît ; cliquez à nouveau pour recompter.

Un lorebook d'un livre sur lequel travaille un agent est affiché en lecture seule (voir [Agents](../agents.md)), sans **Add Entry**, **Search and replace**, **Mass edit**, **Delete Lorebook** ni les réglages ; **Add Lorebook**, l'importation, l'exportation et **Refresh** restent disponibles, pour que vous puissiez toujours commencer un autre lorebook.
<a id="mass-edit"></a>

## Édition en masse

**Édition en masse** ouvre une fenêtre avec toutes les entrées du lorebook sélectionné **et de ses sous-lorebooks** (quelle que soit leur profondeur) dans un seul tableau, classées selon l'**Ordre** des entrées (les entrées de même ordre sont classées par lorebook, puis par nom). Elle sert à mettre de l'ordre dans un gros lorebook :

- Modifiez directement la case *activée*, le **Nom de l'entrée**, l'**Ordre**, les **Mots-clés**, le **Mode de filtrage**, le **Filtrage**, le **Mode d'insertion**, les **Mots-clés négatifs**, le **Contexte d'activation** avec ses **Parties**, la plage de parties (**Depuis le message** et **Jusqu'au message**, vide : pas de limite) et la **Note** d'une entrée, comme dans l'éditeur. Chaque entrée occupe une ligne ; le tableau défile vers le bas (et sur le côté lorsque la fenêtre est étroite). La ligne se déplace à sa nouvelle place lorsque vous modifiez l'ordre. Le texte d'une entrée est raccourci dans le tableau ; ouvrez l'entrée dans le lorebook pour le lire ou le modifier en entier.
- La case **Lorebook** d'une ligne indique dans quel lorebook se trouve l'entrée. Choisissez-en un autre (le lorebook lui-même ou l'un de ses sous-lorebooks) pour y déplacer l'entrée.
- Pour déplacer plusieurs entrées, cochez-les (la case de l'en-tête coche toutes les entrées), choisissez **Déplacer vers le lorebook** en bas, puis appuyez sur **Déplacer les éléments cochés**.
- Pour supprimer plusieurs entrées, cochez-les et appuyez sur **Supprimer les éléments cochés** ; une confirmation est demandée une seule fois, puis elles sont déplacées vers la [corbeille](../trash.md).
- Un lorebook sur lequel travaille un agent est verrouillé : ses entrées sont grisées et ne peuvent être ni modifiées ni déplacées, et rien ne peut y être déplacé. Ce contrôle est fait pour chaque lorebook ; un lorebook peut donc être libre alors que l'un de ses sous-lorebooks est verrouillé (il est utilisé par un autre livre). **Actualiser** relit la fenêtre pour vérifier si un verrou a disparu.

Le lorebook affiché dans l'éditeur est actualisé lorsque la fenêtre est fermée après une modification.

![Édition en masse d'un lorebook et de ses sous-lorebooks](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Édition en plein écran

**Éditeur plein écran** (deuxième ligne de la barre d'outils) ouvre le lorebook sélectionné dans une fenêtre qui occupe tout l'écran, pour travailler sur de longues entrées (le bouton d'agrandissement de la ligne d'une entrée ouvre la fenêtre sur cette entrée ; l'onglet *Lorebook* de la fenêtre d'un livre possède le même bouton, pour le lorebook affiché là). La fenêtre porte le nom du lorebook. Elle se compose d'onglets, comme ceux de l'[éditeur de l'histoire](../books/story-editor.md) : faites glisser un onglet pour le placer à côté ou sous un autre, et faites glisser les séparateurs entre eux.

![L'éditeur de lorebook en plein écran : la liste des entrées et deux entrées ouvertes dans des onglets](../../images/lorebook-fullscreen.png)

- L'onglet **Entrées** liste les entrées, avec la zone de recherche et les filtres de mots-clés et d'état de la page du lorebook, **Ajouter une entrée**, **Modification en masse**, **Étiqueter des entrées**, **Aperçu de l'activation** et **Actualiser** (la modification en masse et les mots-clés montrent aussi leurs changements dans les onglets ouverts). Avec **Inclure les sous-lorebooks**, il liste aussi les entrées des [sous-lorebooks](#sub-lorebooks), chacune avec le nom de son lorebook ; **Ajouter une entrée** ajoute alors l'entrée au lorebook choisi à côté, et l'onglet d'une telle entrée indique son lorebook. Il ne peut pas être fermé. Chaque ligne possède trois boutons :
  - **Ouvrir** (icône de stylo, ou double-clic sur la ligne) affiche l'entrée dans l'onglet d'entrée que vous avez utilisé en dernier ou, s'il n'y en a pas, en ouvre un dans une colonne à droite de la liste ;
  - **Ouvrir dans un nouvel onglet** (icône de flèche) ajoute un onglet pour l'entrée, à côté de l'onglet d'entrée que vous avez utilisé en dernier ;
  - **Supprimer** (icône de corbeille) supprime l'entrée après confirmation, et ferme son onglet.

  Une entrée n'est ouverte que dans un seul onglet au plus : l'ouvrir à nouveau affiche l'onglet qu'elle possède déjà.
- Un **onglet d'entrée** comporte, de haut en bas : le **Nom**, l'**Ordre** et **Activé** ; **Paramètres**, un panneau que vous pouvez replier, avec le reste des réglages de l'entrée (mode et filtre de filtrage, mode d'insertion, contexte et parties d'activation, mots-clés et mots-clés négatifs, plage des parties, les cases de récursion et leur condition, voir [Entrées et activation](entries.md)) ; puis le **contenu** dans un éditeur Markdown (chaque onglet retient s'il affiche le texte, le texte et l'aperçu, ou l'aperçu, y compris lorsque la fenêtre est rouverte), et la **note** à côté lorsque **Afficher la note** est cochée. L'œil ouvre l'[aperçu de l'activation](entries.md#preview-activation) pour cette entrée. La ligne du bas affiche les jetons estimés du contenu et, en rouge, ce qui ne va pas dans son modèle (il est tout de même enregistré). Fermez un onglet avec son **×**.
- **Images dans les entrées.** Collez une image dans le contenu, déposez-en une dessus, ou choisissez-en une avec le bouton image de la barre d'outils (PNG, JPEG, GIF ou WebP, jusqu'à 10 Mo). Elle est stockée avec vos fichiers (l'onglet *Ressources*) et le contenu reçoit une ligne du type `![Carte](resource:…)` à l'emplacement du curseur ; l'aperçu affiche l'image, et le texte entre crochets est sa légende. Supprimez la ligne pour retirer l'image de l'entrée. Le modèle lit l'entrée sans ces lignes. Les images elles-mêmes ne sont envoyées que si les réglages du modèle indiquent *Envoyer les images du lore* (voir [Fournisseurs d'inférence](../inference-providers.md)). Les images voyagent avec le lorebook : un clone, une copie d'un lorebook partagé, un fichier de lorebook exporté et une sauvegarde de livre avec ses fichiers les emportent, et la copie reçoit des images qui lui sont propres. Les exports SillyTavern et les exports de livres les laissent de côté. L'onglet *Lorebook* de l'histoire les affiche sous le texte de l'entrée. Les images ne sont pas encore comptées dans les estimations de jetons.
- **Passer d'une entrée à l'autre.** Ctrl+clic (Cmd+clic sur Mac) sur le nom ou un mot-clé d'une autre entrée dans le contenu, dans le texte ou dans l'aperçu, pour ouvrir cette entrée dans l'onglet que vous avez utilisé en dernier ; avec Maj également, elle s'ouvre dans un nouvel onglet. La ligne du bas liste aussi, après **Mentionné dans :**, les entrées dont le contenu cite celle-ci ; cliquez sur l'une d'elles pour l'ouvrir, Maj+clic pour un nouvel onglet. Une entrée est désignée par son nom et par les mots de son filtre (chaque mot-clé, le texte entier d'un filtre *Texte*, ce que correspond une expression régulière), en mots entiers, sans distinction de casse. Cela ressemble à ce que la [récursion](entries.md) suit, sans être identique : la récursion trouve aussi un mot-clé à l'intérieur d'un mot plus long, exige tous les mots-clés d'un filtre *ET*, et vérifie les mots-clés et la condition.
- L'éditeur Markdown possède une barre d'outils (gras, italique, barré, une ligne, titres, citation, code, bloc de code, tableau et les trois types de listes) et, à droite, les vues *édition*, *édition avec aperçu* (par défaut) et *aperçu*. Les raccourcis habituels fonctionnent (Ctrl+B, Ctrl+I...) ; Tab passe au champ suivant au lieu d'indenter. Ce que vous tapez est enregistré lorsque vous cessez de taper un instant et lorsque vous quittez l'éditeur.
- L'en-tête affiche les jetons estimés des entrées activées, **Compter avec les sous-livres** et **Rechercher et remplacer**, qui affiche aussi le texte remplacé dans les onglets ouverts.

Toutes les modifications sont enregistrées au fur et à mesure, donc **Fermer** ne perd rien. La fenêtre garde en mémoire la manière dont vous l'avez laissée, pour chaque lorebook : la prochaine fois qu'elle s'ouvre avec les mêmes entrées dans les mêmes onglets, disposés de la même façon (les entrées supprimées entre-temps sont omises). La page du lorebook affiche de nouveau les entrées lorsque la fenêtre est fermée.

**Modifications faites ailleurs.** Une modification dans un onglet est enregistrée dans l'entrée telle qu'elle est stockée à ce moment-là ; ce qui a été modifié ailleurs (la page du lorebook, la modification en masse, une autre fenêtre) dans les *autres* champs de l'entrée est conservé, et l'onglet l'affiche. Si le **contenu** ou la **note** a été modifié ailleurs depuis que l'onglet l'a affiché, votre texte n'est pas enregistré par-dessus : la ligne du bas le signale, avec **Garder mon texte** (enregistrer le vôtre quand même) et **Charger le texte enregistré** (abandonner le vôtre). Une entrée supprimée ailleurs ferme son onglet.
La fenêtre suit aussi ce qui est enregistré pendant qu'elle est ouverte : lorsqu'un agent ou une autre fenêtre (un autre onglet du navigateur, l'onglet lorebook de l'histoire) enregistre ou supprime une entrée, la liste est rechargée et l'onglet de cette entrée l'affiche tel qu'il est maintenant (l'onglet d'une entrée supprimée se ferme). Un onglet dont le texte attend **Garder mon texte** ou **Charger le texte enregistré** reste en l'état jusqu'à votre réponse. Une modification enregistrée dans la même seconde où vous tapez peut encore se croiser : la ligne du bas pose alors la question comme ci-dessus.

**Agents.** La fenêtre n'est pas proposée pendant qu'un agent travaille sur un livre qui utilise le lorebook. Si une exécution démarre pendant que la fenêtre est ouverte, la fenêtre passe en lecture seule à votre prochaine modification ou lorsque vous changez d'onglet : une barre nomme l'agent (et indique si votre dernière modification n'a pas pu être enregistrée). Mettez l'exécution en pause ou arrêtez-la, puis cliquez sur **Vérifier à nouveau** : les entrées sont chargées telles que l'exécution les a laissées, et vous pouvez de nouveau modifier.

Les extensions [Générateur de lore](../extensions/lore-generator.md) et [VCS de lorebook](../extensions/lorebook-vcs.md) fonctionnent aussi ici : la baguette se trouve dans les lignes de la liste et dans la ligne du nom d'un onglet d'entrée, le panneau des révisions est au-dessus de la liste, et la barre de révision d'une entrée se trouve dans son onglet.

<a id="tagging-many-at-once"></a>

## Étiqueter plusieurs éléments à la fois

Les deux boutons de mots-clés de la barre d'outils ouvrent une seule fenêtre qui ajoute un mot-clé à plusieurs éléments, ou le retire : **Étiqueter des lorebooks** liste vos lorebooks, **Étiqueter des entrées** les entrées du lorebook sélectionné. Choisissez le **Mot-clé** (ou tapez-en un nouveau puis appuyez sur Entrée) ; la liste affiche chaque élément avec ses mots-clés, et les éléments qui portent le mot-clé choisi viennent en premier, avec le mot-clé mis en évidence. Les boutons **+** et **-** d'une ligne ajoutent ou retirent le mot-clé pour cet élément ; cochez plusieurs éléments (la case de l'en-tête coche tout) et utilisez **Appliquer le mot-clé aux éléments cochés** ou **Retirer le mot-clé des éléments cochés** en bas pour le faire pour tous. Un élément sur lequel travaille un agent (un lorebook d'un livre dont une exécution est en cours, et donc toutes ses entrées) est grisé et ne peut pas être modifié tant que l'exécution n'est pas mise en pause ou arrêtée. La fenêtre étiquette aussi les [livres](../books/managing-books.md#tagging-many-books-at-once).
Les mots-clés qu'elle pose sur les entrées sont les mots-clés ordinaires, voir [Mots-clés](entries.md#tags) ; les mots-clés négatifs se définissent sur l'entrée elle-même.

![Étiquetage des entrées d'un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Utiliser un lorebook dans un livre

Ouvrez le livre et son onglet **Lorebook**, puis choisissez le lorebook dans le champ **Lorebook**. L'éditeur situé en dessous affiche le lorebook, afin que vous puissiez ajouter et modifier des entrées pendant que vous écrivez. Videz le champ pour écrire sans lorebook.

Un livre utilise un seul lorebook. Pour en utiliser plusieurs, créez un lorebook qui les inclut comme sous-lorebooks, et utilisez celui-ci.

Un même lorebook peut être utilisé par autant de livres que l'on veut. Les modifications qui lui sont apportées affectent tous ces livres à partir de la prochaine partie générée ; les parties déjà écrites conservent le lore avec lequel elles ont été écrites.
<a id="sub-lorebooks"></a>

## Sous-lorebooks

**Sous-lorebooks** inclut les entrées d'autres lorebooks dans celui-ci. Une configuration typique :

```
"Saga" (utilisé par les livres de la série)
 ├─ "Monde"          lieux, histoire, règles de la magie
 ├─ "Personnages"    le casting principal
 └─ "Notes du livre 2"
```

- Les sous-lorebooks peuvent eux-mêmes avoir des sous-lorebooks ; tous les niveaux sont inclus.
- Un lorebook peut être le sous-lorebook de plusieurs lorebooks. Lorsqu'il est atteint plusieurs fois, ses entrées ne sont utilisées qu'une seule fois.
- Un lorebook ne peut pas s'inclure lui-même, directement ou par l'intermédiaire d'autres lorebooks : un tel choix est refusé avec le message *Impossible de sélectionner le sous-lorebook : cycle détecté.*
- Un sous-lorebook désactivé est exclu, avec tout ce qu'il inclut.

![Un lorebook avec deux sous-lorebooks : les jetons avec et sans eux](../../images/lorebook-sub-lorebooks.png)

Les entrées de tous les lorebooks inclus sont réunies puis triées selon leur [ordre](entries.md#order-and-insertion), quel que soit le lorebook d'origine.

<a id="disabling-and-deleting"></a>

## Désactivation et suppression

Décochez **Activé** pour désactiver temporairement un lorebook, par exemple pour voir comment un livre se lit sans lui. Les livres continuent de le référencer, et il fonctionne à nouveau lorsque vous le cochez.

**Supprimer le lorebook** retire le lorebook de vos listes. Les livres qui l'utilisaient ne reçoivent plus ses entrées, et les lorebooks qui l'incluaient comme sous-lorebook l'ignorent. Les données restent dans la base de données jusqu'à ce qu'un administrateur lance le [nettoyage](../administration/cleanup.md) ; d'ici là, vous pouvez restaurer le lorebook depuis la [corbeille](../trash.md). Les copies présentes dans les [sauvegardes de livres](../books/backups.md) et les fichiers exportés ne sont pas affectés.
