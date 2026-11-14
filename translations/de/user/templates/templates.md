---
label: Prompt-Vorlagen
order: 100
---

# Prompt-Vorlagen

Vorlagen verwenden die [Handlebars](https://handlebarsjs.com/guide/)-Syntax, erweitert um die SillyTavern-[Makros](macros.md). Diese Seite beschreibt die Variablen, die jede Vorlage erhält, und den Handlebars-Teil der Syntax.

{%{

<a id="variables"></a>

## Variablen

Eine Variable in doppelten geschweiften Klammern wird durch ihren Wert ersetzt: `{{instructions}}` wird zu den Anweisungen, die du für den Teil geschrieben hast. Ein leerer Wert gibt nichts aus.

<a id="variables-of-each-template"></a>

### Variablen jeder Vorlage

| Vorlage | Variablen |
|---|---|
| **Hauptvorlage** | `{{backgroundLore}}` aktivierte Lorebook-Einträge (*Im Lore-Block*), `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}`, `{{summaries}}` alle Zusammenfassungen des Zweigs, `{{annotations}}` die [Notizen](../books/annotations.md#giving-a-note-to-the-model), die du markiert hast, damit sie dem Modell übergeben werden, für die Teile im Prompt |
| **Benutzerprompt** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}` - die Anweisungen für den Zug |
| **Zusammenfassungs-Prompt** | `{{text}}` die zusammenzufassenden Teile, `{{backgroundLore}}` Lore, die aktiv war, als der zusammengefasste Teil geschrieben wurde |
| **Meta-Zusammenfassungs-Prompt** | `{{summaryBlocks}}` die zusammenzuführenden Zusammenfassungen, die älteste zuerst, `{{backgroundLore}}` Lore, die aktiv war, als der Teil der Meta-Zusammenfassung geschrieben wurde |
| **Lorebook-Eintrag** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`, `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` |

<a id="variables-available-everywhere"></a>

### Variablen, die überall verfügbar sind

Neben ihren eigenen Variablen kann jede Vorlage außerdem verwenden:

| Variable | |
|---|---|
| `{{povCharacter}}`, `{{sceneSetting}}`, `{{presentCharacters}}`, `{{instructions}}` | Die Anweisungen für den Zug des Teils, der gerade geschrieben wird (bei einer Zusammenfassung: des zusammengefassten Teils). |
| `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` | Die erzählerischen Einstellungen des Buchs. |
| `{{manuscriptName}}`, `{{manuscriptDescription}}` | Name und Beschreibung des Buchs. |

Das Hinweis-Popover (ⓘ) jedes Vorlagenfeldes listet sie ebenfalls auf.

<a id="typos"></a>

### Tippfehler

Eine Variable, die es nicht gibt, wird als Wort **`Error`** ausgegeben: `{{instruction}}` (ohne das *s*) fügt *Error* in den Prompt ein. Die Vorlagenfelder (Bucheinstellungen *Prompts*, *Einstellungen*, Lorebook-Einträge) listen solche Namen unter dem Feld als *Unbekannte Namen (werden als "Error" an das Modell gesendet)* auf. Das ist nur eine Warnung - die Vorlage wird trotzdem gespeichert -, also korrigiere die aufgelisteten Namen. Variablen und Abschnitte werden überall geprüft, auch in `{{#if}}`-Zweigen, die nicht ausgegeben werden. Namen, die nur als Bedingung verwendet werden, werden nicht gemeldet, weil sie nicht *Error* ausgeben: In `{{#if name}}` ist ein unbekannter Name falsch, in der SillyTavern-Form `{{if name}}` wird er als reiner Text behandelt, und der gilt als wahr.

<a id="sections"></a>

## Abschnitte

Ein Abschnitt gibt seinen Inhalt nur aus, wenn die Variable einen Wert hat. Innerhalb ist `{{.}}` der Wert:

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

Bei leerem *Schauplatz der Szene* verschwindet die ganze Zeile - die Standardvorlagen nutzen das überall, damit der Prompt keine leeren Überschriften hat.

Ein **invertierter Abschnitt** gibt seinen Inhalt nur aus, wenn die Variable leer ist:

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

<a id="conditions"></a>

## Bedingungen

`{{#if}}` mit optionalem `{{else}}`:

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` ist das Gegenteil von `{{#if x}}`. Die SillyTavern-Form `{{if x}}...{{/if}}` funktioniert ebenfalls und hat mehr Optionen (Negation, Variablen, Vergleiche), siehe [Bedingungen](macros.md#conditionals).

Makros ohne Argumente können in Abschnitten und Bedingungen wie Variablen verwendet werden:

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## Kommentare und Escaping

| Syntax | Ergebnis |
|---|---|
| `{{!-- a comment --}}`, `{{! short comment }}` | Wird aus der Ausgabe entfernt. Verwende sie für Notizen in deinen Vorlagen. |
| `\{{instructions}}` | Gibt `{{instructions}}` wörtlich aus, statt des Wertes. |
| `{{{instructions}}}` | Wie `{{instructions}}`. In Marginalia wird nichts HTML-escaped, also werden `<`, `&` und Anführungszeichen unverändert ausgegeben. |

Der SillyTavern-Kommentar `{{// ...}}` und das Escaping `\{\{...\}\}` funktionieren ebenfalls, siehe [Utility](macros.md#utility).

<a id="order-of-evaluation"></a>

## Auswertungsreihenfolge

Für einen generierten Teil werden die Vorlagen in dieser Reihenfolge verarbeitet:

1. der **Benutzerprompt**;
2. die **Lorebook-Einträge**, die aktiviert wurden, in ihrer Reihenfolge;
3. die **Hauptvorlage**.

Alle teilen sich eine Menge von [Variablen](macros.md#variables): Ein mit `{{setvar}}` im Benutzerprompt gesetzter Wert kann in Lorebook-Einträgen und der Hauptvorlage gelesen werden; ein in einem Lorebook-Eintrag gesetzter Wert ist für spätere Einträge und die Hauptvorlage sichtbar, aber nicht für den Benutzerprompt, der bereits verarbeitet wurde.

Lorebook-Filter sehen den verarbeiteten Benutzerprompt, sodass ein Makro im Benutzerprompt Text erzeugen kann, der Einträge aktiviert.

<a id="what-is-kept-between-parts"></a>

## Was zwischen Teilen erhalten bleibt

| | |
|---|---|
| **Lokale Variablen** (`{{setvar}}`, `{{.name}}`) | Werden mit dem generierten Teil gespeichert. Der nächste Teil beginnt mit den Werten des letzten Teils des aktiven Zweigs, sodass jeder Zweig seine eigenen Werte hat. *Neu generieren* beginnt erneut mit den Werten des Teils davor. |
| **Globale Variablen** (`{{setglobalvar}}`, `{{$name}}`) | Werden mit dem Buch gespeichert und von allen Zweigen geteilt. *Neu generieren* oder das Löschen eines Teils macht sie nicht rückgängig. |
| **Zusammenfassungen** | Können Variablen lesen, aber ihre Änderungen werden verworfen. |

`{{pick}}` liefert bei gleichem Buch jedes Mal dasselbe Ergebnis, `{{random}}` jedes Mal ein neues.

<a id="writing-your-own-templates"></a>

## Eigene Vorlagen schreiben

Beginne mit der Standardvorlage: Öffne das Hinweis-Popover und klicke auf **Standardvorlage einfügen**, dann passe sie an. Folgendes solltest du beibehalten:

- den `{{#backgroundLore}}`-Block in der Hauptvorlage - ohne ihn wird das Lorebook nicht verwendet (nur Einträge mit *Vor dem Benutzerprompt* erreichen weiterhin das Modell);
- den `{{#summaries}}`-Block in der Hauptvorlage - ohne ihn gehen Zusammenfassungen verloren;
- `{{instructions}}` im Benutzerprompt - ohne sie sieht das Modell nicht, was geschehen soll;
- `{{text}}` im Zusammenfassungs-Prompt - ohne sie gibt es nichts zusammenzufassen.

}%}
