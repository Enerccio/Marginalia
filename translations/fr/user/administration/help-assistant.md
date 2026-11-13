---
label: Assistant d'aide
order: 60
---

# Assistant d'aide

L'assistant d'aide est un chat qui répond aux questions sur Marginalia à partir de son manuel. Un administrateur choisit le
modèle qu'il utilise et accorde l'autorisation **Assistant d'aide** aux utilisateurs qui peuvent le solliciter, dans leurs
[autorisations](permissions.md) (les administrateurs y ont toujours accès) ; ils n'ont pas besoin de modèle à eux. Les utilisateurs qui existaient
lorsque les autorisations ont été introduites l'ont obtenue si l'assistant était activé à ce moment-là ; les nouveaux utilisateurs ne l'ont pas.

Une fois qu'un utilisateur peut l'utiliser, il voit un bouton robot rond dans le coin inférieur droit de la fenêtre. Il ouvre le chat ; le
panneau indique quel modèle répond et avertit lorsque la limite de l'utilisateur s'applique (voir [Qui paie](#who-pays)). Le modèle cherche les
réponses dans le manuel (il peut le parcourir et lire ses pages), cite les pages utilisées sous forme de liens vers le
[manuel publié](https://enerccio.github.io/Marginalia/) (ils s'ouvrent dans un nouvel onglet), et explique : il
ne modifie rien et ne voit pas les livres de l'utilisateur, sauf si celui-ci l'y autorise (voir [Vos livres](#your-books)). Il peut aussi examiner ce qui a mal tourné lorsqu'une opération échoue (voir
[Quand quelque chose ne va pas](#when-something-goes-wrong)) et emmener l'utilisateur à un endroit précis, si vous l'y autorisez (voir
[Indiquer le chemin](#showing-the-way)). La conversation est conservée jusqu'à la déconnexion de l'utilisateur (fermer le panneau
ou recharger la page la conserve), le bouton balai l'efface. Il lit le manuel dans la langue de l'interface de l'utilisateur lorsque le manuel
y est traduit, et les liens mènent au manuel dans cette langue ; une page non traduite est lue en anglais.

![L'assistant d'aide répondant à une question](../../images/help-assistant.png)

Le bouton apparaît lorsqu'un utilisateur ouvre Marginalia, et le modèle ainsi que l'avertissement de limite sont relus à chaque
ouverture du panneau ; si vous retirez l'autorisation ou supprimez le modèle, les utilisateurs qui ont déjà la page ouverte perdent le bouton lorsqu'ils rechargent la page
(ou lorsqu'ils ouvrent le panneau).

<a id="settings"></a>

## Réglages

Ouvrez **Admin → Assistant d'aide** :

![L'onglet Assistant d'aide](../../images/admin-help-assistant.png)

| Réglage | |
|---|---|
| **Modèle** | L'un de *vos propres* [modèles](../inference-providers.md), partagé ou non avec les utilisateurs. Les modèles que d'autres administrateurs partagent ne figurent pas dans la liste. |
| **Autoriser la lecture des données d'administration** | Permet à l'assistant de lire l'état des extensions et leurs journaux, les sauvegardes de la base de données et les autres données de la page Admin *pour un administrateur qui le demande*, voir [Données d'administration](#administration-data). Désactivé par défaut. |
| **Prompt pour les administrateurs**, **Prompt pour les autres utilisateurs** | Les instructions que le modèle reçoit avec chaque question ; voir [Prompts](#prompts). |

Deux réglages dépendent de l'utilisateur et se trouvent dans son [autorisation](permissions.md#settings-of-a-permission) **Assistant d'aide** (ouvrez-la avec le
bouton clé dans **Admin → Utilisateurs** ; les deux sont désactivés par défaut) :

| Réglage de l'autorisation | |
|---|---|
| **Appliquer les limites** | Uniquement pour un modèle partagé, voir [Qui paie](#who-pays). |
| **Autoriser la navigation dans l'interface** | Permet à l'assistant d'ouvrir une section, de sélectionner un onglet ou d'ouvrir la boîte de dialogue d'ajout lorsqu'un utilisateur lui demande de lui montrer où se trouve quelque chose, voir [Indiquer le chemin](#showing-the-way). |

Un administrateur qui ne possède pas une telle autorisation reçoit des réponses avec les deux désactivés.

Les réglages sont enregistrés avec **Enregistrer**. L'assistant interroge le modèle sans [protocole](../protocols.md), de sorte que les réglages du modèle lui-même s'appliquent. Il cesse de fonctionner de lui-même lorsque le modèle est supprimé,
ou lorsque son propriétaire n'est plus administrateur, afin qu'un modèle appartenant à un utilisateur ordinaire ne soit jamais remis à tout le monde.

<a id="what-the-assistant-sees-of-the-screen"></a>

## Ce que voit l'assistant de l'écran

Le modèle peut voir ce que l'utilisateur fait en ce moment (il en est informé lorsqu'une question porte sur « ici » ou « ceci ») : quelle
section, quel onglet ou quelle boîte de dialogue est ouvert, sous forme de courte description pour chaque partie de l'écran - par exemple le titre du
livre ouvert et son onglet ouvert, ou le titre d'une boîte de dialogue ouverte. Les onglets qui ne sont pas ouverts ne sont que listés, et il ne les lit que
lorsque la question le nécessite. Pour un formulaire ouvert, il reçoit aussi ce qui le bloque à cet instant : les champs que le formulaire
marque comme invalides avec le message affiché, les champs obligatoires vides, et ce que le formulaire vérifie entre plusieurs champs (pour un modèle : la réponse doit être plus petite que le contexte). Uniquement les *noms* des champs, jamais ce qui est saisi
dedans. Il ne reçoit jamais le texte d'une histoire, d'une entrée de livre de références, d'une clé d'API ni d'un mot de passe.

![L'assistant expliquant pourquoi le formulaire d'un nouveau modèle ne peut pas encore être enregistré](../../images/help-assistant-form.png)

<a id="your-books"></a>

## Vos livres

Le panneau de l'assistant contient la case **Laisser l'assistant consulter mes livres**, désactivée par défaut et réglée par chaque utilisateur
pour lui-même (elle est conservée dans ses réglages). Un avis sous celle-ci explique ce que cela signifie : l'assistant lit les noms de dossiers, les titres,
les descriptions et les réglages d'écriture des livres et recherche dans leur texte, sans jamais lire le texte d'une histoire, et ce qu'il lit est
envoyé à l'IA qui répond - c'est le modèle que vous avez choisi ci-dessus, il faut donc le dire aux utilisateurs si c'est un service échappant à votre contrôle.

![Le panneau de l'assistant avec la case pour les livres](../../images/help-assistant-books.png)

Lorsqu'elle est activée, le modèle reçoit trois fonctions, toutes en lecture seule et uniquement pour les livres de l'utilisateur qui le demande :

| Fonction | |
|---|---|
| `list_folder` | Les sous-dossiers d'un dossier, avec le nombre de livres contenus dans chacun, et ses livres avec titre et description. Un chemin comme `Sci-fi/Expanse` désigne le dossier, l'absence de chemin désigne le niveau supérieur. |
| `search_books` | Trouve des livres par les mots du titre ou de la description, ou par le texte de l'histoire, éventuellement dans un dossier (« où est mon livre sur les vrais amoureux ? »). Il indique où une correspondance a été trouvée et dans quel dossier ; le texte trouvé n'est pas renvoyé. |
| `get_book_settings` | La langue, le point de vue, le temps et le style d'un livre et d'où provient chacun (le livre, un dossier, les réglages de l'utilisateur ou la valeur par défaut intégrée). Les prompts ne sont signalés que comme *un texte personnalisé* ou *le texte intégré*, avec leur origine. |

Il ne peut rien modifier, déplacer ni créer. Les listes sont tronquées (cent entrées, vingt correspondances) et les descriptions raccourcies.

<a id="when-something-goes-wrong"></a>

## Quand quelque chose ne va pas

Lorsqu'un utilisateur signale qu'une opération a échoué (« j'ai une erreur », « ça n'enregistre pas », « le modèle ne répond pas »), l'assistant peut lire ce que Marginalia
a affiché à l'utilisateur : les dernières erreurs, les requêtes échouées vers le fournisseur du modèle (mauvaise clé, modèle inexistant, limite
atteinte, délai dépassé...), les avertissements et les messages des formulaires qui n'ont pas pu être enregistrés, du plus récent au plus ancien et avec le temps écoulé
depuis chacun. Il explique ce que signifie le message et ce qu'il faut corriger, et pour un formulaire ouvert il indique quels champs modifier.

Ce qui est consigné, et où :

* tout ce qui est affiché à un utilisateur comme erreur ou avertissement dans une notification, ainsi que le message d'une validation échouée, est conservé
  pour la session (les 20 derniers) ;
* les échecs inattendus (ceux accompagnés d'une boîte de dialogue de trace d'appel) et les requêtes échouées vers un fournisseur sont aussi conservés par l'onglet du navigateur
  (les trois derniers, dans son stockage de session), ils sont donc toujours là après un rechargement de la page ou l'expiration de la session.

L'assistant reçoit le texte vu par l'utilisateur et, pour un échec, les noms et messages des exceptions, sans la trace d'appel.
Chaque texte est tronqué à 400 caractères et ce qui ressemble à une clé, un jeton ou un mot de passe est supprimé, mais le texte d'un message
peut encore contenir une partie de ce qu'un fournisseur ou le système a répondu, par exemple la réponse d'un fournisseur à une requête. Le modèle qui répond est celui que vous avez choisi, qui peut
être un fournisseur externe : gardez-le à l'esprit lorsque vous [le choisissez](#settings).
<a id="showing-the-way"></a>

## Montrer le chemin

Avec **Autoriser la navigation dans l'interface** activé dans l'autorisation de l'utilisateur, l'assistant peut faire quelques actions dans la fenêtre elle-même lorsqu'un utilisateur le demande
(« montre-moi où sont les lorebooks », « emmène-moi aux sauvegardes », « comment j'ajoute un modèle ? ») : ouvrir une section (Livres, Lorebooks, Paramètres, ...), la page Admin
(seuls les administrateurs y ont accès), sélectionner un onglet des Paramètres, de la page Admin ou de l'éditeur de récit, ou appuyer sur le bouton qui
ouvre la boîte de dialogue d'ajout d'un livre, d'un modèle ou d'un protocole (la boîte de dialogue ne fait que s'ouvrir : rien n'est créé tant que l'utilisateur ne l'a pas remplie et confirmée ; elle n'est pas proposée tant qu'une autre boîte de dialogue est ouverte). Il indique à l'utilisateur ce qu'il a fait.
Il ne peut rien faire d'autre : il n'appuie sur aucun autre bouton, ne tape rien, n'enregistre rien, ne supprime rien, ne génère rien, ne ferme pas de boîte de dialogue et ne se déconnecte pas, et un utilisateur qui ne peut pas ouvrir quelque chose ne peut pas faire ouvrir ce même élément par l'assistant. Il ne se déplace pas de sa propre initiative, seulement lorsqu'on le lui demande.
Lorsque la navigation est verrouillée (la section ouverte contient des modifications non enregistrées, un livre est en cours de génération), il ne peut pas se déplacer.

![L'assistant a ouvert l'onglet Database Backups sur demande](../../images/help-assistant-navigate.png)

L'autoriser transmet aussi au modèle les noms des éléments à l'écran et ce qu'ils proposent ; c'est le même type d'informations que dans [Ce que l'assistant voit de l'écran](#what-the-assistant-sees-of-the-screen).

<a id="administration-data"></a>

## Données d'administration

Avec **Autoriser la lecture des données d'administration** activé, l'assistant peut lire les données de la page Admin pour un administrateur qui le demande
(« pourquoi Side Query ne fonctionne-t-il pas ? », « quand a eu lieu la dernière sauvegarde ? »), et pour personne d'autre ; un utilisateur ordinaire
ne les obtient jamais, quel que soit le réglage, et l'accès est vérifié à nouveau à chaque lecture.

| Sujet | Ce qui est lu |
|---|---|
| Extensions | Chaque [extension](../extensions/index.md) installée avec sa version, si elle est active, arrêtée par vous ou si elle n'a pas pu démarrer, et le résultat de sa vérification |
| Le journal d'une extension | Les dernières lignes de son journal (40 par défaut, 200 au maximum), comme **Admin → Extensions → Log** |
| Utilisateurs | Le nombre d'utilisateurs, d'administrateurs et de comptes bloqués ; aucun nom |
| Sauvegardes de la base | La taille de la base, le nombre de sauvegardes et la date de la plus récente, la planification, et si une restauration est en attente |
| Nettoyage | Ce qu'un [nettoyage](cleanup.md) supprimerait (rien n'est supprimé) et ce qui le bloque |
| Application | La version de Java, la mémoire utilisée et la durée de fonctionnement de l'application |

La lecture est seule : l'assistant ne peut rien démarrer, arrêter ni supprimer. Gardez à l'esprit que tout cela est envoyé au modèle qui répond, qui peut être celui d'un fournisseur externe, et que le journal d'une extension peut contenir du texte écrit par l'extension (une ligne de journal est coupée à 400 caractères et les clés, jetons et mots de passe sont retirés, mais rien de plus n'est fait). C'est pourquoi le réglage reste désactivé tant que vous ne l'activez pas.

## Prompts

Il existe deux prompts : un pour les administrateurs (qui peuvent poser des questions sur la page Admin) et un pour tous les autres (à qui l'on indique que l'administration est réservée aux administrateurs). Les deux sont remplis avec les valeurs par défaut ; modifiez le texte puis enregistrez, **Reset to default** rétablit la valeur par défaut. Un prompt vide ou identique à la valeur par défaut est enregistré comme « la valeur par défaut » : il s'améliore donc avec une nouvelle version de Marginalia. Un prompt que vous avez modifié reste le vôtre, c'est à vous de le tenir à jour.

`{{manualIndex}}` dans un prompt est remplacé par la liste des pages du manuel (chemin, titre et sujet de chaque page), ce qui permet au modèle de savoir ce qu'il peut lire. Si vous le retirez, le modèle ne trouve les pages qu'en effectuant une recherche. Conservez les instructions demandant de chercher les réponses dans le manuel et de le signaler lorsque le manuel ne couvre pas une question, sinon les réponses deviennent moins fiables. Les règles des outils qui lisent les erreurs, se déplacent dans l'interface et lisent les données d'administration sont ajoutées au prompt par Marginalia (vous n'avez pas à les y garder), et seulement pour les outils autorisés.

<a id="who-pays"></a>

## Qui paie

Les utilisateurs ne voient jamais le modèle ni sa clé d'API ; leurs questions passent par l'application.

| Modèle | **Apply limits** (autorisation de l'utilisateur) | Ce qui se passe |
|---|---|---|
| Partagé | activé | Une question est une requête comme une autre adressée au modèle partagé : la [limite](../inference-providers.md#limits-of-shared-providers) que vous avez fixée pour l'utilisateur est vérifiée et les jetons sont ajoutés à la consommation de l'utilisateur. Un utilisateur dont la limite est atteinte (ou qui n'a pas le droit d'utiliser le modèle) n'obtient aucune réponse. |
| Partagé | désactivé | C'est le propriétaire du modèle qui paie. La limite de l'utilisateur est ignorée et rien n'est comptabilisé pour lui. |
| Non partagé | (sans effet) | C'est le propriétaire du modèle qui paie : pas de limite, rien de comptabilisé pour l'utilisateur. Le modèle n'est utilisé que par l'assistant. |

Vos propres questions sont comptées de la même façon : pour vous, le modèle n'est qu'un modèle parmi d'autres.
