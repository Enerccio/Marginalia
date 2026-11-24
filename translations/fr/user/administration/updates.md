---
label: Mises à jour
order: 65
---

# Mises à jour

Sur un [serveur Docker](../getting-started/docker-server.md) construit à partir d'une copie Git (checkout), un administrateur peut mettre à jour Marginalia depuis l'onglet **Update** de la page Admin, sans se connecter au serveur : le serveur récupère la version la plus récente de la branche sur laquelle se trouve sa copie, construit l'application et les [plugins inclus](../getting-started/docker-server.md#plugins), puis redémarre.

Cela nécessite un petit programme sur le serveur, le **update watcher**, qui est [installé une seule fois](#setting-up-the-watcher). Sans lui, l'onglet affiche seulement la version en cours et indique que le watcher ne fonctionne pas. C'est aussi ce qu'affichent l'[application de bureau](../getting-started/desktop-app.md) et un serveur utilisant l'[image précompilée](../getting-started/docker-server.md#prebuilt-image) : ils se mettent à jour en installant la nouvelle version.

<a id="the-update-tab"></a>

## L'onglet Update

| | |
|---|---|
| **Running version** | La version et le commit de Marginalia en cours d'exécution. |
| **Branch** | La branche sur laquelle se trouve la copie sur le serveur et la branche distante qu'elle suit (par exemple `dev`, qui suit `origin/dev`), le commit le plus récent de cette branche distante et le moment où le serveur a vérifié pour la dernière fois. Le serveur vérifie toutes les 15 minutes. |
| Statut | *A new version is available* lorsque le commit le plus récent n'est pas celui en cours d'exécution, *Marginalia is up to date* sinon. Pendant une mise à jour, l'onglet l'indique. |
| **Last update** | Quand la dernière mise à jour lancée depuis cet écran s'est terminée, et si elle a réussi ou échoué. |

Le serveur suit la branche extraite (checkout) à cet endroit, quelle qu'elle soit : pour faire passer une installation sur une autre branche, basculez-la avec `git switch` dans la copie sur le serveur ; l'onglet affiche la nouvelle branche après la prochaine vérification.

| Bouton | |
|---|---|
| **Update and restart** | Demande au serveur de se mettre à jour, après une confirmation. Il construit et redémarre aussi lorsqu'il n'y a rien de nouveau (à utiliser après une modification de `docker-compose.yml`, par exemple). |
| **Refresh** | Relit le statut. L'onglet ne se met pas à jour tout seul. |
| **Log** | La sortie de la dernière mise à jour : ce qu'ont écrit Git et la construction. |

On peut faire la même chose depuis un script, avec une [clé d'API](../account.md#api-keys) disposant du droit *Update the installation* : lire le statut, demander une mise à jour et lire le journal (voir le [manuel du développeur](../../developer/api.md)).

<a id="what-an-update-does"></a>

## Ce que fait une mise à jour

1. La nouvelle version est construite : `git pull`, les plugins, l'image. Cela prend plusieurs minutes, et **Marginalia reste en fonctionnement** pendant ce temps. L'onglet affiche *The update is running* ; **Log** indique où en est le processus.
2. Marginalia est arrêté, les nouveaux plugins sont copiés dans `data/extensions` (en remplaçant ceux déjà installés), et la nouvelle version démarre. Cela prend une à deux minutes (voir [Santé](../getting-started/docker-server.md#health)). Tous les utilisateurs en train de travailler sont interrompus, et ce qui est en cours de génération est arrêté : la confirmation indique combien de [tâches](jobs.md) tournent à ce moment-là, alors choisissez un moment calme.
3. La base de données est mise à niveau au démarrage de la nouvelle version, après qu'une [copie de celle-ci](database-backups.md#copy-before-an-upgrade) a été sauvegardée. Faites aussi vous-même une [sauvegarde de la base de données](database-backups.md) au préalable.

Si la construction échoue, rien n'est arrêté : la version précédente continue de tourner, **Last update** indique *failed* et le journal dit pourquoi. La mise à jour suivante reconstruit, même s'il n'y a rien de nouveau à récupérer.

<a id="setting-up-the-watcher"></a>

## Installer le watcher

Marginalia tourne dans un conteneur et ne peut atteindre ni la copie Git ni Docker ; lui donner cet accès ferait de toute personne qui accède à l'application un administrateur du serveur. Il se contente donc de demander : il écrit un fichier dans son [dossier de données](index.md#the-data-folder), et le watcher, qui tourne sur le serveur en dehors du conteneur, fait le travail. Le watcher est le script `tools/update-watcher/watch.sh` de la copie ; il n'exécute jamais que `rebuild.sh` de cette même copie (voir [Mises à jour automatiques](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux avec systemd

Dans la copie sur le serveur (le dossier contenant `docker-compose.yml`) :

```sh
sudo tools/update-watcher/install.sh
```

Il installe et démarre trois unités :

| Unité | |
|---|---|
| `marginalia-update.service` | Lance le watcher une seule fois. |
| `marginalia-update.path` | Le démarre au moment où l'application demande une mise à jour. |
| `marginalia-update.timer` | Le démarre deux minutes après le démarrage du système, puis toutes les 15 minutes, pour chercher de nouveaux commits. |

Ouvrez **Admin → Update** et cliquez sur **Refresh** : l'onglet affiche la branche et le commit le plus récent. C'est tout.

| Option | |
|---|---|
| `--name NAME` | Le nom des unités (par défaut `marginalia-update`). Deux copies sur un même ordinateur (une installation de test et une de production) ont chacune besoin de leur propre nom. |
| `--user USER` | Lance le watcher avec cet utilisateur plutôt qu'avec root, voir [ci-dessous](#who-the-watcher-runs-as). |
| `--uninstall` | Arrête et supprime les unités (avec le même `--name`). |

`systemctl status marginalia-update.service` affiche la dernière exécution et `journalctl -u marginalia-update.service` sa sortie (le même texte que **Log**, ainsi que la raison lorsque le watcher lui-même n'a pas pu fonctionner). Les unités contiennent le chemin de la copie : après avoir déplacé la copie, relancez `install.sh`.

<a id="without-systemd-cron"></a>

### Sans systemd (cron)

Lancez le watcher chaque minute ; il ne fait rien lorsque personne n'a demandé de mise à jour et qu'il a interrogé le dépôt distant il y a moins de 15 minutes, donc cela ne coûte rien :

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Ajoutez cette ligne dans la crontab de root (`sudo crontab -e`), ou dans celle d'un autre utilisateur, voir ci-dessous. Une mise à jour démarre alors dans la minute qui suit le clic.

<a id="settings"></a>

### Réglages

Le watcher lit ces variables d'environnement (avec systemd : les lignes `sudo systemctl edit marginalia-update.service` et `Environment=NAME=value` sous `[Service]`) :

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | Le dossier de données sur le serveur, lorsqu'il ne s'agit pas de `data` dans la copie (la partie gauche du volume dans `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Les arguments de `rebuild.sh`, `--force` par défaut. Sur un serveur qui n'a pas assez de mémoire pour l'application et la construction ensemble, utilisez `--force --stop-first` : Marginalia est alors arrêté avant la construction et reste indisponible pendant toute sa durée. |
| `MARGINALIA_CHECK_MINUTES` | L'âge maximal du statut avant qu'une exécution sans demande n'interroge à nouveau le dépôt distant, 15 par défaut. Avec systemd, c'est le minuteur qui détermine la fréquence d'exécution du watcher. |

<a id="who-the-watcher-runs-as"></a>

### Sous quel utilisateur tourne le watcher

Par défaut, le watcher tourne en tant que **root** : il doit utiliser Docker et écrire dans le dossier de données, qui appartient à l'utilisateur du conteneur. Git lui-même est exécuté en tant que propriétaire de la copie, de sorte que les fichiers de Git restent à ce propriétaire.

Le watcher exécute ce que contient la branche (`rebuild.sh`, le `Dockerfile`), comme le fait toute mise à jour manuelle : ne suivez qu'un dépôt en qui vous avez confiance, et ne laissez la copie accessible en écriture qu'à son propriétaire, car quiconque peut modifier `rebuild.sh` ou `watch.sh` à cet endroit exécute des commandes en tant qu'utilisateur du watcher.

Avec `--user USER` (ou la crontab de cet utilisateur), il tourne sous un autre utilisateur. Cet utilisateur doit être autorisé à utiliser Docker (ce qui, sur la plupart des systèmes, revient à avoir les droits de root), doit posséder la copie, et doit pouvoir écrire dans `data/update` et `data/extensions`, qui appartiennent normalement à l'utilisateur du conteneur.

<a id="windows-and-macos"></a>

### Windows et macOS

Il n'existe pas de watcher prêt à l'emploi pour Docker Desktop. Mettez à jour avec `rebuild.bat` / `rebuild.sh` à la main ou depuis une tâche planifiée, voir [Mises à jour automatiques](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Dépannage

| | |
|---|---|
| *The update watcher does not run on the server* | Le watcher n'a écrit aucun statut depuis une heure, ou jamais. Vérifiez `systemctl status marginalia-update.timer marginalia-update.path` et `journalctl -u marginalia-update.service`. Si le dossier de données n'est pas `data` dans la copie, définissez `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | La copie est sur une branche sans branche distante associée (ou sur un commit détaché), donc il n'y a rien à récupérer. Sur le serveur : `git switch <branch>` ou `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` a échoué lors de la dernière vérification (pas de réseau, dépôt privé sans identifiants pour le propriétaire de la copie). Le commit le plus récent affiché peut être ancien. |
| La mise à jour a été demandée et rien ne se passe | Avec cron, elle démarre dans la minute. Une demande que personne n'a prise en charge pendant une heure est abandonnée, afin qu'elle ne démarre pas par surprise lorsque le watcher est réparé. |
| *Last update: ..., failed* | Lisez **Log**. Un `git pull` échoué signifie généralement que la copie contient des modifications propres (fichiers édités, commits locaux) : résolvez-le sur le serveur. Une construction échouée laisse l'ancienne version en marche. |

Les fichiers du watcher se trouvent dans `update` du dossier de données : `status.properties` (ce qu'affiche l'onglet), `update.log` (la dernière mise à jour) et, brièvement, `request`.
