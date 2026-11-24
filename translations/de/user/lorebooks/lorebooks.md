---
label: Lorebooks erstellen
order: 100
---

# Lorebooks erstellen

<a id="the-lorebook-editor"></a>

## Der Lorebook-Editor

Öffne den Reiter **Lorebooks**. Der Editor zeigt jeweils ein Lorebook. Wähle es im Feld **Lorebook** oben aus.
Lorebooks gehören zu deinem Konto: Andere Benutzer sehen sie nicht, es sei denn, du [teilst](#sharing-a-lorebook) eines.

Die Werkzeugleiste hat zwei Zeilen, die in einem schmalen Fenster auf weitere Zeilen umbrechen: die erste für die Lorebooks (**Lorebook** bis **Tag lorebooks**), die zweite für die Einträge des ausgewählten Lorebooks (**Add Entry** bis **Refresh**).

| Button | |
|---|---|
| **Lorebook** | Wählt das Lorebook zum Bearbeiten aus. |
| **Only root lorebooks** | Zeigt nur die Lorebooks, die kein Sub-Lorebook eines anderen sind (das ausgewählte Lorebook bleibt in der Liste). |
| **Add Lorebook** | Legt ein neues Lorebook namens *New Lorebook* an und wählt es aus. |
| Clone (Symbol mit zwei Blättern) | Erstellt eine Kopie des ausgewählten Lorebooks: fragt nach dem Namen (dem Namen des Originals mit *(Copy)*) und wählt die Kopie aus. Die Kopie hat dieselben Einträge (mit ihren Einstellungen, Tags und negativen Tags), Tags, Beschreibung und Erweiterungsdaten. Ihre Sub-Lorebooks werden nicht kopiert, die Kopie verwendet dieselben. Das Original bleibt unverändert. |
| **Delete Lorebook** | Löscht das ausgewählte Lorebook (nach Bestätigung). |
| Import (Symbol mit Datei) | **Import Marginalia Lorebook**, **Import from SillyTavern**, **Import from archive** (ein ZIP-Archiv mit `.md`-Dateien) oder **Import from other user** (ein Lorebook, das ein anderer Benutzer [teilt](#sharing-a-lorebook)), siehe [Import und Export](import-export.md). |
| Export (Symbol mit Datei) | Lädt das Lorebook mit seinen Sub-Lorebooks herunter, siehe [Import und Export](import-export.md#exporting). |
| Tags (Symbol mit Etiketten) | **Tag lorebooks**: Setzt ein Tag auf mehrere Lorebooks auf einmal, siehe [unten](#tagging-many-at-once). Wird für ein Lorebook, das in einem Buch geöffnet ist, nicht angezeigt. |
| Description (Symbol mit Zeilen, letztes der ersten Zeile) | **Edit description**: Öffnet ein Fenster, in dem du schreibst, worum es im Lorebook geht, siehe [unten](#description). |
| **Add Entry** | Fügt dem Lorebook einen Eintrag hinzu, siehe [Einträge und Aktivierung](entries.md). |
| **Full-screen editor** | Öffnet die Einträge des Lorebooks in einem Vollbildfenster mit Reitern, siehe [Bearbeiten im Vollbild](#editing-in-full-screen). Nur am Desktop. |
| **Search and replace** | Ersetzt einen Text im Namen, im Text und im Kommentar der Einträge des Lorebooks, siehe [Suchen und Ersetzen](entries.md#search-and-replace). |
| **Mass edit** | Öffnet alle Einträge des Lorebooks und seiner Sub-Lorebooks in einer gemeinsamen Tabelle, siehe [Massenbearbeitung](#mass-edit). |
| Tags (Symbol mit Etiketten, rechts neben **Search and replace**, zweite Zeile) | **Tag entries**: Setzt ein Tag auf mehrere Einträge des ausgewählten Lorebooks auf einmal, siehe [unten](#tagging-many-at-once). |
| **Preview activation** | Zeigt, welche Einträge ein Zug mit einem von dir eingegebenen Text verwenden würde und warum, siehe [Vorschau der Aktivierung](entries.md#preview-activation). |
| **Refresh** | Lädt die Einträge neu. |
| Suchfeld | Zeigt nur die Einträge, die den Text enthalten, siehe [Viele Einträge suchen und löschen](entries.md#searching-and-deleting-many-entries). |
| **Delete selected** | Erscheint, wenn Einträge in der Liste ausgewählt sind; löscht sie nach Bestätigung. |

Unter der Werkzeugleiste stehen die Einstellungen des ausgewählten Lorebooks:

| Feld | |
|---|---|
| **Name** | Der Name des Lorebooks, der angezeigt wird, wenn du es für ein Buch oder als Sub-Lorebook auswählst. |
| **Sub Lorebooks** | Andere Lorebooks, deren Einträge mit diesem einbezogen werden, siehe [unten](#sub-lorebooks). Klicke auf den Namen eines ausgewählten Sub-Lorebooks, um es zu öffnen. |
| **Tags** | Tags des gesamten Lorebooks; sie gelten als Tags jedes seiner Einträge, siehe [Tags](entries.md#tags). |
| **Scan depth** | `0` bis `5`: Wie oft der Inhalt der verwendeten Einträge nach den Filtern der anderen Einträge durchsucht wird, siehe [Einträge, die durch andere Einträge hinzukommen](entries.md#entries-brought-in-by-other-entries). Die Tiefe gilt für die Einträge des Lorebooks, das ein Buch verwendet, und für die seiner Sub-Lorebooks, sofern ein Sub-Lorebook nicht eine eigene Tiefe hat (nächste Zeile). |
| **Scan depth as a sub lorebook** | Leer oder `0` bis `5`: die Scan-Tiefe für die Einträge dieses Lorebooks, solange es Sub-Lorebook des Lorebooks ist, das ein Buch verwendet. Leer bedeutet die Scan-Tiefe dieses Lorebooks, `0` bedeutet, dass die Einträge dieses Lorebooks nie durch den Inhalt anderer gefunden werden. |
| **Enabled** | Ist das Häkchen entfernt, liefert das Lorebook nichts: weder seine Einträge noch die seiner Sub-Lorebooks. |
| **Share with other users** | Ist das Häkchen gesetzt, können andere Benutzer das Lorebook ansehen und eine Kopie importieren, siehe [unten](#sharing-a-lorebook). Bei einem neuen Lorebook ist es aus. |

Unter den Einstellungen steht die [Beschreibung](#description) des Lorebooks, sofern es eine hat.

Der Rest des Editors ist die Liste der [Einträge](entries.md). Alle Änderungen werden sofort gespeichert, während du sie vornimmst.

<a id="description"></a>

### Beschreibung

Die Beschreibung ist ein paar Wörter (oder Zeilen) darüber, worum es im Lorebook geht: welche Welt es beschreibt, wofür es da ist, welches Buch es verwendet. Klicke in der ersten Zeile der Werkzeugleiste auf das Symbol mit den Zeilen, schreibe den Text im Fenster und bestätige. Ein leerer Text entfernt die Beschreibung. Ein Lorebook mit Beschreibung zeigt sie unter seinen Einstellungen in einer Zeile an; was nicht hineinpasst, wird mit Auslassungspunkten abgeschnitten. Lege den Mauszeiger auf die Zeile, um den ganzen Text zu lesen. Die Beschreibung wird mit dem Lorebook kopiert (Klonen, Export, Import, Sicherungen). Außerdem ist sie der Text, den andere Benutzer neben dem Namen sehen, wenn du das Lorebook teilst.

![Das Fenster der Beschreibung eines Lorebooks](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Ein Lorebook teilen

Setze das Häkchen bei **Share with other users**, und jeder andere Benutzer dieser Installation sieht das Lorebook unter **Import from other user** (dem Import-Symbol in der ersten Zeile der Werkzeugleiste), mit seinem Namen, seinem Autor (dein vollständiger Name oder, falls du keinen hast, dein Login) und seiner Beschreibung. Sie können eine Zeile öffnen, um die Einträge (und die Einträge der Sub-Lorebooks) zu lesen, und eine Kopie importieren. Entfernst du das Häkchen, verschwindet das Lorebook aus ihrer Liste. Kopien, die sie bereits angelegt haben, bleiben ihre eigenen.

* Teilen erlaubt nur das Lesen. Niemand sonst kann dein Lorebook ändern, löschen oder in einem Buch verwenden. Dein Lorebook bleibt deins, und du siehst es wie bisher.
* Ein Import legt **neue Lorebooks beim anderen Benutzer** an: das Lorebook und seine **geteilten** Sub-Lorebooks, egal wie tief sie verschachtelt sind, mit ihren Einträgen, Tags und ihrer Beschreibung, verknüpft so, wie deine verknüpft sind. Die Kopien sind privat (nicht geteilt). Was du danach mit deinem Lorebook tust, erreicht sie nicht, und was sie ändern, erreicht deins nicht.
* **Nur Sub-Lorebooks, die selbst geteilt sind, werden angezeigt und kopiert.** Ein Sub-Lorebook, das nicht geteilt ist, bleibt samt allem darunter draußen, auch ein geteiltes Lorebook darunter. Setzt du bei einem Lorebook das Häkchen bei **Share with other users**, und hat es Sub-Lorebooks, die nicht geteilt sind, fragt eine Rückfrage, ob sie ebenfalls geteilt werden sollen. **Yes** teilt das Lorebook und alle Sub-Lorebooks (egal wie tief). **No** teilt nur das Lorebook. **Cancel** teilt nichts, und das Häkchen bleibt aus. Ein Sub-Lorebook, das du später hinzufügst, ist erst geteilt, wenn du es teilst.
* Entfernst du das Häkchen, hörst du nur bei diesem Lorebook auf zu teilen. Seine Sub-Lorebooks bleiben geteilt (und erscheinen für sich in der Liste der anderen), bis du auch deren Häkchen entfernst.
* Gelöschte Lorebooks, die Arbeitskopien von Agenten und deine eigenen Lorebooks werden nicht aufgelistet. Für einen gelöschten Benutzer wird nichts aufgelistet.

Unter den Einstellungen wird die Schätzung der Tokens der aktivierten Einträge des Lorebooks angezeigt. Der Link **Count with subbooks** daneben addiert die Tokens der aktivierten Einträge des Lorebooks und aller seiner Sub-Lorebooks, egal wie tief, und zeigt die Summe daneben an. Wie beim Schreiben einer Geschichte wird ein deaktiviertes Sub-Lorebook samt den Lorebooks darunter weggelassen. Ein Lorebook, das in mehr als einer Stelle Sub-Lorebook ist, wird nur einmal gezählt.
Die Summe ist eine Zählung des Augenblicks, in dem du klickst: Änderst du einen Eintrag, verschwindet sie. Klicke erneut, um neu zu zählen.

Ein Lorebook eines Buchs, an dem ein Agent arbeitet, wird nur zum Lesen angezeigt (siehe [Agenten](../agents.md)). Ohne **Add Entry**, **Search and replace**, **Mass edit**, **Delete Lorebook** und die Einstellungen. **Add Lorebook**, Import, Export und **Refresh** bleiben verfügbar, damit du immer ein weiteres Lorebook beginnen kannst.
## Mass edit

**Mass edit** öffnet ein Fenster mit allen Einträgen des ausgewählten Lorebooks **und seiner Unter-Lorebooks** (egal wie tief verschachtelt) in einer einzigen Tabelle.
Sie ist nach der **Order** der Einträge sortiert (Einträge mit derselben Order nach Lorebook, dann nach Name). Sie dient dazu, ein großes Lorebook aufzuräumen:

- Das Kästchen *enabled*, der **Entry Name**, die **Order**, die **Tags**, der **Filtering Mode**, die **Filtering**, der **Insertion Mode**, die
  **Negative Tags**, der **Activation Context** mit seinen **Parts**, der Bereich der Parts (**From message** und **To message**, leer: keine Begrenzung) und
  die **Note** eines Eintrags lassen sich direkt in der Tabelle ändern, wie im Editor. Jeder Eintrag ist eine Zeile; die Tabelle lässt sich nach unten (und bei schmalem Fenster zur Seite) scrollen. Ändert man die Order, wandert die Zeile an ihren neuen Platz. Der Text eines Eintrags ist in der Tabelle gekürzt; um ihn ganz zu lesen oder zu bearbeiten, öffnet man den Eintrag im Lorebook.
- Das Feld **Lorebook** einer Zeile zeigt, in welchem Lorebook der Eintrag liegt. Wählt man ein anderes (das Lorebook selbst oder eines seiner Unter-Lorebooks), wird der Eintrag dorthin verschoben.
- Um viele Einträge zu verschieben, setzt man ihre Häkchen (das Kästchen in der Kopfzeile setzt alle), wählt unten **Move to lorebook** und drückt **Move checked**.
- Um viele Einträge zu löschen, setzt man ihre Häkchen und drückt **Delete checked**; es wird einmal nachgefragt, danach wandern sie in den [Papierkorb](../trash.md).
- Ein Lorebook, mit dem ein Agent arbeitet, ist gesperrt: Seine Einträge sind ausgegraut und lassen sich weder ändern noch verschieben, und in es lässt sich nichts hineinverschieben.
  Das wird für jedes Lorebook einzeln geprüft, daher kann ein Lorebook frei sein, während eines seiner Unter-Lorebooks gesperrt ist (weil es von einem anderen Buch benutzt wird).
  **Refresh** liest das Fenster neu ein, um zu sehen, ob eine Sperre aufgehoben ist.

Das Lorebook im Editor wird aktualisiert, wenn das Fenster nach einer Änderung geschlossen wird.

![Massenbearbeitung eines Lorebooks und seiner Unter-Lorebooks](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Im Vollbild bearbeiten

Der **Vollbild-Editor** (zweite Zeile der Symbolleiste) öffnet das gewählte Lorebook in einem Fenster, das den ganzen Bildschirm ausfüllt. So lassen sich lange Einträge bearbeiten. Der Expand-Button in der Zeile eines Eintrags öffnet das Fenster direkt bei diesem Eintrag. Der *Lorebook*-Tab im Fenster eines Buches hat denselben Button für das dort angezeigte Lorebook. Das Fenster trägt den Namen des Lorebooks als Titel. Es besteht aus Tabs wie der [Story-Editor](../books/story-editor.md). Ziehen Sie einen Tab neben oder unter einen anderen, und verschieben Sie die Trenner zwischen ihnen.

![Der Vollbild-Editor für Lorebooks: die Liste der Einträge und zwei geöffnete Einträge in Tabs](../../images/lorebook-fullscreen.png)

- Der **Entries**-Tab listet die Einträge auf, mit dem Suchfeld und den Tag- und Statusfiltern der Lorebook-Seite, dazu **Add Entry**, **Mass edit**, **Tag entries**, **Preview activation** und **Refresh**. (Mass edit und Tags zeigen ihre Änderungen auch in den offenen Tabs.) Mit **Include sub lorebooks** listet er auch die Einträge der [Sub-Lorebooks](#sub-lorebooks) auf, jeweils mit dem Namen ihres Lorebooks. **Add Entry** fügt den Eintrag dann dem Lorebook hinzu, das daneben gewählt ist, und der Tab eines solchen Eintrags nennt sein Lorebook. Dieser Tab lässt sich nicht schließen. Jede Zeile hat drei Schaltflächen:
  - **Open** (Stiftsymbol, auch ein Doppelklick auf die Zeile) zeigt den Eintrag im zuletzt genutzten Eintrag-Tab. Gibt es keinen, öffnet sich ein Tab in einer Spalte rechts von der Liste.
  - **Open in new tab** (Pfeilsymbol) fügt einen Tab für den Eintrag hinzu, neben dem zuletzt genutzten Eintrag-Tab.
  - **Delete** (Mülleimer-Symbol) löscht den Eintrag nach Bestätigung und schließt seinen Tab.

  Ein Eintrag ist höchstens in einem Tab geöffnet. Öffnen Sie ihn erneut, zeigt sich der Tab, den er schon hat.
- Ein **Eintrag-Tab** enthält von oben nach unten: den **Name**, die **Order** und **Enabled**. Darunter folgt **Settings**, ein einklappbares Feld mit den übrigen Einstellungen des Eintrags (Filtermodus und Filter, Einfügemodus, Aktivierungskontext und -teile, Tags und negative Tags, der Bereich der Teile, die Kontrollkästchen für die Rekursion und die Bedingung; siehe [Einträge und Aktivierung](entries.md)). Danach folgt der **content** in einem Markdown-Editor. Jeder Tab merkt sich, ob er den Text, den Text und die Vorschau oder nur die Vorschau zeigt, auch wenn das Fenster erneut geöffnet wird. Neben dem Inhalt steht die **note**, wenn **Show note** angehakt ist. Das Auge öffnet die [Aktivierungsvorschau](entries.md#preview-activation) für diesen Eintrag. Die untere Zeile zeigt die geschätzten Tokens des Inhalts und, in Rot, was an seiner Vorlage nicht stimmt. Der Eintrag wird trotzdem gespeichert. Mit dem **×** schließen Sie einen Tab.
- **Bilder in Einträgen.** Fügen Sie ein Bild in den Inhalt ein, legen Sie es darauf ab oder wählen Sie es mit dem Bild-Button der Symbolleiste aus (PNG, JPEG, GIF oder WebP, bis 10 MB). Es wird mit Ihren Dateien gespeichert (im *Resources*-Tab), und der Inhalt erhält an der Cursorposition eine Zeile wie `![Map](resource:…)`. Die Vorschau zeigt das Bild, und der Text in den eckigen Klammern ist seine Bildunterschrift. Löschen Sie die Zeile, um das Bild aus dem Eintrag zu entfernen. Das Modell liest den Eintrag ohne diese Zeilen. Die Bilder selbst werden nur gesendet, wenn die Einstellungen des Modells *Multimodal (accepts images)* angeben (siehe [Inferenz-Anbieter](../inference-providers.md)). Bilder wandern mit dem Lorebook: Ein Klon, eine Kopie eines geteilten Lorebooks, eine exportierte Lorebook-Datei und eine Buchsicherung mit ihren Dateien enthalten sie, und die Kopie erhält eigene Bilder. Der SillyTavern-Export und die Buchexporte lassen sie weg. Der *Lorebook*-Tab der Geschichte zeigt sie unter dem Text des Eintrags. Bilder zählen bei den Token-Schätzungen bisher nicht mit.
- **Zwischen Einträgen springen.** Ctrl+Klick (bei einem Mac Cmd+Klick) auf den Namen oder ein Schlüsselwort eines anderen Eintrags im Inhalt, im Text oder in der Vorschau öffnet diesen Eintrag im zuletzt genutzten Tab. Mit zusätzlich gehaltenem Shift öffnet er sich in einem neuen Tab. Die untere Zeile listet außerdem unter **Mentioned in:** die Einträge auf, deren Inhalt diesen Eintrag nennt. Ein Klick öffnet einen davon, Shift+Klick öffnet ihn in einem neuen Tab. Ein Eintrag wird durch seinen Namen und durch die Wörter seines Filters genannt (jedes Schlüsselwort, der ganze Text eines *Text*-Filters, was ein regulärer Ausdruck trifft), als ganze Wörter, unabhängig von Groß- und Kleinschreibung. Das ähnelt dem, was die [Rekursion](entries.md) verfolgt, ist aber nicht dasselbe. Die Rekursion findet ein Schlüsselwort auch innerhalb eines längeren Wortes, braucht alle Schlüsselwörter eines *AND*-Filters und prüft die Tags und die Bedingung.
- Der Markdown-Editor hat eine Symbolleiste (fett, kursiv, durchgestrichen, eine Linie, Überschriften, Zitat, Code, Codeblock, Tabelle und die drei Arten von Listen). Rechts stehen die Ansichten *edit*, *edit with preview* (der Standard) und *preview*. Die üblichen Tastenkürzel funktionieren (Ctrl+B, Ctrl+I ...). Tab springt zum nächsten Feld, statt einzurücken. Was Sie tippen, wird gespeichert, sobald Sie kurz innehalten, und wenn Sie den Editor verlassen.
- Die Kopfzeile zeigt die geschätzten Tokens der aktivierten Einträge, **Count with subbooks** und **Search and replace**. Letzteres zeigt den ersetzten Text auch in den offenen Tabs.

Alle Änderungen werden gespeichert, sobald Sie sie machen. Daher geht bei **Close** nichts verloren. Das Fenster merkt sich für jedes Lorebook, wie Sie es verlassen haben: Beim nächsten Öffnen zeigt es dieselben Einträge in denselben Tabs, gleich angeordnet. (Inzwischen gelöschte Einträge fehlen.) Die Lorebook-Seite zeigt die Einträge wieder an, sobald das Fenster geschlossen ist.

**Änderungen an anderer Stelle.** Eine Änderung in einem Tab wird so in den Eintrag gespeichert, wie er in diesem Moment gespeichert ist. Was anderswo (auf der Lorebook-Seite, durch Mass edit, in einem anderen Fenster) in den *übrigen* Feldern des Eintrags geändert wurde, bleibt daher erhalten, und der Tab zeigt es an. Wurde der **content** oder die **note** anderswo geändert, seit der Tab es angezeigt hat, wird Ihr Text nicht darübergespeichert. Die untere Zeile weist darauf hin, mit **Keep my text** (Ihren Text trotzdem speichern) und **Load the saved text** (Ihren Text verwerfen). Ein anderswo gelöschter Eintrag schließt seinen Tab.
Das Fenster folgt außerdem dem, was gespeichert wird, während es offen ist. Speichert oder löscht ein Agent oder ein anderes Fenster (ein weiterer Browser-Tab, der *Lorebook*-Tab der Geschichte) einen Eintrag, wird die Liste neu geladen, und der Tab des Eintrags zeigt ihn in seinem aktuellen Stand. (Der Tab eines gelöschten Eintrags schließt sich.) Ein Tab, dessen Text auf **Keep my text** oder **Load the saved text** wartet, bleibt unverändert, bis Sie antworten. Eine Änderung, die in derselben Sekunde gespeichert wird, in der Sie tippen, kann sich noch überschneiden. Dann fragt die untere Zeile wie oben nach.

**Agenten.** Das Fenster wird nicht angeboten, solange ein Agent an einem Buch arbeitet, das das Lorebook nutzt. Startet ein Lauf, während das Fenster offen ist, wird das Fenster bei Ihrer nächsten Änderung oder beim Wechsel des Tabs schreibgeschützt. Eine Leiste nennt den Agenten und sagt, wenn Ihre letzte Änderung nicht gespeichert werden konnte. Pausieren oder stoppen Sie den Lauf und klicken Sie dann auf **Check again**. Die Einträge werden so geladen, wie der Lauf sie hinterlassen hat, und Sie können wieder bearbeiten.

Die Erweiterungen [Lore Generator](../extensions/lore-generator.md) und [Lorebook VCS](../extensions/lorebook-vcs.md) funktionieren hier ebenfalls. Der Zauberstab steht in den Zeilen der Liste und in der Namenszeile eines Eintrag-Tabs. Das Revisionsfeld liegt über der Liste, und die Revisionsleiste eines Eintrags befindet sich in seinem Tab.

<a id="tagging-many-at-once"></a>

## Mehrere auf einmal mit Tags versehen

Die beiden Tag-Buttons der Symbolleiste öffnen ein Fenster, mit dem Sie vielen Elementen einen Tag geben oder ihn entfernen können. **Tag lorebooks** listet Ihre Lorebooks auf, **Tag entries** die Einträge des gewählten Lorebooks. Wählen Sie den **Tag** (oder tippen Sie einen neuen ein und drücken Sie Enter). Die Liste zeigt jedes Element mit seinen Tags. Die Elemente mit dem gewählten Tag stehen zuerst, und der Tag ist hervorgehoben. Die Schaltflächen **+** und **-** in einer Zeile setzen den Tag für dieses Element bzw. entfernen ihn. Haken Sie mehrere Elemente an (das Kästchen in der Kopfzeile hakt alle an), und nutzen Sie unten **Apply the tag to the checked** oder **Remove the tag from the checked**, um es für alle auf einmal zu tun. Ein Element, an dem ein Agent arbeitet (ein Lorebook eines Buches mit laufendem Agenten und damit alle seine Einträge), ist ausgegraut und lässt sich erst ändern, wenn der Lauf pausiert oder gestoppt ist. Das Fenster versieht auch [Bücher](../books/managing-books.md#tagging-many-books-at-once) mit Tags.
Die Tags, die es Einträgen gibt, sind die gewöhnlichen; siehe [Tags](entries.md#tags). Negative Tags werden am Eintrag selbst gesetzt.

![Tags für die Einträge eines Lorebooks vergeben](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Ein Lorebook in einem Buch verwenden

Öffnen Sie das Buch und seinen **Lorebook**-Tab, und wählen Sie das Lorebook im Feld **Lorebook** aus. Der Editor darunter zeigt das Lorebook, sodass Sie während des Schreibens Einträge hinzufügen und ändern können. Leeren Sie das Feld, um ohne Lorebook zu schreiben.

Ein Buch nutzt ein Lorebook. Um mehrere zu verwenden, erstellen Sie ein Lorebook, das sie als Sub-Lorebooks enthält, und verwenden Sie dieses.

Dasselbe Lorebook kann von beliebig vielen Büchern genutzt werden. Änderungen daran wirken sich ab dem nächsten erzeugten Teil auf alle aus. Bereits geschriebene Teile behalten das Lore, mit dem sie geschrieben wurden.
## Sub-Lorebooks

**Sub Lorebooks** enthält die Einträge anderer Lorebooks in diesem. Ein typischer Aufbau:

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- Sub-Lorebooks können eigene Sub-Lorebooks haben; alle Ebenen werden einbezogen.
- Ein Lorebook kann Sub-Lorebook mehrerer Lorebooks sein. Wird es mehr als einmal erreicht, werden seine Einträge trotzdem nur einmal verwendet.
- Ein Lorebook kann sich nicht selbst einbinden, weder direkt noch über andere Lorebooks. Eine solche Auswahl wird mit *Cannot select sub lorebook: cycle detected.* abgelehnt.
- Ein deaktiviertes Sub-Lorebook wird zusammen mit allem, was es enthält, ausgelassen.

![Ein Lorebook mit zwei Sub-Lorebooks: die Tokens mit und ohne sie](../../images/lorebook-sub-lorebooks.png)

Die Einträge aller eingebundenen Lorebooks werden zusammengeführt und nach ihrer [Reihenfolge](entries.md#order-and-insertion) sortiert, unabhängig davon, aus welchem Lorebook sie stammen.

<a id="disabling-and-deleting"></a>

## Deaktivieren und Löschen

Entferne das Häkchen bei **Enabled**, um ein Lorebook vorübergehend auszuschalten, zum Beispiel um zu sehen, wie ein Buch ohne es wirkt. Bücher behalten es weiterhin zugeordnet, und es funktioniert wieder, sobald du das Häkchen setzt.

**Delete Lorebook** entfernt das Lorebook aus deinen Listen. Bücher, die es verwendet haben, erhalten seine Einträge nicht mehr, und Lorebooks, die es als Sub-Lorebook eingebunden haben, überspringen es. Die Daten bleiben in der Datenbank, bis ein Administrator die [Bereinigung](../administration/cleanup.md) ausführt. Bis dahin kannst du das Lorebook aus dem [Papierkorb](../trash.md) wiederherstellen. Kopien in [Buch-Sicherungen](../books/backups.md) und exportierten Dateien sind davon nicht betroffen.
