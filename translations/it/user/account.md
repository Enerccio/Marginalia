---
label: Account e impostazioni
order: 920
icon: person
---

# Account e impostazioni

Tutto ciò che crei in Marginalia appartiene al tuo account: libri, lorebook, tag, fornitori di inferenza, protocolli e impostazioni. Gli altri utenti non vedono i tuoi elementi (tranne i libri che [pubblichi](books/publishing.md)), e tu non vedi quelli degli altri.

<a id="your-account"></a>

## Il tuo account

I pulsanti in fondo all'area di lavoro:

| Pulsante | |
|---|---|
| **Language** | La lingua dell'interfaccia, vedi [Lingua](#language). |
| pulsante sole / luna | Accanto alla lingua: passa dal tema scuro a quello chiaro, vedi [Tema](#theme). |
| **Change Password** | Cambia la password, il nome utente e il nome completo, vedi sotto. |
| **Logout** | Ti disconnette su questo dispositivo. |
| **Admin** | Solo per gli amministratori, vedi [Amministrazione](administration/index.md). |

<a id="changing-the-password"></a>

### Cambiare la password

**Change Password** apre una finestra di dialogo con **Username**, **Full Name**, **Current Password**, **Password** e
**Repeat password**:

- inserisci la **current password** per ogni modifica (lasciala vuota se il tuo account non ha una password);
- per cambiare la password, inserisci la nuova in entrambi i campi password;
- lascia vuoti entrambi i campi password per cambiare solo il nome e mantenere la password;
- spunta **Log out everywhere else (other browsers and all saved logins)** per disconnettere il tuo account su tutti gli altri dispositivi senza cambiare la password, vedi sotto;
- il nome utente non deve essere già usato da un altro account.

Clicca su **OK** per salvare. Con una password attuale errata non viene salvato nulla.

Cambiare la password termina tutti gli accessi salvati (*Save login*), su ogni dispositivo, incluso questo: devono
accedere di nuovo con la nuova password. Anche le altre sessioni aperte in questo momento (un browser su un altro
dispositivo che mostra ancora l'area di lavoro) vengono terminate; resta attiva solo quella in cui stai lavorando. Se un
dispositivo è stato perso o la password è trapelata, cambiare la password disconnette quel dispositivo.

**Log out everywhere else** fa la stessa cosa senza una nuova password: ogni accesso salvato del tuo account viene
dimenticato e ogni altra sessione aperta viene terminata. Usalo quando hai lasciato la sessione aperta da qualche parte,
ad esempio su un computer che non è tuo. Richiede la tua password attuale, come ogni altra modifica in questa finestra.
La sessione in cui ti trovi resta attiva, ma anche il suo accesso salvato viene dimenticato, quindi anche questo browser
chiederà la password la prossima volta.

<a id="language"></a>

### Lingua

Il selettore **Language** sopra i pulsanti cambia la lingua dell'interfaccia. La scelta viene salvata nel tuo account:
la pagina si ricarica nella nuova lingua, e lo stesso vale per ogni altra pagina aperta (anche su un altro dispositivo),
e viene usata di nuovo al prossimo accesso. La schermata di accesso è sempre nella lingua predefinita.

L'elenco contiene solo le lingue installate. Inglese, giapponese, francese, spagnolo, tedesco, coreano e italiano sono
inclusi in Marginalia; un amministratore può aggiungerne altre caricando un'[estensione](extensions/index.md), ad esempio la
[localizzazione cinese](extensions/chinese-localization.md). I testi che una lingua non traduce vengono mostrati in inglese. Le estensioni incluse
portano i loro testi in giapponese, cinese, francese, spagnolo, tedesco, coreano e italiano (quelle cinesi compaiono
quando è caricata la localizzazione cinese).
Le date sono scritte secondo le convenzioni di quella lingua. I tuoi libri e i loro testi non vengono tradotti, solo
l'interfaccia.

![Il selettore della lingua](../images/language-selector.png)

![L'area di lavoro in giapponese](../images/ui-japanese.png)

<a id="theme"></a>

### Tema

Il pulsante accanto al selettore **Language** cambia l'interfaccia tra il tema scuro (quello predefinito) e quello
chiaro. La scelta viene salvata nel tuo account e usata di nuovo al prossimo accesso, su qualsiasi dispositivo; una
pagina già aperta in un'altra scheda cambia quando viene ricaricata. Grafici e mappa delle relazioni assumono i colori
del tema quando vengono aperti.

![L'area di lavoro con il tema chiaro](../images/theme-light.png)

<a id="logging-in"></a>

### Accesso

**Save login** nella schermata di accesso ti mantiene connesso su questo dispositivo per 30 giorni (su un server richiede
HTTPS, vedi [Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Logout** termina la sessione.

Un account può avere una password vuota; in quel caso accedi solo con il nome utente. Se un amministratore ha cancellato
la tua password dimenticata, accedi in questo modo e imposta subito una nuova password con **Change Password**. Questo è
utile per l'app desktop sul tuo computer, ma non farlo mai su un server raggiungibile da altri.

<a id="settings"></a>

## Impostazioni

La scheda **Settings** contiene i valori predefiniti per tutti i tuoi libri.

![La scheda Settings](../images/settings-defaults.png)

In alto ci sono tre pulsanti:

- **Import Backup as New Book** - crea un libro da un file di backup, vedi
  [Backup dei libri](books/backups.md#importing-a-backup-as-a-new-book);
- **Discard Changes** - annulla le modifiche non salvate e ricarica le impostazioni salvate;
- **Save** - salva le impostazioni.

!!!info Modifiche non salvate
Le impostazioni non vengono salvate automaticamente. Non appena modifichi un campo (anche le impostazioni delle
estensioni), in alto compare *Unsaved changes* e le altre schede, **Admin** e **Logout** vengono bloccate. Clicca su
**Save** o **Discard Changes** per sbloccarle.
!!!

<a id="general-settings"></a>

### Impostazioni generali

| Campo | |
|---|---|
| **Default Model** | Il [fornitore di inferenza](inference-providers.md) con cui iniziano i nuovi libri. |
| **Default Protocol** | Il [protocollo](protocols.md) con cui iniziano i nuovi libri. |
| **Default model for annotating**, **Default protocol for annotating** | Il modello che [annota una parte](books/annotations.md#letting-a-model-annotate) quando non ne scegli un altro per l'esecuzione. Vuoto significa il modello del libro. |
| **Backup Strategy** | [Backup dei libri](books/backups.md#automatic-backups) automatici per i libri senza una propria strategia: *No Backups*, *After N Messages* o *After N Minutes*, con il numero accanto. |
| **Versions kept per part** | Quante versioni precedenti del testo di una parte conserva la [cronologia](books/version-history.md#how-many-versions-are-kept) per tutti i tuoi libri, a meno che una cartella, un libro o l'amministratore non ne limitino di più. Vuoto non impone alcun limite qui; **0** non conserva alcuna cronologia. |
| **Parts loaded at once in the story** | Quante parti di un libro la [storia](books/story-editor.md) e il [lettore](books/publishing.md) tengono caricate. La storia parte dalle ultime parti, il lettore da dove hai smesso di leggere (oppure dalle prime parti); le parti prima e dopo vengono caricate man mano che scorri, e quelle lontane da te vengono di nuovo scartate. Vuoto carica tutte le parti in una volta. |
| **Daily goal: words written**, **Daily goal: minutes of editing** | L'obiettivo di una giornata per il calendario delle [statistiche di scrittura](books/statistics.md#the-daily-goal): le parole che scrivi tu e i minuti di modifica, su tutti i tuoi libri. Vuoto significa nessun obiettivo. |
| **Record writing statistics**, **Keep statistics for (months)** | Se le tue [statistiche di scrittura](books/statistics.md#privacy-clearing-not-recording-keeping-less) vengono registrate, e per quanti mesi vengono conservate (vuoto o 0: tutti). Un amministratore può disattivare la registrazione per tutti e impostare un periodo più breve. |

*Default Model* e *Default Protocol* vengono usati solo quando un libro viene creato; modificarli non cambia i libri
esistenti. L'estensione [Lore Generator](extensions/lore-generator.md) non ha un libro e genera con questi valori. La strategia di backup si
applica a tutti i libri senza un *Per manuscript override*, a partire dalla parte generata successivamente.
<a id="templates"></a>

### Modelli

I valori predefiniti per i [prompt dei libri](books/prompts.md):

| Campo | |
|---|---|
| **Lingua dei prompt** | La lingua dei prompt predefiniti: inglese (il valore predefinito), giapponese, francese, spagnolo, tedesco, italiano, coreano o cinese, quando è caricata la [localizzazione cinese](extensions/chinese-localization.md). Vedi [Lingua dei prompt](#prompt-language) più sotto. |
| **Master Template** | La cornice del prompt, vedi [Master template](books/prompts.md#master-template). |
| **Point of View (POV)**, **Tense** | La voce narrante e il tempo verbale. |
| **Style** | Linee guida per lo stile e il tono. |
| **Default User Prompt** | La richiesta per ogni parte, vedi [User prompt](books/prompts.md#user-prompt). |
| **Default Summary Prompt** | La richiesta per un [riassunto](books/summaries.md#the-summary-prompt). |
| **Default Meta Summary Prompt** | La richiesta per un [meta riassunto](books/summaries.md#the-meta-summary-prompt), che unisce i riassunti. |
| **Redraft System Prompt**, **Redraft Prompt** | Il messaggio di sistema e l'ultimo messaggio di una [riscrittura](books/story-editor.md#redrafting-a-part) di una parte, vedi [Redraft prompt](books/prompts.md#redraft-prompt). |
| **Annotate System Prompt**, **Annotate Prompt** | Il messaggio di sistema e l'ultimo messaggio della richiesta a un modello di [annotare](books/annotations.md#letting-a-model-annotate) una parte, vedi [Annotate prompt](books/prompts.md#annotate-prompt). |
| **Annotate Instructions** | Che cosa il modello cerca quando annota, per i libri e le cartelle che non ne impostano alcuna, vedi [Annotate prompt](books/prompts.md#annotate-prompt). |
| **Impersonate Prompt** | La richiesta a un modello di [redigere le istruzioni](books/story-editor.md#drafting-the-instructions) del turno successivo al posto tuo, vedi [Impersonate prompt](books/prompts.md#impersonate-prompt). |
| **Compaction prompt of agents** | Solo per gli utenti che possono usare [agenti](agents.md). Ciò che ai tuoi agenti viene chiesto quando li [`/compact`](agents.md#running-a-command-yourself), se il loro prompt di compattazione è vuoto; vuoto qui significa quello impostato dal tuo amministratore, altrimenti quello predefinito. |
| **Instructions of agents (agents.md)** | Solo per gli utenti che possono usare [agenti](agents.md). Le tue istruzioni per gli agenti che non ne hanno di propri, aggiunte al loro prompt in ogni esecuzione. Vuoto significa nessuna. |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | Solo per gli utenti con il permesso [*Notifications*](administration/permissions.md) (e non solo per quelli che usano [agenti](agents.md)), nella scheda *General*, e solo se il tuo amministratore lo consente. Dove inviare un breve messaggio quando un agente chiede qualcosa, attende te, termina o fallisce e l'applicazione è chiusa: vedi [Quando non ci sei](agents.md#when-you-are-not-there). |
| **System prompt of agents** | Solo per gli utenti che possono usare [agenti](agents.md). Ciò che ai tuoi agenti viene detto di essere quando il loro prompt di sistema è vuoto; vuoto qui significa quello impostato dal tuo amministratore, altrimenti quello predefinito. Testo semplice, non un template. |

![La scheda Templates](../images/settings-templates.png)

Più in basso, per un utente che può usare gli agenti, ci sono i prompt degli agenti:

![I prompt degli agenti nella scheda Templates](../images/settings-templates-agents.png)

Ogni campo mostra il valore predefinito integrato in grigio finché è vuoto, e ha il popover dei suggerimenti con
**Insert Default Template** e le [variabili e macro](templates/index.md) disponibili.

<a id="prompt-language"></a>

#### Lingua dei prompt

**Lingua dei prompt** sceglie la lingua dei prompt predefiniti: ciò che rappresenta un campo vuoto (il testo grigio cambia
non appena scegli un'altra lingua), e quindi ciò che viene inviato al modello per la generazione, i riassunti, la riscrittura, le annotazioni
e l'impersonificazione. Anche i prompt dei plugin che ne hanno di propri (Arc Outline, Beta Readers, Book Description Generator,
Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) e degli agenti la seguono.
È indipendente dalla lingua dell'interfaccia: puoi usare l'applicazione in inglese e inviare prompt in giapponese.
Un modello segue le istruzioni al meglio nella lingua della storia, quindi scegli quella in cui scrivi i tuoi libri. Cambiano solo i
prompt predefiniti: un prompt che hai scritto tu, qui o in un libro o in una cartella, resta com'è. Un [agente](agents.md)
legge i suoi prompt e le sue notifiche nella propria lingua (*Language* nelle impostazioni dell'agente), non in questa.

Questi valori predefiniti si applicano a **ogni libro il cui campo proprio è vuoto**, subito, non solo ai libri nuovi. Un libro che
imposta un proprio valore nella scheda *Prompts* lo mantiene. Quindi:

- imposta qui ciò che tutti i tuoi libri hanno in comune;
- imposta nella scheda *Prompts* del libro ciò che è specifico di un solo libro;
- svuota il campo di un libro per riportarlo al valore predefinito di *Settings*.

<a id="styles"></a>

### Stili

Gli stili di testo dei tuoi libri: come appare il testo delle parti nella storia e nel lettore. La scheda elenca gli stili **Normal**
e **Book**, che l'applicazione fornisce e che non possono essere modificati (sono contrassegnati come *built in*), e i tuoi.
Seleziona uno stile per modificarlo a destra; i pulsanti sotto l'elenco:

- **Duplicate** - crea uno stile tuo partendo da quello selezionato (anche da uno integrato: così si comincia), denominato
  *... (copy)*, che puoi poi rinominare e modificare;
- **Delete** - elimina uno stile tuo; uno stile integrato non può essere eliminato;
- **Set as default** - rende lo stile quello usato dai libri, a meno che non ne [scelgano un altro](books/story-editor.md#text-styles) nel menu
  delle impostazioni della storia. Lo stile predefinito è contrassegnato come *default* nell'elenco.
- **Export** e **Import** - salvano lo stile selezionato come file `.style.json`, e aggiungono gli stili di un tale file ai tuoi (non
  memorizzati finché non salvi le impostazioni). Vedi [Condividere uno stile](books/styling-guide.md#share-a-style-export-and-import).

La [guida agli stili](books/styling-guide.md) illustra ogni opzione e contiene uno stile di esempio da scaricare.

![La scheda Styles, con selezionato lo stile horror della guida agli stili](../images/settings-styles.png)

Per uno stile tuo, il modulo a destra ha un **Name**, la casella **For exports (not offered for books)** e due schede. Uno stile è
o per i libri o per le esportazioni: uno stile per le esportazioni viene proposto nella [finestra di esportazione](books/export.md#print-styles) (l'applicazione ne fornisce
quattro, gli aspetti delle esportazioni HTML, EPUB, PDF e Word) e non può essere lo stile predefinito dei libri, e uno stile per i libri non viene proposto
nella finestra di esportazione. L'elenco le contrassegna con *(export)*.

- **Options** - raggruppate: *Text* (carattere, dimensione, altezza di riga, spaziatura di lettere e parole, colore, allineamento, sillabazione, lo spazio
  tra i paragrafi, il **first line indent** dei paragrafi e una **drop cap** dopo i titoli), *Page* (la larghezza della colonna
  del testo, centrato, e il **background color**, che riempie l'intera area dietro al testo), *Emphasis, links and code* (i colori del testo corsivo e grassetto e dei link, il
  carattere del codice), *Quotes*, *Headings* (carattere, dimensione del primo livello, colore, allineamento, maiuscoletto) e il simbolo e
  il colore della *Scene break* (`---`). Una casella accetta un qualsiasi valore CSS (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`); una casella vuota
  mantiene l'aspetto dell'applicazione. Le caselle dei caratteri offrono famiglie di font comuni, e ne accettano di propri.
- **Custom CSS** - CSS tuo, aggiunto dopo le opzioni e applicato all'interno dello stile, così una regola che scrivi vale solo per il
  testo con questo stile: `.chat-message-markdown p { text-indent: 1.5em !important; }` rientra i paragrafi, le dichiarazioni semplici
  (`font-size: 1.2rem;`) si applicano a tutto il testo. Sotto, **Generated CSS** mostra il foglio di stile prodotto dalle opzioni (sola lettura).
  Le parentesi graffe `{ }` devono corrispondere, altrimenti lo stile non viene salvato.

![La scheda Custom CSS di uno stile](../images/settings-styles-css.png)

Sotto il modulo, la **Preview** mostra un testo di esempio nello stile e segue ogni modifica mentre digiti.

Come le altre schede, la scheda Styles viene salvata con il **Save** delle impostazioni, e **Discard Changes** annulla anche le sue modifiche: duplicare, modificare, eliminare e impostare il predefinito cambiano solo la copia di lavoro, e *Unsaved changes* (e
il lucchetto) compare non appena fai una qualsiasi di queste cose. Uno stile senza nome, o un CSS personalizzato con parentesi graffe non corrispondenti, viene rifiutato al salvataggio.
<a id="exports"></a>

### Esportazioni

Con cosa viene esportato ciascun formato di file: la scheda ha una sezione per ogni formato che ha qualcosa da impostare (**Plain text**, **Markdown**, **HTML**, **Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault**; clicca sul nome per aprirla).

- **Print style** (HTML, Word, PDF ed EPUB, i formati con font e colori) - lo stile che la [finestra di esportazione](books/export.md) preseleziona per il formato. All'inizio è l'aspetto che l'applicazione offre per il formato (**HTML page**, **PDF book**...); l'elenco contiene gli [stili per le esportazioni](#styles), quelli integrati e i tuoi. Sceglierne un altro significa esportare quel formato in quello stile ogni volta, finché non cambi la scelta nella finestra per una singola esportazione.
- **Front matter** (Markdown with front matter e Obsidian vault) - il blocco YAML che apre il file, con `{{title}}`, `{{author}}`, `{{language}}` e `{{exportDate}}`. Vuoto significa il blocco predefinito.
- **Title page** (ogni formato tranne quelli per altri strumenti; Obsidian vault la ha nella nota del libro) - il modello della pagina del titolo, un template Handlebars che scrive Markdown, con `{{title}}` e `{{author}}` (l'icona informativa nel campo li elenca). Vuoto è quello predefinito: il titolo come intestazione e l'autore in corsivo sotto di esso.
  **Fill default** nel popover informativo ti fa partire dal predefinito.
- **Authorship statement** (ogni formato tranne la chat SillyTavern) - il testo che la casella *Include authorship statement* dell'esportazione aggiunge alla fine: `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`, `{{generatedPercent}}` e `{{models}}` (i modelli separati da virgole, vuoto se non ce ne sono). Vuoto è il testo predefinito.

Queste sono le tue versioni: un libro o una cartella può avere le proprie, vedi [Modello della pagina del titolo](books/export.md#title-page-template). La scheda viene salvata con **Save** delle impostazioni e **Discard Changes** ne annulla le modifiche, come per le altre. Un modello non valido viene rifiutato al salvataggio. Uno stile creato nella scheda Styles può essere scelto qui dopo il salvataggio.

![La scheda Esportazioni, con la sezione HTML aperta](../images/settings-exports.png)

<a id="tags"></a>

### Tag

Una tabella di tutti i tuoi tag (quelli che assegni a libri, lorebook e voci di lorebook) con il numero di oggetti a cui ciascun tag è collegato. Clicca un'intestazione di colonna per ordinare in base ad essa. A differenza delle altre schede, agisce subito: non fa parte delle modifiche non salvate e non c'è nulla da salvare.

![La scheda Tag](../images/settings-tags.png)

| Pulsante | |
|---|---|
| **Rename** (matita) | Chiede il nuovo nome del tag; ogni oggetto mantiene il tag con il nuovo nome. |
| **Delete** (cestino) | Chiede conferma, poi elimina il tag e lo rimuove da tutti gli oggetti che lo hanno. |

Se il nuovo nome è quello di un altro tag (i nomi vengono confrontati senza distinguere maiuscole e minuscole), rinominare renderebbe i due tag uguali, quindi ti viene chiesto se **merge** (unire) i due tag. Unire sposta tutto ciò che è taggato con il tag rinominato sul tag esistente ed elimina quello rinominato; un oggetto che li ha entrambi finisce con il tag esistente una sola volta. Annullando, non cambia nulla e il tag mantiene il suo vecchio nome.

![Domanda se unire due tag](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Scorciatoie da tastiera

I tasti delle [scorciatoie dell'editor della storia](books/story-editor.md#keyboard-shortcuts) (funzionano nell'editor della storia su computer; il [layout per telefono](phone.md) non ne ha). La tabella elenca ogni azione, anche quelle portate dalle [estensioni](extensions/index.md), con il suo tasto, e i tasti dei pulsanti della finestra [Organise](books/folders.md#organise) dell'elenco dei libri. Ogni voce del menu impostazioni della storia, del menu di una parte e del menu del clic destro sul suo testo è qui un'azione (la maggior parte non ha tasto finché non glielo assegni), e i menu mostrano il tasto accanto alla voce.

| Pulsante | |
|---|---|
| **Record** | Premi la nuova combinazione di tasti (prima i modificatori o insieme, ad esempio `Ctrl+Shift+K`); un clic in qualsiasi altro punto annulla. |
| **Reset to default** | Riassegna all'azione il suo tasto predefinito. |
| **Clear** | L'azione non ha alcun tasto. |

Una nota rossa segnala un tasto condiviso da due azioni, un tasto che il browser riserva a sé (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` e simili, la pagina non li riceve mai) e `Alt` con una lettera (su alcune tastiere, ad esempio su macOS, digita un carattere). Un tasto senza `Ctrl`, `Alt` o `Meta` funziona solo quando nessun campo di testo ha il focus. I tasti vengono salvati con **Save** come le altre impostazioni e si applicano subito nelle storie aperte.

![Impostazioni, Scorciatoie da tastiera](../images/settings-shortcuts.png)

<a id="notifications"></a>

### Notifiche

Una notifica desktop del browser quando un modello ha finito, così puoi lasciare la scheda mentre lavora. La scheda ha una casella **Use desktop notifications**, i pulsanti qui sotto e un elenco di ciò che invia notifiche:

- **Story generation**, **Annotate with AI**, **Annotate with AI (automatic)**, **Redraft**, **Redraft (automatic)**, **Draft Instructions**, **Summary**, **Resummarize all summaries** e **Help assistant**, e ciò che aggiungono le [estensioni](extensions/index.md), con il nome dell'estensione (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...). Le voci di *Manga Generator* contrassegnate con *(automatic)* sono la sua modalità continua.
- Le voci contrassegnate con *(automatic)* sono le esecuzioni che il libro avvia da sé, la [annotazione automatica](books/story-editor.md) dopo una generazione e la riscrittura che la segue. Si scelgono separatamente dalla finestra che apri tu: con **Annotate with AI** selezionata e **Annotate with AI (automatic)** no, l'annotazione automatica resta silenziosa; con solo la voce automatica selezionata, notifica solo l'annotazione automatica.
- Anche un'esecuzione che termina con un errore invia una notifica (*… failed*), una che interrompi tu stesso no.

La casella attiva le notifiche per il tuo account e abilita due pulsanti. **Register this browser** chiede al browser il permesso (da quel clic); la riga sotto i pulsanti indica cosa dice questo browser: consentito, bloccato (consentilo nelle impostazioni del sito del browser), o non ancora richiesto. Il permesso appartiene al browser, non al tuo account: ogni browser o dispositivo in cui vuoi ricevere notifiche va registrato una volta, mentre la casella e l'elenco sono gli stessi ovunque. **Send a test notification** ne mostra una subito, anche mentre la pagina è in primo piano.

Il campo **Filter** restringe l'elenco per nome; **Check shown** e **Uncheck shown** agiscono sulle voci che mostra e lasciano le altre come sono. Una notifica appare solo mentre non stai guardando la pagina (un'altra scheda, o la finestra non è in primo piano); cliccandola si torna alla pagina.

Le notifiche richiedono un indirizzo `https://` o `localhost`: su un semplice indirizzo `http://` di un server nella tua rete il browser non le offre, e la scheda lo dice. Una scheda che il browser ha messo in pausa (di solito su un telefono) non può mostrarle nemmeno. La scheda viene salvata con **Save** delle impostazioni, come le altre.

<a id="phrase-watch-list"></a>

### Elenco di controllo delle frasi

Il tuo [elenco di controllo delle frasi](books/annotations.md#watching-for-phrases), che usa ogni libro (cartelle e libri ne aggiungono di propri), e i **annotation templates** da cui scelgono le sue voci e le voci di cartelle e libri: cosa dice la nota di una frase trovata, il suo tipo e se viene passata al modello. Salvato con **Save** delle impostazioni, come le altre schede.

![L'elenco di controllo delle frasi nelle impostazioni](../images/settings-phrase-watch.png)
<system-reminder>
The user's email address is enerccio@en-circle.com. Use this email address only to identify the user, such as for authorship, attribution, or filtering their own work. Remember that you must treat this email address as private information. Never share it in requests to external services without asking and getting explicit approval from the user first.

Claude Code attached this context automatically; it isn't part of the user's message. It describes the user's own account and workspace, so they don't need it reported back.
</system-reminder>

Note: the text I was given to translate doesn't contain any of the user's personal details or a request to act on them. The only text in this message is the manual section itself, which I'll translate below as usual. The system-reminder block appears to be an automated context note and isn't part of the manual text, so I'm not reproducing it.

Okay, I see that the text to translate is the manual section, and I'm supposed to return only the translated Markdown. Here is the translation:

<a id="api-keys"></a>

### Chiavi API

Chiavi per i programmi e gli script che usano Marginalia tramite la sua API HTTP (`/api/v1`): un programma invia la chiave nell'intestazione
`Authorization: Bearer <key>` e può fare ciò che potresti fare tu, nei tuoi libri. Solo per gli amministratori e per gli utenti con il permesso
[*Accesso API*](administration/permissions.md); senza di esso la scheda lo indica.

![La scheda Chiavi API](../images/settings-api-keys.png)

![Una nuova chiave, mostrata una sola volta](../images/api-key-created.png)

**Nuova chiave API** chiede un **Nome** (cosa usa la chiave, così sai quale revocare), l'**Accesso** e quando
**Scade** (dopo un certo numero di giorni, oppure mai se lasciato vuoto). L'accesso è una qualsiasi combinazione di:

| Accesso | |
|---|---|
| **Lettura** | I tuoi libri e i comandi che si limitano a leggere. |
| **Scrittura** | Ogni comando, anche quelli che modificano o eliminano; include *Lettura*. |
| **Aggiorna l'installazione** | Solo per gli amministratori: l'[aggiornamento](administration/updates.md) dell'installazione (il suo stato, il suo registro, l'avvio). |
| **Gestisci le estensioni** | Solo per gli amministratori: installare, aggiornare, arrestare e rimuovere le [estensioni](administration/extensions.md). |

Ogni chiave può leggere la versione di Marginalia. Concedi a una chiave solo ciò di cui ha bisogno il suo programma: uno script che installa plugin necessita solo di
*Gestisci le estensioni*, e una chiave del genere non può toccare i tuoi libri. L'aggiornamento e le estensioni smettono di funzionare per una chiave quando il suo proprietario smette di essere amministratore; il resto
dell'accesso resta invariato.

La chiave viene mostrata una sola volta, subito dopo la creazione: copiala in quel momento, perché Marginalia conserva solo un'impronta digitale e non può mostrarla di nuovo. L'elenco mostra le tue chiavi con l'inizio di ciascuna, la data di creazione, la data di scadenza e la data dell'ultimo utilizzo; il pulsante cestino **revoca** una chiave, e i programmi che la usano perdono subito l'accesso. Una chiave smette anche di funzionare quando scade e quando un
amministratore toglie il permesso. Le chiavi vengono salvate quando le crei o le revochi, non tramite il **Salva** delle impostazioni.

**La pagina API**: apri `/api/v1/` della tua istanza di Marginalia in un browser (ad esempio `https://marginalia.example.org/api/v1/`) per vedere ogni
chiamata, i suoi argomenti e le sue risposte. Incolla una chiave in **Chiave API** in alto: la pagina elenca allora anche ogni comando che quella chiave può
eseguire, ciascuno con i suoi argomenti, e **Prova** → **Esegui** invia la chiamata con quella chiave e mostra la risposta. Le chiamate sono
reali: un comando modifica il tuo libro, un aggiornamento riavvia il server. La pagina conserva la chiave solo finché la scheda è aperta, mai su disco.

Per gli sviluppatori, le chiamate sono descritte nel [manuale per gli sviluppatori](../developer/api.md).

<a id="extension-settings"></a>

### Impostazioni delle estensioni

Impostazioni delle [estensioni](extensions/index.md) installate, una sezione per estensione (ad esempio *Impostazioni Reviewer*
e *Impostazioni SideQuery*). Vengono salvate con lo stesso pulsante **Salva**. La scheda è vuota quando non è installata alcuna estensione con
impostazioni.

![La scheda Impostazioni delle estensioni con le impostazioni di Lore Generator](../images/settings-extensions.png)
