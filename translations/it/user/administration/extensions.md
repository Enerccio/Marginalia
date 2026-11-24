---
label: Gestione delle estensioni
order: 70
---

# Gestione delle estensioni

La scheda **Extensions** della pagina di amministrazione installa e rimuove [estensioni](../extensions/index.md) per l'intera installazione.

![La scheda Extensions](../../images/admin-extensions.png)

<a id="installing"></a>

## Installazione

Clicca su **Load Extension (.jar)** (o trascina il file su di esso) e scegli il file JAR dell'estensione. Viene copiato nella cartella `extensions` della [cartella dei dati](index.md#the-data-folder) e avviato subito; *Extension loaded
successfully.* ne conferma l'avvio. L'estensione compare nell'elenco:

| Colonna | |
|---|---|
| **ID** | Il numero interno dell'estensione in questa esecuzione. |
| **Extension Name** | Il suo nome. |
| **Version** | La sua versione. Il pulsante **#** accanto ad essa mostra l'hash SHA-256 del JAR dell'estensione quando passi sopra con il mouse; fai clic per copiare l'hash, ad esempio per confrontarlo con quello pubblicato dall'autore. |
| **State** | *Active* quando è in esecuzione; *Stopped* quando l'hai fermata (vedi [Arresto e avvio](#stopping-and-starting)); *Installed* o *Resolved* quando non è riuscita ad avviarsi (vedi [Quando un'estensione fallisce](#when-an-extension-fails)). |
| **Verification** | *Valid* o *Invalid*; fai clic per leggere il rapporto del controllo che Marginalia ha effettuato prima di avviare l'estensione. |
| **Actions** | **Stop** / **Start**, **Log** e **Unload**. |

Gli utenti vedono l'estensione nelle finestre che aprono da quel momento in poi: un libro già aperto va chiuso e riaperto, oppure va ricaricata la pagina.

I file JAR possono anche essere copiati direttamente nella cartella `extensions`, ad esempio su un server senza accesso all'interfaccia; vengono caricati al successivo avvio. Così si può anche preparare la configurazione Docker: metti i JAR in `./data/extensions`. I JAR possono essere compilati anche in Docker, vedi [Plugin](../getting-started/docker-server.md#plugins) nella guida del server.

!!!warning Usa estensioni compilate per questa versione
Le estensioni modificano parti dell'interfaccia utente di una versione di Marginalia. Usa le estensioni compilate dallo stesso codice sorgente dell'applicazione (vedi il [README dei plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)).
Un'estensione incompatibile può non caricarsi o compromettere parti dell'applicazione. L'API delle estensioni non è stabile nella 1.0, quindi aspettati di dover aggiornare le estensioni insieme a Marginalia.
!!!

<a id="stopping-and-starting"></a>

## Arresto e avvio

**Stop** scarica immediatamente l'estensione senza rimuoverla: scompare dalle schermate a cui aveva aggiunto elementi, ma il suo JAR e i suoi dati restano, e compare nell'elenco con lo stato *Stopped*. Il pulsante diventa quindi **Start**, che la ricarica (viene prima controllata, come all'avvio dell'applicazione; se il controllo fallisce, resta ferma e viene mostrato il rapporto). Un'estensione ferma resta ferma anche dopo il riavvio di Marginalia. Caricare una nuova versione di un'estensione ferma la avvia.

L'arresto viene memorizzato tramite un file vuoto con lo stesso nome del JAR con `.STOPPED` aggiunto (`sidequery.jar.STOPPED`) nella cartella `extensions`. Su un server senza accesso all'interfaccia, crealo per impedire che un'estensione si avvii, oppure eliminalo per permetterne l'avvio al prossimo riavvio.

<a id="the-extension-log"></a>

## Il log dell'estensione

**Log** apre il log dell'estensione: ciò che l'estensione stessa ha scritto con il logger che Marginalia fornisce alle estensioni ([Logging](../../developer/plugins/overview.md#logging)). Mostra l'ultima parte del file, **Refresh** lo rilegge e **Clear** lo elimina. Il file è `extensions/<name>.jar.log` (per esempio `sidequery.jar.log`); quando supera i 2 MB viene spostato in `.log.old` e ne viene avviato uno nuovo. Viene eliminato quando l'estensione viene scaricata. Le estensioni che non scrivono nel log hanno un log vuoto. Ciò che l'estensione scrive finisce anche nel log dell'applicazione (chiamato `extension.<name>`, a livello di riga), quindi è presente anche nel log del server; i problemi di Marginalia stesso sono solo lì.

![Il log di un'estensione](../../images/extension-log.png)

<a id="removing"></a>

## Rimozione

**Unload** chiede conferma, poi ferma immediatamente l'estensione ed elimina il suo JAR (e il suo log) dalla cartella `extensions`.
I suoi dati restano (vedi [Dove sono conservati i dati delle estensioni](../extensions/index.md#where-extension-data-is-kept)); ricaricarla riporta indietro i dati.

<a id="updating"></a>

## Aggiornamento

Per installare una versione più recente di un'estensione, usa **Load Extension (.jar)** con il nuovo file - il nome del file non conta. La versione installata viene scaricata, il suo JAR eliminato e la nuova versione avviata al suo posto. Se la nuova versione non si carica, il caricamento mostra l'errore e la vecchia versione continua a funzionare.

Le estensioni possono anche essere installate, aggiornate, fermate e rimosse da uno script, con una [chiave API](../account.md#api-keys) che ha l'accesso *Manage the extensions*. Uno script può prima confrontare il commit che il server esegue con il sorgente da cui sono stati compilati i plugin, per capire se possono essere installati senza aggiornare Marginalia stesso (vedi il [manuale per sviluppatori](../../developer/api.md#plugins-without-a-full-update)).

<a id="when-an-extension-fails"></a>

## Quando un'estensione fallisce

Se un'estensione caricata non può essere avviata, il caricamento mostra *Failed to load extension: ...* e il suo JAR non viene conservato.

Marginalia verifica un'estensione rispetto alla versione in cui sta girando prima di avviarla. Un'estensione che non è compatibile - tipicamente una compilata per un'altra versione di Marginalia - non viene caricata. Un caricamento mostra il rapporto del controllo e non viene conservato; un'estensione nella cartella `extensions` resta nell'elenco, non avviata, con **Invalid** nella colonna *Verification*, e il rapporto è nel log. Il controllo viene ripetuto dopo l'aggiornamento di Marginalia, quindi un'estensione che prima funzionava può essere respinta in seguito: procurati la versione dell'estensione compilata per la nuova versione di Marginalia e caricala. Finché un'estensione non è valida, una **barra rossa in cima all'area di lavoro** avvisa tutti: un amministratore vede al passaggio del mouse quali estensioni sono interessate e apre questa pagina con un clic sulla barra; gli altri utenti sono invitati a informare l'amministratore (un'estensione che hai fermato tu stesso non conta). Accanto al JAR di ogni estensione troverai un piccolo file `.valid` o `.invalid` con il rapporto; viene rimosso insieme all'estensione e può essere lasciato stare.

![La barra rossa sull'area di lavoro quando un'estensione non è valida](../../images/invalid-extensions-warning.png)

![Il rapporto di verifica di un'estensione non compatibile e un'estensione ferma (pulsante Start)](../../images/extension-verification.png)

Un'estensione nella cartella `extensions` che non riesce ad avviarsi (ad esempio una compilata per un'altra versione) non impedisce il caricamento delle altre. Compare nell'elenco con lo stato *Installed* o *Resolved* invece di *Active* e può essere rimossa con **Unload**. Un file che non è affatto un'estensione viene saltato e non elencato - eliminalo dalla cartella `extensions`. Il log (`docker compose logs`, o `desktop/logs/marginalia.log` per l'app desktop) contiene i dettagli dell'errore.
