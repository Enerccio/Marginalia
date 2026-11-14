---
label: Aggiornamenti
order: 65
---

# Aggiornamenti

Su un [server Docker](../getting-started/docker-server.md) costruito da un checkout Git, un amministratore può
aggiornare Marginalia dalla scheda **Update** della pagina Admin, senza effettuare l'accesso al server: il server scarica la
versione più recente del ramo su cui si trova il checkout, costruisce l'applicazione e i
[plugin inclusi](../getting-started/docker-server.md#plugins) e si riavvia.

Serve un piccolo programma sul server, l'**update watcher**, che si [configura una volta](#setting-up-the-watcher).
Senza di esso la scheda mostra soltanto la versione in esecuzione e indica che il watcher non è attivo. Lo stesso vale per
l'[app desktop](../getting-started/desktop-app.md) e per un server con l'[immagine precompilata](../getting-started/docker-server.md#prebuilt-image): si aggiornano installando la nuova
versione.

<a id="the-update-tab"></a>

## La scheda Update

| | |
|---|---|
| **Running version** | La versione e il commit di Marginalia in esecuzione. |
| **Branch** | Il ramo su cui si trova il checkout sul server e il ramo remoto che segue (per esempio `dev`, che segue `origin/dev`), il commit più recente di quel ramo remoto e il momento in cui il server ha controllato per l'ultima volta. Il server controlla ogni 15 minuti. |
| Status | *A new version is available* quando il commit più recente non è quello in esecuzione, *Marginalia is up to date* altrimenti. Mentre un aggiornamento è in corso, la scheda lo indica. |
| **Last update** | Quando è terminato l'ultimo aggiornamento avviato da qui e se è andato a buon fine o è fallito. |

Il server segue il ramo che è estratto (checked out) lì, qualunque esso sia: per spostare un'installazione su un altro ramo,
`git switch` il ramo nel checkout sul server; la scheda mostra il nuovo ramo al controllo successivo.

| Pulsante | |
|---|---|
| **Update and restart** | Chiede al server di aggiornare, previa conferma. Costruisce e riavvia anche quando non c'è nulla di nuovo (utile, per esempio, dopo aver modificato `docker-compose.yml`). |
| **Refresh** | Rilegge lo stato. La scheda non si aggiorna da sola. |
| **Log** | L'output dell'ultimo aggiornamento: quello che hanno scritto Git e la build. |

<a id="what-an-update-does"></a>

## Cosa fa un aggiornamento

1. La nuova versione viene costruita: `git pull`, i plugin, l'immagine. Questo richiede diversi minuti e **Marginalia continua
   a funzionare** per tutto il tempo. La scheda mostra *The update is running*; **Log** indica a che punto si è.
2. Marginalia viene fermato, i nuovi plugin vengono copiati in `data/extensions` (sostituendo quelli installati) e la
   nuova versione si avvia. Questa fase dura uno o due minuti (vedi [Stato](../getting-started/docker-server.md#health)).
   Tutti quelli che stanno lavorando vengono interrotti e qualsiasi generazione in corso viene fermata: la conferma indica
   quanti [job](jobs.md) sono in esecuzione in quel momento, quindi scegli un momento tranquillo.
3. Il database viene aggiornato all'avvio della nuova versione, dopo che è stata salvata una
   [copia](database-backups.md#copy-before-an-upgrade). Fai comunque per conto tuo prima un [backup del database](database-backups.md).

Se la build fallisce, non si ferma nulla: continua a girare la versione precedente, **Last update** indica *failed* e
il log spiega il motivo. Il successivo aggiornamento ricostruisce di nuovo, anche quando non c'è nulla da scaricare.

<a id="setting-up-the-watcher"></a>

## Configurare il watcher

Marginalia gira dentro un container e non può accedere né al checkout Git né a Docker; dargli quell'accesso significherebbe
rendere amministratore del server chiunque riesca ad entrare nell'applicazione. Quindi si limita a chiedere: scrive un file nella
sua [cartella dati](index.md#the-data-folder), e il watcher, che gira sul server fuori dal container, fa
il lavoro. Il watcher è lo script `tools/update-watcher/watch.sh` del checkout; esegue soltanto
`rebuild.sh` dello stesso checkout (vedi [Aggiornamenti automatici](../getting-started/docker-server.md#automatic-updates)).

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
| `marginalia-update.timer` | Lo avvia due minuti dopo l'avvio del sistema e poi ogni 15 minuti, per cercare nuovi commit. |

Apri **Admin → Update** e clicca **Refresh**: la scheda mostra il ramo e il commit più recente. Tutto qui.

| Opzione | |
|---|---|
| `--name NAME` | Il nome delle unit (predefinito `marginalia-update`). Due checkout sullo stesso computer (un'installazione di test e una di produzione) hanno ciascuno bisogno di un nome proprio. |
| `--user USER` | Esegue il watcher con questo utente invece che con root, vedi [sotto](#who-the-watcher-runs-as). |
| `--uninstall` | Ferma e rimuove le unit (con lo stesso `--name`). |

`systemctl status marginalia-update.service` mostra l'ultima esecuzione e `journalctl -u marginalia-update.service` il suo
output (lo stesso testo di **Log**, e il motivo quando il watcher stesso non è riuscito a lavorare). Le unit contengono il percorso
del checkout: dopo aver spostato il checkout, esegui di nuovo `install.sh`.

<a id="without-systemd-cron"></a>

### Senza systemd (cron)

Esegui il watcher ogni minuto; non fa nulla quando nessuno ha chiesto un aggiornamento e ha controllato il remoto meno di
15 minuti fa, quindi non costa nulla:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Inserisci la riga nella crontab di root (`sudo crontab -e`), oppure in quella di un altro utente, vedi sotto. Un aggiornamento
parte quindi entro un minuto dal clic.

<a id="settings"></a>

### Impostazioni

Il watcher legge queste variabili d'ambiente (con systemd: righe `sudo systemctl edit marginalia-update.service` e
`Environment=NAME=value` sotto `[Service]`):

| Variabile | |
|---|---|
| `MARGINALIA_DATA_DIR` | La cartella dati sul server, quando non è `data` nel checkout (il lato sinistro del volume in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Gli argomenti di `rebuild.sh`, `--force` per impostazione predefinita. Su un server senza memoria sufficiente per applicazione e build insieme, usa `--force --stop-first`: Marginalia viene allora fermato prima della build e resta giù per tutta la durata.|
| `MARGINALIA_CHECK_MINUTES` | Quanto può essere vecchio lo stato prima che un'esecuzione senza richiesta controlli di nuovo il remoto; predefinito 15. Con systemd è il timer a decidere ogni quanto gira il watcher. |

<a id="who-the-watcher-runs-as"></a>

### Con quale utente gira il watcher

Per impostazione predefinita il watcher gira come **root**: deve usare Docker e scrivere nella cartella dati, che appartiene
all'utente del container. Git invece viene eseguito come proprietario del checkout, così i file di Git restano suoi.

Il watcher esegue quello che contiene il ramo (`rebuild.sh`, il `Dockerfile`), come fa ogni aggiornamento manuale: segui solo
un repository di cui ti fidi, e mantieni il checkout scrivibile solo dal suo proprietario, perché chiunque possa modificare `rebuild.sh` o
`watch.sh` lì esegue comandi con l'utente del watcher.

Con `--user USER` (o la crontab di quell'utente) gira invece come un altro utente. Quell'utente deve poter usare Docker
(che sulla maggior parte dei sistemi equivale praticamente a root), deve essere proprietario del checkout e deve poter scrivere in `data/update` e
`data/extensions`, che normalmente appartengono all'utente del container.

<a id="windows-and-macos"></a>

### Windows e macOS

Non esiste un watcher pronto per Docker Desktop. Aggiorna a mano con `rebuild.bat` / `rebuild.sh` oppure da un'attività
pianificata, vedi [Aggiornamenti automatici](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Risoluzione dei problemi

| | |
|---|---|
| *Il watcher non gira sul server* | Il watcher non ha scritto alcuno stato per un'ora, o non l'ha mai fatto. Controlla `systemctl status marginalia-update.timer marginalia-update.path` e `journalctl -u marginalia-update.service`. Se la cartella dati non è `data` nel checkout, imposta `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | Il checkout si trova su un ramo senza upstream (o su un commit staccato), quindi non c'è nulla da scaricare. Sul server: `git switch <branch>` oppure `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` è fallito all'ultimo controllo (nessuna rete, un repository privato senza credenziali per il proprietario del checkout). Il commit più recente mostrato potrebbe essere vecchio. |
| La richiesta di aggiornamento è stata fatta ma non succede nulla | Con cron parte entro un minuto. Una richiesta che nessuno ha preso in carico per un'ora viene scartata, così non parte di sorpresa quando il watcher viene riparato. |
| *Last update: ..., failed* | Leggi **Log**. Un `git pull` fallito di solito significa che il checkout ha modifiche proprie (file modificati, commit locali): risolvile sul server. Una build fallita lascia in esecuzione la versione vecchia. |

I file del watcher si trovano in `update` della cartella dati: `status.properties` (ciò che mostra la scheda), `update.log`
(l'ultimo aggiornamento) e, per un breve periodo, `request`.
