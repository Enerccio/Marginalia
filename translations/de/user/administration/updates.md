---
label: Aktualisierungen
order: 65
---

# Aktualisierungen

Auf einem [Docker-Server](../getting-started/docker-server.md), der aus einem Git-Checkout gebaut wurde, kann ein Administrator Marginalia über den Tab **Update** der Admin-Seite aktualisieren, ohne sich auf dem Server anzumelden: Der Server holt die neueste Version des Branches, auf dem sein Checkout liegt, baut die Anwendung und die [mitgelieferten Plugins](../getting-started/docker-server.md#plugins) und startet neu.

Dafür braucht es ein kleines Programm auf dem Server, den **Update-Watcher**, der [einmalig eingerichtet](#setting-up-the-watcher) wird. Ohne ihn zeigt der Tab nur die laufende Version und meldet, dass der Watcher nicht läuft. Das gilt auch für die [Desktop-App](../getting-started/desktop-app.md) und für einen Server mit dem [fertigen Image](../getting-started/docker-server.md#prebuilt-image): Sie werden aktualisiert, indem die neue Version installiert wird.

<a id="the-update-tab"></a>

## Der Update-Tab

| | |
|---|---|
| **Running version** | Die Version und der Commit des laufenden Marginalia. |
| **Branch** | Der Branch, auf dem der Checkout auf dem Server liegt, und der Remote-Branch, dem er folgt (zum Beispiel `dev`, folgt `origin/dev`), der neueste Commit dieses Remote-Branches und wann der Server zuletzt nachgesehen hat. Der Server sieht alle 15 Minuten nach. |
| Status | *A new version is available*, wenn der neueste Commit nicht der laufende ist, sonst *Marginalia is up to date*. Während ein Update läuft, zeigt der Tab das an. |
| **Last update** | Wann das letzte von hier gestartete Update endete und ob es erfolgreich oder fehlgeschlagen war. |

Der Server folgt dem Branch, der dort ausgecheckt ist, egal welcher: Um eine Installation auf einen anderen Branch umzustellen, `git switch` ihn im Checkout auf dem Server; der Tab zeigt den neuen Branch nach dem nächsten Nachsehen an.

| Button | |
|---|---|
| **Update and restart** | Bittet den Server um ein Update, nach einer Bestätigung. Es baut und startet auch dann neu, wenn nichts Neues vorliegt (etwa nach einer Änderung an `docker-compose.yml`). |
| **Refresh** | Liest den Status erneut. Der Tab aktualisiert sich nicht von selbst. |
| **Log** | Die Ausgabe des letzten Updates: was Git und der Build geschrieben haben. |

Dasselbe geht aus einem Skript heraus, mit einem [API-Schlüssel](../account.md#api-keys), der die Berechtigung *Update the installation* hat: Status lesen, ein Update anfordern und das Log lesen (siehe das [Entwicklerhandbuch](../../developer/api.md)).

<a id="what-an-update-does"></a>

## Was ein Update bewirkt

1. Die neue Version wird gebaut: `git pull`, die Plugins, das Image. Das dauert mehrere Minuten, und **Marginalia läuft dabei weiter**. Der Tab zeigt *The update is running*; **Log** zeigt, wie weit es ist.
2. Marginalia wird gestoppt, die neuen Plugins werden nach `data/extensions` kopiert (die installierten werden ersetzt), und die neue Version startet. Das dauert ein bis zwei Minuten (siehe [Health](../getting-started/docker-server.md#health)). Alle, die gerade arbeiten, werden unterbrochen, und was gerade generiert wird, wird angehalten. Die Bestätigung nennt, wie viele [Jobs](jobs.md) in diesem Moment laufen, suchen Sie sich also eine ruhige Zeit aus.
3. Die Datenbank wird beim Start der neuen Version aktualisiert, nachdem eine [Kopie davon](database-backups.md#copy-before-an-upgrade) gespeichert wurde. Machen Sie vorher auch ein eigenes [Datenbank-Backup](database-backups.md).

Schlägt der Build fehl, wird nichts gestoppt: Die bisherige Version läuft weiter, **Last update** zeigt *failed*, und das Log sagt, warum. Das nächste Update baut erneut, auch wenn nichts Neues zum Holen da ist.

<a id="setting-up-the-watcher"></a>

## Den Watcher einrichten

Marginalia läuft in einem Container und kann weder auf den Git-Checkout noch auf Docker zugreifen; diesen Zugriff zu geben, würde jeden, der in die Anwendung gelangt, zum Administrator des Servers machen. Die Anwendung fragt daher nur: Sie schreibt eine Datei in ihren [Datenordner](index.md#the-data-folder), und der Watcher, der außerhalb des Containers auf dem Server läuft, erledigt die Arbeit. Der Watcher ist das Skript `tools/update-watcher/watch.sh` des Checkouts; er führt immer nur `rebuild.sh` desselben Checkouts aus (siehe [Automatische Updates](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux mit systemd

Im Checkout auf dem Server (dem Ordner mit `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Es installiert und startet drei Units:

| Unit | |
|---|---|
| `marginalia-update.service` | Führt den Watcher einmal aus. |
| `marginalia-update.path` | Startet ihn in dem Moment, in dem die Anwendung ein Update anfordert. |
| `marginalia-update.timer` | Startet ihn zwei Minuten nach dem Booten und danach alle 15 Minuten, um nach neuen Commits zu suchen. |

Öffnen Sie **Admin → Update** und klicken Sie auf **Refresh**: Der Tab zeigt den Branch und den neuesten Commit. Mehr ist nicht nötig.

| Option | |
|---|---|
| `--name NAME` | Der Name der Units (Standard `marginalia-update`). Zwei Checkouts auf einem Rechner (eine Test- und eine Produktionsinstallation) brauchen je einen eigenen Namen. |
| `--user USER` | Lässt den Watcher als diesen Benutzer statt als root laufen, siehe [unten](#who-the-watcher-runs-as). |
| `--uninstall` | Stoppt und entfernt die Units (mit demselben `--name`). |

`systemctl status marginalia-update.service` zeigt den letzten Lauf und `journalctl -u marginalia-update.service` seine Ausgabe (derselbe Text wie **Log**, dazu der Grund, wenn der Watcher selbst nicht arbeiten konnte). Die Units enthalten den Pfad des Checkouts: Nach dem Verschieben des Checkouts führen Sie `install.sh` erneut aus.

<a id="without-systemd-cron"></a>

### Ohne systemd (cron)

Lassen Sie den Watcher jede Minute laufen. Er tut nichts, wenn niemand ein Update angefordert hat und er das Remote vor weniger als 15 Minuten angesehen hat, es kostet also nichts:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Tragen Sie die Zeile in die Crontab von root (`sudo crontab -e`) ein oder in die eines anderen Benutzers, siehe unten. Ein Update startet dann innerhalb einer Minute nach dem Klick.

<a id="settings"></a>

### Einstellungen

Der Watcher liest diese Umgebungsvariablen (mit systemd: Zeilen mit `sudo systemctl edit marginalia-update.service` und `Environment=NAME=value` unter `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | Der Datenordner auf dem Server, wenn er nicht `data` im Checkout ist (die linke Seite des Volumes in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Die Argumente von `rebuild.sh`, standardmäßig `--force`. Auf einem Server ohne genug Speicher für Anwendung und Build zusammen verwenden Sie `--force --stop-first`: Marginalia wird dann vor dem Build gestoppt und ist während des gesamten Builds nicht erreichbar. |
| `MARGINALIA_CHECK_MINUTES` | Wie alt der Status werden darf, bevor ein Lauf ohne Anforderung das Remote erneut ansieht; standardmäßig 15 (Minuten). Mit systemd bestimmt der Timer, wie oft der Watcher läuft. |

<a id="who-the-watcher-runs-as"></a>

### Als wer der Watcher läuft

Standardmäßig läuft der Watcher als **root**: Er muss Docker benutzen und in den Datenordner schreiben, der dem Benutzer des Containers gehört. Git selbst läuft als Eigentümer des Checkouts, sodass die Git-Dateien im Besitz des Eigentümers bleiben.

Der Watcher führt aus, was der Branch enthält (`rebuild.sh`, das `Dockerfile`), so wie es jedes Update von Hand tut. Folgen Sie deshalb nur einem Repository, dem Sie vertrauen, und machen Sie den Checkout nur für seinen Eigentümer beschreibbar. Wer dort `rebuild.sh` oder `watch.sh` ändern kann, führt Befehle als Benutzer des Watchers aus.

Mit `--user USER` (oder der Crontab dieses Benutzers) läuft er als jemand anderes. Dieser Benutzer muss Docker verwenden dürfen (auf den meisten Systemen ist das praktisch dasselbe wie root), muss dem Checkout gehören und in `data/update` und `data/extensions` schreiben können, die normalerweise dem Benutzer des Containers gehören.

<a id="windows-and-macos"></a>

### Windows und macOS

Für Docker Desktop gibt es keinen fertigen Watcher. Aktualisieren Sie mit `rebuild.bat` / `rebuild.sh` von Hand oder aus einer geplanten Aufgabe, siehe [Automatische Updates](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Fehlerbehebung

| | |
|---|---|
| *The update watcher does not run on the server* | Der Watcher hat eine Stunde lang oder nie einen Status geschrieben. Prüfen Sie `systemctl status marginalia-update.timer marginalia-update.path` und `journalctl -u marginalia-update.service`. Wenn der Datenordner nicht `data` im Checkout ist, setzen Sie `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | Der Checkout liegt auf einem Branch ohne Upstream (oder auf einem losgelösten Commit), es gibt also nichts zu holen. Auf dem Server: `git switch <branch>` oder `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` ist beim letzten Nachsehen fehlgeschlagen (kein Netz, ein privates Repository ohne Zugangsdaten für den Eigentümer des Checkouts). Der angezeigte neueste Commit ist möglicherweise alt. |
| The update was requested and nothing happens | Mit cron startet es innerhalb einer Minute. Eine Anforderung, die eine Stunde lang niemand übernommen hat, wird verworfen, damit sie nicht überraschend startet, wenn der Watcher repariert wird. |
| *Last update: ..., failed* | Lesen Sie **Log**. Ein fehlgeschlagenes `git pull` bedeutet meist, dass der Checkout eigene Änderungen hat (bearbeitete Dateien, lokale Commits). Lösen Sie das auf dem Server. Ein fehlgeschlagener Build lässt die alte Version laufen. |

Die Dateien des Watchers liegen in `update` des Datenordners: `status.properties` (was der Tab zeigt), `update.log` (das letzte Update) und kurzzeitig `request`.
