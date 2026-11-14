---
label: Ressources
order: 960
icon: image
---

# Ressources

L'onglet **Ressources** liste les fichiers que vous avez téléversés dans Marginalia : les
[images des parties](books/story-editor.md#images) de vos livres et les fichiers audio que des extensions comme l'
[Audiobook](extensions/audiobook.md) y enregistrent. Cet onglet permet de retrouver un fichier, d'écouter un audio,
de le télécharger, de le remplacer par une meilleure version sans toucher aux livres qui l'utilisent, et de supprimer ce
dont vous n'avez plus besoin.

![L'onglet Ressources](../images/resources-tab.png)

Les fichiers sont regroupés selon ce qui les utilise : chaque ligne du tableau est un objet (par exemple une partie d'un livre,
`ChatMessage #123`) avec le nombre de ses fichiers, leur taille totale et la date du plus récent. Cliquez sur la flèche de la
ligne pour ouvrir l'objet et voir ses fichiers. Le tableau se charge au fil du défilement et de l'ouverture des groupes, il reste donc rapide
même avec des milliers de fichiers. Cliquez sur un en-tête de colonne pour trier selon elle : les groupes et les fichiers qu'ils contiennent suivent ce tri ; les plus récents
apparaissent en premier par défaut.

| Ligne / colonne | |
|---|---|
| **Utilisé par** | L'objet auquel les fichiers ont été ajoutés. *rien* regroupe les fichiers qui ne sont utilisés par rien. *(supprimé)* après le nom signifie que l'objet a été supprimé ou n'existe plus ; les fichiers ne sont probablement plus nécessaires. À l'intérieur d'un groupe : un petit aperçu d'une image et le nom du fichier tel qu'il a été téléversé. |
| **Type** | Le type du fichier, par exemple `image/png`. |
| **Taille** | La taille du fichier ; pour un groupe, la somme de ses fichiers. |
| **Créé** | Le moment où le fichier a été téléversé ; pour un groupe, le fichier le plus récent. |

Ce n'est qu'une note pour vous. Marginalia ne l'impose pas : une image continue de fonctionner après la disparition de la partie
à laquelle elle a été ajoutée, et la même image peut être utilisée par plusieurs parties (par exemple après avoir [embranché](books/branches-and-story-tree.md)
l'histoire, la branche reprend les mêmes images que la partie dont elle est issue).

<a id="play"></a>

## Lire

Un fichier de type audio (`audio/mpeg`, `audio/wav`...) possède un bouton **Lire** (triangle) placé devant les autres boutons.
Il lit le fichier dans la page ; le bouton devient **Pause** pendant la lecture, puis redevient Lire lorsque vous l'arrêtez ou que
l'audio se termine. Un seul fichier se lit à la fois : lancer un autre arrête celui qui était en cours.

<a id="download"></a>

## Télécharger

Le bouton de téléchargement (flèche vers le bas) de la ligne enregistre le fichier sur votre ordinateur.

<a id="replace-the-content"></a>

## Remplacer le contenu

Le bouton de téléversement de la ligne ouvre une fenêtre dans laquelle vous choisissez un nouveau fichier. Le nouveau contenu est enregistré comme un nouveau fichier et
l'entrée pointe désormais vers lui ; **tout ce qui utilise l'entrée affiche donc le nouveau contenu**, et vous n'avez pas à modifier
les parties. Servez-vous-en pour remplacer une image par une meilleure version ou corriger un téléversement erroné.

- Une image ne peut être remplacée que par une image (PNG, JPEG, GIF ou WebP, jusqu'à 10 Mo, comme lors de l'
  [ajout d'images](books/story-editor.md#images)).
- Le nom de l'entrée devient le nom du nouveau fichier.
- Le fichier précédent n'est pas supprimé du dossier de données, voir [ci-dessous](#what-delete-does).

![Remplacement du contenu d'une ressource](../images/resources-replace.png)

<a id="deleting"></a>

## Supprimer

Le bouton de corbeille de la ligne supprime un seul fichier. Pour en supprimer plusieurs, cochez-les dans la première colonne et choisissez
**Supprimer la sélection** au-dessus du tableau. Cocher un groupe (une ligne d'objet) supprime tous ses fichiers. La case de l'en-tête sélectionne tous les groupes. Marginalia demande confirmation avant de supprimer.

<a id="what-delete-does"></a>

### Ce que fait la suppression

- Le fichier disparaît du tableau et des parties qui l'utilisent : une partie n'affiche simplement plus d'image. Le texte, les
  résumés et les exports de la partie ne sont pas modifiés.
- L'entrée est seulement **marquée comme supprimée**. Le [nettoyage](administration/cleanup.md) de l'administration la retire
  définitivement de la base de données.
- **Le fichier reste dans le dossier de données** (`data/<your login>/images`), y compris après le nettoyage. La suppression d'une ressource
  ne libère pas d'espace disque en soi : un administrateur supprime les fichiers qui ne sont plus utilisés par rien dans l'onglet
  [Fichiers de ressources](administration/resource-files.md) de l'administration.
- Tant que le nettoyage n'a pas retiré l'entrée, vous pouvez la restaurer depuis la [Corbeille](trash.md) : le fichier est toujours dans le dossier de données,
  donc l'image refonctionne. Après le nettoyage, seule une [sauvegarde complète de la base de données](administration/database-backups.md)
  réalisée auparavant contient encore l'entrée ; le fichier lui-même reste dans le dossier.
