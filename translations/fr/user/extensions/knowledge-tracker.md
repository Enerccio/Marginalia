---
label: Tracker de connaissances
order: 11
---

# Tracker de connaissances

Garde la trace de ce que les personnages de votre histoire savent, et de ce qu'ils ne savent **pas**, et l'intègre au prompt. Un modèle
tend à faire connaître à un personnage des choses sur des gens qu'il n'a jamais rencontrés (leur nom, leur métier, leur visage), à moins que les
instructions ne l'en empêchent ; avec le Tracker de connaissances, le prompt indique, pour chaque personnage suivi dans la scène,
ce que ce personnage sait et qu'il ne sait rien d'autre.

!!!
Vous choisissez qui est suivi. Seuls les personnages que vous ajoutez à l'onglet sont décrits au modèle, et uniquement tant qu'ils font partie des *personnages présents* de la partie en cours d'écriture. Rien n'est deviné à partir du texte de l'histoire.
!!!

<a id="the-knowledge-tab"></a>

## L'onglet Connaissances

La barre latérale gauche de l'[éditeur de l'histoire](../books/story-editor.md) affiche un onglet **Connaissances** à côté de *Plan*. Tout est enregistré dans le livre au fur et à mesure de vos modifications.

![L'onglet Connaissances](../../images/plugin-knowledgetracker.png)

**Suivre un personnage** demande un **nom** et, facultativement, des **alias** : les autres noms sous lesquels le personnage est écrit dans le champ
*Personnages présents* d'une partie, séparés par des virgules. Un nom ou un alias ne peut appartenir qu'à un seul personnage suivi.

Ouvrez un personnage avec la flèche de sa carte pour voir ce qu'il sait :

| | |
|---|---|
| **Sait** (vert) / **Ne sait pas** (rouge) | Une courte phrase sur n'importe quoi : une personne, un lieu, un secret, un événement (*Peter sait qu'un complot se trame contre le duc*, *Fiora ne sait pas que Thomas est son père*). Écrivez explicitement ce qu'un personnage ne sait **pas**, car c'est ce que le modèle respecte. |
| L'ordre et **#12** | Les connaissances les plus récentes sont en haut (les anciennes aussi). Un élément appris par le modèle se termine par le petit numéro de la partie où il a été appris (**#12**) : un clic fait défiler l'histoire jusqu'à cette partie et la fait clignoter (sur téléphone, le tiroir se ferme d'abord). Les éléments que vous avez écrits n'ont pas de numéro, pas plus que ceux dont la partie n'est pas sur la branche active (une autre branche, ou la partie a été supprimée). |
| **Ajouter une connaissance** | Ajoute un élément, actif immédiatement. Limitez-le à **une seule idée** : un élément qui dit deux choses (*ne sait pas qui est Thomas, ne l'a jamais rencontré*) est retiré dès que l'une des deux cesse d'être vraie. Le bouton **modifier** change le texte et le type. |
| **Retirer** (le cercle barré) | L'élément n'est plus vrai (*n'a pas rencontré Thomas* après leur rencontre). Il n'est pas supprimé : il devient une *connaissance ancienne* et n'est plus envoyé au modèle. |
| **Verrouiller** (le cadenas, sur les éléments appris par le modèle) | Le [passage de nettoyage](#tidying-the-knowledge) ne voit jamais un élément verrouillé : utilisez-le pour ce qui doit rester, une trahison, un secret. L'histoire peut tout de même contredire un élément verrouillé (un personnage qui perd la mémoire) : la lecture de l'histoire le retire comme d'habitude. |
| **Supprimer** | Supprime définitivement un élément, après une question. Retirer le conserve, donc utilisez-le en cas de doute. |
| **Sélectionner des éléments** (à côté de **Ajouter une connaissance**) | Supprime rapidement plusieurs éléments : chaque élément du personnage, anciens compris, reçoit une case à cocher. **Tout sélectionner** / **Tout désélectionner** coche ou décoche tout, **Supprimer la sélection (n)** supprime définitivement les éléments cochés après une question, **Annuler** quitte la sélection. |
| **A rencontré** | Les personnes que le personnage a rencontrées, notées par le [traitement](#processing). Le traitement peut noter un nom qui n'est pas une personne (une note dans le champ des personnages présents) : ouvrez **Modifier** et changez la liste **A rencontré**, un nom après l'autre séparés par des virgules. Les noms que vous écrivez restent lorsque l'histoire est relue ; un nom que vous supprimez ne revient que si la partie où il a été vu est relue à nouveau. |
| **Anciennes connaissances (n)** | Repliées par défaut. Chaque ancien élément peut être **copié**, **modifié**, **restauré** (il redevient actif, à partir de maintenant) ou supprimé. |

<a id="mass-changes"></a>

### Modifications en masse

Le bouton **Modifications en masse** (l'icône de liste à côté de **Suivre un personnage**) ouvre une fenêtre avec chaque personnage suivi et
tous ses éléments, anciens compris, dans une grille avec des cases à cocher : le type, le texte, l'état (*Actif*, ou ancien : *retiré*,
*refusé lors d'une relecture*, *nettoyé*), l'origine (*État initial*, *Écrit à la main*, *Appris de l'histoire*) et la
partie où il a été appris. Cochez des éléments dans autant de grilles que vous voulez (la case dans l'en-tête d'une grille coche tous ses éléments, **Tout sélectionner** / **Tout désélectionner** dans la barre du bas concerne tous les éléments de tous les personnages), puis :

| | |
|---|---|
| **Retirer la sélection (n)** | Les éléments actifs cochés deviennent d'anciennes connaissances, comme le fait **Retirer** pour un seul. |
| **Restaurer la sélection (n)** | Les anciens éléments cochés redeviennent actifs, à partir de maintenant. |
| **Supprimer la sélection (n)** | Supprime définitivement les éléments cochés, après une question. |

Les compteurs sur les boutons indiquent combien d'éléments cochés chacun modifie. Les cases restent cochées après une modification, afin que les
éléments retirés puissent ensuite être supprimés.

**Exporter en JSON** (en haut de la fenêtre) télécharge les personnages suivis du livre (noms, alias, éléments avec leur type, leur état, leur origine et leur verrouillage, les personnes rencontrées) sous forme de fichier ; rien du traitement n'y figure, donc il convient à tout livre.
**Importer depuis JSON** lit un tel fichier dans le livre et ne fait qu'ajouter : un personnage dont le nom est déjà suivi par le livre (comme nom ou alias) reçoit les alias, éléments et personnes rencontrées qui lui manquent (un élément de même type et même texte est ignoré), les autres deviennent de nouveaux personnages suivis ; un alias déjà attribué à un autre personnage est omis. Un élément importé n'a derrière lui aucune partie de ce livre, donc un nouveau traitement ne le supprime jamais : celui que le modèle a appris compte comme écrit à la main, un ancien reste ancien. Le message affiché après l'importation indique combien de personnages ont été ajoutés ou mis à jour, et combien d'éléments ont été ajoutés ou existaient déjà.

<a id="what-goes-into-the-prompt"></a>

## Ce qui entre dans le prompt

Pour chaque personnage suivi qui fait partie des personnages présents, le prompt reçoit un bloc de ce genre :

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Seuls les éléments actifs sont envoyés. La dernière ligne (la ligne **monde fermé**) empêche le modèle de donner à un personnage une connaissance qu'il ne peut pas avoir ; elle couvre ce que l'histoire raconte (personnes, lieux, secrets, projets, événements), et non ce qu'une personne de ce monde sait de toute façon, si bien qu'un personnage connaît toujours sa propre ville. Vous pouvez désactiver cette ligne dans les paramètres. Un personnage suivi qui n'est pas présent ne reçoit absolument rien, pas même une ligne indiquant qu'il ne sait pas.

<a id="who-is-present"></a>

### Qui est présent

Le bloc est construit à partir du champ **Personnages présents** de la partie. Ce champ est en texte libre, il est donc lu de manière souple :

- le texte entre crochets est ignoré : `Tomáš (tired), Mizuki` désigne Tomáš et Mizuki ;
- les noms sont découpés aux **séparateurs** (par défaut : `,` `;` un saut de ligne, `/`, `|`, `、` `，` `；`) ;
- ce qui suit ` - ` ou `:` est une note et est écarté (`Mara - wounded`) ;
- la casse, les accents et un *the* initial n'ont pas d'importance ;
- un nom doit correspondre **en entier** : *le frère de Mara* n'est pas *Mara*. Utilisez les alias lorsqu'un personnage est écrit de plusieurs façons. L'idée est qu'une correspondance manquée coûte un bloc, alors qu'une fausse correspondance décrirait un personnage qui n'est pas dans la scène.

Le champ *Point de vue* n'est pas lu seul, donc écrivez aussi le personnage au point de vue parmi les personnages présents.
<a id="processing"></a>

## Traitement

Le plugin suit l'histoire. **Traitement** lit les parties de la branche active dans l'ordre, à partir du **point de départ**, et
conserve un **curseur** (la dernière partie lue). Sans le modèle, il note qui a rencontré qui : un personnage suivi a rencontré
tous les noms présents dans le champ des personnages présents avec lui, et le bloc les liste. Avec le modèle, il trouve aussi ce que
les personnages **ont appris** (voir [Lire ce que les personnages apprennent](#reading-what-the-characters-learn)).

![Le panneau de traitement](../../images/plugin-knowledgetracker-processing.png)

Le panneau en haut de l'onglet indique où il en est. Il se recharge tout seul quand une nouvelle partie est définitive (après
l'annotation et la réécriture automatiques, si le livre en a) ; si vous ajoutez, modifiez ou supprimez des parties d'une autre
manière, appuyez sur le bouton **Actualiser** (les flèches à côté de l'engrenage) pour relire l'histoire. Il comprend :

- **Traiter maintenant** : lit les parties qui ne sont pas encore traitées.
- **Traiter la partie suivante** : ne lit que la première partie non encore traitée, pas à pas (avec le modèle : une seule requête pour cette partie, quelle que soit la valeur de *Parties par exécution*). Utilisez-le pour essayer les réglages ou pour voir ce que le modèle trouve pour une seule partie. La commande `knowledge-process` avec `parts` réglé sur 1 fait la même chose.
- **Point de départ** : la partie à partir de laquelle le traitement commence (*Début du livre* par défaut). Les parties qui le précèdent ne sont jamais lues. Le reculer relit les parties à partir du nouveau point de départ, l'avancer conserve ce qui a été lu.
- **Parties traitées** : les dernières parties lues. Une partie dont le texte **a changé depuis son traitement** (vous l'avez modifiée ou réécrite) est signalée ; rien n'est retraité automatiquement à cause de cela. Le bouton d'une ligne, **Retraiter à partir d'ici**, retire ce qui provient de cette partie et des suivantes (les éléments et les rétractations que vous avez saisis à la main sont conservés) et les relit.

Ce que le plugin fait de lui-même, lorsque **Traiter automatiquement les nouvelles parties** est activé (il est désactivé par défaut) :

- Avant le début d'une génération, les parties écrites depuis la dernière fois sont traitées, pour que le prompt sache qui a rencontré qui. Une partie qui est remplacée par un *Régénérer* ou par un swipe est laissée de côté. Avec le modèle, cela ne lit avec le modèle que lorsque **Demander avant d'appliquer** est désactivé (voir plus bas) ; sinon, il se contente de noter qui a rencontré qui, et ce que le modèle trouve est lancé depuis l'onglet, car une relecture ne vous est jamais imposée au milieu d'une génération.
- Lorsqu'une partie est **régénérée** et que le nouveau texte arrive, ce qui avait été appris à partir de l'ancien texte est retiré.
- Lorsque vous faites un **swipe** ou passez à une autre branche, le curseur revient à la dernière partie que les deux branches ont en commun.
<a id="reading-what-the-characters-learn"></a>

## Lecture de ce que les personnages apprennent

Cochez **Lire ce que les personnages apprennent avec le modèle** dans les réglages. **Traiter maintenant** demande alors au modèle, pour les prochaines parties de l'histoire, ce que chaque personnage suivi qui s'y trouve a **appris** : ce qu'il a vu, entendu, lu ou qu'on lui a dit. Il indique aussi les éléments que l'histoire **contredit** (*n'a jamais rencontré Thomas* alors qu'ils se rencontrent). La requête contient les parties (avec leur point de vue et les personnages présents), ce que chaque personnage suivi sait à présent et, selon votre choix, le manuel de l'univers ; le modèle reçoit pour consigne de laisser de côté ce qu'une personne de cet univers connaît de toute façon (qui est le roi) et ce qui découle du personnage lui-même (une succube sait qu'elle a une queue), et de ne rien signaler dont il n'est pas sûr. Un personnage suivi qui n'est présent dans aucune des parties n'est pas interrogé du tout.

Chaque exécution est une seule requête au modèle. Lorsque **Traiter maintenant** nécessiterait plus de trois requêtes (une longue portion d'histoire non encore traitée, ou **Parties par exécution** réglé bas), il vous indique d'abord combien de parties et de requêtes cela représente, ainsi qu'à peu près la quantité de texte qui passe par le modèle, et ne démarre qu'après votre confirmation. Chaque requête transporte aussi les instructions, le manuel de l'univers et la connaissance des personnages : le coût réel est donc supérieur au seul texte.

Lorsque **Demander avant d'appliquer** est désactivé, les modifications sont appliquées immédiatement, et chacune est inscrite dans le journal **Appliqué automatiquement**, dans la section Traitement de l'onglet : qui a obtenu ou perdu quoi, et à partir de quelle partie. Rien dans le journal n'est modifiable, mais chaque ligne possède un bouton **Annuler la modification** : un ajout est refusé (il reste dans *Ancienne connaissance*, comme un ajout refusé dans l'examen), un élément retiré redevient actif. Une ligne dont l'élément a été modifié, supprimé ou retiré à la main entre-temps est laissée telle quelle. Le journal conserve les 200 dernières modifications.

Lorsque **Demander avant d'appliquer** est désactivé, la nouvelle partie est lue dès qu'elle est définitive : à la fin de la génération, ou, si le livre dispose de l'annotation automatique (et de la réécriture) ou d'une traduction activée, à la fin de celle-ci, afin que le modèle lise le texte que vous conservez et travaille pendant que vous lisez. C'est une étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part). La génération suivante trouve alors la connaissance à jour et n'attend pas. Ce qui reste (une exécution interrompue, une fenêtre fermée) est lu avant la génération suivante, comme auparavant.

Pendant que le modèle travaille, un petit **panneau de progression** flotte au-dessus de la page. Il n'est **pas modal** : vous pouvez continuer à lire et à modifier l'histoire, et vous pouvez le **déplacer** en le saisissant par son titre. Le fermer le masque, le travail continue (**Annuler** l'arrête).

![Le panneau de progression](../../images/plugin-knowledgetracker-progress.png)

Pendant que le modèle répond, le panneau affiche **Ce que le modèle écrit** au fur et à mesure : d'abord son raisonnement, puis la réponse, afin qu'une longue attente sur un modèle lent ne se réduise pas à une barre vide. Une fois la requête terminée, le même texte reste sous **Ce que le modèle a pensé** (un bloc pour chaque requête de l'exécution, avec sa réponse), y compris lorsqu'il n'a **rien** proposé et qu'aucun examen ne s'ouvre : l'onglet conserve un bouton **Dernière exécution : ce que le modèle a pensé** qui rouvre le panneau, pour que vous voyiez pourquoi rien n'a été ajouté.

Lorsque le modèle a répondu, le panneau l'indique et affiche **Examiner les modifications** ; le même bouton se trouve dans le panneau de traitement de l'onglet (*3 modifications à examiner*). **L'examen ne s'ouvre jamais tout seul.** Il s'agit d'un tableau des modifications, regroupées par personnage :

![Examen des modifications](../../images/plugin-knowledgetracker-review.png)

Sous le tableau, **Ce que le modèle a pensé** (replié au départ) contient le raisonnement du modèle qui précède sa réponse, lorsque le modèle le transmet : il montre pourquoi une modification a été proposée, ou pourquoi une modification que vous attendiez manque, afin que vous puissiez juger la modification avant de l'appliquer. Un modèle qui ne raisonne pas n'affiche rien à cet endroit.

| | |
|---|---|
| La case à cocher | Indique si la modification est appliquée. Toutes sont cochées au départ ; chaque personnage dispose de **Tout cocher** / **Tout décocher**. |
| **Retirer** / **Ajouter** | Un retrait rend un élément ancien (il est conservé, voir [l'onglet](#the-knowledge-tab)). Un ajout est un nouvel élément, *Sait* ou *Ne sait pas*. Le texte et le type d'un ajout peuvent être **modifiés** avant d'être appliqués. |
| La citation | Les mots de la partie qui le montrent, et la partie dans laquelle ils figurent. Une citation introuvable dans cette partie est signalée : le modèle a pu l'inventer, vérifiez-la avant d'accepter. |

**Appliquer** applique les modifications cochées et passe à la suite. **Annuler** arrête toute la série et ne change rien : les mêmes parties seront proposées à nouveau la prochaine fois. Un ajout que vous décochez est **conservé comme ancienne connaissance** avec sa citation (vous pouvez le rétablir) ; un retrait non coché laisse l'élément tel quel. Ce que le modèle a dit et qui ne peut pas être utilisé (un personnage pour lequel il n'était pas interrogé, un élément absent, une modification sans citation, quelque chose que le personnage sait déjà) figure dans **Omise**. Ce qui a été appliqué porte sa partie, de sorte que **Reprendre à partir d'ici** le reprend. Si l'histoire a changé pendant votre examen (une autre branche, une partie supprimée), les modifications ne sont pas appliquées et vous relancez le traitement.

Les parties sont lues par **exécutions** de **Parties par exécution** (1 par défaut) ; une partie qui ne tient pas dans le contexte du modèle est tronquée à ce qui tient. Une série se poursuit, exécution après exécution, jusqu'à la dernière partie validée ; dans une série qui vous demande confirmation, une exécution où le modèle n'a rien trouvé est passée sans vous demander. Lorsque **Demander avant d'appliquer** est désactivé, les modifications sont appliquées immédiatement et le panneau conserve un journal avec une ligne par exécution.

!!!
L'instruction a été testée sur un modèle, sur quelques scènes seulement, et non de façon approfondie : elle empêche les personnages d'apprendre ce qu'ils n'ont pas perçu, mais vérifiez ce qui est proposé, surtout pour les personnages présents dans une longue scène. La requête est envoyée au modèle que vous choisissez ci-dessous, avec le coût et la confidentialité habituels de ce service.
!!!

<a id="tidying-the-knowledge"></a>

## Mise en ordre des connaissances

Le modèle note ce que chaque personnage apprend, et sur une longue histoire la liste s'allonge, avec plus de petites choses qu'on ne veut en envoyer au modèle à chaque requête. **Ranger les connaissances** (à côté de **Traiter maintenant**, nécessite le réglage du modèle) demande au modèle de relire ce qu'un personnage sait, avec les dernières parties traitées de l'histoire pour contexte, et de :

- **supprimer** un élément qui ne vaut pas la peine d'être gardé : détail sans importance, détail d'une scène que tous ceux qui s'y trouvaient ont vu (l'histoire le conserve), chose dépassée par des événements postérieurs, élément qui redit un autre ;
- **fusionner** plusieurs éléments portant sur la même chose en un seul qui conserve chaque fait.

Un élément supprimé ou fusionné n'est pas effacé : il devient *Ancienne connaissance* et peut être rétabli. Les modifications sont examinées, comme celles d'une lecture (**Demander avant d'appliquer** en décide), ou appliquées immédiatement et inscrites dans le journal avec un bouton **Annuler la modification**.

Ce que le passage peut toucher se limite à ce que le modèle a appris et que vous n'avez pas **verrouillé**. Les éléments initiaux, ceux que vous avez ajoutés ou modifiés à la main, les éléments verrouillés et les anciens ne sont jamais modifiés : les deux premiers types et les éléments verrouillés sont montrés au modèle afin qu'il voie ce qu'un élément ferait double emploi. Un secret qu'aucune partie n'a évoqué depuis longtemps n'est pas une raison de le supprimer, et l'instruction le précise ; lorsque le modèle hésite, il garde l'élément. Un personnage ayant moins de **Ranger : à partir de ce nombre d'éléments** (8) éléments appris par le modèle n'est pas rangé, et **Ranger : parties de contexte** (10) indique combien des dernières parties traitées sont lues.

**Ranger automatiquement** (un réglage, désactivé par défaut) lance le rangement de lui-même à la fin d'une exécution dont les modifications sont appliquées sans demander (**Demander avant d'appliquer** désactivé, donc la lecture automatique des nouvelles parties également), une fois que le modèle a lu **Ranger après ce nombre de parties** (10) parties depuis le dernier rangement. Ce qu'il modifie est appliqué et inscrit dans le journal avec un bouton **Annuler la modification**, et le journal de l'exécution le signale. Une exécution que vous examinez ne lance jamais de rangement de lui-même, et un personnage ayant trop peu d'éléments est passé sans que le modèle soit interrogé.

Les mêmes actions depuis la barre de commandes : `knowledge-tidy` (`ask true` pour obtenir les modifications sous forme de liste ; l'onglet ouvert affiche alors le bouton d'examen), et `lock-knowledge` / `unlock-knowledge` avec le personnage et le numéro de l'élément.
<a id="settings"></a>

## Paramètres

L'engrenage à côté de *Track a character* :

| Paramètre | |
|---|---|
| **In prompt** | Insère le bloc dans le prompt de chaque génération. Le bouton devient vert lorsqu'il est activé. |
| **Depth** | Où il se place, compté en messages depuis la fin du prompt : `0` après les instructions du tour, `1` juste avant elles. Il ne passe jamais avant le prompt système. |
| **Role** | Envoyé comme message *System*, *Assistant* ou *User*. |
| **Say that nothing else is known** | La ligne de monde fermé. |
| **Give what the characters know to the annotation** | Activé (par défaut) : [Annoter avec l'IA](#annotating-with-what-the-characters-know) reçoit ce que les personnages suivis connaissaient au début de la partie. |
| **Process new parts automatically** | Voir [Traitement](#processing). |
| **Read what the characters learn with the model** | Le traitement demande au modèle ce que chaque personnage suivi a appris et propose le résultat. Désactivé (par défaut) : le traitement note seulement qui a rencontré qui, ce qui ne coûte rien. |
| **Ask before applying** | Activé (par défaut) : chaque proposition est présentée pour relecture. Désactivé : elle est appliquée immédiatement et consignée. |
| **Let the model read earlier parts** | Pendant la lecture des nouvelles parties, le modèle peut lire la partie dans laquelle un élément a été appris (les éléments indiquent « appris dans la partie 12 ») afin de vérifier si ce qu'il lit confirme, répète ou contredit cet élément : un ouï-dire qui est désormais vu de ses propres yeux, par exemple. Au plus 5 lectures, chacune d'une partie différente, par requête ; le texte d'une partie longue est tronqué à ce qui tient dans le contexte, et la relecture indique les parties lues. Chaque lecture est un aller-retour de plus avec le modèle, c'est pourquoi l'option est désactivée par défaut. Un modèle incapable d'appeler des fonctions lit la requête comme auparavant. |
| **Parts per run** | Le nombre de parties lues par une requête. Plus grand, c'est moins cher, mais la réponse est plus longue et la relecture plus difficile. |
| **Tidy automatically**, **Tidy after this many parts** | La passe de nettoyage se lance d'elle-même à la fin d'un traitement qui applique ses modifications sans demander, toutes les ce nombre de parties (10). Désactivé par défaut. |
| **Tidy: parts of context** | Le nombre de dernières parties traitées que la passe de nettoyage lit (10). |
| **Tidy: from this many items** | Un personnage qui a moins d'éléments appris par le modèle (et qui ne sont pas verrouillés) n'est pas nettoyé (8). |
| **Lorebook for the model** | *Entries the parts activate* (par défaut : les entrées qu'une génération utiliserait pour ces parties), *The whole lorebook* (toutes les entrées ; si elles ne tiennent pas dans le contexte, seules les entrées activées sont envoyées et la note le signale) ou *No lorebook*. |
| **Model / Protocol for the extraction** | Vide : le modèle du livre. Un modèle moins cher suffit pour cela, et sur un serveur qui ne conserve qu'un seul cache de prompt, un modèle séparé l'empêche d'être remplacé. |
| **Separators of this book** | La manière dont le champ des personnages présents est découpé en noms, un par ligne (un saut de ligne est `\n`). Un mot tel que `and` découpe entre les espaces, de sorte que *Jack and Jill* reste un seul nom tant que vous ne l'ajoutez pas. |
| **My separators (all books)** | Identique, pour tous vos livres. Les séparateurs propres au livre l'emportent ; sans l'un ni l'autre, les séparateurs intégrés sont utilisés. |

## Prompts

Le message système de la lecture (**Knowledge extraction prompt**) et celui de la passe de nettoyage (**Knowledge tidy prompt**) sont des
[paramètres hérités](../books/prompts.md#inherited-settings) : un livre utilise les siens, sinon le dossier le plus proche au-dessus de lui qui en définit un,
sinon les vôtres, sinon le texte intégré, testé avec un modèle réel.

Définissez-les dans l'onglet *Prompts* du livre, sous *Extension Settings*, dans la boîte de dialogue d'un dossier, ou pour tous vos livres dans
*Settings → Extension Settings → Knowledge Tracker* (vide affiche le texte intégré en gris). Conservez le format de réponse JSON du
texte intégré : l'extension lit la réponse grâce à lui. Les instructions pour `read_part` sont ajoutées par l'extension lorsque le modèle peut lire
des parties antérieures ; votre prompt n'a donc pas besoin de les contenir.

<a id="annotating-with-what-the-characters-know"></a>

## Annoter avec ce que savent les personnages

Le bloc ci-dessus empêche le modèle de rendre un personnage omniscient *pendant qu'il écrit*. Les mêmes enregistrements aident aussi à repérer les erreurs qui
se sont glissées malgré tout. Lorsqu'un modèle [annote une partie](../books/annotations.md#letting-a-model-annotate) (**Annoter avec l'IA**, une plage de parties, l'annotation
automatique, `/generate-annotation`), le suivi lui indique ce que les personnages suivis de cette partie savaient **au début de la partie**,
et l'éditeur signale un personnage qui dit, pense ou fait ce qu'il ne peut pas savoir : *Fiora salue Thomas par son nom ; les enregistrements disent
qu'elle ne l'a jamais rencontré.* La note nomme le fait.

- **Les faits sont ceux de la partie, pas ceux de la fin du livre.** Ce qu'un personnage apprend dans une partie ultérieure n'est pas encore connu ; un élément qu'une
  partie ultérieure retire était encore vrai. Ce que la partie apporte elle-même (ce qui est appris, qui est rencontré, ce qui cesse d'être vrai en elle) est
  listé à part, afin que la première moitié de la partie ne soit pas vérifiée par rapport à la seconde.
- **Quels personnages.** Les personnages suivis qui figurent dans les *Personnages présents* de la partie, qui en sont le point de vue, ou qui sont nommés dans son
  texte (un mot entier, quelle que soit la casse, alias compris).
- **Les éléments qui ne sont jamais des faits** sont laissés de côté : ceux que vous avez retirés à la main, ceux que la passe de nettoyage a supprimés, ceux que vous avez refusés
  lors d'une relecture.
- **Seulement ce qui est enregistré.** Le modèle est informé que ce que les enregistrements ne disent pas n'est pas un fait, afin qu'il ne signale pas un personnage pour une chose
  que le suivi n'a tout simplement jamais sue. *Say that nothing else is known* ajoute aussi « (nobody else) » à la liste des personnes rencontrées par un personnage.
- Si une partie antérieure à celle relue n'a pas été lue par le suivi, les faits le signalent : ce que les personnages y ont appris manque, et
  **Traiter maintenant** d'abord donne une meilleure relecture.
- Les faits coûtent des jetons dans la requête d'annotation, uniquement pour les parties qui ont un personnage suivi. Désactivez-les avec **Give what
  the characters know to the annotation**.

<a id="the-block-as-a-macro"></a>

## Le bloc sous forme de macro

Au lieu de l'insertion automatique (ou en plus de celle-ci), vous pouvez placer le bloc vous-même avec la
[macro {%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). L'argument est la liste des
personnages de la scène ; un modèle peut donc transmettre le champ de la partie :

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

La macro affiche le bloc, que **In prompt** soit activé ou non ; désactivez-le donc lorsque vous utilisez la macro, sans quoi le modèle le reçoit deux fois. La profondeur et le rôle ne s'appliquent pas à la macro (le bloc fait partie du message qui la contient). Elle n'affiche rien
lorsqu'aucun des personnages listés n'est suivi. La réponse d'une macro de commande est conservée avec la partie à laquelle la nouvelle partie fait suite,
si bien qu'une seconde génération à partir de la même partie ne voit pas une modification que vous auriez faite entre-temps ; une régénération et un swipe demandent de nouveau la réponse.

<a id="commands"></a>

## Commandes

Pour la [ligne de commande](../commands/index.md) et pour les agents :

| Commande | |
|---|---|
| `knowledge-status` | Les paramètres et l'état du traitement. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Les personnages suivis, désignés par leur nom, leur alias ou leur numéro. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Les éléments d'un personnage, désignés par leur numéro dans `list-knowledge`. |
| `knowledge-process` | Traite maintenant (`parts` : au plus ce nombre de parties). Le modèle étant activé, `ask true` renvoie ce que le modèle a trouvé sous forme de modifications numérotées et les retient : rien n'est encore modifié. L'onglet Knowledge ouvert affiche un bouton **changes found by a command** qui ouvre la relecture habituelle (Apply / Cancel) ; un agent les applique avec `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Applique les modifications retenues, toutes ou celles dont les numéros sont donnés (identique à **Apply** dans la relecture de l'onglet). |
| `knowledge-tidy` | La passe de nettoyage. Avec `ask true`, elle renvoie les modifications sous forme de liste numérotée et les retient (l'onglet affiche un bouton pour les relire ; `apply-knowledge-changes` les applique). |
| `lock-knowledge`, `unlock-knowledge` | Verrouille un élément (personnage, numéro d'élément) afin que la passe de nettoyage n'y touche pas, ou le déverrouille. |
| `knowledge-reprocess` | Retraite à partir d'une partie (son identifiant ou sa position) ; demande confirmation d'abord. Le modèle étant activé, il ne fait que rétablir l'état, `knowledge-process` relit les parties. |
| `knowledge-set-start` | Définit le point de départ ; sans partie, le début du livre. |
| `knowledge-tracker-block` | Le bloc sous forme de texte, pour la macro. |

La ligne de commande découpe ses arguments aux espaces : placez une liste de personnages entre guillemets (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Stockage

Tout est enregistré avec le livre (dans ses attributs), donc cela fait partie des [sauvegardes du livre](../books/backups.md) et des copies.
