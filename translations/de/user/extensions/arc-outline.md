---
label: Bogenübersicht
order: 24
---

# Bogenübersicht

Der Plan deines Buches in der Seitenleiste: Bögen, jeweils in Kapitel unterteilt, mit dem Status jedes einzelnen, sowie ein Modell, das Bögen und Kapitel für dich schreiben kann. Die Übersicht ist kein Teil des Story-Prompts; die Generatoren und die [Side Query](side-query.md) können sie nutzen, wenn du sie darum bittest.

<a id="the-arcs-tab"></a>

## Der Reiter Arcs

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) erhält einen Reiter **Arcs** neben *Outline*. **Add new arc** öffnet den Dialog eines Bogens:

| Feld | |
|---|---|
| Title | Pflichtfeld. |
| Description | Was darin passiert. |
| Note (private) | Notizen für dich selbst. |
| State | **Planned**, **Current** oder **Finished**. |
| Linked part | Der Teil der Geschichte, zu dem der Bogen oder das Kapitel gehört, siehe [unten](#linking-to-the-story). |

Kapitel haben dieselben Felder. Die Liste zeigt jeden Bogen als Karte, darunter eingerückt seine Kapitel. Die Farbe einer Karte folgt ihrem Status: grau für geplante, blau für den aktuellen, grün mit abgeblendetem Titel für abgeschlossene. Die Schaltflächen einer Karte:

| Schaltfläche | |
|---|---|
| Kalender / Play / Häkchen | Der Status; öffnet ein Menü zum Ändern. |
| Chevron (Bögen) | Blendet die Kapitel des Bogens aus oder ein. |
| Chevron (Kapitel mit Notiz) | Blendet die Notiz des Kapitels unter seiner Beschreibung ein oder aus, damit du sie lesen kannst, ohne den Dialog zu öffnen. |
| Pfeile | Verschiebt den Bogen oder das Kapitel nach oben oder unten. |
| Plus (Bögen) | Fügt am Ende des Bogens ein Kapitel hinzu. |
| Zauberstab (Bögen) | Bittet das Modell, Kapitel für den Bogen zu schreiben, siehe [unten](#generating-chapters). |
| Reißzwecke | Verknüpft (heftet) den Bogen oder das Kapitel mit dem Teil, der in der Geschichte am stärksten sichtbar ist, siehe [unten](#linking-to-the-story). |
| Zielscheibe (Bögen und Kapitel mit verknüpftem Teil) | Scrollt die Geschichte zu dem Teil, mit dem er verknüpft ist. |
| Stift | Öffnet ihn im Dialog. |
| Papierkorb | Löscht ihn (einen Bogen mit allen seinen Kapiteln) nach einer Bestätigung. |
| Roboter (generierte Bögen und Kapitel) | Zeigt, was das Modell geschrieben hat, siehe [unten](#generating-an-arc). |

Neben dem Zauberstab oben befindet sich ein Stift mit Feder: Er entwirft die Anweisungen des nächsten Zugs aus dem aktuellen Kapitel, siehe [unten](#drafting-the-instructions-for-the-current-chapter).

![Der Reiter Arcs: ein abgeschlossener, ein aktueller und ein geplanter Bogen](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Verknüpfung mit der Geschichte

Ein Bogen oder ein Kapitel kann **mit einem Teil der Geschichte verknüpft werden**, sodass du vom Plan direkt zur Stelle im Text springen kannst. Der Dialog enthält das Feld **Linked part**: die Position des Teils im aktiven Zweig, ab 1 gezählt, wie der Reiter *Outline* sie anzeigt (`#12`). Trage sie ein, oder drücke daneben die Schaltfläche **link**, um den Teil zu übernehmen, der in diesem Moment in der Geschichte am **sichtbarsten** ist (scrolle zuerst zur Stelle, öffne dann den Dialog). Die Schaltfläche **target** scrollt die Geschichte zu dem Teil im Feld. Ein verknüpfter Bogen oder ein verknüpftes Kapitel hat außerdem die Schaltfläche target auf seiner Karte. Die **thumbtack** auf einer Karte tut dasselbe wie die Schaltfläche link ohne Dialog: Scrolle zu der Stelle, an der das Kapitel beginnt, und drücke sie. Die Verknüpfung ist die Position im Zweig; sie zeigt daher auf einen anderen Teil, wenn davor Teile gelöscht oder eingefügt werden. Ändere sie dann. Ein Import aus der Geschichte verknüpft die Kapitel für dich, siehe [unten](#importing-an-arc-from-the-story).

<a id="chapter-headings-in-the-text"></a>

### Kapitelüberschriften im Text

**Chapter headings in the text** im Menü des Zauberstabs oben schreibt eine Überschrift an den Anfang des Teils, mit dem jedes Kapitel [verknüpft](#linking-to-the-story) ist, zum Beispiel `# Chapter 4: The Storm`. So trägt der Text der Geschichte seine Kapitel (die Erweiterung Chapter Marker und Exporte übernehmen sie). Der Dialog listet die Überschriften, die er schreiben würde, mit ihrem Teil, während du änderst:

| Option | |
|---|---|
| Arcs get headings too | Aus: nur Kapitel bekommen eine Überschrift (`#`), die Bögen bleiben außen vor. An: jeder Bogen bekommt eine Überschrift (`#`) und seine Kapitel eine Ebene darunter (`##`). Ein Bogen, der selbst nicht verknüpft ist, kommt an den Teil seines ersten verknüpften Kapitels. |
| Prefix of the arcs / of the chapters | Das Wort vor der Nummer, zunächst *Part* und *Chapter*. Leer ergibt `4. The Storm`. |
| Numbers | Arabisch (1, 2, 3), Römisch (I, II, III) oder keine. Kapitel werden nach ihrer Position in der Übersicht nummeriert, verknüpft oder nicht, sodass ein noch nicht geschriebenes Kapitel seine Nummer frei hält. |
| Number the chapters from 1 in every arc | Andernfalls werden die Kapitel durch das ganze Buch nummeriert. |
| Put the titles into the headings | Aus ergibt `Chapter 4` allein. |
| Remove every other heading of the story first | Jede Überschriftenzeile des aktiven Zweigs wird entfernt, bevor die neuen geschrieben werden, etwa wenn Kapitel seit dem letzten Lauf anderswo verknüpft wurden. |
| Keep the summaries valid | An zunächst. Eine Überschrift ändert nicht, was geschieht. Daher werden die Zusammenfassungen, die die Geschichte vor der Änderung beschrieben, wieder als gültig markiert (die bereits veralteten Zusammenfassungen bleiben veraltet). |

**Write the headings** fügt sie ein. Die Überschriften, mit denen ein Teil bereits beginnt, werden ersetzt, sodass ein erneuter Lauf nach einer Änderung der Übersicht sie nicht doppelt einfügt. **Remove all headings** (nach einer Bestätigung) löscht jede Überschriftenzeile aus dem Text des aktiven Zweigs. Jeder geänderte Teil behält seinen alten Text in seinem Verlauf. Ein abgeschlossenes oder aktuelles Kapitel ohne Verknüpfung sowie eine Verknüpfung hinter dem Ende des aktiven Zweigs bekommen keine Überschrift und werden gemeldet. Die Optionen werden gespeichert.

<a id="the-note-of-a-chapter"></a>

### Die Notiz eines Kapitels

Ein Kapitel mit Notiz zeigt rechts einen Chevron. Er öffnet die Notiz unter der Beschreibung in der Liste, damit du die Details eines Kapitels (etwa die, die das Modell dafür geschrieben hat) lesen kannst, ohne den Dialog zu öffnen. Drücke ihn erneut, um die Notiz wieder zu verbergen. Welche Notizen geöffnet sind, wird nicht gespeichert.

![Ein Kapitel mit eingeblendeter Notiz](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Kapitel zwischen Bögen verschieben

Ein Kapitel, das vom ersten Platz seines Bogens nach oben verschoben wird, landet am Ende des vorherigen Bogens. Eines, das vom letzten Platz nach unten verschoben wird, landet am Anfang des nächsten Bogens. Drücke den Pfeil so oft, wie du brauchst, um ein Kapitel so weit zu verschieben, wie nötig.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Die Anweisungen für das aktuelle Kapitel entwerfen

Der Stift mit Feder neben *Add new arc* öffnet das Fenster, in dem das Modell [die Anweisungen](../books/story-editor.md#drafting-the-instructions) des nächsten Zugs entwirft, mit den Daten des Kapitels, an dem die Geschichte gerade steht, als **Plan of the Story**. Das aktuelle Kapitel ist das erste, das als **Current** markiert ist. Gibt es keins, ist es das erste, das nicht **Finished** ist. Ein als aktuell markierter Bogen, der noch keine Kapitel hat, steht allein. Das Modell erhält diesen Bogen und dieses Kapitel mit ihren Beschreibungen und Notizen sowie Titel und Beschreibung (nicht die Notiz) des darauffolgenden Kapitels, mit dem Hinweis, dass die Geschichte dort noch nicht ankommen darf. Das Fenster beginnt mit dem, was du im Feld *Instructions* des Bereichs für den neuen Zug als Leitlinie eingetragen hast. **Use as Instructions** übernimmt den Entwurf in diesen Bereich und öffnet ihn. Ohne aktuelles Kapitel sagt die Schaltfläche das.

Dieselben Daten sind eine Quelle für die [Side Query](side-query.md), siehe [unten](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Einen Bogen abschließen

Wird ein Bogen als **Finished** markiert, werden auch alle seine Kapitel als **Finished** markiert. Wird ein Bogen in einen anderen Status geändert, bleiben seine Kapitel unverändert.

Die Übersicht wird mit dem Buch gespeichert, sobald du sie änderst, und ist daher in seinen Sicherungen enthalten.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## Die Übersicht als Quelle für andere Erweiterungen

Die Übersicht wird anderen Erweiterungen als Datenquelle über das Buch angeboten: Aktiviere in der [Side Query](side-query.md) im Plug-Popover **Arc outline**, um die Bögen, ihre Status, Beschreibungen und Kapitel mit deinen Fragen zu senden. Es gibt zwei Fassungen: **Arc outline** ist die knappe (nur Beschreibungen). **Arc outline (with notes)** ergänzt unter der Beschreibung die Notiz jedes Bogens und Kapitels; dort stehen die Details generierter Bögen und Kapitel. Deine privaten Notizen werden nur gesendet, wenn du die zweite aktivierst. Eine dritte Quelle, **Arc outline: current chapter**, sendet nur den Bogen und das Kapitel, an dem die Geschichte gerade steht (siehe [Die Anweisungen für das aktuelle Kapitel entwerfen](#drafting-the-instructions-for-the-current-chapter)).
<a id="generating-an-arc"></a>

## Einen Handlungsbogen erzeugen

Die Zauberstab-Schaltfläche neben **Add new arc** öffnet ein Menü mit drei Einträgen: **Generate an arc** lässt das Modell einen neuen Handlungsbogen schreiben, **Import from story** fasst zusammen, was du bereits geschrieben hast, siehe [unten](#importing-an-arc-from-the-story), und **Develop the plot** ist ein Gespräch mit dem Modell darüber, wohin die Geschichte geht, siehe [unten](#developing-the-plot-in-a-conversation).

![Das Menü des Zauberstabs](../../images/plugin-arcoutline-menu.png)

 Der Dialog von **Generate an arc** besteht aus zwei Hälften.

Links:

| | |
|---|---|
| Prompt | Die Anweisungen für das Modell, mit drei Variablen: `{{backgroundLore}}` (die Einträge des Lorebooks des Buchs und seiner Unter-Lorebooks), `{{existingArcs}}` (deine Handlungsbögen und Kapitel mit ihren Zuständen und Beschreibungen, aber nicht deine privaten Notizen) und `{{specification}}`. Er beginnt mit dem Prompt aus *Settings → Extension Settings → Arc Outline* (dem Standard-Prompt, solange du ihn nicht geändert hast). Was du hier änderst, gilt nur für diese Erzeugung. |
| Specification | Was du vom Handlungsbogen erwartest: das Thema, die Figuren, wohin er führen soll. Kann leer bleiben. |
| Tools | Nur wenn eine Sammlung von [Werkzeugen für das Modell](tools.md) installiert ist (Namen, Orte): welche Sammlungen und welche ihrer Kategorien das Modell verwenden darf, statt Dinge zu erfinden, genau wie beim [Lore Generator](lore-generator.md). |
| Model, Protocols | Beginnen mit dem Modell und dem Protokoll des Buchs (deine Standardwerte aus *Settings*, wenn das Buch keine hat). Ändere sie, um für diese Erzeugung ein anderes Modell zu verwenden; die Auswahl wird nicht gespeichert. |
| Generate | Startet das Modell. Während es läuft, ist die Schaltfläche ein rotes **Stop**, und alles andere im Dialog ist deaktiviert. |

Rechts siehst du die **Überlegungen** des Modells, während sie geschrieben werden, und sobald die Antwort beginnt, den **Handlungsbogen**, den es
vorschlägt: einen Namen, eine knappe Beschreibung und eine längere Notiz. Er wird wie ein Handlungsbogen der Gliederung dargestellt (die Notiz unter seiner
Beschreibung) und wächst, während die Antwort eintrifft. Die Beschreibung ist das, was die Gliederung zeigt. Deshalb wird das Modell gebeten, sie auf ein paar Sätze zu beschränken und die Einzelheiten in die Notiz zu schreiben, die im Feld **Note** des gespeicherten Handlungsbogens landet. Wenn das Modell fertig ist, wird seine Antwort geprüft. Ist sie kein gültiger Handlungsbogen (etwa fehlt die Notiz), sagt der Dialog das, und der Handlungsbogen kann nicht gespeichert werden.

![Einen Handlungsbogen erzeugen](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** fügt den Handlungsbogen am Ende der Gliederung hinzu. Um es erneut zu versuchen, drücke noch einmal **Generate**, mit demselben oder einem geänderten Prompt oder einer geänderten Spezifikation. Die vorherige Antwort wird ersetzt.

<a id="importing-an-arc-from-the-story"></a>

## Einen Handlungsbogen aus der Geschichte importieren

**Import from story** im Menü des Zauberstabs lässt das Modell einen Teil deiner Geschichte lesen und als einen Handlungsbogen mit seinen Kapiteln niederschreiben, damit du ein bereits geschriebenes Buch in die Gliederung übernehmen kannst. Das Modell liest immer den [aktiven Zweig](../books/branches-and-story-tree.md) des Buchs, so wie er ist, wenn du den Dialog öffnest. Der Dialog ähnelt dem für Handlungsbögen, mit diesen Optionen links:

| | |
|---|---|
| From message, To message | Der Bereich der Geschichte, der gesendet wird. Die Nachrichten des Zweigs, die Text haben, sind ab 1 nummeriert. Zu Beginn ist der ganze Zweig gewählt. Unter jedem Feld siehst du den Anfang der Nachricht, auf die es zeigt. |
| Number of chapters (optional) | Wähle eine Zahl, um genau so viele Kapitel zu erhalten (eine Antwort mit einer anderen Zahl wird abgelehnt). Lass das Feld leer, damit das Modell die Geschichte dort teilt, wo sie sich natürlicherweise teilt. |
| Link the chapters to the parts of the story | Zu Beginn eingeschaltet. Die Nachrichten sind innerhalb des gesendeten Bereichs ab 1 nummeriert, und das Modell sagt, in welcher Nachricht jedes Kapitel beginnt. Das Kapitel wird mit diesem Teil der Geschichte [verknüpft](#linking-to-the-story) (nummeriert im gesamten Zweig, sodass es unabhängig vom Bereich stimmt), und der Handlungsbogen mit dem Teil, in dem sein erstes Kapitel beginnt. Eine Antwort, die eine Nachricht nennt, die nicht gesendet wurde, lässt das Kapitel unverknüpft. |
| State of the imported arc and its chapters | **Finished** zu Beginn, weil die Geschichte geschrieben ist. Wähle **Current** oder **Planned**, wenn du es brauchst. |
| Token counter | Wie viele Nachrichten der Bereich hat und deren ungefähre Token-Anzahl sowie die ungefähre Token-Anzahl des gesamten Prompts (Anweisungen, Lore, Gliederung und Geschichte) neben dem, was das Modell aufnimmt. Es wird zu einer Warnung, wenn der Prompt nicht passt; wähle dann weniger Nachrichten. Es folgt dem Bereich, der Anzahl der Kapitel, dem Prompt und dem Modell. |
| Prompt | Die Anweisungen für das Modell, mit den Variablen `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (die Nachrichten des Bereichs) und `{{numberOfChapters}}`. Er beginnt mit der **Story import prompt** aus *Settings → Extension Settings → Arc Outline*. |

![Einen Handlungsbogen aus der Geschichte importieren](../../images/plugin-arcoutline-import.png)

Hier gibt es keine Werkzeuge, die Namen stehen in der Geschichte. Rechts siehst du die Überlegungen, danach den Handlungsbogen und seine Kapitel, während das Modell sie schreibt. **Save as arc** fügt den Handlungsbogen mit seinen Kapiteln am Ende der Gliederung hinzu. Jedes von ihnen behält die Überlegungen und die Antwort des Modells und hat seinen eigenen Roboter-Button. Eine lange Geschichte importiert man am besten in mehreren Schritten, je ein Handlungsbogen für jeden Bereich.
<a id="developing-the-plot-in-a-conversation"></a>

## Die Handlung im Gespräch entwickeln

Mit **Develop the plot** im Menü des Zauberstabs öffnet sich ein Chat mit dem Modell über die Handlung. Am Buch ändert sich durch das Gespräch nichts: Das Modell sieht nur, was du ihm schickst, und antwortet. Wenn ihr euch auf etwas geeinigt habt, gibst du es mit einem Klick an den Bogengenerator weiter.

Links wählst du, was das Modell sieht und wie es hilft (die Wahl wird für das nächste Mal gespeichert). Rechts steht der Chat:

![Der Handlungsassistent: die Optionen links, das Gespräch rechts](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Model, Protocols | Anfangs das Modell und das Protokoll des Buchs. Eine Änderung gilt nur für dieses Gespräch. |
| How the model helps | **Free talk** (du führst), **Explore directions** (mehrere unterschiedliche Wege, wie die Geschichte weitergehen kann), **Find the problems** (nie aufgelöste Fäden, nicht eingelöste Versprechen, Widersprüche zum Weltwissen, Tempo) oder **Expand an idea** (deine Idee, ausgebaut zu einer Prämisse, Wendepunkten, einem Ende und Kapiteln, die zu deinen Bögen passen). |
| Send the lorebook | Die Einträge des Lorebooks des Buchs und seiner Unter-Lorebooks. |
| Send the outline | **Not sent**, **Descriptions only** oder **With the notes** (auch deine privaten Notizen). |
| Send the notes | Nur wenn [Story Notes](story-notes.md) installiert ist: **Not sent**, **Titles and first lines** (die Voreinstellung) oder **Full text** deiner Notizen, ohne die Seiten, die du vor Assistenten verborgen hast. |
| Send the summaries of the story | Die Zusammenfassungen ersetzen die Geschichte bis zur neuesten von ihnen, so wie eine Generierung sie schickt. |
| Send the latest parts of the story | Die Teile nach der neuesten Zusammenfassung (alle, wenn Zusammenfassungen aus sind), und zwar die neuesten, die in **Tokens of the story** passen. Jeder Teil wird dem Modell unter seiner Nummer in der Gliederung der Geschichte gezeigt. |
| Tokens of the story | Die maximale Anzahl Tokens der Geschichtsteile. Leer: ein Drittel dessen, was das Modell aufnimmt. |
| Prompt | Die Anweisungen für das Modell, mit den Variablen `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` und `{{mode}}`. Die Änderung wird gespeichert. |

Unter den Optionen zeigt eine Zeile, wie viele Zusammenfassungen und Geschichtsteile das Modell bekommt und wie viele Tokens die Anfrage von dem hat, was das Modell aufnimmt. Sie wird zu einer Warnung, wenn es nicht passt. Dann schicke weniger oder [fasse den Chat zusammen](#chats-branches-and-summaries). Das Buch wird für jede Nachricht neu gelesen, daher sieht die nächste Antwort eine Änderung der Gliederung oder der Geschichte.

Rechts steht der Chat. Tippe eine Nachricht und drücke **Send** (während das Modell schreibt, steht dort **Stop**; was schon angekommen ist, bleibt erhalten). Jede Nachricht hat Schaltflächen: kopieren, **branch from here**, löschen. Bei einer Antwort des Modells gibt es außerdem einen Zauberstab, **Generate an arc from this message**, der den Bogengenerator mit der Nachricht als Vorgabe öffnet. Wenn die Besprechung lang war, bitte das Modell zuerst, das Entschiedene als Vorgabe zusammenzufassen.

<a id="letting-the-model-change-the-book"></a>

### Das Modell das Buch ändern lassen

Unter den Optionen steht **What the model may do**. Ist keine davon angekreuzt, ist der Chat nur ein Gespräch. Kreuzt du eine Gruppe an, bekommt das Modell dafür Werkzeuge und kann sie verwenden, wenn du es bittest oder zustimmst. Es wird angewiesen, vor dem Hinzufügen nachzusehen, Ergänzen dem Ersetzen vorzuziehen und zu sagen, was es getan hat:

| Gruppe | Das Modell kann |
|---|---|
| **Planner** | die Szenen des [Planners](../books/story-editor.md#the-planner-tab) auflisten, sie hinzufügen, ändern, verschieben, löschen und als erledigt markieren. |
| **Lorebook** | das Weltwissen durchsuchen und lesen, Einträge anlegen und bestehende ändern (nicht löschen). |
| **Outline** | die Gliederung lesen, Bögen und Kapitel hinzufügen, sie ändern und verschieben (nicht löschen). |
| **Notes** (wenn [Story Notes](story-notes.md) installiert ist) | deine Notizen durchsuchen und lesen und ihren Links folgen, eine Seite anlegen oder zu einer Seite etwas hinzufügen, wenn du willst, dass es etwas festhält, und Text in einer Seite ändern (keine Seite löschen). Seiten, die du vor Assistenten verborgen hast, sind nicht erreichbar. |
| **Characters** (wenn der [Character Tracker](character-tracker.md) installiert ist) | die Figuren des Buchs und ihre Beziehungen lesen, Figuren und Beziehungen hinzufügen, sie ändern (Name, Kurzbeschreibung, Notiz, Farbe) und löschen. Nur das Löschen wird vorher erfragt, eine Änderung nicht: Du siehst sie im Chat und im Reiter Characters. Das Modell darf eine Figur mit ihrer Nummer in der Liste nennen. Die Frage und der Chat zeigen dazu den Namen der Figur. |
| **Generators** | die Bogen- und Kapitelgeneratoren aufrufen und, wenn der [Lore Generator](lore-generator.md) installiert ist, einen Lore-Eintrag erzeugen. Sie liefern einen Entwurf zurück und speichern nichts, außer das Modell wird dazu angewiesen. Es zeigt dir den Entwurf und speichert ihn, wenn du zustimmst. |
| **Tools** (Schaltfläche, wenn eine [Werkzeugsammlung](tools.md) installiert ist) | die Namenswerkzeuge (Namen, Orte) wie bei den Generatoren verwenden, damit es dir echte Namen vorschlagen kann. |

- **Ask me before it changes or deletes something** (standardmäßig an): Das Ändern des Textes bestehender Inhalte (eines Lore-Eintrags, einer Szene, eines Bogens) und das Löschen werden dir zuerst gezeigt, zusammen mit dem, was das Modell übergeben will. Das Modell wartet auf **Allow** oder **Refuse**. Das Hinzufügen einer Szene, eines Eintrags oder eines Bogens wird nicht erfragt: Du siehst es im Chat und im Planner, im Lorebook oder in der Gliederung und kannst es wieder löschen.
- Jedes Werkzeug, das das Modell aufgerufen hat, steht unter seiner Antwort (eine Zeile mit dem Namen und einer Markierung: ✓ hat funktioniert, ✗ ist fehlgeschlagen). Öffne sie, um zu sehen, was übergeben wurde und was zurückkam.
  Die nächsten Nachrichten enthalten die Aufrufe des Modells und was sie zurückgegeben haben (ein langes Ergebnis wird gekürzt). So weiß es, was es schon getan hat, und legt nicht noch einmal denselben Eintrag an. Sie zählen auf das Tokenlimit des Chats.
- Was die Werkzeuge kosten: Ihre Beschreibungen gehören zu jeder Anfrage, daher hat ein Modell mit knappem Kontext weniger Platz. Nur Modelle, die Funktionen aufrufen können, arbeiten damit. Der Dialog sagt es, wenn ein Modell das nicht kann.

![Das Modell hat dem Planner zwei Szenen hinzugefügt; seine Aufrufe stehen unter der Antwort](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Chats, Verzweigungen und Zusammenfassungen

Ein Buch kann beliebig viele Chats haben. Wähle einen in der Liste über den Nachrichten aus, **+** startet einen neuen, der Stift benennt ihn um (bis dahin heißt er nach deiner ersten Nachricht), und der Papierkorb löscht ihn. Sie werden mit dem Buch gespeichert.

- **Branch** kopiert den Chat unverändert in einen neuen Chat. Die Verzweigungs-Schaltfläche an einer Nachricht kopiert ihn bis einschließlich dieser Nachricht, um das Gespräch von dort anders weiterzuführen. Die Kopie hat danach mit dem Original nichts mehr zu tun.
- **Summarize the chat** fragt zuerst, was die Zusammenfassung behalten oder betonen soll (optional, zum Beispiel „den Twist um den Verräter und das Ende behalten, das wir verworfen haben“; leer: das Modell entscheidet). Dann bittet es das Modell um eine Zusammenfassung des Chats (was du entschieden hast, was du verworfen hast, was noch offen ist, die Namen, die festgelegt wurden) und fügt sie als Nachricht hinzu. Von da an bekommt das Modell diese Zusammenfassung und die Nachrichten danach, **nicht** die Nachrichten darüber. Diese bleiben abgedunkelt auf dem Bildschirm. Verwende das, wenn der Chat für das Modell zu lang geworden ist oder wenn du von einem sauberen Stand aus beginnen willst, ohne die Entscheidungen zu verlieren. Fasst du später erneut zusammen, deckt die neue Zusammenfassung die alte ab. Verzweige zuerst, wenn du dem Modell den ganzen Chat erhalten willst.

![Die Frage, was die Zusammenfassung behalten soll](../../images/plugin-arcoutline-wizard-summarize.png)

![Der Chat nach der Zusammenfassung: Die Nachrichten darüber bleiben abgedunkelt auf dem Bildschirm](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Kapitel generieren

Der Zauberstab-Knopf an einem Handlungsbogen bittet das Modell, diesen Bogen in Kapitel aufzuteilen. Der Dialog ist wie der für Handlungsbögen aufgebaut, mit diesen Auswahlmöglichkeiten links:

| | |
|---|---|
| Was das Modell von der Geschichte kennt | **Whole story so far**: die Bögen bis zu diesem, mit allen ihren Kapiteln. **Arc summaries only**: dieselben Bögen ohne Kapitel. **Arc summaries and finished chapters**: dieselben Bögen nur mit den Kapiteln, die Sie als *Finished* markiert haben. Bögen nach diesem werden nie gesendet. |
| Was Sie allgemein von den Kapiteln wollen | Tempo, Ton, was geschehen muss. Kann leer bleiben. |
| Anzahl der Kapitel | In Worten schreiben ("about eight") oder eine genaue Zahl wählen. Eine Zahl fügt für jedes Kapitel ein Textfeld hinzu, in dem Sie festlegen können, was es enthalten soll; alle sind optional. Die Wahl der einen löscht die andere. Bei einer genauen Zahl wird eine Antwort mit einer anderen Kapitelanzahl abgelehnt. |
| Prompt | Die Anweisungen für das Modell mit den Variablen `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` und `{{chapterSpecification}}`. Er beginnt mit der **Chapter generation prompt** aus *Settings → Extension Settings → Arc Outline*. |

Rechts sehen Sie die Begründung, dann den Bogen mit den Kapiteln, die er bereits hat, und die neuen Kapitel, während das Modell sie schreibt. **Save as chapters** fügt sie am Ende des Bogens hinzu, jeweils mit einer knappen Beschreibung und den Details in ihrer Notiz (wie bei Bögen). Jedes neue Kapitel behält die Begründung und die Antwort des Modells und hat seinen eigenen Roboter-Knopf.

![Generating the chapters of an arc](../../images/plugin-arcoutline-generate-chapters.png)

Ein vom Modell geschriebener Bogen oder ein Kapitel behält seine **reasoning** und die **answer**, aus denen er entstanden ist. Der Roboter-Knopf daran öffnet sie in einem kleinen Popover, das nicht im Weg steht.

![What the model wrote for an arc](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Befehle

Der Umriss und seine Generatoren sind auch als [Befehle](../commands/index.md) verfügbar, für die Kommandozeile und für [Agenten](../agents.md). Sie arbeiten am geöffneten Buch (bei einem Agenten: an dessen Arbeitskopie), und ein geöffneter Reiter Arcs zeigt die Änderung sofort. Bögen und Kapitel werden über die **Nummer** angegeben, so wie der Umriss sie zeigt: Der erste Bogen ist `1`, das erste Kapitel eines Bogens ist `1` (die Nummern ändern sich, wenn davor etwas hinzugefügt, entfernt oder verschoben wird). Ein Befehl, der einen Bogen oder ein Kapitel ändert, nimmt nur die Felder, die Sie angeben; ein leerer Text löscht eine Beschreibung oder eine Notiz.

| Befehl | Was er tut |
|---|---|
| `/get-arc-outline [arc [notes]]` | Die Bögen mit ihren Kapiteln, Zuständen und Beschreibungen als JSON. Die privaten Notizen nur mit `notes` `true`; `arc` liefert nur diesen einen Bogen. Nur lesend. |
| `/add-arc title [description [note [state [position]]]]` | Fügt einen Bogen am Ende oder an der Stelle `position` hinzu. |
| `/update-arc arc [title [description [note [state]]]]` | Ändert einen Bogen. `state` ist `PLANNED`, `CURRENT` oder `FINISHED` (einen Bogen abzuschließen schließt seine Kapitel ab). |
| `/move-arc arc position` | Verschiebt einen Bogen an die Stelle `position` (1 ist der erste). |
| `/delete-arc arc` | Löscht einen Bogen mit seinen Kapiteln, nach einer Bestätigung. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Fügt einem Bogen ein Kapitel hinzu. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Ändert ein Kapitel. |
| `/move-outline-chapter arc chapter position [to_arc]` | Verschiebt ein Kapitel innerhalb seines Bogens oder in den Bogen `to_arc`. |
| `/delete-outline-chapter arc chapter` | Löscht ein Kapitel, nach einer Bestätigung. |
| `/generate-arc [specification [save]]` | Bittet das Modell um einen neuen Bogen, so wie *Generate an arc* es tut. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Bittet das Modell um die Kapitel eines Bogens, so wie der Zauberstab des Bogens es tut. `count` ist eine genaue Anzahl von Kapiteln, `context` ist `EVERYTHING` (der Standard), `ARCS_ONLY` oder `ARCS_AND_FINISHED`, entsprechend der Auswahl *What the model knows of the story*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Bittet das Modell, die Nachrichten `from` bis `to` des aktiven Zweigs (gezählt unter den Nachrichten mit Text, wie bei *Import from story*) als Bogen mit Kapiteln zusammenzufassen. `state` (Standard `FINISHED`) ist der Zustand des gespeicherten Bogens und seiner Kapitel. Mit `mapParts` `true` wird jedes Kapitel mit der Nachricht verknüpft, in der es beginnt (sein *Linked part*, so wie es das Kontrollkästchen *Map parts* des Dialogs tut), und die Antwort enthält den Abschnitt jedes Kapitels. |

Die drei generierenden Befehle verwenden dieselben Prompts wie die Dialoge (Ihre *Extension Settings → Arc Outline* oder die Standardwerte), das Modell und das Protokoll des Buchs (Ihr Standardmodell, wenn das Buch keines hat) und keine Werkzeuge. Ein Aufrufer, der Namen möchte, erhält sie über die Befehle des [Namensgenerators](name-generator.md) oder des [Namensanbieters](name-provider.md) und trägt sie in die Spezifikation ein. Wie die Dialoge speichern auch sie nur auf Anfrage: Mit `save` `false` (der Standard) geben sie Bogen oder Kapitel als JSON zurück und speichern nichts; mit `save` `true` fügen sie sie dem Umriss hinzu (ein Bogen am Ende, Kapitel am Ende des Bogens), und die Antwort enthält deren Nummern. Eine Geschichte, die für das Modell zu lang ist, lehnt `/import-arc-from-story` ab: Wählen Sie weniger Nachrichten. Die generierenden Befehle brauchen eine Weile und stoppen, wenn der Lauf angehalten wird.

Hinzufügen und Ändern ist nicht destruktiv; nur die beiden `delete`-Befehle sind es, wonach ein Agent Sie fragen kann.
