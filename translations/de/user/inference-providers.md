---
label: Inferenzanbieter
order: 980
icon: server
---

# Inferenzanbieter

Ein Inferenzanbieter ist eine Verbindung zu einem Modell: die API-Adresse, der API-Schlüssel, das Modell und seine Grenzen. Jedes Buch verwendet genau einen Anbieter. Du kannst beliebig viele anlegen, zum Beispiel ein großes Cloud-Modell für die Geschichte und ein kleines lokales für Experimente, und ein Buch jederzeit zwischen ihnen umschalten.

Anbieter gehören zu deinem Konto. Andere Benutzer sehen sie und ihre API-Schlüssel nicht. API-Schlüssel werden verschlüsselt gespeichert (siehe [Der Datenordner](administration/index.md#the-data-folder)).

Zum Hinzufügen eines Anbieters brauchst du die Berechtigung **Create own models**, die dir ein Administrator erteilt (Administratoren haben sie immer, siehe [Berechtigungen](administration/permissions.md)). Ohne sie kannst du nur die Anbieter nutzen, die die Administratoren mit dir [teilen](#shared-providers); die Schaltfläche **Add Inference Provider** wird nicht angezeigt.

![Der Reiter Inference Providers](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## Unterstützte APIs

Marginalia arbeitet mit jeder API, die die Schnittstelle **OpenAI Chat Completions** implementiert (`/v1/chat/completions`, mit Streaming). Die meisten Dienste und lokalen Server tun das:

| Dienst | OpenAI URL |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Die Ports sind die Standardwerte des jeweiligen Servers; verwende die, auf denen dein Server tatsächlich läuft.

!!!warning Port 8080
Die Docker-Version von Marginalia verwendet ebenfalls Port 8080. Wenn du llama.cpp auf demselben Rechner betreibst, starte es auf einem anderen Port (`llama-server --port 8081`).
!!!

!!!
Bei der [Docker-Version](getting-started/docker-server.md) bedeutet `localhost` den Marginalia-Container. Verwende `http://host.docker.internal:<port>/v1` für einen Modellserver auf demselben Rechner, siehe [Modelle auf demselben Rechner](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Einen Provider hinzufügen

Öffnen Sie den Tab **Inference Providers** und klicken Sie auf **Add Inference Provider**. Bestehende Provider bearbeiten Sie mit dem Stift-Button und löschen sie mit dem Papierkorb-Button. Der Klonen-Button (zwei Blätter) erstellt eine Kopie eines Providers unter einem Namen, den Sie bestätigen (der Name des Originals mit *(Copy)*). Die Kopie hat dieselbe Adresse, dasselbe Modell, denselben **API key** (bzw. dieselben Einstellungen der Erweiterung), dieselben Reasoning-Einstellungen, Token-Grenzen und Parameter. Dadurch braucht ein zweites Modell beim selben Dienst nur einen Namen und einen Klick. Die Kopie ist nicht geteilt und hat keine eigenen Limits oder Nutzungswerte. Sie können nur eigene Provider klonen, nicht einen geteilten Provider eines Administrators, dessen Schlüssel Sie nicht sehen dürfen, und nur dann, wenn Sie Provider anlegen dürfen.

Oben im Dialog stehen diese Felder:

| Feld | |
|---|---|
| **Name** | Ihr Name für den Provider. Er wird angezeigt, wenn Sie für ein Buch ein Modell auswählen. Nennen Sie das Modell mit, z. B. `OpenRouter - Claude Sonnet`. |
| **Type** | Die Art der API: **OpenAI Compatible** oder **Extension** für einen Provider, den eine [Erweiterung](extensions/index.md) mitbringt. Letzterer wird nur angeboten, solange eine solche Erweiterung geladen ist, z. B. [Google Gemini](extensions/google-genai.md) oder [Anthropic Claude](extensions/anthropic.md). Der Typ lässt sich nach dem Anlegen des Providers nicht mehr ändern. |
| **Provider** | Nur für den Typ **Extension**: welcher Provider der Erweiterung gemeint ist. Dessen eigene Einstellungen füllen den Tab *Per Type Settings*. |

<a id="per-type-settings"></a>

### Einstellungen je Typ

![Per Type Settings with the model list loaded](../images/inference-provider.png)

Der Tab ist höher als der Dialog und lässt sich scrollen. Die unteren Felder:

![Timeout, retries, additional parameters and Test Connection](../images/inference-provider-2.png)

| Feld | |
|---|---|
| **OpenAI URL (ends with /v1)** | Die Basisadresse der API, siehe [die Tabelle oben](#supported-apis). Ohne `/chat/completions`. |
| **Api Key** | Der Schlüssel des Dienstes. Ein lokaler Server prüft ihn meist nicht: Lassen Sie das Feld leer, dann sendet Marginalia den Platzhalter `none`. |
| **Model** | Das zu verwendende Modell. Klicken Sie auf **Refresh**, um die Modellliste von der API zu laden, oder tippen Sie die Modell-ID selbst ein (z. B. `anthropic/claude-sonnet-4.5` bei OpenRouter). |
| **Timeout (seconds)** | Wie lange auf die Antwort der API gewartet wird und auf weiteren Text, sobald sie zu schreiben begonnen hat. Der Standard ist 300. Reasoning-Modelle brauchen vor dem ersten Text unter Umständen Minuten. Eine Anfrage, die so lange nichts hört, schlägt mit einer Timeout-Meldung fehl. Das Nachdenken eines Modells zählt als Text, ein Modell, das weiterdenkt, wird also nicht abgebrochen. Eine Änderung gilt ab der nächsten Anfrage, auch für einen Agenten, der gerade arbeitet. |
| **Retries** | Wie oft eine Anfrage wiederholt wird, wenn die API mit einem Rate-Limit (429) oder einem Serverfehler (5xx) antwortet oder nicht erreichbar ist. Die Wartezeit zwischen den Versuchen wächst, und ein `Retry-After`-Header der API wird beachtet. Der Standard ist 2, 0 schaltet das Wiederholen aus. Wiederholt wird nur der Beginn einer Anfrage. Text, der schon angekommen ist, wird nie doppelt geschrieben. |
| **Tokenizer** | Wie Marginalia die Tokens der Texte für diesen Provider zählt. **Automatic** (der Standard) probiert die Tokenizer-Endpunkte des Servers nacheinander aus (OpenAI, llama.cpp, LiteLLM) und verwendet den ersten, der antwortet. Antwortet keiner, dient eine lokale Schätzung. Wählen Sie einen Tokenizer aus, um nur diesen zu verwenden, oder **LOCAL_TOKKIT** für die lokale Schätzung, die nie den Server fragt. Das ist nützlich, wenn das Zählen den Server ein anderes Modell laden ließe. Ein Tokenizer aus einem Plugin steht hier ebenfalls. Er bekommt die Adresse, das Modell und den Text, nie den API-Schlüssel, und nur dann, wenn Sie ihn wählen. **Test tokenizer** zählt einen Beispieltext mit allem, was der Provider versuchen würde, und zeigt, was jeweils geantwortet hat. Beim Anlegen eines Providers ist ein Plugin-Tokenizer, der zur Adresse passt, vorausgewählt, bis Sie selbst wählen. |
| **Send the thinking back** | Ob das Modell sein eigenes Nachdenken in der nächsten Anfrage zurückbekommt, siehe [Send the thinking back](#send-the-thinking-back). Gilt nur für OpenAI-kompatible Provider. Die Provider für Anthropic und Google senden von selbst, was deren APIs brauchen. |
| **Additional Parameters** | Ein JSON-Objekt mit zusätzlichen Feldern für jede Anfrage an diesen Provider, z. B. `{"top_k": 40, "min_p": 0.05}` für einen lokalen Server oder `{"provider": {"order": ["anthropic"]}}` für OpenRouter. Ein hier gesetztes Feld gewinnt gegenüber derselben Einstellung aus dem [Protokoll](protocols.md) (z. B. `temperature`) oder der Antwortgrenze (`max_completion_tokens`). Muss ein gültiges JSON-Objekt oder leer sein. |

Beim Bearbeiten eines bestehenden Providers wird der API-Schlüssel nicht erneut angezeigt. Er bleibt unverändert, bis Sie auf **Reset Api Key** klicken und einen neuen eingeben.

**Refresh** fragt bei der API ihre Modellliste ab (`/v1/models`), mit der URL und dem Schlüssel aus dem Formular. Schlägt das mit *Failed to download model list* fehl, prüfen Sie die URL (sie muss mit `/v1` enden), den Schlüssel und ob der Server läuft. Manche Dienste listen keine Modelle auf. Tippen Sie dann die Modell-ID ein.

**Test Connection** prüft die Werte im Formular, ohne zu speichern. Es fragt bei der API die Modellliste ab und danach eine Antwort aus einem Token zum gewählten **Model**, ohne Wiederholungen. Eine Erfolgsmeldung heißt, dass URL, Schlüssel und Modell zusammen funktionieren. Ein Fehler nennt die Ursache (siehe [Error messages](#error-messages)). Manche Dienste haben keine Modellliste. Dann braucht der Test nur die Antwort aus einem Token.

<a id="general-settings"></a>

### Allgemeine Einstellungen

![General Settings of an inference provider](../images/inference-provider-general.png)

| Feld | |
|---|---|
| **Max Context Size** | Pflichtfeld. Wie viele Tokens das Modell insgesamt annimmt, Prompt und Antwort zusammen. |
| **Max Response Tokens** | Pflichtfeld. Die längste Antwort, die das Modell schreiben darf. Sie wird als `max_completion_tokens` an die API gesendet, und so viel vom Kontext bleibt für die Antwort frei. Muss kleiner sein als *Max Context Size*. |
| **Connections** | Wie viele Anfragen gleichzeitig an dieses Modell unterwegs sein dürfen: alle, von allen Benutzern und aller Art (Ihre Generierung, eine Nebenabfrage, eine Übersetzung, eine Annotation, [Agenten](agents.md#one-model-several-agents), Plugins). Die anderen warten der Reihe nach, in der Reihenfolge ihres Eintreffens. Eine Generierung, die warten muss, zeigt *Waiting for the model: N requests before this one*, und **Stop** nimmt sie aus der Warteschlange. **1** (der Standard) gilt für einen lokalen Server mit einem Slot (eine GPU). Ein Cloud-Modell, das mehrere Anfragen gleichzeitig beantwortet, kann mehr haben, sodass eine Nebenabfrage neben einer Generierung laufen kann. |
| **GPU lock** | Nur wenn der Administrator [GPUs](administration/gpus.md) eingerichtet hat: die Sperre der GPU, auf der dieses Modell läuft, wenn die Karte mit anderen Modellen oder mit Bild- und Sprachservern geteilt wird. Jede Anfrage wartet dann auf ihren Zug an der GPU (gezählt mit den Anfragen davor). Hält die GPU gerade etwas anderes, gibt sie es frei und lädt dieses Modell, wobei zuerst die Schritte der Sperre ausgeführt werden. Leer: keine Sperre. |
| **Multimodal (accepts images)** | Das Modell versteht Bilder, daher werden die [Bilder der Lorebook-Einträge](lorebooks/lorebooks.md#editing-in-full-screen), die ein Zug verwendet, zusammen mit ihrem Text gesendet. Standardmäßig ist es aus. Aktivieren Sie es nur für ein Modell, das Bilder annimmt, denn ein Modell ohne Bilderkennung lehnt die Anfrage ab und der Zug schlägt fehl. Die Bilder von Einträgen im Lore-Block stehen in einer eigenen Nachricht direkt nach dem System-Prompt (mit einer kurzen Antwort des Modells), die übrigen Bilder stehen bei Ihrem Prompt. |
| **Image placement** | (mit *Multimodal*) Wo die Bilder stehen. *In the system message* (nur OpenAI-kompatibel): Die Bilder des Lore-Blocks gehen in die Systemnachricht selbst, neben den Text des Lorebooks. Das gilt nur für einen Server und ein Chat-Template, die Bilder dort annehmen. Die meisten tun das nicht, und dann schlägt die Anfrage fehl. *Before the text of the user message*: Jedes Bild steht vor dem Text seiner Nachricht, wie Gemma 4 es verlangt. *After the text of the user message* (der Standard): Jedes Bild steht danach. Audio und Dateien folgen immer dem Text. |
| **Function definitions** | Wo das Modell die Funktionen liest, die es aufrufen kann (in [Agenten](agents.md), Nebenabfragen und Plugins, die Funktionen anbieten). *In the system message* (der Standard): nur als Tools der Anfrage, die das Chat-Template des Modells in den System-Turn setzt. *Before* / *After the text of the user message*: zusätzlich ausgeschrieben am Anfang oder Ende der letzten Benutzernachricht. Die Tools werden immer ebenfalls gesendet, denn der Server braucht sie, um einen Aufruf zu erkennen. |
| **Function call rule**, **Rule placement** | Eine Regel, die jeder Anfrage hinzugefügt wird, die Funktionen anbietet. Sie ist für Modelle gedacht, die einen Aufruf als Text in ihrer Antwort schreiben statt ihn auszuführen, oder die aufhören zu rufen. Zum Beispiel: *You act only through function calls. Never write a call as text in your reply; to use a function, call it.* Die Platzierung legt fest, wohin die Regel kommt: in die Systemnachricht oder an den Anfang bzw. das Ende der letzten Benutzernachricht (der Standard). Gemma 4 folgt ihr in der Benutzernachricht deutlich besser. Leer: keine Regel. |
| **Needs Jailbreak**, **Jailbreak Prompt** | Ein zusätzlicher Prompt für Modelle, die bestimmte Inhalte verweigern. Ist das Feld angehakt, steht der Jailbreak-Prompt ganz am Anfang des Prompts. |
| **Enable Reasoning**, **Reasoning Effort** | Für Reasoning-Modelle („denkende“ Modelle), siehe [Reasoning-Modelle](#reasoning-models). |

Marginalia hält **Max Response Tokens** des Kontexts für die Antwort frei und füllt den Rest mit dem Prompt. Vorlagen, [Lorebook](lorebooks/index.md)-Einträge und [Zusammenfassungen](books/summaries.md) werden mitgezählt, und die ältesten Teile der Geschichte, die nicht hineinpassen, bleiben weg. Bei einem Kontext von 32 000 Tokens und Antworten von bis zu 2 000 Tokens bekommt der Prompt 30 000 Tokens.

Übernehmen Sie **Max Context Size** aus der Dokumentation des Dienstes, und setzen Sie den Wert etwas niedriger an. Tokenzahlen sind nicht immer exakt, und eine Anfrage, die die echte Grenze überschreitet, schlägt fehl. Ein [Protokoll](protocols.md) kann für manche Bücher beide Grenzen senken, aber nicht erhöhen.
<a id="error-messages"></a>

## Fehlermeldungen

Wenn die API eine Anfrage ablehnt, zeigt Marginalia statt eines internen Fehlers an, was zu beheben ist, gefolgt von dem, was die API selbst gemeldet hat:

![Ein Fehler des Anbieters, dem Nutzer angezeigt](../images/error-dialog.png)

| Meldung | Ursache |
|---|---|
| *The provider rejected the API key.* | Falscher, abgelaufener oder fehlender **Api Key**. |
| *The API key is not allowed to use this model or endpoint.* | Der Schlüssel ist gültig, hat aber keinen Zugriff auf das Modell. |
| *The provider does not know the configured model.* | Falsches **Model**; mit **Refresh** lassen sich die Namen anzeigen, die die API kennt. |
| *Nothing was found at the configured URL.* | Falsche **OpenAI URL**; sie endet meist auf `/v1`. |
| *The prompt does not fit into the context of the model.* | **Max Context Size** ist höher, als das Modell tatsächlich akzeptiert. Setze den Wert niedriger. |
| *The provider is limiting requests.* | Ratenbegrenzung oder aufgebrauchtes Kontingent, auch nach den Wiederholungsversuchen. |
| *The provider failed to process the request.* | Der Dienst hat ein Problem (5xx), auch nach den Wiederholungsversuchen. |
| *The provider did not answer in time.* | **Timeout (seconds)** erhöhen oder den Server prüfen. |
| *Cannot connect to the provider.* | Der Server läuft nicht oder ist von Marginalia aus nicht erreichbar, oder die URL enthält einen Tippfehler. |
| *The provider rejected the request.* | Eine andere Ablehnung, oft ein nicht unterstütztes Feld in **Additional Parameters** oder ein Reasoning, das das Modell nicht unterstützt. |

<a id="warning-before-sending"></a>

## Warnung vor dem Senden

Marginalia füllt den Kontext nur bis **Max Context Size** minus **Max Response Tokens**. Ist der fertige Prompt trotzdem größer (etwa weil eine Erweiterung Text hinzugefügt hat oder die Token-Zählung danebenlag), zeigt eine Warnung vor dem Senden die geschätzten Größen an. Die Anfrage wird trotzdem gesendet; die API lehnt sie möglicherweise ab oder kürzt den Prompt.

![Die Warnung vor einem Prompt, der möglicherweise nicht passt](../images/context-warning.png)

<a id="reasoning-models"></a>

## Reasoning-Modelle

Reasoning-Modelle denken nach, bevor sie antworten. Ist **Enable Reasoning** aktiviert, sendet Marginalia den gewählten **Reasoning Effort** (*None*, *Low*, *Medium* oder *High*) als `reasoning_effort` an die API. Mehr Aufwand bedeutet meist besser geplanten Text, aber langsamere und teurere Antworten. Nicht jede API unterstützt den Parameter; schlagen Anfragen mit aktiviertem Reasoning fehl, schalte es aus.

Gibt das Modell sein Reasoning zurück (als `reasoning_content` oder `reasoning`, je nach Server), wird es über dem generierten Teil unter **View Reasoning** angezeigt. Das Reasoning gehört nicht zur Geschichte und wird nicht an das Modell zurückgeschickt, es sei denn, du aktivierst **Send the thinking back** (unten).

<a id="send-the-thinking-back"></a>

### Das Denken zurückschicken

**Send the thinking back** (in *Per Type Settings* eines OpenAI-kompatiblen Anbieters) legt fest, ob das Denken des Modells in der nächsten Anfrage als `reasoning_content` seiner früheren Nachricht an es zurückgeht:

| Wert | |
|---|---|
| **None** (Standard) | Nie. Richtig für die meisten Modelle und Server sowie für APIs, die ein unbekanntes Feld ablehnen. |
| **Between tool calls** | Nur solange die Tool-Aufrufe einer Antwort weiterlaufen: Das Modell, das ein Tool aufgerufen hat, bekommt sein eigenes Denken mit dem Ergebnis zurück. Eine neue Nachricht von dir beginnt ohne das alte Denken. Google verlangt dies für Gemma 4 (*thoughts must not be removed between the function calls*, aber zwischen den Turns entfernt). |
| **Always** | Auch bei früheren Antworten mit Denken, wo der Chat es behält (der Plot-Assistent des Arc Outline tut das). Ein Server, dessen Template altes Denken von sich aus verwirft, wie das von Gemma 4, ignoriert es. |

Das gilt für die Tool-Schleife (den Plot-Assistenten und die Generatoren, die Tools verwenden) und für Agenten: Ein Agent bekommt das Denken seiner früheren Runden zurück, solange die Tool-Aufrufe eines Turns weiterlaufen (das Denken wird in der Konsole mit der Nachricht gespeichert, nicht in der gespeicherten Nachricht). Der Server muss das Feld annehmen: llama.cpp tut das (es liest `reasoning_content`, nicht `reasoning`), auch hinter LiteLLM. Ein Anbieter, der unbekannte Felder in Nachrichten ablehnt, schlägt fehl; lass die Option dann auf *None*.

![Reasoning eines generierten Teils](../images/reasoning-view.png)

Reasoning zählt zur Antwort: Gib Reasoning-Modellen ein größeres **Max Response Tokens**, sonst kann der Text nach einem langen Reasoning abgeschnitten werden.

<a id="token-counting"></a>

## Token-Zählung

Marginalia zählt Tokens, um zu entscheiden, wie viel von der Geschichte in den Prompt passt. Wenn möglich fragt es den Anbieter, und es probiert diese Methoden in dieser Reihenfolge:

1. den Token-Zähl-Endpunkt von OpenAI (`/v1/responses/input_tokens`),
2. den `/tokenize`-Endpunkt von llama.cpp,
3. den `/tokenize`-Endpunkt von LiteLLM,
4. den `/v1/messages/count_tokens`-Endpunkt von LiteLLM (für Anthropic-Modelle),
5. eine lokale Schätzung mit dem Tokenizer von OpenAI.

Die erste Methode, die funktioniert, wird ab dann verwendet. Die lokale Schätzung ist bei den meisten Modellen nah dran, aber nicht exakt, und das ist ein weiterer Grund, **Max Context Size** etwas unter dem echten Limit zu halten. Anbieter vom Typ *Extension* zählen so, wie ihre Erweiterung es vorsieht; die Anbieter [Google Gemini](extensions/google-genai.md) und [Anthropic Claude](extensions/anthropic.md) lassen den Anbieter zählen.

## Prompt-Cache

Jede Anfrage, die Marginalia für eine Art von Arbeit stellt (einen neuen Teil, eine Anmerkung, eine Neufassung, eine Zusammenfassung ...), trägt den Header `X-Session-ID`, einen Hash, der für denselben Nutzer und dieselbe Art gleich bleibt. Ein Server oder Proxy, der einen Prompt-Cache führt, kann ihn verwenden, um den Cache der vorherigen Anfrage dieser Art zu finden. Ein Server, der ihn nicht kennt, ignoriert ihn. Siehe [Prompt-Cache und Session-ID](prompt-cache.md), mit einem Beispiel für einen auf Festplatte gehaltenen Slot von llama.cpp.

<a id="shared-providers"></a>

## Gemeinsam genutzte Anbieter

Auf einer Installation mit mehreren Nutzern kann ein Administrator einen Anbieter **teilen**: Aktiviere **Shared with all users** im Dialog des Anbieters (das Kontrollkästchen gibt es nur für Administratoren). Jeder Nutzer findet ihn dann in der Liste **Model** seiner Bücher und in seinem Tab **Inference Providers**, markiert mit *Shared* **Yes**. Sie können ihn verwenden, aber nicht öffnen, seine Adresse oder seinen API-Schlüssel sehen, ihn ändern oder löschen; nur sein Besitzer kann das. Bücher, Sicherungen und Wiederherstellungen, die sich auf einen geteilten Anbieter beziehen, funktionieren weiter für alle, die ihn sehen können.

![Ein geteilter Anbieter im Tab Inference Providers eines regulären Nutzers](../images/provider-shared-user.png)

Ein geteilter Anbieter erlaubt den anderen Nutzern, das Kontingent des Besitzers beim Dienst auszugeben. Um zu verhindern, dass Nutzer eigene Anbieter hinzufügen und so nur die geteilten verwenden können, siehe [Benutzer und Anbieter](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Grenzen geteilter Provider

Ein Administrator kann festlegen, was jeder Benutzer mit einem geteilten Provider tun darf. Im Tab **Inference Providers** hat der Administrator, dem ein geteilter Provider gehört, in dessen Zeile einen Button **Limits** (ein Benutzer mit einem Schloss). Er öffnet eine Tabelle mit den festgelegten Grenzen. **Add limit** legt eine neue Grenze an: Wähle den Benutzer und einen Grenzentyp.

![Die Grenzen eines geteilten Providers](../images/provider-limits.png)

| Grenzentyp | Bedeutung |
|---|---|
| Unbegrenzt | Keine Grenze (dasselbe wie keine Grenze zu haben). |
| Token-Limit | Der Benutzer kann pro Zeitraum bis zur angegebenen Anzahl an Tokens verwenden. |
| Nicht verfügbar | Der Benutzer kann den Provider nicht verwenden; alles, was eine Anfrage an ihn senden würde, schlägt fehl. |

Ein **Token-Limit** hat einen **reset** (täglich, wöchentlich ab Montag oder monatlich; Kalenderzeiträume in der Zeitzone des Servers) und drei Grenzen, die jeweils leer gelassen werden können, sodass es keine Grenze gibt: die **gesendeten** Tokens, die **generierten** Tokens und beide zusammen (**insgesamt**). Welche Grenze zuerst erreicht wird, stoppt den Benutzer bis zum nächsten Reset. Sind alle drei gesetzt, darf die Grenze für gesendet plus die für generiert nicht unter der für insgesamt liegen (die Gesamtgrenze könnte sonst nie erreicht werden). Andernfalls verweigert der Dialog das Speichern. Zum Beispiel ist insgesamt 100,000 mit 100,000 gesendet und 20,000 generiert in Ordnung, insgesamt 100,000 mit 20,000 gesendet und 10,000 generiert dagegen nicht.

![Einen Grenzwert hinzufügen](../images/provider-limit-edit.png)

Nichts wird per Timer zurückgesetzt: Die verbrauchten Tokens beginnen bei null, wenn der Benutzer den Provider in einem neuen Zeitraum zum ersten Mal verwendet (oder seine Grenzen ansieht). Die Größe einer Anfrage ist vor dem Senden bekannt. Daher wird eine Anfrage abgelehnt, deren Prompt den Benutzer über die Grenze **gesendet** oder **insgesamt** bringen würde. Der Prompt wird mit Marginalias eigener Schätzung gemessen, die vom Zählwert des Dienstes leicht abweichen kann. Die **generierten** Tokens sind im Voraus nicht bekannt: Sie werden gezählt, wenn die Anfrage endet. Daher wird der Benutzer erst gestoppt, wenn die Grenze erreicht ist, und die letzte Antwort kann sie überschreiten. Benutzer ohne Grenze in der Tabelle sind nicht begrenzt.

Damit die generierten Tokens nicht überschritten werden, aktiviere **Hard limit** (nur bei einer Grenze für die generierten Tokens verfügbar). Eine Anfrage reserviert Platz für die größte Antwort, die sie erlaubt (die **Max Response Size** des Providers oder den kleineren Wert des Protokolls). Mit einer harten Grenze wird sie abgelehnt, wenn diese Reserve nicht in den Rest der generierten Grenze passt. Eine harte Grenze, die niedriger ist als die Antwortgröße des Providers, lehnt daher jede Anfrage ab. Die Tabelle und die Info des Benutzers zeigen *hard* neben einer solchen Grenze. Die harte Grenze gilt nur für die generierten Tokens; die **insgesamt**-Grenze kann durch die letzte Antwort weiterhin überschritten werden.

Jeder Benutzer sieht die geteilten Provider in seinem Tab als schreibgeschützte Zeilen. Der Button **info** (der Kreis mit einem *i*) eines geteilten Providers zeigt die eigene Grenze: was in diesem Zeitraum verbraucht wurde, die Grenzen und wann sie zurückgesetzt werden. Ist eine Grenze erreicht, zeigen der Story-Editor, Zusammenfassungen und Erweiterungen einen Fehler, statt die Anfrage zu senden.

![Die Grenzen, die ein Benutzer sieht, mit dem Verbrauch in diesem Zeitraum](../images/provider-limit-info.png)

<a id="usage"></a>

## Nutzung

Der Tab **Inference Providers** zeigt pro Provider die **Tokens sent** (der Prompt, „prefill“) und **Tokens generated** (die Antwort einschließlich Reasoning), die du damit verbraucht hast. Die Werte sind über alle deine Bücher, Zusammenfassungen und Erweiterungen aufsummiert. Ein geteilter Provider hat für jeden Benutzer einen eigenen Zähler, sodass du nur deinen Anteil siehst.

Administratoren erhalten außerdem einen Button **Usage per user** im Tab. Er öffnet eine Tabelle mit dem, was jeder Benutzer von jedem Provider verbraucht hat (Tokens sent, Tokens generated, Anfragen und der Zeitpunkt der letzten Anfrage).

![Nutzung pro Benutzer](../images/provider-usage.png)

Die Zahlen sind das, was der Dienst meldet. Ein Dienst, der sie nicht meldet (oder eine Anfrage, die du mitten im Vorgang abbrichst), wird mit der lokalen Schätzung gezählt, die unter [Token-Zählung](#token-counting) beschrieben ist. Diese Zahlen sind daher nur ungefähr. Eine Anfrage, die fehlschlägt, bevor Text eintrifft, wird nicht gezählt. Sofern ein Administrator dir nicht eine [Grenze](#limits-of-shared-providers) für einen geteilten Provider gesetzt hat, dienen die Zähler nur zur Information.

!!!
Marginalia bittet den Dienst, die Token-Zahlen zu melden (`stream_options` mit `include_usage`). Um das für einen Provider zu ändern, trage deinen eigenen `stream_options` in dessen **Additional Parameters** ein, zum Beispiel `{"stream_options": {"include_usage": false}}`. Er ersetzt, was Marginalia sendet, und es wird die lokale Schätzung verwendet.
!!!

<a id="using-a-provider-in-a-book"></a>

## Einen Provider in einem Buch verwenden

Jedes Buch hat sein Modell im Feld **Model** seines Tabs **About**. Um einen Provider für alle neuen Bücher zu verwenden, wähle ihn im Tab **Settings** als **Default Model** aus. Eine Änderung des Providers eines Buchs wirkt sich auf den nächsten erzeugten Teil aus. Zuvor geschriebene Teile bleiben unverändert.

<a id="troubleshooting"></a>

## Fehlersuche

| Problem | Was zu prüfen ist |
|---|---|
| *Failed to download model list* | URL endet mit `/v1`, API-Schlüssel, der Server läuft und ist von Marginalia aus erreichbar. Gib die Modell-ID ein, wenn der Dienst keine Modellliste hat. |
| *The provider did not answer in time.* | Erhöhe **Timeout (seconds)** des Providers; Reasoning-Modelle brauchen unter Umständen Minuten. |
| *The provider is limiting requests.* | Warte, oder erhöhe **Retries**. |
| *Book is missing model.* | Dem Buch fehlt ein Provider. Wähle einen in **About → Model**. |
| *Contextual limit not sufficient.* | Der Prompt ohne jede Geschichte passt nicht in den Kontext abzüglich der Antwort-Tokens. Erhöhe **Max Context Size** (oder das **Max Context Tokens** des Protokolls), senke **Max Response Tokens** oder kürze die Vorlagen und Lorebook-Einträge. |
| Text bricht in der Mitte ab | **Max Response Tokens** (oder das **Max Reply Tokens** des Protokolls) ist zu niedrig, besonders bei Reasoning-Modellen. |
| Fehler der API bei eingeschaltetem Reasoning | Die API unterstützt `reasoning_effort` nicht. Schalte **Enable Reasoning** aus. |
