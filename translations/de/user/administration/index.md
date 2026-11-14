---
label: Administration
order: 910
icon: tools
---

# Administration

Administratoren verwalten die Marginalia-Installation: Benutzerkonten, Datenbanksicherungen, Bereinigung gelöschter Daten,
Aktualisierungen und Erweiterungen. Das erste Konto, das beim [ersten Start](../getting-started/first-start.md) angelegt wird, ist ein Administrator;
es kann auch andere Benutzer zu Administratoren machen.

Administratoren sind außerdem normale Benutzer mit eigenen Büchern. Sie sehen nicht die Bücher, Lorebooks oder Einstellungen anderer Benutzer, sondern nur die Lorebooks und Protokolle, die diese Benutzer mit allen teilen.

<a id="the-admin-page"></a>

## Die Admin-Seite

Administratoren haben unten im Arbeitsbereich eine Schaltfläche **Administration**. Im [Telefonlayout](../phone.md#administration) (ein Fenster, das schmaler als 768 Pixel ist) ist es der Reiter **Administration** der Schublade. Sie öffnet die Admin-Seite mit diesen Reitern:

| Reiter | |
|---|---|
| **Benutzer** | Konten anlegen, bearbeiten und löschen sowie Benutzern [Berechtigungen](permissions.md) geben. Siehe [Benutzer](users.md). |
| **Datenbanksicherungen** | Sicherungen der gesamten Datenbank, auf Abruf oder nach Zeitplan, und ihre Wiederherstellung. Siehe [Datenbanksicherungen](database-backups.md). |
| **Bereinigung** | Gelöschte Daten endgültig entfernen. Siehe [Bereinigung](cleanup.md). |
| **Ressourcendateien** | Die Dateien von Ressourcen finden, die nichts mehr verwendet, sie als ZIP herunterladen und von der Festplatte löschen. Siehe [Ressourcendateien](resource-files.md). |
| **Hilfeassistent** | Der Chat, der Fragen aus dem Handbuch beantwortet: das Modell, die Prompts und wer dafür bezahlt. Siehe [Hilfeassistent](help-assistant.md). |
| **Agenten** | Der Systemprompt und der Verdichtungsprompt, denen Agenten folgen, wenn weder sie noch ihr Benutzer einen eigenen haben. Leer bedeutet der eingebaute Prompt; das Info-Symbol hat **Standardvorlage einfügen**, das ihn ausfüllt, damit du eine Kopie bearbeiten kannst. Was Benutzer mit Agenten tun dürfen, regelt ihre [Berechtigung](permissions.md). Außerdem **Agenten dürfen Nachrichten an die Webhooks der Benutzer senden**: Wenn ein Agent einen Benutzer braucht und dieser die Anwendung geschlossen hat, ruft der Server die Adresse auf, die der Benutzer in seinen Einstellungen eingetragen hat ([Wenn du nicht da bist](../agents.md#when-you-are-not-there)). Schalte es dort aus, wo das nicht akzeptabel ist. |
| **Statistik** | Ob die [Schreibstatistiken](../books/statistics.md) überhaupt erfasst werden, wie lange sie aufbewahrt werden, und das Löschen der Statistik eines Benutzers oder aller. Siehe [Statistik](statistics.md). |
| **Jobs** | Das Protokoll aller Anfragen an eine externe KI (wer, wann, welche Aufrufe, wie sie endeten), ein Filter, der alte Einträge von Hand löscht, und die Seite `/jobs` mit den Jobs, die gerade laufen. Siehe [Jobs](jobs.md). |
| **Aktualisieren** | Auf einem Docker-Server: die laufende Version, die neueste Version des Zweigs, dem der Server folgt, und die Schaltfläche, die Marginalia aktualisiert und neu startet. Siehe [Aktualisierungen](updates.md). |
| **Erweiterungen** | Erweiterungen installieren und entfernen. Siehe [Erweiterungen verwalten](extensions.md). |

![Der Reiter Agenten der Admin-Seite](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## Der Datenordner

Alles, was Marginalia speichert, liegt im Ordner `.marginalia` im Home-Verzeichnis des Benutzers, unter dem es läuft (`./data`
auf dem Host bei der [Docker-Einrichtung](../getting-started/docker-server.md)):

| Pfad | |
|---|---|
| `marginalia.sqlite` (mit `-wal`, `-shm`) | Die Datenbank: Benutzer, Bücher, Lorebooks, Einstellungen, alles. |
| `secret.key` | Schlüssel, der die API-Schlüssel in der Datenbank verschlüsselt. Wird beim ersten Start angelegt und ist nur für seinen Eigentümer lesbar (unter Linux und macOS); ohne ihn lassen sich gespeicherte API-Schlüssel nicht lesen. Ist er für andere Benutzer des Computers lesbar, weist das Protokoll darauf hin: `chmod 600 secret.key`. |
| `db-backups/` | [Datenbanksicherungen](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Buchsicherungen](../books/backups.md) jedes Benutzers. |
| `extensions/` | Installierte [Erweiterungen](extensions.md) als JAR-Dateien. |
| `update/` | Die Dateien, über die Marginalia und der [Update-Wächter](updates.md#setting-up-the-watcher) eines Docker-Servers miteinander kommunizieren: der Status, das Protokoll der letzten Aktualisierung, die Anfrage. |
| `agent-workspaces/<agent id>/` | Die Dateien eines [Agenten](../agents.md#the-working-copy) (seine Notizen und Berichte, die er zwischen den Läufen aufbewahrt). |
| `agent-runs/<run id>/` | Was ein Lauf eines Agenten aufbewahrt: der Stand des Buchs am Anfang (um dir die Änderungen zu zeigen, wird mit der Arbeitskopie entfernt), die Kontrollpunkte und die Befunde (werden entfernt, wenn der Lauf durch die Bereinigung gelöscht wird). |
| `desktop/logs/` | Protokolle der [Desktop-App](../getting-started/desktop-app.md). |

Um eine ganze Installation zu sichern, beende Marginalia und kopiere diesen Ordner (einschließlich `secret.key`). Während es läuft, verwende für die Datenbank die
[Datenbanksicherungen](database-backups.md); Buchsicherungen und Erweiterungen sind einfache Dateien und können jederzeit
kopiert werden.
