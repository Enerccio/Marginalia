---
label: Side Query
order: 1
---

# Side Query

Un chat avec le modèle à côté de votre récit : posez des questions sur l'intrigue, vérifiez la cohérence, cherchez des noms ou le prochain chapitre, sans rien ajouter au livre.

!!!
Side Query fonctionne dans la [disposition téléphone](../phone.md#extensions) : son onglet se trouve dans le tiroir du plan du récit.
!!!

![Side Query à côté du récit](../../images/plugin-sidequery.png)

<a id="the-side-query-tab"></a>

## L'onglet Side Query

La barre latérale gauche de l'[éditeur de récit](../books/story-editor.md) affiche un onglet **Side Query** à côté de *Plan*. Faites glisser le séparateur pour lui donner plus de place.

L'onglet contient des conversations, chacune dans son propre sous-onglet : **+** ouvre une nouvelle conversation (*Tab 2*, *Tab 3*...), le crayon renomme un sous-onglet, **×** le ferme (le dernier ne peut pas être fermé). Avec *Enable AI Tab Naming* dans le profil, le modèle nomme un sous-onglet après sa première réponse, sauf si vous l'avez renommé vous-même. Les conversations appartiennent au livre et sont conservées jusqu'à la fermeture de leur sous-onglet.

<a id="asking"></a>

## Poser une question

Saisissez une question dans la zone en bas et cliquez sur **SEND**. La réponse s'affiche au fur et à mesure qu'elle est écrite ; **STOP** (à la place de *SEND*) l'interrompt.

| Bouton | |
|---|---|
| **SEND** | Envoie la question. |
| **UNDO** | Supprime le dernier message. S'il s'agissait de votre question, elle retourne dans la zone de saisie pour être modifiée. |
| **REGENERATE** | Supprime la dernière réponse et repose la question. |

Chaque message dispose de boutons pour le **déplacer** vers le haut ou vers le bas, le **modifier**, le **copier** dans le presse-papiers et l'**exclure** du contexte (ou le réinclure). Les messages exclus restent dans la conversation mais ne sont pas envoyés au modèle : c'est pratique pour écarter une mauvaise réponse sans la supprimer.

<a id="what-the-model-sees"></a>

## Ce que voit le modèle

Les options au-dessus de la conversation déterminent ce qui est envoyé avec vos questions :

| Option | |
|---|---|
| **Lorebook** | Le lore du livre (voir ci-dessous). |
| **Chat Logs** *from* … *to* | Les parties du récit de la branche active dans cette plage, numérotées à partir de 1 comme dans le plan : `1` à `6` correspondent aux parties `#1` à `#6`. |
| **Bouton de plugin** | Ouvre une liste des données que d'autres extensions proposent, avec une case à cocher pour chacune (voir ci-dessous). |
| **Tokens** | La taille approximative de la requête. |

!!!
*Lorebook* rassemble le lore comme le fait une génération : le lorebook du livre et ses sous-lorebooks (les lorebooks désactivés sont ignorés), ainsi que les entrées activées dont les étiquettes correspondent à celles du livre, avec les [macros](../templates/macros.md) traitées. Les filtres par mots-clés ne sont pas appliqués : il n'y a pas de prompt de récit à comparer, donc toutes les entrées applicables sont envoyées. Les variables définies par les entrées ne sont pas conservées.
!!!

<a id="data-of-other-extensions"></a>

### Données des autres extensions

Le bouton de plugin ouvre une fenêtre contextuelle avec les **sources supplémentaires** que proposent d'autres extensions, par exemple [Arc Outline](arc-outline.md) propose votre plan. Chaque source a une case à cocher ; la liste comporte jusqu'à deux parties :

- **About the book** : une source cochée est ajoutée à la requête après le lorebook, sous son nom.
- **About story parts** : une source cochée est demandée pour chaque partie du récit de la plage *Chat Logs*, et ce qu'elle renvoie est ajouté après le texte de cette partie. Elle n'a d'effet que si *Chat Logs* est activé.

Le choix est conservé avec l'onglet. Si une extension n'est pas installée (ou est arrêtée), ses sources ne figurent pas dans la liste et ne sont pas envoyées ; elles réapparaissent dès que l'extension est de nouveau disponible. Si aucune source n'est proposée, la fenêtre contextuelle l'indique.

La requête est construite ainsi :

1. un message système : le *Initial System Query* du profil, les entrées du lorebook (*Lorebook Context*), les sources cochées sur le livre et sur les parties du récit (*Chat History Context*, étiquetées avec leurs numéros de plan *Message #1*, *#2*..., chacune suivie des sources cochées sur les parties du récit) ;
2. les messages de la conversation qui sont inclus, sous forme de chat. Les *Instructions Before User Input* du profil sont placées au début de votre dernière question, avant son texte.

Le modèle et le protocole viennent du profil (voir ci-dessous), par défaut ceux du livre. Un modèle économique et rapide suffit généralement pour les questions sur le récit.

<a id="saved-queries"></a>

## Requêtes enregistrées

Les questions que vous posez souvent peuvent être enregistrées :

- **Save prompt** (icône de disquette) enregistre le texte de la zone de saisie sous le nom sélectionné dans la liste, ou demande un nom ;
- **Save prompt as...** l'enregistre sous un nouveau nom ;
- choisissez un nom dans la liste pour placer le texte enregistré dans la zone de saisie ;
- le bouton corbeille **Supprimer** la requête enregistrée sélectionnée.

Les requêtes enregistrées appartiennent à votre compte et sont disponibles dans chaque livre.

Quelques exemples utiles :

- *Liste chaque personnage nommé apparu jusqu'ici, avec une ligne sur chacun.*
- *Quels fils de l'intrigue restent ouverts ?*
- *Propose cinq noms pour une ville portuaire de ce monde.*

<a id="profiles"></a>

## Profils

Les paramètres de Side Query se trouvent dans **Paramètres → Extension Settings → SideQuery Settings**, organisés en **profils** comme ceux du [Reviewer](reviewer.md#profiles). Le profil sélectionné est celui utilisé :

![Les paramètres de SideQuery](../../images/plugin-sidequery-settings.png)

| Champ | |
|---|---|
| **Profile**, **New**, **Rename**, **Delete** | Choisir et gérer les profils. *_Default* ne peut être ni renommé ni supprimé. |
| **SideQuery Model**, **SideQuery Protocol** | Le [fournisseur d'inférence](../inference-providers.md) et le [protocole](../protocols.md) à utiliser. Vide : ceux du livre. |
| **Initial System Query** | Le début du message système, par exemple le rôle de l'assistant. |
| **Instructions Before User Input** | Texte placé juste avant votre dernière question : règles pour les réponses, par exemple *Answer briefly. Quote the story when you can.* |
| **Enable AI Tab Naming** | Après la première réponse d'un sous-onglet, le modèle est sollicité pour un nom court de la conversation (une requête supplémentaire). Les sous-onglets que vous avez renommés gardent leur nom. |

Cliquez sur **Enregistrer** en haut de la page *Paramètres* pour conserver les modifications.

<a id="storage"></a>

## Stockage

Les conversations sont stockées avec le livre ; elles font donc partie des [sauvegardes du livre](../books/backups.md) et des copies. Les profils et les requêtes enregistrées sont stockés dans les paramètres de votre utilisateur.
