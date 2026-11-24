---
label: GPUs
order: 84
---

# GPUs

Eine Grafikkarte hält oft nur ein Modell gleichzeitig: das Sprachmodell, das Bildmodell oder das Sprachausgabemodell. Der Reiter **GPUs** der Admin-Seite lässt die Modelle und Server auf einer Karte abwechselnd laufen. So startet ein Bild oder ein Hörbuch nicht, während das Sprachmodell die Karte füllt, und das Sprachmodell kommt zurück, wenn es gebraucht wird.

Du richtest es einmal als Administrator ein. Danach wählt jeder Benutzer ein Lock für seine KI oder seine Backends.

<a id="what-it-is-made-of"></a>

## Aus diesen Teilen besteht es

| | |
|---|---|
| **GPU** | Eine Karte (oder jede Maschine, die nur ein Modell gleichzeitig hält): ihr Name und **Also used outside Marginalia** (siehe [unten](#a-gpu-that-others-use-too)). |
| **GPU lock** | Eine Art, eine GPU zu nutzen: was auf ihr geladen ist. Zum Beispiel *Language model*, *ComfyUI* und *Speech* auf derselben GPU. Ein Lock hat zwei geordnete Listen von Schritten: Die **acquire steps** laden es (einen Container starten, ein Modell laden, alles entladen, was vorher weichen muss). Die **release steps** geben die Karte davon frei (den Container stoppen, das Modell entladen). Eine dritte Liste, die **steps before every job**, läuft vor jedem Job des Locks, egal ob die GPU es gerade hält. Hält sie es nicht, läuft sie vor den acquire steps. Sie enthält, was jedes Mal sichergestellt sein muss, weil etwas anderes die Karte benutzt haben könnte. **Jobs at once** begrenzt, wie viele Jobs des Locks gleichzeitig laufen (ein Bildserver, der ein Bild nach dem anderen erzeugt: 1; 0 für kein Limit). |
| **GPU step** | Eine HTTP-Anfrage: eine Adresse, eine Methode, Header (`Name: value`, einer pro Zeile, verschlüsselt gespeichert, sodass dort ein Bearer-Schlüssel stehen kann), ein Body, ein Timeout (leer: 60 Sekunden) und eine Wartezeit nach der Antwort. Ein Schritt kann von mehreren Locks genutzt werden. Zum Beispiel kann ein Schritt *unload the language model* von dem Bild-Lock und dem Sprachausgabe-Lock gemeinsam vorab ausgeführt werden. **Test** sendet die Anfrage sofort. |

![Das Fenster eines Locks](../../images/gpu-lock-dialog.png)

Wähle das Lock aus in:

- den [Einstellungen einer KI](../inference-providers.md) (**GPU lock** in den allgemeinen Einstellungen) für das Sprachmodell;
- einem Backend des [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- einem Backend des [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

Das Feld **GPU lock** wird nur angezeigt, wenn mindestens ein Lock existiert.

<a id="variables-in-the-steps"></a>

## Variablen in den Schritten

Ein Schritt kann benennen, wofür der Job ist. So bedient ein Schritt mehrere Modelle, und die Locks müssen nicht geändert werden, wenn du ein anderes Modell ausprobierst. In der Adresse, den Headern und dem Body:

| Variable | Ist |
|---|---|
| `{{model}}` | das Modell, das der Job verwendet: die Modell-ID der KI (so wie die KI-Einstellungen sie führen, zum Beispiel `openai/big-32b` über LiteLLM), das Modell eines Audiobook-Backends (`higgs-audio-v3`, `chatterbox-turbo`), der Spracherkennung oder des Bild-Backends; leer bei einem Job ohne Modell (das Aufräumen von Clips) |
| `{{lock}}` | den Namen des Locks |
| `{{text_model}}` | nur bei einem Job zur Textgenerierung: die Modell-ID seiner KI (dieselbe wie `{{model}}`); leer bei Sprach- und Bildjobs |
| `{{last_text_model}}` | jeder Job: das Modell der letzten Textgenerierung auf diesem Lock, gemerkt ab dem Start dieses Jobs (sodass ein Textjob seine eigene sieht); leer vor der ersten Textgenerierung und nach einem Neustart von Marginalia |

Filter ändern den Wert: `{{model | strip:openai/}}` entfernt das Präfix, wenn der Wert damit beginnt (der Routenname von LiteLLM, den der dahinterliegende Server nicht kennt). `{{model | last}}` behält, was auf das letzte `/` folgt. `{{model | json}}` maskiert den Wert für einen JSON-String, und `{{model | url}}` kodiert ihn für eine Adresse. Filter lassen sich verketten (`{{model | last | json}}`). Die acquire steps und die steps before every job erhalten die Werte des Jobs. Die release steps erhalten die Werte des letzten Jobs des geladenen Locks (das Modell, das entladen werden soll). **Test** führt einen Schritt mit leeren Variablen aus.

<a id="how-the-turns-work"></a>

## Wie das Abwechseln funktioniert

- Jede Anfrage an ein Modell, jedes Bild und jeder Audiobook-Batch oder jede Probe belegt das Lock seiner KI oder seines Backends **für seine eigene Dauer**.
- Ist die GPU bereits mit diesem Lock geladen, startet der Job sofort, und kein Schritt läuft, außer den **steps before every job** des Locks.
- Ist sie mit einem anderen Lock geladen, wartet der Job, bis die Jobs dieses Locks fertig sind. Dann laufen die **release steps** dieses Locks (alle, auch wenn einer fehlschlägt), danach die steps before every job und die **acquire steps** des neuen Locks. Diese laufen der Reihe nach. Der erste Fehler stoppt sie, und der Job scheitert mit der Meldung des Schritts.
- Schlägt ein release step fehl, wird das neue Lock gar nicht geladen, denn die Karte könnte noch voll sein. Der Job scheitert mit dem Fehler, und die GPU bleibt bei dem Lock, das sie hatte. Der nächste Job eines anderen Locks versucht erneut, sie freizugeben.
- Nach einem Job bleibt die GPU **mit ihrem Lock geladen**. Nichts wird per Timer entladen: Ein Bildfenster, das offen gelassen, eingeklappt oder geschlossen wird, behält keine GPU. Das Bildmodell bleibt geladen, bis das Sprachmodell (oder ein anderes Lock) die Karte braucht.
- Jobs warten in der Reihenfolge ihres Eintreffens. Sobald ein Job auf ein anderes Lock wartet, stellen sich neue Jobs des geladenen Locks hinter ihn. So kann ein ausgelastetes Sprachmodell ein Bild nicht für immer warten lassen.
- Eine KI oder ein Backend ohne Lock oder mit einem gelöschten Lock wartet auf nichts.

Was eine GPU hält, ist nur bekannt, solange die Anwendung läuft. Nach einem Neustart gilt nichts als geladen, und der erste Job führt nur seine acquire steps aus.

<a id="a-gpu-that-others-use-too"></a>

## Eine GPU, die auch andere nutzen

Die Locks ordnen nur die Jobs von Marginalia. Was es über die Karte weiß, stammt aus den eigenen Jobs. Nach einem Neustart von Marginalia oder wenn ein anderes Programm, ein Skript oder eine Person ein Modell auf der Karte lädt (eine Anfrage an den Router, der das Sprachmodell aufweckt, ein Workflow, der auf der eigenen Seite von ComfyUI eingereiht wird), ist das, was Marginalia für geladen hält, falsch. Dann kann ein Job auf einer vollen Karte starten.

- **Also used outside Marginalia** an der GPU bewirkt, dass Marginalia aufhört, dem zu vertrauen, was es weiß. Dann laufen die **acquire steps vor jedem Job**, nicht nur, wenn die GPU das Lock wechselt. Die release steps laufen weiterhin nur bei einem Wechsel. Die acquire steps jedes Locks müssen dann selbst Platz schaffen (die anderen Modelle entladen, den anderen Server stoppen) und gefahrlos wiederholbar sein. Ein Start eines bereits laufenden Servers, ein Entladen eines nicht geladenen Modells und ein Stopp eines bereits gestoppten Containers müssen mit Erfolg antworten, sonst scheitert jeder Job. Das kostet diese Anfragen bei jedem Job.
- **Steps before every job** tun dasselbe für ein einzelnes Lock, ohne die anderen. Nur die Schritte, die jedes Mal geprüft werden müssen (die Modelle des anderen Programms entladen, sicherstellen, dass ein Server antwortet), laufen vor jedem Job. Der Rest läuft nur bei einem Wechsel.
- Der sichere Ort dafür ist die GPU-Maschine selbst, durch die jeder Benutzer geht: ein Proxy, dessen *start ComfyUI* zuerst das Sprachmodell entlädt, oder ein Router-Hook, der ComfyUI stoppt, bevor er das Sprachmodell lädt. Dann kann niemand die Karte doppelt füllen, egal welches Programm fragt.

<a id="the-tab"></a>

## Der Reiter

Der Reiter listet die GPUs mit dem Lock, mit dem jede gerade geladen ist (*Unknown*, wenn noch nichts lief oder sie entladen wurde), und wie viele Jobs darauf laufen und warten. Danach folgen die Locks und die Schritte, jeweils mit Schaltflächen zum Hinzufügen, Bearbeiten und Löschen. **Refresh** liest erneut, was die GPUs halten.

![Der Reiter GPUs mit einer GPU, einem Lock und den Schritten](../../images/admin-gpus.png)

**Unload now** gibt eine GPU von Hand frei, zum Beispiel um die Karte für etwas außerhalb von Marginalia zu nutzen. Es wartet auf die laufenden Jobs, führt die release steps des geladenen Locks aus und lässt die GPU als unbekannt zurück. Das gilt auch, wenn ein Schritt fehlschlug; die Meldung sagt, welcher. Der nächste Job lädt sein Lock wieder.

Das Löschen einer GPU löscht ihre Locks. Das Löschen eines Schritts entfernt ihn aus den Locks, die ihn ausführen. KIs und Backends, die ein gelöschtes Lock benannt hatten, laufen ohne eines weiter.
<a id="an-example-one-card-several-models"></a>

## Ein Beispiel: eine Karte, mehrere Modelle

Mit `llama-server` hinter LiteLLM und einem Proxy im Stil von [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses), der Container startet und stoppt:

| Schritt | Anfrage |
|---|---|
| Unload the language model | `POST http://litellm:4000/v1/chat/completions` mit dem Header `X-Llama-Unload: all` (siehe [GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |

Wird nur von Marginalia genutzt, gibt jede Sperre die Karte nur für sich selbst frei und nichts weiter:

| Sperre | Schritte zum Belegen | Schritte zum Freigeben | Gleichzeitige Jobs |
|---|---|---|---|
| Language model | (keine: der Router lädt es mit der nächsten Anfrage) | Unload the language model | 1 |
| ComfyUI images | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech recognition | Start ComfyUI | Stop ComfyUI | 1 |
| Clip cleanup | Start ComfyUI | Stop ComfyUI | 1 |

Ein Stapel mit Sprachausgaben nach einem Chat führt *Unload the language model* (die Freigabe des Sprachmodells) und *Start ComfyUI* aus; die nächste Chatnachricht führt zuerst *Stop ComfyUI* aus. Die ComfyUI-Sperren sind absichtlich getrennt: Der Wechsel von einer zur anderen stoppt und startet ComfyUI, und genau das gibt den Speicher frei, den ein Modell nach dem Entladen behält (fp32-Higgs belegt etwa 16 GB des Serverspeichers). So sind das Bildmodell, Higgs, die Erkennung und die Bereinigungsmodelle nie gleichzeitig geladen.

Mit **Also used outside Marginalia** muss stattdessen jedes Belegen selbst Platz schaffen: *Language model* belegt mit *Stop ComfyUI*, und jede ComfyUI-Sperre mit *Unload the language model, Start ComfyUI*.

Wenn ein anderes Programm **denselben `llama-server`-Router** mit einem eigenen Modell nutzt (zum Beispiel ein kleines Modell neben seinem Sprachmodell), würde *Unload the language model* auch dessen Modell entladen, und dessen eigenes Entladen würde das von Marginalia entladen. `tools/gpu-control` hat einen Aufruf, der alle Modelle des Routers entlädt, **außer** den genannten: ein Schritt `POST http://litellm:4000/gpu/llama/unload` mit dem Body `{"keep": ["{{model | strip:openai/}}"]}` (im README steht die Weiterleitung). Machen Sie daraus einen **Schritt vor jedem Job** von *Language model*: Jede KI auf dieser Sperre behält ihr eigenes Modell, welches Sie auch wählen; das andere Programm tut dasselbe mit seinem eigenen Modell vor jedem seiner Jobs. Ist das behaltene Modell das einzige geladene, tut der Aufruf nichts und antwortet sofort, sodass keines der beiden Programme vom anderen wissen muss.

Wenn **eine Sperre das Sprachmodell und das Sprachausgabemodell bedient** (beide laufen nebeneinander auf der Karte), ist `{{model}}` in diesem Schritt während eines Sprachjobs das Sprachausgabemodell, auf das kein Router-Modell passt, und das Sprachmodell würde direkt nach jeder Antwort entladen. Behalten Sie stattdessen `{{last_text_model}}`, `{"keep": ["{{last_text_model | strip:openai/}}"]}`: Jeder Job der Sperre behält das Modell der letzten Textgenerierung, egal welche Art von Job es ist. Vor der ersten Textgenerierung wird nichts behalten, und die erste lädt ihr Modell.

Für ComfyUI ist derselbe Schritt-Typ `POST http://litellm:4000/gpu/comfy/ensure`: Er tut nichts, wenn ComfyUI antwortet, startet den Container, wenn er gestoppt ist, und startet ihn neu, wenn er läuft, ComfyUI aber nicht antwortet (innen abgestürzt, hängend), und wartet dann, bis er bereit ist. Als Schritt vor jedem Job der ComfyUI-Sperren sorgt er dafür, dass ComfyUI läuft, egal wer es gestoppt hat.

Die KI des Sprachmodells wählt *Language model*, das Bild-Backend wählt *ComfyUI images*, das Sprach-Backend *ComfyUI speech*, und die [Spracherkennung](../extensions/audiobook.md#speech-recognition) sowie die [Bereinigung der Clips](../extensions/audiobook.md#cleanup-of-clips) des Hörbuchs wählen jeweils ihre eigenen Sperren.
