---
label: Ressourcendateien
order: 85
---

# Ressourcendateien

Die Dateien der [Ressourcen](../resources.md) (die Bilder, die Nutzer an Story-Teile anhängen, hochgeladene Dateien) liegen im
Datenordner jedes Nutzers, in `data/<login>/images` und `data/<login>/resources`. Wird eine Ressource gelöscht oder ihr
Inhalt ersetzt, ändert sich nur die Datenbank: Die Datei bleibt auf der Festplatte, und weder die [Bereinigung](cleanup.md)
noch sonst etwas entfernt sie. **Ressourcendateien** findet die Dateien, die nicht mehr verwendet werden, lässt sie
herunterladen und löscht sie von der Festplatte, um Speicherplatz zurückzugewinnen.

<a id="cleaning-up-the-files"></a>

## Dateien bereinigen

1. Öffne den Tab **Ressourcendateien** der Admin-Seite und klicke auf **Dateien scannen**. Ein Fortschrittsbalken zeigt die
   gelesenen Ordner an. Es wird nichts verändert.
2. Die Tabelle listet die Dateien auf, **auf die keine Ressource verweist**: den Pfad (`<Nutzerordner>/images/<Datei>`), die
   Größe und das Datum der letzten Änderung. Alle sind angehakt. Entferne den Haken bei dem, was du behalten willst.
   **Alle auswählen** und **Keine auswählen** ändern alle Einträge, und der Pfeil im Kopf der ersten Spalte tut dasselbe.
   Eine Zeile über der Tabelle nennt, wie viele Dateien ungenutzt sind, ihre Gesamtgröße und wie viele angehakt sind.
3. Optional klicke auf **Als ZIP herunterladen**, um die angehakten Dateien in einem Archiv zu erhalten (die Einträge sind
   nach ihrem Pfad benannt), oder auf den Pfeil in einer Zeile für eine einzelne Datei. Das ist die Sicherung, die du vor dem
   Löschen aufbewahren solltest.
4. **Ausgewählte löschen** entfernt die angehakten Dateien nach Bestätigung von der Festplatte. **Schließen** leert die
   Tabelle, ohne etwas zu löschen.

![Der Tab Ressourcendateien nach einem Scan](../../images/admin-resource-files.png)

!!!warning
Gelöschte Dateien lassen sich nicht wiederherstellen. Lade sie im Zweifel vorher als ZIP herunter.
!!!

<a id="what-counts-as-used"></a>

## Was als verwendet gilt

Eine Datei gilt als verwendet, wenn **irgendeine Ressource irgendeines Nutzers auf sie verweist**, auch eine gelöschte: Eine
Ressource im [Papierkorb](../trash.md) kann wiederhergestellt werden, und ihr Bild muss dann wieder funktionieren. Die
Dateien gelöschter Ressourcen erscheinen deshalb erst in der Liste, nachdem die [Bereinigung](cleanup.md) die Ressourcen aus
der Datenbank entfernt hat, zusammen mit den Dateien ersetzter Inhalte (die alte Datei bleibt zurück) und den Dateien, die
fehlgeschlagene Uploads hinterlassen haben.

Die Dateien werden anhand des Namens verglichen (einer zufälligen Kennung), und zwar in den Ordnern aller Nutzer, auch in den
Ordnern `<login>-deleted` gelöschter Nutzer: Deren Dateien gelten weiterhin als *verwendet*, bis die Bereinigung den Nutzer
und seine Ressourcen endgültig entfernt.

Gelesen werden nur die Ordner `images` und `resources` in den Ordnern des Datenordners, sonst nichts (Datenbanksicherungen,
Arbeitsbereiche von Agenten und so weiter werden nicht angerührt).

Eine Datei, die ein Nutzer nach dem Scan hochlädt, wird nicht gelöscht: **Ausgewählte löschen** liest die Datenbank erneut und
behält die Dateien, auf die Ressourcen bis dahin verweisen (die Meldung nennt, wie viele Dateien behalten wurden).

Der Scan liest den Datenordner, was bei vielen Dateien eine Weile dauern kann; **Stopp** am Fortschrittsbalken bricht ihn ab.
