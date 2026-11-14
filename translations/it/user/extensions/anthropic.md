---
label: Anthropic Claude
order: 25
---

# Anthropic Claude

Aggiunge i modelli Claude di Anthropic come [provider di inferenza](../inference-providers.md), tramite l'API di Anthropic
invece di una compatibile con OpenAI.

Claude è raggiungibile anche tramite aggregatori come OpenRouter con il normale provider *Compatibile con OpenAI*. Questa estensione
parla direttamente con l'API di Claude (l'SDK di Anthropic), e questo offre cose che un endpoint compatibile non offre: i
**riassunti del ragionamento del modello** mostrati come ragionamento, l'impegno dei modelli Claude 4.6 e successivi e i
conteggi esatti dei token che Anthropic riporta e calcola.

!!!warning Il testo della tua storia va ad Anthropic
Ogni richiesta di un'IA che usa questo provider - il prompt con la storia, le voci del lorebook e i riassunti, le risposte del
modello, i file e le immagini che un agente o un comando invia - viene inviata ai server di Anthropic
(`api.anthropic.com`, oppure il proxy che imposti). Le condizioni d'uso e l'informativa sulla privacy di Anthropic per l'API
stabiliscono cosa succede a questi dati lì (conservazione, misure di sicurezza che possono rifiutare una richiesta). Gli altri
provider inviano al server che hai scelto, che potrebbe essere il tuo. Non usare questo provider per testi che non puoi
inviare ad Anthropic. Su un server condiviso con altri utenti, un amministratore che condivide un'IA di questo provider invia
ad Anthropic il testo della storia di tutti quelli che la usano.
!!!

<a id="installing"></a>

## Installazione

Carica l'estensione da *Admin → Estensioni*, come descritto in [Gestione delle estensioni](../administration/extensions.md).
Il JAR è grande (circa 36 MB): contiene l'SDK di Anthropic e le librerie di cui ha bisogno solo l'SDK. Nessun'altra parte di
Marginalia le usa.

Una volta caricata, la lista **Tipo** della finestra del provider di inferenza ha una nuova voce, **Estensione**, e la lista
**Provider** in cima alla scheda *Impostazioni per tipo* offre **Anthropic Claude** (viene scelto automaticamente finché è
l'unico). Senza un'estensione caricata che fornisce IA, **Estensione** non è disponibile.

<a id="adding-a-claude-provider"></a>

## Aggiungere un provider Claude

1. Ottieni una chiave API nella [Claude Console](https://platform.claude.com).
2. Nella scheda **Provider di inferenza** clicca su **Aggiungi provider di inferenza**, inserisci un **Nome**, scegli il tipo
   **Estensione** e il provider **Anthropic Claude**.
3. Compila le *Impostazioni generali* (dimensione del contesto, dimensione della risposta, [ragionamento](#reasoning)) e le
   impostazioni del provider nella scheda *Impostazioni per tipo*, poi clicca su **Prova la connessione**.

| Campo | |
|---|---|
| **Chiave API** | La chiave della Claude Console. Viene salvata cifrata e non viene più mostrata; quando modifichi il provider, clicca su **Reimposta** per inserirne un'altra. |
| **Modello** | Il modello da usare, ad es. `claude-opus-5-5` o `claude-sonnet-5-5`. **Aggiorna** elenca i modelli che la tua chiave può usare; puoi scrivere un nome che non è nell'elenco. |
| **URL** | Facoltativo. Se vuoto usa l'API di Anthropic (`api.anthropic.com`); impostalo per inviare le richieste tramite un proxy. |
| **Timeout (secondi)** | Quanto attendere la risposta di Anthropic, e altro testo una volta che ha iniziato a scrivere. Il valore predefinito è 300. |
| **Tentativi** | Quante volte una richiesta viene ripetuta quando Anthropic risponde con un limite di frequenza (429) o un errore del server (5xx, anche 529 *overloaded*), oppure quando non può essere raggiunto. Il valore predefinito è 2; 0 disattiva i tentativi. |

**Prova la connessione** elenca i modelli e chiede a quello scelto di generare qualche token, con i valori presenti nella
finestra (prima che tu li salvi).

![Le Impostazioni per tipo del provider Anthropic Claude](../../images/plugin-anthropic.png)

![La lista dei Provider di inferenza con un provider Claude](../../images/plugin-anthropic-list.png)

<a id="reasoning"></a>

## Ragionamento

Le impostazioni del ragionamento sono quelle delle impostazioni generali del provider, come per qualsiasi provider:

- Con **Abilita ragionamento** disattivato non viene inviato nulla riguardo al pensiero. I modelli che pensano per impostazione
  predefinita (Opus 5.5 non può essere istruito a non farlo) pensano, e il loro pensiero non viene mostrato; gli altri non
  pensano.
- Con l'opzione attiva, il modello invia riassunti del suo pensiero, che vengono mostrati come ragionamento della parte di
  storia. Per Claude 4.6 e successivi, **Impegno di ragionamento** è l'impegno del modello (basso, medio, alto) con pensiero
  adattivo: è il modello a decidere quanto pensare. Per i modelli precedenti (Claude 4.5 e precedenti, tra cui Haiku 4.5) è un
  budget di 1 024, 8 192 o 24 576 token di pensiero. **Nessuno** equivale a disattivato.

I token di pensiero contano come token generati e vengono presi dalla dimensione della risposta che imposti: se il modello la
usa tutta per pensare, la parte di storia torna vuota e l'errore segnala che la dimensione della risposta è stata esaurita.
Aumenta **Token massimi di risposta** oppure abbassa l'impegno. Claude richiede una dimensione della risposta in ogni
richiesta: quando né il provider né il protocollo ne impostano una, viene usato 16 000.

<a id="what-works"></a>

## Cosa funziona

Tutto ciò che serve a una storia funziona come con un provider compatibile con OpenAI: lo streaming, l'interruzione di una
generazione, i riassunti, le [estensioni che chiamano il modello](index.md), le funzioni che il modello può chiamare (il
[Generatore di nomi](name-generator.md) ne offre alcune), e le immagini (JPEG, PNG, GIF, WebP), i file PDF e di testo allegati
a un messaggio. I token usati da un utente vengono conteggiati nelle colonne **Token inviati** e **Token generati** a partire
dai numeri che Anthropic riporta (i token letti dalla cache e quelli scritti in essa contano come inviati), e si applicano i
[limiti](../inference-providers.md#limits-of-shared-providers) di un provider condiviso.

I conteggi dei token mostrati nella storia (la dimensione di una parte, il prompt) sono calcolati da Anthropic (la richiesta
`count_tokens` del modello), una volta per ogni testo: le risposte vengono ricordate. Quando Anthropic non può essere
interpellato (nessuna chiave, nessuna rete, un errore), Marginalia stima il conteggio da sé per un minuto e poi riprova; anche
un testo salvato o modificato mentre è in corso un altro salvataggio viene stimato.

Due cose differiscono dagli altri provider:

- **Temperatura, top p e le penalità del protocollo non vengono inviate.** L'API attuale di Claude le rifiuta; il modello
  campiona con le proprie impostazioni. Viene usata la dimensione della risposta del protocollo.
- Una conversazione deve iniziare e finire con un messaggio dell'utente, e Claude non continua una risposta. Il prompt di un
  libro lo fa sempre; una richiesta di un'estensione che non lo fa viene rifiutata con un errore "bad request".

<a id="when-the-extension-is-not-loaded"></a>

## Quando l'estensione non è caricata

I provider che hai creato restano nella lista quando l'estensione viene scaricata, con il tipo *Estensione: anthropic*, e
tornano a funzionare quando l'estensione viene caricata. Finché non è caricata, un libro che usa uno di questi provider non
può generare: l'errore ti invita a caricare l'estensione o a scegliere un altro provider. Il provider può comunque essere aperto
e le sue impostazioni generali modificate; le impostazioni di Claude vengono mostrate solo quando l'estensione è caricata, e
non vengono toccate.

<a id="not-supported"></a>

## Non supportato

- **Amazon Bedrock, Google Vertex AI e Microsoft Foundry.** Sono supportate solo le chiavi API dell'API di Claude.
- La cache dei prompt e gli strumenti eseguiti sui server di Anthropic (ricerca web, esecuzione di codice).
