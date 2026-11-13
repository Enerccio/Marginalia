---
label: Story-Notizen
order: 27
---

# Story-Notizen

Ein kleines Wiki für dein Buch, in der Seitenleiste: Seiten für die Handlung, die du planst, eine Idee, die du noch nicht verwenden kannst, die Regeln eines Magiesystems, was der Schurke wirklich will. Es sind deine eigenen Gedanken, in deinen eigenen Worten, mit Schlagwörtern und Links zwischen den Seiten. Anders als die [Autorennotiz](authors-note.md), die als ein einziger Text bei jeder Generierung mitgeschickt wird, werden die Notizen nicht eigenständig an das Modell gesendet. Der [Handlungsassistent](arc-outline.md#developing-the-plot-in-a-conversation) und Agenten können sie lesen und, wenn du es erlaubst, auch ändern.

<a id="the-notes-tab"></a>

## Der Reiter Notizen

Die linke Seitenleiste des [Geschichteneditors](../books/story-editor.md) erhält neben *Gliederung* einen Reiter **Notizen**. Er listet die Seiten des Buchs nach Titel sortiert auf. Oberhalb der Liste stehen **Hinzufügen** (eine neue Seite), ein Filter nach **Schlagwort** (sichtbar, sobald eine Seite ein Schlagwort hat) und eine Suche über die Titel, die Schlagwörter und den Text der Seiten.

Jede Seite zeigt ihren Titel, die erste Zeile ihres Textes und ihre Schlagwörter. Klicke auf die Seite oder auf den Stift, um sie zu öffnen; der Papierkorb löscht sie nach einer Bestätigung. Zwei kleine Markierungen können auf den Titel folgen: ein durchgestrichenes Auge (die Seite ist [vor Assistenten verborgen](#what-assistants-can-see)) und ein Zauberstab (die letzte Änderung stammt von einem Befehl, nicht von dir).

![Der Reiter Notizen: eine Seite, die vor Assistenten verborgen ist (das Auge), und eine, der der Handlungsassistent etwas hinzugefügt hat (der Zauberstab)](../../images/plugin-storynotes-tab.png)

<a id="a-page"></a>

## Eine Seite

Der Dialog einer Seite enthält:

| | |
|---|---|
| **Titel** | Pflichtfeld, im Buch eindeutig (Groß- und Kleinschreibung spielt keine Rolle), ohne eckige Klammern oder einen Senkrechtstrich (`|`). Darauf verweisen andere Seiten. |
| **Schlagwörter** | Durch Kommas getrennt. Sie filtern die Liste. |
| **Text** | Markdown. **Schreiben** und **Vorschau** wechseln zwischen dem Text und seiner Darstellung. `[[Titel]]` im Text verlinkt auf die Seite mit diesem Titel; `[[Titel|andere Worte]]` verlinkt ebenfalls, zeigt in der Vorschau aber *andere Worte* an (`[[Sprachbarriere|wie sie reden]]`). |
| **Für Assistenten sichtbar** | Standardmäßig eingeschaltet. Siehe unten. |

Unter dem Text listet der Dialog die Seiten auf, auf die der Text **verlinkt**: Ein Link auf eine bestehende Seite öffnet sie, ein Link auf eine nicht vorhandene bietet **Erstellen** an und legt eine neue Seite mit diesem Titel an. Darunter stehen die Seiten, die **hierher verlinken**. Wird eine Seite umbenannt, ändern sich die `[[Links]]` auf sie in den anderen Seiten (die Worte, die ein Link anzeigt, bleiben erhalten).

![Eine Seite mit den Seiten, auf die sie verlinkt, und der Seite, die auf sie verlinkt](../../images/plugin-storynotes-page.png)

**Vorschau** zeigt den Text als Markdown, wobei jedes `[[Link]]` ein Link ist: Klicke darauf, um diese Seite zu öffnen (ein Link auf eine nicht vorhandene Seite legt eine neue mit diesem Titel an):

![Die Vorschau derselben Seite](../../images/plugin-storynotes-preview.png)

Vor **Speichern** wird nichts gesichert; wer eine geänderte Seite schließt, wird vor dem Verwerfen der Änderungen gefragt. Wurde die Seite während deiner Bearbeitung durch einen Befehl verändert, wirst du gefragt, bevor deine Fassung sie überschreibt. **Strg/Cmd+Enter** speichert. Die Seiten werden zusammen mit dem Buch gespeichert und sind daher in dessen Sicherungen und Kopien enthalten.

<a id="what-assistants-can-see"></a>

## Was Assistenten sehen können

„Assistenten“ sind die Befehle, die die Notizen mitbringen und die Agenten, Makros und der Handlungsassistent nutzen. Eine Seite, bei der **Für Assistenten sichtbar** ausgeschaltet ist, existiert für sie nicht: Sie wird nicht aufgelistet, nicht gefunden, nicht gelesen und nicht verändert, und ein Titel, den nur sie trägt, wird abgelehnt, wenn sie versuchen, ihn anzulegen. Schalte es für Dinge aus, die nur du sehen sollst. Seiten, die du ausgeschaltet hast, sind weiterhin in deinen Sicherungen enthalten.

Die Befehle (für die Befehlsleiste und für Agenten; der Titel ist die Adresse einer Seite):

| Befehl | |
|---|---|
| `/list-notes [<tag>]`, `/get-note <note>`, `/search-notes <query>` | Die Seiten, eine Seite mit ihrem Text, ihren Links und den Seiten, die darauf verlinken, und eine Suche. |
| `/create-note <title> [<content> [<tags>]]` | Legt eine Seite an. |
| `/append-to-note <note> <text>` | Fügt einen Absatz am Ende einer Seite hinzu. |
| `/update-note <note> [<title> [<content> [<tags>]]]`, `/edit-note <note> <find> <replace> [<all>]` | Ändert eine Seite oder ersetzt einen Teil ihres Textes: `find` muss genau so in der Seite stehen, wie geschrieben (Groß- und Kleinschreibung zählt), und, sofern `all` nicht `true` ist, genau einmal. Mit `all` wird jedes Vorkommen ersetzt, etwa für einen Namen oder einen Begriff auf der ganzen Seite. Die Antwort sagt, wie viele Stellen ersetzt wurden. Diese Befehle ändern vorhandenen Text und sind daher *destruktiv*: Ein Agent, der so eingestellt ist, dass er nachfragt, fragt dich zuerst. |
| `/delete-note <note>` | Löscht eine Seite nach einer Bestätigung. |
| `/note-text <note>` | Der Text einer Seite, für Makros gedacht: {%{`{{command::note-text::Magic}}`}%} fügt ihn in eine Vorlage ein. Leer für eine Seite, die nicht existiert oder verborgen ist. |

Eine Seite, die ein Befehl verändert hat, trägt den Zauberstab, bis du sie selbst speicherst.

<a id="with-the-plot-wizard"></a>

## Mit dem Handlungsassistenten

Ist diese Erweiterung installiert, hat der [Handlungsassistent](arc-outline.md#developing-the-plot-in-a-conversation) der Bogengliederung die Option **Notizen senden** (die Titel mit ihren ersten Zeilen, den vollständigen Text oder nichts) und eine Gruppe **Notizen** mit Werkzeugen: Das Modell kann die Seiten durchsuchen und lesen, deren Links verfolgen, eine Seite anlegen oder etwas zu einer Seite hinzufügen, wenn du es bittest, etwas festzuhalten, und Text in einer Seite ändern. Das Ändern des Textes einer Seite wird vorher abgefragt, solange *Vor Änderungen oder Löschungen nachfragen* eingeschaltet ist; der Assistent kann keine Seite löschen. Siehe die Option und die Gruppe in der Tabelle des Assistenten.

![Der Handlungsassistent mit den Werkzeugen für Notizen: Er hat eine Seite gelesen und die offene Frage darin ergänzt](../../images/plugin-storynotes-wizard.png)

<a id="other-extensions"></a>

## Andere Erweiterungen

Die Erweiterung stellt anderen Erweiterungen zwei Textquellen zur Verfügung (die [Nebenabfrage](side-query.md) kann sie über ihren Steckerknopf anhängen): **Story-Notizen (Titel)** und **Story-Notizen (alle)**. Beide lassen die verborgenen Seiten aus.
