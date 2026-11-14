---
label: Vorlagen und Makros
order: 940
icon: code
---

# Vorlagen und Makros

Alles, was Marginalia an das Modell sendet, wird aus **Vorlagen** aufgebaut: Texte mit Platzhaltern, die für jeden
generierten Teil ausgefüllt werden. Du kannst sie alle ändern, vom Rahmen des Prompts bis zu einem einzelnen Eintrag im Lorebook.

{%{
```handlebars
Write the next section of the story.
{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{instructions}}
```
}%}

<a id="where-templates-are-used"></a>

## Wo Vorlagen verwendet werden

| Vorlage | Festgelegt in | Wird zu |
|---|---|---|
| **Hauptvorlage** | Buch [Prompts](../books/prompts.md), *Einstellungen → Vorlagen* | der Systemtext: Rolle, Lore, Stil, Zusammenfassungen |
| **User Prompt** | Buch Prompts, *Einstellungen → Vorlagen* | die Anfrage für den Teil, der gerade geschrieben wird |
| **Summary Prompt** | Buch Prompts, *Einstellungen → Vorlagen* | die Anfrage für eine [Zusammenfassung](../books/summaries.md) |
| **Meta Summary Prompt** | Buch Prompts, *Einstellungen → Vorlagen* | die Anfrage für eine [Meta-Zusammenfassung](../books/summaries.md#meta-summaries) |
| **Lorebook entry content** | [Lorebook-Einträge](../lorebooks/entries.md) | die Lore, die in den Prompt eingefügt wird |

*Point of View*, *Tense* und *Style* sind einfacher Text, keine Vorlagen.

<a id="two-kinds-of-placeholders"></a>

## Zwei Arten von Platzhaltern

Vorlagen kombinieren zwei Syntaxen:

- **Handlebars** - Variablen wie {%{`{{instructions}}`}%} und Abschnitte wie
  {%{`{{#sceneSetting}}...{{/sceneSetting}}`}%}, die nur ausgegeben werden, wenn der Wert nicht leer ist. Siehe
  [Prompt-Vorlagen](templates.md).
- **SillyTavern-Makros** - {%{`{{user}}`, `{{random::a::b}}`, `{{setvar::name::value}}`, `{{if ...}}`}%} und die übrigen Makros, die SillyTavern-Nutzer kennen, damit Lorebooks und Prompts aus SillyTavern weiter funktionieren. Siehe
  [Makro-Referenz](macros.md).

Beides funktioniert in jeder Vorlage und lässt sich frei mischen.

<a id="help-in-the-editor"></a>

## Hilfe im Editor

Jedes Vorlagenfeld hat ein Hinweis-Popover:

- **Standardvorlage einfügen** kopiert die Standardvorlage in das Feld;
- **Verfügbare Vorlagenvariablen** listet die Variablen dieser Vorlage auf, einschließlich derer, die überall verfügbar sind;
- **Verfügbare Makros** listet alle Makros mit einer kurzen Beschreibung auf.

![Das Hinweis-Popover eines Vorlagenfelds](../../images/template-hints.png)

Eine Vorlage mit einem Syntaxfehler (zum Beispiel ein nicht geschlossener Abschnitt) lässt sich in den Buch-Prompts und in den Einstellungen nicht speichern. In Lorebook-Einträgen wird sie gespeichert, als ungültig markiert und als einfacher Text verwendet, bis der Fehler behoben ist.

Unbekannte Variablen (meist Tippfehler) verhindern das Speichern nicht; das Feld listet sie darunter als Warnung auf, siehe
[Tippfehler](templates.md#typos).

**Prompt anzeigen** im Menü eines generierten Teils zeigt den Prompt so, wie er gesendet wurde. Das ist der beste Weg zu prüfen, was eine Vorlage erzeugt hat.

<a id="in-this-section"></a>

## In diesem Abschnitt

- [Prompt-Vorlagen](templates.md) - Variablen jeder Vorlage, Abschnitte, Bedingungen, Auswertungsreihenfolge
- [Makro-Referenz](macros.md) - alle Makros, Variablen, Bedingungen und SillyTavern-Kompatibilität
