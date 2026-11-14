---
label: Riassunti
order: 60
---

# Riassunti

Un modello può leggere solo una quantità limitata di testo alla volta (il suo contesto). Quando un libro cresce oltre
quel limite, Marginalia lascia fuori le parti più vecchie, e il modello dimentica ciò che vi accadeva. **Riassunti**
mantiene quella storia nel prompt in forma condensata.

<a id="how-summaries-work"></a>

## Come funzionano i riassunti

Un riassunto appartiene a una parte e copre **quella parte e tutte le parti precedenti**, fino alla parte precedente
con un riassunto:

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
            [S1]            [S2]
└──── S1 ────┘└──── S2 ──────┘ sent in full ...
```

Qui *S1* (sulla parte #4) copre le parti #1-#4 e *S2* (sulla parte #8) copre le parti #5-#8.

Quando viene generata la parte successiva:

- tutti i riassunti del ramo attivo vanno nel master template, dal più vecchio al più recente, nel blocco
  `{%{{{summaries}}}%}` (*Summary of story so far* nel template predefinito);
- le parti coperte dai riassunti **non** vengono più inviate;
- le parti successive al riassunto più recente (#9 e #10 nell'esempio sopra) vengono inviate per intero, quante ne
  entrano.

I riassunti vengono creati solo quando lo chiedi tu; Marginalia non riassume da solo.

<a id="creating-a-summary"></a>

## Creare un riassunto

1. Scegli la parte che chiude un tratto di storia che vuoi condensare, per esempio l'ultima parte di un capitolo.
2. Nel suo menu (☰) scegli **Genera riassunto**.
3. La finestra del riassunto mostra il riassunto mentre il modello lo scrive (e il suo ragionamento, per i modelli di ragionamento). **Interrompi**
   ferma la generazione e scarta il riassunto. La finestra non blocca la storia: **Chiudi** mentre il modello scrive la sposta
   nel [pannello delle esecuzioni](story-editor.md#the-panel-of-the-runs) e il riassunto prosegue (la parte non può essere eliminata o unita
   nel frattempo); riaprila da lì per leggerlo.
4. Quando ha finito, chiudi la finestra con **Esci**.

![Un riassunto in fase di generazione](../../images/summary-dialog.png)

Il menu della parte ora ha **Mostra il riassunto**, che riapre il riassunto, e **Elimina il riassunto**, che lo rimuove. Per
modificare un riassunto, modificalo nella [panoramica dei Riassunti](#the-summaries-overview), oppure eliminalo e generalo di nuovo,
eventualmente con un diverso [prompt di riassunto](#the-summary-prompt).

Il riassunto è scritto dal modello del libro, con il [protocollo](../protocols.md) del libro, e deve entrare nel
[limite di contesto](../protocols.md#limits) insieme al testo delle parti che copre. Per riassumere un tratto lungo con un modello piccolo,
riassumi a piccoli passi.
<a id="the-summaries-overview"></a>

## I riassunti in panoramica

Il pulsante **Riassunti** (l'icona a elenco, a sinistra del menu delle impostazioni ⚙) nella barra inferiore dell'editor della storia apre la
panoramica di tutti i riassunti del ramo attivo. Passa con il mouse sul pulsante per vedere quanti token occupano i riassunti in uso nel prompt.

![Il pulsante Riassunti con i token dei riassunti in uso](../../images/summaries-menu-button.png)

La finestra elenca i riassunti che la prossima generazione invierà, dal più vecchio al più recente, e in alto la somma dei loro token.

![La panoramica dei riassunti](../../images/summaries-dialog.png)

| Colonna | |
|---|---|
| **ID** | ID nel database del riassunto. Un `-` indica un riassunto che è stato [sostituito](#meta-summaries) da un meta riassunto. |
| **ID del messaggio** | ID nel database della parte a cui appartiene il riassunto (lo stesso numero che la struttura dell'editor della storia mostra come *DB ID*). |
| **Ordine** | Posizione di quella parte nel ramo; `1` è la prima parte. |
| **Contenuto** | Il ragionamento del modello (ridotto a icona, se si trattava di un modello di ragionamento) e il testo del riassunto, in un'area scorrevole della stessa altezza per ogni riassunto. |
| **Token** | Dimensione del testo del riassunto. |
| **Seleziona** | Spunta il riassunto per un [meta riassunto](#creating-a-meta-summary). |
| icona del cestino | Elimina il riassunto. |

I riassunti confluiti in un meta riassunto sono mostrati sotto di esso, vedi [sotto](#meta-summaries). Non hanno la casella **Seleziona**, né l'icona del cestino, né la matita: si possono modificare solo i riassunti in uso.

La barra dei menu sopra la griglia contiene **Cerca e sostituisci** (vedi [sotto](#search-and-replace-in-the-summaries)), **Crea riassunti...** (vedi [sotto](#create-summaries)), **Rigenera tutti i riassunti** (vedi [sotto](#resummarize-all-summaries)), **Esporta i riassunti** e **Importa i riassunti** (vedi [Esportazione e importazione](#export-and-import-of-the-summaries)); le [estensioni](../extensions/index.md) possono aggiungervi strumenti.

<a id="editing-a-summary"></a>

### Modificare un riassunto

Clicca sulla matita a destra del contenuto di un riassunto. Il testo diventa un'area di testo della stessa dimensione. Clicca
sul segno di spunta verde per salvare la modifica, oppure sull'icona rossa del cestino per scartarla. Il conteggio dei token viene
ricalcolato dopo il salvataggio.

![Modifica di un riassunto](../../images/summary-edit.png)

Modificare un riassunto cambia solo ciò che riceve il modello, nient'altro: il riassunto resta valido. Si può modificare solo il testo,
non il ragionamento.

<a id="search-and-replace-in-the-summaries"></a>

### Cercare e sostituire nei riassunti

**Cerca e sostituisci** nella barra dei menu della panoramica cambia un nome o una frase solo nei riassunti, per esempio
quando un personaggio è stato rinominato, e lascia invariata la storia. È la [ricerca e sostituzione della storia](story-editor.md#search-and-replace)
con gli stessi campi e le stesse opzioni, tranne che non c'è **Cerca nei riassunti** (i riassunti sono tutto ciò che viene esaminato).
La tabella elenca ogni riassunto con una corrispondenza e le corrispondenze al suo interno.

Esamina anche i riassunti che un meta riassunto rappresenta e il riassunto che un meta riassunto ha sostituito (sono mostrati
sotto il meta riassunto e tornano quando viene [annullato](#unwinding-a-meta-summary)): una corrispondenza in essi viene elencata
con il meta riassunto che li contiene.

Il testo dei riassunti confluiti fa parte di ciò che rende valido un meta riassunto, quindi la sostituzione ricalcola il valore di controllo
di quei meta riassunti e restano validi. **Riconvalida tutti i riassunti** (attivo per impostazione predefinita) lo fa per ogni riassunto
del libro, anche per quelli che la sostituzione non ha toccato e per quelli già non validi prima, vedi
[la ricerca nella storia](story-editor.md#search-and-replace). Con l'opzione disattivata, un meta riassunto su un ramo non esaminato (**Cerca in tutti i rami**
disattivato) diventerebbe non valido, e la finestra te lo segnala indicando quanti sono e chiedendo se procedere.

<a id="create-summaries"></a>

### Crea riassunti

**Crea riassunti...** (anche nel menu delle impostazioni ⚙ della storia, **Analisi**, e proposto dopo un [import](story-editor.md#after-an-import))
scrive in un colpo solo i riassunti di una storia lunga, per esempio di un libro importato: divide la storia in blocchi e ne crea un
riassunto per ciascuno, uno dopo l'altro, dal più vecchio. Un riassunto copre le parti fino alla parte successiva al riassunto che la precede, come
il riassunto di una singola parte, così i blocchi si susseguono senza lacune.

- **Da dove parte.** Ricevono un riassunto solo le parti **successive al riassunto più recente** (tutta la storia, se non ce n'è nessuno). I riassunti
  esistenti vengono ignorati: non vengono scritti di nuovo, non vengono modificati e non determinano dove finiscono i nuovi blocchi. Per riscriverne
  di più vecchi usa [Rigenera tutti i riassunti](#resummarize-all-summaries).
- **Dividi la storia per** stabilisce dove finisce un blocco:
  - **Numero di token**: dopo la parte che porta il blocco a **Token per riassunto** (il taglio viene arrotondato alla fine di quella parte;
    un blocco ha almeno quel numero di token, con tutte le parti necessarie).
  - **Capitoli**: prima di una parte che inizia un capitolo, così il riassunto termina con la parte precedente. Un capitolo è una parte che inizia con
    un titolo (il livello di titolo più superficiale con cui inizia una parte: `#` in un libro creato dall'[import](managing-books.md#importing-a-finished-manuscript)).
    **Numero minimo di token prima che un capitolo chiuda un riassunto** evita che capitoli brevi abbiano un riassunto proprio: quando il blocco
    finora ha meno token, il capitolo che inizia non lo chiude e viene riassunto insieme al successivo.
  - **Numero di parti**: dopo ogni **Parti per riassunto** parti.
- **Fino alla parte** interrompe l'intervallo a una parte (vuoto significa fino alla fine della storia). **Riassumi anche il resto alla fine** dà un riassunto alle
  parti successive all'ultimo taglio, che sono meno di un blocco (con i capitoli solo se hanno il numero minimo di token); disattivalo per
  lasciare senza riassunto le parti più recenti, come sono mentre scrivi.
- Un blocco non diventa mai più lungo di quanto il modello riesca a leggere (80% del contesto, senza la risposta): termina prima della parte che lo
  supererebbe. Una singola parte più lunga di questo limite produce il solito errore.

![Crea riassunti: per capitoli, con un minimo di 500 token](../../images/summaries-create.png)

Una riga sotto le opzioni indica quanti riassunti verranno scritti, per quante parti e quanti token. **Avvia** apre la finestra con il numero
del riassunto in scrittura e il suo testo man mano che arriva. La finestra si chiude quando viene scritto l'ultimo.

![La finestra mentre vengono scritti i riassunti](../../images/summaries-create-progress.png)

Ogni riassunto viene salvato appena scritto: **Interrompi**, oppure una richiesta non riuscita, lascia
i riassunti precedenti, e **Crea riassunti** riprende da lì la volta successiva (parte dopo il riassunto più recente). Il libro non deve essere
bloccato da un [agente](../agents.md#the-book-is-locked); ogni riassunto è una richiesta al modello del libro.

<a id="resummarize-all-summaries"></a>

### Rigenera tutti i riassunti

Dopo aver cambiato il prompt dei riassunti (o quello dei meta riassunti), i riassunti che hai già seguono ancora quello vecchio.
**Rigenera tutti i riassunti** riscrive tutti i riassunti con il modello del libro, così risultano tutti uguali. Prima chiede conferma
e dice quanti riassunti sta per scrivere: una richiesta per ciascuno.

![La domanda prima che i riassunti vengano riscritti](../../images/summaries-resummarize-confirm.png)

- Parte dal riassunto **più vecchio** e procede fino al più recente.
- Un **meta riassunto** viene scritto dopo i riassunti che rappresenta (quelli mostrati sotto di esso, prima i meta riassunti interni di quelli esterni),
  a partire dai testi **nuovi**, così un meta riassunto non è mai composto da testo vecchio.
- Ogni riassunto mantiene il suo posto sulla sua parte. Il valore di controllo di ciascuno viene ricalcolato dalla storia com'è ora, quindi un
  riassunto che era **non valido** risulta valido dopo (il modello ha letto il testo attuale delle parti).
- La finestra mostra il numero del riassunto in scrittura e il testo man mano che arriva. I nuovi testi vengono salvati solo quando
  l'**ultimo** è completato: se premi **Interrompi**, oppure una richiesta fallisce (il prompt non entra nel contesto, il modello non risponde),
  ogni riassunto resta com'era.

![La finestra mentre vengono scritti i riassunti](../../images/summaries-resummarize.png)

I riassunti degli altri rami non vengono modificati. Assicurati che il modello non sia occupato e che il libro non sia lavorato da un
agente. Le modifiche che hai fatto a mano ai testi vengono sostituite: esporta prima i riassunti se vuoi conservarli.

<a id="export-and-import-of-the-summaries"></a>

### Esportazione e importazione dei riassunti

**Esporta i riassunti** scarica i riassunti in uso sul ramo attivo come un unico file di testo (`<book>-summaries.md`), dal più vecchio
al più recente, ciascuno sotto una riga come `<!-- summary part="..." order="3" -->`. Modifica i testi con un qualsiasi editor, mantieni le righe di intestazione, e
riporta il file con **Importa i riassunti**: scegli il file, la tabella indica cosa succederebbe a ciascun riassunto, e non viene scritto nulla
finché non premi **Importa**.

| Stato | |
|---|---|
| **Cambierà** | La parte indicata dall'intestazione ha un riassunto in uso e il suo testo è diverso. |
| **Cambierà (per posizione)** | Vedi sotto. |
| **Invariato** | Il testo è quello che il riassunto ha già. |
| **Parte non trovata** | Il ramo non ha un riassunto in uso per l'intestazione. |
| **Testo vuoto** | Il testo sotto l'intestazione è vuoto; un import non svuota mai un riassunto. |

![La finestra di importazione con un file modificato](../../images/summaries-import.png)

Un'intestazione indica la parte tramite il suo id, quindi un file riporta i riassunti al libro da cui proviene. Per spostare i riassunti in
un altro libro (una copia ha parti con altri id) spunta **Abbina per posizione**: un riassunto la cui parte non è in questo ramo
va al riassunto della parte nella stessa posizione (il `order` dell'intestazione; la radice è 1). Controlla prima la tabella: una
posizione funziona solo se le due storie hanno la stessa struttura.

Il file contiene i riassunti che usa la generazione, e un meta riassunto è uno di essi. I riassunti che un meta riassunto rappresenta non
sono nel file e non vengono mai modificati: il loro testo fa parte di ciò che rende valido il meta riassunto (vedi
[Meta riassunti](#meta-summaries)), quindi una loro modifica lo renderebbe non valido. Un import cambia solo il testo e il conteggio dei token di un riassunto,
come fa la matita, quindi nessun riassunto diventa non valido per effetto dell'import. Viene rifiutato mentre un agente lavora sul
libro.

<a id="deleting-a-summary"></a>

### Eliminare un riassunto

L'icona del cestino di un riassunto fa la stessa cosa di **Elimina riassunto** nel menu della parte. Per un meta riassunto chiede cosa
fare, vedi [Annullare un meta riassunto](#unwinding-a-meta-summary).
<a id="meta-summaries"></a>

## Meta-riassunti

I riassunti restano nel prompt per sempre, quindi un libro lungo riempie il contesto con essi. Un **meta-riassunto** condensa più riassunti in uno solo: è un riassunto di riassunti.

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
        [S1]        [S2]        [S3]
        └─────────── M ─────────────┘
```

Qui *M* unisce *S1*, *S2* e *S3*. Appartiene alla parte del riassunto unito più recente (#9 sopra, che aveva *S3*) e sostituisce quel riassunto sulla parte. Da quel momento:

- solo *M* entra nel blocco `{%{{{summaries}}}%}`, al posto di *S1*, *S2* e *S3*;
- *S1* e *S2* restano sulle loro parti, ma non vengono inviati. Nella panoramica sono mostrati sotto *M*;
- *S3*, il riassunto sostituito da *M*, è conservato dentro *M*, mostrato sotto di esso con un distintivo **Sostituito**.

Le parti coperte dai riassunti uniti non vengono comunque inviate per intero; un meta-riassunto non cambia ciò che i riassunti coprono, ma solo quanto è lungo il testo che li rappresenta.

<a id="creating-a-meta-summary"></a>

### Creare un meta-riassunto

1. Apri la [panoramica dei riassunti](#the-summaries-overview).
2. Spunta la casella **Seleziona** dei riassunti da unire. Almeno due. Spunti solo i riassunti in uso (quelli del livello superiore della griglia).
3. Fai clic su **Crea meta-riassunto**.

Marginalia unisce tutto dal riassunto selezionato più recente al più vecchio, compresi i riassunti intermedi che non erano stati selezionati. La finestra del meta-riassunto funziona come quella del riassunto: mostra il testo mentre il modello lo scrive, e **Interrompi** la annulla senza cambiare nulla. Al termine, chiudi la finestra con **Esci** e la panoramica mostra il risultato.

![Riassunti selezionati e meta-riassunto in generazione](../../images/meta-summary-create.png)

Un meta-riassunto può unire anche altri meta-riassunti: selezionali come qualsiasi altro riassunto. Quelli uniti sono mostrati sotto il nuovo, con i propri riassunti uniti sotto di loro.

I riassunti da unire e la richiesta al modello devono stare nel [limite di contesto](../protocols.md#limits), quindi con un modello piccolo unisci pochi riassunti alla volta.

<a id="unwinding-a-meta-summary"></a>

### Svolgere un meta-riassunto

Eliminare un meta-riassunto (l'icona del cestino nella panoramica, oppure **Elimina il riassunto** nel menu della parte) chiede:

| Pulsante | |
|---|---|
| **Svolgi** | Rimuove il meta-riassunto e ripristina sulla parte il riassunto che aveva sostituito. I riassunti uniti in un meta-riassunto composto da meta-riassunti tornano indietro un livello alla volta, nel modo in cui sono stati creati. |
| **Elimina** | Rimuove il meta-riassunto e lascia la parte senza riassunto. I riassunti che aveva unito tornano in uso (ma quello che aveva sostituito è andato perso). |
| **Annulla** | Non fa nulla. |

Dopo lo svolgimento, i riassunti uniti tornano in uso e la panoramica li mostra al livello superiore.

![Eliminazione di un meta-riassunto](../../images/meta-summary-delete.png)

<a id="the-meta-summary-prompt"></a>

### Il prompt di meta-riassunto

La richiesta per un meta-riassunto è il **Prompt di meta-riassunto predefinito** del libro (nella scheda [Prompts](prompts.md), oppure il predefinito da *Settings → Templates*). Riceve due variabili:

| Variabile | |
|---|---|
| `{%{{{summaryBlocks}}}%}` | I riassunti da unire, dal più vecchio, separati da una riga vuota. |
| `{%{{{backgroundLore}}}%}` | Le voci del lorebook che erano attive quando è stata scritta la parte del meta-riassunto. |

Il prompt integrato chiede di concatenare gli eventi in voci più brevi, circa la metà della lunghezza, senza perdere nomi, oggetti, accordi o fili narrativi irrisolti, e senza aggiungere nulla di nuovo.

<a id="when-summaries-become-outdated"></a>

## Quando i riassunti diventano obsoleti

Un riassunto è valido solo per il testo esatto da cui è stato creato. Quando [modifichi](story-editor.md#editing-the-text) o [elimini](story-editor.md#deleting-a-part) una parte coperta da un riassunto, il riassunto non corrisponde più.

I riassunti obsoleti non vengono inviati al modello. Marginalia lo verifica quando genera la parte successiva e chiede *The summaries of the messages with IDs: [...] are outdated ... Continue generation and delete them?* Rispondi **Sì** per eliminarli e generare; le parti che coprivano vengono di nuovo inviate per intero (per quanto ci stanno) finché non li riassumi di nuovo. Rispondi **No** per fermarti: non si elimina nulla e puoi occuparti dei riassunti prima.

Nella [panoramica dei riassunti](#the-summaries-overview) un riassunto obsoleto ha uno **sfondo rosso** e un'etichetta **Obsoleto**, e i suoi token non vengono conteggiati nella somma in alto. Se dice ancora la cosa giusta (hai corretto un refuso, per esempio), premi il pulsante **check** della sua riga per **riconvalidarlo**, oppure scegli **Riconvalida tutti i riassunti** nella barra dei menu per riconvalidarli tutti insieme. Altrimenti eliminalo o generalo di nuovo. Un meta-riassunto viene verificato come qualsiasi riassunto; i riassunti che ha unito ne fanno parte e non diventano mai obsoleti da soli. La riconvalida viene rifiutata mentre un agente lavora sul libro.

Quando salvi la [modifica di una parte](story-editor.md#editing-the-text) che renderebbe obsoleti dei riassunti, Marginalia ti avvisa prima e ti permette di tornare a modificare, di salvare comunque oppure di salvare e riconvalidare i riassunti.

![La domanda che appare quando un salvataggio rende obsoleti dei riassunti](../../images/summaries-save-warning.png)

Dopo **Salva comunque** la panoramica dei riassunti mostra il riassunto come obsoleto (in rosso, con l'etichetta **Obsoleto**, e non conteggiato nella somma in alto):

![Un riassunto obsoleto nella panoramica](../../images/summaries-outdated.png)

Una [ricerca e sostituzione](story-editor.md#search-and-replace) nella storia mantiene validi i riassunti che copre e avvisa di quelli che diventano obsoleti. La [ricerca e sostituzione dei riassunti](#search-and-replace-in-the-summaries) mantiene validi i meta-riassunti.

Un meta-riassunto diventa obsoleto quando viene modificata o eliminata una parte che copre, ma anche quando viene eliminato un riassunto che ha unito. (Per questo i riassunti uniti non possono essere modificati uno alla volta: la modifica renderebbe obsoleto il meta-riassunto. La [ricerca e sostituzione](#search-and-replace-in-the-summaries) ricalcola il valore di controllo del meta-riassunto.) Rimuovere un meta-riassunto obsoleto equivale a svolgerlo: torna il riassunto che aveva sostituito, e anch'esso viene verificato. Se anche quello è obsoleto, viene rimosso allo stesso modo.

Quando crei un [ramo](branches-and-story-tree.md#branching-from-an-earlier-part) da una parte con un riassunto, il nuovo ramo riceve una copia del riassunto. Una copia di meta-riassunto conserva ciò che aveva sostituito, quindi può essere svolta nel nuovo ramo.

<a id="the-summary-prompt"></a>

## Il prompt di riassunto

La richiesta per un riassunto è il **Prompt di riassunto predefinito** del libro (nella scheda [Prompts](prompts.md), oppure il predefinito da *Settings → Templates*). Riceve due variabili:

| Variabile | |
|---|---|
| `{%{{{text}}}%}` | Il testo delle parti da riassumere, dalla più vecchia. |
| `{%{{{backgroundLore}}}%}` | Le voci del lorebook che erano attive quando è stata scritta la parte riassunta. |

Il prompt integrato chiede un registro compatto degli eventi, raggruppato per luogo: sotto un'intestazione `[LOCATION / SCENE: Name]`, un evento per riga nella forma *chi fa cosa -> cosa ne consegue* (l'intento solo quando il testo lo mostra), senza titoli, numeri o id per gli eventi e senza l'atmosfera. È pensato per il modello, non per un lettore, e misura circa un quarto del testo che sostituisce. Il prompt di meta-riassunto si aspetta lo stesso formato e unisce le righe vicine in catene causali. Sostituiscilo se preferisci un formato o una lunghezza diversi; un riassunto creato prima della modifica del prompt conserva la forma precedente finché non lo rifai.

<a id="tips"></a>

## Suggerimenti

- Riassumi nelle pause naturali: la fine di un capitolo o di una scena.
- Quando i riassunti iniziano a occupare troppo contesto (il suggerimento del pulsante **Riassunti** mostra quanto), unisci i più vecchi in un meta-riassunto. Ripeti più tardi l'operazione con il meta-riassunto e i riassunti successivi.
- I riassunti restano nel prompt per sempre. Con molti di essi, il prompt può riempire da solo il contesto, e la generazione si ferma con *Limite di contesto insufficiente.* Allora accorciali (un prompt di riassunto più conciso), uniscili in [meta-riassunti](#meta-summaries) oppure aumenta il limite di contesto.
- Conserva i fatti che non devono mai essere dimenticati (nomi, relazioni, regole del mondo) nel [lorebook](../lorebooks/index.md), invece di affidarti ai riassunti.
- Controlla cosa ha ricevuto il modello con **Mostra il prompt** sulla parte successiva.
