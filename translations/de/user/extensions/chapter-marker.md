---
label: Kapitelmarkierung
order: 20
---

# Kapitelmarkierung

Verwandelt die **Gliederung** des [Story-Editors](../books/story-editor.md) in ein Inhaltsverzeichnis.

![Kapitel in der Gliederung](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## So funktioniert es

Enthält der Text eines Abschnitts eine Markdown-Überschrift – eine Zeile, die mit `#` beginnt –, zeigt der Eintrag des Abschnitts in der Gliederung statt der üblichen Bezeichnung `#n (DB ID: m)` die Überschrift, hervorgehoben:

```
# Chapter 3: The Harbour

Rain had been falling on Ostra since dawn...
```

erscheint in der Gliederung als **5. Chapter 3: The Harbour** – die Nummer ist die Position des Abschnitts im Zweig, gefolgt von der Überschrift. Abschnitte ohne Überschrift behalten ihre normale Bezeichnung.

- Die Überschrift kann in einer beliebigen Zeile des Abschnitts stehen; sind es mehrere, wird die erste verwendet.
- Jede Überschriftenebene funktioniert (`#`, `##`, `###` ...).
- Die Gliederung wird aktualisiert, sobald du eine [Änderung](../books/story-editor.md#editing-the-text) speicherst: Füge eine Überschrift hinzu, um ein Kapitel zu beginnen, und entferne sie, um den Abschnitt wieder zusammenzuführen.

<a id="usage"></a>

## Verwendung

Es gibt nichts zu konfigurieren. Entweder schreibst du die Überschrift selbst in den Abschnitt, oder du bittest das Modell in den Anweisungen darum, z. B. *Beginne ein neues Kapitel mit dem Titel „The Harbour“ und einer Markdown-Überschrift.*

!!!
Der Standard-[Benutzer-Prompt](../books/prompts.md#user-prompt) weist das Modell an, keine Titel oder Kapitelüberschriften zu schreiben.
Wenn das Modell sie schreiben soll, sag das ausdrücklich in den Anweisungen oder ändere die Regel im Benutzer-Prompt.
!!!

Die Überschriften sind Teil des Erzähltextes; sie werden daher auch an das Modell gesendet und im [Viewer](../books/publishing.md) sowie in Backups angezeigt.

Dieselbe Regel findet die Kapitel im Inhaltsverzeichnis von [exportierten Büchern](../books/export.md#table-of-contents).
