---
label: Makro-Referenz
order: 90
---

# Makro-Referenz

Marginalia versteht die Makros von [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), sodass Prompts und Welt-Informationen, die für SillyTavern geschrieben wurden, ohne Änderungen funktionieren. Makros funktionieren in jeder [Vorlage](templates.md): Hauptvorlage, Benutzer-Prompt, Zusammenfassungs-Prompt und Lorebook-Einträgen.

{%{

## Syntax

| Form | Beispiel |
|---|---|
| ohne Argumente | `{{user}}` |
| Argumente durch `::` getrennt | `{{setvar::mood::nervous}}` |
| erstes Argument nach einem Leerzeichen | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| alte Schreibweise mit einfachem `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| verschachtelte Makros | `{{getvar::{{user}}_mood}}` |
| mit Bereich (Inhalt als letztes Argument) | `{{setvar backstory}}Long text...{{/setvar}}` |

- Makronamen unterscheiden keine Groß- und Kleinschreibung: `{{User}}` und `{{USER}}` funktionieren.
- Leerzeichen um Namen und Argumente werden ignoriert: `{{ getvar :: mood }}`.
- Zusätzliche `::` gehen in das letzte Argument: `{{setvar::x::a::b}}` setzt `x` auf `a::b`.
- Der Inhalt eines Bereichs wird zuerst ausgewertet und danach von überflüssigen Einrückungen befreit und gekürzt. Mit `#` nach den geschweiften Klammern, `{{#setvar name}}...{{/setvar}}`, bleiben die Leerzeichen genau erhalten.

<a id="names-and-participants"></a>

## Namen und Beteiligte

Marginalia kennt keine Personas oder Figurenkarten; die Namen stammen aus den Anweisungen für den Teil, der gerade geschrieben wird.

| Makro | Ergebnis |
|---|---|
| `{{user}}` | Die Perspektivfigur. |
| `{{char}}` | Die Perspektivfigur. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | Die anwesenden Figuren, oder die Perspektivfigur, wenn keine angegeben sind. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | Die anwesenden Figuren außer der Perspektivfigur (durch Komma oder Zeilenumbruch getrennt, verbunden mit `, `). |
| `{{description}}` | Die Beschreibung des Buches. |
| `{{scenario}}` | Der Schauplatz der Szene. |
| `{{input}}` | Die Anweisungen für den Teil. |

Die SillyTavern-Markierungen `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` und `<CHARIFNOTGROUP>` (unabhängig von der Schreibweise) funktionieren ebenfalls.

Mit der Perspektivfigur *Alice* und den anwesenden Figuren *Alice, Bob, Carol*:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## Verlauf der Geschichte

| Makro | Ergebnis |
|---|---|
| `{{lastMessage}}` | Der Text des letzten Teils des aktiven Zweigs. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | Die Position des letzten Teils, ab 0 gezählt. |
| `{{lastUserMessage}}` | Die Anweisungen des letzten Teils. |
| `{{summary}}` | Die neueste [Zusammenfassung](../books/summaries.md) des aktiven Zweigs. |
| `{{closest-message}}`, `{{closest-message::n}}` | Der Teil, der dem gerade geschriebenen am nächsten liegt: der letzte, oder der *n*-te, von dort rückwärts gezählt (`::2` ist der vor dem letzten). Leer, wenn es keinen solchen Teil gibt. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | Dasselbe für die Zusammenfassungen des Zweigs: die neueste, oder die *n*-te, von dort rückwärts gezählt. Alias `{{closestSummary}}`. |
| `{{current-message}}` | Der Text des Teils, den *Neu generieren* oder ein Swipe ersetzt; leer bei einem neuen Teil. Alias `{{currentMessage}}`. |

Beim ersten Teil eines Buches sind sie leer. Bei *Neu generieren* ist der „letzte Teil“ der Teil vor dem neu generierten Teil.

Die Zahl bei `closest-message` und `closest-summary` kann ein Makro sein und daher aus einer Variable stammen:
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` ist der dritte Teil vom Ende. Eine Zahl, die keine positive ganze Zahl ist, ergibt nichts. `closest-summary` zählt die Zusammenfassungen der Teile des Zweigs, einschließlich einer [Meta-Zusammenfassung](../books/summaries.md) und der Zusammenfassungen, die sie ersetzt; `{{summary}}` ist dasselbe wie `{{closest-summary}}`.

<a id="date-and-time"></a>

## Datum und Uhrzeit

| Makro | Beispielergebnis |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - die Uhrzeit mit einer UTC-Abweichung (`UTC-5:30` funktioniert ebenso) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [moment.js-Formatierungstoken](https://momentjs.com/docs/#/displaying/format/), Text in `[brackets]` wird unverändert ausgegeben: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - Zeit seit dem letzten geschriebenen Teil, `just now` für den ersten Teil. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - der Unterschied zwischen zwei Zeitpunkten; `now`, Datumsangaben (`2026-01-08`) und ISO-Uhrzeiten funktionieren. Unbekannte Werte ergeben nichts. |

Zeiten beziehen sich auf die Zeitzone des Computers oder Servers, auf dem Marginalia läuft, und werden immer auf Englisch ausgegeben. Für die [Docker-Version](../getting-started/docker-server.md) legen Sie die Zeitzone mit der Umgebungsvariable `TZ` fest, z. B. `TZ=Europe/Prague`.

<a id="variables"></a>

## Variablen

Variablen speichern Werte zwischen den Teilen: Zähler, die Stimmung einer Figur, Dinge, die die Geschichte etabliert hat. Es gibt zwei Arten:

- **lokale** Variablen gehören zum Zweig der Geschichte: Jeder Teil merkt sich ihre Werte, und ein neuer Teil beginnt mit den Werten des vorherigen Teils;
- **globale** Variablen gehören zum Buch und werden von allen Zweigen gemeinsam genutzt.

Siehe [Was zwischen Teilen erhalten bleibt](templates.md#what-is-kept-between-parts).

| Lokal | Global | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | Der Wert; leer, wenn nicht gesetzt. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Setzt den Wert. Gibt nichts aus. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Addiert eine Zahl oder hängt Text an, wenn einer der beiden Werte keine Zahl ist. Gibt nichts aus. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Addiert 1 und gibt den neuen Wert aus. Ein Wert, der keine Zahl ist, beginnt bei 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Zieht 1 ab und gibt den neuen Wert aus. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` oder `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Entfernt die Variable. Alias `flushvar` / `flushglobalvar`. |

Zahlen werden ohne nachgestellte Nullen ausgegeben: `1.50` plus `1.5` ergibt `3`.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### Kurzschreibweisen

`{{.name}}` ist eine lokale Variable, `{{$name}}` eine globale. Allein gestellt gibt eine Kurzschreibweise den Wert aus. Mit einem Operator:

| Kurzschreibweise | Wirkung | Ausgabe |
|---|---|---|
| `{{.hp = 20}}` | setzt den Wert | nichts |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | addiert / subtrahiert (`+=` hängt Text an Text an) | nichts |
| `{{.turn++}}`, `{{.turn--}}` | addiert / subtrahiert 1 | den neuen Wert |
| `{{.hp == 17}}`, `{{.hp != 17}}` | vergleicht | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | vergleicht Zahlen (`false` bei Text) | `true` / `false` |

Ersatzwerte und Vorgaben:

- `{{.name || Guest}}` gibt *Guest* aus, wenn die Variable leer oder falsch ist (`0`, `false`, `no`, `off`);
- `{{.name ?? Guest}}` gibt *Guest* nur aus, wenn die Variable überhaupt nicht gesetzt ist;
- `{{.title ||= Wanderer}}` setzt die Variable auf *Wanderer*, wenn sie leer oder falsch ist, und gibt sie dann aus;
- `{{.title ??= Wanderer}}` setzt sie nur, wenn sie überhaupt nicht gesetzt ist, und gibt sie dann aus.

Werte können Makros enthalten: `{{.log += -> {{user}}}}`.

Ein Teilzähler etwa oben im Benutzer-Prompt (`+=` gibt nichts aus, und `{{trim}}` entfernt die leere Zeile, die dabei entsteht):

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## Bedingungen

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

Die Bedingung kann sein:

| Bedingung | Wahr, wenn |
|---|---|
| ein Makroname - `{{if user}}` | das Makro einen Wert liefert |
| eine Variable - `{{if .mood}}`, `{{if $chapter}}` | die Variable einen Wert hat |
| eine Vorlagenvariable - `{{if sceneSetting}}` | die Variable einen Wert hat |
| ein Vergleich - `{{if .hp > 20}}` | der Vergleich zutrifft |
| beliebiger Text - `{{if yes}}` | immer, außer bei den falschen Werten |
| Verneinung - `{{if !user}}`, `{{if !.mood}}` | das Gegenteil |

Falsch sind ein leerer Wert sowie `0`, `false`, `no`, `off` (unabhängig von der Schreibweise). Alles andere ist wahr.

- Die Kurzform `{{if user::text}}` gibt *text* aus, wenn die Bedingung wahr ist.
- Nur der Zweig, der genommen wird, wird ausgewertet: ein `{{setvar}}` im anderen Zweig findet nicht statt.
- `{{if}}`-Blöcke werden wie Makros mit Bereich von überflüssigen Einrückungen befreit und gekürzt, sodass sie zur besseren Lesbarkeit eingerückt werden können. `{{#if}}` (das [Handlebars-if](templates.md#conditions)) behält die Leerzeichen bei.
- Ein `{{if}}` ohne `{{/if}}` wird als Text ausgegeben.
<a id="expressions"></a>

## Ausdrücke

`{{eval::expression}}` liefert `true` oder `false`. Verwenden Sie es überall dort, wo ein Vergleich nötig ist, den `{{if}}` nicht leisten kann: bei Werten der Vorlage (nicht nur bei Variablen), verknüpft mit `and`, `or` und `not`:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| In einem Ausdruck | Bedeutung |
|---|---|
| `"text"`, `'text'` | Ein Text. **Ein Text steht immer in Anführungszeichen**: ein Wort ohne Anführungszeichen ist ein Name. |
| `12`, `-3.5`, `true`, `false` | Eine Zahl oder ein Wahrheitswert. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Ein Wert der Vorlage, wie in `{{povCharacter}}`. Ein Makro ohne Argumente funktioniert ebenfalls (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Eine lokale oder globale [Variable](#variables); leer, wenn sie nicht gesetzt ist. |
| `( )` | Gruppierung. |

Vergleiche:

| Operator | Wahr, wenn |
|---|---|
| `==`, `!=` | Gleich, ungleich. Zahlen werden nach ihrem Wert verglichen (`5 == 5.0`), alles andere genau, daher ist `"Fiora" == "fiora"` falsch. |
| `~=` | Gleich, ohne Berücksichtigung der Groß- und Kleinschreibung. |
| `<`, `<=`, `>`, `>=` | Die Zahlen stehen in dieser Reihenfolge. Falsch, wenn eine Seite keine Zahl ist. |
| `contains` | Der Text links enthält den Text rechts, ohne Berücksichtigung der Groß- und Kleinschreibung: `presentCharacters contains "ma"` ist auch für *Mara* wahr. |
| `has` | Die Liste links, getrennt durch Kommas, Semikolons oder Zeilen, enthält das Element rechts, ohne Berücksichtigung der Groß- und Kleinschreibung und der Leerzeichen um die Elemente: `presentCharacters has "Mara"` ist für *Marati* nicht wahr. |

Verknüpfung: `and` (`&&`), `or` (`||`), `not` (`!`); `not` bindet am stärksten, dann `and`, dann `or`. Ein Wert allein ist wahr, außer er ist leer, `0`, `false`, `no` oder `off`. Makros sind innerhalb eines Textes erlaubt (`"{{user}}"`) und werden zuerst ausgegeben.

Ein ungültiger Ausdruck gibt `Error` aus: ein Syntaxfehler, ein Wort ohne Anführungszeichen, das kein Name ist (`povCharacter == Fiora`), oder ein unbekannter Name. Alle Namen werden geprüft, auch in dem Teil eines `and` / `or`, der nicht erreicht würde, damit sich ein Tippfehler nicht verstecken kann. In einer [Lorebook-Bedingung](../lorebooks/entries.md#a-condition-for-an-entry) macht ein `Error` den Eintrag unbenutzt, und die Vorschau zeigt das an.

<a id="randomness"></a>

## Zufall

| Makro | Ergebnis |
|---|---|
| `{{random::red::green::blue}}` | Eine der Optionen, bei jedem Mal eine andere. Außerdem `{{random:red,green,blue}}` und `{{random}}red, green{{/random}}`. |
| `{{pick::sword::axe::bow}}` | Eine der Optionen, aber immer **dieselbe** für dasselbe Buch und dieselbe Stelle in der Vorlage. Verwenden Sie es für Dinge, die konsistent bleiben müssen, etwa einen zufällig gewählten Namen. |
| `{{roll::2d6+3}}` | Ein Würfelwurf: `1d20`, `3d4-10`, `d100`; außerdem `{{roll d100}}`, `{{roll:6}}` (ein sechsseitiger Würfel). Ungültige Würfe ergeben nichts. |

<a id="generation-info"></a>

## Generierungsinformationen

| Makro | Ergebnis |
|---|---|
| `{{model}}` | Der Name des [Inferenz-Anbieters](../inference-providers.md) des Buchs. |
| `{{maxContextTokens}}` | Das Kontextlimit in Tokens (Protokoll oder Anbieter, siehe [Grenzen](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | Das Antwortlimit in Tokens. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Kontext minus Antwort: der Platz für den Prompt. |
| `{{lastGenerationType}}` | `normal` für einen neuen Teil, `regenerate`, `swipe` oder `quiet` in Zusammenfassungen. |

<a id="commands"></a>

## Befehle

| Makro | Ergebnis |
|---|---|
| `{{command::name}}`, `{{command::name::argument::argument}}` | Was ein [Befehl](../commands/index.md) als Text antwortet. Verwendbar ist nur ein Befehl, der für Makros gemacht ist (er liest nur; eine Erweiterung sagt das, etwa `{{command::character-tracker-map}}` des [Character Tracker](../extensions/character-tracker.md)); jeder andere Name gibt `Error` aus. Ein Befehl, der fehlschlägt, gibt nichts aus. |

Der Befehl läuft, wenn die Generierung das Makro erreicht (gar nicht, wenn es in einem nicht gewählten Zweig eines `{{if}}` steht), für das Buch, das generiert wird, und nur in den Prompts einer Generierung (dem Benutzer-Prompt, den Lorebook-Einträgen und der Master-Vorlage); an anderer Stelle gibt das Makro nichts aus. Die Antwort wird bei dem Teil gehalten, an den der neue Teil anschließt: Die nächste Generierung aus demselben Teil und die anderen Vorlagen derselben Generierung verwenden sie, statt den Befehl erneut auszuführen. **Neu generieren** und **swipe** fragen den Befehl erneut ab, ebenso jeder neue Teil, da er an einen neuen letzten Teil anschließt.

<a id="utility"></a>

## Hilfsfunktionen

| Makro | Ergebnis |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Ein oder mehrere Zeilenumbrüche. |
| `{{space}}`, `{{space::4}}` | Ein oder mehrere Leerzeichen. |
| `{{trim}}` | Entfernt die Zeilenumbrüche um es herum. Nützlich nach Zeilen, die nur Variablen setzen. |
| `{{noop}}` | Nichts. |
| `{{join::separator::a::b::c}}` | Die Texte nach dem ersten, verbunden mit dem ersten als Trenner dazwischen, beliebig viele davon (Makros eingeschlossen): `{{join::-::{{user}}::{{scenario}}}}`. Leere Texte werden weggelassen, sodass ein optionales Makro keinen überflüssigen Trenner hinterlässt. Die Leerzeichen um ein geschriebenes Argument werden entfernt; für einen Trenner mit Leerzeichen verwenden Sie daher ein Makro: `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::text}}` | Der Text rückwärts. |
| `{{// comment}}` | Ein Kommentar, wird entfernt. `{{//}}` ... `{{///}}` kommentiert alles dazwischen aus, Makros eingeschlossen. |
| `\{\{user\}\}` | Gibt `{{user}}` wörtlich aus. |

<a id="sillytavern-only-macros"></a>

## Nur für SillyTavern gedachte Makros

Makros, die nur in SillyTavern-Chats Sinn ergeben – Personas und Felder der Charakterkarte (`{{persona}}`, `{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), Sequenzen des Instruct-Modus (`{{instructUserPrefix}}`...), Autorennotizen, Swipe-IDs, `{{outlet}}` und ähnliche –, werden erkannt und geben **nichts** aus, damit importierte Welteninformationen sie nicht in den Prompt einschleusen. `{{isMobile}}` und `{{hasExtension}}` geben `false` aus.

Ein Makro, das Marginalia nicht kennt:

- mit Argumenten (`{{fooBar::x}}`) wird es unverändert ausgegeben, wie SillyTavern es tut;
- ohne Argumente (`{{fooBar}}`) wird es als **`Error`** ausgegeben, wie eine unbekannte [Template-Variable](templates.md#typos).

}%}
