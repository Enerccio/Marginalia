---
label: Story-Editor
order: 90
---

# Story-Editor

Der Reiter **Geschichte** eines Buchs ist der Ort, an dem du schreibst. Er zeigt die Abschnitte des [aktiven Zweigs](branches-and-story-tree.md)
vom Anfang bis zum Ende.

![Der Story-Editor](../../images/hero-story-editor.png)

Diese Seite beschreibt das Layout für den Desktop. Auf einem Telefon ist die Geschichte eine einzelne Spalte, und die
Gliederung liegt in einer Schublade, siehe [Marginalia auf dem Telefon verwenden](../phone.md#the-story).

| Bereich | |
|---|---|
| **Gliederung** (links) | Ein Eintrag pro Abschnitt, `#1`, `#2`... mit der Datenbank-ID des Abschnitts. Klicke auf einen Eintrag, um zum Abschnitt zu scrollen (die davor liegenden Abschnitte werden zuerst geladen, wenn er noch nicht geladen ist). Die Kopfzeile zeigt, wie viele Abschnitte der Zweig hat. Ein Abschnitt, der eine [Zusammenfassung](summaries.md) hat, zeigt nach seinem Eintrag ein kleines Dokumentsymbol. Die Erweiterung [Kapitelmarkierung](../extensions/chapter-marker.md) zeigt hier die Kapiteltitel an. |
| **Lorebook** (links, neben der Gliederung) | Ein Blick auf das Wissen des Buchs mit einem Suchfeld und einer schnellen Bearbeitung des Texts eines Eintrags, siehe [Der Reiter Lorebook](#the-lorebook-tab). |
| **Anmerkungen** (links, neben der Gliederung) | Die [Notizen](annotations.md) zu Textstellen der Geschichte, für das ganze Buch. |
| **Planer** (links, neben der Gliederung) | Die Szenen, die du planst zu schreiben, eine Liste, die du von Hand füllst, siehe [Der Reiter Planer](#the-planner-tab). |
| **Geschichte** (Mitte) | Der Text jedes Abschnitts mit seinem Menü (☰) und seinen Metadaten rechts, darunter die untere Leiste. |
| **Untere Leiste** | Die Schaltfläche **Zusammenfassungen** (Listensymbol) und das Einstellungsmenü (⚙) links, die Befehlszeile und die Schaltfläche **+** rechts. |

So sieht ein neues Buch aus. All diese Bereiche sind Reiter, die du beliebig anordnen kannst, die Geschichte eingeschlossen, siehe
[Reiter anordnen](#arranging-the-tabs). Die Trennlinie zwischen Seitenleiste und Geschichte lässt sich ziehen, und Marginalia
merkt sich, wo du sie zuletzt hingezogen hast (zusammen mit dem Buch gespeichert), und stellt sie beim erneuten Öffnen des Buchs wieder her.

Die Geschichte öffnet sich immer unten, beim neuesten Abschnitt. Ein langes Buch wird nicht auf einmal gezeichnet: Es werden nur die letzten Abschnitte geladen, und die früheren werden oben ergänzt, während du nach oben scrollst, ohne den Abschnitt zu verschieben, den du gerade liest. Die Abschnitte am anderen Ende werden wieder entfernt, sodass nicht mehr als die Einstellung **Auf einmal geladene Abschnitte in der Geschichte** deines [Kontos](../account.md) geladen bleibt (leer lädt alle). Wenn du schneller scrollst, als die Abschnitte eintreffen, zeigt eine Markierung *Frühere Abschnitte werden geladen* (bzw. *spätere Abschnitte*) am Rand, dass es mehr gibt. Ein Klick auf einen Abschnitt in der Gliederung oder das Finden eines Abschnitts mit der Suche lädt die Abschnitte rund um ihn, und das Beginnen eines neuen Abschnitts bringt dich zurück zum Ende der Geschichte.

<a id="searching-the-story"></a>

### Die Geschichte durchsuchen

Über der Gliederung gibt es ein Suchfeld für den Text des **Zweigs, auf dem du dich befindest** (die Suche im Geschichtenbaum
durchsucht alle Zweige). Da ein langes Buch nicht auf einmal geladen wird, kann die Suche des Browsers (Strg+F) keinen Text
in den nicht geladenen Abschnitten finden; diese Suche schon.

- Tippe ein, wonach du suchst; die Suche startet, sobald du aufhörst zu tippen. Mehrere Wörter müssen alle im selben
  Abschnitt vorkommen, eine Wortgruppe setzt du in doppelte Anführungszeichen (`"alter Schlüssel"`), und `*` steht für beliebige Zeichen (`Leucht*turm`). Groß- und Kleinschreibung spielen keine Rolle.
- Jede Stelle, an der ein Wort steht, ist ein Treffer, und sie sind über den ganzen Zweig durchnummeriert. Unter dem Feld siehst du
  *Treffer 3 von 27, in Abschnitt #5* und einen Balken, der zeigt, wie weit du bist. Die Geschichte scrollt zum Treffer und lädt die Abschnitte rund um ihn, wenn sie nicht geladen sind.
- Die Schaltflächen **nach oben** und **nach unten** springen zum vorigen bzw. nächsten Treffer (nach dem letzten geht es wieder zum ersten).
  **Enter** im Feld tut dasselbe wie die Schaltfläche nach unten, **Shift+Enter** wie die Schaltfläche nach oben.
- Alle Treffer in den geladenen Abschnitten sind im Text hervorgehoben, der Treffer, bei dem du gerade bist, in einer kräftigeren Farbe. Leere das Feld, um die Hervorhebung zu entfernen.

Auf dem Telefon befindet sich die Suche in der Gliederungsschublade; die Schublade schließt sich, wenn sie einen Treffer zeigt. Öffne sie erneut, um zum nächsten zu gelangen.

<a id="the-lorebook-tab"></a>

### Der Reiter Lorebook

Der Reiter **Lorebook** der Seitenleiste zeigt das Wissen des Lorebooks des Buchs, ohne dass du die Geschichte verlassen musst. So musst du nicht zwischen Reitern wechseln, um zu prüfen, was ein Eintrag sagt, oder um ein Wort darin zu korrigieren. Er wird jedes Mal neu gelesen, wenn du zum Reiter *Geschichte* zurückkehrst, sodass Änderungen, die du im Reiter *Lorebook* des Buchs gemacht hast, sofort erscheinen.

- Das **Suchfeld** oben zeigt nur die Einträge, die den Text in ihrem Namen, Inhalt, ihrer Notiz oder ihrem Filter haben (Groß- und Kleinschreibung spielen keine Rolle).
- Der **Stift** eines Eintrags öffnet ein kleines Fenster mit seinem Namen, ob er verwendet wird, dem Filter und der Art, wie nach ihm gesucht wird, dem Inhalt und der Notiz; **Speichern** schreibt sie. (Die Tags, die Reihenfolge und die Aktivierungseinstellungen änderst du im Reiter *Lorebook* des Buchs.) Nichts lässt sich ändern, während ein Agent am Lorebook arbeitet.
- Die **Schaltfläche zum Vergrößern** (⤢) öffnet den [Lorebook-Editor im Vollbild](../lorebooks/lorebooks.md#editing-in-full-screen) (nur Desktop): den neben dem Suchfeld beim Lorebook, den eines Eintrags bei diesem Eintrag, den eines Unter-Lorebooks (mit *Unter-Lorebooks einbeziehen*) bei diesem Lorebook. Der Reiter wird neu geladen, wenn du den Editor schließt.

- Jeder Eintrag ist eine Zeile mit seinem Namen und dem Stift; der Pfeil öffnet ihn, um seinen **Inhalt** (als Markdown dargestellt) und den **Text, der ihn aktiviert** (den *Filtertext/-muster*, mit einem Hinweis, wenn es sich um einen regulären Ausdruck handelt) anzuzeigen.
- **Unter-Lorebooks einbeziehen** zeigt das Lorebook und jedes seiner Unter-Lorebooks (und deren Unter-Lorebooks, egal wie tief) als eigene Zeilen, mit ihren Einträgen darunter; klicke auf den Pfeil eines Lorebooks, um seine Einträge auszublenden. Ohne diese Option werden nur die Einträge des Lorebooks des Buchs aufgelistet.
- Einträge und Lorebooks, die ausgeschaltet sind (sie werden für die Geschichte nicht verwendet), werden abgeblendet angezeigt.
- Ein Buch ohne Lorebook zeigt stattdessen einen Hinweis.

![Der Reiter Lorebook mit zwei geöffneten Einträgen](../../images/story-lorebook-tab.png)

Wie jeder Reiter des Editors lässt er sich in ein eigenes Fenster ziehen, siehe unten.
<a id="the-planner-tab"></a>

### Der Planer-Tab

Der Tab **Planer** ist eine Liste der Szenen, die die Geschichte durchlaufen soll, in der Reihenfolge, in der du sie haben willst. Jede Szene
sind die Angaben eines Zugs, der sie schreibt: ein **Titel** für die Liste, der **Szenenort**, die **POV-Figur**, die **anwesenden Figuren** und die **Anweisungen**, dieselben vier
Felder wie im Formular für einen neuen Zug.

![Der Planer-Tab mit vier Szenen, die erste ist geschrieben](../../images/planner-tab.png)

- **Szene hinzufügen** öffnet ein Formular für eine neue Szene; sie kommt ans Ende der Liste. Der Stift bearbeitet eine Szene, die Pfeile verschieben sie nach oben oder unten, der Papierkorb löscht sie.
- Das **+** in der Zeile einer Szene **fügt eine neue Szene direkt darunter ein**, zwischen dieser Szene und der nächsten.
- Die Zusammenführen-Schaltfläche einer Szene **führt sie mit der nächsten zusammen**: Es öffnet sich ein Formular mit den Angaben beider, bereits miteinander verbunden (die beiden Titel, die Szenenorte und die Anweisungen untereinander, die anwesenden Figuren beider, die POV-Figur der ersten; hat die zweite Szene eine andere, sagt das Formular, welche). Ändere, was nicht passt, und drücke **Zusammenführen** (oder <kbd>Ctrl/Cmd+Enter</kbd>): Die beiden Szenen werden zu einer, an der Stelle der ersten. Es geschieht nur, wenn beide erledigt wurden.
- **Aus dem Zweig importieren** fügt für jeden Teil des Zweigs, auf dem das Buch liegt (vom ersten Teil bis zum neuesten), der Zugangaben hat, eine Szene hinzu: dessen Szenenort, POV-Figur, anwesende Figuren und Anweisungen, mit der ersten Zeile der Anweisungen als Titel. Teile ohne Zugangaben werden ausgelassen, und die Szenen kommen hinter die vorhandenen, nicht als erledigt. Verwende dies, um eine Überarbeitung einer Geschichte zu planen, die du geschrieben hast: importieren, die Züge zusammenführen, die zusammengehören, fehlendes einfügen und dann die Szenen auf einem neuen Zweig oder in einer Kopie des Buchs schreiben.
- Das Kästchen vor einer Szene zeigt an, dass sie **erledigt** (geschrieben) ist: Der Titel ist durchgestrichen und die Szene ist abgedunkelt. Niemand prüft das für dich, hake es ab, wenn du die Szene geschrieben hast. Die Zeile über der Liste zählt, wie viele erledigt sind.
- **Klicke auf eine Szene**, um ihre Angaben in das **Formular für einen neuen Zug** zu übernehmen (das Formular öffnet sich mit dem Szenenort, der POV-Figur, den anwesenden Figuren und den Anweisungen der Szene darin; was dort stand, wird ersetzt), und drücke **Generieren**, wenn du damit zufrieden bist. Auf dem Telefon schließt sich zuerst die Schublade.
- Die Szenen werden zusammen mit dem Buch gespeichert, sind also in seinen Sicherungen und Kopien enthalten. Auch ein Modell kann Szenen hinzufügen: Der [Handlungsassistent](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) der Erweiterung Arc Outline und ein [Agent](../agents.md) können den Planer mit [Befehlen](../commands/index.md#the-planner) lesen und ändern; die Liste zeigt sofort, was sie hinzugefügt haben.

![Das Formular für eine neue Szene](../../images/planner-scene-dialog.png)

![Ein Klick auf eine Szene füllt das Formular für einen neuen Zug mit ihren Angaben](../../images/planner-new-turn.png)

Wie jeder Tab des Editors kann er in ein eigenes Panel herausgezogen werden, siehe unten.

<a id="arranging-the-tabs"></a>

### Tabs anordnen

Der Editor besteht aus Tabs: der **Geschichte** selbst (die Teile und die Leiste darunter), der **Gliederung**, dem **Lorebook** und den Tabs, die [Erweiterungen](../extensions/index.md) hinzufügen (Charaktere, Arcs, Side Query ...). Ziehe einen Tab an seiner Kopfzeile, um die Anordnung zu ändern
(nur am Desktop; das Telefon behält eine Spalte, mit der Gliederung in einer Schublade). Ein neues Buch hat die Gliederung und das Lorebook in einem Panel
links und die Geschichte in einer eigenen Spalte rechts; du kannst die Geschichte überall hinsetzen, zum Beispiel in die Mitte mit der
Gliederung auf der einen und dem Lore auf der anderen Seite, oder über der Gliederung:

- Lege ihn auf einen der gestrichelten Streifen, die über, zwischen und unter den Panels einer Spalte erscheinen, um ihn in ein
  eigenes Panel in dieser Spalte zu **lösen**; die Panels einer Spalte stehen untereinander.
- Lege ihn auf einen der hohen Streifen links, rechts oder zwischen den Spalten, um ihn **seitlich zu lösen**: Er bekommt eine
  eigene Spalte neben den anderen. Erweitere den Editor mit den Trennlinien, um Platz für weitere Spalten zu schaffen.

Die Panels und Spalten sind durch Trenner getrennt: Ziehe sie, um jedem Panel den nötigen Platz zu geben. Die Größen werden zusammen
mit der Anordnung gespeichert.

![Die Seitenleiste mit Charakteren in einer eigenen Spalte und Side Query darunter](../../images/story-editor-tabs.png)

- Lege ihn auf die Tab-Kopfzeile oder den Inhalt eines anderen Panels, um dieses Panel zu **ergänzen** (auf einem Tab ablegen setzt ihn davor, auf dem Inhalt setzt ihn ans Ende). Ein Panel, das seinen letzten Tab verliert, verschwindet, ebenso eine Spalte ohne Panels.

Anordnung und Größen werden zusammen mit dem Buch gespeichert, also ist es beim nächsten Öffnen und in jedem Browser dasselbe; der Tab, den jedes
Panel zeigt, wird mit dem übrigen Arbeitsbereich gemerkt. Ein Tab einer Erweiterung, die nicht geladen ist, behält seinen Platz, bis die Erweiterung wieder da ist.

Die Geschichte ist immer irgendwo: Ihr Tab kann nicht geschlossen werden, und ein Tab, der zusammen mit anderen Tabs in einem Panel liegt, ist ausgeblendet,
solange ein anderer ausgewählt ist. Alles, was einen Teil anzeigt, holt die Geschichte zurück: Ein Klick auf einen Eintrag der Gliederung, ein Treffer der
Suche, die Wahl eines Zweigs im [Geschichtenbaum](branches-and-story-tree.md) und das Starten einer Generierung wählen zuerst den Tab **Geschichte** aus. Während eine Generierung ihren Prompt vorbereitet (meist einige Sekunden), sind die anderen Tabs deaktiviert; sobald das Modell schreibt, sind sie wieder benutzbar, siehe [unten](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Tabs minimieren

Jeder Tab außer der Geschichte hat neben seinem Namen eine kleine **⌄**-Schaltfläche (Pfeil nach unten). Sie **minimiert** den Tab: Der Tab verlässt sein Panel (ein Panel,
dessen Tabs alle minimiert sind, verschwindet vorerst) und ein runthings Symbol davon, ein farbiger Kreis mit einer Zeichnung (eine Liste für
die Gliederung, ein Buch für das Lorebook, eine Sprechblase für die Anmerkungen, eine Lupe für Side Query und so weiter; ein Tab einer
Erweiterung ohne Zeichnung bekommt ein oder zwei Buchstaben), erscheint in einer Leiste am Rand des Editors. Fahre mit der Maus über das Symbol, um zu sehen, welcher Tab es ist. Nichts wird geschlossen, der Tab behält, was er zeigte. Klicke auf das Symbol, und der Tab kehrt in das Panel zurück, an seinen Platz unter den Tabs, und wird angezeigt.

Die Leiste ist da, solange ein Tab minimiert ist. Die Schaltfläche oben darin (die kreisenden Pfeile) schickt die Leiste im Uhrzeigersinn zur nächsten Seite des
Editors: links, oben, rechts, unten. Die Geschichte kann nicht minimiert werden, daher ist immer etwas auf dem Bildschirm.

![Minimierte Tabs in der Leiste am Rand des Editors](../../images/story-minimized-tabs.png)

Die Symbole stehen in alphabetischer Reihenfolge der Tab-Namen, bis du sie änderst: Ziehe ein Symbol auf ein anderes, und es nimmt dessen Platz ein. Welche Tabs minimiert sind, wo die Leiste liegt und die Reihenfolge der Symbole werden zusammen mit der Anordnung und den Größen im Buch gespeichert.

<a id="the-default-arrangement"></a>

### Die Standardanordnung

Ein neues Buch öffnet sich mit der Standardanordnung (Gliederung, Lorebook und Anmerkungen links, die Geschichte
rechts). Um deine eigene Anordnung zur Standard zu machen, stelle die Tabs wie gewünscht ein (Panels, Größen, minimierte Tabs, die Seite der
Leiste) und wähle **Layout als meinen Standard speichern** im Einstellungsmenü (⚙) der Geschichte. Jedes Buch ohne eigene Anordnung (ein Buch, dessen Layout du nicht verändert hast) öffnet sich dann damit; ein Buch, das du angeordnet hast, behält seine Anordnung. Der Standard wird
in deinen Benutzereinstellungen gespeichert.

Um zur Standardanordnung zurückzukehren, wähle **Standardlayout wiederherstellen** im Einstellungsmenü (⚙) der Geschichte und bestätige. Es ist die, die du gespeichert hast, oder die Standardanordnung, wenn du keine gespeichert hast. Es verschiebt nur die Tabs zurück (und vergisst die Größen und was minimiert ist): Deine Texte, der Tab, den jedes Panel zeigt, und die Tabs von Erweiterungen bleiben erhalten, die letzteren im ersten Panel.
<a id="writing-the-next-part"></a>

## Den nächsten Teil schreiben

1. Klicken Sie unten rechts auf **+**. Das Panel *Anweisungen für den neuen Zug* öffnet sich.
2. Füllen Sie die Felder aus. Alle sind optional, aber das Modell braucht zumindest eine gewisse Richtung:

   | Feld | |
   |---|---|
   | **Schauplatz der Szene** | Wo und wann der Teil spielt, z. B. *Der Leuchtturm, eine stürmische Nacht im späten Herbst*. |
   | **POV-Figur** | Aus wessen Sicht der Teil geschrieben ist. |
   | **Anwesende Figuren** | Wer in der Szene ist und in welchem Zustand. |
   | **Anweisungen** | Was in diesem Teil passieren soll. |

   *Schauplatz der Szene*, *POV-Figur* und *Anwesende Figuren* werden aus dem letzten Teil übernommen, Sie ändern also nur, was anders ist.
   *Anweisungen* ist jedes Mal leer.
3. Klicken Sie auf **Generieren**.

![Das Panel Anweisungen für den neuen Zug](../../images/new-turn-instructions.png)

Der neue Teil erscheint am Ende und füllt sich, während das Modell ihn schreibt. Bei [Reasoning-Modellen](../inference-providers.md#reasoning-models)
erscheint das Reasoning zuerst unter **Reasoning anzeigen**.

Während ein Teil generiert wird, verwandelt sich die **+**-Schaltfläche in eine rote Schaltfläche **Stopp**. Stoppen behält den bis dahin geschriebenen Text als Teil.

<a id="while-a-part-is-generated"></a>

### Während ein Teil generiert wird

In den ersten Sekunden, während der Prompt aus dem Buch zusammengestellt wird, wartet das ganze Buchfenster. Sobald das Modell zu schreiben beginnt, sind nur der neue Teil und die Teile davor gesperrt:

- **Der neue Teil** lässt sich erst bearbeiten, wenn er fertig ist: keine Bearbeitung, kein Menü.
- **Die Teile davor** bleiben, wo sie sind: Sie können sie bearbeiten, kommentieren und neu entwerfen, aber nicht löschen, zusammenführen oder verzweigen, und die Geschichte bleibt auf ihrem Zweig (kein Wischen, kein Wechsel des Zweigs). Eine Änderung an einem früheren Teil oder an der Lore erreicht den gerade geschriebenen Teil nicht: Sein Prompt wurde bereits zusammengestellt. Sie wirkt sich auf das aus, was danach kommt.
- **Alles andere ist frei**: die Lore, die Einstellungen und der Prompt des Buchs, die Reiter der linken Leiste (Seitenabfrage, Bogenübersicht, Notizen ...), die Befehlsleiste. Eine Anfrage an das Modell wartet auf eine freie Verbindung zum Modell, wenn die Generierung die einzige ist.
- **Wartend**: ein zweiter Zug, Suchen und Ersetzen, ein Import in das Buch und die Massenlöschung. Auch die Reiter für Baum und Sicherung warten.

Was das Fenster ausgraut, wird auch abgelehnt, wenn ein Befehl, ein Agent oder eine Erweiterung es verlangt: Marginalia nennt, was im Weg ist (*Das wird verwendet: …*).

**Das Buch zu verlassen stoppt die Generierung.** Die Ausstiegsschaltfläche fragt zuerst nach; *Ja* entspricht **Stopp**: Was angekommen ist, bleibt erhalten. Das Schließen des Browser-Tabs tut dasselbe, und der Browser fragt nach, bevor der Tab geschlossen oder neu geladen wird, während ein Teil generiert wird. Ein kurzer Netzwerkausfall oder ein für eine Minute zugeklapptes Notebook ist kein Verlassen: Die Seite kommt zurück und zeigt, was inzwischen angekommen ist.

Wenn das Modell einen Fehler zurückgibt (Server nicht erreichbar, falscher API-Schlüssel, Kontext zu lang ...), wird der Fehler angezeigt.
Ist noch kein Text angekommen, bleibt die Geschichte wie sie war: Ein neuer Teil oder Wisch wird entfernt, und ein neu generierter Teil erhält seinen
vorherigen Text zurück. Text, der vor dem Fehler angekommen ist, bleibt erhalten, wie bei **Stopp**. Beheben Sie das Problem und generieren Sie erneut.

Wie die Felder in den Prompt gelangen, legt der [Benutzer-Prompt](prompts.md#user-prompt) des Buchs fest. Mit dem Standard-Benutzer-Prompt erhält das Modell sie als *Aktueller Ort & Zeit*, *Point-of-View-Figur*,
*Aktuell anwesende Figuren / Zustand* und *Handlungsvorgaben für den nächsten Teil*.

!!!
Der Text wird in Markdown geschrieben: `*kursiv*`, `**fett**`, `# Überschrift` und so weiter werden formatiert angezeigt. Ein Teil mit einer
`#`-Überschrift ist ein Kapitel für die Erweiterung [Kapitelmarkierung](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Anweisungen entwerfen

Wissen Sie nicht, was als Nächstes passieren soll? **Anweisungen entwerfen** unter dem Feld *Anweisungen* des Panels lässt das Modell die Anweisungen an Ihrer Stelle schreiben, so wie Sie es tun würden. Ein Fenster öffnet sich mit:

| | |
|---|---|
| **Vorgabe** | Worum der Teil gehen soll: eine grobe Idee, eine Richtung („sie konfrontiert ihn wegen des Briefes“) oder gar nichts, dann führt das Modell die Geschichte dorthin, wohin sie sich entwickelt. Es beginnt mit dem, was Sie bereits in *Anweisungen* eingetragen haben. |
| **Plan der Geschichte** | Nur wenn etwas einen liefert. Die [Bogenübersicht](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) tut das, mit den Daten des aktuellen Kapitels. Nur lesbar; das Modell folgt ihm. |

**Anweisungen entwerfen** startet das Modell und streamt die Antwort in **Entworfene Anweisungen**, die Sie ändern oder leeren können, bevor Sie weitermachen; **Erneut entwerfen** versucht es noch einmal, **Stopp** behält, was angekommen ist. Nichts wird geändert, bis Sie **Als Anweisungen verwenden** klicken: Der Text ersetzt dann die *Anweisungen* des Panels (die anderen Felder bleiben unberührt), und das Panel öffnet sich wieder, bereit für **Generieren**.

Das Modell erhält die bisherige Geschichte (den älteren Teil als [Zusammenfassungen](summaries.md), wie bei einer Generierung), den Schauplatz der Szene, die POV- und anwesenden Figuren aus dem Panel, die Lore des letzten Teils, Ihre letzten Anweisungen als Beispiele dafür, wie Sie schreiben, Ihre Vorgabe und den Plan. Es sieht nicht Ihre Lorebuch-Einträge, die ein neuer Zug aktivieren würde. Die Anfrage verwendet den [Impersonate-Prompt](prompts.md#impersonate-prompt). Das Modell ist das des Buchs; Entwerfen kostet Tokens wie jede Anfrage.

![Entwerfen der Anweisungen für den nächsten Zug](../../images/impersonate-dialog.png)

Dasselbe leistet der Befehl [`/impersonate`](../commands/index.md), der den Text zurückgibt. [Agenten](../agents.md) erhalten ihn nicht, sie schreiben die Geschichte mit eigenen Anweisungen.

<a id="the-settings-menu"></a>

## Das Einstellungsmenü

Das Einstellungsmenü (⚙) in der unteren Leiste fasst zusammen, was auf die ganze Geschichte wirkt. **Suchen und Ersetzen** steht ganz oben; der Rest befindet sich in Untermenüs
(auch auf dem Telefon, außer den nur für den Desktop gedachten Punkten):

| Untermenü | Einträge |
|---|---|
| **Darstellung** | **Stile für Text ändern**, **Generierten Text hervorheben** und **Anmerkungen am Rand** (nur Desktop, der letzte) |
| **Analyse** | **Einen Bereich von Abschnitten mit KI kommentieren**, **Zusammenfassungen erstellen**, **Lore-Notizen mit einem Agenten anfertigen**, **Automatische Anmerkungen**, **Tokens des Zweigs**, **Lore-Nutzung**, **Statistik** |
| **Import & Export** | **Importieren**, **Geschichte exportieren** (und die Einträge von Erweiterungen, die importieren oder exportieren, etwa **Anweisungen exportieren**) |
| **Bereinigung** | **Verlauf des Buchs löschen**, **Papierkorb des Verlaufs leeren**, **Massenlöschung** |
| **Layout** (nur Desktop) | **Layout als meinen Standard speichern**, **Standardlayout wiederherstellen** |

Einträge, die [Erweiterungen](../extensions/index.md) mitbringen, stehen im passenden Untermenü oder am Ende des Menüs. Die Tasten der Einträge stehen unter
*Einstellungen → Tastenkürzel*, wo ein Eintrag mit seinem Untermenü benannt ist (*Analyse › Statistik*). Die Abschnitte unten sagen, wo jeder Eintrag zu finden ist.
<a id="the-part-menu"></a>

## Der Menü des Abschnitts

Jeder Abschnitt hat rechts oben ein Menü (☰). **Bearbeiten**, **Änderungen verwerfen**, **Neu generieren**, **Variante**, **Überarbeiten**, **Verlauf** und **Bilder** stehen im Menü selbst, **Löschen** ist dessen letzter Eintrag, und der Rest befindet sich in vier Untermenüs: **Anmerkungen** (*Abschnitt kommentieren*, *Mit KI kommentieren*), **Zusammenfassung** (*Zusammenfassung generieren* / *Zusammenfassung anzeigen*, *Zusammenfassung löschen*), **Untersuchen** (*Prompt anzeigen*, *Prompts vergleichen*, *Token-Verbrauch*, *Lore-Nutzung*) und **Verzweigen & Zusammenführen** (*Geschichte verzweigen*, *Mit dem vorherigen Abschnitt zusammenführen*). Ein Untermenü, das für den Abschnitt nichts enthält, wird nicht angezeigt. Der Prüfer fügt sein Untermenü **Prüfen** nach *Zusammenfassung* hinzu. Die Einträge:

| Eintrag | |
|---|---|
| **Bearbeiten** | Macht den Text zu einem bearbeitbaren Feld. **Speichern** (derselbe Menüeintrag) speichert ihn. Das Starten einer Generierung oder das Verlassen des Tabs speichert offene Änderungen ebenfalls. |
| **Änderungen verwerfen** | Nur während der Text bearbeitet wird: schließt das Feld ohne Speichern, der Abschnitt behält den Text, mit dem er zuletzt gespeichert wurde. Wenn du den Text geändert hast, wirst du zuerst gefragt. |
| **Neu generieren** | Schreibt den letzten Abschnitt erneut und ersetzt dabei seinen Text. Nur beim letzten Abschnitt. |
| **Variante** | Schreibt eine weitere Version des letzten Abschnitts und behält die alte als eigenen Zweig. Nur beim letzten Abschnitt. Siehe [Zweige & Story-Baum](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Überarbeiten** | Lässt das Modell diesen Abschnitt anhand deiner Anweisungen neu schreiben, bei jedem Abschnitt, siehe [unten](#redrafting-a-part). Nicht während der Text bearbeitet wird. |
| **Verlauf (n)** | Die [Textversionen](version-history.md) des Abschnitts mit der Anzahl der Versionen: vergleichen, eine wiederherstellen, eine benennen. Nicht während der Text bearbeitet wird. |
| **Abschnitt kommentieren** | Erstellt eine [Anmerkung](annotations.md#making-an-annotation) zum ganzen Abschnitt. Um einzelne Wörter zu kommentieren, markiere sie und klicke mit der rechten Maustaste (**Auswahl kommentieren**). |
| **Mit KI kommentieren** | Lässt ein Modell den Abschnitt [prüfen](annotations.md#letting-a-model-annotate) und Anmerkungen vorschlagen. Nicht während der Text bearbeitet wird. Für mehrere Abschnitte auf einmal verwende **Einen Bereich von Abschnitten mit KI kommentieren** im Einstellungsmenü (⚙), und für jeden neuen Abschnitt **Automatische Anmerkungen** (siehe [Anmerkungen](annotations.md#annotating-every-new-part-automatically)). |
| **Geschichte verzweigen** | Startet einen neuen Zweig ab diesem Abschnitt, siehe [Zweige & Story-Baum](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Mit dem vorherigen Abschnitt zusammenführen** | Fügt den Text dieses Abschnitts an den davorliegenden an, siehe [unten](#merging-a-part-into-the-previous-one). Nicht beim ersten Abschnitt. |
| **Prompt anzeigen** | Zeigt den genauen Prompt, der für diesen Abschnitt an das Modell gesendet wurde. |
| **Token-Verbrauch** | Zeigt, wie der Kontext des Modells für diesen Abschnitt genutzt wurde: ein Diagramm der Teile des Prompts (Anweisungen des Modells, Lore, Zusammenfassungen, bisherige Geschichte, deine Anweisungen, für die Antwort reservierter Platz) und die Größe jeder an das Modell gesendeten Nachricht. Wenn ein Teil der Geschichte nicht hineinpasste, steht dort, wie viele Abschnitte weggelassen wurden. Abschnitte, die geschrieben wurden, bevor dies erfasst wurde, haben kein Diagramm. |
| **Lore-Nutzung** | Listet die [Lorebook](../lorebooks/index.md)-Einträge auf, die in den Prompt dieses Abschnitts eingeflossen sind, mit den Tokens, die jeder gekostet hat, und dem, was ihn gefunden hat (der Prompt oder der Inhalt eines anderen Eintrags, wenn das Lorebook tiefer sucht). Abschnitte, die von Hand eingegeben wurden, bevor dies erfasst wurde, oder für ein Buch ohne Lorebook geschrieben wurden, melden, dass nichts aufgezeichnet wurde. Für das ganze Buch verwende **Lore-Nutzung** im Einstellungsmenü (⚙), siehe [unten](#which-lore-entries-were-used). |
| **Bilder** | Hängt Bilder an den Abschnitt an, siehe [Bilder](#images). |
| **Zusammenfassung generieren** / **Zusammenfassung anzeigen** | Fasst die Geschichte bis zu diesem Abschnitt zusammen oder zeigt die Zusammenfassung an, siehe [Zusammenfassungen](summaries.md). |
| **Zusammenfassung löschen** | Entfernt die Zusammenfassung dieses Abschnitts. Bei einer [Metazusammenfassung](summaries.md#meta-summaries) wird gefragt, ob sie aufgelöst oder ganz gelöscht werden soll. |
| **Löschen** | Löscht den Abschnitt, siehe [unten](#deleting-a-part). |

*Neu generieren* und *Variante* verwenden die Details des Zugs des letzten Abschnitts (siehe [unten](#turn-details)). Um zu ändern, was
passieren soll, bearbeite sie zuerst.

![Das Menü eines Abschnitts mit geöffnetem Untermenü Untersuchen](../../images/story-part-menu.png)

![Token-Verbrauch eines Abschnitts](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadaten des Abschnitts

Neben jedem Abschnitt:

| | |
|---|---|
| **Reihenfolge Nr. n \| DB-ID: m** | Die Position des Abschnitts im Zweig und seine ID in der Datenbank. |
| **Modell** | Der Inferenzanbieter, der den Abschnitt geschrieben hat. |
| **Tokens: x (y)** | `x` Tokens Text im Abschnitt, `y` Tokens im Prompt, der ihn erzeugt hat. |
| **Wörter** | Wörter im Abschnitt. |
| **Details des Zugs** | Die Anweisungen, aus denen der Abschnitt geschrieben wurde. |

<a id="turn-details"></a>

### Details des Zugs

**Details des Zugs** öffnet den *Schauplatz der Szene*, die *POV-Figur*, die *Anwesenden Figuren* und die *Anweisungen*, aus denen der Abschnitt
geschrieben wurde. Beim letzten Abschnitt können sie geändert werden (Änderungen werden sofort gespeichert) und werden von *Neu generieren* und
*Variante* verwendet. Bei früheren Abschnitten sind sie nur lesbar.

![Details des Zugs eines Abschnitts](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Den Text bearbeiten

Generierter Text gehört dir und darf von dir geändert werden. Wähle **Bearbeiten** im Menü des Abschnitts, ändere den Text und wähle
**Speichern**. Bearbeitete Abschnitte werden so an das Modell gesendet, wie du sie bearbeitet hast.

Wenn du einen Abschnitt bearbeitest, den eine [Zusammenfassung](summaries.md) abdeckt, wird diese Zusammenfassung veraltet; Marginalia bemerkt das bei der nächsten Generierung und verwirft die Zusammenfassung. Damit das nicht unerwartet passiert, prüft **Speichern** den neuen Text zuerst: Würde er Zusammenfassungen veralten lassen, erscheint ein Fenster, das angibt, wie viele es sind, und **Zurück zur Bearbeitung** anbietet (nichts wird gespeichert, der Editor bleibt mit deinem Text offen), **Trotzdem speichern** (die Zusammenfassungen werden veraltet) und **Speichern und Zusammenfassungen neu validieren** (der Text wird gespeichert und die Zusammenfassungen bleiben gültig, für eine Änderung, die nicht verändert, was sie aussagen, etwa einen Tippfehler). Die Prüfung erfolgt für **Speichern** (im Menü, per Tastenkürzel und über die Schaltfläche des Editors); die automatischen Speicherungen, etwa beim Starten einer Generierung bei geöffnetem Editor, fragen nicht nach.

<a id="keyboard-shortcuts"></a>

## Tastenkürzel

Auf einem Computer (das [Telefon-Layout](../phone.md) hat keine) lässt sich der Schreibablauf mit der Tastatur steuern:

| Taste | |
|---|---|
| `+` | Öffnet das Feld *Anweisungen für den neuen Zug* mit dem Cursor in *Anweisungen*. |
| `Ctrl+Enter` | Generiert aus dem geöffneten Feld heraus. `Esc` schließt es; die Tastatur geht zurück zur Geschichte. |
| `Ctrl+.` | Stoppt die laufende Generierung. |
| `s` / `r` | **Variante** / **Neu generieren** des letzten Abschnitts. |
| `e` | Öffnet den Editor des Abschnitts, der am meisten auf dem Bildschirm sichtbar ist (ohne einen solchen der letzte Abschnitt). |
| `Ctrl+S` | Speichert den Abschnitt, der gerade bearbeitet wird (auch wenn der Cursor in seinem Text steht). |
| `/` | Setzt den Cursor in die Befehlsleiste (der Tab der Geschichte wird zuerst angezeigt). |
| `Ctrl+Shift+F` | Öffnet den Tab *Gliederung* und setzt den Cursor in die Suche des Zweigs (`Enter` / `Shift+Enter` springen zum nächsten / vorherigen Treffer). |
| `Ctrl+Shift+H` | Öffnet das Fenster *Suchen und Ersetzen* des Buches. |
| `a` | **Auswahl kommentieren**: erstellt eine Anmerkung zum markierten Text eines Abschnitts. |
| `Alt+PageDown` / `Alt+PageUp` | Zeigt den nächsten / vorherigen Tab und durchläuft dabei die Tabs aller Blätter in der Reihenfolge des Layouts und beginnt danach wieder von vorn. |

Eine Taste ohne `Ctrl`, `Alt` oder `Meta` (etwa `+`, `s`, `r`, `e`, `a`, `/`) funktioniert nur, wenn kein Textfeld den Fokus hat, damit sie
das Schreiben nie stört. Die Tastenkürzel funktionieren im Geschichte-Tab des geöffneten Buches und nicht, solange ein anderes Fenster darüber geöffnet ist. Die Schaltflächen zeigen ihre Taste im Tooltip, und das Menü des letzten Abschnitts zeigt sie nach *Bearbeiten*, *Neu generieren* und *Variante*. Auf einem Mac funktioniert `Cmd` dort, wo eine Taste `Ctrl` sagt (sofern du diese `Cmd`-Taste nicht einer anderen Aktion zugewiesen hast). Du kannst jede Taste unter
[Einstellungen → Tastenkürzel](../account.md#keyboard-shortcuts) ändern.
<a id="deleting-a-part"></a>

## Teile löschen

**Löschen** entfernt den Teil aus der Geschichte. Die Teile, die darauf folgten, bleiben erhalten und setzen am Teil davor an. Das Löschen eines Teils in der Mitte der Geschichte entfernt ihn also nur. Wird der letzte Teil gelöscht, wird der Teil davor zum neuen Ende des Zweigs.

<a id="deleting-many-parts-at-once"></a>

### Mehrere Teile auf einmal löschen

Das Einstellungsmenü (⚙) enthält **Massenlöschung**. Wenn Sie sie einschalten, erscheint links bei jedem Teil ein Kontrollkästchen, und unter der Geschichte wird eine Leiste mit der Anzahl der markierten Teile und einer Schaltfläche **Löschen** angezeigt. Markieren Sie die Teile, die Sie entfernen möchten (die Markierungen bleiben beim Scrollen erhalten, die Geschichte lädt nur einen Ausschnitt der Teile). **Umschalt + Klick** auf ein Kontrollkästchen markiert (oder hebt die Markierung auf) alle Teile zwischen diesem und dem zuletzt angeklickten Teil. Ein markierter Teil wird rot hinterlegt. Drücken Sie dann **Löschen**: Nach einer Bestätigung werden die Teile nacheinander gelöscht, während ein Fortschrittsbalken angezeigt wird, der nicht angehalten werden kann. Sie werden so gelöscht wie ein einzelner Teil: Die Teile, die einem gelöschten Teil folgten, setzen am Teil davor an, und die gelöschten Teile landen im Papierkorb. Danach wird die Geschichte erneut angezeigt und die Massenlöschung ist beendet. **Abbrechen** in der Leiste oder erneutes Wählen von **Massenlöschung** beendet den Modus, ohne etwas zu löschen. Das Starten einer Generierung beendet ihn ebenfalls. Ein markierter Teil, mit dem ein Ausschnitt oder eine Übersetzung arbeitet, bleibt in der Geschichte erhalten, und Marginalia zeigt an, welcher Vorgang ihn verwendet.

![Massenlöschung mit einigen markierten Teilen](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Teil überarbeiten

**Neu generieren** und **Variante** arbeiten nur auf dem letzten Teil und schreiben ihn von Grund auf neu. **Überarbeiten** arbeitet auf jedem Teil, auch auf einem älteren in der Mitte der Geschichte, und behält, was du nicht zu ändern verlangst. Wähle **Überarbeiten** im Menü des Teils:

1. Schreibe, was sich ändern soll, unter **Anweisungen für die Überarbeitung**: *mehr Dialog zwischen den beiden hinzufügen*, *den Kampf länger machen*, *die Erklärung der Karte streichen*, *das Ende so lassen, wie es ist*. Leere Anweisungen schreiben den Teil mit denselben Ereignissen neu.
2. Wähle **Länge**, wenn der Teil **Kürzer** oder **Länger** werden soll (der Standard ist **Gleiche Länge**): Ein Satz, der dies verlangt, wird nach deinen Anweisungen hinzugefügt (siehe [Redraft Shorter Prompt and Redraft Longer Prompt](prompts.md#redraft-shorter-and-longer)). Wähle **Überarbeiten**. Der Text fließt in **Überarbeiteter Text**; **Stopp** beendet ihn vorzeitig. Wähle **Erneut überarbeiten** für einen weiteren Versuch, gern mit anderen Anweisungen. **Unterschiede** zeigt, was sich gegenüber dem Teil geändert hat, Wort für Wort. Hat der Teil offene [Kommentare](annotations.md), gibt **Deal with the open annotations of the part** (standardmäßig zuerst geprüft) sie dem Modell ebenfalls mit, und **Anwenden** markiert sie als erledigt.
3. Der überarbeitete Text ist ein Feld, das du weiterhin bearbeiten kannst (sein Eckensymbol lässt es wachsen, bis der ganze Text hineinpasst), und **Unterschiede** folgt deinen Änderungen. **Anwenden** ersetzt den Text des Teils durch ihn; **Schließen** verwirft ihn. Vor **Anwenden** ändert sich nichts im Buch.

![Das Überarbeitungsfenster: die Anweisungen, der neue Text und die Unterschiede](../../images/redraft-dialog.png)

Das Modell erhält die [Überarbeitungs-Prompts](prompts.md#redraft-prompt): den Teil, deine Anweisungen, die Geschichte vor dem Teil (die ältere Geschichte als [Zusammenfassungen](summaries.md), wo sie gültig sind, der Rest als Text, der neueste Teil zuerst, wenn nicht alles hineinpasst), so viel von der Geschichte nach dem Teil, wie hineinpasst (bis zu einem Viertel des Platzes), die Lore, die die Lorebooks für diesen Teil jetzt liefern (für den Zug des Teils neu berechnet, sodass eine Änderung an einem Eintrag die Überarbeitung erreicht; die Lore, mit der der Teil geschrieben wurde, wird nur verwendet, wenn das Buch kein Lorebook hat), den Blickwinkel, die Zeitform und den Stil des Buches sowie die Szenerie, die Figur mit Blickwinkel, die anwesenden Figuren und die Anweisungen des Zuges, aus dem der Teil stammt. Die Geschichte nach dem Teil ist da, damit der neue Text weiterhin in sie übergeht: Ereignisse, auf die die folgenden Teile sich stützen, sollten bleiben.

Der Teil behält seinen Platz, seine Zusammenfassung, seine Bilder und wie er erzeugt wurde; der Text, seine Wörter und Tokens werden ersetzt, und der Teil gilt als bearbeitet. Das Anwenden der Überarbeitung ist eine Bearbeitung wie **Speichern** im Editor, daher werden [Zusammenfassungen](summaries.md), die den alten Text beschreiben, veraltet, und Marginalia sagt, wie viele. Eine Zusammenfassung am Teil selbst gehört dazu.

Wenn eine Zusammenfassung den Teil beschreibt (der Teil hat eine, oder ein neuerer Teil des Zweigs hat eine), enthält das Fenster das Kontrollkästchen **Revalidate the summaries this redraft makes outdated**, anfangs nicht aktiviert. Aktiviere es, wenn die Zusammenfassungen noch das Richtige sagen (die Überarbeitung hat die Formulierung geändert, nicht, was geschieht): **Anwenden** berechnet ihren Hash dann neu, und sie bleiben gültig. Lass es deaktiviert, wenn sich die Ereignisse geändert haben, und erstelle die Zusammenfassungen neu.

Das Fenster blockiert die Geschichte nicht, siehe [Mit dem Fenster daneben arbeiten](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Mit dem Überarbeitungs- und Kommentarfenster daneben arbeiten

Auf einem Computer stehen die Fenster **Überarbeiten** und **Mit KI kommentieren** rechts neben der Geschichte und lassen sie nutzbar: Du kannst lesen, scrollen und die Geschichte bearbeiten, während das Modell schreibt. Ziehe ein Fenster an seinem Titel, um es zu verschieben, oder an seinem Rand, um seine Größe zu ändern. Der Pfeil (⌄) im Titel klappt ein Fenster in das [Laufpanel](#the-panel-of-the-runs) unten links der Seite ein: Das Modell arbeitet weiter, **Öffnen** (oder ein Klick auf den Eintrag) holt das Fenster zurück, und **Stopp** beendet das Modell (ist es fertig, schließt **Schließen** das Fenster). Auf einem Telefon verdecken die Fenster die Geschichte weiterhin, lassen sich aber ebenso einklappen.

![Das Überarbeitungsfenster neben der Geschichte, während das Modell schreibt, und der Lauf im Panel unten links](../../images/redraft-beside-story.png)

**Schließen und Stopp sind zwei Dinge.** Das eigene **Stopp** des Fensters beendet das Modell, und das Fenster bleibt mit dem, was angekommen ist. **Schließen**, während das Modell arbeitet, beendet es nicht: Das Fenster klappt in das Panel ein, und das Modell arbeitet in seiner Warteschlange weiter; seine Antwort steht im Fenster, wenn du es wieder öffnest. Schließe ein Fenster, dessen Modell fertig ist, oder schließe es im Panel mit **Schließen**, dann ist es weg; das Schließen des Buches oder des Browser-Tabs beendet auch ein eingeklapptes Fenster. Dasselbe gilt für die anderen Fenster, die auf ein Modell warten: eine **Zusammenfassung** (und *Create summaries*, *Rewrite all summaries*), den [Reviewer](../extensions/reviewer.md), die Buchbeschreibung, die Generatoren für Lore und Gliederung, die Bild- und Manga-Generatoren sowie den Persona-Autor der Beta Readers. Die Fenster *Studio* und *Convert* des Hörbuchs blockieren die Seite ebenfalls nicht; ihr Schließen hat die Konvertierung nie gestoppt.

Solange ein Fenster auf einem Teil offen (oder eingeklappt) ist, zeigt der Teil neben seinem Menü, was er als Nächstes tut (*Redrafting · The book*), und **Neu generieren**, **Variante**, **Überarbeiten**, **Mit KI kommentieren**, **Merge into previous** und **Löschen** sind für diesen Teil deaktiviert, damit das Fenster seinen Teil nicht verliert. Den Teil zu bearbeiten bleibt möglich. Das Schließen des Buches schließt seine Fenster und stoppt ihre Modelle. Ein Teil, an dem ein [Übersetzer](../extensions/real-language-translator.md) arbeitet, zeigt auf die gleiche Weise *Translating the speech of the part...*, und zwei Übersetzer arbeiten nie gleichzeitig an einem Teil: Der zweite meldet, dass der Teil in Gebrauch ist.

- Wenn du eine Überarbeitung des ganzen Teils **Anwenden** und der Teil nach dem Öffnen des Fensters geändert wurde (du hast ihn inzwischen bearbeitet), fragt Marginalia zuerst nach: **Unterschiede** vergleicht dann den Teil, wie er jetzt ist, mit der Überarbeitung, sodass du siehst, was das Anwenden ersetzen würde; wähle **Trotzdem anwenden** oder **Abbrechen**. Eine Überarbeitung, die [sich selbst anwendet](annotations.md#annotating-every-new-part-automatically), tut das nicht, wenn der Teil geändert wurde: Das Fenster wartet stattdessen auf dich.
- Wurde der Teil gelöscht, bevor du die Überarbeitung **Anwenden** oder die Kommentare hinzufügst, sagt das Fenster es, schreibt nichts und schließt sich.

<a id="the-panel-of-the-runs"></a>

### Das Laufpanel

Alles, was gerade mit einem Modell an deinen Büchern arbeitet, steht unten links auf jeder Seite: eine Generierung, eine Überarbeitung oder ein Kommentar, eine Übersetzung durch den Übersetzer [Real Language](../extensions/real-language-translator.md) oder [Conlang](../extensions/conlang-translator.md), eine Neufassung der Zusammenfassungen, der Knowledge Tracker, der die Geschichte liest, eine [Hörbuch](../extensions/audiobook.md)-Konvertierung, ein Befehl der Befehlsleiste, der etwas dauert (*Draft instructions*, eine Zusammenfassung ...: Sein Fenster steht an der Seite und klappt wie die anderen ein). Die Fenster, in denen du auf eine Antwort wartest, stehen ebenfalls darin, solange das Modell antwortet: **Impersonate**, der Hilfeassistent, eine schnelle Übersetzung, der Conlang-Designer, ein Chat von Side Query, Beta Readers oder der Plot-Assistent. Es listet deine Läufe aus **allen deinen offenen Seiten** auf (anderer Tab, Telefon), nicht nur die, die auf dieser Seite gestartet wurden. Läuft nichts, wird nichts angezeigt.

![Das Laufpanel geöffnet: die Überarbeitung eingeklappt darin, mit dem Ende des Modelltexts, Öffnen und Stopp](../../images/runs-panel.png)

- **Geschlossen** ist es eine kleine Leiste: der erste Lauf, wie viele weitere (*Translating the speech of the part... · 2 more*) und ein wanderndes Balkensymbol, solange ein Modell arbeitet. Klicke darauf, um die Liste zu öffnen; sie öffnet sich von selbst, wenn du ein Fenster in sie einklappst.
- **Geöffnet** zeigt jeder Lauf, was er tut, das Buch, wie lange er läuft, worauf er wartet und das Ende dessen, was das Modell schreibt:
  - *Waiting for the model: 2 requests before this one*, wenn das Modell mit anderen Anfragen beschäftigt ist (siehe *Connections* unter [Inferenzanbieter](../inference-providers.md));
  - *The model is thinking...* oder *The model is writing...*, darunter das Ende seines Denkens oder seiner Antwort;
  - *Waits for you*, wenn der Lauf auf eine Entscheidung von dir wartet (eine zu übernehmende Überarbeitung, ein Vorschlag des Knowledge Tracker zur Prüfung, die Frage des Prompt Inspectors vor einer Generierung, ein Befehl des Plot-Assistenten, den du erlauben musst).
- **Stopp** beendet den Lauf, wo immer er gestartet wurde. **Öffnen** oder ein Klick auf den Eintrag zeigt ein eingeklapptes Fenster wieder (nur auf der Seite, auf der es geöffnet wurde) oder den Lauf selbst; es heißt **Prüfen**, wenn der Lauf auf dich wartet. Ein Lauf des Knowledge Tracker öffnet sein Buch auf dieser Seite (wenn hier kein anderes Buch offen ist; sonst wirst du gebeten, dieses zuerst zu schließen), wechselt zum Tab Knowledge und zeigt die Prüfung. **Schließen** schließt ein Fenster endgültig (und stoppt sein Modell).
- Auf einem Computer kannst du das Panel an seinem Griff (⋮ links) verschieben; es bleibt in diesem Browser dort, wo du es lässt. In der unteren Hälfte der Seite wächst es beim Öffnen nach oben, wie an seiner Ecke, und es bleibt immer innerhalb der Seite. Steht es dort, wo der runde Knopf des [Hilfeassistenten](../administration/help-assistant.md) ist (über einem Buch liegen beide in der unteren linken Ecke), weicht der Knopf zur Seite aus und kehrt zurück, wenn der Platz frei ist.

Endet ein Lauf, den du gestartet hast, nachdem seine Seite geschlossen wurde, sendet eine andere offene Seite von dir ihre [Benachrichtigung](../account.md#notifications) (für Läufe, die eine haben, etwa die Neufassung der Zusammenfassungen).

<a id="redrafting-only-a-passage"></a>

### Nur eine Passage überarbeiten

Um einige Sätze neu zu schreiben und den Rest des Teils unberührt zu lassen, markiere sie im Text des Teils und klicke mit der **rechten Maustaste** (auf einem Telefon ein langes Drücken): Das Menü hat **Auswahl überarbeiten**. (Das Menü ☰ des Teils wird dafür nicht angeboten, denn sein Öffnen kann in manchen Browsern die Auswahl verlieren.) Das Fenster arbeitet wie oben, mit zwei Unterschieden: Es zeigt den **Ausgewählten Text**, und die Überarbeitung ist nur der Text, der ihn ersetzt. Das Modell erhält den ganzen Teil mit der markierten Auswahl sowie die Geschichte und Lore darum herum, sodass sich die neue Passage an die Sätze davor und danach anschließt. **Unterschiede** vergleicht die Auswahl mit dem überarbeiteten Text; **Anwenden** setzt den überarbeiteten Text an die Stelle der Auswahl und behält den Rest des Teils, einschließlich des Leerzeichens oder Zeilenumbruchs an den Rändern der Auswahl. Bei offenen [Kommentaren](annotations.md) bietet das Kontrollkästchen nur die an, deren Passage sich mit der Auswahl überschneidet. Wurde der Teil geändert, nachdem du den Text markiert hast (in einem anderen Fenster), verweigert **Anwenden** es, und du markierst ihn erneut. Während der Teil bearbeitet wird, wird die Auswahl im Editor verwendet, und die Bearbeitung wird zuerst gespeichert.

<a id="what-follows-a-generated-part"></a>

### Was auf einen erzeugten Teil folgt

Ist ein Teil, den du erzeugt hast (von der Seite oder aus der Befehlsleiste), fertig, kann das Buch von sich aus weiteres damit tun, in dieser Reihenfolge:

1. **Kommentieren**: Der [automatische Kommentar](annotations.md#annotating-every-new-part-automatically) prüft den Teil.
2. **Überarbeiten**: Der Teil wird neu geschrieben, um die Anmerkungen aufzugreifen.
3. **Übersetzen**: Die Übersetzer [Real language](../extensions/real-language-translator.md) und [Conlang](../extensions/conlang-translator.md) übersetzen seine Reden.
4. **Knowledge**: Der [Knowledge Tracker](../extensions/knowledge-tracker.md) liest ihn.
5. **Audio**: Das [Hörbuch](../extensions/audiobook.md) macht daraus Audio.

Nur die Schritte, die das Buch eingeschaltet hat, laufen. Sie laufen auf dem Server, sodass die Seite geschlossen werden kann, und sie bilden einen Eintrag im [Laufpanel](#the-panel-of-the-runs): *After part 14: Annotate ✓ · Redraft … · Translate*, mit dem Text des Modells und einem **Stopp**. Was den Text ändert, kommt zuerst, damit der Tracker und das Audio den endgültigen Text bekommen. Ein Schritt, der auf dich wartet (Anmerkungen oder eine zu wählende Überarbeitung), öffnet sein Fenster, und der Eintrag hat **Prüfen**, um es in einer anderen Seite zu öffnen.

- **Stopp**, das Schließen eines Fensters, das auf dich wartet, oder ein fehlgeschlagener Schritt beendet die Kette an dieser Stelle: Was erledigt wurde, bleibt, der Rest wird nicht ausgeführt, und eine Meldung nennt, was übrig ist und von Hand zu erledigen ist (jeder Schritt hat seine eigene Aktion am Teil).
- Während die **Überarbeitung** läuft, kann der nächste Teil nicht erzeugt werden (dir wird gesagt, durch welchen Lauf); während die anderen Schritte laufen, geht es.
- Der Teil kann nicht gelöscht werden, bis sein Text endgültig ist; ihn erneut zu erzeugen beendet, was auf seine frühere Version folgte.
- Ein [Agent](../agents.md) führt diese Schritte über eigene Befehle aus: Nach einem Teil, den ein Agent erzeugt hat, folgt nichts.
<a id="merging-a-part-into-the-previous-one"></a>

## Zusammenführen eines Abschnitts mit dem vorherigen

Ein Modell, das mitten im Satz abgebrochen hat (das Antwortlimit, eine unterbrochene Verbindung), lässt sich nicht zuverlässig mit „weiterschreiben“ dazu bringen, denselben Text fortzusetzen. Schreibe stattdessen den nächsten Abschnitt (*an der Stelle fortfahren, an der der letzte Abschnitt aufgehört hat*) und verbinde beide mit **Mit dem vorherigen Abschnitt zusammenführen** im Menü des zweiten. Es öffnet sich eine Überprüfung:

- **Texte verbinden mit**: eine Leerzeile, ein Zeilenumbruch, ein Leerzeichen oder nichts. Die Auswahl ist anhand des Endes des vorherigen Textes voreingestellt: ein Absatz nach einem Text, der einen Satz beendet hat, ein Leerzeichen nach einem, der abgebrochen wurde.
- Die **Szenenbeschreibung**, der **POV-Charakter**, die **anwesenden Charaktere** und die **Anweisungen** beider Durchgänge, zusammengeführt in Feldern, die du bearbeiten kannst. Was die beiden Durchgänge am Anfang gemeinsam haben, wird einmal behalten („Schreibe das Duell. Halte es kurz“ und „Schreibe das Duell. Füge Regen hinzu“ ergeben den ersten Teil, und „Füge Regen hinzu“ steht in der nächsten Zeile).
- **Daten**: was die beiden Abschnitte speichern, Wert für Wert (zum Beispiel die [Variablen](../templates/macros.md) der Geschichte und die Daten der Erweiterungen), mit der Angabe, ob der Wert gleich, verschieden oder nur bei einem der Abschnitte vorhanden ist. Der zusammengeführte Abschnitt behält den Wert des **späteren Abschnitts** (dort steht die Geschichte jetzt), es sei denn, du wählst für ihn den Wert des vorherigen Abschnitts oder lässt den Wert weg. Gleiche Werte sind ausgeblendet, sofern du sie nicht anforderst.

![Die Überprüfung, bevor ein Abschnitt mit dem vorherigen zusammengeführt wird](../../images/part-merge-dialog.png)

Der vorherige Abschnitt behält seinen Platz, seine Zusammenfassung und die Art, wie er erzeugt wurde (Prompt, Modell, Zeiten). Er erhält den verbundenen Text, die verbundene Begründung und die Bilder, und seine Wörter und Tokens werden neu gezählt. Der zusammengeführte Abschnitt wird gelöscht (er liegt im [Papierkorb](../trash.md)), und was auf ihn folgte, setzt sich beim vorherigen Abschnitt fort. Zusammenfassungen, die beide Abschnitte abdecken, werden veraltet, wie nach einer Bearbeitung. Der [Verlauf](version-history.md#backups-copies-and-the-trash) des zusammengeführten Abschnitts wird entfernt, und der verbleibende Abschnitt hat den verbundenen Text als Version **Zusammengeführt**. Ein Abschnitt kann nicht zusammengeführt werden, wenn er der erste Abschnitt des Buches ist oder wenn der Abschnitt davor andere Fortsetzungen hat (Varianten des Abschnitts). Lösche diese oder mache sie zuerst zu einem Zweig.

<a id="images"></a>

## Bilder

Ein Abschnitt kann Bilder haben: eine Karte, ein Charakterporträt, eine Illustration. Wähle **Bilder** im Abschnittsmenü, um die Liste der Bilder des Abschnitts zu öffnen.

![Der Dialog Bilder eines Abschnitts](../../images/images-dialog.png)

- **Bild hinzufügen** (oder eine Datei darauf ziehen) lädt ein PNG-, JPEG-, GIF- oder WebP-Bild mit bis zu 10 MB hoch. WebP-Bilder werden beim Hochladen in PNG umgewandelt, weil Word- und PDF-Dateien kein WebP enthalten können.
- **Bildunterschrift** wird unter dem Bild angezeigt. Sie kann leer bleiben.
- **Position im Abschnitt** ist ein 3x3-Raster. Die Spalte ist die Seite des Textes (links, mitte, rechts), die Zeile ist die Stelle darin: oben steht es vor dem Text, in der Mitte mitten im Text, unten nach dem Text. Klicke auf ein Feld, um es auszuwählen.
- **Text umfließt das Bild** lässt den Text um das Bild herumlaufen, statt darüber und darunter. Links oder rechts läuft der Text neben dem Bild her. In der mittleren Spalte steht der Text auf beiden Seiten: einige Blöcke des Textes (etwa 400 Zeichen, mindestens zwei Blöcke) an dieser Zeile werden zu zwei Spalten, der erste Teil links vom Bild und der Rest rechts, sodass der Text die linke Spalte hinunter gelesen wird und in der rechten weitergeht. Zwei Bilder an derselben Stelle stehen übereinander zwischen den Spalten. Wenn der Text keinen Platz für zwei Spalten hat (nur ein Absatz oder die Blöcke sind schon vom Band darüber belegt), ist das Bild ein eigener, zentrierter Block.
- **Breite** ist die Breite des Bildes in Prozent der Breite des Textes. **Automatisch** behält die Größe des Bildes bei (nie breiter als der Text; ein Bild mit Text auf beiden Seiten ist 30 %). Ein Bild, neben dem der Text umfließt, ist höchstens 60 % breit, eines mit Text auf beiden Seiten höchstens 40 %, damit der Text etwas Platz behält. Die Höhe folgt den Proportionen des Bildes.
- Die Papierkorb-Schaltfläche entfernt das Bild aus dem Abschnitt.

![Die Position eines Bildes: in der Mitte, mit dem Text, der es umfließt](../../images/images-position.png)

**Vorschau wie im Viewer** (eine Schaltfläche des Dialogs und derselbe Eintrag im Abschnittsmenü, wenn der Abschnitt Bilder hat) zeigt den Abschnitt so, wie der [Viewer](publishing.md) ihn zeigt, mit dem Textstil des Buches: die Bilder dort, wo du sie platziert hast, und einem Schalter für eine breite oder eine schmale (Telefon-)Ansicht. Sie verwendet den Text im Editor so, wie er jetzt ist, gespeichert oder nicht.

![Die Vorschau: der Text in zwei Spalten mit dem Bild dazwischen](../../images/images-preview.png)

Der Geschichtseditor selbst verwendet die Position nicht: Er zeigt die Bilder unter dem Text des Abschnitts als Liste. Die Position wird vom Viewer und von den [Exporten](export.md#images) verwendet.

Wo im Text ein Bild steht, hängt nicht von Zahlen ab, die mit ihm gespeichert sind: Der Text wird zwischen seinen Blöcken getrennt (Absätze, Listen, Zitate und Codeblöcke werden nie getrennt), sodass die Bilder dort bleiben, wo sie hingehören, wenn du den Text bearbeitest. Die Mitte ist die Stelle zwischen Blöcken, die der Mitte des Textes am nächsten liegt; ein Bild, um das der Text unten herumläuft, wird vor den letzten Block gesetzt, damit mindestens dieser Block daneben läuft. Bilder an derselben Stelle behalten die Reihenfolge der Liste; solche, die nebeneinander an derselben Stelle stehen (und den Text nicht umfließen lassen), stehen in einer Reihe, wenn sie passen. PDF und Word tun das für Bilder, die eine Breite haben.

Bilder sind **kein Teil des Textes**. Das Modell sieht sie nie, sie stehen nicht im Prompt, zählen nicht als Tokens und ändern die [Zusammenfassungen](summaries.md) nicht. Sie sind auch kein Markdown: Ein `![](...)`-Link, der im Text geschrieben ist, ist kein Bild des Abschnitts, verwende stattdessen **Bilder**.

Gut zu wissen:

- **Neu generieren** behält die Bilder des Abschnitts, auch wenn die Erzeugung fehlschlägt. **Variante** schreibt einen neuen Abschnitt (eine neue Version neben der alten), der ohne Bilder beginnt. **Geschichte verzweigen** kopiert die Bilder in den neuen Abschnitt.
- Bilder gehören zu deinem Konto. Im [Viewer](publishing.md) sehen auch Leser eines veröffentlichten Buches sie, es sei denn, du entfernst das Häkchen bei **Die Bilder den Lesern zeigen** im Reiter *Info* des Buches.
- Ein [Backup](backups.md) merkt sich, welche Bilder ein Abschnitt hat, aber nicht die Bilddateien. Wird ein Backup in ein anderes Konto oder eine andere Installation zurückgespielt, erhalten die Abschnitte ihre Bilder nicht.
- Ein Bild aus einem Abschnitt zu entfernen, nimmt es nur vom Abschnitt weg. Die Datei bleibt und ist weiterhin unter [Ressourcen](../resources.md) aufgeführt, wo du sie herunterladen, ersetzen oder löschen kannst. Wenn du sie dort löschst, wird sie aus jedem Abschnitt entfernt, der sie verwendet. Die Datei selbst bleibt im Datenordner.

<a id="summaries"></a>

## Zusammenfassungen

Die Schaltfläche **Zusammenfassungen** in der unteren Leiste (links vom Einstellungsmenü ⚙) öffnet die Übersicht der Zusammenfassungen des Zweigs, wo sie bearbeitet, gelöscht und zu Meta-Zusammenfassungen zusammengeführt werden können. Der Tooltip der Schaltfläche zeigt, wie viele Tokens die verwendeten Zusammenfassungen im Prompt belegen. Siehe [Zusammenfassungen](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Wie viel des Kontexts die Geschichte belegt

Das Einstellungsmenü (⚙) hat **Tokens des Zweigs**. Es zeigt, bevor du etwas erzeugst, was die Geschichte des aktiven Zweigs vom Prompt belegt: einen Balken für die Geschichte **ohne Zusammenfassungen** (alle Abschnitte) und **mit Zusammenfassungen** (die Zusammenfassungen und die Abschnitte nach dem neuesten), sowie eine Tabelle mit den Abschnitten, den Tokens und dem Anteil am **Prompt-Budget** (dem Kontext des Buch-Modells abzüglich des Platzes für die Antwort). Nur Zusammenfassungen, die die Geschichte noch beschreiben, zählen; die nicht mehr passenden werden genannt, sie werden beim Erzeugen eines Abschnitts verworfen. Das Lore, die Systemvorlage und die Anweisungen des Durchgangs nehmen Platz aus demselben Budget. Ohne ein Modell mit einer Kontextgröße hat die Tabelle keine Anteile.

![Tokens des Zweigs](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Welche Lore-Einträge verwendet wurden

**Lore-Nutzung** im Einstellungsmenü (⚙, **Analyse**) zeigt, welche Einträge des Lorebooks des Buches in den Generierungen verwendet wurden und welche nie,
damit ein Lorebook nach seiner tatsächlichen Nutzung gekürzt werden kann und nicht nach Vermutung. Jede Generierung speichert, welche Einträge in ihren
Prompt eingeflossen sind (und die Tokens jedes Eintrags); das Fenster summiert das über die Abschnitte:

- **Abschnitte** legt fest, was gezählt wird: den **Aktiven Zweig** oder **Alle Zweige**, also jeden Zweig und jede Variante des Buches.
- **Ab Abschnitt** und **Bis Abschnitt** begrenzen den Bereich, nummeriert wie in der Gliederung (der erste Abschnitt eines Zweigs ist 1; in allen Zweigen ist die Nummer die Tiefe des Abschnitts im Baum).
  Leer lassen für den Anfang und das Ende.
- **Anzeigen** zeigt die Einträge, die in den gezählten Abschnitten **Nie verwendet**, **Manchmal verwendet** oder **Immer verwendet** waren.

![Das Fenster Lore-Nutzung](../../images/lore-usage.png)

Die Tabelle hat eine Zeile für jeden Eintrag der Lorebooks des Buches, die meistgenutzten zuerst: sein Lorebook, ob er nie, manchmal oder immer
verwendet wurde, in wie vielen Abschnitten (von den Abschnitten mit einem Eintrag, mit Prozentangabe), die Tokens, die er insgesamt und pro Verwendung
gekostet hat, sowie der erste und der letzte Abschnitt, der ihn verwendet hat. Klicken Sie auf eine Kopfzeile, um zu sortieren. Ein Eintrag, der früher
verwendet wurde, aber jetzt nicht mehr in den Lorebooks des Buches ist (gelöscht, verschoben oder sein Lorebook ist ausgeschaltet oder nicht mehr das des Buches),
bleibt in der Liste, markiert als *derzeit nicht im Lorebook*; ein gelöschter Eintrag hat keinen Namen.

Gezählt werden nur Abschnitte mit einem Eintrag: Ein von Hand geschriebener Abschnitt, einer, der geschrieben wurde, bevor Marginalia diesen Eintrag führte,
oder einer, der entstand, als das Buch noch kein Lorebook hatte, hat keinen Eintrag, und das Fenster sagt, wie viele es sind. *Immer verwendete* Einträge könnten
konstante Einträge sein, *nie verwendete* sind Kandidaten für das Entfernen oder für einen besseren Filter; [**Aktivierung in der Vorschau anzeigen**](../lorebooks/entries.md#preview-activation) im Lorebook-Editor zeigt, warum ein Eintrag jetzt nicht auslöst.

<a id="text-styles"></a>

## Textstile

Das Einstellungsmenü (⚙) in der unteren Leiste hat **Darstellung → Stile für Text ändern**, ein Menü mit einem Eintrag pro Stil, nach Namen sortiert.
Wie Sie einen Stil erstellen, steht im [Leitfaden für Stile](styling-guide.md).
Ein Stil bestimmt, wie der Text der Abschnitte aussieht: Schrift, Größe, Farben, Ausrichtung, Überschriften ... Die Anwendung bringt zwei
Stile mit, die nicht geändert werden können: **Normal** (das Aussehen der Anwendung) und **Buch** (ein buchartiger Lesestil, den der [Viewer](publishing.md) verwendet). Eigene Stile können Sie unter [Einstellungen → Stile](../account.md#styles) anlegen.

- Der erste Eintrag, **Standard (...)**, folgt Ihrem *Standardstil* (dem, der in *Einstellungen → Stile* markiert ist; Normal, wenn Sie keinen gewählt haben). Ein neues Buch hat keinen eigenen Stil und zeigt diesen an.
- Wenn Sie einen anderen Eintrag wählen, wird der Stil **im Buch** gespeichert und überschreibt den Standard nur für dieses Buch; der angehakte Eintrag ist der, den das Buch jetzt hat. **Standard** nimmt die Wahl wieder zurück.
- Ein gelöschter Stil fällt in den Büchern, die ihn verwendet haben, auf Ihren Standard zurück.

![Darstellung, Stile für Text ändern: die Stile zur Auswahl](../../images/story-style-menu.png)

Im Stil **Buch** sind Absätze im Blocksatz, und lange Wörter werden automatisch getrennt, und zwar mit dem Wörterbuch der
**Sprache** des Buches (auf dem [Reiter Info](managing-books.md#the-about-tab) eingestellt). Die Silbentrennung übernimmt der Browser;
für einige seltene Sprachen gibt es kein Wörterbuch, dann wird der Text einfach nicht getrennt.

Bei Büchern, die den alten Schalter *Stile für Text ändern* eingeschaltet hatten, bleibt der Stil Buch angezeigt, bis Sie einen anderen Eintrag wählen.

<a id="highlighting-generated-text"></a>

## Generierten Text hervorheben

Das Einstellungsmenü (⚙) hat außerdem die ankreuzbare Option **Generierten Text hervorheben**. Solange sie eingeschaltet ist, werden die Wörter, die ein Modell geschrieben hat, in jedem Abschnitt hervorgehoben, damit auffällt, was Sie hinzugefügt oder umgeschrieben haben. Marginalia verfolgt den Text durch die [Versionen des Abschnitts](version-history.md): Jede Version wird über die vorherige gelegt, und die Wörter, die sie gemeinsam haben, behalten ihren Urheber. Ein Satz, den Sie in einen generierten Text getippt haben, bleibt also Ihrer, egal wie oft das Modell den Rest umschreibt. Ein Abschnitt, den Sie nie bearbeitet haben, wird ganz hervorgehoben; ein selbst geschriebener Abschnitt hat keine Hervorhebung. Was ein Agent geschrieben hat, zählt als Text des Modells. Die Option wird nicht gespeichert: Beim erneuten Öffnen der Seite ist sie wieder ausgeschaltet.

Ein Abschnitt, dessen [Verlauf ausgeschaltet ist](version-history.md), hat keine Versionen, denen man folgen könnte: Er wird mit dem Text verglichen, den das Modell zuerst generiert hat. Die Hervorhebung funktioniert auch auf dem [Telefon](../phone.md).

Dieselbe Zählung ergibt das Verhältnis: Der Reiter **Info** des Buches hat **Urheberschaft des Zweigs** mit einer Schaltfläche **Berechnen**, die angibt, wie viel Prozent der Wörter des aktiven Zweigs Sie geschrieben haben und wie viele ein Modell generiert hat. [Der Export](export.md) kann die Datei mit einer entsprechenden Angabe abschließen.

![Hervorgehobener generierter Text und das Einstellungsmenü](../../images/highlight-generated.png)

<a id="commands"></a>

## Befehle

Neben dem Menü unter der Geschichte gibt es eine **Befehlszeile**: Geben Sie `/` ein, um die Befehle zu sehen, wählen Sie einen aus und drücken Sie **Enter**
(oder die Abspielschaltfläche), um ihn auszuführen. Was ein Befehl zurückgibt, wird in einem kleinen Fenster über der Zeile angezeigt. Wie Sie Argumente schreiben, was die Hilfe zeigt und wie Sie einen Befehl abbrechen, der länger dauert, steht unter [Befehle](../commands/index.md). Die Befehlszeile gibt es bisher nur im Reiter Geschichte des Desktop-Layouts.
<a id="search-and-replace"></a>

## Suchen und Ersetzen

Das Einstellungsmenü (⚙) enthält außerdem **Verlauf des Buchs löschen** und **Papierkorb des Verlaufs leeren**, siehe [Versionsverlauf](version-history.md#deleting-versions).

Das Einstellungsmenü (⚙) enthält außerdem **Suchen und Ersetzen**. Damit wird ein Wort, ein Name oder eine Wendung in der ganzen Geschichte auf einmal geändert. Das Fenster hat die Felder **Suchen nach** und **Ersetzen durch** sowie diese Optionen:

![Das Einstellungsmenü mit Suchen und Ersetzen](../../images/story-settings-menu.png)

![Das Fenster Suchen und Ersetzen mit einer eingegebenen Suche](../../images/search-replace-dialog.png)

| Option | Standard | Bedeutung |
|---|---|---|
| **Per Regex suchen** | aus | Der Suchtext ist ein [regulärer Ausdruck](../regular-expressions.md), und der Ersatztext kann dessen Gruppen verwenden (`$1`). Andernfalls wird der Text so gesucht, wie er eingegeben wurde. |
| **Groß-/Kleinschreibung beachten** | an | Ein Text wird nur in der Schreibweise gefunden, die eingegeben wurde (*John* findet nicht *john*). Aus: Die Schreibweise spielt keine Rolle. Gesperrt, solange **Per Regex suchen** an ist. |
| **In Zusammenfassungen suchen** | an | Die [Zusammenfassungen](summaries.md) werden ebenfalls durchsucht und geändert. |
| **In allen Zweigen suchen** | aus | Alle [Zweige](branches-and-story-tree.md) werden durchsucht, sonst nur der aktive. |
| **Alle Zusammenfassungen neu validieren** | an | Nach dem Ersetzen ist jede [Zusammenfassung](summaries.md) des Buchs wieder gültig, siehe unten. |

Bei einem regulären Ausdruck wird die Groß- und Kleinschreibung immer so genommen, wie sie eingegeben wurde. Beginne ihn mit `(?i)`, um sie zu ignorieren. Drücke **Suchen** (die Schaltfläche rechts neben den Feldern; am Telefon darunter). Der Fortschrittsbalken zeigt, wie weit die Suche ist, und **Stopp** daneben unterbricht sie. Durch eine Suche wird nichts geändert.

Die Tabelle listet jeden Teil der Geschichte und jede Zusammenfassung auf, in dem bzw. der es einen Treffer gibt, mit Typ und ID. Öffne eine Zeile, um die Treffer darin zu sehen: den Text um den Treffer, so wie er ist und so wie er nach dem Ersetzen aussehen wird. Ein Treffer oder ein Ersatz, der zu lang zum Anzeigen ist, wird als *[gesuchter Text]* bzw. *[Ersatztext]* geschrieben.

![Die Treffer einer Suche, der erste Teil geöffnet](../../images/search-replace-found.png)

Ein Druck auf **Enter** in den Feldern **Suchen nach** oder **Ersetzen durch** bewirkt dasselbe wie **Suchen**.

Wenn die Suche fertig ist, sind die Felder und Optionen gesperrt, und die Schaltfläche **Ersetzen** ist aktiv. **Erneut suchen** vergisst die Treffer und entsperrt das Fenster, sodass du die Suche ändern kannst. **Ersetzen** ändert die Geschichte: Das Fenster ist danach vollständig eingefroren (auch **Stopp** funktioniert nicht), bis der Vorgang beendet ist, und dann ist es wieder leer. Ersetzte Teile werden als bearbeitet markiert, als hättest du sie selbst bearbeitet.

Ein Ersetzen in dem Text, den eine [Zusammenfassung](summaries.md#when-summaries-become-outdated) beschreibt, würde die Zusammenfassung veralten lassen. Ist **Alle Zusammenfassungen neu validieren** an, berechnet Marginalia den Prüfwert jeder Zusammenfassung des Buchs neu (auch die aller Zweige, auch jene, die das Ersetzen nicht berührt hat, und jene, die schon vorher veraltet waren). Danach sind alle gültig. Dadurch wird auch eine veraltete Zusammenfassung gültig, ohne dass ihr Text geprüft wird. Verwende diese Option daher nur, wenn du weißt, dass die Zusammenfassungen weiterhin das Richtige sagen.

Ist die Option aus, werden nur die Zusammenfassungen aktualisiert, die die Suche abdeckt. Die übrigen (Zusammenfassungen von Zweigen, die nicht durchsucht werden und ihre früheren Teile mit dem aktiven Zweig teilen; oder alle, wenn Zusammenfassungen nicht durchsucht werden) werden veraltet. Gibt es solche, zeigt Marginalia an, wie viele es sind, und fragt, ob es weitergehen soll. Eine Zusammenfassung, die schon veraltet war, bleibt es.

![Die Warnung über Zusammenfassungen, die veralten würden](../../images/search-replace-warning.png)

In der [Telefonansicht](../phone.md) stehen dasselbe Fenster mit den Feldern und Schaltflächen untereinander. Die Tabelle hat eine Spalte, die Typ und ID zeigt, darunter stehen Treffer und Ersatz.

Die [Übersicht der Zusammenfassungen](summaries.md#search-and-replace-in-the-summaries) hat dasselbe Fenster nur für die Zusammenfassungen.

Dieselbe Suche und dasselbe Ersetzen lassen sich über die [Kommandozeile](../commands/index.md#commands-that-come-with-marginalia) mit `/search-replace-parts` ausführen; vor dem Ersetzen wird nachgefragt.

<a id="importing-into-the-book"></a>

## Importieren in das Buch

Das Einstellungsmenü (⚙) hat **Importieren** (unter **Import & Export**). Es ist das Fenster des Reiters Bücher, das aus einer Datei ein neues Buch macht (siehe [Bücher verwalten](managing-books.md#creating-a-book)), hier aber für das Buch, in dem du dich gerade befindest: Die Abschnitte werden stattdessen **zur Geschichte hinzugefügt**. Als **Quelle** dient entweder das Zip-Archiv mit `.md`-Dateien, ein Dokument (Word, LibreOffice, RTF, Markdown, Klartext, EPUB), das Zip eines Obsidian-Vaults oder das Zip eines Scrivener-Projekts. Jede Quelle hat dieselben Texte und Optionen wie dort (die Wörter pro Abschnitt und die Überschriftenebene der Kapitel), und eine Zeile sagt, was der Import erzeugen wird. Die Chats von SillyTavern und LM Studio werden nicht angeboten: Sie sind ein Baum aus Zweigen, der in der Mitte einer Geschichte keinen Platz hat.

**Die Abschnitte platzieren** legt fest, wohin sie kommen:

- **Am Ende der Geschichte**: nach dem letzten Abschnitt des aktiven Zweigs (der erste Abschnitt eines leeren Buchs).
- **Vor einem Abschnitt** oder **Nach einem Abschnitt**: Wähle den Abschnitt aus der Liste (die Nummern sind die Stellen im aktiven Zweig, mit dem Anfang des Textes). Die Abschnitte werden zwischen zwei Abschnitte eingefügt: Alles, was auf die Stelle folgte, alle seine Zweige, folgt den neuen Abschnitten, und kein Abschnitt wird verändert oder neu erzeugt. Das Ende der Geschichte bleibt, wo es war (die Geschichte ist um so viele Abschnitte länger). Die Zusammenfassungen, die die Stelle abdecken, werden veralten, da sie die Geschichte ohne die neuen Abschnitte beschreiben. Das Fenster der [Zusammenfassungen](summaries.md) zeigt sie an und erstellt sie neu.

![Das Importfenster eines Buchs](../images/book-import-into.png)

Die Abschnitte eines Archivs sind die Dateien in der Reihenfolge ihrer Namen, wobei Zahlen in den Namen als Zahlen verglichen werden (`2.md` vor `10.md`), auch in Ordnern des Archivs. Ordner, versteckte Dateien (`.name`), der Ordner `__MACOSX` und Dateien, die nicht `.md` sind, werden übersprungen. Die Datei ist der Text des Abschnitts unverändert; sie wird nicht an das Modell geschickt und nicht verändert, alle Abschnitte gelten als von dir geschrieben.

Der Import zeigt einen Fortschrittsbalken mit der Anzahl der fertigen Abschnitte. **Stopp** beendet ihn nach dem Abschnitt, der gerade gespeichert wird: Die bereits hinzugefügten Abschnitte bleiben in der Geschichte (der Import wird nicht rückgängig gemacht), und eine Meldung sagt, wie viele importiert wurden.

<a id="after-an-import"></a>

### Nach einem Import

Sind die Abschnitte im Buch, bietet ein Fenster **Import abgeschlossen** die zwei Dinge an, die das Schreiben einer langen Geschichte erleichtern (ein Buch, das gerade neu importiert wurde, wird zuerst geöffnet, und das Fenster liegt davor). Beide stehen auch im Einstellungsmenü (⚙, **Analyse**), sodass sie später oder erneut ausgeführt werden können:

- **Zusammenfassungen erstellen...** schreibt die Zusammenfassungen des Importierten, siehe [Zusammenfassungen erstellen](summaries.md#create-summaries). Mache das zuerst: Ein Agent sperrt das Buch, während er arbeitet.
- **Notizen zum Lore mit einem Agenten erstellen...** startet einen [Agenten](../agents.md), der die Geschichte liest und die Figuren, Orte, Dinge und Begriffe, die er findet, als Einträge im Lorebook des Buchs schreibt, siehe [Notizen zum Lore](../agents.md#lore-notes-of-an-imported-book).

**Nicht jetzt** schließt das Fenster, und keines von beidem wird ausgeführt.

![Das Fenster nach einem Import](../../images/import-finished.png)

Der gleiche Import in ein Lorebook wird unter [Import & Export](../lorebooks/import-export.md#importing-an-archive-of-text-files) beschrieben.

<a id="exporting-the-story"></a>

## Geschichte exportieren

Das Einstellungsmenü (⚙) hat außerdem **Geschichte exportieren**. Damit wird der aktive Zweig als Text-, Markdown-, HTML-, Word-, PDF- oder EPUB-Datei gespeichert. Siehe [Ein Buch exportieren](export.md).

<a id="what-the-model-gets"></a>

## Was das Modell erhält

Für jeden neuen Abschnitt sendet Marginalia:

1. Die **Haupt-Vorlage** als Systemnachricht: Rolle, [Lorebook](../lorebooks/index.md)-Einträge, Erzählperspektive, Zeitform, Stil und [Zusammenfassungen](summaries.md). Der Jailbreak-Prompt des [Inferenzanbieters](../inference-providers.md#general-settings) steht, falls aktiviert, ganz vorn.
2. Die vorherigen Abschnitte des aktiven Zweigs als eigene frühere Antworten des Modells, so viele, wie in das [Kontextlimit](../protocols.md#limits) passen, die neuesten zuerst. Ältere Abschnitte, die nicht hineinpassen, werden weggelassen (sofern kein [Zusammenfassung](summaries.md) sie abdeckt).
3. Den **Benutzer-Prompt** mit den Anweisungen für den neuen Abschnitt.

*Prompt anzeigen* bei einem beliebigen Abschnitt zeigt genau, was gesendet wurde. Die Vorlagen werden unter [Buch-Prompts](prompts.md) beschrieben.
