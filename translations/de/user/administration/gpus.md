---
label: GPUs
order: 84
---

# GPUs

Eine Grafikkarte hält oft nur ein Modell gleichzeitig: das Sprachmodell, das Bildmodell oder das Sprachausgabemodell. Auf der Registerkarte **GPUs** der Admin-Seite lassen sich die Modelle und Server auf einer Karte der Reihe nach nutzen. So startet ein Bild oder ein Hörbuch nicht, während das Sprachmodell die Karte füllt, und das Sprachmodell wird wieder geladen, wenn es gebraucht wird.

Als Administrator richtest du das einmal ein. Danach wählt jeder Benutzer für seine KI oder seine Backends eine Sperre aus.

<a id="what-it-is-made-of"></a>

## Aus diesen Teilen besteht es

| | |
|---|---|
| **GPU** | Eine Karte (oder jede Maschine, die nur ein Modell gleichzeitig hält). Nur ihr Name. |
| **GPU-Sperre** | Eine Art, die GPU zu nutzen: was auf ihr geladen ist. Zum Beispiel *Sprachmodell*, *ComfyUI* und *Sprachausgabe* auf derselben GPU. Eine Sperre hat zwei geordnete Listen von Schritten: Die **Belegungsschritte** laden sie (einen Container starten, ein Modell laden, zuvor alles entladen, was weg muss). Die **Freigabeschritte** geben die Karte wieder frei (Container stoppen, Modell entladen). **Gleichzeitige Aufträge** begrenzt, wie viele Aufträge der Sperre gleichzeitig laufen (ein Bildserver, der nur ein Bild gleichzeitig erzeugt: 1; 0 für keine Begrenzung). |
| **GPU step** | Eine HTTP-Anfrage: eine Adresse, eine Methode, Header (`Name: value`, einer pro Zeile, verschlüsselt gespeichert, sodass dort ein Bearer-Schlüssel eingetragen werden kann), ein Body, ein Timeout (leer: 60 Sekunden) und eine Wartezeit nach der Antwort. Ein Schritt kann von mehreren Sperren genutzt werden, zum Beispiel ein Schritt *Sprachmodell entladen*, den sowohl die Bild- als auch die Sprachausgabe-Sperre zuerst ausführt. **Testen** sendet die Anfrage sofort. |

Die Sperre wählst du in:

- den [Einstellungen einer KI](../inference-providers.md) (**GPU-Sperre** in den allgemeinen Einstellungen), für das Sprachmodell;
- einem Backend des [Bildgenerators](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- einem Backend des [Hörbuchs](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

Das Feld **GPU-Sperre** erscheint nur, wenn mindestens eine Sperre existiert.

<a id="how-the-turns-work"></a>

## So wechseln sich die Aufträge ab

- Jede Anfrage an ein Modell sowie jeder Bild-, Hörbuch-Batch- oder Stimmprobenauftrag belegt die Sperre seiner KI oder seines Backends **für die eigene Dauer**.
- Ist die GPU bereits mit dieser Sperre geladen, startet der Auftrag sofort und kein Schritt läuft.
- Ist sie mit einer anderen Sperre geladen, wartet der Auftrag, bis die Aufträge dieser Sperre fertig sind. Danach laufen die **Freigabeschritte** dieser Sperre (alle, auch wenn einer fehlschlägt), dann die **Belegungsschritte** der neuen Sperre (der Reihe nach; der erste Fehler bricht ab, und der Auftrag schlägt mit der Meldung des Schritts fehl).
- Schlägt ein Freigabeschritt fehl, wird die neue Sperre gar nicht geladen (die Karte könnte noch voll sein): Der Auftrag schlägt mit dem Fehler fehl, und die GPU bleibt bei der Sperre, die sie hatte. Der nächste Auftrag einer anderen Sperre versucht, sie erneut freizugeben.
- Nach einem Auftrag bleibt die GPU **geladen** mit ihrer Sperre. Nichts wird per Timer entladen: Ein Bildfenster, das offen bleibt, eingeklappt oder geschlossen wird, belegt keine GPU, und das Bildmodell bleibt geladen, bis das Sprachmodell (oder eine andere Sperre) die Karte braucht.
- Aufträge warten in der Reihenfolge, in der sie eingegangen sind. Sobald ein Auftrag auf eine andere Sperre wartet, warten neue Aufträge der geladenen Sperre hinter ihm. So kann ein stark genutztes Sprachmodell ein Bild nicht unbegrenzt aufhalten.
- Eine KI oder ein Backend ohne Sperre oder mit einer gelöschten Sperre wartet auf nichts.

Was eine GPU geladen hat, ist nur bekannt, solange die Anwendung läuft. Nach einem Neustart ist nichts als geladen bekannt, und der erste Auftrag führt nur seine Belegungsschritte aus.

<a id="the-tab"></a>

## Der Tab

Der Tab listet die GPUs mit der Sperre, mit der jede jetzt geladen ist (*Unbekannt*, wenn noch nichts lief oder sie entladen wurde), und wie viele Aufträge darauf laufen und warten. Darunter stehen die Sperren und die Schritte, jeweils mit Schaltflächen zum Hinzufügen, Bearbeiten und Löschen. **Aktualisieren** liest erneut, was die GPUs belegen.

**Jetzt entladen** gibt eine GPU von Hand frei, zum Beispiel um die Karte für etwas außerhalb von Marginalia zu nutzen: Es wartet auf die laufenden Aufträge, führt die Freigabeschritte der geladenen Sperre aus und lässt die GPU als unbekannt zurück (auch wenn ein Schritt fehlschlug; die Meldung sagt, welcher). Der nächste Auftrag lädt seine Sperre wieder.

Das Löschen einer GPU löscht ihre Sperren. Das Löschen eines Schritts entfernt ihn aus den Sperren, die ihn ausführen. KIs und Backends, die eine gelöschte Sperre angegeben haben, laufen ohne Sperre weiter.

<a id="an-example-one-card-three-models"></a>

## Ein Beispiel: eine Karte, drei Modelle

Mit `llama-server` hinter LiteLLM und einem Proxy im Stil von [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses), der Container startet und stoppt:

| Schritt | Anfrage |
|---|---|
| Sprachmodell entladen | `POST http://litellm:4000/v1/chat/completions` mit dem Header `X-Llama-Unload: all` (siehe [Die GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| ComfyUI starten / ComfyUI stoppen | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Sprachausgabe starten / Sprachausgabe stoppen | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Sperre | Belegungsschritte | Freigabeschritte | Gleichzeitige Aufträge |
|---|---|---|---|
| Sprachmodell | (keine: der Router lädt es mit der nächsten Anfrage) | (keine) | 0 |
| ComfyUI | Sprachmodell entladen, ComfyUI starten | ComfyUI stoppen | 1 |
| Sprachausgabe | Sprachmodell entladen, Sprachausgabe starten | Sprachausgabe stoppen | 1 |
| Clip-Bereinigung | Sprachmodell entladen, ComfyUI starten | ComfyUI stoppen | 1 |

Die KI des Sprachmodells wählt *Sprachmodell*, das Bild-Backend *ComfyUI*, das Sprachausgabe-Backend *Sprachausgabe*. Die [Bereinigung der Clips](../extensions/audiobook.md#cleanup-of-clips) des Hörbuchs wählt *Clip-Bereinigung*: Sie läuft auf demselben ComfyUI wie die Bilder, aber eine eigene Sperre startet ComfyUI zwischen den Aufgaben neu, sodass das Bildmodell und die Bereinigungsmodelle nie gleichzeitig im Speicher sind. Ein Sprachausgabe-Batch nach einem Chat führt *Sprachmodell entladen* und *Sprachausgabe starten* aus; die nächste Chatnachricht führt zuerst *Sprachausgabe stoppen* aus.
