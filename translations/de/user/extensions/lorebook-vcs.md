---
label: Lorebook VCS
order: 9
---

# Lorebook VCS

Versionsverlauf für [Lorebook-Einträge](../lorebooks/entries.md): Bewahre frühere Fassungen deines Wissens auf und wechsle
zwischen ihnen - zum Beispiel den Zustand der Welt vor und nach einem Krieg oder eine Figur zu verschiedenen Zeitpunkten
der Geschichte.

!!!
Lorebook VCS funktioniert im [Telefon-Layout](../phone.md#extensions): Das Panel steht unter der Zeile *Aktiviert* des Lorebooks, die Revisionen eines Eintrags stehen oben in seinem Formular.
!!!

!!!
Im [Vollbild-Editor](../lorebooks/lorebooks.md#editing-in-full-screen) steht die Zeile **Lorebook Revisions** über der Liste der
Einträge, und die Revisionsleiste eines Eintrags steht unter der Namenszeile seines Tabs. Wenn du eine Revision wechselst, wird sie sofort im Tab angezeigt.
!!!

Eine Revision ist eine Momentaufnahme des gesamten Eintrags: Name, Inhalt, Notiz, Aktiviert, Reihenfolge, Filter und Filtermodus, Einfügemodus, *Den Inhalt nicht nach anderen Einträgen durchsuchen*, Tags und Negativ-Tags. (Revisionen, die vor diesem Flag erstellt wurden, gelten als ausgeschaltet.)

![Die Revisionsleiste eines Eintrags](../../images/plugin-lorebookvcs.png)

## Entry revisions

Mit installierter Erweiterung beginnen die Details (⌄) jedes Lorebook-Eintrags mit einer Revisionsleiste:

```
Revision: 2 / 3            [−]  [‹]  [Rev 2 (2026-10-08 14:30:12) ▾]  [›]  [+]
Modified: 2026-10-08 14:30:12
```

Revisionen funktionieren wie Speicherplätze. Der Eintrag zeigt immer eine davon - die **aktuelle Revision** - und was du am Eintrag änderst, gehört zu dieser Revision. Die Generierung verwendet den Eintrag immer so, wie er ist, also sieht das Modell die aktuelle Revision.

| Steuerelement | |
|---|---|
| **+** | Speichert den Eintrag in seinem jetzigen Zustand als neue Revision und macht sie zur aktuellen. |
| **‹** / **›**, oder die Liste | Wechselt zur vorherigen / nächsten / ausgewählten Revision. Deine Änderungen bleiben in der Revision, die du verlässt; die andere wird in den Eintrag geladen. |
| **−** | Löscht die aktuelle Revision und wechselt zur vorherigen. Die letzte verbleibende Revision kann nicht gelöscht werden. |
| *n / total*, *Modified* | Welche Revision aktuell ist und wann sie zuletzt geändert wurde. |

Wenn die Details eines Eintrags zum ersten Mal geöffnet werden, wird sein aktueller Zustand zur Revision 1.

Ein typischer Ablauf:

1. Öffne den Eintrag einer Figur und klicke auf **+** - Revision 2 ist eine Kopie von Revision 1.
2. Ändere Revision 2 so, dass die Figur nach den Ereignissen von Kapitel 10 beschrieben wird.
3. Schreibe weiter. Wenn du zurückgehst, um Kapitel 5 zu bearbeiten, wechsle mit **‹** zu Revision 1; später wechselst du mit **›** wieder zurück.

## Lorebook revisions

Der Lorebook-Editor erhält eine Zeile **Lorebook Revisions** mit zwei Schaltflächen. Sie wirken auf das im Editor ausgewählte Lorebook:

| Schaltfläche | |
|---|---|
| export | Lädt den Verlauf aller Einträge des Lorebooks als `<lorebook>_vcs_history.json` herunter. |
| import | **Import Marginalia VCS JSON** - ein von dieser Erweiterung exportierter Verlauf; **Import SillyTavern VCS Extension JSON** - Verlauf aus der Lorebook-Versionsverlauf-Erweiterung von SillyTavern. |

Ein Import **ersetzt** den Verlauf des Lorebooks; wenn das Lorebook bereits einen Verlauf hat oder die Datei aus einem anderen Lorebook exportiert wurde, fragt der Import zuerst nach einer Bestätigung.

Ein Marginalia-Verlauf wird zunächst anhand der Einträge selbst den Einträgen zugeordnet. Einträge, die nicht gefunden werden - zum Beispiel wenn der Verlauf aus einer anderen Kopie des Lorebooks stammt - werden anhand von Name und Reihenfolge ihrer aktuellen Revision zugeordnet, oder nur anhand des Namens, wenn nur ein Eintrag diesen Namen hat. Verläufe, die keinem Eintrag zugeordnet werden können, werden übersprungen; die Bestätigung nennt ihre Anzahl.

SillyTavern-Verläufe werden den Einträgen anhand ihrer Position im Lorebook zugeordnet. Importiere sie daher in ein Lorebook, das [aus denselben Welt-Informationen importiert wurde](../lorebooks/import-export.md#importing-sillytavern-world-info), bevor du Einträge hinzufügst oder löschst. Die Auslöse-Schlüssel ihrer Revisionen werden zum Filter des Eintrags, auf dieselbe Weise, wie der [Import von Welt-Informationen](../lorebooks/import-export.md#importing-sillytavern-world-info) sie umwandelt.

## Storage

Der Verlauf wird zusammen mit dem Lorebook in der Datenbank gespeichert, daher enthalten [Datenbank-Backups](../administration/database-backups.md) ihn. Die Daten, die eine Erweiterung in einem Lorebook speichert, werden auch in [Lorebook-Exporte](../lorebooks/import-export.md#exporting) und in die Lorebooks von [Buch-Backups](../books/backups.md) sowie in die Kopien der Lorebooks eines Buchs geschrieben, mit denen [Agenten](../agents.md) arbeiten. Beim Import eines Lorebooks werden jedoch neue Einträge erstellt, und der Verlauf gehört zur Identität der alten Einträge, daher erscheint er bei ihnen nicht. Um den Verlauf in eine importierte Kopie zu übertragen, exportiere ihn aus dem Original und importiere ihn in die Kopie - die Einträge werden dann anhand von Name und Reihenfolge zugeordnet (siehe [Lorebook revisions](#lorebook-revisions)).
