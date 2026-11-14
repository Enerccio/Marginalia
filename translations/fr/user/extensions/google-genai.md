---
label: Google Gemini
order: 14
---

# Google Gemini

Ajoute les modèles Gemini de Google comme [fournisseur d'inférence](../inference-providers.md), via l'API propre à Google et non via une API compatible OpenAI.

Google propose aussi un point d'accès compatible OpenAI, qui fonctionne avec le fournisseur ordinaire **Compatible OpenAI**. Cette extension s'adresse directement à l'API Gemini (le SDK Google Gen AI), ce qui apporte ce que le point d'accès compatible ne permet pas : les **résumés de la réflexion du modèle** affichés comme raisonnement, le niveau de réflexion de Gemini 3 et le budget de réflexion des modèles antérieurs, les filtres de sécurité, et les nombres exacts de jetons que Google indique.

!!!warning Votre texte de récit est envoyé à Google
Chaque requête d'une IA qui utilise ce fournisseur - le prompt avec le récit, les entrées du lorebook et les résumés, les réponses du modèle, les fichiers et images qu'un agent ou une commande envoie - est transmise aux serveurs de Google (`generativelanguage.googleapis.com`, ou le proxy que vous indiquez). Les conditions d'utilisation et la politique de confidentialité de l'API Gemini de Google déterminent ce qu'il en advient (conservation, utilisation pour améliorer les modèles ; cela dépend de la formule de votre clé d'API). Les autres fournisseurs envoient vers le serveur que vous avez choisi, qui peut être le vôtre. N'utilisez pas ce fournisseur pour un texte que vous ne pouvez pas envoyer à Google. Sur un serveur partagé avec d'autres utilisateurs, un administrateur qui partage une IA de ce fournisseur envoie à Google le texte de récit de tous ceux qui l'utilisent.
!!!

<a id="installing"></a>

## Installation

Chargez l'extension dans *Admin → Extensions*, comme décrit dans [Gérer les extensions](../administration/extensions.md).
Le JAR est volumineux (environ 14 Mo) : il contient le SDK de Google et les bibliothèques dont seul le SDK a besoin. Rien d'autre dans Marginalia ne les utilise.

Une fois l'extension chargée, la liste **Type** de la boîte de dialogue du fournisseur d'inférence propose une nouvelle entrée, **Extension**, et la liste **Fournisseur** en haut de l'onglet *Paramètres par type* propose **Google Gemini** (il est choisi automatiquement tant que c'est le seul). Sans extension chargée qui fournit des IA, **Extension** n'est pas proposé.

<a id="adding-a-gemini-provider"></a>

## Ajouter un fournisseur Gemini

1. Obtenez une clé d'API dans [Google AI Studio](https://aistudio.google.com/apikey).
2. Dans l'onglet **Fournisseurs d'inférence**, cliquez sur **Ajouter un fournisseur d'inférence**, saisissez un **Nom**, choisissez le type **Extension** et le fournisseur **Google Gemini**.
3. Renseignez les *Paramètres généraux* (taille du contexte, taille de la réponse, [raisonnement](#reasoning)) et les réglages du fournisseur dans l'onglet *Paramètres par type*, puis cliquez sur **Tester la connexion**.

![Les paramètres généraux d'un fournisseur de type Extension](../../images/plugin-googlegenai-general.png)

![Les paramètres par type du fournisseur Google Gemini](../../images/plugin-googlegenai.png)

Le fournisseur figure ensuite dans la liste comme les autres, avec le nom du fournisseur dans la colonne *Type* :

![La liste des fournisseurs d'inférence avec un fournisseur Google Gemini](../../images/plugin-googlegenai-list.png)

| Champ | |
|---|---|
| **Clé d'API** | La clé de Google AI Studio. Elle est stockée chiffrée et n'est plus affichée ensuite ; lors de la modification du fournisseur, cliquez sur **Réinitialisation** pour en saisir une autre. |
| **Modèle** | Le modèle à utiliser, par exemple `gemini-2.5-flash` ou `gemini-3-pro-preview`. **Actualiser** liste les modèles que votre clé peut utiliser pour écrire du texte ; vous pouvez saisir un nom absent de la liste. |
| **URL** | Facultatif. Vide utilise l'API de Google (`generativelanguage.googleapis.com`) ; renseignez-le pour faire passer les requêtes par un proxy. |
| **Délai d'attente (secondes)** | Le temps d'attente de la réponse de Google, puis pour la suite du texte une fois qu'il a commencé à écrire. La valeur par défaut est 300. |
| **Nouvelles tentatives** | Le nombre de fois qu'une requête est renouvelée lorsque Google répond par une limite de débit (429) ou une erreur serveur (5xx), ou lorsqu'il est injoignable. La valeur par défaut est 2 ; 0 désactive les nouvelles tentatives. |
| **Désactiver les filtres de sécurité** | Envoie `BLOCK_NONE` pour les catégories de sécurité ajustables (harcèlement, discours haineux, contenu sexuellement explicite, contenu dangereux). La fiction se heurte souvent aux filtres. Google bloque tout de même certains contenus, quoi qu'on envoie. |

**Tester la connexion** liste les modèles et demande au modèle choisi quelques jetons, avec les valeurs de la boîte de dialogue (avant de les enregistrer).

<a id="reasoning"></a>

## Raisonnement

Les réglages de raisonnement sont ceux des paramètres généraux du fournisseur, comme pour tout autre fournisseur :

- Lorsque **Raisonnement activé** est désactivé, le modèle réfléchit comme il le fait par défaut et sa réflexion n'est pas affichée.
- Lorsqu'il est activé, le modèle envoie les résumés de sa réflexion et ils sont affichés comme le raisonnement de la partie du récit. **Effort de raisonnement** règle la quantité de réflexion : pour les modèles Gemini 3, il s'agit du niveau de réflexion (faible, moyen, élevé) ; pour les modèles antérieurs, un budget de 1 024, 8 192 ou 24 576 jetons de réflexion. **Aucun** équivaut à désactivé.

Les jetons de réflexion comptent comme des jetons générés et sont prélevés sur la taille de réponse que vous avez réglée : si le modèle la consomme entièrement pour réfléchir, la partie du récit revient vide et l'erreur indique que la taille de la réponse a été épuisée. Augmentez **Nombre maximal de jetons de réponse** ou abaissez l'effort.

<a id="what-works"></a>

## Ce qui fonctionne

Tout ce dont un récit a besoin fonctionne comme avec un fournisseur compatible OpenAI : la diffusion en continu, l'arrêt d'une génération, les résumés, les [extensions qui appellent le modèle](index.md), les fonctions que le modèle peut appeler (le [générateur de noms](name-generator.md) en propose), ainsi que les images et fichiers joints à un message. Les jetons consommés par un utilisateur sont comptés dans les colonnes **Jetons envoyés** et **Jetons générés** à partir des nombres que Google indique, et les [limites](../inference-providers.md#limits-of-shared-providers) d'un fournisseur partagé s'appliquent.

Les nombres de jetons affichés dans le récit (la taille d'une partie, le prompt) sont calculés par Google (la requête `countTokens` du modèle), une fois pour chaque texte : les résultats sont mémorisés. Lorsque Google ne peut pas être interrogé (pas de clé, pas de réseau, erreur), Marginalia estime lui-même le nombre pendant une minute, puis réessaie auprès de Google ; un texte enregistré ou modifié pendant qu'un autre enregistrement est en cours est également estimé. L'estimation diffère légèrement de ce que Google facture.

<a id="when-the-extension-is-not-loaded"></a>

## Lorsque l'extension n'est pas chargée

Les fournisseurs que vous avez créés restent dans la liste lorsque l'extension est déchargée, avec le type *Extension : google-genai*, et refonctionnent lorsque l'extension est chargée. Tant qu'elle n'est pas chargée, un livre qui utilise un tel fournisseur ne peut pas générer de texte : l'erreur vous invite à charger l'extension ou à choisir un autre fournisseur. Le fournisseur peut toujours être ouvert et ses paramètres généraux modifiés ; les réglages Gemini ne sont affichés que lorsque l'extension est chargée, et ils ne sont pas modifiés.

<a id="not-supported"></a>

## Non pris en charge

- **Vertex AI** (Google Cloud avec un projet et des identifiants). Seules les clés d'API de l'API Gemini sont prises en charge.
- La génération d'images et les autres types de sortie de Gemini : seul le texte est produit.
