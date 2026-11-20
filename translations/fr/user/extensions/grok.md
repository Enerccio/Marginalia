---
label: xAI Grok
order: 26
---

# xAI Grok

Ajoute les modèles Grok de xAI comme [fournisseur d'inférence](../inference-providers.md), via l'API propre à xAI.

Grok est aussi accessible avec le fournisseur habituel *OpenAI Compatible* et l'URL `https://api.x.ai/v1`. Cette extension communique
directement avec l'API de xAI, ce qui permet d'afficher le raisonnement comme tel, les nombres exacts de jetons que xAI
indique (raisonnement inclus) et de les compter, ainsi qu'une liste de modèles et un test de connexion adaptés au service.

!!!warning Votre texte d'histoire est envoyé à xAI
Chaque requête d'une IA qui utilise ce fournisseur - le prompt avec l'histoire, les entrées du lorebook et les résumés, les
réponses du modèle, les images qu'un agent ou une commande envoie - part vers les serveurs de xAI (`api.x.ai`, ou le proxy que vous
avez défini). Les conditions d'utilisation et la politique de confidentialité de l'API de xAI déterminent ce qu'il en advient
ensuite. Les autres fournisseurs envoient vers le serveur que vous avez choisi, qui peut être le vôtre. N'utilisez pas ce
fournisseur pour du texte que vous n'avez pas le droit d'envoyer à xAI. Sur un serveur partagé avec d'autres utilisateurs, un
administrateur qui partage une IA de ce fournisseur envoie à xAI le texte des histoires de tous ceux qui l'utilisent.
!!!

<a id="installing"></a>

## Installation

Chargez l'extension dans *Admin → Extensions*, comme décrit dans [Gestion des extensions](../administration/extensions.md). Une fois chargée, la liste
**Type** de la boîte de dialogue du fournisseur d'inférence propose l'entrée **Extension**, et la liste **Provider** en haut de
l'onglet *Per Type Settings* propose **xAI Grok**.

<a id="adding-a-grok-provider"></a>

## Ajouter un fournisseur Grok

1. Obtenez une clé API dans la [console xAI](https://console.x.ai).
2. Dans l'onglet **Inference Providers**, cliquez sur **Add Inference Provider**, saisissez un **Name**, choisissez le type
   **Extension** et le fournisseur **xAI Grok**.
3. Remplissez les *General Settings* (taille du contexte, taille des réponses, [raisonnement](#reasoning)) et les paramètres du
   fournisseur dans l'onglet *Per Type Settings*, puis cliquez sur **Test Connection**.

| Champ | |
|---|---|
| **Api Key** | La clé de la console xAI. Elle est stockée chiffrée et n'est plus jamais affichée ; lorsque vous modifiez le fournisseur, cliquez sur **Reset** pour en saisir une autre. |
| **Model** | Le modèle à utiliser, par exemple `grok-4` ou `grok-3-mini`. **Refresh** affiche les modèles que votre clé peut utiliser ; vous pouvez saisir un nom absent de la liste. |
| **URL** | Facultatif. Vide utilise l'API de xAI (`https://api.x.ai/v1`) ; renseignez-le pour envoyer les requêtes par un proxy. |
| **Timeout (seconds)** | Le temps d'attente de la réponse de xAI, et de la suite du texte une fois qu'il a commencé à écrire. La valeur par défaut est 300. |
| **Retries** | Le nombre de fois qu'une requête est renvoyée lorsque xAI répond par une limite de débit (429) ou une erreur serveur (5xx), ou lorsqu'il est injoignable. La valeur par défaut est 2 ; 0 désactive les nouvelles tentatives. |
| **Send the reasoning effort** | *Automatic* n'envoie l'effort qu'à `grok-3-mini`, *Always* l'envoie à tous les modèles, *Never* à aucun. |

**Test Connection** affiche les modèles et demande au modèle choisi quelques jetons, avec les valeurs de la boîte de dialogue
(avant que vous les enregistriez).

<a id="reasoning"></a>

## Raisonnement

Les réglages du raisonnement sont ceux des paramètres généraux du fournisseur, comme pour tout autre fournisseur. Lorsque le
raisonnement est activé, le raisonnement qu'un modèle affiche arrive dans la partie narrative. L'effort n'est envoyé qu'aux
modèles qui l'acceptent (`grok-3-mini` : *low*, ou *high* pour medium et high) ; les autres modèles Grok raisonnent par défaut et
refusent ce paramètre.

<a id="what-works"></a>

## Ce qui fonctionne

Le streaming, l'arrêt de la génération, les schémas JSON, les fonctions que le modèle peut appeler, les images (JPEG, PNG) et
les fichiers texte en pièces jointes, les limites des IA partagées et le comptage de l'usage fonctionnent comme avec le
fournisseur compatible OpenAI. L'usage est celui que xAI indique ; les jetons de raisonnement comptent comme des jetons générés.
La température, top p et les pénalités du [protocole](../protocols.md) sont envoyées lorsqu'elles sont activées ; certains modèles de
raisonnement refusent les pénalités. Non pris en charge : les outils côté serveur de xAI (recherche en direct, exécution de
code) ainsi que la génération d'images ou de vidéos.
