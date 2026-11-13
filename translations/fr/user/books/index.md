---
label: Livres
order: 960
icon: book
---

# Livres

Un livre correspond à une histoire. Vous donnez des consignes partie par partie, le modèle rédige le texte, et le livre
conserve tout ce qui s'y rattache : l'arbre de l'histoire avec toutes ses branches, les prompts qui façonnent le style, le
lorebook avec le monde et les personnages, les résumés et les sauvegardes.

![Un livre dans l'éditeur d'histoire](../../images/hero-story-editor.png)

<a id="parts-and-branches"></a>

## Parties et branches

Un livre est composé de **parties**. Chaque partie est une réponse du modèle, rédigée à partir des consignes que vous
avez données pour elle (scène, personnage point de vue, personnages présents et ce qui doit se passer).

Les parties forment un arbre. Le plus souvent, vous poursuivez simplement l'histoire, mais vous pouvez aussi réécrire
une autre version de la dernière partie, ou revenir à une partie antérieure et continuer autrement. Chaque chemin allant
du début jusqu'à une fin est une **branche**. La branche sur laquelle vous travaillez est la **branche active** ; l'éditeur
d'histoire l'affiche, et seules ses parties sont envoyées au modèle. Voir
[Branches et arbre de l'histoire](branches-and-story-tree.md).

<a id="the-book-window"></a>

## La fenêtre du livre

L'ouverture d'un livre affiche sa fenêtre avec ces onglets :

| Onglet | |
|---|---|
| **À propos** | Nom, étiquettes, description, langue, modèle, protocole, publication et nombre de mots. Voir [Gérer les livres](managing-books.md). |
| **Prompts** | Les modèles et réglages de style de ce livre. Voir [Prompts du livre](prompts.md). |
| **Lorebook** | Le lorebook de ce livre et ses entrées. Lorsqu'il possède des sous-lorebooks, la seconde case **Lorebook affiché** permet de faire passer l'onglet sur l'un d'eux (et de revenir) sans modifier le lorebook du livre ; cliquer sur le nom d'un sous-lorebook dans **Sous-lorebooks** a le même effet. Voir [Lorebooks](../lorebooks/index.md). |
| **Histoire** | L'éditeur d'histoire, où les parties sont rédigées et générées. Voir [Éditeur d'histoire](story-editor.md). |
| **Vue des branches** | L'arbre de l'histoire avec toutes les branches. Voir [Branches et arbre de l'histoire](branches-and-story-tree.md). |
| **Sauvegardes** | Les sauvegardes du livre. Voir [Sauvegardes du livre](backups.md). |

Ce qui s'est passé dans un livre jour après jour (mots, temps, jetons, taille) ainsi que le calendrier des jours où vous
avez écrit se trouvent dans sa fenêtre **Statistiques**, voir
[Statistiques d'écriture](statistics.md).

L'histoire peut être sortie de Marginalia sous forme de fichier depuis le menu des réglages de l'onglet *Histoire*, voir
[Exporter un livre](export.md).

Un livre sans parties s'ouvre sur *À propos*, un livre avec des parties s'ouvre sur *Histoire*. Toutes les modifications
sont enregistrées immédiatement ; il n'existe pas de bouton *Enregistrer*. **Quitter** ferme le livre.

Pendant la génération d'une partie, les autres onglets et *Quitter* sont désactivés jusqu'à la fin de la génération ou
jusqu'à son arrêt.

<a id="in-this-section"></a>

## Dans cette section

- [Gérer les livres](managing-books.md) - la liste des livres, créer, modifier et supprimer des livres
- [Dossiers](folders.md) - classer les livres dans des dossiers, réglages hérités par les livres, déplacer et copier
- [Éditeur d'histoire](story-editor.md) - rédiger les consignes, générer et modifier les parties
- [Branches et arbre de l'histoire](branches-and-story-tree.md) - régénérer, faire défiler, créer une branche, la vue en arbre
- [Historique des versions](version-history.md) - les versions antérieures d'une partie : comparer, restaurer, nommer, rechercher
- [Prompts du livre](prompts.md) - modèles, point de vue, temps verbal et style
- [Résumés](summaries.md) - garder les longs livres dans le contexte du modèle
- [Exporter un livre](export.md) - l'histoire en texte, Markdown, HTML, Word, PDF ou EPUB
- [Sauvegardes du livre](backups.md) - sauvegardes, restauration, copies d'un livre
- [Publication et lecteur](publishing.md) - vue de lecture et partage d'un livre
