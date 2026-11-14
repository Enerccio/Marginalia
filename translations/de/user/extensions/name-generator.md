---
label: Namensgenerator
order: 7
---

# Namensgenerator

Gibt dem Modell eine Möglichkeit, Namen anzufordern, statt sie zu erfinden. Die Namen stammen aus Listen echter Namen – Vor- und Nachnamen vieler Länder, Orte, Gottheiten, Monster, Drogen, Firmen, Speisen und mehr – und werden mit einer Markow-Kette neu gebildet, sodass sie wie die Kategorie klingen, ohne Kopien davon zu sein.

Die Erweiterung hat keine eigene Oberfläche und nichts zu konfigurieren. Andere Erweiterungen stellen dem Modell ihre Funktionen zur Verfügung; derzeit sind das der [Lore-Generator](lore-generator.md#tools) und der [Handlungsbogen-Entwurf](arc-outline.md), wo du auswählst, welche Kategorien das Modell verwenden darf (siehe [Werkzeuge für das Modell](tools.md)). Ihre Suite heißt *Name generator*, neben dem *Name provider* des [Namensanbieters](name-provider.md), der echte statt erfundener Namen liefert.

![Auswahl der Namenskategorien im Werkzeugfenster des Lore-Generators](../../images/tools-dialog.png)

<a id="what-the-model-can-do"></a>

## Was das Modell tun kann

| Funktion | Was sie tut |
|---|---|
| `list_name_categories` | Listet die Namenskategorien auf; mit einem `filter` nur diejenigen, deren Namen dessen Wörter enthalten. |
| `generate_names` | Erzeugt bis zu 100 neue Namen einer Kategorie. Optional mit kürzester und längster Länge. |

Ohne Längenangabe sind die Namen so lang, wie die Namen dieser Kategorie üblicherweise sind (eine Kategorie langer Firmennamen ergibt lange Namen, eine mit kurzen Vornamen kurze). Das Modell kann andere Längen zwischen 2 und 30 Buchstaben anfordern. Eine kleine Kategorie liefert unter Umständen weniger Namen als angefordert, weil der Kette die verschiedenen Namen ausgehen. Das Modell darf einen der Namen wählen, ihn abändern oder zwei davon kombinieren; siehe den Hinweis der Suite *Name generator* im [Werkzeugfenster](tools.md#choosing-the-tools).

<a id="commands"></a>

## Befehle

Dieselben Namen sind als [Befehle](../commands/index.md) verfügbar, für die Kommandozeile und für [Agenten](../agents.md), die sie in ihren eigenen Prompts verwenden können (zum Beispiel für `/generate-lore-entry` des [Lore-Generators](lore-generator.md)):

| Befehl | Was er tut |
|---|---|
| `/list-name-categories [<filter>]` | Listet die Kategorien als JSON-Array auf; mit einem Filter nur diejenigen, deren Name alle seine Wörter enthält (jeweils am Wortanfang, in beliebiger Reihenfolge, ohne Beachtung der Groß- und Kleinschreibung: `chad male` findet *Africa - Central Africa - Chad - Male*, aber nicht *…Female*). |
| `/generate-names category count [min_length [max_length]]` | Erzeugt bis zu 100 Namen der Kategorie und gibt sie als JSON-Array zurück. Jedes Mal andere Namen. |

Beide lesen nur.

<a id="categories"></a>

## Kategorien

Es gibt etwa 1270 Kategorien. Ihre Namen sagen, was sie sind:

- **Vor- und Nachnamen der Welt**, aus dem Onomastikon von Kate Monk: `Europe Western - France - Surnames`,
  `Ancient World - Egypt - Male`, `India - Hindu Gods - Goddesses`. Mehrere Hundert davon.
- **Nachnamen aus 75 Ländern**, die latinisierten Nachnamen aus dem [Datensatz der beliebtesten Namen nach Ländern](https://github.com/sigpwned/popular-names-by-country-dataset)
  (CC0): `Armenia - Surnames`, `Japan - Surnames`, `Kosovo - Surnames`. Es sind kleine Listen (6 bis etwa 150 Namen), daher bleiben die
  Ketten kurz und die neuen Namen liegen nahe an den echten; für die echten selbst nutze den [Namensanbieter](name-provider.md).
- **Listen aus anderen Sammlungen**: Götter, Engel, Dämonen, antike Städte und Staaten, Kaiser, Planeten (fantasygen),
  Corpora (Tiere, Tarot, Tolkien-Namen, Game-of-Thrones-Häuser, Drogen, Krankenhäuser, Stoffe, Edelsteine, Programmiersprachen ...) und markov-namegen-lib (Pokémon, Drachen, Cocktails, Banken, römische Ortsnamen ...).

<a id="groups"></a>

### Gruppen

Etwa 170 Einträge sind **Gruppen** von Kategorien. Sie fassen die vielen kleinen Listen einer Sprache zusammen, sodass du `German - Male` anforderst, statt zwischen *Germany - Germanic - Male*, *Germany - Biblical - Male*, *Austria - First names - Male* und den übrigen zu wählen. Sie werden wie jede Kategorie aufgelistet, und eine Gruppe ist ein Eintrag im [Werkzeugfenster](tools.md). Die Gruppen:

- **Nach Sprache**, jeweils als `- Male`, `- Female` und `- Surnames`, sofern es solche Listen gibt: Deutsch, Französisch, Spanisch (die Nachnamen schließen Lateinamerika ein), Italienisch, Portugiesisch, Niederländisch, Skandinavisch, Finnisch, Baltisch, Irisch, Schottisch, Walisisch, Keltisch (alle zusammen), West-, Ost- und Südslawisch, Griechisch, Ungarisch, Rumänisch, Albanisch, Kaukasisch, Turkisch, Persisch, Arabisch, Jüdisch, Hindi (die vier alphabetischen Listen männlicher Namen als eine), Südasiatisch, Südostasiatisch, Malaiisch-Indonesisch, Polynesisch und die afrikanischen Regionen (West, Ost, Süd, Nord, Zentral). `Chinese`, `Japanese`, `Korean` und `Vietnamese` haben `- Surnames`.
- **Beliebiges Geschlecht**: `<Language> - Forenames` (`German - Forenames`, `Spanish - Forenames`, `Celtic - Forenames` ...) nimmt die männlichen und weiblichen Listen der Sprache sowie ihre Listen von Namen für beide Geschlechter, sodass ein Aufruf Männer- und Frauennamen gemischt zurückgibt (die Anteile folgen den Größen der Listen, meist zwischen der Hälfte und drei Vierteln Männer). Chinesisch, Japanisch, Koreanisch und Vietnamesisch haben sie ebenfalls. Bei Sprachen, bei denen die Mischung einseitig ausfiele (Turkisch, Südasiatisch, West- und Südafrikanisch, Malaiisch-Indonesisch), gibt es nur die männliche und die weibliche Gruppe.
- **Aus der Vergangenheit**: Angelsächsisch, Mittelalterlich-Germanisch, Mittelenglisch, Alter Vorderer Orient.
- **Indianisch**: `Native American - Male`, `- Female` und `- Surnames` (die nordamerikanischen Kategorien des Onomastikon: Apache, Sioux, Irokesen, Cherokee, Inuit ...) und dasselbe für Mittel- und Südamerika (Azteken, Maya, Inka, Amazonien).
- **Sonstiges**: `English - Surnames` (Patronymika, Berufe, Orte ...), `Saints`, `Deities`, `Fantasy Creatures`, `Game of Thrones Houses`.

Für jeden Namen wählt die Erweiterung zuerst zufällig eine der Kategorien der Gruppe und bildet den Namen dann mit der Kette dieser Kategorie (mit ihren üblichen Längen). Ein Name klingt dann nach einem Volk und einer Sprache statt nach einer Mischung aller, und ein Aufruf liefert Namen mehrerer davon. Die Listen der Länder, die nach Herkunft oder nach Buchstaben aufgeteilt sind, werden nach ihrer Größe gewählt (eine Liste mit 600 Namen wird öfter gewählt als eine mit 10); bei den Gruppen ganzer Sprachen und Länder (Turkisch, Kaukasisch, Baltisch, Polynesisch, die afrikanischen Regionen, Indianisch ...) hat jedes Mitglied die gleiche Chance. Die Quellen mancher Gruppen sind kurz und teilweise englische Übersetzungen (*Calf-Woman*), daher sind die Namen grob.
Eine Gruppe ist eine Zeile in `name-groups.json` (die Mitglieder und wie sie gewählt werden), siehe die [README des Plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md#groups).

Jede Kategorie hat eine Kette in der Länge, die ihr am besten passte (kurze Listen erhalten kurze Ketten, die die Originale seltener wiederholen), und die Längen, die ihre Namen üblicherweise haben. Die Listen selbst sind nicht Bestandteil von Marginalia: nur die Wahrscheinlichkeiten sind es, sodass die Originalnamen nicht aus der Erweiterung ausgelesen werden können. Manche Quellen haben eigene Lizenzen, siehe die [README des Plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/name-generator/README.md).

<a id="usage"></a>

## Verwendung

Lade die Erweiterung unter *Admin → Extensions*. Sie gilt für die nächste Generierung; Erweiterungen, die sie nutzen, finden sie beim Laden, auch wenn sie nachträglich installiert oder entladen und erneut installiert wurde – ein Neustart des Lore-Generators ist nicht nötig. Ohne sie und die anderen Suiten hat der Lore-Generator keine Schaltfläche *Tools*.
