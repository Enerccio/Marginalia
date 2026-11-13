---
label: Correctif des outils Gemma 4
order: 15
---

# Correctif des outils Gemma 4

Permet l'appel de fonctions avec les modèles Gemma 4 dont l'API attend un autre rôle pour les résultats des appels.

<a id="how-it-works"></a>

## Fonctionnement

Lorsque le modèle appelle une fonction, Marginalia lui renvoie le résultat dans un message avec le rôle `tool`. Certains
serveurs qui exécutent Gemma 4 attendent le rôle `tool_responses` à la place, et répondent par une erreur ou ignorent le résultat sinon.

L'extension change le rôle pour ces modèles : lorsque le **Modèle** d'un [fournisseur d'inférence](../inference-providers.md) contient `gemma4` ou
`gemma-4` (quelle que soit la casse), les résultats des appels de fonctions adressés à ce modèle sont envoyés avec le rôle `tool_responses`. Les requêtes adressées aux autres
modèles ne sont pas modifiées. Il s'agit d'un portage de l'extension SillyTavern
[SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Utilisation

Aucune configuration n'est nécessaire. Chargez l'extension dans **Admin → Extensions** ; elle s'applique à la requête suivante.
