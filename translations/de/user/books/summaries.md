---
label: Zusammenfassungen
order: 60
---

# Zusammenfassungen

Ein Modell kann immer nur eine begrenzte Textmenge auf einmal lesen (seinen Kontext). Wächst ein Buch darüber hinaus, lässt Marginalia die ältesten Teile weg, und das Modell vergisst, was darin passiert ist. **Zusammenfassungen** halten diese Geschichte in verdichteter Form im Prompt.

<a id="how-summaries-work"></a>

## So funktionieren Zusammenfassungen

Eine Zusammenfassung gehört zu einem Teil und deckt **diesen Teil und alle Teile davor** ab, zurück bis zum vorherigen Teil mit einer Zusammenfassung:

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
            [S1]            [S2]
└──── S1 ────┘└──── S2 ──────┘ sent in full ...
```

Hier deckt *S1* (am Teil #4) die Teile #1-#4 ab, und *S2* (am Teil #8) die Teile #5-#8.

Wenn der nächste Teil generiert wird:

- Alle Zusammenfassungen des aktiven Zweigs kommen in die Mastervorlage, die älteste zuerst, im Block `{%{{{summaries}}}%}` (*Summary of story so far* in der Standardvorlage);
- die von Zusammenfassungen abgedeckten Teile werden **nicht** mehr gesendet;
- die Teile nach der neuesten Zusammenfassung (#9 und #10 oben) werden vollständig gesendet, so viele, wie hineinpassen.

Zusammenfassungen werden nur erstellt, wenn du sie anforderst; Marginalia fasst nicht von selbst zusammen.

<a id="creating-a-summary"></a>

## Eine Zusammenfassung erstellen

1. Wähle den Teil, der einen Abschnitt der Geschichte abschließt, den du verdichten willst, zum Beispiel den letzten Teil eines Kapitels.
2. Wähle in dessen Menü (☰) **Zusammenfassung generieren**.
3. Der Dialog für Zusammenfassungen zeigt die Zusammenfassung, während das Modell sie schreibt (und bei Reasoning-Modellen auch dessen Überlegungen). **Stopp** unterbricht sie und verwirft die Zusammenfassung. Der Dialog blockiert die Geschichte nicht: **Schließen** während das Modell schreibt, klappt ihn in das [Panel der Läufe](story-editor.md#the-panel-of-the-runs) ein, und die Zusammenfassung läuft weiter (der Teil kann währenddessen nicht gelöscht oder zusammengeführt werden); öffne sie dort erneut, um sie zu lesen.
4. Ist sie fertig, schließe den Dialog mit **Beenden**.

![Eine Zusammenfassung wird generiert](../../images/summary-dialog.png)

Das Teil-Menü enthält nun **Zusammenfassung anzeigen**, das die Zusammenfassung erneut öffnet, und **Zusammenfassung löschen**, das sie entfernt. Um eine Zusammenfassung zu ändern, bearbeite sie in der [Übersicht der Zusammenfassungen](#the-summaries-overview) oder lösche sie und generiere sie neu, gegebenenfalls mit einem anderen [Prompt für Zusammenfassungen](#the-summary-prompt).

Die Zusammenfassung wird vom Modell des Buches geschrieben, mit dem [Protokoll](../protocols.md) des Buches, und muss zusammen mit dem Text der abgedeckten Teile in das [Kontextlimit](../protocols.md#limits) passen. Um eine lange Strecke mit einem kleinen Modell zusammenzufassen, fasse in kleineren Schritten zusammen.
<a id="the-summaries-overview"></a>

## Die Übersicht der Zusammenfassungen

Die Schaltfläche **Zusammenfassungen** (das Listensymbol links neben dem Einstellungsmenü ⚙) in der unteren Leiste des Story-Editors öffnet die Übersicht aller Zusammenfassungen des aktiven Zweigs. Wenn Sie die Schaltfläche mit der Maus überfahren, sehen Sie, wie viele Tokens die verwendeten Zusammenfassungen im Prompt belegen.

![Die Schaltfläche Zusammenfassungen mit den Tokens der verwendeten Zusammenfassungen](../../images/summaries-menu-button.png)

Der Dialog listet die Zusammenfassungen auf, die die nächste Generierung sendet, die älteste zuerst. Oben steht die Summe ihrer Tokens.

![Die Übersicht der Zusammenfassungen](../../images/summaries-dialog.png)

| Spalte | |
|---|---|
| **ID** | Datenbank-ID der Zusammenfassung. Ein `-` markiert eine Zusammenfassung, die durch eine Meta-Zusammenfassung [ersetzt](#meta-summaries) wurde. |
| **Nachrichten-ID** | Datenbank-ID des Abschnitts, zu dem die Zusammenfassung gehört (dieselbe Zahl, die die Gliederung des Story-Editors als *DB ID* anzeigt). |
| **Reihenfolge** | Position dieses Abschnitts im Zweig, `1` ist der erste Abschnitt. |
| **Inhalt** | Die Überlegungen des Modells (eingeklappt, falls es ein Reasoning-Modell war) und der Text der Zusammenfassung, in einem scrollbaren Bereich mit für jede Zusammenfassung gleicher Höhe. |
| **Tokens** | Umfang des Zusammenfassungstexts. |
| **Auswählen** | Setzt den Haken für eine Zusammenfassung, die in eine [Meta-Zusammenfassung](#creating-a-meta-summary) aufgenommen werden soll. |
| Papierkorb-Symbol | Löscht die Zusammenfassung. |

Zusammenfassungen, die in einer Meta-Zusammenfassung aufgehen, werden unter dieser angezeigt, siehe [unten](#meta-summaries). Sie haben kein Kästchen **Auswählen**, kein Papierkorb-Symbol und keinen Stift: Nur Zusammenfassungen, die verwendet werden, lassen sich ändern.

Die Menüleiste über dem Raster bietet **Suchen und Ersetzen** (siehe [unten](#search-and-replace-in-the-summaries)), **Zusammenfassungen erstellen...** (siehe [unten](#create-summaries)), **Alle Zusammenfassungen neu zusammenfassen** (siehe [unten](#resummarize-all-summaries)), **Zusammenfassungen exportieren** und **Zusammenfassungen importieren** (siehe [Export und Import](#export-and-import-of-the-summaries)); [Erweiterungen](../extensions/index.md) können der Leiste weitere Werkzeuge hinzufügen.

<a id="editing-a-summary"></a>

### Eine Zusammenfassung bearbeiten

Klicken Sie auf den Stift rechts neben dem Inhalt einer Zusammenfassung. Der Text wird zu einem Textfeld derselben Größe. Klicken Sie auf das grüne Häkchen, um die Änderung zu speichern, oder auf das rote Papierkorb-Symbol, um sie zu verwerfen. Die Tokenzahl wird nach dem Speichern neu gezählt.

![Eine Zusammenfassung bearbeiten](../../images/summary-edit.png)

Das Bearbeiten einer Zusammenfassung ändert nur, was das Modell erhält, sonst nichts: Die Zusammenfassung bleibt gültig. Nur der Text lässt sich bearbeiten, nicht die Überlegungen.

<a id="search-and-replace-in-the-summaries"></a>

### Suchen und Ersetzen in den Zusammenfassungen

**Suchen und Ersetzen** in der Menüleiste der Übersicht ändert einen Namen oder eine Wendung nur in den Zusammenfassungen, etwa wenn eine Figur umbenannt wurde, und lässt die Geschichte unverändert. Es ist die [Suchen-und-Ersetzen-Funktion der Geschichte](story-editor.md#search-and-replace) mit denselben Feldern und Optionen, nur dass es kein **In Zusammenfassungen suchen** gibt (die Zusammenfassungen sind alles, was durchsucht wird). Die Tabelle listet jede Zusammenfassung mit einem Treffer und die Treffer darin auf.

Es durchsucht auch die Zusammenfassungen, die eine Meta-Zusammenfassung vertritt, und die Zusammenfassung, die durch eine Meta-Zusammenfassung ersetzt wurde (sie werden unter der Meta-Zusammenfassung angezeigt und kehren zurück, wenn diese [aufgelöst](#unwinding-a-meta-summary) wird): Ein Treffer darin wird mit der Meta-Zusammenfassung aufgeführt, die sie enthält.

Der Text der aufgenommenen Zusammenfassungen ist Teil dessen, was eine Meta-Zusammenfassung gültig macht. Deshalb berechnet das Ersetzen den Prüfwert dieser Meta-Zusammenfassungen neu, sodass sie gültig bleiben. **Alle Zusammenfassungen neu validieren** (standardmäßig aktiviert) tut dies für jede Zusammenfassung des Buchs, auch für jene, die das Ersetzen nicht berührt hat, und für jene, die schon vorher veraltet waren; siehe [die Suche in der Geschichte](story-editor.md#search-and-replace). Ist die Option ausgeschaltet, würde eine Meta-Zusammenfassung auf einem Zweig, der nicht durchsucht wird (**In allen Zweigen suchen** ausgeschaltet), veralten. Das Fenster zeigt dann an, wie viele es betrifft, und fragt, ob fortgefahren werden soll.

<a id="create-summaries"></a>

### Zusammenfassungen erstellen

**Zusammenfassungen erstellen...** (auch im Einstellungsmenü ⚙ der Geschichte unter **Analyse** zu finden und nach einem [Import](story-editor.md#after-an-import) angeboten) schreibt die Zusammenfassungen einer langen Geschichte in einem Zug, etwa für ein importiertes Buch: Es teilt die Geschichte in Blöcke und erstellt für jeden eine Zusammenfassung, nacheinander, die älteste zuerst. Eine Zusammenfassung deckt die Abschnitte bis zu dem Abschnitt nach der vorherigen Zusammenfassung ab, wie die Zusammenfassung eines einzelnen Abschnitts, sodass die Blöcke lückenlos aufeinander folgen.

- **Wo es beginnt.** Nur die Abschnitte **nach der neuesten Zusammenfassung** erhalten Zusammenfassungen (wenn keine existiert, die ganze Geschichte). Die vorhandenen Zusammenfassungen werden ignoriert: Sie werden nicht erneut geschrieben, nicht verändert und bestimmen auch nicht, wo die neuen Blöcke enden. Um ältere erneut zu schreiben, verwenden Sie [Alle Zusammenfassungen neu zusammenfassen](#resummarize-all-summaries).
- **Die Geschichte unterteilen nach** legt fest, wo ein Block endet:
  - **Anzahl der Tokens**: nach dem Abschnitt, der den Block auf **Tokens je Zusammenfassung** bringt (der Schnitt wird auf das Ende dieses Abschnitts gerundet; ein Block hat mindestens so viele Tokens, wie er an Abschnitten braucht).
  - **Kapitel**: vor einem Abschnitt, der ein Kapitel beginnt, sodass die Zusammenfassung auf dem Abschnitt davor endet. Ein Kapitel ist ein Abschnitt, der mit einer Überschrift beginnt (die flachste Überschriftenebene, mit der ein Abschnitt beginnt: `#` in einem Buch, das durch den [Import](managing-books.md#importing-a-finished-manuscript) entstanden ist).
    **Mindestanzahl Tokens, bevor ein Kapitel eine Zusammenfassung beendet** verhindert, dass kurze Kapitel eine eigene Zusammenfassung bekommen: Hat der bisherige Block weniger Tokens, beendet das beginnende Kapitel ihn nicht, sondern es wird mit dem nächsten zusammengefasst.
  - **Anzahl der Abschnitte**: nach jeweils **Abschnitte je Zusammenfassung** Abschnitten.
- **Bis Abschnitt** beendet den Bereich bei einem Abschnitt (leer bedeutet das Ende der Geschichte). **Den Rest am Ende ebenfalls zusammenfassen** erstellt eine Zusammenfassung für die Abschnitte nach dem letzten Schnitt, die weniger als einen Block ausmachen (bei Kapiteln nur, wenn sie die Mindestanzahl Tokens haben). Schalten Sie die Option aus, bleiben die neuesten Abschnitte ohne Zusammenfassung, so wie sie beim Schreiben sind.
- Ein Block wird nie länger, als das Modell lesen kann (80 % des Kontexts ohne die Antwort): Er endet vor dem Abschnitt, der ihn darüber hinaus bringen würde. Ein einzelner Abschnitt, der länger ist, schlägt mit dem üblichen Fehler fehl.

![Zusammenfassungen erstellen: nach Kapiteln, mit einem Minimum von 500 Tokens](../../images/summaries-create.png)

Eine Zeile unter den Optionen zeigt, wie viele Zusammenfassungen geschrieben werden, für wie viele Abschnitte und Tokens. **Starten** öffnet das Fenster mit der Nummer der gerade geschriebenen Zusammenfassung und ihrem Text, während er eintrifft. Das Fenster schließt sich, wenn die letzte geschrieben ist.

![Das Fenster, während die Zusammenfassungen geschrieben werden](../../images/summaries-create-progress.png)

Jede Zusammenfassung wird gespeichert, sobald sie geschrieben ist. **Stopp** oder eine fehlgeschlagene Anfrage lässt die davor geschriebenen Zusammenfassungen bestehen, und **Zusammenfassungen erstellen** macht beim nächsten Mal dort weiter (es beginnt nach der neuesten Zusammenfassung). Das Buch darf nicht von einem [Agenten](../agents.md#the-book-is-locked) gesperrt sein; jede Zusammenfassung ist eine Anfrage an das Modell des Buchs.

<a id="resummarize-all-summaries"></a>

### Alle Zusammenfassungen neu zusammenfassen

Nachdem Sie den Prompt für Zusammenfassungen (oder den für Meta-Zusammenfassungen) geändert haben, folgen die vorhandenen Zusammenfassungen weiterhin dem alten. **Alle Zusammenfassungen neu zusammenfassen** schreibt alle mit dem Modell des Buchs neu, damit sie alle gleich aussehen. Vorher wird nachgefragt, und es wird angegeben, wie viele Zusammenfassungen geschrieben werden: eine Anfrage pro Zusammenfassung.

![Die Nachfrage, bevor die Zusammenfassungen neu geschrieben werden](../../images/summaries-resummarize-confirm.png)

- Es beginnt mit der **ältesten** Zusammenfassung und geht bis zur neuesten weiter.
- Eine **Meta-Zusammenfassung** wird nach den Zusammenfassungen geschrieben, die sie vertritt (den darunter angezeigten, die inneren Meta-Zusammenfassungen vor den äußeren), aus deren **neuen** Texten; eine Meta-Zusammenfassung besteht also nie aus altem Text.
- Jede Zusammenfassung behält ihren Platz an ihrem Abschnitt. Der Prüfwert jeder Zusammenfassung wird aus der Geschichte, wie sie jetzt ist, neu berechnet; eine **veraltete** Zusammenfassung ist danach also gültig (das Modell hat den aktuellen Text der Abschnitte gelesen).
- Das Fenster zeigt die Nummer der gerade geschriebenen Zusammenfassung und ihren Text, während er eintrifft. Die neuen Texte werden erst gespeichert, wenn die **letzte** fertig ist: Drücken Sie **Stopp**, oder schlägt eine Anfrage fehl (der Prompt passt nicht in den Kontext, das Modell antwortet nicht), bleibt jede Zusammenfassung so, wie sie war.

![Das Fenster, während die Zusammenfassungen geschrieben werden](../../images/summaries-resummarize.png)

Die Zusammenfassungen anderer Zweige bleiben unverändert. Achten Sie darauf, dass das Modell nicht beschäftigt ist und dass das Buch nicht von einem Agenten bearbeitet wird. Die Änderungen, die Sie von Hand am Text vorgenommen haben, werden ersetzt; exportieren Sie die Zusammenfassungen vorher, wenn Sie sie behalten wollen.

<a id="export-and-import-of-the-summaries"></a>

### Export und Import der Zusammenfassungen

**Zusammenfassungen exportieren** lädt die verwendeten Zusammenfassungen des aktiven Zweigs als eine Textdatei herunter (`<book>-summaries.md`), die älteste zuerst, jeweils unter einer Zeile wie `<!-- summary part="..." order="3" -->`. Bearbeiten Sie die Texte in einem beliebigen Editor, behalten Sie die Kopfzeilen bei, und spielen Sie die Datei mit **Zusammenfassungen importieren** wieder ein: Wählen Sie die Datei, die Tabelle zeigt, was mit jeder Zusammenfassung geschehen würde, und nichts wird geschrieben, bis Sie **Importieren** drücken.

| Status | |
|---|---|
| **Wird geändert** | Der durch die Kopfzeile benannte Abschnitt hat eine verwendete Zusammenfassung, und deren Text weicht ab. |
| **Wird geändert (nach Position)** | Siehe unten. |
| **Unverändert** | Der Text ist derselbe wie der der Zusammenfassung. |
| **Abschnitt nicht gefunden** | Der Zweig hat für die Kopfzeile keine verwendete Zusammenfassung. |
| **Leerer Text** | Der Text unter der Kopfzeile ist leer; ein Import leert nie eine Zusammenfassung. |

![Das Importfenster mit einer bearbeiteten Datei](../../images/summaries-import.png)

Eine Kopfzeile benennt den Abschnitt über seine ID, sodass eine Datei die Zusammenfassungen zu dem Buch zurückbringt, aus dem sie stammt. Um die Zusammenfassungen in ein anderes Buch zu übertragen (eine Kopie hat Abschnitte mit anderen IDs), aktivieren Sie **Nach Position zuordnen**: Eine Zusammenfassung, deren Abschnitt nicht in diesem Zweig liegt, geht an die Zusammenfassung des Abschnitts an derselben Position (die `order` der Kopfzeile, die Wurzel ist 1). Prüfen Sie zuerst die Tabelle: Eine Position passt nur, wenn die beiden Geschichten dieselbe Form haben.

Die Datei enthält die Zusammenfassungen, die die Generierung verwendet; eine Meta-Zusammenfassung ist eine davon. Die Zusammenfassungen, die eine Meta-Zusammenfassung vertritt, sind nicht darin enthalten und werden nie verändert: Ihr Text ist Teil dessen, was die Meta-Zusammenfassung gültig macht (siehe [Meta-Zusammenfassungen](#meta-summaries)), eine Änderung würde sie also veralten lassen. Ein Import ändert nur den Text und die Tokenzahl einer Zusammenfassung, genauso wie der Stift es tut, sodass dadurch keine Zusammenfassung veraltet. Während ein Agent am Buch arbeitet, wird der Import abgelehnt.

<a id="deleting-a-summary"></a>

### Eine Zusammenfassung löschen

Das Papierkorb-Symbol einer Zusammenfassung tut dasselbe wie **Zusammenfassung löschen** im Menü des Abschnitts. Bei einer Meta-Zusammenfassung fragt es, was geschehen soll; siehe [Eine Meta-Zusammenfassung auflösen](#unwinding-a-meta-summary).
<a id="meta-summaries"></a>

## Metazusammenfassungen

Zusammenfassungen bleiben dauerhaft im Prompt, daher füllt ein langes Buch den Kontext mit ihnen. Eine **Metazusammenfassung** verdichtet
mehrere Zusammenfassungen zu einer einzigen: Sie ist eine Zusammenfassung von Zusammenfassungen.

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
        [S1]        [S2]        [S3]
        └─────────── M ─────────────┘
```

Hier fasst *M* die Zusammenfassungen *S1*, *S2* und *S3* zusammen. Sie gehört zu dem Teil der neuesten zusammengeführten Zusammenfassung (oben #9, die *S3* enthielt)
und ersetzt diese Zusammenfassung an diesem Teil. Von da an gilt:

- Nur *M* wird im `{%{{{summaries}}}%}`-Block gesendet, statt *S1*, *S2* und *S3*;
- *S1* und *S2* bleiben an ihren Teilen erhalten, werden aber nicht gesendet. Sie werden in der Übersicht unter *M* angezeigt;
- *S3*, die Zusammenfassung, die *M* ersetzt hat, bleibt in *M* erhalten und wird darunter mit dem Abzeichen **Ersetzt** angezeigt.

Die Teile, die von den zusammengeführten Zusammenfassungen abgedeckt werden, werden weiterhin nicht vollständig gesendet; eine Metazusammenfassung ändert nicht, was die Zusammenfassungen abdecken, sondern nur, wie lang der Text dafür ist.

<a id="creating-a-meta-summary"></a>

### Eine Metazusammenfassung erstellen

1. Öffne die [Übersicht der Zusammenfassungen](#the-summaries-overview).
2. Markiere das Kästchen **Auswählen** bei den Zusammenfassungen, die du zusammenführen willst. Mindestens zwei. Markiere nur die Zusammenfassungen, die verwendet werden (die auf der obersten Ebene des Rasters).
3. Klicke auf **Metazusammenfassung erstellen**.

Marginalia führt alles von der neuesten bis zur ältesten markierten Zusammenfassung zusammen, einschließlich der Zusammenfassungen dazwischen, die nicht markiert waren. Der Dialog für Metazusammenfassungen funktioniert wie der Zusammenfassungsdialog: Er zeigt den Text, während das Modell ihn schreibt, und **Stopp** bricht ab und ändert nichts. Wenn er endet, schließt du den Dialog mit **Beenden**, und die Übersicht zeigt das Ergebnis.

![Markierte Zusammenfassungen und die Metazusammenfassung, während sie erzeugt wird](../../images/meta-summary-create.png)

Eine Metazusammenfassung kann auch Metazusammenfassungen zusammenführen: Markiere sie wie jede andere Zusammenfassung. Die zusammengeführten werden unter der neuen angezeigt, mit ihren eigenen zusammengeführten Zusammenfassungen darunter.

Die zusammenzuführenden Zusammenfassungen und die Anfrage an das Modell müssen in das [Kontextlimit](../protocols.md#limits) passen. Führe daher bei einem kleinen Modell nur einige wenige Zusammenfassungen auf einmal zusammen.

<a id="unwinding-a-meta-summary"></a>

### Eine Metazusammenfassung auflösen

Beim Löschen einer Metazusammenfassung (das Papierkorb-Symbol in der Übersicht oder **Zusammenfassung löschen** im Menü des Teils) erscheint eine Abfrage:

| Schaltfläche | |
|---|---|
| **Zurückspulen** | Entfernt die Metazusammenfassung und stellt die Zusammenfassung, die sie ersetzt hat, am Teil wieder her. Zusammenfassungen, die in eine Metazusammenfassung aus Metazusammenfassungen eingegangen sind, kommen Ebene für Ebene zurück, so wie sie entstanden sind. |
| **Löschen** | Entfernt die Metazusammenfassung und lässt den Teil ohne Zusammenfassung. Die Zusammenfassungen, die sie zusammengeführt hat, sind wieder in Verwendung (die Zusammenfassung, die sie ersetzt hat, ist aber weg). |
| **Abbrechen** | Tut nichts. |

Nach dem Zurückspulen sind die zusammengeführten Zusammenfassungen wieder in Verwendung, und die Übersicht zeigt sie auf der obersten Ebene.

![Löschen einer Metazusammenfassung](../../images/meta-summary-delete.png)

<a id="the-meta-summary-prompt"></a>

### Der Prompt für Metazusammenfassungen

Die Anfrage für eine Metazusammenfassung ist der **Standard-Metazusammenfassungsprompt** des Buches (auf der Registerkarte [Prompts](prompts.md) oder der Standardwert aus *Einstellungen → Vorlagen*). Er erhält zwei Variablen:

| Variable | |
|---|---|
| `{%{{{summaryBlocks}}}%}` | Die zusammenzuführenden Zusammenfassungen, die älteste zuerst, durch eine leere Zeile getrennt. |
| `{%{{{backgroundLore}}}%}` | Die Lorebook-Einträge, die aktiv waren, als der Teil der Metazusammenfassung geschrieben wurde. |

Der eingebaute Prompt bittet darum, die Ereignisse zu kürzeren Einträgen zu verketten – etwa halb so groß –, ohne Namen, Gegenstände, Vereinbarungen oder offene Handlungsstränge zu verlieren und ohne etwas Neues hinzuzufügen.

<a id="when-summaries-become-outdated"></a>

## Wann Zusammenfassungen veraltet sind

Eine Zusammenfassung ist nur für den genauen Text gültig, aus dem sie erstellt wurde. Wenn du einen Teil, der von einer Zusammenfassung abgedeckt wird, [bearbeitest](story-editor.md#editing-the-text) oder [löschst](story-editor.md#deleting-a-part), passt die Zusammenfassung nicht mehr.

Veraltete Zusammenfassungen werden nicht an das Modell gesendet. Marginalia prüft dies, wenn es den nächsten Teil erzeugt, und fragt: *Die Zusammenfassungen der Nachrichten mit IDs: [...] sind veraltet ... Generierung fortsetzen und sie löschen?* Antworte mit **Ja**, um sie zu löschen und zu generieren; die Teile, die sie abgedeckt haben, werden wieder vollständig gesendet (soweit sie passen), bis du sie erneut zusammenfasst. Antworte mit **Nein**, um abzubrechen: Nichts wird gelöscht, und du kannst zuerst die Zusammenfassungen behandeln.

In der [Übersicht der Zusammenfassungen](#the-summaries-overview) hat eine veraltete Zusammenfassung einen **roten Hintergrund** und das Kennzeichen **Veraltet**, und ihre Tokens werden nicht in der Summe oben gezählt. Wenn sie noch das Richtige aussagt (du hast etwa einen Tippfehler korrigiert), drücke den **Haken**-Button ihrer Zeile, um sie **neu zu validieren**, oder wähle **Alle Zusammenfassungen neu validieren** in der Menüleiste, um alle veralteten auf einmal neu zu validieren. Andernfalls lösche sie oder erzeuge sie neu. Eine Metazusammenfassung wird wie jede Zusammenfassung geprüft; die Zusammenfassungen, die sie zusammengeführt hat, sind Bestandteil von ihr und werden nie allein dadurch veraltet. Das Neuvalidieren wird verweigert, solange ein Agent am Buch arbeitet.

Wenn du die [Bearbeitung eines Teils speicherst](story-editor.md#editing-the-text), die Zusammenfassungen veralten lassen würde, warnt dich Marginalia zuerst und lässt dich zum Bearbeiten zurückkehren, trotzdem speichern oder speichern und die Zusammenfassungen neu validieren.

![Die Frage, wenn ein Speichern Zusammenfassungen veralten lässt](../../images/summaries-save-warning.png)

Nach **Trotzdem speichern** zeigt die Übersicht der Zusammenfassungen die Zusammenfassung als veraltet (rot, mit dem Kennzeichen **Veraltet**, und nicht in der Summe oben gezählt):

![Eine veraltete Zusammenfassung in der Übersicht](../../images/summaries-outdated.png)

Ein [Suchen und Ersetzen](story-editor.md#search-and-replace) in der Geschichte hält die Zusammenfassungen, die es abdecken, gültig und warnt vor denen, die veralten. Das [Suchen und Ersetzen der Zusammenfassungen](#search-and-replace-in-the-summaries) hält die Metazusammenfassungen gültig.

Eine Metazusammenfassung ist veraltet, wenn ein Teil, den sie abdeckt, bearbeitet oder gelöscht wird, aber auch wenn eine Zusammenfassung, die sie zusammengeführt hat, gelöscht wird. (Deshalb können die zusammengeführten Zusammenfassungen nicht einzeln bearbeitet werden: Die Änderung würde die Metazusammenfassung veralten lassen. Das [Suchen und Ersetzen](#search-and-replace-in-the-summaries) berechnet den Prüfwert der Metazusammenfassung neu.) Eine veraltete Metazusammenfassung zu entfernen ist dasselbe wie sie aufzulösen: Die Zusammenfassung, die sie ersetzt hat, kommt zurück und wird ebenfalls geprüft. Wenn auch sie veraltet ist, wird sie auf dieselbe Weise entfernt.

Wenn du von einem Teil mit Zusammenfassung [verzweigst](branches-and-story-tree.md#branching-from-an-earlier-part), erhält der neue Zweig eine Kopie der Zusammenfassung. Eine kopierte Metazusammenfassung behält, was sie ersetzt hat, sodass sie im neuen Zweig aufgelöst werden kann.

<a id="the-summary-prompt"></a>

## Der Zusammenfassungsprompt

Die Anfrage für eine Zusammenfassung ist der **Standard-Zusammenfassungsprompt** des Buches (auf der Registerkarte [Prompts](prompts.md) oder der Standardwert aus *Einstellungen → Vorlagen*). Er erhält zwei Variablen:

| Variable | |
|---|---|
| `{%{{{text}}}%}` | Der Text der zusammenzufassenden Teile, der älteste zuerst. |
| `{%{{{backgroundLore}}}%}` | Die Lorebook-Einträge, die aktiv waren, als der zusammengefasste Teil geschrieben wurde. |

Der eingebaute Prompt bittet um ein kompaktes Ereignisprotokoll, gruppiert nach Ort: unter einer Überschrift `[LOCATION / SCENE: Name]`, ein Ereignis pro Zeile als *wer tut was -> was daraus folgt* (die Absicht nur, wenn der Text sie zeigt), ohne Titel, Nummern oder IDs für die Ereignisse und ohne die Atmosphäre. Es ist für das Modell gedacht, nicht für einen Leser, und umfasst etwa ein Viertel des Umfangs des Textes, den es ersetzt. Der Prompt für Metazusammenfassungen erwartet dasselbe Format und verknüpft benachbarte Zeilen zu Ursache-Wirkungs-Ketten. Ersetze ihn, wenn du ein anderes Format oder eine andere Länge bevorzugst; eine Zusammenfassung, die vor der Änderung des Prompts erstellt wurde, behält ihre alte Form, bis du sie erneut erstellst.

<a id="tips"></a>

## Tipps

- Fasse an natürlichen Einschnitten zusammen: am Ende eines Kapitels oder einer Szene.
- Wenn die Zusammenfassungen zu viel vom Kontext beanspruchen (der Tooltip des Buttons **Zusammenfassungen** zeigt, wie viel), führe die ältesten zu einer Metazusammenfassung zusammen. Wiederhole dies später mit der Metazusammenfassung und den danach folgenden Zusammenfassungen.
- Zusammenfassungen bleiben dauerhaft im Prompt. Bei vielen von ihnen kann der Prompt den Kontext allein füllen, und die Generierung bricht mit *Das Kontextlimit reicht nicht aus.* ab. Kürze sie dann (ein knapperer Zusammenfassungsprompt), führe sie zu [Metazusammenfassungen](#meta-summaries) zusammen oder erhöhe das Kontextlimit.
- Behalte Fakten, die niemals vergessen werden dürfen (Namen, Beziehungen, Regeln der Welt), im [Lorebook](../lorebooks/index.md), statt dich auf Zusammenfassungen zu verlassen.
- Prüfe mit **Prompt anzeigen** am nächsten Teil, was das Modell erhalten hat.
