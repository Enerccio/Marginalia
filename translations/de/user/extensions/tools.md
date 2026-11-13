---
label: Werkzeuge für das Modell
order: 0
---

# Werkzeuge für das Modell

Manche Erweiterungen geben dem Modell **Werkzeuge**: Funktionen, die es beim Schreiben aufrufen kann, um Namen zu erhalten, echte Orte nachzuschlagen und
so weiter, statt sie zu erfinden. Die Werkzeuge einer Erweiterung bilden eine **Suite**; die Suiten sind:

| Suite | Erweiterung | Was das Modell damit tun kann |
|---|---|---|
| Namensgenerator | [Namensgenerator](name-generator.md) | Erfundene Namen erhalten, die wie eine Kategorie klingen (Orte, Gottheiten, Nachnamen eines Landes ...). |
| Namenslieferant | [Namenslieferant](name-provider.md) | Echte Namen in ihrer Originalschreibweise erhalten. |
| Ortslieferant | [Ortslieferant](place-provider.md) | Echte Orte nachschlagen, was in ihrer Umgebung liegt und wie weit sie voneinander entfernt sind. |

Ein Dialog, der das Modell mit Werkzeugen ausführt, hat eine Schaltfläche **Werkzeuge**: die Generatoren des [Lore-Generators](lore-generator.md) und des
[Bogenumrisses](arc-outline.md) (nicht dessen Story-Import, dessen Namen bereits in der Story stehen). Die Schaltfläche wird nur
angezeigt, solange mindestens eine Suite installiert ist, und wenn du etwas ausgewählt hast, zeigt sie an, wie viele Dinge aktiv sind: **Werkzeuge (3)**.

<a id="choosing-the-tools"></a>

## Werkzeuge auswählen

**Werkzeuge** öffnet ein Fenster mit einem Abschnitt für jede Suite, unter dem Namen der Suite, mit der Anzahl der darin markierten Dinge.
Ein Abschnitt mit markierten Dingen startet geöffnet. Was in einem Abschnitt steht, hängt von der Suite ab:

- Eine Suite mit **Einträgen** (der Namensgenerator und der Namenslieferant haben sie für jede Kategorie bzw. Namensliste) zeigt eine Tabelle mit je einem Kontrollkästchen
  und darüber ein **eigenes Filterfeld**: Tippe einen Teil eines Namens ein, um nur die passenden Einträge anzuzeigen. Die
  markierten Einträge bleiben immer in der Tabelle, egal was der Filter sagt. **Angezeigte markieren** markiert alle Einträge, die die Tabelle
  in diesem Moment zeigt, **Markierung der angezeigten aufheben** hebt sie wieder auf, und eine Zeile darunter sagt, wie viele markiert sind. Das Modell erhält
  nur die markierten Einträge: Es kann sie auflisten und nach Namen aus ihnen fragen, aber nach keinen anderen.
- Eine Suite **ohne Einträge** (der Ortslieferant) ist an oder aus: **Diese Suite verwenden**.

![Das Fenster Werkzeuge mit den nach „Japan“ gefilterten Kategorien des Namensgenerators](../../images/tools-dialog.png)

Jeder Abschnitt hat außerdem einen **Hinweis für das Modell**. Das ist ein kurzer Text, der am Ende des Prompts angefügt wird, wenn das Modell die
Suite erhält, und ihm sagt, wann und wie es die Werkzeuge verwenden soll (bei den Namen: die Werkzeuge aufrufen, statt Namen zu erfinden;
aus dem Erhaltenen auswählen; einen echten Namen so behalten, wie er zurückgegeben wurde). Der eingebaute Text wird im Feld grau angezeigt; schreibe deinen eigenen, um ihn zu ersetzen,
oder lass das Feld leer, um den eingebauten zu verwenden.

![Eine eingeschaltete Suite mit geöffnetem Hinweis für das Modell; die beiden anderen Abschnitte sind geschlossen, mit der Anzahl der markierten Einträge](../../images/tools-hint.png)

Das Modell erhält die Suiten, in denen etwas markiert ist: Ist in keiner etwas markiert, gibt es keine Werkzeuge. Die Auswahl wird mit
deinem Konto gespeichert, sie ist also beim nächsten Mal dieselbe; der Lore-Generator speichert die Auswahl außerdem bei jedem Eintrag, den er erzeugt hat, und öffnet diesen Eintrag damit. Eine Suite, die entladen wurde, wird übersprungen, und ein markierter Eintrag, den die Erweiterung nicht mehr hat, wird ignoriert. Erweiterungen können geladen und entladen werden, während Marginalia läuft; die Schaltfläche folgt dem.

Wenn zwei Suiten eine Funktion mit demselben Namen hatten, startet die Generierung nicht und nennt, welche das sind; die Suiten, die mit Marginalia mitgeliefert werden, tun das nicht.

<a id="as-commands"></a>

## Als Befehle

Die Suiten sind außerdem Befehle der [Befehlsleiste](../commands/index.md) und der [Agenten](../agents.md) (`/pick-real-names`,
`/generate-names`, `/search-places` ...). Was das Modell hier erhält und was ein Agent aufrufen kann, ist also dieselbe Suite unter demselben
Namen.

<a id="from-an-older-version"></a>

## Aus einer älteren Version

Die Schaltfläche **Namenswerkzeuge** heißt jetzt **Werkzeuge**. Die Kategorien, die du markiert hattest, sind in den Suiten des Namensgenerators und des
Namenslieferanten markiert, und die Hinweise, die du unter *Einstellungen → Erweiterungseinstellungen* geschrieben hattest (*Hinweis für Namensgenerator*, *Hinweis für Namenslieferant*), sind
jetzt die Hinweise dieser beiden Suiten in diesem Fenster; die Felder in den Einstellungen gibt es nicht mehr.
