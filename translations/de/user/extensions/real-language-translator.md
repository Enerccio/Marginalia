---
label: Realsprach-Übersetzer
order: 3
---

# Realsprach-Übersetzer

Lässt die Figuren deiner Geschichte eine **echte Sprache** sprechen: Eine Geschichte, die in Japan spielt, kannst du auf Englisch schreiben, und die direkte Rede der Figuren wird zu authentischem Japanisch (Kana und Kanji oder Romaji), wobei das Englische dahinter erhalten bleibt. Es ist der [Conlang-Übersetzer](conlang-translator.md) für eine existierende Sprache: dieselbe Idee und dieselben Bildschirme, aber ohne Lexikon.

!!!
Der Text der Geschichte enthält die **Übersetzung**. Alles, was die Geschichte für ein Modell liest (die nächste Generierung, die Zusammenfassungen, die Suche im Lorebook, die Kommentare, die anderen Erweiterungen), liest das **Englische**, aus dem die Rede übersetzt wurde. Du schreibst und steuerst die Geschichte also auf Englisch. Ein Klick auf eine übersetzte Rede zeigt das Englische.
!!!

!!!warning In Arbeit
Die Übersetzung von Teilen, Bereichen und Sätzen, die Einstellungen, der Reiter des Buches, die Ansicht, der Export und die [Befehle](#commands) sind vorhanden. Ein zweiter Durchgang, der die Übersetzung überarbeitet, mehrere Realsprachen in einem Buch und eine Sprache pro Figur sind noch nicht vorhanden, siehe [Grenzen](#limits).
!!!

<a id="the-idea"></a>

## Die Idee

1. Eine **Sprache** ist eine kurze Sammlung von Regeln für den Übersetzer: die **Schrift** (Kana und Kanji, nur Kana, Romaji), ob die **Ehrenbezeichnungen** bei Namen geschrieben werden, wie die **Sprechebene** anhand der Beziehung zwischen Sprecher und Zuhörer gewählt wird, wie **Namen** geschrieben werden und alles andere, woran du dich halten willst. Das Modell kennt die Sprache selbst, deshalb gibt es kein Lexikon. Sprachen werden einmal in deinen Einstellungen gespeichert, und beliebig viele Bücher nutzen sie.
2. Ein **Buch** (oder ein Ordner mit Büchern) wählt seine Sprache aus. Ein Teil wird **von Hand** über sein Menü übersetzt, **von selbst**, sobald er fertig ist, oder **bereichsweise**.
3. Eine Anfrage an das Modell pro Teil klassifiziert jede direkte Rede (wird sie in der Sprache gesagt oder auf Englisch, von dieser Figur, und vom Zuhörer verstanden?) und übersetzt die Rede, die in der Sprache gesagt wird, in der Stimme und der Sprechebene, die die Figur und die Beziehung verlangen.
4. Das Englische bleibt erhalten (in den Daten des Buches, als *Quelle* der Rede) und wird von jedem Modell gelesen. Die Übersetzung ist das Original: Was du von Hand daran änderst, bleibt erhalten, und ein Teil, das du bearbeitest, wird nie hinter deinem Rücken überschrieben.

Die Erweiterung prüft kostenlos, was sie kann: Die zitierte Rede muss im Teil stehen, die Sprache muss die des Buches sein, und die Übersetzung muss in der **Schrift** sein, die du angefordert hast (eine Romaji-Übersetzung mit Kanji darin oder eine Nur-Kana-Übersetzung mit Kanji wird behalten und *gemeldet*). Das Japanische selbst kann sie nicht prüfen: Lies vor einem langen Lauf ein paar Teile.

<a id="languages"></a>

## Sprachen

Eine Sprache bearbeitest du unter **Einstellungen → Einstellungen der Erweiterung → Realsprach-Übersetzer**.

| Schaltfläche | |
|---|---|
| **Hinzufügen** | Eine neue Sprache. Sie beginnt als eine der **Vorlagen** (Japanisch in Kana und Kanji, nur Kana, Romaji oder eine andere Sprache), die die Felder ausfüllt; ändere, was du möchtest. |
| **Bearbeiten**, **Löschen**, **Nach oben**, **Nach unten** | Die ausgewählte Sprache. Ein Löschen fragt nach. |
| **Duplizieren** | Eine unabhängige Kopie mit neuem Namen. Ein Buch, das eine Sprache wählt, teilt sie mit jedem anderen Buch, das sie wählt; eine Kopie ist der Weg, eine Sprache nur für ein Buch zu behalten. |

Der Editor hat diese Felder:

![Die Sprachen in den Einstellungen](../../images/plugin-reallang-settings.png)

| Feld | |
|---|---|
| **Name** | Wie du sie nennst („Japanisch, Romaji für den Roman“). |
| **Die Sprache, wie das Modell sie nennt** | Das Wort, mit dem das Modell für eine Rede in der Sprache antwortet („Japanisch“); leer: der Name. |
| **Schrift** | *Kana und Kanji*, *Nur Kana*, *Romaji (lateinische Buchstaben)* oder *Andere Schrift (nicht geprüft)*. |
| **Ehrenbezeichnungen von Namen schreiben** | san, sama, kun... |
| **Auch die Lesung (Romaji) schreiben** | Das Modell schreibt zusätzlich die Lesung der Übersetzung; das Popover zeigt sie über dem Englischen. |
| **Höflichkeit und Sprechebene**, **Namen von Personen und Orten**, **Weitere Regeln** | Kurze Texte für den Übersetzer. Die japanischen Vorlagen bringen einen Text für die ersten beiden (die einfache Form unter engen Gleichen, die höfliche Form mit Fremden, Keigo gegenüber Vorgesetzten; ein fremder Name in Katakana). |

![Der Editor einer Sprache](../../images/plugin-reallang-editor.png)

Unter der Liste stehen die Einstellungen, die Bücher und Ordner **übernehmen**: die Sprache, ob ein Teil von selbst übersetzt wird, und das Modell und das Protokoll der Übersetzung. Speichere den Bildschirm **Einstellungen** mit **Speichern**, um deine Änderungen zu behalten.

<a id="choosing-the-language-of-a-book"></a>

## Die Sprache eines Buches wählen

Der Reiter **Prompts** im Fenster des Buches enthält unter *Einstellungen der Erweiterung* die Felder der Erweiterung; das Fenster des Ordners hat dieselben. Leer bedeutet *übernehmen*: zuerst der Ordner darüber, dann deine Einstellungen.

| Feld | |
|---|---|
| **Sprache der Rede** | Die Sprache, in die das Buch übersetzt wird. Leer: Das Buch wird nicht übersetzt. |
| **Neuen Teil von selbst übersetzen** | Ein Teil wird übersetzt, wenn er fertig ist: nach einer Generierung, nach der automatischen Kommentierung und Überarbeitung, falls das Buch sie hat, als Schritt von [was dem Teil folgt](../books/story-editor.md#what-follows-a-generated-part) (das Panel der Läufe zeigt ihn, sein **Stopp** hält ihn an). |
| **Modell der Übersetzung**, **Protokoll der Übersetzung** | Leer: das Modell und das Protokoll des Buches, sonst deine Standardwerte. |
| **Leser des veröffentlichten Buches sehen** | Was die [Ansicht](#what-the-reader-sees) zeigt. |

![Die Felder eines Buches](../../images/plugin-reallang-book.png)

<a id="the-real-language-tab-of-the-book"></a>

## Der Reiter Realsprache des Buches

Das Fenster der Geschichte hat einen Reiter **Realsprache** (im selben Dock wie die Kapitel und das Lorebook) für die Sprache *dieses* Buches, mit **Aktualisieren**, um sie neu einzulesen.

![Der Reiter Realsprache der Geschichte, mit den Sprechern und ihrem Verstehen](../../images/plugin-reallang-tab.png)

**Sprecher.** Wer die Sprache **in diesem Buch** spricht (ein Buch mit [mehr als einer Sprache](#more-than-one-language-in-a-book) hat für jede einen Reiter *Sprecher*). Standardmäßig spricht sie jeder außer den aufgelisteten Figuren (oder nur die aufgelisteten). Der Text hebt dies auf („sie sagte auf Englisch“, „die Wache verstand kein Wort“). Notiere bei jeder Figur das **Geschlecht** und die **Stimme**: Die Sprechebene richtet sich danach. Schreibe hier Alter, Rang und wie die Figur mit anderen spricht („ein junger Offizier, höflich zu Vorgesetzten, schroff zu Freunden“): Damit kann der Übersetzer zwischen der einfachen Form und Keigo wählen. Die Sprecher gehören zum Buch.

Jede Figur kann außerdem eine Stufe des **Verstehens** haben: *versteht nichts*, *kennt ein paar Wörter*, *folgt dem meisten* oder *folgt allem*. Lass es leer (oder *folgt allem*), und den Modellen wird nichts mitgeteilt. Nutze es für die Menschen, die die Sprache hören, ohne sie zu sprechen: *Tomáš: versteht nichts (Japanisch)*, *andere Dorfbewohner: folgen dem meisten* (der Name ist nur Text, deshalb funktioniert auch eine Gruppe). Die Stufen sind **für das ganze Buch fest** und werden den Modellen als Information übergeben; was eine Figur damit tut, bleibt dem Modell überlassen, und du kannst in den Anweisungen immer mehr sagen („Tomáš versteht nicht, was sie sagen“). Sie erreichen die Modelle auf zwei Wegen: Das **Erzählmodell** erhält eine kurze Nachricht mit den Stufen (in den Prompt eingefügt wie die Autorennotiz; das Kästchen über der Liste schaltet sie aus, und die Zahl gibt an, wie viele Nachrichten vom Ende sie gerechnet wird), und das Modell, das **einen Teil prüft** (*Mit KI kommentieren*), erhält sie als Abschnitt der Fakten, sodass es eine Figur markieren kann, die auf Worte reagiert, die sie nicht verstehen kann. Wenn du den Text lieber selbst platzieren willst, schalte das Einfügen aus und verwende in einer Vorlage das [Makro](../templates/macros.md#commands) {%{`{{command::reallang-understanding}}`}%}; es nimmt eine optionale Liste der Figuren, die behalten werden sollen.

**Sprachen.** Die anderen Sprachen des Buches neben der, die das Feld **Sprache der Rede** nennt: siehe [unten](#more-than-one-language-in-a-book).

**Schnellübersetzung.** Schreibe einen Text auf Englisch, optional wer ihn sagt und zu wem, und drücke **Übersetzen**: Das Modell übersetzt ihn mit den Regeln der Sprache (bei mehreren Sprachen wählst du, welche). Nichts wird in die Geschichte geschrieben; du kopierst die Übersetzung.

**Glossar.** Wie die Namen und besonderen Wörter der Geschichte in der Sprache geschrieben werden (ein [Abschnitt weiter unten](#the-glossary-of-names-and-special-words)); ein Reiter *Glossar* für jede Sprache des Buches.
<a id="the-glossary-of-names-and-special-words"></a>

## Das Glossar der Namen und Sonderwörter

Ohne Glossar wählt das Modell bei jedem Teil neu, wie ein Name geschrieben wird: `Saellen` wird in einem Teil zu `サエレン`, im nächsten bleibt es in lateinischen Buchstaben, `Tomáš` wird mal zu `トマーシュ`, mal zu `トマシュ`. Das Glossar legt die Wahl einmal fest. Ein Eintrag hat

- das **Wort**, so wie die Geschichte es schreibt (`Saellen`, `witch`); es ist der Schlüssel des Eintrags, Groß- und Kleinschreibung spielen keine Rolle;
- **wie es in der Sprache geschrieben wird** (`サエレン`, `魔女`), oder *keep it as it is* bei einem Titel, einem Ort oder einem erfundenen Begriff, der nicht übersetzt wird;
- **wie es gelesen wird** (optional, bei einer Sprache mit Lesung: `魔女` gelesen `ウィッチ`), und eine **Notiz** für den Übersetzer (die Anrede, die die Figur bekommt, die Reihenfolge des Familiennamens);
- ob er **gilt**.

**Drei Ebenen, zusammengeführt.** Das Glossar einer Sprache liegt auf drei Ebenen: der **Sprache** (in ihrem Editor, in *Settings → Extension Settings*: die Namen und Begriffe aller Bücher in dieser Sprache), den **Ordnern**, in denen das Buch liegt (eine Reihe legt die Namen fest, die ihre Bücher gemeinsam haben; die Ordner können ineinander liegen) und dem **Buch** (dem eigenen, das am meisten benutzt wird). Das Modell bekommt die *Vereinigung*; bei demselben Wort gewinnt die **nächste Ebene**, sodass ein Buch einen Namen anders schreiben kann als sein Ordner. Der Reiter *Glossary* des Tabs *Real language* zeigt alle Einträge mit der Ebene, aus der sie stammen, und ändert einen Eintrag dort, wo er liegt. Ein Wort einer höheren Ebene, das dieses Buch nicht verwenden soll, wird **für dieses Buch ausgeschaltet** (ein Eintrag des Buchs, der aus ist; wieder einschalten geht auf dieselbe Weise, oder löschen, um das Wort der höheren Ebene zurückzubekommen). Ein Eintrag des Buchs kann **nach oben verschoben** werden, in einen Ordner oder in die Sprache, wenn er sich als gängig erweist. Das Glossar eines Ordners und eines Buchs wird **für jede Sprache** des Buchs geführt, weil ein Name in jeder Sprache anders geschrieben wird.

**Was die Übersetzung damit macht.** Das Glossar ist Teil der Anfrage, also schreibt das Modell die Wörter so, wie sie aufgeführt sind. Ein Glossar mit bis zu 100 Einträgen geht bei jeder Anfrage vollständig mit (ein Server, der den Prompt zwischenspeichert, liest es einmal); ein größeres geht mit den Einträgen, die der Teil hat. Nach der Antwort **prüft** das Plugin sie: Eine Rede, deren englischer Text ein Wort des Glossars enthält, deren Übersetzung es aber nicht so schreibt, wie das Glossar es vorsieht, bleibt stehen und wird **gemeldet** (der Autor entscheidet). Ein Name, für den das Modell eine Schreibweise **wählen musste** und der im Glossar fehlt, wird dem Glossar des Buchs als **Vorschlag** hinzugefügt (die Spalte *Status* zeigt *Proposed*). Das Modell verwendet ihn von da an. *Accept* heißt, du hast ihn dir angesehen, und *Bearbeiten* ändert ihn. Ein Wort, das eine Ebene schon hat (auch ausgeschaltet), wird nie wieder vorgeschlagen.

![Das Glossar des Buchs: die Einträge der drei Ebenen, ein vom Modell vorgeschlagener Name wartet auf Annahme](../../images/plugin-reallang-glossary.png)

![Ein Eintrag des Glossars](../../images/plugin-reallang-glossary-entry.png)

![Das Glossar am Ende des Editors einer Sprache](../../images/plugin-reallang-editor-glossary.png)

Die Befehle dafür stehen in der Tabelle unten. Ein Teil, den ein Buch übersetzt hat, bevor das Glossar existierte, behält seine Übersetzung; übersetze ihn erneut, um das Glossar zu nutzen.

<a id="extracting-the-words-by-model"></a>

## Wörter per Modell extrahieren

Das Glossar von Hand zu schreiben, ist bei einem langen Buch langsam. Der Reiter **Word extraction** (neben den Glossar-Reitern des Tabs *Real language*) lässt das Modell einen **Bereich von Teilen** lesen und die Namen und Sonderwörter auflisten, die es findet, jeweils mit der Form, die sie in der Sprache haben. Alles, was es findet und das Glossar noch nicht hat, wird dem Glossar des Buchs als **Vorschlag** hinzugefügt: Das Modell verwendet es ab der nächsten Anfrage, und du nimmst es an, bearbeitest es oder löschst es im Reiter *Glossary*. An der Geschichte ändert sich nichts.

![Der Reiter Word extraction nach einem Lauf: die Begriffe, die das Modell gefunden hat, stehen als Vorschläge im Glossar](../../images/plugin-reallang-extraction.png)

- **Sprache.** Bei mehr als einer Sprache im Buch die Sprache, für die das Glossar bestimmt ist.
- **Prompt.** Anweisungen für das Modell: wonach es suchen soll und wie jede Art von Wort in der Sprache geschrieben wird. Wähle einen aus der Bibliothek oder *None: only my specification*. Das Plugin bringt einen Prompt für Japanisch mit (reale Orte und Organisationen in ihrer üblichen japanischen Form, erfundene fremde Namen in Katakana, japanische Namen in Kanji mit der Lesung, erfundene Begriffe der Welt als Kanji mit Katakana-Lesung, *gikun*). Er ist als *built-in* markiert und immer vorhanden.
- **Specification.** Freitext für *dieses* Buch, der nach dem Prompt angefügt wird: was wegzulassen ist (Nebenfiguren), wie eine Art von Wort hier geschrieben wird, eine Schreibweise, die du willst. Ohne Prompt ist das alles, was dem Modell gesagt wird.
- **From part / To part**, dann **Extract the words**. Die Teile werden nacheinander gelesen (ein Teil braucht die Zeit eines langsamen Modells), der Reiter zeigt *Part 3 of 12*, und **Stopp** hält nach dem Teil an, der gerade gelesen wird. Ein Teil, der fehlschlägt, wird gezählt, und der Lauf geht weiter. Ein Buch ohne Sprache, ohne Modell oder ohne Prompt und ohne Spezifikation beendet den Lauf.

Die Wörter, die das Glossar schon hat (auf jeder Ebene, auch ausgeschaltet), werden dem Modell mitgeteilt und nicht erneut vorgeschlagen. Ein zweiter Lauf über denselben Bereich fügt daher nur hinzu, was neu ist. Die *category*, in die das Modell einen Begriff einordnet („Fictional coined term / gikun“), wird in die Notiz des Eintrags geschrieben. Das Format der Ausgabe beschreibst du nicht: Das Plugin verlangt ein festes Format, egal was der Prompt sagt.

**Die Bibliothek der Prompts.** *Settings → Extension Settings → Real Language Translator* enthält die Liste **Extraction prompts**: deine eigenen Prompts mit Namen und Anweisungen, die du hinzufügen, bearbeiten, löschen und umordnen kannst. **Copy a built-in prompt** legt eine Kopie des eingebauten Prompts in deine Liste, die du dann ändern kannst. Die Prompts gehören dir, nicht einem Buch. Ein Buch merkt sich, welchen Prompt es gewählt hat, und seine Spezifikation.

![Die Bibliothek der Extraktions-Prompts in den Einstellungen](../../images/plugin-reallang-prompts.png)

<a id="more-than-one-language-in-a-book"></a>

## Mehr als eine Sprache in einem Buch

Eine Geschichte mit japanisch und koreanisch sprechenden Figuren braucht beide Sprachen. Ein Buch nennt **zuerst eine Sprache** (das Feld oben, das auch ein Ordner oder deine Einstellungen vorgeben können). Der Reiter **Languages** fügt **weitere Sprachen** aus deiner Bibliothek hinzu, eine nach der anderen, und entfernt sie wieder.

- **Jede Sprache hat ihre eigenen Sprechenden** in einem eigenen Reiter. Die erste Sprache wird von allen gesprochen außer den Figuren, die du aufführst, wie bisher. **Eine Sprache, die du hinzufügst, beginnt mit „nur die aufgeführten Figuren sprechen sie“, und zunächst ist niemand aufgeführt.** Sie wird also nicht abgefragt, bis du aufführst, wer sie spricht. Führe eine Figur als Ausnahme der ersten Sprache auf, wenn eine andere Sprache ihre ist. Sonst wird die erste Sprache auch für ihre Rede abgefragt (eine Rede, die zwei Sprachen beanspruchen, bleibt bei der Sprache, die zuerst abgefragt wird). Eine Figur kann mehrere Sprachen sprechen; trage sie dann in beiden ein. Der Text des Teils sagt dem Modell, in welcher Sprache eine Rede steht.
- **Eine Anfrage für jede Sprache**: die Regeln und Notizen einer Sprache und nur die Reden ihrer Sprechenden. Ein Teil mit Reden in zwei Sprachen braucht zwei Anfragen nacheinander. Die zweite liest den Teil mit dem Englischen der Reden der ersten Sprache und lässt diese Reden übersetzt. Ein Teil, dessen Blickwinkel und anwesende Figuren alle außerhalb der Sprechenden einer Sprache liegen, wird für diese Sprache nicht abgefragt. (Ein Teil, der keine Figur nennt, wird für jede Sprache mit Sprechenden abgefragt.)
- **Eine Passage kennt ihre Sprache.** **Translate dialogs (real language)** erneut für eine Sprache lässt die Reden der anderen Sprachen unverändert. **Put the English back** kann sich auf eine Sprache oder auf alle beziehen.
- **Die Menüs fragen nach der Sprache**, wenn das Buch mehr als eine hat: das Menü eines Teils (eine der Sprachen oder *All languages*) und der Rechtsklick auf einen markierten Satz (eine Sprache). **Translate parts** hat ein Feld *Sprache* (standardmäßig alle Sprachen); *the parts that are not translated* bedeutet „nicht in dieser Sprache übersetzt“.
- **Der automatische Lauf** übersetzt einen neuen Teil in jede Sprache, deren Sprechende darin vorkommen können.
<a id="translating"></a>

## Übersetzen

- **Von Hand:** Das Menü eines Teils enthält das Untermenü **Übersetzen** (der Rechtsklick auf den Text hat ebenfalls eines), und darin **Dialoge übersetzen (echte Sprache)** (diese Einträge und **Teile übersetzen (echte Sprache)** erscheinen nur in einem Buch, das eine Sprache dieses Plugins hat). Ein Teil, der bereits übersetzt ist, wird **aus seinem Englisch** erneut übersetzt. Ein Buch mit mehr als einer Sprache fragt zuerst, [welche](#more-than-one-language-in-a-book).
- **Von selbst:** Mit **Einen neuen Teil von selbst übersetzen** wird ein erzeugter Teil übersetzt, sobald er fertig ist. Ein *Neuerzeugen* eines Teils gibt ihm neues Englisch und entfernt die alte Übersetzung.
- **Verlauf einer Übersetzung.** Die Übersetzung eines Teils (von Hand, einer Auswahl oder von selbst) blockiert die Seite nicht: Sie wird im [Bereich der Läufe](../books/story-editor.md#the-panel-of-the-runs) unten links aufgelistet, mit dem Ende der **Überlegung** des Modells bzw. seiner **Antwort**, während sie eintrifft, und mit einer Schaltfläche **Stopp**. Der Teil zeigt inzwischen *Die Reden des Teils werden übersetzt...*, und nichts anderes kann ihn übersetzen. **Stopp** bricht die Anfrage sofort ab und startet keine weitere; bei mehreren Sprachen bleiben die Sprachen, die vorher geschrieben wurden, übersetzt. Die Übersetzung eines [Bereichs](#translating) hat ein eigenes Fenster, das neben der Geschichte geöffnet bleibt.
- **Vom Modell übersehene Reden:** Eine Übersetzung lässt manchmal eine Rede auf Englisch stehen. **Übersetzen → Fehlende Dialoge übersetzen (echte Sprache)** im Menü eines Teils übersetzt nur die Reden, die noch auf Englisch sind, und lässt die übersetzten unverändert; bei markiertem Text erledigt das der Rechtsklick **Übersetzen → Fehlende Dialoge in der Auswahl übersetzen (echte Sprache)** für die Reden, die die Auswahl berührt. Das Modell erhält den Teil so, wie er ist, mit den übersetzten Reden darin, und wird nur um die noch englischen Reden gebeten; der Lauf ist daher günstiger als eine Übersetzung des ganzen Teils. Es wird nichts außer Rede geschrieben, also ist es kein Weg, einen Satz zu übersetzen, den das Modell nicht als Rede erkennt: Verwenden Sie dafür *Ausgewählten Text übersetzen*.
- **Ein Satz, den Sie geschrieben haben:** Markieren Sie ihn im Text eines Teils, klicken Sie mit der rechten Maustaste und wählen Sie **Übersetzen → Ausgewählten Text übersetzen (echte Sprache)**. Das Modell übersetzt ihn mit der Geschichte um ihn herum; das Englische bleibt als Ausgangstext erhalten. Eine Auswahl, die eine übersetzte Rede enthält, wird abgelehnt.
- **Ein Bereich:** **Teile übersetzen (echte Sprache)** im Werkzeugmenü der Geschichte öffnet ein Fenster, das neben der Geschichte geöffnet bleibt: *Die nicht übersetzten Teile übersetzen*, *Die übersetzten Teile erneut übersetzen* oder *Das Englische wiederherstellen*, von einem Teil bis zu einem Teil, mit einem **Stopp**.

![Das Fenster, das einen Bereich von Teilen übersetzt](../../images/plugin-reallang-range.png)

Eine Übersetzung wird wie jede Textänderung gespeichert: Der Verlauf des Teils erhält eine Version, der Teil wird **nicht als bearbeitet markiert**, und das Englische bleibt erhalten. **Eine Rede, die Sie von Hand geändert haben, bleibt unverändert**, wenn der Teil erneut übersetzt oder auf Englisch zurückgesetzt wird, denn sie enthält Ihre Worte.

<a id="what-the-reader-sees"></a>

## Was der Leser sieht

Eine übersetzte Rede hat eine **gepunktete Linie**; ein Klick darauf zeigt das Englische in einem Popover: die Lesung (wenn die Sprache sie verlangt), wer gesprochen hat und ob der Zuhörer es nicht versteht, sowie einen roten Hinweis, wenn Sie die Übersetzung nach ihrer Erstellung bearbeitet haben.

**Quelle bearbeiten** (eine Schaltfläche im Popover, nur in Ihren eigenen Ansichten) öffnet ein Fenster mit der Übersetzung, so wie die Geschichte sie hat, und dem Englischen, das die Modelle lesen. Verwenden Sie es, wenn eine Muttersprachlerin oder ein Muttersprachler das Japanische von Hand korrigiert hat und das Englische nun etwas anderes sagt, oder wenn das Modell das Englische falsch verstanden hat. Beim Speichern erhält die Rede ihr neues Englisch, die Übersetzung, wie sie jetzt ist, gilt als die, zu der das Englische gehört (der rote Hinweis verschwindet), und die Modelle lesen an ihrer Stelle das neue Englisch. Der Text des Teils wird nicht geändert. `reallang-edit-source` tut dasselbe über die Befehlszeile.

**Suchen und Ersetzen in den Quellen** (die Schaltfläche mit den Pfeilen oben im Reiter *Echte Sprache*) öffnet das Fenster zum Suchen und Ersetzen der Anwendung über die englischen Quellen der übersetzten Reden des aktiven Zweigs (einfacher Text oder ein regulärer Ausdruck). Nur die Quellen ändern sich; die Wörter der Übersetzung selbst werden mit dem Suchen und Ersetzen des Buches ersetzt.

![Das Fenster, das die Quelle einer Rede bearbeitet](../../images/plugin-reallang-source.png)

![Suchen und Ersetzen über die englischen Quellen der Reden](../../images/plugin-reallang-search-replace.png)

![Eine übersetzte Rede mit dem Englischen in einem Popover](../../images/plugin-reallang-story.png)

Das [veröffentlichte Buch](../books/publishing.md) zeigt es so, wie das Buch es verlangt, mit **Leser des veröffentlichten Buches sehen**: *die Übersetzung, das Englische beim Klick* (der Standard), *nur die Übersetzung* (das Englische wird nie an den Browser des Lesers gesendet) oder *nur das Englische*.

<a id="exporting"></a>

## Exportieren

Wenn das Buch übersetzte Rede enthält, hat der Dialog [Export](../books/export.md) das Feld **Übersetzte Rede (echte Sprache)**: *die Übersetzung, wie sie ist*, *nur das Englische*, *die Übersetzung mit dem Englischen in Fußnoten* oder *das Englische mit der Übersetzung in Fußnoten*. Der gespeicherte Text des Buches wird durch einen Export nie verändert.

![Der Exportdialog mit der übersetzten Rede](../../images/plugin-reallang-export.png)

<a id="together-with-the-conlang-translator"></a>

## Zusammen mit dem Conlang Translator

Ein Buch kann beides enthalten: eine erfundene Sprache für die Elfen und Japanisch für die Stadt. Jedes Plugin hat seine eigenen Sprechenden, seine eigene Spracheinstellung und seine eigenen Passagen, und die Passagen der beiden berühren sich nicht. Das Englische, das jedes Modell liest, enthält beide. Der [Export](../books/export.md) fasst die Fußnoten beider in einer Nummerierung zusammen, und der Betrachter zeichnet die Rede beider auf derselben Seite. Setzen Sie für beide denselben Wert unter **Leser des veröffentlichten Buches sehen**: Eine Seite wird einmal gezeichnet, von dem Plugin, das sie zuerst erreicht.
<a id="commands"></a>

## Befehle

Alles, was die Oberfläche tut, ist auch ein Befehl, für die [Kommandozeile](../commands/index.md) und für [Agenten](../agents.md). Eine Sprache wird über ihren **Namen** (Groß- und Kleinschreibung spielt keine Rolle) oder ihre ID angesprochen; ein Teil über sein Kürzel oder seine Position im aktiven Zweig (der erste Teil ist 1).

| Befehl | |
|---|---|
| `reallang-list`, `reallang-create`, `reallang-update`, `reallang-delete` | Die Bibliothek. `reallang-create` nimmt eine `preset` (`ja-kana-kanji`, `ja-kana`, `ja-romaji`, `other`). `reallang-delete` fragt nach. |
| `reallang-set-book-language` | Die Sprache des geöffneten Buchs (das Feld **Sprache der Rede** in den *Einstellungen der Erweiterung*). |
| `reallang-add-book-language`, `reallang-remove-book-language` | Die weiteren Sprachen des geöffneten Buchs. Beim Entfernen wird der Benutzer gefragt. |
| `reallang-status` | Die Sprache des Buchs und welche Teile übersetzt sind. |
| `reallang-translate-part`, `reallang-translate-range`, `reallang-translate-sentence`, `reallang-translate-text`, `reallang-revert-to-source` | Übersetzen: ein Teil (`missingOnly` übersetzt nur die Reden, die noch auf Englisch sind; `within` zitiert eine Passage, um dies einzuschränken), ein Bereich (`mode` *new* oder *again*), ein Satz eines Teils, ein Text, der nur zurückgegeben wird, und das Englische zurück. Mit der optionalen `language` beziehen sie sich auf eine Sprache des Buchs (standardmäßig auf alle, bei einem Satz oder einem Text auf die erste). |
| `reallang-glossary`, `reallang-glossary-set`, `reallang-glossary-remove`, `reallang-glossary-accept`, `reallang-glossary-switch`, `reallang-glossary-move` | Das [Glossar](#the-glossary-of-names-and-special-words) des geöffneten Buchs in einer Sprache: die zusammengeführten Einträge mit ihrer Ebene lesen; einen Eintrag auf einer `level` (`language`, `folder` mit dem Namen des Ordners `folder`, `book`) hinzufügen oder ändern; einen entfernen (fragt nach); die Vorschläge annehmen; ein Wort einer höheren Ebene für das Buch ausschalten oder wieder einschalten; einen Eintrag des Buchs nach oben verschieben. |
| `reallang-extract-terms`, `reallang-extraction-set` | Die [Wortextraktion](#extracting-the-words-by-model): `reallang-extraction-set` wählt die `prompt` und schreibt die `specification` des geöffneten Buchs (was weggelassen wird, bleibt); `reallang-extract-terms` liest die Teile von..bis und schlägt die Begriffe für das Glossar des Buchs vor (`language` für die Sprache). |
| `reallang-extraction-prompts`, `reallang-extraction-prompt-set`, `reallang-extraction-prompt-remove` | Die Bibliothek der Extraktions-Prompts: die eingebauten und eigenen auflisten; einen hinzufügen oder ändern (wenn man einen eingebauten nennt, entsteht eine Kopie); einen entfernen (fragt nach). |
| `reallang-edit-source` | Setzt die englische Quelle einer übersetzten Rede (`part`, `speech` so, wie `reallang-list-passages` sie nummeriert, `source`); die Übersetzung, wie sie jetzt ist, zählt als die, zu der sie gehört. Ersetzt die alte Quelle. |
| `reallang-get-source`, `reallang-list-passages` | Lesen: der Text eines Teils, so wie ihn die Modelle lesen (das Englische), und die übersetzten Reden eines Teils mit ihrer Lesung und dem Englischen. |
| `reallang-speakers`, `reallang-set-speakers-mode`, `reallang-set-speaker`, `reallang-remove-speaker` | Wer die Sprache im geöffneten Buch spricht (`language` nennt eine der Sprachen des Buchs, standardmäßig die erste); `reallang-set-speaker` setzt auch die Stufe von `understands`. |
| `reallang-set-understanding-prompt` | Ob die Verständnisstufen von selbst in den Prompt der Geschichte eingefügt werden und wie viele Nachrichten vom Ende an. |
| `reallang-understanding` | Wer welche Sprache nicht versteht, als Text, für das {%{`{{command::reallang-understanding::<characters>}}`}%} [Makro](../templates/macros.md#commands) (die Figuren sind optional). |

Eine Sprache ist ein paar Zeilen Regeln und hat keine Arbeitskopie für einen Agentenlauf (der Conlang Translator kopiert eine Sprache mit ihrem Lexikon): ein Agent, der eine Sprache anlegt oder ändert, ändert sofort deine Bibliothek. Die Sprecher, die Einstellungen des Buchs und die Passagen gehören dem Buch, und die Kopie des Buchs für den Lauf trägt sie.

<a id="limits"></a>

## Grenzen

- **Die Qualität liegt beim Modell.** Die Höflichkeit ist der schwierige Teil des Japanischen; die Regeln und die Hinweise zu den Schriftzeichen leiten es an, ein paar Teile, die eine Person liest, entscheiden, ob das genügt.
- **Die Sprecher bestimmen, welche Sprachen angefragt werden**, anhand des Standpunkts und der anwesenden Figuren eines Teils. Eine Rede einer Figur, die der Teil nicht nennt, kann an einer Sprache vorbeigehen, deren Sprecher aufgelistet sind; der Text des Teils entscheidet innerhalb einer Anfrage.
- **Kein zweiter Durchgang**, der eine Übersetzung auf Natürlichkeit umschreibt, und noch keine Rückübersetzungsprüfung.
- **Keine Furigana.** Die Lesung steht als Zeile im Popover, nicht als Ruby-Text auf der Seite.
- **Ein Teil, den das andere Übersetzer-Plugin übersetzt hat**, liest dieses Modell mit dieser Übersetzung im Text (nicht dem Englischen); ihm wird nur gesagt, dass die Rede dort in einer anderen Sprache ist.
- **Ein Telefon** hat das Panel und den Editor; die Seite der Geschichte (Menüs, Bereichsfenster, Klick in der Geschichte) wurde dort nicht erprobt.
