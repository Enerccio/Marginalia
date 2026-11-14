---
label: Référence des macros
order: 90
---

# Référence des macros

Marginalia comprend les macros de [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), donc les prompts et les informations de monde écrits pour SillyTavern fonctionnent sans modification. Les macros fonctionnent dans chaque [modèle](templates.md) : modèle principal, prompt utilisateur, prompt de résumé et entrées de lorebook.

{%{

<a id="syntax"></a>

## Syntaxe

| Forme | Exemple |
|---|---|
| sans arguments | `{{user}}` |
| arguments séparés par `::` | `{{setvar::mood::nervous}}` |
| premier argument après un espace | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| ancienne forme à argument unique `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| macros imbriquées | `{{getvar::{{user}}_mood}}` |
| à portée (contenu comme dernier argument) | `{{setvar backstory}}Long text...{{/setvar}}` |

- Les noms de macros ne sont pas sensibles à la casse : `{{User}}` et `{{USER}}` fonctionnent.
- Les espaces autour des noms et des arguments sont ignorés : `{{ getvar :: mood }}`.
- Les `::` supplémentaires vont dans le dernier argument : `{{setvar::x::a::b}}` définit `x` à `a::b`.
- Le contenu d'un bloc à portée est évalué en premier, puis désindenté et débarrassé de ses espaces en début et fin. Avec `#` après les accolades, `{{#setvar name}}...{{/setvar}}`, les espaces sont conservés tels quels.

<a id="names-and-participants"></a>

## Noms et participants

Marginalia n'a ni personas ni fiches de personnage ; les noms proviennent des instructions de tour de la partie en cours d'écriture.

| Macro | Résultat |
|---|---|
| `{{user}}` | Le personnage point de vue. |
| `{{char}}` | Le personnage point de vue. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | Les personnages présents, ou le personnage point de vue si aucun n'est indiqué. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | Les personnages présents sauf le personnage point de vue (séparés par des virgules ou des retours à la ligne, joints par `, `). |
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
| `{{lastMessageId}}` | La position de la dernière partie, comptée à partir de 0. |
| `{{lastUserMessage}}` | Les instructions de la dernière partie. |
| `{{summary}}` | Le [résumé](../books/summaries.md) le plus récent de la branche active. |
| `{{closest-message}}`, `{{closest-message::n}}` | La partie la plus proche de celle en cours d'écriture : la dernière, ou la *n*-ième en remontant à partir d'elle (`::2` est l'avant-dernière). Vide lorsqu'il n'existe pas de telle partie. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | La même chose pour les résumés de la branche : le plus récent, ou le *n*-ième en remontant. Alias `{{closestSummary}}`. |
| `{{current-message}}` | Le texte de la partie que remplace *Régénérer* ou une variante (swipe) ; vide pour une nouvelle partie. Alias `{{currentMessage}}`. |

Pour la première partie d'un livre, ces macros sont vides. Pour *Régénérer*, « dernière partie » désigne la partie qui précède la partie régénérée.

Le nombre passé à `closest-message` et `closest-summary` peut être une macro, donc provenir d'une variable :
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` est la troisième partie en partant de la fin. Un nombre qui n'est pas un entier positif ne donne rien. `closest-summary` compte les résumés des parties de la branche, un [méta-résumé](../books/summaries.md) et les résumés qu'il remplace inclus ; `{{summary}}` est identique à `{{closest-summary}}`.

<a id="date-and-time"></a>

## Date et heure

| Macro | Exemple de résultat |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - l'heure à un décalage UTC (`UTC-5:30` fonctionne aussi) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [jetons de format moment.js](https://momentjs.com/docs/#/displaying/format/), le texte entre `[brackets]` est affiché tel quel : `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - temps écoulé depuis l'écriture de la dernière partie, `just now` pour la première partie. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - la différence entre deux instants ; `now`, les dates (`2026-01-08`) et les heures ISO fonctionnent. Les valeurs inconnues ne donnent rien. |

Les heures sont exprimées dans le fuseau horaire de l'ordinateur ou du serveur qui fait tourner Marginalia, et toujours en anglais. Pour la [version Docker](../getting-started/docker-server.md), définissez le fuseau horaire avec la variable d'environnement `TZ`, par exemple `TZ=Europe/Prague`.

## Variables

Les variables conservent des valeurs d'une partie à l'autre : compteurs, humeur d'un personnage, éléments établis par l'histoire. Il en existe deux types :

- les variables **locales** appartiennent à la branche de l'histoire : chaque partie conserve leurs valeurs, et une nouvelle partie part des valeurs de la partie précédente ;
- les variables **globales** appartiennent au livre, partagées par toutes les branches.

Voir [Ce qui est conservé d'une partie à l'autre](templates.md#what-is-kept-between-parts).

| Locale | Globale | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | La valeur ; vide lorsqu'elle n'est pas définie. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Définit la valeur. N'affiche rien. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Ajoute un nombre, ou ajoute du texte lorsque l'un des deux n'est pas un nombre. N'affiche rien. |
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
| `{{.hp > 10}}`, `>=`, `<`, `<=` | compare des nombres (`false` pour du texte) | `true` / `false` |

Replis et valeurs par défaut :

- `{{.name || Guest}}` affiche *Guest* lorsque la variable est vide ou fausse (`0`, `false`, `no`, `off`) ;
- `{{.name ?? Guest}}` affiche *Guest* uniquement lorsque la variable n'est pas définie du tout ;
- `{{.title ||= Wanderer}}` définit la variable à *Wanderer* si elle est vide ou fausse, puis l'affiche ;
- `{{.title ??= Wanderer}}` ne la définit que si elle n'est pas définie du tout, puis l'affiche.

Les valeurs peuvent contenir des macros : `{{.log += -> {{user}}}}`.

Par exemple, un compteur de parties en tête du prompt utilisateur (`+=` n'affiche rien, et `{{trim}}` supprime la ligne vide qu'il laisse) :

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
| négation - `{{if !user}}`, `{{if !.mood}}` | le contraire |

Sont fausses une valeur vide et `0`, `false`, `no`, `off` (quelle que soit la casse). Tout le reste est vrai.

- La forme courte `{{if user::text}}` affiche *texte* lorsque la condition est vraie.
- Seule la branche empruntée est évaluée : une `{{setvar}}` dans l'autre branche n'est pas exécutée.
- Les blocs `{{if}}` sont désindentés et débarrassés de leurs espaces comme les macros à portée, ce qui permet de les indenter pour la lisibilité. `{{#if}}` (le [if Handlebars](templates.md#conditions)) conserve les espaces.
- Un `{{if}}` sans `{{/if}}` est affiché comme du texte.
## Expressions

`{{eval::expression}}` donne `true` ou `false`. Utilisez-le lorsqu'une comparaison est nécessaire que `{{if}}` ne peut pas faire : sur les valeurs du modèle (pas seulement sur les variables), reliées par `and`, `or` et `not` :

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| Dans une expression | Signification |
|---|---|
| `"text"`, `'text'` | Un texte. **Un texte est toujours entre guillemets** : un mot sans guillemets est un nom. |
| `12`, `-3.5`, `true`, `false` | Un nombre ou une valeur booléenne. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Une valeur du modèle, comme dans `{{povCharacter}}`. Une macro sans arguments fonctionne aussi (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Une [variable](#variables) locale ou globale ; vide lorsqu'elle n'est pas définie. |
| `( )` | Regroupement. |

Comparaisons :

| Opérateur | Vrai lorsque |
|---|---|
| `==`, `!=` | Égal, différent. Les nombres sont comparés par leur valeur (`5 == 5.0`), tout le reste exactement, donc `"Fiora" == "fiora"` est faux. |
| `~=` | Égal, sans tenir compte de la casse. |
| `<`, `<=`, `>`, `>=` | Les nombres sont dans cet ordre. Faux lorsqu'un des deux côtés n'est pas un nombre. |
| `contains` | Le texte de gauche contient le texte de droite, sans tenir compte de la casse : `presentCharacters contains "ma"` est aussi vrai pour *Mara*. |
| `has` | La liste de gauche, séparée par des virgules, des points-virgules ou des retours à la ligne, contient l'élément de droite, sans tenir compte de la casse ni des espaces autour des éléments : `presentCharacters has "Mara"` n'est pas vrai pour *Marati*. |

Liaisons : `and` (`&&`), `or` (`||`), `not` (`!`) ; `not` est prioritaire, puis `and`, puis `or`. Une valeur seule est vraie, sauf si elle est vide, `0`, `false`, `no` ou `off`. Les macros sont autorisées à l'intérieur d'un texte (`"{{user}}"`) et sont calculées en premier.

Une expression non valide affiche `Error` : une erreur de syntaxe, un mot sans guillemets qui n'est pas un nom (`povCharacter == Fiora`), un nom inconnu. Tous les noms sont vérifiés, y compris dans la partie d'un `and` / `or` qui ne serait pas atteinte, afin qu'une faute de frappe ne puisse pas passer inaperçue. Dans une [condition de lorebook](../lorebooks/entries.md#a-condition-for-an-entry), un `Error` rend l'entrée inutilisée et l'aperçu l'indique.

<a id="randomness"></a>

## Hasard

| Macro | Résultat |
|---|---|
| `{{random::red::green::blue}}` | Une des options, différente à chaque fois. Aussi `{{random:red,green,blue}}` et `{{random}}red, green{{/random}}`. |
| `{{pick::sword::axe::bow}}` | Une des options, mais toujours **la même** pour le même livre et le même emplacement dans le modèle. Utilisez-la pour les éléments qui doivent rester cohérents, comme un nom choisi au hasard. |
| `{{roll::2d6+3}}` | Un lancer de dés : `1d20`, `3d4-10`, `d100` ; aussi `{{roll d100}}`, `{{roll:6}}` (un dé à six faces). Les lancers non valides ne donnent rien. |

<a id="generation-info"></a>

## Informations de génération

| Macro | Résultat |
|---|---|
| `{{model}}` | Le nom du [fournisseur d'inférence](../inference-providers.md) du livre. |
| `{{maxContextTokens}}` | La limite de contexte en jetons (protocole ou fournisseur, voir [Limites](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | La limite de réponse en jetons. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Contexte moins réponse : l'espace disponible pour le prompt. |
| `{{lastGenerationType}}` | `normal` pour une nouvelle partie, `regenerate`, `swipe`, ou `quiet` dans les résumés. |

<a id="commands"></a>

## Commandes

| Macro | Résultat |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | Ce que répond une [commande](../commands/index.md), sous forme de texte. Seule une commande conçue pour les macros peut être utilisée (elle ne fait que lire ; une extension l'indique, par exemple `{{command::character-tracker-map}}` du [Character Tracker](../extensions/character-tracker.md)) ; tout autre nom affiche `Error`. Une commande qui échoue n'affiche rien. |

La commande s'exécute lorsque la génération atteint la macro (pas du tout lorsqu'elle se trouve dans une branche d'un `{{if}}` qui n'est pas retenue), pour le livre en cours de génération, et seulement dans les prompts d'une génération (le prompt utilisateur, les entrées du lorebook et le modèle principal) ; ailleurs, la macro n'affiche rien. La réponse est conservée avec la partie à partir de laquelle la nouvelle partie se poursuit : la génération suivante à partir de la même partie, ainsi que les autres modèles de la même génération, l'utilisent au lieu de relancer la commande. **Régénérer** et **swipe** redemandent la commande, et c'est aussi le cas de chaque nouvelle partie, puisqu'elle se poursuit à partir d'une nouvelle dernière partie.

<a id="utility"></a>

## Utilitaires

| Macro | Résultat |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Un ou plusieurs sauts de ligne. |
| `{{space}}`, `{{space::4}}` | Un ou plusieurs espaces. |
| `{{trim}}` | Supprime les sauts de ligne qui l'entourent. Utile après les lignes qui ne font que définir des variables. |
| `{{noop}}` | Rien. |
| `{{join::separator::a::b::c}}` | Les textes après le premier, réunis par le premier placé entre eux, en nombre quelconque (macros comprises) : `{{join::-::{{user}}::{{scenario}}}}`. Les textes vides sont ignorés, de sorte qu'une macro facultative ne laisse aucun séparateur superflu. Les espaces autour d'un argument écrit sont supprimés ; pour un séparateur contenant des espaces, utilisez donc une macro : `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::text}}` | Le texte inversé. |
| `{{// comment}}` | Un commentaire, supprimé. `{{//}}` ... `{{///}}` met en commentaire tout ce qui se trouve entre les deux, macros comprises. |
| `\{\{user\}\}` | Affiche `{{user}}` tel quel. |

<a id="sillytavern-only-macros"></a>

## Macros propres à SillyTavern

Les macros qui n'ont de sens que dans les conversations SillyTavern - personas et champs de fiche de personnage (`{{persona}}`, `{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), séquences du mode instruct (`{{instructUserPrefix}}`...), notes de l'auteur, identifiants de swipe, `{{outlet}}` et autres semblables - sont reconnues et n'affichent **rien**, afin que les informations de monde importées n'y fassent pas fuir ces éléments dans le prompt. `{{isMobile}}` et `{{hasExtension}}` affichent `false`.

Une macro que Marginalia ne connaît pas :

- avec des arguments (`{{fooBar::x}}`) est affichée telle quelle, comme le fait SillyTavern ;
- sans arguments (`{{fooBar}}`) est affichée sous la forme **`Error`**, comme une [variable de modèle](templates.md#typos) inconnue.

}%}
