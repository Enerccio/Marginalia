---
label: Knowledge Tracker
order: 11
---

# Knowledge Tracker

Behält im Blick, was die Figuren deiner Geschichte wissen und was sie **nicht** wissen, und fügt dies in den Prompt ein. Ein Modell neigt dazu, einer Figur Wissen über Menschen zuzuschreiben, denen sie nie begegnet ist (deren Namen, Beruf, Gesicht), sofern die Anweisungen dies nicht verhindern. Mit dem Knowledge Tracker sagt der Prompt für jede verfolgte Figur in der Szene, was diese Figur weiß und dass sie darüber hinaus nichts weiß.

!!!
Du entscheidest, wer verfolgt wird. Nur die Figuren, die du dem Tab hinzufügst, werden dem Modell beschrieben, und nur solange sie zu den *anwesenden Figuren* des gerade geschriebenen Teils gehören. Nichts wird aus dem Erzähltext geraten.
!!!

<a id="the-knowledge-tab"></a>

## Der Knowledge-Tab

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) erhält neben *Outline* einen Tab **Knowledge**. Alles wird im Buch gespeichert, sobald du es änderst.

![Der Knowledge-Tab](../../images/plugin-knowledgetracker.png)

**Figur verfolgen** fragt nach einem **Namen** und optional nach **Aliasen**: den anderen Namen, unter denen die Figur im Feld *Anwesende Figuren* eines Teils geschrieben wird, durch Kommas getrennt. Ein Name oder Alias kann nur zu einer verfolgten Figur gehören.

Öffne eine Figur mit dem Pfeil auf ihrer Karte, um zu sehen, was sie weiß:

| | |
|---|---|
| **Weiß** (grün) / **Weiß nicht** (rot) | Ein kurzer Satz über beliebige Dinge: eine Person, einen Ort, ein Geheimnis, ein Ereignis (*Peter weiß vom Komplott gegen den Herzog*, *Fiora weiß nicht, dass Thomas ihr Vater ist*). Schreibe ausdrücklich auf, was eine Figur **nicht** weiß, denn genau das respektiert das Modell. |
| Die Reihenfolge und **#12** | Das neueste Wissen steht oben (altes Wissen ebenfalls). Ein Eintrag, den das Modell gelernt hat, endet mit der kleinen Nummer des Teils, in dem es gelernt wurde (**#12**): Ein Klick scrollt die Geschichte zu diesem Teil und lässt ihn kurz aufleuchten (auf dem Handy schließt sich zuvor die Schublade). Von dir geschriebene Einträge haben keine Nummer, ebenso wenig einer, dessen Teil nicht auf dem aktiven Zweig liegt (anderer Zweig oder der Teil wurde gelöscht). |
| **Eintrag hinzufügen** | Fügt einen Eintrag hinzu, der sofort aktiv ist. Halte ihn bei **einem Gedanken**: Ein Eintrag, der zwei Dinge sagt (*weiß nicht, wer Thomas ist, ist ihm nie begegnet*), wird zurückgezogen, sobald eines davon nicht mehr stimmt. Die Schaltfläche **Bearbeiten** ändert Text und Art. |
| **Zurückziehen** (der Kreis mit Schrägstrich) | Der Eintrag stimmt nicht mehr (*ist Thomas nie begegnet*, nachdem die beiden sich getroffen haben). Er wird nicht gelöscht, sondern wird zu *altem Wissen* und nicht mehr an das Modell gesendet. |
| **Sperren** (das Vorhängeschloss, bei Einträgen, die das Modell gelernt hat) | Der [Aufräumdurchgang](#tidying-the-knowledge) sieht einen gesperrten Eintrag nie: Verwende ihn für das, was bleiben muss, einen Verrat, ein Geheimnis. Die Geschichte kann einem gesperrten Eintrag trotzdem widersprechen (eine Figur, die ihr Gedächtnis verliert): Das Lesen der Geschichte zieht ihn wie üblich zurück. |
| **Löschen** | Entfernt einen Eintrag endgültig, nach einer Rückfrage. Zurückziehen behält ihn, verwende also im Zweifel dieses. |
| **Einträge auswählen** (neben **Eintrag hinzufügen**) | Löscht schnell viele Einträge: Jeder Eintrag der Figur, auch die alten, erhält ein Kontrollkästchen. **Alle auswählen** / **Auswahl aufheben** setzt oder entfernt alle Häkchen, **Ausgewählte löschen (n)** entfernt die markierten nach einer Rückfrage endgültig, **Abbrechen** verlässt die Auswahl. |
| **Hat getroffen** | Die Personen, denen die Figur begegnet ist, vermerkt durch [Verarbeitung](#processing). Die Verarbeitung kann einen Namen vermerken, der keine Person ist (eine Notiz im Feld anwesende Figuren): Öffne **Bearbeiten** und ändere die Liste **Hat getroffen**, Name für Name, durch Kommas getrennt. Die Namen, die du schreibst, bleiben erhalten, wenn die Geschichte erneut gelesen wird; einer, den du entfernst, kommt nur wieder, wenn der Teil, in dem er gesehen wurde, erneut gelesen wird. |
| **Altes Wissen (n)** | Standardmäßig eingeklappt. Jeder alte Eintrag kann **kopiert**, **bearbeitet**, **wiederhergestellt** (er ist ab jetzt wieder aktiv) oder gelöscht werden. |

<a id="mass-changes"></a>

### Massenänderungen

Die Schaltfläche **Massenänderungen** (das Listensymbol neben **Figur verfolgen**) öffnet ein Fenster mit jeder verfolgten Figur und all ihren Einträgen, auch den alten, in einem Raster mit Kontrollkästchen: die Art, den Text, den Zustand (*Aktiv*, oder alt: *zurückgezogen*, *in einer Prüfung abgelehnt*, *aufgeräumt*), woher er stammt (*Ausgangszustand*, *von Hand geschrieben*, *aus der Geschichte gelernt*) und den Teil, in dem er gelernt wurde. Hake Einträge in beliebig vielen Rastern an (das Kästchen im Kopf eines Rasters hakt alle seine Einträge an, **Alle auswählen** / **Auswahl aufheben** in der unteren Leiste alle Einträge aller Figuren), dann:

| | |
|---|---|
| **Ausgewählte zurückziehen (n)** | Die markierten aktiven Einträge werden zu altem Wissen, wie es **Zurückziehen** für einen einzelnen tut. |
| **Ausgewählte wiederherstellen (n)** | Die markierten alten Einträge sind ab jetzt wieder aktiv. |
| **Ausgewählte löschen (n)** | Entfernt die markierten Einträge endgültig, nach einer Rückfrage. |

Die Zähler auf den Schaltflächen zeigen, wie viele der markierten Einträge jede davon ändert. Die Häkchen bleiben nach einer Änderung erhalten, sodass zurückgezogene Einträge anschließend gelöscht werden können.

**Als JSON exportieren** (oben im Fenster) lädt die verfolgten Figuren des Buchs (Namen, Aliase, Einträge mit ihrer Art, ihrem Zustand, ihrer Herkunft und Sperre, die Personen, denen sie begegnet sind) als Datei herunter; nichts von der Verarbeitung, sodass sie zu jedem Buch passt. **Aus JSON importieren** liest eine solche Datei in das Buch ein und ergänzt nur: Eine Figur, deren Name im Buch bereits verfolgt wird (als Name oder Alias), erhält die Aliase, Einträge und Personen, die sie noch nicht hat (ein Eintrag mit derselben Art und demselben Text wird übersprungen), die übrigen werden zu neuen verfolgten Figuren. Ein Alias, den bereits eine andere Figur hat, wird weggelassen. Ein importierter Eintrag hat keinen Teil dieses Buchs hinter sich, daher entfernt eine erneute Verarbeitung ihn nie: Einer, den das Modell gelernt hat, zählt als von Hand geschrieben, ein alter bleibt alt. Die Meldung nach dem Import sagt, wie viele Figuren hinzugefügt oder aktualisiert wurden und wie viele Einträge hinzugefügt wurden oder schon vorhanden waren.

<a id="what-goes-into-the-prompt"></a>

## Was in den Prompt gelangt

Für jede verfolgte Figur, die zu den anwesenden Figuren gehört, erhält der Prompt einen Block wie diesen:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Nur aktive Einträge werden gesendet. Die letzte Zeile (die Zeile **geschlossene Welt**) verhindert, dass das Modell der Figur Wissen gibt, das sie nicht haben kann. Sie deckt das ab, was die Geschichte erzählt (Personen, Orte, Geheimnisse, Pläne, Ereignisse), nicht aber das, was eine Person in dieser Welt ohnehin weiß, sodass eine Figur weiterhin ihre eigene Stadt kennt. Du kannst die Zeile in den Einstellungen ausschalten. Eine verfolgte Figur, die nicht anwesend ist, erhält überhaupt nichts, auch keine Zeile, die sagt, dass sie etwas nicht weiß.

<a id="who-is-present"></a>

### Wer anwesend ist

Der Block entsteht aus dem Feld **Anwesende Figuren** des Teils. Das Feld ist freier Text und wird deshalb nachsichtig gelesen:

- Text in Klammern wird ignoriert: `Tomáš (müde), Mizuki` sind Tomáš und Mizuki;
- die Namen werden an den **Trennzeichen** getrennt (Standard: `,` `;` ein Zeilenumbruch, `/`, `|`, `、` `，` `；`);
- was nach ` - ` oder `:` steht, ist eine Notiz und wird verworfen (`Mara - verletzt`);
- Groß- und Kleinschreibung, Akzente und ein führendes *the* spielen keine Rolle;
- ein Name muss **als Ganzes** passen: *Maras Bruder* ist nicht *Mara*. Verwende die Aliase, wenn eine Figur auf mehr als eine Weise geschrieben wird. Der Gedanke dahinter: Ein verpasster Treffer kostet einen Block, ein falscher Treffer würde dagegen eine Figur beschreiben, die nicht in der Szene ist.

Das Feld *Point of View* wird nicht für sich allein gelesen, schreibe also die Figur aus der Sicht der Erzählung ebenfalls unter die anwesenden Figuren.
<a id="processing"></a>

## Verarbeitung

Das Plugin folgt der Geschichte. **Verarbeitung** liest die Teile des aktiven Zweigs der Reihe nach, ab dem **Startpunkt**, und
behält einen **Cursor** (den zuletzt gelesenen Teil). Ohne Modell hält es fest, wer wem begegnet ist: Eine verfolgte Figur hat jeden
Namen kennengelernt, der zusammen mit ihr im Feld der anwesenden Figuren steht, und der Block listet sie auf. Mit Modell findet es außerdem,
was die Figuren **erfahren** haben (siehe [Erfahrenes der Figuren auslesen](#reading-what-the-characters-learn)).

![Das Verarbeitungsfenster](../../images/plugin-knowledgetracker-processing.png)

Das Fenster oben im Reiter zeigt, wo die Verarbeitung steht. Es lädt sich selbst neu, wenn ein neuer Teil endgültig ist (nach der automatischen
Annotation und Überarbeitung, falls das Buch sie hat). Wenn du Teile auf anderem Weg hinzufügst, bearbeitest oder löschst, drücke die Schaltfläche **Aktualisieren** (die Pfeile neben dem Zahnrad), um die Geschichte erneut zu lesen. Es enthält:

- **Jetzt verarbeiten**: liest die Teile, die noch nicht verarbeitet sind.
- **Nächsten Teil verarbeiten**: liest nur den ersten noch nicht verarbeiteten Teil, Schritt für Schritt (mit Modell: eine Anfrage für diesen Teil, unabhängig davon, was *Teile pro Lauf* vorgibt). Verwende sie, um die Einstellungen auszuprobieren oder zu sehen, was das Modell für einen einzelnen Teil findet. Der Befehl `knowledge-process` mit `parts` auf 1 tut dasselbe.
- **Startpunkt**: der Teil, bei dem die Verarbeitung beginnt (standardmäßig *Anfang des Buches*). Teile davor werden nie gelesen. Wenn du ihn zurückschiebst, werden die Teile ab dem neuen Start erneut gelesen; wenn du ihn nach vorn schiebst, bleibt das bereits Gelesene erhalten.
- **Verarbeitete Teile**: die zuletzt gelesenen Teile. Ein Teil, dessen Text **seit der Verarbeitung geändert wurde** (du hast ihn bearbeitet oder überarbeitet), ist markiert; deswegen wird von selbst nichts neu verarbeitet. Die Schaltfläche einer Zeile, **Ab hier neu verarbeiten**, nimmt zurück, was aus diesem Teil und den späteren stammt (von dir von Hand geschriebene Einträge und Rücknahmen bleiben erhalten), und liest sie erneut.

Was das Plugin von selbst tut, wenn **Neue Teile automatisch verarbeiten** eingeschaltet ist (standardmäßig ist es aus):

- Bevor eine Generierung startet, werden die seit dem letzten Mal geschriebenen Teile verarbeitet, damit der Prompt weiß, wer wem begegnet ist. Ein Teil, der durch *Neu generieren* oder einen Wechsel ersetzt wird, bleibt unberührt. Mit Modell liest dies nur dann mit dem Modell, wenn **Vor Übernahme nachfragen** aus ist (siehe unten); andernfalls wird nur festgehalten, wer wem begegnet ist, und was das Modell findet, wird aus dem Reiter gestartet, weil dir mitten in einer Generierung nie eine Prüfung aufgezwungen wird.
- Wenn ein Teil **neu generiert** wird und der neue Text eintrifft, wird zurückgenommen, was aus dem alten Text erfahren wurde.
- Wenn du **wischst** oder zu einem anderen Zweig wechselst, springt der Cursor zurück zum letzten Teil, den beide Zweige gemeinsam haben.
<a id="reading-what-the-characters-learn"></a>

## Die Wissensinhalte auswerten

Aktiviere in den Einstellungen **Die Figuren lernen mit dem Modell lesen** („Read what the characters learn with the model“). Mit **Jetzt verarbeiten** fragt das Modell dann für die nächsten Teile der Geschichte, was jede verfolgte Figur, die darin vorkommt, **gelernt** hat: was sie gesehen, gehört, gelesen oder erzählt bekommen hat. Außerdem nennt es die Einträge, die die Geschichte **widerlegt** (*ist Thomas nie begegnet*, nachdem sie einander begegnen). Die Anfrage enthält die Teile (mit ihrem Blickwinkel und den anwesenden Figuren), was jede verfolgte Figur jetzt weiß und – je nach Wahl – das Lorebuch. Das Modell soll weglassen, was eine Person dieser Welt ohnehin weiß (wer der König ist), und was sich aus der Figur selbst ergibt (eine Sukkubus weiß, dass sie einen Schwanz hat). Über nichts berichten, dessen es nicht sicher ist. Eine verfolgte Figur, die in keinem der Teile anwesend ist, wird gar nicht erst gefragt.

Jeder Lauf ist eine Anfrage an das Modell. Wenn **Jetzt verarbeiten** mehr als drei davon bräuchte (ein langer, noch nicht verarbeiteter Abschnitt der Geschichte oder ein niedrig eingestelltes **Teile pro Lauf**), zeigt es zuerst an, wie viele Teile und Anfragen das sind und ungefähr wie viel Text durch das Modell läuft, und startet erst, wenn du bestätigst. Jede Anfrage trägt außerdem die Anweisungen, das Lorebuch und das Wissen der Figuren mit sich, die tatsächlichen Kosten liegen daher über dem reinen Textumfang.

Ist **Vor dem Anwenden fragen** ausgeschaltet, werden die Änderungen sofort angewendet, und jede wird im Protokoll **Automatisch angewendet** im Bereich „Verarbeitung“ des Reiters festgehalten: wer was bekommen oder verloren hat, aus welchem Teil. Das Protokoll ist nicht editierbar, aber jede Zeile hat eine Schaltfläche **Zurücknehmen**: Ein hinzugefügter Eintrag wird abgelehnt (er bleibt unter *Altes Wissen*, wie eine abgelehnte Ergänzung in der Prüfung), ein zurückgezogener Eintrag wird wieder aktiv. Eine Zeile, deren Eintrag du inzwischen von Hand bearbeitet, gelöscht oder zurückgezogen hast, bleibt unberührt. Das Protokoll behält die letzten 200 Änderungen.

Bei ausgeschaltetem **Vor dem Anwenden fragen** wird der neue Teil gelesen, sobald er endgültig ist: wenn die Generierung abgeschlossen ist oder, falls das Buch die automatische Annotation (und Neuentwurf) oder eine Übersetzung eingeschaltet hat, wenn diese fertig ist. So liest das Modell den Text, den du behältst, und arbeitet, während du weiterliest. Es ist ein Schritt von [was auf einen generierten Teil folgt](../books/story-editor.md#what-follows-a-generated-part). Die nächste Generierung findet das Wissen dann aktuell vor und wartet nicht. Was übrig bleibt (ein abgebrochener Lauf, ein geschlossenes Fenster), wird wie bisher vor der nächsten Generierung gelesen.

Während das Modell arbeitet, schwebt ein kleines **Fortschrittsfenster** über der Seite. Es ist **nicht modal**: Du kannst weiter lesen und die Geschichte bearbeiten, und du kannst es an seiner Titelzeile **ziehen**. Schließt du es, wird es nur ausgeblendet, die Arbeit läuft weiter (**Abbrechen** stoppt sie).

![Das Fortschrittsfenster](../../images/plugin-knowledgetracker-progress.png)

Während das Modell antwortet, zeigt das Fenster **Was das Modell schreibt** nach und nach an: zuerst seine Überlegungen, dann die Antwort, sodass eine lange Wartezeit bei einem langsamen Modell nicht als leerer Balken erscheint. Ist die Anfrage beendet, bleibt derselbe Text unter **Was das Modell gedacht hat** stehen (ein Block für jede Anfrage des Laufs, mit der jeweiligen Antwort), auch dann, wenn es **nichts** vorgeschlagen hat und keine Prüfung aufgeht. Der Reiter behält eine Schaltfläche **Letzter Lauf: was das Modell gedacht hat**, die das Fenster wieder öffnet, damit du siehst, warum nichts ergänzt wurde.

Hat das Modell geantwortet, sagt das Fenster es und zeigt **Änderungen prüfen**; dieselbe Schaltfläche findest du im Verarbeitungsbereich des Reiters (*3 Änderungen zu prüfen*). **Die Prüfung öffnet sich nie von selbst.** Sie ist eine Tabelle der Änderungen, nach Figur gruppiert:

![Die Änderungen prüfen](../../images/plugin-knowledgetracker-review.png)

Unter der Tabelle steht **Was das Modell gedacht hat** (anfangs zugeklappt). Dort sind die Überlegungen des Modells, die seiner Antwort vorausgehen, sofern das Modell sie mitschickt. Sie zeigen, warum eine Änderung vorgeschlagen wurde oder warum eine, die du erwartet hast, fehlt, sodass du die Änderung beurteilen kannst, bevor du sie anwendest. Ein Modell, das nicht überlegt, zeigt dort nichts.

| | |
|---|---|
| Das Kontrollkästchen | Ob die Änderung angewendet wird. Anfangs sind alle angehakt; jede Figur hat **Alle markieren** / **Alle abwählen**. |
| **Zurückziehen** / **Hinzufügen** | Ein Zurückziehen macht einen Eintrag zu einem alten (er bleibt erhalten, siehe [der Reiter](#the-knowledge-tab)). Eine Ergänzung ist ein neuer Eintrag, *Weiß* oder *Weiß nicht*. Text und Art einer Ergänzung können vor dem Anwenden **bearbeitet** werden. |
| Das Zitat | Die Worte des Teils, die es belegen, und in welchem Teil sie stehen. Ein Zitat, das in diesem Teil nicht gefunden wird, ist markiert: Das Modell könnte es erfunden haben, prüfe es, bevor du zustimmst. |

**Anwenden** wendet die angehakten Änderungen an und geht weiter. **Abbrechen** stoppt die ganze Serie und ändert nichts: Dieselben Teile werden beim nächsten Mal wieder vorgeschlagen. Eine Ergänzung, die du abwählst, wird **als altes Wissen mit ihrem Zitat behalten** (du kannst sie wiederherstellen); ein abgewähltes Zurückziehen lässt den Eintrag, wie er ist. Was das Modell vorgeschlagen hat und nicht verwendet werden konnte (eine Figur, nach der es nicht gefragt wurde, ein Eintrag, der nicht existiert, eine Änderung ohne Zitat, etwas, das die Figur schon weiß), wird unter *Weggelassen* aufgeführt. Was angewendet wurde, trägt seinen Teil, daher nimmt **Ab hier neu verarbeiten** es wieder zurück. Hat sich die Geschichte während der Prüfung geändert (ein anderer Zweig, ein gelöschter Teil), werden die Änderungen nicht angewendet, und du verarbeitest erneut.

Teile werden in **Läufen** zu je **Teile pro Lauf** gelesen (standardmäßig 1); einer, der nicht in den Kontext des Modells passt, wird auf das gekürzt, was hineinpasst. Eine Serie läuft Lauf für Lauf weiter, bis zum letzten festgelegten Teil; bei einer Serie, die dich fragt, wird ein Lauf, in dem das Modell nichts gefunden hat, übersprungen, ohne dich zu fragen. Ist **Vor dem Anwenden fragen** ausgeschaltet, werden die Änderungen sofort angewendet, und das Fenster führt ein Protokoll mit einer Zeile pro Lauf.

!!!
Die Anweisung wurde an einem Modell mit einigen wenigen Szenen getestet, nicht breit: Sie verhindert, dass Figuren etwas lernen, was sie nicht wahrgenommen haben, aber prüfe, was vorgeschlagen wird, besonders bei Figuren, die in einer langen Szene vorkommen. Die Anfrage geht an das Modell, das du unten wählst, mit den üblichen Kosten und dem üblichen Datenschutz dieses Dienstes.
!!!

<a id="tidying-the-knowledge"></a>

## Das Wissen aufräumen

Das Modell hält fest, was jede Figur erfährt, und über eine lange Geschichte wird die Liste länger, mit mehr Kleinkram, als du bei jeder Anfrage an das Modell mitschicken möchtest. **Wissen aufräumen** (neben **Jetzt verarbeiten**, braucht die Modelleinstellung) bittet das Modell, das, was eine Figur weiß, noch einmal zu lesen, mit den zuletzt verarbeiteten Teilen der Geschichte als Kontext, und

- einen Eintrag zu **streichen**, der es nicht wert ist, behalten zu werden: Nebensächliches, ein Detail einer Szene, das alle darin Anwesenden gesehen haben (die Geschichte hält es fest), etwas, das spätere Ereignisse überholt haben, etwas, das dasselbe sagt wie ein anderer Eintrag;
- **mehrere Einträge** zur selben Sache zu einem einzigen **zusammenzuführen**, der jede Tatsache behält.

Ein gestrichener oder zusammengeführter Eintrag wird nicht gelöscht: Er wird zu *Altem Wissen* und kann wiederhergestellt werden. Die Änderungen werden wie bei einer Lesung geprüft (**Vor dem Anwenden fragen** entscheidet), oder sofort angewendet und mit einer Schaltfläche **Zurücknehmen** im Protokoll festgehalten.

Der Aufräumlauf darf nur anfassen, was das Modell gelernt hat und du nicht **gesperrt** hast. Die Anfangseinträge, die, die du von Hand hinzugefügt oder bearbeitet hast, die gesperrten und die alten werden nie verändert (die ersten beiden Arten und die gesperrten werden dem Modell gezeigt, damit es sieht, was ein Eintrag doppeln würde). Ein Geheimnis, das seit langem in keinem Teil mehr vorkam, ist kein Grund, es zu streichen, und die Anweisung sagt das ausdrücklich; ist sich das Modell unsicher, behält es den Eintrag. Eine Figur mit weniger als **Aufräumen: ab so vielen Einträgen** (8) Einträgen, die das Modell gelernt hat, wird nicht aufgeräumt, und **Aufräumen: Teile als Kontext** (10) ist die Anzahl der zuletzt verarbeiteten Teile, die gelesen werden.

**Automatisch aufräumen** (eine Einstellung, standardmäßig aus) führt das Aufräumen von selbst am Ende eines Laufs aus, dessen Änderungen ohne Nachfrage angewendet werden (**Vor dem Anwenden fragen** ausgeschaltet, also auch das automatische Lesen der neuen Teile), sobald das Modell seit dem letzten Aufräumen **Nach so vielen Teilen aufräumen** (10) Teile gelesen hat. Was es ändert, wird angewendet und mit einer Schaltfläche **Zurücknehmen** im Protokoll festgehalten, und das Protokoll des Laufs vermerkt es. Einen Lauf, den du prüfst, startet nie selbst ein Aufräumen, und eine Figur mit zu wenig Einträgen wird übersprungen, ohne das Modell zu fragen.

Dasselbe von der Befehlsleiste aus: `knowledge-tidy` (`ask true`, um die Änderungen als Liste zu erhalten; der offene Reiter zeigt dann die Schaltfläche zur Prüfung), sowie `lock-knowledge` / `unlock-knowledge` mit der Figur und der Nummer des Eintrags.
<a id="settings"></a>

## Einstellungen

Das Zahnrad neben *Track a character*:

| Einstellung | |
|---|---|
| **In prompt** | Fügt den Block in den Prompt jeder Generierung ein. Der Button leuchtet grün, solange die Option aktiv ist. |
| **Depth** | Wo der Block platziert wird, gezählt in Nachrichten vom Ende des Prompts: `0` nach den Anweisungen des Zugs, `1` direkt davor. Er wird nie vor den System-Prompt gesetzt. |
| **Role** | Ob er als *System*-, *Assistant*- oder *User*-Nachricht gesendet wird. |
| **Say that nothing else is known** | Die Zeile zur geschlossenen Welt. |
| **Give what the characters know to the annotation** | Ein (Standard): [Mit KI kommentieren](#annotating-with-what-the-characters-know) erhält, was die verfolgten Figuren des Teils wussten, als er beginnt. |
| **Process new parts automatically** | Siehe [Verarbeitung](#processing). |
| **Read what the characters learn with the model** | Die Verarbeitung fragt das Modell, was jede verfolgte Figur erfahren hat, und schlägt es vor. Aus (Standard): Die Verarbeitung vermerkt nur, wer wem begegnet ist, was nichts kostet. |
| **Ask before applying** | Ein (Standard): Jeder Vorschlag wird zur Prüfung angezeigt. Aus: Er wird sofort angewendet und protokolliert. |
| **Let the model read earlier parts** | Während das Modell die neuen Teile liest, darf es den Teil lesen, in dem ein Eintrag erfahren wurde (die Einträge sagen „erfahren in Teil 12“), um zu prüfen, ob das Gelesene bestätigt, wiederholt oder widerspricht: etwa Hörensagen, das nun mit eigenen Augen gesehen wird. Pro Anfrage höchstens 5 Lesevorgänge, jeweils eines Teils. Der Text eines langen Teils wird auf das gekürzt, was in den Kontext passt, und die Prüfung zeigt, welche Teile gelesen wurden. Jeder Lesevorgang ist eine weitere Runde zum Modell, daher ist die Option standardmäßig aus. Ein Modell, das keine Funktionen aufrufen kann, liest die Anfrage wie bisher. |
| **Parts per run** | Wie viele Teile eine Anfrage liest. Größere Werte sind günstiger, ergeben aber eine längere Antwort und eine schwerere Prüfung. |
| **Tidy automatically**, **Tidy after this many parts** | Der Aufräumlauf läuft selbstständig am Ende eines Laufs, der Änderungen ohne Nachfrage anwendet, jeweils nach dieser Anzahl von Teilen (10). Standardmäßig aus. |
| **Tidy: parts of context** | Wie viele der zuletzt verarbeiteten Teile der Aufräumlauf liest (10). |
| **Tidy: from this many items** | Eine Figur mit weniger Einträgen, die das Modell erfahren hat (und die nicht gesperrt sind), wird nicht aufgeräumt (8). |
| **Lorebook for the model** | *Entries the parts activate* (Standard: die Einträge, die eine Generierung für diese Teile verwenden würde), *The whole lorebook* (jeder Eintrag; passt er nicht in den Kontext, werden die aktivierten gesendet und der Hinweis sagt es) oder *No lorebook*. |
| **Model / Protocol for the extraction** | Leer: das Modell des Buches. Dafür genügt oft ein günstigeres Modell, und auf einem Server, der nur einen Prompt-Cache hält, verhindert ein eigenes Modell, dass dieser ersetzt wird. |
| **Separators of this book** | Legt fest, wie das Feld der anwesenden Figuren in Namen, einen pro Zeile, aufgeteilt wird (ein Zeilenumbruch ist `\n`). Ein Wort wie `and` trennt zwischen Leerzeichen, daher bleibt *Jack and Jill* ein Name, bis du es hinzufügst. |
| **My separators (all books)** | Dasselbe für alle deine Bücher. Die eigenen Trenner des Buches haben Vorrang; ohne beide werden die eingebauten verwendet. |

## Prompts

Die Systemnachricht der Auswertung (**Knowledge extraction prompt**) und die des Aufräumlaufs (**Knowledge tidy prompt**) sind
[geerbte Einstellungen](../books/prompts.md#inherited-settings): Ein Buch verwendet seine eigene Fassung, sonst den nächsten Ordner darüber, der eine setzt,
sonst deine, sonst den eingebauten Text, der mit einem echten Modell getestet wurde.

Lege sie im Tab *Prompts* des Buches unter *Extension Settings*, im Dialog eines Ordners oder für alle deine Bücher unter
*Settings → Extension Settings → Knowledge Tracker* fest (leer zeigt den eingebauten Text in Grau). Behalte das JSON-Antwortformat des
eingebauten Textes bei: Das Plugin liest die Antwort daran. Die Anweisungen für `read_part` fügt das Plugin hinzu, wenn das Modell frühere Teile lesen darf,
daher muss dein Prompt sie nicht enthalten.

<a id="annotating-with-what-the-characters-know"></a>

## Mit dem Wissen der Figuren kommentieren

Der oben beschriebene Block verhindert, dass ein Modell eine Figur *beim Schreiben* allwissend macht. Dieselben Einträge helfen auch, die Fehler
aufzuspüren, die dennoch hineingerutscht sind. Wenn ein Modell [einen Teil kommentiert](../books/annotations.md#letting-a-model-annotate) (**Mit KI kommentieren**, ein Bereich von Teilen, die automatische Kommentierung, `/generate-annotation`),
teilt der Tracker ihm mit, was die verfolgten Figuren dieses Teils wussten, **wenn der Teil beginnt**. Der Editor markiert eine Figur, die etwas sagt, denkt oder tut, das sie nicht wissen kann: *Fiora begrüßt Thomas mit Namen; die Einträge besagen, dass sie ihm nie begegnet ist.* Die Notiz nennt den Sachverhalt.

- **Die Fakten sind die des Teils, nicht die des Buchendes.** Was eine Figur in einem späteren Teil erfährt, ist noch nicht bekannt; ein Eintrag, den ein späterer Teil zurücknimmt, war noch wahr. Was der Teil selbst mitbringt (was erfahren wird, wem begegnet wird, was in ihm nicht mehr gilt), wird gesondert aufgeführt, damit die erste Hälfte des Teils nicht gegen die zweite geprüft wird.
- **Welche Figuren.** Die verfolgten, die in den *Anwesenden Figuren* des Teils stehen, dessen Point of View sind oder in seinem Text genannt werden (ganzes Wort, Groß- und Kleinschreibung egal, auch Aliasse).
- **Einträge, die zu keinem Zeitpunkt Fakten sind**, bleiben außen vor: die, die du von Hand zurückgenommen hast, die, die der Aufräumlauf entfernt hat, und die, die du in einer Prüfung abgelehnt hast.
- **Nur, was erfasst ist.** Dem Modell wird gesagt, dass das, was die Einträge nicht enthalten, kein Fakt ist, damit es keine Figur für etwas markiert, von dem der Tracker schlicht nie erfahren hat. *Say that nothing else is known* fügt außerdem „(nobody else)“ zu den Begegnungen einer Figur hinzu.
- Wenn ein Teil vor dem geprüften vom Tracker nicht gelesen wurde, sagen die Fakten das: Was die Figuren dort erfahren haben, fehlt, und **Jetzt verarbeiten** davor ergibt eine bessere Prüfung.
- Die Fakten kosten Token in der Anfrage der Kommentierung, nur für die Teile mit einer verfolgten Figur. Schalte sie mit **Give what the characters know to the annotation** ab.

<a id="the-block-as-a-macro"></a>

## Der Block als Makro

Statt der automatischen Einfügung (oder zusätzlich dazu) kannst du den Block selbst mit dem Makro
[{%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands) platzieren. Das Argument ist die Liste der
Figuren der Szene, sodass eine Vorlage das Feld des Teils übergeben kann:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

Das Makro gibt den Block aus, unabhängig davon, ob **In prompt** aktiv ist. Schalte es daher ab, wenn du das Makro verwendest, sonst erhält das Modell den Block zweimal. Depth und Role gelten für das Makro nicht (der Block ist Teil der Nachricht, die das Makro enthält). Es gibt nichts aus, wenn keine der aufgelisteten Figuren verfolgt wird. Die Antwort eines Befehls-Makros wird beim Teil gespeichert, an den der neue Teil anknüpft, sodass eine zweite Generierung aus demselben Teil eine Änderung nicht sieht, die du dazwischen vorgenommen hast; eine Neugenerierung und ein Swipe fragen dagegen erneut.

<a id="commands"></a>

## Befehle

Für die [Kommandozeile](../commands/index.md) und für Agenten:

| Befehl | |
|---|---|
| `knowledge-status` | Die Einstellungen und den Stand der Verarbeitung. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Die verfolgten Figuren, adressiert über Name, Alias oder Nummer. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Die Einträge einer Figur, adressiert über ihre Nummer in `list-knowledge`. |
| `knowledge-process` | Verarbeitet jetzt (`parts`: höchstens so viele). Mit eingeschaltetem Modell gibt `ask true` zurück, was das Modell gefunden hat, als nummerierte Änderungen, und hält sie zurück: Noch wird nichts geändert. Der geöffnete Tab Knowledge zeigt einen Button **changes found by a command**, der die übliche Prüfung öffnet (Apply / Cancel); ein Agent wendet sie mit `apply-knowledge-changes` an. |
| `apply-knowledge-changes` | Wendet die zurückgehaltenen Änderungen an, alle oder die angegebenen Nummern (wie **Anwenden** in der Prüfung des Tabs). |
| `knowledge-tidy` | Der Aufräumlauf. Mit `ask true` gibt er die Änderungen als nummerierte Liste zurück und hält sie zurück (der Tab zeigt einen Button, um sie zu prüfen; `apply-knowledge-changes` wendet sie an). |
| `lock-knowledge`, `unlock-knowledge` | Sperrt einen Eintrag (Figur, Eintragsnummer), damit der Aufräumlauf ihn unberührt lässt, bzw. hebt die Sperre auf. |
| `knowledge-reprocess` | Verarbeitet ab einem Teil neu (sein Handle oder seine Position); fragt vorher nach. Mit eingeschaltetem Modell setzt er nur den Zustand zurück, `knowledge-process` liest die Teile dann erneut. |
| `knowledge-set-start` | Setzt den Startpunkt; ohne Teil den Anfang des Buches. |
| `knowledge-tracker-block` | Der Block als Text, für das Makro. |

Die Kommandozeile teilt ihre Argumente an Leerzeichen: Setze eine Liste von Figuren in Anführungszeichen (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Speicherung

Alles wird mit dem Buch gespeichert (in seinen Attributen), ist also Teil der [Buchsicherungen](../books/backups.md) und Kopien.
