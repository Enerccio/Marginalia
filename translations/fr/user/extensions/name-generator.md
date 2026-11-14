---
label: Générateur de noms
order: 7
---

# Générateur de noms

Donne au modèle un moyen de demander des noms au lieu de les inventer. Les noms proviennent de listes de vrais noms - prénoms et noms de famille de nombreux pays, lieux, divinités, monstres, drogues, entreprises, aliments, et plus encore - et sont recréés par une chaîne de Markov, de sorte qu'ils sonnent comme la catégorie sans en être des copies.

L'extension n'a pas d'écran propre et rien à configurer. D'autres extensions offrent ses fonctions au modèle ; pour l'instant, il s'agit du [Générateur de lore](lore-generator.md#tools) et du [Plan d'arc](arc-outline.md), où vous choisissez les catégories que le modèle peut utiliser (voir [Outils pour le modèle](tools.md)) ; sa suite s'appelle *Générateur de noms*, à côté du *Fournisseur de noms* du [Fournisseur de noms](name-provider.md), qui fournit de vrais noms au lieu de noms inventés.

![Choix des catégories de noms dans la fenêtre Outils du Générateur de lore](../../images/tools-dialog.png)

<a id="what-the-model-can-do"></a>

## Ce que le modèle peut faire

| Fonction | Ce qu'elle fait |
|---|---|
| `list_name_categories` | Liste les catégories de noms ; avec un `filter`, seulement celles dont le nom contient ses mots. |
| `generate_names` | Crée jusqu'à 100 nouveaux noms d'une catégorie. Optionnellement avec une longueur minimale et maximale. |

Sans longueur, les noms ont la longueur habituelle des noms de cette catégorie (une catégorie de longs noms d'entreprises donne de longs noms, une catégorie de prénoms courts donne de petits noms). Le modèle peut demander d'autres longueurs entre 2 et 30 lettres. Une petite catégorie peut renvoyer moins de noms que demandé, car la chaîne finit par manquer de noms différents. Le modèle est libre de choisir l'un des noms, de le modifier ou de combiner deux d'entre eux ; voir l'aide de la suite *Générateur de noms* dans la [fenêtre des outils](tools.md#choosing-the-tools).

<a id="commands"></a>

## Commandes

Les mêmes noms sont disponibles sous forme de [commandes](../commands/index.md), pour la ligne de commande et pour les [agents](../agents.md), qui peuvent les utiliser dans leurs propres invites (par exemple pour `/generate-lore-entry` du [Générateur de lore](lore-generator.md)) :

| Commande | Ce qu'elle fait |
|---|---|
| `/list-name-categories [<filter>]` | Liste les catégories sous forme de tableau JSON ; avec un filtre, seulement celles dont le nom contient tous ses mots (chacun en début de mot, dans n'importe quel ordre, sans tenir compte de la casse : `chad male` trouve *Africa - Central Africa - Chad - Male* mais pas *…Female*). |
| `/generate-names category count [min_length [max_length]]` | Crée jusqu'à 100 noms de la catégorie et les renvoie sous forme de tableau JSON. Des noms différents à chaque fois. |

Les deux commandes ne font que lire.

<a id="categories"></a>

## Catégories

Il existe environ 1270 catégories. Leurs noms indiquent ce qu'elles contiennent :

- **Prénoms et noms de famille du monde**, tirés de l'Onomastikon de Kate Monk : `Europe Western - France - Surnames`,
  `Ancient World - Egypt - Male`, `India - Hindu Gods - Goddesses`. Plusieurs centaines d'entre elles.
- **Noms de famille de 75 pays**, les noms de famille romanisés du [jeu de données des noms populaires par pays](https://github.com/sigpwned/popular-names-by-country-dataset)
  (CC0) : `Armenia - Surnames`, `Japan - Surnames`, `Kosovo - Surnames`. Ce sont de petites listes (de 6 à environ 150 noms), donc les
  chaînes restent courtes et les nouveaux noms sont proches des vrais ; pour les vrais noms eux-mêmes, utilisez le
  [Fournisseur de noms](name-provider.md).
- **Listes issues d'autres collections** : dieux, anges, démons, villes et États antiques, empereurs, planètes (fantasygen),
  Corpora (animaux, tarot, noms tolkiens, maisons de *Game of Thrones*, drogues, hôpitaux, tissus, pierres précieuses, langages de programmation...) et markov-namegen-lib (Pokémon, dragons, cocktails, banques, noms de lieux romains...).

<a id="groups"></a>

### Groupes

Environ 170 entrées sont des **groupes** de catégories. Ils rassemblent les nombreuses petites listes d'une langue, de sorte que vous demandez
`German - Male` au lieu de choisir parmi *Germany - Germanic - Male*, *Germany - Biblical - Male*, *Austria - First names - Male*
et les autres. Ils figurent dans la liste comme n'importe quelle catégorie, et un groupe est un élément de la [fenêtre des outils](tools.md). Les groupes :

- **Par langue**, chacun décliné en `- Male`, `- Female` et `- Surnames` là où de telles listes existent : allemand, français, espagnol (les
  noms de famille incluent l'Amérique latine), italien, portugais, néerlandais, scandinave, finnois, baltique, irlandais, écossais, gallois, celtique (toutes
  les langues), slave occidental, oriental et méridional, grec, hongrois, roumain, albanais, caucasien, turc, persan, arabe, juif, hindou
  (les quatre listes alphabétiques de prénoms masculins réunies en une seule), sud-asiatique, sud-est asiatique, malayo-indonésien, polynésien et les régions
  africaines (ouest, est, sud, nord, centre). `Chinese`, `Japanese`, `Korean` et `Vietnamese` ont `- Surnames`.
- **Tous genres** : `<Language> - Forenames` (`German - Forenames`, `Spanish - Forenames`, `Celtic - Forenames`...) regroupe les listes
  masculines et féminines de la langue ainsi que ses listes de noms pour les deux genres, de sorte qu'un appel renvoie des noms d'hommes et de femmes
  mélangés (les proportions suivent la taille des listes, le plus souvent entre la moitié et les trois quarts d'hommes). Le chinois, le japonais, le coréen et le vietnamien
  en disposent également. Les langues où le mélange serait déséquilibré (turc, sud-asiatique, Afrique de l'Ouest et du Sud, malayo-indonésien) n'ont que
  les groupes masculin et féminin.
- **Du passé** : anglo-saxon, germanique médiéval, anglais médiéval, Proche-Orient ancien.
- **Amérindien** : `Native American - Male`, `- Female` et `- Surnames` (les catégories nord-américaines de l'Onomastikon :
  Apache, Sioux, Iroquois, Cherokee, Inuit...) ainsi que la même chose pour l'Amérique centrale et l'Amérique du Sud (Aztèque, Maya, Inca, Amazonien).
- **Autres** : `English - Surnames` (patronymes, métiers, lieux...), `Saints`, `Deities`, `Fantasy Creatures`, `Game of Thrones Houses`.

Pour chaque nom, l'extension choisit d'abord au hasard l'une des catégories du groupe, puis crée le nom avec la
chaîne de cette catégorie (avec ses longueurs habituelles). Un nom sonne comme un seul peuple et une seule langue au lieu d'un mélange de tous, et un seul appel renvoie des noms de plusieurs d'entre eux. Les listes des pays, qui sont divisées par origine ou par lettre, sont choisies selon leur taille (une liste de 600 noms est choisie plus souvent qu'une liste de 10) ; dans les groupes de langues et de pays entiers (turc, caucasien, baltique, polynésien, régions africaines, amérindien...), chaque membre a la même chance. Les sources de certains groupes sont courtes et en partie des traductions anglaises (*Calf-Woman*), donc les noms sont approximatifs.
Un groupe correspond à une ligne de `name-groups.json` (les membres et la manière dont ils sont choisis), voir le [README de l'extension](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md#groups).

Chaque catégorie possède une chaîne de la longueur qui lui convenait le mieux (les listes courtes ont des chaînes courtes, qui répètent moins les originaux) et les longueurs que ses noms ont habituellement. Les listes elles-mêmes ne font pas partie de Marginalia : seules les probabilités y figurent, de sorte que les noms d'origine ne peuvent pas être relus à partir de l'extension. Certaines sources ont leurs propres licences, voir le [README de l'extension](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md).

<a id="usage"></a>

## Utilisation

Chargez l'extension dans *Admin → Extensions*. Elle s'applique à la prochaine génération ; les extensions qui l'utilisent la trouvent lorsqu'elle est chargée, y compris si elle est installée après elles ou désinstallée puis réinstallée - inutile de redémarrer le Générateur de lore. Sans elle et sans les autres suites, le Générateur de lore n'a pas de bouton *Outils*.
