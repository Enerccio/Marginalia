---
label: Cestino
order: 955
icon: trash
---

# Cestino

Eliminare in Marginalia non distrugge subito nulla: un libro, un lorebook, una parte della storia o un fornitore di inferenza eliminato
viene solo contrassegnato come eliminato e nascosto dagli elenchi. La scheda **Cestino** elenca questi oggetti eliminati e permette di
ripristinarli.

Gli oggetti eliminati restano nel cestino finché un amministratore non esegue la [pulizia](administration/cleanup.md), che li rimuove
dal database in modo definitivo. Ciò che la pulizia ha rimosso non può più essere ripristinato (solo un
[backup del database](administration/database-backups.md) fatto prima lo conserva ancora).

![La scheda Cestino](../images/trash-tab.png)

La tabella si carica man mano che scorri. Gli oggetti eliminati più di recente compaiono per primi.

| Colonna | |
|---|---|
| (casella di spunta) | Seleziona l'oggetto da ripristinare. La casella nell'intestazione seleziona tutti gli oggetti. |
| **Tipo** | Il tipo di oggetto: libro, lorebook, voce di lorebook, messaggio (una parte della storia), versione di una parte (una voce eliminata della [cronologia delle versioni](books/version-history.md#deleting-versions); ripristinarla la toglie dal cestino della cronologia), riassunto, modello (fornitore di inferenza), protocollo, tag, agente, esecuzione di agente, risorsa... |
| **Nome** | Il nome dell'oggetto. Le parti della storia e i collegamenti dei tag non hanno un nome e mostrano il proprio id; usa *Contenuto esteso* per leggere una parte della storia. |
| **Utente** | A chi appartiene l'oggetto. Questa colonna è visibile solo agli amministratori. |
| **Eliminato** | Quando l'oggetto è stato eliminato. |
| **Contenuto esteso** | Apre i dati salvati dell'oggetto, vedi [sotto](#extended-content). |

Sopra la tabella, **Tipo** mostra un solo tipo di oggetto, **Aggiorna** ricarica la tabella.

<a id="restoring"></a>

## Ripristino

Seleziona gli oggetti che vuoi recuperare e fai clic su **Ripristina i selezionati**. Marginalia chiede conferma prima di ripristinarli. Gli
oggetti tornano nei rispettivi elenchi esattamente come erano al momento dell'eliminazione, e le altre schede vengono aggiornate.

<a id="objects-that-belong-to-other-objects"></a>

### Oggetti che appartengono ad altri oggetti

Eliminare un oggetto elimina anche ciò che gli appartiene, e ripristinarlo riporta indietro tutto questo: un libro riporta con sé le sue parti
della storia, i relativi riassunti e i suoi tag; un lorebook le sue voci; una parte della storia il suo riassunto e le parti sottostanti; un
fornitore di inferenza i suoi dati di utilizzo. Non serve selezionarli: ripristina il libro e la sua storia torna con lui.
Compaiono nella tabella come righe separate (un libro con una storia lunga riempie la tabella con le sue parti); ripristinarne uno da solo
funziona solo se l'oggetto a cui appartiene non è eliminato. Qualcosa che avevi eliminato in precedenza da solo (una voce, una parte della
storia) torna indietro anche con il suo lorebook o libro, perché Marginalia non può distinguerlo da ciò che è stato eliminato insieme a loro.

Alcuni oggetti non possono esistere senza un altro oggetto:

- una voce ha bisogno del suo lorebook,
- una parte della storia e un riassunto hanno bisogno del loro libro,
- un libro ha bisogno del suo modello (fornitore di inferenza), del suo protocollo e del suo lorebook, finché li usa,
- un collegamento di tag ha bisogno del suo tag.

Un oggetto il cui lorebook, libro... è eliminato **può essere ripristinato solo insieme a esso**: seleziona entrambi. Se selezioni solo
la voce, Marginalia non ripristina **niente** e ti dice cosa manca:

> Non è stato ripristinato niente. Questi oggetti hanno bisogno di un oggetto eliminato che non è selezionato. Selezionalo anche tu, o lascia questi oggetti eliminati.
> La voce di lorebook "mosh" richiede il lorebook eliminato "Phrasebook" (lorebook)

![Ripristino rifiutato perché anche il lorebook della voce è eliminato](../images/trash-restore-blocked.png)

Seleziona l'oggetto indicato e ripristina di nuovo. Gli oggetti che non sono più eliminati (ripristinati nel frattempo, o oggetti che non
hai eliminato) non sono un problema.

!!!info Una parte della storia ripristinata
Una parte della storia eliminata dal mezzo di una storia ha visto le sue parti successive spostarsi sul suo genitore. Ripristinarla
riporta la parte sotto il suo genitore come nuovo ramo, **senza** le parti che la seguivano: esse restano dove sono state spostate.
Ripristina un intero libro invece di singole parti se vuoi la storia com'era.
!!!

<a id="deleting-for-good"></a>

## Eliminazione definitiva

Non sempre si vuole aspettare la pulizia: una parte della storia o una voce di lorebook che sai di non servire più può essere rimossa subito.
Selezionala e fai clic su **Elimina definitivamente**; Marginalia chiede prima conferma, e **questa operazione non si può annullare**.

![Eliminazione definitiva di una parte della storia](../images/trash-delete-permanently.png)

- Solo le **parti della storia** e le **voci di lorebook** possono essere eliminate definitivamente (insieme ai loro riassunti e tag). Il pulsante
  è disattivato finché è selezionato qualcos'altro: libri, lorebook e il resto attendono la [pulizia](administration/cleanup.md)
  dell'amministratore, così non puoi rimuovere un intero libro per sbaglio.
- Le parti della storia che si trovano sotto una parte rimossa non vengono rimosse con essa, che siano eliminate o no: si spostano sul suo
  genitore, come quando elimini una parte nell'editor. Se la parte era la posizione in cui era aperto un libro, il libro si apre invece sul genitore.
- Marginalia non elimina nulla se qualcosa non può essere rimosso (per esempio un'estensione ha ancora bisogno dell'oggetto) e ti indica
  quale.

<a id="extended-content"></a>

### Contenuto esteso

Marginalia conserva gran parte dei dati di un oggetto (il testo di una parte della storia, le impostazioni di una voce di lorebook, le note che
le [estensioni](extensions/index.md) hanno salvato) in formato JSON accanto all'oggetto stesso. **Contenuto esteso** in una riga lo mostra,
così puoi controllare cosa contiene un oggetto prima di ripristinarlo, per esempio per trovare la parte della storia giusta. Gli oggetti senza
questi dati (risorse) non hanno alcun collegamento; un oggetto che non ne ha mostra *Questo oggetto non ha contenuto esteso.*

<a id="administrators"></a>

## Amministratori

Gli amministratori vedono gli oggetti eliminati di **tutti gli utenti** e possono ripristinarne qualsiasi. Il filtro **Utente** sopra la
tabella mostra gli oggetti di un solo utente; la colonna **Utente** indica di chi sono. Gli oggetti ripristinati tornano al loro proprietario.

La stessa tabella si trova in *Admin → Pulizia* (**Cestino**), così puoi vedere cosa rimuoverà la prossima pulizia. Gli oggetti di un utente
eliminato non possono essere ripristinati: dipendono dall'utente.
