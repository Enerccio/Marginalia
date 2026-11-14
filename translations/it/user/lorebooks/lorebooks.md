---
label: Creazione dei lorebook
order: 100
---

# Creazione dei lorebook

<a id="the-lorebook-editor"></a>

## L'editor dei lorebook

Apri la scheda **Lorebook**. L'editor mostra un lorebook alla volta; sceglilo nel campo **Lorebook** in alto.
I lorebook appartengono al tuo account: gli altri utenti non li vedono, a meno che tu non ne [condivida](#sharing-a-lorebook) uno.

La barra degli strumenti ha due righe, che vanno a capo su più linee in una finestra stretta: la prima per i lorebook (da **Lorebook** a **Tagga i lorebook**), la seconda per le voci del lorebook selezionato (da **Aggiungi voce** ad **Aggiorna**).

| Pulsante | |
|---|---|
| **Lorebook** | Seleziona il lorebook da modificare. |
| **Solo i lorebook radice** | Elenca solo i lorebook che non sono un sotto-lorebook di un altro (il lorebook selezionato resta nell'elenco). |
| **Aggiungi lorebook** | Crea un nuovo lorebook chiamato *New Lorebook* e lo seleziona. |
| Clona (icona di due fogli) | Crea una copia del lorebook selezionato: chiede il nome (quello originale con *(Copy)*) e seleziona la copia. La copia ha le stesse voci (con le loro impostazioni, tag e tag negativi), gli stessi tag, la stessa descrizione e i dati delle estensioni. I suoi sotto-lorebook non vengono copiati: la copia usa gli stessi. L'originale non viene modificato. |
| **Elimina lorebook** | Elimina il lorebook selezionato (dopo conferma). |
| Importa (icona di file) | **Importa un lorebook di Marginalia**, **Importa da SillyTavern**, **Importa da un archivio** (uno zip di file `.md`) oppure **Importa da un altro utente** (un lorebook che un altro utente [condivide](#sharing-a-lorebook)), vedi [Importazione ed esportazione](import-export.md). |
| Esporta (icona di file) | Scarica il lorebook con i suoi sotto-lorebook, vedi [Importazione ed esportazione](import-export.md#exporting). |
| Tag (icona di tag) | **Tagga i lorebook**: applica un tag a molti lorebook in una volta sola, vedi [sotto](#tagging-many-at-once). Non mostrato per un lorebook aperto in un libro. |
| Descrizione (icona di righe, l'ultimo della prima riga) | **Modifica la descrizione**: apre una finestra in cui scrivere di cosa tratta il lorebook, vedi [sotto](#description). |
| **Aggiungi voce** | Aggiunge una voce al lorebook, vedi [Voci e attivazione](entries.md). |
| **Editor a schermo intero** | Apre le voci del lorebook in una finestra a schermo intero con schede, vedi [Modifica a schermo intero](#editing-in-full-screen). Solo desktop. |
| **Cerca e sostituisci** | Cambia un testo nel nome, nel testo e nel commento delle voci del lorebook, vedi [Cerca e sostituisci](entries.md#search-and-replace). |
| **Modifica di massa** | Apre tutte le voci del lorebook e dei suoi sotto-lorebook in un'unica tabella, vedi [Modifica di massa](#mass-edit). |
| Tag (icona di tag, a destra di **Cerca e sostituisci**, seconda riga) | **Tagga le voci**: applica un tag a molte voci del lorebook selezionato in una volta sola, vedi [sotto](#tagging-many-at-once). |
| **Anteprima dell'attivazione** | Mostra quali voci userebbe un turno con un testo che digiti e perché, vedi [Anteprima dell'attivazione](entries.md#preview-activation). |
| **Aggiorna** | Ricarica le voci. |
| Casella di ricerca | Mostra solo le voci che contengono il testo, vedi [Cercare ed eliminare molte voci](entries.md#searching-and-deleting-many-entries). |
| **Elimina i selezionati** | Compare quando nell'elenco sono selezionate delle voci; le elimina dopo conferma. |

Sotto la barra degli strumenti ci sono le impostazioni del lorebook selezionato:

| Campo | |
|---|---|
| **Nome** | Il nome del lorebook, mostrato quando lo scegli per un libro o come sotto-lorebook. |
| **Sotto-lorebook** | Altri lorebook le cui voci sono incluse con questo, vedi [sotto](#sub-lorebooks). Clicca il nome di un sotto-lorebook selezionato per aprirlo. |
| **Tag** | Tag dell'intero lorebook; contano come tag di ciascuna delle sue voci, vedi [Tag](entries.md#tags). |
| **Profondità di scansione** | `0` a `5`: quante volte il contenuto delle voci usate viene cercato per i filtri delle altre voci, vedi [Voci portate dentro da altre voci](entries.md#entries-brought-in-by-other-entries). Vale per le voci del lorebook usato da un libro e per quelle dei suoi sotto-lorebook, a meno che un sotto-lorebook non abbia una profondità propria (riga successiva). |
| **Profondità di scansione come sotto-lorebook** | Vuoto, oppure da `0` a `5`: la profondità di scansione per le voci di questo lorebook mentre è un sotto-lorebook di quello usato da un libro. Vuoto significa la profondità di scansione di quel lorebook; `0` significa che le voci di questo lorebook non vengono mai trovate dal contenuto degli altri. |
| **Attivata** | Se deselezionata, il lorebook non contribuisce con nulla: né con le sue voci né con quelle dei suoi sotto-lorebook. |
| **Condividi con altri utenti** | Se selezionata, gli altri utenti possono consultare il lorebook e importarne una copia, vedi [sotto](#sharing-a-lorebook). Disattivata per un nuovo lorebook. |

Sotto le impostazioni c'è la [descrizione](#description) del lorebook, quando ne ha una.

Il resto dell'editor è l'elenco delle [voci](entries.md). Tutte le modifiche vengono salvate man mano che le fai.

<a id="description"></a>

### Descrizione

La descrizione è poche parole (o righe) sul lorebook: quale mondo descrive, a cosa serve, quale libro lo usa. Clicca l'icona di righe nella prima riga della barra degli strumenti, scrivi il testo nella finestra e confermi; un testo vuoto la rimuove. Un lorebook che ha una descrizione la mostra su una riga sotto le sue impostazioni, troncata con puntini di sospensione dove non ci sta; posa il puntatore del mouse sulla riga per leggerla tutta. La descrizione viene copiata insieme al lorebook (clonazione, esportazione, importazione, backup) ed è il testo che gli altri utenti vedono accanto al nome quando lo condividi.

![La finestra della descrizione di un lorebook](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Condivisione di un lorebook

Seleziona **Condividi con altri utenti** e ogni altro utente di questa installazione vede il lorebook in **Importa da un altro utente** (il pulsante di importazione nella prima riga della barra degli strumenti), con il suo nome, il suo autore (il tuo nome completo, o il tuo login se non ne hai uno) e la sua descrizione. Possono aprire una riga per leggere le voci (e le voci dei sotto-lorebook) e importarne una copia. Deseleziona la casella e il lorebook sparisce dal loro elenco; le copie che hanno già fatto restano loro.

* La condivisione è solo in lettura. Nessun altro può modificare, eliminare o usare il tuo lorebook in un libro; il lorebook resta tuo e tu lo vedi come prima.
* Un'importazione crea **nuovi lorebook dell'altro utente**: il lorebook e i suoi sotto-lorebook **condivisi**, a qualsiasi profondità, con le loro voci, tag e descrizione, collegati come lo sono i tuoi. Le copie sono private (non condivise): qualsiasi cosa tu faccia dopo al tuo lorebook non le raggiunge, e ciò che loro cambiano non raggiunge il tuo.
* **Vengono mostrati e copiati solo i sotto-lorebook che sono a loro volta condivisi.** Un sotto-lorebook non condiviso viene escluso insieme a tutto ciò che sta sotto, anche un lorebook condiviso che si trova più in basso. Quando selezioni **Condividi con altri utenti** su un lorebook che ha sotto-lorebook non condivisi, una domanda chiede se condividere anche quelli: **Sì** condivide il lorebook e tutti i sotto-lorebook (a qualsiasi profondità), **No** condivide solo il lorebook, **Annulla** non condivide nulla e lascia la casella deselezionata. Un sotto-lorebook aggiunto in seguito non viene condiviso finché non lo condividi.
* Deselezionare la casella interrompe la condivisione del solo lorebook; i suoi sotto-lorebook restano condivisi (e compaiono da soli nell'elenco degli altri) finché non li deselezioni anche tu.
* I lorebook eliminati, le copie di lavoro degli agenti e i tuoi lorebook non compaiono nell'elenco, e non compare nulla per un utente che è stato eliminato.

Sotto le impostazioni viene mostrata la stima dei token delle voci attivate del lorebook. Il link **Conta con i sotto-libri** accanto ad essa somma
i token delle voci attivate del lorebook e di tutti i suoi sotto-lorebook, a qualsiasi profondità, e mostra il totale accanto. Come quando una
storia viene scritta, un sotto-lorebook disattivato viene escluso insieme ai lorebook che stanno sotto di esso, e un lorebook che è sotto-lorebook in più punti viene contato una sola volta.
Il totale è un conteggio del momento in cui clicchi: se modifichi una voce scompare, clicca di nuovo per contare di nuovo.

Un lorebook di un libro su cui lavora un agente è mostrato in sola lettura (vedi [Agenti](../agents.md)), senza **Aggiungi voce**, **Cerca e sostituisci**, **Modifica di massa**, **Elimina lorebook** e le impostazioni; **Aggiungi lorebook**, importazione, esportazione e **Aggiorna** restano, così puoi sempre iniziare un altro lorebook.
## Mass edit

**Mass edit** apre una finestra con tutte le voci del lorebook selezionato **e dei suoi sotto-lorebook** (a qualsiasi profondità) in un'unica tabella,
ordinate per **Ordine** delle voci (voci con lo stesso ordine raggruppate per lorebook, poi per nome). Serve a sistemare un lorebook grande:

- Modifica in place la casella *abilitato*, il **Nome della voce**, l'**Ordine**, i **Tag**, la **Modalità di filtro**, il **Filtro**, la **Modalità di inserimento**, i
  **Tag negativi**, il **Contesto di attivazione** con le sue **Parti**, l'intervallo di parti (**Dal messaggio** e **Al messaggio**, vuoto: nessun limite) e la
  **Nota** di una voce, come nell'editor. Ogni voce occupa una riga; la tabella scorre verso il basso (e di lato quando la finestra è stretta). La riga si sposta nella sua nuova posizione quando cambi l'ordine. Il testo di una voce è abbreviato nella tabella; apri la voce nel lorebook per leggerlo o modificarlo per intero.
- La casella **Lorebook** di una riga indica in quale lorebook si trova la voce. Scegline un altro (il lorebook stesso o uno dei suoi sotto-lorebook) per spostarvi la voce.
- Per spostare più voci, selezionale (la casella nell'intestazione seleziona tutte), scegli **Sposta nel lorebook** in fondo e premi **Sposta i selezionati**.
- Per eliminare più voci, selezionale e premi **Elimina i selezionati**; chiede una sola conferma e le sposta nel [cestino](../trash.md).
- Un lorebook su cui lavora un agente è bloccato: le sue voci sono in grigio e non possono essere modificate o spostate, e in esso non si può spostare nulla. Questo controllo è fatto per ciascun lorebook, quindi un lorebook può essere libero mentre uno dei suoi sotto-lorebook è bloccato (perché è usato da un altro libro). **Aggiorna** rilegge la finestra per verificare se un blocco è stato rimosso.

Il lorebook nell'editor viene aggiornato alla chiusura della finestra dopo una modifica.

![Modifica di massa di un lorebook e dei suoi sotto-lorebook](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Modifica a schermo intero

**Editor a schermo intero** (seconda riga della barra degli strumenti) apre il lorebook selezionato in una finestra che occupa tutto lo schermo, per lavorare su voci lunghe
(il pulsante di espansione nella riga di una voce apre la finestra su quella voce); la scheda *Lorebook* della finestra di un libro ha lo stesso pulsante, per il lorebook mostrato lì. La finestra porta il nome del lorebook. È un insieme di schede come quello dell'[editor della storia](../books/story-editor.md): trascina una scheda per metterla accanto o sotto un'altra, e trascina i divisori tra le schede.

![L'editor a schermo intero del lorebook: l'elenco delle voci e due voci aperte in schede](../../images/lorebook-fullscreen.png)

- La scheda **Voci** elenca le voci, con la casella di ricerca e i filtri per tag e stato della pagina del lorebook,
  **Aggiungi voce**, **Modifica di massa**, **Tagga le voci**, **Anteprima dell'attivazione** e **Aggiorna** (modifica di massa e tag mostrano le loro modifiche anche nelle schede aperte). Con **Includi i sotto-lorebook** elenca anche le voci dei [sotto-lorebook](#sub-lorebooks), ciascuna con il nome del proprio lorebook; **Aggiungi voce** aggiunge allora la voce al lorebook scelto accanto ad essa, e la scheda di una tale voce riporta il nome del suo lorebook. Non può essere chiusa. Ogni riga ha tre pulsanti:
  - **Apri** (icona a matita, oppure doppio clic sulla riga) mostra la voce nella scheda della voce che hai usato per ultima o, se non ce n'è una, ne apre una in una colonna a destra dell'elenco;
  - **Apri in una nuova scheda** (icona a freccia) aggiunge una scheda per la voce, accanto alla scheda della voce che hai usato per ultima;
  - **Elimina** (icona del cestino) elimina la voce dopo la conferma, e chiude la sua scheda.

  Una voce è aperta in una scheda al massimo: riaprirla mostra la scheda che ha già.
- Una **scheda della voce** ha, dall'alto, il **Nome**, l'**Ordine** e **Attivata**; **Impostazioni**, un pannello che puoi chiudere,
  con le altre impostazioni della voce (modalità e filtro di filtraggio, modalità di inserimento, contesto e parti di attivazione,
  tag e tag negativi, l'intervallo delle parti, le caselle della ricorsione e la sua condizione, vedi [Voci e attivazione](entries.md)); poi il **contenuto** in un editor Markdown (ogni scheda ricorda se mostra il testo, il
  testo e l'anteprima, o solo l'anteprima, anche quando la finestra viene riaperta), e la **nota** accanto, quando **Mostra la nota** è selezionata. L'occhio apre l'[anteprima dell'attivazione](entries.md#preview-activation) per questa voce. La riga in basso mostra i token stimati del contenuto e, in rosso, cosa non va nel suo template (viene salvata comunque). Chiudi una scheda con la sua **×**.
- **Immagini nelle voci.** Incolla un'immagine nel contenuto, trascinane una sopra, oppure scegline una con il pulsante immagine della barra degli strumenti
  (PNG, JPEG, GIF o WebP, fino a 10 MB). Viene salvata con i tuoi file (la scheda *Risorse*) e nel contenuto compare una riga come `![Map](resource:…)` dove si trovava il cursore; l'anteprima mostra l'immagine, e il testo tra parentesi quadre è la sua didascalia.
  Elimina la riga per togliere l'immagine dalla voce. Il modello legge la voce senza queste righe. Le immagini vengono inviate solo quando le impostazioni del modello dicono *Invia le immagini del lore* (vedi [Fornitori di inferenza](../inference-providers.md)).
  Le immagini viaggiano con il lorebook: un clone, una copia di un lorebook condiviso, un file di lorebook esportato e un backup del libro con i suoi file le portano con sé, e la copia ne ottiene di proprie. L'esportazione per SillyTavern e le esportazioni dei libri le lasciano fuori. La scheda *Lorebook* della storia le mostra sotto il testo della voce. Le immagini non contano ancora nelle stime dei token.
- **Passare da una voce all'altra.** Ctrl+clic (Cmd+clic su Mac) sul nome o su una parola chiave di un'altra voce nel contenuto, nel testo o nell'anteprima, per aprire quella voce nella scheda che hai usato per ultima; con anche Shift si apre in una nuova scheda. La riga in basso elenca anche, dopo **Citato in:**, le voci il cui contenuto nomina questa; cliccane una per aprirla, Shift+clic per una nuova scheda. Una voce è individuata dal suo nome e dalle parole del suo filtro (ogni parola chiave, l'intero testo di un filtro *Testo*, ciò che corrisponde a un'espressione regolare), come parole intere, maiuscole o minuscole indifferentemente. Questo è simile a ciò che segue la [ricorsione](entries.md) ma non è lo stesso: la ricorsione trova anche una parola chiave dentro una parola più lunga, richiede tutte le parole chiave di un filtro *AND* e controlla i tag e la condizione.
- L'editor Markdown ha una barra degli strumenti (grassetto, corsivo, barrato, una linea, titoli, citazione, codice, blocco di codice, tabella e i tre tipi di elenco) e, a destra, le viste *modifica*, *modifica con anteprima* (predefinita) e *anteprima*. Funzionano i soliti tasti (Ctrl+B, Ctrl+I...); Tab passa al campo successivo invece di rientrare. Ciò che scrivi viene salvato quando smetti di scrivere per un momento e quando esci dall'editor.
- L'intestazione mostra i token stimati delle voci attivate, **Conta con i sotto-libri** e **Cerca e sostituisci**, che mostra il testo sostituito anche nelle schede aperte.

Tutte le modifiche vengono salvate man mano che le fai, quindi **Chiudi** non fa perdere nulla. La finestra ricorda come l'hai lasciata, per ciascun lorebook:
la volta successiva che si apre con le stesse voci nelle stesse schede, disposte allo stesso modo (le voci eliminate nel frattempo vengono omesse). La pagina del lorebook mostra di nuovo le voci quando la finestra viene chiusa.

**Modifiche fatte altrove.** Una modifica in una scheda viene salvata nella voce così come è memorizzata in quel momento, quindi ciò che è stato cambiato altrove (la pagina del lorebook, la modifica di massa, un'altra finestra) negli *altri* campi della voce resta, e la scheda lo mostra.
Se il **contenuto** o la **nota** sono stati cambiati altrove da quando la scheda li mostrava, il tuo testo non viene salvato sopra quello: la riga in basso lo segnala, con **Mantieni il mio testo** (salva comunque il tuo) e **Carica il testo salvato** (scarta il tuo). Una voce eliminata altrove chiude la sua scheda.
La finestra segue anche ciò che viene salvato mentre è aperta: quando un agente o un'altra finestra (un'altra scheda del browser, la scheda *Lorebook* della storia) salva o elimina una voce, l'elenco viene ricaricato e la scheda di quella voce mostra lo stato attuale (la scheda di una voce eliminata si chiude). Una scheda il cui testo è in attesa di **Mantieni il mio testo** o **Carica il testo salvato** resta com'è finché non rispondi. Una modifica salvata nello stesso secondo in cui scrivi potrebbe ancora incrociarsi: la riga in basso allora chiede come sopra.

**Agenti.** La finestra non è disponibile mentre un agente lavora su un libro che usa il lorebook. Se un'esecuzione parte mentre la finestra è aperta, la finestra diventa di sola lettura alla tua prossima modifica o quando cambi scheda: una barra indica l'agente (e dice quando la tua ultima modifica non è potuta essere salvata). Metti in pausa o interrompi l'esecuzione, poi clicca **Controlla di nuovo**: le voci vengono caricate come l'esecuzione le ha lasciate e puoi di nuovo modificare.

Anche le estensioni [Generatore di lore](../extensions/lore-generator.md) e [VCS del lorebook](../extensions/lorebook-vcs.md) funzionano qui: la bacchetta si trova nelle righe dell'elenco e nella riga del nome di una scheda della voce, il pannello delle revisioni sta sopra l'elenco, e la barra delle revisioni di una voce si trova nella sua scheda.

<a id="tagging-many-at-once"></a>

## Taggare molti elementi insieme

I due pulsanti dei tag della barra degli strumenti aprono una sola finestra che mette un tag su più elementi, o lo toglie da essi: **Tagga i lorebook** elenca i tuoi lorebook, **Tagga le voci** le voci del lorebook selezionato. Scegli il **Tag** (oppure digita un nuovo tag e premi Invio); l'elenco mostra ogni elemento con i suoi tag, e gli elementi che hanno il tag scelto vengono per primi, con il tag evidenziato. I pulsanti **+** e **-** di una riga applicano o rimuovono il tag per quell'elemento; seleziona più elementi (la casella nell'intestazione seleziona tutti) e usa **Applica il tag a quelli selezionati** o **Rimuovi il tag da quelli selezionati** in fondo per farlo su tutti. Un elemento su cui lavora un agente (un lorebook di un libro con un'esecuzione in corso, e quindi tutte le sue voci) è in grigio e non può essere modificato finché l'esecuzione non viene messa in pausa o interrotta. La finestra tagga anche i [libri](../books/managing-books.md#tagging-many-books-at-once).
I tag che mette sulle voci sono quelli ordinari, vedi [Tag](entries.md#tags); i tag negativi vengono impostati sulla voce stessa.

![Taggare le voci di un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Usare un lorebook in un libro

Apri il libro e la sua scheda **Lorebook**, poi scegli il lorebook nel campo **Lorebook**. L'editor sotto mostra il lorebook, così puoi aggiungere e modificare voci mentre scrivi. Svuota il campo per scrivere senza un lorebook.

Un libro usa un solo lorebook. Per usarne diversi, crea un lorebook che li includa come sotto-lorebook, e usa quello.

Lo stesso lorebook può essere usato da un numero qualsiasi di libri. Le modifiche ad esso hanno effetto su tutti dalla prossima parte generata; le parti già scritte mantengono il lore con cui sono state scritte.
<a id="sub-lorebooks"></a>

## Sotto-lorebook

**Sotto-lorebook** include le voci di altri lorebook in questo. Una configurazione tipica:

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- I sotto-lorebook possono avere a loro volta dei sotto-lorebook; vengono inclusi tutti i livelli.
- Un lorebook può essere sotto-lorebook di più lorebook. Quando viene raggiunto più di una volta, le sue voci vengono comunque usate una sola volta.
- Un lorebook non può includere sé stesso, né direttamente né tramite altri lorebook: una scelta del genere viene rifiutata con il messaggio *Impossibile selezionare il sotto-lorebook: rilevato un ciclo.*
- Un sotto-lorebook disattivato viene escluso insieme a tutto ciò che include.

![Un lorebook con due sotto-lorebook: i token con e senza di essi](../../images/lorebook-sub-lorebooks.png)

Le voci di tutti i lorebook inclusi vengono unite e ordinate per [ordine](entries.md#order-and-insertion), indipendentemente dal lorebook di provenienza.

<a id="disabling-and-deleting"></a>

## Disattivazione ed eliminazione

Deseleziona **Attivata** per disattivare temporaneamente un lorebook, ad esempio per vedere come si legge un libro senza di esso. I libri continuano a usarlo, e torna a funzionare quando lo riattivi.

**Elimina lorebook** rimuove il lorebook dalle tue liste. I libri che lo usavano non ricevono più le sue voci, e i lorebook che lo includevano come sotto-lorebook lo saltano. I dati restano nel database finché un amministratore non esegue la [pulizia](../administration/cleanup.md); fino ad allora puoi ripristinare il lorebook dal [Cestino](../trash.md). Le copie nei [backup dei libri](../books/backups.md) e nei file esportati non sono interessate.
