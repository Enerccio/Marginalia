---
label: Configurazione del server vocale
order: 28
---

# Configurazione del server vocale per l'audiolibro

L'estensione [Audiobook](audiobook.md) richiede un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licenza MIT) raggiungibile via HTTP. Questa pagina riguarda il lato server. La configurazione descritta è stata provata su una sola macchina (un AMD Radeon AI PRO R9700
con 32 GB, LiteLLM davanti a tutto e `llama-server` in modalità router); il tuo hardware e il tuo proxy potrebbero richiedere altri valori.

<a id="which-setup-is-yours"></a>

## Quale configurazione è la tua?

| La tua situazione | Cosa fare |
|---|---|
| Il server vocale gira su **un'altra macchina**, sulla CPU, o su una scheda con spazio sufficiente **accanto al modello linguistico** | Avvia il server, inserisci il suo indirizzo in un backend, **lascia vuote le richieste prima e dopo un batch**. Non serve altro. |
| Il server vocale e il modello linguistico **condividono una scheda** e non ci stanno insieme | Servono le richieste prima e dopo un batch: **prima** libera la scheda e avvia il server vocale, **dopo** lo ferma. Il resto di questa pagina. |
| Il modello linguistico è un modello cloud | Non c'è nulla da liberare: avvia il server vocale dove preferisci e lascia le richieste vuote. |

Quindi: **sì, le richieste pre e post sono necessarie solo quando i due modelli si alternano su una stessa GPU.** Servono a gestire questi turni: l'estensione le esegue una volta sola attorno a un *batch* (tutto ciò che avvii con un clic), non attorno a ogni singola parte.

<a id="1-the-speech-server"></a>

## 1. Il server vocale

Avvia Chatterbox-TTS-Server in un container (oppure dai sorgenti) in modo che `GET /api/model-info` risponda. Punti importanti:

- **Il modello.** Turbo (`chatterbox-turbo`) è il predefinito dell'estensione ed è quello che suona meglio nei test; il modello multilingue serve solo per testi in una lingua diversa da quella del libro. Il modello si imposta nel `config.yaml` del server
  (oppure con `POST /save_settings` e `POST /restart_server`). Il *Speech model* del backend deve essere il modello che il server ha caricato dopo le richieste prima di un batch.
- **Il giapponese richiede `pykakasi` nell'immagine.** Senza di esso il server scrive nel log *pykakasi not available - Japanese text processing skipped* e legge male i kanji. Aggiungi `RUN python3 -m pip install --no-cache-dir pykakasi` al suo Dockerfile (prima di `COPY . .`). Lo stesso salto silenzioso riguarda i pacchetti per ebraico, russo e cinese.
- **Le voci** sono file nella cartella `reference_audio/` del server (montala come volume, così sopravvivono ai riavvii). L'estensione carica da sola le clip dei tuoi personaggi (`POST /upload_reference`) prima di un batch, con un nome ricavato dall'hash della clip. Il server non ha una chiamata per cancellare: le clip si accumulano.
- Il server **non ha autenticazione**. Non pubblicare la sua porta; metti un proxy davanti se non è su una rete privata.
- `restart: "no"` per un container che qualcos'altro avvia su richiesta. `restart: unless-stopped` lo avvia all'accensione e occupa la scheda per nulla.

Nel backend (*Settings → Extension Settings → Audiobook Settings*) inserisci il suo indirizzo, spunta *The server is on this machine or on my network* se è così, e premi **Test connection**: il risultato che vuoi è *the server answers, the model is loaded*.
L'estensione parla con `/tts` (l'insieme completo dei parametri: voce, lingua, exaggeration, CFG weight, temperature, speed, seed) e con `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Se tra te e il server c'è un proxy, deve far passare tutte queste chiamate.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## Una GPU per il modello e per la voce

Una scheda che contiene il modello linguistico non ha spazio per il modello vocale. Il piano è lo stesso con qualsiasi proxy:

```
prima di un batch:  1. scarica il modello linguistico
                    2. avvia il server vocale (e aspetta che sia pronto; caricane il modello se parte vuoto)
la voce:            tutti i pezzi di tutte le parti del batch
dopo un batch:      3. ferma il server vocale (il modello linguistico si ricarica da solo con la richiesta successiva)
```

**L'ordine non è negoziabile.** Avviare il container vocale mentre il modello linguistico occupa la scheda ha riempito la memoria dell'host durante il test (30 GB di RAM, poi lo swap, poi l'OOM killer ha terminato altri servizi): lo scaricamento **deve venire per primo**, e la prima richiesta che fallisce interrompe la lista, così nulla parte su una scheda piena. Le richieste *dopo* un batch vengono eseguite tutte, qualunque cosa accada a una di esse.

Perché *fermare* il container invece di scaricare il modello dentro il server: uno scaricamento interno al server lasciava occupata circa 1 GB della scheda dopo molti cambi di modello; fermare il container libera tutto.

<a id="the-requests"></a>

### Le richieste

Ogni richiesta delle liste è un metodo, un indirizzo, degli header, un corpo e un'attesa successiva (in secondi). Non scrivere la chiave negli header (il modulo mostra gli header come vengono digitati): inseriscila in **Key of the requests around a batch**, oppure lasciala vuota e verrà inviata la *Api key* del backend. Viene inviata come `Authorization: Bearer` con ogni richiesta sotto che non ne ha una propria. Con un LiteLLM davanti a tutto e una sola chiave, le liste sono:

| | Metodo e indirizzo | Header | Corpo | Effetto |
|---|---|---|---|---|
| Prima 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Scarica il modello linguistico; risponde l'hook stesso, impiega da 1 a 2 s. |
| Prima 2 | `POST http://litellm:4000/gpu/tts/start` | | | Avvia il container vocale e risponde quando il server è pronto (circa 13 s). |
| Prima 3 (solo se il server parte senza modello) | `POST http://litellm:4000/gpu/tts/load` | | | Carica il modello (da 8 a 14 s, 50 s la prima volta, quando va scaricato). |
| Dopo 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Ferma il container (meno di un secondo). |

Il modello linguistico torna da solo: la richiesta normale successiva fa sì che il router lo carichi (circa un minuto per un modello grande; con l'[hook della cache del prompt](../prompt-cache.md) viene ripristinata la cache della tua sessione), quindi per lui non serve alcuna richiesta dopo il batch.

Senza LiteLLM, le stesse richieste vanno a ciò che avvia e ferma i tuoi container; l'unica regola è l'ordine sopra.

<a id="what-answers-those-addresses"></a>

### Cosa risponde a questi indirizzi

- **`X-Llama-Unload`** è gestito dall'hook `gpu_unload.py` di LiteLLM, descritto con il suo codice in
  [Liberare la GPU per un altro modello](../prompt-cache.md#freeing-the-gpu-for-another-model) (funziona anche senza l'hook della cache). Con una route `openai/*` il nome del modello nel corpo deve iniziare con `openai/`: un altro nome riceve 400 *dopo* che i modelli sono stati scaricati.
- **`/gpu/tts/start`, `/stop`, `/load`** sono **endpoint pass-through** di LiteLLM verso il piccolo controllore `tools/gpu-control` del repository, che avvia e ferma i container per nome. Il suo
  README contiene il file compose con il **Docker socket proxy** (così il controllore può avviare e fermare container e nient'altro; il socket in sé è root sull'host) e le impostazioni facili da sbagliare: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

  ```yaml
  general_settings:
    pass_through_endpoints:
      - path: "/gpu/tts/start"
        target: "http://gpu-control:8000/chatterbox/start"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/stop"
        target: "http://gpu-control:8000/chatterbox/stop"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/load"
        target: "http://chatterbox:8004/restart_server"
  ```

  (il container si chiama `chatterbox` e `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indica al controllore cosa attendere; il token del controllore resta nel suo `.env` e nell'ambiente di LiteLLM, Marginalia possiede solo la chiave di LiteLLM).
- **Tutti i container e LiteLLM condividono una rete Docker** (`docker network create ai`, una volta sola). Inserisci `networks: [default, ai]` e la rete esterna `ai` in ogni file compose: un `docker network connect` fatto a mano va perso quando un container viene ricreato.

<a id="checking-it"></a>

## Verifica

1. `curl http://<server vocale>/api/model-info` risponde (`loaded` è true dopo un avvio).
2. *Test connection* nel backend dice che il modello è caricato. Se le richieste prima di un batch sono l'unica cosa che avvia il server, provalo con le liste già configurate: *the server answers, no model is loaded* è la risposta di un server fermato e poi riavviato che deve ancora caricare il modello.
3. Converti una parte breve in **Convert to audio**. La finestra mostra i pezzi in lavorazione; alla fine il container vocale dovrebbe essere fermo e `nvidia-smi` / `nvtop` / `rocm-smi` mostrano di nuovo la scheda libera.
4. Se la prima chiamata dopo un caricamento del modello richiede molto tempo, è il motore che si scalda (una volta sola, 28 s nella prima esecuzione, e non si è più ripetuto).
<a id="troubleshooting"></a>

## Risoluzione dei problemi

| Sintomo | Causa |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Il server è partito vuoto (un wrapper di avvio che scarica il modello una volta): aggiungi la richiesta **Before 3**. |
| Le prime parti vengono fatte, poi il server risponde *Model not loaded* e il suo log mostra `Request received for /api/unload` | Qualcosa di diverso da Marginalia scarica il modello (l'estensione non chiama mai `/api/unload`): tipicamente un wrapper di avvio nel container che aspetta che il server risponda e poi scarica il modello per "tenere breve l'avvio". La sua attesa può terminare solo dopo la prima parte, perché il server a thread singolo era occupato, e lo scaricamento arriva quindi a metà del batch (un traceback di timeout di `urllib` nel log del container indica che è quel wrapper in attesa). Il modello da cercare nel file compose è un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Sostituiscilo con `command: ["python3", "server.py"]`: un avvio da solo lascia Turbo caricato, e lo scaricamento serviva solo a tenere libera la scheda dopo un avvio, cosa che un container avviato e fermato dal controller non richiede. Per capire chi l'ha inviato, leggi il log dell'estensione (*Admin → Extensions → Log*): elenca ogni richiesta prima e dopo un batch con il suo indirizzo e l'ora in cui è stata risposta, e quando le parti iniziano e finiscono. Uno scaricamento che arriva mentre il log dice che il batch è in corso, o da un indirizzo che non è nell'elenco, non è dell'estensione. |
| Il batch si ferma a metà (il log dell'estensione dice *Cannot reach*, oppure il log del server semplicemente finisce) e un riavvio manuale funziona | Il server è morto, tipicamente ucciso dal sistema perché ha preso tutta la memoria (`dmesg \| grep -i oom` sull'host: un processo `python3` di molti GB). L'estensione allora lo riavvia da sola (prima le richieste dopo il batch, poi quelle prima) e rifà la parte una sola volta; quando il server non può essere riportato su, o sparisce di nuovo subito, il resto del batch fallisce subito e il log dice perché. Per evitare che un server che perde memoria arrivi a tanto, imposta **Restart the server after (pieces)** nel backend (per esempio 100), e un `mem_limit` sul container, così che il sistema uccida solo lui. |
| La conversione fallisce subito con la risposta del proxy | La prima richiesta prima del batch è fallita (una chiave sbagliata, la rotta di scaricamento non trovata); l'elenco si ferma lì di proposito. |
| L'host esaurisce la memoria quando il batch parte | Il modello linguistico era ancora sulla scheda: la richiesta di scaricamento manca o non è la prima. |
| Il giapponese viene letto come spazzatura | `pykakasi` manca nell'immagine del server; il backend deve inoltre essere quello multilingue per quella parte. |
| Il caricamento di una clip fallisce dietro un proxy | Il proxy deve far passare un corpo multipart (`/upload_reference`) senza modifiche. Non testato attraverso il pass-through di LiteLLM; usa un file vocale del server (`predefined:Name.wav`) finché non funziona. |
| Il batch di un altro utente scarica il modello sotto il tuo | I batch su uno stesso indirizzo di server condividono un cancello interno a Marginalia, e un batch per un altro modello vocale attende che gli altri finiscano. Un server vocale e, per esempio, un server di immagini sulla stessa scheda hanno bisogno dello stesso nome in **GPU (name)** su entrambi i backend per aspettarsi a vicenda (anche quello del Generatore di immagini). Due installazioni di Marginalia sulla stessa scheda non condividono il cancello. |
