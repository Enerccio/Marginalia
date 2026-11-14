---
label: Schreibstatistik
order: 75
---

# Schreibstatistik

Marginalia zählt, was mit deinen Büchern geschieht, Tag für Tag: wie viele Wörter du geschrieben hast und wie viele die Modelle geschrieben haben, wie lange du geschrieben hast, wie viele Tokens an die Modelle gesendet wurden und wie groß das Buch geworden ist. Das Fenster **Statistik** zeigt dies für ein Buch oder für alle Bücher, als Zahlen, Diagramme und einen **Kalender** der Tage, an denen du gearbeitet hast, mit deiner **Serie** und einem **Tagesziel**.

Die Zahlen sind nur für dich bestimmt: Jedes Konto sieht nur seine eigenen Bücher. Das Zählen beginnt mit Version 1.6.0; was davor geschah, wird [so gut wie möglich aus der Historie der Abschnitte rekonstruiert](#days-before-the-statistics).

<a id="opening-the-window"></a>

## Das Fenster öffnen

- **Ein Buch:** Das Einstellungsmenü (die Zahnräder unter der Geschichte) enthält **Statistik**. Das Fenster zählt dieses Buch.
- **Alle Bücher:** Die Liste der Bücher hat **Statistik** im Menü **Verwalten**. Das Fenster addiert alle deine Bücher und hat zwei weitere Reiter, **Bücher** und **Nach Buch**.

Beide Fenster funktionieren gleich. Oben wählst du den **Zeitraum** (*Heute*, *Gestern*, *Letzte 7 Tage*, *Letzte 30 Tage*, *Dieser Monat*, *Gesamte Zeit* oder *Benutzerdefiniert* mit einem **Von**- und einem **Bis**-Datum) und **Gezählt für**: **Alle**, **Ich** (du und die Modelle, die du angefragt hast) oder **Agenten** (was [Agenten](../agents.md) getan haben, das getrennt geführt wird). Die Tage sind die Tage der Uhr des Servers.

![Die Statistik eines Buches: die Tage](../../images/statistics-book.png)

Unterhalb der Auswahl stehen die Summen des Zeitraums, dann die Reiter.

<a id="what-is-counted"></a>

## Was gezählt wird

| | |
|---|---|
| **Geschriebene Wörter** | Wörter, die du selbst geschrieben hast: der Text, den du in einem Abschnitt, im Geschichteneditor oder an anderer Stelle eingegeben hast, wo du einen Abschnitt bearbeitest. |
| **Generierte Wörter** | Wörter, die die Modelle für das Buch geschrieben haben. |
| **Entfernte Wörter** | Wörter, die verschwunden sind: Text, den du aus einem Abschnitt geschnitten hast, oder ein gelöschter Abschnitt. |
| **Erstellte Abschnitte**, **Gelöschte Abschnitte**, **Bearbeitungen** | Wie viele Abschnitte angelegt und gelöscht wurden und wie oft du einen geänderten Abschnitt gespeichert hast. |
| **Gesendete Tokens**, **Generierte Tokens**, **Anfragen** | Was für dieses Buch an die Modelle ging (der gesamte Prompt jedes Mal) und was zurückkam, sowie wie viele Anfragen nötig waren. |
| **Offen** | Wie lange das Buch auf deinem Bildschirm geöffnet war. |
| **Bearbeiten** | Wie lange du in einem Abschnitt getippt hast. Eine längere Pause hält die Uhr an. |
| **Generieren** | Wie lange ein Modell für das Buch gearbeitet hat. |
| **Laufende Agenten** | Wie lange ein Agent am Buch gearbeitet hat. |
| **Wachstum** | Wie viel größer (oder kleiner) das Buch im Zeitraum geworden ist, in Wörtern. |

Die Zeiten richten sich nach der echten Uhr. Wenn zwei Bücher gleichzeitig geöffnet sind oder drei Agenten zusammen laufen, zählt die Summe diesen Zeitabschnitt nur einmal, sodass die Zeilen der Bücher mehr ergeben können als die Gesamtsumme (das Fenster weist darauf hin). Die Wörter und Tokens addieren sich dagegen wie erwartet.

Das Importieren eines Buches oder Textes (ein Archiv, ein Chat aus SillyTavern), das Kopieren eines Buches und das Wiederherstellen einer Sicherung zählen nichts: Der Text wurde hier nicht geschrieben. Was die Agenten tun, wird auf ihrer Seite gezählt (**Agenten**), bezogen auf das Originalbuch, obwohl sie an einer Kopie arbeiten; dazu gehört auch Arbeit, die verworfen wurde oder nie in das Buch übernommen wurde.

<a id="the-tabs"></a>

## Die Reiter

| Reiter | |
|---|---|
| **Tage** | Eine Zeile für jeden Tag, an dem etwas vorgefallen ist: alle obigen Zahlen, neueste zuerst. Klicke auf eine Kopfzeile, um zu sortieren. |
| **Wörter** | Ein Diagramm der geschriebenen, generierten und entfernten Wörter pro Tag. Ein langer Zeitraum wird nach Woche oder Monat zusammengefasst. |
| **Zeit** | Ein Diagramm der Stunden, in denen das Buch offen war, bearbeitet wurde, generiert wurde und ein Agent lief. |
| **Tokens** | Ein Diagramm der gesendeten und generierten Tokens. |
| **Größe** | Die Größe des Buches am Ende jedes Tages und wie stark es jeden Tag gewachsen ist, in **Wörtern** oder in **Tokens**. |
| **Bücher** | (alle Bücher) Eine Zeile für jedes Buch, auch für die im [Papierkorb](../trash.md) oder bereits entfernten, mit Ordner, Zahlen, Zeit, Wachstum und Anteil. **Gelöschte Bücher anzeigen** blendet die Bücher im Papierkorb und die endgültig entfernten aus. Ein Klick auf ein Buch öffnet es. |
| **Nach Buch** | (alle Bücher) Die Bücher als Balken, nach der Zahl, die du bei **Diagramm nach** wählst. Die zehn größten werden gezeigt, der Rest ist ein Balken, *Sonstiges*. |
| **Kalender** | Die Tage, an denen du gearbeitet hast, deine Serien und dein Ziel, siehe unten. |

![Die Zeit pro Tag](../../images/statistics-time.png)

Die Größe eines Buches wird kurz nach einer Änderung aus den Wörtern und Tokens gemessen, die die Abschnitte gespeichert haben. Die Tokens eines alten Abschnitts sind diejenigen, die beim Schreiben gezählt wurden, daher ändert das Wechseln des Modells eines Buches keine Zahl.

<a id="the-calendar"></a>

## Der Kalender

Der Reiter **Kalender** zeigt für jeden Tag des letzten Jahres ein kleines Quadrat (53 Wochen, auf dem Telefon 16), eine Woche pro Spalte, und den neuesten Tag unten rechts, mit einem Rahmen markiert. Je dunkler das Quadrat, desto mehr hast du an diesem Tag getan. Halte den Zeiger über einen Tag, um das Datum, die geschriebenen Wörter, die Zeit der Bearbeitung und den Zeitpunkt zu sehen, an dem du dort zuletzt warst (ein `~` nach einem Tag, der aus der Historie rekonstruiert wurde).

![Der Kalender aller Bücher mit dem Tagesziel](../../images/statistics-global-calendar.png)

Ein Tag zählt als **bearbeitet**, wenn du Wörter geschrieben, einen Abschnitt bearbeitet, einen Abschnitt angelegt oder Zeit mit Bearbeiten verbracht hast. Was die Agenten getan haben, Wörter, die nur generiert wurden, und das Öffnen eines Buches zählen nicht: Der Kalender handelt von *deinem* Schreiben. Ein Quadrat, das nur etwas dunkler ist als ein leeres, ist ein Tag, an dem du gearbeitet, aber wenig geschrieben hast.

Wähle mit **Geschriebene Wörter** oder **Bearbeiten** (Minuten der Bearbeitung), wie die Quadrate eingefärbt werden. Der Kalender zeigt immer die neuesten Wochen, unabhängig vom **Zeitraum**, der darüber gewählt ist.

Oberhalb des Kalenders:

| | |
|---|---|
| **Aktuelle Serie (Tage)** | Die Tage in Folge, an denen du gearbeitet hast, von heute aus rückwärts gezählt. Ein heutiger Tag, an dem du noch nicht gearbeitet hast, unterbricht sie nicht: Die Serie zählt dann ab gestern. |
| **Längste Serie (Tage)** | Die längste Folge von Tagen in Folge, über die gesamte Zeit. |
| **Tage mit Arbeit daran** | Wie viele der angezeigten Tage des Kalenders du bearbeitet hast. |
| **Zielserie (Tage)**, **Längste Zielserie (Tage)** | Wie die obigen Serien, für Tage, die dein Tagesziel erreicht haben (wenn du eines festgelegt hast, im Fenster aller Bücher). |

<a id="the-daily-goal"></a>

### Das Tagesziel

Lege ein Ziel im Reiter **Einstellungen** fest, in den allgemeinen Einstellungen:

| Feld | |
|---|---|
| **Tagesziel: geschriebene Wörter** | Die Wörter, die du an einem Tag schreibst, über alle deine Bücher. Wörter, die die Modelle generieren, zählen nicht. |
| **Tagesziel: Minuten Bearbeitung** | Die Minuten, die du an einem Tag bearbeitest, über alle deine Bücher. |

![Die Ziele in den Einstellungen](../../images/settings-daily-goal.png)

Lass ein Feld leer, wenn du kein Ziel willst. Sind beide gesetzt, erreicht ein Tag das Ziel, wenn er **beide** erreicht. Das Fenster von **allen Büchern** zeigt dann, wie weit du heute bist, als zwei Balken (*Heute: 310 von 500 Wörtern*), die Färbung des Kalenders folgt dem Ziel (ein Tag, der es erreicht hat, ist am dunkelsten), und die **Zielserie** zählt die Tage, die es erreicht haben. Ohne Ziel richtet sich die Färbung nach deinem geschäftigsten Tag im Kalender. Das Fenster eines einzelnen Buches hat kein Ziel (ein Ziel über alle Bücher bedeutet für eines davon wenig): Seine Quadrate werden am geschäftigsten Tag dieses Buches gemessen.

Wenn ein Tag keine erfasste Zeit hat (siehe unten) und der Kalender nach **Bearbeiten** gefärbt wird, ist er nur als Tag markiert, an dem gearbeitet wurde.

![Die Statistik aller Bücher auf dem Telefon](../../images/phone-statistics.png)

<a id="days-before-the-statistics"></a>

## Tage vor der Statistik

Die Statistik wurde vor Version 1.6.0 nicht geführt. Als Marginalia aktualisiert wurde, baute es die früheren Tage aus der Historie der Abschnitte neu auf: Die erste Version eines Abschnitts ist ein Abschnitt, der mit den Wörtern angelegt wurde, die er damals hatte, eine spätere ist eine Bearbeitung, und ein Abschnitt, der nicht mehr existiert, ist eine Löschung. Diese Tage sind eine Schätzung. Sie sind in der Liste der Tage mit einem `~` markiert, und das Fenster sagt, wie viele es sind; die Tokens eines rekonstruierten Tages sind nur eine Untergrenze. Die **Zeit** lässt sich nicht rekonstruieren, weil niemand sie erfasst hat: Das Fenster sagt, ab welchem Tag sie erfasst wird (*Die Zeit wird erfasst ab ...*), die Tage davor zeigen einen Strich und fehlen im Zeitdiagramm. Der **Kalender** zählt einen rekonstruierten Tag wie jeden anderen als bearbeitet.
<a id="privacy-clearing-not-recording-keeping-less"></a>

## Datenschutz: löschen, nicht aufzeichnen, weniger behalten

Die Statistik zeigt, wann Sie an einem Buch gearbeitet haben und wie lange. Sie behält außerdem den Namen und den Ordner jedes Buchs, auch der Bücher, die Sie gelöscht haben. Die Statistik gehört Ihnen, und Sie entscheiden, was davon übrig bleibt.

![Schreibstatistik erfassen und Statistik behalten für, in den Einstellungen](../../images/settings-statistics-privacy.png)

- **Die Statistik dieses Buchs löschen** (Schaltfläche im Fenster eines Buchs) und **Die gesamte Statistik löschen** (im Fenster aller Bücher) entfernen die Zählungen, die Zeiten und die Größen endgültig, nach einer Rückfrage. Die Bücher werden dabei nicht verändert. Das Zählen geht danach bei null weiter.
- In der Liste der Bücher hat ein Buch, das im Papierkorb liegt oder nicht mehr vorhanden ist, die Schaltfläche **Vergessen**. Sie löscht nur die Statistik dieses Buchs.
- **Schreibstatistik erfassen** in den [Einstellungen](../account.md) schaltet das Zählen für Ihre Bücher ab. Was bereits erfasst wurde, bleibt, bis Sie es löschen; das Fenster zeigt an, dass nichts Neues mehr gezählt wird. Ein Administrator kann das Erfassen auch für alle abschalten, mit demselben Hinweis.
- **Statistik behalten für (Monate)** in den Einstellungen löscht die Tage, die älter sind als dieser Wert, einmal am Tag und beim Speichern. Ein leeres Feld oder 0 behält alles. Der Administrator kann für alle eine Grenze festlegen; dann gilt die kürzere der beiden. Von den Größen eines Buchs bleibt der letzte Wert vor der Grenze erhalten, damit das Wachstum am ersten behaltenen Tag weiterhin berechnet werden kann.

Der [Administrator](../administration/statistics.md) kann die Statistik eines Benutzers oder aller Benutzer löschen.

<a id="commands"></a>

## Befehle

Zwei [Befehle](../commands/index.md) lesen dieselben Zahlen: `/get-book-statistics [range] [side]` für das geöffnete Buch, Tag für Tag, und `/get-statistics [range] [by] [side]` für alle Ihre Bücher, nach Tag oder nach Buch. Der Zeitraum ist `today`, `yesterday`, `last-7-days`, `last-30-days`, `this-month`, `all-time`, ein Tag (`2026-03-14`) oder zwei Tage (`2026-03-01..2026-03-14`). Ein [Agent](../agents.md) kann sie ebenfalls verwenden.
