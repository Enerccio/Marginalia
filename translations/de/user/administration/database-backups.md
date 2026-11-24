---
label: Datenbanksicherungen
order: 90
---

# Datenbanksicherungen

Eine Datenbanksicherung ist eine vollständige Kopie der Marginalia-Datenbank: alle Benutzer mit all ihren Büchern, Lorebooks, Einstellungen und Erweiterungsdaten. Verwenden Sie sie, um die Installation gegen Fehler und Ausfälle zu schützen und um sie auf einen anderen Computer zu übertragen. Um ein einzelnes Buch zu sichern, haben Benutzer [Buchsicherungen](../books/backups.md).

![Der Reiter Database Backups](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Eine Sicherung erstellen

Klicken Sie auf dem Reiter **Database Backups** auf **Create Backup**. Die Kopie wird erstellt, während Marginalia läuft, und ist auch dann konsistent, wenn gerade geschrieben wird. Sie erscheint in der Liste mit **Name**, dem Datum **Created**, dem **Type** (*Manual* oder *Scheduled*) und der **Size**. Die Werkzeugleiste zeigt die aktuelle Größe der Datenbank.

Jede Sicherung hat folgende Schaltflächen:

| Schaltfläche | |
|---|---|
| **Download** | Lädt die Sicherungsdatei (`.sqlite`) herunter - bewahren Sie eine Kopie außerhalb des Servers auf. |
| **Restore on restart** | Stellt die Sicherung wieder her, siehe [unten](#restoring). |
| Papierkorb | Löscht die Sicherung. |

Mit **Upload Backup** fügen Sie der Liste eine Sicherungsdatei (`.sqlite` oder `.db`) hinzu, zum Beispiel eine, die von einer anderen Installation heruntergeladen wurde. Die Datei wird zuerst geprüft und mit *The database can't be restored: ...* abgelehnt, samt Grund, wenn sie keine SQLite-Datenbank ist, beschädigt ist, keine Marginalia-Datenbank ist (die Datenbank einer anderen Anwendung) oder aus einer neueren Marginalia-Version stammt als der laufenden.

!!!warning Sicherungen enthalten Geheimnisse
Eine Datenbanksicherung enthält alles, auch die Passwort-Hashes aller Benutzer und ihre API-Schlüssel. Die API-Schlüssel sind mit der Datei `secret.key` im [Datenordner](index.md#the-data-folder) verschlüsselt, die nicht Teil der Sicherung ist - wer aber sowohl die Sicherung als auch `secret.key` besitzt, kann sie lesen. Bewahren Sie heruntergeladene Sicherungen so sicher auf wie den Server selbst.
!!!

Die Dateien werden im Ordner `db-backups` des [Datenordners](index.md#the-data-folder) gespeichert. Kopien, die Marginalia selbst anlegt - `pre-restore-...` vor einer [Wiederherstellung](#restoring) und `pre-migration-...` vor einem [Upgrade](#copy-before-an-upgrade) -, sind ebenfalls in der Liste und werden als *Manual* angezeigt, sodass sie nie durch die Rotation der geplanten Sicherungen gelöscht werden. Buchsicherungen und Erweiterungs-JARs sind separate Dateien und nicht Teil einer Datenbanksicherung.

**Hochgeladene Dateien (die [Bilder der Teile](../books/story-editor.md#images)) sind ebenfalls nicht Teil einer Datenbanksicherung.** Die Datenbank kennt sie nur als Einträge des Reiters [Resources](../resources.md); die Dateien liegen im Ordner `data/<login>/images` des Datenordners. Das ist in Ordnung: Nach einer Wiederherstellung sind die Einträge die aus dem Zeitpunkt der Sicherung, und die Bilder, die sie verwenden, liegen weiterhin im Ordner. Dateien von Bildern, die nach der Sicherung hinzugefügt wurden, bleiben einfach übrig (nichts verweist auf sie) und belegen Speicherplatz. Um die Bilder zu sichern, nehmen Sie den gesamten Datenordner in Ihre eigenen Sicherungen auf oder packen Sie sie in eine [Buchsicherung](../books/backups.md#pictures-in-a-backup).

Wenn Sie eine Sicherung auf **einer anderen Installation** wiederherstellen, kopieren Sie außerdem `secret.key` von der ursprünglichen Installation (bei gestopptem Marginalia), da sonst die gespeicherten API-Schlüssel nicht entschlüsselt werden können und jeder Benutzer sie erneut eingeben muss.

<a id="scheduled-backups"></a>

## Geplante Sicherungen

Der Abschnitt **Scheduled Backups** erstellt Sicherungen automatisch:

| Feld | |
|---|---|
| **Create backups automatically** | Schaltet geplante Sicherungen ein. |
| **Schedule (cron)** | Wann sie erstellt werden, siehe [unten](#schedule-syntax). Die nächsten drei Zeitpunkte werden unter dem Feld angezeigt. |
| **Keep scheduled backups** | Wie viele geplante Sicherungen aufbewahrt werden; ältere werden gelöscht. `0` behält alle. Manuelle und hochgeladene Sicherungen werden nie gelöscht. |

Klicken Sie auf **Save Schedule**, um den Zeitplan zu übernehmen. Die Statuszeile zeigt, wann die letzte geplante Sicherung erstellt wurde und wann die nächste fällig ist.

Wenn Marginalia zu einem geplanten Zeitpunkt nicht lief - typisch für die Desktop-App, die nur läuft, während Sie schreiben -, erstellt es die verpasste Sicherung etwa 30 Sekunden nach dem nächsten Start. Wenn Sie den Zeitplan in diesen 30 Sekunden ausschalten oder ändern, wird diese Sicherung abgebrochen.

<a id="schedule-syntax"></a>

### Syntax des Zeitplans

Der Zeitplan ist ein Cron-Ausdruck mit fünf Feldern, `minute hour day-of-month month day-of-week`:

| Ausdruck | Läuft |
|---|---|
| `0 3 * * *` | jeden Tag um 3:00 |
| `0 */6 * * *` | alle 6 Stunden |
| `30 2 * * 1` | montags um 2:30 (Wochentag: `0` oder `7` ist Sonntag, `1` Montag ...) |
| `0 12 1 * *` | am 1. jedes Monats um 12:00 |
| `@daily`, `@weekly`, `@monthly` | einmal am Tag / in der Woche / im Monat um Mitternacht (`@hourly`, `@yearly` funktionieren auch) |

Die Zeiten gelten in der Zeitzone des Servers (in Docker setzen Sie sie mit der Umgebungsvariable `TZ`).

!!!
Um geplante Sicherungen auszuschalten, deaktivieren Sie *Create backups automatically* und klicken Sie auf *Save Schedule*. Der Ausdruck bleibt erhalten, sodass Sie die Sicherungen später wieder einschalten können. Er wird weiterhin geprüft - ein ungültiger Ausdruck wird nie gespeichert -, aber solange die Sicherungen ausgeschaltet sind, kann das Feld auch leer sein.
!!!

<a id="restoring"></a>

## Wiederherstellen

Eine Datenbank kann nicht ersetzt werden, solange Marginalia sie verwendet. Deshalb findet eine Wiederherstellung beim **nächsten Start** statt:

1. Klicken Sie neben der Sicherung auf **Restore on restart** und bestätigen Sie. Der Reiter zeigt *Database restore is scheduled. Restart the application to apply it.* - mit **Cancel Restore** machen Sie dies rückgängig.
2. Starten Sie Marginalia neu: Beenden Sie die [Desktop-App](../getting-started/desktop-app.md) und starten Sie sie erneut, oder führen Sie auf dem [Server](../getting-started/docker-server.md) `docker compose restart` aus.

Beim Start wird die aktuelle Datenbank nach `db-backups/pre-restore-<time>-marginalia.sqlite` verschoben - sie erscheint in der Liste, sodass eine Wiederherstellung rückgängig gemacht werden kann, indem diese Datei wiederhergestellt wird - und die Sicherung nimmt ihren Platz ein. Eine Sicherung aus einer älteren Marginalia-Version wird automatisch aktualisiert.

Die Sicherung wird vor dem Austausch erneut geprüft (dieselben Prüfungen wie bei *Upload Backup*; auch *Restore on restart* lehnt eine Sicherung ab, die sie nicht besteht). Besteht sie die Prüfung nicht oder schlägt ihre Aktualisierung fehl, behält Marginalia die aktuelle Datenbank (bzw. stellt sie wieder her) und startet normal; die abgelehnte Datei wird nach `db-backups/rejected-restore-<time>-marginalia.sqlite` verschoben, und das Protokoll nennt den Grund.

Alles, was nach der Sicherung entstanden ist, geht verloren: Bücher, Teile und Einstellungen. Angemeldete Benutzer müssen sich erneut anmelden, wenn sich ihr Konto geändert hat.

<a id="copy-before-an-upgrade"></a>

## Kopie vor einem Upgrade

Wenn eine neue Version von Marginalia die Struktur einer bestehenden Datenbank ändern wird, speichert sie zuerst eine Kopie als `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, wobei die Version die ist, auf der die Datenbank zu diesem Zeitpunkt stand. Die Kopie befindet sich in der Liste der Sicherungen, sodass ein Upgrade rückgängig gemacht werden kann, indem die alte Version von Marginalia installiert und diese Kopie [wiederhergestellt](#restoring) wird. Die letzten drei Kopien werden aufbewahrt. Eine neue Installation hat nichts zu kopieren, und wenn die Kopie nicht erstellt werden kann (etwa weil die Festplatte voll ist), vermerkt das Protokoll dies und das Upgrade wird fortgesetzt.

Eine Migration, die auf halbem Weg fehlschlägt, wird automatisch zurückgerollt, und Marginalia startet nicht. Die Kopie ist für ein Upgrade gedacht, das durchgelaufen ist, aber etwas bewirkt hat, das Sie nicht wollen.

<a id="moving-to-another-computer"></a>

## Auf einen anderen Computer umziehen

1. Erstellen Sie eine Sicherung und laden Sie sie mit **Download** herunter.
2. Installieren Sie Marginalia auf dem neuen Computer und legen Sie beim ersten Start einen temporären Administrator an.
3. **Upload Backup**, **Restore on restart**, Neustart.
4. Melden Sie sich mit den Konten aus der Sicherung an.

Kopieren Sie außerdem den Ordner `data` (Buchsicherungen und die Bilder der Teile) und den Ordner `extensions` aus dem alten [Datenordner](index.md#the-data-folder). Oder beenden Sie Marginalia einfach und kopieren Sie den gesamten Ordner `.marginalia`.
