---
label: Historique VCS des livres d'univers
order: 9
---

# Historique VCS des livres d'univers

Historique des révisions pour les [entrées de livre d'univers](../lorebooks/entries.md) : conservez les versions antérieures de votre univers et passez
de l'une à l'autre, par exemple l'état du monde avant et après une guerre, ou un personnage à différents moments de
l'histoire.

!!!
Lorebook VCS fonctionne dans la [disposition téléphone](../phone.md#extensions) : le panneau se trouve sous la ligne **Activé** du livre d'univers, et les révisions d'une entrée se trouvent en haut de son formulaire.
!!!

!!!
Dans l'[éditeur plein écran](../lorebooks/lorebooks.md#editing-in-full-screen), la ligne **Lorebook Revisions** se trouve au-dessus de la liste des entrées, et la barre de révision d'une entrée se trouve sous la ligne du nom de son onglet. Changer de révision affiche aussitôt celle-ci dans l'onglet.
!!!

Une révision est un instantané de l'entrée entière : nom, contenu, note, activé, ordre, filtre et mode de filtrage, mode d'insertion, *Ne pas chercher d'autres entrées dans son contenu*, étiquettes et étiquettes négatives. (Les révisions créées avant que cet indicateur existe sont lues comme désactivé.)

![La barre de révision d'une entrée](../../images/plugin-lorebookvcs.png)

<a id="entry-revisions"></a>

## Révisions d'une entrée

Une fois l'extension installée, les détails (⌄) de chaque entrée de livre d'univers commencent par une barre de révision :

```
Revision: 2 / 3            [−]  [‹]  [Rev 2 (2026-10-08 14:30:12) ▾]  [›]  [+]
Modified: 2026-10-08 14:30:12
```

Les révisions fonctionnent comme des emplacements. L'entrée affiche toujours l'une d'elles - la **révision courante** - et
ce que vous modifiez dans l'entrée appartient à cette révision. La génération utilise toujours l'entrée telle qu'elle est,
donc la révision courante est celle que voit le modèle.

| Contrôle | |
|---|---|
| **+** | Enregistre l'entrée telle qu'elle est maintenant comme nouvelle révision et la rend courante. |
| **‹** / **›**, ou la liste | Passe à la révision précédente / suivante / choisie. Vos modifications restent dans la révision que vous quittez ; l'autre est chargée dans l'entrée. |
| **−** | Supprime la révision courante et passe à la précédente. La dernière révision restante ne peut pas être supprimée. |
| *n / total*, *Modified* | Quelle révision est courante, et quand elle a été modifiée pour la dernière fois. |

La première fois que les détails d'une entrée sont ouverts, son état actuel devient la révision 1.

Un usage typique :

1. Ouvrez l'entrée d'un personnage et cliquez sur **+** - la révision 2 est une copie de la révision 1.
2. Modifiez la révision 2 pour que le personnage corresponde à son état après les événements du chapitre 10.
3. Écrivez. Lorsque vous revenez éditer le chapitre 5, passez à la révision 1 avec **‹** ; revenez-y plus tard avec **›**.

<a id="lorebook-revisions"></a>

## Révisions du livre d'univers

L'éditeur de livre d'univers reçoit une ligne **Lorebook Revisions** avec deux boutons. Ils agissent sur le livre d'univers sélectionné dans l'éditeur :

| Bouton | |
|---|---|
| export | Télécharge l'historique de toutes les entrées du livre d'univers sous la forme `<lorebook>_vcs_history.json`. |
| import | **Import Marginalia VCS JSON** - un historique exporté par cette extension ; **Import SillyTavern VCS Extension JSON** - un historique provenant de l'extension d'historique de versions des livres d'univers de SillyTavern. |

Un import **remplace** l'historique du livre d'univers ; si le livre d'univers a déjà un historique, ou si le fichier a été
exporté depuis un autre livre d'univers, l'import demande d'abord une confirmation.

Un historique Marginalia est d'abord associé aux entrées grâce aux entrées elles-mêmes. Les entrées qu'il ne retrouve pas - par exemple
lorsque l'historique provient d'une autre copie du livre d'univers - sont associées par le nom et l'ordre de leur révision courante, ou par le nom seul lorsqu'une seule entrée le porte. Les historiques qui ne correspondent à aucune entrée sont ignorés ; la confirmation indique combien.

L'historique SillyTavern est associé aux entrées selon leur position dans le livre d'univers ; importez-le donc dans un livre d'univers
[importé depuis les mêmes informations de monde](../lorebooks/import-export.md#importing-sillytavern-world-info), avant d'ajouter ou de supprimer des entrées. Les mots-clés de déclenchement de ses révisions deviennent le filtre de l'entrée, de la même façon que la
[conversion lors de l'import des informations de monde](../lorebooks/import-export.md#importing-sillytavern-world-info) les transforme.

<a id="storage"></a>

## Stockage

L'historique est stocké dans la base de données avec le livre d'univers, donc les [sauvegardes de la base de données](../administration/database-backups.md)
l'incluent. Les données qu'une extension conserve dans un livre d'univers sont aussi écrites dans les [exports de livre d'univers](../lorebooks/import-export.md#exporting)
et dans les livres d'univers des [sauvegardes de livre](../books/backups.md), ainsi que dans les copies des livres d'univers d'un livre
sur lesquelles travaillent les [agents](../agents.md). Mais importer un livre d'univers crée de nouvelles entrées, et l'historique appartient à l'identité des anciennes, donc il n'apparaît pas sur elles. Pour transférer l'historique vers une copie importée, exportez-le depuis l'original et importez-le dans la copie - les entrées sont alors associées par nom et ordre (voir [Révisions du livre d'univers](#lorebook-revisions)).
