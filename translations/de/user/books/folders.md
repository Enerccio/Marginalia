---
label: Ordner
order: 98
---

# Ordner

Ordner sortieren deine Bücher so, wie Ordner auf einer Festplatte Dateien sortieren. Ein Ordner enthält Bücher und andere Ordner, beliebig tief verschachtelt, und die Bücherliste zeigt jeweils nur einen Ordner an. Ein Ordner kann außerdem **Einstellungen** enthalten (Sprache, Erzählperspektive, Zeitform, Stil, die Vorlagen und die Prompts): Die Bücher darin und in den darunterliegenden Ordnern schreiben mit diesen Einstellungen, sofern sie nicht eigene haben. Genau dafür verwendet man Ordner hauptsächlich: Du legst den Stil einer Reihe einmal fest, und jedes Buch der Reihe hat ihn.

![Der Reiter Bücher mit Ordnern](../../images/books-folders.png)

<a id="moving-around"></a>

## Navigation

Oberhalb der Bücher zeigt die Liste die Ordner des aktuellen Ordners, in der Reihenfolge der sortierten Spalten. Klicke auf den **Namen** eines Ordners, um ihn zu öffnen. Die Schaltflächenleiste über der Liste (`Books / Sci-fi / Expanse`) zeigt, wo du dich befindest; klicke auf einen Teil davon, um dorthin zurückzukehren.

Das Kontrollkästchen **Unterordner einbeziehen** listet jedes Buch unterhalb des aktuellen Ordners auf, in beliebiger Tiefe (auf oberster Ebene sind das alle deine Bücher), und fügt eine Spalte **Ordner** mit dem Ort jedes Buches hinzu. Der aktuelle Ordner und das Kontrollkästchen bleiben erhalten, auch wenn du die Seite neu lädst.

Eine Ordnerzeile zeigt an, wie viele Bücher darunter liegen, in beliebiger Tiefe. Eine Buchzeile zeigt die Wörter und Tokens seines [aktiven Zweigs](branches-and-story-tree.md). Die Filter **Name** und **Schlagwörter** wirken sowohl auf Ordner als auch auf Bücher (Ordner haben Schlagwörter, siehe [Ordner mit Schlagwörtern versehen](#tagging-folders)), im aktuellen Ordner oder darunter, wenn *Unterordner einbeziehen* aktiv ist.

Das Feld **Zuletzt geändert** eines Ordners folgt seinen Büchern: Wenn sich ein Buch darin oder in einem darunterliegenden Ordner ändert (oder einer seiner Abschnitte), wird auch der Ordner aktualisiert. Sortiere danach, um zu sehen, wo du zuletzt geschrieben hast. Das Öffnen eines Buches oder das Verschieben zählt nicht.

<a id="what-the-buttons-do"></a>

### Was die Schaltflächen tun

| Schaltfläche | |
|---|---|
| **Neuer Ordner** (Kopfzeile) | Legt einen Ordner im aktuellen Ordner an. Ein Name darf nicht leer sein, nicht länger als 255 Zeichen sein und kein `/` enthalten. Er darf nicht der Name eines anderen Ordners am selben Ort sein (Groß- und Kleinschreibung spielt keine Rolle). |
| das Zahnrad eines Ordners | Die [Einstellungen des Ordners](#folder-settings). |
| der Stift eines Ordners | Benennt ihn um. |
| der Papierkorb eines Ordners | Löscht ihn, siehe [Ordner löschen](#deleting-a-folder). |
| **In Ordner verschieben** (bei einem Buch) | Wählt den Ordner aus, in den das Buch verschoben werden soll. |
| der Stern eines Buches | Markiert es als [Favorit](#favorites). |
| **Buch hinzufügen**, die Importe | Das neue Buch kommt in den **aktuellen Ordner**. Ein [Klon](managing-books.md#cloning-a-book) wird neben sein Original gelegt. |

<a id="folder-settings"></a>

## Ordnereinstellungen

Das Zahnrad eines Ordners öffnet seine Einstellungen (das Fenster **Organisieren** hat dieselbe Schaltfläche, Taste `i`):

![Die Einstellungen eines Ordners](../../images/folder-settings.png)

- **Name**, **Beschreibung** und **Schlagwörter**.
- **Sprache** sowie dieselben Felder wie der Reiter [Prompts](prompts.md) eines Buches: Hauptvorlage, Erzählperspektive, Zeitform, Stil und die Prompts.
- **Aufbewahrte Versionen je Abschnitt**: wie viele frühere Versionen eines Abschnitts der [Verlauf](version-history.md#how-many-versions-are-kept) für die Bücher im Ordner und die darunterliegenden Ordner behält. Von allen oben genannten Grenzen eines Buches gilt die strengste.
- **Einstellungen der Erweiterung**, wenn eine geladene [Erweiterung](../extensions/index.md) Einstellungen hat, die Bücher erben, siehe [Einstellungen von Erweiterungen](prompts.md#settings-of-extensions).

Alles ist optional. Ein leeres Feld wird *geerbt*, und das wird angezeigt: Der graue Text im Feld ist der Wert, den es erbt, und die Zeile darunter sagt, woher er kommt. Drücke **Speichern**, um den Ordner zu sichern; Name und Beschreibung lassen sich jederzeit ändern, aber eine Änderung der Sprache oder einer Texteinstellung wird abgelehnt, solange ein Agent an einem Buch im Ordner oder darunter arbeitet: Die Meldung nennt das Buch, dessen Lauf du zuerst [anhalten oder beenden](../agents.md) musst.

<a id="where-the-settings-of-a-book-come-from"></a>

### Woher die Einstellungen eines Buches stammen

Ein Buch verwendet bei jeder dieser Einstellungen den ersten der folgenden Werte, der nicht leer ist:

1. das **Buch** selbst (seine Reiter *Info* und *Prompts*),
2. den **Ordner**, in dem das Buch liegt,
3. den Ordner darüber, und so weiter bis zur obersten Ebene,
4. deine **Benutzereinstellungen** (*Einstellungen → Vorlagen*, siehe [Konto & Einstellungen](../account.md)),
5. den **eingebauten Standard**.

Ein leerer Text gilt als nicht gesetzt. Das wird nachgeschlagen, wenn es gebraucht wird (wenn ein Abschnitt erzeugt, eine Zusammenfassung erstellt oder ein Abschnitt neu entworfen wird ...). Beim Einordnen eines Buches in einen Ordner wird nichts ins Buch kopiert: **Ein Buch in einen anderen Ordner zu verschieben, ändert, wie es geschrieben wird**, und eine Änderung der Einstellung eines Ordners ändert jedes Buch darunter, das nicht eigene Werte setzt.

Der Reiter [Prompts](prompts.md#inherited-settings) eines Buches zeigt das: Unter jedem Feld steht, woher sein Wert kommt (*Vom Ordner „Sci-fi“ geerbt*, *Aus deinen Einstellungen geerbt* oder *Eingebauter Standard*).

<a id="language"></a>

### Sprache

Das Feld **Sprache** eines Buches (der Reiter [Info](managing-books.md#the-about-tab)) und eines Ordners kann leer bleiben, was *erben* bedeutet: Das Buch übernimmt die Sprache des nächsten Ordners darüber, der eine setzt, andernfalls Englisch. Ein leeres Feld zeigt an, welche Sprache das ist und woher sie stammt. Das Feld hat eine Schaltfläche zum Leeren; wenn du es leerst, wird wieder geerbt (früher wurde daraus Englisch). Alles, was die Sprache eines Buches anzeigt oder verwendet, nutzt die Sprache, die es mit seinen Ordnern hat: die Silbentrennung des Textes, den Export und die Tabelle [Sprache massenhaft ändern](#mass-changes).

<a id="deleting-a-folder"></a>

## Ordner löschen

Die Papierkorb-Schaltfläche eines Ordners fragt nach, was geschehen soll:

- **Ein leerer Ordner** wird nach einer Bestätigung gelöscht;
- **Ein Ordner mit Büchern oder Ordnern darin** bietet **Den Inhalt in den übergeordneten Ordner verschieben** (der Standard) oder **Alles darin ebenfalls löschen** an. Die zweite Option fragt erneut, mit der Anzahl der Bücher und Ordner, und schickt die Bücher in den [Papierkorb](../trash.md), wo du sie wiederherstellen kannst. Der gelöschte Ordner selbst erscheint nicht im Papierkorb.

Wenn ein Ordner darin denselben Namen hat wie ein Ordner im übergeordneten Ordner, wird das Verschieben des Inhalts nach oben abgelehnt (Namen sind eindeutig): Benenne zuerst einen der beiden um. Ein Ordner mit einem Buch, an dem ein Agent arbeitet, kann nicht gelöscht oder verschoben werden, da sich sonst die Einstellungen dieses Buches während des Laufs ändern würden.
<a id="organise"></a>

## Organisieren

**Organisieren** (im Menü **Verwalten** der Kopfzeile der Liste) öffnet ein Fenster mit zwei Bereichen, in denen je ein Ordner angezeigt wird, um Bücher und Ordner von einem in den anderen zu verschieben und zu kopieren, ohne jeden einzeln zu öffnen. Es öffnet sich mit dem linken Bereich auf dem Ordner, in dem du dich gerade befandest, und dem rechten Bereich auf dem, den er zuletzt angezeigt hat.

![Das Fenster Organisieren](../../images/organise-dialog.png)

| Taste | |
|---|---|
| Links / Rechts / Tab | Der andere Bereich. |
| Oben / Unten | Der Cursor. |
| Enter | Öffnet den Ordner unter dem Cursor. |
| Rücktaste | Geht eine Ebene nach oben. |
| Leertaste oder Einfügen | Markiert den Eintrag unter dem Cursor; Umschalt + Oben / Unten markiert beim Bewegen. |
| Klick auf ein Kontrollkästchen | Markiert oder hebt die Markierung des Eintrags auf; **Umschalt + Klick** markiert (oder hebt die Markierung auf) alle Einträge zwischen ihm und dem zuvor angeklickten Kästchen. |

Eine Reihe von Schaltflächen am unteren Rand arbeitet mit dem, was **markiert** ist (oder, wenn nichts markiert ist, mit dem, was unter dem Cursor liegt), und zeigt ihre Taste in Klammern an:

| Schaltfläche | Taste | |
|---|---|---|
| **Kopieren** | `c` | Kopiert in den Ordner des *anderen* Bereichs. |
| **Verschieben** | `m` | Verschiebt in den Ordner des anderen Bereichs. |
| **Neuer Ordner** | `n` | Legt einen Ordner im Ordner des aktiven Bereichs an. |
| **Umbenennen** | `Shift+r` | Benennt den Ordner oder das Buch unter dem Cursor um. |
| **Löschen** | `Delete` | Löscht es, wie in der Liste. |
| **Ordnereinstellungen** | `i` | Die [Einstellungen](#folder-settings) des Ordners unter dem Cursor. |
| **Modell/Protokoll für Massenänderungen**, **Sprache für Massenänderungen**, **Massenumbenennung**, **Bücher taggen** | `p`, `l`, `Shift+n`, `t` | Die [Massenänderungen](#mass-changes) für die markierten Bücher oder für die Bücher des Ordners des aktiven Bereichs (mit eigenem *Unterordner einbeziehen*). Die Bereiche zeigen nach einer Massenumbenennung die neuen Namen an. |
| **Erweiterte Suche** | `f` | Die [Suche](managing-books.md#advanced-search) im Ordner des aktiven Bereichs, optional mit *Unterordner einbeziehen*. Ein Treffer hat **Im Ordner anzeigen**, das den Bereich dorthin bringt und das Buch markiert. |
| **Ordner in der Liste anzeigen** | `o` | Schließt das Fenster und öffnet den Ordner des aktiven Bereichs in der Liste der Bücher. |

Wenn du das Fenster schließt, wird die Liste der Bücher neu eingelesen, sodass sich zeigt, was du erstellt, verschoben, kopiert, umbenannt oder gelöscht hast, sofort. (Ist der Ordner, in dem die Liste war, nicht mehr vorhanden, landet sie in der obersten Ebene.)

Die Tasten sind Aktionen der [Tastenkürzel](../account.md#keyboard-shortcuts) in den *Einstellungen*: Ändere oder entferne sie dort (standardmäßig gibt es keine Funktionstasten, denn ein Mac und die Browser belegen sie). Die Schaltflächen zeigen die Taste an, die du eingestellt hast.

<a id="copying"></a>

### Kopieren

Eine **Kopie** eines Buches ist ein [Klon](managing-books.md#cloning-a-book); das Fenster fragt, ob nur der aktuelle Zweig übernommen werden soll. Eine Kopie eines Ordners ist ein neuer Ordner mit den Einstellungen, der Beschreibung und den Tags des Originals sowie Kopien aller Bücher und Ordner darin. Ein Kopiervorgang kann lange dauern, daher läuft er hinter einem Fortschrittsbalken, der nicht angehalten werden kann. Schlägt er auf halbem Weg fehl, bleiben die bis dahin erstellten Kopien erhalten, und zwar jede vollständig.

Ein **Verschieben** wird sofort ausgeführt und lässt sich durch Zurückverschieben rückgängig machen. Ein Ordner kann nicht in sich selbst oder in einen Ordner darunter verschoben werden.

<a id="when-a-name-is-taken"></a>

### Wenn ein Name bereits vergeben ist

Hat das Ziel bereits einen Ordner mit demselben Namen, fragt das Fenster, was mit diesem Ordner geschehen soll; für die übrigen gilt **Auf alle anwenden**:

| Auswahl | |
|---|---|
| **Umbenennen** | Der Ordner erhält einen anderen Namen. |
| **Überspringen** | Er bleibt, wo er ist (und wird nicht kopiert). |
| **Zusammenführen** | Was darin liegt, wird in den vorhandenen Ordner gelegt; gleichnamige Ordner darin werden nacheinander ebenfalls zusammengeführt. |

Bücher kollidieren nie: Ihre Namen dürfen sich wiederholen.

<a id="mass-changes"></a>

## Massenänderungen

Das Menü **Verwalten → Massenänderungen** der Liste (und die Schaltflächen von Organisieren) ändert viele Bücher auf einmal. **Modell/Protokoll für Massenänderungen** setzt ein Modell und/oder ein Protokoll für die Bücher, **Sprache für Massenänderungen** eine Sprache, **Massenumbenennung** fügt den Namen ein Präfix oder ein Suffix hinzu, ersetzt einen Text darin oder nummeriert sie, **Bücher taggen** und **Ordner taggen** fügen einen Tag hinzu oder entfernen ihn. Siehe [Bücher verwalten](managing-books.md#tagging-many-books-at-once). Aus der Liste heraus arbeiten sie mit den Büchern des aktuellen Ordners, mit eigenem **Unterordner einbeziehen**.

<a id="favorites"></a>

## Favoriten

Der Stern in einer Buchzeile macht es zu einem **Favoriten**. Solange du einen hast, zeigt die oberste Ebene den virtuellen Ordner **Favoriten** (mit einem Stern) mit den Favoritenbüchern aus all deinen Ordnern. Er ist nur eine Ansicht: Du kannst darin kein Buch und keinen Ordner anlegen, und das Entfernen des Sterns nimmt das Buch aus dieser Ansicht, nicht aus seinem Ordner. Favoriten werden in deinen Einstellungen gespeichert.

<a id="tagging-folders"></a>

## Ordner taggen

Ordner haben Tags, so wie Bücher sie haben. **Verwalten → Massenänderungen → Ordner taggen** versieht viele Ordner auf einmal mit Tags. Die Tags eines Ordners ändern die Tags seiner Bücher nicht, und sie sind nicht Teil einer [Sicherung](backups.md).

<a id="folders-and-other-features"></a>

## Ordner und andere Funktionen

- **Sicherungen.** Eine [Sicherung](backups.md#backups-and-folders) merkt sich die Ordner ihres Buches, und eine exportierte Datei kann die Einstellungen enthalten, die das Buch erbt.
- **Agenten.** Ein [Agent](../agents.md#agents-and-folders) schreibt mit den Einstellungen der Ordner seines Buches.
- **Hilfe-Assistent.** Wenn du es erlaubst, kann der [Assistent](../administration/help-assistant.md#your-books) deine Bücher und Ordner nachschlagen.
- **Telefon.** Das [Telefonlayout](../phone.md#books) zeigt dieselben Ordner als Karten und erstellt, benennt um, richtet ein und löscht sie; Organisieren und die Massenänderungen befinden sich im Menü **⋮** der Kopfzeile.
- **Betrachter.** Der [Betrachter](publishing.md#folders-in-the-list) listet die Ordner ebenfalls auf und öffnet sich in dem, den du zuletzt verlassen hast.
