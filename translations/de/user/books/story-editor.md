---
label: Story-Editor
order: 90
---

# Der Story-Editor

Der Tab **Story** eines Buchs ist der Ort, an dem du schreibst. Er zeigt die Teile des [aktiven Zweigs](branches-and-story-tree.md)
vom Anfang bis zum Ende.

![Der Story-Editor](../../images/hero-story-editor.png)

Diese Seite beschreibt das Desktop-Layout. Auf einem Telefon besteht die Geschichte aus einer einzigen Spalte, und die
Gliederung befindet sich in einer Schublade, siehe [Marginalia auf dem Telefon verwenden](../phone.md#the-story).

| Bereich | |
|---|---|
| **Outline** (links) | Ein Eintrag pro Teil, `#1`, `#2`... mit der Datenbank-ID des Teils. Klicke auf einen Eintrag, um zum Teil zu scrollen (die davor liegenden Teile werden zuerst geladen, wenn sie noch nicht geladen sind). Die Kopfzeile zeigt, wie viele Teile der Zweig hat. Ein Teil, der eine [Zusammenfassung](summaries.md) hat, zeigt nach seinem Eintrag ein kleines Dokumentsymbol. Die Erweiterung [Chapter Marker](../extensions/chapter-marker.md) zeigt hier die Kapiteltitel an. |
| **Lorebook** (links, neben der Outline) | Ein Blick auf das Wissen des Buchs, mit einem Suchfeld und einer schnellen Bearbeitung des Texts eines Eintrags, siehe [Der Lorebook-Tab](#the-lorebook-tab). |
| **Annotations** (links, neben der Outline) | Die [Notizen](annotations.md) zu Stellen der Geschichte, für das ganze Buch. |
| **Planner** (links, neben der Outline) | Die Szenen, die du planst, eine Liste, die du von Hand füllst, siehe [Der Planner-Tab](#the-planner-tab). |
| **Story** (Mitte) | Der Text jedes Teils mit seinem Menü (☰) und seinen Metadaten rechts, darunter die untere Leiste. |
| **Untere Leiste** | Die Schaltfläche **Summaries** (Listensymbol) und das Einstellungsmenü (⚙) links, die Befehlszeile und die Schaltfläche **+** rechts. |

So sieht ein neues Buch aus. Alle diese Bereiche sind Tabs, die du beliebig anordnen kannst, auch die Geschichte, siehe
[Tabs anordnen](#arranging-the-tabs). Der Trenner zwischen der Seitenleiste und der Geschichte lässt sich ziehen. Marginalia merkt sich,
wo du ihn gelassen hast (er wird zusammen mit dem Buch gespeichert), und kehrt dorthin zurück, wenn du das Buch wieder öffnest.

Die Geschichte öffnet sich immer unten, beim neuesten Teil. Ein langes Buch wird nicht auf einmal gezeichnet: Es werden
nur die letzten Teile geladen, und die früheren werden oben hinzugefügt, während du nach oben scrollst, ohne den Teil zu
verschieben, den du gerade liest. Die Teile am anderen Ende werden wieder entfernt, sodass nicht mehr als die Anzahl
geladen bleibt, die in der Einstellung **Parts loaded at once in the story** deines [Kontos](../account.md) festgelegt ist
(leer bedeutet, dass alle geladen werden). Wenn du schneller scrollst, als die Teile ankommen, zeigt eine Markierung
*Loading earlier parts* (bzw. *later parts*) am Rand, dass es mehr gibt. Ein Klick auf einen Teil in der Gliederung oder
das Finden eines Teils mit der Suche lädt die Teile um ihn herum, und das Beginnen eines neuen Teils bringt dich zurück
ans Ende der Geschichte.

<a id="searching-the-story"></a>

### Die Geschichte durchsuchen

Über der Gliederung befindet sich ein Suchfeld für den Text des **Zweigs, auf dem du dich befindest** (die Suche im
Story-Baum durchsucht alle Zweige). Da ein langes Buch nicht auf einmal geladen wird, kann die Suche des Browsers
(Ctrl+F) keinen Text in den nicht geladenen Teilen finden. Diese Suche kann es.

- Gib ein, wonach du suchst; die Suche startet, sobald du aufhörst zu tippen. Mehrere Wörter müssen alle im selben Teil
  vorkommen, eine Wortgruppe steht in doppelten Anführungszeichen (`"old key"`), und `*` steht für beliebige Zeichen (`light*house`).
  Groß- und Kleinschreibung spielen keine Rolle.
- Jede Stelle, an der ein Wort steht, ist ein Treffer, und die Treffer sind durch den ganzen Zweig durchnummeriert. Unter
  dem Feld siehst du *Match 3 of 27, in part #5* und eine Leiste, die zeigt, wie weit du bist. Die Geschichte scrollt zum
  Treffer und lädt die Teile um ihn herum, wenn sie nicht geladen sind.
- Die Schaltflächen **up** und **down** gehen zum vorherigen bzw. zum nächsten Treffer (nach dem letzten geht es wieder
  zum ersten). **Enter** im Feld tut dasselbe wie die Schaltfläche **down**, **Shift+Enter** wie die Schaltfläche **up**.
- Alle Treffer in den geladenen Teilen sind im Text hervorgehoben, der Treffer, bei dem du dich gerade befindest, in einer
  kräftigeren Farbe. Leere das Feld, um die Hervorhebung zu entfernen.

Auf dem Telefon befindet sich die Suche in der Gliederungsschublade. Die Schublade schließt sich, wenn sie einen Treffer
zeigt. Öffne sie erneut, um zum nächsten Treffer zu gelangen.

<a id="the-lorebook-tab"></a>

### Der Lorebook-Tab

Der Tab **Lorebook** der Seitenleiste zeigt das Wissen des Lorebooks des Buchs, ohne dass du die Geschichte verlassen
musst. So musst du nicht zwischen Tabs wechseln, um zu prüfen, was ein Eintrag sagt, oder um ein Wort darin zu
korrigieren. Er wird jedes Mal neu gelesen, wenn du zum Tab *Story* zurückkehrst, sodass Änderungen, die du im Tab
*Lorebook* des Buchs gemacht hast, sofort sichtbar sind.

- Das **Suchfeld** oben zeigt nur die Einträge, deren Name, Inhalt, Notiz oder Filter den Text enthält (Groß- und
  Kleinschreibung spielen keine Rolle).
- Der **Stift** eines Eintrags öffnet ein kleines Fenster mit seinem Namen, ob er verwendet wird, dem Filter und der Art,
  wie er gesucht wird, dem Inhalt und der Notiz. **Save** speichert sie. Die Tags, die Reihenfolge und die
  Aktivierungseinstellungen änderst du im Tab *Lorebook* des Buchs. Nichts kann geändert werden, während ein Agent am
  Lorebook arbeitet.
- Die **Erweitern-Schaltfläche** (⤢) öffnet den [Vollbild-Editor für das Lorebook](../lorebooks/lorebooks.md#editing-in-full-screen) (nur Desktop): den neben dem
  Suchfeld am Lorebook, den bei einem Eintrag an diesem Eintrag, den eines Unter-Lorebooks (mit *Include sub lorebooks*)
  an diesem Lorebook. Der Tab wird neu geladen, wenn du den Editor schließt.

- Jeder Eintrag ist eine Zeile mit seinem Namen und dem Stift. Der Pfeil öffnet ihn, um seinen **Inhalt** (als Markdown
  dargestellt) und den **Text, der ihn aktiviert** (das *Filtering Text/Pattern*, mit einem Hinweis, wenn es ein
  regulärer Ausdruck ist) zu zeigen.
- **Include sub lorebooks** zeigt das Lorebook und jedes seiner Unter-Lorebooks (und deren Unter-Lorebooks, egal wie tief)
  als eigene Zeilen, mit ihren Einträgen darunter. Klicke auf den Pfeil eines Lorebooks, um seine Einträge zu verbergen.
  Ohne diese Option werden nur die Einträge des Lorebooks des Buchs aufgelistet.
- Einträge und Lorebooks, die ausgeschaltet sind (sie werden für die Geschichte nicht verwendet), werden abgeschwächt
  angezeigt.
- Ein Buch ohne Lorebook zeigt stattdessen einen Hinweis.

![Der Lorebook-Tab mit zwei geöffneten Einträgen](../../images/story-lorebook-tab.png)

Wie jeder Tab des Editors kann er in ein eigenes Fenster gezogen werden, siehe unten.
<a id="the-planner-tab"></a>

### Der Planner-Tab

Der **Planner**-Tab ist eine Liste der Szenen, die die Geschichte durchlaufen soll, in der Reihenfolge, die du willst. Jede Szene besteht aus den Angaben des Zugs, der sie schreibt: einem **Titel** für die Liste, dem **Schauplatz der Szene**, der **POV-Figur**, den **anwesenden Figuren** und den **Anweisungen**, also denselben vier Feldern wie im Formular für einen neuen Zug.

![Der Planner-Tab mit vier Szenen, die erste bereits geschrieben](../../images/planner-tab.png)

- **Add scene** öffnet ein Formular für eine neue Szene; sie wird ans Ende der Liste gesetzt. Der Stift bearbeitet eine Szene, die Pfeile verschieben sie nach oben oder unten, der Papierkorb löscht sie.
- Das **+** in der Zeile einer Szene **fügt eine neue Szene direkt darunter ein**, zwischen diese Szene und die nächste.
- Die Verbinden-Schaltfläche einer Szene **führt sie mit der nächsten zusammen**: Es öffnet sich ein Formular mit den Angaben beider bereits vereint (die beiden Titel, die Schauplätze und die Anweisungen untereinander, die anwesenden Figuren beider Szenen, die POV-Figur der ersten; hat die zweite Szene eine andere, nennt das Formular sie). Ändere, was nicht passt, und drücke **Merge** (oder <kbd>Ctrl/Cmd+Enter</kbd>): Die beiden Szenen werden zu einer, an der Stelle der ersten. Das geschieht nur, wenn beide Szenen geschrieben wurden.
- **Import from the branch** fügt für jeden Teil des Zweigs, auf dem sich das Buch befindet (vom ersten Teil bis zum neuesten), der Zugangaben hat, eine Szene hinzu: mit Schauplatz, POV-Figur, anwesenden Figuren und Anweisungen, wobei die erste Zeile der Anweisungen als Titel dient. Teile ohne Zugangaben werden ausgelassen, und die Szenen kommen nach den vorhandenen, nicht als erledigt markiert. Verwende das, um eine Umschreibung einer bereits geschriebenen Geschichte zu planen: importieren, zusammengehörige Züge zusammenführen, Fehlendes einfügen und die Szenen dann auf einem neuen Zweig oder in einer Kopie des Buchs schreiben.
- Das Kästchen vor einer Szene zeigt an, dass sie **erledigt** (geschrieben) ist: Der Titel ist durchgestrichen und die Szene abgeblendet. Prüfen tut das niemand für dich, also hake es ab, wenn du die Szene geschrieben hast. Die Zeile über der Liste zählt, wie viele Szenen erledigt sind.
- **Klicke auf eine Szene**, um ihre Angaben in das **neue Zugformular** zu übernehmen (das Formular öffnet sich mit Schauplatz, POV-Figur, anwesenden Figuren und Anweisungen der Szene; was zuvor darin stand, wird ersetzt). Wenn du zufrieden bist, drücke **Generate**. Auf dem Telefon schließt sich zuerst die Schublade.
- Die Szenen werden zusammen mit dem Buch gespeichert und sind daher in seinen Sicherungen und Kopien enthalten. Auch ein Modell kann Szenen hinzufügen: Der [Handlungsassistent](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) der Erweiterung Arc Outline und ein [Agent](../agents.md) können den Planer mit [Befehlen](../commands/index.md#the-planner) lesen und ändern; die Liste zeigt sofort an, was sie hinzugefügt haben.

![Das Formular für eine neue Szene](../../images/planner-scene-dialog.png)

![Ein Klick auf eine Szene füllt das neue Zugformular mit ihren Angaben](../../images/planner-new-turn.png)

Wie jeder Tab des Editors lässt sich auch dieser in ein eigenes Fenster ziehen, siehe unten.

<a id="arranging-the-tabs"></a>

### Die Tabs anordnen

Der Editor besteht aus Tabs: der **Story** selbst (die Teile und die Leiste darunter), der **Outline**, dem **Lorebook** und den Tabs, die [Erweiterungen](../extensions/index.md) hinzufügen (Characters, Arcs, Side Query ...). Ziehe einen Tab an seiner Kopfzeile, um die Tabs neu anzuordnen (nur am Desktop; das Telefon behält eine Spalte, wobei die Gliederung in einer Schublade liegt). Ein neues Buch zeigt die Outline und das Lorebook in einem Bereich links und die Story in einer eigenen Spalte rechts. Du kannst die Story beliebig platzieren, etwa in der Mitte, mit der Gliederung auf der einen und dem Lore auf der anderen Seite, oder über der Gliederung:

- Lass den Tab auf einem der gestrichelten Streifen fallen, die oberhalb, zwischen und unterhalb der Bereiche einer Spalte erscheinen, um ihn in einen **eigenen Bereich** in dieser Spalte **herauszulösen**. Die Bereiche einer Spalte werden untereinander gestapelt.
- Lass den Tab auf einem der hohen Streifen links, rechts oder zwischen den Spalten fallen, um ihn **seitlich herauszulösen**: Er bekommt eine eigene Spalte neben den anderen. Verbreitere den Editor mit den Trennern, um Platz für weitere Spalten zu schaffen.

Die Bereiche und Spalten sind durch Trenner voneinander getrennt: Zieh sie, um jedem Bereich den Platz zu geben, den er braucht. Die Größen werden zusammen mit der Anordnung gespeichert.

![Die Seitenleiste mit Characters in einer eigenen Spalte und Side Query darunter](../../images/story-editor-tabs.png)

- Lass den Tab auf der Kopfzeile eines anderen Tabs oder auf dem Inhalt eines anderen Bereichs fallen, um sich **mit diesem Bereich zu vereinen** (auf einem Tab landet er davor, auf dem Inhalt ganz hinten). Ein Bereich, der seinen letzten Tab verliert, verschwindet, ebenso eine Spalte ohne Bereiche.

Anordnung und Größen werden zusammen mit dem Buch gespeichert, daher bleiben sie beim nächsten Öffnen und in jedem Browser gleich, ebenso der Tab, den jeder Bereich zeigt. Ein Tab einer Erweiterung, die nicht geladen ist, behält seinen Platz, bis die Erweiterung wieder da ist.

Die Story ist immer irgendwo sichtbar: Ihr Tab kann nicht geschlossen werden, und ein Tab, der sich zusammen mit anderen Tabs in einem Bereich befindet, ist verborgen, während ein anderer ausgewählt ist. Was einen Teil anzeigt, holt die Story zurück: Ein Klick auf einen Eintrag der Gliederung, ein Treffer der Suche, die Wahl eines Zweigs im [Story-Baum](branches-and-story-tree.md) und der Start einer Generierung wählen zuerst den Tab **Story** aus. Während eine Generierung ihren Prompt vorbereitet (meist einige Sekunden), sind die anderen Tabs deaktiviert; sobald das Modell schreibt, sind sie wieder benutzbar, siehe [unten](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Tabs minimieren

Jeder Tab außer der Story hat neben seinem Namen eine kleine Schaltfläche **⌄** (Winkel). Sie **minimiert** den Tab: Der Tab verlässt seinen Bereich (ein Bereich, dessen Tabs alle minimiert sind, verschwindet vorübergehend), und ein runder Button mit seinem Symbol erscheint in einer Leiste am Rand des Editors. Das Symbol ist ein farbiger Kreis mit einer Zeichnung (eine Liste für die Gliederung, ein Buch für das Lorebook, eine Sprechblase für die Anmerkungen, eine Lupe für Side Query und so weiter; ein Tab einer Erweiterung ohne Zeichnung bekommt ein oder zwei Buchstaben). Fahre mit der Maus über das Symbol, um zu sehen, welcher Tab es ist. Nichts wird geschlossen, der Tab behält, was er angezeigt hat. Klickst du auf das Symbol, kehrt der Tab in seinen Bereich zurück, an die Stelle unter den Tabs, an der er zuvor war, und wird angezeigt.

Die Leiste bleibt, solange irgendein Tab minimiert ist. Die Schaltfläche an ihrer Spitze (die kreisenden Pfeile) setzt die Leiste an die nächste Seite des Editors im Uhrzeigersinn: links, oben, rechts, unten. Die Story kann nicht minimiert werden, daher ist auf dem Bildschirm immer etwas zu sehen.

![Minimierte Tabs in der Leiste am Rand des Editors](../../images/story-minimized-tabs.png)

Die Symbole stehen in alphabetischer Reihenfolge der Tabnamen, bis du sie änderst: Ziehe ein Symbol auf ein anderes, dann nimmt es dessen Platz ein. Welche Tabs minimiert sind, wo die Leiste liegt und die Reihenfolge der Symbole werden zusammen mit dem Buch gespeichert, zusammen mit der Anordnung und den Größen.

<a id="the-default-arrangement"></a>

### Die Standardanordnung

Ein neues Buch öffnet sich mit der Standardanordnung (Gliederung, Lorebook und Anmerkungen links, Story rechts). Um deine eigene Anordnung zur Standardanordnung zu machen, stelle die Tabs so ein, wie du sie möchtest (Bereiche, Größen, minimierte Tabs, Seite der Leiste), und wähle im Einstellungsmenü (⚙) der Story **Save layout as my default**. Jedes Buch ohne eigene Anordnung (also ein Buch, das du nicht umgeordnet hast) öffnet sich danach mit dieser Anordnung; ein Buch, das du umgeordnet hast, behält seine Anordnung. Die Standardanordnung wird in deinen Benutzereinstellungen gespeichert.

Um zur Standardanordnung zurückzukehren, wähle im Einstellungsmenü (⚙) der Story **Restore default layout** und bestätige. Das ist die Anordnung, die du gespeichert hast, oder die Standardanordnung, falls du keine gespeichert hast. Dabei werden nur die Tabs zurückgesetzt (die Größen und die minimierten Tabs werden vergessen): Deine Texte, der Tab, den jeder Bereich zeigt, und die Tabs der Erweiterungen bleiben erhalten; Letztere stehen dann im ersten Bereich.
<a id="writing-the-next-part"></a>

## Den nächsten Teil schreiben

1. Klicke unten rechts auf **+**. Das Panel *New Turn Instructions* öffnet sich.
2. Fülle die Felder aus. Alle sind optional, aber das Modell braucht zumindest etwas Anleitung:

   | Feld | |
   |---|---|
   | **Scene Setting** | Wo und wann der Teil spielt, z. B. *Der Leuchtturm, eine stürmische Nacht im späten Herbst*. |
   | **POV Character** | Aus wessen Sicht der Teil geschrieben ist. |
   | **Present Characters** | Wer in der Szene ist und in welchem Zustand. |
   | **Instructions** | Was in diesem Teil passieren soll. |

   *Scene Setting*, *POV Character* und *Present Characters* werden aus dem letzten Teil übernommen, du änderst also nur, was anders ist. *Instructions* ist jedes Mal leer.
3. Klicke auf **Generate**.

![Das Panel New Turn Instructions](../../images/new-turn-instructions.png)

Der neue Teil erscheint am Ende und füllt sich, während das Modell ihn schreibt. Bei [Reasoning-Modellen](../inference-providers.md#reasoning-models) erscheint zuerst die Überlegung unter **View Reasoning**.

Während ein Teil erzeugt wird, wird die **+**-Schaltfläche zu einem roten **Stop**-Button. Stoppen behält den bis dahin geschriebenen Text als Teil.

<a id="while-a-part-is-generated"></a>

### Während ein Teil erzeugt wird

In den ersten Sekunden, während der Prompt aus dem Buch zusammengesetzt wird, wartet das gesamte Buchfenster. Sobald das Modell zu schreiben beginnt, sind nur der neue Teil und die Teile davor gesperrt:

- **Der neue Teil** kann nicht angefasst werden, bis er fertig ist: kein Bearbeiten, kein Menü.
- **Die Teile davor** bleiben, wo sie sind: du kannst sie bearbeiten, annotieren und neu entwerfen, aber nicht löschen, zusammenführen oder verzweigen, und die Geschichte bleibt auf ihrem Zweig (kein Swipe, kein Wechsel des Zweigs). Eine Änderung an einem früheren Teil oder am Lore erreicht den gerade geschriebenen Teil nicht: dessen Prompt wurde bereits zusammengesetzt. Sie zählt für das, was danach kommt.
- **Alles andere ist frei**: das Lore, die Einstellungen und der Prompt des Buchs, die Tabs der linken Leiste (side query, arc outline, notes ...), die Befehlsleiste. Eine Anfrage an das Modell wartet auf eine freie Verbindung zum Modell, wenn die Generierung die einzige belegt.
- **Wartend**: ein zweiter Turn, Suche und Ersetzen, ein Import in das Buch und das Massenlöschen. Die Tabs tree und backup warten ebenfalls.

Was das Fenster ausgraut, wird auch abgelehnt, wenn ein Befehl, ein Agent oder eine Erweiterung es anfordert: Marginalia nennt, was im Weg ist (*This is in use: …*).

**Das Verlassen des Buchs stoppt die Generierung.** Der Exit-Button fragt zuerst nach; *Yes* entspricht **Stop**: was angekommen ist, bleibt erhalten. Das Schließen des Browser-Tabs tut dasselbe, und der Browser fragt nach, bevor der Tab geschlossen oder neu geladen wird, während ein Teil erzeugt wird. Ein kurzer Netzwerkausfall oder ein für eine Minute zugeklappter Laptop ist kein Verlassen: die Seite kommt zurück und zeigt, was in der Zwischenzeit angekommen ist.

Gibt das Modell einen Fehler zurück (Server nicht erreichbar, falscher API-Key, Kontext zu lang ...), wird der Fehler angezeigt. Ist noch kein Text angekommen, bleibt die Geschichte wie sie war: ein neuer Teil oder Swipe wird entfernt, und ein neu erzeugter Teil bekommt seinen vorherigen Text zurück. Text, der vor dem Fehler angekommen ist, bleibt erhalten, wie bei **Stop**. Behebe das Problem und erzeuge erneut.

Wie die Felder in den Prompt gelangen, legt das [User Prompt](prompts.md#user-prompt) des Buchs fest. Mit dem Standard-User-Prompt erhält das Modell sie als *Current Location & Time*, *Point of View Character*, *Characters Currently Present / State* und *Plot Specifications for the Next Part*.

!!!
Der Text wird in Markdown geschrieben: `*italics*`, `**bold**`, `# Heading` usw. werden formatiert angezeigt. Ein Teil mit einer `#`-Überschrift ist ein Kapitel für die Erweiterung [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Anweisungen entwerfen

Du weißt nicht, was als Nächstes passieren soll? **Draft Instructions** unter dem Feld *Instructions* des Panels lässt das Modell die Anweisungen an deiner Stelle schreiben, so wie du es tun würdest. Ein Fenster öffnet sich mit:

| | |
|---|---|
| **Guidance** | Worum der Teil gehen soll: eine grobe Idee, eine Richtung ("sie konfrontiert ihn wegen des Briefes") oder gar nichts; dann setzt das Modell die Geschichte fort, wohin sie führt. Anfangs steht darin, was du bereits in *Instructions* eingetippt hast. |
| **Plan of the Story** | Nur wenn etwas einen liefert. Die [Arc Outline](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) tut das, mit den Daten des aktuellen Kapitels. Nur lesbar; das Modell folgt ihm. |

**Draft Instructions** startet das Modell und streamt die Antwort in **Drafted Instructions**, das du ändern oder leeren kannst, bevor du weitermachst; **Draft Again** versucht es noch einmal, **Stop** behält, was angekommen ist. Nichts wird geändert, bis du auf **Use as Instructions** klickst: Der Text ersetzt dann die *Instructions* des Panels (die anderen Felder bleiben unberührt), und das Panel öffnet sich erneut, bereit für **Generate**.

Das Modell erhält die bisherige Geschichte (den älteren Teil als [Zusammenfassungen](summaries.md), wie bei einer Generierung), Szenerie, POV und anwesende Figuren aus dem Panel, das Lore des letzten Teils, deine letzten Anweisungen als Beispiele, wie du sie schreibst, deine Guidance und den Plan. Es sieht nicht die Lorebook-Einträge, die ein neuer Turn aktivieren würde. Die Anfrage ist der [Impersonate-Prompt](prompts.md#impersonate-prompt). Das Modell ist das des Buchs; das Entwerfen kostet Tokens wie jede Anfrage.

![Entwerfen der Anweisungen für den nächsten Turn](../../images/impersonate-dialog.png)

Dasselbe macht der Befehl [`/impersonate`](../commands/index.md), der den Text zurückgibt. [Agenten](../agents.md) bekommen ihn nicht; sie schreiben die Geschichte mit eigenen Anweisungen.

<a id="the-settings-menu"></a>

## Das Einstellungsmenü

Das Einstellungsmenü (⚙) in der unteren Leiste fasst zusammen, was auf die ganze Geschichte wirkt. **Search and replace** steht oben; der Rest ist in Untermenüs (auch auf dem Handy, außer den nur auf dem Desktop verfügbaren Punkten):

| Untermenü | Einträge |
|---|---|
| **Appearance** | **Change Styles For Text**, **Highlight Generated Text** und **Annotations in the margin** (nur Desktop, der letzte) |
| **Analysis** | **Annotate a range of parts with AI**, **Create summaries**, **Take lore notes with an agent**, **Automatic annotation**, **Phrase watch list**, **Branch Tokens**, **Lore Usage**, **Statistics** |
| **Import & export** | **Import**, **Export story** (und die Einträge der Erweiterungen, die importieren oder exportieren, etwa **Export instructions**) |
| **Cleanup** | **Clear history of the book**, **Empty trash of the history**, **Mass delete** |
| **Layout** (nur Desktop) | **Save layout as my default**, **Restore default layout** |

Einträge, die [Erweiterungen](../extensions/index.md) mitbringen, stehen im Untermenü, das zu ihnen passt, oder am Ende des Menüs. Die Tastenkürzel der Einträge stehen unter *Settings → Keyboard shortcuts*, wo ein Eintrag mit seinem Untermenü benannt wird (*Analysis › Statistics*). Die Abschnitte unten sagen, wo jeder Eintrag zu finden ist.
<a id="the-part-menu"></a>

## Das Teil-Menü

Jeder Teil hat in seiner oberen rechten Ecke ein Menü (☰). **Edit**, **Discard Changes**, **Regenerate**, **Swipe**, **Redraft**, **History** und **Images** stehen im Menü selbst, **Delete** ist dessen letzter Eintrag, und der Rest befindet sich in vier Untermenüs: **Annotations** (*Annotate part*, *Annotate with AI*), **Summary** (*Generate summary* / *View summary*, *Delete summary*), **Inspect** (*Show Prompt*, *Compare Prompt*, *Token Usage*, *Lore Usage*) und **Branch & merge** (*Branch Story*, *Merge into previous part*). Ein Untermenü, das für den Teil nichts enthält, wird nicht angezeigt. Der Reviewer fügt sein Untermenü **Review** nach *Summary* hinzu. Die Einträge:

| Eintrag | |
|---|---|
| **Edit** | Macht den Text zu einem bearbeitbaren Feld. **Save** (derselbe Menüeintrag) speichert ihn. Das Starten einer Generierung oder das Verlassen des Tabs speichert offene Bearbeitungen ebenfalls. |
| **Discard Changes** | Nur solange der Text bearbeitet wird: schließt das Feld ohne Speichern, der Teil behält den zuletzt gespeicherten Text. Wenn Sie den Text geändert haben, werden Sie zuerst gefragt. |
| **Regenerate** | Schreibt den letzten Teil neu und ersetzt dessen Text. Nur beim letzten Teil. |
| **Swipe** | Schreibt eine weitere Fassung des letzten Teils und behält die alte als eigenen Zweig. Nur beim letzten Teil. Siehe [Verzweigungen und Story-Baum](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Redraft** | Lässt das Modell diesen Teil anhand Ihrer Anweisungen neu schreiben, bei jedem Teil, siehe [unten](#redrafting-a-part). Nicht, während der Text bearbeitet wird. |
| **History (n)** | Die [Textfassungen](version-history.md) des Teils mit der Anzahl der Fassungen: vergleichen, eine zurückholen, eine benennen. Nicht, während der Text bearbeitet wird. |
| **Annotate part** | Erstellt eine [Notiz](annotations.md#making-an-annotation) zum ganzen Teil. Um einzelne Wörter zu annotieren, markieren Sie sie und klicken mit der rechten Maustaste (**Annotate selection**). |
| **Annotate with AI** | Lässt ein Modell den [Teil prüfen](annotations.md#letting-a-model-annotate) und Notizen vorschlagen. Nicht, während der Text bearbeitet wird. Für mehrere Teile auf einmal verwenden Sie **Annotate a range of parts with AI** im Einstellungsmenü (⚙), und für jeden neuen Teil **Automatic annotation** (siehe [Annotationen](annotations.md#annotating-every-new-part-automatically)). |
| **Branch Story** | Startet einen neuen Zweig ab diesem Teil, siehe [Verzweigungen und Story-Baum](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Merge into previous part** | Fügt den Text dieses Teils dem vorherigen Teil hinzu, siehe [unten](#merging-a-part-into-the-previous-one). Nicht beim ersten Teil. |
| **Show Prompt** | Zeigt den genauen Prompt, der für diesen Teil an das Modell gesendet wurde. |
| **Token Usage** | Zeigt, wie der Kontext des Modells für diesen Teil genutzt wurde: ein Diagramm der Abschnitte des Prompts (Anweisungen des Modells, Lore, Zusammenfassungen, bisherige Story, Ihre Anweisungen, für die Antwort reservierter Platz) und die Größe jeder an das Modell gesendeten Nachricht. Wenn ein Teil der Story nicht hineinpasste, steht dort, wie viele Teile weggelassen wurden. Teile, die geschrieben wurden, bevor dies erfasst wurde, haben kein Diagramm. |
| **Lore Usage** | Listet die [Lorebook](../lorebooks/index.md)-Einträge auf, die in den Prompt dieses Teils eingeflossen sind, mit den Tokens, die jeder kostete, und dem, was ihn gefunden hat (der Prompt oder der Inhalt eines anderen Eintrags, wenn das Lorebook tiefer scannt). Teile, die von Hand getippt, vor dieser Erfassung geschrieben oder für ein Buch ohne Lorebook geschrieben wurden, melden, dass nichts erfasst wurde. Für das ganze Buch verwenden Sie **Lore Usage** im Einstellungsmenü (⚙), siehe [unten](#which-lore-entries-were-used). |
| **Images** | Hängt Bilder an den Teil an, siehe [Bilder](#images). |
| **Generate summary** / **View summary** | Fasst die Story bis zu diesem Teil zusammen oder zeigt die Zusammenfassung, siehe [Zusammenfassungen](summaries.md). |
| **Delete summary** | Entfernt die Zusammenfassung dieses Teils. Bei einer [Meta-Zusammenfassung](summaries.md#meta-summaries) wird gefragt, ob sie aufgelöst oder ganz gelöscht werden soll. |
| **Delete** | Löscht den Teil, siehe [unten](#deleting-a-part). |

*Regenerate* und *Swipe* verwenden die Turn Details des letzten Teils (siehe [unten](#turn-details)). Um zu ändern, was geschehen soll, bearbeiten Sie sie zuerst.

![Das Menü eines Teils, mit geöffnetem Untermenü Inspect](../../images/story-part-menu.png)

![Token Usage eines Teils](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadaten des Teils

Neben jedem Teil:

| | |
|---|---|
| **Order #n \| DB ID: m** | Die Position des Teils im Zweig und seine ID in der Datenbank. |
| **Model** | Der Inferenzanbieter, der den Teil geschrieben hat. |
| **Tokens: x (y)** | `x` Tokens Text im Teil, `y` Tokens im Prompt, der ihn erzeugt hat. |
| **Words** | Wörter im Teil. |
| **Turn Details** | Die Anweisungen, aus denen der Teil geschrieben wurde. |

### Turn Details

**Turn Details** öffnet die *Scene Setting*, *POV Character*, *Present Characters* und *Instructions*, aus denen der Teil geschrieben wurde. Beim letzten Teil können sie geändert werden (Änderungen werden sofort gespeichert) und dienen *Regenerate* und *Swipe* als Grundlage. Bei früheren Teilen sind sie nur lesbar.

![Turn Details eines Teils](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Den Text bearbeiten

Generierter Text gehört Ihnen und darf geändert werden. Wählen Sie **Edit** im Teil-Menü, ändern Sie den Text und wählen Sie **Save**. Bearbeitete Teile werden so an das Modell gesendet, wie Sie sie bearbeitet haben.

Wenn Sie einen Teil bearbeiten, den eine [Zusammenfassung](summaries.md) abdeckt, wird diese Zusammenfassung veraltet; Marginalia bemerkt das bei der nächsten Generierung und verwirft die Zusammenfassung. Damit das nicht überraschend geschieht, prüft **Save** den neuen Text zuerst: Würde er Zusammenfassungen veralten lassen, zeigt ein Fenster, wie viele, und bietet **Back to editing** (nichts wird gespeichert, der Editor bleibt mit Ihrem Text offen), **Save anyway** (die Zusammenfassungen werden veraltet) und **Save and revalidate summaries** (der Text wird gespeichert und die Zusammenfassungen bleiben gültig, für eine Änderung, die nicht verändert, was sie sagen, etwa einen Tippfehler). Die Prüfung erfolgt bei **Save** (im Menü, per Tastenkürzel und über die Editor-Schaltfläche); die automatischen Speicherungen, etwa wenn Sie eine Generierung bei geöffnetem Editor starten, fragen nicht nach.

<a id="keyboard-shortcuts"></a>

## Tastenkürzel

Am Computer (das [Telefon-Layout](../phone.md) hat keine) lässt sich die Schreibschleife mit der Tastatur bedienen:

| Taste | |
|---|---|
| `+` | Öffnet das Feld *New Turn Instructions* mit dem Cursor in *Instructions*. |
| `Ctrl+Enter` | Generiert aus dem geöffneten Feld. `Esc` schließt es; die Tastatur geht zurück zur Story. |
| `Ctrl+.` | Stoppt die laufende Generierung. |
| `s` / `r` | **Swipe** / **Regenerate** den letzten Teil. |
| `e` | Öffnet den Editor des Teils, der am meisten auf dem Bildschirm sichtbar ist (der letzte Teil, wenn keiner sichtbar ist). |
| `Ctrl+S` | Speichert den Teil, der gerade bearbeitet wird (auch wenn der Cursor in seinem Text steht). |
| `/` | Setzt den Cursor in die Befehlsleiste (der Story-Tab wird zuerst angezeigt). |
| `Ctrl+Shift+F` | Öffnet den Tab *Outline* und setzt den Cursor in die Suche des Zweigs (`Enter` / `Shift+Enter` springen zum nächsten / vorherigen Treffer). |
| `Ctrl+Shift+H` | Öffnet das Fenster *Search and replace* des Buches. |
| `a` | **Annotate selection**: erstellt eine Notiz zum markierten Text in einem Teil. |
| `Alt+PageDown` / `Alt+PageUp` | Zeigt den nächsten / vorherigen Tab, durch die Tabs aller Blätter in der Reihenfolge des Layouts und dann wieder von vorn. |

Eine Taste ohne `Ctrl`, `Alt` oder `Meta` (etwa `+`, `s`, `r`, `e`, `a`, `/`) wirkt nur, wenn kein Textfeld den Fokus hat, damit sie dem Schreiben nie in die Quere kommt. Die Tastenkürzel funktionieren im Story-Tab des geöffneten Buches und nicht, solange ein anderes Fenster darüber geöffnet ist. Die Schaltflächen zeigen ihre Taste im Tooltip, und das Menü des letzten Teils zeigt sie nach *Edit*, *Regenerate* und *Swipe*. Auf einem Mac funktioniert `Cmd` dort, wo eine Taste `Ctrl` sagt (sofern Sie diese `Cmd`-Taste nicht einer anderen Aktion gegeben haben). Jede Taste können Sie unter [Settings → Keyboard shortcuts](../account.md#keyboard-shortcuts) ändern.
<a id="deleting-a-part"></a>

## Einen Teil löschen

**Delete** entfernt den Teil aus der Geschichte. Die Teile, die darauf folgten, bleiben erhalten und setzen am Teil davor an. Deshalb wird ein Teil in der Mitte der Geschichte einfach herausgenommen. Wird der letzte Teil gelöscht, wird der Teil davor zum neuen Ende des Zweigs.

<a id="deleting-many-parts-at-once"></a>

### Mehrere Teile auf einmal löschen

Das Einstellungsmenü (⚙) enthält **Mass delete**. Wird es eingeschaltet, erscheint links neben jedem Teil ein Kontrollkästchen, und unter der Geschichte wird eine Leiste mit der Anzahl der angehakten Teile und einer Schaltfläche **Delete** angezeigt. Hake die Teile an, die du entfernen möchtest. Die Haken bleiben beim Scrollen erhalten, denn die Geschichte lädt nur einen Ausschnitt der Teile. Ein Klick auf ein Kontrollkästchen mit gedrückter **Shift**-Taste hakt alle Teile zwischen diesem und dem zuletzt angeklickten an (oder hebt die Haken auf). Ein angehakter Teil wird rot hinterlegt. Drücke dann **Delete**: Nach einer Bestätigung werden die Teile nacheinander gelöscht, während ein Fortschrittsbalken läuft, der nicht abgebrochen werden kann. Jeder Teil wird so gelöscht wie ein einzelner Teil: Die Teile, die auf einen gelöschten Teil folgten, setzen am Teil davor an, und die gelöschten Teile landen im Papierkorb. Danach wird die Geschichte erneut angezeigt, und das Massenlöschen ist beendet. **Cancel** in der Leiste oder erneutes Wählen von **Mass delete** schaltet es aus, ohne etwas zu löschen. Das Starten einer Generierung schaltet es ebenfalls aus. Ein angehakter Teil, mit dem ein Fenster oder eine Übersetzung arbeitet, bleibt in der Geschichte erhalten, und Marginalia zeigt an, welches Fenster oder welche Übersetzung ihn verwendet.

![Massenlöschen mit einigen angehakten Teilen](../../images/story-mass-delete.png)

<a id="redrafting-a-part"></a>

## Einen Teil neu entwerfen

**Regenerate** und **Swipe** arbeiten nur am letzten Teil und schreiben ihn von Grund auf neu. **Redraft** arbeitet an jedem Teil, auch an einem alten in der Mitte der Geschichte, und behält bei, was du nicht ändern lassen möchtest. Wähle **Redraft** im Menü des Teils:

1. Schreibe in **Instructions for the redraft**, was sich ändern soll, zum Beispiel: *mehr Dialog zwischen den beiden einfügen*, *den Kampf verlängern*, *die Erklärung der Karte streichen*, *das Ende so lassen, wie es ist*. Ohne Anweisungen wird der Teil mit denselben Ereignissen neu geschrieben.
2. Wähle bei **Length**, ob der Teil **Shorter** oder **Longer** werden soll (Standard ist **Same length**). Dann wird nach deinen Anweisungen ein Satz angehängt, der dies verlangt (siehe [Prompts für kürzeren und längeren Redraft](prompts.md#redraft-shorter-and-longer)). Wähle **Redraft**. Der Text läuft in **Redrafted text** ein, und **Stop** beendet ihn vorzeitig. Wähle **Redraft again** für einen weiteren Versuch, gern mit anderen Anweisungen. **Differences** zeigt Wort für Wort, was gegenüber dem Teil geändert wurde. Hat der Teil offene [Anmerkungen](annotations.md), gibt **Deal with the open annotations of the part** (anfangs angehakt) sie ebenfalls an das Modell weiter, und **Apply** markiert sie als erledigt.
3. Der neu entworfene Text ist ein Feld, das du weiterhin bearbeiten kannst. Über das Eckensymbol wächst es auf die ganze Länge des Textes, und **Differences** folgt deinen Änderungen. **Apply** ersetzt mit diesem Text den Text des Teils, **Close** verwirft ihn. Vor **Apply** ändert sich nichts im Buch.

![Das Redraft-Fenster: die Anweisungen, der neue Text und die Unterschiede](../../images/redraft-dialog.png)

Das Modell erhält die [Redraft-Prompts](prompts.md#redraft-prompt): den Teil, deine Anweisungen und die Geschichte vor dem Teil. Die ältere Geschichte steht als [Zusammenfassungen](summaries.md), sofern gültige vorliegen, der Rest als Text. Passt nicht alles hinein, steht der neueste Teil zuerst. Dazu kommt so viel von der Geschichte nach dem Teil, wie hineinpasst (bis zu einem Viertel des verfügbaren Platzes). Ferner erhält es das Lore, das die Lorebücher dem Teil jetzt zuordnen. Es wird für den Zug des Teils neu berechnet, sodass eine Änderung an einem Eintrag den Redraft erreicht. Das Lore, mit dem der Teil geschrieben wurde, wird nur verwendet, wenn das Buch kein Lorebuch hat. Außerdem bekommt es die Erzählperspektive, die Zeitform und den Stil des Buches sowie den Szenenrahmen, die Figur der Erzählperspektive, die anwesenden Figuren und die Anweisungen des Zuges, aus dem der Teil stammt. Die Geschichte nach dem Teil ist dabei, damit der neue Text weiterhin in sie hinüberführt. Ereignisse, auf die sich die folgenden Teile stützen, sollten erhalten bleiben.

Der Teil behält seinen Platz, seine Zusammenfassung, seine Bilder und die Art, wie er erzeugt wurde. Text, Wörter und Tokens werden ersetzt, und der Teil gilt als bearbeitet. Das Übernehmen des Redrafts ist wie eine Bearbeitung mit **Save** im Editor. Deshalb werden [Zusammenfassungen](summaries.md), die den alten Text beschreiben, veraltet, und Marginalia nennt, wie viele es sind. Eine Zusammenfassung am Teil selbst gehört dazu.

Beschreibt eine Zusammenfassung den Teil (weil der Teil selbst eine hat oder ein neuerer Teil des Zweigs), zeigt das Fenster das Kontrollkästchen **Revalidate the summaries this redraft makes outdated**, anfangs nicht angehakt. Hake es an, wenn die Zusammenfassungen noch das Richtige sagen, also wenn der Redraft nur den Wortlaut geändert hat und nicht das Geschehen. **Apply** berechnet dann ihren Hash neu, und sie bleiben gültig. Lass es unangehakt, wenn sich die Ereignisse geändert haben, und erstelle die Zusammenfassungen neu.

Das Fenster blockiert die Geschichte nicht, siehe [Neben dem Fenster arbeiten](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Neben dem Redraft- und dem Fenster „Annotate with AI“ arbeiten

Auf dem Computer stehen die Fenster **Redraft** und **Annotate with AI** rechts neben der Geschichte und lassen sie nutzbar: Du kannst lesen, scrollen und die Geschichte bearbeiten, während das Modell schreibt. Ziehe ein Fenster an seinem Titel, um es zu verschieben, oder an seinem Rand, um seine Größe zu ändern. Der Pfeil (⌄) im Titel klappt ein Fenster in die [Leiste der Läufe](#the-panel-of-the-runs) unten links auf der Seite ein. Das Modell arbeitet dann weiter. **Open** (oder ein Klick auf den Eintrag) holt das Fenster zurück, und **Stop** beendet das Modell. Ist das Modell fertig, schließt **Close** das Fenster. Auf dem Telefon decken die Fenster die Geschichte weiterhin ab, klappen sich aber genauso ein.

![Das Redraft-Fenster neben der Geschichte, während das Modell schreibt, und der Lauf in der Leiste unten links](../../images/redraft-beside-story.png)

**Close und Stop sind zwei verschiedene Dinge.** Das **Stop** des Fensters selbst beendet das Modell, und das Fenster bleibt mit dem, was bis dahin angekommen ist. **Close**, während das Modell arbeitet, beendet es nicht: Das Fenster klappt in die Leiste ein, und das Modell arbeitet in seiner Warteschlange weiter. Seine Antwort steht im Fenster, wenn du es wieder öffnest. Ein Fenster, dessen Modell fertig ist, schließt du mit **Close**, oder du schließt es über **Close** in der Leiste. Dann ist es weg. Wird das Buch oder der Browser-Tab geschlossen, werden auch eingeklappte Fenster gestoppt. Dasselbe gilt für die anderen Fenster, die auf ein Modell warten: eine **Zusammenfassung** (sowie *Create summaries*, *Rewrite all summaries*), der [Reviewer](../extensions/reviewer.md), die Buchbeschreibung, die Generatoren für Lore und Gliederung, die Generatoren für Bilder und Manga sowie der Persona-Schreiber der Beta Readers. Auch die Fenster *Studio* und *Convert* des Hörbuchs blockieren die Seite nicht. Das Schließen hat die Konvertierung nie gestoppt.

Solange ein Fenster an einem Teil offen (oder eingeklappt) ist, zeigt der Teil neben seinem Menü an, was er gerade tut (*Redrafting · The book*). **Regenerate**, **Swipe**, **Redraft**, **Annotate with AI**, **Merge into previous** und **Delete** sind für diesen Teil dann ausgeschaltet, damit das Fenster seinen Teil nicht verliert. Den Teil zu bearbeiten bleibt möglich. Das Schließen des Buches schließt seine Fenster und stoppt ihre Modelle. Ein Teil, an dem ein [Übersetzer](../extensions/real-language-translator.md) arbeitet, zeigt auf dieselbe Weise *Translating the speech of the part...* an. Zwei Übersetzer arbeiten nie gleichzeitig an einem Teil: Der zweite meldet, dass der Teil in Verwendung ist.

- Wenn du einen Redraft des ganzen Teils mit **Apply** übernimmst und der Teil nach dem Öffnen des Fensters geändert wurde (du hast ihn inzwischen bearbeitet), fragt Marginalia vorher nach. **Differences** vergleicht dann den Teil, wie er jetzt ist, mit dem Redraft, sodass du siehst, was das Übernehmen ersetzen würde. Wähle **Apply anyway** oder **Cancel**. Ein Redraft, der sich [selbst übernimmt](annotations.md#annotating-every-new-part-automatically), tut dies nicht, wenn der Teil geändert wurde. Stattdessen wartet das Fenster auf dich.
- Wurde der Teil gelöscht, bevor du den Redraft mit **Apply** übernimmst oder die Anmerkungen hinzufügst, sagt das Fenster das, schreibt nichts und schließt sich.
<a id="the-panel-of-the-runs"></a>

### Das Panel der Läufe

Alles, was gerade mit einem Modell an deinen Büchern arbeitet, steht unten links auf jeder Seite: eine Generierung, eine Neufassung oder eine Anmerkung, eine Übersetzung durch den [Real Language](../extensions/real-language-translator.md)- oder [Conlang](../extensions/conlang-translator.md)-Übersetzer, eine Umschreibung der Zusammenfassungen, der Knowledge Tracker, der den Text liest, eine Umwandlung in ein [Hörbuch](../extensions/audiobook.md), ein Befehl der Befehlsleiste, der etwas dauert (*Draft instructions*, eine Zusammenfassung ...: sein Fenster steht seitlich und klappt sich wie die anderen ein), ein [Agent](../agents.md) bei der Arbeit. Ebenso aufgeführt sind die Fenster, in denen du auf eine Antwort wartest, während das Modell antwortet: **Impersonate**, der Hilfeassistent, eine schnelle Übersetzung, der Conlang-Designer, ein Chat von Side Query (und seine Benennung des Tabs), Beta Readers oder der Handlungsassistent. Das Panel listet deine Läufe aus **allen deinen geöffneten Seiten** auf (einem anderen Tab, dem Telefon), nicht nur diejenigen, die auf dieser Seite gestartet wurden. Läuft nichts, wird nichts angezeigt.

![Das Panel der Läufe geöffnet: die Neufassung darin eingeklappt, mit dem Ende des Modelltexts, Open und Stop](../../images/runs-panel.png)

- **Geschlossen** ist es eine kleine Leiste: der erste Lauf, wie viele weitere es gibt (*Translating the speech of the part... · 2 more*) und eine bewegte Leiste, während ein Modell arbeitet. Klicke darauf, um die Liste zu öffnen; sie öffnet sich von selbst, wenn du ein Fenster in sie einklappst.
- **Geöffnet** zeigt jeder Lauf, was er tut, das Buch, wie lange er läuft, worauf er wartet und das Ende dessen, was das Modell schreibt:
  - *Waiting for the model: 2 requests before this one*, wenn das Modell mit anderen Anfragen beschäftigt ist (siehe *Connections* in [Inference providers](../inference-providers.md));
  - *The model is thinking...* oder *The model is writing...*, darunter das Ende seines Denkens bzw. seiner Antwort;
  - *Waits for you*, wenn der Lauf auf eine Entscheidung von dir wartet (eine anzuwendende Neufassung, ein Vorschlag des Knowledge Tracker zur Prüfung, die Frage des Prompt Inspector vor einer Generierung, ein Befehl des Handlungsassistenten, der freigegeben werden muss).
- **Stop** beendet den Lauf, egal wo er gestartet wurde. **Open** oder ein Klick auf den Eintrag zeigt ein eingeklapptes Fenster wieder (nur auf der Seite, auf der es geöffnet wurde) oder den Lauf selbst; wartet der Lauf auf dich, heißt die Schaltfläche **Review**. Ein Lauf des Knowledge Tracker öffnet sein Buch auf dieser Seite (wenn hier kein anderes Buch offen ist; andernfalls wirst du gebeten, dieses zuerst zu schließen), wechselt zum Tab Knowledge und zeigt die Prüfung. **Stop** eines Agenten hält ihn an. **Close** schließt ein Fenster endgültig (und stoppt sein Modell).
- **Was beendet ist** bleibt zwei Stunden lang in der Liste, unter den laufenden Vorgängen, mit dem Ergebnis (*Completed*, *Failed*) und der Dauer: eine Übersetzung, Zusammenfassungen, was auf einen Teil folgte, die Sitzung eines Agenten, ein Import, eine Arbeit, die weiterlief, nachdem du ihre Seite verlassen hast. **Close** nimmt den Eintrag aus der Liste. Eine Generierung der Geschichte und die Läufe, die du gestoppt hast, werden nicht aufbewahrt.
- Auf einem Computer kannst du das Panel an seinem Griff (⋮ links) verschieben; es bleibt in diesem Browser dort, wo du es lässt. In der unteren Hälfte der Seite wächst es beim Öffnen nach oben, wie in seiner Ecke, und es bleibt immer innerhalb der Seite. Steht es dort, wo der runde Knopf des [Hilfeassistenten](../administration/help-assistant.md) ist (bei einem Buch liegen beide in der unteren linken Ecke), weicht der Knopf zur Seite aus und kehrt zurück, sobald der Platz frei ist.

Wenn ein Lauf, den du gestartet hast, endet, nachdem seine Seite geschlossen wurde, sendet eine andere offene Seite von dir die [Benachrichtigung](../account.md#notifications) (für Läufe, die eine haben, etwa die Umschreibung der Zusammenfassungen).

**Was abgelehnt wird, während etwas läuft.** Der nächste Teil wird nicht generiert, solange ein [Neufassungs](#redrafting-a-part)-Fenster des Buches offen ist (schließe es oder wende es zuerst an) oder solange ein Befehl der Befehlsleiste kurz davor ist, zu generieren. Ein Teil wird nicht mit dem davorliegenden zusammengeführt, solange ein Fenster oder eine Übersetzung an diesem arbeitet, und Suchen und Ersetzen lässt den Teil aus, den eine Generierung gerade schreibt (er wird als übersprungen gezählt). Ein [Agent](../agents.md) startet nicht an einem Buch, das in einem Fenster offen ist oder an dem anderes arbeitet. Wenn mehrere Läufe auf ein Modell warten, kommt deine nächste Generierung vor der Anmerkung, Übersetzung oder Audio des davorliegenden Teils.

<a id="redrafting-only-a-passage"></a>

### Nur eine Passage neu fassen

Um ein paar Sätze umzuschreiben und den Rest des Teils unangetastet zu lassen, markiere sie im Text des Teils und klicke mit der **rechten Maustaste** (auf einem Telefon ein langes Drücken): Das Menü enthält **Redraft selection**. (Das Menü ☰ des Teils wird dafür nicht angeboten, denn sein Öffnen kann in manchen Browsern die Markierung verlieren.) Das Fenster funktioniert wie oben, mit zwei Unterschieden: Es zeigt den **Selected text**, und die Neufassung besteht nur aus dem Text, der die Markierung ersetzt. Das Modell erhält den ganzen Teil mit der markierten Stelle sowie die Geschichte und das Lore darum herum, sodass sich die neue Passage an die Sätze davor und danach fügt. **Differences** vergleicht die Markierung mit der Neufassung; **Apply** setzt den neu gefassten Text an die Stelle der Markierung und behält den Rest des Teils bei, einschließlich des Leerzeichens oder Zeilenumbruchs an den Rändern der Markierung. Bei offenen [Anmerkungen](annotations.md) bietet das Kontrollkästchen nur jene an, deren Passage sich mit der Markierung überschneidet. Wurde der Teil nach deiner Markierung geändert (in einem anderen Fenster), verweigert **Apply** die Anwendung, und du markierst den Text erneut. Während der Teil bearbeitet wird, gilt die Markierung im Editor, und die Bearbeitung wird vorher gespeichert.

<a id="what-follows-a-generated-part"></a>

### Was auf einen generierten Teil folgt

Wenn ein Teil, den du generiert hast (von der Seite oder aus der Befehlsleiste), fertig ist, kann das Buch von selbst weitere Schritte ausführen, in dieser Reihenfolge:

1. **Annotate**: die [automatische Anmerkung](annotations.md#annotating-every-new-part-automatically) prüft den Teil.
2. **Mark phrases**: die [Liste überwachter Phrasen](annotations.md#watching-for-phrases) versieht die gefundenen Phrasen mit Anmerkungen (ohne Modell).
3. **Redraft**: der Teil wird neu geschrieben, um auf die Anmerkungen einzugehen.
4. **Translate**: die Übersetzer [Real Language](../extensions/real-language-translator.md) und [Conlang](../extensions/conlang-translator.md) übersetzen seine Reden.
5. **Knowledge**: der [Knowledge Tracker](../extensions/knowledge-tracker.md) liest ihn.
6. **Audio**: das [Hörbuch](../extensions/audiobook.md) macht daraus Audio.

Nur die Schritte, die das Buch eingeschaltet hat, laufen. Sie laufen auf dem Server, sodass die Seite geschlossen werden kann, und sie bilden einen Eintrag im [Panel der Läufe](#the-panel-of-the-runs): *After part 14: Annotate ✓ · Redraft … · Translate*, mit dem Text des Modells und einem **Stop**. Was den Text ändert, kommt zuerst, damit der Tracker und das Audio den endgültigen Text erhalten. Ein Schritt, der auf dich wartet (Anmerkungen oder eine Neufassung zur Auswahl), öffnet sein Fenster, und der Eintrag hat **Review**, um es in einer anderen Seite zu öffnen.

- **Stop**, das Schließen eines Fensters, das auf dich wartet, oder ein fehlgeschlagener Schritt beendet die Kette an dieser Stelle: Was bereits erledigt wurde, bleibt, der Rest wird nicht ausgeführt, und eine Meldung nennt, was offen ist und von Hand erledigt werden muss (jeder Schritt hat seine eigene Aktion am Teil).
- Während die **Neufassung** läuft, kann der nächste Teil nicht generiert werden (du wirst mitgeteilt, durch welchen Lauf); während die anderen Schritte laufen, geht es.
- Der Teil kann erst gelöscht werden, wenn sein Text endgültig ist; generiert man ihn neu, endet, was auf seine frühere Fassung folgte.
- Ein [Agent](../agents.md) führt diese Schritte über seine eigenen Befehle aus: Auf einen Teil, den ein Agent generiert hat, folgt nichts.
<a id="merging-a-part-into-the-previous-one"></a>

## Einen Teil in den vorherigen zusammenführen

Ein Modell, das mitten im Satz aufgehört hat (weil das Antwortlimit erreicht war oder die Verbindung abbrach), lässt sich nicht zuverlässig dazu bringen, denselben Text weiterzuschreiben. Schreiben Sie stattdessen den nächsten Teil (*setze dort fort, wo der letzte Teil aufhört*) und verbinden Sie beide mit **Merge into previous part** im Menü des zweiten. Es öffnet sich eine Prüfung:

- **Join the texts with**: eine Leerzeile, ein Zeilenumbruch, ein Leerzeichen oder nichts. Vorausgewählt wird die Verbindung anhand des Endes des vorherigen Textes: ein Absatz nach einem Text, der einen Satz beendet hat, und ein Leerzeichen nach einem, der abgeschnitten wurde.
- Die **scene setting**, **POV character**, **present characters** und **instructions** beider Durchgänge, zusammengeführt in Feldern, die Sie bearbeiten können. Was die beiden Durchgänge am Anfang gemeinsam haben, wird nur einmal übernommen („Write the duel. Keep it short“ und „Write the duel. Add rain“ ergeben als Anfang „Write the duel. Keep it short“, und „Add rain“ steht in der nächsten Zeile).
- **Data**: welche Werte die beiden Teile jeweils haben, Wert für Wert (zum Beispiel die [Variablen](../templates/macros.md) der Geschichte und die Daten von Erweiterungen), mit der Angabe, ob der Wert gleich, verschieden oder nur in einem der Teile vorhanden ist. Der zusammengeführte Teil behält den Wert des **späteren Teils** (dort steht die Geschichte jetzt), es sei denn, Sie wählen für ihn den Wert des vorherigen Teils oder lassen den Wert weg. Gleiche Werte werden ausgeblendet, sofern Sie sie nicht anzeigen lassen.

![Die Prüfung, bevor ein Teil in den vorherigen zusammengeführt wird](../../images/part-merge-dialog.png)

Der vorherige Teil behält seinen Platz, seine Zusammenfassung und die Angaben dazu, wie er erzeugt wurde (Prompt, Modell, Zeiten). Er erhält den zusammengeführten Text, die zusammengeführte Begründung und die Bilder; seine Wörter und Tokens werden neu gezählt. Der zusammengeführte Teil wird gelöscht (er liegt dann im [Papierkorb](../trash.md)), und was auf ihn folgte, schließt nun an den vorherigen Teil an. Zusammenfassungen, die beide Teile abdecken, werden veraltet, wie nach einer Bearbeitung. Der [Verlauf](version-history.md#backups-copies-and-the-trash) des zusammengeführten Teils wird entfernt, und der verbleibende Teil hat den zusammengeführten Text als Version **Merged**. Ein Teil kann nicht zusammengeführt werden, wenn er der erste Teil des Buchs ist oder wenn der Teil davor andere Fortsetzungen hat (Swipes des Teils). Löschen Sie diese oder machen Sie sie zuerst zu einem Zweig.

<a id="images"></a>

## Bilder

Ein Teil kann Bilder haben: eine Karte, ein Porträt einer Figur, eine Illustration. Wählen Sie im Menü des Teils **Images**, um die Liste der Bilder des Teils zu öffnen.

![Der Dialog Images eines Teils](../../images/images-dialog.png)

- **Add image** (oder eine Datei darauf ziehen) lädt ein PNG-, JPEG-, GIF- oder WebP-Bild von höchstens 10 MB hoch. WebP-Bilder werden beim Hochladen in PNG umgewandelt, weil Word- und PDF-Dateien kein WebP enthalten können.
- **Caption** wird unter dem Bild angezeigt. Das Feld kann leer bleiben.
- **Position in the part** ist ein 3x3-Raster. Die Spalte ist die Seite des Textes (links, Mitte, rechts), die Zeile ist die Stelle darin: oben steht das Bild vor dem Text, in der Mitte mitten im Text, unten nach dem Text. Klicken Sie auf ein Feld, um es auszuwählen.
- **Text flows around the image** lässt den Text um das Bild herumlaufen, statt darüber und darunter. Links oder rechts läuft der Text neben dem Bild her. In der mittleren Spalte steht der Text auf beiden Seiten: Einige Blöcke des Textes (etwa 400 Zeichen, mindestens zwei Blöcke) an dieser Zeile werden zu zwei Spalten, der erste Teil links vom Bild und der Rest rechts. So wird der Text die linke Spalte hinabgelesen und in der rechten weitergeführt. Zwei Bilder an derselben Stelle stehen zwischen den Spalten übereinander. Wenn der Text keinen Platz für zwei Spalten hat (nur ein Absatz, oder die Blöcke sind schon vom Bereich darüber belegt), steht das Bild als eigener, zentrierter Block.
- **Width** ist die Breite des Bildes in Prozent der Breite des Textes. **Automatic** behält die Größe des Bildes bei (nie breiter als der Text; ein Bild mit Text auf beiden Seiten ist 30 %). Ein Bild, neben dem der Text herläuft, ist höchstens 60 %, und eines mit Text auf beiden Seiten höchstens 40 %, damit dem Text etwas Platz bleibt. Die Höhe richtet sich nach den Proportionen des Bildes.
- Die Papierkorb-Schaltfläche entfernt das Bild aus dem Teil.

![Die Position eines Bildes: in der Mitte, mit dem Text, der darum herumläuft](../../images/images-position.png)

**Preview as in the viewer** (eine Schaltfläche des Dialogs, und derselbe Eintrag im Menü des Teils, wenn der Teil Bilder hat) zeigt den Teil so, wie ihn der [Viewer](publishing.md) zeigt, mit dem Textstil des Buchs: die Bilder dort, wo Sie sie platziert haben, und einem Umschalter für eine breite oder eine schmale (Telefon-)Ansicht. Verwendet wird der Text im Editor, so wie er jetzt ist, gespeichert oder nicht.

![Die Vorschau: der Text in zwei Spalten mit dem Bild dazwischen](../../images/images-preview.png)

Der Geschichten-Editor selbst verwendet die Position nicht: Er zeigt die Bilder unter dem Text des Teils als Liste. Die Position wird vom Viewer und von den [Exporten](export.md#images) verwendet.

Wohin im Text ein Bild gehört, hängt nicht von Zahlen ab, die mit ihm gespeichert sind: Der Text wird zwischen seinen Blöcken geschnitten (Absätze, Listen, Zitate und Codeblöcke werden nie geteilt), sodass die Bilder dort bleiben, wo sie hingehören, wenn Sie den Text bearbeiten. Die Mitte ist die Stelle zwischen Blöcken, die der Mitte des Textes am nächsten liegt. Ein Bild, um das der Text unten herumläuft, wird vor den letzten Block gesetzt, sodass wenigstens dieser Block neben ihm verläuft. Bilder an derselben Stelle bleiben in der Reihenfolge der Liste. Solche, die an derselben Stelle nebeneinander stehen und bei denen der Text nicht herumläuft, werden in eine Reihe gesetzt, wenn sie hineinpassen. PDF und Word machen das bei Bildern mit einer Breite.

Bilder sind **nicht Teil des Textes**. Das Modell sieht sie nie, sie stehen nicht im Prompt, zählen nicht als Tokens und ändern die [Zusammenfassungen](summaries.md) nicht. Sie sind außerdem kein Markdown: Ein im Text geschriebener Link `![](...)` ist kein Bild des Teils. Verwenden Sie stattdessen **Images**.

Gut zu wissen:

- **Regenerate** behält die Bilder des Teils, auch wenn die Erzeugung fehlschlägt. **Swipe** schreibt einen neuen Teil (eine neue Version neben der alten), der ohne Bilder beginnt. **Branch Story** kopiert die Bilder in den neuen Teil.
- Bilder gehören zu Ihrem Konto. Im [Viewer](publishing.md) sehen auch Leser eines veröffentlichten Buchs sie, es sei denn, Sie deaktivieren **Show the images to readers** im Reiter *About* des Buchs.
- Ein [Backup](backups.md) merkt sich, welche Bilder ein Teil hat, aber nicht die Bilddateien. Wird ein Backup in ein anderes Konto oder eine andere Installation zurückgespielt, erhalten die Teile keine Bilder.
- Das Entfernen eines Bildes aus einem Teil nimmt es nur vom Teil weg. Die Datei bleibt erhalten und ist weiterhin unter [Ressourcen](../resources.md) aufgeführt, wo Sie sie herunterladen, ersetzen oder löschen können. Löschen Sie sie dort, wird sie aus jedem Teil entfernt, der sie verwendet. Die Datei selbst bleibt im Datenordner.

<a id="summaries"></a>

## Zusammenfassungen

Die Schaltfläche **Summaries** in der unteren Leiste (links vom Einstellungsmenü ⚙) öffnet die Übersicht der Zusammenfassungen des Zweigs. Dort können sie bearbeitet, gelöscht und zu Meta-Zusammenfassungen zusammengeführt werden. Der Tooltip der Schaltfläche zeigt, wie viele Tokens die verwendeten Zusammenfassungen im Prompt belegen. Siehe [Zusammenfassungen](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Wie viel vom Kontext die Geschichte belegt

Das Einstellungsmenü (⚙) enthält **Branch Tokens**. Es zeigt, bevor Sie etwas erzeugen, wie viel vom Prompt die Geschichte des aktiven Zweigs belegt: einen Balken für die Geschichte **ohne Zusammenfassungen** (alle Teile) und **mit Zusammenfassungen** (die Zusammenfassungen und die Teile nach dem neuesten), sowie eine Tabelle mit den Teilen, den Tokens und dem Anteil am **Prompt-Budget** (dem Kontext des Modells des Buchs abzüglich des Platzes für die Antwort). Nur Zusammenfassungen, die die Geschichte noch beschreiben, zählen. Die anderen, die nicht mehr passen, werden namentlich genannt und beim Erzeugen eines Teils verworfen. Das Lore, die Systemvorlage und die Anweisungen des Durchgangs belegen Platz aus demselben Budget. Ohne ein Modell mit Kontextgröße hat die Tabelle keine Anteile.

![Branch Tokens](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Welche Lore-Einträge verwendet wurden

Das Fenster **Lore Usage** im Einstellungsmenü (⚙, **Analysis**) zeigt, welche Einträge des Lorebooks des Buches die Generierungen verwendet haben und welche nie. So lässt sich ein Lorebook anhand dessen kürzen, was es tatsächlich bewirkt, und nicht anhand von Vermutungen. Jede Generierung speichert, welche Einträge in ihren Prompt eingeflossen sind (samt Tokens jedes Eintrags). Das Fenster summiert das über die Teile:

- **Parts** legt fest, was gezählt wird: der **Active branch** oder **All branches**, also jeder Zweig und jeder Swipe des Buches.
- **From part** und **To part** begrenzen den Bereich. Die Teile sind wie in der Gliederung nummeriert (der erste Teil eines Zweigs ist 1; bei allen Zweigen gibt die Nummer an, wie tief der Teil im Baum liegt). Leer lassen, um am Anfang bzw. am Ende zu beginnen.
- **Show** zeigt die Einträge, die in den gezählten Teilen **Never used**, **Sometimes used** oder **Always used** waren.

![Das Fenster Lore Usage](../../images/lore-usage.png)

Die Tabelle hat eine Zeile für jeden Eintrag der Lorebooks des Buches, der am häufigsten verwendete zuerst. Sie zeigt das Lorebook, ob der Eintrag nie, manchmal oder immer verwendet wurde, in wie vielen Teilen (von den Teilen mit Protokoll, mit Prozentangabe), die Tokens, die er in all diesen Teilen und pro Verwendung gekostet hat, sowie den ersten und letzten Teil, der ihn verwendet hat. Auf eine Kopfzeile klicken, um zu sortieren. Ein Eintrag, der früher verwendet wurde, aber jetzt nicht mehr in den Lorebooks des Buches steht (gelöscht, verschoben, oder sein Lorebook ist deaktiviert oder gehört nicht mehr zum Buch), bleibt in der Liste und ist als *not in the lorebook now* markiert. Ein gelöschter Eintrag hat keinen Namen.

Gezählt werden nur Teile mit Protokoll. Ein von Hand getippter Teil, einer, der geschrieben wurde, bevor Marginalia dieses Protokoll führte, oder einer, der entstand, als das Buch noch kein Lorebook hatte, hat keins. Das Fenster zeigt an, wie viele das sind.

Einträge mit *Always used* könnten konstante Einträge sein. *Never used*-Einträge sind Kandidaten zum Entfernen oder für einen besseren Filter. [**Preview activation**](../lorebooks/entries.md#preview-activation) im Lorebook-Editor zeigt, warum ein Eintrag gerade nicht auslöst.

<a id="text-styles"></a>

## Textstile

Das Einstellungsmenü (⚙) in der unteren Leiste enthält **Appearance → Change Styles For Text**, ein Menü mit einem Eintrag pro Stil, nach Namen sortiert. Wie man einen Stil erstellt, steht im [Styling-Leitfaden](styling-guide.md).

Ein Stil bestimmt, wie der Text der Teile aussieht: Schrift, Größe, Farben, Ausrichtung, Überschriften … Die Anwendung bringt zwei Stile mit, die sich nicht ändern lassen: **Normal** (das Aussehen der Anwendung) und **Book** (ein buchähnlicher Lesestil, den der [Viewer](publishing.md) verwendet). Eigene Stile lassen sich unter [Settings → Styles](../account.md#styles) anlegen.

- Der erste Eintrag, **Default (...)**, folgt deinem *Standardstil* (dem, der unter *Settings → Styles* markiert ist; Normal, wenn du keinen gewählt hast). Ein neues Buch hat keinen eigenen Stil und zeigt das auch an.
- Wählst du einen anderen Eintrag, wird der Stil **im Buch** gespeichert. Er überschreibt den Standard nur für dieses Buch. Der angehakte Eintrag ist der, den das Buch jetzt hat. **Default** nimmt die Wahl wieder zurück.
- Ein gelöschter Stil fällt in den Büchern, die ihn verwendet haben, auf deinen Standard zurück.

![Appearance, Change Styles For Text: die Stile zur Auswahl](../../images/story-style-menu.png)

Im Stil **Book** sind Absätze im Blocksatz, und lange Wörter werden automatisch getrennt. Dafür wird das Wörterbuch der **Language** des Buches verwendet (eingestellt auf dem [Reiter About](managing-books.md#the-about-tab)). Die Silbentrennung übernimmt der Browser. Für einige seltene Sprachen gibt es kein Wörterbuch; dann wird der Text schlicht nicht getrennt.

Bücher, bei denen der alte Schalter *Change Styles For Text* eingeschaltet war, zeigen weiterhin den Stil Book, bis du einen anderen Eintrag wählst.

<a id="highlighting-generated-text"></a>

## Generierten Text hervorheben

Das Einstellungsmenü (⚙) hat außerdem die ankreuzbare Option **Highlight Generated Text**. Ist sie aktiv, werden die Wörter, die ein Modell geschrieben hat, in jedem Teil hervorgehoben. So fällt auf, was du ergänzt oder umgeschrieben hast.

Marginalia verfolgt den Text über die [Versionen des Teils](version-history.md): Jede Version liegt über der davor, und die Wörter, die sie gemeinsam haben, behalten ihren Verfasser. Ein Satz, den du in einen generierten Text getippt hast, bleibt also deiner, egal wie oft das Modell den Rest umschreibt. Ein Teil, den du nie bearbeitet hast, wird ganz hervorgehoben. Ein Teil, den du selbst geschrieben hast, bekommt keine Hervorhebung. Was ein Agent geschrieben hat, zählt als Text des Modells. Die Option wird nicht gespeichert: Beim erneuten Öffnen der Seite ist sie wieder aus.

Ein Teil, dessen [Verlauf ausgeschaltet ist](version-history.md), hat keine Versionen, denen man folgen könnte. Er wird mit dem Text verglichen, den das Modell zuerst erzeugt hat. Die Hervorhebung funktioniert auch auf dem [Handy](../phone.md).

Dieselbe Zählung liefert auch das Verhältnis. Der Reiter **Info** des Buches enthält **Authorship of the branch** mit einem Button **Calculate**. Er gibt an, wie viele Prozent der Wörter des aktiven Zweigs du geschrieben hast und wie viele ein Modell erzeugt hat. [Der Export](export.md) kann die Datei mit einer entsprechenden Angabe abschließen.

![Hervorgehobener generierter Text und das Einstellungsmenü](../../images/highlight-generated.png)

<a id="commands"></a>

## Befehle

Neben dem Menü unter der Geschichte gibt es eine **Befehlszeile**. Tippe `/`, um die Befehle zu sehen, wähle einen aus und drücke **Enter** (oder den Abspielknopf), um ihn auszuführen. Was ein Befehl zurückgibt, wird in einem kleinen Fenster über der Zeile angezeigt. Unter [Befehle](../commands/index.md) steht, wie man Argumente schreibt, was die Hilfe zeigt und wie man einen Befehl stoppt, der länger dauert. Die Befehlszeile gibt es bisher nur im Tab Story des Desktop-Layouts.
<a id="search-and-replace"></a>

## Suchen und ersetzen

Das Einstellungsmenü (⚙) enthält außerdem **Clear history of the book** und **Empty trash of the history**, siehe [Versionsverlauf](version-history.md#deleting-versions).

Das Einstellungsmenü (⚙) enthält außerdem **Search and replace**. Damit lässt sich ein Wort, ein Name oder eine Wendung in der gesamten Geschichte auf einmal ändern. Das Fenster hat die Felder **Search for** und **Replace with** sowie diese Optionen:

![Das Einstellungsmenü mit Search and replace](../../images/story-settings-menu.png)

![Das Fenster Search and replace mit eingegebener Suche](../../images/search-replace-dialog.png)

| Option | Standard | Bedeutung |
|---|---|---|
| **Search by regex** | aus | Der Suchtext ist ein [regulärer Ausdruck](../regular-expressions.md), und die Ersetzung kann dessen Gruppen verwenden (`$1`). Andernfalls wird der Text so gesucht, wie er eingegeben wurde. |
| **Case sensitive** | an | Ein Text wird nur in der Schreibweise gefunden, in der er eingegeben wurde (*John* findet *john* nicht). Aus: Die Groß- und Kleinschreibung spielt keine Rolle. Bei eingeschaltetem **Search by regex** gesperrt. |
| **Search in summaries** | an | Die [Zusammenfassungen](summaries.md) werden ebenfalls durchsucht und geändert. |
| **Search in all branches** | aus | Alle [Verzweigungen](branches-and-story-tree.md) werden durchsucht, sonst nur die aktive. |
| **Revalidate all summaries** | an | Nach dem Ersetzen ist jede [Zusammenfassung](summaries.md) des Buchs wieder gültig, siehe unten. |

Bei einem regulären Ausdruck wird die Groß- und Kleinschreibung immer so genommen, wie sie eingegeben wurde. Beginne ihn mit `(?i)`, um sie zu ignorieren. Drücke **Search** (die Schaltfläche rechts neben den Feldern; auf dem Telefon darunter). Der Fortschrittsbalken zeigt, wie weit die Suche ist, und **Stop** daneben unterbricht sie. Eine Suche verändert nichts.

Die Tabelle listet jeden Teil der Geschichte und jede Zusammenfassung mit einem Treffer auf, zusammen mit Typ und ID. Öffne eine Zeile, um die Treffer darin zu sehen: den Text um den Treffer, wie er jetzt ist und wie er nach der Ersetzung aussehen wird. Ein Treffer oder ein Ersatztext, der zu lang zum Anzeigen ist, wird als *[searched text]* bzw. *[replace text]* dargestellt.

![Die Treffer einer Suche, der erste Teil geöffnet](../../images/search-replace-found.png)

Das Drücken von **Enter** im Feld **Search for** oder **Replace with** hat dieselbe Wirkung wie **Search**.

Wenn die Suche abgeschlossen ist, sind die Felder und Optionen gesperrt und die Schaltfläche **Replace** ist aktiviert. **Search again** vergisst die Treffer und entsperrt das Fenster, sodass du die Suche ändern kannst. **Replace** ändert die Geschichte: Das Fenster ist dann vollständig eingefroren (auch **Stop** funktioniert nicht), bis die Ersetzung fertig ist. Danach ist es wieder leer. Ersetzte Teile werden als bearbeitet markiert, als hättest du sie selbst bearbeitet.

Eine Ersetzung in dem Text, den eine [Zusammenfassung](summaries.md#when-summaries-become-outdated) beschreibt, würde die Zusammenfassung veralten lassen. Bei eingeschaltetem **Revalidate all summaries** berechnet Marginalia den Prüfwert jeder Zusammenfassung des Buchs neu. Das gilt für alle Verzweigungen, auch für jene Teile, die die Ersetzung nicht berührt hat, und für solche, die schon vorher veraltet waren. Danach sind also alle gültig. Dadurch wird eine veraltete Zusammenfassung auch gültig, ohne dass ihr Text geprüft wird. Verwende diese Option deshalb nur, wenn du weißt, dass die Zusammenfassungen weiterhin das Richtige sagen.

Bei ausgeschalteter Option werden nur die Zusammenfassungen aktualisiert, die von der Suche erfasst werden. Die übrigen (Zusammenfassungen von Verzweigungen, die nicht durchsucht werden und sich frühere Teile mit der aktiven Verzweigung teilen, oder alle, wenn Zusammenfassungen nicht durchsucht werden) werden veraltet. Gibt es solche, zeigt Marginalia an, wie viele es sind, und fragt, ob es weitergehen soll. Eine Zusammenfassung, die schon vorher veraltet war, bleibt es.

![Die Warnung über Zusammenfassungen, die veralten würden](../../images/search-replace-warning.png)

Im [Telefonlayout](../phone.md) stehen die Felder und Schaltflächen im selben Fenster untereinander, und die Tabelle hat eine Spalte, die Typ und ID zeigt. Darunter stehen dann der Treffer und die Ersetzung.

Die [Übersicht der Zusammenfassungen](summaries.md#search-and-replace-in-the-summaries) hat dasselbe Fenster, jedoch nur für die Zusammenfassungen.

Dieselbe Suche und Ersetzung lässt sich über die [Kommandozeile](../commands/index.md#commands-that-come-with-marginalia) mit `/search-replace-parts` ausführen. Sie fragt vor dem Ersetzen nach.

<a id="importing-into-the-book"></a>

## In das Buch importieren

Das Einstellungsmenü (⚙) hat **Import** (unter **Import & export**). Es ist das Fenster des Reiters Books, das aus einer Datei ein neues Buch macht (siehe [Bücher verwalten](managing-books.md#creating-a-book)). Hier aber werden die Teile für das Buch, in dem du dich gerade befindest, **zur Geschichte hinzugefügt**. Die **Source** ist eine der folgenden Quellen: das Zip-Archiv mit `.md`-Dateien, ein Dokument (Word, LibreOffice, RTF, Markdown, reiner Text, EPUB), das Zip eines Obsidian-Vaults oder das Zip eines Scrivener-Projekts. Jede hat dieselben Texte und Optionen wie dort (die Wörter pro Teil und die Überschriftenebene der Kapitel). Eine Zeile zeigt, was der Import erzeugen wird. Die Chats von SillyTavern und LM Studio werden nicht angeboten: Sie sind ein Baum aus Verzweigungen, der in der Mitte einer Geschichte keinen Platz hat.

**Put the parts** legt fest, wohin die Teile kommen:

- **At the end of the story**: nach dem letzten Teil der aktiven Verzweigung (bei einem leeren Buch ist es der erste Teil).
- **Before a part** oder **After a part**: Wähle den Teil aus der Liste (die Nummern sind die Stellen in der aktiven Verzweigung, mit dem Anfang des Textes). Die Teile werden zwischen zwei Teile eingefügt. Alles, was auf die Stelle folgte, also alle seine Verzweigungen, folgt nun den neuen Teilen, und kein Teil wird verändert oder neu erzeugt. Das Ende der Geschichte bleibt an seiner Stelle, die Geschichte ist um so viele Teile länger. Die Zusammenfassungen, die die Stelle abdecken, werden veraltet, weil sie die Geschichte ohne die neuen Teile beschreiben. Das Fenster [Zusammenfassungen](summaries.md) zeigt sie an und erstellt sie neu.

![Das Importfenster eines Buchs](../images/book-import-into.png)

Die Teile eines Archivs sind die Dateien in der Reihenfolge ihrer Namen, wobei Zahlen in den Namen als Zahlen verglichen werden (`2.md` vor `10.md`), auch in Ordnern des Archivs. Ordner, versteckte Dateien (`.name`), der Ordner `__MACOSX` und Dateien, die keine `.md` sind, werden übersprungen. Die Datei ist der Text des Teils so, wie er ist. Sie wird nicht an das Modell geschickt und nicht verändert. Alle Teile gelten als von dir geschrieben.

Der Import zeigt einen Fortschrittsbalken mit der Anzahl der fertigen Teile. **Stop** beendet ihn nach dem Teil, der gerade gespeichert wird. Die bereits hinzugefügten Teile bleiben in der Geschichte (der Import wird nicht rückgängig gemacht), und eine Meldung sagt, wie viele importiert wurden.

<a id="after-an-import"></a>

### Nach einem Import

Wenn die Teile im Buch sind, bietet ein Fenster **Import finished** zwei Dinge an, die eine lange Geschichte leichter schreiben lassen. (Ein Buch, das gerade als neues importiert wurde, wird zuerst geöffnet, und das Fenster liegt davor.) Beides ist auch im Einstellungsmenü (⚙, **Analysis**) zu finden, sodass du es später oder erneut ausführen kannst:

- **Create summaries...** schreibt Zusammenfassungen dessen, was importiert wurde, siehe [Zusammenfassungen erstellen](summaries.md#create-summaries). Mach das zuerst: Ein Agent sperrt das Buch, während er arbeitet.
- **Take lore notes with an agent...** startet einen [Agenten](../agents.md), der die Geschichte liest und die Figuren, Orte, Dinge und Begriffe, die er findet, als Einträge im Lorebook des Buchs schreibt, siehe [Lore-Notizen](../agents.md#lore-notes-of-an-imported-book).

**Not now** schließt das Fenster, ohne eines von beiden zu tun.

![Das Fenster nach einem Import](../../images/import-finished.png)

Der gleiche Import in ein Lorebook wird unter [Import & export](../lorebooks/import-export.md#importing-an-archive-of-text-files) beschrieben.

<a id="exporting-the-story"></a>

## Die Geschichte exportieren

Das Einstellungsmenü (⚙) enthält außerdem **Export story**. Damit wird die aktive Verzweigung als Text-, Markdown-, HTML-, Word-, PDF- oder EPUB-Datei gespeichert. Siehe [Ein Buch exportieren](export.md).

<a id="what-the-model-gets"></a>

## Was das Modell erhält

Für jeden neuen Teil sendet Marginalia:

1. Die **Master-Vorlage** als Systemnachricht: Rolle, [Lorebook](../lorebooks/index.md)-Einträge, Erzählperspektive, Zeitform, Stil und [Zusammenfassungen](summaries.md). Der Jailbreak-Prompt des [Inferenzanbieters](../inference-providers.md#general-settings) steht, falls aktiviert, ganz vorn.
2. Die früheren Teile der aktiven Verzweigung als die eigenen früheren Antworten des Modells, so viele, wie in das [Kontextlimit](../protocols.md#limits) passen, beginnend mit dem neuesten. Ältere Teile, die nicht hineinpassen, werden weggelassen (sofern nicht eine [Zusammenfassung](summaries.md) sie abdeckt).
3. Den **User-Prompt** mit den Anweisungen für den neuen Teil.

*Show Prompt* bei jedem Teil zeigt genau, was gesendet wurde. Siehe [Buch-Prompts](prompts.md) für die Vorlagen.
