---
label: Audiolibro
order: 27
---

# Audiolibro

Trasforma un libro in un audiolibro, parte per parte, con un server di sintesi vocale: una voce per il narratore e una per ogni personaggio, conservate
insieme alle parti del libro e riprodotte nel lettore. Puoi lasciarlo andare da solo (il modello indica chi parla e come, il server della voce
legge) per una bozza veloce, oppure lavorare sulle parti una alla volta nello **Studio audiolibro**, controllando esattamente cosa viene inviato al
server della voce e modificandolo a mano, ed esportare il risultato come file mp3 con i segnalibri dei capitoli.

L'estensione non genera il parlato da sola: dialoga con un **Chatterbox-TTS-Server** che tu gestisci (vedi
[Configurare il server della voce](audiobook-server.md)). Il testo del libro non viene mai modificato e non viene inviato a nessuno se non ai server
che hai indicato; l'audio viene conservato nella tua installazione, come risorse collegate alle parti.

<a id="what-you-need"></a>

## Cosa ti serve

- Un server della voce e, se condivide la scheda grafica con il tuo modello linguistico, le richieste che le fanno posto e poi la fermano
  ([Configurare il server della voce](audiobook-server.md)).
- Voci: un breve **clip di riferimento** (da 12 a 30 secondi di una sola persona, senza musica) per ogni voce che vuoi clonare, oppure uno dei file di voce
  che il server mette a disposizione. Un narratore ha bisogno di una voce di lettura calma e costante.
- Per un libro con più voci, un modello linguistico in grado di etichettare il testo. Di default è il modello del libro.
- **ffmpeg** è facoltativo. Senza di esso tutto funziona con mp3. Con esso sul server (è incluso nell'immagine Docker; su un desktop viene cercato nel `PATH` o nelle cartelle
  consuete come `/opt/homebrew/bin`; la variabile `MARGINALIA_FFMPEG` ne indica il percorso quando si trova altrove) l'esportazione ottiene il formato **m4b**, e un amministratore può salvare l'audio in wav.

<a id="the-settings"></a>

## Le impostazioni

*Impostazioni → Impostazioni estensioni → Impostazioni Audiolibro* è il pannello del tuo account; salva la pagina *Impostazioni* dopo averlo modificato.

![Il pannello con un backend e gli attori](../../images/plugin-audiobook-settings.png)

<a id="backends"></a>

### Backend

Un backend è un server della voce e il modello con cui parla. Aggiungine uno con **+**.

| Impostazione | Cosa fa |
|---|---|
| Nome | Il nome mostrato nella finestra Converti. |
| Indirizzo | L'indirizzo base del server della voce, o di ciò che lo inoltra, per esempio `http://localhost:8004`. |
| Chiave API | Inviata come token bearer se il server (o il tuo proxy) la richiede. Viene memorizzata cifrata e non viene più mostrata; inseriscine una nuova per sostituirla, oppure **Rimuovi la chiave**. |
| Modello vocale | **Chatterbox Turbo** (inglese, con tag sonori come `[sigh]`, il più veloce e predefinito), **Chatterbox** (inglese, con un'*esagerazione* dell'esecuzione) oppure **Chatterbox Multilingual** (23 lingue). Le richieste prima di un batch devono lasciare questo modello caricato. Un libro che richiede un altro modello usa un altro backend. |
| GPU (nome) | Facoltativo. I server che condividono una GPU, anche quelli del [Generatore di immagini](image-generator.md), ricevono qui lo stesso nome: il loro lavoro attende quindi che gli altri finiscano, e un backend per un altro modello attende che gli altri siano terminati. Vuoto: solo i batch verso questo indirizzo si attendono a vicenda. |
| Il server è su questa macchina o sulla mia rete | Consente indirizzi della rete locale. Lascialo disattivato per un server nel cloud: in tal modo non può essere indotto a chiamare la tua macchina. |
| Timeout di una richiesta | Quanto attendere per un singolo pezzo di parlato. |
| Riavvia il server dopo (pezzi) | Per un server della voce che perde memoria e viene terminato dal sistema in un batch lungo: dopo questo numero di pezzi le richieste successive al batch e poi quelle precedenti vengono rifatte a metà del batch (arresto, avvio, caricamento), e il lavoro prosegue con un server nuovo. 0 (predefinito) non lo fa mai. Un server che si interrompe viene riavviato comunque, qualunque sia questo valore; vedi [Risoluzione dei problemi](audiobook-server.md#troubleshooting). |
| Chiave delle richieste attorno a un batch | La chiave che le richieste prima e dopo un batch inviano come `Authorization: Bearer`. Viene memorizzata cifrata e non viene più mostrata (**Rimuovi la chiave** la cancella), quindi non deve essere scritta negli header di ogni richiesta, dove sarebbe leggibile. Vuoto: si usa la *Chiave API* qui sopra, che è ciò che serve quando un proxy come LiteLLM gestisce tutto. Una richiesta che ha un proprio header `Authorization` lo conserva. |
| Voce del narratore | Il narratore di un libro senza cast. Un file vocale del server si scrive `predefined:Henry.wav`. |
| Predefiniti di un segmento | Temperatura, peso CFG, velocità e dimensione dei blocchi con cui parte un pezzo (lo studio può cambiarli per un solo pezzo). |
| Prima di un batch, Dopo un batch | Le richieste fatte attorno a un batch di parti: *prima* libera la scheda grafica e avvia il server della voce, *dopo* lo arresta. Vedi [Una GPU per il modello e per la voce](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vuoto non fa nulla. |

**Verifica connessione** fa ciò che fa un batch: esegue le richieste prima di un batch (così un server della voce fermo viene avviato), chiede al server se è raggiungibile e se ha un modello caricato,
ed esegue le richieste dopo il batch. Senza richieste si limita a interrogare il server.

Un libro che contiene pezzi in un altro alfabeto (giapponese in un libro inglese) li invia al modello multilingue, se hai un backend per esso, con la loro lingua; restano sul modello del libro se non ne hai uno.

<a id="shared-speech-servers"></a>

### Server della voce condivisi

In un'installazione con più utenti l'amministratore può mantenere server della voce per tutti, così che una macchina con GPU venga configurata una sola volta e nessuno abbia bisogno di un indirizzo o di una chiave. L'amministratore lo fa nello stesso pannello (le ultime sezioni, visibili solo agli amministratori): **Server della voce condivisi** apre una finestra con gli stessi campi di un backend personale, più l'elenco di chi può usarlo: **tutti gli utenti**, oppure gli utenti scelti nell'elenco. Un interruttore qui, **Gli utenti possono aggiungere server della voce propri**, decide se i backend sotto restano consentiti.

Cosa ottieni:

- **Backend in uso** in cima al pannello elenca i server condivisi che puoi usare (contrassegnati *(condiviso)*) e i tuoi. La finestra *Converti in audio* e lo studio usano quello che scegli; anche i comandi lo usano.
- Vedi solo il nome di un server condiviso, mai il suo indirizzo, le chiavi o le richieste. I batch di tutti gli utenti su uno stesso indirizzo di server si attendono a vicenda, così due utenti non possono avviare due volte la stessa GPU.
- Quando l'amministratore ti toglie un server, un lavoro che lo indica ancora viene rifiutato; scegline un altro.
- Quando l'amministratore disattiva i tuoi server, il pannello lo segnala. Quelli che hai aggiunto vengono conservati e tornano utilizzabili se l'interruttore viene riattivato.
- La *Voce del narratore* di un server condiviso dovrebbe essere un file vocale del server (`predefined:Henry.wav`): un clip di riferimento è una risorsa di un solo utente e gli altri non possono leggerlo. Le voci dei tuoi attori funzionano come sempre.

<a id="actors"></a>

### Attori

Un **attore** è una voce tua: un clip di riferimento (una delle tue [risorse](../resources.md) audio, oppure uno nuovo caricato qui) o un
file di voce del server, con voci proprie per un **umore** (un sussurro, un grido) o una **lingua** (un clip che parla
giapponese). La lingua prevale sull'umore, e senza una voce per essi legge la voce dell'attore. I tuoi attori sono conservati
nel tuo account; qualsiasi libro può assegnare un attore al suo narratore e ai suoi personaggi.

**Anteprima** accanto a una voce (nell'attore, nelle impostazioni e nel cast) pronuncia una breve frase con essa (nella lingua della voce, per una lingua) e la riproduce, avviando il server della voce se le richieste prima di un batch lo prevedono. Gli attori sono elencati per nome.

![Un attore con una voce per un umore](../../images/plugin-audiobook-actor.png)

<a id="converting-new-parts-by-itself"></a>

### Conversione automatica delle nuove parti

*Converti automaticamente le nuove parti in audio* è il valore predefinito per tutti i tuoi libri; vedi [Conversione automatica](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Il cast e la pronuncia di un libro

Entrambi sono conservati con il libro (viaggiano nel suo backup) e si modificano dalla finestra di dialogo **Converti in audio**.

**Cast** indica chi legge: un attore per il **narratore**, uno per gli **altri parlanti** (chiunque il modello nomini che il cast non
abbia; vuoto: legge il narratore) e uno per ogni **personaggio**. Un personaggio ha un nome e altri nomi con cui il testo può chiamarlo
(*Tom* per *Tomáš Novák*), separati da virgole; un discorso è attribuito al personaggio quando il suo nome, uno degli altri nomi, oppure una
parola del nome che nessun altro personaggio condivide, corrisponde, ignorando maiuscole e accenti. **Aggiungi i personaggi del libro** riempie
l'elenco con i personaggi del punto di vista e quelli presenti nelle parti.

![Il cast di un libro](../../images/plugin-audiobook-cast.png)

**Pronuncia** è il modo in cui il testo è scritto *per la voce*; il libro stesso non viene mai modificato. Il **dizionario** sostituisce
parole intere (le maiuscole contano) con ciò che viene detto al loro posto: una riscrittura fonetica per un nome o una parola inventata, oppure una
lettura in kana per il giapponese. Per un libro in inglese le regole leggono ad alta voce anche altre cose: numeri, ordinali, anni, orari, denaro e
abbreviazioni come parole, un titolo che è solo un numero come *Chapter four. The Arch* (la parola prima del numero la scegli tu), i segni markdown
come `*` vengono eliminati, e un cambio di scena (`***`, `---`) non viene letto ma produce una pausa più lunga. Disattiva le regole per un libro in
un'altra lingua; il dizionario si applica comunque. **Prova un testo** mostra come verrà letto un pezzo di testo. Un pezzo il cui testo parlato
cambia viene rigenerato, gli altri restano invariati.

![La pronuncia di un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertire un libro

*Menu Strumenti dell'editor della storia (il pulsante con gli ingranaggi) → **Converti in audio***.

![La finestra di dialogo prima di avviare](../../images/plugin-audiobook-convert.png)

1. Scegli il **backend** e premi **Load**: vengono elencate le parti con testo del ramo attivo (la prima è la 1), con ciò che ciascuna ha:
   *Not generated*, *Labelled* (le etichette del modello sono memorizzate, ma non c'è ancora audio), *Generated* (con un pulsante di riproduzione e un download) oppure
   *Generated (the text changed)* (l'audio è stato creato da un testo precedente e viene rifatto); a una parte che lo studio segna come definitiva viene aggiunto *(final)*.
2. Lascia **Label the parts with a language model** disattivato per la versione semplice: il narratore legge ogni paragrafo. Attivalo perché
   il modello decida chi parla in ogni pezzo di dialogo, come (calmo, sussurro, grido...) e dove si inserisce un tag sonoro, e il cast fornisce
   le voci. Il modello del libro è quello scelto; puoi sceglierne un altro se preferisci. Le parti vengono prima etichettate, poi lette ad alta voce. Una parte che ha già etichette per il suo testo viene letta anche con queste quando la casella è disattivata (il modello non viene interrogato), quindi rieseguire un libro etichettato senza la casella non trasforma i suoi dialoghi in semplice narrazione; **Remove labels** serve per chiedere una narrazione semplice o nuove etichette.
3. Spunta le parti (la casella nell'intestazione le spunta tutte) e **plan** ciò che vuoi farne; la colonna **Plan** lo mostra e non viene ancora fatto nulla:
   **Generate** (crea ciò che manca; una parte aggiornata non viene rifatta), **Regenerate** (elimina l'audio, rimuove le etichette, genera), 
   **Delete audio** (i pezzi e l'audio unito della parte; le etichette restano), **Remove labels** (il modello viene interrogato di nuovo la prossima volta che la parte viene creata), 
   **Clear the plan**. I passaggi si sommano su una parte (Delete audio e Remove labels senza Generate fanno solo pulizia); una parte segnata come definitiva viene esclusa da ogni piano.
4. **Start** esegue il piano. Quando elimina audio o etichette, chiede prima conferma. Ciò che deve essere generato entra nella tua coda, che prosegue quando chiudi la finestra
   e quando lasci la pagina; una parte in attesa o in esecuzione nella coda viene esclusa da un piano. Lo stato, i pezzi già creati e il messaggio del job sono mostrati nella riga della parte.
   Mentre il modello etichetta una parte, la sua riga mostra *Labelling pieces 17-32 of 120* (al modello vengono chiesti 16 pezzi alla volta); **clicca sullo stato** di una parte già etichettata per leggere, per ogni richiesta, il
   ragionamento del modello e la sua risposta, man mano che arrivano. Vengono conservati in memoria insieme al job, finché i job terminati non vengono cancellati (*Clear finished*) o l'applicazione non si riavvia.

![La coda: pezzi già creati, e un download quando una parte è completa](../../images/plugin-audiobook-queue.png)

Come viene creata una parte, così sai cosa aspettarti:

- Il testo è **diviso dal programma**, mai dal modello, in pezzi di narrazione e pezzi di dialogo diretto (tra virgolette doppie dritte o curve o parentesi giapponesi a angolo; un titolo è narrazione). Il modello si limita a *etichettare* i pezzi numerati. Un testo con tag sonori viene usato solo quando è il pezzo con i tag aggiunti e nient'altro è cambiato, così il modello non può modificare il tuo libro.
- **Chi parla in un pezzo è deciso dal modello per il dialogo; la narrazione è affidata al narratore.** Una lettera, un biglietto, un messaggio o i pensieri di un personaggio non hanno virgolette, quindi il programma li considera narrazione. Un pezzo del genere viene letto dal personaggio (la voce del cast) solo quando **inizia in corsivo** (`*...*` o `_..._`) e il modello indica il personaggio dal contesto (una firma, "la calligrafia era inconfondibile"). La narrazione in terza persona, anche quando è raccontata dal punto di vista di un personaggio ("Lei guardò Percy attraversare la stanza"), è sempre del narratore: il programma ignora un personaggio che il modello assegna a un pezzo senza corsivo. Il corsivo usato per enfasi, suoni o parole straniere è anch'esso narrazione. Le parole sono sempre le tue.
- **L'istruzione al modello è un template che puoi modificare.** *Impostazioni → Impostazioni dell'estensione → Audiobook Settings → Labelling prompt* è il tuo valore predefinito; un libro (scheda *Prompts* → *Impostazioni dell'estensione* → *Audiobook*) oppure una cartella possono averne uno proprio. Vuoto significa quello della cartella superiore, poi il tuo, poi quello integrato. Si scrive come gli altri [prompt](../books/prompts.md): le variabili sono `{{characters}}`, `{{deliveries}}`, `{{tags}}` e `{{wordsPerTag}}`, e il popover dei suggerimenti ha **Inserisci il template predefinito**. Un'istruzione modificata fa sì che le etichette delle parti da creare vengano richieste di nuovo la prossima volta che vengono create.
- **Il volume delle parti viene livellato** quando è installato ffmpeg del programma (lo stesso che crea l'`.m4b`): l'audio unito di ogni parte viene portato a un unico livello di volume (EBU R128, lineare, così la dinamica resta; **-18 LUFS** salvo modifica in *Impostazioni → Impostazioni dell'estensione → Audiobook Settings*, dove puoi anche disattivarlo). I pezzi restano come il server li ha creati; viene livellata solo la parte unita, così un audiobook non cambia volume di colpo da una parte all'altra. **Le parti create in precedenza** non vengono toccate finché non le converti di nuovo: così vengono riunite dai pezzi memorizzati e livellate, senza chiedere nulla al server vocale (nemmeno lo scaricamento del modello linguistico). Disattivare il livellamento lascia invariate le parti già livellate. Se ffmpeg fallisce per una parte, questa conserva il suo audio com'era e il risultato lo segnala.
- Tutte le parti di un singolo clic vengono create in **un unico batch per modello vocale**: le richieste prima del batch vengono eseguite una volta, poi vengono lette tutti i pezzi, poi vengono eseguite una volta le richieste dopo il batch. È questo che evita che la scheda grafica cambi modello a ogni parte.
- Ogni pezzo viene **conservato in base al suo contenuto** (testo, voce, modello, lingua, impostazioni, seme). Una parte già aggiornata non costa nulla, un paragrafo modificato è l'unico pezzo rifatto, e una parte fallita conserva i pezzi già creati quando premi **Retry**.
- I pezzi vengono uniti con pause che dipendono dalla loro posizione (dopo un titolo, tra i paragrafi, prima di *he said*) in un unico mp3 per parte, conservato insieme alla parte.
- Mentre è in corso una tua generazione, la sintesi vocale attende, così il modello linguistico non viene scaricato sotto di essa.
- Una parte che hai segnato come **definitiva** nello studio viene lasciata invariata, e la finestra di dialogo dice quante sono.
- **L'intero audiobook** unisce ogni parte con audio aggiornato in un unico mp3 con un capitolo per ogni parte e mostra un link di download sotto l'elenco (vengono indicate le parti escluse). Lo stesso file è il formato di esportazione *un mp3 con segnalibri di capitolo*.
- **Stop all** annulla le parti in attesa e interrompe subito quella in esecuzione: la richiesta al modello linguistico (l'etichettatura) o al server vocale (il pezzo in lettura) viene abbandonata, senza attendere. Un batch fermato prima di iniziare non invia le richieste che lo precedono, così il modello linguistico resta caricato. **Download** scarica l'mp3 di una parte completata.
<a id="the-audiobook-studio"></a>

## Lo studio dell'audiolibro

*Menu Strumenti → **Audiobook studio*** (la voce del pezzo selezionato, con un'anteprima, si trova accanto al suo altoparlante) è dove si realizza l'audiolibro finale, una parte alla volta: si vede il copione, lo si modifica, si ascolta un pezzo e si realizza la parte.

![Lo studio con un discorso selezionato](../../images/plugin-audiobook-studio.png)

- Mentre la parte viene annotata o realizzata, una barra indica che è in corso un'operazione e i pulsanti che avvierebbero altro lavoro restano in attesa.
- **Annota** crea il copione della parte (con il modello linguistico, se lo richiedi, altrimenti con il narratore) e mostra i pezzi in una tabella: parlante, modo di esposizione, intensità, il testo parlato e un segno per un pezzo che hai modificato. Mentre è in esecuzione compare accanto un pulsante **Interrompi**, che termina la richiesta al modello linguistico.
- Seleziona un pezzo per modificarne il **parlante**, il **testo parlato**, il **modo di esposizione**, l'**intensità** (l'enfasi della voce, per i modelli che la prevedono), la **lingua**, il **seed**, la **pausa dopo**. **Applica** mantiene la modifica e **blocca** il pezzo: una nuova annotazione lascia un pezzo bloccato così com'è.
  **Sblocca** lo restituisce all'annotazione. *La richiesta che riceve il server vocale* mostra la richiesta esatta.
- **Ascolta** crea l'audio di un pezzo (solo quel pezzo; il server vocale viene avviato per questo); **Un'altra prova** lo rigenera con un nuovo seed, e le **prove** vengono conservate così puoi scegliere quella che preferisci (*Usa questa prova e ascoltala*). Una buona voce dipende spesso dal seed.
- **Realizza la parte da questo copione** unisce i pezzi così come sono, senza una nuova annotazione.
- Lo **stato** della parte è *bozza*, *rivista* o *finale*. Una parte finale non viene più annotata né convertita di nuovo, e l'esportazione può essere limitata alle parti finali.

<a id="exporting"></a>

## Esportazione

L'audiolibro è un formato di *Esporta la storia* del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** e, quando ffmpeg è installato, **Audiobook (one m4b with chapters)**. Sono costruiti con l'audio delle parti aggiornate: una parte che non ha audio, o con audio più vecchio del suo testo o del suo copione nello studio, viene esclusa e la finestra indica quali. I file mp3 contengono i tag ID3 (il titolo del libro, il capitolo, la traccia), e il file unico ha i segnalibri di capitolo che lettori come VLC e la maggior parte delle app per audiolibri mostrano; lo zip contiene una playlist. La casella **Audiobook: only the parts marked final** limita l'esportazione alle parti che hai completato nello studio.

L'**m4b** è il formato che Apple Books e la maggior parte delle app per audiolibri sul telefono richiedono: audio AAC (64 kbit/s, sufficiente per il parlato) in un contenitore MP4 con i capitoli, il titolo, l'autore e il genere. A produrlo è ffmpeg, quindi il formato è offerto solo su un server che ha ffmpeg. L'audio memorizzato come mp3 viene codificato una seconda volta per questo; l'audio memorizzato come wav viene codificato una sola volta.

<a id="how-the-audio-is-stored"></a>

### Come viene memorizzato l'audio

Un amministratore lo sceglie per l'intera installazione in *Settings → Extension Settings → Audiobook Settings* (l'ultima sezione, visibile solo agli amministratori):

- **mp3** (predefinito): piccolo, unito senza bisogno di un decodificatore, non richiede ffmpeg. L'm4b è un secondo passaggio con perdita.
- **wav**: senza perdita, circa dieci volte più grande (un'ora di parlato occupa circa 170 MB), richiede ffmpeg. I pezzi vengono richiesti al server vocale in formato wav e uniti come wav; il browser li riproduce così come sono;
  le esportazioni mp3 e zip vengono codificate da ffmpeg (96 kbit/s) e l'm4b viene codificato una sola volta.

La scelta si applica a ciò che viene realizzato da quel momento in poi. Le parti realizzate prima conservano il loro audio e vengono interpretate così come sono; una parte di entrambi i tipi (un pezzo bloccato dello studio di prima) viene unita come wav, e un'esportazione di parti di entrambi i tipi funziona, ma richiede ffmpeg.

![La finestra di esportazione con i formati dell'audiolibro](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversione automatica

Un libro può creare da sé l'audio di una nuova parte, non appena la parte è finale: dopo l'annotazione automatica, la riscrittura e le traduzioni, se il libro le ha, come ultimo passo di [ciò che segue una parte generata](../books/story-editor.md#what-follows-a-generated-part)
(nel pannello delle esecuzioni finché l'audio non è pronto; il suo **Interrompi** toglie la parte dalla coda). È **disattivata** per impostazione predefinita. Sceglila nella scheda *Prompt* del libro, nelle impostazioni dell'estensione, per una cartella di libri, o per tutti i tuoi libri nelle impostazioni dell'audiolibro; vince quella più vicina:

- **No**,
- **Sì: il narratore legge la parte**,
- **Sì: con etichette assegnate dal modello del libro, il cast fornisce le voci**; un libro senza modello ottiene la narrazione semplice, e te lo viene comunicato.

![L'impostazione in un libro](../../images/plugin-audiobook-auto.png)

La parte entra nella tua coda, con il cast e la pronuncia del libro. Un avviso ti dice che è in coda e un altro che il suo audio è pronto o che è fallito; compaiono solo nella pagina che ha avviato la generazione, mentre il lavoro si trova in ogni caso nella finestra *Convert to audio*.

<a id="listening"></a>

## Ascolto

Un libro pubblicato (o il lettore) che ha parti con audio ottiene una barra del lettore: riproduzione, pausa, stop, la velocità, la parte in ascolto evidenziata nel testo. Parte dalla selezione o dalla prima parte visibile, riproduce le parti in ordine e carica la successiva mentre quella corrente suona, e i controlli multimediali del dispositivo riconoscono il libro. L'audio creato da una versione più vecchia del testo di una parte non viene proposto. L'audio è servito al proprietario del libro e, finché il libro è pubblicato, a chiunque possa aprirlo; un libro ritirato dalla pubblicazione smette di essere servito subito (alla parte successiva, quando il lettore si carica).

![Il lettore nel reader](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte senza audio

Il lettore si ferma su una parte senza audio, perché una parte della storia non venga saltata, e lo segnala. Quando è installata l'estensione [Read Aloud](tts-reader.md), la barra mostra anche un pulsante con l'altoparlante, **Read this part (and the next ones without audio) with the voice of the browser**. Non si legge nulla con una voce artificiale se non lo premi. Legge quella parte e le parti successive senza audio con la voce del tuo dispositivo, e quando arriva a una parte con audio il lettore riprende il controllo. Le parti successive senza audio nello stesso ascolto vengono lette allo stesso modo senza chiedere di nuovo; **Interrompi** la silenzia, e riavviare il lettore chiede di nuovo. La voce, la velocità e il tono sono quelli di Read Aloud.

<a id="commands"></a>

## Comandi

Le stesse operazioni dei [comandi](../commands/index.md) (la riga di comando dell'editor della storia, la macro `command` di un modello, un agente). Le parti sono numerate come le numera la finestra di conversione, oppure indicate con il loro identificativo.

| Comando | Cosa fa |
|---|---|
| `/audiobook-status [from [to]]` | Il server vocale in uso, il modo in cui il libro si converte da solo, quante parti hanno audio (nessuno, pronto, più vecchio del testo, più vecchio del copione), lo stato di ogni parte elencata nello studio, e la coda. Lento solo quando il libro è lungo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Mette le parti in coda (l'intero libro senza argomenti, una sola parte quando è indicato solo `from`). `labelling` `true` usa il modello del libro; `what` è `audio` o `script` (solo il copione per lo studio, nulla viene letto ad alta voce). Con `wait` `true` il comando resta attivo finché i lavori non sono terminati, mostra a che punto sono e dice come è finito ciascuno; interromperlo annulla i lavori non ancora avviati. |
| `/audiobook-stop` | Annulla le parti in attesa; quella in esecuzione si ferma dopo il suo pezzo. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` nello studio; la parte deve avere un copione. |
| `/audiobook-cast` | Il narratore, il ripiego e i personaggi del cast con i loro attori. |

![Il risultato di un comando](../../images/plugin-audiobook-command.png)

L'audio viene creato per **il tuo** libro, non per la copia di lavoro nascosta di un [agente](../agents.md): un agente può leggere il cast, e riceve un rifiuto dagli altri comandi, con il messaggio che devi eseguirli tu. Esegui questi comandi dalla riga di comando, oppure affida a un agente il compito di preparare il libro (annotare, rivedere) e convertilo tu stesso.
<a id="notifications"></a>

## Notifiche

Quando un gruppo di conversioni è terminato, il browser può avvisarti ([notifiche](../account.md#notifications), così puoi lasciare la scheda aperta mentre il server vocale lavora). Nell'elenco delle notifiche della pagina *Impostazioni* ci sono due voci: **Convert to audio** (i gruppi che avvii nella finestra di dialogo o con il comando) e **Convert to audio (automatic)** (una nuova parte trasformata in audio automaticamente). Un gruppo che hai interrotto non genera notifica; un gruppo con una parte non riuscita segnala che è fallito.

![L'elenco delle notifiche](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Cosa non fa (per ora)

- Solo mp3 e wav (niente opus); il volume dei singoli pezzi è quello che restituiscono le voci.
- Nessun effetto sonoro né ambientazione sonora.
- Un solo motore vocale, Chatterbox. Gli altri motori sono il prossimo passo.
- L'ascolto di una voce al di fuori di un segmento.
- Il cast non fa parte del backup di un libro dei tuoi *attori*: un libro ripristinato mantiene il suo cast, ma i nomi degli attori devono esistere nel tuo account.
