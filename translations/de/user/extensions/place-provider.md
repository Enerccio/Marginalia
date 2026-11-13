---
label: Ortsanbieter
order: 5
---

# Ortsanbieter

Gibt dem Modell echte Orte der Erde zum Nachschlagen an die Hand, statt sie zu erfinden: Städte, Länder, Meere und Buchten, Flüsse, Seen,
Inseln, Berge, Wüsten, Häfen und Flughäfen, mit ihren Koordinaten und den Ländern, in denen sie liegen. Er kann außerdem sagen, was
um einen Ort herum liegt und wie weit zwei Orte voneinander entfernt sind, sodass eine Geschichte, die auf der realen Karte spielt,
die richtigen Entfernungen und Nachbarn erhält.

Die Erweiterung hat keine eigene Oberfläche und nichts zu konfigurieren. Der [Lore-Generator](lore-generator.md#tools) und der
[Bogenumriss](arc-outline.md) bieten sie dem Modell als Suite *Place provider* über ihre Schaltfläche **Tools** an
([Werkzeuge für das Modell](tools.md)), ein- oder ausgeschaltet; außerdem gibt es Befehle für [Agenten](../agents.md) und die Kommandozeile.

<a id="what-it-knows"></a>

## Was sie kennt

Die Daten stammen von [Natural Earth](https://www.naturalearthdata.com/) (gemeinfrei) und bilden eine Karte der **bemerkenswerten** Orte ab:
etwa 18 000, darunter 4 900 Städte, 250 Länder, 4 500 Provinzen und Bundesstaaten, 1 100 Flüsse, 500 Seen, 400 Inseln, 350 Berge und
Gebirgszüge, 1 000 Häfen und 900 Flughäfen. Es ist kein Ortsverzeichnis: ein kleiner Bach oder ein Dorf ist nicht enthalten. Natural Earth
zeichnet außerdem die meisten Einbuchtungen als Buchten, sodass die Anfrage nach den Einbuchtungen Kanadas die vier liefert, die es kennt
(Hamilton Inlet, Bathurst Inlet, Minto Inlet und Richard Collinson Inlet), nicht die Hunderte an der Küste von British Columbia.

<a id="commands"></a>

## Befehle

Alle lesen nur.

| Befehl | Was er tut |
|---|---|
| `/list-place-types` | Listet die Arten von Orten (`city`, `capital`, `bay`, `inlet`, `river`, `mountain range`, `island`...) mit ihrer Anzahl auf. |
| `/search-places query [type [country [limit]]]` | Sucht nach Name, Art und Land, der beste Treffer zuerst. `*` als Suchbegriff steht für einen beliebigen Namen: `/search-places * inlets Canada` listet die Einbuchtungen Kanadas auf. |
| `/places-near place radius_km [type [limit]]` | Die Orte im Umkreis eines Ortes oder eines Punktes, der nächste zuerst, mit der Entfernung in Kilometern und der Richtung. |
| `/distance-between from to` | Die Entfernung in einer geraden Linie über die Erde (nicht eine Straße) in Kilometern und Meilen, mit der Richtung. |

Ein *Ort* ist ein Name (`Ottawa`; `Paris, France`, wenn mehrere Orte den Namen tragen: das Komma und das Land oder die Provinz wählen
einen aus) oder Koordinaten (`45.42, -75.69`). Eine *Art* kann im Plural oder so geschrieben werden, wie man sie im Alltag sagt: `inlets`,
`mountains`, `towns`, `states`. Ein *Land* ist ein Name oder ein Code: `Canada`, `Czechia`, `CA`, `CAN`. Namen werden ohne Beachtung von
Groß- und Kleinschreibung und Akzenten verglichen. Es kommen höchstens 100 Orte zurück (25 ohne Grenze), und die Antwort sagt, wie viele
insgesamt passen.

Eine Suche nach einer Art findet auch die Orte einer ähnlichen Gewässerart, die das Wort im Namen tragen: `inlet` findet Hamilton Inlet,
das Natural Earth als Bucht bezeichnet.

<a id="using-it-from-an-agent"></a>

## Verwendung durch einen Agenten

Ein Agent, der die Befehle erlaubt bekommen hat (sie sind in der Auswahl *All non-destructive* und in der Rolle *Auditor* enthalten), kann
sie als Funktionen aufrufen. Sie sind für einen Auftrag gedacht wie „Lege das erste Kapitel in ein Fischerdorf an einem Fjord in Norwegen,
zwei Tagesreisen auf dem Seeweg von der Hauptstadt entfernt“: Der Agent sucht `fjords of Norway`, wählt einen aus und fragt die Entfernung
nach Oslo ab.

<a id="usage"></a>

## Verwendung

Lade die Erweiterung unter *Admin → Extensions*. Im Lore-Generator und im Bogenumriss setzt du unter **Tools** den Haken bei *Diese Suite verwenden*
der Suite Place provider; ihr Hinweis weist das Modell an, einen echten Ort nachzuschlagen, statt zu raten, wo er liegt oder wie weit er entfernt ist.
