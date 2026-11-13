---
label: Statistiques d'écriture
order: 75
---

# Statistiques d'écriture

Marginalia compte ce qui arrive à vos livres, jour après jour : combien de mots vous avez écrits et combien les modèles en ont écrit, combien de temps vous avez écrit, combien de jetons ont été envoyés aux modèles, et la taille que le livre a prise. La fenêtre **Statistiques** affiche tout cela pour un livre ou pour l'ensemble de vos livres, sous forme de chiffres, de graphiques et d'un **calendrier** des jours où vous avez travaillé, avec votre **série** et un **objectif quotidien**.

Ces chiffres ne sont destinés qu'à vous : chaque compte ne voit que ses propres livres. Le comptage commence avec la version 1.6.0 ; ce qui s'est passé auparavant est [reconstitué à partir de l'historique des parties](#days-before-the-statistics) du mieux possible.

<a id="opening-the-window"></a>

## Ouvrir la fenêtre

- **Un seul livre :** le menu des paramètres (les engrenages sous l'histoire) contient **Statistiques**. La fenêtre compte ce livre-là.
- **Tous les livres :** la liste des livres contient **Statistiques** dans son menu **Gérer**. La fenêtre additionne tous vos livres et ajoute deux onglets, **Livres** et **Par livre**.

Les deux fenêtres fonctionnent de la même manière. En haut, vous choisissez la **Période** (*Aujourd'hui*, *Hier*, *7 derniers jours*, *30 derniers jours*, *Ce mois-ci*, *Depuis toujours*, ou *Personnalisée* avec une date **Du** et une date **Au**) et **Compté pour** : **Toutes**, **Moi** (vous et les modèles que vous avez sollicités) ou **Agents** (ce que les [agents](../agents.md) ont fait, comptabilisé à part). Les jours sont ceux de l'horloge du serveur.

![Les statistiques d'un livre : les jours](../../images/statistics-book.png)

Sous ce choix se trouvent les totaux de la période, puis les onglets.

<a id="what-is-counted"></a>

## Ce qui est compté

| | |
|---|---|
| **Mots écrits** | Les mots que vous avez écrits vous-même : le texte tapé dans une partie, dans l'éditeur de l'histoire ou partout ailleurs où vous modifiez une partie. |
| **Mots générés** | Les mots que les modèles ont écrits pour le livre. |
| **Mots supprimés** | Les mots qui ont disparu : le texte que vous avez coupé d'une partie, une partie que vous avez supprimée. |
| **Parties créées**, **Parties supprimées**, **Modifications** | Combien de parties ont été créées et supprimées, et combien de fois vous avez enregistré une partie modifiée. |
| **Jetons envoyés**, **Jetons générés**, **Requêtes** | Ce qui a été envoyé aux modèles pour ce livre (la totalité du prompt à chaque fois) et ce qui est revenu, ainsi que le nombre de requêtes nécessaires. |
| **Ouvert** | Le temps pendant lequel le livre est resté ouvert à l'écran. |
| **Édition** | Le temps pendant lequel vous avez tapé dans une partie. Une longue pause arrête le chronomètre. |
| **Génération** | Le temps pendant lequel un modèle a travaillé pour le livre. |
| **Agents en cours** | Le temps pendant lequel un agent a travaillé sur le livre. |
| **Croissance** | De combien le livre a grandi (ou diminué) sur la période, en mots. |

Les durées correspondent à l'horloge réelle. Lorsque deux livres sont ouverts en même temps, ou que trois agents tournent ensemble, le total ne compte qu'une fois ce laps de temps, de sorte que les lignes des livres peuvent faire plus que le total (la fenêtre le signale). Les mots et les jetons, eux, s'additionnent bien.

L'importation d'un livre ou d'un texte (une archive, une conversation issue de SillyTavern), la copie d'un livre et la restauration d'une sauvegarde ne comptent pour rien : le texte n'a pas été écrit ici. Ce que font les agents est compté de leur côté (**Agents**), par rapport au livre d'origine, bien qu'ils travaillent sur une copie ; cela inclut le travail qui a été abandonné ou jamais fusionné dans le livre.

<a id="the-tabs"></a>

## Les onglets

| Onglet | |
|---|---|
| **Jours** | Une ligne pour chaque jour qui contient quelque chose : tous les chiffres ci-dessus, du plus récent au plus ancien. Cliquez sur un en-tête pour trier. |
| **Mots** | Un graphique des mots écrits, générés et supprimés par jour. Une longue période est regroupée par semaine ou par mois. |
| **Temps** | Un graphique des heures d'ouverture, d'édition, de génération et de travail d'un agent. |
| **Jetons** | Un graphique des jetons envoyés et générés. |
| **Taille** | La taille du livre à la fin de chaque jour et sa croissance quotidienne, en **Mots** ou en **Jetons**. |
| **Livres** | (tous les livres) Une ligne pour chaque livre, y compris ceux de la [corbeille](../trash.md) ou disparus, avec son dossier, ses chiffres, son temps et sa croissance, ainsi que sa part. **Afficher les livres supprimés** masque ceux de la corbeille et ceux qui sont disparus. Un clic sur un livre l'ouvre. |
| **Par livre** | (tous les livres) Les livres sous forme de barres, selon le critère que vous choisissez dans **Graphique par**. Les dix plus gros sont affichés, et le reste forme une seule barre, *Autres*. |
| **Calendrier** | Les jours où vous avez travaillé, vos séries et votre objectif, voir ci-dessous. |

![Le temps par jour](../../images/statistics-time.png)

La taille d'un livre est mesurée un instant après chaque changement, à partir des mots et des jetons que les parties ont enregistrés. Les jetons d'une ancienne partie sont ceux comptés au moment de son écriture ; changer le modèle du livre ne modifie donc aucun chiffre.

<a id="the-calendar"></a>

## Le calendrier

L'onglet **Calendrier** affiche un petit carré pour chaque jour de la dernière année (53 semaines, 16 sur un téléphone), une semaine par colonne, le jour le plus récent se trouvant dans le coin inférieur droit, marqué d'un cadre. Plus le carré est foncé, plus vous avez fait ce jour-là. Placez le pointeur sur un jour pour voir la date, les mots que vous avez écrits, le temps d'édition et la dernière fois que vous y avez travaillé (un `~` après un jour reconstitué à partir de l'historique).

![Le calendrier de tous les livres, avec l'objectif du jour](../../images/statistics-global-calendar.png)

Un jour est compté comme **travaillé** lorsque vous avez écrit des mots, modifié une partie, créé une partie ou passé du temps à éditer. Ce que les agents ont fait, les mots seulement générés et l'ouverture d'un livre ne comptent pas : le calendrier concerne *votre* écriture. Un carré à peine plus foncé qu'un carré vide correspond à un jour où vous avez travaillé mais écrit peu.

Choisissez la manière dont les carrés sont colorés avec **Mots écrits** ou **Édition** (minutes d'édition). Le calendrier affiche toujours les dernières semaines, quelle que soit la **Période** choisie au-dessus.

Au-dessus du calendrier :

| | |
|---|---|
| **Série actuelle (jours)** | Les jours consécutifs où vous avez travaillé, comptés à partir d'aujourd'hui. Un aujourd'hui sur lequel vous n'avez pas encore travaillé ne rompt pas la série : elle est alors comptée à partir d'hier. |
| **Plus longue série (jours)** | La plus longue suite de jours consécutifs, depuis toujours. |
| **Jours de travail** | Le nombre de jours des carrés affichés pendant lesquels vous avez travaillé. |
| **Série d'objectifs (jours)**, **Plus longue série d'objectifs (jours)** | Comme les séries ci-dessus, mais pour les jours qui ont atteint votre objectif quotidien (lorsque vous en avez défini un, dans la fenêtre de tous les livres). |

<a id="the-daily-goal"></a>

### L'objectif quotidien

Définissez un objectif dans l'onglet **Paramètres**, dans les réglages généraux :

| Champ | |
|---|---|
| **Objectif quotidien : mots écrits** | Les mots que vous écrivez dans une journée, tous livres confondus. Les mots générés par les modèles ne comptent pas. |
| **Objectif quotidien : minutes d'édition** | Les minutes que vous passez à éditer dans une journée, tous livres confondus. |

![Les objectifs dans les Paramètres](../../images/settings-daily-goal.png)

Laissez un champ vide pour ne pas avoir d'objectif. Lorsque les deux sont définis, un jour atteint l'objectif lorsqu'il atteint **les deux**. La fenêtre de **tous les livres** indique alors où vous en êtes aujourd'hui sous forme de deux barres (*Aujourd'hui : 310 mots sur 500*), la couleur du calendrier suit l'objectif (un jour qui l'a atteint est le plus foncé), et la **Série d'objectifs** compte les jours qui l'ont atteint. Sans objectif, la couleur suit votre journée la plus chargée du calendrier. La fenêtre d'un seul livre n'a pas d'objectif (un objectif sur tous les livres a peu de sens pour l'un d'entre eux) : ses carrés sont colorés par rapport à la journée la plus chargée de ce livre.

Lorsqu'un jour n'a pas de temps enregistré (voir ci-dessous) et que le calendrier est coloré selon **Édition**, il est simplement marqué comme jour travaillé.

![Les statistiques de tous les livres sur un téléphone](../../images/phone-statistics.png)

<a id="days-before-the-statistics"></a>

## Les jours avant les statistiques

Les statistiques n'étaient pas tenues avant la version 1.6.0. Lors de la mise à jour de Marginalia, les jours antérieurs ont été reconstitués à partir de l'historique des parties : la première version d'une partie correspond à une partie créée avec les mots qu'elle contenait, une version ultérieure à une modification, et une partie disparue à une suppression. Ces jours ne sont qu'une estimation. Ils sont marqués d'un `~` dans la liste des jours et la fenêtre indique combien il y en a ; les jetons d'un jour reconstitué ne sont qu'un minimum. Le **temps** ne peut pas être reconstitué, car personne ne l'a enregistré : la fenêtre indique à partir de quel jour il est enregistré (*Le temps est enregistré depuis ...*), les jours antérieurs affichent un tiret et sont exclus du graphique du temps. Le **calendrier** compte un jour reconstitué comme jour travaillé, comme tout autre.
<a id="privacy-clearing-not-recording-keeping-less"></a>

## Vie privée : effacer, ne pas enregistrer, en garder moins

Les statistiques indiquent quand vous avez travaillé sur un livre, pendant combien de temps, et conservent le nom et le dossier de chaque livre, y compris des livres que vous avez supprimés. Elles vous appartiennent, et c'est vous qui décidez de ce qu'il en reste.

![Enregistrer les statistiques d'écriture et Conserver les statistiques pendant, dans les Paramètres](../../images/settings-statistics-privacy.png)

- **Effacer les statistiques de ce livre** (bouton dans la fenêtre d'un livre) et **Effacer toutes les statistiques** (dans la fenêtre de tous les livres) suppriment définitivement les comptes, les durées et les tailles, après une demande de confirmation. Les livres ne sont pas modifiés. Le comptage reprend ensuite à zéro.
- Dans la liste des livres, un livre qui est dans la corbeille ou supprimé a **Oublier**, qui ne supprime que ses statistiques.
- **Enregistrer les statistiques d'écriture** dans les [paramètres](../account.md) arrête le comptage pour vos livres. Ce qui a déjà été enregistré reste jusqu'à ce que vous l'effaciez ; la fenêtre indique alors que rien de nouveau n'est compté. Un administrateur peut aussi désactiver l'enregistrement pour tout le monde, avec la même note.
- **Conserver les statistiques pendant (mois)** dans les paramètres supprime les jours plus anciens que cette durée, une fois par jour et lors de l'enregistrement. Une valeur vide ou 0 conserve tout. L'administrateur peut fixer une limite pour tous les utilisateurs, et c'est la plus courte des deux qui s'applique. Pour les tailles d'un livre, la dernière valeur antérieure à la limite est conservée, afin de pouvoir encore calculer la croissance du premier jour conservé.

L'[administrateur](../administration/statistics.md) peut effacer les statistiques d'un utilisateur ou de tous les utilisateurs.

<a id="commands"></a>

## Commandes

Deux [commandes](../commands/index.md) lisent les mêmes chiffres : `/get-book-statistics [range] [side]` pour le livre ouvert, jour par jour, et `/get-statistics [range] [by] [side]` pour tous vos livres, par jour ou par livre. La plage peut être `today`, `yesterday`, `last-7-days`, `last-30-days`, `this-month`, `all-time`, un jour (`2026-03-14`) ou deux jours (`2026-03-01..2026-03-14`). Un [agent](../agents.md) peut aussi les utiliser.
