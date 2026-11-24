---
label: Nettoyage
order: 80
---

# Nettoyage

Lorsque les utilisateurs suppriment un élément dans Marginalia (un livre, une partie d'histoire, un lorebook, un fournisseur d'inférence), celui-ci disparaît de leurs listes, mais reste dans la base de données. La suppression est ainsi sans risque, et les références continuent de fonctionner (un livre connaît toujours le fournisseur qui a écrit ses parties). Jusque-là, les utilisateurs peuvent restaurer leurs objets supprimés depuis l'onglet [Trash](../trash.md). **Cleanup** supprime définitivement ces données supprimées, afin de garder la base de données légère.

![L'onglet Cleanup](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Lancer un nettoyage

!!!warning
Le nettoyage ne peut pas être annulé : ce qu'il supprime disparaît aussi de la [Trash](../trash.md). [Créez une sauvegarde de la base de données](database-backups.md) avant tout.
!!!

1. Ouvrez l'onglet **Cleanup** de la page d'administration et cliquez sur **Analyze**. Rien n'est encore modifié.
2. Vérifiez le résultat (voir ci-dessous).
3. Cliquez sur **Run Cleanup** et confirmez *n objects will be permanently removed*.

Le nettoyage porte sur les données de tous les utilisateurs.

Le nettoyage supprime des entrées de la base de données, et seulement les fichiers des [agents](../agents.md) : l'espace de travail d'un agent purgé et le dossier d'une exécution purgée. Les fichiers des [ressources](../resources.md) supprimées ou inutilisées (images des parties, audio) restent dans le dossier de données de leur propriétaire jusqu'à ce que vous les retiriez dans l'onglet [Resource Files](resource-files.md).

<a id="seeing-what-is-deleted"></a>

## Voir ce qui est supprimé

La section **Trash**, sous les tableaux, liste les objets supprimés de tous les utilisateurs, une ligne par objet (ouvrez-la pour charger le tableau). Vous pouvez ainsi voir ce qu'un nettoyage supprimerait, et restaurer ce qu'on vous a demandé de remettre en place. Il s'agit du même tableau que l'onglet [Trash](../trash.md), avec les filtres par utilisateur et par type, ainsi que le lien *Extended Content*.

![La section Trash de l'onglet Cleanup](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Lire l'analyse

Le premier tableau comporte une ligne par type de données :

| Colonne | |
|---|---|
| **Entity** | Le type de données : `Manuscript` (livres), `ChatMessage` (parties d'histoire), `Lorebook`, `LorebookEntry`, `AI` (fournisseurs d'inférence), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Deleted** | Le nombre d'éléments supprimés. |
| **To purge** | Le nombre d'éléments que *Run Cleanup* supprimera, y compris les données qui appartiennent à des objets supprimés. |
| **Blocked** | Éléments supprimés, mais encore utilisés : ils sont donc conservés. |

**Deleted objects still in use** liste les objets bloqués avec leur nom et **Referenced by**, c'est-à-dire ce qui les utilise encore.

<a id="what-is-removed"></a>

## Ce qui est supprimé

Le nettoyage supprime les objets supprimés que plus rien d'actif n'utilise, ainsi que les données qui n'appartiennent qu'à eux :

| Supprimé | Supprimé avec lui |
|---|---|
| un livre | toutes ses parties d'histoire, ses résumés et ses liens d'étiquettes |
| un lorebook | ses entrées et ses liens d'étiquettes ; les autres lorebooks ne l'incluent plus comme sous-lorebook |
| une partie d'histoire | la partie (ses enfants avaient déjà été déplacés vers son parent) et son résumé |
| un fournisseur d'inférence ou un protocole | retiré des valeurs par défaut des utilisateurs dans *Settings* |
| une étiquette | ses liens vers les livres et les entrées |
| un utilisateur | tout ce qui lui appartenait : livres (avec leurs parties d'histoire et leurs résumés), lorebooks, fournisseurs d'inférence, protocoles, étiquettes et paramètres |

Le nettoyage supprime aussi les [ressources](../resources.md) que plus rien n'utilise, même si personne ne les a supprimées : les fichiers ajoutés à un objet (l'audio d'un [livre audio](../extensions/audiobook.md), une image d'une partie) dont l'objet n'existe plus dans la base de données, ou qui est supprimé par ce nettoyage, lorsqu'aucun autre objet ne les mentionne non plus. Une branche créée à partir d'une partie possède les mêmes images et les mêmes clips : ceux-ci restent donc tant que la branche existe. Il en va de même pour les fichiers désignés par un objet supprimé encore présent dans la corbeille. Ils sont comptés dans **To purge** de `Resource`, et non dans *Deleted*. Un fichier qu'aucun objet n'utilise (*nothing* dans l'onglet Resources) est conservé.

<a id="what-is-kept"></a>

## Ce qui est conservé

Un objet supprimé est **blocked** (bloqué, donc conservé) tant qu'un élément actif l'utilise encore :

| Bloqué | Parce que | Pour débloquer |
|---|---|---|
| un fournisseur d'inférence ou un protocole supprimé | un livre l'utilise encore comme modèle ou protocole | choisissez-en un autre dans l'onglet *About* du livre, ou supprimez le livre |
| un lorebook supprimé (avec ses entrées) | un livre l'utilise encore | choisissez un autre lorebook pour le livre, ou videz ce champ |
| une partie d'histoire supprimée | c'est la dernière partie de la branche active d'un livre | faites passer le livre sur une autre branche, ou supprimez le livre |
| un utilisateur supprimé | des données de cet utilisateur sont encore utilisées par un autre utilisateur (un livre utilisant son fournisseur d'inférence ou son lorebook) | modifiez ou supprimez le livre de l'autre utilisateur |

Relancez *Analyze* après avoir débloqué les objets.

<a id="reference-model"></a>

## Modèle de références

**Reference Model**, en bas de page, liste toutes les références entre les types de données, avec leur règle. C'est utile pour comprendre pourquoi un élément est bloqué :

| Règle | Signification |
|---|---|
| **Blocks purge** | Un objet supprimé visé est conservé tant qu'un objet actif y fait référence. |
| **Purged with target** | L'objet qui fait référence appartient à la cible et est supprimé avec elle (les entrées avec leur lorebook). |
| **Owns target** | La cible appartient à l'objet qui fait référence (un résumé à sa partie). |
| **Cleared on purge** | La référence est retirée lorsque la cible est purgée (sous-lorebooks, modèle par défaut). |

Les extensions peuvent participer au nettoyage avec leurs propres règles.
