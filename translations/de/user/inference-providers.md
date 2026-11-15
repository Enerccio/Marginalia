---
label: Inferenzanbieter
order: 980
icon: server
---

# Inferenzanbieter

Ein Inferenzanbieter ist eine Verbindung zu einem Modell: die API-Adresse, der API-Schlüssel, das Modell und seine Grenzwerte. Jedes
Buch verwendet genau einen Anbieter. Sie können beliebig viele anlegen, etwa ein großes Cloud-Modell für die Geschichte und ein
kleines lokales für Experimente, und ein Buch jederzeit zwischen ihnen wechseln.

Anbieter gehören zu Ihrem Konto. Andere Benutzer sehen sie und ihre API-Schlüssel nicht. API-Schlüssel werden verschlüsselt
gespeichert (siehe [Der Datenordner](administration/index.md#the-data-folder)).

Zum Hinzufügen eines Anbieters brauchen Sie die Berechtigung **Eigene Modelle anlegen**, die ein Administrator Ihnen gibt
(Administratoren haben sie immer, siehe [Berechtigungen](administration/permissions.md)). Ohne sie können Sie nur die Anbieter nutzen, die die
Administratoren mit Ihnen [teilen](#shared-providers). Die Schaltfläche **Inferenzanbieter hinzufügen** wird dann nicht angezeigt.

![Der Reiter Inferenzanbieter](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## Unterstützte APIs

Marginalia arbeitet mit jeder API, die die **OpenAI Chat Completions**-Schnittstelle implementiert (`/v1/chat/completions`,
mit Streaming). Die meisten Dienste und lokalen Server tun das:

| Dienst | OpenAI-URL |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Die Ports sind die Standardwerte der jeweiligen Server; verwenden Sie die, auf denen Ihr Server tatsächlich läuft.

!!!warning Port 8080
Die Docker-Version von Marginalia verwendet ebenfalls Port 8080. Wenn Sie llama.cpp auf demselben Rechner betreiben, starten Sie
es auf einem anderen Port (`llama-server --port 8081`).
!!!

!!!
Bei der [Docker-Version](getting-started/docker-server.md) bedeutet `localhost` den Marginalia-Container. Verwenden Sie
`http://host.docker.internal:<port>/v1` für einen Modellserver auf demselben Rechner, siehe
[Modelle auf demselben Rechner](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Anbieter hinzufügen

Öffne den Tab **Inferenzanbieter** und klicke auf **Inferenzanbieter hinzufügen**. Bestehende Anbieter bearbeitest du mit dem Stift-Button, löschen kannst du sie mit dem Papierkorb-Button. Der Klonen-Button (zwei Blätter) erstellt eine Kopie eines Anbieters unter einem Namen, den du bestätigst (der Originalname mit *(Copy)*): Die Kopie hat dieselbe Adresse, dasselbe Modell, denselben **API-Schlüssel** (oder die Einstellungen der Erweiterung), dieselben Reasoning-Einstellungen, Token-Limits und Parameter. So braucht ein zweites Modell beim selben Dienst nur einen Namen und einen Klick. Die Kopie ist nicht geteilt und hat keine eigenen Limits oder Nutzungsdaten. Du kannst nur deine eigenen Anbieter klonen (nicht einen geteilten Anbieter eines Administrators, dessen Schlüssel du nicht sehen darfst) und nur, wenn du Anbieter erstellen darfst.

Oben im Dialog:

| Feld | |
|---|---|
| **Name** | Dein Name für den Anbieter, der angezeigt wird, wenn du für ein Buch ein Modell auswählst. Nenne das Modell mit, z. B. `OpenRouter - Claude Sonnet`. |
| **Typ** | Die Art der API: **OpenAI-kompatibel** oder **Erweiterung** für einen Anbieter, den eine [Erweiterung](extensions/index.md) mitbringt (er wird nur angeboten, solange eine solche Erweiterung geladen ist, z. B. [Google Gemini](extensions/google-genai.md) oder [Anthropic Claude](extensions/anthropic.md)). Der Typ kann nach dem Erstellen des Anbieters nicht mehr geändert werden. |
| **Anbieter** | Nur für den Typ **Erweiterung**: welcher Anbieter der Erweiterung gemeint ist. Seine eigenen Einstellungen füllen den Tab *Einstellungen je Typ*. |

<a id="per-type-settings"></a>

### Einstellungen je Typ

![Einstellungen je Typ mit geladener Modellliste](../images/inference-provider.png)

Der Tab ist höher als der Dialog und lässt sich scrollen. Die unteren Felder:

![Zeitlimit, Wiederholungen, zusätzliche Parameter und Verbindung testen](../images/inference-provider-2.png)

| Feld | |
|---|---|
| **OpenAI-URL (endet auf /v1)** | Die Basisadresse der API, siehe [die Tabelle oben](#supported-apis). Ohne `/chat/completions`. |
| **API-Schlüssel** | Der Schlüssel des Dienstes. Ein lokaler Server prüft ihn meist nicht: Lass das Feld leer, dann sendet Marginalia den Platzhalter `none`. |
| **Modell** | Das zu verwendende Modell. Klicke auf **Aktualisieren**, um die Modellliste von der API zu laden, oder gib die Modell-ID selbst ein (z. B. `anthropic/claude-sonnet-4.5` bei OpenRouter). |
| **Zeitlimit (Sekunden)** | Wie lange auf eine Antwort der API gewartet wird und auf weiteren Text, sobald sie zu schreiben begonnen hat. Der Standardwert ist 300. Reasoning-Modelle brauchen unter Umständen Minuten bis zum ersten Text; eine Anfrage, die so lange nichts hört, scheitert mit einer Zeitüberschreitungsmeldung (das Denken eines Modells zählt als Text, ein Modell, das weiter denkt, wird also nicht abgebrochen). Eine Änderung gilt für die nächste Anfrage, auch eines Agenten, der gerade arbeitet. |
| **Wiederholungen** | Wie oft eine Anfrage wiederholt wird, wenn die API mit einem Ratenlimit (429) oder einem Serverfehler (5xx) antwortet oder nicht erreichbar ist. Die Wartezeit zwischen den Versuchen wird länger, und ein `Retry-After`-Header der API wird beachtet. Der Standardwert ist 2, 0 schaltet das Wiederholen aus. Nur der Beginn einer Anfrage wird wiederholt; Text, der bereits angekommen ist, wird nie doppelt geschrieben. |
| **Tokenizer** | Wie Marginalia die Tokens der Texte für diesen Anbieter zählt. **Automatisch** (der Standard) probiert die Tokenizer-Endpunkte des Servers nacheinander aus (OpenAI, llama.cpp, LiteLLM) und verwendet den ersten, der antwortet; antwortet keiner, wird eine lokale Schätzung verwendet. Wähle einen aus, um nur diesen zu verwenden, oder **LOCAL_TOKKIT** für die lokale Schätzung, die den Server nie anfragt (nützlich, wenn das Zählen den Server dazu bringen würde, ein anderes Modell zu laden). Ein Tokenizer aus einer Erweiterung wird hier ebenfalls aufgeführt; er erhält die Adresse, das Modell und den Text, nie den API-Schlüssel, und nur, wenn du ihn wählst. **Tokenizer testen** zählt einen Beispieltext mit dem, was der Anbieter versuchen würde, und zeigt, was jeder geantwortet hat. Beim Erstellen eines Anbieters ist ein Erweiterungs-Tokenizer, der zur Adresse passt, vorausgewählt, bis du selbst wählst. |
| **Das Denken zurücksenden** | Ob das Modell im nächsten Request sein eigenes Denken zurückbekommt, siehe [Das Denken zurücksenden](#send-the-thinking-back). Nur für OpenAI-kompatible Anbieter: Die Anthropic- und Google-Anbieter senden selbst, was ihre APIs brauchen. |
| **Zusätzliche Parameter** | Ein JSON-Objekt mit zusätzlichen Feldern für jede Anfrage an diesen Anbieter, z. B. `{"top_k": 40, "min_p": 0.05}` für einen lokalen Server oder `{"provider": {"order": ["anthropic"]}}` für OpenRouter. Ein hier gesetztes Feld gewinnt gegen dieselbe Einstellung aus dem [Protokoll](protocols.md) (z. B. `temperature`) oder dem Antwortlimit (`max_completion_tokens`). Muss ein gültiges JSON-Objekt oder leer sein. |

Wenn du einen bestehenden Anbieter bearbeitest, wird der API-Schlüssel nicht erneut angezeigt. Er bleibt unverändert, außer du klickst auf **API-Schlüssel zurücksetzen** und gibst einen neuen ein.

**Aktualisieren** fragt die API nach ihrer Modellliste (`/v1/models`) mit der URL und dem Schlüssel aus dem Formular. Schlägt das mit *Failed to download model list* fehl, prüfe die URL (sie muss auf `/v1` enden), den Schlüssel und ob der Server läuft. Manche Dienste listen keine Modelle auf; gib dann die Modell-ID selbst ein.

**Verbindung testen** prüft die Werte im Formular, ohne sie zu speichern: Es fragt die API nach ihrer Modellliste und dann nach einer Ein-Token-Antwort des gewählten **Modells**, ohne Wiederholungen. Eine Erfolgsmeldung heißt, dass URL, Schlüssel und Modell zusammen funktionieren. Ein Fehler nennt die Ursache (siehe [Fehlermeldungen](#error-messages)). Manche Dienste haben keine Modellliste; der Test braucht dann nur die Ein-Token-Antwort.

<a id="general-settings"></a>

### Allgemeine Einstellungen

![Allgemeine Einstellungen eines Inferenzanbieters](../images/inference-provider-general.png)

| Feld | |
|---|---|
| **Maximale Kontextgröße** | Pflichtfeld. Wie viele Tokens das Modell insgesamt annimmt, Prompt und Antwort zusammen. |
| **Maximale Antwort-Tokens** | Pflichtfeld. Die längste Antwort, die das Modell schreiben darf. Wird als `max_completion_tokens` an die API gesendet, und so viel des Kontexts bleibt für die Antwort frei. Muss kleiner sein als *Maximale Kontextgröße*. |
| **Verbindungen** | Wie viele Anfragen gleichzeitig an dieses Modell unterwegs sein dürfen: alle, von jedem Benutzer und jeder Art (deine Generierung, eine Nebenabfrage, eine Übersetzung, eine Annotation, [Agenten](agents.md#one-model-several-agents), Erweiterungen). Die anderen warten der Reihe nach in der Reihenfolge, in der sie kamen; eine Generierung, die warten muss, zeigt *Warten auf das Modell: N Anfragen vor dieser*, und **Stopp** nimmt sie aus der Warteschlange. **1** (der Standard) für einen lokalen Server mit einem Slot (eine GPU); ein Cloud-Modell, das mehrere Anfragen gleichzeitig beantwortet, kann mehr haben, damit eine Nebenabfrage neben einer Generierung laufen kann. |
| **GPU-Sperre** | Nur wenn der Administrator [GPUs](administration/gpus.md) eingerichtet hat: die Sperre der GPU, auf der dieses Modell läuft, wenn die Karte mit anderen Modellen oder mit Bild- und Sprachservern geteilt wird. Jede Anfrage wartet dann auf ihren Zug an der GPU (gezählt mit den Anfragen davor) und, wenn die GPU etwas anderes geladen hat, gibt sie frei und lädt dieses Modell mit den Schritten der Sperre zuerst. Leer: keine Sperre. |
| **Lore-Bilder senden** | Sendet die [Bilder der Lorebuch-Einträge](lorebooks/lorebooks.md#editing-in-full-screen), die ein Zug verwendet, zusammen mit deren Text. Standardmäßig aus: Schalte es nur für ein Modell ein, das Bilder annimmt; ein Modell ohne Bildverarbeitung lehnt die Anfrage ab, und der Zug schlägt fehl. Die Bilder von Einträgen im Lore-Block gehen in eine eigene Nachricht direkt nach der Systemnachricht (mit einer kurzen Antwort des Modells), die übrigen mit deinem Prompt. |
| **Bilder in der Systemnachricht** | (OpenAI-kompatibel, mit *Lore-Bilder senden*) Legt die Bilder des Lore-Blocks in die Systemnachricht selbst, neben den Text des Lore. Nur für einen Server und ein Chat-Template, die Bilder dort annehmen; die meisten tun das nicht, und die Anfrage schlägt fehl. |
| **Benötigt Jailbreak**, **Jailbreak-Prompt** | Ein zusätzlicher Prompt für Modelle, die manche Inhalte nicht schreiben wollen. Ist das Feld angehakt, wird der Jailbreak-Prompt ganz an den Anfang des Prompts gesetzt. |
| **Reasoning aktivieren**, **Reasoning-Aufwand** | Für Reasoning-Modelle („Denken“), siehe [Reasoning-Modelle](#reasoning-models). |

Marginalia hält die **Maximale Antwort-Tokens** des Kontexts für die Antwort frei und füllt den Rest mit dem Prompt:
Vorlagen, [Lorebuch](lorebooks/index.md)-Einträge und [Zusammenfassungen](books/summaries.md) werden mitgezählt, und die ältesten
Teile der Geschichte, die nicht hineinpassen, bleiben weg. Bei einem Kontext von 32 000 Tokens und Antworten von bis zu 2 000 Tokens
bekommt der Prompt 30 000 Tokens.

Übernimm die **Maximale Kontextgröße** aus der Dokumentation des Dienstes und setze sie etwas niedriger: Token-Zählungen sind nicht
immer exakt, und eine Anfrage, die das echte Limit überschreitet, schlägt fehl. Ein [Protokoll](protocols.md) kann beide Limits
für manche Bücher senken, aber nicht erhöhen.
<a id="error-messages"></a>

## Fehlermeldungen

Wenn die API eine Anfrage ablehnt, sagt Marginalia, was zu korrigieren ist, statt einen internen Fehler anzuzeigen. Danach folgt, was die API selbst gemeldet hat:

![Ein Fehler des Anbieters, dem Benutzer angezeigt](../images/error-dialog.png)

| Meldung | Ursache |
|---|---|
| *Der Anbieter hat den API-Schlüssel abgelehnt.* | Falscher, abgelaufener oder fehlender **API-Schlüssel**. |
| *Der API-Schlüssel darf dieses Modell oder diesen Endpunkt nicht verwenden.* | Der Schlüssel ist gültig, hat aber keinen Zugriff auf das Modell. |
| *Der Anbieter kennt das konfigurierte Modell nicht.* | Falsches **Modell**; mit **Aktualisieren** siehst du die Namen, die die API kennt. |
| *Unter der konfigurierten URL wurde nichts gefunden.* | Falsche **OpenAI URL**; sie endet meist mit `/v1`. |
| *Der Prompt passt nicht in den Kontext des Modells.* | **Maximale Kontextgröße** ist höher, als das Modell tatsächlich akzeptiert. Setze sie niedriger. |
| *Der Anbieter begrenzt die Anfragen.* | Ratenlimit oder erschöpftes Kontingent, auch nach den Wiederholungsversuchen. |
| *Der Anbieter konnte die Anfrage nicht verarbeiten.* | Der Dienst hat ein Problem (5xx), auch nach den Wiederholungsversuchen. |
| *Der Anbieter hat nicht rechtzeitig geantwortet.* | Erhöhe das **Zeitlimit (Sekunden)** oder prüfe den Server. |
| *Verbindung zum Anbieter nicht möglich.* | Der Server läuft nicht oder ist von Marginalia aus nicht erreichbar, oder die URL enthält einen Tippfehler. |
| *Der Anbieter hat die Anfrage abgelehnt.* | Eine andere Ablehnung, oft ein nicht unterstütztes Feld in den **Zusätzlichen Parametern** oder ein Reasoning, das das Modell nicht unterstützt. |

<a id="warning-before-sending"></a>

## Warnung vor dem Senden

Marginalia füllt den Kontext nur bis zur **Maximalen Kontextgröße** abzüglich der **Maximalen Antwort-Tokens**. Ist der fertige Prompt trotzdem größer (etwa weil eine Erweiterung Text hinzugefügt hat oder die Token-Zählung danebenlag), zeigt eine Warnung vor dem Senden die geschätzten Größen an. Die Anfrage wird trotzdem gesendet; die API lehnt den Prompt möglicherweise ab oder kürzt ihn.

![Die Warnung über einen Prompt, der möglicherweise nicht passt](../images/context-warning.png)

<a id="reasoning-models"></a>

## Reasoning-Modelle

Reasoning-Modelle denken nach, bevor sie antworten. Ist **Reasoning aktivieren** angehakt, sendet Marginalia den gewählten **Reasoning-Aufwand** (*Keines*, *Niedrig*, *Mittel* oder *Hoch*) als `reasoning_effort` an die API. Mehr Aufwand bedeutet meist besser geplanten Text, aber langsamere und teurere Antworten. Nicht jede API unterstützt den Parameter; schlagen Anfragen mit aktiviertem Reasoning fehl, schalte es aus.

Gibt das Modell sein Reasoning zurück (als `reasoning_content` oder `reasoning`, je nach Server), wird es über dem erzeugten Teil unter **Reasoning anzeigen** angezeigt. Das Reasoning gehört nicht zur Geschichte und wird nicht an das Modell zurückgesendet, es sei denn, du stellst **Das Denken zurücksenden** ein (siehe unten).

<a id="send-the-thinking-back"></a>

### Das Denken zurücksenden

**Das Denken zurücksenden** (in den *Einstellungen je Typ* eines OpenAI-kompatiblen Anbieters) legt fest, ob das Denken des Modells in der nächsten Anfrage als `reasoning_content` seiner früheren Nachricht an das Modell zurückgeht:

| Wert | |
|---|---|
| **Keines** (Standard) | Nie. Richtig für die meisten Modelle und Server sowie für APIs, die ein unbekanntes Feld ablehnen. |
| **Zwischen Tool-Aufrufen** | Nur solange die Tool-Aufrufe einer Antwort weiterlaufen: Das Modell, das ein Tool aufgerufen hat, bekommt sein eigenes Denken mit dem Ergebnis zurück. Eine neue Nachricht von dir beginnt ohne das alte Denken. Google verlangt das für Gemma 4 (*Gedanken dürfen zwischen den Funktionsaufrufen nicht entfernt werden*, aber zwischen den Runden schon). |
| **Immer** | Auch bei früheren Antworten mit Denken, wo der Chat es behält (der Plot-Assistent des Arc Outline tut das). Ein Server, dessen Vorlage altes Denken von selbst verwirft, wie der von Gemma 4, ignoriert es. |

Es gilt für die Tool-Schleife (den Plot-Assistenten, die Generatoren mit Tools) und für Agenten: Ein Agent bekommt das Denken seiner früheren Runden zurück, solange die Tool-Aufrufe einer Runde weiterlaufen (das Denken wird in der Konsole mit der Nachricht gehalten, nicht in der gespeicherten Nachricht). Der Server muss das Feld annehmen: llama.cpp tut das (es liest `reasoning_content`, nicht `reasoning`), auch hinter LiteLLM. Ein Anbieter, der unbekannte Felder in Nachrichten ablehnt, schlägt fehl; lass die Einstellung dann auf *Keines*.

![Reasoning eines erzeugten Teils](../images/reasoning-view.png)

Auch das Reasoning zählt zur Antwort: Gib Reasoning-Modellen eine größere **Maximale Antwort-Tokens**, sonst kann der Text nach einem langen Reasoning abgeschnitten werden.

<a id="token-counting"></a>

## Tokenzählung

Marginalia zählt Tokens, um zu entscheiden, wie viel von der Geschichte in den Prompt passt. Wenn möglich fragt es den Anbieter, und es probiert diese Methoden der Reihe nach:

1. den Zähl-Endpunkt für Tokens von OpenAI (`/v1/responses/input_tokens`),
2. den `/tokenize`-Endpunkt von llama.cpp,
3. den `/tokenize`-Endpunkt von LiteLLM,
4. den `/v1/messages/count_tokens`-Endpunkt von LiteLLM (für Anthropic-Modelle),
5. eine lokale Schätzung mit dem Tokenizer von OpenAI.

Die erste Methode, die funktioniert, wird danach verwendet. Die lokale Schätzung liegt bei den meisten Modellen nah dran, ist aber nicht exakt; ein weiterer Grund, die **Maximale Kontextgröße** etwas unter dem echten Limit zu halten. Anbieter vom Typ *Erweiterung* zählen so, wie es ihre Erweiterung vorsieht; die Anbieter [Google Gemini](extensions/google-genai.md) und [Anthropic Claude](extensions/anthropic.md) bitten den Anbieter, selbst zu zählen.

## Prompt-Cache

Jede Anfrage, die Marginalia für eine Art von Arbeit stellt (einen neuen Teil, eine Anmerkung, eine Neufassung, eine Zusammenfassung ...), trägt den Header `X-Session-ID`, einen Hash, der für denselben Benutzer und dieselbe Art gleich bleibt. Ein Server oder Proxy, der einen Prompt-Cache führt, kann ihn nutzen, um den Cache der vorherigen Anfrage dieser Art zu finden. Ein Server, der ihn nicht kennt, ignoriert ihn. Siehe [Prompt-Cache und Sitzungs-ID](prompt-cache.md), mit einem Beispiel für einen auf der Festplatte gespeicherten Slot von llama.cpp.

<a id="shared-providers"></a>

## Geteilte Anbieter

Auf einer Installation mit mehreren Benutzern kann ein Administrator einen Anbieter **teilen**: Dazu setzt er im Dialog des Anbieters den Haken bei **Mit allen Benutzern geteilt** (das Kontrollkästchen gibt es nur für Administratoren). Jeder Benutzer findet ihn dann in der Liste **Modell** seiner Bücher und im Reiter **Inferenzanbieter**, markiert mit *Geteilt* **Ja**. Sie können ihn verwenden, aber nicht öffnen, seine Adresse oder seinen API-Schlüssel sehen, ihn ändern oder löschen; das kann nur sein Besitzer. Bücher, Sicherungen und Wiederherstellungen, die auf einen geteilten Anbieter verweisen, funktionieren weiter für alle, die ihn sehen können.

![Ein geteilter Anbieter im Reiter Inferenzanbieter eines normalen Benutzers](../images/provider-shared-user.png)

Ein geteilter Anbieter erlaubt den anderen Benutzern, das Kontingent des Besitzers beim Dienst zu verbrauchen. Um zu verhindern, dass Benutzer eigene Anbieter hinzufügen, sodass sie nur die geteilten nutzen können, siehe [Benutzer und Anbieter](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Limits geteilter Anbieter

Ein Administrator kann festlegen, was jeder Benutzer mit einem geteilten Anbieter tun darf. Im Tab **Inferenzanbieter** hat der Administrator, dem ein geteilter Anbieter gehört, in dessen Zeile einen Button **Limits** (ein Benutzer mit einem Schloss). Er öffnet eine Tabelle mit den festgelegten Limits. Mit **Limit hinzufügen** wird ein neues angelegt: Wählen Sie den Benutzer und einen Limit-Typ.

![Die Limits eines geteilten Anbieters](../images/provider-limits.png)

| Limit-Typ | Bedeutung |
|---|---|
| Unbegrenzt | Kein Limit (dasselbe wie ohne Limit). |
| Token-Limit | Der Benutzer kann pro Zeitraum bis zur angegebenen Anzahl an Tokens verbrauchen. |
| Nicht verfügbar | Der Benutzer kann den Anbieter nicht nutzen; alles, was eine Anfrage an ihn senden würde, schlägt fehl. |

Ein **Token-Limit** hat einen **reset** (täglich, wöchentlich ab Montag oder monatlich; Kalenderzeiträume in der Zeitzone des Servers) sowie drei Grenzen, die jeweils leer bleiben können, wenn es keine Begrenzung geben soll: die **sent** Tokens, die **generated** Tokens und beide zusammen (**in total**). Welche Grenze zuerst erreicht wird, stoppt den Benutzer bis zum nächsten Reset. Sind alle drei gesetzt, darf die Summe aus den Grenzen für **sent** und **generated** nicht unter dem Wert von **in total** liegen, weil sonst **in total** nie erreicht werden könnte. Andernfalls verweigert der Dialog das Speichern. Ein Beispiel: Ein **in total** von 100.000 mit 100.000 **sent** und 20.000 **generated** ist in Ordnung. Ein **in total** von 100.000 mit 20.000 **sent** und 10.000 **generated** ist es nicht.

![Ein Limit hinzufügen](../images/provider-limit-edit.png)

Es gibt keinen Timer für den Reset: Die verbrauchten Tokens beginnen bei null, sobald der Benutzer den Anbieter (oder seine Limits) zum ersten Mal in einem neuen Zeitraum verwendet. Die Größe einer Anfrage ist schon vor dem Senden bekannt. Deshalb wird eine Anfrage abgelehnt, deren Prompt den Benutzer über das Limit **sent** oder **in total** bringen würde. Der Prompt wird mit der eigenen Schätzung von Marginalia gemessen, die von der Zählung des Dienstes leicht abweichen kann. Die **generated** Tokens sind im Voraus nicht bekannt: Sie werden erst gezählt, wenn die Anfrage endet. Deshalb wird der Benutzer erst gestoppt, wenn das Limit erreicht ist, und die letzte Antwort kann es überschreiten. Benutzer ohne Eintrag in der Tabelle sind nicht begrenzt.

Um zu verhindern, dass die generierten Tokens das Limit überschreiten, aktivieren Sie **Hard limit** (nur verfügbar bei einem Limit für die generierten Tokens). Eine Anfrage reserviert Platz für die größte Antwort, die sie zulässt (die **Max Response Size** des Anbieters oder den kleineren Wert des Protokolls). Mit einem Hard limit wird sie abgelehnt, wenn diese Reserve nicht in den Rest des Limits für die generierten Tokens passt. Ein Hard limit, das kleiner ist als die Antwortgröße des Anbieters, lehnt also jede Anfrage ab. Die Tabelle und die Info des Benutzers zeigen *hart* neben einem solchen Limit. Das Hard limit betrifft nur die generierten Tokens. Das Limit **in total** kann durch die letzte Antwort trotzdem überschritten werden.

Jeder Benutzer sieht die geteilten Anbieter in seinem Tab als schreibgeschützte Zeilen. Der Button **info** (der Kreis mit einem *i*) eines geteilten Anbieters zeigt das eigene Limit: was in diesem Zeitraum verbraucht wurde, die Limits und wann sie zurückgesetzt werden. Ist ein Limit erreicht, zeigen der Story-Editor, die Zusammenfassungen und die Erweiterungen eine Fehlermeldung, statt die Anfrage zu senden.

![Die Limits, die ein Benutzer sieht, mit dem Verbrauch in diesem Zeitraum](../images/provider-limit-info.png)

<a id="usage"></a>

## Nutzung

Der Tab **Inferenzanbieter** zeigt pro Anbieter die **Gesendeten Tokens** (den Prompt, „Prefill“) und die **Generierten Tokens** (die Antwort, einschließlich Reasoning), die Sie damit verbraucht haben, summiert über alle Ihre Bücher, Zusammenfassungen und Erweiterungen. Ein geteilter Anbieter hat für jeden Benutzer einen eigenen Zähler, sodass Sie nur Ihren Anteil sehen.

Administratoren finden außerdem auf dem Tab den Button **Nutzung je Benutzer**. Er öffnet eine Tabelle mit dem Verbrauch jedes Benutzers bei jedem Anbieter (gesendete Tokens, generierte Tokens, Anfragen und Zeitpunkt der letzten Anfrage).

![Nutzung je Benutzer](../images/provider-usage.png)

Die Zahlen sind das, was der Dienst meldet. Ein Dienst, der keine Zahlen meldet (oder eine Anfrage, die Sie mittendrin abbrechen), wird mit der lokalen Schätzung aus [Token-Zählung](#token-counting) gezählt, daher sind diese Zahlen ungefähr. Eine Anfrage, die fehlschlägt, bevor Text eingetroffen ist, wird nicht gezählt. Solange ein Administrator für Sie kein [Limit](#limits-of-shared-providers) bei einem geteilten Anbieter festgelegt hat, dienen die Zählungen nur zur Information.

!!!
Marginalia bittet den Dienst, die Token-Zahlen zu melden (`stream_options` mit `include_usage`). Um das für einen Anbieter zu ändern, tragen Sie Ihr eigenes `stream_options` in dessen **Zusätzliche Parameter** ein, zum Beispiel `{"stream_options": {"include_usage": false}}`. Das ersetzt, was Marginalia sendet, und die lokale Schätzung wird verwendet.
!!!

<a id="using-a-provider-in-a-book"></a>

## Einen Anbieter in einem Buch verwenden

Jedes Buch hat sein Modell im Feld **Modell** seines Tabs **Info**. Um einen Anbieter für alle neuen Bücher zu verwenden, wählen Sie ihn im Tab **Einstellungen** als **Standardmodell** aus. Ein Wechsel des Anbieters eines Buches wirkt sich mit dem nächsten generierten Teil aus. Zuvor geschriebene Teile bleiben unverändert.

<a id="troubleshooting"></a>

## Fehlerbehebung

| Problem | Was zu prüfen ist |
|---|---|
| *Modellliste konnte nicht geladen werden* | Die URL endet mit `/v1`, API-Schlüssel, der Server läuft und ist von Marginalia aus erreichbar. Geben Sie die Modell-ID ein, falls der Dienst keine Modellliste hat. |
| *Der Anbieter hat nicht rechtzeitig geantwortet.* | Erhöhen Sie das **Zeitlimit (Sekunden)** des Anbieters. Reasoning-Modelle brauchen unter Umständen Minuten. |
| *Der Anbieter begrenzt die Anfragen.* | Warten Sie, oder erhöhen Sie die **Wiederholungen**. |
| *Dem Buch fehlt ein Modell.* | Dem Buch ist kein Anbieter zugewiesen. Wählen Sie einen unter **Info → Modell** aus. |
| *Das Kontextlimit reicht nicht aus.* | Der Prompt ohne Story passt nicht in den Kontext abzüglich der Antwort-Tokens. Erhöhen Sie die **Maximale Kontextgröße** (oder die **Maximale Kontext-Tokens** des Protokolls), senken Sie die **Maximalen Antwort-Tokens**, oder kürzen Sie die Vorlagen und Einträge des Lorebooks. |
| Der Text bricht mitten im Satz ab | Die **Maximalen Antwort-Tokens** (oder die **Max Reply Tokens** des Protokolls) sind zu niedrig, besonders bei Reasoning-Modellen. |
| Fehler der API bei aktiviertem Reasoning | Die API unterstützt `reasoning_effort` nicht. Deaktivieren Sie **Reasoning aktivieren**. |
