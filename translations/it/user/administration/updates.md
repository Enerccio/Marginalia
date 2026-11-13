---
label: Aggiornamenti
order: 65
---

# Aggiornamenti

Su un [server Docker](../getting-started/docker-server.md) costruito da un checkout Git, un amministratore può
aggiornare Marginalia dalla scheda **Aggiorna** della pagina Admin, senza accedere al server: il server scarica la
versione più recente del ramo su cui si trova il checkout, costruisce l'applicazione e i
[plugin inclusi](../getting-started/docker-server.md#plugins) e riavvia.

Serve un piccolo programma sul server, l'**osservatore degli aggiornamenti**, che si [configura una volta sola](#setting-up-the-watcher).
Senza di esso la scheda mostra solo la versione in esecuzione e indica che l'osservatore non è attivo. È anche ciò che
mostrano l'[app desktop](../getting-started/desktop-app.md) e un server con l'[immagine precompilata](../getting-started/docker-server.md#prebuilt-image):
si aggiornano installando la nuova versione.

<a id="the-update-tab"></a>

## La scheda Aggiorna

| | |
|---|---|
| **Versione in esecuzione** | La versione e il commit di Marginalia in esecuzione. |
| **Ramo** | Il ramo su cui si trova il checkout sul server e il ramo remoto che segue (ad esempio `dev`, che segue `origin/dev`), il commit più recente di quel ramo remoto e il momento in cui il server ha controllato l'ultima volta. Il server controlla ogni 15 minuti. |
| Stato | *È disponibile una nuova versione* quando il commit più recente non è quello in esecuzione, *Marginalia è aggiornato* altrimenti. Mentre un aggiornamento è in corso, la scheda lo indica. |
| **Ultimo aggiornamento** | Quando si è concluso l'ultimo aggiornamento avviato da qui e se è andato a buon fine o è fallito. |

Il server segue il ramo che è estratto lì, qualunque esso sia: per spostare un'installazione su un altro ramo, usa
`git switch` nel checkout sul server; la scheda mostrerà il nuovo ramo dopo il controllo successivo.

| Pulsante | |
|---|---|
| **Aggiorna e riavvia** | Chiede al server di aggiornare, previa conferma. Costruisce e riavvia anche quando non c'è nulla di nuovo (utile, ad esempio, dopo una modifica a `docker-compose.yml`). |
| **Aggiorna** | Rilegge lo stato. La scheda non si aggiorna da sola. |
| **Registro** | L'output dell'ultimo aggiornamento: ciò che hanno scritto Git e la build. |

<a id="what-an-update-does"></a>

## Cosa fa un aggiornamento

1. La nuova versione viene costruita: `git pull`, i plugin, l'immagine. Questo richiede diversi minuti e **Marginalia
   continua a funzionare** per tutta la durata. La scheda mostra *L'aggiornamento è in corso*; **Registro** mostra a
   che punto si è.
2. Marginalia viene fermato, i nuovi plugin vengono copiati in `data/extensions` (sostituendo quelli installati) e
   parte la nuova versione. Questo richiede uno o due minuti (vedi [Stato di salute](../getting-started/docker-server.md#health)).
   Chiunque stia lavorando viene interrotto e tutto ciò che è in fase di generazione viene fermato: la conferma indica
   quanti [job](jobs.md) sono in esecuzione in quel momento, quindi scegli un momento tranquillo.
3. Il database viene aggiornato all'avvio della nuova versione, dopo che è stata salvata una
   [copia](database-backups.md#copy-before-an-upgrade). Fai anche tu prima un [backup del database](database-backups.md).

Se la build fallisce, non si ferma nulla: continua a funzionare la versione precedente, **Ultimo aggiornamento** indica
*fallito* e il registro spiega il motivo. L'aggiornamento successivo ricostruisce di nuovo, anche quando non c'è nulla da scaricare.

<a id="setting-up-the-watcher"></a>

## Configurare l'osservatore

Marginalia gira dentro un container e non può accedere né al checkout Git né a Docker; dargli questo accesso
significherebbe rendere amministratore del server chiunque riesca ad entrare nell'applicazione. Quindi si limita a chiedere: scrive un file nella
sua [cartella dei dati](index.md#the-data-folder), e l'osservatore, che gira sul server fuori dal container, fa il
lavoro. L'osservatore è lo script `tools/update-watcher/watch.sh` del checkout; tutto ciò che esegue è `rebuild.sh`
dello stesso checkout (vedi [Aggiornamenti automatici](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux con systemd

Nel checkout sul server (la cartella con `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Installa e avvia tre unità:

| Unità | |
|---|---|
| `marginalia-update.service` | Esegue l'osservatore una volta. |
| `marginalia-update.path` | Lo avvia nell'istante in cui l'applicazione richiede un aggiornamento. |
| `marginalia-update.timer` | Lo avvia due minuti dopo l'avvio del sistema e poi ogni 15 minuti, per cercare nuovi commit. |

Apri **Admin → Update** e clicca su **Refresh**: la scheda mostra il ramo e il commit più recente. Tutto qui.

| Opzione | |
|---|---|
| `--name NAME` | Il nome delle unità (predefinito `marginalia-update`). Due checkout sullo stesso computer (un'installazione di test e una di produzione) richiedono ciascuno un nome proprio. |
| `--user USER` | Esegue l'osservatore come questo utente invece che come root, vedi [sotto](#who-the-watcher-runs-as). |
| `--uninstall` | Ferma e rimuove le unità (con lo stesso `--name`). |

`systemctl status marginalia-update.service` mostra l'ultima esecuzione e `journalctl -u marginalia-update.service` il suo
output (lo stesso testo di **Registro**, e il motivo quando l'osservatore stesso non ha potuto funzionare). Le unità contengono il percorso
del checkout: dopo aver spostato il checkout, esegui di nuovo `install.sh`.

<a id="without-systemd-cron"></a>

### Senza systemd (cron)

Esegui l'osservatore ogni minuto; non fa nulla quando nessuno ha chiesto un aggiornamento e ha controllato il remoto meno di
15 minuti fa, quindi non costa nulla:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Inserisci la riga nel crontab di root (`sudo crontab -e`) oppure di un altro utente, vedi sotto. Un aggiornamento
parte così entro un minuto dal clic.

<a id="settings"></a>

### Impostazioni

L'osservatore legge queste variabili d'ambiente (con systemd: `sudo systemctl edit marginalia-update.service` e righe
`Environment=NAME=value` sotto `[Service]`):

| Variabile | |
|---|---|
| `MARGINALIA_DATA_DIR` | La cartella dei dati sul server, quando non è `data` nel checkout (il lato sinistro del volume in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Gli argomenti di `rebuild.sh`, `--force` per impostazione predefinita. Su un server senza memoria sufficiente per applicazione e build insieme, usa `--force --stop-first`: Marginalia viene allora fermato prima della build ed è inattivo per tutta la sua durata. |
| `MARGINALIA_CHECK_MINUTES` | Quanto può essere vecchio lo stato prima che un'esecuzione senza richiesta controlli di nuovo il remoto, 15 per impostazione predefinita. Con systemd è il timer a decidere ogni quanto l'osservatore viene eseguito. |

<a id="who-the-watcher-runs-as"></a>

### Con quale utente viene eseguito l'osservatore

Per impostazione predefinita l'osservatore gira come **root**: deve usare Docker e scrivere nella cartella dei dati, che appartiene
all'utente del container. Git stesso viene eseguito come proprietario del checkout, così i file di Git restano suoi.

L'osservatore esegue ciò che contiene il ramo (`rebuild.sh`, il `Dockerfile`), come fa ogni aggiornamento manuale: usa
solo un repository di cui ti fidi, e mantieni il checkout scrivibile solo dal suo proprietario, perché chiunque possa modificare `rebuild.sh` o
`watch.sh` lì esegue comandi come utente dell'osservatore.

Con `--user USER` (o il crontab di quell'utente) gira come un altro utente. Quell'utente deve poter usare Docker
(che sulla maggior parte dei sistemi equivale quasi a essere root), deve essere proprietario del checkout e deve poter scrivere in `data/update` e
`data/extensions`, che normalmente appartengono all'utente del container.

<a id="windows-and-macos"></a>

### Windows e macOS

Non esiste un osservatore pronto per Docker Desktop. Aggiorna a mano con `rebuild.bat` / `rebuild.sh`, oppure da un'attività pianificata, vedi [Aggiornamenti automatici](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Risoluzione dei problemi

| | |
|---|---|
| *L'osservatore degli aggiornamenti non gira sul server* | L'osservatore non ha scritto alcuno stato per un'ora, o non lo ha mai fatto. Controlla `systemctl status marginalia-update.timer marginalia-update.path` e `journalctl -u marginalia-update.service`. Se la cartella dei dati non è `data` nel checkout, imposta `MARGINALIA_DATA_DIR`. |
| *Il ramo ... non segue alcun ramo remoto* | Il checkout si trova su un ramo senza upstream (oppure su un commit staccato), quindi non c'è nulla da scaricare. Sul server: `git switch <ramo>` oppure `git branch --set-upstream-to=origin/<ramo>`. |
| *Il server non è riuscito a raggiungere il repository Git* | `git fetch` è fallito al controllo precedente (nessuna rete, oppure un repository privato senza credenziali per il proprietario del checkout). Il commit più recente mostrato potrebbe essere vecchio. |
| La richiesta di aggiornamento è stata fatta e non succede nulla | Con cron parte entro un minuto. Una richiesta che nessuno ha preso in carico per un'ora viene scartata, così da non partire di sorpresa quando l'osservatore viene riparato. |
| *Ultimo aggiornamento: ..., fallito* | Leggi **Registro**. Un `git pull` fallito di solito significa che il checkout contiene modifiche proprie (file modificati, commit locali): risolvile sul server. Se la build fallisce, resta in esecuzione la versione precedente. |

I file dell'osservatore si trovano in `update` nella cartella dei dati: `status.properties` (ciò che mostra la scheda), `update.log`
(l'ultimo aggiornamento) e, per un momento, `request`.
