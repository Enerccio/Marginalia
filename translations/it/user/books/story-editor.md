---
label: Editor della storia
order: 90
---

# Editor della storia

La scheda **Storia** di un libro è dove si scrive. Mostra le parti del [ramo attivo](branches-and-story-tree.md)
dall'inizio alla fine.

![L'editor della storia](../../images/hero-story-editor.png)

Questa pagina descrive il layout per desktop. Su telefono la storia occupa una sola colonna e la scaletta si trova in un cassetto, vedi
[Usare Marginalia sul telefono](../phone.md#the-story).

| Area | |
|---|---|
| **Scaletta** (a sinistra) | Una voce per parte, `#1`, `#2`... con l'ID della parte nel database. Clicca una voce per scorrere fino alla parte (se non è ancora caricata, vengono caricate prima quelle che la precedono). L'intestazione indica quante parti ha il ramo. Una parte che ha un [riassunto](summaries.md) mostra una piccola icona di documento dopo la sua voce. L'estensione [Chapter Marker](../extensions/chapter-marker.md) mostra qui i titoli dei capitoli. |
| **Lorebook** (a sinistra, accanto alla Scaletta) | Una vista del lore del libro, con una casella di ricerca e la modifica rapida del testo di una voce, vedi [La scheda Lorebook](#the-lorebook-tab). |
| **Annotazioni** (a sinistra, accanto alla Scaletta) | Le [note](annotations.md) sui passaggi della storia, per l'intero libro. |
| **Pianificatore** (a sinistra, accanto alla Scaletta) | Le scene che pianifichi di scrivere, un elenco che compili a mano, vedi [La scheda Pianificatore](#the-planner-tab). |
| **Storia** (al centro) | Il testo di ciascuna parte, con il suo menu (☰) e i suoi metadati a destra, e sotto la barra inferiore. |
| **Barra inferiore** | Il pulsante **Riassunti** (icona di elenco) e il menu delle impostazioni (⚙) a sinistra, la riga di comando e il pulsante **+** a destra. |

Così appare un libro nuovo. Tutte queste sono schede che puoi disporre come preferisci, compresa la storia, vedi
[Disporre le schede](#arranging-the-tabs). Il separatore tra la barra laterale e la storia si può trascinare, e Marginalia
ricorda dove lo hai lasciato (salvato insieme al libro) e vi torna quando riapri il libro.

La storia si apre sempre in fondo, sulla parte più recente. Un libro lungo non viene disegnato tutto insieme: vengono caricate solo le ultime parti,
e quelle precedenti si aggiungono sopra man mano che scorri verso l'alto, senza spostare la parte che stai leggendo. Le parti all'estremo
opposto vengono rimosse di nuovo, così restano caricate non più di quante ne indica l'impostazione **Parti caricate contemporaneamente nella storia** del tuo
[account](../account.md) (se è vuota, vengono caricate tutte). Se scorri più velocemente di quanto arrivano le parti, un segno *Caricamento di parti precedenti* (o *successive*) sul bordo indica che ce ne sono altre. Cliccare una parte nella scaletta, oppure trovarne una con la ricerca, carica le parti intorno ad essa, e iniziare una nuova parte ti riporta alla fine della storia.

<a id="searching-the-story"></a>

### Cercare nella storia

Sopra la scaletta c'è un campo di ricerca per il testo del **ramo in cui ti trovi** (la ricerca dell'albero della storia
cerca in tutti i rami). Poiché un libro lungo non viene caricato tutto insieme, la ricerca del browser (Ctrl+F) non trova testo
nelle parti non caricate; questa sì.

- Digita ciò che cerchi; la ricerca parte quando smetti di digitare. Più parole devono essere tutte nella stessa parte; una frase
  va racchiusa tra virgolette doppie (`"old key"`), e `*` sta per un qualsiasi insieme di caratteri (`light*house`). Non conta la differenza tra maiuscole e minuscole.
- Ogni punto in cui compare una parola è una corrispondenza, e sono numerate in tutto il ramo. Sotto il campo vedi
  *Corrispondenza 3 di 27, nella parte #5* e una barra con il punto a cui sei arrivato. La storia scorre fino alla corrispondenza e carica le parti intorno
  ad essa quando non sono caricate.
- I pulsanti **su** e **giù** vanno alla corrispondenza precedente e a quella successiva (dopo l'ultima si torna alla prima).
  **Invio** nel campo fa la stessa cosa del pulsante giù, **Maiusc+Invio** come il pulsante su.
- Tutte le corrispondenze nelle parti caricate sono evidenziate nel testo, quella in cui ti trovi con un colore più intenso. Svuota il
  campo per rimuovere l'evidenziazione.

Sul telefono la ricerca si trova nel cassetto della scaletta; il cassetto si chiude quando mostra una corrispondenza, riaprilo per la successiva.

<a id="the-lorebook-tab"></a>

### La scheda Lorebook

La scheda **Lorebook** della barra laterale mostra il lore del lorebook del libro senza lasciare la storia, così non devi cambiare scheda per controllare cosa dice una voce o per correggere una parola al suo interno. Viene letta di nuovo ogni volta che torni alla scheda *Storia*, quindi le modifiche fatte nella scheda *Lorebook* del libro si vedono subito.

- La **casella di ricerca** in alto mostra solo le voci che hanno il testo nel loro nome, contenuto, nota o filtro (non conta la differenza tra maiuscole e minuscole).
- La **matita** di una voce apre una piccola finestra con il suo nome, se è utilizzata, il filtro e il modo in cui viene cercata, il contenuto
  e la nota; **Salva** li scrive (i tag, l'ordine e le impostazioni di attivazione si modificano nella scheda *Lorebook* del libro). Non si può modificare nulla
  mentre un agente lavora sul lorebook.
- Il **pulsante di espansione** (⤢) apre l'[editor del lorebook a schermo intero](../lorebooks/lorebooks.md#editing-in-full-screen) (solo desktop): quello accanto alla casella di ricerca nel lorebook, quello di una voce su quella voce, quello di un sotto-lorebook (con *Includi sotto-lorebook*) su quel lorebook. La scheda viene ricaricata quando chiudi l'editor.

- Ogni voce è una riga con il suo nome e la matita; la freccia la apre per mostrarne il **contenuto** (visualizzato come Markdown) e il **testo che la attiva** (il
  *Testo/Schema di filtro*, con una nota quando si tratta di un'espressione regolare).
- **Includi sotto-lorebook** mostra il lorebook e ciascuno dei suoi sotto-lorebook (e quelli dei sotto-lorebook, a qualsiasi profondità) come righe proprie, con le loro voci
  sotto di essi; clicca la freccia di un lorebook per nascondere le sue voci. Senza questa opzione vengono elencate solo le voci del lorebook del libro.
- Le voci e i lorebook disattivati (che non vengono usati per la storia) sono mostrati attenuati.
- Un libro senza lorebook mostra invece una nota.

![La scheda Lorebook con due voci aperte](../../images/story-lorebook-tab.png)

Come ogni scheda dell'editor, può essere trascinata fuori in un pannello proprio, vedi sotto.
<a id="the-planner-tab"></a>

### Il tab Planner

Il tab **Planner** è l'elenco delle scene che vuoi far attraversare alla storia, nell'ordine in cui le vuoi. Ogni scena è il dettaglio di un turno
che la scrive: un **titolo** per l'elenco, la **ambientazione della scena**, il **personaggio POV**, i **personaggi presenti** e le **istruzioni**, gli
stessi quattro campi del modulo del nuovo turno.

![Il tab Planner con quattro scene, la prima già scritta](../../images/planner-tab.png)

- **Aggiungi scena** apre un modulo per una nuova scena; finisce in fondo all'elenco. La penna modifica una scena, le frecce la spostano in su o in giù, il cestino la elimina.
- Il **+** nella riga di una scena **inserisce una nuova scena** subito sotto di essa, tra quella scena e la successiva.
- Il pulsante di unione di una scena **la unisce con la successiva**: si apre un modulo con i dettagli di entrambe già congiunti (i due titoli, le ambientazioni e le istruzioni una sotto l'altra, i personaggi presenti di entrambe, il personaggio POV della prima; quando la seconda ha un altro POV, il modulo lo indica). Modifica ciò che non va bene e premi **Unisci** (oppure <kbd>Ctrl/Cmd+Invio</kbd>): le due scene diventano una sola, al posto della prima. L'operazione riesce solo se entrambe erano scritte.
- **Importa dal ramo** aggiunge una scena per ogni parte del ramo su cui si trova il libro (dalla prima parte fino alla più recente) che ha dei dettagli di turno: la sua ambientazione, il personaggio POV, i personaggi presenti e le istruzioni, con la prima riga delle istruzioni come titolo. Le parti senza dettagli di turno vengono escluse, e le scene si aggiungono dopo quelle che hai già, non come completate. Usalo per pianificare la riscrittura di una storia che hai già scritto: importa, unisci i turni che appartengono insieme, inserisci ciò che manca, poi scrivi le scene su un nuovo ramo o in una copia del libro.
- La casella davanti a una scena indica che è **completata** (scritta): il titolo è barrato e la scena è attenuata. Nessuno la controlla al posto tuo: spuntala quando hai scritto la scena. La riga sopra l'elenco conta quante sono completate.
- **Clicca su una scena** per inserire i suoi dettagli nel **modulo del nuovo turno** (il modulo si apre, con l'ambientazione, il POV, i personaggi presenti e le istruzioni della scena; ciò che c'era prima viene sostituito) e premi **Genera** quando sei soddisfatto. Su telefono il cassetto si chiude prima.
- Le scene sono salvate insieme al libro, quindi sono incluse nei suoi backup e nelle sue copie. Anche un modello può aggiungere scene: il [wizard della trama](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) dell'estensione Arc Outline e un [agente](../agents.md) possono leggere e modificare il planner con i [comandi](../commands/index.md#the-planner); l'elenco mostra subito ciò che hanno aggiunto.

![Il modulo per una nuova scena](../../images/planner-scene-dialog.png)

![Un clic su una scena riempie il modulo del nuovo turno con i suoi dettagli](../../images/planner-new-turn.png)

Come ogni tab dell'editor, può essere trascinato fuori in un pannello tutto suo, vedi sotto.

<a id="arranging-the-tabs"></a>

### Disporre i tab

L'editor è un insieme di tab: la **Storia** stessa (le parti e la barra in basso), l'**Outline**, il **Lorebook** e i tab che le [estensioni](../extensions/index.md) aggiungono (Characters, Arcs, Side Query...). Trascina un tab dalla sua intestazione per riordinarli
(solo desktop; il telefono mantiene una colonna sola, con l'outline in un cassetto). Un libro nuovo ha l'Outline e il Lorebook in un pannello
a sinistra e la Storia in una colonna tutta sua a destra; puoi mettere la storia dove vuoi, per esempio al centro con
l'outline da un lato e il lore dall'altro, oppure sopra l'outline:

- Rilascialo su una delle strisce tratteggiate che compaiono sopra, tra e sotto i pannelli di una colonna per **staccarlo** in un
  pannello tutto suo in quella colonna; i pannelli di una colonna sono impilati uno sotto l'altro.
- Rilascialo su una delle strisce alte a sinistra, a destra o tra le colonne per **staccarlo di lato**: ottiene una colonna tutta sua, accanto alle altre. Allarga l'editor con i divisori per fare spazio a più colonne.

I pannelli e le colonne sono separati da divisori: trascinali per dare a ogni pannello lo spazio che ti serve. Le dimensioni vengono salvate
insieme alla disposizione.

![La barra laterale con Characters in una colonna tutta sua e Side Query sotto](../../images/story-editor-tabs.png)

- Rilascialo sull'intestazione di un altro tab o sul corpo di un altro pannello per **unirlo** a quel pannello (rilasciando su un tab si inserisce prima di quel tab, rilasciando sul corpo si inserisce per ultimo). Un pannello che perde il suo ultimo tab scompare, e con lui una colonna senza pannelli.

La disposizione e le dimensioni sono salvate insieme al libro, quindi restano le stesse la volta successiva che lo apri e in ogni browser, e lo stesso vale per il tab che ogni pannello mostra. Un tab di un'estensione non caricata mantiene il suo posto, pronto a tornare quando l'estensione è di nuovo disponibile.

La storia è sempre presente: il suo tab non può essere chiuso, e un tab che si trova in un pannello insieme ad altri tab è nascosto
mentre ne è selezionato un altro. Qualsiasi cosa mostri una parte riporta in primo piano la storia: cliccare una voce dell'outline, un risultato
della ricerca, scegliere un ramo nell'[albero della storia](branches-and-story-tree.md) e avviare una generazione selezionano prima il tab **Storia**.
Mentre una generazione prepara il suo prompt (di solito qualche secondo) gli altri tab sono disabilitati; una volta che il modello scrive, tornano utilizzabili, vedi [più sotto](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Minimizzare i tab

Ogni tab tranne la storia ha un piccolo pulsante **⌄** (freccia) accanto al suo nome. Esso **minimizza** il tab: il tab lascia il suo pannello (un pannello
con tutti i tab minimizzati scompare per il momento) e compare un'icona rotonda, un cerchio colorato con un disegno (un elenco per
l'outline, un libro per il lorebook, un fumetto per le annotazioni, una lente per Side Query, e così via; un tab di
un'estensione che non ha un disegno riceve una o due lettere), in una barra sul bordo dell'editor. Passa il mouse sull'icona per vedere di quale tab si tratta. Nulla viene chiuso: il tab mantiene ciò che mostrava. Clicca sull'icona e il tab torna nel pannello, nel posto tra gli altri tab in cui si trovava, e viene mostrato.

La barra resta finché c'è un tab minimizzato. Il pulsante in alto (le frecce circolari) sposta la barra sul lato successivo
dell'editor, in senso orario: sinistra, alto, destra, basso. La storia non può essere minimizzata, quindi sullo schermo c'è sempre qualcosa.

![Tab minimizzati nella barra sul bordo dell'editor](../../images/story-minimized-tabs.png)

Le icone sono in ordine alfabetico per nome del tab, finché non lo cambi: trascina un'icona su un'altra e prende il posto di quella. Quali tab sono minimizzati, dove si trova la barra e l'ordine delle icone sono salvati insieme al libro, insieme alla disposizione e alle dimensioni.

<a id="the-default-arrangement"></a>

### La disposizione predefinita

Un libro nuovo si apre con la disposizione standard (l'outline, il lorebook e le annotazioni a sinistra, la storia a destra). Per rendere la tua disposizione quella standard, imposta i tab come preferisci (pannelli, dimensioni, tab minimizzati, lato della barra) e scegli **Salva layout come predefinito** nel menu impostazioni (⚙) della storia. Ogni libro che non ha una disposizione propria (un libro che non hai riordinato) si aprirà allora con essa; un libro che hai disposto mantiene la sua disposizione. Il predefinito è conservato nelle tue impostazioni utente.

Per tornare alla disposizione predefinita, scegli **Ripristina layout predefinito** nel menu impostazioni (⚙) della storia e conferma. Si tratta di quella che hai salvato, oppure di quella standard se non ne hai salvata nessuna. Sposta soltanto i tab nella loro posizione originale (e dimentica le dimensioni e ciò che è minimizzato): i tuoi testi, il tab che ogni pannello mostra e i tab delle estensioni restano, questi ultimi nel primo pannello.
<a id="writing-the-next-part"></a>

## Scrivere la parte successiva

1. Clicca su **+** nell'angolo in basso a destra. Si apre il pannello *New Turn Instructions*.
2. Compila i campi. Sono tutti facoltativi, ma il modello ha bisogno di almeno qualche indicazione:

   | Campo | |
   |---|---|
   | **Scene Setting** | Dove e quando si svolge la parte, ad es. *Il faro, una notte di tempesta a fine autunno*. |
   | **POV Character** | Il punto di vista da cui è scritta la parte. |
   | **Present Characters** | Chi è nella scena, e in quale stato. |
   | **Instructions** | Cosa deve succedere in questa parte. |

   *Scene Setting*, *POV Character* e *Present Characters* sono compilati a partire dall'ultima parte, quindi cambi solo ciò che è diverso. *Instructions* è vuoto ogni volta.
3. Clicca su **Generate**.

![Il pannello New Turn Instructions](../../images/new-turn-instructions.png)

La nuova parte compare alla fine e si riempie man mano che il modello la scrive. Per i [modelli di ragionamento](../inference-providers.md#reasoning-models) il ragionamento compare per primo, sotto **View Reasoning**.

Mentre una parte viene generata, il pulsante **+** diventa un pulsante rosso **Stop**. Fermare la generazione mantiene come parte il testo scritto fino a quel momento.

<a id="while-a-part-is-generated"></a>

### Mentre una parte viene generata

Per i primi secondi, mentre il prompt viene composto a partire dal libro, l'intera finestra del libro resta in attesa. Non appena il modello inizia a scrivere, sono bloccate solo la nuova parte e le parti che la precedono:

- **La nuova parte** non può essere toccata finché non è terminata: niente modifiche, niente menu.
- **Le parti precedenti** restano dove sono: puoi modificarle, annotarle e riscriverle, ma non eliminarle, unirle o creare rami da esse, e la storia resta sul suo ramo (niente swipe, niente cambio di ramo). Una modifica a una parte precedente, o alla lore, non raggiunge la parte in scrittura: il suo prompt è già stato composto. Vale per ciò che viene dopo.
- **Tutto il resto è libero**: la lore, le impostazioni e il prompt del libro, le schede della barra a sinistra (side query, arc outline, note...), la barra dei comandi. Una richiesta al modello attende una connessione libera del modello se la generazione ne sta usando l'unica disponibile.
- **In attesa**: una seconda parte, la ricerca e sostituzione, un import nel libro e l'eliminazione di massa. Anche le schede dell'albero e del backup attendono.

Ciò che la finestra oscura viene rifiutato anche quando un comando, un agente o un'estensione lo richiede: Marginalia indica cosa lo impedisce (*This is in use: …*).

**Uscire dal libro interrompe la generazione.** Il pulsante di uscita chiede prima conferma; *Yes* equivale a **Stop**: ciò che è arrivato viene conservato. Chiudere la scheda del browser fa lo stesso, e il browser chiede conferma prima di chiudere o ricaricare la scheda mentre una parte è in generazione. Una breve perdita della rete, o un portatile chiuso per un minuto, non equivalgono a uscire: la pagina torna e mostra ciò che nel frattempo è arrivato.

Quando il modello restituisce un errore (server non raggiungibile, chiave API errata, contesto troppo lungo...), l'errore viene mostrato. Se non è ancora arrivato alcun testo, la storia resta com'era: una nuova parte o uno swipe viene rimosso, e una parte rigenerata riottiene il testo precedente. Il testo arrivato prima dell'errore viene conservato, come con **Stop**. Risolvi il problema e genera di nuovo.

Il modo in cui i campi entrano nel prompt è stabilito dallo [User Prompt](prompts.md#user-prompt) del libro. Con lo user prompt predefinito, il modello riceve i campi come *Current Location & Time*, *Point of View Character*, *Characters Currently Present / State* e *Plot Specifications for the Next Part*.

!!!
Il testo è scritto in Markdown: `*italics*`, `**bold**`, `# Heading` e così via sono mostrati formattati. Una parte con un'intestazione `#` è un capitolo per l'estensione [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Redigere le istruzioni

Non sai cosa dovrebbe succedere dopo? **Draft Instructions**, sotto il campo *Instructions* del pannello, lascia che il modello scriva le istruzioni al posto tuo, come le scriveresti tu. Si apre una finestra con:

| | |
|---|---|
| **Guidance** | Di cosa dovrebbe trattare la parte: un'idea di massima, una direzione ("lei lo affronta per la lettera"), oppure nulla, e il modello prosegue la storia dove essa porta. Inizialmente contiene ciò che hai già scritto in *Instructions*. |
| **Plan of the Story** | Solo quando qualcosa ne fornisce uno. Lo fa l'[Arc Outline](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter), con i dati del capitolo corrente. Sola lettura; il modello lo segue. |

**Draft Instructions** avvia il modello e trasmette la risposta in **Drafted Instructions**, che puoi modificare o svuotare prima di proseguire; **Draft Again** tenta di nuovo, **Stop** conserva ciò che è arrivato. Nulla cambia finché non clicchi **Use as Instructions**: il testo sostituisce allora il campo *Instructions* del pannello (gli altri campi non vengono toccati) e il pannello si riapre, pronto per **Generate**.

Il modello riceve la storia finora (la parte più vecchia come [summaries](summaries.md), come in una generazione), l'ambientazione della scena, il POV e i personaggi presenti che sono nel pannello, la lore dell'ultima parte, le tue ultime istruzioni come esempi del tuo modo di scrivere, la tua guida e il piano. Non vede le voci della lorebook che una nuova parte attiverebbe. La richiesta è il [prompt di impersonificazione](prompts.md#impersonate-prompt). Il modello è quello del libro; redigere le istruzioni consuma token come qualsiasi richiesta.

![Redazione delle istruzioni del turno successivo](../../images/impersonate-dialog.png)

Lo stesso vale per il comando [`/impersonate`](../commands/index.md), che restituisce il testo. Gli [agenti](../agents.md) non lo ricevono: scrivono la storia con istruzioni proprie.

<a id="the-settings-menu"></a>

## Il menu delle impostazioni

Il menu delle impostazioni (⚙) nella barra in basso raggruppa ciò che agisce sull'intera storia. **Search and replace** è in cima; il resto è nei sottomenu (anche su telefono, tranne le voci solo desktop):

| Sottomenu | Voci |
|---|---|
| **Appearance** | **Change Styles For Text**, **Highlight Generated Text** e **Annotations in the margin** (solo desktop, l'ultima) |
| **Analysis** | **Annotate a range of parts with AI**, **Create summaries**, **Take lore notes with an agent**, **Automatic annotation**, **Phrase watch list**, **Branch Tokens**, **Lore Usage**, **Statistics** |
| **Import & export** | **Import**, **Export story** (e le voci delle estensioni che importano o esportano, come **Export instructions**) |
| **Cleanup** | **Clear history of the book**, **Empty trash of the history**, **Mass delete** |
| **Layout** (solo desktop) | **Save layout as my default**, **Restore default layout** |

Le voci portate dalle [estensioni](../extensions/index.md) si trovano nel sottomenu che le riguarda, oppure in fondo al menu. Le scorciatoie delle voci sono in *Settings → Keyboard shortcuts*, dove una voce è indicata con il suo sottomenu (*Analysis › Statistics*). Le sezioni seguenti indicano dove si trova ciascuna voce.
<a id="the-part-menu"></a>

## Il menu della parte

Ogni parte ha un menu (☰) nell'angolo in alto a destra. **Edit**, **Discard Changes**, **Regenerate**, **Swipe**, **Redraft**, **History** e **Images** si trovano nel menu stesso, **Delete** è la sua ultima voce, e il resto si trova in quattro sottomenu: **Annotations** (*Annotate part*, *Annotate with AI*), **Summary** (*Generate summary* / *View summary*, *Delete summary*), **Inspect** (*Show Prompt*, *Compare Prompt*, *Token Usage*, *Lore Usage*) e **Branch & merge** (*Branch Story*, *Merge into previous part*). Un sottomenu senza voci per la parte non viene mostrato. Il Reviewer aggiunge il suo sottomenu **Review** dopo *Summary*. Le voci:

| Voce | |
|---|---|
| **Edit** | Trasforma il testo in un campo modificabile. **Save** (la stessa voce di menu) lo salva. Avviare una generazione o lasciare la scheda salva anche le modifiche aperte. |
| **Discard Changes** | Disponibile solo mentre il testo è in modifica: chiude il campo senza salvare, e la parte mantiene il testo salvato l'ultima volta. Se hai modificato il testo, viene chiesta conferma. |
| **Regenerate** | Riscrive l'ultima parte, sostituendone il testo. Solo sull'ultima parte. |
| **Swipe** | Scrive un'altra versione dell'ultima parte e conserva quella vecchia come ramo separato. Solo sull'ultima parte. Vedi [Branches & story tree](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Redraft** | Fa riscrivere questa parte al modello in base alle tue istruzioni, su qualsiasi parte, vedi [sotto](#redrafting-a-part). Non mentre il testo è in modifica. |
| **History (n)** | Le [versioni del testo](version-history.md) della parte, con il numero di versioni: confrontarle, ripristinarne una, dare un nome a una. Non mentre il testo è in modifica. |
| **Annotate part** | Crea una [nota](annotations.md#making-an-annotation) sull'intera parte. Per annotare delle parole, selezionale e fai clic destro (**Annotate selection**). |
| **Annotate with AI** | Fa rivedere la parte a un modello, che [propone note](annotations.md#letting-a-model-annotate). Non mentre il testo è in modifica. Per più parti insieme usa **Annotate a range of parts with AI** nel menu delle impostazioni (⚙), e per ogni nuova parte **Automatic annotation** (vedi [Annotations](annotations.md#annotating-every-new-part-automatically)). |
| **Branch Story** | Avvia un nuovo ramo da questa parte, vedi [Branches & story tree](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Merge into previous part** | Aggiunge il testo di questa parte alla parte precedente, vedi [sotto](#merging-a-part-into-the-previous-one). Non sulla prima parte. |
| **Show Prompt** | Mostra il prompt esatto inviato al modello per questa parte. |
| **Token Usage** | Mostra come è stato usato il contesto del modello per questa parte: un grafico delle sezioni del prompt (istruzioni del modello, lore, riassunti, storia finora, tue istruzioni, spazio riservato alla risposta) e la dimensione di ogni messaggio inviato al modello. Se parte della storia non è entrata, indica quante parti sono state escluse. Le parti scritte prima che questo venisse registrato non hanno il grafico. |
| **Lore Usage** | Elenca le voci del [lorebook](../lorebooks/index.md) entrate nel prompt di questa parte, con i token che ciascuna ha consumato e ciò che l'ha trovata (il prompt, oppure il contenuto di un'altra voce quando il lorebook scansiona più in profondità). Le parti scritte a mano, quelle scritte prima che questo venisse registrato o quelle scritte per un libro senza lorebook indicano che non è stato registrato nulla. Per l'intero libro usa **Lore Usage** nel menu delle impostazioni (⚙), vedi [sotto](#which-lore-entries-were-used). |
| **Images** | Allega immagini alla parte, vedi [Images](#images). |
| **Generate summary** / **View summary** | Riassume la storia fino a questa parte, oppure ne mostra il riassunto, vedi [Summaries](summaries.md). |
| **Delete summary** | Rimuove il riassunto di questa parte. Per un [meta riassunto](summaries.md#meta-summaries) chiede se scioglierlo o eliminarlo completamente. |
| **Delete** | Elimina la parte, vedi [sotto](#deleting-a-part). |

*Regenerate* e *Swipe* usano i dettagli del turno dell'ultima parte (vedi [sotto](#turn-details)). Per cambiare ciò che dovrebbe succedere, modificali prima.

![Il menu di una parte, con il sottomenu Inspect aperto](../../images/story-part-menu.png)

![Token Usage di una parte](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadati della parte

Accanto a ogni parte:

| | |
|---|---|
| **Order #n \| DB ID: m** | La posizione della parte nel ramo, e il suo ID nel database. |
| **Model** | Il provider di inferenza che ha scritto la parte. |
| **Tokens: x (y)** | `x` token di testo nella parte, `y` token nel prompt che l'ha prodotta. |
| **Words** | Parole nella parte. |
| **Turn Details** | Le istruzioni da cui è stata scritta la parte. |

<a id="turn-details"></a>

### Dettagli del turno

**Turn Details** apre *Scene Setting*, *POV Character*, *Present Characters* e *Instructions* da cui è stata scritta la parte. Per l'ultima parte possono essere modificati (le modifiche vengono salvate subito) e sono usati da *Regenerate* e *Swipe*. Per le parti precedenti sono di sola lettura.

![Dettagli del turno di una parte](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Modificare il testo

Il testo generato è tuo da modificare. Scegli **Edit** nel menu della parte, cambia il testo e scegli **Save**. Le parti modificate vengono inviate al modello così come le hai modificate.

Modificare una parte coperta da un [riassunto](summaries.md) rende quel riassunto non aggiornato: Marginalia se ne accorge alla generazione successiva e lo elimina. Perché non accada all'improvviso, **Save** controlla prima il nuovo testo: quando renderebbe non aggiornati dei riassunti, appare una finestra che indica quanti sono e propone **Back to editing** (non salva nulla, e l'editor resta aperto con il tuo testo), **Save anyway** (i riassunti diventano non aggiornati) e **Save and revalidate summaries** (il testo viene salvato e i riassunti restano validi, per una modifica che non cambia ciò che dicono, come un refuso). Il controllo viene fatto per **Save** (il menu, la scorciatoia e il pulsante dell'editor); i salvataggi automatici, per esempio quando avvii una generazione con l'editor aperto, non chiedono nulla.

<a id="keyboard-shortcuts"></a>

## Scorciatoie da tastiera

Su un computer (il [layout per telefono](../phone.md) non le ha) il ciclo di scrittura funziona da tastiera:

| Tasto | |
|---|---|
| `+` | Apre il pannello *New Turn Instructions* con il cursore in *Instructions*. |
| `Ctrl+Enter` | Genera dal pannello aperto. `Esc` lo chiude; la tastiera torna alla storia. |
| `Ctrl+.` | Ferma la generazione in corso. |
| `s` / `r` | **Swipe** / **Regenerate** l'ultima parte. |
| `e` | Apre l'editor della parte più visibile sullo schermo (l'ultima parte, se nessuna lo è). |
| `Ctrl+S` | Salva la parte in modifica (anche mentre il cursore è nel suo testo). |
| `/` | Porta il cursore nella barra dei comandi (prima viene mostrata la scheda della storia). |
| `Ctrl+Shift+F` | Apre la scheda *Outline* e porta il cursore nella ricerca del ramo (`Enter` / `Shift+Enter` vanno alla corrispondenza successiva / precedente). |
| `Ctrl+Shift+H` | Apre la finestra *Search and replace* del libro. |
| `a` | **Annotate selection**: crea una nota sul testo selezionato in una parte. |
| `Alt+PageDown` / `Alt+PageUp` | Mostra la scheda successiva / precedente, scorrendo le schede di tutti i fogli nell'ordine del layout e ricominciando da capo. |

Un tasto senza `Ctrl`, `Alt` o `Meta` (come `+`, `s`, `r`, `e`, `a`, `/`) funziona solo quando nessun campo di testo ha il focus, così non interferisce mai con la digitazione. Le scorciatoie funzionano nella scheda della storia del libro aperto e non quando un'altra finestra è aperta sopra di essa. I pulsanti mostrano il loro tasto nel suggerimento, e il menu dell'ultima parte lo mostra dopo *Edit*, *Regenerate* e *Swipe*. Su un Mac `Cmd` funziona dove un tasto dice `Ctrl` (a meno che tu non abbia assegnato quel tasto `Cmd` a un'altra azione). Puoi cambiare ogni tasto in [Settings → Keyboard shortcuts](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Eliminare una parte

**Delete** rimuove la parte dalla storia. Le parti che la seguivano restano e continuano dalla parte precedente, quindi eliminare una parte nel mezzo della storia la toglie semplicemente. Eliminare l'ultima parte fa sì che la parte prima di essa diventi la nuova fine del ramo.

<a id="deleting-many-parts-at-once"></a>

### Eliminare più parti insieme

Il menu delle impostazioni (⚙) ha **Mass delete**. Attivandolo compare una casella di controllo a sinistra di ogni parte, e sotto la storia appare una barra con il numero di parti selezionate e un pulsante **Delete**. Seleziona le parti che vuoi eliminare: le selezioni restano mentre scorri, perché la storia carica solo una porzione delle parti. **Shift + click** su una casella seleziona (o deseleziona) tutte le parti comprese tra quella e l'ultima su cui hai cliccato; una parte selezionata è evidenziata in rosso. Poi premi **Delete**: dopo una conferma le parti vengono eliminate una alla volta, con una barra di avanzamento che non può essere interrotta. Vengono eliminate come una singola parte: le parti che seguivano una parte eliminata continuano dalla parte precedente, e le parti eliminate finiscono nel cestino. Poi la storia viene mostrata di nuovo e la modalità di eliminazione multipla si disattiva. **Cancel** nella barra, o scegliere di nuovo **Mass delete**, disattiva la modalità senza eliminare nulla. Avviare una generazione disattiva anch'essa la modalità. Una parte selezionata che una finestra o una traduzione sta elaborando resta nella storia, e Marginalia lo segnala.

![Cancellazione multipla con alcune parti selezionate](../../images/story-mass-delete.png)

<a id="redrafting-a-part"></a>

## Riscrivere una parte

**Regenerate** e **Swipe** lavorano solo sull'ultima parte e la scrivono da zero. **Redraft** lavora su qualsiasi parte, anche una vecchia nel mezzo della storia, e conserva ciò che non chiedi di cambiare. Scegli **Redraft** nel menu della parte:

1. Scrivi cosa deve cambiare in **Instructions for the redraft**: *aggiungi più dialogo tra i due*, *allunga il combattimento*, *elimina la spiegazione della mappa*, *lascia il finale com'è*. Se le istruzioni sono vuote, la parte viene riscritta con gli stessi eventi.
2. Scegli **Length** se la parte deve diventare **Shorter** o **Longer** (il valore predefinito è **Same length**): una frase che lo richiede viene aggiunta dopo le tue istruzioni (vedi [Prompt di redraft più breve e Prompt di redraft più lungo](prompts.md#redraft-shorter-and-longer)). Scegli **Redraft**. Il testo arriva in **Redrafted text**; **Stop** lo interrompe prima della fine. Scegli **Redraft again** per un altro tentativo, anche con istruzioni diverse. **Differences** mostra, parola per parola, cosa è cambiato rispetto alla parte. Quando la parte ha [annotazioni](annotations.md) aperte, **Deal with the open annotations of the part** (selezionato all'inizio) le passa anch'esso al modello, e **Apply** le contrassegna come risolte.
3. Il testo riscritto è un campo che puoi ancora modificare (la sua icona in basso a destra lo fa crescere fino a contenere tutto il testo), e **Differences** segue le tue modifiche. **Apply** sostituisce il testo della parte con quello; **Close** lo scarta. Niente nel libro cambia prima di **Apply**.

![La finestra di redraft: le istruzioni, il nuovo testo e le differenze](../../images/redraft-dialog.png)

Il modello riceve i [prompt di redraft](prompts.md#redraft-prompt): la parte, le tue istruzioni, la storia prima della parte (quella più vecchia sotto forma di [riassunti](summaries.md) dove ci sono riassunti validi, il resto come testo, con la parte più recente per prima quando non c'è spazio per tutto), quanta più storia dopo la parte possibile (fino a un quarto dello spazio disponibile), il lore che i lorebook danno ora a quella parte (ricalcolato per il turno della parte, così una modifica a una voce arriva al redraft; il lore con cui la parte era stata scritta viene usato solo se il libro non ha un lorebook), il punto di vista, il tempo verbale e lo stile del libro, e l'ambientazione della scena, il personaggio con il punto di vista, i personaggi presenti e le istruzioni del turno da cui proveniva la parte. La storia dopo la parte serve perché il nuovo testo continui a portare a ciò che segue: gli eventi su cui si basano le parti successive dovrebbero restare.

La parte mantiene il suo posto, il suo riassunto, le sue immagini e il modo in cui è stata generata; il testo, le sue parole e i suoi token vengono sostituiti e la parte risulta modificata. Applicare il redraft è una modifica come **Save** nell'editor, quindi i [riassunti](summaries.md) che descrivono il vecchio testo diventano non aggiornati, e Marginalia indica quanti sono. Un riassunto della parte stessa è uno di questi.

Quando un riassunto descrive la parte (ce l'ha la parte stessa, oppure una parte più recente del ramo), la finestra ha la casella **Revalidate the summaries this redraft makes outdated**, inizialmente deselezionata. Selezionala quando i riassunti dicono ancora la cosa giusta (il redraft ha cambiato le parole, non ciò che accade): **Apply** allora ricalcola il loro hash e restano validi. Lasciala deselezionata quando gli eventi sono cambiati, e rifai i riassunti.

La finestra non blocca la storia, vedi [Lavorare accanto alla finestra](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Lavorare accanto alle finestre di redraft e annotazione

Su computer le finestre **Redraft** e **Annotate with AI** stanno a destra della storia e la lasciano utilizzabile: puoi leggere, scorrere e modificare la storia mentre il modello scrive. Trascina una finestra per il titolo per spostarla, o per il bordo per ridimensionarla. La freccia (⌄) nel titolo ripiega una finestra nel [pannello delle esecuzioni](#the-panel-of-the-runs) in basso a sinistra della pagina: il modello prosegue, **Open** (o un clic sulla voce) riporta la finestra, e **Stop** ferma il modello (una volta terminato, **Close** chiude la finestra). Su telefono le finestre continuano a coprire la storia, ma si ripiegano allo stesso modo.

![La finestra di redraft accanto alla storia mentre il modello scrive, e l'esecuzione nel pannello in basso a sinistra](../../images/redraft-beside-story.png)

**Close e Stop sono due cose diverse.** **Stop** nella finestra ferma il modello e la finestra resta con ciò che è arrivato. **Close** mentre il modello lavora non lo ferma: la finestra si ripiega nel pannello e il modello prosegue nella sua coda; la sua risposta è nella finestra quando la riapri. Chiudi una finestra il cui modello è terminato, oppure usa **Close** nel pannello, e sparisce; chiudere il libro o la scheda del browser ferma anche una finestra ripiegata. Lo stesso vale per le altre finestre che attendono un modello: un **summary** (e *Create summaries*, *Rewrite all summaries*), il [Reviewer](../extensions/reviewer.md), la descrizione del libro, i generatori di lore e di outline, i generatori di immagini e manga, e lo scrittore di persone di Beta Readers. Le finestre *Studio* e *Convert* dell'audiolibro non bloccano nemmeno loro la pagina; chiuderle non ha mai fermato la conversione.

Mentre una finestra è aperta (o ripiegata) su una parte, la parte mostra cosa sta facendo accanto al proprio menu (*Redrafting · The book*), e **Regenerate**, **Swipe**, **Redraft**, **Annotate with AI**, **Merge into previous** e **Delete** sono disattivati per quella parte, così la finestra non perde la sua parte. Modificare la parte resta possibile. Chiudere il libro chiude le sue finestre e ferma i loro modelli. Una parte su cui sta lavorando un [traduttore](../extensions/real-language-translator.md) mostra *Translating the speech of the part...* allo stesso modo, e due traduttori non lavorano mai sulla stessa parte contemporaneamente: il secondo segnala che la parte è in uso.

- Quando **Apply** un redraft dell'intera parte e la parte è stata cambiata dopo l'apertura della finestra (l'hai modificata nel frattempo), Marginalia chiede prima conferma: **Differences** confronta allora la parte com'è ora con il redraft, così vedi cosa verrebbe sostituito; scegli **Apply anyway** o **Cancel**. Un redraft che [si applica da solo](annotations.md#annotating-every-new-part-automatically) non lo fa quando la parte è stata modificata: la finestra attende invece che tu intervenga.
- Quando la parte è stata eliminata prima che tu **Apply** il redraft o aggiunga le annotazioni, la finestra lo segnala, non scrive nulla e si chiude.
<a id="the-panel-of-the-runs"></a>

### Il pannello delle esecuzioni

Tutto ciò che sta lavorando sui tuoi libri con un modello in questo momento è elencato in basso a sinistra di ogni pagina: una generazione, una riscrittura o un'annotazione, una traduzione del [Real Language](../extensions/real-language-translator.md) o del [Conlang](../extensions/conlang-translator.md), una riscrittura dei riassunti, il Knowledge Tracker che legge la storia, una conversione in [audiolibro](../extensions/audiobook.md), un comando della barra dei comandi che richiede tempo (*Draft instructions*, un riassunto...: la sua finestra sta a lato e si ripiega come le altre), un [agente](../agents.md) al lavoro. Sono elencate anche le finestre in cui attendi una risposta mentre il modello risponde: **Impersonate**, l'assistente di aiuto, una traduzione rapida, il progettista di Conlang, una chat di Side Query (e la sua denominazione della scheda), Beta Readers o il plot wizard. Il pannello elenca le tue esecuzioni da **tutte le pagine aperte** (un'altra scheda, il telefono), non solo quelle avviate su questa pagina. Se non c'è nulla in esecuzione, non si mostra nulla. La finestra dei Knowledge e State Tracker è anch'essa una voce del pannello: chiusa mentre il modello lavora, si ripiega al suo interno.

![Il pannello delle esecuzioni aperto: la riscrittura ripiegata al suo interno, con la fine del testo del modello, Open e Stop](../../images/runs-panel.png)

- **Closed**: è una piccola barra con la prima esecuzione, quante ne restano (*Translating the speech of the part... · 2 more*) e una barra in movimento mentre un modello lavora. Fai clic per aprire l'elenco; si apre da solo quando ripieghi una finestra nel pannello.
- **Open**: ogni esecuzione mostra cosa fa, il libro, da quanto tempo è in corso, che cosa attende e la fine di ciò che scrive il modello:
  - *Waiting for the model: 2 requests before this one* quando il modello è occupato con altre richieste (vedi *Connections* in [Fornitori di inferenza](../inference-providers.md));
  - *The model is thinking...* oppure *The model is writing...*, con sotto la fine del suo ragionamento o della sua risposta;
  - *Waits for you* quando l'esecuzione attende una tua decisione (una riscrittura da applicare, una proposta del Knowledge Tracker da rivedere, la domanda del Prompt Inspector prima di una generazione, un comando del plot wizard da autorizzare).
- **Stop** interrompe l'esecuzione, ovunque sia stata avviata. **Open**, o un clic sulla voce, mostra di nuovo una finestra ripiegata (solo nella pagina in cui era stata aperta) oppure l'esecuzione stessa; diventa **Review** quando l'esecuzione attende te. Un'esecuzione del Knowledge Tracker apre il suo libro su questa pagina (quando qui non è aperto un altro libro; altrimenti ti viene chiesto di chiudere prima quello), passa alla scheda Knowledge e mostra la revisione. Lo **Stop** di un agente lo mette in pausa. **Close** chiude una finestra in modo definitivo (e interrompe il suo modello).
- **What ended** resta nell'elenco per due ore, sotto le esecuzioni in corso, con l'esito (*Completed*, *Failed*) e la durata: una traduzione, i riassunti, ciò che segue una parte, la sessione di un agente, un'importazione, un lavoro che è proseguito dopo che hai lasciato la sua pagina. **Close** lo rimuove dall'elenco. Una generazione della storia e le esecuzioni che hai interrotto non vengono conservate.
- Su un computer puoi spostare il pannello tramite la sua maniglia (⋮ sulla sinistra); resta dove lo lasci, in questo browser. Nella metà inferiore della pagina si allarga verso l'alto quando lo apri, come nel suo angolo, e rimane sempre entro i limiti della pagina. Quando si trova nel punto del pulsante tondo dell'[assistente di aiuto](../administration/help-assistant.md) (su un libro, entrambi sono nell'angolo in basso a sinistra), il pulsante si sposta di lato e torna quando lo spazio si libera.

Quando un'esecuzione che hai avviato termina dopo la chiusura della sua pagina, un'altra tua pagina aperta invia la sua [notifica](../account.md#notifications) (per le esecuzioni che ne prevedono una, come la riscrittura dei riassunti).

**Cosa viene rifiutato mentre qualcosa è in esecuzione.** La parte successiva non viene generata mentre è aperta una finestra di [riscrittura](#redrafting-a-part) del libro (chiudila, o applicala, prima) oppure mentre un comando della barra dei comandi sta per generare. Una parte non viene unita a quella precedente mentre una finestra o una traduzione lavora su quest'ultima, e la ricerca e sostituzione salta la parte che una generazione sta scrivendo (viene contata come saltata). Un [agente](../agents.md) non parte su un libro aperto in una finestra o su cui sta lavorando qualcos'altro. Quando più esecuzioni attendono lo stesso modello, la tua generazione successiva passa prima dell'annotazione, della traduzione o dell'audio della parte precedente.

<a id="redrafting-only-a-passage"></a>

### Riscrivere solo un passaggio

Per riscrivere alcune frasi lasciando il resto della parte invariato, selezionale nel testo della parte e fai **clic destro** (una pressione prolungata su un telefono): il menu contiene **Redraft selection**. (Il menu ☰ della parte non è disponibile per questo, perché aprirlo può far perdere la selezione in alcuni browser.) La finestra funziona come sopra con due differenze: mostra il **Selected text**, e la riscrittura è solo il testo che sostituisce la selezione. Il modello riceve l'intera parte con la selezione evidenziata, insieme alla storia e al lore intorno, così il nuovo passaggio si raccorda alle frasi prima e dopo. **Differences** confronta la selezione con il testo riscritto; **Apply** colloca il testo riscritto al posto della selezione e mantiene il resto della parte, compresi lo spazio o l'a capo ai bordi della selezione. Con le [annotazioni](annotations.md) aperte, la casella di controllo offre solo quelle il cui passaggio si sovrappone alla selezione. Se la parte è stata modificata dopo che hai selezionato il testo (in un'altra finestra), **Apply** rifiuta l'operazione e devi selezionare di nuovo. Mentre la parte è in modifica si usa la selezione nell'editor, e la modifica viene salvata prima.

<a id="what-follows-a-generated-part"></a>

### Cosa segue una parte generata

Quando una parte che hai generato (dalla pagina o dalla barra dei comandi) è completa, il libro può fare altro da solo, in quest'ordine:

1. **Annotate**: l'[annotazione automatica](annotations.md#annotating-every-new-part-automatically) rivede la parte.
2. **Mark phrases**: la [lista di frasi da controllare](annotations.md#watching-for-phrases) annota le frasi che trova (senza modello).
3. **Redraft**: la parte viene riscritta per tenere conto delle note.
4. **Translate**: i traduttori [Real language](../extensions/real-language-translator.md) e [Conlang](../extensions/conlang-translator.md) traducono i suoi discorsi.
5. **Knowledge**: il [Knowledge Tracker](../extensions/knowledge-tracker.md) la legge.
6. **Audio**: l'[audiolibro](../extensions/audiobook.md) la trasforma in audio.

Vengono eseguiti solo i passi che il libro ha attivato. Girano sul server, quindi la pagina può essere chiusa, e formano un'unica voce nel [pannello delle esecuzioni](#the-panel-of-the-runs): *After part 14: Annotate ✓ · Redraft … · Translate*, con il testo del modello e un solo **Stop**. Ciò che cambia il testo viene prima, così il tracker e l'audio ricevono il testo definitivo. Un passo che attende te (note o una riscrittura da scegliere) apre la sua finestra, e la voce ha **Review** per aprirla in un'altra pagina.

- **Stop**, la chiusura di una finestra che attende te, oppure un passo che fallisce interrompe la sequenza lì: ciò che è stato fatto resta, il resto non viene eseguito, e un messaggio indica cosa resta da fare a mano (ogni passo ha la sua azione sulla parte).
- Mentre la **riscrittura** è in corso, la parte successiva non può essere generata (ti viene detto quale esecuzione lo impedisce); mentre gli altri passi sono in corso, invece, può essere generata.
- La parte non può essere eliminata finché il suo testo non è definitivo; generarla di nuovo interrompe ciò che ne seguiva la versione precedente.
- Un [agente](../agents.md) esegue questi passi con i propri comandi: nulla segue una parte generata da un agente.
<a id="merging-a-part-into-the-previous-one"></a>

## Unire una parte con quella precedente

Un modello che si è interrotto a metà di una frase (per il limite di risposta o per una connessione caduta) non può essere affidabilmente invitato a "continuare" lo stesso testo. Scrivi invece la parte successiva (*continua da dove si era fermata l'ultima parte*) e unisci le due con **Merge into previous part** nel menu della seconda. Si apre una revisione:

- **Join the texts with**: una riga vuota, un a capo, uno spazio oppure nulla. È preselezionato in base alla fine del testo precedente: un paragrafo dopo un testo che terminava una frase, uno spazio dopo uno che era stato interrotto.
- La **scene setting**, il **POV character**, i **present characters** e le **instructions** di entrambi i turni, combinati in campi che puoi modificare. Ciò che i due turni hanno in comune all'inizio viene mantenuto una sola volta ("Write the duel. Keep it short" e "Write the duel. Add rain" danno il primo, e "Add rain" sulla riga successiva).
- **Data**: ciò che le due parti conservano, valore per valore (per esempio le [variabili](../templates/macros.md) della storia e i dati delle estensioni), con l'indicazione se il valore è uguale, diverso o presente solo su una delle parti. La parte unita conserva il valore della **parte successiva** (è il punto in cui si trova ora la storia), a meno che tu non scelga per essa il valore della parte precedente oppure non lasci il valore fuori. I valori uguali sono nascosti, a meno che tu non li chieda.

![La revisione prima che una parte venga unita a quella precedente](../../images/part-merge-dialog.png)

La parte precedente conserva il suo posto, il suo riassunto e il modo in cui è stata generata (prompt, modello, tempi); riceve il testo unito, il ragionamento unito e le immagini, e le sue parole e i suoi token vengono ricalcolati. La parte unita viene eliminata (finisce nel [Cestino](../trash.md)) e ciò che la seguiva continua dalla parte precedente. I riassunti che coprono le due parti diventano obsoleti, come dopo una modifica. La [cronologia](version-history.md#backups-copies-and-the-trash) della parte unita viene rimossa, e la parte superstite ha il testo unito come versione **Merged**. Una parte non può essere unita quando è la prima del libro, o quando la parte che la precede ha altre continuazioni (swipe della parte): eliminale o trasformale prima in un ramo.

<a id="images"></a>

## Immagini

Una parte può avere immagini: una mappa, il ritratto di un personaggio, un'illustrazione. Scegli **Images** nel menu della parte per aprire l'elenco delle immagini della parte.

![La finestra Images di una parte](../../images/images-dialog.png)

- **Add image** (o trascina un file sopra) carica un'immagine PNG, JPEG, GIF o WebP fino a 10 MB. Le immagini WebP vengono convertite in PNG al caricamento, perché i file Word e PDF non possono contenere WebP.
- **Caption** viene mostrata sotto l'immagine. Può essere vuota.
- **Position in the part** è una griglia 3x3. La colonna è il lato del testo (sinistra, centro, destra), la riga è il punto nel testo: in alto è prima del testo, al centro è a metà del testo, in basso è dopo il testo. Clicca su un riquadro per scegliere.
- **Text flows around the image** fa scorrere il testo attorno all'immagine invece che sopra e sotto di essa. A sinistra o a destra, il testo scorre accanto all'immagine. Nella colonna centrale il testo è su entrambi i lati: alcuni blocchi di testo (circa 400 caratteri, almeno due blocchi) a quella riga diventano due colonne, la prima parte a sinistra dell'immagine e il resto a destra, così il testo si legge scendendo la colonna di sinistra e prosegue in quella di destra. Due immagini nello stesso punto stanno una sopra l'altra tra le colonne. Quando il testo non ha spazio per due colonne (un solo paragrafo, oppure i blocchi sono già occupati dalla fascia sopra), l'immagine è un blocco a sé, centrato.
- **Width** è la larghezza dell'immagine in percentuale della larghezza del testo. **Automatic** mantiene la dimensione dell'immagine (mai più larga del testo; un'immagine con testo su entrambi i lati è al 30 %). Un'immagine accanto alla quale scorre il testo arriva al massimo al 60 %, e una con testo su entrambi i lati al massimo al 40 %, così il testo conserva un po' di spazio. L'altezza segue le proporzioni dell'immagine.
- Il pulsante del cestino rimuove l'immagine dalla parte.

![La posizione di un'immagine: al centro, con il testo che scorre attorno ad essa](../../images/images-position.png)

**Preview as in the viewer** (un pulsante della finestra, e la stessa voce nel menu della parte quando la parte ha immagini) mostra la parte come la mostra il [visualizzatore](publishing.md), con lo stile del testo del libro: le immagini dove le hai messe e un interruttore per schermo largo o stretto (telefono). Usa il testo dell'editor così com'è ora, salvato o no.

![L'anteprima: il testo su due colonne con l'immagine in mezzo](../../images/images-preview.png)

L'editor della storia non usa la posizione: mostra le immagini sotto il testo della parte, come elenco. La posizione è usata dal visualizzatore e dalle [esportazioni](export.md#images).

Il punto del testo in cui va un'immagine non dipende da numeri salvati insieme ad essa: il testo viene tagliato tra i suoi blocchi (paragrafi, elenchi, citazioni e blocchi di codice non vengono mai tagliati), così le immagini restano dove devono stare quando modifichi il testo. Il centro è il punto tra i blocchi più vicino al centro del testo; un'immagine attorno alla quale il testo scorre in basso viene messa prima dell'ultimo blocco, in modo che almeno quel blocco scorra accanto ad essa. Le immagini nello stesso punto restano nell'ordine dell'elenco; quelle affiancate nello stesso punto (e che non fanno scorrere il testo attorno a sé) stanno in fila quando ci stanno. PDF e Word lo fanno per le immagini che hanno una larghezza.

Le immagini **non fanno parte del testo**. Il modello non le vede mai, non sono nel prompt, non contano come token e non modificano i [riassunti](summaries.md). Non sono nemmeno Markdown: un collegamento `![](...)` scritto nel testo non è un'immagine della parte; usa invece **Images**.

Da sapere:

- **Regenerate** mantiene le immagini della parte, anche quando la generazione fallisce. **Swipe** scrive una nuova parte (una nuova versione accanto a quella vecchia), che parte senza immagini. **Branch Story** copia le immagini nella nuova parte.
- Le immagini appartengono al tuo account. Nel [visualizzatore](publishing.md) le vedono anche i lettori di un libro pubblicato, a meno che tu non deselezioni **Show the images to readers** nella scheda *About* del libro.
- Un [backup](backups.md) ricorda quali immagini ha una parte, ma non i file delle immagini. Ripristinare un backup in un altro account o in un'altra installazione dà parti senza le loro immagini.
- Rimuovere un'immagine da una parte la toglie soltanto da quella parte. Il file resta e continua a essere elencato in [Risorse](../resources.md), dove puoi scaricarlo, sostituirlo o eliminarlo. Eliminarlo lì lo rimuove da ogni parte che lo usa. Il file stesso resta nella cartella dei dati.

<a id="summaries"></a>

## Riassunti

Il pulsante **Summaries** nella barra in basso (a sinistra del menu delle impostazioni ⚙) apre la panoramica dei riassunti del ramo, dove possono essere modificati, eliminati e uniti in meta-riassunti. Il suggerimento del pulsante mostra quanti token occupano nel prompt i riassunti in uso. Vedi [Riassunti](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Quanto contesto occupa la storia

Il menu delle impostazioni (⚙) contiene **Branch Tokens**. Mostra, prima che tu generi qualcosa, quanto della storia del ramo attivo occupa il prompt: una barra per la storia **senza riassunti** (tutte le parti) e **con riassunti** (i riassunti e le parti successive al più recente), e una tabella con le parti, i token e la quota del **budget del prompt** (il contesto del modello del libro meno lo spazio per la risposta). Contano solo i riassunti che descrivono ancora la storia; quelli che non corrispondono più sono indicati per nome e vengono scartati quando viene generata una parte. Il lore, il template di sistema e le istruzioni del turno occupano spazio dallo stesso budget. Senza un modello con una dimensione di contesto, la tabella non mostra quote.

![Branch Tokens](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Quali voci del lorebook sono state usate

**Lore Usage** nel menu delle impostazioni (⚙, **Analysis**) indica quali voci del lorebook del libro sono state usate dalle generazioni e quali non lo sono mai state, così un lorebook si può ripulire in base a ciò che fa davvero e non per supposizione. Ogni generazione registra quali voci sono entrate nel suo prompt (e i token di ciascuna); la finestra somma i dati delle parti:

- **Parts** sceglie cosa viene contato: il **Active branch**, oppure **All branches**, cioè ogni ramo e ogni alternativa del libro.
- **From part** e **To part** limitano l'intervallo, numerato come nella scaletta (la prima parte di un ramo è 1; in *All branches* il numero indica quanto la parte è profonda nell'albero). Lasciali vuoti per l'inizio e la fine.
- **Show** mantiene le voci che erano **Never used**, **Sometimes used** o **Always used** nelle parti conteggiate.

![La finestra Lore Usage](../../images/lore-usage.png)

La tabella ha una riga per ogni voce dei lorebook del libro, dalla più usata alla meno usata: il suo lorebook, se non è mai stata usata, qualche volta o sempre, in quante parti (tra quelle che hanno una registrazione, con la percentuale), i token che è costata in tutte queste parti e per ogni uso, e la prima e l'ultima parte che l'hanno usata. Clicca un'intestazione per ordinare. Una voce usata in precedenza che ora non si trova più nei lorebook del libro (eliminata, spostata, oppure il suo lorebook è disattivato o non è più quello del libro) resta nell'elenco, contrassegnata come *not in the lorebook now*; una voce eliminata non ha un nome.

Vengono conteggiate solo le parti che hanno una registrazione: una parte scritta a mano, scritta prima che Marginalia conservasse questa registrazione, oppure scritta quando il libro non aveva alcun lorebook non ne ha una, e la finestra indica quante sono. Le voci *Always used* potrebbero essere voci costanti; le voci *Never used* sono candidate alla rimozione o a un filtro migliore. [**Preview activation**](../lorebooks/entries.md#preview-activation) nell'editor del lorebook mostra perché una voce non si attiva in questo momento.

<a id="text-styles"></a>

## Stili del testo

Il menu delle impostazioni (⚙) nella barra in basso ha **Appearance → Change Styles For Text**, un menu con una voce per ogni stile, in ordine alfabetico. Per sapere come creare uno stile, vedi la [guida agli stili](styling-guide.md). Uno stile decide l'aspetto del testo delle parti: carattere, dimensione, colori, allineamento, titoli... L'applicazione ne include due che non si possono modificare: **Normal** (l'aspetto dell'applicazione) e **Book** (uno stile di lettura simile a un libro, quello usato dal [visualizzatore](publishing.md)). Puoi crearne di tuoi in [Settings → Styles](../account.md#styles).

- La prima voce, **Default (...)**, segue il tuo *stile predefinito* (quello contrassegnato in *Settings → Styles*; se non ne hai scelto uno, è Normal). Un libro nuovo non ha uno stile proprio e mostra quello predefinito.
- Scegliere un'altra voce salva lo stile **nel libro**, quindi sostituisce il predefinito solo per quel libro; la voce spuntata è quella che il libro usa ora. **Default** annulla la scelta.
- Uno stile che elimini ricade sul tuo predefinito nei libri che lo usavano.

![Appearance, Change Styles For Text: gli stili tra cui scegliere](../../images/story-style-menu.png)

Nello stile **Book** i paragrafi sono giustificati e le parole lunghe vengono sillabate automaticamente, usando il dizionario della **Language** del libro (impostata nella scheda [About](managing-books.md#the-about-tab)). La sillabazione è eseguita dal browser; per alcune lingue rare non c'è un dizionario e il testo semplicemente non viene sillabato.

I libri che avevano attivo il vecchio interruttore *Change Styles For Text* continuano a mostrare lo stile Book finché non scegli un'altra voce.

<a id="highlighting-generated-text"></a>

## Evidenziare il testo generato

Il menu delle impostazioni (⚙) contiene anche l'opzione spuntabile **Highlight Generated Text**. Quando è attiva, le parole scritte da un modello vengono evidenziate in ogni parte, così ciò che hai aggiunto o riscritto risalta. Marginalia segue il testo attraverso le [versioni della parte](version-history.md): ogni versione viene sovrapposta a quella precedente, e le parole che hanno in comune conservano l'autore originale, quindi una frase che hai scritto in un testo generato resta tua per quante volte il modello riscriva il resto. Una parte che non hai mai modificato viene evidenziata per intero; una parte che hai scritto tu non ha nulla di evidenziato. Ciò che scrive un agente conta come scritto dal modello. L'interruttore non viene salvato: quando la pagina viene riaperta è di nuovo spento.

Una parte la cui [cronologia è disattivata](version-history.md) non ha versioni da seguire: viene confrontata con il testo che il modello ha generato per primo. L'evidenziazione funziona anche sul [telefono](../phone.md).

Lo stesso conteggio dà anche la percentuale: la scheda **Info** del libro ha **Authorship of the branch** con un pulsante **Calculate**, che indica quante percentuali delle parole del ramo attivo hai scritto tu e quante ne ha generate un modello. [L'esportazione](export.md) può chiudere il file con una dichiarazione di questi dati.

![Testo generato evidenziato e il menu delle impostazioni](../../images/highlight-generated.png)

<a id="commands"></a>

## Comandi

Accanto al menu, sotto la storia, c'è una **command line**: digita `/` per vedere i comandi, scegline uno e premi **Enter** (o il pulsante di riproduzione) per eseguirlo. Ciò che restituisce un comando viene mostrato in una piccola finestra sopra la riga. Vedi [Comandi](../commands/index.md) per sapere come scrivere gli argomenti, cosa mostra l'aiuto e come interrompere un comando che richiede tempo. La command line si trova, per ora, solo nella scheda della storia nel layout desktop.
Il menu delle impostazioni (⚙) ha anche **Clear history of the book** e **Empty trash of the history**, vedi [Cronologia delle versioni](version-history.md#deleting-versions).

## Search and replace

Il menu delle impostazioni (⚙) ha anche **Search and replace**, che cambia una parola, un nome o una frase in tutta la storia in una sola volta. La finestra ha i campi **Search for** e **Replace with** e queste opzioni:

![Il menu delle impostazioni con Search and replace](../../images/story-settings-menu.png)

![La finestra Search and replace con una ricerca digitata](../../images/search-replace-dialog.png)

| Opzione | Predefinito | Significato |
|---|---|---|
| **Search by regex** | disattivo | Il testo cercato è una [espressione regolare](../regular-expressions.md) e la sostituzione può usarne i gruppi (`$1`). Altrimenti il testo viene cercato così come lo digiti. |
| **Case sensitive** | attivo | Un testo viene trovato solo con le maiuscole e minuscole con cui lo hai digitato (*John* non trova *john*). Disattivo: maiuscole e minuscole non contano. Bloccato mentre **Search by regex** è attivo. |
| **Search in summaries** | attivo | I [riassunti](summaries.md) vengono cercati e modificati anch'essi. |
| **Search in all branches** | disattivo | Vengono cercati tutti i [rami](branches-and-story-tree.md), altrimenti solo quello attivo. |
| **Revalidate all summaries** | attivo | Dopo la sostituzione ogni [riassunto](summaries.md) del libro torna valido, vedi sotto. |

Con un'espressione regolare la distinzione tra maiuscole e minuscole è sempre quella digitata; inizia con `(?i)` per ignorarla. Premi **Search** (il pulsante a destra dei campi; sul telefono sotto di essi). La barra di avanzamento mostra a che punto è, e **Stop** accanto ad essa la interrompe. Una ricerca non cambia nulla.

La tabella elenca ogni parte della storia e ogni riassunto che ha una corrispondenza, con il suo tipo e il suo id. Apri una riga per vedere le corrispondenze al suo interno: il testo intorno alla corrispondenza com'è e come sarà dopo la sostituzione. Una corrispondenza o una sostituzione troppo lunga per essere mostrata viene scritta come *[searched text]* o *[replace text]*.

![Le corrispondenze di una ricerca, con la prima parte aperta](../../images/search-replace-found.png)

Premendo **Enter** nel campo **Search for** o **Replace with** si ottiene lo stesso effetto di **Search**.

Quando la ricerca è finita, i campi e le opzioni sono bloccati e il pulsante **Replace** è attivo. **Search again** dimentica ciò che è stato trovato e sblocca la finestra, così puoi cambiare la ricerca. **Replace** cambia la storia: la finestra è bloccata del tutto (anche **Stop** non funziona) finché non ha finito, e poi torna vuota. Le parti sostituite sono segnate come modificate, come se le avessi modificate tu.

Una sostituzione nel testo che un [riassunto](summaries.md#when-summaries-become-outdated) descrive renderebbe il riassunto non aggiornato. Con **Revalidate all summaries** attivo, Marginalia ricalcola il valore di controllo di ogni riassunto del libro (anche quelli di tutti i rami, quelli che la sostituzione non ha toccato e quelli che erano già non aggiornati), così tutti risultano validi dopo. Questo rende valido anche un riassunto non aggiornato senza esaminarne il testo, quindi usalo quando sai che i riassunti dicono ancora la cosa giusta.

Con l'opzione disattivata vengono aggiornati solo i riassunti coperti dalla ricerca. Quelli non coperti (riassunti di rami non cercati, che condividono le parti iniziali con il ramo attivo, oppure tutti quelli quando i riassunti non vengono cercati) diventano non aggiornati. Se ce ne sono, Marginalia te lo dice indicandone il numero e chiede se continuare. Un riassunto già non aggiornato resta tale.

![L'avviso sui riassunti che diventerebbero non aggiornati](../../images/search-replace-warning.png)

Nel [layout per telefono](../phone.md) la stessa finestra ha i campi e i pulsanti uno sotto l'altro, e la tabella ha una sola colonna che mostra il tipo e l'id, con sotto di essi la corrispondenza e la sostituzione.

La [Panoramica dei riassunti](summaries.md#search-and-replace-in-the-summaries) ha la stessa finestra, ma solo per i riassunti.

La stessa ricerca e sostituzione può essere eseguita dalla [riga di comando](../commands/index.md#commands-that-come-with-marginalia) con `/search-replace-parts`; chiede conferma prima di sostituire.

<a id="importing-into-the-book"></a>

## Importare nel libro

Il menu delle impostazioni (⚙) ha **Import** (sotto **Import & export**). È la finestra della scheda Books che crea un nuovo libro da un file (vedi [Gestione dei libri](managing-books.md#creating-a-book)), ma per il libro in cui ti trovi: le parti vengono invece **aggiunte alla storia**. La **Source** è una tra: l'archivio zip di file `.md`, un documento (Word, LibreOffice, RTF, Markdown, testo semplice, EPUB), lo zip di un vault di Obsidian o lo zip di un progetto Scrivener. Ognuna ha lo stesso testo e le stesse opzioni che lì (il numero di parole per parte e il livello di intestazione dei capitoli), e una riga dice cosa farà l'import. Le chat di SillyTavern e LM Studio non sono offerte: sono un albero di rami, che non ha posto in mezzo a una storia.

**Put the parts** indica dove vanno:

- **At the end of the story**: dopo l'ultima parte del ramo attivo (la prima parte di un libro vuoto).
- **Before a part** o **After a part**: scegli la parte dall'elenco (i numeri sono le posizioni nel ramo attivo, con l'inizio del testo). Le parti vengono inserite tra due parti: tutto ciò che seguiva il punto, con tutti i suoi rami, segue le nuove parti, e nessuna parte viene modificata o generata di nuovo. La fine della storia resta dov'era (la storia è di tante parti più lunga). I [riassunti](summaries.md) che coprono il punto diventano non aggiornati, perché descrivono la storia senza le nuove parti: la finestra dei riassunti li mostra e li rifa.

![La finestra di importazione di un libro](../images/book-import-into.png)

Le parti di un archivio sono i file in ordine di nome, con i numeri nei nomi confrontati come numeri (`2.md` prima di `10.md`), anche nelle cartelle dell'archivio. Le cartelle, i file nascosti (`.name`), la cartella `__MACOSX` e i file che non sono `.md` vengono saltati. Il file è il testo della parte così com'è: non viene inviato al modello né modificato, e tutte le parti risultano scritte da te.

L'import mostra una barra di avanzamento con il numero di parti completate. **Stop** la termina dopo la parte in corso di salvataggio: le parti già aggiunte restano nella storia (l'import non viene annullato), e un messaggio dice quante ne sono state importate.

<a id="after-an-import"></a>

### Dopo un import

Quando le parti sono nel libro, una finestra **Import finished** propone le due cose che rendono più facile scrivere una storia lunga (un libro appena importato come nuovo viene aperto per primo, e la finestra è davanti ad esso). Entrambe sono anche nel menu delle impostazioni (⚙, **Analysis**), quindi si possono fare dopo o di nuovo:

- **Create summaries...** scrive i riassunti di ciò che è stato importato, vedi [Creare i riassunti](summaries.md#create-summaries). Fallo per primo: un agente blocca il libro mentre lavora.
- **Take lore notes with an agent...** avvia un [agente](../agents.md) che legge la storia e scrive i personaggi, i luoghi, le cose e i termini che trova come voci del lorebook del libro, vedi [Note di lore](../agents.md#lore-notes-of-an-imported-book).

**Not now** chiude la finestra senza fare nessuna delle due cose.

![La finestra dopo un import](../../images/import-finished.png)

Lo stesso import in un lorebook è descritto in [Importazione ed esportazione](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Esportare la storia

Il menu delle impostazioni (⚙) ha anche **Export story**, che salva il ramo attivo come file di testo, Markdown, HTML, Word, PDF o EPUB. Vedi [Esportare un libro](export.md).

<a id="what-the-model-gets"></a>

## Cosa riceve il modello

Per ogni nuova parte Marginalia invia:

1. Il **master template** come messaggio di sistema: ruolo, voci del [lorebook](../lorebooks/index.md), punto di vista,
   tempo verbale, stile e [riassunti](summaries.md). Il prompt di jailbreak del
   [fornitore di inferenza](../inference-providers.md#general-settings), se attivato, viene per primo.
2. Le parti precedenti del ramo attivo come risposte precedenti del modello: quante ne entrano nel
   [limite di contesto](../protocols.md#limits), dalla più recente. Le parti più vecchie che non entrano vengono lasciate fuori (a meno che un
   [riassunto](summaries.md) le copra).
3. Lo **user prompt** con le istruzioni per la nuova parte.

*Show Prompt* su qualsiasi parte mostra esattamente ciò che è stato inviato. Vedi [Prompt del libro](prompts.md) per i template.
