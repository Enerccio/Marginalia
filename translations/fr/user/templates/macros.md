---
label: Référence des macros
order: 90
---

# Référence des macros

Marginalia comprend les macros de [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), donc les prompts et les entrées de monde écrits pour SillyTavern fonctionnent sans modification. Les macros fonctionnent dans tous les [modèles](templates.md) : modèle principal, prompt utilisateur, prompt de résumé et entrées de lorebook.

{%{

<a id="syntax"></a>

## Syntaxe

| Forme | Exemple |
|---|---|
| sans argument | `{{user}}` |
| arguments séparés par `::` | `{{setvar::mood::nervous}}` |
| premier argument après une espace | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| ancienne forme avec un seul `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| macros imbriquées | `{{getvar::{{user}}_mood}}` |
| avec contenu (contenu comme dernier argument) | `{{setvar backstory}}Long text...{{/setvar}}` |

- Les noms de macros ne tiennent pas compte de la casse : `{{User}}` et `{{USER}}` fonctionnent.
- Les espaces autour des noms et des arguments sont ignorés : `{{ getvar :: mood }}`.
- Les `::` supplémentaires vont dans le dernier argument : `{{setvar::x::a::b}}` définit `x` à `a::b`.
- Le contenu avec portée est évalué en premier, puis désindenté et rogné. Avec `#` après les accolades, `{{#setvar name}}...{{/setvar}}`, les espaces sont conservés exactement.

<a id="names-and-participants"></a>

## Noms et participants

Marginalia n'a ni personas ni fiches de personnage : les noms viennent des instructions du tour de la partie en cours d'écriture.

| Macro | Résultat |
|---|---|
| `{{user}}` | Le personnage point de vue. |
| `{{char}}` | Le personnage point de vue. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | Les personnages présents, ou le personnage point de vue si aucun n'est indiqué. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | Les personnages présents, sauf le personnage point de vue (séparés par des virgules ou des retours à la ligne, réunis par `, `). |
| `{{description}}` | La description du livre. |
| `{{scenario}}` | Le cadre de la scène. |
| `{{input}}` | Les instructions de la partie. |

Les marqueurs SillyTavern `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` et `<CHARIFNOTGROUP>` (quelle que soit la casse) fonctionnent aussi.

Avec le personnage point de vue *Alice* et les personnages présents *Alice, Bob, Carol* :
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## Historique de l'histoire

| Macro | Résultat |
|---|---|
| `{{lastMessage}}` | Le texte de la dernière partie de la branche active. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | La position de la dernière partie, en comptant à partir de 0. |
| `{{lastUserMessage}}` | Les instructions de la dernière partie. |
| `{{summary}}` | Le [résumé](../books/summaries.md) le plus récent de la branche active. |
| `{{closest-message}}`, `{{closest-message::n}}` | La partie la plus proche de celle en cours d'écriture : la dernière, ou la *n*-ième en remontant à partir d'elle (`::2` est celle qui précède la dernière). Vide lorsqu'il n'existe pas de telle partie. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | La même chose pour les résumés de la branche : le plus récent, ou le *n*-ième en remontant. Alias `{{closestSummary}}`. |
| `{{current-message}}` | Le texte de la partie que remplace *Régénérer* ou un swipe ; vide pour une nouvelle partie. Alias `{{currentMessage}}`. |

Pour la première partie d'un livre, elles sont vides. Pour *Régénérer*, la « dernière partie » est celle qui précède la partie régénérée.

Le nombre donné à `closest-message` et `closest-summary` peut être une macro, donc provenir d'une variable : `{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` désigne la troisième partie en partant de la fin. Un nombre qui n'est pas un entier positif ne donne rien. `closest-summary` compte les résumés des parties de la branche, un [méta-résumé](../books/summaries.md) et les résumés qu'il remplace inclus ; `{{summary}}` est identique à `{{closest-summary}}`.

<a id="date-and-time"></a>

## Date et heure

| Macro | Exemple de résultat |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - l'heure avec un décalage UTC (`UTC-5:30` fonctionne aussi) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [codes de format moment.js](https://momentjs.com/docs/#/displaying/format/), le texte entre `[crochets]` est affiché tel quel : `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - le temps écoulé depuis la dernière partie écrite, `just now` pour la première partie. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - la différence entre deux moments ; `now`, les dates (`2026-01-08`) et les heures ISO fonctionnent. Les valeurs inconnues ne donnent rien. |

Les heures sont données dans le fuseau horaire de l'ordinateur ou du serveur sur lequel tourne Marginalia, et toujours en anglais. Pour la [version Docker](../getting-started/docker-server.md), définissez le fuseau horaire avec la variable d'environnement `TZ`, par exemple `TZ=Europe/Prague`.

## Variables

Les variables conservent des valeurs d'une partie à l'autre : compteurs, humeur d'un personnage, éléments établis par l'histoire. Il en existe deux types :

- les variables **locales** appartiennent à la branche de l'histoire : chaque partie conserve leurs valeurs, et une nouvelle partie part des valeurs de la partie qui la précède ;
- les variables **globales** appartiennent au livre et sont partagées par toutes les branches.

Voir [Ce qui est conservé d'une partie à l'autre](templates.md#what-is-kept-between-parts).

| Locale | Globale | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | La valeur ; vide si elle n'est pas définie. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Définit la valeur. N'affiche rien. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Ajoute un nombre, ou ajoute du texte à la suite lorsque l'une des valeurs n'est pas un nombre. N'affiche rien. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Ajoute 1 et affiche la nouvelle valeur. Une valeur qui n'est pas un nombre part de 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Retire 1 et affiche la nouvelle valeur. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` ou `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Supprime la variable. Alias `flushvar` / `flushglobalvar`. |

Les nombres sont affichés sans zéros inutiles : `1.50` plus `1.5` donne `3`.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### Raccourcis

`{{.name}}` est une variable locale, `{{$name}}` une variable globale. Seul, un raccourci affiche la valeur. Avec un opérateur :

| Raccourci | Effet | Affiche |
|---|---|---|
| `{{.hp = 20}}` | définit la valeur | rien |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | ajoute / retire (`+=` ajoute du texte à un texte) | rien |
| `{{.turn++}}`, `{{.turn--}}` | ajoute / retire 1 | la nouvelle valeur |
| `{{.hp == 17}}`, `{{.hp != 17}}` | compare | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | compare des nombres (`false` pour un texte) | `true` / `false` |

Valeurs de repli et valeurs par défaut :

- `{{.name || Guest}}` affiche *Guest* lorsque la variable est vide ou fausse (`0`, `false`, `no`, `off`) ;
- `{{.name ?? Guest}}` affiche *Guest* seulement lorsque la variable n'est pas du tout définie ;
- `{{.title ||= Wanderer}}` met la variable à *Wanderer* si elle est vide ou fausse, puis l'affiche ;
- `{{.title ??= Wanderer}}` ne la définit que si elle n'est pas du tout définie, puis l'affiche.

Les valeurs peuvent contenir des macros : `{{.log += -> {{user}}}}`.

Un compteur de parties, par exemple, en haut du prompt utilisateur (`+=` n'affiche rien, et `{{trim}}` supprime la ligne vide qu'il laisse) :

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## Conditions

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

La condition peut être :

| Condition | Vraie lorsque |
|---|---|
| un nom de macro - `{{if user}}` | la macro donne une valeur |
| une variable - `{{if .mood}}`, `{{if $chapter}}` | la variable a une valeur |
| une variable de modèle - `{{if sceneSetting}}` | la variable a une valeur |
| une comparaison - `{{if .hp > 20}}` | la comparaison est vraie |
| n'importe quel texte - `{{if yes}}` | toujours, sauf pour les valeurs fausses |
| une négation - `{{if !user}}`, `{{if !.mood}}` | le contraire |

Sont fausses une valeur vide et `0`, `false`, `no`, `off` (quelle que soit la casse). Tout le reste est vrai.

- La forme courte `{{if user::text}}` affiche *text* lorsque la condition est vraie.
- Seule la branche retenue est évaluée : un `{{setvar}}` dans l'autre branche ne s'exécute pas.
- Les blocs `{{if}}` sont désindentés et rognés comme les macros avec contenu, ils peuvent donc être indentés pour la lisibilité. `{{#if}}` (le [if de Handlebars](templates.md#conditions)) conserve les espaces.
- Un `{{if}}` sans `{{/if}}` est affiché comme du texte.
## Expressions

`{{eval::expression}}` renvoie `true` ou `false`. Utilisez-la lorsqu'une comparaison est nécessaire et que `{{if}}` ne peut pas la faire : sur les valeurs du modèle (pas seulement sur les variables), reliées par `and`, `or` et `not` :

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| Dans une expression | Signification |
|---|---|
| `"text"`, `'text'` | Un texte. **Un texte est toujours entre guillemets** : un mot sans guillemets est un nom. |
| `12`, `-3.5`, `true`, `false` | Un nombre ou une valeur de vérité. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Une valeur du modèle, comme dans `{{povCharacter}}`. Une macro sans arguments fonctionne aussi (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Une [variable](#variables) locale ou globale ; vide lorsqu'elle n'est pas définie. |
| `( )` | Regroupement. |

Comparaisons :

| Opérateur | Vrai lorsque |
|---|---|
| `==`, `!=` | Égaux, différents. Les nombres sont comparés par leur valeur (`5 == 5.0`), tout le reste exactement, donc `"Fiora" == "fiora"` est faux. |
| `~=` | Égaux, sans tenir compte de la casse. |
| `<`, `<=`, `>`, `>=` | Les nombres sont dans cet ordre. Faux lorsqu'un des côtés n'est pas un nombre. |
| `contains` | Le texte à gauche contient le texte à droite, sans tenir compte de la casse : `presentCharacters contains "ma"` est aussi vrai pour *Mara*. |
| `has` | La liste à gauche, séparée par des virgules, des points-virgules ou des retours à la ligne, contient l'élément à droite, sans tenir compte de la casse ni des espaces autour des éléments : `presentCharacters has "Mara"` n'est pas vrai pour *Marati*. |

Liaisons : `and` (`&&`), `or` (`||`), `not` (`!`) ; `not` s'applique en premier, puis `and`, puis `or`. Une valeur seule est vraie sauf si elle est vide, `0`, `false`, `no` ou `off`. Les macros sont autorisées dans un texte (`"{{user}}"`) et sont calculées d'abord.

Une expression invalide affiche `Error` : une erreur de syntaxe, un mot sans guillemets qui n'est pas un nom (`povCharacter == Fiora`), un nom inconnu. Tous les noms sont vérifiés, y compris dans la partie d'un `and` / `or` qui ne serait pas atteinte, afin qu'une faute de frappe ne puisse pas être masquée. Dans une [condition de lorebook](../lorebooks/entries.md#a-condition-for-an-entry), une `Error` rend l'entrée inutilisée, et l'aperçu l'indique.

## Randomness

| Macro | Résultat |
|---|---|
| `{{random::red::green::blue}}` | Une des options, différente à chaque fois. Aussi `{{random:red,green,blue}}` et `{{random}}red, green{{/random}}`. |
| `{{pick::sword::axe::bow}}` | Une des options, mais toujours **la même** pour le même livre et le même emplacement dans le modèle. Utilisez-la pour ce qui doit rester cohérent, comme un nom choisi au hasard. |
| `{{roll::2d6+3}}` | Un jet de dés : `1d20`, `3d4-10`, `d100` ; aussi `{{roll d100}}`, `{{roll:6}}` (un dé à six faces). Les jets invalides ne donnent rien. |

## Generation info

| Macro | Résultat |
|---|---|
| `{{model}}` | Le nom du [fournisseur d'inférence](../inference-providers.md) du livre. |
| `{{maxContextTokens}}` | La limite de contexte en tokens (du protocole ou du fournisseur, voir [Limites](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | La limite de réponse en tokens. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Contexte moins réponse : la place disponible pour le prompt. |
| `{{lastGenerationType}}` | `normal` pour une nouvelle partie, `regenerate`, `swipe`, ou `quiet` dans les résumés. |

## Commands

| Macro | Résultat |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | Ce qu'une [commande](../commands/index.md) répond, sous forme de texte. Seule une commande conçue pour les macros peut être utilisée (elle ne fait que lire ; une extension le précise, par exemple `{{command::character-tracker-map}}` du [Character Tracker](../extensions/character-tracker.md)) ; tout autre nom affiche `Error`. Une commande qui échoue n'affiche rien. |

La commande s'exécute lorsque la génération atteint la macro (pas du tout lorsqu'elle se trouve dans une branche d'un `{{if}}` non retenue) pour le livre qui est généré, et uniquement dans les prompts d'une génération (le prompt utilisateur, les entrées de lorebook et le modèle maître) ; ailleurs, la macro n'affiche rien. La réponse est conservée avec la partie à partir de laquelle la nouvelle partie continue : la génération suivante à partir de la même partie, et les autres modèles de la même génération, l'utilisent au lieu de relancer la commande. **Régénérer** et **swipe** demandent à nouveau la commande, et c'est aussi le cas de chaque nouvelle partie, puisqu'elle continue à partir d'une nouvelle dernière partie.

## Utility

| Macro | Résultat |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Un ou plusieurs retours à la ligne. |
| `{{space}}`, `{{space::4}}` | Une ou plusieurs espaces. |
| `{{trim}}` | Supprime les retours à la ligne autour d'elle. Utile après des lignes qui ne font que définir des variables. |
| `{{noop}}` | Rien. |
| `{{join::separator::a::b::c}}` | Les textes après le premier, réunis avec le premier entre eux, en nombre quelconque (macros incluses) : `{{join::-::{{user}}::{{scenario}}}}`. Les textes vides sont omis, donc une macro facultative ne laisse aucun séparateur superflu. Les espaces autour d'un argument écrit sont supprimés ; pour un séparateur contenant des espaces, utilisez une macro : `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::text}}` | Le texte inversé. |
| `{{// comment}}` | Un commentaire, supprimé. `{{//}}` ... `{{///}}` met en commentaire tout ce qui se trouve entre eux, macros incluses. |
| `\{\{user\}\}` | Affiche `{{user}}` tel quel. |

## SillyTavern-only macros

Les macros qui n'ont de sens que dans les conversations SillyTavern - personas et champs de fiche de personnage (`{{persona}}`, `{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), séquences du mode instruct (`{{instructUserPrefix}}`...), notes de l'auteur, identifiants de swipe, `{{outlet}}` et autres - sont reconnues et n'affichent **rien**, afin que les informations du monde importées n'introduisent pas ces macros dans le prompt. `{{isMobile}}` et `{{hasExtension}}` affichent `false`.

Une macro que Marginalia ne connaît pas :

- avec des arguments (`{{fooBar::x}}`) est affichée telle quelle, comme le fait SillyTavern ;
- sans arguments (`{{fooBar}}`) est affichée sous forme de **`Error`**, comme une [variable de modèle](templates.md#typos) inconnue.

}%}
