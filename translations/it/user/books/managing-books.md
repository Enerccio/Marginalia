---
label: Gestione dei libri
order: 100
---

# Gestione dei libri
<a id="the-book-list"></a>

## La lista dei libri

La scheda **Libri** elenca i tuoi libri. I libri degli altri utenti non vengono mostrati. La lista mostra una [cartella](folders.md) alla volta: le cartelle della cartella corrente sono sopra i suoi libri; al livello più alto, senza cartelle create, sono semplicemente tutti i tuoi libri.

![La scheda Libri](../../images/books.png)

| Colonna | |
|---|---|
| **Nome** | Il nome del libro. |
| **Tag** | I tag del libro. |
| **Creato** | Quando il libro è stato creato. |
| **Ultima modifica** | Quando il libro è stato modificato l'ultima volta. |

La lista è ordinata per *Ultima modifica*, dal più recente. Fai clic su un'intestazione di colonna per ordinare per quella colonna; con più colonne ordinate, la lista è ordinata secondo tutte, nell'ordine indicato.

La riga sotto le intestazioni filtra la lista:

- **Nome** - libri il cui nome *inizia* con il testo. Usa `*` come carattere jolly: `*dragon` trova ogni libro con "dragon" in qualsiasi punto del nome.
- **Tag** - libri che hanno *tutti* i tag selezionati.

Il pulsante di aggiornamento alla fine della riga dei filtri ricarica la lista.

Ogni riga ha dei pulsanti: la stella rende il libro un [preferito](folders.md#favorites), la matita lo apre, **Sposta nella cartella** lo mette in una [cartella](folders.md), il pulsante di clonazione crea una copia e il cestino lo elimina. L'intestazione contiene **Nuova cartella**, il menu **Gestisci** (**Organizza**, la finestra per [spostare e copiare](folders.md#organise) libri e cartelle, **Tagga i libri**, **Statistiche** e il sottomenu **Modifiche di massa**) e **Importa**, che crea un nuovo libro dai file che hai (vedi [Importare un libro](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Un libro è aperto in una sola finestra

Un libro può essere aperto in una sola finestra alla volta, anche tra le schede del browser, altri browser e il telefono. Quando apri un libro che è già aperto altrove, Marginalia indica da quando e chiede **Apri qui**: il libro si apre allora qui e la sua finestra nell'altro punto si chiude, con un messaggio che spiega il motivo (se una parte è in generazione là, la domanda lo dice, e **Apri qui** interrompe la generazione; ciò che è stato scritto finora resta). **Annulla** lascia il libro dov'è. Quando accedi e Marginalia apre il libro che avevi aperto per ultimo, non sottrae il libro a un'altra finestra: lo lascia chiuso e ti indica dove si trova. Una finestra in una scheda chiusa o su un dispositivo spento non trattiene il libro: **Apri qui** lo prende subito.

<a id="advanced-search"></a>

### Ricerca avanzata

**Cerca** (nell'intestazione, accanto a **Gestisci**; la lente nell'intestazione su un telefono) apre una finestra che trova libri con criteri più ampi di quelli della riga dei filtri. Compila ciò che vuoi verificare, lascia vuoto il resto e premi **Cerca** (o Invio in un campo di testo):

| Campo | Trova libri... |
| ----- | -------------- |
| **Nome** | il cui nome contiene il testo (`*` è un carattere jolly). |
| **Tag da includere** | che hanno i tag: *Tutti i tag* o *Uno qualsiasi dei tag*, come scegli accanto al campo. |
| **Tag da escludere** | che non hanno i tag: con *Uno qualsiasi dei tag* viene escluso un libro che ne ha almeno uno, con *Tutti i tag* solo un libro che li ha tutti. |
| **Creato da / a**, **Modificato da / a** | creati o modificati l'ultima volta nell'intervallo; il giorno di una data *a* è incluso. |
| **Numero di parti da / a** | con quel numero di parti in tutti i loro rami (le parti nel cestino non sono conteggiate). |
| **Testo** | con una parte che contiene tutte le parole del testo. Parole, `"phrases"` e `*` funzionano come in [la ricerca nella storia](story-editor.md#searching-the-story). |
| **Includi versioni** | disattivato (predefinito): viene cercato solo il testo attuale delle parti; attivato: un libro viene trovato anche se una versione precedente di una delle sue parti contiene le parole, vedi [Cronologia delle versioni](version-history.md#searching-the-old-text). |
| **Cerca in tutti i rami** | disattivato (predefinito): il testo viene cercato solo nel ramo in cui si trova il libro (la storia come la vedi quando apri il libro); attivato: nelle parti di tutti i rami. |

Un libro deve superare ogni controllo che hai compilato. I libri trovati sono elencati sotto con i loro tag, i modificati più di recente per primi; un clic su un'intestazione di colonna li ordina. Il pulsante cartella apre un libro (nella sua finestra sopra la ricerca, così chiudendo il libro torni ai risultati). **Svuota** cancella i campi.

![La ricerca avanzata dei libri](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Clonare un libro

Il pulsante di clonazione chiede il **Nome** del nuovo libro (il nome dell'originale con *(Copia)* per impostazione predefinita) e ha la casella **Solo il ramo attuale**, selezionata per impostazione predefinita. Il nuovo libro è un libro separato che riceve:

![Clonazione di un libro](../../images/book-clone.png)

- le impostazioni del libro (descrizione, lingua, prompt, punto di vista, tempo verbale, stile, impostazioni di backup), i dati che le estensioni conservano nel libro e i tag;
- lo stesso **Modello**, **Protocollo** e lo stesso lorebook. Il lorebook è *collegato*, non copiato: una modifica di esso compare in entrambi i libri;
- la storia con una propria copia delle parti, dei loro riassunti e delle immagini. Con **Solo il ramo attuale** vengono copiate solo le parti dall'inizio alla fine del [ramo attivo](branches-and-story-tree.md); senza, vengono copiati tutti i rami.

La clonazione avvia una [cronologia delle versioni](version-history.md#backups-copies-and-the-trash) propria (una versione per parte); la cronologia dell'originale non viene copiata.

Il clone non viene pubblicato, anche se l'originale lo è. L'originale non viene modificato. Viene messo nella stessa [cartella](folders.md) dell'originale.

<a id="tagging-many-books-at-once"></a>

### Taggare molti libri in una volta

**Tagga i libri** (nel menu **Gestisci** dell'intestazione) apre una finestra che aggiunge un tag a molti libri, o lo toglie, senza aprirli uno per uno. Scegli il **Tag** (o scrivine uno nuovo e premi Invio): la lista mostra ogni libro con i suoi tag; i libri che hanno il tag scelto compaiono per primi e lo mostrano evidenziato. Ogni riga ha un pulsante **+** che applica il tag a quel libro e un pulsante **-** che lo rimuove. Per fare lo stesso con più libri, spunta le loro caselle (la casella nell'intestazione seleziona tutti) e usa **Applica il tag a quelli selezionati** o **Rimuovi il tag da quelli selezionati** in fondo. Un libro su cui lavora un agente è in grigio: è bloccato, quindi non può essere selezionato o modificato finché l'esecuzione non viene messa in pausa o arrestata. La stessa finestra permette di taggare [lorebook e le loro voci](../lorebooks/lorebooks.md#tagging-many-at-once).

![Taggare i libri](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Cambiare modello e protocollo di molti libri

Il sottomenu **Modifiche di massa** del menu **Gestisci** contiene la voce **Cambia in massa modello/protocollo**. Apre una finestra che assegna un **Modello** e/o un **Protocollo** a molti libri senza aprirli uno per uno. Lascia vuota una casella (mostra *Non sostituire*) e quella parte dei libri resta com'è: scegli solo un modello per cambiare i modelli e mantenere i protocolli. La tabella elenca i libri della [cartella](folders.md) corrente per nome, con il modello e il protocollo che hanno ora; **Includi le sottocartelle** aggiunge i libri al di sotto di essa. Scrivi nel filtro sotto **Nome** per restringere la lista, spunta i libri da modificare (la casella nell'intestazione seleziona tutti i libri visualizzati) e premi **Sostituisci ed esci**. I libri nascosti dal filtro vengono deselezionati, quindi viene modificato solo ciò che vedi. Un libro su cui lavora un agente è in grigio: è bloccato, quindi non può essere selezionato o modificato finché l'esecuzione non viene messa in pausa o arrestata.

![Cambiare modello e protocollo di molti libri](../../images/mass-change-model-protocol.png)

La voce **Cambia la lingua in massa** dello stesso menu funziona allo stesso modo per la **Lingua** dei libri: scegli la lingua, filtra e spunta i libri, premi **Sostituisci ed esci**. La tabella mostra la lingua che ogni libro ha ora, con quella che eredita dalle sue cartelle (vedi [Lingua](folders.md#language)).

![Cambiare la lingua di molti libri](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Rinominare molti libri

La voce **Rinomina di massa** rinomina i libri selezionati in una volta. Scegli come:

| Modo | Cosa fa |
|---|---|
| **Aggiungi un prefisso** | Mette il **Testo da aggiungere** prima del nome. |
| **Aggiungi un suffisso** | Mette il **Testo da aggiungere** dopo il nome. |
| **Trova e sostituisci** | Sostituisce ogni occorrenza di **Trova** nel nome con **Sostituisci con** (vuoto lo rimuove). Il testo è preso alla lettera, non come schema; **Distingui maiuscole e minuscole** è disattivato per impostazione predefinita. |
| **Numera i libri** | Dà ai libri dei numeri: lo **Schema del nome** è il nuovo nome, con `{name}` per il nome attuale e `{n}` per il numero, per esempio `{name} {n}`, `{n}. {name}` o `Chapter {n}`. Il primo libro selezionato riceve **Inizia da**, ogni successivo lo **Passo** in più, e **Cifre** riempie il numero di zeri (`007`). |

![Numerare i libri di tutte le cartelle](../../images/mass-rename.png)

La colonna **Nuovo nome** mostra quale nome riceverebbe ogni libro selezionato mentre scrivi, quindi controllala prima di premere **Rinomina**. La numerazione conta i libri selezionati nell'ordine della lista (per nome). Gli spazi all'inizio e alla fine del nuovo nome vengono rimossi. Nulla viene rinominato, e la finestra resta aperta, quando manca un testo, quando lo schema non contiene `{n}`, quando nessun nome cambierebbe o quando un nome finirebbe vuoto. Il filtro, **Includi le sottocartelle** e i libri bloccati funzionano come negli altri cambi di massa.

**Tagga i libri** e la voce **Tagga le cartelle** agiscono sui libri, o sulle cartelle, della cartella corrente allo stesso modo.
<a id="creating-a-book"></a>

## Creare un libro

1. Fai clic su **Aggiungi libro**.
2. Inserisci il nome e conferma.

Il nuovo libro viene messo nella [cartella](folders.md) mostrata dall'elenco, e così i libri che importi. Si apre subito. Parte con il tuo **Modello predefinito** e il tuo **Protocollo predefinito** dalla scheda *Impostazioni* (vedi [Account e impostazioni](../account.md)), se li hai impostati; altrimenti selezionali nella scheda *Informazioni* prima di generare.

<a id="importing-a-book"></a>

### Importare un libro

**Importa** nell'intestazione dell'elenco (il pulsante con il file sul telefono) apre un'unica finestra per tutto ciò da cui può essere composto un nuovo libro.
Scegli l'**Origine**: il testo, i file accettati e le opzioni sotto il nome dipendono da essa:

| Origine | Caricamento | Opzioni |
|---|---|---|
| **Importa da un archivio** | un `.zip` di file `.md`, una parte per ogni file, vedi [Importazione nel libro](story-editor.md#importing-into-the-book) | nessuna |
| **Importa da SillyTavern** | una o più chat `.jsonl`, vedi [sotto](#importing-from-sillytavern) | Unisci i messaggi dell'utente e dell'assistente in un'unica parte |
| **Importa da LM Studio** | una o più conversazioni, vedi [sotto](#importing-from-lm-studio) | Unisci i messaggi dell'utente e dell'assistente in un'unica parte |
| **Importa da un documento** | un manoscritto in un unico file (Word, LibreOffice, RTF, Markdown, testo semplice, EPUB), vedi [sotto](#importing-a-finished-manuscript) | words per part, livello di titolo dei capitoli |
| **Importa da un vault di Obsidian** | la `.zip` di un vault, vedi [sotto](#from-an-obsidian-vault-or-a-scrivener-project) | lo stesso |
| **Importa da un progetto Scrivener** | la `.zip` di un progetto `.scriv`, vedi [sotto](#from-an-obsidian-vault-or-a-scrivener-project) | lo stesso |

Carica il file, assegna un nome al libro (viene riempito dal file, dal titolo di un documento o dal personaggio di una chat; il libro si chiama *New book* se lasci il campo vuoto) e fai clic su **Importa**. Una finestra di avanzamento con **Interrompi** mostra le parti che vengono salvate; interrompendo si conserva ciò che è già stato salvato. Un file che l'origine non riesce a leggere viene rifiutato con un avviso; gli altri file caricati restano.

La stessa finestra, dal menu delle impostazioni (⚙) di un libro, aggiunge le parti a quel libro nel punto che scegli, vedi [Importazione nel libro](story-editor.md#importing-into-the-book).

![Le origini della finestra di importazione](../../images/import-source.png)

![Importazione di uno zip di file di testo come nuovo libro](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Importare un manoscritto completo

Per una bozza già esistente, scegli **Importa da un documento** e carica il file: un `.docx` (Word), `.odt` (LibreOffice, OpenOffice), `.rtf` (Rich Text, da WordPad, TextEdit o qualsiasi altro programma), `.md` (un unico file Markdown), `.txt` (testo semplice) oppure `.epub` (un e-book). Il testo diventa le parti di un nuovo libro, tutto contrassegnato come scritto da te (l'importazione non viene conteggiata come scrittura nelle [statistiche](statistics.md) e la cronologia di una parte riporta *Import from a document*).

- **Capitoli.** Un paragrafo con lo stile *Titolo* (un titolo `#` in un file Markdown) inizia un capitolo, e ogni capitolo inizia con il proprio titolo (il primo titolo di una parte è il suo titolo di capitolo, come nell'[esportazione](export.md)). **I capitoli iniziano al livello di titolo** indica quali titoli sono: il livello che scegli e quelli sopra di esso (inizialmente viene scelto il livello più alto presente nel documento); i titoli di livello inferiore restano nel testo come titoli più piccoli. Un documento senza titoli è un solo capitolo. Il *Titolo* del documento non viene importato come testo: dà il nome al libro (in un file Markdown è il `title:` di un'intestazione in cima, oppure un singolo titolo `#` prima di `##` capitoli). Un file di testo semplice non ha titoli, quindi una riga a sé che dice `Chapter 12`, `Part IV: The Return`, `Book two` o `Prologue` (anche `Epilogue`, `Interlude`, `Preface`, `Foreword`, `Afterword`) inizia un capitolo; in questo caso la scelta del livello di titolo non serve.
- **Words per part.** Il testo viene diviso tra un paragrafo e l'altro in parti di circa questo numero di parole (inizialmente 1000), al confine tra paragrafi più vicino al numero. Una parte non contiene mai la fine di un capitolo e l'inizio del successivo, e un capitolo non viene diviso inutilmente: ciò che resterebbe alla fine di un capitolo, quando è meno della metà del numero, resta nella parte precedente, così un capitolo fino a una volta e mezza il numero è una sola parte. Un paragrafo più lungo del numero non viene diviso.
- Una riga sotto il nome indica quanti capitoli, parole e parti produrrà l'importazione; cambia quando cambi le opzioni.
- **Paragrafi di un file di testo.** Le righe vuote separano i paragrafi e le righe di un paragrafo vengono unite (un testo andato a capo a una certa colonna diventa un unico paragrafo); un file senza righe vuote ha un paragrafo per ogni riga. Un file Markdown viene mantenuto così com'è scritto, diviso alle righe vuote, ai titoli e ai blocchi di codice; viene letto anche il testo in Windows-1252 invece che in UTF-8.
- **Formattazione.** Corsivo e grassetto vengono conservati (come Markdown `*italic*`, `**bold**`); il resto della formattazione (caratteri, dimensioni, allineamento), le immagini, le intestazioni e i piè di pagina, le note a piè di pagina, i commenti e il testo eliminato delle modifiche tracciate non vengono importati. Le tabelle vengono lette cella per cella.

![Importazione di un documento Word, diviso in parti di circa 500 parole](../../images/document-import.png)

Un vecchio file `.doc` va prima salvato come `.docx`, `.odt` o `.rtf`.

**Un EPUB** viene letto pagina per pagina nel suo ordine di lettura. I titoli delle pagine (da `h1` a `h6`) sono i capitoli; una pagina senza titolo che l'indice dell'e-book nomina riceve quel nome come titolo. La copertina, l'indice, le note a piè di pagina e le indicazioni di lettura del testo ruby vengono esclusi, e il titolo dell'e-book dà il nome al libro. Un libro protetto da DRM non può essere letto.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Da un vault di Obsidian o da un progetto Scrivener

Entrambi sono cartelle, quindi comprimi prima la cartella (su Mac **Compress** nel Finder; anche il `.scriv` di Scrivener è una cartella) e carica il `.zip`. Le opzioni sono le stesse di un documento.

- **Vault di Obsidian.** Ogni nota (`.md`) è un capitolo che prende il nome dalla nota (o dal `title:` della sua intestazione), nell'ordine dei percorsi con i numeri contati come numeri (`Chapter 2` prima di `Chapter 10`). Una cartella è una parte sopra le proprie note, e i titoli dentro una nota stanno sotto quello della nota stessa. I link `[[Note|text]]` diventano il loro testo; gli embed (`[[file]]` preceduto da `!`), i commenti `%%...%%`, gli id dei blocchi e il marcatore di un callout vengono rimossi; un'evidenziazione `==text==` diventa testo semplice. Le cartelle nascoste (`.obsidian`, `.trash`), gli allegati e le note senza testo vengono saltati. Se il vault contiene note che non fanno parte della storia, eliminale prima di comprimere.
- **Progetto Scrivener.** Viene importato il manoscritto (la cartella *Draft* del binder, Scrivener 2 e 3), non la ricerca, il cestino o i documenti che non sono *included in compile*. Una cartella, o un documento che ha documenti sotto di sé, è un titolo (la sua profondità nel binder è il suo livello, il suo titolo è il testo); i documenti che una cartella contiene come unico contenuto sono le sue scene, una dopo l'altra con un'interruzione di scena tra loro (i loro titoli non vengono usati). Un documento direttamente nel manoscritto o in una cartella di altre cartelle è un capitolo con il proprio titolo. Il nome del progetto dà il nome al libro.

Qualunque sia l'origine, il nuovo libro viene aperto a importazione terminata (anche se l'hai interrotta), e la finestra [**Importazione terminata**](story-editor.md#after-an-import) propone di scrivere i riassunti delle parti e di lasciare che un agente crei le note di lore.

<a id="importing-from-sillytavern"></a>

### Importare da SillyTavern

Per continuare un roleplay o una storia iniziata in [SillyTavern](https://github.com/SillyTavern/SillyTavern), usa **Importa → Importa da SillyTavern**. In SillyTavern, esporta la chat (*Manage chat files*, oppure prendi i file `.jsonl` da `data/<user>/chats/<character>/`), poi:

1. Carica **una o più** chat `.jsonl`. Un file che non è una chat di SillyTavern viene rifiutato, gli altri restano.
2. Assegna un nome al libro (viene riempito con il personaggio della prima chat).
3. Spunta **Unisci i messaggi dell'utente e dell'assistente in un'unica parte** se vuoi una parte per ogni turno: il messaggio dell'utente (più messaggi di seguito vengono uniti con una riga vuota) diventa le *instructions* della parte e la risposta che segue ne diventa il testo. Senza questa opzione ogni messaggio è una parte a sé, con il proprio testo come testo della parte, così il libro si legge come si leggeva la chat.
4. Fai clic su **Importa**. Una finestra di avanzamento con **Interrompi** mostra le parti che vengono salvate; interrompendo si conserva ciò che è già stato salvato.

![Importazione di una chat di SillyTavern](../../images/sillytavern-import.png)

SillyTavern copia la chat quando crei un *branch* o un *checkpoint*, quindi le chat di una stessa storia condividono l'inizio. Caricale insieme e Marginalia trova i messaggi comuni e li salva una sola volta: le chat diventano i rami di un unico albero, che puoi percorrere nella [vista ad albero](story-editor.md). Gli *swipes* (risposte alternative) di un messaggio diventano parti sorelle, e il libro si apre alla fine della chat scritta per ultima (in base alla data del suo ultimo messaggio). Le chat che non iniziano con lo stesso messaggio diventano prime parti separate. I messaggi nascosti (di sistema) vengono saltati, così come gli swipe vuoti. Il ragionamento e il nome del modello di una risposta vengono conservati con la parte.

<a id="importing-from-lm-studio"></a>

### Importare da LM Studio

Per continuare una conversazione avuta in [LM Studio](https://lmstudio.ai), usa **Importa → Importa da LM Studio**. LM Studio conserva ogni conversazione in un file `<number>.conversation.json` nella cartella `.lmstudio/conversations` della tua cartella personale (su Windows `%USERPROFILE%\.lmstudio\conversations`). Funziona come [Importare da SillyTavern](#importing-from-sillytavern):

1. Carica **una o più** conversazioni. Un file che non è una conversazione di LM Studio viene rifiutato, gli altri restano.
2. Assegna un nome al libro (viene riempito con il nome della conversazione o, se non ne ha uno, con il nome del file).
3. Spunta **Unisci i messaggi dell'utente e dell'assistente in un'unica parte** per una parte per turno (il tuo messaggio diventa le *instructions* della parte), oppure lasciala deselezionata così che ogni messaggio sia una parte a sé.
4. Fai clic su **Importa**.

![La finestra di importazione da LM Studio con una conversazione caricata](../../images/lmstudio-import.png)

Le versioni di un messaggio che LM Studio conserva (le risposte rigenerate, il testo modificato) diventano parti sorelle, e la versione mostrata è quella su cui si apre il libro; le conversazioni che condividono l'inizio vengono unite in un unico albero. Il ragionamento di un modello di ragionamento viene conservato come ragionamento della parte, insieme al nome del modello. Il prompt di sistema della conversazione, le chiamate agli strumenti e i file allegati non vengono importati.

Per avviare un libro da un file di backup, usa **Importa il backup come nuovo libro** nella scheda *Impostazioni*, vedi [Backup dei libri](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## La scheda Informazioni

![The About tab of a book](../../images/book-about.png)

| Campo | |
|---|---|
| **Nome** | Il nome del libro. |
| **Tag** | Tag per ordinare i libri e per l'attivazione del [lorebook](../lorebooks/entries.md). Digita per scegliere un tag esistente o crearne uno nuovo. |
| **Descrizione** | Di cosa parla il libro. Disponibile per i template dei prompt come descrizione del libro. |
| **Lingua** | La lingua in cui è scritto il libro. Serve per la sillabazione automatica del testo nello stile di lettura del libro e nel [visualizzatore](publishing.md); non cambia ciò che scrive il modello. Vuoto significa *eredita*: la lingua della [cartella](folders.md#language) più vicina sopra il libro che ne imposta una, altrimenti inglese; il campo mostra quale e da dove proviene. |
| **Modello** | Il [fornitore di inferenza](../inference-providers.md) che scrive questo libro. |
| **Protocolli** | Il [protocollo](../protocols.md) con le impostazioni di generazione. |
| **Versioni conservate per parte** | Quante versioni precedenti del testo di una parte conserva la [cronologia](version-history.md). Vuoto significa *eredita*: il numero grigio nel campo è il limite che si applica e la riga sotto indica quale cartella, le tue impostazioni o l'amministratore lo impostano. **0** non conserva alcuna cronologia. |
| **Pubblicato** | Permette ad altri utenti di leggere il libro, vedi [Pubblicazione e visualizzatore](publishing.md). Una volta selezionato, il link di lettura del libro (`<address of Marginalia>/view/<book ID>`) appare sotto la casella; copialo e invialo ai lettori. |
| **Mostra le immagini ai lettori** | Selezionato per impostazione predefinita. I lettori di un libro pubblicato vedono le immagini delle sue parti; deselezionalo per mostrare loro solo il testo. Tu vedi sempre le tue immagini. |

Le modifiche vengono salvate mentre digiti.

Sotto ci sono i conteggi di sola lettura:

| | |
|---|---|
| **Numero totale di parole**, **Numero totale di token** | Tutte le parti del libro, in tutti i rami. |
| **Parole del ramo attivo**, **Token del ramo attivo** | Solo le parti del [ramo attivo](branches-and-story-tree.md). |
| **Paternità del ramo** | Premi **Calcola**: quante percentuali delle parole del ramo attivo hai scritto tu e quante ha generato un modello, e il numero di parole. Viene calcolato dalla [cronologia delle versioni](version-history.md) di ogni parte, quindi richiede un po' di tempo su un libro lungo, e una parte la cui cronologia è disattivata viene confrontata con il testo generato per primo dal modello. Vedi [Evidenziare il testo generato](story-editor.md#highlighting-generated-text). |

Un libro ha bisogno di un modello e di un protocollo per generare. Senza di essi, la generazione si interrompe con *Al libro manca il modello.* oppure *Al libro manca il protocollo.*

<a id="deleting-a-book"></a>

## Eliminare un libro

Clicca il pulsante del cestino nell'elenco dei libri e conferma. Il libro scompare dal tuo elenco.

Un libro non viene eliminato finché qualcosa lavora su di esso: una parte in fase di generazione, un batch di riepiloghi, una traduzione, una finestra di riscrittura o di annotazione, oppure il libro aperto in un'altra finestra. Marginalia indica allora cosa lo impedisce (*Questo è in uso: ...*); attendi che termini o interrompilo, e riprova l'eliminazione. Lo stesso vale per il ripristino di un [backup](backups.md) nel libro mentre un'altra finestra o un'esecuzione vi lavora.

I libri eliminati non vengono rimossi subito dal database. Finché un amministratore non li rimuove, insieme a tutte le loro parti, tramite [Pulizia](../administration/cleanup.md), puoi recuperare il libro dalla scheda [Cestino](../trash.md). Fino ad allora i dati sono ancora nei [backup del database](../administration/database-backups.md). I [backup](backups.md) propri del libro sono file e restano dove si trovano.
