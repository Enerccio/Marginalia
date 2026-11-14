---
label: Charakter-Tracker
order: 19
---

# Charakter-Tracker

Die Besetzung deines Buchs in der Seitenleiste: eine Liste von Charakteren mit einem Slogan, einer Notiz, einem Symbol und einer Farbe, die Beziehungen zwischen ihnen und eine Karte dieser Beziehungen.

<a id="the-characters-tab"></a>

## Der Reiter Charaktere

Die linke Seitenleiste des [Geschichteneditors](../books/story-editor.md) erhält neben *Gliederung* einen Reiter **Charaktere**. Über der Liste steht eine Zeile mit Schaltflächen: **Hinzufügen** (ein neuer Charakter), ein Menü mit dem Import von Lore-Einträgen, die **Beziehungskarte** und eine Sprechblasen-Schaltfläche für den Prompt (Namen beim Darüberfahren).

Die Sprechblasen-Schaltfläche öffnet ein Popover mit **Im Prompt**, **Tiefe** und einer Rolle, ähnlich wie bei der [Autorennotiz](authors-note.md); die Schaltfläche ist grün, solange die Charaktere im Prompt enthalten sind. Ist *Im Prompt* aktiviert, erhält jede Generierung die Charaktere als Nachricht: eine Zeile mit Name, Slogan in Klammern und Notiz je Charakter sowie eine Liste der Beziehungen (`Mara -> Tomas: employs`, `<->`, wenn sie in beide Richtungen gilt). *Tiefe* ist die Anzahl der Nachrichten nach ihr (`0` ganz am Ende, nach deinen Anweisungen, `1` direkt davor), und die Rolle ist *System*, *Assistent* oder *Benutzer*. Der Text beansprucht seine Tokens aus dem Kontingent für die Geschichte. Solange das Buch keine Charaktere hat, wird nichts eingefügt. Die Einstellungen werden zusammen mit dem Buch gespeichert.

Die Zauberstab-Schaltfläche dazwischen hat den Menüpunkt **Lore-Einträge als Charaktere importieren**, siehe [unten](#importing-characters-from-the-lorebook).

Die Liste ist nach Namen sortiert. Jeder Charakter hat seine eigene Farbe und zeigt seinen **Namen** mit dem **Slogan** darunter in kleinerer Schrift (höchstens zwei Zeilen). Die Schaltflächen eines Charakters:

| Schaltfläche | |
|---|---|
| Pfeil nach unten | Klappt den Charakter auf, um sein **Symbol** und seine **Notiz** zu zeigen. |
| Stift | Öffnet den Charakter im Dialog. |
| Papierkorb | Löscht den Charakter und alle seine Beziehungen, nach einer Bestätigung. |

Charaktere werden mit dem Buch gespeichert, sobald du sie änderst, und sind daher in seinen Sicherungen enthalten.

![Der Reiter Charaktere](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Charaktere für einen Zug auswählen

Das Formular für den [neuen Zug](../books/story-editor.md) enthält die Felder **POV-Charakter** und **Anwesende Charaktere**. Sie bleiben Freitext, aber jedes hat am Ende eine Schaltfläche mit einem Personensymbol, die die Charaktere des Buchs nach Namen sortiert auflistet. Ein in der Liste des **POV-Charakters** gewählter Name ersetzt den Inhalt des Feldes; ein in der Liste **Anwesende Charaktere** gewählter Name wird nach einem Komma am Ende angehängt, sofern er dort nicht schon steht. Die Liste wird beim Öffnen gelesen, daher ist ein Charakter, den du soeben angelegt hast, bereits enthalten. Du kannst weiterhin jeden beliebigen Namen eintippen; ein Name im Feld muss kein Charakter des Buchs sein.

<a id="the-character-dialog"></a>

## Der Charakter-Dialog

**Hinzufügen** (die Schaltfläche mit dem Plus; der Tooltip lautet *Neuen Charakter hinzufügen*) und der Stift öffnen denselben Dialog:

| Feld | |
|---|---|
| **Name** | Pflichtfeld. |
| **Slogan** | Einige Worte über den Charakter, angezeigt in der Liste und auf der Karte. |
| **Notiz** | Alles, was du dir merken möchtest. |
| **Symbol** | Lade ein PNG, JPEG oder GIF hoch. Es wird auf höchstens 200 x 200 Pixel verkleinert, die Proportionen bleiben erhalten. |
| **Farbe** | Hintergrund des Charakters in der Liste und auf der Karte. |

Die Felder werden mit **Speichern** übernommen.

![Ein Charakter mit seinen Beziehungen](../../images/plugin-charactertracker-dialog.png)

Wenn du **Abbrechen** (oder Esc) drückst, während ungespeicherte Änderungen vorliegen – einschließlich einer Beziehung, die du ausgefüllt, aber noch nicht hinzugefügt hast –, wirst du gefragt, ob sie verworfen werden sollen.

![Die Rückfrage beim Abbrechen eines Charakters mit ungespeicherten Änderungen](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Beziehungen

Wenn du einen vorhandenen Charakter öffnest, listet der Dialog auch seine Beziehungen auf: jene, auf die der Charakter zeigt (`→`), jene, die auf ihn zeigen (`←`), und die einseitigen (`↔`), jeweils mit Name und Slogan. Die Papierkorb-Schaltfläche löscht eine Beziehung.

Um eine hinzuzufügen, suche den anderen Charakter, wähle die **Richtung** (*dieser Charakter → anderer*, *anderer → dieser Charakter* oder *in beide Richtungen*), gib einen **Namen** ein (z. B. *employs*, *friends*) und optional einen **Slogan**, und drücke **Beziehung hinzufügen** (die Schaltfläche steht über diesen Feldern). Beziehungen werden sofort gespeichert. Ein neuer Charakter kann Beziehungen erhalten, sobald er gespeichert wurde.

<a id="importing-characters-from-the-lorebook"></a>

## Charaktere aus dem Lorebook importieren

Sind deine Charaktere bereits im Lorebook, erstellt sie das Zauberstab-Menü für dich. **Lore-Einträge als Charaktere importieren** öffnet einen Dialog mit den Einträgen des Lorebooks des Buchs und all seiner Unter-Lorebooks, sortiert nach ihrer *Reihenfolge*. Jeder zeigt seinen Namen und ein Kontrollkästchen sowie ein **i**-Symbol, das den Inhalt des Eintrags in einem Popover zeigt, damit du ihn dir vor der Entscheidung kurz ansehen kannst; das Kontrollkästchen im Kopf der Liste markiert oder entfernt alle Einträge. **Importieren** erstellt je angekreuztem Eintrag einen Charakter mit dessen Namen (Slogan, Notiz, Symbol und Farbe bleiben leer, damit du sie ausfüllen kannst), **Schließen** verlässt den Dialog ohne Import.

Einträge werden nicht mit den Charakteren verglichen, die du bereits hast: Wer denselben Eintrag zweimal importiert, erhält zwei Charaktere.

![Charaktere aus dem Lorebook importieren](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## Die Beziehungskarte

**Beziehungskarte** zeigt alle Beziehungen des Buchs als Graph. Jeder Charakter mit einer Beziehung ist ein Kreis mit seinem Symbol (oder einem Kästchen ohne Symbol) auf seiner Farbe, mit Name und Slogan; jede Beziehung ist ein Pfeil zwischen zwei Charakteren mit Name und Slogan. Ein Pfeil zeigt auf das Ziel; eine einseitige Beziehung hat an beiden Enden einen Pfeil. Charaktere ohne Beziehungen sind nicht auf der Karte.

![Die Beziehungskarte](../../images/plugin-charactertracker-map.png)

Die Charaktere sind in Ringen um einen Charakter angeordnet – zunächst um denjenigen mit den meisten Beziehungen: Die Charaktere, mit denen er verbunden ist, liegen auf dem ersten Ring, deren Verbindungen auf dem zweiten, und so weiter. **Doppelklicke auf einen Charakter**, um die Karte um ihn herum anzuordnen. Außerdem kannst du die Charaktere ziehen, die Karte verschieben und mit dem Mausrad zoomen.

<a id="commands"></a>

## Befehle

Die Charaktere sind auch [Befehle](../commands/index.md), für die Befehlszeile und für [Agenten](../agents.md). Sie wirken auf das geöffnete Buch (bei einem Agenten: auf seine Arbeitskopie), und ein geöffneter Reiter Charaktere zeigt die Änderung sofort an. Ein Charakter wird über seinen **Namen** angegeben (Groß- und Kleinschreibung spielen keine Rolle) oder über seine **Nummer** in `/list-characters` (die Reihenfolge der Namen), wenn zwei Charaktere denselben Namen haben. Eine Beziehung wird über ihre Nummer in `/list-character-relations` angegeben. Ein Befehl, der einen Charakter ändert, übernimmt nur die Felder, die du angibst; ein leerer Text löscht einen Slogan oder eine Notiz. Das Symbol kann nur im Dialog gesetzt werden.

| Befehl | Was er tut |
|---|---|
| `/list-characters` | Die Charaktere nach Namen sortiert: Slogan, Farbe, ob es eine Notiz gibt, Anzahl der Beziehungen. Liest nur. |
| `/get-character character` | Ein Charakter mit seiner Notiz und seinen Beziehungen. Liest nur. |
| `/create-character name [tagline [note [color]]]` | Fügt einen Charakter hinzu. `color` ist `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Ändert einen Charakter. |
| `/delete-character character` | Löscht einen Charakter mit seinen Beziehungen, nach einer Bestätigung. |
| `/list-character-relations [character]` | Die Beziehungen oder die eines Charakters. Liest nur. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Fügt eine Beziehung von `source` zu `target` hinzu (*employs*), oder mit `bidirectional` `true` eine, in der beide gleichberechtigt sind (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Ändert den Namen oder den Slogan einer Beziehung. |
| `/delete-character-relation relationship` | Löscht eine Beziehung, nach einer Bestätigung. |
| `/character-tracker-map` | Die Charaktere und ihre Beziehungen als Text, den der Prompt erhält. Liest nur. |

`character-tracker-map` ist für das [Makro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands) gedacht: Füge es in die Hauptvorlage, den Benutzer-Prompt oder einen Lorebook-Eintrag ein, um die Charaktere dort im Prompt zu platzieren, wo du sie haben möchtest – anstelle (oder zusätzlich zu) der automatischen Einfügung durch den Reiter Charaktere. Es zeigt immer den aktuellen Stand der Charaktere, unabhängig davon, ob die automatische Einfügung eingeschaltet ist.

Hinzufügen und Ändern sind nicht destruktiv; nur die `delete`-Befehle sind es, und ein Agent kann so eingestellt sein, dass er dich vorher fragt.

<a id="phone"></a>

## Telefon

Der Reiter befindet sich in der Schublade der Geschichte, neben der Gliederung, wie die [Autorennotiz](authors-note.md). Die Schaltflächen für die POV- und anwesenden Charaktere sind auch im Formular für den neuen Zug auf dem Telefon vorhanden.
