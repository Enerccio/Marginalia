---
label: Convertisseur
order: 16
---

# Convertisseur

!!!
Cette extension s'appelait *SillyTavern Converter* avant la version 1.6.0. Seul le nom a changé ; ses fichiers, les données enregistrées avec vos livres et les dispositions sauvegardées sont les mêmes.
!!!

Réécrivez une conversation de jeu de rôle [SillyTavern](https://github.com/SillyTavern/SillyTavern) sous forme d'histoire, tour par tour : chargez la
conversation (ou une conversation [LM Studio](https://lmstudio.ai)) dans un livre, laissez le modèle la découper en *détails de tour* à partir de la conversation et du lore du livre, puis placez chaque tour
dans le nouveau formulaire de tour de l'histoire d'un simple clic.

La source n'a pas besoin d'être une conversation : le convertisseur peut aussi prendre les parties d'un autre de vos livres (depuis un fichier de sauvegarde ou
le livre lui-même) et vous en donner les instructions une à une, pour [reformuler un livre](#redrafting-a-book).

Pour créer directement un livre à partir d'une conversation (chaque message, ou chaque paire utilisateur et assistant, une partie), voir
[Importer depuis SillyTavern](../books/managing-books.md#importing-from-sillytavern). Le convertisseur sert à l'inverse : vous gardez la
conversation à côté de vous et vous réécrivez l'histoire, avec le modèle, à partir d'instructions tirées de la conversation.

!!!
Le convertisseur fonctionne aussi dans la [disposition téléphone](../phone.md#extensions), voir [Sur téléphone](#on-a-phone).
!!!

<a id="the-converter-tab"></a>

## L'onglet Convertisseur

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **Convertisseur** (petit texte, et l'onglet défile
lorsque la barre latérale est basse). Au-dessus de ses deux listes se trouve la section **Convertir en détails de tour**, fermée tant que vous ne l'ouvrez pas afin que
les listes gardent de la place. Elle contient :

| | |
|---|---|
| **Modèle** et **Protocole** | Ce avec quoi la baguette fonctionne. Ils reprennent d'abord le modèle et le protocole du livre ; le choix est conservé avec le livre. |
| **Du**, **Au** | La plage de messages sur laquelle travaille la baguette. Saisissez les numéros, cliquez dans la conversation, ou utilisez **Toutes**. |
| Nombre de jetons | Le nombre de jetons que prend la requête : les messages de la plage plus l'instruction et le lore (comptés une seule fois). Utilisez-le pour garder la plage dans le contexte du modèle. |
| **Convertir la plage** | La baguette : demande au modèle les détails de tour de la plage. Elle devient **Arrêter** pendant que le modèle travaille. |
| Bouton crayon | Modifie l'instruction de la baguette (variables `{{backgroundLore}}`, `{{chat}}`, `{{firstMessage}}`, `{{lastMessage}}`, `{{previousTurn}}`), avec un bouton **Par défaut**. |
| **Aligner sur les tours** | Déplace la plage vers les limites des tours, voir ci-dessous. Conservé avec le livre. |
| **Mode automatique** | Affiche les commandes qui convertissent toute la plage par lots, voir ci-dessous. |

<a id="align-to-turns"></a>

### Aligner sur les tours

Un tour de l'histoire est constitué des messages de l'utilisateur (les instructions) suivis du message du personnage. Lorsque **Aligner sur les tours** est coché, chaque plage que vous choisissez (en cliquant, en saisissant les numéros ou avec **Toutes**) est déplacée vers les limites des tours :
le début revient au premier message de son tour, et la fin revient au dernier message du personnage qui précède, de sorte que la plage ne se termine jamais sur un message de l'utilisateur appartenant au tour suivant. La fin de la conversation reste à sa place. Une plage qui se situe entièrement dans les messages de l'utilisateur avant une réponse est prolongée jusqu'à cette réponse.

<a id="auto-mode"></a>

### Mode automatique

**Mode automatique** affiche une ligne avec **Messages par lot**, le bouton **Convertir par lots** et le nombre de requêtes que cela fait, avec les jetons qu'elles prennent au total. Le bouton découpe la plage courante en lots de ce nombre de messages et les envoie au modèle l'un après l'autre (après confirmation), comme le fait la baguette pour une seule plage. Avec **Aligner sur les tours**, chaque lot se termine à la fin d'un tour (le dernier message du personnage dans la taille, ou la fin du tour lorsqu'il est plus long que la taille), de sorte qu'aucun tour n'est coupé en deux ; sans cette option, les lots sont coupés exactement selon le nombre, et un tour à la frontière peut être scindé en deux.

Pendant son exécution, la conversation affiche le lot en cours, la ligne d'état indique quel lot sur combien, et le bouton devient **Arrêter**. Les détails de tour de chaque lot sont enregistrés dès qu'il est terminé, comme avec la baguette (les tours qui touchent la plage et que vous n'avez pas cochés sont remplacés). Lorsque tous les lots sont terminés, la plage est telle que vous l'aviez, et l'onglet **Détails de tour** s'ouvre. Un lot dont la réponse ne peut pas être utilisée (vide, ou ne correspondant pas à ce qui était demandé) est renvoyé, jusqu'à trois fois au total. Lorsque vous arrêtez, ou qu'un lot échoue encore, la notification indique la raison et la plage est ce qui reste, de sorte que vous puissiez reprendre à partir de là.

<a id="what-a-range-continues-from"></a>

### Ce que continue une plage

L'instruction indique au modèle les détails du dernier tour qui se termine avant la plage (son cadre de scène, le personnage POV, les personnages présents et les instructions, dans `{{previousTurn}}`), afin que les tours de la plage gardent le même point de vue et la même scène jusqu'à ce que la conversation les modifie. C'est ainsi qu'un lot du mode automatique continue à partir du précédent : les détails de tour d'un lot sont enregistrés avant l'envoi du suivant. Lorsqu'il n'y a pas de tour avant la plage, cette partie de l'instruction est omise.

<a id="the-chat"></a>

## La conversation

Importez le fichier `.jsonl` d'une conversation (dans SillyTavern il se trouve dans `data/<user>/chats/<character>/`, ou exportez-le depuis la
conversation), ou une conversation de LM Studio (`<number>.conversation.json` dans le dossier `.lmstudio/conversations` de votre dossier personnel). Les
deux se distinguent par leur contenu. La conversation ne doit pas avoir de branches : de chaque message, on lit le swipe affiché (dans LM Studio, la version
affichée), et les messages système cachés ainsi que les messages vides sont omis ; le prompt système d'une conversation LM Studio, sa réflexion
et les appels d'outils ne sont pas des messages de la conversation. Si votre conversation a des branches, importez d'abord celle que vous voulez réécrire (voir
[Importer depuis SillyTavern](../books/managing-books.md#importing-from-sillytavern) pour fusionner des branches en un arbre).

L'onglet **Conversation** est un tableau des messages : une case à cocher, le numéro et le message, avec le nom de l'auteur en gras au-dessus du
texte (les messages de l'utilisateur sont marqués d'un `»`).

- Cochez la case d'un message que vous avez parcouru, pour voir ce qui reste. Le bouton à double flèche
  au-dessus du tableau défile jusqu'au prochain message non coché (après celui que vous avez coché ou cliqué en dernier, en repartant
  du haut à la fin).
- **Cliquez** sur un message pour en faire la plage, **Maj+clic** sur un autre pour étendre la plage jusqu'à lui. La plage est surlignée et le
  nombre de jetons suit.
- Le bouton avec la croix au-dessus du tableau retire la conversation et les détails de tour du livre (après confirmation).

![L'onglet Convertisseur avec une conversation et la plage choisie](../../images/plugin-stconverter-chat.png)

<a id="turn-details"></a>

## Détails de tour

Utilisez la baguette, et l'onglet **Détails de tour** s'ouvre avec ce que le modèle a préparé, dans l'ordre de la conversation. Une ligne est un tour :
les messages dont il est fait et, dans un seul élément, le cadre de la scène, le personnage POV, les personnages présents et
les instructions, chacun sous une ligne en gras avec son nom.
Le modèle reçoit pour consigne de couvrir chaque message de la plage exactement une fois ; les numéros en dehors de la plage sont ramenés à celle-ci.

- **Cliquez sur une ligne** pour ouvrir le formulaire de *nouveau tour* de l'histoire et copier les quatre champs du tour dans celui-ci. Modifiez ce que vous voulez et générez. (La case à cocher et le bouton de suppression d'une ligne n'ouvrent pas le formulaire.)
- Cochez la case d'un tour que vous avez utilisé ; la double flèche passe au prochain tour non coché.
- Le bouton de fusion d'un tour **le fusionne avec le suivant** : un formulaire s'ouvre avec les détails des deux déjà réunis (les cadres de scène et les instructions l'un sous l'autre, les personnages présents des deux, le personnage POV du premier ; lorsque le second tour en a un autre, le formulaire l'indique). Modifiez ce qui ne convient pas et appuyez sur **Fusionner**
  (ou <kbd>Ctrl/Cmd+Entrée</kbd>) : les deux lignes deviennent une seule, qui couvre les messages des deux, cochée uniquement si les deux l'étaient.
- Les flèches d'un tour le **déplacent vers le haut ou vers le bas** dans la liste, pour écrire les tours dans un autre ordre que celui de la conversation (un tour
  garde les messages qu'il couvre). Les tours faits à partir des parties d'un livre ne peuvent pas être déplacés, leur ordre est celui des parties.
- Relancer la baguette pour une plage **remplace** les tours précédents qui touchent la plage, sauf les cochés ; les nouveaux tours prennent leur place dans l'ordre de la conversation, et l'ordre que vous avez donné en déplaçant des tours est conservé.

![Les détails de tour préparés par le modèle](../../images/plugin-stconverter-turns.png)
<a id="redrafting-a-book"></a>

## Refaire un livre

Pour réécrire un livre partie par partie, chargez l'ancien comme source. L'onglet affiche **Upload a chat**, **Load a backup** et **Pick a book** tant qu'il n'a pas de source :

- **Pick a book** ouvre la liste de vos livres (sauf celui dans lequel vous êtes). Les parties de la branche sur laquelle se trouve le livre sont chargées, de la première partie jusqu'à celle où en est le livre actuellement.
- **Load a backup** prend un fichier de sauvegarde d'un livre : le JSON, le `.zip` avec les images, ou un fichier que vous avez téléchargé depuis les [sauvegardes](../books/backups.md) d'un livre. La sauvegarde est seulement lue, rien n'est restauré. La branche est celle sur laquelle se trouvait le livre au moment de la sauvegarde.

De chaque partie, le convertisseur ne garde que les instructions, le cadre de la scène, le personnage POV, les personnages présents et le texte de la partie, rien d'autre (ni résumés, ni lorebook, ni raisonnement, ni images, ni autres branches, ni paramètres). Ainsi, un long livre reste léger. Un livre sans parties ne charge rien, et une sauvegarde dont les parties ne remontent pas jusqu'à la première partie est refusée comme endommagée.

L'onglet contient alors deux listes, comme pour un chat mais sans la baguette (le livre a déjà ses instructions, donc la section **Convert to turn details** n'est pas là) :

- **Parties** contient le texte de chaque partie, avec la même case à cocher et la flèche vers la prochaine partie non cochée. Lisez l'ancien texte à côté de l'histoire que vous écrivez.
- **Turn details** contient une ligne par partie, avec son cadre de scène, son personnage POV, ses personnages présents et ses instructions. **Cliquez sur une ligne** pour les placer dans le nouveau formulaire tour de l'histoire, comme pour un chat, puis générez la nouvelle partie. Cochez les tours que vous avez faits. Deux tours peuvent être **fusionnés** comme pour un chat (le bouton de fusion du premier), pour écrire deux parties de l'ancien livre en une seule ; les tours fusionnés sont conservés avec le livre.

Une seule source est conservée par livre. Pour en charger une autre, retirez la source actuelle avec la croix (après confirmation) ; cela oublie aussi les cases cochées faites sur elle.

<a id="on-a-phone"></a>

## Sur téléphone

Dans la disposition pour téléphone, l'onglet **Converter** se trouve dans le tiroir de l'histoire, à côté du plan et des autres onglets des extensions (ouvrez le tiroir avec le bouton en haut à gauche de l'histoire). Tout ce qui précède fonctionne de la même façon, avec ces différences :

- Il n'y a pas de touche Maj, donc la **plage se choisit par deux appuis** : appuyez sur un message pour commencer la plage, appuyez sur un autre pour la terminer (la plage va de l'un à l'autre, dans l'ordre qu'on veut) ; l'appui suivant commence une nouvelle plage. Vous pouvez aussi saisir **Du** et **Au** ou utiliser **Toutes**.
- Les détails des tours n'ont pas de colonne pour les numéros des messages : ils figurent sur la première ligne de chaque tour (**Msgs**).
- **Appuyez sur un tour** et le tiroir se ferme, puis le nouveau formulaire tour de l'histoire s'ouvre avec les quatre champs remplis.
- L'onglet est plus facile à utiliser en mode paysage ; les boîtes de dialogue (l'invite de la baguette, le choix d'un livre) tiennent à l'écran.

<a id="storage"></a>

## Stockage

Le fichier envoyé est enregistré comme une [ressource](../resources.md) à vous, liée au livre (un chat de SillyTavern peut peser plusieurs mégaoctets, avec tout ce que SillyTavern ajoute aux messages). Les parties d'un livre sont enregistrées de la même façon, sous forme de petit fichier JSON (`<book name>.redraft.json`) contenant les instructions et le texte ; le livre ne conserve que les cases cochées. Les cases cochées, les détails des tours, le modèle et l'invite sont stockés dans les attributs du livre, donc ils figurent dans les [sauvegardes de livres](../books/backups.md) et les copies.

!!!warning
Le fichier du chat ne figure pas dans les sauvegardes de livres, et une copie d'un livre pointe vers le même fichier. Après une restauration, les cases cochées et les détails des tours sont là, mais le convertisseur demande de nouveau le chat (envoyez le même fichier ; cela remplace les détails des tours et les cases cochées). Il en va de même pour les parties d'un livre : chargez de nouveau le livre ou la sauvegarde, puis recochez. Retirer la source avec la croix supprime la ressource.
!!!
