---
label: Papierkorb
order: 955
icon: trash
---

# Papierkorb

Löschen in Marginalia zerstört nicht sofort etwas: Ein gelöschtes Buch, Lorebook, Story-Teil oder Inferenzanbieter wird
nur als gelöscht markiert und aus deinen Listen ausgeblendet. Der Tab **Papierkorb** listet diese gelöschten Objekte auf
und holt sie zurück.

Gelöschte Objekte bleiben im Papierkorb, bis ein Administrator die [Bereinigung](administration/cleanup.md) ausführt,
die sie endgültig aus der Datenbank entfernt. Was die Bereinigung entfernt hat, kann nicht mehr wiederhergestellt werden
(nur ein [Datenbank-Backup](administration/database-backups.md), das vorher angelegt wurde, enthält es noch).

![Der Tab Papierkorb](../images/trash-tab.png)

Die Tabelle lädt beim Scrollen nach. Die zuletzt gelöschten Objekte stehen oben.

| Spalte | |
|---|---|
| (Kontrollkästchen) | Wählt das Objekt zum Wiederherstellen aus. Das Kästchen in der Kopfzeile wählt alle Objekte aus. |
| **Typ** | Die Art des Objekts: Buch, Lorebook, Lorebook-Eintrag, Nachricht (ein Story-Teil), Version eines Teils (ein gelöschter Eintrag der [Versionshistorie](books/version-history.md#deleting-versions); wenn du ihn wiederherstellst, wird er aus dem Papierkorb der Historie herausgenommen), Zusammenfassung, Modell (Inferenzanbieter), Protokoll, Tag, Agent, Agent-Lauf, Ressource... |
| **Name** | Der Name des Objekts. Story-Teile und Tag-Verknüpfungen haben keinen Namen und zeigen ihre ID; mit *Erweiterter Inhalt* liest du einen Story-Teil. |
| **Benutzer** | Wem das Objekt gehört. Nur Administratoren sehen diese Spalte. |
| **Gelöscht** | Wann das Objekt gelöscht wurde. |
| **Erweiterter Inhalt** | Öffnet die gespeicherten Daten des Objekts, siehe [unten](#extended-content). |

Über der Tabelle zeigt **Typ** nur eine Art von Objekt an, **Aktualisieren** lädt die Tabelle erneut.

<a id="restoring"></a>

## Wiederherstellen

Wähle die Objekte aus, die du zurückholen willst, und klicke auf **Ausgewählte wiederherstellen**. Marginalia fragt nach,
bevor es sie wiederherstellt. Die Objekte sind danach wieder in ihren Listen, genau so, wie sie gelöscht wurden, und die
anderen Tabs werden aktualisiert.

<a id="objects-that-belong-to-other-objects"></a>

### Objekte, die zu anderen Objekten gehören

Wenn du ein Objekt löschst, werden auch alle Objekte gelöscht, die zu ihm gehören, und beim Wiederherstellen kommen sie
mit zurück: Ein Buch nimmt seine Story-Teile, deren Zusammenfassungen und seine Tags mit, ein Lorebook seine Einträge,
ein Story-Teil seine Zusammenfassung und die Teile darunter, ein Inferenzanbieter seine Nutzungszahlen. Du musst sie
nicht auswählen: Stelle das Buch wieder her, dann kommt seine Geschichte mit. Sie erscheinen in der Tabelle als eigene
Zeilen (ein Buch mit einer langen Geschichte füllt die Tabelle mit seinen Teilen). Einen dieser Einträge allein
wiederherzustellen funktioniert nur, wenn das Objekt, zu dem er gehört, nicht gelöscht ist. Etwas, das du früher allein
gelöscht hast (ein Eintrag, ein Story-Teil), kommt auch mit seinem Lorebook oder Buch zurück, denn Marginalia kann es
nicht von dem unterscheiden, was zusammen mit ihm gelöscht wurde.

Einige Objekte können ohne ein anderes Objekt nicht bestehen:

- ein Eintrag braucht sein Lorebook,
- ein Story-Teil und eine Zusammenfassung brauchen ihr Buch,
- ein Buch braucht sein Modell (Inferenzanbieter), sein Protokoll und sein Lorebook, solange es sie verwendet,
- eine Tag-Verknüpfung braucht ihr Tag.

Ein Objekt, dessen Lorebook, Buch ... gelöscht ist, **kann nur zusammen mit ihm wiederhergestellt werden**: Wähle beide aus.
Wählst du nur den Eintrag aus, stellt Marginalia **nichts** wieder her und sagt dir, was fehlt:

> Nichts wurde wiederhergestellt. Diese Objekte brauchen ein gelöschtes Objekt, das nicht ausgewählt ist. Wähle es ebenfalls aus oder lass die Objekte gelöscht.
> Lorebook-Eintrag "mosh" braucht das gelöschte Lorebook "Phrasebook" (Lorebook)

![Wiederherstellen abgelehnt, weil das Lorebook des Eintrags ebenfalls gelöscht ist](../images/trash-restore-blocked.png)

Wähle das Objekt aus, das genannt wird, und stelle erneut wieder her. Objekte, die nicht mehr gelöscht sind (inzwischen
wiederhergestellt oder ein Objekt, das du nie gelöscht hast), sind kein Problem.

!!!info Ein wiederhergestellter Story-Teil
Ein Story-Teil, der aus der Mitte einer Geschichte gelöscht wurde, hat seine nachfolgenden Teile zu seinem übergeordneten
Teil verschoben. Wenn du ihn wiederherstellst, kommt er als neuer Zweig unter seinen übergeordneten Teil zurück, **ohne**
die Teile, die auf ihn folgten: Sie bleiben dort, wohin sie verschoben wurden. Stelle lieber ein ganzes Buch wieder her
statt einzelner Teile, wenn du die Geschichte so haben willst, wie sie war.
!!!

<a id="deleting-for-good"></a>

## Endgültig löschen

Auf die Bereinigung zu warten ist nicht immer das, was du willst: Ein Story-Teil oder ein Lorebook-Eintrag, den du nie
wieder brauchst, kann sofort entfernt werden. Wähle ihn aus und klicke auf **Endgültig löschen**; Marginalia fragt zuerst
nach, und **das kann nicht rückgängig gemacht werden**.

![Einen Story-Teil endgültig löschen](../images/trash-delete-permanently.png)

- Nur **Story-Teile** und **Lorebook-Einträge** können endgültig gelöscht werden (mit ihren Zusammenfassungen und Tags).
  Die Schaltfläche ist deaktiviert, solange etwas anderes ausgewählt ist: Bücher, Lorebooks und der Rest warten auf die
  [Bereinigung](administration/cleanup.md) durch den Administrator, sodass du nicht versehentlich ein ganzes Buch entfernen
  kannst.
- Story-Teile, die unter einem entfernten Teil liegen, werden nicht mit ihm entfernt, egal ob sie gelöscht sind oder
  nicht: Sie werden zu seinem übergeordneten Teil verschoben, wie wenn du einen Teil im Editor löschst. War der Teil die
  Position, an der ein Buch geöffnet war, öffnet sich das Buch stattdessen am übergeordneten Teil.
- Marginalia löscht nichts, wenn etwas nicht entfernt werden kann (zum Beispiel weil eine Erweiterung das Objekt noch
  braucht), und sagt dir, welches Objekt das ist.

<a id="extended-content"></a>

### Erweiterter Inhalt

Marginalia speichert einen Großteil der Daten eines Objekts (den Text eines Story-Teils, die Einstellungen eines
Lorebook-Eintrags, Notizen, die [Erweiterungen](extensions/index.md) gespeichert haben) als JSON daneben. **Erweiterter
Inhalt** in einer Zeile zeigt sie an, damit du prüfen kannst, was ein Objekt enthält, bevor du es wiederherstellst, zum
Beispiel um den richtigen Story-Teil zu finden. Objekte ohne solche Daten (Ressourcen) haben keinen Link; ein Objekt, das
keine hat, zeigt *Dieses Objekt hat keinen erweiterten Inhalt.*

<a id="administrators"></a>

## Administratoren

Administratoren sehen die gelöschten Objekte **aller Benutzer** und können jedes davon wiederherstellen. Der Filter
**Benutzer** über der Tabelle zeigt die Objekte eines einzelnen Benutzers; die Spalte **Benutzer** zeigt, wem sie gehören.
Wiederhergestellte Objekte gehen zurück an ihren Eigentümer.

Dieselbe Tabelle findest du unter *Admin → Bereinigung* (**Papierkorb**), damit du siehst, was die nächste Bereinigung
entfernt. Objekte eines gelöschten Benutzers können nicht wiederhergestellt werden: Sie hängen vom Benutzer ab.
