---
label: Generatore di lore
order: 10
---

# Generatore di lore

Permette al modello di scrivere il contenuto di una [voce del lorebook](../lorebooks/entries.md) al posto tuo. Legge il lore che hai già, così la nuova voce si inserisce nel mondo, e compatta il risultato nella forma breve e densa in cui le voci di lore funzionano meglio.

!!!
Il Generatore di lore funziona nel [layout per telefono](../phone.md#extensions): il pulsante bacchetta si trova nella riga di una voce, e la finestra mostra il prompt in alto e il risultato sotto.
!!!

<a id="opening-the-generator"></a>

## Aprire il generatore

Con l'estensione installata, ogni riga dell'editor del lorebook riceve un pulsante bacchetta (**Generate lore entry**). Apre il generatore per quella voce. Se la voce ha già un contenuto, il dialogo lo segnala in alto: copiare il risultato nella voce sostituisce il contenuto.

Anche l'[editor a schermo intero](../lorebooks/lorebooks.md#editing-in-full-screen) ha la bacchetta: in ogni riga del suo elenco di voci, e come **Generate lore entry** nella riga del nome di una scheda voce. Ciò che copi nella voce appare subito nella sua scheda.
Aperto da un libro (la sua scheda *Lorebook*, o la scheda lorebook della storia), l'editor conosce il libro, quindi il libro paga il modello come nella pagina del lorebook.

Il generatore parte con il **Modello predefinito** e il **Protocollo predefinito** di *Impostazioni*, vedi [Account e impostazioni](../account.md). Gli elenchi **Modello** e **Protocolli** sotto l'intestazione del dialogo ti permettono di usare un altro modello o protocollo per questa generazione (la scelta non viene salvata); il protocollo è facoltativo. Senza un modello mostra solo un avviso.

![Il generatore di lore](../../images/plugin-loregenerator.png)

<a id="what-the-model-sees"></a>

## Cosa vede il modello

Le due scelte in alto nel dialogo decidono quale lore entra nella richiesta:

| Scelta | |
|---|---|
| **Current Lorebook** (predefinito) / **All Lore** | Solo le voci del lorebook in cui si trova la voce, ignorando i suoi [sotto-lorebook](../lorebooks/lorebooks.md); oppure le voci di quel lorebook insieme a tutti i suoi sotto-lorebook. |
| **Only up to position (n)** (predefinito) / **All entries** | *n* è l'ordine della voce. Vengono mostrate solo le voci che la precedono (un ordine inferiore, oppure lo stesso ordine e creata prima), così il modello costruisce su ciò che esiste già e non riceve lore scritto dopo la voce. *All entries* mostra tutto. |

La voce che stai generando non è mai inclusa, con o senza contenuto. Che altre voci o lorebook siano attivi, i loro tag, i filtri e la modalità di inserimento vengono ignorati: ciò che scegli qui è ciò che viene inviato, nell'ordine in cui le voci vengono inserite in un prompt. I template delle voci vengono elaborati come di consueto.

<a id="prompts"></a>

## Prompt

Il prompt di generazione è ciò che chiedi al modello di scrivere. I prompt sono elencati nella casella accanto alle scelte, nella stessa riga in alto; il testo di quello selezionato si trova nell'area di testo a sinistra, e puoi modificarlo per una singola generazione senza salvarlo. Le due metà del dialogo sono separate da un divisore che puoi trascinare.

| Pulsante | |
|---|---|
| **Salva** | Salva il testo con il nome selezionato. |
| **Save as** | Chiede un nome e salva il testo come nuovo prompt (oppure sostituisce uno con quel nome, dopo conferma). |
| **Elimina** | Elimina il prompt selezionato. Quando si elimina l'ultimo, torna il prompt integrato `DEFAULT`. |

Il prompt integrato ha segnaposto tra parentesi quadre (il soggetto, i dettagli da includere, la lunghezza, la struttura): compilali prima di eseguirlo. I prompt appartengono al tuo account, e quello usato per ultimo viene riselezionato alla successiva apertura del dialogo.

<a id="generation-and-compaction"></a>

## Generazione e compattazione

Il lato destro ha due schede.

**Generation** - **Run Generation** invia il lore seguito dal prompt di generazione. La risposta appare man mano che viene scritta, con il ragionamento del modello (quando ne fornisce uno) in una sezione *Thinking Process* sopra di essa. **Interrompi** (al posto di *Run Generation*) la termina in anticipo; ciò che era già stato scritto viene conservato.

**Compaction** - disponibile quando c'è una generazione. **Run Compaction** invia lo stesso lore seguito dal prompt di compattazione, con dentro il testo generato, e il modello lo condensa in una voce compatta. Il prompt di compattazione si imposta in *Impostazioni → Extension Settings → Lore Generator*; se è vuoto si usa quello integrato (elimina la prosa e mantiene i fatti). Rieseguire la generazione scarta la compattazione.

![La compattazione di una voce generata](../../images/plugin-loregenerator-compaction.png)

Mentre il modello lavora, tutti i controlli sono bloccati tranne **Interrompi**; non puoi nemmeno chiudere il dialogo.

<a id="remembered-per-entry"></a>

### Ricordato per voce

Quando usi **Copy to Lore Entry**, le opzioni di quella generazione vengono salvate con la voce: il testo del prompt, da quale prompt salvato proviene, le scelte di lore ed entries e le categorie di nomi selezionate. Aprendo di nuovo il generatore per quella voce, vengono ripristinate, con priorità sul prompt usato per ultimo e sulle categorie di nomi salvate. Una voce per cui non è mai stata generata una risposta si apre con i valori predefiniti abituali. Le opzioni appartengono alla voce, quindi vengono incluse nei backup e nelle esportazioni del lorebook.

<a id="tools"></a>

### Strumenti

Quando è installata una suite di [strumenti per il modello](tools.md) (nomi dal [Generatore di nomi](name-generator.md) o dal [Fornitore di nomi](name-provider.md), luoghi dal [Fornitore di luoghi](place-provider.md)), la riga in alto ha un pulsante **Strumenti**. Apre la finestra in cui scegli quali suite, e tra quelle con categorie quali categorie, il modello può usare; vedi [Strumenti per il modello](tools.md) per il funzionamento. Finché qualcosa è selezionato, il modello può richiamare gli strumenti durante la generazione (non la compattazione), e il suggerimento di ciascuna suite che riceve viene aggiunto in fondo al prompt di generazione. La scelta viene salvata con il tuo account e con la voce.

<a id="attachments"></a>

### Allegati

Sotto il prompt puoi aggiungere file che il modello può esaminare: immagini di riferimento, una mappa, una scheda personaggio. Trascinali nell'area di caricamento, scegliendoli, oppure incollali nel prompt con Ctrl+V (uno screenshot o un file copiato diventa un allegato; il testo incollato viene inserito normalmente). Fino a 20 MB ciascuno. Sono elencati con nome, tipo e dimensione, e il pulsante del cestino ne rimuove uno. Vengono inviati con la generazione, non con la compattazione, e puoi aggiungerli ed eliminarli solo quando nessuna generazione è in corso.

Se il modello può usarli dipende dal modello e dal [fornitore di inferenza](../inference-providers.md): le immagini vengono inviate come immagini, l'audio solo come WAV o MP3, gli altri file come file. Quando il fornitore rifiuta un allegato, ricevi un messaggio e non viene generato nulla.

<a id="copying-the-result"></a>

## Copiare il risultato

**Copy to Lore Entry** è disponibile quando la compattazione ha prodotto un risultato. Il testo compattato diventa il **content** della voce, e il testo generato prima della compattazione finisce nella sua **note**. Entrambi vengono sostituiti. Se la compattazione è un unico blocco di codice (il prompt integrato lo richiede, per rendere il testo facile da copiare), il blocco di codice attorno viene rimosso. Il lorebook viene aggiornato e il dialogo resta aperto; **Esci** lo chiude.
<a id="commands"></a>

## Comandi

Il generatore funziona anche senza la finestra di dialogo, come tre [comandi](../commands/index.md) per la riga di comando e per gli [agenti](../agents.md). Eseguono gli stessi due passaggi della finestra di dialogo e, come la finestra, non salvano nulla: restituiscono il testo, e spetta a chi chiama inserirlo in una voce (ciò che fa **Copy to Lore Entry**), quindi tutti e tre si limitano a leggere. Il lorebook e la voce vengono indicati con il numero, come in `/list-lorebooks` e `/list-lore-entries`; il modello è il tuo modello e protocollo predefiniti.

| Comando | Cosa fa |
|---|---|
| `/list-lore-templates [template]` | Elenca i tuoi prompt di generazione salvati (nome, lunghezza, inizio del testo) e quello usato per ultimo. Con un nome restituisce l'intero testo di quel prompt. |
| `/generate-lore-entry [prompt [template [lorebook [entry [order [scope [limit [images [compact]]]]]]]]]` | Genera un testo con il lore del lorebook, lo compatta (come `/compact-lore-entry`) e restituisce il testo compattato; con `compact` `false` restituisce il testo generato così com'è. Il testo inviato è un template seguito dal prompt di chi chiama (vedi sotto). Con `images` il modello guarda anche le immagini, come con gli allegati della finestra di dialogo. |
| `/compact-lore-entry text [lorebook [entry [order [scope [limit]]]]]` | Compatta il testo con lo stesso lore e restituisce il risultato, senza il blocco di codice attorno. |

La voce a cui è destinato il testo decide quale lore vede il modello. Indicala con il suo handle o con il suo numero come `entry` (e il `lorebook` in cui si trova, per handle o numero, quando dai il numero) e il lore è lo stesso che si usa nella finestra di dialogo: con `limit` `UP_TO_POSITION` (il valore predefinito) le voci prima di essa, mai la voce stessa. Una voce che non esiste ancora non ha un numero: indica invece l'`order` che otterrà (l'`order` di `/list-lore-entries`, prima viene quello più basso) e il modello vede le voci prima di quell'ordine; senza nessuno dei due, il testo viene scritto per una nuova voce dopo tutte le altre. `scope` è `CURRENT_LOREBOOK` (il valore predefinito) oppure `ALL_LORE`, `limit` è `UP_TO_POSITION` oppure `ALL_ENTRIES`, come nella finestra di dialogo.

La qualità di una generazione dipende dal suo prompt, quindi il comando parte sempre da uno dei tuoi prompt di generazione salvati (vedi [Prompt](#prompts)): quello indicato in `template`, oppure quello usato per ultimo se non ne viene indicato alcuno. Il `prompt` di chi chiama segue il template sotto il titolo *Task for this entry* ed è il punto in cui vanno il soggetto, i fatti, la lunghezza e le sezioni, ossia ciò che il template richiede. `template` `none` invia solo il prompt, per un chiamante che ha scritto da sé l'intero prompt, per esempio dopo aver ottenuto il testo di un template da `/list-lore-templates` e averlo modificato. Un comando chiamato con un template e senza prompt invia solo il template.

Le `images` di `/generate-lore-entry` sono le immagini che la finestra di dialogo allegherebbe alla generazione. Un [agente](../agents.md) passa un elenco di percorsi di file immagine del proprio workspace (`/refs/map.png`; al massimo 8, PNG, JPEG, GIF o WebP, ridimensionati se grandi), che inserisci con **Carica file** nella scheda Workspace. La barra dei comandi non ha modo di indicare un'immagine, quindi `images` è pensato per gli agenti. È il tuo modello predefinito a guardare le immagini: se non le supporta, il comando fallisce e lo segnala. Solo la generazione accetta immagini, come nella finestra di dialogo; la compattazione lavora solo sul testo.

Il parametro `compact` di `/generate-lore-entry` è `true` per impostazione predefinita: il comando genera e compatta in un'unica chiamata, quindi un [agente](../agents.md) ha bisogno di un solo comando e non può dimenticare il secondo passaggio. Passa `compact` `false` per ottenere il lungo testo generato (per esempio per rivederlo o modificarlo), ed esegui `/compact-lore-entry` su di esso in un secondo momento.

Il modello qui non riceve strumenti. Chi chiama e vuole dei nomi li chiede prima, con `/generate-names` del [Generatore di nomi](name-generator.md) o `/pick-real-names` del [Fornitore di nomi](name-provider.md), e scrive quelli che preferisce nel prompt. Un agente può anche prendere il testo di un template da `/list-lore-templates`, modificarlo e passare il risultato come prompt.

<a id="storage"></a>

## Archiviazione

I prompt di generazione e il prompt di compattazione sono salvati con le tue impostazioni, quindi i [backup del database](../administration/database-backups.md) li includono. Nulla viene salvato con il lorebook: il testo generato esiste solo nella finestra di dialogo finché non lo copi.
