---
label: xAI Grok
order: 26
---

# xAI Grok

Aggiunge i modelli Grok di xAI come [provider di inferenza](../inference-providers.md), tramite l'API di xAI.

Grok è raggiungibile anche con il provider normale *OpenAI Compatible* e l'URL `https://api.x.ai/v1`. Questa estensione dialoga
direttamente con l'API di xAI, e questo permette di mostrare il ragionamento come tale, di avere i conteggi esatti dei token
riportati da xAI (inclusi i token di ragionamento) e un elenco di modelli e un test di connessione pensati per il servizio.

!!!warning Il testo della storia va a xAI
Ogni richiesta di un'AI che usa questo provider (il prompt con la storia, le voci del lorebook e i riassunti, le risposte
del modello, le immagini che un agente o un comando invia) viene inviata ai server di xAI (`api.x.ai`, o al proxy che imposti).
Le condizioni d'uso e l'informativa sulla privacy di xAI per l'API stabiliscono cosa succede poi a quei dati. Gli altri
provider inviano al server che hai scelto, che può essere un server tuo. Non usare questo provider per testi che non puoi
inviare a xAI. Su un server condiviso con altri utenti, un amministratore che condivide un'AI di questo provider invia a xAI
il testo delle storie di tutti coloro che la usano.
!!!

<a id="installing"></a>

## Installazione

Carica l'estensione in *Admin → Extensions*, come descritto in [Gestione delle estensioni](../administration/extensions.md). Una volta caricata,
la lista **Type** della finestra del provider di inferenza ha la voce **Extension**, e la lista **Provider** in cima alla
scheda *Per Type Settings* offre **xAI Grok**.

<a id="adding-a-grok-provider"></a>

## Aggiungere un provider Grok

1. Ottieni una chiave API nella [console xAI](https://console.x.ai).
2. Nella scheda **Inference Providers** clicca **Add Inference Provider**, inserisci un **Name**, scegli il tipo **Extension** e
   il provider **xAI Grok**.
3. Compila le *General Settings* (dimensione del contesto, dimensione della risposta, [ragionamento](#reasoning)) e le impostazioni
   del provider nella scheda *Per Type Settings*, poi clicca **Test Connection**.

| Campo | |
|---|---|
| **Api Key** | La chiave della console xAI. Viene salvata cifrata e non viene più mostrata; quando modifichi il provider, clicca **Reset** per inserirne un'altra. |
| **Model** | Il modello da usare, ad esempio `grok-4` o `grok-3-mini`. **Refresh** elenca i modelli che la tua chiave può usare; puoi scrivere un nome che non è nell'elenco. |
| **URL** | Facoltativo. Se vuoto usa l'API di xAI (`https://api.x.ai/v1`); impostalo per inviare le richieste tramite un proxy. |
| **Timeout (seconds)** | Quanto attendere la risposta di xAI, e altro testo una volta che ha iniziato a scrivere. Il valore predefinito è 300. |
| **Retries** | Quante volte una richiesta viene ripetuta quando xAI risponde con un limite di frequenza (429) o un errore del server (5xx), o quando non può essere raggiunto. Il valore predefinito è 2; 0 disattiva i tentativi. |
| **Send the reasoning effort** | *Automatic* invia l'impegno di ragionamento solo a `grok-3-mini`, *Always* a ogni modello, *Never* a nessuno. |

**Test Connection** elenca i modelli e chiede a quello scelto di generare alcuni token, con i valori presenti nella finestra
(prima che tu li salvi).

<a id="reasoning"></a>

## Ragionamento

Le impostazioni del ragionamento sono quelle generali del provider, come per qualsiasi altro provider. Con il ragionamento
attivo, il ragionamento mostrato da un modello arriva come ragionamento della parte di storia. L'impegno viene inviato solo
ai modelli che lo accettano (`grok-3-mini`: *low*, oppure *high* per medium e high); gli altri modelli Grok ragionano come di
default e rifiutano il parametro.

<a id="what-works"></a>

## Cosa funziona

Lo streaming, l'interruzione, gli schemi JSON, le funzioni che il modello può chiamare, le immagini (JPEG, PNG) e i file di
testo come allegati, i limiti delle AI condivise e il conteggio dell'utilizzo funzionano come con il provider compatibile con
OpenAI. L'utilizzo è quello riportato da xAI; i token di ragionamento contano come token generati. La temperatura, top p e le
penalità del [protocollo](../protocols.md) vengono inviate quando attive; alcuni modelli di ragionamento rifiutano le penalità. Non sono
supportati: gli strumenti lato server di xAI (ricerca in tempo reale, esecuzione di codice) e la generazione di immagini o
video.
