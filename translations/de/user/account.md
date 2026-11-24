---
label: Konto & Einstellungen
order: 920
icon: person
---

# Konto & Einstellungen

Alles, was du in Marginalia anlegst, gehört zu deinem Konto: Bücher, Lorebooks, Tags, Inferenz-Anbieter, Protokolle und Einstellungen. Andere Benutzer sehen sie nicht (außer Bücher, die du [veröffentlichst](books/publishing.md)), und du siehst ihre nicht.

<a id="your-account"></a>

## Dein Konto

Die Schaltflächen unten im Arbeitsbereich:

| Schaltfläche | |
|---|---|
| **Language** | Die Sprache der Benutzeroberfläche, siehe [Sprache](#language). |
| Sonne- / Mond-Schaltfläche | Neben der Sprache: wechselt zwischen dunklem und hellem Design, siehe [Design](#theme). |
| **Change Password** | Ändert dein Passwort, deinen Benutzernamen und deinen vollständigen Namen, siehe unten. |
| **Logout** | Meldet dich auf diesem Gerät ab. |
| **Admin** | Nur für Administratoren, siehe [Administration](administration/index.md). |

<a id="changing-the-password"></a>

### Passwort ändern

**Change Password** öffnet einen Dialog mit **Username**, **Full Name**, **Current Password**, **Password** und **Repeat password**:

- gib für jede Änderung dein **aktuelles Passwort** ein (lass es leer, wenn dein Konto kein Passwort hat);
- um das Passwort zu ändern, gib das neue in beide Passwortfelder ein;
- lass beide Passwortfelder leer, um nur den Namen zu ändern und das Passwort zu behalten;
- setze den Haken bei **Log out everywhere else (other browsers and all saved logins)**, um dich auf allen anderen Geräten abzumelden, ohne das Passwort zu ändern, siehe unten;
- der Benutzername darf nicht von einem anderen Konto verwendet werden.

Klicke auf **OK**, um zu speichern. Bei einem falschen aktuellen Passwort wird nichts gespeichert.

Ein Passwortwechsel beendet alle gespeicherten Anmeldungen (*Save login*) auf jedem Gerät, auch auf diesem: Du musst dich mit dem neuen Passwort erneut anmelden. Auch deine anderen gerade geöffneten Sitzungen (ein Browser auf einem anderen Gerät, auf dem der Arbeitsbereich noch angezeigt wird) werden beendet; nur die Sitzung, in der du gerade arbeitest, bleibt bestehen. Wenn ein Gerät verloren wurde oder dein Passwort bekannt geworden ist, meldet ein Passwortwechsel dieses Gerät ab.

**Log out everywhere else** tut dasselbe ohne neues Passwort: Jede gespeicherte Anmeldung deines Kontos wird vergessen, und jede andere offene Sitzung wird beendet. Nutze es, wenn du dich irgendwo angemeldet gelassen hast, zum Beispiel an einem Computer, der nicht dir gehört. Wie jede andere Änderung in diesem Dialog braucht es dein aktuelles Passwort. Die Sitzung, in der du gerade bist, bleibt bestehen, aber ihre gespeicherte Anmeldung wird ebenfalls vergessen, sodass dieser Browser auch beim nächsten Mal nach dem Passwort fragt.

<a id="language"></a>

### Sprache

Der Auswahlfeld **Language** über den Schaltflächen schaltet die Benutzeroberfläche in eine andere Sprache um. Die Auswahl wird in deinem Konto gespeichert: Die Seite lädt mit der neuen Sprache neu, ebenso jede andere Seite, die du geöffnet hast (auch auf einem anderen Gerät), und sie wird beim nächsten Anmelden wieder verwendet. Die Anmeldeseite selbst ist immer in der Standardsprache.

Die Liste enthält nur die installierten Sprachen. Englisch, Japanisch, Französisch, Spanisch, Deutsch, Koreanisch und Italienisch sind in Marginalia enthalten; ein Administrator kann weitere hinzufügen, indem er eine [Erweiterung](extensions/index.md) lädt, zum Beispiel die [chinesische Lokalisierung](extensions/chinese-localization.md). Texte, die eine Sprache nicht übersetzt, werden auf Englisch angezeigt. Die mitgelieferten Erweiterungen bringen ihre eigenen Texte auf Japanisch, Chinesisch, Französisch, Spanisch, Deutsch, Koreanisch und Italienisch mit (die chinesischen erscheinen, wenn die chinesische Lokalisierung geladen ist). Datumsangaben werden in der Form dieser Sprache geschrieben. Deine Bücher und ihre Texte werden nicht übersetzt, nur die Benutzeroberfläche.

![Das Sprachauswahlfeld](../images/language-selector.png)

![Der Arbeitsbereich auf Japanisch](../images/ui-japanese.png)

<a id="theme"></a>

### Design

Der Button neben dem Auswahlfeld **Language** wechselt die Benutzeroberfläche zwischen dem dunklen Design (Standard) und dem hellen. Die Auswahl wird in deinem Konto gespeichert und beim nächsten Anmelden auf jedem Gerät wieder verwendet; eine Seite, die bereits in einem anderen Tab geöffnet ist, ändert sich erst beim Neuladen. Diagramme und die Beziehungskarte übernehmen die Farben des Designs, wenn sie geöffnet werden.

![Der Arbeitsbereich im hellen Design](../images/theme-light.png)

<a id="logging-in"></a>

### Anmelden

**Save login** auf der Anmeldeseite hält dich auf diesem Gerät 30 Tage lang angemeldet (auf einem Server braucht es HTTPS, siehe [Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Logout** beendet die Anmeldung.

Ein Konto kann ein leeres Passwort haben; dann meldest du dich nur mit dem Benutzernamen an. Hat ein Administrator dein vergessenes Passwort gelöscht, melde dich so an und setze sofort mit **Change Password** ein neues Passwort. Das ist praktisch für die Desktop-App auf deinem eigenen Computer, aber tu das niemals auf einem Server, den andere erreichen können.

<a id="settings"></a>

## Einstellungen

Der Reiter **Settings** enthält deine Standardwerte für alle deine Bücher.

![Der Reiter Settings](../images/settings-defaults.png)

Oben stehen drei Schaltflächen:

- **Import Backup as New Book** - legt aus einer Sicherungsdatei ein Buch an, siehe [Buchsicherungen](books/backups.md#importing-a-backup-as-a-new-book);
- **Discard Changes** - verwirft die ungespeicherten Änderungen und lädt die gespeicherten Einstellungen erneut;
- **Save** - speichert die Einstellungen.

!!!info Ungespeicherte Änderungen
Einstellungen werden nicht automatisch gespeichert. Sobald du ein Feld änderst (auch die Einstellungen von Erweiterungen), erscheint oben *Unsaved changes*, und die übrigen Reiter, **Admin** und **Logout** sind gesperrt. Klicke auf **Save** oder **Discard Changes**, um sie wieder freizugeben.
!!!

<a id="general-settings"></a>

### Allgemeine Einstellungen

| Feld | |
|---|---|
| **Default Model** | Der [Inferenz-Anbieter](inference-providers.md), mit dem neue Bücher starten. |
| **Default Protocol** | Das [Protokoll](protocols.md), mit dem neue Bücher starten. |
| **Default model for annotating**, **Default protocol for annotating** | Das Modell, das [einen Teil annotiert](books/annotations.md#letting-a-model-annotate), wenn du für den Lauf kein anderes auswählst. Leer bedeutet das Modell des Buches. |
| **Backup Strategy** | Automatische [Buchsicherungen](books/backups.md#automatic-backups) für Bücher ohne eigene Strategie: *No Backups*, *After N Messages* oder *After N Minutes*, mit der Zahl daneben. |
| **Versions kept per part** | Wie viele frühere Fassungen des Textes eines Teils der [Verlauf](books/version-history.md#how-many-versions-are-kept) für alle deine Bücher behält, sofern nicht ein Ordner, ein Buch oder der Administrator das stärker begrenzt. Leer setzt hier kein Limit; **0** behält keinen Verlauf. |
| **Parts loaded at once in the story** | Wie viele Teile eines Buches die [Story](books/story-editor.md) und der [Viewer](books/publishing.md) geladen halten. Die Story beginnt mit den letzten Teilen, der Viewer dort, wo du zuletzt aufgehört hast (oder mit den ersten Teilen); die Teile davor und danach werden beim Scrollen geladen, und die weit entfernten werden wieder entladen. Leer lädt alle Teile auf einmal. |
| **Daily goal: words written**, **Daily goal: minutes of editing** | Das Tagesziel für den Kalender der [Schreibstatistik](books/statistics.md#the-daily-goal): die Wörter, die du selbst schreibst, und die Minuten, die du überarbeitest, über alle deine Bücher. Leer bedeutet kein Ziel. |
| **Record writing statistics**, **Keep statistics for (months)** | Ob deine [Schreibstatistik](books/statistics.md#privacy-clearing-not-recording-keeping-less) erfasst wird und wie viele Monate davon aufbewahrt werden (leer oder 0: alle). Ein Administrator kann die Erfassung für alle abschalten und eine kürzere Dauer festlegen. |

*Default Model* und *Default Protocol* werden nur beim Anlegen eines Buches verwendet; sie zu ändern, ändert keine bestehenden Bücher. Die Erweiterung [Lore Generator](extensions/lore-generator.md) hat kein Buch und generiert mit ihnen. Die Sicherungsstrategie gilt für alle Bücher ohne *Per manuscript override*, ab ihrem nächsten erzeugten Teil.
### Templates

Die Standardwerte für die [Buch-Prompts](books/prompts.md):

| Field | |
|---|---|
| **Prompt language** | Die Sprache der eingebauten Prompts: Englisch (der Standard), Japanisch, Französisch, Spanisch, Deutsch, Italienisch, Koreanisch oder Chinesisch, wenn die [chinesische Lokalisierung](extensions/chinese-localization.md) geladen ist. Siehe unten [Prompt-Sprache](#prompt-language). |
| **Master Template** | Der Rahmen des Prompts, siehe [Master-Template](books/prompts.md#master-template). |
| **Point of View (POV)**, **Tense** | Die Erzählperspektive und die Zeitform. |
| **Style** | Richtlinien für Stil und Ton. |
| **Default User Prompt** | Die Anfrage für jeden Teil, siehe [User-Prompt](books/prompts.md#user-prompt). |
| **Default Summary Prompt** | Die Anfrage für eine [Zusammenfassung](books/summaries.md#the-summary-prompt). |
| **Default Meta Summary Prompt** | Die Anfrage für eine [Meta-Zusammenfassung](books/summaries.md#the-meta-summary-prompt), die Zusammenfassungen zusammenführt. |
| **Redraft System Prompt**, **Redraft Prompt** | Die Systemnachricht und die letzte Nachricht eines [Neuentwurfs](books/story-editor.md#redrafting-a-part) eines Teils, siehe [Redraft-Prompt](books/prompts.md#redraft-prompt). |
| **Annotate System Prompt**, **Annotate Prompt** | Die Systemnachricht und die letzte Nachricht der Anfrage an ein Modell, einen Teil zu [annotieren](books/annotations.md#letting-a-model-annotate), siehe [Annotate-Prompt](books/prompts.md#annotate-prompt). |
| **Annotate Instructions** | Wonach das Modell beim Annotieren sucht, für die Bücher und Ordner, die nichts festlegen, siehe [Annotate-Prompt](books/prompts.md#annotate-prompt). |
| **Impersonate Prompt** | Die Anfrage an ein Modell, die [Anweisungen](books/story-editor.md#drafting-the-instructions) des nächsten Zugs an deiner Stelle zu entwerfen, siehe [Impersonate-Prompt](books/prompts.md#impersonate-prompt). |
| **Compaction prompt of agents** | Nur für Nutzer, die [Agenten](agents.md) verwenden dürfen. Was deine Agenten gefragt werden, wenn du sie [`/compact`](agents.md#running-a-command-yourself) lässt, falls ihr eigener Compaction-Prompt leer ist; leer heißt hier der von deinem Administrator gesetzte, sonst der eingebaute. |
| **Instructions of agents (agents.md)** | Nur für Nutzer, die [Agenten](agents.md) verwenden dürfen. Deine Anweisungen für die Agenten, die keine eigenen haben; sie werden bei jedem Lauf zu ihrem Prompt hinzugefügt. Leer heißt keine. |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | Nur für Nutzer mit der Berechtigung [*Notifications*](administration/permissions.md) (und nicht nur für die, die [Agenten](agents.md) verwenden), im Tab *General*, und nur wenn dein Administrator es erlaubt. Wohin eine kurze Nachricht gesendet wird, wenn ein Agent etwas fragt, auf dich wartet, fertig wird oder fehlschlägt und du die Anwendung geschlossen hast: siehe [Wenn du nicht da bist](agents.md#when-you-are-not-there). |
| **System prompt of agents** | Nur für Nutzer, die [Agenten](agents.md) verwenden dürfen. Was deinen Agenten als Rolle vorgegeben wird, wenn ihr eigener System-Prompt leer ist; leer heißt hier der von deinem Administrator gesetzte, sonst der eingebaute. Reiner Text, kein Template. |

![Der Tab Templates](../images/settings-templates.png)

Weiter unten stehen, für Nutzer, die Agenten verwenden dürfen, die Prompts der Agenten:

![Die Prompts der Agenten im Tab Templates](../images/settings-templates-agents.png)

Jedes Feld zeigt den eingebauten Standard in Grau, solange es leer ist, und hat das Hinweis-Popover mit
**Insert Default Template** und den verfügbaren [Variablen und Makros](templates/index.md).

<a id="prompt-language"></a>

#### Prompt-Sprache

**Prompt language** legt die Sprache der eingebauten Prompts fest: was ein leeres Feld bedeutet (der graue Text ändert sich, sobald du eine andere Sprache wählst) und damit, was an das Modell für Generierung, Zusammenfassungen, Neuentwurf, Annotationen und Impersonate geschickt wird. Die Prompts der Plugins mit eigenen Prompts (Arc Outline, Beta Readers, Book Description Generator, Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) und der Agenten folgen ihr ebenfalls. Sie ist unabhängig von der Sprache der Oberfläche: Du kannst die Anwendung auf Englisch verwenden und japanische Prompts senden. Ein Modell befolgt Anweisungen am besten in der Sprache der Geschichte, also wähle die Sprache, in der deine Bücher geschrieben sind. Nur die eingebauten Prompts ändern sich: Ein Prompt, den du selbst geschrieben hast, hier oder in einem Buch oder Ordner, bleibt, wie er ist. Ein [Agent](agents.md) liest seine Prompts und Hinweise in seiner eigenen Sprache (*Language* in den Einstellungen des Agenten), nicht in dieser.

Diese Standardwerte gelten sofort für **jedes Buch, dessen eigenes Feld leer ist** – nicht nur für neue Bücher. Ein Buch, das auf seinem Tab *Prompts* einen eigenen Wert setzt, behält ihn. Also:

- lege hier fest, was alle deine Bücher gemeinsam haben;
- lege auf dem Tab *Prompts* eines Buchs fest, was nur für dieses gilt;
- leere das Feld eines Buchs, um es auf den Standard aus *Settings* zurückzusetzen.

<a id="styles"></a>

### Stile

Die Textstile deiner Bücher: wie der Text der Teile in der Geschichte und im Leser aussieht. Der Tab listet die Stile **Normal** und **Book**, die die Anwendung mitbringt und die nicht geändert werden können (sie sind als *built in* markiert), sowie deine eigenen. Wähle einen Stil aus, um ihn rechts zu bearbeiten; die Schaltflächen unter der Liste:

- **Duplicate** – erstellt aus dem gewählten Stil einen eigenen (auch aus einem eingebauten: So fängst du an), benannt *... (copy)*, den du dann umbenennen und ändern kannst;
- **Delete** – löscht einen deiner Stile; ein eingebauter Stil kann nicht gelöscht werden;
- **Set as default** – macht den Stil zu dem, den die Bücher verwenden, sofern sie im Einstellungsmenü der Geschichte nicht [einen anderen wählen](books/story-editor.md#text-styles). Der Standardstil ist in der Liste als *default* markiert.
- **Export** und **Import** – speichern den gewählten Stil als `.style.json`-Datei und fügen die Stile einer solchen Datei deinen eigenen hinzu (nicht gespeichert, bis du die Einstellungen speicherst). Siehe [Einen Stil teilen](books/styling-guide.md#share-a-style-export-and-import).

Der [Leitfaden zum Styling](books/styling-guide.md) geht jede Option durch und enthält einen Beispielstil zum Herunterladen.

![Der Tab Styles mit dem Horror-Stil aus dem Leitfaden zum Styling ausgewählt](../images/settings-styles.png)

Bei einem eigenen Stil hat das Formular rechts ein **Name**, das Kästchen **For exports (not offered for books)** und zwei Tabs. Ein Stil ist entweder für Bücher oder für Exporte: Ein Stil für Exporte wird im [Exportdialog](books/export.md#print-styles) angeboten (die Anwendung bringt vier mit, das Aussehen der HTML-, EPUB-, PDF- und Word-Exporte) und kann nicht der Standardstil der Bücher sein; ein Stil für Bücher wird im Exportdialog nicht angeboten. Die Liste markiert sie mit *(export)*.

- **Options** – in Gruppen: *Text* (Schrift, Größe, Zeilenhöhe, Buchstaben- und Wortabstand, Farbe, Ausrichtung, Silbentrennung, der Abstand zwischen Absätzen, der **first line indent** der Absätze und ein **drop cap** nach Überschriften), *Page* (die Breite der Textspalte, zentriert, und die **background color**, die die ganze Fläche hinter dem Text füllt), *Emphasis, links and code* (die Farben von kursivem und fettem Text und von Links, die Schrift von Code), *Quotes*, *Headings* (Schrift, Größe der ersten Ebene, Farbe, Ausrichtung, Kapitälchen) sowie Symbol und Farbe des *Scene break* (`---`). Ein Feld ist ein beliebiger CSS-Wert (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`); ein leeres Feld behält das Aussehen der Anwendung bei. Die Schriftfelder bieten gängige Schriftstapel an und nehmen auch deine eigenen.
- **Custom CSS** – eigenes CSS, das nach den Optionen hinzugefügt und innerhalb des Stils angewendet wird, sodass eine Regel, die du schreibst, nur für den Text mit diesem Stil gilt: `.chat-message-markdown p { text-indent: 1.5em !important; }` rückt Absätze ein, einfache Deklarationen (`font-size: 1.2rem;`) gelten für den ganzen Text. Darunter zeigt **Generated CSS** das Stylesheet, das die Optionen erzeugen (nur lesbar). Die geschweiften Klammern `{ }` müssen paarweise stehen, sonst wird der Stil nicht gespeichert.

![Der Tab Custom CSS eines Stils](../images/settings-styles-css.png)

Unter dem Formular zeigt die **Preview** einen Beispieltext im Stil und folgt jeder Änderung beim Tippen.

Wie die anderen Tabs wird auch der Tab Styles mit **Save** der Einstellungen gespeichert, und **Discard Changes** verwirft auch seine Änderungen: Duplizieren, Bearbeiten, Löschen und Setzen des Standards ändern nur die Arbeitskopie, und *Unsaved changes* (und das Schloss) erscheint, sobald du eines davon tust. Ein Stil ohne Namen oder Custom CSS mit nicht zusammenpassenden geschweiften Klammern wird beim Speichern abgelehnt.
<a id="exports"></a>

### Exporte

Womit jedes Dateiformat exportiert wird: Der Reiter hat einen Abschnitt für jedes Format, das sich etwas einstellen lässt (**Plain text**, **Markdown**, **HTML**, **Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault**; klicken Sie auf den Namen, um ihn zu öffnen).

- **Print style** (HTML, Word, PDF und EPUB, die Formate mit Schriften und Farben) - der Stil, den der [Exportdialog](books/export.md) für das Format vorauswählt. Er beginnt mit dem Aussehen, das die Anwendung für das Format mitbringt (**HTML page**, **PDF book**...). Die Liste enthält die [Stile für Exporte](#styles), die mitgelieferten und Ihre eigenen. Wählen Sie einen anderen, um dieses Format künftig immer damit zu exportieren, bis Sie die Wahl im Dialog für einen einzelnen Export ändern.
- **Front matter** (Markdown with front matter und Obsidian vault) - der YAML-Block am Anfang der Datei mit `{{title}}`, `{{author}}`, `{{language}}` und `{{exportDate}}`. Leer ist der Standardblock.
- **Title page** (jedes Format außer den Formaten für andere Werkzeuge; der Obsidian vault hat sie in der Notiz des Buchs) - die Vorlage der Titelseite, eine Handlebars-Vorlage, die Markdown schreibt, mit `{{title}}` und `{{author}}` (das Info-Symbol im Feld listet sie auf). Leer ist die Standardvorlage: der Titel als Überschrift und darunter der Autor kursiv. **Fill default** im Info-Popover übernimmt den Standard als Ausgangspunkt.
- **Authorship statement** (jedes Format außer dem SillyTavern-Chat) - der Text, den das Kontrollkästchen *Include authorship statement* des Exports am Ende anfügt: `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`, `{{generatedPercent}}` und `{{models}}` (die Modelle durch Kommas getrennt, leer, wenn es keines gibt). Leer ist der Standardtext.

Dies sind Ihre Versionen: Ein Buch oder ein Ordner kann eigene haben, siehe [Titelseitenvorlage](books/export.md#title-page-template). Der Reiter wird mit **Save** der Einstellungen gespeichert, und **Discard Changes** verwirft seine Änderungen, wie bei den anderen. Eine Vorlage, die nicht gültig ist, wird beim Speichern abgelehnt. Ein Stil, den Sie im Reiter Styles anlegen, lässt sich hier wählen, nachdem Sie gespeichert haben.

![Der Reiter Exports mit geöffnetem Abschnitt HTML](../images/settings-exports.png)

### Tags

Eine Tabelle aller Ihrer Tags (der Tags, die Sie Büchern, Lorebooks und Lorebook-Einträgen geben) mit der Anzahl der Objekte, mit denen jeder Tag verknüpft ist. Klicken Sie auf eine Spaltenüberschrift, um danach zu sortieren. Anders als die anderen Reiter wirkt er sofort: Er gehört nicht zu den ungespeicherten Änderungen und hat nichts zu speichern.

![Der Reiter Tags](../images/settings-tags.png)

| Schaltfläche | |
|---|---|
| **Rename** (Stift) | Fragt nach dem neuen Namen des Tags; jedes Objekt behält den Tag unter dem neuen Namen. |
| **Delete** (Papierkorb) | Fragt nach einer Bestätigung und löscht dann den Tag, wobei er von allen Objekten entfernt wird, die ihn haben. |

Ist der neue Name der Name eines anderen Tags (Namen werden ohne Beachtung der Groß- und Kleinschreibung verglichen), würden die beiden Tags durch das Umbenennen gleich. Deshalb werden Sie gefragt, ob Sie sie **zusammenführen** möchten. Zusammenführen verschiebt alles, was mit dem umbenannten Tag markiert ist, auf den vorhandenen Tag und löscht den umbenannten. Ein Objekt, das beide Tags hat, behält am Ende den vorhandenen Tag einmal. Mit Cancel passiert nichts, und der Tag behält seinen alten Namen.

![Nachfrage, ob zwei Tags zusammengeführt werden sollen](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Tastenkürzel

Die Tasten der [Tastenkürzel des Story-Editors](books/story-editor.md#keyboard-shortcuts) (sie funktionieren im Story-Editor am Computer; das [Layout für das Telefon](phone.md) hat keine).
Die Tabelle listet jede Aktion auf, auch die, die [Erweiterungen](extensions/index.md) mitbringen, mit ihrer Taste, sowie die Tasten der Schaltflächen des [Organise](books/folders.md#organise)-Fensters der Buchliste. Jeder Eintrag des Einstellungsmenüs der Story, des Menüs eines Teils und des Kontextmenüs (Rechtsklick) auf ihren Text ist hier eine Aktion (die meisten haben keine Taste, bis Sie ihnen eine geben). Die Menüs zeigen die Taste neben dem Eintrag an.

| Schaltfläche | |
|---|---|
| **Record** | Drücken Sie die neue Tastenkombination (Modifikatortasten zuerst oder zusammen, z. B. `Ctrl+Shift+K`); ein Klick irgendwo anders bricht ab. |
| **Reset to default** | Stellt die Standardtaste der Aktion wieder her. |
| **Clear** | Die Aktion hat keine Taste. |

Ein roter Hinweis nennt eine Taste, die zwei Aktionen teilen, eine Taste, die der Browser für sich behält (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` und ähnliche; die Seite bekommt sie nie), und `Alt` zusammen mit einem Buchstaben (tippt auf manchen Tastaturen, z. B. macOS, ein Zeichen). Eine Taste ohne `Ctrl`, `Alt` oder `Meta` funktioniert nur, wenn kein Textfeld den Fokus hat. Die Tasten werden mit **Save** wie die anderen Einstellungen gespeichert und gelten sofort in den geöffneten Storys.

![Einstellungen, Tastenkürzel](../images/settings-shortcuts.png)

<a id="notifications"></a>

### Benachrichtigungen

Eine Desktop-Benachrichtigung des Browsers, sobald ein Modell fertig ist, damit Sie den Reiter verlassen können, während es arbeitet. Der Reiter hat ein Kontrollkästchen **Use desktop notifications**, die Schaltflächen darunter und eine Liste, was benachrichtigt:

- **Story generation**, **Annotate with AI**, **Annotate with AI (automatic)**, **Redraft**, **Redraft (automatic)**, **Draft Instructions**, **Summary**, **Resummarize all summaries** und **Help assistant** sowie das, was [Erweiterungen](extensions/index.md) hinzufügen, benannt nach der Erweiterung (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...). Die mit *(automatic)* markierten Einträge des *Manga Generator* sind dessen kontinuierlicher Modus.
- Die mit *(automatic)* markierten Einträge sind die Läufe, die das Buch von selbst startet: die [automatische Annotation](books/story-editor.md) nach einer Generierung und der darauf folgende Redraft. Sie werden getrennt von dem Dialog gewählt, den Sie selbst öffnen. Ist **Annotate with AI** angehakt und **Annotate with AI (automatic)** nicht, bleibt die automatische Annotation stumm. Ist nur der automatische Eintrag angehakt, benachrichtigt nur die automatische Annotation.
- Ein Lauf, der mit einem Fehler endet, benachrichtigt ebenfalls (*… failed*). Einer, den Sie selbst abbrechen, tut es nicht.

Das Kontrollkästchen schaltet Benachrichtigungen für Ihr Konto ein und aktiviert zwei Schaltflächen. **Register this browser** fragt den Browser nach der Erlaubnis (aus diesem Klick heraus). Die Zeile unter den Schaltflächen zeigt, was dieser Browser sagt: erlaubt, blockiert (erlauben Sie es in den Website-Einstellungen des Browsers) oder noch nicht gefragt. Die Erlaubnis gehört zum Browser, nicht zu Ihrem Konto. Jeder Browser oder jedes Gerät, in dem Sie Benachrichtigungen möchten, muss einmal registriert werden; das Kontrollkästchen und die Liste sind überall gleich. **Send a test notification** zeigt sofort eine an, auch wenn die Seite im Vordergrund ist.

Das Feld **Filter** engt die Liste nach Namen ein. **Check shown** und **Uncheck shown** wirken auf die Einträge, die es zeigt, und lassen die anderen, wie sie sind. Eine Benachrichtigung wird nur angezeigt, während Sie die Seite nicht ansehen (ein anderer Reiter oder das Fenster ist nicht im Vordergrund). Ein Klick darauf holt die Seite zurück.

Benachrichtigungen brauchen eine Adresse mit `https://` oder `localhost`: Auf einer einfachen `http://`-Adresse eines Servers in Ihrem Netzwerk bietet der Browser sie nicht an, und der Reiter sagt das. Ein Reiter, den der Browser in den Ruhezustand versetzt hat (meist auf einem Telefon), kann sie ebenfalls nicht anzeigen. Der Reiter wird mit **Save** der Einstellungen gespeichert, wie die anderen.

<a id="phrase-watch-list"></a>

### Phrasen-Beobachtungsliste

Ihre [Phrasen-Beobachtungsliste](books/annotations.md#watching-for-phrases), die jedes Buch verwendet (Ordner und Bücher fügen ihre eigenen hinzu), und die **Annotationsvorlagen**, aus denen ihre Einträge und die Einträge der Ordner und Bücher wählen: was die Notiz zu einer gefundenen Phrase sagt, ihr Typ und ob sie an das Modell übergeben wird. Wird mit **Save** der Einstellungen gespeichert, wie die anderen Reiter.

![Die Phrasen-Beobachtungsliste in den Einstellungen](../images/settings-phrase-watch.png)
<a id="api-keys"></a>

### API-Schlüssel

Schlüssel für Programme und Skripte, die Marginalia über seine HTTP-API (`/api/v1`) nutzen. Ein Programm sendet den Schlüssel im Header `Authorization: Bearer <key>` und darf alles, was Sie selbst in Ihren Büchern tun können. Nur für Administratoren und Benutzer mit der Berechtigung [*API access*](administration/permissions.md); ohne sie weist der Reiter darauf hin.

![Der Reiter API-Schlüssel](../images/settings-api-keys.png)

![Ein neuer Schlüssel, der nur einmal angezeigt wird](../images/api-key-created.png)

**New API key** fragt nach einem **Name** (wofür der Schlüssel verwendet wird, damit Sie wissen, welchen Sie widerrufen müssen), dem **Access** und danach, wann er **Expires** läuft (nach einer Anzahl von Tagen, oder nie, wenn das Feld leer bleibt). Der Zugriff ist eine beliebige Kombination aus:

| Access | |
|---|---|
| **Read** | Ihre Bücher und die Befehle, die nur lesen. |
| **Write** | Jeder Befehl, auch die, die ändern oder löschen; schließt *Read* ein. |
| **Update the installation** | Nur für Administratoren: das [Update](administration/updates.md) der Installation (dessen Status, dessen Protokoll, Starten). |
| **Manage the extensions** | Nur für Administratoren: Installieren, Aktualisieren, Anhalten und Entfernen von [Erweiterungen](administration/extensions.md). |

Jeder Schlüssel kann die Version von Marginalia lesen. Geben Sie einem Schlüssel nur, was sein Programm braucht: Ein Skript, das Erweiterungen installiert, braucht nur *Manage the extensions*, und ein solcher Schlüssel kann Ihre Bücher nicht anrühren. Das Update und die Erweiterungen funktionieren für einen Schlüssel nicht mehr, sobald sein Besitzer kein Administrator mehr ist; der übrige Zugriff bleibt bestehen.

Der Schlüssel wird direkt nach dem Erstellen einmal angezeigt. Kopieren Sie ihn dann, denn Marginalia speichert nur einen Fingerabdruck davon und kann ihn nicht erneut anzeigen. Die Liste zeigt Ihre Schlüssel mit dem Anfang jedes Schlüssels, dem Zeitpunkt der Erstellung, dem Ablaufdatum und dem Zeitpunkt der letzten Nutzung. Die Papierkorb-Schaltfläche **widerruft** einen Schlüssel, und die Programme, die ihn verwenden, verlieren sofort den Zugriff. Ein Schlüssel funktioniert auch nicht mehr, wenn er abgelaufen ist oder wenn ein Administrator die Berechtigung entzieht. Die Schlüssel werden beim Erstellen oder Widerrufen gespeichert, nicht durch **Save** der Einstellungen.

**Die API-Seite**: Öffnen Sie `/api/v1/` Ihres Marginalia in einem Browser (zum Beispiel `https://marginalia.example.org/api/v1/`), um jeden Aufruf, seine Argumente und seine Antworten zu sehen. Fügen Sie oben unter **API key** einen Schlüssel ein. Die Seite listet dann auch jeden Befehl auf, den dieser Schlüssel ausführen darf, jeweils mit seinen Argumenten. Mit **Try it out** → **Execute** senden Sie den Aufruf mit diesem Schlüssel und sehen die Antwort. Die Aufrufe sind echt: Ein Befehl ändert Ihr Buch, und ein Update startet den Server neu. Die Seite behält den Schlüssel nur, solange der Tab offen ist, und speichert ihn nie auf der Festplatte.

Für Entwickler sind die Aufrufe im [Entwicklerhandbuch](../developer/api.md) beschrieben.

<a id="extension-settings"></a>

### Erweiterungseinstellungen

Einstellungen der installierten [Erweiterungen](extensions/index.md), mit einem Abschnitt pro Erweiterung (zum Beispiel *Reviewer Settings* und *SideQuery Settings*). Gespeichert werden sie mit demselben **Save**-Button. Der Reiter ist leer, wenn keine Erweiterung mit Einstellungen installiert ist.

![Der Reiter Extension Settings mit den Einstellungen des Lore Generators](../images/settings-extensions.png)
