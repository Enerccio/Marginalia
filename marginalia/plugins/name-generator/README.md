# Name generator

Gives the model functions to get names instead of inventing them, from Markov chains trained on lists of names.

## Functions

| Function | Arguments | Answer |
|---|---|---|
| `list_name_categories` | `filter` (optional: words the name of the category has to contain, e.g. `chad male`) | the categories of names that can be generated |
| `generate_names` | `category`, `count`, optional `min_length`, `max_length` | `count` names of the category; `count` is limited to 1-100 and the lengths (in letters) to 2-30, whatever the model asks for; without lengths the usual ones of the category are used (see *Chain settings*), else 3 and 20; limits the wrong way round are swapped |

## Commands

The same names for the command bar and for agents (`NameGeneratorCommands`, both `NON_DESTRUCTIVE`): `/list-name-categories [filter]` (a JSON array with a category on each line; `filter` is words that start words in the name of the categories to list, in any order, not case sensitive, since there are over a thousand of them) and `/generate-names category count [min_length [max_length]]` (a JSON array of names, same limits as the function).

## For developers

`NameGeneratorFunctions` is the `@LLMFunction` interface, `NameGeneratorFunctionsImpl` the implementation (`MAX_COUNT`,
`clampCount`) on `NameTables` (loading and sampling). The extension compiles the interface on load and registers a `NameGeneratorToolProvider` with the tool service (`ToolService`, key
`names`, human name *Name generator*): every category is an item, and the functions the model gets (`NameGeneratorToolProvider.Restricted`)
know only the checked ones. The provider brings `NameGeneratorCommands` along, so one registration gives the command bar and agents the
commands and the [Lore Generator](../lore-generator/README.md) and the [Arc Outline](../arc-outline/README.md) (the **Tools** button) the
functions. The service removes both when the extension is unloaded, so it can be unloaded and installed again without restarting the
others. The hint added to the prompt is `NameGeneratorToolProvider.HINT`; the user can replace it in the tool dialog.

## Groups

A category can also be a **group** of categories (`src/main/resources/name-groups.json`, committed, read at run time, no
training needed):

```json
{"groups": [{"title": "Native American - Male", "members": ["America - Northern - * - Male"]},
            {"title": "German - Male", "members": ["Europe Western - Germany - * Male"], "weight": "size"}]}
```

`members` are patterns for whole category titles, not case sensitive, `*` stands for any text, so a title without `*` is one
category and `America - Northern - * - Male` every male category of the northern America (a new source file joins the group
when the tables are trained again). `generate_names` on a group picks one member at random (the same chance for each) for
every name and makes it with the chain and the usual lengths of that member, so a name belongs to one language instead of
a blend. `weight` says how: `"equal"` (default) gives every member the same chance, `"size"` a chance proportional to the
number of names the member was trained on (`size` in `name-tables.json`, written by `GenerateTables`; a member of unknown size
counts as one name), so a list of 600 names is chosen 60 times as often as one of 10. The shipped groups use `size` where the
members are slices of one language's names (the Onomastikon splits a country into Biblical, Germanic, Greek and Latin origins, India
into `Male 'a'`, `'b'`, `'c-k'`, `'l-z'`, the Saints into letters) and `equal` where a member is a whole language or country
(Caucasian, Turkic, Baltic, Polynesian, the African regions, the Native American groups, Deities...). Names are different across
the call as before.

The shipped groups (`name-groups.json`, 128): `<Language> - Male`, `- Female` and `- Surnames` for German, French, Spanish
(with the surnames of Latin America), Italian, Portuguese, Dutch, Scandinavian, Finnic, Baltic, Irish, Scottish, Welsh, Celtic,
West, East and South Slavic, Greek, Hungarian, Romanian, Albanian, Caucasian, Turkic, Persian, Arabic, Jewish, Hindu, South
Asian, Southeast Asian, Malay-Indonesian, Polynesian, the African regions, Anglo-Saxon, Medieval Germanic, Medieval English and
Ancient Near East (those that have no list of a gender have no group of it); `Chinese`, `Japanese`, `Korean` and `Vietnamese`
`- Surnames` (the Onomastikon and the country dataset together); `Native American` (northern, and Central and South America);
and `English - Surnames`, `Saints`, `Deities`, `Fantasy Creatures`, `Game of Thrones Houses`. The lists of both genders
are separate (the unisex lists like `Spanish Forenames` are in no group), patterns are written with `* Male` where the title
is `Celtic Male` or `Aragonese Names Male`.
`NameTables.categories()` lists groups together with the categories (they are items of the tool dialog too), `isGroup`
and `members` tell them apart. A group with no member, or with the title of a category, is left out; groups can't contain
groups. `NameGroupsTest` checks that every pattern of the shipped file matches a category, that a `- Male` group has no
female or surname member and the other way round, and the weights.

## Name tables

The names are not in the repository. A normal build does not touch `name-tables.json`: the training steps run only
with the profile `train-names` (`mvn package -Ptrain-names`; the executions of `exec-maven-plugin` are bound to
`generate-resources` through the property `names.training.phase`, `none` by default). The step running
`src/build/GenerateTables.java` reads every `*.json` of `src/build/resources` and its subfolders (git-ignored):

```json
{"_id": "demons", "title": "Theological Demons", "values": ["aamon", "abaddon", "azazel"]}
```

and writes `src/main/resources/name-tables.json` (committed): per category (`title`) only the Markov chain
probabilities of the next character (`^` pads the start, `$` ends a name) and the number of names it was trained on
(`size`), no names. Without source files the existing
`name-tables.json` is left as it is, so a clone builds without them. Empty source files are skipped; a title used twice
or a malformed file fails the build.

### Chain settings

`src/build/chains.json` (committed) holds per category title the length of the chain and the usual lengths of its names,
which the training applies (`order`, `minLength`, `maxLength` go into `name-tables.json` too; the lengths are what
`generate_names` uses when the model gives none):

```json
{"defaultOrder": 2, "categories": {"American Companies": {"order": 3, "minLength": 8, "maxLength": 25}}}
```

A category without an entry gets order 2 and no lengths. The file is written by `mvn exec:exec@tune-chains` (not part
of the build, run it after the sources changed; `target/chains-report.tsv` shows the measurements): `TuneChains.java`
scores the orders 1-4 of every category by cross-validation (how well a chain predicts names it was not trained on, in
bits per character, with the shorter chains as the fallback for the unseen) and by walking it (share of the names made
that are in the sources, number of different new ones). The order starts at 2, goes up only when the next one is clearly
better (more than 1 %), goes down to 1 when that is, and is lowered while the chain mostly repeats the sources. The
lengths are the 5th and 95th percentile of the lengths of the names, so a category of long names gives no fragments
("Pra", "Ven"). An entry with `"keep": true` is not touched by the tuning, edit it by hand. A title in the file that has
no source is reported by the build. The result is repeatable (fixed seeds).

### fantasygen dictionaries

`src/build/ImportFantasygen.java` (execution `import-fantasygen`, before the next one) clones
[fantasygen](https://github.com/alxgiraud/fantasygen) into `target/fantasygen` and writes every dictionary of its
`data/defaultDictionaries.json` as a source file in `src/build/resources/fantasygen`. Same rules as below for the clone
and for a missing network. The no-break spaces of the dictionaries are removed. Its titles win over the same ones of the
Corpora and markov-namegen-lib lists.

### Corpora

`src/build/ImportCorpora.java` (execution `import-corpora`, after fantasygen) clones
[Corpora](https://github.com/dariusk/corpora) (CC0) into `target/corpora` and turns the lists of names in its `data`
folder into source files in `src/build/resources/corpora`, one per list. The files have no common format, so a list is an
array of strings (the file itself, or a property of the object in it), or an array of objects whose `name` (`title`,
`city`, `color`... see `NAME_FIELDS`) is the name. A file that is not valid JSON (`humans/firstNames.json` misses a comma)
gives its lines that are one quoted string.

- The category is the path of the file below `data` and its name, `animals/dog_names.json` is `Animals - Dog Names`; a
  file with several lists adds the name of the property.
- Left out: the files of `SKIPPED` (words, numbers, texts, real-world data with no use in a story: banned games,
  terrorist groups, weapons, candidates...), names that are templates, links, censored texts or sentences or longer than
  40 characters or 5 words, lists with fewer than 10 names, and a list that has the same names as one imported before
  (Corpora repeats some). A title used by another source file is skipped like in the markov-namegen-lib import.

### markov-namegen-lib word lists

Before the training, `src/build/ImportMarkovNamegen.java` (a second `exec-maven-plugin` execution,
`import-markov-namegen`, same phase) clones [markov-namegen-lib](https://github.com/Tw1ddle/markov-namegen-lib/) (MIT)
into `target/markov-namegen-lib` and turns every `word_lists/*.txt` (one name per line) into a source file in
`src/build/resources/markov-namegen-lib` (git-ignored like the rest).

- The clone is made once and not pulled again; delete `target/markov-namegen-lib` (or `mvn clean`) to fetch it anew.
- Without network or git nothing is imported and the build goes on with the committed `name-tables.json`.
- The category is the file name, `theological_angels.txt` is `Theological Angels`. A list whose title is already
  used by another source file (fantasygen, the Onomastikon, hand-made ones) is skipped, as are lists with fewer than 10 names.
- Names are trimmed and de-duplicated; those with `^` or `$` are dropped.

`generate_names` walks the chain, drops names outside the lengths and duplicates, so a small
category can return fewer names than asked for.

### Popular names by country

`src/build/ImportPopularNames.java` (execution `import-popular-names`, after markov-namegen-lib and before the training)
clones the [popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0) into
`target/popular-names` and turns the **romanized surnames** of its `common-surnames-by-country.csv` into source files in
`src/build/resources/popular-names`, one per country. Only the `Country` and `Romanized Name` columns are read; the
category is `<Country> - Surnames` with the code expanded to the English name (`AM` is `Armenia`, `XK` `Kosovo`). The
alternatives of a cell (`Huỳnh/Hoàng`) are separate names, rows without a romanized name are dropped, countries with fewer than
5 names and titles used by another source are skipped (75 categories). Same rules as above for the clone and for a
missing network. The real names, as pairs with the original script, are the business of the
[Name provider](../name-provider/README.md).

### Kate Monk's Onomastikon

`mvn exec:exec@scrape-onomastikon` (in this folder, not part of the normal build) downloads
[the Onomastikon](https://tekeli.li/onomastikon/) with `src/build/ScrapeOnomastikon.java` into
`src/build/resources/onomastikon`, one source file per category, about 800 requests one at a time (about 2 minutes).
Run the build with `-Ptrain-names` afterwards to train the tables. The collection is (c) Kate Monk, "copies may be made for personal use
only": the downloaded names stay out of git, only the probabilities are committed.

- Every page is read once by its URL, however many links lead to it.
- The category is the path of the page, each folder and page named by the link that leads to it from the index of its
  parent: `Ancient World - Egypt - Male`. A page with several sections (Male, Female, Surnames...) adds the heading of
  the section, so first names and surnames are separate categories.
- No category for pages without names (indexes, histories, sources, rulers, calendars), for sections about them, or
  with fewer than 10 names.
- Tables with a row of column titles (Given Name, Diminutives, Meaning) keep only the columns with names. In a cell,
  alternatives (`Eanna/Enda`, `Ennae, Enda`) are separate names, glosses and dates are dropped, and only capitalized
  words made of letters (one or two) are names.
