---
label: Prompt-Inspektor
order: 4
---

# Prompt-Inspektor

Zeigt den Prompt einer Generierung, bevor er an das Modell gesendet wird, und fragt nach, ob er gesendet werden soll.

<a id="how-it-works"></a>

## So funktioniert es

Die Erweiterung fügt **Inspect prompt before sending** zur Gruppe *Analyse* im Einstellungsmenü (⚙) des [Story-Editors](../books/story-editor.md) hinzu. Es handelt sich um ein Kontrollkästchen: Wählen Sie es aus, um die Prüfung einzuschalten, und wählen Sie es erneut, um sie auszuschalten. Die Auswahl ist eine Einstellung Ihres Benutzers und gilt für alle Ihre Bücher.

Ist sie eingeschaltet, hält eine Generierung (ein neuer Teil, ein Swipe, eine Neugenerierung) kurz vor der Anfrage an und öffnet einen Dialog, **Send this prompt to the model?** Der Text ist der Prompt so, wie das Modell ihn erhält, nachdem alle anderen Erweiterungen ihn verändert haben: jede Nachricht unter ihrer Rolle - `[SYSTEM]` (die Vorlage, das Lore und die Zusammenfassungen), die früheren Teile der Geschichte als `[USER]`- und `[ASSISTANT]`-Nachrichten sowie die Anweisungen des letzten Zugs. Mitgesendete Bilder werden am Ende ihrer Nachricht gezählt.

![Der Prompt, bevor er gesendet wird](../../images/plugin-prompt-inspector.png)

- **Senden** sendet die Anfrage, und die Generierung läuft wie gewohnt weiter.
- **Abbrechen** stoppt die Generierung. Der dafür begonnene Teil wird wieder entfernt (ein neu generierter Teil erhält seinen alten Text zurück), so wie wenn eine Generierung fehlschlägt, bevor Text eintrifft.

<a id="notes"></a>

## Hinweise

- Der Prompt wird wie bei jeder Generierung zuerst erstellt, und Sie werden als letzter Schritt vor der Anfrage gefragt. Ist eine Zusammenfassung ungültig geworden, kommt die übliche Frage, ob ohne sie fortgefahren werden soll, bevor der Prompt angezeigt wird. Vor Ihrer Antwort wird keine Anfrage an das Modell gestellt.
- Nur Generierungen, die aus dem Story-Editor gestartet wurden, werden angehalten. [Agenten](../agents.md) und Befehle haben keine Seite, auf der der Prompt angezeigt werden könnte, und ihre Generierungen laufen ohne Nachfrage weiter.
- Der Dialog lässt sich verschieben und in der Größe ändern, und der Text kann markiert und kopiert werden. Er muss beantwortet werden: Esc und ein Klick außerhalb schließen ihn nicht. Wenn Sie die Seite bei geöffnetem Dialog schließen, gilt das als *Abbrechen*: Die Generierung wird gestoppt und nichts gesendet, sobald der Server bemerkt, dass die Seite nicht mehr da ist (das kann eine Weile dauern).
- Andere Benutzer werden nicht gefragt, und der Schalter bleibt beim Neuladen der Seite erhalten.
