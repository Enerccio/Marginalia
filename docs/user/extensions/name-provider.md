# Name provider

Gives the model real names to pick from instead of inventing them: the most common forenames and surnames of many
countries. Where the [Name generator](name-generator.md) makes up new names that only sound like a category, this
extension hands out names that exist, each with the original spelling next to the romanized one: `Satō` and `佐藤`.

The extension has no screen of its own and nothing to configure. Other extensions offer its functions to the model: the
[Lore Generator](lore-generator.md#tools) and the [Arc Outline](arc-outline.md), where you choose which lists the
model may use. Its suite is *Name provider* in the [tools window](tools.md), next to the *Name generator* one, and you can
check lists of both, of one of them or of none.

## What the model can do

| Function | What it does |
|---|---|
| `list_real_name_lists` | Lists the lists of names; with a `filter` only those that have its words in the name. |
| `pick_real_names` | Picks up to 100 different names of one list at random. Every name is a pair, the romanized name and the original one. |

A list with fewer names than asked for returns all of them. The model is told (see the hint of the *Name provider* suite
in the [tools window](tools.md#choosing-the-tools)) to pick a name from what it gets and to write it as it was returned, with the original next
to it when that suits the setting; it may combine a forename with a surname of the same country.

## Commands

The same names are available as [commands](../commands/index.md), for the command line and for [agents](../agents.md),
which can use them in their own prompts (for example for `/generate-lore-entry` of the [Lore Generator](lore-generator.md)):

| Command | What it does |
|---|---|
| `/list-real-name-lists [<filter>]` | Lists the lists as a JSON array; with a filter only those whose name has all its words (each at the start of a word, in any order, not case sensitive). |
| `/pick-real-names list count` | Picks up to 100 names of the list at random and returns them as a JSON array of pairs, the romanized name and the original one. |

Both only read.

## Lists

There are about 180 lists, named `<Country> - Surnames` and `<Country> - Forenames`: surnames of 75 countries and forenames of
106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). Forenames of both genders are in one list. A list has
from 5 to about 150 names, the most common ones of the country. The names are not made new: a name you get is a name that
people have.

A few dozen entries are **groups** of lists by language: `<Language> - Forenames` and `<Language> - Surnames` for Spanish
(18 countries), Portuguese, French, German, English, Arabic, West, East and South Slavic, North Germanic, Nordic, Uralic,
Baltic, Turkic, Persian, Chinese, South Asian, Southeast Asian, Caucasus, Albanian and Romanian (where there are such lists). They
are listed like any list and are one item in the [tools window](tools.md). Every name of a group is picked from one of its lists
chosen at random, each country with the same chance, so a request for 20 names gets names of several countries, never the same name
twice. Countries where several languages are spoken (Belgium, Luxembourg, Malta, Andorra) are in no group. A group is a
line in `name-groups.json`, see the [plugin README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups).

The data is the [popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), which
takes them from Wikipedia's lists of the most common surnames and the most popular given names. The names written in
non-Latin scripts (Chinese, Japanese, Korean, Arabic, Cyrillic...) have a romanization, either one from the source or
one made by machine translation, so check the spelling when it matters.

## Usage

Load the extension in *Admin → Extensions*. It applies to the next generation, also when it is installed after the
extensions that use it or unloaded and installed again. Without it and the other suites there is no *Tools*
button.
