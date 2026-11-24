---
label: Ressources
order: 960
icon: image
---

# Ressources

L'onglet **Resources** liste les fichiers que vous avez importés dans Marginalia : les
[images des parties](books/story-editor.md#images) de vos livres et les fichiers audio que des extensions comme l'[Audiobook](extensions/audiobook.md)
enregistrent avec eux. Cet onglet sert à retrouver un fichier, à écouter un audio, à le télécharger, à le remplacer
par une meilleure version sans toucher aux livres qui l'utilisent, et à supprimer ce dont vous n'avez plus besoin.

![L'onglet Resources](../images/resources-tab.png)

Les fichiers sont regroupés selon ce qui les utilise : chaque ligne du tableau correspond à un objet (par exemple une
partie d'un livre, `ChatMessage #123`) avec le nombre de ses fichiers, leur taille totale et la date du plus récent. Cliquez sur la
flèche de la ligne pour ouvrir l'objet et voir ses fichiers. Le tableau se charge au fil du défilement et de l'ouverture
des groupes, il reste donc rapide même avec des milliers de fichiers. Cliquez sur l'en-tête d'une colonne pour trier
selon elle : les groupes et les fichiers qu'ils contiennent suivent le tri. Par défaut, les plus récents sont en tête.

| Ligne / colonne | |
|---|---|
| **Used by** | L'objet auquel les fichiers ont été ajoutés. *rien* regroupe les fichiers qui ne sont utilisés par rien. *(disparu)* après le nom signifie que l'objet a été supprimé ou n'existe plus ; les fichiers ne sont probablement plus nécessaires, et le [nettoyage](administration/cleanup.md) de l'administrateur les supprime dès que rien d'autre ne les utilise. Dans un groupe : un petit aperçu d'une image et le nom du fichier tel qu'il a été importé. |
| **Type** | Le type du fichier, par exemple `image/png`. |
| **Size** | La taille du fichier ; pour un groupe, la somme de ses fichiers. |
| **Created** | La date d'import du fichier ; pour un groupe, celle du fichier le plus récent. |

Ceci est une note conservée pour vous. Une image reste disponible après la disparition de la partie à laquelle elle a été
ajoutée, et une même image peut être utilisée par plusieurs parties (par exemple après une [branche](books/branches-and-story-tree.md) de l'histoire,
la branche reprend les mêmes images que la partie dont elle est issue) ; le nettoyage conserve un fichier tant qu'une
partie l'utilise.

<a id="play"></a>

## Lecture

Un fichier de type audio (`audio/mpeg`, `audio/wav`...) a un bouton **Play** (triangle) devant les autres boutons.
Il lit le fichier dans la page ; le bouton devient **Pause** pendant la lecture, puis redevient Play quand vous l'arrêtez
ou quand l'audio se termine. Un seul fichier est lu à la fois : en lancer un autre arrête celui qui jouait.

<a id="download"></a>

## Téléchargement

Le bouton de téléchargement (flèche vers le bas) de la ligne enregistre le fichier sur votre ordinateur.

<a id="replace-the-content"></a>

## Remplacer le contenu

Le bouton d'import de la ligne ouvre une boîte de dialogue où vous choisissez un nouveau fichier. Le nouveau contenu est
enregistré comme un nouveau fichier et l'entrée pointe désormais vers lui, donc **tout ce qui utilise l'entrée affiche
le nouveau contenu** ; vous n'avez pas à modifier les parties. Servez-vous-en pour remplacer une image par une meilleure
version ou pour corriger un import erroné.

- Une image ne peut être remplacée que par une image (PNG, JPEG, GIF ou WebP, jusqu'à 10 Mo, comme lors de
  l'[ajout d'images](books/story-editor.md#images)).
- Le nom de l'entrée devient le nom du nouveau fichier.
- L'ancien fichier n'est pas supprimé du dossier de données, voir [plus bas](#what-delete-does).

![Remplacement du contenu d'une ressource](../images/resources-replace.png)

<a id="deleting"></a>

## Suppression

Le bouton corbeille de la ligne supprime un fichier. Pour en supprimer plusieurs, cochez-les dans la première colonne et
choisissez **Delete selected** au-dessus du tableau. Cocher un groupe (une ligne d'un objet) supprime tous ses fichiers.
La case de l'en-tête sélectionne tous les groupes. Marginalia demande confirmation avant de supprimer.

<a id="what-delete-does"></a>

### Ce que fait la suppression

- Le fichier disparaît du tableau et des parties qui l'utilisent : une partie n'affiche simplement plus d'image. Le texte,
  les résumés et les exports de la partie ne sont pas modifiés.
- L'entrée est seulement **marquée comme supprimée**. Le [nettoyage](administration/cleanup.md) de l'administration la retire
  définitivement de la base de données.
- **Le fichier reste dans le dossier de données** (`data/<your login>/images`), y compris après le nettoyage. La suppression d'une ressource
  ne libère pas d'espace disque à elle seule : un administrateur supprime les fichiers qui ne sont plus utilisés dans
  l'onglet [Resource Files](administration/resource-files.md) de l'administration.
- Tant que le nettoyage n'a pas retiré l'entrée, vous pouvez la restaurer depuis la [Corbeille](trash.md) : le fichier est
  toujours dans le dossier de données, donc l'image refonctionne. Après le nettoyage, seule une [sauvegarde complète de la
  base de données](administration/database-backups.md) faite auparavant contient encore l'entrée ; le fichier lui-même reste dans le dossier.
