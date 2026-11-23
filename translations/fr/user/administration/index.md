---
label: Administration
order: 910
icon: tools
---

# Administration

Les administrateurs gèrent l'installation de Marginalia : comptes utilisateurs, sauvegardes de la base de données, nettoyage des données supprimées, mises à jour et extensions. Le premier compte, créé au [premier démarrage](../getting-started/first-start.md), est un administrateur ; il peut aussi faire d'autres utilisateurs administrateurs.

Les administrateurs sont aussi des utilisateurs ordinaires, avec leurs propres livres. Ils ne voient pas les livres, les lorebooks ni les réglages des autres utilisateurs, seulement les lorebooks et les protocoles que ces utilisateurs partagent avec tout le monde.

<a id="the-admin-page"></a>

## La page Administration

Les administrateurs disposent d'un bouton **Administration** en bas de l'espace de travail. Dans l'[affichage téléphone](../phone.md#administration) (une fenêtre de moins de 768 pixels de large), il s'agit de l'onglet **Administration** du tiroir. Il ouvre la page d'administration, avec les onglets suivants :

| Onglet | |
|---|---|
| **Utilisateurs** | Création, modification et suppression des comptes, et attribution des [droits](permissions.md) aux utilisateurs. Voir [Utilisateurs](users.md). |
| **Sauvegardes de la base de données** | Sauvegardes de l'ensemble de la base de données, à la demande ou selon un calendrier, et restauration. Voir [Sauvegardes de la base de données](database-backups.md). |
| **Nettoyage** | Suppression définitive des données supprimées. Voir [Nettoyage](cleanup.md). |
| **Fichiers de ressources** | Recherche des fichiers de ressources que plus rien n'utilise, téléchargement sous forme de ZIP et suppression du disque. Voir [Fichiers de ressources](resource-files.md). |
| **Assistant d'aide** | Le chat qui répond aux questions à partir du manuel : le modèle, les prompts et qui le paie. Voir [Assistant d'aide](help-assistant.md). |
| **Agents** | Le prompt système et le prompt de compaction que suivent les agents lorsque ni eux ni leur utilisateur n'en ont un propre. Vide signifie le prompt intégré ; l'icône d'information propose **Insérer le modèle par défaut**, qui le remplit pour que vous puissiez modifier une copie. Ce que les utilisateurs peuvent faire avec les agents relève de leur [droit](permissions.md). Également **Les agents peuvent envoyer des messages aux webhooks des utilisateurs** : le serveur appelle l'adresse qu'un utilisateur a saisie dans ses réglages lorsqu'un agent a besoin de lui et que l'application est fermée ([Quand vous n'êtes pas là](../agents.md#when-you-are-not-there)) ; désactivez cette option là où ce n'est pas acceptable. |
| **Statistiques** | Si les [statistiques d'écriture](../books/statistics.md) sont enregistrées, pendant combien de temps elles sont conservées, et l'effacement des statistiques d'un utilisateur ou de tous. Voir [Statistiques](statistics.md). |
| **Tâches** | Le journal de tout ce qui a été demandé à une IA externe (qui, quand, quels appels, et comment ils se sont terminés), un filtre qui supprime à la main les anciennes entrées, et la page `/jobs` avec les tâches en cours. Voir [Tâches](jobs.md). |
| **GPU** | Une carte graphique partagée entre le modèle de langage et les serveurs d'images et de voix : les GPU, les verrous GPU (comment chaque modèle est chargé et la carte libérée), les étapes qu'ils exécutent, ce que chaque GPU contient actuellement, et **Décharger maintenant**. Voir [GPU](gpus.md). |
| **Mettre à jour** | Sur un serveur Docker : la version en cours, la version la plus récente de la branche suivie par le serveur, et le bouton qui met à jour et redémarre Marginalia. Voir [Mises à jour](updates.md). |
| **Extensions** | Installation et suppression des extensions. Voir [Gestion des extensions](extensions.md). |

![L'onglet Agents de la page Administration](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## Le dossier de données

Tout ce que Marginalia stocke se trouve dans le dossier `.marginalia` du répertoire personnel de l'utilisateur qui le lance (`./data` sur l'hôte, avec l'installation [Docker](../getting-started/docker-server.md)) :

| Chemin | |
|---|---|
| `marginalia.sqlite` (avec `-wal`, `-shm`) | La base de données : utilisateurs, livres, lorebooks, réglages, tout. |
| `secret.key` | Clé qui chiffre les clés d'API dans la base de données. Créée au premier démarrage, lisible par son seul propriétaire (sous Linux et macOS) ; sans elle, les clés d'API enregistrées ne peuvent pas être lues. Si d'autres utilisateurs de l'ordinateur peuvent la lire, le journal le signale : `chmod 600 secret.key`. |
| `db-backups/` | [Sauvegardes de la base de données](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Sauvegardes des livres](../books/backups.md) de chaque utilisateur. |
| `extensions/` | Fichiers JAR des [extensions](extensions.md) installées. |
| `update/` | Les fichiers par lesquels Marginalia et le [surveillant de mise à jour](updates.md#setting-up-the-watcher) d'un serveur Docker communiquent : l'état, le journal de la dernière mise à jour, la demande. |
| `agent-workspaces/<agent id>/` | Les fichiers d'un [agent](../agents.md#the-working-copy) (ses notes et ses rapports, qu'il conserve d'une exécution à l'autre). |
| `agent-runs/<run id>/` | Ce que conserve une exécution d'agent : le début du livre (pour afficher les modifications, supprimé avec la copie de travail), les points de contrôle et les constats (supprimés lorsque le nettoyage purge l'exécution). |
| `desktop/logs/` | Journaux de l'[application de bureau](../getting-started/desktop-app.md). |

Pour sauvegarder une installation complète, arrêtez Marginalia et copiez ce dossier (y compris `secret.key`). Pendant qu'il tourne, utilisez les [sauvegardes de la base de données](database-backups.md) pour la base ; les sauvegardes des livres et les extensions sont des fichiers simples et peuvent être copiés à tout moment.
