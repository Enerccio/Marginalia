---
label: Wissens-Tracker
order: 11
---

# Wissens-Tracker

Hält nach, was die Figuren deiner Geschichte wissen und was sie **nicht** wissen, und fügt es in den Prompt ein. Ein Modell neigt dazu, einer Figur Wissen über Menschen zuzuschreiben, denen sie nie begegnet ist (ihren Namen, ihren Beruf, ihr Gesicht), solange die Anweisungen dies nicht verhindern. Mit dem Wissens-Tracker sagt der Prompt für jede verfolgte Figur in der Szene, was diese Figur weiß, und dass sie darüber hinaus nichts weiß.

!!!
Du wählst, wer verfolgt wird. Nur die Figuren, die du dem Reiter hinzufügst, werden dem Modell beschrieben, und nur solange sie unter den *Present Characters* des Teils sind, der gerade geschrieben wird. Nichts wird aus dem Erzähltext erraten.
!!!

<a id="the-knowledge-tab"></a>

## Der Knowledge-Reiter

Die linke Seitenleiste des [Geschichten-Editors](../books/story-editor.md) erhält neben *Outline* einen Reiter **Knowledge**. Alles wird im Buch gespeichert, sobald du etwas änderst.

![Der Reiter Knowledge](../../images/plugin-knowledgetracker.png)

**Track a character** fragt nach einem **Namen** und optional nach **Aliasen**: den anderen Namen, unter denen die Figur im Feld *Present Characters* eines Teils geschrieben wird, durch Kommas getrennt. Ein Name oder Alias kann nur zu einer verfolgten Figur gehören.

**Import from the Character Tracker** (das Personensymbol neben **Track a character**) beginnt, Figuren aus dem [Charakter-Tracker](character-tracker.md) desselben Buchs zu verfolgen. Es listet die Figuren auf, die das Buch noch nicht verfolgt (als Name oder Alias), mit ihren Kurzbeschreibungen, alle angekreuzt. Entferne den Haken bei denen, die du auslassen willst, und drücke **Import**. Nur die Namen werden übernommen: Kurzbeschreibung und Notiz beschreiben eine Figur, sie sind aber keine Dinge, die sie weiß. Mit **Their relationships count as meetings** hat eine importierte Figur die Personen getroffen, mit denen sie im Charakter-Tracker verwandt ist. Diese werden in ihre Liste **Has met** eingetragen, als hättest du sie selbst geschrieben, damit die Verarbeitung sie behält.

**Who has met whom** (das Handschlag-Symbol neben dem Import) legt die Begegnungen zwischen verfolgten Figuren auf einmal fest, ohne Namen einzutippen. Wähle links eine Figur aus. Rechts hat jede andere verfolgte Figur ein Kästchen, das angekreuzt ist, wenn die gewählte Figur sie getroffen hat. Ankreuzen fügt die Begegnung hinzu, Abwählen entfernt sie. Jede Änderung wird sofort gespeichert, und die Karten zeigen sie, wenn du das Fenster schließt. Eine Begegnung gilt nur in eine Richtung: Thomas für Fiora anzukreuzen setzt nicht Fiora für Thomas, außer **Mutual** ist angekreuzt. Dann wird jedes Ankreuzen oder Abwählen auf beiden Seiten zugleich ausgeführt, was schneller geht, wenn sich die beiden schon kennen. Das gilt für die Änderungen, die du bei angekreuzter Option machst, nicht für Begegnungen, die bereits gesetzt sind. Eine angekreuzte Begegnung zählt als deine (die Verarbeitung behält sie). Eine, die du abwählst und die die Geschichte belegt, kommt zurück, wenn dieser Teil erneut gelesen wird. Es braucht mindestens zwei verfolgte Figuren.

Öffne eine Figur mit dem Pfeil auf ihrer Karte, um zu sehen, was sie weiß:

| | |
|---|---|
| **Knows** (grün) / **Does not know** (rot) | Ein kurzer Satz über irgendetwas: eine Person, ein Ort, ein Geheimnis, ein Ereignis (*Peter weiß vom Komplott gegen den Herzog*, *Fiora weiß nicht, dass Thomas ihr Vater ist*). Schreibe ausdrücklich, was eine Figur **nicht** weiß, denn genau das respektiert das Modell. |
| Die Reihenfolge und **#12** | Das neueste Wissen steht oben, altes Wissen ebenfalls. Ein Eintrag, den das Modell gelernt hat, endet mit der kleinen Nummer des Teils, in dem er gelernt wurde (**#12**). Ein Klick scrollt die Geschichte zu diesem Teil und lässt ihn aufblinken (auf dem Telefon schließt sich zuvor die Schublade). Von dir geschriebene Einträge haben keine Nummer, und auch keiner, dessen Teil nicht im aktiven Zweig liegt (ein anderer Zweig, oder der Teil wurde gelöscht). |
| **Add knowledge** | Fügt einen Eintrag hinzu, sofort aktiv. Halte ihn bei **einem Gedanken**: Ein Eintrag, der zwei Dinge sagt (*weiß nicht, wer Thomas ist, ist ihm nie begegnet*), wird zurückgezogen, sobald eines davon nicht mehr stimmt. Die Schaltfläche **edit** ändert Text und Art. |
| **Retract** (der Kreis mit Schrägstrich) | Der Eintrag stimmt nicht mehr (*ist Thomas nicht begegnet*, nachdem sie sich getroffen haben). Er wird nicht gelöscht, sondern wird *altes Wissen* und nicht mehr an das Modell gesendet. |
| **Lock** (das Vorhängeschloss, bei Einträgen, die das Modell gelernt hat) | Der [Aufräumdurchgang](#tidying-the-knowledge) sieht nie einen gesperrten Eintrag. Verwende die Sperre für das, was bleiben muss, einen Verrat, ein Geheimnis. Die Geschichte kann einem gesperrten Eintrag trotzdem widersprechen (eine Figur, die ihr Gedächtnis verliert). Die Lektüre der Geschichte zieht ihn dann wie üblich zurück. |
| **Delete** | Entfernt einen Eintrag endgültig, nach einer Rückfrage. **Retract** behält ihn, verwende also im Zweifel lieber dies. |
| **Select items** (neben **Add knowledge**) | Viele Einträge schnell löschen: Jeder Eintrag der Figur, auch die alten, bekommt ein Kästchen. **Select all** / **Deselect all** kreuzt alle an bzw. ab. **Delete selected (n)** entfernt die angekreuzten endgültig nach einer Rückfrage, **Cancel** verlässt die Auswahl. |
| **Has met** | Die Personen, denen die Figur begegnet ist, vermerkt durch die [Verarbeitung](#processing). Die Verarbeitung kann einen Namen vermerken, der keine Person ist (eine Notiz im Feld *Present Characters*). Öffne **Edit** und ändere die Liste **Has met**, Name für Name durch Kommas getrennt. Die Namen, die du schreibst, bleiben, wenn die Geschichte erneut gelesen wird. Einer, den du entfernst, kommt nur zurück, wenn der Teil, in dem er gesehen wurde, erneut gelesen wird. |
| **Old knowledge (n)** | Standardmäßig eingeklappt. Jeder alte Eintrag sagt, warum er alt ist: *retracted* (mit der Nummer des Teils, dessen Lektüre ihn zurückgezogen hat, wenn das Modell es tat), *declined* in einer Prüfung oder *tidied*. Er kann **copied**, **edited** oder **restored** werden (er ist dann wieder aktiv, ab jetzt) oder gelöscht werden. |

<a id="mass-changes"></a>

### Massenänderungen

Die Schaltfläche **Mass changes** (das Listensymbol neben **Track a character**) öffnet ein Fenster mit jeder verfolgten Figur und allen ihren Einträgen, auch den alten, in einem Raster mit Kästchen. Angezeigt werden die Art, der Text, der Zustand (*Active*, oder alt: *retracted*, *declined in a review*, *tidied*), die Herkunft (*Initial state*, *Written by hand*, *Learned from the story*) und der Teil, in dem der Eintrag gelernt wurde. Kreuze Einträge in beliebig vielen Rastern an. Das Kästchen in der Kopfzeile eines Rasters kreuzt alle seine Einträge an, **Select all** / **Deselect all** in der unteren Leiste alle Einträge aller Figuren. Danach:

| | |
|---|---|
| **Retract selected (n)** | Die angekreuzten aktiven Einträge werden zu altem Wissen, wie es **Retract** bei einem Eintrag tut. |
| **Restore selected (n)** | Die angekreuzten alten Einträge sind wieder aktiv, ab jetzt. |
| **Delete selected (n)** | Entfernt die angekreuzten Einträge endgültig, nach einer Rückfrage. |
| **Copy selected (n)** | Kopiert die angekreuzten Einträge aller Figuren, um sie einer anderen Figur einzufügen. |
| **Paste (n)** (in der Kopfzeile jeder Figur) | Fügt die kopierten Einträge dieser Figur hinzu. Ein eingefügter Eintrag gilt als von Hand geschrieben (ein Anfangseintrag bleibt Anfangseintrag) und behält seine Art, seine Sperre und ob er alt ist. Einen Eintrag, den die Figur schon hat (gleiche Art und gleicher Text), wird übersprungen. Die kopierten Einträge bleiben bis zum nächsten Kopieren erhalten, auch nach dem Schließen des Fensters, sodass man sie mehreren Figuren einfügen kann. |

Die Zahlen auf den Schaltflächen zeigen, wie viele der angekreuzten Einträge jede ändert. Die Haken bleiben nach einer Änderung stehen, sodass zurückgezogene Einträge danach gelöscht werden können.

**Export to JSON** (oben im Fenster) lädt die verfolgten Figuren des Buchs als Datei herunter: Namen, Aliase, Einträge mit Art, Zustand, Herkunft und Sperre sowie die Personen, denen sie begegnet sind. Nichts von der Verarbeitung ist dabei, daher passt die Datei zu jedem Buch. **Import from JSON** liest eine solche Datei ins Buch und fügt nur hinzu. Eine Figur, deren Name das Buch schon verfolgt (als Name oder Alias), erhält die Aliase, Einträge und Personen, die ihr noch fehlen; ein Eintrag mit gleicher Art und gleichem Text wird übersprungen. Die anderen Figuren werden zu neuen verfolgten Figuren. Ein Alias, den bereits eine andere Figur hat, wird ausgelassen. Ein importierter Eintrag hat keinen Teil dieses Buchs hinter sich, daher verwirft eine erneute Verarbeitung ihn nie: Einer, den das Modell gelernt hat, gilt als von Hand geschrieben, ein alter bleibt alt. Die Meldung nach dem Import sagt, wie viele Figuren hinzugefügt oder aktualisiert wurden und wie viele Einträge hinzugefügt wurden oder schon vorhanden waren.
<a id="what-goes-into-the-prompt"></a>

## Was in den Prompt kommt

Für jede verfolgte Figur, die zu den anwesenden Figuren gehört, erhält der Prompt einen Block wie diesen:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Es werden nur aktive Einträge gesendet. Die letzte Zeile (die Zeile **closed world**) verhindert, dass das Modell einer Figur Wissen gibt, das sie nicht haben kann. Sie betrifft, was die Geschichte erzählt (Personen, Orte, Geheimnisse, Pläne, Ereignisse), nicht, was jemand in dieser Welt ohnehin weiß. Deshalb kennt eine Figur trotzdem ihre eigene Stadt. Du kannst die Zeile in den Einstellungen ausschalten. Eine verfolgte Figur, die nicht anwesend ist, erhält überhaupt nichts, auch keine Zeile, die sagt, dass sie etwas nicht weiß.

<a id="who-is-present"></a>

### Wer anwesend ist

Der Block wird aus dem Feld **Present Characters** des Teils erstellt. Das Feld ist freier Text und wird deshalb großzügig gelesen:

- Text in Klammern wird ignoriert: `Tomáš (tired), Mizuki` ergibt Tomáš und Mizuki;
- die Namen werden an den **separators** getrennt (Standard: `,` `;` ein Zeilenumbruch, `/`, `|`, `、` `，` `；`);
- alles nach ` - ` oder `:` ist eine Notiz und wird verworfen (`Mara - wounded`);
- Groß- und Kleinschreibung, Akzente und ein führendes *the* spielen keine Rolle;
- ein Name muss **als Ganzes** passen: *Mara's brother* ist nicht *Mara*. Verwende Aliasse, wenn eine Figur auf mehr als eine Art geschrieben wird. Der Grundgedanke: Ein verpasster Treffer kostet einen Block, ein falscher Treffer würde dagegen eine Figur beschreiben, die nicht in der Szene ist.

Das Feld *Point of View* wird nicht für sich allein gelesen. Schreibe deshalb die Figur mit der Erzählperspektive ebenfalls unter die anwesenden Figuren.

<a id="processing"></a>

## Verarbeitung

Das Plugin folgt der Geschichte. **Processing** liest die Teile des aktiven Branches der Reihe nach, ab dem **Startpunkt**, und führt einen **Cursor** (den zuletzt gelesenen Teil). Ohne Modell notiert es, wer wem begegnet ist: Eine verfolgte Figur ist allen Namen begegnet, die zusammen mit ihr im Feld „Present Characters“ stehen, und der Block listet sie auf. Mit Modell findet es außerdem, was die Figuren **erfahren** haben (siehe [Lesen, was die Figuren erfahren](#reading-what-the-characters-learn)).

![Das Verarbeitungspanel](../../images/plugin-knowledgetracker-processing.png)

Das Panel oben im Tab zeigt, wo die Verarbeitung steht. Es lädt sich von selbst neu, wenn ein Teil final ist (nach der automatischen Annotation und Überarbeitung, falls das Buch sie hat). Wenn du Teile auf andere Weise hinzufügst, bearbeitest oder löschst, drücke die Schaltfläche **Refresh** (die Pfeile neben dem Zahnrad), um die Geschichte erneut zu lesen. Es enthält:

- **Process now**: liest die Teile, die noch nicht verarbeitet sind.
- **Process next part**: liest nur den ersten noch nicht verarbeiteten Teil, Schritt für Schritt (mit Modell: eine Anfrage für diesen Teil, unabhängig davon, was *Parts per run* angibt). Verwende sie, um die Einstellungen auszuprobieren oder um zu sehen, was das Modell für einen einzelnen Teil findet. Der Befehl `knowledge-process` mit `parts` auf 1 macht dasselbe.
- **Start point**: der Teil, bei dem die Verarbeitung beginnt (standardmäßig *Start of the book*). Teile davor werden nie gelesen. Wird der Startpunkt nach hinten verschoben, werden die Teile ab dem neuen Start erneut gelesen. Wird er nach vorne verschoben, bleibt das bereits Gelesene erhalten.
- **Processed parts**: die zuletzt gelesenen Teile. Ein Teil, dessen Text **sich seit der Verarbeitung geändert hat** (du hast ihn bearbeitet oder überarbeitet), wird markiert. Allein deswegen wird aber nichts neu verarbeitet. Die Schaltfläche **Reprocess from here** in einer Zeile nimmt zurück, was von diesem Teil und den späteren stammt, und liest sie erneut. Von Hand geschriebene Einträge und Zurücknahmen bleiben erhalten.

Wenn du in der Figurenliste an diesen Schaltflächen vorbei nach unten scrollst, werden **Process now**, **Process next part** und **Tidy knowledge** auch in der Leiste oben im Tab angeboten. So kannst du verarbeiten, ohne wieder nach oben zu scrollen.

Was das Plugin von selbst tut, wenn **Process new parts automatically** aktiviert ist (standardmäßig ist es deaktiviert):

- Bevor eine Generierung startet, werden die seit dem letzten Mal geschriebenen Teile verarbeitet, damit der Prompt weiß, wer wem begegnet ist. Ein Teil, der durch ein *Regenerate* oder einen Swipe ersetzt wird, bleibt unangetastet. Mit Modell wird nur dann mit dem Modell gelesen, wenn **Ask before applying** deaktiviert ist (siehe unten). Andernfalls wird nur notiert, wer wem begegnet ist, und was das Modell findet, wird aus dem Tab heraus gestartet. Eine Prüfung wird dir nie mitten in einer Generierung aufgezwungen.
- Wenn ein Teil **regeneriert** wird und der neue Text eintrifft, wird zurückgenommen, was aus dem alten Text gelernt wurde.
- Wenn du **swipest** oder zu einem anderen Branch wechselst, springt der Cursor zurück zum letzten Teil, den beide Branches gemeinsam haben.
<a id="reading-what-the-characters-learn"></a>

## Lesen, was die Figuren erfahren

Aktiviere in den Einstellungen **Read what the characters learn with the model**. Danach fragt **Process now** das Modell für die nächsten
Teile der Geschichte, was jede verfolgte Figur, die darin vorkommt, **gelernt** hat: was sie gesehen, gehört, gelesen oder erzählt bekommen hat. Außerdem
nennt es die Einträge, denen die Geschichte **widerspricht** (*hat Thomas nie getroffen*, nachdem sie sich begegnen). Die Anfrage enthält die Teile (mit ihrer Erzählperspektive und den anwesenden Figuren), was jede verfolgte Figur jetzt weiß und, wenn du es wählst, das Lorebook. Das Modell soll weglassen, was eine Person aus dieser Welt ohnehin weiß (wer der König ist), und was sich aus der Figur selbst ergibt (eine Sukkubus weiß, dass sie einen Schwanz hat). Es soll außerdem nichts melden, dessen es nicht sicher ist. Eine verfolgte Figur, die in keinem der Teile vorkommt, wird gar nicht erst gefragt.

Jeder Durchlauf ist eine einzige Anfrage an das Modell. Wenn **Process now** mehr als drei davon bräuchte (ein langer Abschnitt der Geschichte, der noch nicht verarbeitet wurde, oder ein niedrig eingestelltes **Parts per run**), nennt es zuerst, wie viele Teile und Anfragen das sind, und grob, wie viel Text durch das Modell läuft. Es startet erst, wenn du bestätigst. Jede Anfrage enthält außerdem die Anweisungen, das Lorebook und das Wissen der Figuren, die Kosten sind also höher, als der Text allein vermuten lässt.

Wenn **Ask before applying** ausgeschaltet ist, werden die Änderungen sofort übernommen. Jede davon wird im Protokoll **Applied automatically** im Bereich Processing des Tabs festgehalten: wer was bekommen oder verloren hat, und aus welchem Teil. Nichts im Protokoll ist editierbar, aber jede Zeile hat einen Button **Revert**: Ein hinzugefügter Eintrag wird abgelehnt (er bleibt unter *Old knowledge*, wie eine abgelehnte Ergänzung im Review), ein zurückgezogener Eintrag wird wieder aktiv. Eine Zeile, deren Eintrag du inzwischen von Hand bearbeitet, gelöscht oder zurückgezogen hast, bleibt unberührt. Das Protokoll behält die letzten 200 Änderungen.

<a id="returning-to-an-earlier-point"></a>

### Zu einem früheren Punkt zurückkehren

Vor jedem Durchlauf, der das Wissen ändert (ein Durchlauf des Tabs mit oder ohne Review, das automatische Lesen neuer Teile, die Befehle `knowledge-process` und `knowledge-tidy`, ein Aufräumen und ein Lesen ohne das Modell), legt der Tracker einen **Checkpoint** an: den gesamten Zustand seiner Figuren, so wie der Durchlauf ihn vorfand, mit der Stelle, bis zu der er gelesen hatte (*Start of the book* vor dem ersten Durchlauf). **Checkpoints** im Processing-Bereich listet sie, neueste zuerst: den Teil, bis zu dem der Tracker gelesen hatte, die Uhrzeit, welche Art von Durchlauf danach kam und wie viel er hinzugefügt und zurückgezogen hat. Es werden nur Checkpoints aufgelistet, deren Teil auf dem aktiven Branch liegt.

**Return to this point** in einer Zeile macht diesen Durchlauf und alles danach rückgängig: Die Figuren und ihr Wissen, die verarbeiteten Teile und das Protokoll der automatischen Änderungen sind wieder die des Checkpoints. Der nächste Durchlauf liest die Teile nach seinem Teil erneut, auch die des rückgängig gemachten Durchlaufs. Deine Einstellungen und der Startpunkt bleiben, wie sie jetzt sind. Vorher wird nachgefragt, denn alles, was seit dem Checkpoint gelernt wurde, wird rückgängig gemacht, auch die Änderungen, die du seitdem von Hand vorgenommen hast. Was der Tracker bis dahin hatte, bleibt als Checkpoint erhalten (*before a return to a checkpoint*), sodass du dorthin zurückkehren kannst. Nutze das, wenn ein Durchlauf oder eine Reihe von Durchläufen schiefgegangen ist, oder nachdem du eine ältere Sicherung des Buches wiederhergestellt hast und der Tracker ab einem Punkt aufholen soll, dem du vertraust.

Der Tracker behält die letzten 20 Checkpoints eines Buches. Sie werden in deinen Dateien gespeichert (*Resources*, nach Buch gruppiert), nicht im Buch selbst, sodass Buchsicherungen und Kopien sie nicht enthalten.

Wenn **Ask before applying** ausgeschaltet ist, wird der neue Teil gelesen, sobald er endgültig ist: wenn die Generierung beendet ist oder, falls das Buch die automatische Annotation (und Neufassung) oder eine Übersetzung eingeschaltet hat, wenn diese fertig ist. So liest das Modell den Text, den du behältst, und arbeitet, während du weiterliest. Es ist ein Schritt von [what follows the part](../books/story-editor.md#what-follows-a-generated-part). Die nächste Generierung findet das Wissen dann aktuell vor und wartet nicht. Was übrig bleibt (ein Durchlauf, den du abgebrochen hast, ein Fenster, das du geschlossen hast), wird wie bisher vor der nächsten Generierung gelesen.

Während das Modell arbeitet, schwebt ein kleines **progress panel** über der Seite. Es ist **nicht modal**: Du kannst weiterlesen und die Geschichte weiter bearbeiten, und du kannst es an seinem Titel **drag**gen. Schließt du es, wird es ausgeblendet, die Arbeit läuft weiter (**Cancel** stoppt sie).

![The progress panel](../../images/plugin-knowledgetracker-progress.png)

Während das Modell antwortet, zeigt das Panel **What the model is writing** an, sobald der Text eintrifft: zuerst seine Überlegungen, dann die Antwort, sodass eine lange Wartezeit bei einem langsamen Modell nicht als leerer Balken erscheint. Wenn die Anfrage beendet ist, bleibt derselbe Text unter **What the model thought** stehen (ein Block für jede Anfrage des Durchlaufs, mit dem, was geantwortet wurde). Das gilt auch, wenn das Modell **nothing** vorschlug und kein Review geöffnet wird: Der Tab behält einen Button **Last run: what the model thought**, der das Panel erneut öffnet, damit du siehst, warum nichts hinzugefügt wurde.

Hat das Modell geantwortet, sagt das Panel das und zeigt **Review changes**; derselbe Button steht im Processing-Bereich des Tabs (*3 changes to review*). **Das Review öffnet sich nie von selbst.** Es ist eine Tabelle der Änderungen, nach Figur gruppiert:

![Reviewing the changes](../../images/plugin-knowledgetracker-review.png)

Unter der Tabelle steht **What the model thought** (zunächst geschlossen) mit den Überlegungen des Modells vor seiner Antwort, sofern das Modell sie mitsendet. Dort wird erklärt, warum eine Änderung vorgeschlagen wurde oder warum eine, die du erwartet hast, fehlt, sodass du die Änderung prüfen kannst, bevor du sie übernimmst. Ein Modell, das nicht überlegt, zeigt dort nichts.

| | |
|---|---|
| Das Kontrollkästchen | Ob die Änderung übernommen wird. Zunächst sind alle angekreuzt; jede Figur hat **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Ein Zurückziehen macht einen Eintrag zu einem alten (er bleibt erhalten, siehe [the tab](#the-knowledge-tab)). Eine Ergänzung ist ein neuer Eintrag, *Knows* oder *Does not know*. Text und Art einer Ergänzung können vor dem Übernehmen **bearbeitet** werden. |
| Das Zitat | Die Worte des Teils, die es belegen, und welcher Teil es ist. Ein Zitat, das in diesem Teil nicht gefunden wird, ist markiert: Das Modell könnte es erfunden haben; prüfe es, bevor du es annimmst. |

**Apply** übernimmt die angekreuzten Änderungen und geht weiter. **Cancel** stoppt die ganze Reihe und ändert nichts: Dieselben Teile werden beim nächsten Mal erneut vorgeschlagen. Eine Ergänzung, die du abwählst, wird **als altes Wissen behalten**, mit ihrem Zitat (du kannst sie wiederherstellen); eine nicht übernommene Zurücknahme lässt den Eintrag, wie er ist. Was das Modell vorschlug und nicht verwendet werden konnte (eine Figur, nach der es nicht gefragt worden war, ein Eintrag, der nicht existiert, eine Änderung ohne Zitat, etwas, das die Figur schon weiß), steht unter *Left out*. Was übernommen wurde, trägt seinen Teil, sodass **Reprocess from here** es wieder zurücknimmt. Ändert sich die Geschichte, während du prüfst (ein anderer Branch, ein gelöschter Teil), werden die Änderungen nicht übernommen, und du musst neu verarbeiten.

Teile werden in **runs** zu je **Parts per run** gelesen (standardmäßig 1); ein Teil, der nicht in den Kontext des Modells passt, wird auf das gekürzt, was passt. Eine Reihe läuft Durchlauf für Durchlauf weiter, bis zum letzten abgeschlossenen Teil; bei einer Reihe, die nachfragt, wird ein Durchlauf, in dem das Modell nichts fand, ohne Nachfrage übersprungen. Ist **Ask before applying** ausgeschaltet, werden die Änderungen sofort übernommen, und das Panel führt ein Protokoll mit einer Zeile pro Durchlauf.

!!!
Die Anweisung wurde an einem Modell auf wenigen Szenen getestet, nicht umfassend: Sie verhindert, dass Figuren etwas erfahren, was sie nicht wahrgenommen haben, aber prüfe das Vorgeschlagene, besonders bei Figuren, die in einer langen Szene vorkommen. Die Anfrage geht an das Modell, das du unten wählst, mit den üblichen Kosten und dem üblichen Datenschutz dieses Dienstes.
!!!
<a id="tidying-the-knowledge"></a>

## Wissensbestand aufräumen

Das Modell hält fest, was jede Figur erfährt. Bei einer langen Geschichte wird die Liste länger, mit mehr Kleinigkeiten, als man bei jeder Anfrage an das Modell schicken möchte. **Tidy knowledge** (neben **Process now**, braucht die Modelleinstellung) bittet das Modell, noch einmal zu lesen, was eine Figur weiß, mit den zuletzt verarbeiteten Teilen der Geschichte als Kontext, und

- **streichen** ein Element, das nicht wert ist, behalten zu werden: Nebensächlichkeiten, ein Detail einer Szene, das alle darin Anwesenden gesehen haben (die Geschichte hält es fest), etwas, das spätere Ereignisse überholt haben, etwas, das dasselbe sagt wie ein anderes Element;
- **zusammenführen** mehrere Elemente zum selben Thema zu einem einzigen, das jeden Fakt behält.

Ein gestrichenes oder zusammengeführtes Element wird nicht gelöscht: Es wird zu *Old knowledge* und kann wiederhergestellt werden. Die Änderungen werden wie die einer Lesung geprüft (**Ask before applying** entscheidet), oder sofort angewendet und mit einer **Revert**-Schaltfläche im Protokoll vermerkt.

Der Durchgang darf nur verändern, was das Modell gelernt hat und was Sie nicht **sperren**. Die Anfangselemente, die Elemente, die Sie von Hand hinzugefügt oder bearbeitet haben, die gesperrten und die alten Elemente werden nie geändert. Die ersten beiden Arten und die gesperrten werden dem Modell gezeigt, damit es sehen kann, was ein Element doppeln würde. Ein Geheimnis, das seit langer Zeit in keinem Teil erwähnt wurde, ist kein Grund, es zu streichen; das sagt der Prompt ausdrücklich. Ist das Modell unsicher, behält es das Element. Eine Figur mit weniger als **Tidy: from this many items** (8) vom Modell gelernten Elementen wird nicht aufgeräumt. **Tidy: parts of context** (10) gibt an, wie viele der zuletzt verarbeiteten Teile gelesen werden.

**Tidy automatically** (eine Einstellung, standardmäßig aus) startet den Durchgang selbstständig am Ende eines Laufs, dessen Änderungen ohne Rückfrage angewendet werden (**Ask before applying** aus, also auch das automatische Lesen der neuen Teile). Das geschieht, sobald das Modell seit dem letzten Aufräumen **Tidy after this many parts** (10) Teile gelesen hat. Was er ändert, wird angewendet und mit einer **Revert**-Schaltfläche im Protokoll vermerkt, und das Protokoll des Laufs sagt es. Ein Lauf, den Sie prüfen, startet nie selbst ein Aufräumen. Eine Figur mit zu wenigen Elementen wird übersprungen, ohne das Modell zu fragen.

Dasselbe über die Befehlsleiste: `knowledge-tidy` (`ask true`, um die Änderungen als Liste zu erhalten; der geöffnete Tab zeigt dann die Prüfschaltfläche), sowie `lock-knowledge` / `unlock-knowledge` mit der Figur und der Nummer des Elements.

<a id="settings"></a>

## Einstellungen

Das Zahnrad neben *Track a character*:

| Einstellung | |
|---|---|
| **In prompt** | Fügt den Block in den Prompt jeder Generierung ein. Die Schaltfläche leuchtet grün, solange er aktiv ist. |
| **Depth** | Wohin er kommt, gezählt in Nachrichten vom Ende des Prompts: `0` nach den Anweisungen der Runde, `1` direkt davor. Er kommt nie vor den System-Prompt. |
| **Role** | Ob er als *System*-, *Assistant*- oder *User*-Nachricht gesendet wird. |
| **Say that nothing else is known** | Die Zeile, die sagt, dass nichts anderes bekannt ist (geschlossene Welt). |
| **Give what the characters know to the annotation** | Ein (Standard): [Mit KI annotieren](#annotating-with-what-the-characters-know) erhält, was die verfolgten Figuren des Teils wussten, als er begann. |
| **Process new parts automatically** | Siehe [Verarbeitung](#processing). |
| **Read what the characters learn with the model** | Die Verarbeitung fragt das Modell, was jede verfolgte Figur gelernt hat, und schlägt es vor. Aus (Standard): Die Verarbeitung vermerkt nur, wer wem begegnet ist, was nichts kostet. |
| **Ask before applying** | Ein (Standard): Jeder Vorschlag wird zur Prüfung angezeigt. Aus: Er wird sofort angewendet und protokolliert. |
| **Let the model read earlier parts** | Während es die neuen Teile liest, darf das Modell den Teil lesen, in dem ein Element gelernt wurde (die Elemente sagen „learned in part 12“), um zu sehen, ob das Gelesene bestätigt, wiederholt oder widerspricht: zum Beispiel Hörensagen, das nun mit eigenen Augen gesehen wird. Höchstens 5 Lesevorgänge je Teil pro Anfrage. Der Text eines langen Teils wird auf das gekürzt, was in den Kontext passt, und die Prüfung zeigt, welche Teile gelesen wurden. Jedes Lesen ist eine weitere Runde zum Modell, deshalb ist es standardmäßig aus. Ein Modell, das keine Funktionen aufrufen kann, liest die Anfrage wie bisher. |
| **Parts per run** | Wie viele Teile eine Anfrage liest. Größer ist günstiger, liefert aber eine längere Antwort und eine schwerere Prüfung. |
| **Tidy automatically**, **Tidy after this many parts** | Der Aufräumdurchgang läuft selbstständig am Ende eines Laufs, der seine Änderungen ohne Rückfrage anwendet, und zwar alle so viele Teile (10). Standardmäßig aus. |
| **Tidy: parts of context** | Wie viele der zuletzt verarbeiteten Teile der Aufräumdurchgang liest (10). |
| **Tidy: from this many items** | Eine Figur mit weniger vom Modell gelernten (und nicht gesperrten) Elementen wird nicht aufgeräumt (8). |
| **Lorebook for the model** | *Entries the parts activate* (Standard: die Einträge, die eine Generierung für diese Teile verwenden würde), *The whole lorebook* (jeder Eintrag; passt er nicht in den Kontext, werden die aktivierten gesendet, und der Hinweis sagt es) oder *No lorebook*. |
| **Model / Protocol for the extraction** | Leer: das Modell des Buches. Für diese Aufgabe reicht ein günstigeres Modell. Auf einem Server, der einen Prompt-Cache für ein einziges Modell hält, verhindert ein eigenes Modell, dass dieser ersetzt wird. |
| **Separators of this book** | Wie das Feld der anwesenden Figuren in Namen aufgeteilt wird, einer pro Zeile (ein Zeilenumbruch ist `\n`). Ein Wort wie `and` trennt zwischen Leerzeichen, sodass *Jack and Jill* ein Name bleibt, bis Sie es hinzufügen. |
| **My separators (all books)** | Dasselbe für alle Ihre Bücher. Die eigenen Trenner des Buches haben Vorrang; ohne beide werden die eingebauten verwendet. |

**Use these settings for new books** (unter den Modelleinstellungen) speichert die Einstellungen dieses Buches, das Lesen mit dem Modell, einschließlich Modell und Protokoll, als jene, mit denen ein Buch startet, in dem der Knowledge Tracker noch nie verwendet wurde. So liest ein neues Buch ab dem ersten Lauf mit Ihrem Modell. Die Trenner des Buches bleiben bei ihm. **Forget the settings for new books** setzt auf die eingebauten Einstellungen zurück. Bücher, die den Tracker bereits verwenden, behalten ihre eigenen Einstellungen.

## Prompts

Die Systemnachricht der Lesung (**Knowledge extraction prompt**) und die des Aufräumdurchgangs (**Knowledge tidy prompt**) sind [vererbte Einstellungen](../books/prompts.md#inherited-settings): Ein Buch verwendet seine eigene, sonst den nächsten Ordner darüber, der eine setzt, sonst Ihre, sonst den eingebauten Text, der mit einem echten Modell getestet wurde.

Setzen Sie sie im Tab *Prompts* des Buches unter *Extension Settings*, im Dialog eines Ordners oder für alle Ihre Bücher unter *Settings → Extension Settings → Knowledge Tracker* (leer zeigt den eingebauten Text in Grau). Behalten Sie das JSON-Antwortformat des eingebauten Textes bei, denn das Plugin liest die Antwort daran. Die Anweisungen für `read_part` fügt das Plugin hinzu, wenn das Modell frühere Teile lesen darf. Ihr Prompt muss sie daher nicht enthalten.
<a id="annotating-with-what-the-characters-know"></a>

## Annotieren mit dem, was die Figuren wissen

Der Block oben hindert das Modell daran, eine Figur beim Schreiben allwissend werden zu lassen. Dieselben Aufzeichnungen helfen auch, die Fehler zu finden, die trotzdem durchrutschen. Wenn ein Modell einen Teil [annotiert](../books/annotations.md#letting-a-model-annotate) (**Annotate with AI**, ein Bereich von Teilen, die automatische Annotation, `/generate-annotation`), teilt der Tracker ihm mit, was die verfolgten Figuren dieses Teils wussten, **als der Teil beginnt**. Der Editor markiert eine Figur, die etwas sagt, denkt oder tut, das sie nicht wissen kann: *Fiora begrüßt Thomas mit Namen; die Aufzeichnungen sagen, dass sie ihn nie getroffen hat.* Die Notiz nennt die Tatsache.

- **Die Fakten sind die des Teils, nicht die des Buchendes.** Was eine Figur in einem späteren Teil erfährt, ist noch nicht bekannt; ein Eintrag, den ein späterer Teil zurücknimmt, war noch wahr. Was der Teil selbst mitbringt (was erfahren wird, wen man trifft, was in ihm nicht mehr gilt), wird gesondert aufgeführt, damit die erste Hälfte des Teils nicht gegen die zweite geprüft wird.
- **Welche Figuren.** Die verfolgten, die in den *Present Characters* des Teils stehen, dessen Erzählperspektive sie sind oder die im Text des Teils genannt werden (ganzes Wort, Groß- und Kleinschreibung egal, auch Aliasse).
- **Einträge, die zu keinem Zeitpunkt Fakten sind**, bleiben außen vor: die, die du von Hand zurückgenommen hast, die, die der Aufräumdurchlauf entfernt hat, und die, die du bei einer Prüfung abgelehnt hast.
- **Nur, was aufgezeichnet ist.** Dem Modell wird gesagt, dass das, was die Aufzeichnungen nicht festhalten, kein Fakt ist. So markiert es eine Figur nicht wegen einer Sache, von der der Tracker schlicht nie erfahren hat. *Say that nothing else is known* fügt außerdem bei, wen eine Figur getroffen hat, "(niemand sonst)" hinzu.
- Wenn ein Teil vor dem geprüften vom Tracker nicht gelesen wurde, sagen die Fakten das: Was die Figuren dort erfahren haben, fehlt, und **Process now** liefert zuerst eine bessere Prüfung.
- Die Fakten kosten Tokens in der Anfrage der Annotation, nur für Teile mit einer verfolgten Figur. Schalte sie mit **Give what the characters know to the annotation** ab.

<a id="the-block-as-a-macro"></a>

## Der Block als Makro

Statt (oder zusätzlich zum) automatischen Einfügen kannst du den Block selbst mit dem [{%{`{{command::knowledge-tracker-block::<characters>}}`}%}-Makro](../templates/macros.md#commands) setzen. Das Argument ist die Liste der Figuren der Szene, sodass eine Vorlage das Feld des Teils übergeben kann:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

Das Makro gibt den Block aus, unabhängig davon, ob **In prompt** eingeschaltet ist. Schalte es daher aus, wenn du das Makro verwendest, sonst erhält das Modell den Block zweimal. Tiefe und Rolle gelten für das Makro nicht (der Block ist Teil der Nachricht, die das Makro enthält). Es gibt nichts aus, wenn keine der aufgelisteten Figuren verfolgt wird. Die Antwort eines Kommando-Makros wird bei dem Teil gespeichert, aus dem der neue Teil fortführt. Eine zweite Generierung aus demselben Teil sieht daher keine Änderung, die du dazwischen vorgenommen hast. Neugenerieren und Swipe fragen dagegen erneut ab.

<a id="commands"></a>

## Befehle

Für die [Kommandozeile](../commands/index.md) und für Agenten:

| Befehl | |
|---|---|
| `knowledge-status` | Die Einstellungen und der Stand der Verarbeitung. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Die verfolgten Figuren, angesprochen über Name, Alias oder Nummer. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Die Einträge einer Figur, angesprochen über ihre Nummer in `list-knowledge`. |
| `knowledge-process` | Verarbeitet jetzt (`parts`: höchstens so viele). Mit eingeschaltetem Modell gibt `ask true` zurück, was das Modell gefunden hat, als nummerierte Änderungen, und hält sie zurück: Noch wird nichts geändert. Der geöffnete Reiter Knowledge zeigt eine Schaltfläche **changes found by a command**, die die übliche Prüfung öffnet (Apply / Cancel). Ein Agent wendet die Änderungen mit `apply-knowledge-changes` an. |
| `apply-knowledge-changes` | Wendet die zurückgehaltenen Änderungen an, alle oder die angegebenen Nummern (wie **Apply** in der Prüfung des Reiters). |
| `knowledge-tidy` | Der Aufräumdurchlauf. Mit `ask true` gibt er die Änderungen als nummerierte Liste zurück und hält sie zurück (der Reiter zeigt eine Schaltfläche, um sie zu prüfen; `apply-knowledge-changes` wendet sie an). |
| `lock-knowledge`, `unlock-knowledge` | Sperrt einen Eintrag (Figur, Eintragsnummer), damit der Aufräumdurchlauf ihn nicht anfasst, bzw. hebt die Sperre auf. |
| `knowledge-reprocess` | Verarbeitet neu ab einem Teil (sein Handle oder seine Position); fragt vorher nach. Mit eingeschaltetem Modell setzt es nur den Zustand zurück, `knowledge-process` liest die Teile neu ein. |
| `knowledge-set-start` | Setzt den Startpunkt; ohne Teil den Anfang des Buches. |
| `knowledge-tracker-block` | Der Block als Text, für das Makro. |

Die Kommandozeile teilt ihre Argumente an Leerzeichen. Setze eine Liste von Figuren deshalb in Anführungszeichen (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Speicherung

Alles wird mit dem Buch gespeichert (in seinen Attributen). Es ist daher Teil der [Buchsicherungen](../books/backups.md) und Kopien. Die [Checkpoints](#returning-to-an-earlier-point) gehören nicht dazu: Es sind deine eigenen Dateien, die mit dem Buch verknüpft sind.
