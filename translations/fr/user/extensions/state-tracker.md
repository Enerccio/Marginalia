---
label: Suivi de l'état
order: 29
---

# Suivi de l'état

Garde la trace de l'**état** actuel de vos personnages : ce qu'ils portent, ce qu'ils transportent, l'arme qu'ils tiennent en main, leurs blessures, leur humeur, ou tout autre élément que vous définissez. Il place l'état des personnages de la scène dans le prompt. Un modèle oublie facilement que l'épée a été perdue deux chapitres plus tôt ou que la cape est trempée. Avec le State Tracker, le prompt indique, pour chaque personnage suivi présent dans la scène, ce qui est vrai pour lui maintenant.

!!!
Vous choisissez qui est suivi et ce qui est suivi à leur sujet. Seuls les personnages que vous ajoutez à l'onglet sont décrits au modèle, et seulement tant qu'ils figurent parmi les *Personnages présents* de la partie en cours d'écriture. Dans cette version, vous tenez l'état vous-même (ou un agent le fait, avec les commandes ci-dessous) ; le suivi de l'histoire avec le modèle viendra plus tard.
!!!

<a id="categories"></a>

## Catégories

Une **catégorie** est un élément qui peut être suivi pour un personnage. Chaque catégorie possède :

- un **nom**, qui est aussi celui que le prompt utilise (*Wearing*, *Weapon at hand*, *Carrying*) ;
- un **type** :
  - **Valeur unique** : une seule valeur courte, et une nouvelle valeur remplace l'ancienne (l'arme en main, le lieu).
  - **Liste** : des entrées ajoutées et retirées une à une (ce qui est transporté, les blessures).
  - **Description** : un seul texte réécrit en entier (les vêtements, l'humeur).
- une **consigne pour le modèle** : ce qui relève de la catégorie et ce qui n'en relève pas. Le modèle la lira lorsqu'il suivra l'histoire.
- **Only what the text says** : activé, la valeur ne change que lorsque le texte le dit ; désactivé, elle change aussi selon les conséquences évidentes (mouillé après une baignade). Le modèle s'en servira lorsqu'il suivra l'histoire.
- **Say "none" when empty** : le prompt écrit *none* lorsque le personnage n'a pas de valeur (une arme perdue), au lieu d'omettre la catégorie.

<a id="my-categories"></a>

### Mes catégories

Vos propres catégories se trouvent dans **Settings → Extension Settings → State Tracker**. Les livres copient leurs catégories depuis celles-ci. Tant que vous ne les modifiez pas, vous disposez de cinq catégories intégrées : *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* et *Mood*. Ajoutez, modifiez et supprimez les catégories à cet endroit, puis appuyez sur **Enregistrer** dans l'écran Settings. **Reset to the built-in categories** rétablit les cinq catégories intégrées.

<a id="the-categories-of-a-book"></a>

### Les catégories d'un livre

Un livre conserve sa **propre copie** de chaque catégorie qu'il utilise, afin qu'une sauvegarde ou un export du livre contienne tout, et que supprimer une de vos catégories ne casse jamais un livre. Le bouton **Categories of this book** de l'onglet État les liste :

- **Add from my categories** copie une de vos catégories dans le livre.
- **New category for this book** crée une catégorie qui n'existe que dans ce livre (la cargaison d'un navire, une affinité magique). Une telle catégorie est marquée *Only in this book*. **Save to my categories** l'ajoute à vos catégories, pour que d'autres livres puissent aussi l'utiliser.
- Une copie qui ne correspond plus à votre catégorie est marquée *Differs from your category*. **Update from my categories** reprend votre version. Rien n'est mis à jour sans vous, car une consigne modifiée change le comportement du modèle.
- Modifier une catégorie ne la change que dans ce livre. Son type ne peut pas changer tant que des personnages ont des valeurs pour elle.
- Supprimer une catégorie du livre supprime les valeurs de tous les personnages pour cette catégorie.

<a id="the-state-tab"></a>

## L'onglet État

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **État**. Tout est enregistré dans le livre au fur et à mesure que vous le modifiez.

**Track a character** demande un **nom**, facultativement des **aliases** (les autres noms sous lesquels le personnage est écrit dans le champ *Personnages présents* d'une partie, séparés par des virgules), et les **catégories suivies**. La liste propose les catégories du livre et les vôtres ; choisir une des vôtres la copie dans le livre. Un cheval peut ne suivre que *Injuries*, tandis que l'héroïne suit tout.

Chaque personnage suivi affiche ses catégories, chacune avec sa ou ses valeurs actuelles :

- **+** définit la valeur d'une catégorie à valeur unique ou description (la valeur actuelle passe dans l'historique) ou ajoute une entrée à une liste.
- Le stylo modifie une valeur, la croix la **supprime** : elle n'est plus vraie, elle passe donc dans l'historique et n'est pas envoyée au modèle.
- Le cadenas marque une valeur comme **verrouillée** (une cicatrice, l'épée familiale). Le tidy, qui viendra avec le modèle, laisse les valeurs verrouillées intactes.
- **Historique** affiche les anciennes valeurs de la catégorie et la raison de leur fin (*replaced* ou *removed*). **Restaurer** remet une ancienne valeur à l'état actuel (pour une catégorie à valeur unique ou description, la valeur actuelle passe dans l'historique à sa place). Delete supprime définitivement une valeur.

Le stylo du personnage modifie son nom, ses aliases et ses catégories. Décocher une catégorie la masque mais conserve ses valeurs ; la cocher de nouveau les rétablit. La corbeille arrête le suivi du personnage et supprime tout son état.

<a id="in-the-prompt"></a>

## Dans le prompt

Le bouton en forme d'engrenage contient les réglages du livre :

- **In prompt** insère l'état des personnages suivis de la scène dans le prompt de chaque génération (l'engrenage devient vert lorsqu'il est activé).
- **Depth** et **Rôle** indiquent où il est placé, comme pour la Note de l'auteur : *Depth* est le nombre de messages qui suivent (0 après les consignes du tour, 1 juste avant elles).
- **Name separators** indique comment le champ *Personnages présents* est découpé (par défaut `,` `;` `/` `|` et les retours à la ligne). Les notes entre crochets sont ignorées, donc `Mara (asleep), Fiora` trouve les deux.

Le prompt reçoit les personnages de la scène qui ont quelque chose à dire :

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Vous pouvez placer le même texte n'importe où dans un modèle avec la macro {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} (toute liste de noms fonctionne comme argument). La macro affiche l'état que **In prompt** soit activé ou non ; désactivez-le donc lorsque vous utilisez la macro, sinon le modèle reçoit l'état deux fois.

<a id="commands"></a>

## Commandes

Pour la barre de commandes et pour les agents :

| Commande | Ce que cela fait |
|---|---|
| `state-status` | Les réglages, les catégories du livre et celles de vos catégories dont le livre n'a pas de copie. |
| `list-state-characters` | Les personnages suivis avec leurs catégories. |
| `track-state-character` | Commence à suivre un personnage : nom, aliases, catégories par leur nom. |
| `stop-tracking-state-character` | Arrête de suivre un personnage et supprime son état (demande confirmation d'abord). |
| `list-character-state` | Les valeurs d'un personnage, actuelles et passées, numérotées. |
| `set-character-state` | Définit une valeur : personnage, catégorie, texte. |
| `remove-character-state` | Supprime une valeur par son numéro : elle passe dans l'historique. |
| `state-tracker-block` | Le texte du prompt pour une liste de personnages (la macro). |
