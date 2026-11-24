---
label: Suivi d'état
order: 29
---

# Suivi d'état

Garde une trace de l'**état** actuel de vos personnages : ce qu'ils portent, ce qu'ils transportent, l'arme qu'ils ont en main, leurs blessures, leur humeur, ou tout autre élément que vous définissez. Il place l'état des personnages présents dans la scène dans le prompt. Un modèle oublie facilement que l'épée a été perdue deux chapitres plus tôt ou que la cape est trempée. Avec le State Tracker, le prompt indique, pour chaque personnage suivi présent dans la scène, ce qui est vrai à son sujet maintenant.

!!!
Vous choisissez qui est suivi et ce qui est suivi à leur sujet. Seuls les personnages que vous ajoutez à l'onglet sont décrits au modèle, et seulement lorsqu'ils font partie des *personnages présents* de la partie en cours d'écriture. Vous pouvez tenir l'état vous-même, ou laisser un modèle lire l'histoire et proposer les changements (voir [Suivre l'histoire](#following-the-story)).
!!!

<a id="categories"></a>

## Catégories

Une **catégorie** est un élément qui peut être suivi à propos d'un personnage. Chaque catégorie a :

- un **nom**, qui est aussi ce que le prompt appelle la catégorie (*Wearing*, *Weapon at hand*, *Carrying*) ;
- un **type** :
  - **Single value** : une valeur courte, et une nouvelle valeur remplace l'ancienne (l'arme en main, le lieu).
  - **List** : des entrées ajoutées et retirées une à une (ce qui est transporté, les blessures).
  - **Description** : un texte unique, réécrit dans son ensemble (les vêtements, l'humeur).
- une **consigne pour le modèle** : ce qui appartient à la catégorie et ce qui n'y appartient pas. Le modèle la lit lorsqu'il suit l'histoire.
- **Only what the text says** : activé, la valeur ne change que lorsque le texte le dit ; désactivé, elle change aussi selon les conséquences évidentes (mouillé après une baignade).
- **Say "none" when empty** : le prompt écrit *none* lorsque le personnage n'a aucune valeur (une arme perdue), au lieu d'omettre la catégorie.

<a id="my-categories"></a>

### Mes catégories

Vos propres catégories se trouvent dans **Settings → Extension Settings → State Tracker**. Les livres copient leurs catégories à partir de celles-ci. Jusqu'à ce que vous les modifiiez, vous disposez de cinq catégories intégrées : *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* et *Mood*. Ajoutez, modifiez et supprimez des catégories ici, puis appuyez sur **Save** dans l'écran Settings. **Reset to the built-in categories** rétablit les cinq catégories intégrées.

<a id="the-categories-of-a-book"></a>

### Les catégories d'un livre

Un livre garde sa **propre copie** de chaque catégorie qu'il utilise, afin qu'une sauvegarde ou un export du livre contienne tout, et que supprimer une de vos catégories ne casse jamais un livre. Le bouton **Categories of this book** de l'onglet State liste ces catégories :

- **Add from my categories** copie l'une de vos catégories dans le livre.
- **New category for this book** crée une catégorie qui n'existe que dans ce livre (la cargaison d'un navire, un lien magique). Une telle catégorie est marquée *Only in this book*. **Save to my categories** l'ajoute à vos catégories, afin que d'autres livres puissent aussi l'utiliser.
- Une copie qui ne correspond plus à votre catégorie est marquée *Differs from your category*. **Update from my categories** reprend votre version. Rien n'est mis à jour sans vous, car une consigne modifiée change ce que fait le modèle.
- Modifier une catégorie ne la change que dans ce livre. Son type ne peut pas changer tant que des personnages ont des valeurs pour elle.
- Supprimer une catégorie du livre supprime les valeurs de chaque personnage dans cette catégorie.

<a id="the-state-tab"></a>

## L'onglet State

La barre latérale gauche de l'[éditeur d'histoire](../books/story-editor.md) reçoit un onglet **State**. Tout est enregistré dans le livre au fur et à mesure que vous le modifiez.

![L'onglet State avec un personnage suivi](../../images/plugin-statetracker.png)

![L'historique d'une valeur](../../images/plugin-statetracker-history.png)

**Track a character** demande un **name**, éventuellement des **aliases** (les autres noms sous lesquels le personnage est écrit dans le champ *Present Characters* d'une partie, séparés par des virgules), et les **tracked categories**. La liste propose les catégories du livre et les vôtres ; en choisir une des vôtres la copie dans le livre. Un cheval peut ne suivre que *Injuries*, tandis que l'héroïne suit tout.

**Import from the Character Tracker** (l'icône de personne à côté de **Track a character**) commence à suivre des personnages du [Character Tracker](character-tracker.md) du même livre. Elle liste les personnages que le livre ne suit pas encore (par un nom ou un alias) avec leur accroche, tous cochés ; décochez ceux à laisser de côté, cochez les **tracked categories** que chaque personnage importé recevra (celles du livre et les vôtres, comme ci-dessus), puis appuyez sur **Import**. Seuls les noms sont repris ; un personnage peut recevoir d'autres catégories plus tard avec le crayon.

Chaque personnage suivi a une fiche, fermée au départ : ouvrez-la avec la flèche de la fiche pour voir ses catégories, chacune avec sa valeur ou ses valeurs actuelles :

- **+** définit la valeur d'une catégorie de type Single value ou Description (la valeur actuelle passe dans l'historique) ou ajoute une entrée à une liste.
- Le crayon modifie une valeur, la croix **supprime** : elle n'est plus vraie, donc elle passe dans l'historique et n'est pas envoyée au modèle.
- Le cadenas marque une valeur comme **locked** (une cicatrice, l'épée familiale). Le [nettoyage](#tidying-the-state) laisse les valeurs verrouillées intactes ; l'histoire peut quand même les modifier.
- **History** affiche les valeurs passées de la catégorie, avec la raison de leur fin (*replaced*, *removed* ou *tidied*) et, lorsque le modèle les a terminées, la partie qu'il a lue pour cela. **Restore** rend une valeur passée de nouveau actuelle (pour une catégorie Single value ou Description, la valeur actuelle passe dans l'historique à sa place). **Delete** supprime une valeur définitivement.

Le crayon du personnage modifie son nom, ses alias et ses catégories. Décocher une catégorie la masque mais conserve ses valeurs ; la cocher à nouveau les fait réapparaître. La poubelle cesse de suivre le personnage et supprime tout son état.
<a id="following-the-story"></a>

## Suivre l'histoire

Le panneau **Processing** en haut de l'onglet State permet à un modèle de lire l'histoire et de repérer ce qui a changé : une cape trempée dans la rivière, une lanterne donnée à quelqu'un d'autre, une épée tirée, une lettre perdue. Il lit les parties de la branche active dans l'ordre, à partir du **Start point** (le début du livre par défaut ; les parties situées avant ce point ne sont jamais lues), et il retient jusqu'où il est allé.

![Les réglages du traitement](../../images/plugin-statetracker-settings.png)

![La revue des changements proposés](../../images/plugin-statetracker-review.png)

- **Process now** lit les parties qui ne sont pas encore traitées, **Process next part** seulement la suivante. Une longue série demande d'abord combien de requêtes elle va nécessiter. Quand vous faites défiler la liste des personnages au-delà de ces boutons, **Process now**, **Process next part** et **Tidy the state** sont aussi proposés dans la barre en haut de l'onglet.
- Le modèle reçoit les personnages suivis qui sont présents dans les parties, avec leurs catégories, l'instruction de chacun et leurs valeurs actuelles, puis il répond par les changements. Chaque changement doit être accompagné d'une citation tirée de la partie. Les changements qui ne correspondent pas (une catégorie que le personnage ne suit pas, une valeur qu'il possède déjà, une citation provenant d'une partie qui n'existe pas) sont écartés.
- Un panneau affiche la progression et ce que le modèle écrit pendant qu'il écrit ; vous pouvez continuer à lire et à écrire. Avec **Ask before applying** activé (par défaut), le traitement attend votre décision : **Review changes** affiche les changements par personnage, avec la valeur finale barrée et la nouvelle valeur, que vous pouvez corriger, ainsi que la citation. Décochez ce qui est faux, puis appuyez sur **Apply** ; **Cancel** laisse tout en l'état, et les mêmes parties seront proposées à nouveau la fois suivante.
- Une valeur lue par le modèle indique la partie dont elle provient. **Processed parts** liste les parties qui ont été lues ; **Reprocess from here** annule tout ce qui a été lu à partir de cette partie et des suivantes (les valeurs que vous avez écrites ou corrigées sont conservées) et les relit. Faites-le après avoir réécrit une partie antérieure. Une partie régénérée, un swipe ou une autre branche annulent d'eux-mêmes ce qui provenait des parties qui n'existent plus.

<a id="the-story-clock"></a>

### L'horloge de l'histoire

Lorsque **Story clock** est activé (dans les réglages, désactivé par défaut), le modèle suit aussi le temps de l'histoire. Dans la même requête que l'état, il indique pour chaque partie ce que cette partie dit du temps : *le lendemain matin*, *trois jours plus tard*, *cette nuit-là*, *le quarantième jour depuis votre réveil*, *le 20 avril*, une formule vague comme *les semaines se sont confondues* (marquée comme approximative), ou un retour en arrière, un souvenir ou un rêve du passé, qui ne fait pas avancer l'horloge et ne modifie rien dans l'état. Le modèle ne compte pas : c'est le plugin qui additionne les jours.

![L'horloge de l'histoire dans les réglages du traitement](../../images/plugin-statetracker-clock.png)

![Corriger à la main l'heure d'une partie](../../images/plugin-statetracker-clock-dialog.png)

- **Day 1 is the day of** indique l'événement à partir duquel les jours sont comptés (*l'éveil*) : l'heure s'affiche alors *Jour 14 après l'éveil, soir*. **Day at the start point**, **Time of day** et **Date at the start point** donnent l'heure avant la première partie lue ; avec une date, l'horloge compte aussi les dates (*20 avril 1453*).
- Le panneau Processing affiche désormais le **story time**, et chaque partie traitée affiche l'heure qui suit. Un jour ou une date indiqués remplacent une estimation antérieure.
- Le bouton horloge d'une partie (ou de la ligne du temps de l'histoire, pour la dernière partie) **règle l'heure à la main** : un point fixe (jour, moment de la journée, date) que la relecture de la partie ne remplace pas et à partir duquel l'horloge continue de compter. **Remove the fixed point**, dans la même boîte de dialogue, laisse la relecture du modèle compter à nouveau.
- Dans la revue, l'heure vient en premier, sous **Story time**, avec la citation de chaque partie ; décochez ce qui est faux.
- Le bloc de prompt commence par l'heure (*Heure de l'histoire : jour 14 après l'éveil, soir.*), même lorsqu'aucun personnage suivi n'est présent dans la scène.
- Une valeur garde en mémoire le jour de l'histoire où elle a été posée (*jour 4*). Le nettoyage voit depuis combien de temps c'est le cas (*une tasse de thé, posée il y a 9 jours*) et supprime ce qui a eu le temps de s'estomper.
- Avec l'horloge activée, chaque partie est envoyée au modèle, y compris celles sans personnage suivi, car n'importe quelle partie peut faire avancer le temps.
- Un nouveau traitement, une régénération ou une autre branche annulent ce qui a été lu à partir des parties disparues, comme pour l'état ; les points fixes que vous avez réglés sont conservés.

<a id="the-log-of-automatic-changes"></a>

### Le journal des changements automatiques

Ce qui est appliqué sans revue (un traitement avec **Ask before applying** désactivé, le nettoyage automatique, une commande sans `ask`) est inscrit dans **Applied automatically**, dans le panneau Processing : qui, quelle catégorie, à partir de quelle partie (ou *nettoyé*) et ce qui a changé (*+ lanterne*, *− corde*, *épée courte → hache*). Rien dans le journal n'est modifiable, mais chaque ligne possède un bouton **Revert**. Il agit comme si vous aviez décoché le changement dans la revue : la valeur créée par le changement est supprimée et la valeur qui avait été remplacée redevient actuelle. Une ligne est ignorée si vous avez modifié la valeur à la main depuis (en l'éditant, la supprimant, la restaurant ou la supprimant, ou en donnant une autre valeur à une seule catégorie). Le journal conserve les 200 derniers changements.

![Le journal des changements automatiques](../../images/plugin-statetracker-log.png)

<a id="returning-to-an-earlier-point"></a>

### Revenir à un point antérieur

Avant chaque traitement qui applique des changements (un traitement de l'onglet, avec ou sans revue, la lecture automatique de nouvelles parties, les commandes, un nettoyage), le suivi prend un **checkpoint** : l'état complet de ses personnages tel que le traitement l'a trouvé, avec l'horloge de l'histoire et la partie jusqu'à laquelle il avait lu (*Début du livre* avant le premier traitement). **Checkpoints**, dans le panneau Processing, les liste du plus récent au plus ancien : la partie jusqu'à laquelle le suivi avait lu, l'heure, le type de traitement qui a suivi et le nombre de changements appliqués. Seuls les checkpoints dont la partie se trouve sur la branche active sont listés.

![Un changement annulé](../../images/plugin-statetracker-reverted.png)

**Return to this point**, sur une ligne, annule ce traitement et tout ce qui l'a suivi : les valeurs des personnages (actuelles et passées), les repères de l'horloge de l'histoire, les parties traitées et le journal des changements automatiques redeviennent ceux du checkpoint. Le prochain traitement relit alors les parties qui suivent sa partie, y compris celles du traitement annulé. Ce que le suivi contenait est conservé dans un checkpoint (*avant un retour à un checkpoint*), afin que vous puissiez y revenir. Vos réglages, le start point, les réglages de l'horloge et les catégories du livre restent tels qu'ils sont aujourd'hui (une catégorie supprimée depuis réapparaît si les valeurs du checkpoint l'utilisent). Une confirmation est demandée d'abord, car tout ce qui a été lu après le checkpoint est annulé, de même que les changements que vous avez faits à la main depuis. Utilisez cette fonction lorsqu'une série de traitements a mal tourné, ou après avoir restauré une sauvegarde plus ancienne du livre et que vous voulez que le suivi rattrape son retard à partir d'un point fiable.

Le suivi conserve les 20 derniers checkpoints d'un livre. Ils sont stockés parmi vos fichiers (*Resources*, regroupés par livre), et non dans le livre ; les sauvegardes et les copies du livre ne les incluent donc pas.

<a id="tidying-the-state"></a>

### Nettoyer l'état

Certains états s'estompent sans que l'histoire le dise : une humeur, des vêtements mouillés, un souffle court, une tasse gardée longtemps après le repas. **Tidy the state**, dans le panneau Processing, interroge le modèle à leur sujet. Il lit les dernières parties traitées avec les valeurs actuelles des personnages suivis qui y figurent, et pour chaque valeur il peut :

![La revue d'un nettoyage](../../images/plugin-statetracker-tidy-review.png)

- **supprimer** la valeur : elle n'est plus vraie ou ne vaut plus la peine d'être gardée ;
- **mettre à jour** la valeur : les parties montrent qu'elle a évolué (la colère s'est changée en soulagement) ;
- **fusionner** des entrées de liste qui désignent la même chose (*corde* et *bobine de corde*).

Les valeurs que le nettoyage ne mentionne pas restent en place, de même que les valeurs verrouillées. Chaque changement est accompagné d'une raison. Une valeur écrite par le nettoyage (une mise à jour ou une fusion) est marquée *par le nettoyage*, avec la dernière partie lue, car l'histoire ne l'avait pas indiquée ; la modifier la fait devenir la vôtre. Vous la revoyez comme pour tout autre traitement (ou elle est appliquée immédiatement lorsque **Ask before applying** est désactivé). Une valeur que le nettoyage supprime passe dans l'historique comme *nettoyé*, et **Reprocess from here** sur la dernière partie lue avant le nettoyage l'annule. Les personnages absents des dernières parties ne sont pas touchés : une corde portée par quelqu'un qui a été absent pendant vingt chapitres reste donc en place.

Lorsque **Tidy automatically** est activé (par défaut), le nettoyage se lance tout seul à la fin d'un traitement qui applique ses changements sans demander confirmation, une fois que **Tidy after this many parts** parties (10 par défaut) ont été lues depuis le dernier nettoyage. Un traitement que vous revoyez ne déclenche jamais de nettoyage, afin de ne pas vous imposer une seconde revue.

Dans les réglages (l'engrenage) :

- **Process new parts automatically** : lorsqu'une partie est finalisée, le modèle la lit (au plus tard avant la génération suivante). Avec **Ask before applying** activé, elle n'est lue que lorsque vous appuyez sur **Process now** : une revue ne vous est jamais imposée au milieu de l'écriture.
- **Ask before applying** : désactivé, les changements sont appliqués immédiatement et inscrits dans le journal (voir plus haut).
- **Parts per run** : le nombre de parties qu'une requête lit.
- **Tidy automatically** et **Tidy after this many parts** : voir [Nettoyer l'état](#tidying-the-state). Le nettoyage relit aussi ce nombre des dernières parties traitées.
- **Lorebook for the model** : les entrées activées par les parties (par défaut), le lorebook entier, ou aucun.
- **Model for the extraction** et **Protocol for the extraction** : vide signifie le modèle du livre.
- **Use these settings for new books** enregistre les réglages de ce livre (le modèle et le protocole inclus, les séparateurs restant avec le livre) comme ceux avec lesquels démarre un livre où le State Tracker n'a jamais été utilisé. **Forget the settings for new books** revient aux réglages intégrés. Les livres qui utilisent déjà le suivi conservent leurs propres réglages.
## Prompts

Le prompt système de la lecture (**State extraction prompt**) et celui de la mise en ordre (**State tidy prompt**) sont des [paramètres hérités](../books/prompts.md#inherited-settings) : un livre utilise les siens, sinon le dossier parent le plus proche qui en définit, sinon les vôtres, sinon le texte intégré dans la langue de votre prompt (la version anglaise a été testée avec un vrai modèle).

Réglez-les dans l'onglet *Prompts* du livre, sous *Extension Settings*, dans la boîte de dialogue d'un dossier, ou pour tous vos livres dans *Settings → Extension Settings → State Tracker* (vide affiche le texte intégré en gris). Conservez le format de réponse JSON du texte intégré : le plugin lit la réponse grâce à lui. Les règles de l'[horloge de l'histoire](#the-story-clock) sont ajoutées par le plugin après le prompt d'extraction lorsque le livre suit le temps, donc votre prompt n'a pas besoin de les contenir.

## In the prompt

Le bouton engrenage contient les réglages du livre :

- **In prompt** insère l'état des personnages suivis de la scène dans le prompt de chaque génération (l'engrenage devient vert quand cette option est active).
- **Depth** et **Role** indiquent où il est placé, comme pour l'Author's Note : *Depth* est le nombre de messages après lui (0 après les instructions du tour, 1 juste avant elles).
- **Name separators** indiquent comment le champ *Present Characters* est découpé (par défaut `,` `;` `/` `|` et les retours à la ligne). Les notes entre crochets sont ignorées, donc `Mara (asleep), Fiora` trouve les deux.

Le prompt reçoit les personnages de la scène qui ont quelque chose à dire :

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Vous pouvez placer le même texte n'importe où dans un modèle avec la macro {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} (n'importe quelle liste de noms sert d'argument). La macro affiche l'état que **In prompt** soit activé ou non ; désactivez donc cette option lorsque vous utilisez la macro, sinon le modèle reçoit l'état deux fois.

## Commands

Pour la barre de commandes et pour les agents :

| Commande | Ce qu'elle fait |
|---|---|
| `state-status` | Les réglages, les catégories du livre et celles de vos catégories dont le livre n'a pas de copie. |
| `list-state-characters` | Les personnages suivis avec leurs catégories. |
| `track-state-character` | Commence à suivre un personnage : nom, alias, catégories par nom. |
| `stop-tracking-state-character` | Arrête de suivre un personnage et supprime son état (demande d'abord confirmation). |
| `list-character-state` | Les valeurs d'un personnage, actuelles et passées, numérotées. |
| `set-character-state` | Définit une valeur : personnage, catégorie, texte. |
| `remove-character-state` | Retire une valeur par son numéro : elle passe dans l'historique. |
| `state-tracker-block` | Le texte du prompt pour une liste de personnages (la macro). |
| `state-process` | Laisse le modèle lire les parties suivantes et applique les changements ; avec `ask true`, il les renvoie à la place, numérotés. |
| `apply-state-changes` | Applique les changements renvoyés par `state-process` ou `state-tidy`, tous ou ceux dont les numéros sont donnés. |
| `set-story-time` | Règle à la main l'heure de l'histoire après une partie traitée (jour, moment de la journée, date) ; sans aucun de ces éléments, il supprime le point fixe. |
| `state-tidy` | Laisse le modèle supprimer, mettre à jour ou fusionner les valeurs qui se sont estompées ; avec `ask true`, il renvoie les changements numérotés à la place. |
| `state-reprocess` | Annule ce qui a été lu dans une partie et dans les suivantes (demande d'abord confirmation). |
| `state-set-start` | Définit la partie à partir de laquelle la lecture commence. |
