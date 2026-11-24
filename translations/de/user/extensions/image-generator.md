---
label: Bildgenerator
order: 13
---

# Bildgenerator

Erstellt ein Bild für einen Teil der Geschichte. Das Modell schreibt den Bild-Prompt aus dem Teil, du bearbeitest ihn, ein Bild-Backend zeichnet
das Bild, du würfelst neu, bis es passt, und übernimmst es. Es wird zu einem Bild des Teils, so wie eines, das du von Hand mit *Bilder* im
Menü des Teils hinzufügst (es wird in der Geschichte und in den Exporten angezeigt).

Die Erweiterung erstellt selbst keine Bilder: Sie spricht mit einem **ComfyUI**-Server oder mit einem Server mit der **OpenAI-Images-API**
(die auch stable-diffusion.cpp, LiteLLM und viele gehostete Dienste sprechen).

<a id="adding-a-backend"></a>

## Ein Backend hinzufügen

*Einstellungen → Erweiterungseinstellungen → Einstellungen des Bildgenerators* listet die Backends auf. Füge eins hinzu (+), fülle es aus und speichere
die Seite *Einstellungen*.

| Einstellung | Was sie bewirkt |
|---|---|
| Name, Typ | Der Name, der im Fenster angezeigt wird. **ComfyUI** oder **OpenAI-Images-API**. |
| Adresse | Die Basisadresse des Servers, zum Beispiel `http://localhost:8188` für ComfyUI. |
| API-Schlüssel | Wird als Bearer-Token an den Bildserver gesendet. Er wird verschlüsselt gespeichert und nicht erneut angezeigt; gib einen neuen ein, um ihn zu ersetzen, oder nutze **Schlüssel entfernen**. |
| Schlüssel der Anfragen um die Bilder herum | Wird als `Authorization: Bearer` mit jeder Anfrage [vor und nach den Bildern](#one-gpu-for-the-model-and-the-images) gesendet, die keinen eigenen `Authorization`-Header hat, zum Beispiel der Schlüssel eines LiteLLM-Proxys. Verschlüsselt gespeichert. Leer: Es wird der API-Schlüssel verwendet. |
| Modell | Nur OpenAI-Images-API: die `model` der Anfrage. |
| Workflow | Nur ComfyUI: ein in ComfyUI mit **Save (API Format)** gespeicherter Workflow. Schreibe `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` und `{{height}}` an die Stellen, an die sie gehören, sowie `{{lora}}` / `{{lora_strength}}` in einen LoRA-Loader, um [das LoRA im Fenster zu wählen](#choosing-a-lora). |
| Der Server läuft auf diesem Rechner oder in meinem Netzwerk | Erlaubt Adressen des lokalen Netzwerks. Lass es für einen Server in der Cloud deaktiviert: Dann kann er nicht dazu gebracht werden, deinen eigenen Rechner anzusprechen. |
| Breite, Höhe | Womit das Fenster beginnt (du kannst sie dort für ein einzelnes Bild ändern). |
| Prompt-Form | Wie das Sprachmodell den Prompt für dieses Backend schreibt, passend zu seinem Bildmodell: **Tags** (kurze, durch Kommas getrennte Ausdrücke, für SDXL, Pony, Illustrious), **Beschreibende Sätze** (ein Absatz, für Flux, Krea, Qwen-Image, Z-Image) oder **Freier Text**, deine eigene Anweisung für den Prompt-Schreiber (sie beginnt mit der gewählten). Ein Backend, das vor dieser Einstellung angelegt wurde, schreibt Tags oder verwendet die damals gesetzte Anweisung als freien Text. Die eingebauten Anweisungen stehen in deiner [Prompt-Sprache](../account.md#prompt-language) und verlangen den Bild-Prompt auf Englisch, das Bildmodelle am besten lesen. |
| Zeitlimit | Wie lange auf ein Bild gewartet wird. |
| GPU-Sperre | Die [GPU-Sperre](../administration/gpus.md) der GPU, auf der der Server läuft, wenn er sie sich mit dem Sprachmodell oder anderen Servern teilt; siehe [Eine GPU](#one-gpu-for-the-model-and-the-images). Wird nur angezeigt, wenn der Administrator GPU-Sperren eingerichtet hat. |

![Ein Backend in den Einstellungen](../../images/plugin-imagegenerator-settings.png)

**Verbindung testen** prüft die Adresse und den Schlüssel.

Das Panel enthält außerdem zwei Texte, die ein Buch ändern kann:

- **Stil** - wird jedem Prompt vorangestellt: das Aussehen der Bilder, zum Beispiel *Aquarell, weiches Licht*. Auch dem Prompt-Schreiber
  wird das mitgeteilt, damit der Prompt keinen anderen Stil beschreibt.
- **Negativer Prompt** - was die Bilder nicht zeigen dürfen. Ein Turbo-Modell, das ohne Führung (CFG 1) läuft, ignoriert ihn. Der negative
  Prompt, den ein Backend zuvor hatte, wird beim ersten Öffnen des Panels hierher übernommen.

Was du im Panel einstellst, ist dein Standard. Ein Buch (sein Reiter *Prompts*) oder ein [Ordner](../books/folders.md) kann eigene unter
*Erweiterungseinstellungen → Bildgenerator* festlegen; ein Buch verwendet seine eigenen, sonst die des nächsten Ordners, sonst deine.

<a id="making-an-image"></a>

## Ein Bild erstellen

![Bild generieren im Menü eines Teils](../../images/plugin-imagegenerator-menu.png)

1. Wähle **Bild generieren** im Menü eines Teils. Noch wird nichts an ein Modell gesendet: Fülle zuerst aus, woran sich der Prompt-Schreiber
   halten soll, und drücke dann **Prompt mit dem Modell schreiben** (oder <kbd>Strg+Eingabe</kbd> in *Details*). Das Modell des Fensters (standardmäßig
   das des Buches; die letzte Wahl wird gemerkt) schreibt den Prompt des Teils; du kannst es mit **Abbrechen** stoppen, den Prompt ändern,
   selbst schreiben oder neu schreiben lassen. Diese gelten nur für das Schreiben des Prompts, nicht für das Bild selbst:
   - **Lorebook-Einträge senden** - die Einträge des Lorebooks des Buches (und seiner Unter-Lorebooks) werden wie bei einer Generierung an das
     Modell gesendet, damit es weiß, wie Figuren und Orte aussehen. Ein Eintrag wird gesendet, wenn seine Filter im Teil gefunden werden: in
     seinem Text, seinen Anweisungen, der Szenenbeschreibung, der Erzählperspektive und den anwesenden Figuren oder in deinen *Details*.
     Einträge, die immer aktiv sind, kommen ebenfalls hinzu. Es passen nur so viele hinein, wie der Kontext des Modells erlaubt (ein Hinweis
     sagt, wie viele weggelassen wurden). Die Auswahl wird gemerkt.
   - **Details** - was das Bild zeigen oder wie es aussehen soll (*nachts, Nahaufnahme ihres Gesichts, das rote Kleid*); der Prompt-Schreiber
     richtet sich danach. Sie gilt nur für dieses Fenster.
2. Wähle das Backend und drücke **Generieren**. Das Bild entsteht aus dem **Prompt** mit dem **Stil** davor und dem **Negativen Prompt**;
   beide beginnen mit denen des Buches (oder deinem Standard), und eine Änderung gilt nur für dieses Fenster (der Stil wird außerdem dem
   Prompt-Schreiber mitgeteilt). Die Größe beginnt mit der des Backends und kann für dieses Fenster geändert werden.
3. Das Bild erscheint. **Neu würfeln** erstellt ein weiteres mit neuem Seed, **Verwerfen** entfernt es, **Übernehmen** fügt es dem Teil hinzu
   und schließt das Fenster. **Abbrechen** stoppt ein Bild, das gerade erstellt wird (ComfyUI wird angewiesen, den Auftrag zu unterbrechen).

![Das Fenster mit dem vom Modell geschriebenen Prompt](../../images/plugin-imagegenerator-prompt.png)

![Das Bild, mit Übernehmen, Verwerfen und Neu würfeln](../../images/plugin-imagegenerator.png)

Der geöffnete Teil zeigt das neue Bild sofort unter seinem Text an.

Das Fenster merkt sich, was du für jeden Teil verwendet hast: Details, Stil, negativer Prompt, Prompt und Größe werden beim Teil gespeichert, sobald
der Prompt geschrieben und wenn du generierst, und beim nächsten Öffnen des Fensters für diesen Teil beginnt es damit. Für einen Teil, für den
du nie ein Bild erstellt hast, gelten der Stil und der negative Prompt des Buches.

<a id="choosing-a-lora"></a>

## Ein LoRA wählen

Ein LoRA ist eine kleine Erweiterung des Bildmodells, meist für einen Stil. Mit ComfyUI kannst du es im Fenster wählen, statt den Workflow für
jedes einzeln neu zu speichern:

1. Schreibe im exportierten Workflow `{{lora}}` als `lora_name` des LoRA-Loaders (*Load LoRA*, `LoraLoaderModelOnly`, oder
   `LoraLoader`) und `{{lora_strength}}` als seine Stärke (`strength_model`; `strength_clip` ebenfalls für `LoraLoader`).
2. Das Fenster zeigt dann **LoRA** mit den LoRA-Dateien des Servers (aus seinem Ordner `models/loras`) und **LoRA-Stärke**. Du kannst auch den Namen
   einer Datei eintippen, die die Liste nicht zeigt.
3. **Keines** (ein leeres LoRA) entfernt die LoRA-Loader aus dem Workflow, sodass das Bild ohne sie entsteht.

Das LoRA und die Stärke, mit denen du generierst, werden für das Backend gemerkt und sind beim nächsten Mal wieder da. Ebenso die Liste: Ein Server,
der sich die GPU teilt, läuft beim Öffnen des Fensters oft nicht (die Anfragen vor dem ersten Bild starten ihn), daher bietet das Fenster die LoRAs
an, die der Server zuletzt aufgelistet hat, und fragt nach dem ersten Bild erneut. Nur wenn er noch nie antworten konnte, sagt ein Hinweis, dass die
LoRAs nicht gelesen werden konnten; du kannst trotzdem den Namen einer Datei eintippen.
<a id="one-gpu-for-the-model-and-the-images"></a>

## Eine GPU für das Modell und die Bilder

Mit einer kleinen GPU lassen sich das Sprachmodell und das Bildmodell nicht gleichzeitig laden. Der Administrator richtet das einmal für jedes Modell und jeden Server auf dieser GPU ein, unter [Admin → GPUs](../administration/gpus.md): eine **GPU-Sperre** für das Sprachmodell und eine für den Bildserver, jeweils mit den Anfragen, die die GPU belegen, und den Anfragen, die sie wieder freigeben. Die Sperre des Bildservers wählst du unter **GPU-Sperre** im Backend aus (die Sperre des Sprachmodells in dessen [KI-Einstellungen](../inference-providers.md)).

Jedes Bild wartet dann auf seinen Zug an der GPU:

- Ist die GPU bereits mit dem Bildserver geladen, startet das Bild sofort; zwischen den Neuerzeugungen wird nichts entladen.
- Belegt die GPU gerade das Sprachmodell (oder einen anderen Server), wartet das Bild, bis das Modell geantwortet hat, die Sperre des Modells die GPU freigibt und die Sperre des Bildservers sie belegt.
- Zwischen den Bildern wird nichts festgehalten: Ob du das Fenster schließt, einklappst oder offen lässt, die GPU bleibt davon unberührt. Die nächste Anfrage an das Sprachmodell gibt die GPU des Bildservers selbstständig frei, und der Bildserver bleibt bis dahin geladen.

Solange ein Bild auf seinen Zug wartet, zeigt das Fenster *Generating the image* an.

**Anfragen vor und nach jedem Bild.** Ein Backend kann außerdem zwei eigene Listen mit Anfragen haben, die innerhalb der Sperre um jedes Bild herum ausgeführt werden: **Before each image** und **After each image**. Das Freigeben der GPU gehört zur GPU-Sperre; nutze diese Listen für alles andere, was der Server um ein Bild herum braucht. Jede Liste wird nacheinander abgearbeitet (**Add a request** fügt eine Anfrage hinzu, der Papierkorb entfernt eine). Jede Anfrage hat eine Adresse, eine Methode (standardmäßig `POST`), Header (`Name: value`, einer pro Zeile), einen Body, ein **timeout** (leer: 60 Sekunden, höchstens 600) und **Wait afterwards** (Sekunden, die nach der Antwort pausiert werden). Die Header werden verschlüsselt gespeichert, genau wie der API-Schlüssel.

Schlägt eine Anfrage der Liste *Before* fehl, werden die restlichen Anfragen nicht mehr ausgeführt und das Bild wird abgebrochen (die Meldung nennt die fehlgeschlagene Anfrage, etwa *Request 2 of 3: ...*). Die Liste *After* wird **vollständig** abgearbeitet, egal was mit einer ihrer Anfragen passiert; ein Fehler wird im Protokoll der Erweiterung festgehalten, das Bild bleibt erhalten. Ein Backend, das in einer früheren Version mit einer einzelnen Anfrage gespeichert wurde, zeigt diese als erste Anfrage der Liste an.

Backends, die vor der Einführung der GPU-Sperren gespeichert wurden, verlieren ihre Einstellungen **GPU (name)** und **Release after idle**. Wähle stattdessen eine GPU-Sperre aus und verschiebe Anfragen, die die GPU freigegeben haben (etwa das Entladen des Sprachmodells, `POST <address>/free` in ComfyUI), in die Schritte der Sperren.

<a id="what-is-not-done"></a>

## Was nicht umgesetzt ist

- Backends gehören zu einem Benutzer, nicht zu einem Buch.
