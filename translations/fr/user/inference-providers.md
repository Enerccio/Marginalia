---
label: Fournisseurs d'inférence
order: 980
icon: server
---

# Fournisseurs d'inférence

Un fournisseur d'inférence est une connexion à un modèle : l'adresse de l'API, la clé d'API, le modèle et ses limites. Chaque livre utilise un fournisseur. Vous pouvez en avoir autant que vous voulez, par exemple un grand modèle dans le cloud pour l'histoire et un petit modèle local pour les essais, et changer un livre de l'un à l'autre à tout moment.

Les fournisseurs appartiennent à votre compte. Les autres utilisateurs ne les voient pas, pas plus que leurs clés d'API. Les clés d'API sont stockées chiffrées (voir [Le dossier de données](administration/index.md#the-data-folder)).

Ajouter un fournisseur nécessite la permission **Create own models**, que vous accorde un administrateur (les administrateurs l'ont toujours, voir [Permissions](administration/permissions.md)). Sans elle, vous ne pouvez utiliser que les fournisseurs que les administrateurs [partagent](#shared-providers) avec vous ; le bouton **Add Inference Provider** n'est pas affiché.

![L'onglet des fournisseurs d'inférence](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## API prises en charge

Marginalia fonctionne avec toute API qui implémente l'interface **OpenAI Chat Completions** (`/v1/chat/completions`, avec streaming). La plupart des services et serveurs locaux le font :

| Service | URL OpenAI |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Les ports sont ceux par défaut de chaque serveur ; utilisez ceux sur lesquels votre serveur fonctionne réellement.

!!!warning Port 8080
La version Docker de Marginalia utilise aussi le port 8080. Si vous lancez llama.cpp sur la même machine, démarrez-le sur un autre port (`llama-server --port 8081`).
!!!

!!!
Avec la [version Docker](getting-started/docker-server.md), `localhost` désigne le conteneur Marginalia. Utilisez `http://host.docker.internal:<port>/v1` pour un serveur de modèles sur la même machine, voir [Modèles sur la même machine](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Ajouter un fournisseur

Ouvrez l'onglet **Inference Providers** et cliquez sur **Add Inference Provider**. Les fournisseurs existants se modifient avec le bouton crayon
et se suppriment avec le bouton corbeille. Le bouton de clonage (deux feuilles) fait une copie d'un fournisseur sous un nom que vous confirmez (celui de l'original suivi de *(Copy)*) : la copie a la même adresse, le même modèle, la même **API key** (ou les réglages de l'extension), le raisonnement, les limites de tokens et les paramètres. Ainsi, un second modèle sur le même service ne demande qu'un nom et un clic. La copie n'est pas partagée et n'a ni limites ni usage qui lui sont propres. Vous ne pouvez cloner que vos propres fournisseurs (pas un fournisseur partagé par un administrateur, dont vous ne devez pas voir la clé), et seulement si vous êtes autorisé à créer des fournisseurs.

En haut de la fenêtre :

| Champ | |
|---|---|
| **Name** | Votre nom pour le fournisseur, affiché lorsque vous choisissez un modèle pour un livre. Incluez le modèle, par exemple `OpenRouter - Claude Sonnet`. |
| **Type** | Le type d'API : **OpenAI Compatible**, ou **Extension** pour un fournisseur fourni par une [extension](extensions/index.md) (il n'est proposé que si une telle extension est chargée, par exemple [Google Gemini](extensions/google-genai.md) ou [Anthropic Claude](extensions/anthropic.md)). Le type ne peut plus être modifié une fois le fournisseur créé. |
| **Provider** | Seulement pour le type **Extension** : l'extension dont ce fournisseur fait partie. Ses propres réglages remplissent l'onglet *Per Type Settings*. |

### Per Type Settings

![Per Type Settings avec la liste des modèles chargée](../images/inference-provider.png)

L'onglet est plus haut que la fenêtre et défile. Les champs du bas :

![Timeout, retries, paramètres additionnels et Test Connection](../images/inference-provider-2.png)

| Champ | |
|---|---|
| **OpenAI URL (ends with /v1)** | L'adresse de base de l'API, voir [le tableau ci-dessus](#supported-apis). Sans `/chat/completions`. |
| **Api Key** | La clé du service. Un serveur local ne la vérifie généralement pas : laissez le champ vide et Marginalia envoie le marqueur `none`. |
| **Model** | Le modèle à utiliser. Cliquez sur **Refresh** pour charger la liste des modèles depuis l'API, ou saisissez vous-même l'identifiant du modèle (par exemple `anthropic/claude-sonnet-4.5` sur OpenRouter). |
| **Timeout (seconds)** | Le délai d'attente de la réponse de l'API, puis de la suite du texte une fois qu'elle a commencé à écrire. La valeur par défaut est 300. Les modèles de raisonnement peuvent mettre plusieurs minutes avant le premier texte. Une requête qui n'entend rien pendant ce délai échoue avec un message de dépassement de délai (la réflexion d'un modèle compte comme du texte, donc un modèle qui continue de réfléchir n'est pas coupé). Un changement est utilisé par la requête suivante, y compris celle d'un agent en cours de travail. |
| **Retries** | Le nombre de répétitions d'une requête lorsque l'API répond par une limite de débit (429) ou une erreur serveur (5xx), ou lorsqu'elle est injoignable. L'attente entre les tentatives augmente, et un en-tête `Retry-After` de l'API est respecté. La valeur par défaut est 2 ; 0 désactive les nouvelles tentatives. Seul le début d'une requête est retenté : un texte déjà reçu n'est jamais écrit deux fois. |
| **Tokenizer** | La façon dont Marginalia compte les tokens des textes pour ce fournisseur. **Automatic** (la valeur par défaut) essaie les points d'accès tokenizer du serveur l'un après l'autre (OpenAI, llama.cpp, LiteLLM) et utilise le premier qui répond, ou une estimation locale si aucun ne répond. Choisissez-en un pour n'utiliser que celui-là, ou **LOCAL_TOKKIT** pour l'estimation locale qui n'appelle jamais le serveur (utile lorsque compter ferait charger un autre modèle au serveur). Un tokenizer venant d'un plugin figure aussi dans cette liste ; il reçoit l'adresse, le modèle et le texte, jamais la clé API, et seulement si vous le choisissez. **Test tokenizer** compte un texte d'essai avec ce que le fournisseur essaierait, et affiche la réponse de chacun. Lorsque vous créez un fournisseur, un tokenizer de plugin correspondant à l'adresse est présélectionné jusqu'à ce que vous choisissiez vous-même. |
| **Send the thinking back** | Indique si le modèle reçoit à nouveau sa propre réflexion dans la requête suivante, voir [Send the thinking back](#send-the-thinking-back). Seulement pour les fournisseurs compatibles OpenAI : les fournisseurs Anthropic et Google envoient d'eux-mêmes ce que leurs API demandent. |
| **Additional Parameters** | Un objet JSON avec des champs supplémentaires pour chaque requête à ce fournisseur, par exemple `{"top_k": 40, "min_p": 0.05}` pour un serveur local, ou `{"provider": {"order": ["anthropic"]}}` pour OpenRouter. Un champ défini ici l'emporte sur le même réglage venant du [protocole](protocols.md) (par exemple `temperature`) ou de la limite de réponse (`max_completion_tokens`). Doit être un objet JSON valide ou rester vide. |

Lorsque vous modifiez un fournisseur existant, la clé API n'est plus affichée. Elle est conservée telle quelle, sauf si vous cliquez sur
**Reset Api Key** et saisissez une nouvelle clé.

**Refresh** demande à l'API sa liste de modèles (`/v1/models`) avec l'adresse et la clé du formulaire. Si cela échoue avec
*Failed to download model list*, vérifiez l'adresse (elle doit se terminer par `/v1`), la clé, et que le serveur est bien lancé.
Certains services ne listent pas leurs modèles ; saisissez alors l'identifiant du modèle.

**Test Connection** vérifie les valeurs du formulaire sans rien enregistrer : il demande à l'API sa liste de modèles, puis une réponse d'un seul token pour le **Model** sélectionné, sans nouvelle tentative. Un message de succès signifie que l'adresse, la clé et le modèle fonctionnent ensemble. En cas d'échec, la cause est indiquée (voir [Error messages](#error-messages)). Certains services n'ont pas de liste de modèles ; le test ne demande alors que la réponse d'un seul token.

### General Settings

![Réglages généraux d'un fournisseur d'inférence](../images/inference-provider-general.png)

| Champ | |
|---|---|
| **Max Context Size** | Obligatoire. Le nombre total de tokens que le modèle accepte, prompt et réponse réunis. |
| **Max Response Tokens** | Obligatoire. La réponse la plus longue que le modèle peut écrire. Envoyée à l'API sous la forme `max_completion_tokens`, et cette part du contexte est réservée à la réponse. Doit être inférieure à *Max Context Size*. |
| **Connections** | Le nombre de requêtes pouvant être en cours vers ce modèle à la fois : toutes, quels que soient l'utilisateur et le type (votre génération, une requête annexe, une traduction, une annotation, [agents](agents.md#one-model-several-agents), plugins). Les autres attendent leur tour, dans l'ordre d'arrivée ; une génération qui doit attendre affiche *Waiting for the model: N requests before this one*, et **Stop** la retire de la file. **1** (la valeur par défaut) pour un serveur local à un seul emplacement (un seul GPU) ; un modèle cloud qui répond à plusieurs requêtes à la fois peut en accepter davantage, pour qu'une requête annexe tourne en même temps qu'une génération. |
| **GPU lock** | Seulement lorsque l'administrateur a configuré des [GPU](administration/gpus.md) : le verrou du GPU sur lequel ce modèle tourne, lorsque la carte est partagée avec d'autres modèles ou avec des serveurs d'images ou de voix. Chaque requête attend alors son tour sur le GPU (en comptant les requêtes qui la précèdent) et, si le GPU contient autre chose, le libère et charge ce modèle en suivant d'abord les étapes du verrou. Vide : pas de verrou. |
| **Multimodal (accepts images)** | Le modèle comprend les images, si bien que les [images des entrées du lorebook](lorebooks/lorebooks.md#editing-in-full-screen) utilisées par un tour sont envoyées avec leur texte. Désactivé par défaut : activez-le seulement pour un modèle qui accepte les images, car un modèle sans vision refuse la requête et le tour échoue. Les images des entrées du bloc de lore sont placées dans un message à part, juste après le prompt système (avec une réponse courte du modèle), et les autres avec votre prompt. |
| **Image placement** | (avec *Multimodal*) Où placer les images. *In the system message* (compatible OpenAI uniquement) : les images du bloc de lore vont dans le message système lui-même, à côté du texte du lore ; seulement pour un serveur et un chat template qui acceptent les images à cet endroit, ce qui n'est pas le cas de la plupart, et la requête échoue. *Before the text of the user message* : chaque image est placée avant le texte de son message, comme l'attend Gemma 4. *After the text of the user message* (la valeur par défaut) : après le texte. L'audio et les fichiers suivent toujours le texte. |
| **Function definitions** | Où le modèle lit les fonctions qu'il peut appeler (dans les [agents](agents.md), les requêtes annexes et les plugins qui proposent des fonctions). *In the system message* (la valeur par défaut) : uniquement comme outils de la requête, que le chat template du modèle place dans le tour système. *Before* / *After the text of the user message* : elles sont aussi écrites au début ou à la fin du dernier message utilisateur. Les outils sont toujours envoyés également, car le serveur en a besoin pour reconnaître un appel. |
| **Function call rule**, **Rule placement** | Une règle ajoutée à chaque requête qui propose des fonctions, pour les modèles qui écrivent un appel sous forme de texte dans leur réponse au lieu de le faire réellement, ou qui cessent d'appeler les fonctions. Par exemple : *You act only through function calls. Never write a call as text in your reply; to use a function, call it.* Le placement indique où elle va : le message système, ou le début ou la fin du dernier message utilisateur (la valeur par défaut). Gemma 4 la suit bien mieux dans le message utilisateur. Vide : pas de règle. |
| **Needs Jailbreak**, **Jailbreak Prompt** | Un prompt supplémentaire pour les modèles qui refusent d'écrire certains contenus. Lorsque la case est cochée, le prompt de jailbreak est placé tout au début du prompt. |
| **Enable Reasoning**, **Reasoning Effort** | Pour les modèles de raisonnement (« thinking »), voir [Reasoning models](#reasoning-models). |

Marginalia réserve à la réponse la part de contexte correspondant à **Max Response Tokens** et remplit le reste avec le prompt :
les modèles de prompt, les entrées du [lorebook](lorebooks/index.md) et les [résumés](books/summaries.md) y sont comptés, et les parties les plus anciennes de l'histoire qui ne tiennent pas sont laissées de côté. Avec un contexte de 32 000 tokens et des réponses allant jusqu'à 2 000 tokens, le prompt dispose de 30 000 tokens.

Reprenez **Max Context Size** depuis la documentation du service, et réglez-le un peu plus bas : les comptages de tokens ne sont pas toujours exacts, et une requête qui dépasse la limite réelle échoue. Un [protocole](protocols.md) peut abaisser les deux limites pour certains livres, mais pas les relever.
<a id="error-messages"></a>

## Messages d'erreur

Lorsque l'API refuse une requête, Marginalia indique ce qu'il faut corriger au lieu d'afficher une erreur interne, suivi de la réponse de l'API elle-même :

![Une erreur du fournisseur affichée à l'utilisateur](../images/error-dialog.png)

| Message | Cause |
|---|---|
| *Le fournisseur a rejeté la clé API.* | Clé **Api Key** erronée, expirée ou absente. |
| *La clé API n'est pas autorisée à utiliser ce modèle ou ce point d'accès.* | La clé est valide, mais elle n'a pas accès au modèle. |
| *Le fournisseur ne connaît pas le modèle configuré.* | **Model** erroné ; utilisez **Refresh** pour voir les noms que l'API connaît. |
| *Rien n'a été trouvé à l'URL configurée.* | **OpenAI URL** erronée ; elle se termine généralement par `/v1`. |
| *Le prompt ne tient pas dans le contexte du modèle.* | **Max Context Size** est supérieur à ce que le modèle accepte réellement. Baissez-le. |
| *Le fournisseur limite les requêtes.* | Limite de débit ou quota épuisé, même après les nouvelles tentatives. |
| *Le fournisseur n'a pas pu traiter la requête.* | Le service a un problème (5xx), même après les nouvelles tentatives. |
| *Le fournisseur n'a pas répondu à temps.* | Augmentez **Timeout (seconds)**, ou vérifiez le serveur. |
| *Impossible de se connecter au fournisseur.* | Le serveur n'est pas lancé ou n'est pas joignable depuis Marginalia, ou l'URL contient une faute de frappe. |
| *Le fournisseur a rejeté la requête.* | Autre refus, souvent un champ non pris en charge dans **Additional Parameters**, ou un raisonnement que le modèle ne supporte pas. |

<a id="warning-before-sending"></a>

## Avertissement avant l'envoi

Marginalia ne remplit le contexte que jusqu'à **Max Context Size** moins **Max Response Tokens**. Si le prompt final est encore plus grand que cela (par exemple parce qu'une extension a ajouté du texte, ou que le comptage des tokens était inexact), un avertissement affiche les tailles estimées avant l'envoi de la requête. La requête est tout de même envoyée ; l'API peut la refuser ou tronquer le prompt.

![L'avertissement concernant un prompt qui risque de ne pas tenir](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modèles de raisonnement

Les modèles de raisonnement réfléchissent avant de répondre. Lorsque **Enable Reasoning** est coché, Marginalia envoie le **Reasoning Effort** choisi (*None*, *Low*, *Medium* ou *High*) à l'API sous la forme `reasoning_effort`. Un effort plus important donne en général un texte mieux planifié, mais des réponses plus lentes et plus coûteuses. Toutes les API ne prennent pas ce paramètre ; si les requêtes échouent avec le raisonnement activé, désactivez-le.

Lorsque le modèle renvoie son raisonnement (sous la forme `reasoning_content` ou `reasoning`, selon le serveur), il est affiché au-dessus de la partie générée, sous **View Reasoning**. Ce raisonnement ne fait pas partie de l'histoire et n'est pas renvoyé au modèle, sauf si vous activez **Send the thinking back** (voir ci-dessous).

<a id="send-the-thinking-back"></a>

### Renvoyer la réflexion

**Send the thinking back** (dans *Per Type Settings* d'un fournisseur compatible OpenAI) indique si la réflexion du modèle lui est renvoyée, comme `reasoning_content` de son message précédent, dans la requête suivante :

| Valeur | |
|---|---|
| **None** (par défaut) | Jamais. Convient à la plupart des modèles et serveurs, ainsi qu'aux API qui refusent un champ inconnu. |
| **Between tool calls** | Seulement tant que les appels d'outils d'une même réponse se poursuivent : le modèle qui a appelé un outil récupère sa propre réflexion avec le résultat. Un nouveau message de votre part commence sans l'ancienne réflexion. Google demande cela pour Gemma 4 (*thoughts must not be removed between the function calls*, mais elles sont supprimées entre les tours). |
| **Always** | Également pour les réponses précédentes qui ont une réflexion, lorsque le chat la conserve (l'assistant de plan de l'Arc Outline le fait). Un serveur dont le modèle de template supprime seul l'ancienne réflexion, comme celui de Gemma 4, ignore ce réglage. |

Ce réglage s'applique à la boucle d'outils (l'assistant de plan, les générateurs qui utilisent des outils) et aux agents : un agent récupère la réflexion de ses tours précédents tant que les appels d'outils d'un même tour se poursuivent (la réflexion est conservée avec le message dans la console, et non dans le message enregistré). Le serveur doit accepter ce champ : llama.cpp l'accepte (il lit `reasoning_content`, et non `reasoning`), y compris derrière LiteLLM ; un fournisseur qui rejette les champs de message inconnus échoue, auquel cas laissez le réglage sur *None*.

![Raisonnement d'une partie générée](../images/reasoning-view.png)

Le raisonnement compte dans la réponse : donnez aux modèles de raisonnement un **Max Response Tokens** plus grand, sinon le texte risque d'être coupé après un raisonnement long.

<a id="token-counting"></a>

## Comptage des tokens

Marginalia compte les tokens pour décider de la quantité d'histoire qui tient dans le prompt. Il interroge le fournisseur lorsqu'il le peut, et essaie ces méthodes dans l'ordre :

1. le point d'accès de comptage de tokens d'OpenAI (`/v1/responses/input_tokens`),
2. le point d'accès `/tokenize` de llama.cpp,
3. le point d'accès `/tokenize` de LiteLLM,
4. le point d'accès `/v1/messages/count_tokens` de LiteLLM (pour les modèles Anthropic),
5. une estimation locale avec le tokenizer d'OpenAI.

La première méthode qui fonctionne est utilisée ensuite. L'estimation locale est proche pour la plupart des modèles, mais elle n'est pas exacte ; c'est une raison de plus de garder **Max Context Size** un peu en dessous de la limite réelle. Les fournisseurs de type *Extension* comptent selon leur extension ; ceux de [Google Gemini](extensions/google-genai.md) et [Anthropic Claude](extensions/anthropic.md) demandent au fournisseur de compter.

<a id="prompt-cache"></a>

## Cache de prompt

Chaque requête que Marginalia fait pour un type de travail (une nouvelle partie, une annotation, une réécriture, un résumé...) transporte l'en-tête `X-Session-ID`, un hash identique pour le même utilisateur et le même type de travail. Un serveur ou un proxy qui conserve un cache de prompt peut s'en servir pour retrouver le cache de la requête précédente de ce type. Un serveur qui ne le connaît pas l'ignore. Voir [Cache de prompt et identifiant de session](prompt-cache.md), avec un exemple pour un emplacement de llama.cpp sauvegardé sur disque.

<a id="shared-providers"></a>

## Fournisseurs partagés

Sur une installation avec plusieurs utilisateurs, un administrateur peut **partager** un fournisseur : cochez **Shared with all users** dans la boîte de dialogue du fournisseur (la case n'apparaît que pour les administrateurs). Chaque utilisateur retrouve alors ce fournisseur dans la liste **Model** de ses livres et dans son onglet **Inference Providers**, marqué *Shared* **Yes**. Il peut l'utiliser, mais il ne peut pas l'ouvrir, voir son adresse ou sa clé API, le modifier ni le supprimer ; seul son propriétaire le peut. Les livres, sauvegardes et restaurations qui font référence à un fournisseur partagé continuent de fonctionner pour toutes les personnes qui le voient.

![Un fournisseur partagé dans l'onglet Inference Providers d'un utilisateur ordinaire](../images/provider-shared-user.png)

Partager un fournisseur permet aux autres utilisateurs de consommer le quota du propriétaire auprès du service. Pour empêcher les utilisateurs d'ajouter leurs propres fournisseurs, afin qu'ils ne puissent utiliser que les fournisseurs partagés, voir [Utilisateurs et fournisseurs](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Limites des fournisseurs partagés

Un administrateur peut limiter ce que chaque utilisateur peut faire avec un fournisseur partagé. Dans l'onglet **Inference Providers**, l'administrateur propriétaire d'un fournisseur partagé dispose d'un bouton **Limits** (un utilisateur avec un cadenas) dans sa ligne. Ce bouton ouvre un tableau contenant les limites définies. **Add limit** en crée une nouvelle : choisissez l'utilisateur et un type de limite.

![Les limites d'un fournisseur partagé](../images/provider-limits.png)

| Type de limite | Signification |
|---|---|
| Unlimited | Aucune limite (équivalent à l'absence de limite). |
| Token limit | L'utilisateur peut utiliser jusqu'au nombre donné de jetons par période. |
| Not available | L'utilisateur ne peut pas utiliser le fournisseur ; toute requête qui lui serait envoyée échoue. |

Une **token limit** comporte un **reset** (quotidien, hebdomadaire à partir du lundi, ou mensuel ; les périodes suivent le calendrier, dans le fuseau horaire du serveur) et trois seuils qui peuvent chacun être laissés vides pour ne pas avoir de limite : les jetons **sent** (envoyés), les jetons **generated** (générés) et les deux ensemble (**in total**). La première limite atteinte bloque l'utilisateur jusqu'au prochain reset. Si les trois sont renseignés, la limite de jetons envoyés plus celle de jetons générés ne doit pas être inférieure à la limite totale (le total ne pourrait sinon jamais être atteint), faute de quoi la boîte de dialogue refuse l'enregistrement. Par exemple, un total de 100 000 avec 100 000 jetons envoyés et 20 000 générés est correct ; un total de 100 000 avec 20 000 envoyés et 10 000 générés ne l'est pas.

![Ajout d'une limite](../images/provider-limit-edit.png)

Rien n'est remis à zéro à intervalle fixe : les jetons utilisés repartent de zéro lorsque l'utilisateur utilise le fournisseur (ou consulte ses limites) pour la première fois d'une nouvelle période. La taille d'une requête est connue avant son envoi, donc une requête dont le prompt ferait dépasser à l'utilisateur la limite **sent** ou **in total** est refusée. (Le prompt est mesuré avec l'estimation propre à Marginalia, qui peut différer légèrement du décompte du service.) Les jetons **generated** ne sont pas connus à l'avance : ils sont comptés à la fin de la requête. L'utilisateur n'est donc arrêté qu'une fois la limite atteinte, et la dernière réponse peut la dépasser. Les utilisateurs absents du tableau ne sont soumis à aucune limite.

Pour empêcher que les jetons générés dépassent la limite, cochez **Hard limit** (disponible uniquement avec une limite de jetons générés). Une requête réserve une marge pour la plus grande réponse autorisée (le **Max Response Size** du fournisseur, ou le plus petit des deux seuils du protocole). Avec une hard limit, elle est refusée lorsque cette marge ne tient pas dans ce qui reste de la limite de jetons générés. Ainsi, une hard limit inférieure à la taille de réponse du fournisseur refuse toutes les requêtes. Le tableau et les informations de l'utilisateur affichent *hard* à côté d'une telle limite. La hard limit ne porte que sur les jetons générés : la limite **in total** peut encore être dépassée par la dernière réponse.

Chaque utilisateur voit les fournisseurs partagés dans son onglet sous forme de lignes en lecture seule. Le bouton **info** (le cercle avec un *i*) d'un fournisseur partagé affiche sa propre limite : ce qu'il a utilisé pendant la période en cours, les limites et leur date de reset. Lorsqu'une limite est atteinte, l'éditeur de récit, les résumés et les extensions affichent une erreur au lieu d'envoyer la requête.

![Les limites vues par un utilisateur, avec ce qui a été utilisé pendant la période en cours](../images/provider-limit-info.png)

<a id="usage"></a>

## Utilisation

L'onglet **Inference Providers** affiche, pour chaque fournisseur, les **Tokens sent** (le prompt, « prefill ») et les **Tokens generated** (la réponse, raisonnement compris) que vous avez utilisés avec lui, cumulés sur tous vos livres, résumés et extensions. Un fournisseur partagé dispose d'un compteur propre à chaque utilisateur : vous ne voyez que votre part.

Les administrateurs disposent aussi d'un bouton **Usage per user** sur l'onglet, qui ouvre un tableau indiquant ce que chaque utilisateur a consommé de chaque fournisseur (jetons envoyés, jetons générés, nombre de requêtes et date de la dernière).

![Utilisation par utilisateur](../images/provider-usage.png)

Les chiffres sont ceux que remonte le service. Un service qui ne les fournit pas (ou une requête que vous interrompez en cours de route) est compté avec l'estimation locale décrite dans [Comptage des jetons](#token-counting), les chiffres sont donc approximatifs. Une requête qui échoue avant l'arrivée du moindre texte n'est pas comptée. Sauf si un administrateur a défini une [limite](#limits-of-shared-providers) pour vous sur un fournisseur partagé, ces compteurs sont uniquement informatifs.

!!!
Marginalia demande au service de fournir les décomptes de jetons (`stream_options` avec `include_usage`). Pour modifier ce comportement pour un fournisseur, ajoutez votre propre `stream_options` dans ses **Additional Parameters**, par exemple `{"stream_options": {"include_usage": false}}` : il remplace ce que Marginalia envoie, et l'estimation locale est utilisée.
!!!

<a id="using-a-provider-in-a-book"></a>

## Utiliser un fournisseur dans un livre

Chaque livre a son modèle dans le champ **Model** de son onglet **About**. Pour utiliser un fournisseur pour tous les nouveaux livres, sélectionnez-le comme **Default Model** dans l'onglet **Settings**. Un changement de fournisseur pour un livre prend effet avec la prochaine partie générée ; les parties écrites auparavant restent inchangées.

<a id="troubleshooting"></a>

## Dépannage

| Problème | Que vérifier |
|---|---|
| *Failed to download model list* | L'URL se termine par `/v1`, la clé API, le serveur est en marche et accessible depuis Marginalia. Saisissez l'identifiant du modèle si le service ne fournit pas de liste de modèles. |
| *The provider did not answer in time.* | Augmentez le **Timeout (seconds)** du fournisseur ; les modèles de raisonnement peuvent avoir besoin de plusieurs minutes. |
| *The provider is limiting requests.* | Attendez, ou augmentez les **Retries**. |
| *Book is missing model.* | Le livre n'a pas de fournisseur. Sélectionnez-en un dans **About → Model**. |
| *Contextual limit not sufficient.* | Le prompt sans aucune histoire ne tient pas dans le contexte moins les jetons de réponse. Augmentez le **Max Context Size** (ou le **Max Context Tokens** du protocole), réduisez le **Max Response Tokens**, ou raccourcissez les modèles de prompt et les entrées du lorebook. |
| Le texte s'arrête au milieu | Le **Max Response Tokens** (ou le **Max Reply Tokens** du protocole) est trop faible, surtout pour les modèles de raisonnement. |
| Erreurs de l'API avec le raisonnement activé | L'API ne prend pas en charge `reasoning_effort`. Désactivez **Enable Reasoning**. |
