---
label: Bildgenerator
order: 13
---

# Bildgenerator

Erstellt ein Bild für einen Teil der Geschichte. Das Modell schreibt den Bild-Prompt aus dem Teil, du bearbeitest ihn, ein Bild-Backend zeichnet
das Bild, du würfelst neu, bis es passt, und übernimmst es – dann wird es zu einem Bild des Teils, so wie eines, das du von Hand mit *Bilder* im
Menü des Teils hinzufügst (es wird in der Geschichte und in den Exporten angezeigt).

Die Erweiterung erstellt selbst keine Bilder: Sie spricht mit einem **ComfyUI**-Server oder mit einem Server mit der **OpenAI-Images-API**
(auch von stable-diffusion.cpp, LiteLLM und vielen gehosteten Diensten gesprochen).

<a id="adding-a-backend"></a>

## Ein Backend hinzufügen

*Einstellungen → Erweiterungseinstellungen → Bildgenerator-Einstellungen* listet die Backends auf. Füge eines hinzu (+), fülle es aus und speichere die Seite *Einstellungen*.

| Einstellung | Was sie bewirkt |
|---|---|
| Name, Typ | Der Name, der im Fenster angezeigt wird. **ComfyUI** oder **OpenAI-Images-API**. |
| Adresse | Die Basisadresse des Servers, zum Beispiel `http://localhost:8188` für ComfyUI. |
| API-Schlüssel | Wird als Bearer-Token gesendet. Er wird verschlüsselt gespeichert und nicht wieder angezeigt; gib einen neuen ein, um ihn zu ersetzen, oder nutze **Schlüssel entfernen**. |
| Modell | Nur OpenAI-Images-API: das `model` der Anfrage. |
| Workflow | Nur ComfyUI: ein in ComfyUI mit **Save (API Format)** gespeicherter Workflow. Schreibe `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` und `{{height}}` dorthin, wo sie hingehören. |
| Der Server läuft auf diesem Rechner oder in meinem Netzwerk | Erlaubt Adressen des lokalen Netzwerks. Lass es aus für einen Server in der Cloud: Dann kann er nicht dazu gebracht werden, deinen eigenen Rechner aufzurufen. |
| Breite, Höhe, Negativ-Prompt | Womit das Fenster beginnt (du kannst sie dort für ein einzelnes Bild ändern). |
| Timeout | Wie lange auf ein Bild gewartet wird. |
| Nach Leerlauf freigeben | Siehe [Eine GPU](#one-gpu-for-the-model-and-the-images). |

![Ein Backend in den Einstellungen](../../images/plugin-imagegenerator-settings.png)

**Verbindung testen** prüft Adresse und Schlüssel.

Das Panel enthält außerdem den **Stil**, der jedem Prompt vorangestellt wird (der Look des Buchs, zum Beispiel *Aquarell, weiches Licht*), und die
**Anweisung für den Prompt-Schreiber**, also den Text, mit dem das Modell den Prompt aus einem Teil schreibt. Leer oder unverändert wird die
eingebaute Anweisung verwendet.

<a id="making-an-image"></a>

## Ein Bild erstellen

![Bild generieren im Menü eines Teils](../../images/plugin-imagegenerator-menu.png)

1. Wähle **Bild generieren** im Menü eines Teils. Das Modell des Fensters (standardmäßig das des Buchs; die letzte Wahl wird gemerkt)
   beginnt, den Prompt des Teils zu schreiben; du kannst es mit **Abbrechen** stoppen, den Prompt ändern oder **Prompt mit dem Modell schreiben** drücken, um ihn neu zu schreiben. Bevor du ihn neu schreibst, kannst du Folgendes ausfüllen:
   - **Lorebook-Einträge senden** – die Einträge des Lorebooks des Buchs (und seiner Unter-Lorebooks) gehen wie bei einer Generierung an das Modell, damit es weiß, wie die Figuren und Orte aussehen. Ein Eintrag wird gesendet, wenn seine Filter im Teil gefunden werden: sein Text, seine Anweisungen, die Szenenbeschreibung, die Erzählperspektive und die anwesenden Figuren oder in deinen *Details*. Immer aktive Einträge kommen ebenfalls hinzu. Es passen nur so viele hinein, wie der Kontext des Modells erlaubt (ein Hinweis sagt, wie viele weggelassen wurden). Die Auswahl wird gemerkt.
   - **Details** – was das Bild zeigen muss oder wie es aussehen soll (*bei Nacht, Nahaufnahme ihres Gesichts, das rote Kleid*); der Prompt-Schreiber folgt dem. Es gilt nur für dieses Fenster.
2. Wähle das Backend und drücke **Generieren**. Unter *Größe und Negativ-Prompt* kannst du Größe und Negativ-Prompt für dieses Fenster ändern.
3. Das Bild erscheint. **Neu würfeln** erstellt ein weiteres mit neuem Seed, **Verwerfen** entfernt es, **Übernehmen** fügt es dem Teil hinzu und schließt das Fenster. **Abbrechen** stoppt ein gerade entstehendes Bild (ComfyUI wird angewiesen, den Auftrag zu unterbrechen).

![Das Fenster mit dem vom Modell geschriebenen Prompt](../../images/plugin-imagegenerator-prompt.png)

![Das Bild, mit Übernehmen, Verwerfen und Neu würfeln](../../images/plugin-imagegenerator.png)

Der geöffnete Teil zeigt das neue Bild sofort unter seinem Text an.

<a id="one-gpu-for-the-model-and-the-images"></a>

## Eine GPU für das Modell und die Bilder

Bei einer kleinen GPU lassen sich das Sprachmodell und das Bildmodell nicht gleichzeitig laden. Die Erweiterung verwaltet den Speicher nicht;
jedes Backend kann zwei Listen von Anfragen haben, die du einrichtest:

- **Vor dem ersten Bild** – wird einmal ausgeführt, wenn das Fenster beginnt, das Backend zu nutzen. Zum Beispiel eine Anfrage an deinen Proxy, die das Sprachmodell entlädt und das Bildmodell lädt.
- **Nach dem letzten Bild** – wird ausgeführt, wenn du übernimmst, verwirfst oder das Fenster schließt, oder wenn seit dem letzten Bild **Nach Leerlauf freigeben**-Sekunden vergangen sind (damit ein vergessenes Fenster die GPU nicht blockiert; 0 schaltet es ab). Bei ComfyUI kann dies `POST <Adresse>/free` mit dem Body `{"unload_models": true, "free_memory": true}` sein; bei einem Proxy eine Anfrage, die das Sprachmodell wieder lädt.

Mit `llama-server` im Router-Modus hinter LiteLLM kann die Anfrage *vorher* das Sprachmodell über den Proxy entladen: siehe
[GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model).

Beide Zeitpunkte nehmen eine **Liste von Anfragen**, die nacheinander ausgeführt werden (**Anfrage hinzufügen** fügt eine hinzu, der Papierkorb entfernt eine). Ein Wechsel braucht oft mehrere: das Sprachmodell entladen, dann den Bildserver starten, und nach den Bildern wieder stoppen. Jede Anfrage hat eine Adresse, eine Methode
(standardmäßig `POST`), Header (`Name: Wert`, eine pro Zeile), einen Body, ein **Timeout** (leer: 60 Sekunden, höchstens 600; gib einem Server, der ein großes Modell lädt, mehr) und **Danach warten** (Sekunden Pause nach der Antwort, für einen Server, der nach seiner Zusage noch beschäftigt ist). Die Header werden wie der API-Schlüssel verschlüsselt gespeichert (eine Anfrage trägt oft `Authorization: Bearer ...`). Das Backend bleibt belegt, während du neu würfelst, sodass nur das erste Bild den Wechsel bezahlt. Wenn ein Proxy vor den Modellen sie anhand des Modellnamens wechselt, lass beide Felder leer.

Schlägt eine Anfrage der Liste *vorher* fehl, werden die übrigen nicht ausgeführt und das Bild bricht ab (die Meldung sagt, welche Anfrage fehlschlug: *Anfrage 2 von 3: ...*), damit nichts auf einer GPU geladen wird, die noch voll ist. Die Liste *nachher* wird **vollständig** ausgeführt, was auch mit einer ihrer Anfragen geschieht (jeder Server muss gestoppt werden), und der erste Fehler wird als Warnung angezeigt. Ein Backend, das mit der einzelnen Anfrage einer früheren Version gespeichert wurde, zeigt sie als erste Anfrage der Liste an.

**Mehrere Personen auf einer GPU.** Alle, deren Backend dieselbe Serveradresse (`http://host:port`) hat, laufen durch ein gemeinsames Tor: Die Anfrage *vorher* wird für das erste Fenster ausgeführt, das startet, und die Anfrage *nachher*, wenn das letzte fertig ist. So entlädt das Schließen eines Fensters das Modell nicht unter dem Bild eines anderen, und die Bilder entstehen nacheinander (ein zweites Fenster wartet, bis es an der Reihe ist). Es gelten die Anfragen des Nutzers, der zuerst kommt und zuletzt geht.

Ein Backend kann außerdem die GPU benennen, auf der es läuft (**GPU (Name)**). Backends, die dieselbe GPU benennen, warten aufeinander, selbst wenn ihre Adressen verschieden sind; dazu gehören auch die Backends des [Hörbuchs](audiobook.md) (ein Sprachserver und ein Bildserver auf einer Karte): Ein Nutzer, dessen Backend ein anderes Modell braucht, wartet, bis die anderen fertig sind, und führt dann seine eigenen Anfragen aus, statt sich in eine Ladung zu drängen, die nicht seine ist. Lässt du das Feld leer, warten nur Anfragen an dieselbe Adresse aufeinander.

<a id="what-is-not-done"></a>

## Was nicht umgesetzt ist

- Backends gehören zum Nutzer, nicht zum Buch.
