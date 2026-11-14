---
label: Buchprompts
order: 70
---

# Buchprompts

Der Reiter **Prompts** eines Buches enthält die Texte, aus denen Marginalia den Prompt zusammensetzt: zwei Vorlagen, die erzählerischen Einstellungen sowie die Prompts für Zusammenfassungen und Überarbeitungen. Sie bestimmen die Stimme des Buches weit mehr als die Modelleinstellungen.

![Der Reiter Prompts](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Standardwerte und Buchwerte

Jedes Feld ist optional. Ein leeres Feld wird vererbt: vom nächsten [Ordner](folders.md) über dem Buch, der es setzt, sonst vom Standardwert unter **Einstellungen → Vorlagen** (siehe [Konto & Einstellungen](../account.md)), und wenn auch dieser leer ist, vom eingebauten Standard in der [Prompt-Sprache](../account.md#prompt-language) deiner Einstellungen. Der geerbte Wert wird im leeren Feld grau angezeigt.

Lege also fest, was alle deine Bücher gemeinsam haben, unter *Einstellungen*, was eine Reihe gemeinsam hat, in ihrem Ordner, und nur was für ein Buch besonders ist, auf dessen Reiter *Prompts*. Änderungen werden beim Tippen gespeichert.

<a id="inherited-settings"></a>

### Geerbte Einstellungen

Unter einem leeren Feld steht eine Zeile, die angibt, woher der Wert stammt, damit klar ist, warum das Buch so schreibt, wie es schreibt:

- *Vom Ordner „Sci-fi“ geerbt* - ein [Ordner](folders.md#where-the-settings-of-a-book-come-from) setzt den Wert (der nächste Ordner über dem Buch wird genannt),
- *Aus deinen Einstellungen geerbt* - der Wert unter *Einstellungen → Vorlagen*,
- *Eingebauter Standard* - nichts setzt den Wert.

![Der Reiter Prompts eines Buches in einem Ordner](../../images/book-prompts-inherited.png)

Dieselben Felder gibt es in den Einstellungen eines Ordners, mit denselben Zeilen. Ein leerer Text gilt als nicht gesetzt. Da die Suche erfolgt, wenn der Prompt gebaut wird, wirkt sich das Verschieben des Buches in einen anderen Ordner oder das Ändern des Ordners auf den nächsten Teil aus, den du generierst.

<a id="settings-of-extensions"></a>

### Einstellungen von Erweiterungen

Eine [Erweiterung](../extensions/index.md) kann Einstellungen hinzufügen, die Bücher und Ordner auf die gleiche Weise erben (zum Beispiel das Medium, als das ein Chat über das Buch es behandelt). Sie stehen unter dem Abschnitt **Einstellungen der Erweiterung** unterhalb der Prompts, im Reiter *Prompts* eines Buches und in den Einstellungen eines Ordners, je Erweiterung ein **einklappbares Feld** (alle geschlossen, außer dem ersten, in dem ein Wert gesetzt ist, damit ein eingetragener Wert nicht verborgen bleibt). Sie funktionieren wie die Felder oben: Ein leeres Feld wird zuerst von den Ordnern über dem Buch geerbt, dann von den eigenen Einstellungen der Erweiterung unter *Einstellungen → Einstellungen der Erweiterung*, dann von deren Standard, und die Zeile unter dem Feld zeigt, woher der Wert kommt. Die Vorlagen der [Exportformate](export.md#title-page-template) (Titelseite, Einleitung, Urheberhinweis) stehen im selben Abschnitt, je Format ein Feld. Ein Wert, den du für eine Erweiterung setzt, bleibt beim Buch oder Ordner erhalten, auch wenn die Erweiterung nicht geladen ist.

Jedes Feld hat ein Hinweis-Popover mit **Standardvorlage einfügen**, das den Standard in das Feld kopiert, als Ausgangspunkt für deine eigene Version. Die Vorlagenfelder listen außerdem die **Verfügbaren Vorlagenvariablen** und die **Verfügbaren Makros** auf.

![Das Hinweis-Popover eines Vorlagenfelds](../../images/template-hints.png)

<a id="the-fields"></a>

## Die Felder

| Feld | Verwendet in | |
|---|---|---|
| **Hauptvorlage** | Systemnachricht | Der Rahmen des Prompts: die Rolle des Modells und die Stellen, an denen Lore, erzählerische Einstellungen und Zusammenfassungen stehen. Eine Vorlage. |
| **Erzählperspektive (POV)** | Hauptvorlage | Die erzählerische Stimme, z. B. *Ich-Erzähler*, *Dritte Person eingeschränkt*. Reiner Text. |
| **Zeitform** | Hauptvorlage | z. B. *Vergangenheit*, *Präsens*. Reiner Text. |
| **Stil** | Hauptvorlage | Richtlinien für Stil und Ton der Prosa. Reiner Text. |
| **Standard-Benutzerprompt** | letzte Nachricht | Die Anfrage für den nächsten Teil, aufgebaut aus den [Anweisungen für den Zug](story-editor.md#writing-the-next-part). Eine Vorlage. |
| **Standard-Zusammenfassungsprompt** | Zusammenfassungen | Die Anfrage für eine [Zusammenfassung](summaries.md). Eine Vorlage. |
| **Standard-Metazusammenfassungsprompt** | Metazusammenfassungen | Die Anfrage für eine [Metazusammenfassung](summaries.md#meta-summaries), die Zusammenfassungen zusammenführt. Eine Vorlage. |
| **Systemprompt für Überarbeitungen** | Überarbeitungen | Die Systemnachricht einer [Überarbeitung](story-editor.md#redrafting-a-part): die Anweisungen, die Erzählung und die Zusammenfassungen. Eine Vorlage. |
| **Überarbeitungsprompt** | Überarbeitungen | Die letzte Nachricht einer Überarbeitung, die eigentliche Anfrage: die Lore, die die Lorebooks dem Teil jetzt geben (so erreicht eine Änderung eines Eintrags die Überarbeitung; die Lore, mit der der Teil geschrieben wurde, nur, wenn das Buch kein Lorebook hat), die Geschichte nach dem Teil, der Teil und die Anweisungen. Eine Vorlage. |
| **Prompt zum Kürzen beim Überarbeiten**, **Prompt zum Verlängern beim Überarbeiten** | Überarbeitungen | Der Satz, der nach deinen Anweisungen angefügt wird, wenn das Fenster der [Überarbeitung](story-editor.md#redrafting-a-part) auf **Kürzer** oder **Länger** gesetzt ist. Reiner Text, keine Vorlage. Leer bedeutet der eingebaute Text. |
| **Systemprompt für Anmerkungen** | Anmerkungen | Die Systemnachricht einer Anfrage, einen Teil zu [kommentieren](annotations.md#letting-a-model-annotate). Eine Vorlage. |
| **Annotationsprompt** | Anmerkungen | Die letzte Nachricht der Anfrage zum Kommentieren. Eine Vorlage. |
| **Anweisungen für Anmerkungen** (*Worauf geachtet werden soll*) | Anmerkungen | Worauf das Modell beim Kommentieren achtet (*Kontinuitätsfehler markieren*, *schwache Dialoge*), der Text von `{%{{{annotateInstructions}}}%}` im Annotationsprompt. Reiner Text. Setze ihn hier, in einem Ordner oder unter *Einstellungen → Vorlagen*, dann beginnt das Fenster *Mit KI kommentieren* damit. |
| **Impersonate-Prompt** | Anweisungen für den Zug | Die Anfrage an ein Modell, die [Anweisungen](story-editor.md#drafting-the-instructions) für den nächsten Zug an deiner Stelle zu entwerfen. Eine Vorlage. |

*Erzählperspektive*, *Zeitform* und *Stil* werden unverändert in die Hauptvorlage eingefügt; Makros funktionieren in ihnen nicht.

Eine Vorlage mit einem Syntaxfehler wird nicht gespeichert; eine Warnung zeigt den Fehler an.

<a id="templates"></a>

## Vorlagen

Vorlagen sind Text mit Platzhaltern in doppelten geschweiften Klammern. Jede Vorlage hat ihre eigenen Variablen:

| Vorlage | Variablen |
|---|---|
| Hauptvorlage | `{%{{{backgroundLore}}}%}` (aktivierte Lorebook-Einträge), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (die Notizen, die du dem Modell gegeben hast) |
| Benutzerprompt | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Zusammenfassungsprompt | `{%{{{backgroundLore}}}%}` (die Lore, die die Lorebooks dem Teil jetzt geben), `{%{{{text}}}%}` (die zusammenzufassenden Teile) |
| Metazusammenfassungsprompt | `{%{{{backgroundLore}}}%}` (wie oben), `{%{{{summaryBlocks}}}%}` (die zusammenzuführenden Zusammenfassungen) |
| Systemprompt für Überarbeitungen, Überarbeitungsprompt | `{%{{{text}}}%}` (der Teil), `{%{{{redraftInstructions}}}%}` (was geändert werden soll), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, sowie der Zug, aus dem der Teil geschrieben wurde: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, und `{%{{{annotations}}}%}` (deine offenen Notizen zu dem Teil, leer, außer du forderst sie im Überarbeitungsfenster an), und für die [Überarbeitung einer Auswahl](story-editor.md#redrafting-only-a-passage) `{%{{{selection}}}%}` (der ausgewählte Text; leer, wenn der ganze Teil überarbeitet wird), `{%{{{selectionBefore}}}%}` und `{%{{{selectionAfter}}}%}` (der Text des Teils um die Auswahl herum) |
| Impersonate-Prompt | `{%{{{impersonateInstructions}}}%}` (deine Anleitung), `{%{{{outline}}}%}` (der Plan der Geschichte, den ein Aufrufer übergibt, z. B. das aktuelle Kapitel des Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (die Lore des letzten Teils), `{%{{{instructionExamples}}}%}` (deine Anweisungen der letzten drei Teile, die ältesten zuerst), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, sowie der Zug, für den die Anweisungen gelten: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Systemprompt für Anmerkungen, Annotationsprompt | Die Variablen der Überarbeitungsprompts, mit `{%{{{annotateInstructions}}}%}` (was angesehen werden soll) anstelle von `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (die Notizen, die der Teil bereits hat), `{%{{{facts}}}%}` (was Erweiterungen über die Geschichte an dieser Stelle wissen, zum Beispiel was die Figuren wissen; leer ohne eine solche Erweiterung) und `{%{{{outputFormat}}}%}` (wie das Modell angewiesen wird zu antworten) |

Ein Abschnitt gibt seinen Inhalt nur aus, wenn die Variable nicht leer ist, so enthält der Prompt keine leeren Überschriften:

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` innerhalb eines Abschnitts ist der Wert der Variablen. Bei leerem *Schauplatz der Szene* entfällt die ganze Zeile.

Alle Vorlagen können außerdem die SillyTavern-kompatiblen **Makros** verwenden, etwa `{%{{{user}}}%}` (die Figur der Erzählperspektive), `{%{{{description}}}%}` (die Beschreibung des Buches), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` und `{%{{{setvar::name::value}}}%}`. Die vollständige Syntax und die Makroreferenz stehen unter [Vorlagen & Makros](../templates/index.md).

<a id="master-template"></a>

## Hauptvorlage

Die Hauptvorlage wird zur **Systemnachricht**, der ersten Nachricht des Prompts. Der eingebaute Standard hat diese Blöcke:

1. **Systemrolle** - das Modell ist ein Autor, der die Geschichte fortsetzt.
2. **Welt- & Lore-Kontext** - die aktivierten [Lorebook](../lorebooks/index.md)-Einträge (`backgroundLore`).
3. **Schreibstil & Regeln** - Erzählperspektive, Zeitform, Stil und allgemeine Regeln für die Prosa.
4. **Zusammenfassung der bisherigen Geschichte** - die [Zusammenfassungen](summaries.md).
5. **Autorennotizen zur Geschichte** - nur, wenn du dem Modell [Notizen gegeben](annotations.md#giving-a-note-to-the-model) hast.

Ist beim [Inferenzanbieter](../inference-providers.md#general-settings) ein Jailbreak-Prompt aktiviert, steht er vor der Hauptvorlage.

<a id="user-prompt"></a>

## Benutzerprompt

Der Benutzerprompt ist die **letzte Nachricht** des Prompts: die Anfrage nach dem zu schreibenden Teil. Er wird aus den *Anweisungen für den neuen Zug* gefüllt (oder aus den *Details des Zugs* bei *Neu generieren* und *Variante*). Der eingebaute Standard fordert den nächsten Abschnitt mit der Figur der Erzählperspektive, dem Ort, den anwesenden Figuren und den Handlungsanweisungen an, gefolgt von Regeln: keine neuen Figuren, außer wenn verlangt, den Anweisungen folgen, die Kontinuität wahren und nur den Erzähltext ausgeben.

Lorebook-Einträge mit dem Einfügemodus *Vor dem Benutzerprompt* werden unmittelbar davor eingefügt; siehe [Einträge & Aktivierung](../lorebooks/entries.md).
## Redraft-Prompt

Die [Überarbeitung](story-editor.md#redrafting-a-part) eines Teils ist eine einzige Anfrage aus zwei Prompts: Der **Systemprompt für Überarbeitungen** ist die
Systemnachricht, die Geschichte vor dem Teil folgt ihr als frühere Nachrichten der Unterhaltung (die Teile als Antworten des
Modells, so wie eine Generierung sie sendet), und der **Überarbeitungsprompt** ist die letzte Nachricht des Benutzers. Der
eingebaute Standard weist das Modell an, deine Anweisungen umzusetzen, das beizubehalten, was du nicht zu ändern gebeten hast, mit der Geschichte vor und nach dem Teil konsistent zu bleiben,
die Stimme beizubehalten und nur den überarbeiteten Teil zu antworten. Beide Prompts haben dieselben Variablen; `{%{{{previousStory}}}%}` ist nur ein
Hinweis, dass die Geschichte als Nachrichten folgt. Behalte im Systemprompt, was sich von einer Überarbeitung zur nächsten nicht ändert, und im
letzten Prompt, was sich ändert (das Lore, das nicht für jeden Teil gleich ist, die Geschichte nach dem Teil, den Teil, die Anweisungen): Ein Server, der
den gelesenen Prompt speichert, kann dann den größten Teil davon wiederverwenden. Deine eigene Version des letzten Prompts sollte die Variablen behalten, die
sie braucht: Ohne `{%{{{text}}}%}` sieht das Modell den Teil nicht. Für jeden Prompt gilt die Regel aller Prompts: das Feld des Buches, sonst jenes in *Einstellungen → Vorlagen*, sonst der eingebaute.

Wenn du nur eine [markierte Textstelle](story-editor.md#redrafting-only-a-passage) überarbeitest, wird derselbe Prompt mit
`{%{{{selection}}}%}` gefüllt verwendet: Der eingebaute Standard zeigt dann den Teil mit der Textstelle zwischen `[[PASSAGE TO REDRAFT]]` und
`[[/PASSAGE TO REDRAFT]]` und weist das Modell an, nur den Text zu antworten, der die Textstelle ersetzt. Ein eigener Prompt,
der `{%{{{selection}}}%}` nicht kennt, führt dazu, dass das Modell den ganzen Teil antwortet, und die ganze Antwort ersetzt dann die Auswahl;
ergänze einen `{%{{#selection}}%}...{%{{{/selection}}}%}`-Block (und `{%{{{^selection}}}%}` für den anderen Fall), um das zu behandeln.

<a id="redraft-shorter-and-longer"></a>

## Kürzer und länger überarbeiten

Die Auswahl **Länge** im Fenster der [Überarbeitung](story-editor.md#redrafting-a-part) (**Gleiche Länge**, **Kürzer**, **Länger**) verändert die
Überarbeitungsprompts nicht: Bei **Kürzer** wird der Text von **Prompt zum Kürzen beim Überarbeiten** (bei **Länger** der von **Redraft Longer
Prompt**) nach dem, was du in *Anweisungen für die Überarbeitung* geschrieben hast, an der Stelle von `{%{{{redraftInstructions}}}%}` angehängt; so funktioniert es auch mit einem eigenen Überarbeitungsprompt. Bei leeren Anweisungen ist er die einzige Anweisung. Die beiden Texte folgen der Regel aller Prompts: das Feld des Buches, sonst das nächste Ordnerfeld darüber, das eines setzt, sonst jenes in *Einstellungen → Vorlagen*, sonst der eingebaute Text, der etwa ein Drittel bis die Hälfte weniger verlangt (bei Länger: die Hälfte mehr bis doppelt so lang), ohne am Geschehen etwas zu ändern.

<a id="annotate-prompt"></a>

## Anmerkungsprompt

Wenn ein Modell einen [Teil kommentiert](annotations.md#letting-a-model-annotate), erhält es dieselbe Art Anfrage wie eine Überarbeitung: den
**Systemprompt für Anmerkungen** als Systemnachricht, die Geschichte vor dem Teil als frühere Nachrichten und den **Annotationsprompt** als
letzte Nachricht. Der eingebaute Standard macht das Modell zu einem Lektor: Es findet die Stellen, die im Sinne deiner Anweisungen ein Problem haben,
zitiert jede davon buchstabengetreu und schreibt dir eine kurze Notiz, ohne Lob oder Füllwörter und ohne die Notizen zu wiederholen, die der Teil bereits hat.
Wenn eine Erweiterung [Fakten über die Geschichte](annotations.md#letting-a-model-annotate) liefert (der
Knowledge Tracker: was die Figuren wissen), zeigt der Standardprompt sie als *Etablierte Fakten* an, und der Lektor markiert alles, was ihnen widerspricht.
Der Prompt hat die Variable `{%{{{outputFormat}}}%}` an der Stelle, wo die Art der Antwort beschrieben wird (JSON, Textblöcke oder eine freie Besprechung, je nach Antwortformat, das du wählst); behalte sie in einem eigenen Prompt, sonst wird dem Modell nicht gesagt, wie es antworten soll. Beide Annotationsprompts folgen der Regel aller Prompts: das Feld des Buches, sonst jenes in
*Einstellungen → Vorlagen*, sonst der eingebaute.

**Worauf geachtet werden soll** (*Anweisungen für Anmerkungen*) folgt derselben Regel, so kannst du es einmal festlegen: in *Einstellungen → Vorlagen* für alle deine Bücher, in einem Ordner für die Bücher darin oder im Tab *Prompts* für ein Buch. Das Fenster *Mit KI kommentieren* zeigt, was gilt, als graue Schrift in seinem Feld und sagt, woher es stammt. Lass das Feld leer, um es zu verwenden, schreibe etwas hinein, um es für diesen Lauf zu ersetzen, oder klicke auf das Info-Symbol des Feldes und dann auf **Fill default**, um das Geltende in das Feld zu kopieren und es für diesen Lauf zu bearbeiten. Ist nirgends etwas gesetzt, gilt die Standardaufgabe des Prompts. Dasselbe gilt für die automatische Annotation und den Befehl `/generate-annotation`.

## Impersonate-Prompt

Wenn ein Modell die Anweisungen des nächsten Zugs [entwirft](story-editor.md#drafting-the-instructions), erhält es eine Anfrage, die als einzige Nachricht gesendet wird, wie bei der Überarbeitung. Der eingebaute Standard bittet das Modell, das zu schreiben, was du in *Anweisungen* eintippen würdest: eine Handlungsvorgabe für den nächsten Teil (was geschieht, wer was tut, wo der Teil endet), die deine Vorgabe weiterentwickelt und dem Plan folgt, die Geschichte dort fortsetzt, wo sie endete, so geschrieben ist wie deine eigenen früheren Anweisungen und nicht der Text selbst ist. Ohne Vorgabe setzt es die Geschichte dort fort, wohin sie führt. Deine Vorgabe steht dort, wo der Prompt `{%{{{impersonateInstructions}}}%}` einsetzt; behalte sie in einem eigenen Prompt. Der Impersonate-Prompt folgt der Regel aller Prompts: das Feld des Buches, sonst der *Impersonate-Prompt* in *Einstellungen → Vorlagen*, sonst der eingebaute.

<a id="how-the-prompt-is-put-together"></a>

## So wird der Prompt zusammengesetzt

```
system     jailbreak (if enabled) + master template
user       [ Generate story. ]
assistant  part #1
user       [ Generate more story. ]
assistant  part #2
...        (as many earlier parts as fit)
user       lore "Before User Prompt" + user prompt
```

Frühere Teile werden als eigene Antworten des Modells gesendet, sodass es sie im gleichen Stil fortsetzt. Ist die Geschichte länger als das [Kontextlimit](../protocols.md#limits), werden die ältesten Teile zuerst weggelassen; [Zusammenfassungen](summaries.md) halten sie in verdichteter Form in der Geschichte.

**Prompt anzeigen** im Menü eines beliebigen Teils zeigt den genauen Prompt, aus dem dieser Teil geschrieben wurde. **Prompts vergleichen** stellt ihn neben den Prompt eines früheren Teils und zeigt, wo sie zuerst voneinander abweichen; dort endet der [Prompt-Cache](../prompt-cache.md#compare-prompt) eines Servers.

<a id="tips"></a>

## Tipps

- Lege Regeln, die für jeden Teil gelten (keine neuen Figuren, Länge, Formatierung), in den Benutzerprompt: Da er die letzte Nachricht ist, wirkt er am stärksten.
- Lege dauerhafte Fakten über Welt und Figuren ins [Lorebook](../lorebooks/index.md), nicht in *Stil*.
- Verlange eine Länge in den Anweisungen („etwa 800 Wörter“), statt dich auf das Antwortlimit zu verlassen, das den Text abschneidet.
- Prüfe nach einer Änderung an einer Vorlage einen generierten Teil mit **Prompt anzeigen**.
