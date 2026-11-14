---
label: Verzweigungen & Geschichtsbaum
order: 80
---

# Verzweigungen & Geschichtsbaum

Marginalia verwirft nie eine Version. Jede Alternative, die Sie ausprobieren, bleibt als **Verzweigung** der Geschichte
erhalten, und Sie können zu jeder davon zurückkehren.

<a id="how-the-story-tree-works"></a>

## So funktioniert der Geschichtsbaum

Jeder Teil setzt den Teil davor fort. Hat ein Teil mehr als eine Fortsetzung, teilt sich die Geschichte: Jeder Pfad
vom ersten Teil bis zu einem Ende ist eine Verzweigung.

```
#1 ─ #2 ─ #3 ─ #4 ─ #5          ← branch A
           └─ #4' ─ #5' ─ #6'   ← branch B (active)
```

Die **aktive Verzweigung** ist diejenige, die der [Geschichteneditor](story-editor.md) anzeigt und fortsetzt. Nur ihre Teile werden
an das Modell gesendet; die anderen Verzweigungen beeinflussen die Geschichte nicht.

Es gibt drei Möglichkeiten, eine neue Verzweigung zu erzeugen:

| | Was passiert | Die alte Version |
|---|---|---|
| **Variante** (letzter Teil) | Das Modell schreibt eine weitere Version des letzten Teils. | Bleibt als eigene Verzweigung erhalten. |
| **Geschichte verzweigen** (beliebiger Teil) | Die Geschichte wird ab diesem Teil auf einer neuen Verzweigung fortgesetzt. | Die folgenden Teile bleiben auf der alten Verzweigung. |
| **Neu generieren** (letzter Teil) | Das Modell schreibt den letzten Teil erneut, an derselben Stelle. | Wird ersetzt - es entsteht keine Verzweigung. |

*Variante* und *Neu generieren* befinden sich nur im Menü des letzten Teils; *Geschichte verzweigen* befindet sich im Menü
jedes Teils.

<a id="trying-another-version-of-the-last-part"></a>

## Eine andere Version des letzten Teils ausprobieren

Wenn Ihnen der letzte Teil nicht gefällt, wählen Sie **Variante** in seinem Menü. Das Modell schreibt den Teil erneut aus
denselben [Details des Zugs](story-editor.md#turn-details), und die neue Version wird zum Ende der aktiven Verzweigung. Die bisherige
Version bleibt als eigene Verzweigung erhalten; Sie können in der [Zweigansicht](#the-branch-view) wieder zu ihr wechseln.

Um vor einem erneuten Versuch festzulegen, was geschehen soll, öffnen Sie **Details des Zugs** des letzten Teils und
bearbeiten die Anweisungen.

Verwenden Sie **Neu generieren** allein, wenn Sie die alte Version nicht brauchen: Der Teil wird dabei überschrieben.

<a id="branching-from-an-earlier-part"></a>

## Von einem früheren Teil aus verzweigen

Um die Geschichte von einem früheren Punkt aus in eine andere Richtung zu lenken, öffnen Sie das Menü dieses Teils und
wählen **Geschichte verzweigen**. Marginalia kopiert den Teil auf eine neue Verzweigung und macht ihn zum Ende der
aktiven Verzweigung, sodass der Geschichteneditor nun mit diesem Teil endet. Setzen Sie wie gewohnt mit **+** fort.

Die Teile, die danach kamen, gehen nicht verloren: Sie bleiben auf der alten Verzweigung.

[Zusammenfassungen](summaries.md) des kopierten Teils werden mit ihm kopiert.

<a id="the-branch-view"></a>

## Die Zweigansicht

Der Reiter **Zweigansicht** zeigt den gesamten Geschichtsbaum. Jeder Teil ist eine Karte mit seinem Datum, dem
Schauplatz, den anwesenden Figuren und den Anweisungen; fahren Sie mit der Maus über eine Karte, um alle Details zu sehen.

![Der Geschichtsbaum](../../images/story-tree.png)

- Die Teile der **aktiven Verzweigung** sind hervorgehoben, und ihr letzter Teil ist markiert.
- **Klicken Sie auf den letzten Teil einer anderen Verzweigung**, um diese Verzweigung zu aktivieren. Das Buch wechselt
  zum Reiter *Geschichte* und zeigt sie an. Nur Enden von Verzweigungen lassen sich anklicken; um von einem Teil in der
  Mitte fortzusetzen, verwenden Sie [Geschichte verzweigen](#branching-from-an-earlier-part).
- Lange Abschnitte ohne jede Verzweigung (mehr als 20 Teile) werden zu einem Block zusammengefasst, der die ersten und
  die letzten zehn Teile zeigt. Klicken Sie auf den Block, um alle Teile anzuzeigen.

<a id="searching-all-branches"></a>

### Alle Verzweigungen durchsuchen

Das Feld über dem Baum durchsucht den Text **aller Verzweigungen** des Buches, nicht nur der aktiven. Geben Sie ein, was
Sie suchen, und drücken Sie **Enter** oder klicken Sie auf **Suchen**; das Feld merkt sich Ihre letzten Suchen, wählen
Sie eine aus der Liste, um sie zu wiederholen. Das **x** im Feld löscht die Suche.

- Ein Teil passt, wenn sein Text **alle Wörter** enthält, die Sie eingegeben haben, in beliebiger Reihenfolge: `lighthouse key` findet
  die Teile, die beide Wörter enthalten. Setzen Sie Wörter in doppelte Anführungszeichen, um nach einer Wortgruppe zu
  suchen: `"lighthouse key"`.
- Verwenden Sie `*` für beliebige Zeichen: `light*` findet *lighthouse* und *lightning*. Andere Zeichen, einschließlich `%`,
  `_` und `\`, werden unverändert gesucht.
- Groß- und Kleinschreibung spielen keine Rolle. Bei Buchstaben mit Akzent findet ein in Kleinbuchstaben eingegebenes Wort
  auch dasselbe Wort in GROSSBUCHSTABEN oder Großschreibung am Anfang (`žluťoučký` findet *Žluťoučký* und *ŽLUŤOUČKÝ*).
- Durchsucht wird nur der Text der Teile, nicht die Anweisungen, der Schauplatz oder das Lorebook. Gelöschte Teile werden
  nicht durchsucht.

![Die Zweigansicht mit einer Suche](../../images/story-tree-search.png)

Das Kontrollkästchen **Versionen einbeziehen** neben dem Feld durchsucht außerdem die früheren [Versionen des Textes](version-history.md#searching-the-old-text)
der Teile; ein Teil, der die Wörter nur in einer alten Version enthält, wird ebenfalls hervorgehoben, nennt die Version
und hat neben **In der Geschichte anzeigen** die Schaltfläche **In der Historie anzeigen**.

Jeder passende Teil wird orange **hervorgehoben** und zeigt den Text rund um den Treffer; die Zeile über dem Baum nennt,
wie viele Teile gefunden wurden. Lange Abschnitte, die sonst zusammengefasst wären, werden geöffnet, wenn sie einen Treffer
enthalten. Die Suche bleibt bestehen, bis Sie sie löschen oder das Buchfenster schließen; die Hervorhebung wird aktualisiert,
wenn Sie zum Reiter zurückkehren.

Klicken Sie in einem hervorgehobenen Teil auf **In der Geschichte anzeigen**, um ihn zu lesen:

- Liegt der Teil auf der aktiven Verzweigung, wechselt das Buch zum Reiter *Geschichte* und scrollt zu ihm.
- Liegt er auf einer anderen Verzweigung, wird zuerst diese Verzweigung aktiviert. Sie endet am nächsten Ende einer
  Verzweigung unterhalb des gefundenen Teils (bei zwei Möglichkeiten die neuere), sodass der Geschichteneditor die
  Geschichte bis zum gefundenen Teil und das, was darauf folgte, zeigt. Anschließend wechselt das Buch zum Reiter
  *Geschichte* und scrollt zum Teil, der kurz aufblinkt.

Das Wechseln der Verzweigung entspricht dem Klick auf das Ende einer Verzweigung im Baum: Nichts geht verloren, die
bisherige Verzweigung bleibt unverändert, und Sie können über den Baum zu ihr zurückkehren.

<a id="deleting-parts-and-branches"></a>

## Teile und Verzweigungen löschen

**Löschen** im Menü eines Teils entfernt nur diesen Teil. Die Teile danach rücken nach oben und setzen am Teil davor an.
Um eine ganze Verzweigung zu entfernen, löschen Sie ihre Teile vom Ende her.

Die Wort- und Token-Zählungen auf dem Reiter *Info* zeigen sowohl das gesamte Buch (alle Verzweigungen) als auch die
aktive Verzweigung.
