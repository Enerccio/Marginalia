---
label: Import & Export
order: 80
---

# Import & Export

<a id="exporting"></a>

## Exportieren

Wähle das Lorebook im Editor aus und klicke auf die Schaltfläche **export** (Dateisymbol, *Export Lorebook*). Der Browser lädt
`<lorebook name>.json` herunter mit:

- dem Lorebook und **allen seinen Unter-Lorebooks** (jeder Ebene), mit ihren Namen, Beschreibungen, dem Status „aktiviert“ und Tags;
- jedem Eintrag mit Inhalt, Notiz, Reihenfolge, Filter, Einfügemodus, Tags und Negativ-Tags.

Du kannst die Datei nutzen, um eine Kopie aufzubewahren, eine Welt mit anderen Benutzern zu teilen oder sie in eine andere Installation zu übertragen. Die Daten, die [Erweiterungen](../extensions/index.md) in einem Lorebook oder einem Eintrag speichern, sind ebenfalls in der Datei enthalten und werden beim Import wiederhergestellt. Die Eintragsrevisionen der Erweiterung [Lorebook VCS](../extensions/lorebook-vcs.md) gehören zu den alten Einträgen und erscheinen nicht bei den Einträgen, die der Import erzeugt; die Erweiterung hat dafür einen eigenen Export.

<a id="exporting-for-sillytavern"></a>

### Export für SillyTavern

Die Schaltfläche **export** öffnet ein Menü mit zwei Einträgen: **Export Lorebook** (die oben beschriebene Datei) und **Nach SillyTavern exportieren**. Letzterer lädt das Lorebook als Weltinformationsdatei für SillyTavern herunter, die SillyTavern importieren kann. Er ist das Gegenstück zum [Import](#importing-sillytavern-world-info):

| Marginalia | SillyTavern |
|---|---|
| *Eintragsname* | Titel / Memo (`comment`) |
| *Inhalt*, *Reihenfolge*, *Aktiviert* | Inhalt, Reihenfolge, deaktiviert |
| Kein Filter | Konstanter Eintrag (🔵) |
| Filter *Text* | Ein primärer Schlüssel |
| *Schlüsselwörter (beliebiges)* | Ein primärer Schlüssel pro Schlüsselwort |
| *Schlüsselwörter (alle)* | Das erste Schlüsselwort ist der primäre Schlüssel, die übrigen sind sekundäre Schlüssel mit der Logik *AND ALL* |
| *Regex* | Ein Schlüssel in der Form `/muster/flags` |
| *Im Lore-Block* | Position *vor Charakterdefinitionen* |
| *Vor dem Benutzerprompt* | Position *in Tiefe* 1 |
| Tags | Charakterfilter (nach Name); Negativ-Tags nur, wenn der Eintrag keine Tags hat (*exclude*) |

Die **Unter-Lorebooks werden zusammengeführt**: Die Datei enthält zuerst die Einträge des Lorebooks, danach die seiner Unter-Lorebooks, da SillyTavern keine Lorebooks innerhalb von Lorebooks kennt. Was SillyTavern kein Gegenstück bietet, wird weggelassen: die *Notiz* eines Eintrags, Negativ-Tags zusammen mit Tags sowie die Daten von Erweiterungen. SillyTavern gleicht die Schlüssel mit dem letzten Chat ab; für die umgekehrte Richtung siehe [Was anders funktioniert](#what-works-differently).

!!! warning
Ein regulärer Ausdruck wird in der Java-Syntax geschrieben. SillyTavern verwendet JavaScript. Einfache Ausdrücke funktionieren daher, aber alles, was nur in Java gilt (possessive Quantifizierer, Klassen `\p{...}`), wird dort nicht erkannt.
!!!

<a id="importing-a-marginalia-lorebook"></a>

## Ein Marginalia-Lorebook importieren

1. Klicke auf die Schaltfläche **import** (Dateisymbol) und wähle **Import Marginalia Lorebook**.
2. Lade eine aus Marginalia exportierte Datei hoch.

Der Import erzeugt immer **neue** Lorebooks – das Lorebook und seine Unter-Lorebooks mit derselben Struktur –, auch wenn du bereits Lorebooks mit denselben Namen hast. Das importierte Lorebook wird im Editor ausgewählt. Tags werden anhand des Namens mit deinen vorhandenen Tags abgeglichen oder neu angelegt.

Dateien aus älteren Marginalia-Versionen (ein einzelnes Lorebook ohne Unter-Lorebooks) können ebenfalls importiert werden.

!!!
Lorebooks sind auch in [Buch-Backups](../books/backups.md) enthalten. Beim Wiederherstellen oder Klonen eines Backups kann auf bereits vorhandene Lorebooks verwiesen werden, anstatt Kopien zu erstellen; siehe [Wiederherstellen eines Backups](../books/backups.md#restoring-a-backup).
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## Ein Lorebook importieren, das ein anderer Benutzer geteilt hat

**Von anderem Benutzer importieren** im Importmenü listet die Lorebooks auf, die andere Benutzer mit dir [teilen](lorebooks.md#sharing-a-lorebook), mit Name, Autor und Beschreibung. Öffne eine Zeile, um die Einträge zu lesen, und klicke auf **Import**, um eine private Kopie zu erhalten. Es werden nur die Unter-Lorebooks einbezogen, die der Autor teilt. Anders als beim Dateiimport muss nichts exportiert und verschickt werden: Der Autor aktiviert lediglich **Mit anderen Benutzern teilen**.

<a id="importing-an-archive-of-text-files"></a>

## Ein Archiv mit Textdateien importieren

1. Klicke auf die Schaltfläche **import** und wähle **Aus Archiv importieren**.
2. Lade eine `.zip`-Datei mit `.md`-Dateien (Markdown) hoch.
3. Prüfe die **Eintragsnamen** in der Tabelle und ändere sie bei Bedarf, dann klicke auf **Import**.

![Das Fenster zum Hochladen des Archivs](../../images/archive-lorebook-upload.png)

![Die Namen der Einträge vor dem Import](../../images/archive-entry-names.png)

Marginalia erzeugt ein neues Lorebook, das nach der Datei benannt ist (ohne `.zip`), mit **einem Eintrag für jede `.md`-Datei**. Die Dateien werden in der Reihenfolge ihrer Namen übernommen, wobei Zahlen in den Namen als Zahlen verglichen werden (`2.md` kommt vor `10.md`), auch in Ordnern des Archivs; die Einträge erhalten in dieser Reihenfolge die *Reihenfolge* 1, 2, 3 usw. Die gesamte Datei ist der *Inhalt*. Andere Dateien, Ordner, versteckte Dateien (`.name`) und der Ordner `__MACOSX` von auf einem Mac erstellten Archiven werden übersprungen.

**Eintragsnamen** werden in dieser Reihenfolge ermittelt:

1. eine Datei `names.txt` im Stammverzeichnis des Archivs: ein Name pro Zeile, die erste Zeile für die erste `.md`-Datei in der oben genannten Reihenfolge, und so weiter (eine leere Zeile überlässt diese Datei der nächsten Regel; überzählige Zeilen werden ignoriert). Nutze sie, um viele Dateien auf einmal zu benennen, zum Beispiel aus einem Skript;
2. die erste Überschrift der Datei (`# Kapitel 1` ergibt *Kapitel 1*);
3. der Dateiname ohne `.md`.

Die Tabelle im Dialog zeigt das Ergebnis, und du kannst jeden Namen überschreiben.

Der Import zeigt anschließend seinen Fortschritt. **Stopp** beendet ihn nach dem Eintrag, der gerade gespeichert wird: Das Lorebook behält die bereits importierten Einträge (der Import wird nicht rückgängig gemacht), und eine Meldung nennt, wie viele Dateien importiert wurden.

Die Einträge werden **deaktiviert** importiert: Sie haben keinen Filter, und aktiviert würden sie bei jeder Anfrage gesendet, was bei einem ganzen Buch mit Kapiteln kaum gewollt ist. Aktiviere die benötigten, mit einem Filter oder [Tags](entries.md#tags) – **Massenbearbeitung** ist dafür der schnelle Weg bei vielen Einträgen.

Um dieselben Dateien stattdessen als Abschnitte in eine Geschichte einzufügen, siehe [Import in das Buch](../books/story-editor.md#importing-into-the-book).

<a id="importing-sillytavern-world-info"></a>

## SillyTavern-Weltinformationen importieren

1. Klicke auf die Schaltfläche **import** und wähle **Aus SillyTavern importieren**.
2. Lade die aus SillyTavern exportierte Weltinformationsdatei (`.json`) hoch.

Marginalia erzeugt ein neues Lorebook, das nach den Weltinformationen (oder nach der Datei) benannt wird, und wandelt jeden Eintrag um:

| SillyTavern | Marginalia |
|---|---|
| Titel / Memo (`comment`) | *Eintragsname* (der erste Schlüssel, wenn kein Titel vorhanden ist) |
| Inhalt | *Inhalt* – Makros wie `{%{{{user}}}%}` und `{%{{{char}}}%}` funktionieren weiter, siehe [Makros](../templates/macros.md) |
| Reihenfolge | *Reihenfolge* |
| Deaktiviert | *Aktiviert* nicht angehakt |
| Konstant (🔵) oder ohne Schlüssel | immer aktiv (kein Filter) |
| Primäre Schlüssel, optionale sekundäre Schlüssel mit ihrer Logik | ein Filter, siehe unten |
| Groß-/Kleinschreibung beachten, ganze Wörter | im Filter enthalten |
| Position *vor / nach Charakterdefinitionen*, *Beispielnachrichten* | *Im Lore-Block* |
| Position *Autorennotiz* (oben / unten), *in Tiefe* | *Vor dem Benutzerprompt* |
| Charakterfilter | Tags (oder Negativ-Tags bei *exclude*), nach Charaktername und Tag |

**Schlüssel werden zu Filtern.** Ein einzelner einfacher Schlüssel wird zu einem Filter *Text* (`dragon`). Alles andere – mehrere Schlüssel, sekundäre Schlüssel (*AND ANY*, *AND ALL*, *NOT ANY*, *NOT ALL*), Schlüssel in der Form `/regex/`, Groß-/Kleinschreibung beachten oder ganze Wörter – wird zu einem einzigen Filter *Regex*, der dasselbe leistet. Die Schlüssel `dragon`, `wyrm` mit dem sekundären Schlüssel `fire` (*AND ANY*) ergeben zum Beispiel:

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

Du musst diese Ausdrücke nicht verstehen; ändere sie nur, wenn du reguläre Ausdrücke kennst.

<a id="what-works-differently"></a>

### Was anders funktioniert

SillyTavern-Einstellungen ohne Marginalia-Gegenstück werden in der **Notiz** des Eintrags aufgeführt, zum Beispiel *Aus SillyTavern importiert, nicht unterstützt: Wahrscheinlichkeit 50%, Einschlussgruppe „weather“.* Dazu gehören:

- Wahrscheinlichkeit, Einschlussgruppen, Sticky, Cooldown und Verzögerung – der Eintrag ist einfach aktiv, sobald er passt;
- Einfügung *in Tiefe* – der Eintrag steht stattdessen vor dem Benutzerprompt.

Der größte Unterschied ist, **womit die Schlüssel abgeglichen werden**. SillyTavern durchsucht den letzten Chat. Marginalia gleicht sie mit der [Anfrage für den Abschnitt, der gerade geschrieben wird](entries.md#filters) ab – seine Szene, Charaktere und Anweisungen. Ein importierter Eintrag beginnt genau damit (sein **Aktivierungskontext** ist *Anweisungen*). Ein Eintrag, den SillyTavern aktivierte, weil ein Name in den letzten Nachrichten vorkam, wird in Marginalia nur aktiviert, wenn du den Namen im Zug erwähnst. Um das Verhalten von SillyTavern nachzubilden, stelle den **Aktivierungskontext** des Eintrags auf *Anweisungen und Abschnitte der Geschichte* und unter **Abschnitte** die Anzahl der neuesten Abschnitte ein, die durchsucht werden sollen; siehe [In die Geschichte schauen](entries.md#looking-into-the-story). Rekursive Aktivierung (Einträge, die andere Einträge aktivieren) wird beim Import ebenfalls nicht gesetzt: Schalte sie mit der **Scan-Tiefe** des Lorebooks ein, siehe [Von anderen Einträgen eingebrachte Einträge](entries.md#entries-brought-in-by-other-entries).

Sieh dir die Einträge nach dem Import an und entscheide, welche immer aktiv sein sollen, welche ihre Filter behalten und welche besser mit [Tags](entries.md#tags) gesteuert werden.
