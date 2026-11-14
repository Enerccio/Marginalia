---
label: Fehlerbehebung & FAQ
order: 900
icon: question
---

# Fehlerbehebung & FAQ

<a id="where-are-the-logs"></a>

## Wo sind die Logs?

Die meisten Probleme hinterlassen eine Spur im Log:

| Installation | Log |
|---|---|
| Desktop-App | `~/.marginalia/desktop/logs/marginalia.log` (unter Windows `%USERPROFILE%\.marginalia\desktop\logs`); der vorherige Lauf steht in `marginalia.log.1` |
| Docker | `docker compose logs -f marginalia` |

Fehlermeldungen in der Anwendung (*Interner Serverfehler*) zeigen den Fehler ebenfalls an; das Log enthält alle Details.

Wenn du ein Problem meldest, gib den Versionstext in der unteren linken Ecke der Seite an (zum Beispiel
`Marginalia 1.5.0 (93ec118)`: die Version und der Git-Commit des Builds).

<a id="installing-and-starting"></a>

## Installation und Start

**macOS sagt, die App sei „beschädigt“ oder „kann nicht geöffnet werden“.**
Die App ist noch nicht signiert. Klicke sie mit der rechten Maustaste an und wähle *Öffnen*, oder führe
`xattr -dr com.apple.quarantine /Applications/Marginalia.app` aus. Siehe [Desktop-App](getting-started/desktop-app.md).

**Windows SmartScreen blockiert die App.**
Klicke auf *Weitere Informationen* → *Trotzdem ausführen*.

**Der Browser öffnet einen anderen Port als 8765.**
Port 8765 war belegt, daher hat die Desktop-App einen freien Port gewählt. Verwende **Open Marginalia** im Tray-Menü
oder starte mit `--port`, um den Port festzulegen.

**„Marginalia did not start in time“.**
Die Desktop-App konnte ihren Server nicht starten. Das Log sagt, warum - oft hält ein anderes Programm die Dateien
fest oder eine defekte Erweiterung ist schuld (siehe [unten](#extensions)).

**Docker: Port 8080 ist bereits belegt.**
Ändere die linke Seite der Port-Zuordnung in `docker-compose.yml`, z. B. `"8081:8080"`, und öffne Port 8081.

**Marginalia ist langsam oder bricht mit „OutOfMemoryError“ ab.**
Erhöhe den Arbeitsspeicher: `-Xmx2g` (oder mehr) in `JAVA_TOOL_OPTIONS` in `docker-compose.yml`.

<a id="logging-in"></a>

## Anmeldung

**„Anmeldung speichern“ hält mich nicht angemeldet.**
Gespeicherte Anmeldungen benötigen HTTPS auf einem Server. Setze Marginalia hinter einen Reverse-Proxy mit HTTPS, siehe
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy).

**Ich habe mein Passwort vergessen.**
Ein Administrator kann ein neues setzen oder es leeren, damit du dich mit einem leeren Passwort anmelden und selbst
ein neues festlegen kannst - unter *Admin → Benutzer* (siehe [Passwort zurücksetzen](administration/users.md#resetting-a-forgotten-password)).
Es gibt kein Zurücksetzen per E-Mail.

**Niemand kann sich mehr als Administrator anmelden.**
Ohne Administrator kann das Passwort nur in der Datenbank zurückgesetzt werden:

1. Beende Marginalia und kopiere `marginalia.sqlite` aus dem [Datenordner](administration/index.md#the-data-folder) als
   Sicherung.
2. Entferne das Passwort des Administrators mit dem Werkzeug `sqlite3`:
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (verwende den richtigen Benutzernamen).
3. Starte Marginalia, melde dich mit diesem Benutzernamen und einem **leeren** Passwort an und setze sofort ein neues
   Passwort mit **Passwort ändern**.

**„Server connection lost“ oder die Seite lädt immer wieder neu.**
Hinter einem Reverse-Proxy muss der Proxy WebSockets weiterleiten und lange Anfragen zulassen - siehe das nginx-Beispiel in
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy). Bei der Desktop-App wurde die App vermutlich
beendet; starte sie neu.

**Der Arbeitsbereich friert den Browser ein, wenn er sich öffnet.**
Marginalia öffnet wieder, was du beim Verlassen geöffnet hattest. Daher kann ein sehr langes Buch (zum Beispiel eines aus
einem Archiv importiert) die Seite einfrieren, bevor du etwas ändern kannst. Öffne `/reset` unter deiner Marginalia-Adresse
(zum Beispiel `https://marginalia.example.com/reset`) und melde dich an: Der gespeicherte Zustand des Arbeitsbereichs
(geöffnete Bücher, ausgewählte Tabs, Filter) wird vergessen, und du landest in einem leeren Arbeitsbereich. Setze dann
**Auf einmal geladene Abschnitte in der Geschichte** in deinem [Konto](account.md), bevor du das Buch erneut öffnest.

<a id="models-and-generation"></a>

## Modelle und Generierung

**„Failed to download model list“.**
Prüfe, ob die URL auf `/v1` endet, der API-Schlüssel stimmt und der Modellserver läuft. Manche Dienste listen ihre
Modelle nicht auf - gib dann die Modell-ID ein. Siehe [Inferenzanbieter](inference-providers.md#per-type-settings).

**Docker: Ein Modellserver auf demselben Rechner ist nicht erreichbar.**
Innerhalb des Containers ist `localhost` der Container selbst. Verwende `http://host.docker.internal:<port>/v1` und lass
den Modellserver auf allen Adressen lauschen, siehe [Modelle auf demselben Rechner](getting-started/docker-server.md#models-on-the-same-machine).

**„Book is missing model.“ / „Book is missing protocol.“**
Wähle sie auf dem Tab *Info* des Buches aus. Lege *Standardmodell* und *Standardprotokoll* in *Einstellungen* fest, damit
neue Bücher sie erhalten.

**„Contextual limit not sufficient.“**
Der Prompt ohne jede Geschichte ist bereits größer als der Platz, den der Kontext nach der Antwort übrig lässt. Erhöhe
*Maximale Kontextgröße* des Anbieters (oder *Maximale Kontext-Tokens* des Protokolls), senke *Maximale Antwort-Tokens*
oder kürze die Vorlagen, Lorebook-Einträge und Zusammenfassungen. Siehe [Grenzen](protocols.md#limits).

**Das Modell meldet, dass der Kontext zu lang ist.**
Die Token-Zählung von Marginalia ist bei manchen Modellen etwas ungenau. Senke *Maximale Kontextgröße* um einige hundert
Tokens. Eine Warnung vor der Anfrage nennt die geschätzten Größen, wenn der Prompt schon zu groß aussieht.

**„The provider did not answer in time“, „The provider is limiting requests“, „The provider rejected the API key“ ...**
Marginalia erklärt Fehler der Modell-API in einfachen Worten, siehe
[Fehlermeldungen](inference-providers.md#error-messages). *Verbindung testen* im Dialog des Anbieters prüft URL, Schlüssel und
Modell ohne ein Buch. Timeout und Wiederholungen werden pro Anbieter eingestellt.

**Der Text bricht mitten im Satz ab.**
Das Limit für die Antwort wurde erreicht. Erhöhe *Maximale Antwort-Tokens* (oder *Maximale Antwort-Tokens* des Protokolls
bzw. *Max Reply Tokens*) - Reasoning-Modelle brauchen deutlich mehr, weil das Reasoning mitgezählt wird.

**Anfragen schlagen fehl, wenn Reasoning aktiviert ist.**
Die API unterstützt `reasoning_effort` nicht. Schalte *Reasoning aktivieren* beim Anbieter aus.

**„The summaries of the messages with IDs ... are outdated ... Continue generation and delete them?“**
Ein Teil, der von einer Zusammenfassung abgedeckt ist, wurde bearbeitet oder gelöscht. Antworte mit *Ja*, um die veralteten
Zusammenfassungen zu löschen und fortzufahren, und erzeuge danach neue. Antworte mit *Nein*, um anzuhalten und sie zu behalten:
Sie sind in der Übersicht der Zusammenfassungen markiert, wo du diejenigen überprüfen kannst, die noch das Richtige sagen. Siehe
[Wenn Zusammenfassungen veralten](books/summaries.md#when-summaries-become-outdated).

**Der Prompt enthält das Wort „Error“.**
Eine Vorlage verwendet eine Variable oder ein Makro, das es nicht gibt, oft ein Tippfehler wie `instruction` statt
`instructions`. Das Vorlagenfeld listet solche Namen darunter auf. Siehe [Tippfehler](templates/templates.md#typos).

## Lorebooks

**Ein Eintrag wird nicht verwendet.** Gehe die Bedingungen unter [Aktivierung](lorebooks/entries.md#activation) durch:

- Ist der Eintrag aktiviert? Ist sein Lorebook aktiviert - und jedes Unter-Lorebook auf dem Weg dorthin?
- Hat er Tags? Dann braucht das Buch einen davon (Buch-Tags stehen auf dem Tab *Info*). Negative Tags schließen ihn aus.
- Hat das Lorebook selbst **Tags**? Sie gelten für alle seine Einträge.
- Hat er einen Filter? Filter sehen nur die **Anweisungen des Abschnitts, der gerade geschrieben wird**, nicht die frühere
  Geschichte - nenne die Figur unter *Anwesende Figuren* oder in den Anweisungen.
- Ist der Filter ein regulärer Ausdruck? Ein ungültiger passt nie.

**Prompt anzeigen** bei einem erzeugten Abschnitt zeigt, welches Lore gesendet wurde.

<a id="prompts-and-settings"></a>

## Prompts und Einstellungen

**Ich kann nicht zu einem anderen Tab wechseln.**
Der Tab *Einstellungen* hat ungespeicherte Änderungen. Klicke auf **Speichern** oder **Änderungen verwerfen**.

**Mein Zusammenfassungsprompt wird nicht verwendet.**
Der eigene Zusammenfassungsprompt eines Buches (Tab *Prompts*) hat Vorrang vor dem *Standard-Zusammenfassungsprompt* in
*Einstellungen*; leere das Feld des Buches, um den Standard zu verwenden. Metazusammenfassungen haben einen eigenen Prompt,
den *Standard-Metazusammenfassungsprompt*.

**Makros in Stil, Erzählperspektive oder Zeitform funktionieren nicht.**
Diese Felder sind reiner Text; nur Vorlagen verarbeiten Makros. Füge das Makro in die Hauptvorlage oder den Benutzer-Prompt ein.

## Backups

**Ich habe ein Datenbank-Backup wiederhergestellt, aber nichts hat sich geändert.**
Eine Wiederherstellung wird beim nächsten Start angewendet. Starte Marginalia neu. Siehe [Wiederherstellen](administration/database-backups.md#restoring).

**Buch-Backups fehlen nach dem Umzug auf einen anderen Computer.**
Buch-Backups sind Dateien, kein Teil der Datenbank. Kopiere auch den Ordner `data`, siehe
[Der Datenordner](administration/index.md#the-data-folder).

<a id="extensions"></a>

## Erweiterungen

**Eine installierte Erweiterung erscheint nicht.**
Schließe das Buch und öffne es erneut oder lade die Seite neu.

**Eine Erweiterung lässt sich nicht laden und wird nicht unter Admin → Erweiterungen aufgeführt.**
Beende Marginalia, lösche die JAR-Datei aus dem Ordner `extensions` und starte neu. Siehe
[Wenn eine Erweiterung fehlschlägt](administration/extensions.md#when-an-extension-fails).

**Eine Erweiterung wurde nach einem Update von Marginalia abgelehnt oder funktioniert nicht mehr.**
Erweiterungen sind für eine bestimmte Version von Marginalia gebaut und werden beim Laden geprüft. Öffne den Bericht
(Admin → Erweiterungen → *Überprüfung*) und lade die Version der Erweiterung, die zu deinem Marginalia passt. Siehe
[Wenn eine Erweiterung fehlschlägt](administration/extensions.md#when-an-extension-fails).

**Nach dem Aktualisieren einer Erweiterung läuft noch die alte Version.**
Entlade zuerst die alte Version und lade dann die neue - oder starte Marginalia neu.
## FAQ

**Wird mein Text irgendwohin gesendet?**
Nur an die Inferenz-Anbieter, die du konfigurierst: Der Prompt für jeden Teil (und Anfragen zur Token-Zählung) geht an die API dieses
Anbieters. Mit einem Modell, das auf deinem eigenen Computer läuft, verlässt nichts den Rechner. Marginalia sendet keine Telemetriedaten.

**Welches Modell sollte ich verwenden?**
Jedes Modell, das gute Fiktion schreibt und einen ausreichend großen Kontext hat; ein Kontext von 32 000 Token oder mehr ist für lange Bücher komfortabel.
Probier ein paar mit demselben Buch aus – der Anbieter lässt sich mit einem Klick auf dem Tab *Info* wechseln.

**Können mehrere Personen an einem Buch arbeiten?**
Nein. Jedes Buch gehört genau einem Benutzer. Andere Benutzer können es lesen, wenn es [veröffentlicht](books/publishing.md) wurde.

**Kann ich Marginalia offline verwenden?**
Ja, mit einem lokal laufenden Modell (llama.cpp, LM Studio, Ollama ...). Marginalia selbst benötigt keine Internetverbindung.

**Wie bekomme ich mein Buch aus Marginalia heraus?**
[Exportiere die Geschichte](books/export.md) als Text, HTML, Word, PDF oder EPUB über das Einstellungsmenü (⚙) des Tabs *Geschichte*.
Um alles einschließlich aller Verzweigungen zu behalten, [exportiere ein Buch-Backup](books/backups.md#exporting-and-importing) (JSON).
Du kannst es auch im [Viewer](books/publishing.md) lesen.

**Funktioniert es auf einem Handy?**
Der [Viewer](books/publishing.md) ist für Handys gemacht, und Marginalia lässt sich wie eine App auf dem Startbildschirm ablegen
(*Install app* / *Add to Home Screen* im Browser-Menü). Der Editor hat ein [Handy-Layout](phone.md), wenn das Browserfenster schmaler als 768 Pixel ist.
Er hat keine Admin-Seite, einen eingeschränkten Lorebook-Editor und nicht alle Erweiterungen, daher eignet er sich am besten zum Schreiben und Lesen;
Einrichtung und Verwaltung gehen auf einem größeren Bildschirm einfacher.

**Gibt es ein helles Design oder eine andere Sprache?**
Ja, zu beidem: Die Schaltfläche mit Sonne / Mond neben der Sprachauswahl wechselt das Design (standardmäßig dunkel), und die Sprachauswahl bietet Japanisch sowie die Sprachen, die Erweiterungen mitbringen (siehe [Konto](account.md#theme)).
