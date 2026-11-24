---
label: xAI Grok
order: 26
---

# xAI Grok

Ajoute les modèles Grok de xAI comme [fournisseur d'inférence](../inference-providers.md), via l'API propre à xAI.

Grok est aussi accessible avec le fournisseur habituel *OpenAI Compatible* et l'URL `https://api.x.ai/v1`. Cette extension communique
directement avec l'API de xAI, ce qui permet d'afficher le raisonnement comme tel, les nombres exacts de jetons que xAI
indique (jetons de raisonnement inclus) et les décomptes, ainsi qu'une liste de modèles et un test de connexion adaptés à ce service.

!!!warning Le texte de votre histoire est envoyé à xAI
Chaque requête d'une IA qui utilise ce fournisseur (le prompt avec l'histoire, les entrées du lorebook et les résumés, les
réponses du modèle, les images qu'un agent ou une commande envoie) est transmise aux serveurs de xAI (`api.x.ai`, ou le proxy que vous avez défini).
Les conditions d'utilisation et la politique de confidentialité de xAI pour l'API déterminent ce qui advient de ces données.
Les autres fournisseurs envoient vers le serveur que vous avez choisi, qui peut être le vôtre. N'utilisez pas ce fournisseur
pour un texte que vous ne pouvez pas envoyer à xAI. Sur un serveur partagé avec d'autres utilisateurs, un
administrateur qui partage une IA de ce fournisseur envoie à xAI le texte de l'histoire de tous ceux qui l'utilisent.
!!!

<a id="installing"></a>

## Installation

Chargez l'extension dans *Admin → Extensions*, comme décrit dans [Gestion des extensions](../administration/extensions.md). Une fois l'extension chargée, la liste **Type** de la boîte de dialogue du fournisseur d'inférence contient l'entrée **Extension**, et la liste **Provider** en haut de l'onglet *Per Type Settings* propose **xAI Grok**.

<a id="adding-a-grok-provider"></a>

## Ajouter un fournisseur Grok

1. Obtenez une clé API dans la [xAI Console](https://console.x.ai).
2. Dans l'onglet **Inference Providers**, cliquez sur **Add Inference Provider**, saisissez un **Name**, choisissez le type **Extension** et le
   fournisseur **xAI Grok**.
3. Renseignez les *General Settings* (taille du contexte, taille de la réponse, [raisonnement](#reasoning)) et les réglages du fournisseur dans l'onglet *Per Type Settings*, puis cliquez sur **Test Connection**.

![Un fournisseur Grok](../../images/plugin-grok.png)

![Le fournisseur Grok dans la liste](../../images/plugin-grok-list.png)

| Champ | |
|---|---|
| **Api Key** | La clé de la xAI Console. Elle est stockée chiffrée et n'est plus affichée ensuite ; lorsque vous modifiez le fournisseur, cliquez sur **Reset** pour saisir une autre clé. |
| **Model** | Le modèle à utiliser, par exemple `grok-4` ou `grok-3-mini`. **Refresh** liste les modèles que votre clé peut utiliser ; vous pouvez saisir un nom absent de la liste. |
| **URL** | Facultatif. Vide utilise l'API de xAI (`https://api.x.ai/v1`) ; renseignez-le pour envoyer les requêtes par un proxy. |
| **Timeout (seconds)** | Le temps d'attente de la réponse de xAI, et de la suite du texte une fois la génération commencée. La valeur par défaut est 300. |
| **Retries** | Le nombre de répétitions d'une requête lorsque xAI répond par une limite de débit (429) ou une erreur de serveur (5xx), ou lorsqu'il est injoignable. La valeur par défaut est 2 ; 0 désactive les nouvelles tentatives. |
| **Send the reasoning effort** | *Automatic* envoie l'effort de raisonnement à `grok-3-mini` uniquement, *Always* à tous les modèles, *Never* à aucun. |

**Test Connection** liste les modèles et demande au modèle choisi quelques jetons, avec les valeurs de la boîte de dialogue (avant que vous les enregistriez).

<a id="reasoning"></a>

## Raisonnement

Les réglages du raisonnement sont ceux des réglages généraux du fournisseur, comme pour tout autre fournisseur. Lorsque le raisonnement est activé, le raisonnement qu'un modèle affiche apparaît comme raisonnement de la partie du récit. L'effort n'est envoyé qu'aux modèles qui l'acceptent (`grok-3-mini` : *low*, ou *high* pour medium et high) ; les autres modèles Grok raisonnent par défaut et refusent ce paramètre.

<a id="what-works"></a>

## Ce qui fonctionne

Le streaming, l'arrêt de la génération, les schémas JSON, les fonctions que le modèle peut appeler, les images (JPEG, PNG) et les fichiers texte en pièces jointes, les limites des IA partagées et le décompte d'utilisation fonctionnent comme avec le fournisseur compatible OpenAI. L'utilisation est celle que xAI indique ; les jetons de raisonnement sont comptés comme des jetons générés. La température, le top p et les pénalités du [protocole](../protocols.md) sont envoyés lorsqu'ils sont activés ; certains modèles de raisonnement refusent les pénalités. Non pris en charge : les outils côté serveur de xAI (recherche en direct, exécution de code) et la génération d'images ou de vidéos.
