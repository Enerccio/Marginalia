---
label: Protocoles
order: 970
icon: sliders
---

# Protocoles

Un protocole est un ensemble de paramètres de génération : les paramètres d'échantillonnage (température, top P et pénalités) et, facultativement, ses propres limites de contexte et de réponse. Le [fournisseur d'inférence](inference-providers.md) indique *quel* modèle écrit ; le protocole indique *comment*.

Les protocoles sont distincts des fournisseurs : vous pouvez en garder plusieurs, par exemple un protocole prudent et un protocole créatif, et passer d'un livre à l'autre sans toucher à la connexion. Comme les fournisseurs, les protocoles appartiennent à votre compte ; vous pouvez [partager](#sharing-a-protocol) l'un d'eux afin que d'autres utilisateurs puissent en importer une copie.

![L'onglet Protocoles](../images/protocols-list.png)

<a id="creating-a-protocol"></a>

## Créer un protocole

Ouvrez l'onglet **Protocoles** et cliquez sur **Ajouter un protocole**. Les protocoles existants se modifient avec le bouton crayon et se suppriment avec le bouton corbeille. Le bouton de clonage (deux feuilles) crée une copie d'un protocole, avec tous ses paramètres, sous un nom que vous confirmez (celui de l'original suivi de *(Copy)*). C'est pratique pour essayer d'autres réglages d'échantillonnage sans perdre ceux qui fonctionnent.

**Importer d'un autre utilisateur** (au-dessus de la liste) affiche les protocoles que d'autres utilisateurs [partagent](#sharing-a-protocol) et importe une copie de l'un d'eux.

![La boîte de dialogue du protocole](../images/protocol-dialog.png)

| Champ | |
|---|---|
| **Nom** | Le nom que vous donnez au protocole, par exemple `Creative` ou `Long context`. |
| **Type** | Le format des requêtes. Pour l'instant, seul **Chat Completion** est disponible. |
| **Nombre maximal de jetons de contexte** | Facultatif. Nombre total de jetons pour l'invite et la réponse des livres utilisant ce protocole. Vide : la valeur **Taille maximale du contexte** du fournisseur est utilisée. |
| **Nombre maximal de jetons de réponse** | Facultatif. La réponse la plus longue. Vide : la valeur **Nombre maximal de jetons de réponse** du fournisseur est utilisée. |
| **Température** | Le caractère aléatoire du texte. Une valeur basse (autour de `0.7`) est plus ciblée et prévisible, une valeur haute (`1.0` et plus) est plus variée et surprenante. |
| **Top P** | Échantillonnage par noyau : le modèle ne choisit que parmi les mots les plus probables dont les probabilités s'additionnent jusqu'à cette valeur (`0`-`1`). `0.9`-`0.95` est un choix courant. |
| **Pénalité de fréquence** | Pénalise les mots selon le nombre de fois où ils sont déjà apparus, ce qui réduit les répétitions. En général `0`-`1`. |
| **Pénalité de présence** | Pénalise les mots qui sont déjà apparus, quelle que soit leur fréquence, ce qui pousse le modèle vers de nouveaux sujets. En général `0`-`1`. |
| **Partager avec d'autres utilisateurs** | Lorsque la case est cochée, d'autres utilisateurs peuvent consulter le protocole et en importer une copie, voir [ci-dessous](#sharing-a-protocol). Désactivé pour un nouveau protocole. |

Chaque paramètre d'échantillonnage possède une case **Activer**. Un paramètre qui n'est pas activé n'est pas défini par Marginalia, et le modèle utilise sa propre valeur par défaut. N'activez que ce que vous voulez modifier.

<a id="sharing-a-protocol"></a>

## Partager un protocole

Cochez **Partager avec d'autres utilisateurs** dans la boîte de dialogue du protocole : tous les autres utilisateurs de cette installation voient le protocole dans **Importer d'un autre utilisateur** (le bouton à côté de **Ajouter un protocole**), avec son nom et son auteur (votre nom complet, ou votre identifiant de connexion si vous n'en avez pas). Ils peuvent ouvrir une ligne pour voir ses paramètres et cliquer sur **Importer**, ce qui crée un protocole à leur nom, avec les mêmes paramètres et le même nom. Le partage est en lecture seule : personne d'autre ne peut modifier, supprimer ou utiliser votre protocole, et la copie est privée et n'est pas liée à la vôtre : ce que l'un ou l'autre modifie ensuite n'atteint pas l'autre. Décochez la case et le protocole disparaît de leur liste ; les copies qu'ils ont déjà faites leur appartiennent toujours.

<a id="limits"></a>

## Limites

Les limites du [fournisseur d'inférence](inference-providers.md#general-settings) s'appliquent toujours. Un protocole peut les abaisser, chacune séparément, mais jamais les dépasser :

| | Valeur du protocole renseignée | Valeur du protocole vide |
|---|---|---|
| Contexte | **Nombre maximal de jetons de contexte**, ou **Taille maximale du contexte** du fournisseur si celle-ci est plus petite | **Taille maximale du contexte** du fournisseur |
| Réponse | **Nombre maximal de jetons de réponse**, ou **Nombre maximal de jetons de réponse** du fournisseur si celle-ci est plus petite | **Nombre maximal de jetons de réponse** du fournisseur |

Un protocole avec un **Nombre maximal de jetons de contexte** de 128 000 utilisé avec un fournisseur de 32 000 obtient 32 000 : le fournisseur représente ce que le service (ou l'administrateur qui le partage) autorise.

Marginalia réserve de la place pour la réponse et remplit le reste du contexte avec l'invite : modèles, entrées du lorebook, résumés et autant de l'histoire que possible. Avec un contexte de 32 000 jetons et des réponses allant jusqu'à 2 000 jetons, l'invite reçoit 30 000 jetons. La limite de réponse est également transmise à l'API (`max_completion_tokens`).

Utilisez les limites du protocole pour garder les invites plus courtes que ce que le modèle permet, par exemple pour rendre les longs livres moins coûteux et plus rapides avec un modèle cloud à grand contexte, ou pour raccourcir les réponses de certains livres. Les limites du protocole servent aussi aux [résumés](books/summaries.md) des livres utilisant ce protocole.

Laissez les deux champs vides, sauf si vous en avez besoin : ce sont alors les valeurs du fournisseur qui s'appliquent, et lorsque vous changez le fournisseur d'un livre, ses limites le suivent.

<a id="using-a-protocol-in-a-book"></a>

## Utiliser un protocole dans un livre

Chaque livre a son protocole dans le champ **Protocoles** de son onglet **À propos**. Pour utiliser un protocole pour tous les nouveaux livres, sélectionnez-le comme **Protocole par défaut** dans l'onglet **Paramètres**. Un livre sans protocole ne peut pas générer de texte et affiche *Le livre n'a pas de protocole.*

<a id="suggested-settings"></a>

## Réglages suggérés

Points de départ, à ajuster selon votre modèle :

| Usage | Température | Top P | Pénalité de fréquence | Pénalité de présence |
|---|---|---|---|---|
| Prose équilibrée | `0.8` | `0.95` | désactivée | désactivée |
| Créatif, varié | `1.0` | `0.95` | `0.3` | `0.3` |
| Cohérent, proche des instructions | `0.6` | `0.9` | désactivée | désactivée |

Les modèles de raisonnement ignorent souvent ou rejettent les paramètres d'échantillonnage ; laissez-les désactivés pour ceux-ci.
