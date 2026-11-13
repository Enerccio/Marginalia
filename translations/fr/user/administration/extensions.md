---
label: Gestion des extensions
order: 70
---

# Gestion des extensions

L'onglet **Extensions** de la page d'administration installe et supprime des [extensions](../extensions/index.md) pour l'ensemble de l'installation.

![L'onglet Extensions](../../images/admin-extensions.png)

<a id="installing"></a>

## Installation

Cliquez sur **Charger une extension (.jar)** (ou déposez le fichier dessus) et choisissez le fichier JAR de l'extension. Il est copié dans le dossier `extensions` du [dossier de données](index.md#the-data-folder) et démarré immédiatement ; *Extension loaded successfully.* confirme l'opération. L'extension apparaît alors dans la liste :

| Colonne | |
|---|---|
| **ID** | Le numéro interne de l'extension pour cette exécution. |
| **Extension Name** | Son nom. |
| **Version** | Sa version. Le bouton **#** à côté indique l'empreinte SHA-256 du JAR de l'extension lorsque vous le survolez ; cliquez dessus pour copier l'empreinte, par exemple pour la comparer à celle publiée par l'auteur. |
| **State** | *Active* lorsqu'elle est en cours d'exécution ; *Stopped* lorsque vous l'avez arrêtée (voir [Arrêt et démarrage](#stopping-and-starting)) ; *Installed* ou *Resolved* lorsqu'elle n'a pas réussi à démarrer (voir [Lorsqu'une extension échoue](#when-an-extension-fails)). |
| **Verification** | *Valid* ou *Invalid* ; cliquez dessus pour lire le rapport de la vérification effectuée par Marginalia avant de démarrer l'extension. |
| **Actions** | **Stop** / **Start**, **Log** et **Unload**. |

Les utilisateurs voient l'extension dans les fenêtres qu'ils ouvrent à partir de ce moment ; un livre déjà ouvert doit être fermé puis rouvert, ou la page rechargée.

Les fichiers JAR peuvent aussi être copiés directement dans le dossier `extensions`, par exemple sur un serveur sans accès à l'interface ; ils sont chargés au prochain démarrage. C'est aussi ainsi que l'installation Docker peut être préparée : placez les JAR dans `./data/extensions`. Les JAR peuvent aussi être compilés dans Docker, voir [Plugins](../getting-started/docker-server.md#plugins) dans le guide du serveur.

!!!warning Utilisez des extensions compilées pour cette version
Les extensions modifient des parties de l'interface utilisateur d'une version donnée de Marginalia. Utilisez des extensions compilées à partir du même code source que l'application (voir le [README des plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)). Une extension incompatible peut ne pas se charger ou perturber certaines parties de l'application. L'API des extensions n'est pas figée dans la version 1.0 : attendez-vous à mettre à jour les extensions en même temps que Marginalia.
!!!

<a id="stopping-and-starting"></a>

## Arrêt et démarrage

**Stop** décharge immédiatement l'extension sans la supprimer : elle disparaît des écrans auxquels elle avait ajouté des éléments, mais son JAR et ses données sont conservés, et elle figure dans la liste avec l'état *Stopped*. Le bouton affiche alors **Start**, qui la charge à nouveau (elle est d'abord vérifiée, comme au démarrage de l'application ; si la vérification échoue, elle reste arrêtée et le rapport s'affiche). Une extension arrêtée reste arrêtée après un redémarrage de Marginalia. Charger une nouvelle version d'une extension arrêtée la démarre.

L'arrêt est mémorisé par un fichier vide portant le nom du JAR suivi de `.STOPPED` (`sidequery.jar.STOPPED`) dans le dossier `extensions`. Sur un serveur sans accès à l'interface, créez ce fichier pour empêcher le démarrage d'une extension, ou supprimez-le pour que l'extension démarre au prochain lancement.

<a id="the-extension-log"></a>

## Le journal de l'extension

**Log** ouvre le journal de l'extension : ce que l'extension elle-même a écrit avec le logger fourni par Marginalia aux extensions ([Journalisation](../../developer/plugins/overview.md#logging)). Il affiche la dernière partie du fichier ; **Actualiser** le relit et **Effacer** le supprime. Le fichier est `extensions/<name>.jar.log` (par exemple `sidequery.jar.log`) ; lorsqu'il dépasse 2 Mo, il est renommé en `.log.old` et un nouveau fichier est créé. Il est supprimé lorsque l'extension est déchargée. Les extensions qui n'écrivent rien dans le journal ont un journal vide. Ce que l'extension écrit est aussi envoyé au journal de l'application (nommé `extension.<name>`, au niveau de chaque ligne), il figure donc aussi dans le journal du serveur ; les problèmes de Marginalia lui-même n'y figurent que là.

![Le journal d'une extension](../../images/extension-log.png)

<a id="removing"></a>

## Suppression

**Unload** demande une confirmation, puis arrête immédiatement l'extension et supprime son JAR (ainsi que son journal) du dossier `extensions`.
Ses données sont conservées (voir [Où sont conservées les données des extensions](../extensions/index.md#where-extension-data-is-kept)) ; la recharger restaure les données.

<a id="updating"></a>

## Mise à jour

Pour installer une version plus récente d'une extension, utilisez **Charger une extension (.jar)** avec le nouveau fichier — le nom du fichier n'a pas d'importance. La version installée est déchargée, son JAR supprimé et la nouvelle version démarrée à sa place. Si la nouvelle version ne se charge pas, l'envoi affiche l'erreur et l'ancienne version reste en fonctionnement.

<a id="when-an-extension-fails"></a>

## Lorsqu'une extension échoue

Si une extension envoyée ne peut pas être chargée, l'envoi affiche *Failed to load extension: ...* et son JAR n'est pas conservé.

Marginalia vérifie une extension par rapport à la version dans laquelle elle s'exécute avant de la démarrer. Une extension qui ne correspond pas — typiquement une extension compilée pour une autre version de Marginalia — n'est pas chargée. Un envoi affiche le rapport de la vérification et l'extension n'est pas conservée ; une extension présente dans le dossier `extensions` reste dans la liste, non démarrée, avec **Invalid** dans la colonne *Verification*, et le rapport figure dans le journal. La vérification est répétée après une mise à jour de Marginalia : une extension qui fonctionnait auparavant peut donc être refusée ensuite. Procurez-vous alors la version de l'extension compilée pour la nouvelle version de Marginalia et chargez-la. Tant qu'une extension est invalide, une **barre rouge en haut de l'espace de travail** en informe tout le monde : un administrateur voit au survol quelles extensions sont concernées et ouvre cette page d'un clic sur la barre ; les autres utilisateurs sont invités à prévenir l'administrateur (une extension que vous avez vous-même arrêtée n'est pas comptée). À côté du JAR de chaque extension, vous trouverez un petit fichier `.valid` ou `.invalid` contenant le rapport ; il est supprimé avec l'extension et peut être laissé en place.

![La barre rouge au-dessus de l'espace de travail lorsqu'une extension est invalide](../../images/invalid-extensions-warning.png)

![Le rapport de vérification d'une extension qui ne correspond pas, et une extension arrêtée (bouton Start)](../../images/extension-verification.png)

Une extension du dossier `extensions` qui ne parvient pas à démarrer (par exemple une extension compilée pour une autre version) n'empêche pas le chargement des autres. Elle figure dans la liste avec l'état *Installed* ou *Resolved* au lieu de *Active*, et peut être supprimée avec **Unload**. Un fichier qui n'est pas du tout une extension est ignoré et n'apparaît pas dans la liste — supprimez-le du dossier `extensions`. Le journal (`docker compose logs`, ou `desktop/logs/marginalia.log` pour l'application de bureau) contient le détail de l'erreur.
