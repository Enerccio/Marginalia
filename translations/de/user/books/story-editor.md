---
label: Story-Editor
order: 90
---

# Story-Editor

Der Tab **Story** eines Buchs ist der Ort, an dem Sie schreiben. Er zeigt die Abschnitte des [aktiven Zweigs](branches-and-story-tree.md)
vom Anfang bis zum Ende.

![Der Story-Editor](../../images/hero-story-editor.png)

Diese Seite beschreibt das Desktop-Layout. Auf einem Telefon besteht die Geschichte aus einer einzigen Spalte, die Gliederung
steckt in einer Schublade, siehe [Marginalia auf dem Telefon verwenden](../phone.md#the-story).

| Bereich | |
|---|---|
| **Gliederung** (links) | Ein Eintrag pro Abschnitt, `#1`, `#2`... mit der Datenbank-ID des Abschnitts. Klicken Sie auf einen Eintrag, um zum Abschnitt zu scrollen (die davor liegenden Abschnitte werden zuerst geladen, wenn er noch nicht geladen ist). Die Kopfzeile zeigt, wie viele Abschnitte der Zweig hat. Ein Abschnitt, der eine [Zusammenfassung](summaries.md) hat, zeigt hinter seinem Eintrag ein kleines Dokumentsymbol. Die Erweiterung [Kapitelmarkierung](../extensions/chapter-marker.md) zeigt hier die Kapiteltitel an. |
| **Lorebook** (links, neben der Gliederung) | Eine Ansicht des Lore des Buchs mit einem Suchfeld und einer schnellen Bearbeitung des Textes eines Eintrags, siehe [Der Lorebook-Tab](#the-lorebook-tab). |
| **Anmerkungen** (links, neben der Gliederung) | Die [Anmerkungen](annotations.md) zu Passagen der Geschichte, für das gesamte Buch. |
| **Planer** (links, neben der Gliederung) | Die Szenen, die Sie planen, eine Liste, die Sie von Hand füllen, siehe [Der Planer-Tab](#the-planner-tab). |
| **Story** (Mitte) | Der Text jedes Abschnitts mit seinem Menü (☰) und seinen Metadaten rechts und darunter die untere Leiste. |
| **Untere Leiste** | Links die Schaltfläche **Zusammenfassungen** (Listensymbol) und das Einstellungsmenü (⚙), die Befehlszeile und rechts die Schaltfläche **+**. |

So sieht ein neues Buch aus. Alle diese Bereiche sind Tabs, die Sie beliebig anordnen können, die Geschichte eingeschlossen, siehe
[Tabs anordnen](#arranging-the-tabs). Der Trenner zwischen der Seitenleiste und der Geschichte lässt sich ziehen, und Marginalia
merkt sich, wo Sie ihn zuletzt hingezogen haben (er wird mit dem Buch gespeichert), und stellt ihn wieder her, wenn Sie das Buch erneut öffnen.

Die Geschichte öffnet sich immer am unteren Ende, beim neuesten Abschnitt. Ein langes Buch wird nicht auf einmal gezeichnet: Nur die letzten Abschnitte werden
geladen, und die früheren werden oben ergänzt, während Sie nach oben scrollen, ohne den Abschnitt zu verschieben, den Sie gerade lesen. Die Abschnitte am
anderen Ende werden wieder entfernt, sodass nicht mehr als die Einstellung **Auf einmal geladene Abschnitte in der Geschichte** Ihres
[Kontos](../account.md) geladen bleiben (leer lädt alle). Wenn Sie schneller scrollen, als die Abschnitte eintreffen, zeigt eine Markierung *Frühere Abschnitte werden geladen* (oder *spätere Abschnitte*) am Rand, dass es mehr gibt. Ein Klick auf einen Abschnitt in der Gliederung oder das Finden eines Abschnitts mit der Suche lädt die Abschnitte um ihn herum, und das Beginnen eines neuen Abschnitts bringt Sie zurück zum Ende der Geschichte.

<a id="searching-the-story"></a>

### Die Geschichte durchsuchen

Über der Gliederung befindet sich ein Suchfeld für den Text des **Zweigs, auf dem Sie sich befinden** (die Suche im Geschichtenbaum
durchsucht alle Zweige). Da ein langes Buch nicht auf einmal geladen wird, kann die Suche des Browsers (Strg+F) keinen Text in den
nicht geladenen Abschnitten finden; diese Suche kann es.

- Geben Sie ein, wonach Sie suchen; die Suche startet, sobald Sie aufhören zu tippen. Mehrere Wörter müssen alle im selben Abschnitt
  vorkommen, eine Wortgruppe steht in doppelten Anführungszeichen (`"old key"`), und `*` steht für beliebige Zeichen (`light*house`). Groß- und Kleinschreibung spielen keine Rolle.
- Jede Stelle, an der ein Wort steht, ist ein Treffer, und sie sind über den gesamten Zweig durchnummeriert. Unter dem Feld sehen Sie
  *Treffer 3 von 27, in Abschnitt #5* und eine Leiste, die zeigt, wie weit Sie sind. Die Geschichte scrollt zum Treffer und lädt die Abschnitte um ihn herum,
  wenn sie nicht geladen sind.
- Die Schaltflächen **nach oben** und **nach unten** springen zum vorherigen bzw. nächsten Treffer (nach dem letzten geht es wieder zum ersten).
  **Enter** im Feld tut dasselbe wie die Schaltfläche nach unten, **Umschalt+Enter** wie die Schaltfläche nach oben.
- Alle Treffer in den geladenen Abschnitten sind im Text hervorgehoben, der Treffer, bei dem Sie gerade sind, in einer stärkeren Farbe. Leeren Sie das Feld, um
  die Hervorhebung zu entfernen.

Auf dem Telefon befindet sich die Suche in der Gliederungsschublade; die Schublade schließt sich, wenn sie einen Treffer zeigt, öffnen Sie sie erneut für den nächsten.

<a id="the-lorebook-tab"></a>

### Der Lorebook-Tab

Der Tab **Lorebook** der Seitenleiste zeigt den Lore des Lorebooks des Buchs, ohne dass Sie die Geschichte verlassen müssen, sodass Sie nicht
den Tab wechseln müssen, um zu prüfen, was ein Eintrag sagt, oder ein Wort darin zu korrigieren. Er wird jedes Mal neu gelesen, wenn Sie zum Tab *Story* zurückkehren, sodass
Änderungen, die Sie im Tab *Lorebook* des Buchs vorgenommen haben, sofort erscheinen.

- Das **Suchfeld** oben zeigt nur die Einträge, die den Text in ihrem Namen, Inhalt, ihrer Notiz oder ihrem Filter haben (Groß- und Kleinschreibung spielen keine Rolle).
- Der **Stift** eines Eintrags öffnet ein kleines Fenster mit seinem Namen, ob er verwendet wird, dem Filter und der Art, wie er gesucht wird, dem Inhalt
  und der Notiz; **Speichern** schreibt sie (die Tags, die Reihenfolge und die Aktivierungseinstellungen ändern Sie im Tab *Lorebook* des Buchs). Es kann nichts
  geändert werden, während ein Agent am Lorebook arbeitet.
- Die **Schaltfläche zum Vergrößern** (⤢) öffnet den [Vollbild-Lorebook-Editor](../lorebooks/lorebooks.md#editing-in-full-screen) (nur Desktop): den neben dem Suchfeld des Lorebooks, den eines Eintrags bei diesem Eintrag, den eines Unter-Lorebooks (mit *Unter-Lorebooks einbeziehen*) bei diesem Lorebook. Der Tab wird neu geladen, wenn Sie den Editor schließen.

- Jeder Eintrag ist eine Zeile mit seinem Namen und dem Stift; der Pfeil öffnet ihn, um seinen **Inhalt** (als Markdown dargestellt) und den **Text, der ihn aktiviert** (den
  *Filtertext/-muster*, mit einem Hinweis, wenn es sich um einen regulären Ausdruck handelt) anzuzeigen.
- **Unter-Lorebooks einbeziehen** zeigt das Lorebook und jedes seiner Unter-Lorebooks (und deren Unter-Lorebooks, egal wie tief) als eigene Zeilen, mit ihren Einträgen
  darunter; klicken Sie auf den Pfeil eines Lorebooks, um seine Einträge auszublenden. Ohne diese Option werden nur die Einträge des Lorebooks des Buchs aufgelistet.
- Einträge und Lorebooks, die ausgeschaltet sind (sie werden für die Geschichte nicht verwendet), werden abgeblendet angezeigt.
- Ein Buch ohne Lorebook zeigt stattdessen einen Hinweis.

![Der Tab Lorebook mit zwei geöffneten Einträgen](../../images/story-lorebook-tab.png)

Wie jeder Tab des Editors kann er unten in ein eigenes Fenster herausgezogen werden, siehe unten.
<a id="the-planner-tab"></a>

### Der Planer-Tab

Der Tab **Planer** ist eine Liste der Szenen, die die Geschichte durchlaufen soll, in der Reihenfolge, die du dir wünschst. Jede Szene ist die Beschreibung eines Zugs, der sie schreibt: ein **Titel** für die Liste, der **Szenenschauplatz**, die **POV-Figur**, die **anwesenden Figuren** und die **Anweisungen**, dieselben vier Felder wie im Formular für einen neuen Zug.

![Der Tab Planer mit vier Szenen, die erste ist geschrieben](../../images/planner-tab.png)

- **Szene hinzufügen** öffnet ein Formular für eine neue Szene; sie kommt ans Ende der Liste. Der Stift bearbeitet eine Szene, die Pfeile verschieben sie nach oben oder unten, der Papierkorb löscht sie.
- Das **+** in der Zeile einer Szene **fügt eine neue Szene direkt darunter ein**, zwischen diese Szene und die nächste.
- Die Zusammenführen-Schaltfläche einer Szene **führt sie mit der nächsten zusammen**: Ein Formular öffnet sich mit den Angaben beider, bereits verbunden (die beiden Titel, die Szenenschauplätze und die Anweisungen untereinander, die anwesenden Figuren beider, die POV-Figur der ersten; hat die zweite Szene eine andere, sagt das Formular, welche). Ändere, was nicht passt, und drücke **Zusammenführen** (oder <kbd>Strg/Cmd+Enter</kbd>): Die zwei Szenen werden zu einer, an der Stelle der ersten. Das geschieht nur, wenn beide erledigt sind.
- **Aus dem Zweig importieren** fügt für jeden Teil des Zweigs, auf dem das Buch gerade liegt (vom ersten Teil bis zum neuesten), der Zugangaben hat, eine Szene hinzu: seinen Szenenschauplatz, die POV-Figur, die anwesenden Figuren und die Anweisungen, mit der ersten Zeile der Anweisungen als Titel. Teile ohne Zugangaben werden ausgelassen, und die Szenen kommen nach den vorhandenen, nicht als erledigt. Verwende das, um eine Umschreibung einer bereits geschriebenen Geschichte zu planen: importieren, die zusammengehörenden Züge zusammenführen, Fehlendes einfügen und dann die Szenen auf einem neuen Zweig oder in einer Kopie des Buches schreiben.
- Das Kästchen vor einer Szene zeigt an, dass sie **erledigt** (geschrieben) ist: Der Titel ist durchgestrichen und die Szene ist abgedunkelt. Nichts prüft das für dich, hake es ab, wenn du die Szene geschrieben hast. Die Zeile über der Liste zählt, wie viele erledigt sind.
- **Klicke auf eine Szene**, um ihre Angaben in das **Formular für den neuen Zug** zu übernehmen (das Formular öffnet sich, mit dem Szenenschauplatz, der POV-Figur, den anwesenden Figuren und den Anweisungen der Szene darin; was dort stand, wird ersetzt), und drücke **Generieren**, wenn du damit zufrieden bist. Auf dem Telefon schließt sich zuerst die Schublade.
- Die Szenen werden zusammen mit dem Buch gespeichert, sie sind also in seinen Sicherungen und Kopien enthalten. Auch ein Modell kann Szenen hinzufügen: Der [Handlungsassistent](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) der Erweiterung Bogengliederung und ein [Agent](../agents.md) können den Planer mit [Befehlen](../commands/index.md#the-planner) lesen und ändern; die Liste zeigt sofort an, was sie hinzugefügt haben.

![Das Formular für eine neue Szene](../../images/planner-scene-dialog.png)

![Ein Klick auf eine Szene füllt das Formular für den neuen Zug mit ihren Angaben](../../images/planner-new-turn.png)

Wie jeder Tab des Editors kann er in ein eigenes Fenster herausgezogen werden, siehe unten.

<a id="arranging-the-tabs"></a>

### Tabs anordnen

Der Editor besteht aus Tabs: der **Geschichte** selbst (die Teile und die Leiste darunter), der **Gliederung**, dem **Lorebook** und den Tabs, die [Erweiterungen](../extensions/index.md) hinzufügen (Figuren, Bögen, Nebenabfrage ...). Ziehe einen Tab an seiner Kopfzeile, um sie neu anzuordnen (nur am Desktop; das Telefon behält eine Spalte mit der Gliederung in einer Schublade). Ein neues Buch hat die Gliederung und das Lorebook in einem Fenster links und die Geschichte in einer eigenen Spalte rechts; du kannst die Geschichte überall hinsetzen, zum Beispiel in die Mitte, mit der Gliederung auf einer Seite und dem Lore auf der anderen, oder über der Gliederung:

- Lasse ihn auf einem der gestrichelten Streifen fallen, die oberhalb, zwischen und unterhalb der Fenster einer Spalte erscheinen, um ihn in ein eigenes Fenster in dieser Spalte zu **lösen**; die Fenster einer Spalte stehen untereinander.
- Lasse ihn auf einem der hohen Streifen links, rechts oder zwischen den Spalten fallen, um ihn **seitlich zu lösen**: Er bekommt eine eigene Spalte neben den anderen. Vergrößere den Editor mit den Trennlinien, um Platz für weitere Spalten zu schaffen.

Die Fenster und die Spalten sind durch Teiler getrennt: Ziehe sie, um jedem Fenster den Platz zu geben, den es braucht. Die Größen werden zusammen mit der Anordnung gespeichert.

![Die Seitenleiste mit Figuren in einer eigenen Spalte und der Nebenabfrage darunter](../../images/story-editor-tabs.png)

- Lasse ihn auf der Kopfzeile eines anderen Tabs oder auf dem Inhalt eines anderen Fensters fallen, um sich **diesem Fenster anzuschließen** (fällt er auf einen Tab, kommt er vor diesen Tab; fällt er auf den Inhalt, kommt er ans Ende). Ein Fenster, das seinen letzten Tab verliert, verschwindet, und ebenso eine Spalte ohne Fenster.

Die Anordnung und die Größen werden zusammen mit dem Buch gespeichert, es ist also beim nächsten Öffnen und in jedem Browser dasselbe; der Tab, den jedes Fenster zeigt, wird zusammen mit dem übrigen Arbeitsbereich gemerkt. Ein Tab einer Erweiterung, die nicht geladen ist, behält seinen Platz, bis die Erweiterung wieder da ist.

Die Geschichte ist immer irgendwo: Ihr Tab kann nicht geschlossen werden, und ein Tab, der zusammen mit anderen Tabs in einem Fenster liegt, ist verborgen, während ein anderer ausgewählt ist. Was einen Teil zeigt, holt die Geschichte zurück: Ein Klick auf einen Eintrag der Gliederung, ein Treffer der Suche, die Wahl eines Zweigs im [Geschichtsbaum](branches-and-story-tree.md) und das Starten einer Generierung wählen zuerst den Tab **Geschichte** aus. Während eine Generierung ihren Prompt vorbereitet (meist einige Sekunden), sind die anderen Tabs deaktiviert; sobald das Modell schreibt, sind sie wieder benutzbar, siehe [unten](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Tabs minimieren

Jeder Tab außer der Geschichte hat neben seinem Namen eine kleine **⌄**-Schaltfläche (Pfeil nach unten). Sie **minimiert** den Tab: Der Tab verlässt sein Fenster (ein Fenster, dessen Tabs alle minimiert sind, verschwindet vorerst), und ein runder Button mit ihm erscheint, ein farbiger Kreis mit einer Zeichnung (eine Liste für die Gliederung, ein Buch für das Lorebook, eine Sprechblase für die Anmerkungen, eine Lupe für die Nebenabfrage und so weiter; einem Tab einer Erweiterung ohne Zeichnung wird ein oder zwei Buchstaben gegeben), in einer Leiste am Rand des Editors. Fahre mit der Maus über das Symbol, um zu sehen, welcher Tab es ist. Nichts wird geschlossen, der Tab behält, was er gezeigt hat. Klicke auf das Symbol, und der Tab kehrt in sein Fenster an den Platz unter den Tabs zurück, an dem er war, und wird angezeigt.

Die Leiste ist da, solange irgendein Tab minimiert ist. Die Schaltfläche oben darin (die kreisenden Pfeile) schickt die Leiste an die nächste Seite des Editors, im Uhrzeigersinn: links, oben, rechts, unten. Die Geschichte kann nicht minimiert werden, daher ist immer etwas auf dem Bildschirm.

![Minimierte Tabs in der Leiste am Rand des Editors](../../images/story-minimized-tabs.png)

Die Symbole stehen in alphabetischer Reihenfolge der Namen der Tabs, bis du sie änderst: Ziehe ein Symbol auf ein anderes, und es nimmt dessen Platz ein. Welche Tabs minimiert sind, wo die Leiste steht und die Reihenfolge der Symbole werden zusammen mit dem Buch gespeichert, zusammen mit der Anordnung und den Größen.

<a id="the-default-arrangement"></a>

### Die Standardanordnung

Ein neues Buch öffnet sich mit der Standardanordnung (die Gliederung, das Lorebook und die Anmerkungen links, die Geschichte rechts). Um deine eigene Anordnung zur Standardanordnung zu machen, stelle die Tabs so ein, wie du sie möchtest (Fenster, Größen, minimierte Tabs, die Seite der Leiste), und wähle im Einstellungsmenü (⚙) der Geschichte **Layout als meinen Standard speichern**. Jedes Buch, das keine eigene Anordnung hat (ein Buch, dessen Anordnung du nicht geändert hast), öffnet sich dann damit; ein Buch, das du angeordnet hast, behält seine Anordnung. Der Standard wird in deinen Benutzereinstellungen aufbewahrt.

Um zur Standardanordnung zurückzukehren, wähle im Einstellungsmenü (⚙) der Geschichte **Standardlayout wiederherstellen** und bestätige. Das ist die, die du gespeichert hast, oder die Standardanordnung, falls du keine gespeichert hast. Sie verschiebt nur die Tabs zurück (und vergisst die Größen und was minimiert ist): Deine Texte, der Tab, den jedes Fenster zeigt, und die Tabs der Erweiterungen bleiben erhalten, Letztere im ersten Fenster.
<a id="writing-the-next-part"></a>

## Das nächste Kapitel schreiben

1. Klicken Sie unten rechts auf **+**. Das Panel *Anweisungen für den neuen Zug* öffnet sich.
2. Füllen Sie die Felder aus. Alle sind optional, aber das Modell braucht zumindest etwas Orientierung:

   | Feld | |
   |---|---|
   | **Schauplatz der Szene** | Wo und wann der Abschnitt spielt, z. B. *Der Leuchtturm, eine stürmische Nacht im späten Herbst*. |
   | **POV-Figur** | Aus wessen Perspektive der Abschnitt geschrieben ist. |
   | **Anwesende Figuren** | Wer in der Szene ist und in welchem Zustand. |
   | **Anweisungen** | Was in diesem Abschnitt geschehen soll. |

   *Schauplatz der Szene*, *POV-Figur* und *Anwesende Figuren* werden aus dem letzten Abschnitt übernommen, sodass Sie nur ändern,
   was anders ist. *Anweisungen* beginnt jedes Mal leer.
3. Klicken Sie auf **Generieren**.

![Das Panel Anweisungen für den neuen Zug](../../images/new-turn-instructions.png)

Der neue Abschnitt erscheint am Ende und füllt sich, während das Modell ihn schreibt. Bei [Reasoning-Modellen](../inference-providers.md#reasoning-models)
erscheint die Begründung zuerst unter **Reasoning anzeigen**.

Während ein Abschnitt generiert wird, verwandelt sich die Schaltfläche **+** in eine rote Schaltfläche **Stopp**. Wenn Sie stoppen, bleibt der bis dahin geschriebene Text als Abschnitt erhalten.

<a id="while-a-part-is-generated"></a>

### Während ein Abschnitt generiert wird

In den ersten Sekunden, während der Prompt aus dem Buch zusammengestellt wird, wartet das gesamte Buchfenster. Sobald das Modell mit dem Schreiben beginnt, sind nur der neue Abschnitt und die davor liegenden Abschnitte gesperrt:

- **Der neue Abschnitt** kann bis zu seinem Abschluss nicht berührt werden: keine Bearbeitung, kein Menü.
- **Die davor liegenden Abschnitte** bleiben, wo sie sind: Sie können sie bearbeiten, kommentieren und neu entwerfen, aber nicht löschen, zusammenführen oder verzweigen, und die Geschichte bleibt auf ihrem Zweig (kein Swipe, kein Wechsel des Zweigs). Eine Änderung an einem früheren Abschnitt oder am Lore erreicht den gerade geschriebenen Abschnitt nicht: Sein Prompt wurde bereits zusammengestellt. Sie wirkt sich auf das aus, was danach kommt.
- **Alles andere ist frei**: das Lore, die Einstellungen und der Prompt des Buchs, die Tabs der linken Leiste (Seitenabfrage, Bogenübersicht, Notizen ...), die Befehlsleiste. Eine Anfrage an das Modell wartet auf eine freie Verbindung zum Modell, wenn die Generierung die einzige ist.
- **Wartend**: ein zweiter Zug, Suchen und Ersetzen, ein Import in das Buch und die Massenlöschung. Auch die Tabs für Baum und Sicherung warten.

Was das Fenster ausgraut, wird auch abgelehnt, wenn ein Befehl, ein Agent oder eine Erweiterung darum bittet: Marginalia nennt, was im Weg ist (*Dies wird verwendet: …*).

**Das Buch zu verlassen stoppt die Generierung.** Die Schaltfläche zum Verlassen fragt zuerst nach; *Ja* entspricht **Stopp**: Was bereits eingegangen ist, bleibt erhalten. Das Schließen des Browser-Tabs tut dasselbe, und der Browser fragt nach, bevor der Tab geschlossen oder neu geladen wird, während ein Abschnitt generiert wird. Ein kurzer Netzwerkausfall oder ein für eine Minute zugeklappter Laptop gilt nicht als Verlassen: Die Seite kommt zurück und zeigt, was inzwischen eingegangen ist.

Wenn das Modell einen Fehler meldet (Server nicht erreichbar, falscher API-Schlüssel, Kontext zu lang ...), wird der Fehler angezeigt.
Wenn noch kein Text eingegangen ist, bleibt die Geschichte unverändert: Ein neuer Abschnitt oder Swipe wird entfernt, und ein neu generierter Abschnitt erhält seinen
vorherigen Text zurück. Text, der vor dem Fehler bereits eingegangen ist, bleibt erhalten, wie bei **Stopp**. Beheben Sie das Problem und generieren Sie erneut.

Wie die Felder in den Prompt gelangen, legt der [Benutzer-Prompt](prompts.md#user-prompt) des Buchs fest. Mit dem Standard-Benutzer-Prompt erhält das Modell sie als *Aktueller Ort & Zeit*, *Point-of-View-Figur*, *Anwesende Figuren / Zustand* und *Handlungsvorgaben für den nächsten Abschnitt*.

!!!
Der Text wird in Markdown geschrieben: `*italics*`, `**bold**`, `# Heading` und so weiter werden formatiert angezeigt. Ein Abschnitt mit einer
Überschrift `#` ist ein Kapitel für die Erweiterung [Kapitelmarkierung](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Anweisungen entwerfen

Wissen Sie nicht, was als Nächstes geschehen soll? **Anweisungen entwerfen** unter dem Feld *Anweisungen* des Panels lässt das Modell die Anweisungen an Ihrer Stelle schreiben, so wie Sie es tun würden. Es öffnet sich ein Fenster mit:

| | |
|---|---|
| **Vorgabe** | Worum der Abschnitt gehen soll: eine grobe Idee, eine Richtung („sie stellt ihn wegen des Briefes zur Rede“) oder gar nichts, dann führt das Modell die Geschichte dorthin, wohin sie führt. Anfangs steht hier, was Sie bereits in *Anweisungen* eingetragen haben. |
| **Plan der Geschichte** | Nur wenn etwas einen liefert. Die [Bogenübersicht](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) tut das mit den Daten des aktuellen Kapitels. Nur lesbar; das Modell folgt ihm. |

**Anweisungen entwerfen** startet das Modell und streamt die Antwort in **Entworfene Anweisungen**, die Sie ändern oder leeren können, bevor Sie weitergehen; **Erneut entwerfen** versucht es noch einmal, **Stopp** behält, was bereits eingegangen ist. Nichts wird geändert, bis Sie auf **Als Anweisungen verwenden** klicken: Der Text ersetzt dann die *Anweisungen* des Panels (die anderen Felder bleiben unberührt), und das Panel öffnet sich erneut, bereit für **Generieren**.

Das Modell erhält die bisherige Geschichte (die ältere Geschichte als [Zusammenfassungen](summaries.md), wie bei einer Generierung), den Schauplatz der Szene, die POV-Figur und die anwesenden Figuren, die im Panel stehen, das Lore des letzten Abschnitts, Ihre letzten Anweisungen als Beispiele dafür, wie Sie sie schreiben, Ihre Vorgabe und den Plan. Es sieht nicht Ihre Lorebuch-Einträge, die ein neuer Zug aktivieren würde. Die Anfrage ist der [Impersonate-Prompt](prompts.md#impersonate-prompt). Das Modell ist das des Buchs; Entwerfen verbraucht Tokens wie jede andere Anfrage.

![Entwerfen der Anweisungen für den nächsten Zug](../../images/impersonate-dialog.png)

Dasselbe leistet der Befehl [`/impersonate`](../commands/index.md), der den Text zurückgibt. [Agenten](../agents.md) erhalten ihn nicht; sie schreiben die Geschichte mit eigenen Anweisungen.

<a id="the-settings-menu"></a>

## Das Einstellungsmenü

Das Einstellungsmenü (⚙) in der unteren Leiste fasst zusammen, was auf die ganze Geschichte wirkt. **Suchen und Ersetzen** steht oben; der Rest liegt in Untermenüs
(auch auf dem Telefon, außer den nur für den Desktop gedachten Einträgen):

| Untermenü | Einträge |
|---|---|
| **Darstellung** | **Stile für Text ändern**, **Generierten Text hervorheben** und **Anmerkungen am Rand** (nur Desktop, der letzte) |
| **Analyse** | **Einen Bereich von Abschnitten mit KI kommentieren**, **Zusammenfassungen erstellen**, **Lore-Notizen mit einem Agenten anfertigen**, **Automatische Anmerkungen**, **Tokens des Zweigs**, **Lore-Nutzung**, **Statistik** |
| **Import & Export** | **Importieren**, **Geschichte exportieren** (und die Einträge von Erweiterungen, die importieren oder exportieren, etwa **Anweisungen exportieren**) |
| **Bereinigung** | **Verlauf des Buchs löschen**, **Papierkorb des Verlaufs leeren**, **Massenlöschung** |
| **Layout** (nur Desktop) | **Layout als meinen Standard speichern**, **Standardlayout wiederherstellen** |

Einträge, die [Erweiterungen](../extensions/index.md) mitbringen, stehen im passenden Untermenü oder am Ende des Menüs. Die Tasten der Einträge finden Sie unter *Einstellungen → Tastenkürzel*, wo ein Eintrag mit seinem Untermenü benannt ist (*Analyse › Statistik*). Die Abschnitte unten sagen, wo jeder Eintrag zu finden ist.
<a id="the-part-menu"></a>

## Das Menü des Abschnitts

Jeder Abschnitt hat ein Menü (☰) in seiner oberen rechten Ecke. **Bearbeiten**, **Änderungen verwerfen**, **Neu generieren**, **Variante**, **Überarbeiten**, **Verlauf** und **Bilder** befinden sich im
Menü selbst, **Löschen** ist dessen letzter Eintrag, und der Rest steht in vier Untermenüs: **Anmerkungen** (*Abschnitt kommentieren*, *Mit KI kommentieren*), **Zusammenfassung**
(*Zusammenfassung generieren* / *Zusammenfassung anzeigen*, *Zusammenfassung löschen*), **Untersuchen** (*Prompt anzeigen*, *Prompts vergleichen*, *Token-Verbrauch*, *Lore-Nutzung*) und **Verzweigen & Zusammenführen** (*Geschichte verzweigen*,
*Mit dem vorherigen Abschnitt zusammenführen*). Ein Untermenü, das für den Abschnitt leer ist, wird nicht angezeigt. Der Prüfer fügt sein Untermenü **Prüfen** nach *Zusammenfassung* hinzu. Die Einträge:

| Eintrag | |
|---|---|
| **Bearbeiten** | Macht den Text zu einem bearbeitbaren Feld. **Speichern** (derselbe Menüeintrag) speichert ihn. Das Starten einer Generierung oder das Verlassen des Tabs speichert offene Änderungen ebenfalls. |
| **Änderungen verwerfen** | Nur solange der Text bearbeitet wird: schließt das Feld ohne Speichern, der Abschnitt behält den Text, der zuletzt gespeichert wurde. Wenn du den Text geändert hast, wirst du vorher gefragt. |
| **Neu generieren** | Schreibt den letzten Abschnitt erneut und ersetzt dabei seinen Text. Nur beim letzten Abschnitt. |
| **Variante** | Schreibt eine weitere Version des letzten Abschnitts und behält die alte als eigenen Zweig. Nur beim letzten Abschnitt. Siehe [Verzweigungen & Story-Baum](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Überarbeiten** | Lässt das Modell diesen Abschnitt anhand deiner Anweisungen neu schreiben, bei jedem Abschnitt, siehe [unten](#redrafting-a-part). Nicht während der Text bearbeitet wird. |
| **Verlauf (n)** | Die [Textversionen](version-history.md) des Abschnitts mit der Anzahl der Versionen: vergleichen, eine wiederherstellen, eine benennen. Nicht während der Text bearbeitet wird. |
| **Abschnitt kommentieren** | Erstellt eine [Anmerkung](annotations.md#making-an-annotation) zum ganzen Abschnitt. Um einzelne Wörter zu kommentieren, markiere sie und klicke mit der rechten Maustaste (**Auswahl kommentieren**). |
| **Mit KI kommentieren** | Lässt ein Modell den Abschnitt [prüfen](annotations.md#letting-a-model-annotate) und Anmerkungen vorschlagen. Nicht während der Text bearbeitet wird. Für mehrere Abschnitte auf einmal verwende **Einen Bereich von Abschnitten mit KI kommentieren** im Einstellungsmenü (⚙), und für jeden neuen Abschnitt **Automatische Anmerkungen** (siehe [Anmerkungen](annotations.md#annotating-every-new-part-automatically)). |
| **Geschichte verzweigen** | Startet einen neuen Zweig ab diesem Abschnitt, siehe [Verzweigungen & Story-Baum](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Mit dem vorherigen Abschnitt zusammenführen** | Fügt den Text dieses Abschnitts an den davorliegenden an, siehe [unten](#merging-a-part-into-the-previous-one). Nicht beim ersten Abschnitt. |
| **Prompt anzeigen** | Zeigt den genauen Prompt, der für diesen Abschnitt an das Modell gesendet wurde. |
| **Token-Verbrauch** | Zeigt, wie der Kontext des Modells für diesen Abschnitt genutzt wurde: ein Diagramm der Teile des Prompts (Anweisungen des Modells, Lore, Zusammenfassungen, bisherige Geschichte, deine Anweisungen, für die Antwort reservierter Platz) und die Größe jeder an das Modell gesendeten Nachricht. Wenn ein Teil der Geschichte nicht hineinpasste, steht dort, wie viele Abschnitte weggelassen wurden. Abschnitte, die geschrieben wurden, bevor dies erfasst wurde, haben kein Diagramm. |
| **Lore-Nutzung** | Listet die Einträge des [Lorebuchs](../lorebooks/index.md) auf, die in den Prompt dieses Abschnitts eingeflossen sind, mit den Tokens, die jeder gekostet hat, und dem, was ihn gefunden hat (der Prompt oder der Inhalt eines anderen Eintrags, wenn das Lorebuch tiefer sucht). Abschnitte, die von Hand eingegeben, vor dieser Erfassung geschrieben oder für ein Buch ohne Lorebuch geschrieben wurden, melden, dass nichts aufgezeichnet wurde. Für das ganze Buch verwende **Lore-Nutzung** im Einstellungsmenü (⚙), siehe [unten](#which-lore-entries-were-used). |
| **Bilder** | Hängt Bilder an den Abschnitt an, siehe [Bilder](#images). |
| **Zusammenfassung generieren** / **Zusammenfassung anzeigen** | Fasst die Geschichte bis zu diesem Abschnitt zusammen oder zeigt die Zusammenfassung an, siehe [Zusammenfassungen](summaries.md). |
| **Zusammenfassung löschen** | Entfernt die Zusammenfassung dieses Abschnitts. Bei einer [Metazusammenfassung](summaries.md#meta-summaries) wird gefragt, ob sie aufgelöst oder ganz gelöscht werden soll. |
| **Löschen** | Löscht den Abschnitt, siehe [unten](#deleting-a-part). |

*Neu generieren* und *Variante* verwenden die Zugdetails des letzten Abschnitts (siehe [unten](#turn-details)). Um zu ändern, was
passieren soll, bearbeite sie zuerst.

![Das Menü eines Abschnitts mit geöffnetem Untersuchen-Untermenü](../../images/story-part-menu.png)

![Token-Verbrauch eines Abschnitts](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadaten des Abschnitts

Neben jedem Abschnitt:

| | |
|---|---|
| **Reihenfolge #n \| DB-ID: m** | Die Position des Abschnitts im Zweig und seine ID in der Datenbank. |
| **Modell** | Der Inferenzanbieter, der den Abschnitt geschrieben hat. |
| **Tokens: x (y)** | `x` Tokens Text im Abschnitt, `y` Tokens im Prompt, der ihn erzeugt hat. |
| **Wörter** | Wörter im Abschnitt. |
| **Zugdetails** | Die Anweisungen, aus denen der Abschnitt geschrieben wurde. |

<a id="turn-details"></a>

### Zugdetails

**Zugdetails** öffnet den *Schauplatz der Szene*, die *POV-Figur*, die *Anwesenden Figuren* und die *Anweisungen*, aus denen der Abschnitt
geschrieben wurde. Beim letzten Abschnitt lassen sie sich ändern (Änderungen werden sofort gespeichert) und werden von *Neu generieren* und
*Variante* verwendet. Bei früheren Abschnitten sind sie nur lesbar.

![Zugdetails eines Abschnitts](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Den Text bearbeiten

Generierter Text gehört dir und darf geändert werden. Wähle im Menü des Abschnitts **Bearbeiten**, ändere den Text und wähle **Speichern**. Bearbeitete
Abschnitte werden so an das Modell gesendet, wie du sie bearbeitet hast.

Wenn du einen Abschnitt bearbeitest, den eine [Zusammenfassung](summaries.md) abdeckt, wird diese Zusammenfassung veraltet; Marginalia bemerkt das bei der nächsten
Generierung und verwirft die Zusammenfassung. Damit das nicht überraschend geschieht, prüft **Speichern** den neuen Text vorher: Würde er Zusammenfassungen
veralten lassen, zeigt ein Fenster, wie viele es betrifft, und bietet **Zurück zur Bearbeitung** (nichts wird gespeichert, der Editor bleibt mit deinem Text offen),
**Trotzdem speichern** (die Zusammenfassungen werden veraltet) und **Speichern und Zusammenfassungen neu validieren** (der Text wird gespeichert und die Zusammenfassungen bleiben gültig, für eine
Änderung, die nichts an ihrem Inhalt ändert, etwa einen Tippfehler). Die Prüfung erfolgt bei **Speichern** (im Menü, mit dem Tastenkürzel und mit dem Button des Editors); die automatischen
Speicherungen, etwa wenn du bei geöffnetem Editor eine Generierung startest, fragen nicht nach.

<a id="keyboard-shortcuts"></a>

## Tastenkürzel

Auf einem Computer (das [Layout für das Telefon](../phone.md) hat keine) funktioniert die Schreibschleife mit der Tastatur:

| Taste | |
|---|---|
| `+` | Öffnet das Feld *Anweisungen für den neuen Zug* mit dem Cursor in *Anweisungen*. |
| `Ctrl+Enter` | Generiert aus dem geöffneten Feld. `Esc` schließt es; die Tastatur geht zurück zur Geschichte. |
| `Ctrl+.` | Stoppt die laufende Generierung. |
| `s` / `r` | **Variante** / **Neu generieren** für den letzten Abschnitt. |
| `e` | Öffnet den Editor des Abschnitts, der auf dem Bildschirm am stärksten sichtbar ist (ohne einen solchen der letzte Abschnitt). |
| `Ctrl+S` | Speichert den Abschnitt, der gerade bearbeitet wird (auch wenn der Cursor in seinem Text steht). |
| `/` | Setzt den Cursor in die Befehlsleiste (zuerst wird der Tab der Geschichte angezeigt). |
| `Ctrl+Shift+F` | Öffnet den Tab *Gliederung* und setzt den Cursor in die Suche des Zweigs (`Enter` / `Shift+Enter` springen zum nächsten / vorherigen Treffer). |
| `Ctrl+Shift+H` | Öffnet das Fenster *Suchen und Ersetzen* des Buches. |
| `a` | **Auswahl kommentieren**: erstellt eine Anmerkung zum Text, der in einem Abschnitt markiert ist. |
| `Alt+PageDown` / `Alt+PageUp` | Zeigt den nächsten / vorherigen Tab und durchläuft dabei die Tabs aller Blätter in der Reihenfolge des Layouts und wieder von vorn. |

Eine Taste ohne `Ctrl`, `Alt` oder `Meta` (etwa `+`, `s`, `r`, `e`, `a`, `/`) funktioniert nur, wenn kein Textfeld den Fokus hat, damit sie
das Tippen nie stört. Die Tastenkürzel wirken im Tab der Geschichte des offenen Buches und nicht, wenn darüber ein anderes Fenster offen ist. Die Buttons zeigen ihre Taste im Tooltip, und das Menü des letzten Abschnitts zeigt sie nach *Bearbeiten*, *Neu generieren* und *Variante*. Auf einem Mac funktioniert `Cmd` dort, wo eine Taste `Ctrl` angibt (sofern du diese `Cmd`-Taste nicht einer anderen Aktion gegeben hast). Du kannst jede Taste unter
[Einstellungen → Tastenkürzel](../account.md#keyboard-shortcuts) ändern.
<a id="deleting-a-part"></a>

## Einen Abschnitt löschen

**Löschen** entfernt den Abschnitt aus der Geschichte. Die Abschnitte, die auf ihn folgten, bleiben erhalten und setzen an dem Abschnitt davor fort, sodass
das Löschen eines Abschnitts in der Mitte der Geschichte ihn einfach herausnimmt. Wird der letzte Abschnitt gelöscht, wird der Abschnitt davor
das neue Ende des Zweigs.

<a id="deleting-many-parts-at-once"></a>

### Mehrere Abschnitte auf einmal löschen

Das Einstellungsmenü (⚙) enthält **Massenlöschung**. Schaltet man sie ein, erscheint links neben jedem Abschnitt ein Kontrollkästchen, und unter der Geschichte wird eine Leiste mit der Anzahl der markierten Abschnitte und einer Schaltfläche **Löschen** angezeigt. Markiere die Abschnitte, die du entfernen möchtest (die Markierungen bleiben beim Scrollen erhalten, die Geschichte lädt nur einen Ausschnitt der Abschnitte); **Umschalt + Klick** auf ein Kontrollkästchen markiert (oder hebt die Markierung auf) alle Abschnitte zwischen diesem und dem zuvor angeklickten. Ein markierter Abschnitt wird rot eingefärbt. Drücke dann **Löschen**: Nach einer Bestätigung werden die Abschnitte nacheinander gelöscht, während eine Fortschrittsleiste angezeigt wird, die sich nicht abbrechen lässt. Sie werden so gelöscht, wie ein einzelner Abschnitt gelöscht wird: Die Abschnitte, die auf einen gelöschten Abschnitt folgten, setzen an dem Abschnitt davor fort, und die gelöschten Abschnitte landen im Papierkorb. Danach wird die Geschichte erneut angezeigt und die Massenlöschung ist ausgeschaltet. **Abbrechen** in der Leiste oder erneutes Wählen von **Massenlöschung** schaltet sie aus, ohne etwas zu löschen. Das Starten einer Generierung schaltet sie ebenfalls aus. Ein markierter Abschnitt, mit dem ein Ausschnitt oder eine Übersetzung arbeitet, bleibt in der Geschichte erhalten, und Marginalia teilt mit, wer ihn verwendet.

![Massenlöschung mit einigen markierten Abschnitten](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Teil neu überarbeiten

**Neu generieren** und **Variante** arbeiten nur am letzten Teil und schreiben ihn von Grund auf neu. **Überarbeiten** arbeitet an jedem Teil, auch an einem älteren in der Mitte der Geschichte, und behält, was du nicht zu ändern verlangst. Wähle **Überarbeiten** im Menü des Teils:

1. Schreibe in **Anweisungen für die Überarbeitung**, was sich ändern soll: *mehr Dialog zwischen den beiden hinzufügen*, *den Kampf länger machen*, *die Erklärung der Karte streichen*, *das Ende so lassen, wie es ist*. Leere Anweisungen schreiben den Teil mit denselben Ereignissen neu.
2. Wähle **Länge**, wenn der Teil **Kürzer** oder **Länger** werden soll (der Standard ist **Gleiche Länge**): Ein Satz, der das verlangt, wird nach deinen Anweisungen angefügt (siehe [Redraft Shorter Prompt and Redraft Longer Prompt](prompts.md#redraft-shorter-and-longer)). Wähle **Überarbeiten**. Der Text läuft in **Überarbeiteter Text** ein; **Stopp** beendet ihn vorzeitig. Wähle **Erneut überarbeiten** für einen weiteren Versuch, gern mit anderen Anweisungen. **Unterschiede** zeigt, was sich gegenüber dem Teil geändert hat, Wort für Wort. Hat der Teil offene [Kommentare](annotations.md), werden **Die offenen Kommentare des Teils bearbeiten** (zuerst angehakt) dem Modell ebenfalls übergeben, und **Anwenden** markiert sie als erledigt.
3. Der überarbeitete Text ist ein Feld, das du noch bearbeiten kannst (sein Eckensymbol vergrößert es, bis der ganze Text sichtbar ist), und **Unterschiede** folgt deinen Änderungen. **Anwenden** ersetzt den Text des Teils durch ihn; **Schließen** verwirft ihn. Vor **Anwenden** ändert sich nichts im Buch.

![Das Überarbeitungsfenster: die Anweisungen, der neue Text und die Unterschiede](../../images/redraft-dialog.png)

Das Modell bekommt die [Überarbeitungs-Prompts](prompts.md#redraft-prompt): den Teil, deine Anweisungen, die Geschichte vor dem Teil (die ältere Geschichte als [Zusammenfassungen](summaries.md), wo es gültige gibt, den Rest als Text, der neueste Teil zuerst, wenn nicht alles hineinpasst), so viel der Geschichte nach dem Teil, wie hineinpasst (bis zu einem Viertel des Platzes), das Wissen, das die Lorebooks dem Teil jetzt geben (für den Zug des Teils neu berechnet, sodass eine Änderung an einem Eintrag die Überarbeitung erreicht; das Wissen, mit dem der Teil geschrieben wurde, wird nur verwendet, wenn das Buch kein Lorebook hat), die Erzählperspektive, Zeitform und den Stil des Buches sowie Schauplatz, Figur der Perspektive, anwesende Figuren und Anweisungen des Zuges, aus dem der Teil stammt. Die Geschichte nach dem Teil ist dabei, damit der neue Text trotzdem in sie hinüberführt: Ereignisse, auf die die folgenden Teile zählen, sollten bleiben.

Der Teil behält seinen Platz, seine Zusammenfassung, seine Bilder und wie er erzeugt wurde; der Text, seine Wörter und Tokens werden ersetzt, und der Teil gilt als bearbeitet. Das Anwenden der Überarbeitung ist eine Bearbeitung wie **Speichern** im Editor, daher werden [Zusammenfassungen](summaries.md), die den alten Text beschreiben, veraltet, und Marginalia sagt, wie viele. Eine Zusammenfassung am Teil selbst gehört dazu.

Beschreibt eine Zusammenfassung den Teil (der Teil hat eine, oder ein neuerer Teil des Zweigs hat eine), zeigt das Fenster das Kontrollkästchen **Die Zusammenfassungen prüfen, die diese Überarbeitung veralten lässt**, zunächst nicht angehakt. Hake es an, wenn die Zusammenfassungen noch das Richtige sagen (die Überarbeitung hat die Formulierung geändert, nicht, was geschieht): **Anwenden** berechnet ihren Hash dann neu, und sie bleiben gültig. Lass es nicht angehakt, wenn sich die Ereignisse geändert haben, und erstelle die Zusammenfassungen neu.

Das Fenster blockiert die Geschichte nicht, siehe [Mit dem Fenster nebenher arbeiten](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Mit den Fenstern für Überarbeitung und Kommentare nebenher arbeiten

Auf einem Computer stehen die Fenster **Überarbeiten** und **Mit KI kommentieren** rechts neben der Geschichte und lassen sie benutzbar: Du kannst lesen, scrollen und die Geschichte bearbeiten, während das Modell schreibt. Ziehe ein Fenster an seinem Titel, um es zu verschieben, oder an seiner Kante, um es in der Größe zu ändern. Der Pfeil (⌄) im Titel klappt ein Fenster in das [Laufpanel](#the-panel-of-the-runs) unten links auf der Seite ein: Das Modell arbeitet weiter, **Öffnen** (oder ein Klick auf den Eintrag) holt das Fenster zurück, und **Stopp** stoppt das Modell (ist es fertig, schließt **Schließen** das Fenster). Auf einem Telefon verdecken die Fenster die Geschichte trotzdem, lassen sich aber genauso einklappen.

![Das Überarbeitungsfenster neben der Geschichte, während das Modell schreibt, und der Lauf im Panel unten links](../../images/redraft-beside-story.png)

**Schließen und Stopp sind zwei Dinge.** Das eigene **Stopp** des Fensters stoppt das Modell, und das Fenster bleibt mit dem, was angekommen ist. **Schließen**, während das Modell arbeitet, stoppt es nicht: Das Fenster klappt in das Panel ein, und das Modell arbeitet in seiner Warteschlange weiter; seine Antwort steht im Fenster, wenn du es wieder öffnest. Schließe ein Fenster, dessen Modell fertig ist, oder **Schließen** es im Panel, dann ist es weg; das Schließen des Buches oder des Browser-Tabs stoppt auch ein eingeklapptes Fenster. Dasselbe gilt für die anderen Fenster, die auf ein Modell warten: eine **Zusammenfassung** (und *Zusammenfassungen erstellen*, *Alle Zusammenfassungen neu schreiben*), den [Prüfer](../extensions/reviewer.md), die Buchbeschreibung, die Generatoren für Lore und Gliederung, die Bild- und Manga-Generatoren und den Personen-Autor der Beta-Leser. Die Fenster *Studio* und *Konvertieren* des Hörbuchs blockieren die Seite ebenfalls nicht; sie zu schließen hat die Konvertierung nie gestoppt.

Ist ein Fenster offen (oder eingeklappt) an einem Teil, zeigt der Teil neben seinem Menü, was er als Nächstes tut (*Wird überarbeitet · Das Buch*), und **Neu generieren**, **Variante**, **Überarbeiten**, **Mit KI kommentieren**, **Mit vorherigem zusammenführen** und **Löschen** sind für diesen Teil deaktiviert, damit das Fenster seinen Teil nicht verliert. Den Teil zu bearbeiten bleibt möglich. Das Schließen des Buches schließt seine Fenster und stoppt ihre Modelle. Ein Teil, an dem ein [Übersetzer](../extensions/real-language-translator.md) arbeitet, zeigt auf dieselbe Weise *Die Rede des Teils wird übersetzt ...*, und zwei Übersetzer arbeiten nie zugleich an einem Teil: Der zweite sagt, dass der Teil in Gebrauch ist.

- Wenn du eine Überarbeitung des ganzen Teils **anwendest** und der Teil geändert wurde, nachdem das Fenster geöffnet wurde (du hast ihn inzwischen bearbeitet), fragt Marginalia zuerst nach: **Unterschiede** vergleicht dann den Teil, wie er jetzt ist, mit der Überarbeitung, sodass du siehst, was das Anwenden ersetzen würde; wähle **Trotzdem anwenden** oder **Abbrechen**. Eine Überarbeitung, die sich [selbst anwendet](annotations.md#annotating-every-new-part-automatically), tut das nicht, wenn der Teil geändert wurde: Das Fenster wartet dann auf dich.
- Wurde der Teil gelöscht, bevor du die Überarbeitung **anwendest** oder die Kommentare hinzufügst, sagt das Fenster es, schreibt nichts und schließt sich.

<a id="the-panel-of-the-runs"></a>

### Das Laufpanel

Alles, was gerade mit einem Modell an deinen Büchern arbeitet, steht unten links auf jeder Seite: eine Generierung, eine Überarbeitung oder ein Kommentar, eine Übersetzung durch den [Real Language](../extensions/real-language-translator.md)- oder [Conlang](../extensions/conlang-translator.md)-Übersetzer, eine Neufassung der Zusammenfassungen, das Lesen der Geschichte durch den Knowledge Tracker, eine Konvertierung des [Hörbuchs](../extensions/audiobook.md), ein Befehl der Befehlsleiste, der etwas dauert (*Anweisungen entwerfen*, eine Zusammenfassung ...: Sein Fenster steht seitlich und klappt wie die anderen ein). Die Fenster, in denen du auf eine Antwort wartest, stehen ebenfalls darin, solange das Modell antwortet: **Einspringen**, der Hilfe-Assistent, eine schnelle Übersetzung, der Conlang-Designer, ein Chat von Side Query, Beta-Leser oder der Handlungsassistent. Es listet deine Läufe aus **allen deinen offenen Seiten** (anderer Tab, Telefon), nicht nur die, die auf dieser Seite gestartet wurden. Läuft nichts, wird nichts gezeigt.

![Das Laufpanel geöffnet: die eingeklappte Überarbeitung darin, mit dem Ende des Modelltextes, Öffnen und Stopp](../../images/runs-panel.png)

- **Geschlossen** ist es eine kleine Leiste: der erste Lauf, wie viele weitere (*Die Rede des Teils wird übersetzt ... · 2 weitere*) und ein wanderner Balken, während ein Modell arbeitet. Klicke darauf, um die Liste zu öffnen; sie öffnet sich von selbst, wenn du ein Fenster hineinklappst.
- **Offen** zeigt jeder Lauf, was er tut, das Buch, wie lange er läuft, worauf er wartet und das Ende dessen, was das Modell schreibt:
  - *Warten auf das Modell: 2 Anfragen vor dieser* , wenn das Modell mit anderen Anfragen beschäftigt ist (siehe *Verbindungen* unter [Inferenzanbieter](../inference-providers.md));
  - *Das Modell denkt nach ...* oder *Das Modell schreibt ...*, darunter das Ende seines Denkens oder seiner Antwort;
  - *Wartet auf dich*, wenn der Lauf auf eine Entscheidung von dir wartet (eine Überarbeitung zum Anwenden, ein Vorschlag des Knowledge Trackers zum Prüfen, die Frage des Prompt Inspectors vor einer Generierung, ein Befehl des Handlungsassistenten, den du erlauben musst).
- **Stopp** stoppt den Lauf, wo immer er gestartet wurde. **Öffnen** oder ein Klick auf den Eintrag zeigt ein eingeklapptes Fenster wieder (nur auf der Seite, auf der es geöffnet wurde) oder den Lauf selbst; es heißt **Prüfen**, wenn der Lauf auf dich wartet. Ein Lauf des Knowledge Trackers öffnet sein Buch auf dieser Seite (wenn hier kein anderes Buch offen ist; sonst wirst du gebeten, zuerst dieses zu schließen), wechselt zum Reiter Knowledge und zeigt die Prüfung. **Schließen** schließt ein Fenster endgültig (und stoppt sein Modell).
- Auf einem Computer kannst du das Panel an seinem Griff (⋮ links) verschieben; es bleibt, wo du es lässt, in diesem Browser. In der unteren Hälfte der Seite wächst es beim Öffnen nach oben, ebenso in seiner Ecke, und es bleibt immer innerhalb der Seite. Liegt es dort, wo der runde Knopf des [Hilfe-Assistenten](../administration/help-assistant.md) steht (bei einem Buch liegen beide unten links), weicht der Knopf zur Seite und kehrt zurück, wenn der Platz frei ist.

Endet ein Lauf, den du gestartet hast, nachdem seine Seite geschlossen wurde, sendet eine andere offene Seite von dir seine [Benachrichtigung](../account.md#notifications) (bei Läufen, die eine haben, etwa der Neufassung der Zusammenfassungen).

<a id="redrafting-only-a-passage"></a>

### Nur eine Passage überarbeiten

Um ein paar Sätze neu zu schreiben und den Rest des Teils unberührt zu lassen, markiere sie im Text des Teils und klicke mit der **rechten Maustaste** (auf einem Telefon ein langes Drücken): Das Menü enthält **Auswahl überarbeiten**. (Das Menü ☰ des Teils wird dafür nicht angeboten, denn sein Öffnen kann in manchen Browsern die Markierung verlieren.) Das Fenster funktioniert wie oben, mit zwei Unterschieden: Es zeigt den **Ausgewählten Text**, und die Überarbeitung ist nur der Text, der ihn ersetzt. Das Modell bekommt den ganzen Teil mit markierter Auswahl sowie die Geschichte und das Wissen drumherum, damit sich die neue Passage an die Sätze davor und danach fügt. **Unterschiede** vergleicht die Auswahl mit dem überarbeiteten Text; **Anwenden** setzt den überarbeiteten Text an die Stelle der Auswahl und behält den Rest des Teils, einschließlich des Leerzeichens oder Zeilenumbruchs an den Rändern der Auswahl. Bei offenen [Kommentaren](annotations.md) bietet das Kontrollkästchen nur die an, deren Passage sich mit der Auswahl überschneidet. Wurde der Teil geändert, nachdem du den Text markiert hast (in einem anderen Fenster), verweigert **Anwenden** es, und du markierst ihn erneut. Während der Teil bearbeitet wird, gilt die Auswahl im Editor, und die Bearbeitung wird vorher gespeichert.

<a id="what-follows-a-generated-part"></a>

### Was auf einen erzeugten Teil folgt

Ist ein Teil, den du erzeugt hast (von der Seite oder aus der Befehlsleiste), fertig, kann das Buch von selbst noch mehr damit tun, in dieser Reihenfolge:

1. **Kommentieren**: Der [automatische Kommentar](annotations.md#annotating-every-new-part-automatically) prüft den Teil.
2. **Überarbeiten**: Der Teil wird neu geschrieben, um die Notizen aufzugreifen.
3. **Übersetzen**: Die [Real-Language](../extensions/real-language-translator.md)- und [Conlang](../extensions/conlang-translator.md)-Übersetzer übersetzen seine Reden.
4. **Knowledge**: Der [Knowledge Tracker](../extensions/knowledge-tracker.md) liest ihn.
5. **Audio**: Das [Hörbuch](../extensions/audiobook.md) macht daraus Audio.

Nur die Schritte, die das Buch eingeschaltet hat, laufen. Sie laufen auf dem Server, sodass die Seite geschlossen werden kann, und sie bilden einen Eintrag im [Laufpanel](#the-panel-of-the-runs): *Nach Teil 14: Kommentieren ✓ · Überarbeiten … · Übersetzen*, mit dem Text des Modells und einem **Stopp**. Was den Text ändert, kommt zuerst, damit der Tracker und das Audio den endgültigen Text bekommen. Ein Schritt, der auf dich wartet (Notizen oder eine Überarbeitung zum Auswählen), öffnet sein Fenster, und der Eintrag hat **Prüfen**, um es in einer anderen Seite zu öffnen.

- **Stopp**, das Schließen eines Fensters, das auf dich wartet, oder ein fehlgeschlagener Schritt beendet die Kette dort: Was schon getan wurde, bleibt, der Rest läuft nicht, und eine Meldung nennt, was offen ist, damit du es von Hand erledigst (jeder Schritt hat seine eigene Aktion am Teil).
- Während die **Überarbeitung** läuft, kann der nächste Teil nicht erzeugt werden (du wirst gesagt bekommen, durch welchen Lauf); während die anderen Schritte laufen, geht es.
- Der Teil kann nicht gelöscht werden, bevor sein Text endgültig ist; erzeugt man ihn erneut, endet, was auf seine frühere Fassung folgte.
- Ein [Agent](../agents.md) führt diese Schritte über seine eigenen Befehle aus: Auf einen Teil, den ein Agent erzeugt hat, folgt nichts.
<a id="merging-a-part-into-the-previous-one"></a>

## Eine Part in die vorherige zusammenführen

Ein Modell, das mitten im Satz angehalten hat (das Antwortlimit, eine abgebrochene Verbindung), lässt sich nicht zuverlässig dazu bringen, denselben Text „fortzusetzen“. Schreibe stattdessen die nächste Part (*continue from where the last part stopped*) und verbinde beide mit **Merge into previous part** im Menü der zweiten. Es öffnet sich eine Prüfung:

- **Texte verbinden mit**: eine Leerzeile, ein Zeilenumbruch, ein Leerzeichen oder nichts. Vorausgewählt wird anhand des Endes des vorherigen Textes: ein Absatz nach einem Text, der einen Satz beendet hat, ein Leerzeichen nach einem, der abgeschnitten wurde.
- Die **Szenenbeschreibung**, der **POV-Charakter**, die **anwesenden Charaktere** und die **Anweisungen** beider Durchläufe, zusammengeführt in Feldern, die du bearbeiten kannst. Was die beiden Durchläufe am Anfang gemeinsam haben, wird nur einmal übernommen („Write the duel. Keep it short“ und „Write the duel. Add rain“ ergeben den ersten Teil, und „Add rain“ steht in der nächsten Zeile).
- **Daten**: was die beiden Parts speichern, Wert für Wert (zum Beispiel die [Variablen](../templates/macros.md) der Geschichte und die Daten von Erweiterungen), mit der Angabe, ob der Wert gleich, verschieden oder nur auf einem der Parts vorhanden ist. Die zusammengeführte Part behält den Wert der **späteren Part** (dort steht die Geschichte jetzt), sofern du nicht den Wert der vorherigen Part wählst oder ihn weglässt. Gleiche Werte bleiben verborgen, sofern du sie nicht anforderst.

![Die Prüfung, bevor eine Part in die vorherige zusammengeführt wird](../../images/part-merge-dialog.png)

Die vorherige Part behält ihren Platz, ihre Zusammenfassung und die Art, wie sie erzeugt wurde (Prompt, Modell, Zeiten). Sie erhält den verbundenen Text, die verbundene Begründung und die Bilder, und ihre Wörter und Tokens werden neu gezählt. Die zusammengeführte Part wird gelöscht (sie liegt im [Papierkorb](../trash.md)), und was auf sie folgte, geht nun von der vorherigen Part aus. Zusammenfassungen, die beide Parts abdecken, werden veraltet, wie nach einer Bearbeitung. Die [Historie](version-history.md#backups-copies-and-the-trash) der zusammengeführten Part wird entfernt, und die verbleibende Part hat den verbundenen Text als Version **Zusammengeführt**. Eine Part kann nicht zusammengeführt werden, wenn sie die erste Part des Buches ist oder wenn die Part davor weitere Fortsetzungen hat (Varianten der Part). Lösche diese dann oder mache sie zuerst zu einem Zweig.

<a id="images"></a>

## Bilder

Eine Part kann Bilder haben: eine Karte, ein Charakterporträt, eine Illustration. Wähle **Bilder** im Menü der Part, um die Liste der Bilder der Part zu öffnen.

![Der Dialog Bilder einer Part](../../images/images-dialog.png)

- **Bild hinzufügen** (oder eine Datei darauf ziehen) lädt ein PNG-, JPEG-, GIF- oder WebP-Bild mit bis zu 10 MB hoch. WebP-Bilder werden beim Hochladen in PNG umgewandelt, weil Word- und PDF-Dateien kein WebP enthalten können.
- **Bildunterschrift** wird unter dem Bild angezeigt. Sie kann leer bleiben.
- **Position im Abschnitt** ist ein 3x3-Raster. Die Spalte ist die Seite des Textes (links, Mitte, rechts), die Zeile ist die Stelle darin: oben steht das Bild vor dem Text, die Mitte ist mitten im Text, unten steht es nach dem Text. Klicke auf ein Feld, um es auszuwählen.
- **Text umfließt das Bild** lässt den Text um das Bild herumlaufen, statt oberhalb und unterhalb davon. Links oder rechts läuft der Text neben dem Bild. In der mittleren Spalte steht der Text auf beiden Seiten: einige Blöcke des Textes (etwa 400 Zeichen, mindestens zwei Blöcke) an dieser Zeile werden zu zwei Spalten, der erste Teil links vom Bild und der Rest rechts, sodass der Text die linke Spalte hinunterläuft und in der rechten weitergeht. Zwei Bilder an derselben Stelle stehen zwischen den Spalten übereinander. Wenn der Text keinen Platz für zwei Spalten hat (nur ein Absatz oder die Blöcke sind schon vom Band darüber belegt), wird das Bild zu einem eigenen, zentrierten Block.
- **Breite** ist die Breite des Bildes in Prozent der Breite des Textes. **Automatisch** behält die Größe des Bildes bei (nie breiter als der Text; ein Bild mit Text auf beiden Seiten ist 30 %). Ein Bild, neben dem der Text umfließt, ist höchstens 60 % breit, eines mit Text auf beiden Seiten höchstens 40 %, damit der Text etwas Platz behält. Die Höhe folgt den Proportionen des Bildes.
- Die Papierkorb-Schaltfläche entfernt das Bild aus der Part.

![Die Position eines Bildes: in der Mitte, mit dem Text, der es umfließt](../../images/images-position.png)

**Vorschau wie im Viewer** (eine Schaltfläche des Dialogs und derselbe Eintrag im Menü der Part, wenn sie Bilder hat) zeigt die Part so, wie der [Viewer](publishing.md) sie zeigt, mit dem Textstil des Buches: die Bilder dort, wo du sie platziert hast, und einem Schalter für einen breiten oder einen schmalen (Telefon-)Bildschirm. Es verwendet den Text im Editor so, wie er gerade ist, gespeichert oder nicht.

![Die Vorschau: der Text in zwei Spalten mit dem Bild dazwischen](../../images/images-preview.png)

Der Story-Editor selbst verwendet die Position nicht: Er zeigt die Bilder unter dem Text der Part als Liste. Die Position wird vom Viewer und von den [Exporten](export.md#images) genutzt.

Wo im Text ein Bild steht, hängt nicht von Zahlen ab, die mit ihm gespeichert sind: Der Text wird zwischen seinen Blöcken geteilt (Absätze, Listen, Zitate und Codeblöcke werden nie geteilt), sodass die Bilder dort bleiben, wo sie hingehören, wenn du den Text bearbeitest. Die Mitte ist die Stelle zwischen Blöcken, die der Mitte des Textes am nächsten liegt; ein Bild, um das der Text unten umfließt, wird vor den letzten Block gesetzt, damit mindestens dieser Block daneben läuft. Bilder an derselben Stelle bleiben in der Reihenfolge der Liste; solche, die nebeneinander an derselben Stelle liegen (und den Text nicht umfließen lassen), stehen in einer Reihe, wenn sie passen. PDF und Word tun das für Bilder, die eine Breite haben.

Bilder sind **kein Teil des Textes**. Das Modell sieht sie nie, sie stehen nicht im Prompt, zählen nicht als Tokens und verändern die [Zusammenfassungen](summaries.md) nicht. Sie sind auch kein Markdown: Ein in den Text geschriebener `![](...)`-Link ist kein Bild der Part, verwende stattdessen **Bilder**.

Gut zu wissen:

- **Neu generieren** behält die Bilder der Part, auch wenn die Generierung fehlschlägt. **Variante** schreibt eine neue Part (eine neue Version neben der alten), die ohne Bilder beginnt. **Geschichte verzweigen** kopiert die Bilder in die neue Part.
- Bilder gehören zu deinem Konto. Im [Viewer](publishing.md) sehen Leser eines veröffentlichten Buches sie ebenfalls, es sei denn, du entfernst das Häkchen bei **Die Bilder den Lesern zeigen** im Tab *About* des Buches.
- Ein [Backup](backups.md) merkt sich, welche Bilder eine Part hat, aber nicht die Bilddateien. Wenn du ein Backup in ein anderes Konto oder eine andere Installation wiederherstellst, erhalten die Parts keine Bilder.
- Ein Bild aus einer Part zu entfernen, nimmt es nur von der Part herunter. Die Datei bleibt und ist weiterhin unter [Ressourcen](../resources.md) aufgeführt, wo du sie herunterladen, ersetzen oder löschen kannst. Löschst du sie dort, wird sie aus jeder Part entfernt, die sie verwendet. Die Datei selbst bleibt im Datenordner.

<a id="summaries"></a>

## Zusammenfassungen

Die Schaltfläche **Summaries** in der unteren Leiste (links vom Einstellungsmenü ⚙) öffnet die Übersicht der Zusammenfassungen des Zweigs. Dort können sie bearbeitet, gelöscht und zu Meta-Zusammenfassungen zusammengeführt werden. Der Tooltip der Schaltfläche zeigt, wie viele Tokens die verwendeten Zusammenfassungen im Prompt belegen. Siehe [Zusammenfassungen](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Wie viel des Kontexts die Geschichte belegt

Das Einstellungsmenü (⚙) hat **Tokens des Zweigs**. Es zeigt, bevor du etwas generierst, wie viel die Geschichte des aktiven Zweigs vom Prompt belegt: einen Balken für die Geschichte **ohne Zusammenfassungen** (alle Parts) und **mit Zusammenfassungen** (die Zusammenfassungen und die Parts nach der neuesten), sowie eine Tabelle mit den Parts, den Tokens und dem Anteil am **Prompt-Budget** (dem Kontext des Modells des Buches abzüglich des Platzes für die Antwort). Nur Zusammenfassungen, die die Geschichte noch beschreiben, zählen; die, die nicht mehr passen, werden benannt und beim Generieren einer Part verworfen. Das Lore, das System-Template und die Anweisungen des Durchlaufs beanspruchen Platz aus demselben Budget. Ohne ein Modell mit einer Kontextgröße hat die Tabelle keine Anteile.

![Tokens des Zweigs](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Welche Lore-Einträge verwendet wurden

Unter **Lore-Nutzung** im Einstellungsmenü (⚙, **Analyse**) sieht man, welche Einträge der Lorebooks des Buchs die Generierungen verwendet haben und welche nie,
so lässt sich ein Lorebook anhand dessen bereinigen, was es tatsächlich bewirkt, und nicht durch Raten. Jede Generierung speichert, welche Einträge in ihren Prompt
eingeflossen sind (und die Tokens jedes Eintrags); das Fenster summiert das über die Abschnitte:

- **Abschnitte** legt fest, was gezählt wird: den **Aktiven Zweig** oder **Alle Zweige**, also jeden Zweig und jede Variante des Buchs.
- **Ab Abschnitt** und **Bis Abschnitt** begrenzen den Bereich, nummeriert wie in der Gliederung (der erste Abschnitt eines Zweigs ist 1; bei allen Zweigen ist die Nummer die Tiefe des Abschnitts im Baum).
  Lässt man sie leer, gilt der Anfang bzw. das Ende.
- **Anzeigen** behält die Einträge, die in den gezählten Abschnitten **Nie verwendet**, **Manchmal verwendet** oder **Immer verwendet** waren.

![Das Fenster Lore-Nutzung](../../images/lore-usage.png)

Die Tabelle hat eine Zeile für jeden Eintrag der Lorebooks des Buchs, die meistgenutzten zuerst: ihr Lorebook, ob er nie, manchmal oder immer
verwendet wurde, in wie vielen Abschnitten (der Abschnitte, die einen Eintrag haben, mit Prozentwert), die Tokens, die er in all diesen gekostet hat, und pro Verwendung, sowie der erste und
letzte Abschnitt, der ihn verwendet hat. Ein Klick auf eine Kopfzeile sortiert. Ein Eintrag, der früher verwendet wurde, aber jetzt nicht mehr in den Lorebooks des Buchs
ist (gelöscht, verschoben oder sein Lorebook ist ausgeschaltet oder gehört nicht mehr zum Buch), bleibt in der Liste, markiert als *derzeit nicht im Lorebook*; ein gelöschter Eintrag hat keinen Namen.

Nur Abschnitte, die einen Eintrag haben, werden gezählt: ein von Hand eingetippter Abschnitt, einer, der geschrieben wurde, bevor Marginalia diese Aufzeichnung führte, oder einer, der entstand, als das Buch kein Lorebook hatte, hat keinen Eintrag, und das Fenster sagt, wie viele es sind.
*Immer verwendete* Einträge könnten konstante Einträge sein; *nie verwendete* sind Kandidaten zum Entfernen oder für einen besseren Filter. Mit [**Aktivierung in der Vorschau anzeigen**](../lorebooks/entries.md#preview-activation) im Lorebook-Editor sieht man, warum ein Eintrag im Moment nicht auslöst.

<a id="text-styles"></a>

## Textstile

Das Einstellungsmenü (⚙) in der unteren Leiste hat **Darstellung → Stile für Text ändern**, ein Menü mit einem Eintrag pro Stil, nach Name sortiert.
Wie man einen Stil erstellt, steht im [Leitfaden zu Stilen](styling-guide.md).
Ein Stil bestimmt, wie der Text der Abschnitte aussieht: Schrift, Größe, Farben, Ausrichtung, Überschriften ... Die Anwendung bringt zwei
Stile mit, die sich nicht ändern lassen: **Normal** (das Aussehen der Anwendung) und **Buch** (ein buchähnlicher Lesestil, den der [Viewer](publishing.md) verwendet). Eigene Stile kann man unter [Einstellungen → Stile](../account.md#styles) anlegen.

- Der erste Eintrag, **Standard (...)**, folgt deinem *Standardstil* (dem, der in *Einstellungen → Stile* markiert ist; Normal, wenn du keinen gewählt hast). Ein neues Buch hat keinen eigenen Stil und zeigt das an.
- Wählt man einen anderen Eintrag, wird der Stil **im Buch** gespeichert und überschreibt den Standard nur für dieses Buch; der angehakte Eintrag ist der, den das Buch jetzt hat. **Standard** nimmt die Wahl wieder zurück.
- Ein gelöschter Stil fällt in den Büchern, die ihn verwendet haben, auf deinen Standard zurück.

![Darstellung, Stile für Text ändern: die Stile zur Auswahl](../../images/story-style-menu.png)

Im Stil **Buch** sind Absätze im Blocksatz, und lange Wörter werden automatisch getrennt, mit dem Wörterbuch der **Sprache** des Buchs
(eingestellt auf dem [Tab Info](managing-books.md#the-about-tab)). Die Silbentrennung übernimmt der Browser;
für einige seltene Sprachen gibt es dort kein Wörterbuch, und der Text wird dann einfach nicht getrennt.

Bücher, bei denen früher der alte Schalter *Stile für Text ändern* eingeschaltet war, zeigen weiterhin den Stil Buch, bis du einen anderen Eintrag wählst.

<a id="highlighting-generated-text"></a>

## Generierten Text hervorheben

Das Einstellungsmenü (⚙) hat außerdem den ankreuzbaren Eintrag **Generierten Text hervorheben**. Solange er eingeschaltet ist, werden die Wörter, die ein Modell geschrieben hat, in jedem Abschnitt hervorgehoben, sodass auffällt, was du hinzugefügt oder umgeschrieben hast. Marginalia verfolgt den Text durch die [Versionen des Abschnitts](version-history.md): jede Version wird über die vorherige gelegt, und die Wörter, die sie gemeinsam haben, behalten ihren Urheber, sodass ein Satz, den du in einen generierten Text getippt hast, deiner bleibt, wie oft das Modell auch den Rest umschreibt. Ein Abschnitt, den du nie bearbeitet hast, wird ganz hervorgehoben; ein Abschnitt, den du selbst geschrieben hast, hat nichts hervorgehoben. Was ein Agent geschrieben hat, zählt als Text des Modells. Der Schalter wird nicht gespeichert: Beim erneuten Öffnen der Seite ist er wieder aus.

Ein Abschnitt, dessen [Verlauf ausgeschaltet ist](version-history.md), hat keine Versionen, denen man folgen könnte: Er wird mit dem Text verglichen, den das Modell zuerst generiert hat. Die Hervorhebung funktioniert auch auf dem [Telefon](../phone.md).

Dieselbe Zählung liefert auch das Verhältnis: Der Tab **Info** des Buchs hat **Urheberschaft des Zweigs** mit einer Schaltfläche **Berechnen**, die angibt, wie viel Prozent der Wörter des aktiven Zweigs du geschrieben hast und wie viele ein Modell generiert hat, und [der Export](export.md) kann die Datei mit einer Erklärung dazu abschließen.

![Hervorgehobener generierter Text und das Einstellungsmenü](../../images/highlight-generated.png)

<a id="commands"></a>

## Befehle

Neben dem Menü unter der Geschichte gibt es eine **Befehlszeile**: Tippe `/`, um die Befehle zu sehen, wähle einen aus und drücke **Enter**
(oder die Abspiel-Schaltfläche), um ihn auszuführen. Was ein Befehl zurückgibt, erscheint in einem kleinen Fenster über der Zeile. Siehe
[Befehle](../commands/index.md), wie man Argumente schreibt, was die Hilfe anzeigt und wie man einen Befehl stoppt, der eine Weile braucht. Die Befehlszeile gibt es bisher nur im Tab Geschichte des Desktop-Layouts.
<a id="search-and-replace"></a>

## Suche und Ersetzen

Das Einstellungsmenü (⚙) enthält außerdem **Verlauf des Buchs löschen** und **Papierkorb des Verlaufs leeren**, siehe [Versionsverlauf](version-history.md#deleting-versions).

Das Einstellungsmenü (⚙) enthält außerdem **Suchen und Ersetzen**. Damit wird ein Wort, ein Name oder eine Wendung in der
ganzen Geschichte auf einmal geändert. Das Fenster hat die Felder **Suchen nach** und **Ersetzen durch** sowie diese Optionen:

![Das Einstellungsmenü mit Suchen und Ersetzen](../../images/story-settings-menu.png)

![Das Fenster Suchen und Ersetzen mit eingegebener Suche](../../images/search-replace-dialog.png)

| Option | Standard | Bedeutung |
|---|---|---|
| **Per Regex suchen** | aus | Der Suchtext ist ein [regulärer Ausdruck](../regular-expressions.md), und der Ersatz kann dessen Gruppen verwenden (`$1`). Andernfalls wird der Text so gesucht, wie er eingegeben wurde. |
| **Groß-/Kleinschreibung beachten** | an | Ein Text wird nur in der Schreibweise gefunden, die eingegeben wurde (*John* findet *john* nicht). Aus: Die Schreibweise spielt keine Rolle. Gesperrt, solange **Per Regex suchen** an ist. |
| **In Zusammenfassungen suchen** | an | Die [Zusammenfassungen](summaries.md) werden ebenfalls durchsucht und geändert. |
| **In allen Zweigen suchen** | aus | Alle [Zweige](branches-and-story-tree.md) werden durchsucht, sonst nur der aktive. |
| **Alle Zusammenfassungen neu validieren** | an | Nach dem Ersetzen ist jede [Zusammenfassung](summaries.md) des Buchs wieder gültig, siehe unten. |

Bei einem regulären Ausdruck wird die Schreibweise immer so genommen, wie sie eingegeben wurde. Beginne ihn mit `(?i)`, um sie zu ignorieren. Drücke **Suchen**
(die Schaltfläche rechts neben den Feldern; auf dem Telefon darunter). Die Fortschrittsanzeige zeigt, wie weit die Suche ist, und **Stopp**
daneben unterbricht sie. Eine Suche ändert nichts.

Die Tabelle listet jeden Abschnitt der Geschichte und jede Zusammenfassung mit einem Treffer auf, mit ihrem Typ und ihrer id. Öffne eine Zeile, um die
Treffer darin zu sehen: den Text um den Treffer, so wie er ist, und so, wie er nach dem Ersetzen aussehen wird. Ein Treffer oder ein Ersatz, der zu lang
ist, um angezeigt zu werden, wird als *[gesuchter Text]* bzw. *[Ersatztext]* dargestellt.

![Die Treffer einer Suche, der erste Abschnitt geöffnet](../../images/search-replace-found.png)

Das Drücken von **Enter** in den Feldern **Suchen nach** oder **Ersetzen durch** hat dieselbe Wirkung wie **Suchen**.

Ist die Suche abgeschlossen, sind die Felder und Optionen gesperrt, und die Schaltfläche **Ersetzen** ist aktiv. **Erneut suchen** vergisst,
was gefunden wurde, und entsperrt das Fenster, damit du die Suche ändern kannst. **Ersetzen** ändert die Geschichte: Das Fenster ist dann
vollständig eingefroren (selbst **Stopp** funktioniert nicht), bis der Vorgang fertig ist, und danach ist es wieder leer. Ersetzte Abschnitte
werden als bearbeitet markiert, als hättest du sie selbst bearbeitet.

Ein Ersetzen im Text, den eine [Zusammenfassung](summaries.md#when-summaries-become-outdated) beschreibt, würde die Zusammenfassung veralten lassen.
Ist **Alle Zusammenfassungen neu validieren** an, berechnet Marginalia den Prüfwert jeder Zusammenfassung des Buchs neu (auch die aller Zweige, auch
die, die das Ersetzen nicht berührt hat, und die, die schon vorher veraltet waren), sodass danach alle gültig sind. Dadurch wird auch eine veraltete
Zusammenfassung gültig, ohne dass ihr Text geprüft wird. Verwende diese Option also nur, wenn du weißt, dass die Zusammenfassungen weiterhin das Richtige sagen.

Ist die Option aus, werden nur die Zusammenfassungen aktualisiert, die von der Suche erfasst werden. Die übrigen (Zusammenfassungen von Zweigen, die nicht
durchsucht werden und ihre früheren Abschnitte mit dem aktiven Zweig teilen; oder alle, wenn Zusammenfassungen nicht durchsucht werden) werden veraltet.
Gibt es solche Zusammenfassungen, zeigt Marginalia an, wie viele es sind, und fragt, ob fortgefahren werden soll. Eine Zusammenfassung, die schon
vorher veraltet war, bleibt es.

![Die Warnung über Zusammenfassungen, die veralten würden](../../images/search-replace-warning.png)

Im [Telefon-Layout](../phone.md) hat dasselbe Fenster die Felder und Schaltflächen untereinander, und die Tabelle hat eine
Spalte, die Typ und id zeigt, darunter den Treffer und den Ersatz.

Die [Übersicht der Zusammenfassungen](summaries.md#search-and-replace-in-the-summaries) hat dasselbe Fenster, jedoch nur für die Zusammenfassungen.

Dieselbe Suche und dasselbe Ersetzen lassen sich über die [Kommandozeile](../commands/index.md#commands-that-come-with-marginalia) mit `/search-replace-parts` ausführen;
dabei wird vor dem Ersetzen nachgefragt.

<a id="importing-into-the-book"></a>

## Abschnitte in das Buch importieren

Das Einstellungsmenü (⚙) hat **Importieren** (unter **Import & Export**). Es ist das Fenster des Reiters Bücher, das aus einer Datei ein neues Buch macht
(siehe [Bücher verwalten](managing-books.md#creating-a-book)), hier jedoch für das Buch, in dem du dich gerade befindest: Die Abschnitte werden stattdessen **zur
Geschichte hinzugefügt**. Die **Quelle** ist eines von: das Zip-Archiv von `.md`-Dateien, ein Dokument (Word, LibreOffice, RTF, Markdown, Klartext, EPUB), das Zip
eines Obsidian-Vaults oder das Zip eines Scrivener-Projekts. Jede Quelle hat dieselben Texte und Optionen wie dort (die Wörter pro Abschnitt und die
Überschriftenebene der Kapitel), und eine Zeile sagt, was der Import erzeugt. Die Chats von SillyTavern und LM Studio werden nicht angeboten: Sie sind
ein Baum aus Zweigen, der in der Mitte einer Geschichte keinen Platz hat.

**Die Abschnitte platzieren** legt fest, wohin sie kommen:

- **Am Ende der Geschichte**: nach dem letzten Abschnitt des aktiven Zweigs (der erste Abschnitt eines leeren Buchs).
- **Vor einem Abschnitt** oder **Nach einem Abschnitt**: Wähle den Abschnitt aus der Liste (die Nummern sind die Stellen im aktiven Zweig, mit dem
  Anfang des Textes). Die Abschnitte werden zwischen zwei Abschnitte eingefügt: Alles, was auf die Stelle folgte, samt allen seinen Zweigen, folgt den
  neuen Abschnitten, und kein Abschnitt wird geändert oder neu erstellt. Das Ende der Geschichte bleibt, wo es war (die Geschichte ist um so viele Abschnitte
  länger). Die Zusammenfassungen, die die Stelle abdecken, werden veraltet, da sie die Geschichte ohne die neuen Abschnitte beschreiben. Das Fenster
  [Zusammenfassungen](summaries.md) zeigt sie an und erstellt sie neu.

![Das Importfenster eines Buchs](../images/book-import-into.png)

Die Abschnitte eines Archivs sind die Dateien in der Reihenfolge ihrer Namen, wobei Zahlen in den Namen als Zahlen verglichen werden (`2.md`
vor `10.md`), auch in Ordnern des Archivs. Ordner, versteckte Dateien (`.name`), der Ordner `__MACOSX` und Dateien, die keine
`.md` sind, werden übersprungen. Die Datei ist der Text des Abschnitts unverändert; sie wird nicht an das Modell gesendet und nicht verändert. Alle Abschnitte gelten
als von dir geschrieben.

Der Import zeigt eine Fortschrittsanzeige mit der Zahl der fertigen Abschnitte. **Stopp** beendet ihn nach dem Abschnitt, der gerade gespeichert wird: Die
bereits hinzugefügten Abschnitte bleiben in der Geschichte (der Import wird nicht rückgängig gemacht), und eine Meldung zeigt, wie viele importiert wurden.

<a id="after-an-import"></a>

### Nach einem Import

Sind die Abschnitte im Buch, bietet ein Fenster **Import abgeschlossen** die zwei Dinge an, die eine lange Geschichte leichter schreiben lassen. (Ein Buch, das
gerade als neues importiert wurde, wird zuerst geöffnet, und das Fenster liegt davor.) Beide sind auch im Einstellungsmenü (⚙, **Analyse**) zu finden,
damit man sie später oder erneut ausführen kann:

- **Zusammenfassungen erstellen...** schreibt die Zusammenfassungen des Importierten, siehe [Zusammenfassungen erstellen](summaries.md#create-summaries). Tu das zuerst: Ein Agent
  sperrt das Buch, während er arbeitet.
- **Lore-Notizen mit einem Agenten erstellen...** startet einen [Agenten](../agents.md), der die Geschichte liest und die Figuren, Orte, Dinge und Begriffe, die er
  findet, als Einträge im Lorebuch des Buchs schreibt, siehe [Lore-Notizen](../agents.md#lore-notes-of-an-imported-book).

**Nicht jetzt** schließt das Fenster und tut keines von beidem.

![Das Fenster nach einem Import](../../images/import-finished.png)

Derselbe Import in ein Lorebuch wird unter [Import & Export](../lorebooks/import-export.md#importing-an-archive-of-text-files) beschrieben.

<a id="exporting-the-story"></a>

## Geschichte exportieren

Das Einstellungsmenü (⚙) hat außerdem **Geschichte exportieren**, das den aktiven Zweig als Text-, Markdown-, HTML-, Word-, PDF- oder EPUB-Datei speichert.
Siehe [Ein Buch exportieren](export.md).

<a id="what-the-model-gets"></a>

## Was das Modell erhält

Für jeden neuen Abschnitt sendet Marginalia:

1. Die **Mastervorlage** als Systemnachricht: Rolle, [Lorebuch](../lorebooks/index.md)-Einträge, Erzählperspektive,
   Zeitform, Stil und [Zusammenfassungen](summaries.md). Der Jailbreak-Prompt des
   [Inferenzanbieters](../inference-providers.md#general-settings), falls aktiviert, kommt zuerst.
2. Die früheren Abschnitte des aktiven Zweigs als die eigenen früheren Antworten des Modells, so viele, wie in das
   [Kontextlimit](../protocols.md#limits) passen, neueste zuerst. Ältere Abschnitte, die nicht passen, werden weggelassen (sofern nicht eine
   [Zusammenfassung](summaries.md) sie abdeckt).
3. Den **Benutzer-Prompt** mit den Anweisungen für den neuen Abschnitt.

*Prompt anzeigen* bei jedem Abschnitt zeigt genau, was gesendet wurde. Siehe [Buch-Prompts](prompts.md) für die Vorlagen.
