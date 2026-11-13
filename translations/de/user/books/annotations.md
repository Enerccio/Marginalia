---
label: Anmerkungen
order: 95
---

# Anmerkungen

Eine **Anmerkung** ist eine Notiz für dich zu einer Textstelle eines Abschnitts: *schwache Szene*, *Zeitablauf stimmt nicht*, *überarbeiten*. Sie funktioniert wie ein
Kommentar in einem Textverarbeitungsprogramm: Der Text der Geschichte bleibt sauber, die Textstelle wird nur markiert, und die Notiz steht daneben.
Notizen können auch von einem Modell oder von einer [Erweiterung](../extensions/index.md) stammen, die die Geschichte prüft.

Eine Anmerkung ist **für den Autor**. Sie wird nicht an das Modell geschickt, außer du legst das für diese Notiz fest, siehe
[Eine Notiz an das Modell geben](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Was eine Anmerkung ist

| | |
|---|---|
| **Textstelle** | Die Wörter, auf die sich die Notiz bezieht, oder der **ganze Abschnitt**. |
| **Notiz** | Was du dir merken möchtest. |
| **Typ** | Ein optionales Wort, das du wählst (*Kontinuität*, *Stil*, *Tempo*...). |
| **Quelle** | Wer sie geschrieben hat: du, ein Modell (*KI*), ein *Agent* oder eine *Erweiterung*. Die Markierung im Text hat für jede Quelle eine andere Farbe. |
| **Status** | *Offen*, *Erledigt*, *Verworfen* oder *Verwaist*. Nur offene Notizen werden im Text markiert. |

Ein Abschnitt kann beliebig viele Anmerkungen haben, auch zu sich überlappenden Textstellen.

<a id="making-an-annotation"></a>

## Eine Anmerkung anlegen

**Zu einer Textstelle.** Markiere die Wörter im Text des Abschnitts (sie können Formatierungen enthalten, die Auswahl muss nicht genau sein), dann **klicke sie mit der rechten Maustaste** an (auf dem Telefon ein langes Drücken) und wähle **Auswahl kommentieren**, oder drücke ihre [Taste](story-editor.md#keyboard-shortcuts) (`a`). Das Fenster zeigt die Textstelle; schreibe die Notiz, einen
optionalen Typ und lege fest, ob das Modell die Notiz erhält, und klicke auf **Speichern**. Das Menü (☰) des Abschnitts hat keinen solchen Eintrag: Beim Öffnen geht die Auswahl verloren. Das Kontextmenü der rechten Maustaste enthält **Auswahl kommentieren**, [**Auswahl überarbeiten**](story-editor.md#redrafting-only-a-passage) und **Kopieren**. Es ersetzt
das des Browsers auf dem Text eines Abschnitts, deshalb hat es auch **Kopieren**. Während der Abschnitt bearbeitet wird, wird die Auswahl im Editor verwendet: Die Änderung wird vorher gespeichert.

**Zum ganzen Abschnitt.** Wähle **Abschnitt kommentieren** im Menü des Abschnitts.

**Mit einem Modell.** Wähle **Mit KI kommentieren**, siehe [unten](#letting-a-model-annotate).

**Von der Kommandozeile aus** (und für Erweiterungen): [`/create-annotation`](../commands/index.md#annotations).

![Das Kontextmenü des Textes eines Abschnitts](../../images/story-text-menu.png)

![Das Fenster einer neuen Anmerkung](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Die Anmerkungen lesen

Eine Textstelle mit einer offenen Notiz ist mit einer **Wellenlinie** unterstrichen, gefärbt nach dem Verfasser der Notiz (blau du, orange ein
Modell, grün ein Agent, rot eine Erweiterung). Eine Notiz innerhalb einer anderen ist tiefer unterstrichen, damit beide sichtbar sind.

![Die Notiz einer unterstrichenen Textstelle, an der Markierung geöffnet](../../images/annotation-popover.png)

- **Klicke auf den unterstrichenen Text**, um die Notizen zu sehen, die diese Stelle abdecken (alle, wenn sich Textstellen überschneiden). Sie öffnen sich neben dem Text; auf dem Telefon tippe auf die Textstelle.
- Eine **Sprechblase mit einer Zahl** in der Kopfzeile eines Abschnitts zählt die Notizen des Abschnitts, die eine Prüfung brauchen. Klicke darauf, um sie alle zu sehen, auch die zum ganzen Abschnitt und die, deren Textstelle verloren ging.
- Am **Rand** (das Zahnradmenü der Geschichte, **Anmerkungen am Rand**; nur am Desktop, und die Einstellung wird gespeichert) stehen die Notizen aller Abschnitte in einer Spalte links neben ihrem Text, in der Reihenfolge ihrer Textstellen, statt im Popover. Die Notiz unter dem Mauszeiger und ihre unterstrichene Textstelle leuchten gemeinsam auf, und ein Klick auf eine Textstelle bringt ihre Notiz in den Blick. Es sind dieselben Notizen (erledigen, bearbeiten...); erledigte und verworfene Notizen werden dort nicht gezeigt, da sie nicht unterstrichen sind.
- Der Reiter **Anmerkungen** listet die Notizen des ganzen Buches auf, nach Abschnitt gruppiert in der Reihenfolge der Geschichte, mit einem Filter
  (*Zu prüfen*, *Abgeschlossen*, *Alle*). **In der Geschichte anzeigen** (oder die Nummer des Abschnitts) führt dich zum Abschnitt.

![Die Notizen am Rand eines Abschnitts](../../images/annotations-margin.png)

![Der Reiter Anmerkungen](../../images/annotations-tab.png)

Jede Notiz zeigt, wer sie geschrieben hat, ihren Typ, ihren Status, die Textstelle, die Notiz und diese Aktionen:

| | |
|---|---|
| ✓ **Erledigen** | Das Problem ist behoben. Die Notiz verschwindet aus dem Text und bleibt in der Liste. |
| ✗ **Verwerfen** | Die Notiz ist es nicht wert, behandelt zu werden. Ebenso. |
| ↶ **Wieder öffnen** | Holt eine geschlossene Notiz zurück. |
| ✎ **Bearbeiten** | Ändert die Notiz und den Typ. |
| 🗑 **Löschen** | Löscht die Notiz; sie kann aus dem [Papierkorb](../trash.md) wiederhergestellt werden. |
| **Mit dem Abschnitt an das Modell geben** | Siehe unten. |
| **An die Auswahl anheften** | Für eine Notiz ohne Textstelle: Markiere Text im Abschnitt, dann klicke darauf. |

<a id="annotations-follow-the-text"></a>

## Anmerkungen folgen dem Text

Du musst beim Bearbeiten nicht darauf achten, wo eine Notiz steht. Marginalia behält die Textstelle jeder Notiz bei den Wörtern, auf die sie sich bezog:

- Text, der **vor** der Textstelle hinzugefügt oder entfernt wird, verschiebt sie;
- eine Änderung **innerhalb** der Textstelle macht sie länger oder kürzer;
- Wörter, die du **direkt am Anfang oder Ende** der Textstelle tippst, liegen außerhalb von ihr;
- das Korrigieren eines Tippfehlers in einer Textstelle aus einem einzigen Wort behält die Notiz bei diesem Wort.

Wenn die Textstelle **gelöscht** oder so umgeschrieben wird, dass sie nicht mehr gefunden werden kann, wird die Notiz **Verwaist**. Sie bleibt erhalten: Hefte sie an eine andere Textstelle (**An die Auswahl anheften**), verwirf sie oder lösche sie. Wenn du die Wörter wiederherstellst (rückgängig), findet die Notiz ihre Textstelle wieder.

Wenn ein Abschnitt **neu generiert** wird, ist der Text neu, daher finden die Notizen ihre Textstellen anhand der Wörter wieder, die sie zitiert haben; diejenigen, deren Wörter nicht mehr vorhanden sind, werden verwaist. Wenn zwei Abschnitte [zusammengeführt](story-editor.md#merging-a-part-into-the-previous-one) werden, wandern die Notizen des späteren in den zusammengeführten Abschnitt. Ein **Zweig** eines Abschnitts kopiert seine Notizen. Notizen sind Teil der [Sicherungen](backups.md), der Kopien eines Buches und der Exporte einer Sicherung.
<a id="letting-a-model-annotate"></a>

## Ein Modell annotieren lassen

**Mit KI kommentieren** im Menü eines Abschnitts bittet ein Modell, ihn zu prüfen. Sie sagen, worauf geachtet werden soll (*Kontinuitätsfehler markieren*,
*schwache Dialoge*, *widerspricht dem Lore*; bleibt das Feld leer, gilt, was das Buch, sein Ordner oder Ihre Einstellungen [einmal festgelegt](prompts.md#annotate-prompt) haben, sonst die Aufgabe der Vorlage), wählen das Modell und klicken auf **Kommentieren**.

Das Modell liest den Abschnitt so, wie es die [Überarbeitung](story-editor.md#redrafting-a-part) tut: den Abschnitt, die Geschichte davor (ältere
Geschichte als [Zusammenfassungen](summaries.md)), so viel von der Geschichte danach, wie hineinpasst, das Lore und den Stil des Buches sowie die
Anmerkungen, die der Abschnitt bereits hat, damit es sich nicht wiederholt. Es gibt nie Positionen an: Es **zitiert** die Stelle, und Marginalia
findet das Zitat im Text. Ein Zitat, das nicht gefunden wird, geht nicht verloren: Die Anmerkung wird als Anmerkung zum ganzen Abschnitt vorgeschlagen, markiert mit *nicht im Text gefunden*.

**Nichts wird dem Buch hinzugefügt, bis Sie zustimmen.** Die vorgeschlagenen Anmerkungen werden mit je einem Kontrollkästchen aufgelistet (alle angekreuzt). Entfernen Sie das Häkchen bei denen, die Sie nicht wollen, und wählen Sie **Die ausgewählten Anmerkungen hinzufügen**.

Das Fenster blockiert am Computer nicht die Geschichte, und es kann in die Ecke der Seite eingeklappt werden, während das Modell arbeitet, siehe
[Mit den Fenstern nebenher arbeiten](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Option | |
|---|---|
| **Modell** | Das Modell, das prüft. Leer bedeutet das **Standardmodell für Anmerkungen** aus Ihren [Einstellungen](../account.md#general-settings), sonst das Modell des Buches. Prüfen ist Analyse, deshalb ist es oft nicht das Modell, das die Prosa schreibt. |
| **Antwortformat** | *Automatisch* (strukturiert, wenn das Modell mit JSON antworten kann), *Strukturiert (JSON)*, *Textblöcke* (für Modelle, die sich nicht an ein Format halten) oder *Freie Kritik, dann Extraktion* (das Modell schreibt eine freie Kritik, ein zweites Modell, meist ein günstigeres, macht daraus Anmerkungen: wählen Sie es unter **Modell, das die Anmerkungen extrahiert**). |

![Die Anmerkungen, die ein Modell für einen Abschnitt vorschlägt](../../images/annotate-ai-proposals.png)

**Ein Bereich von Abschnitten (Stapel).** **Mit KI kommentieren** im Menü eines Abschnitts prüft nur diesen Abschnitt. Um mehrere zu prüfen, öffnen Sie das Einstellungsmenü (⚙) der Geschichte und wählen **Einen Bereich von Abschnitten mit KI kommentieren** (auch auf dem Telefon). Es ist dasselbe Fenster mit **Ab Abschnitt** und **Bis Abschnitt** zur Auswahl (beide sind nötig, **Kommentieren** wartet darauf; welcher der frühere ist, spielt keine Rolle; nur Abschnitte mit einem Text werden aufgelistet). **Kommentieren** prüft dann die Abschnitte des Bereichs nacheinander, mit denselben Anweisungen, demselben Antwortformat und Modell, jeweils gelesen mit der Geschichte um sie herum wie bei einem einzelnen Abschnitt. Die Vorschläge werden unter der Nummer ihres Abschnitts aufgelistet, alle angekreuzt, und **Die ausgewählten Anmerkungen hinzufügen** fügt jede dem eigenen Abschnitt hinzu. **Stopp** beendet den gerade laufenden Abschnitt und startet keine weiteren; was bis dahin gefunden wurde, bleibt in der Liste. Ein Abschnitt, dessen Antwort nicht gelesen werden kann oder nicht in den Kontext des Modells passt, wird übersprungen (das Fenster sagt, welcher), und der Lauf geht weiter; ein Ausfall des Modells beendet den Lauf. Jeder Abschnitt ist eine Anfrage an das Modell, ein langer Bereich kostet also so viele Anfragen (und Tokens).

![Annotieren eines Bereichs von Abschnitten: Die Vorschläge sind unter der Nummer ihres Abschnitts aufgelistet](../../images/annotate-ai-batch.png)

**Fakten aus Erweiterungen.** Eine [Erweiterung](../extensions/index.md), die mehr über die Geschichte weiß, als ihr Text sagt, kann dies dem Prüfer mitgeben; der markiert dann die Stellen, die dem widersprechen, als Kontinuitätsfehler. Der [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know) tut das: Er teilt dem Modell mit, was jede verfolgte Figur des Abschnitts wusste, als der Abschnitt beginnt, sodass eine Figur, die einen Namen benutzt, den sie nie gehört hat, angemerkt wird. Die Fakten stehen in der eingebauten [Annotate-Vorlage](prompts.md#annotate-prompt) (eine eigene Vorlage muss `{%{{{facts}}}%}` verwenden); ohne eine solche Erweiterung bleibt die Vorlage unverändert.

Wenn eine Antwort nicht gelesen werden kann, fragt Marginalia einmal nach; kann sie immer noch nicht gelesen werden, wird die Antwort des Modells angezeigt, damit die Arbeit nicht verloren geht. Die Vorlage ist die [Annotate-Vorlage](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Jeden neuen Abschnitt automatisch annotieren

Das Einstellungsmenü (⚙) der Geschichte enthält **Automatische Anmerkungen** (auch auf dem Telefon; ein Häkchen zeigt, dass es eingeschaltet ist). Das Buch prüft dann jeden Abschnitt gleich nach seiner Generierung und überarbeitet ihn optional. Die Einstellungen werden zusammen mit dem Buch gespeichert:

| Option | |
|---|---|
| **Modell**, **Protokoll** | Das Modell, das prüft. Leer bedeutet das Modell des Buches (nicht das **Standardmodell für Anmerkungen** Ihrer Einstellungen, mit dem das Fenster startet, das Sie selbst öffnen). |
| **Worauf geachtet werden soll** | Die Anweisungen wie bei **Mit KI kommentieren**. Leer gilt, was das Buch übernimmt (siehe [Annotate-Vorlage](prompts.md#annotate-prompt)), sonst die Aufgabe der Vorlage; wenn Sie es leer lassen, zeigt das Feld danach den Text, der verwendet wurde. |
| **Jeden generierten Abschnitt automatisch kommentieren** | Schaltet es ein. Nachdem ein Abschnitt generiert wurde (ein neuer Abschnitt, ein Swipe oder eine Neugenerierung; nicht einer, der gestoppt wurde oder fehlschlug), prüft das Modell ihn **sofort**, ohne Fenster; das [Panel der Läufe](story-editor.md#the-panel-of-the-runs) zeigt es als Schritt von [was dem Abschnitt folgt](story-editor.md#what-follows-a-generated-part). |
| **Anmerkungen ohne Nachfrage hinzufügen** | Wenn das Modell fertig ist, werden die Anmerkungen, die es gefunden hat, hinzugefügt. Ohne sie öffnet sich das Annotationsfenster mit den Vorschlägen (auf der Seite, auf der Sie generiert haben; über **Prüfen** im Panel auf jeder Seite) und wartet darauf, dass Sie sie auswählen, wie beim Start von Hand. |
| **Danach den Abschnitt überarbeiten, um die Anmerkungen zu berücksichtigen** | Nur mit der Option darüber. Nachdem Anmerkungen hinzugefügt wurden, [überarbeitet](story-editor.md#redrafting-a-part) das Modell den Abschnitt mit den offenen Anmerkungen des Abschnitts und ohne eigene Anweisungen. Es schreibt mit dem Modell des Buches und rührt keine Zusammenfassungen an: Der Abschnitt ist neu, keine Zusammenfassung beschreibt ihn. Ein Modell, das nichts gefunden hat, hat nichts zu überarbeiten. |
| **Überarbeitung ohne Nachfrage anwenden** | Nur mit der Überarbeitung. Wenn das Modell fertig ist, ersetzt die Überarbeitung den Text des Abschnitts, und die Anmerkungen, die sie behandelt hat, werden als erledigt markiert. Ohne sie oder wenn Sie den Abschnitt geändert haben, während das Modell schrieb, öffnet sich das Überarbeitungsfenster mit dem Ergebnis und wartet auf Sie. |

Das Überarbeitungsfenster, das auf Sie wartet, hat zwei Reiter: **Überarbeiteter Text** (der Text, seine Begründung und die Unterschiede zum Abschnitt) und **Originaltext mit Anmerkungen** (der Abschnitt, wie er war, mit den Passagen der offenen Anmerkungen markiert wie in der Geschichte, und den Anmerkungen darunter; klicken Sie auf eine Markierung, um zu ihrer Anmerkung zu springen, oder zeigen Sie auf eine von beiden, um beide hervorzuheben). Sehen Sie sich das Original an, um zu erkennen, was das Modell bearbeiten sollte, ändern Sie den Text oder wenden Sie ihn an.

![Die Einstellungen der automatischen Annotation](../../images/annotate-auto.png)

**Ein Fenster schließen, das auf Sie wartet, oder Stopp im Panel beendet, was dem Abschnitt folgt.** Es wird nichts mehr hinzugefügt oder angewendet, und die späteren Schritte (eine Übersetzung, das Audio) werden nicht ausgeführt; die Meldung des Panels nennt sie, damit Sie sie von Hand erledigen können. Was bereits hinzugefügt oder angewendet wurde, bleibt. Während die Überarbeitung läuft, kann kein neuer Abschnitt generiert werden (er würde am alten Text anknüpfen); während die Anmerkungen entstehen, geht es.

Jeder generierte Abschnitt kostet mindestens eine weitere Anfrage an das Modell (bei der Überarbeitung zwei). Wenn Sie mit einem lokalen Server generieren, der nur einen Prompt in seinem Cache behält, ersetzt die Prüfung die Geschichte darin, und die nächste Generierung muss die Geschichte erneut lesen: Wählen Sie ein anderes **Modell** für die Prüfung oder einen zweiten Server.

<a id="giving-a-note-to-the-model"></a>

## Einer Figur bzw. dem Modell eine Anmerkung geben

Aktivieren Sie **Mit dem Abschnitt an das Modell geben** bei einer Anmerkung, und sie wird auch zu einer Anmerkung an das Modell: Die Anmerkungen der Abschnitte, die im Prompt stehen, gehen als `{%{{{annotations}}}%}` in die Hauptvorlage (die eingebaute Vorlage hat einen Block *Anmerkungen des Autors zur Geschichte*; eine eigene Vorlage muss die Variable verwenden). Nur **offene** Anmerkungen mit diesem Häkchen werden übergeben, und nur für die Abschnitte, die in den Prompt passen. Verwenden Sie es für das, was das Modell beim Weiterschreiben im Sinn behalten soll (*diese Figur verbirgt den Brief*), nicht für Bearbeitungshinweise.

<a id="redrafting-to-address-the-notes"></a>

## Überarbeiten, um die Anmerkungen zu berücksichtigen

Hat ein Abschnitt offene Anmerkungen, hat das Fenster der [Überarbeitung](story-editor.md#redrafting-a-part) ein Kontrollkästchen **Die offenen Anmerkungen des Abschnitts berücksichtigen**. Ist es angekreuzt, bekommt das Modell die Anmerkungen (die Stelle und die Anmerkung) zusammen mit Ihren Anweisungen und schreibt den Abschnitt um, um sie zu behandeln; wenn Sie die Überarbeitung **anwenden**, werden diese Anmerkungen als erledigt markiert. Bei der [Überarbeitung einer Auswahl](story-editor.md#redrafting-only-a-passage) werden nur die Anmerkungen übergeben, deren Stelle sich mit der Auswahl überschneidet.
<a id="commands"></a>

## Befehle

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`,
`/resolve-annotation`, `/reattach-annotation` und `/delete-annotation` arbeiten mit den Anmerkungen über die Kommandozeile.
Erweiterungen können dieselben Befehle verwenden. [`/generate-annotation <part>`](../commands/index.md#annotations) ist **Mit KI kommentieren** als Befehl: Das Modell prüft den Abschnitt mit dem Modell des Buches
(ein Agent verwendet sein eigenes Modell, wenn *Das Buch mit dem Modell des Agenten schreiben* aktiviert ist; `profile` wählt ein anderes Modellprofil des Agenten),
mit den Anweisungen, dem Antwortformat und dem Prompt des Buches, und jede gefundene Anmerkung wird sofort hinzugefügt,
da niemand da ist, der sie auswählt. Es ist eine einzelne Anfrage an das Modell, läuft daher allein und verursacht Token-Kosten.

<a id="annotations-and-agents"></a>

## Anmerkungen und Agenten

Ein [Agent](../agents.md) liest und schreibt Anmerkungen mit denselben Befehlen: Er kann ein ganzes Buch kommentieren (*Kontinuität*, *Stil*...), Anmerkungen, die er bearbeitet hat, auflösen sowie sie bearbeiten
oder löschen. Er arbeitet auf einer Kopie des Buches, daher gelangt nichts in deine Anmerkungen, bevor du [das Ergebnis geprüft hast](../agents.md#reviewing-the-result):

- Eine Anmerkung, die der Agent erstellt hat, ist eine Zeile **Anmerkung - Hinzugefügt**; eine, die er aufgelöst, verworfen oder umformuliert hat, ist **Geändert** (mit Anmerkung, Typ, Status und *an das Modell geben* als
  Felder, jeweils mit eigenem Kästchen **Diese Änderung übernehmen**); eine, die er gelöscht hat, ist **Gelöscht**. Die Anmerkungen eines Abschnitts, den der Agent gelöscht hat, gehen mit dem Abschnitt und haben keine Zeilen.
- Die Zeile gibt an, in welchem Abschnitt die Anmerkung steht (`3: ` und den Anfang der Anmerkung); **In der Kopie anzeigen** öffnet den Abschnitt in der Kopie des Agenten. Eine Anmerkung zu einem Abschnitt, den der Agent geschrieben hat, braucht
  diesen Abschnitt: Wenn du die Anmerkung markierst, wird auch der Abschnitt markiert.
- **Nur Anmerkungen** oberhalb der Liste markiert nur die Anmerkungen. Die Stelle einer übernommenen Anmerkung ist dieselbe, wenn der Text des Abschnitts gleich ist; wenn du den neuen Text des Abschnitts des Agenten übernimmst oder dein eigener Text abweicht, findet die Anmerkung ihre Wörter im Text deines Buches erneut und wird, wenn ihr das nicht gelingt, zu einer Anmerkung über den ganzen Abschnitt.
- Eine Anmerkung, die du geschrieben hast, während der Agent pausiert war, gelangt beim Fortsetzen in seine Kopie (und eine Anmerkung, die du aufgelöst hast, wird dort aufgelöst), damit er davon weiß; eine Anmerkung, die du und der Agent beide geändert habt, ist ein Konflikt wie jeder andere.
