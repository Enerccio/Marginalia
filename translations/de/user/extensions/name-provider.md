---
label: Namensanbieter
order: 6
---

# Namensanbieter

Liefert dem Modell echte Namen zur Auswahl, statt dass es welche erfindet: die häufigsten Vor- und Nachnamen vieler Länder. Während der [Namensgenerator](name-generator.md) neue Namen erfindet, die nur nach einer Kategorie klingen, gibt diese Erweiterung Namen aus, die tatsächlich existieren, jeweils mit der Originalschreibweise neben der romanisierten: `Satō` und `佐藤`.

Die Erweiterung hat keine eigene Oberfläche und nichts zu konfigurieren. Andere Erweiterungen stellen dem Modell ihre Funktionen zur Verfügung: der [Lore Generator](lore-generator.md#tools) und der [Arc Outline](arc-outline.md), wo du auswählst, welche Listen das Modell verwenden darf. Ihre Suite heißt *Name provider* im [Tools-Fenster](tools.md), neben der Suite *Name generator*. Du kannst Listen von beiden, von nur einer oder von keiner aktivieren.

<a id="what-the-model-can-do"></a>

## Was das Modell tun kann

| Funktion | Was sie tut |
|---|---|
| `list_real_name_lists` | Listet die Namenslisten auf; mit einem `filter` nur diejenigen, deren Name dessen Wörter enthält. |
| `pick_real_names` | Wählt bis zu 100 verschiedene Namen einer Liste zufällig aus. Jeder Name ist ein Paar aus romanisiertem Namen und Originalname. |

Enthält eine Liste weniger Namen als angefordert, werden alle zurückgegeben. Dem Modell wird (siehe den Hinweis der Suite *Name provider* im [Tools-Fenster](tools.md#choosing-the-tools)) mitgeteilt, einen Namen aus dem Erhaltenen auszuwählen und ihn so zu schreiben, wie er zurückgegeben wurde, mit dem Original daneben, wenn das zur Welt passt. Es darf einen Vornamen mit einem Nachnamen desselben Landes kombinieren.

<a id="commands"></a>

## Befehle

Dieselben Namen sind als [Befehle](../commands/index.md) verfügbar, für die Kommandozeile und für [Agenten](../agents.md), die sie in ihren eigenen Prompts verwenden können (zum Beispiel für `/generate-lore-entry` des [Lore Generators](lore-generator.md)):

| Befehl | Was er tut |
|---|---|
| `/list-real-name-lists [<filter>]` | Gibt die Listen als JSON-Array aus; mit einem Filter nur diejenigen, deren Name alle Wörter enthält (jeweils am Wortanfang, in beliebiger Reihenfolge, ohne Beachtung der Groß- und Kleinschreibung). |
| `/pick-real-names list count` | Wählt bis zu 100 Namen der Liste zufällig aus und gibt sie als JSON-Array von Paaren zurück, dem romanisierten Namen und dem Originalnamen. |

Beide lesen nur.

<a id="lists"></a>

## Listen

Es gibt rund 180 Listen mit den Namen `<Country> - Surnames` und `<Country> - Forenames`: Nachnamen aus 75 Ländern und Vornamen aus 106 (`Japan - Surnames`, `Czechia - Forenames`, `Kosovo - Surnames`). Vornamen beider Geschlechter stehen in einer Liste. Eine Liste hat 5 bis etwa 150 Namen, die häufigsten des Landes. Die Namen werden nicht neu erfunden: Ein Name, den du erhältst, ist ein Name, den Menschen tatsächlich tragen.

Einige Dutzend Einträge sind **Gruppen** von Listen nach Sprache: `<Language> - Forenames` und `<Language> - Surnames` für Spanisch (18 Länder), Portugiesisch, Französisch, Deutsch, Englisch, Arabisch, West-, Ost- und Südslawisch, Nordgermanisch, Nordisch, Uralisch, Baltisch, Turkisch, Persisch, Chinesisch, Südasiatisch, Südostasiatisch, Kaukasisch, Albanisch und Rumänisch (sofern es solche Listen gibt). Sie sind wie jede Liste aufgeführt und im [Tools-Fenster](tools.md) ein einzelner Eintrag. Jeder Name einer Gruppe wird aus einer ihrer Listen gewählt, die zufällig bestimmt wird, wobei jedes Land die gleiche Chance hat. Eine Anfrage nach 20 Namen liefert also Namen aus mehreren Ländern, nie denselben Namen zweimal. Länder, in denen mehrere Sprachen gesprochen werden (Belgien, Luxemburg, Malta, Andorra), gehören zu keiner Gruppe. Eine Gruppe ist eine Zeile in `name-groups.json`, siehe das [Plugin-README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-provider/README.md#groups).

Die Daten stammen aus dem [Datensatz der beliebtesten Namen nach Land](https://github.com/sigpwned/popular-names-by-country-dataset) (CC0), der die Namen aus Wikipedias Listen der häufigsten Nachnamen und der beliebtesten Vornamen übernimmt. Namen in nichtlateinischen Schriften (Chinesisch, Japanisch, Koreanisch, Arabisch, Kyrillisch ...) haben eine Romanisierung, entweder aus der Quelle oder durch maschinelle Übersetzung erzeugt. Prüfe die Schreibweise, wenn sie wichtig ist.

<a id="usage"></a>

## Verwendung

Lade die Erweiterung unter *Admin → Extensions*. Sie wirkt bei der nächsten Generierung, auch wenn sie nach den Erweiterungen installiert wird, die sie nutzen, oder wenn sie entladen und erneut installiert wurde. Ohne sie und die anderen Suiten gibt es keinen *Tools*-Button.
