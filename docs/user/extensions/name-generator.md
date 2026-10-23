# Name generator

Gives the model a way to ask for names instead of inventing them. The names come from lists of real names - given
names and surnames of many countries, places, deities, monsters, drugs, companies, foods, and more - and are made new by
a Markov chain, so they sound like the category without being copies of it.

The extension has no screen of its own and nothing to configure. Other extensions offer its functions to the model; at
the moment that are the [Lore Generator](lore-generator.md#tools) and the [Arc Outline](arc-outline.md), where you
choose which categories the model may use (see [Tools for the model](tools.md)); its suite is *Name generator*, next to the
*Name provider* of the [Name provider](name-provider.md), which hands out real names instead of made up ones.

![Choosing the categories of names in the Tools window of the Lore Generator](../../images/tools-dialog.png)

## What the model can do

| Function | What it does |
|---|---|
| `list_name_categories` | Lists the categories of names; with a `filter` only those that have its words in the name. |
| `generate_names` | Makes up to 100 new names of one category. Optionally with a shortest and longest length. |

Without a length, the names are as long as the names of that category usually are (a category of long company names gives
long names, one of short forenames short ones). The model can ask for other lengths between 2 and 30 letters. A small
category may return fewer names than asked for, because the chain runs out of different ones. The model is free to
pick one of the names, change it or combine two of them; see the hint of the *Name generator* suite in the [tools window](tools.md#choosing-the-tools).

## Commands

The same names are available as [commands](../commands/index.md), for the command line and for [agents](../agents.md),
which can use them in their own prompts (for example for `/generate-lore-entry` of the [Lore Generator](lore-generator.md)):

| Command | What it does |
|---|---|
| `/list-name-categories [<filter>]` | Lists the categories as a JSON array; with a filter only those whose name has all its words (each at the start of a word, in any order, not case sensitive: `chad male` finds *Africa - Central Africa - Chad - Male* but not *…Female*). |
| `/generate-names category count [min_length [max_length]]` | Makes up to 100 names of the category and returns them as a JSON array. Different names each time. |

Both only read.

## Categories

There are about 1270 categories. Their names say what they are:

- **Given names and surnames of the world**, from Kate Monk's Onomastikon: `Europe Western - France - Surnames`,
  `Ancient World - Egypt - Male`, `India - Hindu Gods - Goddesses`. Several hundred of them.
- **Surnames of 75 countries**, the romanized surnames of the [popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset)
  (CC0): `Armenia - Surnames`, `Japan - Surnames`, `Kosovo - Surnames`. They are small lists (6 to about 150 names), so the
  chains stay short and the new names are close to the real ones; for the real ones themselves use the
  [Name provider](name-provider.md).
- **Lists from other collections**: gods, angels, demons, ancient cities and states, emperors, planets (fantasygen),
  Corpora (animals, tarot, Tolkien names, Game of Thrones houses, drugs, hospitals, fabrics, gemstones, programming
  languages...) and markov-namegen-lib (Pokemon, dragons, cocktails, banks, Roman place names...).

Each category has a chain of the length that suited it best (short lists get short chains, which repeat the originals
less) and the lengths its names usually have. The lists themselves are not part of Marginalia: only the probabilities
are, so the original names can't be read out of the extension. Some of the sources have licences of their own, see the
[plugin README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md).

## Usage

Load the extension in *Admin → Extensions*. It applies to the next generation; extensions that use it find it when it is
loaded, also if it is installed after them or unloaded and installed again - there is no need to restart the Lore
Generator. Without it and the other suites, the Lore Generator has no *Tools* button.
