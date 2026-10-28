# Group logic

How groups of categories work in the name generator, why the shipped groups look the way they do, and how to change them.
The [Name provider](../name-provider/README.md) has the same idea without weights (see the last section).

## What a group is

A group is an entry of the category list (`list_name_categories`, `/list-name-categories`, one item of the tool dialog) that has
no chain of its own. It is a list of member categories. For **every name** `generate_names` makes, it

1. picks one member of the group at random (how, see *Weights*),
2. makes the name with the chain of that member, within the usual lengths of that member (or the `min_length` / `max_length`
   the caller gave),
3. drops duplicates across the whole call, like for a plain category.

So a name always comes from one language and never from a blend of several chains, one call returns names of several members,
and the attempts budget (`ATTEMPTS_PER_NAME` per wanted name) is shared by the whole group. A group that cannot make enough
different names returns fewer, as a small category does.

The point is discoverability and variety: the Onomastikon is split into hundreds of small lists (a country into Biblical, Germanic,
Greek and Latin origins, India's male names into four alphabetical files, the saints into 25 letters), and a model that wants
"a German name" should not have to know them.

## Files and format

| File | Content |
|---|---|
| `src/main/resources/name-groups.json` | the groups, committed, read at run time (no training, no build step) |
| `src/main/resources/name-tables.json` | the chains; every category has a `size` since this change |
| `src/build/GenerateTables.java` | writes `size` (the number of names the chain was trained on) |
| `src/test/java/.../NameGroupsTest.java` | the checks below |

```json
{"groups": [
  {"title": "Native American - Male", "members": ["America - Northern - * - Male"]},
  {"title": "German - Male", "members": ["Europe Western - Germany - * Male", "Europe Western - Austria - First names - Male"], "weight": "size"}
]}
```

- `members` are patterns for **whole category titles**, not case sensitive; `*` stands for any text (also none), so a title
  without `*` names exactly one category. A pattern that matches nothing is simply empty, the test below catches it.
- `weight` is `"equal"` (default) or `"size"`; anything else fails to load.
- A group with no resolved member, or with the title of a category, is **left out** (a plugin that does not load is worse than a
  missing group). Groups cannot contain groups (no cycles, nothing to resolve twice).
- Titles are not uniform: genders are `Male`, `Celtic Male`, `Aragonese Names Male`, `Male 'a'`, `Names - Male`. `* Male` (with the
  space) catches `Celtic Male` and `Biblical - Male` and not `Female`; a title like `Indonesia - Male` needs its own pattern, because
  `Indonesia - * Male` needs two spaces.

## Weights

`equal`: every member has the same chance. `size`: the chance is proportional to `size`, a member of unknown size counts as one name
(`NameTables.cumulativeSizes`, `member`; the roll is a `long`).

Why both: with equal chance a list of 10 names is chosen as often as one of 600, which is right when a member is a whole
language or country, and wrong when the members are slices of one language's names.

| Use `size` | Use `equal` |
|---|---|
| a country split by origin (Spain: Biblical 175, Latin 219, Catalonia 15...) | members that are languages or countries (Caucasian: Armenia 994, Georgia 22) |
| India male `a` 1080, `b` 463, `c-k` 1378, `l-z` 358; Saints `A` 970 ... `Q` 28 | Turkic, Baltic, Polynesian, the African regions, South Asian, Southeast Asian |
| the English surname kinds, Anglo-Saxon, Medieval English, Irish, Welsh, the Slavic groups | Native American (one nation = one language, the lists are short), Deities, Fantasy Creatures, Medieval Germanic, Ancient Near East |

With `size` a huge member can crowd the others out (it is the reason Caucasian, Turkic and the African regions are `equal`), with
`equal` a tiny list of 10 names is repeated more often than its names can give. There is no automatic rule; when a group feels
wrong, look at the sizes in the table and switch the weight.

## The shipped groups (167: 113 `size`, 54 `equal`)

Names is the total of the member sizes (one name in two members counts twice).

| Group | Weight | Members | Names |
|---|---|---:|---:|
| Native American - Male | equal | 6 | 220 |
| Native American - Female | equal | 5 | 160 |
| Native American - Surnames | equal | 2 | 37 |
| Native American (Central and South America) - Male | equal | 4 | 305 |
| Native American (Central and South America) - Female | equal | 4 | 114 |
| German - Male | size | 8 | 825 |
| German - Female | size | 8 | 881 |
| German - Surnames | size | 6 | 2348 |
| French - Male | size | 7 | 781 |
| French - Female | size | 7 | 661 |
| French - Surnames | size | 3 | 1953 |
| Spanish - Male | size | 9 | 907 |
| Spanish - Female | size | 9 | 764 |
| Spanish - Surnames | size | 13 | 1485 |
| Italian - Male | size | 5 | 1039 |
| Italian - Female | size | 5 | 544 |
| Italian - Surnames | size | 2 | 1892 |
| Portuguese - Male | size | 5 | 255 |
| Portuguese - Female | size | 4 | 91 |
| Portuguese - Surnames | size | 3 | 276 |
| Dutch - Male | size | 6 | 578 |
| Dutch - Female | size | 6 | 439 |
| Dutch - Surnames | size | 3 | 1392 |
| Scandinavian - Male | size | 22 | 1060 |
| Scandinavian - Female | size | 22 | 1083 |
| Scandinavian - Surnames | size | 9 | 1330 |
| Finnic - Male | size | 5 | 463 |
| Finnic - Female | size | 5 | 455 |
| Finnic - Surnames | size | 4 | 689 |
| Baltic - Male | equal | 2 | 225 |
| Baltic - Female | equal | 2 | 38 |
| Baltic - Surnames | equal | 4 | 624 |
| Irish - Male | size | 6 | 1041 |
| Irish - Female | size | 5 | 642 |
| Irish - Surnames | size | 4 | 2581 |
| Scottish - Male | size | 6 | 513 |
| Scottish - Female | size | 5 | 302 |
| Scottish - Surnames | size | 3 | 949 |
| Welsh - Male | size | 5 | 755 |
| Welsh - Female | size | 4 | 304 |
| Welsh - Surnames | size | 2 | 249 |
| Celtic - Male | size | 20 | 4414 |
| Celtic - Female | size | 17 | 1742 |
| Celtic - Surnames | size | 11 | 4131 |
| West Slavic - Male | size | 11 | 825 |
| West Slavic - Female | size | 10 | 625 |
| West Slavic - Surnames | size | 9 | 1429 |
| East Slavic - Male | size | 7 | 825 |
| East Slavic - Female | size | 7 | 487 |
| East Slavic - Surnames | size | 6 | 1607 |
| South Slavic - Male | size | 8 | 642 |
| South Slavic - Female | size | 7 | 313 |
| South Slavic - Surnames | size | 15 | 1591 |
| Greek - Male | size | 3 | 229 |
| Greek - Female | size | 4 | 231 |
| Greek - Surnames | size | 2 | 400 |
| Hungarian - Male | size | 6 | 534 |
| Hungarian - Female | size | 5 | 164 |
| Hungarian - Surnames | size | 2 | 321 |
| Romanian - Male | size | 1 | 117 |
| Romanian - Female | size | 1 | 94 |
| Romanian - Surnames | size | 4 | 319 |
| Albanian - Male | size | 1 | 232 |
| Albanian - Female | size | 1 | 69 |
| Albanian - Surnames | size | 3 | 324 |
| Caucasian - Male | equal | 4 | 1145 |
| Caucasian - Female | equal | 3 | 502 |
| Caucasian - Surnames | equal | 6 | 2928 |
| Turkic - Male | equal | 4 | 978 |
| Turkic - Female | equal | 2 | 683 |
| Turkic - Surnames | equal | 8 | 515 |
| Persian - Male | size | 1 | 387 |
| Persian - Female | size | 1 | 429 |
| Persian - Surnames | size | 1 | 188 |
| Arabic - Male | size | 6 | 713 |
| Arabic - Female | size | 4 | 454 |
| Arabic - Surnames | size | 6 | 283 |
| Jewish - Male | size | 3 | 849 |
| Jewish - Female | size | 3 | 924 |
| Jewish - Surnames | size | 2 | 512 |
| Hindu - Male | size | 4 | 3279 |
| Hindu - Female | size | 1 | 1324 |
| Hindu - Surnames | size | 2 | 1986 |
| South Asian - Male | equal | 6 | 3696 |
| South Asian - Female | equal | 2 | 1421 |
| South Asian - Surnames | equal | 6 | 2383 |
| Chinese - Surnames | size | 3 | 345 |
| Japanese - Surnames | size | 3 | 1167 |
| Korean - Surnames | size | 2 | 215 |
| Vietnamese - Surnames | size | 2 | 124 |
| Southeast Asian - Male | equal | 6 | 503 |
| Southeast Asian - Female | equal | 4 | 221 |
| Southeast Asian - Surnames | equal | 6 | 349 |
| Malay-Indonesian - Male | size | 3 | 162 |
| Malay-Indonesian - Female | size | 2 | 25 |
| Malay-Indonesian - Surnames | size | 2 | 85 |
| Polynesian - Male | equal | 5 | 211 |
| Polynesian - Female | equal | 4 | 110 |
| Polynesian - Surnames | equal | 4 | 172 |
| West African - Male | equal | 9 | 832 |
| West African - Female | equal | 4 | 493 |
| West African - Surnames | equal | 7 | 307 |
| East African - Male | equal | 8 | 686 |
| East African - Female | equal | 7 | 392 |
| East African - Surnames | equal | 7 | 365 |
| Southern African - Male | equal | 7 | 275 |
| Southern African - Female | equal | 4 | 161 |
| Southern African - Surnames | equal | 7 | 420 |
| North African - Male | equal | 4 | 383 |
| North African - Female | equal | 3 | 186 |
| North African - Surnames | equal | 5 | 205 |
| Central African - Male | equal | 3 | 50 |
| Central African - Female | equal | 1 | 18 |
| Central African - Surnames | equal | 4 | 122 |
| Anglo-Saxon - Male | size | 3 | 2916 |
| Anglo-Saxon - Female | size | 3 | 854 |
| Medieval Germanic - Male | equal | 4 | 1112 |
| Medieval Germanic - Female | equal | 4 | 627 |
| Medieval English - Male | size | 10 | 2616 |
| Medieval English - Female | size | 10 | 2584 |
| Medieval English - Surnames | size | 2 | 611 |
| Ancient Near East - Male | equal | 8 | 554 |
| Ancient Near East - Female | equal | 6 | 107 |
| English - Surnames | size | 12 | 3448 |
| Saints | size | 25 | 6219 |
| Deities | equal | 7 | 803 |
| Fantasy Creatures | equal | 6 | 894 |
| Game of Thrones Houses | size | 9 | 244 |
| Native American - Forenames | equal | 11 | 380 |
| Native American (Central and South America) - Forenames | equal | 8 | 419 |
| German - Forenames | size | 17 | 2038 |
| French - Forenames | size | 15 | 1720 |
| Spanish - Forenames | size | 20 | 2613 |
| Italian - Forenames | size | 11 | 1923 |
| Portuguese - Forenames | size | 9 | 346 |
| Dutch - Forenames | size | 13 | 1260 |
| Scandinavian - Forenames | size | 45 | 2344 |
| Finnic - Forenames | size | 10 | 918 |
| Baltic - Forenames | equal | 4 | 263 |
| Irish - Forenames | size | 12 | 2167 |
| Scottish - Forenames | size | 11 | 815 |
| Welsh - Forenames | size | 9 | 1059 |
| Celtic - Forenames | size | 38 | 6640 |
| West Slavic - Forenames | size | 21 | 1450 |
| East Slavic - Forenames | size | 15 | 1572 |
| South Slavic - Forenames | size | 15 | 955 |
| Greek - Forenames | size | 7 | 460 |
| Hungarian - Forenames | size | 11 | 698 |
| Romanian - Forenames | size | 2 | 211 |
| Albanian - Forenames | size | 2 | 301 |
| Caucasian - Forenames | equal | 7 | 1647 |
| Persian - Forenames | size | 2 | 816 |
| Arabic - Forenames | size | 10 | 1167 |
| Jewish - Forenames | size | 6 | 1773 |
| Hindu - Forenames | size | 6 | 4912 |
| Southeast Asian - Forenames | equal | 10 | 724 |
| Polynesian - Forenames | equal | 9 | 321 |
| East African - Forenames | equal | 15 | 1078 |
| North African - Forenames | equal | 7 | 569 |
| Anglo-Saxon - Forenames | size | 6 | 3770 |
| Medieval Germanic - Forenames | equal | 8 | 1739 |
| Medieval English - Forenames | size | 20 | 5200 |
| Ancient Near East - Forenames | equal | 14 | 661 |
| Chinese - Forenames | size | 2 | 852 |
| Japanese - Forenames | size | 3 | 2776 |
| Korean - Forenames | size | 2 | 334 |
| Vietnamese - Forenames | size | 2 | 193 |

### Choices behind them

- **`- Male` and `- Female` never mix genders.** The Onomastikon keeps the genders in separate lists. The unisex lists (`Spanish Forenames`,
  `German Forenames`, `Japanese Forenames`, `Humans - First Names`, `Sweden - Norse`, `Germany - Names`...) are in no `- Male` or `- Female`
  group. `NameGroupsTest.shippedGroupsKeepTheirGender` fails when a `- Male` group has a female or surname member and the other way
  round, or a `- Forenames` group a surname one.
- **`<Language> - Forenames` is the any-gender version.** Its members are the members of the `- Male` group, of the `- Female` group and
  the unisex lists of the language (`German Forenames`; `Spanish Forenames` and `Humans - Spanish First Names`; the Italian, French, Dutch,
  Swedish, Irish and Russian ones; `Indian Forenames` and `India - Sikh - First Names` for South Asian; `Japanese Forenames`), with the weight of
  the language group; `Chinese`, `Japanese`, `Korean` and `Vietnamese` are made of their male and female categories (and `Japanese Forenames`). The
  share of the genders is not a choice but follows from the weight: with `size` it is the share of the male lists in the names of both
  (male share: Persian 47%, Jewish 48%, German 48%, Scandinavian 49%, Greek 50%, Medieval English 50%, Finnic 50%, French 54%, Spanish 54%, Romanian 55%, Dutch 57%, West Slavic 57%, Arabic 61%, Irish 62%, East Slavic 63%, Scottish 63%, Italian 66%, South Slavic 67%, Hindu 71%, Welsh 71%, Celtic 72%, Portuguese 74%, Hungarian 77%, Albanian 77%, Anglo-Saxon 77%), with `equal` it is the share of the male members among all the members. A group is **not made** when the genders
  would be very unequal: `equal` with the male members under 40 % or over 60 % of the members (Turkic 4 male to 2 female members, South Asian
  6 to 2, West African 9 to 4, Southern African 7 to 4, Central African 3 to 1), `size` with under 20 % or over 80 % men (`Malay-Indonesian`,
  162 male names to 25 female). Ask for `- Male` and `- Female` separately where a balance matters. The unisex list `Icelandic Forenames`
  (3896 names) is in no group, it would outweigh the other Scandinavian lists three to one.
- **A language has a group only when it adds something**: `Chinese`, `Japanese`, `Korean` and `Vietnamese` have only
  `- Surnames` (the Onomastikon list and the country dataset together), because their given names are one category each.
  A group with a single member (Persian, `Romanian - Male`/`- Female`, `Albanian - Male`/`- Female`, `Hindu - Female`, `Central African - Female`) is only
  an alias: it makes the language name find the category (`persian`, `romanian male`), the logic is the same. The Name provider
  does not make such groups.
- **Origin buckets stay in the national groups.** `German - Male` includes the Biblical, Greek and Latin buckets of Germany: they are
  the German forms of international names (Michael, Peter), which is what a story set in Germany needs.
- **Surnames of the country dataset** (`<Country> - Surnames`, 6 to 150 names) are members of the surname groups too (Spanish has 11
  Latin American ones). With `size` they hardly count (6 to 150 names against hundreds); in an `equal` group (Turkic, Caucasian, South
  Asian, Southeast Asian) each of them is a full member, so Bangladesh's 6 surnames get one share of `South Asian - Surnames`. That is
  a known weakness of `equal` there; move them out of the pattern list if it shows.
- **Leftovers** with no group (single categories or tiny lists): Basque, Old Norse, Tibetan, Mongolian, Madagascar, the Byzantine and
  medieval French/Italian lists, Belgium (mixed Dutch and French), Malta, Luxembourg, Fiji.
- **Native American** is `equal`: northern America (Algonquin, Apache, Cherokee, Inuit, Iroquois, `Native`, Sioux) and, separately,
  Central and South America (Aztec, Maya, Inca, Amazonian). The sources are short and partly English translations (*Calf-Woman*), so
  the names are rough; a Wikidata or native-languages.org feed would be new member categories, not a change of the logic.

## Checks

`NameGroupsTest` (JUnit 6, `mvn test` in the plugin):

- patterns match whole titles, with `*`, not case sensitive, `.` and other regex characters are literal;
- a group is listed, found by the filter, `has` / `isGroup` / `members`;
- a group without members or with the title of a category is left out;
- every name comes from a member and every member is used; a group does not take members outside its patterns;
- `weight` is read, a wrong value fails; `size` gives about 89 % / 10 % / 1 % for sizes 90 / 10 / unknown, `equal` a third each;
- the shipped file: every group resolves, every pattern matches a category, every group makes names, no group has the wrong gender (and `German - Forenames` has the male, the female and the unisex members, `Turkic - Forenames` is not made).

## Changing the groups

1. Edit `name-groups.json` (no training needed). Run `mvn test`; a pattern with no category or a wrong-gender member fails.
2. After a retraining (`mvn package -Ptrain-names`) a new source file joins a group when its title matches a pattern. `size` is
   rewritten from the sources; the tables of an unchanged source are byte for byte the same.
3. Sizes came from `GenerateTables` run over the local sources, and the result was identical to the committed
   `name-tables.json` except for the new `size` on all 1271 categories (checked before it was installed).
4. Update the groups paragraph in `README.md` and in `manual/user/extensions/name-generator.md` when groups are added or removed.

## The same in the Name provider

`name-provider/src/main/resources/name-groups.json` has the same format without `weight`: the members are lists of real names, a
pick takes names from a member chosen at random among those that still have names (equal chance, no repeats, all the names when
the group has fewer than asked for). The 38 groups are `<Language> - Forenames` and `- Surnames`; a group with fewer than two lists
is not made. See `NameLists.pick`.
