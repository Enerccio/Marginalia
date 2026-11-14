---
label: Datenbanksicherungen
order: 90
---

# Datenbanksicherungen

Eine Datenbanksicherung ist eine vollständige Kopie der Marginalia-Datenbank: alle Benutzer mit all ihren Büchern, Lorebooks, Einstellungen und Erweiterungsdaten. Mit ihr schützt du die Installation vor Fehlern und Ausfällen und kannst sie auf einen anderen Computer umziehen. Um ein einzelnes Buch zu sichern, gibt es [Buchsicherungen](../books/backups.md).

![Der Reiter Datenbanksicherungen](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Eine Sicherung erstellen

Klicke auf dem Reiter **Datenbanksicherungen** auf **Sicherung erstellen**. Die Kopie wird angefertigt, während Marginalia läuft, und ist auch dann konsistent, wenn Leute gerade schreiben. Sie erscheint in der Liste mit ihrem **Namen**, dem Datum **Erstellt**, dem **Typ** (*Manuell* oder *Geplant*) und der **Größe**. Die Werkzeugleiste zeigt die aktuelle Größe der Datenbank an.

Jede Sicherung hat:

| Schaltfläche | |
|---|---|
| **Herunterladen** | Lädt die Sicherungsdatei herunter (`.sqlite`) – bewahre eine Kopie außerhalb des Servers auf. |
| **Beim Neustart wiederherstellen** | Stellt die Sicherung wieder her, siehe [unten](#restoring). |
| Papierkorb | Löscht die Sicherung. |

**Sicherung hochladen** fügt eine Sicherungsdatei (`.sqlite` oder `.db`) zur Liste hinzu, etwa eine, die von einer anderen Installation heruntergeladen wurde. Die Datei wird zuerst geprüft und abgelehnt, mit *Die Datenbank kann nicht wiederhergestellt werden: ...* und dem Grund, wenn sie keine SQLite-Datenbank ist, beschädigt ist, keine Marginalia-Datenbank ist (die Datenbank einer anderen Anwendung) oder von einer neueren Marginalia-Version stammt als der laufenden.

!!!warning Sicherungen enthalten Geheimnisse
Eine Datenbanksicherung enthält alles, auch die Passwort-Hashes aller Benutzer und ihre API-Schlüssel. Die API-Schlüssel sind mit der Datei `secret.key` im [Datenordner](index.md#the-data-folder) verschlüsselt, die nicht Teil der Sicherung ist – wer aber sowohl die Sicherung als auch `secret.key` besitzt, kann sie lesen. Bewahre heruntergeladene Sicherungen genauso sicher auf wie den Server selbst.
!!!

Die Dateien werden im Ordner `db-backups` des [Datenordners](index.md#the-data-folder) gespeichert. Kopien, die Marginalia selbst anlegt – `pre-restore-...` vor einer [Wiederherstellung](#restoring) und `pre-migration-...` vor einem [Upgrade](#copy-before-an-upgrade) – stehen ebenfalls in der Liste, angezeigt als *Manuell*, sodass sie nie von der Rotation der geplanten Sicherungen gelöscht werden. Buchsicherungen und Erweiterungs-JARs sind separate Dateien und gehören nicht zu einer Datenbanksicherung.

**Hochgeladene Dateien (die [Bilder der Teile](../books/story-editor.md#images)) sind ebenfalls nicht Teil einer Datenbanksicherung.** Die Datenbank kennt sie nur als Einträge im Reiter [Ressourcen](../resources.md); die Dateien liegen im Ordner `data/<login>/images` des Datenordners. Das ist in Ordnung: Nach einer Wiederherstellung sind die Einträge die Stände von der Zeit der Sicherung, und die Bilder, die sie verwenden, sind weiterhin im Ordner vorhanden. Dateien von Bildern, die nach der Sicherung hinzugefügt wurden, bleiben einfach übrig (nichts verweist auf sie) und belegen Speicherplatz. Um die Bilder zu schützen, nimm den gesamten Datenordner in deine eigenen Sicherungen auf oder packe sie in eine [Buchsicherung](../books/backups.md#pictures-in-a-backup).

Wenn du eine Sicherung auf einer **anderen Installation** wiederherstellst, kopiere zusätzlich `secret.key` von der ursprünglichen Installation (bei gestopptem Marginalia). Andernfalls können die gespeicherten API-Schlüssel nicht entschlüsselt werden, und jeder Benutzer muss sie erneut eingeben.

<a id="scheduled-backups"></a>

## Geplante Sicherungen

Der Abschnitt **Geplante Sicherungen** erstellt Sicherungen automatisch:

| Feld | |
|---|---|
| **Sicherungen automatisch erstellen** | Schaltet geplante Sicherungen ein. |
| **Zeitplan (Cron)** | Wann sie erstellt werden, siehe [unten](#schedule-syntax). Die nächsten drei Zeitpunkte werden unter dem Feld angezeigt. |
| **Geplante Sicherungen aufbewahren** | Wie viele geplante Sicherungen behalten werden; ältere werden gelöscht. `0` behält alle. Manuelle und hochgeladene Sicherungen werden nie gelöscht. |

Klicke auf **Zeitplan speichern**, um ihn zu übernehmen. Die Statuszeile zeigt, wann die letzte geplante Sicherung erstellt wurde und wann die nächste fällig ist.

Wenn Marginalia zu einem geplanten Zeitpunkt nicht lief – typisch für die Desktop-App, die nur läuft, während du schreibst –, holt es die verpasste Sicherung etwa 30 Sekunden nach dem nächsten Start nach.

<a id="schedule-syntax"></a>

### Syntax des Zeitplans

Der Zeitplan ist ein Cron-Ausdruck mit fünf Feldern, `minute hour day-of-month month day-of-week`:

| Ausdruck | Läuft |
|---|---|
| `0 3 * * *` | jeden Tag um 3:00 |
| `0 */6 * * *` | alle 6 Stunden |
| `30 2 * * 1` | montags um 2:30 (Wochentag: `0` oder `7` ist Sonntag, `1` Montag ...) |
| `0 12 1 * *` | am 1. jeden Monats um 12:00 |
| `@daily`, `@weekly`, `@monthly` | einmal am Tag / in der Woche / im Monat um Mitternacht (`@hourly`, `@yearly` funktionieren ebenfalls) |

Die Zeiten richten sich nach der Zeitzone des Servers (in Docker setzt du sie mit der Umgebungsvariable `TZ`).

!!!
Um geplante Sicherungen auszuschalten, entferne den Haken bei *Sicherungen automatisch erstellen* und klicke auf *Zeitplan speichern*. Der Ausdruck bleibt erhalten, sodass du die Sicherungen später wieder einschalten kannst. Er wird weiterhin geprüft – ein ungültiger Ausdruck wird nie gespeichert –, aber solange die Sicherungen aus sind, kann das Feld auch leer sein.
!!!

<a id="restoring"></a>

## Wiederherstellen

Eine Datenbank kann nicht ersetzt werden, solange Marginalia sie verwendet. Deshalb geschieht eine Wiederherstellung beim **nächsten Start**:

1. Klicke neben der Sicherung auf **Beim Neustart wiederherstellen** und bestätige. Der Reiter zeigt *Datenbankwiederherstellung ist geplant. Starte die Anwendung neu, um sie anzuwenden.* – **Wiederherstellung abbrechen** macht dies rückgängig.
2. Starte Marginalia neu: Beende und starte die [Desktop-App](../getting-started/desktop-app.md) oder führe `docker compose restart` auf dem [Server](../getting-started/docker-server.md) aus.

Beim Start wird die aktuelle Datenbank nach `db-backups/pre-restore-<time>-marginalia.sqlite` verschoben – sie erscheint in der Liste, sodass eine Wiederherstellung rückgängig gemacht werden kann, indem diese Datei wiederhergestellt wird – und die Sicherung nimmt ihren Platz ein. Eine Sicherung aus einer älteren Marginalia-Version wird automatisch aktualisiert.

Die Sicherung wird vor dem Einsetzen erneut geprüft (dieselben Prüfungen wie bei *Sicherung hochladen*; auch *Beim Neustart wiederherstellen* lehnt eine Sicherung ab, die diese Prüfungen nicht besteht). Wenn sie die Prüfung nicht besteht oder ihre Aktualisierung fehlschlägt, behält Marginalia die aktuelle Datenbank (oder stellt sie wieder her) und startet normal; die abgelehnte Datei wird nach `db-backups/rejected-restore-<time>-marginalia.sqlite` verschoben, und das Protokoll nennt den Grund.

Alles, was nach der Sicherung entstanden ist, geht verloren: Bücher, Teile und Einstellungen, die seitdem angelegt wurden. Angemeldete Benutzer müssen sich erneut anmelden, wenn sich ihr Konto geändert hat.

<a id="copy-before-an-upgrade"></a>

## Kopie vor einem Upgrade

Wenn eine neue Version von Marginalia die Struktur einer bestehenden Datenbank ändern wird, speichert sie zuerst eine Kopie als `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, wobei die Version diejenige ist, auf der sich die Datenbank befand. Die Kopie steht in der Liste der Sicherungen, sodass ein Upgrade rückgängig gemacht werden kann, indem die alte Marginalia-Version installiert und diese Kopie [wiederhergestellt](#restoring) wird. Die letzten drei Kopien werden aufbewahrt. Bei einer neuen Installation gibt es nichts zu kopieren; und wenn die Kopie nicht angelegt werden kann (etwa weil die Festplatte voll ist), vermerkt das Protokoll dies, und das Upgrade läuft weiter.

Eine Migration, die auf halbem Weg fehlschlägt, wird automatisch zurückgerollt, und Marginalia startet nicht. Die Kopie dient für ein Upgrade, das erfolgreich durchlief, aber etwas bewirkt hat, das du nicht willst.

<a id="moving-to-another-computer"></a>

## Auf einen anderen Computer umziehen

1. Erstelle eine Sicherung und **lade sie herunter**.
2. Installiere Marginalia auf dem neuen Computer und lege beim ersten Start einen temporären Administrator an.
3. **Sicherung hochladen**, **Beim Neustart wiederherstellen**, neu starten.
4. Melde dich mit den Konten aus der Sicherung an.

Kopiere außerdem den Ordner `data` (Buchsicherungen und die Bilder der Teile) und den Ordner `extensions` aus dem alten [Datenordner](index.md#the-data-folder). Oder stoppe Marginalia einfach und kopiere den gesamten Ordner `.marginalia`.
