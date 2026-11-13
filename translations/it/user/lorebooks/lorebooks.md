---
label: Creazione dei lorebook
order: 100
---

# Creazione dei lorebook

<a id="the-lorebook-editor"></a>

## L'editor dei lorebook

Apri la scheda **Lorebook**. L'editor mostra un lorebook alla volta; sceglilo nel campo **Lorebook** in alto.
I lorebook appartengono al tuo account: gli altri utenti non li vedono, a meno che tu non ne [condivida](#sharing-a-lorebook) uno.

La barra degli strumenti ha due righe, che vanno a capo su più linee in una finestra stretta: la prima per i lorebook (da **Lorebook** a **Tag lorebook**), la seconda per le voci del lorebook selezionato (da **Aggiungi voce** ad **Aggiorna**).

| Pulsante | |
|---|---|
| **Lorebook** | Seleziona il lorebook da modificare. |
| **Solo i lorebook radice** | Elenca solo i lorebook che non sono sotto-lorebook di un altro (il lorebook selezionato resta nell'elenco). |
| **Aggiungi lorebook** | Crea un nuovo lorebook chiamato *New Lorebook* e lo seleziona. |
| Clona (icona di due fogli) | Crea una copia del lorebook selezionato: chiede il nome (quello dell'originale con *(Copy)*) e seleziona la copia. La copia ha le stesse voci (con le loro impostazioni, tag e tag negativi), i tag, la descrizione e i dati delle estensioni. I suoi sotto-lorebook non vengono copiati: la copia usa gli stessi. L'originale non viene modificato. |
| **Elimina lorebook** | Elimina il lorebook selezionato (dopo conferma). |
| Importa (icona di file) | **Importa un lorebook di Marginalia**, **Importa da SillyTavern**, **Importa da un archivio** (uno zip di file `.md`) o **Importa da un altro utente** (un lorebook che un altro utente [condivide](#sharing-a-lorebook)), vedi [Importazione ed esportazione](import-export.md). |
| Esporta (icona di file) | Scarica il lorebook con i suoi sotto-lorebook, vedi [Importazione ed esportazione](import-export.md#exporting). |
| Tag (icona di tag) | **Tagga i lorebook**: applica un tag a più lorebook contemporaneamente, vedi [sotto](#tagging-many-at-once). Non mostrato per un lorebook aperto in un libro. |
| Descrizione (icona di righe, ultima della prima riga) | **Modifica la descrizione**: apre una finestra in cui scrivere di cosa tratta il lorebook, vedi [sotto](#description). |
| **Aggiungi voce** | Aggiunge una voce al lorebook, vedi [Voci e attivazione](entries.md). |
| **Editor a schermo intero** | Apre le voci del lorebook in una finestra a schermo intero con schede, vedi [Modifica a schermo intero](#editing-in-full-screen). Solo desktop. |
| **Cerca e sostituisci** | Cambia un testo nel nome, nel testo e nel commento delle voci del lorebook, vedi [Cerca e sostituisci](entries.md#search-and-replace). |
| **Modifica di massa** | Apre tutte le voci del lorebook e dei suoi sotto-lorebook in un'unica tabella, vedi [Modifica di massa](#mass-edit). |
| Tag (icona di tag, a destra di **Cerca e sostituisci**, seconda riga) | **Tagga le voci**: applica un tag a più voci del lorebook selezionato contemporaneamente, vedi [sotto](#tagging-many-at-once). |
| **Anteprima dell'attivazione** | Mostra quali voci userebbe un turno con un testo che scrivi e perché, vedi [Anteprima dell'attivazione](entries.md#preview-activation). |
| **Aggiorna** | Ricarica le voci. |
| Casella di ricerca | Mostra solo le voci che contengono il testo, vedi [Cercare ed eliminare molte voci](entries.md#searching-and-deleting-many-entries). |
| **Elimina i selezionati** | Compare quando nell'elenco sono selezionate delle voci; le elimina dopo conferma. |

Sotto la barra degli strumenti ci sono le impostazioni del lorebook selezionato:

| Campo | |
|---|---|
| **Nome** | Il nome del lorebook, mostrato quando lo scegli per un libro o come sotto-lorebook. |
| **Sotto-lorebook** | Altri lorebook le cui voci sono incluse insieme a questo, vedi [sotto](#sub-lorebooks). Clicca sul nome di un sotto-lorebook selezionato per aprirlo. |
| **Tag** | Tag dell'intero lorebook; contano come tag di ciascuna sua voce, vedi [Tag](entries.md#tags). |
| **Profondità di scansione** | `0` a `5`: quante volte il contenuto delle voci usate viene cercato per i filtri delle altre voci, vedi [Voci portate dentro da altre voci](entries.md#entries-brought-in-by-other-entries). Vale per le voci del lorebook che un libro usa e per quelle dei suoi sotto-lorebook, a meno che un sotto-lorebook non abbia una propria profondità (riga successiva). |
| **Profondità di scansione come sotto-lorebook** | Vuoto, oppure da `0` a `5`: la profondità di scansione per le voci di questo lorebook mentre è un sotto-lorebook di quello usato da un libro. Vuoto significa la profondità di scansione di quel lorebook; `0` significa che le voci di questo lorebook non vengono mai trovate dal contenuto di altre voci. |
| **Attivata** | Se deselezionata, il lorebook non contribuisce con nulla: né le sue voci né quelle dei suoi sotto-lorebook. |
| **Condividi con altri utenti** | Se selezionata, gli altri utenti possono consultare il lorebook e importarne una copia, vedi [sotto](#sharing-a-lorebook). Disattivata per un nuovo lorebook. |

Sotto le impostazioni c'è la [descrizione](#description) del lorebook, quando ne ha una.

Il resto dell'editor è l'elenco delle [voci](entries.md). Tutte le modifiche vengono salvate man mano che le fai.

<a id="description"></a>

### Descrizione

La descrizione è di poche parole (o righe) sul lorebook: che mondo è, a cosa serve, quale libro lo usa. Clicca sull'icona di righe nella prima riga della barra degli strumenti, scrivi il testo nella finestra e conferma; un testo vuoto la rimuove. Un lorebook che ha una descrizione la mostra su una riga sotto le sue impostazioni, troncata con puntini di sospensione dove non ci sta; posa il puntatore del mouse sulla riga per leggerla tutta. La descrizione viene copiata insieme al lorebook (clonazione, esportazione, importazione, backup) ed è il testo che gli altri utenti vedono accanto al suo nome quando lo condividi.

![La finestra della descrizione di un lorebook](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Condivisione di un lorebook

Seleziona **Condividi con altri utenti** e ogni altro utente di questa installazione vedrà il lorebook in **Importa da un altro utente** (il pulsante di importazione nella prima riga della barra degli strumenti), con il suo nome, il suo autore (il tuo nome completo, o il tuo login se non ne hai uno) e la sua descrizione. Possono aprire una riga per leggere le voci (e le voci dei sotto-lorebook) e importarne una copia. Deseleziona la casella e il lorebook scompare dal loro elenco; le copie che hanno già fatto restano loro.

* La condivisione è solo in lettura. Nessun altro può modificare, eliminare o usare il tuo lorebook in un libro; il tuo lorebook resta tuo e tu lo vedi come prima.
* Un'importazione crea **nuovi lorebook dell'altro utente**: il lorebook e i suoi sotto-lorebook **condivisi**, a qualsiasi profondità, con le loro voci, tag e descrizione, collegati come lo sono i tuoi. Le copie sono private (non condivise): qualsiasi cosa tu faccia poi al tuo lorebook non le raggiunge, e ciò che loro modificano non raggiunge il tuo.
* **Vengono mostrati e copiati solo i sotto-lorebook che sono a loro volta condivisi.** Un sotto-lorebook non condiviso viene escluso insieme a tutto ciò che sta sotto di esso, anche un lorebook condiviso al di sotto. Quando selezioni **Condividi con altri utenti** su un lorebook che ha sotto-lorebook non condivisi, una domanda chiede se condividere anche quelli: **Sì** condivide il lorebook e tutti i sotto-lorebook (a qualsiasi profondità), **No** condivide solo il lorebook, **Annulla** non condivide nulla e lascia la casella deselezionata. Un sotto-lorebook aggiunto in seguito non viene condiviso finché non lo condividi tu.
* Deselezionare la casella interrompe la condivisione del solo lorebook; i suoi sotto-lorebook restano condivisi (e compaiono nell'elenco degli altri da soli) finché non li deselezioni anche tu.
* I lorebook eliminati, le copie di lavoro degli agenti e i tuoi lorebook non compaiono nell'elenco, e non compare nulla per un utente che è stato eliminato.

Sotto le impostazioni viene mostrata la stima dei token delle voci attivate del lorebook. Il link **Conta con i sotto-libri** accanto ad essa somma
i token delle voci attivate del lorebook e di tutti i suoi sotto-lorebook, a qualsiasi profondità, e mostra il totale accanto. Come quando una
storia viene scritta, un sotto-lorebook disattivato viene escluso insieme ai lorebook che stanno sotto di esso, e un lorebook che è sotto-lorebook in più punti viene contato una sola volta.
Il totale è un conteggio del momento in cui clicchi: se modifichi una voce scompare, clicca di nuovo per contare di nuovo.

Un lorebook di un libro su cui lavora un agente viene mostrato in sola lettura (vedi [Agenti](../agents.md)), senza **Aggiungi voce**, **Cerca e sostituisci**, **Modifica di massa**, **Elimina lorebook** e le impostazioni; **Aggiungi lorebook**, importazione, esportazione e **Aggiorna** restano disponibili, così puoi sempre avviare un altro lorebook.
<a id="mass-edit"></a>

## Modifica di massa

**Modifica di massa** apre una finestra con tutte le voci del lorebook selezionato **e dei suoi sotto-lorebook** (a qualsiasi profondità) in un'unica tabella,
ordinate per **Ordine** delle voci (voci con lo stesso ordine per lorebook, poi per nome). Serve a riordinare un lorebook grande:

- Modifica direttamente, come nell'editor, la casella *enabled*, il **Nome della voce**, l'**Ordine**, i **Tag**, la **Modalità di filtro**, il **Filtraggio**, la **Modalità di inserimento**, i
  **Tag negativi**, il **Contesto di attivazione** con le sue **Parti**, l'intervallo di parti (**Dal messaggio** e **Al messaggio**, vuoto: nessun limite) e la
  **Nota** di una voce. Ogni voce occupa una riga; la tabella scorre verso il basso (e di lato quando la finestra è stretta). La riga si sposta nella sua nuova posizione quando cambi l'ordine. Il testo di una voce è abbreviato nella tabella; apri la voce nel lorebook per leggerlo o modificarlo per intero.
- La casella **Lorebook** di una riga indica in quale lorebook si trova la voce. Scegline un altro (il lorebook stesso o uno dei suoi sotto-lorebook) per spostarla lì.
- Per spostare più voci, selezionale (la casella nell'intestazione seleziona tutte), scegli **Sposta nel lorebook** in fondo e premi **Sposta i selezionati**.
- Per eliminare più voci, selezionale e premi **Elimina i selezionati**; chiede una sola conferma e le sposta nel [cestino](../trash.md).
- Un lorebook su cui lavora un agente è bloccato: le sue voci sono in grigio e non possono essere modificate o spostate, e non si può spostare nulla al suo interno. Il controllo avviene per ciascun lorebook, quindi un lorebook può essere libero mentre uno dei suoi sotto-lorebook è bloccato (perché usato da un altro libro). **Aggiorna** rilegge la finestra per verificare se un blocco è stato rimosso.

Il lorebook nell'editor viene aggiornato quando la finestra si chiude dopo una modifica.

![Modifica di massa di un lorebook e dei suoi sotto-lorebook](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Modifica a schermo intero

**Editor a schermo intero** (seconda riga della barra degli strumenti) apre il lorebook selezionato in una finestra che occupa tutto lo schermo, per lavorare su voci lunghe
(il pulsante di espansione nella riga di una voce apre la finestra su quella voce; la scheda *Lorebook* della finestra di un libro ha lo stesso pulsante, per il lorebook mostrato lì). La finestra porta il nome del lorebook. È un insieme di schede come quello dell'[editor della storia](../books/story-editor.md): trascina una scheda per metterla accanto o sotto un'altra, e trascina i divisori tra di esse.

![L'editor a schermo intero del lorebook: l'elenco delle voci e due voci aperte in schede](../../images/lorebook-fullscreen.png)

- La scheda **Voci** elenca le voci, con la casella di ricerca e i filtri per tag e stato della pagina del lorebook,
  **Aggiungi voce**, **Modifica di massa**, **Tagga le voci**, **Anteprima dell'attivazione** e **Aggiorna** (modifica di massa e tag mostrano le loro modifiche anche nelle schede aperte). Con **Includi i sotto-lorebook** elenca anche le voci dei [sotto-lorebook](#sub-lorebooks), ciascuna con il nome del proprio lorebook; **Aggiungi voce** aggiunge allora la voce al lorebook scelto accanto ad esso, e la scheda di una voce del genere indica il suo lorebook. Non può essere chiusa. Ogni riga ha tre pulsanti:
  - **Apri** (icona a penna, oppure doppio clic sulla riga) mostra la voce nella scheda della voce che hai usato per ultima o, se non ce n'è una, ne apre una in una colonna a destra dell'elenco;
  - **Apri in una nuova scheda** (icona a freccia) aggiunge una scheda per la voce, accanto alla scheda della voce che hai usato per ultima;
  - **Elimina** (icona del cestino) elimina la voce dopo la conferma, e chiude la sua scheda.

  Una voce è aperta in una sola scheda al massimo: aprirla di nuovo mostra la scheda che ha già.
- Una **scheda voce** ha, dall'alto: il **Nome**, l'**Ordine** e **Attivata**; **Impostazioni**, un pannello che puoi ripiegare,
  con le altre impostazioni della voce (modalità e filtro di filtraggio, modalità di inserimento, contesto e parti di attivazione,
  tag e tag negativi, l'intervallo delle parti, le caselle della ricorsione e la condizione, vedi [Voci e attivazione](entries.md));
  poi il **contenuto** in un editor Markdown (ogni scheda ricorda se mostra il testo, il testo e l'anteprima, o solo l'anteprima, anche quando la finestra viene riaperta), e la **nota** accanto, quando **Mostra la nota** è selezionata. L'occhio apre l'[anteprima dell'attivazione](entries.md#preview-activation) per questa voce. La riga in basso mostra i token stimati del contenuto e, in rosso, cosa non va nel suo modello (viene comunque salvato). Chiudi una scheda con la sua **×**.
- **Immagini nelle voci.** Incolla un'immagine nel contenuto, trascinane una sopra, oppure scegline una con il pulsante immagine della barra degli strumenti
  (PNG, JPEG, GIF o WebP, fino a 10 MB). Viene salvata insieme ai tuoi file (la scheda *Risorse*) e nel contenuto compare una
  riga come `![Mappa](resource:…)` dove si trovava il cursore; l'anteprima mostra l'immagine, e il testo tra parentesi quadre è la sua didascalia.
  Elimina la riga per togliere l'immagine dalla voce. Il modello legge la voce senza queste righe. Le immagini vengono inviate
  solo se le impostazioni del modello dicono *Invia le immagini del lore* (vedi [Fornitori di inferenza](../inference-providers.md)).
  Le immagini viaggiano con il lorebook: un clone, una copia di un lorebook condiviso, un file di lorebook esportato e un backup del libro
  con i suoi file le portano con sé, e la copia ottiene immagini proprie. L'esportazione per SillyTavern e le esportazioni dei libri le lasciano fuori. La scheda *Lorebook* della storia le mostra sotto il testo della voce. Le immagini non sono ancora conteggiate nelle stime dei token.
- **Passare da una voce all'altra.** Ctrl+clic (Cmd+clic su Mac) sul nome o su una parola chiave di un'altra voce nel contenuto,
  nel testo o nell'anteprima, per aprire quella voce nella scheda che hai usato per ultima; con anche Shift si apre in una nuova scheda. La riga in basso elenca anche, dopo **Citato in:**, le voci il cui contenuto nomina questa; cliccane una per aprirla, Shift+clic per una nuova scheda. Una voce è identificata dal suo nome e dalle parole del suo filtro (ogni parola chiave, l'intero testo di un filtro *Testo*, ciò che corrisponde a un'espressione regolare), come parole intere, maiuscole o minuscole indifferentemente. Questo è simile a ciò che la [ricorsione](entries.md) segue, ma non è lo stesso: la ricorsione trova anche una parola chiave dentro una parola più lunga, richiede tutte le parole chiave di un filtro *AND* e controlla i tag e la condizione.
- L'editor Markdown ha una barra degli strumenti (grassetto, corsivo, barrato, una linea, titoli, citazione, codice, blocco di codice, tabella e i tre tipi di elenco) e, a destra, le viste *modifica*, *modifica con anteprima* (predefinita) e *anteprima*. Funzionano i soliti tasti (Ctrl+B, Ctrl+I...); Tab passa al campo successivo invece di rientrare. Ciò che scrivi viene salvato quando smetti di scrivere per un momento e quando esci dall'editor.
- L'intestazione mostra i token stimati delle voci attivate, **Conta con i sotto-libri** e **Cerca e sostituisci**, che mostra il testo sostituito anche nelle schede aperte.

Tutte le modifiche vengono salvate mentre le fai, quindi **Chiudi** non fa perdere nulla. La finestra ricorda come l'hai lasciata, per ogni lorebook:
la prossima volta che si apre con le stesse voci nelle stesse schede, disposte allo stesso modo (le voci eliminate nel frattempo vengono escluse). La pagina del lorebook mostra di nuovo le voci quando la finestra viene chiusa.

**Modifiche fatte altrove.** Una modifica in una scheda viene salvata nella voce così come è memorizzata in quel momento, quindi ciò che è stato cambiato altrove (la pagina del lorebook, la modifica di massa, un'altra finestra) negli *altri* campi della voce resta, e la scheda lo mostra.
Se il **contenuto** o la **nota** sono stati cambiati altrove da quando la scheda li mostrava, il tuo testo non viene salvato al loro posto: la riga in basso lo segnala, con **Mantieni il mio testo** (salva comunque il tuo) e **Carica il testo salvato** (scarta il tuo). Una voce eliminata altrove chiude la sua scheda.
La finestra segue anche ciò che viene salvato mentre è aperta: quando un agente o un'altra finestra (un'altra scheda del browser, la scheda *Lorebook* della storia) salva o elimina una voce, l'elenco viene ricaricato e la scheda di quella voce la mostra com'è ora (la scheda di una voce eliminata si chiude). Una scheda il cui testo è in attesa di **Mantieni il mio testo** o **Carica il testo salvato** resta com'è finché non rispondi. Una modifica salvata nello stesso secondo in cui scrivi può ancora incrociarsi: la riga in basso chiede allora come sopra.

**Agenti.** La finestra non viene offerta mentre un agente lavora su un libro che usa il lorebook. Se una esecuzione parte mentre la finestra è aperta, la finestra diventa di sola lettura alla tua prossima modifica o quando cambi scheda: una barra indica l'agente (e dice quando la tua ultima modifica non è stata salvata). Metti in pausa o ferma l'esecuzione, poi clicca **Controlla di nuovo**: le voci vengono caricate come le ha lasciate l'esecuzione e puoi di nuovo modificare.

Le estensioni [Lore Generator](../extensions/lore-generator.md) e [Lorebook VCS](../extensions/lorebook-vcs.md) funzionano anche qui: la bacchetta è nelle righe dell'elenco e nella riga del nome di una scheda voce, il pannello delle revisioni è sopra l'elenco e la barra delle revisioni di una voce si trova nella sua scheda.

<a id="tagging-many-at-once"></a>

## Tagging di molti elementi insieme

I due pulsanti dei tag della barra degli strumenti aprono un'unica finestra che assegna un tag a molti elementi, o glielo toglie: **Tagga i lorebook** elenca i tuoi lorebook, **Tagga le voci** le voci del lorebook selezionato. Scegli il **Tag** (oppure scrivine uno nuovo e premi Invio); l'elenco mostra ogni elemento con i suoi tag, e gli elementi che hanno il tag scelto compaiono per primi, con il tag evidenziato. I pulsanti **+** e **-** di una riga applicano o rimuovono il tag per quell'elemento; seleziona più elementi (la casella nell'intestazione seleziona tutti) e usa **Applica il tag a quelli selezionati** o **Rimuovi il tag da quelli selezionati** in fondo per farlo per tutti. Un elemento su cui lavora un agente (un lorebook di un libro con un agente in esecuzione, e quindi tutte le sue voci) è in grigio e non può essere modificato finché l'esecuzione non viene messa in pausa o fermata. La finestra tagga anche i [libri](../books/managing-books.md#tagging-many-books-at-once).
I tag che mette sulle voci sono quelli ordinari, vedi [Tag](entries.md#tags); i tag negativi si impostano sulla voce stessa.

![Tagging delle voci di un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Usare un lorebook in un libro

Apri il libro e la sua scheda **Lorebook**, e scegli il lorebook nel campo **Lorebook**. L'editor sotto mostra il lorebook, così puoi aggiungere e cambiare voci mentre scrivi. Svuota il campo per scrivere senza un lorebook.

Un libro usa un solo lorebook. Per usarne più di uno, crea un lorebook che li includa come sotto-lorebook, e usa quello.

Lo stesso lorebook può essere usato da un numero qualsiasi di libri. Le modifiche ad esso hanno effetto su tutti da la prossima parte generata in poi; le parti già scritte mantengono il lore con cui sono state scritte.
<a id="sub-lorebooks"></a>

## Sotto-lorebook

**Sotto-lorebook** include le voci di altri lorebook all'interno di questo. Una configurazione tipica:

```
"Saga" (usato dai libri della serie)
 ├─ "Mondo"       luoghi, storia, regole della magia
 ├─ "Personaggi"  il cast principale
 └─ "Note libro 2"
```

- I sotto-lorebook possono avere a loro volta dei sotto-lorebook; tutti i livelli vengono inclusi.
- Un lorebook può essere sotto-lorebook di più lorebook. Quando viene raggiunto più di una volta, le sue voci vengono comunque usate una sola volta.
- Un lorebook non può includere se stesso, né direttamente né tramite altri lorebook: una scelta del genere viene rifiutata con
  *Impossibile selezionare il sotto-lorebook: rilevato un ciclo.*
- Un sotto-lorebook disattivato viene escluso insieme a tutto ciò che include.

![Un lorebook con due sotto-lorebook: i token con e senza di essi](../../images/lorebook-sub-lorebooks.png)

Le voci di tutti i lorebook inclusi vengono unite e ordinate in base al loro [ordine](entries.md#order-and-insertion),
indipendentemente dal lorebook di provenienza.

<a id="disabling-and-deleting"></a>

## Disattivazione ed eliminazione

Deseleziona **Attivata** per disattivare temporaneamente un lorebook, ad esempio per vedere come si legge un libro senza di esso. I libri continuano
a usarlo e funziona di nuovo quando lo riselezioni.

**Elimina lorebook** rimuove il lorebook dalle tue liste. I libri che lo usavano non ricevono più le sue voci, e i lorebook che lo includevano come sotto-lorebook lo ignorano. I dati restano nel database finché un amministratore non esegue [Pulizia](../administration/cleanup.md), e fino ad allora puoi ripristinare il lorebook dal [Cestino](../trash.md); le copie presenti nei [backup dei libri](../books/backups.md) e nei file esportati non sono interessate.
