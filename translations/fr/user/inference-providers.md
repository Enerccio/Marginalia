---
label: Fournisseurs d'inférence
order: 980
icon: server
---

# Fournisseurs d'inférence

Un fournisseur d'inférence est une connexion à un modèle : l'adresse de l'API, la clé d'API, le modèle et ses limites. Chaque
livre utilise un fournisseur. Vous pouvez en avoir autant que vous le souhaitez, par exemple un grand modèle dans le cloud
pour l'histoire et un petit modèle local pour les expériences, et changer de fournisseur pour un livre à tout moment.

Les fournisseurs appartiennent à votre compte. Les autres utilisateurs ne voient ni eux ni leurs clés d'API. Les clés d'API
sont stockées chiffrées (voir [Le dossier de données](administration/index.md#the-data-folder)).

Ajouter un fournisseur nécessite la permission **Créer ses propres modèles**, que vous accorde un administrateur (les administrateurs
l'ont toujours, voir [Permissions](administration/permissions.md)). Sans elle, vous ne pouvez utiliser que les fournisseurs que les
administrateurs [partagent](#shared-providers) avec vous ; le bouton **Ajouter un fournisseur d'inférence** n'apparaît pas.

![L'onglet Fournisseurs d'inférence](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## API prises en charge

Marginalia fonctionne avec toute API qui implémente l'interface **OpenAI Chat Completions** (`/v1/chat/completions`,
avec streaming). La plupart des services et des serveurs locaux le font :

| Service | URL OpenAI |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| Proxy LiteLLM | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Les ports sont ceux par défaut de chaque serveur ; utilisez ceux sur lesquels votre serveur fonctionne réellement.

!!!warning Port 8080
La version Docker de Marginalia utilise aussi le port 8080. Si vous lancez llama.cpp sur la même machine, démarrez-le sur un
autre port (`llama-server --port 8081`).
!!!

!!!
Avec la [version Docker](getting-started/docker-server.md), `localhost` désigne le conteneur de Marginalia. Utilisez
`http://host.docker.internal:<port>/v1` pour un serveur de modèles sur la même machine, voir
[Modèles sur la même machine](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Ajouter un fournisseur

Ouvrez l'onglet **Fournisseurs d'inférence** et cliquez sur **Ajouter un fournisseur d'inférence**. Les fournisseurs existants se modifient avec le bouton en forme de crayon et se suppriment avec le bouton en forme de corbeille. Le bouton de clonage (deux feuilles) crée une copie d'un fournisseur sous un nom que vous confirmez (celui de l'original suivi de *(Copy)*) : la copie a la même adresse, le même modèle, la même **clé d'API** (ou les paramètres de l'extension), le même raisonnement, les mêmes limites de jetons et les mêmes paramètres, si bien qu'un second modèle sur le même service ne demande qu'un nom et un clic. La copie n'est pas partagée et n'a ni limites ni utilisation propres. Vous ne pouvez cloner que vos propres fournisseurs (pas un fournisseur partagé par un administrateur, dont vous ne devez pas voir la clé) et seulement lorsque vous êtes autorisé à créer des fournisseurs.

En haut de la boîte de dialogue :

| Champ | |
|---|---|
| **Nom** | Votre nom pour le fournisseur, affiché lorsque vous choisissez un modèle pour un livre. Incluez le modèle, par exemple `OpenRouter - Claude Sonnet`. |
| **Type** | Le type d'API : **Compatible OpenAI**, ou **Extension** pour un fournisseur qu'apporte une [extension](extensions/index.md) (il n'est proposé que lorsqu'une telle extension est chargée, par exemple [Google Gemini](extensions/google-genai.md) ou [Anthropic Claude](extensions/anthropic.md)). Le type ne peut plus être modifié une fois le fournisseur créé. |
| **Fournisseur** | Uniquement pour le type **Extension** : l'extension dont ce fournisseur provient. Ses propres paramètres remplissent l'onglet *Paramètres par type*. |

<a id="per-type-settings"></a>

### Paramètres par type

![Paramètres par type avec la liste des modèles chargée](../images/inference-provider.png)

L'onglet est plus haut que la boîte de dialogue et défile. Les champs du bas :

![Délai d'attente, nouvelles tentatives, paramètres supplémentaires et Tester la connexion](../images/inference-provider-2.png)

| Champ | |
|---|---|
| **URL OpenAI (se termine par /v1)** | L'adresse de base de l'API, voir [le tableau ci-dessus](#supported-apis). Sans `/chat/completions`. |
| **Clé d'API** | La clé du service. Un serveur local ne la vérifie généralement pas : laissez le champ vide et Marginalia envoie le substitut `none`. |
| **Modèle** | Le modèle à utiliser. Cliquez sur **Actualiser** pour charger la liste des modèles depuis l'API, ou saisissez vous-même l'identifiant du modèle (par exemple `anthropic/claude-sonnet-4.5` sur OpenRouter). |
| **Délai d'attente (secondes)** | Le temps d'attente de la réponse de l'API, puis de la suite du texte une fois qu'elle a commencé à écrire. La valeur par défaut est 300. Les modèles de raisonnement peuvent mettre plusieurs minutes avant le premier texte ; une requête qui n'entend rien pendant ce délai échoue avec un message de dépassement de délai (la réflexion d'un modèle compte comme du texte, donc un modèle qui continue de réfléchir n'est pas interrompu). Une modification est utilisée par la requête suivante, y compris celle d'un agent en cours de travail. |
| **Nouvelles tentatives** | Le nombre de répétitions d'une requête lorsque l'API répond par une limitation de débit (429) ou une erreur de serveur (5xx), ou lorsqu'elle est injoignable. Le délai entre les tentatives augmente, et l'en-tête `Retry-After` de l'API est respecté. La valeur par défaut est 2 ; 0 désactive les nouvelles tentatives. Seul le début d'une requête est réessayé : un texte déjà reçu n'est jamais écrit deux fois. |
| **Tokeniseur** | La façon dont Marginalia compte les jetons des textes pour ce fournisseur. **Automatique** (la valeur par défaut) essaie les points de terminaison de tokenisation du serveur l'un après l'autre (OpenAI, llama.cpp, LiteLLM) et utilise le premier qui répond, ou une estimation locale si aucun ne répond. Choisissez-en un pour n'utiliser que celui-là, ou **LOCAL_TOKKIT** pour l'estimation locale qui n'appelle jamais le serveur (utile lorsque le comptage obligerait le serveur à charger un autre modèle). Un tokeniseur fourni par une extension figure aussi dans la liste ; il reçoit l'adresse, le modèle et le texte, jamais la clé d'API, et seulement lorsque vous le choisissez. **Tester le tokeniseur** compte un texte d'exemple avec ce que le fournisseur essaierait et affiche la réponse de chacun. Lorsque vous créez un fournisseur, un tokeniseur d'extension qui correspond à l'adresse est présélectionné jusqu'à ce que vous choisissiez vous-même. |
| **Renvoyer la réflexion** | Indique si le modèle reçoit sa propre réflexion lors de la requête suivante, voir [Renvoyer la réflexion](#send-the-thinking-back). Seulement pour les fournisseurs compatibles OpenAI : les fournisseurs Anthropic et Google envoient d'eux-mêmes ce que leurs API demandent. |
| **Paramètres supplémentaires** | Un objet JSON contenant des champs supplémentaires pour chaque requête vers ce fournisseur, par exemple `{"top_k": 40, "min_p": 0.05}` pour un serveur local, ou `{"provider": {"order": ["anthropic"]}}` pour OpenRouter. Un champ défini ici l'emporte sur le même réglage venant du [protocole](protocols.md) (par exemple `temperature`) ou sur la limite de réponse (`max_completion_tokens`). Doit être un objet JSON valide, ou rester vide. |

Lorsque vous modifiez un fournisseur existant, la clé d'API n'est plus affichée. Elle est conservée telle quelle, sauf si vous cliquez sur **Réinitialiser la clé d'API** et saisissez une nouvelle clé.

**Actualiser** demande à l'API sa liste de modèles (`/v1/models`) avec l'URL et la clé du formulaire. Si cela échoue avec le message *Failed to download model list*, vérifiez l'URL (elle doit se terminer par `/v1`), la clé, et que le serveur est bien lancé. Certains services ne listent pas leurs modèles ; saisissez alors l'identifiant du modèle.

**Tester la connexion** vérifie les valeurs du formulaire sans rien enregistrer : il demande à l'API sa liste de modèles, puis une réponse d'un jeton au **Modèle** sélectionné, sans nouvelle tentative. Un message de succès signifie que l'URL, la clé et le modèle fonctionnent ensemble. Un échec indique la cause (voir [Messages d'erreur](#error-messages)). Certains services n'ont pas de liste de modèles ; le test se limite alors à la réponse d'un jeton.

<a id="general-settings"></a>

### Paramètres généraux

![Paramètres généraux d'un fournisseur d'inférence](../images/inference-provider-general.png)

| Champ | |
|---|---|
| **Taille maximale du contexte** | Obligatoire. Le nombre total de jetons que le modèle accepte, prompt et réponse réunis. |
| **Nombre maximal de jetons de réponse** | Obligatoire. La réponse la plus longue que le modèle peut écrire. Envoyé à l'API sous le nom `max_completion_tokens`, et cette quantité du contexte est réservée à la réponse. Doit être inférieur à *Taille maximale du contexte*. |
| **Connexions** | Le nombre de requêtes pouvant être en route vers ce modèle en même temps : toutes, quels que soient l'utilisateur et le type (votre génération, une requête annexe, une traduction, une annotation, [agents](agents.md#one-model-several-agents), extensions). Les autres attendent leur tour, dans l'ordre d'arrivée ; une génération qui doit attendre affiche *Waiting for the model: N requests before this one*, et **Arrêter** la retire de la file d'attente. **1** (la valeur par défaut) pour un serveur local à un seul emplacement (un GPU) ; un modèle cloud qui répond à plusieurs requêtes à la fois peut en accepter davantage, afin qu'une requête annexe puisse tourner en parallèle d'une génération. |
| **Envoyer les images du lore** | Envoie les [images des entrées du lorebook](lorebooks/lorebooks.md#editing-in-full-screen) qu'un tour utilise, avec leur texte. Désactivé par défaut : activez-le uniquement pour un modèle qui accepte les images, car un modèle sans vision refuse la requête et le tour échoue. Les images des entrées du bloc de lore vont dans un message distinct juste après le prompt système (avec une réponse courte du modèle), les autres avec votre prompt. |
| **Images dans le message système** | (Compatible OpenAI, avec *Envoyer les images du lore*) Place les images du bloc de lore dans le message système lui-même, à côté du texte du lore. Uniquement pour un serveur et un modèle de chat qui acceptent les images à cet endroit ; la plupart ne le font pas, et la requête échoue. |
| **Nécessite un jailbreak**, **Prompt de jailbreak** | Un prompt supplémentaire pour les modèles qui refusent d'écrire certains contenus. Lorsqu'elle est cochée, le prompt de jailbreak est placé tout au début du prompt. |
| **Activer le raisonnement**, **Effort de raisonnement** | Pour les modèles de raisonnement (« thinking »), voir [Modèles de raisonnement](#reasoning-models). |

Marginalia réserve au contexte le **Nombre maximal de jetons de réponse** pour la réponse, et remplit le reste avec le prompt : les modèles, les entrées du [lorebook](lorebooks/index.md) et les [résumés](books/summaries.md) y sont comptés, et les parties les plus anciennes de l'histoire qui ne tiennent pas sont laissées de côté. Avec un contexte de 32 000 jetons et des réponses allant jusqu'à 2 000 jetons, le prompt dispose de 30 000 jetons.

Reprenez la **Taille maximale du contexte** de la documentation du service, et réglez-la un peu plus bas : les comptages de jetons ne sont pas toujours exacts, et une requête qui dépasse la limite réelle échoue. Un [protocole](protocols.md) peut abaisser les deux limites pour certains livres, mais jamais les relever.
<a id="error-messages"></a>

## Messages d'erreur

Lorsque l'API refuse une requête, Marginalia indique ce qu'il faut corriger au lieu d'afficher une erreur interne, suivi de
ce que l'API elle-même a répondu :

![Une erreur du fournisseur affichée à l'utilisateur](../images/error-dialog.png)

| Message | Cause |
|---|---|
| *Le fournisseur a rejeté la clé d'API.* | **Clé d'API** erronée, expirée ou absente. |
| *La clé d'API n'est pas autorisée à utiliser ce modèle ou ce point d'accès.* | La clé est valide mais n'a pas accès au modèle. |
| *Le fournisseur ne connaît pas le modèle configuré.* | **Modèle** erroné ; utilisez **Actualiser** pour voir les noms connus de l'API. |
| *Rien n'a été trouvé à l'URL configurée.* | **URL OpenAI** erronée ; elle se termine généralement par `/v1`. |
| *Le prompt ne tient pas dans le contexte du modèle.* | **Taille maximale du contexte** est supérieure à ce que le modèle accepte réellement. Diminuez-la. |
| *Le fournisseur limite les requêtes.* | Limite de débit ou quota épuisé, même après les nouvelles tentatives. |
| *Le fournisseur n'a pas pu traiter la requête.* | Le service a un problème (5xx), même après les nouvelles tentatives. |
| *Le fournisseur n'a pas répondu à temps.* | Augmentez **Délai d'attente (secondes)**, ou vérifiez le serveur. |
| *Impossible de se connecter au fournisseur.* | Le serveur ne fonctionne pas ou n'est pas joignable depuis Marginalia, ou l'URL contient une faute de frappe. |
| *Le fournisseur a rejeté la requête.* | Autre refus, souvent un champ non pris en charge dans **Paramètres supplémentaires** ou un raisonnement que le modèle ne supporte pas. |

<a id="warning-before-sending"></a>

## Avertissement avant l'envoi

Marginalia ne remplit le contexte que jusqu'à **Taille maximale du contexte** moins **Nombre maximal de jetons de réponse**. Si le prompt final dépasse encore cette limite (par exemple parce qu'une extension a ajouté du texte, ou que le comptage des jetons était inexact), un avertissement affiche les tailles estimées avant l'envoi de la requête. La requête est tout de même envoyée ; l'API peut la refuser ou tronquer le prompt.

![L'avertissement concernant un prompt qui risque de ne pas tenir](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modèles de raisonnement

Les modèles de raisonnement réfléchissent avant de répondre. Lorsque **Activer le raisonnement** est coché, Marginalia envoie l'**Effort de raisonnement** choisi (*Aucun*, *Faible*, *Moyen* ou *Élevé*) à l'API sous la forme `reasoning_effort`. Plus d'effort donne en général un texte mieux planifié, mais des réponses plus lentes et plus coûteuses. Toutes les API ne prennent pas ce paramètre ; si les requêtes échouent avec le raisonnement activé, désactivez-le.

Lorsque le modèle renvoie son raisonnement (sous la forme `reasoning_content` ou `reasoning`, selon le serveur), il est affiché au-dessus de la partie générée, sous **Voir le raisonnement**. Ce raisonnement ne fait pas partie de l'histoire et n'est pas renvoyé au modèle, sauf si vous activez **Renvoyer la réflexion** (voir ci-dessous).

<a id="send-the-thinking-back"></a>

### Renvoyer la réflexion

**Renvoyer la réflexion** (dans *Paramètres par type* d'un fournisseur compatible OpenAI) indique si la réflexion du modèle lui est renvoyée, sous la forme `reasoning_content` de son message précédent, dans la requête suivante :

| Valeur | |
|---|---|
| **Aucun** (par défaut) | Jamais. Convient à la plupart des modèles et serveurs, ainsi qu'aux API qui refusent un champ inconnu. |
| **Entre les appels d'outils** | Seulement tant que les appels d'outils d'une même réponse se poursuivent : le modèle qui a appelé un outil récupère sa propre réflexion avec le résultat. Un nouveau message de votre part commence sans l'ancienne réflexion. C'est ce que demande Google pour Gemma 4 (*thoughts must not be removed between the function calls*, mais supprimées entre les tours). |
| **Toujours** | Également avec les réponses précédentes qui ont une réflexion, lorsque le chat la conserve (l'assistant de intrigue de l'Arc Outline le fait). Un serveur dont le modèle de template supprime seul l'ancienne réflexion, comme celui de Gemma 4, l'ignore. |

Cela s'applique à la boucle d'outils (l'assistant de intrigue, les générateurs qui utilisent des outils) et aux agents : un agent récupère la réflexion de ses tours précédents tant que les appels d'outils d'un même tour se poursuivent (la réflexion est conservée avec le message dans la console, et non dans le message enregistré). Le serveur doit accepter ce champ : llama.cpp l'accepte (il lit `reasoning_content`, et non `reasoning`), y compris derrière LiteLLM ; un fournisseur qui rejette les champs de message inconnus échouera, il faut alors le laisser sur *Aucun*.

![Raisonnement d'une partie générée](../images/reasoning-view.png)

Le raisonnement compte dans la réponse : donnez aux modèles de raisonnement un **Nombre maximal de jetons de réponse** plus élevé, sinon le texte risque d'être coupé après un long raisonnement.

<a id="token-counting"></a>

## Comptage des jetons

Marginalia compte les jetons pour décider de la quantité d'histoire qui tient dans le prompt. Il interroge le fournisseur lorsque c'est possible, et essaie ces méthodes dans l'ordre :

1. le point d'accès de comptage des jetons d'OpenAI (`/v1/responses/input_tokens`),
2. le point d'accès `/tokenize` de llama.cpp,
3. le point d'accès `/tokenize` de LiteLLM,
4. le point d'accès `/v1/messages/count_tokens` de LiteLLM (pour les modèles Anthropic),
5. une estimation locale avec le tokenizer d'OpenAI.

La première méthode qui fonctionne est utilisée ensuite. L'estimation locale est proche pour la plupart des modèles, mais pas exacte, ce qui est une raison de plus de garder **Taille maximale du contexte** un peu en dessous de la limite réelle. Les fournisseurs de type *Extension* comptent selon ce que dit leur extension ; ceux de [Google Gemini](extensions/google-genai.md) et [Anthropic Claude](extensions/anthropic.md) demandent au fournisseur de compter.

<a id="prompt-cache"></a>

## Cache de prompt

Chaque requête que Marginalia envoie pour un type de travail (une nouvelle partie, une annotation, une réécriture, un résumé...) porte l'en-tête `X-Session-ID`, un hachage identique pour un même utilisateur et un même type de travail. Un serveur ou un proxy qui conserve un cache de prompt peut s'en servir pour retrouver le cache de la requête précédente de ce type. Un serveur qui ne le connaît pas l'ignore. Voir [Cache de prompt et identifiant de session](prompt-cache.md), avec un exemple pour un emplacement de llama.cpp conservé sur disque.

<a id="shared-providers"></a>

## Fournisseurs partagés

Sur une installation à plusieurs utilisateurs, un administrateur peut **partager** un fournisseur : cochez **Partagé avec tous les utilisateurs** dans la boîte de dialogue du fournisseur (la case n'apparaît que pour les administrateurs). Chaque utilisateur le trouve alors dans la liste **Modèle** de ses livres et dans son onglet **Fournisseurs d'inférence**, marqué *Partagé* **Oui**. Il peut l'utiliser, mais il ne peut pas l'ouvrir, voir son adresse ou sa clé d'API, le modifier ou le supprimer ; seul son propriétaire le peut. Les livres, sauvegardes et restaurations qui font référence à un fournisseur partagé continuent de fonctionner pour toutes les personnes qui peuvent le voir.

![Un fournisseur partagé dans l'onglet Fournisseurs d'inférence d'un utilisateur ordinaire](../images/provider-shared-user.png)

Partager un fournisseur permet aux autres utilisateurs de consommer le quota du propriétaire auprès du service. Pour empêcher les utilisateurs d'ajouter leurs propres fournisseurs, afin qu'ils ne puissent utiliser que les partagés, voir [Utilisateurs et fournisseurs](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Limites des fournisseurs partagés

Un administrateur peut limiter ce que chaque utilisateur peut faire avec un fournisseur partagé. Dans l'onglet **Fournisseurs d'inférence**, l'administrateur propriétaire d'un fournisseur partagé dispose d'un bouton **Limites** (un utilisateur avec un cadenas) sur sa ligne, qui ouvre un tableau avec les limites définies. **Ajouter une limite** en crée une nouvelle : choisissez l'utilisateur et un type de limite.

![Les limites d'un fournisseur partagé](../images/provider-limits.png)

| Type de limite | Signification |
|---|---|
| Illimitée | Aucune limite (identique à l'absence de limite). |
| Limite de jetons | L'utilisateur peut utiliser jusqu'au nombre donné de jetons par période. |
| Non disponible | L'utilisateur ne peut pas utiliser le fournisseur ; tout ce qui enverrait une requête à celui-ci échoue. |

Une **limite de jetons** a une **réinitialisation** (quotidienne, hebdomadaire à partir du lundi, ou mensuelle ; périodes calendaires dans le fuseau horaire du serveur) et trois limites qui peuvent chacune être laissées vides pour ne pas avoir de limite : les jetons **envoyés**, les jetons **générés**, et les deux ensemble (**au total**). La première limite atteinte bloque l'utilisateur jusqu'à la prochaine réinitialisation. Si les trois sont définies, la limite des envoyés plus celle des générés ne doit pas être inférieure au total (le total ne pourrait sinon jamais être atteint), faute de quoi la boîte de dialogue refuse l'enregistrement. Par exemple, un total de 100 000 avec 100 000 envoyés et 20 000 générés est acceptable ; un total de 100 000 avec 20 000 envoyés et 10 000 générés ne l'est pas.

![Ajout d'une limite](../images/provider-limit-edit.png)

Rien n'est réinitialisé par minuterie : les jetons utilisés repartent de zéro lorsque l'utilisateur utilise le fournisseur (ou consulte ses limites) pour la première fois d'une nouvelle période. La taille d'une requête est connue avant son envoi ; une requête dont le prompt ferait dépasser à l'utilisateur la limite des **envoyés** ou la limite **au total** est donc refusée (le prompt est mesuré avec l'estimation propre à Marginalia, qui peut légèrement différer du décompte du service). Les jetons **générés** ne sont pas connus à l'avance : ils sont comptés à la fin de la requête, si bien que l'utilisateur n'est bloqué qu'une fois la limite atteinte, et la dernière réponse peut la dépasser. Les utilisateurs sans limite dans le tableau ne sont pas limités.

Pour empêcher les jetons générés de dépasser la limite, cochez **Limite stricte** (disponible uniquement avec une limite de jetons générés). Une requête réserve de la place pour la plus grande réponse autorisée (la **Taille maximale de réponse** du fournisseur, ou la plus petite des deux valeurs du protocole), et avec une limite stricte, elle est refusée lorsque cette réserve ne tient pas dans ce qui reste de la limite des générés. Ainsi, une limite stricte inférieure à la taille de réponse du fournisseur refuse toutes les requêtes ; le tableau et les informations de l'utilisateur affichent *stricte* à côté d'une telle limite. La limite stricte ne concerne que les jetons générés : la limite **au total** peut encore être dépassée par la dernière réponse.

Chaque utilisateur voit les fournisseurs partagés dans son onglet sous forme de lignes en lecture seule. Le bouton **info** (le cercle avec un *i*) d'un fournisseur partagé affiche sa propre limite : ce qu'il a utilisé sur cette période, les limites et le moment de leur réinitialisation. Lorsqu'une limite est atteinte, l'éditeur de récit, les résumés et les extensions affichent une erreur au lieu d'envoyer la requête.

![Les limites vues par un utilisateur, avec ce qui a été utilisé sur cette période](../images/provider-limit-info.png)

<a id="usage"></a>

## Utilisation

L'onglet **Fournisseurs d'inférence** affiche, pour chaque fournisseur, les **Jetons envoyés** (le prompt, « prefill ») et les **Jetons générés** (la réponse, raisonnement inclus) que vous avez utilisés avec lui, additionnés sur tous vos livres, résumés et extensions. Un fournisseur partagé dispose d'un compteur propre à chaque utilisateur, de sorte que vous ne voyez que votre part.

Les administrateurs disposent également d'un bouton **Utilisation par utilisateur** sur l'onglet, qui ouvre un tableau indiquant ce que chaque utilisateur a consommé de chaque fournisseur (jetons envoyés, jetons générés, requêtes et date de la dernière).

![Utilisation par utilisateur](../images/provider-usage.png)

Les chiffres sont ceux que transmet le service. Un service qui ne les transmet pas (ou une requête que vous interrompez en cours de route) est compté avec l'estimation locale décrite dans [Comptage des jetons](#token-counting), de sorte que ces chiffres sont approximatifs. Une requête qui échoue avant l'arrivée de tout texte n'est pas comptée. Sauf si un administrateur a défini une [limite](#limits-of-shared-providers) pour vous sur un fournisseur partagé, les compteurs ne sont qu'informatifs.

!!!
Marginalia demande au service de transmettre les décomptes de jetons (`stream_options` avec `include_usage`). Pour modifier cela pour un fournisseur, placez votre propre `stream_options` dans ses **Paramètres supplémentaires**, par exemple `{"stream_options": {"include_usage": false}}` : il remplace ce que Marginalia envoie, et l'estimation locale est alors utilisée.
!!!

<a id="using-a-provider-in-a-book"></a>

## Utiliser un fournisseur dans un livre

Chaque livre a son modèle dans le champ **Modèle** de son onglet **À propos**. Pour utiliser un fournisseur pour tous les nouveaux livres, sélectionnez-le comme **Modèle par défaut** dans l'onglet **Paramètres**. Un changement de fournisseur pour un livre prend effet à la prochaine partie générée ; les parties écrites auparavant restent inchangées.

<a id="troubleshooting"></a>

## Dépannage

| Problème | Que vérifier |
|---|---|
| *Failed to download model list* | L'URL se termine par `/v1`, la clé d'API, le serveur est lancé et accessible depuis Marginalia. Saisissez l'identifiant du modèle si le service ne fournit pas de liste de modèles. |
| *The provider did not answer in time.* | Augmentez le **Délai d'attente (secondes)** du fournisseur ; les modèles à raisonnement peuvent avoir besoin de plusieurs minutes. |
| *The provider is limiting requests.* | Attendez, ou augmentez les **Nouvelles tentatives**. |
| *Book is missing model.* | Le livre n'a pas de fournisseur. Sélectionnez-en un dans **À propos → Modèle**. |
| *Contextual limit not sufficient.* | Le prompt sans aucun récit ne tient pas dans le contexte moins les jetons de réponse. Augmentez la **Taille maximale du contexte** (ou le **Nombre maximal de jetons de contexte** du protocole), réduisez le **Nombre maximal de jetons de réponse**, ou raccourcissez les modèles et les entrées du lorebook. |
| Le texte s'arrête au milieu | Le **Nombre maximal de jetons de réponse** (ou le **Nombre maximal de jetons de réponse** du protocole) est trop bas, surtout pour les modèles à raisonnement. |
| Erreurs de l'API avec le raisonnement activé | L'API ne prend pas en charge `reasoning_effort`. Désactivez **Activer le raisonnement**. |
