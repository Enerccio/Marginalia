---
label: Google Gemini
order: 14
---

# Google Gemini

Fügt Googles Gemini-Modelle als [Inferenzanbieter](../inference-providers.md) hinzu, und zwar über Googles eigene API statt über eine OpenAI-kompatible.

Google bietet außerdem einen OpenAI-kompatiblen Endpunkt an, der mit dem üblichen Anbieter *OpenAI-kompatibel* funktioniert. Diese Erweiterung spricht die Gemini-API direkt an (das Google Gen AI SDK). Dadurch stehen Funktionen zur Verfügung, die der kompatible Endpunkt nicht bietet: die **Zusammenfassungen des Denkens des Modells**, die als Reasoning angezeigt werden, die Denkstufe von Gemini 3 und das Denkbudget der früheren Modelle, die Sicherheitsfilter sowie die exakten Token-Zahlen, die Google meldet.

!!!warning Dein Story-Text geht an Google
Jede Anfrage einer KI, die diesen Anbieter nutzt – der Prompt mit der Story, die Lorebook-Einträge und die Zusammenfassungen, die Antworten des Modells, die Dateien und Bilder, die ein Agent oder ein Befehl sendet – wird an die Server von Google geschickt (`generativelanguage.googleapis.com` oder der Proxy, den du einstellst). Was dort damit geschieht, bestimmen die Nutzungsbedingungen und die Datenschutzerklärung Googles für die Gemini-API (Aufbewahrung, ob die Daten zur Verbesserung der Modelle verwendet werden; das hängt vom Tarif deines API-Schlüssels ab). Die anderen Anbieter senden an den Server, den du gewählt hast, der auch dein eigener sein kann. Verwende diesen Anbieter nicht für Text, den du nicht an Google senden darfst. Auf einem Server, den andere Nutzer mitbenutzen, sendet eine Administratorin oder ein Administrator, die oder der eine KI mit diesem Anbieter teilt, den Story-Text aller Nutzer dieser KI an Google.
!!!

<a id="installing"></a>

## Installieren

Lade die Erweiterung unter *Admin → Erweiterungen*, wie in [Erweiterungen verwalten](../administration/extensions.md) beschrieben.
Die JAR-Datei ist groß (etwa 14 MB): Sie enthält das SDK von Google und die Bibliotheken, die nur das SDK braucht. Andere Teile von Marginalia verwenden sie nicht.

Sobald die Erweiterung geladen ist, enthält die Liste **Typ** im Dialog des Inferenzanbieters einen neuen Eintrag, **Erweiterung**. Die Liste **Anbieter** oben im Reiter *Einstellungen je Typ* bietet **Google Gemini** an (sie wird für dich gewählt, solange es der einzige ist). Ohne eine geladene Erweiterung, die KIs bereitstellt, wird **Erweiterung** nicht angeboten.

<a id="adding-a-gemini-provider"></a>

## Einen Gemini-Anbieter hinzufügen

1. Hol dir einen API-Schlüssel in [Google AI Studio](https://aistudio.google.com/apikey).
2. Klicke im Reiter **Inferenzanbieter** auf **Inferenzanbieter hinzufügen**, gib einen **Name** ein, wähle den Typ **Erweiterung** und den Anbieter **Google Gemini**.
3. Fülle die *Allgemeinen Einstellungen* (Kontextgröße, Antwortgröße, [Reasoning](#reasoning)) und die Einstellungen des Anbieters im Reiter *Einstellungen je Typ* aus und klicke dann auf **Verbindung testen**.

![Die allgemeinen Einstellungen eines Anbieters vom Typ Erweiterung](../../images/plugin-googlegenai-general.png)

![Die Einstellungen je Typ des Anbieters Google Gemini](../../images/plugin-googlegenai.png)

Der Anbieter steht danach wie jeder andere in der Liste, mit dem Namen des Anbieters in der Spalte *Typ*:

![Die Liste der Inferenzanbieter mit einem Anbieter Google Gemini](../../images/plugin-googlegenai-list.png)

| Feld | |
|---|---|
| **API-Schlüssel** | Der Schlüssel von Google AI Studio. Er wird verschlüsselt gespeichert und nicht erneut angezeigt; wenn du den Anbieter bearbeitest, klicke auf **Zurücksetzen**, um einen anderen einzugeben. |
| **Modell** | Das zu verwendende Modell, z. B. `gemini-2.5-flash` oder `gemini-3-pro-preview`. **Aktualisieren** listet die Modelle auf, die dein Schlüssel zum Schreiben von Text nutzen kann; du kannst auch einen Namen eintippen, der nicht in der Liste steht. |
| **URL** | Optional. Leer verwendet die API von Google (`generativelanguage.googleapis.com`); trage hier einen Wert ein, um die Anfragen über einen Proxy zu schicken. |
| **Zeitlimit (Sekunden)** | Wie lange auf eine Antwort von Google gewartet wird und auf weiteren Text, sobald es zu schreiben begonnen hat. Der Standardwert ist 300. |
| **Wiederholungen** | Wie oft eine Anfrage wiederholt wird, wenn Google mit einem Ratenlimit (429) oder einem Serverfehler (5xx) antwortet oder nicht erreichbar ist. Der Standardwert ist 2, 0 schaltet das Wiederholen aus. |
| **Sicherheitsfilter ausschalten** | Sendet `BLOCK_NONE` für die einstellbaren Sicherheitskategorien (Belästigung, Hassrede, sexuell explizite Inhalte, gefährliche Inhalte). Fiktion stößt oft an die Filter. Google blockiert manche Inhalte trotzdem, egal was gesendet wird. |

**Verbindung testen** listet die Modelle auf und lässt das gewählte Modell einige Tokens erzeugen, mit den Werten im Dialog (noch bevor du sie speicherst).

## Reasoning

Die Reasoning-Einstellungen sind die der allgemeinen Einstellungen des Anbieters, wie bei jedem Anbieter:

- Mit ausgeschaltetem **Reasoning aktivieren** denkt das Modell wie standardmäßig und sein Denken wird nicht angezeigt.
- Mit eingeschaltetem sendet das Modell die Zusammenfassungen seines Denkens, und sie werden als Reasoning des Story-Teils angezeigt. Der **Reasoning-Aufwand** legt fest, wie viel es denkt: Bei Gemini-3-Modellen ist es die Denkstufe (niedrig, mittel, hoch), bei den früheren Modellen ein Budget von 1 024, 8 192 oder 24 576 Denk-Tokens. **Keines** entspricht ausgeschaltet.

Denk-Tokens zählen als generierte Tokens und werden der Antwortgröße entnommen, die du eingestellt hast: Verbraucht das Modell sie komplett für das Denken, kommt der Story-Teil leer zurück, und die Fehlermeldung sagt, dass die Antwortgröße aufgebraucht wurde. Erhöhe **Maximale Antwort-Tokens** oder senke den Aufwand.

<a id="what-works"></a>

## Was funktioniert

Alles, was eine Story braucht, funktioniert wie bei einem OpenAI-kompatiblen Anbieter: Streaming, das Anhalten einer Generierung, Zusammenfassungen, [Erweiterungen, die das Modell aufrufen](index.md), Funktionen, die das Modell aufrufen kann (der [Namensgenerator](name-generator.md) bietet einige an), sowie Bilder und Dateien, die einer Nachricht angehängt sind. Die Tokens, die ein Nutzer verbraucht, werden in den Spalten **Gesendete Tokens** und **Generierte Tokens** aus den Zahlen gezählt, die Google meldet, und die [Limits](../inference-providers.md#limits-of-shared-providers) eines geteilten Anbieters gelten.

Die Token-Zahlen, die in der Story angezeigt werden (die Größe eines Teils, der Prompt), werden von Google gezählt (die Anfrage `countTokens` des Modells), und zwar einmal für jeden Text: Die Ergebnisse werden gespeichert. Wenn Google nicht gefragt werden kann (kein Schlüssel, kein Netz, ein Fehler), schätzt Marginalia die Zahl selbst für eine Minute und versucht dann erneut, Google zu fragen; ein Text, der gespeichert oder bearbeitet wird, während ein anderes Speichern läuft, wird ebenfalls geschätzt. Die Schätzung weicht etwas von dem ab, was Google abrechnet.

<a id="when-the-extension-is-not-loaded"></a>

## Wenn die Erweiterung nicht geladen ist

Die Anbieter, die du angelegt hast, bleiben in der Liste, wenn die Erweiterung entladen wird, mit dem Typ *Erweiterung: google-genai*, und funktionieren wieder, sobald die Erweiterung geladen ist. Solange sie nicht geladen ist, kann ein Buch, das einen solchen Anbieter verwendet, nichts generieren: Die Fehlermeldung fordert dich auf, die Erweiterung zu laden oder einen anderen Anbieter zu wählen. Der Anbieter lässt sich trotzdem öffnen und seine allgemeinen Einstellungen lassen sich ändern; die Gemini-Einstellungen werden nur angezeigt, wenn die Erweiterung geladen ist, und sie werden nicht verändert.

<a id="not-supported"></a>

## Nicht unterstützt

- **Vertex AI** (Google Cloud mit einem Projekt und Zugangsdaten). Unterstützt werden nur API-Schlüssel der Gemini-API.
- Bilderzeugung und andere Ausgabetypen von Gemini: Es wird nur Text geschrieben.
