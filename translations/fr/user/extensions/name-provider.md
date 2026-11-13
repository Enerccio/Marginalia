---
label: Fournisseur de noms
order: 6
---

# Fournisseur de noms

Fournit au modèle de vrais noms parmi lesquels choisir, au lieu d'en inventer : les prénoms et noms de famille les plus courants de nombreux
pays. Là où le [Générateur de noms](name-generator.md) crée de nouveaux noms qui ne font que ressembler à une catégorie, cette
extension fournit des noms qui existent, chacun avec son orthographe d'origine à côté de la forme romanisée : `Satō` et `佐藤`.

L'extension n'a pas d'écran propre et rien à configurer. D'autres extensions proposent ses fonctions au modèle : le
[Générateur de lore](lore-generator.md#tools) et le [Plan de l'arc](arc-outline.md), où vous choisissez les listes
que le modèle peut utiliser. Sa suite s'intitule *Fournisseur de noms* dans la [fenêtre des outils](tools.md), à côté de la suite *Générateur de noms*, et vous pouvez cocher les listes de l'une, de l'autre ou des deux, ou d'aucune.

<a id="what-the-model-can-do"></a>

## Ce que le modèle peut faire

| Fonction | Ce qu'elle fait |
|---|---|
| `list_real_name_lists` | Liste les listes de noms ; avec un `filter`, seulement celles dont le nom contient ses mots. |
| `pick_real_names` | Choisit au hasard jusqu'à 100 noms différents d'une liste. Chaque nom est une paire : le nom romanisé et le nom d'origine. |

Une liste comptant moins de noms que demandé renvoie tous ses noms. Le modèle reçoit la consigne (voir l'indication de la suite *Fournisseur de noms* dans la [fenêtre des outils](tools.md#choosing-the-tools)) de choisir un nom parmi ceux qu'il obtient et de l'écrire tel qu'il a été renvoyé, avec l'original à côté lorsque cela convient au cadre ; il peut associer un prénom à un nom de famille du même pays.

<a id="commands"></a>

## Commandes

Les mêmes noms sont accessibles sous forme de [commandes](../commands/index.md), pour la ligne de commande et pour les [agents](../agents.md), qui peuvent les utiliser dans leurs propres prompts (par exemple pour `/generate-lore-entry` du [Générateur de lore](lore-generator.md)) :

| Commande | Ce qu'elle fait |
|---|---|
| `/list-real-name-lists [<filter>]` | Liste les listes sous forme de tableau JSON ; avec un filtre, seulement celles dont le nom contient tous ses mots (chacun au début d'un mot, dans n'importe quel ordre, sans tenir compte de la casse). |
| `/pick-real-names list count` | Choisit au hasard jusqu'à 100 noms de la liste et les renvoie sous forme de tableau JSON de paires, le nom romanisé et le nom d'origine. |

Les deux commandes ne font que lire.

<a id="lists"></a>

## Listes

Il existe environ 180 listes, nommées `<Pays> - Surnames` et `<Pays> - Forenames` : les noms de famille de 75 pays et les prénoms de 106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). Les prénoms des deux genres figurent dans une même liste. Une liste compte de 5 à environ 150 noms, les plus courants du pays. Les noms ne sont pas inventés : un nom que vous obtenez est un nom que des personnes portent réellement.

Une trentaine d'entrées sont des **groupes** de listes par langue : `<Langue> - Forenames` et `<Langue> - Surnames` pour l'espagnol (18 pays), le portugais, le français, l'allemand, l'anglais, l'arabe, les langues slaves de l'Ouest, de l'Est et du Sud, les langues nordiques germaniques, les langues nordiques, les langues ouraliennes, les langues baltes, les langues turciques, le persan, le chinois, l'Asie du Sud, l'Asie du Sud-Est, le Caucase, l'albanais et le roumain (lorsque de telles listes existent). Ils sont listés comme n'importe quelle liste et constituent un seul élément dans la [fenêtre des outils](tools.md). Chaque nom d'un groupe est tiré au hasard d'une de ses listes, choisie au hasard, chaque pays ayant la même chance ; une demande de 20 noms donne donc des noms de plusieurs pays, jamais le même nom deux fois. Les pays où plusieurs langues sont parlées (Belgique, Luxembourg, Malte, Andorre) ne figurent dans aucun groupe. Un groupe correspond à une ligne du fichier `name-groups.json` ; voir le [README de l'extension](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups).

Les données proviennent du [jeu de données des noms populaires par pays](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), qui les reprend des listes de Wikipédia des noms de famille les plus courants et des prénoms les plus populaires. Les noms écrits dans des écritures non latines (chinois, japonais, coréen, arabe, cyrillique...) disposent d'une romanisation, soit celle de la source, soit une romanisation produite par traduction automatique ; vérifiez donc l'orthographe lorsqu'elle compte.

<a id="usage"></a>

## Utilisation

Chargez l'extension dans *Admin → Extensions*. Elle s'applique à la génération suivante, y compris lorsqu'elle est installée après les extensions qui l'utilisent, ou déchargée puis réinstallée. Sans elle et sans les autres suites, il n'y a pas de bouton *Outils*.
