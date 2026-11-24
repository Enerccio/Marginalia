---
label: Befehle
order: 945
icon: terminal
---

# Befehle

Befehle sind kleine Aktionen, die du durch Eintippen ausführst, statt dich durch Menüs zu klicken: etwas in der Geschichte
oder in den Lorebooks nachschlagen, suchen und ersetzen, ein Backup anlegen. [Erweiterungen](../extensions/index.md) fügen eigene Befehle hinzu,
zum Beispiel um einen Lore-Eintrag zu erzeugen oder Namen zu erfinden. Mit denselben Befehlen arbeitet auch ein [Agent](../agents.md),
daher tut ein Befehl genau eine klare Sache und sagt, was er tut: Das kannst du beim Tippen lesen.

Die **Befehlszeile** ist das Eingabefeld neben dem Menü unter der Geschichte, im [Story-Editor](../books/story-editor.md).

<a id="running-a-command"></a>

## Einen Befehl ausführen

1. Klicke in die Befehlszeile und tippe `/`. Die Liste zeigt alle Befehle, jeweils mit Namen und einer kurzen
   Beschreibung. Wenn du weitertippst, wird die Liste eingeengt: Der Filter sieht sich den Namen des Befehls, seinen Titel
   und seine Beschreibung an.
2. Wähle den Befehl mit der Maus oder mit den Pfeiltasten und **Enter**. Ein Befehl, der nichts braucht, läuft sofort;
   bei einem Befehl mit Argumenten wird `/name ` in das Feld geschrieben, und du machst mit den Argumenten weiter.
![Die Befehlsliste, geöffnet durch Eingabe eines Schrägstrichs](../../images/command-list.png)

3. Tippe die Argumente ein und drücke **Enter** oder klicke auf die Abspielschaltfläche neben dem Feld, um den Befehl
   auszuführen. Das Feld wird geleert, sobald der Befehl startet. Ein Text, der kein Befehl ist, bleibt stehen, damit du
   ihn korrigieren kannst.

Text, der nicht mit `/` beginnt, ist kein Befehl, und es wird nichts ausgeführt. Wenn du das Feld verlässt oder **Esc**
drückst, wird nie etwas ausgeführt, du kannst also nicht versehentlich einen Befehl starten.

<a id="the-help"></a>

## Die Hilfe

Während du tippst, öffnet sich über dem Feld ein kleines Fenster. Es verdeckt einen Teil der Geschichte, verschiebt aber
nichts, und es schließt sich, wenn du das Feld verlässt oder den Befehl ausführst. Es zeigt:

- was der Befehl tut,
- die Formen des Befehls (siehe [Formen](#forms-of-a-command)), die zu dem passen, was du bisher getippt hast, mit dem Argument, das du
  gerade tippst, hervorgehoben und mit seiner Beschreibung,
- was der Befehl zurückgibt,
- einen Hinweis, wenn du mehr Argumente eingegeben hast, als der Befehl annimmt, oder wenn kein Befehl diesen Namen hat.

![Die Hilfe von /search-replace-lore-entries, während das zweite Argument getippt wird](../../images/command-help.png)

Solange du noch den Namen tippst, ist die Befehlsliste die Hilfe.

<a id="arguments"></a>

## Argumente

Argumente werden durch Leerzeichen getrennt, und mehrere Leerzeichen hintereinander zählen als eines. Um ein Leerzeichen
*innerhalb* eines Arguments zu schreiben, gibt es drei Möglichkeiten:

| Du tippst                    | Der Befehl erhält               | Warum                                      |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | ein Argument, `two words`       | `\ ` (Backslash, Leerzeichen) ist ein Leerzeichen |
| `/search-parts "two words"`  | ein Argument, `two words`       | doppelte Anführungszeichen halten alles zusammen |
| `/search-parts say\ "hi"`    | ein Argument, `say hi`          | Anführungszeichen können mitten in einem Argument beginnen |

Die anderen Sonderzeichen:

| Du tippst                     | Du erhältst                |
|-------------------------------|----------------------------|
| `\\`                          | einen Backslash            |
| `\"`                          | ein doppeltes Anführungszeichen, mit oder ohne Anführungszeichen darum: `"say \"hi\""` ergibt `say "hi"` |
| `""`                          | ein leeres Argument        |

Ein Backslash vor allem anderen bleibt erhalten, `C:\books\story` wird also unverändert übergeben. Wenn du ein Anführungszeichen
öffnest und nicht wieder schließt, gehört alles bis zum Ende der Zeile zu diesem Argument.

Argumente sind Text. Ein Befehl, der eine Zahl, ein Ja/Nein oder eine von wenigen Optionen verlangt, wandelt den Text selbst
um; passt der Text nicht, wird der Befehl nicht ausgeführt, und eine Benachrichtigung sagt es dir.

<a id="forms-of-a-command"></a>

## Formen eines Befehls

Ein Befehl kann verschiedene Argumente annehmen: `/list-parts` ohne Argument und `/list-parts 2` mit einem Argument sind zwei **Formen**
desselben Befehls. Marginalia wählt die Form nach der Anzahl der Argumente, die du eingegeben hast, und danach, wie diese
aussehen. Passt keine Form, zeigt eine Benachrichtigung, dass der Befehl diese Argumente nicht annimmt, und die Hilfe zeigt
die Formen, die es gibt.

<a id="the-result"></a>

## Das Ergebnis

Wenn ein Befehl etwas zurückgibt, wird es in einem Fenster über der Befehlszeile angezeigt. Du schließt es mit dem Kreuz,
mit **Esc** oder fährst einfach fort: Es verschwindet, sobald du etwas Neues tippst oder einen anderen Befehl ausführst. Ein
Befehl, der nichts zurückgibt, zeigt kein Fenster.

![Das Ergebnis von /take-backup über der Befehlszeile](../../images/command-result.png)

Wenn ein Befehl fehlschlägt oder es den Befehl nicht gibt, erhältst du eine Benachrichtigung mit dem Grund.

<a id="commands-that-take-time"></a>

## Befehle, die Zeit brauchen

Die meisten Befehle antworten sofort. Ein Befehl, der Zeit braucht, zeigt ein Fenster mit einem animierten Balken und einer
Schaltfläche **Stop**. Das Fenster steht seitlich, und die Seite bleibt bedienbar; mit seinem ⌄ klappst du es in die
[Laufübersicht](../books/story-editor.md#the-panel-of-the-runs) ein, wo der Befehl zusammen mit dem Text, den das Modell schreibt, aufgelistet ist, auf allen deinen
Seiten. Unter dem Balken kann der Befehl sagen, was er gerade tut, und in kleinerer Schrift das Detail des Schritts. **Stop**
bricht den Befehl ab; das sollte sofort zurückkehren. Befehle, bei denen das Modell eine Zusammenfassung oder Notizen
schreibt, etwa `/generate-annotation`, gehören zu dieser Art.

`/next`, `/swipe` und `/regenerate`, in die Befehlsleiste der Geschichte getippt, haben kein solches Fenster: Der neue Teil wird in die
Geschichte gestreamt, wie wenn du die runde Schaltfläche drückst, die runde Schaltfläche stoppt es, und die Geschichte ist
nur so gesperrt wie bei jeder Generierung (du kannst weiter am Lore, an den anderen Teilen und den Seitenreitern arbeiten).
Das Ergebnis des Befehls erscheint über der Leiste, wenn der Teil fertig ist. Bis dahin nimmt die Leiste keinen zweiten
dieser Befehle an (dein Text bleibt erhalten, du kannst ihn später ausführen); andere Befehle funktionieren.

![Das Fenster eines laufenden Befehls](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Befehle, die Daten ändern

Jeder Befehl sagt, was er mit den bereits vorhandenen Daten macht, und die Befehlsleiste zeigt das als kleines Etikett neben
dem Befehl, in der Liste und in der Hilfe:

- **Destructive** (rot) – der Befehl ändert oder entfernt vorhandene Daten, wie die Such- und Ersetzbefehle, die das Buch
  umschreiben. Zeige mit dem Mauszeiger auf das Etikett, um die Erklärung zu sehen.
- **Non-destructive** (grün) – der Befehl liest nur Daten oder fügt neue hinzu, wie `/take-backup`, das ein Backup anlegt und sonst
  nichts ändert.

Das Etikett ist eine Information für dich; es hält den Befehl beim Ausführen nicht auf. Ein [Agent](../agents.md) richtet sich danach:
Er bekommt keinen destruktiven Befehl, oder er muss dich vorher fragen, oder er führt ihn aus, wie es für diesen Agenten
eingestellt ist. Ein Befehl, der selbst um Bestätigung bittet, wird unten beschrieben.

<a id="commands-that-ask-first"></a>

## Befehle, die vorher nachfragen

Ein Befehl, der viel verändert, kann vorher nachfragen. Die Such- und Ersetzbefehle haben keine Vorschau: Nach der Suche
zeigen sie, wie viele Treffer in wie vielen Einträgen gefunden wurden und was durch was ersetzt würde. Nur **Yes** ändert
das Buch; **No** lässt alles so, wie es war. Die Frage bleibt stehen, bis du sie beantwortest.

![Die Rückfrage von /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Befehle, die mit Marginalia mitkommen

| Befehl                               | Was er tut                                                                         |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Suche und Ersetzen im Text des geöffneten Buchs, wie das Fenster **Search and replace** des Reiters Story. Die Optionen sind `true`/`false`, in dieser Reihenfolge; jede Option, die Sie weglassen, hat den Standardwert dieses Fensters ([Regex](../regular-expressions.md) nein, Groß-/Kleinschreibung beachten ja, Zusammenfassungen ja, alle Zweige nein, neu validieren ja). Um eine davon zu setzen, geben Sie vorher auch alle davorstehenden an. `maxHits` verweigert eine Ersetzung, die mehr Treffer findet als diese Zahl; mit `dryRun` `true` wird nichts verändert: die Antwort zählt die Treffer und zeigt einige, so schaut ein Agent nach, bevor er ersetzt. Es gibt keine Vorschau: nach der Suche wird gefragt, ob die gefundene Anzahl an Treffern ersetzt werden soll, und nur **Yes** ändert das Buch. Die Ersetzung kann nicht abgebrochen werden. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Dasselbe für die Einträge (Name, Text und Kommentar) des Lorebooks des geöffneten Buchs. |
| `/take-backup`                       | Legt eine Sicherungskopie des geöffneten Buchs an; sie erscheint im Reiter **Backups** des Buchs. Meldet, dass kein Buch geöffnet ist, wenn keines offen ist. |
| `/get-book-info` | Name, Einstellungen, Modell, Summen (Teile, Wörter, Tokens) und Tags des geöffneten Buchs. |
| `/list-parts [branch] [offset] [limit]` | Die Teile eines Zweigs: Handle, Position, Größe, Swipes, Zustand der Zusammenfassung und eine kurze Vorschau. |
| `/get-part <part> [branch] [offset] [length] [field]` | Der Text eines Teils in Abschnitten (oder seine `instructions`, `scene`, `pov`, `present` oder `reasoning`) und woraus er entstanden ist. |
| `/list-swipes <part> [branch]` | Die Alternativen (Swipes) eines Teils, mit der Zweignummer, unter der jede angezeigt wird. |
| `/list-branches [offset] [limit]` | Die Zweige des Buchs, nummeriert, mit der Stelle, an der sich jeder von den anderen trennt. |
| `/get-part-metrics <part> [branch]` | Tokens, Zeiten, Modell und Protokoll eines Teils. |
| `/show-prompt <part> [branch] [offset] [length]` | Der Prompt, aus dem ein generierter Teil entstanden ist, so wie er gespeichert wurde. |
| `/search-parts <query> [limit]` | Volltextsuche in den Teilen aller Zweige, mit Ausschnitten. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Wie oft und wo Namen oder Wörter vorkommen (Standardwerte: kein Regex, Groß-/Kleinschreibung ignorieren, nur Teile, aktiver Zweig). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Die Sätze, die sich zwischen zwei Teilen unterscheiden; `0` als `b` vergleicht Teil `a` mit dem Text, den das Modell zuerst erzeugt hat. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Tokens (mit dem Tokenizer des Modells des Buchs), Wörter und Zeichen eines Textes oder eines Teils, wenn der Text dessen Handle ist. |
| `/list-summaries [branch]` | Die Zusammenfassungen eines Zweigs, die Teile, die sie abdecken, und ob sie noch gültig sind. |
| `/get-summary <part> [branch]` | Die Zusammenfassung eines Teils. |
| `/check-summaries [branch]` | Die veralteten Zusammenfassungen und die bearbeiteten Teile, die sie veralten lassen. |
| `/get-template-variables <scope> [part] [branch]` | Die Template-Variablen des Buchs (`global`) oder eines Teils (`part`). |
| `/list-lorebooks` | Die Lorebooks des geöffneten Buchs (das Wurzel-Lorebook und seine Unter-Lorebooks) mit ihren Nummern, Eintragszahlen, Tags und Unter-Lorebooks. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Die Einträge eines Lorebooks mit ihren Einstellungen und einer Vorschau; der Filter behält die Einträge, deren Name oder Tag ihn enthält. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Ein Eintrag: Einstellungen, Schlüssel, Aktivierung (Rekursion, Kontext, Bereich der Teile, Bedingung), Tags, Kommentar und sein Text in Abschnitten. |
| `/search-lore <query> [regex] [limit]` | Die Einträge aller Lorebooks des Buchs, deren Name, Text oder Kommentar die Suchanfrage enthält, mit Ausschnitten. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Welche Einträge eine Generierung für einen Text oder für einen Zug wie den, der einen Teil erzeugt hat, verwenden würde, und warum (kein Filter; Filter gefunden – im Text oder im Inhalt eines anderen Eintrags, wenn das Lorebook eine [Scan-Tiefe](../lorebooks/entries.md#entries-brought-in-by-other-entries) hat – oder nicht verwendet, weil er ausgeschaltet ist, ein Tag des Buchs entscheidet, oder der Filter steht nicht im Text), mit dem Lore-Text und seinen Tokens. Der Text darf das Handle eines Teils sein. |
| `/list-annotations [status [part [offset [limit]]]]` | Die [Anmerkungen](../books/annotations.md) des Buchs oder eines Teils: die offenen, sofern `status` nicht `resolved`, `dismissed`, `orphaned` oder `all` angibt. |
| `/get-annotation <annotation>` | Eine Anmerkung vollständig, mit dem Text um ihre Stelle herum. |
| `/get-book-statistics [range] [side]` | Was am geöffneten Buch Tag für Tag getan wurde (Tokens, Wörter, Teile, Bearbeitungen, Zeit, Größe): siehe [Schreibstatistik](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | Dasselbe für alle Ihre Bücher, nach Tag oder nach Buch. Benötigt kein geöffnetes Buch. |

<a id="reading-the-story"></a>

### Die Geschichte lesen

Die oben genannten Befehle, die das Buch lesen (`/get-book-info` bis `/get-template-variables`), verändern nichts;
sie antworten als JSON-Text, sodass auch ein Agent sie verwenden kann. Jede Zeile einer Antwort hat eine **`handle`**: die Kennung des Teils (oder Lorebooks bzw. Eintrags), die sich nicht ändert, wenn Teile hinzugefügt, verschoben oder gelöscht werden.
Ein Teil wird durch sein Handle oder durch seine **Position** auf einem Zweig angegeben, wobei der erste Teil der Geschichte `1` ist; ein Zweig durch das Handle eines beliebigen seiner Teile oder durch seine **Nummer** ab `/list-branches` (die Nummern folgen der Erstellung der Zweigenden, sie ändern sich also, wenn ein Zweig hinzugefügt oder gelöscht wird).
Positionen und Nummern sind leichter einzugeben, Handles sind das, was ein Agent verwenden sollte, und die Befehle, die die Geschichte verändern, akzeptieren nur Handles. Ohne Angabe eines Zweigs arbeiten die Befehle mit dem aktiven Zweig.
Listen werden mit `offset` und `limit` seitenweise abgerufen, lange Texte werden in Abschnitte geschnitten (`offset`, `length`), und die Antwort nennt die Gesamtlänge. Wie bei den anderen Befehlen sind die Optionen positional: Um eine zu setzen, geben Sie vorher auch alle davorstehenden an.

Die Befehle, die die Lorebooks lesen (`/list-lorebooks` bis `/preview-lore-activation`), funktionieren genauso und erreichen nur die Lorebooks des geöffneten
Buchs: das Wurzel-Lorebook und seine Unter-Lorebooks. Ein Lorebook wird durch sein **Handle** oder seine **Nummer** ab `/list-lorebooks` angegeben (das Wurzel-Lorebook ist `1`), ein Eintrag durch sein **Handle** oder seine **Nummer im Lorebook** in der Reihenfolge des Lorebooks (der erste Eintrag ist `1`); die Nummern ändern sich, wenn Einträge oder Unter-Lorebooks hinzugefügt oder entfernt werden. Ohne Angabe eines Lorebooks wird das Wurzel-Lorebook verwendet.
<a id="changing-the-story-and-the-lore"></a>

### Die Geschichte und die Lorebooks ändern

Diese Befehle ändern das geöffnete Buch (oder die Arbeitskopie eines Agenten, dorthin gehen die Änderungen eines Agenten). Ein Teil, ein Lorebook oder ein Eintrag wird immer über sein **Handle** angegeben, nie über eine Position oder eine Nummer: Diese verschieben sich, wenn etwas hinzugefügt oder gelöscht wird, und eine Position, die beim Lesen stimmte, kann beim Schreiben schon einen anderen Teil bezeichnen. Die Optionen sind wie zuvor positionsabhängig. Ein Befehl, der Text oder Daten entfernt, ist `DESTRUCTIVE` und verlangt eine Bestätigung (ein Agent wird stattdessen durch seinen destruktiven Modus abgedeckt); Hinzufügen und Ändern nicht. Eine Änderung der Geschichte kann Zusammenfassungen veralten lassen (eine Zusammenfassung trägt den Hash der Geschichte, die sie beschreibt): Die Antwort listet sie auf und sagt, was zu tun ist, `/revalidate-summaries` wenn sie noch stimmen, `/regenerate-summary` wenn nicht.

| Befehl | Was er tut |
|--------|------------|
| `/edit-part <part> <old> <new> [field]` | Ersetzt `old` durch `new` im Text eines Teils; `old` muss genau einmal gefunden werden, sonst ändert sich nichts und der Fehler sagt, wie oft er gefunden wurde. `field` bearbeitet stattdessen die `instructions`, `scene`, `pov` oder `present` Zeichen des Turns. |
| `/replace-part-text <part> <text> [field]` | Ersetzt den gesamten Text eines Teils. Fragt vorher nach. |
| `/append-to-part <part> <text> [separator]` | Hängt Text ans Ende eines Teils an, nach einer Leerzeile, sofern `separator` nichts anderes sagt. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Ändert, woraus ein Teil erzeugt wurde, was eine neue Generierung des Teils wiederverwendet; ein leerer Text leert ein Feld, ein weggelassenes Feld bleibt. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Schreibt einen neuen Teil nach dem letzten Teil des aktiven Zweigs oder nach `parent` (ein neuer Zweig); er wird das Ende des aktiven Zweigs. |
| `/insert-part <text> <part> [before]` | Fügt einen neuen Teil direkt nach `part` in die Geschichte ein (oder direkt davor, wenn `before` `true` ist). Alles, was auf diese Stelle folgte, alle Zweige eingeschlossen, folgt jetzt dem neuen Teil; nichts wird gelöscht oder neu erzeugt, daher behalten die anderen Teile ihre Zusammenfassungen und Daten und rücken eine Position nach unten. Die Zusammenfassungen, die die Stelle abdeckten, veralten (die Antwort sagt, welche). Am Ende des Zweigs ist es dasselbe wie `/create-part`. |
| `/branch-part <part>` | Kopiert einen Teil (und seine Zusammenfassung) daneben, als neuen Swipe, der einen Zweig beginnt, und macht die Kopie zum aktiven Ende. |
| `/delete-part <part>` | Löscht einen Teil und seine Zusammenfassung; die Teile, die ihm folgten, bleiben. Fragt vorher nach. |
| `/switch-branch <part>` | Macht den Zweig durch einen Teil zum aktiven. |
| `/set-summary <part> <text>` | Ersetzt den Text der Zusammenfassung eines Teils. |
| `/remove-summary <part> [unwind]` | Entfernt die Zusammenfassung eines Teils (`unwind` holt zurück, was eine Meta-Zusammenfassung ersetzt hat). Fragt vorher nach. |
| `/revalidate-summaries [part]` | Erklärt, dass die veralteten Zusammenfassungen die Geschichte weiterhin beschreiben (den gesamten aktiven Zweig oder nur den Teil). Nur verwenden, wenn du es geprüft hast. |
| `/regenerate-summary <part>` | Lässt das Modell die Zusammenfassung eines Teils neu erstellen. Langsam, kostet Tokens. |
| `/create-meta-summary <from> <to>` | Lässt das Modell die Zusammenfassungen vom Teil `from` (neueste) bis `to` zu einer einzigen zusammenführen. Langsam, kostet Tokens. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Lässt das Modell den nächsten Teil schreiben, wie die Schaltfläche **New turn**. Langsam, kostet Tokens; eine Generierung eines Buches läuft gleichzeitig, die anderen warten. `profile` benennt eines der Modellprofile eines Agenten. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Lässt das Modell eine andere Version des letzten Teils eines Zweigs schreiben. Was du weglässt, wird aus dem Teil übernommen. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Lässt das Modell den letzten Teil eines Zweigs neu schreiben und seinen Text ersetzen. Fragt vorher nach. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Lässt das Modell einen beliebigen Teil nach deinen Anweisungen neu schreiben (siehe [Einen Teil neu entwerfen](../books/story-editor.md#redrafting-a-part)) und ersetzt seinen Text. `prompt` ist ein Überarbeitungs-Prompt, der statt dem des Buches verwendet wird, `profile` ein Modellprofil eines Agenten, `apply` `false` gibt nur den Text zurück. `DESTRUCTIVE`; fragt vorher nach. Langsam, kostet Tokens. |
| `/impersonate [instructions] [outline] [prompt]` | Lässt das Modell die Anweisungen des nächsten Turns an deiner Stelle entwerfen (siehe [Anweisungen entwerfen](../books/story-editor.md#drafting-the-instructions)) und gibt den Text zurück; nichts wird verändert. `instructions` ist, worum es im Teil gehen soll (leer führt die Geschichte fort, wohin sie führt), `outline` ein Plan der Geschichte für das Modell, `prompt` ein Impersonate-Prompt, der statt dem des Buches verwendet wird. Nur für dich an der Befehlszeile: Ein [Agent](../agents.md) bekommt ihn nicht angeboten. Langsam, kostet Tokens. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Ändert die beschreibenden Einstellungen des Buches; nur die Werte, die du angibst. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Ersetzt die Prompt-Vorlagen des Buches. Jede wird vorher geprüft. `DESTRUCTIVE`: Eine fehlerhafte Vorlage verdirbt jede spätere Generierung. |
| `/set-book-tags [add] [remove]` | Fügt Tags des Buches hinzu und entfernt sie (durch Kommas getrennt). |
| `/set-book-history-limit [maxVersions]` | Legt fest, wie viele Versionen eines Teils der [Verlauf](../books/version-history.md#how-many-versions-are-kept) des Buches behält (`0` behält ab jetzt keine); ohne Zahl setzt das Buch kein Limit. Die Limits der Ordner, des Benutzers und des Administrators gelten weiterhin. Löscht keine Version. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Fügt einen Eintrag zu einem Lorebook hinzu (die Wurzel, wenn keines angegeben ist; ein Buch ohne Lorebook bekommt eines, benannt wie das Buch). `keys` ist eine Liste von Schlüsselwörtern, durch Kommas getrennt; `filteringMode` ist `KEYWORDS_OR` (Standard), `KEYWORDS_AND`, `TEXT` oder `REGEX`. Ein Problem der Vorlage oder des regulären Ausdrucks wird gespeichert und gemeldet. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Ändert die angegebenen Felder. |
| `/delete-lore-entry <entry>` | Löscht einen Eintrag. Fragt vorher nach. |
| `/move-lore-entry <entry> <lorebook>` | Verschiebt einen Eintrag in ein anderes Lorebook des Buches. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Ändert die Tags und negativen Tags eines Eintrags (durch Kommas getrennt). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Ändert, wann ein Eintrag verwendet wird: `preventRecursion`, `recursionOnly` und `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` oder `INCLUDE_PARTS`) mit `activationParts`, den Bereich der Teile `fromMessage` / `toMessage` (0 entfernt eine Grenze) und die `condition`-Vorlage (leerer Text entfernt sie). Ein als leerer Text angegebener Wert bleibt unverändert (außer `condition`). |
| `/create-lorebook <name> [parent]` | Erstellt ein Lorebook, das ein Unterbuch von `parent` ist (die Wurzel, wenn keines angegeben ist; ein Buch ohne Lorebook bekommt zuerst eines). |
| `/set-lorebook <lorebook> [name] [enabled]` | Benennt ein Lorebook um oder schaltet es ein oder aus. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Ändert, welche Lorebooks ein Lorebook enthält; ein Lorebook kann sich nicht selbst enthalten. Für einen [Agenten](../agents.md) bringt `/add-subbook` außerdem ein Lorebook zurück, das er zuvor gelöscht oder aus seinem übergeordneten Lorebook herausgenommen hat (er benennt es über das Handle, das es hatte), samt seinen Einträgen. |
| `/delete-lorebook <lorebook>` | Löscht ein Lorebook mit seinen Einträgen (nicht die Wurzel). Fragt vorher nach. |
<a id="annotations"></a>

### Anmerkungen

Die Befehle, die [Anmerkungen](../books/annotations.md) lesen (`/list-annotations`, `/get-annotation`), antworten im JSON wie die anderen Lesebefehle;
eine Anmerkung wird über ihre **Kennung** angegeben, ein Teil über seine Kennung bei den Befehlen, die etwas ändern. Diese Befehle ändern nur die Notizen,
nie die Geschichte, deshalb fragt nur das Löschen vorher nach. Ein Agent kann alle verwenden; seine Notizen gelangen in dein Buch, wenn du [das Ergebnis prüfst](../books/annotations.md#annotations-and-agents).

| Befehl | Was er tut |
|--------|------------|
| `/create-annotation <part> <text> [quote [type]]` | Fügt einem Teil eine Notiz hinzu. `quote` ist der Wortlaut der Passage, aus dem Teil kopiert: Die Notiz sitzt an dieser Passage und folgt ihr, wenn der Text bearbeitet wird. Ohne Zitat oder mit einem, das nicht im Text steht, bezieht sich die Notiz auf den ganzen Teil (die Antwort sagt das). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Ändert die Notiz, den Typ oder ob das Modell die Notiz erhält; ein leerer Text oder Typ bleibt, wie er ist. |
| `/resolve-annotation <annotation> [status]` | Setzt den Status: `resolved` (Standard), `dismissed` oder `open`. |
| `/reattach-annotation <annotation> [quote]` | Verschiebt eine Notiz an die Passage, die das Zitat in ihrem Teil findet; ohne Zitat bezieht sich die Notiz auf den ganzen Teil. |
| `/delete-annotation <annotation>` | Löscht eine Notiz (sie kann aus dem Papierkorb wiederhergestellt werden). Fragt vorher nach. |
| `/scan-phrases [from to]` | Prüft die Teile des aktiven Zweigs auf die [Phrasenwarnliste](../books/annotations.md) des Buchs und legt für jeden neuen Treffer eine Notiz an (ohne Modell). Ohne Argumente den ganzen Zweig; mit zwei Teilen die Teile von dem einen bis zum anderen. Antwortet, wie viele Notizen angelegt wurden. |
| `/generate-annotation <part> [profile]` | Lässt das Modell den Teil wie [Mit KI annotieren](../books/annotations.md#letting-a-model-annotate) prüfen und fügt alles, was es findet, als Notizen hinzu (Quelle *AI*; ein Zitat, das nicht im Text steht, ergibt eine Notiz zum ganzen Teil). Es wird nichts nachgefragt: Die Notizen sind die einzige Änderung, und sie können erledigt, bearbeitet oder gelöscht werden. Es prüft mit dem Modell des Buchs; ein Agent verwendet sein eigenes Modell, wenn *Write the book with the model of the agent* eingeschaltet ist (wie die Befehle des Lore Generator), `profile` ist ein Modellprofil eines Agenten. Läuft für sich allein, braucht Zeit und kostet Tokens. |

<a id="the-planner"></a>

### Der Planer

Die Befehle für den [Planer](../books/story-editor.md#the-planner-tab) des Buchs. Eine Szene wird über ihre **Nummer** in der Liste angegeben (ab 1; `/get-planned-scenes`
zeigt sie an). Die Nummern ändern sich, wenn eine Szene verschoben oder gelöscht wird, also lies die Liste nach jeder Änderung erneut. Sie ändern nur den Planer, nie die Geschichte. Hinzufügen,
Verschieben und Markieren einer Szene als erledigt werden nicht nachgefragt; den Text einer Szene zu ändern und eine Szene zu löschen sind dagegen destruktiv (ein [Agent](../agents.md) wird wie bei anderen destruktiven Befehlen gefragt).

| Befehl | Was er tut |
|--------|------------|
| `/get-planned-scenes` | Die Szenen mit ihrer Nummer, ihrem Titel, Schauplatz, POV-Figur, anwesenden Figuren, Anweisungen und ob sie erledigt sind. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Fügt eine Szene am Ende der Liste hinzu. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Ändert den Titel und die angegebenen Details; was weggelassen wird, bleibt erhalten, ein leerer Text löscht es. |
| `/set-planned-scene-done <scene> [done]` | Markiert die Szene als geschrieben (`done` ist `true` als Standard) oder als nicht geschrieben. |
| `/move-planned-scene <scene> <to>` | Verschiebt die Szene an die Stelle `to` (1 ist die erste, bei einer zu großen Zahl die letzte Stelle). |
| `/delete-planned-scene <scene>` | Löscht die Szene. |

Die Befehle, bei denen das Modell schreibt oder prüft, brauchen Zeit: `/generate-annotation` und die beiden Zusammenfassungen zeigen das Fenster **Stop**,
`/next`, `/swipe` und `/regenerate` werden in die Geschichte gestreamt (siehe [Befehle mit längerer Laufzeit](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Befehle, die mit Erweiterungen kommen

Diese siehst du in der Liste, solange die Erweiterung geladen ist; jeder wird in der Dokumentation seiner Erweiterung beschrieben.

| Befehl | Erweiterung | Was er tut |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | Deine gespeicherten Generierungs-Prompts oder der Text eines davon. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Generiert den Text eines Lore-Eintrags mit dem Lore des Lorebuchs, verdichtet ihn (außer `compact` ist `false`) und gibt ihn zurück. Speichert nichts. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Verdichtet einen generierten Text zu einem dichten Lore-Eintragstext und gibt ihn zurück. Speichert nichts. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Die Kategorien erfundener Namen und Namen aus einer davon. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Die Listen echter Namen und Namen, die aus einer davon gewählt werden. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | Die Typen echter Orte, eine Suche nach Name, Typ und Land, die Orte rund um einen Punkt und die Entfernung zweier Orte. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Die Bögen und Kapitel der Gliederung. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Hinzufügen, Ändern, Verschieben und Löschen eines Bogens (`<arc>` ist seine Nummer). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | Dasselbe für die Kapitel eines Bogens. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Die Generatoren des Tabs Arcs. Sie geben das Ergebnis zurück und fügen es nur mit `save` der Gliederung hinzu. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | Die Figuren des Buchs und eine mit ihrer Notiz und ihren Beziehungen. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Hinzufügen, Ändern und Löschen einer Figur. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Die Beziehungen zwischen Figuren. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Die Seiten der Notizen des Buchs (nicht die, die vor Assistenten verborgen sind), eine davon mit ihrem Text und ihren Links, und eine Suche. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Hinzufügen, Ergänzen, Ändern, Bearbeiten und Löschen einer Seite; `note-text` ist für das Makro {%{`{{command::note-text::Title}}`}%} gedacht. |

Die Befehle des Lore Generator, des Name generator, des Name provider und des Place provider lesen nur: Die generierenden geben Text zurück und
überlassen es dir oder dem Agenten, der sie aufgerufen hat, ihn in einen Eintrag zu übernehmen. Die Befehle von Arc Outline und
Character Tracker ändern auch die Gliederung und die Figuren des geöffneten Buchs; nur ihre `delete`-Befehle sind destruktiv. Story Notes
ändern ebenfalls die Seiten des geöffneten Buchs; dort sind `update-note` und `edit-note` ebenfalls destruktiv, da sie vorhandenen Text ersetzen.

<a id="limits"></a>

## Grenzen

- Die Befehlszeile befindet sich im Tab Story des Desktop-Layouts. Die [Telefonansicht](../phone.md) hat sie noch nicht.
- Ein Befehl läuft als du, in deiner Sitzung, und arbeitet mit dem Buch, das du geöffnet hast.
