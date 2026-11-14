---
label: Sauvegardes du livre
order: 50
---

# Sauvegardes du livre

Une sauvegarde de livre est un instantané d'un seul livre : ses paramètres et ses prompts, toutes les parties de toutes les branches, les résumés, les étiquettes et le lorebook. Utilisez les sauvegardes pour revenir à un état antérieur de l'histoire, pour faire une copie d'un livre, ou pour transférer un livre vers une autre installation de Marginalia ou un autre utilisateur.

Les sauvegardes de livre sont distinctes des [sauvegardes de la base de données](../administration/database-backups.md) que les administrateurs font de l'installation complète.

![L'onglet Sauvegardes d'un livre](../../images/book-backups.png)

<a id="taking-a-backup"></a>

## Faire une sauvegarde

Dans l'onglet **Sauvegardes** du livre, cliquez sur **Faire une sauvegarde**. La sauvegarde apparaît en haut de la liste avec sa **Date**, le nombre de **Messages** (parties) qu'elle contient et le nombre d'**Images** (les [images des parties](story-editor.md#images)) utilisées par ses parties.

Les sauvegardes peuvent aussi être faites automatiquement, voir [ci-dessous](#automatic-backups).

Une sauvegarde contient l'arbre complet de l'histoire, les résumés et les [annotations](annotations.md) des parties ; la restauration, la copie d'un livre et l'importation d'une sauvegarde rétablissent les notes.

<a id="the-history-of-the-parts"></a>

## L'historique des parties

Une sauvegarde ne contient pas l'[historique des versions](version-history.md) des parties, sauf si vous le demandez : cochez **Inclure l'historique des versions** à côté de **Faire une sauvegarde** (la case est décochée par défaut, car l'historique peut peser plusieurs fois la taille de l'histoire). La restauration d'une telle sauvegarde, en tant que livre ou en tant que copie, rétablit les versions avec leurs noms, le raisonnement des versions générées et la corbeille de l'historique ; les versions sont réduites à la limite du livre où elles arrivent. Les sauvegardes automatiques ne contiennent jamais l'historique. Restaurer une sauvegarde sans historique donne à chaque partie une première version, comme pour un livre antérieur à l'historique.

<a id="backups-and-folders"></a>

## Sauvegardes et dossiers

Une sauvegarde mémorise les [dossiers](folders.md) dans lesquels se trouvait son livre, du plus haut au plus bas, chacun avec son nom, sa description, sa langue et les paramètres de texte qu'il définit. **Restaurer comme nouveau livre** et **Cloner la sauvegarde** placent le nouveau livre dans le dossier que nomme la sauvegarde, si vous l'avez encore ; sinon, dans les dossiers portant les mêmes noms (un dossier que vous possédez est utilisé tel quel, un dossier manquant est créé avec les paramètres qu'il avait). Une sauvegarde sans dossier, ou dont les dossiers ne peuvent pas être créés, est restaurée au niveau supérieur. **Restaurer** un livre (complet ou messages seulement) garde le livre dans son dossier. Les étiquettes d'un dossier ne font pas partie de la sauvegarde. Les anciennes sauvegardes se restaurent comme auparavant.

<a id="pictures-in-a-backup"></a>

## Images dans une sauvegarde

Une sauvegarde mémorise **quelles images** les parties possèdent (et leurs légendes), mais **pas les fichiers d'images**. Cela garde les sauvegardes, y compris les automatiques, petites. Les images restent dans vos [Ressources](../resources.md). Pour emporter les images avec une sauvegarde, incluez-les lorsque vous l'[exportez](#exporting-and-importing).

<a id="restoring-a-backup"></a>

## Restaurer une sauvegarde

Cliquez sur **Restaurer** à côté de la sauvegarde et choisissez :

| Option | Ce qui est restauré |
|---|---|
| **Restauration complète** | Tout : nom, description, publication, prompts et autres paramètres du livre, étiquettes, modèle, protocole, lorebook et toutes les parties. |
| **Restaurer les messages seulement** | Seulement les parties (toutes les branches) et les résumés. Les paramètres, prompts et lorebook actuels du livre restent tels quels. |

!!!warning
Restaurer remplace **toutes les parties** du livre par celles de la sauvegarde. Les parties écrites après la sauvegarde sont perdues. Faites d'abord une sauvegarde de l'état actuel si vous pourriez vouloir le récupérer.
!!!

La fenêtre du livre se ferme puis se rouvre avec le livre restauré.

Les images des parties restaurées, ainsi que tout autre fichier vers lequel pointent les parties (l'audio d'un [livre audio](../extensions/audiobook.md), par exemple), sont ceux que la sauvegarde énumère, tant que vous les avez toujours dans les [Ressources](../resources.md). Une ressource que vous avez supprimée depuis n'est plus dans la partie. La restauration ne rapporte pas les fichiers supprimés, sauf si la sauvegarde provient d'un export avec les ressources que vous avez [importé](#importing-a-backup).

Le **modèle et le protocole** sont recherchés parmi vos fournisseurs d'inférence et vos protocoles : d'abord le même exact que dans la sauvegarde, puis celui qui porte le même nom. Si aucun n'existe, le livre garde son modèle ou protocole actuel.

Les **lorebooks** de la sauvegarde sont comparés aux vôtres. Lorsqu'un lorebook portant le même nom, mais une identité différente, existe, Marginalia demande pour chacun s'il faut **Lier à l'existant** ou **Créer un nouveau** à partir de la sauvegarde. Les lorebooks que vous ne possédez pas du tout peuvent être importés (**Importer**) ou laissés de côté (**Ne pas importer**).

![Choisir quoi faire des lorebooks](../../images/lorebook-import.png)

<a id="copying-a-book"></a>

## Copier un livre

**Cloner la sauvegarde** crée un nouveau livre à partir de la sauvegarde, sous un nom que vous saisissez. Le livre original n'est pas modifié. Utilisez-le pour tenter quelque chose de radical avec une copie du livre, ou pour démarrer un nouveau livre à partir de la même ouverture.

Si la sauvegarde pointe vers des ressources (images, audio d'un livre audio, autres fichiers), Marginalia demande **Avec les ressources** ou **Sans les ressources** :

- *Avec les ressources* donne au nouveau livre **ses propres copies** de celles-ci (les mêmes fichiers, de nouvelles entrées dans les [Ressources](../resources.md)), de sorte que supprimer ou remplacer une image dans un livre ne touche pas l'autre. Les ressources que vous avez supprimées depuis la sauvegarde sont laissées de côté.
- *Sans les ressources* clone le texte seulement.

![Cloner avec ou sans les ressources](../../images/backup-clone-images.png)
<a id="exporting-and-importing"></a>

## Exporter et importer

**Exporter** télécharge la sauvegarde sous forme de fichier JSON (`<book name>_backup.json`). Conservez-le comme copie supplémentaire, ou emportez-le vers une autre installation.

Si la sauvegarde pointe vers des ressources, Marginalia demande s'il faut les empaqueter (les images des parties et tout autre fichier que leurs données désignent, comme l'audio d'un livre audio) :

![Exporter une sauvegarde qui contient des images](../../images/backup-export-images.png)

- **Sans les ressources** télécharge tout de suite le JSON, comme ci-dessus. Les ressources n'y sont que mentionnées.
- **Avec les ressources** empaquette la sauvegarde et toutes ses ressources dans un **fichier ZIP** (`<book name>_backup.zip`). Une barre de progression indique combien de fichiers sont traités ; le fichier est construit sur le disque un fichier à la fois, si bien que même un livre comptant de nombreux gros fichiers n'utilise pas beaucoup de mémoire. Le livre audio d'un long roman pèse plusieurs centaines de mégaoctets : ne l'empaquetez que lorsque vous en avez besoin. Une fois prêt, cliquez sur **Télécharger**. Le fichier n'est conservé que jusqu'à la fermeture de cette boîte de dialogue.

Si une ressource ne peut pas être empaquetée parce que vous l'avez supprimée ou que son fichier manque, la boîte de dialogue indique combien ont été omises.

La case **Inclure les paramètres hérités dans l'export** (activée par défaut, à côté des boutons d'export) détermine ce que le fichier indique au sujet des paramètres que le livre reçoit de ses [dossiers](folders.md#where-the-settings-of-a-book-come-from). Activée, la langue, le modèle maître, le point de vue, le temps, le style et les consignes que le livre ne définit pas lui-même sont écrits dans le fichier comme ses propres valeurs, prises d'abord dans les dossiers où se trouvait le livre au moment de la sauvegarde (le plus proche en premier), puis dans vos paramètres ; ainsi le livre se comporte de la même façon dans une installation ou un compte qui ne possède pas ces dossiers et ces paramètres. Les valeurs par défaut intégrées ne sont pas écrites. Désactivée, le fichier ne contient que ce que le livre définit lui-même. Le fichier n'indique jamais dans quel dossier se trouve le livre : le classement d'un livre est un choix local. (L'export du récit, [Exporter un livre](export.md), n'écrit que le texte et n'a pas de paramètres.)

Le ZIP contient `backup.json` (le même JSON), `resources.json` (la liste des ressources) et les fichiers. Vous n'avez pas besoin de le décompresser : les deux méthodes de réimportation d'une sauvegarde acceptent aussi bien le `.zip` que le `.json`.

Il existe deux façons de réimporter un fichier de sauvegarde :

- **Importer une sauvegarde** dans l'onglet *Sauvegardes* ajoute le fichier à la liste des sauvegardes de **ce livre**. Rien ne change tant que vous ne le *Restaurez* pas.
- **Importer la sauvegarde comme nouveau livre** dans l'onglet *Paramètres* crée un **nouveau livre** à partir du fichier.

Les deux acceptent un fichier `.json` ou `.zip`. Le fichier est envoyé sur le serveur et lu à partir de là, si bien qu'une grosse sauvegarde ne pose aucun problème.

<a id="importing-a-backup"></a>

### Importer une sauvegarde

Lorsque le fichier est un ZIP contenant des images, les images sont d'abord ajoutées à vos [Ressources](../resources.md), et la sauvegarde les utilise ensuite. Une image que vous possédez déjà (le même fichier) n'est pas stockée une seconde fois : l'existante est utilisée, si bien que réimporter votre propre export n'ajoute rien. Un fichier JSON sans images est importé tel quel.

Importer **comme nouveau livre** donne au nouveau livre **ses propres entrées** dans les Ressources, même si vous possédez déjà les mêmes images : les entrées pointent vers les mêmes fichiers, donc aucun espace disque n'est utilisé deux fois. Si le fichier ne contient pas d'images (un JSON simple), le nouveau livre reçoit des copies des images que le fichier énumère et que vous possédez encore ; celles que vous ne possédez plus sont omises. Tout élément du fichier qui n'est pas une image utilisable (ni PNG, JPEG, GIF ou WebP, ou trop volumineux) est ignoré.

<a id="importing-a-backup-as-a-new-book"></a>

### Importer une sauvegarde comme nouveau livre

1. Ouvrez l'onglet **Paramètres** et cliquez sur **Importer la sauvegarde comme nouveau livre**.
2. Saisissez le **Nom du nouveau livre** — l'envoi n'est activé qu'une fois le nom renseigné.
3. Envoyez le fichier de sauvegarde.
4. Décidez pour les livres de référence (lorebooks), si cela vous est demandé.

Le nouveau livre apparaît dans l'onglet *Livres*.

<a id="deleting-backups"></a>

## Supprimer des sauvegardes

Le bouton corbeille supprime définitivement une sauvegarde. Les sauvegardes automatiques ne sont pas remplacées en rotation : elles restent jusqu'à ce que vous les supprimiez.

<a id="automatic-backups"></a>

## Sauvegardes automatiques

Marginalia peut prendre une sauvegarde de lui-même pendant que vous écrivez. Réglez la valeur par défaut pour tous les livres dans **Paramètres → Paramètres généraux → Stratégie de sauvegarde** :

| Stratégie | |
|---|---|
| **Aucune sauvegarde** | Aucune sauvegarde automatique. |
| **Après N messages** | Une sauvegarde après chaque *N* parties générées. |
| **Après N minutes** | Une sauvegarde après une partie générée lorsqu'au moins *N* minutes se sont écoulées depuis la dernière sauvegarde automatique. |

Pour utiliser une autre stratégie pour un seul livre, cochez **Réglage propre au manuscrit** dans son onglet *Sauvegardes*, puis choisissez-y la stratégie et le nombre. Décocher la case rétablit la valeur par défaut des *Paramètres*.

Les sauvegardes automatiques ne sont prises qu'après la génération d'une partie : un livre que vous ne faites que lire ne reçoit donc pas de nouvelles sauvegardes.

<a id="where-backups-are-stored"></a>

## Emplacement des sauvegardes

Chaque sauvegarde est un fichier JSON dans le dossier de données de Marginalia :

```
~/.marginalia/data/<your login>/backups/manuscripts/<book id>/
```

Avec la [version Docker](../getting-started/docker-server.md), ce dossier se trouve à `./data/data/<login>/...` sur l'hôte. Les fichiers ne sont pas dans la base de données, de sorte qu'une [sauvegarde de la base de données](../administration/database-backups.md) ne les contient pas. Incluez tout le dossier `.marginalia` dans vos propres sauvegardes.

Les fichiers d'images ne sont pas non plus dans les sauvegardes : ils se trouvent dans le dossier `data/<your login>/images`, à côté de `backups`. Ils ne figurent pas non plus dans une [sauvegarde de la base de données](../administration/database-backups.md) ; voir cette page.
