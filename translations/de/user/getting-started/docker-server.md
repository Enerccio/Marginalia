---
label: Server (Docker)
order: 90
---

# Server (Docker)

Betreiben Sie Marginalia als Server, wenn mehrere Personen es nutzen oder Sie es von anderen Geräten aus öffnen möchten. Jeder Benutzer
erhält eigene Bücher, Lorebooks, Inferenz-Anbieter und Protokolle.

<a id="requirements"></a>

## Voraussetzungen

- [Docker](https://docs.docker.com/get-docker/) mit Docker Compose
- Etwa 1 GB Arbeitsspeicher für Marginalia
- Für den Zugriff über das Internet: eine Domain und ein Reverse Proxy mit HTTPS (siehe [unten](#https-and-reverse-proxy))

<a id="install"></a>

## Installation

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

Der erste `docker compose up`-Aufruf erstellt das Image aus dem Quellcode, was einige Minuten dauert. Anschließend lauscht Marginalia auf Port
**8080**. Öffnen Sie `http://<server>:8080` und legen Sie das Administratorkonto an (siehe [Erster Start](first-start.md)).

<a id="prebuilt-image"></a>

### Fertiges Image

Statt aus dem Quellcode zu bauen, können Sie das Image verwenden, das mit jeder Version veröffentlicht wird (`linux/amd64` und `linux/arm64`).
Ersetzen Sie in `docker-compose.yml` den Block `build:` durch `image:`:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` ist die neueste Version, `edge` die Entwicklungsversion, und `1.1.0` (oder `1.1`) fixiert eine Version. Der Rest der
Datei bleibt gleich.

<a id="configuration"></a>

## Konfiguration

Alles wird in `docker-compose.yml` festgelegt:

```yaml
services:
  marginalia:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: marginalia-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - ./data:/var/marginalia/.marginalia
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/marginalia/.marginalia/heapdump.hprof
```

| Einstellung | Wirkung |
|---|---|
| `ports` | `"8080:8080"` veröffentlicht Marginalia auf Port 8080 des Hosts. Verwenden Sie `"127.0.0.1:8080:8080"`, wenn nur ein Reverse Proxy auf demselben Host den Dienst erreichen soll. |
| `volumes` | `./data` enthält die Datenbank, Sicherungen und Erweiterungen. Behalten Sie es bei und nehmen Sie es in Ihre Server-Sicherungen auf. Es wird beim ersten Start angelegt, falls es nicht existiert; beim Start macht der Container es zum Eigentum des Benutzers, unter dem Marginalia im Container läuft (`jetty`), sodass die Dateien darin auf dem Host der ID dieses Benutzers gehören. |
| `-Xmx1g` | Maximaler Arbeitsspeicher. Erhöhen Sie ihn (zum Beispiel `-Xmx2g`) bei vielen Benutzern oder sehr großen Büchern. |

Nach einer Änderung der Datei wenden Sie sie mit `docker compose up -d` an.

<a id="environment-variables"></a>

### Umgebungsvariablen

Die eigenen Einstellungen von Marginalia sind Umgebungsvariablen des Dienstes, neben `JAVA_TOOL_OPTIONS`:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| Variable | Standard | Wirkung |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` im Image) | Ordner der Datenbank, Sicherungen, Erweiterungen und des Schlüssels, der API-Schlüssel verschlüsselt. Ändern Sie ihn nur zusammen mit einem passenden Volume (`./data:/your/folder`); der Container legt beim Start den Eigentümer dieses Ordners fest. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Sprache der Anmeldeseite und der Benutzer, die keine gewählt haben (`en` oder `ja`; die Sprache einer Erweiterung kann hier nicht verwendet werden). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` blendet *Angemeldet bleiben* aus. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Wie lange eine gespeicherte Anmeldung gültig ist, in Sekunden (30 Tage). |
| `MARGINALIA_TRUSTED_PROXIES` | leer | Reverse Proxies, deren `X-Forwarded-For`-Header vertraut wird, siehe [unten](#https-and-reverse-proxy). Ersetzt `-DtrustedProxies`. |

Dieselben Einstellungen können als Java-Optionen angegeben werden, `-Dmarginalia.default-language=ja`, die Vorrang vor den
Variablen haben. Ein Wert, der nicht passt (Text bei der Anzahl der Sekunden, eine unbekannte Sprache), verhindert den Start
von Marginalia; der Grund steht im Protokoll.

<a id="https-and-reverse-proxy"></a>

## HTTPS und Reverse Proxy

!!!danger Kein unverschlüsseltes HTTP über das Internet
Passwörter und Anmelde-Cookies werden bei jeder Anfrage gesendet. Über unverschlüsseltes HTTP kann sie jeder auf dem Weg mitlesen.
Betreiben Sie Marginalia hinter einem Reverse Proxy mit HTTPS.
!!!

Die Option *Angemeldet bleiben* auf der Anmeldeseite benötigt ebenfalls HTTPS: Ihre Cookies sind als sicher markiert, daher behalten Browser
sie nur bei HTTPS-Verbindungen.

Marginalia aktualisiert die Seite über eine WebSocket-Verbindung, daher muss der Proxy WebSocket-Upgrades weiterleiten. Beispiele
für `marginalia.example.com`:

+++ Caddy
Caddy holt das Zertifikat und leitet WebSockets automatisch weiter.

```
marginalia.example.com {
    reverse_proxy localhost:8080
}
```
+++ nginx
```nginx
server {
    listen 443 ssl;
    server_name marginalia.example.com;

    ssl_certificate     /etc/letsencrypt/live/marginalia.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/marginalia.example.com/privkey.pem;

    client_max_body_size 100m;   # uploads of backups and extensions

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # long generations keep the connection open
    }
}
```
+++

Fehlgeschlagene Anmeldungen werden pro Benutzer und pro Client-Adresse gedrosselt (nach 30 Fehlversuchen von einer Adresse werden Anmeldungen von dort für eine wachsende Zeit abgelehnt, bis zu 15 Minuten). Marginalia vertraut nur dem `X-Forwarded-For`-Header eines Proxys, den Sie benennen: ohne ihn erscheinen alle Benutzer so, als kämen sie von der Adresse des Proxys, und 30 Fehlversuche von irgendjemandem blockieren alle für eine Weile. Behalten Sie den Header in der Proxy-Konfiguration (wie oben) und setzen Sie `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>`
in der `environment` von `docker-compose.yml`. Ein Proxy auf dem Docker-Host erreicht den Container aus dem Docker-Netzwerk, normalerweise `172.16.0.0/12`; mehrere Einträge werden durch Kommas getrennt. Die Drosselung wird im Arbeitsspeicher gehalten, ein Neustart von Marginalia setzt sie zurück.

<a id="models-on-the-same-machine"></a>

## Modelle auf derselben Maschine

Innerhalb des Containers ist `localhost` der Container selbst, nicht der Server. Um einen Modellserver zu verwenden, der auf dem
Docker-Host läuft (llama.cpp, Ollama, LM Studio...), verwenden Sie die Adresse `http://host.docker.internal:<port>/v1` im
[Inferenz-Anbieter](../inference-providers.md). Unter Linux fügen Sie dies im Dienst in `docker-compose.yml` hinzu:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

Der Modellserver muss außerdem auf einer Adresse lauschen, die der Container erreichen kann, nicht nur auf `127.0.0.1`.

<a id="updating"></a>

## Aktualisierung

```sh
git pull
docker compose up -d --build
```

Mit dem [fertigen Image](#prebuilt-image) verwenden Sie stattdessen `docker compose pull` und `docker compose up -d`.

Die Datenbank wird automatisch aktualisiert, wenn die neue Version startet. Marginalia speichert vor einer Änderung ihrer Struktur eine
[Kopie der Datenbank](../administration/database-backups.md#copy-before-an-upgrade), dennoch sollten Sie vorher auch selbst ein [Datenbank-Backup](../administration/database-backups.md) anlegen.

<a id="automatic-updates"></a>

### Automatische Aktualisierung

`rebuild.sh` (Linux, macOS) und `rebuild.bat` (Windows) im Wurzelverzeichnis des Repositorys erledigen die Aktualisierung für Sie; starten Sie sie
von Hand oder aus einem Cron-Job oder einer geplanten Aufgabe. Sie holen den Remote des Branches, auf dem der Checkout liegt (welcher auch immer das ist),
und tun nichts, wenn nichts Neues vorliegt (`--force` baut und startet trotzdem neu, etwa nach einer Änderung von
`docker-compose.yml`). Andernfalls `git pull` sie, bauen die [mitgelieferten Erweiterungen](#plugins) und das Image, **während
Marginalia weiterläuft**, stoppen dann den Container, kopieren die Plugin-JARs nach `data/extensions` (die installierten werden ersetzt) und starten die neue Version. Schlägt ein Schritt fehl, bricht das Skript mit einer Meldung ab; betrifft es den Pull
oder einen Build, läuft die alte Version noch, und der nächste Lauf baut erneut.

`--stop-first` stoppt den Container vor dem Build, für einen Server, der nicht genug Arbeitsspeicher für die Anwendung
und den Build zusammen hat; Marginalia ist dann während des gesamten Builds nicht erreichbar.

Unter Linux müssen die Skripte gar nicht von Hand gestartet werden: Ist der Update-Watcher installiert, aktualisiert ein Administrator
den Server mit einem Knopf, siehe [Aktualisierungen](../administration/updates.md).

<a id="plugins"></a>

## Erweiterungen

Das Docker-Image enthält die [Erweiterungen](../extensions/index.md) nicht; sie müssen aus demselben
Quellcode wie die Anwendung gebaut werden. Dafür brauchen Sie weder Java noch Maven: `marginalia/plugins/docker-compose.yml` baut alle
von ihnen in einem Container, der zuerst die Anwendung und dann jede Erweiterung baut, und kopiert die JARs nach
`marginalia/plugins/build`:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

Der erste Build dauert mehrere Minuten (er lädt die Abhängigkeiten der Anwendung herunter). Kopieren Sie danach die JARs nach
`data/extensions` und starten Sie Marginalia neu, oder laden Sie sie in *Admin → Erweiterungen* (siehe
[Erweiterungen](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` erledigen das Kopieren für Sie, sodass die Erweiterungen auf einem Server,
der sie verwendet, immer zusammen mit der Anwendung aktualisiert werden.

<a id="health"></a>

## Zustand

Das Image hat einen Docker-Health-Check: `docker ps` zeigt `(health: starting)`, während Marginalia startet (bis zu zwei
Minuten), danach `(healthy)`. Er steht auf `unhealthy`, wenn `http://localhost:8080/health` nicht mehr mit `200` antwortet. Docker
startet einen fehlerhaften Container nicht von selbst neu; nutzen Sie den Status für Ihr Monitoring oder ein Auto-Heal-Werkzeug.

`/health` benötigt keine Anmeldung, daher können auch ein Monitoring-Werkzeug oder ein Load Balancer es nutzen (über den Proxy:
`https://marginalia.example.com/health`). Es antwortet mit `200` und `{"status":"UP"}`, wenn Marginalia läuft und seine Datenbank
antwortet, und mit `503` und `{"status":"DOWN",...}`, während es startet oder etwas nicht stimmt. Es zeigt nichts weiter
(keine Version, keine Details); der Grund steht im Protokoll.

<a id="logs"></a>

## Protokolle

```sh
docker compose logs -f marginalia
```
