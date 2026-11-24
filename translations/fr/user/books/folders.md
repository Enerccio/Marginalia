---
label: Dossiers
order: 98
---

# Dossiers

Les dossiers classent vos livres comme les dossiers d'un disque classent les fichiers. Un dossier contient des livres et d'autres dossiers, aussi profondément que vous le voulez, et la liste des livres n'affiche qu'un dossier à la fois. Un dossier peut aussi contenir des **réglages** (langue, point de vue, temps, style, modèles et invites) : les livres qu'il contient, ainsi que ceux des dossiers situés en dessous, écrivent avec ces réglages, sauf s'ils en ont de propres. C'est la principale raison d'utiliser les dossiers : régler le style d'une série une seule fois, et chaque livre de la série en profite.

![L'onglet Books avec des dossiers](../../images/books-folders.png)

<a id="moving-around"></a>

## Se déplacer

Au-dessus des livres, la liste affiche les dossiers du dossier courant, dans l'ordre des colonnes triées. Cliquez sur le **nom** d'un dossier pour l'ouvrir. Le chemin de boutons au-dessus de la liste (`Books / Sci-fi / Expanse`) indique où vous vous trouvez ; cliquez sur une partie de ce chemin pour revenir en arrière.

La case **Include subfolders** affiche tous les livres situés sous le dossier courant, à n'importe quelle profondeur (au niveau racine, cela correspond à tous vos livres), et ajoute une colonne **Folder** indiquant l'emplacement de chacun. Le dossier courant et la case sont mémorisés, y compris après un rechargement de la page.

Une ligne de dossier indique le nombre de livres qu'il contient, à n'importe quelle profondeur. Une ligne de livre affiche les mots et les tokens de sa [branche active](branches-and-story-tree.md). Les filtres **Name** et **Tags** fonctionnent sur les dossiers comme sur les livres (les dossiers ont des étiquettes, voir [Étiqueter les dossiers](#tagging-folders)), dans le dossier courant, ou en dessous avec *Include subfolders*.

La date **Last Modified** d'un dossier suit celle de ses livres : lorsqu'un livre qu'il contient, ou un livre d'un dossier situé en dessous, est modifié (ou l'une de ses parties), le dossier est lui aussi mis à jour. Triez sur cette colonne pour voir où vous avez écrit en dernier. Ouvrir un livre ou le déplacer ne compte pas.

<a id="what-the-buttons-do"></a>

### À quoi servent les boutons

| Bouton | |
|---|---|
| **New folder** (en-tête) | Crée un dossier dans le dossier courant. Un nom ne doit pas être vide, dépasser 255 caractères ni contenir un `/`, et ne doit pas être le nom d'un autre dossier au même emplacement (la casse n'a pas d'importance). |
| la roue dentée d'un dossier | Les [réglages du dossier](#folder-settings). |
| le crayon d'un dossier | Renomme le dossier. |
| la corbeille d'un dossier | Supprime le dossier, voir [Supprimer un dossier](#deleting-a-folder). |
| **Move to folder** (un livre) | Choisit le dossier dans lequel placer le livre. |
| l'étoile d'un livre | En fait un [favori](#favorites). |
| **Add Book**, les imports | Le nouveau livre va dans le **dossier courant**. Un [clone](managing-books.md#cloning-a-book) est placé à côté de son original. |

<a id="folder-settings"></a>

## Réglages d'un dossier

La roue dentée d'un dossier ouvre ses réglages (la fenêtre **Organise** a le même bouton, raccourci `i`) :

![Les réglages d'un dossier](../../images/folder-settings.png)

- **Name**, **Description** et **Tags**.
- **Language**, ainsi que les mêmes champs que l'onglet [Prompts](prompts.md) d'un livre : modèle principal, point de vue, temps, style et invites.
- **Versions kept per part** : le nombre de versions antérieures d'une partie conservées dans l'[historique](version-history.md#how-many-versions-are-kept) pour les livres du dossier et des dossiers situés en dessous. Parmi toutes les limites ci-dessus, c'est la plus stricte qui s'applique à un livre.
- **Extension Settings**, lorsqu'une [extension](../extensions/index.md) chargée a des réglages dont héritent les livres, voir [Réglages des extensions](prompts.md#settings-of-extensions).
- **Phrase watch list** : les phrases recherchées dans les livres du dossier, ajoutées à celles de vos réglages et des dossiers supérieurs, voir [Surveiller des phrases](annotations.md#watching-for-phrases).

Tout est facultatif. Un champ vide est *hérité*, et l'indique : le texte gris dans le champ est la valeur héritée, et la ligne en dessous indique d'où elle vient. Appuyez sur **Save** pour enregistrer le dossier ; le nom et la description peuvent être modifiés à tout moment, mais un changement de langue ou d'un réglage de texte est refusé tant qu'un agent travaille sur un livre du dossier ou situé en dessous : le message indique le livre concerné, il faut d'abord [mettre en pause ou arrêter son exécution](../agents.md).

<a id="where-the-settings-of-a-book-come-from"></a>

### D'où viennent les réglages d'un livre

Pour chacun de ces réglages, un livre utilise le premier des éléments suivants qui n'est pas vide :

1. le **livre** lui-même (ses onglets *About* et *Prompts*),
2. le **dossier** dans lequel se trouve le livre,
3. le dossier au-dessus de celui-ci, et ainsi de suite jusqu'à la racine,
4. vos **réglages utilisateur** (*Settings → Templates*, voir [Compte et réglages](../account.md)),
5. la **valeur par défaut intégrée**.

Un texte vide compte comme non défini. Cette recherche se fait au moment où le réglage est nécessaire (lorsqu'une partie est générée, qu'un résumé est produit, qu'une partie est réécrite...) ; rien n'est copié dans le livre lorsque vous le placez dans un dossier : **déplacer un livre dans un autre dossier modifie la façon dont il est écrit**, et modifier un réglage d'un dossier change tous les livres situés en dessous qui ne définissent pas le leur.

L'onglet [Prompts](prompts.md#inherited-settings) d'un livre l'indique : sous chaque champ figure l'origine de sa valeur (*Inherited from the folder "Sci-fi"*, *Inherited from your settings* ou *Built-in default*).

<a id="language"></a>

### Langue

La case **Language** d'un livre (onglet [About](managing-books.md#the-about-tab)) et celle d'un dossier peuvent être laissées vides, ce qui signifie *hériter* : le livre prend la langue du dossier le plus proche au-dessus de lui qui en définit une, sinon l'anglais. Une case vide indique quelle est cette langue et d'où elle vient. La case possède un bouton d'effacement ; l'effacer rétablit l'héritage (auparavant, elle repassait à l'anglais). Tout ce qui affiche ou utilise la langue d'un livre utilise celle qu'il obtient avec ses dossiers : la césure du texte, l'export et le tableau [Mass Change Language](#mass-changes).

<a id="deleting-a-folder"></a>

## Supprimer un dossier

Le bouton corbeille d'un dossier demande quoi faire :

- **un dossier vide** est supprimé après confirmation ;
- **un dossier contenant des livres ou des dossiers** propose **Move its content to the folder above** (par défaut) ou **Delete everything in it as well**. La seconde option demande une nouvelle confirmation, avec le nombre de livres et de dossiers, et envoie les livres dans la [Trash](../trash.md), d'où vous pouvez les restaurer. Le dossier supprimé lui-même n'apparaît pas dans la Trash.

Si un dossier contenu a le nom d'un dossier situé au-dessus, le déplacement du contenu vers le haut est refusé (les noms sont uniques) : renommez d'abord l'un des deux. Un dossier contenant un livre sur lequel un agent travaille ne peut être ni supprimé ni déplacé, car les réglages de ce livre changeraient pendant l'exécution.
## Organise

**Organise** (dans le menu **Manage** de l'en-tête de la liste) ouvre une fenêtre à deux volets, chacun affichant un dossier, pour déplacer et copier des livres et des dossiers d'un volet à l'autre sans les ouvrir un par un. Elle s'ouvre avec le volet de gauche sur le dossier où vous étiez et le volet de droite sur celui qu'elle affichait la fois précédente.

![La fenêtre Organise](../../images/organise-dialog.png)

| Touche | |
|---|---|
| Left / Right / Tab | L'autre volet. |
| Up / Down | Le curseur. |
| Enter | Ouvre le dossier sous le curseur. |
| Backspace | Remonte d'un niveau. |
| Space ou Insert | Marque l'entrée sous le curseur ; Shift + Up / Down marque en se déplaçant. |
| Clic sur une case à cocher | Marque ou démarque l'entrée ; **Shift + clic** marque (ou efface) toutes les entrées entre elle et la case cliquée précédemment. |

Une rangée de boutons en bas agit sur ce qui est **marqué** (ou, si rien ne l'est, sur ce qui est sous le curseur) et affiche sa touche entre crochets :

| Bouton | Touche | |
|---|---|---|
| **Copy** | `c` | Copie dans le dossier de l'*autre* volet. |
| **Move** | `m` | Déplace dans le dossier de l'autre volet. |
| **New folder** | `n` | Crée un dossier dans le dossier du volet actif. |
| **Rename** | `Shift+r` | Renomme le dossier ou le livre sous le curseur. |
| **Delete** | `Delete` | Supprime l'élément, comme dans la liste. |
| **Folder settings** | `i` | Les [paramètres](#folder-settings) du dossier sous le curseur. |
| **Mass Change Model/Protocol**, **Mass Change Language**, **Mass Rename**, **Tag books** | `p`, `l`, `Shift+n`, `t` | Les [modifications en masse](#mass-changes) pour les livres marqués, ou pour les livres du dossier du volet actif (avec leur propre *Include subfolders*). Les volets affichent les nouveaux noms après un renommage en masse. |
| **Advanced search** | `f` | La [recherche](managing-books.md#advanced-search) dans le dossier du volet actif, éventuellement avec *Include subfolders*. Un résultat propose **Show in folder**, qui place le volet sur ce dossier et marque le livre. |
| **Show folder in the list** | `o` | Ferme la fenêtre et ouvre le dossier du volet actif dans la liste des livres. |

Lorsque vous fermez la fenêtre, la liste des livres est relue ; ce que vous avez fait, déplacé, copié, renommé ou supprimé apparaît donc immédiatement (si le dossier où se trouvait la liste n'existe plus, la liste passe au niveau supérieur).

Les touches sont des actions des [raccourcis clavier](../account.md#keyboard-shortcuts) des *Settings* : modifiez-les ou effacez-les là-bas (il n'y a pas de touches de fonction par défaut, un Mac et les navigateurs les réservent). Les boutons affichent la touche que vous avez définie.

<a id="copying"></a>

### Copie

Une **copie** d'un livre est un [clone](managing-books.md#cloning-a-book) ; la fenêtre demande s'il faut ne prendre que la branche courante. Une copie de dossier est un nouveau dossier avec les paramètres, la description et les étiquettes de l'original, ainsi que des copies de tous les livres et dossiers qu'il contient. Une copie peut être longue : elle s'exécute derrière une barre de progression qui ne peut pas être arrêtée. Si elle échoue en cours de route, les copies faites auparavant sont conservées, chacune complète.

Un **déplacement** est immédiat et peut être annulé en le déplaçant en sens inverse. Un dossier ne peut pas être déplacé dans lui-même ni dans un dossier qui se trouve sous lui.

<a id="when-a-name-is-taken"></a>

### Quand un nom est déjà pris

Lorsque la cible contient déjà un dossier du même nom, la fenêtre demande quoi faire de ce dossier, avec **Apply to all** pour les suivants :

| Choix | |
|---|---|
| **Rename** | Le dossier reçoit un autre nom. |
| **Skip** | Il reste à sa place (et n'est pas copié). |
| **Merge** | Son contenu est placé dans le dossier existant ; les dossiers de même nom à l'intérieur fusionnent à leur tour. |

Les livres n'entrent jamais en conflit : leurs noms peuvent se répéter.

<a id="mass-changes"></a>

## Modifications en masse

Le menu **Manage → Mass changes** de la liste (et les boutons d'Organise) modifie plusieurs livres à la fois. **Mass Change Model/Protocol** applique un modèle et/ou un protocole aux livres, **Mass Change Language** une langue, **Mass Rename** ajoute un préfixe ou un suffixe à leurs noms, remplace un texte dans ceux-ci ou les numérote, et **Tag books** et **Tag folders** ajoutent ou retirent une étiquette. Voir [Gérer les livres](managing-books.md#tagging-many-books-at-once). Depuis la liste, elles agissent sur les livres du dossier courant, avec leur propre **Include subfolders**.

<a id="favorites"></a>

## Favoris

L'étoile sur la ligne d'un livre en fait un **favori**. Tant qu'il en existe, le niveau supérieur affiche le dossier virtuel **Favorites** (une étoile) avec les livres favoris de tous vos dossiers. Ce n'est qu'une vue : vous ne pouvez pas y créer de livre ou de dossier, et retirer l'étoile retire le livre de cette vue, pas de son dossier. Les favoris sont conservés dans vos paramètres.

<a id="tagging-folders"></a>

## Étiqueter des dossiers

Les dossiers ont des étiquettes, comme les livres. **Manage → Mass changes → Tag folders** étiquette de nombreux dossiers à la fois. Les étiquettes d'un dossier ne modifient pas celles de ses livres, et elles ne font pas partie d'une [sauvegarde](backups.md).

<a id="folders-and-other-features"></a>

## Dossiers et autres fonctions

- **Sauvegardes.** Une [sauvegarde](backups.md#backups-and-folders) retient les dossiers de son livre, et un fichier exporté peut contenir les paramètres que le livre hérite.
- **Agents.** Un [agent](../agents.md#agents-and-folders) écrit avec les paramètres des dossiers de son livre.
- **Assistant d'aide.** Si vous l'autorisez, l'[assistant](../administration/help-assistant.md#your-books) peut consulter vos livres et dossiers.
- **Téléphone.** La [disposition pour téléphone](../phone.md#books) affiche les mêmes dossiers sous forme de cartes et permet de les créer, de les renommer, de les configurer et de les supprimer ; Organise et les modifications en masse se trouvent dans le menu **⋮** de son en-tête.
- **Visionneuse.** La [visionneuse](publishing.md#folders-in-the-list) affiche aussi les dossiers et s'ouvre dans celui que vous avez quitté.
