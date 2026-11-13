---
label: Corbeille
order: 955
icon: trash
---

# Corbeille

Supprimer dans Marginalia ne détruit rien immédiatement : un livre, un lorebook, une partie de l'histoire ou un fournisseur d'inférence supprimé est seulement marqué comme supprimé et masqué de vos listes. L'onglet **Corbeille** liste ces objets supprimés et permet de les restaurer.

Les objets supprimés restent dans la corbeille jusqu'à ce qu'un administrateur lance le [nettoyage](administration/cleanup.md), qui les supprime définitivement de la base de données. Ce que le nettoyage a retiré ne peut plus être restauré (seule une [sauvegarde de la base de données](administration/database-backups.md) réalisée auparavant le contient encore).

![L'onglet Corbeille](../images/trash-tab.png)

La table se charge au fil du défilement. Les objets supprimés le plus récemment apparaissent en premier.

| Colonne | |
|---|---|
| (case à cocher) | Sélectionne l'objet à restaurer. La case de l'en-tête sélectionne tous les objets. |
| **Type** | Le genre d'objet : livre, lorebook, entrée de lorebook, message (une partie de l'histoire), version d'une partie (une entrée supprimée de l'[historique des versions](books/version-history.md#deleting-versions) ; la restaurer la sort de la corbeille de l'historique), résumé, modèle (fournisseur d'inférence), protocole, étiquette, agent, exécution d'agent, ressource... |
| **Nom** | Le nom de l'objet. Les parties de l'histoire et les liens d'étiquette n'ont pas de nom et affichent leur identifiant ; utilisez *Contenu étendu* pour lire une partie de l'histoire. |
| **Utilisateur** | À qui appartient l'objet. Seuls les administrateurs voient cette colonne. |
| **Supprimés** | Le moment de la suppression. |
| **Contenu étendu** | Ouvre les données enregistrées de l'objet, voir [ci-dessous](#extended-content). |

Au-dessus de la table, **Type** n'affiche qu'un seul genre d'objet, **Actualiser** recharge la table.

<a id="restoring"></a>

## Restaurer

Cochez les objets à restaurer puis cliquez sur **Restaurer la sélection**. Marginalia demande confirmation avant de les restaurer. Les objets reviennent dans leurs listes, exactement tels qu'ils étaient au moment de la suppression, et les autres onglets sont actualisés.

<a id="objects-that-belong-to-other-objects"></a>

### Objets qui appartiennent à d'autres objets

Supprimer un objet supprime ce qui lui appartient, et le restaurer ramène ces éléments : un livre emporte avec lui ses parties de l'histoire, leurs résumés et ses étiquettes ; un lorebook, ses entrées ; une partie de l'histoire, son résumé et les parties situées en dessous ; un fournisseur d'inférence, ses statistiques d'utilisation. Vous n'avez pas besoin de les cocher : restaurez le livre, et son histoire revient avec lui. Ils apparaissent dans la table comme des lignes distinctes (un livre à l'histoire longue remplit la table de ses parties) ; restaurer l'un d'eux seul n'est possible que si l'objet auquel il appartient n'est pas supprimé. Un élément que vous aviez supprimé séparément auparavant (une entrée, une partie de l'histoire) revient aussi avec son lorebook ou son livre, car Marginalia ne peut pas le distinguer de ce qui a été supprimé en même temps.

Certains objets ne peuvent pas exister sans un autre objet :

- une entrée a besoin de son lorebook,
- une partie de l'histoire et un résumé ont besoin de leur livre,
- un livre a besoin de son modèle (fournisseur d'inférence), de son protocole et de son lorebook, tant qu'il les utilise,
- un lien d'étiquette a besoin de son étiquette.

Un objet dont le lorebook, le livre... est supprimé **ne peut être restauré qu'avec lui** : cochez les deux. Si vous ne cochez que l'entrée, Marginalia ne restaure **rien** et vous indique ce qui manque :

> Rien n'a été restauré. Ces objets ont besoin d'un objet supprimé qui n'est pas sélectionné. Sélectionnez-le aussi, ou laissez-les supprimés.
> L'entrée de lorebook « mosh » a besoin du lorebook supprimé « Phrasebook » (lorebook)

![Restauration refusée parce que le lorebook de l'entrée est lui aussi supprimé](../images/trash-restore-blocked.png)

Cochez l'objet indiqué et relancez la restauration. Les objets qui ne sont plus supprimés (restaurés entre-temps, ou jamais supprimés) ne posent pas de problème.

!!!info Une partie de l'histoire restaurée
Une partie de l'histoire supprimée au milieu d'un récit a fait remonter ses parties suivantes vers son parent. La restaurer la ramène sous son parent comme une nouvelle branche, **sans** les parties qui la suivaient : elles restent là où elles ont été déplacées. Restaurez plutôt le livre entier lorsque vous voulez retrouver l'histoire telle qu'elle était.
!!!

<a id="deleting-for-good"></a>

## Supprimer définitivement

Attendre le nettoyage n'est pas toujours souhaitable : une partie de l'histoire ou une entrée de lorebook dont vous savez n'avoir plus jamais besoin peut être retirée tout de suite. Cochez-la puis cliquez sur **Supprimer définitivement** ; Marginalia demande d'abord confirmation, et **cette action est irréversible**.

![Suppression définitive d'une partie de l'histoire](../images/trash-delete-permanently.png)

- Seules les **parties de l'histoire** et les **entrées de lorebook** peuvent être supprimées définitivement (avec leurs résumés et étiquettes). Le bouton est désactivé dès qu'autre chose est coché : les livres, lorebooks et le reste attendent le [nettoyage](administration/cleanup.md) de l'administrateur, afin que vous ne puissiez pas supprimer un livre entier par accident.
- Les parties de l'histoire situées sous une partie retirée ne sont pas retirées avec elle, qu'elles soient supprimées ou non : elles remontent vers son parent, comme lorsque vous supprimez une partie dans l'éditeur. Si la partie était la position à laquelle un livre était ouvert, le livre s'ouvre désormais sur le parent.
- Marginalia ne supprime rien lorsqu'un élément ne peut pas être retiré (par exemple si une extension a encore besoin de l'objet) et vous indique lequel.

<a id="extended-content"></a>

### Contenu étendu

Marginalia conserve une grande partie des données d'un objet (le texte d'une partie de l'histoire, les réglages d'une entrée de lorebook, les notes que des [extensions](extensions/index.md) ont enregistrées) sous forme de JSON à côté de lui. **Contenu étendu** dans une ligne affiche ces données, afin de vérifier ce qu'un objet contient avant de le restaurer, par exemple pour retrouver la bonne partie de l'histoire. Les objets sans de telles données (ressources) n'ont pas de lien ; un objet qui n'en a pas affiche *Cet objet n'a pas de contenu étendu.*

<a id="administrators"></a>

## Administrateurs

Les administrateurs voient les objets supprimés de **tous les utilisateurs** et peuvent restaurer n'importe lequel d'entre eux. Le filtre **Utilisateur** au-dessus de la table affiche les objets d'un seul utilisateur ; la colonne **Utilisateur** indique à qui ils appartiennent. Les objets restaurés retournent à leur propriétaire.

La même table se trouve dans *Admin → Nettoyage* (**Corbeille**), ce qui permet de voir ce que le prochain nettoyage retirera. Les objets d'un utilisateur supprimé ne peuvent pas être restaurés : ils dépendent de cet utilisateur.
