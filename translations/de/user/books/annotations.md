---
label: Anmerkungen
order: 95
---

# Anmerkungen

Eine **Anmerkung** ist eine Notiz für dich über eine Passage eines Teils, zum Beispiel *schwache Szene*, *Zeitablauf stimmt nicht*, *überarbeiten*. Sie funktioniert wie ein Kommentar in einer Textverarbeitung: Der Text der Geschichte bleibt unverändert, die Passage wird nur markiert, und die Notiz steht daneben. Notizen können auch von einem Modell oder von einer [Erweiterung](../extensions/index.md) stammen, die die Geschichte prüft.

Eine Anmerkung ist **für den Autor bestimmt**. Sie wird nicht an das Modell geschickt, außer du legst das für diese Notiz ausdrücklich fest, siehe [Eine Notiz an das Modell geben](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Was eine Anmerkung ist

| | |
|---|---|
| **Passage** | Die Wörter, auf die sich die Notiz bezieht, oder der **ganze Teil**. |
| **Note** | Was du dir merken möchtest. |
| **Type** | Ein optionales Wort, das du wählst (*Kontinuität*, *Stil*, *Tempo* ...). |
| **Source** | Wer sie geschrieben hat: du, ein Modell (*AI*), ein *Agent* oder eine *Erweiterung*. Die Markierung im Text hat für jede Quelle eine andere Farbe. |
| **Status** | *Open*, *Resolved*, *Dismissed* oder *Orphaned*. Im Text werden nur offene Notizen markiert. |

Ein Teil kann beliebig viele Anmerkungen haben, auch zu sich überlappenden Passagen.

<a id="making-an-annotation"></a>

## Eine Anmerkung erstellen

**Zu einer Passage.** Markiere die Wörter im Text des Teils (sie können Formatierungen enthalten, die Markierung muss nicht genau passen). Dann führe einen **Rechtsklick** aus (auf dem Telefon ein langes Drücken) und wähle **Annotate selection**, oder drücke die zugehörige [Taste](story-editor.md#keyboard-shortcuts) (`a`). Das Fenster zeigt die Passage. Schreibe die Notiz, optional einen Typ, und lege fest, ob das Modell die Notiz erhält. Dann klicke auf **Save**. Das Menü (☰) des Teils hat keinen solchen Eintrag: Beim Öffnen geht die Markierung verloren. Das Kontextmenü mit dem Rechtsklick enthält **Annotate selection**, [**Redraft selection**](story-editor.md#redrafting-only-a-passage) und **Copy**. Es ersetzt das des Browsers auf dem Text eines Teils, daher hat es auch **Copy**. Während der Teil bearbeitet wird, wird die Markierung im Editor verwendet; die Änderung wird vorher gespeichert.

**Zum ganzen Teil.** Wähle **Annotate part** im Menü des Teils.

**Mit einem Modell.** Wähle **Annotate with AI**, siehe [unten](#letting-a-model-annotate).

**Über die Kommandozeile** (und für Erweiterungen): [`/create-annotation`](../commands/index.md#annotations).

![Das Kontextmenü beim Rechtsklick auf den Text eines Teils](../../images/story-text-menu.png)

![Das Fenster einer neuen Anmerkung](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Anmerkungen lesen

Eine Passage mit einer offenen Notiz ist mit einer **Wellenlinie** unterstrichen. Die Farbe zeigt, wer die Notiz geschrieben hat: blau für dich, orange für ein Modell, grün für einen Agenten, rot für eine Erweiterung. Eine Notiz innerhalb einer anderen wird tiefer unterstrichen, damit beide sichtbar sind.

![Die Notiz einer unterstrichenen Passage, geöffnet an der Markierung](../../images/annotation-popover.png)

- **Klicke auf den unterstrichenen Text**, um die Notizen zu sehen, die diese Stelle abdecken (alle, wenn sich Passagen überlappen). Sie öffnen sich neben dem Text. Auf dem Telefon tippst du auf die Passage.
- Eine **Sprechblase mit einer Zahl** im Kopf eines Teils zählt die Notizen des Teils, die eine Prüfung brauchen. Klicke darauf, um alle zu sehen, auch die, die sich auf den ganzen Teil beziehen, und die, deren Passage verloren ging.
- Am **Rand** (das Zahnradmenü der Geschichte, **Annotations in the margin**; nur am Desktop, und deine Einstellung wird gespeichert) stehen die Notizen aller Teile in einer Spalte links neben dem Text, in der Reihenfolge ihrer Passagen, statt im Popover. Die Notiz unter dem Mauszeiger und ihre unterstrichene Passage werden gemeinsam hervorgehoben, und ein Klick auf eine Passage holt ihre Notiz ins Blickfeld. Es sind dieselben Notizen (auflösen, bearbeiten ...). Gelöste und verworfene Notizen werden dort nicht angezeigt, weil sie auch nicht unterstrichen sind.
- Der Tab **Annotations** listet die Notizen des ganzen Buches auf, gruppiert nach Teil in der Reihenfolge der Geschichte, mit einem Filter (*Need a look*, *Closed*, *All*). **Show in story** (oder die Nummer des Teils) bringt dich zum Teil.

![Die Notizen am Rand eines Teils](../../images/annotations-margin.png)

![Der Tab Annotations](../../images/annotations-tab.png)

Jede Notiz zeigt, wer sie geschrieben hat, ihren Typ, ihren Status, die Passage, die Notiz und diese Aktionen:

| | |
|---|---|
| ✓ **Resolve** | Das Problem ist erledigt. Die Notiz verschwindet aus dem Text und bleibt in der Liste. |
| ✗ **Dismiss** | Die Notiz lohnt die Mühe nicht. Ansonsten wie bei **Resolve**. |
| ↶ **Reopen** | Holt eine geschlossene Notiz zurück. |
| ✎ **Edit** | Ändert die Notiz und den Typ. |
| 🗑 **Delete** | Löscht die Notiz; sie kann aus dem [Papierkorb](../trash.md) wiederhergestellt werden. |
| **Give to the model with the part** | Siehe unten. |
| **Attach to selection** | Für eine Notiz ohne Passage: Markiere Text im Teil und klicke dann darauf. |

<a id="annotations-follow-the-text"></a>

## Anmerkungen folgen dem Text

Du musst beim Bearbeiten nicht darauf achten, wo eine Notiz steht. Marginalia behält die Passage jeder Notiz bei den Wörtern, auf die sie sich bezog:

- Text, der **vor** der Passage hinzugefügt oder entfernt wird, verschiebt sie;
- eine Änderung **innerhalb** der Passage macht sie länger oder kürzer;
- Wörter, die du **direkt am Anfang oder Ende** der Passage tippst, liegen außerhalb davon;
- das Korrigieren eines Tippfehlers in einer Passage aus einem einzigen Wort behält die Notiz bei diesem Wort.

Wenn die Passage **gelöscht** wird oder so umgeschrieben wird, dass sie nicht mehr gefunden werden kann, wird die Notiz **Orphaned**. Sie bleibt erhalten: Hänge sie an eine andere Passage (**Attach to selection**), verwirf sie oder lösche sie. Wenn du die Wörter wiederherstellst (Rückgängig), findet die Notiz ihre Passage wieder.

Wenn ein Teil **neu generiert** wird, ist der Text neu. Dann finden die Notizen ihre Passagen anhand der Wörter wieder, die sie zitiert haben. Die Notizen, deren Wörter nicht mehr vorkommen, werden verwaist. Wenn zwei Teile [zusammengeführt](story-editor.md#merging-a-part-into-the-previous-one) werden, wandern die Notizen des späteren Teils in den zusammengeführten Teil. Eine **Verzweigung** eines Teils kopiert seine Notizen. Notizen sind Teil der [Sicherungen](backups.md), der Kopien eines Buches und der Exporte einer Sicherung.
<a id="letting-a-model-annotate"></a>

## Ein Modell annotieren lassen

**Annotate with AI** im Menü eines Teils bittet ein Modell, ihn zu prüfen. Sie sagen, worauf es achten soll (*flag continuity errors*, *weak dialogue*, *contradicts the lore*). Leer lassen heißt: Es gilt, was das Buch, sein Ordner oder Ihre [einmal festgelegten](prompts.md#annotate-prompt) Einstellungen vorgeben, sonst die Aufgabe des Prompts. Dann wählen Sie das Modell und klicken auf **Annotate**.

Das Modell liest den Teil so, wie es der [Neuentwurf](story-editor.md#redrafting-a-part) tut: den Teil, die Geschichte davor (ältere Geschichte als [Zusammenfassungen](summaries.md)), so viel von der Geschichte danach, wie hineinpasst, das Lorebook und den Stil des Buchs sowie die Notizen, die der Teil bereits hat, damit es sich nicht wiederholt. Es gibt keine Positionen an, sondern **zitiert** die Textstelle, und Marginalia findet das Zitat im Text. Ein Zitat, das nicht gefunden wird, geht nicht verloren: Die Notiz wird als Notiz zum ganzen Teil vorgeschlagen und als *not found in the text* markiert.

**Dem Buch wird nichts hinzugefügt, bevor Sie es annehmen.** Die vorgeschlagenen Notizen werden jeweils mit einem Kontrollkästchen aufgelistet (alle angehakt). Entfernen Sie den Haken bei denen, die Sie nicht wollen, und klicken Sie auf **Add the selected annotations**.

Das Fenster blockiert am Computer nicht die Geschichte. Während das Modell arbeitet, lässt es sich in die Ecke der Seite falten, siehe [Neben dem Fenster arbeiten](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Option | |
|---|---|
| **Model** | Das Modell, das prüft. Leer heißt: das **Default model for annotating** aus Ihren [Einstellungen](../account.md#general-settings), sonst das Modell des Buchs. Prüfen ist Analyse, deshalb ist es oft nicht das Modell, das den Text schreibt. |
| **Answer format** | *Automatic* (strukturiert, wenn das Modell im JSON-Format antworten kann), *Structured (JSON)*, *Text blocks* (für Modelle, die sich nicht an ein Format halten) oder *Free critique, then extraction* (das Modell schreibt eine freie Kritik, und ein zweites Modell, meist ein günstigeres, macht daraus Notizen. Wählen Sie es unter **Model that extracts the notes**). |

![Die Notizen, die ein Modell für einen Teil vorschlägt](../../images/annotate-ai-proposals.png)

**Eine Reihe von Teilen (Stapel).** **Annotate with AI** im Menü eines Teils prüft nur diesen einen Teil. Um mehrere zu prüfen, öffnen Sie das Einstellungsmenü (⚙) der Geschichte und wählen **Annotate a range of parts with AI** (auch auf dem Telefon). Es ist dasselbe Fenster mit **From part** und **To part** als Auswahl (beide sind Pflichtfelder, **Annotate** wartet auf sie; es spielt keine Rolle, welcher der frühere ist; es werden nur Teile mit Text aufgelistet). **Annotate** prüft dann die Teile der Reihe nacheinander, mit denselben Anweisungen, demselben Antwortformat und Modell, und jeden Teil zusammen mit der Geschichte um ihn herum, als wäre es ein einzelner Teil. Die Vorschläge werden unter der Nummer ihres Teils aufgelistet, alle angehakt, und **Add the selected annotations** fügt jede Notiz dem eigenen Teil hinzu. **Stop** beendet den gerade laufenden Teil und startet keinen weiteren. Was bis dahin gefunden wurde, bleibt in der Liste. Ein Teil, dessen Antwort sich nicht lesen lässt oder der nicht in den Kontext des Modells passt, wird übersprungen (das Fenster sagt, welcher), und der Lauf geht weiter. Ein Fehler des Modells beendet den Lauf. Jeder Teil ist eine Anfrage an das Modell, eine lange Reihe kostet also genauso viele Anfragen (und Tokens).

![Annotieren einer Reihe von Teilen: Die Vorschläge sind unter der Nummer ihres Teils aufgelistet](../../images/annotate-ai-batch.png)

**Fakten aus Erweiterungen.** Eine [Erweiterung](../extensions/index.md), die mehr über die Geschichte weiß, als ihr Text sagt, kann dem Prüfer dieses Wissen geben. Der Prüfer markiert dann die Stellen, die dem widersprechen, als Kontinuitätsfehler. Der [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know) tut das: Er sagt dem Modell, was jede verfolgte Figur des Teils zu Beginn des Teils wusste. So wird eine Figur, die einen Namen benutzt, den sie nie gehört hat, notiert. Die Fakten stehen im eingebauten [Annotate-Prompt](prompts.md#annotate-prompt) (ein eigener Prompt muss `{%{{{facts}}}%}` verwenden). Ohne eine solche Erweiterung bleibt der Prompt unverändert.

Lässt sich eine Antwort nicht lesen, fragt Marginalia noch einmal nach. Lässt sie sich auch dann nicht lesen, wird die Antwort des Modells angezeigt, damit die Arbeit nicht verloren geht. Der Prompt ist der [Annotate-Prompt](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Jeden neuen Teil automatisch annotieren

Das Einstellungsmenü (⚙) der Geschichte enthält **Automatic annotation** (auch auf dem Telefon; ein Häkchen zeigt, dass es eingeschaltet ist). Damit prüft das Buch jeden Teil direkt nach seiner Erzeugung und optional mit einem Neuentwurf. Die Einstellungen werden mit dem Buch gespeichert:

| Option | |
|---|---|
| **Model**, **Protocol** | Das Modell, das prüft. Leer heißt: das Modell des Buchs (nicht das **Default model for annotating** Ihrer Einstellungen, mit dem das Fenster startet, das Sie selbst öffnen). |
| **What to look for** | Die Anweisungen, wie bei **Annotate with AI**. Leer heißt: Es gilt, was das Buch erbt (siehe [Annotate-Prompt](prompts.md#annotate-prompt)), sonst die Aufgabe des Prompts. Wenn Sie das Feld leer lassen, zeigt es danach den verwendeten Text. |
| **Annotate every generated part automatically** | Schaltet es ein. Nachdem ein Teil erzeugt wurde (ein neuer Teil, ein Swipe oder eine Neugenerierung; nicht ein abgebrochener oder fehlgeschlagener), prüft das Modell ihn **sofort**, ohne Fenster. Das [Panel der Läufe](story-editor.md#the-panel-of-the-runs) zeigt es als Schritt von [dem, was dem Teil folgt](story-editor.md#what-follows-a-generated-part). |
| **Add the annotations without asking** | Wenn das Modell fertig ist, werden die gefundenen Notizen hinzugefügt. Ohne diese Option öffnet sich das Annotierungsfenster mit den Vorschlägen (auf der Seite, auf der Sie erzeugt haben; über **Review** im Panel auf jeder Seite) und wartet, bis Sie auswählen, wie beim Start von Hand. |
| **Then redraft the part to address the annotations** | Nur zusammen mit der oberen Option. Nachdem Notizen hinzugefügt wurden, [entwirft](story-editor.md#redrafting-a-part) das Modell den Teil neu, mit den offenen Notizen des Teils und ohne eigene Anweisungen. Es schreibt mit dem Modell des Buchs und fasst keine Zusammenfassungen an: Der Teil ist neu, und keine Zusammenfassung beschreibt ihn. Ein Modell, das nichts gefunden hat, hat nichts zum Neuentwerfen. |
| **Apply the redraft without asking** | Nur mit dem Neuentwurf. Wenn das Modell fertig ist, ersetzt der Neuentwurf den Text des Teils, und die Notizen, um die es sich gekümmert hat, werden als erledigt markiert. Ohne diese Option oder wenn Sie den Teil geändert haben, während das Modell schrieb, öffnet sich das Neuentwurf-Fenster mit dem Ergebnis und wartet auf Sie. |

Das wartende Neuentwurf-Fenster hat zwei Reiter: **Redrafted text** (den Text, seine Begründung und die Unterschiede zum Teil) und **Original text with annotations** (den Teil, wie er war, mit den Passagen der offenen Notizen markiert wie in der Geschichte, und darunter die Notizen. Klicken Sie auf eine Markierung, um zu ihrer Notiz zu springen, oder zeigen Sie auf eine von beiden, um beide hervorzuheben). Sehen Sie sich das Original an, um zu erkennen, womit das Modell sich befassen sollte, und ändern Sie dann den Text oder übernehmen Sie ihn.

![Die Einstellungen der automatischen Annotierung](../../images/annotate-auto.png)

**Ein Fenster schließen, das auf Sie wartet, oder Stop im Panel beendet alles, was dem Teil folgt.** Es wird nichts mehr hinzugefügt oder übernommen, und die späteren Schritte (eine Übersetzung, das Audio) werden nicht ausgeführt. Die Meldung des Panels nennt sie, damit Sie sie von Hand erledigen können. Was schon hinzugefügt oder übernommen wurde, bleibt. Während der Neuentwurf läuft, kann kein neuer Teil erzeugt werden (er würde am alten Text anknüpfen). Während die Notizen entstehen, ist das möglich.

Jeder erzeugte Teil kostet mindestens eine weitere Anfrage an das Modell (bei Neuentwurf zwei). Wenn Sie mit einem lokalen Server erzeugen, der nur einen Prompt in seinem Cache behält, ersetzt die Prüfung die Geschichte darin, und die nächste Erzeugung muss die Geschichte erneut lesen. Wählen Sie dann für die Prüfung ein anderes **Model** oder einen zweiten Server.
<a id="watching-for-phrases"></a>

## Auf Phrasen achten

Eine **Phrasen-Watchlist** nennt Wörter, die im Text nicht vorkommen sollen: Klischees (*a shiver ran down her spine*, *tapestry*), die Eigenheiten eines Modells, ein Name, den du fallen gelassen hast. Ein Teil wird **ohne Modell** auf sie geprüft, und jede Fundstelle wird zu einer Annotation dieser Textstelle (ihre Quelle ist *Phrase watch list*, und der Eintrag, auf den sie passt, wird mit genannt). Von dort ist sie eine Notiz wie jede andere: Lass die [Überarbeitung](#redrafting-to-address-the-notes) sie behandeln, oder **Dismiss** einen Treffer, der in Ordnung ist (etwa in der wörtlichen Rede).

![Die Phrasen-Watchlist eines Buches](../../images/phrase-watch-dialog.png)

Die Phrasen sind nicht aus der Ausgabe des Modells verbannt. Ein Verbot würde eine Phrase erst stoppen, nachdem sie schon begonnen hat, und das Modell zum nächstliegenden Synonym drängen; eine Notiz am fertigen Text lässt den Abschnitt als Ganzes umschreiben.

**Wo die Listen stehen.** Deine Einstellungen haben einen Reiter **Phrase watch list** mit deiner Liste, die jedes Buch verwendet; ein [Ordner](folders.md#folder-settings) fügt eine Liste für die Bücher darin und die Ordner darunter hinzu, ein Buch eine eigene im Menü **Phrase watch list** der Einstellungen (⚙) der Geschichte. Die Listen addieren sich. Ein Eintrag mit derselben Phrase wie ein geerbter ersetzt ihn (etwa um ihm eine andere Vorlage zu geben), und einer, der mit **Off here** markiert ist, entfernt sie für diesen Ordner oder dieses Buch; die geerbten Phrasen stehen unter deinen eigenen, jede mit **Switch off here**.

**Wie eine Phrase gefunden wird.** Ohne Unterscheidung von Groß- und Kleinschreibung und nur als ganze Wörter (*tapestry* wird nicht in *tapestries* gefunden); jedes Leerzeichen oder jeder Zeilenumbruch passt auf jedes andere, und gerade und typografische Anführungszeichen und Apostrophe passen aufeinander. Markiere **Regular expression**, um stattdessen einen [regulären Ausdruck](../regular-expressions.md) zu schreiben, ebenfalls ohne Beachtung der Groß- und Kleinschreibung. Ein Ausdruck, der nicht verwendet werden kann, wird beim Speichern abgelehnt. In einem Teil werden höchstens 100 Notizen angelegt.

**Annotationsvorlagen.** Was eine Notiz sagt, ist eine **Annotationsvorlage** deiner Einstellungen (derselbe Reiter): ein Name, die **Note**, wobei `{%{{{match}}}%}` die gefundenen Wörter und `{%{{{phrase}}}%}` der Eintrag der Liste ist, ein **Type** (die Farbe der Markierung) und **Give to the model with the part** (siehe [Eine Notiz an das Modell geben](#giving-a-note-to-the-model)). Jeder Eintrag einer Liste wählt eine aus; ein Eintrag ohne eine bekommt eine eingebaute Notiz (*Watched phrase: "…"*), ohne Typ und nicht an das Modell gegeben.

**Wann die Teile geprüft werden.** Im Reiter **Phrase watch list** des Buches:

| Option | |
|---|---|
| **Annotate the watched phrases in every generated part** | Nachdem ein Teil erzeugt wurde, wird er gleich nach der [automatischen Annotation](#annotating-every-new-part-automatically) geprüft (ein Schritt von [was auf den Teil folgt](story-editor.md#what-follows-a-generated-part), ob die automatische Annotation eingeschaltet ist oder nicht). Überarbeitet das Buch außerdem automatisch (**Add the annotations without asking** und **Then redraft the part…** der automatischen Annotation), kümmert sich die Überarbeitung auch um diese Notizen; eine ohne Nachfrage übernommene Überarbeitung wird erneut geprüft, und wenn sie eine Phrase wieder hineingeschrieben hat, wird sie erneut überarbeitet, höchstens dreimal (die Treffer der letzten bleiben als Notizen für dich). Die Teile, die ein [Agent](../agents.md) schreibt, werden ebenfalls geprüft. |
| **Save and scan the story** | Speichert die Liste und prüft die Teile des aktiven Zweigs jetzt, für ein Buch, das schon geschrieben ist: alle, oder diejenigen von **From part** bis **To part** (die Nummern der Teile im Zweig). Der Befehl `/scan-phrases [from to]` tut dasselbe. |

Eine Stelle, die schon eine Notiz der Liste für dieselbe Phrase hat, bekommt keine zweite, auch wenn du sie verworfen hast, damit die Geschichte erneut geprüft werden kann, nachdem sie sich geändert hat. Eine gelöste Notiz zählt nicht: Ist die Phrase noch da (eine Überarbeitung hat sie wieder hineingeschrieben), bekommt sie eine neue Notiz. Die Annotationsvorlagen gehören zu deinen Einstellungen: Ein Buch, das mit dem exportiert wird, was es erbt, nimmt die Phrasen mit, und auf einer anderen Installation sind ihre Notizen die eingebaute.

<a id="giving-a-note-to-the-model"></a>

## Eine Notiz an das Modell geben

Setzt du **Give to the model with the part** bei einer Notiz, wird sie auch eine Notiz an das Modell: Die Notizen der Teile, die im Prompt stehen, gehen als `{%{{{annotations}}}%}` in die Hauptvorlage (die eingebaute Vorlage hat einen Block *Author's notes on the story*; eine eigene Vorlage muss die Variable verwenden). Nur **offene** Notizen mit diesem Haken werden gegeben, und nur für die Teile, die in den Prompt passen. Verwende ihn für das, woran das Modell denken soll, während es weiterschreibt (*this character is hiding the letter*), nicht für Bemerkungen zum Überarbeiten.

<a id="redrafting-to-address-the-notes"></a>

## Überarbeiten, um die Notizen zu behandeln

Hat ein Teil offene Notizen, hat das Fenster der [Überarbeitung](story-editor.md#redrafting-a-part) ein Kontrollkästchen **Deal with the open annotations of the part**. Ist es markiert, bekommt das Modell die Notizen (die Textstelle und die Notiz) zusammen mit deinen Anweisungen und schreibt den Teil um, um sie zu behandeln; wenn du die Überarbeitung mit **Apply** übernimmst, werden diese Notizen als gelöst markiert. Bei der [Überarbeitung einer Auswahl](story-editor.md#redrafting-only-a-passage) werden nur die Notizen gegeben, deren Textstelle die Auswahl überschneidet.

<a id="commands"></a>

## Befehle

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`, `/resolve-annotation`, `/reattach-annotation` und `/delete-annotation` arbeiten mit den Notizen über die Kommandozeile. Erweiterungen können dieselben Befehle verwenden. [`/generate-annotation <part>`](../commands/index.md#annotations) ist **Annotate with AI** als Befehl: Das Modell prüft den Teil mit dem Modell des Buches (ein Agent verwendet sein eigenes Modell, wenn *Write the book with the model of the agent* eingeschaltet ist; `profile` wählt ein anderes Modellprofil des Agenten), mit den Anweisungen, dem Antwortformat und dem Prompt des Buches, und jede Notiz, die es findet, wird sofort hinzugefügt, da niemand da ist, der sie auswählt. Es ist eine einzige Anfrage an das Modell, läuft also allein und kostet Tokens.

<a id="annotations-and-agents"></a>

## Annotationen und Agenten

Ein [Agent](../agents.md) liest und schreibt Annotationen mit denselben Befehlen: Er kann ein ganzes Buch annotieren (*continuity*, *style*...), Notizen lösen, die er behandelt hat, sowie sie bearbeiten oder löschen. Er arbeitet an einer Kopie des Buches, sodass nichts zu deinen Notizen gelangt, bis du [das Ergebnis prüfst](../agents.md#reviewing-the-result):

- Eine Notiz, die der Agent angelegt hat, ist eine Zeile **Annotation - Added**, eine, die er gelöst, verworfen oder umgeschrieben hat, ist **Changed** (mit Notiz, Typ, Status und *give to the model* als Felder, jedes mit eigenem Kästchen **Take this change**), eine, die er gelöscht hat, ist **Deleted**. Die Notizen eines Teils, den der Agent gelöscht hat, gehen mit dem Teil und haben keine Zeilen.
- Die Zeile sagt, in welchem Teil die Notiz steht (`3: ` und der Anfang der Notiz); **Show in the copy** öffnet den Teil in der Kopie des Agenten. Eine Notiz zu einem Teil, den der Agent geschrieben hat, braucht diesen Teil: Wenn du die Notiz markierst, markierst du auch den Teil.
- **Only annotations** oberhalb der Liste markiert nur die Notizen. Die Textstelle einer übernommenen Notiz bleibt dieselbe, wenn der Text des Teils gleich ist; wenn du den neuen Text des Teils des Agenten übernimmst oder dein eigener Text abweicht, findet die Notiz ihre Wörter im Text deines Buches erneut und wird, wenn das nicht gelingt, zu einer Notiz über den ganzen Teil.
- Eine Notiz, die du geschrieben hast, während der Agent angehalten war, kommt beim Fortsetzen in seine Kopie (und eine Notiz, die du gelöst hast, ist dort gelöst), damit er davon weiß; eine Notiz, die du und der Agent beide geändert habt, ist ein Konflikt wie jeder andere.
