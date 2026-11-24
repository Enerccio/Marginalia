---
label: Jobs
order: 86
---

# Jobs

Alles, was die Anwendung von einer **externen KI** verlangt, ist ein *Job*: eine Generierung, eine Zusammenfassung, eine Annotation, ein Agentenlauf, ein Kapitel, das in Audio umgewandelt wird, oder ein Bild. Jeder Job wird protokolliert, mit dem Benutzer, für den er lief, Start- und Endzeit, der Art seines Endes und jedem *Aufruf* an ein Modell oder an ein Sprach- oder Bild-Backend, ebenfalls mit eigenem Start und Ende. Der Tab **Jobs** der Admin-Seite zeigt das Protokoll; die Seite `/jobs` zeigt, was gerade läuft.

Gespeichert werden nur Metadaten: wer, wann, wie lange, welche KI oder welcher Server (der Name der KI bzw. der Host des Sprach- oder Bildservers, nie der Pfad oder die Abfrage der Adresse), das Modell, wie der Job endete und die kurze Fehlermeldung eines Fehlschlags. **Kein Prompt, keine Antwort, kein Text** wird jemals gespeichert.

<a id="what-is-one-job"></a>

## Was ein Job ist

- Eine **Anfrage, die nichts gruppiert** (eine Generierung, eine Annotation, eine Neufassung, eine Impersonation, eine Zusammenfassung eines Teils, eine Frage an den Hilfe-Assistenten, eine Anfrage eines Plugins wie eine Übersetzung eines Teils oder eines Lore-Eintrags) ist ein eigener Job mit einem einzigen Aufruf. Seine Art ist die Art der Arbeit (`generation`, `annotate`, `summary` ...).
- Eine **Neuschreibung der Zusammenfassungen** ist ein Job (`summary`) mit einem Aufruf pro geschriebener Zusammenfassung.
- Eine **Arbeitssitzung eines Agentenlaufs** ist ein Job (`agent`) mit einem Aufruf pro Runde des Agenten. Ein Lauf, der pausiert und fortgesetzt wird, hat für jede Sitzung einen Job, und die Anfragen eines Unteragenten sind eigene Jobs.
- Eine **Konvertierung in Audio** ist pro Stapel von Teilen ein Job (`audiobook`), wobei die Anfragen des Beschriftungsmodells und jedes gesprochene Stück als Aufrufe gelten.
- Ein Bild, das der **Image Generator** erzeugt, ist ein eigener Job (`image-generator`) mit einem Bildaufruf.

<a id="what-runs-now-jobs"></a>

## Was jetzt läuft: `/jobs`

![Die Seite /jobs mit einer laufenden Generierung](../../images/jobs-live.png)

Öffne `/jobs` unter der Adresse deines Marginalia (zum Beispiel `http://localhost:8080/jobs`; der Link **Open the live view** des Tabs öffnet sie). Sie verlangt die Anmeldung wie jede andere Seite und zeigt Nicht-Administratoren nichts. Die Liste aktualisiert sich alle zwei Sekunden und zeigt für jeden laufenden Job den Benutzer, die Art, wie lange er läuft, wie viele seiner Aufrufe erledigt sind und was gerade unterwegs ist (die KI oder der Server und wie lange der Aufruf wartet), dazu das Buch des Laufs, für den der Job arbeitet, mit seinem **Stop**. Sie ist der schnelle Blick, wenn der Server ausgelastet scheint: Eine lange Liste von Aufrufen, die auf ein einziges Modell warten, erklärt, warum alles langsam ist.

Unter den Jobs listet **Runs on the books** auf, was gerade an einem Buch arbeitet, und verhindert, dass Teile davon unter ihm gelöscht oder verändert werden (siehe das [Panel der Läufe](../books/story-editor.md#the-panel-of-the-runs), das jedem Benutzer für seine eigenen Bücher dasselbe zeigt). Angezeigt werden der Benutzer, das Buch, was der Lauf tut, wo er steht (so, wie es das Panel sagt: *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits for you*, und bei einer Generierung ihre Stufe, etwa *INFERENCE*, während sie ihren Teil schreibt; das Fenster eines geöffneten Buchs zeigt dort nichts), seit wann er läuft und wie viele Objekte er hält (das Buch, Teile). Das Fenster eines geöffneten Buchs ist ebenfalls ein Lauf. **Stop** beendet den Lauf so, wie sein Benutzer es täte. **End** beendet einen Lauf nach einer Rückfrage, ohne seine Arbeit zu stoppen: Was er hält, wird sofort freigegeben. Benutze es nur bei einem Lauf, der nie endet; seine Arbeit kann trotzdem noch schreiben, wenn sie aufwacht. Die meisten solchen Läufe enden von selbst: Ein Lauf, an dem eine Stunde lang nichts gearbeitet hat und der weder auf dich noch auf sein Modell wartet, wird vom Server beendet, ebenso das Fenster einer Seite, die geschlossen wurde.

<a id="the-log-the-jobs-tab"></a>

## Das Protokoll: der Tab Jobs

![Der Tab Jobs der Admin-Seite](../../images/admin-jobs.png)

| | |
|---|---|
| **User, Kind, Status** | Filter. Die *Art* ist, was der Job ist (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator` ...); eine Anfrage, die von Code gestellt wurde, der seine Arbeit nicht benannt hat, ist `untagged`. |
| **Older than (days)** | Nur die Jobs, die so viele Tage oder früher begonnen haben. |
| **Refresh** | Liest das Protokoll neu. |
| **Delete these jobs** | Löscht die Jobs, die der Filter zeigt, und ihre Aufrufe endgültig, nach einer Rückfrage, die die Anzahl nennt. Ohne gesetzten Filter wird das gesamte Protokoll gelöscht. |
| **Open the live view** | Öffnet `/jobs` in einem neuen Tab. |

Klicke auf eine Zeile, um die Aufrufe des Jobs zu sehen: die Art (Sprachmodell, Audio, Bild), die KI oder den Server, das Modell, Start, Ende, Dauer, Status und den Fehler eines fehlgeschlagenen Aufrufs. Ein Job, der als fehlgeschlagen angezeigt wird, hat einen Aufruf gemacht, der fehlschlug, oder er wurde vom Code, der ihn ausführte, als fehlgeschlagen beendet. Ein abgebrochener Aufruf ist einer, den du gestoppt hast (oder der noch lief, als sein Job endete).

![Die Aufrufe einer Hörbuch-Konvertierung](../../images/admin-jobs-calls.png)

Ein fehlgeschlagener Job zeigt, welcher Aufruf fehlschlug und warum:

![Eine fehlgeschlagene Konvertierung mit dem Fehler ihres Sprachaufrufs](../../images/admin-jobs-failed.png)

**Nichts wird von selbst gelöscht.** Das Protokoll wächst mit jeder Anfrage, deshalb leere es von Zeit zu Zeit: Setze *Older than (days)* (zum Beispiel auf 30), sieh dir an, was aufgelistet wird, und lösche es dann. Die [Bereinigung](cleanup.md) entfernt die Jobs eines Benutzers zusammen mit dem Benutzer.

![Die Jobs älter als 30 Tage und die Rückfrage, bevor sie gelöscht werden](../../images/admin-jobs-old.png)

![Die Rückfrage, bevor die Jobs gelöscht werden](../../images/admin-jobs-delete.png)

Ein Job wird ins Protokoll geschrieben, wenn er endet; ein Job, der noch läuft, steht nur auf `/jobs`. Wenn die Anwendung stoppt, gehen die laufenden Jobs verloren, weil sie nie beendet wurden. Ein Job, den niemand beendet hat und der eine halbe Stunde lang keinen Aufruf macht, wird als abgeschlossen beendet.
