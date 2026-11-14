---
label: Usare Marginalia sul telefono
order: 985
icon: device-mobile
---

# Usare Marginalia sul telefono

Marginalia ha un'interfaccia pensata per i telefoni. Dal browser del telefono puoi scrivere, modificare e rigenerare parti del testo, modificare i lorebook, gestire i tuoi modelli e (come amministratore) l'installazione. L'interfaccia per telefono **non è una copia di quella per desktop**: le stesse cose sono disposte in una sola colonna, alcune finestre sono più semplici e non tutte le estensioni funzionano. Questa pagina elenca le differenze.

Per leggere un libro sul telefono c'è anche il [visualizzatore](books/publishing.md), pensato proprio per questo.

<a id="when-the-phone-layout-is-used"></a>

## Quando viene usata l'interfaccia per telefono

Marginalia controlla la larghezza della finestra del browser **quando la pagina viene caricata**. Con una larghezza inferiore a 768 pixel ottieni l'interfaccia per telefono, con una larghezza superiore quella normale dello [spazio di lavoro](index.md#the-workspace).

- I tablet e i telefoni in orizzontale hanno di solito una larghezza superiore a 768 pixel e ricevono l'interfaccia per desktop.
- L'interfaccia **non cambia mentre lavori**. Se ruoti il telefono o il tablet, o ridimensioni la finestra, l'interfaccia resta com'era finché non **ricarichi la pagina**. Quindi, per avere su un tablet o su un telefono ruotato l'interfaccia più grande per desktop, ruotalo e ricarica la pagina; per tornare all'interfaccia per telefono, riportalo in verticale e ricarica di nuovo.
- Anche una finestra del browser da desktop stretta riceve l'interfaccia per telefono, utile per provarla.

Come per il desktop, l'interfaccia per telefono può essere aggiunta alla schermata Home come un'app (*Install app* / *Add to Home Screen* nel menu del browser).

<a id="the-workspace"></a>

## Lo spazio di lavoro

Le schede a sinistra sono sostituite da un **cassetto**. Tocca il pulsante del menu nella barra in alto per aprirlo, poi tocca una scheda per passare a essa. La scheda attiva occupa tutto lo schermo e il suo nome è mostrato nella barra in alto.

![Lo spazio di lavoro del telefono con il cassetto aperto](../images/phone-workspace-drawer.png)

Il cassetto contiene le stesse schede del desktop - *Libri*, *Lorebook*, *Impostazioni*, *Protocolli*, *Provider di inferenza*, *Agenti* (solo per gli utenti che possono usare gli [agenti](agents.md#on-a-phone)), *Risorse* e *Cestino* - e, per un amministratore, *Amministratore* (il pulsante del desktop è qui una scheda). In basso ci sono il selettore **Lingua**, **Cambia password** ed **Esci**. Le estensioni possono aggiungere pulsanti, ma solo nei punti che conoscono sul telefono, vedi [Estensioni](#extensions).

Mentre la scheda *Impostazioni* ha modifiche non salvate, il cassetto è bloccato come sul desktop: salva o scarta prima.

<a id="books"></a>

### Libri

La lista dei libri è su una colonna. Sopra di essa ci sono il filtro per nome, il filtro per tag e i pulsanti di ordinamento (*Ultima modifica*, *Creato*, *Nome*; tocca di nuovo quello attivo per invertire l'ordine), poi **Aggiungi libro**, la lente (la [ricerca avanzata](books/managing-books.md#advanced-search): tag da includere ed escludere, date, numero di parti e testo) e l'aggiornamento. Ogni libro ha dei pulsanti: la stella lo rende un preferito, **Sposta nella cartella** lo mette in una [cartella](books/folders.md), la matita lo apre e il cestino lo elimina (nel [Cestino](trash.md)).

![La lista dei libri su un telefono](../images/phone-book-list.png)

La lista mostra una [cartella](books/folders.md) alla volta, come sul desktop: il percorso della cartella in cui ti trovi, come pulsanti per tornare indietro, la casella **Includi le sottocartelle**, poi le cartelle sopra i libri (tocca una cartella per entrarci; mostra quanti libri contiene) e, al livello più alto, la cartella virtuale **Preferiti**. I filtri per nome e per tag cercano nella cartella corrente, oppure in tutto ciò che sta sotto con *Includi le sottocartelle*. **Aggiungi libro** e le importazioni inseriscono il nuovo libro nella cartella in cui ti trovi. Viene ricordata la cartella in cui ti trovi.

![Cartelle su un telefono](../images/phone-folders.png)

![Il menu della lista dei libri](../images/phone-books-menu.png)

Una cartella si crea con il pulsante **Nuova cartella** dell'intestazione, e si rinomina, configura (descrizione, tag, lingua, modello e protocollo per i suoi libri) o elimina dal pulsante **⋮** della sua riga. Il pulsante **⋮** nell'intestazione apre le finestre che agiscono su più libri: **Organizza** (i due pannelli delle cartelle sono uno sopra l'altro sul telefono, non c'è alcun suggerimento da tastiera), **Tagga i libri**, **Tagga le cartelle** e le modifiche di massa delle [cartelle](books/folders.md) (modello e protocollo, lingua, rinomina). Anche qui la lingua di un libro può essere ereditata da una cartella, e la scheda *Prompt* mostra da dove proviene ciascun valore.

<a id="lorebooks"></a>

### Lorebook

Il telefono ha l'**intero editor dei lorebook** del desktop, disposto su una colonna. Il lorebook si sceglie in una casella; il pulsante **⋮** accanto apre il menu del lorebook: **Aggiungi lorebook**, **Clona**, **Elimina**, **Importa** (un file di lorebook, un file SillyTavern, un archivio zip di file Markdown, un lorebook che un altro utente condivide), **Esporta** (il lorebook o il formato SillyTavern), **Tagga i lorebook** e **Modifica la descrizione**. Sotto la casella *Attivata*, il conteggio dei token e i pulsanti per **Cerca e sostituisci** e l'**anteprima di attivazione**, la sezione **Impostazioni** apre il nome, i sotto-lorebook, i tag, la profondità di scansione e *Condividi con altri utenti* del lorebook. Vedi [Lorebook](lorebooks/index.md).

Più sotto ci sono **Aggiungi voce** (e il **⋮** con la modifica di massa e i tag di più voci), la casella di ricerca e le voci. Una voce si apre con la casella di attivazione e il nome su una riga; aperta, mostra un modulo: nome, ordine, tag, modalità di filtraggio e testo, modalità di inserimento, tag negativi, il contesto di attivazione e il suo intervallo, *Cerca anche nei riassunti*, *Previeni la ricorsione*, *Solo tramite altre voci*, il contenuto, la nota e **Elimina voce**. Tutto ciò che viene modificato è salvato subito, come sul desktop. Sotto la casella di ricerca ci sono le caselle dei tag e dello stato che limitano l'elenco; **Seleziona le voci** nel menu **⋮** mette una casella di spunta davanti a ogni voce e mostra **Elimina i selezionati** per quelle spuntate. Ciò che non c'è qui: la casella *Solo i lorebook radice*.

La scheda **Lorebook** del cassetto della storia mostra il sapere del libro come la scheda del desktop: una casella di ricerca, le voci che si aprono sul loro contenuto e la penna che modifica il testo di una voce.

Lo stesso editor è usato nella scheda *Lorebook* di un libro. Quando il lorebook del libro ha dei sotto-lorebook, la casella **Lorebook mostrato** sotto di essa permette di passare la scheda a uno qualsiasi di essi, e di tornare indietro, senza cambiare il lorebook del libro. Mentre un [agente](agents.md) lavora su un libro che usa il lorebook, il lorebook è mostrato come testo e nulla al suo interno può essere modificato.

![Un lorebook su un telefono](../images/phone-lorebook.png)

![Il menu del lorebook](../images/phone-lorebook-menu.png)

![Le impostazioni del lorebook](../images/phone-lorebook-settings.png)

![Una voce aperta nel suo modulo, con sopra le revisioni del VCS del lorebook](../images/phone-lorebook-entry.png)

Il pulsante accanto alla casella *Attivata* apre **Cerca e sostituisci** per le voci del lorebook, vedi [Voci](lorebooks/entries.md#search-and-replace).

![Cerca e sostituisci in un lorebook su un telefono](../images/phone-lorebook-search-replace.png)

<a id="settings-protocols-providers-resources-trash"></a>

### Impostazioni, protocolli, provider, risorse, cestino

- Le **Impostazioni** sono le stesse del desktop, disposte in una colonna stretta; **Importa il backup come nuovo libro** è il pulsante sopra *Scarta le modifiche*.
- **Protocolli** e **Provider di inferenza** elencano ogni voce come testo con i pulsanti accanto (sotto di essa, per i provider, che ne hanno di più). Le finestre di modifica sono quelle del desktop, quindi un protocollo può essere [condiviso](protocols.md#sharing-a-protocol) anche dal telefono, e il pulsante **Importa da un altro utente** sopra l'elenco importa un protocollo che un altro utente condivide.
- **Risorse** raggruppa i file in base all'oggetto che li usa (apri un gruppo con la sua freccia); ogni file è una scheda (un'immagine, il nome, il tipo, la dimensione, la data e i pulsanti per scaricare, sostituire ed eliminare), con una casella per *Elimina i selezionati*; spuntare un gruppo elimina tutti i suoi file.
- **Cestino** mostra ogni oggetto eliminato come una scheda (nome, tipo, quando è stato eliminato). La spiegazione del cestino si trova dietro il pulsante *i*; i pulsanti dei filtri vanno a capo su una seconda riga. Il ripristino funziona come descritto in [Cestino](trash.md).

![Le risorse su un telefono](../images/phone-resources.png)

![I provider di inferenza su un telefono, con il pulsante info di un provider condiviso](../images/phone-providers.png)

![Il cestino su un telefono](../images/phone-trash.png)
<a id="the-book-window"></a>

## La finestra del libro

Aprendo un libro questo si mostra a **schermo intero**. Il pulsante di chiusura è la **✕** nell'angolo in alto, e le schede - *Informazioni*,
*Prompt*, *Lorebook*, *Storia*, *Vista dei rami*, *Backup* - sono su una riga che si scorre lateralmente. Fanno le stesse cose della versione desktop (vedi [Libri](books/index.md)), con i campi disposti in una sola colonna.

<a id="the-story"></a>

### La storia

La scheda della storia è una sola colonna di parti. Le differenze rispetto all'[editor della storia](books/story-editor.md):

![La storia di un libro su un telefono](../images/phone-book-story.png)

- La **scaletta** (e le schede che le estensioni aggiungono accanto, come le [Note dell'autore](extensions/authors-note.md))
  si trova in un **cassetto**. Aprilo con il pulsante in alto a sinistra.
- Il pulsante **Riassunti** e il menu delle impostazioni (⚙, con *Cambia gli stili del testo*, *Cerca e sostituisci*, *Importa*, *Eliminazione di massa* ed *Esporta la storia*) si trovano nella barra in alto, non
  in basso.
- Il pulsante **+** per una nuova parte è in basso.
- La finestra **Riassunti** ha la sua barra dei menu (*Cerca e sostituisci*, *Crea riassunti...*, *Rigenera tutti i riassunti*, *Esporta i riassunti*, *Importa i riassunti*) su una riga propria;
  su uno schermo stretto le voci sono dietro al pulsante **⋯**. [Importare i riassunti](books/summaries.md#export-and-import-of-the-summaries)
  è la stessa finestra della versione desktop, nella larghezza dello schermo.

![Importa i riassunti su un telefono](../images/phone-summaries-import.png)
- [Annotazioni](books/annotations.md): la scheda **Annotazioni** si trova nel cassetto accanto alla scaletta; un tocco su un brano sottolineato
  apre la sua nota; **Annota la parte** e **Annota con l'IA** sono nel menu della parte, **Annota la selezione** nel menu che appare con una pressione prolungata sul testo selezionato.
- **Cerca e sostituisci** apre la stessa finestra della versione desktop, con i campi e i pulsanti uno sotto l'altro:

  ![Cerca e sostituisci nella storia su un telefono](../images/phone-search-replace.png)

- Una parte è solo il suo testo sotto una piccola riga con due icone. La prima è il **menu** della parte (modifica, rigenera,
  scorri, dirama, cronologia, mostra prompt, immagini, riassunto, elimina); la seconda apre un **popup** con ciò che la versione desktop mostra
  nella colonna a destra di una parte: gli ID, il modello, il protocollo, i token, le parole e i dettagli del turno.
- **Cronologia** nel menu di una parte apre la [cronologia delle versioni](books/version-history.md) come pagina a schermo intero: le versioni sono
  schede sopra la versione selezionata, con le stesse azioni della versione desktop.

  ![La cronologia delle versioni di una parte su un telefono](../images/phone-history.png)

- **Statistiche** si trova nel menu delle impostazioni della storia (le statistiche del libro) e in un pulsante nell'intestazione dell'elenco dei libri
  (tutti i libri). La finestra è una pagina a schermo intero; il suo calendario mostra le ultime 16 settimane (un anno nella versione desktop), vedi
  [Statistiche di scrittura](books/statistics.md).

  ![Le statistiche di tutti i libri su un telefono](../images/phone-statistics.png)

- La panoramica dei [riassunti](books/summaries.md) mantiene una larghezza fissa e si scorre lateralmente.

<a id="administration"></a>

## Amministrazione

Un amministratore ha la scheda **Amministratore** nel cassetto, con le stesse schede della [pagina desktop](administration/index.md) su una riga che si scorre lateralmente:
*Utenti*, *Backup del database*, *Pulizia*, *File di risorse*, *Assistente di aiuto*, *Agenti*, *Aggiorna* ed *Estensioni*. Le liste (utenti, backup, estensioni) sono
schede con i pulsanti sotto il testo, e le barre degli strumenti vanno a capo su una seconda riga. Installare un'estensione (*Carica estensione*) e caricare un backup del database aprono il selettore file del telefono. Gli altri utenti non vedono la scheda.

![Gli utenti della scheda Amministratore su un telefono](../images/phone-admin-users.png)

![I backup del database su un telefono](../images/phone-admin-backups.png)

<a id="what-is-missing"></a>

## Cosa manca

Il telefono fa quello che fa il desktop, con queste differenze:

| Assente o diverso sul telefono | Dove farlo |
|---|---|
| La casella *Solo i lorebook radice* e la selezione di molte voci del lorebook da eliminare in una volta | Layout desktop. |
| La barra dei comandi, le scorciatoie da tastiera, la vista delle differenze di una parte rigenerata e le note a margine | Layout desktop: richiedono una tastiera o uno schermo largo. |
| Le finestre con molte colonne o due pannelli (la panoramica dei [riassunti](books/summaries.md), la conversazione dei Beta Readers, il Manga Generator) sono ridotte | Funzionano: in orizzontale è più comodo. |

Tutto ciò che non è in questo elenco funziona come sul desktop: generare, modificare, rigenerare, scorrere e diramare
parti, riassunti e meta-riassunti, prompt del libro, l'albero dei rami, backup del libro, esportazione, risorse, il cestino, i lorebook e l'amministrazione.

<a id="extensions"></a>

## Estensioni

Il layout del telefono ha classi proprie, quindi un'estensione deve supportarlo esplicitamente. Le estensioni che vengono con
Marginalia:

| Estensione | Sul telefono |
|---|---|
| [Arc Outline](extensions/arc-outline.md) | Funziona. La scheda Archi è nel cassetto della scaletta. |
| [Note dell'autore](extensions/authors-note.md) | Funziona. La nota è nel cassetto della scaletta della storia. |
| [Beta Readers](extensions/beta-readers.md) | Funziona. La scheda Beta Readers è nel cassetto della scaletta; lo spazio per la conversazione è ridotto, in orizzontale è più comodo. |
| [Generatore di descrizione del libro](extensions/book-description-generator.md) | Funziona. Il pulsante è in fondo alla scheda Informazioni; la finestra mostra le opzioni sopra il risultato. |
| [Chapter Marker](extensions/chapter-marker.md) | Funziona. I titoli dei capitoli sono nel cassetto della scaletta. |
| [Character Tracker](extensions/character-tracker.md) | Funziona. La scheda Personaggi è nel cassetto della scaletta. |
| [Note della storia](extensions/story-notes.md) | Funziona. La scheda Note è nel cassetto della scaletta; la finestra della pagina si adatta allo schermo, una pagina lunga è più comoda da scrivere in orizzontale. |
| [Conlang Translator](extensions/conlang-translator.md) | In parte. Il pannello in *Impostazioni → Impostazioni estensione*, l'editor, la revisione e la procedura guidata funzionano (la griglia del lessico scorre lateralmente), e così il tocco su un discorso tradotto nel visualizzatore; il lato storia (**Traduci dialoghi**, **Traduci parti (lingua costruita)**, il clic nella storia) non è stato provato su un telefono, vedi [Limiti](extensions/conlang-translator.md#limits). |
| [Convertitore](extensions/sillytavern-converter.md) | Funziona. La scheda Convertitore è nel cassetto della scaletta; l'intervallo della chat si sceglie con due tocchi, vedi [Su un telefono](extensions/sillytavern-converter.md#on-a-phone). |
| [Esportatore di istruzioni](extensions/instruction-exporter.md) | Funziona. **Esporta istruzioni** è nel menu delle impostazioni della storia. |
| [Manga Generator](extensions/manga-generator.md) | Funziona. **Manga Generator** è nel menu delle impostazioni della storia; la sua finestra è grande, quindi in orizzontale è più comodo. |
| [Reviewer](extensions/reviewer.md) | Funziona. Le sue voci sono nel menu di una parte, le sue impostazioni in *Impostazioni → Impostazioni estensione*. |
| [Side Query](extensions/side-query.md) | Funziona. La scheda Side Query è nel cassetto della scaletta. |
| [Lore Generator](extensions/lore-generator.md) | Funziona. Il pulsante con la bacchetta è nella riga di ogni voce del lorebook; la sua finestra mostra il prompt sopra il risultato. |
| [Lorebook VCS](extensions/lorebook-vcs.md) | Funziona. Il pannello del lorebook si trova sotto la sua riga *Attivata*, le revisioni di una voce sono in cima al suo modulo. |
| [Image Generator](extensions/image-generator.md) | Funziona. **Genera immagine** è nel menu di una parte. |

Le estensioni di altre fonti possono comportarsi in modo diverso o non aggiungere nulla sul telefono; compaiono dove il
loro autore le ha rese funzionanti. Se un'estensione sembra mancare, controllala nel layout desktop. Come un'estensione
supporta il telefono è descritto nel [manuale dello sviluppatore](../developer/plugins/extendable.md#the-phone-twins).

<a id="tips"></a>

## Suggerimenti

- Scrivere un testo lungo sul telefono è più facile in orizzontale. Il layout resta quello del telefono dopo il cambio, finché
  non ricarichi la pagina.
- Se ti serve qualcosa che sul telefono manca, ruota il telefono e ricarica la pagina, oppure apri Marginalia in una
  finestra più larga; i tuoi libri e le tue impostazioni sono gli stessi ovunque.
- Per raggiungere Marginalia da un telefono deve girare come [server](getting-started/docker-server.md): la
  [app desktop](getting-started/desktop-app.md) ascolta solo sul tuo computer.
