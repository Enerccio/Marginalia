---
label: Zusammenfassungen
order: 60
---

# Zusammenfassungen

Ein Modell kann immer nur eine begrenzte Textmenge auf einmal lesen (seinen Kontext). Wächst ein Buch darüber hinaus, lässt Marginalia die ältesten Teile weg, und das Modell vergisst, was darin geschah. **Zusammenfassungen** halten diese Vorgeschichte in verdichteter Form im Prompt.

<a id="how-summaries-work"></a>

## So funktionieren Zusammenfassungen

Eine Zusammenfassung gehört zu einem Teil und deckt **diesen Teil und alle davor liegenden Teile** ab, bis zurück zum vorherigen Teil mit einer Zusammenfassung:

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
            [S1]            [S2]
└──── S1 ────┘└──── S2 ──────┘ vollständig gesendet ...
```

Hier deckt *S1* (am Teil #4) die Teile #1-#4 ab, und *S2* (am Teil #8) deckt die Teile #5-#8 ab.

Wenn der nächste Teil generiert wird:

- Alle Zusammenfassungen des aktiven Zweigs wandern in die Hauptvorlage, die älteste zuerst, im Block `{%{{{summaries}}}%}` (*Summary of story so far* in der Standardvorlage);
- die von Zusammenfassungen abgedeckten Teile werden **nicht mehr** gesendet;
- die Teile nach der neuesten Zusammenfassung (#9 und #10 oben) werden vollständig gesendet, so viele wie hineinpassen.

Zusammenfassungen werden nur erstellt, wenn du sie anforderst; Marginalia fasst nicht von selbst zusammen.

<a id="creating-a-summary"></a>

## Eine Zusammenfassung erstellen

1. Wähle den Teil, der einen Abschnitt der Geschichte abschließt, den du verdichten möchtest, zum Beispiel den letzten Teil eines Kapitels.
2. Wähle in seinem Menü (☰) **Zusammenfassung generieren**.
3. Der Zusammenfassungsdialog zeigt die Zusammenfassung, während das Modell sie schreibt (und bei Reasoning-Modellen auch dessen Überlegungen). **Stopp** unterbricht sie und verwirft die Zusammenfassung. Der Dialog blockiert die Geschichte nicht: **Schließen**, während das Modell schreibt, klappt ihn in das [Laufpanel](story-editor.md#the-panel-of-the-runs) ein, und die Zusammenfassung läuft weiter (der Teil kann in der Zwischenzeit nicht gelöscht oder zusammengeführt werden). Öffne sie dort erneut, um sie zu lesen.
4. Wenn sie fertig ist, schließe den Dialog mit **Beenden**.

![Eine Zusammenfassung wird generiert](../../images/summary-dialog.png)

Das Teilmenü enthält nun **Zusammenfassung anzeigen**, das die Zusammenfassung erneut öffnet, und **Zusammenfassung löschen**, das sie entfernt. Um eine Zusammenfassung zu ändern, bearbeite sie in der [Übersicht der Zusammenfassungen](#the-summaries-overview) oder lösche sie und generiere sie neu, gegebenenfalls mit einem anderen [Zusammenfassungs-Prompt](#the-summary-prompt).

Die Zusammenfassung wird vom Modell des Buchs geschrieben, mit dem [Protokoll](../protocols.md) des Buchs, und muss zusammen mit dem Text der abgedeckten Teile in das [Kontextlimit](../protocols.md#limits) passen. Um eine lange Strecke mit einem kleinen Modell zusammenzufassen, fasse in kleineren Schritten zusammen.
<a id="the-summaries-overview"></a>

## Die Übersicht der Zusammenfassungen

Die Schaltfläche **Zusammenfassungen** (das Listensymbol links neben dem Einstellungsmenü ⚙) in der unteren Leiste des Story-Editors öffnet die Übersicht aller Zusammenfassungen des aktiven Zweigs. Wenn Sie die Schaltfläche mit der Maus überfahren, sehen Sie, wie viele Tokens die verwendeten Zusammenfassungen im Prompt belegen.

![Die Schaltfläche Zusammenfassungen mit den Tokens der verwendeten Zusammenfassungen](../../images/summaries-menu-button.png)

Der Dialog listet die Zusammenfassungen auf, die die nächste Generierung senden wird, die älteste zuerst, und oben die Summe ihrer Tokens.

![Die Übersicht der Zusammenfassungen](../../images/summaries-dialog.png)

| Spalte | |
|---|---|
| **ID** | Datenbank-ID der Zusammenfassung. Ein `-` markiert eine Zusammenfassung, die durch eine [Metazusammenfassung](#meta-summaries) ersetzt wurde. |
| **Nachrichten-ID** | Datenbank-ID des Abschnitts, zu dem die Zusammenfassung gehört (dieselbe Nummer, die die Gliederung des Story-Editors als *DB ID* anzeigt). |
| **Reihenfolge** | Position dieses Abschnitts im Zweig, `1` ist der erste Abschnitt. |
| **Inhalt** | Die Überlegungen des Modells (eingeklappt, falls es ein Reasoning-Modell war) und der Text der Zusammenfassung, in einem scrollbaren Bereich, der für jede Zusammenfassung gleich hoch ist. |
| **Tokens** | Umfang des Zusammenfassungstexts. |
| **Auswählen** | Hakt die Zusammenfassung für eine [Metazusammenfassung](#creating-a-meta-summary) an. |
| Papierkorb-Symbol | Löscht die Zusammenfassung. |

Zusammenfassungen, die in einer Metazusammenfassung aufgehen, werden unter dieser angezeigt, siehe [unten](#meta-summaries). Sie haben kein Kontrollkästchen **Auswählen**, kein Papierkorb-Symbol und keinen Stift: Nur Zusammenfassungen, die verwendet werden, können geändert werden.

Die Menüleiste über der Tabelle enthält **Suchen und Ersetzen** (siehe [unten](#search-and-replace-in-the-summaries)), **Zusammenfassungen erstellen...** (siehe [unten](#create-summaries)), **Alle Zusammenfassungen neu zusammenfassen** (siehe [unten](#resummarize-all-summaries)), **Zusammenfassungen exportieren** und **Zusammenfassungen importieren** (siehe [Export und Import](#export-and-import-of-the-summaries)); [Erweiterungen](../extensions/index.md) können dem Menü weitere Werkzeuge hinzufügen.

<a id="editing-a-summary"></a>

### Eine Zusammenfassung bearbeiten

Klicken Sie auf den Stift rechts neben dem Inhalt einer Zusammenfassung. Der Text wird zu einem Textfeld derselben Größe. Klicken Sie auf das grüne Häkchen, um die Änderung zu speichern, oder auf das rote Papierkorb-Symbol, um sie zu verwerfen. Die Tokenanzahl wird nach dem Speichern neu gezählt.

![Eine Zusammenfassung bearbeiten](../../images/summary-edit.png)

Das Bearbeiten einer Zusammenfassung ändert nur, was das Modell erhält, sonst nichts: Die Zusammenfassung bleibt gültig. Bearbeitet werden kann nur der Text, nicht die Überlegungen.

<a id="search-and-replace-in-the-summaries"></a>

### Suchen und Ersetzen in den Zusammenfassungen

**Suchen und Ersetzen** in der Menüleiste der Übersicht ändert einen Namen oder eine Wendung nur in den Zusammenfassungen und lässt die Geschichte unverändert, etwa wenn eine Figur umbenannt wurde. Es ist die [Suche und Ersetzen der Geschichte](story-editor.md#search-and-replace) mit denselben Feldern und Optionen, nur gibt es kein **In Zusammenfassungen suchen** (Zusammenfassungen sind alles, was durchsucht wird). Die Tabelle listet jede Zusammenfassung mit einem Treffer und die Treffer darin auf.

Es durchsucht auch die Zusammenfassungen, die eine Metazusammenfassung vertritt, und die Zusammenfassung, die eine Metazusammenfassung ersetzt hat (sie werden unter der Metazusammenfassung angezeigt und kehren zurück, wenn sie [aufgelöst](#unwinding-a-meta-summary) wird): Ein Treffer darin wird mit der Metazusammenfassung aufgeführt, die sie enthält.

Der Text der zusammengefassten Zusammenfassungen gehört zu dem, was eine Metazusammenfassung gültig macht. Deshalb berechnet das Ersetzen den Prüfwert dieser Metazusammenfassungen neu, und sie bleiben gültig. **Alle Zusammenfassungen neu validieren** (standardmäßig aktiviert) tut dies für jede Zusammenfassung des Buchs, auch für jene, die das Ersetzen nicht berührt hat, und für jene, die schon vorher veraltet waren, siehe [die Suche in der Geschichte](story-editor.md#search-and-replace). Ist es ausgeschaltet, würde eine Metazusammenfassung in einem Zweig, der nicht durchsucht wird (**In allen Zweigen suchen** ausgeschaltet), veralten; das Fenster sagt dann, wie viele es sind, und fragt, ob fortgefahren werden soll.

<a id="create-summaries"></a>

### Zusammenfassungen erstellen

**Zusammenfassungen erstellen...** (auch im Einstellungsmenü ⚙ der Geschichte, unter **Analyse**, und nach einem [Import](story-editor.md#after-an-import) angeboten) schreibt die Zusammenfassungen einer langen Geschichte auf einmal, etwa für ein importiertes Buch: Es teilt die Geschichte in Blöcke und erstellt für jeden eine Zusammenfassung, nacheinander, die älteste zuerst. Eine Zusammenfassung deckt die Abschnitte bis zu dem Abschnitt nach der vorherigen Zusammenfassung ab, wie die Zusammenfassung eines einzelnen Abschnitts, sodass die Blöcke lückenlos aneinander anschließen.

- **Wo es beginnt.** Nur die Abschnitte **nach der neuesten Zusammenfassung** erhalten Zusammenfassungen (die ganze Geschichte, wenn es keine gibt). Vorhandene Zusammenfassungen werden ignoriert: Sie werden nicht erneut geschrieben, nicht verändert und bestimmen nicht, wo die neuen Blöcke enden. Um ältere erneut zu schreiben, verwenden Sie [Alle Zusammenfassungen neu zusammenfassen](#resummarize-all-summaries).
- **Die Geschichte unterteilen nach** legt fest, wo ein Block endet:
  - **Anzahl der Tokens**: nach dem Abschnitt, der den Block auf **Tokens je Zusammenfassung** bringt (der Schnitt wird auf das Ende dieses Abschnitts gerundet, ein Block hat mindestens so viele Tokens, so viele Abschnitte wie nötig).
  - **Kapitel**: vor einem Abschnitt, der ein Kapitel beginnt, sodass die Zusammenfassung auf dem Abschnitt davor liegt. Ein Kapitel ist ein Abschnitt, der mit einer Überschrift beginnt (die flachste Überschriftenebene, mit der ein Abschnitt beginnt: `#` in einem Buch, das durch den [Import](managing-books.md#importing-a-finished-manuscript) entstanden ist).
    **Mindestanzahl Tokens, bevor ein Kapitel eine Zusammenfassung beendet** verhindert, dass kurze Kapitel eine eigene Zusammenfassung bekommen: Hat der bisherige Block weniger Tokens, beendet das beginnende Kapitel ihn nicht, und es wird mit dem nächsten zusammengefasst.
  - **Anzahl der Abschnitte**: nach jeweils **Abschnitte je Zusammenfassung** Abschnitten.
- **Bis Abschnitt** beendet den Bereich bei einem Abschnitt (leer heißt bis zum Ende der Geschichte). **Den Rest am Ende ebenfalls zusammenfassen** gibt den Abschnitten nach dem letzten Schnitt, die weniger als einen Block ausmachen, eine Zusammenfassung (bei Kapiteln nur, wenn sie die Mindestanzahl Tokens haben); schalten Sie es aus, um die neuesten Abschnitte ohne Zusammenfassung zu lassen, wie sie es beim Schreiben sind.
- Ein Block wird nie länger, als das Modell lesen kann (80 % des Kontexts ohne die Antwort): Er endet vor dem Abschnitt, der ihn überschreiten würde. Ein einzelner Abschnitt, der länger ist, schlägt mit dem üblichen Fehler fehl.

![Zusammenfassungen erstellen: nach Kapiteln, mit einem Minimum von 500 Tokens](../../images/summaries-create.png)

Eine Zeile unter den Optionen sagt, wie viele Zusammenfassungen für wie viele Abschnitte und Tokens geschrieben werden. **Starten** öffnet das Fenster mit der Nummer der gerade geschriebenen Zusammenfassung und ihrem Text, während er eintrifft. Das Fenster schließt sich, wenn die letzte geschrieben ist.

![Das Fenster während die Zusammenfassungen geschrieben werden](../../images/summaries-create-progress.png)

Jede Zusammenfassung wird gespeichert, sobald sie geschrieben ist: **Stopp** oder eine fehlgeschlagene Anfrage lässt die davor geschriebenen Zusammenfassungen bestehen, und **Zusammenfassungen erstellen** macht beim nächsten Mal dort weiter (es beginnt nach der neuesten Zusammenfassung). Das Buch darf nicht durch einen [Agenten](../agents.md#the-book-is-locked) gesperrt sein; jede Zusammenfassung ist eine Anfrage an das Modell des Buchs.

<a id="resummarize-all-summaries"></a>

### Alle Zusammenfassungen neu zusammenfassen

Nachdem Sie den Zusammenfassungs-Prompt (oder den Prompt der Metazusammenfassung) geändert haben, folgen die vorhandenen Zusammenfassungen weiterhin dem alten. **Alle Zusammenfassungen neu zusammenfassen** schreibt alle mit dem Modell des Buchs erneut, damit sie alle gleich aussehen. Es fragt zuerst nach und sagt, wie viele Zusammenfassungen es schreiben wird: eine Anfrage pro Zusammenfassung.

![Die Rückfrage, bevor die Zusammenfassungen neu geschrieben werden](../../images/summaries-resummarize-confirm.png)

- Es beginnt mit der **ältesten** Zusammenfassung und geht bis zur neuesten weiter.
- Eine **Metazusammenfassung** wird nach den Zusammenfassungen geschrieben, die sie vertritt (die darunter angezeigten, die inneren Metazusammenfassungen vor den äußeren), aus deren **neuen** Texten, sodass eine Metazusammenfassung nie aus altem Text entsteht.
- Jede Zusammenfassung behält ihren Platz am Abschnitt. Der Prüfwert jeder Zusammenfassung wird aus der aktuellen Geschichte neu berechnet, sodass eine **veraltete** Zusammenfassung danach gültig ist (das Modell hat den aktuellen Text der Abschnitte gelesen).
- Das Fenster zeigt die Nummer der gerade geschriebenen Zusammenfassung und den Text, während er eintrifft. Die neuen Texte werden erst gespeichert, wenn die **letzte** fertig ist: Drücken Sie **Stopp** oder schlägt eine Anfrage fehl (der Prompt passt nicht in den Kontext, das Modell antwortet nicht), bleibt jede Zusammenfassung wie zuvor.

![Das Fenster während die Zusammenfassungen geschrieben werden](../../images/summaries-resummarize.png)

Die Zusammenfassungen anderer Zweige werden nicht verändert. Achten Sie darauf, dass das Modell nicht beschäftigt ist und dass das Buch nicht von einem Agenten bearbeitet wird. Die Änderungen, die Sie von Hand am Text vorgenommen haben, werden ersetzt; exportieren Sie die Zusammenfassungen zuerst, wenn Sie sie behalten möchten.

<a id="export-and-import-of-the-summaries"></a>

### Export und Import der Zusammenfassungen

**Zusammenfassungen exportieren** lädt die verwendeten Zusammenfassungen des aktiven Zweigs als eine Textdatei (`<book>-summaries.md`) herunter, die älteste zuerst, jede unter einer Zeile wie `<!-- summary part="..." order="3" -->`. Bearbeiten Sie die Texte in einem beliebigen Editor, behalten Sie die Kopfzeilen bei und bringen Sie die Datei mit **Zusammenfassungen importieren** zurück: Wählen Sie die Datei, die Tabelle sagt, was mit jeder Zusammenfassung geschehen würde, und es wird nichts geschrieben, bis Sie **Importieren** drücken.

| Status | |
|---|---|
| **Wird geändert** | Der Abschnitt, den die Kopfzeile nennt, hat eine verwendete Zusammenfassung, und ihr Text unterscheidet sich. |
| **Wird geändert (nach Position)** | Siehe unten. |
| **Unverändert** | Der Text ist der, den die Zusammenfassung schon hat. |
| **Abschnitt nicht gefunden** | Der Zweig hat keine verwendete Zusammenfassung für die Kopfzeile. |
| **Leerer Text** | Der Text unter der Kopfzeile ist leer; ein Import leert nie eine Zusammenfassung. |

![Das Importfenster mit einer bearbeiteten Datei](../../images/summaries-import.png)

Eine Kopfzeile benennt den Abschnitt über seine ID, sodass eine Datei die Zusammenfassungen zu dem Buch zurückbringt, aus dem sie stammt. Um die Zusammenfassungen in ein anderes Buch zu übertragen (eine Kopie hat Abschnitte mit anderen IDs), aktivieren Sie **Nach Position zuordnen**: Eine Zusammenfassung, deren Abschnitt nicht in diesem Zweig liegt, geht an die Zusammenfassung des Abschnitts an derselben Position (die `order` der Kopfzeile, die Wurzel ist 1). Prüfen Sie die Tabelle vorher: Eine Position passt nur, wenn die beiden Geschichten dieselbe Form haben.

Die Datei enthält die Zusammenfassungen, die die Generierung verwendet; eine Metazusammenfassung gehört dazu. Die Zusammenfassungen, die eine Metazusammenfassung vertritt, sind nicht enthalten und werden nie verändert: Ihr Text gehört zu dem, was die Metazusammenfassung gültig macht (siehe [Metazusammenfassungen](#meta-summaries)), eine Änderung würde sie veralten lassen. Ein Import ändert nur den Text und die Tokenanzahl einer Zusammenfassung, genau wie der Stift es tut, sodass keine Zusammenfassung dadurch veraltet. Solange ein Agent am Buch arbeitet, wird der Import abgelehnt.

<a id="deleting-a-summary"></a>

### Eine Zusammenfassung löschen

Das Papierkorb-Symbol einer Zusammenfassung tut dasselbe wie **Zusammenfassung löschen** im Abschnittsmenü. Bei einer Metazusammenfassung wird gefragt, was geschehen soll, siehe [Eine Metazusammenfassung auflösen](#unwinding-a-meta-summary).
<a id="meta-summaries"></a>

## Metazusammenfassungen

Zusammenfassungen bleiben dauerhaft im Prompt, daher füllt ein langes Buch den Kontext mit ihnen. Eine **Metazusammenfassung** verdichtet
mehrere Zusammenfassungen zu einer einzigen: Sie ist eine Zusammenfassung von Zusammenfassungen.

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
        [S1]        [S2]        [S3]
        └─────────── M ─────────────┘
```

Hier fasst *M* die Zusammenfassungen *S1*, *S2* und *S3* zusammen. Sie gehört zu dem Teil der neuesten zusammengefassten Zusammenfassung (oben #9, der *S3* enthielt)
und ersetzt diese Zusammenfassung an diesem Teil. Von da an gilt:

- Nur *M* wird in den `{%{{{summaries}}}%}`-Block geschickt, statt *S1*, *S2* und *S3*;
- *S1* und *S2* bleiben an ihren Teilen erhalten, werden aber nicht gesendet. Sie werden in der Übersicht unter *M* angezeigt;
- *S3*, die Zusammenfassung, die *M* ersetzt hat, bleibt in *M* erhalten und wird darunter mit einem Abzeichen **Ersetzt** angezeigt.

Die Teile, die von den zusammengefassten Zusammenfassungen abgedeckt werden, werden weiterhin nicht vollständig gesendet; eine Metazusammenfassung ändert nicht,
was die Zusammenfassungen abdecken, sondern nur, wie lang der Text dafür ist.

<a id="creating-a-meta-summary"></a>

### Eine Metazusammenfassung erstellen

1. Öffne die [Übersicht der Zusammenfassungen](#the-summaries-overview).
2. Setze den Haken im Kästchen **Auswählen** bei den Zusammenfassungen, die du zusammenführen möchtest. Mindestens zwei. Du setzt Haken nur bei den Zusammenfassungen, die in Gebrauch sind (denen auf der obersten Ebene des Rasters).
3. Klicke auf **Metazusammenfassung erstellen**.

Marginalia führt alles vom neuesten bis zum ältesten markierten Eintrag zusammen, einschließlich der Zusammenfassungen dazwischen, die nicht markiert waren. Der Dialog für Metazusammenfassungen funktioniert wie der Zusammenfassungsdialog: Er zeigt den Text, während das Modell ihn schreibt, und **Stopp** bricht ab und ändert nichts. Wenn er endet, schließe den Dialog mit **Beenden**, und die Übersicht zeigt das Ergebnis.

![Markierte Zusammenfassungen und die Metazusammenfassung, die erzeugt wird](../../images/meta-summary-create.png)

Eine Metazusammenfassung kann auch Metazusammenfassungen zusammenführen: Markiere sie wie jede andere Zusammenfassung. Die zusammengeführten werden unter der neuen angezeigt, mit ihren eigenen zusammengeführten Zusammenfassungen darunter.

Die zusammenzuführenden Zusammenfassungen und die Anfrage an das Modell müssen in das [Kontextlimit](../protocols.md#limits) passen, daher führe bei einem kleinen Modell nur einige wenige Zusammenfassungen auf einmal zusammen.

<a id="unwinding-a-meta-summary"></a>

### Eine Metazusammenfassung auflösen

Das Löschen einer Metazusammenfassung (das Papierkorb-Symbol in der Übersicht oder **Zusammenfassung löschen** im Menü des Teils) fragt nach:

| Schaltfläche | |
|---|---|
| **Zurückspulen** | Entfernt die Metazusammenfassung und stellt die Zusammenfassung wieder her, die sie am Teil ersetzt hat. Zusammenfassungen, die in eine Metazusammenfassung aus Metazusammenfassungen zusammengeführt wurden, kommen Ebene für Ebene zurück, so wie sie erstellt wurden. |
| **Löschen** | Entfernt die Metazusammenfassung und lässt den Teil ohne Zusammenfassung. Die Zusammenfassungen, die sie zusammengeführt hat, sind wieder in Gebrauch (die ersetzte Zusammenfassung ist aber weg). |
| **Abbrechen** | Tut nichts. |

Nach dem Zurückspulen sind die zusammengeführten Zusammenfassungen wieder in Gebrauch, und die Übersicht zeigt sie auf der obersten Ebene.

![Löschen einer Metazusammenfassung](../../images/meta-summary-delete.png)

<a id="the-meta-summary-prompt"></a>

### Der Prompt für Metazusammenfassungen

Die Anfrage für eine Metazusammenfassung ist der **Standard-Metazusammenfassungsprompt** des Buches (auf dem Reiter [Prompts](prompts.md) oder der Standard aus *Einstellungen → Vorlagen*). Er erhält zwei Variablen:

| Variable | |
|---|---|
| `{%{{{summaryBlocks}}}%}` | Die zusammenzuführenden Zusammenfassungen, die älteste zuerst, durch eine leere Zeile getrennt. |
| `{%{{{backgroundLore}}}%}` | Die Einträge des Lorebuchs, die aktiv waren, als der Teil der Metazusammenfassung geschrieben wurde. |

Der eingebaute Prompt bittet darum, die Ereignisse zu kürzeren Einträgen zu verketten – etwa halb so groß –, ohne Namen, Gegenstände, Vereinbarungen oder offene Handlungsstränge zu verlieren und ohne etwas Neues hinzuzufügen.

<a id="when-summaries-become-outdated"></a>

## Wann Zusammenfassungen veraltet sind

Eine Zusammenfassung ist nur für den genauen Text gültig, aus dem sie erstellt wurde. Wenn du einen Teil, der von einer Zusammenfassung abgedeckt wird, [bearbeitest](story-editor.md#editing-the-text) oder [löschst](story-editor.md#deleting-a-part), passt die Zusammenfassung nicht mehr.

Veraltete Zusammenfassungen werden nicht an das Modell gesendet. Marginalia prüft dies, wenn es den nächsten Teil erzeugt, und fragt: *Die Zusammenfassungen der Nachrichten mit IDs: [...] sind veraltet ... Erzeugung fortsetzen und sie löschen?* Antworte mit **Ja**, um sie zu löschen und zu erzeugen; die Teile, die sie abgedeckt haben, werden dann vollständig erneut gesendet (soweit sie passen), bis du sie erneut zusammenfasst. Antworte mit **Nein**, um abzubrechen: Nichts wird gelöscht, und du kannst dich zuerst um die Zusammenfassungen kümmern.

In der [Übersicht der Zusammenfassungen](#the-summaries-overview) hat eine veraltete Zusammenfassung einen **roten Hintergrund** und eine Markierung **Veraltet**, und ihre Token werden nicht in der Summe oben mitgezählt. Wenn sie trotzdem noch das Richtige sagt (du hast zum Beispiel einen Tippfehler korrigiert), drücke die Schaltfläche **Häkchen** in ihrer Zeile, um sie **neu zu validieren**, oder wähle **Alle Zusammenfassungen neu validieren** in der Menüleiste, um alle veralteten auf einmal neu zu validieren. Andernfalls lösche sie oder erzeuge sie neu. Eine Metazusammenfassung wird wie jede Zusammenfassung geprüft; die Zusammenfassungen, die sie zusammengeführt hat, sind Teil von ihr und werden nie für sich allein veraltet. Das Neuvalidieren wird verweigert, solange ein Agent am Buch arbeitet.

Wenn du die [Bearbeitung eines Teils](story-editor.md#editing-the-text) speicherst, die Zusammenfassungen veralten lassen würde, warnt dich Marginalia vorher und lässt dich zum Bearbeiten zurückkehren, trotzdem speichern oder speichern und die Zusammenfassungen neu validieren.

![Die Frage, wenn ein Speichern Zusammenfassungen veralten lässt](../../images/summaries-save-warning.png)

Nach **Trotzdem speichern** zeigt die Übersicht der Zusammenfassungen die Zusammenfassung als veraltet (rot, mit der Markierung **Veraltet** und nicht in der Summe oben gezählt):

![Eine veraltete Zusammenfassung in der Übersicht](../../images/summaries-outdated.png)

Eine [Suchen-und-Ersetzen-Funktion](story-editor.md#search-and-replace) in der Geschichte hält die Zusammenfassungen, die sie abdecken, gültig und warnt vor denen, die veralten. Das [Suchen und Ersetzen der Zusammenfassungen](#search-and-replace-in-the-summaries) hält die Metazusammenfassungen gültig.

Eine Metazusammenfassung ist veraltet, wenn ein Teil, den sie abdeckt, bearbeitet oder gelöscht wird, aber auch wenn eine Zusammenfassung, die sie zusammengeführt hat, gelöscht wird. (Deshalb können die zusammengeführten Zusammenfassungen nicht einzeln bearbeitet werden: Die Änderung würde die Metazusammenfassung veralten lassen. Das [Suchen und Ersetzen](#search-and-replace-in-the-summaries) berechnet den Prüfwert der Metazusammenfassung neu.) Eine veraltete Metazusammenfassung zu entfernen ist dasselbe wie das Zurückspulen: Die Zusammenfassung, die sie ersetzt hat, kommt zurück und wird ebenfalls geprüft. Ist auch diese veraltet, wird sie auf dieselbe Weise entfernt.

Wenn du an einem Teil mit Zusammenfassung [verzweigst](branches-and-story-tree.md#branching-from-an-earlier-part), erhält der neue Zweig eine Kopie der Zusammenfassung. Eine kopierte Metazusammenfassung behält, was sie ersetzt hat, sodass sie im neuen Zweig zurückgespult werden kann.

<a id="the-summary-prompt"></a>

## Der Zusammenfassungsprompt

Die Anfrage für eine Zusammenfassung ist der **Standard-Zusammenfassungsprompt** des Buches (auf dem Reiter [Prompts](prompts.md) oder der Standard aus *Einstellungen → Vorlagen*). Er erhält zwei Variablen:

| Variable | |
|---|---|
| `{%{{{text}}}%}` | Der Text der zusammenzufassenden Teile, der älteste zuerst. |
| `{%{{{backgroundLore}}}%}` | Die Einträge des Lorebuchs, die aktiv waren, als der zusammengefasste Teil geschrieben wurde. |

Der eingebaute Prompt verlangt ein kompaktes Ereignisprotokoll, gruppiert nach Ort: unter einer Kopfzeile `[LOCATION / SCENE: Name]`, ein Ereignis pro Zeile als *wer tut was -> was daraus folgt* (die Absicht nur, wenn der Text sie zeigt), ohne Titel, Nummern oder IDs für die Ereignisse und ohne die Atmosphäre. Er ist für das Modell gedacht, nicht für einen Leser, und etwa ein Viertel so groß wie der Text, den er ersetzt. Der Prompt für Metazusammenfassungen erwartet dasselbe Format und führt benachbarte Zeilen zu kausalen Ketten zusammen. Ersetze ihn, wenn du ein anderes Format oder eine andere Länge bevorzugst; eine Zusammenfassung, die vor der Änderung des Prompts erstellt wurde, behält ihre alte Form, bis du sie neu erstellst.

<a id="tips"></a>

## Tipps

- Fasse an natürlichen Einschnitten zusammen: am Ende eines Kapitels oder einer Szene.
- Wenn die Zusammenfassungen zu viel vom Kontext beanspruchen (der Tooltip der Schaltfläche **Zusammenfassungen** zeigt, wie viel), führe die ältesten zu einer Metazusammenfassung zusammen. Wiederhole dies später mit der Metazusammenfassung und den Zusammenfassungen danach.
- Zusammenfassungen bleiben dauerhaft im Prompt. Bei vielen von ihnen kann der Prompt allein den Kontext füllen, und die Erzeugung bricht mit *Das Kontextlimit reicht nicht aus.* ab. Kürze sie dann (ein knapperer Zusammenfassungsprompt), führe sie zu [Metazusammenfassungen](#meta-summaries) zusammen oder erhöhe das Kontextlimit.
- Behalte Fakten, die niemals vergessen werden dürfen (Namen, Beziehungen, Regeln der Welt), im [Lorebuch](../lorebooks/index.md), statt dich auf Zusammenfassungen zu verlassen.
- Prüfe mit **Prompt anzeigen** am nächsten Teil, was das Modell erhalten hat.
