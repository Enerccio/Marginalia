---
label: Hörbuch
order: 27
---

# Hörbuch

Verwandelt ein Buch Teil für Teil in ein Hörbuch, mit einem Text-to-Speech-Server: eine Stimme für den Erzähler und eine für jede Figur, gespeichert bei den Teilen des Buches und im Leser abgespielt. Sie können es laufen lassen (das Modell kennzeichnet, wer spricht und wie, der Sprachserver liest vor), etwa für einen groben Entwurf. Sie können die Teile aber auch einzeln im **Audiobook studio** bearbeiten, dabei genau sehen, was an den Sprachserver geschickt wird, es von Hand ändern und das Ergebnis als mp3-Dateien mit Kapitelmarken exportieren.

Die Erweiterung erzeugt selbst keine Sprache: Sie spricht mit einem **Chatterbox-TTS-Server**, den Sie betreiben (siehe [Einrichten des Sprachservers](audiobook-server.md)). Der Text des Buches wird nie verändert und nur an die Server gesendet, die Sie angegeben haben. Das Audio wird in Ihrer eigenen Installation gespeichert, als Ressourcen, die mit den Teilen verknüpft sind.

<a id="what-you-need"></a>

## Was Sie brauchen

- Einen Sprachserver und, falls er sich die Grafikkarte mit Ihrem Sprachmodell teilt, die Anfragen, die Platz dafür schaffen und ihn danach wieder freigeben ([Einrichten des Sprachservers](audiobook-server.md)).
- Stimmen: einen kurzen **Referenzclip** (12 bis 30 Sekunden einer Person, ohne Musik) für jede Stimme, die Sie klonen möchten, oder eine der Stimmdateien, die der Server bereitstellt. Ein Erzähler braucht eine ruhige, gleichmäßige Lesestimme.
- Für ein Buch mit mehreren Stimmen ein Sprachmodell, das den Text kennzeichnen kann. Standardmäßig ist es das Modell des Buches.
- **ffmpeg** ist optional. Ohne es funktioniert alles mit mp3. Mit ffmpeg auf dem Server (es ist im Docker-Image enthalten; auf einem Desktop wird es im `PATH` oder in den üblichen Ordnern wie `/opt/homebrew/bin` gefunden; die Variable `MARGINALIA_FFMPEG` nennt den Pfad, wenn es woanders liegt) erhält der Export das Format **m4b**, und ein Administrator kann das Audio als wav speichern.

<a id="the-settings"></a>

## Die Einstellungen

*Einstellungen → Extension Settings → Audiobook Settings* ist das Fenster Ihres Kontos. Speichern Sie die Seite *Einstellungen* nach einer Änderung.

![Das Fenster mit einem Backend und den Darstellern](../../images/plugin-audiobook-settings.png)

### Backends

Ein Backend ist ein Sprachserver und das Modell, mit dem er spricht. Fügen Sie eines mit **+** hinzu. **Duplicate** kopiert das ausgewählte Backend (Adresse, Schlüssel, GPU-Sperre, Stimme, Voreinstellungen und die Anfragen vor und nach einem Stapel) unter einem neuen Namen. So kommen Sie schnell zu einem Backend mit einem anderen Modell auf demselben Rechner, zum Beispiel Higgs neben Chatterbox. Dort ändern Sie dann das Modell, die Adresse und die Start- und Stopp-Anfragen.

| Einstellung | Was sie tut |
|---|---|
| Name | Der Name, der im Dialog zum Konvertieren angezeigt wird. |
| Adresse | Die Basisadresse des Sprachservers oder dessen, was an ihn weiterleitet, zum Beispiel `http://localhost:8004`. |
| Api key | Wird als Bearer-Token gesendet, wenn der Server (oder Ihr Proxy) eines verlangt. Er wird verschlüsselt gespeichert und nicht wieder angezeigt. Geben Sie einen neuen ein, um ihn zu ersetzen, oder nutzen Sie **Remove the key**. |
| Sprachmodell | **Chatterbox Turbo** (Englisch, mit Klangmarken wie `[sigh]`, das schnellste und der Standard), **Chatterbox** (Englisch, mit einer *exaggeration* der Betonung), **Chatterbox Multilingual** (23 Sprachen) oder **Higgs Audio v3 in ComfyUI** (viele Sprachen, Stimmen, die die Stimmung einer Zeile übernehmen, und Geräusche wie Weinen, Keuchen oder Stöhnen; siehe [unten](#higgs-audio-v3-emotions-and-sounds)). Die [GPU-Sperre](../administration/gpus.md) des Backends (oder die Anfragen vor einem Stapel) muss dieses Modell geladen lassen. Ein Buch, das ein anderes Modell braucht, verwendet ein anderes Backend. |
| GPU-Sperre | Die [GPU-Sperre](../administration/gpus.md) der GPU, auf der der Server läuft, wenn er sie sich mit dem Sprachmodell oder anderen Servern teilt (auch mit dem [Bildgenerator](image-generator.md)). Jeder Stapel und jede Probe wartet auf seinen Zug an der GPU, und die Sperre lädt zuerst den Sprachserver. Wird nur angezeigt, wenn der Administrator GPU-Sperren eingerichtet hat. |
| Der Server läuft auf diesem Rechner oder in meinem Netzwerk | Erlaubt Adressen des lokalen Netzwerks. Lassen Sie es aus, wenn der Server in der Cloud läuft: Dann kann er nicht dazu gebracht werden, Ihren eigenen Rechner anzurufen. |
| Zeitlimit einer Anfrage | Wie lange auf ein Stück Sprache gewartet wird. |
| Server neu starten nach (Stücken) | Für einen Sprachserver, der Speicher verliert und in einem langen Stapel vom System beendet wird: Nach so vielen Stücken werden die Anfragen nach dem Stapel und dann die davor mitten im Stapel erneut gestellt (stoppen, starten, laden), und die Arbeit geht mit einem frischen Server weiter. 0 (der Standard) macht das nie. Ein Server, der abstürzt, wird unabhängig davon neu gestartet, siehe [Fehlerbehebung](audiobook-server.md#troubleshooting). |
| Schlüssel der Anfragen um einen Stapel | Der Schlüssel, den die Anfragen vor und nach einem Stapel als `Authorization: Bearer` mitschicken. Er wird verschlüsselt gespeichert und nicht wieder angezeigt (**Remove the key** leert ihn). So muss er nicht in die Header jeder Anfrage geschrieben werden, wo er lesbar wäre. Leer: Es wird der *Api key* oben verwendet. Das ist richtig, wenn ein Proxy wie LiteLLM alles bedient. Eine Anfrage, die einen eigenen `Authorization`-Header hat, behält ihn. |
| Erzählerstimme | Der Erzähler eines Buches ohne Besetzung. Eine Stimmdatei des Servers wird `predefined:Henry.wav` geschrieben. |
| Genauigkeit | Nur Higgs: die Genauigkeit, mit der das Modell rechnet. **fp32** (der Standard) ist die sichere Wahl: Auf einer AMD-Karte haben **bf16**-Stücke Kauderwelsch, falsche Geräusche und falsche Stimmen erzeugt. bf16 braucht etwa die Hälfte des Grafikspeichers (etwa 8 GB statt 16) und ist dort schneller, wo es funktioniert. |
| Den Text des Clips an Higgs senden | Nur Higgs, experimentell: Der [Text des Clips](#voices-and-clips) wird mitgeschickt. Lassen Sie es aus, denn in Tests hat es die Stimme verändert. |
| Länge der Stücke prüfen | Standardmäßig an. Ein Stück, das weit länger ist als sein Text (4 s plus 0,6 s pro Wort: Das Modell hat weitergeredet oder statt der Wörter ein Geräusch erzeugt), wird mit dem nächsten Seed neu erzeugt, bis zu **Wiederholungen** Mal. Die erste Fassung mit richtiger Länge bleibt, sonst die kürzeste. |
| Wörter mit Spracherkennung prüfen | Standardmäßig aus; braucht die [Spracherkennung](#speech-recognition). Nach jedem Stapel wird jedes Stück transkribiert und mit seinem Text verglichen. Ein Stück, das bei mehr als **Max. word errors (%)** seiner Wörter abweicht, wird mit dem nächsten Seed neu erzeugt und erneut geprüft, bis zu **Wiederholungen** Runden. Die Fassung mit den wenigsten falschen Wörtern bleibt. Die Konvertierung meldet, wie viele Stücke neu erzeugt wurden und wie viele immer noch abweichen. |
| Voreinstellungen eines Segments | Temperatur, CFG-Gewicht, Tempo und Chunk-Größe, mit denen ein Stück beginnt (das Studio kann sie für ein einzelnes Stück ändern). Higgs nutzt nur die Temperatur. |
| Vor einem Stapel, Nach einem Stapel | Anfragen, die um jeden Stapel von Teilen (und jede Probe) gestellt werden, innerhalb der GPU-Sperre. Die Grafikkarte freizugeben und den Sprachserver zu starten ist Aufgabe der GPU-Sperre; siehe [Eine GPU für Modell und Sprache](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Leer: Es passiert nichts. |

**Test connection** tut dasselbe wie ein Stapel: Es nimmt die GPU-Sperre (sodass ein angehaltener Sprachserver gestartet wird), stellt die Anfragen vor einem Stapel, fragt den Server, ob er erreichbar ist und ein Modell geladen hat, und stellt danach die Anfragen nach dem Stapel. Ohne Sperre oder Anfragen fragt er nur nach.

Ein Buch mit Stücken in einer anderen Schrift (Japanisch in einem englischen Buch) schickt diese an das mehrsprachige Modell, wenn Sie dafür ein Backend haben, mit ihrer Sprache. Haben Sie keines, bleiben sie beim Modell des Buches. Ein Higgs-Buch behält sie: Higgs liest sie selbst, in derselben Stimme.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: Emotionen und Laute

Mit einem **Higgs Audio v3 in ComfyUI**-Backend werden die Auszeichnungen des Sprachmodells hörbar. Die Adresse des Backends ist Ihr ComfyUI
(mit den TTS-Audio-Suite-Knoten, siehe [Einrichten des Sprachservers](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- Die **Vortragsweise** einer Sprechpassage wird gesprochen: traurig, wütend, freudig, amüsiert, angespannt, ängstlich, überrascht, zärtlich, sarkastisch, flehend, verzweifelt,
  angewidert, erregt, ein Flüstern oder ein Schrei. Eine starke (Intensität 0.8 und mehr) wirkt ausdrucksvoller, eine schwache (unter 0.2) wird schlicht gelesen.
  Der Erzähler liest schlicht.
- Das Sprachmodell kann mehr **Laute** in die Rede setzen als bei Chatterbox Turbo: Lachen, Glucksen, Kichern, Seufzen, Keuchen, Husten, Räuspern,
  Schniefen, Niesen, Ächzen, Weinen, Schluchzen, Schreien, Hecheln, Stöhnen, Summen und Schhh.
- Das Programm liest außerdem die **Erzählung um jede Rede** (auf Englisch) und gibt der Rede den Laut, den sie nennt, wenn das Modell keinen gesetzt hat:
  *"You look funny," she giggled.* bekommt ein Kichern, *She burst out laughing. "Careful!"* ein Lachen, *a soft moan escaped her lips. "Yes..."* ein Stöhnen,
  *panting* oder *out of breath* ergeben ein Hecheln, *cried quietly* oder *burst into tears* ein Weinen. Es lässt aus, was nur wie ein Laut aussieht: *"Stop!" she cried.*
  (ein Ausruf), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh* und ein Laut, den die Erzählung
  einer anderen benannten Figur gibt. *"You look funny," he mocked her.* bekommt einen sarkastischen oder amüsierten Vortrag und keinen Laut. Die Erzählung selbst wird so gelesen, wie sie geschrieben ist.
- **Der Erzähler wird immer schlicht gelesen**: ohne Laute und ohne Stimmung, auch bei einer Rede, die das Modell dem Erzähler überließ.
- Higgs braucht keinen Clip pro Stimmung: ein Clip einer Stimme genügt. Er kopiert die Aufnahme des Clips, daher ist ein sauberer Clip wichtiger als bei Chatterbox
  ([Ein guter Clip](#a-good-clip)).
- Jeder Abschnitt wird von ComfyUI erzeugt, und dessen Speicher wird danach geleert (einige Sekunden pro Abschnitt), was verhindert, dass manche Grafikkarten abstürzen.
- Higgs redet oft nach dem letzten Wort eines Abschnitts weiter (ein Atemzug, der Anfang eines anderen Lauts), und der Clip endet mitten darin. Das ist als kurzes „Whoosh“ vor der Pause zu hören. Mit **ffmpeg** schneidet die Erweiterung diesen Schwanz ab und blendet jeden Abschnitt aus; ohne sie bleiben die Abschnitte so, wie Higgs sie erzeugt hat.
- Das Higgs-Audio-v3-Modell steht unter einer **Lizenz für Forschung und nicht-kommerzielle Nutzung**: ein Hörbuch, das Sie verkaufen, braucht eine Lizenz von Boson AI.

<a id="speech-recognition"></a>

### Spracherkennung

**Spracherkennung** schreibt auf, was eine Sprechpassage sagt. Sie prüft die Abschnitte eines Stapels (wenn ein Backend **Check the words with speech
recognition** aktiviert hat) und erkennt den [Text eines Stimmclips](#voices-and-clips). Wählen Sie, wo sie läuft:

| Einstellung | Was sie tut |
|---|---|
| Spracherkennung | **ComfyUI (Granite ASR)**: das Granite-Sprachmodell der TTS Audio Suite in Ihrem ComfyUI (die Adresse ist die von ComfyUI). **OpenAI-compatible (whisper)**: jeder Server mit `/v1/audio/transcriptions`, etwa ein Whisper-Server (faster-whisper, speaches) oder die OpenAI-API. |
| Address, Api key | Die Adresse von ComfyUI oder die Basis der API (mit oder ohne `/v1`); der Schlüssel wird verschlüsselt gespeichert. Leere Adresse: keine Spracherkennung. |
| GPU lock | Die [GPU-Sperre](../administration/gpus.md), die die Erkennung belegt. Auf einer Karte mit fp32-Higgs geben Sie ihr eine eigene Sperre, deren Schritte ComfyUI neu starten: ComfyUI hält das Sprachmodell im Speicher des Servers, und das Laden der Erkennung daneben ließ dem Server den Speicher ausgehen. |
| Modell der Erkennung | Leer: `granite-speech-4.1-2b` für ComfyUI, `whisper-1` für den OpenAI-kompatiblen. |
| Timeout, The server is on this machine or on my network | Wie bei einem Backend. |

**Test connection** prüft, ob ComfyUI die Knoten hat oder ob der Server antwortet. Die Abschnitte eines Stapels werden nach dem Stapel geprüft, wenn der Sprachserver wieder freigegeben ist. So brauchen Erkennung und Sprachmodell nie gleichzeitig die Karte.

<a id="cleanup-of-clips"></a>

### Bereinigung von Clips

**Cleanup of clips** bereinigt einen Referenzclip auf einem ComfyUI mit der [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (derselben wie für Higgs-Werke). Das Ergebnis ist eine neue **Version** des Clips in [Stimmen und Clips](#voices-and-clips); der Clip selbst bleibt erhalten.

| Einstellung | Was sie tut |
|---|---|
| Address, Api key | Die Adresse von ComfyUI; der Schlüssel wird verschlüsselt gespeichert. Leere Adresse: keine Bereinigung. |
| GPU lock | Die [GPU-Sperre](../administration/gpus.md), die die Bereinigung belegt. Geben Sie ihr eine eigene Sperre, nicht die der Sprachausgabe oder der Spracherkennung, deren Schritte ComfyUI neu starten. So werden die Bereinigungsmodelle nie neben dem Sprachmodell geladen. |
| Timeout, The server is on this machine or on my network | Wie bei einem Backend (das Zeitlimit gilt für eine Bereinigung, standardmäßig 10 Minuten: ComfyUI lädt ein Modell beim ersten Einsatz herunter). |
| Bereinigung | Die Bereinigungen (Voreinstellungen) zur Auswahl in Stimmen und Clips, nach Namen. Jede ist eine Liste von Schritten, die nacheinander ausgeführt werden. **Default cleanups** stellt die mitgelieferten wieder her. |

Ein Schritt ist einer der folgenden:

- **Separation model**: ein Modell der Suite, das das Audio in zwei Teile trennt, den *extracted*-Teil und den *remaining*-Teil, und einen davon behält. Welcher Teil die Stimme enthält, hängt vom Modell ab: ein Vocals-Modell (`MelBandRoformer`) extrahiert die Stimme, ein Entrauschungsmodell extrahiert das Rauschen, also behalten Sie den *remaining*-Teil. **Aggressiveness** (0 bis 20) bestimmt, wie viel weggenommen wird.
- **Voice fixer**: stellt eine beeinträchtigte Stimme wieder her (ein Telefon, eine alte Aufnahme). Dabei verändert er den Klang der Stimme stärker als eine Trennung.
- **ffmpeg (here)**: der Filter von **Clean with ffmpeg**, den die Anwendung zwischen den Schritten auf ComfyUI ausführt.

Die mitgelieferten Bereinigungen sind *Voice from music* (die Stimme aus einem Film oder Lied), *Remove noise*, *Restore the voice* und *Film or game clip* (die Stimme aus der Musik, dann das Rauschen entfernt). **Test connection** prüft, ob ComfyUI die Knoten hat, und füllt die Modellliste mit den Modellen Ihres Servers. Die Modelle lädt ComfyUI beim ersten Einsatz einer Bereinigung herunter.

<a id="shared-speech-servers"></a>

### Gemeinsame Sprachserver

Auf einer Installation mit mehreren Benutzern kann der Administrator Sprachserver für alle bereithalten. So wird eine GPU-Maschine einmal eingerichtet, und niemand braucht eine Adresse oder einen Schlüssel. Der Administrator tut das im selben Bereich (den letzten Abschnitten, die nur Administratoren sehen): **Shared speech servers** öffnet ein Fenster mit denselben Feldern wie ein eigenes Backend, dazu die Angabe, wer ihn nutzen darf: **all users** oder die in der Liste ausgewählten Benutzer. Ein Schalter dort, **Users may add speech servers of their own**, entscheidet, ob die unten genannten Backends weiterhin erlaubt sind.

Was Sie erhalten:

- **Backend in use** oben im Bereich listet die gemeinsamen Server, die Sie nutzen dürfen (mit *(shared)* markiert), und Ihre eigenen. Das Fenster *Convert to audio* und das Studio nutzen den, den Sie auswählen; die Befehle nutzen ihn ebenfalls.
- Sie sehen nur den Namen eines gemeinsamen Servers, nie seine Adresse, Schlüssel oder Anfragen. Die Stapel aller Benutzer auf einer Serveradresse warten aufeinander, sodass zwei Benutzer dieselbe GPU nicht doppelt starten können.
- Entzieht der Administrator Ihnen einen Server, wird ein Auftrag, der ihn noch nennt, abgelehnt; wählen Sie dann einen anderen.
- Schaltet der Administrator Ihre eigenen Server ab, zeigt der Bereich das an. Die von Ihnen hinzugefügten bleiben erhalten und werden wieder genutzt, wenn der Schalter wieder eingeschaltet wird.
- Die *Narrator voice* eines gemeinsamen Servers sollte eine Stimmdatei des Servers sein (`predefined:Henry.wav`): ein Referenzclip gehört einem Benutzer und kann von den anderen nicht gelesen werden. Stimmen Ihrer Figuren funktionieren wie gewohnt.
<a id="actors"></a>

### Schauspieler

Ein **Schauspieler** ist eine Stimme von dir: ein Referenzclip (eine deiner Audio-[Ressourcen](../resources.md) oder ein hier hochgeladener neuer Clip) oder eine
Stimmdatei des Servers, mit eigenen Stimmen für eine **Stimmung** (ein Flüstern, ein Schreien) oder eine **Sprache** (ein Clip, der Japanisch spricht). Die Sprache hat Vorrang vor der Stimmung, und ohne eine Stimme für beide liest die Stimme des Schauspielers. Deine Schauspieler werden in deinem Konto gespeichert; jedes Buch kann einem Schauspieler den Erzähler und seine Figuren zuweisen.

**Vorschau** neben einer Stimme (im Schauspieler, in den Einstellungen und in der Besetzung) spricht ein paar Sätze mit ihr (einen kurzen in der Sprache der Stimme bei einer Sprache) und spielt sie ab; wenn die GPU etwas anderes belegt, lädt die GPU-Sperre zuerst den Sprachserver. Der Pfeil daneben bietet weitere Beispiele an: **mit Geräuschen** (ein Seufzen, ein Lachen, ein Keuchen) für die Modelle, die das vertonen (Chatterbox Turbo, Higgs Audio v3), und bei Higgs jede **Stimmung** aus der Kennzeichnung (traurig, flüsternd, sarkastisch ...). Die Schauspieler sind nach Namen aufgelistet.

Jedes Beispiel beansprucht die GPU nur für sich. Die GPU bleibt nach einem Beispiel mit dem Sprachserver geladen, sodass die nächsten Beispiele sofort kommen, bis eine Anfrage des Sprachmodells (oder eines anderen Servers) die GPU braucht. Das Öffnen von **Stimmen und Clips** lädt den Sprachserver bereits, sodass sein erstes Beispiel nicht wartet.

![Ein Schauspieler mit einer Stimme für eine Stimmung](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Stimmen und Clips

**Stimmen und Clips** (neben den Schauspielern) ist ein Fenster für deine Schauspieler und die Clips, aus denen ihre Stimmen geklont werden. Es hat drei Reiter, zunächst nebeneinander: **Schauspieler**, **Clips** und **Clip**. Ziehe einen Reiter, um ihn neben, unter oder zusammen mit einem anderen anzuordnen; das Fenster öffnet sich wieder so, wie du es verlassen hast.

- **Schauspieler**: jeweils mit seiner Stimme, der Anzahl weiterer Stimmen (Stimmungen, Sprachen) und der schlechtesten Qualität seiner Clips; hinzufügen, bearbeiten oder löschen.
- **Clips**: deine Clips in **Ordnern** (zum Beispiel einer pro Figur), mit den darunter stehenden **Versionen**, die aus einem Clip gemacht wurden, jeweils mit Länge, ihrer **Qualität** und dem Ort, an dem sie verwendet wird (Schauspieler, mit Stimmung oder Sprache, und die Erzähler deiner Backends). **Clip hinzufügen** lädt einen hoch.
- **Clip**: der ausgewählte Clip.
  - **Name** und **Ordner**: benennt den Clip um (die Datei behält ihren Namen) und legt ihn in einen Ordner, einen neuen, indem du seinen Namen eingibst. Eine Version bleibt im Ordner des Clips, aus dem sie gemacht wurde, und zeigt, woraus und wie sie gemacht wurde.
  - **Abspielen** spielt ihn ab, zeigt, was mit ihm nicht stimmt, **Ersetzen...** ersetzt ihn oder **Löschen** entfernt ihn (nur, wenn nichts ihn verwendet; seine Versionen rücken zu dem Clip auf, aus dem sie gemacht wurden).
  - **Mit ffmpeg bereinigen** erstellt eine Version, in der das gleichmäßige Zischen und Brummen schwächer ist. Es entfernt keine Musik, keine Effekte und keine anderen Stimmen.
  - **Mit KI bereinigen** erstellt eine Version mit der gewählten [Bereinigung](#cleanup-of-clips) auf ComfyUI. Sie läuft im Hintergrund: du kannst weiterarbeiten, und eine Meldung sagt, wann die Version fertig ist.
  - **Diese Version verwenden** setzt eine Version überall dort ein, wo der Clip, aus dem sie gemacht wurde, verwendet wird. Die Teile werden mit der neuen Stimme neu erzeugt, und der alte Clip bleibt, bis du ihn löschst.
  - **Text des Clips**: die im Clip gesprochenen Wörter. Tippe sie ein oder drücke **Erkennen**, damit die [Spracherkennung](#speech-recognition) sie aufschreibt, korrigiere sie und **Speichern**. Der Dialog des Schauspielers zeigt dasselbe Feld für den Clip der Stimme des Schauspielers. Eine Version und eine Ersetzung übernehmen den Text. Higgs bekommt ihn nur, wenn das Backend ihn sendet (standardmäßig aus).
- **Ersetzen...** setzt einen anderen Clip, einen deiner eigenen oder einen hier hochgeladenen, überall dort ein, wo der Clip verwendet wird.

Auswahllisten einer Stimme (des Schauspielers, des Erzählers eines Backends) zeigen einen Clip als *Ordner / Clip › Version*.

<a id="a-good-clip"></a>

#### Ein guter Clip

Ein Stimmmodell kopiert die **Aufnahme**, nicht nur die Stimme: der Raum, ein Zischen, die Musik unter einer Filmszene kehren in jeder Zeile zurück, oft als leises „Wusch“ unter den Worten, das kein Filter des Ergebnisses entfernt. Daher:

- Verwende eine saubere Aufnahme eines einzelnen Sprechers mit echten Pausen (eine Probe eines Synchronsprechers, eine zu Hause in einem ruhigen Raum gemachte Lesung), 12 bis 30 Sekunden. Ein aus einem Film oder einer Serie geschnittener Clip ist der schlimmste Fall.
- Die Qualitätsmarken werden am Clip gemessen: **Sauber** (die Pausen liegen mindestens 45 dB unter der Sprache), **Mittel** (35 bis 45 dB oder länger als 30 s, was Chatterbox kürzt), **Schlecht** (Hintergrundgeräusch, unter 35 dB; verzerrt; kürzer als 8 s). Ein Clip, der nicht sauber ist, wird beim Hinzufügen gemeldet.
- Bereinige einen Clip mit KI ([Bereinigung von Clips](#cleanup-of-clips)): Im Test hat die Bereinigung *Film- oder Spielclip* einen Clip mit Zischen, Brummen und Echo von *Schlecht* auf *Sauber* gebracht (die Pausen stiegen von 38 auf 89 dB unter der Sprache). Höre die Version an, bevor du sie verwendest: Ein Trennmodell kann auch einen Atemzug oder das Ende eines leisen Wortes entfernen. Die ffmpeg-Bereinigung senkt nur ein gleichmäßiges Geräusch: Bei einem Filmclip gewann sie etwa 3 dB, und der Clip blieb *Schlecht*.
- Ohne ComfyUI bereinigst du einen Clip von Hand: die Rauschunterdrückung von [Audacity](https://www.audacityteam.org/) mit einem Rauschprofil aus einer Pause des Clips oder einem Gesangsentferner wie [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) für einen Clip mit Musik.

<a id="converting-new-parts-by-itself"></a>

### Neue Teile automatisch vertonen

*Neue Teile automatisch in Audio umwandeln* ist die Voreinstellung für alle deine Bücher; siehe [Automatische Umwandlung](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Die Besetzung und die Aussprache eines Buches

Beide werden beim Buch gespeichert (sie reisen in seiner Sicherung mit) und werden im Dialog **In Audio umwandeln** bearbeitet.

**Besetzung** legt fest, wer spricht: einen Schauspieler für den **Erzähler**, einen für **andere Sprecher** (wer immer das Modell nennt und die Besetzung nicht hat; leer: der Erzähler liest) und einen für jede **Figur**. Eine Figur hat einen Namen und weitere Namen, die der Text für sie verwenden darf (*Tom* für *Tomáš Novák*), durch Kommas getrennt; eine Rede wird der Figur zugeordnet, wenn ihr Name, einer der weiteren Namen oder ein Wort des Namens, das keine andere Figur teilt, passt, ohne Beachtung von Groß- und Kleinschreibung und Akzenten. **Figuren des Buches hinzufügen** füllt die Liste aus der Perspektive und den vorkommenden Figuren der Teile.

![Die Besetzung eines Buches](../../images/plugin-audiobook-cast.png)

**Aussprache** ist, wie der Text *für die Stimme* geschrieben wird; das Buch selbst wird nie verändert. Das **Wörterbuch** ersetzt ganze Wörter (Groß- und Kleinschreibung zählt) durch das, was für sie gesprochen wird: eine Lautschreibung für einen Namen oder ein erfundenes Wort oder eine Lesung in Kana für Japanisch. Für ein Buch auf Englisch lesen die Regeln auch andere Dinge laut vor: Zahlen, Ordinalzahlen, Jahre, Uhrzeiten, Geld und Abkürzungen als Wörter, eine Überschrift, die nur aus einer Zahl besteht, als *Chapter four. The Arch* (das Wort vor der Zahl ist deins), Markdown-Zeichen wie `*` werden weggelassen, und ein Szenenwechsel (`***`, `---`) wird nicht vorgelesen, sondern ist eine längere Pause. Schalte die Regeln für ein Buch in einer anderen Sprache aus; das Wörterbuch gilt weiterhin. **Einen Text testen** zeigt, wie ein Textstück gelesen wird. Ein Stück, dessen gesprochener Text sich ändert, wird neu erzeugt, die anderen bleiben erhalten.

![Die Aussprache eines Buches](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Ein Buch umwandeln

*Werkzeugmenü des Geschichteneditors (das Zahnradsymbol) → **Convert to audio***.

![Der Dialog vor dem Start](../../images/plugin-audiobook-convert.png)

1. Wähle das **backend** und drücke **Load**: Die Teile mit Text des aktiven Zweigs werden aufgelistet (der erste ist 1), jeweils mit ihrem Status:
   *Not generated*, *Labelled* (die Labels des Modells sind gespeichert, noch kein Audio), *Generiert* (mit Abspielknopf und Möglichkeit zum Herunterladen) oder
   *Generated (the text changed)* (das Audio wurde aus einem älteren Text erstellt und wird neu erstellt); *(final)* wird einem Teil angehängt, den das Studio als final markiert.
2. Lass **Label the parts with a language model** für die einfache Version ausgeschaltet: Der Erzähler liest jeden Absatz. Schalte es ein, damit das KI-Modell entscheidet, wer jedes Stück wörtlicher Rede spricht, wie (ruhig, flüsternd, schreiend ...) und wo ein Klangmarker passt; die Besetzung legt die Stimmen fest. Das Modell des Buchs ist ausgewählt; wähle ein anderes, wenn du willst. Die Teile werden zuerst beschriftet, dann gesprochen. Ein Teil, der bereits Labels für seinen Text hat, wird auch bei ausgeschaltetem Kästchen mit ihnen gelesen (das KI-Modell wird nicht befragt). Ein beschriftetes Buch, das du ohne das Kästchen erneut laufen lässt, wird daher nicht zu einfacher Erzählung für seine Dialoge. **Remove labels** ist der Weg, um einfache Erzählung oder neue Labels anzufordern.
3. Markiere die Teile (das Kästchen in der Kopfzeile markiert alle) und **plan**, was mit ihnen geschehen soll; die Spalte **Plan** zeigt es an, und noch wird nichts getan:
   **Generieren** (fehlendes erstellen; ein Teil, der aktuell ist, wird nicht erneut erstellt), **Neu generieren** (Audio löschen, Labels entfernen, erstellen), **Delete audio** (die Stücke und das zusammengefügte Audio des Teils; die Labels bleiben), **Remove labels** (das Modell wird beim nächsten Erstellen des Teils erneut befragt), **Clear the plan**. Schritte summieren sich bei einem Teil (Delete audio und Remove labels ohne Generieren räumen nur auf); ein Teil, der als final markiert ist, wird aus jedem Plan ausgelassen.

**Starten** führt den Plan aus. Wenn dabei Audio oder Labels entfernt werden, fragt es vorher nach. Was erstellt werden soll, kommt in deine Warteschlange, die weiterläuft, wenn du das Fenster schließt und die Seite verlässt; ein Teil, der wartet oder in der Warteschlange läuft, wird aus einem Plan ausgelassen. Status, bisher erstellte Stücke und die Meldung des Auftrags werden in der Zeile des Teils angezeigt.
Während das KI-Modell ein Teil beschriftet, zeigt die Zeile *Labelling pieces 17-32 of 120* (das Modell wird jeweils zu 16 Stücken befragt). **Klicke auf den Status** eines Teils, der beschriftet wurde, um für jede Anfrage die Überlegungen und die Antwort des Modells zu lesen, sobald sie eintreffen. Sie werden zusammen mit dem Auftrag im Speicher gehalten, bis die fertigen Aufträge gelöscht werden (*Clear finished*) oder die Anwendung neu startet.

![Die Warteschlange: bisher erstellte Stücke und ein Download, wenn ein Teil fertig ist](../../images/plugin-audiobook-queue.png)

So entsteht ein Teil, damit du weißt, was du erwarten kannst:

- Der Text wird **vom Programm geteilt**, nie vom Modell, in Stücke mit Erzählung und Stücke mit wörtlicher Rede (zwischen geraden oder typografischen doppelten Anführungszeichen oder japanischen Winkelklammern; eine Überschrift ist Erzählung). Das Modell *beschriftet* nur die nummerierten Stücke. Ein Text mit Lautmarkern wird nur verwendet, wenn er das Stück mit hinzugefügten Markern ist und sonst nichts verändert wurde, sodass das Modell dein Buch nie verändern kann.
- **Wer ein Stück spricht, entscheidet das Modell bei wörtlicher Rede; Erzählung gehört dem Erzähler.** Ein Brief, eine Notiz, eine Nachricht oder die Gedanken einer Figur haben keine Anführungszeichen, daher nennt das Programm sie Erzählung. Ein solches Stück wird nur dann von der Figur (der Stimme der Besetzung) gelesen, wenn es **kursiv beginnt** (`*...*` oder `_..._`) und das Modell die Figur aus dem Kontext nennt (eine Unterschrift, „die Handschrift war unverwechselbar“). Erzählung in der dritten Person, auch wenn sie aus der Sicht einer Figur erzählt ist („Sie sah zu, wie Percy den Raum durchquerte“), gehört immer dem Erzähler: Das Programm ignoriert eine Figur, die das Modell einem nicht kursiven Stück zuweist. Kursivschrift für Betonung, Laute oder Fremdwörter ist ebenfalls Erzählung. Die Worte bleiben immer deine.
- **Die Anweisung an das Modell ist eine Vorlage, die du ändern kannst.** *Settings → Einstellungen der Erweiterung → Audiobook Settings → Labelling prompt* ist dein Standard; ein Buch (Reiter *Prompts* → *Einstellungen der Erweiterung* → *Audiobook*) oder ein Ordner kann eine eigene haben. Leer bedeutet die des übergeordneten Ordners, dann deine, dann die eingebaute. Sie ist wie die anderen [Prompts](../books/prompts.md) geschrieben: Die Variablen sind `{{characters}}`, `{{deliveries}}`, `{{tags}}` und `{{wordsPerTag}}`, und das Hinweis-Popover hat **Standardvorlage einfügen**. Eine geänderte Anweisung bewirkt, dass die Labels der Teile beim nächsten Erstellen erneut angefragt werden.
- **Die Lautstärke der Teile wird angeglichen**, wenn das Programm ffmpeg installiert ist (dasselbe, das `.m4b` erzeugt): Das zusammengefügte Audio jedes Teils wird auf eine gemeinsame Lautheit gebracht (EBU R128, linear, damit die Dynamik erhalten bleibt; **-18 LUFS**, sofern du es nicht in *Settings → Einstellungen der Erweiterung → Audiobook Settings* änderst, wo du es auch ausschalten kannst). Die Stücke bleiben so, wie der Server sie erstellt hat; nur das zusammengefügte Teil wird angeglichen, damit ein Hörbuch nicht von Teil zu Teil in der Lautstärke springt. **Teile, die vorher erstellt wurden**, bleiben unberührt, bis du sie erneut umwandelst: Dann werden sie aus den gespeicherten Stücken neu zusammengefügt und angeglichen, und der Sprachserver wird dafür nicht angefragt (auch das Entladen des KI-Modells entfällt). Wenn du die Angleichung ausschaltest, bleiben die bereits angeglichenen Teile so, wie sie sind. Schlägt ffmpeg bei einem Teil fehl, behält er sein Audio unverändert, und das Ergebnis meldet das.
- Alle Teile eines Klicks werden in **einem Stapel pro Sprachmodell** erstellt: Die Anfragen vor dem Stapel laufen einmal, dann werden alle Stücke gesprochen, danach laufen die Anfragen nach dem Stapel einmal. So wird verhindert, dass die Grafikkarte für jeden Teil das Modell wechselt.
- Jedes Stück wird **anhand seines Inhalts behalten** (Text, Stimme, Modell, Sprache, Einstellungen, Seed). Ein Teil, der bereits aktuell ist, kostet nichts, ein geänderter Absatz ist das einzige Stück, das erneut erstellt wird, und ein fehlgeschlagener Teil behält die bereits erstellten Stücke, wenn du **Retry** drückst.
- Die Stücke werden mit Pausen zusammengefügt, die von ihrer Stelle abhängen (nach einer Überschrift, zwischen Absätzen, vor *he said*), zu einer MP3 pro Teil, die beim Teil gespeichert wird.
- Solange eine deiner Erstellungen läuft, wartet die Sprachausgabe, damit das KI-Modell nicht entladen wird.
- Ein Teil, den du im Studio als **final** markiert hast, bleibt unangetastet, und der Dialog nennt, wie viele es waren.
- **Das ganze Hörbuch** fügt jeden Teil mit aktuellem Audio zu einer MP3 mit einer Kapitelmarke pro Teil zusammen und zeigt unter der Liste einen Link zum Herunterladen (die ausgelassenen Teile werden genannt). Dieselbe Datei ist das Exportformat *one mp3 with chapter marks*.
- **Stop all** bricht die wartenden Teile ab und stoppt den laufenden sofort: Die Anfrage an das KI-Modell (das Beschriften) oder an den Sprachserver (das gerade gesprochene Stück) wird verworfen, nicht abgewartet. Ein Stapel, der gestoppt wird, bevor er begonnen hat, sendet die Anfragen davor nicht, sodass das KI-Modell geladen bleibt. **Herunterladen** liefert die MP3 eines fertigen Teils.
<a id="the-audiobook-studio"></a>

## Das Hörbuchstudio

*Menü Werkzeuge → **Audiobook studio*** (die Stimme des ausgewählten Stücks mit einer Vorschau steht neben seinem Lautsprecher) ist der Ort, an dem das fertige Hörbuch entsteht, Teil für Teil: Sie sehen das Skript, ändern es, hören ein Stück an und erstellen den Teil.

![Das Studio mit einer ausgewählten Äußerung](../../images/plugin-audiobook-studio.png)

- Während der Teil kommentiert oder erstellt wird, zeigt eine Leiste an, dass etwas läuft, und die Schaltflächen, die weitere Arbeit starten würden, warten.
- **Kommentieren** erstellt das Skript des Teils (mit dem Sprachmodell, wenn Sie es verlangen, sonst mit dem Erzähler) und zeigt die Stücke in einer Tabelle: Sprecher, Vortrag, Intensität, den gesprochenen Text und eine Markierung für ein Stück, das Sie geändert haben. Während sie läuft, erscheint daneben eine Schaltfläche **Stopp**, die die Anfrage an das Sprachmodell beendet.
- Wählen Sie ein Stück aus, um seinen **Sprecher**, den **gesprochenen Text**, den **Vortrag**, die **Intensität** (die Betonung der Stimme, bei den Modellen, die sie haben; bei Higgs sind Vortrag und Intensität die Markierungen wie `<|emotion:sadness|>` am Anfang des gesprochenen Textes, die ihnen folgen, wenn Sie sie ändern), die **Sprache**, den **Seed** und die **Pause nach** ihm zu bearbeiten. **Anwenden** behält die Änderung und **sperrt** das Stück: Eine neue Kommentierung lässt ein gesperrtes Stück unverändert.
  **Entsperren** gibt es wieder für die Kommentierung frei. *Die Anfrage an den Sprachserver* zeigt die genaue Anfrage.
- **Anhören** erstellt die Audiodatei eines Stücks (nur dieses Stück; dafür wird der Sprachserver gestartet). **Andere Aufnahme** erstellt sie mit einem neuen Seed erneut, und die **Aufnahmen** werden aufbewahrt, damit Sie die gewünschte auswählen können (*Diese Aufnahme verwenden und anhören*). Eine gute Stimme hängt oft vom Seed ab.
- **Teil aus diesem Skript erstellen** fügt die Stücke so zusammen, wie sie sind, ohne neue Kommentierung.
- Der **Status** des Teils ist *Entwurf*, *überarbeitet* oder *fertig*. Ein fertiger Teil wird nicht erneut kommentiert oder umgewandelt, und der Export kann auf die fertigen Teile beschränkt werden.

<a id="exporting"></a>

## Exportieren

Das Hörbuch ist ein Format des Exports der Geschichte (*Geschichte exportieren*): **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** und, wenn ffmpeg installiert ist, **Audiobook (one m4b with chapters)**. Sie bestehen aus dem Audio der Teile, die auf dem neuesten Stand sind: Ein Teil ohne Audio oder mit Audio, das älter ist als sein Text oder sein Skript im Studio, wird weggelassen, und das Fenster sagt, welche. Die mp3-Dateien tragen ID3-Tags (den Titel des Buches, das Kapitel, die Titelnummer), und die Einzeldatei hat Kapitelmarken, die Player wie VLC und die meisten Hörbuch-Apps anzeigen; das Zip-Archiv enthält eine Wiedergabeliste. Das Kontrollkästchen **Audiobook: only the parts marked final** beschränkt den Export auf die Teile, die Sie im Studio fertiggestellt haben.

Das **m4b** ist das Format, das Apple Books und die meisten Hörbuch-Apps auf dem Telefon verlangen: AAC-Audio (64 kbit/s, für Sprache ausreichend) in einem MP4-Container mit den Kapiteln, dem Titel, dem Autor und dem Genre. ffmpeg erstellt es, daher wird das Format nur auf einem Server angeboten, auf dem ffmpeg vorhanden ist. Audio, das als mp3 gespeichert ist, wird dafür ein zweites Mal kodiert; Audio, das als wav gespeichert ist, wird einmal kodiert.

<a id="how-the-audio-is-stored"></a>

### Wie das Audio gespeichert wird

Ein Administrator wählt dies für die gesamte Installation unter *Einstellungen → Extension Settings → Audiobook Settings* (der letzte Abschnitt, nur für Administratoren sichtbar):

- **mp3** (Standard): klein, wird ohne Decoder zusammengefügt, kein ffmpeg nötig. Das m4b ist ein zweiter verlustbehafteter Schritt.
- **wav**: verlustfrei, etwa zehnmal so groß (eine Stunde Sprache sind etwa 170 MB), benötigt ffmpeg. Die Stücke werden als wav vom Sprachserver angefordert und als wav zusammengefügt; der Browser spielt sie unverändert ab;
  die mp3- und Zip-Exporte werden von ffmpeg kodiert (96 kbit/s), und das m4b wird einmal kodiert.

Die Auswahl gilt für alles, was ab dann erstellt wird. Früher erstellte Teile behalten ihr Audio und werden so verstanden, wie sie sind; ein Teil beider Arten (ein gesperrtes Stück aus dem Studio von früher) wird als wav zusammengefügt, und ein Export von Teilen beider Arten funktioniert, benötigt aber ffmpeg.

Ein Export des ganzen Buches wird aus den gespeicherten Dateien der Teile in temporäre Dateien des Servers erstellt und von dort heruntergeladen. Seine Länge ist daher nicht durch den Speicher der Anwendung begrenzt, aber der temporäre Ordner braucht während der Erstellung Platz für etwa das Doppelte des Audios des Buches (bei wav etwa 350 MB pro Stunde).

![Der Exportdialog mit den Hörbuchformaten](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Automatische Umwandlung

Ein Buch kann von selbst das Audio eines neuen Teils erstellen, sobald der Teil fertig ist: nach der automatischen Kommentierung, der Überarbeitung und den Übersetzungen, falls das Buch sie hat, als letzter Schritt von [was dem Teil folgt](../books/story-editor.md#what-follows-a-generated-part)
(im Bereich der Läufe, bis das Audio erstellt ist; mit seiner Schaltfläche **Stopp** wird der Teil aus der Warteschlange genommen). Sie ist standardmäßig **aus**. Wählen Sie sie im Reiter *Prompts* des Buches unter den Erweiterungseinstellungen, für einen Ordner mit Büchern oder für alle Ihre Bücher in den Hörbucheinstellungen; die nächstliegende gilt:

- **Nein**,
- **Ja: der Erzähler liest den Teil**,
- **Ja: mit dem Modell des Buches markiert, die Besetzung gibt die Stimmen vor**; ein Buch ohne Modell erhält die einfache Erzählung, und Sie werden darauf hingewiesen.

![Die Einstellung in einem Buch](../../images/plugin-audiobook-auto.png)

Der Teil kommt in Ihre Warteschlange, mit der Besetzung und der Aussprache des Buches. Ein Hinweis teilt Ihnen mit, dass er in der Warteschlange steht, und ein weiterer, dass sein Audio bereit ist oder fehlgeschlagen ist; sie erscheinen nur auf der Seite, die die Erstellung gestartet hat. Der Auftrag steht in jedem Fall im Fenster *Convert to audio*.

<a id="listening"></a>

## Anhören

Ein veröffentlichtes Buch (oder der Leser) mit Teilen, die Audio haben, erhält eine Player-Leiste: Abspielen, Pause, Stopp, die Geschwindigkeit, und der gerade gehörte Teil ist im Text markiert. Er beginnt bei der Auswahl oder beim ersten sichtbaren Teil, spielt die Teile in der Reihenfolge ab und lädt den nächsten, während der aktuelle läuft; die Medientasten des Geräts kennen das Buch. Audio, das aus einer älteren Fassung eines Teils erstellt wurde, wird nicht angeboten. Das Audio wird dem Besitzer des Buches ausgeliefert und, solange das Buch veröffentlicht ist, jedem, der es öffnen kann; ein Buch, dessen Veröffentlichung zurückgenommen wird, wird sofort nicht mehr ausgeliefert (beim nächsten Teil, den der Player lädt).

![Der Player im Leser](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Ein Teil ohne Audio

Der Player hält bei einem Teil ohne Audio an, damit kein Teil der Geschichte übersprungen wird, und sagt dies. Wenn die Erweiterung [Read Aloud](tts-reader.md) installiert ist, zeigt die Leiste außerdem eine Lautsprecher-Schaltfläche **Read this part (and the next ones without audio) with the voice of the browser**. Ohne Ihr Drücken wird nichts mit einer maschinellen Stimme gelesen. Sie liest dann diesen Teil und die folgenden Teile ohne Audio mit der Stimme Ihres Geräts, und wenn sie einen Teil mit Audio erreicht, übernimmt der Player wieder. Spätere Teile ohne Audio in derselben Hörsitzung werden ebenso gelesen, ohne erneut zu fragen; **Stopp** bringt sie zum Schweigen, und ein erneuter Start des Players fragt wieder. Stimme, Geschwindigkeit und Tonhöhe sind die von Read Aloud.
<a id="commands"></a>

## Befehle

Dieselben Dinge wie [Befehle](../commands/index.md) (die Kommandozeile des Geschichtseditors, das Makro `command` einer Vorlage, ein Agent). Teile werden so nummeriert, wie der Dialog Konvertieren sie nummeriert, oder über ihren Namen angesprochen.

| Befehl | Was er tut |
|---|---|
| `/audiobook-status [from [to]]` | Der verwendete Sprachserver, wie das Buch sich selbst konvertiert, wie viele Teile Audio haben (keines, bereit, älter als der Text, älter als das Skript), der Status jedes aufgelisteten Teils im Studio und die Warteschlange. Nur bei einem langen Buch langsam. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Stellt die Teile in die Warteschlange (ohne Argumente das ganze Buch, wenn nur `from` angegeben ist, einen Teil). `labelling` `true` verwendet das Modell des Buches; `what` ist `audio` oder `script` (nur das Skript für das Studio, es wird nichts gesprochen). Mit `wait` `true` bleibt der Befehl aktiv, bis die Aufträge abgeschlossen sind, zeigt den Fortschritt und meldet, wie jeder ausgegangen ist; Abbrechen storniert die Aufträge, die noch nicht begonnen haben. |
| `/audiobook-stop` | Bricht die wartenden Teile ab; der laufende hört nach seinem Stück auf. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` oder `final` im Studio; der Teil braucht ein Skript. |
| `/audiobook-cast` | Der Erzähler, der Ersatz und die Figuren der Besetzung mit ihren Sprechern. |

![Das Ergebnis eines Befehls](../../images/plugin-audiobook-command.png)

Das Audio wird für **dein** Buch erstellt, nicht für die verborgene Arbeitskopie eines [Agenten](../agents.md): Ein Agent kann die Besetzung lesen, erhält aber von den übrigen Befehlen eine Ablehnung mit dem Hinweis, dass du sie selbst ausführen musst. Führe sie über die Kommandozeile aus, oder gib einem Agenten die Aufgabe, das Buch vorzubereiten (annotieren, prüfen) und konvertiere es dann selbst.

<a id="notifications"></a>

## Benachrichtigungen

Wenn ein Stapel von Konvertierungen abgeschlossen ist, kann der Browser dir Bescheid geben ([Benachrichtigungen](../account.md#notifications), damit du den Tab verlassen kannst, während der Sprachserver arbeitet). In der Benachrichtigungsliste der Seite *Einstellungen* gibt es zwei Zeilen: **Convert to audio** (die Stapel, die du im Dialog oder mit dem Befehl startest) und **Convert to audio (automatic)** (ein neuer Teil, der automatisch in Audio umgewandelt wurde). Ein Stapel, den du abgebrochen hast, löst keine Benachrichtigung aus; ein Stapel mit einem fehlgeschlagenen Teil meldet, dass er fehlgeschlagen ist.

![Die Liste der Benachrichtigungen](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Was es noch nicht kann

- Nur Mp3 und Wav (kein Opus); die Lautstärke der Teile ist das, was die Stimmen liefern.
- Keine Soundeffekte oder Umgebungsgeräusche.
- Zwei Sprachengines, Chatterbox und Higgs Audio v3 (in ComfyUI).
- Das Probehören einer Stimme außerhalb eines Segments.
- Die Besetzung ist nicht Teil der Sicherung der *Sprecher* eines Buches: Ein wiederhergestelltes Buch behält seine Besetzung, nennt aber Sprecher, die in deinem Konto vorhanden sein müssen.
