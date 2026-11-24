---
label: Zustandsverfolgung
order: 29
---

# Zustandsverfolgung

Verfolgt den aktuellen **Zustand** Ihrer Figuren: was sie tragen, was sie bei sich haben, welche Waffe sie gerade in der Hand halten, ihre Verletzungen,
ihre Stimmung oder alles andere, das Sie festlegen. Er fügt den Zustand der Figuren in der Szene in den Prompt ein. Ein Modell vergisst leicht, dass das Schwert vor zwei Kapiteln verloren ging oder dass der Umhang durchnässt ist. Mit der Zustandsverfolgung sagt der Prompt zu jeder verfolgten Figur in der Szene, was jetzt von ihr zutrifft.

!!!
Sie bestimmen, wer verfolgt wird und was über die Figuren verfolgt wird. Nur die Figuren, die Sie in den Reiter aufnehmen, werden dem Modell beschrieben, und nur solange sie zu den *Present Characters* des gerade geschriebenen Teils gehören. Sie können den Zustand selbst pflegen oder ein Modell die Geschichte lesen und die Änderungen vorschlagen lassen (siehe [Dem Verlauf der Geschichte folgen](#following-the-story)).
!!!

<a id="categories"></a>

## Kategorien

Eine **Kategorie** ist eine Sache, die über eine Figur verfolgt werden kann. Jede Kategorie hat:

- einen **Namen**, der auch im Prompt so genannt wird (*Wearing*, *Weapon at hand*, *Carrying*);
- eine **Art**:
  - **Einzelwert**: ein kurzer Wert; ein neuer Wert ersetzt den alten (die Waffe in der Hand, der Ort).
  - **Liste**: Einträge, die einzeln hinzugefügt und entfernt werden (was man bei sich hat, die Verletzungen).
  - **Beschreibung**: ein Text, der als Ganzes neu geschrieben wird (die Kleidung, die Stimmung).
- eine **Anweisung für das Modell**: was in die Kategorie gehört und was nicht. Das Modell liest sie, wenn es der Geschichte folgt.
- **Nur, was im Text steht**: Eingeschaltet ändert sich der Wert nur, wenn der Text es sagt; ausgeschaltet auch bei offensichtlichen Folgen (nass nach einem Bad im See).
- **Bei leerem Wert „none“ sagen**: Der Prompt schreibt *none*, wenn die Figur keinen Wert hat (eine verlorene Waffe), statt die Kategorie wegzulassen.

<a id="my-categories"></a>

### Meine Kategorien

Ihre eigenen Kategorien finden Sie unter **Settings → Extension Settings → State Tracker**. Bücher übernehmen ihre Kategorien von dort. Solange Sie nichts ändern, haben Sie fünf eingebaute: *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* und *Mood*. Kategorien dort hinzufügen, bearbeiten und löschen und im Einstellungsbildschirm auf **Save** drücken. **Reset to the built-in categories** stellt die fünf eingebauten wieder her.

<a id="the-categories-of-a-book"></a>

### Die Kategorien eines Buchs

Ein Buch behält eine **eigene Kopie** jeder Kategorie, die es verwendet, damit eine Sicherung oder ein Export des Buchs alles enthält, und das Löschen einer Ihrer Kategorien beschädigt nie ein Buch. Die Schaltfläche **Categories of this book** im Reiter State listet sie auf:

- **Add from my categories** kopiert eine Ihrer Kategorien in das Buch.
- **New category for this book** erstellt eine Kategorie, die nur in diesem Buch existiert (die Ladung eines Schiffs, eine magische Abstimmung). Eine solche Kategorie ist als *Only in this book* markiert. **Save to my categories** fügt sie Ihren Kategorien hinzu, sodass auch andere Bücher sie verwenden können.
- Eine Kopie, die nicht mehr mit Ihrer Kategorie übereinstimmt, ist als *Differs from your category* markiert. **Update from my categories** übernimmt Ihre Version. Ohne Sie wird nichts aktualisiert, denn eine geänderte Anweisung ändert, was das Modell tut.
- Eine Kategorie zu bearbeiten ändert sie nur in diesem Buch. Ihre Art lässt sich nicht ändern, solange Figuren Werte dafür haben.
- Wird eine Kategorie aus dem Buch gelöscht, werden die Werte aller Figuren darin gelöscht.

<a id="the-state-tab"></a>

## Der Reiter State

In der linken Seitenleiste des [Story-Editors](../books/story-editor.md) erscheint ein Reiter **State**. Alles wird im Buch gespeichert, sobald Sie es ändern.

![Der Reiter State mit einer verfolgten Figur](../../images/plugin-statetracker.png)

![Der Verlauf eines Werts](../../images/plugin-statetracker-history.png)

**Track a character** fragt nach einem **name**, optional nach **aliases** (den anderen Namen, unter denen die Figur im Feld *Present Characters* eines Teils geschrieben wird, durch Kommas getrennt) und den **tracked categories**. Die Liste bietet die Kategorien des Buchs und Ihre eigenen an; wählen Sie eine Ihrer eigenen, wird sie in das Buch kopiert. Ein Pferd kann nur *Injuries* verfolgen, während die Heldin alles verfolgt.

**Import from the Character Tracker** (das Personensymbol neben **Track a character**) beginnt, Figuren aus dem [Character Tracker](character-tracker.md) desselben Buchs zu verfolgen. Es listet die Figuren auf, die das Buch noch nicht verfolgt (als Name oder Alias), mit ihren Taglines, alle angehakt. Entfernen Sie den Haken bei denen, die ausgelassen werden sollen, prüfen Sie die **tracked categories**, die jede importierte Figur erhält (die des Buchs und Ihre eigenen, wie oben), und drücken Sie **Import**. Nur die Namen werden übernommen; eine Figur kann später mit dem Stift andere Kategorien erhalten.

Jede verfolgte Figur hat eine Karte, zunächst geschlossen. Öffnen Sie sie mit dem Pfeil auf der Karte, um ihre Kategorien zu sehen, jede mit ihrem aktuellen Wert oder ihren Werten:

- **+** setzt den Wert einer Einzelwert- oder Beschreibungskategorie (der aktuelle Wert wandert in den Verlauf) oder fügt einer Liste einen Eintrag hinzu.
- Der Stift bearbeitet einen Wert, das Kreuz **entfernt** ihn: Er trifft nicht mehr zu, wandert daher in den Verlauf und wird nicht an das Modell gesendet.
- Das Schloss markiert einen Wert als **locked** (eine Narbe, das Familienschwert). Das [Aufräumen](#tidying-the-state) lässt gesperrte Werte unberührt; die Geschichte kann sie trotzdem ändern.
- **History** zeigt die früheren Werte der Kategorie und warum sie endeten (*replaced*, *removed* oder *tidied*) und, wenn das Modell sie beendet hat, den Teil, aus dem es das las. **Restore** macht einen früheren Wert wieder aktuell (bei einer Einzelwert- oder Beschreibungskategorie wandert der aktuelle an dessen Stelle in den Verlauf). Delete entfernt einen Wert endgültig.

Der Stift der Figur bearbeitet ihren Namen, ihre Aliase und Kategorien. Das Entfernen des Hakens bei einer Kategorie blendet sie aus, behält aber ihre Werte; Anhaken bringt sie zurück. Der Papierkorb beendet die Verfolgung der Figur und löscht ihren gesamten Zustand.
<a id="following-the-story"></a>

## Die Handlung verfolgen

Das Panel **Processing** oben im Reiter State lässt ein Modell die Geschichte lesen und herausfinden, was sich verändert hat: einen Umhang, der im Fluss durchnässt wurde, eine Laterne, die an jemand anderen übergeben wurde, ein gezogenes Schwert, einen verlorenen Brief. Es liest die Teile des aktiven Zweigs der Reihe nach, ab dem **Start point** (standardmäßig der Anfang des Buches; Teile davor werden nie gelesen), und merkt sich, wie weit es gekommen ist.

![Die Verarbeitungseinstellungen](../../images/plugin-statetracker-settings.png)

![Die Prüfung der vorgeschlagenen Änderungen](../../images/plugin-statetracker-review.png)

- **Process now** liest die Teile, die noch nicht verarbeitet sind, **Process next part** nur den nächsten. Bei einer langen Reihe wird zuerst gefragt, wie viele Anfragen es werden. Wenn du in der Liste der Figuren über diese Schaltflächen hinaus nach unten scrollst, werden **Process now**, **Process next part** und **Tidy the state** auch in der Leiste oben im Reiter angeboten.
- Das Modell erhält die verfolgten Figuren, die in den Teilen vorkommen, mit ihren Kategorien, der Anweisung zu jeder und den aktuellen Werten, und antwortet mit den Änderungen. Jede Änderung braucht ein Zitat aus dem Teil. Änderungen, die nicht passen (eine Kategorie, die die Figur nicht verfolgt, ein Wert, den sie schon hat, ein Zitat aus einem Teil, der nicht existiert), werden ausgelassen.
- Ein Panel zeigt den Fortschritt und was das Modell währenddessen schreibt; du kannst weiterlesen und weiterschreiben. Bei aktivem **Ask before applying** (der Standard) wartet der Lauf auf dich: **Review changes** zeigt die Änderungen je Figur, mit dem bisherigen Wert durchgestrichen und dem neuen Wert, den du korrigieren kannst, sowie dem Zitat. Entferne den Haken bei allem, was falsch ist, und drücke **Apply**; **Cancel** lässt alles, wie es war, und dieselben Teile werden beim nächsten Mal wieder vorgeschlagen.
- Ein vom Modell gelesener Wert zeigt den Teil, aus dem er stammt. **Processed parts** listet die gelesenen Teile auf; **Reprocess from here** nimmt alles zurück, was ab diesem Teil und den späteren gelesen wurde (von dir geschriebene oder korrigierte Werte bleiben), und liest sie erneut. Tu das, nachdem du einen früheren Teil umgeschrieben hast. Ein neu generierter Teil, ein Swipe oder ein anderer Zweig nehmen von selbst zurück, was aus Teilen stammt, die nicht mehr vorhanden sind.

<a id="the-story-clock"></a>

### Die Story-Uhr

Mit eingeschalteter **Story clock** (in den Einstellungen, standardmäßig aus) verfolgt das Modell zusätzlich die Zeit der Geschichte. In derselben Anfrage wie der Zustand meldet es für jeden Teil, was dieser Teil über die Zeit sagt: *am nächsten Morgen*, *drei Tage später*, *in dieser Nacht*, *der vierzigste Tag seit deinem Erwachen*, *am zwanzigsten April*, ein vages *die Wochen verschwammen ineinander* (als ungefähr markiert), oder einen Rückblick, eine Erinnerung oder einen Traum aus der Vergangenheit, der die Uhr nicht weiterstellt und am Zustand nichts ändert. Das Modell zählt nicht: Das Plugin addiert die Tage.

![Die Story-Uhr in den Verarbeitungseinstellungen](../../images/plugin-statetracker-clock.png)

![Die Zeit eines Teils von Hand korrigieren](../../images/plugin-statetracker-clock-dialog.png)

- **Day 1 is the day of** benennt das Ereignis, ab dem die Tage gezählt werden (*das Erwachen*): Die Zeit lautet dann *Tag 14 nach dem Erwachen, Abend*. **Day at the start point**, **Time of day** und **Date at the start point** geben die Zeit vor dem ersten gelesenen Teil an; mit einem Datum zählt die Uhr auch Daten (*20. April 1453*).
- Das Panel Processing zeigt jetzt die **story time** an, und jeder verarbeitete Teil zeigt die Zeit nach ihm. Ein genannter Tag oder ein genanntes Datum ersetzt eine Schätzung davor.
- Die Uhr-Schaltfläche eines Teils (oder der Zeitleiste der Story, bei dem letzten Teil) **setzt die Zeit von Hand**: einen festen Punkt (Tag, Tageszeit, Datum), den das erneute Lesen des Teils nicht ersetzt und von dem die Uhr weiterzählt. **Remove the fixed point** im selben Dialog lässt das Lesen des Modells wieder zählen.
- Bei der Prüfung steht die Zeit zuerst, unter **Story time**, mit dem Zitat jedes Teils; entferne den Haken bei allem, was falsch ist.
- Der Prompt-Block beginnt mit der Zeit (*Story time: Tag 14 nach dem Erwachen, Abend.*), auch wenn keine verfolgte Figur in der Szene ist.
- Ein Wert merkt sich den Tag der Geschichte, an dem er gesetzt wurde (*Tag 4*). Das Aufräumen sieht, wie lange das her ist (*eine Tasse Tee, vor 9 Tagen gesetzt*), und entfernt, was Zeit hatte zu verblassen.
- Bei eingeschalteter Uhr wird jeder Teil an das Modell geschickt, auch die ohne verfolgte Figur, weil jeder Teil die Zeit verschieben kann.
- Ein erneutes Verarbeiten, ein Neugenerieren oder ein anderer Zweig nehmen zurück, was aus den nicht mehr vorhandenen Teilen gelesen wurde, wie beim Zustand; feste Punkte, die du gesetzt hast, bleiben.

<a id="the-log-of-automatic-changes"></a>

### Das Protokoll automatischer Änderungen

Was ohne Prüfung übernommen wird (ein Lauf mit ausgeschaltetem **Ask before applying**, das automatische Aufräumen, ein Befehl ohne `ask`), wird im Panel Processing unter **Applied automatically** festgehalten: wer, welche Kategorie, aus welchem Teil (oder *tidied*) und was sich geändert hat (*+ lantern*, *− rope*, *short sword → axe*). Am Protokoll lässt sich nichts bearbeiten, aber jede Zeile hat eine Schaltfläche **Revert**. Sie wirkt, als hättest du die Änderung bei der Prüfung abgewählt: Der Wert, den die Änderung gesetzt hat, wird gelöscht, und der Wert, auf den sie endete, ist wieder aktuell. Eine Zeile bleibt unberührt, wenn du den Wert seitdem von Hand geändert hast (bearbeitet, entfernt, wiederhergestellt oder gelöscht oder einer einzelnen Kategorie einen anderen Wert gegeben hast). Das Protokoll behält die letzten 200 Änderungen.

![Das Protokoll automatischer Änderungen](../../images/plugin-statetracker-log.png)

<a id="returning-to-an-earlier-point"></a>

### Zu einem früheren Punkt zurückkehren

Vor jedem Lauf, der Änderungen übernimmt (ein Lauf des Reiters mit oder ohne Prüfung, das automatische Lesen neuer Teile, die Befehle, ein Aufräumen), legt der Tracker einen **checkpoint** an: den gesamten Zustand seiner Figuren, wie der Lauf ihn vorfand, mit der Story-Uhr und dem Punkt, bis zu dem er gelesen hatte (*Start of the book* vor dem ersten Lauf). **Checkpoints** im Panel Processing listet sie auf, neueste zuerst: den Teil, bis zu dem der Tracker gelesen hatte, die Zeit, welche Art Lauf danach kam und wie viele Änderungen er übernahm. Nur Checkpoints, deren Teil im aktiven Zweig liegt, werden aufgelistet.

![Eine Änderung zurücknehmen](../../images/plugin-statetracker-reverted.png)

**Return to this point** in einer Zeile macht diesen Lauf und alles danach rückgängig: Die Werte der Figuren (aktuelle und vergangene), die Markierungen der Story-Uhr, die verarbeiteten Teile und das Protokoll automatischer Änderungen sind die des Checkpoints, und der nächste Lauf liest die Teile nach seinem Teil wieder, auch die des rückgängig gemachten Laufs. Was der Tracker bis dahin hatte, wird als Checkpoint aufbewahrt (*before a return to a checkpoint*), sodass du dorthin zurückkehren kannst. Deine Einstellungen, der Startpunkt, die Einstellungen der Uhr und die Kategorien des Buchs bleiben wie jetzt (eine Kategorie, die du seitdem gelöscht hast, kommt zurück, wenn die Werte des Checkpoints sie verwenden). Vorher wird nachgefragt, weil alles, was nach dem Checkpoint gelesen wurde, rückgängig gemacht wird, und seither auch deine Änderungen von Hand. Verwende es, wenn eine Reihe von Läufen schiefgelaufen ist oder nachdem du eine ältere Sicherung des Buchs wiederhergestellt hast und der Tracker ab einem Punkt, dem du vertraust, aufholen soll.

Der Tracker behält die letzten 20 Checkpoints eines Buchs. Sie werden unter deinen Dateien gespeichert (*Resources*, nach Buch gruppiert), nicht im Buch, daher enthalten Sicherungen und Kopien des Buchs sie nicht.

<a id="tidying-the-state"></a>

### Den Zustand aufräumen

Manche Zustände verblassen, ohne dass die Geschichte es sagt: eine Stimmung, nasse Kleidung, Atemlosigkeit, eine Tasse, die nach dem Essen noch lange in der Hand gehalten wird. **Tidy the state** im Panel Processing fragt das Modell danach. Es liest die zuletzt verarbeiteten Teile mit den aktuellen Werten der verfolgten Figuren, die in diesen Teilen vorkommen, und kann zu jedem Wert:

![Die Prüfung einer Aufräumaktion](../../images/plugin-statetracker-tidy-review.png)

- **drop** ihn: er stimmt nicht mehr oder ist nicht mehr behaltenswert;
- **update** ihn: die Teile zeigen, dass er sich weiterentwickelt hat (die Wut ist in Erleichterung umgeschlagen);
- **merge** Listeneinträge, die dasselbe sind (*rope* und *coil of rope*).

Werte, die es nicht erwähnt, bleiben wie sie sind, ebenso gesperrte Werte. Jede Änderung kommt mit einer Begründung. Ein Wert, den das Aufräumen schreibt (eine Aktualisierung oder ein Zusammenführen), wird mit *by the tidy* und dem letzten gelesenen Teil markiert, da die Geschichte ihn nicht genannt hat; wenn du ihn bearbeitest, wird er deiner. Du prüfst ihn wie jeden anderen Lauf (oder er wird sofort übernommen, wenn **Ask before applying** ausgeschaltet ist). Ein Wert, den das Aufräumen beendet, geht in den Verlauf als *tidied*, und **Reprocess from here** ab dem letzten Teil, der vor dem Aufräumen gelesen wurde, macht es rückgängig. Figuren, die in den letzten Teilen nicht vorkommen, bleiben unberührt, sodass ein Seil, das jemand trägt, der seit zwanzig Kapiteln abwesend ist, erhalten bleibt.

Ist **Tidy automatically** eingeschaltet (Standard), läuft das Aufräumen auch von selbst am Ende eines Laufs, der Änderungen ohne Nachfrage übernimmt, sobald seit dem letzten Aufräumen **Tidy after this many parts** Teile (standardmäßig 10) gelesen wurden. Ein Lauf, den du prüfst, startet nie ein Aufräumen, damit dir keine zweite Prüfung aufgezwungen wird.

Im Einstellungsdialog (das Zahnrad):

- **Process new parts automatically**: Ist ein Teil fertig, liest ihn das Modell (spätestens vor der nächsten Generierung). Ist **Ask before applying** eingeschaltet, wird er nur gelesen, wenn du **Process now** drückst: Mitten im Schreiben wird dir nie eine Prüfung aufgezwungen.
- **Ask before applying**: Ausgeschaltet werden die Änderungen sofort übernommen und im Protokoll festgehalten (siehe unten).
- **Parts per run**: wie viele Teile eine Anfrage liest.
- **Tidy automatically** und **Tidy after this many parts**: siehe [Den Zustand aufräumen](#tidying-the-state). Das Aufräumen liest auch so viele der zuletzt verarbeiteten Teile.
- **Lorebook for the model**: die Einträge, die die Teile aktivieren (Standard), das gesamte Lorebook oder keines.
- **Model for the extraction** und **Protocol for the extraction**: leer bedeutet das Modell des Buchs.
- **Use these settings for new books** speichert die Einstellungen dieses Buchs (einschließlich Modell und Protokoll; die Trennzeichen bleiben beim Buch) als Vorgabe, mit der ein Buch startet, in dem der State Tracker noch nie benutzt wurde. **Forget the settings for new books** setzt die eingebauten Einstellungen wieder ein. Bücher, die den Tracker bereits verwenden, behalten ihre eigenen Einstellungen.
## Prompts

Die System-Nachricht des Lesens (**State extraction prompt**) und die des Aufräumens (**State tidy prompt**) sind [geerbte Einstellungen](../books/prompts.md#inherited-settings): Ein Buch verwendet seine eigene, sonst den nächsthöheren Ordner, der eine setzt, sonst Ihre eigene, sonst den eingebauten Text in Ihrer Prompt-Sprache (der englische wurde mit einem echten Modell getestet).

Legen Sie sie im Reiter *Prompts* des Buches unter *Extension Settings* fest, im Dialog eines Ordners oder für alle Ihre Bücher unter *Settings → Extension Settings → State Tracker* (leer zeigt den eingebauten Text in Grau). Behalten Sie das JSON-Antwortformat des eingebauten Textes bei, denn das Plugin liest die Antwort daran. Die Regeln der [Story-Uhr](#the-story-clock) fügt das Plugin nach dem Extraktions-Prompt hinzu, wenn das Buch der Zeit folgt. Ihr Prompt muss sie daher nicht enthalten.

<a id="in-the-prompt"></a>

## Im Prompt

Das Zahnrad-Symbol enthält die Einstellungen des Buches:

- **In prompt** fügt den Zustand der verfolgten Figuren der Szene in den Prompt jeder Generierung ein (das Zahnrad wird grün, solange es eingeschaltet ist).
- **Depth** und **Role** legen fest, wohin er kommt, wie bei der Author's Note: *Depth* ist die Anzahl der Nachrichten danach (0 nach den Anweisungen des Zugs, 1 direkt davor).
- **Name separators** legen fest, wie das Feld *Present Characters* getrennt wird (standardmäßig `,` `;` `/` `|` und Zeilenumbrüche). Notizen in Klammern werden ignoriert, sodass `Mara (asleep), Fiora` beide findet.

Der Prompt erhält die Figuren der Szene, die etwas zu sagen haben:

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Sie können denselben Text überall in einer Vorlage mit dem Makro {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} einfügen (jede Liste von Namen funktioniert als Argument). Das Makro gibt den Zustand aus, unabhängig davon, ob **In prompt** eingeschaltet ist. Schalten Sie es daher aus, wenn Sie das Makro verwenden, sonst erhält das Modell den Zustand doppelt.

<a id="commands"></a>

## Befehle

Für die Befehlsleiste und für Agenten:

| Befehl | Was er tut |
|---|---|
| `state-status` | Die Einstellungen, die Kategorien des Buches und jene aus Ihrer Sammlung, von denen das Buch keine Kopie hat. |
| `list-state-characters` | Die verfolgten Figuren mit ihren Kategorien. |
| `track-state-character` | Beginnt, eine Figur zu verfolgen: Name, Aliasse, Kategorien nach Name. |
| `stop-tracking-state-character` | Beendet die Verfolgung einer Figur und löscht ihren Zustand (fragt vorher nach). |
| `list-character-state` | Die Werte einer Figur, aktuelle und frühere, nummeriert. |
| `set-character-state` | Setzt einen Wert: Figur, Kategorie, Text. |
| `remove-character-state` | Entfernt einen Wert anhand seiner Nummer: Er wandert in den Verlauf. |
| `state-tracker-block` | Der Text des Prompts für eine Liste von Figuren (das Makro). |
| `state-process` | Lässt das Modell die nächsten Teile lesen und wendet die Änderungen an. Mit `ask true` gibt es sie stattdessen nummeriert zurück. |
| `apply-state-changes` | Wendet die Änderungen an, die `state-process` oder `state-tidy` zurückgegeben hat, alle oder die angegebenen Nummern. |
| `set-story-time` | Setzt die Story-Zeit nach einem verarbeiteten Teil von Hand (Tag, Tageszeit, Datum). Ohne eines davon wird der feste Zeitpunkt entfernt. |
| `state-tidy` | Lässt das Modell die verblassten Werte löschen, aktualisieren oder zusammenführen. Mit `ask true` gibt es die Änderungen nummeriert zurück. |
| `state-reprocess` | Nimmt zurück, was aus einem Teil und den späteren gelesen wurde (fragt vorher nach). |
| `state-set-start` | Legt den Teil fest, bei dem das Lesen beginnt. |
