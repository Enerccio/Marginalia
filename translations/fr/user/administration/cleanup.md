---
label: Nettoyage
order: 80
---

# Nettoyage

Lorsque les utilisateurs suppriment quelque chose dans Marginalia - un livre, une partie de récit, un lorebook, un fournisseur d'inférence - l'élément disparaît
de leurs listes, mais reste dans la base de données. Cela rend la suppression sans risque et maintient le fonctionnement des références (un livre
connaît toujours le fournisseur qui a écrit ses parties). Jusqu'à ce moment, les utilisateurs peuvent restaurer leurs objets supprimés depuis l'onglet [Corbeille](../trash.md). **Nettoyage** supprime définitivement ces données supprimées, afin de garder la base de données légère.

![L'onglet Nettoyage](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Lancer un nettoyage

!!!warning
Le nettoyage ne peut pas être annulé : ce qu'il supprime disparaît aussi de la [Corbeille](../trash.md). [Créez d'abord une sauvegarde de la base de données](database-backups.md).
!!!

1. Ouvrez l'onglet **Nettoyage** de la page d'administration et cliquez sur **Analyser**. Rien n'est encore modifié.
2. Vérifiez le résultat (voir ci-dessous).
3. Cliquez sur **Lancer le nettoyage** et confirmez *n objets seront supprimés définitivement*.

Le nettoyage couvre les données de tous les utilisateurs.

Le nettoyage supprime des entrées de la base de données, et uniquement les fichiers des [agents](../agents.md) : l'espace de travail d'un agent purgé et le dossier d'une exécution purgée. Les fichiers des [ressources](../resources.md) supprimées (images des parties) restent dans le dossier de données de leur propriétaire jusqu'à ce que vous les retiriez dans l'onglet [Fichiers des ressources](resource-files.md).

<a id="seeing-what-is-deleted"></a>

## Voir ce qui est supprimé

**Corbeille** sous les tableaux liste les objets supprimés de tous les utilisateurs, une ligne par objet (ouvrez-la pour charger le tableau),
afin que vous puissiez voir ce qu'un nettoyage supprimerait et restaurer ce que quelqu'un vous a demandé de rétablir. C'est le même tableau
que l'onglet [Corbeille](../trash.md), y compris les filtres par utilisateur et par type ainsi que le lien *Contenu étendu*.

![La section Corbeille de l'onglet Nettoyage](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Lire l'analyse

Le premier tableau comporte une ligne par type de données :

| Colonne | |
|---|---|
| **Entité** | Le type de données : `Manuscript` (livres), `ChatMessage` (parties de récit), `Lorebook`, `LorebookEntry`, `AI` (fournisseurs d'inférence), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Supprimés** | Le nombre d'éléments supprimés. |
| **À purger** | Le nombre d'éléments qui seront supprimés par *Lancer le nettoyage*, y compris les données appartenant à des objets supprimés. |
| **Bloqués** | Supprimés, mais encore utilisés : ils sont donc conservés. |

**Objets supprimés encore utilisés** liste les objets bloqués avec leur nom et **Référencé par** - ce qui les utilise encore.

<a id="what-is-removed"></a>

## Ce qui est supprimé

Le nettoyage supprime les objets supprimés que plus aucun élément actif n'utilise, ainsi que les données qui n'appartiennent qu'à eux :

| Supprimé | Supprimé avec lui |
|---|---|
| un livre | toutes ses parties de récit, ses résumés et ses liens de mots-clés |
| un lorebook | ses entrées et ses liens de mots-clés ; les autres lorebooks ne l'incluent plus comme sous-lorebook |
| une partie de récit | la partie (ses enfants ont déjà été rattachés à son parent) et son résumé |
| un fournisseur d'inférence ou un protocole | retiré des valeurs par défaut des utilisateurs dans *Paramètres* |
| un mot-clé | ses liens vers les livres et les entrées |
| un utilisateur | tout ce qui lui appartenait : livres (avec leurs parties de récit et résumés), lorebooks, fournisseurs d'inférence, protocoles, mots-clés et paramètres |

<a id="what-is-kept"></a>

## Ce qui est conservé

Un objet supprimé est **bloqué** - conservé - tant qu'un élément actif l'utilise encore :

| Bloqué | Parce que | Pour le débloquer |
|---|---|---|
| un fournisseur d'inférence ou un protocole supprimé | un livre l'utilise encore comme modèle ou protocole | sélectionnez-en un autre dans l'onglet *À propos* du livre, ou supprimez le livre |
| un lorebook supprimé (avec ses entrées) | un livre l'utilise encore | sélectionnez un autre lorebook dans le livre, ou retirez-le |
| une partie de récit supprimée | c'est la dernière partie de la branche active d'un livre | basculez le livre sur une autre branche, ou supprimez le livre |
| un utilisateur supprimé | des éléments d'un autre utilisateur utilisent encore ses données (un livre utilisant son fournisseur d'inférence ou son lorebook) | modifiez ou supprimez le livre de l'autre utilisateur |

Relancez *Analyser* après avoir débloqué les objets.

<a id="reference-model"></a>

## Modèle de référence

**Modèle de référence** en bas de page liste toutes les références entre les types de données et leur politique - utile pour comprendre pourquoi quelque chose est bloqué :

| Politique | Signification |
|---|---|
| **Bloque la purge** | Un élément supprimé est conservé tant qu'un objet actif s'y réfère. |
| **Purgé avec la cible** | L'objet qui fait référence appartient à la cible et est supprimé avec elle (les entrées avec leur lorebook). |
| **Possède la cible** | La cible appartient à l'objet qui fait référence (un résumé à sa partie). |
| **Vidé à la purge** | La référence est retirée lorsque la cible est purgée (sous-lorebooks, modèle par défaut). |

Les extensions peuvent participer au nettoyage avec leurs propres règles.
