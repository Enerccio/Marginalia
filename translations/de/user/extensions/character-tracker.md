---
label: Figurenverwaltung
order: 19
---

# Figurenverwaltung

Die Figuren deines Buchs in der Seitenleiste: eine Liste von Figuren mit Kurzbeschreibung, Notiz, Symbol und Farbe, die Beziehungen zwischen ihnen und eine Karte dieser Beziehungen.

<a id="the-characters-tab"></a>

## Der Tab Characters

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) erhält einen Tab **Characters** neben *Outline*. Über der Liste steht eine Zeile mit Schaltflächen: **Add** (eine neue Figur), ein Menü zum Import von Lore-Einträgen, die **Relationship map** und eine Sprechblasen-Schaltfläche für den Prompt (beim Darüberfahren erscheinen die Namen).

Die Sprechblasen-Schaltfläche öffnet ein Popover mit **In prompt**, **Depth** und einer Rolle, ähnlich wie beim [Author's Note](authors-note.md). Die Schaltfläche ist grün, solange die Figuren im Prompt sind. Ist *In prompt* aktiviert, erhalten alle Generierungen die Figuren als Nachricht: eine Zeile mit Name, Kurzbeschreibung in Klammern und Notiz jeder Figur sowie eine Liste der Beziehungen (`Mara -> Tomas: employs`, `<->`, wenn sie in beide Richtungen gelten). *Depth* gibt an, nach wie vielen Nachrichten sie eingefügt wird (`0` ganz ans Ende, nach deinen Anweisungen, `1` direkt davor). Die Rolle ist *System*, *Assistant* oder *User*. Der Text beansprucht Tokens aus dem Kontingent für die Geschichte. Solange das Buch keine Figuren hat, wird nichts eingefügt. Die Einstellungen werden mit dem Buch gespeichert.

Der Zauberstab-Button zwischen beiden enthält den Menüpunkt **Import lore entries as characters**, siehe [unten](#importing-characters-from-the-lorebook).

Die Liste ist nach Namen sortiert. Jede Figur hat ihre eigene Farbe und zeigt ihren **Namen** mit der **Kurzbeschreibung** darunter in kleinerer Schrift (höchstens zwei Zeilen). Die Schaltflächen einer Figur:

| Schaltfläche | |
|---|---|
| Winkel | Klappt die Figur auf, um ihr **Symbol** und ihre **Notiz** zu zeigen. |
| Stift | Öffnet die Figur im Dialog. |
| Papierkorb | Löscht die Figur und alle ihre Beziehungen, nach einer Bestätigung. |

Figuren werden mit dem Buch gespeichert, sobald du sie änderst, und sind daher in seinen Sicherungen enthalten.

![Der Tab Characters](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Figuren für einen Zug auswählen

Das Formular für den [neuen Zug](../books/story-editor.md) hat die Felder **POV character** und **Present characters**. Sie bleiben Freitextfelder, aber jedes hat am Ende eine Schaltfläche mit einem Personensymbol, die die Figuren des Buchs nach Namen sortiert auflistet. Ein in der Liste von **POV character** gewählter Name ersetzt den Inhalt des Feldes. Ein in der Liste von **Present characters** gewählter Name wird nach einem Komma am Ende angefügt, sofern er dort noch nicht steht. Die Liste wird beim Öffnen gelesen, daher erscheint auch eine Figur, die du gerade eben angelegt hast. Du kannst weiterhin einen beliebigen Namen eintippen; ein Name im Feld muss keine Figur des Buchs sein.

<a id="the-character-dialog"></a>

## Der Figurendialog

**Add** (die Schaltfläche mit dem Plus; der Tooltip lautet *Add new character*) und der Stift öffnen denselben Dialog:

| Feld | |
|---|---|
| **Name** | Pflichtfeld. |
| **Tagline** | Ein paar Worte über die Figur, angezeigt in der Liste und auf der Karte. |
| **Note** | Alles, was du dir merken willst. |
| **Icon** | Lade ein PNG, JPEG oder GIF hoch. Es wird auf höchstens 200 x 200 Pixel verkleinert, die Proportionen bleiben erhalten. |
| **Color** | Hintergrundfarbe der Figur in der Liste und auf der Karte. |

Die Felder werden mit **Save** übernommen.

![Eine Figur mit ihren Beziehungen](../../images/plugin-charactertracker-dialog.png)

Drückst du **Cancel** (oder Esc), während ungespeicherte Änderungen vorliegen, auch eine Beziehung, die du ausgefüllt, aber noch nicht hinzugefügt hast, wirst du gefragt, ob sie verworfen werden sollen.

![Die Rückfrage beim Abbrechen einer Figur mit ungespeicherten Änderungen](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Beziehungen

Öffnest du eine bestehende Figur, listet der Dialog zusätzlich ihre Beziehungen auf: jene, auf die die Figur zeigt (`→`), jene, die auf sie zeigen (`←`), und die einseitigen (`↔`), jeweils mit Name und Kurzbeschreibung. Die Papierkorb-Schaltfläche löscht eine Beziehung.

Um eine hinzuzufügen, suche die andere Figur, wähle die **Direction** (*this character → other*, *other → this character* oder *both ways*), gib einen **Name** ein (z. B. *employs*, *friends*) und optional eine **Tagline**, und drücke **Add relationship** (die Schaltfläche steht über diesen Feldern). Beziehungen werden sofort gespeichert. Eine neue Figur kann Beziehungen erst erhalten, nachdem sie gespeichert wurde.

<a id="importing-characters-from-the-lorebook"></a>

## Figuren aus dem Lorebook importieren

Stehen deine Figuren bereits im Lorebook, legt das Zauberstab-Menü sie für dich an. **Import lore entries as characters** öffnet einen Dialog mit den Einträgen des Lorebooks des Buchs und aller seiner Unter-Lorebooks, sortiert nach ihrer *order*. Jeder Eintrag zeigt seinen Namen und ein Kontrollkästchen sowie ein **i**-Symbol, das den Inhalt des Eintrags in einem Popover zeigt, damit du vor der Entscheidung kurz hineinschauen kannst. Das Kontrollkästchen im Kopf der Liste wählt alle aus oder hebt die Auswahl auf. **Import** legt für jeden markierten Eintrag eine Figur mit dessen Namen an (Kurzbeschreibung, Notiz, Symbol und Farbe bleiben leer, damit du sie selbst ausfüllst). **Close** schließt den Dialog, ohne zu importieren.

Einträge werden nicht mit den bereits vorhandenen Figuren verglichen: Importierst du denselben Eintrag zweimal, entstehen zwei Figuren.

![Import von Figuren aus dem Lorebook](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## Die Beziehungskarte

**Relationship map** zeigt alle Beziehungen des Buchs als Graph. Jede Figur mit einer Beziehung ist ein Kreis mit ihrem Symbol (oder ein Kasten ohne Symbol) auf ihrer Farbe, mit Name und Kurzbeschreibung. Jede Beziehung ist ein Pfeil zwischen zwei Figuren, mit ihrem Namen und ihrer Kurzbeschreibung. Ein Pfeil zeigt auf das Ziel; eine einseitige Beziehung hat Pfeilspitzen an beiden Enden. Figuren ohne Beziehungen sind nicht auf der Karte.

![Die Beziehungskarte](../../images/plugin-charactertracker-map.png)

Die Figuren liegen in Ringen um eine Figur angeordnet – zunächst um jene mit den meisten Beziehungen: Die Figuren, mit denen sie verbunden ist, liegen auf dem ersten Ring, deren Verbindungen auf dem zweiten, und so weiter. **Doppelklicke auf eine Figur**, um die Karte um sie herum anzuordnen. Du kannst die Figuren außerdem ziehen, die Karte verschieben und mit dem Mausrad zoomen.

<a id="commands"></a>

## Befehle

Die Figuren sind auch [Befehle](../commands/index.md) für die Kommandozeile und für [Agenten](../agents.md). Sie arbeiten mit dem geöffneten Buch (bei einem Agenten mit dessen Arbeitskopie), und ein geöffneter Tab Characters zeigt die Änderung sofort an. Eine Figur wird über ihren **Namen** angegeben (Groß- und Kleinschreibung spielen keine Rolle) oder über ihre **Nummer** in `/list-characters` (der Reihenfolge der Namen), wenn zwei Figuren denselben Namen haben. Eine Beziehung wird über ihre Nummer in `/list-character-relations` angegeben. Ein Befehl, der eine Figur ändert, übernimmt nur die Felder, die du angibst; ein leerer Text löscht eine Kurzbeschreibung oder eine Notiz. Das Symbol lässt sich nur im Dialog setzen.

| Befehl | Was er tut |
|---|---|
| `/list-characters` | Die Figuren nach Namen sortiert: Kurzbeschreibung, Farbe, ob eine Notiz vorhanden ist, Anzahl der Beziehungen. Nur lesend. |
| `/get-character character` | Eine Figur mit ihrer Notiz und ihren Beziehungen. Nur lesend. |
| `/create-character name [tagline [note [color]]]` | Fügt eine Figur hinzu. `color` ist `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Ändert eine Figur. |
| `/delete-character character` | Löscht eine Figur mit ihren Beziehungen, nach einer Bestätigung. |
| `/list-character-relations [character]` | Die Beziehungen oder jene einer Figur. Nur lesend. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Fügt eine Beziehung von `source` zu `target` hinzu (*employs*) oder mit `bidirectional` `true` eine, bei der beide gleichberechtigt sind (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Ändert den Namen oder die Kurzbeschreibung einer Beziehung. |
| `/delete-character-relation relationship` | Löscht eine Beziehung, nach einer Bestätigung. |
| `/character-tracker-map` | Die Figuren und ihre Beziehungen als Text, den der Prompt erhält. Nur lesend. |

`character-tracker-map` ist für das [Makro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands) gedacht: Füge es in die Hauptvorlage, den Benutzer-Prompt oder einen Lorebook-Eintrag ein, um die Figuren dort im Prompt zu platzieren, wo du sie haben willst, anstelle (oder zusätzlich zu) der automatischen Einfügung des Tabs Characters. Es zeigt immer den aktuellen Stand der Figuren, unabhängig davon, ob die automatische Einfügung eingeschaltet ist.

Hinzufügen und Ändern sind nicht destruktiv; nur die `delete`-Befehle sind es. Ein Agent kann so eingestellt werden, dass er dich vor ihrer Ausführung fragt.

Der [Handlungsassistent](arc-outline.md) der Arc Outline nutzt dieselben Befehle, wenn du seine Werkzeuge für **Characters** ankreuzt. So kannst du das Figurenensemble weiterentwickeln, während du über die Handlung sprichst.

<a id="phone"></a>

## Telefon

Der Tab befindet sich im Menü der Geschichte neben der Gliederung, ebenso wie der [Author's Note](authors-note.md). Die Schaltflächen für die POV- und die anwesenden Figuren gibt es auch im Formular für den neuen Zug auf dem Telefon.
