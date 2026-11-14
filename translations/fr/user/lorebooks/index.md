---
label: Livres de lore
order: 950
icon: archive
---

# Livres de lore

!!!
La [mise en page téléphone](../phone.md#lorebooks) propose le même éditeur sur une seule colonne : les boutons des livres de lore sont dans un menu, les réglages du
livre de lore se trouvent dans une section qui s'ouvre, et une entrée s'ouvre dans un formulaire. Seuls quelques éléments restent sur l'ordinateur, voir la liste ci-dessous.
!!!

Un livre de lore contient ce que le modèle doit savoir sur votre monde : personnages, lieux, histoire, règles, terminologie. Chaque
élément de connaissance est une **entrée**. Avant chaque partie générée, Marginalia décide quelles entrées sont pertinentes et les place
dans le prompt, si bien que le modèle sait qui est le capitaine Varro lorsqu'il entre dans la scène, sans que chaque entrée occupe de la place dans chaque prompt.

![L'éditeur de livres de lore](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## Comment les pièces s'articulent

```
Book ──uses──▶ Lorebook "Saga" ──sub lorebooks──▶ "Characters", "Places", "Magic"
                   │                                   │
                 entries                             entries
```

- Un **livre** utilise un livre de lore (dans son onglet *Lorebook*).
- Un livre de lore peut en inclure d'autres comme **sous-livres de lore**. Leurs entrées participent elles aussi, de sorte que vous pouvez garder les éléments communs (un monde utilisé par plusieurs livres, une distribution de personnages) dans leur propre livre de lore et l'inclure là où il faut.
- Chaque **entrée** contient le texte qui va dans le prompt, ainsi que les conditions qui déterminent quand elle est utilisée.

<a id="when-an-entry-is-used"></a>

## Quand une entrée est utilisée

Pour chaque partie générée, une entrée est **activée** lorsque toutes ces conditions sont réunies :

1. l'entrée et son livre de lore sont activés ;
2. ses **étiquettes** correspondent aux étiquettes du livre (si elle en a) ;
3. son **filtre** correspond aux instructions de la partie (s'il en a un) ou, lorsque l'entrée regarde dans l'histoire, les parties les plus récentes de l'histoire également.

Les entrées activées sont insérées selon leur **ordre** - dans le bloc de lore du prompt système, ou juste avant la demande de la partie. Voir [Entrées et activation](entries.md).

Les entrées sans étiquettes et sans filtre sont toujours actives : utilisez-les pour ce que le modèle doit toujours savoir.

<a id="lorebooks-and-other-features"></a>

## Livres de lore et autres fonctionnalités

- Les entrées de livre de lore peuvent utiliser des [modèles et macros](../templates/index.md) - les informations de monde de SillyTavern continuent de fonctionner.
- Les [informations de monde SillyTavern](import-export.md#importing-sillytavern-world-info) peuvent être importées.
- Les [sauvegardes de livres](../books/backups.md) contiennent le livre de lore du livre avec ses sous-livres de lore.
- L'extension [Lore Generator](../extensions/lore-generator.md) rédige le contenu d'une entrée avec le modèle, à partir du lore que vous avez déjà.
- L'extension [Lorebook VCS](../extensions/lorebook-vcs.md) conserve un historique des révisions des entrées.

<a id="in-this-section"></a>

## Dans cette section

- [Créer des livres de lore](lorebooks.md) - l'éditeur de livres de lore, les sous-livres de lore, utiliser un livre de lore dans un livre
- [Entrées et activation](entries.md) - champs des entrées, étiquettes, filtres, ordre et insertion
- [Import et export](import-export.md) - fichiers de livres de lore Marginalia et informations de monde SillyTavern
