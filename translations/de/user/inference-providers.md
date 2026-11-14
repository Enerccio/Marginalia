---
label: Inferenzanbieter
order: 980
icon: server
---

# Inferenzanbieter

Ein Inferenzanbieter ist eine Verbindung zu einem Modell: die API-Adresse, der API-Schlüssel, das Modell und seine Grenzwerte. Jedes
Buch verwendet genau einen Anbieter. Sie können beliebig viele anlegen, zum Beispiel ein großes Cloud-Modell für die Geschichte und ein
kleines lokales Modell für Experimente, und ein Buch jederzeit zwischen ihnen umschalten.

Anbieter gehören zu Ihrem Konto. Andere Benutzer sehen sie nicht, ebenso wenig ihre API-Schlüssel. API-Schlüssel werden verschlüsselt
gespeichert (siehe [Der Datenordner](administration/index.md#the-data-folder)).

Zum Hinzufügen eines Anbieters benötigen Sie die Berechtigung **Eigene Modelle anlegen**, die ein Administrator vergibt (Administratoren
haben sie immer, siehe [Berechtigungen](administration/permissions.md)). Ohne sie können Sie nur die Anbieter nutzen, die Administratoren
mit Ihnen [teilen](#shared-providers); die Schaltfläche **Inferenzanbieter hinzufügen** wird nicht angezeigt.

![Der Reiter Inferenzanbieter](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## Unterstützte APIs

Marginalia arbeitet mit jeder API, die die **OpenAI Chat Completions**-Schnittstelle implementiert (`/v1/chat/completions`, mit
Streaming). Die meisten Dienste und lokalen Server tun das:

| Dienst | OpenAI-URL |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM-Proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Die Ports sind die Standardwerte der jeweiligen Server; verwenden Sie die Ports, auf denen Ihr Server tatsächlich läuft.

!!!warning Port 8080
Die Docker-Version von Marginalia verwendet ebenfalls Port 8080. Wenn Sie llama.cpp auf demselben Rechner betreiben, starten Sie es
auf einem anderen Port (`llama-server --port 8081`).
!!!

!!!
Bei der [Docker-Version](getting-started/docker-server.md) bezeichnet `localhost` den Marginalia-Container. Verwenden Sie
`http://host.docker.internal:<port>/v1` für einen Modellserver auf demselben Rechner, siehe
[Modelle auf demselben Rechner](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Einen Anbieter hinzufügen

Öffne den Reiter **Inferenzanbieter** und klicke auf **Inferenzanbieter hinzufügen**. Bestehende Anbieter bearbeitest du mit der Stift-Schaltfläche und löschst sie mit der Papierkorb-Schaltfläche. Die Kopier-Schaltfläche (zwei Blätter) erstellt eine Kopie eines Anbieters unter einem Namen, den du bestätigst (der Name des Originals mit *(Copy)*): Die Kopie hat dieselbe Adresse, dasselbe Modell, denselben **API-Schlüssel** (bzw. dieselben Einstellungen der Erweiterung), dasselbe Reasoning, dieselben Token-Grenzen und Parameter. So braucht ein zweites Modell beim selben Dienst nur einen Namen und einen Klick. Die Kopie wird nicht geteilt und hat keine eigenen Limits oder Nutzungswerte. Du kannst nur deine eigenen Anbieter klonen (nicht einen geteilten Anbieter eines Administrators, dessen Schlüssel du nicht sehen darfst), und nur, wenn du Anbieter erstellen darfst.

Oben im Dialog:

| Feld | |
|---|---|
| **Name** | Dein Name für den Anbieter, der angezeigt wird, wenn du ein Modell für ein Buch auswählst. Nimm das Modell mit auf, z. B. `OpenRouter - Claude Sonnet`. |
| **Typ** | Die Art der API: **OpenAI-kompatibel** oder **Erweiterung** für einen Anbieter, den eine [Erweiterung](extensions/index.md) mitbringt (er wird nur angeboten, solange eine solche Erweiterung geladen ist, z. B. [Google Gemini](extensions/google-genai.md) oder [Anthropic Claude](extensions/anthropic.md)). Der Typ kann nach dem Erstellen des Anbieters nicht mehr geändert werden. |
| **Anbieter** | Nur für den Typ **Erweiterung**: welcher Anbieter der Erweiterung gemeint ist. Seine eigenen Einstellungen füllen den Reiter *Einstellungen je Typ*. |

<a id="per-type-settings"></a>

### Einstellungen je Typ

![Einstellungen je Typ mit geladener Modellliste](../images/inference-provider.png)

Der Reiter ist höher als der Dialog und lässt sich scrollen. Die unteren Felder:

![Zeitlimit, Wiederholungen, zusätzliche Parameter und Verbindung testen](../images/inference-provider-2.png)

| Feld | |
|---|---|
| **OpenAI-URL (endet auf /v1)** | Die Basisadresse der API, siehe [die Tabelle oben](#supported-apis). Ohne `/chat/completions`. |
| **API-Schlüssel** | Der Schlüssel des Dienstes. Ein lokaler Server prüft ihn meist nicht: Lass das Feld leer, dann sendet Marginalia den Platzhalter `none`. |
| **Modell** | Das zu verwendende Modell. Klicke auf **Aktualisieren**, um die Modellliste von der API zu laden, oder tippe die Modell-ID selbst ein (z. B. `anthropic/claude-sonnet-4.5` bei OpenRouter). |
| **Zeitlimit (Sekunden)** | Wie lange auf die Antwort der API gewartet wird und auf weiteren Text, nachdem sie zu schreiben begonnen hat. Der Standardwert ist 300. Reasoning-Modelle brauchen unter Umständen Minuten bis zum ersten Text; eine Anfrage, die so lange nichts hört, scheitert mit einer Zeitüberschreitungsmeldung (das Denken eines Modells zählt als Text, ein Modell, das weiter denkt, wird also nicht abgebrochen). Eine Änderung gilt für die nächste Anfrage, auch die eines Agenten, der gerade arbeitet. |
| **Wiederholungen** | Wie oft eine Anfrage wiederholt wird, wenn die API mit einem Ratenlimit (429) oder einem Serverfehler (5xx) antwortet oder nicht erreichbar ist. Die Wartezeit zwischen den Versuchen wächst, und ein `Retry-After`-Header der API wird beachtet. Der Standardwert ist 2, 0 schaltet das Wiederholen aus. Nur der Beginn einer Anfrage wird wiederholt; Text, der schon angekommen ist, wird nie doppelt geschrieben. |
| **Tokenizer** | Wie Marginalia die Tokens der Texte für diesen Anbieter zählt. **Automatisch** (der Standard) probiert die Tokenizer-Endpunkte des Servers nacheinander aus (OpenAI, llama.cpp, LiteLLM) und verwendet den ersten, der antwortet; antwortet keiner, wird eine lokale Schätzung genutzt. Wähle einen aus, um nur diesen zu verwenden, oder **LOCAL_TOKKIT** für die lokale Schätzung, die den Server nie aufruft (nützlich, wenn das Zählen dazu führen würde, dass der Server ein anderes Modell lädt). Ein Tokenizer aus einer Erweiterung wird hier ebenfalls aufgelistet; er erhält die Adresse, das Modell und den Text, nie den API-Schlüssel, und nur, wenn du ihn wählst. **Tokenizer testen** zählt einen Beispieltext mit dem, was der Anbieter ausprobieren würde, und zeigt, was jeder beantwortet hat. Beim Erstellen eines Anbieters ist ein Erweiterungs-Tokenizer, der zur Adresse passt, vorausgewählt, bis du selbst wählst. |
| **Das Denken zurücksenden** | Ob das Modell sein eigenes Denken in der nächsten Anfrage zurückbekommt, siehe [Das Denken zurücksenden](#send-the-thinking-back). Nur für OpenAI-kompatible Anbieter: Die Anthropic- und Google-Anbieter senden selbst, was ihre APIs brauchen. |
| **Zusätzliche Parameter** | Ein JSON-Objekt mit zusätzlichen Feldern für jede Anfrage an diesen Anbieter, z. B. `{"top_k": 40, "min_p": 0.05}` für einen lokalen Server oder `{"provider": {"order": ["anthropic"]}}` für OpenRouter. Ein hier gesetztes Feld gewinnt gegenüber derselben Einstellung aus dem [Protokoll](protocols.md) (z. B. `temperature`) oder der Antwortgrenze (`max_completion_tokens`). Muss ein gültiges JSON-Objekt oder leer sein. |

Wenn du einen bestehenden Anbieter bearbeitest, wird der API-Schlüssel nicht erneut angezeigt. Er bleibt unverändert, außer du klickst auf **API-Schlüssel zurücksetzen** und gibst einen neuen ein.

**Aktualisieren** fragt die API nach ihrer Modellliste (`/v1/models`) mit der URL und dem Schlüssel aus dem Formular. Schlägt das mit *Failed to download model list* fehl, prüfe die URL (sie muss auf `/v1` enden), den Schlüssel und ob der Server läuft. Manche Dienste listen keine Modelle auf; tippe dann die Modell-ID selbst ein.

**Verbindung testen** prüft die Werte im Formular, ohne zu speichern: Es fragt die API nach ihrer Modellliste und dann nach einer Antwort mit einem Token für das gewählte **Modell**, ohne Wiederholungen. Eine Erfolgsmeldung bedeutet, dass URL, Schlüssel und Modell zusammen funktionieren. Ein Fehlschlag nennt die Ursache (siehe [Fehlermeldungen](#error-messages)). Manche Dienste haben keine Modellliste; der Test braucht dann nur die Antwort mit einem Token.

<a id="general-settings"></a>

### Allgemeine Einstellungen

![Allgemeine Einstellungen eines Inferenzanbieters](../images/inference-provider-general.png)

| Feld | |
|---|---|
| **Maximale Kontextgröße** | Pflichtfeld. Wie viele Tokens das Modell insgesamt annimmt, Prompt und Antwort zusammen. |
| **Maximale Antwort-Tokens** | Pflichtfeld. Die längste Antwort, die das Modell schreiben darf. Wird als `max_completion_tokens` an die API gesendet, und so viel vom Kontext bleibt für die Antwort frei. Muss kleiner sein als *Maximale Kontextgröße*. |
| **Verbindungen** | Wie viele Anfragen gleichzeitig an dieses Modell unterwegs sein dürfen: alle, von jedem Benutzer und jeder Art (deine Generierung, eine Nebenabfrage, eine Übersetzung, eine Annotation, [Agenten](agents.md#one-model-several-agents), Erweiterungen). Die übrigen warten der Reihe nach, in der Reihenfolge ihres Eintreffens; eine Generierung, die warten muss, zeigt *Warten auf das Modell: N Anfragen vor dieser*, und **Stopp** nimmt sie aus der Warteschlange. **1** (der Standard) für einen lokalen Server mit einem Slot (eine GPU); ein Cloud-Modell, das mehrere Anfragen gleichzeitig beantwortet, kann mehr haben, damit eine Nebenabfrage neben einer Generierung laufen kann. |
| **Lore-Bilder senden** | Sendet die [Bilder der Lorebuch-Einträge](lorebooks/lorebooks.md#editing-in-full-screen), die ein Zug verwendet, zusammen mit deren Text. Standardmäßig aus: Schalte es nur für ein Modell ein, das Bilder annimmt; ein Modell ohne Bildverarbeitung lehnt die Anfrage ab, und der Zug schlägt fehl. Die Bilder von Einträgen im Lore-Block gehen in eine eigene Nachricht direkt nach dem Systemprompt (mit einer kurzen Antwort des Modells), die übrigen mit deinem Prompt. |
| **Bilder in der Systemnachricht** | (OpenAI-kompatibel, mit *Lore-Bilder senden*) Legt die Bilder des Lore-Blocks in die Systemnachricht selbst, neben den Text der Lore. Nur für einen Server und eine Chat-Vorlage, die Bilder dort annehmen; die meisten tun das nicht, und die Anfrage schlägt fehl. |
| **Benötigt Jailbreak**, **Jailbreak-Prompt** | Ein zusätzlicher Prompt für Modelle, die es ablehnen, manche Inhalte zu schreiben. Ist das Feld angekreuzt, wird der Jailbreak-Prompt ganz an den Anfang des Prompts gesetzt. |
| **Reasoning aktivieren**, **Reasoning-Aufwand** | Für Reasoning-Modelle („Denk“-Modelle), siehe [Reasoning-Modelle](#reasoning-models). |

Marginalia hält **Maximale Antwort-Tokens** des Kontexts für die Antwort frei und füllt den Rest mit dem Prompt: Vorlagen, [Lorebuch](lorebooks/index.md)-Einträge und [Zusammenfassungen](books/summaries.md) werden mitgezählt, und die ältesten Teile der Geschichte, die nicht hineinpassen, bleiben weg. Bei einem Kontext von 32 000 Tokens und Antworten von bis zu 2 000 Tokens bekommt der Prompt 30 000 Tokens.

Übernimm **Maximale Kontextgröße** aus der Dokumentation des Dienstes und setze den Wert etwas niedriger: Tokenzählungen sind nicht immer exakt, und eine Anfrage, die die echte Grenze überschreitet, schlägt fehl. Ein [Protokoll](protocols.md) kann beide Grenzen für manche Bücher senken, aber nicht erhöhen.
<a id="error-messages"></a>

## Fehlermeldungen

Lehnt die API eine Anfrage ab, nennt Marginalia statt eines internen Fehlers, was zu beheben ist, gefolgt von dem, was die API selbst gemeldet hat:

![Ein Fehler des Anbieters, dem Benutzer angezeigt](../images/error-dialog.png)

| Meldung | Ursache |
|---|---|
| *Der Anbieter hat den API-Schlüssel abgelehnt.* | Falscher, abgelaufener oder fehlender **API-Schlüssel**. |
| *Der API-Schlüssel darf dieses Modell oder diesen Endpunkt nicht verwenden.* | Der Schlüssel ist gültig, hat aber keinen Zugriff auf das Modell. |
| *Der Anbieter kennt das konfigurierte Modell nicht.* | Falsches **Modell**; mit **Aktualisieren** sehen Sie die Namen, die die API kennt. |
| *Unter der konfigurierten URL wurde nichts gefunden.* | Falsche **OpenAI-URL**; sie endet üblicherweise mit `/v1`. |
| *Der Prompt passt nicht in den Kontext des Modells.* | **Maximale Kontextgröße** ist höher, als das Modell tatsächlich akzeptiert. Senken Sie den Wert. |
| *Der Anbieter begrenzt die Anfragen.* | Rate-Limit oder erschöpftes Kontingent, auch nach den Wiederholungsversuchen. |
| *Der Anbieter konnte die Anfrage nicht verarbeiten.* | Der Dienst hat ein Problem (5xx), auch nach den Wiederholungsversuchen. |
| *Der Anbieter hat nicht rechtzeitig geantwortet.* | **Zeitlimit (Sekunden)** erhöhen oder den Server prüfen. |
| *Verbindung zum Anbieter nicht möglich.* | Der Server läuft nicht oder ist von Marginalia aus nicht erreichbar, oder die URL enthält einen Tippfehler. |
| *Der Anbieter hat die Anfrage abgelehnt.* | Eine andere Ablehnung, oft ein nicht unterstütztes Feld in **Zusätzliche Parameter** oder ein Reasoning, das das Modell nicht unterstützt. |

<a id="warning-before-sending"></a>

## Warnung vor dem Senden

Marginalia füllt den Kontext nur bis zur **Maximalen Kontextgröße** minus den **Maximalen Antwort-Tokens**. Ist der fertige Prompt trotzdem größer (zum Beispiel, weil eine Erweiterung Text hinzugefügt hat oder die Token-Zählung danebenlag), zeigt eine Warnung vor dem Senden der Anfrage die geschätzten Größen an. Die Anfrage wird dennoch gesendet; die API kann sie ablehnen oder den Prompt kürzen.

![Die Warnung vor einem Prompt, der möglicherweise nicht passt](../images/context-warning.png)

<a id="reasoning-models"></a>

## Reasoning-Modelle

Reasoning-Modelle denken nach, bevor sie antworten. Ist **Reasoning aktivieren** angehakt, sendet Marginalia den gewählten **Reasoning-Aufwand** (*Keines*, *Niedrig*, *Mittel* oder *Hoch*) als `reasoning_effort` an die API. Mehr Aufwand bedeutet meist besser geplanten Text, aber langsamere und teurere Antworten. Nicht jede API unterstützt den Parameter; schlagen Anfragen mit aktiviertem Reasoning fehl, schalten Sie es ab.

Liefert das Modell seine Überlegungen zurück (als `reasoning_content` oder `reasoning`, je nach Server), werden sie über dem generierten Teil unter **Reasoning anzeigen** angezeigt. Das Reasoning gehört nicht zur Geschichte und wird nicht an das Modell zurückgeschickt, es sei denn, Sie aktivieren **Das Denken zurücksenden** (unten).

<a id="send-the-thinking-back"></a>

### Das Denken zurücksenden

**Das Denken zurücksenden** (unter *Einstellungen je Typ* eines OpenAI-kompatiblen Anbieters) legt fest, ob das Denken des Modells in der nächsten Anfrage als `reasoning_content` seiner früheren Nachricht an es zurückgeht:

| Wert | |
|---|---|
| **Keines** (Standard) | Nie. Richtig für die meisten Modelle und Server sowie für APIs, die ein unbekanntes Feld ablehnen. |
| **Zwischen Tool-Aufrufen** | Nur solange die Tool-Aufrufe einer Antwort weiterlaufen: Das Modell, das ein Tool aufgerufen hat, bekommt sein eigenes Denken mit dem Ergebnis zurück. Eine neue Nachricht von Ihnen beginnt ohne das alte Denken. Das verlangt Google für Gemma 4 (*thoughts must not be removed between the function calls*, aber zwischen den Runden entfernt). |
| **Immer** | Auch bei früheren Antworten mit Denken, sofern der Chat es behält (der Plot-Assistent des Bogenumrisses tut das). Ein Server, dessen Vorlage altes Denken selbst verwirft, wie die von Gemma 4, ignoriert es. |

Es gilt für die Tool-Schleife (den Plot-Assistenten, die Generatoren, die Tools verwenden) und für Agenten: Ein Agent bekommt das Denken seiner früheren Runden zurück, solange die Tool-Aufrufe eines Zuges weiterlaufen (das Denken wird in der Konsole bei der Nachricht gehalten, nicht in der gespeicherten Nachricht). Der Server muss das Feld annehmen: llama.cpp tut das (es liest `reasoning_content`, nicht `reasoning`), auch hinter LiteLLM. Ein Anbieter, der unbekannte Nachrichtenfelder ablehnt, schlägt fehl; lassen Sie ihn dann auf *Keines*.

![Reasoning eines generierten Teils](../images/reasoning-view.png)

Das Reasoning zählt zur Antwort: Geben Sie Reasoning-Modellen eine größere **Maximale Antwort-Tokens**, sonst kann der Text nach einem langen Reasoning abgeschnitten werden.

<a id="token-counting"></a>

## Token-Zählung

Marginalia zählt Tokens, um zu entscheiden, wie viel der Geschichte in den Prompt passt. Wenn möglich, fragt es den Anbieter, und probiert diese Methoden in dieser Reihenfolge:

1. den Token-Zähl-Endpunkt der OpenAI-API (`/v1/responses/input_tokens`),
2. den Endpunkt `/tokenize` von llama.cpp,
3. den Endpunkt `/tokenize` von LiteLLM,
4. den Endpunkt `/v1/messages/count_tokens` von LiteLLM (für Anthropic-Modelle),
5. eine lokale Schätzung mit dem Tokenizer von OpenAI.

Die erste Methode, die funktioniert, wird von da an verwendet. Die lokale Schätzung ist für die meisten Modelle nah dran, aber nicht exakt; ein weiterer Grund, die **Maximale Kontextgröße** etwas unter der echten Grenze zu halten. Anbieter vom Typ *Erweiterung* zählen so, wie ihre Erweiterung es vorsieht; die [Google Gemini](extensions/google-genai.md)- und [Anthropic Claude](extensions/anthropic.md)-Erweiterungen lassen den Anbieter zählen.

## Prompt-Cache

Jede Anfrage, die Marginalia für eine Art von Arbeit stellt (einen neuen Teil, eine Anmerkung, eine Überarbeitung, eine Zusammenfassung ...), trägt den Header `X-Session-ID`, einen Hash, der für denselben Benutzer und dieselbe Art gleich bleibt. Ein Server oder Proxy, der einen Prompt-Cache führt, kann ihn nutzen, um den Cache der vorherigen Anfrage dieser Art zu finden. Ein Server, der ihn nicht kennt, ignoriert ihn. Siehe [Prompt-Cache und Session-ID](prompt-cache.md), mit einem Beispiel für einen auf der Festplatte gehaltenen Slot von llama.cpp.

<a id="shared-providers"></a>

## Geteilte Anbieter

Auf einer Installation mit mehreren Benutzern kann ein Administrator einen Anbieter **teilen**: Aktivieren Sie **Mit allen Benutzern geteilt** im Dialog des Anbieters (das Kontrollkästchen ist nur für Administratoren sichtbar). Jeder Benutzer findet ihn dann in der Liste **Modell** seiner Bücher und in seinem Tab **Inferenzanbieter**, markiert als *Geteilt* **Ja**. Sie können ihn verwenden, aber nicht öffnen, seine Adresse oder seinen API-Schlüssel sehen, ihn ändern oder löschen; nur sein Eigentümer kann das. Bücher, Sicherungen und Wiederherstellungen, die sich auf einen geteilten Anbieter beziehen, funktionieren für alle weiterhin, die ihn sehen können.

![Ein geteilter Anbieter im Tab Inferenzanbieter eines regulären Benutzers](../images/provider-shared-user.png)

Das Teilen eines Anbieters erlaubt den anderen Benutzern, das Kontingent des Eigentümers beim Dienst aufzubrauchen. Um Benutzern zu verwehren, eigene Anbieter hinzuzufügen, sodass sie nur die geteilten nutzen können, siehe [Benutzer und Anbieter](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Grenzen geteilter Anbieter

Ein Administrator kann festlegen, was jeder Benutzer mit einem geteilten Anbieter tun darf. Im Tab **Inferenzanbieter** hat der Administrator, dem ein geteilter Anbieter gehört, in dessen Zeile eine Schaltfläche **Limits** (ein Benutzer mit einem Schloss), die eine Tabelle mit den festgelegten Limits öffnet. **Limit hinzufügen** legt ein neues an: Wähle den Benutzer und einen Limit-Typ.

![Die Limits eines geteilten Anbieters](../images/provider-limits.png)

| Limit-Typ | Bedeutung |
|---|---|
| Unbegrenzt | Kein Limit (dasselbe wie gar keins). |
| Token-Limit | Der Benutzer kann bis zur angegebenen Anzahl an Tokens pro Zeitraum verbrauchen. |
| Nicht verfügbar | Der Benutzer kann den Anbieter nicht nutzen; alles, was eine Anfrage an ihn senden würde, schlägt fehl. |

Ein **Token-Limit** hat einen **Reset** (täglich, wöchentlich ab Montag oder monatlich; Kalenderzeiträume in der Zeitzone des Servers) und drei Grenzen, die jeweils leer gelassen werden können, sodass es keine Grenze gibt: die gesendeten Tokens **gesendet**, die generierten Tokens **generiert** und beide zusammen (**insgesamt**). Was zuerst erreicht wird, stoppt den Benutzer bis zum nächsten Reset. Sind alle drei gesetzt, darf die Summe aus gesendeter und generierter Grenze nicht unter dem Gesamtwert liegen (der Gesamtwert wäre sonst nie erreichbar), andernfalls verweigert der Dialog das Speichern. Zum Beispiel ist ein Gesamtwert von 100.000 mit 100.000 gesendet und 20.000 generiert in Ordnung, ein Gesamtwert von 100.000 mit 20.000 gesendet und 10.000 generiert dagegen nicht.

![Ein Limit hinzufügen](../images/provider-limit-edit.png)

Nichts wird per Timer zurückgesetzt: Die verbrauchten Tokens beginnen bei null, sobald der Benutzer den Anbieter in einem neuen Zeitraum zum ersten Mal nutzt (oder seine Limits ansieht). Die Größe einer Anfrage ist bekannt, bevor sie gesendet wird. Daher wird eine Anfrage abgelehnt, deren Prompt den Benutzer über das Limit **gesendet** oder **insgesamt** bringen würde. (Der Prompt wird mit Marginalias eigener Schätzung gemessen, die vom Zählwert des Dienstes ein wenig abweichen kann.) Die generierten Tokens sind im Voraus nicht bekannt: Sie werden gezählt, wenn die Anfrage endet. Der Benutzer wird also erst gestoppt, wenn das Limit erreicht ist, und die letzte Antwort kann es überschreiten. Benutzer ohne Eintrag in der Tabelle sind nicht begrenzt.

Damit die generierten Tokens nicht darüber hinausgehen, aktiviere **Hartes Limit** (nur verfügbar bei einem Limit für die generierten Tokens). Eine Anfrage reserviert Platz für die größte Antwort, die sie zulässt (die **Maximale Antwortgröße** des Anbieters oder den kleineren Wert des Protokolls), und mit einem harten Limit wird sie abgelehnt, wenn diese Reserve nicht in den verbleibenden Rest des Limits für generierte Tokens passt. Ein hartes Limit, das niedriger ist als die Antwortgröße des Anbieters, lehnt also jede Anfrage ab; die Tabelle und die Info des Benutzers zeigen *hart* neben einem solchen Limit. Das harte Limit betrifft nur die generierten Tokens; das Limit **insgesamt** kann durch die letzte Antwort trotzdem überschritten werden.

Jeder Benutzer sieht die geteilten Anbieter in seinem Tab als schreibgeschützte Zeilen. Die Schaltfläche **Info** (der Kreis mit einem *i*) bei einem geteilten Anbieter zeigt sein eigenes Limit: was er in diesem Zeitraum verbraucht hat, die Limits und wann sie zurückgesetzt werden. Ist ein Limit erreicht, zeigen der Story-Editor, Zusammenfassungen und Erweiterungen eine Fehlermeldung, anstatt die Anfrage zu senden.

![Die Limits, die ein Benutzer sieht, mit dem Verbrauch in diesem Zeitraum](../images/provider-limit-info.png)

<a id="usage"></a>

## Nutzung

Der Tab **Inferenzanbieter** zeigt je Anbieter die **Gesendeten Tokens** (den Prompt, „Prefill“) und die **Generierten Tokens** (die Antwort, einschließlich Reasoning), die du mit ihm verbraucht hast, aufsummiert über alle deine Bücher, Zusammenfassungen und Erweiterungen. Ein geteilter Anbieter hat für jeden Benutzer einen eigenen Zähler, sodass du nur deinen Anteil siehst.

Administratoren erhalten zusätzlich auf dem Tab eine Schaltfläche **Nutzung je Benutzer**, die eine Tabelle öffnet mit dem, was jeder Benutzer von jedem Anbieter verbraucht hat (gesendete Tokens, generierte Tokens, Anfragen und der Zeitpunkt der letzten).

![Nutzung je Benutzer](../images/provider-usage.png)

Die Zahlen sind das, was der Dienst meldet. Ein Dienst, der keine Zahlen meldet (oder eine Anfrage, die du mitten im Vorgang abbrichst), wird mit der lokalen Schätzung gezählt, die unter [Token-Zählung](#token-counting) beschrieben ist, daher sind diese Zahlen ungefähr. Eine Anfrage, die fehlschlägt, bevor irgendein Text ankommt, wird nicht gezählt. Sofern ein Administrator dir nicht ein [Limit](#limits-of-shared-providers) für einen geteilten Anbieter gesetzt hat, dienen die Zähler nur zur Information.

!!!
Marginalia bittet den Dienst, die Token-Zahlen zu melden (`stream_options` mit `include_usage`). Um das für einen Anbieter zu ändern, trage deine eigenen `stream_options` in seine **Zusätzlichen Parameter** ein, zum Beispiel `{"stream_options": {"include_usage": false}}`: Das ersetzt, was Marginalia sendet, und es wird die lokale Schätzung verwendet.
!!!

<a id="using-a-provider-in-a-book"></a>

## Einen Anbieter in einem Buch verwenden

Jedes Buch hat sein Modell im Feld **Modell** seines Tabs **Info**. Um einen Anbieter für alle neuen Bücher zu verwenden, wähle ihn im Tab **Einstellungen** als **Standardmodell** aus. Ein Wechsel des Anbieters eines Buches wirkt sich mit dem nächsten generierten Teil aus; zuvor geschriebene Teile bleiben unverändert.

<a id="troubleshooting"></a>

## Fehlerbehebung

| Problem | Was zu prüfen ist |
|---|---|
| *Failed to download model list* | URL endet auf `/v1`, API-Schlüssel, der Server läuft und ist von Marginalia aus erreichbar. Gib die Modell-ID ein, wenn der Dienst keine Modellliste hat. |
| *The provider did not answer in time.* | Erhöhe das **Zeitlimit (Sekunden)** des Anbieters; Reasoning-Modelle brauchen eventuell Minuten. |
| *The provider is limiting requests.* | Warte, oder erhöhe die **Wiederholungen**. |
| *Book is missing model.* | Dem Buch fehlt ein Anbieter. Wähle einen unter **Info → Modell** aus. |
| *Contextual limit not sufficient.* | Der Prompt ohne jede Geschichte passt nicht in den Kontext abzüglich der Antwort-Tokens. Erhöhe die **Maximale Kontextgröße** (oder die **Maximale Kontext-Tokens** des Protokolls), senke die **Maximalen Antwort-Tokens**, oder kürze die Vorlagen und Lorebook-Einträge. |
| Text bricht mitten im Satz ab | Die **Maximalen Antwort-Tokens** (oder die **Maximalen Antwort-Tokens** des Protokolls) sind zu niedrig, besonders bei Reasoning-Modellen. |
| Fehler der API bei eingeschaltetem Reasoning | Die API unterstützt `reasoning_effort` nicht. Schalte **Reasoning aktivieren** aus. |
