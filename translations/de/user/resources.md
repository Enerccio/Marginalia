---
label: Ressourcen
order: 960
icon: image
---

# Ressourcen

Der Reiter **Ressourcen** listet die Dateien auf, die Sie in Marginalia hochgeladen haben: die [Bilder der Teile](books/story-editor.md#images) Ihrer Bücher und die Audiodateien, die Erweiterungen wie das [Hörbuch](extensions/audiobook.md) dazu speichern. Der Reiter dient dazu, eine Datei wiederzufinden, Audio anzuhören, sie herunterzuladen, sie durch eine bessere zu ersetzen, ohne die Bücher anzufassen, die sie verwenden, und das zu löschen, was Sie nicht mehr brauchen.

![Der Reiter Ressourcen](../images/resources-tab.png)

Die Dateien sind danach gruppiert, wovon sie verwendet werden: Jede Zeile der Tabelle ist ein Objekt (zum Beispiel ein Teil eines Buchs, `ChatMessage #123`) mit der Anzahl seiner Dateien, deren Gesamtgröße und dem Datum der neuesten. Klicken Sie in der Zeile auf den Pfeil, um das Objekt zu öffnen und seine Dateien zu sehen. Die Tabelle lädt beim Scrollen und beim Öffnen einer Gruppe nach, sodass sie auch mit Tausenden Dateien schnell bleibt. Ein Klick auf eine Spaltenüberschrift sortiert nach dieser Spalte; die Gruppen und die Dateien in ihnen folgen der Sortierung. Standardmäßig stehen die neuesten zuerst.

| Zeile / Spalte | |
|---|---|
| **Verwendet von** | Das Objekt, dem die Dateien hinzugefügt wurden. *nichts* gruppiert die Dateien, die von nichts verwendet werden. *(gelöscht)* hinter dem Namen bedeutet, dass das Objekt gelöscht wurde oder nicht mehr existiert; die Dateien werden vermutlich nicht mehr gebraucht. Innerhalb einer Gruppe: eine kleine Vorschau eines Bildes und der Name der Datei, so wie sie hochgeladen wurde. |
| **Typ** | Der Typ der Datei, z. B. `image/png`. |
| **Größe** | Die Größe der Datei, bei einer Gruppe die Summe ihrer Dateien. |
| **Erstellt** | Wann die Datei hochgeladen wurde, bei einer Gruppe die neueste Datei. |

Dies ist nur ein Hinweis zu Ihrer Information. Marginalia erzwingt ihn nicht: Ein Bild funktioniert auch noch, nachdem der Teil, dem es hinzugefügt wurde, verschwunden ist, und dasselbe Bild kann von mehreren Teilen verwendet werden (wenn Sie zum Beispiel die Geschichte [verzweigen](books/branches-and-story-tree.md), hat der Zweig dieselben Bilder wie der Teil, aus dem er entstanden ist).

<a id="play"></a>

## Abspielen

Eine Datei mit einem Audiotyp (`audio/mpeg`, `audio/wav` ...) hat vor den anderen Schaltflächen eine **Abspielen**-Schaltfläche (Dreieck). Sie spielt die Datei auf der Seite ab; während sie läuft, wird die Schaltfläche zu **Pausieren**, und wenn Sie anhalten oder das Audio endet, wird sie wieder zu Abspielen. Es spielt immer nur eine Datei: Wenn Sie eine andere starten, wird die laufende gestoppt.

<a id="download"></a>

## Herunterladen

Die Download-Schaltfläche (Pfeil nach unten) in der Zeile speichert die Datei auf Ihrem Computer.

<a id="replace-the-content"></a>

## Inhalt ersetzen

Die Upload-Schaltfläche in der Zeile öffnet einen Dialog, in dem Sie eine neue Datei auswählen. Der neue Inhalt wird als neue Datei gespeichert, und der Eintrag verweist nun darauf. Daher **zeigt alles, was den Eintrag verwendet, den neuen Inhalt**; Sie müssen die Teile nicht ändern. Verwenden Sie dies, um ein Bild durch eine bessere Version zu ersetzen oder einen falschen Upload zu korrigieren.

- Ein Bild kann nur durch ein Bild ersetzt werden (PNG, JPEG, GIF oder WebP, bis 10 MB, wie beim [Hinzufügen von Bildern](books/story-editor.md#images)).
- Der Name des Eintrags wird zum Namen der neuen Datei.
- Die bisherige Datei wird nicht aus dem Datenordner gelöscht, siehe [unten](#what-delete-does).

![Inhalt einer Ressource ersetzen](../images/resources-replace.png)

<a id="deleting"></a>

## Löschen

Die Papierkorb-Schaltfläche in der Zeile löscht eine Datei. Um mehrere zu löschen, markieren Sie sie in der ersten Spalte und wählen oberhalb der Tabelle **Ausgewählte löschen**. Das Markieren einer Gruppe (einer Zeile eines Objekts) löscht alle ihre Dateien. Das Kontrollkästchen in der Kopfzeile wählt alle Gruppen aus. Marginalia fragt nach, bevor etwas gelöscht wird.

<a id="what-delete-does"></a>

### Was Löschen bewirkt

- Die Datei verschwindet aus der Tabelle und aus den Teilen, die sie verwenden: Ein Teil zeigt dann einfach kein Bild mehr. Text, Zusammenfassungen und Exporte des Teils werden nicht verändert.
- Der Eintrag wird nur **als gelöscht markiert**. Die [Bereinigung](administration/cleanup.md) in der Administration entfernt ihn endgültig aus der Datenbank.
- **Die Datei bleibt im Datenordner** (`data/<your login>/images`), auch nach der Bereinigung. Das Löschen einer Ressource gibt also nicht von selbst Speicherplatz frei: Ein Administrator entfernt die Dateien, die nichts mehr verwenden, im Reiter [Ressourcendateien](administration/resource-files.md) der Administration.
- Bis die Bereinigung den Eintrag entfernt, können Sie ihn aus dem [Papierkorb](trash.md) wiederherstellen: Die Datei liegt noch im Datenordner, daher funktioniert das Bild wieder. Nach der Bereinigung enthält nur ein [Backup der gesamten Datenbank](administration/database-backups.md), das vorher erstellt wurde, den Eintrag; die Datei selbst liegt weiterhin im Ordner.
