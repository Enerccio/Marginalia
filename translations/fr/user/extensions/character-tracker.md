---
label: Gestionnaire de personnages
order: 19
---

# Gestionnaire de personnages

La distribution de votre livre dans la barre latérale : une liste de personnages avec une accroche, une note, une icône et une couleur, les
relations entre eux, et une carte de ces relations.

<a id="the-characters-tab"></a>

## L'onglet Personnages

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **Personnages** à côté de *Plan*. Au-dessus de la
liste se trouve une rangée de boutons : **Add** (un nouveau personnage), un menu avec l'import des entrées de lore, la **Relationship map**
et un bouton en forme de bulle pour l'invite (survolez-le pour voir les noms).

Le bouton en forme de bulle ouvre un popover avec **In prompt**, **Depth** et un rôle, comme la [Note de l'auteur](authors-note.md) ;
le bouton est vert tant que les personnages sont dans l'invite. Lorsque *In prompt* est coché, chaque génération reçoit les personnages sous forme de message : une ligne avec le nom, l'accroche entre crochets
et la note de chaque personnage, ainsi qu'une liste des relations (`Mara -> Tomas: employs`, `<->` lorsqu'elle va dans les deux sens). *Depth* est le nombre de messages placés après lui (`0` tout à la fin, après vos instructions, `1` juste avant elles) et
le rôle est *System*, *Assistant* ou *User*. Le texte prend ses jetons dans l'espace réservé à l'histoire. Rien n'est inséré
tant que le livre ne contient aucun personnage. Les réglages sont enregistrés avec le livre.

Le bouton baguette situé entre eux donne accès au menu **Import lore entries as characters**, voir
[ci-dessous](#importing-characters-from-the-lorebook).

La liste est classée par nom. Chaque personnage a sa propre couleur et affiche son **nom** avec l'**accroche** en dessous, en plus petit
(deux lignes au maximum). Les boutons d'un personnage :

| Bouton | |
|---|---|
| chevron | Déploie le personnage pour afficher son **icône** et sa **note**. |
| crayon | Ouvre le personnage dans la boîte de dialogue. |
| corbeille | Supprime le personnage et toutes ses relations, après une confirmation. |

Les personnages sont enregistrés avec le livre dès que vous les modifiez, ils sont donc inclus dans ses sauvegardes.

![L'onglet Personnages](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Choisir les personnages pour un tour

Le formulaire du [nouveau tour](../books/story-editor.md) contient les champs **POV character** et **Present characters**. Ce sont des champs de texte libre, mais chacun a un bouton avec une icône de personne à son extrémité, qui affiche la liste des personnages du livre, classés par nom. Un nom choisi dans la liste de **POV character** remplace le contenu du champ ; un nom choisi dans la liste de **Present characters** est ajouté à la fin, après une virgule, sauf s'il s'y trouve déjà. La liste est lue à l'ouverture, donc un personnage que vous venez d'ajouter y figure. Vous pouvez toujours saisir n'importe quel nom, et un nom saisi dans le champ n'a pas besoin d'être un personnage du livre.

<a id="the-character-dialog"></a>

## La boîte de dialogue d'un personnage

**Add** (le bouton avec le plus ; son info-bulle indique *Add new character*) et le crayon ouvrent la même boîte de dialogue :

| Champ | |
|---|---|
| **Nom** | Obligatoire. |
| **Accroche** | Quelques mots sur le personnage, affichés dans la liste et sur la carte. |
| **Note** | Tout ce que vous voulez retenir. |
| **Icône** | Importez un PNG, un JPEG ou un GIF. Elle est réduite à 200 x 200 pixels au maximum, les proportions sont conservées. |
| **Couleur** | Couleur de fond du personnage dans la liste et sur la carte. |

Les champs sont appliqués avec **Enregistrer**.

![Un personnage avec ses relations](../../images/plugin-charactertracker-dialog.png)

Si vous appuyez sur **Annuler** (ou Échap) alors qu'il reste des modifications non enregistrées, y compris une
relation que vous avez remplie mais pas encore ajoutée, on vous demande si vous voulez les abandonner.

![La question posée lorsqu'un personnage ayant des modifications non enregistrées est annulé](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Relations

Lorsque vous ouvrez un personnage existant, la boîte de dialogue liste aussi ses relations : celles qui partent du personnage (`→`),
celles qui pointent vers lui (`←`) et les relations unidirectionnelles (`↔`), chacune avec un nom et une accroche. Le bouton corbeille
supprime une relation.

Pour en ajouter une, recherchez l'autre personnage, choisissez la **Direction** (*this character → other*, *other → this
character* ou *both ways*), saisissez un **Nom** (par exemple *employs*, *friends*) et, si vous le souhaitez, une **Accroche**, puis appuyez sur **Add
relationship** (le bouton se trouve au-dessus de ces champs). Les relations sont enregistrées immédiatement. Un nouveau personnage peut recevoir des relations
une fois enregistré.

<a id="importing-characters-from-the-lorebook"></a>

## Importer des personnages depuis le lore

Si vos personnages figurent déjà dans le lore, le menu baguette les crée pour vous. **Import lore entries as
characters** ouvre une boîte de dialogue avec les entrées du lore du livre et de tous ses sous-lores, classées par leur
*order*. Chacune affiche son nom et une case à cocher, ainsi qu'une icône **i** qui montre le contenu de l'entrée dans un popover, pour un coup d'œil
rapide avant de décider ; la case de l'en-tête de la liste coche ou décoche toutes les entrées. **Import** crée un personnage
portant le nom de chaque entrée cochée (l'accroche, la note, l'icône et la couleur restent vides pour que vous les remplissiez), **Close** quitte
sans rien importer.

Les entrées ne sont pas comparées aux personnages que vous avez déjà : importer deux fois la même entrée crée deux personnages.

![Importer des personnages depuis le lore](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## La carte des relations

**Relationship map** affiche toutes les relations du livre sous forme de graphe. Chaque personnage ayant une relation est un cercle avec son icône (ou une case sans icône) sur sa couleur, avec son nom et son accroche ; chaque relation est une flèche
entre deux personnages, avec son nom et son accroche. Une flèche pointe vers la cible ; une relation unidirectionnelle a une flèche
aux deux extrémités. Les personnages sans relation ne figurent pas sur la carte.

![La carte des relations](../../images/plugin-charactertracker-map.png)

Les personnages sont disposés en anneaux autour d'un personnage — au départ, celui qui a le plus de relations : les
personnages qui lui sont liés se trouvent sur le premier anneau, ceux de ces derniers sur le deuxième, et ainsi de suite. **Double-cliquez sur un personnage** pour
disposer la carte autour de lui. Vous pouvez aussi glisser les personnages, déplacer la carte et zoomer avec la molette de la souris.

<a id="commands"></a>

## Commandes

Les personnages sont aussi des [commandes](../commands/index.md), pour la ligne de commande et pour les [agents](../agents.md). Elles agissent sur
le livre ouvert (pour un agent : sur sa copie de travail), et un onglet Personnages ouvert affiche la modification immédiatement. Un personnage est désigné par son
**nom** (la casse n'a pas d'importance), ou par son **numéro** dans `/list-characters` (l'ordre des noms) lorsque deux
personnages portent le même nom. Une relation est désignée par son numéro dans `/list-character-relations`. Une commande qui modifie un
personnage ne prend que les champs que vous lui donnez ; un texte vide efface une accroche ou une note. L'icône ne peut être définie que
dans la boîte de dialogue.

| Commande | Ce qu'elle fait |
|---|---|
| `/list-characters` | Les personnages classés par nom : accroche, couleur, présence d'une note, nombre de relations. Lecture seule. |
| `/get-character character` | Un personnage avec sa note et ses relations. Lecture seule. |
| `/create-character name [tagline [note [color]]]` | Ajoute un personnage. `color` est au format `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Modifie un personnage. |
| `/delete-character character` | Supprime un personnage avec ses relations, après une confirmation. |
| `/list-character-relations [character]` | Les relations, ou celles d'un seul personnage. Lecture seule. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Ajoute une relation de `source` vers `target` (*employs*), ou, avec `bidirectional` à `true`, une relation dans laquelle ils sont à égalité (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Modifie le nom ou l'accroche d'une relation. |
| `/delete-character-relation relationship` | Supprime une relation, après une confirmation. |
| `/character-tracker-map` | Les personnages et leurs relations, sous la forme de texte que reçoit l'invite. Lecture seule. |

`character-tracker-map` est conçue pour la [macro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands) : placez-la dans le
modèle principal, l'invite utilisateur ou une entrée du lore pour situer les personnages là où vous le voulez dans l'invite, à la place (ou
en plus) de l'insertion automatique de l'onglet Personnages. Elle donne toujours l'état actuel des personnages, que l'insertion soit activée ou non.

Ajouter et modifier ne sont pas destructifs ; seules les commandes `delete` le sont, et un agent peut être configuré pour vous demander confirmation à leur sujet.

<a id="phone"></a>

## Téléphone

L'onglet se trouve dans le tiroir de l'histoire, à côté du plan, comme la [Note de l'auteur](authors-note.md). Les boutons pour les
personnages POV et présents figurent aussi dans le formulaire de nouveau tour du téléphone.
