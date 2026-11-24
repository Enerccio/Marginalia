---
label: Ordner
order: 98
---

# Ordner

Ordner sortieren deine Bücher so, wie Ordner auf einer Festplatte Dateien sortieren. Ein Ordner enthält Bücher und
andere Ordner, beliebig tief verschachtelt, und die Bücherliste zeigt jeweils nur einen Ordner an. Ein Ordner kann auch
**Einstellungen** enthalten (Sprache, Erzählperspektive, Zeitform, Stil, die Vorlagen und die Prompts). Die Bücher darin
und in den darunterliegenden Ordnern schreiben mit diesen Einstellungen, sofern sie nicht eigene haben. Das ist der
Hauptgrund, Ordner zu verwenden: Lege den Stil einer Reihe einmal fest, dann hat jedes Buch der Reihe diesen Stil.

![Der Tab Books mit Ordnern](../../images/books-folders.png)

<a id="moving-around"></a>

## Navigation

Über den Büchern zeigt die Liste die Ordner des aktuellen Ordners, in der Reihenfolge der sortierten Spalten. Klicke auf den **Namen** eines
Ordners, um ihn zu öffnen. Die Schaltflächenleiste über der Liste (`Books / Sci-fi / Expanse`) zeigt, wo du dich gerade befindest. Klicke auf einen Teil davon,
um dorthin zurückzukehren.

Das Kontrollkästchen **Include subfolders** listet jedes Buch unterhalb des aktuellen Ordners auf, in beliebiger Tiefe (auf der obersten Ebene sind das
alle deine Bücher). Außerdem fügt es eine Spalte **Folder** mit dem Ort jedes Buches hinzu. Der aktuelle Ordner und das Kontrollkästchen
werden gemerkt, auch wenn du die Seite neu lädst.

Eine Ordnerzeile zeigt, wie viele Bücher darunter liegen, in beliebiger Tiefe. Eine Buchzeile zeigt die Wörter und Tokens seines
[aktiven Zweigs](branches-and-story-tree.md). Die Filter **Name** und **Tags** funktionieren sowohl für Ordner als auch für Bücher
(Ordner haben Tags, siehe [Tags für Ordner vergeben](#tagging-folders)), und zwar im aktuellen Ordner oder darunter, wenn *Include subfolders* aktiv ist.

Das **Last Modified** eines Ordners richtet sich nach seinen Büchern: Wenn sich ein Buch darin oder in einem darunterliegenden Ordner ändert (oder einer seiner
Teile), wird auch der Ordner als geändert markiert. Sortiere danach, um zu sehen, wo du zuletzt geschrieben hast. Ein Buch zu öffnen oder zu verschieben zählt nicht.

<a id="what-the-buttons-do"></a>

### Was die Schaltflächen tun

| Schaltfläche | |
|---|---|
| **New folder** (Kopfzeile) | Legt einen Ordner im aktuellen Ordner an. Der Name darf nicht leer sein, nicht länger als 255 Zeichen sein und kein `/` enthalten. Er darf auch nicht der Name eines anderen Ordners am selben Ort sein (Groß- und Kleinschreibung spielt keine Rolle). |
| das Zahnrad eines Ordners | Die [Einstellungen des Ordners](#folder-settings). |
| der Stift eines Ordners | Benennt ihn um. |
| der Papierkorb eines Ordners | Löscht ihn, siehe [Einen Ordner löschen](#deleting-a-folder). |
| **Move to folder** (bei einem Buch) | Wählt den Ordner aus, in den das Buch verschoben werden soll. |
| der Stern eines Buches | Markiert es als [Favorit](#favorites). |
| **Add Book** und die Importe | Das neue Buch landet im **aktuellen Ordner**. Ein [Klon](managing-books.md#cloning-a-book) wird neben sein Original gesetzt. |

<a id="folder-settings"></a>

## Ordnereinstellungen

Das Zahnrad eines Ordners öffnet seine Einstellungen (das Fenster **Organise** hat dieselbe Schaltfläche, Taste `i`):

![Die Einstellungen eines Ordners](../../images/folder-settings.png)

- **Name**, **Description** und **Tags**.
- **Language** sowie dieselben Felder wie im Tab [Prompts](prompts.md) eines Buches: Hauptvorlage, Erzählperspektive, Zeitform,
  Stil und die Prompts.
- **Versions kept per part**: wie viele frühere Fassungen eines Teils die [Historie](version-history.md#how-many-versions-are-kept)
  für die Bücher im Ordner und in den darunterliegenden Ordnern behält. Von allen oben genannten Grenzen gilt für ein Buch die strengste.
- **Extension Settings**, sofern eine geladene [Erweiterung](../extensions/index.md) Einstellungen hat, die Bücher übernehmen. Siehe
  [Einstellungen von Erweiterungen](prompts.md#settings-of-extensions).
- **Phrase watch list**: die Formulierungen, auf die die Bücher im Ordner geprüft werden. Sie werden zu denen deiner Einstellungen und der
  übergeordneten Ordner hinzugefügt, siehe [Auf Formulierungen achten](annotations.md#watching-for-phrases).

Alles ist optional. Ein leeres Feld wird *geerbt*, und das wird angezeigt: Der graue Text im Feld ist der Wert, den es erbt, und die Zeile darunter
sagt, woher er stammt. Drücke **Save**, um den Ordner zu speichern. Name und Beschreibung lassen sich jederzeit ändern. Eine Änderung der Sprache oder einer Texteinstellung
wird aber abgelehnt, solange ein Agent an einem Buch im Ordner oder darunter arbeitet. Die Meldung nennt das Buch. [Pausiere oder stoppe dessen Lauf](../agents.md), bevor du die Änderung vornimmst.

<a id="where-the-settings-of-a-book-come-from"></a>

### Woher die Einstellungen eines Buches kommen

Ein Buch verwendet für jede dieser Einstellungen den ersten Wert, der nicht leer ist, und zwar in dieser Reihenfolge:

1. das **Buch** selbst (seine Tabs *About* und *Prompts*),
2. der **Ordner**, in dem das Buch liegt,
3. der Ordner darüber, und so weiter bis nach oben,
4. deine **Benutzereinstellungen** (*Settings → Templates*, siehe [Konto und Einstellungen](../account.md)),
5. der **eingebaute Standardwert**.

Ein leerer Text gilt als nicht gesetzt. Das wird erst nachgeschlagen, wenn es gebraucht wird (wenn ein Teil erzeugt, eine Zusammenfassung erstellt oder ein Teil neu geschrieben wird ...). Beim Einordnen in einen Ordner wird nichts ins Buch kopiert: **Ein Buch in einen anderen Ordner zu verschieben, ändert, wie es schreibt**, und das Ändern einer Einstellung eines Ordners ändert jedes darunterliegende Buch, das keine eigene Einstellung hat.

Der Tab [Prompts](prompts.md#inherited-settings) eines Buches zeigt das an: Unter jedem Feld steht, woher der Wert kommt
(*Inherited from the folder "Sci-fi"*, *Inherited from your settings* oder *Built-in default*).

<a id="language"></a>

### Sprache

Das Feld **Language** eines Buches (im Tab [About](managing-books.md#the-about-tab)) und eines Ordners kann leer bleiben.
Das bedeutet *erben*: Das Buch übernimmt die Sprache des nächsten Ordners darüber, der eine setzt, andernfalls Englisch. Ein leeres
Feld zeigt, welche Sprache das ist und woher sie stammt. Das Feld hat eine Schaltfläche zum Leeren. Wenn du es leerst, wird wieder geerbt (früher wurde daraus Englisch). Alles, was die Sprache eines Buches anzeigt oder verwendet, nutzt die Sprache, die es über seine Ordner erhält: die Silbentrennung des Textes, den Export und die Tabelle [Mass Change Language](#mass-changes).

<a id="deleting-a-folder"></a>

## Einen Ordner löschen

Die Papierkorb-Schaltfläche eines Ordners fragt, was geschehen soll:

- **ein leerer Ordner** wird nach einer Bestätigung gelöscht;
- **ein Ordner mit Büchern oder Ordnern darin** bietet **Move its content to the folder above** (die Voreinstellung) oder **Delete everything in it as well** an. Die zweite Option fragt noch einmal nach und zeigt die Anzahl der Bücher und Ordner. Sie verschiebt die Bücher in den [Trash](../trash.md), von wo aus du sie wiederherstellen kannst. Der gelöschte Ordner selbst erscheint nicht im Trash.

Trägt ein Ordner darin den Namen eines Ordners im Ordner darüber, wird das Hochverschieben des Inhalts abgelehnt, weil Namen eindeutig sein müssen. Benenne in diesem Fall zuerst einen der beiden um. Ein Ordner mit einem Buch, an dem ein Agent arbeitet, kann nicht gelöscht oder verschoben werden, da sich die Einstellungen dieses Buches sonst während des Laufs ändern würden.
<a id="organise"></a>

## Organisieren

**Organise** (im Menü **Manage** der Kopfzeile der Liste) öffnet ein Fenster mit zwei Bereichen, die jeweils einen Ordner anzeigen. Damit verschieben und kopieren Sie Bücher und Ordner von einem Bereich in den anderen, ohne jeden einzeln zu öffnen. Das Fenster öffnet sich mit dem linken Bereich im Ordner, in dem Sie gerade waren, und dem rechten Bereich im Ordner, den er zuletzt angezeigt hat.

![Das Fenster Organise](../../images/organise-dialog.png)

| Taste | |
|---|---|
| Left / Right / Tab | Der andere Bereich. |
| Up / Down | Der Cursor. |
| Enter | Öffnet den Ordner unter dem Cursor. |
| Backspace | Geht eine Ebene nach oben. |
| Space oder Insert | Markiert den Eintrag unter dem Cursor; Shift + Up / Down markiert beim Bewegen. |
| Klick auf ein Kontrollkästchen | Markiert oder demarkiert den Eintrag; **Shift + Klick** markiert (oder demarkiert) alle Einträge zwischen ihm und dem zuvor angeklickten Kästchen. |

Eine Reihe von Schaltflächen unten führt die Aktion auf dem aus, was **markiert** ist (oder, falls nichts markiert ist, auf dem Eintrag unter dem Cursor). Die Taste steht jeweils in Klammern:

| Schaltfläche | Taste | |
|---|---|---|
| **Copy** | `c` | Kopiert in den Ordner des *anderen* Bereichs. |
| **Move** | `m` | Verschiebt in den Ordner des anderen Bereichs. |
| **New folder** | `n` | Legt einen Ordner im Ordner des aktiven Bereichs an. |
| **Rename** | `Shift+r` | Benennt den Ordner oder das Buch unter dem Cursor um. |
| **Delete** | `Delete` | Löscht es, wie in der Liste. |
| **Folder settings** | `i` | Die [Einstellungen](#folder-settings) des Ordners unter dem Cursor. |
| **Mass Change Model/Protocol**, **Mass Change Language**, **Mass Rename**, **Tag books** | `p`, `l`, `Shift+n`, `t` | Die [Massenänderungen](#mass-changes) für die markierten Bücher oder für die Bücher des Ordners im aktiven Bereich (mit eigenem *Include subfolders*). Nach einem Massenumbenennen zeigen die Bereiche die neuen Namen. |
| **Advanced search** | `f` | Die [Suche](managing-books.md#advanced-search) im Ordner des aktiven Bereichs, optional mit *Include subfolders*. Ein Treffer hat **Show in folder**, das den Bereich dorthin bringt und das Buch markiert. |
| **Show folder in the list** | `o` | Schließt das Fenster und öffnet den Ordner des aktiven Bereichs in der Liste der Bücher. |

Wenn Sie das Fenster schließen, wird die Liste der Bücher neu eingelesen. Was Sie erstellt, verschoben, kopiert, umbenannt oder gelöscht haben, ist damit sofort sichtbar. (Ist der Ordner, in dem die Liste war, nicht mehr vorhanden, geht sie auf die oberste Ebene.)

Die Tasten sind Aktionen der [Tastenkürzel](../account.md#keyboard-shortcuts) in *Settings*. Ändern oder leeren Sie sie dort. Standardmäßig gibt es keine Funktionstasten, weil ein Mac und die Browser sie belegen. Die Schaltflächen zeigen die Taste, die Sie eingestellt haben.

<a id="copying"></a>

### Kopieren

Eine **Kopie** eines Buches ist ein [Klon](managing-books.md#cloning-a-book). Das Fenster fragt, ob nur der aktuelle Zweig übernommen werden soll.

Eine Kopie eines Ordners ist ein neuer Ordner mit den Einstellungen, der Beschreibung und den Tags des Originals sowie Kopien aller Bücher und Ordner darin. Das Kopieren kann lange dauern. Es läuft hinter einer Fortschrittsanzeige, die sich nicht anhalten lässt. Bricht es auf halbem Weg ab, bleiben die bis dahin erstellten Kopien erhalten, und jede davon ist vollständig.

Ein **Verschieben** erfolgt sofort und lässt sich durch Zurückverschieben rückgängig machen. Ein Ordner kann nicht in sich selbst oder in einen Ordner darunter verschoben werden.

<a id="when-a-name-is-taken"></a>

### Wenn ein Name belegt ist

Hat das Ziel bereits einen Ordner gleichen Namens, fragt das Fenster, was mit diesem Ordner geschehen soll. Mit **Apply to all** gilt die Wahl für die übrigen:

| Auswahl | |
|---|---|
| **Rename** | Der Ordner erhält einen anderen Namen. |
| **Skip** | Er bleibt, wo er ist (und wird nicht kopiert). |
| **Merge** | Was darin liegt, wird in den vorhandenen Ordner gelegt. Gleichnamige Ordner darin werden wiederum zusammengeführt. |

Bücher kollidieren nie, ihre Namen dürfen sich wiederholen.

<a id="mass-changes"></a>

## Massenänderungen

Das Menü **Manage → Mass changes** der Liste (und die Schaltflächen von Organise) ändert viele Bücher auf einmal. **Mass Change Model/Protocol** setzt ein Modell und/oder ein Protokoll für die Bücher, **Mass Change Language** eine Sprache. **Mass Rename** fügt den Namen ein Präfix oder Suffix an, ersetzt darin einen Text oder nummeriert sie. **Tag books** und **Tag folders** fügen ein Tag hinzu oder entfernen eines. Siehe [Bücher verwalten](managing-books.md#tagging-many-books-at-once). Aus der Liste heraus wirken sie auf die Bücher des aktuellen Ordners, mit eigenem **Include subfolders**.

<a id="favorites"></a>

## Favoriten

Der Stern in der Zeile eines Buches macht es zu einem **Favoriten**. Solange Sie einen Favoriten haben, zeigt die oberste Ebene den virtuellen Ordner **Favorites** (mit einem Stern) mit den Favoritenbüchern aller Ihrer Ordner. Er ist nur eine Ansicht: Darin können Sie kein Buch und keinen Ordner anlegen. Entfernen Sie den Stern, wird das Buch aus dieser Ansicht entfernt, nicht aber aus seinem Ordner. Favoriten werden in Ihren Einstellungen gespeichert.

<a id="tagging-folders"></a>

## Tags für Ordner

Ordner haben Tags wie Bücher. **Manage → Mass changes → Tag folders** versieht viele Ordner auf einmal mit Tags. Die Tags eines Ordners ändern die Tags seiner Bücher nicht, und sie gehören nicht zu einem [Backup](backups.md).

<a id="folders-and-other-features"></a>

## Ordner und andere Funktionen

- **Backups.** Ein [Backup](backups.md#backups-and-folders) merkt sich die Ordner seines Buches. Eine exportierte Datei kann außerdem die Einstellungen enthalten, die das Buch erbt.
- **Agenten.** Ein [Agent](../agents.md#agents-and-folders) schreibt mit den Einstellungen der Ordner seines Buches.
- **Hilfe-Assistent.** Wenn Sie es erlauben, kann der [Assistent](../administration/help-assistant.md#your-books) Ihre Bücher und Ordner nachschlagen.
- **Telefon.** Das [Telefonlayout](../phone.md#books) zeigt dieselben Ordner als Karten und legt sie an, benennt sie um, richtet sie ein und löscht sie. Organise und die Massenänderungen befinden sich im Menü **⋮** seiner Kopfzeile.
- **Viewer.** Der [Viewer](publishing.md#folders-in-the-list) listet ebenfalls die Ordner auf und öffnet sich in dem Ordner, den Sie zuletzt verlassen haben.
