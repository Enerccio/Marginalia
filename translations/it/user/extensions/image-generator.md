---
label: Generatore di immagini
order: 13
---

# Generatore di immagini

Crea un'immagine per una parte della storia. Il modello scrive il prompt dell'immagine a partire dalla parte, tu lo modifichi, un backend di immagini disegna il quadro, tu rigeneri finché non va bene e la accetti: diventa un'immagine della parte, come quelle che aggiungi a mano con *Immagini* nel menu della parte (viene mostrata nella storia e nelle esportazioni).

L'estensione non crea immagini da sola: parla con un server **ComfyUI** oppure con un server che usa la **OpenAI images API** (supportata anche da stable-diffusion.cpp, LiteLLM e molti servizi ospitati).

<a id="adding-a-backend"></a>

## Aggiungere un backend

*Impostazioni → Extension Settings → Image Generator Settings* elenca i backend. Aggiungine uno (+), compilalo e salva la pagina *Impostazioni*.

| Setting | Funzione |
|---|---|
| Name, Type | Il nome mostrato nella finestra. **ComfyUI** o **OpenAI images API**. |
| Address | L'indirizzo base del server, per esempio `http://localhost:8188` per ComfyUI. |
| Api key | Inviata come bearer token al server di immagini. È memorizzata cifrata e non viene più mostrata; scrivine una nuova per sostituirla, oppure usa **Remove the key**. |
| Key of the requests around the images | Inviata come `Authorization: Bearer` con ogni richiesta [prima e dopo le immagini](#one-gpu-for-the-model-and-the-images) che non ha un header `Authorization` proprio, per esempio la chiave di un proxy LiteLLM. Memorizzata cifrata. Se è vuota, si usa la api key. |
| Model | Solo per OpenAI images API: il `model` della richiesta. |
| Workflow | Solo ComfyUI: un workflow salvato in ComfyUI con **Save (API Format)**. Scrivi `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` e `{{height}}` dove vanno, e `{{lora}}` / `{{lora_strength}}` in un LoRA loader per [scegliere la LoRA nella finestra](#choosing-a-lora). |
| The server is on this machine or on my network | Consente gli indirizzi della rete locale. Lascialo spento per un server nel cloud: così non può essere indotto a chiamare la tua macchina. |
| Width, Height | Con cosa si apre la finestra (puoi cambiarli lì per una singola immagine). |
| Prompt form | Come il modello linguistico scrive il prompt per questo backend, in base al suo modello di immagini: **Tag** (frasi brevi separate da virgole, per SDXL, Pony, Illustrious), **Descriptive sentences** (un paragrafo, per Flux, Krea, Qwen-Image, Z-Image) oppure **Free text**, una tua istruzione per chi scrive il prompt (parte da quella scelta). Un backend creato prima che esistesse questa impostazione scrive tag, oppure usa come free text l'istruzione che avevi impostato allora. Le istruzioni integrate sono nella tua [lingua dei prompt](../account.md#prompt-language) e chiedono il prompt dell'immagine in inglese, la lingua che i modelli di immagini leggono meglio. |
| Timeout | Quanto attendere per una immagine. |
| GPU lock | Il [lock della GPU](../administration/gpus.md) della GPU su cui gira il server, quando la condivide con il modello linguistico o con altri server; vedi [Una GPU](#one-gpu-for-the-model-and-the-images). Mostrato solo se l'amministratore ha impostato i lock della GPU. |

![Un backend nelle impostazioni](../../images/plugin-imagegenerator-settings.png)

**Test connection** controlla l'indirizzo e la chiave.

Il pannello ha anche due testi che un libro può cambiare:

- **Stile** - messo davanti a ogni prompt: l'aspetto delle immagini, per esempio *watercolor, soft light*. Anche chi scrive il prompt ne è informato, così il prompt non descrive un altro stile.
- **Negative prompt** - ciò che le immagini non devono mostrare. Un modello turbo che gira senza guida (CFG 1) lo ignora. Il negative prompt che un backend aveva prima viene ripreso qui la prima volta che apri il pannello.

Quello che imposti nel pannello è il tuo default. Un libro (nella sua scheda *Prompt*) o una [cartella](../books/folders.md) possono impostarne uno proprio in *Extension Settings → Image Generator*; un libro usa il proprio, altrimenti quello della cartella più vicina, altrimenti il tuo.

<a id="making-an-image"></a>

## Creare un'immagine

![Generate image nel menu di una parte](../../images/plugin-imagegenerator-menu.png)

1. Scegli **Generate image** nel menu di una parte. Non si invia ancora nulla a un modello: prima compila ciò che chi scrive il prompt deve seguire, poi premi **Write the prompt with the model** (oppure <kbd>Ctrl+Enter</kbd> in *Details*). Il modello della finestra (quello del libro per default; l'ultima scelta viene ricordata) scrive il prompt della parte; puoi interromperlo con **Abort**, modificare il prompt, scriverlo tu o riscriverlo. Questi valori si applicano solo alla scrittura del prompt, non all'immagine stessa:
   - **Send lorebook entries** - le voci del lorebook del libro (e dei suoi sub lorebook) vengono inviate al modello come in una generazione, così sa come appaiono personaggi e luoghi. Una voce viene inviata quando i suoi filtri si trovano nella parte: nel suo testo, nelle istruzioni, nell'ambientazione della scena, nel punto di vista e nei personaggi presenti, oppure nei tuoi *Details*. Le voci sempre attive vengono incluse. Ne entrano solo quante ne consente il contesto del modello (un avviso indica quante ne sono rimaste fuori). La scelta viene ricordata.
   - **Details** - ciò che l'immagine deve mostrare o come deve apparire (*di notte, primo piano sul suo viso, il vestito rosso*); chi scrive il prompt lo segue. Vale solo per questa finestra.
2. Scegli il backend e premi **Genera**. L'immagine viene creata a partire dal **Prompt**, con lo **Stile** davanti e il **Negative prompt**; entrambi partono da quelli del libro (o dal tuo default) e una modifica vale solo per questa finestra (lo stile viene comunicato anche a chi scrive il prompt). La dimensione parte da quella del backend e può essere cambiata per questa finestra.
3. L'immagine compare. **Reroll** ne crea un'altra con un nuovo seed, **Discard** la scarta, **Accept** la aggiunge alla parte e chiude la finestra. **Abort** interrompe un'immagine in corso di creazione (ComfyUI riceve la richiesta di interrompere il lavoro).

![La finestra con il prompt scritto dal modello](../../images/plugin-imagegenerator-prompt.png)

![L'immagine, con Accept, Discard e Reroll](../../images/plugin-imagegenerator.png)

La parte aperta mostra subito la nuova immagine sotto il suo testo.

La finestra ricorda cosa hai usato per ogni parte: i dettagli, lo stile, il negative prompt, il prompt e la dimensione vengono conservati con la parte quando il prompt è stato scritto e quando generi, e la volta successiva che apri la finestra su quella parte riparte da essi. Una parte per cui non hai mai creato un'immagine parte con lo stile e il negative prompt del libro.

<a id="choosing-a-lora"></a>

## Scegliere una LoRA

Una LoRA è un piccolo complemento del modello di immagini, di solito per uno stile. Con ComfyUI puoi sceglierla nella finestra invece di salvare di nuovo il workflow per ciascuna:

1. Nel workflow esportato, scrivi `{{lora}}` come `lora_name` del LoRA loader (*Load LoRA*, `LoraLoaderModelOnly`, oppure `LoraLoader`), e `{{lora_strength}}` come sua forza (`strength_model`; `strength_clip` anche per `LoraLoader`).
2. La finestra mostra allora **LoRA** con i file LoRA del server (dalla sua cartella `models/loras`) e **LoRA strength**. Puoi anche digitare il nome di un file che l'elenco non mostra.
3. **None** (una LoRA vuota) toglie i LoRA loader dal workflow, così l'immagine viene creata senza di essi.

La LoRA e la forza con cui generi vengono ricordate per il backend e sono lì la volta successiva. Lo stesso vale per l'elenco: un server che condivide la GPU spesso non è in esecuzione quando si apre la finestra (le richieste prima della prima immagine lo avviano), quindi la finestra propone le LoRA che il server aveva elencato per ultimo e chiede di nuovo dopo la prima immagine. Solo quando non è mai riuscita a chiedere, un avviso dice che non è stato possibile leggere le LoRA; puoi comunque digitare il nome di un file.
<a id="one-gpu-for-the-model-and-the-images"></a>

## Una GPU per il modello e le immagini

Con una sola GPU piccola, il modello linguistico e il modello per le immagini non possono essere caricati insieme. L'amministratore configura questo una volta sola, per ogni modello e server su quella GPU, in [Admin → GPUs](../administration/gpus.md): un **Blocco GPU** per il modello linguistico e uno per il server delle immagini, ciascuno con le richieste che lo caricano e che liberano la GPU. Scegli il blocco del server delle immagini in **Blocco GPU** del backend (e il blocco del modello linguistico nelle sue [impostazioni AI](../inference-providers.md)).

Ogni immagine attende quindi il proprio turno sulla GPU:

- Se la GPU è già caricata con il server delle immagini, l'immagine parte subito; tra una rigenerazione e l'altra non viene scaricato nulla.
- Se la GPU ospita il modello linguistico (o un altro server), l'immagine attende che il modello abbia risposto; il blocco del modello libera la GPU e il blocco del server delle immagini la carica.
- Tra un'immagine e l'altra non viene trattenuto nulla: chiudere o ridurre la finestra, o lasciarla aperta, non tiene occupata alcuna GPU. La richiesta successiva al modello linguistico libera da sola la GPU del server delle immagini, che resta caricato fino a quel momento.

La finestra mostra *Generating the image* mentre attende il proprio turno.

**Richieste prima e dopo ogni immagine.** Un backend può anche avere due liste proprie di richieste, eseguite dentro il blocco attorno a ogni immagine: **Before each image** e **After each image**. Il rilascio della GPU spetta al blocco GPU; usa queste liste per tutto ciò di cui il server ha bisogno attorno a un'immagine. Ogni lista viene eseguita una richiesta dopo l'altra (**Add a request** ne aggiunge una, il cestino ne rimuove una). Ogni richiesta ha un indirizzo, un metodo (`POST` per impostazione predefinita), delle intestazioni (`Name: value`, una per riga), un corpo, un **timeout** (vuoto: 60 secondi, al massimo 600) e **Wait afterwards** (secondi di pausa dopo la risposta). Le intestazioni sono salvate cifrate, come la chiave API.

Quando una richiesta della lista *before* fallisce, le successive non vengono eseguite e l'immagine si ferma (il messaggio indica quale richiesta è fallita: *Request 2 of 3: ...*). La lista *after* viene eseguita **per intero** qualunque cosa accada a una delle sue richieste, e un errore viene scritto nel log dell'estensione; l'immagine viene comunque conservata. Un backend salvato con l'unica richiesta di una versione precedente la mostra come prima richiesta della lista.

I backend salvati prima che esistessero i blocchi GPU perdono le impostazioni **GPU (name)** e **Release after idle**: scegli invece un blocco GPU e sposta le richieste che liberavano la GPU (lo scaricamento del modello linguistico, `POST <address>/free` di ComfyUI) nei passaggi dei blocchi.

<a id="what-is-not-done"></a>

## Cosa non viene fatto

- I backend sono per utente, non per libro.
