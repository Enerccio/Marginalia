---
label: Einrichten des Sprachservers
order: 28
---

# Einrichten des Sprachservers für das Hörbuch

Die Erweiterung [Hörbuch](audiobook.md) braucht einen **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, MIT-Lizenz), den sie per HTTP erreichen kann. Diese Seite behandelt die Serverseite. Was hier steht, wurde auf einem Rechner ausprobiert (eine AMD Radeon AI PRO R9700
mit 32 GB, LiteLLM davor, `llama-server` im Router-Modus). Ihre Hardware und Ihr Proxy brauchen womöglich andere Werte.

<a id="which-setup-is-yours"></a>

## Welche Konfiguration passt zu Ihnen?

| Ihre Situation | Was zu tun ist |
|---|---|
| Der Sprachserver läuft auf **einem anderen Rechner**, auf der CPU oder auf einer Karte mit genug Platz **neben dem Sprachmodell** | Den Server starten, seine Adresse in ein Backend eintragen, **keine GPU-Sperre wählen**. Mehr ist nicht nötig. |
| Der Sprachserver und das Sprachmodell **teilen sich eine Karte** und passen nicht zusammen | Sie brauchen eine [GPU-Sperre](../administration/gpus.md): Ihre **Acquire**-Schritte geben die Karte frei und starten den Sprachserver, ihre **Release**-Schritte stoppen ihn. Der Rest dieser Seite. |
| Das Sprachmodell ist ein Cloud-Modell | Es gibt nichts freizugeben: Starten Sie den Sprachserver, wo Sie möchten, und wählen Sie keine GPU-Sperre. |

Also: **Anfragen davor und danach sind nur nötig, wenn sich die beiden Modelle eine GPU teilen.** Sie sorgen für den Wechsel: Das Plugin stellt sie einmal um einen *Stapel* (alles, was Sie mit einem Klick gestartet haben), nicht um jeden einzelnen Teil.

<a id="1-the-speech-server"></a>

## 1. Der Sprachserver

Starten Sie Chatterbox-TTS-Server in einem Container (oder aus dem Quellcode), sodass `GET /api/model-info` antwortet. Wichtig sind:

- **Das Modell.** Turbo (`chatterbox-turbo`) ist der Standard der Erweiterung und klingt in den Tests am besten. Das mehrsprachige Modell braucht man nur für Text in einer anderen Sprache als der des Buchs. Das Modell wird in der Konfiguration des Servers über `config.yaml`
  (oder `POST /save_settings` und `POST /restart_server`) festgelegt. Das *Sprachmodell* des Backends muss das Modell sein, das der Server nach den Acquire-Schritten seiner GPU-Sperre geladen hat.
- **Japanisch braucht `pykakasi` im Image.** Ohne dieses Paket meldet der Server *pykakasi not available - Japanese text processing skipped* und liest Kanji falsch. Fügen Sie `RUN python3 -m pip install --no-cache-dir pykakasi` in das Dockerfile ein (vor `COPY . .`). Dasselbe stille Überspringen gibt es für die Pakete für Hebräisch, Russisch und Chinesisch.
- **Stimmen** sind Dateien im Ordner `reference_audio/` des Servers (binden Sie ihn ein, damit sie erhalten bleiben). Die Erweiterung lädt die Clips Ihrer Figuren selbst hoch (`POST /upload_reference`), bevor ein Stapel läuft, unter einem Namen, der aus dem Hash des Clips gebildet wird. Der Server hat keine Löschfunktion: Die Clips sammeln sich dort an.
- Der Server hat **keine Authentifizierung**. Veröffentlichen Sie seinen Port nicht. Setzen Sie einen Proxy davor, wenn er nicht in einem privaten Netz läuft.
- `restart: "no"` für einen Container, den etwas anderes bei Bedarf startet. `restart: unless-stopped` startet ihn beim Systemstart und belegt die Karte dauerhaft.

Im Backend (*Settings → Extension Settings → Audiobook Settings*) tragen Sie die Adresse ein, setzen den Haken bei *The server is on this machine or on my network*, falls das zutrifft, und klicken auf **Test connection**. Gewünscht ist: *the server answers, the model is loaded*.
Die Erweiterung spricht mit `/tts` (der vollständige Satz der Parameter: Stimme, Sprache, Übertreibung, CFG-Gewicht, Temperatur, Geschwindigkeit, Seed) sowie mit `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Wenn zwischen Ihnen und dem Server ein Proxy steht, muss er alle diese Aufrufe durchlassen.

## Higgs Audio v3 in ComfyUI

Ein Backend mit dem Sprachmodell **Higgs Audio v3 in ComfyUI** spricht mit einem [ComfyUI](https://github.com/comfyanonymous/ComfyUI) mit den Knoten der
[TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite), nicht mit Chatterbox-TTS-Server. Seine Adresse ist die von ComfyUI (zum Beispiel
`http://gpu-server:8188`). Eingerichtet wurde es auf derselben AMD R9700 im Image `yanwk/comfyui-boot:rocm`.

1. **Installieren Sie die Suite** in den Ordner `custom_nodes` von ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). Ihre Python-Pakete landen im
   Container. Damit sie erhalten bleiben, wenn der Container neu erstellt wird, geben Sie pip einen Ordner auf einem Volume: Fügen Sie `PIP_USER=true` zur Umgebung des Containers hinzu und binden Sie
   `./storage/local:/root/.local` ein.
2. **Lassen Sie pip niemals das torch des Images ersetzen** (ein CUDA-Build würde den ROCm-Build verdrängen). Schreiben Sie die installierten Versionen in eine Datei und installieren Sie mit ihr als Constraint:
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   Führen Sie `install.py` der Suite nicht als Ganzes aus: Es kann torch neu installieren. `librosa` steht nicht in ihrer `requirements.txt`, und ohne es lädt die Suite nicht.
3. **Das Modell**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (etwa 8 GB), oder Sie lassen die
   Suite es beim ersten Mal herunterladen. Es braucht etwa 8 GB der Grafikkarte.
4. **Test connection** im Backend meldet *the server answers*, mit dem Modell des Knotens, wenn ComfyUI und der Knoten vorhanden sind.

Was die Erweiterung damit macht: Sie lädt die Clips der Besetzung in den Eingabeordner von ComfyUI hoch (`POST /upload/image`, unter einem Namen, der aus dem Clip gebildet wird), listet
die Stimmen der Suite und die Clips mit `GET /object_info` auf, schickt für jeden Abschnitt einen eigenen Workflow an `/prompt`, wartet auf `/history` und holt das Audio
von `/view` ab. Der Workflow gibt außerdem die Generierungsinformationen des TTS-Knotens an den Kernknoten `PreviewAny` weiter: Die Suite meldet einen Fehler mit einer Sekunde Stille und einem erfolgreichen Lauf, und nur diese Information zeigt, dass der Abschnitt fehlgeschlagen ist. Er schlägt dann mit dem Grund fehl, statt behalten zu werden. Mit ffmpeg auf der Marginalia-Seite wird er als FLAC übernommen, nach dem letzten Wort geschnitten, ausgeblendet und als MP3 oder WAV gespeichert. Ohne ffmpeg kommt das MP3 von ComfyUI ungeschnitten an (die Speicherung als WAV funktioniert dann nicht). **Nach jedem Abschnitt ruft sie `POST /free` mit
`{"free_memory": true}` auf**: Auf der R9700 brachte der dritte Abschnitt in Folge bei geladenem Modell die Grafikkarte zum Absturz (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI beendet sich).
Das Leeren des Speichers zwischen den Abschnitten hält das Modell geladen und verhindert den Absturz, kostet aber etwa 3 Sekunden pro Abschnitt. ComfyUI wird vor und nach einem Stapel mit derselben Art von Anfragen gestartet und gestoppt wie der Sprachserver ([die Anfragen](#the-requests)).

**Verwenden Sie fp32 auf einer AMD-Karte.** Mit der Präzision `auto` (bf16 auf der R9700 mit ROCm 7.2) kamen viele Abschnitte als Kauderwelsch heraus, als Soundeffekte (eine Zeile über Klopfen wurde zu elf Klopfgeräuschen) oder in einer falschen Stimme. Das Ändern des Seeds half nur bei der Trefferquote. Dieselben Abschnitte in fp32 waren wortgetreu. Die **Präzision** des Backends ist standardmäßig fp32; sie braucht etwa 16 GB der Karte. Die Granite-Spracherkennung der Suite hat dasselbe Problem in bf16 (ab der zweiten Erkennung Kauderwelsch), daher führt die Erweiterung sie ebenfalls in float32 aus.

**Die Bereinigung von Clips** ([Bereinigung von Clips](audiobook.md#cleanup-of-clips)) verwendet zwei weitere Knoten derselben Suite, `VocalRemovalNode` (Trennmodelle)
und `VoiceFixerNode`, mit `LoadAudio` und `SaveAudio` von ComfyUI. Ein weiteres Knotenpaket ist nicht nötig. Die Suite lädt beim ersten Gebrauch ein Modell herunter (die MelBand-Roformer-Modelle sind je etwa 900 MB groß). Auf der R9700 funktionierten die MelBand-Modelle (Gesang, Rauschunterdrückung) und der Voice Fixer, etwa 25 Sekunden für einen 16-Sekunden-Clip. Die UVR-Modelle funktionierten zunächst nicht:

- `UVR-DeEcho-DeReverb` (verwendet von *Remove echo* und *Film or game clip*) schlug mit `module 'samplerate' has no attribute 'resample'` fehl. Der Container hatte `samplerate` 0.1.0, einen Wrapper, der die Systembibliothek `libsamplerate` braucht, die dem Image fehlt. Das aktuelle Paket bringt seine eigene mit. Installieren Sie es
  mit dem torch-Pin und starten Sie ComfyUI neu:
  ```sh
  PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install --user 'samplerate>=0.2.1'
  ```
  Danach dauerte die Nachhallentfernung etwa 5 Sekunden für einen 16-Sekunden-Clip.
- `UVR-DeNoise` ließ sich nicht herunterladen (die Adresse der Suite antwortete mit 404). Legen Sie die Datei von Hand in `models/TTS/UVR/`, um sie zu verwenden. Das MelBand-Modell zur Rauschunterdrückung braucht diesen Schritt nicht und gehört zu den Standardbereinigungen.

Die Engine **Fish Audio S2 Pro** der Suite funktionierte auf dieser Karte nicht: Ihr Audio kam als ungültige Zahlen (NaN) heraus. Die Gewichte von Higgs Audio v3 stehen unter einer Lizenz für Forschung und nichtkommerzielle Nutzung.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## Eine GPU für das Sprachmodell und die Sprachausgabe

Eine Karte, die das Sprachmodell hält, hat keinen Platz für das Sprachausgabe-Modell. Der Administrator richtet den Wechsel einmalig unter [Admin → GPUs](../administration/gpus.md) ein, mit einer GPU und einer Sperre für jedes Element, das darauf geladen werden kann. In der KI des Sprachmodells und im Sprachausgabe-Backend wählt er die Sperre aus. Der Plan ist für jeden Proxy derselbe:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

Ein Batch (oder ein Sample) des Sprachausgabe-Backends führt die Belegungsschritte der Sprachausgabe-Sperre aus, wenn die GPU das Sprachmodell hält, und danach alle Teile. Danach bleibt die GPU mit dem Sprachausgabe-Server geladen. Die nächste Anfrage an das Sprachmodell wartet auf den Batch, führt den Freigabeschritt der Sprachausgabe-Sperre aus (Server stoppen) und läuft weiter. Mehrere Batches und Samples hintereinander bezahlen den Wechsel nur einmal.

**Die Reihenfolge ist nicht verhandelbar.** Den Sprachausgabe-Container zu starten, während das Sprachmodell die Karte belegt, hat im Test den Speicher des Hosts gefüllt (30 GB RAM, dann der Swap, danach hat der OOM-Killer andere Dienste beendet). Das Entladen **muss zuerst** kommen. Der erste fehlschlagende Belegungsschritt beendet die Liste, sodass nichts auf einer vollen Karte startet. Wenn ein Freigabeschritt fehlschlägt, wird die nächste Sperre gar nicht geladen (die GPU könnte noch voll sein), und der Job schlägt mit dem Fehler fehl. Die Freigabeschritte laufen alle, egal was mit einem von ihnen passiert.

**Higgs**, die Spracherkennung und die Bereinigung von Clips auf einem ComfyUI erhalten jeweils eine eigene Sperre. Alle drei haben dieselben Schritte (Sprachmodell entladen, ComfyUI starten; Freigabe: ComfyUI stoppen). ComfyUI behält ein entladenes Modell im Speicher des Servers (fp32-Higgs braucht etwa 16 GB). Das Laden der Spracherkennung daneben hat im Test den Server aus dem Speicher laufen lassen. Bei drei Sperren startet jeder Wechsel ComfyUI neu, sodass immer nur eines davon geladen ist.

Warum den Container *stoppen*, statt das Modell im Server zu entladen: Ein Entladen im Server hat nach vielen Modellwechseln etwa 1 GB der Karte belegt gelassen. Das Stoppen des Containers gibt alles frei.

<a id="the-steps"></a>

### Die Schritte

Jeder Schritt besteht aus einer Methode, einer Adresse, Headern, einem Body, einem Timeout und einer Wartezeit danach (in Sekunden). Die Header werden verschlüsselt gespeichert, daher kann der Schlüssel des Proxys in sie eingetragen werden (`Authorization: Bearer ...`). Mit einem LiteLLM vor allem stehen, sind die Schritte der Sprachausgabe-Sperre folgende (*Vorher 1* bis *3* sind ihre Belegungsschritte, *Nachher 1* ihr Freigabeschritt):

| | Methode und Adresse | Header | Body | Wirkung |
|---|---|---|---|---|
| Vorher 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Entlädt das Sprachmodell; wird vom Hook selbst beantwortet, dauert 1 bis 2 s. |
| Vorher 2 | `POST http://litellm:4000/gpu/tts/start` | | | Startet den Sprachausgabe-Container und antwortet, wenn der Server bereit ist (etwa 13 s). |
| Vorher 3 (nur wenn der Server ohne Modell startet) | `POST http://litellm:4000/gpu/tts/load` | | | Lädt das Modell (8 bis 14 s, beim ersten Mal mit Download 50 s). |
| Nachher 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Stoppt den Container (unter einer Sekunde). |

Bei einem **Higgs**-Backend ist *Vorher 2* `POST http://litellm:4000/gpu/comfy/start` (antwortet, wenn ComfyUI läuft, etwa 15 s; das Modell wird mit dem ersten Teil geladen). Es gibt kein *Vorher 3*, und *Nachher 1* ist `POST http://litellm:4000/gpu/comfy/stop`. Die beiden Pass-throughs stehen in der README von `tools/gpu-control`.

Das Sprachmodell kommt von selbst zurück: Die nächste normale Anfrage lässt den Router es laden (etwa eine Minute bei einem großen Modell; mit dem [Prompt-Cache-Hook](../prompt-cache.md) wird der Cache der Sitzung wiederhergestellt). Dafür ist deshalb keine Anfrage nach dem Batch nötig.

Ohne LiteLLM gehen dieselben Anfragen an das, was deine Container startet und stoppt. Die einzige Regel ist die Reihenfolge oben.

<a id="what-answers-those-addresses"></a>

### Was auf diese Adressen antwortet

- **`X-Llama-Unload`** wird vom Hook `gpu_unload.py` von LiteLLM beantwortet. Er ist mit seinem Code in [GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model) beschrieben (er funktioniert auch ohne den Cache-Hook). Bei einer Route `openai/*` muss der Modellname im Body mit `openai/` beginnen: Ein anderer Name wird mit 400 beantwortet, *nachdem* die Modelle entladen wurden.
- **`/gpu/tts/start`, `/stop`, `/load`** sind LiteLLM-**Pass-through-Endpunkte** zum kleinen Controller `tools/gpu-control` des Repositorys, der Container anhand ihres Namens startet und stoppt. Seine README enthält die Compose-Datei mit dem **Docker-Socket-Proxy** (damit der Controller Container nur starten und stoppen und sonst nichts tun kann; der Socket selbst ist auf dem Host Root) sowie die Einstellungen, die leicht falsch werden: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

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

  (der Container heißt `chatterbox`, und `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` sagt dem Controller, worauf er warten soll; das Token des Controllers bleibt in seinem `.env` und in der Umgebung von LiteLLM, Marginalia hat nur den Schlüssel von LiteLLM).
- **Alle Container und LiteLLM teilen sich ein Docker-Netzwerk** (`docker network create ai`, einmalig). Trage `networks: [default, ai]` und das externe Netzwerk `ai` in jede Compose-Datei ein. Ein von Hand erstelltes `docker network connect` geht verloren, wenn ein Container neu erstellt wird.

<a id="checking-it"></a>

## Prüfen

1. `curl http://<speech server>/api/model-info` antwortet (`loaded` ist nach einem Start true).
2. *Test connection* im Backend meldet, dass das Modell geladen ist. Wenn die GPU-Sperre das Einzige ist, das den Server startet, teste mit ausgewählter Sperre: *der Server antwortet, kein Modell geladen* ist die Antwort eines gestoppten und neu gestarteten Servers, der das Modell noch laden muss.
3. Konvertiere einen kurzen Teil unter **Convert to audio**. Das Fenster zeigt die entstehenden Teile. Danach sollte der Sprachausgabe-Container gestoppt sein, und `nvidia-smi` / `nvtop` / `rocm-smi` zeigen die Karte wieder als frei.
4. Dauert der erste Aufruf nach dem Laden eines Modells lange, wärmt sich die Engine auf. Das passiert einmalig (im ersten Lauf 28 s) und wurde danach nicht mehr beobachtet.
<a id="troubleshooting"></a>

## Fehlerbehebung

| Symptom | Ursache |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Der Server wurde leer gestartet (ein Start-Wrapper, der das Modell einmal entlädt). Fügen Sie den Schritt **Before 3** zu den Acquire-Schritten des Locks hinzu. |
| Die ersten Stücke werden erzeugt, dann meldet der Server *Model not loaded*, und sein Log zeigt `Request received for /api/unload` | Etwas außer Marginalia entlädt das Modell (die Erweiterung ruft `/api/unload` nie auf). Typischerweise ist es ein Start-Wrapper im Container, der wartet, bis der Server antwortet, und das Modell dann entlädt, um „den Start günstig zu halten“. Sein Warten kann erst nach dem ersten Stück enden, weil der Server mit nur einem Thread beschäftigt war. Das Entladen landet dann mitten im Batch. Ein `urllib`-Timeout-Traceback im Container-Log ist das Warten dieses Wrappers. Das Muster, nach dem Sie in der Compose-Datei suchen müssen, ist ein `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Ersetzen Sie es durch `command: ["python3", "server.py"]`: Ein Start allein liefert einen geladenen Turbo, und das Entladen diente nur dazu, die Karte nach einem Booten frei zu halten. Ein Container, den der Controller startet und stoppt, braucht das nicht. Um herauszufinden, wer die Anfrage gesendet hat, lesen Sie das Log der Erweiterung (*Admin → Extensions → Log*). Es listet jede Anfrage vor und nach einem Batch mit Adresse und Zeitpunkt der Antwort sowie Beginn und Ende der Stücke. Die Schritte des GPU-Locks stehen im Anwendungslog. Ein Entladen, das kommt, während das Log sagt, dass der Batch läuft, oder von einer Adresse, die nicht in der Liste steht, stammt nicht von der Erweiterung. |
| Der Batch bricht mitten ab (das Log der Erweiterung meldet *Cannot reach*, oder das Log des Servers endet einfach), und ein Neustart von Hand funktioniert | Der Server ist abgestürzt, typischerweise vom System beendet, weil er den gesamten Speicher belegt hat (`dmesg \| grep -i oom` auf dem Host: ein `python3`-Prozess mit vielen GB). Die Erweiterung startet ihn dann selbst neu: zuerst die Anfragen nach dem Batch, dann die Release- und Acquire-Schritte des GPU-Locks, dann die Anfragen davor. Anschließend erzeugt sie das Stück noch einmal. Kann der Server nicht zurückgeholt werden oder ist er sofort wieder weg, schlagen der Rest des Batches sofort fehl, und das Log sagt, warum. Damit ein undichter Server es nicht so weit kommen lässt, setzen Sie im Backend **Restart the server after (pieces)** (zum Beispiel auf 100) und ein `mem_limit` am Container, damit das System nur diesen Prozess beendet. |
| Higgs auf ComfyUI: Nach etwa 15 Stücken scheitert jedes Stück mit *Higgs failed on the server: CUDA out of memory* (`Tried to allocate 1.45 GiB`), bis ComfyUI neu gestartet wird | Der Audio-Codec von Higgs wird nie freigegeben. transformers 5.15.1 behält jeden geladenen Codec in einem Cache (`@lru_cache` auf einer Methode von `HiggsAudioV2TokenizerModel`). Das sind etwa 770 MB der Karte in fp32 für jedes Neuladen des Modells, und die Suite lädt es für jedes Stück neu, sobald ComfyUI es entladen hat. Setzen Sie im Backend **Restart the server after (pieces)** auf 10, oder leeren Sie diesen Cache, wenn die Suite das Modell freigibt (`HiggsAudioV2TokenizerModel._get_conv1d_layers.cache_clear()`), bis transformers das behebt. |
| Die Konvertierung scheitert sofort mit der Antwort des Proxys | Der erste Acquire-Schritt des GPU-Locks ist fehlgeschlagen (ein falscher Schlüssel, die Entlade-Route nicht gefunden). Die Liste hält dort absichtlich an. |
| Der Host läuft beim Start des Batches aus dem Speicher | Das Sprachmodell war noch auf der Karte. Entweder fehlt der Entladeschritt im Sprach-Lock, oder er steht nicht an erster Stelle, oder die KI des Sprachmodells hat keinen Lock derselben GPU. |
| Japanisch wird als Unsinn gelesen | `pykakasi` fehlt im Image des Servers. Das Backend muss für dieses Stück außerdem das mehrsprachige sein. |
| Der Upload eines Clips scheitert hinter einem Proxy | Der Proxy muss einen Multipart-Body (`/upload_reference`) unverändert weiterleiten. Nicht über LiteLLM-Pass-through getestet. Verwenden Sie bis dahin eine Stimmdatei des Servers (`predefined:Name.wav`). |
| Der Batch eines anderen Benutzers entlädt das Modell unter Ihrem | Jedes Backend und jede KI auf einer Karte muss einen Lock derselben [GPU](../administration/gpus.md) benennen. Nur dann wechseln sie sich ab. Ein Backend ohne Lock wartet auf niemanden. Zwei Marginalia-Installationen auf einer Karte teilen sich keine GPU. |
