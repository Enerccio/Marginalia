---
label: Konverter
order: 16
---

# Konverter

!!!
Diese Erweiterung hieß vor 1.6.0 *SillyTavern Converter*. Nur der Name hat sich geändert; ihre Dateien, die mit Ihren Büchern gespeicherten Daten und die gespeicherten Layouts sind dieselben.
!!!

Schreiben Sie einen [SillyTavern](https://github.com/SillyTavern/SillyTavern)-Rollenspiel-Chat Runde für Runde als Geschichte neu: Laden Sie den Chat (oder eine [LM Studio](https://lmstudio.ai)-Unterhaltung) in ein Buch, lassen Sie das Modell ihn anhand des Chats und der Lore des Buchs in *Rundendetails* aufteilen, und übernehmen Sie jede Runde mit einem Klick in das neue Rundenformular der Geschichte.

Die Quelle muss kein Chat sein: Der Konverter kann auch die Teile eines anderen Buchs von Ihnen (aus einer Sicherungsdatei oder dem Buch selbst) übernehmen und Ihnen deren Anweisungen einzeln geben, um [ein Buch neu zu entwerfen](#redrafting-a-book).

Um direkt aus einem Chat ein Buch zu machen (jede Nachricht oder jedes Paar aus Nutzer- und Assistenten-Nachricht, ein Teil), siehe [Import aus SillyTavern](../books/managing-books.md#importing-from-sillytavern). Der Konverter dient dem umgekehrten Weg: Sie behalten den Chat neben sich und schreiben die Geschichte mit dem Modell neu, anhand von Anweisungen, die aus dem Chat entstehen.

!!!
Der Konverter funktioniert auch im [Telefon-Layout](../phone.md#extensions), siehe [Auf dem Telefon](#on-a-phone).
!!!

<a id="the-converter-tab"></a>

## Der Konverter-Reiter

Die linke Seitenleiste des [Geschichteneditors](../books/story-editor.md) erhält einen Reiter **Konverter** (kleiner Text; der Reiter scrollt, wenn die Seitenleiste nicht genug Platz hat). Über seinen beiden Listen steht der Abschnitt **Convert to turn details**, der geschlossen ist, bis Sie ihn öffnen, damit die Listen Platz haben. Er enthält:

| | |
|---|---|
| **Modell** und **Protokoll** | Womit der Zauberstab arbeitet. Sie übernehmen zunächst Modell und Protokoll des Buchs; die Auswahl wird mit dem Buch gespeichert. |
| **Von**, **Bis** | Der Bereich der Nachrichten, auf dem der Zauberstab arbeitet. Geben Sie die Nummern ein, klicken Sie in den Chat oder verwenden Sie **Alle**. |
| Token-Anzahl | Wie viele Tokens die Anfrage benötigt: die Nachrichten des Bereichs plus Prompt und Lore (einmal gezählt). Verwenden Sie sie, um den Bereich im Kontext des Modells zu halten. |
| **Bereich konvertieren** | Der Zauberstab: Er fragt das Modell nach den Rundendetails des Bereichs. Während das Modell arbeitet, wird daraus **Stopp**. |
| Stift-Schaltfläche | Bearbeitet den Prompt des Zauberstabs (Variablen `{{backgroundLore}}`, `{{chat}}`, `{{firstMessage}}`, `{{lastMessage}}`, `{{previousTurn}}`), mit einer Schaltfläche **Standard**. |
| **An Runden ausrichten** | Verschiebt den Bereich an die Grenzen der Runden, siehe unten. Wird mit dem Buch gespeichert. |
| **Automatischer Modus** | Zeigt die Steuerelemente, die den gesamten Bereich in Stapeln konvertieren, siehe unten. |

<a id="align-to-turns"></a>

### An Runden ausrichten

Eine Runde der Geschichte besteht aus den Nachrichten des Nutzers (den Anweisungen), gefolgt von der Nachricht der Figur. Ist **An Runden ausrichten** angehakt, wird jeder Bereich, den Sie wählen (durch Klicken, Eingabe der Nummern oder **Alle**), an die Grenzen der Runden verschoben: Der Anfang rückt zur ersten Nachricht seiner Runde zurück, und das Ende rückt zur letzten Nachricht der Figur davor zurück, sodass der Bereich nie auf einer Nachricht des Nutzers endet, die zur folgenden Runde gehört. Das Ende des Chats bleibt, wo es ist. Ein Bereich, der ganz in den Nachrichten des Nutzers vor einer Antwort liegt, wird bis zu dieser Antwort nach vorn erweitert.

<a id="auto-mode"></a>

### Automatischer Modus

**Automatischer Modus** zeigt eine Zeile mit **Nachrichten pro Stapel**, die Schaltfläche **In Stapeln konvertieren** und die Anzahl der Anfragen, die das ergibt, mit den Tokens, die sie insgesamt benötigen. Die Schaltfläche teilt den aktuellen Bereich in Stapel mit so vielen Nachrichten und schickt sie nacheinander an das Modell (nach Rückfrage), genauso wie der Zauberstab es für einen Bereich tut. Mit **An Runden ausrichten** endet jeder Stapel am Ende einer Runde (der letzten Nachricht der Figur innerhalb der Größe oder dem Ende der Runde, wenn sie länger als die Größe ist), sodass keine Runde zerschnitten wird; ohne diese Option werden die Stapel genau nach der Anzahl geschnitten, und eine Runde an der Grenze kann in zwei geteilt werden.

Während der Lauf läuft, zeigt der Chat den gerade bearbeiteten Stapel, die Statuszeile sagt, welcher Stapel von wie vielen es ist, und die Schaltfläche wird zu **Stopp**. Die Rundendetails jedes Stapels werden gespeichert, sobald er fertig ist, wie beim Zauberstab (die Runden, die den Bereich berühren und die Sie nicht angehakt haben, werden ersetzt). Wenn alle Stapel fertig sind, ist der Bereich wieder so, wie Sie ihn hatten, und der Reiter **Rundendetails** öffnet sich. Ein Stapel, dessen Antwort nicht verwendet werden kann (leer oder nicht dem entsprechend, was angefragt war), wird erneut gesendet, insgesamt bis zu dreimal. Wenn Sie abbrechen oder ein Stapel weiterhin fehlschlägt, nennt die Benachrichtigung den Grund, und der Bereich besteht aus dem, was übrig ist, sodass Sie von dort aus weitermachen können.

<a id="what-a-range-continues-from"></a>

### Woran ein Bereich anknüpft

Der Prompt teilt dem Modell die Details der letzten Runde mit, die vor dem Bereich endet (ihr Szenenort, die Perspektivfigur, die anwesenden Figuren und Anweisungen, in `{{previousTurn}}`), damit die Runden des Bereichs dieselbe Perspektive und Szene behalten, bis der Chat sie ändert. So knüpft ein Stapel des automatischen Modus an den vorherigen an: Die Rundendetails eines Stapels werden gespeichert, bevor der nächste gesendet wird. Gibt es keine Runde vor dem Bereich, wird dieser Teil des Prompts weggelassen.

<a id="the-chat"></a>

## Der Chat

Laden Sie die Datei `.jsonl` eines Chats hoch (in SillyTavern liegt sie in `data/<user>/chats/<character>/`, oder Sie exportieren sie aus dem Chat), oder eine Unterhaltung aus LM Studio (`<number>.conversation.json` im Ordner `.lmstudio/conversations` Ihres Benutzerordners). Die beiden werden anhand des Inhalts der Datei unterschieden. Der Chat darf keine Verzweigungen haben: Von jeder Nachricht wird die Variante gelesen, die angezeigt wurde (in LM Studio die Version, die angezeigt wurde), und ausgeblendete Systemnachrichten und leere Nachrichten werden weggelassen; der Systemprompt einer LM-Studio-Unterhaltung, deren Denkvorgänge und die Aufrufe von Werkzeugen sind keine Nachrichten des Chats. Hat Ihr Chat Verzweigungen, importieren Sie zuerst den Zweig, den Sie neu schreiben möchten (siehe [Import aus SillyTavern](../books/managing-books.md#importing-from-sillytavern) zum Zusammenführen von Zweigen zu einem Baum).

Der Reiter **Chat** ist eine Tabelle der Nachrichten: ein Kontrollkästchen, die Nummer und die Nachricht, mit dem Namen des Sprechers fett über dem Text (Nachrichten des Nutzers sind mit `»` markiert).

- Haken Sie das Kontrollkästchen einer Nachricht an, die Sie durchgegangen sind, damit Sie sehen, was noch übrig ist. Die Schaltfläche mit dem Doppelpfeil über der Tabelle springt zur nächsten nicht angehakten Nachricht (nach der zuletzt angehakten oder angeklickten, und am Ende wieder von oben).
- **Klicken** Sie auf eine Nachricht, um sie zum Bereich zu machen, **Umschalt+Klick** auf eine andere, um den Bereich bis dorthin zu erweitern. Der Bereich wird hervorgehoben, und die Token-Anzahl folgt.
- Die Schaltfläche mit dem Kreuz über der Tabelle entfernt den Chat und die Rundendetails aus dem Buch (nach Rückfrage).

![Der Konverter-Reiter mit einem Chat und dem gewählten Bereich](../../images/plugin-stconverter-chat.png)

<a id="turn-details"></a>

## Rundendetails

Verwenden Sie den Zauberstab, und der Reiter **Rundendetails** öffnet sich mit dem, was das Modell vorbereitet hat, in der Reihenfolge des Chats. Eine Zeile ist eine Runde: die Nachrichten, aus denen sie entstanden ist, und in einem Element der Szenenort, die Perspektivfigur, die anwesenden Figuren und die Anweisungen, jeweils unter einer fetten Zeile mit ihrem Namen. Das Modell wird angewiesen, jede Nachricht des Bereichs genau einmal abzudecken; Nummern außerhalb des Bereichs werden auf ihn beschnitten.

- **Klicken Sie auf eine Zeile**, um das *neue Runden*-Formular der Geschichte zu öffnen und alle vier Felder der Runde hineinzukopieren. Ändern Sie, was Sie möchten, und generieren Sie. (Das Kontrollkästchen und die Löschen-Schaltfläche einer Zeile öffnen das Formular nicht.)
- Haken Sie das Kontrollkästchen einer Runde an, die Sie verwendet haben; der Doppelpfeil geht zur nächsten nicht angehakten Runde.
- Die Zusammenführen-Schaltfläche einer Runde **führt sie mit der nächsten zusammen**: Ein Formular öffnet sich mit den Details beider bereits verbunden (die Szenenorte und Anweisungen untereinander, die anwesenden Figuren beider, die Perspektivfigur der ersten; hat die zweite Runde eine andere, zeigt das Formular, welche). Ändern Sie, was nicht passt, und drücken Sie **Zusammenführen** (oder <kbd>Strg/Cmd+Enter</kbd>): Die beiden Zeilen werden zu einer, die die Nachrichten beider abdeckt und nur angehakt ist, wenn beide es waren.
- Die Pfeile einer Runde **verschieben sie nach oben oder unten** in der Liste, um die Runden in einer anderen Reihenfolge zu schreiben, als der Chat sie hat (eine Runde behält die Nachrichten, die sie abdeckt). Runden, die aus den Teilen eines Buchs entstanden sind, können nicht verschoben werden; ihre Reihenfolge ist die der Teile.
- Wenn Sie den Zauberstab für einen Bereich erneut ausführen, **ersetzt** er die früheren Runden, die den Bereich berühren, außer den angehakten; die neuen Runden kommen an ihren Platz in der Reihenfolge des Chats, und die von Ihnen durch Verschieben gewählte Reihenfolge bleibt erhalten.

![Die Rundendetails, die das Modell vorbereitet hat](../../images/plugin-stconverter-turns.png)
<a id="redrafting-a-book"></a>

## Umarbeiten eines Buchs

Um ein Buch Abschnitt für Abschnitt neu zu schreiben, lädst du das alte Buch als Quelle. Solange noch keine Quelle vorhanden ist, zeigt der Tab **Upload a chat**, **Load a backup** und **Pick a book**:

- **Pick a book** öffnet eine Liste deiner Bücher (nicht das, in dem du gerade bist). Die Abschnitte des Zweigs, auf dem das Buch liegt, werden vom ersten Abschnitt bis zu dem geladen, bei dem das Buch jetzt steht.
- **Load a backup** nimmt eine Sicherungsdatei eines Buchs: die JSON-Datei, die `.zip`-Datei mit den Bildern oder eine Datei, die du aus den [Sicherungen](../books/backups.md) eines Buchs heruntergeladen hast. Die Sicherung wird nur gelesen, es wird nichts wiederhergestellt. Der Zweig ist der, auf dem das Buch lag, als die Sicherung erstellt wurde.

Von jedem Abschnitt behält der Konverter die Anweisungen, den Schauplatz, die Figur aus der Erzählperspektive, die anwesenden Figuren und den Text des Abschnitts, sonst nichts (keine Zusammenfassungen, kein Lorebook, keine Überlegungen, keine Bilder, keine anderen Zweige und keine Einstellungen). So bleibt auch ein langes Buch klein. Ein Buch ohne Abschnitte lädt nichts, und eine Sicherung, deren Abschnitte nicht zum ersten Abschnitt zurückführen, wird als beschädigt abgelehnt.

Der Tab hat dann zwei Listen, wie bei einem Chat, aber ohne den Zauberstab (das Buch hat seine Anweisungen bereits, daher gibt es den Abschnitt **Convert to turn details** nicht):

- **Parts** zeigt den Text jedes Abschnitts mit dem Kontrollkästchen und dem Pfeil zum nächsten nicht markierten Abschnitt. Lies den alten Text neben der Geschichte, die du schreibst.
- **Turn details** enthält pro Abschnitt eine Zeile mit Schauplatz, Figur aus der Erzählperspektive, anwesenden Figuren und Anweisungen. **Klicke auf eine Zeile**, um sie in das neue Turn-Formular der Geschichte zu übernehmen, wie bei einem Chat, und den neuen Abschnitt zu generieren. Markiere die Turns, die du erledigt hast. Zwei Turns können wie bei einem Chat **zusammengeführt** werden (über die Zusammenführen-Schaltfläche des ersten), um zwei Abschnitte des alten Buchs als einen zu schreiben; die zusammengeführten Turns bleiben beim Buch erhalten.

Zu einem Buch wird nur eine Quelle gespeichert. Um eine andere zu laden, entfernst du die aktuelle mit dem Kreuz (nach Rückfrage). Dadurch werden auch die dafür gesetzten Markierungen vergessen.

<a id="on-a-phone"></a>

## Auf dem Telefon

In der Telefonansicht befindet sich der Tab **Converter** in der Schublade der Geschichte, neben der Gliederung und den anderen Tabs der Erweiterungen (die Schublade öffnest du mit der Schaltfläche oben links in der Geschichte). Alles oben Beschriebene funktioniert gleich, mit diesen Unterschieden:

- Es gibt keine Umschalttaste, daher wird der **Bereich mit zwei Tipps gewählt**: Tippe auf eine Nachricht, um den Bereich zu beginnen, tippe auf eine andere, um ihn zu beenden (der Bereich reicht von der einen bis zur anderen, in welcher Reihenfolge auch immer); der nächste Tipp beginnt einen neuen Bereich. Du kannst auch **Von** und **Bis** eintippen oder **Alle** verwenden.
- Die Turn-Details haben keine Spalte für die Nummern der Nachrichten: Sie stehen als erste Zeile jedes Turns (**Msgs**).
- **Tippe auf einen Turn**, dann schließt sich die Schublade und das neue Turn-Formular der Geschichte öffnet sich mit den vier ausgefüllten Feldern.
- Der Tab ist im Querformat leichter zu bedienen; die Dialoge (die Eingabeaufforderung des Zauberstabs, die Auswahl eines Buchs) passen auf den Bildschirm.

<a id="storage"></a>

## Speicherung

Die hochgeladene Datei wird als [Ressource](../resources.md) von dir gespeichert (ein Chat von SillyTavern kann Megabytes groß sein, mit allem, was SillyTavern zu den Nachrichten hinzufügt) und mit dem Buch verknüpft. Die Abschnitte eines Buchs werden genauso gespeichert, als kleine JSON-Datei (`<book name>.redraft.json`) mit den Anweisungen und dem Text; das Buch behält nur die Markierungen. Die Markierungen, die Turn-Details, das Modell und der Prompt werden in den Attributen des Buchs gespeichert, sodass sie in den [Buchsicherungen](../books/backups.md) und Kopien enthalten sind.

!!!warning
Die Datei des Chats ist nicht in Buchsicherungen enthalten, und eine Kopie eines Buchs verweist auf dieselbe Datei. Nach einer Wiederherstellung sind die Markierungen und Turn-Details vorhanden, aber der Konverter fragt erneut nach dem Chat (lade dieselbe Datei hoch; dies ersetzt die Turn-Details und die Markierungen). Dasselbe gilt für die Abschnitte eines Buchs: Lade das Buch oder die Sicherung erneut und markiere erneut. Das Entfernen der Quelle mit dem Kreuz löscht die Ressource.
!!!
