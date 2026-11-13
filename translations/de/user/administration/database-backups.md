---
label: Datenbanksicherungen
order: 90
---

# Datenbanksicherungen

Eine Datenbanksicherung ist eine vollständige Kopie der Marginalia-Datenbank: alle Benutzer mit all ihren Büchern, Lorebooks, Einstellungen und Erweiterungsdaten. Verwenden Sie sie, um die Installation gegen Fehler und Ausfälle zu schützen und um sie auf einen anderen Computer zu übertragen. Um ein einzelnes Buch zu sichern, stehen Benutzern [Buchsicherungen](../books/backups.md) zur Verfügung.

![Der Reiter Datenbanksicherungen](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Eine Sicherung erstellen

Klicken Sie im Reiter **Datenbanksicherungen** auf **Sicherung erstellen**. Die Kopie wird angefertigt, während Marginalia läuft, und ist auch dann konsistent, wenn Personen gerade schreiben. Sie erscheint in der Liste mit ihrem **Namen**, dem Datum **Erstellt**, dem **Typ** (*Manuell* oder *Geplant*) und der **Größe**. Die Werkzeugleiste zeigt die aktuelle Größe der Datenbank an.

Jede Sicherung hat:

| Schaltfläche | |
|---|---|
| **Herunterladen** | Lädt die Sicherungsdatei (`.sqlite`) herunter – bewahren Sie eine Kopie außerhalb des Servers auf. |
| **Beim Neustart wiederherstellen** | Stellt die Sicherung wieder her, siehe [unten](#restoring). |
| Papierkorb | Löscht die Sicherung. |

**Sicherung hochladen** fügt der Liste eine Sicherungsdatei (`.sqlite` oder `.db`) hinzu, zum Beispiel eine, die von einer anderen Installation heruntergeladen wurde. Die Datei wird zuerst geprüft und abgelehnt, mit *Die Datenbank kann nicht wiederhergestellt werden: ...* und dem Grund, wenn sie keine SQLite-Datenbank ist, beschädigt ist, keine Marginalia-Datenbank ist (die Datenbank einer anderen Anwendung) oder aus einer neueren Marginalia-Version stammt als der laufenden.

!!!warning Sicherungen enthalten Geheimnisse
Eine Datenbanksicherung enthält alles, auch die Passwort-Hashes aller Benutzer und deren API-Schlüssel. Die API-Schlüssel sind mit der Datei `secret.key` im [Datenordner](index.md#the-data-folder) verschlüsselt, die nicht Teil der Sicherung ist – wer jedoch sowohl die Sicherung als auch `secret.key` besitzt, kann sie lesen. Bewahren Sie heruntergeladene Sicherungen so sicher auf wie den Server selbst.
!!!

Die Dateien werden im Ordner `db-backups` des [Datenordners](index.md#the-data-folder) gespeichert. Kopien, die Marginalia selbst anlegt – `pre-restore-...` vor einer [Wiederherstellung](#restoring) und `pre-migration-...` vor einem [Upgrade](#copy-before-an-upgrade) –, stehen ebenfalls in der Liste, angezeigt als *Manuell*, sodass sie nie durch die Rotation der geplanten Sicherungen gelöscht werden. Buchsicherungen und Erweiterungs-JARs sind separate Dateien und nicht Teil einer Datenbanksicherung.

**Hochgeladene Dateien (die [Bilder der Teile](../books/story-editor.md#images)) sind ebenfalls nicht Teil einer Datenbanksicherung.** Die Datenbank kennt sie nur als Einträge im Reiter [Ressourcen](../resources.md); die Dateien liegen im Ordner `data/<login>/images` des Datenordners. Das ist in Ordnung: Nach einer Wiederherstellung sind die Einträge jene aus dem Zeitpunkt der Sicherung, und die Bilder, die sie verwenden, liegen weiterhin im Ordner. Dateien von Bildern, die nach der Sicherung hinzugefügt wurden, bleiben einfach übrig (nichts verweist auf sie) und belegen Speicherplatz. Um die Bilder zu sichern, nehmen Sie den gesamten Datenordner in Ihre eigenen Sicherungen auf oder packen Sie sie in eine [Buchsicherung](../books/backups.md#pictures-in-a-backup).

Wenn Sie eine Sicherung auf einer **anderen Installation** wiederherstellen, kopieren Sie auch `secret.key` von der ursprünglichen Installation (bei gestopptem Marginalia). Andernfalls können die gespeicherten API-Schlüssel nicht entschlüsselt werden, und jeder Benutzer muss sie erneut eingeben.

<a id="scheduled-backups"></a>

## Geplante Sicherungen

Der Abschnitt **Geplante Sicherungen** erstellt Sicherungen automatisch:

| Feld | |
|---|---|
| **Sicherungen automatisch erstellen** | Schaltet geplante Sicherungen ein. |
| **Zeitplan (Cron)** | Wann sie erstellt werden, siehe [unten](#schedule-syntax). Die nächsten drei Zeitpunkte werden unter dem Feld angezeigt. |
| **Geplante Sicherungen aufbewahren** | Wie viele geplante Sicherungen behalten werden; ältere werden gelöscht. `0` behält alle. Manuelle und hochgeladene Sicherungen werden nie gelöscht. |

Klicken Sie auf **Zeitplan speichern**, um ihn anzuwenden. Die Statuszeile zeigt, wann die letzte geplante Sicherung erstellt wurde und wann die nächste fällig ist.

Wenn Marginalia zu einem geplanten Zeitpunkt nicht lief – typisch für die Desktop-App, die nur läuft, während Sie schreiben –, holt es die verpasste Sicherung etwa 30 Sekunden nach dem nächsten Start nach.

<a id="schedule-syntax"></a>

### Syntax des Zeitplans

Der Zeitplan ist ein Cron-Ausdruck mit fünf Feldern, `Minute Stunde Tag-des-Monats Monat Wochentag`:

| Ausdruck | Läuft |
|---|---|
| `0 3 * * *` | jeden Tag um 3:00 |
| `0 */6 * * *` | alle 6 Stunden |
| `30 2 * * 1` | montags um 2:30 (Wochentag: `0` oder `7` ist Sonntag, `1` Montag ...) |
| `0 12 1 * *` | am 1. jedes Monats um 12:00 |
| `@daily`, `@weekly`, `@monthly` | einmal täglich / wöchentlich / monatlich um Mitternacht (`@hourly` und `@yearly` funktionieren ebenfalls) |

Die Zeiten beziehen sich auf die Zeitzone des Servers (bei Docker setzen Sie sie mit der Umgebungsvariable `TZ`).

!!!
Um geplante Sicherungen auszuschalten, deaktivieren Sie *Sicherungen automatisch erstellen* und klicken Sie auf *Zeitplan speichern*. Der Ausdruck bleibt erhalten, sodass Sie die Sicherungen später wieder einschalten können. Er wird weiterhin geprüft – ein ungültiger Ausdruck wird nie gespeichert –, aber während die Sicherungen aus sind, darf das Feld auch leer sein.
!!!

<a id="restoring"></a>

## Wiederherstellen

Eine Datenbank kann nicht ersetzt werden, während Marginalia sie verwendet. Deshalb geschieht eine Wiederherstellung beim **nächsten Start**:

1. Klicken Sie neben der Sicherung auf **Beim Neustart wiederherstellen** und bestätigen Sie.
   Der Reiter zeigt *Datenbankwiederherstellung ist geplant. Starten Sie die Anwendung neu, um sie anzuwenden.* – **Wiederherstellung abbrechen** macht dies rückgängig.
2. Starten Sie Marginalia neu: Beenden und starten Sie die [Desktop-App](../getting-started/desktop-app.md) oder führen Sie auf dem [Server](../getting-started/docker-server.md) `docker compose restart` aus.

Beim Start wird die aktuelle Datenbank nach `db-backups/pre-restore-<time>-marginalia.sqlite` verschoben – sie erscheint in der Liste, sodass eine Wiederherstellung durch Wiederherstellen dieser Datei rückgängig gemacht werden kann – und die Sicherung nimmt ihren Platz ein. Eine Sicherung aus einer älteren Marginalia-Version wird automatisch aktualisiert.

Die Sicherung wird vor dem Einsetzen erneut geprüft (dieselben Prüfungen wie bei *Sicherung hochladen*; auch *Beim Neustart wiederherstellen* lehnt eine Sicherung ab, die diese Prüfungen nicht besteht). Besteht sie die Prüfung nicht oder schlägt ihre Aktualisierung fehl, behält Marginalia die aktuelle Datenbank (oder stellt sie wieder her) und startet normal; die abgelehnte Datei wird nach `db-backups/rejected-restore-<time>-marginalia.sqlite` verschoben, und das Protokoll nennt den Grund.

Alles nach der Sicherung geht verloren: Bücher, Teile und Einstellungen, die seitdem angelegt wurden. Angemeldete Benutzer müssen sich erneut anmelden, falls sich ihr Konto geändert hat.

<a id="copy-before-an-upgrade"></a>

## Kopie vor einem Upgrade

Wenn eine neue Version von Marginalia die Struktur einer bestehenden Datenbank ändern wird, speichert sie zuerst eine Kopie als `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, wobei die Version jene ist, auf der die Datenbank zuvor stand. Die Kopie steht in der Liste der Sicherungen, sodass ein Upgrade rückgängig gemacht werden kann, indem Sie die alte Marginalia-Version installieren und diese Kopie [wiederherstellen](#restoring). Die letzten drei Kopien werden aufbewahrt. Eine neue Installation hat nichts zu kopieren, und wenn die Kopie nicht angelegt werden kann (etwa weil die Festplatte voll ist), vermerkt das Protokoll dies und das Upgrade läuft weiter.

Eine Migration, die auf halbem Weg fehlschlägt, wird automatisch zurückgerollt, und Marginalia startet nicht; die Kopie dient einem Upgrade, das zwar durchlief, aber etwas bewirkt hat, was Sie nicht wollen.

<a id="moving-to-another-computer"></a>

## Auf einen anderen Computer umziehen

1. Erstellen Sie eine Sicherung und **laden** Sie sie **herunter**.
2. Installieren Sie Marginalia auf dem neuen Computer und legen Sie beim ersten Start einen temporären Administrator an.
3. **Sicherung hochladen**, **Beim Neustart wiederherstellen**, neu starten.
4. Melden Sie sich mit den Konten aus der Sicherung an.

Kopieren Sie außerdem den Ordner `data` (Buchsicherungen und die Bilder der Teile) und den Ordner `extensions` aus dem alten [Datenordner](index.md#the-data-folder). Oder beenden Sie Marginalia einfach und kopieren Sie den gesamten Ordner `.marginalia`.
