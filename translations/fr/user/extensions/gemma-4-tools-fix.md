---
label: Correctif des outils Gemma 4
order: 15
---

# Correctif des outils Gemma 4

Permet l'appel de fonctions avec les modèles Gemma 4 dont l'API attend un autre rôle pour les résultats des appels.

!!!warning Inutile avec un template de chat à jour
Ne le chargez que pour un ancien modèle Gemma 4. Le template de chat de Gemma 4 publié par Google lit les résultats avec le rôle `tool`, celui qu'envoie Marginalia, depuis avril 2026 (tous les GGUF plus récents l'ont). Avec un tel template, l'extension dégrade le prompt : le résultat arrive comme un tour supplémentaire que le template ne connaît pas. llama.cpp ignore aussi l'ancienne forme, donc le rôle `tool` est le seul qui fonctionne avec lui.

Un ancien GGUF conserve le template avec lequel il a été créé. Pour vérifier le vôtre, faites le rendu d'un appel de fonction et de son résultat avec la commande `/apply-template` de llama.cpp, puis cherchez `<|tool_response>` dans le tour du modèle : s'il s'y trouve, ne chargez pas l'extension. Un modèle qui écrit ses appels sous forme de texte au lieu de les émettre est mieux aidé par la *Function call rule* des [réglages du modèle](../inference-providers.md).
!!!

<a id="how-it-works"></a>

## Fonctionnement

Lorsque le modèle appelle une fonction, Marginalia lui renvoie le résultat dans un message dont le rôle est `tool`. Certains serveurs qui exécutent Gemma 4 attendent le rôle `tool_responses` à la place, et répondent par une erreur ou ignorent le résultat sinon.

L'extension change le rôle pour ces modèles : lorsque le *Model* d'un [fournisseur d'inférence](../inference-providers.md) contient `gemma4` ou `gemma-4` (quelle que soit la casse), les résultats des appels de fonction vers ce modèle sont envoyés avec le rôle `tool_responses`. Les requêtes vers les autres modèles ne sont pas modifiées. Il s'agit d'un portage de l'extension SillyTavern [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Utilisation

Il n'y a rien à configurer. Chargez l'extension dans *Admin → Extensions* ; elle s'applique à la requête suivante.
