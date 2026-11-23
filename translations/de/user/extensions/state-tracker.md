---
label: Zustandsverfolgung
order: 29
---

# Zustandsverfolgung

Verfolgt den aktuellen **Zustand** deiner Figuren: was sie tragen, was sie bei sich haben, die Waffe in der Hand, ihre Verletzungen, ihre Stimmung oder alles andere, das du definierst. Der Zustand der Figuren in der Szene wird in den Prompt eingefügt. Ein Modell vergisst leicht, dass das Schwert vor zwei Kapiteln verloren ging oder dass der Umhang durchnässt ist. Mit der Zustandsverfolgung steht im Prompt für jede verfolgte Figur in der Szene, was jetzt von ihr gilt.

!!!
Du entscheidest, wer verfolgt wird und was über sie verfolgt wird. Nur die Figuren, die du in den Tab aufnimmst, werden dem Modell beschrieben, und nur solange sie zu den *Anwesenden Figuren* des Teils gehören, der gerade geschrieben wird. In dieser Version pflegst du den Zustand selbst (oder ein Agent tut es mit den unten stehenden Befehlen); das Mitverfolgen der Geschichte durch das Modell kommt später.
!!!

<a id="categories"></a>

## Kategorien

Eine **Kategorie** ist eine Sache, die über eine Figur verfolgt werden kann. Jede Kategorie hat:

- einen **Namen**, der auch das ist, was der Prompt dafür verwendet (*Wearing*, *Weapon at hand*, *Carrying*);
- eine **Art**:
  - **Einzelwert**: ein kurzer Wert, und ein neuer Wert ersetzt den alten (die Waffe in der Hand, der Ort).
  - **Liste**: Einträge, die einzeln hinzugefügt und entfernt werden (was getragen wird, die Verletzungen).
  - **Beschreibung**: ein Text, der als Ganzes neu geschrieben wird (die Kleidung, die Stimmung).
- eine **Anweisung für das Modell**: was in die Kategorie gehört und was nicht. Das Modell liest sie, wenn es der Geschichte folgt.
- **Nur was der Text sagt**: an, der Wert ändert sich nur, wenn der Text es sagt; aus, auch bei offensichtlichen Folgen (nass nach dem Schwimmen). Das Modell nutzt dies, wenn es der Geschichte folgt.
- **Say "none" when empty**: Der Prompt schreibt *none*, wenn eine Figur keinen Wert hat (eine verlorene Waffe), statt die Kategorie wegzulassen.

<a id="my-categories"></a>

### Meine Kategorien

Deine eigenen Kategorien findest du unter **Settings → Extension Settings → State Tracker**. Bücher kopieren ihre Kategorien von dort. Bis du etwas änderst, gibt es fünf eingebaute: *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* und *Mood*. Füge Kategorien dort hinzu, bearbeite und lösche sie, und drücke im Einstellungsfenster auf **Speichern**. **Reset to the built-in categories** bringt die eingebauten fünf zurück.

<a id="the-categories-of-a-book"></a>

### Die Kategorien eines Buches

Ein Buch behält eine **eigene Kopie** jeder Kategorie, die es verwendet. So enthält ein Backup oder Export des Buches alles, und das Löschen einer deiner Kategorien bricht nie ein Buch. Der Button **Categories of this book** im Status-Tab listet sie auf:

- **Add from my categories** kopiert eine deiner Kategorien in das Buch.
- **New category for this book** erstellt eine Kategorie, die nur in diesem Buch existiert (die Ladung eines Schiffs, eine magische Bindung). Eine solche Kategorie ist als *Only in this book* markiert. **Save to my categories** fügt sie deinen Kategorien hinzu, damit auch andere Bücher sie nutzen können.
- Eine Kopie, die nicht mehr deiner Kategorie entspricht, ist als *Differs from your category* markiert. **Update from my categories** übernimmt deine Version. Nichts wird ohne dich aktualisiert, denn eine geänderte Anweisung ändert, was das Modell tut.
- Das Bearbeiten einer Kategorie ändert sie nur in diesem Buch. Ihre Art kann sich nicht ändern, solange Figuren Werte davon haben.
- Das Löschen einer Kategorie aus dem Buch löscht die Werte jeder Figur darin.

<a id="the-state-tab"></a>

## Der Status-Tab

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) erhält einen Tab **Status**. Alles wird im Buch gespeichert, sobald du es änderst.

**Track a character** fragt nach einem **Namen**, optional nach **Aliasen** (den anderen Namen, unter denen die Figur im Feld *Anwesende Figuren* eines Teils geschrieben wird, durch Kommas getrennt), und nach den **verfolgten Kategorien**. Die Liste bietet die Kategorien des Buches und deine eigenen an; wenn du eine deiner eigenen wählst, wird sie in das Buch kopiert. Ein Pferd kann nur *Injuries* verfolgen, während die Heldin alles verfolgt.

Jede verfolgte Figur zeigt ihre Kategorien, jede mit dem aktuellen Wert oder den aktuellen Werten:

- **+** setzt den Wert einer Einzelwert- oder Beschreibungskategorie (der aktuelle Wert wandert in den Verlauf) oder fügt einer Liste einen Eintrag hinzu.
- Der Stift bearbeitet einen Wert, das Kreuz **entfernt** ihn: Er gilt nicht mehr, also wandert er in den Verlauf und wird nicht an das Modell geschickt.
- Das Schloss markiert einen Wert als **gesperrt** (eine Narbe, das Familienschwert). Das Aufräumen, das mit dem Modell kommen wird, lässt gesperrte Werte unberührt.
- **History** zeigt die früheren Werte der Kategorie mit dem Grund ihres Endes (*replaced* oder *removed*). **Wiederherstellen** macht einen früheren Wert wieder aktuell (bei einer Einzelwert- oder Beschreibungskategorie wandert der aktuelle Wert an dessen Stelle in den Verlauf). **Delete** entfernt einen Wert endgültig.

Der Stift bei der Figur bearbeitet ihren Namen, ihre Aliase und Kategorien. Das Abwählen einer Kategorie blendet sie aus, behält aber ihre Werte; erneutes Anwählen bringt sie zurück. Der Mülleimer beendet das Verfolgen der Figur und löscht ihren gesamten Zustand.

<a id="in-the-prompt"></a>

## Im Prompt

Das Zahnrad enthält die Einstellungen des Buches:

- **In prompt** fügt den Zustand der verfolgten Figuren der Szene in den Prompt jeder Generierung ein (das Zahnrad wird grün, solange es an ist).
- **Depth** und **Rolle** legen fest, wohin er kommt, wie beim Autorenkommentar: *Depth* ist die Anzahl der Nachrichten danach (0 nach den Anweisungen des Zugs, 1 direkt davor).
- **Name separators** legt fest, wie das Feld *Anwesende Figuren* aufgeteilt wird (standardmäßig `,` `;` `/` `|` und Zeilenumbrüche). Hinweise in Klammern werden ignoriert, sodass `Mara (asleep), Fiora` beides findet.

Der Prompt erhält die Figuren der Szene, die etwas zu sagen haben:

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Denselben Text kannst du an jeder Stelle einer Vorlage mit dem Makro `{{command::state-tracker-block::{{presentCharacters}}}}` einfügen (jede Liste von Namen funktioniert als Argument). Das Makro gibt den Zustand aus, unabhängig davon, ob **In prompt** an ist. Schalte es daher aus, wenn du das Makro verwendest, sonst erhält das Modell den Zustand doppelt.

<a id="commands"></a>

## Befehle

Für die Befehlsleiste und für Agenten:

| Befehl | Was er tut |
|---|---|
| `state-status` | Die Einstellungen, die Kategorien des Buches und deine, zu denen es keine Kopie hat. |
| `list-state-characters` | Die verfolgten Figuren mit ihren Kategorien. |
| `track-state-character` | Beginnt, eine Figur zu verfolgen: Name, Aliase, Kategorien nach Namen. |
| `stop-tracking-state-character` | Beendet das Verfolgen einer Figur und löscht ihren Zustand (fragt vorher nach). |
| `list-character-state` | Die Werte einer Figur, aktuelle und frühere, nummeriert. |
| `set-character-state` | Setzt einen Wert: Figur, Kategorie, Text. |
| `remove-character-state` | Entfernt einen Wert anhand seiner Nummer: Er wandert in den Verlauf. |
| `state-tracker-block` | Der Text des Prompts für eine Liste von Figuren (das Makro). |
