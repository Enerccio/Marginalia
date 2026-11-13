---
label: Commandes
order: 945
icon: terminal
---

# Commandes

Les commandes sont de petites actions que l'on lance en les tapant, au lieu de cliquer dans les menus : chercher quelque chose dans l'histoire ou dans les fiches de lore, rechercher et remplacer, faire une sauvegarde. Les [extensions](../extensions/index.md) ajoutent leurs propres commandes, par exemple pour générer une fiche de lore ou pour inventer des noms. Ce sont les mêmes commandes qu'un [agent](../agents.md) utilise, si bien qu'une commande fait une chose claire et dit ce qu'elle fait : on peut le lire pendant qu'on la tape.

La **ligne de commande** est le champ situé à côté du menu, sous l'histoire, dans l'[éditeur d'histoire](../books/story-editor.md).

<a id="running-a-command"></a>

## Lancer une commande

1. Cliquez dans la ligne de commande et tapez `/`. La liste affiche toutes les commandes, chacune avec son nom et une courte description. En tapant davantage, la liste se réduit : le filtre regarde le nom de la commande, son titre et sa description.
2. Choisissez la commande avec la souris, ou avec les flèches du clavier et **Enter**. Une commande qui n'a pas besoin d'arguments s'exécute aussitôt ; pour une commande qui en prend, `/nom ` est écrit dans le champ et vous continuez avec les arguments.
![La liste des commandes, ouverte en tapant une barre oblique](../../images/command-list.png)

3. Tapez les arguments et appuyez sur **Enter**, ou cliquez sur le bouton de lecture à côté du champ, pour lancer la commande. Le champ est vidé dès que la commande démarre ; un texte qui n'est pas une commande reste, afin que vous puissiez le corriger.

Un texte qui ne commence pas par `/` n'est pas une commande, et rien n'est lancé. Cliquer en dehors du champ ou appuyer sur **Esc** ne lance jamais rien, de sorte qu'on ne peut pas démarrer une commande par accident.

<a id="the-help"></a>

## L'aide

Pendant que vous tapez, une petite fenêtre s'ouvre au-dessus du champ. Elle recouvre une partie de l'histoire mais ne déplace rien, et elle se ferme quand vous quittez le champ ou lancez la commande. Elle affiche :

- ce que fait la commande,
- les formes de la commande (voir [Formes](#forms-of-a-command)) qui correspondent à ce que vous avez déjà tapé, avec l'argument que vous êtes en train de taper mis en évidence et sa description,
- ce que la commande renvoie,
- un avertissement lorsque vous avez tapé plus d'arguments que la commande n'en prend, ou lorsqu'aucune commande ne porte ce nom.

![L'aide de /search-replace-lore-entries pendant la saisie de son second argument](../../images/command-help.png)

Tant que vous êtes encore en train de taper le nom, la liste des commandes sert d'aide.

## Arguments

Les arguments sont séparés par des espaces, et des espaces répétés comptent pour un seul. Pour mettre un espace *à l'intérieur* d'un argument, vous disposez de trois moyens :

| Vous tapez                   | La commande reçoit              | Pourquoi                                   |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | un argument, `two words`        | `\ ` (antislash, espace) est un espace     |
| `/search-parts "two words"`  | un argument, `two words`        | les guillemets doubles gardent tout ensemble |
| `/search-parts say\ "hi"`    | un argument, `say hi`           | les guillemets peuvent commencer au milieu d'un argument |

Les autres caractères spéciaux :

| Vous tapez                    | Vous obtenez               |
|-------------------------------|----------------------------|
| `\\`                          | un antislash               |
| `\"`                          | un guillemet double, avec ou sans guillemets autour : `"say \"hi\""` donne `say "hi"` |
| `""`                          | un argument vide           |

Un antislash placé devant tout autre caractère reste tel quel, de sorte que `C:\books\story` est transmis sans changement. Si vous ouvrez un guillemet sans le refermer, tout ce qui suit jusqu'à la fin de la ligne appartient à cet argument.

Les arguments sont du texte. Une commande qui attend un nombre, un oui/non ou l'un de quelques choix convertit elle-même le texte ; si le texte ne correspond pas, la commande n'est pas lancée et une notification le signale.

<a id="forms-of-a-command"></a>

## Formes d'une commande

Une même commande peut accepter différents arguments : `/list-parts` sans rien, et `/list-parts 2` avec un argument, sont deux **formes** de la même commande. Marginalia choisit la forme d'après le nombre d'arguments tapés et d'après leur aspect. Si aucune forme ne convient, une notification indique que la commande ne prend pas ces arguments, et l'aide montre les formes qu'elle possède.

<a id="the-result"></a>

## Le résultat

Lorsqu'une commande renvoie quelque chose, cela s'affiche dans une fenêtre au-dessus de la ligne de commande. Fermez-la avec la croix, avec **Esc**, ou continuez simplement : elle disparaît quand vous tapez quelque chose de nouveau ou lancez une autre commande. Une commande qui ne renvoie rien n'affiche aucune fenêtre.

![Le résultat de /take-backup au-dessus de la ligne de commande](../../images/command-result.png)

Si une commande échoue, ou si elle n'existe pas, une notification vous en donne la raison.

<a id="commands-that-take-time"></a>

## Commandes qui prennent du temps

La plupart des commandes répondent aussitôt. Une commande qui a besoin de temps affiche une fenêtre avec une barre animée et un bouton **Arrêter**. La fenêtre se place sur le côté et la page reste utilisable ; son ⌄ la replie dans le [panneau des exécutions](../books/story-editor.md#the-panel-of-the-runs), où la commande figure avec ce que le modèle écrit, sur toutes vos pages. Sous la barre, la commande peut indiquer ce qu'elle fait en ce moment et, en plus petit, le détail de l'étape. **Arrêter** annule la commande ; celle-ci devrait revenir immédiatement. Les commandes qui demandent au modèle d'écrire un résumé ou des notes, comme `/generate-annotation`, sont de ce type.

`/next`, `/swipe` et `/regenerate` tapés dans la barre de commande de l'histoire n'ont pas de fenêtre de ce genre : la nouvelle partie s'écrit en continu dans l'histoire comme lorsque vous appuyez sur le bouton rond, le bouton rond l'arrête, et l'histoire n'est retenue que comme pour toute génération (vous pouvez continuer à travailler dans le lore, dans les autres parties et dans les onglets latéraux). Le résultat de la commande apparaît au-dessus de la barre lorsque la partie est terminée. D'ici là, la barre refuse une seconde commande de ce type (vous gardez le texte et pouvez la lancer plus tard) ; les autres commandes fonctionnent.

![La fenêtre d'une commande en cours d'exécution](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Commandes qui modifient les données

Chaque commande indique ce qu'elle fait aux données existantes, et la barre de commande affiche cela sous la forme d'une petite étiquette à côté de la commande, dans la liste comme dans l'aide :

- **Destructrice** (rouge) - la commande modifie ou supprime des données existantes, comme les commandes de recherche et remplacement qui réécrivent le livre. Placez le pointeur sur l'étiquette pour voir l'explication.
- **Non destructrice** (vert) - la commande ne fait que lire des données ou en ajoute de nouvelles, comme `/take-backup`, qui ajoute une sauvegarde et ne change rien d'autre.

L'étiquette est une information pour vous ; elle n'empêche pas la commande d'être lancée. Un [agent](../agents.md) s'y fie : il ne reçoit pas une commande destructrice, ou doit vous demander avant, ou l'exécute, selon ce qui est réglé pour cet agent. Une commande qui demande elle-même une confirmation est décrite ci-dessous.

<a id="commands-that-ask-first"></a>

## Commandes qui demandent d'abord

Une commande qui modifie beaucoup de choses peut demander avant d'agir. Les commandes de recherche et remplacement n'ont pas d'aperçu ; après la recherche, elles affichent combien de correspondances elles ont trouvées, dans combien d'éléments, et par quoi chaque occurrence serait remplacée, et seul **Oui** modifie le livre ; **Non** laisse tout tel quel. La question reste affichée tant que vous n'y avez pas répondu.

![La question de /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Commandes fournies avec Marginalia

| Commande                              | Ce qu'elle fait                                                                    |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Recherche et remplace dans l'histoire du livre ouvert, comme la fenêtre **Rechercher et remplacer** de l'onglet de l'histoire. Les options sont `true`/`false`, dans cet ordre, et chacune que vous omettez prend la valeur par défaut de cette fenêtre ([expressions régulières](../regular-expressions.md) non, casse sensible oui, résumés oui, toutes les branches non, revalidation des résumés oui) ; pour en régler une, donnez aussi celles qui la précèdent. `maxHits` refuse un remplacement qui trouve plus de correspondances que ce nombre, et avec `dryRun` à `true` rien n'est modifié : la réponse compte les correspondances et en montre quelques, ce qui est la façon dont un agent vérifie avant de remplacer. Il n'y a pas d'aperçu : après la recherche, on vous demande si vous voulez remplacer le nombre de correspondances trouvées, et seul **Oui** modifie le livre. Le remplacement ne peut pas être interrompu. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | La même chose pour les entrées (nom, texte et commentaire) du lorebook du livre ouvert. |
| `/take-backup`                       | Fait une sauvegarde du livre ouvert ; elle apparaît dans l'onglet **Sauvegardes** du livre. Signale qu'aucun livre n'est ouvert s'il n'y en a pas. |
| `/get-book-info` | Le nom, les réglages, le modèle, les totaux (parties, mots, tokens) et les étiquettes du livre ouvert. |
| `/list-parts [branch] [offset] [limit]` | Les parties d'une branche : handle, position, taille, swipes, état du résumé et un bref aperçu. |
| `/get-part <part> [branch] [offset] [length] [field]` | Le texte d'une partie par tranches (ou ses `instructions`, `scene`, `pov`, `present` ou `reasoning`) et ce à partir de quoi elle a été produite. |
| `/list-swipes <part> [branch]` | Les alternatives (swipes) d'une partie, avec le numéro de branche qui affiche chacune. |
| `/list-branches [offset] [limit]` | Les branches du livre, numérotées, avec l'endroit où chacune se sépare des autres. |
| `/get-part-metrics <part> [branch]` | Tokens, durées, modèle et protocole d'une partie. |
| `/show-prompt <part> [branch] [offset] [length]` | Le prompt à partir duquel une partie générée a été produite, tel qu'il a été stocké. |
| `/search-parts <query> [limit]` | Recherche plein texte dans les parties de toutes les branches, avec des extraits. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Combien de fois et où des noms ou des mots apparaissent (valeurs par défaut : sans regex, casse non sensible, parties uniquement, branche active). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Les phrases qui diffèrent entre deux parties ; `0` comme `b` compare la partie `a` avec le texte produit en premier par le modèle. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Tokens (avec le tokenizer du modèle du livre), mots et caractères d'un texte, ou d'une partie lorsque le texte est son handle. |
| `/list-summaries [branch]` | Les résumés d'une branche, les parties qu'ils couvrent et s'ils sont toujours valides. |
| `/get-summary <part> [branch]` | Le résumé d'une partie. |
| `/check-summaries [branch]` | Les résumés périmés et les parties modifiées qui les ont rendus tels. |
| `/get-template-variables <scope> [part] [branch]` | Les variables de modèle du livre (`global`) ou d'une partie (`part`). |
| `/list-lorebooks` | Les lorebooks du livre ouvert (la racine et ses sous-livres) avec leurs numéros, nombres d'entrées, étiquettes et sous-livres. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Les entrées d'un lorebook avec leurs réglages et un aperçu ; le filtre ne garde que les entrées dont le nom ou une étiquette contient ce texte. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Une entrée : réglages, clés, activation (récursion, contexte, plage de parties, condition), étiquettes, commentaire et son texte par tranches. |
| `/search-lore <query> [regex] [limit]` | Les entrées de tous les lorebooks du livre dont le nom, le texte ou le commentaire contient la requête, avec des extraits. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Quelles entrées une génération utiliserait pour un texte, ou pour un tour semblable à celui qui a produit une partie, et pourquoi (pas de filtre, filtre trouvé - dans le texte, ou dans le contenu d'une autre entrée lorsque le lorebook a une [profondeur d'analyse](../lorebooks/entries.md#entries-brought-in-by-other-entries) - ou non utilisée parce qu'elle est désactivée, une étiquette du livre décide, ou le filtre n'est pas dans le texte), avec le texte de lore et ses tokens. Le texte peut être le handle d'une partie. |
| `/list-annotations [status [part [offset [limit]]]]` | Les [annotations](../books/annotations.md) du livre ou d'une partie : les ouvertes, sauf si `status` indique `resolved`, `dismissed`, `orphaned` ou `all`. |
| `/get-annotation <annotation>` | Une annotation en entier, avec le texte autour de son passage. |
| `/get-book-statistics [range] [side]` | Ce qui a été fait au livre ouvert jour par jour (tokens, mots, parties, modifications, temps, taille) : voir [Statistiques d'écriture](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | La même chose pour tous vos livres, par jour ou par livre. Ne nécessite aucun livre ouvert. |

<a id="reading-the-story"></a>

### Lire l'histoire

Les commandes ci-dessus qui lisent le livre (`/get-book-info` à `/get-template-variables`) ne modifient rien ; elles répondent en texte JSON,
donc un agent peut aussi les utiliser. Chaque ligne d'une réponse a un **`handle`** : l'identifiant de la partie (ou du lorebook, ou de l'entrée), qui
ne change pas quand des parties sont ajoutées, déplacées ou supprimées. Une partie est donnée par son handle, ou par sa **position** sur une branche, la première
partie de l'histoire étant `1` ; une branche par le handle d'une de ses parties, ou par son **numéro** dans `/list-branches` (les numéros suivent la création des extrémités des branches, donc ils changent quand une branche est ajoutée ou supprimée). Les positions et les numéros sont plus faciles à taper, les handles sont ce qu'un agent doit utiliser, et les commandes qui modifient l'histoire n'acceptent que des handles. Sans branche, les commandes travaillent sur la branche active. Les listes sont paginées avec `offset` et `limit`, les longs textes sont découpés en tranches (`offset`, `length`) et la réponse indique la longueur totale. Comme pour les autres commandes, les options sont positionnelles : pour en régler une, donnez aussi celles qui la précèdent.

Les commandes qui lisent les lorebooks (`/list-lorebooks` à `/preview-lore-activation`) fonctionnent de la même façon et n'atteignent que les lorebooks du livre ouvert : le lorebook racine et ses sous-livres. Un lorebook est désigné par son **handle** ou son **numéro** dans `/list-lorebooks` (la racine est `1`), une entrée par son **handle** ou son **numéro dans le lorebook**, dans l'ordre du lorebook (la première entrée est `1`) ; les numéros changent quand des entrées ou des sous-livres sont ajoutés ou retirés. Sans lorebook indiqué, c'est le lorebook racine qui est utilisé.
<a id="changing-the-story-and-the-lore"></a>

### Modification de l'histoire et du lore

Ces commandes modifient le livre ouvert (ou la copie de travail d'un agent, où vont les modifications d'un agent). Une partie, un lorebook ou une entrée est toujours désigné par son **identifiant**, jamais par une position ou un numéro : ceux-ci changent lorsqu'un élément est ajouté ou supprimé, et une position juste au moment de la lecture peut désigner une autre partie au moment de l'écriture. Les options restent positionnelles, comme précédemment. Une commande qui supprime du texte ou des données est `DESTRUCTIVE` et demande confirmation (pour un agent, c'est son mode destructif qui s'applique) ; l'ajout et la modification ne demandent pas de confirmation. Une modification de l'histoire peut rendre les résumés obsolètes (un résumé porte l'empreinte de l'histoire qu'il décrit) : la réponse les liste et indique quoi faire, `/revalidate-summaries` s'ils sont toujours exacts, `/regenerate-summary` sinon.

| Commande | Ce qu'elle fait |
|----------|-----------------|
| `/edit-part <part> <ancien> <nouveau> [champ]` | Remplace `ancien` par `nouveau` dans le texte d'une partie ; `ancien` doit être trouvé exactement une fois, sinon rien ne change et l'erreur indique le nombre d'occurrences trouvées. `champ` modifie à la place les `instructions`, `scene`, `pov` ou `present` tour du tour. |
| `/replace-part-text <part> <texte> [champ]` | Remplace tout le texte d'une partie. Demande d'abord confirmation. |
| `/append-to-part <part> <texte> [séparateur]` | Ajoute du texte à la fin d'une partie, après une ligne vide sauf si `séparateur` indique autre chose. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Modifie ce dont une partie a été générée, que réutilise une nouvelle génération de cette partie ; un texte vide efface un champ, un champ omis reste inchangé. |
| `/create-part <texte> [parent] [instructions] [scene] [pov] [present]` | Écrit une nouvelle partie après la dernière partie de la branche active, ou après `parent` (une nouvelle branche) ; elle devient la fin de la branche active. |
| `/insert-part <texte> <part> [avant]` | Insère une nouvelle partie dans l'histoire, juste après `part` (ou juste avant lorsque `avant` vaut `true`). Tout ce qui suivait cet endroit, toutes branches comprises, suit désormais la nouvelle partie ; rien n'est supprimé ni généré à nouveau, donc les autres parties conservent leurs résumés et leurs données et descendent d'une position. Les résumés qui couvraient cet endroit deviennent obsolètes (la réponse indique lesquels). En fin de branche, c'est équivalent à `/create-part`. |
| `/branch-part <part>` | Copie une partie (et son résumé) à côté d'elle, une nouvelle variante qui démarre une branche, et fait de la copie la fin active. |
| `/delete-part <part>` | Supprime une partie et son résumé ; les parties qui la suivaient restent. Demande d'abord confirmation. |
| `/switch-branch <part>` | Rend active la branche passant par une partie. |
| `/set-summary <part> <texte>` | Remplace le texte du résumé d'une partie. |
| `/remove-summary <part> [unwind]` | Supprime le résumé d'une partie (`unwind` rétablit ce qu'un méta-résumé avait remplacé). Demande d'abord confirmation. |
| `/revalidate-summaries [part]` | Indique que les résumés obsolètes décrivent toujours l'histoire (toute la branche active, ou seulement celui de cette partie). À utiliser seulement après vérification. |
| `/regenerate-summary <part>` | Demande au modèle de refaire le résumé d'une partie. Lent, consomme des jetons. |
| `/create-meta-summary <from> <to>` | Demande au modèle de fusionner en un seul les résumés allant de la partie `from` (la plus récente) jusqu'à `to`. Lent, consomme des jetons. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Demande au modèle d'écrire la partie suivante, comme le bouton **New turn**. Lent, consomme des jetons ; une seule génération d'un livre s'exécute à la fois, les autres attendent. `profile` désigne l'un des profils de modèle d'un agent. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Demande au modèle d'écrire une autre version de la dernière partie d'une branche. Ce qui est omis est repris de la partie. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Demande au modèle de réécrire la dernière partie d'une branche, en remplaçant son texte. Demande d'abord confirmation. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Demande au modèle de réécrire n'importe quelle partie à partir de vos instructions (voir [Reformuler une partie](../books/story-editor.md#redrafting-a-part)) et remplace son texte. `prompt` est un prompt de reformulation à utiliser à la place de celui du livre, `profile` un profil de modèle d'un agent, `apply` `false` ne fait que renvoyer le texte. `DESTRUCTIVE` ; demande d'abord confirmation. Lent, consomme des jetons. |
| `/impersonate [instructions] [outline] [prompt]` | Demande au modèle de rédiger à votre place les instructions du tour suivant (voir [Rédiger les instructions](../books/story-editor.md#drafting-the-instructions)) et renvoie le texte ; rien n'est modifié. `instructions` indique de quoi doit parler la partie (vide : la suite de l'histoire s'enchaîne d'elle-même), `outline` un plan de l'histoire destiné au modèle, `prompt` un prompt d'impersonation à utiliser à la place de celui du livre. Réservé à vous, à la barre de commande : un [agent](../agents.md) ne le reçoit pas. Lent, consomme des jetons. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Modifie les réglages descriptifs du livre ; seules les valeurs que vous donnez changent. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Remplace les modèles de prompt du livre. Chacun est vérifié d'abord. `DESTRUCTIVE` : un mauvais modèle gâche toutes les générations suivantes. |
| `/set-book-tags [add] [remove]` | Ajoute et retire des étiquettes du livre (séparées par des virgules). |
| `/set-book-history-limit [maxVersions]` | Fixe le nombre de versions d'une partie que conserve l'[historique](../books/version-history.md#how-many-versions-are-kept) du livre (`0` n'en conserve plus aucune à partir de maintenant) ; sans nombre, le livre ne fixe aucune limite. Les limites des dossiers, de l'utilisateur et de l'administrateur s'appliquent toujours. Ne supprime aucune version. |
| `/create-lore-entry <nom> <contenu> [lorebook] [commentaire] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Ajoute une entrée à un lorebook (la racine si aucun n'est donné ; un livre sans lorebook en reçoit un, nommé comme le livre). `keys` est une liste de mots-clés séparés par des virgules ; `filteringMode` vaut `KEYWORDS_OR` (par défaut), `KEYWORDS_AND`, `TEXT` ou `REGEX`. Un problème du modèle ou de l'expression régulière est enregistré et signalé. |
| `/update-lore-entry <entrée> [nom] [contenu] [commentaire] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Modifie les champs que vous donnez. |
| `/delete-lore-entry <entrée>` | Supprime une entrée. Demande d'abord confirmation. |
| `/move-lore-entry <entrée> <lorebook>` | Déplace une entrée vers un autre lorebook du livre. |
| `/set-lore-entry-tags <entrée> [add] [remove] [addNegative] [removeNegative]` | Modifie les étiquettes et les étiquettes négatives d'une entrée (séparées par des virgules). |
| `/set-lore-entry-activation <entrée> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Modifie le moment où une entrée est utilisée : `preventRecursion`, `recursionOnly` et `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` ou `INCLUDE_PARTS`) avec `activationParts`, la plage de parties `fromMessage` / `toMessage` (0 supprime une limite) et le modèle `condition` (un texte vide le supprime). Une valeur donnée sous forme de texte vide reste inchangée (sauf `condition`). |
| `/create-lorebook <nom> [parent]` | Crée un lorebook qui est un sous-livre de `parent` (la racine si aucun n'est donné ; un livre sans lorebook en reçoit d'abord un). |
| `/set-lorebook <lorebook> [nom] [enabled]` | Renomme un lorebook ou l'active ou le désactive. |
| `/add-subbook <lorebook> <sous-livre>`, `/remove-subbook <lorebook> <sous-livre>` | Modifie les lorebooks contenus dans un lorebook ; un lorebook ne peut pas se contenir lui-même. Pour un [agent](../agents.md), `/add-subbook` rétablit aussi un lorebook qu'il avait supprimé ou retiré de son parent auparavant (on le désigne par l'identifiant qu'il avait), avec ses entrées. |
| `/delete-lorebook <lorebook>` | Supprime un lorebook avec ses entrées (pas la racine). Demande d'abord confirmation. |
### Annotations

Les commandes qui lisent les [annotations](../books/annotations.md) (`/list-annotations`, `/get-annotation`) répondent en JSON, comme les autres commandes de lecture ;
une annotation est désignée par son **handle**, une partie par son handle pour les commandes qui modifient quelque chose. Ces commandes ne modifient que les notes,
jamais l'histoire, donc seule la suppression demande une confirmation. Un agent peut utiliser toutes ces commandes ; ses notes atteignent votre livre lorsque vous [examinez le résultat](../books/annotations.md#annotations-and-agents).

| Commande | Ce qu'elle fait |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | Ajoute une note à une partie. `quote` est le passage du texte, copié depuis la partie : la note se place sur ce passage et le suit lorsque le texte est modifié. Sans citation, ou avec une citation absente du texte, la note porte sur la partie entière (la réponse l'indique). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Modifie la note, le type ou l'envoi de la note au modèle ; un texte ou un type vide reste inchangé. |
| `/resolve-annotation <annotation> [status]` | Définit le statut : `resolved` (par défaut), `dismissed` ou `open`. |
| `/reattach-annotation <annotation> [quote]` | Déplace une note vers le passage que la citation trouve dans sa partie ; sans citation, la note porte sur la partie entière. |
| `/delete-annotation <annotation>` | Supprime une note (elle peut être restaurée depuis la corbeille). Demande une confirmation. |
| `/generate-annotation <part> [profile]` | Demande au modèle d'examiner la partie comme [Annoter avec l'IA](../books/annotations.md#letting-a-model-annotate) et ajoute tout ce qu'il trouve comme notes (source *AI*; une citation absente du texte donne une note sur la partie entière). Rien n'est demandé : les notes sont la seule modification, et elles peuvent être résolues, modifiées ou supprimées. Il examine avec le modèle du livre ; un agent utilise son propre modèle lorsque *Écrire le livre avec le modèle de l'agent* est activé (comme le font les commandes du Lore Generator), `profile` est un profil de modèle d'un agent. S'exécute seule, prend du temps, consomme des tokens. |

<a id="the-planner"></a>

### Le planificateur

Les commandes du [planificateur](../books/story-editor.md#the-planner-tab) du livre. Une scène est désignée par son **numéro** dans la liste (à partir de 1, `/get-planned-scenes`
les affiche) ; les numéros changent lorsqu'une scène est déplacée ou supprimée, relisez donc la liste après une modification. Elles ne modifient que le planificateur, jamais l'histoire. L'ajout,
le déplacement et le marquage d'une scène comme terminée ne demandent pas de confirmation ; la modification du texte d'une scène et sa suppression sont destructives (un [agent](../agents.md) demande confirmation comme pour les autres
commandes destructives).

| Commande | Ce qu'elle fait |
|---------|--------------|
| `/get-planned-scenes` | Les scènes avec leur numéro, titre, cadre, personnage POV, personnages présents, instructions et indication de leur achèvement. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Ajoute une scène à la fin de la liste. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Modifie le titre et les détails fournis ; ce qui est omis reste, un texte vide l'efface. |
| `/set-planned-scene-done <scene> [done]` | Marque la scène comme écrite (`done` vaut `true` par défaut) ou non écrite. |
| `/move-planned-scene <scene> <to>` | Déplace la scène à la place `to` (1 est la première place, la dernière si elle est trop grande). |
| `/delete-planned-scene <scene>` | Supprime la scène. |

Les commandes qui demandent au modèle d'écrire ou d'examiner prennent du temps : `/generate-annotation` et les deux résumés affichent la fenêtre **Arrêter**,
`/next`, `/swipe` et `/regenerate` s'écrivent en continu dans l'histoire (voir [Commandes qui prennent du temps](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Commandes fournies par les extensions

Vous les voyez dans la liste tant que l'extension est chargée ; chacune est décrite dans la documentation de son extension.

| Commande | Extension | Ce qu'elle fait |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | Vos prompts de génération enregistrés, ou le texte de l'un d'eux. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Génère le texte d'une entrée de lore avec le lore du lorebook, le compacte (sauf si `compact` vaut `false`) et le renvoie. N'enregistre rien. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Compacte un texte généré en une entrée de lore dense et le renvoie. N'enregistre rien. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Les catégories de noms inventés, et des noms de l'une d'elles. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Les listes de vrais noms, et des noms tirés de l'une d'elles. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | Les types de lieux réels, une recherche par nom, type et pays, les lieux autour d'un point, et la distance entre deux lieux. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Les arcs et les chapitres du plan. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Ajouter, modifier, déplacer et supprimer un arc (`<arc>` est son numéro). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | La même chose pour les chapitres d'un arc. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Les générateurs de l'onglet Arcs. Ils renvoient le résultat et ne l'ajoutent au plan qu'avec `save`. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | Les personnages du livre, et l'un d'eux avec sa note et ses relations. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Ajouter, modifier et supprimer un personnage. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Les relations entre personnages. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Les pages des notes du livre (pas celles masquées aux assistants), une page avec son texte et ses liens, et une recherche. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Ajouter, compléter, modifier, éditer et supprimer une page ; `note-text` est fait pour la macro {%{`{{command::note-text::Title}}`}%}. |

Les commandes du Lore Generator, du Name generator, du Name provider et du Place provider ne font que lire : celles qui génèrent renvoient du texte et
laissent à vous, ou à l'agent qui les a appelées, le soin de le placer dans une entrée. Les commandes de l'Arc Outline et du
Character Tracker modifient aussi le plan et les personnages du livre ouvert ; seules leurs commandes `delete` sont destructives. Les Story Notes
modifient aussi les pages du livre ouvert ; là, `update-note` et `edit-note` sont également destructives, puisqu'elles remplacent un texte existant.

<a id="limits"></a>

## Limites

- La ligne de commande se trouve dans l'onglet histoire de la disposition bureau. La [vue téléphone](../phone.md) ne l'a pas encore.
- Une commande s'exécute en tant que vous, dans votre session, et travaille avec le livre que vous avez ouvert.
