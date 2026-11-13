---
label: Befehle
order: 945
icon: terminal
---

# Befehle

Befehle sind kleine Aktionen, die Sie durch Eintippen ausführen, anstatt sich durch Menüs zu klicken: etwas in der Geschichte oder
den Lorebooks nachschlagen, suchen und ersetzen, ein Backup anlegen. [Erweiterungen](../extensions/index.md) fügen eigene Befehle hinzu,
zum Beispiel um einen Lore-Eintrag zu erzeugen oder Namen zu erfinden. Dieselben Befehle nutzt auch ein [Agent](../agents.md),
daher tut ein Befehl genau eine klare Sache und sagt, was er tut: Sie können es lesen, während Sie tippen.

Die **Befehlszeile** ist das Feld neben dem Menü unter der Geschichte, im [Geschichtseditor](../books/story-editor.md).

<a id="running-a-command"></a>

## Einen Befehl ausführen

1. Klicken Sie in die Befehlszeile und tippen Sie `/`. Die Liste zeigt alle Befehle, jeweils mit Namen und einer kurzen
   Beschreibung. Wenn Sie weitertippen, wird die Liste enger: Der Filter schaut auf den Namen des Befehls, seinen Titel und seine Beschreibung.
2. Wählen Sie den Befehl mit der Maus oder mit den Pfeiltasten und **Enter**. Ein Befehl, der nichts braucht, läuft sofort; bei einem,
   der Argumente verlangt, wird `/name ` in das Feld geschrieben und Sie fahren mit den Argumenten fort.
![Die Liste der Befehle, geöffnet durch Eingabe eines Schrägstrichs](../../images/command-list.png)

3. Tippen Sie die Argumente und drücken Sie **Enter** oder klicken Sie auf die Abspielen-Schaltfläche neben dem Feld, um ihn auszuführen. Das Feld wird
   geleert, sobald der Befehl startet; ein Text, der kein Befehl ist, bleibt stehen, damit Sie ihn korrigieren können.

Text, der nicht mit `/` beginnt, ist kein Befehl, und es wird nichts ausgeführt. Ein Klick außerhalb des Feldes oder das Drücken von **Esc**
führt nie etwas aus, sodass Sie einen Befehl nicht versehentlich starten können.

<a id="the-help"></a>

## Die Hilfe

Während Sie tippen, öffnet sich ein kleines Fenster über dem Feld. Es verdeckt einen Teil der Geschichte, verschiebt aber nichts, und es schließt sich,
wenn Sie das Feld verlassen oder den Befehl ausführen. Es zeigt:

- was der Befehl tut,
- die Formen des Befehls (siehe [Formen](#forms-of-a-command)), die zu dem passen, was Sie bisher getippt haben, wobei das gerade getippte Argument hervorgehoben und mit seiner Beschreibung angezeigt wird,
- was der Befehl zurückgibt,
- einen Hinweis, wenn Sie mehr Argumente getippt haben, als der Befehl annimmt, oder wenn kein Befehl diesen Namen hat.

![Die Hilfe von /search-replace-lore-entries, während das zweite Argument getippt wird](../../images/command-help.png)

Solange Sie noch den Namen tippen, ist die Liste der Befehle zugleich die Hilfe.

<a id="arguments"></a>

## Argumente

Argumente werden durch Leerzeichen getrennt, und mehrere Leerzeichen hintereinander zählen als eines. Um ein Leerzeichen *innerhalb* eines Arguments
zu schreiben, gibt es drei Möglichkeiten:

| Sie tippen                   | Der Befehl erhält               | Warum                                      |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | ein Argument, `two words`       | `\ ` (Backslash, Leerzeichen) ist ein Leerzeichen |
| `/search-parts "two words"`  | ein Argument, `two words`       | doppelte Anführungszeichen halten alles zusammen |
| `/search-parts say\ "hi"`    | ein Argument, `say hi`          | Anführungszeichen können mitten in einem Argument beginnen |

Die anderen Sonderzeichen:

| Sie tippen                    | Sie erhalten               |
|-------------------------------|----------------------------|
| `\\`                          | einen Backslash            |
| `\"`                          | ein doppeltes Anführungszeichen, mit oder ohne Anführungszeichen drumherum: `"say \"hi\""` ergibt `say "hi"` |
| `""`                          | ein leeres Argument        |

Ein Backslash vor allem anderen bleibt erhalten, sodass `C:\books\story` unverändert übergeben wird. Wenn Sie ein Anführungszeichen öffnen und
nie schließen, gehört alles bis zum Zeilenende zu diesem Argument.

Argumente sind Text. Ein Befehl, der eine Zahl, ein Ja/Nein oder eine von wenigen Optionen erwartet, wandelt den Text selbst um; passt der Text nicht,
wird der Befehl nicht ausgeführt, und eine Benachrichtigung teilt das mit.

<a id="forms-of-a-command"></a>

## Formen eines Befehls

Ein Befehl kann verschiedene Argumente annehmen: `/list-parts` ohne alles und `/list-parts 2` mit einem Argument
sind zwei **Formen** desselben Befehls. Marginalia wählt die Form anhand der Anzahl der getippten Argumente und danach, wie sie aussehen. Passt keine Form,
meldet eine Benachrichtigung, dass der Befehl diese Argumente nicht annimmt, und die Hilfe zeigt die Formen, die er hat.

<a id="the-result"></a>

## Das Ergebnis

Gibt ein Befehl etwas zurück, wird es in einem Fenster über der Befehlszeile angezeigt. Schließen Sie es mit dem Kreuz, mit **Esc**
oder fahren Sie einfach fort: Es verschwindet, sobald Sie etwas Neues tippen oder einen anderen Befehl ausführen. Ein Befehl, der nichts zurückgibt, zeigt
kein Fenster.

![Das Ergebnis von /take-backup über der Befehlszeile](../../images/command-result.png)

Schlägt ein Befehl fehl oder gibt es keinen solchen Befehl, erhalten Sie eine Benachrichtigung mit dem Grund.

<a id="commands-that-take-time"></a>

## Befehle, die Zeit brauchen

Die meisten Befehle antworten sofort. Ein Befehl, der Zeit braucht, zeigt ein Fenster mit einem animierten Balken und einer **Stopp**-Schaltfläche. Das
Fenster steht seitlich, und die Seite bleibt bedienbar; sein ⌄ klappt es in das
[Laufpanel](../books/story-editor.md#the-panel-of-the-runs) ein, wo der Befehl mit dem aufgeführt wird, was das Modell schreibt, auf allen
Ihren Seiten. Unter dem Balken kann der Befehl sagen, was er gerade tut, und in kleinerer Schrift das Detail des Schritts. **Stopp** bricht den Befehl ab; das
sollte sofort zurückkehren. Befehle, bei denen das Modell eine Zusammenfassung oder Notizen schreibt, etwa `/generate-annotation`, gehören zu dieser Art.

`/next`, `/swipe` und `/regenerate`, die in der Befehlszeile der Geschichte getippt werden, haben kein solches Fenster: Der neue Teil wird wie beim Drücken der runden Schaltfläche
in die Geschichte gestreamt, die runde Schaltfläche stoppt ihn, und die Geschichte wird nur so gehalten wie bei jeder Generierung (Sie können
weiter an den Lorebooks, den anderen Teilen und den Seitenreitern arbeiten). Das Ergebnis des Befehls erscheint über der Leiste, wenn der Teil fertig ist. Bis dahin
nimmt die Leiste keinen zweiten dieser Befehle an (Sie behalten den Text und können ihn später ausführen); andere Befehle funktionieren.

![Das Fenster eines laufenden Befehls](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Befehle, die Daten ändern

Jeder Befehl sagt, was er mit den bereits vorhandenen Daten macht, und die Befehlsleiste zeigt das als kleine Markierung neben dem Befehl, in der Liste und in der Hilfe:

- **Destruktiv** (rot) – der Befehl ändert oder entfernt vorhandene Daten, wie die Such- und Ersetzen-Befehle, die das Buch umschreiben. Zeigen Sie auf die Markierung, um die Erklärung zu sehen.
- **Nicht destruktiv** (grün) – der Befehl liest Daten nur oder fügt neue hinzu, wie `/take-backup`, das ein Backup anlegt und sonst nichts ändert.

Die Markierung ist eine Information für Sie; sie hält den Befehl beim Ausführen nicht auf. Ein [Agent](../agents.md) richtet sich danach: Ihm wird kein destruktiver Befehl gegeben, oder er muss vorher bei Ihnen nachfragen, oder er führt ihn aus, wie für diesen Agenten eingestellt. Ein Befehl, der selbst um Bestätigung bittet, wird unten beschrieben.

<a id="commands-that-ask-first"></a>

## Befehle, die vorher fragen

Ein Befehl, der viel verändert, kann vor dem Ausführen nachfragen. Die Such- und Ersetzen-Befehle haben keine Vorschau; nach der Suche zeigen sie, wie viele Treffer sie in wie vielen Elementen gefunden haben und was durch was ersetzt würde, und nur **Ja** ändert das Buch; **Nein** lässt alles, wie es war. Die Frage bleibt stehen, bis Sie antworten.

![Die Frage von /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Befehle, die Marginalia mitbringt

| Befehl                              | Was er tut                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Sucht und ersetzt im Geschichtentext des geöffneten Buches, wie das Fenster **Suchen und Ersetzen** im Reiter der Geschichte. Die Optionen sind `true`/`false` und in dieser Reihenfolge; jede weggelassene Option hat den Standardwert dieses Fensters ([regex](../regular-expressions.md) nein, Groß-/Kleinschreibung ja, Zusammenfassungen ja, alle Zweige nein, Zusammenfassungen neu prüfen ja). Um eine Option zu setzen, gib alle davor ebenfalls an. `maxHits` verweigert ein Ersetzen, das mehr Treffer findet als angegeben. Mit `dryRun` `true` wird nichts verändert: Die Antwort zählt die Treffer und zeigt einige davon, so sieht ein Agent nach, bevor er ersetzt. Es gibt keine Vorschau: Nach der Suche wird gefragt, ob die gefundene Anzahl von Treffern ersetzt werden soll, und nur **Ja** ändert das Buch. Das Ersetzen kann nicht abgebrochen werden. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Dasselbe für die Einträge (Name, Text und Kommentar) des Lorebuchs des geöffneten Buches. |
| `/take-backup`                       | Legt eine Sicherung des geöffneten Buches an; sie erscheint im Reiter **Sicherungen** des Buches. Meldet, dass kein Buch geöffnet ist, wenn keines offen ist. |
| `/get-book-info` | Name, Einstellungen, Modell, Summen (Teile, Wörter, Tokens) und Schlagwörter des geöffneten Buches. |
| `/list-parts [branch] [offset] [limit]` | Die Teile eines Zweigs: Kennung, Position, Umfang, Alternativen (Swipes), Zustand der Zusammenfassung und eine kurze Vorschau. |
| `/get-part <part> [branch] [offset] [length] [field]` | Der Text eines Teils in Abschnitten (oder seine `instructions`, `scene`, `pov`, `present` oder `reasoning`) und woraus er entstanden ist. |
| `/list-swipes <part> [branch]` | Die Alternativen (Swipes) eines Teils mit der Zweignummer, unter der sie jeweils angezeigt werden. |
| `/list-branches [offset] [limit]` | Die Zweige des Buches, nummeriert, mit Angabe, wo sich jeder von den anderen abzweigt. |
| `/get-part-metrics <part> [branch]` | Tokens, Zeiten, Modell und Protokoll eines Teils. |
| `/show-prompt <part> [branch] [offset] [length]` | Der Prompt, aus dem ein generierter Teil entstanden ist, so wie er gespeichert wurde. |
| `/search-parts <query> [limit]` | Volltextsuche in den Teilen aller Zweige, mit Textausschnitten. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Wie oft und wo Namen oder Wörter vorkommen (Standardwerte: keine regulären Ausdrücke, Groß-/Kleinschreibung ignorieren, nur Teile, aktiver Zweig). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Die Sätze, die sich zwischen zwei Teilen unterscheiden; `0` als `b` vergleicht Teil `a` mit dem Text, den das Modell zuerst erzeugt hat. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Tokens (mit dem Tokenizer des Modells des Buches), Wörter und Zeichen eines Textes oder eines Teils, wenn der Text dessen Kennung ist. |
| `/list-summaries [branch]` | Die Zusammenfassungen eines Zweigs, die Teile, die sie abdecken, und ob sie noch gültig sind. |
| `/get-summary <part> [branch]` | Die Zusammenfassung eines Teils. |
| `/check-summaries [branch]` | Die veralteten Zusammenfassungen und die bearbeiteten Teile, die sie veraltet haben. |
| `/get-template-variables <scope> [part] [branch]` | Die Vorlagenvariablen des Buches (`global`) oder eines Teils (`part`). |
| `/list-lorebooks` | Die Lorebücher des geöffneten Buches (das Hauptbuch und seine Unterbücher) mit ihren Nummern, Eintragszahlen, Schlagwörtern und Unterbüchern. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Die Einträge eines Lorebuchs mit ihren Einstellungen und einer Vorschau; der Filter behält die Einträge, deren Name oder Schlagwort ihn enthält. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Ein Eintrag: Einstellungen, Schlüssel, Aktivierung (Rekursion, Kontext, Bereich der Teile, Bedingung), Schlagwörter, Kommentar und sein Text in Abschnitten. |
| `/search-lore <query> [regex] [limit]` | Die Einträge aller Lorebücher des Buches, deren Name, Text oder Kommentar die Suchanfrage enthält, mit Textausschnitten. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Welche Einträge eine Generierung für einen Text oder für einen Durchgang wie den, der einen Teil erzeugt hat, verwenden würde, und warum (kein Filter, Filter gefunden - im Text oder im Inhalt eines anderen Eintrags, wenn das Lorebuch eine [Suchtiefe](../lorebooks/entries.md#entries-brought-in-by-other-entries) hat - oder nicht verwendet, weil er ausgeschaltet ist, ein Schlagwort des Buches entscheidet oder der Filter nicht im Text steht), mit dem Loretext und seinen Tokens. Der Text darf die Kennung eines Teils sein. |
| `/list-annotations [status [part [offset [limit]]]]` | Die [Anmerkungen](../books/annotations.md) des Buches oder eines Teils: die offenen, sofern `status` nicht `resolved`, `dismissed`, `orphaned` oder `all` angibt. |
| `/get-annotation <annotation>` | Eine Anmerkung vollständig, mit dem Text um ihre Textstelle herum. |
| `/get-book-statistics [range] [side]` | Was am geöffneten Buch Tag für Tag getan wurde (Tokens, Wörter, Teile, Bearbeitungen, Zeit, Größe): siehe [Schreibstatistik](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | Dasselbe für alle deine Bücher, nach Tag oder nach Buch. Braucht kein geöffnetes Buch. |

<a id="reading-the-story"></a>

### Die Geschichte lesen

Die oben genannten Befehle, die das Buch lesen (`/get-book-info` bis `/get-template-variables`), verändern nichts; sie antworten als JSON-Text,
sodass auch ein Agent sie verwenden kann. Jede Zeile einer Antwort hat eine **`handle`**: die Kennung des Teils (oder des Lorebuchs oder Eintrags),
die sich nicht ändert, wenn Teile hinzugefügt, verschoben oder gelöscht werden. Ein Teil wird über seine Kennung oder über seine **Position** in einem Zweig
angegeben, wobei der erste Teil der Geschichte `1` ist; ein Zweig wird über die Kennung eines beliebigen seiner Teile oder über seine **Nummer** aus
`/list-branches` angegeben (die Nummern folgen der Entstehung der Zweigenden und ändern sich daher, wenn ein Zweig hinzugefügt oder gelöscht wird).
Positionen und Nummern lassen sich leichter eintippen, Kennungen sind das, was ein Agent verwenden sollte, und die Befehle, die die Geschichte
verändern, nehmen nur Kennungen entgegen. Ohne Angabe eines Zweigs arbeiten die Befehle auf dem aktiven Zweig. Listen werden mit `offset` und `limit`
seitenweise ausgegeben, lange Texte werden in Abschnitte (`offset`, `length`) geteilt, und die Antwort nennt die Gesamtlänge. Wie bei den übrigen Befehlen
sind die Optionen positional: Um eine Option zu setzen, gib alle davor ebenfalls an.

Die Befehle, die die Lorebücher lesen (`/list-lorebooks` bis `/preview-lore-activation`), funktionieren genauso und erreichen nur die Lorebücher des
geöffneten Buches: das Hauptlorebuch und seine Unterbücher. Ein Lorebuch wird über seine **Kennung** oder seine **Nummer** aus `/list-lorebooks` angegeben
(das Hauptbuch ist `1`), ein Eintrag über seine **Kennung** oder seine **Nummer im Lorebuch**, in der Reihenfolge des Lorebuchs (der erste Eintrag ist `1`);
die Nummern ändern sich, wenn Einträge oder Unterbücher hinzugefügt oder entfernt werden. Ohne Angabe eines Lorebuchs wird das Hauptlorebuch verwendet.
<a id="changing-the-story-and-the-lore"></a>

### Ändern der Geschichte und des Lore

Diese Befehle ändern das geöffnete Buch (oder die Arbeitskopie eines Agenten, dorthin gehen die Änderungen eines Agenten). Ein Teil, ein Lorebook oder ein Eintrag wird immer über seinen **Handle** angegeben, nie über eine Position oder eine Nummer: Diese verschieben sich, wenn etwas hinzugefügt oder gelöscht wird, und eine Position, die beim Lesen stimmte, kann beim Schreiben bereits einen anderen Teil bezeichnen. Die Optionen sind wie bisher positionsabhängig. Ein Befehl, der Text oder Daten entfernt, ist `DESTRUCTIVE` und fragt zur Bestätigung nach (bei einem Agenten gilt stattdessen dessen destruktiver Modus); Hinzufügen und Ändern fragen nicht nach. Eine Änderung der Geschichte kann Zusammenfassungen veralten lassen (eine Zusammenfassung trägt den Hash der Geschichte, die sie beschreibt): Die Antwort listet sie auf und sagt, was zu tun ist, `/revalidate-summaries`, wenn sie noch stimmen, `/regenerate-summary`, wenn nicht.

| Befehl | Was er tut |
|--------|------------|
| `/edit-part <part> <old> <new> [field]` | Ersetzt `old` durch `new` im Text eines Teils; `old` muss genau einmal gefunden werden, andernfalls ändert sich nichts, und der Fehler nennt, wie oft es gefunden wurde. `field` bearbeitet stattdessen die `instructions`, `scene`, `pov` oder `present`-Figuren des Zuges. |
| `/replace-part-text <part> <text> [field]` | Ersetzt den gesamten Text eines Teils. Fragt zuerst nach. |
| `/append-to-part <part> <text> [separator]` | Fügt Text am Ende eines Teils an, nach einer Leerzeile, sofern `separator` nichts anderes angibt. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Ändert, woraus ein Teil erstellt wurde, was eine neue Generierung davon wiederverwendet; ein leerer Text leert ein Feld, ein weggelassenes Feld bleibt. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Schreibt einen neuen Teil nach dem letzten Teil des aktiven Zweigs oder nach `parent` (ein neuer Zweig); er wird das Ende des aktiven Zweigs. |
| `/insert-part <text> <part> [before]` | Fügt einen neuen Teil direkt nach `part` in die Geschichte ein (oder direkt davor, wenn `before` auf `true` steht). Alles, was auf diese Stelle folgte, alle Zweige eingeschlossen, folgt nun dem neuen Teil; nichts wird gelöscht oder neu erzeugt, sodass die übrigen Teile ihre Zusammenfassungen und Daten behalten und um eine Position nach unten rücken. Die Zusammenfassungen, die diese Stelle abdeckten, veralten (die Antwort sagt, welche). Am Ende des Zweigs ist es dasselbe wie `/create-part`. |
| `/branch-part <part>` | Kopiert einen Teil (und seine Zusammenfassung) neben ihn, eine neue Variante, die einen Zweig beginnt, und macht die Kopie zum aktiven Ende. |
| `/delete-part <part>` | Löscht einen Teil und seine Zusammenfassung; die Teile, die ihm folgten, bleiben. Fragt zuerst nach. |
| `/switch-branch <part>` | Macht den Zweig durch einen Teil zum aktiven. |
| `/set-summary <part> <text>` | Ersetzt den Text der Zusammenfassung zu einem Teil. |
| `/remove-summary <part> [unwind]` | Entfernt die Zusammenfassung eines Teils (`unwind` stellt wieder her, was eine Meta-Zusammenfassung ersetzt hat). Fragt zuerst nach. |
| `/revalidate-summaries [part]` | Bestätigt, dass die veralteten Zusammenfassungen die Geschichte weiterhin beschreiben (der gesamte aktive Zweig oder nur die des genannten Teils). Nur verwenden, wenn Sie es geprüft haben. |
| `/regenerate-summary <part>` | Lässt das Modell die Zusammenfassung zu einem Teil neu erstellen. Langsam, kostet Tokens. |
| `/create-meta-summary <from> <to>` | Lässt das Modell die Zusammenfassungen vom Teil `from` (dem neuesten) bis `to` zu einer einzigen zusammenführen. Langsam, kostet Tokens. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Lässt das Modell den nächsten Teil schreiben, wie die Schaltfläche **New turn**. Langsam, kostet Tokens; eine Generierung eines Buches läuft jeweils allein, die anderen warten. `profile` nennt eines der Modellprofile eines Agenten. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Lässt das Modell eine weitere Fassung des letzten Teils eines Zweigs schreiben. Was Sie weglassen, wird vom Teil übernommen. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Lässt das Modell den letzten Teil eines Zweigs erneut schreiben und ersetzt dabei seinen Text. Fragt zuerst nach. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Lässt das Modell einen beliebigen Teil anhand Ihrer Anweisungen neu schreiben (siehe [Einen Teil neu entwerfen](../books/story-editor.md#redrafting-a-part)) und ersetzt seinen Text. `prompt` ist eine Neuentwurfs-Vorlage, die anstelle der des Buches verwendet wird, `profile` ein Modellprofil eines Agenten, `apply` mit `false` gibt nur den Text zurück. `DESTRUCTIVE`; fragt zuerst nach. Langsam, kostet Tokens. |
| `/impersonate [instructions] [outline] [prompt]` | Lässt das Modell an Ihrer Stelle die Anweisungen für den nächsten Zug entwerfen (siehe [Anweisungen entwerfen](../books/story-editor.md#drafting-the-instructions)) und gibt den Text zurück; es wird nichts verändert. `instructions` ist, worum es im Teil gehen soll (leer führt die Geschichte dorthin weiter, wohin sie tendiert), `outline` ein Plan der Geschichte für das Modell, `prompt` eine Impersonate-Vorlage, die anstelle der des Buches verwendet wird. Nur für Sie an der Befehlszeile: Ein [Agent](../agents.md) bekommt ihn nicht angeboten. Langsam, kostet Tokens. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Ändert die beschreibenden Einstellungen des Buches; nur die Werte, die Sie angeben. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Ersetzt die Prompt-Vorlagen des Buches. Jede wird zuerst geprüft. `DESTRUCTIVE`: Eine fehlerhafte Vorlage verdirbt jede spätere Generierung. |
| `/set-book-tags [add] [remove]` | Fügt Tags zum Buch hinzu und entfernt sie (durch Kommas getrennt). |
| `/set-book-history-limit [maxVersions]` | Legt fest, wie viele Versionen eines Teils der [Verlauf](../books/version-history.md#how-many-versions-are-kept) des Buches behält (`0` behält ab sofort keine); ohne Zahl setzt das Buch kein Limit. Die Limits der Ordner, des Benutzers und der Administration gelten weiterhin. Löscht keine Version. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Fügt einem Lorebook einen Eintrag hinzu (das Wurzel-Lorebook, wenn keines angegeben ist; ein Buch ohne Lorebook bekommt eines, benannt wie das Buch). `keys` ist eine durch Kommas getrennte Liste von Schlüsselwörtern; `filteringMode` ist `KEYWORDS_OR` (Standard), `KEYWORDS_AND`, `TEXT` oder `REGEX`. Ein Problem der Vorlage oder des regulären Ausdrucks wird gespeichert und gemeldet. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Ändert die Felder, die Sie angeben. |
| `/delete-lore-entry <entry>` | Löscht einen Eintrag. Fragt zuerst nach. |
| `/move-lore-entry <entry> <lorebook>` | Verschiebt einen Eintrag in ein anderes Lorebook des Buches. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Ändert die Tags und Gegen-Tags eines Eintrags (durch Kommas getrennt). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Ändert, wann ein Eintrag verwendet wird: `preventRecursion`, `recursionOnly` und `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` oder `INCLUDE_PARTS`) mit `activationParts`, der Bereich der Teile `fromMessage` / `toMessage` (0 hebt eine Grenze auf) und die `condition`-Vorlage (leerer Text entfernt sie). Ein als leerer Text angegebener Wert bleibt unverändert (außer `condition`). |
| `/create-lorebook <name> [parent]` | Erstellt ein Lorebook, das ein Unterbuch von `parent` ist (das Wurzel-Lorebook, wenn keines angegeben ist; ein Buch ohne Lorebook bekommt zuerst eines). |
| `/set-lorebook <lorebook> [name] [enabled]` | Benennt ein Lorebook um oder schaltet es ein oder aus. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Ändert, welche Lorebooks ein Lorebook enthält; ein Lorebook kann nicht sich selbst enthalten. Bei einem [Agenten](../agents.md) holt `/add-subbook` auch ein Lorebook zurück, das er zuvor gelöscht oder aus seinem Elternbuch entfernt hat (es wird über den Handle benannt, den es hatte), samt seinen Einträgen. |
| `/delete-lorebook <lorebook>` | Löscht ein Lorebook mit seinen Einträgen (nicht das Wurzel-Lorebook). Fragt zuerst nach. |
<a id="annotations"></a>

### Annotationen

Die Befehle, die [Annotationen](../books/annotations.md) lesen (`/list-annotations`, `/get-annotation`), antworten wie die anderen Lesebefehle im JSON-Format.
Eine Annotation wird über ihr **Handle** angegeben, ein Teil über sein Handle für die Befehle, die etwas ändern. Diese Befehle verändern nur die Notizen,
nie die Geschichte; nur das Löschen fragt vorher nach. Ein Agent kann alle davon verwenden; seine Notizen gelangen in dein Buch, wenn du [das Ergebnis prüfst](../books/annotations.md#annotations-and-agents).

| Befehl | Was er tut |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | Fügt einem Teil eine Notiz hinzu. `quote` sind die Worte der Textstelle, aus dem Teil kopiert: Die Notiz sitzt an dieser Stelle und folgt ihr, wenn der Text bearbeitet wird. Ohne Zitat oder mit einem, das nicht im Text steht, bezieht sich die Notiz auf den ganzen Teil (die Antwort sagt das). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Ändert die Notiz, den Typ oder ob das Modell die Notiz erhält; ein leerer Text oder Typ bleibt unverändert. |
| `/resolve-annotation <annotation> [status]` | Setzt den Status: `resolved` (Standard), `dismissed` oder `open`. |
| `/reattach-annotation <annotation> [quote]` | Verschiebt eine Notiz zu der Textstelle, die das Zitat in ihrem Teil findet; ohne Zitat bezieht sich die Notiz auf den ganzen Teil. |
| `/delete-annotation <annotation>` | Löscht eine Notiz (sie kann aus dem Papierkorb wiederhergestellt werden). Fragt vorher nach. |
| `/generate-annotation <part> [profile]` | Lässt das Modell den Teil prüfen wie [Mit KI annotieren](../books/annotations.md#letting-a-model-annotate) und fügt alles, was es findet, als Notizen hinzu (Quelle *AI*; ein Zitat, das nicht im Text steht, ergibt eine Notiz zum ganzen Teil). Es wird nichts nachgefragt: Die Notizen sind die einzige Änderung, und sie können gelöst, bearbeitet oder gelöscht werden. Geprüft wird mit dem Modell des Buches; ein Agent verwendet sein eigenes Modell, wenn *Das Buch mit dem Modell des Agenten schreiben* aktiv ist (wie die Befehle des Lore Generators), `profile` ist ein Modellprofil eines Agenten. Läuft allein, braucht Zeit, kostet Tokens. |

<a id="the-planner"></a>

### Der Planer

Die Befehle für den [Planer](../books/story-editor.md#the-planner-tab) des Buches. Eine Szene wird über ihre **Nummer** in der Liste angegeben (ab 1; `/get-planned-scenes`
zeigt sie an); die Nummern ändern sich, wenn eine Szene verschoben oder gelöscht wird, also lies die Liste nach einer Änderung erneut. Diese Befehle ändern nur den Planer, nie die Geschichte. Hinzufügen,
Verschieben und Als-geschrieben-markieren fragen nicht nach; das Ändern des Textes einer Szene und das Löschen einer Szene sind dagegen destruktiv (ein [Agent](../agents.md) wird wie bei anderen destruktiven Befehlen gefragt).

| Befehl | Was er tut |
|---------|--------------|
| `/get-planned-scenes` | Die Szenen mit ihrer Nummer, ihrem Titel, Szenenschauplatz, POV-Figur, anwesenden Figuren, Anweisungen und ob sie geschrieben sind. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Fügt eine Szene am Ende der Liste hinzu. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Ändert den Titel und die angegebenen Angaben; was weggelassen wird, bleibt, ein leerer Text leert es. |
| `/set-planned-scene-done <scene> [done]` | Markiert die Szene als geschrieben (`done` ist standardmäßig `true`) oder als nicht geschrieben. |
| `/move-planned-scene <scene> <to>` | Verschiebt die Szene an die Stelle `to` (1 ist die erste, die letzte Stelle, wenn die Zahl zu groß ist). |
| `/delete-planned-scene <scene>` | Löscht die Szene. |

Die Befehle, bei denen das Modell schreibt oder prüft, brauchen Zeit: `/generate-annotation` und die beiden Zusammenfassungen zeigen das Fenster **Stopp**,
`/next`, `/swipe` und `/regenerate` streamen in die Geschichte (siehe [Befehle, die Zeit brauchen](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Befehle, die mit Erweiterungen kommen

Du siehst diese in der Liste, solange die Erweiterung geladen ist; jede wird in der Dokumentation ihrer Erweiterung beschrieben.

| Befehl | Erweiterung | Was er tut |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | Deine gespeicherten Generierungs-Prompts oder der Text eines davon. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Erzeugt den Text eines Lore-Eintrags mit dem Lore des Lorebooks, verdichtet ihn (sofern `compact` nicht `false` ist) und gibt ihn zurück. Speichert nichts. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Verdichtet einen erzeugten Text zu einem dichten Lore-Eintragstext und gibt ihn zurück. Speichert nichts. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Die Kategorien erfundener Namen und Namen aus einer davon. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Die Listen echter Namen und Namen, die aus einer davon ausgewählt werden. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | Die Typen echter Orte, eine Suche nach Name, Typ und Land, die Orte um einen Punkt und die Entfernung zweier Orte. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Die Bögen und Kapitel der Gliederung. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Einen Bogen hinzufügen, ändern, verschieben und löschen (`<arc>` ist seine Nummer). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | Dasselbe für die Kapitel eines Bogens. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Die Generatoren des Reiters Bögen. Sie geben das Ergebnis zurück und fügen es nur mit `save` der Gliederung hinzu. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | Die Figuren des Buches und eine davon mit ihrer Notiz und ihren Beziehungen. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Eine Figur hinzufügen, ändern und löschen. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Die Beziehungen zwischen Figuren. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Die Seiten der Notizen des Buches (nicht die, die vor Assistenten verborgen sind), eine davon mit Text und Links, und eine Suche. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Eine Seite hinzufügen, ergänzen, ändern, bearbeiten und löschen; `note-text` ist für das Makro {%{`{{command::note-text::Title}}`}%} gedacht. |

Die Befehle des Lore Generators, des Name generators, des Name providers und des Place providers lesen nur: Die erzeugenden geben Text zurück und
überlassen es dir oder dem Agenten, der sie aufgerufen hat, ihn in einen Eintrag zu übernehmen. Die Befehle des Arc Outline und des
Character Trackers ändern außerdem die Gliederung und die Figuren des offenen Buches; nur ihre `delete`-Befehle sind destruktiv. Die Story Notes
ändern ebenfalls die Seiten des offenen Buches; dort sind `update-note` und `edit-note` ebenfalls destruktiv, da sie vorhandenen Text ersetzen.

<a id="limits"></a>

## Grenzen

- Die Befehlszeile befindet sich im Reiter Geschichte des Desktop-Layouts. Die [Telefonansicht](../phone.md) hat sie noch nicht.
- Ein Befehl läuft als du, in deiner Sitzung, und arbeitet mit dem Buch, das du geöffnet hast.
