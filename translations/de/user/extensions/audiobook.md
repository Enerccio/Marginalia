---
label: Hörbuch
order: 27
---

# Hörbuch

Verwandelt ein Buch Teil für Teil in ein Hörbuch mit einem Sprachserver: eine Stimme für den Erzähler und eine für jede Figur, bei den Teilen des Buches gespeichert und im Leser abspielbar. Du kannst es laufen lassen (das Modell legt fest, wer spricht und wie, der Sprachserver liest vor), etwa für einen groben Entwurf. Oder du bearbeitest die Teile einzeln im **Audiobook studio**, siehst genau, was an den Sprachserver geschickt wird, änderst es von Hand und exportierst das Ergebnis als mp3-Dateien mit Kapitelmarken.

Die Erweiterung erzeugt selbst keine Sprache. Sie spricht mit einem **Chatterbox-TTS-Server**, den du betreibst (siehe [Einrichten des Sprachservers](audiobook-server.md)). Der Text des Buches wird nie verändert und nur an die Server geschickt, die du angegeben hast. Das Audio wird in deiner eigenen Installation gespeichert, als Ressourcen, die mit den Teilen verknüpft sind.

<a id="what-you-need"></a>

## Was du brauchst

- Einen Sprachserver und, falls er sich die Grafikkarte mit deinem Sprachmodell teilt, die Anfragen, die Platz schaffen und ihn danach wieder anhalten ([Einrichten des Sprachservers](audiobook-server.md)).
- Stimmen: einen kurzen **Referenzclip** (12 bis 30 Sekunden einer Person, ohne Musik) für jede Stimme, die du klonen willst, oder eine der Stimmdateien, die der Server mitbringt. Ein Erzähler braucht eine ruhige, gleichmäßige Lesestimme.
- Bei einem Buch mit mehreren Stimmen ein Sprachmodell, das den Text beschriften kann. Standardmäßig ist das das Modell des Buches.
- **ffmpeg** ist optional. Ohne es funktioniert alles mit mp3. Mit ffmpeg auf dem Server (es ist im Docker-Image enthalten, auf einem Desktop wird es im `PATH` oder in üblichen Ordnern wie `/opt/homebrew/bin` gefunden; die Variable `MARGINALIA_FFMPEG` nennt den Pfad, wenn es woanders liegt) bekommt der Export das Format **m4b**, und ein Administrator kann das Audio als wav speichern.

<a id="the-settings"></a>

## Die Einstellungen

*Einstellungen → Extension Settings → Audiobook Settings* ist das Panel deines Kontos. Speichere die Seite *Einstellungen* nach einer Änderung.

![Das Panel mit einem Backend und den Akteuren](../../images/plugin-audiobook-settings.png)

### Backends

Ein Backend ist ein Sprachserver und das Modell, mit dem er spricht. Füge eines mit **+** hinzu. **Duplicate** kopiert das ausgewählte Backend (Adresse, Schlüssel, GPU-Sperre, Stimme, Standardwerte sowie die Anfragen vor und nach einem Batch) unter einem neuen Namen. So bekommst du schnell ein Backend mit einem anderen Modell auf demselben Rechner, zum Beispiel Higgs neben Chatterbox. Dort änderst du dann das Modell, die Adresse und die Anfragen zum Starten und Anhalten.

| Einstellung | Was sie bewirkt |
|---|---|
| Name | Der Name, der im Convert-Dialog angezeigt wird. |
| Address | Die Basisadresse des Sprachservers oder dessen, was an ihn weiterleitet, zum Beispiel `http://localhost:8004`. |
| Api key | Wird als Bearer-Token gesendet, wenn der Server (oder dein Proxy) eines verlangt. Er wird verschlüsselt gespeichert und nicht mehr angezeigt. Gib einen neuen ein, um ihn zu ersetzen, oder nutze **Remove the key**. |
| Speech model | **Chatterbox Turbo** (Englisch, mit Klangmarken wie `[sigh]`, am schnellsten und der Standard), **Chatterbox** (Englisch, mit einer *exaggeration* der Betonung), **Chatterbox Multilingual** (23 Sprachen) oder **Higgs Audio v3 in ComfyUI** (viele Sprachen, Stimmen, die die Stimmung einer Zeile treffen, und Laute wie Weinen, Keuchen oder Stöhnen; siehe [unten](#higgs-audio-v3-emotions-and-sounds)). Die [GPU-Sperre](../administration/gpus.md) des Backends (oder die Anfragen vor einem Batch) muss dieses Modell geladen lassen. Ein Buch, das ein anderes Modell braucht, verwendet ein anderes Backend. |
| GPU lock | Die [GPU-Sperre](../administration/gpus.md) der GPU, auf der der Server läuft, wenn er sie sich mit dem Sprachmodell oder anderen Servern teilt (auch mit dem [Image Generator](image-generator.md)). Jeder Batch und jede Probe wartet auf seinen Zug an der GPU, und die Sperre lädt zuerst den Sprachserver. Wird nur angezeigt, wenn der Administrator GPU-Sperren eingerichtet hat. |
| The server is on this machine or on my network | Erlaubt Adressen des lokalen Netzwerks. Lass es ausgeschaltet bei einem Server in der Cloud: dann kann er nicht dazu gebracht werden, deinen eigenen Rechner anzurufen. |
| Timeout of a request | Wie lange auf ein Stück Sprache gewartet wird. |
| Restart the server after (pieces) | Für einen Sprachserver, der Speicher verliert und vom System in einem langen Batch beendet wird: Nach so vielen Teilen werden die Anfragen nach dem Batch und dann die davor mitten im Batch erneut gesendet (anhalten, starten, laden), und die Arbeit geht mit einem frischen Server weiter. 0 (der Standard) tut das nie. Ein Server, der abstürzt, wird unabhängig davon neu gestartet, siehe [Fehlerbehebung](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | Der Schlüssel, den die Anfragen vor und nach einem Batch als `Authorization: Bearer` mitschicken. Er wird verschlüsselt gespeichert und nicht mehr angezeigt (**Remove the key** leert ihn), sodass er nicht in die Header jeder Anfrage geschrieben werden muss, wo er lesbar wäre. Ist er leer, wird der *Api key* oben verwendet. Das ist richtig, wenn ein Proxy wie LiteLLM alles bedient. Eine Anfrage, die einen eigenen `Authorization`-Header hat, behält ihn. |
| Narrator voice | Die Erzählerstimme eines Buches ohne Besetzung. Eine Stimmdatei des Servers schreibt man `predefined:Henry.wav`. |
| Precision | Nur Higgs: die Genauigkeit, mit der das Modell rechnet. **fp32** (der Standard) ist die sichere Wahl. Auf einer AMD-Karte haben **bf16**-Stücke Kauderwelsch, Klangeffekte und falsche Stimmen erzeugt. bf16 braucht etwa halb so viel Grafikspeicher (etwa 8 GB statt 16) und ist dort, wo es funktioniert, schneller. |
| Send the text of the clip to Higgs | Nur Higgs, experimentell: Der [Text des Clips](#voices-and-clips) wird mitgeschickt. Lass es ausgeschaltet: In Tests hat es die Stimme verändert. |
| Check the length of the pieces | Standardmäßig an. Ein Stück, das weit länger ist als sein Text (4 s plus 0,6 s pro Wort: Das Modell hat weitergeredet oder einen Laut statt der Wörter gemacht), wird mit dem nächsten Seed erneut erzeugt, bis zu so vielen Malen, wie **Wiederholungen** angibt. Der erste Durchgang in der richtigen Länge wird behalten, sonst der kürzeste. |
| Check the words with speech recognition | Standardmäßig aus; benötigt die [Spracherkennung](#speech-recognition). Nach jedem Batch wird jedes Stück transkribiert und mit seinem Text verglichen. Ein Stück, das bei mehr als **Max. word errors (%)** seiner Wörter abweicht, wird mit dem nächsten Seed erneut erzeugt und erneut geprüft, bis zu **Wiederholungen** Runden. Der Durchgang mit den wenigsten falschen Wörtern wird behalten. Die Konvertierung meldet, wie viele Stücke erneut erzeugt wurden und wie viele immer noch abweichen. |
| Defaults of a segment | Temperatur, CFG-Gewicht, Geschwindigkeit und Chunk-Größe, mit denen ein Stück beginnt (das Studio kann sie für ein einzelnes Stück ändern). Higgs verwendet nur die Temperatur. |
| Before a batch, After a batch | Anfragen, die um jeden Batch von Teilen (und jede Probe) gesendet werden, innerhalb der GPU-Sperre. Die Grafikkarte freizugeben und den Sprachserver zu starten ist Aufgabe der GPU-Sperre; siehe [Eine GPU für Modell und Sprache](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Leer bedeutet: nichts tun. |

**Test connection** macht, was ein Batch macht: Es nimmt die GPU-Sperre (sodass ein angehaltener Sprachserver gestartet wird), sendet die Anfragen vor einem Batch, fragt den Server, ob er erreichbar ist und ein Modell geladen hat, und sendet dann die Anfragen nach dem Batch. Ohne Sperre oder Anfragen fragt es nur ab.

Ein Buch, das Teile in einer anderen Schrift hat (Japanisch in einem englischen Buch), schickt sie an das mehrsprachige Modell, wenn du dafür ein Backend hast, und zwar mit ihrer Sprache. Ohne ein solches Backend bleiben sie beim Modell des Buches. Ein Higgs-Buch behält sie: Higgs liest sie selbst, mit derselben Stimme.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: Emotionen und Geräusche

Mit einem **Higgs Audio v3 in ComfyUI** als Backend werden die Markierungen des Sprachmodells hörbar. Die Adresse des Backends ist die Ihrer ComfyUI (mit den Knoten der TTS Audio Suite, siehe [Einrichten des Sprachservers](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- Der **Vortrag** einer Sprachpassage wird gesprochen: sad, angry, joyful, amused, tense, fearful, surprised, tender, sarcastic, pleading, desperate, disgusted, aroused, a whisper oder a shout. Eine starke Stufe (Intensität 0.8 und mehr) klingt ausdrucksstärker, eine schwache (unter 0.2) wird schlicht gelesen. Der Erzähler liest schlicht.
- Das Sprachmodell darf mehr **Geräusche** in die Sprache legen als bei Chatterbox Turbo: laugh, chuckle, giggle, sigh, gasp, cough, clear throat, sniff, sneeze, groan, cry, sob, scream, pant, moan, hum und shush.
- Das Programm liest außerdem die **Erzählung um jede Sprachpassage** (auf Englisch) und gibt der Passage das Geräusch, das sie nennt, sofern das Modell keines gesetzt hat: *"You look funny," she giggled.* bekommt ein giggle, *She burst out laughing. "Careful!"* ein laugh, *a soft moan escaped her lips. "Yes..."* ein moan, *panting* oder *out of breath* ein panting, *cried quietly* oder *burst into tears* ein crying. Es lässt weg, was nur wie ein Geräusch aussieht: *"Stop!" she cried.* (ein Ausruf), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, sowie ein Geräusch, das die Erzählung einer anderen benannten Figur gibt. *"You look funny," he mocked her.* bekommt einen sarcastic oder amused Vortrag und kein Geräusch. Die Erzählung selbst wird wie geschrieben gelesen.
- **Der Erzähler wird immer schlicht gelesen**: ohne Geräusche und ohne Stimmung, auch bei einer Passage, die das Modell dem Erzähler überlassen hat.
- Higgs braucht keinen Clip pro Stimmung: ein Clip einer Stimme genügt. Er kopiert die Aufnahme des Clips, deshalb ist ein sauberer Clip wichtiger als bei Chatterbox ([Ein guter Clip](#a-good-clip)).
- Jede Passage wird von ComfyUI erzeugt, und danach wird der Speicher geleert (einige Sekunden pro Passage), was verhindert, dass manche Grafikkarten abstürzen.
- Higgs spricht nach dem letzten Wort einer Passage oft weiter (ein Atemzug, der Beginn eines anderen Geräuschs), und der Clip endet mitten darin, was als kurzes „Whoosh“ vor der Pause zu hören ist. Mit **ffmpeg** schneidet die Erweiterung diesen Ausklang ab und blendet jede Passage aus; ohne sie bleiben die Passagen so, wie Higgs sie erzeugt hat.
- Das Higgs-Audio-v3-Modell steht unter einer **Forschungs- und Nichtkommerz-Lizenz**: Ein Hörbuch, das Sie verkaufen, braucht eine Lizenz von Boson AI.

<a id="speech-recognition"></a>

### Spracherkennung

**Speech recognition** schreibt auf, was eine Sprachpassage sagt. Sie prüft die Passagen eines Stapels (wenn ein Backend **Check the words with speech recognition** aktiviert hat) und erkennt den [Text eines Stimmclips](#voices-and-clips). Wählen Sie, wo sie läuft:

| Einstellung | Was sie tut |
|---|---|
| Spracherkennung | **ComfyUI (Granite ASR)**: das Granite-Sprachmodell der TTS Audio Suite in Ihrer ComfyUI (die Adresse ist die von ComfyUI). **OpenAI-compatible (whisper)**: jeder Server mit `/v1/audio/transcriptions`, etwa ein Whisper-Server (faster-whisper, speaches) oder die OpenAI-API. |
| Adresse, API-Schlüssel | Die Adresse von ComfyUI oder die Basis der API (mit oder ohne `/v1`); der Schlüssel wird verschlüsselt gespeichert. Leere Adresse: keine Spracherkennung. |
| GPU-Sperre | Die [GPU-Sperre](../administration/gpus.md), die die Erkennung belegt. Bei einer Karte mit fp32-Higgs geben Sie ihr eine eigene Sperre, deren Schritte ComfyUI neu starten: ComfyUI hält das Sprachmodell im Speicher des Servers, und das Laden der Erkennung daneben brachte den Server an die Speichergrenze. |
| Modell der Erkennung | Leer: `granite-speech-4.1-2b` für ComfyUI, `whisper-1` für den OpenAI-kompatiblen Server. |
| Timeout, Der Server läuft auf diesem Rechner oder in meinem Netzwerk | Wie bei einem Backend. |

**Test connection** prüft, ob ComfyUI die Knoten hat oder ob der Server antwortet. Die Passagen eines Stapels werden nach dem Stapel geprüft, wenn der Sprachserver wieder freigegeben wird, sodass Erkennung und Sprachmodell nie gleichzeitig die Karte brauchen.

<a id="shared-speech-servers"></a>

### Gemeinsame Sprachserver

Auf einer Installation mit mehreren Benutzern kann der Administrator Sprachserver für alle bereitstellen. So wird eine GPU-Maschine einmal eingerichtet, und niemand braucht eine Adresse oder einen Schlüssel. Der Administrator tut das im selben Bereich (die letzten Abschnitte, nur für Administratoren sichtbar): **Shared speech servers** öffnet ein Fenster mit denselben Feldern wie ein eigenes Backend, dazu die Angabe, wer ihn nutzen darf: **all users** oder die in der Liste ausgewählten Benutzer. Ein Schalter dort, **Users may add speech servers of their own**, entscheidet, ob die Backends darunter weiterhin erlaubt sind.

Was Sie davon haben:

- **Backend in use** oben im Bereich listet die gemeinsamen Server, die Sie nutzen dürfen (mit *(shared)* markiert), und Ihre eigenen. Das Fenster *Convert to audio* und das Studio nutzen den, den Sie wählen; die Befehle nutzen ihn ebenfalls.
- Sie sehen nur den Namen eines gemeinsamen Servers, nie seine Adresse, Schlüssel oder Anfragen. Die Stapel aller Benutzer auf einer Serveradresse warten aufeinander, sodass zwei Benutzer nicht dieselbe GPU doppelt starten können.
- Wenn der Administrator Ihnen einen Server entzieht, wird ein Auftrag, der ihn noch nennt, abgelehnt; wählen Sie dann einen anderen.
- Wenn der Administrator Ihre eigenen Server abschaltet, zeigt der Bereich das an. Die von Ihnen hinzugefügten Server bleiben erhalten und werden wieder genutzt, wenn der Schalter zurückgestellt wird.
- Die *Narrator voice* eines gemeinsamen Servers sollte eine Stimmdatei des Servers sein (`predefined:Henry.wav`): ein Referenzclip ist eine Ressource eines Benutzers und kann von den anderen nicht gelesen werden. Die Stimmen Ihrer Darsteller funktionieren wie immer.

<a id="actors"></a>

### Darsteller

Ein **Darsteller** ist eine Stimme von Ihnen: ein Referenzclip (eine Ihrer Audio-[Ressourcen](../resources.md) oder ein neu hier hochgeladener) oder eine Stimmdatei des Servers, mit eigenen Stimmen für eine **Stimmung** (ein Flüstern, ein Schreien) oder eine **Sprache** (ein Clip, der Japanisch spricht). Die Sprache hat Vorrang vor der Stimmung; ohne eine Stimme dafür liest die Stimme des Darstellers. Ihre Darsteller bleiben in Ihrem Konto; jedes Buch kann einem Darsteller den Erzähler und seine Figuren zuweisen.

**Vorschau** neben einer Stimme (im Darsteller, in den Einstellungen und in der Besetzung) lässt einige Sätze mit ihr sprechen (bei einer Sprache ein kurzer Satz in der Sprache der Stimme) und spielt sie ab. Belegt die GPU gerade etwas anderes, lädt die GPU-Sperre zuerst den Sprachserver. Der Pfeil daneben bietet andere Proben an: **with sounds** (ein Seufzer, ein Lachen, ein Keuchen) für die Modelle, die es vertonen (Chatterbox Turbo, Higgs Audio v3), und für Higgs jede **Stimmung** der Markierung (sad, whispered, sarcastic...). Die Darsteller sind nach Namen aufgelistet.

Jede Probe belegt die GPU nur für sich. Die GPU bleibt nach einer Probe mit dem Sprachserver geladen, sodass die nächsten Proben sofort kommen, bis eine Anfrage des Sprachmodells (oder eines anderen Servers) die GPU braucht. Wenn Sie **Voices and clips** öffnen, lädt das bereits den Sprachserver, sodass die erste Probe nicht wartet.

![Ein Darsteller mit einer Stimme für eine Stimmung](../../images/plugin-audiobook-actor.png)
<a id="voices-and-clips"></a>

### Stimmen und Clips

**Stimmen und Clips** (neben den Sprechern) ist ein Fenster für Ihre Sprecher und die Clips, aus denen deren Stimmen geklont werden:

- **Sprecher**: jeweils mit seiner Stimme, der Anzahl weiterer Stimmen (Stimmungen, Sprachen) und der schlechtesten Qualität seiner Clips; hinzufügen, bearbeiten oder löschen.
- **Clips**: jeweils mit Länge, **Qualität** und Verwendungsort (Sprecher, mit Stimmung oder Sprache, sowie die Erzähler Ihrer Backends).
  Wählen Sie einen Clip aus, um zu lesen, was an ihm nicht stimmt, und spielen Sie ihn mit **Abspielen** ab, ersetzen Sie ihn mit **Ersetzen...**, **Mit ffmpeg bereinigen** (mit ffmpeg) oder löschen Sie ihn (nur, wenn nichts ihn verwendet).
- **Ersetzen...** setzt einen anderen Clip ein, einen Ihrer eigenen oder einen, den Sie hochladen, überall dort, wo der Clip verwendet wird. Die Teile werden mit der neuen Stimme neu erzeugt;
  der alte Clip bleibt bestehen, bis Sie ihn löschen.
- **Text des Clips**: die Worte, die im ausgewählten Clip gesprochen werden. Tippen Sie sie ein oder drücken Sie **Erkennen**, damit die [Spracherkennung](#speech-recognition) sie mitschreibt, korrigieren Sie sie und klicken Sie auf **Speichern**. Der Sprecherdialog zeigt dasselbe Feld für den Clip der Stimme des Sprechers. Eine bereinigte Kopie und ein Ersatz
  übernehmen den Text. Higgs erhält ihn nur, wenn das Backend ihn sendet (standardmäßig aus).
- **Mit ffmpeg bereinigen** erstellt eine bereinigte Kopie neben dem Clip (weniger gleichmäßiges Rauschen und Brummen), die erneut gemessen wird, damit Sie sie abspielen und vergleichen können; ersetzen Sie den Clip durch sie, wenn
  sie besser klingt. Musik, Effekte oder andere Stimmen werden nicht entfernt.

<a id="a-good-clip"></a>

#### Ein guter Clip

Ein Stimmmodell kopiert die **Aufnahme**, nicht nur die Stimme: der Raum, ein Rauschen, die Musik unter einer Filmszene kehren in jeder Zeile wieder, oft als leises „Whoosh“ unter den Worten, das kein Filter aus dem Ergebnis entfernt. Daher gilt:

- Verwenden Sie eine saubere Aufnahme eines einzelnen Sprechers mit echten Pausen (eine Probeaufnahme eines Synchronsprechers, eine Lesung, die zu Hause in einem ruhigen Raum gemacht wurde), 12 bis 30 Sekunden lang. Ein aus einem Film oder einer Serie geschnittener Clip ist der schlechteste Fall.
- Die Qualitätsmarken werden am Clip gemessen: **Sauber** (die Pausen liegen mindestens 45 dB unter der Sprache), **Mittel** (35 bis 45 dB oder länger als 30 s, was Chatterbox kürzt), **Schlecht** (Hintergrundgeräusch, unter 35 dB; verzerrt; kürzer als 8 s). Ein Clip, der nicht sauber ist, wird beim Hinzufügen gemeldet.
- Für das beste Ergebnis bereinigen Sie einen Clip von Hand: die Rauschunterdrückung von [Audacity](https://www.audacityteam.org/) mit einem Rauschprofil aus einer Pause des
  Clips oder einen Gesangsentferner wie [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) für einen Clip mit Musik. Die ffmpeg-Bereinigung
  senkt nur ein gleichmäßiges Rauschen: Bei einem Filmclip gewann sie im Test etwa 3 dB, und der Clip blieb *Schlecht*.

<a id="converting-new-parts-by-itself"></a>

### Neue Teile automatisch umwandeln

*Neue Teile automatisch in Audio umwandeln* ist die Voreinstellung für alle Ihre Bücher; siehe [Automatische Umwandlung](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Die Besetzung und die Aussprache eines Buches

Beides wird mit dem Buch gespeichert (es reist in dessen Sicherung mit) und im Dialog **In Audio umwandeln** bearbeitet.

**Besetzung** legt fest, wer liest: ein Sprecher für den **Erzähler**, einer für **andere Sprecher** (wer immer das Modell nennt und das die Besetzung nicht enthält; leer: der Erzähler liest) und einer für jede **Figur**. Eine Figur hat einen Namen und weitere Namen, unter denen der Text sie nennen darf (*Tom* für *Tomáš Novák*), durch Kommas getrennt; eine Rede wird der Figur zugeordnet, wenn ihr Name, einer der weiteren Namen oder ein Wort des Namens, das keine andere Figur teilt, übereinstimmt, ohne Beachtung von Groß- und Kleinschreibung und Akzenten. **Figuren des Buches hinzufügen** füllt die Liste mit den Figuren aus Erzählperspektive und den auftretenden Figuren der Teile.

![Die Besetzung eines Buches](../../images/plugin-audiobook-cast.png)

**Aussprache** ist die Schreibweise des Textes *für die Stimme*; das Buch selbst wird nie verändert. Das **Wörterbuch** ersetzt ganze
Wörter (Groß- und Kleinschreibung zählt) durch das, was für sie gesagt wird: eine Umschreibung für einen Namen oder ein erfundenes Wort oder eine Lesung in Kana für Japanisch. Für ein englischsprachiges Buch lesen die Regeln auch die übrigen Dinge laut vor: Zahlen, Ordnungszahlen, Jahre, Uhrzeiten, Geldbeträge und Abkürzungen als Wörter; eine Überschrift, die nur aus einer Zahl besteht, als *Chapter four. The Arch* (das Wort vor der Zahl bestimmen Sie); Markdown-Zeichen wie `*` werden weggelassen, und ein Szenenwechsel (`***`, `---`) wird nicht vorgelesen, sondern als längere Pause umgesetzt. Schalten Sie die Regeln für ein Buch in einer anderen Sprache aus; das Wörterbuch gilt weiterhin. **Text ausprobieren** zeigt, wie ein Textstück gelesen wird. Ein Stück, dessen gesprochener Text sich ändert, wird neu erzeugt, die anderen bleiben erhalten.

![Die Aussprache eines Buches](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Ein Buch in Audio umwandeln

*Tools*-Menü des Story-Editors (Zahnrad-Schaltfläche) → **Convert to audio**

![Der Dialog vor dem Start](../../images/plugin-audiobook-convert.png)

1. Wähle das **backend** und drücke **Load**. Die Teile mit Text des aktiven Zweigs werden aufgelistet (der erste ist 1), jeweils mit ihrem Status:
   *Not generated*, *Labelled* (die Labels des Modells sind gespeichert, noch kein Audio), *Generiert* (mit Wiedergabe-Schaltfläche und **Herunterladen**) oder
   *Generiert (der Text wurde geändert)* (das Audio wurde aus einem älteren Text erstellt und wird neu erstellt). *(final)* wird einem Teil angehängt, den das Studio als final markiert.
2. Lass **Label the parts with a language model** für die schlichte Fassung ausgeschaltet: Der Erzähler liest jeden Absatz. Schalte es ein, damit das Modell entscheidet, wer jede Rede spricht, wie (ruhig, flüsternd, schreiend ...) und wo ein Klangsignal passt. Die Besetzung liefert dann die Stimmen. Das Modell des Buches ist ausgewählt; wähle ein anderes, wenn du möchtest. Die Teile werden zuerst beschriftet und danach gesprochen. Ein Teil, der bereits Labels für seinen Text hat, wird auch mit ausgeschaltetem Kästchen mit diesen Labels gelesen (das Modell wird dann nicht gefragt). Ein Buch mit Labels erneut ohne das Kästchen laufen zu lassen, macht seine Dialoge also nicht zu einfacher Erzählung. **Remove labels** ist der Weg, um einfache Erzählung oder neue Labels anzufordern.
3. Markiere die Teile (das Kästchen in der Kopfzeile markiert alle) und **plan** fest, was mit ihnen geschehen soll. Die Spalte **Plan** zeigt es an, es geschieht aber noch nichts:
   **Generieren** (fehlendes erstellen; ein aktueller Teil wird nicht erneut erstellt), **Neu generieren** (das Audio löschen, die Labels entfernen, dann generieren),
   **Delete audio** (die Stücke und das zusammengefügte Audio des Teils; die Labels bleiben), **Remove labels** (das Modell wird beim nächsten Erstellen des Teils erneut gefragt),
   **Clear the plan** (Plan leeren). Schritte addieren sich bei einem Teil (**Delete audio** und **Remove labels** ohne **Generieren** räumen nur auf). Ein als final markierter Teil wird aus jedem Plan ausgelassen.
4. **Starten** führt den Plan aus. Wenn er Audio oder Labels entfernt, fragt er vorher nach. Was zu erstellen ist, kommt in deine Warteschlange, die weiterläuft, wenn du das Fenster schließt und die Seite verlässt. Ein Teil, der wartet oder in der Warteschlange läuft, wird aus einem Plan ausgelassen. Der Status, die bisher erstellten Stücke und die Meldung des Jobs stehen in der Zeile des Teils.
   Während das Modell einen Teil beschriftet, zeigt seine Zeile *Labelling pieces 17-32 of 120* (das Modell wird jeweils zu 16 Stücken befragt). **Klicke auf den Status** eines beschrifteten Teils, um für jede Anfrage die Überlegung des Modells und seine Antwort zu lesen, sobald sie eintreffen. Sie bleiben zusammen mit dem Job im Speicher, bis die fertigen Jobs gelöscht werden (*Clear finished*) oder die Anwendung neu startet.

![Die Warteschlange: bisher erstellte Stücke und ein Download, wenn ein Teil fertig ist](../../images/plugin-audiobook-queue.png)

So entsteht ein Teil, damit du weißt, was du erwarten kannst:

- Der Text wird **vom Programm** aufgeteilt, nie vom Modell, in Stücke mit Erzählung und Stücke mit direkter Rede (zwischen geraden oder gebogenen doppelten Anführungszeichen oder japanischen Winkelklammern; eine Überschrift ist Erzählung). Das Modell *beschriftet* nur die nummerierten Stücke. Ein Text mit Klangsignalen wird nur verwendet, wenn es das Stück mit hinzugefügten Signalen ist und sonst nichts geändert wurde. So kann das Modell dein Buch nie verändern.
- **Wer ein Stück spricht, entscheidet das Modell bei Rede; Erzählung gehört dem Erzähler.** Ein Brief, eine Notiz, eine Nachricht oder die Gedanken einer Figur haben keine Anführungszeichen, daher nennt das Programm sie Erzählung. Ein solches Stück wird nur dann von der Figur (der Stimme der Besetzung) gelesen, wenn es **kursiv beginnt** (`*...*` oder `_..._`) und das Modell die Figur aus dem Kontext nennt (eine Unterschrift, „die Handschrift war unverkennbar“). Erzählung in der dritten Person gehört immer dem Erzähler, auch wenn sie aus der Sicht einer Figur erzählt wird („Sie sah zu, wie Percy den Raum durchquerte“). Das Programm ignoriert eine Figur, die das Modell einem nicht kursiven Stück zuweist. Kursive Hervorhebungen, Laute oder Fremdwörter gelten ebenfalls als Erzählung. Die Worte bleiben immer deine.
- **Die Anweisung an das Modell ist eine Vorlage, die du ändern kannst.** *Settings → Einstellungen der Erweiterung → Audiobook Settings → Labelling prompt* ist dein Standard. Ein Buch (*Prompts*-Reiter → *Einstellungen der Erweiterung* → *Audiobook*) oder ein Ordner kann eine eigene haben. Leer bedeutet die des übergeordneten Ordners, dann deine, dann die eingebaute. Sie ist wie die anderen [Prompts](../books/prompts.md) geschrieben: Die Variablen sind `{{characters}}`, `{{deliveries}}`, `{{tags}}` und `{{wordsPerTag}}`, und das Hinweis-Popover hat **Standardvorlage einfügen**. Eine geänderte Anweisung führt dazu, dass die Labels der Teile beim nächsten Erstellen erneut angefragt werden.
- **Die Lautstärke der Teile wird angeglichen**, wenn das Programm ffmpeg installiert ist (dasselbe, das `.m4b` erstellt). Das zusammengefügte Audio jedes Teils wird auf eine Lautheit gebracht (EBU R128, linear, damit die Dynamik erhalten bleibt): **-18 LUFS**, sofern du es nicht unter *Settings → Einstellungen der Erweiterung → Audiobook Settings* änderst, wo du es auch ausschalten kannst. Die Stücke bleiben so, wie der Server sie erzeugt hat; nur das zusammengefügte Teil wird angeglichen, damit ein Hörbuch von Teil zu Teil nicht in der Lautstärke springt. **Teile, die vorher erstellt wurden**, werden erst angefasst, wenn du sie erneut konvertierst. Dann werden sie aus den gespeicherten Stücken neu zusammengefügt und angeglichen, und der Sprachserver wird dafür nicht angefragt (auch das Sprachmodell wird nicht entladen). Wenn du die Angleichung ausschaltest, bleiben die bereits angeglichenen Teile so, wie sie sind. Schlägt ffmpeg bei einem Teil fehl, behält er sein Audio wie es war, und das Ergebnis meldet dies.
- Alle Teile eines Klicks werden in **einem Batch pro Sprachmodell** erstellt: Die Anfragen vor dem Batch laufen einmal, dann werden alle Stücke gesprochen, dann laufen die Anfragen nach dem Batch einmal. So wechselt die Grafikkarte nicht für jeden Teil das Modell.
- Jedes Stück wird **nach seinem Inhalt gespeichert** (Text, Stimme, Modell, Sprache, Einstellungen, Seed). Ein Teil, der bereits aktuell ist, kostet nichts. Ein geänderter Absatz ist das einzige Stück, das erneut erstellt wird, und ein fehlgeschlagener Teil behält die bereits erstellten Stücke, wenn du **Retry** drückst.
- Die Stücke werden mit Pausen zusammengefügt, die von ihrer Stelle abhängen (nach einer Überschrift, zwischen Absätzen, vor *he said*), zu einer mp3-Datei pro Teil, die beim Teil gespeichert bleibt.
- Während eine Erstellung von dir läuft, wartet die Sprachausgabe, damit das Sprachmodell nicht darunter entladen wird.
- Ein Teil, den du im Studio als **final** markiert hast, bleibt unangetastet, und der Dialog sagt, wie viele es sind.
- **Das ganze Hörbuch** fügt jeden Teil mit aktuellem Audio zu einer mp3-Datei mit einer Kapitelmarke pro Teil zusammen und zeigt unter der Liste einen Download-Link (die ausgelassenen Teile werden genannt). Dieselbe Datei ist das Exportformat *one mp3 with chapter marks*.
- **Stop all** bricht die wartenden Teile ab und stoppt den laufenden sofort: Die Anfrage an das Sprachmodell (die Beschriftung) oder an den Sprachserver (das gerade gesprochene Stück) wird verworfen, nicht abgewartet. Ein Batch, der gestoppt wird, bevor er beginnt, schickt die Anfragen davor nicht ab, daher bleibt das Sprachmodell geladen. **Herunterladen** liefert die mp3-Datei eines fertigen Teils.
<a id="the-audiobook-studio"></a>

## Das Hörbuch-Studio

*Menü Werkzeuge → **Audiobook studio*** (die Stimme des ausgewählten Stücks mit einer Vorschau steht neben dem Lautsprecher) ist der Ort, an dem das fertige Hörbuch entsteht, Teil für Teil: Sie sehen das Skript, ändern es, hören ein Stück an und erstellen den Teil.

![Das Studio mit ausgewählter Äußerung](../../images/plugin-audiobook-studio.png)

- Solange der Teil annotiert oder erstellt wird, zeigt eine Leiste an, dass etwas läuft, und die Schaltflächen, die weitere Arbeit starten würden, warten.
- **Annotate** erstellt das Skript des Teils (mit dem Sprachmodell, wenn Sie das wünschen, sonst mit dem Erzähler) und zeigt die Stücke in einer Tabelle: Sprecher, Vortragsweise, Intensität, den gesprochenen Text und eine Markierung für ein Stück, das Sie geändert haben. Während es läuft, erscheint daneben eine Schaltfläche **Stop**, die die Anfrage an das Sprachmodell beendet.
- Wählen Sie ein Stück aus, um dessen **Sprecher**, den **gesprochenen Text**, die **Vortragsweise**, die **Intensität** (die Betonung der Stimme, bei Modellen, die dies unterstützen; bei Higgs sind Vortragsweise und Intensität die Markierungen wie `<|emotion:sadness|>` am Anfang des gesprochenen Textes, die ihnen folgen, wenn Sie sie ändern), die **Sprache**, den **Seed**, die **Pause nach** dem Stück zu bearbeiten. **Apply** übernimmt die Änderung und **sperrt** das Stück: Eine neue Annotation lässt ein gesperrtes Stück unverändert.
  **Unlock** gibt es wieder an die Annotation frei. *Die Anfrage an den Sprachserver* zeigt die genaue Anfrage.
- **Hear** erstellt das Audio eines Stücks (nur dieses Stück; der Sprachserver wird dafür gestartet). **Another take** erstellt es mit einem neuen Seed neu, und die **Takes** werden aufbewahrt, sodass Sie den gewünschten auswählen können (*Use this take and hear it*). Eine gute Stimme hängt oft vom Seed ab.
- **Make the part from this script** fügt die Stücke so zusammen, wie sie sind, ohne neue Annotation.
- Der **Status** des Teils ist *draft*, *reviewed* oder *final*. Ein finaler Teil wird nicht erneut annotiert oder konvertiert, und der Export kann auf die finalen Teile beschränkt werden.

<a id="exporting"></a>

## Exportieren

Das Hörbuch ist ein Format des Buchexports **Geschichte exportieren**: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** und, wenn ffmpeg installiert ist, **Audiobook (one m4b with chapters)**. Sie entstehen aus dem Audio der Teile, die auf dem aktuellen Stand sind: Ein Teil ohne Audio oder mit Audio, das älter ist als sein Text oder sein Skript im Studio, wird ausgelassen, und das Fenster meldet, welche es sind. Die mp3-Dateien enthalten ID3-Tags (den Titel des Buchs, das Kapitel, die Spur), und die einzelne Datei hat Kapitelmarken, die Player wie VLC und die meisten Hörbuch-Apps anzeigen; die zip-Datei enthält eine Playlist. Das Kontrollkästchen **Audiobook: only the parts marked final** beschränkt den Export auf die Teile, die Sie im Studio abgeschlossen haben.

Das **m4b** ist das Format, das Apple Books und die meisten Hörbuch-Apps auf dem Telefon verlangen: AAC-Audio (64 kbit/s, für Sprache ausreichend) in einem MP4-Container mit den Kapiteln, dem Titel, dem Autor und dem Genre. ffmpeg erstellt es, daher wird das Format nur auf einem Server angeboten, auf dem ffmpeg vorhanden ist. Audio, das als mp3 gespeichert ist, wird dafür ein zweites Mal kodiert; Audio, das als wav gespeichert ist, wird einmal kodiert.

<a id="how-the-audio-is-stored"></a>

### Wie das Audio gespeichert wird

Ein Administrator wählt dies für die gesamte Installation unter *Settings → Extension Settings → Audiobook Settings* (der letzte Abschnitt, nur für Administratoren sichtbar):

- **mp3** (Standard): klein, wird ohne Decoder zusammengefügt, kein ffmpeg nötig. Das m4b ist ein zweiter verlustbehafteter Schritt.
- **wav**: verlustfrei, etwa zehnmal so groß (eine Stunde Sprache sind etwa 170 MB), benötigt ffmpeg. Die Stücke werden vom Sprachserver als wav angefordert und als wav zusammengefügt; der Browser spielt sie unverändert ab;
  die mp3- und zip-Exporte werden von ffmpeg kodiert (96 kbit/s), und das m4b wird einmal kodiert.

Die Auswahl gilt für alles, was ab dann erstellt wird. Teile, die vorher erstellt wurden, behalten ihr Audio und werden so übernommen, wie sie sind; ein Teil beider Arten (ein gesperrtes Stück aus dem Studio von früher) wird als wav zusammengefügt, und ein Export von Teilen beider Arten funktioniert, benötigt aber ffmpeg.

Ein Export des ganzen Buchs wird aus den gespeicherten Dateien der Teile in temporäre Dateien des Servers erstellt und von dort heruntergeladen; seine Länge ist daher nicht durch den Arbeitsspeicher der Anwendung begrenzt, doch der temporäre Ordner braucht während der Erstellung Platz für etwa das Doppelte des Audios des Buchs (bei wav etwa 350 MB pro Stunde).

![Der Exportdialog mit den Hörbuchformaten](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Automatische Konvertierung

Ein Buch kann das Audio eines neuen Teils selbstständig erstellen, sobald der Teil final ist: nach der automatischen Annotation, der Neufassung und den Übersetzungen, falls das Buch sie hat, als letzter Schritt von [was dem Teil folgt](../books/story-editor.md#what-follows-a-generated-part)
(im Bereich der Läufe, bis das Audio erstellt ist; dessen **Stop** nimmt den Teil aus der Warteschlange). Sie ist standardmäßig **aus**. Wählen Sie sie im Reiter *Prompts* des Buchs unter den Erweiterungseinstellungen, für einen Ordner von Büchern oder für alle Ihre Bücher in den Hörbuch-Einstellungen; die nächstliegende Einstellung gilt:

- **Nein**,
- **Ja: der Erzähler liest den Teil vor**,
- **Ja: nach Modell des Buchs gekennzeichnet, die Besetzung gibt die Stimmen vor**; ein Buch ohne Modell erhält die einfache Erzählung, und Sie werden darüber informiert.

![Die Einstellung in einem Buch](../../images/plugin-audiobook-auto.png)

Der Teil wird in Ihre Warteschlange gestellt, mit der Besetzung und der Aussprache des Buchs. Eine Mitteilung sagt Ihnen, dass er in der Warteschlange steht, und eine weitere, dass sein Audio bereit ist oder fehlgeschlagen ist; sie erscheinen nur auf der Seite, die die Erstellung gestartet hat, der Auftrag ist aber in jedem Fall im Fenster *Convert to audio* zu sehen.

<a id="listening"></a>

## Anhören

Ein veröffentlichtes Buch (oder der Leser) mit Teilen, die Audio haben, erhält eine Player-Leiste: Abspielen, Pause, Stopp, die Geschwindigkeit, und der gerade gehörte Teil ist im Text markiert. Er beginnt bei der Auswahl oder beim ersten Teil im sichtbaren Bereich, spielt die Teile der Reihe nach ab und lädt den nächsten Teil, während der aktuelle läuft; die Mediensteuerung des Geräts kennt das Buch. Audio, das aus einer älteren Fassung eines Teils erstellt wurde, wird nicht angeboten. Das Audio wird dem Besitzer des Buchs bereitgestellt und, solange das Buch veröffentlicht ist, jedem, der es öffnen kann; ein Buch, das aus der Veröffentlichung zurückgezogen wird, wird sofort nicht mehr ausgeliefert (beim nächsten Teil, den der Player lädt).

![Der Player im Leser](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Ein Teil ohne Audio

Der Player hält bei einem Teil ohne Audio an, damit kein Teil der Geschichte übersprungen wird, und sagt dies. Ist die Erweiterung [Read Aloud](tts-reader.md) installiert, zeigt die Leiste außerdem eine Lautsprecher-Schaltfläche **Read this part (and the next ones without audio) with the voice of the browser**. Ohne Ihr Zutun wird nichts mit einer maschinellen Stimme vorgelesen. Nach dem Drücken liest sie diesen Teil und die folgenden Teile ohne Audio mit der Stimme Ihres Geräts, und wenn sie einen Teil mit Audio erreicht, übernimmt der Player wieder. Spätere Teile ohne Audio in derselben Wiedergabe werden ebenso gelesen, ohne erneut zu fragen; **Stop** schaltet die Stimme stumm, und ein erneuter Start des Players fragt wieder nach. Stimme, Geschwindigkeit und Tonhöhe sind die von Read Aloud.
<a id="commands"></a>

## Befehle

Dasselbe wie [Befehle](../commands/index.md) (die Kommandozeile des Story-Editors, das Makro `command` einer Vorlage, ein Agent). Teile werden so nummeriert, wie der Convert-Dialog sie nummeriert, oder über ihren Namen angesprochen.

| Befehl | Was er tut |
|---|---|
| `/audiobook-status [from [to]]` | Der verwendete Sprachserver, die Art, wie das Buch sich selbst konvertiert, wie viele Teile Audio haben (keine, bereit, älter als der Text, älter als das Skript), der Status jedes aufgeführten Teils im Studio und die Warteschlange. Nur bei einem langen Buch langsam. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Stellt die Teile in die Warteschlange (ohne Argumente das ganze Buch, ein einzelner Teil, wenn nur `from` angegeben ist). `labelling` `true` verwendet das Modell des Buches; `what` ist `audio` oder `script` (nur das Skript für das Studio, es wird nichts gesprochen). Mit `wait` `true` bleibt der Befehl so lange aktiv, bis die Aufträge abgeschlossen sind, zeigt deren Fortschritt und meldet, wie jeder endete. Ein Abbruch bricht die Aufträge ab, die noch nicht begonnen haben. |
| `/audiobook-stop` | Bricht die wartenden Teile ab; der laufende hält nach seinem Abschnitt an. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` oder `final` im Studio; der Teil braucht ein Skript. |
| `/audiobook-cast` | Der Erzähler, der Fallback und die Figuren der Besetzung mit ihren Sprechern. |

![Das Ergebnis eines Befehls](../../images/plugin-audiobook-command.png)

Das Audio wird für **Ihr** Buch erzeugt, nicht für die verborgene Arbeitskopie eines [Agenten](../agents.md): Ein Agent kann die Besetzung lesen und erhält bei den anderen Befehlen eine Ablehnung mit dem Hinweis, dass
Sie sie selbst ausführen müssen. Führen Sie sie über die Kommandozeile aus, oder geben Sie einem Agenten die Aufgabe, das Buch vorzubereiten (annotieren, prüfen), und konvertieren Sie es selbst.

<a id="notifications"></a>

## Benachrichtigungen

Wenn ein Stapel von Konvertierungen abgeschlossen ist, kann der Browser Sie benachrichtigen ([Benachrichtigungen](../account.md#notifications)), sodass Sie den Tab verlassen können, während der Sprachserver arbeitet. In der Liste der Benachrichtigungen auf der Seite *Einstellungen* gibt es zwei Zeilen: **Convert to audio** (die Stapel, die Sie im Dialog oder mit dem Befehl starten) und **Convert to audio (automatic)** (ein neuer Teil, der selbstständig in Audio umgewandelt wurde). Ein Stapel, den Sie abgebrochen haben, erzeugt keine Benachrichtigung; ein Stapel mit einem fehlgeschlagenen Teil meldet, dass er fehlgeschlagen ist.

![Die Liste der Benachrichtigungen](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Was noch nicht möglich ist

- Nur Mp3 und Wav (kein Opus); die Lautstärke der Teile ist das, was die Stimmen liefern.
- Keine Soundeffekte oder Atmosphäre.
- Nur zwei Sprach-Engines: Chatterbox und Higgs Audio v3 (in ComfyUI).
- Das Probehören einer Stimme außerhalb eines Segments.
- Die Besetzung ist nicht Teil der Sicherung eines Buches Ihrer *Sprecher*: Ein wiederhergestelltes Buch behält seine Besetzung, verweist aber auf Sprecher, die in Ihrem Konto vorhanden sein müssen.
