---
label: Server (Docker)
order: 90
---

# Server (Docker)

Esegui Marginalia come server quando lo usano più persone, o quando vuoi aprirlo da altri dispositivi. Ogni utente
ha i propri libri, lorebook, fornitori di inferenza e protocolli.

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

Il primo `docker compose up` costruisce l'immagine dal codice sorgente, operazione che richiede alcuni minuti. Marginalia poi resta in ascolto sulla porta
**8080**. Apri `http://<server>:8080` e crea l'account amministratore (vedi [Primo avvio](first-start.md)).

<a id="prebuilt-image"></a>

### Immagine precompilata

Invece di costruire dal codice sorgente puoi usare l'immagine pubblicata con ogni release (`linux/amd64` e `linux/arm64`).
In `docker-compose.yml`, sostituisci il blocco `build:` con `image:`:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` è la release più recente, `edge` la versione di sviluppo, e `1.1.0` (o `1.1`) fissa una release. Il resto
del file resta invariato.

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

| Impostazione | A cosa serve |
|---|---|
| `ports` | `"8080:8080"` pubblica Marginalia sulla porta 8080 dell'host. Usa `"127.0.0.1:8080:8080"` quando deve raggiungerlo solo un reverse proxy sullo stesso host. |
| `volumes` | `./data` contiene il database, i backup e le estensioni. Conservala e includila nei backup del server. Viene creata al primo avvio se non esiste; all'avvio il container la rende di proprietà dell'utente con cui Marginalia gira all'interno del container (`jetty`), quindi i file al suo interno appartengono all'ID di quell'utente sull'host. |
| `-Xmx1g` | Memoria massima. Aumentala (ad esempio `-Xmx2g`) per molti utenti o libri molto grandi. |

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

| Variabile | Predefinito | A cosa serve |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` nell'immagine) | Cartella del database, dei backup, delle estensioni e della chiave che cifra le chiavi API. Cambiala solo con un volume corrispondente (`./data:/your/folder`); il container imposta il proprietario di questa cartella all'avvio. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Lingua della pagina di accesso e degli utenti che non ne hanno scelta una (`en` o `ja`; la lingua di un'estensione non può essere usata qui). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` nasconde *Ricorda accesso*. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Durata di un accesso salvato, in secondi (30 giorni). |
| `MARGINALIA_TRUSTED_PROXIES` | vuoto | Reverse proxy di cui viene considerata attendibile l'intestazione `X-Forwarded-For`, vedi [sotto](#https-and-reverse-proxy). Sostituisce `-DtrustedProxies`. |
| `MARGINALIA_COOKIE_NAMESPACE` | vuoto | Un nome da anteporre ai nomi dei cookie di *Ricorda accesso*, vedi [Più applicazioni sullo stesso host](#several-applications-on-one-host). Impostarlo disconnette tutti quelli che hanno salvato l'accesso. |
| `MARGINALIA_SESSION_COOKIE` | `JSESSIONID_MARGINALIA` nell'immagine (`JSESSIONID` fuori di essa) | Nome del cookie di sessione, vedi [Più applicazioni sullo stesso host](#several-applications-on-one-host). |

Le stesse impostazioni si possono passare come opzioni Java, `-Dmarginalia.default-language=ja`, che hanno la precedenza sulle
variabili. Un valore non valido (testo al posto del numero di secondi, una lingua sconosciuta) impedisce a Marginalia
di avviarsi; il motivo è nel log.

<a id="https-and-reverse-proxy"></a>

## HTTPS e reverse proxy

!!!danger Non usare HTTP semplice su internet
Password e cookie di accesso vengono inviati con ogni richiesta. Su HTTP semplice chiunque sul percorso può leggerli.
Metti Marginalia dietro un reverse proxy con HTTPS.
!!!

Anche l'opzione *Ricorda accesso* della schermata di accesso richiede HTTPS: i suoi cookie sono contrassegnati come sicuri, quindi i browser
li conservano solo sulle connessioni HTTPS.

Marginalia aggiorna la pagina tramite una connessione WebSocket, quindi il proxy deve inoltrare gli upgrade WebSocket. Esempi
per `marginalia.example.com`:

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

I tentativi di accesso falliti vengono limitati per utente e per indirizzo del client (dopo 30 fallimenti da un indirizzo, gli accessi da esso vengono rifiutati per un tempo crescente, fino a 15 minuti). Marginalia considera attendibile l'intestazione `X-Forwarded-For` solo di un proxy che indichi esplicitamente: senza di essa, tutti gli utenti risultano provenire dall'indirizzo del proxy, e 30 fallimenti di chiunque bloccano tutti per un po'. Mantieni l'intestazione nella configurazione del proxy (come sopra) e imposta `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>`
nel `environment` di `docker-compose.yml`. Un proxy sull'host Docker raggiunge il container dalla rete Docker, di solito `172.16.0.0/12`; più voci si separano con virgole. La limitazione è mantenuta in memoria: riavviare Marginalia la azzera.

<a id="several-applications-on-one-host"></a>

### Più applicazioni sullo stesso host

I browser conservano i cookie per nome host, **non per porta**: Marginalia su `localhost:8080` e un altro programma su
`localhost:8090` (o due programmi sullo stesso dominio dietro un unico proxy) condividono i cookie di `localhost`. Quando l'altro programma
è anch'esso un'applicazione web Java (un'altra applicazione costruita sulla piattaforma di Marginalia, una seconda istanza di Marginalia), entrambe usano il
cookie di sessione `JSESSIONID` e ogni visita a una delle due ti disconnette dall'altra. L'immagine quindi dà al
proprio cookie di sessione il nome `JSESSIONID_MARGINALIA`. Quando esegui due container Marginalia sullo stesso host, dai al secondo
altri nomi: `MARGINALIA_SESSION_COOKIE=JSESSIONID_MARGINALIA2` e `MARGINALIA_COOKIE_NAMESPACE=second` (i cookie di *Ricorda accesso*). Un
nome host separato per ciascuno (`marginalia.example.com`, `other.example.com`) evita il problema senza impostazioni.

<a id="models-on-the-same-machine"></a>

## Modelli sulla stessa macchina

Dentro il container, `localhost` indica il container stesso, non il server. Per usare un server di modelli che gira sull'host Docker (llama.cpp, Ollama, LM Studio...), usa l'indirizzo `http://host.docker.internal:<port>/v1` nel
[fornitore di inferenza](../inference-providers.md). Su Linux, aggiungi questo al servizio in `docker-compose.yml`:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

Anche il server dei modelli deve restare in ascolto su un indirizzo raggiungibile dal container, non solo su `127.0.0.1`.

<a id="updating"></a>

## Aggiornamento

```sh
git pull
docker compose up -d --build
```

Con l'[immagine precompilata](#prebuilt-image) usa invece `docker compose pull` e `docker compose up -d`.

Il database viene aggiornato automaticamente all'avvio della nuova versione. Marginalia salva una
[copia del database](../administration/database-backups.md#copy-before-an-upgrade) prima di modificarne la struttura,
ma fai comunque prima un tuo [backup del database](../administration/database-backups.md).

<a id="automatic-updates"></a>

### Aggiornamenti automatici

`rebuild.sh` (Linux, macOS) e `rebuild.bat` (Windows) nella radice del repository fanno l'aggiornamento per te; eseguili a
mano o da un cron job o un'attività pianificata. Recuperano il remoto del ramo su cui si trova il checkout (qualunque sia)
e non fanno nulla se non ci sono novità (`--force` ricostruisce e riavvia comunque, ad esempio dopo aver modificato
`docker-compose.yml`). Altrimenti `git pull`, costruiscono i [plugin inclusi](#plugins) e l'immagine **mentre
Marginalia continua a girare**, poi fermano il container, copiano i JAR dei plugin in `data/extensions` (sostituendo quelli installati) e avviano la nuova versione. Se un passaggio fallisce, lo script si ferma con un messaggio; se il problema è nel pull
o in una build, la versione precedente è ancora in esecuzione e la prossima esecuzione ricostruisce da capo.

`--stop-first` ferma il container prima della build, per un server che non ha memoria sufficiente per applicazione e build insieme; Marginalia è allora non disponibile per tutta la durata della build.

Su Linux gli script non devono nemmeno essere eseguiti a mano: con il watcher degli aggiornamenti installato, un amministratore aggiorna il server con un pulsante, vedi [Aggiornamenti](../administration/updates.md).

<a id="plugins"></a>

## Plugin

L'immagine Docker non contiene le [estensioni](../extensions/index.md), che devono essere costruite dallo stesso
codice sorgente dell'applicazione. Per questo non serve Java né Maven: `marginalia/plugins/docker-compose.yml` costruisce
tutte in un container, che prima compila l'applicazione e poi ogni plugin, e copia i JAR in
`marginalia/plugins/build`:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

La prima build richiede alcuni minuti (scarica le dipendenze dell'applicazione). Poi copia i JAR in
`data/extensions` e riavvia Marginalia, oppure caricali da *Admin → Estensioni* (vedi
[Estensioni](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` fanno la copia per te, quindi su un server
che li usa i plugin vengono sempre aggiornati insieme all'applicazione.
<a id="health"></a>

## Stato di salute

L'immagine ha un controllo di integrità Docker: `docker ps` mostra `(health: starting)` mentre Marginalia si avvia (fino a due minuti), poi `(healthy)`. Diventa `unhealthy` quando `http://localhost:8080/health` smette di rispondere con `200`. Docker non riavvia da solo un container non integro; usa lo stato per il monitoraggio o per uno strumento di auto-heal.

`/health` non richiede accesso, quindi anche uno strumento di monitoraggio o un load balancer può usarlo (tramite il proxy: `https://marginalia.example.com/health`). Risponde `200` e `{"status":"UP"}` quando Marginalia è in esecuzione e il suo database risponde, e `503` con `{"status":"DOWN",...}` mentre si sta avviando o quando qualcosa non va. Non mostra altro (nessuna versione, nessun dettaglio); il motivo è nel log.

<a id="logs"></a>

## Log

```sh
docker compose logs -f marginalia
```
