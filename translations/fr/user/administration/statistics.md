---
label: Statistiques
order: 87
---

# Statistiques

Les [statistiques d'écriture](../books/statistics.md) de chaque utilisateur (les mots, le temps passé, la taille de chaque livre, jour par jour) sont conservées
dans la base de données. Elles enregistrent les moments où les utilisateurs ont travaillé sur leurs livres ainsi que les noms et dossiers des livres, y compris ceux qui ont été
supprimés. L'onglet **Statistiques** de la page d'administration vous permet de limiter cela. Les utilisateurs peuvent faire de même pour eux-mêmes dans leurs
[paramètres](../account.md) et effacer leurs propres statistiques dans les fenêtres de statistiques.

![L'onglet Statistiques de la page d'administration](../../images/admin-statistics.png)

| | |
|---|---|
| **Enregistrer les statistiques d'écriture** | Désactivé arrête l'enregistrement pour tous les utilisateurs, quel que soit leur choix. Rien de ce qui a déjà été enregistré n'est supprimé par cette option. Les fenêtres de statistiques indiquent que plus rien de nouveau n'est comptabilisé. |
| **Conserver les statistiques pendant (mois)** | Les jours plus anciens que cette durée sont supprimés définitivement des statistiques de tous les utilisateurs, une fois par jour (vers 4 h 20, et peu après le démarrage) et lorsque vous enregistrez. Vide ou 0 conserve tout. Un utilisateur peut choisir une durée plus courte pour lui-même, jamais plus longue. |
| **Effacer les statistiques de l'utilisateur** | Supprime toutes les statistiques de l'utilisateur choisi, après une confirmation. Ses livres ne sont pas modifiés. |
| **Effacer les statistiques de tout le monde** | Même action pour tous les utilisateurs. |

Les statistiques supprimées ne peuvent pas être restaurées, sauf à partir d'une [sauvegarde de la base de données](database-backups.md) : les statistiques font partie de la
base de données (la sauvegarde d'un seul livre ne les contient pas).

Le [nettoyage](cleanup.md) ne touche pas aux statistiques, car ce ne sont pas des objets supprimés : elles référencent le livre par son identifiant et lui survivent volontairement.
