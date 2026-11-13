---
label: Reguläre Ausdrücke
order: 935
icon: search
---

# Reguläre Ausdrücke

Ein regulärer Ausdruck (*Regex*) ist ein kurzes Muster, das Text beschreibt, statt ihn Wort für Wort auszuschreiben: „das Wort Harry, aber
nicht Harrys“, „zwei beliebige Wörter in Anführungszeichen“, „drei oder mehr Leerzeichen hintereinander“. Marginalia verwendet sie an zwei Stellen:

- **Suchen und Ersetzen** der [Geschichte](books/story-editor.md#search-and-replace) und eines
  [Lorebuchs](lorebooks/entries.md#search-and-replace) sowie die entsprechenden [Befehle](commands/index.md), wenn **Per Regex suchen** aktiviert ist. Das Muster findet den Text, die Ersetzung kann Teile davon wiederverwenden.
- Der Filter **Regex** eines [Lorebuch-Eintrags](lorebooks/entries.md#activation), der bestimmt, wann der Eintrag in den Prompt aufgenommen wird.

Die Syntax entspricht der von Java. Diese Seite zeigt, was Sie am häufigsten brauchen; die vollständige Referenz ist am Ende verlinkt.

!!!
Für einfache Wörter brauchen Sie keine Regexes. Bei ausgeschaltetem **Per Regex suchen** wird der Text genau so gesucht, wie Sie ihn eingeben. Ein `.` oder eine `(` darin ist dann einfach ein Punkt bzw. eine Klammer. Schalten Sie den Regex nur ein, wenn ein einzelner fester Text nicht ausreicht.
!!!

<a id="the-building-blocks"></a>

## Die Bausteine

| Schreiben Sie | Trifft auf | Beispiel |
|---|---|---|
| einen Buchstaben oder eine Ziffer | genau dieses Zeichen | `cat` findet *cat* in *concatenate* |
| `.` | ein beliebiges Zeichen (kein Zeilenumbruch) | `c.t` findet *cat*, *cot*, *c7t* |
| `\d` `\w` `\s` | eine Ziffer, ein Buchstabe/eine Ziffer/`_`, ein Leerzeichen oder Zeilenumbruch | `\d\d` findet *42* |
| `[abc]` `[a-z]` | eines der aufgelisteten Zeichen oder ein Bereich | `gr[ae]y` findet *grey* und *gray* |
| `[^abc]` | ein Zeichen, das *nicht* aufgelistet ist | `[^"]` ist alles außer einem Anführungszeichen |
| `\b` | die Grenze eines Wortes | `\bcat\b` findet *cat*, aber nicht *concatenate* |
| `^` `$` | der Anfang und das Ende einer Zeile | `^Chapter` findet *Chapter* am Zeilenanfang |
| `x?` `x*` `x+` | `x` null- oder einmal, beliebig oft, mindestens einmal | `colou?r` findet *color* und *colour* |
| `x{2,4}` | `x` zwei- bis viermal | `\.{3}` sind drei Punkte hintereinander |
| `\` | das nächste Zeichen wird wörtlich genommen | `\.` ist ein Punkt, `\?` ein Fragezeichen, `\(` eine Klammer |

Klammern bilden eine **Gruppe**, und ein senkrechter Strich innerhalb einer Gruppe bedeutet „oder“: `(Mr|Mrs) Dursley` findet *Mr Dursley* und
*Mrs Dursley*, und `\b(Voldemort|You-Know-Who)\b` findet einen der beiden Namen. Ein `?`, `*` oder `+` nach einer Gruppe bezieht sich auf die ganze Gruppe: `(very )+good` findet *very good* und *very very good*.

Die Zeichen `. ? * + ^ $ | \ ( ) [ ] { }` haben eine eigene Bedeutung. Um eines davon als solches zu suchen, stellen Sie ein `\` davor: Um *Really?!* zu finden, schreiben Sie `Really\?!`.

Standardmäßig berücksichtigt ein Regex die Groß- und Kleinschreibung: *harry* findet nicht *Harry*. Setzen Sie `(?i)` an den Anfang, um die Schreibung zu ignorieren: `(?i)harry` findet *Harry*, *HARRY* und *harry*. (Die Fenster Suchen und Ersetzen sperren **Groß-/Kleinschreibung beachten**, solange der Regex aktiv ist. Hier schalten Sie es also auf diesem Weg aus; der Filter **Regex** im Lorebuch ignoriert die Schreibung immer.)

Ein Treffer ohne jede Länge (ein Muster wie `a*`, das auch nichts treffen kann) wird bei Suchen und Ersetzen ignoriert.

<a id="typical-uses-in-a-story"></a>

## Typische Anwendungen in einer Geschichte

Jede Zeile beschreibt etwas, das Sie an einem Buch tun möchten. **Suchen nach** ist das, was Sie in das Feld *Suchen nach* eintragen, **Ersetzen durch** das, was Sie in *Ersetzen durch* eintragen.

| Was Sie wollen | Suchen nach | Ersetzen durch |
|---|---|---|
| Eine Figur umbenennen, aber keinen Teil eines längeren Wortes (*Harry*, nicht *Harrys* oder *Harrison*) | `\bHarry\b` | `Henry` |
| Umbenennen, unabhängig von der Schreibung | `(?i)\bharry\b` | `Henry` |
| Einen Namen und sein Possessiv in einem Schritt umbenennen | `\bHarry('s)?\b` | `Henry$1` |
| Gerade Anführungszeichen in typografische umwandeln | `"([^"]*)"` | `„$1“` |
| Drei Punkte in ein Auslassungszeichen umwandeln | `\.{3}` | `…` |
| Zwei oder mehr Leerzeichen in eines | ` {2,}` | ` ` (ein Leerzeichen) |
| Leerzeichen am Zeilenende entfernen | ` +$` | (leer) |
| Einen langen Gedankenstrich aus zwei Bindestrichen | ` ?-- ?` | ` — ` |
| *Nachname, Vorname* in *Vorname Nachname* tauschen | `(\w+), (\w+)` | `$2 $1` |
| Finden, wo ein Wort verwendet wird, ohne etwas zu ändern | das Muster | (leer lassen und nur **Suchen** drücken) |

Um mehrere Schreibweisen zu einem Namen zusammenzufassen, suchen Sie nach `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` und ersetzen Sie es durch
`Tom Riddle`.

<a id="taking-the-match-into-the-replacement"></a>

### Den Treffer in die Ersetzung übernehmen

Alles, was ein Regex in runde Klammern setzt, ist eine **Gruppe**; sie wird von links nach ihrer öffnenden Klammer nummeriert: `$1` ist der Text der ersten Gruppe, `$2` der zweiten, `$0` der gesamte Treffer. Bei `(\w+), (\w+)` ist die erste Gruppe das Wort vor dem Komma, die zweite das Wort danach; `$2 $1` tauscht sie also: *Potter, Harry* wird zu *Harry Potter*.

Sie können eine Gruppe benennen, `(?<last>\w+), (?<first>\w+)`, und den Namen in der Ersetzung verwenden: `${first} ${last}`.

Ein `$` oder ein `\`, das in der Ersetzung als solches erhalten bleiben soll, muss maskiert werden: Um *$5* in den Text zu schreiben, schreiben Sie `\$5`. Ohne Regex wird die Ersetzung immer genau so übernommen, wie Sie sie eingeben.

Ist das Muster fehlerhaft (eine Klammer, die nie geschlossen wird) oder nennt die Ersetzung eine Gruppe, die das Muster nicht hat, weist eine Meldung darauf hin und nichts geschieht; eine Suche verändert niemals etwas.

<a id="match-only-in-a-context"></a>

### Nur in einem Kontext treffen

Ein *Lookahead* `(?=...)` und ein *Lookbehind* `(?<=...)` betrachten den Text um den Treffer, ohne ihn zu übernehmen. So wird nur der Teil außerhalb davon ersetzt:

| Suchen nach | Findet |
|---|---|
| `Harry(?= Potter)` | *Harry* nur, wenn *Potter* folgt (und lässt *Potter* unberührt) |
| `(?<=Professor )McGonagall` | *McGonagall* nur nach *Professor* |
| `Harry(?! Potter)` | *Harry*, auf das nicht *Potter* folgt |

<a id="lazy-and-greedy"></a>

### Faul und gierig

`*` und `+` nehmen so viel, wie sie können: Bei `He said "yes" and "no"` trifft das Muster `".*"` das gesamte `"yes" and "no"`.
Fügen Sie ein `?` hinzu (`".*?"`) oder beschreiben Sie, was nicht erlaubt ist (`"[^"]*"`), damit am ersten schließenden Anführungszeichen Schluss ist.

Die Suche betrachtet jede Zeile für sich: `.` überschreitet keinen Zeilenumbruch, und `^` und `$` treffen auf den Anfang und das Ende jeder Zeile eines Abschnitts zu, nicht nur des gesamten Textes. Um einen Zeilenumbruch zu treffen, schreiben Sie `\n`.

<a id="in-a-lorebook-entry-filter"></a>

## Im Filter eines Lorebuch-Eintrags

Der Filter eines Lorebuch-Eintrags, der auf **Regex** gesetzt ist, wird gegen die Anfrage für den Abschnitt geprüft, und zwar überall darin. Er ignoriert die Groß- und Kleinschreibung, und `.` trifft dort auch Zeilenumbrüche, sodass `varro.*harbor` Varro und den Hafen selbst dann findet, wenn sie in verschiedenen Zeilen stehen. Typische Filter:

- `\bvarro\b` - Varro als ganzes Wort.
- `\b(varro|captain)\b` - Varro oder der Captain.
- `varro.*(harbor|docks)` - Varro und danach, später, der Hafen oder die Docks.
- `dragons?` - *dragon* oder *dragons*.
- `(?<!no )magic` - *magic*, sofern es nicht direkt nach *no* steht.

Ein ungültiger Regex aktiviert den Eintrag nie, und der Grund wird im Protokoll festgehalten. Weitere Informationen darüber, wie ein Eintrag ausgewählt wird, finden Sie unter
[Einträge und Aktivierung](lorebooks/entries.md#activation).

<a id="in-the-commands"></a>

## In den Befehlen

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) und `/search-replace-lore-entries` nehmen den Regex als drittes Argument (`true`) entgegen, nach Suche und Ersetzung. Die Befehlszeile trennt an Leerzeichen, daher wird ein Muster oder eine Ersetzung mit Leerzeichen in doppelte Anführungszeichen gesetzt. Ein Backslash vor einem Buchstaben wird unverändert weitergegeben (`\b`, `\d`), aber `\\` wird zu einem einzelnen Backslash und `\"` zu einem Anführungszeichen. Ein wörtlicher Backslash im Muster ist daher `\\\\`.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

Die Befehle zeigen keine Trefferliste an: Sie fragen nur, ob die gefundene Anzahl an Treffern ersetzt werden soll. Wenn Sie bei einem Muster unsicher sind, probieren Sie es zuerst im Fenster **Suchen und Ersetzen** aus, das jeden Treffer so zeigt, wie er ist und wie er nach der Ersetzung aussehen wird.

<a id="tips"></a>

## Tipps

- Probieren Sie ein Muster zuerst mit **Suchen** aus und öffnen Sie einige der Zeilen: Das Fenster zeigt jeden Treffer mit seiner Umgebung, so wie er ist und wie er werden wird. Ersetzen Sie nur, wenn das Ergebnis Ihren Vorstellungen entspricht.
- Beginnen Sie einfach und erweitern Sie das Muster Schritt für Schritt; ein Muster, das zu viel findet, braucht meist `\b` um ein Wort herum oder ein `?`, um ein `*` zu bremsen.
- Ein [Backup](books/backups.md) vor einem großen Ersetzen ist günstig (`/take-backup`), und ein Ersetzen lässt sich nicht stoppen, sobald es begonnen hat.
- Geänderte Zusammenfassungen: Betrifft das Ersetzen Text, aus dem eine [Zusammenfassung](books/summaries.md) erstellt wurde, warnt das Fenster, dass die Zusammenfassung veraltet würde; siehe [Suchen und Ersetzen](books/story-editor.md#search-and-replace).

<a id="further-reading"></a>

## Weiterführende Literatur

Diese Seite behandelt die Grundlagen. Die vollständige Syntax von Java mit allen Konstrukten, Flags und Zeichenklassen finden Sie in der
[Dokumentation von `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html),
und die Syntax der Ersetzung (`$1`, `${name}`, `\$`) in
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
