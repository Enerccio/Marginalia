---
label: Application de bureau
order: 100
---

# Application de bureau

L'application de bureau est une version autonome de Marginalia pour un seul ordinateur. Elle embarque son propre environnement d'exécution Java et son serveur, n'écoute que sur votre ordinateur et s'ouvre dans votre navigateur par défaut.

<a id="download"></a>

## Téléchargement

Téléchargez le programme d'installation ou l'archive correspondant à votre système depuis la
[page des versions](https://github.com/Enerccio/Marginalia/releases). Quatre variantes sont publiées pour chaque système :

| Fichier | Contenu |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | Le programme d'installation : `.deb` sous Linux (Debian, Ubuntu et dérivés), `.dmg` sous macOS, `.msi` sous Windows |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | Le même programme d'installation avec tous les [plugins](../extensions/index.md) intégrés déjà installés (voir ci-dessous) |
| `marginalia-<version>-<os>-<arch>-app` | L'application : `Marginalia.app` sous macOS, `Marginalia.exe` sous Windows, `bin/Marginalia` sous Linux |
| `marginalia-<version>-<os>-<arch>` | Un dossier portable lancé avec un script, avec des [options](#options) en ligne de commande |

Des versions sont disponibles pour Windows x64, macOS (Apple Silicon et Intel) et Linux (x64 et arm64). `<arch>` dans le
nom de fichier vaut `x86_64` pour les processeurs Intel et AMD 64 bits (Windows, macOS Intel, Linux) et `aarch64` pour
ARM (Apple Silicon, Linux arm64).

<a id="install-and-start"></a>

## Installation et démarrage

+++ macOS
Avec le programme d'installation : ouvrez le `.dmg` et glissez *Marginalia* dans *Applications*. Avec l'archive :

1. Extrayez l'archive et déplacez `Marginalia.app` dans *Applications*.
2. Ouvrez-la. L'application n'est pas encore signée : la première fois, macOS refuse de l'ouvrir. Faites un clic droit sur l'application et choisissez
   *Ouvrir*, ou exécutez :

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
Avec le programme d'installation : lancez le `.msi`. Il s'installe pour votre utilisateur (pas besoin de droits administrateur), ajoute Marginalia au menu Démarrer et au bureau, et un `.msi` plus récent met à jour la version installée (vos données restent dans `.marginalia`). Avec l'archive :

1. Extrayez l'archive.
2. Lancez `Marginalia\Marginalia.exe`.
3. SmartScreen peut afficher un avertissement concernant un éditeur inconnu. Cliquez sur *Informations complémentaires* → *Exécuter quand même*.
+++ Linux
Avec le `.deb` (Debian, Ubuntu et dérivés) :

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

Il s'installe dans `/opt/marginalia` et ajoute *Marginalia* au menu des applications ; `sudo apt remove marginalia` le désinstalle (vos données dans `~/.marginalia` sont conservées). Avec l'archive :

1. Extrayez l'archive.
2. Lancez `Marginalia/bin/Marginalia`.
+++ Dossier portable
1. Extrayez l'archive.
2. Lancez `bin/marginalia` (`bin\marginalia.bat` sous Windows).
+++

Le démarrage prend quelques secondes. Marginalia s'ouvre alors dans votre navigateur à l'adresse `http://127.0.0.1:8765` et affiche la boîte de dialogue de [premier démarrage](first-start.md).

<a id="installer-with-the-plugins"></a>

### Programme d'installation avec les plugins

Le programme d'installation **full** (`-full` dans le nom du fichier) est la même application et apporte tous les plugins qui font partie de Marginalia
(les fournisseurs Anthropic et Google, Reviewer, Beta Readers, Side Query, Arc Outline, les générateurs et les autres). La
première fois que Marginalia démarre, les plugins sont copiés dans `~/.marginalia/extensions` et apparaissent dans *Admin → Extensions* ;
avec le programme d'installation standard, vous les installez vous-même, un par un.

- Un plugin n'est installé qu'une fois. Si vous le supprimez dans *Admin → Extensions*, il reste supprimé, y compris après une mise à jour vers une version plus récente de Marginalia (qui apporte les plugins plus récents et les place à côté des anciens ; Marginalia utilise le plus récent et conserve les anciens JAR dans `extensions/old-versions`). Un plugin nouveau dans une mise à jour est installé.
- Installer le programme d'installation full par-dessus le standard (ou l'inverse) conserve vos données et vos réglages, et il en va de même pour chaque mise à jour.
- Le programme d'installation standard ne supprime pas les plugins. Les plugins que vous avez installés vous-même ne sont jamais touchés.

<a id="tray-icon"></a>

## Icône de la zone de notification

Pendant son exécution, Marginalia affiche son icône (une page de manuscrit avec un petit arbre de ramification dans la marge) dans la zone de notification du système (dans la barre des menus sous macOS). Son menu comporte deux
entrées :

- **Open Marginalia** - rouvre Marginalia dans le navigateur, par exemple après avoir fermé l'onglet.
- **Quit Marginalia** - arrête le serveur. Fermer l'onglet du navigateur n'arrête pas Marginalia.

Lancer l'application une seconde fois alors qu'elle tourne déjà se contente de l'ouvrir dans le navigateur.

![L'icône de la zone de notification et son menu](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## Où se trouvent vos données

| Quoi | Où |
|---|---|
| Base de données, sauvegardes, extensions | `~/.marginalia` (`%USERPROFILE%\.marginalia` sous Windows) |
| Journaux | `~/.marginalia/desktop/logs/marginalia.log` (l'exécution précédente est conservée sous `marginalia.log.1`) |

Pour déplacer Marginalia vers un autre ordinateur, quittez-le et copiez le dossier `.marginalia` (avec `secret.key`, qui
déchiffre les clés d'API enregistrées). Avant les modifications importantes, faites une
[sauvegarde de la base de données](../administration/database-backups.md).

## Options

Le lanceur portable accepte ces options :

```
marginalia --port 8765        # port d'écoute ; un port libre est choisi si 8765 est occupé
marginalia --host 127.0.0.1   # adresse d'écoute
marginalia --home /path       # dossier qui contient le dossier .marginalia (par défaut : votre dossier personnel)
marginalia --no-browser       # ne pas ouvrir le navigateur
marginalia --no-tray          # pas d'icône dans la zone de notification ; arrêt avec Ctrl+C
marginalia --help
```

!!!warning Autres adresses
Avec `--host 0.0.0.0` (ou une autre adresse), Marginalia est accessible depuis votre réseau en HTTP simple. Pour un accès
depuis d'autres appareils, utilisez plutôt le [serveur](docker-server.md) derrière HTTPS.
!!!

<a id="updating"></a>

## Mise à jour

Quittez Marginalia, remplacez l'application (ou le dossier portable) par la nouvelle version, puis relancez-la. Vos données restent dans
`.marginalia` et sont mises à niveau automatiquement au premier démarrage ; avant que la structure de la base de données ne change,
Marginalia enregistre une [copie de celle-ci](../administration/database-backups.md#copy-before-an-upgrade) dans `db-backups`. Les extensions sont compilées pour une version donnée de Marginalia : mettez-les à jour en même temps (voir [Gestion des extensions](../administration/extensions.md)).

<a id="uninstalling"></a>

## Désinstallation

Supprimez l'application ou le dossier. Pour supprimer aussi vos données, supprimez le dossier `.marginalia`.
