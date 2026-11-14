---
label: Desktop-App
order: 100
---

# Desktop-App

Die Desktop-App ist eine in sich geschlossene Version von Marginalia für einen Computer. Sie bringt ihre eigene Java-Laufzeitumgebung und
ihren eigenen Server mit, lauscht nur auf Ihrem Computer und öffnet sich in Ihrem Standardbrowser.

## Download

Laden Sie das Installationsprogramm oder das Archiv für Ihr System von der
[Releases-Seite](https://github.com/Enerccio/Marginalia/releases) herunter. Für jedes System werden vier Varianten veröffentlicht:

| Datei | Inhalt |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | Das Installationsprogramm: `.deb` unter Linux (Debian, Ubuntu und Derivate), `.dmg` unter macOS, `.msi` unter Windows |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | Dasselbe Installationsprogramm **mit allen mitgelieferten [Plugins](../extensions/index.md)** bereits installiert (siehe unten) |
| `marginalia-<version>-<os>-<arch>-app` | Die App: `Marginalia.app` unter macOS, `Marginalia.exe` unter Windows, `bin/Marginalia` unter Linux |
| `marginalia-<version>-<os>-<arch>` | Ein portabler Ordner, der mit einem Skript gestartet wird, mit Kommandozeilen-[Optionen](#options) |

Builds sind für Windows x64, macOS (Apple Silicon und Intel) und Linux (x64 und arm64) verfügbar. `<arch>` im
Dateinamen steht `x86_64` für 64-Bit-Prozessoren von Intel und AMD (Windows, macOS Intel, Linux) und `aarch64` für ARM
(Apple Silicon, Linux arm64).

<a id="install-and-start"></a>

## Installieren und starten

+++ macOS
Mit dem Installationsprogramm: Öffnen Sie die `.dmg` und ziehen Sie *Marginalia* nach *Programme*. Mit dem Archiv:

1. Entpacken Sie das Archiv und verschieben Sie `Marginalia.app` nach *Programme*.
2. Öffnen Sie die App. Sie ist noch nicht signiert, deshalb verweigert macOS das Öffnen beim ersten Mal. Klicken Sie mit der rechten Maustaste auf die App und wählen Sie
   *Öffnen*, oder führen Sie aus:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
Mit dem Installationsprogramm: Starten Sie `.msi`. Es installiert sich für Ihren Benutzer (keine Administratorrechte nötig), legt Marginalia im Startmenü und auf dem Desktop ab, und ein neueres `.msi` aktualisiert die installierte Version (Ihre Daten bleiben in `.marginalia`). Mit dem Archiv:

1. Entpacken Sie das Archiv.
2. Starten Sie `Marginalia\Marginalia.exe`.
3. SmartScreen warnt möglicherweise vor einem unbekannten Herausgeber. Klicken Sie auf *Weitere Informationen* → *Trotzdem ausführen*.
+++ Linux
Mit dem `.deb` (Debian, Ubuntu und Derivate):

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

Es installiert sich nach `/opt/marginalia` und fügt *Marginalia* dem Anwendungsmenü hinzu; `sudo apt remove marginalia` entfernt es (Ihre Daten in `~/.marginalia` bleiben erhalten). Mit dem Archiv:

1. Entpacken Sie das Archiv.
2. Starten Sie `Marginalia/bin/Marginalia`.
+++ Portabler Ordner
1. Entpacken Sie das Archiv.
2. Starten Sie `bin/marginalia` (`bin\marginalia.bat` unter Windows).
+++

Der Start dauert ein paar Sekunden. Marginalia öffnet sich dann in Ihrem Browser unter `http://127.0.0.1:8765` und zeigt den Dialog
[Erststart](first-start.md).

<a id="installer-with-the-plugins"></a>

### Installationsprogramm mit Plugins

Das **vollständige** Installationsprogramm (`-full` im Dateinamen) ist dieselbe App und bringt jedes Plugin mit, das zu Marginalia gehört
(die Anbieter Anthropic und Google, Reviewer, Beta Readers, Side Query, Arc Outline, die Generatoren und die übrigen). Beim ersten Start
von Marginalia werden die Plugins nach `~/.marginalia/extensions` kopiert und erscheinen unter *Admin → Erweiterungen*;
mit dem einfachen Installationsprogramm installieren Sie sie dort selbst, einzeln.

- Ein Plugin wird einmal installiert. Wenn Sie es unter *Admin → Erweiterungen* entfernen, bleibt es entfernt, auch nachdem Sie auf eine neuere Version von Marginalia aktualisiert haben
  (diese bringt neuere Plugins mit und legt sie neben die alten; Marginalia verwendet die neueste und behält die älteren JARs in `extensions/old-versions`). Ein Plugin, das in einem Update neu hinzukommt, wird installiert.
- Das vollständige Installationsprogramm über das einfache zu installieren (oder umgekehrt) behält Ihre Daten und Einstellungen, ebenso jedes Update.
- Das einfache Installationsprogramm entfernt keine Plugins. Plugins, die Sie selbst installiert haben, werden nie angerührt.

<a id="tray-icon"></a>

## Tray-Symbol

Während die App läuft, zeigt Marginalia sein Symbol (eine Manuskriptseite mit einem kleinen verzweigten Baum am Rand) im Infobereich der Taskleiste (auf macOS in der Menüleiste). Das Menü hat zwei
Einträge:

- **Marginalia öffnen** - öffnet Marginalia erneut im Browser, zum Beispiel nachdem Sie den Tab geschlossen haben.
- **Marginalia beenden** - stoppt den Server. Das Schließen des Browser-Tabs beendet Marginalia nicht.

Wird die App ein zweites Mal gestartet, während sie bereits läuft, öffnet sie sich lediglich im Browser.

![Das Tray-Symbol und sein Menü](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## Wo Ihre Daten liegen

| Was | Wo |
|---|---|
| Datenbank, Backups, Erweiterungen | `~/.marginalia` (`%USERPROFILE%\.marginalia` unter Windows) |
| Protokolle | `~/.marginalia/desktop/logs/marginalia.log` (der vorherige Lauf wird als `marginalia.log.1` aufbewahrt) |

Um Marginalia auf einen anderen Computer zu übertragen, beenden Sie es und kopieren Sie den Ordner `.marginalia` (mit `secret.key`, das
die gespeicherten API-Schlüssel entschlüsselt). Erstellen Sie vor größeren Änderungen eine
[Datenbanksicherung](../administration/database-backups.md).

<a id="options"></a>

## Optionen

Der portable Launcher akzeptiert diese Optionen:

```
marginalia --port 8765        # port to listen on; a free one is picked when 8765 is taken
marginalia --host 127.0.0.1   # address to listen on
marginalia --home /path       # directory that holds the .marginalia folder (default: your home directory)
marginalia --no-browser       # don't open the browser
marginalia --no-tray          # no tray icon; stop with Ctrl+C
marginalia --help
```

!!!warning Andere Adressen
Mit `--host 0.0.0.0` (oder einer anderen Adresse) ist Marginalia über Ihr Netzwerk per einfachem HTTP erreichbar. Für den Zugriff von anderen Geräten
verwenden Sie stattdessen den [Server](docker-server.md) hinter HTTPS.
!!!

<a id="updating"></a>

## Aktualisieren

Beenden Sie Marginalia, ersetzen Sie die App (oder den portablen Ordner) durch die neue Version und starten Sie sie erneut. Ihre Daten bleiben in
`.marginalia` und werden beim ersten Start automatisch aktualisiert; bevor sich die Struktur der Datenbank ändert, legt
Marginalia eine [Kopie davon](../administration/database-backups.md#copy-before-an-upgrade) in `db-backups` ab. Erweiterungen sind für eine bestimmte Marginalia-Version gebaut, deshalb
aktualisieren Sie sie gleichzeitig (siehe [Erweiterungen verwalten](../administration/extensions.md)).

<a id="uninstalling"></a>

## Deinstallieren

Löschen Sie die App oder den Ordner. Um auch Ihre Daten zu entfernen, löschen Sie zusätzlich den Ordner `.marginalia`.
