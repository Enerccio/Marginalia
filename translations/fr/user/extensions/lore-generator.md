---
label: Générateur de lore
order: 10
---

# Générateur de lore

Permet au modèle de rédiger à votre place le contenu d'une [entrée de lorebook](../lorebooks/entries.md). Il lit le lore
que vous avez déjà, afin que la nouvelle entrée s'intègre au monde, puis compacte le résultat dans la forme courte et dense
qui convient le mieux aux entrées de lore.

!!!
Le Générateur de lore fonctionne dans la [disposition téléphone](../phone.md#extensions) : le bouton baguette se trouve dans la ligne d'une entrée, et la fenêtre affiche l'invite au-dessus du résultat.
!!!

<a id="opening-the-generator"></a>

## Ouvrir le générateur

Une fois l'extension installée, chaque ligne de l'éditeur de lorebook reçoit un bouton baguette (**Generate lore entry**). Il ouvre
le générateur pour cette entrée. Si l'entrée contient déjà du contenu, la boîte de dialogue l'indique en haut : copier le résultat
dans l'entrée remplace le contenu.

L'[éditeur plein écran](../lorebooks/lorebooks.md#editing-in-full-screen) possède lui aussi la baguette : dans chaque ligne de sa liste
d'entrées, et sous forme de **Generate lore entry** dans la ligne du nom d'un onglet d'entrée. Ce que vous copiez dans l'entrée apparaît
immédiatement dans son onglet. Ouvert depuis un livre (son onglet *Lorebook*, ou l'onglet lorebook de l'histoire), l'éditeur connaît le livre,
et le livre paie le modèle comme sur la page du lorebook.

Le générateur démarre avec votre **Modèle par défaut** et votre **Protocole par défaut** issus de *Paramètres*, voir
[Compte et paramètres](../account.md). Les listes **Modèle** et **Protocoles** sous l'en-tête de la boîte de dialogue permettent d'utiliser
un autre modèle ou protocole pour cette génération (le choix n'est pas enregistré) ; le protocole est facultatif. Sans modèle, le
générateur affiche seulement un avertissement.

![Le générateur de lore](../../images/plugin-loregenerator.png)

<a id="what-the-model-sees"></a>

## Ce que voit le modèle

Les deux choix en haut de la boîte de dialogue déterminent le lore qui entre dans la requête :

| Choix | |
|---|---|
| **Current Lorebook** (par défaut) / **All Lore** | Seules les entrées du lorebook dans lequel se trouve l'entrée, en ignorant ses [sous-lorebooks](../lorebooks/lorebooks.md) ; ou les entrées de ce lorebook ainsi que celles de tous ses sous-lorebooks. |
| **Only up to position (n)** (par défaut) / **All entries** | *n* est l'ordre de l'entrée. Seules les entrées qui la précèdent sont affichées (un ordre inférieur, ou le même ordre et créées plus tôt), afin que le modèle s'appuie sur ce qui existe déjà et ne reçoive pas de lore écrit après l'entrée. *All entries* affiche tout. |

L'entrée que vous générez n'est jamais incluse, avec ou sans contenu. Que d'autres entrées ou lorebooks soient activés ou non, leurs étiquettes,
leurs filtres et leur mode d'insertion sont ignorés : ce que vous choisissez ici est ce qui est envoyé, dans l'ordre où les entrées
sont insérées dans une invite. Les modèles des entrées sont traités comme d'habitude.

<a id="prompts"></a>

## Invites

L'invite de génération est ce que vous demandez au modèle d'écrire. Les invites sont listées dans la zone à côté des choix, sur la même
ligne supérieure ; le texte de celle qui est sélectionnée se trouve dans la zone de texte à gauche, et vous pouvez le modifier pour une seule
génération sans l'enregistrer. Les deux moitiés de la boîte de dialogue sont séparées par un séparateur que vous pouvez faire glisser.

| Bouton | |
|---|---|
| **Enregistrer** | Enregistre le texte sous le nom sélectionné. |
| **Save as** | Demande un nom et enregistre le texte comme nouvelle invite (ou remplace une invite de ce nom, après confirmation). |
| **Supprimer** | Supprime l'invite sélectionnée. Quand la dernière est supprimée, l'invite intégrée `DEFAULT` revient. |

L'invite intégrée comporte des emplacements entre crochets (le sujet, les détails à inclure, la longueur, la structure) :
remplissez-les avant de la lancer. Les invites appartiennent à votre compte, et celle que vous avez utilisée en dernier est
de nouveau sélectionnée à la prochaine ouverture de la boîte de dialogue.

<a id="generation-and-compaction"></a>

## Génération et compactage

Le côté droit comporte deux onglets.

**Generation** - **Run Generation** envoie le lore suivi de l'invite de génération. La réponse apparaît au fur et à mesure qu'elle
est écrite, avec le raisonnement du modèle (quand il en fournit un) dans une section *Thinking Process* au-dessus. **Arrêter**
(à la place de *Run Generation*) l'interrompt ; ce qui a été écrit jusque-là est conservé.

**Compaction** - disponible une fois qu'une génération existe. **Run Compaction** envoie le même lore suivi de l'invite de compactage,
avec le texte généré à l'intérieur, et le modèle le condense en une entrée compacte. L'invite de compactage se règle dans
*Paramètres → Extension Settings → Lore Generator* ; lorsqu'elle est vide, l'invite intégrée est utilisée (elle retire la prose et
garde les faits). Relancer la génération supprime le compactage.

![Le compactage d'une entrée générée](../../images/plugin-loregenerator-compaction.png)

Pendant que le modèle travaille, toutes les commandes sont gelées sauf **Arrêter** ; vous ne pouvez pas non plus fermer la boîte de dialogue.

<a id="remembered-per-entry"></a>

### Mémorisé par entrée

Quand vous faites **Copy to Lore Entry**, les options de cette génération sont enregistrées avec l'entrée : le texte de l'invite, l'invite
enregistrée d'où elle provient, les choix de lore et d'entrées, et les catégories de noms cochées. Ouvrir à nouveau le générateur pour cette entrée
rétablit ces options, en priorité sur l'invite utilisée en dernier et sur vos catégories de noms enregistrées. Une entrée qui n'a jamais reçu
de génération s'ouvre avec les valeurs par défaut habituelles. Les options appartiennent à l'entrée : elles voyagent donc avec les sauvegardes
et exports du lorebook.

<a id="tools"></a>

### Outils

Lorsqu'une suite d'[outils pour le modèle](tools.md) est installée (noms issus du [Générateur de noms](name-generator.md) ou du
[Fournisseur de noms](name-provider.md), lieux issus du [Fournisseur de lieux](place-provider.md)), la ligne supérieure comporte un bouton **Tools**.
Il ouvre la fenêtre où vous choisissez les suites, et parmi celles qui ont des catégories, les catégories que le modèle peut utiliser ; voir
[Outils pour le modèle](tools.md) pour le fonctionnement. Tant qu'un élément est coché, le modèle peut appeler les outils pendant la
génération (pas le compactage), et l'indication de chaque suite qu'il reçoit est ajoutée à la fin de l'invite de génération. Le choix est
enregistré avec votre compte et avec l'entrée.

<a id="attachments"></a>

### Pièces jointes

Sous l'invite, vous pouvez ajouter des fichiers que le modèle peut examiner : images de référence, carte, fiche de personnage. Déposez-les dans la zone
d'envoi, choisissez-les, ou collez-les dans l'invite avec Ctrl+V (une capture d'écran ou un fichier copié devient une pièce jointe ; le texte collé est
inséré comme d'habitude). Jusqu'à 20 Mo chacune. Elles sont listées avec leur nom, leur type et leur taille, et le bouton corbeille en supprime une.
Elles sont envoyées avec la génération, et non avec le compactage ; vous ne pouvez les ajouter et les supprimer que lorsqu'aucune génération n'est en cours.

Le modèle peut ou non les utiliser selon le modèle et le [fournisseur d'inférence](../inference-providers.md) : les images sont envoyées
comme images, l'audio uniquement en WAV ou MP3, les autres fichiers comme fichiers. Lorsque le fournisseur refuse une pièce jointe, un message
s'affiche et rien n'est généré.

<a id="copying-the-result"></a>

## Copier le résultat

**Copy to Lore Entry** est disponible lorsque le compactage a un résultat. Le texte compacté devient le **contenu** de
l'entrée, et le texte généré avant le compactage va dans sa **note**. Les deux sont remplacés. Si le compactage est un seul bloc de code
(l'invite intégrée le demande, pour faciliter la copie du texte), le bloc de code qui l'entoure est retiré. Le lorebook est rafraîchi, et la
boîte de dialogue reste ouverte ; **Quitter** la ferme.
<a id="commands"></a>

## Commandes

Le générateur fonctionne aussi sans la boîte de dialogue, sous forme de trois [commandes](../commands/index.md) pour la ligne de commande et pour les
[agents](../agents.md). Elles effectuent les deux mêmes étapes que la boîte de dialogue et, comme elle, ne stockent rien : elles renvoient
le texte, et c'est à l'appelant de le placer dans une entrée (ce que fait **Copy to Lore Entry**). Les trois commandes ne font donc que lire. Le
lorebook et l'entrée sont donnés par leur numéro, comme dans `/list-lorebooks` et `/list-lore-entries` ; le modèle est votre modèle
et protocole par défaut.

| Commande | Ce qu'elle fait |
|---|---|
| `/list-lore-templates [template]` | Liste vos prompts de génération enregistrés (nom, longueur, début du texte) et celui utilisé en dernier. Avec un nom, elle renvoie le texte complet de ce prompt. |
| `/generate-lore-entry [prompt [template [lorebook [entry [order [scope [limit [images [compact]]]]]]]]]` | Génère un texte avec le lore du lorebook, le compacte (comme `/compact-lore-entry`) et renvoie le texte compacté ; avec `compact` `false`, elle renvoie le texte généré tel quel. Le texte envoyé est un modèle (template) suivi du prompt de l'appelant (voir plus bas). Avec `images`, le modèle regarde aussi des images, comme avec les pièces jointes de la boîte de dialogue. |
| `/compact-lore-entry text [lorebook [entry [order [scope [limit]]]]]` | Compacte le texte avec le même lore et renvoie le résultat, sans le bloc de code qui l'entoure. |

L'entrée à laquelle le texte est destiné détermine le lore que voit le modèle. Donnez son identifiant ou son numéro comme `entry` (et le `lorebook`
dans lequel elle se trouve, par identifiant ou numéro, lorsque vous donnez le numéro) : le lore est alors le même que dans
la boîte de dialogue. Avec `limit` `UP_TO_POSITION` (la valeur par défaut), ce sont les entrées qui la précèdent, jamais l'entrée elle-même. Une entrée qui
n'existe pas encore n'a pas de numéro : donnez à la place l'`order` qu'elle recevra (l'`order` de `/list-lore-entries`, la plus petite
vient en premier) et le modèle voit les entrées situées avant cet ordre ; sans l'un ni l'autre, le texte est écrit pour une nouvelle entrée placée après toutes les autres.
`scope` vaut `CURRENT_LOREBOOK` (la valeur par défaut) ou `ALL_LORE`, `limit` vaut `UP_TO_POSITION` ou `ALL_ENTRIES`, comme dans la boîte de dialogue.

La qualité d'une génération dépend de son prompt : la commande part donc toujours de l'un de vos prompts de génération enregistrés (voir
[Prompts](#prompts)) : celui nommé dans `template`, ou celui utilisé en dernier si aucun n'est nommé. Le `prompt` de l'appelant
suit ce modèle, sous le titre *Task for this entry*, et c'est là que vont le sujet, les faits, la longueur et les sections, comme le demande le modèle. `template` `none` n'envoie que le prompt, pour un appelant qui a écrit le prompt complet lui-même, par exemple après avoir récupéré le texte d'un modèle avec `/list-lore-templates` et l'avoir modifié. Une commande
appelée avec un modèle et sans prompt n'envoie que le modèle.

Les `images` de `/generate-lore-entry` sont les images que la boîte de dialogue joindrait à la génération. Un [agent](../agents.md) transmet une liste de
chemins de fichiers image de son espace de travail (`/refs/map.png` ; 8 au maximum, PNG, JPEG, GIF ou WebP, réduits lorsqu'ils sont grands), que vous y placez avec
**Téléverser un fichier** dans l'onglet Workspace. La barre de commandes n'offre aucun moyen de nommer une image : `images` est donc destiné aux agents. C'est votre modèle par défaut qui examine
les images : s'il ne prend pas en charge les images, la commande échoue et l'indique. Seule la génération accepte des images, comme dans la boîte de dialogue ; la compaction ne travaille que sur le texte.

Le paramètre `compact` de `/generate-lore-entry` vaut `true` par défaut : la commande génère et compacte en un seul appel, si bien qu'un [agent](../agents.md) n'a besoin que d'une commande et ne peut pas oublier la seconde étape. Passez `compact` `false` pour obtenir le long texte généré (par exemple pour le relire ou le modifier), et lancez ensuite `/compact-lore-entry` dessus.

Le modèle ne reçoit aucun outil ici. Un appelant qui veut des noms les demande d'abord, avec `/generate-names` du [Générateur de noms](name-generator.md) ou `/pick-real-names` du [Fournisseur de noms](name-provider.md), puis écrit ceux qu'il préfère dans le prompt. Un agent peut aussi récupérer le texte d'un modèle depuis `/list-lore-templates`, le modifier et transmettre le résultat comme prompt.

<a id="storage"></a>

## Stockage

Les prompts et le prompt de compaction sont stockés avec vos réglages, de sorte que les [sauvegardes de la base de données](../administration/database-backups.md)
les incluent. Rien n'est stocké avec le lorebook ; le texte généré n'existe que dans la boîte de dialogue, jusqu'à ce que vous le copiiez.
