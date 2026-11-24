---
label: Cartelle
order: 98
---

# Cartelle

Le cartelle ordinano i tuoi libri come le cartelle su un disco ordinano i file. Una cartella contiene libri e altre cartelle, a qualsiasi profondità, e l'elenco dei libri mostra una cartella alla volta. Una cartella può anche contenere **impostazioni** (lingua, punto di vista, tempo verbale, stile, i modelli e i prompt): i libri che vi si trovano, e quelli delle cartelle sottostanti, scrivono con esse a meno che non abbiano le proprie. Questo è il motivo principale per usare le cartelle: imposti una volta lo stile di una serie, e ogni libro della serie lo possiede.

![La scheda Books con le cartelle](../../images/books-folders.png)

<a id="moving-around"></a>

## Spostarsi

Sopra i libri, l'elenco mostra le cartelle di quella corrente, nell'ordine delle colonne ordinate. Fai clic sul **nome** di una cartella per aprirla. Il percorso di pulsanti sopra l'elenco (`Books / Sci-fi / Expanse`) mostra dove ti trovi; fai clic su una sua parte per tornare lì.

La casella **Include subfolders** elenca ogni libro sotto la cartella corrente, a qualsiasi profondità (al livello più alto sono tutti i tuoi libri), e aggiunge una colonna **Folder** con il percorso di ciascuno. La cartella corrente e la casella vengono ricordate, anche quando ricarichi la pagina.

Una riga di cartella mostra quanti libri ci sono sotto di essa, a qualsiasi profondità. Una riga di libro mostra le parole e i token del suo [ramo attivo](branches-and-story-tree.md). I filtri **Name** e **Tags** funzionano sulle cartelle oltre che sui libri (le cartelle hanno tag, vedi [Assegnare tag alle cartelle](#tagging-folders)), nella cartella corrente, o sotto di essa con *Include subfolders*.

La **Last Modified** di una cartella segue i suoi libri: quando un libro al suo interno, o in una cartella sottostante, cambia (o cambia una sua parte), anche la cartella viene aggiornata. Ordina per questa colonna per vedere dove hai scritto per ultimo. Aprire un libro, o spostarlo, non conta.

<a id="what-the-buttons-do"></a>

### Cosa fanno i pulsanti

| Pulsante | |
|---|---|
| **New folder** (intestazione) | Crea una cartella in quella corrente. Il nome non deve essere vuoto, non deve superare 255 caratteri né contenere un `/`, e non deve essere il nome di un'altra cartella nello stesso posto (le maiuscole non contano). |
| la rotella di una cartella | Le [impostazioni della cartella](#folder-settings). |
| la penna di una cartella | Rinomina la cartella. |
| il cestino di una cartella | Elimina la cartella, vedi [Eliminare una cartella](#deleting-a-folder). |
| **Move to folder** (di un libro) | Sceglie la cartella in cui mettere il libro. |
| la stella di un libro | Lo rende un [preferito](#favorites). |
| **Add Book**, le importazioni | Il nuovo libro finisce nella **cartella corrente**. Un [clone](managing-books.md#cloning-a-book) viene messo accanto al suo originale. |

<a id="folder-settings"></a>

## Impostazioni della cartella

La rotella di una cartella apre le sue impostazioni (la finestra **Organise** ha lo stesso pulsante, con il tasto `i`):

![Le impostazioni di una cartella](../../images/folder-settings.png)

- **Name**, **Description** e **Tags**.
- **Language**, e gli stessi campi della scheda [Prompts](prompts.md) di un libro: modello principale, punto di vista, tempo verbale, stile e i prompt.
- **Versions kept per part**: quante versioni precedenti di una parte conserva la [cronologia](version-history.md#how-many-versions-are-kept) per i libri della cartella e per le cartelle sottostanti. Tra tutti i limiti sopra indicati vale quello più restrittivo per un libro.
- **Extension Settings**, quando un'[estensione](../extensions/index.md) caricata ha impostazioni che i libri ereditano, vedi [Impostazioni delle estensioni](prompts.md#settings-of-extensions).
- **Phrase watch list**: le frasi per cui vengono controllati i libri della cartella, aggiunte a quelle delle tue impostazioni e delle cartelle superiori, vedi [Controllo delle frasi](annotations.md#watching-for-phrases).

Tutto è facoltativo. Un campo vuoto è *ereditato*, e lo indica: il testo grigio nel campo è il valore che eredita, e la riga sotto dice da dove proviene. Premi **Save** per salvare la cartella; il nome e la descrizione possono essere cambiati in qualsiasi momento, ma un cambio di lingua o di un'impostazione testuale viene rifiutato mentre un agente lavora su un libro nella cartella o sotto di essa: il messaggio indica il libro, da [mettere in pausa o fermare](../agents.md) prima.

<a id="where-the-settings-of-a-book-come-from"></a>

### Da dove vengono le impostazioni di un libro

Un libro usa, per ciascuna di queste impostazioni, la prima tra queste che non sia vuota:

1. il **libro** stesso (le sue schede *About* e *Prompts*),
2. la **cartella** in cui si trova,
3. la cartella sopra quella, e così via fino in cima,
4. le tue **impostazioni utente** (*Settings → Templates*, vedi [Account e impostazioni](../account.md)),
5. il **valore predefinito integrato**.

Un testo vuoto conta come non impostato. Questa ricerca avviene quando serve (quando una parte viene generata, un riassunto viene creato, una parte viene riscritta...), e nulla viene copiato nel libro quando lo metti in una cartella: **spostare un libro in un'altra cartella ne cambia il modo di scrivere**, e cambiare un'impostazione di una cartella cambia ogni libro sotto di essa che non ne imposta una propria.

La scheda [Prompts](prompts.md#inherited-settings) di un libro lo mostra: sotto ogni campo c'è la provenienza del suo valore (*Inherited from the folder "Sci-fi"*, *Inherited from your settings* o *Built-in default*).

<a id="language"></a>

### Lingua

La casella **Language** di un libro (la scheda [About](managing-books.md#the-about-tab)) e quella di una cartella possono essere lasciate vuote, il che significa *eredita*: il libro prende la lingua della cartella più vicina sopra di esso che ne imposta una, altrimenti l'inglese. Una casella vuota mostra quale lingua sia e da dove proviene. La casella ha un pulsante di cancellazione; svuotarla torna a ereditare (prima diventava inglese). Tutto ciò che mostra o usa la lingua di un libro usa quella che ha insieme alle sue cartelle: la sillabazione del testo, l'esportazione e la tabella [Mass Change Language](#mass-changes).

<a id="deleting-a-folder"></a>

## Eliminare una cartella

Il pulsante del cestino di una cartella chiede cosa fare:

- **una cartella vuota** viene eliminata dopo una conferma;
- **una cartella con libri o cartelle al suo interno** offre **Move its content to the folder above** (l'opzione predefinita) oppure **Delete everything in it as well**. La seconda chiede di nuovo, mostrando il numero di libri e cartelle, e manda i libri nel [Cestino](../trash.md), da cui possono essere recuperati. La cartella eliminata in sé non compare nel Cestino.

Se una cartella interna ha il nome di una cartella di quella sopra, lo spostamento del contenuto verso l'alto viene rifiutato (i nomi sono unici): rinomina prima una delle due. Una cartella con un libro su cui lavora un agente non può essere eliminata o spostata, perché le impostazioni di quel libro cambierebbero durante l'esecuzione.
<a id="organise"></a>

## Organizza

**Organise** (nel menu **Manage** dell'intestazione della lista) apre una finestra con due riquadri, ciascuno con una cartella, per spostare e copiare libri e cartelle dall'uno all'altro senza aprirli uno per uno. Si apre con il riquadro di sinistra sulla cartella in cui eri e quello di destra su quella mostrata l'ultima volta.

![La finestra Organise](../../images/organise-dialog.png)

| Key | |
|---|---|
| Left / Right / Tab | L'altro riquadro. |
| Up / Down | Il cursore. |
| Enter | Apre la cartella sotto il cursore. |
| Backspace | Sale di un livello. |
| Space or Insert | Seleziona la voce sotto il cursore; Shift + Up / Down seleziona mentre ci si sposta. |
| Click on a check box | Seleziona o deseleziona la voce; **Shift + clic** seleziona (o deseleziona) ogni voce compresa tra questa e la casella cliccata prima. |

Una fila di pulsanti in basso agisce su ciò che è **selezionato** (o, se non c'è nulla di selezionato, su ciò che è sotto il cursore) e mostra il relativo tasto tra parentesi:

| Button | Key | |
|---|---|---|
| **Copy** | `c` | Copia nella cartella dell'*altro* riquadro. |
| **Move** | `m` | Sposta nella cartella dell'altro riquadro. |
| **New folder** | `n` | Crea una cartella nella cartella del riquadro attivo. |
| **Rename** | `Shift+r` | Rinomina la cartella o il libro sotto il cursore. |
| **Delete** | `Delete` | Lo elimina, come nella lista. |
| **Folder settings** | `i` | Le [impostazioni](#folder-settings) della cartella sotto il cursore. |
| **Mass Change Model/Protocol**, **Mass Change Language**, **Mass Rename**, **Tag books** | `p`, `l`, `Shift+n`, `t` | Le [modifiche di massa](#mass-changes) per i libri selezionati, o per i libri della cartella del riquadro attivo (con la propria *Include subfolders*). I riquadri mostrano i nuovi nomi dopo un rinominamento di massa. |
| **Advanced search** | `f` | La [ricerca](managing-books.md#advanced-search) nella cartella del riquadro attivo, eventualmente con *Include subfolders*. Un risultato ha **Show in folder**, che porta il riquadro lì e seleziona il libro. |
| **Show folder in the list** | `o` | Chiude la finestra e apre la cartella del riquadro attivo nell'elenco dei libri. |

Quando chiudi la finestra, l'elenco dei libri viene riletto, quindi ciò che hai fatto, spostato, copiato, rinominato o eliminato compare subito (se la cartella in cui si trovava l'elenco non esiste più, si torna al livello più alto).

I tasti sono azioni delle [scorciatoie da tastiera](../account.md#keyboard-shortcuts) in *Settings*: modificali o cancellali lì (per impostazione predefinita non ci sono tasti funzione, perché un Mac e i browser li riservano). I pulsanti mostrano il tasto che hai impostato.

<a id="copying"></a>

### Copia

Una **copia** di un libro è un [clone](managing-books.md#cloning-a-book); la finestra chiede se prendere solo il ramo corrente. Una copia di una cartella è una nuova cartella con le impostazioni, la descrizione e i tag dell'originale e le copie di tutti i libri e le cartelle al suo interno. Una copia può richiedere tempo, quindi viene eseguita con una barra di avanzamento che non può essere interrotta. Se fallisce a metà, restano le copie fatte prima, ciascuna completa.

Uno **spostamento** avviene subito e può essere annullato spostando di nuovo. Una cartella non può essere spostata dentro se stessa o in una cartella sottostante.

<a id="when-a-name-is-taken"></a>

### Quando un nome è già in uso

Quando la destinazione ha già una cartella con lo stesso nome, la finestra chiede cosa fare con quella cartella, con **Apply to all** per le successive:

| Choice | |
|---|---|
| **Rename** | La cartella riceve un altro nome. |
| **Skip** | Resta dov'è (e non viene copiata). |
| **Merge** | Il contenuto viene messo nella cartella esistente; le cartelle con lo stesso nome al suo interno si fondono a loro volta. |

I libri non entrano mai in conflitto: i loro nomi possono ripetersi.

<a id="mass-changes"></a>

## Modifiche di massa

Il menu **Manage → Mass changes** dell'elenco (e i pulsanti di Organise) modificano molti libri in una volta. **Mass Change Model/Protocol** assegna un modello e/o un protocollo ai libri, **Mass Change Language** una lingua, **Mass Rename** aggiunge un prefisso o un suffisso ai loro nomi, sostituisce un testo al loro interno o li numera, **Tag books** e **Tag folders** aggiungono o rimuovono un tag. Vedi [Gestione dei libri](managing-books.md#tagging-many-books-at-once). Dall'elenco lavorano sui libri della cartella corrente, con la propria **Include subfolders**.

<a id="favorites"></a>

## Preferiti

La stella su una riga di libro lo rende un **favorito**. Finché ne hai almeno uno, il livello più alto mostra la cartella virtuale **Favorites** (una stella) con i libri preferiti di tutte le tue cartelle. È solo una vista: non puoi creare un libro o una cartella al suo interno, e togliere la stella rimuove il libro da lì, non dalla sua cartella. I preferiti sono salvati nelle tue impostazioni.

<a id="tagging-folders"></a>

## Etichettare le cartelle

Le cartelle hanno tag come i libri. **Manage → Mass changes → Tag folders** applica tag a molte cartelle in una volta. I tag di una cartella non cambiano i tag dei suoi libri e non fanno parte di un [backup](backups.md).

<a id="folders-and-other-features"></a>

## Cartelle e altre funzioni

- **Backups.** Un [backup](backups.md#backups-and-folders) ricorda le cartelle del suo libro, e un file esportato può contenere le impostazioni che il libro eredita.
- **Agents.** Un [agente](../agents.md#agents-and-folders) scrive con le impostazioni delle cartelle del suo libro.
- **Help assistant.** Se lo consenti, l'[assistente](../administration/help-assistant.md#your-books) può cercare i tuoi libri e le tue cartelle.
- **Phone.** Il [layout per telefono](../phone.md#books) mostra le stesse cartelle come schede e permette di crearle, rinominarle, configurarle ed eliminarle; Organise e le modifiche di massa si trovano nel menu **⋮** della sua intestazione.
- **Viewer.** Il [visualizzatore](publishing.md#folders-in-the-list) elenca anche le cartelle e si apre in quella che avevi lasciato.
