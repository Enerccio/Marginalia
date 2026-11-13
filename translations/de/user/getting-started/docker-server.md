---
label: Server (Docker)
order: 90
---

# Server (Docker)

Führe Marginalia als Server aus, wenn mehrere Personen es nutzen oder du es von anderen Geräten aus öffnen möchtest. Jeder Benutzer
erhält seine eigenen Bücher, Lorebooks, Inference-Provider und Protokolle.

<a id="requirements"></a>

## Voraussetzungen

- [Docker](https://docs.docker.com/get-docker/) mit Docker Compose
- Etwa 1 GB Arbeitsspeicher für Marginalia
- Für den Zugriff über das Internet: eine Domain und ein Reverse-Proxy mit HTTPS (siehe [unten](#https-and-reverse-proxy))

<a id="install"></a>

## Installation

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

Das erste `docker compose up` baut das Image aus dem Quellcode, was einige Minuten dauert. Danach lauscht Marginalia auf Port
**8080**. Öffne `http://<server>:8080` und lege das Administratorkonto an (siehe [Erster Start](first-start.md)).

<a id="prebuilt-image"></a>

### Fertiges Image

Statt aus dem Quellcode zu bauen, kannst du das Image verwenden, das mit jeder Version veröffentlicht wird (`linux/amd64` und `linux/arm64`).
Ersetze in `docker-compose.yml` den Block `build:` durch `image:`:

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
| `ports` | `"8080:8080"` veröffentlicht Marginalia auf Port 8080 des Hosts. Verwende `"127.0.0.1:8080:8080"`, wenn nur ein Reverse-Proxy auf demselben Host es erreichen soll. |
| `volumes` | `./data` enthält die Datenbank, Backups und Erweiterungen. Behalte es bei und nimm es in deine Server-Backups auf. Es wird beim ersten Start angelegt, falls es nicht existiert; beim Start setzt der Container als Besitzer den Benutzer, unter dem Marginalia im Container läuft (`jetty`), sodass die Dateien darin auf dem Host der ID dieses Benutzers gehören. |
| `-Xmx1g` | Maximaler Arbeitsspeicher. Erhöhe ihn (z. B. `-Xmx2g`) bei vielen Benutzern oder sehr großen Büchern. |

Nach einer Änderung der Datei wende sie mit `docker compose up -d` an.

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
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` im Image) | Ordner der Datenbank, Backups, Erweiterungen und des Schlüssels, der API-Schlüssel verschlüsselt. Ändere ihn nur zusammen mit einem passenden Volume (`./data:/dein/ordner`); der Container legt beim Start den Besitzer dieses Ordners fest. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Sprache der Anmeldeseite und der Benutzer, die keine gewählt haben (`en` oder `ja`; die Sprache einer Erweiterung kann hier nicht verwendet werden). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` blendet **Angemeldet bleiben** aus. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Wie lange eine gespeicherte Anmeldung gültig ist, in Sekunden (30 Tage). |
| `MARGINALIA_TRUSTED_PROXIES` | leer | Reverse-Proxys, deren `X-Forwarded-For`-Header vertraut wird, siehe [unten](#https-and-reverse-proxy). Ersetzt `-DtrustedProxies`. |

Dieselben Einstellungen können als Java-Optionen angegeben werden, `-Dmarginalia.default-language=ja`, die Vorrang vor den
Variablen haben. Ein Wert, der nicht passt (Text statt einer Anzahl von Sekunden, eine unbekannte Sprache), verhindert, dass Marginalia
startet; der Grund steht im Protokoll.

<a id="https-and-reverse-proxy"></a>

## HTTPS und Reverse-Proxy

!!!danger Kein unverschlüsseltes HTTP über das Internet
Passwörter und Anmelde-Cookies werden bei jeder Anfrage gesendet. Über unverschlüsseltes HTTP kann sie jeder auf dem Weg mitlesen.
Betreibe Marginalia hinter einem Reverse-Proxy mit HTTPS.
!!!

Die Option **Angemeldet bleiben** auf der Anmeldeseite benötigt ebenfalls HTTPS: Ihre Cookies sind als sicher markiert, daher behalten Browser sie
nur bei HTTPS-Verbindungen.

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

    client_max_body_size 100m;   # Uploads von Backups und Erweiterungen

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # lange Generierungen halten die Verbindung offen
    }
}
```
+++

Fehlgeschlagene Anmeldungen werden pro Benutzer und pro Client-Adresse gedrosselt (nach 30 Fehlversuchen von einer Adresse werden Anmeldungen von dort für eine wachsende Zeit abgelehnt, bis zu 15 Minuten). Marginalia vertraut nur dem `X-Forwarded-For`-Header eines Proxys, den du benennst: Ohne das erscheinen alle Benutzer so, als kämen sie von der Adresse des Proxys, und 30 Fehlversuche von irgendjemandem blockieren alle für eine Weile. Behalte den Header in der Proxy-Konfiguration (wie oben) und setze `MARGINALIA_TRUSTED_PROXIES=<Adresse oder Bereich des Proxys>`
im `environment` von `docker-compose.yml`. Ein Proxy auf dem Docker-Host erreicht den Container aus dem Docker-Netzwerk, normalerweise `172.16.0.0/12`; mehrere Einträge werden durch Kommas getrennt. Die Drosselung wird im Arbeitsspeicher gehalten, ein Neustart von Marginalia setzt sie zurück.

<a id="models-on-the-same-machine"></a>

## Modelle auf demselben Rechner

Innerhalb des Containers bezeichnet `localhost` den Container selbst, nicht den Server. Um einen Modellserver zu verwenden, der auf dem
Docker-Host läuft (llama.cpp, Ollama, LM Studio...), verwende die Adresse `http://host.docker.internal:<port>/v1` im
[Inference-Provider](../inference-providers.md). Unter Linux füge dies dem Dienst in `docker-compose.yml` hinzu:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

Der Modellserver muss außerdem auf einer Adresse lauschen, die der Container erreichen kann, nicht nur auf `127.0.0.1`.

<a id="updating"></a>

## Aktualisieren

```sh
git pull
docker compose up -d --build
```

Mit dem [fertigen Image](#prebuilt-image) verwende stattdessen `docker compose pull` und `docker compose up -d`.

Die Datenbank wird beim Start der neuen Version automatisch aktualisiert. Marginalia legt eine
[Kopie der Datenbank](../administration/database-backups.md#copy-before-an-upgrade) an, bevor es ihre Struktur ändert,
aber erstelle trotzdem vorher selbst ein [Datenbank-Backup](../administration/database-backups.md).

<a id="automatic-updates"></a>

### Automatische Aktualisierung

`rebuild.sh` (Linux, macOS) und `rebuild.bat` (Windows) im Wurzelverzeichnis des Repositorys erledigen die Aktualisierung für dich; starte sie von Hand oder aus einem Cron-Job oder einer geplanten Aufgabe. Sie holen den Remote-Stand des Branches, auf dem der Checkout gerade liegt (welcher auch immer das ist), und tun nichts, wenn es nichts Neues gibt (`--force` baut und startet trotzdem neu, zum Beispiel nach einer Änderung an `docker-compose.yml`). Andernfalls führen sie `git pull` aus, bauen die [mitgelieferten Erweiterungen](#plugins) und das Image **während Marginalia weiterläuft**, stoppen dann den Container, kopieren die Plugin-JARs nach `data/extensions` (die installierten werden ersetzt) und starten die neue Version. Schlägt ein Schritt fehl, bricht das Skript mit einer Meldung ab; betrifft es den Pull oder einen Build, läuft die alte Version weiter, und der nächste Lauf baut erneut.

`--stop-first` stoppt den Container vor dem Build, für einen Server, der nicht genug Arbeitsspeicher für Anwendung und Build gleichzeitig hat; Marginalia ist dann während des gesamten Builds nicht erreichbar.

Unter Linux müssen die Skripte gar nicht von Hand ausgeführt werden: Ist der Update-Watcher installiert, aktualisiert ein Administrator den Server per Schaltfläche, siehe [Aktualisierungen](../administration/updates.md).

<a id="plugins"></a>

## Erweiterungen

Das Docker-Image enthält die [Erweiterungen](../extensions/index.md) nicht, und sie müssen aus demselben
Quellcode wie die Anwendung gebaut werden. Dafür brauchst du weder Java noch Maven: `marginalia/plugins/docker-compose.yml` baut alle
in einem Container, der zuerst die Anwendung und dann jede Erweiterung baut, und kopiert die JARs nach
`marginalia/plugins/build`:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

Der erste Build dauert mehrere Minuten (er lädt die Abhängigkeiten der Anwendung herunter). Kopiere danach die JARs nach
`data/extensions` und starte Marginalia neu, oder lade sie unter *Admin → Extensions* (siehe
[Erweiterungen](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` erledigen das Kopieren für dich, sodass auf einem Server,
der sie verwendet, die Erweiterungen immer zusammen mit der Anwendung aktualisiert werden.

<a id="health"></a>

## Zustand

Das Image hat einen Docker-Health-Check: `docker ps` zeigt `(health: starting)`, während Marginalia startet (bis zu zwei
Minuten), danach `(healthy)`. Der Status ist `unhealthy`, wenn `http://localhost:8080/health` nicht mehr mit `200` antwortet. Docker
startet einen unhealthy Container nicht von selbst neu; nutze den Status für dein Monitoring oder ein Auto-Heal-Werkzeug.

`/health` benötigt keine Anmeldung, daher kann auch ein Monitoring-Werkzeug oder ein Load-Balancer es nutzen (über den Proxy:
`https://marginalia.example.com/health`). Es antwortet mit `200` und `{"status":"UP"}`, wenn Marginalia läuft und seine Datenbank
antwortet, und mit `503` und `{"status":"DOWN",...}`, während es startet oder etwas nicht stimmt. Es zeigt nichts weiter
(keine Version, keine Details); der Grund steht im Protokoll.

<a id="logs"></a>

## Protokolle

```sh
docker compose logs -f marginalia
```
