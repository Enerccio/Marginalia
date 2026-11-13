---
label: Expressions régulières
order: 935
icon: search
---

# Expressions régulières

Une expression régulière (*regex*) est un court motif qui décrit un texte au lieu de l'écrire en entier : « le mot Harry, mais
pas Harrys », « deux mots quelconques entre guillemets », « trois espaces ou plus à la suite ». Marginalia les utilise à deux endroits :

- **Rechercher et remplacer** de l'[histoire](books/story-editor.md#search-and-replace) et d'un
  [manuel de lore](lorebooks/entries.md#search-and-replace), ainsi que les [commandes](commands/index.md) correspondantes, lorsque **Rechercher par expression régulière** est activé. Le motif trouve le texte, et le remplacement peut réutiliser des morceaux de ce qu'il a trouvé.
- Le filtre **Expression régulière** d'une [entrée de lore](lorebooks/entries.md#activation), qui décide quand l'entrée entre dans le prompt.

Le dialecte est celui de Java. Cette page présente ce que vous utiliserez le plus souvent ; la référence complète est donnée à la fin.

!!!
Vous n'avez pas besoin d'expressions régulières pour des mots simples. Avec **Rechercher par expression régulière** désactivé, le texte est recherché exactement tel que vous l'écrivez, donc un `.` ou une `(` qu'il contient n'est qu'un point ou une parenthèse. Activez l'expression régulière seulement lorsqu'un texte fixe ne suffit pas.
!!!

<a id="the-building-blocks"></a>

## Les éléments de base

| Écrire | Correspond à | Exemple |
|---|---|---|
| une lettre ou un chiffre | ce caractère | `cat` trouve *cat* dans *concatener* |
| `.` | un caractère quelconque (sauf un saut de ligne) | `c.t` trouve *cat*, *cot*, *c7t* |
| `\d` `\w` `\s` | un chiffre, une lettre/chiffre/`_`, un espace ou un saut de ligne | `\d\d` trouve *42* |
| `[abc]` `[a-z]` | l'un des caractères listés, ou un intervalle | `gr[ae]y` trouve *grey* et *gray* |
| `[^abc]` | un caractère qui n'est *pas* listé | `[^"]` est tout sauf un guillemet |
| `\b` | la limite d'un mot | `\bcat\b` trouve *cat* mais pas *concatener* |
| `^` `$` | le début et la fin d'une ligne | `^Chapitre` trouve *Chapitre* en début de ligne |
| `x?` `x*` `x+` | `x` zéro ou une fois, un nombre quelconque de fois, au moins une fois | `colou?r` trouve *color* et *colour* |
| `x{2,4}` | `x` deux à quatre fois | `\.{3}` correspond à trois points à la suite |
| `\` | le caractère suivant est pris au sens littéral | `\.` est un point, `\?` un point d'interrogation, `\(` une parenthèse |

Les parenthèses forment un **groupe**, et une barre verticale à l'intérieur d'un groupe signifie « ou » : `(Mr|Mrs) Dursley` trouve *Mr Dursley* et
*Mrs Dursley*, et `\b(Voldemort|You-Know-Who)\b` trouve l'un ou l'autre nom. Un `?`, `*` ou `+` placé après un groupe s'applique à l'ensemble : `(very )+good` trouve *very good* et *very very good*.

Les caractères `. ? * + ^ $ | \ ( ) [ ] { }` ont une signification propre. Pour rechercher l'un d'eux tel quel, placez un `\` devant lui : pour trouver *Really?!* écrivez `Really\?!`.

Par défaut, une expression régulière ne fait aucune exception à la casse : *harry* ne trouve pas *Harry*. Placez `(?i)` au début pour ignorer la casse : `(?i)harry` trouve *Harry*, *HARRY* et *harry*. (Les fenêtres de Rechercher et remplacer verrouillent **Respecter la casse** tant que l'expression régulière est active ; c'est donc ainsi que vous la désactivez à cet endroit. Le filtre **Expression régulière** d'un manuel de lore ignore toujours la casse.)

Une correspondance de longueur nulle (un motif comme `a*` qui peut ne rien trouver) est ignorée par Rechercher et remplacer.

<a id="typical-uses-in-a-story"></a>

## Usages courants dans une histoire

Chaque ligne correspond à ce que vous pouvez vouloir faire dans un livre. **Rechercher** est ce que vous saisissez dans *Rechercher*, **Remplacer** ce que vous saisissez dans *Remplacer par*.

| Ce que vous voulez | Rechercher | Remplacer |
|---|---|---|
| Renommer un personnage, sans toucher à un mot plus long (*Harry* mais pas *Harrys* ni *Harrison*) | `\bHarry\b` | `Henry` |
| Renommer, quelle que soit la casse | `(?i)\bharry\b` | `Henry` |
| Renommer un nom et son possessif d'un coup | `\bHarry('s)?\b` | `Henry$1` |
| Remplacer les guillemets droits par des guillemets typographiques | `"([^"]*)"` | `“$1”` |
| Trois points en un seul caractère de points de suspension | `\.{3}` | `…` |
| Deux espaces ou plus en un seul | ` {2,}` | ` ` (une espace) |
| Supprimer les espaces en fin de ligne | ` +$` | (vide) |
| Un tiret long formé de deux traits d'union | ` ?-- ?` | ` — ` |
| Intervertir *Nom, Prénom* en *Prénom Nom* | `(\w+), (\w+)` | `$2 $1` |
| Trouver où un mot est utilisé, sans rien modifier | le motif | (laissez vide et appuyez seulement sur **Rechercher**) |

Pour regrouper plusieurs graphies d'un même nom, recherchez `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` et remplacez-le par
`Tom Riddle`.

<a id="taking-the-match-into-the-replacement"></a>

### Reprendre le texte trouvé dans le remplacement

Tout ce qu'une expression régulière place entre parenthèses est un **groupe**, numéroté de gauche à droite d'après sa parenthèse ouvrante : le remplacement
`$1` est le texte du premier groupe, `$2` celui du deuxième, `$0` la correspondance entière. Dans `(\w+), (\w+)`, le premier groupe est le
mot avant la virgule, le second le mot après ; `$2 $1` les intervertit : *Potter, Harry* devient *Harry Potter*.

Vous pouvez nommer un groupe, `(?<last>\w+), (?<first>\w+)`, et utiliser ce nom dans le remplacement : `${first} ${last}`.

Un `$` ou un `\` qui doit rester tel quel dans le remplacement doit être protégé : pour écrire *$5* dans le texte, tapez `\$5`.
Sans expression régulière, le remplacement est toujours pris exactement tel que vous l'avez saisi.

Si le motif est incorrect (une parenthèse jamais refermée) ou si le remplacement renvoie à un groupe que le motif ne possède pas, une notification le signale et rien ne se passe ; une recherche ne modifie jamais rien.

<a id="match-only-in-a-context"></a>

### Ne trouver qu'en contexte

Une *anticipation* `(?=...)` et une *rétrospection* `(?<=...)` regardent le texte autour de la correspondance sans la prendre, de sorte que seule la partie hors de ces éléments est remplacée :

| Rechercher | Trouve |
|---|---|
| `Harry(?= Potter)` | *Harry* seulement lorsque *Potter* suit (et laisse *Potter* intact) |
| `(?<=Professor )McGonagall` | *McGonagall* seulement après *Professor* |
| `Harry(?! Potter)` | *Harry* non suivi de *Potter* |

<a id="lazy-and-greedy"></a>

### Gourmand et paresseux

`*` et `+` prennent autant qu'ils peuvent : dans `He said "yes" and "no"`, le motif `".*"` correspond à l'ensemble `"yes" and "no"`.
Ajoutez un `?` (`".*?"`), ou indiquez ce qui n'est pas permis (`"[^"]*"`), pour vous arrêter au premier guillemet fermant.

La recherche traite chaque ligne séparément : `.` ne franchit pas un saut de ligne, et `^` et `$` correspondent au début et à la fin de chaque ligne d'une partie, et pas seulement à celles de l'ensemble du texte. Pour correspondre à un saut de ligne, écrivez `\n`.

<a id="in-a-lorebook-entry-filter"></a>

## Dans le filtre d'une entrée de lore

Le filtre d'une entrée de manuel de lore réglée sur **Expression régulière** est comparé à la requête de la partie, à n'importe quel endroit de celle-ci. Il ignore la casse, et `.` correspond bien aux sauts de ligne ici, de sorte que `varro.*harbor` trouve Varro et le port même sur des lignes différentes. Filtres typiques :

- `\bvarro\b` - Varro comme mot entier.
- `\b(varro|captain)\b` - Varro ou le capitaine.
- `varro.*(harbor|docks)` - Varro, puis plus loin, le port ou les quais.
- `dragons?` - *dragon* ou *dragons*.
- `(?<!no )magic` - *magic*, sauf lorsqu'il suit immédiatement *no*.

Une expression régulière invalide n'active jamais l'entrée, et la raison est écrite dans le journal. Voir
[entrées et activation](lorebooks/entries.md#activation) pour le reste du fonctionnement du choix d'une entrée.

<a id="in-the-commands"></a>

## Dans les commandes

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) et `/search-replace-lore-entries` prennent l'expression régulière comme troisième argument (`true`), après la recherche et le remplacement. La ligne de commande se découpe aux espaces, donc un motif ou un remplacement contenant une espace se met entre guillemets doubles. Une barre oblique inverse devant une lettre est transmise telle quelle (`\b`, `\d`), mais `\\` devient une seule barre oblique inverse et `\"` un guillemet ; une barre oblique inverse littérale dans le motif s'écrit donc `\\\\`.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

Les commandes n'affichent pas la liste des correspondances : elles vous demandent si vous voulez remplacer le nombre de correspondances trouvées. Si vous n'êtes pas sûr d'un motif, essayez-le d'abord dans la fenêtre **Rechercher et remplacer**, qui affiche chaque correspondance telle qu'elle est et telle qu'elle sera.

<a id="tips"></a>

## Conseils

- Essayez d'abord un motif avec **Rechercher** et ouvrez quelques lignes des résultats : la fenêtre affiche chaque correspondance avec son contexte, telle qu'elle est et telle qu'elle sera. Ne remplacez que lorsque le résultat est celui que vous vouliez.
- Commencez simplement et ajoutez au motif pas à pas ; un motif qui trouve trop souvent a généralement besoin de `\b` autour d'un mot, ou de `?` pour limiter un `*`.
- Une [sauvegarde](books/backups.md) avant un gros remplacement ne coûte rien (`/take-backup`), et un remplacement ne peut pas être arrêté une fois commencé.
- Résumés modifiés : lorsque le remplacement touche le texte à partir duquel un [résumé](books/summaries.md) a été fait, la fenêtre avertit que le résumé deviendrait obsolète ; voir [Rechercher et remplacer](books/story-editor.md#search-and-replace).

<a id="further-reading"></a>

## Pour aller plus loin

Cette page ne couvre que les bases. La syntaxe complète de Java, avec toutes les constructions, options et classes de caractères, se trouve dans la
[documentation de `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html),
et la syntaxe de remplacement (`$1`, `${name}`, `\$`) dans
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
