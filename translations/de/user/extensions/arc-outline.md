---
label: Arc-Gliederung
order: 24
---

# Arc-Gliederung

Der Plan deines Buches in der Seitenleiste: Arcs, jeweils unterteilt in Kapitel, mit dem Zustand jedes einzelnen davon, sowie ein Modell, das Arcs und Kapitel für dich schreiben kann. Die Gliederung ist nicht Teil des Story-Prompts; die Generatoren und die [Side Query](side-query.md) können sie nutzen, wenn du sie darum bittest.

<a id="the-arcs-tab"></a>

## Der Reiter Arcs

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) erhält neben *Outline* einen Reiter **Arcs**. **Add new arc** öffnet den Dialog eines Arcs:

| Feld | |
|---|---|
| Titel | Pflichtfeld. |
| Beschreibung | Was darin geschieht. |
| Notiz (privat) | Notizen für dich selbst. |
| Zustand | **Planned**, **Current** oder **Finished**. |
| Verknüpfter Teil | Der Teil der Geschichte, zu dem der Arc oder das Kapitel gehört, siehe [unten](#linking-to-the-story). |

Kapitel haben dieselben Felder. Die Liste zeigt jeden Arc als Karte, wobei seine Kapitel darunter eingerückt erscheinen. Die Farbe einer Karte richtet sich nach ihrem Zustand: Grau für geplant, Blau für den aktuellen, Grün mit abgeblendetem Titel für abgeschlossene. Die Schaltflächen einer Karte:

| Schaltfläche | |
|---|---|
| Kalender / Play / Haken | Der Zustand; öffnet ein Menü zum Ändern. |
| Chevron (Arcs) | Blendet die Kapitel des Arcs ein oder aus. |
| Chevron (Kapitel mit Notiz) | Zeigt oder verbirgt die Notiz des Kapitels unter seiner Beschreibung, damit du sie lesen kannst, ohne den Dialog zu öffnen. |
| Pfeile | Verschiebt den Arc oder das Kapitel nach oben oder unten. |
| Plus (Arcs) | Fügt am Ende des Arcs ein Kapitel hinzu. |
| Zauberstab (Arcs) | Bittet das Modell, Kapitel für den Arc zu schreiben, siehe [unten](#generating-chapters). |
| Zielscheibe (Arcs und Kapitel mit verknüpftem Teil) | Scrollt die Geschichte zu dem Teil, mit dem sie verknüpft ist. |
| Stift | Öffnet ihn im Dialog. |
| Papierkorb | Löscht ihn (einen Arc samt allen seinen Kapiteln) nach einer Bestätigung. |
| Roboter (generierte Arcs und Kapitel) | Zeigt, was das Modell geschrieben hat, siehe [unten](#generating-an-arc). |

Neben dem Zauberstab oben befindet sich ein Stift mit einer Feder: Er entwirft die Anweisungen für den nächsten Zug aus dem aktuellen Kapitel, siehe [unten](#drafting-the-instructions-for-the-current-chapter).

![Der Reiter Arcs: ein abgeschlossener, ein aktueller und ein geplanter Arc](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Verknüpfung mit der Geschichte

Ein Arc oder ein Kapitel kann **mit einem Teil der Geschichte verknüpft** werden, sodass du von der Planung direkt an die Stelle im Text springen kannst. Der Dialog hat das Feld **Verknüpfter Teil**: die Position des Teils im aktiven Zweig, ab 1 nummeriert, so wie der Reiter *Outline* sie anzeigt (`#12`). Trage sie ein oder drücke die Schaltfläche **link** daneben, um den Teil zu übernehmen, der in diesem Moment in der Geschichte **am stärksten sichtbar** ist (scrolle zuerst zur Stelle und öffne dann den Dialog); die Schaltfläche **target** scrollt die Geschichte zu dem Teil im Feld. Ein verknüpfter Arc oder ein verknüpftes Kapitel hat außerdem die Zielscheibe auf seiner Karte. Die Verknüpfung ist die Position im Zweig, sie zeigt also auf einen anderen Teil, wenn davor Teile gelöscht oder eingefügt werden; ändere sie dann. Ein Import aus der Geschichte verknüpft die Kapitel für dich, siehe [unten](#importing-an-arc-from-the-story).

<a id="the-note-of-a-chapter"></a>

### Die Notiz eines Kapitels

Ein Kapitel mit einer Notiz zeigt rechts einen Chevron. Er öffnet die Notiz unter der Beschreibung in der Liste, sodass du die Details eines Kapitels (etwa die, die das Modell dafür geschrieben hat) lesen kannst, ohne den Dialog zu öffnen; drücke erneut, um sie wieder zu verbergen. Welche Notizen geöffnet sind, wird nicht gespeichert.

![Ein Kapitel mit eingeblendeter Notiz](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Kapitel zwischen Arcs verschieben

Ein Kapitel, das vom ersten Platz seines Arcs nach oben verschoben wird, wandert an das Ende des vorherigen Arcs; eines, das vom letzten Platz nach unten verschoben wird, an den Anfang des nächsten Arcs. Drücke den Pfeil so oft, wie du brauchst, um ein Kapitel so weit zu bewegen.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Anweisungen für das aktuelle Kapitel entwerfen

Der Stift mit der Feder neben *Add new arc* öffnet das Fenster, mit dem das Modell die [Anweisungen entwerfen](../books/story-editor.md#drafting-the-instructions) kann, die für den nächsten Zug gelten, und zwar mit den Daten des Kapitels, bei dem die Geschichte gerade steht, als ihrem **Plan der Geschichte**. Das aktuelle Kapitel ist das erste Kapitel, das als **Current** markiert ist; gibt es keines, das erste, das nicht **Finished** ist; ein als aktuell markierter Arc ohne Kapitel steht allein. Das Modell erhält diesen Arc und dieses Kapitel mit ihren Beschreibungen und Notizen sowie Titel und Beschreibung (nicht die Notiz) des darauffolgenden Kapitels, zusammen mit dem Hinweis, dass die Geschichte dorthin noch nicht gelangen darf. Das Fenster beginnt mit dem, was du im Feld *Instructions* des Panels für den neuen Zug eingetragen hast, als Leitlinie; **Use as Instructions** übernimmt den Entwurf in dieses Panel und öffnet es. Ohne aktuelles Kapitel sagt die Schaltfläche das ausdrücklich.

Dieselben Daten dienen auch als Quelle für die [Side Query](side-query.md), siehe [unten](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Einen Arc abschließen

Wenn du einen Arc als **Finished** markierst, werden auch alle seine Kapitel als **Finished** markiert. Wechselst du einen Arc in einen anderen Zustand, bleiben seine Kapitel unverändert.

Die Gliederung wird mit dem Buch gespeichert, sobald du sie änderst, sodass sie in dessen Sicherungen enthalten ist.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## Die Gliederung als Quelle für andere Erweiterungen

Die Gliederung wird anderen Erweiterungen als Datenquelle über das Buch angeboten: In der [Side Query](side-query.md) setzt du im Popover des Plug-Symbols einen Haken bei **Arc outline**, um die Arcs, ihre Zustände, Beschreibungen und Kapitel zusammen mit deinen Fragen zu senden. Es gibt zwei Fassungen: **Arc outline** ist die knappe (nur Beschreibungen), **Arc outline (with notes)** fügt unter der Beschreibung jedes Arcs und Kapitels dessen Notiz hinzu; dort stehen die Details generierter Arcs und Kapitel. Deine privaten Notizen werden nur gesendet, wenn du die zweite Option ankreuzt. Eine dritte Quelle, **Arc outline: current chapter**, sendet nur den Arc und das Kapitel, bei dem die Geschichte gerade steht (siehe [Anweisungen für das aktuelle Kapitel entwerfen](#drafting-the-instructions-for-the-current-chapter)).

<a id="generating-an-arc"></a>

## Einen Arc generieren

Der Zauberstab neben **Add new arc** öffnet ein Menü mit drei Einträgen: **Generate an arc** bittet das Modell, einen neuen Arc zu schreiben, **Import from story** fasst zusammen, was du bereits geschrieben hast, siehe [unten](#importing-an-arc-from-the-story), und **Develop the plot** ist ein Gespräch mit dem Modell darüber, wohin die Geschichte führt, siehe [unten](#developing-the-plot-in-a-conversation).

![Das Menü des Zauberstabs](../../images/plugin-arcoutline-menu.png)

 Der Dialog von **Generate an arc** hat zwei Hälften.

Links:

| | |
|---|---|
| Prompt | Die Anweisungen für das Modell mit drei Variablen: `{{backgroundLore}}` (die Einträge des Lorebooks des Buches und seiner Unter-Lorebooks), `{{existingArcs}}` (deine Arcs und Kapitel mit ihren Zuständen und Beschreibungen, aber ohne deine privaten Notizen) und `{{specification}}`. Er beginnt mit dem Prompt aus *Settings → Extension Settings → Arc Outline* (der Standard-Prompt, solange du ihn nicht geändert hast); was du hier bearbeitest, gilt nur für diese Generierung. |
| Spezifikation | Was du vom Arc erwartest: das Thema, die Figuren, wohin er führen soll. Kann leer bleiben. |
| Tools | Nur wenn eine Sammlung von [Tools für das Modell](tools.md) installiert ist (Namen, Orte): welche Sammlungen und welche ihrer Kategorien das Modell nutzen darf, statt Dinge zu erfinden, genau wie beim [Lore Generator](lore-generator.md). |
| Modell, Protokolle | Beginnen mit dem Modell und dem Protokoll des Buches (deine Standardwerte aus *Settings*, wenn das Buch keine hat). Ändere sie, um für diese Generierung ein anderes Modell zu verwenden; die Auswahl wird nicht gespeichert. |
| Generieren | Startet das Modell. Während es läuft, ist die Schaltfläche ein rotes **Stopp**, und alles andere im Dialog ist deaktiviert. |

Rechts siehst du die **Überlegungen** des Modells, während sie geschrieben werden, und sobald die Antwort beginnt, den **Arc**, den es vorschlägt – einen Namen, eine knappe Beschreibung und eine längere Notiz –, dargestellt wie ein Arc der Gliederung (die Notiz unter seiner Beschreibung) und mit dem Eintreffen der Antwort wachsend. Die Beschreibung ist das, was die Gliederung anzeigt, deshalb wird das Modell gebeten, sie auf ein paar Sätze zu beschränken und die Details in die Notiz zu schreiben, die in der **Notiz** des gespeicherten Arcs landet. Wenn das Modell fertig ist, wird seine Antwort geprüft; ist sie kein gültiger Arc (etwa fehlt die Notiz), sagt der Dialog das und der Arc kann nicht gespeichert werden.

![Einen Arc generieren](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** fügt den Arc am Ende der Gliederung hinzu. Um es erneut zu versuchen, drücke noch einmal **Generieren**, mit demselben oder einem geänderten Prompt oder einer geänderten Spezifikation; die vorherige Antwort wird ersetzt.
<a id="importing-an-arc-from-the-story"></a>

## Arc aus der Geschichte importieren

**Aus Geschichte importieren** im Menü des Zauberstabs bittet das Modell, einen Teil deiner Geschichte zu lesen und als einen Arc mit seinen Kapiteln niederzuschreiben, damit du ein bereits geschriebenes Buch in die Gliederung übernehmen kannst. Das Modell liest immer den [aktiven Zweig](../books/branches-and-story-tree.md) des Buches, so wie er ist, wenn du den Dialog öffnest. Der Dialog ist wie der für Arcs aufgebaut, mit diesen Auswahlmöglichkeiten auf der linken Seite:

| | |
|---|---|
| Von Nachricht, Bis Nachricht | Der Bereich der Geschichte, der gesendet wird. Die Nachrichten des Zweigs, die Text haben, sind ab 1 nummeriert; zu Beginn ist der ganze Zweig ausgewählt. Unter jedem Feld siehst du den Anfang der Nachricht, auf die es zeigt. |
| Anzahl der Kapitel (optional) | Wähle eine Zahl, um genau so viele Kapitel zu erhalten (eine Antwort mit einer anderen Zahl wird abgelehnt). Lass das Feld leer, damit das Modell die Geschichte dort teilt, wo sie sich natürlich teilt. |
| Kapitel mit den Teilen der Geschichte verknüpfen | Zu Beginn aktiviert. Die Nachrichten sind innerhalb des gesendeten Bereichs ab 1 nummeriert, und das Modell gibt an, in welcher Nachricht jedes Kapitel beginnt. Das Kapitel wird mit diesem Teil der Geschichte [verknüpft](#linking-to-the-story) (nummeriert im ganzen Zweig, sodass es unabhängig vom Bereich stimmt), und der Arc mit dem Teil, in dem sein erstes Kapitel beginnt. Eine Antwort, die eine Nachricht nennt, die nicht gesendet wurde, lässt das Kapitel unverknüpft. |
| Zustand des importierten Arcs und seiner Kapitel | Zu Beginn **Abgeschlossen**, weil die Geschichte geschrieben ist; wähle **Aktuell** oder **Geplant**, wenn du das brauchst. |
| Token-Zähler | Wie viele Nachrichten der Bereich hat und ihre ungefähren Token sowie die ungefähren Token des gesamten Prompts (Anweisungen, Lore, Gliederung und Geschichte) neben dem, was das Modell aufnimmt. Es wird zu einer Warnung, wenn der Prompt nicht passt; wähle dann weniger Nachrichten. Er folgt dem Bereich, der Anzahl der Kapitel, dem Prompt und dem Modell. |
| Prompt | Die Anweisungen für das Modell, mit den Variablen `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (die Nachrichten des Bereichs) und `{{numberOfChapters}}`. Er beginnt als **Prompt für den Geschichtsimport** unter *Einstellungen → Erweiterungseinstellungen → Arc Outline*. |

![Importieren eines Arcs aus der Geschichte](../../images/plugin-arcoutline-import.png)

Hier gibt es keine Werkzeuge, die Namen stehen in der Geschichte. Auf der rechten Seite siehst du die Begründung, dann den Arc und seine Kapitel, so wie das Modell sie schreibt. **Als Arc speichern** fügt den Arc mit seinen Kapiteln am Ende der Gliederung hinzu; jedes von ihnen behält die Begründung und die Antwort des Modells und hat seinen eigenen Roboter-Button. Eine lange Geschichte importiert man am besten in mehreren Schritten, ein Arc für jeden Bereich.
<a id="developing-the-plot-in-a-conversation"></a>

## Die Handlung in einem Gespräch entwickeln

**Handlung entwickeln** im Menü des Zauberstabs öffnet einen Chat mit dem Modell über die Handlung. Am Buch ändert sich durch das Gespräch nichts: Das Modell sieht, was du zu senden wählst, antwortet, und wenn ihr euch auf etwas geeinigt habt, gibst du es mit einem Klick an den Bogengenerator weiter.

Links wählst du, was das Modell sieht und wie es hilft (die Auswahl wird für das nächste Mal gespeichert), rechts steht der Chat:

![Der Handlungsassistent: die Optionen links, das Gespräch rechts](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Modell, Protokolle | Beginnen mit dem Modell und Protokoll des Buches. Eine Änderung gilt nur für dieses Gespräch. |
| Wie das Modell hilft | **Freies Gespräch** (du führst), **Richtungen erkunden** (mehrere verschiedene Wege, wie die Geschichte weitergehen kann), **Probleme finden** (nie aufgelöste Fäden, nicht eingelöste Versprechen, Widersprüche zum Hintergrundwissen, Tempo) oder **Eine Idee ausbauen** (deine Idee als Prämisse ausgearbeitet, Wendepunkte, ein Ende und Kapitel, die nach deinen Bögen passen). |
| Lorebuch senden | Die Einträge des Lorebuchs des Buches und seiner Unter-Lorebücher. |
| Gliederung senden | **Nicht senden**, **Nur Beschreibungen** oder **Mit den Notizen** (auch deine privaten Notizen). |
| Notizen senden | Nur wenn [Story-Notizen](story-notes.md) installiert ist: **Nicht senden**, **Titel und erste Zeilen** (der Standard) oder **Volltext** deiner Notizen, ohne die Seiten, die du vor Assistenten verborgen hast. |
| Zusammenfassungen der Geschichte senden | Die Zusammenfassungen stehen für die Geschichte bis zur neuesten von ihnen, so wie eine Generierung sie sendet. |
| Die neuesten Teile der Geschichte senden | Die Teile nach der neuesten Zusammenfassung (alle, wenn Zusammenfassungen aus sind), die neuesten, die in **Token der Geschichte** passen. Jeder Teil wird dem Modell unter seiner Nummer in der Gliederung der Geschichte gezeigt. |
| Token der Geschichte | Die höchstens zulässige Anzahl Token an Geschichtsteilen. Leer: ein Drittel dessen, was das Modell aufnimmt. |
| Prompt | Die Anweisungen an das Modell mit den Variablen `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` und `{{mode}}`. Die Änderung wird gespeichert. |

Unter den Optionen zeigt eine Zeile an, wie viele Zusammenfassungen und Geschichtsteile das Modell erhält und wie viele Token die Anfrage von dem hat, was das Modell aufnimmt. Sie wird zu einer Warnung, wenn es nicht passt; sende dann weniger oder [fasse den Chat zusammen](#chats-branches-and-summaries). Das Buch wird für jede Nachricht neu gelesen, daher sieht die nächste Antwort eine Änderung der Gliederung oder der Geschichte.

Rechts steht der Chat. Schreibe eine Nachricht und drücke **Senden** (während das Modell schreibt, steht dort **Stopp**; was bereits angekommen ist, bleibt erhalten). Jede Nachricht hat Schaltflächen: kopieren, **Zweig ab hier**, löschen, und bei einer Antwort des Modells außerdem einen Zauberstab, **Bogen aus dieser Nachricht erzeugen**, der den Bogengenerator mit der Nachricht als Vorgabe öffnet. Bitte das Modell zuerst, zusammenzufassen, was du entschieden hast, wenn die Diskussion lang war.

<a id="letting-the-model-change-the-book"></a>

### Das Modell das Buch ändern lassen

Unter den Optionen steht **Was das Modell tun darf**. Ist keine dieser Gruppen angekreuzt, ist der Chat nur ein Gespräch. Kreuzt du eine Gruppe an, erhält das Modell Werkzeuge dafür und kann sie nutzen, wenn du darum bittest oder zustimmst; es wird angewiesen, vor dem Hinzufügen nachzusehen, Ergänzen dem Ersetzen vorzuziehen und zu sagen, was es getan hat:

| Gruppe | Das Modell kann |
|---|---|
| **Planer** | die Szenen des [Planers](../books/story-editor.md#the-planner-tab) auflisten, sie hinzufügen, ändern, verschieben, löschen und als erledigt markieren. |
| **Lorebuch** | im Hintergrundwissen suchen und es lesen, Einträge anlegen und vorhandene ändern (nicht löschen). |
| **Gliederung** | die Gliederung lesen, Bögen und Kapitel hinzufügen, ändern und verschieben (nicht löschen). |
| **Notizen** (wenn [Story-Notizen](story-notes.md) installiert ist) | deine Notizen durchsuchen und lesen und deren Links folgen, eine Seite hinzufügen oder zu einer Seite etwas ergänzen, wenn du willst, dass etwas festgehalten wird, und Text in einer Seite ändern (keine Seite löschen). Seiten, die du vor Assistenten verborgen hast, sind nicht erreichbar. |
| **Generatoren** | den Bogen- und den Kapitelgenerator aufrufen und, wenn der [Lore-Generator](lore-generator.md) installiert ist, einen Hintergrundwissen-Eintrag erzeugen. Sie liefern einen Entwurf zurück und speichern nichts, außer das Modell wird dazu angewiesen; es zeigt dir den Entwurf und speichert ihn, wenn du zustimmst. |
| **Werkzeuge** (Schaltfläche, wenn eine [Werkzeugsammlung](tools.md) installiert ist) | die Namenswerkzeuge (Namen, Orte) wie bei den Generatoren, damit es dir echte Namen vorschlagen kann. |

- **Vor einer Änderung oder Löschung fragen** (standardmäßig an): Das Ändern des Textes von Vorhandenem (ein Hintergrundwissen-Eintrag, eine Szene, ein Bogen) und Löschen werden dir zuerst gezeigt, mit dem, was das Modell übergeben will, und das Modell wartet auf **Erlauben** oder **Ablehnen**. Das Hinzufügen einer Szene, eines Eintrags oder eines Bogens wird nicht abgefragt: Du siehst es im Chat und im Planer, im Lorebuch oder in der Gliederung und kannst es löschen.
- Jedes Werkzeug, das das Modell aufgerufen hat, wird unter seiner Antwort aufgelistet (eine Zeile mit dem Namen und einer Markierung: ✓ hat funktioniert, ✗ ist fehlgeschlagen; öffne sie, um zu sehen, was übergeben und was zurückgegeben wurde). Die nächsten Nachrichten enthalten die Aufrufe des Modells und deren Ergebnisse (ein langes Ergebnis wird gekürzt), damit es weiß, was es schon getan hat und nicht denselben Eintrag noch einmal anlegt; sie zählen auf das Token-Limit des Chats.
- Was die Werkzeuge kosten: Ihre Beschreibungen sind Teil jeder Anfrage, daher hat ein Modell mit knappem Kontext weniger Platz. Nur Modelle, die Funktionen aufrufen können, arbeiten damit; der Dialog sagt es dir, wenn ein Modell das nicht kann.

![Das Modell hat zwei Szenen zum Planer hinzugefügt; seine Aufrufe sind unter der Antwort aufgelistet](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Chats, Zweige und Zusammenfassungen

Ein Buch kann beliebig viele Chats haben; wähle einen in der Liste über den Nachrichten, **+** startet einen neuen, der Stift benennt ihn um (bis dahin heißt er nach deiner ersten Nachricht) und der Papierkorb löscht ihn. Sie werden mit dem Buch gespeichert.

- **Zweig** kopiert den Chat unverändert in einen neuen Chat; die Zweig-Schaltfläche an einer Nachricht kopiert ihn bis einschließlich dieser Nachricht, um das Gespräch von dort anders weiterzuführen. Die Kopie hat danach nichts mehr mit dem Original zu tun.
- **Chat zusammenfassen** fragt zuerst, was die Zusammenfassung behalten oder betonen soll (optional, zum Beispiel „die Wendung um den Verräter und das verworfene Ende behalten“; leer: das Modell entscheidet), bittet dann das Modell um eine Zusammenfassung des Chats (was du entschieden hast, was du verworfen hast, was noch offen ist, die festgelegten Namen) und fügt sie als Nachricht hinzu. Von da an erhält das Modell diese Zusammenfassung und die Nachrichten danach, **nicht** die Nachrichten darüber, die auf dem Bildschirm bleiben, abgedunkelt. Nutze das, wenn der Chat für das Modell zu lang geworden ist oder um von einem sauberen Stand aus zu beginnen, ohne die Entscheidungen zu verlieren. Fasst du später erneut zusammen, deckt die neue Zusammenfassung die alte mit ab. Zweige zuerst, wenn du dem Modell den ganzen Chat behalten willst.

![Die Frage, was die Zusammenfassung behalten soll](../../images/plugin-arcoutline-wizard-summarize.png)

![Der Chat nach der Zusammenfassung: die Nachrichten darüber bleiben abgedunkelt auf dem Bildschirm](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Erzeugen von Kapiteln

Die Zauberstab-Schaltfläche an einem Handlungsbogen bittet das Modell, diesen Bogen in Kapitel zu gliedern. Der Dialog ähnelt dem für Handlungsbögen, mit diesen Auswahlmöglichkeiten auf der linken Seite:

| | |
|---|---|
| Was das Modell von der Geschichte kennt | **Gesamte Geschichte bisher**: die Handlungsbögen bis einschließlich diesem, mit all ihren Kapiteln. **Nur Zusammenfassungen der Handlungsbögen**: dieselben Bögen ohne Kapitel. **Zusammenfassungen der Handlungsbögen und abgeschlossene Kapitel**: dieselben Bögen, mit nur den Kapiteln, die Sie als *Abgeschlossen* markiert haben. Nachfolgende Handlungsbögen werden nie gesendet. |
| Was Sie allgemein von den Kapiteln erwarten | Tempo, Ton, was geschehen muss. Kann leer bleiben. |
| Anzahl der Kapitel | In Worten angeben („etwa acht“) oder eine genaue Zahl wählen. Eine Zahl fügt für jedes Kapitel ein Textfeld hinzu, in dem Sie angeben können, was es enthalten soll; alle sind optional. Die Verwendung einer der beiden Angaben löscht die andere. Bei einer genauen Anzahl wird eine Antwort mit einer anderen Kapitelzahl abgelehnt. |
| Prompt | Die Anweisungen für das Modell, mit den Variablen `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` und `{{chapterSpecification}}`. Er beginnt mit dem **Prompt für die Kapitelerzeugung** aus *Einstellungen → Erweiterungseinstellungen → Arc Outline*. |

Rechts sehen Sie die Begründung, dann den Handlungsbogen mit den Kapiteln, die er bereits hat, und die neuen Kapitel, während das Modell sie schreibt. **Als Kapitel speichern** fügt sie am Ende des Bogens hinzu, jeweils mit einer knappen Beschreibung und den Details in ihrer Notiz (wie bei Bögen). Jedes neue Kapitel behält die Begründung und die Antwort des Modells und hat seine eigene Roboter-Schaltfläche.

![Erzeugen der Kapitel eines Handlungsbogens](../../images/plugin-arcoutline-generate-chapters.png)

Ein vom Modell geschriebener Handlungsbogen oder ein Kapitel behält seine **Begründung** und die **Antwort**, aus der er stammt. Die Roboter-Schaltfläche daran öffnet beides in einem kleinen Popover, aus dem Weg.

![Was das Modell für einen Handlungsbogen geschrieben hat](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Befehle

Die Gliederung und ihre Generatoren sind auch [Befehle](../commands/index.md), für die Kommandozeile und für
[Agenten](../agents.md). Sie arbeiten am geöffneten Buch (ein Agent: an seiner Arbeitskopie), und ein offener Tab *Bögen* zeigt die Änderung sofort an. Handlungsbögen und Kapitel werden über die **Nummer** angegeben, so wie die Gliederung sie anzeigt: der erste Bogen ist `1`, das erste Kapitel eines Bogens ist `1` (die Nummern ändern sich, wenn davor etwas hinzugefügt, entfernt oder verschoben wird). Ein Befehl, der einen Bogen oder ein Kapitel ändert, nimmt nur die Felder, die Sie angeben; ein leerer Text löscht eine Beschreibung oder eine Notiz.

| Befehl | Was er tut |
|---|---|
| `/get-arc-outline [arc [notes]]` | Die Bögen mit ihren Kapiteln, Zuständen und Beschreibungen als JSON. Die privaten Notizen nur mit `notes` `true`; `arc` liefert nur diesen einen Bogen. Liest nur. |
| `/add-arc title [description [note [state [position]]]]` | Fügt einen Bogen hinzu, am Ende oder an `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Ändert einen Bogen. `state` ist `PLANNED`, `CURRENT` oder `FINISHED` (einen Bogen abzuschließen schließt seine Kapitel ab). |
| `/move-arc arc position` | Verschiebt einen Bogen an die Stelle `position` (1 ist der erste). |
| `/delete-arc arc` | Löscht einen Bogen mit seinen Kapiteln, nach einer Bestätigung. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Fügt einem Bogen ein Kapitel hinzu. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Ändert ein Kapitel. |
| `/move-outline-chapter arc chapter position [to_arc]` | Verschiebt ein Kapitel innerhalb seines Bogens oder in den Bogen `to_arc`. |
| `/delete-outline-chapter arc chapter` | Löscht ein Kapitel, nach einer Bestätigung. |
| `/generate-arc [specification [save]]` | Bittet das Modell um einen neuen Bogen, so wie *Einen Bogen erzeugen* es tut. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Bittet das Modell um die Kapitel eines Bogens, so wie der Zauberstab des Bogens es tut. `count` ist eine genaue Kapitelzahl, `context` ist `EVERYTHING` (der Standard), `ARCS_ONLY` oder `ARCS_AND_FINISHED`, entsprechend der Auswahl *Was das Modell von der Geschichte kennt*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Bittet das Modell, die Nachrichten `from` bis `to` des aktiven Zweigs (nummeriert unter den Nachrichten mit Text, wie bei *Aus Geschichte importieren*) als Bogen mit Kapiteln zusammenzufassen. `state` (Standard `FINISHED`) ist der Zustand des gespeicherten Bogens und seiner Kapitel. Mit `mapParts` `true` wird jedes Kapitel mit der Nachricht verknüpft, in der es beginnt (sein *Verknüpfter Abschnitt*, so wie das Kontrollkästchen *Abschnitte zuordnen* des Dialogs es tut), und die Antwort enthält den Abschnitt jedes Kapitels. |

Die drei erzeugenden Befehle verwenden dieselben Prompts wie die Dialoge (Ihre *Erweiterungseinstellungen → Arc Outline* oder die Standardwerte), das Modell und das Protokoll des Buchs (Ihr Standardmodell, wenn das Buch keines hat) und keine Werkzeuge. Wer Namen möchte, erhält sie mit den Befehlen des [Namensgenerators](name-generator.md) oder des [Namensanbieters](name-provider.md) und schreibt sie in die Spezifikation. Wie die Dialoge speichern sie nur auf Anfrage: Mit `save` `false` (der Standard) geben sie den Bogen oder die Kapitel als JSON zurück und speichern nichts, mit `save` `true` fügen sie sie der Gliederung hinzu (ein Bogen am Ende, Kapitel am Ende des Bogens), und die Antwort enthält ihre Nummern. Eine Geschichte, die für das Modell zu lang ist, wird von `/import-arc-from-story` abgelehnt: wählen Sie weniger Nachrichten. Die erzeugenden Befehle brauchen eine Weile und enden, wenn der Lauf gestoppt wird.

Hinzufügen und Ändern ist nicht destruktiv; nur die beiden `delete`-Befehle sind es, und ein Agent kann so eingestellt sein, dass er Sie dazu fragt.
