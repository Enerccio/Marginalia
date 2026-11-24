---
label: Commandes
order: 945
icon: terminal
---

# Commandes

Les commandes sont de petites actions que vous lancez en les tapant, au lieu de passer par des menus : chercher quelque chose dans l'histoire ou dans les lorebooks, rechercher et remplacer, faire une sauvegarde. Les [extensions](../extensions/index.md) ajoutent leurs propres commandes, par exemple pour générer une entrée de lore ou pour inventer des noms. Ce sont les mêmes commandes qu'un [agent](../agents.md) utilise. Chaque commande fait donc une seule chose précise et dit ce qu'elle fait : vous pouvez le lire pendant que vous tapez.

La **ligne de commande** est le champ situé à côté du menu, sous l'histoire, dans l'[éditeur de l'histoire](../books/story-editor.md).

<a id="running-a-command"></a>

## Lancer une commande

1. Cliquez dans la ligne de commande et tapez `/`. La liste affiche toutes les commandes, chacune avec son nom et une courte description. Taper davantage réduit la liste : le filtre examine le nom de la commande, son titre et sa description.
2. Choisissez la commande avec la souris, ou avec les flèches et **Enter**. Une commande qui n'a besoin de rien s'exécute immédiatement. Pour une commande qui prend des arguments, `/name ` est écrit dans le champ et vous continuez avec les arguments.
![La liste des commandes, ouverte en tapant une barre oblique](../../images/command-list.png)

3. Tapez les arguments, puis appuyez sur **Enter** ou cliquez sur le bouton de lecture à côté du champ pour lancer la commande. Le champ est vidé dès que la commande démarre. Un texte qui n'est pas une commande reste en place, pour que vous puissiez le corriger.

Un texte qui ne commence pas par `/` n'est pas une commande, et rien n'est exécuté. Cliquer ailleurs que dans le champ ou appuyer sur **Esc** n'exécute jamais rien : vous ne pouvez donc pas lancer une commande par accident.

<a id="the-help"></a>

## L'aide

En tapant, une petite fenêtre s'ouvre au-dessus du champ. Elle recouvre une partie de l'histoire sans rien déplacer, et elle se ferme lorsque vous quittez le champ ou lancez la commande. Elle affiche :

- ce que fait la commande,
- les formes de la commande (voir [Formes](#forms-of-a-command)) qui correspondent à ce que vous avez tapé jusque-là, avec l'argument que vous êtes en train de taper mis en évidence et sa description,
- ce que la commande renvoie,
- un indice lorsque vous avez tapé plus d'arguments que la commande n'en accepte, ou lorsqu'aucune commande ne porte ce nom.

![L'aide de /search-replace-lore-entries pendant la saisie de son second argument](../../images/command-help.png)

Tant que vous tapez encore le nom, la liste des commandes sert d'aide.

## Arguments

Les arguments sont séparés par des espaces, et des espaces répétés comptent pour un seul. Pour mettre un espace *à l'intérieur* d'un argument, vous disposez de trois moyens :

| Vous tapez                   | La commande reçoit              | Pourquoi                                   |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | un argument, `two words`       | `\ ` (antislash, espace) est un espace     |
| `/search-parts "two words"`  | un argument, `two words`       | les guillemets doubles gardent tout ensemble |
| `/search-parts say\ "hi"`    | un argument, `say hi`          | les guillemets peuvent commencer au milieu d'un argument |

Les autres caractères spéciaux :

| Vous tapez                    | Vous obtenez               |
|-------------------------------|----------------------------|
| `\\`                          | un antislash               |
| `\"`                          | un guillemet double, avec ou sans guillemets autour : `"say \"hi\""` donne `say "hi"` |
| `""`                          | un argument vide           |

Un antislash placé devant tout autre caractère reste tel quel : `C:\books\story` est transmis sans changement. Si vous ouvrez un guillemet sans jamais le refermer, tout ce qui suit jusqu'à la fin de la ligne appartient à cet argument.

Les arguments sont du texte. Une commande qui attend un nombre, un oui ou non, ou l'un de quelques choix convertit elle-même le texte. Si le texte ne correspond pas, la commande n'est pas exécutée et une notification le signale.

<a id="forms-of-a-command"></a>

## Formes d'une commande

Une même commande peut prendre des arguments différents : `/list-parts` sans argument et `/list-parts 2` avec un argument sont deux **formes** de la même commande. Marginalia choisit la forme d'après le nombre d'arguments tapés et d'après leur aspect. Si aucune forme ne convient, une notification indique que la commande n'accepte pas ces arguments, et l'aide montre les formes qu'elle possède.

<a id="the-result"></a>

## Le résultat

Lorsqu'une commande renvoie quelque chose, le résultat s'affiche dans une fenêtre au-dessus de la ligne de commande. Fermez-la avec la croix, avec **Esc**, ou continuez simplement : elle disparaît lorsque vous tapez autre chose ou lancez une autre commande. Une commande qui ne renvoie rien n'affiche aucune fenêtre.

![Le résultat de /take-backup au-dessus de la ligne de commande](../../images/command-result.png)

Si une commande échoue, ou s'il n'existe aucune commande de ce nom, une notification vous en donne la raison.

<a id="commands-that-take-time"></a>

## Commandes qui prennent du temps

La plupart des commandes répondent immédiatement. Une commande qui a besoin de temps affiche une fenêtre avec une barre animée et un bouton **Stop**. La fenêtre se place sur le côté et la page reste utilisable. Son ⌄ la replie dans le [panneau des exécutions](../books/story-editor.md#the-panel-of-the-runs), où la commande est listée avec ce que le modèle écrit, sur toutes vos pages. Sous la barre, la commande peut indiquer ce qu'elle fait en ce moment et, en plus petit, le détail de l'étape. **Stop** annule la commande, qui doit alors revenir immédiatement. Les commandes qui font écrire au modèle un résumé ou des notes, comme `/generate-annotation`, sont de ce type.

`/next`, `/swipe` et `/regenerate`, tapées dans la barre de commandes de l'histoire, n'ont pas de fenêtre de ce genre : la nouvelle partie s'écrit en continu dans l'histoire, comme lorsque vous appuyez sur le bouton rond. Ce bouton rond l'arrête, et l'histoire est mise en attente comme pour toute génération (vous pouvez continuer à travailler dans le lore, les autres parties et les onglets latéraux). Le résultat de la commande apparaît au-dessus de la barre lorsque la partie est terminée. Jusque-là, la barre refuse une deuxième de ces commandes (vous gardez le texte et pouvez la lancer plus tard). Les autres commandes fonctionnent.

![La fenêtre d'une commande en cours d'exécution](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Commandes qui modifient les données

Chaque commande indique ce qu'elle fait aux données existantes, et la barre de commandes affiche une petite étiquette à côté de la commande, dans la liste et dans l'aide :

- **Destructive** (rouge) - la commande modifie ou supprime des données existantes, comme les commandes de recherche et remplacement qui réécrivent le livre. Pointez l'étiquette pour voir l'explication.
- **Non-destructive** (vert) - la commande ne fait que lire des données ou en ajouter de nouvelles, comme `/take-backup`, qui ajoute une sauvegarde sans rien changer d'autre.

L'étiquette vous informe ; elle n'empêche pas la commande de s'exécuter lorsque vous la lancez. Un [agent](../agents.md) s'y fie : il ne reçoit pas de commande destructive, ou doit vous demander d'abord, ou l'exécute, selon ce qui est réglé pour cet agent. Une commande qui demande elle-même une confirmation est décrite ci-dessous.

<a id="commands-that-ask-first"></a>

## Commandes qui demandent d'abord

Une commande qui change beaucoup de choses peut demander confirmation avant d'agir. Les commandes de recherche et remplacement n'ont pas d'aperçu : après la recherche, elles affichent le nombre de correspondances trouvées, dans combien d'éléments, et par quoi chacune serait remplacée. Seul **Yes** modifie le livre, **No** laisse tout tel quel. La question reste affichée tant que vous n'avez pas répondu.

![La question de /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Commandes fournies avec Marginalia

| Commande                             | Ce qu'elle fait                                                                    |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Recherche et remplacement dans l'histoire du livre ouvert, comme la fenêtre **Search and replace** de l'onglet de l'histoire. Les options sont `true`/`false`, dans cet ordre ; chaque option omise prend la valeur par défaut de cette fenêtre ([expression régulière](../regular-expressions.md) non, sensible à la casse oui, résumés oui, toutes les branches non, revalider oui) ; pour en fixer une, donnez aussi celles qui la précèdent. `maxHits` refuse un remplacement qui trouve plus de correspondances que ce nombre, et avec `dryRun` `true` rien n'est modifié : la réponse compte les correspondances et en montre quelques-unes, ce qui permet à un agent de vérifier avant de remplacer. Il n'y a pas d'aperçu : après la recherche, on vous demande si vous voulez remplacer le nombre de correspondances trouvées, et seul **Yes** modifie le livre. Le remplacement ne peut pas être interrompu. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Idem, pour les entrées (nom, texte et commentaire) du lorebook du livre ouvert. |
| `/take-backup`                                 | Fait une sauvegarde du livre ouvert ; elle apparaît dans l'onglet **Backups** du livre. Indique qu'aucun livre n'est ouvert s'il n'y en a pas. |
| `/get-book-info` | Le nom, les réglages, le modèle, les totaux (parties, mots, jetons) et les étiquettes du livre ouvert. |
| `/list-parts [branch] [offset] [limit]` | Les parties d'une branche : identifiant, position, taille, variantes (swipes), état des résumés et un court aperçu. |
| `/get-part <part> [branch] [offset] [length] [field]` | Le texte d'une partie par tranches (ou son `instructions`, `scene`, `pov`, `present` ou `reasoning`) et ce à partir de quoi elle a été faite. |
| `/list-swipes <part> [branch]` | Les variantes (swipes) d'une partie, avec le numéro de branche qui indique chacune. |
| `/list-branches [offset] [limit]` | Les branches du livre, numérotées, avec l'endroit où chacune se détache des autres. |
| `/get-part-metrics <part> [branch]` | Jetons, durées, modèle et protocole d'une partie. |
| `/show-prompt <part> [branch] [offset] [length]` | Le prompt à partir duquel une partie générée a été faite, tel qu'il a été enregistré. |
| `/search-parts <query> [limit]` | Recherche plein texte dans les parties de toutes les branches, avec des extraits. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Fréquence et lieux d'apparition des noms ou des mots (valeurs par défaut : sans expression régulière, insensible à la casse, parties seulement, branche active). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Les phrases qui diffèrent entre deux parties ; `0` comme `b` compare la partie `a` avec le texte produit d'abord par le modèle. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Jetons (avec le tokenizer du modèle du livre), mots et caractères d'un texte, ou d'une partie lorsque le texte est son identifiant. |
| `/list-summaries [branch]` | Les résumés d'une branche, les parties qu'ils couvrent et s'ils sont encore valides. |
| `/get-summary <part> [branch]` | Le résumé d'une partie. |
| `/check-summaries [branch]` | Les résumés périmés et les parties modifiées qui les ont rendus tels. |
| `/get-template-variables <scope> [part] [branch]` | Les variables de modèle du livre (`global`) ou d'une partie (`part`). |
| `/list-lorebooks` | Les lorebooks du livre ouvert (la racine et ses sous-livres) avec leurs numéros, nombres d'entrées, étiquettes et sous-livres. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Les entrées d'un lorebook avec leurs réglages et un aperçu ; le filtre garde les entrées dont le nom ou l'étiquette le contient. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Une entrée : réglages, clés, activation (récursion, contexte, plage de parties, condition), étiquettes, commentaire et son texte par tranches. |
| `/search-lore <query> [regex] [limit]` | Les entrées de tous les lorebooks du livre dont le nom, le texte ou le commentaire contiennent la requête, avec des extraits. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Quelles entrées une génération utiliserait pour un texte, ou pour un tour semblable à celui qui a produit une partie, et pourquoi (pas de filtre, filtre trouvé dans le texte, ou dans le contenu d'une autre entrée lorsque le lorebook a une [profondeur d'analyse](../lorebooks/entries.md#entries-brought-in-by-other-entries), ou non utilisée parce qu'elle est désactivée, qu'une étiquette du livre décide, ou que le filtre n'est pas dans le texte), avec le texte de lore et ses jetons. Le texte peut être l'identifiant d'une partie. |
| `/list-annotations [status [part [offset [limit]]]]` | Les [annotations](../books/annotations.md) du livre ou d'une partie : les ouvertes, sauf si `status` dit `resolved`, `dismissed`, `orphaned` ou `all`. |
| `/get-annotation <annotation>` | Une annotation en entier, avec le texte autour de son passage. |
| `/get-book-statistics [range] [side]` | Ce qui a été fait sur le livre ouvert jour après jour (jetons, mots, parties, modifications, durée, taille) : voir [Statistiques d'écriture](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | La même chose pour tous vos livres, par jour ou par livre. Ne nécessite aucun livre ouvert. |

<a id="reading-the-story"></a>

### Lire l'histoire

Les commandes ci-dessus qui lisent le livre (`/get-book-info` à `/get-template-variables`) ne modifient rien ; elles répondent en texte JSON, de sorte qu'un agent peut aussi les utiliser. Chaque ligne d'une réponse a un **`handle`** : l'identifiant de la partie (ou du lorebook, ou de l'entrée), qui ne change pas lorsque des parties sont ajoutées, déplacées ou supprimées. Une partie est donnée par son identifiant, ou par sa **position** sur une branche, la première partie de l'histoire étant `1` ; une branche par l'identifiant de l'une quelconque de ses parties, ou par son **numéro** à partir de `/list-branches` (les numéros suivent la création des extrémités des branches, donc ils changent lorsqu'une branche est ajoutée ou supprimée). Les positions et les numéros sont plus faciles à taper, mais les identifiants sont ce qu'un agent doit utiliser, et les commandes qui modifient l'histoire n'acceptent que des identifiants. Sans branche, les commandes travaillent sur la branche active. Les listes sont paginées avec `offset` et `limit`, les longs textes sont découpés en tranches (`offset`, `length`) et la réponse indique la longueur totale. Comme pour les autres commandes, les options sont positionnelles : pour en fixer une, donnez aussi celles qui la précèdent.

Les commandes qui lisent les lorebooks (`/list-lorebooks` à `/preview-lore-activation`) fonctionnent de la même façon et n'atteignent que les lorebooks du livre ouvert : le lorebook racine et ses sous-livres. Un lorebook est donné par son **identifiant** ou par son **numéro** à partir de `/list-lorebooks` (la racine est `1`), et une entrée par son **identifiant** ou par son **numéro dans le lorebook**, dans l'ordre du lorebook (la première entrée est `1`) ; les numéros changent lorsque des entrées ou des sous-livres sont ajoutés ou retirés. Sans lorebook, le lorebook racine est utilisé.
<a id="changing-the-story-and-the-lore"></a>

### Modifier l'histoire et le lore

Ces commandes modifient le livre ouvert (ou la copie de travail d'un agent, où vont les changements d'un agent). Une partie, un lorebook ou une entrée se désigne toujours par son **handle**, jamais par une position ou un numéro : ces valeurs changent quand un élément est ajouté ou supprimé, et une position juste au moment de la lecture peut désigner une autre partie au moment de l'écriture. Les options restent positionnelles, comme avant. Une commande qui supprime du texte ou des données est `DESTRUCTIVE` et vous demande de confirmer (pour un agent, c'est son mode destructif qui s'applique à la place) ; ajouter et modifier ne le sont pas. Une modification de l'histoire peut rendre des résumés obsolètes (un résumé porte le hash de l'histoire qu'il décrit) : la réponse les liste et indique quoi faire, `/revalidate-summaries` s'ils sont toujours justes, `/regenerate-summary` sinon.

| Commande | Ce qu'elle fait |
|----------|-----------------|
| `/edit-part <part> <old> <new> [field]` | Remplace `old` par `new` dans le texte d'une partie ; `old` doit être trouvé exactement une fois, sinon rien ne change et l'erreur indique combien de fois il a été trouvé. `field` modifie à la place les caractères `instructions`, `scene`, `pov` ou `present` du tour. |
| `/replace-part-text <part> <text> [field]` | Remplace tout le texte d'une partie. Demande d'abord confirmation. |
| `/append-to-part <part> <text> [separator]` | Ajoute du texte à la fin d'une partie, après une ligne vide sauf si `separator` indique le contraire. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Modifie ce à partir de quoi une partie a été créée, ce que réutilise une nouvelle génération de celle-ci ; un texte vide efface un champ, un champ omis est conservé. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Écrit une nouvelle partie après la dernière partie de la branche active, ou après `parent` (une nouvelle branche) ; elle devient la fin de la branche active. |
| `/insert-part <text> <part> [before]` | Insère une nouvelle partie dans l'histoire juste après `part` (ou juste avant lorsque `before` vaut `true`). Tout ce qui suivait cet emplacement, toutes branches comprises, suit maintenant la nouvelle partie ; rien n'est supprimé ni regénéré, donc les autres parties conservent leurs résumés et leurs données et descendent d'une position. Les résumés qui couvraient cet emplacement deviennent obsolètes (la réponse indique lesquels). À la fin de la branche, c'est la même chose que `/create-part`. |
| `/branch-part <part>` | Copie une partie (et son résumé) à côté d'elle, en créant une nouvelle variante qui démarre une branche, et fait de la copie la fin active. |
| `/delete-part <part>` | Supprime une partie et son résumé ; les parties qui la suivaient restent. Demande d'abord confirmation. |
| `/switch-branch <part>` | Rend active la branche qui passe par une partie. |
| `/set-summary <part> <text>` | Remplace le texte du résumé d'une partie. |
| `/remove-summary <part> [unwind]` | Supprime le résumé d'une partie (`unwind` rétablit ce qu'un méta-résumé avait remplacé). Demande d'abord confirmation. |
| `/revalidate-summaries [part]` | Indique que les résumés obsolètes décrivent toujours l'histoire (toute la branche active, ou seulement celui de cette partie). À utiliser uniquement après vérification. |
| `/regenerate-summary <part>` | Demande au modèle de refaire le résumé d'une partie. Lent, consomme des tokens. |
| `/create-meta-summary <from> <to>` | Demande au modèle de fusionner en un seul les résumés allant de la partie `from` (la plus récente) jusqu'à `to`. Lent, consomme des tokens. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Demande au modèle d'écrire la partie suivante, comme le bouton **New turn**. Lent, consomme des tokens ; une seule génération d'un livre s'exécute à la fois, les autres attendent. `profile` désigne l'un des profils de modèle d'un agent. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Demande au modèle d'écrire une autre version de la dernière partie d'une branche. Ce qui est omis est repris de la partie. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Demande au modèle de réécrire la dernière partie d'une branche, en remplaçant son texte. Demande d'abord confirmation. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Demande au modèle de réécrire n'importe quelle partie à partir de vos instructions (voir [Réécrire une partie](../books/story-editor.md#redrafting-a-part)) et remplace son texte. `prompt` est un prompt de réécriture à utiliser à la place de celui du livre, `profile` un profil de modèle d'un agent, `apply` `false` se contente de renvoyer le texte. `DESTRUCTIVE` ; demande d'abord confirmation. Lent, consomme des tokens. |
| `/impersonate [instructions] [outline] [prompt]` | Demande au modèle de rédiger à votre place les instructions du prochain tour (voir [Rédiger les instructions](../books/story-editor.md#drafting-the-instructions)) et renvoie le texte ; rien n'est modifié. `instructions` est ce sur quoi doit porter la partie (vide : l'histoire continue là où elle mène), `outline` un plan de l'histoire destiné au modèle, `prompt` un prompt d'imitation à utiliser à la place de celui du livre. Réservé à vous, dans la barre de commandes : un [agent](../agents.md) ne la reçoit pas. Lent, consomme des tokens. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Modifie les réglages descriptifs du livre ; seules les valeurs que vous donnez sont changées. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Remplace les modèles de prompt du livre. Chacun est vérifié d'abord. `DESTRUCTIVE` : un mauvais modèle gâche toutes les générations suivantes. |
| `/set-book-tags [add] [remove]` | Ajoute et retire des étiquettes du livre (séparées par des virgules). |
| `/set-book-history-limit [maxVersions]` | Définit combien de versions d'une partie conserve l'[historique](../books/version-history.md#how-many-versions-are-kept) du livre (`0` n'en garde aucune à partir de maintenant) ; sans nombre, le livre ne fixe aucune limite. Les limites des dossiers, de l'utilisateur et de l'administrateur s'appliquent toujours. Ne supprime aucune version. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Ajoute une entrée à un lorebook (la racine si aucun n'est donné ; un livre sans lorebook en reçoit un, nommé comme le livre). `keys` est une liste de mots-clés séparés par des virgules ; `filteringMode` vaut `KEYWORDS_OR` (par défaut), `KEYWORDS_AND`, `TEXT` ou `REGEX`. Un problème du modèle ou de l'expression régulière est enregistré et signalé. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Modifie les champs que vous donnez. |
| `/delete-lore-entry <entry>` | Supprime une entrée. Demande d'abord confirmation. |
| `/move-lore-entry <entry> <lorebook>` | Déplace une entrée vers un autre lorebook du livre. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Modifie les étiquettes et les étiquettes négatives d'une entrée (séparées par des virgules). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Modifie le moment où une entrée est utilisée : `preventRecursion`, `recursionOnly` et `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` ou `INCLUDE_PARTS`) avec `activationParts`, la plage de parties `fromMessage` / `toMessage` (0 supprime une limite) et le modèle `condition` (un texte vide le supprime). Une valeur donnée sous forme de texte vide est laissée telle quelle (sauf `condition`). |
| `/create-lorebook <name> [parent]` | Crée un lorebook qui est un sous-livre de `parent` (la racine si aucun n'est donné ; un livre sans lorebook en reçoit d'abord un). |
| `/set-lorebook <lorebook> [name] [enabled]` | Renomme un lorebook, ou l'active ou le désactive. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Modifie les lorebooks contenus dans un lorebook ; un lorebook ne peut pas se contenir lui-même. Pour un [agent](../agents.md), `/add-subbook` rétablit aussi un lorebook qu'il avait supprimé ou retiré de son parent plus tôt (il le désigne par le handle qu'il avait), avec ses entrées. |
| `/delete-lorebook <lorebook>` | Supprime un lorebook avec ses entrées (pas la racine). Demande d'abord confirmation. |
### Annotations

Les commandes qui lisent les [annotations](../books/annotations.md) (`/list-annotations`, `/get-annotation`) répondent en JSON, comme les autres commandes de lecture. Une annotation est désignée par son **identifiant**, et une partie par son identifiant pour les commandes qui modifient quelque chose. Ces commandes ne modifient que les notes, jamais l'histoire ; seule la suppression demande une confirmation. Un agent peut utiliser toutes ces commandes. Ses notes arrivent dans votre livre lorsque vous [relisez le résultat](../books/annotations.md#annotations-and-agents).

| Commande | Ce qu'il fait |
|----------|---------------|
| `/create-annotation <part> <text> [quote [type]]` | Ajoute une note à une partie. `quote` désigne les mots du passage, copiés depuis la partie : la note se place sur ce passage et le suit lorsque le texte est modifié. Sans citation, ou avec une citation absente du texte, la note porte sur la partie entière (la réponse le précise). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Modifie la note, le type, ou le fait que le modèle reçoive la note ; un texte ou un type vide reste inchangé. |
| `/resolve-annotation <annotation> [status]` | Définit le statut : `resolved` (par défaut), `dismissed` ou `open`. |
| `/reattach-annotation <annotation> [quote]` | Déplace une note vers le passage que la citation trouve dans sa partie ; sans citation, la note porte sur la partie entière. |
| `/delete-annotation <annotation>` | Supprime une note (elle peut être restaurée depuis la corbeille). Demande d'abord une confirmation. |
| `/scan-phrases [from to]` | Vérifie les parties de la branche active par rapport à la [liste de surveillance des expressions](../books/annotations.md) du livre, et ajoute une note pour chaque nouvelle occurrence (sans modèle). Sans argument, toute la branche ; avec deux parties, les parties comprises entre les deux. Répond le nombre de notes créées. |
| `/generate-annotation <part> [profile]` | Demande au modèle de relire la partie comme le fait [Annotate with AI](../books/annotations.md#letting-a-model-annotate), et ajoute tout ce qu'il trouve comme notes (source *AI* ; une citation absente du texte crée une note sur la partie entière). Rien n'est demandé : les notes sont la seule modification, et elles peuvent être résolues, modifiées ou supprimées. Elle relit avec le modèle du livre ; un agent utilise son propre modèle lorsque *Write the book with the model of the agent* est activé (comme le font les commandes du Lore Generator). `profile` est un profil de modèle d'un agent. S'exécute seule, prend du temps et consomme des jetons. |

<a id="the-planner"></a>

### Le planificateur

Les commandes du [planificateur](../books/story-editor.md#the-planner-tab) du livre. Une scène est désignée par son **numéro** dans la liste (à partir de 1 ; `/get-planned-scenes` les affiche). Les numéros changent lorsqu'une scène est déplacée ou supprimée, donc relisez la liste après une modification. Ces commandes ne modifient que le planificateur, jamais l'histoire. Ajouter, déplacer et marquer une scène comme terminée ne demandent pas de confirmation ; modifier le texte d'une scène et supprimer une scène sont destructifs (un [agent](../agents.md) doit confirmer, comme pour les autres commandes destructives).

| Commande | Ce qu'il fait |
|----------|---------------|
| `/get-planned-scenes` | Les scènes avec leur numéro, leur titre, le cadre de la scène, le personnage du point de vue (POV), les personnages présents, les consignes, et si elles sont terminées. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Ajoute une scène à la fin de la liste. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Modifie le titre et les détails indiqués ; ce qui est omis reste, et un texte vide l'efface. |
| `/set-planned-scene-done <scene> [done]` | Marque la scène comme écrite (`done` est `true` par défaut) ou non écrite. |
| `/move-planned-scene <scene> <to>` | Déplace la scène à la place `to` (1 est la première place ; si elle est trop grande, la scène va à la dernière place). |
| `/delete-planned-scene <scene>` | Supprime la scène. |

Les commandes qui font écrire ou relire le modèle prennent du temps : `/generate-annotation` et les deux résumés affichent la fenêtre **Stop**, et `/next`, `/swipe` et `/regenerate` s'écrivent en flux dans l'histoire (voir [Commands that take time](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Commandes fournies par les extensions

Vous les voyez dans la liste tant que l'extension est chargée ; chacune est décrite dans la documentation de son extension.

| Commande | Extension | Ce qu'il fait |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | Vos prompts de génération enregistrés, ou le texte de l'un d'eux. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Génère le texte d'une entrée de lore avec le lore du lorebook, le compacte (sauf si `compact` est `false`) et le renvoie. Ne stocke rien. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Compacte un texte généré en un texte d'entrée de lore dense, et le renvoie. Ne stocke rien. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Les catégories de noms inventés, et des noms de l'une d'elles. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Les listes de vrais noms, et des noms choisis dans l'une d'elles. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | Les types de vrais lieux, une recherche par nom, type et pays, les lieux autour d'un point, et la distance entre deux lieux. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Les arcs et les chapitres du plan. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Ajoutent, modifient, déplacent et suppriment un arc (`<arc>` est son numéro). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | La même chose pour les chapitres d'un arc. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Les générateurs de l'onglet Arcs. Ils renvoient le résultat, et ne l'ajoutent au plan qu'avec `save`. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | Les personnages du livre, et un personnage avec sa note et ses relations. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Ajoutent, modifient et suppriment un personnage. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Les relations entre les personnages. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Les pages des notes du livre (pas celles cachées aux assistants), une page avec son texte et ses liens, et une recherche. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Ajoutent, complètent, modifient, éditent et suppriment une page ; `note-text` est prévu pour la macro {%{`{{command::note-text::Title}}`}%}. |

Les commandes du Lore Generator, du Name generator, du Name provider et du Place provider ne font que lire : celles qui génèrent renvoient du texte et laissent à vous, ou à l'agent qui les a appelées, le soin de le placer dans une entrée. Les commandes d'Arc Outline et de Character Tracker modifient aussi le plan et les personnages du livre ouvert ; seules leurs commandes `delete` sont destructives. Story Notes modifie aussi les pages du livre ouvert ; là, `update-note` et `edit-note` sont destructives également, car elles remplacent un texte existant.

<a id="limits"></a>

## Limites

- La ligne de commande se trouve dans l'onglet story de la disposition de bureau. La [vue téléphone](../phone.md) ne l'a pas encore.
- Une commande s'exécute en tant que vous, dans votre session, et travaille avec le livre que vous avez ouvert.
