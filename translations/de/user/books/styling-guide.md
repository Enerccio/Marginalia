---
label: Stil-Leitfaden
order: 54
---

# Stil-Leitfaden: Ihr Buch soll wie Ihr Buch aussehen

Ein **Textstil** legt fest, wie der Text Ihrer Teile aussieht: die Schrift, die Farben, die Breite der Spalte, die Überschriften und die Linie
zwischen Szenen. Dieser Leitfaden führt Sie von der ersten Änderung bis zu einer fertigen Gestaltung und endet mit einem vollständigen Beispiel
zum Herunterladen: einem Stil für ein Horrorbuch, der jede Option und etwas eigenes CSS nutzt.

Wo ein Stil verwendet wird:

| Wo | Was passiert |
|---|---|
| **Der Story-Editor** | Der Stil des Buches (oder Ihr Standard) ist das Aussehen jedes Teils. Siehe [Textstile](story-editor.md#text-styles). |
| **Der Leser** | Ein [veröffentlichtes Buch](publishing.md) wird in dem Stil gelesen, den der *Leser* aus seinen eigenen Stilen gewählt hat (bis er wählt, der Buchstil). |
| **Exporte** | Ein Stil, der als **Für Exporte** markiert ist, wird im [Exportdialog](export.md#print-styles) gewählt. HTML und EPUB übernehmen das Stylesheet, PDF und Word die Zahlenwerte (siehe [was jeder Export übernimmt](#what-the-exports-take)). |

<a id="make-your-first-style"></a>

## Den ersten Stil erstellen

1. Öffnen Sie **Einstellungen → Stile** (der [Reiter Stile](../account.md#styles)).
2. Wählen Sie **Buch** in der Liste und drücken Sie **Duplizieren**. Die eingebauten Stile können nicht geändert werden, deshalb beginnen Sie immer mit einer Kopie namens
   *Buch (copy)*. Geben Sie ihr einen Namen.
3. Ändern Sie einige Felder auf dem Reiter **Optionen**. Die **Vorschau** unter dem Formular zeigt einen Beispieltext (einen Kapiteltitel, Betonung, ein Zitat, einen Szenenwechsel,
   eine kleinere Überschrift) und folgt jeder Änderung, während Sie tippen.
4. Drücken Sie **Speichern** in den Einstellungen. Vorher wird nichts gespeichert, und **Änderungen verwerfen** wirft die Arbeit weg.
5. Öffnen Sie im Story-Editor das Einstellungsmenü (⚙) → **Darstellung** → **Stile für Text ändern** und setzen Sie den Haken bei Ihrem Stil. Er wird im
   Buch gespeichert. Um den Stil für alle Ihre Bücher zu verwenden, wählen Sie ihn in der Liste aus und drücken Sie **Als Standard festlegen**.

![Die Stile im Menü der Geschichte](../../images/story-style-menu.png)

Ein leeres Feld behält das Aussehen der Anwendung, daher legt ein Stil nur fest, was Sie anders möchten. Ein Stil mit lauter leeren Feldern ist das schlichte Aussehen.

<a id="the-options"></a>

## Die Optionen

Jedes Feld nimmt einen CSS-Wert an. Einheiten, die Sie verwenden werden: `rem` (die Größe des Textes der Seite; 1rem sind normalerweise 16px), `em` (die Größe des
aktuellen Textes), `px`, `%`, und Farben als `#336699`, `rgb(51, 102, 153)` oder einen Namen wie `ivory`.

### Text

| Option | Was sie bewirkt | Beispiel |
|---|---|---|
| **Schriftfamilie** | Die Schrift des Textes. Geben Sie eine Liste an: Die erste, die der Computer hat, wird verwendet; die letzte sollte eine generische Familie sein (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Schriftgröße** | Die Größe des Textes. | `1.1rem` |
| **Zeilenhöhe** | Der Abstand zwischen den Zeilen. Eine reine Zahl ist ein Vielfaches der Schriftgröße. | `1.7` |
| **Buchstabenabstand**, **Wortabstand** | Zusätzlicher Abstand zwischen Buchstaben, zwischen Wörtern. Kleine Werte: Ein winziger Buchstabenabstand lässt den Text luftiger wirken. | `0.01em`, `0.05em` |
| **Textfarbe** | Die Farbe des Textes. | `#cfc6bb` |
| **Textausrichtung** | `left`, `right`, `center` oder `justify`. | `justify` |
| **Silbentrennung** | Trennt lange Wörter am Zeilenende. Sie nutzt das Wörterbuch der [Sprache](managing-books.md#the-about-tab) des Buches, stellen Sie also zuerst die Sprache ein. Am besten zusammen mit *Blocksatz*. | an |
| **Absatzabstand** | Der Abstand unter einem Absatz. Ein Buch hat meist wenig, oder keinen, bei einem Einzug der ersten Zeile. | `0.3em` |
| **Einzug der ersten Zeile** | Rückt die erste Zeile eines Absatzes ein, wie in einem gedruckten Buch, außer nach einer Überschrift oder einem Szenenwechsel. | `1.5em` |
| **Initiale** | Ein großer erster Buchstabe des ersten Absatzes nach einer Überschrift. | an |

<a id="page"></a>

### Seite

| Option | Was sie bewirkt | Beispiel |
|---|---|---|
| **Textbreite** | Eine Spalte eigener Breite, zentriert. Eine Zeile mit 60 bis 75 Zeichen liest sich am besten, etwa `34em` bis `40em`. | `38em` |
| **Hintergrundfarbe** | Die Farbe hinter dem Text; sie füllt den ganzen Bereich, nicht nur die Textblöcke. Wählen Sie eine Textfarbe, die darauf gut lesbar ist. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### Betonung, Links und Code

| Option | Was sie bewirkt |
|---|---|
| **Farbe der Betonung** | Die Farbe von *kursivem* Text. |
| **Farbe für Fettdruck** | Die Farbe von **fettem** Text. |
| **Linkfarbe** | Die Farbe von Links. |
| **Schriftfamilie für Code** | Die Schrift von `code` und von Codeblöcken. |

<a id="quotes"></a>

### Zitate

| Option | Was sie bewirkt |
|---|---|
| **Farbe des Zitats** | Die Farbe des Textes eines Zitats (`> a line`). |
| **Farbe des Zitatrahmens** | Die Farbe des Balkens an der Seite eines Zitats. |

<a id="headings"></a>

### Überschriften

| Option | Was sie bewirkt |
|---|---|
| **Schriftfamilie der Überschriften** | Die Schrift der Überschriften `#`, `##` und `###`. |
| **Größe der Überschriften** | Die Größe der ersten Ebene; die tieferen sind im Verhältnis kleiner. |
| **Farbe der Überschriften** | Die Farbe der Überschriften. |
| **Ausrichtung der Überschriften** | `left`, `center` oder `right`. |
| **Kapitälchen für Überschriften** | Setzt die Überschriften in Kapitälchen. |

<a id="scene-break"></a>

### Szenenwechsel

Eine Zeile mit `---` im Text ist ein Szenenwechsel. Standardmäßig sind es drei Punkte. **Text des Szenenwechsels** ist, was stattdessen angezeigt wird
(`* * *`, `~`, `† † †`), **Farbe des Szenenwechsels** seine Farbe.

<a id="custom-css"></a>

## Eigenes CSS

Die Optionen decken das Übliche ab. Für alles Weitere nimmt der Reiter **Eigenes CSS** eigenes CSS entgegen, das nach allem anderen angefügt und nur
innerhalb des Stils angewendet wird. Zwei Arten, es zu schreiben:

- **Einfache Deklarationen** gelten für den ganzen Bereich, der den Text enthält:

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Regeln** mit einem Selektor. Schreiben Sie sie mit `&` (dem Bereich des Stils) davor oder ohne es: In beiden Fällen erreichen sie nur Text, der diesen Stil hat.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

Der Text eines Teils steht in einem Element mit der Klasse `chat-message-markdown`, daher beginnt ein Selektor für etwas im Text dort. Das Feld
**Generiertes CSS** unter dem CSS-Feld zeigt das Stylesheet, das die Optionen erzeugen: Kopieren Sie dessen Selektoren, um zu sehen, was Sie überschreiben können.

| Sie möchten ändern | Selektor (nach `& .chat-message-markdown`) |
|---|---|
| Absätze | `p` |
| Überschriften | `h1`, `h2`, `h3` |
| Kursiv, Fettdruck | `em`, `strong` |
| Links | `a`, `a:hover` |
| Zitate | `blockquote` |
| Listen | `ul`, `ol`, `li` |
| Code | `code` (im Text), `pre` (Block) |
| Szenenwechsel | `hr`, `hr::after` (das Symbol) |
| die erste Zeile eines Absatzes | `p::first-line` |
| markierter Text | `::selection` |

Wissenswert:

- **Die geschweiften Klammern `{ }` müssen zusammenpassen**, sonst wird der Stil beim Speichern abgelehnt. Kommentare `/* ... */` sind erlaubt.
- Einige Regeln, die die Anwendung benötigt, sind `!important` (der Text wird nie breiter als sein Bereich, lange Wörter brechen um, Absätze behalten ihre Zeilenumbrüche). Ihr CSS kann diese nicht überschreiben; es kann alles andere überschreiben, weil es zuletzt kommt.
- CSS kann ändern, wie etwas aussieht, nicht, was es sagt. Es erreicht nicht die Werkzeugleisten und Reiter um den Text herum.
- Eigenes CSS wird in der Geschichte, beim Leser und in HTML-Exporten verwendet. EPUB, PDF und Word übernehmen es nicht.

<a id="what-the-exports-take"></a>

## Was die Exporte übernehmen

Ein Stil für Exporte wird im [Exportdialog](export.md#print-styles) gewählt; **Duplizieren** Sie einen Stil und setzen Sie den Haken bei **Für Exporte**, oder beginnen Sie mit einem der vier,
die die Anwendung mitbringt. Farben, die vom Design der Anwendung abhängen (der helle Text des dunklen Designs), werden beim Export zu dunklen Farben für
eine weiße Seite.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| Schriften, Größen, Zeilenhöhe, Abstände, Ausrichtung, Einzug der ersten Zeile | ✓ | ✓ | ✓ | ✓ |
| Farben von Text, Betonung, Fettdruck, Zitaten, Überschriften, Szenenwechsel | ✓ | ✓ | ✓ | ✓ |
| Hintergrundfarbe | ✓ | ✓ | ✓ | ✓ (die Farbe der Seite) |
| Textbreite, Initialen, Silbentrennung, Kapitälchen der Überschriften | ✓ | ✓ | | |
| eigenes CSS | ✓ | | | |

E-Book-Reader verwenden oft ihre eigenen Schriften, Größen und Farben, egal was das Stylesheet sagt. PDF und Word können nur die Schriften verwenden, die das Format bietet: Das PDF wählt Serif, Sans oder Monospace aus dem Stapel, den Sie geschrieben haben; Word nennt die erste Schrift des
Stapels und zeigt eine eigene, wenn dem Computer diese fehlt.
<a id="share-a-style-export-and-import"></a>

## Styles teilen: Exportieren und Importieren

Unter der Liste der Stile speichert **Exportieren** den ausgewählten Stil als Datei (`name.style.json`; auch ein eingebauter Stil kann exportiert werden), und **Importieren**
liest eine solche Datei ein. Ein importierter Stil wird als neuer Stil zu deinen eigenen Stilen hinzugefügt; er wird erst gespeichert, wenn du **Speichern** drückst, und er behält seinen Namen. Benenne ihn also um, wenn du bereits einen ähnlichen hast.

Die Datei ist JSON:

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

Sie kann mehrere Stile enthalten, und du kannst sie auch von Hand schreiben. Die Optionen heißen in Großbuchstaben (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT` ...); das [Horror-Beispiel](#an-example-a-horror-book) enthält alle. Eine Option, die diese Version von Marginalia nicht kennt, wird weggelassen (du bekommst die Anzahl angezeigt), eine Datei aus einem neueren Formatstand wird abgelehnt, und ebenso ein Stil, dessen eigenes CSS unausgeglichene geschweifte Klammern hat.

<a id="an-example-a-horror-book"></a>

## Ein Beispiel: ein Horrorbuch

[Horror-Stil herunterladen](../../files/horror-book.style.json) (`horror-book.style.json`), in *Einstellungen → Stile* importieren und auswählen, um ihn in der Vorschau zu sehen. Er setzt **jede Option** und verwendet eigenes CSS für das, was die Optionen nicht abdecken:

| | |
|---|---|
| **Seite** | Fast schwarzer Hintergrund `#0c0a0a`, eine Spalte von `38em` und eine dunkle Vignette, die von den Rändern her einengt (eigenes CSS). |
| **Text** | Garamond mit `1.12rem`, Zeilenhöhe `1.8`, Blocksatz mit Silbentrennung, ein Einzug der ersten Zeile von `1.6em` und fast kein Abstand zwischen den Absätzen, wie in einem gedruckten Roman. Der Text hat das Grau alten Papiers `#cfc6bb`, und nach jeder Überschrift steht ein Initial. |
| **Betonung** | Kursiv in staubigem Rosé `#b99a94`, fett in Blutrot `#b3261e`. |
| **Links** | Kränklich Grün `#8fa07e`, das rot und wellig wird, wenn der Zeiger darauf liegt (eigenes CSS). |
| **Zitate** | Grauer Text mit einem dunkelroten Balken `#5c1010` und einem schwachen roten Schleier dahinter (eigenes CSS). |
| **Überschriften** | Palatino in Kapitälchen, zentriert, dunkelrot `#8e1b17`, mit einem schwachen Leuchten (eigenes CSS). |
| **Szenenwechsel** | `† † †` in Dunkelrot; Listen bekommen ein Kreuz statt eines Punkts (eigenes CSS). |
| **Code** | Courier New auf fast schwarzem Hintergrund. |

![Eine Geschichte im Horror-Stil](../../images/story-style-horror.png)

Um es an einem echten Text auszuprobieren, füge dies in einen Teil ein (oder die Vorschau zeigt ein ähnliches Beispiel):

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

Ändere danach: Mach den Hintergrund `#14100c` für ein kerzenbeleuchtetes Braun, setze **Textbreite** auf `32em` für eine schmalere Seite, oder entferne das eigene CSS, um zu sehen, was die Optionen allein bewirken. Wenn dir das Ergebnis gefällt, **Exportieren** es und bewahre die Datei zusammen mit deinem Buch auf.
