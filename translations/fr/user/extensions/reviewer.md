---
label: Relecteur
order: 2
---

# Relecteur

Permet au modèle de relire ce que vous avez écrit : réactions de lecteurs, critique d'éditeur, contrôle de cohérence -
tout ce pour quoi vous le sollicitez. Les relectures sont conservées avec la partie, à côté de l'histoire, et ne font
jamais partie du livre.

![Une relecture d'une partie d'histoire](../../images/plugin-reviewer.png)

<a id="reviewing-a-part"></a>

## Relire une partie

Le menu (☰) de chaque partie dans l'[éditeur d'histoire](../books/story-editor.md) comporte un sous-menu **Examiner** :

| Élément | |
|---|---|
| **View / Generate Review** | Ouvre la boîte de dialogue de relecture. Si la partie n'a pas encore de relecture, une relecture est générée immédiatement. |
| **Advanced Options** | Ouvre les [options avancées](#advanced-options), puis génère une relecture avec elles. |
| **Delete Review** | Supprime toutes les relectures de la partie. |

Dans la boîte de dialogue de relecture :

- **‹** / **›** parcourent les relectures de la partie ; **›** sur la dernière relecture en génère une nouvelle ;
- **Stop generating** interrompt la relecture en cours d'écriture ;
- cliquez sur le texte d'une relecture pour la modifier ; elle est enregistrée lorsque vous cliquez en dehors du champ ;
- *Thinking Process* affiche le raisonnement des [modèles de raisonnement](../inference-providers.md#reasoning-models) ;
- **Fermer** ferme la boîte de dialogue.

<a id="what-the-model-gets"></a>

## Ce que reçoit le modèle

Par défaut, la relecture utilise le **même prompt que celui à partir duquel la partie a été écrite** (*Use standard prompt info*) : le prompt système avec le lore et les résumés, les parties précédentes, puis la partie elle-même, et enfin le prompt de relecture. Le modèle relit la partie avec le contexte complet de l'histoire.

Le **pré-prompt** du profil, s'il est défini, arrive en premier sous forme de message système.

<a id="advanced-options"></a>

## Options avancées

| Option | |
|---|---|
| **Prompt Override** | Un autre prompt de relecture pour cette relecture. Démarre avec le prompt du profil. |
| **Use standard prompt info** | Activée : le prompt à partir duquel la partie a été écrite, comme ci-dessus. Désactivée : uniquement les parties de l'histoire, avec les options ci-dessous. |
| **Include Lorebook** | (désactivée uniquement) Ajoute le lore qui était actif au moment où la partie a été écrite. |
| **Avec les résumés** | (désactivée uniquement) Envoie d'abord les [résumés](../books/summaries.md) de l'histoire, puis les parties qui suivent le résumé le plus récent, comme lors d'une génération : la partie associée au résumé le plus récent et tout ce qui la précède sont représentés par les résumés. Les résumés prennent d'abord leur place dans la limite de tokens, les parties reçoivent le reste, de la plus récente à la plus ancienne. Un résumé dont l'histoire a été modifiée après sa création n'est pas envoyé (la relecture ne le supprime pas). Désactivée : pas de résumés, autant des parties les plus récentes qui tiennent. |
| **Send original text** | Affiché uniquement lorsqu'un traducteur ([Real Language Translator](real-language-translator.md), [Conlang Translator](conlang-translator.md)) est chargé. Désactivée : la relecture lit l'anglais derrière les répliques traduites, comme le fait tout modèle. Activée : la partie relue (et, avec *Use standard prompt info* désactivée, les parties précédentes) est envoyée telle qu'elle est stockée, avec les répliques dans la traduction. Le prompt à partir duquel la partie a été écrite (*Use standard prompt info* activée) a été construit à partir de l'anglais et le reste. |
| **Token Limit** | (désactivée uniquement) Nombre de tokens d'histoire à envoyer : les parties jusqu'à la partie relue incluse, de la plus récente à la plus ancienne, autant qu'il en tient. Commence par la place réservée au prompt du modèle. |

Avec *Use standard prompt info* désactivée, la boîte de dialogue affiche la taille de la requête (*Total tokens*). **Générer** ouvre la boîte de dialogue de relecture avec ces options ; les relectures suivantes générées dans cette boîte de dialogue les utilisent également.

![Options avancées de relecture](../../images/plugin-reviewer-advanced.png)

<a id="profiles"></a>

## Profils

Les paramètres du relecteur se trouvent dans **Paramètres → Extension Settings → Reviewer Settings**. Ils sont organisés en **profils** :

![Les paramètres du relecteur](../../images/plugin-reviewer-settings.png)

| Champ | |
|---|---|
| **Profile** | Le profil à modifier - et celui utilisé pour toutes les relectures. |
| **New**, **Renommer**, **Supprimer** | Gèrent les profils. Le profil *_Default* ne peut être ni renommé ni supprimé. |
| **Reviewer Model** | Le [fournisseur d'inférence](../inference-providers.md) qui rédige les relectures. Vide : le modèle du livre. |
| **Reviewer Protocol** | Le [protocole](../protocols.md). Vide : le protocole du livre. |
| **Review Pre-Prompt (System)** | Un message système envoyé en premier, par exemple *You are a demanding fiction editor.* |
| **Review Post-Prompt (User)** | La demande de relecture, envoyée en dernier. |

Cliquez sur **Enregistrer** en haut de la page *Paramètres* pour conserver les modifications.

Le post-prompt par défaut simule une discussion sur un forum d'histoires : un fil fictif avec trente réponses de lecteurs différents, positives et négatives. Autres idées :

- *List continuity errors in the last part compared to the earlier story. Quote the passages.*
- *As a line editor, point out clichés, repetitions and weak verbs in the last part, with suggestions.*
- *Describe how a first-time reader would feel at the end of this part, and what they expect next.*

Créez un profil pour chaque type de relecture et sélectionnez celui que vous voulez avant de relire. Un modèle moins coûteux pour les relectures permet de réduire les frais : définissez-le comme *Reviewer Model* du profil.

<a id="storage"></a>

## Stockage

Les relectures sont enregistrées avec la partie d'histoire ; elles font donc partie des [sauvegardes de livres](../books/backups.md) et des copies. [Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part) ne les copie pas. Les profils sont enregistrés dans vos paramètres utilisateur.
