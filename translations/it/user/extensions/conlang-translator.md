---
label: Traduttore di conlang
order: 17
---

# Traduttore di conlang

Permette ai personaggi della tua storia di parlare una lingua costruita. Tu descrivi la lingua (le sue regole e le sue parole) e il modello
scrive il **discorso diretto** dei personaggi che la parlano, mentre la storia conserva l'inglese da cui deriva.

!!!
Il testo della storia contiene il **conlang**. Tutto ciò che legge la storia per un modello (la generazione successiva, i riassunti,
la scansione del lorebook, le annotazioni, le altre estensioni) legge l'**inglese** da cui il discorso è stato tradotto, quindi il modello
non deve mai capire la tua lingua per proseguire la storia. Un clic su un discorso tradotto mostra l'inglese.
!!!

!!!warning Lavori in corso
L'estensione funziona per una persona che usa le schermate descritte qui e, tramite [comandi](#commands), per un agente. Il telefono ne ha solo una parte, vedi
[Limiti](#limits).
!!!

<a id="the-idea"></a>

## L'idea

1. Una **lingua** è due cose: **regole** (testo, scritto per il modello come un manuale di grammatica) e un **lessico** (le parole, scritte
   per lo più dal modello e giudicate da te). Le lingue sono conservate nelle tue impostazioni, una sola volta, e qualsiasi numero di libri le usa.
2. Un **libro** (o una cartella di libri) sceglie la sua lingua. Una parte viene tradotta **a mano** dal suo menu, **da sola** quando
   è definitiva, oppure **a intervalli**.
3. Una richiesta al modello per parte classifica ogni discorso diretto (è pronunciato nella lingua o nella lingua comune della
   storia, da questo personaggio, compreso dall'interlocutore?) e traduce il discorso che lo è. Una parola che la lingua non possiede viene inventata
   secondo le regole, **segnalata con il motivo** e aggiunta al lessico come **nuova**, perché tu la accetti, la modifichi, la blocchi o la scarti.
4. L'inglese viene conservato (nei dati del libro, come *fonte* del discorso) e letto da ogni modello. Il conlang è l'originale:
   ciò che modifichi a mano resta, e una parte che hai modificato non viene mai sovrascritta a tua insaputa.
<a id="languages"></a>

## Lingue

Una lingua si modifica in **Impostazioni → Impostazioni dell'estensione → Conlang Translator**, il pannello che elenca le tue lingue.

![Le lingue nelle impostazioni](../../images/plugin-conlang-settings.png)

| Pulsante | |
|---|---|
| **New language** | Una lingua vuota. |
| **Design a language** | Una [procedura guidata](#the-designer) in cui il modello propone ogni parte di una nuova lingua e tu la modifichi e la accetti. |
| **Edit** | L'[editor](#the-editor) della lingua selezionata. |
| **Duplicate** | Una copia indipendente con un nuovo nome, parole comprese. Usala per provare una modifica (una copia è anche l'unico modo per avere una lingua *per un solo libro*: un libro che sceglie una lingua la condivide con ogni altro libro che la sceglie). |
| **Compare** | Porta le modifiche di un'altra lingua in quella selezionata, dopo una [revisione](#comparing-and-merging). |
| **Export**, **Import** | Una lingua come documento (JSON): le regole e il lessico, senza i collegamenti a un libro. L'importazione di una lingua con un nome che hai già offre la revisione delle differenze oppure una nuova lingua. |
| **Delete** | Dopo una domanda di conferma. |

In alto ci sono il **modello e il protocollo del progettista**. Sotto l'elenco ci sono le scelte che libri e cartelle **ereditano** da te: la lingua, se una parte viene tradotta da sola,
e il modello e il protocollo della traduzione. Un libro o una cartella può impostare i propri, vedi [Scegliere la lingua di un
libro](#choosing-the-language-of-a-book). **Save** nella schermata Impostazioni per conservare le modifiche (le parole che una traduzione ha aggiunto nel frattempo
non vanno perse: una lingua che non hai toccato viene riletta quando la schermata viene salvata).

<a id="the-editor"></a>

### L'editor

L'editor è una finestra con cinque schede; lavora su una copia fino a **Save**, e può essere ridimensionata, trascinata e massimizzata.

| Scheda | |
|---|---|
| **Overview** | Il nome, una descrizione per te, l'opzione **A hyphen is a part of a word** (disattivata: `thal-orun` e `thalorun` sono la stessa parola; attivata, il trattino fa parte della forma), e il **example book** e l'**example lorebook** che il progettista legge. |
| **Rules** | Le regole in **sections** (suoni e ortografia, grammatica, formazione delle parole...). Ogni sezione indica **chi la legge**: *the translator* (inviata in ogni richiesta), *only the designer* (la storia della lingua, note per creare parole) oppure *nobody* (le tue note personali). Il plugin non analizza le regole: sono testo per il modello. |
| **Categories** | Gruppi di parole (*People & Social*, *Nature*, *Particles*...). Il lessico viene mostrato e inviato al modello sotto queste intestazioni. Una categoria che è **always sent** (particelle, pronomi, numeri) finisce in ogni richiesta anche quando il lessico è troppo grande per essere inviato per intero. |
| **Registers** | Gli stili di linguaggio (*formal*, *soft*, *vulgar*, *archaic*...) con una nota. Un concetto può avere una forma per ogni registro; il modello sceglie in base a chi parla e a chi si rivolge. |
| **Lexicon** | Le parole, vedi sotto. |

![Le regole di una lingua](../../images/plugin-conlang-rules.png)

<a id="the-lexicon"></a>

### Il lessico

![Il lessico di una lingua](../../images/plugin-conlang-lexicon.png)

La griglia ha una **search** (per parola o significato), filtri per **category** e **status**, e le azioni **Add**, **Edit**, **Accept**,
**Lock**, **Retire**, **Delete** e **Accept all new**. Una parola ha:

- la **form** (come la storia la scrive), facoltativamente la forma in uno **script**, una **category** e un **kind** (root, compound, particle,
  name, number, phrase);
- uno o più **meanings** (un omofono ne ha diversi, ognuno con una parte del discorso e una nota) e le **other forms of the concept**
  (la parola formale, soft e vulgar per la stessa cosa, mostrate e inviate insieme);
- la **rationale**: perché il modello ha scelto la parola (la regola o le radici). La leggi per giudicare la parola, e torna nelle richieste successive
  così che l'invenzione seguente segua la stessa logica;
- un'**origin**: tua, oppure del modello (con la parte per cui è stata inventata, il modello e l'ora). Una parola del modello che modifichi diventa tua;
- uno **status**: **New** (il modello l'ha creata e tu non l'hai ancora esaminata), **Accepted**, **Locked** (il modello può usarla ma non ridefinirla mai) oppure **Retired**
  (conservata, non offerta al modello: per una parola che è stata sostituita).

Una parola di cui il modello ha bisogno ma che manca nel lessico **non viene mai scartata perché è nuova**: viene aggiunta subito, così la parte successiva dice la stessa cosa nello stesso modo, e resta in attesa come *New*. Una traduzione che usa parole non presenti nel lessico e che il modello **non** ha segnalato viene contrassegnata, perché nessuno può dire se la parola sia un errore.

<a id="names-for-the-name-generator"></a>

### Nomi per il Name generator

La scheda **Names** dell'editor rende la lingua disponibile al [Name generator](name-generator.md): i nomi che produce suonano come la lingua. Aggiungi una **name category**
(per esempio *Places* o *People*), poi seleziona le parole del lessico di cui è composta. La **search** e il filtro per **category** restringono l'elenco, **Check all shown** e **Uncheck all shown**
agiscono sulle parole mostrate, e **Preview names** produce 20 nomi dalle parole selezionate in quel momento, prima che tu salvi qualsiasi cosa. Il Name generator addestra una catena di Markov sulle parole selezionate,
quindi una categoria ha bisogno di alcune parole (una dozzina o più dà nomi migliori).

Si conserva solo la tua scelta, non le parole: una parola che modifichi viene letta com'è ora, e una che elimini o ritiri viene esclusa quando i nomi vengono generati. Dopo aver salvato le impostazioni, la
categoria viene offerta nel Name generator (nella finestra **Tools** del generatore di lore e dello schema dell'arco, nel comando `generate-names`, nell'esecuzione di un agente) come *Conlang - lingua - categoria*.
È una tua proprietà: un altro utente dell'installazione vede solo le sue categorie.

![La scheda Names: una categoria di nomi, le parole di cui è composta e venti nomi ricavati da esse](../../images/plugin-conlang-names.png)

<a id="the-designer"></a>

### Il progettista

Il menu **Design with AI** dell'editor chiede al modello un aiuto, sempre con la lingua **così com'è** (le sue regole, i registri, le categorie, il lessico e l'example lorebook), e
sempre come **proposta**: non viene scritto nulla prima che tu l'abbia esaminata.

![Il menu del progettista](../../images/plugin-conlang-designer-menu.png)

| Azione | |
|---|---|
| **Generate words...** | Parole per una categoria, un certo numero di esse, con una nota ("la cucina di un accampamento d'assedio"). |
| **Add a section with AI...** | Una nuova sezione delle regole per ciò che manca (numeri, colori, cortesia) e le parole di cui ha bisogno. |
| **Rewrite a section...** | Il modello riscrive una sezione come richiesto. Il modello restituisce l'**intera sezione**, quindi una sezione molto lunga è lenta e costosa: tieni le sezioni brevi, un argomento per ciascuna. |
| **Review the language** | Un elenco di contraddizioni, lacune e parole che rompono il sistema dei suoni, ciascuna con una sezione e una correzione. Non viene cambiato nulla. |
| **Ask the designer...** | Una conversazione; una risposta con parole o sezioni può essere revisionata come modifiche. |

**Design a language** nel pannello è una procedura guidata di sette passi (la traccia, i suoni, la scrittura, la grammatica, la formazione delle parole, le prime parole per categoria e una prova con frasi).
Ogni passo è una sezione che il modello propone e tu modifichi, affini con una nota e accetti; la lingua esiste solo nella finestra fino a **Finish**.

![La procedura guidata che progetta una lingua](../../images/plugin-conlang-wizard.png)

<a id="comparing-and-merging"></a>

### Confronto e unione

**Compare** (e l'importazione di una lingua che hai già) apre una finestra con le differenze tra due lingue, ciascuna una riga con una casella di controllo: cosa è stato aggiunto,
modificato, eliminato o spostato tra i campi della lingua, le regole, le categorie, i registri, le parole e le categorie di nomi. Una modifica che entrambe le parti hanno fatto in modo diverso è un **conflitto**:
il tuo valore viene conservato finché non scegli quello dell'altra parte o ne scrivi uno tuo. Ciò che scegli viene scritto nella lingua sullo schermo e memorizzato quando salvi la schermata Impostazioni. La
stessa finestra mostra ciò che propone il progettista.

![La revisione delle differenze tra due lingue](../../images/plugin-conlang-review.png)

<a id="choosing-the-language-of-a-book"></a>

## Scegliere la lingua di un libro

La scheda **Prompts** della finestra del libro, sotto *Extension Settings*, contiene i campi dell'estensione; la finestra della cartella ha gli stessi. Vuoto significa *eredita*: la cartella
superiore, poi le impostazioni del tuo utente.

![I campi di un libro](../../images/plugin-conlang-book.png)

| Campo | |
|---|---|
| **Language of the speech** | La lingua in cui viene tradotto il libro. Vuoto: il libro non viene tradotto. |
| **Translate a new part by itself** | Una parte viene tradotta quando è definitiva: dopo una generazione, dopo l'annotazione automatica e la riscrittura se il libro le prevede, come passo di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part) (il pannello delle esecuzioni lo mostra, e il suo **Stop** lo interrompe). |
| **Model of the translation**, **Protocol of the translation** | Vuoto: il modello e il protocollo del libro, altrimenti i tuoi valori predefiniti. |
| **Readers of the published book see** | Ciò che mostra il [viewer](#the-viewer). |
<a id="the-conlang-tab-of-the-book"></a>

## La scheda Conlang del libro

La finestra della storia ha una scheda **Conlang** (nello stesso pannello delle capitoli e del lorebook; trascinala dove preferisci) per la lingua di *questo* libro. Ha quattro schede
proprie, e **Aggiorna** rilegge la lingua dopo che l'hai modificata nella finestra del libro. Un libro con [più di una lingua](#more-than-one-language-in-a-book) ha un campo
accanto a **Aggiorna** che sceglie a quale di esse si riferiscono le schede *Parole*, *Parlanti* e *Traduzione rapida*.

<a id="words"></a>

### Parole

Il lessico della lingua del libro, con la ricerca, i filtri e le azioni dell'[editor](#the-lexicon): cerca una parola mentre scrivi, modificala, accetta le parole che il
modello ha inventato, bloccane una o ritirala. La modifica viene salvata subito nella tua libreria (le parole aggiunte nel frattempo da una traduzione non vanno perse).

<a id="speakers"></a>

### Parlanti

Chi parla la lingua **in questo libro**. I parlanti appartengono al libro, non alla lingua: la stessa lingua è parlata da altre persone in un'altra storia, quindi una lingua
che duplichi, esporti o confronti non porta con sé nessun personaggio.

Per impostazione predefinita **tutti parlano la lingua, eccetto i personaggi elencati** (oppure, al contrario, solo quelli elencati). Questo è solo il valore predefinito: il
testo lo sovrascrive ("disse nella lingua comune", "la guardia non capiva una parola") e il modello decide per ogni battuta. Per ogni personaggio
puoi indicare il **genere** (una lingua che marca il genere lo sbaglia se la parte dice solo "lei") e la **voce** ("formale, secco, rozzo"), che il
modello abbina ai registri della lingua. Le modifiche vengono salvate subito nel libro.

Ogni personaggio può avere anche un livello di **comprensione**: *non capisce nulla*, *conosce qualche parola*, *segue la maggior parte* o *segue tutto*. Lascialo vuoto (o *segue tutto*) e ai
modelli non viene detto nulla. Usalo per le persone che sentono la lingua senza parlarla: *Tomáš: non capisce nulla*, *Altri abitanti del villaggio: segue la maggior parte* (il nome è solo testo, quindi va bene anche un gruppo). I livelli sono
**statici per tutto il libro** e vengono forniti ai modelli come informazione; ciò che un personaggio ne fa spetta al modello, e puoi sempre dire di più nelle istruzioni ("Tomáš non capisce quello che dicono").
Raggiungono i modelli in due modi: il **modello della storia** riceve un breve messaggio con i livelli (inserito nel prompt come l'Author's Note; la casella sopra l'elenco lo disattiva, e il numero indica da quanti messaggi dalla fine viene inserito) e il modello che **revisiona una parte** (*Annota con l'IA*) li riceve come una sezione dei fatti, così può segnalare un personaggio che reagisce a parole che non riesce a seguire. Se preferisci collocare tu il testo, disattiva l'inserimento e usa la [macro](../templates/macros.md#commands) {%{`{{command::conlang-understanding}}`}%} in un template; accetta una lista facoltativa di personaggi da mantenere.

![La scheda Conlang: i parlanti, e due personaggi che non seguono la lingua](../../images/plugin-conlang-tab.png)

<a id="languages"></a>

### Lingue

Le altre lingue del libro, oltre a quella che il suo campo **Lingua del parlato** indica: vedi [Più di una lingua in un libro](#more-than-one-language-in-a-book).

<a id="quick-translate"></a>

### Traduzione rapida

Scrivi un testo in inglese (una frase, un nome, una battuta che non hai ancora scritto), facoltativamente chi la dice e a chi, e premi **Traduci**: il modello la traduce
nella lingua del libro con le regole, i registri e il lessico, e con le note dei personaggi citati nel testo. Nulla viene scritto nella storia. Il risultato è la
traduzione, una nota sulle scelte e le **parole di cui la traduzione aveva bisogno** che mancavano nel lessico, ciascuna con il suo significato e il motivo; seleziona quelle che vuoi e **Importa le parole selezionate**:
entrano nel lessico come *nuove*, come le parole di una parte tradotta. Le parole della traduzione che non sono né nel lessico né segnalate vengono elencate come avviso.

<a id="more-than-one-language-in-a-book"></a>

## Più di una lingua in un libro

Un poema epico può avere una lingua per gli elfi, un'altra per i nani e una terza per gli orchi. Un libro indica **prima una lingua** (il campo qui sopra, che può essere impostato anche da una cartella o dalle tue impostazioni)
e la scheda **Lingue** della scheda **Conlang** aggiunge **altre lingue** dalla tua libreria, una alla volta, e le toglie di nuovo.

- **Ogni lingua ha i propri parlanti.** La scheda *Parlanti* riguarda la lingua che sceglie il campo in alto. La prima lingua è parlata da tutti tranne dai personaggi che elenchi, come prima;
  **una lingua che aggiungi parte come "la parlano solo i personaggi elencati" con nessuno nell'elenco**, quindi non viene richiesta finché non indichi chi la parla. Elenca un personaggio come eccezione della prima lingua quando un'altra lingua è la sua,
  altrimenti per il suo parlato viene richiesta anche la prima lingua (un parlato che due lingue rivendicano resta a quella richiesta per prima). Un personaggio può parlare più lingue
  (elencalo in entrambe; il testo della parte indica al modello in quale lingua è una battuta).
- **Una richiesta per ogni lingua.** Una richiesta contiene le regole e il lessico di una sola lingua, e solo i parlati dei suoi parlanti; una parte con il parlato di due lingue richiede due
  richieste, una dopo l'altra. La seconda legge la parte con l'inglese dei parlati della prima e lascia tradotti quei parlati. Una parte il cui punto di vista e i cui personaggi presenti sono tutti fuori dai parlanti di una
  lingua non viene richiesta per quella lingua (una parte che non nomina alcun personaggio viene richiesta per ogni lingua che ha dei parlanti).
- **Un passaggio conosce la propria lingua.** Il popover, l'esportazione e il visualizzatore funzionano come prima; **Traduci dialoghi (lingua costruita)** per una sola lingua lascia intatti i parlati delle altre, e **Rimetti l'inglese**
  può riguardare una lingua o tutte.
- **I menu chiedono quale lingua** quando il libro ne ha più di una: il menu di una parte (*Traduci dialoghi (lingua costruita)*: una delle lingue oppure *Tutte le lingue*) e il clic destro su una frase selezionata (una lingua).
  **Traduci parti (lingua costruita)** ha un campo *Lingua* (tutte le lingue per impostazione predefinita); *le parti non tradotte* significa "non tradotte in quella lingua".
- **L'esecuzione automatica** traduce una nuova parte in ogni lingua i cui parlanti possono esservi presenti.
<a id="translating"></a>

## Traduzione

- **A mano:** il menu di una parte ha il sottomenu **Traduci** (anche il clic destro sul testo ne ha uno), e al suo interno **Traduci dialoghi (lingua costruita)** (queste voci e **Traduci parti** compaiono solo in un libro che ha una lingua di questo plugin). Una parte già tradotta viene tradotta di nuovo **a partire dal suo inglese**: è quindi anche il modo per ritradurre una parte dopo che le regole o il lessico sono cambiati. Un libro con più di una lingua chiede prima [quale](#more-than-one-language-in-a-book).
- **Da sola:** con **Traduci automaticamente una nuova parte**, una parte generata viene tradotta non appena è definitiva. Una *rigenerazione* di una parte le dà un nuovo inglese e rimuove la vecchia traduzione.
- **Battute che il modello ha saltato:** a volte una traduzione lascia una battuta in inglese. **Traduci → Traduci dialoghi mancanti (lingua costruita)** nel menu di una parte traduce solo le battute che sono ancora in inglese e lascia com'erano quelle già tradotte; con del testo selezionato, il clic destro **Traduci → Traduci dialoghi mancanti nella selezione (lingua costruita)** fa lo stesso per le battute che la selezione tocca. Al modello viene inviata la parte così com'è, con le battute tradotte al suo interno, e gli si chiedono solo le battute ancora in inglese, quindi l'operazione costa meno di una traduzione dell'intera parte. Non viene scritto nient'altro che battute, quindi non è un modo per tradurre una frase che il modello non riconosce come battuta: per quello usa *Traduci il testo selezionato*.
- **Una frase che hai scritto:** selezionala nel testo di una parte, fai clic destro e scegli **Traduci → Traduci il testo selezionato (lingua costruita)**. Il modello traduce il testo selezionato (gli spazi e una coppia di virgolette attorno ad esso sono esclusi) con la lingua, le note dei personaggi e la storia intorno; il testo viene sostituito dalla traduzione e l'inglese viene conservato come fonte della battuta, così un clic su di essa mostra l'inglese come per ogni battuta tradotta. Le parole che il modello ha dovuto inventare vengono aggiunte al lessico come *nuove*, come per una parte tradotta. Una selezione che contiene una battuta tradotta viene rifiutata (rimetti prima l'inglese), e così pure una selezione fatta in una parte che nel frattempo è cambiata.
- **Un intervallo:** **Traduci parti (lingua costruita)** nel menu strumenti della storia apre una finestra che resta aperta accanto alla storia.

  ![Traduci parti](../../images/plugin-conlang-range.png)

  Scegli **Dalla parte** e **Alla parte** (parti con testo, numerate da 1 come nell'esportazione) e cosa fare: *Traduci le parti non tradotte*, *Traduci di nuovo le parti tradotte* (dopo che le regole o il lessico sono cambiati), oppure *Rimetti l'inglese*, dopo una domanda. La finestra indica quante parti contiene l'intervallo e quante ne userà l'operazione, mostra *Parte n di m*, e un pulsante **Interrompi** che ha effetto dopo la parte in lavorazione (una richiesta al modello già avviata non può essere annullata). Una parte che fallisce viene conteggiata e l'operazione prosegue; una lingua o un modello mancanti la terminano. La storia viene ridisegnata al termine dell'operazione. Una parte che modifichi mentre l'operazione è in corso fa fallire quella sola parte, non l'intera operazione.

Una traduzione viene memorizzata come ogni modifica del testo: la cronologia della parte riceve una versione, la parte **non viene marcata come modificata** e l'inglese viene conservato. **Una battuta che hai modificato a mano viene lasciata com'è** quando la parte viene ritradotta o riportata all'inglese, perché contiene le tue parole.

!!!
Una traduzione richiede tanto tempo quanto il modello impiega per la parte: minuti con un modello locale che ragiona. L'estensione chiede al modello di ragionare (la traduzione di genere, numero e registro riesce molto meglio così), quindi concedigli un limite di output generoso.
!!!

<a id="what-the-reader-sees"></a>

## Cosa vede il lettore

<a id="in-the-story"></a>

### Nella storia

Una battuta tradotta ha una **linea tratteggiata**. Un clic su di essa mostra l'inglese in un popover: chi la pronuncia, e *(non compresa dall'ascoltatore)* quando l'ascoltatore non capisce la lingua; una nota rossa indica che hai modificato il testo nella lingua costruita dopo la traduzione, quindi l'inglese potrebbe non dire più ciò che dice ora.

**Modifica la fonte** (un pulsante nel popover, solo nelle tue viste; i lettori di un libro pubblicato non lo vedono) apre una piccola finestra con la traduzione così come è nella storia e l'inglese che leggono i modelli. Usala quando un parlante nativo ha corretto a mano la traduzione e l'inglese ora dice altro, oppure quando il modello ha reso male l'inglese. Salvando, la battuta riceve il suo nuovo inglese, la traduzione attuale diventa quella a cui appartiene l'inglese (la nota rossa scompare) e da quel momento i modelli leggono il nuovo inglese al suo posto. Il testo della parte non cambia, e *Traduci dialoghi (lingua costruita)* di nuovo sostituisce entrambi. `conlang-edit-source` fa lo stesso dalla riga di comando.

![La finestra che modifica la fonte di una battuta](../../images/plugin-conlang-source.png)

**Cerca e sostituisci nelle fonti** (il pulsante con le frecce in cima alla scheda Conlang) apre la finestra di ricerca e sostituzione dell'applicazione sulle fonti inglesi delle battute tradotte del ramo attivo (testo semplice o espressione regolare, con o senza distinzione di maiuscole; la tabella elenca le battute con una corrispondenza per parte e numero di battuta). Usala per un nome scritto in due modi diversi nell'inglese. Cambiano solo le fonti; per modificare le parole della traduzione stessa usa la ricerca e sostituzione del libro. Una battuta la cui fonte è stata modificata dopo la ricerca viene lasciata com'è e conteggiata.

![Ricerca e sostituzione sulle fonti inglesi delle battute](../../images/plugin-conlang-search-replace.png)

![L'inglese dietro una battuta tradotta](../../images/plugin-conlang-story.png)

<a id="the-viewer"></a>

### Il visualizzatore

Il visualizzatore di un [libro pubblicato](../books/publishing.md) mostra la battuta tradotta nel modo richiesto dal libro, con il campo **I lettori del libro pubblicato vedono** (una scelta del libro o delle sue cartelle; i tuoi valori predefiniti non decidono mai cosa vede un lettore):

| | |
|---|---|
| **La lingua costruita, l'inglese al clic** | Il default. Un clic (un tocco su un telefono) apre il popover con l'inglese. |
| **Solo la lingua costruita** | L'inglese non viene mai inviato al browser del lettore. |
| **Solo l'inglese** | Il libro come se non fosse mai stato tradotto. |

Nella modalità con clic la pagina contiene solo la lingua costruita; l'inglese di una battuta viene richiesto al server quando il lettore ci clicca.

![L'inglese dietro una battuta tradotta, nel visualizzatore](../../images/plugin-conlang-viewer.png)

<a id="exporting"></a>

## Esportazione

Quando il libro contiene battute tradotte, la finestra di [esportazione](../books/export.md) ha il campo **Battute tradotte**:

![La finestra di esportazione](../../images/plugin-conlang-export.png)

| | |
|---|---|
| **La lingua costruita, così com'è** | Il default: il testo così come è memorizzato. |
| **Solo l'inglese** | La battuta viene sostituita dal suo inglese. |
| **La lingua costruita, l'inglese nelle note a piè di pagina** | Ogni battuta tradotta riceve una nota a piè di pagina con l'inglese (e chi la pronuncia). |
| **L'inglese, la lingua costruita nelle note a piè di pagina** | Il contrario. |

Le note a piè di pagina sono le [note dell'esportazione](../books/export.md#footnotes): note collegate in HTML ed EPUB, note finali dopo ogni parte nel testo semplice, vere note a piè di pagina in Word e PDF. Una battuta che hai modificato dopo la sua traduzione resta com'è e non ha nota. Il testo memorizzato del libro non viene mai modificato da un'esportazione.
<a id="commands"></a>

## Comandi

Tutto ciò che fanno le schermate è anche un comando, per la [riga di comando](../commands/index.md) e per gli [agenti](../agents.md): un agente può creare una lingua, ampliarla, tradurre un libro e verificare il risultato senza
che una persona debba rispondere ai dialoghi. Una lingua si indica con il suo **nome** (le maiuscole non contano) o con il suo id; una sezione delle regole con il suo titolo o con il suo numero in `conlang-list-rules`; una categoria con il suo nome o numero; una parola con la sua forma (l'id di `conlang-list-words` serve solo a distinguere gli omofoni); una parte come negli altri comandi dell'applicazione, con il suo handle o con la sua posizione nel ramo attivo (la prima parte è la 1). La riga di comando accetta tutti
gli argomenti di un comando; un agente può omettere quelli facoltativi.

| Comando | |
|---|---|
| `conlang-list`, `conlang-get`, `conlang-create`, `conlang-duplicate`, `conlang-update`, `conlang-delete`, `conlang-export`, `conlang-import`, `conlang-set-references` | La libreria. `conlang-delete` chiede conferma e non è disponibile a un agente. |
| `conlang-list-name-categories`, `conlang-add-name-category`, `conlang-update-name-category`, `conlang-delete-name-category`, `conlang-preview-names` | Le categorie di nomi per il [Generatore di nomi](name-generator.md) (la scheda **Nomi**). Aggiunta e modifica accettano le parole del lessico (separate da `;`) oppure un'intera categoria del lessico; l'eliminazione chiede conferma all'utente; l'anteprima richiede il Generatore di nomi. I comandi `list-name-categories` e `generate-names` del generatore stesso elencano e creano i nomi. |
| `conlang-set-book-language` | La lingua del libro aperto (il campo **Language of the speech** delle sue *Impostazioni dell'estensione*). |
| `conlang-add-book-language`, `conlang-remove-book-language` | Le altre lingue del libro aperto. La rimozione chiede conferma all'utente e non è disponibile a un agente. |
| `conlang-list-rules`, `conlang-get-rules`, `conlang-add-rules`, `conlang-update-rules`, `conlang-delete-rules`, `conlang-move-rules` | Le sezioni delle regole. |
| `conlang-list-categories`, `conlang-add-category`, `conlang-update-category`, `conlang-move-category`, `conlang-delete-category` | Le categorie delle parole. |
| `conlang-list-registers`, `conlang-add-register`, `conlang-update-register`, `conlang-delete-register` | I registri. |
| `conlang-list-words` | Il lessico, a pagine, con filtri: categoria, stato (*new*, accepted, locked, retired) e un testo. |
| `conlang-get-word`, `conlang-add-word`, `conlang-update-word`, `conlang-delete-word` | Una parola. Una parola che un agente aggiunge o modifica appartiene al modello ed è **new**; una parola bloccata non si può modificare né eliminare. |
| `conlang-lock-word`, `conlang-unlock-word`, `conlang-retire-word`, `conlang-restore-word`, `conlang-accept-words` | Lo stato delle parole. |
| `conlang-check-lexicon` | Significati ripetuti, parole senza categoria, composti con parti che non sono nel lessico e omofoni (a titolo informativo). Non usa il modello. |
| `conlang-status` | La lingua del libro e le parti già tradotte. |
| `conlang-translate-part`, `conlang-translate-range`, `conlang-translate-sentence`, `conlang-translate-text`, `conlang-revert-to-source` | Traduzione: una parte (`missingOnly` traduce solo i discorsi ancora in inglese, `within` cita un passaggio per limitare la traduzione a quel passaggio), un intervallo (`mode` *new* oppure *again*), una frase di una parte, un testo che viene solo restituito, e il ritorno all'inglese. Un intervallo chiede conferma all'utente per *again*; la stessa cosa vale per il ritorno all'inglese. Con l'argomento facoltativo `language` agiscono su una lingua del libro (su tutte per impostazione predefinita, sulla prima per una frase o un testo). |
| `conlang-edit-source` | Imposta il testo inglese di origine di un discorso tradotto (`part`, `speech` come lo numera `conlang-list-passages`, `source`); la traduzione così com'è ora conta come quella a cui appartiene. Sostituisce la vecchia fonte. |
| `conlang-check-translation`, `conlang-get-source`, `conlang-list-passages` | Lettura: le parole di un discorso tradotto che il lessico non contiene, il testo di una parte come lo leggono i modelli (l'inglese), e i discorsi tradotti di una parte con il loro inglese. |
| `conlang-speakers`, `conlang-set-speakers-mode`, `conlang-set-speaker`, `conlang-remove-speaker` | Chi parla la lingua nel libro aperto (`language` indica una delle lingue del libro, la prima per impostazione predefinita); `conlang-set-speaker` imposta anche il livello di `understands`. |
| `conlang-set-understanding-prompt` | Se i livelli di comprensione vengono inseriti da soli nel prompt della storia e quanti messaggi dalla fine. |
| `conlang-design-words`, `conlang-design-rules`, `conlang-rewrite-rules`, `conlang-design-stage`, `conlang-ask-designer`, `conlang-try-sentences` | Il progettista. Con `ask true` un comando restituisce la proposta come elenco numerato di modifiche e la trattiene; non cambia nulla. |
| `conlang-review` | La revisione della lingua da parte del progettista: solo i rilievi. |
| `conlang-apply-changes` | Applica le modifiche trattenute, tutte oppure quelle con i numeri indicati (come **Applica** nella revisione). |
| `conlang-pending-changes`, `conlang-take-pending-changes`, `conlang-discard-pending-changes` | Ciò che una esecuzione di agente ha lasciato nel libro, vedi [più sotto](#agent-runs). |
| `conlang-understanding` | Chi non segue quale lingua, come testo, per la macro {%{`{{command::conlang-understanding::<characters>}}`}%} (i personaggi sono facoltativi). |
| `conlang-lexicon`, `conlang-rules` | Il lessico e le regole che una richiesta di traduzione invia, come testo, per le [macro](../templates/macros.md#commands) {%{`{{command::conlang-lexicon::<language>}}`}%} e {%{`{{command::conlang-rules::<language>}}`}%}. |

<a id="agent-runs"></a>

### Esecuzioni degli agenti

Un agente lavora su una **copia del libro**, e le lingue non fanno parte del libro: un agente quindi non può cambiare la tua libreria alle tue spalle. La prima volta che un'esecuzione **modifica** una lingua (una parola, una regola o le parole che una traduzione inventa), quella lingua viene copiata nella copia del libro, insieme alla versione da cui è stata copiata; da quel momento l'esecuzione legge e scrive sulla propria copia. Un'esecuzione che soltanto legge, o che traduce solo con le parole che già ha, non copia mai nulla. La copia segue la copia del libro: i punti di salvataggio e il loro ripristino, la revisione dell'esecuzione e la sua eliminazione.

Quando **applichi** il risultato dell'esecuzione, la lingua finisce nel libro, **non nella tua libreria**. La scheda **Conlang** del libro mostra allora *An agent run changed the language ...* con **Revisione** e **Scarta**.
Revisione confronta tre versioni: la lingua al momento in cui l'esecuzione l'ha copiata, la versione dell'esecuzione e la tua libreria così com'è ora; quindi ciò che nel frattempo hai cambiato nella libreria viene mantenuto, e ciò che avete cambiato entrambi è un conflitto che decidi tu. Lo stesso dai comandi: `conlang-pending-changes` elenca le modifiche come numeri e `conlang-take-pending-changes` le prende (chiede conferma). Una lingua creata dall'esecuzione viene aggiunta per intero dopo la tua conferma.

L'esecuzione legge la libreria finché non modifica per la prima volta una lingua, e poi la propria copia: ciò che cambi in una lingua mentre l'esecuzione lavora non viene visto dall'esecuzione, ma non va perso.

<a id="limits"></a>

## Limiti

- **La qualità è quella del modello.** L'estensione verifica gratuitamente ciò che può (che il discorso citato sia nella parte, la lingua, le parole rispetto al lessico e alle sue regole per composti e affissi) e segnala il resto; non controlla la tua grammatica. Prova una lingua su alcune parti prima di un'esecuzione lunga, e usa **Revisiona la lingua** del progettista quando un errore si ripete.
- **Una lingua è condivisa dai libri che la usano.** Una parola che modifichi, o una parola che una traduzione aggiunge, cambia la lingua per ogni libro della serie. È voluto (un solo mondo), e **Duplica** serve a tenere una lingua separata.
- **Le modifiche alle lingue di un agente restano nel libro.** Le porti nella libreria tu, dopo aver applicato l'esecuzione; una lingua non viene bloccata mentre un'esecuzione lavora.
- **I comandi non sostituiscono una parola nella storia**, e una ritraduzione non può conservare le traduzioni il cui inglese non è cambiato (ogni parte dell'intervallo viene richiesta di nuovo).
- **Su un telefono** sono disponibili il pannello, l'editor, la revisione e la procedura guidata, ma la griglia del lessico scorre in orizzontale; il tocco su un discorso nel visualizzatore funziona; il lato storia (il menu di una parte, la finestra dell'intervallo, il clic nella storia) non è stato provato lì.
