---
label: Hörbuch
order: 27
---

# Hörbuch

Verwandelt ein Buch Teil für Teil in ein Hörbuch, mithilfe eines Text-to-Speech-Servers: eine Stimme für den Erzähler und eine für jede Figur, gespeichert
bei den Teilen des Buches und im Leser abspielbar. Du kannst es durchlaufen lassen (das Modell kennzeichnet, wer spricht und wie, der Sprachserver
liest vor) für einen groben Entwurf, oder die Teile einzeln im **Hörbuch-Studio** bearbeiten, dabei genau sehen, was an den Sprachserver geschickt wird,
und es von Hand ändern, und das Ergebnis als mp3-Dateien mit Kapitelmarken exportieren.

Die Erweiterung erzeugt selbst keine Sprache: Sie spricht mit einem **Chatterbox-TTS-Server**, den du betreibst (siehe
[Einrichten des Sprachservers](audiobook-server.md)). Der Text des Buches wird nie verändert und nur an die Server gesendet, die du angegeben hast;
das Audio wird in deiner eigenen Installation gespeichert, als Ressourcen, die mit den Teilen verknüpft sind.

<a id="what-you-need"></a>

## Was du brauchst

- Einen Sprachserver und, falls er sich die Grafikkarte mit deinem Sprachmodell teilt, die Anfragen, die Platz schaffen und ihn danach wieder stoppen
  ([Einrichten des Sprachservers](audiobook-server.md)).
- Stimmen: einen kurzen **Referenzclip** (12 bis 30 Sekunden einer Person, ohne Musik) für jede Stimme, die du klonen willst, oder eine der Stimmdateien,
  die der Server mitbringt. Ein Erzähler braucht eine ruhige, gleichmäßige Lesestimme.
- Für ein Buch mit mehreren Stimmen ein Sprachmodell, das den Text kennzeichnen kann. Standardmäßig ist es das Modell des Buches.
- **ffmpeg** ist optional. Ohne es funktioniert alles mit mp3. Mit ffmpeg auf dem Server (es ist im Docker-Image enthalten; auf einem Desktop wird es im `PATH` oder in den üblichen
  Ordnern wie `/opt/homebrew/bin` gefunden; die Variable `MARGINALIA_FFMPEG` nennt es, wenn es woanders liegt) bekommt der Export das Format **m4b**, und ein Administrator kann das Audio als wav speichern.

<a id="the-settings"></a>

## Die Einstellungen

*Einstellungen → Extension Settings → Audiobook Settings* ist das Panel deines Kontos; speichere die Seite *Einstellungen* nach einer Änderung.

![Das Panel mit einem Backend und den Sprechern](../../images/plugin-audiobook-settings.png)

### Backends

Ein Backend ist ein Sprachserver und das Modell, mit dem er spricht. Füge eines mit **+** hinzu.

| Einstellung | Was sie tut |
|---|---|
| Name | Der Name, der im Dialog Konvertieren angezeigt wird. |
| Adresse | Die Basisadresse des Sprachservers oder dessen, was an ihn weiterleitet, zum Beispiel `http://localhost:8004`. |
| Api-Schlüssel | Wird als Bearer-Token gesendet, wenn der Server (oder dein Proxy) einen verlangt. Er wird verschlüsselt gespeichert und nicht wieder angezeigt; gib einen neuen ein, um ihn zu ersetzen, oder **Remove the key**. |
| Sprachmodell | **Chatterbox Turbo** (Englisch, mit Klangmarken wie `[sigh]`, das schnellste und der Standard), **Chatterbox** (Englisch, mit einer *exaggeration* der Betonung) oder **Chatterbox Multilingual** (23 Sprachen). Die Anfragen vor einem Stapel müssen dieses Modell geladen lassen. Ein Buch, das ein anderes Modell braucht, verwendet ein anderes Backend. |
| GPU (Name) | Optional. Server, die sich eine GPU teilen, auch die des [Bildgenerators](image-generator.md), bekommen hier denselben Namen: Ihre Arbeit wartet dann aufeinander, und ein Backend für ein anderes Modell wartet, bis die anderen fertig sind. Leer: nur Stapel an dieselbe Adresse warten aufeinander. |
| Der Server läuft auf diesem Rechner oder in meinem Netzwerk | Erlaubt Adressen des lokalen Netzwerks. Lass es aus für einen Server in der Cloud: Dann kann er nicht dazu gebracht werden, deinen eigenen Rechner anzurufen. |
| Timeout einer Anfrage | Wie lange auf ein Stück Sprache gewartet wird. |
| Server nach (Stücken) neu starten | Für einen Sprachserver, der Speicher verliert und in einem langen Stapel vom System beendet wird: Nach so vielen Stücken werden die Anfragen nach dem Stapel und danach die Anfragen davor mitten im Stapel erneut gesendet (stoppen, starten, laden), und die Arbeit geht mit einem frischen Server weiter. 0 (der Standard) tut es nie. Ein Server, der abstürzt, wird unabhängig davon neu gestartet, siehe [Fehlerbehebung](audiobook-server.md#troubleshooting). |
| Schlüssel der Anfragen um einen Stapel | Der Schlüssel, den die Anfragen vor und nach einem Stapel als `Authorization: Bearer` mitführen. Er wird verschlüsselt gespeichert und nicht wieder angezeigt (**Remove the key** löscht ihn), damit er nicht in die Header jeder Anfrage geschrieben werden muss, wo er lesbar wäre. Leer: Es wird der *Api-Schlüssel* oben verwendet, was du willst, wenn ein Proxy wie LiteLLM alles bedient. Eine Anfrage, die einen eigenen `Authorization`-Header hat, behält ihn. |
| Erzählerstimme | Die Erzählerstimme eines Buches ohne Besetzung. Eine Stimmdatei des Servers wird als `predefined:Henry.wav` geschrieben. |
| Voreinstellungen eines Segments | Temperatur, CFG-Gewicht, Geschwindigkeit und Chunk-Größe, mit denen ein Stück beginnt (das Studio kann sie für ein einzelnes Stück ändern). |
| Vor einem Stapel, Nach einem Stapel | Die Anfragen, die um einen Stapel von Teilen gesendet werden: *Vor* gibt die Grafikkarte frei und startet den Sprachserver, *Nach* stoppt ihn. Siehe [Eine GPU für das Modell und die Sprache](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Leer tut nichts. |

**Test connection** tut dasselbe wie ein Stapel: Es sendet die Anfragen vor einem Stapel (damit ein gestoppter Sprachserver gestartet wird), fragt den Server, ob er erreichbar ist und ein Modell geladen hat,
und sendet die Anfragen nach dem Stapel. Ohne Anfragen fragt es nur ab.

Ein Buch, das Stücke in einer anderen Schrift hat (Japanisch in einem englischen Buch), schickt sie an das mehrsprachige Modell, falls du dafür ein Backend hast, mit ihrer Sprache; ohne eines bleiben sie beim Modell des Buches.

<a id="shared-speech-servers"></a>

### Gemeinsame Sprachserver

Auf einer Installation mit mehreren Benutzern kann der Administrator Sprachserver für alle bereitstellen, sodass ein GPU-Rechner einmal eingerichtet wird und niemand eine Adresse oder einen Schlüssel braucht. Der Administrator tut das im selben Panel (in den letzten Abschnitten, nur für Administratoren sichtbar): **Shared speech servers** öffnet ein Fenster mit denselben Feldern wie ein eigenes Backend, dazu, wer es nutzen darf: **all users** oder die in der Liste ausgewählten Benutzer. Ein Schalter dort, **Users may add speech servers of their own**, entscheidet, ob die Backends unten noch erlaubt sind.

Was du davon hast:

- **Backend in use** oben im Panel listet die gemeinsamen Server, die du nutzen darfst (mit *(shared)* gekennzeichnet), und deine eigenen. Das Fenster *Convert to audio* und das Studio verwenden das, das du auswählst; die Befehle verwenden es ebenfalls.
- Du siehst nur den Namen eines gemeinsamen Servers, nie seine Adresse, Schlüssel oder Anfragen. Die Stapel aller Benutzer an einer Serveradresse warten aufeinander, sodass zwei Benutzer nicht dieselbe GPU doppelt belegen können.
- Wenn der Administrator dir einen Server wegnimmt, wird ein Auftrag, der ihn noch nennt, abgelehnt; wähle dann einen anderen.
- Wenn der Administrator deine eigenen Server abschaltet, sagt das Panel es. Die, die du hinzugefügt hast, bleiben erhalten und werden wieder verwendet, sobald der Schalter wieder eingeschaltet wird.
- Die *Erzählerstimme* eines gemeinsamen Servers sollte eine Stimmdatei des Servers sein (`predefined:Henry.wav`): Ein Referenzclip ist eine Ressource eines einzelnen Benutzers und kann von den anderen nicht gelesen werden. Stimmen deiner Sprecher funktionieren wie immer.

<a id="actors"></a>

### Sprecher

Ein **Sprecher** ist eine Stimme von dir: ein Referenzclip (eine deiner Audio-[Ressourcen](../resources.md) oder einer, der hier hochgeladen wird) oder eine
Stimmdatei des Servers, mit eigenen Stimmen für eine **Stimmung** (ein Flüstern, ein Schrei) oder eine **Sprache** (ein Clip, der Japanisch spricht). Die Sprache hat Vorrang vor der Stimmung, und ohne eine Stimme für sie liest die Stimme des Sprechers. Deine Sprecher werden in deinem Konto gespeichert; jedes Buch kann einem Sprecher seinen Erzähler und seine Figuren zuweisen.

**Preview** neben einer Stimme (im Sprecher, den Einstellungen und der Besetzung) hat einen kurzen Satz, der mit ihr gesprochen wird (in der Sprache der Stimme bei einer Sprache), und spielt ihn ab; dafür wird der Sprachserver gestartet, falls die Anfragen vor einem Stapel das tun. Die Sprecher werden nach Namen aufgelistet.

![Ein Sprecher mit einer Stimme für eine Stimmung](../../images/plugin-audiobook-actor.png)

<a id="converting-new-parts-by-itself"></a>

### Neue Teile automatisch konvertieren

*Convert new parts to audio by itself* ist der Standard für alle deine Bücher; siehe [Automatische Konvertierung](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Die Besetzung und die Aussprache eines Buchs

Beide werden mit dem Buch gespeichert (sie sind im Backup enthalten) und werden im Dialog **In Audio umwandeln** bearbeitet.

**Besetzung** legt fest, wer liest: eine Stimme für den **Erzähler**, eine für **andere Sprecher** (wer vom Modell genannt wird, für den die Besetzung keine Stimme hat; leer: der Erzähler liest) und eine für jede **Figur**. Eine Figur hat einen Namen und weitere Namen, unter denen der Text sie nennen darf (*Tom* für *Tomáš Novák*), durch Kommas getrennt. Eine Rede wird der Figur zugeordnet, wenn ihr Name, einer ihrer weiteren Namen oder ein Wort des Namens, das keine andere Figur teilt, übereinstimmt, wobei Groß- und Kleinschreibung sowie Akzente ignoriert werden. **Figuren des Buchs hinzufügen** füllt die Liste mit den Figuren aus der Erzähl- und Blickperspektive und den auftretenden Figuren der Teile.

![Die Besetzung eines Buchs](../../images/plugin-audiobook-cast.png)

**Aussprache** legt fest, wie der Text *für die Stimme* geschrieben wird; das Buch selbst wird nie verändert. Das **Wörterbuch** ersetzt ganze Wörter (Groß- und Kleinschreibung zählt) durch das, was für sie gesprochen wird: eine Lautschreibung für einen Namen oder ein erfundenes Wort oder eine Lesung in Kana für Japanisch. Für ein Buch auf Englisch lesen die Regeln außerdem die übrigen Dinge vor: Zahlen, Ordnungszahlen, Jahre, Uhrzeiten, Geldbeträge und Abkürzungen als Wörter, eine Überschrift, die nur aus einer Zahl besteht, als *Chapter four. The Arch* (das Wort vor der Zahl bestimmst du selbst), Markdown-Zeichen wie `*` werden weggelassen, und ein Szenenwechsel (`***`, `---`) wird nicht vorgelesen, sondern führt zu einer längeren Pause. Schalte die Regeln für ein Buch in einer anderen Sprache aus; das Wörterbuch gilt weiterhin. **Einen Text testen** zeigt, wie ein Textstück gelesen wird. Ein Stück, dessen gesprochener Text sich ändert, wird neu erzeugt, die übrigen bleiben erhalten.

![Die Aussprache eines Buchs](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Ein Buch konvertieren

*Werkzeugmenü des Story-Editors (das Zahnradsymbol) → **In Audio konvertieren***.

![Der Dialog vor dem Start](../../images/plugin-audiobook-convert.png)

1. Wähle das **Backend** und drücke **Laden**: Die Teile mit Text des aktiven Zweigs werden aufgelistet (der erste ist 1), mit dem jeweiligen Status:
   *Nicht generiert*, *Beschriftet* (die Labels des Modells sind gespeichert, noch kein Audio), *Generiert* (mit einer Abspielschaltfläche und einem Download) oder
   *Generiert (der Text hat sich geändert)* (das Audio wurde aus einem älteren Text erstellt und wird neu erzeugt); *(final)* wird einem Teil angehängt, den das Studio als final markiert.
2. Lass **Teile mit einem Sprachmodell beschriften** für die einfache Version ausgeschaltet: Der Erzähler liest jeden Absatz. Schalte es ein, damit
   das Modell entscheidet, wer jede Redepassage spricht, wie (ruhig, flüsternd, schreiend ...) und wo ein Klangtag passt; die Besetzung liefert die
   Stimmen. Das Modell des Buchs ist vorausgewählt; wähle bei Bedarf ein anderes. Die Teile werden zuerst beschriftet und danach gesprochen. Ein Teil, der bereits Labels für seinen Text hat, wird
   auch bei ausgeschaltetem Kästchen mit diesen Labels gelesen (das Modell wird nicht gefragt). Wenn du also ein beschriftetes Buch ohne Kästchen erneut konvertierst, wird der Dialog nicht zu einfacher Erzählung. **Labels entfernen** ist der Weg, um einfache Erzählung oder neue Labels anzufordern.
3. Markiere die Teile (das Kästchen in der Kopfzeile markiert alle) und **plane**, was mit ihnen geschehen soll. Die Spalte **Plan** zeigt es an, es wird aber noch nichts ausgeführt:
   **Generieren** (fehlendes erzeugen; ein aktueller Teil wird nicht erneut erzeugt), **Neu generieren** (Audio löschen, Labels entfernen, generieren),
   **Audio löschen** (die Schnipsel und das zusammengefügte Audio des Teils; die Labels bleiben), **Labels entfernen** (das Modell wird beim nächsten Erzeugen des Teils erneut gefragt), **Plan leeren**. Schritte addieren sich bei einem Teil (Audio löschen und Labels entfernen ohne Generieren räumen nur auf); ein als final markierter Teil wird aus jedem Plan ausgelassen.
4. **Starten** führt den Plan aus. Wenn Audio oder Labels entfernt werden, wird vorher nachgefragt. Was zu generieren ist, landet in deiner Warteschlange, die weiterläuft, wenn du das Fenster schließt
   oder die Seite verlässt; ein Teil, der wartet oder in der Warteschlange läuft, wird aus einem Plan ausgelassen. Der Status, die bisher erzeugten Schnipsel und die Meldung des Auftrags werden in der Zeile des Teils angezeigt.
   Während das Modell einen Teil beschriftet, zeigt seine Zeile *Beschriftung der Schnipsel 17–32 von 120* (das Modell wird jeweils zu 16 Schnipseln befragt). **Klicke auf den Status** eines beschrifteten Teils, um für jede Anfrage die Überlegungen und die Antwort des Modells zu lesen, sobald sie eintreffen. Sie werden zusammen mit dem Auftrag im Speicher gehalten, bis die abgeschlossenen Aufträge entfernt werden (*Fertige entfernen*) oder die Anwendung neu startet.

![Die Warteschlange: bisher erzeugte Schnipsel und ein Download, wenn ein Teil fertig ist](../../images/plugin-audiobook-queue.png)

So entsteht ein Teil, damit du weißt, was zu erwarten ist:

- Der Text wird **vom Programm** geteilt, nie vom Modell, in Schnipsel mit Erzählung und Schnipsel mit direkter Rede (zwischen geraden oder typografischen doppelten Anführungszeichen oder japanischen Eckklammern; eine Überschrift ist Erzählung). Das Modell *beschriftet* nur die nummerierten Schnipsel. Ein Text
  mit Klangtags wird nur verwendet, wenn es der Schnipsel mit hinzugefügten Tags ist und sonst nichts verändert wurde, damit das Modell dein Buch nie verändern kann.
- **Wer einen Schnipsel spricht, entscheidet das Modell bei Rede; Erzählung gehört dem Erzähler.** Ein Brief, eine Notiz, eine Nachricht oder die Gedanken einer Figur haben keine Anführungszeichen, daher nennt das Programm sie Erzählung. Ein solcher Schnipsel wird nur dann von der Figur (der Stimme der Besetzung) gelesen, wenn er **kursiv beginnt** (`*...*` oder `_..._`) und das Modell die Figur aus dem Kontext nennt (eine Unterschrift, „die Handschrift war unverkennbar“). Erzählung in der dritten Person, auch wenn sie aus der Sicht einer Figur erzählt wird („Sie sah zu, wie Percy durch den Raum ging“), gehört immer dem Erzähler: Das Programm ignoriert eine Figur, die das Modell einem Schnipsel ohne Kursivschrift zuweist. Kursivschrift zur Betonung, für Laute oder Fremdwörter ist ebenfalls Erzählung. Die Worte bleiben immer deine.
- **Die Anweisung an das Modell ist eine Vorlage, die du ändern kannst.** *Einstellungen → Einstellungen der Erweiterung → Audiobook-Einstellungen → Labelling prompt* ist dein Standard; ein Buch (Reiter *Prompts* → *Einstellungen der Erweiterung* → *Audiobook*) oder ein Ordner kann eine eigene haben. Leer bedeutet die des übergeordneten Ordners, dann deine, dann die eingebaute. Sie wird wie die anderen [Prompts](../books/prompts.md) geschrieben: Die Variablen sind `{{characters}}`, `{{deliveries}}`, `{{tags}}` und `{{wordsPerTag}}`, und das Hinweis-Popover hat **Standardvorlage einfügen**. Eine geänderte Anweisung sorgt dafür, dass die Labels der Teile beim nächsten Erzeugen erneut angefordert werden.
- **Die Lautstärke der Teile wird angeglichen**, wenn das Programm ffmpeg installiert ist (dasselbe, das die `.m4b` erstellt): Das zusammengefügte Audio jedes Teils wird auf eine Lautheit gebracht (EBU R128, linear, damit die Dynamik erhalten bleibt; **-18 LUFS**, sofern du es nicht in *Einstellungen → Einstellungen der Erweiterung → Audiobook-Einstellungen* änderst, wo du es auch ausschalten kannst). Die Schnipsel
  bleiben so, wie der Server sie erzeugt hat; nur das zusammengefügte Teil wird angeglichen, damit ein Hörbuch von Teil zu Teil nicht in der Lautstärke springt. **Zuvor erzeugte Teile** werden nicht angefasst, bis du sie erneut konvertierst: Dann werden sie aus den gespeicherten Schnipseln neu zusammengefügt und angeglichen, und der Sprachserver wird dafür nicht befragt (auch das Sprachmodell wird nicht entladen). Wenn du die Angleichung ausschaltest, bleiben die bereits angeglichenen Teile so, wie sie sind. Schlägt ffmpeg bei einem Teil fehl, behält er sein Audio wie es war, und das Ergebnis meldet das.
- Alle Teile eines Klicks werden in **einem Stapel pro Sprachmodell** erzeugt: Die Anfragen vor dem Stapel laufen einmal, danach werden alle Schnipsel gesprochen, dann laufen die Anfragen nach dem Stapel einmal. So wechselt die Grafikkarte nicht bei jedem Teil das Modell.
- Jeder Schnipsel wird **nach seinem Inhalt gespeichert** (Text, Stimme, Modell, Sprache, Einstellungen, Seed). Ein Teil, der bereits aktuell ist, kostet nichts; ein geänderter Absatz ist der einzige Schnipsel, der erneut erzeugt wird, und ein Teil, der fehlschlug, behält die bereits erzeugten Schnipsel, wenn du **Wiederholen** drückst.
- Die Schnipsel werden mit Pausen zusammengefügt, die von ihrer Stelle abhängen (nach einer Überschrift, zwischen Absätzen, vor *sagte er*) zu einer mp3 pro Teil, die beim Teil gespeichert wird.
- Während eine Generierung von dir läuft, wartet die Sprachausgabe, damit das Sprachmodell darunter nicht entladen wird.
- Ein Teil, das du im Studio als **final** markiert hast, bleibt unangetastet, und der Dialog sagt, wie viele es waren.
- **Das ganze Hörbuch** fügt jeden Teil mit aktuellem Audio zu einer mp3 mit einer Kapitelmarke pro Teil zusammen und zeigt einen Downloadlink unter der Liste (die ausgelassenen Teile werden genannt). Dieselbe Datei ist das Exportformat *eine mp3 mit Kapitelmarken*.
- **Alle stoppen** bricht die wartenden Teile ab und stoppt den laufenden sofort: Die Anfrage an das Sprachmodell (das Beschriften) oder an den Sprachserver (den gerade gesprochenen Schnipsel) wird verworfen, nicht abgewartet. Ein Stapel, der vor seinem Beginn gestoppt wird, sendet die Anfragen davor nicht, sodass das Sprachmodell geladen bleibt. **Herunterladen** liefert die mp3 eines fertigen Teils.
<a id="the-audiobook-studio"></a>

## Das Hörbuchstudio

*Werkzeuge-Menü → **Audiobook studio*** (die Stimme des ausgewählten Stücks mit einer Vorschau steht neben seinem Lautsprecher) ist der Ort, an dem das fertige Hörbuch Teil für Teil entsteht: Sie sehen das Skript, ändern es, hören ein Stück an und erstellen den Teil.

![Das Studio mit einer ausgewählten Rede](../../images/plugin-audiobook-studio.png)

- Während der Teil kommentiert oder erstellt wird, zeigt eine Leiste, dass etwas läuft, und die Schaltflächen, die weitere Arbeit starten würden, warten.
- **Kommentieren** erstellt das Skript des Teils (mit dem Sprachmodell, wenn Sie das wünschen, sonst mit dem Erzähler) und zeigt die Stücke in einer Tabelle: Sprecher, Vortrag, Intensität, den gesprochenen Text und eine Markierung für ein Stück, das Sie geändert haben. Während es läuft, erscheint daneben eine Schaltfläche **Stopp**, die die Anfrage an das Sprachmodell beendet.
- Wählen Sie ein Stück aus, um seinen **Sprecher**, den **gesprochenen Text**, den **Vortrag**, die **Intensität** (die Übertreibung der Stimme, bei Modellen, die diese besitzen), die **Sprache**, den **Seed**, die **Pause nach** ihm zu bearbeiten. **Anwenden** behält die Änderung bei und **sperrt** das Stück: Eine neue Kommentierung lässt ein gesperrtes Stück unverändert.
  **Entsperren** gibt es wieder für die Kommentierung frei. *Die Anfrage, die der Sprachserver erhält* zeigt die genaue Anfrage.
- **Anhören** erstellt das Audio eines Stücks (nur dieses Stück; der Sprachserver wird dafür gestartet). **Neue Fassung** erstellt es mit einem neuen Seed erneut, und die **Fassungen** bleiben erhalten, damit Sie die gewünschte auswählen können (*Diese Fassung verwenden und anhören*). Eine gute Stimme hängt oft vom Seed ab.
- **Teil aus diesem Skript erstellen** fügt die Stücke so zusammen, wie sie sind, ohne eine neue Kommentierung.
- Der **Status** des Teils ist *Entwurf*, *überprüft* oder *final*. Ein finaler Teil wird nicht erneut kommentiert oder umgewandelt, und der Export kann auf die finalen Teile beschränkt werden.

<a id="exporting"></a>

## Exportieren

Das Hörbuch ist ein Format von *Geschichte exportieren* des Buchs: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** und, wenn ffmpeg installiert ist, **Audiobook (one m4b with chapters)**. Sie bestehen aus dem Audio der Teile, die aktuell sind: Ein Teil ohne Audio oder mit Audio, das älter ist als sein Text oder als sein Skript im Studio, wird ausgelassen, und das Fenster sagt, welche. Die mp3-Dateien tragen ID3-Tags (den Titel des Buchs, das Kapitel, die Spurnummer), und die einzelne Datei hat Kapitelmarken, die Player wie VLC und die meisten Hörbuch-Apps anzeigen; die zip-Datei enthält eine Wiedergabeliste. Das Kontrollkästchen **Audiobook: only the parts marked final** beschränkt den Export auf die Teile, die Sie im Studio abgeschlossen haben.

Die **m4b** ist das Format, das Apple Books und die meisten Hörbuch-Apps auf dem Telefon verlangen: AAC-Audio (64 kbit/s, für Sprache ausreichend) in einem MP4-Container mit den Kapiteln, dem Titel, dem Autor und dem Genre. ffmpeg erstellt sie, daher wird das Format nur auf einem Server angeboten, der ffmpeg hat. Audio, das als mp3 gespeichert ist, wird dafür ein zweites Mal kodiert; Audio, das als wav gespeichert ist, wird nur einmal kodiert.

<a id="how-the-audio-is-stored"></a>

### Wie das Audio gespeichert wird

Ein Administrator wählt dies für die gesamte Installation unter *Einstellungen → Extension Settings → Audiobook Settings* (der letzte Abschnitt, nur für Administratoren sichtbar):

- **mp3** (Standard): klein, ohne Decoder zusammengefügt, kein ffmpeg nötig. Die m4b ist ein zweiter verlustbehafteter Schritt.
- **wav**: verlustfrei, etwa zehnmal so groß (eine Stunde Sprache sind etwa 170 MB), benötigt ffmpeg. Die Stücke werden vom Sprachserver als wav angefordert und als wav zusammengefügt; der Browser spielt sie unverändert ab;
  die mp3- und zip-Exporte werden von ffmpeg kodiert (96 kbit/s), und die m4b wird nur einmal kodiert.

Die Auswahl gilt für alles, was ab dann erstellt wird. Vorher erstellte Teile behalten ihr Audio und werden so verstanden, wie sie sind; ein Teil beider Arten (ein gesperrtes Stück aus dem Studio von früher) wird als wav zusammengefügt, und ein Export von Teilen beider Arten funktioniert, benötigt aber ffmpeg.

![Der Exportdialog mit den Hörbuchformaten](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Automatische Umwandlung

Ein Buch kann das Audio eines neuen Teils selbstständig erstellen, sobald der Teil final ist: nach der automatischen Kommentierung, der Überarbeitung und den Übersetzungen, falls das Buch sie hat, als letzter Schritt von [was auf einen erzeugten Teil folgt](../books/story-editor.md#what-follows-a-generated-part) (im Bereich der Läufe, bis das Audio erstellt ist; dessen **Stopp** nimmt den Teil aus der Warteschlange). Sie ist standardmäßig **aus**. Wählen Sie sie im Reiter *Prompts* des Buchs unter den Erweiterungseinstellungen, für einen Ordner mit Büchern oder für alle Ihre Bücher in den Hörbucheinstellungen; die nächstliegende gilt:

- **Nein**,
- **Ja: der Erzähler liest den Teil**,
- **Ja: vom Modell des Buchs gekennzeichnet, die Besetzung gibt die Stimmen vor**; ein Buch ohne Modell erhält die einfache Erzählung, und Sie werden darauf hingewiesen.

![Die Einstellung in einem Buch](../../images/plugin-audiobook-auto.png)

Der Teil kommt in Ihre Warteschlange, mit der Besetzung und der Aussprache des Buchs. Ein Hinweis teilt Ihnen mit, dass er eingereiht ist, und ein weiterer, dass sein Audio fertig oder fehlgeschlagen ist; sie erscheinen nur auf der Seite, die die Erstellung gestartet hat, der Auftrag ist in jedem Fall im Fenster *Convert to audio* zu sehen.

<a id="listening"></a>

## Anhören

Ein veröffentlichtes Buch (oder der Leser) mit Teilen, die Audio haben, erhält eine Player-Leiste: Wiedergabe, Pause, Stopp, die Geschwindigkeit, der gerade gehörte Teil im Text markiert. Sie beginnt bei der Auswahl oder beim ersten sichtbaren Teil, spielt die Teile der Reihe nach ab und lädt den nächsten, während der aktuelle läuft, und die Medienbedienelemente des Geräts kennen das Buch. Audio, das aus einem älteren Text eines Teils erstellt wurde, wird nicht angeboten. Das Audio wird dem Besitzer des Buchs bereitgestellt und, solange das Buch veröffentlicht ist, jedem, der es öffnen kann; ein Buch, das aus der Veröffentlichung zurückgenommen wird, wird sofort nicht mehr ausgeliefert (beim nächsten Teil, den der Player lädt).

![Der Player im Leser](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Ein Teil ohne Audio

Der Player hält bei einem Teil ohne Audio an, damit kein Teil der Geschichte übersprungen wird, und sagt dies. Wenn die Erweiterung [Read Aloud](tts-reader.md) installiert ist, zeigt die Leiste außerdem eine Lautsprecher-Schaltfläche, **Read this part (and the next ones without audio) with the voice of the browser**. Nichts wird mit einer maschinellen Stimme gelesen, solange Sie sie nicht drücken. Dann liest sie diesen Teil und die folgenden Teile ohne Audio mit der Stimme Ihres Geräts, und sobald sie einen Teil mit Audio erreicht, übernimmt der Player wieder. Spätere Teile ohne Audio beim selben Zuhören werden ebenso gelesen, ohne erneut zu fragen; **Stopp** schaltet es stumm, und ein erneuter Start des Players fragt wieder nach. Stimme, Geschwindigkeit und Tonhöhe sind die von Read Aloud.

<a id="commands"></a>

## Befehle

Dasselbe wie [Befehle](../commands/index.md) (die Befehlszeile des Geschichteneditors, das Makro `command` einer Vorlage, ein Agent). Teile werden so nummeriert, wie der Konvertieren-Dialog sie nummeriert, oder über ihren Bezeichner angesprochen.

| Befehl | Was er tut |
|---|---|
| `/audiobook-status [from [to]]` | Der verwendete Sprachserver, die Art, wie das Buch sich selbst umwandelt, wie viele Teile Audio haben (keines, bereit, älter als der Text, älter als das Skript), der Status jedes aufgelisteten Teils im Studio und die Warteschlange. Nur bei einem langen Buch langsam. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Stellt die Teile in die Warteschlange (ohne Argumente das ganze Buch, bei nur `from` ein einzelner Teil). `labelling` `true` verwendet das Modell des Buchs; `what` ist `audio` oder `script` (nur das Skript für das Studio, nichts wird gesprochen). Mit `wait` `true` bleibt der Befehl bis die Aufträge beendet sind, zeigt den Fortschritt und meldet, wie jeder ausging; ein Abbruch des Befehls bricht die Aufträge ab, die noch nicht begonnen haben. |
| `/audiobook-stop` | Bricht die wartenden Teile ab; der laufende hält nach seinem Stück an. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` oder `final` im Studio; der Teil braucht ein Skript. |
| `/audiobook-cast` | Der Erzähler, der Ersatz und die Figuren der Besetzung mit ihren Sprechern. |

![Das Ergebnis eines Befehls](../../images/plugin-audiobook-command.png)

Das Audio wird für **Ihr** Buch erstellt, nicht für die verborgene Arbeitskopie eines [Agenten](../agents.md): Ein Agent kann die Besetzung lesen und erhält bei den anderen Befehlen eine Ablehnung mit dem Hinweis, dass Sie sie ausführen müssen. Führen Sie sie über die Befehlszeile aus, oder geben Sie einem Agenten die Aufgabe, das Buch vorzubereiten (kommentieren, überprüfen), und wandeln Sie es selbst um.
<a id="notifications"></a>

## Benachrichtigungen

Wenn ein Stapel von Umwandlungen abgeschlossen ist, kann dir der Browser Bescheid geben ([Benachrichtigungen](../account.md#notifications), damit du den Tab offen lassen kannst, während der Sprachserver arbeitet). In der Benachrichtigungsliste der Seite *Einstellungen* gibt es zwei Zeilen: **Convert to audio** (die Stapel, die du im Dialog oder mit dem Befehl startest) und **Convert to audio (automatic)** (ein neuer Teil, der automatisch in Audio umgewandelt wurde). Ein Stapel, den du abgebrochen hast, erzeugt keine Benachrichtigung; ein Stapel mit einem fehlgeschlagenen Teil meldet, dass er fehlgeschlagen ist.

![Die Benachrichtigungsliste](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Was es (noch) nicht kann

- Nur Mp3 und Wav (kein Opus); die Lautstärke der Teile ist das, was die Stimmen liefern.
- Keine Soundeffekte oder Atmosphäre.
- Eine Sprach-Engine, Chatterbox. Weitere Engines sind der nächste Schritt.
- Das Probehören einer Stimme außerhalb eines Segments.
- Die Besetzung ist nicht Teil der Sicherung der *Schauspieler* eines Buches: Ein wiederhergestelltes Buch behält seine Besetzung, nennt aber Schauspieler, die in deinem Konto vorhanden sein müssen.
