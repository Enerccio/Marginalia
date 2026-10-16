# Name provider

Gives the model real names to pick from instead of inventing them: the most common forenames and surnames of many
countries, each as a pair of the romanized name and the original one. Where the [Name generator](../name-generator/README.md)
makes up new names that sound like a category, this one hands out names that exist.

## Functions

| Function | Arguments | Answer |
|---|---|---|
| `list_real_name_lists` | none | the lists (categories) of names that can be picked from |
| `pick_real_names` | `category`, `count` | up to `count` different names of the category chosen at random, each `{"romanized": "Satō", "original": "佐藤"}`; `count` is limited to 1-100 whatever the model asks for, a list with fewer names returns all of them |

The names of the functions differ from the ones of the Name generator, so a request can offer both.

## Categories

One category per country and kind, `<Country> - Surnames` or `<Country> - Forenames` (the country code of the data is
expanded to the English name of the country, `XK` is `Kosovo`): about 180, 75 countries with surnames and 106 with
forenames. Forenames of both genders are in one list.

## Data

The names are in `src/main/resources/name-lists.json` (committed, about 110 KB):

```json
{"categories": [{"title": "Japan - Surnames", "names": [["Satō", "佐藤"], ["Suzuki", "鈴木"]]}]}
```

Every name is an array of the romanized name and the original one (the same for a name written in Latin script). The
file is written by the build step (`exec-maven-plugin`, phase `generate-resources`, running
`src/build/ImportPopularNames.java`), which clones the
[popular names by country dataset](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0) into
`target/popular-names` (once, an existing clone is not pulled again; `mvn clean` or deleting it fetches the data anew)
and reads `common-surnames-by-country.csv` and `common-forenames-by-country.csv`. Of the columns only `Country`,
`Localized Name` (the original) and `Romanized Name` are used. A row without a romanized name is dropped, a row without
an original name takes the romanized one, a pair is kept once per category, and the names stay in the order of the file
(the most popular first). Without git or network the committed file is left as it is.

## For developers

`NameProviderFunctions` is the `@LLMFunction` interface, `NameProviderFunctionsImpl` the implementation (`MAX_COUNT`,
`clampCount`) on `NameLists` (loading and the random choice), `NameEntry` the answer. The extension compiles the interface
on load; `NameProviderExtension.functions()` returns the bound `FunctionSet` to pass to `InferenceService.stream`.
The [Lore Generator](../lore-generator/README.md) and the [Arc Outline](../arc-outline/README.md) offer the functions
to the model for the categories the user checks (prefix `Provider: `); they find this extension by reflection
(`NameToolsBridge`), so it can be unloaded and installed again without restarting them.
