---
label: Berechtigungen
order: 95
---

# Berechtigungen

Eine Berechtigung erlaubt einem Benutzer etwas, das nicht allen offensteht. Administratoren vergeben Berechtigungen einzeln an Benutzer. Ein Benutzer ohne Berechtigungen kann alles andere in Marginalia wie gewohnt tun, also Bücher schreiben, Lorebooks aufbauen und die Modelle nutzen, die Administratoren [freigeben](../inference-providers.md#shared-providers).

| Permission | What it allows | Settings |
|---|---|---|
| **Create own models** | Hinzufügen von [Inference-Providern](../inference-providers.md). Ohne diese Berechtigung kann der Benutzer nur die Provider nutzen, die Administratoren freigegeben haben. Bereits vorhandene Provider des Benutzers funktionieren weiter und können bearbeitet werden. | |
| **Help assistant** | Fragen an den [Hilfe-Assistenten](help-assistant.md), den Chat, der Fragen aus dem Handbuch beantwortet. | **Apply limits** und **Allow navigating the interface**, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |
| **Agents** | (Experimentell, siehe [Agents](../agents.md)) Der Tab [Agents](../agents.md): Agents erstellen, klonen und auf einem Buch ausführen. | **Most agents**, **Concurrent runs**, **Requests to the model at once**, **Token budget of a run** sowie die Kontingente für Kopien und Dateien, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |
| **Version history** | Die höchste Anzahl an [Versionen eines Teils](../books/version-history.md#how-many-versions-are-kept), die der Versionsverlauf der Bücher des Benutzers speichert. Jeder Benutzer hat 50 (auch ein Benutzer ohne diese Zeile). Eine nicht vergebene Berechtigung bedeutet für diesen Benutzer keinen Verlauf. | **Versions kept per part**, eine Obergrenze, die Ordner, Bücher und der Benutzer nicht überschreiten können. Ein leeres Feld setzt hier kein Limit. Eine neu hinzugefügte Berechtigung beginnt bei 50. |
| **API access** | Schlüssel der [HTTP-API](../account.md#api-keys): Programme, die im Namen des Benutzers in seinen Büchern agieren. Wird die Berechtigung entzogen, werden die Schlüssel des Benutzers gestoppt. Administratoren brauchen keine Berechtigung. Nur sie können einem Schlüssel Zugriff auf das Update oder die Erweiterungen geben. | |
| **Notifications** | Die [Nachricht an eine URL](../agents.md#when-you-are-not-there), die der Server sendet, wenn ein Agent den Benutzer braucht und kein Bildschirm geöffnet ist. Der Server ruft eine Adresse auf, die der Benutzer eingegeben hat. Deshalb ist es eine eigene Berechtigung: Ohne sie gibt es die Felder in den Einstellungen des Benutzers nicht, und es wird nichts gesendet. | **Allow addresses on this machine and in private networks**, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |

Administratoren haben immer alle Berechtigungen. Sie brauchen keine, daher gibt es für sie nichts zu vergeben (die Schaltflächen und Bereiche eines Administrators hängen nie von einer Berechtigung ab). Neue Benutzer haben keine Berechtigungen. Geben Sie ihnen, was sie brauchen, direkt nachdem Sie sie [hinzugefügt](users.md#adding-a-user) haben.

<a id="giving-and-taking-permissions"></a>

## Berechtigungen vergeben und entziehen

Die Schlüssel-Schaltfläche in **Admin → Users** öffnet in der Zeile des Benutzers dessen Berechtigungen:

![Die Berechtigungen eines Benutzers](../../images/permissions-dialog.png)

Das Fenster ist eine Tabelle der Berechtigungen des Benutzers mit drei Spalten:

| Column | |
|---|---|
| **Name** | Die Berechtigung. Eine Berechtigung mit Einstellungen hat einen Pfeil vor ihrem Namen, siehe [Einstellungen einer Berechtigung](#settings-of-a-permission). |
| **Granted** | Ob die Berechtigung wirkt. Eine nicht vergebene Berechtigung entspricht keiner Berechtigung, bleibt aber mit ihren Einstellungen erhalten. So können Sie sie eine Zeit lang ausschalten und später wieder einschalten. |
| Die Papierkorb-Schaltfläche | Löscht die Berechtigung (und ihre Einstellungen) endgültig. |

Um eine Berechtigung zu vergeben, klicken Sie oberhalb der Tabelle auf **Add permission** und wählen eine der Berechtigungen aus, die der Benutzer noch nicht hat. Sie wird in die Tabelle aufgenommen und ist vergeben. Gespeichert wird erst, wenn Sie auf **Save** klicken:

![Auswahl einer hinzuzufügenden Berechtigung](../../images/permissions-add.png)

- **Save** ist deaktiviert, solange nichts zu speichern ist.
- **Exit** schließt das Fenster. Gibt es ungespeicherte Änderungen, wird vorher nachgefragt, damit eine Änderung nicht versehentlich verloren geht.
- Eine gelöschte Berechtigung bleibt in der Tabelle, bis Sie speichern, und wird erst dann endgültig entfernt. Vorher können Sie sie zurückholen, indem Sie das Fenster mit **Exit** ohne Speichern schließen.

Nach dem Speichern wird die Seite des Benutzers neu geladen, sofern er angemeldet ist. Dadurch erscheinen oder verschwinden die Schaltflächen und Bereiche, die von den Berechtigungen abhängen, sofort. Ihre eigene Seite wird nicht neu geladen, auch wenn Sie Ihre eigenen Berechtigungen ändern.

<a id="settings-of-a-permission"></a>

## Einstellungen einer Berechtigung

Manche Berechtigungen haben Einstellungen, die dem Benutzer gehören und nicht der gesamten Installation. Die Zeile einer solchen Berechtigung beginnt mit einem Pfeil. Klicken Sie darauf, um die Einstellungen aufzuklappen. Beim Öffnen des Fensters sind sie zugeklappt, eine gerade hinzugefügte Berechtigung ist dagegen aufgeklappt. Ein erneuter Klick klappt sie wieder zu. Das Zuklappen dient nur dazu, eine lange Liste übersichtlich zu halten. Es ändert nichts und gilt nicht als ungespeicherte Änderung.

![Die Einstellungen der Berechtigung Help assistant](../../images/permissions-settings.png)

**Help assistant**, **Agents** und **Notifications** haben Einstellungen. Die des Hilfe-Assistenten:

| Setting | |
|---|---|
| **Apply limits** | Bei einem geteilten Modell: ob die Fragen des Benutzers wie jede andere Anfrage auf sein Limit und seine Nutzung angerechnet werden, siehe [Wer bezahlt](help-assistant.md#who-pays). Standardmäßig aus. |
| **Allow navigating the interface** | Erlaubt dem Assistenten, einen Bereich zu öffnen, einen Tab auszuwählen oder den Dialog zum Hinzufügen zu öffnen, wenn der Benutzer ihn bittet, ihm den Weg zu zeigen, siehe [Den Weg zeigen](help-assistant.md#showing-the-way). Standardmäßig aus. |

Die der Agents. Ein leeres Feld bedeutet den Standardwert:

| Setting | |
|---|---|
| **Most agents** | Wie viele Agents der Benutzer haben kann, einschließlich der Klone eines Agents. Ist das Feld leer, sind es fünf. Null bedeutet, dass der Benutzer seine Agents behalten und ausführen, aber keinen weiteren erstellen oder klonen kann. Ein gelöschter Agent gibt seinen Platz wieder frei. |
| **Concurrent runs** | Wie viele Läufe des Benutzers gleichzeitig arbeiten können. Ist das Feld leer, ist es einer. |
| **Requests to the model at once (per run)** | Wie viele Anfragen an das Modell ein Lauf gleichzeitig in der Luft haben kann, der Agent und seine [Sub-Agents](../agents.md#sub-agents) zusammen. Ist das Feld leer, ist es eine: Während ein Sub-Agent das Modell befragt, wartet der Agent, und umgekehrt. Mehr erlaubt es Sub-Agents, parallel zum Agent zu arbeiten. (Das Modell des Agents kann außerdem eine eigene [Obergrenze für Anfragen](../inference-providers.md#general-settings) haben, die ebenfalls gilt.) |
| **Token budget of a run** | Die höchste Anzahl an Tokens (gesendet und erzeugt), die ein Lauf des Benutzers verbrauchen kann. Leer oder null bedeutet kein Limit. |
| **Working copies** | Wie viele Arbeitskopien von Büchern die Läufe des Benutzers gleichzeitig halten. Ein nicht beendeter Lauf behält seine Kopie, auch ein pausierter, und gibt sie frei, wenn er beendet, gestoppt oder gelöscht wird. Leer oder null bedeutet kein Limit außer der Anzahl der Agents. |
| **Largest file (KB)** | Die größte Datei, die ein Agent des Benutzers in seinen Arbeitsbereich schreiben kann. Ist das Feld leer, sind es 1024 (1 MB). |
| **Workspace size (MB)** | Wie viel alle Dateien im Arbeitsbereich eines Agents des Benutzers zusammen belegen dürfen. Ist das Feld leer, sind es 20. |
| **Files in a workspace** | Wie viele Dateien der Arbeitsbereich eines Agents des Benutzers enthalten kann. Ordner sind kostenlos. Ist das Feld leer, sind es 500. |
| **Checkpoints of a run (MB)** | Wie viel alle [Checkpoints](../agents.md#checkpoints) eines Laufs (Wiederherstellungspunkte der Arbeitskopie und ihres Arbeitsbereichs) zusammen belegen dürfen. Ein Teil davon ist für die automatischen Checkpoints reserviert. Ist das Feld leer, sind es 100. |

![Die Einstellungen der Berechtigung Agents](../../images/permissions-settings-agents.png)

Die der Notifications:

| Setting | |
|---|---|
| **Allow addresses on this machine and in private networks** | Eine Unterberechtigung: Das Kästchen lässt sich nur ankreuzen, solange **Notifications** vergeben ist. Andernfalls ist es ausgegraut, und wenn Sie die Berechtigung entziehen, wird es geleert. Ohne diese Option werden nur Adressen im Internet aufgerufen. Nicht aufgerufen werden `localhost`, private Netzwerke (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), Link-Local- und Shared-Adressen (`169.254.x.x`, dort liegt bei Cloud-Hosts der Metadaten-Dienst, `100.64-127.x.x`) sowie die Unique-Local-Adressen von IPv6. Aktivieren Sie das Kästchen für einen Benutzer, dessen ntfy oder Gotify auf derselben Maschine oder in Ihrem eigenen Netzwerk läuft. Standardmäßig aus. Ein Name wird anhand der Adressen geprüft, auf die er auflöst. Ein Name, der auf eine private Adresse zeigt, wird daher ebenfalls abgelehnt, und ebenso einer, der nicht gefunden werden kann. |

Administratoren brauchen weder die Berechtigung noch die Unterberechtigung. Sie erhalten die Nachrichten und dürfen lokale Adressen verwenden. Lassen Sie die Unterberechtigung auf einem Server, den Sie mit nicht vollständig vertrauenswürdigen Personen teilen, ausgeschaltet. Andernfalls würde der Server Anfragen an alles senden, was der Benutzer eingibt, und zwar innerhalb Ihres Netzwerks.

<a id="when-permissions-were-introduced"></a>

## Wann die Berechtigungen eingeführt wurden

Vor den Berechtigungen waren **Users can create their own models** und der Schalter **Enabled** des Hilfe-Assistenten (mit **Apply limits** und **Allow navigating the interface**) Einstellungen der gesamten Installation. Beim Update hat Marginalia sie in Berechtigungen überführt. Jeder damals vorhandene Benutzer erhielt **Create own models**, wenn alle Modelle erstellen durften (das war der Standard), und **Help assistant** mit denselben beiden Einstellungen, wenn der Assistent aktiviert war. Für diese Benutzer ändert sich nichts, bis Sie es ändern. Benutzer, die Sie ab jetzt hinzufügen, beginnen ohne Berechtigungen.
