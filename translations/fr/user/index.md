---
label: Guide utilisateur
icon: person
expanded: true
order: 900
---

# Guide utilisateur

Cette partie du manuel s'adresse aux personnes qui écrivent avec Marginalia et à celles qui l'administrent pour d'autres. Elle explique
chaque écran et chaque paramètre, de l'installation de Marginalia et de la connexion d'un modèle à la création de branches dans une histoire, en passant par la construction d'un livre de lore et la sauvegarde de la base de données. Il n'est pas nécessaire de connaître la programmation ; quelques pages consacrées aux modèles vont plus loin pour ceux qui souhaitent affiner ce qui est envoyé au modèle.

Si vous débutez, commencez par [Premiers pas](getting-started/index.md) : vous y passerez de l'installation à la première partie générée d'un livre en quelques minutes.

<a id="how-marginalia-works"></a>

## Fonctionnement de Marginalia

Vous écrivez une histoire avec un grand modèle de langage. Pour chaque **partie** de l'histoire, vous indiquez ce qui doit se passer - la scène, le point de vue, les personnages présents, quelques phrases d'instructions - et le modèle rédige le texte. Avant chaque partie, Marginalia construit le prompt destiné au modèle à partir de :

```mermaid
flowchart LR
    instr["Vos instructions<br/>pour la partie suivante"] --> prompt
    story["L'histoire jusqu'ici<br/>(branche active)"] --> prompt
    sum["Résumés<br/>des parties anciennes"] --> prompt
    lore["Entrées du livre de lore<br/>pertinentes maintenant"] --> prompt
    \style["Prompts du livre<br/>point de vue, temps, style"] --> prompt
    prompt["Prompt<br/>(modèles)"] --> model["Modèle<br/>(fournisseur d'inférence + protocole)"]
    model --> part["Nouvelle partie"]
```

Chacun des éléments de ce schéma peut être ajusté, et le guide utilisateur est organisé selon ce parcours.

<a id="core-concepts"></a>

## Concepts clés

| Concept                | Ce que c'est                                                                                                                                                           | En savoir plus                                            |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| **Livre**              | Une histoire avec tout ce qui lui appartient : parties, prompts, livre de lore, résumés, sauvegardes.                                                                  | [Livres](books/index.md)                                  |
| **Partie**             | Un fragment de l'histoire, en général une réponse du modèle. Vous pouvez la modifier, la régénérer ou la supprimer.                                                  | [Éditeur d'histoire](books/story-editor.md)               |
| **Branche**            | Les parties forment un arbre : vous pouvez écrire une autre version d'une partie ou poursuivre autrement depuis n'importe quelle partie antérieure. La *branche active* est ce que vous voyez et ce que lit le modèle. | [Branches et arbre de l'histoire](books/branches-and-story-tree.md) |
| **Livre de lore**      | Notes sur le monde et les personnages. Chaque **entrée** n'est ajoutée au prompt que lorsqu'elle est pertinente, par exemple lorsque le nom d'un personnage apparaît. | [Livres de lore](lorebooks/index.md)                      |
| **Résumé**             | Une version condensée des parties anciennes, afin qu'un long livre tienne encore dans ce que le modèle peut lire d'un seul coup.                                      | [Résumés](books/summaries.md)                             |
| **Méta-résumé**        | Un résumé de résumés : plusieurs résumés fusionnés en un texte plus court, pour libérer du contexte.                                                                  | [Résumés](books/summaries.md#meta-summaries)              |
| **Modèle**             | Le texte avec des emplacements et des macros à partir duquel le prompt est construit. Chaque livre a ses propres prompts, avec des valeurs par défaut dans *Paramètres*. | [Modèles et macros](templates/index.md)                   |
| **Fournisseur d'inférence** | La connexion à un modèle : adresse de l'API, clé d'API, modèle et ses limites.                                                                                   | [Fournisseurs d'inférence](inference-providers.md)        |
| **Protocole**          | Réglages de génération : température, top P, pénalités et limites facultatives. Le fournisseur indique *quel* modèle écrit, le protocole *comment*.                  | [Protocoles](protocols.md)                                |
| **Dossier**            | Un emplacement pour les livres, imbriqué autant que souhaité. Il peut aussi contenir des réglages (langue, style, prompts) dont héritent les livres qu'il contient.   | [Dossiers](books/folders.md)                              |
| **Mot-clé**            | Une étiquette posée sur les livres et les livres de lore ; les mots-clés déterminent aussi les entrées du livre de lore utilisées par un livre.                       | [Gérer les livres](books/managing-books.md)               |
| **Extension**          | Un module d'extension qui ajoute des fonctionnalités, comme des marqueurs de chapitres ou des relectures par IA.                                                      | [Extensions](extensions/index.md)                         |
| **Commande**           | Une petite action que vous lancez en tapant `/nom` dans la ligne de commande située à côté du menu de l'histoire ; les extensions en ajoutent d'autres.               | [Commandes](commands/index.md)                            |
| **Agent**              | Un LLM qui travaille seul sur un livre, à travers les commandes, sur une copie du livre ; vous examinez ce qu'il a fait, puis vous le reprenez ou le jetez.          | [Agents](agents.md)                                       |

Tout ce que vous créez appartient à votre compte. Les autres utilisateurs de la même instance de Marginalia ne voient ni vos livres, ni vos livres de lore, ni vos fournisseurs, ni vos paramètres - seulement les livres que vous choisissez de [publier](books/publishing.md), ainsi que les livres de lore et les protocoles que vous choisissez de [partager](lorebooks/lorebooks.md#sharing-a-lorebook) (les autres peuvent en importer une copie, [les protocoles](protocols.md#sharing-a-protocol) compris).

<a id="the-workspace"></a>

## L'espace de travail

Après la connexion, vous voyez l'espace de travail. Les onglets à gauche sont les grands espaces de Marginalia, chacun marqué d'une petite icône :

![L'espace de travail](../images/workspace.png)

| Onglet                  |                                                                                                             |
|-------------------------|-------------------------------------------------------------------------------------------------------------|
| **Livres**              | Vos livres. Ouvrez-en un pour écrire. Voir [Livres](books/index.md).                                        |
| **Livres de lore**      | Tous vos livres de lore et leurs entrées. Voir [Livres de lore](lorebooks/index.md).                        |
| **Paramètres**          | Vos valeurs par défaut : modèle, protocole et prompts par défaut des nouveaux livres. Voir [Compte et paramètres](account.md). |
| **Protocoles**          | Réglages de génération. Voir [Protocoles](protocols.md).                                                    |
| **Fournisseurs d'inférence** | Connexions aux modèles. Voir [Fournisseurs d'inférence](inference-providers.md).                       |
| **Ressources**          | Les fichiers que vous avez envoyés, comme des images de parties. Voir [Ressources](resources.md).           |
| **Corbeille**           | Ce que vous avez supprimé, jusqu'à ce qu'un administrateur fasse le ménage. Restaurez-le depuis ici. Voir [Corbeille](trash.md). |
| **Agents**              | (Expérimental) Vos agents, leurs exécutions et les résultats à examiner. Réservé aux utilisateurs autorisés à utiliser des agents. Voir [Agents](agents.md). |

En bas se trouvent **Changer le mot de passe**, **Déconnexion** et, pour les administrateurs, **Administration**. Les extensions peuvent ajouter d'autres onglets et boutons.

Sur un écran étroit (moins de 768 pixels), vous obtenez à la place une [disposition pour téléphone](phone.md), plus simple : les onglets sont dans un tiroir (Administration compris) et les fenêtres sont sur une seule colonne. La disposition est choisie au chargement de la page : après avoir tourné un appareil ou redimensionné la fenêtre, rechargez la page pour obtenir l'autre disposition.

<a id="sections-of-this-guide"></a>

## Parties de ce guide

| Section                                       | Pour                                                                                                                                                                                                                 |
|-----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Premiers pas](getting-started/index.md)      | Installer l'[application de bureau](getting-started/desktop-app.md) ou un [serveur](getting-started/docker-server.md), le [premier démarrage](getting-started/first-start.md) et un [démarrage rapide](getting-started/quick-start.md). |
| [Fournisseurs d'inférence](inference-providers.md) | Connecter des modèles dans le cloud et en local, limites de contexte, modèles de raisonnement.                                                                                                                       |
| [Ressources](resources.md)                    | Les fichiers que vous avez envoyés : les télécharger, les remplacer et les supprimer.                                                                                                                                |
| [Corbeille](trash.md)                         | Livres, livres de lore et autres objets supprimés, et comment les restaurer.                                                                                                                                         |
| [Protocoles](protocols.md)                    | Température, top P, pénalités et limites par protocole.                                                                                                                                                              |
| [Livres](books/index.md)                      | Gérer les livres (cloner, mots-clés, importer depuis une archive), l'éditeur d'histoire (recherche et remplacement), les branches, les prompts du livre, les résumés, les sauvegardes et la publication.              |
| [Livres de lore](lorebooks/index.md)          | Créer des livres de lore, des entrées et comprendre leur activation, import et export (y compris SillyTavern).                                                                                                       |
| [Modèles et macros](templates/index.md)       | Ce qui est envoyé au modèle et comment le modifier ; la référence des macros.                                                                                                                                        |
| [Extensions](extensions/index.md)             | Les extensions fournies : Author's Note, Chapter Marker, correctif d'outils Gemma 4, Google Gemini, Instruction Exporter, Lore Generator, Lorebook VCS, Name generator, Name provider, Place provider, Reviewer, Side Query, Arc Outline, Character Tracker et la localisation chinoise. |
| [Agents](agents.md)                           | (Expérimental) Laisser un modèle travailler seul sur un livre : agents, console, examen et application du résultat, limites.                                                                                         |
| [Commandes](commands/index.md)                | La ligne de commande située à côté du menu de l'histoire : lancer des commandes, écrire les arguments, arrêter une commande longue.                                                                                  |
| [Expressions régulières](regular-expressions.md) | Les expressions régulières de la recherche et du remplacement et des filtres, et la limite de temps.                                                                                                              |
| [Compte et paramètres](account.md)            | Votre mot de passe, votre nom d'utilisateur et vos paramètres par défaut.                                                                                                                                            |
| [Administration](administration/index.md)     | Pour les administrateurs : utilisateurs et leurs droits, sauvegardes de la base de données, nettoyage, installation des extensions, l'assistant d'aide et les agents.                                                |
| [Utiliser Marginalia sur un téléphone](phone.md) | La disposition pour téléphone : en quoi elle diffère de l'ordinateur, ce qui manque, quelles extensions fonctionnent.                                                                                             |
| [Dépannage et FAQ](troubleshooting.md)        | Journaux, problèmes courants et leurs solutions.                                                                                                                                                                     |

## Conventions

- Les noms d'onglets, de boutons et de champs sont en **gras** ou en *italique*, écrits tels qu'ils apparaissent dans Marginalia, par exemple *Administration → Utilisateurs*.
- `~/.marginalia` est le dossier de données dans votre répertoire personnel (`%USERPROFILE%\.marginalia` sous Windows) ; sur un serveur Docker, il s'agit du dossier `./data` situé à côté de `docker-compose.yml`.
- La plupart des modifications sont enregistrées dès que vous les effectuez. L'exception est l'onglet *Paramètres*, qui possède un bouton **Enregistrer**.
