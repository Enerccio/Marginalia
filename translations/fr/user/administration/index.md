---
label: Administration
order: 910
icon: tools
---

# Administration

Les administrateurs gèrent l'installation de Marginalia : comptes utilisateurs, sauvegardes de la base de données, nettoyage des données supprimées,
mises à jour et extensions. Le premier compte, créé lors du [premier démarrage](../getting-started/first-start.md), est un administrateur ;
il peut aussi faire d'autres utilisateurs des administrateurs.

Les administrateurs sont aussi des utilisateurs ordinaires avec leurs propres livres : ils ne voient pas les livres, lorebooks ni réglages des autres utilisateurs, seulement les lorebooks et protocoles que ces utilisateurs partagent avec tout le monde.

<a id="the-admin-page"></a>

## La page Admin

Les administrateurs disposent d'un bouton **Admin** en bas de l'espace de travail. Dans la [disposition téléphone](../phone.md#administration) (une fenêtre plus étroite que 768 pixels), c'est l'onglet **Admin** du tiroir. Il ouvre la page d'administration avec ces onglets :

| Onglet | |
|---|---|
| **Utilisateurs** | Créer, modifier et supprimer des comptes, et accorder des [permissions](permissions.md) aux utilisateurs. Voir [Utilisateurs](users.md). |
| **Sauvegardes de la base de données** | Sauvegardes de la base entière, à la demande ou selon un planning, et leur restauration. Voir [Sauvegardes de la base de données](database-backups.md). |
| **Nettoyage** | Suppression définitive des données supprimées. Voir [Nettoyage](cleanup.md). |
| **Fichiers de ressources** | Repérer les fichiers des ressources que plus rien n'utilise, les télécharger dans une archive ZIP et les supprimer du disque. Voir [Fichiers de ressources](resource-files.md). |
| **Assistant d'aide** | Le chat qui répond aux questions à partir du manuel : le modèle, les prompts et qui le paie. Voir [Assistant d'aide](help-assistant.md). |
| **Agents** | Le prompt système et le prompt de compaction que suivent les agents lorsque ni eux ni leur utilisateur n'en ont un propre. Vide, c'est le prompt intégré ; l'icône d'information propose **Insérer le modèle par défaut**, qui le remplit pour que vous puissiez modifier une copie. Ce que les utilisateurs peuvent faire avec les agents relève de leur [permission](permissions.md). Également **Les agents peuvent envoyer des messages aux webhooks des utilisateurs** : le serveur appelle l'adresse saisie par un utilisateur dans ses réglages lorsqu'un agent a besoin de lui et qu'il a fermé l'application ([Quand vous n'êtes pas là](../agents.md#when-you-are-not-there)) ; désactivez-la là où cela n'est pas acceptable. |
| **Statistiques** | Savoir si les [statistiques d'écriture](../books/statistics.md) sont enregistrées, combien de temps elles sont conservées, et effacer les statistiques d'un utilisateur ou de tous. Voir [Statistiques](statistics.md). |
| **Tâches** | Le journal de tout ce qui a été demandé à une IA externe (qui, quand, quels appels, et comment ils se sont terminés), un filtre qui supprime à la main les anciennes entrées, et la page `/jobs` avec les tâches en cours. Voir [Tâches](jobs.md). |
| **Mettre à jour** | Sur un serveur Docker : la version en cours, la version la plus récente de la branche suivie par le serveur, et le bouton qui met à jour et redémarre Marginalia. Voir [Mises à jour](updates.md). |
| **Extensions** | Installer et supprimer des extensions. Voir [Gérer les extensions](extensions.md). |

![L'onglet Agents de la page Admin](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## Le dossier de données

Tout ce que Marginalia stocke se trouve dans le dossier `.marginalia` du répertoire personnel de l'utilisateur qui le lance (`./data`
sur l'hôte avec l'[installation Docker](../getting-started/docker-server.md)) :

| Chemin | |
|---|---|
| `marginalia.sqlite` (avec `-wal`, `-shm`) | La base de données : utilisateurs, livres, lorebooks, réglages, tout. |
| `secret.key` | Clé qui chiffre les clés d'API dans la base de données. Créée au premier démarrage, lisible par son seul propriétaire (sous Linux et macOS) ; sans elle, les clés d'API enregistrées ne peuvent pas être lues. Si d'autres utilisateurs de l'ordinateur peuvent la lire, le journal le signale : `chmod 600 secret.key`. |
| `db-backups/` | [Sauvegardes de la base de données](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Sauvegardes des livres](../books/backups.md) de chaque utilisateur. |
| `extensions/` | Les JAR des [extensions](extensions.md) installées. |
| `update/` | Les fichiers par lesquels Marginalia et le [surveillant de mises à jour](updates.md#setting-up-the-watcher) d'un serveur Docker communiquent : le statut, le journal de la dernière mise à jour, la demande. |
| `agent-workspaces/<agent id>/` | Les fichiers d'un [agent](../agents.md#the-working-copy) (ses notes et rapports, qu'il conserve d'une exécution à l'autre). |
| `agent-runs/<run id>/` | Ce que garde une exécution d'un agent : le début du livre (pour vous montrer les modifications, supprimé avec la copie de travail), les points de contrôle et les constats (supprimés lorsque l'exécution est purgée par le nettoyage). |
| `desktop/logs/` | Les journaux de l'[application de bureau](../getting-started/desktop-app.md). |

Pour sauvegarder une installation complète, arrêtez Marginalia et copiez ce dossier (y compris `secret.key`). Pendant qu'il tourne, utilisez
les [sauvegardes de la base de données](database-backups.md) pour la base ; les sauvegardes des livres et les extensions sont de simples fichiers et peuvent être
copiés à tout moment.
