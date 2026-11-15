---
label: GPU
order: 84
---

# GPU

Une carte graphique ne contient souvent qu'un modèle à la fois : le modèle de langage, le modèle d'images ou le modèle de parole. L'onglet **GPU** de la page Admin permet aux modèles et aux serveurs d'une même carte de se relayer dessus. Ainsi, une image ou un livre audio ne démarre pas pendant que le modèle de langage occupe la carte, et le modèle de langage revient lorsqu'il est nécessaire.

Vous le configurez une fois, en tant qu'administrateur ; chaque utilisateur choisit ensuite un verrou pour son IA ou ses backends.

<a id="what-it-is-made-of"></a>

## Ce qu'il contient

| | |
|---|---|
| **GPU** | Une carte (ou toute machine qui ne contient qu'un modèle à la fois). Seulement son nom. |
| **Verrou GPU** | Une façon d'utiliser un GPU : ce qui est chargé dessus. Par exemple *Modèle de langage*, *ComfyUI* et *Parole* sur le même GPU. Un verrou comporte deux listes ordonnées d'étapes : les **étapes d'acquisition** le chargent (démarrer un conteneur, charger un modèle, décharger ce qui doit partir d'abord), et les **étapes de libération** libèrent la carte (arrêter le conteneur, décharger le modèle). **Tâches simultanées** limite le nombre de tâches du verrou qui s'exécutent ensemble (un serveur d'images qui ne fait qu'une image à la fois : 1 ; 0 pour aucune limite). |
| **Étape GPU** | Une requête HTTP : une adresse, une méthode, des en-têtes (`Name: value`, un par ligne, stockés chiffrés, de sorte qu'une clé bearer peut y être écrite), un corps, un délai d'expiration (vide : 60 secondes) et une attente après la réponse. Une étape peut être utilisée par plusieurs verrous, par exemple une étape *Décharger le modèle de langage* que les verrous d'images et de parole exécutent tous deux en premier. **Tester** envoie la requête immédiatement. |

Choisissez le verrou dans :

- les [réglages d'une IA](../inference-providers.md) (**Verrou GPU** dans les réglages généraux), pour le modèle de langage ;
- un backend de [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images) ;
- un backend de [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

La case **Verrou GPU** n'apparaît que si au moins un verrou existe.

<a id="how-the-turns-work"></a>

## Comment les tours s'enchaînent

- Chaque requête vers un modèle, chaque image et chaque lot ou échantillon de livre audio prend le verrou de son IA ou de son backend **pour toute sa durée**.
- Lorsque le GPU est déjà chargé avec ce verrou, la tâche démarre immédiatement et aucune étape ne s'exécute.
- Lorsqu'il est chargé avec un autre verrou, la tâche attend que les tâches de ce verrou soient terminées. Ensuite, les **étapes de libération** de ce verrou s'exécutent (toutes, même si l'une d'elles échoue), puis les **étapes d'acquisition** du nouveau verrou (dans l'ordre ; la première erreur les arrête et la tâche échoue avec le message de l'étape).
- Lorsqu'une étape de libération échoue, le nouveau verrou n'est pas chargé du tout (la carte peut encore être pleine) : la tâche échoue avec l'erreur et le GPU reste avec le verrou qu'il avait. La tâche suivante d'un autre verrou tente de le libérer à nouveau.
- Après une tâche, le GPU **reste chargé** avec son verrou. Rien n'est déchargé par un minuteur : une fenêtre d'images laissée ouverte, repliée ou fermée ne libère pas de GPU, et le modèle d'images reste chargé jusqu'à ce que le modèle de langage (ou un autre verrou) ait besoin de la carte.
- Les tâches attendent dans l'ordre d'arrivée. Dès qu'une tâche attend un autre verrou, les nouvelles tâches du verrou chargé attendent derrière elle, de sorte qu'un modèle de langage très sollicité ne puisse pas faire attendre indéfiniment une image.
- Une IA ou un backend sans verrou, ou avec un verrou qui a été supprimé, n'attend rien.

Ce qu'un GPU contient n'est connu que pendant que l'application tourne : après un redémarrage, rien n'est considéré comme chargé, et la première tâche n'exécute que ses étapes d'acquisition.

<a id="the-tab"></a>

## L'onglet

L'onglet liste les GPU avec le verrou avec lequel chacun est chargé maintenant (*Unknown* si rien n'a encore tourné ou s'il a été déchargé), ainsi que le nombre de tâches qui s'y exécutent et qui y attendent. Viennent ensuite les verrous et les étapes, chacun avec des boutons pour ajouter, modifier et supprimer. **Actualiser** relit ce que contiennent les GPU.

**Décharger maintenant** libère un GPU à la main, par exemple pour utiliser la carte pour quelque chose hors de Marginalia : elle attend les tâches en cours, exécute les étapes de libération du verrou chargé et laisse le GPU inconnu (même si une étape a échoué ; le message indique laquelle). La tâche suivante recharge son verrou.

Supprimer un GPU supprime ses verrous ; supprimer une étape la retire des verrous qui l'exécutent. Les IA et les backends qui désignaient un verrou supprimé s'exécutent sans verrou.

<a id="an-example-one-card-three-models"></a>

## Un exemple : une carte, trois modèles

Avec `llama-server` derrière LiteLLM et un proxy de style [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) qui démarre et arrête des conteneurs :

| Étape | Requête |
|---|---|
| Décharger le modèle de langage | `POST http://litellm:4000/v1/chat/completions` avec l'en-tête `X-Llama-Unload: all` (voir [Libérer le GPU pour un autre modèle](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Démarrer ComfyUI / Arrêter ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Démarrer la parole / Arrêter la parole | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Verrou | Étapes d'acquisition | Étapes de libération | Tâches simultanées |
|---|---|---|---|
| Modèle de langage | (aucune : le routeur le charge avec la requête suivante) | (aucune) | 0 |
| ComfyUI | Décharger le modèle de langage, Démarrer ComfyUI | Arrêter ComfyUI | 1 |
| Parole | Décharger le modèle de langage, Démarrer la parole | Arrêter la parole | 1 |

L'IA du modèle de langage choisit *Modèle de langage*, le backend d'images *ComfyUI*, et le backend de parole *Parole*. Un lot de parole après une discussion exécute *Décharger le modèle de langage* puis *Démarrer la parole* ; le message de chat suivant exécute d'abord *Arrêter la parole*.
