---
label: Bücher verwalten
order: 100
---

# Bücher verwalten
<a id="the-book-list"></a>

## Die Buchliste

Der Reiter **Bücher** listet deine Bücher auf. Bücher anderer Benutzer werden nicht angezeigt. Die Liste zeigt jeweils einen [Ordner](folders.md) an, die Ordner des aktuellen Ordners oberhalb seiner Bücher; auf oberster Ebene, wenn noch keine Ordner angelegt sind, sind es einfach alle deine Bücher.

![Der Reiter Bücher](../../images/books.png)

| Spalte | |
|---|---|
| **Name** | Der Name des Buchs. |
| **Tags** | Die Tags des Buchs. |
| **Erstellt** | Wann das Buch erstellt wurde. |
| **Zuletzt geändert** | Wann das Buch zuletzt geändert wurde. |

Die Liste ist nach *Zuletzt geändert* sortiert, das neueste zuerst. Klicke auf eine Spaltenüberschrift, um danach zu sortieren; bei mehreren sortierten Spalten wird die Liste nach allen von ihnen der Reihe nach sortiert.

Die Zeile unter den Überschriften filtert die Liste:

- **Name** - Bücher, deren Name mit dem Text *beginnt*. Verwende `*` als Platzhalter: `*dragon` findet jedes Buch mit „dragon“ irgendwo im Namen.
- **Tags** - Bücher, die *alle* ausgewählten Tags haben.

Die Aktualisieren-Schaltfläche am Ende der Filterzeile lädt die Liste neu.

Jede Zeile hat Schaltflächen: der Stern macht das Buch zu einem [Favoriten](folders.md#favorites), der Stift öffnet es, **In Ordner verschieben** legt es in einen [Ordner](folders.md), die Klonen-Schaltfläche erstellt eine Kopie davon und die Papierkorb-Schaltfläche löscht es. Die Kopfzeile hat **Neuer Ordner**, das Menü **Verwalten** (**Organisieren**, das Fenster zum [Verschieben und Kopieren](folders.md#organise) von Büchern und Ordnern, **Bücher taggen**, **Statistik** und das Untermenü **Massenänderungen**) und **Importieren**, das aus Dateien, die du hast, ein neues Buch erstellt (siehe [Ein Buch importieren](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Ein Buch ist in einem Fenster geöffnet

Ein Buch kann jeweils nur in einem Fenster geöffnet sein, auch über deine Browser-Tabs, andere Browser und dein Telefon hinweg. Wenn du ein Buch öffnest, das irgendwo anders geöffnet ist, sagt Marginalia, seit wann, und fragt **Hier öffnen**: Das Buch öffnet sich dann hier, und sein Fenster an der anderen Stelle schließt sich mit einer Erklärung dafür (wird dort gerade ein Teil generiert, sagt die Frage das, und **Hier öffnen** beendet die Generierung; was bis dahin geschrieben wurde, bleibt erhalten). **Abbrechen** lässt es, wo es ist. Wenn du dich anmeldest und Marginalia das Buch öffnet, das du zuletzt geöffnet hattest, entzieht es dieses nicht einem anderen Fenster: Es lässt es geschlossen und sagt dir, wo es ist. Ein Fenster in einem Tab, der geschlossen wurde, oder einem Gerät, das ausgeschaltet wurde, hält das Buch nicht fest: **Hier öffnen** übernimmt es sofort.

<a id="advanced-search"></a>

### Erweiterte Suche

**Suchen** (in der Kopfzeile, neben **Verwalten**; die Lupe in der Kopfzeile auf einem Telefon) öffnet ein Fenster, das Bücher nach mehr findet, als die Filterzeile kann. Fülle aus, was du prüfen möchtest, lasse den Rest leer und drücke **Suchen** (oder Enter in einem Textfeld):

| Feld | Findet Bücher... |
| ----- | -------------- |
| **Name** | deren Name den Text enthält (`*` ist ein Platzhalter). |
| **Einzuschließende Tags** | die die Tags haben: *Alle Tags* oder *Beliebiger Tag*, wie du neben dem Feld wählst. |
| **Auszuschließende Tags** | die diese Tags nicht haben: Bei *Beliebiger Tag* wird ein Buch mit mindestens einem davon ausgeschlossen, bei *Alle Tags* nur ein Buch, das jeden davon hat. |
| **Erstellt von / bis**, **Geändert von / bis** | erstellt oder zuletzt geändert im Zeitraum; der Tag eines *bis*-Datums ist eingeschlossen. |
| **Anzahl Teile von / bis** | mit so vielen Teilen in allen seinen Zweigen (Teile im Papierkorb werden nicht gezählt). |
| **Text** | mit einem Teil, der jedes Wort des Textes hat. Wörter, `"phrases"` und `*` funktionieren wie in [der Suche der Geschichte](story-editor.md#searching-the-story). |
| **Versionen einbeziehen** | aus (der Standard): nur der aktuelle Text der Teile wird durchsucht; an: ein Buch wird auch gefunden, wenn eine frühere Version eines seiner Teile die Wörter hat, siehe [Versionsverlauf](version-history.md#searching-the-old-text). |
| **In allen Zweigen suchen** | aus (der Standard): der Text wird nur im Zweig durchsucht, in dem das Buch steht (die Geschichte, wie du sie beim Öffnen des Buchs siehst); an: in den Teilen aller Zweige. |

Ein Buch muss jede Prüfung bestehen, die du ausgefüllt hast. Die gefundenen Bücher werden unten mit ihren Tags aufgelistet, zuletzt geändert zuerst; ein Klick auf eine Spaltenüberschrift sortiert sie. Die Ordner-Schaltfläche öffnet ein Buch (in seinem Fenster oberhalb der Suche, sodass du mit dem Schließen des Buchs zu den Ergebnissen zurückkehrst). **Leeren** leert die Felder.

![Die erweiterte Suche der Bücher](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Ein Buch klonen

Die Klonen-Schaltfläche fragt nach dem **Name** des neuen Buchs (standardmäßig der Name des Originals mit *(Kopie)*) und hat das Kontrollkästchen **Nur aktueller Zweig**, das standardmäßig angekreuzt ist. Das neue Buch ist ein eigenständiges Buch, das erhält:

![Ein Buch klonen](../../images/book-clone.png)

- die Einstellungen des Buchs (Beschreibung, Sprache, Prompts, Erzählperspektive, Zeitform, Stil, Sicherungseinstellungen), die Daten, die Erweiterungen im Buch speichern, und die Tags;
- dasselbe **Modell**, **Protokoll** und dasselbe Lorebook. Das Lorebook wird *verknüpft*, nicht kopiert: Eine Änderung daran zeigt sich in beiden Büchern;
- die Geschichte mit einer eigenen Kopie der Teile, ihrer Zusammenfassungen und Bilder. Mit **Nur aktueller Zweig** werden nur die Teile vom Anfang bis zum Ende des [aktiven Zweigs](branches-and-story-tree.md) kopiert; ohne diese Option werden alle Zweige kopiert.

Der Klon beginnt mit einem eigenen [Versionsverlauf](version-history.md#backups-copies-and-the-trash) (eine Version pro Teil); der Verlauf des Originals wird nicht kopiert.

Der Klon wird nicht veröffentlicht, auch wenn das Original veröffentlicht ist. Das Original wird nicht verändert. Er wird in denselben [Ordner](folders.md) gelegt wie das Original.

<a id="tagging-many-books-at-once"></a>

### Viele Bücher auf einmal taggen

**Bücher taggen** (im Menü **Verwalten** der Kopfzeile) öffnet ein Fenster, das einen Tag zu vielen Büchern hinzufügt oder von ihnen entfernt, ohne jedes einzeln zu öffnen. Wähle den **Tag** (oder tippe einen neuen ein und drücke Enter); die Liste zeigt jedes Buch mit seinen Tags. Die Bücher, die den gewählten Tag haben, stehen zuerst und zeigen ihn hervorgehoben. Jede Zeile hat eine Schaltfläche **+**, die den Tag auf dieses Buch anwendet, und eine Schaltfläche **-**, die ihn entfernt. Um dasselbe für mehrere Bücher zu tun, hake deren Kontrollkästchen an (das Kästchen in der Kopfzeile hakt alle an) und verwende unten **Den Tag auf die Markierten anwenden** oder **Den Tag von den Markierten entfernen**. Ein Buch, an dem ein Agent arbeitet, ist ausgegraut: Es ist gesperrt, sodass es nicht angehakt oder geändert werden kann, bis der Lauf angehalten oder gestoppt wird. Dasselbe Fenster taggt [Lorebooks und ihre Einträge](../lorebooks/lorebooks.md#tagging-many-at-once).

![Bücher taggen](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Modell und Protokoll vieler Bücher ändern

Das Untermenü **Massenänderungen** des Menüs **Verwalten** hat den Eintrag **Modell/Protokoll für Massenänderungen**. Er öffnet ein Fenster, das ein **Modell** und/oder ein **Protokoll** auf viele Bücher setzt, ohne jedes einzeln zu öffnen. Lasse ein Feld leer (dort steht *Nicht ersetzen*), dann bleibt dieser Teil der Bücher unverändert: Wähle nur ein Modell, um die Modelle zu ändern und die Protokolle zu behalten. Die Tabelle listet die Bücher des aktuellen [Ordners](folders.md) mit Namen, dem jetzigen Modell und Protokoll auf; **Unterordner einbeziehen** fügt die Bücher darunter hinzu. Tippe in den Filter unter **Name**, um die Liste einzugrenzen, hake die zu ändernden Bücher an (das Kästchen in der Kopfzeile hakt alle angezeigten Bücher an) und drücke **Ersetzen und beenden**. Bücher, die der Filter ausblendet, werden abgehakt, sodass nur geändert wird, was du siehst. Ein Buch, an dem ein Agent arbeitet, ist ausgegraut: Es ist gesperrt, sodass es nicht angehakt oder geändert werden kann, bis der Lauf angehalten oder gestoppt wird.

![Modell und Protokoll vieler Bücher ändern](../../images/mass-change-model-protocol.png)

Der Eintrag **Sprache für Massenänderungen** desselben Menüs funktioniert genauso für die **Sprache** der Bücher: Wähle die Sprache, filtere und hake die Bücher an, drücke **Ersetzen und beenden**. Die Tabelle zeigt die Sprache, die jedes Buch jetzt hat, mit dem, was es von seinen Ordnern erbt (siehe [Sprache](folders.md#language)).

![Sprache vieler Bücher ändern](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Viele Bücher umbenennen

Der Eintrag **Massenumbenennung** benennt die markierten Bücher auf einmal um. Wähle die Art:

| Art | Was sie tut |
|---|---|
| **Präfix hinzufügen** | Setzt den **Hinzuzufügenden Text** vor den Namen. |
| **Postfix hinzufügen** | Setzt den **Hinzuzufügenden Text** hinter den Namen. |
| **Suchen und ersetzen** | Ersetzt jedes Vorkommen von **Suchen** im Namen durch **Ersetzen durch** (leer entfernt es). Der Text wird wörtlich genommen, nicht als Muster; **Groß-/Kleinschreibung beachten** ist standardmäßig aus. |
| **Bücher nummerieren** | Gibt den Büchern Nummern: Das **Namensmuster** ist der neue Name, mit `{name}` für den aktuellen Namen und `{n}` für die Nummer, zum Beispiel `{name} {n}`, `{n}. {name}` oder `Chapter {n}`. Das erste markierte Buch erhält **Beginnen bei**, jedes nächste die **Schrittweite** mehr, und **Stellen** füllt die Nummer mit Nullen auf (`007`). |

![Die Bücher aller Ordner nummerieren](../../images/mass-rename.png)

Die Spalte **Neuer Name** zeigt, was jedes markierte Buch beim Tippen bekäme, also prüfe es, bevor du **Umbenennen** drückst. Eine Nummerierung zählt die markierten Bücher in der Reihenfolge der Liste (nach Name). Leerzeichen am Anfang und am Ende des neuen Namens werden entfernt. Nichts wird umbenannt, und das Fenster bleibt offen, wenn ein Text fehlt, wenn das Muster kein `{n}` enthält, wenn sich kein Name ändern würde oder wenn ein Name leer würde. Filtern, **Unterordner einbeziehen** und gesperrte Bücher funktionieren wie bei den anderen Massenänderungen.

**Bücher taggen** und der Eintrag **Ordner taggen** arbeiten auf die Bücher bzw. die Ordner des aktuellen Ordners auf die gleiche Weise.
<a id="creating-a-book"></a>

## Ein Buch anlegen

1. Klicken Sie auf **Buch hinzufügen**.
2. Geben Sie den Namen ein und bestätigen Sie.

Das neue Buch wird in den [Ordner](folders.md) gelegt, den die Liste zeigt, ebenso die Bücher, die Sie importieren. Es öffnet sich sofort. Es beginnt mit Ihrem **Standardmodell** und **Standardprotokoll** aus dem Reiter *Einstellungen*
(siehe [Konto & Einstellungen](../account.md)), sofern Sie sie festgelegt haben; andernfalls wählen Sie sie auf dem Reiter *Info* aus, bevor Sie etwas generieren.

<a id="importing-a-book"></a>

### Ein Buch importieren

**Importieren** in der Kopfzeile der Liste (auf dem Telefon die Dateischaltfläche) öffnet ein Fenster für alles, woraus ein neues Buch bestehen kann.
Wählen Sie die **Quelle**; der Text, die akzeptierten Dateien und die Optionen unter dem Namen richten sich danach:

| Quelle | Upload | Optionen |
|---|---|---|
| **Aus Archiv importieren** | ein `.zip` aus `.md`-Dateien, ein Abschnitt für jede Datei, siehe [Import in das Buch](story-editor.md#importing-into-the-book) | keine |
| **Aus SillyTavern importieren** | ein oder mehrere `.jsonl`-Chats, siehe [unten](#importing-from-sillytavern) | Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen |
| **Aus LM Studio importieren** | eine oder mehrere Unterhaltungen, siehe [unten](#importing-from-lm-studio) | Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen |
| **Aus einem Dokument importieren** | ein Manuskript in einer Datei (Word, LibreOffice, RTF, Markdown, Klartext, EPUB), siehe [unten](#importing-a-finished-manuscript) | Wörter pro Abschnitt, Überschriftenebene der Kapitel |
| **Aus einem Obsidian-Vault importieren** | der `.zip` eines Vaults, siehe [unten](#from-an-obsidian-vault-or-a-scrivener-project) | dasselbe |
| **Aus einem Scrivener-Projekt importieren** | der `.zip` eines `.scriv`-Projekts, siehe [unten](#from-an-obsidian-vault-or-a-scrivener-project) | dasselbe |

Laden Sie die Datei hoch, geben Sie dem Buch einen Namen (er wird aus der Datei, dem Titel eines Dokuments oder der Figur eines Chats übernommen; das Buch heißt *Neues Buch*, wenn Sie das Feld leer lassen) und klicken Sie auf **Importieren**. Ein Fortschrittsfenster mit **Stopp** zeigt die Abschnitte, die gespeichert werden; durch Stoppen bleibt erhalten, was bereits gespeichert wurde. Eine Datei, die die Quelle nicht lesen kann, wird mit einer Warnung abgelehnt, die übrigen Uploads bleiben erhalten.

Dasselbe Fenster fügt über das Einstellungsmenü (⚙) eines Buchs die Abschnitte diesem Buch an einer Stelle Ihrer Wahl hinzu, siehe [Import in das Buch](story-editor.md#importing-into-the-book).

![Die Quellen des Importfensters](../../images/import-source.png)

![Import eines Zip-Archivs mit Textdateien als neues Buch](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Ein fertiges Manuskript importieren

Für einen bereits vorhandenen Entwurf wählen Sie **Aus einem Dokument importieren** und laden die Datei hoch: ein `.docx` (Word), `.odt` (LibreOffice,
OpenOffice), `.rtf` (Rich Text, aus WordPad, TextEdit und anderen Programmen), `.md` (eine Markdown-Datei), `.txt` (Klartext) oder `.epub` (ein E-Book). Der Text wird zu den Abschnitten eines neuen Buches, und alles davon ist als von Ihnen geschrieben markiert (der Import zählt nicht als Schreiben in der [Statistik](statistics.md), und in der Historie eines Abschnitts steht *Import aus einem Dokument*).

- **Kapitel.** Ein Absatz im Stil *Überschrift* (in einer Markdown-Datei eine `#`-Überschrift) beginnt ein Kapitel, und jedes Kapitel beginnt mit seiner Überschrift (die erste
  Überschrift eines Abschnitts ist sein Kapitelttitel, wie beim [Export](export.md)). **Kapitel beginnen bei Überschriftenebene** legt fest, welche
  Überschriften das sind: die gewählte Ebene und die darüberliegenden (zu Beginn wird die flachste Ebene gewählt, die das Dokument hat); tiefere
  Überschriften bleiben als kleinere Überschriften im Text. Ein Dokument ohne Überschriften ist ein einziges Kapitel. Der *Titel* des Dokuments wird nicht als Text importiert, er benennt das Buch (in einer Markdown-Datei die `title:` einer Kopfzeile oben, oder eine einzelne `#`-Überschrift vor `##`-Kapiteln). Eine Klartextdatei hat keine Überschriften, daher beginnt eine eigene Zeile, die `Chapter 12`, `Part IV: The Return`,
  `Book two` oder `Prologue` (auch `Epilogue`, `Interlude`, `Preface`, `Foreword`, `Afterword`) sagt, ein Kapitel; die Wahl der
  Überschriftenebene entfällt dann.
- **Wörter pro Abschnitt.** Der Text wird zwischen Absätzen in Abschnitte von etwa dieser Wortzahl geschnitten (zu Beginn 1000), an der Absatzgrenze, die der Zahl am nächsten liegt. Ein Abschnitt enthält nie das Ende eines Kapitels und den Anfang des nächsten, und ein Kapitel wird nicht grundlos geteilt: Was am Ende eines Kapitels übrig bliebe, wenn es weniger als die Hälfte der Zahl ist, bleibt beim vorherigen Abschnitt, sodass ein Kapitel von bis zu eineinhalbfacher Zahl ein Abschnitt ist. Ein Absatz, der länger als die Zahl ist, wird nicht geschnitten.
- Eine Zeile unter dem Namen gibt an, wie viele Kapitel, Wörter und Abschnitte der Import ergibt; sie ändert sich, wenn Sie die Optionen ändern.
- **Absätze einer Textdatei.** Leerzeilen trennen die Absätze, und die Zeilen eines Absatzes werden zusammengefügt (auf eine Spalte umgebrochener Text wird zu einem Absatz); eine Datei ohne Leerzeilen hat einen Absatz pro Zeile. Eine Markdown-Datei bleibt so erhalten, wie sie geschrieben ist, geschnitten an Leerzeilen, Überschriften und Codeblöcken; Text in Windows-1252 statt UTF-8 wird ebenfalls gelesen.
- **Formatierung.** Kursiv und fett bleiben erhalten (als Markdown `*italic*`, `**bold**`); die übrige Formatierung (Schriften, Größen, Ausrichtung), Bilder, Kopf- und Fußzeilen, Fußnoten, Kommentare und gelöschter Text nachverfolgter Änderungen werden nicht importiert. Tabellen werden Zelle für Zelle gelesen.

![Import eines Word-Dokuments, geschnitten in Abschnitte von etwa 500 Wörtern](../../images/document-import.png)

Eine alte `.doc`-Datei muss zuerst als `.docx`, `.odt` oder `.rtf` gespeichert werden.

**Ein EPUB** wird Seite für Seite in seiner Lesereihenfolge gelesen. Die Überschriften der Seiten (`h1` bis `h6`) sind die Kapitel; eine Seite ohne
Überschrift, die das Inhaltsverzeichnis des E-Books nennt, erhält diesen Namen als Überschrift. Umschlag, Inhaltsverzeichnis, Fußnoten und die Lesungen von Ruby-Text bleiben weg, und der Titel des E-Books benennt das Buch. Ein Buch mit DRM-Schutz kann nicht gelesen werden.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Aus einem Obsidian-Vault oder einem Scrivener-Projekt

Beides sind Ordner; packen Sie den Ordner daher zuerst in ein Zip-Archiv (auf dem Mac **Komprimieren** im Finder; der `.scriv` von Scrivener ist ebenfalls ein Ordner) und laden Sie das `.zip` hoch. Die Optionen sind dieselben wie bei einem Dokument.

- **Obsidian-Vault.** Jede Notiz (`.md`) ist ein Kapitel, benannt nach der Notiz (oder dem `title:` ihrer Kopfzeile), in der Reihenfolge der Pfade, wobei Zahlen als Zahlen gezählt werden (`Chapter 2` vor `Chapter 10`). Ein Ordner ist ein Abschnitt über seinen Notizen, und die Überschriften innerhalb einer Notiz liegen unterhalb der eigenen Ebene der Notiz. Links `[[Note|text]]` werden zu ihrem Text; Einbettungen (`[[file]]` mit einem `!` davor), Kommentare `%%...%%`, Block-IDs und die Markierung eines Hinweises werden entfernt; eine Hervorhebung `==text==` ist einfacher Text. Versteckte Ordner (`.obsidian`, `.trash`), Anhänge und Notizen ohne Text werden übersprungen. Enthält der Vault Notizen, die nicht Teil der Geschichte sind, löschen Sie sie, bevor Sie das Archiv erstellen.
- **Scrivener-Projekt.** Importiert wird das Manuskript (der Ordner *Draft* der Binder-Ansicht, Scrivener 2 und 3), nicht die Recherche, der Papierkorb oder die Dokumente, die nicht *In Kompilierung enthalten* sind. Ein Ordner oder ein Dokument mit Dokumenten darunter ist eine Überschrift (ihre Tiefe im Binder ist ihre Ebene, ihr Titel der Text); die Dokumente, die ein Ordner nur enthält, sind seine Szenen, nacheinander mit einem Szenenwechsel dazwischen (ihre Titel werden nicht verwendet). Ein Dokument direkt im Manuskript oder in einem Ordner mit anderen Ordnern ist ein Kapitel mit seinem Titel. Der Name des Projekts benennt das Buch.

Unabhängig von der Quelle wird das neue Buch nach Abschluss des Imports geöffnet (auch wenn Sie abgebrochen haben), und das Fenster [**Import abgeschlossen**](story-editor.md#after-an-import) bietet an, die Zusammenfassungen der Abschnitte zu schreiben und einen Agenten die Lore-Notizen anlegen zu lassen.

<a id="importing-from-sillytavern"></a>

### Aus SillyTavern importieren

Um ein Rollenspiel oder eine Geschichte fortzusetzen, die Sie in [SillyTavern](https://github.com/SillyTavern/SillyTavern) begonnen haben, verwenden Sie
**Importieren → Aus SillyTavern importieren**. Exportieren Sie in SillyTavern den Chat (*Chat-Dateien verwalten*, oder nehmen Sie die `.jsonl`-Dateien aus `data/<user>/chats/<character>/`), dann:

1. Laden Sie **einen oder mehrere** `.jsonl`-Chats hoch. Eine Datei, die kein SillyTavern-Chat ist, wird abgelehnt, die übrigen bleiben erhalten.
2. Geben Sie dem Buch einen Namen (er wird aus der Figur des ersten Chats übernommen).
3. Setzen Sie den Haken bei **Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen**, wenn Sie einen Abschnitt pro Runde möchten: Die Nachricht des Benutzers (mehrere Nachrichten hintereinander werden mit einer Leerzeile verbunden) wird zu den *Anweisungen* des Abschnitts, und die Antwort danach wird sein Text. Ohne Haken ist jede Nachricht ein eigener Abschnitt, mit ihrem Text als Text des Abschnitts, sodass das Buch so liest, wie der Chat gelesen wurde.
4. Klicken Sie auf **Importieren**. Ein Fortschrittsfenster mit **Stopp** zeigt die Abschnitte, die gespeichert werden; durch Stoppen bleibt erhalten, was bereits gespeichert wurde.

![Import eines SillyTavern-Chats](../../images/sillytavern-import.png)

SillyTavern kopiert den Chat, wenn Sie einen *Zweig* oder einen *Kontrollpunkt* anlegen, sodass die Chats einer Geschichte ihren Anfang teilen. Laden Sie sie zusammen hoch, und Marginalia findet die gemeinsamen Nachrichten und speichert sie einmal: Die Chats werden zu den Zweigen eines Baums, den Sie in der [Baumansicht](story-editor.md) durchgehen können. Die *Swipes* (alternative Antworten) einer Nachricht werden zu Geschwisterabschnitten, und das Buch öffnet sich am Ende des zuletzt geschriebenen Chats (nach dem Datum seiner letzten Nachricht). Chats, die nicht mit derselben Nachricht beginnen, werden zu getrennten ersten Abschnitten. Versteckte (System-)Nachrichten werden übersprungen, ebenso leere Swipes. Die Begründung und der Modellname einer Antwort bleiben beim Abschnitt erhalten.

<a id="importing-from-lm-studio"></a>

### Aus LM Studio importieren

Um eine Unterhaltung fortzusetzen, die Sie in [LM Studio](https://lmstudio.ai) geführt haben, verwenden Sie **Importieren → Aus LM Studio importieren**.
LM Studio speichert jede Unterhaltung in einer Datei `<number>.conversation.json` im Ordner `.lmstudio/conversations` Ihres Benutzerordners (unter Windows
`%USERPROFILE%\.lmstudio\conversations`). Es funktioniert wie [Aus SillyTavern importieren](#importing-from-sillytavern):

1. Laden Sie **eine oder mehrere** Unterhaltungen hoch. Eine Datei, die keine Unterhaltung von LM Studio ist, wird abgelehnt, die übrigen bleiben erhalten.
2. Geben Sie dem Buch einen Namen (er wird mit dem Namen der Unterhaltung gefüllt oder, wenn sie keinen hat, mit dem Dateinamen).
3. Setzen Sie den Haken bei **Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen** für einen Abschnitt pro Runde (Ihre Nachricht sind die *Anweisungen* des Abschnitts), oder lassen Sie ihn weg, sodass jede Nachricht ein eigener Abschnitt ist.
4. Klicken Sie auf **Importieren**.

![Das Fenster des Imports aus LM Studio mit einer hochgeladenen Unterhaltung](../../images/lmstudio-import.png)

Die Versionen einer Nachricht, die LM Studio aufbewahrt (die Antworten, die Sie neu erzeugt haben, der Text, den Sie bearbeitet haben), werden zu Geschwisterabschnitten, und das Buch öffnet sich bei der Version, die angezeigt wurde; Unterhaltungen, die ihren Anfang teilen, werden zu einem Baum verbunden. Das Denken eines Reasoning-Modells bleibt als Begründung des Abschnitts erhalten, zusammen mit dem Modellnamen. Der Systemprompt der Unterhaltung, die Tool-Aufrufe und angehängte Dateien werden nicht importiert.

Um ein Buch aus einer Sicherungsdatei zu beginnen, verwenden Sie **Sicherung als neues Buch importieren** im Reiter *Einstellungen*, siehe [Buchsicherungen](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## Der Reiter „Über“

![Der Reiter „Über“ eines Buches](../../images/book-about.png)

| Feld | |
|---|---|
| **Name** | Der Name des Buches. |
| **Tags** | Tags zum Sortieren deiner Bücher und zur Aktivierung des [Lorebooks](../lorebooks/entries.md). Tippe, um einen vorhandenen Tag auszuwählen oder einen neuen anzulegen. |
| **Beschreibung** | Worum es in dem Buch geht. Den Prompt-Vorlagen als Buchbeschreibung zur Verfügung gestellt. |
| **Sprache** | Die Sprache, in der das Buch geschrieben ist. Wird für die automatische Silbentrennung des Textes im Buchlesestil und im [Viewer](publishing.md) verwendet; sie ändert nicht, was das Modell schreibt. Leer bedeutet *erben*: die Sprache des nächstgelegenen [Ordners](folders.md#language) über dem Buch, der eine festlegt, sonst Englisch; das Feld zeigt dann an, welche Sprache es ist und woher sie stammt. |
| **Modell** | Der [Inferenzanbieter](../inference-providers.md), der dieses Buch schreibt. |
| **Protokolle** | Das [Protokoll](../protocols.md) mit den Generierungseinstellungen. |
| **Aufbewahrte Versionen je Abschnitt** | Wie viele frühere Versionen des Textes eines Abschnitts der [Verlauf](version-history.md) behält. Leer bedeutet *erben*: die graue Zahl im Feld ist die Grenze, die gilt, und die Zeile darunter sagt, welcher Ordner, deine Einstellungen oder der Administrator sie festlegt. **0** behält keinen Verlauf. |
| **Veröffentlicht** | Erlaubt anderen Benutzern, das Buch zu lesen, siehe [Veröffentlichen & Viewer](publishing.md). Sobald das Kästchen aktiviert ist, wird der Leselink des Buches (`<address of Marginalia>/view/<book ID>`) unter dem Kästchen angezeigt; kopiere ihn und schicke ihn an die Leser. |
| **Die Bilder den Lesern zeigen** | Standardmäßig aktiviert. Leser eines veröffentlichten Buches sehen die Bilder seiner Abschnitte; deaktiviere das Kästchen, um ihnen nur den Text zu zeigen. Deine eigenen Bilder siehst du immer. |

Änderungen werden beim Tippen gespeichert.

Darunter stehen schreibgeschützte Zählwerte:

| | |
|---|---|
| **Wörter insgesamt**, **Tokens insgesamt** | Alle Abschnitte des Buches, in allen Zweigen. |
| **Wörter des aktiven Zweigs**, **Tokens des aktiven Zweigs** | Nur die Abschnitte des [aktiven Zweigs](branches-and-story-tree.md). |
| **Urheberschaft des Zweigs** | Drücke **Berechnen**: wie viel Prozent der Wörter des aktiven Zweigs du geschrieben hast und wie viele ein Modell generiert hat, sowie die Anzahl der Wörter. Sie wird aus dem [Versionsverlauf](version-history.md) jedes Abschnitts ermittelt, daher dauert es bei einem langen Buch eine Weile; ein Abschnitt, dessen Verlauf ausgeschaltet ist, wird mit dem Text verglichen, den das Modell zuerst generiert hat. Siehe [Hervorheben generierten Textes](story-editor.md#highlighting-generated-text). |

Ein Buch braucht ein Modell und ein Protokoll, um zu generieren. Fehlen sie, bricht die Generierung mit *Dem Buch fehlt ein Modell.* oder *Dem Buch fehlt ein Protokoll.* ab.

<a id="deleting-a-book"></a>

## Ein Buch löschen

Klicke in der Buchliste auf die Papierkorb-Schaltfläche und bestätige. Das Buch verschwindet aus deiner Liste.

Ein Buch wird nicht gelöscht, solange etwas daran arbeitet: ein Abschnitt, der generiert wird, ein Zusammenfassungsstapel, eine Übersetzung, ein Neuentwurf- oder Anmerkungsfenster, oder das Buch ist in einem anderen Fenster geöffnet. Marginalia sagt dann, was im Weg ist (*Dies ist in Verwendung: ...*); warte, bis es fertig ist, oder stoppe es, und lösche erneut. Dasselbe gilt für das Wiederherstellen eines [Backups](backups.md) in das Buch, während ein anderes Fenster oder ein Lauf daran arbeitet.

Gelöschte Bücher werden nicht sofort aus der Datenbank entfernt. Bis ein Administrator sie zusammen mit all ihren Abschnitten über [Bereinigung](../administration/cleanup.md) entfernt, kannst du das Buch über den Reiter [Papierkorb](../trash.md) wiederherstellen. Bis dahin sind die Daten noch in den [Datenbank-Backups](../administration/database-backups.md) enthalten. Die eigenen [Backups](backups.md) des Buches sind Dateien und bleiben, wo sie sind.
