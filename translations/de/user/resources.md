---
label: Ressourcen
order: 960
icon: image
---

# Ressourcen

Der Reiter **Resources** listet die Dateien auf, die Sie in Marginalia hochgeladen haben: die [Bilder der Teile](books/story-editor.md#images) Ihrer Bücher und die Audiodateien, die Erweiterungen wie das [Audiobook](extensions/audiobook.md) dazu speichern. Im Reiter finden Sie eine Datei wieder, hören Audio an, laden Dateien herunter, tauschen eine Datei gegen eine bessere aus, ohne die Bücher anzutasten, die sie verwenden, und entfernen, was Sie nicht mehr brauchen.

![Der Reiter Resources](../images/resources-tab.png)

Die Dateien sind nach dem gruppiert, was sie verwendet: Jede Zeile der Tabelle ist ein Objekt (zum Beispiel ein Teil eines Buches, `ChatMessage #123`) mit der Anzahl seiner Dateien, ihrer Gesamtgröße und dem Datum der neuesten Datei. Klicken Sie in der Zeile auf den Pfeil, um das Objekt zu öffnen und seine Dateien zu sehen. Die Tabelle lädt beim Scrollen und beim Öffnen einer Gruppe nach, daher bleibt sie auch bei Tausenden Dateien schnell. Klicken Sie auf eine Spaltenüberschrift, um danach zu sortieren; die Gruppen und die Dateien darin folgen. Standardmäßig stehen die neuesten oben.

| Zeile / Spalte | |
|---|---|
| **Used by** | Das Objekt, dem die Dateien hinzugefügt wurden. *nothing* gruppiert die Dateien, die von keinem Objekt verwendet werden. *(gone)* hinter dem Namen bedeutet, dass das Objekt gelöscht wurde oder nicht mehr existiert; die Dateien werden vermutlich nicht mehr benötigt: Die [Bereinigung](administration/cleanup.md) durch den Administrator entfernt sie, sobald nichts anderes sie verwendet. Innerhalb einer Gruppe: eine kleine Vorschau eines Bildes und der Name der Datei, wie sie hochgeladen wurde. |
| **Type** | Der Typ der Datei, z. B. `image/png`. |
| **Size** | Die Größe der Datei, bei einer Gruppe die Summe ihrer Dateien. |
| **Created** | Wann die Datei hochgeladen wurde, bei einer Gruppe die neueste Datei. |

Dies ist ein Hinweis zu Ihrer Information. Ein Bild funktioniert auch noch, nachdem der Teil, dem es hinzugefügt wurde, verschwunden ist, und dasselbe Bild kann von mehreren Teilen verwendet werden (zum Beispiel hat ein Zweig, den Sie nach dem [Verzweigen](books/branches-and-story-tree.md) der Geschichte anlegen, dieselben Bilder wie der Teil, aus dem er entstanden ist). Die Bereinigung behält eine Datei, solange noch ein Teil sie verwendet.

<a id="play"></a>

## Abspielen

Eine Datei eines Audiotyps (`audio/mpeg`, `audio/wav`...) hat vor den anderen Schaltflächen eine Schaltfläche **Play** (Dreieck). Sie spielt die Datei auf der Seite ab. Während sie läuft, wird die Schaltfläche zu **Pause**; wenn Sie die Wiedergabe anhalten oder das Audio endet, wird sie wieder zu Play. Es spielt immer nur eine Datei: Wenn Sie eine andere starten, stoppt die zuvor laufende.

<a id="download"></a>

## Herunterladen

Die Download-Schaltfläche (Pfeil nach unten) in der Zeile speichert die Datei auf Ihrem Computer.

<a id="replace-the-content"></a>

## Inhalt ersetzen

Die Upload-Schaltfläche in der Zeile öffnet einen Dialog, in dem Sie eine neue Datei auswählen. Der neue Inhalt wird als neue Datei gespeichert, und der Eintrag verweist nun darauf. Dadurch **zeigt alles, was den Eintrag verwendet, den neuen Inhalt**; Sie müssen die Teile nicht ändern. Verwenden Sie diese Funktion, um ein Bild durch eine bessere Version zu ersetzen oder einen falschen Upload zu korrigieren.

- Ein Bild kann nur durch ein Bild ersetzt werden (PNG, JPEG, GIF oder WebP, bis 10 MB, wie beim [Hinzufügen von Bildern](books/story-editor.md#images)).
- Der Name des Eintrags wird zum Namen der neuen Datei.
- Die bisherige Datei wird nicht aus dem Datenordner gelöscht, siehe [unten](#what-delete-does).

![Ersetzen des Inhalts einer Ressource](../images/resources-replace.png)

<a id="deleting"></a>

## Löschen

Die Papierkorb-Schaltfläche in der Zeile löscht eine Datei. Um mehrere Dateien zu löschen, markieren Sie sie in der ersten Spalte und wählen oberhalb der Tabelle **Delete selected**. Wenn Sie eine Gruppe markieren (eine Zeile eines Objekts), werden alle ihre Dateien gelöscht. Das Kontrollkästchen in der Kopfzeile wählt alle Gruppen aus. Marginalia fragt nach, bevor es löscht.

<a id="what-delete-does"></a>

### Was Löschen bewirkt

- Die Datei verschwindet aus der Tabelle und aus den Teilen, die sie verwenden: Ein Teil zeigt dann einfach kein Bild mehr. Text, Zusammenfassungen und Exporte des Teils bleiben unverändert.
- Der Eintrag wird nur **als gelöscht markiert**. Die [Bereinigung](administration/cleanup.md) in der Administration entfernt ihn endgültig aus der Datenbank.
- **Die Datei bleibt im Datenordner** (`data/<your login>/images`), auch nach der Bereinigung. Das Löschen einer Ressource gibt nicht von selbst Speicherplatz frei: Ein Administrator entfernt die Dateien, die nichts mehr verwenden, im Reiter [Resource Files](administration/resource-files.md) der Administration.
- Solange die Bereinigung den Eintrag nicht entfernt hat, können Sie ihn aus dem [Trash](trash.md) zurückholen: Die Datei liegt noch im Datenordner, daher funktioniert das Bild wieder. Nach der Bereinigung enthält nur eine [Sicherung der gesamten Datenbank](administration/database-backups.md), die vorher erstellt wurde, den Eintrag; die Datei selbst liegt weiterhin im Ordner.
