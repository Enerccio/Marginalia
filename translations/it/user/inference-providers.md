---
label: Provider di inferenza
order: 980
icon: server
---

# Provider di inferenza

Un provider di inferenza è una connessione a un modello: l'indirizzo dell'API, la chiave API, il modello e i suoi limiti. Ogni
libro usa un provider. Puoi averne quanti ne vuoi, ad esempio un grande modello in cloud per la storia e uno piccolo
locale per le prove, e passare un libro dall'uno all'altro in qualsiasi momento.

I provider appartengono al tuo account. Gli altri utenti non li vedono né vedono le loro chiavi API. Le chiavi API sono
memorizzate in forma cifrata (vedi [La cartella dei dati](administration/index.md#the-data-folder)).

Per aggiungere un provider serve il permesso **Crea modelli propri**, che ti assegna un amministratore (gli amministratori
lo hanno sempre, vedi [Permessi](administration/permissions.md)). Senza di esso puoi usare solo i provider che gli
amministratori ti [condividono](#shared-providers); il pulsante **Aggiungi provider di inferenza** non viene mostrato.

![La scheda Provider di inferenza](../images/inference-providers-list.png)

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
Con la [versione Docker](getting-started/docker-server.md), `localhost` indica il contenitore di Marginalia. Per un server
di modelli sulla stessa macchina usa `http://host.docker.internal:<porta>/v1`, vedi
[Modelli sulla stessa macchina](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Aggiungere un provider

Apri la scheda **Provider di inferenza** e clicca **Aggiungi provider di inferenza**. I provider esistenti si modificano con il pulsante a forma di matita e si eliminano con il pulsante del cestino. Il pulsante di clonazione (due fogli) crea una copia di un provider con un nome che confermi (quello dell'originale con *(Copy)*): la copia ha lo stesso indirizzo, modello, **Chiave API** (o le impostazioni dell'estensione), ragionamento, limiti di token e parametri, quindi un secondo modello sullo stesso servizio richiede un nome e un clic. La copia non è condivisa e non ha limiti o utilizzo propri. Puoi clonare solo i tuoi provider (non uno condiviso di un amministratore, di cui non devi vedere la chiave) e solo quando sei autorizzato a creare provider.

In cima alla finestra di dialogo:

| Campo | |
|---|---|
| **Nome** | Il tuo nome per il provider, mostrato quando scegli un modello per un libro. Includi il modello, ad esempio `OpenRouter - Claude Sonnet`. |
| **Tipo** | Il tipo di API: **Compatibile con OpenAI**, oppure **Estensione** per un provider che porta un'[estensione](extensions/index.md) (è offerto solo mentre tale estensione è caricata, ad esempio [Google Gemini](extensions/google-genai.md) o [Anthropic Claude](extensions/anthropic.md)). Il tipo non può essere cambiato dopo la creazione del provider. |
| **Provider** | Solo per il tipo **Estensione**: di quale estensione è questo provider. Le sue impostazioni riempiono la scheda *Impostazioni per tipo*. |

<a id="per-type-settings"></a>

### Impostazioni per tipo

![Impostazioni per tipo con l'elenco dei modelli caricato](../images/inference-provider.png)

La scheda è più alta della finestra di dialogo e scorre. I campi in basso:

![Timeout, tentativi, parametri aggiuntivi e Prova la connessione](../images/inference-provider-2.png)

| Campo | |
|---|---|
| **URL OpenAI (termina con /v1)** | L'indirizzo base dell'API, vedi [la tabella sopra](#supported-apis). Senza `/chat/completions`. |
| **Chiave API** | La chiave del servizio. Un server locale di solito non la verifica: lascia il campo vuoto e Marginalia invia il segnaposto `none`. |
| **Modello** | Il modello da usare. Clicca **Aggiorna** per caricare l'elenco dei modelli dall'API, oppure digita tu l'ID del modello (ad esempio `anthropic/claude-sonnet-4.5` su OpenRouter). |
| **Timeout (secondi)** | Quanto attendere la risposta dell'API e altro testo una volta iniziata la scrittura. Il valore predefinito è 300. I modelli di ragionamento possono impiegare minuti prima del primo testo; una richiesta che non riceve nulla per questo tempo fallisce con un messaggio di timeout (il ragionamento di un modello conta come testo, quindi un modello che continua a ragionare non viene interrotto). Una modifica viene usata dalla richiesta successiva, anche di un agente che sta lavorando. |
| **Tentativi** | Quante volte una richiesta viene ripetuta quando l'API risponde con un limite di frequenza (429) o un errore del server (5xx), oppure non può essere raggiunta. L'attesa tra i tentativi cresce e viene rispettato l'header `Retry-After` dell'API. Il valore predefinito è 2; 0 disattiva i tentativi. Viene ripetuto solo l'inizio di una richiesta: il testo già arrivato non viene mai scritto due volte. |
| **Tokenizer** | Come Marginalia conta i token dei testi per questo provider. **Automatico** (il predefinito) prova uno dopo l'altro gli endpoint tokenizer del server (OpenAI, llama.cpp, LiteLLM) e usa il primo che risponde, e una stima locale se nessuno risponde. Scegline uno per usare solo quello, oppure **LOCAL_TOKKIT** per la stima locale che non chiama mai il server (utile quando contare farebbe caricare al server un altro modello). Un tokenizer di un plugin compare anche qui; riceve l'indirizzo, il modello e il testo, mai la chiave API, e solo quando lo scegli. **Prova il tokenizer** conta un testo di esempio con quello che il provider proverebbe e mostra cosa ha risposto ciascuno. Quando crei un provider, un tokenizer di plugin adatto all'indirizzo viene preselezionato finché non scegli tu. |
| **Rinvia il ragionamento** | Se il modello riceve indietro il proprio ragionamento nella richiesta successiva, vedi [Rinvia il ragionamento](#send-the-thinking-back). Solo per i provider compatibili con OpenAI: i provider Anthropic e Google inviano da soli ciò che le loro API richiedono. |
| **Parametri aggiuntivi** | Un oggetto JSON con campi extra per ogni richiesta a questo provider, ad esempio `{"top_k": 40, "min_p": 0.05}` per un server locale, o `{"provider": {"order": ["anthropic"]}}` per OpenRouter. Un campo impostato qui prevale sulla stessa impostazione del [protocollo](protocols.md) (ad esempio `temperature`) o sul limite di risposta (`max_completion_tokens`). Deve essere un oggetto JSON valido oppure vuoto. |

Quando modifichi un provider esistente, la chiave API non viene mostrata di nuovo. Resta com'è, a meno che tu non clicchi **Reimposta la chiave API** e ne inserisca una nuova.

**Aggiorna** chiede all'API il suo elenco di modelli (`/v1/models`) con l'URL e la chiave del modulo. Se fallisce con *Failed to download model list*, controlla l'URL (deve terminare con `/v1`), la chiave e che il server sia in esecuzione. Alcuni servizi non elencano i modelli; in tal caso digita l'ID del modello.

**Prova la connessione** verifica i valori del modulo senza salvarli: chiede all'API il suo elenco di modelli e poi una risposta di un solo token dal **Modello** selezionato, senza tentativi. Un messaggio di successo significa che URL, chiave e modello funzionano insieme. Un errore indica la causa (vedi [Messaggi di errore](#error-messages)). Alcuni servizi non hanno un elenco di modelli; in quel caso il test richiede solo la risposta di un token.

<a id="general-settings"></a>

### Impostazioni generali

![Impostazioni generali di un provider di inferenza](../images/inference-provider-general.png)

| Campo | |
|---|---|
| **Dimensione massima del contesto** | Obbligatorio. Quanti token il modello accetta in totale, prompt e risposta insieme. |
| **Token massimi di risposta** | Obbligatorio. La risposta più lunga che il modello può scrivere. Viene inviato all'API come `max_completion_tokens`, e questa quantità del contesto resta libera per la risposta. Deve essere inferiore a *Dimensione massima del contesto*. |
| **Connessioni** | Quante richieste possono essere in corso verso questo modello contemporaneamente: tutte quante, di ogni utente e di ogni tipo (la tua generazione, una richiesta secondaria, una traduzione, un'annotazione, [agenti](agents.md#one-model-several-agents), plugin). Le altre aspettano il loro turno, nell'ordine di arrivo; una generazione che deve attendere mostra *In attesa del modello: N richieste prima di questa*, e **Interrompi** la toglie dalla coda. **1** (il predefinito) per un server locale con un solo slot (una GPU); un modello in cloud che risponde a più richieste contemporaneamente può averne di più, così una richiesta secondaria può girare accanto a una generazione. |
| **Invia le immagini del lore** | Invia le [immagini delle voci del lorebook](lorebooks/lorebooks.md#editing-in-full-screen) che un turno usa insieme al loro testo. Disattivato per impostazione predefinita: attivalo solo per un modello che accetta immagini, perché un modello senza visione rifiuta la richiesta e il turno fallisce. Le immagini delle voci nel blocco del lore vanno in un messaggio a sé subito dopo il prompt di sistema (con una breve risposta del modello), le altre insieme al tuo prompt. |
| **Immagini nel messaggio di sistema** | (Compatibile con OpenAI, con *Invia le immagini del lore*) Inserisce le immagini del blocco del lore nel messaggio di sistema stesso, accanto al testo del lore. Solo per un server e un template di chat che le accettano lì; la maggior parte non le accetta, e la richiesta fallisce. |
| **Richiede jailbreak**, **Prompt di jailbreak** | Un prompt extra per i modelli che rifiutano di scrivere alcuni contenuti. Se selezionato, il prompt di jailbreak viene messo all'inizissimo del prompt. |
| **Abilita il ragionamento**, **Impegno di ragionamento** | Per i modelli di ragionamento ("thinking"), vedi [Modelli di ragionamento](#reasoning-models). |

Marginalia riserva ai token di risposta la quantità **Token massimi di risposta** del contesto e riempie il resto con il prompt: template, voci del [lorebook](lorebooks/index.md) e [riassunti](books/summaries.md) vi vengono conteggiati, e le parti più vecchie della storia che non entrano vengono escluse. Con un contesto di 32 000 token e risposte fino a 2 000 token, il prompt ottiene 30 000 token.

Prendi **Dimensione massima del contesto** dalla documentazione del servizio, e impostala un po' più bassa: i conteggi dei token non sono sempre esatti, e una richiesta che supera il limite reale fallisce. Un [protocollo](protocols.md) può abbassare entrambi i limiti per alcuni libri, ma non alzarli.
<a id="error-messages"></a>

## Messaggi di errore

Quando l'API rifiuta una richiesta, Marginalia indica cosa correggere invece di mostrare un errore interno, seguito da
quanto ha detto l'API stessa:

![Un errore del provider mostrato all'utente](../images/error-dialog.png)

| Messaggio | Causa |
|---|---|
| *Il provider ha rifiutato la chiave API.* | **Chiave API** errata, scaduta o mancante. |
| *La chiave API non è autorizzata a usare questo modello o questo endpoint.* | La chiave è valida ma non ha accesso al modello. |
| *Il provider non conosce il modello configurato.* | **Modello** errato; usa **Aggiorna** per vedere i nomi che l'API conosce. |
| *Non è stato trovato nulla all'URL configurato.* | **OpenAI URL** errato; di solito termina con `/v1`. |
| *Il prompt non entra nel contesto del modello.* | **Dimensione massima del contesto** è superiore a quella che il modello accetta davvero. Abbassala. |
| *Il provider sta limitando le richieste.* | Limite di frequenza o quota esaurita, anche dopo i tentativi. |
| *Il provider non è riuscito a elaborare la richiesta.* | Il servizio ha un problema (5xx), anche dopo i tentativi. |
| *Il provider non ha risposto in tempo.* | Aumenta **Timeout (secondi)**, oppure controlla il server. |
| *Impossibile connettersi al provider.* | Il server non è in esecuzione o non è raggiungibile da Marginalia, oppure l'URL contiene un errore di battitura. |
| *Il provider ha rifiutato la richiesta.* | Altro rifiuto, spesso un campo non supportato in **Parametri aggiuntivi** oppure un ragionamento che il modello non supporta. |

<a id="warning-before-sending"></a>

## Avviso prima dell'invio

Marginalia riempie il contesto solo fino a **Dimensione massima del contesto** meno **Token massimi di risposta**. Se il
prompt finale è ancora più grande (ad esempio perché un'estensione ha aggiunto testo, o perché il conteggio dei token
era impreciso), prima dell'invio della richiesta compare un avviso con le dimensioni stimate. La richiesta viene inviata
comunque; l'API potrebbe rifiutarla o troncare il prompt.

![L'avviso su un prompt che potrebbe non entrare](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modelli di ragionamento

I modelli di ragionamento riflettono prima di rispondere. Con **Abilita il ragionamento** selezionato, Marginalia invia
l'**Impegno di ragionamento** scelto (*Nessuno*, *Basso*, *Medio* o *Alto*) all'API come `reasoning_effort`. Più impegno
di solito significa testo meglio pianificato, ma risposte più lente e più costose. Non tutte le API supportano il
parametro; se le richieste falliscono con il ragionamento attivo, disattivalo.

Quando il modello restituisce il proprio ragionamento (come `reasoning_content` o `reasoning`, a seconda del server), esso
viene mostrato sopra la parte generata, sotto **Mostra il ragionamento**. Il ragionamento non fa parte della storia e
non viene rinviato al modello, a meno che tu non attivi **Rinvia il ragionamento** (vedi sotto).

<a id="send-the-thinking-back"></a>

### Rinvia il ragionamento

**Rinvia il ragionamento** (in *Impostazioni per tipo* di un provider compatibile con OpenAI) stabilisce se il
ragionamento del modello gli viene restituito, come `reasoning_content` del suo messaggio precedente, nella richiesta
successiva:

| Valore | |
|---|---|
| **Nessuno** (predefinito) | Mai. Adatto alla maggior parte dei modelli e dei server, e alle API che rifiutano un campo sconosciuto. |
| **Tra le chiamate agli strumenti** | Solo finché proseguono le chiamate agli strumenti di una stessa risposta: il modello che ha chiamato uno strumento riceve indietro il proprio ragionamento insieme al risultato. Un nuovo messaggio tuo inizia senza il vecchio ragionamento. È ciò che Google richiede per Gemma 4 (*thoughts must not be removed between the function calls*, ma rimossi tra un turno e l'altro). |
| **Sempre** | Anche con le risposte precedenti che hanno un ragionamento, quando la chat lo conserva (la procedura guidata della trama dell'Arc Outline lo fa). Un server il cui template elimina da solo il vecchio ragionamento, come quello di Gemma 4, lo ignora. |

Si applica al ciclo degli strumenti (la procedura guidata della trama, i generatori che usano gli strumenti) e agli agenti:
un agente riceve indietro il ragionamento dei suoi turni precedenti finché proseguono le chiamate agli strumenti di un
turno (il ragionamento viene conservato con il messaggio nella console, non nel messaggio memorizzato). Il server deve
accettare il campo: llama.cpp lo accetta (legge `reasoning_content`, non `reasoning`), anche dietro LiteLLM; un provider
che rifiuta i campi sconosciuti dei messaggi fallisce, quindi lascialo su *Nessuno*.

![Ragionamento di una parte generata](../images/reasoning-view.png)

Il ragionamento conta nella risposta: assegna ai modelli di ragionamento un **Token massimi di risposta** più alto,
altrimenti il testo può essere troncato dopo un ragionamento lungo.

<a id="token-counting"></a>

## Conteggio dei token

Marginalia conta i token per decidere quanta parte della storia entra nel prompt. Quando può, lo chiede al provider, e
prova questi metodi in ordine:

1. l'endpoint di conteggio dei token di OpenAI (`/v1/responses/input_tokens`),
2. l'endpoint `/tokenize` di llama.cpp,
3. l'endpoint `/tokenize` di LiteLLM,
4. l'endpoint `/v1/messages/count_tokens` di LiteLLM (per i modelli Anthropic),
5. una stima locale con il tokenizer di OpenAI.

Da quel momento viene usato il primo metodo che funziona. La stima locale è vicina per la maggior parte dei modelli ma
non esatta, ed è un altro motivo per tenere **Dimensione massima del contesto** un po' sotto il limite reale. I provider
di tipo *Estensione* contano come dice la loro estensione; quelli di [Google Gemini](extensions/google-genai.md) e
[Anthropic Claude](extensions/anthropic.md) chiedono al provider di contare.

<a id="prompt-cache"></a>

## Cache del prompt

Ogni richiesta che Marginalia fa per un tipo di lavoro (una nuova parte, un'annotazione, una riscrittura, un riassunto...)
porta l'intestazione `X-Session-ID`, un hash uguale per lo stesso utente e lo stesso tipo. Un server o un proxy che
conserva una cache del prompt può usarlo per trovare la cache della richiesta precedente di quel tipo. Un server che non
lo conosce lo ignora. Vedi [Cache del prompt e session id](prompt-cache.md), con un esempio per uno slot di llama.cpp
salvato su disco.

<a id="shared-providers"></a>

## Provider condivisi

In un'installazione con più utenti, un amministratore può **condividere** un provider: seleziona **Condiviso con tutti
gli utenti** nella finestra di dialogo del provider (la casella è visibile solo agli amministratori). Ogni utente lo
trova poi nell'elenco **Modello** dei suoi libri e nella scheda **Provider di inferenza**, contrassegnato come *Condiviso*
**Sì**. Possono usarlo, ma non aprirlo, vederne l'indirizzo o la chiave API, modificarlo o eliminarlo; lo può fare solo
il suo proprietario. I libri, i backup e i ripristini che fanno riferimento a un provider condiviso continuano a
funzionare per chiunque possa vederlo.

![Un provider condiviso nella scheda Provider di inferenza di un utente normale](../images/provider-shared-user.png)

Condividere un provider permette agli altri utenti di spendere la quota del proprietario presso il servizio. Per
impedire agli utenti di aggiungere provider propri, così che possano usare solo quelli condivisi, vedi
[Utenti e provider](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Limiti dei provider condivisi

Un amministratore può limitare cosa ogni utente può fare con un provider condiviso. Nella scheda **Provider di inferenza**, l'amministratore che possiede un provider condiviso ha un pulsante **Limiti** (un utente con un lucchetto) nella sua riga, che apre una tabella con i limiti impostati. **Aggiungi limite** ne crea uno nuovo: si sceglie l'utente e un tipo di limite.

![I limiti di un provider condiviso](../images/provider-limits.png)

| Tipo di limite | Significato |
|---|---|
| Illimitato | Nessun limite (equivale a non averne). |
| Limite di token | L'utente può usare fino al numero indicato di token per periodo. |
| Non disponibile | L'utente non può usare il provider; qualsiasi richiesta che lo invierebbe fallisce. |

Un **limite di token** ha un **reset** (giornaliero, settimanale a partire dal lunedì, o mensile; periodi di calendario nel fuso orario del server) e tre limiti che possono essere lasciati vuoti per nessun limite: i token **inviati**, i token **generati** e i due insieme (**in totale**). Quello raggiunto per primo blocca l'utente fino al reset successivo. Se sono impostati tutti e tre, il limite di inviati più generati non deve essere inferiore al totale (il totale non sarebbe altrimenti mai raggiungibile), altrimenti la finestra di dialogo rifiuta il salvataggio. Ad esempio, un totale di 100.000 con 100.000 inviati e 20.000 generati è corretto; un totale di 100.000 con 20.000 inviati e 10.000 generati non lo è.

![Aggiunta di un limite](../images/provider-limit-edit.png)

Nulla viene azzerato con un timer: i token usati ripartono da zero quando l'utente usa il provider (o consulta i propri limiti) per la prima volta in un nuovo periodo. La dimensione di una richiesta è nota prima dell'invio, quindi una richiesta il cui prompt porterebbe l'utente oltre il limite **inviati** o **in totale** viene rifiutata (il prompt è misurato con la stima interna di Marginalia, che può differire leggermente dal conteggio del servizio). I token **generati** non sono noti in anticipo: vengono conteggiati alla fine della richiesta, quindi l'utente viene bloccato solo quando il limite è stato raggiunto e l'ultima risposta può superarlo. Gli utenti senza un limite nella tabella non sono limitati.

Per evitare che i token generati superino il limite, selezionare **Limite rigido** (disponibile solo con un limite di token generati). Una richiesta riserva spazio per la risposta più lunga che consente (la **Dimensione massima della risposta** del provider, o la più bassa tra questa e quella del protocollo), e con un limite rigido viene rifiutata quando tale riserva non entra in ciò che resta del limite di generati. Quindi un limite rigido inferiore alla dimensione di risposta del provider rifiuta ogni richiesta; la tabella e le informazioni dell'utente mostrano *rigido* accanto a un limite di questo tipo. Il limite rigido riguarda solo i token generati: il limite **in totale** può comunque essere superato dall'ultima risposta.

Ogni utente vede i provider condivisi nella propria scheda come righe in sola lettura. Il pulsante **info** (il cerchio con una *i*) di un provider condiviso mostra il proprio limite: quanto ha usato in questo periodo, i limiti e quando si azzerano. Quando un limite viene raggiunto, l'editor della storia, i riassunti e le estensioni mostrano un errore invece di inviare la richiesta.

![I limiti visti da un utente, con quanto usato in questo periodo](../images/provider-limit-info.png)

<a id="usage"></a>

## Utilizzo

La scheda **Provider di inferenza** mostra, per ogni provider, i **Token inviati** (il prompt, "prefill") e i **Token generati** (la risposta, ragionamento incluso) che hai usato con esso, sommati su tutti i tuoi libri, riassunti ed estensioni. Un provider condiviso ha un conteggio proprio per ciascun utente, quindi vedi solo la tua parte.

Gli amministratori dispongono anche di un pulsante **Utilizzo per utente** nella scheda, che apre una tabella con quanto ogni utente ha usato di ogni provider (token inviati, token generati, richieste e l'orario dell'ultima).

![Utilizzo per utente](../images/provider-usage.png)

I numeri sono quelli comunicati dal servizio. Un servizio che non li comunica (o una richiesta che interrompi a metà) viene conteggiato con la stima locale descritta in [Conteggio dei token](#token-counting), quindi questi numeri sono approssimativi. Una richiesta che fallisce prima che arrivi del testo non viene conteggiata. A meno che un amministratore non abbia impostato per te un [limite](#limits-of-shared-providers) su un provider condiviso, i conteggi sono solo informativi.

!!!
Marginalia chiede al servizio di comunicare i conteggi dei token (`stream_options` con `include_usage`). Per cambiare questo comportamento per un provider, inserisci i tuoi `stream_options` nei suoi **Parametri aggiuntivi**, ad esempio `{"stream_options": {"include_usage": false}}`: sostituiscono ciò che Marginalia invia, e viene usata la stima locale.
!!!

<a id="using-a-provider-in-a-book"></a>

## Usare un provider in un libro

Ogni libro ha il proprio modello nel campo **Modello** della sua scheda **Informazioni**. Per usare un provider per tutti i nuovi libri, selezionalo come **Modello predefinito** nella scheda **Impostazioni**. Cambiare il provider di un libro ha effetto con la prossima parte generata; le parti scritte in precedenza restano invariate.

<a id="troubleshooting"></a>

## Risoluzione dei problemi

| Problema | Cosa controllare |
|---|---|
| *Impossibile scaricare l'elenco dei modelli* | L'URL termina con `/v1`, la chiave API, il server è in esecuzione e raggiungibile da Marginalia. Scrivi l'ID del modello se il servizio non ha un elenco di modelli. |
| *Il provider non ha risposto in tempo.* | Aumenta il **Timeout (secondi)** del provider; i modelli con ragionamento possono richiedere minuti. |
| *Il provider sta limitando le richieste.* | Attendi, oppure aumenta i **Tentativi**. |
| *Al libro manca il modello.* | Il libro non ha un provider. Selezionane uno in **Informazioni → Modello**. |
| *Limite di contesto insufficiente.* | Il prompt senza alcuna storia non entra nel contesto meno i token della risposta. Aumenta la **Dimensione massima del contesto** (o i **Token massimi di contesto** del protocollo), abbassa i **Token massimi di risposta**, oppure accorcia i template e le voci del lorebook. |
| Il testo si interrompe a metà | I **Token massimi di risposta** (o i **Token massimi di risposta** del protocollo) sono troppo bassi, soprattutto per i modelli con ragionamento. |
| Errori dell'API con il ragionamento attivo | L'API non supporta `reasoning_effort`. Disattiva **Abilita il ragionamento**. |
