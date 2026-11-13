---
label: Historique des versions
order: 85
---

# Historique des versions d'une partie

Chaque fois que le texte d'une partie change, Marginalia conserve le texte qu'elle avait. Vous pouvez consulter les versions antérieures d'une partie,
voir ce qui a changé entre elles, en restaurer une, donner un nom à une version pour qu'elle ne soit jamais perdue, et rechercher dans l'ancien texte.

Une partie est modifiée par le modèle qui l'écrit (la première génération, **Régénérer**, **Variante**, [**Réécrire**](story-editor.md#redrafting-a-part)),
par vous (**Modifier** et **Enregistrer**), par une action de masse (**Rechercher et remplacer**, un import, une commande), par un
[agent](../agents.md), par une [extension](../extensions/index.md), par la [fusion](story-editor.md#merging-a-part-into-the-previous-one)
de deux parties, et par la restauration d'une version. Chacune de ces opérations crée une **version** avec l'heure et l'auteur de la modification :

| Source | |
|---|---|
| **Générée**, **Régénérée**, **Réécrite** | Un modèle a écrit le texte. La version indique quel modèle et quel protocole ont été utilisés, et conserve le raisonnement qui l'accompagnait. |
| **Auteur** | Vous l'avez tapé. Une modification faite par une commande, une action de masse, un import ou un agent le signale dans la ligne de la version (un agent est marqué). |
| **Extension** | Une extension a modifié le texte. |
| **Fusionnée** | Le texte d'une partie qui a été fusionnée dans celle-ci. |
| **Restaurée** | Une version qui a été remise en haut, une copie de la version qu'elle désigne. |
| **Version enregistrée** | Une version que vous avez enregistrée à la main, avec un nom. |

L'historique n'est pas une seconde copie du livre. Les versions sont stockées à part des parties, de sorte qu'un long historique ne
ralentit pas l'histoire, et un livre exporté, publié ou lu dans la visionneuse ne contient que le texte actuel.

<a id="looking-at-the-history"></a>

## Consulter l'historique

Choisissez **Historique** dans le menu (☰) de la partie. Le nombre entre crochets indique combien de versions la partie possède. (L'élément n'est pas proposé pendant que le texte de la partie est modifié, ni pour une partie qui n'est pas encore enregistrée.)

![Le menu d'une partie avec l'élément Historique](../../images/history-part-menu.png)

![L'historique d'une partie : les versions à gauche, la version sélectionnée avec les modifications à droite](../../images/history-dialog.png)

La liste à gauche place la version la plus récente en haut. Chaque ligne indique l'heure, la source, le nombre de mots et le
nom de la version, si elle en a un ; la version la plus récente est marquée **Actuelle** : c'est le texte que la partie a maintenant. Cliquez sur une ligne
pour voir cette version à droite. Au-dessus du texte, choisissez ce à quoi elle est comparée :

| | |
|---|---|
| **Version précédente** | Les modifications par rapport à la version qui la précède, mot à mot. |
| **Texte actuel** | Les modifications par rapport au texte actuel de la partie. |
| **Texte seulement** | Le texte tel qu'il était, sans comparaison. |

Une version écrite par un modèle affiche son raisonnement dans un bloc que vous pouvez déplier. Une version **Restaurée** indique la version dont elle est
la copie.

Sur un téléphone, la fenêtre occupe tout l'écran : les versions sont des cartes au-dessus de la version sélectionnée, et rien n'exige la souris.

<a id="putting-a-version-back"></a>

## Restaurer une version

Il y a deux façons de faire, et elles diffèrent par ce qui advient des versions postérieures :

| | |
|---|---|
| **Restaurer par-dessus** | Le texte de la version choisie redevient le texte de la partie, sous la forme d'une nouvelle version **Restaurée** en haut. Rien n'est perdu : les versions intermédiaires restent dans la liste. Utilisez-la pour reprendre une formulation antérieure. |
| **Revenir jusqu'ici** | La partie revient à la version choisie, et **toutes les versions plus récentes passent dans la corbeille** de l'historique (on vous le demande, et on vous indique combien). Elles peuvent en être sorties à nouveau. Utilisez-la pour annuler toute une série de modifications. |

Les deux modifient la partie comme le fait une édition : les [résumés](summaries.md) qui décrivent l'ancien texte deviennent obsolètes, et
Marginalia indique combien, et la partie est considérée comme modifiée. L'histoire affiche aussitôt le texte restauré.

<a id="naming-a-version"></a>

## Nommer une version

Une version qui porte un nom est une **version enregistrée**, et l'historique ne la supprime jamais de lui-même.

- **Enregistrer la version** dans la fenêtre enregistre le texte que la partie a maintenant, avec le nom que vous donnez.
- **Libellé…** donne un nom à une version existante, ou modifie ou retire ce nom.

Servez-vous-en avant de tenter quelque chose de risqué : *le brouillon que l'éditeur a aimé*, *avant la coupe du duel*.

<a id="deleting-versions"></a>

## Supprimer des versions

- **Supprimer** place une version dans la **corbeille de l'historique**. Cochez **Afficher les versions supprimées** pour voir la corbeille (les lignes sont grisées) ; **Sortir de la corbeille** remet une version à sa place dans la liste, et **Supprimer définitivement** en retire une pour de bon.
  La version la plus récente ne peut pas être supprimée : c'est le texte de la partie.
- **Vider la corbeille** supprime définitivement tout ce qui s'y trouve.
- **Effacer l'historique** supprime définitivement toutes les versions de la partie sauf la version actuelle. La fenêtre indique combien de versions disparaissent et quelle place elles occupent. **Garder les versions enregistrées et la première version générée** (coché par défaut) conserve celles que vous avez nommées ainsi que la toute première chose que le modèle a écrite pour la partie.

![Ce que Effacer l'historique est sur le point de supprimer](../../images/history-clear.png)

Le menu des réglages (⚙) de l'histoire contient les mêmes actions pour l'ensemble du livre : **Effacer l'historique du livre** et **Vider la corbeille de l'historique**.

![Le menu des réglages de l'histoire avec les actions sur l'historique du livre](../../images/history-menu.png)

<a id="how-many-versions-are-kept"></a>

## Combien de versions sont conservées

Une partie conserve **50 versions**, sauf si une limite indique autre chose. Lorsqu'une nouvelle version dépasserait ce nombre, l'historique supprime d'abord la plus ancienne version de la corbeille, puis la plus ancienne version qui n'est pas dans la corbeille. Les versions dans la corbeille comptent dans la limite tant qu'elles ne sont pas purgées. Trois versions ne sont jamais supprimées : la plus récente, celles qui portent un nom, et la toute première chose que le modèle a générée pour la partie (elles ne comptent pas non plus dans la limite).

La limite peut être fixée à quatre niveaux, et **c'est la plus stricte qui s'applique** :

| Niveau | Où |
|---|---|
| Le livre | **Versions conservées par partie** dans l'onglet [À propos](managing-books.md#the-about-tab) du livre. |
| Les dossiers | **Versions conservées par partie** dans les [réglages du dossier](folders.md#folder-settings) ; tous les dossiers au-dessus du livre sont pris en compte. |
| Vous | **Versions conservées par partie** dans l'onglet [Réglages](../account.md). |
| L'administrateur | La [permission](../administration/permissions.md) **Historique des versions**, un plafond pour l'utilisateur. |

Un champ vide signifie que ce niveau ne fixe aucune limite. **0** ne conserve aucun historique (les versions existantes restent jusqu'à ce que vous les effaciez, mais rien de nouveau n'est ajouté). Le champ affiche, dans son indication, la limite qu'il hérite et le niveau qui la fixe, par exemple *Limitée à 20 par le dossier « Romans »*. Un livre ne peut pas relever une limite fixée au-dessus de lui.

<a id="searching-the-old-text"></a>

## Rechercher dans l'ancien texte

Les recherches principales ne portent que sur le texte actuel, de sorte qu'une ancienne phrase que vous avez modifiée ne fait pas apparaître une partie. Pour la retrouver, cochez **Inclure les versions** (décoché au départ, et non mémorisé, pour qu'une recherche ne vous surprenne jamais avec un ancien texte) :

- dans la [recherche avancée des livres](managing-books.md#advanced-search) : un livre est trouvé lorsqu'une version antérieure de l'une de ses parties contient les mots ;
- dans la recherche de la [vue des branches](branches-and-story-tree.md#searching-all-branches) : une partie qui ne contient les mots que dans une version antérieure est marquée, le résultat indique *Trouvé dans la version du 12 oct. 2026 14:05 (Générée)*, et **Afficher dans l'historique** ouvre l'historique de la partie sur cette version.

![Une partie trouvée grâce à une version antérieure](../../images/history-search.png)

Un agent peut faire de même avec la commande `search-parts` et son option `includeVersions` ; chaque résultat indique alors de quelle version il provient. Les versions dans la corbeille ne sont pas recherchées, et **Rechercher et remplacer** ne modifie jamais une version.

<a id="backups-copies-and-the-trash"></a>

## Sauvegardes, copies et corbeille

- Une [sauvegarde du livre](backups.md#the-history-of-the-parts) peut emporter l'historique avec elle (**Inclure l'historique des versions**, décoché au départ, car il peut être volumineux). Restaurer une telle sauvegarde, comme livre ou comme copie, rétablit les versions, avec leurs noms et la corbeille. Une sauvegarde sans lui donne à chaque partie une première version.
- Un [duplicata d'un livre](managing-books.md#cloning-a-book) démarre son propre historique : une version par partie, à partir du texte avec lequel il a été copié.
- Supprimer une partie, ou un livre, emporte leurs versions ; elles se trouvent dans la [Corbeille](../trash.md) tant que la partie existe, et sont supprimées définitivement avec elle.
- Fusionner une partie dans la précédente supprime l'historique de la partie fusionnée ; le texte apparaît dans l'historique de la partie conservée sous la forme d'une version **Fusionnée**.
<a id="books-from-before-the-history"></a>

## Livres antérieurs à l'historique

Lorsque Marginalia a été mis à jour, chaque partie existante a reçu ses premières versions : une partie qu'un modèle a rédigée commence par une version **Générée** du texte telle que le modèle l'a écrit (avec son raisonnement et le modèle utilisé), et possède une version **Auteur** lorsque vous l'avez modifiée depuis ; toute autre partie commence avec une version **Auteur**. Les modifications antérieures de ces parties ne sont pas connues et ne figurent pas dans la liste.
