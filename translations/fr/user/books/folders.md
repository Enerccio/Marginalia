---
label: Dossiers
order: 98
---

# Dossiers

Les dossiers classent vos livres comme les dossiers d'un disque classent les fichiers. Un dossier contient des livres et d'autres dossiers, aussi profonds que vous le souhaitez,
et la liste des livres n'affiche qu'un dossier à la fois. Un dossier peut aussi contenir des **paramètres** (langue, point de vue, temps, style,
les modèles et les consignes) : les livres qu'il contient, ainsi que ceux des dossiers placés en dessous, écrivent avec eux, sauf s'ils ont leurs propres paramètres.
C'est la principale raison d'utiliser des dossiers : réglez le style d'une série une seule fois, et chaque livre de la série en bénéficie.

![L'onglet Livres avec les dossiers](../../images/books-folders.png)

<a id="moving-around"></a>

## Se déplacer

Au-dessus des livres, la liste affiche les dossiers du dossier courant, dans l'ordre des colonnes triées. Cliquez sur le **nom** d'un
dossier pour l'ouvrir. Le chemin de boutons au-dessus de la liste (`Books / Sci-fi / Expanse`) indique où vous vous trouvez ; cliquez sur une partie
de ce chemin pour y revenir.

La case **Inclure les sous-dossiers** liste tous les livres situés sous le dossier courant, à n'importe quelle profondeur (au niveau racine, cela correspond à
tous vos livres), et ajoute une colonne **Dossier** indiquant l'emplacement de chacun. Le dossier courant et la case sont
mémorisés, y compris lorsque vous rechargez la page.

Une ligne de dossier indique le nombre de livres qu'il contient, à n'importe quelle profondeur. Une ligne de livre affiche les mots et les jetons de sa
[branche active](branches-and-story-tree.md). Les filtres **Nom** et **Mots-clés** fonctionnent aussi bien sur les dossiers que sur les livres
(les dossiers ont des mots-clés, voir [Ajouter des mots-clés à un dossier](#tagging-folders)), dans le dossier courant, ou en dessous avec *Inclure les sous-dossiers*.

La **Dernière modification** d'un dossier suit celle de ses livres : lorsqu'un livre qu'il contient, ou un livre d'un dossier placé en dessous, change (ou qu'une de ses parties change), le dossier est lui aussi mis à jour. Triez par cette colonne pour voir où vous avez écrit en dernier. Ouvrir un livre ou le déplacer ne compte pas.

<a id="what-the-buttons-do"></a>

### Rôle des boutons

| Bouton | |
|---|---|
| **Nouveau dossier** (en-tête) | Crée un dossier dans le dossier courant. Un nom ne doit pas être vide, dépasser 255 caractères ni contenir un `/`, et ne doit pas être le nom d'un autre dossier au même emplacement (la casse n'a pas d'importance). |
| la roue crantée d'un dossier | Les [paramètres du dossier](#folder-settings). |
| le crayon d'un dossier | Le renomme. |
| la corbeille d'un dossier | Le supprime, voir [Supprimer un dossier](#deleting-a-folder). |
| **Déplacer vers le dossier** (un livre) | Choisit le dossier où placer le livre. |
| l'étoile d'un livre | En fait un [favori](#favorites). |
| **Ajouter un livre**, les imports | Le nouveau livre va dans le **dossier courant**. Un [clone](managing-books.md#cloning-a-book) est placé à côté de son original. |

<a id="folder-settings"></a>

## Paramètres d'un dossier

La roue crantée d'un dossier ouvre ses paramètres (la fenêtre **Organiser** possède le même bouton, touche `i`) :

![Les paramètres d'un dossier](../../images/folder-settings.png)

- **Nom**, **Description** et **Mots-clés**.
- **Langue**, ainsi que les mêmes champs que l'onglet [Consignes](prompts.md) d'un livre : modèle principal, point de vue, temps,
  style et les consignes.
- **Versions conservées par partie** : le nombre de versions antérieures d'une partie que l'[historique](version-history.md#how-many-versions-are-kept)
  conserve pour les livres du dossier et pour les dossiers placés en dessous. De toutes les limites qui s'appliquent à un livre, c'est la plus stricte qui compte.
- **Paramètres des extensions**, lorsqu'une [extension](../extensions/index.md) chargée possède des paramètres dont les livres héritent, voir
  [Paramètres des extensions](prompts.md#settings-of-extensions).

Tout est facultatif. Un champ vide est *hérité*, et le dit : le texte gris du champ est la valeur qu'il hérite,
et la ligne en dessous indique d'où elle vient. Appuyez sur **Enregistrer** pour sauvegarder le dossier ; le nom et la description peuvent être
modifiés à tout moment, mais un changement de langue ou d'un paramètre de texte est refusé tant qu'un agent travaille sur un livre du
dossier ou de l'un de ses sous-dossiers : le message indique le livre, [mettez d'abord son exécution en pause ou arrêtez-la](../agents.md).

<a id="where-the-settings-of-a-book-come-from"></a>

### D'où viennent les paramètres d'un livre

Pour chacun de ces paramètres, un livre utilise la première de ces sources qui n'est pas vide :

1. le **livre** lui-même (ses onglets *À propos* et *Consignes*),
2. le **dossier** qui contient le livre,
3. le dossier au-dessus de celui-ci, et ainsi de suite jusqu'à la racine,
4. vos **paramètres utilisateur** (*Paramètres → Modèles*, voir [Compte et paramètres](../account.md)),
5. la **valeur intégrée par défaut**.

Un texte vide compte comme non défini. Cette recherche se fait au moment où elle est nécessaire (lors de la génération d'une partie, de la création d'un résumé, de la réécriture d'une partie...) ; rien n'est copié dans le livre lorsque vous le placez dans un dossier : **déplacer un livre dans un autre dossier modifie la manière dont il est écrit**, et modifier un paramètre d'un dossier change chaque livre situé en dessous qui ne définit pas le sien.

L'onglet [Consignes](prompts.md#inherited-settings) d'un livre l'affiche : sous chaque champ se trouve l'origine de sa valeur
(*Hérité du dossier « Sci-fi »*, *Hérité de vos paramètres* ou *Valeur intégrée par défaut*).

<a id="language"></a>

### Langue

La case **Langue** d'un livre (onglet [À propos](managing-books.md#the-about-tab)) et celle d'un dossier peuvent être laissées vides,
ce qui signifie *hériter* : le livre prend la langue du dossier le plus proche au-dessus de lui qui en définit une, sinon l'anglais. Une case vide indique
quelle langue cela donne et d'où elle vient. La case possède un bouton d'effacement ; l'effacer rétablit l'héritage (elle se transformait autrefois en anglais).
Tout ce qui affiche ou utilise la langue d'un livre emploie celle qu'il obtient avec ses dossiers : la
césure du texte, l'export et le tableau [Changer la langue en masse](#mass-changes).

<a id="deleting-a-folder"></a>

## Supprimer un dossier

Le bouton corbeille d'un dossier demande ce qu'il faut faire :

- **un dossier vide** est supprimé après confirmation ;
- **un dossier contenant des livres ou des dossiers** propose **Déplacer son contenu dans le dossier parent** (par défaut) ou **Supprimer aussi tout ce qu'il contient**. La seconde option demande une nouvelle confirmation, avec le nombre de livres et de dossiers, et envoie les livres dans la [Corbeille](../trash.md), d'où vous pouvez les restaurer. Un dossier supprimé n'apparaît pas lui-même dans la Corbeille.

Si un dossier contenu porte le nom d'un dossier du niveau supérieur, le déplacement du contenu vers le haut est refusé (les noms sont uniques) : renommez d'abord l'un des deux. Un dossier contenant un livre sur lequel un agent travaille ne peut être ni supprimé ni déplacé, car les paramètres de ce livre changeraient en cours d'exécution.
<a id="organise"></a>

## Organiser

**Organiser** (dans le menu **Gérer** de l'en-tête de la liste) ouvre une fenêtre à deux volets, chacun affichant un dossier, pour déplacer et copier des livres et des dossiers d'un volet à l'autre sans ouvrir chacun d'eux. Elle s'ouvre avec le volet gauche sur le dossier où vous étiez et le volet droit sur celui qu'elle affichait la fois précédente.

![La fenêtre Organiser](../../images/organise-dialog.png)

| Touche | |
|---|---|
| Gauche / Droite / Tab | L'autre volet. |
| Haut / Bas | Le curseur. |
| Entrée | Ouvre le dossier sous le curseur. |
| Retour arrière | Remonte d'un niveau. |
| Espace ou Insert | Marque l'élément sous le curseur ; Maj + Haut / Bas marque en se déplaçant. |
| Clic sur une case à cocher | Marque ou démarque l'élément ; **Maj + clic** marque (ou efface) tous les éléments entre celui-ci et la case cliquée précédemment. |

Une rangée de boutons en bas agit sur ce qui est **marqué** (ou, si rien ne l'est, sur ce qui se trouve sous le curseur), et affiche sa touche entre crochets :

| Bouton | Touche | |
|---|---|---|
| **Copier** | `c` | Copie dans le dossier de l'*autre* volet. |
| **Déplacer** | `m` | Déplace dans le dossier de l'autre volet. |
| **Nouveau dossier** | `n` | Crée un dossier dans le dossier du volet actif. |
| **Renommer** | `Shift+r` | Renomme le dossier ou le livre sous le curseur. |
| **Supprimer** | `Delete` | Le supprime, comme dans la liste. |
| **Paramètres du dossier** | `i` | Les [paramètres](#folder-settings) du dossier sous le curseur. |
| **Changer le modèle/protocole en masse**, **Changer la langue en masse**, **Renommage en masse**, **Étiqueter des livres** | `p`, `l`, `Shift+n`, `t` | Les [modifications en masse](#mass-changes) pour les livres marqués, ou pour les livres du dossier du volet actif (avec leur propre option *Inclure les sous-dossiers*). Les volets affichent les nouveaux noms après un renommage en masse. |
| **Recherche avancée** | `f` | La [recherche](managing-books.md#advanced-search) dans le dossier du volet actif, éventuellement avec *Inclure les sous-dossiers*. Un résultat propose **Afficher dans le dossier**, qui place le volet à cet endroit et marque le livre. |
| **Afficher le dossier dans la liste** | `o` | Ferme la fenêtre et ouvre le dossier du volet actif dans la liste des livres. |

Lorsque vous fermez la fenêtre, la liste des livres est relue, de sorte que ce que vous avez fait, déplacé, copié, renommé ou supprimé apparaît immédiatement (si le dossier où se trouvait la liste n'existe plus, celle-ci passe au niveau racine).

Les touches correspondent aux actions des [raccourcis clavier](../account.md#keyboard-shortcuts) dans *Paramètres* : modifiez-les ou supprimez-les là-bas (il n'y a pas de touches de fonction par défaut, un Mac et les navigateurs les réservent). Les boutons affichent la touche que vous avez définie.

<a id="copying"></a>

### Copier

Une **copie** d'un livre est un [clone](managing-books.md#cloning-a-book) ; la fenêtre demande s'il faut ne prendre que la branche courante. Une copie de dossier est un nouveau dossier avec les paramètres, la description et les étiquettes de l'original, ainsi que des copies de tous les livres et dossiers qu'il contient. Une copie peut durer longtemps : elle s'effectue derrière une barre de progression qui ne peut pas être interrompue. Si elle échoue à mi-parcours, les copies déjà faites sont conservées, chacune complète.

Un **déplacement** est effectué immédiatement et peut être annulé en repoussant l'élément dans l'autre sens. Un dossier ne peut pas être déplacé dans lui-même ni dans un dossier situé en dessous de lui.

<a id="when-a-name-is-taken"></a>

### Lorsqu'un nom est déjà pris

Lorsque la destination contient déjà un dossier du même nom, la fenêtre demande quoi faire de ce dossier, avec **Appliquer à tous** pour la suite :

| Choix | |
|---|---|
| **Renommer** | Le dossier reçoit un autre nom. |
| **Passer** | Il reste à sa place (et n'est pas copié). |
| **Fusionner** | Son contenu est placé dans le dossier existant ; les dossiers de même nom qu'il contient fusionnent à leur tour. |

Les livres ne posent jamais de conflit : leurs noms peuvent se répéter.

<a id="mass-changes"></a>

## Modifications en masse

Le menu **Gérer → Modifications en masse** de la liste (et les boutons d'Organiser) modifient plusieurs livres à la fois. **Changer le modèle/protocole en masse** applique un modèle et/ou un protocole aux livres, **Changer la langue en masse** une langue, **Renommage en masse** ajoute un préfixe ou un suffixe à leurs noms, remplace un texte dans ceux-ci ou les numérote, **Étiqueter des livres** et **Étiqueter des dossiers** ajoutent ou retirent une étiquette. Voir [Gérer les livres](managing-books.md#tagging-many-books-at-once). Depuis la liste, elles travaillent sur les livres du dossier courant, avec leur propre option **Inclure les sous-dossiers**.

<a id="favorites"></a>

## Favoris

L'étoile sur la ligne d'un livre en fait un **favori**. Tant que vous en avez un, le niveau racine affiche le dossier virtuel **Favoris** (une étoile) qui regroupe les livres favoris de tous vos dossiers. Ce n'est qu'une vue : vous ne pouvez pas y créer de livre ou de dossier, et retirer l'étoile supprime le livre de cette vue, sans le retirer de son dossier. Les favoris sont conservés dans vos paramètres.

<a id="tagging-folders"></a>

## Étiqueter des dossiers

Les dossiers ont des étiquettes, comme les livres. **Gérer → Modifications en masse → Étiqueter des dossiers** étiquette plusieurs dossiers à la fois. Les étiquettes d'un dossier ne modifient pas les étiquettes de ses livres, et elles ne font pas partie d'une [sauvegarde](backups.md).

<a id="folders-and-other-features"></a>

## Dossiers et autres fonctionnalités

- **Sauvegardes.** Une [sauvegarde](backups.md#backups-and-folders) retient les dossiers de son livre, et un fichier exporté peut transporter les paramètres que le livre hérite.
- **Agents.** Un [agent](../agents.md#agents-and-folders) écrit avec les paramètres des dossiers de son livre.
- **Assistant d'aide.** Si vous l'autorisez, l'[assistant](../administration/help-assistant.md#your-books) peut consulter vos livres et dossiers.
- **Téléphone.** La [disposition téléphone](../phone.md#books) affiche les mêmes dossiers sous forme de cartes et permet de les créer, de les renommer, de les configurer et de les supprimer ; Organiser et les modifications en masse se trouvent dans le menu **⋮** de son en-tête.
- **Visionneuse.** La [visionneuse](publishing.md#folders-in-the-list) liste aussi les dossiers et s'ouvre dans celui que vous avez quitté.
