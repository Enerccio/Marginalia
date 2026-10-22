---
label: Place provider
order: 86
---

# Place provider

Gives the model real places of the Earth to look up instead of inventing them: cities, countries, seas and bays, rivers, lakes,
islands, mountains, deserts, ports and airports, with their coordinates and the countries they are in. It can also say what lies
around a place and how far apart two places are, so a story set on the real map gets its distances and neighbours right.

The extension has no screen of its own and nothing to configure. The [Lore Generator](lore-generator.md#tools) and the
[Arc Outline](arc-outline.md) offer it to the model as the suite *Place provider* of their **Tools** button
([Tools for the model](tools.md)), on or off; it is also commands for [agents](../agents.md) and the command line.

## What it knows

The data is [Natural Earth](https://www.naturalearthdata.com/) (public domain), which is a map of the **notable** places: about
18 000, among them 4 900 cities, 250 countries, 4 500 provinces and states, 1 100 rivers, 500 lakes, 400 islands, 350 mountains and
ranges, 1 000 ports and 900 airports. It is not a gazetteer: a small creek or a village is not there. Natural Earth also draws
most inlets as bays, so asking for the inlets of Canada gives the four it has (Hamilton Inlet, Bathurst Inlet, Minto Inlet and
Richard Collinson Inlet), not the hundreds of the coast of British Columbia.

## Commands

All of them only read.

| Command | What it does |
|---|---|
| `/list-place-types` | Lists the types of places (`city`, `capital`, `bay`, `inlet`, `river`, `mountain range`, `island`...) with how many there are. |
| `/search-places query [type [country [limit]]]` | Searches by name, type and country, the best match first. `*` as the query is any name: `/search-places * inlets Canada` lists the inlets of Canada. |
| `/places-near place radius_km [type [limit]]` | The places within the radius of a place or a point, the closest first, with the distance in kilometers and the direction. |
| `/distance-between from to` | The distance in a straight line over the Earth (not a road) in kilometers and miles, with the direction. |

A *place* is a name (`Ottawa`; `Paris, France` when several have the name, the comma and the country or province pick one) or
coordinates (`45.42, -75.69`). A *type* can be written in the plural or as people say it: `inlets`, `mountains`, `towns`, `states`. A
*country* is a name or a code: `Canada`, `Czechia`, `CA`, `CAN`. Names are compared without case and accents. At most 100 places
come back (25 without a limit), and the answer says how many fit in all.

A search for a type also finds the places of a similar water type that have the word in their name: `inlet` finds Hamilton Inlet,
which Natural Earth calls a bay.

## Using it from an agent

An agent that is allowed the commands (they are in the *All non-destructive* selection and the *Auditor* role) can call them as
functions. They are made for a prompt like "set the first chapter in a fishing village on a fjord in Norway, two days by sea from
the capital": the agent searches `fjords of Norway`, picks one and asks for the distance to Oslo.

## Usage

Load the extension in *Admin → Extensions*. In the Lore Generator and the Arc Outline check *Use this suite* of the Place
provider in **Tools**; its hint tells the model to look a real place up instead of guessing where it is or how far.
