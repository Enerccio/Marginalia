---
label: Wissensverfolgung
order: 11
---

# Wissensverfolgung

Verfolgt, was die Figuren deiner Geschichte wissen und was sie **nicht** wissen, und fügt das in den Prompt ein. Ein Modell neigt dazu, einer Figur Wissen über Personen zuzuschreiben, denen sie nie begegnet ist (ihren Namen, ihren Beruf, ihr Gesicht), sofern die Anweisungen dem nicht vorbeugen. Mit der Wissensverfolgung sagt der Prompt für jede verfolgte Figur der Szene, was diese Figur weiß und dass sie nichts darüber hinaus weiß.

!!!
Du entscheidest, wer verfolgt wird. Nur die Figuren, die du dem Reiter hinzufügst, werden dem Modell beschrieben, und nur solange sie zu den *anwesenden Figuren* des Teils gehören, der gerade geschrieben wird. Nichts wird aus dem Erzähltext erraten.
!!!

<a id="the-knowledge-tab"></a>

## Der Reiter Wissen

Die linke Seitenleiste des [Geschichteneditors](../books/story-editor.md) bekommt neben *Gliederung* einen Reiter **Wissen**. Alles wird im Buch gespeichert, sobald du es änderst.

![Der Reiter Wissen](../../images/plugin-knowledgetracker.png)

**Figur verfolgen** fragt nach einem **Namen** und optional nach **Alias-Namen**: den anderen Namen, unter denen die Figur im Feld *Anwesende Figuren* eines Teils geschrieben wird, durch Kommas getrennt. Ein Name oder Alias kann nur zu einer verfolgten Figur gehören.

Öffne eine Figur mit dem Pfeil auf ihrer Karte, um zu sehen, was sie weiß:

| | |
|---|---|
| **Weiß** (grün) / **Weiß nicht** (rot) | Ein kurzer Satz zu einer beliebigen Sache: einer Person, einem Ort, einem Geheimnis, einem Ereignis (*Peter weiß vom Komplott gegen den Herzog*, *Fiora weiß nicht, dass Thomas ihr Vater ist*). Schreibe ausdrücklich auf, was eine Figur **nicht** weiß, denn genau das respektiert das Modell. |
| Die Reihenfolge und **#12** | Das neueste Wissen steht oben (altes Wissen ebenso). Ein Eintrag, den das Modell gelernt hat, endet mit der kleinen Nummer des Teils, in dem er gelernt wurde (**#12**): Ein Klick scrollt die Geschichte zu diesem Teil und lässt ihn kurz aufleuchten (auf einem Telefon schließt er vorher die Schublade). Eigene Einträge haben keine Nummer, ebenso wenig einer, dessen Teil nicht auf dem aktiven Zweig liegt (ein anderer Zweig oder der Teil wurde gelöscht). |
| **Wissen hinzufügen** | Fügt einen Eintrag hinzu, sofort aktiv. Halte ihn bei **einem Gedanken**: Ein Eintrag, der zwei Dinge sagt (*weiß nicht, wer Thomas ist, ist ihm nie begegnet*), wird zurückgezogen, sobald eines davon nicht mehr zutrifft. Die Schaltfläche **Bearbeiten** ändert Text und Art. |
| **Zurückziehen** (der Kreis mit Schrägstrich) | Der Eintrag stimmt nicht mehr (*ist Thomas nicht begegnet*, nachdem sie sich getroffen haben). Er wird nicht gelöscht, sondern wird zu *altem Wissen* und nicht mehr an das Modell gesendet. |
| **Sperren** (das Vorhängeschloss, bei Einträgen, die das Modell gelernt hat) | Die [Aufräumdurchgang](#tidying-the-knowledge) sieht einen gesperrten Eintrag nie: Verwende sie für das, was bleiben muss, einen Verrat, ein Geheimnis. Die Geschichte kann einem gesperrten Eintrag trotzdem widersprechen (eine Figur, die ihr Gedächtnis verliert): Die Lesung der Geschichte zieht ihn wie üblich zurück. |
| **Löschen** | Entfernt einen Eintrag endgültig, nach einer Rückfrage. Zurückziehen behält ihn, verwende also im Zweifel das. |
| **Einträge auswählen** (neben **Wissen hinzufügen**) | Löscht schnell viele Einträge: Jeder Eintrag der Figur, auch die alten, bekommt ein Kontrollkästchen. **Alle auswählen** / **Auswahl aufheben** setzt oder entfernt die Häkchen, **Ausgewählte löschen (n)** entfernt die markierten endgültig nach einer Rückfrage, **Abbrechen** verlässt die Auswahl. |
| **Getroffen** | Die Personen, denen die Figur begegnet ist, vermerkt durch die [Verarbeitung](#processing). Die Verarbeitung kann einen Namen vermerken, der keine Person ist (ein Hinweis im Feld der anwesenden Figuren): Öffne **Bearbeiten** und ändere die Liste **Getroffen**, einen Namen nach dem anderen, durch Kommas getrennt. Die Namen, die du schreibst, bleiben erhalten, wenn die Geschichte erneut gelesen wird; einen Namen, den du entfernst, bekommt die Figur nur zurück, wenn der Teil, in dem er vorkam, erneut gelesen wird. |
| **Altes Wissen (n)** | Standardmäßig eingeklappt. Jeder alte Eintrag kann **kopiert**, **bearbeitet**, **wiederhergestellt** (er ist ab jetzt wieder aktiv) oder gelöscht werden. |

<a id="mass-changes"></a>

### Massenänderungen

Die Schaltfläche **Massenänderungen** (das Listensymbol neben **Figur verfolgen**) öffnet ein Fenster mit jeder verfolgten Figur und all ihren Einträgen, auch den alten, in einer Tabelle mit Kontrollkästchen: die Art, den Text, den Zustand (*Aktiv*, oder alt: *zurückgezogen*, *in einer Prüfung abgelehnt*, *aufgeräumt*), woher er stammt (*Anfangszustand*, *Von Hand geschrieben*, *Aus der Geschichte gelernt*) und den Teil, in dem er gelernt wurde. Setze Häkchen bei Einträgen in beliebig vielen Tabellen (das Kästchen in der Kopfzeile einer Tabelle hakt alle ihre Einträge an, **Alle auswählen** / **Auswahl aufheben** in der unteren Leiste alle Einträge aller Figuren), dann:

| | |
|---|---|
| **Ausgewählte zurückziehen (n)** | Die markierten aktiven Einträge werden zu altem Wissen, so wie es **Zurückziehen** bei einem einzelnen tut. |
| **Ausgewählte wiederherstellen (n)** | Die markierten alten Einträge sind ab jetzt wieder aktiv. |
| **Ausgewählte löschen (n)** | Entfernt die markierten Einträge endgültig, nach einer Rückfrage. |

Die Zahlen auf den Schaltflächen zeigen, wie viele der markierten Einträge die jeweilige Aktion ändert. Die Häkchen bleiben nach einer Änderung erhalten, sodass zurückgezogene Einträge danach gelöscht werden können.

**Als JSON exportieren** (oben im Fenster) lädt die verfolgten Figuren des Buches (Namen, Aliase, Einträge mit Art, Zustand, Herkunft und Sperre, die Personen, denen sie begegnet sind) als Datei herunter; nichts von der Verarbeitung ist dabei, daher passt sie zu jedem Buch. **Aus JSON importieren** liest eine solche Datei in das Buch ein und fügt nur hinzu: Eine Figur, deren Name im Buch bereits verfolgt wird (als Name oder Alias), erhält die Aliase, Einträge und Personen, die ihr noch fehlen (ein Eintrag mit gleicher Art und gleichem Text wird übersprungen), die anderen werden zu neuen verfolgten Figuren. Ein Alias, den bereits eine andere Figur hat, wird ausgelassen. Ein importierter Eintrag hat keinen Teil dieses Buches hinter sich, daher löscht eine erneute Verarbeitung ihn nie: Einer, den das Modell gelernt hat, gilt als von Hand geschrieben, ein alter bleibt alt. Die Meldung nach dem Import sagt, wie viele Figuren hinzugefügt oder aktualisiert wurden und wie viele Einträge hinzugefügt wurden oder schon vorhanden waren.

<a id="what-goes-into-the-prompt"></a>

## Was in den Prompt gelangt

Für jede verfolgte Figur, die zu den anwesenden Figuren gehört, bekommt der Prompt einen Block wie diesen:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Nur aktive Einträge werden gesendet. Die letzte Zeile (die Zeile **geschlossene Welt**) verhindert, dass das Modell einer Figur Wissen zuschreibt, das sie nicht haben kann. Sie betrifft, was die Geschichte erzählt (Personen, Orte, Geheimnisse, Pläne, Ereignisse), nicht das, was eine Person in dieser Welt ohnehin weiß, sodass eine Figur weiterhin ihre eigene Stadt kennt. Du kannst die Zeile in den Einstellungen abschalten. Eine verfolgte Figur, die nicht anwesend ist, bekommt überhaupt nichts, auch keine Zeile, die sagt, dass sie etwas nicht weiß.

<a id="who-is-present"></a>

### Wer ist anwesend

Der Block entsteht aus dem Feld **Anwesende Figuren** des Teils. Das Feld ist freier Text, daher wird es nachsichtig gelesen:

- Text in Klammern wird ignoriert: `Tomáš (tired), Mizuki` ist Tomáš und Mizuki;
- die Namen werden an den **Trennzeichen** getrennt (Standard: `,` `;` ein Zeilenumbruch, `/`, `|`, `、` `，` `；`);
- was nach ` - ` oder `:` steht, ist ein Hinweis und wird verworfen (`Mara - wounded`);
- Groß- und Kleinschreibung, Akzente und ein führendes *der*, *die* oder *das* spielen keine Rolle;
- ein Name muss **als Ganzes** passen: *Maras Bruder* ist nicht *Mara*. Verwende die Aliase, wenn eine Figur auf mehr als eine Weise geschrieben wird. Der Gedanke dahinter: Ein verpasster Treffer kostet einen Block, während ein falscher Treffer eine Figur beschreiben würde, die nicht in der Szene ist.

Das Feld *Blickwinkel* wird nicht für sich allein gelesen, schreibe die Figur, aus deren Blickwinkel erzählt wird, daher auch unter die anwesenden Figuren.
## Processing

**Processing** liest die Teile des aktiven Zweigs der Reihe nach, ab dem **Startpunkt**, und
führt einen **Cursor** (den zuletzt gelesenen Teil). Ohne das Modell hält es fest, wer wem begegnet ist: Eine verfolgte Figur ist jedem Namen begegnet, der zusammen mit ihr im Feld der anwesenden Figuren steht, und der Block listet diese auf. Mit dem Modell findet es außerdem, was die Figuren **erfahren** (siehe [Was die Figuren erfahren](#reading-what-the-characters-learn)).

![Das Processing-Panel](../../images/plugin-knowledgetracker-processing.png)

Das Panel oben im Tab zeigt, wo es steht. Es lädt sich von selbst neu, wenn ein neuer Teil endgültig ist (nach der automatischen Annotation und Überarbeitung, falls das Buch sie hat). Wenn du Teile auf andere Weise hinzufügst, bearbeitest oder löschst, drücke die Schaltfläche **Aktualisieren** (die Pfeile neben dem Zahnrad), um die Geschichte erneut zu lesen. Es enthält:

- **Process now**: liest die Teile, die noch nicht verarbeitet sind.
- **Process next part**: liest nur den ersten noch nicht verarbeiteten Teil, Schritt für Schritt (mit dem Modell: eine Anfrage für diesen Teil, unabhängig davon, was *Parts per run* vorgibt). Nutze ihn, um die Einstellungen auszuprobieren oder um zu sehen, was das Modell bei einem einzelnen Teil findet. Der Befehl `knowledge-process` mit `parts` auf 1 gesetzt tut dasselbe.
- **Start point**: der Teil, bei dem die Verarbeitung beginnt (standardmäßig *Start of the book*). Teile davor werden nie gelesen. Wird er zurück verschoben, werden die Teile ab dem neuen Start erneut gelesen; wird er weiter nach vorn verschoben, bleibt das bereits Gelesene erhalten.
- **Processed parts**: die zuletzt gelesenen Teile. Ein Teil, dessen Text sich **seit der Verarbeitung geändert hat** (du ihn bearbeitet oder überarbeitet hast), wird markiert; neu verarbeitet wird deswegen nichts von selbst. Die Schaltfläche einer Zeile, **Reprocess from here**, nimmt zurück, was aus diesem Teil und den späteren stammt (von Hand geschriebene Einträge und Zurücknahmen bleiben erhalten), und liest diese Teile erneut.

Was das Plugin von selbst tut, wenn **Process new parts automatically** eingeschaltet ist (standardmäßig ist es ausgeschaltet):

- Bevor eine Generierung startet, werden die seit dem letzten Mal geschriebenen Teile verarbeitet, damit der Prompt weiß, wer wem begegnet ist. Ein Teil, der gerade durch ein *Neu generieren* oder einen Swipe ersetzt wird, bleibt unberührt. Mit dem Modell liest dies nur dann mit dem Modell, wenn **Ask before applying** ausgeschaltet ist (siehe unten). Andernfalls wird nur notiert, wer wem begegnet ist, und was das Modell findet, wird aus dem Tab gestartet, weil eine Prüfung nie mitten in einer Generierung erzwungen wird.
- Wenn ein Teil **neu generiert** wird und der neue Text ankommt, wird zurückgenommen, was aus dem alten Text erfahren wurde.
- Wenn du einen Swipe machst oder zu einem anderen Zweig wechselst, springt der Cursor zurück zum letzten Teil, den beide Zweige gemeinsam haben.
<a id="reading-what-the-characters-learn"></a>

## Was die Figuren erfahren

Aktiviere in den Einstellungen **Was die Figuren mit dem Modell erfahren lassen**. Danach fragt **Jetzt verarbeiten** das Modell für die nächsten Teile der Geschichte, was jede verfolgte Figur, die darin vorkommt, **erfahren** hat: was sie gesehen, gehört, gelesen oder erzählt bekommen hat. Außerdem nennt es die Einträge, die die Geschichte **widerlegt** (*ist Thomas nie begegnet*, nachdem sie einander begegnet sind). Die Anfrage enthält die Teile (mit ihrer Erzählperspektive und den anwesenden Figuren), das, was jede verfolgte Figur jetzt weiß, und auf Wunsch das Lorebook. Das Modell wird angewiesen, auszulassen, was eine Person dieser Welt ohnehin weiß (wer der König ist), und was sich aus der Figur selbst ergibt (eine Sukkubus weiß, dass sie einen Schwanz hat). Außerdem soll es nichts melden, dessen es nicht sicher ist. Eine verfolgte Figur, die in keinem der Teile anwesend ist, wird gar nicht erst gefragt.

Jeder Lauf ist eine einzige Anfrage an das Modell. Wenn **Jetzt verarbeiten** mehr als drei davon bräuchte (ein langer, noch nicht verarbeiteter Abschnitt der Geschichte oder ein niedrig gesetztes **Teile pro Lauf**), nennt es zuerst, wie viele Teile und Anfragen das sind und ungefähr wie viel Text an das Modell geht, und startet erst, wenn du bestätigst. Jede Anfrage enthält außerdem die Anweisungen, das Lorebook und das Wissen der Figuren, die tatsächlichen Kosten liegen also höher als der reine Text.

Ist **Vor dem Anwenden fragen** ausgeschaltet, werden die Änderungen sofort angewendet, und jede wird im Protokoll **Automatisch angewendet** im Abschnitt Verarbeitung des Tabs festgehalten: wer was bekommen oder verloren hat, aus welchem Teil. Im Protokoll lässt sich nichts bearbeiten, aber jede Zeile hat einen Button **Zurücknehmen**: Ein hinzugefügter Eintrag wird abgelehnt (er bleibt unter *Altes Wissen*, wie eine abgelehnte Ergänzung in der Prüfung), ein zurückgezogener Eintrag wird wieder aktiv. Eine Zeile, deren Eintrag du seither von Hand bearbeitet, gelöscht oder zurückgezogen hast, bleibt unberührt. Das Protokoll behält die letzten 200 Änderungen.

Mit ausgeschaltetem **Vor dem Anwenden fragen** wird der neue Teil gelesen, sobald er feststeht: wenn die Generierung beendet ist oder, falls das Buch die automatische Annotation (und Neufassung) oder eine Übersetzung eingeschaltet hat, wenn diese fertig ist. So liest das Modell den Text, den du behältst, und arbeitet, während du weiterliest. Es ist ein Schritt von [dem, was dem Teil folgt](../books/story-editor.md#what-follows-a-generated-part). Die nächste Generierung findet das Wissen dann bereits aktuell vor und muss nicht warten. Was übrig bleibt (ein Lauf, den du abgebrochen hast, ein geschlossenes Fenster), wird wie bisher vor der nächsten Generierung gelesen.

Während das Modell arbeitet, schwebt ein kleines **Fortschrittsfeld** über der Seite. Es ist **nicht modal**: Du kannst weiterlesen und die Geschichte weiter bearbeiten, und du kannst es an seiner Titelzeile **ziehen**. Schließt du es, wird es nur ausgeblendet, die Arbeit läuft weiter (**Abbrechen** stoppt sie).

![Das Fortschrittsfeld](../../images/plugin-knowledgetracker-progress.png)

Während das Modell antwortet, zeigt das Feld **Was das Modell schreibt** nach und nach an: zuerst seine Überlegungen, dann die Antwort. So steht man bei einem langsamen Modell nicht vor einer leeren Leiste. Ist die Anfrage beendet, bleibt derselbe Text unter **Was das Modell gedacht hat** stehen (ein Block für jede Anfrage des Laufs, mit der Antwort), auch wenn es **nichts** vorgeschlagen hat und keine Prüfung öffnet. Der Tab behält dann einen Button **Letzter Lauf: was das Modell gedacht hat**, der das Feld wieder öffnet, damit du siehst, warum nichts ergänzt wurde.

Hat das Modell geantwortet, sagt das Feld das und zeigt **Änderungen prüfen**. Derselbe Button steht im Verarbeitungsfeld des Tabs (*3 Änderungen zu prüfen*). **Die Prüfung öffnet sich nie von selbst.** Sie ist eine Tabelle der Änderungen, nach Figur gruppiert:

![Die Änderungen prüfen](../../images/plugin-knowledgetracker-review.png)

Unter der Tabelle enthält **Was das Modell gedacht hat** (zunächst eingeklappt) die Überlegungen des Modells, die seiner Antwort vorausgehen, sofern das Modell sie mitschickt. Dort siehst du, warum eine Änderung vorgeschlagen wurde oder warum eine, die du erwartet hast, fehlt, sodass du sie beurteilen kannst, bevor du sie anwendest. Ein Modell, das nicht überlegt, zeigt dort nichts.

| | |
|---|---|
| Das Kästchen | Ob die Änderung angewendet wird. Zunächst sind alle angekreuzt; jede Figur hat **Alle ankreuzen** / **Alle abwählen**. |
| **Zurückziehen** / **Hinzufügen** | Ein Zurückziehen macht einen Eintrag alt (er bleibt erhalten, siehe [den Tab](#the-knowledge-tab)). Eine Ergänzung ist ein neuer Eintrag, *Weiß* oder *Weiß nicht*. Text und Art einer Ergänzung können vor dem Anwenden **bearbeitet** werden. |
| Das Zitat | Die Worte des Teils, die es belegen, und in welchem Teil sie stehen. Ein Zitat, das in diesem Teil nicht gefunden wird, ist markiert: Das Modell könnte es erfunden haben, prüfe es, bevor du es annimmst. |

**Anwenden** wendet die angekreuzten Änderungen an und geht weiter. **Abbrechen** stoppt die ganze Serie und ändert nichts: Dieselben Teile werden beim nächsten Mal wieder vorgeschlagen. Eine Ergänzung, die du abwählst, wird **als altes Wissen behalten** samt ihrem Zitat (du kannst sie wiederherstellen). Ein abgewähltes Zurückziehen lässt den Eintrag, wie er ist. Was das Modell vorgeschlagen hat und nicht verwendet werden konnte (eine Figur, nach der es nicht gefragt wurde, ein Eintrag, der nicht existiert, eine Änderung ohne Zitat, etwas, das die Figur schon weiß), wird unter *Weggelassen* aufgelistet. Was angewendet wurde, trägt seinen Teil, daher nimmt **Ab hier neu verarbeiten** es wieder zurück. Hat sich die Geschichte während der Prüfung geändert (ein anderer Zweig, ein gelöschter Teil), werden die Änderungen nicht angewendet, und du verarbeitest erneut.

Die Teile werden in **Läufen** zu je **Teile pro Lauf** gelesen (standardmäßig 1). Ein Teil, der nicht in den Kontext des Modells passt, wird auf das gekürzt, was hineinpasst. Eine Serie läuft Lauf für Lauf weiter, bis zum letzten festgelegten Teil. Bei einer Serie, die nachfragt, wird ein Lauf, in dem das Modell nichts gefunden hat, ohne Nachfrage übersprungen. Ist **Vor dem Anwenden fragen** ausgeschaltet, werden die Änderungen sofort angewendet, und das Feld führt ein Protokoll mit einer Zeile pro Lauf.

!!!
Die Anweisung wurde an einem Modell mit einigen wenigen Szenen getestet, nicht breit. Sie verhindert, dass Figuren etwas erfahren, das sie nicht wahrgenommen haben, aber prüfe die Vorschläge, besonders bei Figuren, die in einer langen Szene vorkommen. Die Anfrage geht an das Modell, das du unten wählst, mit den üblichen Kosten und dem üblichen Datenschutz dieses Dienstes.
!!!

<a id="tidying-the-knowledge"></a>

## Das Wissen aufräumen

Das Modell hält fest, was jede Figur erfährt, und über eine lange Geschichte wird die Liste länger, mit mehr Kleinkram, als du bei jeder Anfrage an das Modell schicken willst. **Wissen aufräumen** (neben **Jetzt verarbeiten**, braucht die Modelleinstellung) lässt das Modell lesen, was eine Figur weiß, erneut, mit den zuletzt verarbeiteten Teilen der Geschichte als Kontext, und

- einen Eintrag **streichen**, der nicht aufbewahrt werden muss: Nebensächliches, ein Detail einer Szene, das alle darin Anwesenden gesehen haben (die Geschichte hält es fest), etwas, das spätere Ereignisse überholt haben, etwas, das dasselbe sagt wie ein anderer Eintrag;
- mehrere Einträge zur selben Sache **zusammenführen** zu einem, der jede Tatsache behält.

Ein gestrichener oder zusammengeführter Eintrag wird nicht gelöscht: Er wird *Altes Wissen* und kann wiederhergestellt werden. Die Änderungen werden wie die einer Lektüre geprüft (**Vor dem Anwenden fragen** entscheidet), oder sofort angewendet und mit einem Button **Zurücknehmen** im Protokoll festgehalten.

Der Durchlauf darf nur anfassen, was die Figur gelernt hat und was du nicht **gesperrt** hast. Die Anfangseinträge, die du selbst hinzugefügt oder von Hand bearbeitet hast, die gesperrten und die alten Einträge werden nie geändert (die ersten beiden Arten und die gesperrten werden dem Modell gezeigt, damit es sehen kann, was ein Eintrag doppeln würde). Ein Geheimnis, das seit langer Zeit in keinem Teil mehr vorkam, ist kein Grund, es zu streichen, und die Anweisung sagt das ausdrücklich; ist das Modell unsicher, behält es den Eintrag. Eine Figur mit weniger als **Aufräumen: ab so vielen Einträgen** (8) vom Modell gelernten Einträgen wird nicht aufgeräumt, und **Aufräumen: Teile als Kontext** (10) ist, wie viele der zuletzt verarbeiteten Teile gelesen werden.

**Automatisch aufräumen** (eine Einstellung, standardmäßig aus) führt das Aufräumen am Ende eines Laufs von selbst aus, dessen Änderungen ohne Nachfrage angewendet werden (**Vor dem Anwenden fragen** ausgeschaltet, also auch das automatische Lesen der neuen Teile), sobald das Modell seit dem letzten Aufräumen **Nach so vielen Teilen aufräumen** (10) Teile gelesen hat. Was es ändert, wird angewendet und mit einem Button **Zurücknehmen** im Protokoll festgehalten, und das Protokoll des Laufs sagt es. Ein Lauf, den du prüfst, startet nie von selbst ein Aufräumen, und eine Figur mit zu wenigen Einträgen wird ohne Anfrage an das Modell übersprungen.

Dasselbe geht über die Befehlsleiste: `knowledge-tidy` (`ask true`, um die Änderungen als Liste zu erhalten; der offene Tab zeigt dann den Button zur Prüfung), und `lock-knowledge` / `unlock-knowledge` mit der Figur und der Nummer des Eintrags.
<a id="settings"></a>

## Einstellungen

Das Zahnradsymbol neben *Track a character*:

| Einstellung | |
|---|---|
| **In prompt** | Fügt den Block in den Prompt jeder Generierung ein. Die Schaltfläche wird grün, solange sie aktiv ist. |
| **Depth** | Wo er platziert wird, gezählt in Nachrichten vom Ende des Prompts: `0` nach den Anweisungen des Zugs, `1` direkt davor. Er wird nie vor den System-Prompt gesetzt. |
| **Role** | Ob er als *System*-, *Assistant*- oder *User*-Nachricht gesendet wird. |
| **Say that nothing else is known** | Die Zeile zur geschlossenen Welt. |
| **Give what the characters know to the annotation** | Ein (Standard): [Mit KI kommentieren](#annotating-with-what-the-characters-know) erhält, was die verfolgten Figuren des Teils wussten, als er beginnt. |
| **Process new parts automatically** | Siehe [Verarbeitung](#processing). |
| **Read what the characters learn with the model** | Die Verarbeitung fragt das Modell, was jede verfolgte Figur gelernt hat, und schlägt es vor. Aus (Standard): Die Verarbeitung vermerkt nur, wer wen getroffen hat, was nichts kostet. |
| **Ask before applying** | Ein (Standard): Jeder Vorschlag wird zur Prüfung angezeigt. Aus: Er wird sofort angewendet und protokolliert. |
| **Let the model read earlier parts** | Während das Modell die neuen Teile liest, darf es den Teil lesen, in dem ein Eintrag gelernt wurde (die Einträge sagen „learned in part 12"), um zu prüfen, ob das Gelesene es bestätigt, wiederholt oder widerspricht: etwa Hörensagen, das nun mit eigenen Augen gesehen wird. Höchstens 5 Lesevorgänge je Teil pro Anfrage, der Text eines langen Teils wird auf das Kontextfenster gekürzt, und die Prüfung zeigt, welche Teile gelesen wurden. Jedes Lesen ist eine weitere Runde zum Modell, daher ist es standardmäßig aus. Ein Modell, das keine Funktionen aufrufen kann, liest die Anfrage wie bisher. |
| **Parts per run** | Wie viele Teile eine Anfrage liest. Größer ist günstiger, erzeugt aber eine längere Antwort und eine schwierigere Prüfung. |
| **Tidy automatically**, **Tidy after this many parts** | Der Aufräumdurchlauf läuft von selbst am Ende eines Laufs, der Änderungen ohne Nachfrage anwendet, alle diese Anzahl Teile (10). Standardmäßig aus. |
| **Tidy: parts of context** | Wie viele der zuletzt verarbeiteten Teile der Aufräumdurchlauf liest (10). |
| **Tidy: from this many items** | Eine Figur mit weniger Einträgen, die das Modell gelernt hat (und die nicht gesperrt sind), wird nicht aufgeräumt (8). |
| **Lorebook for the model** | *Entries the parts activate* (Standard: die Einträge, die eine Generierung für diese Teile verwenden würde), *The whole lorebook* (jeder Eintrag; passt er nicht ins Kontextfenster, werden die aktivierten gesendet und der Hinweis sagt es) oder *No lorebook*. |
| **Model / Protocol for the extraction** | Leer: das Modell des Buchs. Dafür genügt oft ein günstigeres Modell, und auf einem Server, der nur einen Prompt-Cache hält, verhindert ein eigenes Modell, dass dieser ersetzt wird. |
| **Separators of this book** | Legt fest, wie das Feld „Anwesende Figuren“ in Namen aufgeteilt wird, einen pro Zeile (ein Zeilenumbruch ist `\n`). Ein Wort wie `and` trennt zwischen Leerzeichen, daher bleibt *Jack and Jill* ein Name, bis du es hinzufügst. |
| **My separators (all books)** | Dasselbe für alle deine Bücher. Die eigenen Trennzeichen des Buchs haben Vorrang; fehlen beide, werden die eingebauten verwendet. |

## Prompts

Der Systemtext der Auswertung (**Knowledge extraction prompt**) und der des Aufräumdurchlaufs (**Knowledge tidy prompt**) sind
[vererbte Einstellungen](../books/prompts.md#inherited-settings): Ein Buch verwendet seinen eigenen Text, sonst den des nächsthöheren Ordners, der einen setzt,
sonst deinen, sonst den eingebauten Text, der mit einem echten Modell getestet wurde.

Setze sie im Reiter *Prompts* des Buchs unter *Extension Settings*, im Dialog eines Ordners oder für alle deine Bücher unter
*Settings → Extension Settings → Knowledge Tracker* (leer zeigt den eingebauten Text in Grau an). Behalte das JSON-Antwortformat des
eingebauten Textes bei: Das Plugin liest die Antwort anhand davon. Die Anweisungen für `read_part` fügt das Plugin hinzu, wenn das Modell frühere Teile lesen darf,
daher muss dein Prompt sie nicht enthalten.

<a id="annotating-with-what-the-characters-know"></a>

## Annotieren mit dem, was die Figuren wissen

Der oben beschriebene Block verhindert, dass ein Modell eine Figur *während des Schreibens* allwissend macht. Dieselben Aufzeichnungen helfen auch,
Fehler zu finden, die trotzdem hineingerutscht sind. Wenn ein Modell [einen Teil annotiert](../books/annotations.md#letting-a-model-annotate) (**Mit KI kommentieren**, ein Bereich von Teilen, die
automatische Annotation, `/generate-annotation`), teilt der Tracker ihm mit, was die verfolgten Figuren dieses Teils wussten, **wenn der Teil beginnt**,
und der Editor markiert eine Figur, die etwas sagt, denkt oder tut, das sie nicht wissen kann: *Fiora grüßt Thomas beim Namen; die Aufzeichnungen sagen, dass sie ihn nie getroffen hat.* Der Hinweis nennt die Tatsache.

- **Die Tatsachen sind die des Teils, nicht die des Buchendes.** Was eine Figur in einem späteren Teil erfährt, ist noch nicht bekannt; ein Eintrag, den ein späterer Teil zurücknimmt, war bis dahin noch wahr. Was der Teil selbst bringt (was gelernt wird, wen man trifft, was in ihm nicht mehr gilt), wird gesondert aufgeführt, damit die erste Hälfte des Teils nicht gegen die zweite geprüft wird.
- **Welche Figuren.** Die verfolgten, die in den *Anwesenden Figuren* des Teils stehen, dessen Blickpunkt sind oder in seinem Text genannt werden (ganzes Wort, Groß- und Kleinschreibung egal, auch Alias).
- **Einträge, die zu keinem Zeitpunkt Tatsachen sind**, werden ausgelassen: die, die du von Hand zurückgenommen hast, die, die der Aufräumdurchlauf entfernt hat, und die, die du in einer Prüfung abgelehnt hast.
- **Nur, was aufgezeichnet ist.** Dem Modell wird gesagt, dass das, was die Aufzeichnungen nicht enthalten, keine Tatsache ist, damit es eine Figur nicht für etwas markiert, von dem der Tracker schlicht nichts erfahren hat. *Say that nothing else is known* fügt außerdem bei, wen eine Figur getroffen hat, „(nobody else)“ hinzu.
- Wenn ein Teil vor dem geprüften vom Tracker nicht gelesen wurde, weisen die Tatsachen darauf hin: Was die Figuren dort erfahren haben, fehlt, und **Jetzt verarbeiten** liefert zuerst eine bessere Prüfung.
- Die Tatsachen kosten Tokens in der Anfrage der Annotation, und zwar nur für Teile, die eine verfolgte Figur haben. Schalte sie mit **Give what the characters know to the annotation** aus.

<a id="the-block-as-a-macro"></a>

## Der Block als Makro

Statt (oder zusätzlich zur) automatischen Einfügung kannst du den Block selbst mit dem
[{%{`{{command::knowledge-tracker-block::<characters>}}`}%}-Makro](../templates/macros.md#commands) platzieren. Das Argument ist die Liste der
Figuren der Szene, sodass eine Vorlage das Feld des Teils übergeben kann:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

Das Makro gibt den Block aus, unabhängig davon, ob **In prompt** an ist. Schalte es daher aus, wenn du das Makro verwendest, sonst erhält das Modell ihn zweimal. Depth und Role gelten für das Makro nicht (der Block ist Teil der Nachricht, die das Makro enthält). Es gibt nichts aus, wenn keine der aufgeführten Figuren verfolgt wird. Die Antwort eines Befehlsmakros wird bei dem Teil gespeichert, an den der neue Teil anschließt, sodass eine zweite Generierung aus demselben Teil eine Änderung, die du dazwischen vorgenommen hast, nicht sieht; ein Neugenerieren oder Swipe fragt erneut.

<a id="commands"></a>

## Befehle

Für die [Kommandozeile](../commands/index.md) und für Agenten:

| Befehl | |
|---|---|
| `knowledge-status` | Die Einstellungen und den Stand der Verarbeitung. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Die verfolgten Figuren, angesprochen über Name, Alias oder Nummer. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Die Einträge einer Figur, angesprochen über ihre Nummer in `list-knowledge`. |
| `knowledge-process` | Verarbeitet jetzt (`parts`: höchstens so viele). Mit eingeschaltetem Modell gibt `ask true` das, was das Modell gefunden hat, als nummerierte Änderungen zurück und hält sie: Noch nichts wird verändert. Der geöffnete Reiter Knowledge zeigt eine Schaltfläche **changes found by a command**, die die übliche Prüfung (Apply / Cancel) öffnet; ein Agent wendet sie mit `apply-knowledge-changes` an. |
| `apply-knowledge-changes` | Wendet die gehaltenen Änderungen an, alle oder die angegebenen Nummern (dasselbe wie **Apply** in der Prüfung des Reiters). |
| `knowledge-tidy` | Der Aufräumdurchlauf. Mit `ask true` gibt er die Änderungen als nummerierte Liste zurück und hält sie (der Reiter zeigt eine Schaltfläche, um sie zu prüfen; `apply-knowledge-changes` wendet sie an). |
| `lock-knowledge`, `unlock-knowledge` | Sperren einen Eintrag (Figur, Eintragsnummer), damit der Aufräumdurchlauf ihn unberührt lässt, oder heben die Sperre auf. |
| `knowledge-reprocess` | Verarbeitet ab einem Teil neu (sein Handle oder seine Position); fragt zuerst nach. Mit eingeschaltetem Modell setzt es nur den Zustand zurück, `knowledge-process` liest die Teile erneut. |
| `knowledge-set-start` | Setzt den Startpunkt; ohne Teil den Anfang des Buchs. |
| `knowledge-tracker-block` | Der Block als Text, für das Makro. |

Die Kommandozeile teilt ihre Argumente an Leerzeichen; setze eine Liste von Figuren in Anführungszeichen (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Speicherung

Alles wird mit dem Buch gespeichert (in seinen Attributen), ist also Teil der [Buchsicherungen](../books/backups.md) und Kopien.
