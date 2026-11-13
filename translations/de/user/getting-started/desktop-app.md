---
label: Desktop-App
order: 100
---

# Desktop-App

Die Desktop-App ist eine in sich geschlossene Version von Marginalia für einen Computer. Sie bringt eine eigene Java-Laufzeitumgebung und einen eigenen Server mit, lauscht nur auf Ihrem Computer und öffnet sich im Standardbrowser.

<a id="download"></a>

## Herunterladen

Laden Sie das Installationsprogramm oder das Archiv für Ihr System von der [Releases-Seite](https://github.com/Enerccio/Marginalia/releases) herunter. Für jedes System werden vier Varianten veröffentlicht:

| Datei | Inhalt |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | Das Installationsprogramm: `.deb` unter Linux (Debian, Ubuntu und Abkömmlinge), `.dmg` unter macOS, `.msi` unter Windows |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | Dasselbe Installationsprogramm, bei dem bereits **alle mitgelieferten [Plugins](../extensions/index.md)** installiert sind (siehe unten) |
| `marginalia-<version>-<os>-<arch>-app` | Die App: `Marginalia.app` unter macOS, `Marginalia.exe` unter Windows, `bin/Marginalia` unter Linux |
| `marginalia-<version>-<os>-<arch>` | Ein portables Verzeichnis, das über ein Skript mit Kommandozeilen-[Optionen](#options) gestartet wird |

Builds gibt es für Windows x64, macOS (Apple Silicon und Intel) und Linux (x64 und arm64). `<arch>` im Dateinamen steht für `x86_64` bei 64-Bit-Prozessoren von Intel und AMD (Windows, macOS Intel, Linux) und für `aarch64` bei ARM-Prozessoren (Apple Silicon, Linux arm64).

<a id="install-and-start"></a>

## Installieren und starten

+++ macOS
Mit dem Installationsprogramm: Öffnen Sie die `.dmg` und ziehen Sie *Marginalia* in *Programme*. Mit dem Archiv:

1. Entpacken Sie das Archiv und verschieben Sie `Marginalia.app` in *Programme*.
2. Öffnen Sie die App. Die App ist noch nicht signiert, deshalb verweigert macOS das Öffnen beim ersten Versuch. Klicken Sie die App mit der rechten Maustaste an und wählen Sie *Öffnen*, oder führen Sie aus:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
Mit dem Installationsprogramm: Starten Sie die `.msi`. Sie installiert für Ihren Benutzer (keine Administratorrechte nötig), fügt Marginalia zum Startmenü und zum Desktop hinzu, und eine neuere `.msi` aktualisiert die installierte Version (Ihre Daten bleiben in `.marginalia` erhalten). Mit dem Archiv:

1. Entpacken Sie das Archiv.
2. Starten Sie `Marginalia\Marginalia.exe`.
3. SmartScreen warnt möglicherweise vor einem unbekannten Herausgeber. Klicken Sie auf *Weitere Informationen* → *Trotzdem ausführen*.
+++ Linux
Mit der `.deb` (Debian, Ubuntu und Abkömmlinge):

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

Sie wird nach `/opt/marginalia` installiert und fügt *Marginalia* dem Anwendungsmenü hinzu; `sudo apt remove marginalia` entfernt sie (Ihre Daten in `~/.marginalia` bleiben erhalten). Mit dem Archiv:

1. Entpacken Sie das Archiv.
2. Starten Sie `Marginalia/bin/Marginalia`.
+++ Portables Verzeichnis
1. Entpacken Sie das Archiv.
2. Starten Sie `bin/marginalia` (unter Windows `bin\marginalia.bat`).
+++

Der Start dauert einige Sekunden. Danach öffnet sich Marginalia in Ihrem Browser unter `http://127.0.0.1:8765` und zeigt den Dialog [Erster Start](first-start.md).

<a id="installer-with-the-plugins"></a>

### Installationsprogramm mit den Plugins

Das **full**-Installationsprogramm (`-full` im Dateinamen) ist dieselbe App und bringt alle Plugins mit, die Teil von Marginalia sind (die Anbieter Anthropic und Google, Reviewer, Beta Readers, Side Query, Arc Outline, die Generatoren und die übrigen). Beim ersten Start von Marginalia werden die Plugins nach `~/.marginalia/extensions` kopiert und erscheinen unter *Admin → Extensions*. Mit dem einfachen Installationsprogramm installieren Sie diese dort selbst, eines nach dem anderen.

- Ein Plugin wird einmal installiert. Wenn Sie es unter *Admin → Extensions* entfernen, bleibt es entfernt, auch nach einer Aktualisierung auf eine neuere Version von Marginalia (die neuere Plugins mitbringt und diese neben die alten stellt; Marginalia verwendet die neueste Version und behält die älteren JARs in `extensions/old-versions`). Ein Plugin, das in einer Aktualisierung neu hinzukommt, wird installiert.
- Das vollständige Installationsprogramm über das einfache (oder umgekehrt) zu installieren lässt Ihre Daten und Einstellungen unberührt, ebenso jede Aktualisierung.
- Das einfache Installationsprogramm entfernt keine Plugins. Plugins, die Sie selbst installiert haben, werden niemals angetastet.

<a id="tray-icon"></a>

## Infobereich-Symbol

Solange sie läuft, zeigt Marginalia ihr Symbol (eine Manuskriptseite mit einem kleinen verzweigten Baum am Rand) im Infobereich der Taskleiste an (unter macOS in der Menüleiste). Das Menü hat zwei Einträge:

- **Open Marginalia** - öffnet Marginalia wieder im Browser, zum Beispiel nachdem Sie den Tab geschlossen haben.
- **Quit Marginalia** - beendet den Server. Das Schließen des Browser-Tabs beendet Marginalia nicht.

Wird die App ein zweites Mal gestartet, während sie bereits läuft, öffnet sie nur den Browser.

![Das Infobereich-Symbol und sein Menü](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## Wo Ihre Daten liegen

| Was | Wo |
|---|---|
| Datenbank, Backups, Extensions | `~/.marginalia` (`%USERPROFILE%\.marginalia` unter Windows) |
| Protokolle | `~/.marginalia/desktop/logs/marginalia.log` (der vorherige Lauf wird als `marginalia.log.1` aufbewahrt) |

Um Marginalia auf einen anderen Computer zu übertragen, beenden Sie es und kopieren Sie den Ordner `.marginalia` (mit `secret.key`, das die gespeicherten API-Schlüssel entschlüsselt). Legen Sie vor größeren Änderungen ein [Datenbank-Backup](../administration/database-backups.md) an.

<a id="options"></a>

## Optionen

Der portable Starter akzeptiert diese Optionen:

```
marginalia --port 8765        # Port, auf dem gelauscht wird; ist 8765 belegt, wird ein freier gewählt
marginalia --host 127.0.0.1   # Adresse, auf der gelauscht wird
marginalia --home /path       # Verzeichnis, das den Ordner .marginalia enthält (Standard: Ihr Home-Verzeichnis)
marginalia --no-browser       # den Browser nicht öffnen
marginalia --no-tray          # kein Infobereich-Symbol; Beenden mit Strg+C
marginalia --help
```

!!!warning Andere Adressen
Mit `--host 0.0.0.0` (oder einer anderen Adresse) ist Marginalia über Ihr Netzwerk per einfachem HTTP erreichbar. Für den Zugriff von anderen Geräten verwenden Sie stattdessen den [Server](docker-server.md) hinter HTTPS.
!!!

<a id="updating"></a>

## Aktualisieren

Beenden Sie Marginalia, ersetzen Sie die App (oder das portable Verzeichnis) durch die neue Version und starten Sie sie erneut. Ihre Daten bleiben in `.marginalia` erhalten und werden beim ersten Start automatisch aktualisiert. Bevor sich die Struktur der Datenbank ändert, speichert Marginalia eine [Kopie davon](../administration/database-backups.md#copy-before-an-upgrade) in `db-backups`. Extensions sind für eine bestimmte Marginalia-Version gebaut, aktualisieren Sie sie daher gleichzeitig (siehe [Extensions verwalten](../administration/extensions.md)).

<a id="uninstalling"></a>

## Deinstallieren

Löschen Sie die App oder das Verzeichnis. Um auch Ihre Daten zu entfernen, löschen Sie den Ordner `.marginalia`.
