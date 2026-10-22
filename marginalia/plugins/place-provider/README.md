# Place provider

Gives the model, and agents and the command bar, real places of the Earth to look up instead of inventing them: a search by
name, type and country (`inlets of Canada`, `fjords of Norway`, `mountains of Japan`, `ports of Italy`), the places around a point
and the distance of two places. The data is [Natural Earth](https://www.naturalearthdata.com/) (public domain), the 1:10m
layers, which is a map of the **notable** places: 18 000 of them, not a gazetteer. A small creek or a village is not there, and
Natural Earth draws most inlets as bays, so the search for inlets of Canada finds four (Hamilton, Bathurst, Minto and Richard
Collinson Inlet).

## Functions

| Function | Arguments | Answer |
|---|---|---|
| `list_place_types` | none | the types of places with how many there are of each |
| `search_places` | optional `query`, `type`, `country`, `limit` (at least one of the first three) | `{total, places}`, the best matches first |
| `places_near` | `place`, `radius_km`, optional `type`, `limit` | `{origin, total, places}`, each with `km` and the compass `direction` from the origin, the closest first |
| `distance_between` | `from`, `to` | `{from, to, km, miles, direction}` |

A place is `{name, type, lat, lon, bbox, countries, alt, within, pop, elevation, code}`; everything but the first four may be
missing and is then left out. `bbox` is `[west, south, east, north]` of a line or an area (a river, a bay, a country),
`countries` are the countries it is in or touches (the one with most of it first, at most 12), `within` the province of a city or
the continent of a physical feature, `code` the IATA code of an airport or the ISO code of a country.

- **query**: part of a name, compared without case, accents and punctuation, in the name and in the other names; `*` or nothing is
  any name. A name equal to the query comes before one that starts with it, then ones that have it as a word, then ones that
  contain it; among equals the bigger places (population, elevation, the scale rank of Natural Earth) come first.
- **type**: a type from `list_place_types`, its plural, or what people call it: `mountains` (mountains, peaks and ranges), `towns`,
  `states` (provinces), `islands`... For the water types the places of another water type that have the word in their name count
  too: `inlet` finds `Hamilton Inlet`, which the data has as a `bay`, and `river` finds `St. Lawrence River`, a `channel`.
- **country**: a name, an alternative name or a code (`Canada`, `Czechia`, `Czech Republic`, `CZ`, `CZE`); a text that is part of
  only one name is enough (`Korea` is not, there are two). A place counts when it is in the country or touches it.
- **place** (`places_near`, `distance_between`): a name, `Paris, France` (a comma and the country or the province picks one
  of several places of the name; without it the best match, then the biggest, is taken and the answer says which it was), or
  coordinates `45.42, -75.69`.

At most 100 places come back whatever the model asks for (25 without a limit); `total` says how many fit. A wrong question (an
unknown type or country, a place that is not there) fails the call with a message that says what is accepted.

## Commands

The same for the command bar and for agents (`PlaceProviderCommands`, all `NON_DESTRUCTIVE`): `/list-place-types`,
`/search-places query [type [country [limit]]]`, `/places-near place radius_km [type [limit]]` and `/distance-between from to`.
The answers are JSON with a place on each line. Give `*` as the query to search by type or country alone.

## Types

About 50. Settlements: `city`, `capital`, `provincial capital`, `station`, `historic place`; administrative: `country`, `province`; water:
`ocean`, `sea`, `gulf`, `bay`, `inlet`, `strait`, `sound`, `channel`, `fjord`, `lagoon`, `reef`, `lake`, `reservoir`, `river`; land:
`continent`, `island`, `island group`, `peninsula`, `cape`, `isthmus`, `mountain`, `mountain range`, `plateau`, `desert`,
`plain`, `delta`, `valley`... ; and `port`, `airport`, `park`.

## For developers

`PlaceProviderFunctions` is the `@LLMFunction` interface, `PlaceProviderFunctionsImpl` the implementation on `PlaceIndex` (loading,
the search, the distances), `Place` and the records `PlaceSearch`, `Nearby`, `NearbyPlace`, `Distance` and `TypeCount` the answers. The
extension compiles the interface on load and registers a `PlaceProviderToolProvider` with the tool service (`ToolService`, key
`places`, human name *Place provider*): a suite that is on or off, whose functions are the interface bound to `PlaceProviderFunctionsImpl`
and which brings `PlaceProviderCommands` along, so one registration gives the command bar and agents the commands and every dialog with
a **Tools** button (the Lore Generator, the Arc Outline) the functions. The service removes both when the extension is unloaded.

## Data

The places are in `src/main/resources/places.json` (committed, about 2.3 MB, one place on each line):

```json
{"source": "...", "countries": [{"name": "Canada", "iso2": "CA", "iso3": "CAN", "alt": ["Canada"]}], "places": [
{"name": "Hudson Bay", "type": "bay", "lat": 59.643, "lon": -85.152, "bbox": [-94.96, 54.7, -76.52, 66.02], "countries": ["Canada"]}
]}
```

A normal build does not touch it. The two steps that write it run only with the profile `import-places`
(`mvn package -Pimport-places`; the executions of `exec-maven-plugin` are bound to `generate-resources` through the property
`places.import.phase`, `none` by default):

1. `src/build/FetchNaturalEarth.java` downloads the 12 layers it needs of the
   [natural-earth-geojson](https://github.com/martynafford/natural-earth-geojson) repository (CC0, about 80 MB) into
   `target/natural-earth`, once (delete a file to fetch it again). Only the listed files, not the repository. A file that cannot
   be downloaded is reported and skipped.
2. `src/build/BuildPlaces.java` reads them and writes `places.json`, without the geometry. Without all the files the output
   is left as it is, so a clone builds without network. It takes a few seconds.

Natural Earth does not say which country a feature is in, so `BuildPlaces` finds it from the polygons of `admin_0_countries`: a
point is in the country that contains it (else in the nearest one within 0.5 degrees, the coastline is generalized); a line or an area
takes the countries of 24 vertices and of its label point (within 0.3 degrees), which is how a bay, whose label point is in the
water, is found to be Canadian. Pieces of one name and type whose boxes touch (a river is many lines) are one place. The names
Natural Earth writes in capitals are put in title case; a country is called by its long name (`Democratic Republic of the Congo`).

Layers: countries, admin-1 label points (provinces, once each), populated places (`populated_places_simple`), ports, airports,
parks and protected lands, marine polygons, regions (polygons, points and elevation points), lakes and the river
centerlines.
