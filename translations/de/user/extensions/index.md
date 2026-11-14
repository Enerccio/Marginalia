---
label: Erweiterungen
order: 930
icon: plug
---

# Erweiterungen

Erweiterungen ergänzen Marginalia um Funktionen, ohne die Anwendung selbst zu verändern. Marginalia wird mit dreiundzwanzig Erweiterungen ausgeliefert:

| Erweiterung | Was sie ergänzt |
|---|---|
| [Arc Outline](arc-outline.md) | Die Handlungsbögen des Buches und ihre Kapitel mit ihrem Status in der Seitenleiste; das Modell kann einen Bogen für dich schreiben. |
| [Anthropic Claude](anthropic.md) | Die Claude-Modelle von Anthropic als Inferenzanbieter, über die eigene API von Anthropic, mit den Zusammenfassungen des Denkens als Begründung angezeigt. |
| [Audiobook](audiobook.md) | Verwandelt ein Buch mit einem Sprachserver in ein Hörbuch: eine Stimme für den Erzähler und für jede Figur, ein Studio für die endgültige Fassung, Export als mp3 mit Kapitelmarken. [Einrichten des Sprachservers](audiobook-server.md). |
| [Author's Note](authors-note.md) | Eine Notiz an das Modell, die in jede Generierung des Buches eingefügt wird. |
| [Beta Readers](beta-readers.md) | Erfundene Leser (Personas) und ein Chat mit ihnen über das Buch, allein oder in einer Gruppe. |
| [Book Description Generator](book-description-generator.md) | Schreibt mit dem Modell den Namen und die Beschreibung des Buches aus einem Bereich seiner Geschichte, über die Schaltfläche des Reiters About. |
| [Chapter Marker](chapter-marker.md) | Kapiteltitel in der Gliederung der Geschichte, aus Markdown-Überschriften in den Teilen. |
| [Character Tracker](character-tracker.md) | Die Figuren des Buches mit ihren Beziehungen und eine Karte von ihnen. |
| [Conlang Translator](conlang-translator.md) | Die Rede deiner Figuren in einer konstruierten Sprache, die du beschreibst, mit dem Englischen dahinter für die Modelle und einem Klick für die Leserin bzw. den Leser. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Lässt Funktionsaufrufe mit Gemma-4-Modellen funktionieren, deren API für Werkzeugergebnisse eine andere Rolle verlangt. |
| [Google Gemini](google-genai.md) | Die Gemini-Modelle von Google als Inferenzanbieter, über die eigene API von Google, mit den Zusammenfassungen des Denkens als Begründung angezeigt. |
| [Image Generator](image-generator.md) | Erstellt ein Bild für einen Teil der Geschichte: Das Modell schreibt den Prompt, ComfyUI oder eine API für Bilder im OpenAI-Stil zeichnet es, du würfelst neu und übernimmst es. |
| [Prompt History](prompt-history.md) | Behält die letzten 20 Anfragen an die Modelle in deiner Sitzung (und pro Agentenlauf) mit den Antworten und der Begründung, zum Debuggen von Erweiterungen und zur Prüfung von Prompts; geöffnet über Einstellungen → Erweiterungseinstellungen oder die Konsole eines Agenten. |
| [Prompt Inspector](prompt-inspector.md) | Zeigt den Prompt einer Generierung und fragt nach, bevor er an das Modell gesendet wird; ein- und ausgeschaltet im Einstellungsmenü der Geschichte. |
| [Instruction Exporter](instruction-exporter.md) | Kopiert die Anweisungen, Szenenvorgaben und Figuren der Geschichte in die Zwischenablage. |
| [Knowledge Tracker](knowledge-tracker.md) | Was jede verfolgte Figur weiß und nicht weiß, in den Prompt eingefügt, damit das Modell den Figuren kein Wissen mehr gibt, das sie nicht haben können. |
| [Lore Generator](lore-generator.md) | Schreibt mit dem Modell den Inhalt eines Lorebook-Eintrags aus dem Wissen, das du bereits hast. |
| [Lorebook VCS](lorebook-vcs.md) | Versionsverlauf für Lorebook-Einträge. |
| [Manga Generator](manga-generator.md) | Verwandelt Teile der Geschichte in ein Manga-Skript aus Seiten und Panels sowie in Tags für die Bildgenerierung. |
| [Tools for the model](tools.md) | Die Schaltfläche **Tools** der Generatoren: welche Werkzeugsammlungen (Namen, Orte) das Modell verwenden darf und welche ihrer Kategorien. |
| [Name generator](name-generator.md) | Namen, die das Modell verwenden soll, statt sie zu erfinden: etwa 1270 Namenslisten als Markow-Ketten. |
| [Name provider](name-provider.md) | Echte Namen, aus denen das Modell auswählen kann: Vor- und Nachnamen aus etwa 100 Ländern, in der Originalschreibweise. |
| [Place provider](place-provider.md) | Echte Orte, die das Modell nachschlagen kann: Städte, Meere, Buchten, Flüsse, Berge und mehr mit Koordinaten und Ländern, was um einen Ort herum liegt und die Entfernung zweier Orte. |
| [Real Language Translator](real-language-translator.md) | Die Rede deiner Figuren in einer echten Sprache (Japanisch für eine Geschichte in Japan), mit dem Englischen dahinter für die Modelle und einem Klick für die Leserin bzw. den Leser. |
| [Reviewer](reviewer.md) | Besprechungen von Geschichtsteilen, die vom Modell geschrieben wurden: Reaktionen von Lesern, Kritik. |
| [Side Query](side-query.md) | Ein Chat mit dem Modell über dein Buch, neben der Geschichte. |
| [Converter](sillytavern-converter.md) | Ein SillyTavern-Chat neben der Geschichte und Details zu Wendungen, die das Modell daraus vorbereitet, um den Chat als Geschichte neu zu schreiben; oder die Teile eines älteren Buches (aus einer Sicherung oder dem Buch selbst) mit ihren Anweisungen, um es neu zu entwerfen. |
| [Story Notes](story-notes.md) | Ein kleines Wiki aus eigenen Seiten (Schlagwörter, Verknüpfungen zwischen ihnen) in der Seitenleiste, für Handlungen, Ideen und Regeln der Welt; der Handlungsassistent und Agenten können die Seiten lesen und ändern, die du freigibst. |
| [Read Aloud](tts-reader.md) | Liest ein veröffentlichtes Buch im Viewer mit der Stimme deines Geräts vor. |
| [Chinese localization](chinese-localization.md) | Vereinfachtes Chinesisch als Sprache der Benutzeroberfläche. |

<a id="installing"></a>

## Installieren

Erweiterungen werden von einem **Administrator** für die gesamte Marginalia-Installation installiert, unter *Admin → Extensions*:
Sie sind JAR-Dateien, die mit **Erweiterung laden (.jar)** hochgeladen werden. Einzelheiten stehen unter [Erweiterungen verwalten](../administration/extensions.md).

Once installed, an extension is available to every user. An extension loaded while Marginalia runs shows up in
windows opened afterwards: close and reopen a book (or reload the page) to see it.

Nach der Installation steht eine Erweiterung jedem Benutzer zur Verfügung. Eine Erweiterung, die geladen wird, während Marginalia läuft, erscheint in danach geöffneten Fenstern: Schließe ein Buch und öffne es erneut (oder lade die Seite neu), um sie zu sehen.

!!!warning Passende Versionen verwenden
Erweiterungen greifen in die Benutzeroberfläche einer bestimmten Marginalia-Version ein. Eine Erweiterung, die für eine andere Version gebaut wurde, kann ihre Funktion verlieren oder Teile der Anwendung beschädigen. Verwende die Erweiterungen, die mit deiner Version von Marginalia mitgeliefert werden.
!!!

Die oben genannten Erweiterungen sind Teil des Quellcodes von Marginalia (`marginalia/plugins`) und werden getrennt gebaut;
siehe die [README der Plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

<a id="extensions-on-a-phone"></a>

## Erweiterungen auf dem Smartphone

Das [Layout für das Smartphone](../phone.md) hat eigene Klassen, daher muss eine Erweiterung es unterstützen. Arc Outline, Author's Note, Beta Readers, Chapter Marker,
Character Tracker, Instruction Exporter und Reviewer tun das; Side Query, Lore Generator und Lorebook VCS fügen auf dem Smartphone nichts hinzu (siehe [Marginalia auf dem Smartphone verwenden](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix und Google Gemini haben keine eigenen Bildschirme (Google Gemini fügt nur einen Anbieter im Modelldialog hinzu, den auch ein Smartphone hat), und Chinese localization ist im Sprachauswahlfeld des Smartphone-Menüs. Erweiterungen werden über die Admin-Seite installiert, die ein Smartphone nicht hat.

<a id="extension-settings"></a>

## Einstellungen der Erweiterungen

Erweiterungen mit Einstellungen fügen einen Abschnitt unter **Einstellungen → Erweiterungseinstellungen** hinzu (der dritte Reiter der Seite *Einstellungen*).
Diese Einstellungen gehören zu deinem Konto, wie der Rest der *Einstellungen*, und werden mit deren Schaltfläche **Speichern** gespeichert.

<a id="where-extension-data-is-kept"></a>

## Wo die Daten der Erweiterungen liegen

Erweiterungen bewahren ihre Daten zusammen mit den Daten auf, zu denen sie gehören – Besprechungen beim Geschichtsteil, Gespräche beim Buch, Revisionen beim Lorebook – so sind sie in [Datenbanksicherungen](../administration/database-backups.md) enthalten. Ob sie auch Teil von [Buchsicherungen](../books/backups.md) und Exporten sind, wird auf der Seite der jeweiligen Erweiterung beschrieben.

Das Deinstallieren einer Erweiterung löscht ihre Daten nicht: Wird sie erneut installiert, sind die Daten wieder da.
