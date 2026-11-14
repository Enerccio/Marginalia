---
label: Gestione delle estensioni
order: 70
---

# Gestione delle estensioni

La scheda **Estensioni** della pagina di amministrazione installa e rimuove [estensioni](../extensions/index.md) per l'intera installazione.

![La scheda Estensioni](../../images/admin-extensions.png)

<a id="installing"></a>

## Installazione

Fai clic su **Carica estensione (.jar)** (oppure trascina il file su di esso) e scegli il file JAR dell'estensione. Il file viene copiato nella cartella `extensions` della [cartella dei dati](index.md#the-data-folder) e avviato subito; il messaggio *Extension loaded successfully.* ne conferma il caricamento. L'estensione compare nell'elenco:

| Colonna | |
|---|---|
| **ID** | Il numero interno dell'estensione in questa esecuzione. |
| **Nome dell'estensione** | Il suo nome. |
| **Versione** | La sua versione. Il pulsante **#** accanto ad essa mostra, al passaggio del mouse, l'hash SHA-256 del JAR dell'estensione; fai clic per copiare l'hash, ad esempio per confrontarlo con quello pubblicato dall'autore. |
| **Stato** | *Attiva* quando è in esecuzione; *Arrestata* quando l'hai interrotta (vedi [Interruzione e avvio](#stopping-and-starting)); *Installata* o *Risolta* quando non è riuscita ad avviarsi (vedi [Quando un'estensione non funziona](#when-an-extension-fails)). |
| **Verifica** | *Valido* o *Non valido*; fai clic per leggere il rapporto del controllo che Marginalia ha effettuato prima di avviare l'estensione. |
| **Azioni** | **Interrompi** / **Avvia**, **Registro** e **Scarica**. |

Gli utenti vedono l'estensione nelle finestre che aprono da quel momento in poi: un libro già aperto va chiuso e riaperto, oppure la pagina ricaricata.

I file JAR possono anche essere copiati direttamente nella cartella `extensions`, ad esempio su un server senza accesso all'interfaccia; vengono caricati al successivo avvio. È anche così che si può preparare l'installazione con Docker: metti i JAR in `./data/extensions`. I JAR si possono compilare anche in Docker, vedi [Plugin](../getting-started/docker-server.md#plugins) nella guida del server.

!!!warning Usa estensioni compilate per questa versione
Le estensioni modificano parti dell'interfaccia utente di una specifica versione di Marginalia. Usa estensioni compilate dallo stesso codice sorgente dell'applicazione (vedi il [README dei plugin](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)).
Un'estensione incompatibile può non caricarsi oppure compromettere parti dell'applicazione. L'API delle estensioni non è congelata nella 1.0, quindi aspettati di dover aggiornare le estensioni insieme a Marginalia.
!!!

<a id="stopping-and-starting"></a>

## Interruzione e avvio

**Interrompi** scarica l'estensione immediatamente senza rimuoverla: scompare dalle schermate a cui aveva aggiunto elementi, ma il suo JAR e i suoi dati restano, e compare nell'elenco con lo stato *Arrestata*. Il pulsante diventa quindi **Avvia**, che la carica di nuovo (prima viene controllata, come all'avvio dell'applicazione; se il controllo fallisce, resta arrestata e viene mostrato il rapporto). Un'estensione arrestata resta tale anche dopo il riavvio di Marginalia. Caricare una nuova versione di un'estensione arrestata la avvia.

L'interruzione viene memorizzata tramite un file vuoto con lo stesso nome del JAR a cui viene aggiunto `.STOPPED` (`sidequery.jar.STOPPED`), nella cartella `extensions`. Su un server senza accesso all'interfaccia, crea questo file per impedire che un'estensione si avvii, oppure eliminalo per permetterne l'avvio al successivo riavvio.

<a id="the-extension-log"></a>

## Il registro dell'estensione

**Registro** apre il registro dell'estensione: ciò che l'estensione stessa ha scritto con il logger che Marginalia mette a disposizione delle estensioni ([Logging](../../developer/plugins/overview.md#logging)). Mostra la parte finale del file; **Aggiorna** lo rilegge e **Svuota** lo cancella. Il file è `extensions/<name>.jar.log` (ad esempio `sidequery.jar.log`); quando supera i 2 MB viene spostato in `.log.old` e ne viene avviato uno nuovo. Viene eliminato quando l'estensione viene scaricata. Le estensioni che non scrivono nulla nel registro hanno un registro vuoto. Ciò che l'estensione registra va anche nel registro dell'applicazione (chiamato `extension.<name>`, al livello della riga), quindi è presente anche nel registro del server; i problemi di Marginalia stesso si trovano solo lì.

![Il registro di un'estensione](../../images/extension-log.png)

<a id="removing"></a>

## Rimozione

**Scarica** chiede una conferma, poi interrompe immediatamente l'estensione ed elimina il suo JAR (e il suo registro) dalla cartella `extensions`.
I suoi dati restano (vedi [Dove sono conservati i dati delle estensioni](../extensions/index.md#where-extension-data-is-kept)); caricarla di nuovo ripristina i dati.

<a id="updating"></a>

## Aggiornamento

Per installare una versione più recente di un'estensione, usa **Carica estensione (.jar)** con il nuovo file: il nome del file non ha importanza. La versione installata viene scaricata, il suo JAR eliminato e la nuova versione avviata al suo posto. Se la nuova versione non si carica, il caricamento mostra l'errore e la vecchia versione continua a funzionare.

<a id="when-an-extension-fails"></a>

## Quando un'estensione non funziona

Se un'estensione caricata non può essere caricata, il caricamento mostra *Failed to load extension: ...* e il suo JAR non viene conservato.

Marginalia verifica un'estensione rispetto alla versione in cui è in esecuzione prima di avviarla. Un'estensione che non è compatibile, tipicamente una compilata per un'altra versione di Marginalia, non viene caricata. Un caricamento mostra il rapporto del controllo e non viene conservato; un'estensione nella cartella `extensions` resta nell'elenco, non avviata, con **Non valido** nella colonna *Verifica*, e il rapporto si trova nel registro. Il controllo viene ripetuto dopo ogni aggiornamento di Marginalia, quindi un'estensione che funzionava prima può essere rifiutata in seguito: procurati la versione dell'estensione compilata per la nuova versione di Marginalia e caricala. Finché un'estensione non è valida, una **barra rossa in cima all'area di lavoro** avvisa tutti: un amministratore vede al passaggio del mouse quali estensioni sono interessate e, con un clic sulla barra, apre questa pagina; agli altri utenti viene chiesto di avvisare l'amministratore (un'estensione che hai interrotta tu stesso non viene conteggiata). Accanto al JAR di ogni estensione troverai un piccolo file `.valid` o `.invalid` con il rapporto; viene rimosso insieme all'estensione e può essere lasciato com'è.

![La barra rossa sopra l'area di lavoro quando un'estensione non è valida](../../images/invalid-extensions-warning.png)

![Il rapporto di verifica di un'estensione non compatibile, e un'estensione arrestata (pulsante Avvia)](../../images/extension-verification.png)

Un'estensione nella cartella `extensions` che non riesce ad avviarsi (ad esempio una compilata per un'altra versione) non impedisce il caricamento delle altre. Compare nell'elenco con lo stato *Installata* o *Risolta* invece di *Attiva* e può essere rimossa con **Scarica**. Un file che non è affatto un'estensione viene saltato e non compare nell'elenco: eliminalo dalla cartella `extensions`. Il registro (`docker compose logs`, o `desktop/logs/marginalia.log` per l'app desktop) contiene i dettagli dell'errore.
