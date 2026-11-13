---
label: Buch exportieren
order: 55
---

# Buch exportieren

Der Export verwandelt die Geschichte in eine Datei, die Sie lesen, drucken, verschicken oder anderswo veröffentlichen können. Anders als ein [Backup](backups.md) enthält er nur den Text, als fertiges Buch.

Exportiert wird nur der [aktive Zweig](branches-and-story-tree.md) – dieselben Teile, die der Geschichten-Editor anzeigt. Um eine andere Fassung der Geschichte zu exportieren, wechseln Sie zuerst auf diesen Zweig.

<a id="exporting"></a>

## Exportieren

1. Öffnen Sie das Buch und wechseln Sie zum Tab **Geschichte**.
2. Öffnen Sie das Einstellungsmenü (⚙) in der unteren Leiste und wählen Sie **Geschichte exportieren**.
3. Füllen Sie den Dialog aus und wählen Sie **Exportieren**.
4. Marginalia sammelt die Teile des Zweigs und schreibt die Datei, was bei einem langen Buch einen Moment dauert. Ein Fortschrittsbalken zeigt, wie weit es ist.
5. Wenn die Datei fertig ist, bietet ein Dialog sie zum Herunterladen an. Wählen Sie die Schaltfläche **Herunterladen**, um sie zu speichern.

![Der Dialog Geschichte exportieren](../../images/export-dialog.png)

| Feld | |
|---|---|
| **Format** | Der Dateityp, siehe unten. |
| **Druckstil** | Nur für die Formate, die Schriften und Farben haben (HTML, Word, PDF, EPUB). Das Aussehen des Textes, gewählt aus den Stilen *für Exporte*: Die Anwendung bringt je einen für jedes dieser Formate mit, **HTML-Seite**, **E-Book (EPUB)**, **PDF-Buch** und **Word-Dokument**, also genau die Aussehen, die Exporte schon immer hatten; sie werden automatisch gewählt, sobald Sie das Format auswählen. Sie können auch eigene Stile anlegen, siehe [Druckstile](#print-styles). Der Stil, den Sie für das Format unter [Einstellungen → Exporte](../account.md#exports) gewählt haben, ist vorausgewählt. |
| **Dateiname** | Name der heruntergeladenen Datei ohne die Endung, die Marginalia hinzufügt. Beginnt mit dem Namen des Buches. |
| **Buchtitel** | Der Titel in der Datei (Titelseite und Dateieigenschaften). Beginnt mit dem Namen des Buches; eine Änderung benennt das Buch nicht um. |
| **Autor** | Beginnt mit Ihrem vollständigen Namen aus Ihrem [Konto](../account.md). Leer bedeutet kein Autor. |
| **Ab Nachricht**, **Bis Nachricht** | Welche Teile exportiert werden. Teile werden ab 1 in der Reihenfolge der Geschichte gezählt; Teile ohne Text werden nicht mitgezählt. Lassen Sie **Bis Nachricht** leer, um bis zum Ende zu exportieren. |
| **Titelseite einfügen** | Beginnt die Datei mit einer ersten Seite, die Titel und Autor enthält. |
| **Urheberangabe einfügen** | Beendet die Datei mit einer kurzen Erklärung, wer die Worte geschrieben hat: wie viele Worte Sie geschrieben haben und wie viele ein Modell erzeugt hat, in Worten und Prozent, mit den Modellen. Manche Verlage und Wettbewerbe verlangen sie. Sie wird aus der [Versionsgeschichte](version-history.md) der exportierten Teile berechnet (siehe [Erzeugten Text hervorheben](story-editor.md#highlighting-generated-text)). Wird für den SillyTavern-Chat nicht angeboten. Der Wortlaut ist eine Vorlage unter [Einstellungen → Exporte](../account.md#exports). |

![Der fertige Export, bereit zum Herunterladen](../../images/export-download.png)

<a id="formats"></a>

## Formate

| Format | |
|---|---|
| **Reiner Text** (`.txt`) | Nur der Text in UTF-8. Formatierungen wie *Kursivschrift* entfallen, Absätze werden durch eine Leerzeile getrennt. |
| **Markdown** (`.md`) | Der Text so, wie er in der Geschichte geschrieben ist, für Editoren wie Obsidian oder Typora, eine statische Website oder ein Repository. Betonungen, Überschriften, Zitate, Listen, Codeblöcke und Szenenwechsel bleiben Markdown. Roh-HTML im Text bleibt wie eingegeben erhalten; ob der Markdown-Leser es anzeigt, entscheidet er selbst. |
| **HTML** (`.html`) | Eine Seite mit eingebettetem buchartigem Stil, damit sie in jedem Browser gleich aussieht. Sie kann gedruckt werden und hat keine externen Dateien. |
| **Word-Dokument** (`.docx`) | Öffnet sich in Word, LibreOffice und ähnlichen Programmen. Blocksatz, mit Titel und Autor in den Dateieigenschaften. |
| **PDF** (`.pdf`) | A5-Seiten mit eingebetteten Schriften, die lateinischen, griechischen und kyrillischen Text abdecken, sowie einer mitgelieferten japanischen Schrift (Noto Sans JP) für Kana, Kanji und japanische Satzzeichen. Text anderer Schriftsysteme (nur chinesische Zeichen, Koreanisch, Arabisch) wird nicht gezeichnet: Für ihn hat das PDF keine Schrift. |
| **EPUB** (`.epub`) | Ein E-Book für E-Reader und Lese-Apps. Lange Bücher werden in mehrere Dateien innerhalb des EPUB aufgeteilt, damit die Leser flink bleiben. |
| **HTML für Archive of Our Own** (`.html`) | Zum Veröffentlichen auf AO3, siehe [unten](#for-other-tools). |
| **Markdown mit Front Matter** (`.md`) | Für Generatoren statischer Websites, siehe [unten](#for-other-tools). |
| **SillyTavern-Chat** (`.jsonl`) | Die Geschichte als Chat, siehe [unten](#for-other-tools). |
| **Obsidian-Vault** (`.zip`) | Die Kapitel und das Lorebook als verlinkte Notizen, siehe [unten](#for-other-tools). |
| **LaTeX** (`.zip`) | Ein LaTeX-Projekt des Buches (`main.tex` und die Bilder), siehe [unten](#for-other-tools). |

Der Text der Teile ist Markdown. Betonungen, Überschriften, Zitate, Listen, Codeblöcke und Szenenwechsel (`---`) bleiben in jedem Format erhalten, das sie darstellen kann. Roh-HTML im Text wird als Text angezeigt und niemals ausgeführt.

<a id="footnotes"></a>

## Fußnoten

Eine Fußnote wird im Text eines Teils als `[^1]` dort geschrieben, wo die Markierung steht, und als `[^1]: die Notiz` in einer eigenen Zeile (die Bezeichnung kann ein beliebiges Wort sein). Alle Formate kennen sie: **Markdown** behält die Syntax bei, **HTML**, **EPUB** und **HTML für Archive of Our Own** setzen eine kleine hochgestellte Zahl, die auf die Notizen unter dem Text des Teils verlinkt (AO3 erhält die einfache Auszeichnung, die sein Editor behält), **reiner Text** schreibt die Markierung als `[1]` und die Notizen als Zeilen, die mit `[1]` beginnen, nach dem Text des Teils, so wie eine Endnote aussieht. **Word** und **PDF** haben echte Fußnoten: Die Markierung ist eine kleine hochgestellte Zahl, und die Notiz steht unten auf derselben Seite in kleinerer Schrift (im PDF unter einer kurzen Linie; die Seiten werden so umbrochen, dass jede Seite den Platz für ihre Notizen hat). Word nummeriert und platziert die Notizen selbst, sie wandern also mit, wenn Sie das Dokument bearbeiten. Auch Erweiterungen können sie nutzen: Die Erweiterung Conlang Translator schreibt die englische Übersetzung jeder Rede als Fußnote.

<a id="images"></a>

## Bilder

[Bilder](story-editor.md#images) der Teile werden dort platziert, wo ihre Position es vorgibt (nach dem Text, sofern Sie keinen anderen Ort gewählt haben), mit ihren Bildunterschriften und in der Breite, die Sie ihnen gegeben haben (ein Prozentwert der Textbreite; ohne Breite behält das Bild seine Größe, auf die Seite verkleinert). Nicht jedes Format kann Text neben einem Bild fließen lassen:

| Format | |
|---|---|
| **HTML**, **EPUB** | Eingebettet (in der Seite oder als Dateien des Buches). Der Text fließt links oder rechts um die Bilder herum; ein Bild in der Mitte steht zwischen zwei Textspalten. Auf einem schmalen Bildschirm stehen sie über dem Text, und ein Leser ohne Spalten (manche E-Reader) zeigt den ersten Text, das Bild und den Rest untereinander. Nebeneinander stehende Bilder an einer Stelle bilden eine Reihe. |
| **Word-Dokument** | Das Bild, um das der Text fließt, steht in einer kleinen Tabelle, die Word seitlich oder in der Mitte platziert; Word umfließt es mit dem Text (bei der Mitte auf beiden Seiten, Zeile für Zeile). Die Bildunterschrift steht unter dem Bild. Bilder mit einer Breite, die nebeneinander passen, bilden eine Zeile einer Tabelle. |
| **PDF** | Der Text fließt seitlich um die Bilder, die Bildunterschrift steht unter dem Bild. Ein Bild in der Mitte steht zwischen zwei Spalten linksbündigen Textes (ein Block, der am Seitenende nicht mehr passt, beginnt auf der nächsten Seite). Bilder mit einer Breite, die nebeneinander passen, bilden eine Reihe. Ein Bild, das für den Rest der Seite zu hoch ist, wandert auf die nächste Seite. |
| **Markdown** | Als `![Bildunterschrift](data:...)` in die Datei eingebettet, die Bildunterschrift kursiv darunter, sodass es weiterhin eine einzige Datei ist. Die Position im Text bleibt erhalten; Breite und Textfluss nicht. Nicht jeder Leser zeigt eingebettete Bilder (GitHub tut es nicht), und ein Buch mit vielen Bildern hat sehr lange Zeilen. Hängen Sie Bilder den Teilen deshalb nur an, wenn Sie sie im Markdown-Export haben möchten. |
| **Reiner Text** | An ihrer Stelle im Text mit `[Bild]` oder `[Bild: Bildunterschrift]` markiert. |
| **HTML für Archive of Our Own** | Mit `[Bild: Bildunterschrift]` markiert: AO3 übernimmt Bilder nur über Links. |
| **Obsidian-Vault** | Im Ordner `Attachments` des Vaults gespeichert und an ihrer Stelle in die Notiz ihres Kapitels eingebettet, mit der Bildunterschrift kursiv. |
| **LaTeX** | Im Ordner `images` des Projekts gespeichert und an ihrer Stelle in den Text eingebunden (als Abbildung in der Breite des Bildes, nie breiter als der Text), mit der Bildunterschrift darunter. Der Text fließt nicht um sie herum. |
| **SillyTavern-Chat** | Weggelassen, ein Chat hat nur Text. |
<a id="for-other-tools"></a>

## Für andere Werkzeuge

Diese Formate sind für ein Werkzeug gedacht, das sie einliest, nicht zum Lesen. Sie haben die Auswahl *Druckstil* nicht.

**HTML für Archive of Our Own.** AO3 verlangt den Titel, den Autor und die Kapiteltitel in eigenen Feldern und verwirft, was es nicht kennt (Stilblöcke, die Seite um den Text, Bilder, die keine Links sind). Deshalb hat die Datei keine Titelseite. Öffne sie in einem Browser, um das Werk so zu sehen, wie es nach dem Einfügen aussehen wird. Jedes Kapitel (ein Teil, der mit einer Überschrift beginnt, und die Teile danach bis zur nächsten Überschrift) hat seinen Titel über zwei Kommentaren, `BEGIN chapter N` und `END chapter N`: Kopiere, was zwischen ihnen steht, in den HTML-Editor des Kapitels auf AO3, und den Titel in dessen Titelfeld. Der Text ist sauberes HTML ohne Zeilenumbrüche zwischen den Tags, weil AO3 jeden Zeilenumbruch in ein `<br>` umwandelt.

**Markdown mit Front Matter.** Dasselbe Markdown wie das Format *Markdown*, aber die Datei beginnt mit einem YAML-Block (`title`, `author`, `lang`, `date` des Exports), den Jekyll, Hugo, Eleventy und ähnliche Generatoren lesen, anstelle einer Titelseite; **Titelseite einfügen** hat darauf keine Wirkung. Der Block ist eine Vorlage, die du unter [Einstellungen → Exporte](../account.md#exports) ändern kannst (`{{title}}`, `{{author}}`, `{{language}}`, `{{exportDate}}`; der Titel und der Autor sind bereits für die Verwendung in doppelten Anführungszeichen maskiert).

**SillyTavern-Chat.** Eine JSON-Lines-Datei, die der Chat-Import von SillyTavern einliest: Der Charakter wird nach dem Buch benannt, und du bist der Autor. Jeder Teil der Geschichte ist eine Nachricht des Charakters (der Text). Wenn der Teil mit Anweisungen geschrieben wurde, stehen diese davor als deine Nachricht. Die Argumentation des Modells, falls der Teil eine hat, wird als Argumentation der Nachricht übernommen. Die Titelseite entfällt.

**Obsidian-Vault.** Ein ZIP mit einem Ordner, der nach dem Buch benannt ist; entpacke es und öffne den Ordner als Vault. Er enthält eine Notiz mit dem Namen des Buches (das Front Matter, die Titelseite sowie die Listen der Kapitel und des Wissens), eine Notiz für jedes Kapitel in `Chapters` (mit Links zum vorherigen und nächsten Kapitel), eine Notiz für jeden aktivierten Eintrag des [Lorebooks](../lorebooks/index.md) des Buches und seiner Unter-Lorebooks in `Lore/<lorebook>` (die Tags des Eintrags sind die Tags der Notiz, sein Kommentar ist ein Callout), und die Bilder in `Attachments`. In jeder Notiz ist die erste Nennung des Namens eines Eintrags (ein Name von mindestens drei Zeichen, ganze Wörter, so wie geschrieben) ein Link zu dessen Notiz, sodass die Graphansicht zeigt, welcher Eintrag in welchem Kapitel vorkommt. Ein Teil mit einer Überschrift beginnt ein Kapitel; Teile vor der ersten Überschrift bilden die Notiz *Beginning* (oder *Story*, wenn das Buch keine Überschriften hat). Das Front Matter und die Titelseite sind Vorlagen, die du unter [Einstellungen → Exporte](../account.md#exports) ändern kannst.

**LaTeX.** Ein ZIP mit einem Ordner, der nach dem Buch benannt ist. `main.tex` ist eine `book`-Klasse, die mit `pdflatex`, `xelatex` oder `lualatex` kompiliert wird, und die Bilder der Teile liegen in `images`. Die Titelseite ist ein `titlepage`, das aus der [Vorlage für die Titelseite](../account.md#exports) gebildet wird. Die oberste Überschriftsebene der Geschichte ist ein Kapitel, die nächste Ebene ein Abschnitt, und so weiter; sie sind nicht nummeriert (eine Geschichte benennt ihre Kapitel selbst), werden aber in einem Inhaltsverzeichnis aufgeführt. Betonungen, Zitate, Listen, Codeblöcke und Szenenwechsel werden zu dem, was sie in LaTeX sind, die Fußnoten sind echte Fußnoten, die Zeichen, die für LaTeX eine Bedeutung haben, werden als Text geschrieben, und die Sprache des Buches wählt die Silbentrennung (`babel`), wenn sie zu den gängigen gehört. Ein Buch mit japanischem, chinesischem oder koreanischem Text braucht `xelatex` oder `lualatex` (`pdflatex` bricht mit einer Meldung ab): Die Datei lädt dafür `xeCJK` oder `luatexja`, und mit `xelatex` legst du eine Schrift fest, die deine Zeichen enthält (die Zeile zum Ändern steht im Kommentar darüber, zum Beispiel `\setCJKmainfont{Noto Serif CJK JP}`); andernfalls wird nur die chinesische Schrift verwendet, die mit `xeCJK` mitgeliefert wird. Lade das ZIP auf [Overleaf](https://www.overleaf.com) hoch oder entpacke es und führe `pdflatex main.tex` aus (zweimal, für das Inhaltsverzeichnis). Der Druckstil entfällt: Das Aussehen bestimmt die Klasse; ändere es in der Präambel von `main.tex`.

<a id="print-styles"></a>

## Druckstile

Der Export verwendet die Textstile aus [Einstellungen → Stile](../account.md#styles), die mit **Für Exporte** markiert sind. Stile sind entweder für Bücher (die Geschichte und der Leser) oder für Exporte: Das Menü *Stile für Text ändern* und der Leser zeigen nur die erste Art, der Export-Dialog nur die zweite. Die vier eingebauten sind die Aussehen der Formate; **Duplizieren** einen davon (oder einen Buchstil und setze das Häkchen bei **Für Exporte**), um deinen eigenen zu erstellen, zum Beispiel den Buchstil auf Papier. Der Stil wird für Papier berechnet: Farben, die vom Design der Anwendung abhängen (die helle Schrift des dunklen Designs), werden für eine weiße Seite zu den dunklen.

| Format | Was der Stil bewirkt |
|---|---|
| **HTML** | Das gesamte Stylesheet des Stils, einschließlich des **benutzerdefinierten CSS**, der **Initialen** (Drop Caps), der Textbreite und des Hintergrunds; die Titelseite und das Inhaltsverzeichnis behalten das Aussehen des Formats. |
| **EPUB** | Das Stylesheet des Stils ohne das benutzerdefinierte CSS (nicht alle E-Reader verstehen es; manche Reader legen außerdem eigene Schriften und Größen fest). |
| **PDF** | Die Schriftfamilie (Serif, Sans oder Monospace der eingebetteten Schriften, aus dem Schriftstapel gewählt), Größe, Zeilenhöhe, Ausrichtung, der Abstand zwischen Absätzen, der Einzug der ersten Zeile, die **Hintergrundfarbe** der Seite (die ganze Seite, auch die Titelseite), die Farben von Text, Kursivschrift, Fettschrift, Zitaten, Überschriften und Szenenwechsel, die Ausrichtung und Größe der Überschriften sowie das Symbol des Szenenwechsels. |
| **Word-Dokument** | Dasselbe wie das PDF, mit der im Stil genannten Schrift (Word zeigt seine eigene, wenn dem Computer die Schrift fehlt) statt der eingebetteten. Der Hintergrund ist die Farbe der Seite: Word zeigt sie auf dem Bildschirm an, druckt sie aber nur, wenn *Hintergrundfarben und -bilder drucken* in den Druckoptionen aktiviert ist. |

Initialen, das benutzerdefinierte CSS (PDF, Word, EPUB), Kapitälchen bei Überschriften und die Textbreite werden in den PDF- und Word-Dateien nicht erzeugt.

<a id="title-page-template"></a>

## Vorlage für die Titelseite

Die Titelseite wird aus einer Vorlage geschrieben, eine für jedes Format. Ändere sie unter [Einstellungen → Exporte](../account.md#exports): Die Vorlage ist Markdown mit `{{title}}` (der Buchtitel des Dialogs) und `{{author}}`, zum Beispiel `## {{title}}` und eine Zeile `geschrieben von {{author}}`. Der Standard ist der Titel als Überschrift und der Autor kursiv.

Dieselben Vorlagen (die Titelseite, das Front Matter, die Urheberangabe) können **für ein Buch oder für einen Ordner** geändert werden: Im Reiter *Prompts* des Buches und im Fenster des Ordners hat **Einstellungen der Erweiterung** einen Bereich für jedes Exportformat, mit einem Feld für jede seiner Vorlagen. Es wird zuerst die eigene Fassung des Buches verwendet, sonst die des nächsthöheren Ordners, sonst deine aus *Einstellungen → Exporte*, sonst der Standard, sodass eine Reihe eine eigene Titelseite haben kann. Ein leeres Feld wird übernommen und zeigt an, woher.
<a id="table-of-contents"></a>

## Inhaltsverzeichnis

Enthält der Text Kapitel, erhält die Datei nach der Titelseite ein Inhaltsverzeichnis. Ein Kapitel ist ein Teil der Geschichte, der eine Markdown-Überschrift enthält (eine Zeile, die mit `#` beginnt, z. B. `# Chapter 3: The Harbour`). Dieselbe Regel verwendet das Plugin [Chapter Marker](../extensions/chapter-marker.md) in der Story-Seitenleiste. Die erste Überschrift eines Teils ist sein Titel; Teile ohne Überschrift gehören zum davorliegenden Kapitel. Eine Geschichte ohne jegliche Überschrift hat kein Inhaltsverzeichnis.

![Die Titelseite und das Inhaltsverzeichnis eines exportierten PDFs](../../images/export-result.png)

In PDF-, Word- und EPUB-Dateien beginnt jedes Kapitel auf einer neuen Seite. Auch die Titelseite und das Inhaltsverzeichnis sind jeweils eigene Seiten.

Die Einträge sind Links zu den Kapiteln:

| Format | |
|---|---|
| **HTML** | Verlinkt auf die Kapitel der Seite. |
| **PDF** | Anklickbare Einträge mit der Seitenzahl des Kapitels; die Kapitel stehen außerdem in der Gliederung (Lesezeichen), die Ihr PDF-Reader neben den Seiten anzeigt. |
| **EPUB** | Das Inhaltsverzeichnis des Readers, das zugleich eine Seite des Buches nach der Titelseite ist. |
| **Word-Dokument** (`.docx`) | Anklickbare Einträge mit der Seitenzahl des Kapitels. Word fragt beim Öffnen der Datei, ob die Felder aktualisiert werden sollen; antworten Sie mit **Ja**, um die Seitenzahlen einzutragen (LibreOffice trägt sie selbstständig ein). Es handelt sich um eine Liste von Links und nicht um ein Word-Inhaltsverzeichnisfeld. |
| **Markdown** | Kein Inhaltsverzeichnis. Die Kapitelüberschriften bleiben im Text, und die meisten Markdown-Editoren erstellen daraus eine Gliederung. |
| **Nur Text** | Kein Inhaltsverzeichnis. |

<a id="page-numbers"></a>

## Seitenzahlen

PDF- und Word-Dateien zeigen auf jeder Seite außer der Titelseite die Seitenzahl in der Fußzeile. Die Zahl entspricht der Position der Seite in der Datei: Die Titelseite ist Seite 1, die erste Seite danach ist Seite 2. Die anderen Formate haben keine eigenen Seiten.

<a id="the-title-page"></a>

## Die Titelseite

Mit **Titelseite einfügen** beginnt die Datei mit dem Buchtitel und dem Autor auf einer eigenen Seite. Die Seite wird aus einer eingebauten Vorlage (`DEFAULT_EXPORT_HEADER_TEMPLATE`) erzeugt, die in der Anwendung derzeit nicht bearbeitet werden kann.

<a id="good-to-know"></a>

## Gut zu wissen

- Der Export ist eine Momentaufnahme des Zweigs zum Zeitpunkt, an dem Sie **Exportieren** wählen. Teile, die danach erzeugt oder bearbeitet werden, sind nicht in der Datei enthalten.
- Die exportierte Datei wird nur so lange aufbewahrt, bis Sie den Download-Dialog schließen. Exportieren Sie erneut, wenn Sie sie später brauchen.
- Zusammenfassungen, Lorebooks, Anweisungen und die Überlegungen des Modells werden nicht exportiert, nur der Text und die Bilder der Teile. Um alles zu sichern, erstellen Sie ein [Backup](backups.md).
