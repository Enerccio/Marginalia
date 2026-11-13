---
label: Anthropic Claude
order: 25
---

# Anthropic Claude

Fügt Anthropics Claude-Modelle als [Inferenzanbieter](../inference-providers.md) hinzu, und zwar über Anthropics eigene API statt
einer OpenAI-kompatiblen Schnittstelle.

Claude ist auch über Aggregatoren wie OpenRouter mit dem üblichen Anbieter *OpenAI-kompatibel* erreichbar. Diese Erweiterung
spricht dagegen direkt mit der Claude-API (dem Anthropic SDK). Das bietet Dinge, die ein kompatibler Endpunkt nicht bietet: die **Zusammenfassungen
des Denkprozesses des Modells**, die als Reasoning angezeigt werden, den Aufwand von Claude-4.6-Modellen und neueren sowie die genauen Token-Zahlen,
die Anthropic meldet und zählt.

!!!warning Dein Story-Text geht an Anthropic
Jede Anfrage einer KI, die diesen Anbieter nutzt – der Prompt mit der Geschichte, die Lorebook-Einträge und die Zusammenfassungen, die
Antworten des Modells, die Dateien und Bilder, die ein Agent oder ein Befehl sendet – wird an die Server von Anthropic geschickt
(`api.anthropic.com` oder den Proxy, den du einstellst). Die Nutzungsbedingungen und die Datenschutzerklärung von Anthropic für die API
legen fest, was dort damit geschieht (Speicherdauer, Schutzmechanismen, die eine Anfrage ablehnen können). Die anderen Anbieter senden an den
Server, den du gewählt hast, der auch dein eigener sein kann. Verwende diesen Anbieter nicht für Text, den du nicht an Anthropic senden darfst.
Auf einem Server, den andere Nutzer teilen, sendet ein Administrator, der eine KI dieses Anbieters teilt, den Story-Text aller Nutzer dieser KI an Anthropic.
!!!

<a id="installing"></a>

## Installieren

Lade die Erweiterung unter *Admin → Erweiterungen*, wie in [Erweiterungen verwalten](../administration/extensions.md) beschrieben.
Die JAR-Datei ist groß (etwa 36 MB): Sie enthält das SDK von Anthropic und die Bibliotheken, die nur das SDK braucht. Sonst nutzt
Marginalia diese Bibliotheken nirgends.

Sobald sie geladen ist, enthält die Liste **Typ** im Dialog des Inferenzanbieters einen neuen Eintrag, **Erweiterung**, und die Liste
**Anbieter** oben im Reiter *Einstellungen je Typ* bietet **Anthropic Claude** an (sie wird dir ausgewählt, solange sie die einzige ist).
Ohne eine geladene Erweiterung, die KIs bereitstellt, wird **Erweiterung** nicht angeboten.

<a id="adding-a-claude-provider"></a>

## Einen Claude-Anbieter hinzufügen

1. Hol dir einen API-Schlüssel in der [Claude Console](https://platform.claude.com).
2. Klicke im Reiter **Inferenzanbieter** auf **Inferenzanbieter hinzufügen**, gib einen **Name** ein, wähle den Typ **Erweiterung** und
   den Anbieter **Anthropic Claude**.
3. Fülle die *Allgemeinen Einstellungen* (Kontextgröße, Antwortgröße, [Reasoning](#reasoning)) und die Einstellungen des
   Anbieters im Reiter *Einstellungen je Typ* aus, dann klicke auf **Verbindung testen**.

| Feld | |
|---|---|
| **API-Schlüssel** | Der Schlüssel der Claude Console. Er wird verschlüsselt gespeichert und nicht erneut angezeigt; wenn du den Anbieter bearbeitest, klicke auf **Zurücksetzen**, um einen anderen einzugeben. |
| **Modell** | Das zu verwendende Modell, z. B. `claude-opus-5-5` oder `claude-sonnet-5-5`. **Aktualisieren** listet die Modelle auf, die dein Schlüssel nutzen kann; du kannst auch einen Namen eintippen, der nicht in der Liste steht. |
| **URL** | Optional. Leer verwendet die API von Anthropic (`api.anthropic.com`); setze sie, um die Anfragen über einen Proxy zu schicken. |
| **Zeitlimit (Sekunden)** | Wie lange auf die Antwort von Anthropic gewartet wird und auf weiteren Text, sobald es zu schreiben begonnen hat. Der Standardwert ist 300. |
| **Wiederholungen** | Wie oft eine Anfrage wiederholt wird, wenn Anthropic mit einem Ratenlimit (429) oder einem Serverfehler (5xx, auch 529 *overloaded*) antwortet oder nicht erreicht werden kann. Der Standardwert ist 2, 0 schaltet das Wiederholen aus. |

**Verbindung testen** listet die Modelle auf und bittet das gewählte Modell um einige Tokens, mit den Werten im Dialog (bevor du sie speicherst).

![Die Einstellungen je Typ des Anbieters Anthropic Claude](../../images/plugin-anthropic.png)

![Die Liste der Inferenzanbieter mit einem Claude-Anbieter](../../images/plugin-anthropic-list.png)

## Reasoning

Die Reasoning-Einstellungen sind die Allgemeinen Einstellungen des Anbieters, wie bei jedem Anbieter:

- Mit ausgeschaltetem **Reasoning aktivieren** wird nichts über das Denken gesendet. Die Modelle, die standardmäßig denken (Opus 5.5 lässt sich nicht
  davon abbringen), tun es trotzdem, und ihr Denkprozess wird nicht angezeigt; die anderen denken nicht.
- Ist es eingeschaltet, sendet das Modell Zusammenfassungen seines Denkprozesses, und diese werden als Reasoning des Story-Teils angezeigt. Bei Claude 4.6
  und neueren ist der **Reasoning-Aufwand** der Aufwand des Modells (niedrig, mittel, hoch) mit adaptivem Denken: Das Modell entscheidet selbst,
  wie viel es nachdenkt. Bei den früheren Modellen (Claude 4.5 und älter, darunter Haiku 4.5) ist es ein Budget von 1 024, 8 192 oder
  24 576 Denk-Tokens. **Keines** bedeutet dasselbe wie aus.

Denk-Tokens zählen als generierte Tokens und werden aus der Antwortgröße genommen, die du einstellst: Wenn das Modell sie komplett fürs Denken
aufbraucht, kommt der Story-Teil leer zurück, und die Fehlermeldung sagt, dass die Antwortgröße aufgebraucht wurde. Erhöhe **Maximale Antwort-Tokens**
oder senke den Aufwand. Claude braucht bei jeder Anfrage eine Antwortgröße: Wenn weder der Anbieter noch das Protokoll eine setzt, werden 16 000 verwendet.

<a id="what-works"></a>

## Was funktioniert

Alles, was eine Geschichte braucht, funktioniert wie bei einem OpenAI-kompatiblen Anbieter: Streaming, das Abbrechen einer Generierung, Zusammenfassungen,
[Erweiterungen, die das Modell aufrufen](index.md), Funktionen, die das Modell aufrufen kann (der [Namensgenerator](name-generator.md) bietet einige an),
sowie Bilder (JPEG, PNG, GIF, WebP), PDF- und Textdateien, die an eine Nachricht angehängt werden. Die Tokens, die ein Nutzer verbraucht, werden in den Spalten
**Gesendete Tokens** und **Generierte Tokens** gezählt, anhand der Zahlen, die Anthropic meldet (aus dem Cache gelesene und dorthin geschriebene Tokens
zählen als gesendet), und die [Limits](../inference-providers.md#limits-of-shared-providers) eines geteilten Anbieters gelten.

Die Token-Zahlen, die in der Geschichte angezeigt werden (die Größe eines Teils, der Prompt), zählt Anthropic (die Anfrage `count_tokens` des
Modells), einmal für jeden Text: Die Antworten werden gemerkt. Wenn Anthropic nicht gefragt werden kann (kein Schlüssel, kein Netz, ein Fehler),
schätzt Marginalia die Zahl selbst für eine Minute und versucht es dann erneut; ein Text, der gespeichert oder bearbeitet wird, während ein anderes
Speichern läuft, wird ebenfalls geschätzt.

Zwei Dinge unterscheiden sich von anderen Anbietern:

- **Temperatur, Top-p und die Strafen des Protokolls werden nicht gesendet.** Die aktuelle Claude-API lehnt sie ab; das Modell wählt mit seinen eigenen
  Einstellungen. Die Antwortgröße des Protokolls wird verwendet.
- Eine Unterhaltung muss mit einer Nachricht des Nutzers beginnen und enden, und Claude setzt eine Antwort nicht fort. Der Prompt eines Buches tut das immer; eine Anfrage
  einer Erweiterung, die es nicht tut, wird mit einem Fehler „bad request“ abgelehnt.

<a id="when-the-extension-is-not-loaded"></a>

## Wenn die Erweiterung nicht geladen ist

Die Anbieter, die du angelegt hast, bleiben in der Liste, wenn die Erweiterung entladen wird, mit dem Typ *Erweiterung: anthropic*, und
funktionieren wieder, sobald die Erweiterung geladen ist. Solange sie nicht geladen ist, kann ein Buch, das einen solchen Anbieter nutzt, nicht generieren: Die Fehlermeldung
fordert dich auf, die Erweiterung zu laden oder einen anderen Anbieter zu wählen. Der Anbieter kann trotzdem geöffnet und seine Allgemeinen Einstellungen
geändert werden; die Claude-Einstellungen werden nur angezeigt, wenn die Erweiterung geladen ist, und sie bleiben unangetastet.

<a id="not-supported"></a>

## Nicht unterstützt

- **Amazon Bedrock, Google Vertex AI und Microsoft Foundry.** Es werden nur API-Schlüssel der Claude-API unterstützt.
- Prompt-Caching und die Werkzeuge, die auf den Servern von Anthropic laufen (Websuche, Codeausführung).
