---
label: Bereinigung
order: 80
---

# Bereinigung

Wenn Benutzer in Marginalia etwas löschen, etwa ein Buch, einen Story-Teil, ein Lorebook oder einen Inferenz-Anbieter, verschwindet es aus ihren Listen, bleibt aber in der Datenbank. Dadurch ist Löschen gefahrlos, und Verweise bleiben intakt (ein Buch kennt weiterhin den Anbieter, der seine Teile geschrieben hat). Bis dahin können Benutzer gelöschte Objekte im Tab [Trash](../trash.md) wiederherstellen. **Cleanup** entfernt solche gelöschten Daten endgültig, damit die Datenbank klein bleibt.

![Der Tab Cleanup](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Eine Bereinigung ausführen

!!!warning
Eine Bereinigung kann nicht rückgängig gemacht werden. Was sie entfernt, ist auch aus dem [Trash](../trash.md) verschwunden. [Erstelle zuerst ein Datenbank-Backup](database-backups.md).
!!!

1. Öffne den Tab **Cleanup** der Admin-Seite und klicke auf **Analyze**. Noch wird nichts verändert.
2. Prüfe das Ergebnis (siehe unten).
3. Klicke auf **Run Cleanup** und bestätige *n objects will be permanently removed*.

Die Bereinigung betrifft die Daten aller Benutzer.

Die Bereinigung löscht Einträge aus der Datenbank und nur die Dateien von [Agents](../agents.md): den Arbeitsbereich eines bereinigten Agents und den Ordner eines bereinigten Laufs. Die Dateien gelöschter und nicht genutzter [Ressourcen](../resources.md) (Bilder der Teile, Audio) bleiben im Datenordner ihres Besitzers, bis du sie im Tab [Resource Files](resource-files.md) entfernst.

<a id="seeing-what-is-deleted"></a>

## Gelöschtes einsehen

**Trash** unterhalb der Tabellen listet die gelöschten Objekte aller Benutzer auf, eine Zeile pro Objekt (öffne den Bereich, um die Tabelle zu laden). So siehst du, was eine Bereinigung entfernen würde, und kannst wiederherstellen, was jemand zurückverlangt hat. Es ist dieselbe Tabelle wie im Tab [Trash](../trash.md), einschließlich der Filter nach Benutzer und Typ und des Links *Extended Content*.

![Der Abschnitt Trash des Tabs Cleanup](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Die Analyse lesen

Die erste Tabelle hat eine Zeile pro Datenart:

| Spalte | |
|---|---|
| **Entity** | Die Datenart: `Manuscript` (Bücher), `ChatMessage` (Story-Teile), `Lorebook`, `LorebookEntry`, `AI` (Inferenz-Anbieter), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Deleted** | Wie viele gelöscht sind. |
| **To purge** | Wie viele durch *Run Cleanup* entfernt werden, einschließlich Daten, die zu gelöschten Objekten gehören. |
| **Blocked** | Gelöscht, aber noch in Verwendung, daher bleiben sie erhalten. |

**Deleted objects still in use** listet die blockierten Objekte mit dem Namen und **Referenced by**, also was sie noch verwendet.

<a id="what-is-removed"></a>

## Was entfernt wird

Die Bereinigung entfernt gelöschte Objekte, die nichts Lebendiges mehr verwendet, zusammen mit den Daten, die nur zu ihnen gehören:

| Gelöscht | Mitentfernt |
|---|---|
| ein Buch | alle seine Story-Teile, Zusammenfassungen und Tag-Verknüpfungen |
| ein Lorebook | seine Einträge und Tag-Verknüpfungen; andere Lorebooks binden es nicht mehr als Sub-Lorebook ein |
| ein Story-Teil | der Teil (seine Kinder wurden bereits zu seinem Elternteil verschoben) und seine Zusammenfassung |
| ein Inferenz-Anbieter oder Protokoll | aus den Standardeinstellungen der Benutzer in *Settings* entfernt |
| ein Tag | seine Verknüpfungen zu Büchern und Einträgen |
| ein Benutzer | alles, was ihm gehörte: Bücher (mit ihren Story-Teilen und Zusammenfassungen), Lorebooks, Inferenz-Anbieter, Protokolle, Tags und Einstellungen |

Die Bereinigung entfernt außerdem die [Ressourcen](../resources.md), die nichts mehr verwendet, obwohl niemand sie gelöscht hat: Dateien, die einem Objekt hinzugefügt wurden (das Audio eines [Hörbuchs](../extensions/audiobook.md), ein Bild eines Teils), wenn dessen Objekt nicht mehr in der Datenbank ist oder durch diese Bereinigung entfernt wird und kein anderes Objekt sie ebenfalls nennt. Ein aus dem Teil erstellter Zweig hat dieselben Bilder und Clips, daher bleiben sie, solange der Zweig existiert. Ebenso bleiben die Dateien, die ein gelöschtes Objekt im Papierkorb nennt, erhalten. Sie zählen unter **To purge** von `Resource`, nicht unter *Deleted*. Eine Datei, die von keinem Objekt verwendet wird (*nothing* im Tab Resources), bleibt erhalten.

<a id="what-is-kept"></a>

## Was erhalten bleibt

Ein gelöschtes Objekt ist **blockiert**, also erhalten, solange noch etwas Lebendiges es verwendet:

| Blockiert | Weil | Zum Entsperren |
|---|---|---|
| ein gelöschter Inferenz-Anbieter oder ein Protokoll | ein Buch verwendet ihn noch als Modell oder Protokoll | wähle im Tab *About* des Buchs einen anderen aus oder lösche das Buch |
| ein gelöschtes Lorebook (mit seinen Einträgen) | ein Buch verwendet es noch | wähle im Buch ein anderes Lorebook aus oder leere das Feld |
| ein gelöschter Story-Teil | er ist der letzte Teil des aktiven Zweigs eines Buchs | wechsle das Buch zu einem anderen Zweig oder lösche das Buch |
| ein gelöschter Benutzer | etwas eines anderen Benutzers verwendet noch dessen Daten (ein Buch, das deren Inferenz-Anbieter oder Lorebook nutzt) | ändere oder lösche das Buch des anderen Benutzers |

Führe nach dem Entsperren *Analyze* erneut aus.

<a id="reference-model"></a>

## Referenzmodell

**Reference Model** unten listet jeden Verweis zwischen den Datenarten und seine Regel auf. Das ist nützlich, wenn du verstehen willst, warum etwas blockiert ist:

| Regel | Bedeutung |
|---|---|
| **Blocks purge** | Ein gelöschtes Ziel bleibt erhalten, solange ein lebendes Objekt darauf verweist. |
| **Purged with target** | Das verweisende Objekt gehört zum Ziel und wird mit ihm entfernt (Einträge mit ihrem Lorebook). |
| **Owns target** | Das Ziel gehört dem verweisenden Objekt (eine Zusammenfassung zu ihrem Teil). |
| **Cleared on purge** | Der Verweis wird entfernt, wenn das Ziel entfernt wird (Sub-Lorebooks, Standardmodell). |

Erweiterungen können mit eigenen Regeln an der Bereinigung teilnehmen.
