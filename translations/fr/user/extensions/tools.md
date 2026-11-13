---
label: Outils pour le modèle
order: 0
---

# Outils pour le modèle

Certaines extensions donnent au modèle des **outils** : des fonctions qu'il peut appeler pendant qu'il écrit, pour obtenir des noms, chercher de vrais lieux, et
ainsi de suite, au lieu de les inventer. Les outils d'une extension forment une **suite** ; les suites disponibles sont :

| Suite | Extension | Ce que le modèle peut faire avec |
|---|---|---|
| Générateur de noms | [Générateur de noms](name-generator.md) | Obtenir des noms inventés pour ressembler à une catégorie (lieux, divinités, noms de famille d'un pays...). |
| Fournisseur de noms | [Fournisseur de noms](name-provider.md) | Obtenir de vrais noms avec leur orthographe d'origine. |
| Fournisseur de lieux | [Fournisseur de lieux](place-provider.md) | Chercher de vrais lieux, ce qui les entoure et la distance qui les sépare. |

Une boîte de dialogue qui fait tourner le modèle avec des outils comporte un bouton **Outils** : les générateurs du [Générateur de lore](lore-generator.md) et de
l'[Plan de l'arc](arc-outline.md) (pas leur import d'histoire, dont les noms sont déjà dans l'histoire). Le bouton n'apparaît que
lorsqu'au moins une suite est installée, et lorsque vous avez coché quelque chose il indique combien d'éléments sont activés : **Outils (3)**.

<a id="choosing-the-tools"></a>

## Choisir les outils

**Outils** ouvre une fenêtre avec une section pour chaque suite, sous le nom de la suite, avec le nombre d'éléments cochés dans celle-ci.
Une section comportant des éléments cochés s'ouvre par défaut. Le contenu d'une section dépend de la suite :

- Une suite avec des **éléments** (le Générateur de noms et le Fournisseur de noms en ont un pour chaque catégorie ou liste de noms) affiche un tableau
  avec une case à cocher par ligne et un **champ de filtre qui lui est propre** au-dessus : tapez une partie d'un nom pour n'afficher que les
  éléments correspondants. Les éléments cochés restent toujours dans le tableau, quel que soit le filtre. **Cocher ceux affichés** coche tous les
  éléments que le tableau affiche à ce moment-là, **Décocher ceux affichés** les décoche, et une ligne en dessous indique combien sont cochés. Le modèle ne reçoit
  que les éléments cochés : il peut les lister et demander des noms parmi eux, et aucun autre.
- Une suite **sans éléments** (le Fournisseur de lieux) est active ou inactive : **Utiliser cette suite**.

![La fenêtre Outils avec les catégories du Générateur de noms filtrées par « Japan »](../../images/tools-dialog.png)

Chaque section comporte aussi une **Indication pour le modèle**. Il s'agit d'un court texte ajouté à la fin de l'invite lorsque le modèle reçoit la
suite, qui lui indique quand et comment utiliser les outils (pour les noms : appeler les outils au lieu d'inventer des noms,
choisir parmi ce qui est renvoyé, conserver un nom réel tel qu'il a été renvoyé). Le texte intégré s'affiche en gris dans le champ ; écrivez le vôtre pour le remplacer,
laissez le champ vide pour utiliser celui par défaut.

![Une suite active, avec son indication pour le modèle ouverte ; les deux autres sections fermées avec le nombre d'éléments cochés](../../images/tools-hint.png)

Le modèle reçoit les suites qui ont au moins un élément coché : aucun élément coché dans aucune d'elles, aucun outil. Le choix est enregistré avec
votre compte, il reste donc le même la fois suivante ; le Générateur de lore enregistre aussi le choix avec chaque entrée qu'il a générée, et
ouvre cette entrée avec ce choix. Une suite qui a été déchargée est ignorée, et un élément coché que l'extension ne possède plus est
ignoré. Les extensions peuvent être chargées et déchargées pendant que Marginalia tourne ; le bouton suit.

Si deux suites possédaient une fonction du même nom, la génération ne démarrerait pas et indiquerait laquelle ; les suites livrées avec
Marginalia n'ont pas ce problème.

<a id="as-commands"></a>

## En tant que commandes

Les suites sont aussi des commandes de la [barre de commandes](../commands/index.md) et des [agents](../agents.md) (`/pick-real-names`,
`/generate-names`, `/search-places`...), de sorte que ce que reçoit le modèle ici et ce qu'un agent peut appeler sont la même suite, sous le même
nom.

<a id="from-an-older-version"></a>

## Depuis une version antérieure

Le bouton **Name tools** est devenu **Outils**. Les catégories que vous aviez cochées le sont dans les suites du Générateur de noms et du
Fournisseur de noms, et les indications que vous aviez écrites dans *Paramètres → Paramètres des extensions* (*Name generator hint*, *Name provider hint*) sont
désormais les indications de ces deux suites dans cette fenêtre ; les champs des paramètres ont disparu.
