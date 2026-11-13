---
label: Bücher
order: 960
icon: book
---

# Bücher

Ein Buch ist eine Geschichte. Du gibst ihm Anweisungen Teil für Teil, das Modell schreibt den Text, und das Buch behält
alles, was dazugehört: den Geschichtenbaum mit all seinen Zweigen, die Prompts für den Stil, das Lorebook mit der Welt
und den Figuren, Zusammenfassungen und Sicherungen.

![Ein Buch im Geschichten-Editor](../../images/hero-story-editor.png)

<a id="parts-and-branches"></a>

## Teile und Zweige

Ein Buch besteht aus **Teilen**. Jeder Teil ist eine Antwort des Modells, geschrieben aus den Anweisungen, die du dafür
gegeben hast (Szene, Erzählfigur, anwesende Figuren und was geschehen soll).

Die Teile bilden einen Baum. Meist setzt du die Geschichte einfach fort, du kannst aber auch eine weitere Fassung des
letzten Teils schreiben oder von einem früheren Teil aus anders weitermachen. Jeder Weg vom Anfang bis zu einem Ende ist
ein **Zweig**. Der Zweig, an dem du gerade arbeitest, ist der **aktive Zweig**; der Geschichten-Editor zeigt ihn an, und
nur seine Teile werden an das Modell gesendet. Siehe [Zweige & Geschichtenbaum](branches-and-story-tree.md).

<a id="the-book-window"></a>

## Das Buchfenster

Wenn du ein Buch öffnest, erscheint sein Fenster mit diesen Reitern:

| Reiter | |
|---|---|
| **Info** | Name, Tags, Beschreibung, Sprache, Modell, Protokoll, Veröffentlichung und Wortzahlen. Siehe [Bücher verwalten](managing-books.md). |
| **Prompts** | Die Vorlagen und Stileinstellungen dieses Buches. Siehe [Buch-Prompts](prompts.md). |
| **Lorebook** | Das Lorebook dieses Buches und seine Einträge. Wenn es Unter-Lorebooks hat, wechselt das zweite Feld **Angezeigtes Lorebook** den Reiter zu einem von ihnen (und zurück), ohne das Lorebook des Buches zu ändern; ein Klick auf den Namen eines Unter-Lorebooks unter **Unter-Lorebooks** tut dasselbe. Siehe [Lorebooks](../lorebooks/index.md). |
| **Geschichte** | Der Geschichten-Editor, in dem Teile geschrieben und erzeugt werden. Siehe [Geschichten-Editor](story-editor.md). |
| **Zweigansicht** | Der Geschichtenbaum mit allen Zweigen. Siehe [Zweige & Geschichtenbaum](branches-and-story-tree.md). |
| **Sicherungen** | Sicherungen des Buches. Siehe [Buch-Sicherungen](backups.md). |

Was mit einem Buch Tag für Tag geschehen ist (Wörter, Zeit, Token, Größe) und der Kalender der Tage, an denen du geschrieben
hast, findest du im Fenster **Statistik** des Buches, siehe [Schreibstatistik](statistics.md).

Die Geschichte kann über das Einstellungsmenü des Reiters *Geschichte* als Datei aus Marginalia herausgeführt werden, siehe
[Ein Buch exportieren](export.md).

Ein Buch ohne Teile öffnet sich auf *Info*, ein Buch mit Teilen auf *Geschichte*. Alle Änderungen werden sofort
gespeichert; es gibt keine Schaltfläche *Speichern*. **Beenden** schließt das Buch.

Während ein Teil erzeugt wird, sind die anderen Reiter und *Beenden* deaktiviert, bis die Erzeugung abgeschlossen oder
du sie abgebrochen hast.

<a id="in-this-section"></a>

## In diesem Abschnitt

- [Bücher verwalten](managing-books.md) - die Bücherliste, Anlegen, Bearbeiten und Löschen von Büchern
- [Ordner](folders.md) - Bücher in Ordnern sortieren, Einstellungen, die Bücher erben, Verschieben und Kopieren
- [Geschichten-Editor](story-editor.md) - Anweisungen schreiben, Teile erzeugen und bearbeiten
- [Zweige & Geschichtenbaum](branches-and-story-tree.md) - neu erzeugen, wischen, verzweigen, die Baumansicht
- [Versionsverlauf](version-history.md) - die früheren Fassungen eines Teils: vergleichen, wiederherstellen, benennen, suchen
- [Buch-Prompts](prompts.md) - Vorlagen, Erzählperspektive, Zeitform und Stil
- [Zusammenfassungen](summaries.md) - lange Bücher im Kontext des Modells halten
- [Ein Buch exportieren](export.md) - die Geschichte als Text, Markdown, HTML, Word, PDF oder EPUB
- [Buch-Sicherungen](backups.md) - Sicherungen, Wiederherstellung, Kopien eines Buches
- [Veröffentlichung & Betrachter](publishing.md) - Leseansicht und das Teilen eines Buches
