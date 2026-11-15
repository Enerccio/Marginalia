---
label: GPU
order: 84
---

# GPU

Una scheda grafica spesso contiene un solo modello alla volta: il modello linguistico, oppure il modello di immagini, oppure il modello vocale. La scheda **GPU** della pagina di amministrazione permette ai modelli e ai server di una stessa scheda di alternarsi nel suo utilizzo, così che un'immagine o un audiolibro non inizino mentre il modello linguistico occupa la scheda, e che il modello linguistico torni quando serve.

Lo configuri una volta, come amministratore; poi ogni utente sceglie un blocco per la sua IA o per i suoi backend.

<a id="what-it-is-made-of"></a>

## Di cosa è composta

| | |
|---|---|
| **GPU** | Una scheda (o qualsiasi macchina che contiene un solo modello alla volta). Solo il suo nome. |
| **Blocco GPU** | Un modo di usare una GPU: ciò che è caricato su di essa. Ad esempio *Modello linguistico*, *ComfyUI* e *Voce* sulla stessa GPU. Un blocco ha due elenchi ordinati di passaggi: i passaggi di **acquisizione** lo caricano (avviano un container, caricano un modello, scaricano ciò che deve essere tolto prima), i passaggi di **rilascio** liberano la scheda da esso (fermano il container, scaricano il modello). **Lavori contemporanei** limita quanti lavori del blocco girano insieme (un server di immagini che ne produce una alla volta: 1; 0 per nessun limite). |
| **Passaggio GPU** | Una singola richiesta HTTP: un indirizzo, un metodo, le intestazioni (`Name: value`, una per riga, memorizzate cifrate, così si può scrivere lì una chiave bearer), un corpo, un timeout (vuoto: 60 secondi) e un'attesa dopo la risposta. Un passaggio può essere usato da più blocchi, ad esempio un unico passaggio *Scarica il modello linguistico* che sia il blocco delle immagini sia quello della voce eseguono per primo. **Prova** esegue subito la richiesta. |

Scegli il blocco in:

- le [impostazioni di un'IA](../inference-providers.md) (**Blocco GPU** nelle impostazioni generali), per il modello linguistico;
- un backend di [Generatore di immagini](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- un backend di [Audiolibro](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

La casella **Blocco GPU** compare solo quando esiste almeno un blocco.

<a id="how-the-turns-work"></a>

## Come funzionano i turni

- Ogni richiesta a un modello, ogni immagine e ogni lotto o campione di audiolibro prende il blocco della sua IA o del suo backend **per tutta la propria durata**.
- Quando la GPU è già caricata con quel blocco, il lavoro parte subito e non viene eseguito alcun passaggio.
- Quando è caricata con un altro blocco, il lavoro attende che finiscano i lavori di quel blocco. Poi vengono eseguiti i passaggi di **rilascio** di quel blocco (tutti, anche se uno fallisce), quindi i passaggi di **acquisizione** del nuovo blocco (in ordine; il primo errore li interrompe e il lavoro fallisce con il messaggio del passaggio).
- Quando un passaggio di rilascio fallisce, il nuovo blocco non viene caricato affatto (la scheda potrebbe essere ancora piena): il lavoro fallisce con l'errore e la GPU resta con il blocco che aveva. Il lavoro successivo di un altro blocco proverà di nuovo a liberarla.
- Dopo un lavoro la GPU **resta caricata** con il suo blocco. Nulla viene scaricato con un timer: una finestra di immagini lasciata aperta, ridotta o chiusa non occupa la GPU, e il modello di immagini resta caricato finché il modello linguistico (o un altro blocco) non ha bisogno della scheda.
- I lavori attendono nell'ordine in cui sono arrivati. Una volta che un lavoro attende un altro blocco, i nuovi lavori del blocco caricato attendono dietro di lui, così un modello linguistico molto impegnato non può tenere un'immagine in attesa per sempre.
- Una IA o un backend senza blocco, o con un blocco che è stato eliminato, non attende nulla.

Ciò che una GPU contiene è noto solo mentre l'applicazione è in esecuzione: dopo un riavvio non si sa che nulla sia caricato, e il primo lavoro esegue solo i suoi passaggi di acquisizione.

<a id="the-tab"></a>

## La scheda

La scheda elenca le GPU con il blocco con cui ciascuna è caricata ora (*Sconosciuto* quando non è ancora stato eseguito nulla o è stata scaricata) e quanti lavori girano e attendono su di essa; poi elenca i blocchi e i passaggi, ciascuno con pulsanti per aggiungere, modificare ed eliminare. **Aggiorna** rilegge ciò che le GPU contengono.

**Scarica ora** libera a mano una GPU, ad esempio per usare la scheda per qualcosa al di fuori di Marginalia: attende i lavori in corso, esegue i passaggi di rilascio del blocco caricato e lascia la GPU come sconosciuta (anche quando un passaggio è fallito; il messaggio indica quale). Il lavoro successivo carica di nuovo il suo blocco.

Eliminare una GPU elimina i suoi blocchi; eliminare un passaggio lo toglie dai blocchi che lo eseguono. Le IA e i backend che indicavano un blocco eliminato vengono eseguiti senza blocco.

<a id="an-example-one-card-three-models"></a>

## Un esempio: una scheda, tre modelli

Con `llama-server` dietro LiteLLM e un proxy in stile [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) che avvia e ferma container:

| Passaggio | Richiesta |
|---|---|
| Scarica il modello linguistico | `POST http://litellm:4000/v1/chat/completions` con l'intestazione `X-Llama-Unload: all` (vedi [Liberare la GPU per un altro modello](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Avvia ComfyUI / Ferma ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Avvia voce / Ferma voce | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Blocco | Passaggi di acquisizione | Passaggi di rilascio | Lavori contemporanei |
|---|---|---|---|
| Modello linguistico | (nessuno: il router lo carica con la richiesta successiva) | (nessuno) | 0 |
| ComfyUI | Scarica il modello linguistico, Avvia ComfyUI | Ferma ComfyUI | 1 |
| Voce | Scarica il modello linguistico, Avvia voce | Ferma voce | 1 |

L'IA del modello linguistico sceglie *Modello linguistico*, il backend di immagini *ComfyUI*, il backend vocale *Voce*. Un lotto di voce dopo una chat esegue *Scarica il modello linguistico* e *Avvia voce*; il messaggio successivo in chat esegue prima *Ferma voce*.
