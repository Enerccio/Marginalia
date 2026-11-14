---
label: Account e impostazioni
order: 920
icon: person
---

# Account e impostazioni

Tutto ciò che crei in Marginalia appartiene al tuo account: libri, lorebook, tag, fornitori di inferenza, protocolli
e impostazioni. Gli altri utenti non li vedono (eccetto i libri che [pubblichi](books/publishing.md)), e tu non vedi i loro.

<a id="your-account"></a>

## Il tuo account

I pulsanti in fondo all'area di lavoro:

| Pulsante | |
|---|---|
| **Lingua** | La lingua dell'interfaccia utente, vedi [Lingua](#language). |
| pulsante sole / luna | Accanto alla lingua: passa dal tema scuro a quello chiaro, vedi [Tema](#theme). |
| **Cambia password** | Cambia la password, il nome utente e il nome completo, vedi sotto. |
| **Esci** | Ti disconnette su questo dispositivo. |
| **Amministratore** | Solo per gli amministratori, vedi [Amministrazione](administration/index.md). |

<a id="changing-the-password"></a>

### Cambiare la password

**Cambia password** apre una finestra di dialogo con **Nome utente**, **Nome completo**, **Password attuale**, **Password** e
**Ripeti la password**:

- inserisci la **password attuale** per ogni modifica (lasciala vuota se il tuo account non ha una password);
- per cambiare la password, inserisci la nuova in entrambi i campi della password;
- lascia vuoti entrambi i campi della password per cambiare solo il nome e mantenere la password;
- spunta **Log out everywhere else (other browsers and all saved logins)** per disconnettere il tuo account su tutti gli altri dispositivi
  senza cambiare la password, vedi sotto;
- il nome utente non deve essere già usato da un altro account.

Clicca su **OK** per salvare. Con una password attuale errata non viene salvato nulla.

Cambiare la password termina tutti gli accessi salvati (*Ricorda l'accesso*), su ogni dispositivo incluso questo: devono
accedere di nuovo con la nuova password. Vengono chiuse anche le altre sessioni aperte in questo momento (un browser su un altro dispositivo che mostra
ancora l'area di lavoro); resta attiva solo quella in cui stai lavorando. Se un dispositivo è stato smarrito o la tua password è trapelata,
cambiare la password disconnette quel dispositivo.

**Log out everywhere else** fa lo stesso senza una nuova password: ogni accesso salvato del tuo account viene dimenticato e
ogni altra sessione aperta viene chiusa. Usalo quando sei rimasto connesso da qualche parte, per esempio su un computer che
non è tuo. Richiede la tua password attuale come ogni altra modifica in questa finestra. La sessione in cui ti trovi resta attiva, ma
anche il suo accesso salvato viene dimenticato, quindi anche questo browser chiederà la password la prossima volta.

<a id="language"></a>

### Lingua

Il selettore **Lingua** sopra i pulsanti cambia l'interfaccia utente in un'altra lingua. La scelta viene salvata nel
tuo account: la pagina si ricarica con la nuova lingua, e lo stesso vale per ogni altra pagina che hai aperto (anche su un altro dispositivo),
e viene riutilizzata alla prossima connessione. La schermata di accesso è sempre nella lingua predefinita.

L'elenco contiene solo le lingue installate. Inglese, giapponese, francese, spagnolo, tedesco, coreano e italiano sono inclusi in Marginalia; un amministratore
può aggiungerne altre caricando un'[estensione](extensions/index.md), per esempio la
[localizzazione cinese](extensions/chinese-localization.md). I testi che una lingua non traduce vengono mostrati in inglese. Le estensioni incluse portano i propri testi in giapponese, cinese, francese, spagnolo, tedesco, coreano e italiano (quelle cinesi compaiono quando è caricata la localizzazione cinese).
Le date sono scritte nel modo tipico di quella lingua. I tuoi libri e i loro testi non vengono tradotti, solo l'interfaccia.

![Il selettore della lingua](../images/language-selector.png)

![L'area di lavoro in giapponese](../images/ui-japanese.png)

<a id="theme"></a>

### Tema

Il pulsante accanto al selettore **Lingua** passa l'interfaccia utente dal tema scuro (quello predefinito) a quello chiaro e viceversa. La scelta viene salvata nel tuo account e riutilizzata alla prossima connessione, su qualsiasi dispositivo; una pagina già aperta in un'altra scheda cambia quando viene ricaricata. Grafici e mappa delle relazioni assumono i colori del tema quando vengono aperti.

![L'area di lavoro con il tema chiaro](../images/theme-light.png)

<a id="logging-in"></a>

### Accesso

**Ricorda l'accesso** nella schermata di accesso ti mantiene connesso su questo dispositivo per 30 giorni (su un server richiede HTTPS, vedi
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Esci** lo termina.

Un account può avere una password vuota; in tal caso accedi con il solo nome utente. Se un amministratore ha azzerato la tua password dimenticata, accedi in questo modo e imposta subito una nuova password con **Cambia password**. Questo è comodo per l'app desktop sul tuo computer, ma non farlo mai su un server raggiungibile da altri.

<a id="settings"></a>

## Impostazioni

La scheda **Impostazioni** contiene i tuoi valori predefiniti per tutti i tuoi libri.

![La scheda Impostazioni](../images/settings-defaults.png)

In alto ci sono tre pulsanti:

- **Importa il backup come nuovo libro**: crea un libro da un file di backup, vedi
  [Backup dei libri](books/backups.md#importing-a-backup-as-a-new-book);
- **Scarta le modifiche**: elimina le modifiche non salvate e ricarica le impostazioni salvate;
- **Salva**: salva le impostazioni.

!!!info Modifiche non salvate
Le impostazioni non vengono salvate automaticamente. Non appena modifichi un campo (anche quelli delle estensioni), compare *Unsaved changes*
in alto e le altre schede, **Amministratore** ed **Esci** vengono bloccate. Clicca su **Salva** o **Scarta le modifiche** per sbloccarle.
!!!

<a id="general-settings"></a>

### Impostazioni generali

| Campo | |
|---|---|
| **Modello predefinito** | Il [fornitore di inferenza](inference-providers.md) con cui partono i nuovi libri. |
| **Protocollo predefinito** | Il [protocollo](protocols.md) con cui partono i nuovi libri. |
| **Modello predefinito per annotare**, **Protocollo predefinito per annotare** | Il modello che [annota una parte](books/annotations.md#letting-a-model-annotate) quando non ne scegli un altro per l'esecuzione. Se vuoto, è il modello del libro. |
| **Strategia di backup** | [Backup automatici dei libri](books/backups.md#automatic-backups) per i libri senza una strategia propria: *Nessun backup*, *Dopo N messaggi* o *Dopo N minuti*, con il numero accanto. |
| **Versioni conservate per parte** | Quante versioni precedenti del testo di una parte la [cronologia](books/version-history.md#how-many-versions-are-kept) conserva per tutti i tuoi libri, a meno che una cartella, un libro o l'amministratore non ne limitino di più. Se vuoto, non viene impostato alcun limite qui; **0** non conserva alcuna cronologia. |
| **Parti caricate insieme nella storia** | Quante parti di un libro la [storia](books/story-editor.md) e il [lettore](books/publishing.md) tengono caricate. La storia si apre con le ultime parti, il lettore dal punto in cui avevi smesso di leggere (o dalle prime parti); le parti precedenti e successive vengono caricate man mano che scorri e quelle lontane da te vengono di nuovo scartate. Se vuoto, carica tutte le parti insieme. |
| **Obiettivo giornaliero: parole scritte**, **Obiettivo giornaliero: minuti di modifica** | L'obiettivo di una giornata per il calendario delle [statistiche di scrittura](books/statistics.md#the-daily-goal): le parole che scrivi di tuo pugno e i minuti che dedichi alla modifica, su tutti i tuoi libri. Se vuoto, non c'è alcun obiettivo. |
| **Registra le statistiche di scrittura**, **Keep statistics for (months)** | Se le tue [statistiche di scrittura](books/statistics.md#privacy-clearing-not-recording-keeping-less) vengono conteggiate, e per quanti mesi vengono conservate (se vuoto o 0: tutti). Un amministratore può disattivare la registrazione per tutti e impostare un periodo più breve. |

*Modello predefinito* e *Protocollo predefinito* vengono usati solo quando un libro viene creato; cambiarli non modifica i libri esistenti.
L'estensione [Lore Generator](extensions/lore-generator.md) non ha un libro e genera con questi valori. La strategia di backup si applica a tutti i libri senza una *Sostituzione per manoscritto*, a partire dalla parte generata successiva.
<a id="templates"></a>

### Modelli

I default predefiniti per i [prompt dei libri](books/prompts.md):

| Campo | |
|---|---|
| **Lingua dei prompt** | La lingua dei prompt integrati: inglese (il predefinito), giapponese, francese, spagnolo, tedesco, italiano, coreano o cinese, quando è caricata la [localizzazione cinese](extensions/chinese-localization.md). Vedi [Lingua dei prompt](#prompt-language) più sotto. |
| **Template principale** | La cornice del prompt, vedi [Template principale](books/prompts.md#master-template). |
| **Punto di vista (POV)**, **Tempo verbale** | La voce narrante e il tempo verbale. |
| **Stile** | Linee guida per lo stile e il tono. |
| **Prompt utente predefinito** | La richiesta per ogni parte, vedi [Prompt utente](books/prompts.md#user-prompt). |
| **Prompt di riassunto predefinito** | La richiesta per un [riassunto](books/summaries.md#the-summary-prompt). |
| **Prompt di meta-riassunto predefinito** | La richiesta per un [meta-riassunto](books/summaries.md#the-meta-summary-prompt), che unisce i riassunti. |
| **Prompt di sistema per riscrivere**, **Prompt di riscrittura** | Il messaggio di sistema e l'ultimo messaggio di una [riscrittura](books/story-editor.md#redrafting-a-part) di una parte, vedi [Prompt di riscrittura](books/prompts.md#redraft-prompt). |
| **Prompt di sistema per annotare**, **Prompt di annotazione** | Il messaggio di sistema e l'ultimo messaggio della richiesta a un modello di [annotare](books/annotations.md#letting-a-model-annotate) una parte, vedi [Prompt di annotazione](books/prompts.md#annotate-prompt). |
| **Istruzioni di annotazione** | Ciò che il modello cerca quando annota, per i libri e le cartelle che non ne impostano alcuna, vedi [Prompt di annotazione](books/prompts.md#annotate-prompt). |
| **Prompt di impersonificazione** | La richiesta a un modello di [redigere le istruzioni](books/story-editor.md#drafting-the-instructions) del turno successivo al posto tuo, vedi [Prompt di impersonificazione](books/prompts.md#impersonate-prompt). |
| **Prompt di compattazione degli agenti** | Solo per gli utenti che possono usare gli [agenti](agents.md). Ciò che viene chiesto ai tuoi agenti quando li [`/compact`](agents.md#running-a-command-yourself), se il loro prompt di compattazione è vuoto; vuoto qui significa quello impostato dal tuo amministratore, altrimenti quello integrato. |
| **Istruzioni degli agenti (agents.md)** | Solo per gli utenti che possono usare gli [agenti](agents.md). Le tue istruzioni per gli agenti che non ne hanno di propri, aggiunte al loro prompt in ogni esecuzione. Vuoto significa nessuna. |
| **Messaggio a un URL quando un agente ha bisogno di te**, **Formato del messaggio**, **Anche quando l'applicazione è aperta**, **Invia un messaggio di prova** | Solo per gli utenti con il permesso [*Notifiche*](administration/permissions.md) (e non solo per chi usa gli [agenti](agents.md)), nella scheda *Generale*, e solo se il tuo amministratore lo consente. Dove inviare un breve messaggio quando un agente chiede qualcosa, aspetta te, termina o fallisce e tu hai l'applicazione chiusa: vedi [Quando non ci sei](agents.md#when-you-are-not-there). |
| **Prompt di sistema degli agenti** | Solo per gli utenti che possono usare gli [agenti](agents.md). Ciò che ai tuoi agenti viene detto di essere quando il loro prompt di sistema è vuoto; vuoto qui significa quello impostato dal tuo amministratore, altrimenti quello integrato. Testo semplice, non un template. |

![La scheda Modelli](../images/settings-templates.png)

Più sotto, per un utente che può usare gli agenti, ci sono i prompt degli agenti:

![I prompt degli agenti nella scheda Modelli](../images/settings-templates-agents.png)

Ogni campo mostra in grigio il valore predefinito integrato finché è vuoto, e ha il popover dei suggerimenti con
**Inserisci il template predefinito** e le [variabili e macro](templates/index.md) disponibili.

<a id="prompt-language"></a>

#### Lingua dei prompt

**Lingua dei prompt** sceglie la lingua dei prompt integrati: ciò che rappresenta un campo vuoto (il testo grigio cambia
non appena scegli un'altra lingua), e quindi ciò che viene inviato al modello per la generazione, i riassunti, la riscrittura, le annotazioni
e l'impersonificazione. I prompt dei plugin che ne hanno di propri (Arc Outline, Beta Readers, Book Description Generator,
Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) e quelli degli agenti la seguono anch'essi.
È indipendente dalla lingua dell'interfaccia: puoi usare l'applicazione in inglese e inviare prompt in giapponese.
Un modello segue le istruzioni meglio nella lingua della storia, quindi scegli quella in cui sono scritti i tuoi libri. Cambiano solo
i prompt integrati: un prompt che hai scritto tu, qui o in un libro o in una cartella, resta com'è. Un [agente](agents.md)
legge i propri prompt e le proprie notifiche nella propria lingua (*Lingua* nelle impostazioni dell'agente), non in questa.

Questi valori predefiniti si applicano a **ogni libro il cui campo proprio è vuoto**, subito, non solo ai nuovi libri. Un libro che
imposta il proprio valore nella scheda *Prompt* lo mantiene. Quindi:

- imposta qui ciò che hanno in comune tutti i tuoi libri;
- imposta nella scheda *Prompt* di un libro ciò che è specifico per quel libro;
- svuota il campo di un libro per riportarlo al predefinito delle *Impostazioni*.

<a id="styles"></a>

### Stili

Gli stili di testo dei tuoi libri: come appare il testo delle parti nella storia e nel lettore. La scheda elenca gli stili **Normale**
e **Libro**, che l'applicazione fornisce e che non si possono modificare (sono contrassegnati come *integrato*), e i tuoi. Seleziona uno stile per modificarlo a destra; i pulsanti sotto l'elenco:

- **Duplica** - crea uno stile tuo a partire da quello selezionato (anche da uno integrato: così si comincia), chiamato
  *... (copia)*, che poi puoi rinominare e modificare;
- **Elimina** - elimina uno stile tuo; uno stile integrato non si può eliminare;
- **Imposta come predefinito** - fa sì che lo stile sia quello usato dai libri, a meno che non [ne scelgano un altro](books/story-editor.md#text-styles) nel menu delle impostazioni della storia. Lo stile predefinito è contrassegnato come *predefinito* nell'elenco.
- **Esporta** e **Importa** - salvano lo stile selezionato in un file `.style.json`, e aggiungono gli stili di un tale file ai tuoi (non memorizzati
  finché non salvi le impostazioni). Vedi [Condividere uno stile](books/styling-guide.md#share-a-style-export-and-import).

La [guida allo stile](books/styling-guide.md) passa in rassegna ogni opzione e ha uno stile di esempio da scaricare.

![La scheda Stili, con lo stile horror della guida allo stile selezionato](../images/settings-styles.png)

Per uno stile tuo, il modulo a destra ha un **Nome**, la casella **Per le esportazioni (non offerto per i libri)** e due schede. Uno stile è
o per i libri o per le esportazioni: uno stile per le esportazioni è offerto nella [finestra di esportazione](books/export.md#print-styles) (l'applicazione ne fornisce
quattro, gli aspetti delle esportazioni HTML, EPUB, PDF e Word) e non può essere lo stile predefinito dei libri, e uno stile per i libri non è
offerto nella finestra di esportazione. L'elenco li contrassegna con *(export)*.

- **Opzioni** - in gruppi: *Testo* (carattere, dimensione, altezza di riga, spaziatura di lettere e parole, colore, allineamento, sillabazione, lo spazio
  tra i paragrafi, il **rientro della prima riga** dei paragrafi e una **lettera maiuscola decorativa** dopo i titoli), *Pagina* (la larghezza della colonna
  del testo, centrata, e il **colore di sfondo**, che riempie tutta l'area dietro al testo), *Enfasi, link e codice* (i colori del corsivo, del grassetto e
  dei link, il carattere del codice), *Citazioni*, *Titoli* (carattere, dimensione del primo livello, colore, allineamento, maiuscoletto) e il simbolo e il
  colore della *Separazione delle scene* (`---`). Una casella accetta qualsiasi valore CSS (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`); una casella
  vuota mantiene l'aspetto dell'applicazione. Le caselle dei caratteri offrono insiemi di font comuni, e accettano i tuoi.
- **CSS personalizzato** - CSS tuo, aggiunto dopo le opzioni e applicato all'interno dello stile, così una regola che scrivi vale solo per il
  testo con questo stile: `.chat-message-markdown p { text-indent: 1.5em !important; }` rientra i paragrafi, le dichiarazioni semplici
  (`font-size: 1.2rem;`) si applicano a tutto il testo. Sotto, **CSS generato** mostra il foglio di stile che creano le opzioni (sola lettura).
  Le parentesi graffe `{ }` devono corrispondere, altrimenti lo stile non viene salvato.

![La scheda CSS personalizzato di uno stile](../images/settings-styles-css.png)

Sotto il modulo, l'**Anteprima** mostra un testo di esempio nello stile e segue ogni modifica mentre scrivi.

Come le altre schede, la scheda Stili viene salvata con **Salva** delle impostazioni, e **Scarta le modifiche** getta via anche le sue modifiche: duplicare, modificare, eliminare e impostare il predefinito cambiano solo la copia di lavoro, e *Modifiche non salvate* (e il blocco) compare non appena fai una di queste cose. Uno stile senza nome, o con CSS personalizzato le cui parentesi graffe non corrispondono, viene rifiutato al salvataggio.
<a id="exports"></a>

### Esportazioni

Con quali impostazioni viene esportato ciascun formato di file: la scheda ha una sezione per ogni formato che ha qualcosa da impostare (**Testo semplice**, **Markdown**, **HTML**, **Word**, **PDF**, **EPUB**, **Markdown con front matter**, **Obsidian vault**; clicca sul nome per aprirla).

- **Stile di stampa** (HTML, Word, PDF ed EPUB, i formati con caratteri e colori) - lo stile che la [finestra di esportazione](books/export.md) preseleziona per il formato. Parte dall'aspetto che l'applicazione assegna al formato (**Pagina HTML**, **Libro PDF**...); l'elenco contiene gli [stili per le esportazioni](#styles), quelli integrati e i tuoi. Scegline un altro per esportare sempre quel formato con esso, finché non cambi la scelta nella finestra per una singola esportazione.
- **Front matter** (Markdown con front matter e Obsidian vault) - il blocco YAML che apre il file, con `{{title}}`, `{{author}}`, `{{language}}` e `{{exportDate}}`. Vuoto significa il blocco predefinito.
- **Pagina del titolo** (tutti i formati tranne quelli per altri strumenti; l'Obsidian vault la ha nella nota del libro) - il modello della pagina del titolo, un template Handlebars che produce Markdown, con `{{title}}` e `{{author}}` (l'icona info del campo li elenca). Vuoto significa quello predefinito: il titolo come intestazione e l'autore in corsivo sotto di esso.
  **Riempi predefinito** nel popup info ti fa partire dal predefinito.
- **Dichiarazione di paternità** (tutti i formati tranne la chat di SillyTavern) - il testo che la casella **Includi la dichiarazione di paternità** dell'esportazione aggiunge alla fine: `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`, `{{generatedPercent}}` e `{{models}}` (i modelli separati da virgole, vuoto se non ce ne sono). Vuoto significa il testo predefinito.

Queste sono le tue versioni: un libro o una cartella possono averne di propri, vedi [Modello della pagina del titolo](books/export.md#title-page-template). La scheda viene salvata con **Salva** delle impostazioni e **Scarta le modifiche** ne elimina le modifiche, come per le altre. Un modello non valido viene rifiutato al salvataggio. Uno stile creato nella scheda Stili può essere scelto qui dopo che l'hai salvato.

![La scheda Esportazioni, con la sezione HTML aperta](../images/settings-exports.png)

<a id="tags"></a>

### Tag

Una tabella di tutti i tuoi tag (quelli che assegni a libri, lorebook e voci dei lorebook) con il numero di oggetti a cui ciascun tag è collegato. Clicca un'intestazione di colonna per ordinare in base ad essa. A differenza delle altre schede, agisce subito: non fa parte delle modifiche non salvate e non c'è nulla da salvare.

![La scheda Tag](../images/settings-tags.png)

| Pulsante | |
|---|---|
| **Rinomina** (matita) | Chiede il nuovo nome del tag; ogni oggetto mantiene il tag con il nuovo nome. |
| **Elimina** (cestino) | Chiede conferma, poi elimina il tag e lo rimuove da tutti gli oggetti che lo hanno. |

Se il nuovo nome è quello di un altro tag (i nomi sono confrontati ignorando maiuscole e minuscole), rinominando i due tag diventerebbero uguali, quindi ti viene chiesto se **unirli**. Unire sposta tutto ciò che è marcato con il tag rinominato sul tag esistente ed elimina quello rinominato; un oggetto che ha entrambi finisce con il tag esistente una sola volta. Annulla e non cambia nulla: il tag mantiene il suo vecchio nome.

![Domanda se unire due tag](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Scorciatoie da tastiera

I tasti delle [scorciatoie dell'editor della storia](books/story-editor.md#keyboard-shortcuts) (funzionano nell'editor della storia su computer; il [layout per telefono](phone.md) non le ha). La tabella elenca ogni azione, anche quelle che portano le [estensioni](extensions/index.md), con il suo tasto, e i tasti dei pulsanti della finestra [Organizza](books/folders.md#organise) dell'elenco dei libri. Ogni voce del menu impostazioni della storia, del menu di una parte e del menu del clic destro sul suo testo è qui un'azione (la maggior parte non ha ancora un tasto finché non ne assegni uno), e i menu mostrano il tasto accanto alla voce.

| Pulsante | |
|---|---|
| **Registra** | Premi la nuova combinazione di tasti (prima i modificatori o insieme, ad esempio `Ctrl+Shift+K`); un clic in un altro punto annulla. |
| **Ripristina il predefinito** | Restituisce all'azione il suo tasto predefinito. |
| **Svuota** | L'azione non ha un tasto. |

Una nota rossa segnala un tasto condiviso da due azioni, un tasto che il browser riserva a sé (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` e simili, la pagina non li riceve mai) e `Alt` con una lettera (su alcune tastiere, ad esempio macOS, digita un carattere). Un tasto senza `Ctrl`, `Alt` o `Meta` funziona solo quando nessun campo di testo ha il focus. I tasti vengono salvati con **Salva**, come le altre impostazioni, e si applicano subito alle storie aperte.

![Impostazioni, Scorciatoie da tastiera](../images/settings-shortcuts.png)

<a id="notifications"></a>

### Notifiche

Una notifica desktop del browser quando un modello ha finito, così puoi lasciare la scheda mentre lavora. La scheda ha una casella **Usa le notifiche del desktop**, i pulsanti qui sotto e un elenco di ciò che notifica:

- **Generazione della storia**, **Annota con l'IA**, **Annota con l'IA (automatico)**, **Riscrittura**, **Riscrittura (automatico)**, **Abbozza le istruzioni**, **Riassunto**, **Rigenera tutti i riassunti** e **Assistente di aiuto**, e ciò che aggiungono le [estensioni](extensions/index.md), denominato con il nome dell'estensione (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...). Le voci *Manga Generator* contrassegnate *(automatico)* sono la sua modalità continua.
- Le voci contrassegnate *(automatico)* sono le esecuzioni che il libro avvia da solo, l'[annotazione automatica](books/story-editor.md) dopo una generazione e la riscrittura che la segue. Sono scelte separatamente dalla finestra che apri tu: con **Annota con l'IA** spuntata e **Annota con l'IA (automatico)** no, l'annotazione automatica è silenziosa; con solo la voce automatica spuntata, solo l'annotazione automatica notifica.
- Anche un'esecuzione che termina con un errore notifica (*… non riuscito*), una che interrompi tu no.

La casella attiva le notifiche per il tuo account e abilita due pulsanti. **Registra questo browser** chiede al browser il permesso (da quel clic); la riga sotto i pulsanti indica che cosa dice questo browser: consentito, bloccato (consentilo nelle impostazioni del sito del browser) o non ancora richiesto. Il permesso appartiene al browser, non al tuo account: ogni browser o dispositivo in cui vuoi le notifiche va registrato una volta, mentre la casella e l'elenco sono gli stessi ovunque. **Invia una notifica di prova** ne mostra una subito, anche con la pagina in primo piano.

Il campo **Filtro** restringe l'elenco per nome; **Seleziona i mostrati** e **Deseleziona i mostrati** agiscono sulle voci che mostra e lasciano le altre come sono. Una notifica viene mostrata solo quando non stai guardando la pagina (un'altra scheda, o la finestra non è in primo piano); cliccarla riporta la pagina in primo piano.

Le notifiche richiedono un indirizzo `https://` o `localhost`: su un indirizzo `http://` semplice di un server nella tua rete il browser non le offre, e la scheda lo segnala. Una scheda che il browser ha messo in sospensione (di solito su un telefono) non può mostrarle. La scheda viene salvata con **Salva** delle impostazioni, come le altre.

<a id="extension-settings"></a>

### Impostazioni delle estensioni

Impostazioni delle [estensioni](extensions/index.md) installate, una sezione per estensione (per esempio *Reviewer Settings* e *SideQuery Settings*). Vengono salvate con lo stesso pulsante **Salva**. La scheda è vuota quando non è installata alcuna estensione con impostazioni.

![La scheda Impostazioni delle estensioni con le impostazioni di Lore Generator](../images/settings-extensions.png)
