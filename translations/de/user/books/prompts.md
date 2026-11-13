---
label: Buchprompts
order: 70
---

# Buchprompts

Der Reiter **Prompts** eines Buchs enthält die Texte, aus denen Marginalia den Prompt zusammensetzt: zwei Vorlagen, die erzählerischen Einstellungen sowie die Prompts für Zusammenfassungen und Überarbeitungen. Sie bestimmen die Stimme des Buchs weit mehr als die Modelleinstellungen.

![Der Reiter Prompts](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Standardwerte und Buchwerte

Jedes Feld ist optional. Ein leeres Feld wird geerbt: vom nächsten [Ordner](folders.md) über dem Buch, der es setzt, sonst vom Standard unter **Einstellungen → Vorlagen** (siehe [Konto & Einstellungen](../account.md)), und wenn auch dieser leer ist, vom eingebauten Standard in der [Prompt-Sprache](../account.md#prompt-language) deiner Einstellungen. Der geerbte Wert wird im leeren Feld grau angezeigt.

Lege also fest, was alle deine Bücher gemeinsam haben, unter *Einstellungen*, was eine Reihe gemeinsam hat, in ihrem Ordner, und nur das, was für ein Buch besonders ist, auf seinem Reiter *Prompts*. Änderungen werden gespeichert, während du tippst.

<a id="inherited-settings"></a>

### Geerbte Einstellungen

Unter einem leeren Feld steht eine Zeile, die angibt, woher sein Wert stammt, damit klar ist, warum das Buch so schreibt, wie es schreibt:

- *Geerbt vom Ordner „Sci-fi“* - ein [Ordner](folders.md#where-the-settings-of-a-book-come-from) setzt den Wert (der nächste Ordner über dem Buch wird genannt),
- *Geerbt aus deinen Einstellungen* - der Wert aus *Einstellungen → Vorlagen*,
- *Eingebauter Standard* - nichts setzt den Wert.

![Der Reiter Prompts eines Buchs in einem Ordner](../../images/book-prompts-inherited.png)

Dieselben Felder gibt es in den Einstellungen eines Ordners, mit denselben Zeilen. Ein leerer Text gilt als nicht gesetzt. Da die Suche erst beim Erstellen des Prompts erfolgt, wirkt sich das Verschieben des Buchs in einen anderen Ordner oder das Ändern des Ordners auf den nächsten Teil aus, den du generierst.

<a id="settings-of-extensions"></a>

### Einstellungen von Erweiterungen

Eine [Erweiterung](../extensions/index.md) kann Einstellungen hinzufügen, die Bücher und Ordner auf dieselbe Weise erben (zum Beispiel das Medium, als das ein Chat über das Buch es behandelt). Sie stehen unter dem Abschnitt **Extension Settings** unterhalb der Prompts, im Reiter *Prompts* eines Buchs und in den Einstellungen eines Ordners, mit je einem **einklappbaren Feld pro Erweiterung** (alle geschlossen, außer dem ersten, in dem etwas gesetzt ist, damit ein eingetragener Wert nicht verborgen bleibt). Sie funktionieren wie die Felder oben: Ein leeres Feld wird zuerst von den Ordnern über dem Buch geerbt, dann von den eigenen Einstellungen der Erweiterung unter *Einstellungen → Extension Settings*, dann von ihrem Standard, und die Zeile unter dem Feld sagt, welcher gilt. Die Vorlagen der [Exportformate](export.md#title-page-template) (Titelseite, Vorspann, Urheberhinweis) stehen im selben Abschnitt, mit je einem Feld pro Format. Ein Wert, den du für eine Erweiterung setzt, bleibt beim Buch oder Ordner erhalten, auch wenn die Erweiterung nicht geladen ist.

Jedes Feld hat ein Hinweis-Popover mit **Standardvorlage einfügen**, das den Standard in das Feld kopiert, als Ausgangspunkt für deine eigene Version. Die Vorlagenfelder listen außerdem die **Verfügbaren Vorlagenvariablen** und die **Verfügbaren Makros** auf.

![Das Hinweis-Popover eines Vorlagenfelds](../../images/template-hints.png)

<a id="the-fields"></a>

## Die Felder

| Feld | Verwendet in | |
|---|---|---|
| **Hauptvorlage** | Systemnachricht | Der Rahmen des Prompts: die Rolle des Modells und die Stellen, an denen Lore, erzählerische Einstellungen und Zusammenfassungen stehen. Eine Vorlage. |
| **Erzählperspektive (POV)** | Hauptvorlage | Die erzählerische Stimme, z. B. *First person*, *Third-Person Limited*. Reiner Text. |
| **Zeitform** | Hauptvorlage | z. B. *Past Tense*, *Present Tense*. Reiner Text. |
| **Stil** | Hauptvorlage | Richtlinien für Stil und Ton der Prosa. Reiner Text. |
| **Default User Prompt** | letzte Nachricht | Die Anfrage für den nächsten Teil, aufgebaut aus den [Anweisungen für den Zug](story-editor.md#writing-the-next-part). Eine Vorlage. |
| **Standard-Zusammenfassungsprompt** | Zusammenfassungen | Die Anfrage für eine [Zusammenfassung](summaries.md). Eine Vorlage. |
| **Standard-Metazusammenfassungsprompt** | Metazusammenfassungen | Die Anfrage für eine [Metazusammenfassung](summaries.md#meta-summaries), die Zusammenfassungen zusammenführt. Eine Vorlage. |
| **Systemprompt für Überarbeitungen** | Überarbeitungen | Die Systemnachricht einer [Überarbeitung](story-editor.md#redrafting-a-part): die Anweisungen, die Erzählung und die Zusammenfassungen. Eine Vorlage. |
| **Überarbeitungsprompt** | Überarbeitungen | Die letzte Nachricht einer Überarbeitung, die Anfrage selbst: die Lore, die die Lorebooks dem Teil jetzt zuordnen (damit eine Änderung an einem Eintrag die Überarbeitung erreicht; die Lore, mit der der Teil geschrieben wurde, nur, wenn das Buch kein Lorebook hat), die Geschichte nach dem Teil, den Teil und die Anweisungen. Eine Vorlage. |
| **Prompt zum Kürzen beim Überarbeiten**, **Prompt zum Verlängern beim Überarbeiten** | Überarbeitungen | Der Satz, der nach deinen Anweisungen angefügt wird, wenn das Fenster der [Überarbeitung](story-editor.md#redrafting-a-part) auf **Kürzer** oder **Länger** gestellt ist. Reiner Text, keine Vorlage. Leer bedeutet den eingebauten Text. |
| **Systemprompt für Anmerkungen** | Kommentieren | Die Systemnachricht einer Anfrage an ein Modell, einen Teil zu [kommentieren](annotations.md#letting-a-model-annotate). Eine Vorlage. |
| **Annotationsprompt** | Kommentieren | Die letzte Nachricht der Anfrage zum Kommentieren. Eine Vorlage. |
| **Annotate Instructions** (*Worauf geachtet werden soll*) | Kommentieren | Worauf das Modell beim Kommentieren achtet (*Kontinuitätsfehler markieren*, *schwache Dialoge*), der Text von `{%{{{annotateInstructions}}}%}` im Annotationsprompt. Reiner Text. Setze ihn hier, in einem Ordner oder unter *Einstellungen → Vorlagen*, dann beginnt das Fenster *Mit KI kommentieren* damit. |
| **Impersonate-Prompt** | Entwurf der Anweisungen | Die Anfrage an ein Modell, die [Anweisungen](story-editor.md#drafting-the-instructions) des nächsten Zugs an deiner Stelle zu entwerfen. Eine Vorlage. |

*Erzählperspektive*, *Zeitform* und *Stil* werden unverändert in die Hauptvorlage eingesetzt; Makros funktionieren in ihnen nicht.

Eine Vorlage mit einem Syntaxfehler wird nicht gespeichert; eine Warnung zeigt den Fehler an.

<a id="templates"></a>

## Vorlagen

Vorlagen sind Text mit Platzhaltern in doppelten geschweiften Klammern. Jede Vorlage hat ihre eigenen Variablen:

| Vorlage | Variablen |
|---|---|
| Hauptvorlage | `{%{{{backgroundLore}}}%}` (aktivierte Lorebook-Einträge), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (die Notizen, die du dem Modell gegeben hast) |
| Benutzerprompt | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Zusammenfassungsprompt | `{%{{{backgroundLore}}}%}` (die Lore, die die Lorebooks dem Teil jetzt zuordnen), `{%{{{text}}}%}` (die zusammenzufassenden Teile) |
| Metazusammenfassungsprompt | `{%{{{backgroundLore}}}%}` (wie oben), `{%{{{summaryBlocks}}}%}` (die zusammenzuführenden Zusammenfassungen) |
| Systemprompt für Überarbeitungen, Überarbeitungsprompt | `{%{{{text}}}%}` (der Teil), `{%{{{redraftInstructions}}}%}` (was du geändert haben willst), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, und der Zug, aus dem der Teil geschrieben wurde: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, sowie `{%{{{annotations}}}%}` (deine offenen Notizen zum Teil, leer, sofern du sie nicht im Überarbeitungsfenster anforderst), und für die [Überarbeitung einer Auswahl](story-editor.md#redrafting-only-a-passage) `{%{{{selection}}}%}` (der markierte Text; leer, wenn der ganze Teil überarbeitet wird), `{%{{{selectionBefore}}}%}` und `{%{{{selectionAfter}}}%}` (der Text des Teils um die Auswahl herum) |
| Impersonate-Prompt | `{%{{{impersonateInstructions}}}%}` (deine Anleitung), `{%{{{outline}}}%}` (der Plan der Geschichte, den ein Aufrufer übergibt, z. B. das aktuelle Kapitel des Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (die Lore des letzten Teils), `{%{{{instructionExamples}}}%}` (deine Anweisungen der letzten drei Teile, die ältesten zuerst), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, und der Zug, für den die Anweisungen gelten: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Systemprompt für Anmerkungen, Annotationsprompt | Die Variablen der Überarbeitungsprompts, mit `{%{{{annotateInstructions}}}%}` (was du angesehen haben willst) statt `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (die Notizen, die der Teil bereits hat), `{%{{{facts}}}%}` (was Erweiterungen über die Geschichte an diesem Teil wissen, zum Beispiel, was die Figuren wissen; leer ohne eine solche Erweiterung) und `{%{{{outputFormat}}}%}` (wie das Modell zur Antwort angewiesen wird) |

Ein Abschnitt gibt seinen Inhalt nur aus, wenn die Variable nicht leer ist, damit der Prompt keine leeren Überschriften hat:

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` innerhalb eines Abschnitts ist der Wert der Variablen. Ist *Schauplatz der Szene* leer, wird die ganze Zeile weggelassen.

Alle Vorlagen können außerdem die SillyTavern-kompatiblen **Makros** verwenden, etwa `{%{{{user}}}%}` (die Figur aus der Erzählperspektive), `{%{{{description}}}%}` (die Beschreibung des Buchs), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` und `{%{{{setvar::name::value}}}%}`. Die vollständige Syntax und die Makro-Referenz findest du unter [Vorlagen & Makros](../templates/index.md).

<a id="master-template"></a>

## Hauptvorlage

Die Hauptvorlage wird zur **Systemnachricht**, der ersten Nachricht des Prompts. Der eingebaute Standard hat diese Blöcke:

1. **Systemrolle** - das Modell ist ein Autor, der die Geschichte fortsetzt.
2. **Welt- und Lore-Kontext** - die aktivierten [Lorebook](../lorebooks/index.md)-Einträge (`backgroundLore`).
3. **Schreibstil & Regeln** - Erzählperspektive, Zeitform, Stil und allgemeine Regeln für die Prosa.
4. **Zusammenfassung der bisherigen Geschichte** - die [Zusammenfassungen](summaries.md).
5. **Autorennotizen zur Geschichte** - nur, wenn du dem Modell [Notizen gegeben](annotations.md#giving-a-note-to-the-model) hast.

Ist beim [Inferenzanbieter](../inference-providers.md#general-settings) ein Jailbreak-Prompt aktiviert, wird er vor die Hauptvorlage gesetzt.

<a id="user-prompt"></a>

## Benutzerprompt

Der Benutzerprompt ist die **letzte Nachricht** des Prompts: die Anfrage für den zu schreibenden Teil. Er wird aus den *Anweisungen für den neuen Zug* gefüllt (oder aus den *Details des Zugs* bei *Neu generieren* und *Variante*). Der eingebaute Standard fragt nach dem nächsten Abschnitt mit der Figur aus der Erzählperspektive, dem Schauplatz, den anwesenden Figuren und den Handlungsanweisungen, gefolgt von Regeln: keine neuen Figuren, sofern nicht verlangt, den Anweisungen folgen, die Kontinuität wahren und nur den Text der Geschichte ausgeben.

Lorebook-Einträge mit dem Einfügemodus *Vor dem Benutzerprompt* werden direkt davor gesetzt; siehe [Einträge & Aktivierung](../lorebooks/entries.md).
## Redraft-Prompt

Die [Überarbeitung](story-editor.md#redrafting-a-part) eines Teils ist eine einzige Anfrage aus zwei Prompts: Der **Systemprompt für Überarbeitungen** ist die
Systemnachricht, die Geschichte vor dem Teil folgt ihr als frühere Nachrichten der Unterhaltung (die Teile als Antworten des
Modells, so wie eine Generierung sie sendet), und der **Überarbeitungsprompt** ist die letzte Nachricht des Benutzers. Der
eingebaute Standard weist das Modell an, deine Anweisungen umzusetzen, das beizubehalten, was du nicht zu ändern gebeten hast,
konsistent zur Geschichte vor und nach dem Teil zu bleiben, die Stimme zu wahren und nur den überarbeiteten Teil zu antworten.
Beide Prompts haben dieselben Variablen; `{%{{{previousStory}}}%}` ist nur ein Hinweis, dass die Geschichte als Nachrichten
folgt. Behalte im Systemprompt, was sich von einer Überarbeitung zur nächsten nicht ändert, und im letzten Prompt, was sich ändert
(das Lore, das nicht für jeden Teil dasselbe ist, die Geschichte nach dem Teil, den Teil selbst, die Anweisungen): Ein Server, der
sich den gelesenen Prompt merkt, kann so den größten Teil wiederverwenden. Deine eigene Version des letzten Prompts sollte die
Variablen behalten, die sie braucht: Ohne `{%{{{text}}}%}` sieht das Modell den Teil nicht. Für jeden Prompt gilt die Regel aller Prompts:
das Feld des Buchs, sonst das in *Einstellungen → Vorlagen*, sonst das eingebaute.

Wenn du nur eine [markierte Textstelle](story-editor.md#redrafting-only-a-passage) überarbeitest, wird derselbe Prompt mit
gefülltem `{%{{{selection}}}%}` verwendet: Der eingebaute Standard zeigt dann den Teil mit der Textstelle zwischen `[[PASSAGE TO REDRAFT]]` und
`[[/PASSAGE TO REDRAFT]]` und weist das Modell an, nur den Text zu antworten, der die Textstelle ersetzt. Ein eigener Prompt, der
`{%{{{selection}}}%}` nicht kennt, lässt das Modell den ganzen Teil antworten, und die ganze Antwort ersetzt dann die Markierung;
ergänze einen Block `{%{{#selection}}%}...{%{{{/selection}}}%}` (und `{%{{{^selection}}}%}` für den anderen Fall), um dies abzufangen.

<a id="redraft-shorter-and-longer"></a>

## Überarbeitung kürzer und länger

Die Auswahl **Länge** im Fenster der [Überarbeitung](story-editor.md#redrafting-a-part) (**Gleiche Länge**, **Kürzer**, **Länger**) verändert
die Überarbeitungsprompts nicht: Bei **Kürzer** wird der Text von **Prompt zum Kürzen beim Überarbeiten** (bei **Länger** der von **Redraft Longer
Prompt**) nach dem, was du in *Anweisungen für die Überarbeitung* geschrieben hast, an der Stelle von `{%{{{redraftInstructions}}}%}` angehängt. Das funktioniert also auch mit einem eigenen Überarbeitungsprompt. Bei leeren Anweisungen ist er die einzige Anweisung. Für die beiden Texte gilt die Regel aller Prompts: das Feld des Buchs, sonst der nächstgelegene Ordner darüber, der eines setzt, sonst das in *Einstellungen → Vorlagen*, sonst der eingebaute Text. Dieser verlangt etwa ein Drittel bis die Hälfte weniger (bei Länger: die Hälfte mehr bis doppelt so lang), ohne am Inhalt des Geschehens etwas zu ändern.

<a id="annotate-prompt"></a>

## Annotationsprompt

Wenn ein Modell [einen Teil annotiert](annotations.md#letting-a-model-annotate), erhält es dieselbe Art Anfrage wie eine Überarbeitung: den **Systemprompt für Anmerkungen** als Systemnachricht, die Geschichte vor dem Teil als frühere Nachrichten und den **Annotationsprompt** als letzte Nachricht. Der eingebaute Standard macht das Modell zu einem Lektor: Es findet die Stellen, die im Sinne deiner Anweisungen ein Problem haben, zitiert jede Stelle wortgetreu und schreibt dir eine kurze Notiz, ohne Lob oder Füllwörter und ohne die Notizen zu wiederholen, die der Teil bereits hat. Wenn eine Erweiterung [Fakten über die Geschichte](annotations.md#letting-a-model-annotate) liefert (der Knowledge Tracker: was die Figuren wissen), zeigt der Standardprompt sie als *Established facts* an, und der Lektor markiert alles, was ihnen widerspricht. Der Prompt enthält die Variable `{%{{{outputFormat}}}%}`, an der beschrieben wird, wie geantwortet werden soll (JSON, Textblöcke oder eine freie Besprechung, je nach dem Antwortformat, das du wählst). Behalte sie in einem eigenen Prompt, sonst wird dem Modell nicht mitgeteilt, wie es antworten soll. Für beide Annotationsprompts gilt die Regel aller Prompts: das Feld des Buchs, sonst das in *Einstellungen → Vorlagen*, sonst das eingebaute.

**Worauf geachtet werden soll** (*Annotate Instructions*) folgt derselben Regel, sodass du es einmal festlegen kannst: in *Einstellungen → Vorlagen* für alle deine Bücher, in einem Ordner für die Bücher darin oder im Reiter *Prompts* für ein einzelnes Buch. Das Fenster **Mit KI kommentieren** zeigt, was gilt, als graue Schrift in seinem Feld und sagt, woher es stammt. Lass das Feld leer, um es zu verwenden, schreibe etwas hinein, um es für diesen Lauf zu ersetzen, oder klicke auf das Info-Symbol des Feldes und auf **Fill default**, um das Geltende in das Feld zu kopieren und für diesen Lauf zu bearbeiten. Ist nirgends etwas gesetzt, gilt die Standardaufgabe des Prompts. Dasselbe gilt für die automatische Annotation und den Befehl `/generate-annotation`.

## Impersonate-Prompt

Wenn ein Modell [die Anweisungen für den nächsten Zug entwirft](story-editor.md#drafting-the-instructions), erhält es eine einzige Anfrage, die wie die Überarbeitung als einzige Nachricht gesendet wird. Der eingebaute Standard bittet das Modell, das zu schreiben, was du in *Anweisungen* eintippen würdest: eine Handlungsvorgabe für den nächsten Teil (was geschieht, wer was tut, wo der Teil endet), die deine Vorgaben weiterentwickelt und dem Plan folgt, die Geschichte dort fortsetzt, wo sie endete, so geschrieben ist wie deine eigenen früheren Anweisungen, und nicht der Prosatext selbst ist. Ohne Vorgaben setzt sie die Geschichte fort, wohin sie führt. Deine Vorgabe steht an der Stelle, an die der Prompt `{%{{{impersonateInstructions}}}%}` setzt; behalte sie in einem eigenen Prompt. Der Impersonate-Prompt folgt der Regel aller Prompts: das Feld des Buchs, sonst der *Impersonate-Prompt* in *Einstellungen → Vorlagen*, sonst der eingebaute.

<a id="how-the-prompt-is-put-together"></a>

## Wie der Prompt aufgebaut ist

```
system     Jailbreak (falls aktiviert) + Master-Vorlage
user       [ Generate story. ]
assistant  Teil #1
user       [ Generate more story. ]
assistant  Teil #2
...        (so viele frühere Teile, wie hineinpassen)
user       Lore "Before User Prompt" + Benutzer-Prompt
```

Frühere Teile werden als eigene Antworten des Modells gesendet, sodass es sie im gleichen Stil fortsetzt. Wenn die Geschichte länger
als das [Kontextlimit](../protocols.md#limits) ist, werden zuerst die ältesten Teile weggelassen; [Zusammenfassungen](summaries.md)
halten sie in verdichteter Form in der Geschichte.

**Prompt anzeigen** im Menü jedes Teils zeigt genau den Prompt, aus dem dieser Teil geschrieben wurde. **Prompts vergleichen** stellt ihn neben den Prompt eines früheren Teils und zeigt, wo sie zuerst voneinander abweichen; genau dort endet der [Prompt-Cache](../prompt-cache.md#compare-prompt) eines Servers.

<a id="tips"></a>

## Tipps

- Lege Regeln, die für jeden Teil gelten (keine neuen Figuren, Länge, Formatierung), in den Benutzer-Prompt: Da er die letzte
  Nachricht ist, wirkt er am stärksten.
- Lege dauerhafte Fakten über Welt und Figuren ins [Lorebook](../lorebooks/index.md), nicht in *Stil*.
- Fordere eine Länge in den Anweisungen an („ungefähr 800 Wörter“), statt auf das Antwortlimit zu vertrauen, das den Text abschneidet.
- Prüfe nach jeder Änderung einer Vorlage mit einem generierten Teil und **Prompt anzeigen**.
