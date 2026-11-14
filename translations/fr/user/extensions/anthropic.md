---
label: Anthropic Claude
order: 25
---

# Anthropic Claude

Ajoute les modèles Claude d'Anthropic comme [fournisseur d'inférence](../inference-providers.md), via l'API propre d'Anthropic plutôt qu'une API compatible OpenAI.

Claude est aussi accessible via des agrégateurs comme OpenRouter avec le fournisseur habituel **Compatible OpenAI**. Cette extension
communique directement avec l'API Claude (le SDK Anthropic), ce qui permet ce qu'un point de terminaison compatible ne permet pas : les **résumés
de la réflexion du modèle** affichés comme raisonnement, l'effort des modèles Claude 4.6 et plus récents, et les décomptes exacts de jetons
que fournit et calcule Anthropic.

!!!warning Le texte de votre histoire est envoyé à Anthropic
Chaque requête d'une IA qui utilise ce fournisseur - le prompt avec l'histoire, les entrées du livre de connaissances et les résumés,
les réponses du modèle, les fichiers et images qu'un agent ou une commande envoie - est transmise aux serveurs d'Anthropic
(`api.anthropic.com`, ou le proxy que vous avez défini). Les conditions d'utilisation et la politique de confidentialité d'Anthropic
pour l'API déterminent ce qu'il advient de ces données là-bas (conservation, garde-fous pouvant refuser une requête). Les autres
fournisseurs envoient les données au serveur que vous avez choisi, qui peut être le vôtre.
N'utilisez pas ce fournisseur pour du texte que vous n'avez pas le droit d'envoyer à Anthropic. Sur un serveur partagé avec d'autres
utilisateurs, un administrateur qui partage une IA de ce fournisseur envoie à Anthropic le texte de l'histoire de tous ceux qui l'utilisent.
!!!

<a id="installing"></a>

## Installation

Chargez l'extension dans *Admin → Extensions*, comme décrit dans [Gérer les extensions](../administration/extensions.md).
Le JAR est volumineux (environ 36 Mo) : il contient le SDK d'Anthropic et les bibliothèques dont seul le SDK a besoin. Rien d'autre
dans Marginalia ne les utilise.

Une fois l'extension chargée, la liste **Type** de la boîte de dialogue du fournisseur d'inférence propose une nouvelle entrée, **Extension**,
et la liste **Fournisseur** en haut de l'onglet *Paramètres par type* propose **Anthropic Claude** (il est choisi automatiquement tant qu'il
est le seul). Sans extension chargée qui fournit des IA, **Extension** n'est pas proposée.

<a id="adding-a-claude-provider"></a>

## Ajouter un fournisseur Claude

1. Obtenez une clé d'API dans la [Claude Console](https://platform.claude.com).
2. Dans l'onglet **Fournisseurs d'inférence**, cliquez sur **Ajouter un fournisseur d'inférence**, saisissez un **Nom**, choisissez le type **Extension**
   et le fournisseur **Anthropic Claude**.
3. Renseignez les *Paramètres généraux* (taille du contexte, taille de la réponse, [raisonnement](#reasoning)) et les réglages du
   fournisseur dans l'onglet *Paramètres par type*, puis cliquez sur **Tester la connexion**.

| Champ | |
|---|---|
| **Clé d'API** | La clé de la Claude Console. Elle est stockée chiffrée et n'est plus affichée ensuite ; lorsque vous modifiez le fournisseur, cliquez sur **Réinitialisation** pour saisir une autre clé. |
| **Modèle** | Le modèle à utiliser, par exemple `claude-opus-5-5` ou `claude-sonnet-5-5`. **Actualiser** liste les modèles que votre clé peut utiliser ; vous pouvez saisir un nom absent de la liste. |
| **URL** | Facultatif. Vide utilise l'API d'Anthropic (`api.anthropic.com`) ; renseignez-la pour envoyer les requêtes via un proxy. |
| **Délai d'attente (secondes)** | Le temps d'attente de la réponse d'Anthropic, puis de la suite du texte une fois qu'elle a commencé à écrire. La valeur par défaut est 300. |
| **Nouvelles tentatives** | Le nombre de fois qu'une requête est répétée lorsque Anthropic répond par une limitation de débit (429) ou une erreur serveur (5xx, dont 529 *overloaded*), ou lorsqu'il est injoignable. La valeur par défaut est 2 ; 0 désactive les nouvelles tentatives. |

**Tester la connexion** liste les modèles et demande au modèle choisi quelques jetons, avec les valeurs de la boîte de dialogue (avant
de les enregistrer).

![Les Paramètres par type du fournisseur Anthropic Claude](../../images/plugin-anthropic.png)

![La liste des fournisseurs d'inférence avec un fournisseur Claude](../../images/plugin-anthropic-list.png)

<a id="reasoning"></a>

## Raisonnement

Les réglages du raisonnement sont ceux des paramètres généraux du fournisseur, comme pour tout fournisseur :

- Lorsque **Raisonnement activé** est désactivé, rien concernant la réflexion n'est envoyé. Les modèles qui réfléchissent par défaut (Opus 5.5 ne peut pas être configuré pour s'en abstenir) le font, et leur réflexion n'est pas affichée ; les autres ne réfléchissent pas.
- Lorsqu'il est activé, le modèle envoie des résumés de sa réflexion, affichés comme le raisonnement de la partie de l'histoire. Pour Claude 4.6 et plus récents, **Effort de raisonnement** correspond à l'effort du modèle (faible, moyen, élevé) avec une réflexion adaptative : le modèle décide de la quantité à réfléchir. Pour les modèles antérieurs (Claude 4.5 et avant, dont Haiku 4.5), c'est un budget de 1 024, 8 192 ou 24 576 jetons de réflexion. **Aucun** équivaut à désactivé.

Les jetons de réflexion comptent comme jetons générés et sont prélevés sur la taille de réponse que vous avez définie : si le modèle l'utilise entièrement pour réfléchir, la partie de l'histoire revient vide et l'erreur indique que la taille de la réponse a été épuisée. Augmentez **Nombre maximal de jetons de réponse** ou baissez l'effort. Claude exige une taille de réponse à chaque requête : lorsque ni le fournisseur ni le protocole n'en fixe une, 16 000 est utilisé.

<a id="what-works"></a>

## Ce qui fonctionne

Tout ce dont une histoire a besoin fonctionne comme avec un fournisseur compatible OpenAI : le streaming, l'arrêt d'une génération, les résumés,
les [extensions qui appellent le modèle](index.md), les fonctions que le modèle peut appeler (le [Générateur de noms](name-generator.md) en propose
quelques-unes), ainsi que les images (JPEG, PNG, GIF, WebP), les fichiers PDF et texte joints à un message. Les jetons qu'un utilisateur consomme
sont comptés dans les colonnes **Jetons envoyés** et **Jetons générés** à partir des nombres que fournit Anthropic (les jetons lus depuis le cache
et ceux qui y sont écrits comptent comme envoyés), et les [limites](../inference-providers.md#limits-of-shared-providers) d'un fournisseur partagé s'appliquent.

Les décomptes de jetons affichés dans l'histoire (la taille d'une partie, le prompt) sont calculés par Anthropic (la requête `count_tokens`
du modèle), une fois pour chaque texte : les réponses sont mémorisées. Lorsque Anthropic ne peut pas être interrogé (pas de clé, pas de réseau,
une erreur), Marginalia estime lui-même le décompte pendant une minute, puis réessaie ; un texte enregistré ou modifié pendant qu'un autre
enregistrement est en cours est aussi estimé.

Deux points diffèrent des autres fournisseurs :

- **La température, le top p et les pénalités du protocole ne sont pas envoyés.** L'API Claude actuelle les refuse ; le modèle échantillonne avec ses propres réglages. La taille de réponse du protocole est utilisée.
- Une conversation doit commencer et se terminer par un message de l'utilisateur, et Claude ne poursuit pas une réponse. Le prompt d'un livre le fait toujours ; une requête d'une extension qui ne le fait pas est refusée avec une erreur « bad request ».

<a id="when-the-extension-is-not-loaded"></a>

## Lorsque l'extension n'est pas chargée

Les fournisseurs que vous avez créés restent dans la liste lorsque l'extension est déchargée, avec le type *Extension: anthropic*, et
fonctionnent à nouveau une fois l'extension chargée. Tant qu'elle n'est pas chargée, un livre qui utilise un tel fournisseur ne peut pas générer
de texte : l'erreur vous invite à charger l'extension ou à choisir un autre fournisseur. Le fournisseur peut toujours être ouvert et ses
paramètres généraux modifiés ; les réglages Claude ne sont affichés que lorsque l'extension est chargée, et ils ne sont pas modifiés.

<a id="not-supported"></a>

## Non pris en charge

- **Amazon Bedrock, Google Vertex AI et Microsoft Foundry.** Seules les clés d'API de l'API Claude sont prises en charge.
- La mise en cache des prompts et les outils qui s'exécutent sur les serveurs d'Anthropic (recherche web, exécution de code).
