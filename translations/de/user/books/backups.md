---
label: Buchsicherungen
order: 50
---

# Buchsicherungen

Eine Buchsicherung ist ein Schnappschuss eines Buches: seine Einstellungen und Prompts, alle Teile in allen Zweigen, Zusammenfassungen, Tags und das
Lorebook. Verwenden Sie Sicherungen, um zu einem früheren Stand der Geschichte zurückzukehren, eine Kopie eines Buches zu erstellen oder ein Buch auf eine
andere Marginalia-Installation oder zu einem anderen Benutzer zu übertragen.

Buchsicherungen sind getrennt von den [Datenbanksicherungen](../administration/database-backups.md), die Administratoren von der gesamten Installation anlegen.

![Der Reiter Sicherungen eines Buches](../../images/book-backups.png)

<a id="taking-a-backup"></a>

## Eine Sicherung erstellen

Klicken Sie auf dem Reiter **Sicherungen** des Buches auf **Sicherung erstellen**. Die Sicherung erscheint oben in der Liste, mit ihrem **Datum**,
der Anzahl der **Nachrichten** (Teile), die sie enthält, und der Anzahl der **Bilder** (die [Bilder der Teile](story-editor.md#images)), die ihre Teile verwenden.

Sicherungen können auch automatisch erstellt werden, siehe [unten](#automatic-backups).

Eine Sicherung enthält den gesamten Geschichtsbaum, die Zusammenfassungen und die [Anmerkungen](annotations.md) der Teile; beim Wiederherstellen,
beim Kopieren eines Buches und beim Importieren einer Sicherung kehren auch die Notizen zurück.

<a id="the-history-of-the-parts"></a>

## Der Verlauf der Teile

Eine Sicherung enthält den [Versionsverlauf](version-history.md) der Teile nur, wenn Sie es verlangen: Aktivieren Sie **Versionsverlauf einbeziehen**
neben **Sicherung erstellen** (das Kästchen ist zunächst nicht aktiviert, weil der Verlauf um ein Vielfaches größer sein kann als die Geschichte selbst).
Beim Wiederherstellen einer solchen Sicherung, als Buch oder als Kopie, kehren die Versionen mit ihren Namen, den Begründungen der generierten
Versionen und dem Papierkorb des Verlaufs zurück; die Versionen werden auf das Limit des Buches gekürzt, in dem sie landen. Automatische Sicherungen
enthalten niemals den Verlauf. Eine Wiederherstellung ohne Verlauf gibt jedem Teil eine erste Version, wie bei einem Buch von vor dem Verlauf.

<a id="backups-and-folders"></a>

## Sicherungen und Ordner

Eine Sicherung merkt sich die [Ordner](folders.md), in denen ihr Buch lag, von oben nach unten, jeweils mit Name, Beschreibung,
Sprache und den Textenstellungen, die sie festlegen. **Als neues Buch wiederherstellen** und **Sicherung klonen** legen das neue Buch in den Ordner,
den die Sicherung nennt, sofern Sie ihn noch haben; falls nicht, in die Ordner mit denselben Namen (ein vorhandener wird unverändert verwendet, ein fehlender
wird mit den Einstellungen angelegt, die er hatte). Eine Sicherung ohne Ordner oder mit Ordnern, die nicht angelegt werden können, wird auf der obersten Ebene wiederhergestellt.
**Wiederherstellen** bei einem Buch (vollständig oder nur Nachrichten) belässt das Buch in seinem Ordner. Die Tags eines Ordners sind nicht in der Sicherung enthalten.
Ältere Sicherungen werden wie bisher wiederhergestellt.

<a id="pictures-in-a-backup"></a>

## Bilder in einer Sicherung

Eine Sicherung merkt sich, **welche Bilder** die Teile haben (und deren Bildunterschriften), aber **nicht die Bilddateien**. So bleiben
Sicherungen, auch die automatischen, klein. Die Bilder bleiben in Ihren [Ressourcen](../resources.md). Um die Bilder mit einer Sicherung mitzunehmen, packen Sie sie beim [Exportieren](#exporting-and-importing) mit ein.

<a id="restoring-a-backup"></a>

## Eine Sicherung wiederherstellen

Klicken Sie neben der Sicherung auf **Wiederherstellen** und wählen Sie:

| Option | Was wiederhergestellt wird |
|---|---|
| **Vollständige Wiederherstellung** | Alles: Name, Beschreibung, Veröffentlichung, Prompts und andere Bucheinstellungen, Tags, Modell, Protokoll, Lorebook und alle Teile. |
| **Nur Nachrichten wiederherstellen** | Nur die Teile (alle Zweige) und Zusammenfassungen. Die aktuellen Einstellungen, Prompts und das Lorebook des Buches bleiben unverändert. |

!!!warning
Das Wiederherstellen ersetzt **alle Teile** des Buches durch die aus der Sicherung. Nach der Sicherung geschriebene Teile sind weg.
Erstellen Sie zuerst eine Sicherung des aktuellen Stands, falls Sie ihn womöglich zurückwollen.
!!!

Das Buchfenster schließt sich und öffnet sich wieder mit dem wiederhergestellten Buch.

Die Bilder der wiederhergestellten Teile und alle anderen Dateien, auf die die Teile verweisen (etwa die Audiodatei eines [Hörbuchs](../extensions/audiobook.md)), sind jene, die die Sicherung
auflistet, sofern Sie sie noch in den [Ressourcen](../resources.md) haben. Eine Ressource, die Sie seitdem gelöscht haben, ist vom Teil verschwunden. Das Wiederherstellen bringt
gelöschte Dateien nicht zurück, es sei denn, die Sicherung stammt aus einem Export mit Ressourcen, den Sie [importiert](#importing-a-backup) haben.

**Modell und Protokoll** werden unter Ihren Inferenzanbietern und Protokollen gesucht: zuerst das genau gleiche aus der
Sicherung, dann eines mit demselben Namen. Gibt es keines von beiden, behält das Buch sein aktuelles Modell bzw. Protokoll.

**Lorebooks** in der Sicherung werden mit Ihren verglichen. Existiert ein Lorebook mit demselben Namen, aber einer anderen Identität,
fragt Marginalia für jedes einzelne, ob es aus der Sicherung **Mit vorhandenem verknüpfen** oder **Neu erstellen** werden soll. Lorebooks, die Sie gar nicht haben,
können importiert (**Importieren**) oder ausgelassen werden (**Nicht importieren**).

![Auswahl, was mit den Lorebooks geschehen soll](../../images/lorebook-import.png)

<a id="copying-a-book"></a>

## Ein Buch kopieren

**Sicherung klonen** erstellt aus der Sicherung ein neues Buch unter einem Namen, den Sie eingeben. Das ursprüngliche Buch wird nicht verändert. Verwenden Sie es,
um mit einer Kopie des Buches etwas Radikales auszuprobieren, oder um ein neues Buch mit demselben Anfang zu beginnen.

Verweist die Sicherung auf Ressourcen (Bilder, die Audiodatei eines Hörbuchs, andere Dateien), fragt Marginalia **Mit Ressourcen** oder **Ohne Ressourcen**:

- *Mit Ressourcen* gibt dem neuen Buch **eigene Kopien** davon (dieselben Dateien, neue Einträge in den
  [Ressourcen](../resources.md)), sodass das Löschen oder Ersetzen eines Bildes in einem Buch das andere nicht berührt. Ressourcen, die Sie seit
  der Sicherung gelöscht haben, bleiben außen vor.
- *Ohne Ressourcen* klont nur den Text.

![Klonen mit oder ohne Ressourcen](../../images/backup-clone-images.png)
<a id="exporting-and-importing"></a>

## Exportieren und Importieren

**Exportieren** lädt die Sicherung als JSON-Datei herunter (`<Buchname>_backup.json`). Bewahren Sie sie als zusätzliche Kopie auf oder nehmen Sie sie mit auf eine andere Installation.

Verweist die Sicherung auf Ressourcen, fragt Marginalia, ob diese gepackt werden sollen (die Bilder der Teile und alle anderen Dateien, auf die ihre Daten verweisen, etwa die Audiodateien eines Hörbuchs):

![Export einer Sicherung, die Bilder enthält](../../images/backup-export-images.png)

- **Ohne Ressourcen** lädt die JSON-Datei sofort herunter, wie oben beschrieben. Die Ressourcen werden darin nur erwähnt.
- **Mit Ressourcen** packt die Sicherung zusammen mit allen Ressourcen in eine **ZIP-Datei** (`<Buchname>_backup.zip`). Ein Fortschrittsbalken zeigt, wie viele Dateien schon fertig sind. Die Datei wird auf der Festplatte jeweils eine Datei nach der anderen erstellt, daher braucht auch ein Buch mit vielen großen Dateien nicht viel Arbeitsspeicher. Ein Hörbuch eines langen Buchs umfasst hunderte Megabyte: Packen Sie es nur, wenn Sie es wirklich brauchen. Ist die Datei fertig, klicken Sie auf **Herunterladen**. Die Datei bleibt nur erhalten, bis Sie diesen Dialog schließen.

Kann eine Ressource nicht gepackt werden, weil Sie sie gelöscht haben oder ihre Datei fehlt, nennt der Dialog, wie viele ausgelassen wurden.

Das Kontrollkästchen **Geerbte Einstellungen in den Export aufnehmen** (standardmäßig aktiv, neben den Exportschaltflächen) legt fest, was die Datei über die Einstellungen aussagt, die das Buch von seinen [Ordnern](folders.md#where-the-settings-of-a-book-come-from) erbt. Ist es aktiv, werden Sprache, Hauptvorlage, Erzählperspektive, Zeitform, Stil und Prompts, die das Buch nicht selbst setzt, als eigene Werte in die Datei geschrieben. Sie stammen aus den Ordnern, in denen das Buch zum Zeitpunkt der Sicherung lag (zuerst der nächste), und danach aus Ihren Einstellungen. So wirkt das Buch auch in einer Installation oder einem Konto gleich, die diese Ordner und Einstellungen nicht haben. Eingebaute Standardwerte werden nicht geschrieben. Ist das Kontrollkästchen aus, enthält die Datei nur, was das Buch selbst festlegt. Die Datei verrät nie, in welchem Ordner das Buch liegt: wo ein Buch abgelegt ist, ist eine lokale Entscheidung. (Der Story-Export, [Ein Buch exportieren](export.md), schreibt nur den Text und hat keine Einstellungen.)

Das ZIP enthält `backup.json` (dasselbe JSON), `resources.json` (die Liste der Ressourcen) und die Dateien. Sie müssen es nicht entpacken: Beide Wege, eine Sicherung zurückzuholen, akzeptieren die `.zip` ebenso wie die `.json`.

Es gibt zwei Wege, eine Sicherungsdatei wieder einzuspielen:

- **Sicherung importieren** auf dem Reiter *Sicherungen* fügt die Datei der Sicherungsliste **dieses Buchs** hinzu. Nichts ändert sich, bis Sie sie *Wiederherstellen*.
- **Sicherung als neues Buch importieren** auf dem Reiter *Einstellungen* erstellt aus der Datei ein **neues Buch**.

Beide akzeptieren eine `.json`- oder eine `.zip`-Datei. Die Datei wird auf den Server hochgeladen und dort gelesen, daher ist auch eine große Sicherung kein Problem.

<a id="importing-a-backup"></a>

### Eine Sicherung importieren

Ist die Datei eine ZIP-Datei mit Bildern, werden die Bilder zuerst zu Ihren [Ressourcen](../resources.md) hinzugefügt, und die Sicherung verwendet sie dann. Ein Bild, das Sie bereits haben (dieselbe Datei), wird nicht erneut gespeichert: Es wird das vorhandene verwendet. Daher fügt der Import Ihres eigenen Exports nichts hinzu. Eine JSON-Datei ohne Bilder wird unverändert importiert.

Beim Import **als neues Buch** erhält das neue Buch **eigene Einträge** in den Ressourcen, auch wenn Sie dieselben Bilder bereits haben: Die Einträge verweisen auf dieselben Dateien, daher wird kein Speicherplatz doppelt belegt. Enthält die Datei keine Bilder (eine einfache JSON-Datei), erhält das neue Buch Kopien derjenigen Bilder, die die Datei auflistet und die Sie noch haben; die, die Sie nicht haben, werden ausgelassen. Alles in der Datei, das kein verwendbares Bild ist (kein PNG, JPEG, GIF oder WebP, oder zu groß), wird ignoriert.

<a id="importing-a-backup-as-a-new-book"></a>

### Eine Sicherung als neues Buch importieren

1. Öffnen Sie den Reiter **Einstellungen** und klicken Sie auf **Sicherung als neues Buch importieren**.
2. Geben Sie den **Name des neuen Buchs** ein – der Upload ist erst möglich, wenn der Name ausgefüllt ist.
3. Laden Sie die Sicherungsdatei hoch.
4. Entscheiden Sie über die Lorebooks, falls danach gefragt wird.

Das neue Buch erscheint auf dem Reiter *Bücher*.

<a id="deleting-backups"></a>

## Sicherungen löschen

Die Papierkorb-Schaltfläche entfernt eine Sicherung endgültig. Automatische Sicherungen werden nicht rotiert: Sie bleiben bestehen, bis Sie sie löschen.

<a id="automatic-backups"></a>

## Automatische Sicherungen

Marginalia kann beim Schreiben selbstständig eine Sicherung anlegen. Den Standard für alle Bücher legen Sie unter **Einstellungen → Allgemeine Einstellungen → Sicherungsstrategie** fest:

| Strategie | |
|---|---|
| **Keine Sicherungen** | Keine automatischen Sicherungen. |
| **Nach N Nachrichten** | Eine Sicherung nach jeweils *N* erzeugten Teilen. |
| **Nach N Minuten** | Eine Sicherung nach einem erzeugten Teil, wenn seit der letzten automatischen Sicherung mindestens *N* Minuten vergangen sind. |

Um für ein einzelnes Buch eine andere Strategie zu verwenden, aktivieren Sie auf seinem Reiter *Sicherungen* das Kontrollkästchen **Abweichung je Manuskript** und wählen dort Strategie und Anzahl. Deaktivieren Sie es, kehrt das Buch zum Standard aus *Einstellungen* zurück.

Automatische Sicherungen entstehen nur, nachdem ein Teil erzeugt wurde. Ein Buch, das Sie nur lesen, bekommt daher keine neuen Sicherungen.

<a id="where-backups-are-stored"></a>

## Wo Sicherungen gespeichert werden

Jede Sicherung ist eine JSON-Datei im Datenordner von Marginalia:

```
~/.marginalia/data/<your login>/backups/manuscripts/<book id>/
```

Bei der [Docker-Version](../getting-started/docker-server.md) liegt dieser Ordner auf dem Host unter `./data/data/<login>/...`. Die Dateien sind nicht in der Datenbank gespeichert, daher enthält eine [Datenbanksicherung](../administration/database-backups.md) sie nicht. Nehmen Sie den gesamten Ordner `.marginalia` in Ihre eigenen Sicherungen auf.

Die Bilddateien sind ebenfalls nicht in den Sicherungen enthalten; sie liegen im Ordner `data/<your login>/images` neben `backups`. Auch in einer [Datenbanksicherung](../administration/database-backups.md) sind sie nicht enthalten, siehe dort.
