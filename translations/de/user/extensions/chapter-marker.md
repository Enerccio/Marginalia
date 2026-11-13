---
label: Kapitelmarkierung
order: 20
---

# Kapitelmarkierung

Macht aus der **Gliederung** des [Story-Editors](../books/story-editor.md) ein Inhaltsverzeichnis.

![Kapitel in der Gliederung](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## So funktioniert es

Enthält der Text eines Teils eine Markdown-Überschrift – eine Zeile, die mit `#` beginnt –, zeigt der Eintrag des Teils in der Gliederung statt der üblichen Bezeichnung `#n (DB ID: m)` die Überschrift an, hervorgehoben:

```
# Kapitel 3: Der Hafen

Seit dem Morgen regnete es auf Ostra...
```

erscheint in der Gliederung als **5. Kapitel 3: Der Hafen** – die Zahl ist die Position des Teils im Zweig, gefolgt von der Überschrift. Teile ohne Überschrift behalten ihre normale Bezeichnung.

- Die Überschrift kann in einer beliebigen Zeile des Teils stehen; sind es mehrere, wird die erste verwendet.
- Jede Überschriftenebene funktioniert (`#`, `##`, `###` ...).
- Die Gliederung aktualisiert sich, sobald du eine [Bearbeitung](../books/story-editor.md#editing-the-text) speicherst: Füge eine Überschrift hinzu, um ein Kapitel zu beginnen, entferne sie, um den Teil wieder mit dem vorherigen zu verbinden.

<a id="usage"></a>

## Verwendung

Es gibt nichts zu konfigurieren. Entweder schreibst du die Überschrift selbst in den Teil, oder du bittest das Modell in den Anweisungen darum, z. B. *Beginne ein neues Kapitel mit dem Titel „Der Hafen“ und einer Markdown-Überschrift.*

!!!
Der Standard-[Benutzer-Prompt](../books/prompts.md#user-prompt) weist das Modell an, keine Titel oder Kapitelüberschriften zu schreiben. Wenn das Modell sie schreiben soll, sag es in den Anweisungen ausdrücklich oder ändere diese Regel im Benutzer-Prompt.
!!!

Die Überschriften sind Teil des Story-Textes; sie werden daher ebenfalls an das Modell gesendet und im [Viewer](../books/publishing.md) sowie in Backups angezeigt.

Dieselbe Regel findet die Kapitel im Inhaltsverzeichnis [exportierter Bücher](../books/export.md#table-of-contents).
