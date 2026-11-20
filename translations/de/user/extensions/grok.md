---
label: xAI Grok
order: 26
---

# xAI Grok

Fügt die Grok-Modelle von xAI als [Inferenz-Provider](../inference-providers.md) hinzu, über die eigene API von xAI.

Grok ist auch über den normalen Provider *OpenAI Compatible* und die URL `https://api.x.ai/v1` erreichbar. Diese Erweiterung spricht die API von xAI direkt an. Dadurch wird das Reasoning als solches angezeigt, und du erhältst die genauen Token-Zahlen, die xAI meldet (einschließlich der Reasoning-Tokens), sowie eine Modellliste und einen Verbindungstest, die für diesen Dienst gemacht sind.

!!!warning Dein Story-Text geht an xAI
Jede Anfrage einer KI, die diesen Provider nutzt – der Prompt mit der Story, die Lorebook-Einträge und die Zusammenfassungen, die Antworten des Modells, die Bilder, die ein Agent oder ein Befehl schickt – wird an die Server von xAI gesendet (`api.x.ai`, oder an den Proxy, den du eingestellt hast).
Was dort damit geschieht, bestimmen die Nutzungsbedingungen und die Datenschutzerklärung von xAI für die API. Die anderen Provider senden an den Server, den du gewählt hast, der auch dein eigener sein kann. Verwende diesen Provider nicht für Texte, die du nicht an xAI senden darfst. Auf einem Server, den mehrere Nutzer teilen, schickt ein Administrator, der eine KI dieses Providers teilt, den Story-Text aller, die sie nutzen, an xAI.
!!!

<a id="installing"></a>

## Installation

Lade die Erweiterung unter *Admin → Extensions*, wie in [Erweiterungen verwalten](../administration/extensions.md) beschrieben. Sobald sie geladen ist, enthält die Liste **Type** des Dialogs für Inferenz-Provider den Eintrag **Extension**, und die Liste **Provider** oben im Tab *Per Type Settings* bietet **xAI Grok** an.

<a id="adding-a-grok-provider"></a>

## Einen Grok-Provider hinzufügen

1. Hol dir einen API-Schlüssel in der [xAI-Konsole](https://console.x.ai).
2. Klicke auf dem Tab **Inference Providers** auf **Add Inference Provider**, gib einen **Name** ein und wähle den Typ **Extension** und den Provider **xAI Grok**.
3. Fülle die *General Settings* (Kontextgröße, Antwortgröße, [Reasoning](#reasoning)) und die Einstellungen des Providers im Tab *Per Type Settings* aus. Klicke dann auf **Test Connection**.

| Feld | |
|---|---|
| **Api Key** | Der Schlüssel aus der xAI-Konsole. Er wird verschlüsselt gespeichert und nicht erneut angezeigt. Wenn du den Provider bearbeitest, klicke auf **Reset**, um einen anderen einzugeben. |
| **Model** | Das zu verwendende Modell, z. B. `grok-4` oder `grok-3-mini`. **Refresh** listet die Modelle auf, die dein Schlüssel nutzen kann. Du kannst auch einen Namen eintippen, der nicht in der Liste steht. |
| **URL** | Optional. Leer lässt die API von xAI (`https://api.x.ai/v1`) verwenden. Setze hier einen Wert, um die Anfragen über einen Proxy zu schicken. |
| **Timeout (seconds)** | Wie lange auf eine Antwort von xAI gewartet wird und auf weiteren Text, sobald das Modell zu schreiben begonnen hat. Der Standardwert ist 300. |
| **Retries** | Wie oft eine Anfrage wiederholt wird, wenn xAI mit einem Limit für Anfragen (429) oder einem Serverfehler (5xx) antwortet oder nicht erreichbar ist. Der Standardwert ist 2, 0 schaltet das Wiederholen aus. |
| **Send the reasoning effort** | *Automatic* sendet den Aufwand nur an `grok-3-mini`, *Always* an alle Modelle, *Never* an keines. |

**Test Connection** listet die Modelle auf und fragt das gewählte Modell nach ein paar Tokens, und zwar mit den Werten im Dialog (noch bevor du sie speicherst).

## Reasoning

Die Reasoning-Einstellungen sind die allgemeinen Einstellungen des Providers, wie bei jedem Provider. Ist Reasoning aktiviert, erscheint das Reasoning, das ein Modell zeigt, als Reasoning des Story-Teils. Der Aufwand wird nur an die Modelle gesendet, die ihn annehmen (`grok-3-mini`: *low*, bei medium und high *high*). Die anderen Grok-Modelle denken standardmäßig, wie sie es immer tun, und lehnen den Parameter ab.

<a id="what-works"></a>

## Was funktioniert

Streaming, Stoppen, JSON-Schemas, Funktionen, die das Modell aufrufen kann, sowie Bilder (JPEG, PNG) und Textdateien als Anhänge funktionieren wie beim OpenAI-kompatiblen Provider, ebenso die Grenzen geteilter KIs und die Zählung der Nutzung. Die Nutzung ist das, was xAI meldet. Die Reasoning-Tokens zählen als erzeugte Tokens. Temperatur, Top p und die Strafwerte des [Protokolls](../protocols.md) werden gesendet, wenn sie aktiviert sind. Manche Reasoning-Modelle lehnen die Strafwerte ab. Nicht unterstützt werden die serverseitigen Werkzeuge von xAI (Live-Suche, Codeausführung) sowie die Erzeugung von Bildern und Videos.
