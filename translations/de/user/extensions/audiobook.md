---
label: Hörbuch
order: 27
---

# Hörbuch

Verwandelt ein Buch Teil für Teil in ein hörbares Buch, mit einem Text-to-Speech-Server: eine Stimme für den Erzähler und eine für jede Figur, gespeichert bei den Teilen des Buches und im Leser abgespielt. Sie können es laufen lassen (das Modell legt fest, wer spricht und wie, der Sprachserver liest vor), etwa für einen groben Entwurf, oder die Teile einzeln im **Audiobook studio** bearbeiten, genau sehen, was an den Sprachserver geschickt wird, und es von Hand ändern. Das Ergebnis lässt sich als mp3-Dateien mit Kapitelmarken exportieren.

Die Erweiterung erzeugt selbst keine Sprache: Sie spricht mit einem **Chatterbox-TTS-Server**, den Sie betreiben (siehe [Einrichten des Sprachservers](audiobook-server.md)). Der Text des Buches wird nie verändert und nur an die Server gesendet, die Sie angegeben haben. Das Audio wird in Ihrer eigenen Installation gespeichert, als Ressourcen, die mit den Teilen verknüpft sind.

<a id="what-you-need"></a>

## Was Sie brauchen

- Einen Sprachserver und, falls er sich die Grafikkarte mit Ihrem Sprachmodell teilt, die Anfragen, die Platz dafür schaffen und ihn danach wieder stoppen ([Einrichten des Sprachservers](audiobook-server.md)).
- Stimmen: einen kurzen **Referenz-Clip** (12 bis 30 Sekunden einer Person, ohne Musik) für jede Stimme, die Sie klonen wollen, oder eine der Stimmdateien, die der Server bereitstellt. Ein Erzähler braucht eine ruhige, gleichmäßige Lesestimme.
- Bei einem Buch mit mehreren Stimmen ein Sprachmodell, das den Text beschriften kann. Standardmäßig ist es das Modell des Buches.
- **ffmpeg** ist optional. Ohne es funktioniert alles mit mp3. Mit ffmpeg auf dem Server (es ist im Docker-Image enthalten; auf einem Desktop wird es im `PATH` oder in den üblichen Ordnern wie `/opt/homebrew/bin` gefunden; die Variable `MARGINALIA_FFMPEG` nennt den Pfad, wenn es anderswo liegt) bekommt der Export das Format **m4b**, und ein Administrator kann das Audio als wav speichern.

<a id="the-settings"></a>

## Die Einstellungen

*Settings → Extension Settings → Audiobook Settings* ist das Panel Ihres Kontos. Speichern Sie die Seite *Settings* nach einer Änderung.

![Das Panel mit einem Backend und den Sprechern](../../images/plugin-audiobook-settings.png)

### Backends

Ein Backend ist ein Sprachserver und das Modell, mit dem er spricht. Fügen Sie eines mit **+** hinzu. **Duplicate** kopiert das gewählte Backend (Adresse, Schlüssel, GPU-Sperre, Stimme, Standardwerte und die Anfragen vor und nach einem Batch) unter einem neuen Namen. Das ist der schnelle Weg zu einem Backend mit einem anderen Modell auf derselben Maschine, zum Beispiel Higgs neben Chatterbox. Dort ändern Sie dann das Modell, die Adresse und die Start- und Stopp-Anfragen.

| Setting | What it does |
|---|---|
| Name | Der Name, der im Dialog Convert angezeigt wird. |
| Address | Die Basisadresse des Sprachservers oder dessen, was an ihn weiterleitet, zum Beispiel `http://localhost:8004`. |
| Api key | Wird als Bearer-Token gesendet, falls der Server (oder Ihr Proxy) einen will. Er wird verschlüsselt gespeichert und nicht wieder angezeigt. Geben Sie einen neuen ein, um ihn zu ersetzen, oder nutzen Sie **Remove the key**. |
| Speech model | **Chatterbox Turbo** (Englisch, mit Klangmarken wie `[sigh]`, das schnellste und der Standard), **Chatterbox** (Englisch, mit einer *exaggeration* der Betonung), **Chatterbox Multilingual** (23 Sprachen) oder **Higgs Audio v3 in ComfyUI** (viele Sprachen, Stimmen, die die Stimmung einer Zeile treffen, und Geräusche wie Weinen, Keuchen oder Stöhnen; siehe [unten](#higgs-audio-v3-emotions-and-sounds)). Die [GPU-Sperre](../administration/gpus.md) des Backends (oder die Anfragen vor einem Batch) muss dieses Modell geladen lassen. Ein Buch, das ein anderes Modell braucht, nutzt ein anderes Backend. |
| GPU lock | Die [GPU-Sperre](../administration/gpus.md) der GPU, auf der der Server läuft, wenn er sich diese mit dem Sprachmodell oder anderen Servern teilt (auch der des [Image Generator](image-generator.md)): Jeder Batch und jede Probe wartet auf seinen Zug auf der GPU, und die Sperre lädt zuerst den Sprachserver. Wird nur angezeigt, wenn der Administrator GPU-Sperren eingerichtet hat. |
| The server is on this machine or on my network | Erlaubt Adressen des lokalen Netzwerks. Lassen Sie es aus bei einem Server in der Cloud: Dann kann er nicht dazu gebracht werden, Ihren eigenen Rechner anzurufen. |
| Timeout of a request | Wie lange auf ein Stück Sprache gewartet wird. |
| Restart the server after (pieces) | Für einen Sprachserver, der Speicher verliert und bei einem langen Batch vom System beendet wird: Nach so vielen Teilen werden die Anfragen nach dem Batch und dann die davor mitten im Batch erneut gesendet (stoppen, starten, laden), und die Arbeit geht mit einem frischen Server weiter. 0 (der Standard) tut das nie. Ein Server, der abstürzt, wird unabhängig davon neu gestartet, siehe [Fehlerbehebung](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | Der Schlüssel, den die Anfragen vor und nach einem Batch als `Authorization: Bearer` mitschicken. Er wird verschlüsselt gespeichert und nicht wieder angezeigt (**Remove the key** löscht ihn), sodass er nicht in die Header jeder Anfrage geschrieben werden muss, wo er lesbar wäre. Leer: Es wird der *Api key* oben verwendet, was Sie wollen, wenn ein Proxy wie LiteLLM alles bedient. Eine Anfrage mit einem eigenen `Authorization`-Header behält ihn. |
| Narrator voice | Die Stimme des Erzählers eines Buches ohne Besetzung. Eine Stimmdatei des Servers schreibt man `predefined:Henry.wav`. |
| Precision | Nur Higgs: die Genauigkeit, in der das Modell rechnet. **fp32** (der Standard) ist die sichere Wahl: Auf einer AMD-Karte haben **bf16** Teile in Kauderwelsch, Klangeffekte und falsche Stimmen verwandelt. bf16 braucht etwa die Hälfte des Grafikspeichers (etwa 8 GB statt 16) und ist schneller, wo es funktioniert. |
| Send the text of the clip to Higgs | Nur Higgs, experimentell: Der [Text des Clips](#voices-and-clips) wird mitgeschickt. Lassen Sie es aus, denn in Tests hat es die Stimme verändert. |
| Check the length of the pieces | Standardmäßig an. Ein Teil, das weit länger ist als sein Text (4 s plus 0.6 s pro Wort: Das Modell hat weitergeredet oder statt der Wörter einen Laut erzeugt), wird mit dem nächsten Seed neu erzeugt, bis zu **Retries** Mal. Die erste Fassung mit richtiger Länge wird behalten, sonst die kürzeste. |
| Check the words with speech recognition | Standardmäßig aus; braucht die [Spracherkennung](#speech-recognition). Nach jedem Batch wird jedes Teil transkribiert und mit seinem Text verglichen. Ein Teil, das um mehr als **Max. word errors (%)** seiner Wörter abweicht, wird mit dem nächsten Seed neu erzeugt und erneut geprüft, bis zu **Retries** Runden. Die Fassung mit den wenigsten falschen Wörtern wird behalten. Die Konvertierung sagt, wie viele Teile neu erzeugt wurden und wie viele noch abweichen. |
| Defaults of a segment | Temperatur, CFG-Gewicht, Geschwindigkeit und Chunk-Größe, mit denen ein Teil startet (das Studio kann sie für ein einzelnes Teil ändern). Higgs verwendet nur die Temperatur. |
| Before a batch, After a batch | Anfragen, die um jeden Batch von Teilen (und jede Probe) herum gesendet werden, innerhalb der GPU-Sperre. Die Grafikkarte freizugeben und den Sprachserver zu starten ist Aufgabe der GPU-Sperre; siehe [Eine GPU für das Modell und die Sprache](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Leer bedeutet: Es passiert nichts. |

**Test connection** tut dasselbe wie ein Batch: Es nimmt die GPU-Sperre (damit ein gestoppter Sprachserver gestartet wird), sendet die Anfragen vor einem Batch, fragt den Server, ob er erreichbar ist und ein Modell geladen hat, und sendet dann die Anfragen nach dem Batch. Ohne Sperre oder Anfragen fragt es nur ab.

Ein Buch, das Teile in einer anderen Schrift hat (Japanisch in einem englischen Buch), schickt sie an das mehrsprachige Modell, falls Sie dafür ein Backend haben, mit ihrer Sprache. Ohne ein solches Backend bleiben sie beim Modell des Buches. Ein Higgs-Buch behält sie: Higgs liest sie selbst, in derselben Stimme.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: Emotionen und Geräusche

Mit dem Backend **Higgs Audio v3 in ComfyUI** werden die Markierungen des Sprachmodells hörbar. Die Adresse des Backends ist Ihr ComfyUI
(mit den Knoten des TTS Audio Suite, siehe [Einrichten des Sprachservers](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- Die **Vortragsweise** einer Rede wird gesprochen: traurig, wütend, fröhlich, amüsiert, angespannt, ängstlich, überrascht, zärtlich, sarkastisch, flehend, verzweifelt,
  angewidert, erregt, ein Flüstern oder ein Schrei. Eine starke (Intensität 0,8 und mehr) wirkt ausdrucksstärker, eine schwache (unter 0,2) wird neutral gelesen.
  Der Erzähler liest neutral.
- Das Sprachmodell darf mehr **Geräusche** in die Rede setzen als bei Chatterbox Turbo: Lachen, Kichern, Glucksen, Seufzen, Keuchen, Husten, Räuspern,
  Schniefen, Niesen, Stöhnen, Weinen, Schluchzen, Schreien, Hecheln, Stöhnen vor Lust, Summen und Ruhe.
- Das Programm liest auch die **Erzählung um jede Rede** (auf Englisch) und gibt der Rede das Geräusch, das sie nennt, sofern das Modell keines gesetzt hat:
  *"You look funny," she giggled.* bekommt ein Kichern, *She burst out laughing. "Careful!"* ein Lachen, *a soft moan escaped her lips. "Yes..."* ein Stöhnen,
  *panting* oder *out of breath* Hecheln, *cried quietly* oder *burst into tears* Weinen. Es lässt weg, was nur wie ein Geräusch aussieht: *"Stop!" she cried.*
  (ein Ausruf), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, sowie ein Geräusch, das die Erzählung
  einer anderen benannten Figur zuschreibt. *"You look funny," he mocked her.* bekommt eine sarkastische oder amüsierte Vortragsweise und kein Geräusch. Die Erzählung selbst wird so gelesen, wie sie geschrieben ist.
- **Der Erzähler wird immer neutral gelesen**: ohne Geräusche und ohne Stimmung, auch bei einer Rede, die das Modell dem Erzähler überlassen hat.
- Higgs braucht für jede Stimmung keinen eigenen Clip: Ein Clip einer Stimme genügt. Es kopiert die Aufnahme des Clips, deshalb ist ein sauberer Clip wichtiger als bei Chatterbox
  ([Ein guter Clip](#a-good-clip)).
- Jedes Stück wird von ComfyUI erzeugt, und dessen Speicher wird danach geleert (einige Sekunden pro Stück). Das verhindert, dass manche Grafikkarten abstürzen.
- Higgs spricht oft nach dem letzten Wort eines Stücks weiter (ein Atemzug, der Beginn eines anderen Geräuschs), und der Clip endet mitten darin. Das hört man als
  kurzes „Wusch“ vor der Pause. Mit **ffmpeg** schneidet die Erweiterung diesen Ausklang ab und blendet jedes Stück aus; ohne ffmpeg bleiben die Stücke so, wie Higgs sie erzeugt hat.
- Das Higgs-Audio-v3-Modell steht unter einer **Lizenz für Forschung und nicht-kommerzielle Nutzung**: Ein Hörbuch, das Sie verkaufen, braucht eine Lizenz von Boson AI.

<a id="speech-recognition"></a>

### Spracherkennung

**Spracherkennung** schreibt auf, was eine Rede sagt. Sie prüft die Stücke eines Stapels (wenn ein Backend **Check the words with speech
recognition** aktiviert hat) und erkennt den [Text eines Stimmclips](#voices-and-clips). Wählen Sie, wo sie läuft:

| Setting | What it does |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)**: das Granite-Sprachmodell des TTS Audio Suite in Ihrem ComfyUI (die Adresse ist die von ComfyUI). **OpenAI-compatible (whisper)**: jeder Server mit `/v1/audio/transcriptions`, etwa ein Whisper-Server (faster-whisper, speaches) oder die OpenAI-API. |
| Address, Api key | Die Adresse von ComfyUI oder die Basis der API (mit oder ohne `/v1`); der Schlüssel wird verschlüsselt gespeichert. Leere Adresse: keine Spracherkennung. |
| GPU lock | Die [GPU-Sperre](../administration/gpus.md), die die Erkennung belegt. Bei einer einzigen Karte mit fp32-Higgs erhält sie eine eigene Sperre, deren Schritte ComfyUI neu starten: ComfyUI hält das Sprachmodell im Speicher des Servers, und das Laden der Erkennung daneben hat den Server aus dem Speicher laufen lassen. |
| Model of the recognition | Leer: `granite-speech-4.1-2b` für ComfyUI, `whisper-1` für den OpenAI-kompatiblen Server. |
| Timeout, The server is on this machine or on my network | Wie bei einem Backend. |

**Test connection** prüft, ob ComfyUI die Knoten hat oder ob der Server antwortet. Die Stücke eines Stapels werden nach dem Stapel geprüft, wenn der Sprachserver
wieder frei ist, sodass Erkennung und Sprachmodell nie gleichzeitig die Karte brauchen.

<a id="cleanup-of-clips"></a>

### Bereinigung von Clips

**Cleanup of clips** bereinigt einen Referenzclip auf einem ComfyUI mit dem [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (demselben
wie für Higgs-Arbeiten). Das Ergebnis ist eine neue **Version** des Clips in [Stimmen und Clips](#voices-and-clips); der Clip selbst bleibt erhalten.

| Setting | What it does |
|---|---|
| Address, Api key | Die Adresse von ComfyUI; der Schlüssel wird verschlüsselt gespeichert. Leere Adresse: keine Bereinigung. |
| GPU lock | Die [GPU-Sperre](../administration/gpus.md), die die Bereinigung belegt. Geben Sie ihr eine eigene Sperre, nicht die der Sprachausgabe oder der Spracherkennung, deren Schritte ComfyUI neu starten, damit die Bereinigungsmodelle nie neben dem Sprachmodell geladen werden. |
| Timeout, The server is on this machine or on my network | Wie bei einem Backend (der Timeout gilt für eine Bereinigung und beträgt standardmäßig 10 Minuten, weil ComfyUI ein Modell beim ersten Einsatz herunterlädt). |
| Cleanup | Die Bereinigungen (Voreinstellungen), die Sie in Stimmen und Clips nach Namen wählen. Jede ist eine Liste von Schritten, die nacheinander ausgeführt werden. **Default cleanups** stellt die mitgelieferten wieder her. |

Ein Schritt ist einer der folgenden:

- **Separation model**: ein Modell des Suite, das das Audio in zwei Teile trennt, den *extrahierten* und den *verbleibenden* Teil, und einen davon behält. Welcher Teil die Stimme enthält, hängt vom Modell ab: Ein Vocals-Modell (`MelBandRoformer`) extrahiert die Stimme, ein Entrauschungs- oder Enthallungsmodell extrahiert das Rauschen oder den Raum, daher behalten Sie den verbleibenden Teil. **Aggressiveness** (0 bis 20) bestimmt, wie viel entfernt wird.
- **Voice fixer**: stellt eine beschädigte Stimme wieder her (ein Telefon, eine alte Aufnahme). Es verändert den Klang der Stimme stärker als eine Trennung.
- **ffmpeg (here)**: der Filter von **Clean with ffmpeg**, den die Anwendung zwischen den Schritten auf ComfyUI ausführt.
- **Normalize loudness (here)**: hebt (oder senkt) den Pegel des Clips auf die Lautheit, die Sie angeben (standardmäßig −18 LUFS, wie beim Hörbuch), mit
  ffmpeg in der Anwendung. Nur der Pegel ändert sich, die Spitzen bleiben unter der Übersteuerung. Setzen Sie ihn ans Ende, nach die Schritte, die etwas entfernen (sie machen die Stimme leiser). Eine Bereinigung nur mit diesem Schritt (und dem ffmpeg-Filter) verwendet weder ComfyUI noch dessen GPU-Sperre.

Die mitgelieferten Bereinigungen sind: *Voice from music* (die Stimme aus einem Film oder Lied), *Remove noise*, *Remove echo* (der Raum und das Echo),
*Raise a quiet voice* (nur der Lautheitsschritt), *Restore the voice* und *Film or game clip* (die Stimme aus der Musik, dann der Raum, dann das Rauschen: Hätte man das Rauschen zuerst entfernt, wäre der Raum eines Filmclips hervorgetreten). *Remove echo* braucht in manchen ComfyUI-Einrichtungen eine Korrektur, siehe [den Sprachserver](audiobook-server.md#higgs-audio-v3-in-comfyui). **Test connection** prüft, ob ComfyUI die Knoten hat, und füllt die Liste der Modelle mit denen Ihres
Servers. Die Modelle lädt ComfyUI beim ersten Einsatz einer Bereinigung herunter.

<a id="shared-speech-servers"></a>

### Gemeinsame Sprachserver

Auf einer Installation mit mehreren Benutzern kann der Administrator Sprachserver für alle bereitstellen, sodass eine GPU-Maschine nur einmal eingerichtet wird und niemand eine Adresse oder einen Schlüssel braucht. Der Administrator tut das im selben Bereich (den letzten Abschnitten, nur für Administratoren sichtbar): **Shared speech servers** öffnet ein Fenster mit denselben Feldern wie ein eigenes Backend sowie der Angabe, wer ihn nutzen darf: **alle Benutzer** oder die in der Liste ausgewählten Benutzer. Ein Schalter dort, **Users may add speech servers of their own**, entscheidet, ob die unten genannten Backends weiterhin erlaubt sind.

Was Sie davon haben:

- **Backend in use** oben im Bereich listet die gemeinsamen Server auf, die Sie nutzen dürfen (mit *(shared)* markiert), und Ihre eigenen. Das Fenster *Convert to audio* und das Studio verwenden denjenigen, den Sie wählen; die Befehle verwenden ihn ebenfalls.
- Sie sehen nur den Namen eines gemeinsamen Servers, nie dessen Adresse, Schlüssel oder Anfragen. Die Stapel aller Benutzer auf einer Serveradresse warten aufeinander, sodass zwei Benutzer dieselbe GPU nicht gleichzeitig starten können.
- Wenn der Administrator Ihnen einen Server entzieht, wird ein Auftrag, der ihn noch nennt, abgelehnt; wählen Sie dann einen anderen.
- Wenn der Administrator Ihre eigenen Server abschaltet, zeigt der Bereich das an. Die von Ihnen hinzugefügten bleiben erhalten und werden wieder verwendet, wenn der Schalter wieder eingeschaltet wird.
- Die *Narrator voice* eines gemeinsamen Servers sollte eine Stimmdatei des Servers sein (`predefined:Henry.wav`): Ein Referenzclip gehört einem einzelnen Benutzer und kann von den anderen nicht gelesen werden. Stimmen Ihrer Figuren funktionieren wie gewohnt.
<a id="actors"></a>

### Akteure

Ein **Akteur** ist eine Stimme von dir: ein Referenzclip (eine deiner Audio-[Ressourcen](../resources.md) oder ein neuer, hier hochgeladener) oder eine Stimmdatei des Servers, mit eigenen Stimmen für eine **Stimmung** (ein Flüstern, ein Schreien) oder eine **Sprache** (ein Clip, der Japanisch spricht). Die Sprache hat Vorrang vor der Stimmung, und ohne eine Stimme für beides liest die Stimme des Akteurs. Deine Akteure werden in deinem Konto gespeichert; jedes Buch kann seinem Erzähler und seinen Figuren einen Akteur geben.

Ein Akteur kann eine **Kurznotiz** haben, eine kurze Notiz von dir: die Figur, die er spielt, ein Buch, *deep and old*, jede Markierung, die dir hilft, die richtige Stimme zu finden. Die Auswahlfelder eines Akteurs (die Einstellungen, die Besetzung) zeigen sie unter dem Namen, und *Voices and clips* zeigt sie unter dem Namen in der Liste der Akteure. In der Besetzung tippst du in ein Akteurfeld, um einen Akteur über einen Teil seines Namens oder seiner Kurznotiz zu finden.

**Preview** neben einer Stimme (im Akteur, in den Einstellungen und in der Besetzung) spricht ein paar Sätze mit ihr (bei einer Sprache ein kurzer Satz in der Sprache der Stimme) und spielt sie ab. Ist die GPU mit etwas anderem belegt, lädt die GPU-Sperre zuerst den Sprachserver. Der Pfeil daneben bietet andere Beispiele an: **with sounds** (ein Seufzer, ein Lachen, ein Keuchen) für die Modelle, die diese Stimme sprechen (Chatterbox Turbo, Higgs Audio v3), und bei Higgs jede **Stimmung** der Kennzeichnung (sad, whispered, sarcastic...). Ist die Stimme ein Clip, spielt der Knopf mit der Audiodatei den **Referenzclip** selbst ab, so wie er gespeichert ist, sofort (kein Sprachserver, keine GPU). Die Akteure sind nach Namen aufgelistet.

Jede Probe nimmt die GPU nur für sich. Die GPU bleibt nach einer Probe mit dem Sprachserver geladen, sodass die nächsten Proben sofort kommen, bis eine Anfrage des Sprachmodells (oder eines anderen Servers) die GPU braucht.

![Ein Akteur mit einer Stimme für eine Stimmung](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Stimmen und Clips

**Voices and clips** (neben den Akteuren) ist ein Fenster für deine Akteure und die Clips, aus denen ihre Stimmen geklont werden. Es hat drei Tabs, zunächst nebeneinander: **Actors**, **Clips** und **Clip**. Ziehe einen Tab, um ihn neben, unter oder zusammen mit einem anderen abzulegen; das Fenster öffnet sich wieder so, wie du es verlassen hast.

- **Actors**: Jeder Akteur mit seiner Stimme, der Anzahl seiner weiteren Stimmen (Stimmungen, Sprachen) und der schlechtesten Qualität seiner Clips. Hinzufügen, bearbeiten oder löschen. Das Fadenkreuz eines Akteurs, dessen Stimme ein Clip ist, wählt diesen Clip in **Clips** aus und öffnet die Ordner darüber.
  - **Clips**: deine Clips in **Ordnern** (zum Beispiel einer pro Figur), darunter die **Versionen**, die aus einem Clip gemacht wurden, jeweils mit ihrer Länge, ihrer **Qualität** und ihrer Verwendung (Akteure mit Stimmung oder Sprache und die Erzähler deiner Backends). **Add a clip** lädt einen Clip hoch. Die Ordner beginnen geschlossen; was du öffnest, bleibt offen, solange das Fenster offen ist, auch nach einer Änderung an einem Clip.
    - Clips zum gleichzeitigen Bearbeiten ankreuzen (Ordner haben kein Kästchen). **Merge** macht aus den angekreuzten Clips einen neuen, einen nach dem anderen in der Reihenfolge der Liste, ohne Pause dazwischen: eine längere Referenz aus kurzen Aufnahmen einer Stimme. Er wird nach ihnen benannt, kommt in ihren Ordner, wenn sie einen gemeinsamen haben, und übernimmt ihre Texte, wenn jeder einen hat. Die angekreuzten Clips bleiben. Wav-Clips desselben Formats werden unverändert aneinandergefügt; mp3-Clips oder wavs verschiedener Formate brauchen ffmpeg. **Delete checked** löscht die angekreuzten Clips nach einer Rückfrage. Ein Clip, der noch verwendet wird, bleibt, und die Meldung nennt ihn.
- **Clip**: der ausgewählte Clip.
  - **Name** und **Folder**: den Clip umbenennen (die Datei behält ihren Namen) und ihn in einen Ordner legen, einen neuen, indem du dessen Namen eintippst. Eine Version bleibt im Ordner des Clips, aus dem sie gemacht wurde, und zeigt an, woraus und wie sie gemacht wurde.
  - Seine **Wellenform**: Klicken setzt den Abspielkopf dorthin, **Play** startet die Wiedergabe. Ziehen über die Wellenform wählt einen Bereich aus (die Felder **From** und **To** folgen, auf die Hundertstelsekunde genau, und die Auswahl folgt ihnen). Die Lupe zoomt in die Auswahl für eine genaue Kante, die andere zeigt den ganzen Clip wieder. **Play the selection** spielt den Bereich ab, **Play around the cut** spielt je eineinhalb Sekunden auf beiden Seiten davon ohne ihn ab: so klingt der Clip, wenn der Bereich herausgeschnitten ist.
  - **Download** des Clips (benannt nach dem Clip und seiner Version), Anzeige, was mit ihm nicht stimmt, **Replace...** oder **Delete** (nur wenn nichts ihn verwendet; seine Versionen wandern dann zu dem Clip, aus dem sie gemacht wurden).
  - **Clean with ffmpeg** erstellt eine Version, in der ein gleichmäßiges Zischen oder Brummen leiser ist. Musik, Effekte oder andere Stimmen entfernt es nicht.
  - **Clean with AI** erstellt eine Version mit der gewählten [Bereinigung](#cleanup-of-clips) auf ComfyUI. Sie läuft im Hintergrund: Du kannst weiterarbeiten, und eine Meldung sagt dir, wann die Version fertig ist.
  - **Trim** erstellt eine Version nur mit dem ausgewählten Bereich des Clips (**Keep this part**) oder ohne ihn (**Cut this part out**), für einen Laut, den keine Bereinigung entfernt: ein Tier, ein Husten, eine Tür. Die Übergänge werden ausgeblendet, damit sie nicht knacken. Ein wav-Clip wird ohne ffmpeg gekürzt, eine mp3 braucht ffmpeg.
  - **Use this version** setzt eine Version überall dort ein, wo der Clip, aus dem sie gemacht wurde, verwendet wird. Die Teile werden mit der neuen Stimme neu erzeugt, und der alte Clip bleibt, bis du ihn löschst.
  - **Try the voice**: Text eintippen und **Speak**. Das Backend in Verwendung spricht ihn mit diesem Clip genau so, wie eingetippt (ohne Kennzeichnung, ohne hinzugefügte Laute), mit dem **Seed**, den du angibst (leer: 42, derselbe wie bei den Konvertierungen), und spielt ihn ab. Das ⓘ listet die Tags auf, die das Modell des Backends in seiner eigenen Syntax liest (`[cough]` für Chatterbox Turbo, `<|sfx:cough|>` oder `<|emotion:sadness|>` für Higgs); ein Klick setzt einen in den Text. Es nimmt die GPU-Sperre des Backends und erscheint in den Aufträgen wie eine Konvertierung. Bereinigung und Erkennung eines Clips sind ebenfalls Aufträge.
  - **Text of the clip**: die im Clip gesprochenen Wörter. Tippe sie ein, oder drücke **Recognize**, damit die [Spracherkennung](#speech-recognition) sie aufschreibt; korrigiere sie und drücke **Save**. Der Akteur-Dialog zeigt dasselbe Feld für den Clip der Akteursstimme. Eine Version und ein Ersatz übernehmen den Text. Higgs erhält ihn nur, wenn das Backend ihn sendet (standardmäßig aus).
- **Replace...** setzt einen anderen Clip, einen von dir oder einen, den du hier hochlädst, überall dort ein, wo der Clip verwendet wird.

Auswahlfelder einer Stimme (der Akteur, der Erzähler eines Backends) zeigen einen Clip als *folder / clip › version*.

<a id="a-good-clip"></a>

#### Ein guter Clip

Ein Stimmmodell kopiert die **Aufnahme**, nicht nur die Stimme: Der Raum, ein Zischen, die Musik unter einer Filmszene kehren in jeder Zeile zurück, oft als leises „Whoosh" unter den Wörtern, das kein Filter des Ergebnisses entfernt. Deshalb:

- Verwende eine saubere Aufnahme eines Sprechers mit echten Pausen (eine Sprechprobe, eine zu Hause in einem ruhigen Raum gemachte Lesung), 12 bis 30 Sekunden. Ein aus einem Film oder einer Serie geschnittener Clip ist der schlimmste Fall.
- Die Qualitätsmarken werden am Clip gemessen: **Clean** (die Pausen sind mindestens 45 dB unter der Sprache), **Fair** (35 bis 45 dB, oder länger als 30 s, was Chatterbox kürzt), **Poor** (Hintergrundgeräusch, unter 35 dB; verzerrt; kürzer als 8 s). Ein Clip, der nicht *Clean* ist, wird beim Hinzufügen gemeldet.
- Ein Trennmodell behält, was wie eine Stimme klingt: Der Ruf einer Eule oder der Gesang eines Vogels bleibt (im Test mit mehreren Modellen). Schneide einen solchen Laut mit **Trim** heraus.
- Bereinige einen Clip mit KI ([Bereinigung von Clips](#cleanup-of-clips)): Im Test hat die Bereinigung *Film or game clip* einen Clip mit Zischen, Brummen und Echo von *Poor* auf *Clean* gebracht (die Pausen gingen von 38 auf 89 dB unter der Sprache). Höre die Version an, bevor du sie verwendest: Ein Trennmodell kann auch einen Atemzug oder das Ende eines leisen Wortes entfernen. Die ffmpeg-Bereinigung senkt nur ein gleichmäßiges Rauschen: Bei einem Filmclip gewann sie etwa 3 dB, und der Clip blieb *Poor*.
- Ohne ComfyUI bereinige einen Clip von Hand: die Rauschunterdrückung von [Audacity](https://www.audacityteam.org/) mit einem Rauschprofil aus einer Pause des Clips, oder ein Vocal-Remover wie [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) für einen Clip mit Musik.

<a id="converting-new-parts-by-itself"></a>

### Neue Teile selbst konvertieren

*Convert new parts to audio by itself* ist die Voreinstellung für alle deine Bücher; siehe [Automatische Konvertierung](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Die Besetzung und die Aussprache eines Buches

Beide werden zusammen mit dem Buch gespeichert (sie sind in seinem Backup enthalten) und im Dialog **Convert to audio** bearbeitet.

**Cast** legt fest, wer liest: einen Sprecher für den **narrator**, einen für **other speakers** (wer auch immer das Modell nennt und die Besetzung nicht kennt; leer: der Erzähler liest) und einen für jede **Figur**. Eine Figur hat einen Namen und weitere Namen, die der Text für sie verwenden darf (*Tom* für *Tomáš Novák*), durch Kommas getrennt. Eine Rede wird der Figur zugeordnet, wenn ihr Name, einer der weiteren Namen oder ein Wort des Namens, das keine andere Figur teilt, übereinstimmt. Dabei werden Groß- und Kleinschreibung sowie Akzente ignoriert. **Add the characters of the book** füllt die Liste mit den Figuren aus der Erzählperspektive und den vorkommenden Figuren der Teile.

![Die Besetzung eines Buches](../../images/plugin-audiobook-cast.png)

**Unbenannte Sprecher** (der Wächter, der Wirt, das Mädchen am Fenster) können jeweils eine eigene Stimme aus einer oder zwei Aufnahmen erhalten. Lege für die Besetzung Basis-Sprecher fest: **Other men**, **Other women** und, falls gewünscht, **Children** (leer: die Frauenstimme, angehoben). Die Beschriftung gibt dann auch Geschlecht und Alter jedes Sprechers an, den die Besetzung nicht benennt. Jeder von ihnen spricht mit einer *Variante* des Basisclips seiner Art: etwas höher oder tiefer, mit anderen Formanten, schneller oder langsamer, dunkler oder heller. Ein jugendlicher Junge bekommt die Männerstimme, ein jugendliches Mädchen und ein Kind die Frauenstimme, ein alter Sprecher eine tiefere und langsamere. Die Variante stammt aus dem Namen des Sprechers und dem Buch. Deshalb klingt derselbe Sprecher in jedem Kapitel und bei jedem Durchlauf gleich. Lässt du alle drei leer, wird jeder unbenannte Sprecher wie bisher von **Other speakers** gelesen.

Die bisher erkannten Sprecher sind unter den Basen aufgelistet, mit dem Geschlecht und Alter, die sie zuerst erhalten haben (ein späterer Teil ändert sie nicht). Du kannst Geschlecht oder Alter korrigieren, den Sprecher anhören, ihm **another variation** geben (der Würfel) oder ihn aus der Liste entfernen. Ein geänderter Sprecher wird beim nächsten Konvertieren der Teile neu erstellt.

Eine Variante benötigt **ffmpeg** mit dem Filter *rubberband* auf dem Server (die Debian- und Ubuntu-Pakete, die das Docker-Image installiert, sind normalerweise damit gebaut; `ffmpeg -filters | grep rubberband` zeigt, ob deine Installation ihn enthält). Jede Variante wird einmal erstellt, wenn sie erstmals gebraucht wird, und als versteckte Kopie ihres Clips gespeichert, sodass sie mit deinen Backups mitwandert. Ohne ffmpeg meldet die Besetzung das: unbenannte Sprecher teilen sich die Basisstimme ihrer Art, und die bereits erstellten Varianten werden weiterhin verwendet. Eine Variante verändert nur einen Clip von dir; ein Akteur, dessen Stimme eine Stimme des Sprachservers ist, wird unverändert verwendet.

**Pronunciation** legt fest, wie der Text *für die Stimme* geschrieben wird; das Buch selbst wird nie verändert. Das **dictionary** ersetzt ganze Wörter (Groß- und Kleinschreibung zählt) durch das, was für sie gesprochen wird: eine Umschreibung für einen Namen oder ein erfundenes Wort oder eine Lesung in Kana für Japanisch. Für ein englisches Buch lesen die Regeln auch die übrigen Dinge laut vor: Zahlen, Ordnungszahlen, Jahre, Uhrzeiten, Geldbeträge und Abkürzungen als Wörter. Eine Überschrift, die nur eine Zahl ist, wird als *Chapter four. The Arch* gelesen (das Wort vor der Zahl bestimmst du selbst). Markdown-Zeichen wie `*` werden weggelassen, und ein Szenenwechsel (`***`, `---`) wird nicht gelesen, sondern ergibt eine längere Pause. Schalte die Regeln für ein Buch in einer anderen Sprache aus; das Wörterbuch gilt trotzdem. **Try a text** zeigt, wie ein Textstück gelesen wird. Ein Stück, dessen gesprochener Text sich ändert, wird neu erstellt, die anderen bleiben erhalten.

![Die Aussprache eines Buches](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Ein Buch konvertieren

*Werkzeugmenü des Geschichtseditors (die Zahnrad-Schaltfläche) → **Convert to audio***.

![Der Dialog vor dem Start](../../images/plugin-audiobook-convert.png)

1. Wähle das **backend** und drücke **Load**. Die Teile mit Text des aktiven Zweigs werden aufgelistet (das erste ist 1), jeweils mit ihrem Status:
   *Not generated*, *Labelled* (die Labels des Modells sind gespeichert, noch kein Audio), *Generated* (mit Wiedergabeknopf und Download) oder
   *Generated (the text changed)* (das Audio wurde aus einem älteren Text erzeugt und wird neu erzeugt). Ein Teil, den das Studio als final markiert, erhält zusätzlich *(final)*.
2. Lass **Label the parts with a language model** für die einfache Version ausgeschaltet: Der Erzähler liest jeden Absatz vor. Schalte es ein, damit das Modell festlegt, wer jede Rede spricht, wie (ruhig, flüsternd, schreiend ...) und wo ein Klangmarker passt. Die Besetzung liefert die Stimmen. Das Modell des Buchs ist ausgewählt; wähle bei Bedarf ein anderes. Die Teile werden zuerst beschriftet und danach gesprochen. Ein Teil, der bereits Labels für seinen Text hat, wird auch bei ausgeschaltetem Kästchen mit diesen Labels gelesen (das Modell wird nicht befragt). Wenn du ein beschriftetes Buch erneut ohne das Kästchen laufen lässt, wird seine wörtliche Rede also nicht zu einfacher Erzählung. **Remove labels** ist der Weg, um einfache Erzählung oder neue Labels anzufordern.
3. Markiere die Teile (das Kästchen in der Kopfzeile markiert alle) und **plan**, was mit ihnen geschehen soll. Die Spalte **Plan** zeigt es an, es wird aber noch nichts ausgeführt:
   **Generate** (erzeugt, was fehlt; ein Teil, der auf dem neuesten Stand ist, wird nicht erneut erzeugt), **Regenerate** (löscht das Audio, entfernt die Labels und erzeugt neu),
   **Delete audio** (die Stücke und das zusammengefügte Audio des Teils; die Labels bleiben), **Remove labels** (das Modell wird beim nächsten Erzeugen des Teils erneut befragt),
   **Clear the plan**. Schritte summieren sich bei einem Teil (Delete audio und Remove labels ohne Generate räumen nur auf). Ein als final markierter Teil wird aus jedem Plan ausgelassen.
4. **Start** führt den Plan aus. Wenn dabei Audio oder Labels entfernt werden, fragt das Programm vorher nach. Was erzeugt werden soll, landet in deiner Warteschlange, die weiterläuft, wenn du das Fenster schließt oder die Seite verlässt. Ein Teil, der wartet oder in der Warteschlange läuft, bleibt aus einem Plan ausgeschlossen. Der Status, die bisher erzeugten Stücke und die Meldung des Auftrags werden in der Zeile des Teils angezeigt.
   Während das Modell einen Teil beschriftet, zeigt seine Zeile etwa *Labelling pieces 17-32 of 120* (das Modell wird jeweils nach 16 Stücken gefragt). **Klicke auf den Status** eines beschrifteten Teils, um für jede Anfrage die Überlegungen und die Antwort des Modells zu lesen, sobald sie eintreffen. Wenn die Beschriftung eines Teils endet (auch wenn sie fehlschlägt oder gestoppt wird), wird alles beim Teil behalten, jeder Lauf nach den vorherigen. Lies es erneut oder lösche es mit **Reasoning** im Studio. Neue Labels und neues Audio lassen es unberührt, und es ist in den Sicherungen und Exporten des Buchs mit dem Audio enthalten.

![Die Warteschlange: bisher erzeugte Stücke und ein Download, wenn ein Teil fertig ist](../../images/plugin-audiobook-queue.png)

So entsteht ein Teil, damit du weißt, was du erwarten kannst:

- Der Text wird **vom Programm zerteilt**, niemals vom Modell, in Stücke mit Erzählung und Stücke mit wörtlicher Rede (zwischen geraden oder typografischen doppelten Anführungszeichen oder japanischen Eckklammern; eine Überschrift ist Erzählung). Das Modell **beschriftet** nur die nummerierten Stücke. Ein Text mit Klangmarkern wird nur verwendet, wenn es das Stück mit hinzugefügten Markern ist und sonst nichts geändert wurde, sodass das Modell dein Buch niemals verändern kann.
- **Wer ein Stück spricht, entscheidet das Modell bei wörtlicher Rede; Erzählung gehört dem Erzähler.** Ein Brief, eine Notiz, eine Nachricht oder die Gedanken einer Figur haben keine Anführungszeichen, daher nennt das Programm sie Erzählung. Ein solches Stück wird nur dann von der Figur gelesen (der Stimme aus der Besetzung), wenn es **kursiv beginnt** (`*...*` oder `_..._`) und das Modell die Figur aus dem Kontext nennt (eine Unterschrift, „die Handschrift war unverkennbar“). Erzählung in der dritten Person, auch wenn sie aus der Sicht einer Figur erzählt wird („Sie sah zu, wie Percy durch den Raum ging“), gehört immer dem Erzähler: Das Programm ignoriert eine Figur, die das Modell einem nicht kursiven Stück zuweist. Kursivschrift zur Betonung, für Laute oder Fremdwörter gilt ebenfalls als Erzählung. Die Worte bleiben immer deine.
- **Die Anweisung an das Modell ist eine Vorlage, die du ändern kannst.** *Settings → Extension Settings → Audiobook Settings → Labelling prompt* ist deine Voreinstellung; ein Buch (Reiter *Prompts* → *Extension Settings* → *Audiobook*) oder ein Ordner kann eine eigene haben. Leer bedeutet die des übergeordneten Ordners, dann deine, dann die eingebaute. Sie ist wie die anderen [Prompts](../books/prompts.md) geschrieben: Die Variablen sind `{{characters}}`, `{{deliveries}}`, `{{tags}}` und `{{wordsPerTag}}`, und das Hinweis-Popover enthält **Insert Default Template**. Eine geänderte Anweisung sorgt dafür, dass die Labels der betroffenen Teile beim nächsten Erzeugen erneut angefragt werden.
- **Die Lautstärke der Teile wird angeglichen**, wenn das Programm ffmpeg installiert ist (dasselbe, das `.m4b` erzeugt): Das zusammengefügte Audio jedes Teils wird auf eine einheitliche Lautstärke gebracht (EBU R128, linear, damit die Dynamik erhalten bleibt; **-18 LUFS**, sofern du es nicht in *Settings → Extension Settings → Audiobook Settings* änderst, wo du es auch ausschalten kannst). Die Stücke bleiben so, wie der Server sie erzeugt hat; nur das zusammengefügte Teil wird angeglichen, damit ein Hörbuch von Teil zu Teil nicht in der Lautstärke springt. **Teile, die früher erzeugt wurden**, bleiben unberührt, bis du sie erneut konvertierst: Dann werden sie aus den gespeicherten Stücken neu zusammengefügt und angeglichen, ohne dass der Sprachserver etwas erhält (auch kein Entladen des Sprachmodells). Wenn du die Angleichung ausschaltest, bleiben die bereits angeglichenen Teile so, wie sie sind. Schlägt ffmpeg bei einem Teil fehl, behält er sein Audio wie zuvor, und das Ergebnis meldet dies.
- Alle Teile eines Klicks werden in **einem Stapel pro Sprachmodell** erzeugt: Die Anfragen vor dem Stapel laufen einmal, dann werden alle Stücke gesprochen, dann laufen die Anfragen nach dem Stapel einmal. So wird verhindert, dass die Grafikkarte bei jedem Teil das Modell wechselt.
- Jedes Stück wird **nach seinem Inhalt gespeichert** (Text, Stimme, Modell, Sprache, Einstellungen, Seed). Ein Teil, der bereits auf dem neuesten Stand ist, kostet nichts; ein geänderter Absatz ist das einzige Stück, das erneut erzeugt wird; und ein fehlgeschlagener Teil behält die bereits erzeugten Stücke, wenn du **Retry** drückst.
- Die Stücke werden mit Pausen zusammengefügt, die von ihrer Stelle abhängen (nach einer Überschrift, zwischen Absätzen, vor *he said*) zu einer mp3-Datei pro Teil, die beim Teil behalten wird.
- Solange eine Erzeugung von dir läuft, wartet die Sprachausgabe, damit das Sprachmodell nicht darunter entladen wird.
- Ein Teil, das du im Studio als **final** markiert hast, bleibt unberührt, und der Dialog sagt, wie viele es waren.
- **Das ganze Hörbuch** fügt jeden Teil mit aktuellem Audio zu einer mp3-Datei zusammen, mit einer Kapitelmarke für jeden Teil, und zeigt einen Download-Link unter der Liste (die ausgelassenen Teile werden genannt). Dieselbe Datei ist das Exportformat *one mp3 with chapter marks*.
- **Stop all** bricht die wartenden Teile ab und stoppt das laufende sofort: Die Anfrage an das Sprachmodell (die Beschriftung) oder an den Sprachserver (das gerade gesprochene Stück) wird verworfen, nicht abgewartet. Ein Stapel, der gestoppt wird, bevor er beginnt, sendet die Anfragen davor nicht, sodass das Sprachmodell geladen bleibt. Ein Teil, dessen Stücke alle vor dem Stopp erzeugt wurden, erhält trotzdem sein Audio (es wird nur zusammengefügt); die anderen behalten die Stücke, die sie haben, und der nächste Lauf erzeugt nur die fehlenden. **Download** liefert die mp3-Datei eines fertigen Teils.
<a id="the-audiobook-studio"></a>

## Das Hörbuch-Studio

*Tools menu → **Audiobook studio*** (die Stimme des ausgewählten Stücks mit einer Vorschau steht neben dessen Lautsprecher) ist der Ort, an dem das fertige Hörbuch entsteht, Teil für Teil: Sie sehen das Skript, ändern es, hören ein Stück an und erstellen den Teil.

![Das Studio mit einem ausgewählten Sprechstück](../../images/plugin-audiobook-studio.png)

- Während der Teil annotiert oder erstellt wird, zeigt eine Leiste an, dass etwas läuft, und die Schaltflächen, die weitere Arbeit starten würden, warten.
- **Annotate** erstellt das Skript des Teils (mit dem Sprachmodell, wenn Sie es verlangen, sonst mit dem Erzähler) und zeigt die Stücke in einer Tabelle: Sprecher, Vortrag, Intensität, den gesprochenen Text und eine Markierung für ein Stück, das Sie geändert haben. Während es läuft, erscheint daneben eine Schaltfläche **Stop**, die die Anfrage an das Sprachmodell beendet.
- **Reasoning** zeigt, was das Sprachmodell jedes Mal sagte, wenn es den Teil beschriftete (das Datum des Laufs und die Stücke jeder Anfrage, seine Begründung und seine Antwort), damit Sie sehen, warum eine Zeile an einen Sprecher ging oder einen bestimmten Vortrag bekam. **Delete the reasoning** entfernt sie dort; die Beschriftungen und das Audio bleiben.
- Wählen Sie ein Stück aus, um seinen **speaker**, den **spoken text**, die **delivery**, die **intensity** (die Übertreibung der Stimme, für die Modelle, die eine solche besitzen; bei Higgs sind Vortrag und Intensität die Markierungen wie `<|emotion:sadness|>` am Anfang des gesprochenen Textes, die ihnen folgen, wenn Sie sie ändern), die **language**, den **seed** und die **pause after** zu bearbeiten. **Apply** behält die Änderung und **sperrt** das Stück: Eine neue Annotation lässt ein gesperrtes Stück unverändert.
  **Unlock** gibt es der Annotation zurück. *The request the speech server gets* zeigt die genaue Anfrage.
- **Hear** erzeugt das Audio eines Stücks (nur dieses Stück; der Sprachserver wird dafür gestartet). **Another take** erzeugt es erneut mit einem neuen Seed, und die **takes** werden aufbewahrt, damit Sie die gewünschte auswählen können (*Use this take and hear it*). Eine gute Stimme ist oft eine Frage des Seeds.
- **Make the part from this script** fügt die Stücke so zusammen, wie sie sind, ohne neue Annotation.
- Der **status** des Teils ist *draft*, *reviewed* oder *final*. Ein *final* markierter Teil wird nicht erneut annotiert oder umgewandelt, und der Export kann auf die Teile beschränkt werden, die *final* sind.

<a id="exporting"></a>

## Exportieren

Das Hörbuch ist ein Format des *Export story* des Buches: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** und, wenn ffmpeg installiert ist, **Audiobook (one m4b with chapters)**. Sie werden aus dem Audio der Teile erstellt, die auf dem neuesten Stand sind: Ein Teil ohne Audio oder mit Audio, das älter ist als sein Text oder als sein Skript im Studio, wird ausgelassen, und das Fenster sagt, welche Teile das betrifft. Die mp3-Dateien tragen ID3-Tags (den Titel des Buches, das Kapitel, die Spur), und die einzelne Datei hat Kapitelmarken, die Player wie VLC und die meisten Hörbuch-Apps anzeigen; die zip-Datei enthält eine Playlist. Das Kontrollkästchen **Audiobook: only the parts marked final** begrenzt den Export auf die Teile, die Sie im Studio fertiggestellt haben.

Das **m4b** ist das Format, das Apple Books und die meisten Hörbuch-Apps auf dem Telefon verlangen: AAC-Audio (64 kbit/s, für Sprache ausreichend) in einem MP4-Container mit den Kapiteln, dem Titel, dem Autor und dem Genre. ffmpeg erstellt es, daher wird das Format nur auf einem Server angeboten, der ffmpeg hat. Audio, das als mp3 gespeichert ist, wird dafür ein zweites Mal kodiert; Audio, das als wav gespeichert ist, wird nur einmal kodiert.

<a id="how-the-audio-is-stored"></a>

### Wie das Audio gespeichert wird

Ein Administrator wählt es für die gesamte Installation unter *Settings → Extension Settings → Audiobook Settings* (der letzte Abschnitt, nur für Administratoren sichtbar):

- **mp3** (Standard): klein, ohne Decoder zusammengefügt, kein ffmpeg nötig. Der m4b ist ein zweiter verlustbehafteter Schritt.
- **wav**: verlustfrei, etwa zehnmal so groß (eine Stunde Sprache sind etwa 170 MB), benötigt ffmpeg. Die Stücke werden vom Sprachserver als wav angefordert und als wav zusammengefügt; der Browser spielt sie so ab, wie sie sind;
  die mp3- und zip-Exporte werden von ffmpeg kodiert (96 kbit/s), und der m4b wird einmal kodiert.

Die Wahl gilt für alles, was von da an erstellt wird. Früher erstellte Teile behalten ihr Audio und werden so verstanden, wie sie sind; ein Teil beider Arten (ein gesperrtes Stück aus dem Studio von früher) wird als wav zusammengefügt, und ein Export von Teilen beider Arten funktioniert, benötigt aber ffmpeg.

Ein Export des ganzen Buches wird aus den gespeicherten Dateien der Teile in temporäre Dateien des Servers erstellt und von dort heruntergeladen; seine Länge ist daher nicht durch den Arbeitsspeicher der Anwendung begrenzt, aber der temporäre Ordner braucht während der Erstellung Platz für etwa das Doppelte des Audios des Buches (bei wav etwa 350 MB pro Stunde).

![Der Exportdialog mit den Hörbuchformaten](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Automatische Umwandlung

Ein Buch kann von selbst das Audio eines neuen Teils erzeugen, sobald der Teil *final* ist: nach der automatischen Annotation, Überarbeitung und den Übersetzungen, falls das Buch sie hat, als letzter Schritt von [was dem Teil folgt](../books/story-editor.md#what-follows-a-generated-part) (im Bereich der Läufe, bis das Audio erstellt ist; dessen **Stop** nimmt den Teil aus der Warteschlange). Sie ist standardmäßig **aus**. Wählen Sie sie im Reiter *Prompts* des Buches unter den Erweiterungseinstellungen, für einen Ordner von Büchern oder für alle Ihre Bücher in den Hörbuch-Einstellungen; die speziellste Einstellung gilt:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; ein Buch ohne Modell erhält die einfache Erzählung, und Sie werden darauf hingewiesen.

![Die Einstellung in einem Buch](../../images/plugin-audiobook-auto.png)

Der Teil kommt in Ihre Warteschlange, mit der Besetzung und der Aussprache des Buches. Eine Meldung teilt Ihnen mit, dass er eingereiht ist, und eine weitere, dass sein Audio fertig oder fehlgeschlagen ist; sie erscheinen nur auf der Seite, die die Erzeugung gestartet hat. Der Auftrag steht in jedem Fall im Fenster *Convert to audio*.

<a id="listening"></a>

## Anhören

Ein veröffentlichtes Buch (oder der Leser) mit Teilen, die Audio haben, erhält eine Player-Leiste: Abspielen, Pause, Stopp, die Geschwindigkeit und der gerade gehörte Teil, im Text markiert. Sie beginnt bei der Auswahl oder beim ersten Teil in der Ansicht, spielt die Teile in der Reihenfolge ab und lädt den nächsten, während der aktuelle läuft; die Medientasten des Geräts kennen das Buch. Audio, das aus einem älteren Text eines Teils erstellt wurde, wird nicht angeboten. Das Audio wird dem Besitzer des Buches und, solange das Buch veröffentlicht ist, allen ausgeliefert, die es öffnen können; ein Buch, das aus der Veröffentlichung zurückgezogen wird, wird sofort nicht mehr ausgeliefert (beim nächsten Teil, den der Player lädt).

![Der Player im Leser](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Ein Teil ohne Audio

Der Player hält bei einem Teil ohne Audio an, damit kein Teil der Geschichte übersprungen wird, und sagt dies. Ist die Erweiterung [Read Aloud](tts-reader.md) installiert, zeigt die Leiste außerdem einen Lautsprecher-Button **Read this part (and the next ones without audio) with the voice of the browser**. Nichts wird mit einer Maschinenstimme gelesen, solange Sie es nicht drücken. Dann liest er diesen Teil und die folgenden Teile ohne Audio mit der Stimme Ihres Geräts, und sobald er einen Teil mit Audio erreicht, übernimmt der Player wieder. Spätere Teile ohne Audio in demselben Hördurchgang werden auf die gleiche Weise gelesen, ohne erneut zu fragen; **Stop** schaltet es stumm, und ein erneuter Start des Players fragt wieder. Stimme, Geschwindigkeit und Tonhöhe sind die von Read Aloud.
<a id="commands"></a>

## Befehle

Dasselbe wie [Befehle](../commands/index.md) (die Befehlszeile des Story-Editors, das `command`-Makro einer Vorlage, ein Agent). Teile werden so nummeriert, wie der Convert-Dialog sie nummeriert, oder über ihren Namen (Handle) angesprochen.

| Befehl | Was er tut |
|---|---|
| `/audiobook-status [from [to]]` | Der verwendete Sprachserver, wie das Buch sich selbst konvertiert, wie viele Teile Audio haben (keines, fertig, älter als der Text, älter als das Skript), der Status jedes aufgelisteten Teils im Studio und die Warteschlange. Nur bei einem langen Buch langsam. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Stellt Teile in die Warteschlange (ohne Argumente das ganze Buch, ein einzelner Teil, wenn nur `from` angegeben ist). `labelling` `true` verwendet das Modell des Buchs; `what` ist `audio` oder `script` (nur das Skript für das Studio, es wird nichts gesprochen). Mit `wait` `true` bleibt der Befehl so lange aktiv, bis die Aufträge beendet sind, zeigt an, wie weit sie sind, und meldet, wie jeder ausgegangen ist. Wird er gestoppt, werden die Aufträge abgebrochen, die noch nicht begonnen haben. |
| `/audiobook-stop` | Bricht die wartenden Teile ab; der laufende Teil hält nach seinem Stück an. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` oder `final` im Studio; der Teil braucht ein Skript. |
| `/audiobook-cast` | Der Erzähler, der Fallback und die Figuren der Besetzung mit ihren Sprechern, die Basissprecher unbenannter Sprecher und die bisher begegneten unbenannten Sprecher (Geschlecht, Alter, Variation). |

![Das Ergebnis eines Befehls](../../images/plugin-audiobook-command.png)

Das Audio wird für **Ihr** Buch erstellt, nicht für die verborgene Arbeitskopie eines [Agenten](../agents.md): Ein Agent kann die Besetzung lesen und bekommt von den anderen Befehlen eine Absage mit dem Hinweis, dass
Sie sie selbst ausführen müssen. Führen Sie sie über die Befehlszeile aus, oder geben Sie einem Agenten die Aufgabe, das Buch vorzubereiten (annotieren, prüfen), und konvertieren Sie es selbst.

<a id="notifications"></a>

## Benachrichtigungen

Wenn eine Serie von Konvertierungen fertig ist, kann der Browser Sie benachrichtigen ([Benachrichtigungen](../account.md#notifications), damit Sie den Tab verlassen können, während der Sprachserver arbeitet). In der Benachrichtigungsliste der Seite *Settings* gibt es zwei Zeilen: **Convert to audio** (die Stapel, die Sie im Dialog oder mit dem Befehl starten) und **Convert to audio (automatic)** (ein neuer Teil, der von selbst in Audio umgewandelt wurde). Ein Stapel, den Sie abgebrochen haben, löst keine Benachrichtigung aus. Ein Stapel mit einem fehlgeschlagenen Teil meldet, dass er fehlgeschlagen ist.

![Die Benachrichtigungsliste](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Was es (noch) nicht kann

- Nur Mp3 und Wav (kein Opus); die Lautstärke der Teile ist das, was die Stimmen liefern.
- Keine Soundeffekte oder Hintergrundatmosphäre.
- Nur zwei Sprach-Engines: Chatterbox und Higgs Audio v3 (in ComfyUI).
- Das Vorhören einer Stimme außerhalb eines Segments.
- Die Besetzung ist nicht Teil der Sicherung Ihrer *Sprecher* in einem Buch: Ein wiederhergestelltes Buch behält seine Besetzung, verweist aber auf Sprecher, die in Ihrem Konto bereits vorhanden sein müssen.
