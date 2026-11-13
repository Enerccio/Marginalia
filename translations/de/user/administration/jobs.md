---
label: Aufgaben
order: 86
---

# Aufgaben

Alles, was die Anwendung von einer **externen KI** verlangt, ist eine *Aufgabe*: eine Generierung, eine Zusammenfassung, eine Annotation, ein Agentenlauf, ein Kapitel, das in Audio umgewandelt wird, ein Bild. Jede Aufgabe wird mit dem Benutzer protokolliert, für den sie lief, mit Start- und Endzeit, Ergebnis und jedem *Aufruf* an ein Modell oder an ein Sprach- oder Bild-Backend, wiederum mit eigenem Start und Ende. Die Registerkarte **Aufgaben** der Admin-Seite zeigt das Protokoll; die Seite `/jobs` zeigt, was gerade läuft.

Es werden nur Metadaten gespeichert: wer, wann, wie lange, welche KI oder welcher Server (der Name der KI bzw. der Host des Sprach- oder Bildservers; niemals der Pfad oder die Abfrage der Adresse), das Modell, das Ergebnis und die kurze Fehlermeldung bei einem Fehlschlag. **Kein Prompt, keine Antwort und kein Text** wird jemals gespeichert.

<a id="what-is-one-job"></a>

## Was eine Aufgabe ist

- Eine **Anfrage, die nichts zusammenfasst** (eine Generierung, eine Annotation, eine Neufassung, eine Identitätsübernahme, eine Zusammenfassung eines Teils, eine Frage an den Hilfe-Assistenten, eine Anfrage eines Plugins wie die Übersetzung eines Teils oder eines Lore-Eintrags) ist eine eigene Aufgabe mit einem Aufruf. Ihre Art ist die Art der Arbeit (`generation`, `annotate`, `summary` ...).
- Eine **Neuschreibung der Zusammenfassungen** ist eine Aufgabe (`summary`) mit einem Aufruf je geschriebener Zusammenfassung.
- Eine **Arbeitssitzung eines Agentenlaufs** ist eine Aufgabe (`agent`) mit einem Aufruf je Runde des Agenten; ein Lauf, der pausiert und fortgesetzt wird, hat für jede Sitzung eine Aufgabe, und die Anfragen eines Unteragenten sind eigene Aufgaben.
- Eine **Umwandlung in Audio** ist je Stapel von Teilen eine Aufgabe (`audiobook`), mit den Anfragen des Kennzeichnungsmodells und jedem gesprochenen Stück als Aufrufen.
- Ein Bild, das der **Bildgenerator** erstellt, ist eine eigene Aufgabe (`image-generator`) mit einem Bildaufruf.

<a id="what-runs-now-jobs"></a>

## Was jetzt läuft: `/jobs`

![Die Seite /jobs mit einer laufenden Generierung](../../images/jobs-live.png)

Öffne `/jobs` unter der Adresse deines Marginalia (zum Beispiel `http://localhost:8080/jobs`; der Link **Die Live-Ansicht öffnen** der Registerkarte erledigt das ebenso). Die Seite fragt wie jede andere nach der Anmeldung und zeigt Nicht-Administratoren nichts. Die Liste aktualisiert sich alle zwei Sekunden und zeigt für jede laufende Aufgabe den Benutzer, die Art, wie lange sie läuft, wie viele ihrer Aufrufe erledigt sind und was gerade in Arbeit ist (die KI oder der Server und wie lange der Aufruf wartet). Das ist der schnelle Blick, wenn der Server ausgelastet scheint: Eine lange Liste von Aufrufen, die auf ein einziges Modell warten, erklärt, warum alles langsam ist.

Unter den Aufgaben listet **Läufe auf den Büchern** auf, was gerade an einem Buch arbeitet, und verhindert, dass Teile davon unter ihm gelöscht oder verändert werden (siehe das [Panel der Läufe](../books/story-editor.md#the-panel-of-the-runs), das jedem Benutzer für seine eigenen Bücher dasselbe zeigt): den Benutzer, das Buch, was es tut, wo es steht (so wie das Panel es sagt: *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits for you*, und bei einer Generierung ihre Stufe, etwa *INFERENCE*, während sie ihren Teil schreibt; das Fenster eines geöffneten Buches zeigt dort nichts), seit wann, und wie viele Objekte es hält (das Buch, Teile). Das Fenster eines geöffneten Buches ist ebenfalls ein Lauf. **Stopp** beendet den Lauf so, wie es sein Benutzer täte. **Beenden** beendet nach einer Rückfrage einen Lauf, ohne seine Arbeit anzuhalten: Was er hält, wird sofort freigegeben. Verwende es nur für einen Lauf, der nie endet (seine Arbeit ist festgefahren und hält das Buch bis zu einem Neustart); seine Arbeit kann beim Aufwachen trotzdem noch schreiben.

<a id="the-log-the-jobs-tab"></a>

## Das Protokoll: die Registerkarte Aufgaben

![Die Registerkarte Aufgaben der Admin-Seite](../../images/admin-jobs.png)

| | |
|---|---|
| **Benutzer, Art, Status** | Filter. Die *Art* ist, was die Aufgabe ist (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator` ...); eine Anfrage, die vom Code gestellt wurde und ihre Arbeit nicht benannt hat, ist `untagged`. |
| **Älter als (Tage)** | Nur die Aufgaben, die vor so vielen Tagen oder früher begonnen haben. |
| **Aktualisieren** | Liest das Protokoll erneut. |
| **Diese Jobs löschen** | Löscht die Aufgaben, die der Filter zeigt, samt ihren Aufrufen endgültig, nach einer Rückfrage, die sagt, wie viele es sind. Ist kein Filter gesetzt, wird das gesamte Protokoll gelöscht. |
| **Die Live-Ansicht öffnen** | Öffnet `/jobs` in einem neuen Tab. |

Klicke auf eine Zeile, um die Aufrufe der Aufgabe zu sehen: die Art (Sprachmodell, Audio, Bild), die KI oder den Server, das Modell, Start, Ende, Dauer, Status und den Fehler eines fehlgeschlagenen Aufrufs. Eine Aufgabe, die als fehlgeschlagen angezeigt wird, hat einen Aufruf gemacht, der fehlschlug, oder wurde vom Code, der sie ausführte, als fehlgeschlagen beendet; ein abgebrochener Aufruf ist einer, den du gestoppt hast (oder einer, der noch lief, als seine Aufgabe endete).

![Die Aufrufe einer Audiobuch-Umwandlung](../../images/admin-jobs-calls.png)

Eine fehlgeschlagene Aufgabe zeigt, welcher Aufruf fehlschlug und warum:

![Eine fehlgeschlagene Umwandlung mit dem Fehler ihres Sprachaufrufs](../../images/admin-jobs-failed.png)

**Nichts wird von selbst gelöscht.** Das Protokoll wächst mit jeder Anfrage, deshalb räume es von Zeit zu Zeit auf: Stelle *Älter als (Tage)* ein (zum Beispiel 30), prüfe, was aufgelistet wird, und lösche. Die [Bereinigung](cleanup.md) entfernt die Aufgaben eines Benutzers zusammen mit dem Benutzer.

![Die Aufgaben älter als 30 Tage und die Rückfrage, bevor sie gelöscht werden](../../images/admin-jobs-old.png)

![Die Rückfrage, bevor die Aufgaben gelöscht werden](../../images/admin-jobs-delete.png)

Eine Aufgabe wird beim Ende ins Protokoll geschrieben; eine Aufgabe, die noch läuft, steht nur auf `/jobs`. Wird die Anwendung beendet, gehen die laufenden Aufgaben verloren (sie wurden nie abgeschlossen). Eine Aufgabe, die niemand beendet hat und die eine halbe Stunde lang keinen Aufruf macht, wird als abgeschlossen beendet.
