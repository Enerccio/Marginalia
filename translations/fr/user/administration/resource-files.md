---
label: Fichiers de ressources
order: 85
---

# Fichiers de ressources

Les fichiers des [ressources](../resources.md) (les images que les utilisateurs joignent aux parties de l'histoire, les fichiers envoyés) sont stockés dans le
dossier de données de chaque utilisateur, dans `data/<login>/images` et `data/<login>/resources`. Supprimer une ressource, ou remplacer son
contenu, ne modifie que la base de données : le fichier reste sur le disque, et ni le [nettoyage](cleanup.md) ni autre chose
ne le supprime. **Fichiers de ressources** repère les fichiers que plus rien n'utilise, permet de les télécharger et de les supprimer
du disque pour récupérer de la place.

<a id="cleaning-up-the-files"></a>

## Nettoyer les fichiers

1. Ouvrez l'onglet **Fichiers de ressources** de la page d'administration et cliquez sur **Analyser les fichiers**. Une barre de progression affiche les dossiers en cours de lecture.
   Rien n'est modifié.
2. Le tableau liste les fichiers **qu'aucune ressource ne référence** : le chemin (`<user folder>/images/<file>`), la taille et la date de
   dernière modification. Ils sont tous cochés. Décochez ceux que vous voulez garder ; **Tout sélectionner** et **Ne rien sélectionner** agissent sur
   tous, et la flèche de l'en-tête de la première colonne fait de même. Une ligne au-dessus du tableau indique combien de fichiers
   sont inutilisés, leur taille et combien sont cochés.
3. Vous pouvez cliquer sur **Télécharger en ZIP** pour obtenir les fichiers cochés dans une seule archive (les entrées portent leur chemin comme nom), ou sur la flèche d'une ligne pour un seul fichier. C'est la sauvegarde à conserver avant de supprimer.
4. **Supprimer la sélection** retire du disque les fichiers cochés, après confirmation. **Fermer** vide le tableau sans rien supprimer.

![L'onglet Fichiers de ressources après une analyse](../../images/admin-resource-files.png)

!!!warning
Les fichiers supprimés ne peuvent pas être récupérés. Téléchargez-les d'abord en ZIP en cas de doute.
!!!

<a id="what-counts-as-used"></a>

## Ce qui compte comme utilisé

Un fichier est utilisé lorsque **une ressource quelconque de n'importe quel utilisateur le référence**, même une ressource supprimée : une ressource placée dans la [corbeille](../trash.md) peut être restaurée, et son
image doit alors fonctionner à nouveau. Ainsi, les fichiers des ressources supprimées n'apparaissent dans la liste qu'après que le [nettoyage](cleanup.md) a retiré les ressources de la base de données, avec les fichiers du contenu remplacé (l'ancien fichier reste en place) et les fichiers laissés par des envois échoués.

Les fichiers sont comparés par leur nom (un identifiant aléatoire), dans les dossiers de tous les utilisateurs, y compris les dossiers `<login>-deleted` des utilisateurs supprimés : leurs fichiers restent *utilisés* tant que le nettoyage ne purge pas l'utilisateur et ses ressources.

Seuls les dossiers `images` et `resources` du dossier de données sont lus, rien d'autre (sauvegardes de la base de données, espaces de travail des agents, etc.) n'est touché.

Un fichier qu'un utilisateur envoie après l'analyse n'est pas supprimé : **Supprimer la sélection** relit la base de données et conserve les fichiers que des ressources référencent à ce moment-là (le message indique combien de fichiers ont été conservés).

L'analyse lit le dossier de données, ce qui peut prendre du temps s'il contient beaucoup de fichiers ; **Arrêter** sur la barre de progression l'annule.
