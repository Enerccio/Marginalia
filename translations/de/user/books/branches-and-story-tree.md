---
label: Verzweigungen & Geschichtsbaum
order: 80
---

# Verzweigungen & Geschichtsbaum

Marginalia lässt dich nie eine Version wegwerfen. Jede Alternative, die du ausprobierst, wird als **Verzweigung** der Geschichte gespeichert, und
du kannst zu jeder davon zurückkehren.

<a id="how-the-story-tree-works"></a>

## So funktioniert der Geschichtsbaum

Jeder Teil setzt den Teil davor fort. Hat ein Teil mehr als eine Fortsetzung, teilt sich die Geschichte: Jeder Pfad vom
ersten Teil bis zu einem Ende ist eine Verzweigung.

```
#1 ─ #2 ─ #3 ─ #4 ─ #5          ← Verzweigung A
           └─ #4' ─ #5' ─ #6'   ← Verzweigung B (aktiv)
```

Die **aktive Verzweigung** ist diejenige, die der [Story-Editor](story-editor.md) anzeigt und fortsetzt. Nur ihre Teile werden an das
Modell gesendet; die anderen Verzweigungen beeinflussen die Geschichte nicht.

Es gibt drei Wege, eine neue Verzweigung zu erhalten:

| | Was passiert | Die alte Version |
|---|---|---|
| **Variante** (letzter Teil) | Das Modell schreibt eine weitere Version des letzten Teils. | Bleibt als eigene Verzweigung erhalten. |
| **Geschichte verzweigen** (beliebiger Teil) | Die Geschichte wird ab diesem Teil auf einer neuen Verzweigung fortgesetzt. | Die folgenden Teile bleiben auf der alten Verzweigung. |
| **Neu generieren** (letzter Teil) | Das Modell schreibt den letzten Teil an derselben Stelle noch einmal. | Wird ersetzt - es entsteht keine Verzweigung. |

*Variante* und *Neu generieren* stehen nur im Menü des letzten Teils; *Geschichte verzweigen* steht im Menü jedes Teils.

<a id="trying-another-version-of-the-last-part"></a>

## Eine andere Version des letzten Teils ausprobieren

Wenn dir der letzte Teil nicht gefällt, wähle im Menü dieses Teils **Variante**. Das Modell schreibt den Teil erneut aus denselben
[Details des Zugs](story-editor.md#turn-details), und die neue Version wird zum Ende der aktiven Verzweigung. Die vorherige
Version bleibt als eigene Verzweigung erhalten; du kannst sie über die [Zweigansicht](#the-branch-view) wieder aktivieren.

Um zu ändern, was vor einem neuen Versuch geschehen soll, öffne **Details des Zugs** des letzten Teils und bearbeite die Anweisungen.

Verwende **Neu generieren** nur, wenn du die alte Version nicht brauchst: Sie überschreibt den Teil.

<a id="branching-from-an-earlier-part"></a>

## Von einem früheren Teil aus verzweigen

Um die Geschichte von einem früheren Punkt aus in eine andere Richtung zu lenken, öffne das Menü dieses Teils und wähle
**Geschichte verzweigen**. Marginalia kopiert den Teil auf eine neue Verzweigung und macht ihn zum Ende der aktiven Verzweigung,
sodass der Story-Editor nun mit diesem Teil endet. Setze wie gewohnt mit **+** fort.

Die Teile, die danach kamen, gehen nicht verloren: Sie bleiben auf der alten Verzweigung.

[Zusammenfassungen](summaries.md) des kopierten Teils werden mit ihm kopiert.

<a id="the-branch-view"></a>

## Die Zweigansicht

Der Reiter **Zweigansicht** zeigt den gesamten Geschichtsbaum. Jeder Teil ist eine Karte mit Datum, Szenenort, anwesenden
Figuren und Anweisungen; fahre mit der Maus über eine Karte, um alle Details zu sehen.

![Der Geschichtsbaum](../../images/story-tree.png)

- Die Teile der **aktiven Verzweigung** sind hervorgehoben, und ihr letzter Teil ist markiert.
- **Klicke auf den letzten Teil einer anderen Verzweigung**, um diese Verzweigung zu aktivieren. Das Buch wechselt zum Reiter *Geschichte*
  und zeigt sie an. Nur die Enden von Verzweigungen lassen sich anklicken; um von einem Teil in der Mitte fortzufahren, verwende
  [Geschichte verzweigen](#branching-from-an-earlier-part).
- Lange Abschnitte ohne Verzweigung (mehr als 20 Teile) werden zu einem Block zusammengefasst, der die ersten zehn und die
  letzten zehn Teile zeigt. Klicke auf den Block, um alle Teile anzuzeigen.

<a id="searching-all-branches"></a>

### Alle Verzweigungen durchsuchen

Das Feld über dem Baum durchsucht den Text von **allen Verzweigungen** des Buches, nicht nur der aktiven. Gib ein, wonach du suchst, und
drücke **Enter** oder klicke auf **Suchen**; das Feld merkt sich deine letzten Suchen, wähle eine aus der Liste, um sie zu wiederholen. Das **x** im
Feld löscht die Suche.

- Ein Teil passt, wenn sein Text **jedes Wort** enthält, das du eingegeben hast, in beliebiger Reihenfolge: `Leuchtturm Schlüssel` findet die Teile, die
  beide Wörter enthalten. Setze Wörter in doppelte Anführungszeichen, um nach einer Wortgruppe zu suchen: `"Leuchtturm Schlüssel"`.
- Verwende `*` für beliebige Zeichen: `Leucht*` findet *Leuchtturm* und *Leuchtfeuer*. Andere Zeichen, einschließlich `%`, `_` und
  `\`, werden so gesucht, wie sie sind.
- Groß- und Kleinschreibung spielt keine Rolle. Bei Buchstaben mit Akzenten findet ein in Kleinbuchstaben eingegebenes Wort auch dasselbe Wort in
  GROSSBUCHSTABEN oder Großgeschrieben (`žluťoučký` findet *Žluťoučký* und *ŽLUŤOUČKÝ*).
- Durchsucht wird nur der Text der Teile, nicht die Anweisungen, der Szenenort oder das Lexikon. Gelöschte Teile
  werden nicht durchsucht.

![Die Zweigansicht mit einer Suche](../../images/story-tree-search.png)

Die Option **Versionen einbeziehen** neben dem Feld durchsucht außerdem die früheren [Versionen des Textes](version-history.md#searching-the-old-text)
der Teile; ein Teil, der die Wörter nur in einer alten Version enthält, wird ebenfalls hervorgehoben, gibt an, welche Version es ist, und hat neben
**In der Geschichte anzeigen** die Schaltfläche **In der Versionshistorie anzeigen**.

Jeder passende Teil wird orange **hervorgehoben** und zeigt den Text um den Treffer; die Zeile über dem Baum sagt,
wie viele Teile gefunden wurden. Lange Abschnitte, die sonst zusammengefasst würden, werden geöffnet, wenn sie einen Treffer enthalten. Die Suche
bleibt bestehen, bis du sie löschst oder das Fenster des Buches schließt, und die Hervorhebung wird aktualisiert, wenn du zu dem Reiter zurückkehrst.

Klicke bei einem hervorgehobenen Teil auf **In der Geschichte anzeigen**, um ihn zu lesen:

- Liegt der Teil auf der aktiven Verzweigung, wechselt das Buch zum Reiter *Geschichte* und scrollt zu ihm.
- Liegt er auf einer anderen Verzweigung, wird zuerst diese Verzweigung aktiv. Sie endet am nächstgelegenen Ende einer Verzweigung
  unterhalb des gefundenen Teils (das neuere, wenn es zwei gibt), sodass der Story-Editor die Geschichte bis zum gefundenen Teil und
  das, was darauf folgte, zeigt. Anschließend wechselt das Buch zum Reiter *Geschichte* und scrollt zu dem Teil, der kurz aufblinkt.

Das Wechseln der Verzweigung entspricht dem Klick auf das Ende einer Verzweigung im Baum: Nichts geht verloren, die vorherige Verzweigung bleibt
unverändert, und du kannst über den Baum zu ihr zurückkehren.

<a id="deleting-parts-and-branches"></a>

## Teile und Verzweigungen löschen

**Löschen** im Menü eines Teils entfernt nur diesen Teil. Die Teile danach rücken nach oben und setzen am Teil davor an. Um eine ganze
Verzweigung zu entfernen, lösche ihre Teile vom Ende her.

Die Wort- und Token-Zählungen im Reiter *Info* zeigen sowohl das gesamte Buch (alle Verzweigungen) als auch die aktive Verzweigung.
