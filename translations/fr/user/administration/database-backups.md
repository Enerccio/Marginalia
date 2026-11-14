---
label: Sauvegardes de la base de données
order: 90
---

# Sauvegardes de la base de données

Une sauvegarde de la base de données est une copie complète de la base de données de Marginalia : tous les utilisateurs avec l'ensemble de leurs livres, de leurs livres de connaissances, de leurs réglages et de leurs données d'extensions. Utilisez-la pour protéger l'installation contre les erreurs et les pannes, et pour la transférer sur un autre ordinateur. Pour sauvegarder un seul livre, les utilisateurs disposent des [sauvegardes de livres](../books/backups.md).

![L'onglet Sauvegardes de la base de données](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Créer une sauvegarde

Cliquez sur **Créer une sauvegarde** dans l'onglet **Sauvegardes de la base de données**. La copie est réalisée pendant que Marginalia fonctionne ; elle reste cohérente même lorsque des personnes écrivent. La sauvegarde apparaît dans la liste avec son **Nom**, sa date de **Créé**, son **Type** (*Manuelle* ou *Planifiée*) et sa **Taille**. La barre d'outils affiche la taille actuelle de la base de données.

Chaque sauvegarde dispose des boutons suivants :

| Bouton | |
|---|---|
| **Télécharger** | Télécharge le fichier de sauvegarde (`.sqlite`) - conservez une copie en dehors du serveur. |
| **Restaurer au redémarrage** | Restaure la sauvegarde, voir [ci-dessous](#restoring). |
| corbeille | Supprime la sauvegarde. |

**Téléverser une sauvegarde** ajoute un fichier de sauvegarde (`.sqlite` ou `.db`) à la liste, par exemple un fichier téléchargé depuis une autre installation. Le fichier est vérifié au préalable et refusé avec le message *La base de données ne peut pas être restaurée : ...* suivi de la raison lorsqu'il ne s'agit pas d'une base SQLite, qu'il est endommagé, qu'il ne s'agit pas d'une base de données Marginalia (base d'une autre application), ou qu'il provient d'une version de Marginalia plus récente que celle en cours d'exécution.

!!!warning Les sauvegardes contiennent des secrets
Une sauvegarde de la base de données contient tout, y compris les empreintes des mots de passe de tous les utilisateurs et leurs clés d'API. Les clés d'API sont chiffrées avec le fichier `secret.key` du [dossier de données](index.md#the-data-folder), qui ne fait pas partie de la sauvegarde - mais quiconque possède à la fois la sauvegarde et `secret.key` peut les lire. Conservez les sauvegardes téléchargées avec le même niveau de protection que le serveur lui-même.
!!!

Les fichiers sont stockés dans le dossier `db-backups` du [dossier de données](index.md#the-data-folder). Les copies que Marginalia réalise elle-même - `pre-restore-...` avant une [restauration](#restoring) et `pre-migration-...` avant une [mise à niveau](#copy-before-an-upgrade) - figurent également dans la liste, affichées comme *Manuelle*, afin de ne jamais être supprimées par la rotation des sauvegardes planifiées. Les sauvegardes de livres et les fichiers JAR des extensions sont des fichiers distincts et ne font pas partie d'une sauvegarde de la base de données.

**Les fichiers téléversés (les [images des parties](../books/story-editor.md#images)) ne font pas partie d'une sauvegarde de la base de données** non plus. La base de données ne connaît ces fichiers que sous forme d'entrées de l'onglet [Ressources](../resources.md) ; les fichiers eux-mêmes se trouvent dans le dossier `data/<login>/images` du dossier de données. C'est normal : après une restauration, les entrées sont celles du moment de la sauvegarde, et les images qu'elles utilisent sont toujours présentes dans le dossier. Les fichiers des images ajoutées après la sauvegarde restent simplement en place (plus rien ne les référence) et occupent de l'espace disque. Pour protéger les images, incluez l'ensemble du dossier de données dans vos propres sauvegardes, ou intégrez-les dans une [sauvegarde de livre](../books/backups.md#pictures-in-a-backup).

Lorsque vous restaurez une sauvegarde sur **une autre installation**, copiez également `secret.key` depuis l'installation d'origine (Marginalia étant arrêté), sans quoi les clés d'API enregistrées ne pourront pas être déchiffrées et chaque utilisateur devra les saisir à nouveau.

<a id="scheduled-backups"></a>

## Sauvegardes planifiées

La section **Sauvegardes planifiées** crée automatiquement des sauvegardes :

| Champ | |
|---|---|
| **Créer des sauvegardes automatiquement** | Active les sauvegardes planifiées. |
| **Planification (cron)** | Moment de création des sauvegardes, voir [ci-dessous](#schedule-syntax). Les trois prochaines dates sont affichées sous le champ. |
| **Sauvegardes planifiées à conserver** | Nombre de sauvegardes planifiées à conserver ; les plus anciennes sont supprimées. `0` conserve tout. Les sauvegardes manuelles et téléversées ne sont jamais supprimées. |

Cliquez sur **Enregistrer la planification** pour l'appliquer. La ligne d'état indique quand la dernière sauvegarde planifiée a été réalisée et quand la suivante est prévue.

Lorsque Marginalia n'était pas en marche à une heure planifiée - cas typique de l'application de bureau, qui ne fonctionne que pendant que vous écrivez -, elle réalise la sauvegarde manquée environ 30 secondes après le prochain démarrage.

<a id="schedule-syntax"></a>

### Syntaxe de la planification

La planification est une expression cron à cinq champs, `minute hour day-of-month month day-of-week` :

| Expression | Exécution |
|---|---|
| `0 3 * * *` | tous les jours à 3 h 00 |
| `0 */6 * * *` | toutes les 6 heures |
| `30 2 * * 1` | le lundi à 2 h 30 (jour de la semaine : `0` ou `7` désigne le dimanche, `1` le lundi...) |
| `0 12 1 * *` | le 1er de chaque mois à midi |
| `@daily`, `@weekly`, `@monthly` | une fois par jour / semaine / mois à minuit (`@hourly`, `@yearly` fonctionnent aussi) |

Les heures correspondent au fuseau horaire du serveur (dans Docker, définissez-le avec la variable d'environnement `TZ`).

!!!
Pour désactiver les sauvegardes planifiées, décochez *Créer des sauvegardes automatiquement* puis cliquez sur *Enregistrer la planification*. L'expression est conservée, de sorte que vous pourrez réactiver les sauvegardes plus tard. Elle reste vérifiée - une expression invalide n'est jamais enregistrée - mais, lorsque les sauvegardes sont désactivées, le champ peut aussi être vide.
!!!

<a id="restoring"></a>

## Restaurer

Une base de données ne peut pas être remplacée pendant que Marginalia l'utilise ; la restauration a donc lieu au **prochain démarrage** :

1. Cliquez sur **Restaurer au redémarrage** à côté de la sauvegarde, puis confirmez.
   L'onglet affiche *La restauration de la base de données est planifiée. Redémarrez l'application pour l'appliquer.* - **Annuler la restauration** l'annule.
2. Redémarrez Marginalia : quittez puis relancez l'[application de bureau](../getting-started/desktop-app.md), ou exécutez `docker compose restart` sur le [serveur](../getting-started/docker-server.md).

Au démarrage, la base de données actuelle est déplacée vers `db-backups/pre-restore-<time>-marginalia.sqlite` - elle apparaît dans la liste, de sorte qu'une restauration peut être annulée en restaurant ce fichier - et la sauvegarde prend sa place. Une sauvegarde provenant d'une version plus ancienne de Marginalia est mise à niveau automatiquement.

La sauvegarde est vérifiée à nouveau avant d'être mise en place (mêmes contrôles que pour *Téléverser une sauvegarde* ; *Restaurer au redémarrage* refuse aussi une sauvegarde qui échoue à ces contrôles). Si elle échoue au contrôle, ou si sa mise à niveau échoue, Marginalia conserve (ou rétablit) la base de données actuelle et démarre normalement ; le fichier refusé est déplacé vers `db-backups/rejected-restore-<time>-marginalia.sqlite` et le journal indique la raison.

Tout ce qui a été créé après la sauvegarde est perdu : livres, parties et réglages. Les utilisateurs connectés doivent se reconnecter si leur compte a été modifié.

<a id="copy-before-an-upgrade"></a>

## Copie avant une mise à niveau

Lorsqu'une nouvelle version de Marginalia doit modifier la structure d'une base de données existante, elle enregistre d'abord une copie nommée `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, où la version est celle à laquelle se trouvait la base de données. La copie figure dans la liste des sauvegardes, de sorte qu'une mise à niveau peut être annulée en installant l'ancienne version de Marginalia et en [restaurant](#restoring) cette copie. Les trois dernières copies sont conservées. Une nouvelle installation n'a rien à copier ; et si la copie ne peut pas être réalisée (par exemple, si le disque est plein), le journal le signale et la mise à niveau se poursuit.

Une migration qui échoue en cours de route est annulée automatiquement et Marginalia ne démarre pas ; la copie sert pour une mise à niveau qui s'est terminée mais a produit un résultat que vous ne souhaitez pas.

<a id="moving-to-another-computer"></a>

## Transférer vers un autre ordinateur

1. Créez une sauvegarde puis **Télécharger**-la.
2. Installez Marginalia sur le nouvel ordinateur et créez un administrateur temporaire au premier démarrage.
3. **Téléverser une sauvegarde**, puis **Restaurer au redémarrage**, et redémarrez.
4. Connectez-vous avec les comptes de la sauvegarde.

Copiez également les dossiers `data` (sauvegardes de livres et images des parties) et `extensions` depuis l'ancien [dossier de données](index.md#the-data-folder). Ou bien, plus simplement, arrêtez Marginalia et copiez l'ensemble du dossier `.marginalia`.
