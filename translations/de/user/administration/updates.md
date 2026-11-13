---
label: Aktualisierungen
order: 65
---

# Aktualisierungen

Auf einem [Docker-Server](../getting-started/docker-server.md), der aus einem Git-Checkout gebaut wurde, kann ein Administrator
Marginalia über den Reiter **Update** der Admin-Seite aktualisieren, ohne sich auf dem Server anzumelden: Der Server holt
die neueste Version des Zweigs, auf dem sein Checkout liegt, baut die Anwendung und die
[mitgelieferten Erweiterungen](../getting-started/docker-server.md#plugins) und startet neu.

Dafür braucht es ein kleines Programm auf dem Server, den **Update-Watcher**, der [einmalig eingerichtet](#setting-up-the-watcher)
wird. Ohne ihn zeigt der Reiter nur die laufende Version an und meldet, dass der Watcher nicht läuft. Genau das zeigen auch die
[Desktop-App](../getting-started/desktop-app.md) und ein Server mit dem
[fertigen Image](../getting-started/docker-server.md#prebuilt-image): Sie werden aktualisiert, indem die neue
Version installiert wird.

<a id="the-update-tab"></a>

## Der Reiter Update

| | |
|---|---|
| **Laufende Version** | Die Version und der Commit des laufenden Marginalia. |
| **Zweig** | Der Zweig, auf dem der Checkout auf dem Server liegt, und der Remote-Zweig, dem er folgt (zum Beispiel `dev`, das `origin/dev` folgt), der neueste Commit dieses Remote-Zweigs und wann der Server zuletzt nachgesehen hat. Der Server sieht alle 15 Minuten nach. |
| Status | *Eine neue Version ist verfügbar*, wenn der neueste Commit nicht der laufende ist, sonst *Marginalia ist auf dem neuesten Stand*. Während eine Aktualisierung läuft, zeigt der Reiter das an. |
| **Letzte Aktualisierung** | Wann die letzte Aktualisierung von hier aus endete und ob sie erfolgreich oder fehlgeschlagen ist. |

Der Server folgt dem Zweig, der dort ausgecheckt ist, egal welcher es ist: Um eine Installation auf einen anderen Zweig umzustellen,
führe `git switch` im Checkout auf dem Server aus; der Reiter zeigt den neuen Zweig nach dem nächsten Nachsehen an.

| Schaltfläche | |
|---|---|
| **Aktualisieren und neu starten** | Bittet den Server, nach einer Bestätigung zu aktualisieren. Es baut und startet auch dann neu, wenn nichts Neues vorliegt (zum Beispiel nach einer Änderung an `docker-compose.yml`). |
| **Aktualisieren** | Liest den Status erneut ein. Der Reiter aktualisiert sich nicht von selbst. |
| **Protokoll** | Die Ausgabe der letzten Aktualisierung: was Git und der Build geschrieben haben. |

<a id="what-an-update-does"></a>

## Was eine Aktualisierung tut

1. Die neue Version wird gebaut: `git pull`, die Erweiterungen, das Image. Das dauert mehrere Minuten, und **Marginalia läuft
   dabei weiter**. Der Reiter zeigt *Die Aktualisierung läuft*; **Protokoll** zeigt, wie weit sie ist.
2. Marginalia wird angehalten, die neuen Erweiterungen werden nach `data/extensions` kopiert (die installierten werden ersetzt)
   und die neue Version startet. Das dauert eine bis zwei Minuten (siehe [Zustand](../getting-started/docker-server.md#health)).
   Alle, die gerade arbeiten, werden unterbrochen, und was gerade erzeugt wird, wird angehalten: Die Bestätigung nennt dir,
   wie viele [Aufgaben](jobs.md) in diesem Moment laufen, also wähle eine ruhige Zeit.
3. Die Datenbank wird beim Start der neuen Version aktualisiert, nachdem eine [Kopie davon](database-backups.md#copy-before-an-upgrade)
   gespeichert wurde. Erstelle vorher auch selbst eine [Datenbanksicherung](database-backups.md).

Schlägt der Build fehl, wird nichts angehalten: Die Version von vorher läuft weiter, **Letzte Aktualisierung** sagt *fehlgeschlagen*,
und das Protokoll sagt, warum. Die nächste Aktualisierung baut erneut, auch dann, wenn nichts Neues zu holen ist.

<a id="setting-up-the-watcher"></a>

## Den Watcher einrichten

Marginalia läuft in einem Container und kann weder auf den Git-Checkout noch auf Docker zugreifen; ihm diesen Zugriff zu geben würde
jeden, der in die Anwendung gelangt, zum Administrator des Servers machen. Daher fragt es nur nach: Es schreibt eine Datei in seinen
[Datenordner](index.md#the-data-folder), und der Watcher, der auf dem Server außerhalb des Containers läuft, erledigt die Arbeit.
Der Watcher ist das Skript `tools/update-watcher/watch.sh` des Checkouts; er startet immer nur `rebuild.sh`
desselben Checkouts (siehe [Automatische Aktualisierungen](../getting-started/docker-server.md#automatic-updates)).

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
| `marginalia-update.path` | Startet ihn in dem Moment, in dem die Anwendung eine Aktualisierung anfordert. |
| `marginalia-update.timer` | Startet ihn zwei Minuten nach dem Booten und danach alle 15 Minuten, um nach neuen Commits zu suchen. |

Öffne **Admin → Update** und klicke auf **Aktualisieren**: Der Reiter zeigt den Zweig und den neuesten Commit. Mehr ist nicht nötig.

| Option | |
|---|---|
| `--name NAME` | Der Name der Units (Standard `marginalia-update`). Zwei Checkouts auf einem Rechner (eine Test- und eine Produktionsinstallation) brauchen je einen eigenen Namen. |
| `--user USER` | Führt den Watcher als diesen Benutzer statt als root aus, siehe [unten](#who-the-watcher-runs-as). |
| `--uninstall` | Stoppt und entfernt die Units (mit demselben `--name`). |

`systemctl status marginalia-update.service` zeigt den letzten Lauf, und `journalctl -u marginalia-update.service` zeigt seine
Ausgabe (derselbe Text wie **Protokoll**, sowie der Grund, wenn der Watcher selbst nicht arbeiten konnte). Die Units enthalten den
Pfad des Checkouts: Wenn du den Checkout verschiebst, führe `install.sh` erneut aus.

<a id="without-systemd-cron"></a>

### Ohne systemd (cron)

Führe den Watcher jede Minute aus; er tut nichts, wenn niemand eine Aktualisierung angefordert hat und er das Remote vor weniger als
15 Minuten angesehen hat, daher kostet das nichts:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Trage die Zeile in die Crontab von root (`sudo crontab -e`) ein oder in die eines anderen Benutzers, siehe unten. Eine Aktualisierung
startet dann innerhalb einer Minute nach dem Klick.

<a id="settings"></a>

### Einstellungen

Der Watcher liest diese Umgebungsvariablen (mit systemd: `sudo systemctl edit marginalia-update.service` und
Zeilen `Environment=NAME=wert` unter `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | Der Datenordner auf dem Server, falls er nicht `data` im Checkout ist (die linke Seite des Volumes in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Die Argumente von `rebuild.sh`, standardmäßig `--force`. Auf einem Server ohne genug Speicher für Anwendung und Build zusammen: `--force --stop-first` verwenden. Marginalia wird dann vor dem Build angehalten und ist für den gesamten Build nicht erreichbar. |
| `MARGINALIA_CHECK_MINUTES` | Wie alt der Status werden darf, bevor ein Lauf ohne Anfrage das Remote erneut ansieht; standardmäßig 15. Mit systemd bestimmt der Timer, wie oft der Watcher läuft. |

<a id="who-the-watcher-runs-as"></a>

### Als wer der Watcher läuft

Standardmäßig läuft der Watcher als **root**: Er muss Docker benutzen und in den Datenordner schreiben, der dem Benutzer des
Containers gehört. Git selbst läuft als Eigentümer des Checkouts, sodass die Dateien von Git ihm gehörig bleiben.

Der Watcher führt aus, was der Zweig enthält (`rebuild.sh`, das `Dockerfile`), so wie jede Aktualisierung von Hand es tut: Verwende
nur ein Repository, dem du vertraust, und halte den Checkout nur für seinen Eigentümer beschreibbar, denn wer dort `rebuild.sh` oder
`watch.sh` ändern kann, führt Befehle als Benutzer des Watchers aus.

Mit `--user USER` (oder der Crontab dieses Benutzers) läuft er als jemand anderes. Dieser Benutzer muss Docker verwenden dürfen
(was auf den meisten Systemen so viel bedeutet wie root), muss dem Checkout gehören und in `data/update` und `data/extensions`
schreiben dürfen, die normalerweise dem Benutzer des Containers gehören.

<a id="windows-and-macos"></a>

### Windows und macOS

Für Docker Desktop gibt es keinen fertigen Watcher. Aktualisiere von Hand mit `rebuild.bat` / `rebuild.sh` oder über eine geplante
Aufgabe, siehe [Automatische Aktualisierungen](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Fehlersuche

| | |
|---|---|
| *Der Update-Watcher läuft nicht auf dem Server* | Der Watcher hat seit einer Stunde oder überhaupt nie einen Status geschrieben. Prüfe `systemctl status marginalia-update.timer marginalia-update.path` und `journalctl -u marginalia-update.service`. Wenn der Datenordner nicht `data` im Checkout ist, setze `MARGINALIA_DATA_DIR`. |
| *Zweig ... folgt keinem Remote-Zweig* | Der Checkout liegt auf einem Zweig ohne Upstream (oder auf einem losgelösten Commit), daher gibt es nichts zu holen. Auf dem Server: `git switch <zweig>` oder `git branch --set-upstream-to=origin/<zweig>`. |
| *Der Server konnte das Git-Repository nicht erreichen* | `git fetch` ist beim letzten Nachsehen fehlgeschlagen (kein Netzwerk, ein privates Repository ohne Zugangsdaten für den Eigentümer des Checkouts). Der angezeigte neueste Commit ist möglicherweise veraltet. |
| Die Aktualisierung wurde angefordert, und nichts passiert | Mit cron startet sie innerhalb einer Minute. Eine Anfrage, die eine Stunde lang niemand abgeholt hat, wird verworfen, damit sie nicht überraschend startet, wenn der Watcher repariert wird. |
| *Letzte Aktualisierung: ..., fehlgeschlagen* | Lies **Protokoll**. Ein fehlgeschlagenes `git pull` bedeutet meist, dass der Checkout eigene Änderungen hat (bearbeitete Dateien, lokale Commits): Löse das auf dem Server. Ein fehlgeschlagener Build lässt die alte Version weiterlaufen. |

Die Dateien des Watchers liegen im Ordner `update` des Datenordners: `status.properties` (was der Reiter anzeigt), `update.log`
(die letzte Aktualisierung) und, für einen Moment, `request`.
