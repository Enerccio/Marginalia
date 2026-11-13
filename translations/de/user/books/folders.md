---
label: Ordner
order: 98
---

# Ordner

Ordner sortieren deine Bücher so, wie Ordner auf einer Festplatte Dateien sortieren. Ein Ordner enthält Bücher und andere Ordner, so tief du willst,
und die Buchliste zeigt jeweils nur einen Ordner an. Ein Ordner kann außerdem **Einstellungen** enthalten (Sprache, Erzählperspektive, Zeitform, Stil,
die Vorlagen und die Prompts): Die Bücher darin und in den Ordnern darunter schreiben mit diesen Einstellungen, sofern sie keine eigenen haben.
Das ist der Hauptgrund, Ordner zu verwenden: Den Stil einer Reihe einmal festlegen, und jedes Buch der Reihe hat ihn.

![Der Reiter Bücher mit Ordnern](../../images/books-folders.png)

<a id="moving-around"></a>

## Navigieren

Über den Büchern zeigt die Liste die Ordner des aktuellen Ordners, in der Reihenfolge der sortierten Spalten. Klicke auf den **Namen** eines
Ordners, um ihn zu öffnen. Der Pfad aus Schaltflächen über der Liste (`Bücher / Sci-fi / Expanse`) zeigt, wo du dich befindest; klicke auf einen Teil davon,
um dorthin zurückzukehren.

Das Kontrollkästchen **Unterordner einbeziehen** listet jedes Buch unterhalb des aktuellen Ordners auf, in beliebiger Tiefe (auf der obersten Ebene sind das
alle deine Bücher), und fügt eine Spalte **Ordner** mit dem Ort jedes Buches hinzu. Der aktuelle Ordner und das Kontrollkästchen
werden gemerkt, auch wenn du die Seite neu lädst.

Eine Ordnerzeile zeigt an, wie viele Bücher darunter liegen, in beliebiger Tiefe. Eine Buchzeile zeigt die Wörter und Tokens seines
[aktiven Zweigs](branches-and-story-tree.md). Die Filter **Name** und **Tags** funktionieren sowohl für Ordner als auch für Bücher
(Ordner haben Tags, siehe [Ordner taggen](#tagging-folders)), im aktuellen Ordner oder darunter mit *Unterordner einbeziehen*.

Das **Zuletzt geändert** eines Ordners richtet sich nach seinen Büchern: Wenn sich ein Buch darin oder in einem Ordner darunter ändert (oder einer seiner
Abschnitte), wird auch der Ordner aktualisiert. Sortiere danach, um zu sehen, wo du zuletzt geschrieben hast. Das Öffnen eines Buches oder das Verschieben zählt nicht.

<a id="what-the-buttons-do"></a>

### Was die Schaltflächen tun

| Schaltfläche | |
|---|---|
| **Neuer Ordner** (Kopfzeile) | Erstellt einen Ordner im aktuellen Ordner. Ein Name darf nicht leer sein, nicht länger als 255 Zeichen sein oder ein `/` enthalten und darf nicht der Name eines anderen Ordners am selben Ort sein (Groß- und Kleinschreibung spielt keine Rolle). |
| das Zahnrad eines Ordners | Die [Einstellungen des Ordners](#folder-settings). |
| der Stift eines Ordners | Benennt ihn um. |
| der Papierkorb eines Ordners | Löscht ihn, siehe [Einen Ordner löschen](#deleting-a-folder). |
| **In Ordner verschieben** (ein Buch) | Wählt den Ordner, in den das Buch gelegt werden soll. |
| der Stern eines Buches | Macht es zu einem [Favoriten](#favorites). |
| **Buch hinzufügen**, die Importe | Das neue Buch landet im **aktuellen Ordner**. Ein [Klon](managing-books.md#cloning-a-book) wird neben sein Original gelegt. |

<a id="folder-settings"></a>

## Ordnereinstellungen

Das Zahnrad eines Ordners öffnet seine Einstellungen (das Fenster **Organisieren** hat dieselbe Schaltfläche, Taste `i`):

![Die Einstellungen eines Ordners](../../images/folder-settings.png)

- **Name**, **Beschreibung** und **Tags**.
- **Sprache** sowie dieselben Felder wie der Reiter [Prompts](prompts.md) eines Buches: Hauptvorlage, Erzählperspektive, Zeitform,
  Stil und die Prompts.
- **Aufbewahrte Versionen je Abschnitt**: Wie viele frühere Versionen eines Abschnitts der [Verlauf](version-history.md#how-many-versions-are-kept)
  für die Bücher im Ordner und die Ordner darunter behält. Von allen oben genannten Grenzen eines Buches gilt die strengste.
- **Einstellungen der Erweiterung**, wenn eine geladene [Erweiterung](../extensions/index.md) Einstellungen hat, die Bücher erben, siehe
  [Einstellungen von Erweiterungen](prompts.md#settings-of-extensions).

Alles ist optional. Ein leeres Feld wird *geerbt*, und das wird angezeigt: Der graue Text im Feld ist der Wert, den es erbt, und die Zeile darunter
sagt, woher er kommt. Drücke **Speichern**, um den Ordner zu speichern; Name und Beschreibung können jederzeit geändert werden, aber eine Änderung der Sprache oder einer Texteinstellung
wird abgelehnt, solange ein Agent an einem Buch im Ordner oder darunter arbeitet: Die Meldung nennt das Buch, [pausiere oder stoppe zuerst seinen Lauf](../agents.md).

<a id="where-the-settings-of-a-book-come-from"></a>

### Woher die Einstellungen eines Buches stammen

Ein Buch verwendet für jede dieser Einstellungen die erste davon, die nicht leer ist:

1. das **Buch** selbst (seine Reiter *Info* und *Prompts*),
2. der **Ordner**, in dem das Buch liegt,
3. der Ordner darüber, und so weiter bis zur obersten Ebene,
4. deine **Benutzereinstellungen** (*Einstellungen → Vorlagen*, siehe [Konto & Einstellungen](../account.md)),
5. der **eingebaute Standard**.

Ein leerer Text gilt als nicht gesetzt. Das wird nachgeschlagen, wenn es benötigt wird (wenn ein Abschnitt erzeugt, eine Zusammenfassung erstellt, ein Abschnitt neu entworfen wird ...), und beim Verschieben in einen Ordner wird nichts ins Buch kopiert: **Ein Buch in einen anderen Ordner zu verschieben, ändert, wie es schreibt**, und eine Änderung einer Ordnereinstellung ändert jedes Buch darunter, das keine eigene setzt.

Der Reiter [Prompts](prompts.md#inherited-settings) eines Buches zeigt es: Unter jedem Feld steht, woher sein Wert stammt (*Vom Ordner „Sci-fi" geerbt*, *Aus deinen Einstellungen geerbt* oder *Eingebauter Standard*).

<a id="language"></a>

### Sprache

Das Feld **Sprache** eines Buches (der Reiter [Info](managing-books.md#the-about-tab)) und eines Ordners kann leer bleiben,
was *erben* bedeutet: Das Buch übernimmt die Sprache des nächsten Ordners darüber, der eine setzt, sonst Englisch. Ein leeres
Feld zeigt, welche Sprache das ist und woher sie kommt. Das Feld hat eine Schaltfläche zum Leeren; Leeren erbt wieder (früher wurde daraus Englisch).
Alles, was die Sprache eines Buches anzeigt oder verwendet, nutzt die, die es mit seinen Ordnern hat: die
Silbentrennung des Textes, den Export und die Tabelle [Massenänderung der Sprache](#mass-changes).

<a id="deleting-a-folder"></a>

## Einen Ordner löschen

Die Papierkorb-Schaltfläche eines Ordners fragt, was geschehen soll:

- **ein leerer Ordner** wird nach einer Bestätigung gelöscht;
- **ein Ordner mit Büchern oder Ordnern darin** bietet **Den Inhalt in den übergeordneten Ordner verschieben** (der Standard) oder **Alles darin ebenfalls löschen**. Die zweite Option fragt erneut, mit der Anzahl der Bücher und Ordner, und schickt die Bücher in den [Papierkorb](../trash.md), wo du sie wiederherstellen kannst. Der gelöschte Ordner selbst wird nicht im Papierkorb aufgeführt.

Wenn ein Ordner darin den Namen eines Ordners im übergeordneten Ordner trägt, wird das Hochverschieben des Inhalts abgelehnt (Namen sind eindeutig): Benenne zuerst einen von beiden um. Ein Ordner mit einem Buch, an dem ein Agent arbeitet, kann nicht gelöscht oder verschoben werden, da sich die Einstellungen dieses Buches sonst während des Laufs ändern würden.
<a id="organise"></a>

## Organisieren

**Organisieren** (im Menü **Verwalten** in der Kopfzeile der Liste) öffnet ein Fenster mit zwei Bereichen, in denen jeweils ein Ordner angezeigt wird. Damit lassen sich Bücher und Ordner ohne Öffnen von einem Bereich in den anderen verschieben und kopieren. Es öffnet sich mit dem linken Bereich im Ordner, in dem Sie sich befanden, und dem rechten Bereich im Ordner, der beim letzten Mal angezeigt wurde.

![Das Fenster Organisieren](../../images/organise-dialog.png)

| Taste | |
|---|---|
| Links / Rechts / Tab | Der andere Bereich. |
| Hoch / Runter | Der Cursor. |
| Enter | Öffnet den Ordner unter dem Cursor. |
| Rücktaste | Geht nach oben. |
| Leertaste oder Einfg | Markiert den Eintrag unter dem Cursor; Umschalt + Hoch / Runter markiert beim Bewegen. |
| Klick auf ein Kontrollkästchen | Markiert oder hebt die Markierung des Eintrags auf; **Umschalt + Klick** markiert (oder hebt die Markierung auf) alle Einträge zwischen ihm und dem zuvor angeklickten Kästchen. |

Eine Reihe von Schaltflächen am unteren Rand arbeitet mit dem, was **markiert** ist (oder, falls nichts markiert ist, mit dem Eintrag unter dem Cursor), und zeigt ihr Tastenkürzel in Klammern:

| Schaltfläche | Taste | |
|---|---|---|
| **Kopieren** | `c` | Kopiert in den Ordner des *anderen* Bereichs. |
| **Verschieben** | `m` | Verschiebt in den Ordner des anderen Bereichs. |
| **Neuer Ordner** | `n` | Legt einen Ordner im Ordner des aktiven Bereichs an. |
| **Umbenennen** | `Shift+r` | Benennt den Ordner oder das Buch unter dem Cursor um. |
| **Löschen** | `Delete` | Löscht es, wie in der Liste. |
| **Ordnereinstellungen** | `i` | Die [Einstellungen](#folder-settings) des Ordners unter dem Cursor. |
| **Modell/Protokoll für Massenänderungen**, **Sprache für Massenänderungen**, **Massenumbenennung**, **Bücher taggen** | `p`, `l`, `Shift+n`, `t` | Die [Massenänderungen](#mass-changes) für die markierten Bücher oder für die Bücher des Ordners des aktiven Bereichs (mit ihrer eigenen Option *Unterordner einbeziehen*). Die Bereiche zeigen nach einer Massenumbenennung die neuen Namen an. |
| **Erweiterte Suche** | `f` | Die [Suche](managing-books.md#advanced-search) im Ordner des aktiven Bereichs, optional mit *Unterordner einbeziehen*. Ein Ergebnis hat **Im Ordner anzeigen**, das den Bereich dorthin bringt und das Buch markiert. |
| **Ordner in der Liste anzeigen** | `o` | Schließt das Fenster und öffnet den Ordner des aktiven Bereichs in der Liste der Bücher. |

Wenn Sie das Fenster schließen, wird die Liste der Bücher neu eingelesen, sodass alles, was Sie erstellt, verschoben, kopiert, umbenannt oder gelöscht haben, sofort sichtbar ist (wenn der Ordner, in dem sich die Liste befand, nicht mehr existiert, wird die oberste Ebene angezeigt).

Die Tasten sind Aktionen der [Tastenkürzel](../account.md#keyboard-shortcuts) in den *Einstellungen*: Ändern oder leeren Sie sie dort (standardmäßig gibt es keine Funktionstasten, denn Mac und Browser belegen sie). Die Schaltflächen zeigen die von Ihnen festgelegte Taste an.

<a id="copying"></a>

### Kopieren

Eine **Kopie** eines Buches ist ein [Klon](managing-books.md#cloning-a-book); das Fenster fragt, ob nur der aktuelle Zweig übernommen werden soll. Eine Kopie eines Ordners ist ein neuer Ordner mit den Einstellungen, der Beschreibung und den Tags des Originals sowie Kopien aller darin enthaltenen Bücher und Ordner. Ein Kopiervorgang kann lange dauern, daher läuft er hinter einer Fortschrittsanzeige, die nicht angehalten werden kann. Schlägt er auf halbem Weg fehl, bleiben die bis dahin erstellten Kopien erhalten, und zwar jede vollständig.

Ein **Verschieben** wird sofort ausgeführt und lässt sich durch Zurückverschieben rückgängig machen. Ein Ordner kann nicht in sich selbst oder in einen Ordner darunter verschoben werden.

<a id="when-a-name-is-taken"></a>

### Wenn ein Name bereits vergeben ist

Hat das Ziel bereits einen Ordner mit demselben Namen, fragt das Fenster, was mit diesem Ordner geschehen soll; für die übrigen Fälle gibt es **Auf alle anwenden**:

| Auswahl | |
|---|---|
| **Umbenennen** | Der Ordner erhält einen anderen Namen. |
| **Überspringen** | Er bleibt, wo er ist (und wird nicht kopiert). |
| **Zusammenführen** | Was darin liegt, wird in den vorhandenen Ordner übernommen; gleichnamige Ordner darin werden ihrerseits zusammengeführt. |

Bücher können nie kollidieren: Ihre Namen dürfen sich wiederholen.

<a id="mass-changes"></a>

## Massenänderungen

Das Menü **Verwalten → Massenänderungen** der Liste (sowie die Schaltflächen von Organisieren) ändert viele Bücher auf einmal. **Modell/Protokoll für Massenänderungen** setzt ein Modell und/oder ein Protokoll für die Bücher, **Sprache für Massenänderungen** eine Sprache, **Massenumbenennung** fügt ihren Namen ein Präfix oder Suffix hinzu, ersetzt einen Text darin oder nummeriert sie, **Bücher taggen** und **Ordner taggen** fügen einen Tag hinzu oder entfernen ihn. Siehe [Bücher verwalten](managing-books.md#tagging-many-books-at-once). Aus der Liste heraus arbeiten sie mit den Büchern des aktuellen Ordners, mit ihrer eigenen Option **Unterordner einbeziehen**.

<a id="favorites"></a>

## Favoriten

Der Stern in der Zeile eines Buches macht es zu einem **Favoriten**. Solange Sie einen haben, zeigt die oberste Ebene den virtuellen Ordner **Favoriten** (mit einem Stern) mit den Favoriten-Büchern aus allen Ihren Ordnern. Er ist nur eine Ansicht: Sie können darin kein Buch und keinen Ordner anlegen, und wenn Sie den Stern entfernen, wird das Buch nur daraus entfernt, nicht aus seinem Ordner. Favoriten werden in Ihren Einstellungen gespeichert.

<a id="tagging-folders"></a>

## Ordner taggen

Ordner haben Tags wie Bücher. **Verwalten → Massenänderungen → Ordner taggen** versieht viele Ordner auf einmal mit Tags. Die Tags eines Ordners ändern nicht die Tags seiner Bücher, und sie sind nicht Teil eines [Backups](backups.md).

<a id="folders-and-other-features"></a>

## Ordner und andere Funktionen

- **Backups.** Ein [Backup](backups.md#backups-and-folders) merkt sich die Ordner seines Buches, und eine exportierte Datei kann die Einstellungen enthalten, die das Buch erbt.
- **Agenten.** Ein [Agent](../agents.md#agents-and-folders) schreibt mit den Einstellungen der Ordner seines Buches.
- **Hilfe-Assistent.** Wenn Sie es erlauben, kann der [Assistent](../administration/help-assistant.md#your-books) Ihre Bücher und Ordner nachschlagen.
- **Telefon.** Die [Telefonansicht](../phone.md#books) zeigt dieselben Ordner als Karten und erstellt, benennt um, richtet ein und löscht sie; Organisieren und die Massenänderungen finden Sie im Menü **⋮** der Kopfzeile.
- **Viewer.** Der [Viewer](publishing.md#folders-in-the-list) listet die Ordner ebenfalls auf und öffnet sich in dem Ordner, den Sie verlassen haben.
