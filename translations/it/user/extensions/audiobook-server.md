---
label: Configurare il server vocale
order: 28
---

# Configurare il server vocale per l'audiolibro

L'estensione [Audiobook](audiobook.md) richiede un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licenza MIT) a cui può accedere via HTTP. Questa pagina riguarda il lato server. Quanto descritto è stato provato su una sola macchina (una AMD Radeon AI PRO R9700 con 32 GB, LiteLLM davanti a tutto, `llama-server` in modalità router); il tuo hardware e il tuo proxy potrebbero richiedere valori diversi.

<a id="which-setup-is-yours"></a>

## Quale configurazione hai?

| La tua situazione | Cosa fare |
|---|---|
| Il server vocale gira su **un'altra macchina**, sulla CPU, o su una scheda con spazio sufficiente **accanto al modello linguistico** | Avvia il server, inserisci il suo indirizzo in un backend, **lascia vuote le richieste prima e dopo un batch**. Non serve altro. |
| Il server vocale e il modello linguistico **condividono una scheda** e non ci stanno insieme | Ti servono le richieste prima e dopo un batch: **prima** libera la scheda e avvia il server vocale, **dopo** lo ferma. Il resto di questa pagina. |
| Il modello linguistico è un modello in cloud | Non c'è nulla da liberare: avvia il server vocale dove preferisci e lascia vuote le richieste. |

Quindi: **sì, le richieste prima e dopo servono solo quando i due modelli si alternano su una sola GPU.** Sono ciò che rende possibile l'alternanza: il plugin le esegue una volta sola attorno a un *batch* (tutto ciò che hai avviato con un clic), non attorno a ogni singola parte.

<a id="1-the-speech-server"></a>

## 1. Il server vocale

Avvia Chatterbox-TTS-Server in un container (o dal codice sorgente) in modo che `GET /api/model-info` risponda. Aspetti importanti:

- **Il modello.** Turbo (`chatterbox-turbo`) è il default dell'estensione e quello che suona meglio nei test; il modello multilingue serve solo per testi in una lingua diversa da quella del libro. Il modello si imposta nel `config.yaml` del server
  (oppure `POST /save_settings` e `POST /restart_server`). Lo *Speech model* del backend deve essere il modello che il server ha caricato dopo le richieste prima di un batch.
- **Il giapponese richiede `pykakasi` nell'immagine.** Senza, il server scrive nel log *pykakasi not available - Japanese text processing skipped* e legge male i kanji. Aggiungi `RUN python3 -m pip install --no-cache-dir pykakasi` al suo Dockerfile (prima di `COPY . .`). Lo stesso salto silenzioso esiste per i pacchetti per ebraico, russo e cinese.
- **Le voci** sono file nella cartella `reference_audio/` del server (montala come volume, così sopravvivono). L'estensione carica da sola le clip dei tuoi personaggi (`POST /upload_reference`) prima di un batch, con un nome ricavato dall'hash della clip. Il server non ha una chiamata di cancellazione: le clip si accumulano lì.
- Il server **non ha autenticazione**. Non pubblicare la sua porta; metti un proxy davanti se non è su una rete privata.
- `restart: "no"` è adatto a un container che qualcos'altro avvia su richiesta. `restart: unless-stopped` lo avvia all'avvio e occupa la scheda inutilmente.

Nel backend (*Settings → Extension Settings → Audiobook Settings*) inserisci il suo indirizzo, spunta *The server is on this machine or on my network* se è così, e premi **Test connection**: quello che vuoi vedere è *the server answers, the model is loaded*.
L'estensione parla con `/tts` (l'insieme completo dei parametri: voce, lingua, exaggeration, peso CFG, temperature, velocità, seed) e con `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Se tra te e il server c'è un proxy, deve far passare tutti questi.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## Una GPU per il modello e per il parlato

Una scheda che contiene il modello linguistico non ha spazio per il modello vocale. Lo schema è lo stesso con qualsiasi proxy:

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**L'ordine non è negoziabile.** Avviare il container vocale mentre il modello linguistico occupa la scheda ha saturato la memoria dell'host durante il test
(30 GB di RAM, la swap, poi l'OOM killer ha terminato altri servizi): lo scaricamento **deve venire per primo**, e la prima richiesta che fallisce interrompe
l'elenco, così nulla si avvia su una scheda piena. Le richieste *dopo* un batch vengono tutte eseguite, qualunque cosa succeda a una di loro.

Perché *fermare* il container invece di scaricare il modello dentro il server: uno scaricamento interno al server, dopo molti cambi di modello, lasciava occupato circa 1 GB della scheda; fermare il container libera tutto.

<a id="the-requests"></a>

### Le richieste

Ogni richiesta degli elenchi è composta da un metodo, un indirizzo, header, un corpo e un'attesa successiva (in secondi). Non scrivere la chiave negli header (il modulo mostra gli header come vengono digitati): inseriscila in **Key of the requests around a batch**, oppure lasciala vuota e viene inviata la *Api key* del backend. Viene inviata come `Authorization: Bearer` con ogni richiesta sotto che non ne ha una propria. Con un LiteLLM
davanti a tutto e una sola chiave, gli elenchi sono:

| | Metodo e indirizzo | Header | Corpo | Fa |
|---|---|---|---|---|
| Prima 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Scarica il modello linguistico; risponde l'hook stesso, impiega da 1 a 2 s. |
| Prima 2 | `POST http://litellm:4000/gpu/tts/start` | | | Avvia il container vocale e risponde quando il server è pronto (circa 13 s). |
| Prima 3 (solo se il server si avvia senza modello) | `POST http://litellm:4000/gpu/tts/load` | | | Carica il modello (da 8 a 14 s, 50 s la prima volta, quando viene scaricato). |
| Dopo 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Ferma il container (meno di un secondo). |

Il modello linguistico torna da solo: la richiesta normale successiva fa caricare il modello al router (circa un minuto per un modello grande; con l'[hook della cache del prompt](../prompt-cache.md) viene ripristinata la cache della tua sessione), quindi per lui non serve alcuna richiesta dopo il batch.

Senza LiteLLM le stesse richieste vanno a ciò che avvia e ferma i tuoi container; l'unica regola è l'ordine sopra.

<a id="what-answers-those-addresses"></a>

### Chi risponde a questi indirizzi

- **`X-Llama-Unload`** è gestito dall'hook `gpu_unload.py` di LiteLLM, descritto con il suo codice in
  [Liberare la GPU per un altro modello](../prompt-cache.md#freeing-the-gpu-for-another-model) (funziona anche senza l'hook della cache). Con una route `openai/*` il nome del modello nel corpo deve iniziare con `openai/`: un altro nome riceve una risposta 400 *dopo* che i modelli sono stati scaricati.
- **`/gpu/tts/start`, `/stop`, `/load`** sono **pass-through endpoints** di LiteLLM verso il piccolo controller `tools/gpu-control` del repository, che avvia e ferma container per nome. Il suo
  README contiene il file compose con il **Docker socket proxy** (così il controller può avviare e fermare container e nient'altro; il socket stesso è root sull'host) e le impostazioni che è facile sbagliare: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

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

  (il container si chiama `chatterbox` e `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indica al controller che cosa attendere; il token del controller resta nel suo `.env` e nell'ambiente di LiteLLM, Marginalia ha soltanto la chiave di LiteLLM).
- **Tutti i container e LiteLLM condividono una rete Docker** (`docker network create ai`, una volta sola). Inserisci `networks: [default, ai]` e la rete esterna `ai` in ogni file compose: un `docker network connect` fatto a mano va perso quando un container viene ricreato.

<a id="checking-it"></a>

## Verificarlo

1. `curl http://<speech server>/api/model-info` risponde (`loaded` vero dopo un avvio).
2. *Test connection* nel backend dice che il modello è caricato. Se le richieste prima di un batch sono l'unica cosa che avvia il server, verificalo con gli elenchi già impostati: *the server answers, no model is loaded* è la risposta di un server che è stato fermato e riavviato e che deve ancora caricare il modello.
3. Converti una parte breve con **Convert to audio**. La finestra mostra i pezzi in lavorazione; dopo, il container vocale dovrebbe essere fermo e `nvidia-smi` / `nvtop` / `rocm-smi` dovrebbero mostrare di nuovo la scheda libera.
4. Se la prima chiamata dopo un caricamento del modello richiede molto tempo, è il motore che si scalda (una volta sola, 28 s nella prima esecuzione, non più ripetuto).
<a id="troubleshooting"></a>

## Risoluzione dei problemi

| Sintomo | Causa |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Il server è partito vuoto (un wrapper di avvio che scarica il modello una volta): aggiungi la richiesta **Before 3**. |
| Le prime parti vengono create, poi il server dice *Model not loaded* e il suo log mostra `Request received for /api/unload` | Qualcosa di diverso da Marginalia scarica il modello (l'estensione non chiama mai `/api/unload`): tipicamente un wrapper di avvio nel container che attende che il server risponda e poi scarica il modello per "tenere breve l'avvio". La sua attesa può terminare solo dopo la prima parte, perché il server a thread singolo era occupato, e lo scaricamento arriva quindi a metà del batch (un traceback di timeout `urllib` nel log del container indica che è quel wrapper in attesa). Il modello da cercare nel file compose è `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Sostituiscilo con `command: ["python3", "server.py"]`: un semplice avvio lascia Turbo caricato, e lo scaricamento serviva solo a liberare la scheda dopo un avvio, cosa che un container avviato e fermato dal controller non richiede. Per capire chi l'ha inviato, leggi il log dell'estensione (*Admin → Extensions → Log*): elenca ogni richiesta prima e dopo un batch con il suo indirizzo e l'ora in cui ha ricevuto risposta, e quando le parti iniziano e finiscono. Uno scaricamento che arriva mentre il log dice che il batch è in corso, oppure da un indirizzo che non compare nell'elenco, non è dell'estensione. |
| Il batch si ferma a metà (il log dell'estensione dice *Cannot reach*, oppure il log del server semplicemente finisce) e un riavvio manuale funziona | Il server è morto, di solito ucciso dal sistema perché ha occupato tutta la memoria (`dmesg \| grep -i oom` sull'host: un processo `python3` di molti GB). L'estensione allora lo riavvia da sola (prima le richieste dopo il batch, poi quelle prima) e rifà la parte una sola volta; quando il server non riesce a tornare su, o sparisce di nuovo subito, il resto del batch fallisce subito e il log spiega il motivo. Per evitare che un server che perde memoria arrivi a tanto, imposta **Restart the server after (pieces)** nel backend (per esempio 100), e un `mem_limit` sul container, così che il sistema uccida solo quello. |
| La conversione fallisce subito con la risposta del proxy | La prima richiesta prima del batch è fallita (una chiave sbagliata, la rotta di scaricamento non trovata); l'elenco si ferma lì di proposito. |
| L'host esaurisce la memoria quando il batch parte | Il modello linguistico era ancora sulla scheda: la richiesta di scaricamento manca oppure non è la prima. |
| Il giapponese viene letto come spazzatura | `pykakasi` manca nell'immagine del server; il backend deve essere anche quello multilingue per quella parte. |
| Il caricamento di una clip fallisce dietro un proxy | Il proxy deve passare un corpo multipart (`/upload_reference`) senza modifiche. Non testato tramite il pass-through di LiteLLM; usa un file vocale del server (`predefined:Name.wav`) finché non funziona. |
| Il batch di un altro utente scarica il modello sotto al tuo | I batch su uno stesso indirizzo del server condividono un cancello interno a Marginalia, e un batch per un altro modello vocale attende che gli altri finiscano. Un server vocale e, per esempio, un server di immagini su una stessa scheda devono avere lo stesso nome in **GPU (name)** su entrambi i backend per attendersi a vicenda (anche quello del Generatore di immagini). Due installazioni di Marginalia sulla stessa scheda non condividono il cancello. |
