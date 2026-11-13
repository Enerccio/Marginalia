---
label: Sauvegardes de la base de données
order: 90
---

# Sauvegardes de la base de données

Une sauvegarde de la base de données est une copie complète de la base de Marginalia : tous les utilisateurs avec l'ensemble de leurs livres, de leurs livres de références, de leurs paramètres et des données de leurs extensions. Elle sert à protéger l'installation contre les erreurs et les pannes, et à la transférer sur un autre ordinateur. Pour sauvegarder un seul livre, les utilisateurs disposent des [sauvegardes de livres](../books/backups.md).

![L'onglet Sauvegardes de la base de données](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Créer une sauvegarde

Cliquez sur **Créer une sauvegarde** dans l'onglet **Sauvegardes de la base de données**. La copie est réalisée pendant que Marginalia fonctionne, et reste cohérente même lorsque des personnes écrivent. Elle apparaît dans la liste avec son **Nom**, sa date **Créé**, son **Type** (*Manuelle* ou *Planifiée*) et sa **Taille**. La barre d'outils affiche la taille actuelle de la base de données.

Chaque sauvegarde dispose des éléments suivants :

| Bouton | |
|---|---|
| **Télécharger** | Télécharge le fichier de sauvegarde (`.sqlite`) : conservez une copie en dehors du serveur. |
| **Restaurer au redémarrage** | Restaure la sauvegarde, voir [ci-dessous](#restoring). |
| corbeille | Supprime la sauvegarde. |

**Téléverser une sauvegarde** ajoute à la liste un fichier de sauvegarde (`.sqlite` ou `.db`), par exemple un fichier téléchargé depuis une autre installation. Le fichier est d'abord vérifié et refusé avec le message *The database can't be restored: ...* suivi de la raison lorsqu'il ne s'agit pas d'une base SQLite, qu'il est endommagé, qu'il ne s'agit pas d'une base Marginalia (base d'une autre application) ou qu'il provient d'une version de Marginalia plus récente que celle en cours d'exécution.

!!!warning Les sauvegardes contiennent des secrets
Une sauvegarde de la base de données contient tout, y compris les empreintes des mots de passe de tous les utilisateurs et leurs clés d'API. Les clés d'API sont chiffrées avec le fichier `secret.key` du [dossier de données](index.md#the-data-folder), qui ne fait pas partie de la sauvegarde ; mais quiconque possède à la fois la sauvegarde et `secret.key` peut les lire. Conservez les sauvegardes téléchargées avec le même soin que le serveur lui-même.
!!!

Les fichiers sont stockés dans le dossier `db-backups` du [dossier de données](index.md#the-data-folder). Les copies que Marginalia réalise elle-même — `pre-restore-...` avant une [restauration](#restoring) et `pre-migration-...` avant une [mise à niveau](#copy-before-an-upgrade) — figurent également dans la liste, affichées comme *Manuelle*, de sorte qu'elles ne sont jamais supprimées par la rotation des sauvegardes planifiées. Les sauvegardes de livres et les JAR des extensions sont des fichiers distincts et ne font pas partie d'une sauvegarde de la base de données.

**Les fichiers téléversés (les [images des parties](../books/story-editor.md#images)) ne font pas partie d'une sauvegarde de la base de données** non plus. La base ne connaît ces fichiers que par les entrées de l'onglet [Ressources](../resources.md) ; les fichiers se trouvent dans le dossier `data/<login>/images` du dossier de données. C'est normal : après une restauration, les entrées sont celles du moment de la sauvegarde, et les images qu'elles utilisent sont toujours présentes dans le dossier. Les fichiers des images ajoutées après la sauvegarde restent simplement sur le disque (plus rien ne les référence) et occupent de l'espace. Pour conserver vos images en sécurité, incluez l'ensemble du dossier de données dans vos propres sauvegardes, ou intégrez-les dans une [sauvegarde de livre](../books/backups.md#pictures-in-a-backup).

Lorsque vous restaurez une sauvegarde sur **une autre installation**, copiez également `secret.key` depuis l'installation d'origine (Marginalia étant arrêté), sans quoi les clés d'API enregistrées ne pourront pas être déchiffrées et chaque utilisateur devra les saisir à nouveau.

<a id="scheduled-backups"></a>

## Sauvegardes planifiées

La section **Sauvegardes planifiées** crée des sauvegardes automatiquement :

| Champ | |
|---|---|
| **Créer des sauvegardes automatiquement** | Active les sauvegardes planifiées. |
| **Planification (cron)** | Moment de création des sauvegardes, voir [ci-dessous](#schedule-syntax). Les trois prochaines échéances s'affichent sous le champ. |
| **Sauvegardes planifiées à conserver** | Nombre de sauvegardes planifiées à conserver ; les plus anciennes sont supprimées. `0` conserve toutes les sauvegardes. Les sauvegardes manuelles et téléversées ne sont jamais supprimées. |

Cliquez sur **Enregistrer la planification** pour l'appliquer. La ligne d'état indique quand la dernière sauvegarde planifiée a été réalisée et quand la suivante est prévue.

Lorsque Marginalia ne fonctionnait pas à une heure planifiée — situation typique de l'application de bureau, qui ne tourne que pendant que vous écrivez —, il réalise la sauvegarde manquée environ 30 secondes après le prochain démarrage.

<a id="schedule-syntax"></a>

### Syntaxe de la planification

La planification est une expression cron à cinq champs, `minute heure jour-du-mois mois jour-de-la-semaine` :

| Expression | Exécution |
|---|---|
| `0 3 * * *` | tous les jours à 3 h 00 |
| `0 */6 * * *` | toutes les 6 heures |
| `30 2 * * 1` | les lundis à 2 h 30 (jour de la semaine : `0` ou `7` pour dimanche, `1` pour lundi...) |
| `0 12 1 * *` | le 1er de chaque mois à midi |
| `@daily`, `@weekly`, `@monthly` | une fois par jour / semaine / mois à minuit (`@hourly` et `@yearly` fonctionnent également) |

Les heures sont exprimées dans le fuseau horaire du serveur (avec Docker, définissez-le à l'aide de la variable d'environnement `TZ`).

!!!
Pour désactiver les sauvegardes planifiées, décochez *Créer des sauvegardes automatiquement* puis cliquez sur *Enregistrer la planification*. L'expression est conservée, vous pourrez donc réactiver les sauvegardes plus tard. Elle reste vérifiée : une expression invalide n'est jamais enregistrée, mais tant que les sauvegardes sont désactivées, le champ peut aussi être vide.
!!!

<a id="restoring"></a>

## Restaurer

Une base de données ne peut pas être remplacée pendant que Marginalia l'utilise ; une restauration a donc lieu au **prochain démarrage** :

1. Cliquez sur **Restaurer au redémarrage** à côté de la sauvegarde, puis confirmez.
   L'onglet affiche *Database restore is scheduled. Restart the application to apply it.* — **Annuler la restauration** l'annule.
2. Redémarrez Marginalia : quittez puis relancez l'[application de bureau](../getting-started/desktop-app.md), ou exécutez `docker compose restart` sur le [serveur](../getting-started/docker-server.md).

Au démarrage, la base actuelle est déplacée vers `db-backups/pre-restore-<time>-marginalia.sqlite` — elle apparaît dans la liste, de sorte qu'une restauration peut être annulée en restaurant ce fichier — et la sauvegarde prend sa place. Une sauvegarde provenant d'une version plus ancienne de Marginalia est mise à niveau automatiquement.

La sauvegarde est de nouveau vérifiée avant d'être mise en place (mêmes contrôles que pour *Téléverser une sauvegarde* ; *Restaurer au redémarrage* refuse aussi une sauvegarde qui échoue à ces contrôles). Si elle échoue à la vérification, ou si sa mise à niveau échoue, Marginalia conserve (ou rétablit) la base actuelle et démarre normalement ; le fichier refusé est déplacé vers `db-backups/rejected-restore-<time>-marginalia.sqlite` et le journal indique la raison.

Tout ce qui a été créé après la sauvegarde est perdu : livres, parties et paramètres. Les utilisateurs connectés devront se reconnecter si leur compte a changé.

<a id="copy-before-an-upgrade"></a>

## Copie avant une mise à niveau

Lorsqu'une nouvelle version de Marginalia doit modifier la structure d'une base existante, elle enregistre d'abord une copie sous la forme `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, où la version est celle de la base avant la mise à niveau. Cette copie figure dans la liste des sauvegardes ; une mise à niveau peut donc être annulée en installant l'ancienne version de Marginalia et en [restaurant](#restoring) cette copie. Les trois dernières copies sont conservées. Une nouvelle installation n'a rien à copier ; si la copie ne peut pas être réalisée (par exemple parce que le disque est plein), le journal le signale et la mise à niveau se poursuit.

Une migration qui échoue en cours de route est annulée automatiquement et Marginalia ne démarre pas ; la copie sert pour une mise à niveau qui s'est déroulée sans erreur mais a produit un résultat que vous ne souhaitez pas.

<a id="moving-to-another-computer"></a>

## Transférer vers un autre ordinateur

1. Créez une sauvegarde et **Télécharger**-la.
2. Installez Marginalia sur le nouvel ordinateur, puis créez un administrateur temporaire au premier démarrage.
3. **Téléverser une sauvegarde**, **Restaurer au redémarrage**, redémarrez.
4. Connectez-vous avec les comptes de la sauvegarde.

Copiez également les dossiers `data` (sauvegardes de livres et images des parties) et `extensions` depuis l'ancien [dossier de données](index.md#the-data-folder). Ou, plus simplement, arrêtez Marginalia et copiez l'intégralité du dossier `.marginalia`.
