---
label: Server (Docker)
order: 90
---

# Server (Docker)

Betreibe Marginalia als Server, wenn mehrere Personen es nutzen oder du von anderen Geräten darauf zugreifen möchtest. Jeder Benutzer hat seine eigenen Bücher, Lorebooks, Inferenz-Anbieter und Protokolle.

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

Der erste `docker compose up` baut das Image aus dem Quellcode, was einige Minuten dauert. Marginalia lauscht danach auf Port **8080**. Öffne `http://<server>:8080` und lege das Administratorkonto an (siehe [Erster Start](first-start.md)).

<a id="prebuilt-image"></a>

### Vorgefertigtes Image

Statt aus dem Quellcode zu bauen, kannst du das Image verwenden, das mit jeder Version veröffentlicht wird (`linux/amd64` und `linux/arm64`). Ersetze in `docker-compose.yml` den `build:`-Block durch `image:`:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` ist die neueste Version, `edge` die Entwicklungsversion, und `1.1.0` (oder `1.1`) fixiert eine Version. Der Rest der Datei bleibt gleich.

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
| `ports` | `"8080:8080"` veröffentlicht Marginalia auf Port 8080 des Hosts. Verwende `"127.0.0.1:8080:8080"`, wenn nur ein Reverse Proxy auf demselben Host sie erreichen soll. |
| `volumes` | `./data` enthält die Datenbank, Sicherungen und Erweiterungen. Behalte ihn und nimm ihn in deine Server-Sicherungen auf. Er wird beim ersten Start angelegt, falls er nicht existiert; beim Start macht der Container ihn dem Benutzer gehörig, unter dem Marginalia im Container läuft (`jetty`), sodass die Dateien darin auf dem Host der Benutzer-ID dieses Benutzers gehören. |
| `-Xmx1g` | Maximaler Arbeitsspeicher. Erhöhe ihn (zum Beispiel auf `-Xmx2g`) bei vielen Benutzern oder sehr großen Büchern. |

Nach einer Änderung der Datei wendest du sie mit `docker compose up -d` an.

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
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` im Image) | Ordner der Datenbank, Sicherungen, Erweiterungen und des Schlüssels, der die API-Schlüssel verschlüsselt. Ändere ihn nur zusammen mit einem passenden Volume (`./data:/your/folder`); der Container setzt den Eigentümer dieses Ordners beim Start fest. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Sprache der Anmeldeseite und der Benutzer, die keine Sprache gewählt haben (`en` oder `ja`; die Sprache einer Erweiterung kann hier nicht verwendet werden). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` blendet *Save login* aus. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Wie lange ein gespeichertes Login gilt, in Sekunden (30 Tage). |
| `MARGINALIA_TRUSTED_PROXIES` | leer | Reverse Proxys, deren `X-Forwarded-For`-Header vertraut wird, siehe [unten](#https-and-reverse-proxy). Ersetzt `-DtrustedProxies`. |
| `MARGINALIA_COOKIE_NAMESPACE` | leer | Ein Name, der den Namen der *Save login*-Cookies vorangestellt wird, siehe [Mehrere Anwendungen auf einem Host](#several-applications-on-one-host). Wenn du ihn setzt, werden alle abgemeldet, die das Login gespeichert haben. |
| `MARGINALIA_SESSION_COOKIE` | `JSESSIONID_MARGINALIA` im Image (`JSESSIONID` außerhalb davon) | Name des Session-Cookies, siehe [Mehrere Anwendungen auf einem Host](#several-applications-on-one-host). |

Dieselben Einstellungen können als Java-Optionen angegeben werden, `-Dmarginalia.default-language=ja`, die Vorrang vor den Variablen haben. Ein Wert, der nicht passt (Text statt einer Anzahl von Sekunden, eine unbekannte Sprache), hält Marginalia am Start; der Grund steht im Log.

<a id="https-and-reverse-proxy"></a>

## HTTPS und Reverse Proxy

!!!danger Kein unverschlüsseltes HTTP über das Internet
Passwörter und Login-Cookies werden bei jeder Anfrage gesendet. Über unverschlüsseltes HTTP kann jeder auf dem Weg sie mitlesen.
Stelle Marginalia hinter einen Reverse Proxy mit HTTPS.
!!!

Die Option *Save login* auf der Anmeldeseite benötigt ebenfalls HTTPS: Ihre Cookies sind als secure markiert, sodass Browser sie nur über HTTPS-Verbindungen behalten.

Marginalia aktualisiert die Seite über eine WebSocket-Verbindung, daher muss der Proxy WebSocket-Upgrades weiterleiten. Beispiele für `marginalia.example.com`:

+++ Caddy
Caddy besorgt das Zertifikat und leitet WebSockets automatisch weiter.

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

Fehlgeschlagene Anmeldungen werden pro Benutzer und pro Client-Adresse gedrosselt (nach 30 Fehlversuchen von einer Adresse werden Anmeldungen von dort für eine wachsende Zeit abgelehnt, bis zu 15 Minuten). Marginalia vertraut nur dem `X-Forwarded-For`-Header eines Proxys, den du nennst: Ohne ihn erscheinen alle Benutzer mit der Adresse des Proxys, und 30 Fehlversuche von irgendjemandem blockieren alle für eine Weile. Behalte den Header in der Proxy-Konfiguration (wie oben) und setze `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>` in der `environment` von `docker-compose.yml`. Ein Proxy auf dem Docker-Host erreicht den Container aus dem Docker-Netz, normalerweise `172.16.0.0/12`; mehrere Einträge werden durch Kommas getrennt. Die Drosselung wird im Speicher gehalten; ein Neustart von Marginalia setzt sie zurück.

<a id="several-applications-on-one-host"></a>

### Mehrere Anwendungen auf einem Host

Browser speichern Cookies pro Host-Name, **nicht pro Port**: Marginalia auf `localhost:8080` und ein anderes Programm auf `localhost:8090` (oder zwei Programme unter einer Domain hinter einem Proxy) teilen sich die Cookies von `localhost`. Ist das andere Programm ebenfalls eine Java-Webanwendung (eine andere Anwendung auf der Plattform von Marginalia, ein zweites Marginalia), verwenden beide das Session-Cookie `JSESSIONID`, und jeder Besuch bei einem meldet dich beim anderen ab. Das Image gibt seinem Session-Cookie deshalb den Namen `JSESSIONID_MARGINALIA`. Wenn du zwei Marginalia-Container auf einem Host betreibst, gib dem zweiten andere Namen: `MARGINALIA_SESSION_COOKIE=JSESSIONID_MARGINALIA2` und `MARGINALIA_COOKIE_NAMESPACE=second` (die Cookies von *Save login*). Ein eigener Host-Name für jede Instanz (`marginalia.example.com`, `other.example.com`) vermeidet das ohne weitere Einstellungen.

<a id="models-on-the-same-machine"></a>

## Modelle auf demselben Rechner

Im Container ist `localhost` der Container selbst, nicht der Server. Um einen Modellserver zu nutzen, der auf dem Docker-Host läuft (llama.cpp, Ollama, LM Studio ...), verwende die Adresse `http://host.docker.internal:<port>/v1` im [Inferenz-Anbieter](../inference-providers.md). Füge unter Linux dazu in `docker-compose.yml` beim Dienst Folgendes hinzu:

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

Mit dem [vorgefertigten Image](#prebuilt-image) verwendest du stattdessen `docker compose pull` und `docker compose up -d`.

Die Datenbank wird beim Start der neuen Version automatisch aktualisiert. Marginalia speichert eine [Kopie der Datenbank](../administration/database-backups.md#copy-before-an-upgrade), bevor es ihre Struktur ändert, aber erstelle trotzdem vorher selbst eine [Datenbanksicherung](../administration/database-backups.md).

<a id="automatic-updates"></a>

### Automatische Aktualisierungen

`rebuild.sh` (Linux, macOS) und `rebuild.bat` (Windows) im Wurzelverzeichnis des Repositorys erledigen die Aktualisierung für dich; führe sie von Hand aus oder über einen Cron-Job oder eine geplante Aufgabe. Sie holen das Remote des Branches, auf dem der Checkout gerade liegt (egal welcher), und tun nichts, wenn nichts Neues vorhanden ist (`--force` baut und startet trotzdem neu, zum Beispiel nach einer Änderung von `docker-compose.yml`). Andernfalls ziehen sie die Änderungen mit `git pull` herunter, bauen die [mitgelieferten Plugins](#plugins) und das Image **während Marginalia weiterläuft**, dann stoppen sie den Container, kopieren die Plugin-JARs nach `data/extensions` (die installierten werden ersetzt) und starten die neue Version. Schlägt ein Schritt fehl, bricht das Skript mit einer Meldung ab; betrifft es den Pull oder einen Build, läuft noch die alte Version, und der nächste Lauf baut erneut.

`--stop-first` stoppt den Container vor dem Build, für einen Server, dessen Arbeitsspeicher nicht für Anwendung und Build zugleich reicht; Marginalia ist dann während des gesamten Builds nicht erreichbar.

Unter Linux müssen die Skripte gar nicht von Hand ausgeführt werden: Ist der Update-Watcher installiert, aktualisiert ein Administrator den Server mit einem Button, siehe [Aktualisierungen](../administration/updates.md).

## Plugins

Das Docker-Image enthält die [Erweiterungen](../extensions/index.md) nicht; sie müssen aus demselben Quellcode gebaut werden wie die Anwendung. Dafür brauchst du weder Java noch Maven: `marginalia/plugins/docker-compose.yml` baut alle in einem Container, der zuerst die Anwendung und dann jedes Plugin baut, und kopiert die JARs nach `marginalia/plugins/build`:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

Der erste Build dauert mehrere Minuten (er lädt die Abhängigkeiten der Anwendung herunter). Kopiere die JARs danach nach `data/extensions` und starte Marginalia neu, oder lade sie unter *Admin → Extensions* (siehe [Erweiterungen](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` erledigen das Kopieren für dich, sodass auf einem Server, der sie verwendet, die Plugins immer zusammen mit der Anwendung aktualisiert werden.
<a id="health"></a>

## Zustandsprüfung

Das Image hat einen Docker-Healthcheck: `docker ps` zeigt `(health: starting)`, während Marginalia startet (bis zu zwei Minuten), danach `(healthy)`. Er ist `unhealthy`, wenn `http://localhost:8080/health` nicht mehr mit `200` antwortet. Docker startet einen unhealthy Container nicht von selbst neu. Nutze den Status für dein Monitoring oder ein Auto-Heal-Tool.

`/health` benötigt keinen Login, daher können ein Monitoring-Tool oder ein Load Balancer ihn ebenfalls nutzen (über den Proxy: `https://marginalia.example.com/health`). Er antwortet `200` und `{"status":"UP"}`, wenn Marginalia läuft und seine Datenbank antwortet, und `503` mit `{"status":"DOWN",...}`, während es startet oder etwas nicht stimmt. Er zeigt nichts weiter (keine Version, keine Details). Der Grund steht im Log.

<a id="logs"></a>

## Protokolle

```sh
docker compose logs -f marginalia
```
