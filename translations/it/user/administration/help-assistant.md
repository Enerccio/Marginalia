---
label: Assistente di aiuto
order: 60
---

# Assistente di aiuto

L'assistente di aiuto è una chat che risponde alle domande su Marginalia basandosi sul suo manuale. Un amministratore sceglie quale
modello usa e concede il permesso **Assistente di aiuto** agli utenti che possono farle domande, nei loro
[permessi](permissions.md) (gli amministratori possono sempre usarlo); non serve che abbiano un modello proprio. Gli utenti che esistevano
quando i permessi sono stati introdotti lo hanno ricevuto se l'assistente era attivo in quel momento; i nuovi utenti non ce l'hanno.

Quando un utente può usarlo, vede in basso a destra della finestra un pulsante rotondo con un robot. Il pulsante apre la chat; il
pannello indica quale modello risponde e avvisa quando si applica il limite dell'utente (vedi [Chi paga](#who-pays)). Il modello cerca
le risposte nel manuale (può cercarvi e leggerne le pagine), indica le pagine usate come link al
[manuale pubblicato](https://enerccio.github.io/Marginalia/) (si aprono in una nuova scheda) e spiega: non
modifica nulla e non vede i libri dell'utente, a meno che l'utente non lo consenta (vedi [I tuoi libri](#your-books)). Può anche verificare cosa è andato storto quando qualcosa fallisce (vedi
[Quando qualcosa va storto](#when-something-goes-wrong)) e portare l'utente in un punto preciso, se lo consenti (vedi
[Indicare la strada](#showing-the-way)). La conversazione viene mantenuta fino al logout dell'utente (chiudere il pannello
o ricaricare la pagina non la cancella); il pulsante della scopa la azzera. Il manuale viene letto nella lingua dell'interfaccia dell'utente, quando il manuale è tradotto in quella lingua, e i link portano al manuale in quella lingua; una pagina non tradotta viene letta in inglese.

![L'assistente di aiuto che risponde a una domanda](../../images/help-assistant.png)

Il pulsante compare quando un utente apre Marginalia, e il modello e l'avviso sul limite vengono letti ogni volta che il pannello
viene aperto; se togli il permesso o rimuovi il modello, gli utenti che hanno già la pagina aperta perdono il pulsante quando la ricaricano
(o quando aprono il pannello).

<a id="settings"></a>

## Impostazioni

Apri **Admin → Assistente di aiuto**:

![La scheda Assistente di aiuto](../../images/admin-help-assistant.png)

| Impostazione | |
|---|---|
| **Modello** | Uno dei *tuoi* [modelli](../inference-providers.md), condiviso o no con gli utenti. I modelli che altri amministratori condividono non compaiono nell'elenco. |
| **Consenti di leggere i dati di amministrazione** | Permette all'assistente di leggere lo stato delle estensioni e i loro log, i backup del database e gli altri dati della pagina Admin *per un amministratore che lo chiede*, vedi [Dati di amministrazione](#administration-data). Disattivato per impostazione predefinita. |
| **Prompt per gli amministratori**, **Prompt per gli altri utenti** | Le istruzioni che il modello riceve con ogni domanda; vedi [Prompt](#prompts). |

Due impostazioni dipendono dall'utente e si trovano nel suo permesso **Assistente di aiuto** [permessi](permissions.md#settings-of-a-permission) (aprilo con il
pulsante della chiave in **Admin → Utenti**; entrambe sono disattivate per impostazione predefinita):

| Impostazione del permesso | |
|---|---|
| **Applica i limiti** | Solo per un modello condiviso, vedi [Chi paga](#who-pays). |
| **Consenti di navigare nell'interfaccia** | Permette all'assistente di aprire una sezione, selezionare una scheda o aprire la finestra per aggiungere qualcosa quando un utente gli chiede di mostrargli dove si trova qualcosa, vedi [Indicare la strada](#showing-the-way). |

Un amministratore che non ha tale permesso riceve risposte con entrambe disattivate.

Le impostazioni si salvano con **Salva**. L'assistente chiede al modello senza un [protocollo](../protocols.md), quindi valgono le impostazioni del modello stesso. Smette di funzionare da solo quando il modello viene eliminato,
o quando il suo proprietario non è più un amministratore, così un modello che appartiene a un utente comune non viene mai messo a disposizione
di tutti.

<a id="what-the-assistant-sees-of-the-screen"></a>

## Cosa vede l'assistente dello schermo

Il modello può vedere cosa sta facendo l'utente in questo momento (gli viene detto quando una domanda riguarda "qui" o "questo"): quale
sezione, scheda o finestra è aperta, come breve descrizione di ogni parte dello schermo - per esempio il titolo del
libro aperto e la sua scheda aperta, o il titolo di una finestra aperta. Le schede che non sono aperte vengono solo elencate, e vengono lette
solo quando la domanda lo richiede. Per un modulo aperto riceve anche ciò che non va in quel momento: i campi che il modulo segnala
come non validi con il messaggio che mostra, i campi obbligatori vuoti e ciò che il modulo verifica tra più campi (per un modello: la risposta deve essere più piccola
del contesto). Riceve solo i *nomi* dei campi, mai ciò che è scritto in essi. Non riceve mai il testo di una storia, una voce del lorebook, una chiave API o una password.

![L'assistente che spiega perché il modulo di un nuovo modello non può ancora essere salvato](../../images/help-assistant-form.png)

<a id="your-books"></a>

## I tuoi libri

Il pannello dell'assistente ha la casella **Lascia che l'assistente guardi i miei libri**, disattivata per impostazione predefinita e impostata da ciascun utente
per sé stesso (viene salvata nelle sue impostazioni). Un avviso sotto di essa spiega cosa significa: l'assistente legge i nomi delle cartelle, i titoli,
le descrizioni e le impostazioni di scrittura dei libri e cerca nel loro testo, non legge mai il testo di una storia, e ciò che legge viene
inviato all'IA che risponde - cioè il modello che hai scelto sopra, quindi dillo agli utenti se si tratta di un servizio al di fuori del tuo controllo.

![Il pannello dell'assistente con la casella per i libri](../../images/help-assistant-books.png)

Quando è attiva, il modello riceve tre funzioni, tutte in sola lettura e solo per i libri dell'utente che chiede:

| Funzione | |
|---|---|
| `list_folder` | Le sottocartelle di una cartella, con il numero di libri che contengono, e i suoi libri con titolo e descrizione. Un percorso come `Sci-fi/Expanse` indica la cartella, nessun percorso indica il livello più alto. |
| `search_books` | Trova libri in base alle parole del titolo o della descrizione, oppure in base al testo della storia, eventualmente all'interno di una cartella ("dov'è il mio libro sugli amanti veri?"). Indica dove c'è stata una corrispondenza e in quale cartella; il testo trovato non viene restituito. |
| `get_book_settings` | La lingua, il punto di vista, il tempo verbale e lo stile di un libro e da dove proviene ciascuno (il libro, una cartella, le impostazioni dell'utente o il valore predefinito integrato). I prompt vengono indicati solo come *un testo personalizzato* o *il testo predefinito integrato*, con la loro provenienza. |

Non può modificare, spostare o creare nulla. Gli elenchi sono tagliati (cento voci, venti corrispondenze) e le descrizioni accorciate.

<a id="when-something-goes-wrong"></a>

## Quando qualcosa va storto

Quando un utente dice che qualcosa non ha funzionato ("ottengo un errore", "non salva", "il modello non risponde"), l'
assistente può leggere ciò che Marginalia ha mostrato all'utente: gli ultimi errori, le richieste fallite al fornitore del modello (chiave sbagliata, modello inesistente,
limite raggiunto, timeout...), gli avvisi e i messaggi dei moduli che non è stato possibile salvare, dal più recente e con indicazione di quanto tempo fa
è avvenuto ciascuno. Spiega cosa significa il messaggio e cosa correggere, e per un modulo aperto indica quali campi cambiare.

Cosa viene registrato, e dove:

* tutto ciò che un utente vede come errore o avviso in una notifica, e il messaggio di una convalida fallita, viene conservato
  per la sessione (gli ultimi 20);
* gli errori imprevisti (quelli con la finestra della traccia dello stack) e le richieste fallite a un fornitore vengono conservati anche dalla scheda del browser
  (gli ultimi tre, nel suo archivio di sessione), così restano disponibili dopo il ricaricamento della pagina o la scadenza della sessione.

L'assistente riceve il testo che l'utente ha visto e, per un errore, i nomi e i messaggi delle eccezioni, senza la traccia dello stack.
Ogni testo viene tagliato a 400 caratteri e ciò che sembra una chiave, un token o una password viene rimosso, ma il testo di un messaggio
può comunque contenere una parte di ciò che un fornitore o il sistema ha risposto, per esempio la risposta di un fornitore a una richiesta. Il modello che risponde è quello che hai scelto, che potrebbe
essere un fornitore esterno: tienilo presente quando lo [scegli](#settings).
<a id="showing-the-way"></a>

## Mostrare la strada

Con **Consenti di navigare nell'interfaccia** attivo nel permesso dell'utente, l'assistente può fare alcune cose nella finestra stessa quando un utente lo chiede
("mostrami dove sono i lorebook", "portami ai backup", "come aggiungo un modello?"): aprire una sezione (Libri, Lorebook, Impostazioni, ...), la pagina Admin
(la ha solo l'amministratore), selezionare una scheda delle Impostazioni, della pagina Admin o dell'editor della storia, oppure premere il pulsante che
apre la finestra per aggiungere un libro, un modello o un protocollo (la finestra si limita ad aprirsi: non si crea nulla finché l'utente non la compila e non conferma; non viene proposta mentre un'altra finestra è aperta). Riferisce all'utente che cosa ha fatto.
Non può fare altro: non preme altri pulsanti, non scrive, non salva, non elimina, non genera, non chiude una finestra e non esce dall'account, e un utente che non può aprire qualcosa non può far aprire a l'assistente al posto suo. Non si sposta di propria iniziativa, ma solo quando gli viene chiesto.
Dove la navigazione è bloccata (la sezione aperta ha modifiche non salvate, un libro è in fase di generazione) non può spostarsi.

![L'assistente ha aperto la scheda Backup del database su richiesta](../../images/help-assistant-navigate.png)

Consentirlo comunica al modello anche i nomi degli elementi presenti sullo schermo e ciò che offrono; è lo stesso tipo di informazione di [Che cosa l'assistente vede dello schermo](#what-the-assistant-sees-of-the-screen).

<a id="administration-data"></a>

## Dati di amministrazione

Con **Consenti di leggere i dati di amministrazione** attivo, l'assistente può leggere i dati della pagina Admin per un amministratore che lo chiede
("perché Side Query non funziona?", "quando è stato fatto l'ultimo backup?"), e per nessun altro; un utente normale
non li ottiene mai, qualunque sia l'impostazione, e il controllo viene ripetuto ogni volta che vengono letti.

| Argomento | Che cosa legge |
|---|---|
| Estensioni | Ogni [estensione](../extensions/index.md) installata con la sua versione, se è attiva, se è stata fermata da te o se non è riuscita ad avviarsi, e il risultato della sua verifica |
| Il log di un'estensione | Le ultime righe del suo log (40 per impostazione predefinita, 200 al massimo), come **Admin → Estensioni → Log** |
| Utenti | Quanti utenti ci sono, quanti amministratori e quanti sono bloccati; nessun nome |
| Backup del database | La dimensione del database, quanti backup ci sono e quando è stato creato il più recente, la pianificazione, se un ripristino è in attesa |
| Pulizia | Che cosa rimuoverebbe una [pulizia](cleanup.md) (non rimuove nulla) e che cosa la blocca |
| Applicazione | La versione di Java, la memoria in uso e da quanto tempo l'applicazione è in esecuzione |

È di sola lettura: l'assistente non può avviare, fermare o rimuovere nulla. Tieni presente che tutto questo viene inviato al modello che
risponde, che potrebbe essere un fornitore esterno, e che il log di un'estensione può contenere testo scritto dall'estensione stessa (una
riga di log viene troncata a 400 caratteri e chiavi, token e password vengono rimossi, ma non viene fatto altro). Per questo l'impostazione è disattivata finché non la attivi.

<a id="prompts"></a>

## Prompt

Ci sono due prompt: uno per gli amministratori (che possono chiedere della pagina Admin) e uno per tutti gli altri (ai quali viene
detto che l'amministrazione è riservata agli amministratori). Entrambi sono compilati con i valori predefiniti; modifica il testo e salva, **Ripristina
il predefinito** rimette quello predefinito. Un prompt vuoto o uguale al predefinito viene memorizzato come "il predefinito", quindi
migliora con una nuova versione di Marginalia; uno che hai modificato resta tuo, e sta a te tenerlo aggiornato.

`{{manualIndex}}` in un prompt viene sostituito con l'elenco delle pagine del manuale (percorso, titolo e argomento di ciascuna pagina), ed è così che il modello
sa che cosa può leggere. Se lo ometti, il modello trova le pagine solo tramite la ricerca. Mantieni le istruzioni che dicono di cercare le informazioni
nel manuale e di dirlo quando il manuale non copre una domanda, altrimenti le risposte diventano meno affidabili. Le regole
per gli strumenti che leggono gli errori, si spostano nell'interfaccia e leggono i dati di amministrazione vengono aggiunte al prompt da
Marginalia (non devi conservarle tu), e solo per gli strumenti consentiti.

<a id="who-pays"></a>

## Chi paga

Gli utenti non vedono mai il modello né la sua chiave API; le loro domande passano attraverso l'applicazione.

| Modello | **Applica i limiti** (permesso dell'utente) | Che cosa succede |
|---|---|---|
| Condiviso | attivo | Una domanda è una richiesta come le altre al modello condiviso: viene verificato il [limite](../inference-providers.md#limits-of-shared-providers) che hai impostato per l'utente e i token vengono aggiunti al consumo dell'utente. Un utente che ha raggiunto il limite (o che non può usare il modello) non riceve risposta. |
| Condiviso | disattivo | Paga il proprietario del modello. Il limite dell'utente viene ignorato e per l'utente non viene conteggiato nulla. |
| Non condiviso | (nessun effetto) | Paga il proprietario del modello: nessun limite, nulla conteggiato per l'utente. Il modello è usato solo dall'assistente. |

Anche le tue domande vengono conteggiate allo stesso modo: per te il modello è semplicemente un altro modello.
