---
label: Suivi des connaissances
order: 11
---

# Suivi des connaissances

Garde la trace de ce que les personnages de votre histoire savent, et de ce qu'ils ne savent **pas**, et l'intègre dans le prompt. Un modèle a tendance à faire savoir à un personnage des choses sur des gens qu'il n'a jamais rencontrés (leur nom, leur métier, leur visage), sauf si les instructions l'en empêchent. Avec le Knowledge Tracker, le prompt indique, pour chaque personnage suivi dans la scène, ce que ce personnage sait, et qu'il ne sait rien d'autre.

!!!
Vous choisissez qui est suivi. Seuls les personnages que vous ajoutez à l'onglet sont décrits au modèle, et seulement lorsqu'ils font partie des personnages présents (*Present Characters*) de la partie en cours d'écriture. Rien n'est deviné à partir du texte de l'histoire.
!!!

<a id="the-knowledge-tab"></a>

## L'onglet Knowledge

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **Knowledge**, à côté de *Outline*. Tout est enregistré dans le livre au fur et à mesure que vous le modifiez.

![L'onglet Knowledge](../../images/plugin-knowledgetracker.png)

**Track a character** demande un **nom** et, facultativement, des **aliases** : les autres noms sous lesquels le personnage est écrit dans le champ *Present Characters* d'une partie, séparés par des virgules. Un nom ou un alias ne peut appartenir qu'à un seul personnage suivi.

**Import from the Character Tracker** (l'icône de personne à côté de **Track a character**) commence à suivre des personnages du [Character Tracker](character-tracker.md) du même livre. Il liste les personnages que le livre ne suit pas encore (par nom ou alias), avec leurs accroches, tous cochés ; décochez ceux à exclure, puis appuyez sur **Import**. Seuls les noms sont repris : l'accroche et la note décrivent un personnage, elles ne sont pas quelque chose qu'il sait. Avec **Their relationships count as meetings**, un personnage importé a rencontré les personnes avec lesquelles il est en relation dans le Character Tracker (elles sont inscrites dans sa liste **Has met** comme si vous les aviez écrites, afin que le traitement les conserve).

**Who has met whom** (l'icône de poignée de main, à côté de l'import) définit d'un coup les rencontres entre personnages suivis, sans écrire de noms. Choisissez un personnage à gauche ; à droite, chaque autre personnage suivi a une case à cocher, cochée lorsque le personnage choisi l'a rencontré. Cochez pour ajouter la rencontre, décochez pour la retirer ; chaque modification est enregistrée aussitôt et les fiches l'affichent lorsque vous fermez la fenêtre. Une rencontre va dans un seul sens : cocher Thomas pour Fiora ne coche pas Fiora pour Thomas, sauf si **Mutual** est coché : chaque coche ou décoche est alors faite des deux côtés à la fois, ce qui est plus rapide lorsque les deux se connaissent déjà (cela s'applique aux modifications faites tant que l'option est cochée, et non aux rencontres déjà définies). Une rencontre cochée compte comme vôtre (le traitement la conserve) ; une rencontre que vous décochez, et que l'histoire affiche, revient si cette partie est de nouveau lue. Il faut au moins deux personnages suivis.

Ouvrez un personnage avec la flèche de sa fiche pour voir ce qu'il sait :

| | |
|---|---|
| **Knows** (vert) / **Does not know** (rouge) | Une courte phrase sur n'importe quel sujet : une personne, un lieu, un secret, un événement (*Peter knows about the plot against the duke*, *Fiora does not know that Thomas is her father*). Écrivez explicitement ce qu'un personnage ne **sait pas**, car c'est ce que le modèle respecte. |
| L'ordre et **#12** | Les connaissances les plus récentes sont en haut (les anciennes aussi). Un élément appris par le modèle se termine par le petit numéro de la partie où il a été appris (**#12**) : un clic fait défiler l'histoire jusqu'à cette partie et la fait clignoter (sur téléphone, cela ferme d'abord le tiroir). Les éléments que vous avez écrits n'ont pas de numéro, et il n'en a pas non plus un dont la partie ne fait pas partie de la branche active (une autre branche, ou une partie supprimée). |
| **Add knowledge** | Ajoute un élément, actif immédiatement. Gardez-le à **une seule idée** : un élément qui dit deux choses (*does not know who Thomas is, has never met him*) est retiré dès que l'une d'elles cesse d'être vraie. Le bouton **edit** modifie le texte et le type. |
| **Retract** (le cercle barré) | L'élément n'est plus vrai (*has not met Thomas* après leur rencontre). Il n'est pas supprimé : il devient une *old knowledge* (ancienne connaissance) et n'est plus envoyé au modèle. |
| **Lock** (le cadenas, sur les éléments appris par le modèle) | La [passe de nettoyage](#tidying-the-knowledge) ne voit jamais un élément verrouillé : utilisez-le pour ce qui doit rester, une trahison, un secret. L'histoire peut toutefois contredire un élément verrouillé (un personnage qui perd la mémoire) : la lecture de l'histoire le retire comme d'habitude. |
| **Delete** | Supprime définitivement un élément, après une confirmation. **Retract** le conserve, utilisez-le donc en cas de doute. |
| **Select items** (à côté de **Add knowledge**) | Supprime rapidement plusieurs éléments : chaque élément du personnage, anciens compris, reçoit une case à cocher. **Select all** / **Deselect all** coche ou décoche tout, **Delete selected (n)** supprime définitivement les éléments cochés après une confirmation, **Cancel** annule la sélection. |
| **Has met** | Les personnes que le personnage a rencontrées, notées par le [traitement](#processing). Le traitement peut noter un nom qui n'est pas une personne (une note dans le champ des personnages présents) : ouvrez **Edit** et modifiez la liste **Has met**, un nom après l'autre, séparés par des virgules. Les noms que vous écrivez restent lorsque l'histoire est relue ; un nom que vous retirez ne revient que si la partie où il a été vu est relue. |
| **Old knowledge (n)** | Replié par défaut. Chaque ancien élément indique pourquoi il est ancien : *retracted* (avec le numéro de la partie dont la lecture l'a retiré, lorsque le modèle l'a fait), *declined* lors d'une relecture, ou *tidied* (nettoyé). Il peut être **copié**, **modifié**, **restored** (il redevient actif, à partir de maintenant) ou supprimé. |

<a id="mass-changes"></a>

### Modifications en masse

Le bouton **Mass changes** (l'icône de liste, à côté de **Track a character**) ouvre une fenêtre avec chaque personnage suivi et tous ses éléments, anciens compris, dans une grille avec des cases à cocher : le type, le texte, l'état (*Active*, ou ancien : *retracted*, *declined in a review*, *tidied*), l'origine (*Initial state*, *Written by hand*, *Learned from the story*) et la partie où il a été appris. Cochez des éléments dans autant de grilles que vous le souhaitez (la case de l'en-tête d'une grille coche tous ses éléments ; **Select all** / **Deselect all** dans la barre du bas concernent tous les éléments de tous les personnages), puis :

| | |
|---|---|
| **Retract selected (n)** | Les éléments actifs cochés deviennent d'anciennes connaissances, comme le fait **Retract** pour un seul. |
| **Restore selected (n)** | Les anciens éléments cochés redeviennent actifs, à partir de maintenant. |
| **Delete selected (n)** | Supprime définitivement les éléments cochés, après une confirmation. |
| **Copy selected (n)** | Copie les éléments cochés, de tous les personnages, pour les coller chez un autre personnage. |
| **Paste (n)** (dans l'en-tête de chaque personnage) | Ajoute les éléments copiés à ce personnage. Un élément collé compte comme écrit à la main (un élément initial reste initial) et conserve son type, son verrou et son statut d'ancien élément ; un élément que le personnage possède déjà (même type et même texte) est ignoré. Les éléments copiés restent jusqu'à la copie suivante, y compris après la fermeture de la fenêtre, afin de pouvoir les coller chez plusieurs personnages. |

Les compteurs sur les boutons indiquent combien d'éléments cochés chacun modifie. Les coches restent après une modification, afin que les éléments retirés puissent ensuite être supprimés.

**Export to JSON** (en haut de la fenêtre) télécharge les personnages suivis du livre (noms, alias, éléments avec leur type, leur état, leur origine et leur verrou, les personnes rencontrées) sous forme de fichier ; rien du traitement n'y figure, il convient donc à n'importe quel livre. **Import from JSON** lit un tel fichier dans le livre et ne fait qu'ajouter : un personnage dont le nom est déjà suivi par le livre (en tant que nom ou alias) reçoit les alias, éléments et personnes rencontrées qui lui manquent (un élément de même type et même texte est ignoré) ; les autres deviennent de nouveaux personnages suivis ; un alias déjà attribué à un autre personnage est laissé de côté. Un élément importé n'a pas de partie de ce livre derrière lui, donc un nouveau traitement ne le supprime jamais : un élément appris par le modèle compte comme écrit à la main, et un ancien élément reste ancien. Le message affiché après l'import indique combien de personnages ont été ajoutés ou mis à jour, et combien d'éléments ont été ajoutés ou existaient déjà.
<a id="what-goes-into-the-prompt"></a>

## Ce qui entre dans le prompt

Pour chaque personnage suivi qui fait partie des personnages présents, le prompt reçoit un bloc de ce type :

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Seuls les éléments actifs sont envoyés. La dernière ligne (la ligne **monde fermé**) empêche le modèle de donner au personnage des connaissances qu'il ne peut pas avoir. Elle concerne ce que l'histoire révèle (les personnes, les lieux, les secrets, les projets, les événements), et non ce qu'une personne de ce monde sait de toute façon : un personnage connaît toujours sa propre ville. Vous pouvez désactiver cette ligne dans les paramètres. Un personnage suivi qui n'est pas présent n'obtient rien du tout, pas même une ligne indiquant qu'il ne sait pas.

<a id="who-is-present"></a>

### Qui est présent

Le bloc est construit à partir du champ **Present Characters** de la partie. Ce champ est un texte libre, donc il est lu de façon souple :

- le texte entre crochets est ignoré : `Tomáš (tired), Mizuki` vaut Tomáš et Mizuki ;
- les noms sont séparés aux **séparateurs** (par défaut : `,` `;` un saut de ligne, `/`, `|`, `、` `，` `；`) ;
- ce qui suit ` - ` ou `:` est une note et est ignoré (`Mara - wounded`) ;
- la casse, les accents et un *the* en début de nom n'ont pas d'importance ;
- un nom doit correspondre **en entier** : *Mara's brother* n'est pas *Mara*. Utilisez les alias lorsqu'un personnage est écrit de plusieurs façons. L'idée est qu'une correspondance manquée coûte un bloc, alors qu'une fausse correspondance décrirait un personnage qui n'est pas dans la scène.

Le champ *Point of View* n'est pas lu seul : écrivez donc aussi le personnage au point de vue parmi les personnages présents.

<a id="processing"></a>

## Traitement

Le plugin suit l'histoire. **Processing** lit dans l'ordre les parties de la branche active, à partir du **point de départ**, et conserve un **curseur** (la dernière partie lue). Sans le modèle, il note qui a rencontré qui : un personnage suivi a rencontré tous les noms présents dans le champ des personnages présents avec lui, et le bloc les liste. Avec le modèle, il trouve aussi ce que les personnages ont **appris** (voir [Lire ce que les personnages apprennent](#reading-what-the-characters-learn)).

![Le panneau de traitement](../../images/plugin-knowledgetracker-processing.png)

Le panneau en haut de l'onglet indique où en est le traitement. Il se recharge automatiquement lorsqu'une nouvelle partie est finalisée (après l'annotation automatique et la réécriture, si le livre les utilise). Si vous ajoutez, modifiez ou supprimez des parties autrement, appuyez sur le bouton **Refresh** (les flèches à côté de l'engrenage) pour relire l'histoire. Ce panneau comprend :

- **Process now** : lit les parties qui ne sont pas encore traitées.
- **Process next part** : ne lit que la première partie non traitée, pas à pas (avec le modèle : une seule requête pour cette partie, quelle que soit la valeur de *Parts per run*). Utilisez-le pour essayer les réglages ou pour voir ce que le modèle trouve dans une seule partie. La commande `knowledge-process` avec `parts` réglé sur 1 fait la même chose.
- **Start point** : la partie à partir de laquelle le traitement commence (*Start of the book* par défaut). Les parties situées avant ne sont jamais lues. Reculer ce point relit les parties à partir du nouveau point de départ ; l'avancer conserve ce qui a déjà été lu.
- **Processed parts** : les dernières parties lues. Une partie dont le texte a **changé depuis son traitement** (parce que vous l'avez modifiée ou réécrite) est signalée ; rien n'est retraité automatiquement pour cette raison. Le bouton de chaque ligne, **Reprocess from here**, retire ce qui provient de cette partie et des suivantes (les éléments et les rétractations que vous avez écrits à la main sont conservés), puis les relit.

Lorsque vous faites défiler la liste des personnages au-delà de ces boutons, **Process now**, **Process next part** et **Tidy knowledge** sont aussi proposés dans la barre en haut de l'onglet, pour traiter sans remonter.

Ce que le plugin fait de lui-même, lorsque **Process new parts automatically** est activé (il est désactivé par défaut) :

- Avant le début d'une génération, les parties écrites depuis la dernière fois sont traitées, pour que le prompt sache qui a rencontré qui. Une partie remplacée par un *Regenerate* ou un swipe est laissée de côté. Avec le modèle, cette lecture n'a lieu avec le modèle que si **Ask before applying** est désactivé ; sinon, le plugin se contente de noter qui a rencontré qui, et ce que le modèle trouve est lancé depuis l'onglet, car une relecture n'est jamais imposée au milieu d'une génération.
- Lorsqu'une partie est **régénérée** et que le nouveau texte arrive, ce qui avait été appris de l'ancien texte est retiré.
- Lorsque vous faites un **swipe** ou passez à une autre branche, le curseur revient à la dernière partie que les deux branches ont en commun.
<a id="reading-what-the-characters-learn"></a>

## Lire ce que les personnages apprennent

Cochez **Read what the characters learn with the model** dans les réglages. Ensuite, **Process now** demande au modèle, pour les parties suivantes de l'histoire, ce que chaque personnage suivi qui y est présent a **appris** : ce qu'il a vu, entendu, lu ou ce qu'on lui a dit. Il indique aussi quels éléments l'histoire **contredit** (*n'a jamais rencontré Thomas* alors qu'ils se rencontrent).

La requête contient les parties (avec leur point de vue et les personnages présents), ce que chaque personnage suivi sait à ce moment, et, si vous le choisissez, le lorebook. Le modèle reçoit pour consigne d'omettre ce que toute personne de ce monde sait de toute façon (qui est le roi) et ce qui découle du personnage lui-même (une succube sait qu'elle a une queue), et de ne rien signaler dont il n'est pas sûr. Un personnage suivi qui n'est présent dans aucune des parties n'est pas interrogé du tout.

Chaque exécution est une seule requête au modèle. Lorsque **Process now** nécessiterait plus de trois requêtes (une longue portion de l'histoire non encore traitée, ou **Parts per run** réglé bas), il indique d'abord combien de parties et de requêtes cela représente et à peu près la quantité de texte envoyée au modèle, et ne démarre qu'après votre confirmation. Chaque requête transporte aussi les instructions, le lorebook et les connaissances des personnages, si bien que le coût réel est supérieur à celui du seul texte.

Lorsque **Ask before applying** est désactivé, les modifications sont appliquées immédiatement, et chacune est inscrite dans le journal **Applied automatically** de la section Processing de l'onglet : qui a reçu quoi ou perdu quoi, et à partir de quelle partie. Rien dans le journal n'est modifiable, mais chaque ligne a un bouton **Revert** : un élément ajouté est refusé (il reste sous *Old knowledge*, comme une addition refusée dans la revue), et un élément retiré redevient actif. Une ligne dont l'élément a été modifié, supprimé ou retiré à la main depuis est laissée telle quelle. Le journal conserve les 200 dernières modifications.

<a id="returning-to-an-earlier-point"></a>

### Revenir à un point antérieur

Avant chaque exécution qui modifie les connaissances (une exécution de l'onglet avec ou sans revue, la lecture automatique des nouvelles parties, les commandes `knowledge-process` et `knowledge-tidy`, un nettoyage, et une lecture sans le modèle), le suiveur crée un **checkpoint** : l'état complet de ses personnages tel que l'exécution l'a trouvé, avec le point jusqu'auquel il avait lu (*Start of the book* avant la première exécution). **Checkpoints** dans le panneau Processing les liste, du plus récent au plus ancien : la partie jusqu'à laquelle le suiveur avait lu, l'heure, le type d'exécution qui a suivi et le nombre d'éléments ajoutés et retirés. Seuls les checkpoints dont la partie se trouve sur la branche active sont listés.

**Return to this point** sur une ligne annule cette exécution et tout ce qui a suivi : les personnages et leurs connaissances, les parties traitées et le journal des modifications automatiques sont ceux du checkpoint, et la prochaine exécution relit à nouveau les parties après sa partie, y compris celles de l'exécution annulée. Vos réglages et le point de départ restent tels qu'ils sont maintenant. Il demande d'abord confirmation, car tout ce qui a été appris après le checkpoint est annulé, y compris les modifications faites à la main depuis ; ce que le suiveur avait est conservé sous forme de checkpoint (*before a return to a checkpoint*), pour que vous puissiez y revenir. Utilisez-le lorsqu'une exécution ou une série d'exécutions a mal tourné, ou après avoir restauré une sauvegarde plus ancienne du livre et voulu que le suiveur rattrape son retard à partir d'un point de confiance.

Le suiveur conserve les 20 derniers checkpoints d'un livre. Ils sont conservés parmi vos fichiers (*Resources*, regroupés par livre), et non dans le livre, si bien que les sauvegardes et copies du livre ne les incluent pas.

Lorsque **Ask before applying** est désactivé, la nouvelle partie est lue dès qu'elle est définitive : à la fin de la génération, ou, si le livre a l'annotation automatique (et la réécriture) ou une traduction activée, lorsque celle-ci est terminée, de sorte que le modèle lit le texte que vous gardez et travaille pendant que vous lisez. C'est une étape de [ce qui suit la partie](../books/story-editor.md#what-follows-a-generated-part). La génération suivante trouve alors les connaissances à jour et n'attend pas. Ce qui reste (une exécution que vous avez arrêtée, une fenêtre que vous avez fermée) est lu avant la génération suivante, comme auparavant.

Pendant que le modèle travaille, un petit **progress panel** flotte au-dessus de la page. Il n'est **pas modal** : vous pouvez continuer à lire et à modifier l'histoire, et vous pouvez le **déplacer** par son titre. Le fermer le masque, le travail se poursuit (**Cancel** l'arrête).

![Le panneau de progression](../../images/plugin-knowledgetracker-progress.png)

Pendant que le modèle répond, le panneau affiche **What the model is writing** au fur et à mesure : d'abord son raisonnement, puis la réponse, afin qu'une longue attente avec un modèle lent ne soit pas une barre vide. Une fois la requête terminée, le même texte reste sous **What the model thought** (un bloc pour chaque requête de l'exécution, avec ce qu'elle a répondu), y compris lorsque le modèle n'a rien proposé et qu'aucune revue ne s'ouvre : l'onglet conserve un bouton **Last run: what the model thought** qui rouvre le panneau, pour que vous puissiez voir pourquoi rien n'a été ajouté.

Lorsque le modèle a répondu, le panneau l'indique et affiche **Review changes** ; le même bouton se trouve dans le panneau Processing de l'onglet (*3 changes to review*). **La revue ne s'ouvre jamais d'elle-même.** C'est un tableau des modifications, regroupées par personnage :

![Revue des modifications](../../images/plugin-knowledgetracker-review.png)

Sous le tableau, **What the model thought** (fermé au départ) contient le raisonnement du modèle qui précède sa réponse, lorsque le modèle l'envoie : il montre pourquoi une modification a été proposée, ou pourquoi une modification attendue manque, afin que vous puissiez juger du changement avant de l'appliquer. Un modèle qui ne raisonne pas n'affiche rien à cet endroit.

| | |
|---|---|
| La case à cocher | Si la modification est appliquée. Toutes sont cochées au départ ; chaque personnage a **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Un retrait rend un élément ancien (il est conservé, voir [l'onglet](#the-knowledge-tab)). Un ajout est un nouvel élément, *Knows* ou *Does not know*. Le texte et le type d'un ajout peuvent être **edited** avant d'être appliqués. |
| La citation | Les mots de la partie qui le montrent, et la partie où ils se trouvent. Une citation qui n'est pas retrouvée dans cette partie est signalée : le modèle a pu l'inventer, vérifiez-la avant d'accepter. |

**Apply** applique les modifications cochées et passe à la suite. **Cancel** arrête toute la série et ne change rien : les mêmes parties seront proposées à nouveau la prochaine fois. Un ajout décoché est **kept as old knowledge** avec sa citation (vous pouvez le restaurer) ; un retrait décoché laisse l'élément tel quel. Ce que le modèle a dit et qui ne peut pas être utilisé (un personnage qu'on ne lui a pas demandé, un élément qui n'existe pas, une modification sans citation, quelque chose que le personnage sait déjà) est listé sous *Left out*. Ce qui a été appliqué porte sa partie, si bien que **Reprocess from here** le reprend à nouveau. Si l'histoire a changé pendant que vous étiez en revue (autre branche, partie supprimée), les modifications ne sont pas appliquées et vous relancez le traitement.

Les parties sont lues par **exécutions** de **Parts per run** (1 par défaut) ; une exécution qui ne tient pas dans le contexte du modèle est réduite à ce qui tient. Une série se poursuit, exécution après exécution, jusqu'à la dernière partie stabilisée ; dans une série qui vous demande confirmation, une exécution où le modèle n'a rien trouvé est sautée sans demande. Lorsque **Ask before applying** est désactivé, les modifications sont appliquées immédiatement et le panneau tient un journal avec une ligne par exécution.

!!!
Le prompt a été testé avec un modèle sur quelques scènes, sans grande ampleur : il empêche les personnages d'apprendre ce qu'ils n'ont pas perçu, mais vérifiez ce qui est proposé, surtout pour les personnages présents dans une longue scène. La requête est envoyée au modèle que vous choisissez ci-dessous, avec le coût et la confidentialité habituels de ce service.
!!!
<a id="tidying-the-knowledge"></a>

## Nettoyer les connaissances

Le modèle note ce que chaque personnage apprend. Au fil d'une longue histoire, la liste s'allonge et contient plus de petits éléments qu'on ne veut en envoyer au modèle à chaque requête. **Tidy knowledge** (à côté de **Process now**, nécessite le réglage du modèle) demande au modèle de relire ce que sait un personnage, en tenant compte des dernières parties traitées de l'histoire comme contexte, et de :

- **supprimer** un élément qui ne vaut pas la peine d'être gardé : les détails anodins, un détail d'une scène que tous les présents ont vu (l'histoire le retient), ce que des événements ultérieurs ont dépassé, ce qui redit un autre élément ;
- **fusionner** plusieurs éléments portant sur une même chose en un seul qui conserve chaque fait.

Un élément supprimé ou fusionné n'est pas effacé : il devient *Old knowledge* et peut être restauré. Les modifications sont examinées, comme celles d'une lecture (c'est **Ask before applying** qui décide), ou appliquées aussitôt et inscrites dans le journal avec un bouton **Revert**.

La passe ne peut toucher que ce que le modèle a appris et que vous n'avez pas mis en **lock**. Les éléments initiaux, ceux que vous avez ajoutés ou modifiés à la main, les éléments verrouillés et les anciens ne sont jamais changés (les deux premiers types et les éléments verrouillés sont montrés au modèle pour qu'il voie ce qu'un élément ferait double emploi avec). Un secret qu'aucune partie n'a évoqué depuis longtemps n'est pas une raison de le supprimer, et le prompt le précise ; dans le doute, le modèle garde l'élément. Un personnage ayant moins de **Tidy: from this many items** (8) éléments appris par le modèle n'est pas nettoyé, et **Tidy: parts of context** (10) correspond au nombre des dernières parties traitées que le modèle lit.

**Tidy automatically** (un réglage, désactivé par défaut) lance la passe de nettoyage toute seule à la fin d'une exécution dont les modifications sont appliquées sans demande (**Ask before applying** désactivé, donc la lecture automatique des nouvelles parties aussi), une fois que le modèle a lu **Tidy after this many parts** (10) parties depuis le dernier nettoyage. Ce qu'elle modifie est appliqué et inscrit dans le journal avec un bouton **Revert**, et le journal de l'exécution le signale. Une exécution que vous examinez ne lance jamais un nettoyage de lui-même, et un personnage avec trop peu d'éléments est ignoré sans que le modèle soit sollicité.

La même chose depuis la barre de commandes : `knowledge-tidy` (`ask true` pour obtenir les modifications sous forme de liste ; l'onglet ouvert affiche alors le bouton de révision), et `lock-knowledge` / `unlock-knowledge` avec le personnage et le numéro de l'élément.

<a id="settings"></a>

## Réglages

L'engrenage à côté de *Track a character* :

| Réglage | |
|---|---|
| **In prompt** | Insère le bloc dans le prompt de chaque génération. Le bouton devient vert lorsqu'il est activé. |
| **Depth** | Où il se place, compté en messages depuis la fin du prompt : `0` après les instructions du tour, `1` juste avant elles. Il ne passe jamais avant le prompt système. |
| **Role** | S'il est envoyé comme message *System*, *Assistant* ou *User*. |
| **Say that nothing else is known** | La ligne de monde clos. |
| **Give what the characters know to the annotation** | Activé (par défaut) : [Annoter avec l'IA](#annotating-with-what-the-characters-know) reçoit ce que les personnages suivis savaient au début de la partie. |
| **Process new parts automatically** | Voir [Traitement](#processing). |
| **Read what the characters learn with the model** | Le traitement demande au modèle ce que chaque personnage suivi a appris et le propose. Désactivé (par défaut) : le traitement note seulement qui a rencontré qui, ce qui ne coûte rien. |
| **Ask before applying** | Activé (par défaut) : chaque proposition est soumise à examen. Désactivé : elle est appliquée aussitôt et journalisée. |
| **Let the model read earlier parts** | Pendant la lecture des nouvelles parties, le modèle peut lire la partie où un élément a été appris (les éléments disent « learned in part 12 »), pour voir si ce qu'il lit confirme, répète ou contredit cet élément : un ouï-dire que l'on voit maintenant de ses propres yeux, par exemple. Au plus 5 lectures d'une partie chacune par requête, le texte d'une partie longue est coupé pour tenir dans le contexte, et l'examen indique les parties lues. Chaque lecture est un aller-retour de plus avec le modèle, c'est pourquoi l'option est désactivée par défaut. Un modèle incapable d'appeler des fonctions lit la requête comme avant. |
| **Parts per run** | Combien de parties une requête lit. Plus grand, c'est moins cher, mais la réponse est plus longue et l'examen plus difficile. |
| **Tidy automatically**, **Tidy after this many parts** | La passe de nettoyage se lance toute seule à la fin d'une exécution qui applique ses modifications sans demander, toutes les ce nombre de parties (10). Désactivé par défaut. |
| **Tidy: parts of context** | Combien des dernières parties traitées la passe de nettoyage lit (10). |
| **Tidy: from this many items** | Un personnage ayant moins d'éléments appris par le modèle (et non verrouillés) n'est pas nettoyé (8). |
| **Lorebook for the model** | *Entries the parts activate* (par défaut : les entrées qu'une génération utiliserait pour ces parties), *The whole lorebook* (chaque entrée ; quand elle ne tient pas dans le contexte, les entrées activées sont envoyées et la note le signale) ou *No lorebook*. |
| **Model / Protocol for the extraction** | Vide : le modèle du livre. Un modèle moins cher suffit pour cela, et sur un serveur qui ne garde qu'un seul cache de prompt, un modèle séparé évite d'être remplacé. |
| **Separators of this book** | Comment le champ des personnages présents est découpé en noms, un par ligne (un retour à la ligne est `\n`). Un mot comme `and` découpe entre les espaces, donc *Jack and Jill* reste un seul nom tant que vous ne l'ajoutez pas. |
| **My separators (all books)** | Idem pour tous vos livres. Les séparateurs propres au livre l'emportent ; sans les deux, ce sont les séparateurs intégrés qui servent. |

**Use these settings for new books** (sous les réglages du modèle) enregistre les réglages de ce livre, la lecture avec le modèle, le modèle et le protocole compris, comme ceux avec lesquels démarre un livre là où le Knowledge Tracker n'a jamais servi. Ainsi, un nouveau livre lit avec votre modèle dès sa première exécution. Les séparateurs du livre restent attachés à celui-ci. **Forget the settings for new books** revient aux réglages intégrés. Les livres qui utilisent déjà le tracker gardent leurs propres réglages.

## Prompts

Le message système de la lecture (**Knowledge extraction prompt**) et celui de la passe de nettoyage (**Knowledge tidy prompt**) sont des [réglages hérités](../books/prompts.md#inherited-settings) : un livre utilise le sien, sinon le dossier le plus proche au-dessus de lui qui en définit un, sinon le vôtre, sinon le texte intégré qui a été testé sur un vrai modèle.

Définissez-les dans l'onglet *Prompts* du livre, sous *Extension Settings*, dans la boîte de dialogue d'un dossier, ou pour tous vos livres dans *Settings → Extension Settings → Knowledge Tracker* (vide affiche le texte intégré en gris). Conservez le format de réponse JSON du texte intégré : le plugin lit la réponse grâce à lui. Les instructions pour `read_part` sont ajoutées par le plugin lorsque le modèle peut lire des parties antérieures, donc votre prompt n'en a pas besoin.
<a id="annotating-with-what-the-characters-know"></a>

## Annoter avec ce que les personnages savent

Le bloc ci-dessus empêche le modèle de rendre un personnage omniscient *pendant qu'il écrit*. Ces mêmes fiches aident aussi à repérer les erreurs qui se sont glissées malgré tout. Quand un modèle [annote une partie](../books/annotations.md#letting-a-model-annotate) (**Annotate with AI**, une plage de parties, l'annotation automatique, `/generate-annotation`), le suivi lui indique ce que les personnages suivis savaient de cette partie **au moment où elle commence**, et l'éditeur signale un personnage qui dit, pense ou fait ce qu'il ne peut pas savoir : *Fiora salue Thomas par son nom ; les fiches disent qu'elle ne l'a jamais rencontré.* La note nomme le fait.

- **Les faits sont ceux de la partie, pas ceux de la fin du livre.** Ce qu'un personnage apprend dans une partie ultérieure n'est pas encore connu ; un élément qu'une partie ultérieure retire était encore vrai. Ce que la partie apporte elle-même (ce qui est appris, qui est rencontré, ce qui cesse d'être vrai en elle) est listé à part, afin que la première moitié de la partie ne soit pas vérifiée par rapport à la seconde.
- **Quels personnages.** Les personnages suivis qui figurent dans les *Present Characters* de la partie, qui sont son point de vue, ou qui sont nommés dans son texte (mot entier, quelle que soit la casse, alias compris).
- **Les éléments qui ne sont des faits à aucun moment** sont exclus : ceux que vous avez retirés à la main, ceux que la passe de nettoyage a supprimés, ceux que vous avez refusés lors d'une révision.
- **Seulement ce qui est consigné.** On dit au modèle que ce que les fiches ne disent pas n'est pas un fait, afin qu'il ne signale pas un personnage pour une chose dont le suivi n'a tout simplement jamais eu connaissance. *Say that nothing else is known* ajoute aussi « (personne d'autre) » à la liste des personnes qu'un personnage a rencontrées.
- Si une partie antérieure à celle qui est révisée n'a pas été lue par le suivi, les faits le signalent : ce que les personnages y ont appris manque, et **Process now** d'abord donne une meilleure révision.
- Les faits coûtent des jetons dans la requête d'annotation, uniquement pour les parties qui ont un personnage suivi. Désactivez-les avec **Give what the characters know to the annotation**.

<a id="the-block-as-a-macro"></a>

## Le bloc en macro

Au lieu de l'insertion automatique (ou en plus de celle-ci), vous pouvez placer le bloc vous-même avec la [macro {%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). L'argument est la liste des personnages de la scène, de sorte qu'un modèle peut transmettre le champ de la partie :

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

La macro affiche le bloc, que **In prompt** soit activé ou non ; désactivez-le donc lorsque vous utilisez la macro, sinon le modèle le reçoit deux fois. La profondeur et le rôle ne s'appliquent pas à la macro (le bloc fait partie du message qui la contient). Elle n'affiche rien si aucun des personnages listés n'est suivi. La réponse d'une macro de commande est conservée avec la partie à laquelle la nouvelle partie fait suite, de sorte qu'une seconde génération à partir de la même partie ne voit pas une modification que vous auriez faite entre-temps ; une régénération et un swipe demandent à nouveau.

<a id="commands"></a>

## Commandes

Pour la [ligne de commande](../commands/index.md) et pour les agents :

| Commande | |
|---|---|
| `knowledge-status` | Les paramètres et l'état d'avancement du traitement. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Les personnages suivis, désignés par leur nom, leur alias ou leur numéro. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Les éléments d'un personnage, désignés par leur numéro dans `list-knowledge`. |
| `knowledge-process` | Traite maintenant (`parts` : au plus ce nombre). Avec le modèle activé, `ask true` renvoie ce que le modèle a trouvé sous forme de modifications numérotées et les retient : rien n'est encore modifié. L'onglet Knowledge ouvert affiche un bouton **changes found by a command** qui ouvre la révision habituelle (Apply / Cancel) ; un agent les applique avec `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Applique les modifications retenues, toutes ou seulement les numéros donnés (identique à **Apply** dans la révision de l'onglet). |
| `knowledge-tidy` | La passe de nettoyage. Avec `ask true`, elle renvoie les modifications sous forme de liste numérotée et les retient (l'onglet affiche un bouton pour les réviser ; `apply-knowledge-changes` les applique). |
| `lock-knowledge`, `unlock-knowledge` | Verrouillent un élément (personnage, numéro d'élément) pour que la passe de nettoyage n'y touche pas, ou le déverrouillent. |
| `knowledge-reprocess` | Retraite depuis une partie (son identifiant ou sa position) ; demande confirmation d'abord. Avec le modèle activé, elle se contente de restaurer l'état ; `knowledge-process` relit les parties. |
| `knowledge-set-start` | Définit le point de départ ; sans partie, le début du livre. |
| `knowledge-tracker-block` | Le bloc sous forme de texte, pour la macro. |

La ligne de commande découpe ses arguments aux espaces : mettez une liste de personnages entre guillemets (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Stockage

Tout est stocké avec le livre (dans ses attributs), donc cela fait partie des [sauvegardes du livre](../books/backups.md) et des copies. Les [points de contrôle](#returning-to-an-earlier-point) n'en font pas partie : ce sont des fichiers à vous, liés au livre.
