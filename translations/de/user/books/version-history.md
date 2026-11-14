---
label: Versionsverlauf
order: 85
---

# Versionsverlauf eines Abschnitts

Jedes Mal, wenn sich der Text eines Abschnitts ändert, bewahrt Marginalia den Text, den er vorher hatte. Du kannst dir die früheren Versionen eines Abschnitts ansehen,
sehen, was sich zwischen ihnen geändert hat, eine davon wiederherstellen, einer Version einen Namen geben, damit sie nie verloren geht, und im alten Text suchen.

Ein Abschnitt wird geändert durch das Modell, das ihn schreibt (die erste Generierung, **Neu generieren**, **Variante**, [**Überarbeiten**](story-editor.md#redrafting-a-part)),
durch dich (**Bearbeiten** und **Speichern**), durch eine Massenaktion (**Suchen und Ersetzen**, ein Import, ein Befehl), durch einen
[Agenten](../agents.md), durch eine [Erweiterung](../extensions/index.md), durch das [Zusammenführen](story-editor.md#merging-a-part-into-the-previous-one)
zweier Abschnitte und durch das Zurücksetzen auf eine Version. Jede dieser Änderungen erzeugt eine **Version** mit Zeitpunkt und Urheber:

| Quelle | |
|---|---|
| **Generiert**, **Neu generiert**, **Überarbeitet** | Ein Modell hat den Text geschrieben. Die Version nennt Modell und Protokoll und behält die Begründung, die dazu gehörte. |
| **Autor** | Du hast den Text eingegeben. Eine Änderung durch einen Befehl, eine Massenaktion, einen Import oder einen Agenten vermerkt die Zeile der Version (ein Agent ist markiert). |
| **Erweiterung** | Eine Erweiterung hat den Text geändert. |
| **Zusammengeführt** | Der Text eines Abschnitts, der in diesen hinein zusammengeführt wurde. |
| **Wiederhergestellt** | Eine Version, die obenauf zurückgesetzt wurde, eine Kopie der Version, die sie nennt. |
| **Gespeicherte Version** | Eine Version, die du von Hand mit einem Namen gespeichert hast. |

Der Verlauf ist keine zweite Kopie des Buchs. Die Versionen werden getrennt von den Abschnitten gespeichert, sodass ein langer Verlauf die Geschichte
nicht langsamer macht, und ein Buch, das exportiert, veröffentlicht oder im Viewer gelesen wird, enthält nur den aktuellen Text.

<a id="looking-at-the-history"></a>

## Den Verlauf ansehen

Wähle **Verlauf** im Menü (☰) des Abschnitts. Die Zahl in Klammern ist die Anzahl der Versionen des Abschnitts. (Der Eintrag wird nicht angeboten, während der Text des Abschnitts bearbeitet wird, und auch nicht für einen Abschnitt, der noch nicht gespeichert ist.)

![Das Menü eines Abschnitts mit dem Eintrag Verlauf](../../images/history-part-menu.png)

![Der Verlauf eines Abschnitts: die Versionen links, die ausgewählte Version mit den Änderungen rechts](../../images/history-dialog.png)

Die Liste links zeigt die neueste Version oben. Jede Zeile enthält den Zeitpunkt, die Quelle, die Anzahl der Wörter und den
Namen der Version, falls sie einen hat; die neueste Version ist mit **Aktuell** markiert: Sie ist der Text, den der Abschnitt jetzt hat. Klicke auf eine Zeile,
um diese Version rechts zu sehen. Oberhalb des Textes wählst du, womit sie verglichen wird:

| | |
|---|---|
| **Vorherige Version** | Die Änderungen gegenüber der Version davor, Wort für Wort. |
| **Aktueller Text** | Die Änderungen gegenüber dem Text des Abschnitts jetzt. |
| **Nur Text** | Der Text, wie er war, ohne Vergleich. |

Eine Version, die ein Modell geschrieben hat, zeigt ihre Begründung in einem Block, den du aufklappen kannst. Eine **Wiederhergestellte** Version sagt, welche Version sie
als Kopie enthält.

Auf dem Handy füllt der Dialog den Bildschirm: Die Versionen sind Karten oberhalb der ausgewählten Version, und nichts benötigt die Maus.

<a id="putting-a-version-back"></a>

## Eine Version zurücksetzen

Es gibt zwei Wege, die sich darin unterscheiden, was mit den später entstandenen Versionen geschieht:

| | |
|---|---|
| **Obenauf wiederherstellen** | Der Text der gewählten Version wird wieder der Text des Abschnitts, als neue **Wiederhergestellte** Version ganz oben. Nichts geht verloren: Die dazwischenliegenden Versionen bleiben in der Liste. Verwende es, um eine frühere Formulierung zurückzuholen. |
| **Bis hierher zurücksetzen** | Der Abschnitt geht zur gewählten Version zurück, und **alle neueren Versionen wandern in den Papierkorb** des Verlaufs (du wirst gefragt und erfährst, wie viele es sind). Sie können aus dem Papierkorb wieder geholt werden. Verwende es, um eine ganze Reihe von Änderungen rückgängig zu machen. |

Beides ändert den Abschnitt so, wie es eine Bearbeitung tut: Die [Zusammenfassungen](summaries.md), die den alten Text beschreiben, werden veraltet, und
Marginalia sagt, wie viele es sind, und der Abschnitt gilt als bearbeitet. Die Geschichte zeigt den wiederhergestellten Text sofort an.

<a id="naming-a-version"></a>

## Eine Version benennen

Eine Version mit Namen ist eine **gespeicherte Version**, und der Verlauf entfernt sie nie von selbst.

- **Version speichern** im Dialog speichert den Text, den der Abschnitt jetzt hat, unter einem Namen, den du vergibst.
- **Bezeichnung …** gibt einer vorhandenen Version einen Namen oder ändert bzw. entfernt ihn.

Verwende es, bevor du etwas Riskantes ausprobierst: *der Entwurf, der dem Lektor gefiel*, *vor dem Schnitt des Duells*.

<a id="deleting-versions"></a>

## Versionen löschen

- **Löschen** verschiebt eine Version in den **Papierkorb des Verlaufs**. Setze **Gelöschte Versionen anzeigen**, um den Papierkorb zu sehen (die Zeilen sind ausgegraut); **Aus dem Papierkorb holen** setzt eine Version zurück an ihren Platz in der Liste, und **Endgültig löschen** entfernt eine für immer. Die neueste Version kann nicht gelöscht werden: Sie ist der Text des Abschnitts.
- **Papierkorb leeren** löscht alles im Papierkorb endgültig.
- **Verlauf löschen** löscht alle Versionen des Abschnitts außer der aktuellen, endgültig. Der Dialog sagt, wie viele Versionen wegfallen und wie viel Platz sie belegen. **Gespeicherte Versionen und die erste generierte Version behalten** (zu Beginn angehakt) behält die, die du benannt hast, und das Erste, was das Modell für den Abschnitt geschrieben hat.

![Was Verlauf löschen gleich entfernen wird](../../images/history-clear.png)

Das Einstellungsmenü (⚙) der Geschichte hat dasselbe für das ganze Buch: **Verlauf des Buchs löschen** und **Papierkorb des Verlaufs leeren**.

![Das Einstellungsmenü der Geschichte mit den Aktionen für den Verlauf des Buchs](../../images/history-menu.png)

<a id="how-many-versions-are-kept"></a>

## Wie viele Versionen aufbewahrt werden

Ein Abschnitt behält **50 Versionen**, sofern keine Grenze etwas anderes festlegt. Würde eine neue Version eine zu viel sein, entfernt der Verlauf zuerst die älteste Version im Papierkorb und danach die älteste Version, die nicht im Papierkorb liegt. Versionen im Papierkorb zählen zur Grenze, bis sie endgültig entfernt sind. Drei Versionen werden nie entfernt: die neueste, die benannten und das Erste, was das Modell für den Abschnitt generiert hat (auch sie zählen nicht zur Grenze).

Die Grenze lässt sich auf vier Ebenen festlegen, und **die strengste gewinnt**:

| Ebene | Wo |
|---|---|
| Das Buch | **Versionen je Abschnitt aufbewahrt** auf dem Reiter [Über](managing-books.md#the-about-tab) des Buchs. |
| Die Ordner | **Aufbewahrte Versionen je Abschnitt** in den [Einstellungen des Ordners](folders.md#folder-settings); die Ordner oberhalb des Buchs werden alle berücksichtigt. |
| Du | **Aufbewahrte Versionen je Abschnitt** im Reiter [Einstellungen](../account.md). |
| Der Administrator | Die [Berechtigung](../administration/permissions.md) **Versionsverlauf**, eine Obergrenze für den Benutzer. |

Ein leeres Feld bedeutet, dass diese Ebene keine Grenze setzt. **0** behält gar keinen Verlauf (die vorhandenen Versionen bleiben, bis du sie löschst, aber es kommt nichts Neues hinzu). Das Feld zeigt als Hinweis die Grenze, die es erbt, und welche Ebene sie festlegt, zum Beispiel *Auf 20 durch den Ordner „Romane“ begrenzt*. Ein Buch kann eine Grenze nicht über eine darüberliegende hinaus anheben.

<a id="searching-the-old-text"></a>

## Im alten Text suchen

Die üblichen Suchen betrachten nur den aktuellen Text, sodass ein alter Satz, den du geändert hast, keinen Abschnitt auftauchen lässt. Um ihn zu finden, setze **Versionen einbeziehen** (zu Beginn aus, und nicht gemerkt, damit dich eine Suche nie mit altem Text überrascht):

- in der [erweiterten Suche der Bücher](managing-books.md#advanced-search): Ein Buch wird gefunden, wenn eine frühere Version eines seiner Abschnitte die Wörter enthält;
- in der Suche der [Verzweigungsansicht](branches-and-story-tree.md#searching-all-branches): Ein Abschnitt, der die Wörter nur in einer früheren Version hat, wird markiert, der Treffer lautet *Gefunden in der Version vom 12.10.2026 14:05 (Generiert)*, und **Im Verlauf anzeigen** öffnet den Verlauf des Abschnitts an dieser Version.

![Ein Abschnitt, der über eine frühere Version gefunden wurde](../../images/history-search.png)

Ein Agent kann dasselbe mit dem Befehl `search-parts` und seiner Option `includeVersions` tun; jeder Treffer sagt dann, aus welcher Version er stammt. Versionen im Papierkorb werden nicht durchsucht, und **Suchen und Ersetzen** verändert nie eine Version.

<a id="backups-copies-and-the-trash"></a>

## Sicherungen, Kopien und der Papierkorb

- Eine [Buchsicherung](backups.md#the-history-of-the-parts) kann den Verlauf mitnehmen (**Versionsverlauf einbeziehen**, zu Beginn nicht angehakt, weil er groß werden kann). Das Wiederherstellen einer solchen Sicherung, als Buch oder als Kopie, bringt die Versionen mit ihren Namen und dem Papierkorb zurück. Eine Sicherung ohne ihn gibt jedem Abschnitt eine erste Version.
- Ein [Duplikat eines Buchs](managing-books.md#cloning-a-book) beginnt mit einem eigenen Verlauf: eine Version je Abschnitt, aus dem Text, mit dem es kopiert wurde.
- Das Löschen eines Abschnitts oder eines Buchs nimmt dessen Versionen mit; sie liegen im [Papierkorb](../trash.md), solange der Abschnitt dort liegt, und werden mit ihm endgültig gelöscht.
- Wenn ein Abschnitt in den vorherigen zusammengeführt wird, wird der Verlauf des zusammengeführten Abschnitts entfernt; der Text erscheint im Verlauf des verbleibenden Abschnitts als **Zusammengeführt**-Version.
<a id="books-from-before-the-history"></a>

## Bücher aus der Zeit vor dem Verlauf

Als Marginalia aktualisiert wurde, erhielt jeder Teil, der bereits existierte, seine ersten Fassungen: Ein Teil, den ein Modell geschrieben hat, beginnt mit einer **Generiert**-Fassung des Textes, so wie das Modell ihn geschrieben hat (mit seiner Begründung und dem verwendeten Modell), und hat eine **Autor**-Fassung, wenn du ihn seitdem bearbeitet hast; jeder andere Teil beginnt mit einer **Autor**-Fassung. Die früheren Bearbeitungen dieser Teile sind nicht bekannt und stehen nicht in der Liste.
