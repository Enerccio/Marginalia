---
label: Einrichtung des Sprachservers
order: 28
---

# Einrichtung des Sprachservers für das Hörbuch

Die [Hörbuch](audiobook.md)-Erweiterung braucht einen **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, MIT-Lizenz), den sie per HTTP erreichen kann. Diese Seite beschreibt die Serverseite. Was hier steht, wurde auf einem Rechner erprobt (eine AMD Radeon AI PRO R9700 mit 32 GB, LiteLLM davor, `llama-server` im Router-Modus). Deine Hardware und dein Proxy brauchen unter Umständen andere Werte.

<a id="which-setup-is-yours"></a>

## Welches Setup ist deins?

| Deine Situation | Was zu tun ist |
|---|---|
| Der Sprachserver läuft auf **einem anderen Rechner**, auf der CPU oder auf einer Karte mit genug Platz **neben dem Sprachmodell** | Den Server starten, seine Adresse in ein Backend eintragen, **die Anfragen vor und nach einem Stapel leer lassen**. Mehr ist nicht nötig. |
| Sprachserver und Sprachmodell **teilen sich eine Karte** und passen nicht zusammen | Du brauchst die Anfragen vor und nach einem Stapel: **davor** gibt die Karte frei und startet den Sprachserver, **danach** stoppt er ihn. Der Rest dieser Seite. |
| Das Sprachmodell ist ein Cloud-Modell | Es gibt nichts freizugeben: Den Sprachserver betreibst du, wo du willst, und die Anfragen bleiben leer. |

Also: **Ja, Anfragen vor und nach einem Stapel braucht man nur, wenn sich beide Modelle abwechselnd eine GPU teilen.** Sie sorgen für den Wechsel. Das Plugin führt sie einmal um einen *Stapel* herum aus (alles, was du mit einem Klick gestartet hast), nicht um jeden einzelnen Teil.

<a id="1-the-speech-server"></a>

## 1. Der Sprachserver

Chatterbox-TTS-Server in einem Container (oder aus dem Quellcode) starten, sodass `GET /api/model-info` antwortet. Wichtig sind:

- **Das Modell.** Turbo (`chatterbox-turbo`) ist der Standard der Erweiterung und klingt in den Tests am besten. Das mehrsprachige Modell braucht man nur für Text in einer anderen Sprache als der des Buches. Das Modell wird in der `config.yaml` des Servers gesetzt (oder über `POST /save_settings` und `POST /restart_server`). Das *Speech model* des Backends muss das Modell sein, das der Server nach den Anfragen vor einem Stapel geladen hat.
- **Japanisch braucht `pykakasi` im Image.** Ohne es protokolliert der Server *pykakasi not available - Japanese text processing skipped* und liest Kanji falsch. Füge `RUN python3 -m pip install --no-cache-dir pykakasi` in sein Dockerfile ein (vor `COPY . .`). Dasselbe stille Überspringen gibt es bei den Paketen für Hebräisch, Russisch und Chinesisch.
- **Stimmen** sind Dateien im Ordner `reference_audio/` des Servers (binde ihn ein, damit sie erhalten bleiben). Die Erweiterung lädt die Clips deiner Figuren vor einem Stapel selbst hoch (`POST /upload_reference`), unter einem Namen, der aus dem Hash des Clips gebildet wird. Der Server hat keinen Lösch-Aufruf: Die Clips sammeln sich dort an.
- Der Server hat **keine Authentifizierung**. Veröffentliche seinen Port nicht. Setze einen Proxy davor, wenn er nicht in einem privaten Netz läuft.
- `restart: "no"` für einen Container, den etwas anderes bei Bedarf startet. `restart: unless-stopped` startet ihn beim Booten und belegt die Karte für nichts.

Im Backend (*Settings → Extension Settings → Audiobook Settings*) trägst du seine Adresse ein. Wenn er auf diesem Rechner oder in deinem Netz läuft, setzt du den Haken bei *The server is on this machine or on my network* und drückst **Test connection**. Gewünscht ist *the server answers, the model is loaded*.
Die Erweiterung spricht mit `/tts` (der vollständige Satz an Parametern: voice, language, exaggeration, CFG weight, temperature, speed, seed) sowie mit `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files` und `/upload_reference`. Steht ein Proxy zwischen dir und dem Server, muss er all diese Aufrufe durchreichen.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## Eine GPU für Sprachmodell und Vertonung

Eine Karte, die das Sprachmodell hält, hat keinen Platz für das TTS-Modell. Der Ablauf ist für jeden Proxy derselbe:

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**Die Reihenfolge ist nicht verhandelbar.** Den Sprachcontainer zu starten, während das Sprachmodell die Karte hält, hat im Test den Arbeitsspeicher des Hosts gefüllt (30 GB RAM, dann der Swap, danach hat der OOM-Killer andere Dienste beendet). Das Entladen **muss zuerst** kommen. Die erste fehlschlagende Anfrage beendet die Liste, sodass nichts auf einer vollen Karte startet. Die Anfragen *nach* einem Stapel laufen alle, egal was mit einer davon passiert.

Warum man den Container *stoppt*, statt das Modell im Server zu entladen: Ein Entladen im Server ließ nach vielen Modellwechseln rund 1 GB der Karte belegt. Den Container zu stoppen gibt alles frei.

<a id="the-requests"></a>

### Die Anfragen

Jede Anfrage der Listen besteht aus einer Methode, einer Adresse, Headern, einem Body und einer Wartezeit danach (in Sekunden). Schreib den Schlüssel nicht in die Header (das Formular zeigt Header so, wie sie eingegeben wurden). Trag ihn unter **Key of the requests around a batch** ein, oder lass das Feld leer, dann wird der *Api key* des Backends gesendet. Er geht als `Authorization: Bearer` mit jeder der folgenden Anfragen, die keinen eigenen hat. Mit einem LiteLLM davor und einem Schlüssel sehen die Listen so aus:

| | Methode und Adresse | Header | Body | Wirkung |
|---|---|---|---|---|
| Vorher 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Entlädt das Sprachmodell; wird vom Hook selbst beantwortet und dauert 1 bis 2 s. |
| Vorher 2 | `POST http://litellm:4000/gpu/tts/start` | | | Startet den Sprachcontainer und antwortet, wenn der Server bereit ist (etwa 13 s). |
| Vorher 3 (nur wenn der Server ohne Modell startet) | `POST http://litellm:4000/gpu/tts/load` | | | Lädt das Modell (8 bis 14 s, beim ersten Mal, wenn es heruntergeladen wird, 50 s). |
| Nachher 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Stoppt den Container (unter einer Sekunde). |

Das Sprachmodell kommt von selbst zurück: Die nächste gewöhnliche Anfrage veranlasst den Router, es zu laden (bei einem großen Modell etwa eine Minute; mit dem [Prompt-Cache-Hook](../prompt-cache.md) wird der Cache deiner Sitzung wiederhergestellt). Dafür ist daher keine Anfrage nach dem Stapel nötig.

Ohne LiteLLM gehen dieselben Anfragen an das, was deine Container startet und stoppt. Die einzige Regel ist die Reihenfolge oben.

<a id="what-answers-those-addresses"></a>

### Was auf diese Adressen antwortet

- **`X-Llama-Unload`** wird vom Hook `gpu_unload.py` von LiteLLM beantwortet. Er ist mit seinem Code beschrieben in [Die GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model) (er funktioniert auch ohne den Cache-Hook). Bei einer Route `openai/*` muss der Modellname im Body mit `openai/` beginnen. Ein anderer Name wird mit 400 beantwortet, *nachdem* die Modelle entladen wurden.
- **`/gpu/tts/start`, `/stop` und `/load`** sind LiteLLM-**Pass-through-Endpunkte** zum kleinen Controller `tools/gpu-control` des Repositorys, der Container anhand ihres Namens startet und stoppt. Seine README enthält die Compose-Datei mit dem **Docker-Socket-Proxy** (damit der Controller Container starten und stoppen kann und sonst nichts; der Socket selbst ist Root auf dem Host) und die Einstellungen, die man leicht falsch macht: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. In LiteLLM:

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

  (Der Container heißt `chatterbox`, und `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` sagt dem Controller, worauf er warten soll. Das Token des Controllers bleibt in seiner `.env` und in der Umgebung von LiteLLM. Marginalia hat nur den Schlüssel von LiteLLM.)
- **Alle Container und LiteLLM teilen sich ein Docker-Netz** (`docker network create ai`, einmal). Setze `networks: [default, ai]` und das externe Netz `ai` in jede Compose-Datei. Ein von Hand ausgeführtes `docker network connect` geht verloren, sobald ein Container neu erstellt wird.

<a id="checking-it"></a>

## Prüfen

1. `curl http://<speech server>/api/model-info` antwortet (`loaded` ist nach einem Start true).
2. *Test connection* im Backend meldet, dass das Modell geladen ist. Wenn die Anfragen vor einem Stapel das Einzige sind, was den Server startet, teste mit den Listen an Ort und Stelle: *the server answers, no model is loaded* ist die Antwort eines gestoppten und neu gestarteten Servers, der noch laden muss.
3. Konvertiere einen kurzen Teil unter **Convert to audio**. Das Fenster zeigt die entstehenden Stücke. Danach sollte der Sprachcontainer gestoppt sein, und `nvidia-smi`, `nvtop` oder `rocm-smi` zeigen die Karte wieder frei.
4. Wenn der erste Aufruf nach dem Laden eines Modells lange dauert, wärmt sich die Engine auf. Das passiert einmalig (im ersten Lauf 28 s, danach nicht mehr beobachtet).
<a id="troubleshooting"></a>

## Fehlerbehebung

| Symptom | Ursache |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | Der Server ist leer gestartet (ein Start-Wrapper, der das Modell einmal entlädt). Füge die Anfrage **Before 3** hinzu. |
| Die ersten Teile werden erzeugt, dann meldet der Server *Model not loaded*, und sein Log zeigt `Request received for /api/unload` | Etwas anderes als Marginalia entlädt das Modell (die Erweiterung ruft nie `/api/unload` auf). Typischerweise ist es ein Start-Wrapper im Container, der wartet, bis der Server antwortet, und das Modell dann entlädt, „um den Start schlank zu halten“. Seine Wartezeit kann erst nach dem ersten Teil enden, weil der Single-Thread-Server beschäftigt war, und das Entladen landet dann mitten im Stapel. (Ein `urllib`-Timeout-Traceback im Container-Log stammt von diesem Wrapper, der wartet.) Das Muster, nach dem man in der Compose-Datei sucht, ist ein `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Ersetze es durch `command: ["python3", "server.py"]`: Ein Start allein liefert einen geladenen Turbo, und das Entladen diente nur dazu, die Karte nach einem Start frei zu halten. Ein Container, den der Controller startet und stoppt, braucht das nicht. Um herauszufinden, wer die Anfrage gesendet hat, lies das Log der Erweiterung (*Admin → Extensions → Log*). Es listet jede Anfrage vor und nach einem Stapel mit ihrer Adresse und dem Zeitpunkt der Antwort sowie den Beginn und das Ende der Teile. Ein Entladen, das kommt, während das Log sagt, dass der Stapel läuft, oder das von einer Adresse kommt, die nicht in der Liste steht, stammt nicht von der Erweiterung. |
| Der Stapel bricht mitten drin ab (das Log der Erweiterung sagt *Cannot reach*, oder das Log des Servers endet einfach), und ein Neustart von Hand funktioniert | Der Server ist gestorben, typischerweise vom System beendet, weil er den gesamten Speicher beansprucht (`dmesg \| grep -i oom` auf dem Host: ein `python3`-Prozess mit vielen GB). Die Erweiterung startet ihn dann selbst neu (zuerst die Anfragen nach dem Stapel, dann die davor) und erzeugt das Teil noch einmal. Wenn der Server nicht zurückgeholt werden kann oder sofort wieder weg ist, schlägt der Rest des Stapels sofort fehl, und das Log sagt, warum. Damit ein undichter Server es nicht so weit kommen lässt, setze im Backend **Restart the server after (pieces)** (zum Beispiel auf 100) und ein `mem_limit` am Container, damit das System nur ihn beendet. |
| Die Konvertierung schlägt sofort mit der Antwort des Proxys fehl | Die erste Anfrage vor dem Stapel ist fehlgeschlagen (ein falscher Schlüssel, die Entlade-Route nicht gefunden). Die Liste bricht dort absichtlich ab. |
| Dem Host geht der Speicher aus, wenn der Stapel startet | Das Sprachmodell war noch auf der Karte: Die Entlade-Anfrage fehlt oder steht nicht an erster Stelle. |
| Japanisch wird als Unsinn gelesen | `pykakasi` fehlt im Image des Servers. Außerdem muss für dieses Teil das Backend das mehrsprachige sein. |
| Der Upload eines Clips schlägt hinter einem Proxy fehl | Der Proxy muss einen Multipart-Body (`/upload_reference`) unverändert durchreichen. Nicht über LiteLLM-Pass-through getestet. Verwende bis dahin eine Stimmdatei des Servers (`predefined:Name.wav`). |
| Der Stapel eines anderen Benutzers entlädt das Modell unter deinem | Stapel auf derselben Server-Adresse teilen sich innerhalb von Marginalia ein Gate, und ein Stapel für ein anderes Sprachmodell wartet, bis die anderen fertig sind. Ein Sprach-Server und zum Beispiel ein Bild-Server auf einer Karte brauchen denselben Namen in **GPU (name)** auf beiden Backends, damit sie aufeinander warten (ebenso der des Image Generators). Zwei Marginalia-Installationen auf einer Karte teilen sich kein Gate. |
