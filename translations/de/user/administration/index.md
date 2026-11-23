---
label: Administration
order: 910
icon: tools
---

# Administration

Administratoren verwalten die Marginalia-Installation: Benutzerkonten, Datenbanksicherungen, die Bereinigung gelöschter Daten, Aktualisierungen und Erweiterungen. Das erste Konto, das beim [ersten Start](../getting-started/first-start.md) angelegt wird, ist ein Administrator; er kann auch andere Benutzer zu Administratoren machen.

Administratoren sind außerdem normale Benutzer mit eigenen Büchern. Sie sehen die Bücher, Lorebooks oder Einstellungen anderer Benutzer nicht, nur die Lorebooks und Protokolle, die diese Benutzer mit allen teilen.

<a id="the-admin-page"></a>

## Die Seite Administration

Administratoren finden unten im Arbeitsbereich die Schaltfläche **Administration**. Im [Telefon-Layout](../phone.md#administration) (ein Fenster, das schmaler als 768 Pixel ist) ist es der Reiter **Administration** der Schublade. Sie öffnet die Administrationsseite mit diesen Reitern:

| Reiter | |
|---|---|
| **Benutzer** | Anlegen, Bearbeiten und Löschen von Konten sowie das Vergeben von [Berechtigungen](permissions.md). Siehe [Benutzer](users.md). |
| **Datenbanksicherungen** | Sicherungen der gesamten Datenbank, auf Abruf oder nach Zeitplan, und ihr Wiederherstellen. Siehe [Datenbanksicherungen](database-backups.md). |
| **Bereinigung** | Endgültiges Entfernen gelöschter Daten. Siehe [Bereinigung](cleanup.md). |
| **Ressourcendateien** | Finden der Dateien von Ressourcen, die nicht mehr verwendet werden, Herunterladen als ZIP und Löschen von der Festplatte. Siehe [Ressourcendateien](resource-files.md). |
| **Hilfeassistent** | Der Chat, der Fragen aus dem Handbuch beantwortet: das Modell, die Prompts und wer dafür bezahlt. Siehe [Hilfeassistent](help-assistant.md). |
| **Agenten** | Der Systemprompt und der Verdichtungs-Prompt, denen Agenten folgen, wenn weder sie noch ihr Benutzer einen eigenen haben. Leer bedeutet der eingebaute Prompt; das Info-Symbol hat **Standardvorlage einfügen**, das ihn ausfüllt, damit Sie eine Kopie bearbeiten können. Was Benutzer mit Agenten tun dürfen, ist ihre [Berechtigung](permissions.md). Außerdem **Agenten dürfen Nachrichten an die Webhooks der Benutzer senden**: Der Server ruft die Adresse auf, die ein Benutzer in seinen Einstellungen eingetragen hat, wenn ein Agent ihn braucht und er die Anwendung geschlossen hat ([Wenn Sie nicht da sind](../agents.md#when-you-are-not-there)). Schalten Sie es dort aus, wo das nicht akzeptabel ist. |
| **Statistik** | Ob die [Schreibstatistiken](../books/statistics.md) überhaupt erfasst werden, wie lange sie aufbewahrt werden, und das Löschen der Statistik eines Benutzers oder aller. Siehe [Statistik](statistics.md). |
| **Jobs** | Das Protokoll aller Anfragen an eine externe KI (wer, wann, welche Aufrufe, wie sie endeten), ein Filter, der alte Einträge von Hand löscht, und die Seite `/jobs` mit den Jobs, die gerade laufen. Siehe [Jobs](jobs.md). |
| **GPUs** | Eine Grafikkarte, die sich das Sprachmodell und die Bild- und Sprachserver teilen: die GPUs, die GPU-Sperren (wie jedes Modell geladen wird und die Karte davon freigemacht wird) und die Schritte, die sie ausführen, was jede GPU gerade hält und **Jetzt entladen**. Siehe [GPUs](gpus.md). |
| **Aktualisieren** | Auf einem Docker-Server: die laufende Version, die neueste Version des Zweigs, dem der Server folgt, und die Schaltfläche, die Marginalia aktualisiert und neu startet. Siehe [Aktualisierungen](updates.md). |
| **Erweiterungen** | Installieren und Entfernen von Erweiterungen. Siehe [Erweiterungen verwalten](extensions.md). |

![Der Reiter Agenten der Seite Administration](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## Der Datenordner

Alles, was Marginalia speichert, liegt im Ordner `.marginalia` im Home-Verzeichnis des Benutzers, unter dem Marginalia läuft (`./data` auf dem Host bei der [Docker-Einrichtung](../getting-started/docker-server.md)):

| Pfad | |
|---|---|
| `marginalia.sqlite` (mit `-wal`, `-shm`) | Die Datenbank: Benutzer, Bücher, Lorebooks, Einstellungen, alles. |
| `secret.key` | Schlüssel, der die API-Schlüssel in der Datenbank verschlüsselt. Wird beim ersten Start angelegt und ist nur für seinen Besitzer lesbar (unter Linux und macOS). Ohne ihn können gespeicherte API-Schlüssel nicht gelesen werden. Ist er für andere Benutzer des Computers lesbar, meldet das Protokoll das: `chmod 600 secret.key`. |
| `db-backups/` | [Datenbanksicherungen](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Buchsicherungen](../books/backups.md) jedes Benutzers. |
| `extensions/` | Installierte JARs der [Erweiterungen](extensions.md). |
| `update/` | Die Dateien, über die Marginalia und der [Update-Watcher](updates.md#setting-up-the-watcher) eines Docker-Servers miteinander sprechen: der Status, das Protokoll der letzten Aktualisierung, die Anforderung. |
| `agent-workspaces/<agent id>/` | Die Dateien eines [Agenten](../agents.md#the-working-copy) (seine Notizen und Berichte, die er zwischen den Läufen behält). |
| `agent-runs/<run id>/` | Was ein Lauf eines Agenten behält: der Anfangszustand des Buches (um Ihnen die Änderungen zu zeigen, wird mit der Arbeitskopie entfernt), die Checkpoints und die Befunde (werden entfernt, wenn der Lauf durch die Bereinigung endgültig gelöscht wird). |
| `desktop/logs/` | Protokolle der [Desktop-App](../getting-started/desktop-app.md). |

Um eine ganze Installation zu sichern, beenden Sie Marginalia und kopieren diesen Ordner (einschließlich `secret.key`). Solange es läuft, verwenden Sie für die Datenbank [Datenbanksicherungen](database-backups.md). Buchsicherungen und Erweiterungen sind einfache Dateien und können jederzeit kopiert werden.
