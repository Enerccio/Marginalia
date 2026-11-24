---
label: Aggiornamenti
order: 65
---

# Aggiornamenti

Su un [server Docker](../getting-started/docker-server.md) costruito da un checkout Git, un amministratore può aggiornare Marginalia dalla scheda **Update** della pagina Admin, senza effettuare l'accesso al server: il server scarica la versione più recente del branch su cui si trova il suo checkout, costruisce l'applicazione e i [plugin inclusi](../getting-started/docker-server.md#plugins) e si riavvia.

Serve un piccolo programma sul server, l'**update watcher**, che si [configura una sola volta](#setting-up-the-watcher). Senza di esso la scheda mostra solo la versione in esecuzione e indica che il watcher non è in funzione. È ciò che mostrano anche l'[app desktop](../getting-started/desktop-app.md) e un server con l'[immagine precompilata](../getting-started/docker-server.md#prebuilt-image): questi si aggiornano installando la nuova versione.

<a id="the-update-tab"></a>

## La scheda Update

| | |
|---|---|
| **Running version** | La versione e il commit di Marginalia in esecuzione. |
| **Branch** | Il branch su cui si trova il checkout sul server e il branch remoto che segue (per esempio `dev`, che segue `origin/dev`), il commit più recente di quel branch remoto e quando il server l'ha controllato per l'ultima volta. Il server controlla ogni 15 minuti. |
| Status | *A new version is available* quando il commit più recente non è quello in esecuzione, *Marginalia is up to date* altrimenti. Mentre un aggiornamento è in corso, la scheda lo indica. |
| **Last update** | Quando è terminato l'ultimo aggiornamento avviato da qui e se è riuscito o fallito. |

Il server segue il branch che è in checkout lì, qualunque esso sia: per spostare un'installazione su un altro branch, esegui `git switch` nel checkout sul server; la scheda mostra il nuovo branch dopo il controllo successivo.

| Pulsante | |
|---|---|
| **Update and restart** | Chiede al server di aggiornarsi, dopo una conferma. Costruisce e riavvia anche quando non c'è nulla di nuovo (utile, per esempio, dopo aver modificato `docker-compose.yml`). |
| **Refresh** | Rilegge lo stato. La scheda non si aggiorna da sola. |
| **Log** | L'output dell'ultimo aggiornamento: ciò che hanno scritto Git e la build. |

Lo stesso si può fare da uno script, con una [chiave API](../account.md#api-keys) che ha l'accesso *Update the installation*: leggere lo stato, chiedere un aggiornamento e leggere il log (vedi il [manuale per sviluppatori](../../developer/api.md)).

<a id="what-an-update-does"></a>

## Cosa fa un aggiornamento

1. La nuova versione viene costruita: `git pull`, i plugin, l'immagine. Richiede diversi minuti e **Marginalia continua a funzionare** per tutto il tempo. La scheda mostra *The update is running*; **Log** mostra a che punto è.
2. Marginalia viene fermato, i nuovi plugin vengono copiati in `data/extensions` (sostituendo quelli installati) e la nuova versione si avvia. Richiede uno o due minuti (vedi [Stato di salute](../getting-started/docker-server.md#health)). Chiunque stia lavorando viene interrotto e ciò che è in generazione viene fermato: la conferma indica quanti [job](jobs.md) sono in esecuzione in quel momento, quindi scegli un momento tranquillo.
3. Il database viene aggiornato all'avvio della nuova versione, dopo che ne è stata salvata una [copia](database-backups.md#copy-before-an-upgrade). Fai anche un tuo [backup del database](database-backups.md) prima.

Quando la build fallisce, non si ferma nulla: la versione precedente continua a girare, **Last update** indica *failed* e il log spiega il motivo. Il successivo aggiornamento costruisce di nuovo, anche quando non c'è nulla da scaricare.

<a id="setting-up-the-watcher"></a>

## Configurare il watcher

Marginalia viene eseguito dentro un container e non può raggiungere né il checkout Git né Docker; concedergli quell'accesso farebbe diventare amministratore del server chiunque riesca a entrare nell'applicazione. Quindi si limita a chiedere: scrive un file nella sua [cartella dei dati](index.md#the-data-folder), e il watcher, che gira sul server fuori dal container, fa il lavoro. Il watcher è lo script `tools/update-watcher/watch.sh` del checkout; tutto ciò che esegue è `rebuild.sh` dello stesso checkout (vedi [Aggiornamenti automatici](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux con systemd

Nel checkout sul server (la cartella con `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Installa e avvia tre unit:

| Unit | |
|---|---|
| `marginalia-update.service` | Esegue il watcher una volta. |
| `marginalia-update.path` | Lo avvia nel momento in cui l'applicazione chiede un aggiornamento. |
| `marginalia-update.timer` | Lo avvia due minuti dopo il boot e poi ogni 15 minuti, per cercare nuovi commit. |

Apri **Admin → Update** e clicca **Refresh**: la scheda mostra il branch e il commit più recente. Tutto qui.

| Opzione | |
|---|---|
| `--name NAME` | Il nome delle unit (predefinito `marginalia-update`). Due checkout sullo stesso computer (un'installazione di test e una di produzione) hanno bisogno ciascuno di un proprio nome. |
| `--user USER` | Esegue il watcher come questo utente invece che come root, vedi [sotto](#who-the-watcher-runs-as). |
| `--uninstall` | Ferma e rimuove le unit (con lo stesso `--name`). |

`systemctl status marginalia-update.service` mostra l'ultima esecuzione e `journalctl -u marginalia-update.service` il suo output (lo stesso testo di **Log**, e il motivo quando il watcher stesso non è riuscito a lavorare). Le unit contengono il percorso del checkout: dopo aver spostato il checkout, esegui di nuovo `install.sh`.

<a id="without-systemd-cron"></a>

### Senza systemd (cron)

Esegui il watcher ogni minuto; non fa nulla quando nessuno ha chiesto un aggiornamento e ha controllato il remoto meno di 15 minuti fa, quindi non costa nulla:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Inserisci la riga nel crontab di root (`sudo crontab -e`), o di un altro utente, vedi sotto. Un aggiornamento parte così entro un minuto dal clic.

<a id="settings"></a>

### Impostazioni

Il watcher legge queste variabili d'ambiente (con systemd: righe `sudo systemctl edit marginalia-update.service` e `Environment=NAME=value` sotto `[Service]`):

| Variabile | |
|---|---|
| `MARGINALIA_DATA_DIR` | La cartella dei dati sul server, quando non è `data` nel checkout (il lato sinistro del volume in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Gli argomenti di `rebuild.sh`, `--force` per impostazione predefinita. Su un server senza memoria sufficiente per applicazione e build insieme, usa `--force --stop-first`: Marginalia viene allora fermato prima della build e resta giù per tutta la durata. |
| `MARGINALIA_CHECK_MINUTES` | Quanto può essere vecchio lo stato prima che un'esecuzione senza richiesta controlli di nuovo il remoto; 15 per impostazione predefinita. Con systemd decide il timer la frequenza con cui il watcher gira. |

<a id="who-the-watcher-runs-as"></a>

### Con quale utente viene eseguito il watcher

Per impostazione predefinita il watcher viene eseguito come **root**: deve usare Docker e scrivere nella cartella dei dati, che appartiene all'utente del container. Git stesso viene eseguito come proprietario del checkout, così i file di Git restano suoi.

Il watcher esegue ciò che contiene il branch (`rebuild.sh`, il `Dockerfile`), come fa ogni aggiornamento manuale: segui solo un repository di cui ti fidi, e lascia il checkout scrivibile solo al suo proprietario, perché chiunque possa modificare `rebuild.sh` o `watch.sh` lì esegue comandi come utente del watcher.

Con `--user USER` (o il crontab di quell'utente) viene eseguito come un altro utente. Quell'utente deve poter usare Docker (che sulla maggior parte dei sistemi equivale praticamente a root), deve essere proprietario del checkout e deve poter scrivere in `data/update` e `data/extensions`, che normalmente appartengono all'utente del container.

<a id="windows-and-macos"></a>

### Windows e macOS

Non esiste un watcher pronto per Docker Desktop. Aggiorna a mano con `rebuild.bat` / `rebuild.sh` oppure da un'attività pianificata, vedi [Aggiornamenti automatici](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Risoluzione dei problemi

| | |
|---|---|
| *The update watcher does not run on the server* | Il watcher non ha scritto alcuno stato da un'ora, o mai. Controlla `systemctl status marginalia-update.timer marginalia-update.path` e `journalctl -u marginalia-update.service`. Se la cartella dei dati non è `data` nel checkout, imposta `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | Il checkout si trova su un branch senza upstream (o su un commit staccato), quindi non c'è nulla da scaricare. Sul server: `git switch <branch>` oppure `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` non è riuscito all'ultimo controllo (nessuna rete, oppure un repository privato senza credenziali per il proprietario del checkout). Il commit più recente mostrato potrebbe essere vecchio. |
| The update was requested and nothing happens | Con cron parte entro un minuto. Una richiesta che nessuno ha preso in carico per un'ora viene scartata, così non parte di sorpresa quando il watcher viene riparato. |
| *Last update: ..., failed* | Leggi **Log**. Un `git pull` fallito di solito significa che il checkout ha modifiche proprie (file modificati, commit locali): risolvile sul server. Una build fallita lascia in esecuzione la versione vecchia. |

I file del watcher si trovano in `update` della cartella dei dati: `status.properties` (ciò che mostra la scheda), `update.log` (l'ultimo aggiornamento) e, per un momento, `request`.
