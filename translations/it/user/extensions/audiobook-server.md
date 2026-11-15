---
label: Configurare il server vocale
order: 28
---

# Configurare il server vocale per l'Audiobook

L'estensione [Audiobook](audiobook.md) ha bisogno di un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licenza MIT) raggiungibile via HTTP. Questa pagina riguarda il lato server. Ciò che è descritto qui è stato provato su una sola macchina (una AMD Radeon AI PRO R9700
con 32 GB, LiteLLM davanti a tutto, `llama-server` in modalità router); il tuo hardware e il tuo proxy potrebbero richiedere altri valori.

<a id="which-setup-is-yours"></a>

## Quale configurazione hai?

| La tua situazione | Cosa fare |
|---|---|
| Il server vocale gira su **un'altra macchina**, sulla CPU, o su una scheda con spazio sufficiente **accanto al modello linguistico** | Avvia il server, inserisci il suo indirizzo in un backend, **non scegliere alcun GPU lock**. Non serve altro. |
| Il server vocale e il modello linguistico **condividono una scheda** e non ci stanno entrambi | Ti serve un [GPU lock](../administration/gpus.md): i suoi passaggi **acquire** liberano la scheda e avviano il server vocale, i suoi passaggi **release** lo fermano. Il resto di questa pagina. |
| Il modello linguistico è un modello nel cloud | Non c'è nulla da liberare: avvia il server vocale dove preferisci e non scegliere alcun GPU lock. |

Quindi: **le richieste pre e post sono necessarie solo quando i due modelli si alternano su una stessa GPU.** Servono proprio a fare questi turni: il plugin le invia una volta sola attorno a un *batch* (tutto ciò che avvii con un clic), non attorno a ogni singola parte.

<a id="1-the-speech-server"></a>

## 1. Il server vocale

Avvia Chatterbox-TTS-Server in un container (o dal sorgente) in modo che `GET /api/model-info` risponda. Ecco i punti importanti:

- **Il modello.** Turbo (`chatterbox-turbo`) è il predefinito dell'estensione ed è quello che suona meglio nei test; il modello multilingue serve solo per testi in una lingua diversa da quella del libro. Il modello si imposta nella `config.yaml` del server
  (oppure `POST /save_settings` e `POST /restart_server`). Il *Speech model* del backend deve essere il modello che il server ha caricato dopo i passaggi acquire del suo GPU lock.
- **Il giapponese richiede `pykakasi` nell'immagine.** Senza, il server scrive nel log *pykakasi not available - Japanese text processing skipped* e legge male i kanji. Aggiungi `RUN python3 -m pip install --no-cache-dir pykakasi` al suo Dockerfile (prima di `COPY . .`). Lo stesso salto silenzioso esiste per i pacchetti di ebraico, russo e cinese.
- **Le voci** sono file nella cartella `reference_audio/` del server (montala su un volume, così sopravvivono). L'estensione carica da sola le clip dei tuoi attori (`POST /upload_reference`) prima di un batch, con un nome ricavato dall'hash della clip. Il server non ha una chiamata di cancellazione: le clip si accumulano lì.
- Il server **non ha autenticazione**. Non pubblicare la sua porta; metti un proxy davanti se non è su una rete privata.
- `restart: "no"` per un container che qualcos'altro avvia su richiesta. `restart: unless-stopped` lo avvia all'accensione e occupa la scheda per sempre.

Nel backend (*Settings → Extension Settings → Audiobook Settings*) inserisci il suo indirizzo, spunta *The server is on this machine or on my network* se è così, e premi **Test connection**: ti serve la risposta *the server answers, the model is loaded*.
L'estensione parla con `/tts` (l'insieme completo dei parametri: voce, lingua, exaggeration, CFG weight, temperature, speed, seed) e con `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Se tra te e il server c'è un proxy, deve far passare tutti questi endpoint.

## Higgs Audio v3 in ComfyUI

Un backend con il modello vocale **Higgs Audio v3 in ComfyUI** non parla con Chatterbox-TTS-Server, ma con un [ComfyUI](https://github.com/comfyanonymous/ComfyUI) che ha i nodi della
[TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite). Il suo indirizzo è quello di ComfyUI (per esempio
`http://gpu-server:8188`). È stato configurato sulla stessa AMD R9700, nell'immagine `yanwk/comfyui-boot:rocm`.

1. **Installa la suite** nella cartella `custom_nodes` di ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). I suoi pacchetti Python finiscono nel
   container. Per conservarli quando il container viene ricreato, dai a pip una cartella su un volume: aggiungi `PIP_USER=true` all'ambiente del container e monta
   `./storage/local:/root/.local`.
2. **Non lasciare mai che pip sostituisca il torch dell'immagine** (una build CUDA sostituirebbe quella ROCm). Scrivi le versioni installate in un file e installa usandolo come constraint:
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   Non eseguire `install.py` della suite nel suo insieme: potrebbe reinstallare torch. `librosa` non è nel suo `requirements.txt` e la suite non si carica senza.
3. **Il modello**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (circa 8 GB), oppure lascia che la
   suite lo scarichi la prima volta. Richiede circa 8 GB della scheda grafica.
4. **Test connection** nel backend dice *the server answers* con il modello del nodo quando ComfyUI e il nodo sono presenti.

Che cosa fa l'estensione con questo: carica le clip del cast nella cartella di input di ComfyUI (`POST /upload/image`, con un nome ricavato dalla clip), elenca le voci della suite e le clip con `GET /object_info`, invia un workflow proprio a `/prompt` per ogni pezzo, attende su `/history` e recupera l'audio
da `/view`: con ffmpeg dal lato Marginalia come flac, che viene ritagliato dopo l'ultima parola, sfumato in uscita e salvato come mp3 o wav; senza ffmpeg come mp3 di ComfyUI, non ritagliato (il salvataggio in wav in quel caso non funziona). **Dopo ogni pezzo chiama `POST /free` con
`{"free_memory": true}`**: sulla R9700 il terzo pezzo di fila con il modello caricato ha mandato in crash la scheda grafica (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI termina);
svuotare la memoria tra un pezzo e l'altro mantiene il modello caricato ed evita il problema, al costo di circa 3 secondi per pezzo. ComfyUI viene avviato e fermato attorno a un batch con lo stesso tipo di richieste del server vocale ([le richieste](#the-requests)).

**Usa fp32 su una scheda AMD.** Con la precisione `auto` (bf16 sulla R9700 con ROCm 7.2) molti pezzi sono usciti come parole senza senso, come effetti sonori (una battuta sul bussare è diventata undici colpi) o con una voce sbagliata, e cambiare il seed cambiava solo la fortuna. Gli stessi pezzi in fp32 sono risultati perfetti parola per parola. Il valore **Precision** del backend è fp32 di default; richiede circa 16 GB della scheda. Il riconoscimento vocale Granite della suite ha lo stesso problema in bf16 (testo senza senso dal secondo riconoscimento in poi), quindi anche l'estensione lo esegue in float32.

Il motore **Fish Audio S2 Pro** della suite non ha funzionato su quella scheda: il suo audio è uscito come numeri non validi (NaN). I pesi di Higgs Audio v3 sono distribuiti con una licenza di ricerca e non commerciale.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## Una sola GPU per il modello linguistico e la voce

Una scheda che contiene il modello linguistico non ha spazio per il modello vocale. L'amministratore configura lo scambio una sola volta in [Admin → GPUs](../administration/gpus.md), con una GPU e un lock per ogni cosa che può essere caricata su di essa, e sceglie il lock nell'AI del modello linguistico e nel backend vocale. Il piano è lo stesso per qualsiasi proxy:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

Un batch (o un campione) del backend vocale esegue i passi di acquisizione del lock vocale quando la GPU contiene il modello linguistico, poi tutti i pezzi; dopo, la GPU resta caricata con il server vocale. La richiesta successiva al modello linguistico attende il batch, esegue il passo di rilascio del lock vocale (arresto del server) e prosegue. Più batch e campioni di seguito pagano lo scambio una sola volta.

**L'ordine non è negoziabile.** Avviare il container vocale mentre il modello linguistico occupa la scheda ha saturato la memoria dell'host nel test (30 GB di RAM, lo swap, poi l'OOM killer ha terminato altri servizi): lo scaricamento **deve venire per primo**. Il primo passo di acquisizione che fallisce interrompe l'elenco, quindi nulla si avvia su una scheda piena; e quando un passo di rilascio fallisce, il lock successivo non viene caricato affatto (la GPU potrebbe essere ancora piena) e il job fallisce con l'errore. I passi di rilascio vengono tutti eseguiti, qualunque cosa accada a uno di essi.

Perché *fermare* il container invece di scaricare il modello dentro il server: uno scaricamento dentro il server lasciava occupato circa 1 GB della scheda dopo molti cambi di modello; fermare il container libera tutto.

<a id="the-steps"></a>

### I passi

Ogni passo è un metodo, un indirizzo, le intestazioni, un corpo, un timeout e un'attesa successiva (in secondi); le intestazioni sono memorizzate cifrate, quindi la chiave del proxy può esservi scritta (`Authorization: Bearer ...`). Con un LiteLLM davanti a tutto, i passi del lock vocale sono (*Before 1* a *3* sono i suoi passi di acquisizione, *After 1* il suo passo di rilascio):

| | Metodo e indirizzo | Intestazioni | Corpo | Funzione |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Scarica il modello linguistico; risponde l'hook stesso, impiega da 1 a 2 s. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | Avvia il container vocale e risponde quando il server è pronto (circa 13 s). |
| Before 3 (solo se il server si avvia senza modello) | `POST http://litellm:4000/gpu/tts/load` | | | Carica il modello (da 8 a 14 s, 50 s la prima volta, quando viene scaricato). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Ferma il container (meno di un secondo). |

Per un backend **Higgs** *Before 2* è `POST http://litellm:4000/gpu/comfy/start` (risponde quando ComfyUI è attivo, circa 15 s; il modello viene caricato con il primo pezzo), non c'è *Before 3*, e *After 1* è `POST http://litellm:4000/gpu/comfy/stop`. I due pass-through sono descritti nel README di `tools/gpu-control`.

Il modello linguistico torna da solo: la richiesta ordinaria successiva fa sì che il router lo carichi (circa un minuto per un modello grande; con l'[hook della cache dei prompt](../prompt-cache.md) viene ripristinata la cache della tua sessione), quindi per questo non serve alcuna richiesta dopo il batch.

Senza LiteLLM le stesse richieste vanno a ciò che avvia e ferma i tuoi container; l'unica regola è l'ordine sopra.

<a id="what-answers-those-addresses"></a>

### Chi risponde a questi indirizzi

- **`X-Llama-Unload`** è gestito dall'hook `gpu_unload.py` di LiteLLM, descritto con il suo codice in [Liberare la GPU per un altro modello](../prompt-cache.md#freeing-the-gpu-for-another-model) (funziona anche senza l'hook della cache). Con una route `openai/*` il nome del modello nel corpo deve iniziare con `openai/`: un altro nome riceve 400 *dopo* che i modelli sono stati scaricati.
- **`/gpu/tts/start`, `/stop`, `/load`** sono **endpoint pass-through** di LiteLLM verso il piccolo controller `tools/gpu-control` del repository, che avvia e ferma i container per nome. Il suo README contiene il file compose con il **proxy del socket Docker** (così il controller può avviare e fermare i container e nient'altro; il socket stesso è root sull'host) e le impostazioni che si sbagliano facilmente: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

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

  (il container si chiama `chatterbox` e `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indica al controller che cosa attendere; il token del controller resta nel suo `.env` e nell'ambiente di LiteLLM, Marginalia ha solo la chiave di LiteLLM).
- **Tutti i container e LiteLLM condividono una rete Docker** (`docker network create ai`, una volta sola). Inserisci `networks: [default, ai]` e la rete esterna `ai` in ogni file compose: una `docker network connect` fatta a mano va persa quando un container viene ricreato.

<a id="checking-it"></a>

## Verifica

1. `curl http://<speech server>/api/model-info` risponde (`loaded` è true dopo un avvio).
2. *Test connection* nel backend indica che il modello è caricato. Se il lock della GPU è l'unica cosa che avvia il server, testalo con il lock selezionato: *il server risponde, nessun modello caricato* è la risposta di un server fermato e riavviato che deve ancora caricare il modello.
3. Converti una parte breve con **Convert to audio**. La finestra mostra i pezzi in lavorazione; dopo, il container vocale dovrebbe essere fermo e `nvidia-smi` / `nvtop` / `rocm-smi` mostrano di nuovo la scheda libera.
4. Se la prima chiamata dopo un caricamento del modello richiede molto tempo, è il motore che si scalda (una volta sola, 28 s nella prima esecuzione, non più visto).
<a id="troubleshooting"></a>

## Risoluzione dei problemi

| Sintomo | Causa |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Il server è partito vuoto (un wrapper di avvio scarica il modello una volta): aggiungi il passo **Before 3** ai passi di acquisizione del lock. |
| La prima parte viene prodotta, poi il server dice *Model not loaded* e il suo log mostra `Request received for /api/unload` | Qualcosa di diverso da Marginalia scarica il modello (l'estensione non chiama mai `/api/unload`): di solito un wrapper di avvio nel container che attende che il server risponda e poi scarica il modello "per tenere breve l'avvio". La sua attesa può terminare solo dopo la prima parte, perché il server a thread singolo era occupato, e lo scaricamento arriva quindi a metà del batch (un traceback di timeout di `urllib` nel log del container indica che è il wrapper in attesa). Il pattern da cercare nel file compose è un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Sostituiscilo con `command: ["python3", "server.py"]`: un semplice avvio lascia Turbo caricato, e lo scaricamento serviva solo a liberare la scheda dopo un avvio, cosa che un container avviato e fermato dal controller non richiede. Per capire chi l'ha inviato, leggi il log dell'estensione (*Admin → Extensions → Log*): elenca ogni richiesta prima e dopo un batch, con il suo indirizzo e l'ora della risposta, e indica quando le parti iniziano e finiscono; i passi del lock della GPU sono nel log dell'applicazione. Uno scaricamento che arriva mentre il log dice che il batch è in corso, o da un indirizzo che non compare nell'elenco, non è dell'estensione. |
| Il batch si interrompe a metà (il log dell'estensione dice *Cannot reach*, oppure il log del server semplicemente finisce) e un riavvio manuale funziona | Il server è morto, di solito ucciso dal sistema perché ha occupato tutta la memoria (`dmesg \| grep -i oom` sull'host: un processo `python3` di molti GB). L'estensione allora lo riavvia da sola (le richieste dopo il batch, i passi di rilascio e acquisizione del lock della GPU, poi le richieste prima di esso) e rifà la parte una sola volta; quando il server non può essere riportato in vita, o sparisce di nuovo subito, il resto del batch fallisce immediatamente e il log spiega perché. Per evitare che un server che perde memoria arrivi a tanto, imposta **Restart the server after (pieces)** nel backend (per esempio 100), e un `mem_limit` sul container, così il sistema uccide solo lui. |
| La conversione fallisce subito con la risposta del proxy | Il primo passo di acquisizione del lock della GPU è fallito (una chiave errata, la rotta di scaricamento non trovata); l'elenco si ferma lì di proposito. |
| L'host esaurisce la memoria all'avvio del batch | Il modello linguistico era ancora sulla scheda: il passo di scaricamento manca nel lock della voce o non è il primo, oppure l'AI del modello linguistico non ha un lock sulla stessa GPU. |
| Il giapponese viene letto come se fosse senza senso | `pykakasi` manca nell'immagine del server; anche il backend deve essere quello multilingue per quella parte. |
| Il caricamento di una clip fallisce dietro un proxy | Il proxy deve passare un corpo multipart (`/upload_reference`) senza modifiche. Non testato tramite il pass-through di LiteLLM; usa un file vocale del server (`predefined:Name.wav`) finché non funziona. |
| Il batch di un altro utente scarica il modello sotto il tuo | Ogni backend e ogni AI su una stessa scheda devono indicare un lock dello stesso [GPU](../administration/gpus.md): solo così si alternano. Un backend senza lock non aspetta nessuno. Due installazioni di Marginalia sulla stessa scheda non condividono una GPU. |
