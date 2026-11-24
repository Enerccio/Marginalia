---
label: xAI Grok
order: 26
---

# xAI Grok

Aggiunge i modelli Grok di xAI come [fornitore di inferenza](../inference-providers.md), tramite l'API di xAI.

Grok è raggiungibile anche con il normale fornitore *OpenAI Compatible* e l'URL `https://api.x.ai/v1`. Questa estensione si collega direttamente all'API di xAI, e questo permette di mostrare il ragionamento come tale, i conteggi esatti dei token riportati da xAI (inclusi i token di ragionamento) e i conteggi, oltre a un elenco dei modelli e un test di connessione pensati per il servizio.

!!!warning Il testo della tua storia viene inviato a xAI
Ogni richiesta di un'IA che usa questo fornitore - il prompt con la storia, le voci del lorebook e i riassunti, le risposte del modello, le immagini che un agente o un comando invia - viene inviata ai server di xAI (`api.x.ai`, oppure al proxy che imposti). I termini e l'informativa sulla privacy di xAI per l'API stabiliscono cosa succede lì. Gli altri fornitori inviano al server che hai scelto, che potrebbe essere il tuo. Non usare questo fornitore per testo che non puoi inviare a xAI. Su un server condiviso con altri utenti, un amministratore che condivide un'IA di questo fornitore invia a xAI il testo della storia di tutti coloro che la usano.
!!!

<a id="installing"></a>

## Installazione

Carica l'estensione in *Admin → Extensions*, come descritto in [Gestione delle estensioni](../administration/extensions.md). Una volta caricata, l'elenco **Type** della finestra del fornitore di inferenza include la voce **Extension**, e l'elenco **Provider** in cima alla scheda *Per Type Settings* offre **xAI Grok**.

<a id="adding-a-grok-provider"></a>

## Aggiungere un fornitore Grok

1. Ottieni una chiave API nella [xAI Console](https://console.x.ai).
2. Nella scheda **Inference Providers** clicca **Add Inference Provider**, inserisci un **Name**, scegli il tipo **Extension** e il fornitore **xAI Grok**.
3. Compila le *General Settings* (dimensione del contesto, dimensione della risposta, [ragionamento](#reasoning)) e le impostazioni del fornitore nella scheda *Per Type Settings*, poi clicca **Test Connection**.

![Un fornitore Grok](../../images/plugin-grok.png)

![Il fornitore Grok nell'elenco](../../images/plugin-grok-list.png)

| Campo | |
|---|---|
| **Api Key** | La chiave della xAI Console. Viene salvata cifrata e non viene più mostrata; quando modifichi il fornitore, clicca **Reset** per inserirne un'altra. |
| **Model** | Il modello da usare, ad esempio `grok-4` o `grok-3-mini`. **Refresh** elenca i modelli che la tua chiave può usare; puoi digitare un nome che non compare nell'elenco. |
| **URL** | Facoltativo. Se vuoto, usa l'API di xAI (`https://api.x.ai/v1`); impostalo per inviare le richieste tramite un proxy. |
| **Timeout (seconds)** | Quanto attendere che xAI risponda, e poi altro testo una volta che ha iniziato a scrivere. Il valore predefinito è 300. |
| **Retries** | Quante volte una richiesta viene ripetuta quando xAI risponde con un limite di frequenza (429) o un errore del server (5xx), oppure non può essere raggiunto. Il valore predefinito è 2; 0 disattiva i tentativi. |
| **Send the reasoning effort** | *Automatic* invia l'impegno di ragionamento solo a `grok-3-mini`, *Always* a ogni modello, *Never* a nessuno. |

**Test Connection** elenca i modelli e chiede al modello scelto alcuni token, usando i valori presenti nella finestra (prima che tu li salvi).

<a id="reasoning"></a>

## Ragionamento

Le impostazioni del ragionamento sono quelle generali del fornitore, come per qualsiasi altro fornitore. Con il ragionamento attivo, il ragionamento mostrato da un modello arriva come ragionamento della parte di storia. L'impegno viene inviato solo ai modelli che lo accettano (`grok-3-mini`: *low*, oppure *high* per medium e high); gli altri modelli Grok ragionano come fanno per impostazione predefinita e rifiutano il parametro.

<a id="what-works"></a>

## Cosa funziona

Lo streaming, l'interruzione, gli schemi JSON, le funzioni che il modello può chiamare, le immagini (JPEG, PNG) e i file di testo come allegati, i limiti delle IA condivise e il conteggio dell'uso funzionano come con il fornitore compatibile con OpenAI. L'uso è quello riportato da xAI; i token di ragionamento contano come token generati. La temperatura, top p e le penalità del [protocollo](../protocols.md) vengono inviate quando sono attivate; alcuni modelli di ragionamento rifiutano le penalità. Non sono supportati: gli strumenti lato server di xAI (ricerca in tempo reale, esecuzione di codice) e la generazione di immagini o video.
