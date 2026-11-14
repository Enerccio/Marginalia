---
label: Bildgenerator
order: 13
---

# Bildgenerator

Erstellt ein Bild für einen Teil der Geschichte. Das Modell schreibt den Bild-Prompt aus dem Teil, du bearbeitest ihn, ein Bild-Backend zeichnet
das Bild, du würfelst neu, bis es passt, und übernimmst es. Es wird zu einem Bild des Teils, so wie ein Bild, das du von Hand mit *Bilder* im
Menü des Teils hinzufügst (es wird in der Geschichte und in den Exporten angezeigt).

Die Erweiterung erzeugt selbst keine Bilder: Sie spricht mit einem **ComfyUI**-Server oder mit einem Server mit der **OpenAI-Images-API**
(die auch von stable-diffusion.cpp, LiteLLM und vielen gehosteten Diensten gesprochen wird).

<a id="adding-a-backend"></a>

## Ein Backend hinzufügen

*Einstellungen → Erweiterungs-Einstellungen → Bildgenerator-Einstellungen* listet die Backends auf. Füge eins hinzu (+), fülle es aus und speichere die Seite *Einstellungen*.

| Einstellung | Was sie bewirkt |
|---|---|
| Name, Typ | Der Name, der im Fenster angezeigt wird. **ComfyUI** oder **OpenAI-Images-API**. |
| Adresse | Die Basisadresse des Servers, zum Beispiel `http://localhost:8188` für ComfyUI. |
| API-Schlüssel | Wird als Bearer-Token gesendet. Er wird verschlüsselt gespeichert und nicht wieder angezeigt; gib einen neuen ein, um ihn zu ersetzen, oder nutze **Schlüssel entfernen**. |
| Modell | Nur OpenAI-Images-API: das `model` der Anfrage. |
| Workflow | Nur ComfyUI: ein in ComfyUI mit **Save (API Format)** gespeicherter Workflow. Schreibe `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` und `{{height}}` dorthin, wo sie hingehören. |
| Der Server läuft auf diesem Rechner oder in meinem Netzwerk | Erlaubt Adressen des lokalen Netzwerks. Lass es aus für einen Server in der Cloud: Dann kann er nicht dazu gebracht werden, deinen eigenen Rechner anzusprechen. |
| Breite, Höhe, Negativer Prompt | Womit das Fenster beginnt (du kannst sie dort für ein einzelnes Bild ändern). |
| Zeitlimit | Wie lange auf ein Bild gewartet wird. |
| Nach Leerlauf freigeben | Siehe [Eine GPU](#one-gpu-for-the-model-and-the-images). |

![Ein Backend in den Einstellungen](../../images/plugin-imagegenerator-settings.png)

**Verbindung testen** prüft die Adresse und den Schlüssel.

Das Panel enthält außerdem den **Stil**, der jedem Prompt vorangestellt wird (die Optik des Buches, zum Beispiel *Aquarell, weiches Licht*), und die
**Anweisung für den Prompt-Schreiber**, also den Text, den das Modell erhält, um aus einem Teil den Prompt zu schreiben. Leer oder unverändert
wird die eingebaute verwendet.

<a id="making-an-image"></a>

## Ein Bild erstellen

![Bild generieren im Menü eines Teils](../../images/plugin-imagegenerator-menu.png)

1. Wähle **Bild generieren** im Menü eines Teils. Das Modell des Fensters (standardmäßig das des Buches; die letzte Wahl wird gemerkt)
   beginnt, den Prompt des Teils zu schreiben; du kannst es mit **Abbrechen** stoppen, den Prompt ändern oder **Prompt mit dem Modell schreiben** drücken,
   um ihn erneut schreiben zu lassen. Bevor du ihn erneut schreiben lässt, kannst du Folgendes ausfüllen:
   - **Lorebook-Einträge senden** – die Einträge des Lorebooks des Buches (und seiner Unter-Lorebooks) gehen wie bei einer Generierung an das Modell,
     damit es weiß, wie die Figuren und Orte aussehen. Ein Eintrag wird gesendet, wenn seine Filter im Teil gefunden werden: in seinem Text, seinen
     Anweisungen, der Szenenbeschreibung, dem Blickwinkel und den anwesenden Figuren oder in deinen *Details*. Dauerhaft aktive Einträge kommen ebenfalls hinzu.
     Es werden nur so viele übernommen, wie der Kontext des Modells erlaubt (ein Hinweis zeigt, wie viele ausgelassen wurden). Die Auswahl wird gemerkt.
   - **Details** – was das Bild zeigen oder wie es aussehen muss (*bei Nacht, Nahaufnahme ihres Gesichts, das rote Kleid*); der Prompt-Schreiber
     richtet sich danach. Es gilt nur für dieses Fenster.
2. Wähle das Backend und drücke **Generieren**. Unter *Größe und negativer Prompt* kannst du die Größe und den negativen Prompt
   für dieses Fenster ändern.
3. Das Bild erscheint. **Neu würfeln** erstellt ein weiteres mit einem neuen Seed, **Verwerfen** entfernt es, **Übernehmen** fügt es dem Teil hinzu
   und schließt das Fenster. **Abbrechen** stoppt ein Bild, das gerade erstellt wird (ComfyUI wird angewiesen, den Auftrag zu unterbrechen).

![Das Fenster mit dem vom Modell geschriebenen Prompt](../../images/plugin-imagegenerator-prompt.png)

![Das Bild mit Übernehmen, Verwerfen und Neu würfeln](../../images/plugin-imagegenerator.png)

Der geöffnete Teil zeigt das neue Bild sofort unter seinem Text an.

<a id="one-gpu-for-the-model-and-the-images"></a>

## Eine GPU für das Modell und die Bilder

Bei einer kleinen GPU lassen sich das Sprachmodell und das Bildmodell nicht gleichzeitig laden. Die Erweiterung verwaltet den Speicher nicht;
jedes Backend kann zwei Listen von Anfragen haben, die du einrichtest:

- **Vor dem ersten Bild** – wird einmal ausgeführt, wenn das Fenster das Backend zu verwenden beginnt. Zum Beispiel eine Anfrage an deinen Proxy, die das Sprachmodell entlädt und das Bildmodell lädt.
- **Nach dem letzten Bild** – wird ausgeführt, wenn du übernimmst, verwirfst oder das Fenster schließt, oder wenn seit dem letzten Bild **Nach Leerlauf freigeben**-Sekunden vergangen sind
  (damit ein vergessenes Fenster die GPU nicht blockiert; 0 schaltet es aus). Bei ComfyUI kann das `POST <address>/free` mit dem
  Body `{"unload_models": true, "free_memory": true}` sein; bei einem Proxy eine Anfrage, die das Sprachmodell wieder lädt.

Mit `llama-server` im Router-Modus hinter LiteLLM kann die *Vor*-Anfrage das Sprachmodell über den Proxy entladen: siehe
[Die GPU für ein anderes Modell freigeben](../prompt-cache.md#freeing-the-gpu-for-another-model).

Beide Zeitpunkte nehmen eine **Liste von Anfragen** entgegen, die nacheinander ausgeführt werden (**Anfrage hinzufügen** fügt eine hinzu, der Papierkorb entfernt eine). Ein Wechsel braucht oft
mehrere: das Sprachmodell entladen, dann den Bildserver starten und ihn nach den Bildern wieder stoppen. Jede Anfrage hat eine Adresse, eine Methode
(standardmäßig `POST`), Header (`Name: value`, einer pro Zeile), einen Body, ein **Zeitlimit** (leer: 60 Sekunden, höchstens 600; gib einem Server, der ein großes Modell lädt, mehr) und **Danach warten** (Sekunden Pause nach der Antwort, für einen Server, der nach seiner Zusage noch beschäftigt ist).
Die Header werden wie der API-Schlüssel verschlüsselt gespeichert (eine Anfrage trägt oft `Authorization: Bearer ...`). Das Backend bleibt belegt, während
du neu würfelst, sodass nur das erste Bild den Wechsel bezahlt. Wenn ein Proxy vor den Modellen sie anhand des Modellnamens wechselt, lass beide Felder leer.

Schlägt eine Anfrage der *Vor*-Liste fehl, werden die übrigen nicht ausgeführt und das Bild bricht ab (die Meldung sagt, welche Anfrage fehlgeschlagen ist: *Anfrage 2
von 3: ...*), damit nichts auf einer GPU geladen wird, die noch voll ist. Die *Nach*-Liste wird **vollständig** ausgeführt, was auch immer mit einer ihrer Anfragen passiert (jeder Server muss gestoppt werden), und der erste Fehler wird als Warnung angezeigt. Ein Backend, das mit der einzelnen Anfrage einer früheren Version gespeichert wurde, zeigt sie als erste Anfrage der Liste an.

**Mehrere Personen auf einer GPU.** Alle, deren Backend dieselbe Serveradresse hat (`http://host:port`), laufen durch ein gemeinsames Tor: Die
*Vor*-Anfrage wird für das erste Fenster ausgeführt, das startet, und die *Nach*-Anfrage, wenn das letzte fertig ist. So entlädt das Schließen eines Fensters
nicht das Modell unter dem Bild eines anderen, und die Bilder entstehen nacheinander (ein zweites Fenster wartet, bis es an der Reihe ist).
Es gelten die Anfragen des Benutzers, der zuerst kommt und zuletzt geht.

Ein Backend kann auch angeben, auf welcher GPU es läuft (**GPU (Name)**). Backends, die dieselbe GPU angeben, warten aufeinander, selbst wenn ihre Adressen verschieden sind; dazu gehören auch die Backends des [Hörbuchs](audiobook.md) (ein Sprachserver und ein Bildserver auf einer Karte): Ein Benutzer, dessen Backend ein anderes Modell braucht, wartet, bis die anderen fertig sind, und führt dann seine eigenen Anfragen aus, statt sich in eine Ladung zu drängen, die nicht seine ist. Lass es leer, dann warten nur Anfragen an dieselbe Adresse aufeinander.

<a id="what-is-not-done"></a>

## Was nicht umgesetzt ist

- Backends gehören zu einzelnen Benutzern, nicht zu einzelnen Büchern.
