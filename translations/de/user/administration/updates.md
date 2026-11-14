---
label: Aktualisierungen
order: 65
---

# Aktualisierungen

Auf einem [Docker-Server](../getting-started/docker-server.md), der aus einem Git-Checkout gebaut wurde, kann ein Administrator Marginalia über den Tab **Update** der Admin-Seite aktualisieren, ohne sich auf dem Server anzumelden: Der Server holt die neueste Version des Branches, auf dem sein Checkout steht, baut die Anwendung und die [mitgelieferten Plugins](../getting-started/docker-server.md#plugins) und startet neu.

Dafür braucht es ein kleines Programm auf dem Server, den **Update-Wächter**, der [einmalig eingerichtet](#setting-up-the-watcher) wird. Ohne ihn zeigt der Tab nur die laufende Version und meldet, dass der Wächter nicht läuft. Das gilt auch für die [Desktop-App](../getting-started/desktop-app.md) und für einen Server mit dem [fertigen Image](../getting-started/docker-server.md#prebuilt-image): Sie werden durch Installation der neuen Version aktualisiert.

<a id="the-update-tab"></a>

## Der Tab Update

| | |
|---|---|
| **Laufende Version** | Die Version und der Commit des laufenden Marginalia. |
| **Branch** | Der Branch, auf dem der Checkout auf dem Server steht, und der Remote-Branch, dem er folgt (zum Beispiel `dev`, folgt `origin/dev`), der neueste Commit dieses Remote-Branches und der Zeitpunkt, zu dem der Server zuletzt nachgesehen hat. Der Server sieht alle 15 Minuten nach. |
| Status | *Eine neue Version ist verfügbar*, wenn der neueste Commit nicht der laufende ist, sonst *Marginalia ist auf dem neuesten Stand*. Während eine Aktualisierung läuft, zeigt der Tab das an. |
| **Letzte Aktualisierung** | Wann die letzte Aktualisierung, die von hier aus gestartet wurde, geendet hat und ob sie erfolgreich oder fehlgeschlagen ist. |

Der Server folgt dem Branch, der dort ausgecheckt ist, egal welcher es ist: Um eine Installation auf einen anderen Branch umzustellen, `git switch` sie im Checkout auf dem Server; der Tab zeigt den neuen Branch nach der nächsten Prüfung an.

| Schaltfläche | |
|---|---|
| **Aktualisieren und neu starten** | Bittet den Server nach einer Bestätigung um eine Aktualisierung. Es wird auch gebaut und neu gestartet, wenn nichts Neues vorliegt (etwa nach einer Änderung an `docker-compose.yml`). |
| **Aktualisieren** | Liest den Status erneut ein. Der Tab aktualisiert sich nicht von selbst. |
| **Protokoll** | Die Ausgabe der letzten Aktualisierung: was Git und der Build geschrieben haben. |

<a id="what-an-update-does"></a>

## Was eine Aktualisierung tut

1. Die neue Version wird gebaut: `git pull`, die Plugins, das Image. Das dauert mehrere Minuten, und **Marginalia läuft währenddessen weiter**. Der Tab zeigt *Die Aktualisierung läuft*; **Protokoll** zeigt, wie weit sie ist.
2. Marginalia wird angehalten, die neuen Plugins werden nach `data/extensions` kopiert (die installierten werden ersetzt), und die neue Version startet. Das dauert ein bis zwei Minuten (siehe [Zustand](../getting-started/docker-server.md#health)). Alle, die gerade arbeiten, werden unterbrochen, und was gerade erzeugt wird, wird angehalten: Die Bestätigung nennt, wie viele [Aufgaben](jobs.md) in diesem Moment laufen, also wähle eine ruhige Zeit.
3. Die Datenbank wird beim Start der neuen Version aktualisiert, nachdem eine [Kopie davon](database-backups.md#copy-before-an-upgrade) gespeichert wurde. Mach außerdem vorher selbst ein [Datenbank-Backup](database-backups.md).

Schlägt der Build fehl, wird nichts angehalten: Die vorherige Version läuft weiter, **Letzte Aktualisierung** zeigt *fehlgeschlagen*, und das Protokoll sagt, warum. Die nächste Aktualisierung baut erneut, auch wenn nichts Neues zu holen ist.

<a id="setting-up-the-watcher"></a>

## Den Wächter einrichten

Marginalia läuft in einem Container und kann weder auf den Git-Checkout noch auf Docker zugreifen; ihm diesen Zugriff zu geben, würde jeden, der in die Anwendung eindringt, zum Administrator des Servers machen. Deshalb fragt es nur nach: Es schreibt eine Datei in seinen [Datenordner](index.md#the-data-folder), und der Wächter, der auf dem Server außerhalb des Containers läuft, erledigt die Arbeit. Der Wächter ist das Skript `tools/update-watcher/watch.sh` des Checkouts; er führt nur `rebuild.sh` desselben Checkouts aus (siehe [Automatische Aktualisierungen](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux mit systemd

Im Checkout auf dem Server (dem Ordner mit `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Das installiert und startet drei Units:

| Unit | |
|---|---|
| `marginalia-update.service` | Führt den Wächter einmal aus. |
| `marginalia-update.path` | Startet ihn in dem Moment, in dem die Anwendung eine Aktualisierung anfordert. |
| `marginalia-update.timer` | Startet ihn zwei Minuten nach dem Booten und danach alle 15 Minuten, um nach neuen Commits zu sehen. |

Öffne **Admin → Update** und klicke auf **Aktualisieren**: Der Tab zeigt den Branch und den neuesten Commit. Mehr ist nicht nötig.

| Option | |
|---|---|
| `--name NAME` | Der Name der Units (Standard `marginalia-update`). Zwei Checkouts auf einem Rechner (eine Test- und eine Produktionsinstallation) brauchen jeweils einen eigenen Namen. |
| `--user USER` | Führt den Wächter als diesen Benutzer statt als root aus, siehe [unten](#who-the-watcher-runs-as). |
| `--uninstall` | Stoppt und entfernt die Units (mit demselben `--name`). |

`systemctl status marginalia-update.service` zeigt den letzten Lauf und `journalctl -u marginalia-update.service` dessen Ausgabe (derselbe Text wie **Protokoll**, und den Grund, wenn der Wächter selbst nicht arbeiten konnte). Die Units enthalten den Pfad des Checkouts: Wenn du den Checkout verschiebst, führe `install.sh` erneut aus.

<a id="without-systemd-cron"></a>

### Ohne systemd (cron)

Führe den Wächter jede Minute aus; er tut nichts, wenn niemand eine Aktualisierung angefordert hat und er das Remote vor weniger als 15 Minuten angesehen hat, daher kostet das nichts:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Trage die Zeile in die Crontab von root (`sudo crontab -e`) oder eines anderen Benutzers ein, siehe unten. Eine Aktualisierung startet dann innerhalb einer Minute nach dem Klick.

<a id="settings"></a>

### Einstellungen

Der Wächter liest diese Umgebungsvariablen (bei systemd: Zeilen `sudo systemctl edit marginalia-update.service` und `Environment=NAME=value` unter `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | Der Datenordner auf dem Server, wenn er nicht `data` im Checkout ist (die linke Seite des Volumes in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Die Argumente von `rebuild.sh`, standardmäßig `--force`. Auf einem Server ohne Speicher für Anwendung und Build zusammen verwende `--force --stop-first`: Marginalia wird dann vor dem Build angehalten und ist für die ganze Zeit nicht erreichbar. |
| `MARGINALIA_CHECK_MINUTES` | Wie alt der Status werden darf, bevor ein Lauf ohne Anforderung das Remote erneut prüft; standardmäßig 15 (Minuten). Bei systemd bestimmt der Timer, wie oft der Wächter läuft. |

<a id="who-the-watcher-runs-as"></a>

### Als wer der Wächter läuft

Standardmäßig läuft der Wächter als **root**: Er muss Docker benutzen und in den Datenordner schreiben, der dem Benutzer des Containers gehört. Git selbst läuft als Besitzer des Checkouts, damit die Dateien von Git bei ihm bleiben.

Der Wächter führt aus, was der Branch enthält (`rebuild.sh`, die `Dockerfile`), wie jede Aktualisierung von Hand es auch tut: Folge nur einem Repository, dem du vertraust, und halte den Checkout nur für seinen Besitzer beschreibbar, denn wer dort `rebuild.sh` oder `watch.sh` ändern kann, führt Befehle als Benutzer des Wächters aus.

Mit `--user USER` (oder der Crontab dieses Benutzers) läuft er als jemand anderes. Dieser Benutzer muss Docker benutzen dürfen (was auf den meisten Systemen so viel ist wie root), muss den Checkout besitzen und muss in `data/update` und `data/extensions` schreiben können, die normalerweise dem Benutzer des Containers gehören.

<a id="windows-and-macos"></a>

### Windows und macOS

Für Docker Desktop gibt es keinen fertigen Wächter. Aktualisiere mit `rebuild.bat` / `rebuild.sh` von Hand oder aus einer geplanten Aufgabe, siehe [Automatische Aktualisierungen](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Fehlerbehebung

| | |
|---|---|
| *Der Update-Wächter läuft nicht auf dem Server* | Der Wächter hat seit einer Stunde keinen Status geschrieben oder nie einen. Prüfe `systemctl status marginalia-update.timer marginalia-update.path` und `journalctl -u marginalia-update.service`. Wenn der Datenordner nicht `data` im Checkout ist, setze `MARGINALIA_DATA_DIR`. |
| *Branch ... folgt keinem Remote-Branch* | Der Checkout steht auf einem Branch ohne Upstream (oder auf einem losgelösten Commit), daher gibt es nichts zu holen. Auf dem Server: `git switch <branch>` oder `git branch --set-upstream-to=origin/<branch>`. |
| *Der Server konnte das Git-Repository nicht erreichen* | `git fetch` ist bei der letzten Prüfung fehlgeschlagen (kein Netz, ein privates Repository ohne Zugangsdaten für den Besitzer des Checkouts). Der angezeigte neueste Commit ist möglicherweise alt. |
| Die Aktualisierung wurde angefordert, und nichts passiert | Mit cron startet sie innerhalb einer Minute. Eine Anforderung, die eine Stunde lang niemand abgeholt hat, wird verworfen, damit sie nicht überraschend startet, wenn der Wächter repariert wird. |
| *Letzte Aktualisierung: ..., fehlgeschlagen* | Lies **Protokoll**. Ein fehlgeschlagenes `git pull` bedeutet meist, dass der Checkout eigene Änderungen hat (bearbeitete Dateien, lokale Commits): Löse das auf dem Server. Ein fehlgeschlagener Build lässt die alte Version weiterlaufen. |

Die Dateien des Wächters liegen in `update` des Datenordners: `status.properties` (was der Tab anzeigt), `update.log` (die letzte Aktualisierung) und kurzzeitig `request`.
