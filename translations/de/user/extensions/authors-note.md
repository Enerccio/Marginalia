---
label: Autorennotiz
order: 23
---

# Autorennotiz

Eine Notiz an das Modell, die bei jeder Generierung des Buches mitgeschickt wird: der Ton, den du gerade möchtest, worum es im aktuellen Kapitel geht, eine Erinnerung, die das Modell immer wieder vergisst. Anders als die Anweisungen eines Zuges bleibt die Notiz bestehen, bis du sie änderst.

<a id="the-authors-note-tab"></a>

## Der Reiter Autorennotiz

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) erhält neben *Gliederung* einen Reiter **Autorennotiz**. Änderungen werden beim Tippen im Buch gespeichert und bei der nächsten Generierung verwendet.

![Der Reiter Autorennotiz](../../images/plugin-authorsnote.png)

| Feld | |
|---|---|
| **In Prompt einfügen** | Schaltet die Notiz ein oder aus, ohne sie zu löschen. |
| **Einfügetiefe** | Wo die Notiz eingefügt wird, gezählt in Nachrichten vom Ende des Prompts: `0` - nach den Anweisungen des Zuges (die allerletzte Nachricht), `1` - direkt davor, `2` - vor dem letzten Storyteil, und so weiter. Eine große Tiefe setzt die Notiz direkt hinter den System-Prompt. |
| **Rolle** | Ob die Notiz als Nachricht *System*, *Benutzer* oder *Assistent* gesendet wird. |
| **Autorennotiz** | Der Text, der an das Modell gesendet wird. Eine leere Notiz wird nicht gesendet. Darunter eine Schätzung ihrer Größe in Tokens. |
| **Autorennotiz (privat)** | Notizen für dich selbst - Pläne, Erinnerungen. Werden nie an das Modell gesendet. |

!!!
Je näher die Notiz am Ende des Prompts steht, desto stärker wirkt sie. Tiefe `1` mit der Rolle *System* ist ein guter Ausgangspunkt.
Die Notiz wird nicht als [Vorlage](../templates/macros.md) verarbeitet: Makros werden so gesendet, wie sie geschrieben sind.
!!!

<a id="how-the-prompt-looks"></a>

## So sieht der Prompt aus

Eine Generierung sendet zuerst den System-Prompt, dann die Story als Chat - jeder Teil als Antwort des Modells auf *Story generieren* - und zuletzt die Anweisungen des Zuges. Bei Tiefe `1` wird die Notiz direkt vor den Anweisungen eingefügt:

1. System-Prompt (Vorlage, Lore, Zusammenfassungen)
2. Storyteile…
3. **die Autorennotiz**
4. die Anweisungen des Zuges

Die Tokens der Notiz werden vom Platz für die Story abgezogen: Eine längere Notiz bedeutet weniger Storyteile im Prompt. Reicht eine Notiz nicht einmal für den übrigen Prompt aus, bricht die Generierung mit *Kontextgrenze nicht ausreichend* ab.

<a id="the-note-as-a-macro"></a>

## Die Notiz als Makro

Statt der automatischen Einfügung (oder zusätzlich dazu) kannst du die Notiz selbst platzieren, mit dem [Makro {%{`{{command::authors-note}}`}%}](../templates/macros.md#commands): Setze es in die Hauptvorlage, den Benutzer-Prompt oder einen Lore-Eintrag, und es gibt den Text der Notiz dort aus, wo du ihn im Prompt haben willst. Es gibt die Notiz so aus, wie sie jetzt gespeichert ist, unabhängig davon, ob **In Prompt einfügen** eingeschaltet ist. Schalte es daher aus, wenn du die Notiz über das Makro platzierst, sonst erhält das Modell sie zweimal. Die Tiefe und die Rolle gelten für das Makro nicht (die Notiz ist Teil der Nachricht, die das Makro enthält), und eine leere Notiz gibt nichts aus.

<a id="storage"></a>

## Speicherung

Die Notiz wird zusammen mit dem Buch gespeichert und ist damit Teil der [Buchsicherungen](../books/backups.md) und Kopien.
