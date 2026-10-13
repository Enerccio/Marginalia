---
label: Name generator
order: 84
---

# Name generator

Gives the model a way to ask for names instead of inventing them. The names come from lists of real names - given
names and surnames of many countries, places, deities, monsters, drugs, companies, foods, and more - and are made new by
a Markov chain, so they sound like the category without being copies of it.

The extension has no screen of its own and nothing to configure. Other extensions offer its functions to the model; at
the moment that is the [Lore Generator](lore-generator.md#name-tools), where you choose which categories the model
may use.

## What the model can do

| Function | What it does |
|---|---|
| `list_name_categories` | Lists the categories of names. |
| `generate_names` | Makes up to 100 new names of one category. Optionally with a shortest and longest length. |

Without a length, the names are as long as the names of that category usually are (a category of long company names gives
long names, one of short forenames short ones). The model can ask for other lengths between 2 and 30 letters. A small
category may return fewer names than asked for, because the chain runs out of different ones. The model is free to
pick one of the names, change it or combine two of them; see the *Name tools hint* of the Lore Generator.

## Categories

There are about 1200 categories. Their names say what they are:

- **Given names and surnames of the world**, from Kate Monk's Onomastikon: `Europe Western - France - Surnames`,
  `Ancient World - Egypt - Male`, `India - Hindu Gods - Goddesses`. Several hundred of them.
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
Generator. Without it, the Lore Generator has no *Name tools* button.
