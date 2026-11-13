---
label: Berechtigungen
order: 95
---

# Berechtigungen

Eine Berechtigung erlaubt es einem Benutzer, etwas zu tun, das nicht für alle vorgesehen ist. Administratoren vergeben Berechtigungen einzeln an Benutzer;
wer keine hat, kann in Marginalia alles Übrige wie gewohnt tun (Bücher schreiben, Lorebooks aufbauen, die Modelle nutzen, die Administratoren [freigeben](../inference-providers.md#shared-providers)).

| Berechtigung | Was sie erlaubt | Einstellungen |
|---|---|---|
| **Eigene Modelle anlegen** | Hinzufügen von [Inference-Providern](../inference-providers.md): Ohne sie kann der Benutzer nur die Provider nutzen, die die Administratoren freigeben; die Provider, die er bereits hat, funktionieren weiter und können weiterhin bearbeitet werden. | |
| **Hilfeassistent** | Fragen an den [Hilfeassistenten](help-assistant.md) stellen, den Chat, der aus dem Handbuch antwortet. | **Limits anwenden** und **Navigation in der Oberfläche erlauben**, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |
| **Agenten** | (Experimentell, siehe [Agenten](../agents.md)) Der Reiter [Agenten](../agents.md): Agenten anlegen, klonen und auf einem Buch laufen lassen. | **Maximale Anzahl Agenten**, **Gleichzeitige Läufe**, **Gleichzeitige Anfragen an das Modell (je Lauf)**, **Token-Budget eines Laufs** sowie die Kontingente für Kopien und Dateien, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |
| **Versionsverlauf** | Die höchste Anzahl [Versionen eines Abschnitts](../books/version-history.md#how-many-versions-are-kept), die der Verlauf der Bücher des Benutzers speichert. Jeder Benutzer hat sie mit 50 (auch ein Benutzer ohne die Zeile); eine nicht erteilte Berechtigung bedeutet für diesen Benutzer keinen Verlauf. | **Aufbewahrte Versionen je Abschnitt**, eine Obergrenze, die die Ordner, die Bücher und der Benutzer nicht erhöhen können. Ein leeres Feld setzt hier kein Limit. Eine neu hinzugefügte Berechtigung beginnt bei 50. |
| **Benachrichtigungen** | Die [Nachricht an eine URL](../agents.md#when-you-are-not-there), die der Server sendet, wenn ein Agent den Benutzer braucht und kein Bildschirm geöffnet ist. Der Server ruft eine Adresse auf, die der Benutzer eingegeben hat; deshalb ist es eine eigene Berechtigung: Ohne sie fehlen die Felder in den Einstellungen des Benutzers, und es wird nichts gesendet. | **Adressen auf diesem Rechner und in privaten Netzwerken erlauben**, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |

Administratoren haben immer alle Berechtigungen, sie brauchen keine, daher gibt es für sie nichts zu vergeben (die Schaltflächen und Bereiche eines Administrators hängen nie von einer Berechtigung ab). Neue Benutzer haben keine Berechtigung: Geben Sie ihnen, was sie brauchen, direkt nachdem Sie sie [hinzugefügt](users.md#adding-a-user) haben.

<a id="giving-and-taking-permissions"></a>

## Berechtigungen vergeben und entziehen

Die Schlüssel-Schaltfläche in **Admin → Benutzer**, in der Zeile des Benutzers, öffnet die Berechtigungen dieses Benutzers:

![Die Berechtigungen eines Benutzers](../../images/permissions-dialog.png)

Das Fenster ist eine Tabelle der Berechtigungen, die der Benutzer hat, mit drei Spalten:

| Spalte | |
|---|---|
| **Name** | Die Berechtigung. Eine Berechtigung mit Einstellungen hat einen Pfeil vor ihrem Namen, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |
| **Erteilt** | Ob die Berechtigung wirkt. Eine nicht erteilte Berechtigung ist dasselbe wie keine, sie wird aber mit ihren Einstellungen behalten, sodass Sie sie vorübergehend ausschalten und später wieder einschalten können. |
| Die Papierkorb-Schaltfläche | Löscht die Berechtigung (und ihre Einstellungen) endgültig. |

Um eine Berechtigung zu vergeben, klicken Sie oberhalb der Tabelle auf **Berechtigung hinzufügen** und wählen Sie eine der Berechtigungen, die der Benutzer noch nicht hat;
sie wird der Tabelle als erteilt hinzugefügt. Gespeichert wird erst, wenn Sie auf **Speichern** klicken:

![Auswahl einer hinzuzufügenden Berechtigung](../../images/permissions-add.png)

- **Speichern** ist deaktiviert, solange nichts zu speichern ist.
- **Beenden** schließt das Fenster; gibt es ungespeicherte Änderungen, fragt es zuerst nach, sodass eine Änderung nie versehentlich verloren geht.
- Eine gelöschte Berechtigung bleibt in der Tabelle, bis Sie speichern, und wird erst dann endgültig entfernt. Vorher können Sie sie zurückholen, indem Sie das Fenster mit **Beenden** ohne Speichern schließen.

Nach dem Speichern wird die Seite des Benutzers neu geladen, wenn er angemeldet ist, sodass die Schaltflächen und Bereiche, die von den Berechtigungen abhängen, sofort erscheinen oder verschwinden. Ihre eigene Seite wird nicht neu geladen, auch nicht, wenn Sie Ihre eigenen Berechtigungen ändern.

<a id="settings-of-a-permission"></a>

## Einstellungen einer Berechtigung

Manche Berechtigungen haben Einstellungen, die zum Benutzer gehören, nicht zur ganzen Installation. Die Zeile einer solchen Berechtigung beginnt mit einem Pfeil: Klicken Sie darauf, um die Einstellungen aufzuklappen (sie sind eingeklappt, wenn das Fenster sich öffnet, und eine gerade hinzugefügte Berechtigung ist aufgeklappt), klicken Sie erneut, um sie einzuklappen. Das Ein- und Ausklappen dient nur dazu, eine lange Liste kurz zu halten; es ändert nichts und gilt nicht als ungespeicherte Änderung.

![Die Einstellungen der Berechtigung Hilfeassistent](../../images/permissions-settings.png)

**Hilfeassistent**, **Agenten** und **Benachrichtigungen** haben solche Einstellungen. Die des Hilfeassistenten:

| Einstellung | |
|---|---|
| **Limits anwenden** | Bei einem freigegebenen Modell: ob die Fragen des Benutzers wie jede andere Anfrage auf sein Limit und seine Nutzung angerechnet werden, siehe [Wer zahlt](help-assistant.md#who-pays). Standardmäßig aus. |
| **Navigation in der Oberfläche erlauben** | Erlaubt dem Assistenten, einen Bereich zu öffnen, einen Reiter auszuwählen oder den Dialog zum Hinzufügen zu öffnen, wenn der Benutzer ihn bittet, ihm den Weg zu zeigen, siehe [Den Weg zeigen](help-assistant.md#showing-the-way). Standardmäßig aus. |

Die der Agenten; ein leeres Feld gilt als Standard:

| Einstellung | |
|---|---|
| **Maximale Anzahl Agenten** | Wie viele Agenten der Benutzer haben kann, die Klone eines Agenten eingeschlossen. Fünf, wenn leer. Null erlaubt dem Benutzer, seine Agenten zu behalten und laufen zu lassen, aber keinen weiteren anzulegen oder zu klonen. Ein gelöschter Agent gibt seinen Platz frei. |
| **Gleichzeitige Läufe** | Wie viele Läufe des Benutzers gleichzeitig arbeiten können. Einer, wenn leer. |
| **Gleichzeitige Anfragen an das Modell (je Lauf)** | Wie viele Anfragen an das Modell ein Lauf gleichzeitig offen haben kann, der Agent und seine [Unteragenten](../agents.md#sub-agents) zusammen. Einer, wenn leer: Während ein Unteragent das Modell fragt, wartet der Agent, und umgekehrt. Mehr lässt Unteragenten parallel zum Agenten arbeiten (das Modell des Agenten kann zusätzlich eine eigene [Obergrenze für Anfragen](../inference-providers.md#general-settings) haben, die ebenfalls gilt). |
| **Token-Budget eines Laufs** | Die höchste Anzahl Tokens (gesendete und erzeugte), die ein Lauf des Benutzers verbrauchen kann. Leer oder null bedeutet kein Limit. |
| **Arbeitskopien** | Wie viele Arbeitskopien von Büchern die Läufe des Benutzers gleichzeitig behalten. Ein Lauf, der nicht beendet ist, behält seine Kopie, ein pausierter ebenfalls, und gibt sie frei, wenn er beendet, gestoppt oder gelöscht wird. Leer oder null bedeutet kein Limit außer der Anzahl der Agenten. |
| **Größte Datei (KB)** | Die größte Datei, die ein Agent des Benutzers in seinen Arbeitsbereich schreiben kann. 1024 (1 MB), wenn leer. |
| **Größe des Arbeitsbereichs (MB)** | Wie viel alle Dateien im Arbeitsbereich eines Agenten des Benutzers zusammen belegen dürfen. 20, wenn leer. |
| **Dateien in einem Arbeitsbereich** | Wie viele Dateien der Arbeitsbereich eines Agenten des Benutzers enthalten kann; Ordner sind frei. 500, wenn leer. |
| **Checkpoints eines Laufs (MB)** | Wie viel alle [Checkpoints](../agents.md#checkpoints) eines Laufs (Wiederherstellungspunkte der Arbeitskopie und ihres Arbeitsbereichs) zusammen belegen dürfen; ein Teil davon ist für die automatischen reserviert. 100, wenn leer. |

![Die Einstellungen der Berechtigung Agenten](../../images/permissions-settings-agents.png)

Die der Benachrichtigungen:

| Einstellung | |
|---|---|
| **Adressen auf diesem Rechner und in privaten Netzwerken erlauben** | Eine Unterberechtigung: Das Kästchen kann nur angehakt werden, solange **Benachrichtigungen** erteilt ist (sonst ist es ausgegraut, und wenn die Berechtigung entzogen wird, wird es geleert). Ohne sie werden nur Adressen im Internet aufgerufen: nicht `localhost`, kein privates Netzwerk (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), keine Link-Local- oder Shared-Adresse (`169.254.x.x`, dort hält der Metadatendienst von Cloud-Hosts seine Daten, `100.64-127.x.x`), keine Unique-Local-Adressen von IPv6. Setzen Sie den Haken für einen Benutzer, dessen ntfy oder Gotify auf demselben Rechner oder in Ihrem eigenen Netzwerk läuft. Standardmäßig aus. Ein Name wird anhand der Adressen geprüft, auf die er zeigt; ein Name, der auf eine private Adresse zeigt, wird also ebenfalls abgelehnt, ebenso einer, der nicht gefunden werden kann. |

Administratoren brauchen weder die Berechtigung noch die Unterberechtigung: Sie erhalten die Nachrichten und dürfen lokale Adressen verwenden. Auf einem Server, den Sie mit Menschen teilen, denen Sie nicht vollständig vertrauen, lassen Sie die Unterberechtigung aus: Der Server würde sonst Anfragen an alles stellen, was der Benutzer eingibt, innerhalb Ihres Netzwerks.

<a id="when-permissions-were-introduced"></a>

## Wann die Berechtigungen eingeführt wurden

Vor den Berechtigungen waren **Benutzer können eigene Modelle anlegen** und der Schalter **Aktiviert** des Hilfeassistenten (mit **Limits anwenden** und **Navigation in der Oberfläche erlauben**) Einstellungen der ganzen Installation. Marginalia hat sie beim Update in Berechtigungen überführt: Jeder damals vorhandene Benutzer erhielt **Eigene Modelle anlegen**, wenn alle Modelle anlegen durften (was der Standard war), und **Hilfeassistent** mit denselben zwei Einstellungen, wenn der Assistent aktiviert war. Für sie ändert sich nichts, bis Sie es ändern; Benutzer, die Sie ab jetzt hinzufügen, beginnen ohne Berechtigung.
