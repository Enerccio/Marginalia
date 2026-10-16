---
label: Name provider
order: 85
---

# Name provider

Gives the model real names to pick from instead of inventing them: the most common forenames and surnames of many
countries. Where the [Name generator](name-generator.md) makes up new names that only sound like a category, this
extension hands out names that exist, each with the original spelling next to the romanized one: `Satō` and `佐藤`.

The extension has no screen of its own and nothing to configure. Other extensions offer its functions to the model: the
[Lore Generator](lore-generator.md#name-tools) and the [Arc Outline](arc-outline.md), where you choose which lists the
model may use. Its lists have the prefix `Provider: ` there, the ones of the Name generator `Generator: `, and you can
check lists of both, of one of them or of none.

## What the model can do

| Function | What it does |
|---|---|
| `list_real_name_lists` | Lists the lists of names. |
| `pick_real_names` | Picks up to 100 different names of one list at random. Every name is a pair, the romanized name and the original one. |

A list with fewer names than asked for returns all of them. The model is told (see the *Name provider hint* of the Lore
Generator and the Arc Outline) to pick a name from what it gets and to write it as it was returned, with the original next
to it when that suits the setting; it may combine a forename with a surname of the same country.

## Lists

There are about 180 lists, named `<Country> - Surnames` and `<Country> - Forenames`: surnames of 75 countries and forenames of
106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). Forenames of both genders are in one list. A list has
from 5 to about 150 names, the most common ones of the country. The names are not made new: a name you get is a name that
people have.

The data is the [popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), which
takes them from Wikipedia's lists of the most common surnames and the most popular given names. The names written in
non-Latin scripts (Chinese, Japanese, Korean, Arabic, Cyrillic...) have a romanization, either one from the source or
one made by machine translation, so check the spelling when it matters.

## Usage

Load the extension in *Admin → Extensions*. It applies to the next generation, also when it is installed after the
extensions that use it or unloaded and installed again. Without it and without the Name generator there is no *Name tools*
button.
