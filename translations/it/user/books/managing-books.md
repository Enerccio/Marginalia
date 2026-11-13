---
label: Gestione dei libri
order: 100
---

# Gestione dei libri
<a id="the-book-list"></a>

## La lista dei libri

La scheda **Libri** elenca i tuoi libri. I libri degli altri utenti non vengono mostrati. La lista mostra una [cartella](folders.md) alla volta: le cartelle della cartella corrente compaiono sopra i suoi libri; al livello principale, senza cartelle create, è semplicemente l'elenco di tutti i tuoi libri.

![La scheda Libri](../../images/books.png)

| Colonna | |
|---|---|
| **Nome** | Il nome del libro. |
| **Tag** | I tag del libro. |
| **Creato** | Quando il libro è stato creato. |
| **Ultima modifica** | Quando il libro è stato modificato l'ultima volta. |

L'elenco è ordinato per *Ultima modifica*, dal più recente. Fai clic su un'intestazione di colonna per ordinare in base a essa; con più colonne ordinate, l'elenco è ordinato secondo tutte, in sequenza.

La riga sotto le intestazioni filtra l'elenco:

- **Nome** - libri il cui nome *inizia* con il testo. Usa `*` come carattere jolly: `*dragon` trova ogni libro con "dragon" in qualsiasi punto del nome.
- **Tag** - libri che hanno *tutti* i tag selezionati.

Il pulsante di aggiornamento alla fine della riga dei filtri ricarica l'elenco.

Ogni riga ha dei pulsanti: la stella rende il libro un [preferito](folders.md#favorites), la matita lo apre, **Sposta nella cartella** lo sposta in una [cartella](folders.md), il pulsante di clonazione crea una copia del libro e il pulsante del cestino lo elimina. L'intestazione contiene **Nuova cartella**, il menu **Gestisci** (**Organizza**, la finestra per [spostare e copiare](folders.md#organise) libri e cartelle, **Tagga i libri**, **Statistiche** e il sottomenu **Modifiche di massa**) e **Importa**, che crea un nuovo libro a partire da file in tuo possesso (vedi [Importare un libro](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Un libro è aperto in una sola finestra

Un libro può essere aperto in una sola finestra alla volta, anche tra le schede del browser, altri browser e il telefono. Quando apri un libro che è aperto altrove, Marginalia indica da quando e chiede **Apri qui**: il libro si apre allora qui e la sua finestra nell'altro posto si chiude, con un messaggio che ne spiega il motivo (se una parte è in fase di generazione lì, la domanda lo segnala e **Apri qui** interrompe la generazione; quanto scritto finora resta). **Annulla** lo lascia dov'è. Quando effettui l'accesso e Marginalia apre il libro che avevi aperto per ultimo, non sottrae il libro a un'altra finestra: lo lascia chiuso e ti dice dove si trova. Una finestra in una scheda chiusa o su un dispositivo spento non trattiene il libro: **Apri qui** lo prende subito.

<a id="advanced-search"></a>

### Ricerca avanzata

**Cerca** (nell'intestazione, accanto a **Gestisci**; la lente d'ingrandimento nell'intestazione su telefono) apre una finestra che trova libri con criteri più ampi di quelli della riga dei filtri. Compila ciò che vuoi verificare, lascia vuoti gli altri campi e premi **Cerca** (oppure Invio in un campo di testo):

| Campo | Trova libri... |
| ----- | -------------- |
| **Nome** | il cui nome contiene il testo (`*` è un carattere jolly). |
| **Tag da includere** | che hanno i tag: *Tutti i tag* oppure *Uno qualsiasi dei tag*, come scegli accanto al campo. |
| **Tag da escludere** | che non hanno i tag: con *Uno qualsiasi dei tag* viene escluso un libro che ne ha almeno uno, con *Tutti i tag* solo un libro che li ha tutti. |
| **Creato da / a**, **Modificato da / a** | creati o modificati l'ultima volta nell'intervallo; il giorno di una data *a* è incluso. |
| **Numero di parti da / a** | con quel numero di parti in tutti i suoi rami (le parti nel cestino non sono conteggiate). |
| **Testo** | con una parte che contiene tutte le parole del testo. Parole, `"frasi"` e `*` funzionano come in [la ricerca nella storia](story-editor.md#searching-the-story). |
| **Includi versioni** | disattivato (predefinito): viene cercato solo il testo attuale delle parti; attivato: un libro viene trovato anche se una versione precedente di una delle sue parti contiene le parole, vedi [Cronologia delle versioni](version-history.md#searching-the-old-text). |
| **Cerca in tutti i rami** | disattivato (predefinito): il testo viene cercato solo nel ramo in cui si trova il libro (la storia come la vedi quando apri il libro); attivato: nelle parti di tutti i rami. |

Un libro deve superare ogni controllo che hai compilato. I libri trovati sono elencati sotto con i loro tag, i più recenti per ultima modifica in cima; un clic su un'intestazione di colonna li ordina. Il pulsante cartella apre un libro (nella sua finestra sopra la ricerca, quindi chiudendo il libro torni ai risultati). **Svuota** cancella i campi.

![La ricerca avanzata dei libri](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Clonare un libro

Il pulsante di clonazione chiede il **Nome** del nuovo libro (il nome dell'originale con *(Copia)* per impostazione predefinita) e ha la casella **Solo il ramo attuale**, selezionata per impostazione predefinita. Il nuovo libro è un libro separato che riceve:

![Clonare un libro](../../images/book-clone.png)

- le impostazioni del libro (descrizione, lingua, prompt, punto di vista, tempo verbale, stile, impostazioni di backup), i dati che le estensioni conservano nel libro e i tag;
- lo stesso **Modello**, **Protocollo** e lo stesso lorebook. Il lorebook è *collegato*, non copiato: una modifica al lorebook appare in entrambi i libri;
- la storia con una propria copia delle parti, dei loro riassunti e delle immagini. Con **Solo il ramo attuale** vengono copiate solo le parti dall'inizio alla fine del [ramo attivo](branches-and-story-tree.md); senza di essa vengono copiati tutti i rami.

Il clone inizia una [cronologia delle versioni](version-history.md#backups-copies-and-the-trash) propria (una versione per parte); la cronologia dell'originale non viene copiata.

Il clone non viene pubblicato, anche se l'originale lo è. L'originale non viene modificato. Il clone viene inserito nella stessa [cartella](folders.md) dell'originale.

<a id="tagging-many-books-at-once"></a>

### Taggare molti libri insieme

**Tagga i libri** (nel menu **Gestisci** dell'intestazione) apre una finestra che applica un tag a molti libri, o lo rimuove da essi, senza aprire ciascuno. Scegli il **Tag** (oppure digita un nuovo tag e premi Invio) e l'elenco mostra ogni libro con i suoi tag; i libri che hanno il tag scelto compaiono per primi e lo evidenziano. Ogni riga ha un pulsante **+** che applica il tag a quel libro e un pulsante **-** che lo rimuove. Per fare lo stesso con più libri, spunta le loro caselle (la casella nell'intestazione seleziona tutti) e usa **Applica il tag a quelli selezionati** o **Rimuovi il tag da quelli selezionati** in fondo. Un libro su cui lavora un agente è in grigio: è bloccato, quindi non può essere selezionato o modificato finché l'esecuzione non viene messa in pausa o interrotta. La stessa finestra tagga [i lorebook e le loro voci](../lorebooks/lorebooks.md#tagging-many-at-once).

![Taggare i libri](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Cambiare modello e protocollo di molti libri

Il sottomenu **Modifiche di massa** del menu **Gestisci** contiene la voce **Cambia in massa modello/protocollo**. Apre una finestra che applica un unico **Modello** e/o un unico **Protocollo** a molti libri senza aprire ciascuno. Lascia vuota una casella (mostra *Non sostituire*) e quella parte dei libri resta invariata: scegli solo un modello per cambiare i modelli e conservare i protocolli. La tabella elenca i libri della [cartella](folders.md) corrente per nome, con il modello e il protocollo che hanno ora; **Includi le sottocartelle** aggiunge i libri al suo interno. Scrivi nel filtro sotto **Nome** per restringere l'elenco, spunta i libri da modificare (la casella nell'intestazione seleziona tutti i libri mostrati) e premi **Sostituisci ed esci**. I libri che il filtro nasconde vengono deselezionati, quindi viene modificato solo ciò che vedi. Un libro su cui lavora un agente è in grigio: è bloccato, quindi non può essere selezionato o modificato finché l'esecuzione non viene messa in pausa o interrotta.

![Cambiare modello e protocollo di molti libri](../../images/mass-change-model-protocol.png)

La voce **Cambia la lingua in massa** dello stesso menu funziona allo stesso modo per la **Lingua** dei libri: scegli la lingua, filtra e spunta i libri, premi **Sostituisci ed esci**. La tabella mostra la lingua che ogni libro ha ora, con ciò che eredita dalle sue cartelle (vedi [Lingua](folders.md#language)).

![Cambiare la lingua di molti libri](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Rinominare molti libri

La voce **Rinomina di massa** rinomina insieme i libri selezionati. Scegli come:

| Modo | Cosa fa |
|---|---|
| **Aggiungi un prefisso** | Inserisce il **Testo da aggiungere** prima del nome. |
| **Aggiungi un suffisso** | Inserisce il **Testo da aggiungere** dopo il nome. |
| **Trova e sostituisci** | Sostituisce ogni occorrenza di **Trova** nel nome con **Sostituisci con** (vuoto lo rimuove). Il testo è preso così com'è, non come schema; **Distingui maiuscole e minuscole** è disattivato per impostazione predefinita. |
| **Numera i libri** | Assegna numeri ai libri: lo **Schema del nome** è il nuovo nome, con `{name}` per il nome attuale e `{n}` per il numero, per esempio `{name} {n}`, `{n}. {name}` o `Capitolo {n}`. Il primo libro selezionato riceve **Inizia da**, ogni successivo un **Passo** in più, e **Cifre** completa il numero con zeri (`007`). |

![Numerare i libri di tutte le cartelle](../../images/mass-rename.png)

La colonna **Nuovo nome** mostra quale nome riceverebbe ciascun libro selezionato mentre scrivi, quindi controllalo prima di premere **Rinomina**. Una numerazione conta i libri selezionati nell'ordine dell'elenco (per nome). Gli spazi all'inizio e alla fine del nuovo nome vengono rimossi. Nulla viene rinominato, e la finestra resta aperta, quando manca un testo, quando lo schema non contiene `{n}`, quando nessun nome cambierebbe o quando un nome risulterebbe vuoto. Filtro, **Includi le sottocartelle** e libri bloccati funzionano come nelle altre modifiche di massa.

**Tagga i libri** e la voce **Tagga le cartelle** agiscono sui libri, o sulle cartelle, della cartella corrente allo stesso modo.
<a id="creating-a-book"></a>

## Creazione di un libro

1. Fai clic su **Aggiungi libro**.
2. Inserisci il nome e conferma.

Il nuovo libro viene inserito nella [cartella](folders.md) mostrata dall'elenco, e così i libri che importi. Si apre subito. Parte con il tuo **Modello predefinito** e il tuo **Protocollo predefinito** dalla scheda *Impostazioni*
(vedi [Account e impostazioni](../account.md)), se li hai impostati; altrimenti selezionali nella scheda *Informazioni* prima di generare.

<a id="importing-a-book"></a>

### Importare un libro

**Importa** nell'intestazione dell'elenco (il pulsante con il file su un telefono) apre un'unica finestra per tutto ciò da cui può nascere un nuovo libro.
Scegli l'**Origine**; il testo, i file accettati e le opzioni sotto il nome seguono la scelta:

| Origine | Caricamento | Opzioni |
|---|---|---|
| **Importa da un archivio** | uno `.zip` di file `.md`, una parte per ogni file, vedi [Importare nel libro](story-editor.md#importing-into-the-book) | nessuna |
| **Importa da SillyTavern** | una o più chat `.jsonl`, vedi [sotto](#importing-from-sillytavern) | unisci i messaggi dell'utente e dell'assistente |
| **Importa da LM Studio** | una o più conversazioni, vedi [sotto](#importing-from-lm-studio) | unisci i messaggi dell'utente e dell'assistente |
| **Importa da un documento** | un manoscritto in un solo file (Word, LibreOffice, RTF, Markdown, testo semplice, EPUB), vedi [sotto](#importing-a-finished-manuscript) | parole per parte, livello di titolo dei capitoli |
| **Importa da un vault di Obsidian** | lo `.zip` di un vault, vedi [sotto](#from-an-obsidian-vault-or-a-scrivener-project) | lo stesso |
| **Importa da un progetto Scrivener** | lo `.zip` di un progetto `.scriv`, vedi [sotto](#from-an-obsidian-vault-or-a-scrivener-project) | lo stesso |

Carica il file, dai un nome al libro (viene compilato dal file, dal titolo di un documento o dal personaggio di una chat; il libro si chiama *New book* se lasci il campo vuoto) e fai clic su **Importa**. Una finestra di avanzamento con **Interrompi** mostra le parti via via salvate;
interrompere mantiene ciò che è già stato salvato. Un file che l'origine non sa leggere viene rifiutato con un avviso, gli altri caricamenti restano.

La stessa finestra, dal menu delle impostazioni (⚙) di un libro, aggiunge le parti a quel libro nella posizione che scegli, vedi
[Importare nel libro](story-editor.md#importing-into-the-book).

![Le origini della finestra di importazione](../../images/import-source.png)

![Importazione di uno zip di file di testo come nuovo libro](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Importare un manoscritto completo

Per una bozza già esistente, scegli **Importa da un documento** e carica il file: un `.docx` (Word), `.odt` (LibreOffice,
OpenOffice), `.rtf` (Rich Text, da WordPad, TextEdit e qualsiasi altro programma), `.md` (un file Markdown), `.txt` (testo semplice) o `.epub` (un e-book). Il testo diventa le parti di un nuovo libro, tutto contrassegnato come scritto da te (l'importazione non viene conteggiata nelle [statistiche](statistics.md) come scrittura, e la cronologia di una parte riporta *Import from a document*).

- **Capitoli.** Un paragrafo con lo stile *Titolo* (un titolo `#` in un file Markdown) inizia un capitolo, e ogni capitolo inizia con il suo titolo (il primo
  titolo di una parte è il titolo del suo capitolo, come nell'[esportazione](export.md)). **I capitoli iniziano al livello di titolo** indica quali
  titoli sono quelli: il livello che scegli e quelli sopra di esso (al primo utilizzo viene scelto il livello più alto presente nel documento); i titoli
  più profondi restano nel testo come titoli più piccoli. Un documento senza titoli è un solo capitolo. Il *Titolo* del documento non
  viene importato come testo, dà il nome al libro (in un file Markdown il `title:` dell'intestazione in cima, oppure un unico titolo `#` prima dei capitoli `##`). Un file di testo semplice non ha titoli, quindi una riga a sé stante che dice `Chapter 12`, `Part IV: The Return`,
  `Book two` o `Prologue` (anche `Epilogue`, `Interlude`, `Preface`, `Foreword`, `Afterword`) inizia un capitolo; la scelta del livello di titolo non serve in quel caso.
- **Parole per parte.** Il testo viene tagliato tra i paragrafi in parti di circa questo numero di parole (1000 al primo utilizzo), al confine di paragrafo più vicino al numero. Una parte non contiene mai la fine di un capitolo e l'inizio del successivo, e un capitolo non viene tagliato senza motivo: ciò che resterebbe alla fine di un capitolo, quando è meno della metà del numero, rimane con la parte precedente,
  così un capitolo fino a una volta e mezza il numero forma una sola parte. Un paragrafo più lungo del numero non viene tagliato.
- Una riga sotto il nome indica quanti capitoli, parole e parti produrrà l'importazione; cambia quando cambi le opzioni.
- **Paragrafi di un file di testo.** Le righe vuote separano i paragrafi e le righe di un paragrafo vengono unite (il testo a capo fisso su una colonna diventa un solo paragrafo); un file senza righe vuote ha un paragrafo per ogni riga. Un file Markdown viene mantenuto come è scritto, tagliato alle righe vuote, ai titoli e ai blocchi di codice; viene letto anche il testo in Windows-1252 invece che in UTF-8.
- **Formattazione.** Corsivo e grassetto vengono mantenuti (come Markdown `*corsivo*`, `**grassetto**`); il resto della formattazione (font, dimensioni, allineamento), le immagini, le intestazioni e i piè di pagina, le note a piè di pagina, i commenti e il testo eliminato delle modifiche tracciate non vengono importati. Le tabelle vengono lette cella per cella.

![Importazione di un documento Word, diviso in parti di circa 500 parole](../../images/document-import.png)

Un vecchio file `.doc` va prima salvato come `.docx`, `.odt` o `.rtf`.

**Un EPUB** viene letto pagina per pagina nel suo ordine di lettura. I titoli delle pagine (da `h1` a `h6`) sono i capitoli; una pagina senza titolo che l'indice dell'e-book nomina riceve quel nome come titolo. La copertina, l'indice, le note a piè di pagina e le letture del testo ruby vengono esclusi, e il titolo dell'e-book dà il nome al libro. Un libro protetto da DRM non può essere letto.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Da un vault di Obsidian o da un progetto Scrivener

Entrambi sono cartelle, quindi comprimi prima la cartella in uno zip (su un Mac **Comprimi** nel Finder; anche il `.scriv` di Scrivener è una cartella) e carica lo `.zip`. Le opzioni sono le stesse
di un documento.

- **Vault di Obsidian.** Ogni nota (`.md`) è un capitolo che prende il nome della nota (o il `title:` della sua intestazione), nell'ordine dei percorsi con i numeri
  considerati come numeri (`Chapter 2` prima di `Chapter 10`). Una cartella è una parte sopra le sue note, e i titoli dentro una nota stanno sotto quelli della nota stessa. I link `[[Nota|testo]]` diventano il loro testo; gli embed (`[[file]]` con un `!` davanti), i commenti `%%...%%`, gli id di blocco e il marcatore di un callout vengono rimossi; un evidenziato `==testo==` è testo semplice. Le cartelle nascoste (`.obsidian`, `.trash`), gli allegati e le note senza testo vengono saltati. Se il vault contiene note che non fanno parte della storia, eliminale prima di comprimere.
- **Progetto Scrivener.** Viene importato il manoscritto (la cartella *Draft* del binder, Scrivener 2 e 3), non la ricerca, il cestino o i documenti che non sono *included in compile*. Una cartella, o un documento che ha dei documenti sotto di sé, è un titolo (la sua profondità nel binder è il suo livello, il suo titolo il testo); i documenti che sono tutto ciò che contiene una cartella sono le sue scene, una dopo l'altra con un'interruzione di scena
  tra di esse (i loro titoli non vengono usati). Un documento direttamente nel manoscritto, o in una cartella che contiene altre cartelle, è un capitolo con il suo titolo. Il nome del progetto dà il nome al libro.

Qualunque sia l'origine, il nuovo libro viene aperto al termine dell'importazione (anche se l'hai interrotta), e la finestra [**Importazione terminata**](story-editor.md#after-an-import) propone di scrivere i riassunti delle parti e di far compilare le note del lore a un agente.

<a id="importing-from-sillytavern"></a>

### Importare da SillyTavern

Per proseguire un gioco di ruolo o una storia iniziati in [SillyTavern](https://github.com/SillyTavern/SillyTavern), usa
**Importa → Importa da SillyTavern**. In SillyTavern esporta la chat
(*Manage chat files*, oppure prendi i file `.jsonl` da `data/<user>/chats/<character>/`), poi:

1. Carica **una o più** chat `.jsonl`. Un file che non è una chat di SillyTavern viene rifiutato, gli altri restano.
2. Dai un nome al libro (viene compilato dal personaggio della prima chat).
3. Spunta **Unisci i messaggi dell'utente e dell'assistente in un'unica parte** se vuoi una parte per ogni turno: il messaggio dell'utente
   (più messaggi di seguito vengono uniti con una riga vuota) diventa le *istruzioni* della parte e la risposta che segue il suo testo. Senza la spunta ogni messaggio è una parte a sé, con il suo testo come testo della parte, così il libro si legge come si leggeva la chat.
4. Fai clic su **Importa**. Una finestra di avanzamento con **Interrompi** mostra le parti via via salvate; interrompere mantiene ciò che è già stato salvato.

![Importazione di una chat di SillyTavern](../../images/sillytavern-import.png)

SillyTavern copia la chat quando crei un *branch* o un *checkpoint*, quindi le chat di una storia condividono l'inizio. Caricale insieme e Marginalia trova i messaggi comuni e li salva una sola volta: le chat diventano i rami di un unico albero, che puoi percorrere nella [vista ad albero](story-editor.md). Le *swipe* (risposte alternative) di un messaggio diventano parti sorelle, e il libro si apre alla fine della chat scritta per ultima (in base alla data del suo ultimo messaggio). Le chat che non iniziano con lo stesso messaggio diventano prime parti separate. I messaggi nascosti (di sistema) vengono saltati, così come le swipe vuote. Il ragionamento e il nome del modello di una risposta vengono mantenuti con la parte.

<a id="importing-from-lm-studio"></a>

### Importare da LM Studio

Per proseguire una conversazione avuta in [LM Studio](https://lmstudio.ai), usa **Importa → Importa da LM Studio**.
LM Studio conserva ogni conversazione in un file `<numero>.conversation.json` nella cartella `.lmstudio/conversations` della tua cartella utente (su Windows
`%USERPROFILE%\.lmstudio\conversations`). Funziona come [Importare da SillyTavern](#importing-from-sillytavern):

1. Carica **una o più** conversazioni. Un file che non è una conversazione di LM Studio viene rifiutato, gli altri restano.
2. Dai un nome al libro (viene compilato con il nome della conversazione, o con il nome del file se ne è privo).
3. Spunta **Unisci i messaggi dell'utente e dell'assistente in un'unica parte** per una parte per turno (il tuo messaggio diventa le *istruzioni* della parte), oppure lasciala deselezionata così che ogni messaggio sia una parte a sé.
4. Fai clic su **Importa**.

![La finestra di importazione da LM Studio con una conversazione caricata](../../images/lmstudio-import.png)

Le versioni di un messaggio che LM Studio conserva (le risposte rigenerate, il testo modificato) diventano parti sorelle, e il libro si apre sulla versione che era visualizzata; le conversazioni che condividono l'inizio vengono unite in un unico albero. Il ragionamento di un modello con ragionamento viene mantenuto come ragionamento della parte, insieme al nome del modello. Il prompt di sistema della conversazione, le chiamate degli strumenti e i file allegati non vengono importati.

Per avviare un libro da un file di backup, usa **Importa il backup come nuovo libro** nella scheda *Impostazioni*, vedi
[Backup dei libri](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## La scheda Informazioni

![La scheda Informazioni di un libro](../../images/book-about.png)

| Campo | |
|---|---|
| **Nome** | Il nome del libro. |
| **Tag** | Tag per ordinare i tuoi libri e per l'attivazione del [lorebook](../lorebooks/entries.md). Scrivi per scegliere un tag esistente o crearne uno nuovo. |
| **Descrizione** | Di cosa parla il libro. Disponibile per i template dei prompt come descrizione del libro. |
| **Lingua** | La lingua in cui è scritto il libro. Serve per la sillabazione automatica del testo nello stile di lettura del libro e nel [visualizzatore](publishing.md); non cambia ciò che scrive il modello. Vuoto significa *eredita*: la lingua della [cartella](folders.md#language) più vicina sopra il libro che ne imposta una, altrimenti inglese; la casella mostra quale e da dove viene. |
| **Modello** | Il [fornitore di inferenza](../inference-providers.md) che scrive questo libro. |
| **Protocolli** | Il [protocollo](../protocols.md) con le impostazioni di generazione. |
| **Versioni conservate per parte** | Quante versioni precedenti del testo di una parte conserva la [cronologia](version-history.md). Vuoto significa *eredita*: il numero grigio nella casella è il limite che si applica e la riga sotto indica quale cartella, le tue impostazioni o l'amministratore lo impostano. **0** non conserva alcuna cronologia. |
| **Pubblicato** | Permette ad altri utenti di leggere il libro, vedi [Pubblicazione e visualizzatore](publishing.md). Una volta selezionata, sotto la casella compare il link di lettura del libro (`<indirizzo di Marginalia>/view/<ID libro>`); copialo e invialo ai lettori. |
| **Mostra le immagini ai lettori** | Selezionata per impostazione predefinita. I lettori di un libro pubblicato vedono le immagini delle sue parti; deseleziona per mostrare loro solo il testo. Vedi sempre le tue immagini. |

Le modifiche vengono salvate mentre scrivi.

Di seguito sono riportati i conteggi di sola lettura:

| | |
|---|---|
| **Numero totale di parole**, **Numero totale di token** | Tutte le parti del libro, in tutti i rami. |
| **Parole del ramo attivo**, **Token del ramo attivo** | Solo le parti del [ramo attivo](branches-and-story-tree.md). |
| **Paternità del ramo** | Premi **Calcola**: quante percentuali delle parole del ramo attivo hai scritto tu e quante ha generato un modello, e il numero di parole. Viene calcolato dalla [cronologia delle versioni](version-history.md) di ogni parte, quindi richiede un po' di tempo su un libro lungo; una parte la cui cronologia è disattivata viene confrontata con il testo generato per primo dal modello. Vedi [Evidenziazione del testo generato](story-editor.md#highlighting-generated-text). |

Per generare, un libro ha bisogno di un modello e di un protocollo. Senza di essi, la generazione si interrompe con *Al libro manca il modello.* o
*Al libro manca il protocollo.*

<a id="deleting-a-book"></a>

## Eliminare un libro

Clicca il pulsante del cestino nell'elenco dei libri e conferma. Il libro scompare dal tuo elenco.

Un libro non viene eliminato mentre qualcosa lavora su di esso: una parte in generazione, un lotto di riassunti, una traduzione, una finestra di riscrittura o di annotazione, oppure il libro aperto in un'altra finestra. Marginalia indica allora cosa lo impedisce (*Questo è in uso: ...*); attendi che termini o interrompilo, poi elimina di nuovo. Vale lo stesso per il ripristino di un [backup](backups.md) nel libro mentre un'altra finestra o un'esecuzione vi lavora.

I libri eliminati non vengono rimossi subito dal database. Finché un amministratore non li rimuove, insieme a tutte le loro parti, con [Pulizia](../administration/cleanup.md), puoi riportare il libro dalla scheda [Cestino](../trash.md). Fino ad allora i dati sono ancora presenti nei [backup del database](../administration/database-backups.md). I [backup](backups.md) del libro stesso sono file e restano dove si trovano.
