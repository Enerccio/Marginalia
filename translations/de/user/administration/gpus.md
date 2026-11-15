---
label: GPUs
order: 84
---

# GPUs

Eine Grafikkarte hält oft jeweils nur ein Modell: das Sprachmodell, das Bildmodell oder das Sprachausgabe-Modell. Der Reiter **GPUs** auf der Admin-Seite lässt die Modelle und Server auf einer Karte einander abwechseln. So startet ein Bild oder ein Hörbuch nicht, während das Sprachmodell die Karte füllt, und das Sprachmodell wird wieder geladen, wenn es gebraucht wird.

Sie richten das einmal als Administrator ein. Danach wählt jeder Benutzer für seine KI oder seine Backends eine Sperre aus.

<a id="what-it-is-made-of"></a>

## Aus welchen Teilen es besteht

| | |
|---|---|
| **GPU** | Eine Karte (oder jede Maschine, die jeweils nur ein Modell hält). Nur ihr Name. |
| **GPU-Sperre** | Eine Art, eine GPU zu nutzen: was auf ihr geladen ist. Zum Beispiel *Sprachmodell*, *ComfyUI* und *Speech* auf derselben GPU. Eine Sperre hat zwei geordnete Listen von Schritten: Die **Ladeschritte** laden sie (einen Container starten, ein Modell laden, alles entladen, was vorher weg muss). Die **Freigabeschritte** geben die Karte davon wieder frei (den Container stoppen, das Modell entladen). **Gleichzeitige Aufträge** begrenzt, wie viele Aufträge der Sperre zugleich laufen (ein Bildserver, der jeweils ein Bild erzeugt: 1; 0 für keine Grenze). |
| **GPU-Schritt** | Eine HTTP-Anfrage: eine Adresse, eine Methode, Header (`Name: value`, einer pro Zeile, verschlüsselt gespeichert, sodass dort ein Bearer-Schlüssel stehen kann), ein Body, ein Timeout (leer: 60 Sekunden) und eine Wartezeit nach der Antwort. Ein Schritt kann von mehreren Sperren genutzt werden, zum Beispiel ein Schritt *Sprachmodell entladen*, den sowohl die Bild- als auch die Speech-Sperre zuerst ausführen. **Testen** sendet die Anfrage sofort. |

Die Sperre wählen Sie aus in:

- den [Einstellungen einer KI](../inference-providers.md) (**GPU-Sperre** in den allgemeinen Einstellungen), für das Sprachmodell;
- einem Backend des [Bildgenerators](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- einem Backend des [Hörbuchs](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

Das Feld **GPU-Sperre** wird nur angezeigt, wenn mindestens eine Sperre existiert.

<a id="how-the-turns-work"></a>

## Wie der Wechsel abläuft

- Jede Anfrage an ein Modell, jedes Bild sowie jeder Hörbuch-Stapel oder jede Hörprobe belegt die Sperre seiner KI oder seines Backends **für ihre gesamte Dauer**.
- Ist die GPU bereits mit dieser Sperre geladen, startet der Auftrag sofort und kein Schritt läuft.
- Ist sie mit einer anderen Sperre geladen, wartet der Auftrag, bis die Aufträge dieser Sperre erledigt sind. Dann laufen die **Freigabeschritte** dieser Sperre (alle, auch wenn einer fehlschlägt), danach die **Ladeschritte** der neuen Sperre (in Reihenfolge; der erste Fehler bricht sie ab, und der Auftrag schlägt mit der Meldung des Schritts fehl).
- Schlägt ein Freigabeschritt fehl, wird die neue Sperre gar nicht geladen (die Karte könnte noch voll sein). Der Auftrag scheitert mit dem Fehler, und die GPU bleibt bei der Sperre, die sie hatte. Der nächste Auftrag einer anderen Sperre versucht, sie erneut freizugeben.
- Nach einem Auftrag **bleibt** die GPU mit ihrer Sperre geladen. Nichts wird per Timer entladen: Ein Bildfenster, das offen gelassen, eingeklappt oder geschlossen wird, belegt keine GPU, und das Bildmodell bleibt geladen, bis das Sprachmodell (oder eine andere Sperre) die Karte braucht.
- Aufträge warten in der Reihenfolge ihres Eingangs. Sobald ein Auftrag auf eine andere Sperre wartet, warten neue Aufträge der geladenen Sperre hinter ihm, damit ein ausgelastetes Sprachmodell ein Bild nicht ewig aufhalten kann.
- Eine KI oder ein Backend ohne Sperre oder mit einer gelöschten Sperre wartet auf nichts.

Was eine GPU gerade hält, ist nur bekannt, solange die Anwendung läuft. Nach einem Neustart gilt nichts als geladen, und der erste Auftrag führt nur seine Ladeschritte aus.

<a id="the-tab"></a>

## Der Reiter

Der Reiter listet die GPUs mit der Sperre, mit der jede jetzt geladen ist (*Unknown*, wenn noch nichts lief oder sie entladen wurde), und wie viele Aufträge darauf laufen und warten. Danach folgen die Sperren und die Schritte, jeweils mit Schaltflächen zum Hinzufügen, Bearbeiten und Löschen. **Aktualisieren** liest erneut, was die GPUs halten.

**Jetzt entladen** gibt eine GPU von Hand frei, zum Beispiel um die Karte für etwas außerhalb von Marginalia zu nutzen. Es wartet auf die laufenden Aufträge, führt die Freigabeschritte der geladenen Sperre aus und lässt die GPU als unbekannt zurück (auch wenn ein Schritt fehlschlug; die Meldung sagt, welcher). Der nächste Auftrag lädt seine Sperre wieder.

Eine GPU zu löschen, löscht ihre Sperren. Einen Schritt zu löschen, entfernt ihn aus den Sperren, die ihn ausführen. KIs und Backends, die eine gelöschte Sperre genannt haben, laufen ohne eine.

<a id="an-example-one-card-three-models"></a>

## Ein Beispiel: eine Karte, drei Modelle

Mit `llama-server` hinter LiteLLM und einem Proxy nach dem Muster von [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses), der Container startet und stoppt:

| Schritt | Anfrage |
|---|---|
| Sprachmodell entladen | `POST http://litellm:4000/v1/chat/completions` mit dem Header `X-Llama-Unload: all` (siehe [Freigabe der GPU für ein anderes Modell](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| ComfyUI starten / ComfyUI stoppen | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Speech starten / Speech stoppen | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Sperre | Ladeschritte | Freigabeschritte | Gleichzeitige Aufträge |
|---|---|---|---|
| Sprachmodell | (keine: der Router lädt es mit der nächsten Anfrage) | (keine) | 0 |
| ComfyUI | Sprachmodell entladen, ComfyUI starten | ComfyUI stoppen | 1 |
| Speech | Sprachmodell entladen, Speech starten | Speech stoppen | 1 |

Die KI des Sprachmodells wählt *Sprachmodell*, das Bild-Backend *ComfyUI* und das Sprachausgabe-Backend *Speech*. Ein Sprachausgabe-Stapel nach einem Chat führt *Sprachmodell entladen* und *Speech starten* aus. Die nächste Chat-Nachricht führt zuerst *Speech stoppen* aus.
