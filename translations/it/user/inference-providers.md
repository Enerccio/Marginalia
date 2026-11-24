---
label: Fornitori di inferenza
order: 980
icon: server
---

# Fornitori di inferenza

Un fornitore di inferenza è una connessione a un modello: l'indirizzo API, la chiave API, il modello e i suoi limiti. Ogni
libro usa un fornitore. Puoi averne quanti vuoi, per esempio un modello grande in cloud per la storia e uno piccolo
locale per gli esperimenti, e passare un libro dall'uno all'altro in qualsiasi momento.

I fornitori appartengono al tuo account. Gli altri utenti non vedono né i fornitori né le loro chiavi API. Le chiavi API sono
memorizzate cifrate (vedi [La cartella dei dati](administration/index.md#the-data-folder)).

Aggiungere un fornitore richiede il permesso **Create own models**, che ti concede un amministratore (gli amministratori
lo hanno sempre, vedi [Permessi](administration/permissions.md)). Senza di esso puoi usare solo i fornitori che gli amministratori
[condividono](#shared-providers) con te; il pulsante **Add Inference Provider** non viene mostrato.

![La scheda Inference Providers](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## API supportate

Marginalia funziona con qualsiasi API che implementi l'interfaccia **OpenAI Chat Completions** (`/v1/chat/completions`,
con streaming). La maggior parte dei servizi e dei server locali lo fa:

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
Con la [versione Docker](getting-started/docker-server.md), `localhost` indica il container di Marginalia. Usa
`http://host.docker.internal:<port>/v1` per un server di modelli sulla stessa macchina, vedi
[Modelli sulla stessa macchina](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Aggiungere un provider

Apri la scheda **Inference Providers** e clicca su **Add Inference Provider**. I provider esistenti si modificano con il
pulsante a forma di matita e si eliminano con il pulsante del cestino. Il pulsante di clonazione (due fogli) crea una copia di un provider con un nome che confermi (quello dell'originale con *(Copy)*): la copia ha lo stesso indirizzo, modello, **API key** (o le impostazioni dell'estensione), ragionamento, limiti di token e parametri, quindi un secondo modello sullo stesso servizio richiede un nome e un clic. La copia non è condivisa e non ha limiti né utilizzo propri. Puoi clonare solo i tuoi provider (non uno condiviso da un amministratore, la cui chiave non devi poter vedere) e solo se ti è consentito creare provider.

In cima alla finestra di dialogo:

| Campo | |
|---|---|
| **Name** | Il tuo nome per il provider, mostrato quando scegli un modello per un libro. Includi il modello, ad esempio `OpenRouter - Claude Sonnet`. |
| **Type** | Il tipo di API: **OpenAI Compatible**, oppure **Extension** per un provider fornito da un [estensione](extensions/index.md) (è offerto solo quando tale estensione è caricata, ad esempio [Google Gemini](extensions/google-genai.md) o [Anthropic Claude](extensions/anthropic.md)). Il tipo non può essere cambiato dopo la creazione del provider. |
| **Provider** | Solo per il tipo **Extension**: quale provider dell'estensione è questo. Le sue impostazioni riempiono la scheda *Per Type Settings*. |

### Per Type Settings

![Per Type Settings con l'elenco dei modelli caricato](../images/inference-provider.png)

La scheda è più alta della finestra di dialogo e scorre. I campi inferiori:

![Timeout, retry, parametri aggiuntivi e Test Connection](../images/inference-provider-2.png)

| Campo | |
|---|---|
| **OpenAI URL (ends with /v1)** | L'indirizzo base dell'API, vedi [la tabella sopra](#supported-apis). Senza `/chat/completions`. |
| **Api Key** | La chiave del servizio. Un server locale di solito non la verifica: lascia il campo vuoto e Marginalia invia il segnaposto `none`. |
| **Model** | Il modello da usare. Clicca su **Refresh** per caricare l'elenco dei modelli dall'API, oppure digita tu l'ID del modello (ad esempio `anthropic/claude-sonnet-4.5` su OpenRouter). |
| **Timeout (seconds)** | Quanto attendere la risposta dell'API e, una volta iniziata la scrittura, altro testo. Il valore predefinito è 300. I modelli di ragionamento possono impiegare minuti prima del primo testo; una richiesta che non riceve nulla per questo tempo fallisce con un messaggio di timeout (il ragionamento di un modello conta come testo, quindi un modello che continua a ragionare non viene interrotto). Una modifica viene usata dalla richiesta successiva, anche di un agente che sta lavorando. |
| **Retries** | Quante volte una richiesta viene ripetuta quando l'API risponde con un limite di frequenza (429) o un errore del server (5xx), oppure non può essere raggiunta. L'attesa tra i tentativi cresce e viene rispettato un header `Retry-After` dell'API. Il valore predefinito è 2; 0 disattiva i nuovi tentativi. Viene ripetuto solo l'avvio di una richiesta: il testo già arrivato non viene mai scritto due volte. |
| **Tokenizer** | Come Marginalia conta i token dei testi per questo provider. **Automatic** (il predefinito) prova gli endpoint del tokenizer del server uno dopo l'altro (OpenAI, llama.cpp, LiteLLM) e usa il primo che risponde, oppure una stima locale se nessuno risponde. Sceglierne uno significa usare solo quello, oppure **LOCAL_TOKKIT** per la stima locale che non chiama mai il server (utile quando il conteggio farebbe caricare al server un altro modello). Anche un tokenizer di un plugin compare qui; riceve l'indirizzo, il modello e il testo, mai la chiave API, e solo se lo scegli. **Test tokenizer** conta un testo di esempio con ciò che il provider proverebbe e mostra la risposta di ciascuno. Quando crei un provider, un tokenizer di plugin adatto all'indirizzo viene preselezionato finché non scegli tu. |
| **Send the thinking back** | Se il modello riceve indietro il proprio ragionamento nella richiesta successiva, vedi [Send the thinking back](#send-the-thinking-back). Solo per i provider compatibili con OpenAI: i provider Anthropic e Google inviano da soli ciò che le loro API richiedono. |
| **Additional Parameters** | Un oggetto JSON con campi extra per ogni richiesta a questo provider, ad esempio `{"top_k": 40, "min_p": 0.05}` per un server locale, o `{"provider": {"order": ["anthropic"]}}` per OpenRouter. Un campo impostato qui prevale sulla stessa impostazione del [protocollo](protocols.md) (ad esempio `temperature`) o sul limite di risposta (`max_completion_tokens`). Deve essere un oggetto JSON valido oppure vuoto. |

Quando modifichi un provider esistente, la chiave API non viene più mostrata. Resta invariata a meno che tu non clicchi
**Reset Api Key** e ne inserisca una nuova.

**Refresh** chiede all'API il suo elenco di modelli (`/v1/models`) con l'URL e la chiave del modulo. Se fallisce con
*Failed to download model list*, controlla l'URL (deve terminare con `/v1`), la chiave e che il server sia in esecuzione.
Alcuni servizi non elencano i modelli; digita invece l'ID del modello.

**Test Connection** verifica i valori del modulo senza salvarli: chiede all'API il suo elenco di modelli e poi una
risposta di un solo token dal **Model** selezionato, senza nuovi tentativi. Un messaggio di successo significa che URL, chiave e modello funzionano insieme. Un errore indica la causa (vedi [Messaggi di errore](#error-messages)). Alcuni servizi non hanno un elenco di modelli; in quel caso il test richiede solo la risposta di un token.

### General Settings

![General Settings di un provider di inferenza](../images/inference-provider-general.png)

| Campo | |
|---|---|
| **Max Context Size** | Obbligatorio. Quanti token il modello accetta in totale, prompt e risposta insieme. |
| **Max Response Tokens** | Obbligatorio. La risposta più lunga che il modello può scrivere. Viene inviata all'API come `max_completion_tokens`, e questa quantità del contesto resta libera per la risposta. Deve essere inferiore a *Max Context Size*. |
| **Connections** | Quante richieste possono essere in corso verso questo modello contemporaneamente: tutte quelle di ogni utente e di ogni tipo (la tua generazione, una query secondaria, una traduzione, un'annotazione, [agenti](agents.md#one-model-several-agents), plugin). Le altre attendono il loro turno, nell'ordine di arrivo; una generazione che deve attendere mostra *Waiting for the model: N requests before this one*, e **Stop** la toglie dalla coda. **1** (il predefinito) per un server locale con un solo slot (una GPU); un modello in cloud che risponde a più richieste contemporaneamente può averne di più, così una query secondaria può girare accanto a una generazione. |
| **GPU lock** | Solo quando l'amministratore ha configurato le [GPU](administration/gpus.md): il lock della GPU su cui gira questo modello, quando la scheda è condivisa con altri modelli o con server di immagini e voce. Ogni richiesta attende quindi il proprio turno sulla GPU (contato con le richieste che la precedono) e, quando la GPU ospita altro, lo libera e carica questo modello con i passaggi del lock prima di tutto. Vuoto: nessun lock. |
| **Multimodal (accepts images)** | Il modello comprende le immagini, quindi le [immagini delle voci del lorebook](lorebooks/lorebooks.md#editing-in-full-screen) usate da un turno vengono inviate insieme al loro testo. Disattivato per impostazione predefinita: attivalo solo per un modello che accetta immagini, perché un modello senza visione rifiuta la richiesta e il turno fallisce. Le immagini delle voci nel blocco del lore vanno in un messaggio a sé subito dopo il prompt di sistema (con una breve risposta del modello), le altre con il tuo prompt. |
| **Image placement** | (con *Multimodal*) Dove vanno le immagini. *In the system message* (solo OpenAI compatible): le immagini del blocco del lore vanno nel messaggio di sistema stesso, accanto al testo del lore; solo per un server e un template di chat che le accettano lì, la maggior parte no, e la richiesta fallisce. *Before the text of the user message*: ogni immagine va prima del testo del proprio messaggio, come richiede Gemma 4. *After the text of the user message* (il predefinito): dopo. Audio e file seguono sempre il testo. |
| **Function definitions** | Dove il modello legge le funzioni che può chiamare (negli [agenti](agents.md), nelle query secondarie e nei plugin che offrono funzioni). *In the system message* (il predefinito): solo come strumenti della richiesta, che il template di chat del modello inserisce nel turno di sistema. *Before* / *After the text of the user message*: anche scritte per esteso all'inizio o alla fine dell'ultimo messaggio utente. Gli strumenti vengono sempre inviati comunque, perché il server ne ha bisogno per riconoscere una chiamata. |
| **Function call rule**, **Rule placement** | Una regola aggiunta a ogni richiesta che offre funzioni, per i modelli che scrivono una chiamata come testo nella risposta invece di farla, oppure che smettono di chiamare. Ad esempio: *You act only through function calls. Never write a call as text in your reply; to use a function, call it.* La posizione indica dove va: il messaggio di sistema, oppure l'inizio o la fine dell'ultimo messaggio utente (il predefinito); Gemma 4 la segue molto meglio nel messaggio utente. Vuoto: nessuna regola. |
| **Needs Jailbreak**, **Jailbreak Prompt** | Un prompt extra per i modelli che rifiutano di scrivere alcuni contenuti. Se selezionato, il prompt di jailbreak viene inserito all'inizissimo del prompt. |
| **Enable Reasoning**, **Reasoning Effort** | Per i modelli di ragionamento ("thinking"), vedi [Modelli di ragionamento](#reasoning-models). |

Marginalia riserva a **Max Response Tokens** la parte del contesto per la risposta e riempie il resto con il prompt:
i template, le voci del [lorebook](lorebooks/index.md) e i [riassunti](books/summaries.md) vengono conteggiati, e le parti più vecchie della storia che non entrano vengono escluse. Con un contesto di 32 000 token e risposte fino a 2 000 token, il prompt ottiene 30 000 token.

Prendi **Max Context Size** dalla documentazione del servizio e impostalo un po' più in basso: i conteggi dei token non sono sempre esatti, e una richiesta che supera il limite reale fallisce. Un [protocollo](protocols.md) può abbassare entrambi i limiti per alcuni libri, ma non alzarli.
<a id="error-messages"></a>

## Messaggi di errore

Quando l'API rifiuta una richiesta, Marginalia indica cosa correggere invece di mostrare un errore interno, seguito da quanto ha risposto l'API stessa:

![Un errore del provider mostrato all'utente](../images/error-dialog.png)

| Messaggio | Causa |
|---|---|
| *Il provider ha rifiutato la chiave API.* | **Api Key** errata, scaduta o mancante. |
| *La chiave API non è autorizzata a usare questo modello o endpoint.* | La chiave è valida ma non ha accesso al modello. |
| *Il provider non conosce il modello configurato.* | **Model** errato; usa **Refresh** per vedere i nomi che l'API conosce. |
| *Non è stato trovato nulla all'URL configurato.* | **OpenAI URL** errato; di solito termina con `/v1`. |
| *Il prompt non entra nel contesto del modello.* | **Max Context Size** è superiore a quanto il modello accetta davvero. Abbassalo. |
| *Il provider sta limitando le richieste.* | Limite di frequenza o quota esaurita, anche dopo i tentativi. |
| *Il provider non è riuscito a elaborare la richiesta.* | Il servizio ha un problema (5xx), anche dopo i tentativi. |
| *Il provider non ha risposto in tempo.* | Aumenta **Timeout (seconds)**, oppure controlla il server. |
| *Impossibile connettersi al provider.* | Il server non è in esecuzione o non è raggiungibile da Marginalia, oppure l'URL contiene un errore di battitura. |
| *Il provider ha rifiutato la richiesta.* | Un altro rifiuto, spesso dovuto a un campo non supportato in **Additional Parameters** o a un ragionamento che il modello non supporta. |

<a id="warning-before-sending"></a>

## Avviso prima dell'invio

Marginalia riempie il contesto solo fino a **Max Context Size** meno **Max Response Tokens**. Se il prompt finito è ancora più grande di così (ad esempio perché un'estensione ha aggiunto testo, o il conteggio dei token era impreciso), prima dell'invio compare un avviso con le dimensioni stimate. La richiesta viene inviata comunque: l'API potrebbe rifiutarla o tagliare il prompt.

![L'avviso su un prompt che potrebbe non entrare](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modelli di ragionamento

I modelli di ragionamento pensano prima di rispondere. Con **Enable Reasoning** selezionato, Marginalia invia all'API la **Reasoning Effort** scelta (*None*, *Low*, *Medium* o *High*) come `reasoning_effort`. Più impegno di solito significa un testo meglio pianificato, ma risposte più lente e più costose. Non tutte le API supportano il parametro; se le richieste falliscono con il ragionamento attivo, disattivalo.

Quando il modello restituisce il proprio ragionamento (come `reasoning_content` o `reasoning`, a seconda del server), questo viene mostrato sopra la parte generata, sotto **View Reasoning**. Il ragionamento non fa parte della storia e non viene rimandato al modello, a meno che tu non imposti **Send the thinking back** (vedi sotto).

<a id="send-the-thinking-back"></a>

### Rimandare indietro il ragionamento

**Send the thinking back** (in *Per Type Settings* di un provider compatibile con OpenAI) stabilisce se il ragionamento del modello gli viene rimandato, come `reasoning_content` del suo messaggio precedente, nella richiesta successiva:

| Valore | |
|---|---|
| **None** (predefinito) | Mai. Adatto alla maggior parte dei modelli e dei server, e alle API che rifiutano un campo sconosciuto. |
| **Between tool calls** | Solo mentre continuano le chiamate agli strumenti di una stessa risposta: il modello che ha chiamato uno strumento riceve indietro il proprio ragionamento insieme al risultato. Un nuovo messaggio dell'utente parte senza il vecchio ragionamento. È ciò che Google richiede per Gemma 4 (*i thoughts non devono essere rimossi tra le chiamate di funzione*, ma vanno rimossi tra un turno e l'altro). |
| **Always** | Anche con le risposte precedenti che hanno del ragionamento, dove la chat lo conserva (lo fa la procedura guidata della trama dell'Arc Outline). Un server il cui template elimina da solo il ragionamento vecchio, come quello di Gemma 4, lo ignora. |

Si applica al ciclo degli strumenti (la procedura guidata della trama, i generatori che usano strumenti) e agli agenti: un agente riceve il ragionamento dei suoi turni precedenti finché continuano le chiamate agli strumenti di un turno (il ragionamento è conservato con il messaggio nella console, non nel messaggio salvato). Il server deve accettare il campo: llama.cpp lo fa (legge `reasoning_content`, non `reasoning`), anche dietro LiteLLM. Un provider che rifiuta campi sconosciuti nei messaggi fallisce: in tal caso lascialo su *None*.

![Ragionamento di una parte generata](../images/reasoning-view.png)

Il ragionamento conta nella risposta: ai modelli di ragionamento assegna un **Max Response Tokens** più alto, altrimenti il testo può essere tagliato dopo un ragionamento lungo.

<a id="token-counting"></a>

## Conteggio dei token

Marginalia conta i token per decidere quanta storia entra nel prompt. Chiede al provider quando può, e prova questi metodi in ordine:

1. l'endpoint di conteggio dei token di OpenAI (`/v1/responses/input_tokens`),
2. l'endpoint `/tokenize` di llama.cpp,
3. l'endpoint `/tokenize` di LiteLLM,
4. l'endpoint `/v1/messages/count_tokens` di LiteLLM (per i modelli Anthropic),
5. una stima locale con il tokenizzatore di OpenAI.

Viene usato da quel momento in poi il primo metodo che funziona. La stima locale è vicina per la maggior parte dei modelli ma non esatta, ed è un altro motivo per tenere **Max Context Size** un po' sotto il limite reale. I provider di tipo *Extension* contano come dice la loro estensione; quelli [Google Gemini](extensions/google-genai.md) e [Anthropic Claude](extensions/anthropic.md) chiedono al provider di contare.

<a id="prompt-cache"></a>

## Cache del prompt

Ogni richiesta che Marginalia fa per un tipo di lavoro (una nuova parte, un'annotazione, una riscrittura, un riassunto...) porta l'intestazione `X-Session-ID`, un hash uguale per lo stesso utente e lo stesso tipo. Un server o un proxy che mantiene una cache dei prompt può usarla per trovare la cache della richiesta precedente di quel tipo. Un server che non la conosce la ignora. Vedi [Cache del prompt e session id](prompt-cache.md), con un esempio per uno slot di llama.cpp salvato su disco.

<a id="shared-providers"></a>

## Provider condivisi

Su un'installazione con più utenti, un amministratore può **condividere** un provider: spunta **Shared with all users** nella finestra di dialogo del provider (la casella è visibile solo agli amministratori). Ogni utente lo trova poi nell'elenco **Model** dei propri libri e nella scheda **Inference Providers**, contrassegnato come *Shared* **Yes**. Può usarlo, ma non può aprirlo, vedere il suo indirizzo o la chiave API, modificarlo o eliminarlo: solo il suo proprietario può farlo. Libri, backup e ripristini che fanno riferimento a un provider condiviso continuano a funzionare per chiunque possa vederlo.

![Un provider condiviso nella scheda Inference Providers di un utente normale](../images/provider-shared-user.png)

Condividere un provider permette agli altri utenti di consumare la quota del proprietario presso il servizio. Per impedire agli utenti di aggiungere provider propri, così che possano usare solo quelli condivisi, vedi [Utenti e provider](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Limiti dei provider condivisi

Un amministratore può limitare ciò che ciascun utente può fare con un provider condiviso. Nella scheda **Inference Providers** l'amministratore proprietario di un provider condiviso ha un pulsante **Limits** (un utente con un lucchetto) nella sua riga, che apre una tabella con i limiti impostati. **Add limit** crea un nuovo limite: si sceglie l'utente e un tipo di limite.

![I limiti di un provider condiviso](../images/provider-limits.png)

| Tipo di limite | Significato |
|---|---|
| Unlimited | Nessun limite (uguale a non averne). |
| Token limit | L'utente può usare fino al numero indicato di token per periodo. |
| Not available | L'utente non può usare il provider; qualsiasi cosa che invii una richiesta al provider fallisce. |

Un **token limit** ha un **reset** (giornaliero, settimanale a partire dal lunedì, o mensile; periodi di calendario nel fuso orario del server) e tre limiti che possono essere lasciati vuoti per nessun limite: i token **sent** (inviati), i token **generated** (generati) e i due insieme (**in total**). Quello raggiunto per primo blocca l'utente fino al reset successivo. Se sono impostati tutti e tre, il limite di invio più quello di generazione non deve essere inferiore al totale (altrimenti il totale non potrebbe mai essere raggiunto), altrimenti la finestra di dialogo rifiuta di salvare. Per esempio, un totale di 100.000 con 100.000 inviati e 20.000 generati è corretto; un totale di 100.000 con 20.000 inviati e 10.000 generati non lo è.

![Aggiunta di un limite](../images/provider-limit-edit.png)

Nulla viene riazzerato con un timer: i token usati ripartono da zero quando l'utente usa il provider (o consulta i propri limiti) per la prima volta in un nuovo periodo. La dimensione di una richiesta è nota prima dell'invio, quindi una richiesta il cui prompt porterebbe l'utente oltre il limite **sent** o **in total** viene rifiutata (il prompt è misurato con la stima interna di Marginalia, che può differire un po' dal conteggio del servizio). I token **generated** non sono noti in anticipo: vengono conteggiati alla fine della richiesta, quindi l'utente viene bloccato solo una volta raggiunto il limite, e l'ultima risposta può superarlo. Gli utenti senza un limite nella tabella non sono limitati.

Per evitare che i token generati superino il limite, seleziona **Hard limit** (disponibile solo con un limite di token generati). Una richiesta riserva spazio per la risposta più grande che consente (il **Max Response Size** del provider, o il più basso tra quello del protocollo), e con un hard limit viene rifiutata quando tale riserva non entra in ciò che resta del limite di generazione. Quindi un hard limit inferiore alla dimensione di risposta del provider rifiuta ogni richiesta; la tabella e le informazioni dell'utente mostrano *hard* accanto a un limite del genere. L'hard limit riguarda solo i token generati: il limite **in total** può comunque essere superato dall'ultima risposta.

Ogni utente vede i provider condivisi nella propria scheda come righe di sola lettura. Il pulsante **info** (il cerchio con una *i*) di un provider condiviso mostra il proprio limite: quanto ha usato in questo periodo, i limiti e quando si resettano. Quando un limite viene raggiunto, l'editor della storia, i riassunti e le estensioni mostrano un errore invece di inviare la richiesta.

![I limiti visti da un utente, con quanto usato in questo periodo](../images/provider-limit-info.png)

<a id="usage"></a>

## Utilizzo

La scheda **Inference Providers** mostra, per provider, i **Tokens sent** (il prompt, "prefill") e i **Tokens generated** (la risposta, ragionamento incluso) che hai usato con esso, sommati su tutti i tuoi libri, riassunti ed estensioni. Un provider condiviso ha un conteggio proprio per ciascun utente, quindi vedi solo la tua parte.

Gli amministratori ottengono anche un pulsante **Usage per user** nella scheda, che apre una tabella con ciò che ogni utente ha usato di ogni provider (token inviati, token generati, richieste e l'ora dell'ultima).

![Utilizzo per utente](../images/provider-usage.png)

I numeri sono quelli riportati dal servizio. Un servizio che non li riporta (o una richiesta che interrompi a metà) viene conteggiato con la stima locale descritta in [Token counting](#token-counting), quindi quei numeri sono approssimativi. Una richiesta che fallisce prima che arrivi del testo non viene conteggiata. A meno che un amministratore non abbia impostato per te un [limite](#limits-of-shared-providers) su un provider condiviso, i conteggi sono solo informativi.

!!!
Marginalia chiede al servizio di riportare i conteggi dei token (`stream_options` con `include_usage`). Per cambiare questo comportamento per un provider, inserisci il tuo `stream_options` in **Additional Parameters**, per esempio `{"stream_options": {"include_usage": false}}`: sostituisce ciò che Marginalia invia, e viene usata la stima locale.
!!!

<a id="using-a-provider-in-a-book"></a>

## Usare un provider in un libro

Ogni libro ha il suo modello nel campo **Model** della sua scheda **About**. Per usare un provider per tutti i nuovi libri, selezionalo come **Default Model** nella scheda **Settings**. Il cambio del provider di un libro ha effetto con la prossima parte generata; le parti scritte prima restano invariate.

<a id="troubleshooting"></a>

## Risoluzione dei problemi

| Problema | Cosa controllare |
|---|---|
| *Failed to download model list* | L'URL termina con `/v1`, la chiave API, il server è in esecuzione e raggiungibile da Marginalia. Digita l'ID del modello se il servizio non ha un elenco di modelli. |
| *The provider did not answer in time.* | Aumenta **Timeout (seconds)** del provider; i modelli di ragionamento possono richiedere minuti. |
| *The provider is limiting requests.* | Attendi, oppure aumenta **Retries**. |
| *Book is missing model.* | Il libro non ha un provider. Selezionane uno in **About → Model**. |
| *Contextual limit not sufficient.* | Il prompt senza alcuna storia non entra nel contesto meno i token di risposta. Aumenta **Max Context Size** (o **Max Context Tokens** del protocollo), riduci **Max Response Tokens**, oppure accorcia i template e le voci del lorebook. |
| Il testo si interrompe a metà | **Max Response Tokens** (o **Max Reply Tokens** del protocollo) è troppo basso, soprattutto per i modelli di ragionamento. |
| Errori dalle API con il ragionamento attivo | L'API non supporta `reasoning_effort`. Disattiva **Enable Reasoning**. |
