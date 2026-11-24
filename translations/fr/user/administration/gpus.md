---
label: GPU
order: 84
---

# GPU

Une carte graphique ne contient souvent qu'un seul modèle à la fois : le modèle de langage, ou le modèle d'image, ou le modèle de parole. L'onglet **GPUs** de la page Admin permet aux modèles et aux serveurs d'une même carte de se relayer dessus. Ainsi, une image ou un livre audio ne démarre pas pendant que le modèle de langage remplit la carte, et le modèle de langage revient lorsqu'il est nécessaire.

Vous configurez cela une fois, en tant qu'administrateur ; chaque utilisateur choisit ensuite un verrou pour son IA ou ses backends.

<a id="what-it-is-made-of"></a>

## De quoi il est composé

| | |
|---|---|
| **GPU** | Une carte (ou toute machine qui ne contient qu'un modèle à la fois) : son nom, et **Also used outside Marginalia** (voir [ci-dessous](#a-gpu-that-others-use-too)). |
| **GPU lock** | Une façon d'utiliser un GPU : ce qui est chargé dessus. Par exemple *Language model*, *ComfyUI* et *Speech* sur le même GPU. Un verrou comporte deux listes ordonnées d'étapes : les **acquire steps** le chargent (démarrer un conteneur, charger un modèle, décharger ce qui doit partir au préalable), et les **release steps** libèrent la carte (arrêter le conteneur, décharger le modèle). Une troisième liste, les **steps before every job**, s'exécute avant chaque job du verrou, que le GPU détienne ou non ce verrou (avant les acquire steps s'il ne le détient pas) : elle sert à vérifier ce qui doit l'être à chaque fois, car quelqu'un d'autre a pu utiliser la carte entre-temps. **Jobs at once** limite le nombre de jobs du verrou qui tournent ensemble (un serveur d'images qui fait une image à la fois : 1 ; 0 pour aucune limite). |
| **GPU step** | Une requête HTTP : une adresse, une méthode, des en-têtes (`Name: value`, un par ligne, stockés chiffrés, ce qui permet d'y écrire une clé bearer), un corps, un délai d'attente (vide : 60 secondes) et une attente après la réponse. Une étape peut être utilisée par plusieurs verrous, par exemple une étape *unload the language model* que les verrous de l'image et de la parole exécutent tous les deux en premier. **Test** envoie la requête immédiatement. |

![La fenêtre d'un verrou](../../images/gpu-lock-dialog.png)

Choisissez le verrou dans :

- les [paramètres d'une IA](../inference-providers.md) (**GPU lock** dans les paramètres généraux), pour le modèle de langage ;
- un backend de l'[Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images) ;
- un backend de l'[Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

La case **GPU lock** n'apparaît que lorsqu'au moins un verrou existe.

<a id="variables-in-the-steps"></a>

## Variables dans les étapes

Une étape peut indiquer à quoi sert le job, afin qu'une même étape serve à plusieurs modèles et que les verrous n'aient pas à être modifiés lorsque vous essayez un autre modèle. Dans l'adresse, les en-têtes et le corps :

| Variable | Valeur |
|---|---|
| `{{model}}` | le modèle utilisé par le job : l'identifiant du modèle de l'IA (tel qu'il figure dans les paramètres de l'IA, par exemple `openai/big-32b` via LiteLLM), le modèle d'un backend de livre audio (`higgs-audio-v3`, `chatterbox-turbo`), celui de la reconnaissance vocale ou celui du backend d'image ; vide pour un job qui n'en utilise pas (le nettoyage des clips) |
| `{{lock}}` | le nom du verrou |
| `{{text_model}}` | uniquement pour un job de génération de texte : l'identifiant du modèle de son IA (le même que `{{model}}`) ; vide pour les jobs de parole et d'image |
| `{{last_text_model}}` | chaque job : le modèle de la dernière génération de texte sur ce verrou, mémorisé dès le démarrage de ce job (ainsi, un job de texte voit le sien) ; vide avant la première génération de texte et après un redémarrage de Marginalia |

Les filtres modifient la valeur : `{{model | strip:openai/}}` retire le préfixe lorsque la valeur commence par lui (le nom de route de LiteLLM, que le serveur derrière ne connaît pas), `{{model | last}}` garde ce qui suit le dernier `/`, `{{model | json}}` l'échappe pour une chaîne JSON et `{{model | url}}` l'encode pour une adresse ; ils peuvent être enchaînés (`{{model | last | json}}`). Les acquire steps et les steps before every job reçoivent les valeurs du job ; les release steps reçoivent celles du dernier job du verrou chargé (le modèle qui doit être déchargé). **Test** exécute une étape avec les variables vides.

<a id="how-the-turns-work"></a>

## Comment les tours se succèdent

- Chaque requête vers un modèle, chaque image et chaque lot ou échantillon de livre audio prend le verrou de son IA ou de son backend **pendant toute sa durée**.
- Lorsque le GPU est déjà chargé avec ce verrou, le job démarre aussitôt et aucune étape ne s'exécute, sauf les **steps before every job** du verrou.
- Lorsqu'il est chargé avec un autre verrou, le job attend que les jobs de cet autre verrou soient terminés. Ensuite, les **release steps** de ce verrou s'exécutent (toutes, même si l'une d'elles échoue), puis les steps before every job et les **acquire steps** du nouveau verrou (dans l'ordre ; la première erreur les arrête et le job échoue avec le message de l'étape).
- Lorsqu'une release step échoue, le nouveau verrou n'est pas chargé du tout (la carte peut encore être pleine) : le job échoue avec l'erreur, et le GPU reste avec le verrou qu'il avait. Le job suivant d'un autre verrou tente de nouveau de la libérer.
- Après un job, le GPU **reste chargé** avec son verrou. Rien n'est déchargé par minuterie : une fenêtre d'image laissée ouverte, repliée ou fermée ne garde aucun GPU, et le modèle d'image reste chargé jusqu'à ce que le modèle de langage (ou un autre verrou) ait besoin de la carte.
- Les jobs attendent dans l'ordre d'arrivée. Dès qu'un job attend un autre verrou, les nouveaux jobs du verrou chargé attendent derrière lui ; ainsi, un modèle de langage très sollicité ne peut pas empêcher indéfiniment une image d'être générée.
- Une IA ou un backend sans verrou, ou dont le verrou a été supprimé, n'attend rien.

Ce qu'un GPU contient n'est connu que pendant que l'application tourne : après un redémarrage, rien n'est considéré comme chargé, et le premier job n'exécute que ses acquire steps.

<a id="a-gpu-that-others-use-too"></a>

## Un GPU que d'autres utilisent aussi

Les verrous n'ordonnent que les jobs de Marginalia. Ce que Marginalia sait de la carte provient de ses propres jobs. Après un redémarrage de Marginalia, ou lorsqu'un autre programme, un script ou une personne charge un modèle sur la carte (une requête au routeur qui réveille le modèle de langage, un workflow mis en file d'attente depuis la page de ComfyUI), ce que Marginalia croit chargé est faux, et un job peut démarrer sur une carte pleine.

- **Also used outside Marginalia** sur le GPU fait que Marginalia cesse de faire confiance à ce qu'il sait : les **acquire steps s'exécutent avant chaque job**, et pas seulement lorsque le GPU change de verrou (les release steps ne s'exécutent toujours qu'en cas de changement). Les acquire steps de chaque verrou doivent alors faire de la place par eux-mêmes (décharger les autres modèles, arrêter l'autre serveur) et pouvoir être répétés sans risque : démarrer un serveur déjà lancé, décharger un modèle qui n'est pas chargé et arrêter un conteneur déjà arrêté doivent répondre par un succès, sinon chaque job échoue. Cela coûte ces requêtes à chaque job.
- Les **steps before every job** font la même chose pour un seul verrou, sans les autres : seules les étapes qui doivent être vérifiées à chaque fois (décharger les modèles d'un autre programme, s'assurer qu'un serveur répond) s'exécutent avant chaque job, le reste uniquement lors d'un changement.
- Le lieu sûr pour cela est la machine GPU elle-même, par laquelle passe chaque utilisateur : un proxy dont *start ComfyUI* décharge d'abord le modèle de langage, ou un crochet du routeur qui arrête ComfyUI avant de charger le modèle de langage. Ainsi, personne ne peut remplir la carte deux fois, quel que soit le programme qui la demande.

<a id="the-tab"></a>

## L'onglet

L'onglet liste les GPU avec le verrou que chacun détient actuellement (*Unknown* lorsque rien n'a encore tourné ou qu'il a été déchargé), ainsi que le nombre de jobs qui s'y exécutent et qui y attendent. Viennent ensuite les verrous et les étapes, chacun avec des boutons pour ajouter, modifier et supprimer. **Refresh** relit ce que contiennent les GPU.

![L'onglet GPUs avec un GPU, un verrou et les étapes](../../images/admin-gpus.png)

**Unload now** libère un GPU à la main, par exemple pour utiliser la carte à une fin extérieure à Marginalia : il attend les jobs en cours, exécute les release steps du verrou chargé et laisse le GPU à l'état inconnu (même si une étape a échoué ; le message indique laquelle). Le job suivant recharge alors son verrou.

Supprimer un GPU supprime ses verrous ; supprimer une étape la retire des verrous qui l'exécutent. Les IA et backends qui désignaient un verrou supprimé s'exécutent sans verrou.
<a id="an-example-one-card-several-models"></a>

## Un exemple : une carte, plusieurs modèles

Avec `llama-server` derrière LiteLLM et un proxy de style [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) qui démarre et arrête les conteneurs :

| Étape | Requête |
|---|---|
| Unload the language model | `POST http://litellm:4000/v1/chat/completions` avec l'en-tête `X-Llama-Unload: all` (voir [Libérer le GPU pour un autre modèle](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |

Utilisé uniquement par Marginalia, chaque verrou ne libère la carte que pour lui-même, et rien de plus :

| Verrou | Étapes d'acquisition | Étapes de libération | Tâches simultanées |
|---|---|---|---|
| Language model | (aucune : le routeur le charge avec la requête suivante) | Unload the language model | 1 |
| ComfyUI images | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech recognition | Start ComfyUI | Stop ComfyUI | 1 |
| Clip cleanup | Start ComfyUI | Stop ComfyUI | 1 |

Un lot de synthèse vocale après un chat exécute *Unload the language model* (la libération du modèle de langage) puis *Start ComfyUI* ; le message de chat suivant exécute d'abord *Stop ComfyUI*. Les verrous ComfyUI sont séparés à dessein : passer de l'un à l'autre arrête et redémarre ComfyUI, ce qui libère la mémoire qu'un modèle conserve après son déchargement (Higgs en fp32 garde environ 16 Go de la mémoire du serveur). Ainsi, le modèle d'images, Higgs, les modèles de reconnaissance et de nettoyage ne sont jamais présents ensemble.

Avec **Also used outside Marginalia**, chaque acquisition doit au contraire faire de la place par elle-même : *Language model* acquiert avec *Stop ComfyUI*, et chaque verrou ComfyUI avec *Unload the language model, Start ComfyUI*.

Lorsqu'un autre programme utilise **le même routeur `llama-server`** avec un modèle à lui (par exemple un petit modèle à côté de son modèle de synthèse vocale), *Unload the language model* éjecterait aussi son modèle, et son propre déchargement éjecterait celui de Marginalia. `tools/gpu-control` dispose d'un appel qui décharge tous les modèles du routeur **sauf** ceux qui sont nommés : une étape `POST http://litellm:4000/gpu/llama/unload` avec le corps `{"keep": ["{{model | strip:openai/}}"]}` (son README décrit le passage direct). Placez-la comme **étape avant chaque tâche** de *Language model* : chaque IA de ce verrou conserve son propre modèle, quel que soit celui que vous choisissez ; l'autre programme fait de même avec son propre modèle avant chacune de ses tâches. Lorsque le modèle conservé est le seul chargé, l'appel ne fait rien et répond aussitôt, si bien qu'aucun des deux programmes n'a besoin de connaître l'autre.

Lorsqu'un seul verrou sert à la fois le modèle de langage et le modèle de synthèse vocale (les deux fonctionnent côte à côte sur la carte), `{{model}}` dans cette étape désigne le modèle de synthèse vocale pendant une tâche de synthèse, qui ne correspond à aucun modèle du routeur, et le modèle de langage serait déchargé juste après chaque réponse. Gardez plutôt `{{last_text_model}}`, `{"keep": ["{{last_text_model | strip:openai/}}"]}` : chaque tâche du verrou conserve le modèle de la dernière génération de texte, quel que soit le type de tâche. Avant la première génération de texte, rien n'est conservé, et la première génération charge son modèle.

Pour ComfyUI, la même étape est `POST http://litellm:4000/gpu/comfy/ensure` : elle ne fait rien lorsque ComfyUI répond, démarre le conteneur s'il est arrêté, et le redémarre lorsqu'il tourne sans répondre (planté en interne, bloqué), puis attend qu'il soit prêt. Placée comme étape avant chaque tâche des verrous ComfyUI, elle s'assure que ComfyUI est vivant, quel que soit celui qui l'a arrêté.

L'IA du modèle de langage choisit *Language model*, le backend d'images choisit *ComfyUI images*, le backend de synthèse vocale choisit *ComfyUI speech*, et la [reconnaissance vocale](../extensions/audiobook.md#speech-recognition) ainsi que le [nettoyage des clips](../extensions/audiobook.md#cleanup-of-clips) de l'Audiobook prennent leurs propres verrous.
