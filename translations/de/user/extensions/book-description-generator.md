---
label: Buchbeschreibungs-Generator
order: 21
---

# Buchbeschreibungs-Generator

Lässt das Modell den **Namen** und die **Beschreibung** eines Buchs aus seiner Geschichte schreiben. Du wählst, welchen Teil der Geschichte es liest, und kopierst das Ergebnis in das Buch.

<a id="opening-it"></a>

## Öffnen

Wenn die Erweiterung installiert ist, steht am Ende des Tabs **Info** im Buchfenster die Schaltfläche **Name und Beschreibung generieren**. Sie funktioniert auch im [Telefon-Layout](../phone.md#extensions). Der Dialog zeigt die Optionen links und das Ergebnis rechts.

![Das Fenster, das den Namen und die Beschreibung generiert](../../images/plugin-book-description.png)

| Option | |
|---|---|
| **Modell**, **Protokolle** | Womit die Generierung läuft. Sie beginnt mit dem Modell und Protokoll des Buchs, sonst mit deinen Standardwerten aus *Einstellungen* (siehe [Konto & Einstellungen](../account.md)); die Auswahl gilt nur für diese Generierung. Ohne Modell erscheint nur eine Warnung. |
| **Ab Nachricht**, **Bis Nachricht** | Der Bereich der Geschichte, in den Nummern der Nachrichten mit Text des aktiven Zweigs. Der Anfang der Nachricht wird unter dem Feld angezeigt, damit du erkennst, welche es ist. Er beginnt mit der ganzen Geschichte. |
| **Zusammenfassungen verwenden** | Eine [Zusammenfassung](../books/summaries.md) ersetzt die Nachrichten, die sie abdeckt, wenn alle von ihnen im Bereich liegen; das macht einen langen Bereich deutlich günstiger. Eine Zusammenfassung, die über den Bereich hinausreicht, wird nicht verwendet, ebenso wenig eine, die die Geschichte nicht mehr beschreibt (dann werden stattdessen die Nachrichten verwendet). Wird für das nächste Mal gemerkt. |
| **Antwortformat** | *Automatisch* verlangt JSON mit einem Schema, wenn der Dienst des Modells das annehmen kann, und Markdown, wenn nicht. *JSON* besteht auf einem Schema. *JSON ohne Schema* beschreibt das JSON stattdessen im Prompt, für einen Server, der kein Schema zusammen mit dem Denken des Modells verarbeiten kann. *Markdown* verlangt den Namen als erste Überschrift und die Beschreibung darunter. Was das Modell schreibt, wird als JSON oder als Markdown gelesen, sodass auch ein Modell, das die Anweisung ignoriert, verstanden wird. Wird für das nächste Mal gemerkt. |

Unter den Optionen zeigt ein Zähler, wie viele Tokens der Bereich mit dem Prompt ausmacht, wie viele das Modell aufnimmt und ob die Geschichte unverändert gesendet wird oder vorher zu Notizen verarbeitet werden muss.

**Generieren** startet das Modell, dieselbe Schaltfläche stoppt es. Der Name und die Beschreibung erscheinen rechts, jeweils mit einer eigenen Schaltfläche **In das Buch kopieren**: Sie setzt den Text in das Feld des Tabs Info und speichert das Buch sofort (auch der Titel des Buchfensters ändert sich). Wenn die Antwort nicht als Name und Beschreibung gelesen werden kann, zeigt der Dialog den Text, den das Modell geschrieben hat.

<a id="a-story-that-is-too-long"></a>

## Eine zu lange Geschichte

Ist der Bereich größer, als das Modell aufnimmt, wird die Geschichte in passende Teile der Reihe nach aufgeteilt, und das Modell schreibt zu jedem Teil **Notizen** (sie erscheinen unter *Notizen*, sobald sie fertig sind). Die Beschreibung wird dann aus den Notizen statt aus der Geschichte geschrieben. Notizen, die noch zu lang sind, werden erneut aufgeteilt und notiert, bis zu vier Runden. Eine einzelne Nachricht, die größer als ein Teil ist, wird an einem Absatz, einer Zeile oder einem Satz geschnitten.

!!!
Wenn weder für das Modell noch für das Protokoll eine Kontextgröße gesetzt ist, kann die Erweiterung nicht erkennen, was zu lang ist, und sendet die Geschichte immer unverändert.
!!!

## Prompts

Es gibt zwei Prompts: den, der den Namen und die Beschreibung schreibt, und den, der die Notizen zu einem Teil einer langen Geschichte schreibt. Sie sind [vererbte Einstellungen](../books/prompts.md#inherited-settings): Ein Buch verwendet seine eigenen, sonst den nächsten Ordner darüber, der einen setzt, sonst deine, sonst den eingebauten Text.

| Prompt | Variablen |
|---|---|
| **Prompt für die Beschreibung** | `{{story}}` (die Geschichte oder die daraus gemachten Notizen), `{{fromNotes}}`, `{{bookName}}`, `{{bookDescription}}` |
| **Prompt für die Notizen** | `{{story}}` (der Teil), `{{part}}`, `{{parts}}`, `{{bookName}}`, `{{bookDescription}}` |

Setze sie im Tab *Prompts* des Buchs unter *Einstellungen der Erweiterung*, im Dialog eines Ordners oder für alle deine Bücher unter *Einstellungen → Einstellungen der Erweiterung → Book Description Generator*. Die Anweisungen, die dem Modell sagen, wie es die Antwort schreibt (JSON oder Markdown), fügt die Erweiterung nach dem Prompt für die Beschreibung hinzu, sodass dein Prompt nur sagen muss, was geschrieben werden soll.
