---
label: Bereinigung
order: 80
---

# Bereinigung

Wenn Benutzer in Marginalia etwas löschen – ein Buch, einen Story-Teil, ein Lorebook oder einen Inferenz-Anbieter –, verschwindet es
aus ihren Listen, bleibt aber in der Datenbank erhalten. Dadurch ist Löschen gefahrlos, und Verweise funktionieren weiterhin (ein Buch
kennt weiterhin den Anbieter, der seine Teile geschrieben hat). Bis dahin können Benutzer ihre gelöschten Objekte im Tab
[Papierkorb](../trash.md) wiederherstellen. **Bereinigung** entfernt solche gelöschten Daten endgültig, damit die Datenbank klein bleibt.

![Der Tab Bereinigung](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Eine Bereinigung ausführen

!!!warning
Die Bereinigung kann nicht rückgängig gemacht werden. Was sie entfernt, ist auch im [Papierkorb](../trash.md) endgültig weg. [Erstelle zuerst eine Datenbanksicherung](database-backups.md).
!!!

1. Öffne den Tab **Bereinigung** der Administrationsseite und klicke auf **Analysieren**. Noch wird nichts verändert.
2. Prüfe das Ergebnis (siehe unten).
3. Klicke auf **Bereinigung ausführen** und bestätige *n Objekte werden endgültig entfernt*.

Die Bereinigung betrifft die Daten aller Benutzer.

Die Bereinigung löscht Einträge aus der Datenbank und nur die Dateien von [Agenten](../agents.md): den Arbeitsbereich eines entfernten
Agenten und den Ordner eines entfernten Laufs. Die Dateien gelöschter [Ressourcen](../resources.md) (Bilder der Teile) bleiben im
Datenordner ihres Besitzers, bis du sie im Tab [Ressourcendateien](resource-files.md) entfernst.

<a id="seeing-what-is-deleted"></a>

## Gelöschte Daten einsehen

**Papierkorb** unter den Tabellen listet die gelöschten Objekte aller Benutzer auf, eine Zeile pro Objekt (öffne ihn, um die Tabelle zu laden).
So siehst du, was eine Bereinigung entfernen würde, und kannst wiederherstellen, was jemand zurückhaben wollte. Es ist dieselbe Tabelle
wie im Tab [Papierkorb](../trash.md), einschließlich der Filter nach Benutzer und Typ sowie des Links *Erweiterter Inhalt*.

![Der Abschnitt Papierkorb im Tab Bereinigung](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Die Analyse lesen

Die erste Tabelle hat eine Zeile pro Datenart:

| Spalte | |
|---|---|
| **Entität** | Die Datenart: `Manuscript` (Bücher), `ChatMessage` (Story-Teile), `Lorebook`, `LorebookEntry`, `AI` (Inferenz-Anbieter), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Gelöscht** | Wie viele gelöscht sind. |
| **Zu entfernen** | Wie viele *Bereinigung ausführen* entfernen wird, einschließlich der Daten, die zu gelöschten Objekten gehören. |
| **Blockiert** | Gelöscht, werden aber noch verwendet, daher bleiben sie erhalten. |

**Gelöschte Objekte, die noch verwendet werden** listet die blockierten Objekte mit Namen und **Referenziert von** – also auf, was sie noch verwendet.

<a id="what-is-removed"></a>

## Was entfernt wird

Die Bereinigung entfernt gelöschte Objekte, die kein lebendes Objekt mehr verwendet, zusammen mit den Daten, die nur zu ihnen gehören:

| Gelöscht | Wird damit entfernt |
|---|---|
| ein Buch | alle seine Story-Teile, Zusammenfassungen und Tag-Verknüpfungen |
| ein Lorebook | seine Einträge und Tag-Verknüpfungen; andere Lorebooks nehmen es nicht mehr als Unter-Lorebook auf |
| ein Story-Teil | der Teil (seine Kinder wurden bereits an seinen Elternteil verschoben) und seine Zusammenfassung |
| ein Inferenz-Anbieter oder ein Protokoll | wird aus den Standardeinstellungen der Benutzer in *Einstellungen* entfernt |
| ein Tag | seine Verknüpfungen zu Büchern und Einträgen |
| ein Benutzer | alles, was ihm gehörte: Bücher (mit ihren Story-Teilen und Zusammenfassungen), Lorebooks, Inferenz-Anbieter, Protokolle, Tags und Einstellungen |

<a id="what-is-kept"></a>

## Was erhalten bleibt

Ein gelöschtes Objekt wird **blockiert** – also erhalten –, solange noch etwas Lebendes es verwendet:

| Blockiert | Weil | So hebst du die Blockierung auf |
|---|---|---|
| ein gelöschter Inferenz-Anbieter oder ein Protokoll | ein Buch es noch als Modell bzw. Protokoll verwendet | wähle im Tab *Info* des Buches einen anderen aus oder lösche das Buch |
| ein gelöschtes Lorebook (mit seinen Einträgen) | ein Buch es noch verwendet | wähle im Buch ein anderes Lorebook aus oder leere das Feld |
| ein gelöschter Story-Teil | er ist der letzte Teil des aktiven Zweigs eines Buches | wechsle das Buch zu einem anderen Zweig oder lösche das Buch |
| ein gelöschter Benutzer | etwas eines anderen Benutzers verwendet dessen Daten (ein Buch verwendet dessen Inferenz-Anbieter oder Lorebook) | ändere oder lösche das Buch des anderen Benutzers |

Führe *Analysieren* erneut aus, nachdem du die Blockierung aufgehoben hast.

<a id="reference-model"></a>

## Referenzmodell

**Referenzmodell** am Ende listet jeden Verweis zwischen den Datenarten und seine Regel auf – nützlich, wenn du verstehen willst, warum etwas blockiert ist:

| Regel | Bedeutung |
|---|---|
| **Blockiert die Entfernung** | Ein gelöschtes Ziel bleibt erhalten, solange ein lebendes Objekt darauf verweist. |
| **Wird mit dem Ziel entfernt** | Das verweisende Objekt gehört zum Ziel und wird mit ihm entfernt (Einträge mit ihrem Lorebook). |
| **Besitzt das Ziel** | Das Ziel gehört zum verweisenden Objekt (eine Zusammenfassung zu ihrem Teil). |
| **Wird beim Entfernen geleert** | Der Verweis wird entfernt, wenn das Ziel entfernt wird (Unter-Lorebooks, Standardmodell). |

Erweiterungen können mit eigenen Regeln an der Bereinigung teilnehmen.
