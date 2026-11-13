---
label: Editor della storia
order: 90
---

# Editor della storia

La scheda **Storia** di un libro è dove si scrive. Mostra le parti del [ramo attivo](branches-and-story-tree.md)
dall'inizio alla fine.

![L'editor della storia](../../images/hero-story-editor.png)

Questa pagina descrive il layout per desktop. Su telefono la storia è una sola colonna con la struttura in un cassetto, vedi
[Usare Marginalia sul telefono](../phone.md#the-story).

| Area | |
|---|---|
| **Struttura** (a sinistra) | Una voce per parte, `#1`, `#2`... con l'ID della parte nel database. Clicca una voce per scorrere alla parte (le parti precedenti vengono caricate per prime se non sono ancora caricate). L'intestazione mostra quante parti ha il ramo. Una parte che ha un [riassunto](summaries.md) ha una piccola icona di documento dopo la sua voce. L'estensione [Chapter Marker](../extensions/chapter-marker.md) mostra qui i titoli dei capitoli. |
| **Lorebook** (a sinistra, accanto alla Struttura) | Una vista del lore del libro, con una casella di ricerca e una modifica rapida del testo di una voce, vedi [La scheda Lorebook](#the-lorebook-tab). |
| **Annotazioni** (a sinistra, accanto alla Struttura) | Le [note](annotations.md) sui passaggi della storia, per l'intero libro. |
| **Pianificatore** (a sinistra, accanto alla Struttura) | Le scene che prevedi di scrivere, un elenco che compili a mano, vedi [La scheda Pianificatore](#the-planner-tab). |
| **Storia** (al centro) | Il testo di ogni parte, con il suo menu (☰) e i suoi metadati a destra, e sotto la barra inferiore. |
| **Barra inferiore** | Il pulsante **Riassunti** (icona di elenco) e il menu delle impostazioni (⚙) a sinistra, la riga di comando e il pulsante **+** a destra. |

Così appare un libro nuovo. Tutte queste sono schede che puoi disporre come preferisci, compresa la storia, vedi
[Disporre le schede](#arranging-the-tabs). Il divisore tra la barra laterale e la storia può essere trascinato, e Marginalia
ricorda dove l'hai lasciato (salvato con il libro) e vi torna quando riapri il libro.

La storia si apre sempre in fondo, alla parte più recente. Un libro lungo non viene disegnato tutto in una volta: vengono caricate solo le ultime parti, e quelle precedenti si aggiungono sopra man mano che scorri verso l'alto, senza spostare la parte che stai leggendo. Le parti all'altro estremo vengono rimosse, così restano caricate non più di quante ne indica l'impostazione **Parti caricate insieme nella storia** del tuo [account](../account.md) (se è vuota le carica tutte). Se scorri più velocemente di quanto arrivino le parti, un segno *Caricamento di parti precedenti* (o *successive*) al bordo indica che ce ne sono altre. Cliccare una parte nella struttura, o trovarne una con la ricerca, carica le parti attorno ad essa, e iniziare una nuova parte ti riporta alla fine della storia.

<a id="searching-the-story"></a>

### Cercare nella storia

Sopra la struttura c'è un campo di ricerca per il testo del **ramo su cui ti trovi** (la ricerca dell'albero della storia cerca in tutti i rami). Poiché un libro lungo non viene caricato tutto insieme, la ricerca del browser (Ctrl+F) non trova testo nelle parti non caricate; questa sì.

- Scrivi ciò che cerchi; la ricerca parte quando smetti di scrivere. Più parole devono essere tutte nella stessa parte, una frase va tra virgolette doppie (`"vecchia chiave"`) e `*` sta per un qualsiasi insieme di caratteri (`luce*faro`). Non conta maiuscole o minuscole.
- Ogni punto in cui compare una parola è una corrispondenza, e sono numerate in tutto il ramo. Sotto il campo vedi *Corrispondenza 3 di 27, nella parte #5* e una barra con a che punto sei. La storia scorre fino alla corrispondenza e carica le parti attorno ad essa quando non sono caricate.
- I pulsanti **su** e **giù** vanno alla corrispondenza precedente e a quella successiva (dopo l'ultima torna alla prima). **Invio** nel campo fa lo stesso del pulsante giù, **Maiusc+Invio** come il pulsante su.
- Tutte le corrispondenze nelle parti caricate sono evidenziate nel testo, quella su cui sei con un colore più intenso. Svuota il campo per rimuovere l'evidenziazione.

Sul telefono la ricerca è nel cassetto della struttura; il cassetto si chiude quando mostra una corrispondenza, riaprilo per la successiva.

<a id="the-lorebook-tab"></a>

### La scheda Lorebook

La scheda **Lorebook** della barra laterale mostra il lore del lorebook del libro senza lasciare la storia, così non devi cambiare scheda per controllare cosa dice una voce, o per correggere una parola in essa. Viene riletta ogni volta che torni alla scheda *Storia*, quindi le modifiche fatte nella scheda *Lorebook* del libro compaiono subito.

- La **casella di ricerca** in alto mostra solo le voci che hanno il testo nel loro nome, contenuto, nota o filtro (non conta maiuscole o minuscole).
- La **penna** di una voce apre una piccola finestra con il suo nome, se è usata, il filtro e come viene cercata, il contenuto e la nota; **Salva** li scrive (i tag, l'ordine e le impostazioni di attivazione si cambiano nella scheda *Lorebook* del libro). Non si può cambiare nulla mentre un agente lavora sul lorebook.
- Il pulsante **espandi** (⤢) apre l'[editor del lorebook a schermo intero](../lorebooks/lorebooks.md#editing-in-full-screen) (solo desktop): quello accanto alla casella di ricerca sul lorebook, quello di una voce su quella voce, quello di un sotto-lorebook (con *Includi i sotto-lorebook*) su quel lorebook. La scheda viene ricaricata quando chiudi l'editor.

- Ogni voce è una riga con il suo nome e la penna; la freccia la apre per mostrarne il **contenuto** (reso come Markdown) e il **testo che la attiva** (il *Testo/pattern di filtro*, con una nota quando è un'espressione regolare).
- **Includi i sotto-lorebook** mostra il lorebook e ciascuno dei suoi sotto-lorebook (e i loro, a qualsiasi profondità) come righe a sé, con le loro voci sotto; clicca la freccia di un lorebook per nascondere le sue voci. Senza di esso vengono elencate solo le voci del lorebook del libro.
- Le voci e i lorebook disattivati (non vengono usati per la storia) sono mostrati attenuati.
- Un libro senza lorebook mostra invece una nota.

![La scheda Lorebook con due voci aperte](../../images/story-lorebook-tab.png)

Come ogni scheda dell'editor, può essere trascinata fuori in un pannello a sé, vedi sotto.
<a id="the-planner-tab"></a>

### La scheda Pianificatore

La scheda **Pianificatore** è un elenco delle scene che vuoi che la storia attraversi, nell'ordine che desideri. Ogni scena contiene i dettagli di un turno che
la scrive: un **titolo** per l'elenco, l'**ambientazione della scena**, il **personaggio POV**, i **personaggi presenti** e le **istruzioni**, gli stessi
quattro campi del modulo per il nuovo turno.

![La scheda Pianificatore con quattro scene, la prima già scritta](../../images/planner-tab.png)

- **Aggiungi scena** apre un modulo per una nuova scena; la scena finisce in fondo all'elenco. La penna modifica una scena, le frecce la spostano su o giù, il cestino la elimina.
- Il **+** nella riga di una scena **inserisce una nuova scena** subito sotto di essa, tra quella scena e la successiva.
- Il pulsante di unione di una scena la **unisce con la successiva**: si apre un modulo con i dettagli di entrambe già congiunti (i due titoli, le ambientazioni e le istruzioni una sotto l'altra, i personaggi presenti di entrambe, il personaggio POV della prima; quando la seconda scena ne ha un altro, il modulo lo segnala). Modifica ciò che non va bene e premi **Unisci** (oppure <kbd>Ctrl/Cmd+Invio</kbd>): le due scene diventano una sola, al posto della prima. L'operazione è completata solo se entrambe erano state scritte.
- **Importa dal ramo** aggiunge una scena per ogni parte del ramo su cui si trova il libro (dalla prima parte fino alla più recente) che ha dettagli di turno: la sua ambientazione, il personaggio POV, i personaggi presenti e le istruzioni, con la prima riga delle istruzioni come titolo. Le parti senza dettagli di turno vengono escluse, e le scene si aggiungono dopo quelle che hai già, non scritte. Usalo per pianificare la riscrittura di una storia che hai già scritto: importa, unisci i turni che appartengono insieme, inserisci ciò che manca, poi scrivi le scene su un nuovo ramo o in una copia del libro.
- La casella davanti a una scena indica che è **fatta** (scritta): il titolo è barrato e la scena appare attenuata. Nulla lo verifica al posto tuo, spuntala quando hai scritto la scena. La riga sopra l'elenco conta quante scene sono fatte.
- **Clicca una scena** per inserire i suoi dettagli nel **modulo del nuovo turno** (il modulo si apre con l'ambientazione, il POV, i personaggi presenti e le istruzioni della scena; ciò che c'era prima viene sostituito) e premi **Genera** quando sei soddisfatto. Sul telefono il cassetto si chiude prima.
- Le scene sono salvate insieme al libro, quindi fanno parte dei suoi backup e delle sue copie. Anche un modello può aggiungere scene: la [procedura guidata della trama](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) dell'estensione Arc Outline e un [agente](../agents.md) possono leggere e modificare il pianificatore con i [comandi](../commands/index.md#the-planner); l'elenco mostra subito ciò che hanno aggiunto.

![Il modulo per una nuova scena](../../images/planner-scene-dialog.png)

![Un clic su una scena riempie il modulo del nuovo turno con i suoi dettagli](../../images/planner-new-turn.png)

Come ogni scheda dell'editor, può essere trascinata fuori in un pannello separato, vedi sotto.

<a id="arranging-the-tabs"></a>

### Disposizione delle schede

L'editor è composto da schede: la **Storia** stessa (le parti e la barra inferiore sotto di esse), la **Struttura**, il **Lorebook**, e le schede che aggiungono le [estensioni](../extensions/index.md) (Personaggi, Archi, Side Query...). Trascina una scheda dalla sua intestazione per riordinarle
(solo desktop; il telefono mantiene una colonna con la struttura in un cassetto). Un libro nuovo ha la Struttura e il Lorebook in un pannello a sinistra e la Storia in una colonna propria a destra; puoi mettere la storia dove vuoi, per esempio al centro con la struttura da un lato e il lore dall'altro, oppure sopra la struttura:

- Rilascia la scheda su una delle strisce tratteggiate che compaiono sopra, tra e sotto i pannelli di una colonna per **staccarla** in un pannello proprio in quella colonna; i pannelli di una colonna sono impilati uno sotto l'altro.
- Rilascia la scheda su una delle strisce alte a sinistra, a destra o tra le colonne per **staccarla lateralmente**: ottiene una colonna propria, accanto alle altre. Allarga l'editor con i divisori per fare spazio a ulteriori colonne.

I pannelli e le colonne sono separati da divisori: trascinali per dare a ogni pannello lo spazio che ti serve. Le dimensioni vengono salvate con la disposizione.

![La barra laterale con Personaggi in una colonna propria e Side Query sotto](../../images/story-editor-tabs.png)

- Rilascia la scheda sull'intestazione di un'altra scheda o sul corpo di un altro pannello per **unirla** a quel pannello (rilasciando su una scheda la inserisce prima di essa, rilasciando sul corpo la mette per ultima). Un pannello che perde la sua ultima scheda scompare, e così una colonna senza pannelli.

La disposizione e le dimensioni vengono salvate con il libro, quindi sono le stesse la volta successiva che lo apri e in ogni browser; la scheda mostrata da ciascun pannello viene ricordata insieme al resto del tuo spazio di lavoro. Una scheda di un'estensione non caricata mantiene il suo posto finché l'estensione non torna disponibile.

La storia è sempre presente da qualche parte: la sua scheda non può essere chiusa, e una scheda che sta in un pannello insieme ad altre resta nascosta mentre ne è selezionata un'altra. Qualsiasi cosa mostri una parte riporta in primo piano la storia: cliccare una voce della struttura, un risultato della ricerca, scegliere un ramo nell'[albero della storia](branches-and-story-tree.md) e avviare una generazione selezionano prima la scheda **Storia**. Mentre una generazione prepara il suo prompt (di solito pochi secondi) le altre schede sono disabilitate; una volta che il modello scrive, tornano utilizzabili, vedi [sotto](#while-a-part-is-generated).
<a id="minimizing-tabs"></a>

### Minimizzare le schede

Ogni scheda tranne quella della storia ha un piccolo pulsante **⌄** (freccia a V) accanto al nome. Serve a **minimizzare** la scheda: la scheda esce dal suo pannello (un pannello
con tutte le schede minimizzate scompare per il momento) e un'icona rotonda che la rappresenta, un cerchio colorato con un disegno (un elenco per
la scaletta, un libro per il lorebook, un fumetto per le annotazioni, una lente per Side Query e così via; una scheda di una
estensione che non ha un disegno ne riceve una o due lettere), compare in una barra sul bordo dell'editor. Passa il mouse sull'icona per vedere di quale scheda si tratta. Non si chiude nulla: la scheda conserva ciò che stava mostrando. Fai clic sull'icona e la scheda torna nel pannello, nel posto tra le schede che occupava, e viene mostrata.

La barra c'è finché una scheda è minimizzata. Il pulsante in alto (le frecce circolari) sposta la barra sul lato successivo dell'editor, in senso orario: sinistra, alto, destra, basso. La storia non può essere minimizzata, quindi sullo schermo c'è sempre qualcosa.

![Schede minimizzate nella barra sul bordo dell'editor](../../images/story-minimized-tabs.png)

Le icone seguono l'ordine alfabetico dei nomi delle schede finché non lo cambi: trascina un'icona sopra un'altra e prende il posto di quella. Quali schede sono minimizzate, dove si trova la barra e l'ordine delle icone vengono salvati con il libro, insieme alla disposizione e alle dimensioni.

<a id="the-default-arrangement"></a>

### La disposizione predefinita

Un nuovo libro si apre con la disposizione standard (la scaletta, il lorebook e le annotazioni a sinistra, la storia a destra). Per rendere standard la tua disposizione, imposta le schede come preferisci (pannelli, dimensioni, schede minimizzate, lato della barra) e scegli **Salva il layout come mio predefinito** nel menu delle impostazioni (⚙) della storia. Ogni libro che non ha una disposizione propria (un libro che non hai riorganizzato) si aprirà poi con quella; un libro che hai organizzato mantiene la sua disposizione. Il predefinito è conservato nelle impostazioni utente.

Per tornare alla disposizione predefinita, scegli **Ripristina il layout predefinito** nel menu delle impostazioni (⚙) della storia e conferma. È quella che hai salvato, oppure quella standard se non ne hai salvata nessuna. Sposta soltanto le schede (e dimentica le dimensioni e ciò che è minimizzato): i tuoi testi, la scheda che ogni pannello mostra e le schede delle estensioni restano, queste ultime nel primo pannello.
<a id="writing-the-next-part"></a>

## Scrivere la parte successiva

1. Clicca su **+** nell'angolo in basso a destra. Si apre il pannello *Istruzioni del nuovo turno*.
2. Compila i campi. Sono tutti facoltativi, ma il modello ha bisogno di almeno qualche indicazione:

   | Campo | |
   |---|---|
   | **Ambientazione della scena** | Dove e quando si svolge la parte, ad esempio *Il faro, una notte di tempesta alla fine dell'autunno*. |
   | **Personaggio POV** | Dal punto di vista di chi è scritta la parte. |
   | **Personaggi presenti** | Chi c'è nella scena, e in che condizioni. |
   | **Istruzioni** | Cosa deve succedere in questa parte. |

   *Ambientazione della scena*, *Personaggio POV* e *Personaggi presenti* vengono compilati a partire dall'ultima parte, quindi modifichi
   solo ciò che è diverso. *Istruzioni* parte ogni volta vuoto.
3. Clicca su **Genera**.

![Il pannello Istruzioni del nuovo turno](../../images/new-turn-instructions.png)

La nuova parte compare in fondo e si riempie man mano che il modello la scrive. Per i [modelli di ragionamento](../inference-providers.md#reasoning-models)
il ragionamento appare prima, sotto **Mostra il ragionamento**.

Mentre una parte è in generazione, il pulsante **+** diventa un pulsante rosso **Interrompi**. Interrompere mantiene come parte il testo
scritto fino a quel momento.

<a id="while-a-part-is-generated"></a>

### Mentre una parte viene generata

Per i primi secondi, mentre il prompt viene composto a partire dal libro, l'intera finestra del libro resta in attesa. Non appena il modello
inizia a scrivere, vengono bloccate solo la nuova parte e le parti che la precedono:

- **La nuova parte** non può essere toccata finché non è terminata: niente modifiche, niente menu.
- **Le parti precedenti** restano dove sono: puoi modificarle, annotarle e riscriverle, ma non eliminarle, unirle o diramarle, e la
  storia resta sul suo ramo (niente swipe, niente cambio di ramo). Una modifica a una parte precedente, o al lore, non raggiunge la parte
  in scrittura: il suo prompt era già stato composto. Vale per ciò che viene dopo.
- **Tutto il resto è libero**: il lore, le impostazioni e il prompt del libro, le schede della barra a sinistra (query laterale, schema
  dell'arco, note...), la barra dei comandi. Una richiesta al modello attende una connessione libera del modello se la generazione ne usa
  l'unica disponibile.
- **In attesa**: un secondo turno, la ricerca e sostituzione, un importo nel libro e l'eliminazione di massa. Anche le schede dell'albero e
  del backup attendono.

Ciò che la finestra oscura viene rifiutato anche quando un comando, un agente o un'estensione lo richiede: Marginalia indica cosa
blocca (*Questo è in uso: …*).

**Uscire dal libro interrompe la generazione.** Il pulsante di uscita chiede conferma; *Sì* equivale a **Interrompi**: quanto è arrivato
viene conservato. Chiudere la scheda del browser fa lo stesso, e il browser chiede conferma prima di chiudere o ricaricare la scheda
mentre una parte è in generazione. Una breve perdita della rete, o un portatile chiuso per un minuto, non sono un'uscita: la pagina
torna e mostra ciò che nel frattempo è arrivato.

Quando il modello restituisce un errore (server non raggiungibile, chiave API errata, contesto troppo lungo...), l'errore viene mostrato.
Se non è ancora arrivato alcun testo, la storia resta com'era: una nuova parte o uno swipe viene rimosso, e una parte rigenerata
riottiene il testo precedente. Il testo arrivato prima dell'errore viene conservato, come con **Interrompi**. Risolvi il problema e
genera di nuovo.

Il modo in cui i campi entrano nel prompt è stabilito dal [Prompt utente](prompts.md#user-prompt) del libro. Con il prompt utente
predefinito, il modello li riceve come *Current Location & Time*, *Point of View Character*,
*Characters Currently Present / State* e *Plot Specifications for the Next Part*.

!!!
Il testo è scritto in Markdown: `*corsivo*`, `**grassetto**`, `# Titolo` e così via vengono mostrati formattati. Una parte con un titolo
`#` è un capitolo per l'estensione [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Abbozzare le istruzioni

Non sai cosa dovrebbe succedere dopo? **Abbozza le istruzioni** sotto il campo *Istruzioni* del pannello fa scrivere le istruzioni al modello al
tuo posto, come le scriveresti tu. Si apre una finestra con:

| | |
|---|---|
| **Indicazioni** | Di cosa dovrebbe parlare la parte: un'idea di massima, una direzione ("lei lo affronta per la lettera"), oppure niente, e il modello prosegue la storia dove lo porta. All'inizio contiene ciò che hai già scritto in *Istruzioni*. |
| **Piano della storia** | Solo quando qualcosa ne fornisce uno. Lo fa lo [Schema dell'arco](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter), con i dati del capitolo corrente. Sola lettura; il modello lo segue. |

**Abbozza le istruzioni** avvia il modello e trasmette la risposta in **Istruzioni abbozzate**, che puoi modificare o svuotare
prima di proseguire; **Abbozza di nuovo** ci riprova una volta, **Interrompi** conserva ciò che è arrivato. Non cambia nulla finché non clicchi
**Usa come istruzioni**: il testo sostituisce allora *Istruzioni* del pannello (gli altri campi non vengono toccati) e il pannello si riapre,
pronto per **Genera**.

Il modello riceve la storia finora (la parte più vecchia come [riassunti](summaries.md), come in una generazione), l'ambientazione della scena,
il POV e i personaggi presenti che sono nel pannello, il lore dell'ultima parte, le tue ultime istruzioni come esempi di come le scrivi, le
tue indicazioni e il piano. Non vede le voci del lorebook che un nuovo turno attiverebbe. La richiesta usa il
[prompt di impersonificazione](prompts.md#impersonate-prompt). Il modello è quello del libro; abbozzare consuma token come qualsiasi richiesta.

![Abbozzo delle istruzioni del turno successivo](../../images/impersonate-dialog.png)

Lo stesso fa il comando [`/impersonate`](../commands/index.md), che restituisce il testo. Gli [agenti](../agents.md) non lo ricevono: scrivono
la storia con istruzioni proprie.
<a id="the-settings-menu"></a>

## Il menu delle impostazioni

Il menu delle impostazioni (⚙) nella barra inferiore raggruppa le funzioni che agiscono sull'intera storia. **Cerca e sostituisci** è in cima; il resto si trova nei sottomenu
(anche sul telefono, tranne le voci solo per desktop):

| Sottomenu | Voci |
|---|---|
| **Aspetto** | **Cambia gli stili del testo**, **Evidenzia il testo generato** e **Annotazioni a margine** (solo desktop, l'ultima) |
| **Analisi** | **Annota un intervallo di parti con l'IA**, **Crea riassunti**, **Prendi appunti sul lore con un agente**, **Annotazione automatica**, **Token del ramo**, **Utilizzo del lore**, **Statistiche** |
| **Importazione ed esportazione** | **Importa**, **Esporta la storia** (e le voci delle estensioni che importano o esportano, come **Esporta istruzioni**) |
| **Pulizia** | **Cancella la cronologia del libro**, **Svuota il cestino della cronologia**, **Eliminazione di massa** |
| **Layout** (solo desktop) | **Salva il layout come mio predefinito**, **Ripristina il layout predefinito** |

Le voci portate dalle [estensioni](../extensions/index.md) si trovano nel sottomenu che le riguarda, oppure in fondo al menu. I tasti delle voci sono in
*Impostazioni → Scorciatoie da tastiera*, dove una voce è indicata con il suo sottomenu (*Analisi › Statistiche*). Le sezioni seguenti indicano dove si trova ogni voce.

<a id="the-part-menu"></a>

## Il menu della parte

Ogni parte ha un menu (☰) nell'angolo in alto a destra. **Modifica**, **Scarta le modifiche**, **Rigenera**, **Scambia**, **Riscrittura**, **Cronologia** e **Immagini** si trovano
nel menu stesso, **Elimina** ne è l'ultima voce, e il resto è in quattro sottomenu: **Annotazioni** (*Annota la parte*, *Annota con l'IA*), **Riassunto**
(*Genera riassunto* / *Mostra il riassunto*, *Elimina riassunto*), **Ispeziona** (*Mostra il prompt*, *Confronta il prompt*, *Utilizzo dei token*, *Utilizzo del lore*) e **Rami e unione** (*Dirama la storia*,
*Unisci alla parte precedente*). Un sottomenu senza voci per la parte non viene mostrato. Il Revisore aggiunge il suo sottomenu **Revisione** dopo *Riassunto*. Le voci:

| Voce | |
|---|---|
| **Modifica** | Rende il testo un campo modificabile. **Salva** (la stessa voce di menu) lo salva. Avviare una generazione o lasciare la scheda salva anche le modifiche aperte. |
| **Scarta le modifiche** | Solo mentre il testo è in modifica: chiude il campo senza salvare, la parte mantiene il testo con cui era stata salvata l'ultima volta. Se hai cambiato il testo, ti viene chiesta conferma. |
| **Rigenera** | Riscrive l'ultima parte, sostituendone il testo. Solo sull'ultima parte. |
| **Scambia** | Scrive un'altra versione dell'ultima parte e conserva la vecchia come ramo separato. Solo sull'ultima parte. Vedi [Rami e struttura ad albero](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Riscrittura** | Fa riscrivere a il modello questa parte a partire dalle tue istruzioni, su qualsiasi parte, vedi [sotto](#redrafting-a-part). Non mentre il testo è in modifica. |
| **Cronologia (n)** | Le [versioni del testo](version-history.md) della parte, con il numero di versioni: confrontarle, ripristinarne una, dare un nome a una. Non mentre il testo è in modifica. |
| **Annota la parte** | Crea una [nota](annotations.md#making-an-annotation) sull'intera parte. Per annotare parole, selezionale e fai clic destro (**Annota la selezione**). |
| **Annota con l'IA** | Permette a un modello di [rivedere la parte](annotations.md#letting-a-model-annotate) e proporre note. Non mentre il testo è in modifica. Per più parti insieme usa **Annota un intervallo di parti con l'IA** nel menu delle impostazioni (⚙), e per ogni nuova parte **Annotazione automatica** (vedi [Annotazioni](annotations.md#annotating-every-new-part-automatically)). |
| **Dirama la storia** | Avvia un nuovo ramo da questa parte, vedi [Rami e struttura ad albero](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Unisci alla parte precedente** | Aggiunge il testo di questa parte alla parte che la precede, vedi [sotto](#merging-a-part-into-the-previous-one). Non sulla prima parte. |
| **Mostra il prompt** | Mostra il prompt esatto inviato al modello per questa parte. |
| **Utilizzo dei token** | Mostra come è stato usato il contesto del modello per questa parte: un grafico delle sezioni del prompt (istruzioni del modello, lore, riassunti, storia fin qui, tue istruzioni, spazio riservato alla risposta) e la dimensione di ogni messaggio inviato al modello. Se parte della storia non è entrata, indica quante parti sono state escluse. Le parti scritte prima che questo fosse registrato non hanno il grafico. |
| **Utilizzo del lore** | Elenca le voci del [lorebook](../lorebooks/index.md) finite nel prompt di questa parte, con i token che ognuna ha richiesto e ciò che l'ha trovata (il prompt, oppure il contenuto di un'altra voce quando il lorebook scansiona più in profondità). Le parti scritte a mano, quelle scritte prima che questo fosse registrato, o scritte per un libro senza lorebook indicano che non è stato registrato nulla. Per l'intero libro usa **Utilizzo del lore** nel menu delle impostazioni (⚙), vedi [sotto](#which-lore-entries-were-used). |
| **Immagini** | Allega immagini alla parte, vedi [Immagini](#images). |
| **Genera riassunto** / **Mostra il riassunto** | Riassume la storia fino a questa parte, oppure mostra il riassunto, vedi [Riassunti](summaries.md). |
| **Elimina riassunto** | Rimuove il riassunto di questa parte. Per un [meta riassunto](summaries.md#meta-summaries) chiede se scioglierlo o eliminarlo del tutto. |
| **Elimina** | Elimina la parte, vedi [sotto](#deleting-a-part). |

*Rigenera* e *Scambia* usano i dettagli del turno dell'ultima parte (vedi [sotto](#turn-details)). Per cambiare
ciò che succede, modificali prima.

![Il menu di una parte, con il sottomenu Ispeziona aperto](../../images/story-part-menu.png)

![Utilizzo dei token di una parte](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadati della parte

Accanto a ogni parte:

| | |
|---|---|
| **Ordine n \| ID DB: m** | La posizione della parte nel ramo, e il suo ID nel database. |
| **Modello** | Il fornitore di inferenza che ha scritto la parte. |
| **Token: x (y)** | `x` token di testo nella parte, `y` token nel prompt che l'ha prodotta. |
| **Parole** | Parole nella parte. |
| **Dettagli del turno** | Le istruzioni da cui la parte è stata scritta. |

<a id="turn-details"></a>

### Dettagli del turno

**Dettagli del turno** apre *Ambientazione della scena*, *Personaggio POV*, *Personaggi presenti* e *Istruzioni* da cui la parte è stata
scritta. Per l'ultima parte possono essere modificati (le modifiche sono salvate subito) e vengono usati da *Rigenera* e
*Scambia*. Per le parti precedenti sono di sola lettura.

![Dettagli del turno di una parte](../../images/turn-details.png)
<a id="editing-the-text"></a>

## Modifica del testo

Il testo generato è modificabile. Scegli **Modifica** nel menu della parte, cambia il testo e scegli **Salva**. Le parti
modificate vengono inviate al modello così come le hai modificate.

Modificare una parte coperta da un [riassunto](summaries.md) rende il riassunto non più valido; Marginalia se ne accorge
alla generazione successiva e scarta il riassunto. Per evitare sorprese, **Salva** controlla prima il nuovo testo: quando
renderebbe non validi dei riassunti, compare una finestra che indica quanti sono e offre **Torna alla modifica** (non viene
salvato nulla e l'editor resta aperto con il tuo testo), **Salva comunque** (i riassunti diventano non validi) e **Salva e
riconvalida i riassunti** (il testo viene salvato e i riassunti restano validi, per una modifica che non cambia ciò che
dicono, come un refuso). Il controllo viene fatto per **Salva** (nel menu, con la scorciatoia e con il pulsante
dell'editor); i salvataggi automatici, per esempio quando avvii una generazione con l'editor aperto, non chiedono nulla.

<a id="keyboard-shortcuts"></a>

## Scorciatoie da tastiera

Su un computer (il [layout per telefono](../phone.md) non ne ha) il ciclo di scrittura funziona con la tastiera:

| Tasto | |
|---|---|
| `+` | Apre il pannello *Istruzioni del nuovo turno* con il cursore in *Istruzioni*. |
| `Ctrl+Enter` | Genera dal pannello aperto. `Esc` lo chiude; la tastiera torna alla storia. |
| `Ctrl+.` | Interrompe la generazione in corso. |
| `s` / `r` | **Swipe** / **Rigenera** l'ultima parte. |
| `e` | Apre l'editor della parte più visibile sullo schermo (l'ultima parte, se nessuna lo è). |
| `Ctrl+S` | Salva la parte in modifica (anche mentre il cursore è nel suo testo). |
| `/` | Porta il cursore nella barra dei comandi (prima viene mostrata la scheda della storia). |
| `Ctrl+Shift+F` | Apre la scheda *Struttura* e porta il cursore nella ricerca del ramo (`Enter` / `Shift+Enter` vanno alla corrispondenza successiva / precedente). |
| `Ctrl+Shift+H` | Apre la finestra *Cerca e sostituisci* del libro. |
| `a` | **Annota la selezione**: crea una nota sul testo selezionato in una parte. |
| `Alt+PageDown` / `Alt+PageUp` | Mostra la scheda successiva / precedente, scorrendo le schede di tutti i fogli nell'ordine del layout e ripartendo dall'inizio. |

Un tasto senza `Ctrl`, `Alt` o `Meta` (come `+`, `s`, `r`, `e`, `a`, `/`) funziona solo quando nessun campo di testo ha il
fuoco, così non interferisce mai con la scrittura. Le scorciatoie funzionano nella scheda della storia del libro aperto e
non mentre un'altra finestra è aperta sopra di essa. I pulsanti mostrano il loro tasto nel suggerimento, e il menu
dell'ultima parte lo mostra dopo *Modifica*, *Rigenera* e *Swipe*. Su Mac `Cmd` funziona dove un tasto dice `Ctrl` (a meno
che tu non abbia assegnato quel `Cmd` a un'altra azione). Puoi cambiare ogni tasto in
[Impostazioni → Scorciatoie da tastiera](../account.md#keyboard-shortcuts).

<a id="deleting-a-part"></a>

## Eliminare una parte

**Elimina** rimuove la parte dalla storia. Le parti che la seguivano restano e continuano dalla parte che la precede, quindi
eliminare una parte nel mezzo della storia la toglie semplicemente. Eliminare l'ultima parte fa diventare la parte precedente
la nuova fine del ramo.

<a id="deleting-many-parts-at-once"></a>

### Eliminare molte parti insieme

Il menu delle impostazioni (⚙) ha **Eliminazione di massa**. Attivandola compare una casella di spunta a sinistra di ogni
parte e una barra sotto la storia con il numero di parti spuntate e un pulsante **Elimina**. Spunta le parti che vuoi
eliminare (le spunte restano mentre scorri, perché la storia carica solo una finestra di parti); **Shift + clic** su una casella
spunta (o deseleziona) tutte le parti comprese tra quella e quella cliccata in precedenza; una parte spuntata è colorata di
rosso. Poi premi **Elimina**: dopo una conferma le parti vengono eliminate una per una dietro una barra di avanzamento che
non può essere interrotta. Vengono eliminate come si elimina una singola parte: le parti che seguivano una parte eliminata
continuano dalla parte che la precede, e le parti eliminate finiscono nel cestino. Poi la storia viene mostrata di nuovo e
l'eliminazione di massa si disattiva. **Annulla** nella barra, o scegliere di nuovo **Eliminazione di massa**, la disattiva
senza eliminare nulla. Avviare una generazione la disattiva a sua volta. Una parte spuntata che una finestra o una
traduzione sta elaborando resta nella storia, e Marginalia indica chi la sta usando.

![Eliminazione di massa con alcune parti spuntate](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Riscrittura di una parte

**Rigenera** e **Alternativa** lavorano solo sull'ultima parte e la riscrivono da zero. **Riscrittura** lavora su qualsiasi parte, anche una vecchia nel mezzo della storia, e conserva ciò che non chiedi di cambiare. Scegli **Riscrittura** nel menu della parte:

1. Scrivi cosa dovrebbe cambiare in **Istruzioni per la riscrittura**: *aggiungi più dialogo tra i due*, *allunga il combattimento*, *elimina la spiegazione della mappa*, *lascia il finale com'è*. Istruzioni vuote riscrivono la parte con gli stessi eventi.
2. Scegli **Lunghezza** se la parte deve diventare **Più breve** o **Più lungo** (il valore predefinito è **Stessa lunghezza**): dopo le tue istruzioni viene aggiunta una frase che lo chiede (vedi [Prompt di riscrittura più breve e più lungo](prompts.md#redraft-shorter-and-longer)). Scegli **Riscrittura**. Il testo scorre in **Testo riscritto**; **Interrompi** lo termina prima. Scegli **Riscrivi di nuovo** per un altro tentativo, anche con altre istruzioni. **Differenze** mostra cosa è cambiato rispetto alla parte, parola per parola. Quando la parte ha [annotazioni](annotations.md) aperte, **Gestisci le annotazioni aperte della parte** (selezionata all'inizio) le passa anche al modello, e **Applica** le segna come risolte.
3. Il testo riscritto è un campo che puoi ancora modificare (l'icona dell'angolo lo fa crescere fino a contenere tutto il testo), e **Differenze** segue le tue modifiche. **Applica** sostituisce il testo della parte con quello; **Chiudi** lo scarta. Niente nel libro cambia prima di **Applica**.

![La finestra di riscrittura: le istruzioni, il nuovo testo e le differenze](../../images/redraft-dialog.png)

Il modello riceve i [prompt di riscrittura](prompts.md#redraft-prompt): la parte, le tue istruzioni, la storia prima della parte (la storia più vecchia come [riassunti](summaries.md) dove ce ne sono di validi, il resto come testo, con la parte più recente per prima quando non entra tutto), tanta storia dopo la parte quanta ne entra (fino a un quarto dello spazio disponibile), il lore che i libri di lore danno ora a quella parte (ricalcolato per il turno della parte, così una modifica a una voce arriva alla riscrittura; il lore con cui la parte era stata scritta viene usato solo quando il libro non ha un libro di lore), il punto di vista, il tempo verbale e lo stile del libro, e l'ambientazione della scena, il personaggio con il punto di vista, i personaggi presenti e le istruzioni del turno da cui proviene la parte. La storia dopo la parte serve perché il nuovo testo continui ancora a portare a ciò che segue: gli eventi da cui dipendono le parti successive dovrebbero restare.

La parte mantiene il suo posto, il suo riassunto, le sue immagini e il modo in cui è stata generata; il testo, le sue parole e i suoi token vengono sostituiti e la parte risulta modificata. Applicare la riscrittura è una modifica come **Salva** nell'editor, quindi i [riassunti](summaries.md) che descrivono il vecchio testo diventano non aggiornati, e Marginalia dice quanti sono. Anche un riassunto sulla parte stessa è tra questi.

Quando un riassunto descrive la parte (la parte ne ha uno, oppure ne ha uno una parte più recente del ramo), la finestra mostra la casella **Rivalida i riassunti che questa riscrittura rende non aggiornati**, inizialmente non selezionata. Selezionala quando i riassunti dicono ancora la cosa giusta (la riscrittura ha cambiato la formulazione, non ciò che accade): **Applica** ricalcola allora il loro hash e restano validi. Lasciala non selezionata quando gli eventi sono cambiati, e rifai i riassunti.

La finestra non blocca la storia, vedi [Lavorare accanto alle finestre di riscrittura e annotazione](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Lavorare accanto alle finestre di riscrittura e annotazione

Su computer le finestre **Riscrittura** e **Annota con l'IA** stanno a destra della storia e la lasciano utilizzabile: puoi leggere, scorrere e modificare la storia mentre il modello scrive. Trascina una finestra dal suo titolo per spostarla, o dal bordo per ridimensionarla. La freccia (⌄) nel titolo ripiega una finestra nel [pannello delle esecuzioni](#the-panel-of-the-runs) in basso a sinistra della pagina: il modello prosegue, **Apri** (o un clic sulla voce) riporta la finestra, e **Interrompi** ferma il modello (a lavoro finito, **Chiudi** chiude la finestra). Su telefono le finestre coprono comunque la storia, ma si ripiegano allo stesso modo.

![La finestra di riscrittura accanto alla storia mentre il modello scrive, e l'esecuzione nel pannello in basso a sinistra](../../images/redraft-beside-story.png)

**Chiudi e Interrompi sono due cose diverse.** **Interrompi` della finestra stessa ferma il modello e la finestra resta con ciò che è arrivato. **Chiudi** mentre il modello lavora non lo ferma: la finestra si ripiega nel pannello e il modello prosegue nella sua coda; la sua risposta è nella finestra quando la riapri. Chiudi una finestra il cui modello ha finito, oppure **Chiudi** nel pannello, e sparisce; chiudere il libro o la scheda del browser ferma anche una finestra ripiegata. Lo stesso vale per le altre finestre che aspettano un modello: un **riassunto** (e *Crea riassunti*, *Riscrivi tutti i riassunti*), il [Revisore](../extensions/reviewer.md), le descrizioni del libro, i generatori di lore e di scaletta, i generatori di immagini e manga, e lo scrittore di personaggi dei Lettori Beta. Gli *Studio* e *Converti* dell'audiolibro non bloccano la pagina; chiuderli non ha mai interrotto la conversione.

Mentre una finestra è aperta (o ripiegata) su una parte, la parte mostra cosa sta facendo accanto al suo menu (*Riscrittura in corso · Il libro*), e **Rigenera**, **Alternativa**, **Riscrittura**, **Annota con l'IA**, **Unisci nella precedente** ed **Elimina** sono disattivati per quella parte, così la finestra non perde la sua parte. Modificare la parte resta possibile. Chiudere il libro chiude le sue finestre e ferma i loro modelli. Una parte su cui sta lavorando un [traduttore](../extensions/real-language-translator.md) mostra *Traduzione del parlato della parte...* allo stesso modo, e due traduttori non lavorano mai sulla stessa parte insieme: il secondo segnala che la parte è in uso.

- Quando **Applica** una riscrittura dell'intera parte e la parte è stata modificata dopo l'apertura della finestra (l'hai cambiata nel frattempo), Marginalia chiede prima: **Differenze** confronta allora la parte com'è ora con la riscrittura, così vedi cosa l'applicazione sostituirebbe; scegli **Applica comunque** o **Annulla**. Una riscrittura che [si applica da sola](annotations.md#annotating-every-new-part-automatically) non lo fa quando la parte è stata modificata: la finestra attende te.
- Quando la parte è stata eliminata prima che tu **Applichi** la riscrittura o aggiunga le annotazioni, la finestra lo dice, non scrive nulla e si chiude.
<a id="the-panel-of-the-runs"></a>

### Il pannello delle esecuzioni

Tutto ciò che sta lavorando sui tuoi libri con un modello in questo momento è elencato in basso a sinistra di ogni pagina: una generazione, una
riscrittura o un'annotazione, una traduzione del [traduttore Real Language](../extensions/real-language-translator.md) o
[Conlang](../extensions/conlang-translator.md), una riscrittura dei riassunti, il Knowledge Tracker che legge la storia, una conversione [Audiobook](../extensions/audiobook.md), un comando della barra dei comandi che richiede tempo (*Abbozza le istruzioni*, un riassunto...: la sua finestra sta di lato e si ripiega come le altre). Sono elencate anche le finestre in cui attendi una risposta mentre il modello risponde: **Impersonate**, l'assistente di aiuto, una traduzione rapida, il progettatore Conlang, una chat di Side Query, Beta Readers o la procedura guidata della trama. Elenca le tue esecuzioni da **tutte le pagine aperte** (un'altra scheda, il telefono), non solo quelle avviate in questa pagina. Se non c'è nulla in corso, non viene mostrato nulla.

![Il pannello delle esecuzioni aperto: la riscrittura ripiegata al suo interno, con la fine del testo del modello, Apri e Interrompi](../../images/runs-panel.png)

- **Chiuso** è una piccola barra: la prima esecuzione, quante altre ce ne sono (*Traduzione del parlato della parte... · altre 2*) e una barra in movimento mentre un modello lavora. Fai clic per aprire l'elenco; si apre da solo quando ripieghi una finestra al suo interno.
- **Aperto**, ogni esecuzione mostra cosa fa, il libro, da quanto tempo è in corso, cosa sta aspettando e la fine di ciò che scrive il modello:
  - *In attesa del modello: 2 richieste prima di questa* quando il modello è occupato con altre richieste (vedi *Connessioni* in [Fornitori di inferenza](../inference-providers.md));
  - *Il modello sta ragionando...* oppure *Il modello sta scrivendo...*, con sotto la fine del suo ragionamento o della sua risposta;
  - *Ti aspetta* quando l'esecuzione attende una tua decisione (una riscrittura da applicare, una proposta del Knowledge Tracker da rivedere, la domanda dell'Prompt Inspector prima di una generazione, un comando della procedura guidata della trama da autorizzare).
- **Interrompi** ferma l'esecuzione, ovunque sia stata avviata. **Apri**, oppure un clic sulla voce, mostra di nuovo una finestra ripiegata (solo sulla pagina in cui era stata aperta) oppure l'esecuzione stessa; diventa **Revisione** quando l'esecuzione ti aspetta. Un'esecuzione del Knowledge Tracker apre il suo libro in questa pagina (quando qui non è aperto nessun altro libro; altrimenti ti viene chiesto di chiudere prima quello), passa alla scheda Knowledge e mostra la revisione. **Chiudi** chiude una finestra in modo definitivo (e interrompe il suo modello).

Su un computer puoi spostare il pannello dalla sua maniglia (⋮ sul lato sinistro); resta dove lo lasci in questo browser. Nella metà inferiore della pagina cresce verso l'alto quando lo apri, come nel suo angolo, e rimane sempre entro i limiti della pagina. Quando si trova dove c'è il pulsante tondo dell'[assistente di aiuto](../administration/help-assistant.md) (su un libro, entrambi sono nell'angolo in basso a sinistra), il pulsante si sposta di lato e torna quando lo spazio è libero.

Quando un'esecuzione avviata da te termina dopo che la sua pagina è stata chiusa, un'altra pagina aperta invia la sua [notifica](../account.md#notifications) (per le esecuzioni che ne hanno una, come la riscrittura dei riassunti).

<a id="redrafting-only-a-passage"></a>

### Riscrivere solo un passaggio

Per riscrivere poche frasi lasciando il resto della parte invariato, selezionale nel testo della parte e fai **clic destro** (una pressione prolungata su un telefono): il menu contiene **Riscrivi la selezione**. (Il menu ☰ della parte non è proposto per questo, perché aprirlo può far perdere la selezione in alcuni browser.) La finestra funziona come descritto sopra con due differenze: mostra il **Testo selezionato**, e la riscrittura è solo il testo che lo sostituisce. Il modello riceve l'intera parte con la selezione evidenziata, insieme alla storia e al lore intorno ad essa, così il nuovo passaggio si collega alle frasi prima e dopo. **Differenze** confronta la selezione con il testo riscritto; **Applica** mette il testo riscritto al posto della selezione e conserva il resto della parte, incluso lo spazio o l'a capo ai bordi della selezione. Con le [annotazioni](annotations.md) aperte, la casella di controllo propone solo quelle il cui passaggio si sovrappone alla selezione. Se la parte è stata modificata dopo che hai selezionato il testo (in un'altra finestra), **Applica** si rifiuta e devi selezionarlo di nuovo. Mentre la parte è in modifica si usa la selezione dell'editor, e la modifica viene prima salvata.

<a id="what-follows-a-generated-part"></a>

### Cosa segue una parte generata

Quando una parte che hai generato (dalla pagina o dalla barra dei comandi) è completa, il libro può fare altro da solo, in quest'ordine:

1. **Annota**: l'[annotazione automatica](annotations.md#annotating-every-new-part-automatically) rivede la parte.
2. **Riscrittura**: la parte viene riscritta per tenere conto delle note.
3. **Traduci**: i traduttori [Real language](../extensions/real-language-translator.md) e [Conlang](../extensions/conlang-translator.md) traducono i suoi discorsi.
4. **Knowledge**: il [Knowledge Tracker](../extensions/knowledge-tracker.md) la legge.
5. **Audio**: l'[Audiobook](../extensions/audiobook.md) la trasforma in audio.

Vengono eseguiti solo i passaggi che il libro ha attivato. Girano sul server, quindi la pagina può essere chiusa, e formano un'unica voce nel [pannello delle esecuzioni](#the-panel-of-the-runs): *Dopo la parte 14: Annota ✓ · Riscrittura … · Traduci*, con il testo del modello e un solo **Interrompi**. Prima vengono i passaggi che cambiano il testo, così il tracker e l'audio ricevono il testo definitivo. Un passaggio che attende una tua decisione (note o una riscrittura da scegliere) apre la sua finestra, e la voce ha **Revisione** per aprirla in un'altra pagina.

- **Interrompi**, la chiusura di una finestra che ti attende, oppure un passaggio che fallisce la termina lì: ciò che è stato fatto resta, il resto non viene eseguito, e un messaggio indica cosa manca, da fare a mano (ogni passaggio ha la sua azione sulla parte).
- Mentre la **riscrittura** è in corso, la parte successiva non può essere generata (ti viene detto quale esecuzione lo impedisce); mentre gli altri passaggi sono in corso, può esserlo.
- La parte non può essere eliminata finché il suo testo non è definitivo; generarla di nuovo termina ciò che seguiva la sua versione precedente.
- Un [agente](../agents.md) esegue questi passaggi con i suoi comandi: nulla segue una parte generata da un agente.
<a id="merging-a-part-into-the-previous-one"></a>

## Unire una parte con quella precedente

Un modello che si è fermato a metà di una frase (il limite di risposta, una connessione interrotta) non può essere
"fatto continuare" in modo affidabile sullo stesso testo. Scrivi invece la parte successiva (*continua da dove si è
fermata l'ultima parte*) e unisci i due testi con **Unisci alla parte precedente** nel menu della seconda. Si apre una
revisione:

- **Unisci i testi con**: una riga vuota, un a capo, uno spazio oppure nulla. È preselezionato in base alla fine del
  testo precedente: un paragrafo dopo un testo che terminava una frase, uno spazio dopo uno che è stato interrotto.
- L'**ambientazione della scena**, il **personaggio POV**, i **personaggi presenti** e le **istruzioni** di entrambi i
  turni, combinati in campi modificabili. Ciò che i due turni hanno in comune all'inizio viene mantenuto una sola volta
  ("Scrivi il duello. Mantienilo breve" e "Scrivi il duello. Aggiungi la pioggia" danno la prima riga, e "Aggiungi la
  pioggia" nella riga successiva).
- **Dati**: ciò che le due parti conservano, valore per valore (per esempio le [variabili](../templates/macros.md) della
  storia e i dati delle estensioni), con l'indicazione se il valore è uguale, diverso o presente solo in una delle parti.
  La parte unita mantiene il valore della **parte successiva** (è quella in cui la storia si trova ora), a meno che tu non
  scelga per essa il valore della parte precedente, oppure non ne lasci il valore. I valori uguali sono nascosti, salvo
  che tu non li richieda.

![La revisione prima che una parte venga unita con quella precedente](../../images/part-merge-dialog.png)

La parte precedente mantiene il proprio posto, il proprio riepilogo e il modo in cui è stata generata (prompt, modello,
tempi); riceve il testo unito, il ragionamento unito e le immagini, e le sue parole e i suoi token vengono ricalcolati.
La parte che hai unito viene eliminata (si trova nel [Cestino](../trash.md)) e ciò che la seguiva continua dalla parte
precedente. I riepiloghi che coprono le due parti diventano obsoleti, come dopo una modifica. La [cronologia](version-history.md#backups-copies-and-the-trash) della parte unita viene rimossa e la parte superstite ha il testo unito come versione **Unito**. La parte non può essere unita quando è la prima parte del libro, oppure quando la parte che la precede ha altre continuazioni (swipe della parte): eliminale o trasformale prima in un ramo.
<a id="images"></a>

## Immagini

Una parte può avere delle immagini: una mappa, il ritratto di un personaggio, un'illustrazione. Scegli **Immagini** nel menu della parte per aprire l'elenco delle immagini della parte.

![La finestra Immagini di una parte](../../images/images-dialog.png)

- **Aggiungi immagine** (o trascina un file su di esso) carica un'immagine PNG, JPEG, GIF o WebP di al massimo 10 MB. Le immagini WebP vengono convertite in PNG al caricamento, perché i file Word e PDF non possono contenere WebP.
- **Didascalia** viene mostrata sotto l'immagine. Può essere vuota.
- **Posizione nella parte** è una griglia 3x3. La colonna è il lato del testo (sinistra, centro, destra), la riga è il punto nel testo: in alto l'immagine va prima del testo, al centro va a metà del testo, in basso va dopo il testo. Fai clic su un quadrato per scegliere.
- **Il testo scorre attorno all'immagine** permette al testo di scorrere intorno all'immagine invece che sopra e sotto di essa. A sinistra o a destra, il testo scorre accanto all'immagine. Nella colonna centrale il testo si trova su entrambi i lati: alcuni blocchi del testo (circa 400 caratteri, due blocchi almeno) in quella riga diventano due colonne, la prima parte a sinistra dell'immagine e il resto a destra, così il testo si legge scendendo nella colonna di sinistra e prosegue in quella di destra. Due immagini nello stesso punto stanno una sopra l'altra tra le colonne. Quando il testo non ha spazio per due colonne (un solo paragrafo, oppure i blocchi sono già occupati dalla fascia sopra), l'immagine è un blocco a sé, centrato.
- **Larghezza** è la larghezza dell'immagine come percentuale della larghezza del testo. **Automatico** mantiene la dimensione dell'immagine (mai più larga del testo; un'immagine con testo su entrambi i lati è al 30 %). Un'immagine accanto alla quale scorre il testo è al massimo al 60 %, e una con testo su entrambi i lati al massimo al 40 %, così il testo conserva un po' di spazio. L'altezza segue le proporzioni dell'immagine.
- Il pulsante del cestino rimuove l'immagine dalla parte.

![La posizione di un'immagine: al centro, con il testo che scorre attorno ad essa](../../images/images-position.png)

**Anteprima come nel visualizzatore** (un pulsante della finestra, e la stessa voce nel menu della parte quando la parte ha immagini) mostra la parte come la mostra il [visualizzatore](publishing.md), con lo stile del testo del libro: le immagini dove le hai messe, e un interruttore per uno schermo largo o stretto (telefono). Usa il testo presente nell'editor così com'è ora, salvato o no.

![L'anteprima: il testo in due colonne con l'immagine in mezzo](../../images/images-preview.png)

L'editor della storia non usa la posizione: mostra le immagini sotto il testo della parte, come elenco. La posizione viene usata dal visualizzatore e dagli [export](export.md#images).

Il punto del testo in cui va un'immagine non dipende da numeri salvati insieme ad essa: il testo viene tagliato tra i suoi blocchi (paragrafi, elenchi, citazioni e blocchi di codice non vengono mai tagliati), così le immagini restano dove devono stare quando modifichi il testo. Il centro è il punto tra i blocchi più vicino alla metà del testo; un'immagine con il testo che scorre attorno in basso viene messa prima dell'ultimo blocco, così che almeno quel blocco scorra accanto ad essa. Le immagini nello stesso punto restano nell'ordine dell'elenco; quelle vicine nello stesso punto (che non lasciano scorrere il testo attorno a loro) stanno una accanto all'altra in una fila, quando ci stanno. PDF e Word lo fanno per le immagini che hanno una larghezza.

Le immagini **non fanno parte del testo**. Il modello non le vede, non sono nel prompt, non contano come token e non modificano i [riassunti](summaries.md). Inoltre non sono Markdown: un link `![](...)` scritto nel testo non è un'immagine della parte, usa invece **Immagini**.

Utile da sapere:

- **Rigenera** mantiene le immagini della parte, anche quando la generazione fallisce. **Swipe** scrive una nuova parte (una nuova versione accanto a quella vecchia), che parte senza immagini. **Dirama la storia** copia le immagini nella nuova parte.
- Le immagini appartengono al tuo account. Nel [visualizzatore](publishing.md) anche i lettori di un libro pubblicato le vedono, a meno che tu non deselezioni **Mostra le immagini ai lettori** nella scheda *Informazioni* del libro.
- Un [backup](backups.md) ricorda quali immagini ha una parte, ma non i file delle immagini. Ripristinare un backup in un altro account o in un'altra installazione dà parti senza le loro immagini.
- Rimuovere un'immagine da una parte la toglie solo dalla parte. Il file resta e continua a comparire in [Risorse](../resources.md), dove puoi scaricarlo, sostituirlo o eliminarlo. Eliminarlo lì lo rimuove da ogni parte che lo usa. Il file stesso resta nella cartella dei dati.

<a id="summaries"></a>

## Riassunti

Il pulsante **Riassunti** nella barra in basso (a sinistra del menu impostazioni ⚙) apre la panoramica dei riassunti del ramo, dove possono essere modificati, eliminati e uniti in meta-riassunti. La descrizione del pulsante mostra quanti token occupano nel prompt i riassunti in uso. Vedi [Riassunti](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Quanto contesto occupa la storia

Il menu impostazioni (⚙) ha **Token del ramo**. Mostra, prima di generare qualsiasi cosa, quanto la storia del ramo attivo occupa nel prompt: una barra per la storia **senza riassunti** (tutte le parti) e **con riassunti** (i riassunti e le parti successive all'ultima), e una tabella con le parti, i token e la quota del **budget del prompt** (il contesto del modello del libro meno lo spazio per la risposta). Contano solo i riassunti che descrivono ancora la storia; quelli che non corrispondono più sono indicati per nome e vengono scartati quando una parte viene generata. Il lore, il template di sistema e le istruzioni del turno occupano spazio dallo stesso budget. Senza un modello con una dimensione di contesto, la tabella non ha quote.

![Token del ramo](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Quali voci del lore sono state usate

**Utilizzo del lore** nel menu impostazioni (⚙, **Analisi**) indica quali voci del lorebook del libro sono state usate dalle generazioni e quali non lo sono mai state,
così un lorebook si può sfoltire in base a ciò che fa e non a supposizioni. Ogni generazione registra quali voci sono entrate nel suo prompt
(e i token di ciascuna); la finestra somma questi dati sulle parti:

- **Parti** sceglie cosa viene conteggiato: il **Ramo attivo**, oppure **Tutti i rami**, cioè ogni ramo e ogni variante di ogni parte del libro.
- **Dalla parte** e **Alla parte** limitano l'intervallo, numerato come nello schema (la prima parte di un ramo è 1; in tutti i rami il numero indica
  quanto la parte è in profondità nell'albero). Lasciali vuoti per l'inizio e la fine.
- **Mostra** mantiene le voci che sono state **Mai usata**, **A volte usata** o **Sempre usata** nelle parti conteggiate.

![La finestra Utilizzo del lore](../../images/lore-usage.png)

La tabella ha una riga per ogni voce dei lorebook del libro, dalla più usata alla meno usata: il suo lorebook, se è stata mai, a volte o sempre
usata, in quante parti (tra le parti che hanno una registrazione, con la percentuale), i token che ha consumato in tutte e per ogni uso, e la prima e
l'ultima parte che l'hanno usata. Clicca su un'intestazione per ordinare. Una voce usata in precedenza ma che ora non è più nei lorebook del libro (eliminata, spostata, o
il cui lorebook è disattivato o non è più quello del libro) resta nell'elenco contrassegnata con *ora non è nel lorebook*; una voce eliminata non ha un nome.

Vengono conteggiate solo le parti che hanno una registrazione: una parte scritta a mano, scritta prima che Marginalia conservasse questa registrazione, oppure scritta quando il libro non aveva alcun lorebook non ne ha una, e la finestra indica quante sono. Le voci *Sempre usata* potrebbero essere voci costanti; le voci *Mai usata* sono candidate alla rimozione o a un filtro migliore. [**Anteprima dell'attivazione**](../lorebooks/entries.md#preview-activation) nell'editor del lorebook mostra perché una voce ora non si attiva.

<a id="text-styles"></a>

## Stili del testo

Il menu impostazioni (⚙) nella barra in basso ha **Aspetto → Cambia gli stili del testo**, un menu con una voce per stile, in ordine alfabetico. Per sapere come creare uno stile, vedi la [guida agli stili](styling-guide.md).
Uno stile decide come appare il testo delle parti: carattere, dimensione, colori, allineamento, titoli... L'applicazione include due
stili che non si possono modificare: **Normale** (l'aspetto dell'applicazione) e **Libro** (uno stile di lettura simile a un libro, quello usato dal [visualizzatore](publishing.md)); puoi creare i tuoi in [Impostazioni → Stili](../account.md#styles).

- La prima voce, **Predefinito (...)**, segue il tuo *stile predefinito* (quello indicato in *Impostazioni → Stili*, Normale se non ne hai scelto uno). Un nuovo libro non ha uno stile proprio e mostra quello.
- Scegliere un'altra voce salva lo stile **nel libro**, quindi sostituisce il predefinito solo per quel libro; la voce spuntata è quella che il libro ha ora. **Predefinito** annulla la scelta.
- Uno stile che elimini ricade sul tuo predefinito nei libri che lo usavano.

![Aspetto, Cambia gli stili del testo: gli stili tra cui scegliere](../../images/story-style-menu.png)

Nello stile **Libro** i paragrafi sono giustificati e le parole lunghe vengono sillabate automaticamente, usando il dizionario della
**Lingua** del libro (impostata nella scheda [Informazioni](managing-books.md#the-about-tab)). La sillabazione è affidata al browser;
per alcune lingue rare non ha un dizionario e il testo semplicemente non viene sillabato.

I libri che avevano attivo il vecchio interruttore *Cambia gli stili del testo* continuano a mostrare lo stile Libro finché non ne scegli un altro.

<a id="highlighting-generated-text"></a>

## Evidenziare il testo generato

Il menu impostazioni (⚙) ha anche la voce spuntabile **Evidenzia il testo generato**. Quando è attiva, le parole scritte da un modello sono evidenziate in ogni parte, così ciò che hai aggiunto o riscritto risalta. Marginalia segue il testo attraverso le [versioni della parte](version-history.md): ogni versione viene sovrapposta a quella precedente, e le parole che hanno in comune conservano chi le ha scritte, quindi una frase che hai inserito in un testo generato resta tua per quante volte il modello riscriva il resto. Una parte che non hai mai modificato è evidenziata per intero; una parte che hai scritto tu non ha nulla di evidenziato. Ciò che scrive un agente conta come del modello. L'interruttore non viene salvato: alla riapertura della pagina è di nuovo spento.

Una parte la cui [cronologia è disattivata](version-history.md) non ha versioni da seguire: viene confrontata con il testo generato per primo dal modello. L'evidenziazione funziona anche sul [telefono](../phone.md).

Lo stesso conteggio fornisce il rapporto: la scheda **Info** del libro ha **Paternità del ramo** con un pulsante **Calcola**, che indica quale percentuale delle parole del ramo attivo hai scritto tu e quale percentuale ha generato un modello, e
[l'esportazione](export.md) può chiudere il file con un riepilogo di questi dati.

![Testo generato evidenziato e il menu impostazioni](../../images/highlight-generated.png)

<a id="commands"></a>

## Comandi

Accanto al menu sotto la storia c'è una **riga di comando**: digita `/` per vedere i comandi, scegline uno e premi **Invio** (o il pulsante di riproduzione) per eseguirlo. Ciò che restituisce un comando viene mostrato in una piccola finestra sopra la riga. Vedi
[Comandi](../commands/index.md) per sapere come scrivere gli argomenti, cosa mostra l'aiuto e come interrompere un comando che richiede tempo. Per ora la riga di comando è presente solo nella scheda della storia del layout desktop.
<a id="search-and-replace"></a>

## Ricerca e sostituzione

Il menu delle impostazioni (⚙) contiene anche **Cancella la cronologia del libro** e **Svuota il cestino della cronologia**, vedi [Cronologia delle versioni](version-history.md#deleting-versions).

Il menu delle impostazioni (⚙) contiene anche **Cerca e sostituisci**, che cambia una parola, un nome o una frase in tutta la storia in un'unica operazione. La finestra ha i campi **Cerca** e **Sostituisci con** e queste opzioni:

![Il menu delle impostazioni con Cerca e sostituisci](../../images/story-settings-menu.png)

![La finestra Cerca e sostituisci con una ricerca inserita](../../images/search-replace-dialog.png)

| Opzione | Predefinito | Significato |
|---|---|---|
| **Cerca con regex** | disattivata | Il testo di ricerca è una [espressione regolare](../regular-expressions.md) e la sostituzione può usare i suoi gruppi (`$1`). Altrimenti il testo viene cercato così come è scritto. |
| **Distingui maiuscole e minuscole** | attiva | Un testo viene trovato solo nella forma maiuscola/minuscola con cui l'hai scritto (*John* non trova *john*). Disattivata: le maiuscole e le minuscole non contano. Bloccata mentre **Cerca con regex** è attiva. |
| **Cerca nei riassunti** | attiva | I [riassunti](summaries.md) vengono cercati e modificati anch'essi. |
| **Cerca in tutti i rami** | disattivata | Vengono cercati tutti i [rami](branches-and-story-tree.md), altrimenti solo quello attivo. |
| **Riconvalida tutti i riassunti** | attiva | Dopo la sostituzione ogni [riassunto](summaries.md) del libro torna valido, vedi sotto. |

Con un'espressione regolare la distinzione tra maiuscole e minuscole segue sempre quanto scritto; inizia l'espressione con `(?i)` per ignorarla. Premi **Cerca** (il pulsante a destra dei campi; sul telefono sotto di essi). La barra di avanzamento mostra a che punto è la ricerca e **Interrompi** accanto ad essa la blocca. La ricerca non modifica nulla.

La tabella elenca ogni parte della storia e ogni riassunto che contiene una corrispondenza, con il suo tipo e il suo id. Apri una riga per vedere le corrispondenze che contiene: il testo intorno alla corrispondenza com'è ora e com'è dopo la sostituzione. Una corrispondenza o una sostituzione troppo lunga per essere mostrata viene indicata come *[testo cercato]* o *[testo sostitutivo]*.

![Le corrispondenze di una ricerca, con la prima parte aperta](../../images/search-replace-found.png)

Premere **Invio** nel campo **Cerca** o **Sostituisci con** equivale a premere **Cerca**.

Quando la ricerca è terminata, i campi e le opzioni vengono bloccati e il pulsante **Sostituisci** è abilitato. **Cerca di nuovo** dimentica ciò che è stato trovato e sblocca la finestra, così puoi modificare la ricerca. **Sostituisci** modifica la storia: la finestra resta completamente bloccata (nemmeno **Interrompi** funziona) finché l'operazione non è conclusa, poi torna vuota. Le parti sostituite vengono contrassegnate come modificate, come se le avessi modificate tu stesso.

Una sostituzione nel testo che un [riassunto](summaries.md#when-summaries-become-outdated) descrive renderebbe il riassunto non aggiornato. Con **Riconvalida tutti i riassunti** attiva, Marginalia ricalcola il valore di controllo di ogni riassunto del libro (quelli di tutti i rami, anche quelli che la sostituzione non ha toccato, e quelli che erano già non aggiornati), così alla fine risultano tutti validi. Questo rende valido anche un riassunto non aggiornato senza esaminarne il testo, quindi usala quando sai che i riassunti dicono ancora la cosa giusta.

Con l'opzione disattivata vengono aggiornati solo i riassunti coperti dalla ricerca. Quelli che non lo sono (riassunti di rami non cercati, che condividono le parti iniziali con il ramo attivo, oppure tutti quelli quando i riassunti non sono cercati) diventano non aggiornati. Se ce ne sono, Marginalia te lo comunica con il numero e chiede se procedere. Un riassunto già non aggiornato resta tale.

![L'avviso sui riassunti che diventerebbero non aggiornati](../../images/search-replace-warning.png)

Nel [layout per telefono](../phone.md) la stessa finestra ha i campi e i pulsanti uno sotto l'altro, e la tabella ha una sola colonna che mostra il tipo e l'id, e sotto di essa la corrispondenza e la sostituzione.

La [Panoramica dei riassunti](summaries.md#search-and-replace-in-the-summaries) ha la stessa finestra solo per i riassunti.

La stessa ricerca e sostituzione può essere eseguita dalla [riga di comando](../commands/index.md#commands-that-come-with-marginalia) con `/search-replace-parts`; chiede conferma prima di sostituire.
<a id="importing-into-the-book"></a>

## Importazione nel libro

Il menu delle impostazioni (⚙) contiene **Importa** (sotto **Importazione ed esportazione**). È la finestra della scheda Libri che crea un nuovo libro da un file (vedi [Gestione dei libri](managing-books.md#creating-a-book)), per il libro in cui ti trovi: le parti vengono invece **aggiunte alla storia**. La **Sorgente** è uno dei seguenti: archivio zip di file `.md`, un documento (Word, LibreOffice, RTF, Markdown, testo semplice, EPUB), lo zip di un vault di Obsidian oppure lo zip di un progetto Scrivener; ciascuno ha lo stesso testo e le stesse opzioni del caso precedente (le parole per parte e il livello di intestazione dei capitoli), e una riga indica che cosa produrrà l'importazione. Le chat di SillyTavern e LM Studio non sono offerte: sono un albero di diramazioni, che non ha posto nel mezzo di una storia.

**Inserisci le parti** indica dove vanno:

- **Alla fine della storia**: dopo l'ultima parte del ramo attivo (la prima parte di un libro vuoto).
- **Prima di una parte** oppure **Dopo una parte**: scegli la parte dalla lista (i numeri sono le posizioni nel ramo attivo, con l'inizio del testo). Le parti vengono inserite tra due parti: tutto ciò che seguiva il punto, con tutte le sue diramazioni, segue le nuove parti, e nessuna parte viene modificata o generata di nuovo. La fine della storia resta dov'era (la storia è più lunga di quante parti siano state aggiunte). I riassunti che coprono il punto diventano obsoleti, perché descrivono la storia senza le nuove parti: la finestra dei [riassunti](summaries.md) li mostra e li rigenera.

![La finestra di importazione di un libro](../images/book-import-into.png)

Le parti di un archivio sono i file in ordine di nome, con i numeri nel nome confrontati come numeri (`2.md` prima di `10.md`), anche nelle cartelle dell'archivio. Le cartelle, i file nascosti (`.nome`), la cartella `__MACOSX` e i file che non sono `.md` vengono ignorati. Il file è il testo della parte così com'è: non viene inviato al modello né modificato, e tutte le parti risultano scritte da te.

L'importazione mostra una barra di avanzamento con il numero di parti completate. **Interrompi** la termina dopo la parte in corso di salvataggio: le parti già aggiunte restano nella storia (l'importazione non viene annullata) e un messaggio indica quante parti sono state importate.

<a id="after-an-import"></a>

### Dopo un'importazione

Quando le parti sono nel libro, una finestra **Importazione terminata** propone le due operazioni che rendono più facile scrivere una storia lunga. (Un libro appena importato come nuovo viene aperto per primo, e la finestra compare davanti a esso.) Entrambe si trovano anche nel menu delle impostazioni (⚙, **Analisi**), quindi possono essere eseguite più tardi o di nuovo:

- **Crea riassunti...** scrive i riassunti di ciò che è stato importato, vedi [Creare i riassunti](summaries.md#create-summaries). Fallo per primo: un agente blocca il libro mentre lavora.
- **Prendi note del lore con un agente...** avvia un [agente](../agents.md) che legge la storia e scrive i personaggi, i luoghi, gli oggetti e i termini che trova come voci del lorebook del libro, vedi [Note del lore](../agents.md#lore-notes-of-an-imported-book).

**Non ora** chiude la finestra senza fare nessuna delle due cose.

![La finestra dopo un'importazione](../../images/import-finished.png)

La stessa importazione in un lorebook è descritta in [Importazione ed esportazione](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Esportare la storia

Il menu delle impostazioni (⚙) contiene anche **Esporta la storia**, che salva il ramo attivo come file di testo, Markdown, HTML, Word, PDF o EPUB. Vedi [Esportare un libro](export.md).

<a id="what-the-model-gets"></a>

## Cosa riceve il modello

Per ogni nuova parte Marginalia invia:

1. Il **modello principale** come messaggio di sistema: ruolo, voci del [lorebook](../lorebooks/index.md), punto di vista, tempo verbale, stile e [riassunti](summaries.md). Il prompt jailbreak del [fornitore di inferenza](../inference-providers.md#general-settings), se attivo, viene per primo.
2. Le parti precedenti del ramo attivo, come risposte precedenti del modello stesso, quante ne entrano nel [limite di contesto](../protocols.md#limits), dalla più recente. Le parti più vecchie che non entrano vengono escluse (a meno che un [riassunto](summaries.md) non le copra).
3. Il **prompt utente** con le istruzioni per la nuova parte.

*Mostra il prompt* su una qualsiasi parte mostra esattamente ciò che è stato inviato. Vedi [Prompt del libro](prompts.md) per i modelli.
