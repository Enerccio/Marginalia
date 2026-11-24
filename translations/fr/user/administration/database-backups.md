---
label: Sauvegardes de la base de données
order: 90
---

# Sauvegardes de la base de données

Une sauvegarde de la base de données est une copie complète de la base Marginalia : tous les utilisateurs avec l'ensemble de leurs livres, de leurs livres de référence (lorebooks), de leurs paramètres et des données de leurs extensions. Utilisez-la pour protéger l'installation contre les erreurs et les pannes, et pour la transférer sur un autre ordinateur. Pour sauvegarder un seul livre, les utilisateurs disposent des [sauvegardes de livre](../books/backups.md).

![L'onglet Database Backups](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Créer une sauvegarde

Cliquez sur **Create Backup** dans l'onglet **Database Backups**. La copie est réalisée pendant que Marginalia fonctionne et reste cohérente même lorsque des personnes écrivent. Elle apparaît dans la liste avec son **Name**, sa date **Created**, son **Type** (*Manual* ou *Scheduled*) et sa **Size**. La barre d'outils affiche la taille actuelle de la base de données.

Chaque sauvegarde dispose de :

| Bouton | |
|---|---|
| **Download** | Télécharge le fichier de sauvegarde (`.sqlite`) - conservez une copie en dehors du serveur. |
| **Restore on restart** | Restaure la sauvegarde, voir [ci-dessous](#restoring). |
| corbeille | Supprime la sauvegarde. |

**Upload Backup** ajoute un fichier de sauvegarde (`.sqlite` ou `.db`) à la liste, par exemple un fichier téléchargé depuis une autre installation. Le fichier est vérifié au préalable et refusé avec *The database can't be restored: ...* suivi de la raison lorsqu'il ne s'agit pas d'une base SQLite, qu'il est endommagé, qu'il ne s'agit pas d'une base Marginalia (base d'une autre application), ou qu'il provient d'une version de Marginalia plus récente que celle en cours d'exécution.

!!!warning Les sauvegardes contiennent des secrets
Une sauvegarde de la base contient tout, y compris les empreintes de mots de passe de tous les utilisateurs et leurs clés API. Les clés API sont chiffrées avec le fichier `secret.key` du [dossier de données](index.md#the-data-folder), qui ne fait pas partie de la sauvegarde, mais quiconque possède à la fois la sauvegarde et `secret.key` peut les lire. Conservez les sauvegardes téléchargées avec le même soin que le serveur lui-même.
!!!

Les fichiers sont stockés dans le dossier `db-backups` du [dossier de données](index.md#the-data-folder). Les copies que Marginalia crée lui-même, à savoir `pre-restore-...` avant une [restauration](#restoring) et `pre-migration-...` avant une [mise à niveau](#copy-before-an-upgrade), figurent également dans la liste, affichées comme *Manual*, de sorte qu'elles ne sont jamais supprimées par la rotation des sauvegardes planifiées. Les sauvegardes de livre et les JAR des extensions sont des fichiers séparés et ne font pas partie d'une sauvegarde de la base de données.

**Les fichiers téléversés (les [images des parties](../books/story-editor.md#images)) ne font pas partie d'une sauvegarde de la base de données** non plus. La base ne connaît ces fichiers que par les entrées de l'onglet [Resources](../resources.md) ; les fichiers se trouvent dans le dossier `data/<login>/images` du dossier de données. C'est normal : après une restauration, les entrées sont celles de la date de la sauvegarde, et les images qu'elles utilisent sont toujours présentes dans le dossier. Les fichiers des images ajoutées après la sauvegarde restent simplement sans référence (plus rien ne pointe vers eux) et occupent de la place sur le disque. Pour conserver les images en sécurité, incluez l'ensemble du dossier de données dans vos propres sauvegardes, ou intégrez-les dans une [sauvegarde de livre](../books/backups.md#pictures-in-a-backup).

Lorsque vous restaurez une sauvegarde sur **une autre installation**, copiez également `secret.key` depuis l'installation d'origine (Marginalia étant arrêté), sans quoi les clés API enregistrées ne pourront pas être déchiffrées et chaque utilisateur devra les saisir à nouveau.

<a id="scheduled-backups"></a>

## Sauvegardes planifiées

La section **Scheduled Backups** crée les sauvegardes automatiquement :

| Champ | |
|---|---|
| **Create backups automatically** | Active les sauvegardes planifiées. |
| **Schedule (cron)** | Quand les créer, voir [ci-dessous](#schedule-syntax). Les trois prochaines dates sont affichées sous le champ. |
| **Keep scheduled backups** | Le nombre de sauvegardes planifiées à conserver ; les plus anciennes sont supprimées. `0` conserve tout. Les sauvegardes manuelles et téléversées ne sont jamais supprimées. |

Cliquez sur **Save Schedule** pour l'appliquer. La ligne d'état indique quand la dernière sauvegarde planifiée a été faite et quand la suivante est prévue.

Lorsque Marginalia ne fonctionnait pas à une heure planifiée (cas typique de l'application de bureau, qui ne tourne que pendant que vous écrivez), il effectue la sauvegarde manquée environ 30 secondes après le prochain démarrage. Désactiver la planification ou la modifier pendant ces 30 secondes annule cette sauvegarde.

<a id="schedule-syntax"></a>

### Syntaxe de la planification

La planification est une expression cron à cinq champs, `minute hour day-of-month month day-of-week` :

| Expression | Exécution |
|---|---|
| `0 3 * * *` | tous les jours à 3 h 00 |
| `0 */6 * * *` | toutes les 6 heures |
| `30 2 * * 1` | les lundis à 2 h 30 (jour de la semaine : `0` ou `7` désigne le dimanche, `1` le lundi...) |
| `0 12 1 * *` | le 1er de chaque mois à midi |
| `@daily`, `@weekly`, `@monthly` | une fois par jour / semaine / mois à minuit (`@hourly`, `@yearly` fonctionnent aussi) |

Les heures sont exprimées dans le fuseau horaire du serveur (avec Docker, réglez-le avec la variable d'environnement `TZ`).

!!!
Pour désactiver les sauvegardes planifiées, décochez *Create backups automatically* puis cliquez sur *Save Schedule*. L'expression est conservée, ce qui vous permet de réactiver les sauvegardes plus tard. Elle est toujours vérifiée (une expression invalide n'est jamais enregistrée), mais tant que les sauvegardes sont désactivées, le champ peut aussi rester vide.
!!!

<a id="restoring"></a>

## Restaurer

Une base de données ne peut pas être remplacée pendant que Marginalia l'utilise ; la restauration a donc lieu au **prochain démarrage** :

1. Cliquez sur **Restore on restart** à côté de la sauvegarde, puis confirmez.
   L'onglet affiche *Database restore is scheduled. Restart the application to apply it.* - **Cancel Restore** annule l'opération.
2. Redémarrez Marginalia : quittez puis relancez l'[application de bureau](../getting-started/desktop-app.md), ou `docker compose restart` sur le [serveur](../getting-started/docker-server.md).

Au démarrage, la base actuelle est déplacée vers `db-backups/pre-restore-<time>-marginalia.sqlite` (elle apparaît dans la liste, de sorte qu'une restauration peut être annulée en restaurant ce fichier) et la sauvegarde prend sa place. Une sauvegarde issue d'une version plus ancienne de Marginalia est mise à niveau automatiquement.

La sauvegarde est vérifiée à nouveau avant d'être mise en place (mêmes contrôles que pour *Upload Backup* ; *Restore on restart* refuse également une sauvegarde qui échoue à ces contrôles). Si elle échoue à la vérification, ou si sa mise à niveau échoue, Marginalia conserve (ou rétablit) la base actuelle et démarre normalement ; le fichier refusé est déplacé vers `db-backups/rejected-restore-<time>-marginalia.sqlite` et le journal indique la raison.

Tout ce qui a été créé après la sauvegarde est perdu : livres, parties et paramètres. Les utilisateurs connectés doivent se reconnecter si leur compte a changé.

<a id="copy-before-an-upgrade"></a>

## Copie avant une mise à niveau

Lorsqu'une nouvelle version de Marginalia doit modifier la structure d'une base existante, elle enregistre d'abord une copie nommée `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, où la version est celle de la base à ce moment-là. Cette copie figure dans la liste des sauvegardes, de sorte qu'une mise à niveau peut être annulée en installant l'ancienne version de Marginalia et en [restaurant](#restoring) cette copie. Les trois dernières copies sont conservées. Une nouvelle installation n'a rien à copier ; et si la copie ne peut pas être faite (par exemple si le disque est plein), le journal le signale et la mise à niveau se poursuit.

Une migration qui échoue en cours de route est annulée automatiquement et Marginalia ne démarre pas ; la copie sert pour une mise à niveau qui s'est déroulée jusqu'au bout mais a produit un résultat indésirable.

<a id="moving-to-another-computer"></a>

## Déménager vers un autre ordinateur

1. Créez une sauvegarde et cliquez sur **Download**.
2. Installez Marginalia sur le nouvel ordinateur, créez un administrateur temporaire au premier démarrage.
3. **Upload Backup**, **Restore on restart**, redémarrez.
4. Connectez-vous avec les comptes de la sauvegarde.

Copiez également le dossier `data` (sauvegardes de livre et images des parties) et le dossier `extensions` depuis l'ancien [dossier de données](index.md#the-data-folder). Ou arrêtez simplement Marginalia et copiez l'ensemble du dossier `.marginalia`.
