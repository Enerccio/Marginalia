---
label: Marginalia auf dem Smartphone verwenden
order: 985
icon: device-mobile
---

# Marginalia auf dem Smartphone verwenden

Marginalia hat eine Oberfläche, die für Smartphones gemacht ist. Du schreibst, bearbeitest und generierst Teile neu, bearbeitest Lorebooks, verwaltest deine Modelle und
(als Administrator) die Installation über den Smartphone-Browser. Die Smartphone-Oberfläche ist **keine Kopie der Desktop-Oberfläche**: dieselben Dinge sind in einer
Spalte angeordnet, einige Fenster sind einfacher, und nicht jede Erweiterung funktioniert darin. Diese Seite listet die Unterschiede auf.

Zum Lesen eines Buchs auf dem Smartphone gibt es außerdem den [Viewer](books/publishing.md), der dafür gemacht ist.

<a id="when-the-phone-layout-is-used"></a>

## Wann die Smartphone-Oberfläche verwendet wird

Marginalia prüft die Breite des Browserfensters **beim Laden der Seite**. Schmaler als 768 Pixel ergibt die Smartphone-Oberfläche, alles Breitere den normalen [Arbeitsbereich](index.md#the-workspace).

- Tablets und Smartphones im Querformat sind meist breiter als 768 Pixel und erhalten die Desktop-Oberfläche.
- Die Oberfläche wird **während der Arbeit nicht umgeschaltet**. Wenn du das Smartphone oder Tablet zur Seite drehst oder das Fenster in der Größe änderst, bleibt die Oberfläche so, wie sie war, bis du die **Seite neu lädst**. Um die größere Desktop-Oberfläche auf einem Tablet oder einem gedrehten Smartphone zu erhalten, drehe das Gerät und lade neu; um zur Smartphone-Oberfläche zurückzukehren, drehe es zurück und lade erneut.
- Ein schmales Desktop-Browserfenster erhält ebenfalls die Smartphone-Oberfläche, was praktisch ist, um sie auszuprobieren.

Wie der Desktop lässt sich auch die Smartphone-Oberfläche wie eine App auf dem Startbildschirm ablegen (*Install app* / *Add to Home Screen* im Menü des Browsers).

<a id="the-workspace"></a>

## Der Arbeitsbereich

Die Reiter links werden durch eine **Schublade** ersetzt. Tippe auf die Menütaste in der oberen Leiste, um sie zu öffnen, und tippe auf einen Reiter, um zu ihm zu wechseln. Der aktive Reiter füllt den ganzen Bildschirm, und sein Name wird in der oberen Leiste angezeigt.

![Der Smartphone-Arbeitsbereich mit geöffneter Schublade](../images/phone-workspace-drawer.png)

Die Schublade hat dieselben Reiter wie der Desktop – *Bücher*, *Lorebooks*, *Einstellungen*, *Protokolle*, *Inferenzanbieter*, *Agenten* (nur für Benutzer, die [Agenten](agents.md#on-a-phone) verwenden dürfen), *Ressourcen* und *Papierkorb* – und für einen Administrator zusätzlich *Administration* (der Knopf des Desktops ist hier ein Reiter). Unten stehen die Auswahl der **Sprache**, **Passwort ändern** und **Abmelden**. Erweiterungen können Schaltflächen hinzufügen, aber nur an den Stellen, die sie auf dem Smartphone kennen, siehe [Erweiterungen](#extensions).

Solange der Reiter *Einstellungen* ungespeicherte Änderungen hat, ist die Schublade wie auf dem Desktop gesperrt: zuerst speichern oder verwerfen.

<a id="books"></a>

### Bücher

Die Buchliste ist einspaltig. Darüber stehen der Namensfilter, der Tag-Filter und die Sortierknöpfe (*Zuletzt geändert*, *Erstellt*, *Name*; tippe auf den aktiven Knopf erneut, um die Reihenfolge umzukehren), dann **Buch hinzufügen**, die Lupe (die [erweiterte Suche](books/managing-books.md#advanced-search): Tags ein- und ausschließen, Daten, Anzahl der Teile und Text) und Aktualisieren. Jedes Buch hat Schaltflächen: der Stern macht es zum Favoriten, **In Ordner verschieben** legt es in einen [Ordner](books/folders.md), der Stift öffnet es, und der Papierkorb löscht es (in den [Papierkorb](trash.md)).

![Die Buchliste auf dem Smartphone](../images/phone-book-list.png)

Die Liste zeigt jeweils einen [Ordner](books/folders.md), wie am Desktop: den Pfad des aktuellen Ordners als Schaltflächen, um zurückzugehen, das Kontrollkästchen **Unterordner einbeziehen**, dann die Ordner über den Büchern (tippe auf einen, um hineinzugehen; angezeigt wird, wie viele Bücher darunter liegen) und auf der obersten Ebene den virtuellen Ordner **Favoriten**. Der Namens- und der Tag-Filter durchsuchen den aktuellen Ordner oder, mit *Unterordner einbeziehen*, alles darunter. **Buch hinzufügen** und die Importe legen das neue Buch in den Ordner, in dem du dich gerade befindest. Der aktuelle Ordner wird gemerkt.

![Ordner auf dem Smartphone](../images/phone-folders.png)

![Das Menü der Buchliste](../images/phone-books-menu.png)

Ein Ordner wird mit der Schaltfläche **Neuer Ordner** in der Kopfzeile angelegt und über die **⋮**-Taste seiner Zeile umbenannt, eingerichtet (Beschreibung, Tags, Sprache, Modell und Protokoll für seine Bücher) oder gelöscht. Die **⋮**-Taste in der Kopfzeile enthält die Fenster, die mit vielen Büchern arbeiten: **Organisieren** (die beiden Bereiche der Ordner liegen auf dem Smartphone übereinander, es gibt keinen Tastaturhinweis), **Bücher taggen**, **Ordner taggen** und die Massenänderungen der [Ordner](books/folders.md) (Modell und Protokoll, Sprache, umbenennen). Die Sprache eines Buchs kann hier ebenfalls von einem Ordner geerbt werden, und der Reiter *Prompts* zeigt, woher jeder Wert stammt.

### Lorebooks

Das Smartphone hat den **gesamten Lorebook-Editor** des Desktops, einspaltig angeordnet. Das Lorebook wird in einem Feld gewählt; die **⋮**-Taste daneben öffnet das Menü des Lorebooks: **Lorebook hinzufügen**, **Klonen**, **Löschen**, **Importieren** (eine Lorebook-Datei, eine SillyTavern-Datei, ein Zip-Archiv mit Markdown-Dateien, ein Lorebook, das ein anderer Benutzer teilt), **Exportieren** (das Lorebook oder im SillyTavern-Format), **Lorebooks taggen** und **Beschreibung bearbeiten**. Unter dem Kontrollkästchen *Aktiviert*, der Tokenzahl und den Schaltflächen **Suchen und Ersetzen** und der **Aktivierungsvorschau** öffnet sich der Abschnitt **Einstellungen** mit dem Namen, den Unter-Lorebooks, den Tags, der Scan-Tiefe und *Mit anderen Benutzern teilen* des Lorebooks. Siehe [Lorebooks](lorebooks/index.md).

Darunter stehen **Eintrag hinzufügen** (und die **⋮**-Taste mit der Massenbearbeitung und den Tags vieler Einträge), das Suchfeld und die Einträge. Ein Eintrag öffnet sich mit seinem Kontrollkästchen *Aktiviert* und Namen in einer Zeile; geöffnet hat er ein Formular: Name, Reihenfolge, Tags, Filtermodus und Text, Einfügemodus, negative Tags, den Aktivierungskontext und dessen Bereich, *Auch die Zusammenfassungen durchsuchen*, *Rekursion verhindern*, *Nur über andere Einträge*, den Inhalt, die Notiz und **Eintrag löschen**. Alles, was geändert wird, wird sofort gespeichert, wie am Desktop. Unter dem Suchfeld stehen die Tag- und Statusfelder, die die Liste einschränken; **Einträge auswählen** im Menü **⋮** setzt vor jeden Eintrag ein Kontrollkästchen und zeigt **Ausgewählte löschen** für die angekreuzten. Was hier fehlt: das Kontrollkästchen *Nur Wurzel-Lorebooks*.

Der Reiter **Lorebook** der Schublade der Geschichte zeigt das Wissen des Buchs wie der Desktop-Reiter: ein Suchfeld, die Einträge, die sich zu ihrem Inhalt öffnen, und den Stift, der den Text eines Eintrags bearbeitet.

Derselbe Editor wird im Reiter *Lorebook* eines Buchs verwendet. Wenn das Lorebook des Buchs Unter-Lorebooks hat, schaltet das Feld **Angezeigtes Lorebook** darunter den Reiter auf eines von ihnen um und wieder zurück, ohne das Lorebook des Buchs zu ändern. Während ein [Agent](agents.md) an einem Buch arbeitet, das das Lorebook verwendet, wird das Lorebook als Text angezeigt, und nichts darin kann geändert werden.

![Ein Lorebook auf dem Smartphone](../images/phone-lorebook.png)

![Das Menü des Lorebooks](../images/phone-lorebook-menu.png)

![Die Einstellungen des Lorebooks](../images/phone-lorebook-settings.png)

![Ein geöffneter Eintrag mit seinem Formular, darüber die Revisionen des Lorebook-VCS](../images/phone-lorebook-entry.png)

Die Schaltfläche neben dem Kontrollkästchen *Aktiviert* öffnet **Suchen und Ersetzen** für die Einträge des Lorebooks, siehe [Einträge](lorebooks/entries.md#search-and-replace).

![Suchen und Ersetzen in einem Lorebook auf dem Smartphone](../images/phone-lorebook-search-replace.png)

<a id="settings-protocols-providers-resources-trash"></a>

### Einstellungen, Protokolle, Anbieter, Ressourcen, Papierkorb

- **Einstellungen** sind wie am Desktop, in einer schmalen Spalte angeordnet; **Sicherung als neues Buch importieren** ist die Schaltfläche über *Änderungen verwerfen*.
- **Protokolle** und **Inferenzanbieter** listen jeden Eintrag als Text mit den Schaltflächen daneben auf (darunter bei den Anbietern, die mehrere haben). Die Bearbeitungsdialoge sind die des Desktops, sodass ein Protokoll auch vom Smartphone aus [geteilt](protocols.md#sharing-a-protocol) werden kann, und die Schaltfläche **Von anderem Benutzer importieren** über der Liste importiert ein Protokoll, das ein anderer Benutzer teilt.
- **Ressourcen** gruppiert die Dateien nach dem Objekt, das sie verwendet (öffne eine Gruppe mit ihrem Pfeil). Jede Datei ist eine Karte (ein Bild, der Name, Typ, Größe, Datum und die Schaltflächen zum Herunterladen, Ersetzen und Löschen), mit einem Kontrollkästchen für *Ausgewählte löschen*; eine Gruppe anzukreuzen löscht alle ihre Dateien.
- **Papierkorb** zeigt jedes gelöschte Objekt als Karte (Name, Typ, wann es gelöscht wurde). Die Erklärung zum Papierkorb steht hinter der *i*-Taste; die Schaltflächen der Filter brechen in eine zweite Zeile um. Das Wiederherstellen funktioniert wie unter [Papierkorb](trash.md) beschrieben.

![Die Ressourcen auf dem Smartphone](../images/phone-resources.png)

![Die Inferenzanbieter auf dem Smartphone, mit der Info-Taste eines geteilten Anbieters](../images/phone-providers.png)

![Der Papierkorb auf dem Smartphone](../images/phone-trash.png)
<a id="the-book-window"></a>

## Das Buch-Fenster

Ein Buch öffnet sich im **Vollbild**. Die Schließen-Schaltfläche ist das **✕** in der oberen Ecke. Die Reiter - *About*,
*Prompts*, *Lorebook*, *Story*, *Branch view*, *Backups* - bilden eine Zeile, die Sie seitlich scrollen. Sie arbeiten
wie auf dem Desktop (siehe [Bücher](books/index.md)), ihre Felder stehen jedoch in einer Spalte.

<a id="the-story"></a>

### Die Geschichte

Der Reiter Story ist eine Spalte mit Abschnitten. Die Unterschiede zum [Story-Editor](books/story-editor.md):

![Die Geschichte eines Buches auf dem Telefon](../images/phone-book-story.png)

- Die **Gliederung** (und die Reiter, die Erweiterungen daneben hinzufügen, etwa die [Author's Note](extensions/authors-note.md))
  befindet sich in einer **Schublade**. Öffnen Sie sie mit der Schaltfläche oben links.
- Die Schaltfläche **Zusammenfassungen** und das Einstellungsmenü (⚙, mit *Stile für Text ändern*, *Suchen und Ersetzen*, *Importieren*, *Massenlöschung* und *Geschichte exportieren*) befinden sich in der oberen Leiste, nicht
  unten.
- Die Schaltfläche **+** für einen neuen Abschnitt befindet sich unten.
- Das Fenster **Zusammenfassungen** hat seine Menüleiste (*Suchen und Ersetzen*, *Zusammenfassungen erstellen...*, *Alle Zusammenfassungen neu zusammenfassen*, *Zusammenfassungen exportieren*, *Zusammenfassungen importieren*) in einer eigenen Zeile;
  auf einem schmalen Bildschirm stehen die Einträge hinter der Schaltfläche **⋯**. [Zusammenfassungen importieren](books/summaries.md#export-and-import-of-the-summaries)
  ist dasselbe Fenster wie auf dem Desktop, in der Breite des Bildschirms.

![Zusammenfassungen importieren auf dem Telefon](../images/phone-summaries-import.png)
- [Anmerkungen](books/annotations.md): Der Reiter **Anmerkungen** befindet sich in der Schublade neben der Gliederung; ein Tippen auf eine unterstrichene Passage
  öffnet ihre Notiz. **Abschnitt kommentieren** und **Mit KI kommentieren** stehen im Menü des Abschnitts, **Auswahl kommentieren** im Menü eines langen Drückens auf den markierten Text.
- **Suchen und Ersetzen** öffnet dasselbe Fenster wie auf dem Desktop, mit den Feldern und Schaltflächen untereinander:

  ![Suchen und Ersetzen in der Geschichte auf dem Telefon](../images/phone-search-replace.png)

- Ein Abschnitt besteht nur aus seinem Text unter einer kleinen Zeile mit zwei Symbolen. Das erste ist das **Menü** des Abschnitts (bearbeiten, neu generieren,
  wischen, verzweigen, Verlauf, Prompt anzeigen, Bilder, Zusammenfassung, löschen); das zweite öffnet ein **Popup** mit dem, was der Desktop in der
  Spalte rechts neben einem Abschnitt zeigt: die IDs, das Modell, das Protokoll, Tokens, Wörter und die Details des Zugs.
- **Verlauf** im Menü eines Abschnitts öffnet den [Versionsverlauf](books/version-history.md) als Vollbildseite: Die Versionen sind
  Karten über der ausgewählten Version, mit denselben Aktionen wie auf dem Desktop.

  ![Der Versionsverlauf eines Abschnitts auf dem Telefon](../images/phone-history.png)

- **Statistik** befindet sich im Einstellungsmenü der Geschichte (die Statistik des Buches) und als Schaltfläche in der Kopfzeile der Liste der Bücher
  (alle Bücher). Das Fenster ist eine Vollbildseite; sein Kalender zeigt die letzten 16 Wochen (auf dem Desktop ein Jahr), siehe
  [Schreibstatistik](books/statistics.md).

  ![Die Statistik aller Bücher auf dem Telefon](../images/phone-statistics.png)

- Die Übersicht der [Zusammenfassungen](books/summaries.md) behält eine feste Breite und lässt sich seitlich scrollen.

## Administration

Ein Administrator hat den Reiter **Admin** in der Schublade, mit denselben Reitern wie die [Desktop-Seite](administration/index.md) in einer Zeile, die Sie seitlich scrollen:
*Benutzer*, *Datenbanksicherungen*, *Bereinigung*, *Ressourcendateien*, *Hilfeassistent*, *Agenten*, *Aktualisieren* und *Erweiterungen*. Die Listen (Benutzer, Sicherungen, Erweiterungen) sind
Karten mit ihren Schaltflächen unter dem Text, und die Werkzeugleisten brechen auf eine zweite Zeile um. Das Installieren einer Erweiterung (*Load Extension*) und das Hochladen einer Datenbanksicherung öffnen den Dateiauswahldialog des Telefons. Andere Benutzer erhalten den Reiter nicht.

![Die Benutzer im Reiter Admin auf dem Telefon](../images/phone-admin-users.png)

![Die Datenbanksicherungen auf dem Telefon](../images/phone-admin-backups.png)

<a id="what-is-missing"></a>

## Was fehlt

Das Telefon macht dasselbe wie der Desktop, mit diesen Unterschieden:

| Fehlt oder ist anders auf dem Telefon | Wo es zu tun ist |
|---|---|
| Das Kontrollkästchen *Only root lorebooks* und die Auswahl vieler Lorebook-Einträge zum gleichzeitigen Löschen | Desktop-Ansicht. |
| Die Befehlsleiste, Tastenkürzel, die Diff-Ansicht eines neu generierten Abschnitts und die Notizen am Rand | Desktop-Ansicht; sie brauchen eine Tastatur oder einen breiten Bildschirm. |
| Fenster mit vielen Spalten oder zwei Bereichen (die Übersicht der [Zusammenfassungen](books/summaries.md), das Gespräch der Beta Readers, der Manga Generator) sind klein | Sie funktionieren: Querformat ist bequemer. |

Alles, was nicht in dieser Liste steht, funktioniert wie auf dem Desktop: Generieren, Bearbeiten, Neu generieren, Wischen und Verzweigen von
Abschnitten, Zusammenfassungen und Meta-Zusammenfassungen, Buch-Prompts, der Verzweigungsbaum, Buchsicherungen, Export, Ressourcen, der Papierkorb, Lorebooks und die Administration.

<a id="extensions"></a>

## Erweiterungen

Das Telefon-Layout hat eigene Klassen, daher muss eine Erweiterung es ausdrücklich unterstützen. Die Erweiterungen, die mit
Marginalia kommen:

| Erweiterung | Auf dem Telefon |
|---|---|
| [Arc Outline](extensions/arc-outline.md) | Funktioniert. Der Reiter Arcs befindet sich in der Gliederungsschublade. |
| [Author's Note](extensions/authors-note.md) | Funktioniert. Die Notiz befindet sich in der Gliederungsschublade der Geschichte. |
| [Beta Readers](extensions/beta-readers.md) | Funktioniert. Der Reiter Beta Readers befindet sich in der Gliederungsschublade; der Platz für das Gespräch ist klein, Querformat ist bequemer. |
| [Book Description Generator](extensions/book-description-generator.md) | Funktioniert. Die Schaltfläche steht am Ende des Reiters About; der Dialog zeigt die Optionen über dem Ergebnis. |
| [Chapter Marker](extensions/chapter-marker.md) | Funktioniert. Kapitelüberschriften befinden sich in der Gliederungsschublade. |
| [Character Tracker](extensions/character-tracker.md) | Funktioniert. Der Reiter Characters befindet sich in der Gliederungsschublade. |
| [Story Notes](extensions/story-notes.md) | Funktioniert. Der Reiter Notes befindet sich in der Gliederungsschublade; das Seitenfenster passt auf den Bildschirm, eine lange Seite schreibt sich im Querformat leichter. |
| [Conlang Translator](extensions/conlang-translator.md) | Teilweise. Das Panel unter *Einstellungen → Erweiterungseinstellungen*, der Editor, die Überprüfung und der Assistent funktionieren (das Lexikongitter lässt sich seitlich scrollen), ebenso das Antippen einer übersetzten Rede im Betrachter; die Seite der Geschichte (**Dialoge übersetzen**, **Abschnitte übersetzen (Plansprache)**, das Anklicken in der Geschichte) wurde auf einem Telefon nicht ausprobiert, siehe [Grenzen](extensions/conlang-translator.md#limits). |
| [Converter](extensions/sillytavern-converter.md) | Funktioniert. Der Reiter Converter befindet sich in der Gliederungsschublade; der Bereich des Chats wird mit zwei Tippern gewählt, siehe [Auf dem Telefon](extensions/sillytavern-converter.md#on-a-phone). |
| [Instruction Exporter](extensions/instruction-exporter.md) | Funktioniert. **Export instructions** steht im Einstellungsmenü der Geschichte. |
| [Manga Generator](extensions/manga-generator.md) | Funktioniert. **Manga Generator** steht im Einstellungsmenü der Geschichte; sein Fenster ist groß, Querformat ist bequemer. |
| [Reviewer](extensions/reviewer.md) | Funktioniert. Seine Einträge befinden sich im Menü eines Abschnitts, seine Einstellungen unter *Einstellungen → Erweiterungseinstellungen*. |
| [Side Query](extensions/side-query.md) | Funktioniert. Der Reiter Side Query befindet sich in der Gliederungsschublade. |
| [Lore Generator](extensions/lore-generator.md) | Funktioniert. Die Zauberstab-Schaltfläche steht in der Zeile jedes Eintrags des Lorebooks; sein Fenster zeigt den Prompt über dem Ergebnis. |
| [Lorebook VCS](extensions/lorebook-vcs.md) | Funktioniert. Das Panel des Lorebooks steht unter seiner Zeile *Aktiviert*, die Revisionen eines Eintrags stehen oben in seinem Formular. |
| [Image Generator](extensions/image-generator.md) | Funktioniert. **Generate image** steht im Menü eines Abschnitts. |

Erweiterungen aus anderen Quellen können sich anders verhalten oder auf dem Telefon gar nichts hinzufügen; sie erscheinen dort, wo
ihr Autor sie funktionsfähig gemacht hat. Wenn eine Erweiterung zu fehlen scheint, prüfen Sie sie im Desktop-Layout. Wie eine Erweiterung
das Telefon unterstützt, beschreibt das [Entwicklerhandbuch](../developer/plugins/extendable.md#the-phone-twins).

<a id="tips"></a>

## Tipps

- Langen Text auf dem Telefon zu schreiben ist im Querformat leichter. Das Layout bleibt nach dem Drehen das Telefon-Layout, bis Sie
  die Seite neu laden.
- Wenn Ihnen etwas fehlt, das auf dem Telefon nicht vorhanden ist, drehen Sie das Telefon zur Seite und laden Sie neu, oder öffnen Sie Marginalia in einem
  breiteren Fenster; Ihre Bücher und Einstellungen sind überall dieselben.
- Um von einem Telefon aus auf Marginalia zuzugreifen, muss es als [Server](getting-started/docker-server.md) laufen: Die
  [Desktop-App](getting-started/desktop-app.md) lauscht nur auf Ihrem Computer.
