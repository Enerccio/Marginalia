---
label: Buchsicherungen
order: 50
---

# Buchsicherungen

Eine Buchsicherung ist ein Schnappschuss eines einzelnen Buches: seiner Einstellungen und Prompts, aller Teile in allen Zweigen, Zusammenfassungen, Tags und des Lorebooks. Mit Sicherungen kannst du zu einem früheren Stand der Geschichte zurückkehren, eine Kopie eines Buches anlegen oder ein Buch auf eine andere Marginalia-Installation oder zu einem anderen Benutzer verschieben.

Buchsicherungen sind getrennt von den [Datenbanksicherungen](../administration/database-backups.md), die Administratoren von der gesamten Installation anlegen.

![Der Reiter Sicherungen eines Buches](../../images/book-backups.png)

<a id="taking-a-backup"></a>

## Eine Sicherung erstellen

Klicke im Reiter **Sicherungen** des Buches auf **Sicherung erstellen**. Die Sicherung erscheint oben in der Liste, mit ihrem **Datum**, der Anzahl der **Nachrichten** (Teile), die sie enthält, und der Anzahl der **Bilder** (die [Bilder der Teile](story-editor.md#images)), die deine Teile verwenden.

Sicherungen können auch automatisch angelegt werden, siehe [unten](#automatic-backups).

Eine Sicherung enthält den gesamten Story-Baum, die Zusammenfassungen und die [Anmerkungen](annotations.md) der Teile; beim Wiederherstellen, beim Kopieren eines Buches und beim Importieren einer Sicherung kommen die Notizen mit zurück.

<a id="the-history-of-the-parts"></a>

## Der Verlauf der Teile

Eine Sicherung enthält den [Versionsverlauf](version-history.md) der Teile nur, wenn du es ausdrücklich wünschst: Aktiviere **Versionsverlauf einbeziehen** neben **Sicherung erstellen** (das Kästchen ist zunächst nicht aktiviert, weil der Verlauf um ein Vielfaches größer sein kann als die Geschichte selbst). Beim Wiederherstellen einer solchen Sicherung, als Buch oder als Kopie, kommen die Versionen mit ihren Namen, den Begründungen der generierten Versionen und dem Papierkorb des Verlaufs zurück; die Versionen werden auf das Limit des Buches gekürzt, in das sie kommen. Automatische Sicherungen enthalten nie den Verlauf. Wird eine Sicherung ohne Verlauf wiederhergestellt, erhält jeder Teil eine erste Version, wie bei einem Buch aus der Zeit vor dem Verlauf.

<a id="backups-and-folders"></a>

## Sicherungen und Ordner

Eine Sicherung merkt sich die [Ordner](folders.md), in denen ihr Buch lag, vom obersten bis zum untersten, jeweils mit Name, Beschreibung, Sprache und den Texteinstellungen, die sie festlegen. **Als neues Buch wiederherstellen** und **Sicherung klonen** legen das neue Buch in den Ordner, den die Sicherung nennt, falls du ihn noch hast; falls nicht, in die Ordner mit denselben Namen (ein vorhandener wird unverändert verwendet, ein fehlender wird mit den Einstellungen angelegt, die er hatte). Eine Sicherung ohne Ordner oder mit Ordnern, die sich nicht anlegen lassen, wird auf der obersten Ebene wiederhergestellt. **Wiederherstellen** bei einem Buch (vollständig oder nur Nachrichten) behält das Buch in seinem Ordner. Die Tags eines Ordners sind nicht in der Sicherung enthalten. Ältere Sicherungen werden wie bisher wiederhergestellt.

<a id="pictures-in-a-backup"></a>

## Bilder in einer Sicherung

Eine Sicherung merkt sich, **welche Bilder** die Teile haben (und deren Bildunterschriften), aber **nicht die Bilddateien**. So bleiben Sicherungen und vor allem die automatischen klein. Die Bilder bleiben in deinen [Ressourcen](../resources.md). Möchtest du die Bilder mit einer Sicherung mitnehmen, packst du sie beim [Exportieren](#exporting-and-importing) ein.

<a id="restoring-a-backup"></a>

## Eine Sicherung wiederherstellen

Klicke neben der Sicherung auf **Wiederherstellen** und wähle:

| Option | Was wiederhergestellt wird |
|---|---|
| **Vollständige Wiederherstellung** | Alles: Name, Beschreibung, Veröffentlichung, Prompts und andere Bucheinstellungen, Tags, Modell, Protokoll, Lorebook und alle Teile. |
| **Nur Nachrichten wiederherstellen** | Nur die Teile (alle Zweige) und Zusammenfassungen. Die aktuellen Einstellungen, Prompts und das Lorebook des Buches bleiben unverändert. |

!!!warning
Beim Wiederherstellen werden **alle Teile** des Buches durch die aus der Sicherung ersetzt. Teile, die nach der Sicherung geschrieben wurden, sind danach weg.
Erstelle zuerst eine Sicherung des aktuellen Stands, falls du ihn vielleicht zurückhaben möchtest.
!!!

Das Buchfenster schließt sich und öffnet sich danach wieder mit dem wiederhergestellten Buch.

Die Bilder der wiederhergestellten Teile und alle anderen Dateien, auf die die Teile verweisen (zum Beispiel das Audio eines [Hörbuchs](../extensions/audiobook.md)), sind die, die die Sicherung auflistet, solange du sie noch in deinen [Ressourcen](../resources.md) hast. Eine Ressource, die du inzwischen gelöscht hast, ist aus dem Teil verschwunden. Beim Wiederherstellen werden gelöschte Dateien nicht zurückgeholt, es sei denn, die Sicherung stammt aus einem Export mit den Ressourcen, den du [importiert](#importing-a-backup) hast.

**Modell und Protokoll** werden unter deinen Inferenzanbietern und Protokollen gesucht: zuerst das genaue aus der Sicherung, dann eines mit demselben Namen. Gibt es keines von beiden, behält das Buch sein aktuelles Modell bzw. Protokoll.

**Lorebooks** aus der Sicherung werden mit deinen verglichen. Gibt es ein Lorebook mit demselben Namen, aber einer anderen Identität, fragt Marginalia für jedes einzeln, ob es **Mit vorhandenem verknüpfen** oder **Neu erstellen** aus der Sicherung soll. Lorebooks, die du gar nicht hast, können **importiert** (**Importieren**) oder weggelassen werden (**Nicht importieren**).

![Auswahl, was mit den Lorebooks geschehen soll](../../images/lorebook-import.png)

<a id="copying-a-book"></a>

## Ein Buch kopieren

**Sicherung klonen** legt aus der Sicherung ein neues Buch unter einem Namen an, den du eingibst. Das ursprüngliche Buch bleibt unverändert. Verwende es, um mit einer Kopie des Buches etwas Radikales auszuprobieren oder um ein neues Buch mit demselben Anfang zu beginnen.

Verweist die Sicherung auf Ressourcen (Bilder, das Audio eines Hörbuchs, andere Dateien), fragt Marginalia nach **Mit Ressourcen** oder **Ohne Ressourcen**:

- *Mit Ressourcen* gibt dem neuen Buch **eigene Kopien** davon (dieselben Dateien, als neue Einträge in den [Ressourcen](../resources.md)), sodass das Löschen oder Ersetzen eines Bildes in einem Buch das andere nicht berührt. Ressourcen, die du seit dem Erstellen der Sicherung gelöscht hast, werden weggelassen.
- *Ohne Ressourcen* klont nur den Text.

![Klonen mit oder ohne Ressourcen](../../images/backup-clone-images.png)
<a id="exporting-and-importing"></a>

## Exportieren und Importieren

**Exportieren** lädt die Sicherung als JSON-Datei herunter (`<book name>_backup.json`). Bewahren Sie sie als zusätzliche Kopie auf oder nehmen Sie sie mit auf eine andere Installation.

Wenn die Sicherung auf Ressourcen verweist, fragt Marginalia, ob diese gepackt werden sollen (die Bilder der Teile und alle anderen Dateien, die in ihren Daten genannt werden, etwa die Audiodateien eines Hörbuchs):

![Exportieren einer Sicherung, die Bilder enthält](../../images/backup-export-images.png)

- **Ohne Ressourcen** lädt die JSON-Datei sofort herunter, wie oben beschrieben. Die Ressourcen werden darin nur erwähnt.
- **Mit Ressourcen** packt die Sicherung zusammen mit allen Ressourcen in eine **ZIP-Datei** (`<book name>_backup.zip`). Ein Fortschrittsbalken zeigt, wie viele bereits fertig sind. Die Datei wird Datei für Datei auf der Festplatte erstellt, daher belastet auch ein Buch mit vielen großen Dateien den Arbeitsspeicher kaum. Ein Hörbuch eines langen Buchs umfasst Hunderte Megabyte: Packen Sie es nur, wenn Sie es wirklich brauchen. Ist die Datei fertig, klicken Sie auf **Herunterladen**. Die Datei wird nur so lange aufbewahrt, bis Sie diesen Dialog schließen.

Kann eine Ressource nicht gepackt werden, weil Sie sie gelöscht haben oder ihre Datei fehlt, nennt der Dialog, wie viele ausgelassen wurden.

Das Kontrollkästchen **Geerbte Einstellungen in den Export aufnehmen** (standardmäßig aktiviert, neben den Exportschaltflächen) legt fest, was die Datei über die Einstellungen aussagt, die das Buch aus seinen [Ordnern](folders.md#where-the-settings-of-a-book-come-from) erbt. Ist es aktiviert, werden Sprache, Hauptvorlage, Erzählperspektive, Zeitform, Stil und Prompts, die das Buch nicht selbst festlegt, als eigene Werte in die Datei geschrieben. Die Werte stammen aus den Ordnern, in denen das Buch zum Zeitpunkt der Sicherung lag (zuerst der nächste), und danach aus Ihren Einstellungen. So wirkt das Buch gleich, auch in einer Installation oder einem Konto ohne diese Ordner und Einstellungen. Eingebaute Standardwerte werden nicht geschrieben. Ist das Kontrollkästchen deaktiviert, enthält die Datei nur, was das Buch selbst festlegt. Die Datei verrät nie, in welchem Ordner das Buch liegt: Wo ein Buch abgelegt ist, ist eine lokale Entscheidung. (Der Textexport [Ein Buch exportieren](export.md) schreibt nur den Text und enthält keine Einstellungen.)

Die ZIP-Datei enthält `backup.json` (dieselbe JSON-Datei), `resources.json` (die Liste der Ressourcen) und die Dateien. Sie müssen sie nicht entpacken: Beide Wege, eine Sicherung zurückzuholen, akzeptieren sowohl `.zip` als auch `.json`.

Es gibt zwei Wege, eine Sicherungsdatei wieder einzuspielen:

- **Sicherung importieren** auf dem Reiter *Sicherungen* fügt die Datei der Sicherungsliste **dieses Buchs** hinzu. Solange Sie sie nicht *Wiederherstellen*, ändert sich nichts.
- **Sicherung als neues Buch importieren** im Reiter *Einstellungen* erstellt aus der Datei ein **neues Buch**.

Beide akzeptieren eine `.json`- oder eine `.zip`-Datei. Die Datei wird auf den Server hochgeladen und dort gelesen, daher ist auch eine große Sicherung kein Problem.

<a id="importing-a-backup"></a>

### Eine Sicherung importieren

Ist die Datei eine ZIP-Datei mit Bildern, werden die Bilder zuerst zu Ihren [Ressourcen](../resources.md) hinzugefügt, und die Sicherung verwendet dann diese. Ein Bild, das Sie bereits haben (dieselbe Datei), wird nicht erneut gespeichert: Das vorhandene wird verwendet. Daher fügt der Import Ihres eigenen Exports nichts hinzu. Eine JSON-Datei ohne Bilder wird unverändert importiert.

Beim Import **als neues Buch** erhält das neue Buch **eigene Einträge** in den Ressourcen, auch dann, wenn Sie dieselben Bilder bereits haben: Die Einträge verweisen auf dieselben Dateien, daher wird kein Speicherplatz doppelt belegt. Enthält die Datei keine Bilder (eine einfache JSON-Datei), erhält das neue Buch Kopien jener Bilder, die die Datei auflistet und die Sie noch besitzen; die, die Sie nicht besitzen, werden ausgelassen. Alles in der Datei, was kein verwendbares Bild ist (kein PNG, JPEG, GIF oder WebP oder zu groß), wird ignoriert.

<a id="importing-a-backup-as-a-new-book"></a>

### Eine Sicherung als neues Buch importieren

1. Öffnen Sie den Reiter **Einstellungen** und klicken Sie auf **Sicherung als neues Buch importieren**.
2. Geben Sie den **Name des neuen Buchs** ein. Der Upload ist erst möglich, wenn der Name ausgefüllt ist.
3. Laden Sie die Sicherungsdatei hoch.
4. Entscheiden Sie über die Lorebooks, falls danach gefragt wird.

Das neue Buch erscheint auf dem Reiter *Bücher*.

<a id="deleting-backups"></a>

## Sicherungen löschen

Die Papierkorb-Schaltfläche entfernt eine Sicherung endgültig. Automatische Sicherungen werden nicht rotiert: Sie bleiben, bis Sie sie löschen.

<a id="automatic-backups"></a>

## Automatische Sicherungen

Marginalia kann beim Schreiben selbstständig eine Sicherung anlegen. Den Standard für alle Bücher legen Sie unter **Einstellungen → Allgemeine Einstellungen → Sicherungsstrategie** fest:

| Strategie | |
|---|---|
| **Keine Sicherungen** | Keine automatischen Sicherungen. |
| **Nach N Nachrichten** | Nach jeweils *N* erzeugten Teilen wird eine Sicherung angelegt. |
| **Nach N Minuten** | Nach einem erzeugten Teil wird eine Sicherung angelegt, wenn seit der letzten automatischen Sicherung mindestens *N* Minuten vergangen sind. |

Um für ein einzelnes Buch eine andere Strategie zu verwenden, aktivieren Sie auf seinem Reiter *Sicherungen* das Kontrollkästchen **Abweichung je Manuskript** und wählen dort die Strategie und die Anzahl. Wenn Sie es deaktivieren, kehrt das Buch zum Standard aus den *Einstellungen* zurück.

Automatische Sicherungen entstehen nur, nachdem ein Teil erzeugt wurde. Ein Buch, das Sie nur lesen, erhält daher keine neuen Sicherungen.

<a id="where-backups-are-stored"></a>

## Wo Sicherungen gespeichert werden

Jede Sicherung ist eine JSON-Datei im Datenordner von Marginalia:

```
~/.marginalia/data/<your login>/backups/manuscripts/<book id>/
```

Bei der [Docker-Version](../getting-started/docker-server.md) liegt dieser Ordner auf dem Host unter `./data/data/<login>/...`. Die Dateien befinden sich nicht in der Datenbank, daher enthält eine [Datenbanksicherung](../administration/database-backups.md) sie nicht. Nehmen Sie den gesamten Ordner `.marginalia` in Ihre eigenen Sicherungen auf.

Auch die Bilddateien befinden sich nicht in den Sicherungen. Sie liegen im Ordner `data/<your login>/images` neben `backups`. Ebenso sind sie nicht in einer [Datenbanksicherung](../administration/database-backups.md) enthalten, siehe dort.
