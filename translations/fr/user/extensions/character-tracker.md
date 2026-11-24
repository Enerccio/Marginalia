---
label: Suivi des personnages
order: 19
---

# Suivi des personnages

La liste des personnages de votre livre dans la barre latérale : une liste de personnages avec une accroche, une note, une icône et une couleur, les relations entre eux, et une carte de ces relations.

<a id="the-characters-tab"></a>

## L'onglet Characters

La barre latérale gauche de l'[éditeur de récit](../books/story-editor.md) reçoit un onglet **Characters** à côté de *Outline*. Au-dessus de la liste se trouve une ligne de boutons : **Add** (un nouveau personnage), un menu pour importer des entrées du lorebook, la **Relationship map** et un bouton en forme de bulle de dialogue pour le prompt (survolez-le pour voir les noms).

Le bouton en forme de bulle ouvre un popover avec **In prompt**, **Depth** et un rôle, comme pour la [Author's Note](authors-note.md) ; le bouton est vert tant que les personnages sont dans le prompt. Lorsque *In prompt* est coché, chaque génération reçoit les personnages sous forme de message : une ligne par personnage avec son nom, son accroche entre crochets et sa note, puis la liste des relations (`Mara -> Tomas: employs`, `<->` lorsqu'elle va dans les deux sens). *Depth* est le nombre de messages après celui-ci (`0` tout à la fin, après vos instructions, `1` juste avant elles) et le rôle est *System*, *Assistant* ou *User*. Le texte prend ses tokens sur l'espace réservé à l'histoire. Rien n'est inséré tant que le livre n'a pas de personnages. Les réglages sont enregistrés avec le livre.

Le bouton baguette situé entre les deux contient l'élément de menu **Import lore entries as characters**, voir [ci-dessous](#importing-characters-from-the-lorebook).

La liste est triée par nom. Chaque personnage a sa propre couleur et affiche son **name**, suivi du **tagline** en plus petit (deux lignes au maximum). Les boutons d'un personnage :

| Bouton | |
|---|---|
| chevron | Déplie le personnage pour afficher son **icon** et sa **note**. |
| stylo | Ouvre le personnage dans la boîte de dialogue. |
| corbeille | Supprime le personnage et toutes ses relations, après confirmation. |

Les personnages sont enregistrés avec le livre dès que vous les modifiez ; ils sont donc inclus dans ses sauvegardes.

![L'onglet Characters](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Choisir des personnages pour un tour

Le formulaire du [nouveau tour](../books/story-editor.md) contient les champs **POV character** et **Present characters**. Ce sont toujours des champs de texte libre, mais chacun a, à son extrémité, un bouton avec une icône de personne qui affiche les personnages du livre, triés par nom. Un nom choisi dans la liste de **POV character** remplace le contenu du champ ; un nom choisi dans la liste de **Present characters** est ajouté à la fin, après une virgule, sauf s'il s'y trouve déjà. La liste est lue à l'ouverture, donc un personnage ajouté il y a un instant y figure. Vous pouvez toujours saisir n'importe quel nom ; un nom présent dans le champ n'a pas besoin d'être un personnage du livre.

<a id="the-character-dialog"></a>

## La boîte de dialogue du personnage

**Add** (le bouton avec le plus ; son info-bulle indique *Add new character*) et le stylo ouvrent la même boîte de dialogue :

| Champ | |
|---|---|
| **Name** | Obligatoire. |
| **Tagline** | Quelques mots sur le personnage, affichés dans la liste et sur la carte. |
| **Note** | Tout ce que vous voulez retenir. |
| **Icon** | Importez un PNG, un JPEG ou un GIF. Il est réduit à 200 x 200 pixels au maximum, les proportions sont conservées. |
| **Color** | Couleur de fond du personnage dans la liste et sur la carte. |

Les champs sont appliqués avec **Save**.

![Un personnage avec ses relations](../../images/plugin-charactertracker-dialog.png)

Si vous appuyez sur **Cancel** (ou Esc) alors qu'il reste des modifications non enregistrées, y compris une relation que vous avez remplie mais pas encore ajoutée, on vous demande si vous voulez les abandonner.

![La question posée lorsqu'on annule un personnage avec des modifications non enregistrées](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Relations

Lorsque vous ouvrez un personnage existant, la boîte de dialogue affiche aussi ses relations : celles qui partent du personnage (`→`), celles qui y arrivent (`←`) et les relations à sens unique (`↔`), chacune avec un nom et une accroche. Le bouton corbeille supprime une relation.

Pour en ajouter une, recherchez l'autre personnage, choisissez la **Direction** (*this character → other*, *other → this character* ou *both ways*), saisissez un **Name** (par ex. *employs*, *friends*) et, si vous le souhaitez, un **Tagline**, puis appuyez sur **Add relationship** (le bouton est au-dessus de ces champs). Les relations sont enregistrées immédiatement. Un nouveau personnage ne peut recevoir de relations qu'après avoir été enregistré.

<a id="importing-characters-from-the-lorebook"></a>

## Importer des personnages depuis le lorebook

Si vos personnages figurent déjà dans le lorebook, le menu baguette les crée pour vous. **Import lore entries as characters** ouvre une boîte de dialogue avec les entrées du lorebook du livre et de tous ses sous-lorebooks, triées par leur *order*. Chacune affiche son nom et une case à cocher, ainsi qu'une icône **i** qui montre le contenu de l'entrée dans un popover, pour un coup d'œil rapide avant de décider ; la case de l'en-tête de la liste coche ou décoche toutes les entrées. **Import** crée un personnage portant le nom de chaque entrée cochée (l'accroche, la note, l'icône et la couleur restent vides pour que vous les complétiez), **Close** ferme sans importer.

Les entrées ne sont pas comparées aux personnages existants : importer deux fois la même entrée crée deux personnages.

![Importer des personnages depuis le lorebook](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## La carte des relations

**Relationship map** affiche toutes les relations du livre sous forme de graphe. Chaque personnage ayant une relation est un cercle portant son icône (ou un carré sans icône) sur sa couleur, avec son nom et son accroche ; chaque relation est une flèche entre deux personnages, avec son nom et son accroche. Une flèche pointe vers la cible ; une relation à sens unique a une flèche aux deux extrémités. Les personnages sans relation n'apparaissent pas sur la carte.

![La carte des relations](../../images/plugin-charactertracker-map.png)

Les personnages sont disposés en anneaux autour d'un personnage : au départ celui qui a le plus de relations. Les personnages qui lui sont liés se trouvent sur le premier anneau, ceux qui leur sont liés sur le second, et ainsi de suite. **Double-cliquez sur un personnage** pour disposer la carte autour de lui. Vous pouvez aussi déplacer les personnages, déplacer la carte et zoomer avec la molette de la souris.

<a id="commands"></a>

## Commandes

Les personnages sont aussi accessibles par des [commandes](../commands/index.md), pour la ligne de commande et pour les [agents](../agents.md). Elles agissent sur le livre ouvert (pour un agent : sur sa copie de travail), et un onglet Characters ouvert affiche le changement immédiatement. Un personnage est désigné par son **name** (la casse n'a pas d'importance), ou par son numéro dans `/list-characters` (l'ordre des noms) lorsque deux personnages ont le même nom. Une relation est désignée par son numéro dans `/list-character-relations`. Une commande qui modifie un personnage ne prend que les champs que vous lui donnez ; un texte vide efface une accroche ou une note. L'icône ne peut être définie que dans la boîte de dialogue.

| Commande | Ce qu'elle fait |
|---|---|
| `/list-characters` | Les personnages triés par nom : accroche, couleur, présence d'une note, nombre de relations. Lecture seule. |
| `/get-character character` | Un personnage avec sa note et ses relations. Lecture seule. |
| `/create-character name [tagline [note [color]]]` | Ajoute un personnage. `color` vaut `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Modifie un personnage. |
| `/delete-character character` | Supprime un personnage avec ses relations, après confirmation. |
| `/list-character-relations [character]` | Les relations, ou celles d'un personnage. Lecture seule. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Ajoute une relation de `source` vers `target` (*employs*), ou avec `bidirectional` `true` une relation où les deux personnages sont à égalité (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Modifie le nom ou l'accroche d'une relation. |
| `/delete-character-relation relationship` | Supprime une relation, après confirmation. |
| `/character-tracker-map` | Les personnages et leurs relations sous forme de texte que reçoit le prompt. Lecture seule. |

`character-tracker-map` est prévu pour la [macro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands) : placez-la dans le modèle maître, dans le prompt utilisateur ou dans une entrée du lorebook pour placer les personnages où vous le souhaitez dans le prompt, à la place de (ou en plus de) l'insertion automatique de l'onglet Characters. Elle reflète toujours l'état actuel des personnages, que l'insertion soit activée ou non.

Ajouter et modifier ne sont pas destructifs ; seules les commandes `delete` le sont, et un agent peut être réglé pour vous demander confirmation avant de les exécuter.

L'[assistant d'intrigue](arc-outline.md) de l'Arc Outline utilise les mêmes commandes lorsque vous cochez ses outils **Characters**, afin de développer la distribution pendant que vous parlez de l'intrigue.

<a id="phone"></a>

## Téléphone

L'onglet se trouve dans le tiroir du récit, à côté du plan, comme la [Author's Note](authors-note.md). Les boutons pour le POV et les personnages présents se trouvent aussi dans le formulaire du nouveau tour sur le téléphone.
