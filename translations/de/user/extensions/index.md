---
label: Erweiterungen
order: 930
icon: plug
---

# Erweiterungen

Erweiterungen ergänzen Marginalia um Funktionen, ohne die Anwendung selbst zu verändern. Marginalia bringt dreißig Erweiterungen mit:

| Erweiterung | Was sie ergänzt |
|---|---|
| [xAI Grok](grok.md) | Die Grok-Modelle von xAI als Inferenzanbieter, über die eigene API von xAI, wobei die Begründung getrennt von der Antwort angezeigt wird. |
| [Arc Outline](arc-outline.md) | Die Handlungsbögen des Buches und ihre Kapitel mit ihrem Stand in der Seitenleiste; das Modell kann für dich einen Bogen schreiben. |
| [Anthropic Claude](anthropic.md) | Die Claude-Modelle von Anthropic als Inferenzanbieter, über die eigene API von Anthropic, wobei die Zusammenfassungen des Denkens als Begründung angezeigt werden. |
| [Audiobook](audiobook.md) | Macht aus einem Buch ein Hörbuch mit einem Sprachserver: eine Stimme für den Erzähler und für jede Figur, ein Studio für die endgültige Fassung, Export als mp3 mit Kapitelmarken. [Einrichten des Sprachservers](audiobook-server.md). |
| [Author's Note](authors-note.md) | Eine Notiz an das Modell, die in jede Generierung des Buches eingefügt wird. |
| [Beta Readers](beta-readers.md) | Erfundene Leserinnen und Leser (Personas) und ein Chat mit ihnen über das Buch, allein oder in einer Gruppe. |
| [Book Description Generator](book-description-generator.md) | Schreibt mit dem Modell den Namen und die Beschreibung des Buches aus einem Ausschnitt seiner Geschichte, über die Schaltfläche im Reiter About. |
| [Chapter Marker](chapter-marker.md) | Kapiteltitel in der Handlungsübersicht, aus Markdown-Überschriften in den Teilen. |
| [Character Tracker](character-tracker.md) | Die Figuren des Buches mit ihren Beziehungen und eine Karte von ihnen. |
| [Conlang Translator](conlang-translator.md) | Die Rede deiner Figuren in einer konstruierten Sprache, die du beschreibst, wobei das Englische dahinter für die Modelle erhalten bleibt und ein Klick es für die Leserschaft zeigt. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Macht Funktionsaufrufe mit älteren Gemma-4-Modellen möglich, deren API für Werkzeugergebnisse eine andere Rolle verlangt; mit einer aktuellen Chat-Vorlage nicht nötig. |
| [Google Gemini](google-genai.md) | Die Gemini-Modelle von Google als Inferenzanbieter, über die eigene API von Google, wobei die Zusammenfassungen des Denkens als Begründung angezeigt werden. |
| [Image Generator](image-generator.md) | Erstellt ein Bild für einen Teil der Geschichte: Das Modell schreibt den Prompt, ComfyUI oder eine Bild-API im OpenAI-Stil zeichnet es, du würfelst neu und übernimmst das Ergebnis. |
| [Prompt History](prompt-history.md) | Behält die letzten 20 Anfragen an die Modelle in deiner Sitzung (und je Agentenlauf), mit den Antworten und der Begründung, zum Debuggen von Erweiterungen und zum Prüfen von Prompts; geöffnet über Settings → Extension Settings oder über die Konsole eines Agenten. |
| [Prompt Inspector](prompt-inspector.md) | Zeigt den Prompt einer Generierung und fragt nach, bevor er an das Modell geschickt wird; ein- und ausgeschaltet im Einstellungsmenü der Geschichte. |
| [Instruction Exporter](instruction-exporter.md) | Kopiert die Anweisungen, Szeneneinstellungen und Figuren der Geschichte in die Zwischenablage. |
| [Knowledge Tracker](knowledge-tracker.md) | Was jede verfolgte Figur weiß und nicht weiß, wird in den Prompt eingefügt, damit das Modell den Figuren kein Wissen mehr gibt, das sie nicht haben können. |
| [Lore Generator](lore-generator.md) | Schreibt mit dem Modell den Inhalt eines Lorebook-Eintrags aus dem vorhandenen Lore. |
| [Lorebook VCS](lorebook-vcs.md) | Versionsverlauf für Lorebook-Einträge. |
| [Manga Generator](manga-generator.md) | Macht Teile der Geschichte zu einem Manga-Skript aus Seiten und Panels und zu Tags für die Bildgenerierung. |
| [Tools for the model](tools.md) | Der Button **Tools** der Generatoren: welche Werkzeugsammlungen (Namen, Orte) das Modell verwenden darf und welche ihrer Kategorien. |
| [Name generator](name-generator.md) | Namen, die das Modell verwenden kann, statt sie zu erfinden: etwa 1270 Namenslisten als Markov-Ketten. |
| [Name provider](name-provider.md) | Echte Namen zur Auswahl für das Modell: Vor- und Nachnamen aus etwa 100 Ländern, in der Originalschreibweise. |
| [Place provider](place-provider.md) | Echte Orte, die das Modell nachschlagen kann: Städte, Meere, Buchten, Flüsse, Berge und mehr, mit Koordinaten und Ländern, dem Umfeld eines Ortes und der Entfernung zwischen zwei Orten. |
| [Real Language Translator](real-language-translator.md) | Die Rede deiner Figuren in einer echten Sprache (Japanisch für eine in Japan spielende Geschichte), wobei das Englische dahinter für die Modelle erhalten bleibt und ein Klick es für die Leserschaft zeigt. |
| [Reviewer](reviewer.md) | Besprechungen von Geschichtsteilen, vom Modell geschrieben: Leserreaktionen, Kritik. |
| [Side Query](side-query.md) | Ein Chat mit dem Modell über dein Buch, neben der Geschichte. |
| [Converter](sillytavern-converter.md) | Ein SillyTavern-Chat neben der Geschichte und die Angaben zu den Wendungen, die das Modell daraus vorbereitet, um den Chat als Geschichte umzuschreiben; oder die Teile eines älteren Buches (aus einem Backup oder aus dem Buch selbst) mit ihren Anweisungen, um es neu zu entwerfen. |
| [State Tracker](state-tracker.md) | Der aktuelle Zustand verfolgter Figuren (Kleidung, was sie bei sich tragen, die Waffe in der Hand, Verletzungen, Stimmung oder deine eigenen Kategorien), der in den Prompt eingefügt wird, damit das Modell sich daran hält. |
| [Story Notes](story-notes.md) | Ein kleines Wiki aus eigenen Seiten (mit Tags und Links untereinander) in der Seitenleiste, für Handlungen, Ideen und Regeln der Welt; der Handlungsassistent und Agenten können die Seiten lesen und ändern, die du freigibst. |
| [Read Aloud](tts-reader.md) | Liest ein veröffentlichtes Buch im Viewer mit der Stimme deines Geräts vor. |
| [Chinese localization](chinese-localization.md) | Vereinfachtes Chinesisch als Sprache der Benutzeroberfläche. |

<a id="installing"></a>

## Installation

Erweiterungen werden von einem **Administrator** für die gesamte Marginalia-Installation installiert, in *Admin → Extensions*:
Sie sind JAR-Dateien, die mit **Load Extension (.jar)** hochgeladen werden. Details dazu finden sich unter [Erweiterungen verwalten](../administration/extensions.md)
.

Once installed, an extension is available to every user. An extension loaded while Marginalia runs shows up in
windows opened afterwards: close and reopen a book (or reload the page) to see it.

Eine installierte Erweiterung steht allen Benutzerinnen und Benutzern zur Verfügung. Eine Erweiterung, die geladen wird, während Marginalia läuft, erscheint in danach geöffneten Fenstern: Schließe ein Buch und öffne es erneut (oder lade die Seite neu), um sie zu sehen.

!!!warning Passende Versionen verwenden
Erweiterungen greifen in die Benutzeroberfläche einer bestimmten Marginalia-Version ein. Eine Erweiterung für eine andere Version kann nicht mehr funktionieren oder Teile der Anwendung beschädigen. Verwende die Erweiterungen, die mit deiner Version von Marginalia mitgeliefert werden.
!!!

Die oben genannten Erweiterungen sind Teil des Quellcodes von Marginalia (`marginalia/plugins`) und werden separat gebaut;
siehe die [README der Plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

<a id="extensions-on-a-phone"></a>

## Erweiterungen auf dem Smartphone

Das [Layout für das Smartphone](../phone.md) hat eigene Klassen, daher muss eine Erweiterung es unterstützen. Arc Outline, Author's Note, Beta Readers, Chapter Marker,
Character Tracker, Instruction Exporter und Reviewer tun dies. Side Query, Lore Generator und Lorebook VCS ergänzen auf dem Smartphone nichts (siehe [Marginalia auf dem Smartphone verwenden](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix und Google Gemini haben keine eigenen Bildschirme (Google Gemini ergänzt nur einen Anbieter im Modelldialog, den auch ein Smartphone hat), und Chinese localization ist im Sprachauswahlfeld des Smartphone-Menüs enthalten. Erweiterungen werden über die Admin-Seite installiert, die ein Smartphone nicht hat.

<a id="extension-settings"></a>

## Erweiterungseinstellungen

Erweiterungen mit Einstellungen fügen einen Abschnitt unter **Settings → Extension Settings** hinzu (dem dritten Reiter der Seite *Settings*).
Diese Einstellungen gehören zu deinem Konto, wie der Rest von *Settings*, und werden mit dem Button **Save** gespeichert.

<a id="where-extension-data-is-kept"></a>

## Wo die Daten der Erweiterungen gespeichert werden

Erweiterungen bewahren ihre Daten zusammen mit den Daten auf, zu denen sie gehören: Besprechungen mit dem Geschichtsteil, Gespräche mit dem Buch, Überarbeitungen mit dem Lorebook. Daher sind sie in [Datenbank-Backups](../administration/database-backups.md) enthalten.
Ob sie auch Teil von [Buch-Backups](../books/backups.md) und Exporten sind, wird auf der Seite jeder Erweiterung beschrieben.

Das Deinstallieren einer Erweiterung löscht ihre Daten nicht: Wird sie erneut installiert, sind die Daten wieder da.
