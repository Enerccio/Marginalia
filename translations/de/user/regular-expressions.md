---
label: Reguläre Ausdrücke
order: 935
icon: search
---

# Reguläre Ausdrücke

Ein regulärer Ausdruck (*Regex*) ist ein kurzes Muster, das Text beschreibt, statt ihn wörtlich auszuschreiben: „das Wort Harry, aber
nicht Harrys“, „zwei beliebige Wörter in Anführungszeichen“, „drei oder mehr Leerzeichen hintereinander“. Marginalia verwendet sie an zwei Stellen:

- **Suchen und Ersetzen** der [Geschichte](books/story-editor.md#search-and-replace) und eines
  [Lorebooks](lorebooks/entries.md#search-and-replace) sowie die entsprechenden [Befehle](commands/index.md), wenn **Per Regex suchen** aktiviert ist. Das Muster findet den Text, die Ersetzung kann Teile davon wiederverwenden.
- Der Filter **Regex** eines [Lorebook-Eintrags](lorebooks/entries.md#activation), der entscheidet, wann der Eintrag in den
  Prompt aufgenommen wird.

Der verwendete Dialekt ist der von Java. Diese Seite zeigt, was du am häufigsten brauchst; die vollständige Referenz ist am Ende verlinkt.

!!!
Für einfache Wörter brauchst du keine Regex. Bei ausgeschaltetem **Per Regex suchen** wird der Text genau so gesucht, wie du ihn eingibst. Ein
`.` oder eine `(` darin ist dann nur ein Punkt bzw. eine Klammer. Aktiviere die Regex nur, wenn ein einzelner fester Text nicht ausreicht.
!!!

<a id="the-building-blocks"></a>

## Die Bausteine

| Schreibe | Trifft auf | Beispiel |
|---|---|---|
| einen Buchstaben oder eine Ziffer | genau dieses Zeichen | `cat` findet *Katze* in *Katzenjammer* |
| `.` | ein beliebiges Zeichen (außer Zeilenumbruch) | `c.t` findet *Katze*, *Kante*, *K4tze* |
| `\d` `\w` `\s` | eine Ziffer, einen Buchstaben/eine Ziffer/`_`, ein Leerzeichen oder einen Zeilenumbruch | `\d\d` findet *42* |
| `[abc]` `[a-z]` | eines der aufgelisteten Zeichen oder einen Bereich | `gr[ae]y` findet *grau* und *grey* |
| `[^abc]` | ein Zeichen, das *nicht* aufgelistet ist | `[^"]` ist alles außer einem Anführungszeichen |
| `\b` | die Grenze eines Wortes | `\bcat\b` findet *Katze*, aber nicht *Katzenjammer* |
| `^` `$` | Anfang und Ende einer Zeile | `^Chapter` findet *Kapitel* am Zeilenanfang |
| `x?` `x*` `x+` | `x` null- oder einmal, beliebig oft, mindestens einmal | `colou?r` findet *Farbe* und *Farbe* mit anderer Schreibweise |
| `x{2,4}` | `x` zwei- bis viermal | `\.{3}` sind drei Punkte hintereinander |
| `\` | das nächste Zeichen wird wörtlich genommen | `\.` ist ein Punkt, `\?` ein Fragezeichen, `\(` eine Klammer |

Klammern bilden eine **Gruppe**, und ein senkrechter Strich innerhalb einer Gruppe bedeutet „oder“: `(Mr|Mrs) Dursley` findet *Mr Dursley* und
*Mrs Dursley*, und `\b(Voldemort|You-Know-Who)\b` findet einen der beiden Namen. Ein `?`, `*` oder `+` nach einer Gruppe gilt für die ganze Gruppe:
`(very )+good` findet *sehr gut* und *sehr sehr gut*.

Die Zeichen `. ? * + ^ $ | \ ( ) [ ] { }` haben eine eigene Bedeutung. Um eines von ihnen als solches zu suchen, setze ein
`\` davor: Um *Wirklich?!* zu finden, schreibe `Really\?!`.

Standardmäßig ignoriert eine Regex nichts: *harry* findet nicht *Harry*. Setze `(?i)` an den Anfang, um die Groß- und Kleinschreibung zu ignorieren:
`(?i)harry` findet *Harry*, *HARRY* und *harry*. (In den Fenstern für Suchen und Ersetzen ist **Groß-/Kleinschreibung beachten** bei aktiver Regex gesperrt; so schaltest du sie dort aus. Der Filter **Regex** im Lorebook ignoriert die Groß- und Kleinschreibung immer.)

Ein Treffer ohne jede Länge (ein Muster wie `a*`, das nichts treffen kann) wird von Suchen und Ersetzen ignoriert.

<a id="typical-uses-in-a-story"></a>

## Typische Anwendungen in einer Geschichte

Jede Zeile ist etwas, was du mit einem Buch machen möchtest. **Suchen nach** ist das, was du in *Suchen nach* eingibst, **Ersetzen** das, was du in *Ersetzen durch* eingibst.

| Was du willst | Suchen nach | Ersetzen durch |
|---|---|---|
| Eine Figur umbenennen, aber keinen Teil eines längeren Wortes (*Harry*, aber nicht *Harrys* oder *Harrison*) | `\bHarry\b` | `Henry` |
| Umbenennen, unabhängig von der Schreibweise | `(?i)\bharry\b` | `Henry` |
| Einen Namen und seinen Genitiv in einem Schritt umbenennen | `\bHarry('s)?\b` | `Henry$1` |
| Gerade Anführungszeichen in typografische umwandeln | `"([^"]*)"` | `“$1”` |
| Drei Punkte in ein Auslassungszeichen umwandeln | `\.{3}` | `…` |
| Zwei oder mehr Leerzeichen in eines | ` {2,}` | ` ` (ein Leerzeichen) |
| Leerzeichen am Zeilenende entfernen | ` +$` | (leer) |
| Einen langen Strich aus zwei Bindestrichen | ` ?-- ?` | ` — ` |
| *Nachname, Vorname* zu *Vorname Nachname* tauschen | `(\w+), (\w+)` | `$2 $1` |
| Herausfinden, wo ein Wort verwendet wird, ohne etwas zu ändern | das Muster | (leer lassen und nur **Suchen** drücken) |

Um mehrere Schreibweisen zu einem Namen zusammenzufassen, suche nach `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` und ersetze es durch
`Tom Riddle`.

<a id="taking-the-match-into-the-replacement"></a>

### Den Treffer in die Ersetzung übernehmen

Alles, was eine Regex in runde Klammern setzt, ist eine **Gruppe**, nummeriert von links nach ihrer öffnenden Klammer: Die Ersetzung
`$1` ist der Text der ersten Gruppe, `$2` der der zweiten, `$0` der ganze Treffer. In `(\w+), (\w+)` ist die erste Gruppe das
Wort vor dem Komma, die zweite das Wort danach, sodass `$2 $1` sie vertauscht: *Potter, Harry* wird zu *Harry Potter*.

Du kannst eine Gruppe benennen, `(?<last>\w+), (?<first>\w+)`, und den Namen in der Ersetzung verwenden: `${first} ${last}`.

Ein `$` oder ein `\`, das in der Ersetzung als solches stehen bleiben soll, muss maskiert werden: Um *$5* in den Text zu bringen, schreibe `\$5`.
Ohne Regex wird die Ersetzung immer genau so übernommen, wie du sie eingibst.

Ist das Muster fehlerhaft (eine Klammer, die nie geschlossen wird) oder verweist die Ersetzung auf eine Gruppe, die das Muster nicht hat, zeigt eine Benachrichtigung das an und nichts geschieht; eine Suche ändert nie etwas.

<a id="match-only-in-a-context"></a>

### Nur in einem Kontext treffen

Ein *Lookahead* `(?=...)` und ein *Lookbehind* `(?<=...)` betrachten den Text um den Treffer, ohne ihn zu übernehmen, sodass nur der
Teil außerhalb von ihnen ersetzt wird:

| Suchen nach | Findet |
|---|---|
| `Harry(?= Potter)` | *Harry* nur, wenn *Potter* folgt (und lässt *Potter* unberührt) |
| `(?<=Professor )McGonagall` | *McGonagall* nur nach *Professor* |
| `Harry(?! Potter)` | *Harry*, auf das nicht *Potter* folgt |

<a id="lazy-and-greedy"></a>

### Faul und gierig

`*` und `+` nehmen so viel wie möglich: In `He said "yes" and "no"` trifft das Muster `".*"` den ganzen Ausdruck `"yes" and "no"`.
Füge ein `?` hinzu (`".*?"`) oder beschreibe, was nicht erlaubt ist (`"[^"]*"`), damit beim ersten schließenden Anführungszeichen gestoppt wird.

Die Suche behandelt jede Zeile für sich: `.` überschreitet keinen Zeilenumbruch, und `^` und `$` treffen am Anfang und am Ende
jeder Zeile eines Abschnitts, nicht nur des ganzen Textes. Um einen Zeilenumbruch zu treffen, schreibe `\n`.

<a id="in-a-lorebook-entry-filter"></a>

## Im Filter eines Lorebook-Eintrags

Der Filter eines Lorebook-Eintrags, der auf **Regex** gesetzt ist, wird gegen die Anfrage für den Abschnitt geprüft, und zwar überall darin. Er ignoriert
die Groß- und Kleinschreibung, und `.` trifft dort tatsächlich Zeilenumbrüche, sodass `varro.*harbor` Varro und den Hafen selbst dann findet, wenn sie in verschiedenen Zeilen stehen.
Typische Filter:

- `\bvarro\b` - Varro als ganzes Wort.
- `\b(varro|captain)\b` - Varro oder der Kapitän.
- `varro.*(harbor|docks)` - Varro und danach, später, der Hafen oder die Docks.
- `dragons?` - *Drache* oder *Drachen*.
- `(?<!no )magic` - *Magie*, außer wenn sie direkt auf *keine* folgt.

Eine ungültige Regex aktiviert den Eintrag nie, und der Grund wird im Protokoll festgehalten. Siehe
[Einträge und Aktivierung](lorebooks/entries.md#activation) für den Rest der Auswahl eines Eintrags.

<a id="in-the-commands"></a>

## In den Befehlen

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) und `/search-replace-lore-entries` nehmen
die Regex als drittes Argument (`true`) entgegen, nach Suche und Ersetzung. Die Befehlszeile trennt an Leerzeichen, daher wird ein Muster oder eine Ersetzung mit Leerzeichen in doppelte Anführungszeichen gesetzt. Ein Backslash vor einem Buchstaben wird unverändert übergeben
(`\b`, `\d`), aber `\\` wird zu einem einzelnen Backslash und `\"` zu einem Anführungszeichen, sodass ein wörtlicher Backslash im Muster `\\\\` lautet.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

Die Befehle zeigen keine Trefferliste: Sie fragen nach, ob die gefundenen Treffer ersetzt werden sollen. Wenn du bei einem Muster unsicher bist, probiere es zuerst im Fenster **Suchen und Ersetzen** aus, das jeden Treffer so zeigt, wie er ist und wie er danach aussehen wird.

<a id="tips"></a>

## Tipps

- Probiere ein Muster zuerst mit **Suchen** aus und öffne ein paar der Zeilen: Das Fenster zeigt jeden Treffer mit seiner Umgebung, so wie er ist und wie er wird. Ersetze erst, wenn das Ergebnis dem entspricht, was du wolltest.
- Fang einfach an und erweitere das Muster Schritt für Schritt; ein Muster, das zu viel findet, braucht meist `\b` um ein Wort, oder `?`, um ein `*` zu stoppen.
- Ein [Backup](books/backups.md) vor einem großen Ersetzen ist günstig (`/take-backup`), und ein Ersetzen kann nicht gestoppt werden, sobald es begonnen hat.
- Geänderte Zusammenfassungen: Berührt das Ersetzen Text, aus dem eine [Zusammenfassung](books/summaries.md) erstellt wurde, warnt das Fenster, dass die Zusammenfassung veraltet würde; siehe [Suchen und Ersetzen](books/story-editor.md#search-and-replace).

<a id="further-reading"></a>

## Weiterführende Literatur

Diese Seite behandelt die Grundlagen. Die vollständige Syntax von Java mit allen Konstrukten, Flags und Zeichenklassen steht in der
[Dokumentation von `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html),
und die Syntax der Ersetzung (`$1`, `${name}`, `\$`) in
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
