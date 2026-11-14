---
label: Cartelle
order: 98
---

# Cartelle

Le cartelle ordinano i tuoi libri come le cartelle su un disco ordinano i file. Una cartella contiene libri e altre cartelle, con la profondità che preferisci, e l'elenco dei libri mostra una cartella alla volta. Una cartella può anche contenere **impostazioni** (lingua, punto di vista, tempo verbale, stile, i modelli e i prompt): i libri al suo interno, e quelli nelle cartelle sottostanti, scrivono con queste impostazioni a meno che non ne abbiano di proprie. È questo il motivo principale per usare le cartelle: imposti lo stile di una serie una sola volta, e ogni libro della serie lo eredita.

![La scheda Books con le cartelle](../../images/books-folders.png)

<a id="moving-around"></a>

## Spostarsi

Sopra i libri, l'elenco mostra le cartelle di quella corrente, nell'ordine delle colonne ordinate. Fai clic sul **nome** di una cartella per aprirla. Il percorso di pulsanti sopra l'elenco (`Books / Sci-fi / Expanse`) indica dove ti trovi; fai clic su una parte di esso per tornare indietro.

La casella **Includi le sottocartelle** elenca tutti i libri sotto la cartella corrente, a qualsiasi profondità (al livello più alto, sono tutti i tuoi libri), e aggiunge una colonna **Cartella** con la posizione di ciascuno. La cartella corrente e la casella restano memorizzate anche se ricarichi la pagina.

Una riga di cartella mostra quanti libri ci sono sotto di essa, a qualsiasi profondità. Una riga di libro mostra le parole e i token del suo [ramo attivo](branches-and-story-tree.md). I filtri **Nome** e **Tag** funzionano sulle cartelle oltre che sui libri (le cartelle hanno tag, vedi [Taggare le cartelle](#tagging-folders)), nella cartella corrente, oppure sotto di essa con *Includi le sottocartelle*.

La **Ultima modifica** di una cartella segue i suoi libri: quando un libro al suo interno, o in una cartella sottostante, cambia (o cambia una delle sue parti), anche la cartella viene aggiornata. Ordina per questa colonna per vedere dove hai scritto per ultimo. Aprire un libro o spostarlo non conta.

<a id="what-the-buttons-do"></a>

### Cosa fanno i pulsanti

| Pulsante | |
|---|---|
| **Nuova cartella** (intestazione) | Crea una cartella in quella corrente. Il nome non deve essere vuoto, superare 255 caratteri o contenere un `/`, e non deve essere il nome di un'altra cartella nello stesso posto (le maiuscole non contano). |
| l'ingranaggio di una cartella | Le [impostazioni della cartella](#folder-settings). |
| la matita di una cartella | Ne cambia il nome. |
| il cestino di una cartella | La elimina, vedi [Eliminare una cartella](#deleting-a-folder). |
| **Sposta nella cartella** (di un libro) | Sceglie la cartella in cui inserire il libro. |
| la stella di un libro | Lo rende un [preferito](#favorites). |
| **Aggiungi libro**, le importazioni | Il nuovo libro va nella **cartella corrente**. Un [clone](managing-books.md#cloning-a-book) viene messo accanto al suo originale. |

<a id="folder-settings"></a>

## Impostazioni della cartella

L'ingranaggio di una cartella apre le sue impostazioni (anche la finestra **Organizza** ha lo stesso pulsante, con il tasto `i`):

![Le impostazioni di una cartella](../../images/folder-settings.png)

- **Nome**, **Descrizione** e **Tag**.
- **Lingua**, e gli stessi campi della scheda [Prompt](prompts.md) di un libro: modello principale, punto di vista, tempo verbale, stile e prompt.
- **Versioni conservate per parte**: quante versioni precedenti di una parte la [cronologia](version-history.md#how-many-versions-are-kept) conserva per i libri della cartella e per le cartelle sottostanti. Tra tutti i limiti sopra un libro, si applica quello più restrittivo.
- **Impostazioni dell'estensione**, quando una [estensione](../extensions/index.md) caricata ha impostazioni che i libri ereditano, vedi [Impostazioni delle estensioni](prompts.md#settings-of-extensions).

Tutto è facoltativo. Un campo vuoto è *ereditato*, e lo dichiara: il testo grigio nel campo è il valore che eredita, e la riga sotto indica da dove proviene. Premi **Salva** per salvare la cartella; il nome e la descrizione si possono cambiare in qualsiasi momento, ma una modifica della lingua o di un'impostazione di testo viene rifiutata mentre un agente lavora su un libro della cartella o di una sottostante: il messaggio indica il libro, quindi [metti in pausa o ferma la sua esecuzione](../agents.md) prima.

<a id="where-the-settings-of-a-book-come-from"></a>

### Da dove vengono le impostazioni di un libro

Per ciascuna di queste impostazioni, un libro usa la prima che non sia vuota tra le seguenti:

1. il libro stesso (le sue schede *Informazioni* e *Prompt*),
2. la **cartella** in cui si trova il libro,
3. la cartella sopra quella, e così via fino alla cima,
4. le tue **impostazioni utente** (*Settings → Templates*, vedi [Account e impostazioni](../account.md)),
5. il **predefinito integrato**.

Un testo vuoto conta come non impostato. Questa ricerca avviene quando serve (quando si genera una parte, si crea un riassunto, si riscrive una parte...), e nulla viene copiato nel libro quando lo inserisci in una cartella: **spostare un libro in un'altra cartella cambia il modo in cui scrive**, e cambiare un'impostazione di una cartella cambia ogni libro sottostante che non ne imposta una propria.

La scheda [Prompt](prompts.md#inherited-settings) di un libro lo mostra: sotto ogni campo c'è l'indicazione di da dove proviene il suo valore (*Inherited from the folder "Sci-fi"*, *Ereditato dalle tue impostazioni* oppure *Predefinito integrato*).

<a id="language"></a>

### Lingua

La casella **Lingua** di un libro (la scheda [Informazioni](managing-books.md#the-about-tab)) e di una cartella può essere lasciata vuota, il che significa *eredita*: il libro prende la lingua della cartella più vicina sopra di esso che ne imposta una, altrimenti l'inglese. Una casella vuota mostra quale lingua è e da dove proviene. La casella ha un pulsante di cancellazione; cancellarla torna a ereditare (in passato diventava inglese). Tutto ciò che mostra o usa la lingua di un libro usa quella che ha insieme alle sue cartelle: la sillabazione del testo, l'esportazione e la tabella [Mass Change Language](#mass-changes).

<a id="deleting-a-folder"></a>

## Eliminare una cartella

Il cestino di una cartella chiede cosa fare:

- **una cartella vuota** viene eliminata dopo una conferma;
- **una cartella con libri o cartelle al suo interno** offre **Sposta il contenuto nella cartella superiore** (l'opzione predefinita) oppure **Delete everything in it as well**. La seconda chiede di nuovo, con il numero di libri e cartelle, e manda i libri nel [Cestino](../trash.md), da dove puoi recuperarli. La cartella eliminata in sé non compare nel Cestino.

Se una cartella interna ha lo stesso nome di una cartella sopra di essa, lo spostamento del contenuto verso l'alto viene rifiutato (i nomi sono unici): rinominane prima una delle due. Una cartella con un libro su cui lavora un agente non può essere eliminata o spostata, perché le impostazioni di quel libro cambierebbero durante l'esecuzione.
<a id="organise"></a>

## Organizza

**Organizza** (nel menu **Gestisci** dell'intestazione dell'elenco) apre una finestra con due riquadri, ciascuno con una cartella, per spostare e copiare libri e cartelle da uno all'altro senza aprire ciascuno. Si apre con il riquadro sinistro sulla cartella in cui eri e il riquadro destro su quella mostrata l'ultima volta.

![La finestra Organizza](../../images/organise-dialog.png)

| Tasto | |
|---|---|
| Sinistra / Destra / Tab | L'altro riquadro. |
| Su / Giù | Il cursore. |
| Invio | Apre la cartella sotto il cursore. |
| Backspace | Risale di un livello. |
| Spazio o Insert | Contrassegna la voce sotto il cursore; Maiusc + Su / Giù contrassegna durante lo spostamento. |
| Clic su una casella di controllo | Contrassegna o deseleziona la voce; **Maiusc + clic** contrassegna (o deseleziona) ogni voce compresa tra essa e la casella cliccata in precedenza. |

Una riga di pulsanti in basso agisce su ciò che è **contrassegnato** (o, se non c'è nulla di contrassegnato, su ciò che si trova sotto il cursore) e mostra il relativo tasto tra parentesi:

| Pulsante | Tasto | |
|---|---|---|
| **Copia** | `c` | Copia nella cartella dell'*altro* riquadro. |
| **Sposta** | `m` | Sposta nella cartella dell'altro riquadro. |
| **Nuova cartella** | `n` | Crea una cartella nella cartella del riquadro attivo. |
| **Rinomina** | `Shift+r` | Rinomina la cartella o il libro sotto il cursore. |
| **Elimina** | `Delete` | Elimina, come nell'elenco. |
| **Impostazioni della cartella** | `i` | Le [impostazioni](#folder-settings) della cartella sotto il cursore. |
| **Cambia in massa modello/protocollo**, **Cambia la lingua in massa**, **Rinomina di massa**, **Tagga i libri** | `p`, `l`, `Shift+n`, `t` | Le [modifiche di massa](#mass-changes) per i libri contrassegnati, o per i libri della cartella del riquadro attivo (con la propria opzione *Includi le sottocartelle*). I riquadri mostrano i nuovi nomi dopo una rinomina di massa. |
| **Ricerca avanzata** | `f` | La [ricerca](managing-books.md#advanced-search) nella cartella del riquadro attivo, facoltativamente con *Includi le sottocartelle*. Un risultato ha **Mostra nella cartella**, che porta il riquadro lì e contrassegna il libro. |
| **Mostra la cartella nell'elenco** | `o` | Chiude la finestra e apre la cartella del riquadro attivo nell'elenco dei libri. |

Quando chiudi la finestra, l'elenco dei libri viene riletto, quindi ciò che hai creato, spostato, copiato, rinominato o eliminato compare subito (se la cartella in cui si trovava l'elenco non esiste più, si passa al livello più alto).

I tasti sono azioni delle [scorciatoie da tastiera](../account.md#keyboard-shortcuts) in *Impostazioni*: puoi modificarle o eliminarle lì (non ci sono tasti funzione predefiniti, perché un Mac e i browser li utilizzano). I pulsanti mostrano il tasto che hai impostato.

<a id="copying"></a>

### Copia

Una **copia** di un libro è un [clone](managing-books.md#cloning-a-book); la finestra chiede se copiare solo il ramo corrente. Una copia di una cartella è una nuova cartella con le impostazioni, la descrizione e i tag dell'originale e con copie di tutti i libri e di tutte le cartelle che contiene. Una copia può richiedere tempo, quindi viene eseguita con una barra di avanzamento che non può essere interrotta. Se si interrompe a metà, le copie già fatte vengono mantenute, ciascuna completa.

Uno **spostamento** avviene subito e può essere annullato spostando di nuovo indietro. Una cartella non può essere spostata dentro sé stessa o in una cartella sottostante.

<a id="when-a-name-is-taken"></a>

### Quando un nome è già in uso

Quando la destinazione ha già una cartella con lo stesso nome, la finestra chiede cosa fare di quella cartella, con **Applica a tutti** per le restanti:

| Scelta | |
|---|---|
| **Rinomina** | La cartella riceve un altro nome. |
| **Salta** | Resta dov'è (e non viene copiata). |
| **Unisci** | Ciò che contiene viene inserito nella cartella già presente; le cartelle con lo stesso nome al suo interno vengono unite a loro volta. |

I libri non entrano mai in conflitto: i loro nomi possono ripetersi.

<a id="mass-changes"></a>

## Modifiche di massa

Il menu **Gestisci → Modifiche di massa** dell'elenco (e i pulsanti di Organizza) modifica molti libri in una volta sola. **Cambia in massa modello/protocollo** applica un modello e/o un protocollo ai libri, **Cambia la lingua in massa** una lingua, **Rinomina di massa** aggiunge un prefisso o un suffisso ai loro nomi, sostituisce un testo al loro interno o li numera, **Tagga i libri** e **Tagga le cartelle** aggiungono o rimuovono un tag. Vedi [Gestire i libri](managing-books.md#tagging-many-books-at-once). Dall'elenco lavorano sui libri della cartella corrente, con la propria opzione **Includi le sottocartelle**.

<a id="favorites"></a>

## Preferiti

La stella su una riga di libro lo rende un **preferito**. Finché ne hai almeno uno, il livello più alto mostra la cartella virtuale **Preferiti** (una stella) con i libri preferiti di tutte le tue cartelle. È solo una vista: non puoi creare un libro o una cartella al suo interno, e togliere la stella rimuove il libro da essa, non dalla sua cartella. I preferiti sono conservati nelle tue impostazioni.

<a id="tagging-folders"></a>

## Taggare le cartelle

Le cartelle hanno tag come i libri. **Gestisci → Modifiche di massa → Tagga le cartelle** applica tag a molte cartelle in una volta sola. I tag di una cartella non modificano i tag dei suoi libri e non fanno parte di un [backup](backups.md).

<a id="folders-and-other-features"></a>

## Cartelle e altre funzionalità

- **Backup.** Un [backup](backups.md#backups-and-folders) ricorda le cartelle del suo libro, e un file esportato può contenere le impostazioni che il libro eredita.
- **Agenti.** Un [agente](../agents.md#agents-and-folders) scrive con le impostazioni delle cartelle del suo libro.
- **Assistente di aiuto.** Se lo consenti, l'[assistente](../administration/help-assistant.md#your-books) può consultare i tuoi libri e le tue cartelle.
- **Telefono.** Il [layout per telefono](../phone.md#books) mostra le stesse cartelle come schede e permette di crearle, rinominarle, configurarle ed eliminarle; Organizza e le modifiche di massa si trovano nel menu **⋮** della sua intestazione.
- **Visualizzatore.** Il [visualizzatore](publishing.md#folders-in-the-list) elenca anche le cartelle e si apre in quella che hai lasciato.
