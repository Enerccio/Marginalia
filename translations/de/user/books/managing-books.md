---
label: Bücher verwalten
order: 100
---

# Bücher verwalten
<a id="the-book-list"></a>

## Die Bücherliste

Der Reiter **Bücher** listet deine Bücher auf. Bücher anderer Benutzer werden nicht angezeigt. Die Liste zeigt jeweils einen [Ordner](folders.md) an, die Ordner des aktuellen Ordners stehen oberhalb ihrer Bücher; auf der obersten Ebene, wenn noch keine Ordner angelegt sind, sind es einfach alle deine Bücher.

![Der Reiter Bücher](../../images/books.png)

| Spalte | |
|---|---|
| **Name** | Der Name des Buches. |
| **Tags** | Die Tags des Buches. |
| **Erstellt** | Wann das Buch erstellt wurde. |
| **Zuletzt geändert** | Wann das Buch zuletzt geändert wurde. |

Die Liste ist nach *Zuletzt geändert* sortiert, neueste zuerst. Klicke auf eine Spaltenüberschrift, um danach zu sortieren; bei mehreren sortierten Spalten wird die Liste nach allen von ihnen in der angegebenen Reihenfolge sortiert.

Die Zeile unter den Überschriften filtert die Liste:

- **Name** - Bücher, deren Name *mit* dem Text beginnt. Verwende `*` als Platzhalter: `*dragon` findet jedes Buch mit „dragon“ irgendwo im Namen.
- **Tags** - Bücher, die *alle* ausgewählten Tags haben.

Die Aktualisieren-Schaltfläche am Ende der Filterzeile lädt die Liste neu.

Jede Zeile hat Schaltflächen: der Stern macht das Buch zu einem [Favoriten](folders.md#favorites), der Stift öffnet es, **In Ordner verschieben** legt es in einen [Ordner](folders.md), die Kopierschaltfläche erstellt eine Kopie davon und die Papierkorb-Schaltfläche löscht es. Die Kopfzeile enthält **Neuer Ordner**, das Menü **Verwalten** (**Organisieren**, das Fenster zum [Verschieben und Kopieren](folders.md#organise) von Büchern und Ordnern, **Bücher taggen**, **Statistik** und das Untermenü **Massenänderungen**) sowie **Importieren**, das aus vorhandenen Dateien ein neues Buch erstellt (siehe [Ein Buch importieren](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Ein Buch ist in einem Fenster geöffnet

Ein Buch kann jeweils in einem Fenster geöffnet sein, auch über deine Browser-Tabs, andere Browser und dein Telefon hinweg. Wenn du ein Buch öffnest, das anderswo bereits geöffnet ist, zeigt Marginalia an, seit wann, und fragt **Hier öffnen**: Das Buch öffnet sich dann hier, und das Fenster an der anderen Stelle wird geschlossen, mit einem Hinweis auf den Grund (wird dort gerade ein Teil generiert, fragt die Meldung nach, und **Hier öffnen** beendet die Generierung; was bis dahin geschrieben wurde, bleibt erhalten). **Abbrechen** lässt es, wo es ist. Wenn du dich anmeldest und Marginalia das Buch öffnet, das du zuletzt geöffnet hattest, nimmt es dir das Buch nicht aus einem anderen Fenster weg: Es bleibt geschlossen, und du erhältst einen Hinweis, wo es geöffnet ist. Ein Fenster in einem geschlossenen Tab oder einem Gerät, das ausgeschaltet wurde, hält das Buch nicht fest: **Hier öffnen** übernimmt es sofort.

<a id="advanced-search"></a>

### Erweiterte Suche

**Suchen** (in der Kopfzeile, neben **Verwalten**; auf dem Telefon die Lupe in der Kopfzeile) öffnet ein Fenster, das Bücher nach mehr Kriterien findet, als die Filterzeile erlaubt. Fülle aus, was du prüfen möchtest, lass den Rest leer und drücke **Suchen** (oder Enter in einem Textfeld):

| Feld | Findet Bücher... |
| ----- | -------------- |
| **Name** | deren Name den Text enthält (`*` ist ein Platzhalter). |
| **Einzuschließende Tags** | die die Tags haben: *Alle Tags* oder *Beliebiger Tag*, je nachdem, was du neben dem Feld wählst. |
| **Auszuschließende Tags** | die diese Tags nicht haben: Bei *Beliebiger Tag* fällt ein Buch mit mindestens einem davon heraus, bei *Alle Tags* nur ein Buch, das jeden davon hat. |
| **Erstellt von / bis**, **Geändert von / bis** | erstellt oder zuletzt geändert im Zeitraum; der Tag eines *bis*-Datums ist eingeschlossen. |
| **Anzahl der Teile von / bis** | mit so vielen Teilen in allen seinen Zweigen (Teile im Papierkorb werden nicht gezählt). |
| **Text** | mit einem Teil, der jedes Wort des Textes enthält. Wörter, `"Wendungen"` und `*` funktionieren wie in [der Suche der Geschichte](story-editor.md#searching-the-story). |
| **Versionen einbeziehen** | aus (Standard): Durchsucht nur den aktuellen Text der Teile; an: Ein Buch wird auch gefunden, wenn eine frühere Version eines seiner Teile die Wörter enthält, siehe [Versionsverlauf](version-history.md#searching-the-old-text). |
| **In allen Zweigen suchen** | aus (Standard): Der Text wird nur im Zweig durchsucht, in dem das Buch steht (die Geschichte, wie du sie beim Öffnen des Buches siehst); an: in den Teilen aller Zweige. |

Ein Buch muss jede Prüfung bestehen, die du ausgefüllt hast. Die gefundenen Bücher werden unten mit ihren Tags aufgelistet, zuletzt geänderte zuerst; ein Klick auf eine Spaltenüberschrift sortiert sie. Die Ordner-Schaltfläche öffnet ein Buch (in seinem Fenster über der Suche, sodass du nach dem Schließen des Buches zu den Ergebnissen zurückkehrst). **Leeren** leert die Felder.

![Die erweiterte Suche der Bücher](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Ein Buch kopieren

Die Kopierschaltfläche fragt nach dem **Name** des neuen Buches (standardmäßig der Name des Originals mit *(Kopie)*) und hat das Kontrollkästchen **Nur aktueller Zweig**, das standardmäßig aktiviert ist. Das neue Buch ist ein eigenständiges Buch und erhält:

![Ein Buch kopieren](../../images/book-clone.png)

- die Einstellungen des Buches (Beschreibung, Sprache, Prompts, Erzählperspektive, Zeitform, Stil, Backup-Einstellungen), die Daten, die Erweiterungen im Buch speichern, und die Tags;
- das gleiche **Modell**, **Protokoll** und dasselbe Lorebook. Das Lorebook wird *verknüpft*, nicht kopiert: Eine Änderung daran erscheint in beiden Büchern;
- die Geschichte mit eigener Kopie der Teile, ihrer Zusammenfassungen und Bilder. Mit **Nur aktueller Zweig** werden nur die Teile vom Anfang bis zum Ende des [aktiven Zweigs](branches-and-story-tree.md) kopiert; ohne diese Option werden alle Zweige kopiert.

Die Kopie beginnt mit einem eigenen [Versionsverlauf](version-history.md#backups-copies-and-the-trash) (eine Version pro Teil); der Verlauf des Originals wird nicht kopiert.

Die Kopie wird nicht veröffentlicht, auch wenn das Original veröffentlicht ist. Das Original wird nicht verändert. Sie wird in denselben [Ordner](folders.md) wie das Original gelegt.

<a id="tagging-many-books-at-once"></a>

### Viele Bücher auf einmal taggen

**Bücher taggen** (im Menü **Verwalten** der Kopfzeile) öffnet ein Fenster, das einen Tag zu vielen Büchern hinzufügt oder von ihnen entfernt, ohne jedes einzeln zu öffnen. Wähle den **Tag** (oder gib einen neuen ein und drücke Enter); die Liste zeigt jedes Buch mit seinen Tags. Bücher mit dem gewählten Tag stehen oben und zeigen ihn hervorgehoben. Jede Zeile hat eine Schaltfläche **+**, die den Tag diesem Buch hinzufügt, und eine Schaltfläche **-**, die ihn entfernt. Um dasselbe für mehrere Bücher zu tun, setze die Häkchen in ihren Kontrollkästchen (das Kästchen in der Kopfzeile markiert alle) und verwende unten **Den Tag auf die Markierten anwenden** oder **Den Tag von den Markierten entfernen**. Ein Buch, mit dem ein Agent arbeitet, ist ausgegraut: Es ist gesperrt, sodass es erst angehakt oder geändert werden kann, wenn der Lauf pausiert oder gestoppt wird. Dasselbe Fenster taggt [Lorebooks und ihre Einträge](../lorebooks/lorebooks.md#tagging-many-at-once).

![Bücher taggen](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Modell und Protokoll vieler Bücher ändern

Das Untermenü **Massenänderungen** des Menüs **Verwalten** enthält den Eintrag **Modell/Protokoll für Massenänderungen**. Er öffnet ein Fenster, das ein **Modell** und/oder ein **Protokoll** für viele Bücher setzt, ohne jedes einzeln zu öffnen. Lass ein Feld leer (es zeigt *Nicht ersetzen*), dann bleibt dieser Teil der Bücher unverändert: Wähle nur ein Modell, um die Modelle zu ändern, und behalte die Protokolle. Die Tabelle listet die Bücher des aktuellen [Ordners](folders.md) mit Namen sowie dem Modell und Protokoll, die sie jetzt haben; **Unterordner einbeziehen** nimmt die Bücher darunter hinzu. Gib im Filter unter **Name** etwas ein, um die Liste einzugrenzen, hake die zu ändernden Bücher an (das Kästchen in der Kopfzeile markiert alle angezeigten Bücher) und drücke **Ersetzen und beenden**. Bücher, die der Filter ausblendet, werden abgewählt, sodass nur geändert wird, was du siehst. Ein Buch, mit dem ein Agent arbeitet, ist ausgegraut: Es ist gesperrt, sodass es erst angehakt oder geändert werden kann, wenn der Lauf pausiert oder gestoppt wird.

![Modell und Protokoll vieler Bücher ändern](../../images/mass-change-model-protocol.png)

Der Eintrag **Sprache für Massenänderungen** desselben Menüs funktioniert genauso für die **Sprache** der Bücher: Wähle die Sprache, filtere und hake die Bücher an, drücke **Ersetzen und beenden**. Die Tabelle zeigt die Sprache, die jedes Buch jetzt hat, mit dem, was es von seinen Ordnern erbt (siehe [Sprache](folders.md#language)).

![Sprache vieler Bücher ändern](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Viele Bücher umbenennen

Der Eintrag **Massenumbenennung** benennt die angehakten Bücher auf einmal um. Wähle die Art:

| Art | Was sie tut |
|---|---|
| **Präfix hinzufügen** | Setzt den **Hinzuzufügenden Text** vor den Namen. |
| **Postfix hinzufügen** | Setzt den **Hinzuzufügenden Text** hinter den Namen. |
| **Suchen und ersetzen** | Ersetzt jedes Vorkommen von **Suchen** im Namen durch **Ersetzen durch** (leer entfernt es). Der Text wird wörtlich genommen, nicht als Muster; **Groß-/Kleinschreibung beachten** ist standardmäßig aus. |
| **Bücher nummerieren** | Gibt den Büchern Nummern: Das **Namensmuster** ist der neue Name, mit `{name}` für den aktuellen Namen und `{n}` für die Nummer, zum Beispiel `{name} {n}`, `{n}. {name}` oder `Kapitel {n}`. Das erste angehakte Buch erhält **Beginnen bei**, jedes weitere den **Schrittweite**-Wert mehr, und **Stellen** füllt die Nummer mit Nullen auf (`007`). |

![Bücher aller Ordner nummerieren](../../images/mass-rename.png)

Die Spalte **Neuer Name** zeigt, was jedes angehakte Buch beim Tippen erhalten würde; prüfe das, bevor du **Umbenennen** drückst. Eine Nummerierung zählt die angehakten Bücher in der Reihenfolge der Liste (nach Name). Leerzeichen am Anfang und Ende des neuen Namens werden entfernt. Es wird nichts umbenannt, und das Fenster bleibt offen, wenn ein Text fehlt, das Muster kein `{n}` enthält, sich kein Name ändern würde oder ein Name leer würde. Filtern, **Unterordner einbeziehen** und gesperrte Bücher funktionieren wie bei den anderen Massenänderungen.

**Bücher taggen** und der Eintrag **Ordner taggen** arbeiten ebenso auf den Büchern beziehungsweise Ordnern des aktuellen Ordners.
<a id="creating-a-book"></a>

## Ein Buch erstellen

1. Klicken Sie auf **Buch hinzufügen**.
2. Geben Sie den Namen ein und bestätigen Sie.

Das neue Buch wird in den [Ordner](folders.md) gelegt, den die Liste gerade zeigt, ebenso die Bücher, die Sie importieren. Es öffnet sich sofort. Es übernimmt Ihr **Standardmodell** und Ihr **Standardprotokoll** aus dem Reiter *Einstellungen*
(siehe [Konto & Einstellungen](../account.md)), sofern Sie sie festgelegt haben; andernfalls wählen Sie sie vor dem Generieren im Reiter *Info* aus.

<a id="importing-a-book"></a>

### Ein Buch importieren

**Importieren** in der Kopfzeile der Liste (auf dem Telefon die Dateischaltfläche) öffnet ein Fenster für alles, woraus ein neues Buch entstehen kann.
Wählen Sie die **Quelle**; der Text, die akzeptierten Dateien und die Optionen unter dem Namen richten sich danach:

| Quelle | Upload | Optionen |
|---|---|---|
| **Aus Archiv importieren** | eine `.zip` mit `.md`-Dateien, ein Abschnitt für jede Datei, siehe [Import in das Buch](story-editor.md#importing-into-the-book) | keine |
| **Aus SillyTavern importieren** | eine oder mehrere `.jsonl`-Chats, siehe [unten](#importing-from-sillytavern) | Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen |
| **Aus LM Studio importieren** | eine oder mehrere Unterhaltungen, siehe [unten](#importing-from-lm-studio) | Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen |
| **Aus einem Dokument importieren** | ein Manuskript in einer Datei (Word, LibreOffice, RTF, Markdown, Klartext, EPUB), siehe [unten](#importing-a-finished-manuscript) | Wörter pro Abschnitt, Überschriftenebene der Kapitel |
| **Aus einem Obsidian-Vault importieren** | die `.zip` eines Vaults, siehe [unten](#from-an-obsidian-vault-or-a-scrivener-project) | dasselbe |
| **Aus einem Scrivener-Projekt importieren** | die `.zip` eines `.scriv`-Projekts, siehe [unten](#from-an-obsidian-vault-or-a-scrivener-project) | dasselbe |

Laden Sie die Datei hoch, geben Sie dem Buch einen Namen (er wird aus der Datei, dem Titel eines Dokuments oder der Figur eines Chats vorgeschlagen; das Buch heißt *Neues Buch*, wenn Sie das Feld leer lassen) und klicken Sie auf **Importieren**. Ein Fortschrittsfenster mit **Stopp** zeigt, wie die Abschnitte gespeichert werden;
Stoppen behält, was bereits gespeichert wurde. Eine Datei, die die Quelle nicht lesen kann, wird mit einer Warnung abgelehnt, die übrigen Uploads bleiben erhalten.

Dasselbe Fenster fügt über das Einstellungsmenü (⚙) eines Buches die Abschnitte diesem Buch an einer Stelle Ihrer Wahl hinzu, siehe
[Import in das Buch](story-editor.md#importing-into-the-book).

![Die Quellen im Importfenster](../../images/import-source.png)

![Import eines Zip-Archivs mit Textdateien als neues Buch](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Ein fertiges Manuskript importieren

Für einen bereits vorhandenen Entwurf wählen Sie **Aus einem Dokument importieren** und laden die Datei hoch: eine `.docx` (Word), `.odt` (LibreOffice, OpenOffice), `.rtf` (Rich Text, von WordPad, TextEdit und anderen Programmen), `.md` (eine Markdown-Datei), `.txt` (Klartext) oder `.epub` (ein E-Book). Der Text wird zu den Abschnitten eines neuen Buches, alles als von Ihnen geschrieben markiert (der Import zählt nicht als Schreiben in der [Statistik](statistics.md), und der Verlauf eines Abschnitts lautet *Import aus einem Dokument*).

- **Kapitel.** Ein Absatz im Format *Überschrift* (eine `#`-Überschrift in einer Markdown-Datei) beginnt ein Kapitel, und jedes Kapitel beginnt mit seiner Überschrift (die erste
  Überschrift eines Abschnitts ist sein Kapiteltitel, wie beim [Export](export.md)). **Kapitel beginnen bei Überschriftenebene** legt fest, welche
  Überschriften das sind: die gewählte Ebene und alle darüberliegenden (anfangs wird die flachste Ebene des Dokuments gewählt); tiefere
  Überschriften bleiben als kleinere Überschriften im Text. Ein Dokument ohne Überschriften ist ein einziges Kapitel. Der *Titel* des Dokuments wird nicht als Text importiert, er benennt das Buch (in einer Markdown-Datei der `title:` im Kopf am Anfang oder eine einzelne `#`-Überschrift vor den `##`-Kapiteln). Eine Klartextdatei hat keine Überschriften, daher beginnt eine eigene Zeile wie `Kapitel 12`, `Teil IV: Die Rückkehr`,
  `Buch zwei` oder `Prolog` (auch `Epilog`, `Zwischenspiel`, `Vorwort`, `Nachwort`) ein Kapitel; die Wahl der Überschriftenebene entfällt dann.
- **Wörter pro Abschnitt.** Der Text wird zwischen Absätzen in Abschnitte von etwa dieser Anzahl Wörter geschnitten (anfangs 1000), und zwar an der Absatzgrenze, die der Zahl am nächsten liegt. Ein Abschnitt enthält nie das Ende eines Kapitels und den Anfang des nächsten, und ein Kapitel wird nicht grundlos geteilt: Was am Ende eines Kapitels übrig bliebe, wenn es weniger als die Hälfte der Zahl ist, bleibt beim vorhergehenden Abschnitt, sodass ein Kapitel von bis zu anderthalbfacher Zahl ein einziger Abschnitt ist. Ein Absatz, der länger als die Zahl ist, wird nicht geschnitten.
- Eine Zeile unter dem Namen zeigt, wie viele Kapitel, Wörter und Abschnitte der Import ergeben wird; sie ändert sich, wenn Sie die Optionen ändern.
- **Absätze einer Textdatei.** Leerzeilen trennen die Absätze, und die Zeilen eines Absatzes werden zusammengefügt (Text, der bei einer Spaltenbreite umbricht, wird zu einem Absatz); eine Datei ohne Leerzeilen hat für jede Zeile einen Absatz. Eine Markdown-Datei wird so übernommen, wie sie geschrieben ist, geschnitten an Leerzeilen, Überschriften und Codeblöcken; Text in Windows-1252 statt UTF-8 wird ebenfalls gelesen.
- **Formatierung.** Kursiv und fett bleiben erhalten (als Markdown `*kursiv*`, `**fett**`); die übrige Formatierung (Schriften, Größen, Ausrichtung), Bilder, Kopf- und Fußzeilen, Fußnoten, Kommentare und gelöschter Text aus nachverfolgten Änderungen werden nicht importiert. Tabellen werden Zelle für Zelle gelesen.

![Import eines Word-Dokuments, geschnitten in Abschnitte von etwa 500 Wörtern](../../images/document-import.png)

Eine alte `.doc`-Datei muss zuerst als `.docx`, `.odt` oder `.rtf` gespeichert werden.

**Ein EPUB** wird Seite für Seite in seiner Lesereihenfolge gelesen. Die Überschriften der Seiten (`h1` bis `h6`) sind die Kapitel; eine Seite ohne Überschrift, die das Inhaltsverzeichnis des E-Books nennt, erhält diesen Namen als Überschrift. Umschlag, Inhaltsverzeichnis, Fußnoten und die Lesungen von Ruby-Text bleiben weg, und der Titel des E-Books benennt das Buch. Ein per DRM geschütztes Buch kann nicht gelesen werden.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Aus einem Obsidian-Vault oder einem Scrivener-Projekt

Beides sind Ordner, daher packen Sie den Ordner zuerst als ZIP (auf dem Mac **Komprimieren** im Finder; die `.scriv` von Scrivener ist ebenfalls ein Ordner) und laden die `.zip` hoch. Die Optionen sind dieselben wie bei einem Dokument.

- **Obsidian-Vault.** Jede Notiz (`.md`) ist ein Kapitel, benannt nach der Notiz (oder dem `title:` ihres Kopfes), in der Reihenfolge der Pfade, wobei Zahlen als Zahlen gezählt werden (`Kapitel 2` vor `Kapitel 10`). Ein Ordner ist ein Abschnitt über seinen Notizen, und die Überschriften innerhalb einer Notiz liegen unter der eigenen Überschrift der Notiz. Links `[[Notiz|Text]]` werden zu ihrem Text; Einbettungen (`[[Datei]]` mit einem `!` davor), Kommentare `%%...%%`, Block-IDs und die Markierung eines Hinweises werden entfernt; eine Hervorhebung `==Text==` ist einfacher Text. Versteckte Ordner (`.obsidian`, `.trash`), Anhänge und Notizen ohne Text werden übersprungen. Wenn der Vault Notizen enthält, die nicht zur Geschichte gehören, löschen Sie sie, bevor Sie zippen.
- **Scrivener-Projekt.** Importiert wird das Manuskript (der Ordner *Draft* des Binders, Scrivener 2 und 3), nicht die Recherche, der Papierkorb oder die Dokumente, die nicht *in Kompilierung enthalten* sind. Ein Ordner oder ein Dokument, unter dem Dokumente liegen, ist eine Überschrift (ihre Tiefe im Binder ist ihre Ebene, ihr Titel der Text); die Dokumente, die ein Ordner nur enthält, sind seine Szenen, eine nach der anderen, mit einem Szenenwechsel dazwischen (ihre Titel werden nicht verwendet). Ein Dokument direkt im Manuskript oder in einem Ordner mit anderen Ordnern ist ein Kapitel mit seinem Titel. Der Name des Projekts benennt das Buch.

Was auch immer die Quelle ist, das neue Buch wird nach Abschluss des Imports geöffnet (auch wenn Sie abgebrochen haben), und das Fenster [**Import abgeschlossen**](story-editor.md#after-an-import) bietet an, die Zusammenfassungen der Abschnitte zu schreiben und einen Agenten die Lore-Notizen erstellen zu lassen.

<a id="importing-from-sillytavern"></a>

### Aus SillyTavern importieren

Um ein Rollenspiel oder eine Geschichte fortzusetzen, die Sie in [SillyTavern](https://github.com/SillyTavern/SillyTavern) begonnen haben, verwenden Sie
**Importieren → Aus SillyTavern importieren**. Exportieren Sie in SillyTavern den Chat
(*Chatdateien verwalten*, oder nehmen Sie die `.jsonl`-Dateien aus `data/<user>/chats/<character>/`), dann:

1. Laden Sie **einen oder mehrere** `.jsonl`-Chats hoch. Eine Datei, die kein SillyTavern-Chat ist, wird abgelehnt, die übrigen bleiben erhalten.
2. Geben Sie dem Buch einen Namen (er wird aus der Figur des ersten Chats vorgeschlagen).
3. Setzen Sie den Haken bei **Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen**, wenn Sie einen Abschnitt pro Runde möchten: Die Nachricht des Benutzers (mehrere Nachrichten hintereinander werden durch eine Leerzeile verbunden) wird zu den *Anweisungen* des Abschnitts und die Antwort danach zu seinem Text. Ohne den Haken ist jede Nachricht ein eigener Abschnitt mit ihrem Text als Text des Abschnitts, sodass das Buch so liest, wie der Chat gelesen wurde.
4. Klicken Sie auf **Importieren**. Ein Fortschrittsfenster mit **Stopp** zeigt, wie die Abschnitte gespeichert werden; Stoppen behält, was gespeichert wurde.

![Import eines SillyTavern-Chats](../../images/sillytavern-import.png)

SillyTavern kopiert den Chat, wenn Sie einen *Zweig* oder einen *Checkpoint* erstellen, daher teilen die Chats einer Geschichte ihren Anfang. Laden Sie sie zusammen hoch, und Marginalia findet die gemeinsamen Nachrichten und speichert sie nur einmal: Die Chats werden zu den Zweigen eines Baums, den Sie in der [Baumansicht](story-editor.md) durchgehen können. Die *Swipes* (alternative Antworten) einer Nachricht werden zu Geschwisterabschnitten, und das Buch öffnet sich am Ende des zuletzt geschriebenen Chats (nach dem Datum seiner letzten Nachricht). Chats, die nicht mit derselben Nachricht beginnen, werden zu getrennten ersten Abschnitten. Versteckte (System-)Nachrichten werden übersprungen, ebenso leere Swipes. Die Begründung und der Modellname einer Antwort bleiben beim Abschnitt erhalten.

<a id="importing-from-lm-studio"></a>

### Aus LM Studio importieren

Um eine Unterhaltung fortzusetzen, die Sie in [LM Studio](https://lmstudio.ai) geführt haben, verwenden Sie **Importieren → Aus LM Studio importieren**.
LM Studio speichert jede Unterhaltung in einer Datei `<Nummer>.conversation.json` im Ordner `.lmstudio/conversations` in Ihrem Benutzerordner (unter Windows
`%USERPROFILE%\.lmstudio\conversations`). Es funktioniert wie [Aus SillyTavern importieren](#importing-from-sillytavern):

1. Laden Sie **eine oder mehrere** Unterhaltungen hoch. Eine Datei, die keine Unterhaltung von LM Studio ist, wird abgelehnt, die übrigen bleiben erhalten.
2. Geben Sie dem Buch einen Namen (er wird mit dem Namen der Unterhaltung vorgeschlagen, oder dem Dateinamen, wenn sie keinen hat).
3. Setzen Sie den Haken bei **Benutzer- und Assistentennachrichten zu einem Abschnitt zusammenführen** für einen Abschnitt pro Runde (Ihre Nachricht sind die *Anweisungen* des Abschnitts), oder lassen Sie ihn weg, sodass jede Nachricht ein eigener Abschnitt ist.
4. Klicken Sie auf **Importieren**.

![Das Fenster des Imports aus LM Studio mit einer hochgeladenen Unterhaltung](../../images/lmstudio-import.png)

Die Fassungen einer Nachricht, die LM Studio aufbewahrt (die neu generierten Antworten, der bearbeitete Text), werden zu Geschwisterabschnitten, und das Buch öffnet sich auf der Fassung, die angezeigt wurde; Unterhaltungen, die denselben Anfang haben, werden zu einem Baum verbunden. Das Denken eines Reasoning-Modells wird als Begründung des Abschnitts zusammen mit dem Modellnamen behalten. Der Systemprompt der Unterhaltung, die Werkzeugaufrufe und angehängte Dateien werden nicht importiert.

Um ein Buch aus einer Sicherungsdatei zu beginnen, verwenden Sie **Sicherung als neues Buch importieren** im Reiter *Einstellungen*, siehe
[Buchsicherungen](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## Der Reiter „Über“

![Der Reiter „Über“ eines Buchs](../../images/book-about.png)

| Feld | |
|---|---|
| **Name** | Der Name des Buchs. |
| **Tags** | Tags zum Sortieren deiner Bücher und zur Aktivierung von [Lorebooks](../lorebooks/entries.md). Tippe, um einen vorhandenen Tag auszuwählen oder einen neuen anzulegen. |
| **Beschreibung** | Worum es im Buch geht. Den Prompt-Vorlagen als Buchbeschreibung zur Verfügung gestellt. |
| **Sprache** | Die Sprache, in der das Buch geschrieben ist. Sie dient der automatischen Silbentrennung des Textes im Buchlesestil und im [Viewer](publishing.md); sie ändert nicht, was das Modell schreibt. Ein leeres Feld bedeutet *erben*: Es gilt die Sprache des nächsten [Ordners](folders.md#language) über dem Buch, der eine setzt, andernfalls Englisch. Das Feld zeigt dann an, welche Sprache es ist und woher sie stammt. |
| **Modell** | Der [Inferenzanbieter](../inference-providers.md), der dieses Buch schreibt. |
| **Protokolle** | Das [Protokoll](../protocols.md) mit den Generierungseinstellungen. |
| **Aufbewahrte Versionen je Abschnitt** | Wie viele frühere Versionen des Textes eines Abschnitts der [Verlauf](version-history.md) aufbewahrt. Ein leeres Feld bedeutet *erben*: Die graue Zahl im Feld ist die Grenze, die gilt, und die Zeile darunter sagt, welcher Ordner, deine Einstellungen oder der Administrator sie festlegt. **0** bewahrt keinen Verlauf auf. |
| **Veröffentlicht** | Erlaubt anderen Benutzern, das Buch zu lesen, siehe [Veröffentlichung & Viewer](publishing.md). Sobald das Kästchen angehakt ist, wird der Leselink des Buchs (`<Adresse von Marginalia>/view/<Buch-ID>`) unter dem Kästchen angezeigt; kopiere ihn und sende ihn an die Leser. |
| **Die Bilder den Lesern zeigen** | Standardmäßig angehakt. Leser eines veröffentlichten Buchs sehen die Bilder seiner Abschnitte; entferne den Haken, um ihnen nur den Text zu zeigen. Deine eigenen Bilder siehst du immer. |

Änderungen werden beim Tippen gespeichert.

Darunter stehen schreibgeschützte Zählungen:

| | |
|---|---|
| **Wörter insgesamt**, **Tokens insgesamt** | Alle Abschnitte des Buchs, in allen Zweigen. |
| **Wörter des aktiven Zweigs**, **Tokens des aktiven Zweigs** | Nur die Abschnitte des [aktiven Zweigs](branches-and-story-tree.md). |
| **Urheberschaft des Zweigs** | Drücke **Berechnen**: Wie viele Prozent der Wörter des aktiven Zweigs du geschrieben hast und wie viele ein Modell erzeugt hat, sowie die Anzahl der Wörter. Sie wird aus dem [Verlauf](version-history.md) jedes Abschnitts ermittelt, daher dauert es bei einem langen Buch eine Weile. Ein Abschnitt, dessen Verlauf ausgeschaltet ist, wird mit dem Text verglichen, den das Modell zuerst erzeugt hat. Siehe [Markierung generierten Textes](story-editor.md#highlighting-generated-text). |

Ein Buch braucht ein Modell und ein Protokoll, um zu generieren. Fehlen sie, bricht die Generierung mit *Dem Buch fehlt ein Modell.* oder *Dem Buch fehlt ein Protokoll.* ab.

<a id="deleting-a-book"></a>

## Ein Buch löschen

Klicke in der Buchliste auf die Papierkorb-Schaltfläche und bestätige. Das Buch verschwindet aus deiner Liste.

Ein Buch wird nicht gelöscht, solange etwas daran arbeitet: ein Abschnitt, der generiert wird, ein Zusammenfassungs-Stapel, eine Übersetzung, ein Neuentwurf- oder Anmerkungsfenster, oder das Buch ist in einem anderen Fenster geöffnet. Marginalia sagt dann, was im Weg ist (*Dies wird gerade verwendet: ...*); warte, bis es fertig ist, oder stoppe es, und lösche erneut. Dasselbe gilt für das Wiederherstellen eines [Backups](backups.md) in das Buch, während ein anderes Fenster oder ein Lauf daran arbeitet.

Gelöschte Bücher werden nicht sofort aus der Datenbank entfernt. Bis ein Administrator sie zusammen mit all ihren Abschnitten mit [Bereinigung](../administration/cleanup.md) entfernt, kannst du das Buch über den Reiter [Papierkorb](../trash.md) zurückholen. Bis dahin sind die Daten noch in den [Datenbank-Backups](../administration/database-backups.md) enthalten. Die eigenen [Backups](backups.md) des Buchs sind Dateien und bleiben, wo sie sind.
