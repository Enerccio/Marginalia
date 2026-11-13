---
label: Reviewer
order: 2
---

# Reviewer

Lässt das Modell prüfen, was du geschrieben hast: Leserreaktionen, eine Lektoratskritik, eine Kontinuitätsprüfung – wofür auch immer du es im Prompt verlangst. Prüfungen werden beim Teil gespeichert, neben der Geschichte, und werden nie Teil des Buchs.

![Eine Prüfung eines Geschichtsteils](../../images/plugin-reviewer.png)

<a id="reviewing-a-part"></a>

## Einen Teil prüfen

Das Menü (☰) jedes Teils im [Geschichtseditor](../books/story-editor.md) enthält ein Untermenü **Prüfen**:

| Eintrag | |
|---|---|
| **View / Generate Review** | Öffnet den Prüfdialog. Hat der Teil noch keine Prüfung, wird sofort eine erzeugt. |
| **Advanced Options** | Öffnet die [erweiterten Optionen](#advanced-options) und erzeugt dann eine Prüfung mit ihnen. |
| **Delete Review** | Entfernt alle Prüfungen des Teils. |

Im Prüfdialog:

- **‹** / **›** blättern durch die Prüfungen des Teils; **›** bei der letzten Prüfung erzeugt eine neue;
- **Stop generating** unterbricht die gerade geschriebene Prüfung;
- klicke auf den Text einer Prüfung, um ihn zu bearbeiten; er wird gespeichert, wenn du außerhalb des Feldes klickst;
- *Thinking Process* zeigt die Überlegungen von [Reasoning-Modellen](../inference-providers.md#reasoning-models);
- **Close** schließt den Dialog.

<a id="what-the-model-gets"></a>

## Was das Modell erhält

Standardmäßig verwendet die Prüfung **denselben Prompt, aus dem der Teil geschrieben wurde** (*Use standard prompt info*): den System-Prompt mit Lore und Zusammenfassungen, die früheren Teile und zuletzt den Teil selbst, schließlich den Prüf-Prompt. Das Modell prüft den Teil mit dem vollständigen Kontext der Geschichte.

Der **Pre-Prompt** des Profils, falls gesetzt, steht als Systemnachricht ganz vorne.

<a id="advanced-options"></a>

## Erweiterte Optionen

| Option | |
|---|---|
| **Prompt Override** | Ein anderer Prüf-Prompt für diese Prüfung. Beginnt mit dem Prompt des Profils. |
| **Use standard prompt info** | An: der Prompt, aus dem der Teil geschrieben wurde, wie oben. Aus: nur die Geschichtsteile, mit den unten genannten Optionen. |
| **Include Lorebook** | (nur bei Aus) Fügt die Lore hinzu, die beim Schreiben des Teils aktiv war. |
| **With summaries** | (nur bei Aus) Sendet zuerst die [Zusammenfassungen](../books/summaries.md) der Geschichte, dann die Teile nach der neuesten Zusammenfassung, so wie es eine Generierung tut: Der Teil mit der neuesten Zusammenfassung und alles davor wird durch die Zusammenfassungen dargestellt. Die Zusammenfassungen nehmen zuerst Platz aus dem Token-Limit, die Teile erhalten den Rest, neueste zuerst. Eine Zusammenfassung, deren Geschichte nach ihrer Erstellung geändert wurde, wird nicht gesendet (die Prüfung entfernt sie nicht). Aus: keine Zusammenfassungen, so viele der neuesten Teile, wie hineinpassen. |
| **Send original text** | Wird nur angezeigt, wenn ein Übersetzer ([Real Language Translator](real-language-translator.md), [Conlang Translator](conlang-translator.md)) geladen ist. Aus: Die Prüfung liest das Englische hinter der übersetzten Sprache, wie jedes Modell es tut. An: Der geprüfte Teil (und, bei ausgeschaltetem *Use standard prompt info*, die früheren Teile) wird so gesendet, wie er gespeichert ist, mit der Sprache in der Übersetzung. Der Prompt, aus dem der Teil geschrieben wurde (*Use standard prompt info* an), wurde aus dem Englischen aufgebaut und bleibt so. |
| **Token Limit** | (nur bei Aus) Wie viele Tokens der Geschichte gesendet werden: die Teile bis einschließlich des geprüften, neueste zuerst, so viele wie hineinpassen. Beginnt mit dem Platz für den Prompt des Modells. |

Bei ausgeschaltetem *Use standard prompt info* zeigt der Dialog die Größe der Anfrage (*Total tokens*). **Generate** öffnet den Prüfdialog mit diesen Optionen; weitere Prüfungen, die in diesem Dialog erzeugt werden, verwenden sie ebenfalls.

![Erweiterte Optionen für Prüfungen](../../images/plugin-reviewer-advanced.png)

<a id="profiles"></a>

## Profile

Die Einstellungen des Reviewers findest du unter **Einstellungen → Extension Settings → Reviewer Settings**. Sie sind in **Profilen** organisiert:

![Die Einstellungen des Reviewers](../../images/plugin-reviewer-settings.png)

| Feld | |
|---|---|
| **Profile** | Das Profil, das bearbeitet wird – und das für alle Prüfungen verwendet wird. |
| **New**, **Rename**, **Delete** | Profile verwalten. Das Profil *_Default* kann nicht umbenannt oder gelöscht werden. |
| **Reviewer Model** | Der [Inference Provider](../inference-providers.md), der Prüfungen schreibt. Leer: das Modell des Buchs. |
| **Reviewer Protocol** | Das [Protokoll](../protocols.md). Leer: das Protokoll des Buchs. |
| **Review Pre-Prompt (System)** | Eine Systemnachricht, die zuerst gesendet wird, z. B. *Du bist ein anspruchsvoller Lektor für Belletristik.* |
| **Review Post-Prompt (User)** | Die Bitte um die Prüfung, die zuletzt gesendet wird. |

Klicke oben auf der Seite *Einstellungen* auf **Save**, um die Änderungen zu behalten.

Der Standard-Post-Prompt simuliert eine Diskussion in einem Forum für Geschichten: ein erfundener Thread mit dreißig Antworten von verschiedenen Lesern, positiv und negativ. Weitere Ideen:

- *Liste Kontinuitätsfehler im letzten Teil im Vergleich zur früheren Geschichte auf. Zitiere die Textstellen.*
- *Weise als Zeilenlektor auf Klischees, Wiederholungen und schwache Verben im letzten Teil hin, mit Verbesserungsvorschlägen.*
- *Beschreibe, wie sich ein Leser, der zum ersten Mal da ist, am Ende dieses Teils fühlt, und was er als Nächstes erwartet.*

Lege für jede Art von Prüfung ein eigenes Profil an und wähle vor dem Prüfen das gewünschte aus. Ein günstigeres Modell für Prüfungen spart Kosten: Setze es als *Reviewer Model* des Profils.

<a id="storage"></a>

## Speicherung

Prüfungen werden zusammen mit dem Geschichtsteil gespeichert und sind daher Teil von [Buch-Backups](../books/backups.md) und Kopien. [Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part) kopiert sie nicht. Profile werden in deinen Benutzereinstellungen gespeichert.
