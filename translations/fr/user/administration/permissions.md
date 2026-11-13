---
label: Autorisations
order: 95
---

# Autorisations

Une autorisation permet à un utilisateur de faire quelque chose qui n'est pas ouvert à tous. Les administrateurs accordent les autorisations aux utilisateurs une à une ;
un utilisateur qui n'en a aucune peut faire tout le reste dans Marginalia comme d'habitude (écrire des livres, construire des livres de référence, utiliser les modèles que les administrateurs [partagent](../inference-providers.md#shared-providers)).

| Autorisation | Ce qu'elle permet | Paramètres |
|---|---|---|
| **Créer ses propres modèles** | Ajouter des [fournisseurs d'inférence](../inference-providers.md) : sans elle, l'utilisateur ne peut utiliser que les fournisseurs que les administrateurs partagent ; les fournisseurs qu'il possède déjà continuent de fonctionner et peuvent toujours être modifiés. | |
| **Assistant d'aide** | Poser des questions à l'[assistant d'aide](help-assistant.md), le chat qui répond à partir du manuel. | **Appliquer les limites** et **Autoriser la navigation dans l'interface**, voir [Paramètres d'une autorisation](#settings-of-a-permission). |
| **Agents** | (Expérimental, voir [Agents](../agents.md)) L'onglet [Agents](../agents.md) : créer des agents, les cloner et les exécuter sur un livre. | **Nombre maximal d'agents**, **Exécutions simultanées**, **Requêtes simultanées au modèle (par exécution)**, **Budget de jetons d'une exécution** et les quotas de copies et de fichiers, voir [Paramètres d'une autorisation](#settings-of-a-permission). |
| **Historique des versions** | Le nombre maximal de [versions d'une partie](../books/version-history.md#how-many-versions-are-kept) que l'historique des livres de l'utilisateur conserve. Chaque utilisateur l'a avec 50 (un utilisateur sans la ligne aussi) ; une autorisation non accordée signifie aucun historique pour cet utilisateur. | **Versions conservées par partie**, un plafond que les dossiers, les livres et l'utilisateur ne peuvent pas dépasser. Un nombre vide ne fixe aucune limite ici. Une autorisation que vous ajoutez démarre à 50. |
| **Notifications** | Le [message vers une URL](../agents.md#when-you-are-not-there) que le serveur envoie lorsqu'un agent a besoin de l'utilisateur et qu'aucun écran n'est ouvert. Le serveur appelle une adresse saisie par l'utilisateur, c'est pourquoi il s'agit d'une autorisation à part entière : sans elle, les champs n'apparaissent pas dans les paramètres de l'utilisateur et rien n'est envoyé. | **Autoriser les adresses de cette machine et des réseaux privés**, voir [Paramètres d'une autorisation](#settings-of-a-permission). |

Les administrateurs ont toujours toutes les autorisations, ils n'en ont pas besoin, il n'y a donc rien à leur accorder (les boutons et sections
d'un administrateur ne dépendent jamais d'une autorisation). Les nouveaux utilisateurs n'ont aucune autorisation : accordez-leur ce dont ils ont besoin juste après
les avoir [ajoutés](users.md#adding-a-user).

<a id="giving-and-taking-permissions"></a>

## Accorder et retirer des autorisations

Le bouton clé dans **Admin → Utilisateurs**, sur la ligne de l'utilisateur, ouvre les autorisations de cet utilisateur :

![Les autorisations d'un utilisateur](../../images/permissions-dialog.png)

La fenêtre est un tableau des autorisations de l'utilisateur, avec trois colonnes :

| Colonne | |
|---|---|
| **Nom** | L'autorisation. Une autorisation qui a des paramètres porte une flèche devant son nom, voir [Paramètres d'une autorisation](#settings-of-a-permission). |
| **Accordée** | Indique si l'autorisation fonctionne. Une autorisation non accordée équivaut à aucune, mais elle est conservée avec ses paramètres, ce qui permet de la désactiver un temps puis de la réactiver. |
| Le bouton corbeille | Supprime l'autorisation (et ses paramètres) définitivement. |

Pour accorder une autorisation, cliquez sur **Ajouter une autorisation** au-dessus du tableau et choisissez l'une des autorisations que l'utilisateur n'a pas ;
elle est ajoutée au tableau, accordée. Rien n'est enregistré tant que vous ne cliquez pas sur **Enregistrer** :

![Choisir une autorisation à ajouter](../../images/permissions-add.png)

- **Enregistrer** est désactivé tant qu'il n'y a rien à enregistrer.
- **Quitter** ferme la fenêtre ; s'il reste des modifications non enregistrées, elle demande d'abord confirmation, afin qu'une modification ne soit jamais perdue
  par accident.
- Une autorisation que vous supprimez reste dans le tableau jusqu'à l'enregistrement, et n'est supprimée définitivement qu'à ce moment-là. Avant cela, rétablissez-la en fermant la fenêtre avec **Quitter** sans enregistrer.

Après l'enregistrement, la page de l'utilisateur est rechargée s'il est connecté, afin que les boutons et sections qui dépendent des
autorisations apparaissent ou disparaissent immédiatement. Votre propre page n'est pas rechargée, même lorsque vous modifiez vos propres autorisations.

<a id="settings-of-a-permission"></a>

## Paramètres d'une autorisation

Certaines autorisations ont des paramètres qui appartiennent à l'utilisateur, et non à l'installation entière. La ligne d'une telle autorisation
commence par une flèche : cliquez dessus pour déplier les paramètres (ils sont repliés à l'ouverture de la fenêtre, et une autorisation que vous venez
d'ajouter est dépliée), cliquez à nouveau pour les replier. Le pliage sert seulement à garder une longue liste courte ; il ne change rien et ne compte pas comme une modification non enregistrée.

![Les paramètres de l'autorisation Assistant d'aide](../../images/permissions-settings.png)

**Assistant d'aide**, **Agents** et **Notifications** en ont. Ceux de l'assistant d'aide :

| Paramètre | |
|---|---|
| **Appliquer les limites** | Pour un modèle partagé, indique si les questions de l'utilisateur comptent dans sa limite et sa consommation comme toute autre requête, voir [Qui paie](help-assistant.md#who-pays). Désactivé par défaut. |
| **Autoriser la navigation dans l'interface** | Permet à l'assistant d'ouvrir une section, de sélectionner un onglet ou d'ouvrir la fenêtre d'ajout de quelque chose lorsque l'utilisateur lui demande de lui montrer le chemin, voir [Montrer le chemin](help-assistant.md#showing-the-way). Désactivé par défaut. |

Ceux des agents ; un champ vide correspond à la valeur par défaut :

| Paramètre | |
|---|---|
| **Nombre maximal d'agents** | Combien d'agents l'utilisateur peut avoir, clones d'un agent compris. Cinq lorsque le champ est vide. Zéro permet à l'utilisateur de conserver et d'exécuter ses agents, mais pas d'en créer ni d'en cloner un autre. Un agent supprimé libère sa place. |
| **Exécutions simultanées** | Combien d'exécutions de l'utilisateur peuvent travailler en même temps. Une lorsque le champ est vide. |
| **Requêtes simultanées au modèle (par exécution)** | Combien de requêtes au modèle une exécution peut avoir en vol en même temps, l'agent et ses [sous-agents](../agents.md#sub-agents) ensemble. Une lorsque le champ est vide : pendant qu'un sous-agent interroge le modèle, l'agent attend, et inversement. Une valeur plus élevée permet aux sous-agents de travailler en parallèle avec l'agent (le modèle de l'agent peut aussi avoir sa propre [limite de requêtes](../inference-providers.md#general-settings), qui s'applique également). |
| **Budget de jetons d'une exécution** | Le nombre maximal de jetons (envoyés et générés) qu'une exécution de l'utilisateur peut consommer. Vide ou zéro signifie aucune limite. |
| **Copies de travail** | Combien de copies de travail de livres les exécutions de l'utilisateur conservent en même temps. Une exécution non terminée conserve sa copie, de même une exécution suspendue, et la rend lorsqu'elle se termine, est arrêtée ou supprimée. Vide ou zéro signifie aucune limite, hormis le nombre d'agents. |
| **Fichier le plus grand (Ko)** | Le plus grand fichier qu'un agent de l'utilisateur peut écrire dans son espace de travail. 1024 (1 Mo) lorsque le champ est vide. |
| **Taille de l'espace de travail (Mo)** | Ce que tous les fichiers de l'espace de travail d'un agent de l'utilisateur peuvent occuper au total. 20 lorsque le champ est vide. |
| **Fichiers dans un espace de travail** | Combien de fichiers l'espace de travail d'un agent de l'utilisateur peut contenir, les dossiers ne comptent pas. 500 lorsque le champ est vide. |
| **Points de contrôle d'une exécution (Mo)** | Ce que tous les [points de contrôle](../agents.md#checkpoints) d'une exécution (points de restauration de la copie de travail et de son espace de travail) peuvent occuper au total ; une partie est réservée aux points automatiques. 100 lorsque le champ est vide. |

![Les paramètres de l'autorisation Agents](../../images/permissions-settings-agents.png)

Celui des notifications :

| Paramètre | |
|---|---|
| **Autoriser les adresses de cette machine et des réseaux privés** | Une sous-autorisation : la case ne peut être cochée que si **Notifications** est accordée (elle est grisée sinon, et retirer l'autorisation la décoche). Sans elle, seules les adresses sur Internet sont appelées : ni `localhost`, ni un réseau privé (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), ni une adresse link-local ou partagée (`169.254.x.x`, celle où les hébergeurs cloud placent leur service de métadonnées, `100.64-127.x.x`), ni les adresses locales uniques d'IPv6. Cochez-la pour un utilisateur dont ntfy ou Gotify tourne sur la même machine ou sur votre propre réseau. Désactivé par défaut. Un nom est vérifié à travers les adresses vers lesquelles il pointe : un nom qui renvoie vers une adresse privée est donc refusé lui aussi, de même qu'un nom introuvable. |

Les administrateurs n'ont besoin ni de l'autorisation ni de la sous-autorisation : ils reçoivent les messages et peuvent utiliser les adresses locales. Sur un serveur partagé avec des personnes en qui vous n'avez pas entièrement confiance, laissez la sous-autorisation désactivée : sinon le serveur enverrait des requêtes vers n'importe quelle adresse saisie par l'utilisateur, à l'intérieur de votre réseau.

<a id="when-permissions-were-introduced"></a>

## Quand les autorisations ont été introduites

Avant les autorisations, **Les utilisateurs peuvent créer leurs propres modèles** et l'interrupteur **Activé** de l'assistant d'aide (avec **Appliquer les limites** et **Autoriser la navigation dans l'interface**) étaient des paramètres de l'installation entière. Marginalia les a transformés en autorisations lors de sa mise à jour : chaque utilisateur existant a reçu **Créer ses propres modèles** si tout le monde pouvait créer des modèles (ce qui était le comportement par défaut), et **Assistant d'aide** avec les mêmes deux paramètres si l'assistant était activé. Rien ne change pour eux tant que vous ne le modifiez pas ; les utilisateurs que vous ajoutez à partir de maintenant démarrent sans autorisation.
