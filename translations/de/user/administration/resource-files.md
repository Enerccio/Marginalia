---
label: Ressourcendateien
order: 85
---

# Ressourcendateien

Die Dateien der [Ressourcen](../resources.md) (die Bilder, die Benutzer an Story-Abschnitte anhängen, hochgeladene Dateien) werden im
Datenordner jedes Benutzers gespeichert, in `data/<login>/images` und `data/<login>/resources`. Das Löschen einer Ressource oder das Ersetzen ihres
Inhalts ändert nur die Datenbank: Die Datei bleibt auf der Festplatte, und weder die [Bereinigung](cleanup.md) noch sonst etwas
entfernt sie. **Ressourcendateien** findet die Dateien, die nichts mehr verwendet, lässt Sie diese herunterladen und von der
Festplatte löschen, um Speicherplatz zurückzugewinnen.

<a id="cleaning-up-the-files"></a>

## Dateien bereinigen

1. Öffnen Sie den Reiter **Ressourcendateien** der Administrationsseite und klicken Sie auf **Dateien scannen**. Ein Fortschrittsbalken zeigt die Ordner, die gelesen werden.
   Es wird nichts verändert.
2. Die Tabelle listet die Dateien auf, **auf die keine Ressource verweist**: den Pfad (`<user folder>/images/<file>`), die Größe und das Datum, an dem
   sie zuletzt geändert wurden. Alle sind angehakt. Haken Sie ab, was Sie behalten möchten. **Alle auswählen** und **Keine auswählen** ändern alle
   Einträge, und der Pfeil im Kopf der ersten Spalte tut dasselbe. Eine Zeile über der Tabelle zeigt, wie viele der Dateien
   ungenutzt sind, ihre Größe und wie viele angehakt sind.
3. Klicken Sie optional auf **Als ZIP herunterladen**, um die angehakten Dateien in einem Archiv zu erhalten (die Einträge
   heißen nach ihrem Pfad), oder auf den Pfeil in einer Zeile für eine einzelne Datei. Das ist die Sicherungskopie, die Sie vor dem Löschen aufbewahren sollten.
4. **Ausgewählte löschen** entfernt die angehakten Dateien nach Bestätigung von der Festplatte. **Schließen** leert die Tabelle, ohne etwas zu löschen.

![Der Reiter Ressourcendateien nach einem Scan](../../images/admin-resource-files.png)

!!!warning
Gelöschte Dateien können nicht wiederhergestellt werden. Laden Sie sie im Zweifel vorher als ZIP herunter.
!!!

<a id="what-counts-as-used"></a>

## Was als verwendet gilt

Eine Datei gilt als verwendet, wenn **irgendeine Ressource irgendeines Benutzers auf sie verweist**, auch eine gelöschte: Eine Ressource
im [Papierkorb](../trash.md) kann wiederhergestellt werden, und ihr Bild muss dann wieder funktionieren. Die Dateien gelöschter Ressourcen erscheinen
daher erst in der Liste, nachdem die [Bereinigung](cleanup.md) die Ressourcen aus der Datenbank entfernt hat, zusammen mit den Dateien ersetzter
Inhalte (die alte Datei bleibt zurück) und Dateien, die durch fehlgeschlagene Uploads übrig geblieben sind.

Die Dateien werden anhand ihres Namens (einer zufälligen Kennung) verglichen, und zwar in den Ordnern aller Benutzer, auch in den
`<login>-deleted`-Ordnern gelöschter Benutzer: Deren Dateien gelten weiterhin als *verwendet*, bis die Bereinigung den Benutzer und seine Ressourcen endgültig entfernt.

Gelesen werden nur die Ordner `images` und `resources` innerhalb des Datenordners, sonst nichts (Datenbanksicherungen, Arbeitsbereiche
von Agenten und Ähnliches bleiben unberührt).

Eine Datei, die ein Benutzer nach dem Scan hochlädt, wird nicht gelöscht: **Ausgewählte löschen** liest die Datenbank erneut und behält die Dateien,
auf die Ressourcen bis dahin verweisen (die Meldung nennt, wie viele Dateien behalten wurden).

Der Scan liest den Datenordner, was bei vielen Dateien eine Weile dauern kann; **Stopp** auf dem Fortschrittsbalken bricht ihn ab.
