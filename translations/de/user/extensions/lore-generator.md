---
label: Lore-Generator
order: 10
---

# Lore-Generator

Lässt das Modell den Inhalt eines [Lorebuch-Eintrags](../lorebooks/entries.md) für dich schreiben. Es liest das Lore, das du bereits hast, damit der neue Eintrag zur Welt passt, und verdichtet das Ergebnis in die kurze, dichte Form, in der sich Lore-Einträge am besten eignen.

!!!
Der Lore-Generator funktioniert im [Telefon-Layout](../phone.md#extensions): Der Zauberstab-Button steht in der Zeile eines Eintrags, und das Fenster zeigt den Prompt oben und das Ergebnis darunter.
!!!

<a id="opening-the-generator"></a>

## Den Generator öffnen

Wenn die Erweiterung installiert ist, erhält jede Zeile des Lorebuch-Editors einen Zauberstab-Button (**Lore-Eintrag generieren**). Er öffnet den Generator für diesen Eintrag. Hat der Eintrag bereits Inhalt, weist der Dialog oben darauf hin: Das Kopieren des Ergebnisses in den Eintrag ersetzt den Inhalt.

Der [Vollbild-Editor](../lorebooks/lorebooks.md#editing-in-full-screen) hat den Zauberstab ebenfalls: in jeder Zeile seiner Eintragsliste und als **Lore-Eintrag generieren** in der Namenszeile eines Eintrag-Tabs. Was du in den Eintrag kopierst, erscheint sofort in dessen Tab. Wird er aus einem Buch heraus geöffnet (aus dessen Tab *Lorebook* oder dem Lorebuch-Tab der Geschichte), kennt der Editor das Buch, und das Buch bezahlt das Modell wie auf der Lorebuch-Seite.

Der Generator startet mit deinem **Standardmodell** und **Standardprotokoll** aus *Einstellungen*, siehe [Konto & Einstellungen](../account.md). Die Listen **Modell** und **Protokolle** unter der Kopfzeile des Dialogs erlauben es, für diese Generierung ein anderes Modell oder Protokoll zu verwenden (die Auswahl wird nicht gespeichert); das Protokoll ist optional. Ohne Modell zeigt er lediglich eine Warnung.

![Der Lore-Generator](../../images/plugin-loregenerator.png)

<a id="what-the-model-sees"></a>

## Was das Modell sieht

Die beiden Auswahlmöglichkeiten oben im Dialog bestimmen, welches Lore in die Anfrage aufgenommen wird:

| Auswahl | |
|---|---|
| **Aktuelles Lorebuch** (Standard) / **Gesamtes Lore** | Nur die Einträge des Lorebuchs, in dem der Eintrag steht, unter Ausschluss seiner [Unter-Lorebücher](../lorebooks/lorebooks.md); oder die Einträge dieses Lorebuchs zusammen mit allen seinen Unter-Lorebüchern. |
| **Nur bis Position (n)** (Standard) / **Alle Einträge** | *n* ist die Reihenfolge des Eintrags. Es werden nur die Einträge angezeigt, die davor stehen (eine niedrigere Reihenfolge oder dieselbe Reihenfolge und früher angelegt), damit das Modell auf dem aufbaut, was bereits existiert, und kein Lore bekommt, das nach dem Eintrag geschrieben wurde. *Alle Einträge* zeigt alles. |

Der Eintrag, den du gerade generierst, wird nie einbezogen, egal ob er Inhalt hat oder nicht. Ob andere Einträge oder Lorebücher aktiviert sind, spielt keine Rolle; ihre Tags, Filter und ihr Einfügemodus werden ignoriert. Was du hier auswählst, wird gesendet, in der Reihenfolge, in der die Einträge in einen Prompt eingefügt werden. Die Vorlagen der Einträge werden wie gewohnt verarbeitet.

## Prompts

Der Generierungs-Prompt ist das, was du das Modell schreiben lässt. Die Prompts sind in dem Kasten neben den Auswahlmöglichkeiten in derselben oberen Zeile aufgelistet; der Text des ausgewählten Prompts steht im Textfeld links, und du kannst ihn für eine einzelne Generierung ändern, ohne ihn zu speichern. Die beiden Hälften des Dialogs sind durch einen Teiler getrennt, den du ziehen kannst.

| Button | |
|---|---|
| **Speichern** | Speichert den Text unter dem ausgewählten Namen. |
| **Speichern unter** | Fragt nach einem Namen und speichert den Text als neuen Prompt (oder ersetzt nach Rückfrage einen Prompt mit diesem Namen). |
| **Löschen** | Löscht den ausgewählten Prompt. Wird der letzte gelöscht, kommt der eingebaute `DEFAULT`-Prompt zurück. |

Der eingebaute Prompt hat Platzhalter in eckigen Klammern (das Thema, die einzubeziehenden Details, die Länge, die Struktur): Fülle sie aus, bevor du ihn ausführst. Prompts gehören zu deinem Konto, und der zuletzt verwendete wird beim nächsten Öffnen des Dialogs wieder ausgewählt.

<a id="generation-and-compaction"></a>

## Generierung und Verdichtung

Die rechte Seite hat zwei Tabs.

**Generierung** - **Generierung starten** sendet das Lore, gefolgt vom Generierungs-Prompt. Die Antwort erscheint, während sie geschrieben wird, und die Überlegungen des Modells (falls es welche liefert) stehen darüber in einem Abschnitt *Denkprozess*. **Stopp** (anstelle von *Generierung starten*) beendet sie vorzeitig; was bis dahin geschrieben wurde, bleibt erhalten.

**Verdichtung** - verfügbar, sobald es eine Generierung gibt. **Verdichtung starten** sendet dasselbe Lore, gefolgt vom Verdichtungs-Prompt mit dem generierten Text darin, und das Modell verdichtet ihn zu einem kompakten Eintrag. Der Verdichtungs-Prompt wird unter *Einstellungen → Erweiterungs-Einstellungen → Lore-Generator* festgelegt; ist er leer, wird der eingebaute verwendet (er entfernt die Prosa und behält die Fakten). Wird die Generierung erneut ausgeführt, wird die Verdichtung verworfen.

![Die Verdichtung eines generierten Eintrags](../../images/plugin-loregenerator-compaction.png)

Während das Modell arbeitet, sind alle Steuerelemente außer **Stopp** gesperrt; auch den Dialog kannst du nicht schließen.

<a id="remembered-per-entry"></a>

### Pro Eintrag gespeichert

Wenn du **In Lore-Eintrag kopieren** wählst, werden die Optionen dieser Generierung zusammen mit dem Eintrag gespeichert: der Prompt-Text, welcher gespeicherte Prompt verwendet wurde, die Auswahl für Lore und Einträge sowie die angekreuzten Namenskategorien. Öffnest du den Generator für diesen Eintrag erneut, werden sie wiederhergestellt; sie haben Vorrang vor dem zuletzt verwendeten Prompt und deinen gespeicherten Namenskategorien. Ein Eintrag, für den nie generiert wurde, öffnet sich mit den üblichen Standardwerten. Die Optionen gehören zum Eintrag und wandern deshalb mit Sicherungen und Exporten des Lorebuchs mit.

<a id="tools"></a>

### Werkzeuge

Ist eine Sammlung von [Werkzeugen für das Modell](tools.md) installiert (Namen aus dem [Namensgenerator](name-generator.md) oder dem [Namensanbieter](name-provider.md), Orte aus dem [Ortsanbieter](place-provider.md)), hat die obere Zeile einen Button **Werkzeuge**. Er öffnet das Fenster, in dem du auswählst, welche Sammlungen und, bei denen mit Kategorien, welche Kategorien das Modell verwenden darf; siehe [Werkzeuge für das Modell](tools.md), wie das funktioniert. Solange etwas angekreuzt ist, kann das Modell während der Generierung die Werkzeuge aufrufen (nicht bei der Verdichtung), und der Hinweis jeder Sammlung, den es erhält, wird ans Ende des Generierungs-Prompts angehängt. Die Auswahl wird mit deinem Konto und mit dem Eintrag gespeichert.

<a id="attachments"></a>

### Anhänge

Unter dem Prompt kannst du Dateien hinzufügen, die das Modell ansehen soll: Referenzbilder, eine Karte, ein Charakterblatt. Lege sie auf den Upload-Bereich, wähle sie aus, oder füge sie mit Strg+V in den Prompt ein (ein Screenshot oder eine kopierte Datei wird zum Anhang; eingefügter Text wird wie üblich eingefügt). Bis zu 20 MB je Datei. Sie werden mit Name, Typ und Größe aufgelistet, und der Papierkorb-Button entfernt eine Datei. Sie werden mit der Generierung gesendet, nicht mit der Verdichtung, und du kannst sie nur hinzufügen und löschen, solange keine Generierung läuft.

Ob das Modell sie verwenden kann, hängt vom Modell und vom [Inferenzanbieter](../inference-providers.md) ab: Bilder werden als Bilder gesendet, Audio nur als WAV oder MP3, andere Dateien als Dateien. Lehnt der Anbieter einen Anhang ab, erhältst du eine Meldung, und es wird nichts generiert.

<a id="copying-the-result"></a>

## Das Ergebnis kopieren

**In Lore-Eintrag kopieren** ist verfügbar, sobald die Verdichtung ein Ergebnis hat. Der verdichtete Text wird zum **Inhalt** des Eintrags, und der generierte Text vor der Verdichtung kommt in seine **Notiz**. Beides wird ersetzt. Besteht die Verdichtung aus einem Codeblock (der eingebaute Prompt verlangt das, damit der Text leicht zu kopieren ist), wird der Codeblock darum entfernt. Das Lorebuch wird aktualisiert, und der Dialog bleibt offen; **Beenden** schließt ihn.
<a id="commands"></a>

## Befehle

Der Generator funktioniert auch ohne den Dialog, als drei [Befehle](../commands/index.md) für die Kommandozeile und für
[Agenten](../agents.md). Sie führen dieselben zwei Schritte aus wie der Dialog und speichern, ebenfalls wie der Dialog, nichts: Sie geben
den Text zurück, und ob er in einen Eintrag übernommen wird (was **Copy to Lore Entry** tut), entscheidet der Aufrufer. Alle drei lesen daher nur. Das Lorebook und der Eintrag werden über ihre Nummer angegeben, wie bei `/list-lorebooks` und `/list-lore-entries`; das Modell ist Ihr Standardmodell und -protokoll.

| Befehl | Was er tut |
|---|---|
| `/list-lore-templates [template]` | Listet Ihre gespeicherten Generierungs-Prompts (Name, Länge, Anfang des Textes) und den zuletzt verwendeten auf. Mit einem Namen gibt er den vollständigen Text dieses Prompts zurück. |
| `/generate-lore-entry [prompt [template [lorebook [entry [order [scope [limit [images [compact]]]]]]]]]` | Erzeugt einen Text mit dem Lore des Lorebooks, verdichtet ihn (wie `/compact-lore-entry`) und gibt den verdichteten Text zurück; mit `compact` `false` gibt er den erzeugten Text unverändert zurück. Der gesendete Text besteht aus einer Vorlage, gefolgt vom Prompt des Aufrufers (siehe unten). Mit `images` betrachtet das Modell zusätzlich Bilder, wie die Anhänge im Dialog. |
| `/compact-lore-entry text [lorebook [entry [order [scope [limit]]]]]` | Verdichtet den Text mit demselben Lore und gibt das Ergebnis zurück, ohne den Codeblock darum. |

Der Eintrag, für den der Text bestimmt ist, legt fest, welchen Lore das Modell sieht. Geben Sie sein Kürzel oder seine Nummer als `entry` an (und das `lorebook`, in dem er liegt, per Kürzel oder Nummer, wenn Sie die Nummer angeben), dann ist der Lore derselbe wie im
Dialog: Bei `limit` `UP_TO_POSITION` (dem Standard) sind es die Einträge vor ihm, der Eintrag selbst nie. Ein Eintrag, der noch nicht existiert, hat noch keine Nummer: Geben Sie stattdessen die `order` an, die er erhalten wird (die `order` aus `/list-lore-entries`, niedrigere kommt zuerst), dann sieht das Modell die Einträge vor dieser Reihenfolge; ohne eines von beiden wird der Text für einen neuen Eintrag nach allen anderen geschrieben. `scope` ist `CURRENT_LOREBOOK` (der Standard) oder `ALL_LORE`, `limit` ist `UP_TO_POSITION` oder `ALL_ENTRIES`, wie im Dialog.

Eine Generierung ist nur so gut wie ihr Prompt. Deshalb geht der Befehl immer von einem Ihrer gespeicherten Generierungs-Prompts aus (siehe
[Prompts](#prompts)): dem, der in `template` genannt ist, oder dem zuletzt verwendeten, wenn keiner genannt ist. Der `prompt` des Aufrufers
folgt ihm unter der Überschrift *Task for this entry* und enthält den Gegenstand, die Fakten, die Länge und die Abschnitte, so wie es die Vorlage verlangt. `template` `none` sendet nur den Prompt, für einen Aufrufer, der den ganzen Prompt selbst geschrieben hat, etwa nachdem er den Text einer Vorlage aus `/list-lore-templates` geholt und bearbeitet hat. Ein Befehl, der mit einer Vorlage und ohne Prompt aufgerufen wird, sendet nur die Vorlage.

Die `images` von `/generate-lore-entry` sind die Bilder, die der Dialog der Generierung anhängen würde. Ein [Agent](../agents.md) übergibt eine Liste von
Pfaden zu Bilddateien seines Arbeitsbereichs (`/refs/map.png`; höchstens 8, PNG, JPEG, GIF oder WebP, bei großen Bildern verkleinert), die Sie dort mit
**Datei hochladen** im Reiter Workspace ablegen. Die Befehlsleiste hat keine Möglichkeit, ein Bild zu nennen, daher ist `images` für Agenten gedacht. Es ist Ihr Standardmodell, das die
Bilder betrachtet: Wenn es keine Bilder verarbeitet, schlägt der Befehl fehl und meldet das. Nur die Generierung nimmt Bilder entgegen, wie im Dialog; die Verdichtung arbeitet nur mit dem Text.

Der Parameter `compact` von `/generate-lore-entry` ist standardmäßig `true`: Der Befehl generiert und verdichtet in einem Aufruf, sodass ein [Agent](../agents.md) einen einzigen Befehl braucht und den zweiten Schritt nicht vergessen kann. Übergeben Sie `compact` `false`, um den langen erzeugten Text zu erhalten (etwa um ihn zu prüfen oder zu bearbeiten), und führen Sie `/compact-lore-entry` später darauf aus.

Das Modell bekommt hier keine Werkzeuge. Ein Aufrufer, der Namen möchte, fragt sie zuerst über `/generate-names` des
[Namensgenerators](name-generator.md) oder `/pick-real-names` des [Namensanbieters](name-provider.md) ab und schreibt die, die ihm gefallen, in den Prompt. Ein Agent kann auch den Text einer Vorlage aus `/list-lore-templates` übernehmen, ihn ändern und das
Ergebnis als Prompt übergeben.

<a id="storage"></a>

## Speicherung

Die Prompts und der Verdichtungs-Prompt werden zusammen mit Ihren Einstellungen gespeichert, daher gehören sie zu den [Datenbank-Backups](../administration/database-backups.md).
Mit dem Lorebook wird nichts gespeichert; der erzeugte Text existiert nur im Dialog, bis Sie ihn kopieren.
