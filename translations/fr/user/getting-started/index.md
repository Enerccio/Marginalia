---
label: Bien démarrer
order: 990
icon: rocket
---

# Bien démarrer

Marginalia fonctionne dans votre navigateur. Vous pouvez l'installer de deux façons :

| | [Application de bureau](desktop-app.md) | [Serveur (Docker)](docker-server.md) |
|---|---|---|
| Idéal pour | Une personne sur un seul ordinateur | Plusieurs utilisateurs, ou un accès depuis d'autres appareils |
| Installation | Télécharger et lancer, rien d'autre à installer | Docker et Docker Compose |
| Adresse | `http://127.0.0.1:8765` (votre ordinateur uniquement) | Port `8080`, généralement derrière un reverse proxy avec HTTPS |
| Données | `~/.marginalia` | `./data` à côté de `docker-compose.yml` |

Les deux versions sont la même application, avec les mêmes fonctionnalités : livres, lorebooks, utilisateurs, sauvegardes et extensions.
Pour écrire depuis un téléphone, lancez la version serveur et ouvrez-la dans le navigateur du téléphone : voir
[Utiliser Marginalia sur un téléphone](../phone.md).

Marginalia n'a pas de modèle propre. Il a besoin d'une **API compatible OpenAI** pour écrire du texte : un service cloud (OpenAI,
OpenRouter...) ou un modèle exécuté sur votre propre ordinateur (llama.cpp, LM Studio, Ollama, vLLM...). Préparez l'adresse de l'API et une clé API (un serveur local qui n'en demande pas fonctionne sans clé, voir [Ajouter un fournisseur](../inference-providers.md#adding-a-provider)).

<a id="steps"></a>

## Étapes

1. Installez Marginalia : [application de bureau](desktop-app.md) ou [serveur](docker-server.md).
2. [Créez le compte administrateur](first-start.md) au premier lancement.
3. Suivez le [démarrage rapide](quick-start.md) : connectez un modèle, créez un protocole et écrivez la première partie d'un livre.
4. Facultatif : ouvrez le [livre d'exemple](example-book.md), une histoire terminée avec des résumés, des annotations et des traductions, pour voir à quoi ressemble un long livre.
