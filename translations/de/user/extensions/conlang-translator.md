---
label: Conlang-Übersetzer
order: 17
---

# Conlang-Übersetzer

Lässt die Figuren deiner Geschichte eine konstruierte Sprache sprechen. Du beschreibst die Sprache (ihre Regeln und ihre Wörter), und das Modell
schreibt die **wörtliche Rede** der Figuren, die in dieser Sprache sprechen, während die Geschichte das Englische dahinter behält.

!!!
Der Text der Geschichte enthält die **Konlang**. Alles, was die Geschichte für ein Modell liest (die nächste Generierung, die Zusammenfassungen, den Lorebook-Scan, die Anmerkungen, die anderen Erweiterungen), liest das **Englische**, aus dem die Rede übersetzt wurde. So muss das Modell deine Sprache nie verstehen, um die Geschichte weiterzuführen. Ein Klick auf eine übersetzte Rede zeigt das Englische.
!!!

!!!warning Arbeit in Bearbeitung
Die Erweiterung funktioniert für eine Person, die die hier beschriebenen Bildschirme verwendet, und über [Befehle](#commands) für einen Agenten. Das Telefon hat nur einen Teil davon, siehe
[Grenzen](#limits).
!!!

<a id="the-idea"></a>

## Die Idee

1. Eine **Sprache** besteht aus zwei Dingen: **Regeln** (Text, für das Modell geschrieben wie ein Grammatikbuch) und einem **Lexikon** (den Wörtern, größtenteils vom Modell geschrieben und von dir beurteilt). Sprachen werden einmal in deinen Einstellungen gespeichert, und beliebig viele Bücher können sie verwenden.
2. Ein **Buch** (oder ein Ordner mit Büchern) wählt seine Sprache. Ein Teil wird **von Hand** über sein Menü übersetzt, **automatisch**, sobald er final ist, oder **einen Bereich nach dem anderen**.
3. Eine Anfrage an das Modell pro Teil klassifiziert jede wörtliche Rede (wird sie in der Sprache gesagt oder in der gemeinsamen Sprache der Geschichte, von dieser Figur, und vom Zuhörer verstanden?) und übersetzt die Rede, die in der Sprache steht. Ein Wort, das der Sprache fehlt, wird durch die Regeln erfunden, **mit Begründung gemeldet** und als **neu** dem Lexikon hinzugefügt, damit du es annehmen, ändern, sperren oder verwerfen kannst.
4. Das Englische wird (in den Daten des Buches als *Quelle* der Rede) aufbewahrt und von jedem Modell gelesen. Die Konlang ist das **Original**: Was du daran von Hand änderst, bleibt erhalten, und ein Teil, den du bearbeitet hast, wird nie stillschweigend überschrieben.
<a id="languages"></a>

## Sprachen

Eine Sprache wird unter **Einstellungen → Einstellungen der Erweiterung → Conlang Translator** bearbeitet, dem Bereich, der deine Sprachen auflistet.

![Die Sprachen in den Einstellungen](../../images/plugin-conlang-settings.png)

| Schaltfläche | |
|---|---|
| **New language** | Eine leere Sprache. |
| **Design a language** | Ein [Assistent](#the-designer), in dem das Modell jeden Teil einer neuen Sprache vorschlägt und du ihn bearbeitest und übernimmst. |
| **Bearbeiten** | Der [Editor](#the-editor) der ausgewählten Sprache. |
| **Duplizieren** | Eine unabhängige Kopie mit neuem Namen, samt Wörtern. Verwende sie, um eine Änderung auszuprobieren (eine Kopie ist außerdem der einzige Weg, eine Sprache *nur für ein Buch* zu haben: ein Buch, das eine Sprache wählt, teilt sie mit jedem anderen Buch, das sie wählt). |
| **Compare** | Übernimmt die Änderungen einer anderen Sprache in die ausgewählte, nach einer [Prüfung](#comparing-and-merging). |
| **Exportieren**, **Importieren** | Eine Sprache als Dokument (JSON): die Regeln und das Lexikon, ohne die Verknüpfungen zu einem Buch. Der Import einer Sprache, deren Name du bereits hast, bietet die Prüfung der Unterschiede oder eine neue Sprache an. |
| **Löschen** | Nach einer Rückfrage. |

Oben stehen das **Modell und Protokoll des Designers**. Unter der Liste stehen die Einstellungen, die Bücher und Ordner von dir **erben**: die Sprache, ob ein Teil für sich übersetzt wird,
und das Modell und Protokoll der Übersetzung. Ein Buch oder ein Ordner kann eigene Werte setzen, siehe [Die Sprache eines Buchs wählen](#choosing-the-language-of-a-book). **Speichern** den Bildschirm der Einstellungen, um deine Änderungen zu behalten (die Wörter, die eine Übersetzung in der Zwischenzeit hinzugefügt hat, gehen nicht verloren: eine Sprache, die du nicht angefasst hast, wird beim Speichern des Bildschirms neu gelesen).

<a id="the-editor"></a>

### Der Editor

Der Editor ist ein Fenster mit fünf Reitern; er arbeitet auf einer Kopie bis **Speichern**, und er lässt sich in der Größe ändern, verschieben und maximieren.

| Reiter | |
|---|---|
| **Overview** | Der Name, eine Beschreibung für dich, die Option **A hyphen is a part of a word** (aus: `thal-orun` und `thalorun` sind dasselbe Wort; an: der Bindestrich gehört zur Form), sowie das **example book** und das **example lorebook**, die der Designer liest. |
| **Rules** | Die Regeln in **Abschnitten** (Laute und Schreibung, Grammatik, Wortbildung ...). Jeder Abschnitt legt fest, **wer ihn liest**: *der Übersetzer* (wird bei jeder Anfrage gesendet), *nur der Designer* (die Geschichte der Sprache, Notizen zum Bilden von Wörtern) oder *niemand* (deine eigenen Notizen). Das Plugin wertet die Regeln nicht aus: Es ist Text für das Modell. |
| **Categories** | Wortgruppen (*People & Social*, *Nature*, *Particles* ...). Das Lexikon wird unter diesen Überschriften angezeigt und dem Modell so gesendet. Eine Kategorie, die **immer gesendet** wird (Partikel, Pronomen, Zahlen), gelangt in jede Anfrage, auch wenn das Lexikon zu groß ist, um es ganz zu senden. |
| **Registers** | Die Sprachstile (*formal*, *soft*, *vulgar*, *archaic* ...) mit einer Notiz. Ein Begriff kann für jedes Register eine Form haben; das Modell wählt je nachdem, wer spricht und zu wem. |
| **Lexicon** | Die Wörter, siehe unten. |

![Die Regeln einer Sprache](../../images/plugin-conlang-rules.png)

<a id="the-lexicon"></a>

### Das Lexikon

![Das Lexikon einer Sprache](../../images/plugin-conlang-lexicon.png)

Das Raster hat eine **Suche** (nach Wort oder Bedeutung), Filter nach **Kategorie** und **Status** sowie die Aktionen **Add**, **Bearbeiten**, **Accept**,
**Lock**, **Retire**, **Löschen** und **Accept all new**. Ein Wort hat:

- die **Form** (wie die Geschichte es schreibt), optional die Form in einer **Schrift**, eine **Kategorie** und eine **Art** (Wurzel, Kompositum, Partikel,
  Name, Zahl, Wendung);
- eine oder mehrere **Bedeutungen** (ein Homophon hat mehrere, jede mit einer Wortart und einer Notiz) und die **anderen Formen des Begriffs**
  (das formelle, sanfte und vulgäre Wort für dieselbe Sache, gemeinsam angezeigt und gesendet);
- die **Begründung**: warum das Modell das Wort gewählt hat (die Regel oder die Wurzeln). Du liest sie, um das Wort zu beurteilen, und sie fließt in spätere Anfragen ein,
  damit die nächste Erfindung derselben Logik folgt;
- eine **Herkunft**: deine, oder die des Modells (mit dem Teil, für den es erfunden wurde, dem Modell und dem Zeitpunkt). Ein Wort des Modells, das du bearbeitest, wird deins;
- einen **Status**: **New** (das Modell hat es gebildet, und du hast noch nicht hingeschaut), **Accepted**, **Locked** (das Modell darf es verwenden, aber nie neu definieren) oder **Retired**
  (behalten, aber dem Modell nicht angeboten: für ein Wort, das ersetzt wurde).

Ein Wort, das das Modell braucht, das aber im Lexikon fehlt, wird **nie allein deshalb verworfen, weil es neu ist**: Es wird sofort hinzugefügt, damit der nächste Teil dasselbe auf dieselbe Weise sagt, und es wartet als *New*. Eine Übersetzung, die Wörter verwendet, die nicht im Lexikon stehen und die das Modell **nicht** gemeldet hat, wird markiert, weil niemand sagen kann, ob das Wort ein Fehler ist.

<a id="names-for-the-name-generator"></a>

### Namen für den Namensgenerator

Der Reiter **Names** des Editors macht die Sprache für den [Namensgenerator](name-generator.md) verfügbar: Die Namen, die er erzeugt, klingen wie die Sprache. Füge eine **Namenskategorie** hinzu
(zum Beispiel *Places* oder *People*) und markiere dann die Wörter des Lexikons, aus denen sie besteht. Die **Suche** und der **Kategoriefilter** schränken die Liste ein, **Check all shown** und **Uncheck all shown**
wirken auf die angezeigten Wörter, und **Preview names** erzeugt 20 Namen aus den jetzt markierten Wörtern, bevor du etwas speicherst. Der Namensgenerator trainiert eine Markov-Kette auf den markierten
Wörtern, daher braucht eine Kategorie einige Wörter (ein Dutzend oder mehr ergibt bessere Namen).

Gespeichert wird nur deine Auswahl, nicht die Wörter: Ein Wort, das du änderst, wird so gelesen, wie es jetzt ist, und eines, das du löschst oder zurückziehst, wird beim Erzeugen der Namen ausgelassen. Nachdem du die Einstellungen gespeichert hast, wird die
Kategorie im Namensgenerator angeboten (im Dialog **Tools** des Lore-Generators und der Handlungsbogen-Gliederung, im Befehl `generate-names`, im Lauf eines Agenten) als *Conlang - Sprache - Kategorie*.
Sie gehört dir: Ein anderer Benutzer der Installation sieht nur seine eigenen Kategorien.

![Der Reiter Names: eine Namenskategorie, die Wörter, aus denen sie besteht, und zwanzig Namen daraus](../../images/plugin-conlang-names.png)

<a id="the-designer"></a>

### Der Designer

Das Menü **Design with AI** des Editors bittet das Modell um Hilfe, immer mit der Sprache **so, wie sie ist** (ihren Regeln, Registern, Kategorien, ihrem Lexikon und dem Beispiel-Lorebook), und
immer als **Vorschlag**: Nichts wird geschrieben, bevor du es geprüft hast.

![Das Menü des Designers](../../images/plugin-conlang-designer-menu.png)

| Aktion | |
|---|---|
| **Generate words...** | Wörter für eine Kategorie, eine Anzahl davon, mit einer Notiz (»die Küche eines Belagerungslagers«). |
| **Add a section with AI...** | Ein neuer Regelabschnitt für das, was fehlt (Zahlen, Farben, Höflichkeit), und die Wörter, die er braucht. |
| **Rewrite a section...** | Das Modell schreibt einen Abschnitt auf deine Bitte neu. Das Modell gibt den **ganzen Abschnitt** zurück, daher ist ein sehr langer Abschnitt langsam und teuer: Halte Abschnitte kurz, eines Themas pro Abschnitt. |
| **Review the language** | Eine Liste von Widersprüchen, Lücken und Wörtern, die das Lautsystem brechen, jeweils mit einem Abschnitt und einer Korrektur. Nichts wird geändert. |
| **Ask the designer...** | Ein Gespräch; eine Antwort mit Wörtern oder Abschnitten kann als Änderungen geprüft werden. |

**Design a language** im Bereich ist ein Assistent aus sieben Schritten (das Konzept, die Laute, die Schrift, die Grammatik, die Wortbildung, die ersten Wörter nach Kategorie und ein Test mit
Sätzen). Jeder Schritt ist ein Abschnitt, den das Modell vorschlägt und den du bearbeitest, mit einer Notiz verfeinerst und übernimmst; die Sprache existiert nur im Fenster, bis **Beenden**.

![Der Assistent, der eine Sprache entwirft](../../images/plugin-conlang-wizard.png)

<a id="comparing-and-merging"></a>

### Vergleichen und Zusammenführen

**Compare** (und der Import einer Sprache, die du bereits hast) öffnet ein Fenster mit den Unterschieden zwischen zwei Sprachen, jeweils eine Zeile mit einem Kontrollkästchen: was bei den Feldern, Regeln, Kategorien, Registern, Wörtern und Namenskategorien der Sprache hinzugefügt,
geändert, gelöscht oder verschoben wurde. Eine Änderung, die beide Seiten unterschiedlich vorgenommen haben, ist ein **Konflikt**: Dein Wert bleibt erhalten, bis du den anderen wählst oder deinen eigenen schreibst. Was du auswählst, wird in die Sprache auf dem Bildschirm geschrieben und beim Speichern des Bildschirms der Einstellungen gespeichert. Dasselbe Fenster zeigt, was der Designer vorschlägt.

![Die Prüfung der Unterschiede zwischen zwei Sprachen](../../images/plugin-conlang-review.png)

<a id="choosing-the-language-of-a-book"></a>

## Die Sprache eines Buchs wählen

Der Reiter **Prompts** im Buchfenster, unter *Einstellungen der Erweiterung*, enthält die Felder der Erweiterung; das Ordnerfenster hat dieselben. Leer bedeutet *erben*: der Ordner darüber,
dann die Einstellungen deines Benutzers.

![Die Felder eines Buchs](../../images/plugin-conlang-book.png)

| Feld | |
|---|---|
| **Language of the speech** | Die Sprache, in die das Buch übersetzt wird. Leer: Das Buch wird nicht übersetzt. |
| **Translate a new part by itself** | Ein Teil wird übersetzt, wenn er endgültig ist: nach einer Generierung, nach der automatischen Annotation und Überarbeitung, falls das Buch sie hat, als Schritt von [was auf den Teil folgt](../books/story-editor.md#what-follows-a-generated-part) (das Panel der Läufe zeigt ihn; mit **Stopp** wird er angehalten). |
| **Model of the translation**, **Protocol of the translation** | Leer: das Modell und Protokoll des Buchs, sonst deine Standardwerte. |
| **Readers of the published book see** | Was der [Viewer](#the-viewer) zeigt. |
<a id="the-conlang-tab-of-the-book"></a>

## Die Conlang-Registerkarte des Buchs

Im Erzählfenster gibt es eine Registerkarte **Conlang** (im selben Andockbereich wie die Kapitel und das Lorebook; ziehe sie, wohin du möchtest) für die Sprache *dieses* Buchs. Sie hat vier eigene Registerkarten,
und **Aktualisieren** liest die Sprache neu ein, nachdem du sie im Fenster des Buchs geändert hast. Ein Buch mit [mehr als einer Sprache](#more-than-one-language-in-a-book) hat neben **Aktualisieren** ein
Feld, das wählt, auf welche der Sprachen sich die Registerkarten *Wörter*, *Sprecher* und *Schnellübersetzung* beziehen.

<a id="words"></a>

### Wörter

Das Lexikon der Sprache des Buchs, mit der Suche, den Filtern und den Aktionen des [Editors](#the-lexicon): Schlage ein Wort nach, während du schreibst, bearbeite es, übernimm die Wörter, die das Modell erfunden hat, sperre oder ziehe ein Wort zurück. Die Änderung wird sofort in deiner Bibliothek gespeichert (die Wörter, die eine Übersetzung in der Zwischenzeit hinzugefügt hat, gehen nicht verloren).

<a id="speakers"></a>

### Sprecher

Wer die Sprache **in diesem Buch** spricht. Die Sprecher gehören zum Buch, nicht zur Sprache: Dieselbe Sprache wird in einer anderen Geschichte von anderen Personen gesprochen, daher trägt eine Sprache, die du duplizierst, exportierst oder vergleichst, keine Figuren mit sich.

Standardmäßig **spricht jeder die Sprache außer den aufgelisteten Figuren** (oder umgekehrt, nur die aufgelisteten). Dies ist nur der Standard: Der Text überschreibt ihn („sie sagte in der Gemeinsprache“, „die Wache verstand kein Wort“), und das Modell entscheidet bei jeder Rede. Für jede Figur
kannst du das **Geschlecht** notieren (eine Sprache, die das Geschlecht markiert, liegt sonst falsch, wenn der Abschnitt nur „sie“ sagt) und die **Stimme** („förmlich, knapp, derb“), die das Modell mit den Registern der Sprache abgleicht. Änderungen werden sofort im Buch gespeichert.

Jede Figur kann außerdem eine Stufe des **Verstehens** haben: *versteht nichts*, *kennt ein paar Wörter*, *folgt den meisten Gedanken* oder *folgt allem*. Lass das Feld leer (oder wähle *folgt allem*), dann erfahren die Modelle nichts darüber. Verwende es für Menschen, die die Sprache hören, ohne sie zu sprechen: *Tomáš: versteht nichts*, *Andere Dorfbewohner: folgt den meisten Gedanken* (der Name ist nur Text, daher geht auch eine Gruppe). Die Stufen gelten **für das ganze Buch unverändert** und werden den Modellen als Information übergeben; was eine Figur damit tut, bleibt dem Modell überlassen, und du kannst jederzeit mehr in den Anweisungen sagen („Tomáš versteht nicht, was sie sagen“).
Sie erreichen die Modelle auf zwei Wegen: Das **Erzählmodell** erhält eine kurze Nachricht mit den Stufen (eingefügt in den Prompt wie die Autorennotiz; das Kontrollkästchen über der Liste schaltet sie ab, und die Zahl gibt an, wie viele Nachrichten vom Ende sie zurückgeht), und das Modell, das **einen Abschnitt prüft** (*Mit KI kommentieren*), erhält sie als Abschnitt der Fakten, sodass es eine Figur markieren kann, die auf Wörter reagiert, die sie nicht verstehen kann. Wenn du den Text lieber selbst platzieren möchtest, schalte das Einfügen aus und verwende das {%{`{{command::conlang-understanding}}`}%}-[Makro](../templates/macros.md#commands) in einer Vorlage; es nimmt eine optionale Liste von Figuren entgegen, die behalten werden sollen.

![Die Conlang-Registerkarte: die Sprecher und zwei Figuren, die die Sprache nicht verstehen](../../images/plugin-conlang-tab.png)

<a id="languages"></a>

### Sprachen

Die anderen Sprachen des Buchs, neben der, die das Feld **Sprache der Rede** nennt: siehe [Mehr als eine Sprache in einem Buch](#more-than-one-language-in-a-book).

<a id="quick-translate"></a>

### Schnellübersetzung

Schreibe einen Text auf Englisch (eine Wendung, einen Namen, eine Zeile, die du noch nicht geschrieben hast), optional wer sie sagt und zu wem, und drücke **Übersetzen**: Das Modell übersetzt ihn in die Sprache des Buchs mit den Regeln, den Registern und dem Lexikon sowie den Anmerkungen zu den Figuren, die im Text genannt werden. Nichts wird in die Geschichte geschrieben. Das Ergebnis ist die Übersetzung, eine Notiz zu den getroffenen Entscheidungen und die **Wörter, die die Übersetzung brauchte** und die im Lexikon fehlten, jeweils mit Bedeutung und Begründung. Wähle die gewünschten aus und klicke auf **Ausgewählte Wörter importieren**: Sie gehen als *neu* ins Lexikon, genau wie die Wörter eines übersetzten Abschnitts. Wörter in der Übersetzung, die weder im Lexikon stehen noch gemeldet wurden, erscheinen als Warnung.

<a id="more-than-one-language-in-a-book"></a>

## Mehr als eine Sprache in einem Buch

Ein Epos kann eine Sprache für die Elfen, eine weitere für die Zwerge und eine dritte für die Orks haben. Ein Buch nennt **zuerst eine Sprache** (das Feld oben, das auch ein Ordner oder deine Einstellungen vorgeben können), und die Registerkarte **Sprachen** der Registerkarte **Conlang** fügt **weitere Sprachen** aus deiner Bibliothek hinzu, eine nach der anderen, und entfernt sie auch wieder.

- **Jede Sprache hat ihre eigenen Sprecher.** Die Registerkarte *Sprecher* betrifft die Sprache, die das Feld oben wählt. Die erste Sprache wird von allen gesprochen außer den Figuren, die du auflistest, wie zuvor; **eine Sprache, die du hinzufügst, beginnt mit „nur die aufgelisteten Figuren sprechen sie“, ohne dass jemand aufgelistet ist**, sodass nichts von ihr verlangt wird, bis du auflistest, wer sie spricht. Trage eine Figur als Ausnahme der ersten Sprache ein, wenn eine andere Sprache ihre ist, sonst wird die erste Sprache auch nach deren Rede gefragt (eine Rede, die zwei Sprachen beanspruchen, bleibt bei der zuerst gefragten). Eine Figur kann mehr als eine Sprache sprechen (trage sie in beiden ein; der Text des Abschnitts sagt dem Modell, in welcher Sprache eine Rede steht).
- **Eine Anfrage für jede Sprache.** Eine Anfrage enthält nur die Regeln und das Lexikon einer einzigen Sprache und nur die Reden ihrer Sprecher; ein Abschnitt mit der Rede in zwei Sprachen braucht zwei Anfragen, nacheinander. Die zweite liest den Abschnitt mit dem Englisch der Reden der ersten und lässt diese Reden übersetzt stehen. Ein Abschnitt, dessen Blickwinkel und anwesende Figuren alle außerhalb der Sprecher einer Sprache liegen, wird für diese Sprache nicht angefragt (ein Abschnitt, der keine Figur nennt, wird für jede Sprache mit Sprechern angefragt).
- **Eine Passage kennt ihre Sprache.** Das Popover, der Export und der Viewer funktionieren wie zuvor; **Dialoge übersetzen (Konstruierte Sprache)** für eine Sprache lässt die Reden der anderen unberührt, und **Englisch wiederherstellen** kann sich auf eine Sprache oder auf alle beziehen.
- **Die Menüs fragen nach der Sprache**, wenn das Buch mehr als eine hat: das Menü eines Abschnitts (*Dialoge übersetzen (Konstruierte Sprache)*: eine der Sprachen oder *Alle Sprachen*) und der Rechtsklick auf einen markierten Satz (eine Sprache). **Abschnitte übersetzen (Konstruierte Sprache)** hat ein Feld *Sprache* (standardmäßig alle Sprachen); *die Abschnitte, die nicht übersetzt sind* bedeutet „in dieser Sprache nicht übersetzt“.
- **Der automatische Lauf** übersetzt einen neuen Abschnitt in jede Sprache, deren Sprecher darin vorkommen können.
<a id="translating"></a>

## Übersetzen

- **Von Hand:** Das Menü eines Abschnitts hat das Untermenü **Übersetzen** (der Rechtsklick auf den Text hat es ebenfalls), und darin **Dialoge übersetzen (Konlang)** (diese Einträge und **Abschnitte übersetzen** erscheinen nur in einem Buch, das eine Sprache dieses Plugins hat). Ein Abschnitt, der bereits übersetzt ist, wird **aus seinem Englisch** erneut übersetzt; so holt man also auch die Übersetzung eines Abschnitts nach, wenn sich die Regeln oder das Lexikon geändert haben. Ein Buch mit mehr als einer Sprache fragt zuerst, [welche](#more-than-one-language-in-a-book).
- **Von selbst:** Mit **Einen neuen Abschnitt von selbst übersetzen** wird ein erzeugter Abschnitt übersetzt, sobald er fertig ist. Ein *Neu erzeugen* eines Abschnitts gibt ihm neues Englisch und entfernt die alte Übersetzung.
- **Vom Modell übersehene Dialoge:** Eine Übersetzung lässt manchmal eine Rede auf Englisch stehen. **Übersetzen → Fehlende Dialoge übersetzen (Konlang)** im Menü eines Abschnitts übersetzt nur die Reden, die noch auf Englisch sind, und lässt die übersetzten Reden unverändert; bei markiertem Text erledigt das der Rechtsklick **Übersetzen → Fehlende Dialoge in der Auswahl übersetzen (Konlang)** für die Reden, die die Auswahl berührt. Dem Modell wird der Abschnitt so geschickt, wie er ist, mit den übersetzten Reden darin, und es wird nur nach den Reden gefragt, die noch auf Englisch sind; der Lauf ist daher billiger als eine Übersetzung des ganzen Abschnitts. Es wird nur Rede geschrieben, deshalb ist es kein Weg, einen Satz zu übersetzen, den das Modell nicht als Rede sieht: dafür *Markierten Text übersetzen* verwenden.
- **Ein Satz, den Sie geschrieben haben:** Markieren Sie ihn im Text eines Abschnitts, klicken Sie mit der rechten Maustaste und wählen Sie **Übersetzen → Markierten Text übersetzen (Konlang)**. Das Modell übersetzt den markierten Text (Leerzeichen und ein Paar Anführungszeichen um ihn herum bleiben außen vor) mit der Sprache, den Notizen zu den Figuren und der Geschichte rundherum. Der Text wird durch die Übersetzung ersetzt, und das Englische bleibt als Quelle der Rede erhalten, sodass ein Klick darauf das Englische zeigt wie bei jeder übersetzten Rede. Die Wörter, die das Modell erfinden musste, werden wie bei einem übersetzten Abschnitt als *neu* ins Lexikon aufgenommen. Eine Auswahl, die eine übersetzte Rede enthält, wird abgelehnt (stellen Sie zuerst das Englische wieder her), ebenso eine Auswahl in einem Abschnitt, der sich inzwischen geändert hat.
- **Ein Bereich:** **Abschnitte übersetzen (Konlang)** im Werkzeugmenü der Geschichte öffnet ein Fenster, das neben der Geschichte geöffnet bleibt.

  ![Abschnitte übersetzen](../../images/plugin-conlang-range.png)

  Wählen Sie **Ab Abschnitt** und **Bis Abschnitt** (Abschnitte mit Text, von 1 an gezählt wie im Export) und was geschehen soll: *Die nicht übersetzten Abschnitte übersetzen*, *Die übersetzten Abschnitte erneut übersetzen* (wenn sich die Regeln oder das Lexikon geändert haben) oder *Das Englische wiederherstellen*, nach einer Rückfrage. Das Fenster zeigt, wie viele Abschnitte der Bereich enthält und wie viele die Auswahl betrifft, zeigt *Abschnitt n von m* und einen Button **Stopp**, der nach dem Abschnitt wirkt, der gerade bearbeitet wird (eine Anfrage an das Modell, die schon begonnen hat, lässt sich nicht zurücknehmen). Ein Abschnitt, der fehlschlägt, wird gezählt, und der Lauf geht weiter; eine fehlende Sprache oder ein fehlendes Modell beendet ihn. Die Geschichte wird nach dem Ende des Laufs neu gezeichnet. Ein Abschnitt, den Sie während des Laufs bearbeiten, lässt nur diesen einen Abschnitt fehlschlagen, nicht den ganzen Lauf.

Eine Übersetzung wird wie jede Änderung des Textes gespeichert: Der Verlauf des Abschnitts erhält eine Version, der Abschnitt wird **nicht als bearbeitet markiert**, und das Englische bleibt erhalten. **Eine Rede, die Sie von Hand geändert haben, bleibt unverändert**, wenn der Abschnitt erneut übersetzt oder auf Englisch zurückgesetzt wird, denn sie enthält Ihre Worte.

!!!
Eine Übersetzung dauert so lange, wie das Modell für den Abschnitt braucht: Minuten bei einem lokalen Modell, das nachdenkt. Die Erweiterung bittet das Modell, nachzudenken (die Übersetzung von Genus, Numerus und Register gelingt damit viel besser), also geben Sie dem Modell eine großzügige Ausgabegrenze.
!!!

<a id="what-the-reader-sees"></a>

## Was der Leser sieht

<a id="in-the-story"></a>

### In der Geschichte

Eine übersetzte Rede hat eine **gepunktete Linie**. Ein Klick darauf zeigt das Englische in einem Popover: wer es gesagt hat, und *(vom Zuhörer nicht verstanden)*, wenn der Zuhörer die Sprache nicht versteht; ein roter Hinweis sagt, dass Sie den Konlang-Text nach der Übersetzung bearbeitet haben, sodass das Englische möglicherweise nicht mehr das sagt, was jetzt dasteht.

**Quelle bearbeiten** (ein Button im Popover, nur in Ihren eigenen Ansichten; die Leser eines veröffentlichten Buchs haben ihn nicht) öffnet ein kleines Fenster mit der Übersetzung, so wie die Geschichte sie hat, und dem Englischen, das die Modelle lesen. Verwenden Sie es, wenn eine Muttersprachlerin oder ein Muttersprachler die Übersetzung von Hand korrigiert hat und das Englische jetzt etwas anderes sagt, oder wenn das Modell das Englische falsch wiedergegeben hat. Beim Speichern erhält die Rede ihr neues Englisch, die Übersetzung, so wie sie jetzt ist, wird als die Übersetzung genommen, zu der das Englische gehört (der rote Hinweis verschwindet), und ab dann lesen die Modelle an ihrer Stelle das neue Englische. Der Text des Abschnitts wird nicht verändert, und *Dialoge übersetzen (Konlang)* ersetzt beides erneut. `conlang-edit-source` tut dasselbe über die Kommandozeile.

![Das Fenster, das die Quelle einer Rede bearbeitet](../../images/plugin-conlang-source.png)

**Suchen und Ersetzen in den Quellen** (der Button mit den Pfeilen oben im Reiter Konlang) öffnet das Suchen-und-Ersetzen-Fenster der Anwendung über die englischen Quellen der übersetzten Reden des aktiven Zweigs (einfacher Text oder regulärer Ausdruck, mit oder ohne Groß-/Kleinschreibung; die Tabelle listet die Reden mit einem Treffer nach Abschnitt und Redenummer). Verwenden Sie es für einen Namen, der im Englischen auf zwei Arten geschrieben war. Nur die Quellen ändern sich; um die Wörter der Übersetzung selbst zu ändern, verwenden Sie das Suchen und Ersetzen des Buchs. Eine Rede, deren Quelle nach der Suche geändert wurde, bleibt unverändert und wird gezählt.

![Suchen und Ersetzen über die englischen Quellen der Reden](../../images/plugin-conlang-search-replace.png)

![Das Englische hinter einer übersetzten Rede](../../images/plugin-conlang-story.png)

<a id="the-viewer"></a>

### Der Viewer

Der Viewer eines [veröffentlichten Buchs](../books/publishing.md) zeigt die übersetzte Rede so, wie das Buch es verlangt, mit dem Feld **Leser des veröffentlichten Buchs sehen** (eine Auswahl des Buchs oder seiner Ordner; Ihre eigenen Voreinstellungen entscheiden nie, was ein Leser sieht):

| | |
|---|---|
| **Die Konlang-Sprache, das Englische bei einem Klick** | Der Standard. Ein Klick (ein Tipper auf dem Telefon) öffnet das Popover mit dem Englischen. |
| **Nur die Konlang-Sprache** | Das Englische wird nie an den Browser des Lesers geschickt. |
| **Nur das Englische** | Das Buch, als wäre es nie übersetzt worden. |

Im Klickmodus enthält die Seite nur die Konlang-Sprache; das Englische einer Rede wird erst vom Server geholt, wenn der Leser sie anklickt.

![Das Englische hinter einer übersetzten Rede, im Viewer](../../images/plugin-conlang-viewer.png)

<a id="exporting"></a>

## Exportieren

Wenn das Buch übersetzte Rede enthält, hat der Dialog [Export](../books/export.md) das Feld **Übersetzte Rede**:

![Der Exportdialog](../../images/plugin-conlang-export.png)

| | |
|---|---|
| **Die Konlang-Sprache, so wie sie ist** | Der Standard: der Text, so wie er gespeichert ist. |
| **Nur das Englische** | Die Rede wird durch ihr Englisch ersetzt. |
| **Die Konlang-Sprache, das Englische in Fußnoten** | Jede übersetzte Rede bekommt eine Fußnote mit dem Englischen (und dem Sprecher). |
| **Das Englische, die Konlang-Sprache in Fußnoten** | Umgekehrt. |

Die Fußnoten sind die [Fußnoten des Exports](../books/export.md#footnotes): verlinkte Notizen in HTML und EPUB, Endnoten nach jedem Abschnitt im Klartext, echte Fußnoten am Seitenende in Word und PDF. Eine Rede, die Sie nach der Übersetzung bearbeitet haben, bleibt unverändert und bekommt keine Fußnote. Der gespeicherte Text des Buchs wird durch einen Export nie verändert.
<a id="commands"></a>

## Befehle

Alles, was die Oberfläche tut, ist auch ein Befehl, für die [Kommandozeile](../commands/index.md) und für [Agenten](../agents.md): ein Agent kann eine Sprache anlegen, ausbauen, ein Buch übersetzen und das Ergebnis prüfen, ohne dass eine Person die Dialoge bedient. Eine Sprache wird über ihren **Namen** (Groß- und Kleinschreibung spielen keine Rolle) oder ihre ID angesprochen; ein Regelabschnitt über seinen Titel oder seine Nummer in `conlang-list-rules`; eine Kategorie über ihren Namen oder ihre Nummer; ein Wort über seine Form (die ID aus `conlang-list-words` dient nur dazu, Homophone zu unterscheiden); ein Teil wie in den anderen Befehlen der Anwendung, über sein Handle oder seine Position auf dem aktiven Zweig (der erste Teil ist 1). Die Kommandozeile nimmt jedes Argument eines Befehls entgegen; ein Agent darf die optionalen weglassen.

| Befehl | |
|---|---|
| `conlang-list`, `conlang-get`, `conlang-create`, `conlang-duplicate`, `conlang-update`, `conlang-delete`, `conlang-export`, `conlang-import`, `conlang-set-references` | Die Bibliothek. `conlang-delete` fragt nach und steht Agenten nicht zur Verfügung. |
| `conlang-list-name-categories`, `conlang-add-name-category`, `conlang-update-name-category`, `conlang-delete-name-category`, `conlang-preview-names` | Die Kategorien von Namen für den [Namengenerator](name-generator.md) (der Reiter **Names**). Hinzufügen und Ändern nehmen die Wörter des Lexikons (getrennt durch `;`) oder eine ganze Kategorie des Lexikons; Löschen fragt den Benutzer; die Vorschau braucht den Namengenerator. Die eigenen Befehle des Generators `list-name-categories` und `generate-names` listen die Kategorien auf und erzeugen die Namen. |
| `conlang-set-book-language` | Die Sprache des geöffneten Buchs (das Feld **Language of the speech** in dessen *Einstellungen der Erweiterung*). |
| `conlang-add-book-language`, `conlang-remove-book-language` | Die weiteren Sprachen des geöffneten Buchs. Das Entfernen fragt den Benutzer und steht Agenten nicht zur Verfügung. |
| `conlang-list-rules`, `conlang-get-rules`, `conlang-add-rules`, `conlang-update-rules`, `conlang-delete-rules`, `conlang-move-rules` | Die Abschnitte der Regeln. |
| `conlang-list-categories`, `conlang-add-category`, `conlang-update-category`, `conlang-move-category`, `conlang-delete-category` | Die Kategorien der Wörter. |
| `conlang-list-registers`, `conlang-add-register`, `conlang-update-register`, `conlang-delete-register` | Die Register. |
| `conlang-list-words` | Das Lexikon, seitenweise, mit Filtern: Kategorie, Zustand (*new*, accepted, locked, retired) und einem Text. |
| `conlang-get-word`, `conlang-add-word`, `conlang-update-word`, `conlang-delete-word` | Ein Wort. Ein Wort, das ein Agent hinzufügt oder ändert, gehört dem Modell und ist **new**; ein gesperrtes Wort kann nicht geändert oder gelöscht werden. |
| `conlang-lock-word`, `conlang-unlock-word`, `conlang-retire-word`, `conlang-restore-word`, `conlang-accept-words` | Der Zustand der Wörter. |
| `conlang-check-lexicon` | Wiederholte Bedeutungen, Wörter ohne Kategorie, Komposita mit Teilen, die nicht im Lexikon stehen, und die Homophone (zur Information). Ohne Modell. |
| `conlang-status` | Die Sprache des Buchs und welche Teile übersetzt sind. |
| `conlang-translate-part`, `conlang-translate-range`, `conlang-translate-sentence`, `conlang-translate-text`, `conlang-revert-to-source` | Übersetzen: ein Teil (`missingOnly` übersetzt nur die Redebeiträge, die noch auf Englisch sind; `within` zitiert eine Passage, um dies zu begrenzen), ein Bereich (`mode` *new* oder *again*), ein Satz eines Teils, ein Text, der nur zurückgegeben wird, und das Zurückführen auf das Englische. Ein Bereich fragt den Benutzer bei *again*; ebenso das Zurückführen auf das Englische. Mit dem optionalen `language` beziehen sie sich auf eine Sprache des Buchs (standardmäßig alle, bei einem Satz oder Text die erste). |
| `conlang-edit-source` | Setzt den englischen Quelltext eines übersetzten Redebeitrags (`part`, `speech` so nummeriert, wie `conlang-list-passages` sie zählt, `source`); die Übersetzung, wie sie jetzt ist, gilt als die dazugehörige. Ersetzt den alten Quelltext. |
| `conlang-check-translation`, `conlang-get-source`, `conlang-list-passages` | Lesen: die Wörter eines übersetzten Redebeitrags, die das Lexikon nicht hat, der Text eines Teils, so wie die Modelle ihn lesen (das Englische), und die übersetzten Redebeiträge eines Teils mit ihrem Englisch. |
| `conlang-speakers`, `conlang-set-speakers-mode`, `conlang-set-speaker`, `conlang-remove-speaker` | Wer im geöffneten Buch die Sprache spricht (`language` nennt eine der Sprachen des Buchs, standardmäßig die erste); `conlang-set-speaker` setzt auch die Stufe von `understands`. |
| `conlang-set-understanding-prompt` | Ob die Stufen des Verstehens von selbst in den Prompt der Geschichte eingefügt werden, und wie viele Nachrichten vom Ende an. |
| `conlang-design-words`, `conlang-design-rules`, `conlang-rewrite-rules`, `conlang-design-stage`, `conlang-ask-designer`, `conlang-try-sentences` | Der Designer. Mit `ask true` gibt ein Befehl den Vorschlag als nummerierte Liste von Änderungen zurück und hält ihn zurück; nichts wird geändert. |
| `conlang-review` | Die Prüfung der Sprache durch den Designer: nur Befunde. |
| `conlang-apply-changes` | Wendet die zurückgehaltenen Änderungen an, alle oder die angegebenen Nummern (dasselbe wie **Anwenden** in der Prüfung). |
| `conlang-pending-changes`, `conlang-take-pending-changes`, `conlang-discard-pending-changes` | Was ein Agentenlauf im Buch zurückgelassen hat, siehe [unten](#agent-runs). |
| `conlang-understanding` | Wer welcher Sprache nicht folgt, als Text, für das Makro {%{`{{command::conlang-understanding::<characters>}}`}%} (die Figuren sind optional). |
| `conlang-lexicon`, `conlang-rules` | Das Lexikon und die Regeln, die eine Übersetzungsanfrage sendet, als Text, für die Makros {%{`{{command::conlang-lexicon::<language>}}`}%} und {%{`{{command::conlang-rules::<language>}}`}%} [Makros](../templates/macros.md#commands). |

<a id="agent-runs"></a>

### Agentenläufe

Ein Agent arbeitet an einer **Kopie des Buchs**, und die Sprachen sind nicht Teil des Buchs; deshalb kann ein Agent Ihre Bibliothek nicht hinter Ihrem Rücken verändern. Wenn ein Lauf zum ersten Mal eine Sprache **ändert** (ein Wort, eine Regel oder die Wörter, die eine Übersetzung erfindet), wird diese Sprache zusammen mit der Version, aus der sie kopiert wurde, in die Kopie des Buchs kopiert; von da an liest und schreibt der Lauf seine eigene Kopie. Ein Lauf, der nur liest oder nur mit den vorhandenen Wörtern übersetzt, kopiert nie etwas. Die Kopie folgt der Kopie des Buchs überallhin: den Checkpoints und deren Wiederherstellung, der Prüfung des Laufs und seinem Löschen.

Wenn Sie das Ergebnis des Laufs **anwenden**, landet die Sprache im Buch, **nicht in Ihrer Bibliothek**. Der Reiter **Conlang** des Buchs zeigt dann *An agent run changed the language ...* mit **Prüfen** und **Verwerfen**. Prüfen vergleicht drei Versionen: die Sprache zu dem Zeitpunkt, als der Lauf sie kopiert hat, die Version des Laufs und Ihre Bibliothek, so wie sie jetzt ist. Was Sie in der Bibliothek inzwischen geändert haben, bleibt also erhalten, und was beide geändert haben, ist ein Konflikt, über den Sie entscheiden. Aus Befehlen: `conlang-pending-changes` listet die Änderungen als Nummern, und `conlang-take-pending-changes` übernimmt sie (es fragt Sie). Eine Sprache, die der Lauf angelegt hat, wird nach Ihrer Bestätigung ganz hinzugefügt.

Der Lauf liest die Bibliothek, bis er eine Sprache zum ersten Mal ändert, und danach seine eigene Kopie: Was Sie während des Laufs an einer Sprache ändern, sieht der Lauf nicht, geht aber auch nicht verloren.

<a id="limits"></a>

## Grenzen

- **Die Qualität liegt beim Modell.** Die Erweiterung prüft, was sie kostenlos prüfen kann (dass die zitierte Rede im Teil steht, die Sprache, die Wörter gegen das Lexikon und dessen Regeln für Komposita und Affixe) und meldet den Rest; sie prüft Ihre Grammatik nicht. Probieren Sie eine Sprache an einigen Teilen aus, bevor ein langer Lauf beginnt, und verwenden Sie **Die Sprache prüfen** des Designers, wenn ein Fehler sich wiederholt.
- **Eine Sprache wird von den Büchern geteilt, die sie verwenden.** Ein Wort, das Sie ändern, oder ein Wort, das eine Übersetzung hinzufügt, ändert es für jedes Buch der Reihe. Das ist so gewollt (eine Welt), und **Duplizieren** ist der Weg, eine Sprache getrennt zu halten.
- **Die Änderungen einer Sprache durch einen Agenten warten im Buch.** Sie übernehmen sie nach dem Anwenden des Laufs in die Bibliothek; eine Sprache wird während eines Laufs nicht gesperrt.
- **Befehle ersetzen kein Wort in der Geschichte**, und eine erneute Übersetzung kann die Übersetzungen nicht behalten, deren Englisch sich nicht geändert hat (jeder Teil des Bereichs wird erneut angefragt).
- **Ein Telefon** hat das Panel, den Editor, die Prüfung und den Assistenten, aber das Raster des Lexikons scrollt seitlich; das Antippen eines Redebeitrags im Betrachter funktioniert; die Seite der Geschichte (das Menü eines Teils, das Fenster für Bereiche, der Klick in die Geschichte) wurde dort nicht ausprobiert.
