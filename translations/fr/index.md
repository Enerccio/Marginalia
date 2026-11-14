---
label: Accueil
icon: home
order: 1000
---

# Marginalia

Marginalia est un outil d'écriture auto-hébergé pour la fiction longue, co-écrite avec de grands modèles de langage. Vous rédigez les instructions de chaque partie de l'histoire, le modèle écrit la prose, et Marginalia garde tout ce qui l'entoure en ordre : l'arbre narratif ramifié, le lore du monde, les résumés qui permettent de faire tenir les longs livres dans le contexte du modèle, et les réglages de prompt qui façonnent le style.

Il fonctionne dans votre navigateur - comme application de bureau sur votre propre ordinateur, ou comme petit serveur pour vous et vos amis - et fonctionne avec toute API compatible OpenAI : OpenAI, OpenRouter, LiteLLM, llama.cpp, LM Studio, Ollama, vLLM, et d'autres.

![L'éditeur d'histoire](images/hero-story-editor.png)

[!button variant="primary" text="Commencer"](user/getting-started/index.md)
[!button text="Télécharger"](https://github.com/Enerccio/Marginalia/releases)
[!button text="Code source sur GitHub"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## Ce que it fait

- **Livres à histoires ramifiées** – régénérer ou parcourir une partie, se ramifier depuis n'importe quel point, se déplacer dans l'arbre de l'histoire, modifier le texte généré. Voir [Livres](user/books/index.md).
- **Lorebooks** – notes sur le monde et les personnages, ajoutées au prompt uniquement quand elles comptent : par étiquettes de livre, filtres de texte ou d'expressions régulières, lorebooks imbriqués. Les informations de monde de SillyTavern peuvent être importées. Voir [Lorebooks](user/lorebooks/index.md).
- **Modèles et macros** - les prompts sont des modèles Handlebars avec des macros compatibles SillyTavern, si bien que le lore importé continue de fonctionner. Voir [Modèles et macros](user/templates/index.md).
- **Résumés** – les parties anciennes sont condensées afin que les longs livres tiennent dans la fenêtre de contexte. Voir [Résumés](user/books/summaries.md).
- **Tout modèle compatible OpenAI** - y compris les modèles de raisonnement, avec des limites par modèle et des réglages de génération modifiables à la volée. Voir [Fournisseurs d'inférence](user/inference-providers.md) et [Protocoles](user/protocols.md).
- **Sauvegardes** - sauvegardes par livre avec restauration et copie, et sauvegardes complètes de la base de données selon un planning. Voir [Sauvegardes de livres](user/books/backups.md) et [Sauvegardes de la base de données](user/administration/database-backups.md).
- **Plusieurs utilisateurs** – chacun avec ses propres livres, son lore et ses modèles ; les administrateurs gèrent les utilisateurs, les sauvegardes et le nettoyage. Voir [Administration](user/administration/index.md).
- **Livres publiés** – une vue de lecture en lecture seule que vous pouvez partager. Voir [Publication et visionneuse](user/books/publishing.md).
- **Recherche et remplacement, commandes** - recherche et remplacement dans l'histoire et dans les lorebooks, et une ligne de commande à côté du menu de l'histoire avec des commandes qui lisent et modifient le livre. Voir [Commandes](user/commands/index.md) et [Expressions régulières](user/regular-expressions.md).
- **Agents** - un modèle qui travaille seul sur un livre à l'aide des commandes, sur une copie : vous le regardez faire, lui parlez dans une console et examinez ses modifications avant qu'elles n'atteignent le livre. Voir [Agents](user/agents.md).
- **Extensions** - des plugins ajoutent des fonctionnalités à l'exécution : marqueurs de chapitres, historique des lorebooks, relectures par IA, questions annexes au modèle. Voir [Extensions](user/extensions/index.md).

<a id="which-guide-do-you-need"></a>

## Quel guide vous faut-il ?

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[Guide utilisateur](user/index.md)**    | Vous voulez installer Marginalia, le faire tourner pour d'autres, ou écrire avec. Commencez par [Premiers pas](user/getting-started/index.md). |
| **[Guide développeur](developer/index.md)** | Vous voulez compiler Marginalia depuis les sources, le modifier, écrire une extension, ou comprendre son fonctionnement interne. |

<a id="quick-links"></a>

## Liens rapides

| Je veux...                     |                                                          |
|--------------------------------|----------------------------------------------------------|
| L'installer sur mon ordinateur | [Application de bureau](user/getting-started/desktop-app.md) |
| Le faire tourner sur un serveur | [Serveur (Docker)](user/getting-started/docker-server.md) |
| Connecter un modèle            | [Fournisseurs d'inférence](user/inference-providers.md)  |
| Écrire la première partie d'un livre | [Démarrage rapide](user/getting-started/quick-start.md) |
| Utiliser mes lorebooks SillyTavern | [Import et export](user/lorebooks/import-export.md)  |
| Résoudre un problème           | [Dépannage et FAQ](user/troubleshooting.md)              |
| Écrire une extension           | [Développement de plugins](developer/plugins/index.md)   |
| Signaler un bug ou contribuer  | [Contribuer](developer/contributing.md)                  |

<a id="about"></a>

## À propos

Marginalia est un logiciel libre sous [licence MIT](https://github.com/Enerccio/Marginalia/blob/master/LICENSE).
Les images utilisées dans les captures d'écran de ce manuel sont créditées sur la page [Crédits](credits.md).
Les versions sont disponibles sur [GitHub](https://github.com/Enerccio/Marginalia/releases) ; les bugs et idées vont sur le [gestionnaire de tickets](https://github.com/Enerccio/Marginalia/issues).
