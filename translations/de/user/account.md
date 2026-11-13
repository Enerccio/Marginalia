---
label: Konto & Einstellungen
order: 920
icon: person
---

# Konto & Einstellungen

Alles, was du in Marginalia anlegst, gehört zu deinem Konto: Bücher, Lorebooks, Tags, Inferenzanbieter, Protokolle
und Einstellungen. Andere Benutzer sehen sie nicht (außer Bücher, die du [veröffentlichst](books/publishing.md)), und du siehst deren nicht.

<a id="your-account"></a>

## Dein Konto

Die Schaltflächen unten im Arbeitsbereich:

| Schaltfläche | |
|---|---|
| **Sprache** | Die Sprache der Benutzeroberfläche, siehe [Sprache](#language). |
| Sonne / Mond-Schaltfläche | Neben der Sprache: wechselt zwischen dem dunklen und dem hellen Design, siehe [Design](#theme). |
| **Passwort ändern** | Ändert dein Passwort, deinen Benutzernamen und deinen vollständigen Namen, siehe unten. |
| **Abmelden** | Meldet dich auf diesem Gerät ab. |
| **Administration** | Nur für Administratoren, siehe [Administration](administration/index.md). |

<a id="changing-the-password"></a>

### Passwort ändern

**Passwort ändern** öffnet einen Dialog mit **Benutzername**, **Vollständiger Name**, **Aktuelles Passwort**, **Passwort** und
**Passwort wiederholen**:

- gib für jede Änderung dein **aktuelles Passwort** ein (lass es leer, wenn dein Konto kein Passwort hat);
- um das Passwort zu ändern, gib das neue in beiden Passwortfeldern ein;
- lass beide Passwortfelder leer, um nur den Namen zu ändern und das Passwort beizubehalten;
- setze den Haken bei **Auf allen anderen Geräten abmelden (andere Browser und alle gespeicherten Anmeldungen)**, um dein Konto auf allen anderen Geräten abzumelden, ohne das Passwort zu ändern, siehe unten;
- der Benutzername darf nicht von einem anderen Konto verwendet werden.

Klicke auf **OK**, um zu speichern. Bei einem falschen aktuellen Passwort wird nichts gespeichert.

Eine Änderung des Passworts beendet alle gespeicherten Anmeldungen (*Angemeldet bleiben*) auf allen Geräten, auch auf diesem: Sie müssen sich mit dem neuen Passwort erneut anmelden. Deine anderen Sitzungen, die gerade geöffnet sind (ein Browser auf einem anderen Gerät, der noch den Arbeitsbereich zeigt), werden ebenfalls beendet; nur die, in der du gerade arbeitest, bleibt bestehen. Wenn ein Gerät verloren wurde oder dein Passwort abhandengekommen ist, meldet eine Passwortänderung dieses Gerät ab.

**Auf allen anderen Geräten abmelden** tut dasselbe ohne ein neues Passwort: Jede gespeicherte Anmeldung deines Kontos wird vergessen und jede andere offene Sitzung beendet. Nutze es, wenn du dich irgendwo angemeldet gelassen hast, zum Beispiel an einem Computer, der nicht dir gehört. Dafür wird dein aktuelles Passwort benötigt, wie bei jeder anderen Änderung in diesem Dialog. Die Sitzung, in der du gerade bist, bleibt bestehen, aber ihre gespeicherte Anmeldung wird ebenfalls vergessen, sodass dieser Browser auch beim nächsten Mal nach dem Passwort fragt.

<a id="language"></a>

### Sprache

Die Auswahl **Sprache** über den Schaltflächen wechselt die Benutzeroberfläche in eine andere Sprache. Die Wahl wird in
deinem Konto gespeichert: Die Seite wird mit der neuen Sprache neu geladen, ebenso jede andere Seite, die du geöffnet hast (auch auf anderen Geräten),
und sie wird beim nächsten Anmelden wieder verwendet. Der Anmeldebildschirm selbst ist immer in der Standardsprache.

Die Liste enthält nur die Sprachen, die installiert sind. Englisch, Japanisch, Französisch, Spanisch, Deutsch, Koreanisch und Italienisch sind in Marginalia enthalten; ein Administrator
kann weitere hinzufügen, indem er eine [Erweiterung](extensions/index.md) lädt, zum Beispiel die
[chinesische Lokalisierung](extensions/chinese-localization.md). Texte, die eine Sprache nicht übersetzt, werden auf Englisch angezeigt. Die mitgelieferten Erweiterungen bringen ihre eigenen Texte auf Japanisch, Chinesisch, Französisch, Spanisch, Deutsch, Koreanisch und Italienisch mit (die chinesischen werden angezeigt, wenn die chinesische Lokalisierung geladen ist).
Datumsangaben werden so geschrieben, wie es die jeweilige Sprache vorsieht. Deine Bücher und ihre Texte werden nicht übersetzt, nur die Oberfläche.

![Die Auswahl der Sprache](../images/language-selector.png)

![Der Arbeitsbereich auf Japanisch](../images/ui-japanese.png)

<a id="theme"></a>

### Design

Die Schaltfläche neben der Auswahl **Sprache** wechselt die Benutzeroberfläche zwischen dem dunklen Design (der Standard) und dem hellen. Die Wahl wird in deinem Konto gespeichert und beim nächsten Anmelden auf jedem Gerät wieder verwendet; eine Seite, die bereits in einem anderen Tab geöffnet ist, ändert sich beim erneuten Laden. Diagramme und die Beziehungskarte übernehmen die Farben des Designs, wenn sie geöffnet werden.

![Der Arbeitsbereich im hellen Design](../images/theme-light.png)

<a id="logging-in"></a>

### Anmelden

**Angemeldet bleiben** auf dem Anmeldebildschirm hält dich auf diesem Gerät 30 Tage lang angemeldet (dafür ist HTTPS auf einem Server nötig, siehe
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Abmelden** beendet dies.

Ein Konto kann ein leeres Passwort haben; dann meldest du dich nur mit dem Benutzernamen an. Hat ein Administrator dein vergessenes Passwort entfernt, melde dich so an und lege sofort mit **Passwort ändern** ein neues Passwort fest. Das ist praktisch für die Desktop-App auf deinem eigenen Computer, aber mach das niemals auf einem Server, den andere erreichen können.

<a id="settings"></a>

## Einstellungen

Der Reiter **Einstellungen** enthält deine Standardwerte für alle deine Bücher.

![Der Reiter Einstellungen](../images/settings-defaults.png)

Oben stehen drei Schaltflächen:

- **Sicherung als neues Buch importieren** - legt ein Buch aus einer Sicherungsdatei an, siehe
  [Buchsicherungen](books/backups.md#importing-a-backup-as-a-new-book);
- **Änderungen verwerfen** - verwirft die nicht gespeicherten Änderungen und lädt die gespeicherten Einstellungen erneut;
- **Speichern** - speichert die Einstellungen.

!!!info Nicht gespeicherte Änderungen
Einstellungen werden nicht automatisch gespeichert. Sobald du ein Feld änderst (auch die Einstellungen von Erweiterungen), erscheint oben *Nicht gespeicherte Änderungen*, und die anderen Reiter, **Administration** und **Abmelden** sind gesperrt. Klicke auf **Speichern** oder **Änderungen verwerfen**, um sie wieder freizugeben.
!!!

<a id="general-settings"></a>

### Allgemeine Einstellungen

| Feld | |
|---|---|
| **Standardmodell** | Der [Inferenzanbieter](inference-providers.md), mit dem neue Bücher beginnen. |
| **Standardprotokoll** | Das [Protokoll](protocols.md), mit dem neue Bücher beginnen. |
| **Standardmodell für Anmerkungen**, **Standardprotokoll für Anmerkungen** | Das Modell, das [einen Abschnitt annotiert](books/annotations.md#letting-a-model-annotate), wenn du für den Lauf kein anderes wählst. Leer bedeutet das Modell des Buches. |
| **Sicherungsstrategie** | Automatische [Buchsicherungen](books/backups.md#automatic-backups) für Bücher ohne eigene Strategie: *Keine Sicherungen*, *Nach N Nachrichten* oder *Nach N Minuten*, mit der Zahl daneben. |
| **Aufbewahrte Versionen je Abschnitt** | Wie viele frühere Versionen des Textes eines Abschnitts der [Verlauf](books/version-history.md#how-many-versions-are-kept) für alle deine Bücher behält, sofern nicht ein Ordner, ein Buch oder der Administrator es stärker begrenzt. Leer setzt hier keine Grenze; **0** behält keinen Verlauf. |
| **Auf einmal geladene Abschnitte in der Geschichte** | Wie viele Abschnitte eines Buches die [Geschichte](books/story-editor.md) und die [Ansicht](books/publishing.md) geladen halten. Die Geschichte beginnt mit den letzten Abschnitten, die Ansicht dort, wo du zuletzt gelesen hast (oder mit den ersten Abschnitten); die davor und danach liegenden Abschnitte werden beim Scrollen geladen, und die, die weit von dir entfernt sind, werden wieder entladen. Leer lädt alle Abschnitte auf einmal. |
| **Tagesziel: geschriebene Wörter**, **Tagesziel: Minuten Bearbeitung** | Das Ziel eines Tages für den Kalender der [Schreibstatistik](books/statistics.md#the-daily-goal): die Wörter, die du selbst schreibst, und die Minuten, die du bearbeitest, über alle deine Bücher hinweg. Leer bedeutet kein Ziel. |
| **Schreibstatistik erfassen**, **Statistiken behalten für (Monate)** | Ob deine [Schreibstatistik](books/statistics.md#privacy-clearing-not-recording-keeping-less) erfasst wird und wie viele Monate davon aufbewahrt werden (leer oder 0: alle). Ein Administrator kann die Erfassung für alle abschalten und eine kürzere Dauer festlegen. |

*Standardmodell* und *Standardprotokoll* werden nur beim Anlegen eines Buches verwendet; ihre Änderung ändert bestehende Bücher nicht. Die Erweiterung [Lore Generator](extensions/lore-generator.md) hat kein Buch und generiert mit ihnen. Die Sicherungsstrategie gilt für alle Bücher ohne *Abweichung je Manuskript* ab dem nächsten erzeugten Abschnitt.
<a id="templates"></a>

### Vorlagen

Die Standardwerte für die [Buchprompts](books/prompts.md):

| Feld | |
|---|---|
| **Sprache der Prompts** | Die Sprache der eingebauten Prompts: Englisch (der Standard), Japanisch, Französisch, Spanisch, Deutsch, Italienisch, Koreanisch oder Chinesisch, wenn die [chinesische Lokalisierung](extensions/chinese-localization.md) geladen ist. Siehe unten [Sprache der Prompts](#prompt-language). |
| **Hauptvorlage** | Der Rahmen des Prompts, siehe [Hauptvorlage](books/prompts.md#master-template). |
| **Erzählperspektive (POV)**, **Zeitform** | Die erzählerische Stimme und die Zeitform. |
| **Stil** | Richtlinien für Stil und Ton. |
| **Standard-Benutzerprompt** | Die Anfrage für jeden Teil, siehe [Benutzerprompt](books/prompts.md#user-prompt). |
| **Standard-Zusammenfassungsprompt** | Die Anfrage für eine [Zusammenfassung](books/summaries.md#the-summary-prompt). |
| **Standard-Metazusammenfassungsprompt** | Die Anfrage für eine [Metazusammenfassung](books/summaries.md#the-meta-summary-prompt), die Zusammenfassungen zusammenführt. |
| **Systemprompt für Überarbeitungen**, **Überarbeitungsprompt** | Die Systemnachricht und die letzte Nachricht einer [Überarbeitung](books/story-editor.md#redrafting-a-part) eines Teils, siehe [Überarbeitungsprompt](books/prompts.md#redraft-prompt). |
| **Systemprompt für Anmerkungen**, **Annotationsprompt** | Die Systemnachricht und die letzte Nachricht der Anfrage an ein Modell, einen Teil zu [annotieren](books/annotations.md#letting-a-model-annotate), siehe [Annotationsprompt](books/prompts.md#annotate-prompt). |
| **Annotationsanweisungen** | Worauf das Modell beim Annotieren achtet, für die Bücher und Ordner, die nichts festlegen, siehe [Annotationsprompt](books/prompts.md#annotate-prompt). |
| **Impersonate-Prompt** | Die Anfrage an ein Modell, [die Anweisungen](books/story-editor.md#drafting-the-instructions) für den nächsten Zug an deiner Stelle zu entwerfen, siehe [Impersonate-Prompt](books/prompts.md#impersonate-prompt). |
| **Verdichtungsprompt der Agenten** | Nur für Benutzer, die [Agenten](agents.md) verwenden dürfen. Was deine Agenten gefragt werden, wenn du sie mit [`/compact`](agents.md#running-a-command-yourself) verdichtest, falls ihr eigener Verdichtungsprompt leer ist; leer heißt hier der Wert, den dein Administrator gesetzt hat, sonst der eingebaute. |
| **Anweisungen der Agenten (agents.md)** | Nur für Benutzer, die [Agenten](agents.md) verwenden dürfen. Deine Anweisungen für die Agenten, die keine eigenen haben; sie werden bei jedem Lauf zu ihrem Prompt hinzugefügt. Leer heißt keine. |
| **Nachricht an eine URL, wenn ein Agent dich braucht**, **Format der Nachricht**, **Auch wenn die Anwendung geöffnet ist**, **Testnachricht senden** | Nur für Benutzer mit der Berechtigung [*Benachrichtigungen*](administration/permissions.md) (und nicht nur für diejenigen, die [Agenten](agents.md) verwenden), im Tab *Allgemein*, und nur wenn dein Administrator es erlaubt. Hier wird eine kurze Nachricht hingeschickt, wenn ein Agent etwas fragt, auf dich wartet, fertig wird oder fehlschlägt und du die Anwendung geschlossen hast: siehe [Wenn du nicht da bist](agents.md#when-you-are-not-there). |
| **Systemprompt der Agenten** | Nur für Benutzer, die [Agenten](agents.md) verwenden dürfen. Was deinen Agenten als Rolle vorgegeben wird, wenn ihr eigener Systemprompt leer ist; leer heißt hier der Wert, den dein Administrator gesetzt hat, sonst der eingebaute. Reiner Text, keine Vorlage. |

![Der Tab Vorlagen](../images/settings-templates.png)

Weiter unten, für Benutzer, die Agenten verwenden dürfen, stehen die Prompts der Agenten:

![Die Prompts der Agenten im Tab Vorlagen](../images/settings-templates-agents.png)

Jedes Feld zeigt den eingebauten Standard in Grau, solange es leer ist, und hat das Hinweis-Popover mit
**Standardvorlage einfügen** und den verfügbaren [Variablen und Makros](templates/index.md).

<a id="prompt-language"></a>

#### Sprache der Prompts

**Sprache der Prompts** wählt die Sprache der eingebauten Prompts: wofür ein leeres Feld steht (der graue Text ändert sich,
sobald du eine andere Sprache wählst), und damit, was dem Modell für Generierung, Zusammenfassungen, Überarbeitung, Annotationen
und Impersonate geschickt wird. Die Prompts der Erweiterungen, die eigene haben (Arc Outline, Beta Readers, Book Description Generator,
Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook), und die der Agenten folgen ihr ebenfalls.
Sie ist unabhängig von der Sprache der Oberfläche: Du kannst die Anwendung auf Englisch verwenden und japanische Prompts senden.
Ein Modell folgt Anweisungen am besten in der Sprache der Geschichte, also wähle die Sprache, in der deine Bücher geschrieben sind. Nur die
eingebauten Prompts ändern sich: Ein Prompt, den du selbst geschrieben hast, hier, im Buch oder im Ordner, bleibt, wie er ist. Ein [Agent](agents.md)
liest seine Prompts und Hinweise in seiner eigenen Sprache (*Sprache* in den Einstellungen des Agenten), nicht in dieser.

Diese Standardwerte gelten **für jedes Buch, dessen eigenes Feld leer ist**, sofort, nicht nur für neue Bücher. Ein Buch, das
auf seinem Tab *Prompts* einen eigenen Wert setzt, behält ihn. Also:

- Setze hier, was alle deine Bücher gemeinsam haben;
- setze auf dem Tab *Prompts* eines Buches, was nur für dieses eine gilt;
- leere das Feld eines Buches, um es auf den Standard aus den *Einstellungen* zurückzusetzen.

<a id="styles"></a>

### Stile

Die Textstile deiner Bücher: wie der Text der Teile in der Geschichte und beim Leser aussieht. Der Tab listet die Stile **Normal** und **Buch**, die die Anwendung mitbringt und die nicht geändert werden können (sie sind als *eingebaut* markiert), sowie deine eigenen. Wähle einen Stil aus, um ihn rechts zu bearbeiten. Die Schaltflächen unter der Liste:

- **Duplizieren** - macht aus dem gewählten Stil einen eigenen Stil (auch aus einem eingebauten: so fängst du an), benannt
  *... (Kopie)*, den du dann umbenennen und ändern kannst;
- **Löschen** - löscht einen deiner Stile; ein eingebauter Stil kann nicht gelöscht werden;
- **Als Standard festlegen** - macht den Stil zu dem, den die Bücher verwenden, sofern sie in den Einstellungen der Geschichte im
  Menü [einen anderen wählen](books/story-editor.md#text-styles). Der Standardstil ist in der Liste als *Standard* markiert.
- **Exportieren** und **Importieren** - speichern den gewählten Stil als `.style.json`-Datei und fügen die Stile einer solchen Datei
  deinen eigenen hinzu (nicht gespeichert, bis du die Einstellungen speicherst). Siehe [Einen Stil teilen](books/styling-guide.md#share-a-style-export-and-import).

Der [Leitfaden für Stile](books/styling-guide.md) geht jede Option durch und enthält einen Beispielstil zum Herunterladen.

![Der Tab Stile, mit dem Horror-Stil aus dem Leitfaden für Stile ausgewählt](../images/settings-styles.png)

Für einen eigenen Stil hat das Formular rechts einen **Name**, das Kästchen **Für Exporte (für Bücher nicht angeboten)** und zwei Tabs. Ein Stil ist
entweder für Bücher oder für Exporte: Ein Stil für Exporte wird im [Exportdialog](books/export.md#print-styles) angeboten (die Anwendung bringt
vier mit, das Aussehen der HTML-, EPUB-, PDF- und Word-Exporte), und er kann nicht der Standardstil der Bücher sein; ein Stil für Bücher wird im
Exportdialog nicht angeboten. Die Liste markiert sie mit *(Export)*.

- **Optionen** - in Gruppen: *Text* (Schrift, Größe, Zeilenhöhe, Buchstaben- und Wortabstand, Farbe, Ausrichtung, Silbentrennung, der
  Abstand zwischen Absätzen, der **Erstzeileneinzug** der Absätze und ein **Initial** nach Überschriften), *Seite* (die Breite der Textspalte, zentriert, und die **Hintergrundfarbe**, die den ganzen Bereich hinter dem Text füllt), *Hervorhebungen, Links und Code* (die Farben von kursivem und fettem Text und von Links, die Schrift für Code), *Zitate*, *Überschriften* (Schrift, Größe der ersten Ebene, Farbe, Ausrichtung, Kapitälchen) sowie Symbol und
  Farbe des *Szenenwechsels* (`---`). Ein Feld nimmt jeden CSS-Wert auf (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`); ein leeres Feld
  behält das Aussehen der Anwendung. Die Schriftfelder bieten gängige Schriftfolgen an und nehmen auch deine eigenen.
- **Eigenes CSS** - eigenes CSS, das nach den Optionen hinzugefügt und innerhalb des Stils angewendet wird, sodass eine Regel, die du schreibst, nur
  für den Text mit diesem Stil gilt: `.chat-message-markdown p { text-indent: 1.5em !important; }` rückt Absätze ein, einfache Deklarationen
  (`font-size: 1.2rem;`) gelten für den ganzen Text. Darunter zeigt **Generiertes CSS** das Stylesheet, das die Optionen erzeugen (nur lesbar).
  Die Klammern `{ }` müssen übereinstimmen, sonst wird der Stil nicht gespeichert.

![Der Tab Eigenes CSS eines Stils](../images/settings-styles-css.png)

Unter dem Formular zeigt die **Vorschau** einen Beispieltext im Stil und folgt jeder Änderung, während du tippst.

Wie die anderen Tabs wird auch der Tab Stile mit dem **Speichern** der Einstellungen gespeichert, und **Änderungen verwerfen** verwirft auch dessen Änderungen: Duplizieren, Bearbeiten, Löschen und Festlegen des Standards ändern nur die Arbeitskopie, und *Ungespeicherte Änderungen* (und die Sperre) erscheint, sobald du eines davon tust. Ein Stil ohne Namen oder mit eigenem CSS, dessen Klammern nicht übereinstimmen, wird beim Speichern abgelehnt.
<a id="exports"></a>

### Exporte

Womit jedes Dateiformat exportiert wird: Der Tab hat einen Abschnitt für jedes Format, das etwas einzustellen hat (**Plain text**, **Markdown**, **HTML**,
**Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault**; ein Klick auf den Namen öffnet ihn).

- **Print style** (HTML, Word, PDF und EPUB, die Formate mit Schriften und Farben) - der Stil, den der [Exportdialog](books/export.md) für das Format vorauswählt.
  Anfangs ist es das Aussehen, das die Anwendung für das Format mitbringt (**HTML page**, **PDF book**...); die Liste enthält die
  [Stile für Exporte](#styles), die eingebauten und deine eigenen. Wähle einen anderen, um dieses Format jedes Mal in ihm zu exportieren, bis du die
  Auswahl im Dialog für einen einzelnen Export änderst.
- **Front matter** (Markdown with front matter und Obsidian vault) - der YAML-Block am Anfang der Datei, mit `{{title}}`, `{{author}}`,
  `{{language}}` und `{{exportDate}}`. Leer bedeutet den Standardblock.
- **Title page** (jedes Format außer den Formaten für andere Werkzeuge; der Obsidian vault hat sie in der Notiz des Buches) - die Vorlage der Titelseite, eine Handlebars-Vorlage, die Markdown schreibt, mit `{{title}}` und
  `{{author}}` (das Info-Symbol im Feld listet sie auf). Leer ist die Standardvorlage: der Titel als Überschrift und darunter der Autor kursiv.
  **Fill default** im Info-Popover übernimmt den Standard als Ausgangspunkt.
- **Authorship statement** (jedes Format außer dem SillyTavern-Chat) - der Text, den das Kontrollkästchen *Include authorship statement* des Exports am Ende anfügt: `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`,
  `{{generatedPercent}}` und `{{models}}` (die Modelle, durch Kommas getrennt, leer, wenn keines vorhanden ist). Leer ist der Standardtext.

Dies sind deine Versionen: Ein Buch oder ein Ordner kann eigene haben, siehe [Title page template](books/export.md#title-page-template). Der Tab wird mit **Speichern** der Einstellungen gesichert, und **Änderungen verwerfen** verwirft die Änderungen, wie bei den anderen. Eine Vorlage, die nicht gültig ist, wird beim Speichern abgelehnt. Ein Stil, den du im Tab Styles anlegst, lässt sich hier wählen, nachdem du gespeichert hast.

![Der Tab Exports, mit geöffnetem HTML-Abschnitt](../images/settings-exports.png)

### Tags

Eine Tabelle aller deiner Tags (der Schlagwörter, die du Büchern, Lorebooks und Lorebook-Einträgen gibst) mit der Anzahl der Objekte, mit denen jedes
Tag verknüpft ist. Ein Klick auf eine Spaltenüberschrift sortiert nach dieser Spalte. Anders als die anderen Tabs wirkt er sofort: Er gehört nicht zu den
ungespeicherten Änderungen und hat nichts zu speichern.

![Der Tab Tags](../images/settings-tags.png)

| Button | |
|---|---|
| **Umbenennen** (Stift) | Fragt nach dem neuen Namen des Tags; jedes Objekt behält das Tag unter dem neuen Namen. |
| **Löschen** (Mülleimer) | Fragt nach einer Bestätigung, dann löscht es das Tag und entfernt es von allen Objekten, die es haben. |

Wenn der neue Name der Name eines anderen Tags ist (Namen werden ohne Beachtung der Groß- und Kleinschreibung verglichen), würden die beiden Tags beim Umbenennen gleich werden. Daher wirst du gefragt, ob du sie **zusammenführen** willst. Zusammenführen verschiebt alles, was mit dem umbenannten Tag markiert ist, zum vorhandenen
Tag und löscht das umbenannte; ein Objekt, das beide hat, behält das vorhandene Tag einmal. Abbrechen, und nichts
ändert sich, das Tag behält seinen alten Namen.

![Nachfrage, ob zwei Tags zusammengeführt werden sollen](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Tastenkürzel

Die Tasten der [Tastenkürzel des Story-Editors](books/story-editor.md#keyboard-shortcuts) (sie funktionieren im Story-Editor auf einem Computer; das [Telefon-Layout](phone.md) hat keine).
Die Tabelle listet jede Aktion auf, auch die, die [Erweiterungen](extensions/index.md) mitbringen, mit ihrer Taste, sowie die Tasten der Schaltflächen des Fensters [Organise](books/folders.md#organise) der Buchliste. Jeder Eintrag des Einstellungsmenüs einer Story, des Menüs eines Teils und des Kontextmenüs seines Textes ist hier eine Aktion (die meisten haben noch keine Taste, bis du ihnen eine gibst), und die Menüs zeigen die Taste neben dem Eintrag an.

| Button | |
|---|---|
| **Aufnehmen** | Drücke die neue Tastenkombination (Modifikatoren zuerst oder gleichzeitig, z. B. `Ctrl+Shift+K`); ein Klick an eine andere Stelle bricht ab. |
| **Auf Standard zurücksetzen** | Gibt der Aktion ihre Standardtaste zurück. |
| **Leeren** | Die Aktion hat keine Taste. |

Ein roter Hinweis nennt eine Taste, die sich zwei Aktionen teilen, eine Taste, die der Browser für sich behält (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` und ähnliche; die Seite bekommt sie nie), und `Alt` mit einem Buchstaben (er tippt auf manchen Tastaturen ein Zeichen, z. B. unter macOS). Eine Taste ohne
`Ctrl`, `Alt` oder `Meta` funktioniert nur, wenn kein Textfeld den Fokus hat. Die Tasten werden mit **Speichern** wie die anderen
Einstellungen gesichert und gelten sofort in den geöffneten Geschichten.

![Einstellungen, Tastenkürzel](../images/settings-shortcuts.png)

<a id="notifications"></a>

### Benachrichtigungen

Eine Desktop-Benachrichtigung des Browsers, wenn ein Modell fertig ist, damit du den Tab verlassen kannst, während es arbeitet. Der Tab hat ein Kontrollkästchen
**Desktop-Benachrichtigungen verwenden**, die Schaltflächen unten und eine Liste, was benachrichtigt:

- **Generierung der Geschichte**, **Mit KI kommentieren**, **Mit KI kommentieren (automatisch)**, **Überarbeiten**, **Überarbeiten (automatisch)**, **Anweisungen entwerfen**,
  **Zusammenfassung**, **Alle Zusammenfassungen neu zusammenfassen** und **Hilfeassistent**, sowie das, was [Erweiterungen](extensions/index.md) hinzufügen, benannt nach der Erweiterung
  (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...).
  Die mit *(automatic)* markierten Einträge des *Manga Generator* sind dessen fortlaufender Modus.
- Die mit *(automatic)* markierten Einträge sind die Läufe, die das Buch selbst startet, die [automatische Annotation](books/story-editor.md) nach einer Generierung
  und die Überarbeitung, die darauf folgt. Sie werden getrennt von dem Dialog gewählt, den du selbst öffnest: Ist **Mit KI kommentieren** angehakt und
  **Mit KI kommentieren (automatisch)** nicht, bleibt die automatische Annotation stumm; ist nur der automatische Eintrag angehakt, benachrichtigt nur die
  automatische Annotation.
- Ein Lauf, der mit einem Fehler endet, benachrichtigt ebenfalls (*… failed*), einer, den du selbst stoppst, nicht.

Das Kontrollkästchen schaltet Benachrichtigungen für dein Konto ein und aktiviert zwei Schaltflächen. **Diesen Browser registrieren** fragt den Browser nach der
Erlaubnis (aus diesem Klick heraus); die Zeile unter den Schaltflächen zeigt, was dieser Browser meldet: erlaubt, blockiert (erlaube es in den Seiteneinstellungen
des Browsers) oder noch nicht gefragt. Die Erlaubnis gehört zum Browser, nicht zu deinem Konto: Jeder Browser oder jedes Gerät, in dem du Benachrichtigungen willst, muss
einmal registriert werden; das Kontrollkästchen und die Liste sind überall dieselben. **Testbenachrichtigung senden** zeigt sofort eine an, auch während die Seite im Vordergrund ist.

Das Feld **Filter** engt die Liste nach Name ein; **Angezeigte markieren** und **Markierung der angezeigten aufheben** wirken auf die Einträge, die es zeigt, und lassen die anderen
unverändert. Eine Benachrichtigung wird nur angezeigt, solange du die Seite nicht ansiehst (anderer Tab oder das Fenster ist nicht im Vordergrund); ein Klick darauf
bringt die Seite zurück.

Benachrichtigungen brauchen eine `https://`-Adresse oder `localhost`: Auf einer einfachen `http://`-Adresse eines Servers in deinem Netzwerk bietet der Browser sie nicht an, und der Tab sagt das. Ein Tab, den der Browser schlafen gelegt hat (meist auf einem Telefon), kann sie ebenfalls nicht anzeigen. Der Tab wird mit dem
**Speichern** der Einstellungen gesichert, wie die anderen.

<a id="extension-settings"></a>

### Erweiterungseinstellungen

Einstellungen der installierten [Erweiterungen](extensions/index.md), ein Abschnitt pro Erweiterung (zum Beispiel *Reviewer Settings*
und *SideQuery Settings*). Sie werden mit derselben Schaltfläche **Speichern** gesichert. Der Tab ist leer, wenn keine Erweiterung mit
Einstellungen installiert ist.

![Der Tab Erweiterungseinstellungen mit den Einstellungen des Lore Generator](../images/settings-extensions.png)
