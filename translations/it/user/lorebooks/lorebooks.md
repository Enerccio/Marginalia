---
label: Creazione dei lorebook
order: 100
---

# Creazione dei lorebook

<a id="the-lorebook-editor"></a>

## L'editor dei lorebook

Apri la scheda **Lorebook**. L'editor mostra un lorebook alla volta; sceglilo nel campo **Lorebook** in alto.
I lorebook appartengono al tuo account: gli altri utenti non li vedono, a meno che tu non ne [condivida](#sharing-a-lorebook) uno.

La barra degli strumenti ha due righe, che vanno a capo su più linee in una finestra stretta: la prima per i lorebook (da **Lorebook** a **Tag lorebooks**), la seconda per le voci del lorebook selezionato (da **Add Entry** a **Refresh**).

| Pulsante | |
|---|---|
| **Lorebook** | Seleziona il lorebook da modificare. |
| **Only root lorebooks** | Elenca solo i lorebook che non sono sub lorebook di un altro (il lorebook selezionato resta nell'elenco). |
| **Add Lorebook** | Crea un nuovo lorebook chiamato *New Lorebook* e lo seleziona. |
| Clone (icona di due fogli) | Crea una copia del lorebook selezionato: chiede il nome (quello dell'originale con *(Copy)*) e seleziona la copia. La copia ha le stesse voci (con le loro impostazioni, tag e tag negativi), gli stessi tag, la stessa descrizione e i dati delle estensioni. I suoi sub lorebook non vengono copiati: la copia usa gli stessi. L'originale non viene modificato. |
| **Delete Lorebook** | Elimina il lorebook selezionato (dopo conferma). |
| Import (icona di file) | **Import Marginalia Lorebook**, **Import from SillyTavern**, **Import from archive** (un zip di file `.md`) oppure **Import from other user** (un lorebook che un altro utente [condivide](#sharing-a-lorebook)), vedi [Importazione ed esportazione](import-export.md). |
| Export (icona di file) | Scarica il lorebook con i suoi sub lorebook, vedi [Importazione ed esportazione](import-export.md#exporting). |
| Tags (icona di tag) | **Tag lorebooks**: applica un tag a molti lorebook in una sola volta, vedi [sotto](#tagging-many-at-once). Non è mostrato per un lorebook aperto in un libro. |
| Description (icona di righe, ultimo della prima riga) | **Edit description**: apre una finestra per scrivere di che cosa tratta il lorebook, vedi [sotto](#description). |
| **Add Entry** | Aggiunge una voce al lorebook, vedi [Voci e attivazione](entries.md). |
| **Full-screen editor** | Apre le voci del lorebook in una finestra a schermo intero con schede, vedi [Modifica a schermo intero](#editing-in-full-screen). Solo desktop. |
| **Search and replace** | Sostituisce un testo nel nome, nel testo e nel commento delle voci del lorebook, vedi [Cerca e sostituisci](entries.md#search-and-replace). |
| **Mass edit** | Apre tutte le voci del lorebook e dei suoi sub lorebook in un'unica tabella, vedi [Modifica di massa](#mass-edit). |
| Tags (icona di tag, a destra di **Search and replace**, seconda riga) | **Tag entries**: applica un tag a molte voci del lorebook selezionato in una sola volta, vedi [sotto](#tagging-many-at-once). |
| **Preview activation** | Mostra quali voci userebbe un turno con un testo che digiti e perché, vedi [Anteprima dell'attivazione](entries.md#preview-activation). |
| **Refresh** | Ricarica le voci. |
| Casella di ricerca | Mostra solo le voci che contengono il testo, vedi [Cercare ed eliminare molte voci](entries.md#searching-and-deleting-many-entries). |
| **Delete selected** | Compare quando nell'elenco sono selezionate delle voci; le elimina dopo conferma. |

Sotto la barra degli strumenti ci sono le impostazioni del lorebook selezionato:

| Campo | |
|---|---|
| **Name** | Il nome del lorebook, mostrato quando lo scegli per un libro o come sub lorebook. |
| **Sub Lorebooks** | Altri lorebook le cui voci sono incluse con questo, vedi [sotto](#sub-lorebooks). Fai clic sul nome di un sub lorebook selezionato per aprirlo. |
| **Tags** | Tag dell'intero lorebook; contano come tag di ciascuna delle sue voci, vedi [Tag](entries.md#tags). |
| **Scan depth** | Da `0` a `5`: quante volte il contenuto delle voci usate viene cercato per i filtri delle altre voci, vedi [Voci portate da altre voci](entries.md#entries-brought-in-by-other-entries). Vale per le voci del lorebook che un libro usa e per quelle dei suoi sub lorebook, a meno che un sub lorebook non abbia una propria profondità (riga successiva). |
| **Scan depth as a sub lorebook** | Vuoto, oppure da `0` a `5`: la profondità di scansione per le voci di questo lorebook mentre è un sub lorebook di quello usato da un libro. Vuoto significa la profondità di scansione di quel lorebook; `0` significa che le voci di questo lorebook non vengono mai trovate dal contenuto di altre. |
| **Enabled** | Se deselezionato, il lorebook non contribuisce con nulla: né le sue voci né quelle dei suoi sub lorebook. |
| **Share with other users** | Se selezionato, altri utenti possono consultare il lorebook e importarne una copia, vedi [sotto](#sharing-a-lorebook). Disattivato per un nuovo lorebook. |

Sotto le impostazioni c'è la [descrizione](#description) del lorebook, quando ne ha una.

Il resto dell'editor è l'elenco delle [voci](entries.md). Tutte le modifiche vengono salvate man mano che le fai.

<a id="description"></a>

### Descrizione

La descrizione è di poche parole (o righe) sul lorebook: quale mondo descrive, a cosa serve, quale libro lo usa. Fai clic sull'icona di righe nella prima riga della barra degli strumenti, scrivi nella finestra e conferma; un testo vuoto la rimuove. Un lorebook che ha una descrizione la mostra su una riga sotto le sue impostazioni, troncata con dei puntini dove non ci sta; posa il puntatore del mouse sulla riga per leggerla tutta. La descrizione viene copiata insieme al lorebook (clonazione, esportazione, importazione, backup) ed è il testo che gli altri utenti vedono accanto al nome quando lo condividi.

![La finestra della descrizione di un lorebook](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Condividere un lorebook

Seleziona **Share with other users** e ogni altro utente di questa installazione vede il lorebook in **Import from other user** (il pulsante di importazione nella prima riga della barra degli strumenti), con il suo nome, il suo autore (il tuo nome completo, o il tuo login se non ne hai uno) e la sua descrizione. Possono aprire una riga per leggere le voci (e le voci dei sub lorebook) e importarne una copia. Se deselezioni la casella, il lorebook scompare dal loro elenco; le copie che hanno già fatto restano loro.

* La condivisione è solo in lettura. Nessun altro può modificare, eliminare o usare il tuo lorebook in un libro; il lorebook resta tuo e lo vedi come prima.
* Un'importazione crea **nuovi lorebook dell'altro utente**: il lorebook e i suoi sub lorebook **condivisi**, a qualsiasi profondità, con le loro voci, tag e descrizione, collegati come lo sono i tuoi. Le copie sono private (non condivise); qualsiasi cosa tu faccia al tuo lorebook in seguito non le raggiunge, e ciò che loro modificano non raggiunge il tuo.
* **Vengono mostrati e copiati solo i sub lorebook che sono a loro volta condivisi.** Un sub lorebook non condiviso viene escluso insieme a tutto ciò che sta sotto di lui, anche un lorebook condiviso che gli sta sotto. Quando selezioni **Share with other users** su un lorebook che ha sub lorebook non condivisi, una domanda chiede se condividere anche quelli: **Yes** condivide il lorebook e tutti loro (a qualsiasi profondità), **No** condivide solo il lorebook, **Cancel** non condivide nulla e lascia la casella deselezionata. Un sub lorebook aggiunto in seguito non viene condiviso finché non lo condividi.
* Deselezionare la casella interrompe la condivisione solo di quel lorebook; i suoi sub lorebook restano condivisi (e compaiono da soli nell'elenco degli altri) finché non deseleziona anche loro.
* I lorebook eliminati, le copie di lavoro degli agenti e i tuoi lorebook non compaiono nell'elenco, e non compare nulla per un utente che è stato eliminato.

Sotto le impostazioni viene mostrata la stima dei token delle voci attive del lorebook. Il link **Count with subbooks** accanto ad essa somma
i token delle voci attive del lorebook e di tutti i suoi sub lorebook, a qualsiasi profondità, e mostra il totale accanto. Come quando si scrive
una storia, un sub lorebook disattivato viene escluso insieme ai lorebook che stanno sotto di lui, e un lorebook che è sub lorebook in più punti viene contato una sola volta.
Il totale è un conteggio del momento in cui fai clic: se modifichi una voce, scompare; fai di nuovo clic per contare di nuovo.

Un lorebook di un libro su cui lavora un agente è mostrato in sola lettura (vedi [Agenti](../agents.md)), senza **Add Entry**, **Search and replace**, **Mass edit**, **Delete Lorebook** e le impostazioni; **Add Lorebook**, importazione, esportazione e **Refresh** restano, così puoi sempre iniziare un altro lorebook.
<a id="mass-edit"></a>

## Modifica di massa

**Modifica di massa** apre una finestra con tutte le voci del lorebook selezionato **e dei suoi sotto-lorebook** (a qualsiasi profondità) in un'unica tabella,
ordinata per **Ordine** delle voci (voci con lo stesso ordine per lorebook, poi per nome). Serve a riordinare un lorebook grande:

- Modifica la casella *abilitato*, **Nome voce**, **Ordine**, **Tag**, **Modalità di filtro**, **Filtro**, **Modalità di inserimento**,
  **Tag negativi**, **Contesto di attivazione** con le sue **Parti**, l'intervallo di parti (**Dal messaggio** e **Al messaggio**, vuoto: nessun limite) e
  la **Nota** di una voce direttamente, come nell'editor. Ogni voce occupa una riga; la tabella scorre verso il basso (e di lato quando la
  finestra è stretta). La riga si sposta nella sua nuova posizione quando cambi l'ordine. Il testo di una voce è abbreviato nella tabella; apri la voce nel
  lorebook per leggerlo o modificarlo per intero.
- La casella **Lorebook** di una riga indica in quale lorebook si trova la voce. Scegline un altro (il lorebook stesso o uno qualsiasi dei suoi sotto-lorebook) per spostarvi la voce.
- Per spostare più voci, selezionale (la casella nell'intestazione seleziona tutte), scegli **Sposta nel lorebook** in fondo e premi **Sposta selezionate**.
- Per eliminare più voci, selezionale e premi **Elimina selezionate**; chiede conferma una sola volta e le sposta nel [cestino](../trash.md).
- Un lorebook su cui lavora un agente è bloccato: le sue voci sono in grigio e non possono essere modificate o spostate, e in esso non si può spostare nulla.
  Il controllo viene fatto per ogni lorebook, quindi un lorebook può essere libero mentre uno dei suoi sotto-lorebook è bloccato (perché usato da un altro libro).
  **Aggiorna** rilegge la finestra per verificare se un blocco è stato rimosso.

Il lorebook nell'editor viene aggiornato alla chiusura della finestra dopo una modifica.

![Modifica di massa di un lorebook e dei suoi sotto-lorebook](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Modifica a schermo intero

**L'editor a schermo intero** (seconda riga della barra degli strumenti) apre il lorebook selezionato in una finestra che occupa tutto lo schermo, per lavorare su voci lunghe. Il pulsante di espansione nella riga di una voce apre la finestra su quella voce. La scheda *Lorebook* della finestra di un libro ha lo stesso pulsante, per il lorebook mostrato lì. La finestra ha come titolo il nome del lorebook. È un insieme di schede, come quelle dell'[editor della storia](../books/story-editor.md): trascina una scheda per affiancarla o sistemarla sotto un'altra, e trascina i divisori tra le schede.

![L'editor a schermo intero del lorebook: l'elenco delle voci e due voci aperte in schede](../../images/lorebook-fullscreen.png)

- La scheda **Entries** elenca le voci, con la casella di ricerca e i filtri per tag e stato della pagina del lorebook, **Add Entry**, **Mass edit**, **Tag entries**, **Preview activation** e **Refresh** (la modifica di massa e i tag mostrano le modifiche anche nelle schede aperte). Con **Include sub lorebooks** elenca anche le voci dei [sub lorebook](#sub-lorebooks), ognuna con il nome del suo lorebook. In questo caso **Add Entry** aggiunge la voce al lorebook scelto accanto ad esso, e la scheda di una voce del genere indica il suo lorebook. La scheda non può essere chiusa. Ogni riga ha tre pulsanti:
  - **Open** (icona a matita, o anche un doppio clic sulla riga) mostra la voce nella scheda della voce che hai usato per ultima oppure, se non ce n'è una, ne apre una in una colonna a destra dell'elenco;
  - **Open in new tab** (icona a freccia) aggiunge una scheda per la voce, accanto alla scheda della voce che hai usato per ultima;
  - **Delete** (icona del cestino) elimina la voce dopo la conferma e chiude la sua scheda.

  Una voce è aperta in una sola scheda al massimo: aprirla di nuovo mostra la scheda che ha già.
- Una **scheda di voce** contiene, dall'alto: il **Name**, l'**Order** e **Enabled**; **Settings**, un pannello che puoi ripiegare, con le altre impostazioni della voce (modalità e filtro di filtraggio, modalità di inserimento, contesto e parti di attivazione, tag e tag negativi, l'intervallo delle parti, le caselle di controllo della ricorsione e la relativa condizione; vedi [Voci e attivazione](entries.md)); poi il **contenuto** in un editor Markdown (ogni scheda ricorda se mostra il testo, il testo e l'anteprima, oppure l'anteprima, anche quando la finestra viene riaperta), e la **nota** accanto, quando **Show note** è selezionato. L'occhio apre l'[anteprima dell'attivazione](entries.md#preview-activation) per questa voce. La riga in basso mostra i token stimati del contenuto e, in rosso, cosa non va nel suo modello (viene comunque salvato). Chiudi una scheda con la sua **×**.
- **Immagini nelle voci.** Incolla un'immagine nel contenuto, trascinala sopra di esso oppure scegline una con il pulsante immagine della barra degli strumenti (PNG, JPEG, GIF o WebP, fino a 10 MB). L'immagine viene salvata insieme ai tuoi file (la scheda *Resources*) e il contenuto riceve una riga come `![Map](resource:…)` nel punto in cui si trovava il cursore. L'anteprima mostra l'immagine, e il testo tra parentesi quadre è la sua didascalia. Elimina la riga per togliere l'immagine dalla voce. Il modello legge la voce senza queste righe. Le immagini stesse vengono inviate solo quando le impostazioni del modello indicano *Multimodal (accepts images)* (vedi [Provider di inferenza](../inference-providers.md)). Le immagini viaggiano con il lorebook: un clone, una copia di un lorebook condiviso, un file di lorebook esportato e un backup del libro con i suoi file le includono, e la copia riceve immagini proprie. Le esportazioni in SillyTavern e quelle dei libri le escludono. La scheda *Lorebook* della storia le mostra sotto il testo della voce. Per ora le immagini non contano nelle stime dei token.
- **Passare da una voce all'altra.** Ctrl+clic (Cmd+clic su Mac) sul nome o su una parola chiave di un'altra voce nel contenuto, nel testo o nell'anteprima, apre quella voce nella scheda che hai usato per ultima; con anche Shift, la apre in una nuova scheda. La riga in basso elenca anche, dopo **Mentioned in:**, le voci il cui contenuto menziona questa; clicca su una di esse per aprirla, oppure Shift+clic per una nuova scheda. Una voce è individuata dal suo nome e dalle parole del suo filtro (ogni parola chiave, l'intero testo di un filtro *Text*, ciò che corrisponde a un'espressione regolare), come parole intere, indipendentemente dalle maiuscole. Questo è simile a ciò che segue la [ricorsione](entries.md), ma non è lo stesso: la ricorsione trova anche una parola chiave dentro una parola più lunga, richiede tutte le parole chiave di un filtro *AND* e controlla anche i tag e la condizione.
- L'editor Markdown ha una barra degli strumenti (grassetto, corsivo, barrato, una linea, titoli, citazione, codice, blocco di codice, tabella e i tre tipi di elenco) e, a destra, le viste *edit*, *edit with preview* (predefinita) e *preview*. Funzionano le scorciatoie usuali (Ctrl+B, Ctrl+I...); Tab passa al campo successivo invece di rientrare il testo. Ciò che scrivi viene salvato quando smetti di scrivere per un momento e quando esci dall'editor.
- L'intestazione mostra i token stimati delle voci attive, **Count with subbooks** e **Search and replace**, che mostra il testo sostituito anche nelle schede aperte.

Tutte le modifiche vengono salvate man mano che le fai, quindi **Close** non fa perdere nulla. La finestra ricorda come l'hai lasciata, per ogni lorebook: la volta successiva che si apre con le stesse voci nelle stesse schede, disposte nello stesso modo (le voci eliminate nel frattempo vengono escluse). La pagina del lorebook mostra di nuovo le voci quando la finestra viene chiusa.

**Modifiche fatte altrove.** Una modifica in una scheda viene salvata nella voce così come è memorizzata in quel momento, quindi ciò che è stato cambiato altrove (la pagina del lorebook, la modifica di massa, un'altra finestra) negli *altri* campi della voce resta, e la scheda lo mostra. Se il **contenuto** o la **nota** sono stati modificati altrove dopo che la scheda li aveva mostrati, il tuo testo non sovrascrive quello salvato: la riga in basso lo segnala, con **Keep my text** (salva comunque il tuo) e **Load the saved text** (scarta il tuo). Una voce eliminata altrove chiude la sua scheda.
La finestra segue anche ciò che viene salvato mentre è aperta: quando un agente o un'altra finestra (un'altra scheda del browser, la scheda *Lorebook* della storia) salva o elimina una voce, l'elenco viene ricaricato e la scheda di quella voce mostra la situazione attuale (la scheda di una voce eliminata si chiude). Una scheda il cui testo è in attesa di **Keep my text** o **Load the saved text** resta così com'è finché non rispondi. Una modifica salvata nello stesso secondo in cui scrivi può ancora non arrivare: in quel caso la riga in basso chiede come sopra.

**Agenti.** La finestra non è offerta mentre un agente lavora su un libro che usa il lorebook. Se un'esecuzione parte mentre la finestra è aperta, la finestra diventa di sola lettura alla tua modifica successiva o quando cambi scheda: una barra indica l'agente (e segnala quando la tua ultima modifica non è stata salvata). Metti in pausa o ferma l'esecuzione, poi clicca su **Check again**: le voci vengono caricate come l'esecuzione le ha lasciate e puoi di nuovo modificare.

Anche le estensioni [Lore Generator](../extensions/lore-generator.md) e [Lorebook VCS](../extensions/lorebook-vcs.md) funzionano qui: la bacchetta si trova nelle righe dell'elenco e nella riga del nome di una scheda di voce, il pannello delle revisioni sta sopra l'elenco e la barra delle revisioni di una voce si trova nella sua scheda.

<a id="tagging-many-at-once"></a>

## Assegnare tag a molti elementi insieme

I due pulsanti dei tag della barra degli strumenti aprono un'unica finestra che assegna un tag a molti elementi, oppure lo rimuove da essi: **Tag lorebooks** elenca i tuoi lorebook, **Tag entries** le voci del lorebook selezionato. Scegli il **Tag** (oppure scrivine uno nuovo e premi Invio); l'elenco mostra ogni elemento con i suoi tag, e gli elementi che hanno il tag scelto compaiono per primi, con il tag evidenziato. I pulsanti **+** e **-** di una riga applicano o rimuovono il tag per quell'elemento; seleziona più elementi (la casella nell'intestazione seleziona tutti) e usa **Apply the tag to the checked** o **Remove the tag from the checked** in fondo per farlo su tutti. Un elemento su cui lavora un agente (un lorebook di un libro con un'esecuzione in corso, e quindi tutte le sue voci) è grigio e non può essere modificato finché l'esecuzione non viene messa in pausa o fermata. La finestra assegna anche tag ai [libri](../books/managing-books.md#tagging-many-books-at-once).
I tag che mette sulle voci sono quelli ordinari, vedi [Tag](entries.md#tags); i tag negativi vengono impostati sulla voce stessa.

![Assegnazione di tag alle voci di un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Usare un lorebook in un libro

Apri il libro e la sua scheda **Lorebook**, poi scegli il lorebook nel campo **Lorebook**. L'editor sottostante mostra il lorebook, così puoi aggiungere e modificare voci mentre scrivi. Svuota il campo per scrivere senza un lorebook.

Un libro usa un solo lorebook. Per usarne più di uno, crea un lorebook che li includa come sub lorebook, e usa quello.

Lo stesso lorebook può essere usato da un numero qualsiasi di libri. Le modifiche al lorebook riguardano tutti i libri a partire dalla parte generata successiva; le parti già scritte mantengono il lore con cui sono state scritte.
<a id="sub-lorebooks"></a>

## Sub lorebook

**Sub Lorebooks** include le voci di altri lorebook al loro interno. Una configurazione tipica:

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- I sub lorebook possono avere a loro volta dei sub lorebook; tutti i livelli vengono inclusi.
- Un lorebook può essere sub lorebook di più lorebook. Quando viene raggiunto più di una volta, le sue voci vengono comunque usate una sola volta.
- Un lorebook non può includere se stesso, né direttamente né attraverso altri lorebook: una scelta del genere viene rifiutata con *Cannot select sub lorebook: cycle detected.*
- Un sub lorebook disabilitato viene escluso insieme a tutto ciò che include.

![Un lorebook con due sub lorebook: i token con e senza di essi](../../images/lorebook-sub-lorebooks.png)

Le voci di tutti i lorebook inclusi vengono raccolte insieme e ordinate per [ordine](entries.md#order-and-insertion), indipendentemente dal lorebook di provenienza.

<a id="disabling-and-deleting"></a>

## Disabilitare ed eliminare

Deseleziona **Enabled** per disattivare temporaneamente un lorebook, ad esempio per vedere come si legge un libro senza di esso. I libri continuano a farvi riferimento, e il lorebook torna a funzionare quando lo riselezioni.

**Delete Lorebook** rimuove il lorebook dalle tue liste. I libri che lo usavano non ricevono più le sue voci, e i lorebook che lo includevano come sub lorebook lo ignorano. I dati restano nel database finché un amministratore non esegue [Cleanup](../administration/cleanup.md); fino ad allora puoi ripristinare il lorebook dal [Trash](../trash.md). Le copie nei [backup dei libri](../books/backups.md) e nei file esportati non sono interessate.
