---
label: Lecteurs bêta
order: 22
---

# Lecteurs bêta

Des lecteurs que vous inventez, et une discussion avec eux à propos de votre livre. Vous décrivez un **persona** (un nom, une icône et quelques lignes sur la façon dont la personne lit et sur ce qui compte pour elle), et le modèle joue ce lecteur : il réagit aux parties de votre histoire comme le ferait ce lecteur, seul ou dans un groupe qui débat entre ses membres. Rien de ce que disent les lecteurs ne fait partie du livre.

![Une discussion de groupe avec deux personas](../../images/plugin-betareaders-chat.png)

<a id="the-beta-readers-tab"></a>

## L'onglet Lecteurs bêta

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **Lecteurs bêta**, à côté de *Plan*. Il comporte un onglet pour chaque discussion ouverte du livre. Le bouton **+** démarre une discussion, et le menu (⋮) à côté contient :

| Élément | |
|---|---|
| Nouvelle discussion | Identique à **+**. |
| Renommer la discussion | Modifie le titre de la discussion. |
| Fermer la discussion | Ferme l'onglet ; la discussion est conservée et se trouve dans *Discussions fermées*. |
| Paramètres de la discussion | Modifie les [options de la discussion](#the-options-of-a-chat). |
| Supprimer la discussion | Supprime la discussion et son historique, après confirmation. |
| Personas de ce livre | Les personas qui n'appartiennent qu'à ce livre, voir [plus bas](#personas). |
| Discussions fermées | Rouvre une discussion fermée. |

Les discussions sont enregistrées avec le livre, elles figurent donc dans ses sauvegardes.

![L'onglet, avant la première discussion](../../images/plugin-betareaders-tab.png)

![Le menu des discussions](../../images/plugin-betareaders-menu.png)

## Personas

Un persona possède un **nom**, une **description** (ce que joue le modèle : la façon de lire du persona, ce qu'il aime et ce qui l'agace) et une **icône** facultative, une image réduite à une petite taille. Sans icône, le persona est représenté par un cercle portant les premières lettres de son nom.

Les personas de votre compte forment la **bibliothèque de personas** dans *Paramètres → Paramètres de l'extension → Lecteurs bêta*, qui démarre avec six lecteurs : Athena, Vesper, Ember, Quill, Nox et Sable. **Modifier les personas** ouvre la bibliothèque dans une fenêtre où vous pouvez ajouter, modifier, copier, supprimer et réordonner des personas, téléverser leurs icônes et **Ajouter les personas de départ** qui manquent à la bibliothèque. Les modifications sont appliquées avec **Enregistrer**, et **Annuler** les abandonne.

![La bibliothèque de personas](../../images/plugin-betareaders-library.png)

Un livre peut avoir ses propres personas : *Personas de ce livre* dans le menu des discussions ouvre la même fenêtre, avec les personas que seul ce livre propose. Ils sont marqués *(ce livre)* lorsque vous les choisissez pour une discussion.

![Les personas d'un livre](../../images/plugin-betareaders-book-personas.png)

**Écrire avec le modèle** dans la fenêtre d'un persona fait l'écriture à votre place : décrivez le persona en quelques mots (*un lecteur qui ne s'intéresse qu'à qui l'a fait et pourquoi*), appuyez sur le bouton, et le modèle remplit le **Nom** et la **Description** dans le style de la bibliothèque. Ce ne sont que des champs : modifiez-les, ou recommencez, puis **Enregistrer** lorsqu'ils vous conviennent. Le modèle qui écrit est celui des discussions (celui du livre pour les personas d'un livre, celui des paramètres de l'extension pour la bibliothèque, sinon votre modèle par défaut) ; les noms déjà présents dans la liste lui sont signalés pour qu'il n'en prenne pas un.

![Un persona écrit par le modèle](../../images/plugin-betareaders-write-persona.png)

Une discussion conserve une **copie** de ses personas, de sorte qu'une modification de la bibliothèque ne change pas une discussion déjà commencée ; la copie est modifiée lorsque vous le demandez (voir [le groupe](#a-group)).

<a id="starting-a-chat"></a>

## Démarrer une discussion

**+** ouvre la fenêtre d'une nouvelle discussion.

![La fenêtre d'une nouvelle discussion](../../images/plugin-betareaders-new-chat.png)

| Champ | |
|---|---|
| Type | **Un persona** ou **Un groupe**. Il ne peut pas être modifié ensuite. |
| Titre | Vide : les noms des personas. |
| Persona(s) | Un seul persona pour une discussion à un ; pour un groupe, au moins un (un groupe peut commencer avec un persona, et d'autres se joindre plus tard). |

La partie repliée, **Histoire, support, modèle et format**, contient les [options d'une discussion](#the-options-of-a-chat). Chacune d'elles peut rester vide, et elle est alors héritée.

Une discussion avec **un persona** est une conversation entre vous et ce persona. Un **groupe** est une conversation de tous les personas présents avec vous et entre eux : une réponse peut faire parler plusieurs d'entre eux, l'un après l'autre, et ils peuvent être en désaccord, se taquiner et s'appuyer sur les remarques des autres. Un message de la réponse peut aussi être une narration (*Narrateur*) : une courte description de ce que fait le persona.
<a id="talking"></a>

## Discussion

Écrivez dans la zone en bas et appuyez sur **Envoyer** ; la réponse apparaît au fur et à mesure qu'elle est écrite, et **Arrêter** (le bouton s'appelle *Arrêter* pendant que le modèle répond) l'interrompt. Une réponse correspond à un tour du modèle : sa réflexion, lorsqu'il en envoie une (une section **Raisonnement** repliée ; un modèle qui ne réfléchit pas n'en affiche aucune), et les messages des personas, chacun avec ce qu'il fait et ce qu'il dit.

<a id="sending-a-scene"></a>

### Envoyer une scène

**Envoyer comme scène** (à côté de *Envoyer*) envoie le texte comme une scène plutôt que comme une parole : un changement de décor (*Les lumières s'éteignent et le bar se tait.*), ou la description de ce que vous faites (*Je pose le manuscrit sur la table et j'attends.*). Elle apparaît sous forme de carte **Scène** dans l'historique, et les personas réagissent à mesure, comme à un message. Utilisez la scène de la discussion (voir [plus bas](#the-scene)) pour indiquer où la conversation commence, et ce bouton pour ce qui se passe ensuite. Un message de scène peut être modifié et supprimé comme vos messages ; il n'envoie pas de parties de l'histoire (la case *Inclure les dernières parties* concerne *Envoyer*).

![Une scène dans l'historique d'une discussion, et la réponse à celle-ci](../../images/plugin-betareaders-scene.png)

Les personas du groupe sont informés de qui est présent à la fin de votre dernier message uniquement (rien de cela n'est enregistré), de sorte qu'ajouter ou retirer un persona ne modifie jamais l'historique antérieur.

<a id="sending-the-story"></a>

### Envoyer l'histoire

La discussion ne conserve pas de copie de l'histoire. Elle se souvient des parties que vous avez **données** aux personas, et pour chaque requête, l'histoire qu'ils lisent est reconstruite à partir de l'histoire telle qu'elle est actuellement : les parties qui leur ont été données, avec les résumés à la place des plus anciennes, dans la limite de ce que le contexte du modèle peut contenir. Les parties se placent devant le message qui les a données, de sorte qu'une partie n'est présente dans la requête qu'une fois, et que le début de la requête reste le même texte lorsque vous donnez davantage de parties : un serveur de modèle qui conserve le début du prompt ne lit que ce qui est nouveau. Une partie modifiée est lue telle qu'elle a été modifiée, et une partie supprimée n'est plus lue, ce qui modifie la requête à partir du message qui l'avait donnée. Un résumé créé plus tard remplace les parties qu'il décrit à partir du message suivant.

**Inclure les dernières parties** est ce qui donne aux personas davantage de l'histoire : il ajoute les parties qu'ils n'ont pas encore reçues (voir [plus bas](#how-much-of-the-story-the-personas-read)). Laissez-la décochée pour simplement discuter et leur donner l'histoire plus tard. L'étiquette de la case indique combien de parties elle ajouterait (*Inclure l'histoire (5 parties)*, *Inclure les dernières parties (2 nouvelles)*, *aucune nouvelle*). Elle suit l'histoire d'elle-même : lorsque vous ajoutez, modifiez ou supprimez une partie, ou que vous changez de branche, le nombre change et la case est cochée lorsqu'il y a du nouveau. Un de vos messages indique les parties qu'il a données (*Parties 1-5 envoyées*).

**Dernière partie** et **Première partie**, à côté de la case, déterminent [quelle part de l'histoire les personas lisent](#how-much-of-the-story-the-personas-read) ; modifiez-les à cet endroit au fil de votre lecture.

Sous la case figurent ce que les personas ont lu (*Les personas ont lu : parties 1-5*) et la taille du contexte (*Contexte : 2 266 sur 30 720 jetons*) : ce que enverrait le prochain message, c'est-à-dire l'historique, ce que vous avez tapé et, lorsque **Inclure les dernières parties** est cochée, les parties qu'il ajouterait. Cela change lorsque vous cochez la case ou que vous faites une pause pendant la saisie. Les parties ont au moins la moitié du contexte ; lorsque l'historique ne tient pas à côté d'elles, les messages les plus anciens (avec les parties qu'ils ont données) sont laissés de côté dans la requête, sans être supprimés, et un avis indique combien il y en a.

<a id="changing-the-conversation"></a>

### Modifier la discussion

| Bouton | |
|---|---|
| crayon (votre message) | Modifie votre message ; les parties qu'il a données restent données. |
| corbeille | Supprime votre message, ou un message d'une réponse (le reste du tour reste, et le modèle voit le tour sans celui-ci). |
| Régénérer | Supprime la dernière réponse et redemande. |

Lorsqu'une réponse a échoué ou qu'il n'y en a pas, le panneau l'indique et propose **Demander à nouveau**.

![Les actions d'un message](../../images/plugin-betareaders-actions.png)

<a id="a-group"></a>

### Un groupe

*Personas (n)* au-dessus de la conversation indique qui est présent. Dans un groupe, vous pouvez :

- **Faire entrer un persona** depuis la bibliothèque ou le livre, avec une note facultative qui explique au groupe pourquoi il est venu (« Mara a lu tout le livre »). L'historique reçoit une ligne (*Mara a rejoint la conversation.*), afin que le modèle sache quand.
- **Faire sortir** un persona (le bouton flèche de sa ligne), à nouveau avec une note. Il reste dans la liste, grisé et marqué *(parti)*. Le dernier persona ne peut pas partir.
- **Mettre à jour depuis la bibliothèque** : reprend dans la discussion la description et l'icône actuelles du persona depuis la bibliothèque (ou le livre).

![Un persona qui a rejoint le groupe](../../images/plugin-betareaders-joined.png)

![Faire entrer un persona](../../images/plugin-betareaders-bring-in.png)
<a id="the-options-of-a-chat"></a>

## Les options d'un chat

La fenêtre d'un nouveau chat et **Paramètres du chat** proposent les mêmes options ; chacune peut rester vide, et le chat hérite alors de la valeur.

| Option | |
|---|---|
| Votre nom | Le nom par lequel les personnages vous appellent ; vos messages affichent celui-ci à la place de *Vous*. Vide : pas de nom. |
| Scène | Le lieu et la manière dont la conversation se déroule, en quelques phrases : *un bar pluvieux après la fermeture*, *un appel vidéo après le club de lecture*. Sans scène, le chat est un chat en ligne, voir [plus bas](#the-scene). Changer la scène d'un chat en cours modifie le prompt système à partir de la requête suivante et ne laisse aucune ligne dans l'historique ; utilisez [un message de scène](#sending-a-scene) pour ce qui se passe à partir de maintenant. |
| Support | Ce que représente le texte : un livre, un film, une série télévisée, un manga ou une bande dessinée, une fiction audio, une pièce de théâtre, un jeu ou un roman visuel. Les personnages réagissent comme le ferait le public de ce support. Changer le support d'un chat en cours laisse une ligne dans l'historique (*À partir de maintenant, le texte est lu comme : ...*). |
| Modèle | L'IA qui répond. |
| Protocole | Le [protocole](../protocols.md) utilisé avec lui. |
| Format de réponse | La manière dont le modèle est invité à écrire : **Automatique** (JSON selon un schéma lorsque le modèle sait le suivre, texte libre sinon), **JSON** (toujours selon un schéma), **JSON sans schéma** ou **Texte libre**. Voir [le raisonnement](#the-reasoning). |
| Les personnages lisent jusqu'à la partie | La [limite de spoilers](#how-much-of-the-story-the-personas-read). Uniquement dans la fenêtre d'un nouveau chat ; un chat existant la propose sous la zone de saisie. |
| Commencer à la partie | La partie à partir de laquelle l'histoire lue par les personnages commence. Même remarque que ci-dessus. |
| Utiliser les résumés de l'histoire | Voir ci-dessous. |
| Messages maximum par tour | Le nombre maximum de messages que les personnages écrivent en réponse à un de vos messages (de 1 à 50). Vide : votre réglage dans *Paramètres → Paramètres des extensions → Beta Readers*. Une réponse qui en contient davantage est coupée, et le modèle en est informé. |

![Les paramètres d'un chat](../../images/plugin-betareaders-chat-settings.png)

<a id="the-reasoning"></a>

### Le raisonnement

Le **Raisonnement** affiché au-dessus d'une réponse est la réflexion du modèle, telle que la transmet le serveur (comme dans les autres générateurs de Marginalia) ; un modèle qui ne réfléchit pas n'a pas de raisonnement, et rien n'est affiché. L'extension ne demande pas au modèle un raisonnement propre : quelques phrases écrites selon un schéma seraient davantage un plan qu'une réflexion, et gêneraient un modèle qui réfléchit déjà. La réflexion écrite dans le texte entre des balises `<think>` est elle aussi affichée comme raisonnement.

**JSON sans schéma** (dans *Paramètres du chat*) demande le même JSON uniquement dans le prompt, sans format de réponse ; utilisez-le avec un serveur qui ne peut pas recevoir de schéma en même temps que la réflexion du modèle.

<a id="the-text-as-it-is-written"></a>

### Le texte tel qu'il s'écrit

La réponse s'affiche pendant que le modèle l'écrit, message par message et mot par mot : le message en cours d'écriture apparaît dès que l'on sait qui parle, puis grandit. Une partie qui est une action apparaît dès que son type est écrit, si bien qu'un geste ne ressemble jamais d'abord à une réplique.

La conversation suit la réponse à mesure qu'elle grandit. Si vous remontez pour relire quelque chose de plus ancien, la vue reste à cet endroit ; redescendez jusqu'à la fin et elle suit à nouveau. L'envoi d'un message amène toujours à la fin.

<a id="the-scene"></a>

### La scène

La scène est un texte qui entre dans le prompt système de chaque chat, après le prompt, afin que les personnages sachent où ils se trouvent et comment ils se parlent. C'est un [réglage hérité](../books/prompts.md#inherited-settings) : celui du chat, sinon celui du livre, sinon celui du dossier le plus proche, sinon celui de *Paramètres → Paramètres des extensions → Beta Readers*, sinon le réglage intégré : **un chat en ligne**, où chacun écrit de courts messages depuis l'endroit où il se trouve. Un prompt à vous qui contient `{{scene}}` décide de la place de la scène ; sans lui, la scène est ajoutée après le prompt.

<a id="how-much-of-the-story-the-personas-read"></a>

### Quelle part de l'histoire les personnages lisent

Les parties sont numérotées à partir de 1, comme dans l'onglet *Plan* de l'histoire.

- **Les personnages lisent jusqu'à la partie** (le champ **Dernière partie** sous la zone de saisie) est la limite de spoilers : les personnages ne reçoivent jamais une partie postérieure, et les résumés ne sont recueillis que dans l'histoire jusqu'à celle-ci. Relevez-la lorsque l'histoire avance ; le message suivant proposera alors comme nouvelles les parties mises de côté. Abaissez-la, et les personnages cessent immédiatement de lire les parties suivantes.
- **Commencer à la partie** (**Première partie**) laisse de côté les parties précédentes, pas même sous forme de résumés. Utilisez-le pour que les personnages ne lisent qu'un seul chapitre.
- **Utiliser les résumés de l'histoire** (activé par défaut) permet à l'histoire de commencer par les résumés, puis par les parties postérieures au plus récent d'entre eux, ce qui garde une longue histoire compacte. Les résumés ne s'appliquent que lorsque l'histoire commence à son début.

Une première partie postérieure à la dernière est refusée.

Modifier ces réglages sous la zone de saisie (ils sont enregistrés dès que vous arrêtez de taper ou cliquez sur un bouton de pas) change l'histoire lue par les personnages à partir du message suivant ; rien n'est renvoyé, car l'histoire est construite pour chaque message.

![Un chat qui ne lit que les parties 2 à 3](../../images/plugin-betareaders-spoiler.png)

Le lorebook n'est pas transmis aux personnages : ils ne connaissent que ce que l'histoire leur raconte.

<a id="settings"></a>

## Paramètres

*Paramètres → Paramètres des extensions → Beta Readers* est le lieu de vos valeurs par défaut :

![Les paramètres de l'extension](../../images/plugin-betareaders-settings.png)

| Réglage | |
|---|---|
| Support | Le support des chats. |
| Modèle des chats | L'IA. Vide : le modèle du livre. |
| Protocole des chats | Vide : le protocole du livre. |
| Prompt d'un chat avec un personnage | Le prompt système d'un chat en tête-à-tête. Vide : le prompt intégré. |
| Prompt d'un chat de groupe | Le prompt système d'un chat de groupe. Vide : le prompt intégré. |
| Scène | Voir [plus haut](#the-scene). Vide : un chat en ligne. |
| Messages maximum par tour | Le nombre maximum de messages que les personnages écrivent en réponse à un de vos messages, pour chaque chat qui ne fixe pas de limite propre (de 1 à 50). Vide : automatique, soit 3 pour un chat avec un personnage et environ deux pour chaque personnage d'un groupe (8 au maximum). Une limite courte garde les réponses brèves ; augmentez-la lorsque les personnages sont coupés en pleine conversation. |
| Bibliothèque de personnages | **Modifier les personnages**, voir [plus haut](#personas). |
| Supports | **Modifier les supports** : chaque support a un nom et le prompt qui indique aux personnages comment y réagir. **Restaurer les supports intégrés** ajoute ceux qui manquent. |

`{{medium}}` dans un prompt est remplacé par le texte du support, et `{{scene}}` par la scène. Les réglages sont enregistrés avec le bouton **Enregistrer** de la page *Paramètres*.

La scène, le support, le modèle, le protocole et les deux prompts sont aussi des [réglages hérités](../books/prompts.md#inherited-settings) : l'onglet *Prompts* d'un livre et les réglages d'un [dossier](../books/folders.md) ont ces valeurs dans la section **Paramètres des extensions**. Un livre utilise sa propre valeur, sinon celle du dossier le plus proche, sinon celle de *Paramètres*, sinon celle intégrée ; le modèle et le protocole du livre servent en dernier recours. Un chat remplace pour lui-même le support, le modèle et le protocole dans ses [options](#the-options-of-a-chat).

<a id="on-a-phone"></a>

## Sur téléphone

Fonctionne. L'onglet **Beta Readers** se trouve dans le tiroir du plan de l'histoire, et les chats sont les mêmes que sur ordinateur ; l'espace réservé à la conversation est restreint, donc le mode paysage est plus pratique. Voir [Utiliser Marginalia sur téléphone](../phone.md#extensions).

<a id="storage"></a>

## Stockage

Les personnages et les chats d'un livre sont enregistrés dans les attributs du livre (donc avec ses sauvegardes), les personnages de votre compte et les réglages dans vos paramètres utilisateur. Le modèle est sollicité selon les limites habituelles de l'IA, et les jetons sont comptés comme utilisés par le livre.
