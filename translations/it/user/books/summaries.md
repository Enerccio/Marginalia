---
label: Riassunti
order: 60
---

# Riassunti

Un modello può leggere solo una quantità limitata di testo alla volta (il suo contesto). Quando un libro cresce oltre
quel limite, Marginalia lascia fuori le parti più vecchie e il modello dimentica ciò che vi accadeva. I **Riassunti**
mantengono quella storia nel prompt in forma condensata.

<a id="how-summaries-work"></a>

## Come funzionano i riassunti

Un riassunto appartiene a una parte e copre **quella parte e tutte le parti precedenti**, fino alla parte precedente
che ha un riassunto:

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
            [S1]            [S2]
└──── S1 ────┘└──── S2 ──────┘ inviate per intero ...
```

Qui *S1* (sulla parte #4) copre le parti da #1 a #4 e *S2* (sulla parte #8) copre le parti da #5 a #8.

Quando viene generata la parte successiva:

- tutti i riassunti del ramo attivo entrano nel modello principale, dal più vecchio al più recente, nel blocco
  `{%{{{summaries}}}%}` (*Riassunto della storia finora* nel modello predefinito);
- le parti coperte dai riassunti **non** vengono più inviate;
- le parti successive al riassunto più recente (#9 e #10 nell'esempio sopra) vengono inviate per intero, quante ne entrano.

I riassunti vengono creati solo quando lo chiedi: Marginalia non riassume da solo.

<a id="creating-a-summary"></a>

## Creare un riassunto

1. Scegli la parte che chiude un tratto di storia che vuoi condensare, ad esempio l'ultima parte di un capitolo.
2. Nel suo menu (☰) scegli **Genera riassunto**.
3. La finestra del riassunto mostra il riassunto mentre il modello lo scrive (e il suo ragionamento, per i modelli di ragionamento). **Interrompi**
   blocca la generazione e scarta il riassunto. La finestra non blocca la storia: **Chiudi** mentre il modello scrive la sposta
   nel [pannello delle esecuzioni](story-editor.md#the-panel-of-the-runs) e il riassunto prosegue (nel frattempo la parte non può essere eliminata né unita ad altre);
   riaprilo da lì per leggerlo.
4. Quando ha finito, chiudi la finestra con **Esci**.

![Un riassunto in corso di generazione](../../images/summary-dialog.png)

Il menu della parte ora contiene **Mostra il riassunto**, che riapre il riassunto, e **Elimina il riassunto**, che lo
rimuove. Per modificare un riassunto, modificalo nella [panoramica dei riassunti](#the-summaries-overview), oppure eliminalo
e generalo di nuovo, eventualmente con un diverso [prompt per i riassunti](#the-summary-prompt).

Il riassunto è scritto dal modello del libro, con il [protocollo](../protocols.md) del libro, e deve entrare nel
[limite di contesto](../protocols.md#limits) insieme al testo delle parti che copre. Per riassumere un tratto lungo con un
modello piccolo, riassumi a passi più brevi.
<a id="the-summaries-overview"></a>

## La panoramica dei riassunti

Il pulsante **Riassunti** (l'icona a elenco, a sinistra del menu delle impostazioni ⚙) nella barra inferiore dell'editor della storia apre la panoramica di tutti i riassunti del ramo attivo. Passa con il mouse sul pulsante per vedere quanti token occupano i riassunti in uso nel prompt.

![Il pulsante Riassunti con i token dei riassunti in uso](../../images/summaries-menu-button.png)

La finestra elenca i riassunti che la prossima generazione invierà, dal più vecchio al più recente, e in alto la somma dei loro token.

![La panoramica dei riassunti](../../images/summaries-dialog.png)

| Colonna | |
|---|---|
| **ID** | ID nel database del riassunto. Un `-` indica un riassunto [sostituito](#meta-summaries) da un meta riassunto. |
| **ID del messaggio** | ID nel database della parte a cui appartiene il riassunto (lo stesso numero che la struttura dell'editor della storia mostra come *DB ID*). |
| **Ordine** | Posizione di quella parte nel ramo, `1` è la prima parte. |
| **Contenuto** | Il ragionamento del modello (compresso, se si trattava di un modello di ragionamento) e il testo del riassunto, in un'area scorrevole della stessa altezza per ogni riassunto. |
| **Token** | Dimensione del testo del riassunto. |
| **Seleziona** | Spunta il riassunto per un [meta riassunto](#creating-a-meta-summary). |
| icona del cestino | Elimina il riassunto. |

I riassunti che confluiscono in un meta riassunto sono mostrati sotto di esso, vedi [più sotto](#meta-summaries). Non hanno la casella **Seleziona**, né l'icona del cestino, né la matita: si possono modificare solo i riassunti in uso.

La barra dei menu sopra la tabella contiene **Cerca e sostituisci** (vedi [più sotto](#search-and-replace-in-the-summaries)), **Crea riassunti...** (vedi [più sotto](#create-summaries)), **Rigenera tutti i riassunti** (vedi [più sotto](#resummarize-all-summaries)), **Esporta i riassunti** e **Importa i riassunti** (vedi [Esportazione e importazione](#export-and-import-of-the-summaries)); le [estensioni](../extensions/index.md) possono aggiungervi strumenti.

<a id="editing-a-summary"></a>

### Modificare un riassunto

Clicca sulla matita a destra del contenuto di un riassunto. Il testo diventa un'area di testo delle stesse dimensioni. Clicca sulla spunta verde per salvare la modifica, oppure sull'icona rossa del cestino per scartarla. Il conteggio dei token viene ricalcolato dopo il salvataggio.

![Modificare un riassunto](../../images/summary-edit.png)

Modificare un riassunto cambia ciò che riceve il modello, nient'altro: il riassunto resta valido. Si può modificare solo il testo, non il ragionamento.

<a id="search-and-replace-in-the-summaries"></a>

### Cerca e sostituisci nei riassunti

**Cerca e sostituisci** nella barra dei menu della panoramica cambia un nome o una frase solo nei riassunti, per esempio quando un personaggio è stato rinominato, e lascia la storia com'è. È la [ricerca e sostituzione della storia](story-editor.md#search-and-replace) con gli stessi campi e le stesse opzioni, tranne che non c'è **Cerca nei riassunti** (i riassunti sono tutto ciò che viene esaminato). La tabella elenca ogni riassunto con una corrispondenza, e le corrispondenze al suo interno.

Esamina anche i riassunti che un meta riassunto sostituisce e il riassunto che un meta riassunto ha sostituito (sono mostrati sotto il meta riassunto e tornano quando viene [annullato](#unwinding-a-meta-summary)): una corrispondenza in essi è elencata con il meta riassunto che li contiene.

Il testo dei riassunti confluiti fa parte di ciò che rende valido un meta riassunto, quindi la sostituzione ricalcola il valore di controllo di quei meta riassunti e restano validi. **Riconvalida tutti i riassunti** (attivo di default) lo fa per ogni riassunto del libro, anche per quelli che la sostituzione non ha toccato e per quelli già non aggiornati prima, vedi [la ricerca nella storia](story-editor.md#search-and-replace). Con l'opzione disattivata, un meta riassunto su un ramo non cercato (**Cerca in tutti i rami** disattivato) diventerebbe non aggiornato, e la finestra ti dice quanti sono e chiede se procedere.

<a id="create-summaries"></a>

### Crea riassunti

**Crea riassunti...** (anche nel menu delle impostazioni ⚙ della storia, **Analisi**, e proposto dopo un [importazione](story-editor.md#after-an-import)) scrive in una volta sola i riassunti di una storia lunga, per esempio di un libro importato: divide la storia in blocchi e crea un riassunto per ciascuno, uno dopo l'altro, dal più vecchio. Un riassunto copre le parti fino alla parte successiva al riassunto precedente, come fa il riassunto di una singola parte, così i blocchi si susseguono senza lacune.

- **Da dove inizia.** Solo le parti **dopo il riassunto più recente** ricevono un riassunto (tutta la storia, se non ce n'è nessuno). I riassunti esistenti vengono ignorati: non vengono riscritti, né modificati, e non decidono dove finiscono i nuovi blocchi. Per riscrivere quelli più vecchi usa [Rigenera tutti i riassunti](#resummarize-all-summaries).
- **Dividi la storia per** indica dove finisce un blocco:
  - **Numero di token**: dopo la parte che porta il blocco a **Token per riassunto** (il taglio viene arrotondato alla fine di quella parte; un blocco ha almeno quel numero di token, con tutte le parti necessarie).
  - **Capitoli**: prima di una parte che inizia un capitolo, così il riassunto va sulla parte precedente. Un capitolo è una parte che inizia con un titolo (il livello di titolo più basso con cui inizia una parte: `#` in un libro creato con l'[importazione](managing-books.md#importing-a-finished-manuscript)). **Numero minimo di token prima che un capitolo chiuda un riassunto** evita che i capitoli brevi abbiano un riassunto tutto loro: quando il blocco accumulato ha meno token, il capitolo che inizia non lo chiude e viene riassunto insieme al successivo.
  - **Numero di parti**: dopo ogni **Parti per riassunto** parti.
- **Fino alla parte** ferma l'intervallo a una parte (vuoto significa fino alla fine della storia). **Riassumi anche il resto alla fine** dà un riassunto alle parti dopo l'ultimo taglio, che sono meno di un blocco (con i capitoli solo se hanno il numero minimo di token); disattivalo per lasciare senza riassunto le parti più recenti, come sono mentre scrivi.
- Un blocco non diventa mai più lungo di quanto il modello possa leggere (80% del contesto, senza la risposta): termina prima della parte che lo farebbe sforare. Una singola parte più lunga di questo limite fallisce con il solito errore.

![Crea riassunti: per capitoli, con un minimo di 500 token](../../images/summaries-create.png)

Una riga sotto le opzioni indica quanti riassunti verranno scritti, per quante parti e token. **Avvia** apre la finestra con il numero del riassunto in scrittura e il suo testo man mano che arriva. La finestra si chiude quando viene scritto l'ultimo.

![La finestra mentre vengono scritti i riassunti](../../images/summaries-create-progress.png)

Ogni riassunto viene memorizzato appena scritto: **Interrompi**, oppure una richiesta che fallisce, lascia i riassunti precedenti, e **Crea riassunti** riparte da lì la volta successiva (comincia dopo il riassunto più recente). Il libro non deve essere bloccato da un [agente](../agents.md#the-book-is-locked); ogni riassunto è una richiesta al modello del libro.

<a id="resummarize-all-summaries"></a>

### Rigenera tutti i riassunti

Dopo aver cambiato il prompt dei riassunti (o quello dei meta riassunti), i riassunti che hai già seguono ancora il vecchio. **Rigenera tutti i riassunti** li riscrive tutti con il modello del libro, così sono tutti uguali. Chiede prima conferma e dice quanti riassunti sta per scrivere: una richiesta per ciascuno.

![La domanda prima che i riassunti vengano riscritti](../../images/summaries-resummarize-confirm.png)

- Parte dal riassunto **più vecchio** e prosegue fino al più recente.
- Un **meta riassunto** viene scritto dopo i riassunti che sostituisce (quelli mostrati sotto di esso, prima i meta riassunti interni di quelli esterni), a partire dai testi **nuovi**, quindi un meta riassunto non è mai fatto di testo vecchio.
- Ogni riassunto mantiene il suo posto sulla sua parte. Il valore di controllo di ciascuno viene ricalcolato dalla storia com'è ora, quindi un riassunto **non aggiornato** diventa valido (il modello ha letto il testo attuale delle parti).
- La finestra mostra il numero del riassunto in scrittura e il testo man mano che arriva. I nuovi testi vengono memorizzati solo quando l'**ultimo** è completato: se premi **Interrompi**, oppure una richiesta fallisce (il prompt non entra nel contesto, il modello non risponde), ogni riassunto resta com'era.

![La finestra mentre vengono scritti i riassunti](../../images/summaries-resummarize.png)

I riassunti di altri rami non vengono modificati. Assicurati che il modello non sia occupato e che il libro non sia lavorato da un agente. Le modifiche che hai fatto a mano ai testi vengono sostituite: esporta prima i riassunti se vuoi conservarli.

<a id="export-and-import-of-the-summaries"></a>

### Esportazione e importazione dei riassunti

**Esporta i riassunti** scarica i riassunti in uso sul ramo attivo come un unico file di testo (`<libro>-summaries.md`), dal più vecchio, ciascuno sotto una riga come `<!-- summary part="..." order="3" -->`. Modifica i testi con un editor qualsiasi, conserva le righe di intestazione, e riportalo con **Importa i riassunti**: scegli il file, la tabella indica cosa accadrebbe a ciascun riassunto, e non viene scritto nulla finché non premi **Importa**.

| Stato | |
|---|---|
| **Cambierà** | La parte indicata dall'intestazione ha un riassunto in uso e il suo testo è diverso. |
| **Cambierà (per posizione)** | Vedi sotto. |
| **Invariato** | Il testo è quello che il riassunto ha già. |
| **Parte non trovata** | Il ramo non ha un riassunto in uso per l'intestazione. |
| **Testo vuoto** | Il testo sotto l'intestazione è vuoto; un'importazione non svuota mai un riassunto. |

![La finestra di importazione con un file modificato](../../images/summaries-import.png)

Un'intestazione indica la parte tramite il suo id, quindi un file riporta i riassunti al libro da cui proviene. Per spostare i riassunti in un altro libro (una copia ha parti con altri id), spunta **Abbina per posizione**: un riassunto la cui parte non è in questo ramo va al riassunto della parte nella stessa posizione (l'`order` dell'intestazione; la radice è 1). Controlla prima la tabella: una posizione va bene solo se le due storie hanno la stessa struttura.

Il file contiene i riassunti che usa la generazione, e un meta riassunto è uno di essi. I riassunti che un meta riassunto sostituisce non sono nel file e non vengono mai modificati: il loro testo fa parte di ciò che rende valido il meta riassunto (vedi [Meta riassunti](#meta-summaries)), quindi una loro modifica lo renderebbe non aggiornato. Un'importazione cambia solo il testo e il conteggio dei token di un riassunto, come fa la matita, quindi nessun riassunto diventa non aggiornato per effetto di essa. Viene rifiutata mentre un agente lavora sul libro.

<a id="deleting-a-summary"></a>

### Eliminare un riassunto

L'icona del cestino di un riassunto fa lo stesso di **Elimina il riassunto** nel menu della parte. Per un meta riassunto chiede cosa fare, vedi [Annullare un meta riassunto](#unwinding-a-meta-summary).
<a id="meta-summaries"></a>

## Meta-riassunti

I riassunti restano nel prompt per sempre, quindi un libro lungo riempie il contesto con essi. Un **meta-riassunto** condensa più riassunti in uno solo: è un riassunto di riassunti.

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
        [S1]        [S2]        [S3]
        └─────────── M ─────────────┘
```

Qui *M* unisce *S1*, *S2* e *S3*. Appartiene alla parte del riassunto unito più recente (#9 nello schema sopra, che aveva *S3*) e sostituisce quel riassunto sulla parte. Da quel momento in poi:

- nel blocco `{%{{{summaries}}}%}` va inviato solo *M*, al posto di *S1*, *S2* e *S3*;
- *S1* e *S2* restano sulle loro parti, ma non vengono inviati. Sono mostrati sotto *M* nella panoramica;
- *S3*, il riassunto che *M* ha sostituito, è conservato dentro *M*, mostrato sotto di esso con un badge **Sostituito**.

Le parti coperte dai riassunti uniti non vengono comunque inviate per intero; un meta-riassunto non cambia ciò che i riassunti coprono, solo quanto è lungo il testo che li rappresenta.

<a id="creating-a-meta-summary"></a>

### Creare un meta-riassunto

1. Apri la [panoramica dei riassunti](#the-summaries-overview).
2. Spunta la casella **Seleziona** dei riassunti da unire. Almeno due. Spunti solo i riassunti in uso (quelli al primo livello della griglia).
3. Clicca su **Crea meta-riassunto**.

Marginalia unisce tutto dal riassunto spuntato più recente a quello più vecchio, inclusi i riassunti intermedi che non erano spuntati. La finestra del meta-riassunto funziona come quella del riassunto: mostra il testo mentre il modello lo scrive, e **Interrompi** annulla l'operazione senza modificare nulla. Quando termina, chiudi la finestra con **Esci** e la panoramica mostra il risultato.

![Riassunti spuntati e meta-riassunto in generazione](../../images/meta-summary-create.png)

Un meta-riassunto può unire anche altri meta-riassunti: spuntali come qualsiasi altro riassunto. Quelli uniti vengono mostrati sotto quello nuovo, con i propri riassunti uniti sotto di essi.

I riassunti da unire e la richiesta al modello devono rientrare nel [limite di contesto](../protocols.md#limits), quindi con un modello piccolo unisci pochi riassunti alla volta.

<a id="unwinding-a-meta-summary"></a>

### Svolgere un meta-riassunto

Eliminare un meta-riassunto (l'icona del cestino nella panoramica, o **Elimina il riassunto** nel menu della parte) chiede:

| Pulsante | |
|---|---|
| **Svolgi** | Rimuove il meta-riassunto e rimette sulla parte il riassunto che aveva sostituito. I riassunti uniti in un meta-riassunto fatto di meta-riassunti tornano un livello alla volta, nell'ordine in cui sono stati creati. |
| **Elimina** | Rimuove il meta-riassunto e lascia la parte senza riassunto. I riassunti che aveva unito tornano in uso (ma quello che aveva sostituito è andato perso). |
| **Annulla** | Non fa nulla. |

Dopo lo svolgimento, i riassunti uniti tornano in uso e la panoramica li mostra al primo livello.

![Eliminazione di un meta-riassunto](../../images/meta-summary-delete.png)

<a id="the-meta-summary-prompt"></a>

### Il prompt del meta-riassunto

La richiesta per un meta-riassunto è il **Prompt di meta-riassunto predefinito** del libro (nella scheda [Prompt](prompts.md), oppure il predefinito da *Impostazioni → Modelli*). Riceve due variabili:

| Variabile | |
|---|---|
| `{%{{{summaryBlocks}}}%}` | I riassunti da unire, dal più vecchio, separati da una riga vuota. |
| `{%{{{backgroundLore}}}%}` | Le voci del lorebook attive quando è stata scritta la parte del meta-riassunto. |

Il prompt predefinito chiede di concatenare gli eventi in voci più brevi, circa la metà della dimensione, senza perdere nomi, oggetti, accordi o fili di trama in sospeso, e senza aggiungere nulla di nuovo.

<a id="when-summaries-become-outdated"></a>

## Quando i riassunti diventano obsoleti

Un riassunto è valido solo per il testo esatto da cui è stato creato. Quando [modifichi](story-editor.md#editing-the-text) o [elimini](story-editor.md#deleting-a-part) una parte coperta da un riassunto, il riassunto non corrisponde più.

I riassunti obsoleti non vengono inviati al modello. Marginalia lo verifica quando genera la parte successiva e chiede *The summaries of the messages with IDs: [...] are outdated ... Continue generation and delete them?* Rispondi **Sì** per eliminarli e generare; le parti che coprivano vengono inviate di nuovo per intero (per quanto entrano nel contesto) finché non le riassumi di nuovo. Rispondi **No** per fermarti: non viene eliminato nulla e puoi occuparti prima dei riassunti.

Nella [panoramica dei riassunti](#the-summaries-overview) un riassunto obsoleto ha uno **sfondo rosso** e l'etichetta **Obsoleto**, e i suoi token non vengono conteggiati nella somma in alto. Se dice ancora la cosa giusta (hai corretto un refuso, per esempio), premi il pulsante **check** della sua riga per **riconvalidarlo**, oppure scegli **Riconvalida tutti i riassunti** nella barra dei menu per riconvalidarli tutti insieme. Altrimenti eliminalo o generalo di nuovo. Un meta-riassunto viene verificato come qualsiasi altro riassunto; i riassunti che unisce ne fanno parte e non diventano obsoleti da soli. La riconvalida è rifiutata mentre un agente lavora sul libro.

Quando salvi la [modifica di una parte](story-editor.md#editing-the-text) che renderebbe obsoleti dei riassunti, Marginalia ti avvisa prima e ti permette di tornare alla modifica, di salvare comunque, oppure di salvare e riconvalidare i riassunti.

![La domanda quando un salvataggio rende obsoleti dei riassunti](../../images/summaries-save-warning.png)

Dopo **Salva comunque**, la panoramica dei riassunti mostra il riassunto come obsoleto (rosso, con l'etichetta **Obsoleto**, e non conteggiato nella somma in alto):

![Un riassunto obsoleto nella panoramica](../../images/summaries-outdated.png)

Una [ricerca e sostituzione](story-editor.md#search-and-replace) nella storia mantiene validi i riassunti che coprono, e avvisa per quelli che diventano obsoleti. La [ricerca e sostituzione dei riassunti](#search-and-replace-in-the-summaries) mantiene validi i meta-riassunti.

Un meta-riassunto è obsoleto quando viene modificata o eliminata una parte che copre, ma anche quando viene eliminato un riassunto che aveva unito. (Per questo i riassunti uniti non si possono modificare uno per uno: la modifica renderebbe obsoleto il meta-riassunto. La [ricerca e sostituzione](#search-and-replace-in-the-summaries) ricalcola il valore di controllo del meta-riassunto.) Rimuovere un meta-riassunto obsoleto equivale a svolgerlo: torna il riassunto che aveva sostituito, e viene verificato a sua volta. Se anche quello è obsoleto, viene rimosso allo stesso modo.

Quando [crei un ramo](branches-and-story-tree.md#branching-from-an-earlier-part) da una parte con un riassunto, il nuovo ramo riceve una copia del riassunto. Un meta-riassunto copiato conserva ciò che aveva sostituito, così può essere svolto nel nuovo ramo.

<a id="the-summary-prompt"></a>

## Il prompt del riassunto

La richiesta per un riassunto è il **Prompt di riassunto predefinito** del libro (nella scheda [Prompt](prompts.md), oppure il predefinito da *Impostazioni → Modelli*). Riceve due variabili:

| Variabile | |
|---|---|
| `{%{{{text}}}%}` | Il testo delle parti da riassumere, dalla più vecchia. |
| `{%{{{backgroundLore}}}%}` | Le voci del lorebook attive quando è stata scritta la parte riassunta. |

Il prompt predefinito chiede un registro compatto degli eventi, raggruppato per luogo: sotto un'intestazione `[LOCATION / SCENE: Nome]`, un evento per riga nella forma *chi fa cosa -> cosa ne consegue* (l'intenzione solo quando il testo la mostra), senza titoli, numeri o identificativi per gli eventi e senza l'atmosfera. È pensato per il modello, non per un lettore, e occupa circa un quarto della dimensione del testo che sostituisce. Il prompt del meta-riassunto si aspetta lo stesso formato e unisce le righe vicine in catene causali. Sostituiscilo se preferisci un formato o una lunghezza diversi; un riassunto creato prima della modifica del prompt conserva la sua forma precedente finché non lo rifai.

<a id="tips"></a>

## Suggerimenti

- Riassumi nei punti di pausa naturali: la fine di un capitolo o di una scena.
- Quando i riassunti iniziano a occupare troppo contesto (il tooltip del pulsante **Riassunti** mostra quanto), unisci i più vecchi in un meta-riassunto. Ripeti più avanti con il meta-riassunto e i riassunti successivi.
- I riassunti restano nel prompt per sempre. Con molti di loro, il prompt può riempire da solo il contesto, e la generazione si interrompe con *Limite di contesto insufficiente.* Allora accorciali (un prompt di riassunto più conciso), uniscili in [meta-riassunti](#meta-summaries) oppure aumenta il limite di contesto.
- Tieni i fatti che non devono mai essere dimenticati (nomi, relazioni, regole del mondo) nel [lorebook](../lorebooks/index.md), invece di affidarti ai riassunti.
- Controlla cosa ha ricevuto il modello con **Mostra il prompt** sulla parte successiva.
