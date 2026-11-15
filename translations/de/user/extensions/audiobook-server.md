---
label: Einrichten des Sprachservers
order: 28
---

# Einrichten des Sprachservers für das Audiobook

Die Erweiterung [Audiobook](audiobook.md) braucht einen **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, MIT-Lizenz), den sie über HTTP erreichen kann. Diese Seite behandelt die Serverseite. Was hier steht, wurde auf einem Rechner ausprobiert (eine AMD Radeon AI PRO R9700 mit 32 GB, LiteLLM vor allem anderen, `llama-server` im Router-Modus). Ihre Hardware und Ihr Proxy brauchen womöglich andere Werte.

<a id="which-setup-is-yours"></a>

## Welche Konfiguration haben Sie?

| Ihre Situation | Was zu tun ist |
|---|---|
| Der Sprachserver läuft auf **einem anderen Rechner**, auf der CPU oder auf einer Karte mit genug Platz **neben dem Sprachmodell** | Den Server starten, seine Adresse in ein Backend eintragen, **keine GPU-Sperre wählen**. Mehr ist nicht nötig. |
| Sprachserver und Sprachmodell **teilen sich eine Karte** und passen nicht gemeinsam hinein | Sie brauchen eine [GPU-Sperre](../administration/gpus.md): Ihre **Acquire**-Schritte geben die Karte frei und starten den Sprachserver, ihre **Release**-Schritte stoppen ihn. Der Rest dieser Seite. |
| Das Sprachmodell ist ein Cloud-Modell | Nichts muss freigegeben werden: Den Sprachserver betreiben Sie, wo Sie möchten, und wählen keine GPU-Sperre. |

Also: **Vorab- und Nachfolgeanfragen (pre- und post-Anfragen) sind nur nötig, wenn die beiden Modelle sich eine GPU abwechselnd teilen.** Sie sorgen für den Wechsel: Die Erweiterung stellt sie einmal um einen *Stapel* (alles, was Sie mit einem Klick gestartet haben) und nicht um jeden einzelnen Teil.

<a id="1-the-speech-server"></a>

## 1. Der Sprachserver

Starten Sie Chatterbox-TTS-Server in einem Container (oder aus dem Quellcode), sodass `GET /api/model-info` antwortet. Wichtig sind folgende Punkte:

- **Das Modell.** Turbo (`chatterbox-turbo`) ist der Standard der Erweiterung und klingt in den Tests am besten. Das mehrsprachige Modell braucht man nur für Text in einer anderen Sprache als der des Buches. Das Modell wird in `config.yaml` des Servers gesetzt
  (oder über `POST /save_settings` und `POST /restart_server`). Das *Speech model* des Backends muss das Modell sein, das der Server nach den Acquire-Schritten seiner GPU-Sperre geladen hat.
- **Japanisch braucht `pykakasi` im Image.** Ohne es meldet der Server im Log *pykakasi not available - Japanese text processing skipped* und liest Kanji falsch. Ergänzen Sie `RUN python3 -m pip install --no-cache-dir pykakasi` im Dockerfile (vor `COPY . .`). Dasselbe stille Überspringen gibt es für die Pakete für Hebräisch, Russisch und Chinesisch.
- **Stimmen** sind Dateien im Ordner `reference_audio/` des Servers (binden Sie ihn ein, damit sie erhalten bleiben). Die Erweiterung lädt die Clips Ihrer Figuren selbst hoch (`POST /upload_reference`), vor jedem Stapel, unter einem Namen, der aus dem Hash des Clips gebildet wird. Der Server hat keinen Lösch-Aufruf: Die Clips häufen sich dort an.
- Der Server hat **keine Authentifizierung**. Veröffentlichen Sie seinen Port nicht. Setzen Sie einen Proxy davor, wenn er nicht in einem privaten Netz liegt.
- `restart: "no"` für einen Container, den etwas anderes bei Bedarf startet. `restart: unless-stopped` startet ihn beim Booten und belegt die Karte unnötig.

Tragen Sie im Backend (*Settings → Extension Settings → Audiobook Settings*) seine Adresse ein. Setzen Sie den Haken bei *The server is on this machine or on my network*, falls das zutrifft, und drücken Sie **Test connection**. Gewünscht ist *the server answers, the model is loaded*.
Die Erweiterung spricht mit `/tts` (die vollständige Parameterliste: Stimme, Sprache, Ausdruck (exaggeration), CFG-Gewicht, Temperatur, Geschwindigkeit, Seed) sowie mit `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Steht ein Proxy zwischen Ihnen und dem Server, muss er all diese Aufrufe durchreichen.

## Higgs Audio v3 in ComfyUI

Ein Backend mit dem Sprachmodell **Higgs Audio v3 in ComfyUI** spricht nicht mit Chatterbox-TTS-Server, sondern mit einem [ComfyUI](https://github.com/comfyanonymous/ComfyUI), in dem die Knoten der [TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite) laufen. Seine Adresse ist die von ComfyUI (zum Beispiel `http://gpu-server:8188`). Eingerichtet wurde es auf derselben AMD R9700 im Image `yanwk/comfyui-boot:rocm`.

1. **Installieren Sie die Suite** in den Ordner `custom_nodes` von ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). Ihre Python-Pakete landen im Container. Damit sie erhalten bleiben, wenn der Container neu erstellt wird, geben Sie pip einen Ordner auf einem Volume: Ergänzen Sie `PIP_USER=true` in der Umgebung des Containers und binden Sie `./storage/local:/root/.local` ein.
2. **Lassen Sie pip niemals das torch des Images ersetzen** (ein CUDA-Build würde den ROCm-Build ersetzen). Schreiben Sie die installierten Versionen in eine Datei und installieren Sie mit ihr als Constraint:
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   Führen Sie `install.py` der Suite nicht als Ganzes aus, denn es könnte torch neu installieren. `librosa` steht nicht in `requirements.txt`, und ohne es lädt die Suite nicht.
3. **Das Modell**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (etwa 8 GB), oder lassen Sie die Suite es beim ersten Mal herunterladen. Es braucht etwa 8 GB der Grafikkarte.
4. **Test connection** im Backend zeigt *the server answers* mit dem Modell des Knotens, wenn ComfyUI und der Knoten vorhanden sind.

Was die Erweiterung damit tut: Sie lädt die Clips der Besetzung in den Eingabeordner von ComfyUI hoch (`POST /upload/image`, unter einem Namen, der aus dem Clip gebildet wird). Sie listet die Stimmen der Suite und die Clips mit `GET /object_info` auf, schickt für jeden Teil einen eigenen Workflow an `/prompt`, wartet auf `/history` und holt das Audio von `/view`. Mit ffmpeg auf der Marginalia-Seite kommt es als flac, wird nach dem letzten Wort gekürzt, ausgeblendet und als mp3 oder wav gespeichert. Ohne ffmpeg kommt es als mp3 von ComfyUI, ungekürzt (die Speicherung als wav funktioniert dann nicht). **Nach jedem Teil ruft sie `POST /free` mit `{"free_memory": true}` auf**: Auf der R9700 brachte der dritte Teil in Folge auf dem geladenen Modell die Grafikkarte zum Absturz (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI beendet sich). Das Leeren des Speichers zwischen den Teilen hält das Modell geladen und vermeidet den Absturz, kostet aber etwa 3 Sekunden pro Teil. ComfyUI wird um einen Stapel herum mit derselben Art von Anfragen gestartet und gestoppt wie der Sprachserver ([die Anfragen](#the-requests)).

**Verwenden Sie fp32 auf einer AMD-Karte.** Mit dem Wert `auto` für Precision (bf16 auf der R9700 mit ROCm 7.2) kamen viele Teile als Kauderwelsch heraus, als Soundeffekte (eine Zeile über Klopfen wurde zu elf Schlägen) oder mit einer falschen Stimme. Das Ändern des Seeds änderte nur das Glück. Dieselben Teile in fp32 waren wortgenau. Die **Precision** des Backends steht standardmäßig auf fp32; sie braucht etwa 16 GB der Karte. Die Granite-Spracherkennung der Suite hat dasselbe Problem in bf16 (ab der zweiten Erkennung Kauderwelsch), deshalb führt die Erweiterung sie ebenfalls in float32 aus.

Die Engine **Fish Audio S2 Pro** der Suite funktionierte auf dieser Karte nicht: Ihr Audio kam als ungültige Zahlen (NaN) heraus. Die Gewichte von Higgs Audio v3 stehen unter einer Lizenz für Forschung und nichtkommerzielle Nutzung.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## Eine GPU für das Modell und die Sprache

Eine Karte, die das Sprachmodell hält, hat keinen Platz für das Sprachmodell zur Sprachausgabe. Der Administrator richtet den Wechsel einmal unter
[Admin → GPUs](../administration/gpus.md) ein, mit einer GPU und einer Sperre für jedes Element, das auf ihr geladen werden kann, und wählt die Sperre in der KI des
Sprachmodells und im Sprach-Backend aus. Der Ablauf ist für jeden Proxy gleich:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

Ein Batch (oder ein Sample) des Sprach-Backends führt die Acquire-Schritte der Sprachsperre aus, wenn die GPU das Sprachmodell hält, danach alle
Teile; die GPU bleibt nach dem Start mit dem Sprachserver geladen. Die nächste Anfrage an das Sprachmodell wartet auf den Batch, führt den
Release-Schritt der Sprachsperre aus (Server stoppen) und läuft weiter. Mehrere Batches und Samples hintereinander bezahlen den Wechsel nur einmal.

**Die Reihenfolge ist nicht verhandelbar.** Den Sprachcontainer zu starten, während das Sprachmodell die Karte belegt, hat im Test den Arbeitsspeicher des Hosts gefüllt (30 GB RAM, dann der Swap, dann hat der OOM-Killer andere Dienste beendet): Das Entladen **muss zuerst** kommen. Der erste fehlgeschlagene Acquire-Schritt beendet die Liste, sodass nichts auf einer vollen Karte startet; und wenn ein Release-Schritt fehlschlägt, wird die nächste Sperre nicht geladen (die GPU könnte noch voll sein), und der Job schlägt mit dem Fehler fehl. Die Release-Schritte laufen alle, egal was mit einem von ihnen passiert.

Warum den Container *stoppen*, statt das Modell im Server zu entladen? Ein Entladen im Server ließ nach vielen Modellwechseln etwa 1 GB der Karte belegt; Stoppen des Containers gibt alles frei.

<a id="the-steps"></a>

### Die Schritte

Jeder Schritt besteht aus einer Methode, einer Adresse, Headern, einem Body, einem Timeout und einer Wartezeit danach (Sekunden); die Header werden verschlüsselt gespeichert, daher kann der Schlüssel des
Proxys dort eingetragen werden (`Authorization: Bearer ...`). Mit einem LiteLLM davor sind die Schritte der Sprachsperre (*Before 1*
bis *3* sind ihre Acquire-Schritte, *After 1* ihr Release-Schritt):

| | Methode und Adresse | Header | Body | Aktion |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Entlädt das Sprachmodell; wird vom Hook selbst beantwortet, dauert 1 bis 2 s. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | Startet den Sprachcontainer und antwortet, wenn der Server bereit ist (etwa 13 s). |
| Before 3 (nur wenn der Server ohne Modell startet) | `POST http://litellm:4000/gpu/tts/load` | | | Lädt das Modell (8 bis 14 s, beim ersten Mal wegen des Downloads 50 s). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Stoppt den Container (unter einer Sekunde). |

Für ein **Higgs**-Backend ist *Before 2* `POST http://litellm:4000/gpu/comfy/start` (es antwortet, wenn ComfyUI läuft, etwa 15 s; das Modell wird mit dem ersten Teil geladen), es gibt kein *Before 3*, und *After 1* ist `POST http://litellm:4000/gpu/comfy/stop`. Die beiden Pass-throughs sind in der README von `tools/gpu-control` beschrieben.

Das Sprachmodell kommt von selbst zurück: Die nächste gewöhnliche Anfrage lässt den Router es laden (etwa eine Minute bei einem großen Modell; mit dem [Prompt-Cache-Hook](../prompt-cache.md) wird der Cache deiner Sitzung wiederhergestellt), daher ist für dieses Modell nach dem Batch keine Anfrage nötig.

Ohne LiteLLM gehen dieselben Anfragen an das, was deine Container startet und stoppt; die einzige Regel ist die oben genannte Reihenfolge.

<a id="what-answers-those-addresses"></a>

### Was auf diese Adressen antwortet

- **`X-Llama-Unload`** wird vom Hook `gpu_unload.py` von LiteLLM beantwortet, beschrieben mit seinem Code in
  [GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model) (es funktioniert auch ohne den Cache-Hook). Mit einer Route `openai/*` muss der Modellname im Body mit `openai/` beginnen: ein anderer Name wird mit 400 beantwortet, *nachdem* die Modelle entladen wurden.
- **`/gpu/tts/start`, `/stop`, `/load`** sind LiteLLM-**Pass-through-Endpunkte** zum kleinen Controller `tools/gpu-control` des Repositorys, der Container anhand ihres Namens startet und stoppt. Seine
  README enthält die Compose-Datei mit dem **Docker-Socket-Proxy** (damit der Controller Container nur starten und stoppen und sonst nichts tun kann; der Socket selbst ist auf dem Host root) und die Einstellungen, die leicht falsch gemacht werden: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

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

  (der Container heißt `chatterbox`, und `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` legt fest, worauf der Controller wartet; das Token des Controllers bleibt in seiner `.env` und in der Umgebung von LiteLLM, Marginalia kennt nur den Schlüssel von LiteLLM).
- **Alle Container und LiteLLM teilen sich ein Docker-Netzwerk** (`docker network create ai`, einmalig). Trage `networks: [default, ai]` und das externe Netzwerk `ai` in jede Compose-Datei ein; eine von Hand gemachte `docker network connect` geht verloren, wenn ein Container neu erstellt wird.

<a id="checking-it"></a>

## Prüfen

1. `curl http://<speech server>/api/model-info` antwortet (`loaded` ist nach einem Start true).
2. *Test connection* im Backend meldet, dass das Modell geladen ist. Wenn die GPU-Sperre das Einzige ist, das den Server startet, teste mit ausgewählter Sperre: *the server answers, no model is loaded* ist die Antwort eines gestoppten und wieder gestarteten Servers, der das Modell noch laden muss.
3. Konvertiere einen kurzen Teil in **Convert to audio**. Das Fenster zeigt, wie die Teile erzeugt werden; danach sollte der Sprachcontainer gestoppt sein, und `nvidia-smi` / `nvtop` / `rocm-smi` zeigen die Karte wieder frei.
4. Wenn der erste Aufruf nach dem Laden eines Modells lange dauert, ist das das Aufwärmen der Engine (einmalig, beim ersten Lauf 28 s, danach nicht mehr beobachtet).
<a id="troubleshooting"></a>

## Fehlerbehebung

| Symptom | Ursache |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Der Server ist leer gestartet (ein Startskript, das das Modell einmalig entlädt). Füge der Sperre die Stufe **Before 3** zu den Schritten zum Erwerben der Sperre hinzu. |
| Die ersten Teile werden erstellt, dann meldet der Server *Model not loaded*, und sein Log zeigt `Request received for /api/unload` | Etwas anderes als Marginalia entlädt das Modell (die Erweiterung ruft `/api/unload` nie auf). Typischerweise ist es ein Startskript im Container, das wartet, bis der Server antwortet, und dann das Modell entlädt, "um den Start schlank zu halten". Sein Warten kann erst nach dem ersten Teil enden, weil der Single-Thread-Server beschäftigt war. Das Entladen landet dann mitten im Stapel (ein `urllib`-Timeout-Traceback im Container-Log ist das Warten dieses Skripts). Das Muster, nach dem man in der Compose-Datei suchen sollte, ist ein `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Ersetze es durch `command: ["python3", "server.py"]`: Ein Start allein liefert einen geladenen Turbo. Das Entladen diente nur dazu, die Karte nach dem Booten frei zu halten, was ein Container, den der Controller startet und stoppt, nicht braucht. Um herauszufinden, wer das Entladen geschickt hat, lies das Log der Erweiterung (*Admin → Extensions → Log*). Es listet jede Anfrage vor und nach einem Stapel mit ihrer Adresse und dem Zeitpunkt der Antwort sowie Beginn und Ende der Teile. Die Schritte der GPU-Sperre stehen im Anwendungslog. Ein Entladen, das kommt, während das Log sagt, dass der Stapel läuft, oder von einer Adresse, die nicht in der Liste steht, stammt nicht von der Erweiterung. |
| Der Stapel bricht mitten drin ab (das Log der Erweiterung meldet *Cannot reach*, oder das Log des Servers endet einfach), und ein Neustart von Hand funktioniert | Der Server ist abgestürzt, typischerweise vom System beendet, weil er den gesamten Speicher belegt hat (`dmesg \| grep -i oom` auf dem Host: ein `python3`-Prozess mit vielen GB). Die Erweiterung startet ihn dann selbst neu: die Anfragen nach dem Stapel, die Freigabe- und Anforderungsschritte der GPU-Sperre, dann die Anfragen davor. Sie erstellt das Teil danach einmal neu. Kann der Server nicht zurückgeholt werden oder ist er sofort wieder weg, schlägt der Rest des Stapels sofort fehl, und das Log sagt, warum. Damit ein leckender Server es nicht so weit kommen lässt, setze **Restart the server after (pieces)** im Backend (zum Beispiel auf 100) und ein `mem_limit` am Container, damit das System nur ihn beendet. |
| Die Konvertierung schlägt sofort mit der Antwort des Proxys fehl | Der erste Anforderungsschritt der GPU-Sperre ist fehlgeschlagen (ein falscher Schlüssel, die Entlade-Route nicht gefunden). Die Liste hält dort absichtlich an. |
| Dem Host geht der Speicher aus, wenn der Stapel startet | Das Sprachmodell war noch auf der Karte. Der Entladeschritt fehlt in der Sperre für die Sprachausgabe oder steht nicht an erster Stelle, oder die KI des Sprachmodells hat keine Sperre für dieselbe GPU. |
| Japanisch wird als Unsinn gelesen | `pykakasi` fehlt im Image des Servers. Das Backend muss für dieses Teil außerdem das mehrsprachige sein. |
| Der Upload eines Clips schlägt hinter einem Proxy fehl | Der Proxy muss einen Multipart-Body (`/upload_reference`) unverändert durchreichen. Nicht getestet über das LiteLLM-Pass-through. Bis es funktioniert, eine Stimmdatei des Servers (`predefined:Name.wav`) verwenden. |
| Der Stapel eines anderen Nutzers entlädt das Modell unter deinem | Jedes Backend und jede KI auf einer Karte muss eine Sperre derselben [GPU](../administration/gpus.md) angeben. Nur dann wechseln sie sich ab. Ein Backend ohne Sperre wartet auf niemanden. Zwei Marginalia-Installationen auf einer Karte teilen sich keine GPU. |
