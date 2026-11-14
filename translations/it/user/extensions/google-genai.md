---
label: Google Gemini
order: 14
---

# Google Gemini

Aggiunge i modelli Gemini di Google come [provider di inferenza](../inference-providers.md), tramite l'API di Google invece che tramite una compatibile con OpenAI.

Google offre anche un endpoint compatibile con OpenAI, che funziona con il normale provider *Compatibile con OpenAI*. Questa estensione si collega direttamente all'API Gemini (il Google Gen AI SDK), e questo offre cose che l'endpoint compatibile non offre: i **riassunti del ragionamento del modello** mostrati come ragionamento, il livello di ragionamento dei Gemini 3 e il budget di ragionamento dei modelli precedenti, i filtri di sicurezza e i conteggi esatti dei token riportati da Google.

!!!warning Il testo della tua storia va a Google
Ogni richiesta di un'IA che usa questo provider - il prompt con la storia, le voci del lorebook e i riassunti, le risposte del modello, i file e le immagini che un agente o un comando invia - viene inviata ai server di Google (`generativelanguage.googleapis.com`, oppure il proxy che imposti). Le condizioni d'uso e l'informativa sulla privacy di Google per l'API Gemini decidono cosa succede a questi dati lì (conservazione, eventuale utilizzo per migliorare i modelli; questo dipende dal piano della tua chiave API). Gli altri provider inviano al server che hai scelto, che potrebbe essere un server tuo. Non usare questo provider per testi che non puoi inviare a Google. Su un server condiviso con altri utenti, un amministratore che condivide un'IA di questo provider invia a Google il testo della storia di chiunque la usi.
!!!

<a id="installing"></a>

## Installazione

Carica l'estensione in *Admin → Extensions*, come descritto in [Gestione delle estensioni](../administration/extensions.md). Il JAR è grande (circa 14 MB): contiene l'SDK di Google e le librerie di cui ha bisogno solo l'SDK. Nient'altro in Marginalia le usa.

Una volta caricata, la lista **Type** della finestra del provider di inferenza ha una nuova voce, **Extension**, e la lista **Provider** in cima alla scheda *Per Type Settings* offre **Google Gemini** (viene scelto automaticamente finché è l'unico disponibile). Senza un'estensione caricata che fornisce IA, **Extension** non viene proposta.

<a id="adding-a-gemini-provider"></a>

## Aggiungere un provider Gemini

1. Ottieni una chiave API in [Google AI Studio](https://aistudio.google.com/apikey).
2. Nella scheda **Inference Providers** clicca su **Aggiungi provider di inferenza**, inserisci un **Nome**, scegli il tipo **Extension** e il provider **Google Gemini**.
3. Compila le *Impostazioni generali* (dimensione del contesto, dimensione della risposta, [ragionamento](#reasoning)) e le impostazioni del provider nella scheda *Per Type Settings*, quindi clicca su **Prova la connessione**.

![Le impostazioni generali di un provider di tipo Extension](../../images/plugin-googlegenai-general.png)

![Le Impostazioni per tipo del provider Google Gemini](../../images/plugin-googlegenai.png)

Il provider compare poi nell'elenco come gli altri, con il nome del provider nella colonna *Type*:

![L'elenco Inference Providers con un provider Google Gemini](../../images/plugin-googlegenai-list.png)

| Campo | |
|---|---|
| **Chiave API** | La chiave di Google AI Studio. Viene memorizzata cifrata e non viene più mostrata; quando modifichi il provider, clicca su **Reimposta** per inserirne un'altra. |
| **Modello** | Il modello da usare, ad esempio `gemini-2.5-flash` o `gemini-3-pro-preview`. **Aggiorna** elenca i modelli che la tua chiave può usare per scrivere testi; puoi digitare un nome che non è nell'elenco. |
| **URL** | Facoltativo. Se vuoto usa l'API di Google (`generativelanguage.googleapis.com`); impostalo per inviare le richieste tramite un proxy. |
| **Timeout (secondi)** | Quanto attendere che Google risponda, e per altro testo una volta che ha iniziato a scrivere. Il valore predefinito è 300. |
| **Tentativi** | Quante volte una richiesta viene ripetuta quando Google risponde con un limite di frequenza (429) o un errore del server (5xx), oppure non può essere raggiunto. Il valore predefinito è 2; 0 disattiva i tentativi. |
| **Disattiva i filtri di sicurezza** | Invia `BLOCK_NONE` per le categorie di sicurezza regolabili (molestie, incitamento all'odio, contenuti sessualmente espliciti, contenuti pericolosi). La narrativa spesso incontra i filtri. Google blocca comunque alcuni contenuti, indipendentemente da ciò che viene inviato. |

**Prova la connessione** elenca i modelli e chiede a quello scelto di generare alcuni token, con i valori presenti nella finestra (prima che tu li salvi).

<a id="reasoning"></a>

## Ragionamento

Le impostazioni di ragionamento sono quelle delle impostazioni generali del provider, come per ogni altro provider:

- Con **Enabled Reasoning** disattivato il modello ragiona come fa per impostazione predefinita e il suo ragionamento non viene mostrato.
- Con l'opzione attiva, il modello invia i riassunti del suo ragionamento e vengono mostrati come ragionamento della parte di storia. **Reasoning Effort** imposta quanto ragiona: per i modelli Gemini 3 è il livello di ragionamento (low, medium, high), per i modelli precedenti un budget di 1 024, 8 192 o 24 576 token di ragionamento. **None** equivale a disattivato.

I token di ragionamento contano come token generati e vengono prelevati dalla dimensione di risposta che hai impostato: se il modello la usa tutta per ragionare, la parte di storia torna vuota e l'errore segnala che la dimensione della risposta è stata esaurita. Aumenta **Token massimi di risposta** oppure abbassa l'impegno.

<a id="what-works"></a>

## Cosa funziona

Tutto ciò di cui una storia ha bisogno funziona come con un provider compatibile con OpenAI: lo streaming, l'interruzione di una generazione, i riassunti, le [estensioni che chiamano il modello](index.md), le funzioni che il modello può richiamare (il [Generatore di nomi](name-generator.md) ne offre alcune), e le immagini e i file allegati a un messaggio. I token usati da un utente vengono conteggiati nelle colonne **Token inviati** e **Token generati** a partire dai numeri riportati da Google, e si applicano i [limiti](../inference-providers.md#limits-of-shared-providers) di un provider condiviso.

I conteggi dei token mostrati nella storia (la dimensione di una parte, il prompt) sono calcolati da Google (la richiesta `countTokens` del modello), una volta per ogni testo: le risposte vengono ricordate. Quando Google non può essere interrogato (nessuna chiave, nessuna rete, un errore), Marginalia stima da sé il conteggio per un minuto e poi riprova con Google; un testo salvato o modificato mentre un altro salvataggio è in corso viene stimato anch'esso. La stima differisce un po' da ciò che Google fattura.

<a id="when-the-extension-is-not-loaded"></a>

## Quando l'estensione non è caricata

I provider che hai creato restano nell'elenco quando l'estensione viene scaricata, con il tipo *Extension: google-genai*, e tornano a funzionare quando l'estensione viene caricata di nuovo. Mentre non è caricata, un libro che usa un tale provider non può generare: l'errore ti invita a caricare l'estensione o a scegliere un altro provider. Il provider può comunque essere aperto e le sue impostazioni generali modificate; le impostazioni Gemini vengono mostrate solo quando l'estensione è caricata, e non vengono toccate.

<a id="not-supported"></a>

## Non supportato

- **Vertex AI** (Google Cloud con un progetto e delle credenziali). Sono supportate solo le chiavi API dell'API Gemini.
- Generazione di immagini e gli altri tipi di output di Gemini: viene scritto solo testo.
