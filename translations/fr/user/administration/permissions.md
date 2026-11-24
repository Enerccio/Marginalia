---
label: Autorisations
order: 95
---

# Autorisations

Une autorisation permet à un utilisateur de faire quelque chose qui n'est pas accessible à tout le monde. Les administrateurs donnent les autorisations aux utilisateurs une par une ; un utilisateur qui n'en a aucune peut faire tout le reste dans Marginalia comme d'habitude (écrire des livres, construire des lorebooks, utiliser les modèles que les administrateurs [partagent](../inference-providers.md#shared-providers)).

| Autorisation | Ce qu'elle permet | Réglages |
|---|---|---|
| **Create own models** | Ajouter des [fournisseurs d'inférence](../inference-providers.md) : sans elle, l'utilisateur ne peut utiliser que les fournisseurs que les administrateurs partagent ; ceux qu'il a déjà continuent de fonctionner et peuvent toujours être modifiés. | |
| **Help assistant** | Poser des questions à l'[assistant d'aide](help-assistant.md), la conversation qui répond à partir du manuel. | **Apply limits** et **Allow navigating the interface**, voir [Réglages d'une autorisation](#settings-of-a-permission). |
| **Agents** | (Expérimental, voir [Agents](../agents.md)) L'onglet [Agents](../agents.md) : créer des agents, les cloner et les exécuter sur un livre. | **Most agents**, **Concurrent runs**, **Requests to the model at once**, **Token budget of a run** et les quotas de copies et de fichiers, voir [Réglages d'une autorisation](#settings-of-a-permission). |
| **Version history** | Le nombre maximal de [versions d'une partie](../books/version-history.md#how-many-versions-are-kept) que conserve l'historique des livres de l'utilisateur. Chaque utilisateur en a 50 (un utilisateur sans la ligne aussi) ; une autorisation non accordée signifie aucun historique pour cet utilisateur. | **Versions kept per part**, un plafond que les dossiers, les livres et l'utilisateur ne peuvent pas dépasser. Un champ vide n'applique aucune limite ici. Une autorisation que vous ajoutez commence à 50. |
| **API access** | Les clés de l'[API HTTP](../account.md#api-keys) : des programmes qui agissent comme l'utilisateur dans ses livres. Retirer l'autorisation révoque les clés de l'utilisateur. Les administrateurs n'ont pas besoin d'autorisation, et seuls eux peuvent donner à une clé l'accès à la mise à jour ou aux extensions. | |
| **Notifications** | Le [message vers une URL](../agents.md#when-you-are-not-there) que le serveur envoie lorsqu'un agent a besoin de l'utilisateur et qu'aucun écran n'est ouvert. Le serveur appelle une adresse saisie par l'utilisateur, c'est donc une autorisation à part entière : sans elle, les champs n'apparaissent pas dans les réglages de l'utilisateur et rien n'est envoyé. | **Allow addresses on this machine and in private networks**, voir [Réglages d'une autorisation](#settings-of-a-permission). |

Les administrateurs ont toujours toutes les autorisations, ils n'en ont pas besoin, il n'y a donc rien à leur donner (les boutons et sections d'un administrateur ne dépendent jamais d'une autorisation). Les nouveaux utilisateurs n'ont aucune autorisation : accordez-leur ce dont ils ont besoin juste après les [avoir ajoutés](users.md#adding-a-user).

<a id="giving-and-taking-permissions"></a>

## Donner et retirer des autorisations

Le bouton clé dans **Admin → Users**, sur la ligne de l'utilisateur, ouvre les autorisations de cet utilisateur :

![Les autorisations d'un utilisateur](../../images/permissions-dialog.png)

La fenêtre est un tableau des autorisations de l'utilisateur, avec trois colonnes :

| Colonne | |
|---|---|
| **Name** | L'autorisation. Une autorisation qui a des réglages porte une flèche devant son nom, voir [Réglages d'une autorisation](#settings-of-a-permission). |
| **Granted** | Indique si l'autorisation est active. Une autorisation non accordée équivaut à aucune, mais elle garde ses réglages : vous pouvez la désactiver temporairement puis la réactiver. |
| Le bouton corbeille | Supprime l'autorisation (et ses réglages) définitivement. |

Pour donner une autorisation, cliquez sur **Add permission** au-dessus du tableau et choisissez l'une des autorisations que l'utilisateur n'a pas ; elle est ajoutée au tableau, accordée. Rien n'est enregistré tant que vous ne cliquez pas sur **Save** :

![Choisir une autorisation à ajouter](../../images/permissions-add.png)

- **Save** est désactivé tant qu'il n'y a rien à enregistrer.
- **Exit** ferme la fenêtre ; s'il reste des modifications non enregistrées, elle demande confirmation, pour qu'une modification ne soit jamais perdue par accident.
- Une autorisation que vous supprimez reste dans le tableau jusqu'à l'enregistrement, et n'est supprimée définitivement qu'à ce moment-là. D'ici là, pour la rétablir, fermez la fenêtre avec **Exit** sans enregistrer.

Après l'enregistrement, la page de l'utilisateur est rechargée s'il est connecté, afin que les boutons et sections qui dépendent des autorisations apparaissent ou disparaissent immédiatement. Votre propre page n'est pas rechargée, même lorsque vous modifiez vos propres autorisations.

<a id="settings-of-a-permission"></a>

## Réglages d'une autorisation

Certaines autorisations ont des réglages qui appartiennent à l'utilisateur, et non à l'installation entière. La ligne d'une telle autorisation commence par une flèche : cliquez dessus pour dérouler les réglages (ils sont repliés à l'ouverture de la fenêtre, et une autorisation que vous venez d'ajouter est dépliée), cliquez à nouveau pour les replier. Replier n'est qu'un moyen de garder une longue liste courte ; cela ne change rien et ne compte pas comme une modification non enregistrée.

![Les réglages de l'autorisation Help assistant](../../images/permissions-settings.png)

**Help assistant**, **Agents** et **Notifications** en ont. Voici ceux de l'assistant d'aide :

| Réglage | |
|---|---|
| **Apply limits** | Pour un modèle partagé, indique si les questions de l'utilisateur comptent dans sa limite et son usage comme toute autre requête, voir [Qui paie](help-assistant.md#who-pays). Désactivé par défaut. |
| **Allow navigating the interface** | Permet à l'assistant d'ouvrir une section, de sélectionner un onglet ou d'ouvrir la boîte de dialogue d'ajout lorsque l'utilisateur lui demande de lui montrer le chemin, voir [Indiquer le chemin](help-assistant.md#showing-the-way). Désactivé par défaut. |

Ceux des agents ; un champ vide correspond à la valeur par défaut :

| Réglage | |
|---|---|
| **Most agents** | Le nombre d'agents que l'utilisateur peut avoir, clones compris. Cinq si vide. Zéro permet à l'utilisateur de garder et d'exécuter ses agents, mais ni d'en créer ni d'en cloner un autre. Un agent supprimé libère sa place. |
| **Concurrent runs** | Le nombre d'exécutions de l'utilisateur pouvant travailler en même temps. Une si vide. |
| **Requests to the model at once (per run)** | Le nombre de requêtes au modèle qu'une exécution peut avoir en cours en même temps, l'agent et ses [sous-agents](../agents.md#sub-agents) ensemble. Une si vide : pendant qu'un sous-agent interroge le modèle, l'agent attend, et réciproquement. Une valeur plus élevée permet aux sous-agents de travailler en parallèle avec l'agent (le modèle de l'agent peut aussi avoir sa propre [limite de requêtes](../inference-providers.md#general-settings), qui s'applique également). |
| **Token budget of a run** | Le nombre maximal de jetons (envoyés et générés) qu'une exécution de l'utilisateur peut consommer. Vide ou zéro signifie aucune limite. |
| **Working copies** | Le nombre de copies de travail de livres que les exécutions de l'utilisateur conservent en même temps. Une exécution non terminée garde sa copie, une exécution en pause aussi, et la rend lorsqu'elle se termine, est arrêtée ou supprimée. Vide ou zéro signifie aucune limite autre que le nombre d'agents. |
| **Largest file (KB)** | Le plus gros fichier qu'un agent de l'utilisateur peut écrire dans son espace de travail. 1024 (1 Mo) si vide. |
| **Workspace size (MB)** | La taille totale que peuvent occuper tous les fichiers de l'espace de travail d'un agent de l'utilisateur. 20 si vide. |
| **Files in a workspace** | Le nombre de fichiers que peut contenir l'espace de travail d'un agent de l'utilisateur ; les dossiers ne comptent pas. 500 si vide. |
| **Checkpoints of a run (MB)** | La taille totale que peuvent occuper tous les [points de contrôle](../agents.md#checkpoints) d'une exécution (points de restauration de la copie de travail et de son espace de travail) ; une partie est réservée aux points automatiques. 100 si vide. |

![Les réglages de l'autorisation Agents](../../images/permissions-settings-agents.png)

Celui des notifications :

| Réglage | |
|---|---|
| **Allow addresses on this machine and in private networks** | Une sous-autorisation : la case ne peut être cochée que si **Notifications** est accordée (elle est grisée sinon, et retirer l'autorisation la vide). Sans elle, seules les adresses sur Internet sont appelées : ni `localhost`, ni un réseau privé (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), ni une adresse de lien local ou partagée (`169.254.x.x`, où les hébergeurs cloud gardent leur service de métadonnées, `100.64-127.x.x`), ni les adresses locales uniques IPv6. Cochez-la pour un utilisateur dont le serveur ntfy ou Gotify tourne sur la même machine ou sur votre propre réseau. Désactivé par défaut. Un nom est vérifié d'après les adresses vers lesquelles il pointe : un nom qui renvoie à une adresse privée est donc refusé aussi, de même qu'un nom introuvable. |

Les administrateurs n'ont besoin ni de l'autorisation ni de la sous-autorisation : ils reçoivent les messages et peuvent utiliser des adresses locales. Sur un serveur partagé avec des personnes auxquelles vous ne faites pas entièrement confiance, laissez la sous-autorisation désactivée : le serveur ferait sinon des requêtes vers n'importe quelle adresse saisie par l'utilisateur, à l'intérieur de votre réseau.

<a id="when-permissions-were-introduced"></a>

## Quand les autorisations ont été introduites

Avant les autorisations, **Users can create their own models** et l'interrupteur **Enabled** de l'assistant d'aide (avec **Apply limits** et **Allow navigating the interface**) étaient des réglages de toute l'installation. Marginalia les a transformés en autorisations lors de sa mise à jour : chaque utilisateur existant a alors reçu **Create own models** si tout le monde pouvait créer des modèles (ce qui était la valeur par défaut), et **Help assistant** avec les deux mêmes réglages si l'assistant était activé. Rien ne change pour eux tant que vous ne le modifiez pas ; les utilisateurs que vous ajouterez à partir de maintenant commencent sans aucune autorisation.
