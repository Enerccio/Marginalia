---
label: Lorbooks erstellen
order: 100
---

# Lorebooks erstellen

<a id="the-lorebook-editor"></a>

## Der Lorebook-Editor

Öffne den Reiter **Lorebooks**. Der Editor zeigt jeweils ein Lorebook; wähle es im Feld **Lorebook** oben aus.
Lorebooks gehören deinem Konto: Andere Benutzer sehen sie nicht, es sei denn, du [teilst](#sharing-a-lorebook) eines.

Die Werkzeugleiste hat zwei Zeilen, die in einem schmalen Fenster auf mehr Zeilen umbrechen: die erste für die Lorebooks (**Lorebook** bis **Lorebooks taggen**), die zweite für die Einträge des ausgewählten Lorebooks (**Eintrag hinzufügen** bis **Aktualisieren**).

| Schaltfläche | |
|---|---|
| **Lorebook** | Wählt das Lorebook zum Bearbeiten aus. |
| **Nur Wurzel-Lorebooks** | Zeigt nur die Lorebooks, die kein Unter-Lorebook eines anderen sind (das ausgewählte Lorebook bleibt in der Liste). |
| **Lorebook hinzufügen** | Erstellt ein neues Lorebook namens *Neues Lorebook* und wählt es aus. |
| Klonen (Symbol mit zwei Blättern) | Erstellt eine Kopie des ausgewählten Lorebooks: fragt nach dem Namen (der des Originals mit *(Kopie)*) und wählt die Kopie aus. Die Kopie hat dieselben Einträge (mit ihren Einstellungen, Tags und Negativ-Tags), Tags, Beschreibung und Daten der Erweiterungen. Ihre Unter-Lorebooks werden nicht kopiert; die Kopie verwendet dieselben. Das Original wird nicht verändert. |
| **Lorebook löschen** | Löscht das ausgewählte Lorebook (nach Bestätigung). |
| Importieren (Dateisymbol) | **Marginalia-Lorebook importieren**, **Aus SillyTavern importieren**, **Aus Archiv importieren** (eine zip-Datei mit `.md`-Dateien) oder **Von anderem Benutzer importieren** (ein Lorebook, das ein anderer Benutzer [teilt](#sharing-a-lorebook)), siehe [Import & Export](import-export.md). |
| Exportieren (Dateisymbol) | Lädt das Lorebook mit seinen Unter-Lorebooks herunter, siehe [Import & Export](import-export.md#exporting). |
| Tags (Tag-Symbol) | **Lorebooks taggen**: Setzt ein Tag auf viele Lorebooks auf einmal, siehe [unten](#tagging-many-at-once). Wird nicht angezeigt für ein Lorebook, das in einem Buch geöffnet ist. |
| Beschreibung (Zeilen-Symbol, das letzte der ersten Zeile) | **Beschreibung bearbeiten**: Öffnet ein Fenster, in dem du festhältst, worum es im Lorebook geht, siehe [unten](#description). |
| **Eintrag hinzufügen** | Fügt dem Lorebook einen Eintrag hinzu, siehe [Einträge & Aktivierung](entries.md). |
| **Vollbild-Editor** | Öffnet die Einträge des Lorebooks in einem Vollbildfenster mit Reitern, siehe [Bearbeiten im Vollbild](#editing-in-full-screen). Nur auf dem Desktop. |
| **Suchen und Ersetzen** | Ändert einen Text im Namen, im Text und im Kommentar der Einträge des Lorebooks, siehe [Suchen und Ersetzen](entries.md#search-and-replace). |
| **Massenbearbeitung** | Öffnet alle Einträge des Lorebooks und seiner Unter-Lorebooks in einer einzigen Tabelle, siehe [Massenbearbeitung](#mass-edit). |
| Tags (Tag-Symbol, rechts neben **Suchen und Ersetzen**, zweite Zeile) | **Einträge taggen**: Setzt ein Tag auf viele Einträge des ausgewählten Lorebooks auf einmal, siehe [unten](#tagging-many-at-once). |
| **Aktivierung in der Vorschau anzeigen** | Zeigt, welche Einträge ein Zug mit einem von dir eingegebenen Text verwenden würde und warum, siehe [Aktivierung in der Vorschau anzeigen](entries.md#preview-activation). |
| **Aktualisieren** | Lädt die Einträge neu. |
| Suchfeld | Zeigt nur die Einträge, die den Text enthalten, siehe [Viele Einträge suchen und löschen](entries.md#searching-and-deleting-many-entries). |
| **Ausgewählte löschen** | Erscheint, wenn Einträge in der Liste ausgewählt sind; löscht sie nach Bestätigung. |

Unter der Werkzeugleiste stehen die Einstellungen des ausgewählten Lorebooks:

| Feld | |
|---|---|
| **Name** | Der Name des Lorebooks, der angezeigt wird, wenn du es für ein Buch oder als Unter-Lorebook auswählst. |
| **Unter-Lorebooks** | Andere Lorebooks, deren Einträge mit diesem einbezogen werden, siehe [unten](#sub-lorebooks). Klicke auf den Namen eines ausgewählten Unter-Lorebooks, um es zu öffnen. |
| **Tags** | Tags des gesamten Lorebooks; sie gelten als Tags jedes seiner Einträge, siehe [Tags](entries.md#tags). |
| **Scan-Tiefe** | `0` bis `5`: wie oft der Inhalt der verwendeten Einträge nach den Filtern der anderen Einträge durchsucht wird, siehe [Von anderen Einträgen eingebrachte Einträge](entries.md#entries-brought-in-by-other-entries). Sie gilt für die Einträge des Lorebooks, das ein Buch verwendet, und für die seiner Unter-Lorebooks, sofern ein Unter-Lorebook nicht seine eigene Tiefe hat (nächste Zeile). |
| **Scan-Tiefe als Unter-Lorebook** | Leer oder `0` bis `5`: die Scan-Tiefe für die Einträge dieses Lorebooks, solange es Unter-Lorebook desjenigen Lorebooks ist, das ein Buch verwendet. Leer bedeutet die Scan-Tiefe dieses Lorebooks, `0` bedeutet, dass die Einträge dieses Lorebooks nie durch den Inhalt anderer gefunden werden. |
| **Aktiviert** | Ist das Häkchen entfernt, trägt das Lorebook nichts bei – weder seine Einträge noch die seiner Unter-Lorebooks. |
| **Mit anderen Benutzern teilen** | Ist das Häkchen gesetzt, können andere Benutzer das Lorebook ansehen und eine Kopie importieren, siehe [unten](#sharing-a-lorebook). Bei einem neuen Lorebook ist es aus. |

Unter den Einstellungen steht die [Beschreibung](#description) des Lorebooks, sofern es eine hat.

Der Rest des Editors ist die Liste der [Einträge](entries.md). Alle Änderungen werden gespeichert, sobald du sie vornimmst.

<a id="description"></a>

### Beschreibung

Die Beschreibung sind ein paar Worte (oder Zeilen) über das Lorebook: welche Welt es beschreibt, wofür es da ist, welches Buch es verwendet. Klicke in der ersten Zeile der Werkzeugleiste auf das Zeilen-Symbol, schreibe den Text in das Fenster und bestätige; ein leerer Text entfernt sie. Ein Lorebook mit Beschreibung zeigt sie einzeilig unter seinen Einstellungen, abgeschnitten mit Auslassungspunkten, wo sie nicht hineinpasst; bewege den Mauszeiger auf die Zeile, um den ganzen Text zu lesen. Die Beschreibung wird mit dem Lorebook kopiert (Klonen, Export, Import, Sicherungen) und ist der Text, den andere Benutzer neben dem Namen sehen, wenn du es teilst.

![Das Fenster der Beschreibung eines Lorebooks](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Ein Lorebook teilen

Setze das Häkchen bei **Mit anderen Benutzern teilen**, und jeder andere Benutzer dieser Installation sieht das Lorebook unter **Von anderem Benutzer importieren** (der Importknopf in der ersten Zeile der Werkzeugleiste), mit seinem Namen, seinem Autor (dein vollständiger Name oder, falls du keinen hast, dein Login) und seiner Beschreibung. Sie können eine Zeile öffnen, um die Einträge (und die Einträge der Unter-Lorebooks) zu lesen, und eine Kopie importieren. Entfernst du das Häkchen, verschwindet das Lorebook aus ihrer Liste; Kopien, die sie bereits gemacht haben, bleiben ihre.

* Teilen ist nur Lesen. Niemand sonst kann dein Lorebook ändern, löschen oder in einem Buch verwenden; dein Lorebook bleibt deins, und du siehst es wie zuvor.
* Ein Import erstellt **neue Lorebooks des anderen Benutzers**: das Lorebook und seine **geteilten** Unter-Lorebooks, egal wie tief, mit ihren Einträgen, Tags und Beschreibung, verknüpft so, wie deine verknüpft sind. Die Kopien sind privat (nicht geteilt); was du danach mit deinem Lorebook tust, erreicht sie nicht, und was sie ändern, erreicht deins nicht.
* **Nur Unter-Lorebooks, die selbst geteilt sind, werden angezeigt und kopiert.** Ein Unter-Lorebook, das nicht geteilt ist, bleibt samt allem darunter draußen, auch ein geteiltes Lorebook darunter. Wenn du bei einem Lorebook, das nicht geteilte Unter-Lorebooks hat, das Häkchen bei **Mit anderen Benutzern teilen** setzt, fragt eine Rückfrage, ob du sie ebenfalls teilen willst: **Ja** teilt das Lorebook und alle diese Unter-Lorebooks (egal wie tief), **Nein** teilt nur das Lorebook, **Abbrechen** teilt nichts und lässt das Kästchen leer. Ein Unter-Lorebook, das du später hinzufügst, ist nicht geteilt, bis du es teilst.
* Das Entfernen des Häkchens beendet nur das Teilen dieses Lorebooks; seine Unter-Lorebooks bleiben geteilt (und erscheinen für sich allein in der Liste der anderen), bis du auch bei ihnen das Häkchen entfernst.
* Gelöschte Lorebooks, die Arbeitskopien von Agenten und deine eigenen Lorebooks werden nicht aufgeführt, und für einen gelöschten Benutzer wird nichts aufgeführt.

Unter den Einstellungen wird die Schätzung der Tokens der aktivierten Einträge des Lorebooks angezeigt. Der Link **Mit Unterbüchern zählen** daneben addiert die Tokens der aktivierten Einträge des Lorebooks und aller seiner Unter-Lorebooks, egal wie tief, und zeigt die Summe daneben an. Wie beim Schreiben einer Geschichte werden ein deaktiviertes Unter-Lorebook samt den Lorebooks darunter ausgelassen, und ein Lorebook, das in mehr als einer Stelle Unter-Lorebook ist, wird einmal gezählt.
Die Summe ist eine Zählung des Augenblicks, in dem du klickst: Änderst du einen Eintrag, verschwindet sie; klicke erneut, um neu zu zählen.

Ein Lorebook eines Buchs, an dem ein Agent arbeitet, wird nur lesend angezeigt (siehe [Agenten](../agents.md)), ohne **Eintrag hinzufügen**, **Suchen und Ersetzen**, **Massenbearbeitung**, **Lorebook löschen** und die Einstellungen; **Lorebook hinzufügen**, Import, Export und **Aktualisieren** bleiben, damit du immer ein weiteres Lorebook beginnen kannst.
<a id="mass-edit"></a>

## Massenbearbeitung

**Massenbearbeitung** öffnet ein Fenster mit allen Einträgen des gewählten Lorebooks **und seiner Unter-Lorebooks** (egal wie tief) in einer einzigen Tabelle,
sortiert nach der **Reihenfolge** der Einträge (Einträge mit gleicher Reihenfolge nach Lorebook, dann nach Name). Sie dient dazu, ein großes Lorebook aufzuräumen:

- Das Kontrollkästchen *aktiviert*, den **Eintragsnamen**, die **Reihenfolge**, die **Tags**, den **Filtermodus**, die **Filterung**, den **Einfügemodus**, die
  **Negativen Tags**, den **Aktivierungskontext** mit seinen **Abschnitten**, den Bereich der Abschnitte (**Ab Nachricht** und **Bis Nachricht**, leer: keine Begrenzung) und die
  **Notiz** eines Eintrags direkt an Ort und Stelle ändern, wie im Editor. Jeder Eintrag ist eine Zeile; die Tabelle scrollt nach unten (und zur Seite, wenn das
  Fenster schmal ist). Die Zeile wandert an ihre neue Position, wenn du die Reihenfolge änderst. Der Text eines Eintrags wird in der Tabelle gekürzt; öffne den Eintrag im
  Lorebook, um den gesamten Text zu lesen oder zu bearbeiten.
- Das Feld **Lorebook** einer Zeile zeigt, in welchem Lorebook der Eintrag liegt. Wähle ein anderes (das Lorebook selbst oder eines seiner Unter-Lorebooks), um den Eintrag dorthin zu verschieben.
- Um viele Einträge zu verschieben, markiere sie (das Kästchen in der Kopfzeile markiert alle), wähle unten **In Lorebook verschieben** und drücke **Markierte verschieben**.
- Um viele Einträge zu löschen, markiere sie und drücke **Markierte löschen**; es wird einmal nachgefragt, und sie wandern in den [Papierkorb](../trash.md).
- Ein Lorebook, mit dem ein Agent arbeitet, ist gesperrt: Seine Einträge sind ausgegraut und können weder geändert noch verschoben werden, und in es kann nichts verschoben werden. Dies wird für jedes Lorebook einzeln geprüft, daher kann ein Lorebook frei sein, während eines seiner Unter-Lorebooks gesperrt ist (weil es von einem anderen Buch verwendet wird). **Aktualisieren** liest das Fenster neu, um zu sehen, ob eine Sperre aufgehoben wurde.

Das Lorebook im Editor wird aktualisiert, wenn das Fenster nach einer Änderung geschlossen wird.

![Massenbearbeitung eines Lorebooks und seiner Unter-Lorebooks](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Vollbild-Bearbeitung

**Vollbild-Editor** (zweite Zeile der Werkzeugleiste) öffnet das ausgewählte Lorebook in einem Fenster, das den ganzen Bildschirm ausfüllt, zum Arbeiten an langen Einträgen (der Erweitern-Button in der Zeile eines Eintrags öffnet das Fenster direkt mit diesem Eintrag); der Tab *Lorebook* im Fenster eines Buchs hat denselben Button für das dort angezeigte Lorebook. Das Fenster trägt den Namen des Lorebooks als Titel. Es besteht aus Tabs wie beim [Story-Editor](../books/story-editor.md): Ziehen Sie einen Tab neben oder unter einen anderen, um ihn anzuordnen, und ziehen Sie die Trennleisten zwischen ihnen.

![Der Vollbild-Editor für Lorebooks: die Liste der Einträge und zwei Einträge in Tabs](../../images/lorebook-fullscreen.png)

- Der Tab **Einträge** listet die Einträge mit dem Suchfeld und den Tag- und Statusfiltern der Lorebook-Seite, **Eintrag hinzufügen**, **Massenbearbeitung**, **Einträge taggen**, **Aktivierung in der Vorschau anzeigen** und **Aktualisieren** (Massenbearbeitung und Tags zeigen ihre Änderungen auch in den offenen Tabs). Mit **Unter-Lorebooks einbeziehen** listet er auch die Einträge der [Unter-Lorebooks](#sub-lorebooks), jeweils mit dem Namen ihres Lorebooks; **Eintrag hinzufügen** fügt den Eintrag dann dem Lorebook hinzu, das daneben ausgewählt ist, und der Tab eines solchen Eintrags nennt sein Lorebook. Dieser Tab kann nicht geschlossen werden. Jede Zeile hat drei Buttons:
  - **Öffnen** (Stift-Symbol, auch ein Doppelklick auf die Zeile) zeigt den Eintrag im zuletzt benutzten Eintrags-Tab oder, falls es keinen gibt, öffnet einen in einer Spalte rechts von der Liste;
  - **In neuem Tab öffnen** (Pfeil-Symbol) fügt einen Tab für den Eintrag neben dem zuletzt benutzten Eintrags-Tab hinzu;
  - **Löschen** (Papierkorb-Symbol) löscht den Eintrag nach Bestätigung und schließt seinen Tab.

  Ein Eintrag ist höchstens in einem Tab geöffnet: Wenn Sie ihn erneut öffnen, wird der Tab angezeigt, den er bereits hat.
- Ein **Eintrags-Tab** enthält von oben: den **Namen**, die **Reihenfolge** und **Aktiviert**; **Einstellungen**, ein einklappbares Feld mit den übrigen Einstellungen des Eintrags (Filtermodus und Filter, Einfügemodus, Aktivierungskontext und -teile, Tags und negative Tags, der Bereich der Teile, die Kontrollkästchen für die Rekursion und deren Bedingung, siehe [Einträge & Aktivierung](entries.md)); danach der **Inhalt** in einem Markdown-Editor (jeder Tab merkt sich, ob er den Text, den Text und die Vorschau oder nur die Vorschau zeigt, auch wenn das Fenster später erneut geöffnet wird), und die **Notiz** daneben, wenn **Notiz anzeigen** aktiviert ist. Das Auge öffnet die [Aktivierungsvorschau](entries.md#preview-activation) für diesen Eintrag. Die unterste Zeile zeigt die geschätzten Tokens des Inhalts und, in Rot, was an seiner Vorlage nicht stimmt (er wird trotzdem gespeichert). Schließen Sie einen Tab mit seinem **×**.
- **Bilder in Einträgen.** Fügen Sie ein Bild in den Inhalt ein, legen Sie eines darauf ab oder wählen Sie eines mit dem Bild-Button der Werkzeugleiste (PNG, JPEG, GIF oder WebP, bis zu 10 MB). Es wird mit Ihren Dateien gespeichert (im Tab *Ressourcen*), und an der Cursorposition erhält der Inhalt eine Zeile wie `![Karte](resource:…)`; die Vorschau zeigt das Bild, und der Text in den eckigen Klammern ist seine Bildunterschrift. Löschen Sie die Zeile, um das Bild aus dem Eintrag zu entfernen. Das Modell liest den Eintrag ohne diese Zeilen. Die Bilder selbst werden nur gesendet, wenn in den Einstellungen des Modells *Lore-Bilder senden* aktiviert ist (siehe [Inferenz-Anbieter](../inference-providers.md)). Bilder wandern mit dem Lorebook: Ein Klon, eine Kopie eines geteilten Lorebooks, eine exportierte Lorebook-Datei und ein Buch-Backup mit seinen Dateien enthalten sie, und die Kopie erhält eigene Bilder. Der SillyTavern-Export und die Buch-Exporte lassen sie weg. Der Tab *Lorebook* der Geschichte zeigt sie unter dem Text des Eintrags. Bilder zählen bisher nicht in die Token-Schätzungen.
- **Zwischen Einträgen springen.** Strg+Klick (Cmd+Klick auf dem Mac) auf den Namen oder ein Schlüsselwort eines anderen Eintrags im Inhalt, im Text oder in der Vorschau öffnet diesen Eintrag im zuletzt benutzten Tab; mit zusätzlich gehaltener Umschalttaste öffnet er sich in einem neuen Tab. Die unterste Zeile listet außerdem nach **Erwähnt in:** die Einträge auf, deren Inhalt diesen nennt; klicken Sie auf einen, um ihn zu öffnen, Umschalt+Klick für einen neuen Tab. Ein Eintrag wird über seinen Namen und die Wörter seines Filters genannt (jedes Schlüsselwort, der ganze Text eines *Text*-Filters, was ein regulärer Ausdruck trifft), als ganze Wörter, unabhängig von Groß- und Kleinschreibung. Das ist dem nahe, was [Rekursion](entries.md) verfolgt, aber nicht dasselbe: Die Rekursion findet ein Schlüsselwort auch innerhalb eines längeren Wortes, braucht alle Schlüsselwörter eines *UND*-Filters und prüft die Tags und die Bedingung.
- Der Markdown-Editor hat eine Werkzeugleiste (fett, kursiv, durchgestrichen, eine Linie, Überschriften, Zitat, Code, Codeblock, Tabelle und die drei Arten von Listen) und rechts die Ansichten *Bearbeiten*, *Bearbeiten mit Vorschau* (der Standard) und *Vorschau*. Die üblichen Tastenkürzel funktionieren (Strg+B, Strg+I ...); Tab springt zum nächsten Feld statt einzurücken. Was Sie tippen, wird gespeichert, sobald Sie kurz innehalten, und wenn Sie den Editor verlassen.
- Die Kopfzeile zeigt die geschätzten Tokens der aktivierten Einträge, **Mit Unterbüchern zählen** und **Suchen und ersetzen**, das den ersetzten Text auch in den offenen Tabs anzeigt.

Alle Änderungen werden gespeichert, sobald Sie sie machen, daher geht mit **Schließen** nichts verloren. Das Fenster merkt sich, wie Sie es verlassen haben, für jedes Lorebook: Beim nächsten Öffnen erscheinen dieselben Einträge in denselben Tabs, gleich angeordnet (inzwischen gelöschte Einträge fallen weg). Die Lorebook-Seite zeigt die Einträge nach dem Schließen des Fensters wieder an.

**Änderungen an anderer Stelle.** Eine Änderung in einem Tab wird so in den Eintrag gespeichert, wie er in diesem Moment gespeichert ist. Was an anderer Stelle (der Lorebook-Seite, der Massenbearbeitung, einem anderen Fenster) in den *übrigen* Feldern des Eintrags geändert wurde, bleibt daher erhalten, und der Tab zeigt es an. Wurde der **Inhalt** oder die **Notiz** an anderer Stelle geändert, seit der Tab sie angezeigt hat, wird Ihr Text nicht darüber gespeichert: Die unterste Zeile weist darauf hin, mit **Meinen Text behalten** (Ihren Text trotzdem speichern) und **Den gespeicherten Text laden** (Ihren Text verwerfen). Ein anderswo gelöschter Eintrag schließt seinen Tab.

Das Fenster folgt auch dem, was während seiner Anzeige gespeichert wird: Wenn ein Agent oder ein anderes Fenster (ein anderer Browser-Tab, der Tab *Lorebook* der Geschichte) einen Eintrag speichert oder löscht, wird die Liste neu geladen, und der Tab dieses Eintrags zeigt ihn so, wie er jetzt ist (der Tab eines gelöschten Eintrags schließt sich). Ein Tab, dessen Text auf **Meinen Text behalten** oder **Den gespeicherten Text laden** wartet, bleibt unverändert, bis Sie geantwortet haben. Eine Änderung, die in derselben Sekunde gespeichert wird, in der Sie tippen, kann sich noch überschneiden: Die unterste Zeile fragt dann wie oben nach.

**Agenten.** Das Fenster wird nicht angeboten, während ein Agent an einem Buch arbeitet, das das Lorebook verwendet. Startet ein Lauf, während das Fenster offen ist, wird das Fenster bei Ihrer nächsten Änderung oder beim Wechsel des Tabs schreibgeschützt: Eine Leiste nennt den Agenten (und sagt, wenn Ihre letzte Änderung nicht gespeichert werden konnte). Pausieren oder stoppen Sie den Lauf und klicken Sie dann auf **Erneut prüfen**: Die Einträge werden so geladen, wie der Lauf sie hinterlassen hat, und Sie können wieder bearbeiten.

Die Erweiterungen [Lore-Generator](../extensions/lore-generator.md) und [Lorebook-VCS](../extensions/lorebook-vcs.md) funktionieren auch hier: Der Zauberstab steht in den Zeilen der Liste und in der Namenszeile eines Eintrags-Tabs, das Revisionsfeld liegt über der Liste, und die Revisionsleiste eines Eintrags befindet sich in seinem Tab.

<a id="tagging-many-at-once"></a>

## Viele auf einmal taggen

Die beiden Tag-Buttons der Werkzeugleiste öffnen ein Fenster, das einen Tag auf viele Elemente setzt oder von ihnen entfernt: **Lorebooks taggen** listet Ihre Lorebooks, **Einträge taggen** die Einträge des ausgewählten Lorebooks. Wählen Sie den **Tag** (oder tippen Sie einen neuen ein und drücken Sie Enter); die Liste zeigt jedes Element mit seinen Tags, und die Elemente mit dem gewählten Tag stehen zuerst, der Tag ist hervorgehoben. Die Buttons **+** und **-** einer Zeile setzen oder entfernen den Tag für dieses Element. Markieren Sie mehrere Elemente (das Kästchen im Kopf markiert alle) und verwenden Sie unten **Den Tag auf die Markierten anwenden** oder **Den Tag von den Markierten entfernen**, um es für alle zu tun. Ein Element, an dem ein Agent arbeitet (ein Lorebook eines Buchs mit laufendem Agenten und damit alle seine Einträge), ist ausgegraut und kann erst geändert werden, wenn der Lauf pausiert oder gestoppt ist. Das Fenster taggt auch [Bücher](../books/managing-books.md#tagging-many-books-at-once). Die Tags, die es Einträgen setzt, sind die gewöhnlichen, siehe [Tags](entries.md#tags); negative Tags werden am Eintrag selbst gesetzt.

![Das Taggen der Einträge eines Lorebooks](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Ein Lorebook in einem Buch verwenden

Öffnen Sie das Buch und seinen Tab **Lorebook**, und wählen Sie das Lorebook im Feld **Lorebook** aus. Der Editor darunter zeigt das Lorebook, sodass Sie Einträge hinzufügen und ändern können, während Sie schreiben. Leeren Sie das Feld, um ohne Lorebook zu schreiben.

Ein Buch verwendet ein Lorebook. Um mehrere zu verwenden, erstellen Sie ein Lorebook, das sie als Unter-Lorebooks einbindet, und verwenden Sie dieses.

Dasselbe Lorebook kann von beliebig vielen Büchern verwendet werden. Änderungen daran wirken sich ab dem nächsten erzeugten Teil auf alle aus; bereits geschriebene Teile behalten das Lore, mit dem sie geschrieben wurden.
<a id="sub-lorebooks"></a>

## Unter-Lorebooks

**Unter-Lorebooks** bindet die Einträge anderer Lorebooks in dieses ein. Ein typischer Aufbau:

```
"Saga" (von den Büchern der Reihe verwendet)
 ├─ "Welt"          Orte, Geschichte, Magieregeln
 ├─ "Figuren"       die Hauptbesetzung
 └─ "Notizen Buch 2"
```

- Unter-Lorebooks können eigene Unter-Lorebooks haben; alle Ebenen werden einbezogen.
- Ein Lorebook kann Unter-Lorebook mehrerer Lorebooks sein. Wird es mehrfach erreicht, werden seine Einträge trotzdem nur einmal verwendet.
- Ein Lorebook kann sich nicht selbst einbinden, weder direkt noch über andere Lorebooks: Eine solche Auswahl wird mit *Das Unter-Lorebook kann nicht ausgewählt werden: Zyklus erkannt.* abgelehnt.
- Ein deaktiviertes Unter-Lorebook wird samt allem, was es enthält, ausgelassen.

![Ein Lorebook mit zwei Unter-Lorebooks: die Tokens mit und ohne sie](../../images/lorebook-sub-lorebooks.png)

Die Einträge aller eingebundenen Lorebooks werden zusammengeführt und nach ihrer [Reihenfolge](entries.md#order-and-insertion) sortiert, unabhängig davon, aus welchem Lorebook sie stammen.

<a id="disabling-and-deleting"></a>

## Deaktivieren und Löschen

Entfernen Sie den Haken bei **Aktiviert**, um ein Lorebook vorübergehend auszuschalten, zum Beispiel um zu sehen, wie ein Buch ohne es wirkt. Bücher verwenden es weiterhin, und es funktioniert wieder, sobald Sie den Haken setzen.

**Lorebook löschen** entfernt das Lorebook aus Ihren Listen. Bücher, die es verwendet haben, erhalten seine Einträge nicht mehr, und Lorebooks, die es als Unter-Lorebook eingebunden haben, überspringen es. Die Daten bleiben in der Datenbank, bis ein Administrator [Bereinigung](../administration/cleanup.md) ausführt; bis dahin können Sie das Lorebook aus dem [Papierkorb](../trash.md) wiederherstellen. Kopien in [Buch-Backups](../books/backups.md) und exportierten Dateien sind davon nicht betroffen.
