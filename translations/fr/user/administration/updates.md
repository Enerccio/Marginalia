---
label: Mises à jour
order: 65
---

# Mises à jour

Sur un [serveur Docker](../getting-started/docker-server.md) construit à partir d'une copie Git, un administrateur peut
mettre à jour Marginalia depuis l'onglet **Mettre à jour** de la page d'administration, sans se connecter au serveur : le serveur récupère
la version la plus récente de la branche sur laquelle est placée sa copie, construit l'application et les
[plugins fournis](../getting-started/docker-server.md#plugins), puis redémarre.

Cela nécessite un petit programme sur le serveur, le **surveillant de mises à jour**, qui est [installé une seule fois](#setting-up-the-watcher).
Sans lui, l'onglet n'affiche que la version en cours d'exécution et indique que le surveillant ne tourne pas. C'est aussi ce que
montrent l'[application de bureau](../getting-started/desktop-app.md) et un serveur utilisant l'
[image précompilée](../getting-started/docker-server.md#prebuilt-image) : leur mise à jour se fait en installant la nouvelle
version.

<a id="the-update-tab"></a>

## L'onglet Mettre à jour

| | |
|---|---|
| **Version en cours** | La version et le commit de Marginalia qui tourne. |
| **Branche** | La branche sur laquelle est placée la copie sur le serveur et la branche distante qu'elle suit (par exemple `dev`, qui suit `origin/dev`), le commit le plus récent de cette branche distante et le moment où le serveur a vérifié pour la dernière fois. Le serveur vérifie toutes les 15 minutes. |
| Statut | *Une nouvelle version est disponible* lorsque le commit le plus récent n'est pas celui qui tourne, *Marginalia est à jour* dans le cas contraire. Pendant qu'une mise à jour est en cours, l'onglet l'indique. |
| **Dernière mise à jour** | Le moment où la dernière mise à jour lancée depuis cet onglet s'est terminée, et si elle a réussi ou échoué. |

Le serveur suit la branche qui est extraite à cet endroit, quelle qu'elle soit : pour faire passer une installation sur une autre branche,
exécutez `git switch` dans la copie sur le serveur ; l'onglet affiche la nouvelle branche lors de la prochaine vérification.

| Bouton | |
|---|---|
| **Mettre à jour et redémarrer** | Demande au serveur de se mettre à jour, après confirmation. Il construit et redémarre aussi lorsqu'il n'y a rien de nouveau (utile après une modification de `docker-compose.yml`, par exemple). |
| **Actualiser** | Relit le statut. L'onglet ne se rafraîchit pas tout seul. |
| **Journal** | La sortie de la dernière mise à jour : ce que Git et la construction ont écrit. |

<a id="what-an-update-does"></a>

## Ce que fait une mise à jour

1. La nouvelle version est construite : `git pull`, les plugins, l'image. Cela prend plusieurs minutes et **Marginalia reste
   en marche** pendant ce temps. L'onglet affiche *La mise à jour est en cours* ; **Journal** montre où en est le processus.
2. Marginalia est arrêté, les nouveaux plugins sont copiés dans `data/extensions` (en remplaçant ceux déjà installés) et la
   nouvelle version démarre. Cela prend une minute ou deux (voir [Santé](../getting-started/docker-server.md#health)).
   Toutes les personnes qui travaillent sont interrompues et tout ce qui est en cours de génération est arrêté : la confirmation vous indique
   combien de [tâches](jobs.md) tournent à ce moment-là, alors choisissez un moment calme.
3. La base de données est mise à niveau au démarrage de la nouvelle version, après qu'une
   [copie de celle-ci](database-backups.md#copy-before-an-upgrade) a été enregistrée. Faites aussi vous-même une
   [sauvegarde de la base de données](database-backups.md) au préalable.

Lorsque la construction échoue, rien n'est arrêté : l'ancienne version continue de tourner, **Dernière mise à jour** indique *échoué* et
le journal explique pourquoi. La mise à jour suivante reconstruit, même lorsqu'il n'y a rien de nouveau à récupérer.

<a id="setting-up-the-watcher"></a>

## Installer le surveillant

Marginalia tourne dans un conteneur et ne peut accéder ni à la copie Git ni à Docker ; lui donner cet accès ferait de
toute personne qui entre dans l'application un administrateur du serveur. Il se contente donc de demander : il écrit un fichier dans son
[dossier de données](index.md#the-data-folder), et le surveillant, qui tourne sur le serveur en dehors du conteneur, fait
le travail. Le surveillant est le script `tools/update-watcher/watch.sh` de la copie ; il exécute uniquement `rebuild.sh`
de cette même copie (voir [Mises à jour automatiques](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux avec systemd

Dans la copie sur le serveur (le dossier contenant `docker-compose.yml`) :

```sh
sudo tools/update-watcher/install.sh
```

Cela installe et démarre trois unités :

| Unité | |
|---|---|
| `marginalia-update.service` | Lance le surveillant une fois. |
| `marginalia-update.path` | Le démarre dès que l'application demande une mise à jour. |
| `marginalia-update.timer` | Le démarre deux minutes après le démarrage de la machine, puis toutes les 15 minutes, pour rechercher de nouveaux commits. |

Ouvrez **Administration → Mettre à jour** et cliquez sur **Actualiser** : l'onglet affiche la branche et le commit le plus récent. C'est tout.

| Option | |
|---|---|
| `--name NAME` | Le nom des unités (par défaut `marginalia-update`). Deux copies sur un même ordinateur (une installation de test et une installation de production) ont chacune besoin de leur propre nom. |
| `--user USER` | Lance le surveillant sous cet utilisateur au lieu de root, voir [ci-dessous](#who-the-watcher-runs-as). |
| `--uninstall` | Arrête et supprime les unités (avec le même `--name`). |

`systemctl status marginalia-update.service` affiche la dernière exécution et `journalctl -u marginalia-update.service` sa
sortie (le même texte que **Journal**, ainsi que la raison lorsque le surveillant lui-même n'a pas pu fonctionner). Les unités contiennent le chemin
de la copie : après avoir déplacé la copie, relancez `install.sh`.

<a id="without-systemd-cron"></a>

### Sans systemd (cron)

Lancez le surveillant chaque minute ; il ne fait rien lorsque personne n'a demandé de mise à jour et qu'il a vérifié le dépôt distant il y a moins de
15 minutes, donc cela ne coûte rien :

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Placez la ligne dans la crontab de root (`sudo crontab -e`), ou celle d'un autre utilisateur, voir plus bas. Une mise à jour démarre alors
dans la minute qui suit le clic.

<a id="settings"></a>

### Paramètres

Le surveillant lit ces variables d'environnement (avec systemd : `sudo systemctl edit marginalia-update.service` puis des lignes
`Environment=NOM=valeur` sous `[Service]`) :

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | Le dossier de données sur le serveur, lorsqu'il n'est pas `data` dans la copie (le côté gauche du volume dans `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Les arguments de `rebuild.sh`, `--force` par défaut. Sur un serveur sans assez de mémoire pour l'application et la construction ensemble, utilisez `--force --stop-first` : Marginalia est alors arrêté avant la construction et reste indisponible pendant toute sa durée. |
| `MARGINALIA_CHECK_MINUTES` | Âge maximal du statut avant qu'une exécution sans demande ne vérifie à nouveau le dépôt distant, 15 par défaut. Avec systemd, c'est le minuteur qui détermine la fréquence d'exécution du surveillant. |

<a id="who-the-watcher-runs-as"></a>

### Sous quel utilisateur le surveillant s'exécute

Par défaut, le surveillant s'exécute en tant que **root** : il doit utiliser Docker et écrire dans le dossier de données, qui appartient à
l'utilisateur du conteneur. Git lui-même est exécuté sous le propriétaire de la copie, de sorte que les fichiers de Git restent à leur propriétaire.

Le surveillant exécute ce que contient la branche (`rebuild.sh`, le `Dockerfile`), comme le fait toute mise à jour manuelle : ne suivez
qu'un dépôt en lequel vous avez confiance, et gardez la copie accessible en écriture uniquement à son propriétaire, car quiconque peut modifier `rebuild.sh` ou
`watch.sh` à cet endroit exécute des commandes sous l'utilisateur du surveillant.

Avec `--user USER` (ou la crontab de cet utilisateur), il s'exécute sous un autre compte. Cet utilisateur doit être autorisé à utiliser Docker
(ce qui, sur la plupart des systèmes, équivaut à être root), doit posséder la copie, et doit pouvoir écrire dans `data/update` et
`data/extensions`, qui appartiennent normalement à l'utilisateur du conteneur.

<a id="windows-and-macos"></a>

### Windows et macOS

Il n'existe pas de surveillant prêt à l'emploi pour Docker Desktop. Mettez à jour à la main avec `rebuild.bat` / `rebuild.sh` ou depuis une
tâche planifiée, voir [Mises à jour automatiques](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Dépannage

| | |
|---|---|
| *Le surveillant de mises à jour ne tourne pas sur le serveur* | Le surveillant n'a écrit aucun statut depuis une heure, ou jamais. Vérifiez `systemctl status marginalia-update.timer marginalia-update.path` et `journalctl -u marginalia-update.service`. Si le dossier de données n'est pas `data` dans la copie, définissez `MARGINALIA_DATA_DIR`. |
| *La branche ... ne suit aucune branche distante* | La copie est placée sur une branche sans amont (ou sur un commit détaché), il n'y a donc rien à récupérer. Sur le serveur : `git switch <branche>` ou `git branch --set-upstream-to=origin/<branche>`. |
| *Le serveur n'a pas pu atteindre le dépôt Git* | `git fetch` a échoué lors de la dernière vérification (pas de réseau, dépôt privé sans identifiants pour le propriétaire de la copie). Le commit le plus récent affiché peut être ancien. |
| La mise à jour a été demandée et rien ne se passe | Avec cron, elle démarre dans la minute. Une demande que personne n'a prise en charge pendant une heure est abandonnée, afin qu'elle ne démarre pas par surprise lorsque le surveillant sera réparé. |
| *Dernière mise à jour : ..., échoué* | Lisez **Journal**. Un `git pull` échoué signifie en général que la copie contient des modifications propres (fichiers édités, commits locaux) : réglez cela sur le serveur. Une construction échouée laisse l'ancienne version en marche. |

Les fichiers du surveillant se trouvent dans `update` du dossier de données : `status.properties` (ce qu'affiche l'onglet), `update.log`
(la dernière mise à jour) et, pendant un court instant, `request`.
