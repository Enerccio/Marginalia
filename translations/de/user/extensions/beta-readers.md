---
label: Beta-Leser
order: 22
---

# Beta-Leser

Erfundene Leser und ein Chat mit ihnen über dein Buch. Du beschreibst eine **Persona** (einen Namen, ein Symbol und ein paar Zeilen dazu, wie die Person
liest und worauf sie achtet), und das Modell spielt sie: Es reagiert auf die Teile deiner Geschichte so, wie es dieser Leser täte, allein oder in einer Gruppe,
in der sich die Leser gegenseitig widersprechen. Nichts, was die Leser sagen, wird Teil des Buchs.

![Ein Gruppenchat mit zwei Personas](../../images/plugin-betareaders-chat.png)

<a id="the-beta-readers-tab"></a>

## Der Reiter Beta-Leser

Die linke Seitenleiste des [Story-Editors](../books/story-editor.md) bekommt einen Reiter **Beta-Leser** neben *Gliederung*. Er hat einen Reiter für jeden
offenen Chat des Buchs. Mit **+** startest du einen Chat, und das Menü (⋮) daneben enthält:

| Eintrag | |
|---|---|
| Neuer Chat | Dasselbe wie **+**. |
| Chat umbenennen | Ändert den Titel des Chats. |
| Chat schließen | Schließt den Reiter; der Chat bleibt erhalten und steht unter *Geschlossene Chats*. |
| Chat-Einstellungen | Ändert die [Optionen des Chats](#the-options-of-a-chat). |
| Chat löschen | Löscht den Chat und seinen Verlauf, nach einer Bestätigung. |
| Personas dieses Buchs | Die Personas, die nur zu diesem Buch gehören, siehe [unten](#personas). |
| Geschlossene Chats | Öffnet einen geschlossenen Chat wieder. |

Die Chats werden zusammen mit dem Buch gespeichert, sind also in dessen Sicherungen enthalten.

![Der Reiter vor dem ersten Chat](../../images/plugin-betareaders-tab.png)

![Das Menü der Chats](../../images/plugin-betareaders-menu.png)

## Personas

Eine Persona hat einen **Namen**, eine **Beschreibung** (was das Modell spielt: wie die Persona liest, was sie mag und was sie stört) und ein optionales
**Symbol**, ein auf eine kleine Größe verkleinertes Bild. Ohne Symbol ist die Persona ein Kreis mit den Anfangsbuchstaben des Namens.

Die Personas deines Kontos bilden die **Persona-Bibliothek** unter *Einstellungen → Erweiterungseinstellungen → Beta-Leser*, die mit sechs Lesern beginnt:
Athena, Vesper, Ember, Quill, Nox und Sable. **Personas bearbeiten** öffnet die Bibliothek in einem Fenster, in dem du Personas hinzufügen, bearbeiten, kopieren, löschen und
neu ordnen, ihre Symbole hochladen und **Die Start-Personas hinzufügen**, die der Bibliothek fehlen. Die Änderungen werden mit **Speichern** übernommen, und **Abbrechen**
verwirft sie.

![Die Persona-Bibliothek](../../images/plugin-betareaders-library.png)

Ein Buch kann eigene Personas haben: *Personas dieses Buchs* im Menü der Chats öffnet dasselbe Fenster mit den Personas, die nur dieses Buch anbietet. Sie sind mit
*(dieses Buch)* gekennzeichnet, wenn du sie für einen Chat auswählst.

![Die Personas eines Buchs](../../images/plugin-betareaders-book-personas.png)

**Mit dem Modell schreiben** im Fenster einer Persona erledigt das Schreiben für dich: Beschreibe die Persona in wenigen Worten (*ein Leser, der nur wissen will,
wer es war und warum*), drücke den Knopf, und das Modell füllt **Name** und **Beschreibung** im Stil der Bibliothek aus. Es sind nur Felder: Ändere sie oder schreibe
erneut, und **Speichern**, wenn sie stimmen. Das Modell, das schreibt, ist das der Chats (das des Buchs für die Personas eines Buchs, das in den Einstellungen des
Plugins für die Bibliothek, sonst dein Standardmodell); die Namen, die bereits in der Liste stehen, werden ihm mitgeteilt, damit es keinen davon wählt.

![Eine vom Modell geschriebene Persona](../../images/plugin-betareaders-write-persona.png)

Ein Chat behält eine **Kopie** seiner Personas, sodass eine Änderung in der Bibliothek einen begonnenen Chat nicht verändert; die Kopie wird geändert, wenn du es
verlangst (siehe [die Gruppe](#a-group)).

<a id="starting-a-chat"></a>

## Einen Chat starten

**+** öffnet das Fenster eines neuen Chats.

![Das Fenster eines neuen Chats](../../images/plugin-betareaders-new-chat.png)

| Feld | |
|---|---|
| Art | **Eine Persona** oder **Eine Gruppe**. Sie kann danach nicht mehr geändert werden. |
| Titel | Leer: die Namen der Personas. |
| Persona(s) | Eine Persona für einen Chat mit einer; für eine Gruppe mindestens eine (eine Gruppe kann mit einer Persona beginnen, und weitere kommen später hinzu). |

Der eingeklappte Teil **Geschichte, Medium, Modell und Format** enthält die [Optionen eines Chats](#the-options-of-a-chat). Jede davon darf leer bleiben, dann wird sie übernommen.

Ein Chat mit **einer Persona** ist ein Gespräch zwischen dir und dieser Persona. Eine **Gruppe** ist ein Gespräch aller anwesenden Personas mit dir und untereinander:
Eine Antwort kann mehrere von ihnen nacheinander sprechen lassen, und sie dürfen widersprechen, sich necken und auf die Punkte der anderen aufbauen. Eine Nachricht in der Antwort
kann auch eine Erzählung sein (*Erzähler*): eine kurze Beschreibung dessen, was die Persona tut.
<a id="talking"></a>

## Sprechen

Schreiben Sie in das Feld unten und drücken Sie **Senden**. Die Antwort erscheint, während sie geschrieben wird, und **Stopp** (die Schaltfläche heißt *Stopp*, solange das Modell antwortet) bricht sie ab. Eine Antwort ist ein Zug des Modells: das Nachdenken des Modells, wenn es eines sendet (ein geschlossener Abschnitt **Reasoning**; ein Modell, das nicht nachdenkt, zeigt keinen), und die Nachrichten der Personas, jeweils mit dem, was sie tun, und dem, was sie sagen.

<a id="sending-a-scene"></a>

### Eine Szene senden

**Als Szene senden** (neben *Senden*) sendet den Text als Szene statt als etwas, das Sie sagen: einen Szenenwechsel (*Das Licht erlischt, und die Bar verstummt.*) oder eine Beschreibung dessen, was Sie tun (*Ich lege das Manuskript auf den Tisch und warte.*). Sie erscheint im Verlauf als Karte **Szene**, und die Personas reagieren darauf in dem Moment, in dem sie geschieht, wie auf eine Nachricht. Verwenden Sie die Szene des Chats (siehe [unten](#the-scene)), um festzulegen, wo das Gespräch beginnt, und diese Schaltfläche für das, was danach geschieht. Eine Szenennachricht kann wie Ihre Nachrichten bearbeitet und gelöscht werden; sie sendet keine Teile der Geschichte (das Feld *Die neuesten Teile einbeziehen* gehört zu *Senden*).

![Eine Szene im Verlauf eines Chats und die Antwort darauf](../../images/plugin-betareaders-scene.png)

Den Personas der Gruppe wird nur am Ende Ihrer letzten Nachricht mitgeteilt, wer anwesend ist (dazu wird nichts gespeichert). Das Hinzufügen oder Entfernen einer Persona ändert daher nie den früheren Verlauf.

<a id="sending-the-story"></a>

### Die Geschichte senden

Der Chat bewahrt keine Kopie der Geschichte auf. Er merkt sich, welche Teile Sie den Personas **gegeben** haben, und für jede Anfrage wird die Geschichte, die sie lesen, neu aufgebaut, aus der Geschichte, wie sie jetzt ist: die Teile, die sie erhalten haben, wobei die Zusammenfassungen an die Stelle der ältesten treten, soweit sie in den Kontext des Modells passen. Die Teile stehen vor der Nachricht, die sie gegeben hat, sodass ein Teil genau einmal in der Anfrage vorkommt und der Anfang der Anfrage gleich bleibt, wenn Sie weitere Teile geben: Ein Modellserver, der den Anfang des Prompts behält, liest nur das Neue. Ein bearbeiteter Teil wird als bearbeitet gelesen, und ein gelöschter Teil wird nicht mehr gelesen, was die Anfrage ab der Nachricht, die ihn gegeben hat, verändert. Eine später erstellte Zusammenfassung ersetzt ab der nächsten Nachricht die Teile, die sie beschreibt.

**Die neuesten Teile einbeziehen** verschafft den Personas mehr von der Geschichte: Es fügt die Teile hinzu, die sie noch nicht erhalten haben (siehe [unten](#how-much-of-the-story-the-personas-read)). Lassen Sie es aus, um einfach zu chatten und ihnen die Geschichte später zu geben. Die Beschriftung des Felds zeigt, wie viele Teile es hinzufügen würde (*Geschichte einbeziehen (5 Teile)*, *Neueste Teile einbeziehen (2 neu)*, *keine neuen*). Es folgt der Geschichte von selbst: Wenn Sie einen Teil hinzufügen, bearbeiten oder löschen oder den Zweig wechseln, ändert sich die Zahl, und das Feld ist angekreuzt, sobald es etwas Neues gibt. Eine Nachricht von Ihnen sagt, welche Teile sie gegeben hat (*Teile 1-5 gesendet*).

**Letzter Abschnitt** und **Erster Abschnitt** neben dem Feld legen fest, [wie viel von der Geschichte die Personas lesen](#how-much-of-the-story-the-personas-read); ändern Sie sie dort, während Sie weiterlesen.

Unter dem Feld steht, was die Personas gelesen haben (*Die Personas haben gelesen: Teile 1-5*) und die Größe des Kontexts (*Kontext: 2.266 von 30.720 Tokens*): was die nächste Nachricht senden würde, also der Verlauf, was Sie eingetippt haben und, wenn **Die neuesten Teile einbeziehen** angekreuzt ist, die Teile, die es hinzufügen würde. Es ändert sich, wenn Sie das Feld ankreuzen oder beim Tippen kurz innehalten. Die Teile erhalten mindestens die Hälfte des Kontexts; passt der Verlauf nicht neben sie, werden die ältesten Nachrichten (mit den Teilen, die sie gegeben haben) aus der Anfrage ausgelassen, nicht gelöscht, und ein Hinweis sagt, wie viele es sind.

<a id="changing-the-conversation"></a>

### Das Gespräch ändern

| Schaltfläche | |
|---|---|
| Stift (Ihre Nachricht) | Bearbeitet Ihre Nachricht; die Teile, die sie gegeben hat, bleiben gegeben. |
| Papierkorb | Löscht Ihre Nachricht oder eine Nachricht einer Antwort (der Rest des Zugs bleibt, und das Modell sieht den Zug ohne sie). |
| Neu generieren | Verwirft die letzte Antwort und fragt erneut. |

Wenn eine Antwort fehlgeschlagen ist oder keine vorhanden ist, sagt das Panel dies und bietet **Erneut fragen** an.

![Die Aktionen einer Nachricht](../../images/plugin-betareaders-actions.png)

<a id="a-group"></a>

### Eine Gruppe

*Personas (n)* über dem Gespräch zeigt, wer anwesend ist. In einer Gruppe können Sie:

- **Eine Persona hinzuholen** aus der Bibliothek oder dem Buch, mit einer optionalen Notiz, die der Gruppe sagt, warum sie gekommen ist („Mara hat das ganze Buch gelesen“). Der Verlauf erhält eine Zeile (*Mara ist dem Gespräch beigetreten.*), damit das Modell weiß, wann.
- **Eine Persona verabschieden** (die Pfeiltaste in ihrer Zeile), wieder mit einer Notiz. Sie bleibt in der Liste, ausgegraut und mit *(verlassen)* markiert. Die letzte Persona kann nicht gehen.
- **Aus der Bibliothek aktualisieren**: übernimmt die aktuelle Beschreibung und das Symbol der Persona aus der Bibliothek (oder dem Buch) in den Chat.

![Eine Persona, die der Gruppe beigetreten ist](../../images/plugin-betareaders-joined.png)

![Eine Persona hinzuholen](../../images/plugin-betareaders-bring-in.png)
<a id="the-options-of-a-chat"></a>

## Die Optionen eines Chats

Das Fenster eines neuen Chats und **Chat-Einstellungen** haben dieselben Optionen; jede davon darf leer bleiben, dann übernimmt der Chat sie.

| Option | |
|---|---|
| Dein Name | Wie die Personas dich nennen; deine Nachrichten zeigen ihn statt *Du*. Leer: nicht benannt. |
| Szene | Wo und wie das Gespräch stattfindet, in wenigen Sätzen: *eine verregnete Bar nach Feierabend*, *ein Videoanruf nach dem Buchclub*. Ohne Angabe ist der Chat ein Online-Chat, siehe [unten](#the-scene). Eine Änderung in einem laufenden Chat ändert den Systemprompt ab der nächsten Anfrage und hinterlässt keine Zeile im Verlauf; für das, was ab jetzt geschieht, verwende [eine Szenennachricht](#sending-a-scene). |
| Medium | Wofür der Text steht: ein Buch, ein Film, eine Serie, ein Manga oder Comic, ein Hörspiel, ein Theaterstück, ein Spiel oder eine Visual Novel. Die Personas reagieren so, wie das Publikum dieses Mediums reagieren würde. Eine Änderung in einem laufenden Chat hinterlässt eine Zeile im Verlauf (*Ab jetzt wird der Text gelesen als: ...*). |
| Modell | Die KI, die antwortet. |
| Protokoll | Das [Protokoll](../protocols.md), das damit verwendet wird. |
| Antwortformat | Wie das Modell zum Schreiben aufgefordert wird: **Automatisch** (JSON mit einem Schema, wenn das Modell einem Schema folgen kann, sonst Freitext), **JSON** (immer mit Schema), **JSON ohne Schema** oder **Freitext**. Siehe [die Argumentation](#the-reasoning). |
| Personas lesen bis Abschnitt | Die [Spoiler-Grenze](#how-much-of-the-story-the-personas-read). Nur im Fenster eines neuen Chats; ein bestehender Chat hat sie unter dem Textfeld. |
| Beginnen bei Abschnitt | Der Abschnitt, bei dem die Geschichte beginnt, die die Personas bekommen. Wie oben. |
| Zusammenfassungen der Geschichte verwenden | Siehe unten. |
| Meiste Nachrichten pro Zug | Die meisten Nachrichten, die die Personas als Antwort auf eine deiner Nachrichten schreiben (1 bis 50). Leer: deine Einstellung unter *Einstellungen → Einstellungen der Erweiterung → Beta Readers*. Eine Antwort, die mehr enthält, wird abgeschnitten, und das Modell erfährt die Grenze. |

![Die Einstellungen eines Chats](../../images/plugin-betareaders-chat-settings.png)

<a id="the-reasoning"></a>

### Die Argumentation

Die **Argumentation** über einer Antwort ist das Nachdenken des Modells, so wie der Server es sendet (wie bei den anderen Generatoren von Marginalia); ein Modell, das nicht nachdenkt, hat keine, und es wird nichts angezeigt. Das Plugin bittet das Modell nicht um eine eigene Argumentation: ein paar Sätze unter einem Schema wären eher ein Plan als Nachdenken und würden nur einem Modell im Weg stehen, das denkt. Das Nachdenken, das als Text zwischen `<think>`-Tags geschrieben wird, wird ebenfalls als Argumentation angezeigt.

**JSON ohne Schema** (in *Chat-Einstellungen*) verlangt dasselbe JSON nur im Prompt, ohne ein Antwortformat; verwende es mit einem Server, der kein Schema zusammen mit dem Nachdenken des Modells verarbeiten kann.

<a id="the-text-as-it-is-written"></a>

### Der Text, wie er geschrieben wird

Die Antwort wird angezeigt, während das Modell sie schreibt, Nachricht für Nachricht und Wort für Wort: Die gerade geschriebene Nachricht erscheint, sobald klar ist, wer spricht, und wächst. Ein Teil, der eine Handlung ist, erscheint, sobald sein Typ geschrieben wird, sodass eine Geste nie zuerst wie Rede aussieht.

Das Gespräch folgt der Antwort beim Wachsen nach unten. Scrollst du nach oben, um etwas Früheres zu lesen, bleibt es an dieser Stelle; scrollst du zurück zum Ende, folgt es wieder. Das Senden einer Nachricht geht immer ans Ende.

<a id="the-scene"></a>

### Die Szene

Die Szene ist ein Text, der in den Systemprompt jedes Chats kommt, nach dem Prompt, damit die Personas wissen, wo sie sind und wie sie miteinander reden. Sie ist eine [vererbte Einstellung](../books/prompts.md#inherited-settings): die des Chats, sonst die des Buchs, sonst die des nächsten Ordners, sonst die unter *Einstellungen → Einstellungen der Erweiterung → Beta Readers*, sonst die eingebaute: **ein Online-Chat**, in dem alle kurze Nachrichten schreiben, von wo auch immer sie gerade sind. Ein eigener Prompt, der `{{scene}}` enthält, bestimmt, wo die Szene steht; ohne diesen Platzhalter wird die Szene nach dem Prompt angefügt.

<a id="how-much-of-the-story-the-personas-read"></a>

### Wie viel der Geschichte die Personas lesen

Die Abschnitte sind ab 1 nummeriert, wie im Reiter *Gliederung* der Geschichte.

- **Personas lesen bis Abschnitt** (das Feld **Letzter Abschnitt** unter dem Textfeld) ist die Spoiler-Grenze: Die Personas bekommen nie einen Abschnitt nach ihr, und die Zusammenfassungen werden nur aus der Geschichte bis dorthin gesammelt. Erhöhe sie, wenn die Geschichte weitergeht, dann bieten die nächste Nachricht die zurückgehaltenen Abschnitte als neue an. Senkst du sie, lesen die Personas die späteren Abschnitte sofort nicht mehr.
- **Beginnen bei Abschnitt** (**Erster Abschnitt**) lässt die früheren Abschnitte weg, nicht einmal als Zusammenfassungen. Verwende es, damit die Personas nur ein Kapitel lesen.
- **Zusammenfassungen der Geschichte verwenden** (standardmäßig an) lässt die Geschichte mit den Zusammenfassungen beginnen und danach mit den Abschnitten nach der neuesten davon, was eine lange Geschichte klein hält. Zusammenfassungen gelten nur, wenn die Geschichte am Anfang beginnt.

Ein erster Abschnitt nach dem letzten wird abgelehnt.

Änderst du diese Werte unter dem Textfeld (sie werden gespeichert, sobald du aufhörst zu tippen oder auf einen Schritt-Button klickst), ändert sich die Geschichte, die die Personas lesen, ab der nächsten Nachricht; nichts wird erneut gesendet, weil die Geschichte für jede Nachricht neu aufgebaut wird.

![Ein Chat, der nur die Abschnitte 2 bis 3 liest](../../images/plugin-betareaders-spoiler.png)

Das Lorebook wird den Personas nicht gesendet: Sie wissen nur, was die Geschichte ihnen erzählt.

<a id="settings"></a>

## Einstellungen

*Einstellungen → Einstellungen der Erweiterung → Beta Readers* ist der Ort deiner Voreinstellungen:

![Die Einstellungen des Plugins](../../images/plugin-betareaders-settings.png)

| Einstellung | |
|---|---|
| Medium | Das Medium der Chats. |
| Modell der Chats | Die KI. Leer: das Modell des Buchs. |
| Protokoll der Chats | Leer: das Protokoll des Buchs. |
| Prompt eines Chats mit einer Persona | Der Systemprompt eines Einzel-Chats. Leer: der eingebaute Prompt. |
| Prompt eines Gruppen-Chats | Der Systemprompt eines Gruppen-Chats. Leer: der eingebaute Prompt. |
| Szene | Siehe [oben](#the-scene). Leer: ein Online-Chat. |
| Meiste Nachrichten pro Zug | Die meisten Nachrichten, die die Personas als Antwort auf eine deiner Nachrichten schreiben, für jeden Chat, der keine eigene Grenze setzt (1 bis 50). Leer: automatisch, 3 für einen Chat mit einer Persona und etwa zwei für jede Persona einer Gruppe (höchstens 8). Eine kurze Grenze hält die Antworten knapp; erhöhe sie, wenn die Personas mitten im Gespräch abgeschnitten werden. |
| Bibliothek der Personas | **Personas bearbeiten**, siehe [oben](#personas). |
| Medien | **Medien bearbeiten**: Jedes Medium hat einen Namen und den Prompt, der den Personas sagt, wie sie darauf reagieren. **Eingebaute Medien wiederherstellen** ergänzt die fehlenden. |

`{{medium}}` in einem Prompt wird durch den Text des Mediums ersetzt und `{{scene}}` durch die Szene. Die Einstellungen werden mit dem Button **Speichern** der Seite *Einstellungen* gespeichert.

Die Szene, das Medium, das Modell, das Protokoll und die beiden Prompts sind ebenfalls [vererbte Einstellungen](../books/prompts.md#inherited-settings): Der Reiter *Prompts* eines Buchs und die Einstellungen eines [Ordners](../books/folders.md) haben sie im Abschnitt **Einstellungen der Erweiterung**. Ein Buch verwendet seinen eigenen Wert, sonst den des nächsten Ordners, sonst den aus *Einstellungen*, sonst den eingebauten; das Modell und das Protokoll des Buchs sind der letzte Rückgriff. Ein Chat überschreibt für sich das Medium, das Modell und das Protokoll in seinen [Optionen](#the-options-of-a-chat).

<a id="on-a-phone"></a>

## Auf dem Telefon

Funktioniert. Der Reiter **Beta Readers** befindet sich in der Gliederungsschublade der Geschichte, und die Chats sind dieselben wie auf dem Desktop; der Platz für das Gespräch ist klein, daher ist Querformat einfacher. Siehe [Marginalia auf dem Telefon verwenden](../phone.md#extensions).

<a id="storage"></a>

## Speicherung

Die Personas und die Chats eines Buchs werden in den Attributen des Buchs gespeichert (also mit seinen Sicherungen), die Personas deines Kontos und die Einstellungen in deinen Benutzereinstellungen. Das Modell wird mit den üblichen Grenzen der KI angefragt, und die Tokens zählen als vom Buch verbraucht.
