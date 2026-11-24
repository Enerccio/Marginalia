---
label: xAI Grok
order: 26
---

# xAI Grok

Fügt die Grok-Modelle von xAI als [Inferenzanbieter](../inference-providers.md) hinzu, über die eigene API von xAI.

Grok ist auch über den normalen Anbieter *OpenAI Compatible* und die URL `https://api.x.ai/v1` erreichbar. Diese Erweiterung spricht die xAI-API direkt an. Dadurch wird das Reasoning als solches angezeigt, es werden die genauen Token-Zahlen übernommen, die xAI meldet (einschließlich der Reasoning-Token), und es gibt eine Modellliste und einen Verbindungstest, die auf den Dienst zugeschnitten sind.

!!!warning Ihr Story-Text geht an xAI
Jede Anfrage einer KI, die diesen Anbieter nutzt – der Prompt mit der Geschichte, die Lorebook-Einträge und die Zusammenfassungen, die Antworten des Modells, die Bilder, die ein Agent oder ein Befehl sendet – wird an die Server von xAI geschickt (`api.x.ai` oder den Proxy, den Sie eingestellt haben). Was dort damit geschieht, regeln die Nutzungsbedingungen und die Datenschutzerklärung von xAI für die API. Die anderen Anbieter senden an den Server, den Sie gewählt haben, der auch Ihr eigener sein kann. Verwenden Sie diesen Anbieter nicht für Text, den Sie nicht an xAI senden dürfen. Auf einem Server, den mehrere Nutzer teilen, schickt ein Administrator, der eine KI dieses Anbieters teilt, den Story-Text aller Nutzer, die sie verwenden, an xAI.
!!!

## Installing

Laden Sie die Erweiterung unter *Admin → Extensions*, wie in [Erweiterungen verwalten](../administration/extensions.md) beschrieben. Sobald sie geladen ist, enthält die Liste **Type** im Dialog des Inferenzanbieters den Eintrag **Extension**, und die Liste **Provider** oben im Tab *Per Type Settings* bietet **xAI Grok** an.

## Adding a Grok provider

1. Holen Sie sich einen API-Schlüssel in der [xAI Console](https://console.x.ai).
2. Klicken Sie auf dem Tab **Inference Providers** auf **Add Inference Provider**, geben Sie einen **Name** ein, wählen Sie den Typ **Extension** und den Anbieter **xAI Grok**.
3. Füllen Sie die *General Settings* (Kontextgröße, Antwortgröße, [Reasoning](#reasoning)) und die Einstellungen des Anbieters auf dem Tab *Per Type Settings* aus und klicken Sie dann auf **Test Connection**.

![Ein Grok-Anbieter](../../images/plugin-grok.png)

![Der Grok-Anbieter in der Liste](../../images/plugin-grok-list.png)

| Feld | |
|---|---|
| **Api Key** | Der Schlüssel aus der xAI Console. Er wird verschlüsselt gespeichert und nicht erneut angezeigt; wenn Sie den Anbieter bearbeiten, klicken Sie auf **Reset**, um einen anderen einzugeben. |
| **Model** | Das zu verwendende Modell, z. B. `grok-4` oder `grok-3-mini`. **Refresh** listet die Modelle auf, die Ihr Schlüssel verwenden kann; Sie können auch einen Namen eintippen, der nicht in der Liste steht. |
| **URL** | Optional. Leer lassen, um die API von xAI (`https://api.x.ai/v1`) zu verwenden; setzen Sie sie, um die Anfragen über einen Proxy zu leiten. |
| **Timeout (seconds)** | Wie lange auf die Antwort von xAI gewartet wird und auf weiteren Text, sobald das Modell mit dem Schreiben begonnen hat. Der Standardwert ist 300. |
| **Retries** | Wie oft eine Anfrage wiederholt wird, wenn xAI mit einem Ratenlimit (429) oder einem Serverfehler (5xx) antwortet oder nicht erreicht werden kann. Der Standardwert ist 2, 0 schaltet das Wiederholen aus. |
| **Send the reasoning effort** | *Automatic* sendet den Aufwand nur an `grok-3-mini`, *Always* an jedes Modell, *Never* an keines. |

**Test Connection** listet die Modelle auf und fragt das gewählte Modell nach einigen Token, mit den Werten im Dialog (bevor Sie sie speichern).

## Reasoning

Die Reasoning-Einstellungen sind die allgemeinen Einstellungen des Anbieters, wie bei jedem anderen Anbieter. Ist Reasoning eingeschaltet, erscheint das Reasoning, das ein Modell zeigt, als Reasoning des Story-Teils. Der Aufwand wird nur an die Modelle gesendet, die ihn annehmen (`grok-3-mini`: *low*, bzw. *high* für medium und high). Die anderen Grok-Modelle denken standardmäßig wie gewohnt und lehnen den Parameter ab.

## What works

Streaming, Stoppen, JSON-Schemas, Funktionen, die das Modell aufrufen kann, Bilder (JPEG, PNG) und Textdateien als Anhänge, die Grenzen geteilter KIs und die Nutzungszählung funktionieren wie beim OpenAI-kompatiblen Anbieter. Die Nutzung ist das, was xAI meldet; die Reasoning-Token zählen als generierte Token. Temperatur, Top p und die Strafen des [Protokolls](../protocols.md) werden gesendet, wenn sie aktiviert sind; manche Reasoning-Modelle lehnen die Strafen ab. Nicht unterstützt werden die serverseitigen Werkzeuge von xAI (Live-Suche, Codeausführung) sowie Bild- oder Videoerzeugung.
