---
label: Server (Docker)
order: 90
---

# Server (Docker)

Usa Marginalia come server quando più persone lo utilizzano, o quando vuoi aprirlo da altri dispositivi. Ogni utente ha i propri libri, lorebook, provider di inferenza e protocolli.

<a id="requirements"></a>

## Requisiti

- [Docker](https://docs.docker.com/get-docker/) con Docker Compose
- Circa 1 GB di memoria per Marginalia
- Per l'accesso da internet: un dominio e un reverse proxy con HTTPS (vedi [sotto](#https-and-reverse-proxy))

<a id="install"></a>

## Installazione

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

Il primo `docker compose up` compila l'immagine dai sorgenti, operazione che richiede qualche minuto. Marginalia poi resta in ascolto sulla porta **8080**. Apri `http://<server>:8080` e crea l'account amministratore (vedi [Primo avvio](first-start.md)).

<a id="prebuilt-image"></a>

### Immagine precompilata

Invece di compilare dai sorgenti puoi usare l'immagine pubblicata con ogni release (`linux/amd64` e `linux/arm64`).
In `docker-compose.yml`, sostituisci il blocco `build:` con `image:`:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` è l'ultima release, `edge` la versione di sviluppo, e `1.1.0` (o `1.1`) fissa una release. Il resto del file resta invariato.

<a id="configuration"></a>

## Configurazione

Tutto si imposta in `docker-compose.yml`:

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

| Impostazione | Cosa fa |
|---|---|
| `ports` | `"8080:8080"` pubblica Marginalia sulla porta 8080 dell'host. Usa `"127.0.0.1:8080:8080"` quando solo un reverse proxy sullo stesso host deve raggiungerlo. |
| `volumes` | `./data` contiene il database, i backup e le estensioni. Conservalo e includilo nei backup del server. Viene creato al primo avvio se non esiste; all'avvio il container lo assegna all'utente con cui Marginalia gira nel container (`jetty`), quindi i file al suo interno appartengono all'ID di quell'utente sull'host. |
| `-Xmx1g` | Memoria massima. Aumentala (per esempio `-Xmx2g`) per molti utenti o libri molto grandi. |

Dopo aver modificato il file, applicalo con `docker compose up -d`.

<a id="environment-variables"></a>

### Variabili d'ambiente

Le impostazioni di Marginalia sono variabili d'ambiente del servizio, accanto a `JAVA_TOOL_OPTIONS`:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| Variabile | Predefinito | Cosa fa |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` nell'immagine) | Cartella del database, dei backup, delle estensioni e della chiave che cifra le chiavi API. Cambiala solo con un volume corrispondente (`./data:/your/folder`); il container imposta il proprietario di questa cartella all'avvio. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Lingua della pagina di accesso e degli utenti che non ne hanno scelta una (`en` o `ja`; qui non si può usare la lingua di un'estensione). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` nasconde *Ricorda l'accesso*. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Per quanto tempo resta valido un accesso ricordato, in secondi (30 giorni). |
| `MARGINALIA_TRUSTED_PROXIES` | vuoto | Reverse proxy di cui si considera attendibile l'header `X-Forwarded-For`, vedi [sotto](#https-and-reverse-proxy). Sostituisce `-DtrustedProxies`. |

Le stesse impostazioni si possono passare come opzioni Java, `-Dmarginalia.default-language=ja`, che hanno la precedenza sulle variabili. Un valore non valido (un testo al posto del numero di secondi, una lingua sconosciuta) impedisce l'avvio di Marginalia; il motivo è nel log.

<a id="https-and-reverse-proxy"></a>

## HTTPS e reverse proxy

!!!danger Non usare HTTP semplice su internet
Password e cookie di accesso vengono inviati con ogni richiesta. Su HTTP semplice chiunque lungo il percorso può leggerli.
Metti Marginalia dietro un reverse proxy con HTTPS.
!!!

L'opzione *Ricorda l'accesso* nella schermata di accesso richiede anche HTTPS: i suoi cookie sono marcati come sicuri, quindi i browser li conservano solo sulle connessioni HTTPS.

Marginalia aggiorna la pagina tramite una connessione WebSocket, quindi il proxy deve inoltrare gli upgrade WebSocket. Esempi per `marginalia.example.com`:

+++ Caddy
Caddy ottiene il certificato e inoltra i WebSocket automaticamente.

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

I tentativi di accesso falliti sono limitati per utente e per indirizzo del client (dopo 30 errori da un indirizzo, gli accessi da quell'indirizzo vengono rifiutati per un tempo crescente, fino a 15 minuti). Marginalia considera attendibile l'header `X-Forwarded-For` solo di un proxy che indichi esplicitamente: senza di esso, tutti gli utenti sembrano provenire dall'indirizzo del proxy e 30 errori di chiunque bloccano tutti per un po'. Mantieni l'header nella configurazione del proxy (come sopra) e imposta `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>` nelle `environment` di `docker-compose.yml`. Un proxy sull'host Docker raggiunge il container dalla rete Docker, normalmente `172.16.0.0/12`; più voci si separano con virgole. La limitazione è mantenuta in memoria: riavviare Marginalia la azzera.

<a id="models-on-the-same-machine"></a>

## Modelli sulla stessa macchina

Dentro il container, `localhost` indica il container stesso, non il server. Per usare un server di modelli in esecuzione sull'host Docker (llama.cpp, Ollama, LM Studio...), usa l'indirizzo `http://host.docker.internal:<port>/v1` nel [provider di inferenza](../inference-providers.md). Su Linux, aggiungi questo al servizio in `docker-compose.yml`:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

Anche il server di modelli deve essere in ascolto su un indirizzo raggiungibile dal container, non solo su `127.0.0.1`.

<a id="updating"></a>

## Aggiornamento

```sh
git pull
docker compose up -d --build
```

Con l'[immagine precompilata](#prebuilt-image) usa invece `docker compose pull` e `docker compose up -d`.

Il database viene aggiornato automaticamente all'avvio della nuova versione. Marginalia salva una [copia del database](../administration/database-backups.md#copy-before-an-upgrade) prima di modificarne la struttura, ma fai anche tu prima un [backup del database](../administration/database-backups.md).

<a id="automatic-updates"></a>

### Aggiornamenti automatici

`rebuild.sh` (Linux, macOS) e `rebuild.bat` (Windows) nella radice del repository eseguono l'aggiornamento al posto tuo; lanciali a mano o da un cron job o un'attività pianificata. Scaricano il remote del branch su cui si trova il checkout (qualunque sia) e non fanno nulla se non c'è niente di nuovo (`--force` ricompila e riavvia comunque, per esempio dopo aver modificato `docker-compose.yml`). Altrimenti `git pull`, compilano i [plugin inclusi](#plugins) e l'immagine **mentre Marginalia continua a funzionare**, poi fermano il container, copiano i JAR dei plugin in `data/extensions` (sostituendo quelli installati) e avviano la nuova versione. Se un passaggio fallisce, lo script si ferma con un messaggio; se è il pull o una compilazione, la vecchia versione è ancora in esecuzione e la volta successiva si ricompila.

`--stop-first` ferma il container prima della compilazione, per un server che non ha memoria sufficiente per l'applicazione e la compilazione insieme; Marginalia resta quindi non disponibile per tutta la compilazione.

Su Linux gli script non devono nemmeno essere lanciati a mano: con il controllore degli aggiornamenti installato, un amministratore aggiorna il server con un pulsante, vedi [Aggiornamenti](../administration/updates.md).

<a id="plugins"></a>

## Plugin

L'immagine Docker non contiene le [estensioni](../extensions/index.md), che vanno compilate dagli stessi sorgenti dell'applicazione. Per farlo non servono Java né Maven: `marginalia/plugins/docker-compose.yml` compila tutto in un container, che prima compila l'applicazione e poi ogni plugin, e copia i JAR in `marginalia/plugins/build`:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

La prima compilazione richiede diversi minuti (scarica le dipendenze dell'applicazione). Poi copia i JAR in `data/extensions` e riavvia Marginalia, oppure caricali da *Admin → Extensions* (vedi [Estensioni](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` fanno la copia al posto tuo, quindi su un server che li usa i plugin vengono sempre aggiornati insieme all'applicazione.

<a id="health"></a>

## Stato di salute

L'immagine ha un health check Docker: `docker ps` mostra `(health: starting)` mentre Marginalia si avvia (fino a due minuti), poi `(healthy)`. Diventa `unhealthy` quando `http://localhost:8080/health` smette di rispondere con `200`. Docker non riavvia da solo un container non sano; usa lo stato per il tuo monitoraggio o per uno strumento di auto-heal.

`/health` non richiede accesso, quindi può usarlo anche uno strumento di monitoraggio o un load balancer (tramite il proxy: `https://marginalia.example.com/health`). Risponde `200` e `{"status":"UP"}` quando Marginalia è in esecuzione e il suo database risponde, e `503` con `{"status":"DOWN",...}` mentre si sta avviando o quando qualcosa non va. Non mostra altro (nessuna versione, nessun dettaglio); il motivo è nel log.

<a id="logs"></a>

## Log

```sh
docker compose logs -f marginalia
```
