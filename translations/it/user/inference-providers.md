---
label: Provider di inferenza
order: 980
icon: server
---

# Provider di inferenza

Un provider di inferenza è una connessione a un modello: l'indirizzo dell'API, la chiave API, il modello e i suoi limiti. Ogni
libro usa un provider. Puoi averne quanti ne vuoi, per esempio un grande modello in cloud per la storia e uno piccolo
in locale per gli esperimenti, e cambiare in qualsiasi momento il provider di un libro.

I provider appartengono al tuo account. Gli altri utenti non vedono né i provider né le loro chiavi API. Le chiavi API sono memorizzate cifrate (vedi [La cartella dei dati](administration/index.md#the-data-folder)).

Per aggiungere un provider serve il permesso **Crea modelli propri**, che assegna un amministratore (gli amministratori lo hanno sempre, vedi [Permessi](administration/permissions.md)). Senza di esso puoi usare solo i provider che gli amministratori
[condividono](#shared-providers) con te; il pulsante **Aggiungi provider di inferenza** non viene mostrato.

![La scheda Provider di inferenza](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## API supportate

Marginalia funziona con qualsiasi API che implementa l'interfaccia **OpenAI Chat Completions** (`/v1/chat/completions`,
con streaming). La maggior parte dei servizi e dei server locali la supporta:

| Servizio | URL OpenAI |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Le porte sono quelle predefinite di ciascun server; usa quelle su cui il tuo server è effettivamente in esecuzione.

!!!warning Porta 8080
Anche la versione Docker di Marginalia usa la porta 8080. Se esegui llama.cpp sulla stessa macchina, avvialo su un'altra
porta (`llama-server --port 8081`).
!!!

!!!
Con la [versione Docker](getting-started/docker-server.md), `localhost` indica il contenitore di Marginalia. Usa
`http://host.docker.internal:<port>/v1` per un server di modelli sulla stessa macchina, vedi
[Modelli sulla stessa macchina](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Aggiungere un provider

Apri la scheda **Provider di inferenza** e fai clic su **Aggiungi provider di inferenza**. I provider esistenti si modificano con il pulsante con la matita e si eliminano con il pulsante del cestino. Il pulsante di clonazione (due fogli) crea una copia di un provider con un nome che confermi (quello originale con *(Copy)*): la copia ha lo stesso indirizzo, modello, **chiave API** (o le impostazioni dell'estensione), ragionamento, limiti di token e parametri, quindi un secondo modello sullo stesso servizio richiede un nome e un clic. La copia non è condivisa e non ha limiti né utilizzo propri. Puoi clonare solo i tuoi provider (non quelli condivisi da un amministratore, di cui non devi poter vedere la chiave) e solo quando ti è consentito crearne.

In cima alla finestra di dialogo:

| Campo | |
|---|---|
| **Nome** | La tua etichetta per il provider, mostrata quando scegli un modello per un libro. Includi il modello, ad esempio `OpenRouter - Claude Sonnet`. |
| **Tipo** | Il tipo di API: **Compatibile con OpenAI**, oppure **Estensione** per un provider che porta un'[estensione](extensions/index.md) (è offerto solo mentre tale estensione è caricata, ad esempio [Google Gemini](extensions/google-genai.md) o [Anthropic Claude](extensions/anthropic.md)). Il tipo non può essere cambiato dopo la creazione del provider. |
| **Provider** | Solo per il tipo **Estensione**: di quale estensione è il provider. Le sue impostazioni compilano la scheda *Impostazioni per tipo*. |

<a id="per-type-settings"></a>

### Impostazioni per tipo

![Impostazioni per tipo con l'elenco dei modelli caricato](../images/inference-provider.png)

La scheda è più alta della finestra di dialogo e scorre. I campi in basso:

![Timeout, tentativi, parametri aggiuntivi e Prova la connessione](../images/inference-provider-2.png)

| Campo | |
|---|---|
| **URL OpenAI (termina con /v1)** | L'indirizzo base dell'API, vedi [la tabella sopra](#supported-apis). Senza `/chat/completions`. |
| **Chiave API** | La chiave del servizio. Un server locale di solito non la controlla: lascia il campo vuoto e Marginalia invia il segnaposto `none`. |
| **Modello** | Il modello da usare. Fai clic su **Aggiorna** per caricare l'elenco dei modelli dall'API, oppure digita tu l'ID del modello (ad esempio `anthropic/claude-sonnet-4.5` su OpenRouter). |
| **Timeout (secondi)** | Quanto attendere la risposta dell'API e, una volta iniziato a scrivere, altro testo. Il valore predefinito è 300. I modelli di ragionamento possono richiedere minuti prima del primo testo; una richiesta che non riceve nulla per questo tempo fallisce con un messaggio di timeout (il ragionamento di un modello conta come testo, quindi un modello che continua a ragionare non viene interrotto). Una modifica viene usata dalla richiesta successiva, anche di un agente che sta lavorando. |
| **Tentativi** | Quante volte una richiesta viene ripetuta quando l'API risponde con un limite di frequenza (429) o un errore del server (5xx), oppure non può essere raggiunta. L'attesa tra un tentativo e l'altro cresce, e viene rispettato l'header `Retry-After` dell'API. Il valore predefinito è 2; 0 disattiva i nuovi tentativi. Viene ripetuto solo l'inizio di una richiesta: il testo già arrivato non viene mai scritto due volte. |
| **Tokenizer** | Come Marginalia conta i token dei testi per questo provider. **Automatico** (il predefinito) prova uno dopo l'altro gli endpoint tokenizer del server (OpenAI, llama.cpp, LiteLLM) e usa il primo che risponde, e una stima locale se nessuno risponde. Scegline uno per usare solo quello, oppure **LOCAL_TOKKIT** per la stima locale che non chiama mai il server (utile quando contare farebbe caricare al server un altro modello). Anche un tokenizer di un plugin compare qui; riceve l'indirizzo, il modello e il testo, mai la chiave API, e solo se lo scegli tu. **Prova il tokenizer** conta un testo di esempio con quello che il provider proverebbe e mostra cosa ha risposto ciascuno. Quando crei un provider, un tokenizer di plugin adatto all'indirizzo viene preselezionato finché non scegli tu. |
| **Rinvia il ragionamento** | Se il modello riceve indietro il proprio ragionamento nella richiesta successiva, vedi [Rinvia il ragionamento](#send-the-thinking-back). Solo per i provider compatibili con OpenAI: i provider Anthropic e Google inviano da soli ciò che le loro API richiedono. |
| **Parametri aggiuntivi** | Un oggetto JSON con campi extra per ogni richiesta a questo provider, ad esempio `{"top_k": 40, "min_p": 0.05}` per un server locale, o `{"provider": {"order": ["anthropic"]}}` per OpenRouter. Un campo impostato qui prevale sulla stessa impostazione del [protocollo](protocols.md) (ad esempio `temperature`) o sul limite di risposta (`max_completion_tokens`). Deve essere un oggetto JSON valido oppure vuoto. |

Quando modifichi un provider esistente, la chiave API non viene più mostrata. Resta invariata finché non fai clic su **Reimposta la chiave API** e non ne inserisci una nuova.

**Aggiorna** chiede all'API l'elenco dei modelli (`/v1/models`) con l'URL e la chiave inseriti nel modulo. Se fallisce con *Failed to download model list*, controlla l'URL (deve terminare con `/v1`), la chiave e che il server sia in esecuzione. Alcuni servizi non elencano i modelli: in quel caso digita invece l'ID del modello.

**Prova la connessione** verifica i valori nel modulo senza salvarli: chiede all'API l'elenco dei modelli e poi una risposta di un solo token dal **Modello** selezionato, senza tentativi. Un messaggio di successo significa che URL, chiave e modello funzionano insieme. In caso di errore viene indicata la causa (vedi [Messaggi di errore](#error-messages)). Alcuni servizi non hanno un elenco di modelli; in quel caso il test richiede solo la risposta di un token.

<a id="general-settings"></a>

### Impostazioni generali

![Impostazioni generali di un provider di inferenza](../images/inference-provider-general.png)

| Campo | |
|---|---|
| **Dimensione massima del contesto** | Obbligatorio. Quanti token il modello accetta in totale, prompt e risposta insieme. |
| **Token massimi di risposta** | Obbligatorio. La risposta più lunga che il modello può scrivere. Viene inviata all'API come `max_completion_tokens`, e questa quantità del contesto viene riservata alla risposta. Deve essere inferiore a *Dimensione massima del contesto*. |
| **Connessioni** | Quante richieste possono essere in corso verso questo modello contemporaneamente: tutte, di ogni utente e di ogni tipo (la tua generazione, una query secondaria, una traduzione, un'annotazione, [agenti](agents.md#one-model-several-agents), plugin). Le altre attendono il proprio turno, nell'ordine di arrivo; una generazione che deve attendere mostra *Waiting for the model: N requests before this one*, e **Interrompi** la toglie dalla coda. **1** (il predefinito) per un server locale con un solo slot (una GPU); un modello in cloud che risponde a più richieste insieme può averne di più, così una query secondaria può girare accanto a una generazione. |
| **Blocco GPU** | Solo quando l'amministratore ha configurato le [GPU](administration/gpus.md): il blocco della GPU su cui gira questo modello, quando la scheda è condivisa con altri modelli o con server di immagini e voce. Ogni richiesta attende quindi il proprio turno sulla GPU (contato con le richieste che la precedono) e, quando la GPU contiene altro, lo libera e carica questo modello con i passaggi del blocco prima. Vuoto: nessun blocco. |
| **Invia le immagini del lore** | Invia le [immagini delle voci del lorebook](lorebooks/lorebooks.md#editing-in-full-screen) che un turno usa insieme al loro testo. Disattivato di default: attivalo solo per un modello che accetta immagini; un modello senza visione rifiuta la richiesta e il turno fallisce. Le immagini delle voci nel blocco lore vanno in un messaggio a sé subito dopo il prompt di sistema (con una risposta breve del modello), le altre con il tuo prompt. |
| **Immagini nel messaggio di sistema** | (Compatibile con OpenAI, con *Invia le immagini del lore*) Inserisce le immagini del blocco lore nel messaggio di sistema stesso, accanto al testo del lore. Solo per un server e un chat template che le accettano lì; la maggior parte no, e la richiesta fallisce. |
| **Richiede jailbreak**, **Prompt di jailbreak** | Un prompt extra per i modelli che rifiutano di scrivere alcuni contenuti. Quando è selezionato, il prompt di jailbreak viene messo in primissima posizione nel prompt. |
| **Abilita il ragionamento**, **Impegno di ragionamento** | Per i modelli di ragionamento ("thinking"), vedi [Modelli di ragionamento](#reasoning-models). |

Marginalia lascia libera per la risposta la quota di contesto indicata da **Token massimi di risposta** e riempie il resto con il prompt: i template, le voci del [lorebook](lorebooks/index.md) e i [riassunti](books/summaries.md) vengono conteggiati, e le parti più vecchie della storia che non ci stanno vengono escluse. Con un contesto di 32 000 token e risposte fino a 2 000 token, il prompt ottiene 30 000 token.

Prendi il valore di **Dimensione massima del contesto** dalla documentazione del servizio, e impostalo un po' più basso: i conteggi dei token non sono sempre esatti, e una richiesta che supera il limite reale fallisce. Un [protocollo](protocols.md) può abbassare entrambi i limiti per alcuni libri, ma non alzarli.
<a id="error-messages"></a>

## Messaggi di errore

Quando l'API rifiuta una richiesta, Marginalia indica cosa correggere invece di mostrare un errore interno, seguito da quanto ha detto l'API stessa:

![Un errore del provider mostrato all'utente](../images/error-dialog.png)

| Messaggio | Causa |
|---|---|
| *Il provider ha rifiutato la chiave API.* | **Chiave API** errata, scaduta o mancante. |
| *La chiave API non è autorizzata a usare questo modello o questo endpoint.* | La chiave è valida, ma non ha accesso al modello. |
| *Il provider non riconosce il modello configurato.* | **Modello** errato; usa **Aggiorna** per vedere i nomi che l'API conosce. |
| *Nessun risultato trovato all'URL configurato.* | **OpenAI URL** errato; di solito termina con `/v1`. |
| *Il prompt non entra nel contesto del modello.* | **Dimensione massima del contesto** è superiore a quella che il modello accetta davvero. Abbassala. |
| *Il provider sta limitando le richieste.* | Limite di frequenza superato o quota esaurita, anche dopo i tentativi ripetuti. |
| *Il provider non è riuscito a elaborare la richiesta.* | Il servizio ha un problema (errore 5xx), anche dopo i tentativi ripetuti. |
| *Il provider non ha risposto in tempo.* | Aumenta **Timeout (secondi)**, oppure controlla il server. |
| *Impossibile connettersi al provider.* | Il server non è in esecuzione o non è raggiungibile da Marginalia, oppure l'URL contiene un errore di battitura. |
| *Il provider ha rifiutato la richiesta.* | Un altro rifiuto, spesso dovuto a un campo non supportato in **Parametri aggiuntivi** oppure al ragionamento che il modello non supporta. |

<a id="warning-before-sending"></a>

## Avviso prima dell'invio

Marginalia riempie il contesto solo fino a **Dimensione massima del contesto** meno **Token massimi di risposta**. Se il prompt finale è comunque più grande di questo valore (per esempio perché un'estensione ha aggiunto testo, oppure perché il conteggio dei token non era preciso), prima dell'invio della richiesta compare un avviso con le dimensioni stimate. La richiesta viene inviata comunque; l'API potrebbe rifiutarla oppure troncare il prompt.

![L'avviso su un prompt che potrebbe non entrare nel contesto](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modelli di ragionamento

I modelli di ragionamento pensano prima di rispondere. Con **Abilita il ragionamento** selezionato, Marginalia invia all'API l'**Impegno di ragionamento** scelto (*Nessuno*, *Basso*, *Medio* o *Alto*) come `reasoning_effort`. Un impegno maggiore di solito produce testi meglio pianificati, ma risposte più lente e più costose. Non tutte le API supportano questo parametro: se con il ragionamento attivo le richieste falliscono, disattivalo.

Quando il modello restituisce il proprio ragionamento (come `reasoning_content` o `reasoning`, a seconda del server), questo viene mostrato sopra la parte generata, sotto **Mostra il ragionamento**. Il ragionamento non fa parte della storia e non viene rinviato al modello, a meno che tu non imposti **Rinvia il ragionamento** (vedi sotto).

<a id="send-the-thinking-back"></a>

### Rinvia il ragionamento

**Rinvia il ragionamento** (in *Impostazioni per tipo* di un provider compatibile con OpenAI) stabilisce se il ragionamento del modello viene rinviato al modello, come `reasoning_content` del suo messaggio precedente, nella richiesta successiva:

| Valore | |
|---|---|
| **Nessuno** (predefinito) | Mai. Adatto alla maggior parte dei modelli e dei server, e alle API che rifiutano un campo sconosciuto. |
| **Tra le chiamate agli strumenti** | Solo mentre continuano le chiamate agli strumenti di una stessa risposta: il modello che ha chiamato uno strumento riceve indietro il proprio ragionamento insieme al risultato. Un nuovo messaggio dell'utente parte senza il ragionamento precedente. È ciò che Google richiede per Gemma 4 (*thoughts must not be removed between the function calls*, ma rimossi tra un turno e l'altro). |
| **Sempre** | Anche con le risposte precedenti che hanno un ragionamento, dove la chat lo conserva (lo fa la procedura guidata della trama dell'Arc Outline). Un server il cui template elimina da solo il ragionamento vecchio, come quello di Gemma 4, lo ignora. |

Vale per il ciclo degli strumenti (la procedura guidata della trama, i generatori che usano strumenti) e per gli agenti: un agente riceve indietro il ragionamento dei propri turni precedenti finché continuano le chiamate agli strumenti di un unico turno (il ragionamento viene conservato insieme al messaggio nella console, non nel messaggio memorizzato). Il server deve accettare il campo: llama.cpp lo accetta (legge `reasoning_content`, non `reasoning`), anche dietro LiteLLM; se un provider rifiuta i campi sconosciuti dei messaggi e fallisce, lascia l'impostazione su *Nessuno*.

![Ragionamento di una parte generata](../images/reasoning-view.png)

Il ragionamento conta ai fini della risposta: dai ai modelli di ragionamento un **Token massimi di risposta** più alto, altrimenti il testo potrebbe essere troncato dopo un ragionamento lungo.

<a id="token-counting"></a>

## Conteggio dei token

Marginalia conta i token per decidere quanta parte della storia entra nel prompt. Quando può, chiede il conteggio al provider, provando questi metodi in ordine:

1. l'endpoint di conteggio dei token di OpenAI (`/v1/responses/input_tokens`),
2. l'endpoint `/tokenize` di llama.cpp,
3. l'endpoint `/tokenize` di LiteLLM,
4. l'endpoint `/v1/messages/count_tokens` di LiteLLM (per i modelli Anthropic),
5. una stima locale con il tokenizzatore di OpenAI.

Viene usato il primo metodo che funziona, da quel momento in poi. La stima locale è vicina per la maggior parte dei modelli, ma non esatta: è un altro motivo per tenere **Dimensione massima del contesto** un po' sotto il limite reale. I provider di tipo *Estensione* contano come dichiara la loro estensione; quelli di [Google Gemini](extensions/google-genai.md) e [Anthropic Claude](extensions/anthropic.md) chiedono al provider di contare.

<a id="prompt-cache"></a>

## Cache dei prompt

Ogni richiesta che Marginalia fa per un tipo di lavoro (una nuova parte, un'annotazione, una riscrittura, un riassunto...) porta con sé l'intestazione `X-Session-ID`, un hash uguale per lo stesso utente e lo stesso tipo di lavoro. Un server o un proxy che mantiene una cache dei prompt può usarla per trovare la cache della richiesta precedente di quel tipo. Un server che non la conosce la ignora. Vedi [Cache dei prompt e id di sessione](prompt-cache.md), con un esempio per uno slot di llama.cpp salvato su disco.

<a id="shared-providers"></a>

## Provider condivisi

In un'installazione con più utenti, un amministratore può **condividere** un provider: seleziona **Condiviso con tutti gli utenti** nella finestra di dialogo del provider (la casella è visibile solo agli amministratori). Ogni utente trova allora il provider nell'elenco **Modello** dei propri libri e nella scheda **Provider di inferenza**, contrassegnato come *Condiviso* *Sì*. Possono usarlo, ma non aprirlo, vederne l'indirizzo o la chiave API, modificarlo o eliminarlo; solo il proprietario può farlo. I libri, i backup e i ripristini che fanno riferimento a un provider condiviso continuano a funzionare per chiunque possa vederlo.

![Un provider condiviso nella scheda Provider di inferenza di un utente normale](../images/provider-shared-user.png)

Condividere un provider permette agli altri utenti di consumare la quota del proprietario presso il servizio. Per impedire agli utenti di aggiungere provider propri, così da poter usare solo quelli condivisi, vedi [Utenti e provider](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Limiti dei provider condivisi

Un amministratore può limitare ciò che ogni utente può fare con un provider condiviso. Nella scheda **Provider di inferenza**, l'amministratore proprietario di un provider condiviso ha un pulsante **Limiti** (un utente con un lucchetto) nella sua riga, che apre una tabella con i limiti impostati. **Aggiungi limite** ne crea uno nuovo: scegli l'utente e un tipo di limite.

![I limiti di un provider condiviso](../images/provider-limits.png)

| Tipo di limite | Significato |
|---|---|
| Illimitato | Nessun limite (equivale a non averne). |
| Limite di token | L'utente può usare fino al numero dato di token per periodo. |
| Non disponibile | L'utente non può usare il provider; qualsiasi cosa invii una richiesta a esso fallisce. |

Un **limite di token** ha un **reset** (giornaliero, settimanale a partire dal lunedì, o mensile; periodi di calendario nel fuso orario del server) e tre limiti che possono essere lasciati vuoti per nessun limite: i token **sent**, i token **generated** e i token complessivi (**in total**). Il primo limite raggiunto blocca l'utente fino al reset successivo. Se sono impostati tutti e tre, il limite di **sent** più quello di **generated** non deve essere inferiore al totale (altrimenti il totale non sarebbe mai raggiungibile), altrimenti la finestra di dialogo rifiuta di salvare. Ad esempio, un totale di 100.000 con 100.000 di **sent** e 20.000 di **generated** va bene; un totale di 100.000 con 20.000 di **sent** e 10.000 di **generated** no.

![Aggiunta di un limite](../images/provider-limit-edit.png)

Nulla viene azzerato con un timer: i token usati ripartono da zero quando l'utente usa il provider (o consulta i propri limiti) per la prima volta in un nuovo periodo. La dimensione di una richiesta è nota prima dell'invio, quindi una richiesta il cui prompt porterebbe l'utente oltre il limite **sent** o **in total** viene rifiutata (il prompt è misurato con la stima interna di Marginalia, che può differire leggermente dal conteggio del servizio). I token **generated** non sono noti in anticipo: vengono conteggiati alla fine della richiesta, quindi l'utente viene bloccato solo quando il limite è raggiunto, e l'ultima risposta può superarlo. Gli utenti senza un limite nella tabella non sono limitati.

Per evitare che i token generati superino il limite, seleziona **Hard limit** (disponibile solo con un limite di token generati). Una richiesta riserva spazio per la risposta più grande consentita (la **Max Response Size** del provider, o la più bassa tra quella del provider e quella del protocollo), e con un hard limit viene rifiutata quando tale spazio non entra in ciò che resta del limite di **generated**. Quindi un hard limit inferiore alla dimensione di risposta del provider rifiuta ogni richiesta; la tabella e le informazioni dell'utente mostrano *rigido* accanto a un limite di questo tipo. L'hard limit riguarda solo i token generati: il limite **in total** può comunque essere superato dall'ultima risposta.

Ogni utente vede i provider condivisi nella propria scheda come righe in sola lettura. Il pulsante **info** (il cerchio con una *i*) di un provider condiviso mostra il suo limite personale: quanto ha usato in questo periodo, i limiti e quando si azzerano. Quando un limite è raggiunto, l'editor della storia, i riassunti e le estensioni mostrano un errore invece di inviare la richiesta.

![I limiti visti da un utente, con ciò che ha usato in questo periodo](../images/provider-limit-info.png)

<a id="usage"></a>

## Utilizzo

La scheda **Provider di inferenza** mostra, per ogni provider, i **Token inviati** (il prompt, "prefill") e i **Token generati** (la risposta, ragionamento incluso) che hai usato con esso, sommati su tutti i tuoi libri, riassunti ed estensioni. Un provider condiviso ha un conteggio proprio per ogni utente, così vedi solo la tua parte.

Anche gli amministratori hanno un pulsante **Utilizzo per utente** nella scheda, che apre una tabella con ciò che ogni utente ha usato di ogni provider (token inviati, token generati, richieste e ora dell'ultima richiesta).

![Utilizzo per utente](../images/provider-usage.png)

I numeri sono quelli riportati dal servizio. Un servizio che non li riporta (o una richiesta che interrompi a metà) viene conteggiato con la stima locale descritta in [Conteggio dei token](#token-counting), quindi quei numeri sono approssimativi. Una richiesta che fallisce prima che arrivi del testo non viene conteggiata. A meno che un amministratore non abbia impostato per te un [limite](#limits-of-shared-providers) su un provider condiviso, i conteggi sono solo informativi.

!!!
Marginalia chiede al servizio di riportare i conteggi dei token (`stream_options` con `include_usage`). Per cambiarlo per un provider, inserisci il tuo `stream_options` in **Parametri aggiuntivi**, ad esempio `{"stream_options": {"include_usage": false}}`: sostituisce ciò che Marginalia invia, e viene usata la stima locale.
!!!

<a id="using-a-provider-in-a-book"></a>

## Usare un provider in un libro

Ogni libro ha il suo modello nel campo **Modello** della scheda **Informazioni**. Per usare un provider per tutti i nuovi libri, selezionalo come **Modello predefinito** nella scheda **Impostazioni**. Cambiare il provider di un libro ha effetto con la prossima parte generata; le parti scritte prima restano invariate.

<a id="troubleshooting"></a>

## Risoluzione dei problemi

| Problema | Cosa controllare |
|---|---|
| *Failed to download model list* | L'URL termina con `/v1`, la chiave API, il server è in esecuzione e raggiungibile da Marginalia. Scrivi l'ID del modello se il servizio non ha un elenco di modelli. |
| *The provider did not answer in time.* | Aumenta **Timeout (secondi)** del provider; i modelli di ragionamento possono richiedere minuti. |
| *The provider is limiting requests.* | Attendi, oppure aumenta **Tentativi**. |
| *Al libro manca il modello.* | Il libro non ha un provider. Selezionalo in **Informazioni → Modello**. |
| *Limite di contesto insufficiente.* | Il prompt senza alcuna storia non entra nel contesto meno i token di risposta. Aumenta **Dimensione massima del contesto** (o **Token massimi di contesto** del protocollo), abbassa **Token massimi di risposta**, oppure accorcia i template e le voci del lorebook. |
| Il testo si interrompe a metà | **Token massimi di risposta** (o **Max Reply Tokens** del protocollo) è troppo basso, soprattutto per i modelli di ragionamento. |
| Errori dell'API con il ragionamento attivo | L'API non supporta `reasoning_effort`. Disattiva **Abilita il ragionamento**. |
