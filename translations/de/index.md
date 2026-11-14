---
label: Startseite
icon: home
order: 1000
---

# Marginalia

Marginalia ist ein selbst gehostetes Schreibwerkzeug für lange Fiktion, die gemeinsam mit großen Sprachmodellen verfasst wird. Du schreibst die Anweisungen für jeden Teil der Geschichte, das Modell schreibt den Text, und Marginalia hält alles drumherum in Ordnung: den verzweigten Handlungsbaum, das Weltwissen, die Zusammenfassungen, die dafür sorgen, dass lange Bücher in den Kontext des Modells passen, und die Prompt-Einstellungen, die den Stil prägen.

Es läuft im Browser – als Desktop-App auf deinem eigenen Computer oder als kleiner Server für dich und deine Freunde – und arbeitet mit jeder OpenAI-kompatiblen API: OpenAI, OpenRouter, LiteLLM, llama.cpp, LM Studio, Ollama, vLLM und anderen.

![Der Geschichteneditor](images/hero-story-editor.png)

[!button variant="primary" text="Loslegen"](user/getting-started/index.md)
[!button text="Herunterladen"](https://github.com/Enerccio/Marginalia/releases)
[!button text="Quellcode auf GitHub"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## Was es kann

- **Bücher mit verzweigten Geschichten** – einen Teil neu generieren oder durchwischen, von jedem beliebigen Punkt aus verzweigen, im Handlungsbaum navigieren und den generierten Text bearbeiten. Siehe [Bücher](user/books/index.md).
- **Lorebooks** – Notizen zu Welt und Figuren, die nur dann in den Prompt aufgenommen werden, wenn sie relevant sind: nach Buch-Tags, Text- oder Regex-Filtern, verschachtelte Lorebooks. SillyTavern-Weltinformationen können importiert werden. Siehe [Lorebooks](user/lorebooks/index.md).
- **Vorlagen und Makros** – Prompts sind Handlebars-Vorlagen mit SillyTavern-kompatiblen Makros, sodass importiertes Weltwissen weiter funktioniert. Siehe [Vorlagen & Makros](user/templates/index.md).
- **Zusammenfassungen** – ältere Teile werden verdichtet, damit lange Bücher in das Kontextfenster passen. Siehe [Zusammenfassungen](user/books/summaries.md).
- **Jedes OpenAI-kompatible Modell** – einschließlich Reasoning-Modellen, mit modellspezifischen Grenzen und umschaltbaren Generierungseinstellungen. Siehe [Inferenzanbieter](user/inference-providers.md) und [Protokolle](user/protocols.md).
- **Sicherungen** – Sicherungen pro Buch mit Wiederherstellung und Kopie sowie vollständige Datenbanksicherungen nach Zeitplan. Siehe [Buch-Sicherungen](user/books/backups.md) und [Datenbanksicherungen](user/administration/database-backups.md).
- **Mehrere Benutzer** – jeder mit eigenen Büchern, eigenem Weltwissen und eigenen Modellen; Administratoren verwalten Benutzer, Sicherungen und Aufräumarbeiten. Siehe [Administration](user/administration/index.md).
- **Veröffentlichte Bücher** – eine schreibgeschützte Leseansicht, die du teilen kannst. Siehe [Veröffentlichung & Viewer](user/books/publishing.md).
- **Suchen und Ersetzen, Befehle** – Suchen und Ersetzen in der Geschichte und den Lorebooks sowie eine Befehlszeile neben dem Geschichtenmenü mit Befehlen, die das Buch lesen und verändern. Siehe [Befehle](user/commands/index.md) und [Reguläre Ausdrücke](user/regular-expressions.md).
- **Agenten** – ein Modell, das über die Befehle eigenständig an einem Buch arbeitet, und zwar an einer Kopie: Du siehst ihm zu, sprichst in einer Konsole mit ihm und prüfst seine Änderungen, bevor sie das Buch erreichen. Siehe [Agenten](user/agents.md).
- **Erweiterungen** – Plugins ergänzen zur Laufzeit Funktionen: Kapitelmarkierungen, Lorebook-Verlauf, KI-Überprüfungen, Nebenfragen an das Modell. Siehe [Erweiterungen](user/extensions/index.md).

<a id="which-guide-do-you-need"></a>

## Welche Anleitung brauchst du?

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[Benutzerhandbuch](user/index.md)**     | Du möchtest Marginalia installieren, es für andere betreiben oder damit schreiben. Beginne mit [Erste Schritte](user/getting-started/index.md). |
| **[Entwicklerhandbuch](developer/index.md)** | Du möchtest Marginalia aus dem Quellcode bauen, es verändern, eine Erweiterung schreiben oder verstehen, wie es intern funktioniert. |

<a id="quick-links"></a>

## Schnellzugriff

| Ich möchte...                  |                                                          |
|--------------------------------|----------------------------------------------------------|
| Es auf meinem Computer installieren | [Desktop-App](user/getting-started/desktop-app.md)       |
| Es auf einem Server betreiben  | [Server (Docker)](user/getting-started/docker-server.md) |
| Ein Modell anbinden            | [Inferenzanbieter](user/inference-providers.md)          |
| Den ersten Teil eines Buches schreiben | [Schnellstart](user/getting-started/quick-start.md) |
| Meine SillyTavern-Lorebooks verwenden | [Import & Export](user/lorebooks/import-export.md) |
| Ein Problem beheben            | [Fehlerbehebung & FAQ](user/troubleshooting.md)          |
| Eine Erweiterung schreiben     | [Plugin-Entwicklung](developer/plugins/index.md)         |
| Einen Fehler melden oder mitwirken | [Mitwirken](developer/contributing.md)               |

<a id="about"></a>

## Über

Marginalia ist Open Source unter der [MIT-Lizenz](https://github.com/Enerccio/Marginalia/blob/master/LICENSE).
Die Bilder in den Screenshots dieses Handbuchs sind auf der Seite [Danksagungen](credits.md) angegeben.
Releases gibt es auf [GitHub](https://github.com/Enerccio/Marginalia/releases); Fehler und Ideen gehen an den [Issue-Tracker](https://github.com/Enerccio/Marginalia/issues).
