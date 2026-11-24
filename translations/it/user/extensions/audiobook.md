---
label: Audiolibro
order: 27
---

# Audiolibro

Trasforma un libro in un audiolibro, parte per parte, con un server di sintesi vocale: una voce per il narratore e una per ogni personaggio, conservate insieme alle parti del libro e riprodotte nel lettore. Puoi lasciarlo girare (il modello indica chi parla e come, il server di sintesi legge) per ottenere una bozza veloce, oppure lavorare sulle parti una alla volta nello **Audiobook studio**, controllando esattamente cosa viene inviato al server di sintesi e modificandolo a mano, ed esportare il risultato come file mp3 con i segni di capitolo.

L'estensione non produce il parlato da sola: si collega a un **Chatterbox-TTS-Server** che gestisci tu (vedi [Configurare il server di sintesi](audiobook-server.md)). Il testo del libro non viene mai modificato e non viene inviato a nessuno, tranne ai server che hai indicato; l'audio è conservato nella tua installazione, come risorse collegate alle parti.

<a id="what-you-need"></a>

## Cosa ti serve

- Un server di sintesi e, se condivide la scheda grafica con il tuo modello linguistico, le richieste che gli fanno spazio e lo fermano di nuovo ([Configurare il server di sintesi](audiobook-server.md)).
- Voci: un breve **clip di riferimento** (da 12 a 30 secondi di una sola persona, senza musica) per ogni voce che vuoi clonare, oppure uno dei file vocali che il server mette a disposizione. Un narratore ha bisogno di una voce calma e costante nella lettura.
- Per un libro con più voci, un modello linguistico capace di etichettare il testo. Per impostazione predefinita è il modello del libro.
- **ffmpeg** è facoltativo. Senza di esso tutto funziona con mp3. Con ffmpeg installato sul server (è incluso nell'immagine Docker; su un desktop viene cercato in `PATH` o nelle cartelle abituali come `/opt/homebrew/bin`; la variabile `MARGINALIA_FFMPEG` indica il percorso se si trova altrove), l'esportazione ottiene il formato **m4b**, e un amministratore può salvare l'audio in formato wav.

<a id="the-settings"></a>

## Le impostazioni

*Settings → Extension Settings → Audiobook Settings* è il pannello del tuo account; salva la pagina *Settings* dopo averlo modificato.

![Il pannello con un backend e gli attori](../../images/plugin-audiobook-settings.png)

<a id="backends"></a>

### Backend

Un backend è un server di sintesi e il modello con cui parla. Aggiungine uno con **+**. **Duplicate** copia quello selezionato (indirizzo, chiavi, blocco GPU, voce, impostazioni predefinite e le richieste prima e dopo un batch) con un nuovo nome: è il modo rapido per avere un backend di un altro modello sulla stessa macchina, per esempio Higgs accanto a Chatterbox, dove poi cambi il modello, l'indirizzo e le richieste di avvio e arresto.

| Impostazione | Cosa fa |
|---|---|
| Name | Il nome mostrato nella finestra di conversione. |
| Address | L'indirizzo base del server di sintesi, o di ciò che lo inoltra a lui, per esempio `http://localhost:8004`. |
| Api key | Inviata come bearer token se il server (o il tuo proxy) la richiede. È memorizzata in forma cifrata e non viene più mostrata; scrivine una nuova per sostituirla, oppure usa **Remove the key**. |
| Speech model | **Chatterbox Turbo** (inglese, con tag sonori come `[sigh]`, il più veloce e quello predefinito), **Chatterbox** (inglese, con un'*exaggeration* dell'espressione), **Chatterbox Multilingual** (23 lingue) o **Higgs Audio v3 in ComfyUI** (molte lingue, voci che rispecchiano lo stato d'animo di una riga e suoni come pianto, affanno o un gemito; vedi [più sotto](#higgs-audio-v3-emotions-and-sounds)). Il [blocco GPU](../administration/gpus.md) del backend (o le richieste prima di un batch) deve lasciare questo modello caricato. Un libro che richiede un altro modello usa un altro backend. |
| GPU lock | Il [blocco GPU](../administration/gpus.md) della GPU su cui gira il server, quando la condivide con il modello linguistico o con altri server (anche il [Generatore di immagini](image-generator.md)): ogni batch e ogni campione attende il proprio turno sulla GPU, e il blocco carica per primo il server di sintesi. Compare solo se l'amministratore ha configurato i blocchi GPU. |
| The server is on this machine or on my network | Consente gli indirizzi della rete locale. Lascialo disattivato per un server nel cloud: in quel caso non può essere indotto a chiamare la tua macchina. |
| Timeout of a request | Quanto attendere per un singolo pezzo di parlato. |
| Restart the server after (pieces) | Per un server di sintesi che perde memoria e viene terminato dal sistema in un batch lungo: dopo questo numero di pezzi, le richieste successive al batch e poi quelle precedenti vengono rifatte a metà del batch (arresto, avvio, caricamento), e il lavoro prosegue con un server nuovo. 0 (il valore predefinito) non lo fa mai. Un server che si blocca viene riavviato comunque, qualunque cosa indichi questa impostazione; vedi [Risoluzione dei problemi](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | La chiave che le richieste prima e dopo un batch inviano come `Authorization: Bearer`. È memorizzata in forma cifrata e non viene più mostrata (**Remove the key** la cancella), così non deve essere scritta nelle intestazioni di ogni richiesta, dove sarebbe leggibile. Se è vuota, viene usata la *Api key* sopra, che è ciò che vuoi quando un unico proxy, come LiteLLM, serve tutto. Una richiesta che ha già un'intestazione `Authorization` propria la mantiene. |
| Narrator voice | Il narratore di un libro senza cast. Un file vocale del server si scrive `predefined:Henry.wav`. |
| Precision | Solo per Higgs: la precisione con cui il modello esegue i calcoli. **fp32** (il predefinito) è la scelta sicura: su una scheda AMD **bf16** ha reso i pezzi incomprensibili, con effetti sonori e voci sbagliate. bf16 richiede circa la metà della memoria grafica (circa 8 GB invece di 16) ed è più veloce dove funziona. |
| Send the text of the clip to Higgs | Solo per Higgs, sperimentale: il [testo del clip](#voices-and-clips) viene inviato insieme all'audio. Lascialo disattivato: nei test ha cambiato la voce. |
| Check the length of the pieces | Attivo per impostazione predefinita. Un pezzo molto più lungo del suo testo (4 s più 0,6 s per parola: il modello ha continuato a parlare, o ha prodotto un suono invece delle parole) viene rifatto con il seed successivo, fino a **Retries** volte; si conserva la prima versione di lunghezza corretta, altrimenti la più corta. |
| Check the words with speech recognition | Disattivato per impostazione predefinita; richiede il [riconoscimento vocale](#speech-recognition). Dopo ogni batch, ogni pezzo viene trascritto e confrontato con il suo testo. Un pezzo che si discosta di più della percentuale **Max. word errors (%)** delle sue parole viene rifatto con il seed successivo e ricontrollato, fino a **Retries** tentativi; si conserva la versione con meno parole sbagliate. La conversione indica quanti pezzi sono stati rifatti e quanti risultano ancora diversi. |
| Defaults of a segment | Temperatura, peso CFG, velocità e dimensione dei blocchi con cui inizia un pezzo (lo studio può modificarli per un singolo pezzo). Higgs usa solo la temperatura. |
| Before a batch, After a batch | Richieste fatte prima e dopo ogni batch di parti (e di ogni campione), all'interno del blocco GPU. Liberare la scheda grafica e avviare il server di sintesi è compito del blocco GPU; vedi [Una GPU per il modello e per il parlato](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Se è vuoto, non fa nulla. |

**Test connection** esegue ciò che fa un batch: prende il blocco GPU (così un server di sintesi fermo viene avviato), esegue le richieste prima di un batch, chiede al server se è raggiungibile e se ha un modello caricato, ed esegue le richieste dopo il batch. Senza blocco o richieste, si limita a fare la domanda.

Un libro che ha pezzi in un'altra scrittura (giapponese in un libro inglese) li invia al modello multilingue, se hai un backend per esso, con la loro lingua; restano sul modello del libro se non ne hai uno. Un libro Higgs li conserva: Higgs li legge da sé, con la stessa voce.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: emozioni e suoni

Con il backend **Higgs Audio v3 in ComfyUI** si sentono le etichette del modello linguistico. L'indirizzo del backend è quello del tuo ComfyUI
(con i nodi di TTS Audio Suite, vedi [Configurazione del server vocale](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- L'**interpretazione** di un pezzo di parlato è recitata: triste, arrabbiato, gioioso, divertito, teso, spaventato, sorpreso, tenero, sarcastico, implorante, disperato,
  disgustato, eccitato, un sussurro o un grido. Un'interpretazione forte (intensità 0.8 e oltre) è più espressiva, una debole (sotto 0.2) viene letta in modo semplice.
  Il narratore legge in modo semplice.
- Il modello linguistico può inserire più **suoni** nel parlato rispetto a Chatterbox Turbo: risata, risatina, ridacchio, sospiro, ansito, tosse, schiarimento di gola,
  tirata su col naso, starnuto, gemito, pianto, singhiozzo, urlo, affanno, lamento, canticchio e shh.
- Il programma legge anche la **narrazione attorno a ogni parlato** (in inglese) e dà al parlato il suono che essa nomina, dove il modello non ne ha messo nessuno: *"You look funny," she giggled.* riceve una risatina, *She burst out laughing. "Careful!"* una risata, *a soft moan escaped her lips. "Yes..."* un lamento, *panting* o *out of breath* un affanno, *cried quietly* o *burst into tears* un pianto. Lascia fuori ciò che solo sembra un suono: *"Stop!" she cried.*
  (un'esclamazione), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, e un suono che la narrazione attribuisce a un altro personaggio con nome. *"You look funny," he mocked her.* riceve un'interpretazione sarcastica o divertita e nessun suono. La narrazione stessa viene letta come scritta.
- **Il narratore viene sempre letto in modo semplice**: nessun suono e nessun umore, anche per un parlato che il modello ha lasciato al narratore.
- Higgs non ha bisogno di una clip per ogni stato d'animo: basta una sola clip di una voce. Copia la registrazione della clip, quindi una clip pulita conta più che con Chatterbox
  ([Una buona clip](#a-good-clip)).
- Ogni pezzo viene prodotto da ComfyUI e la sua memoria viene svuotata dopo ciascuno (qualche secondo per pezzo), il che evita che alcune schede grafiche si blocchino.
- Higgs spesso continua dopo l'ultima parola di un pezzo (un respiro, l'inizio di un altro suono) e la clip finisce nel mezzo di quel suono, che si sente come un breve "whoosh" prima della pausa. Con **ffmpeg** l'estensione taglia quella coda e sfuma ogni pezzo; senza, i pezzi restano come li ha prodotti Higgs.
- Il modello Higgs Audio v3 è distribuito con una **licenza di ricerca e non commerciale**: un audiolibro che vendi richiede una licenza da Boson AI.

<a id="speech-recognition"></a>

### Riconoscimento vocale

**Riconoscimento vocale** trascrive ciò che dice un pezzo di parlato. Controlla i pezzi di un batch (quando un backend ha **Check the words with speech
recognition**) e riconosce il [testo di una clip vocale](#voices-and-clips). Scegli dove viene eseguito:

| Impostazione | Cosa fa |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)**: il modello vocale Granite della TTS Audio Suite nel tuo ComfyUI (l'indirizzo è quello di ComfyUI). **OpenAI-compatible (whisper)**: qualsiasi server con `/v1/audio/transcriptions`, come un server whisper (faster-whisper, speaches) o le API di OpenAI. |
| Address, Api key | L'indirizzo di ComfyUI, o la base dell'API (con o senza `/v1`); la chiave è memorizzata cifrata. Indirizzo vuoto: nessun riconoscimento vocale. |
| GPU lock | Il [lock GPU](../administration/gpus.md) che prende il riconoscimento. Su una sola scheda con Higgs in fp32, dagli un lock tutto suo i cui passi riavviano ComfyUI: ComfyUI tiene il modello vocale nella memoria del server, e caricare il riconoscimento accanto ha esaurito la memoria del server. |
| Model of the recognition | Vuoto: `granite-speech-4.1-2b` per ComfyUI, `whisper-1` per quello compatibile con OpenAI. |
| Timeout, The server is on this machine or on my network | Come per un backend. |

**Test connection** verifica che ComfyUI abbia i nodi, oppure che il server risponda. I pezzi di un batch vengono controllati dopo il batch, quando il server vocale viene restituito, così il riconoscimento e il modello vocale non hanno mai bisogno della scheda nello stesso momento.

<a id="cleanup-of-clips"></a>

### Pulizia delle clip

**Cleanup of clips** pulisce una clip di riferimento su un ComfyUI con la [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (la stessa usata per i lavori Higgs). Il risultato è una nuova **versione** della clip in [Voci e clip](#voices-and-clips); la clip stessa resta.

| Impostazione | Cosa fa |
|---|---|
| Address, Api key | L'indirizzo di ComfyUI; la chiave è memorizzata cifrata. Indirizzo vuoto: nessuna pulizia. |
| GPU lock | Il [lock GPU](../administration/gpus.md) che prende la pulizia. Dagli un lock tutto suo, non quello del parlato o del riconoscimento vocale, i cui passi riavviano ComfyUI, così i modelli di pulizia non vengono mai caricati accanto al modello vocale. |
| Timeout, The server is on this machine or on my network | Come per un backend (il timeout è per una singola pulizia, 10 minuti per impostazione predefinita: ComfyUI scarica un modello la prima volta che lo usa). |
| Cleanup | Le pulizie (preset) tra cui scegliere in Voci e clip, per nome. Ognuna è un elenco di passi eseguiti uno dopo l'altro. **Default cleanups** ripristina quelle che include. |

Un passo è uno dei seguenti:

- **Separation model**: un modello della suite che divide l'audio in due, la parte *extracted* e la parte *remaining*, e ne tiene una. Quale parte contiene la voce dipende dal modello: un modello per le voci (`MelBandRoformer`) estrae la voce, un modello di denoise o de-reverb estrae il rumore o la stanza, quindi tieni la parte *remaining*. **Aggressiveness** (da 0 a 20) decide quanto viene tolto.
- **Voice fixer**: ripristina una voce degradata (un telefono, una vecchia registrazione). Cambia il suono della voce più di quanto faccia una separazione.
- **ffmpeg (here)**: il filtro di **Clean with ffmpeg**, eseguito dall'applicazione tra i passi su ComfyUI.
- **Normalize loudness (here)**: alza (o abbassa) il livello della clip fino al volume che indichi (-18 LUFS, come per l'audiolibro, per impostazione predefinita), con ffmpeg nell'applicazione. Cambia solo il livello; i picchi restano sotto il clipping. Mettilo per ultimo, dopo i passi che tolgono qualcosa (lasciano la voce più bassa). Una pulizia di solo questo passo (e del filtro ffmpeg) non usa ComfyUI né il suo lock GPU.

Le pulizie incluse sono: *Voice from music* (la voce da un film o da una canzone), *Remove noise*, *Remove echo* (la stanza e l'eco), *Raise a quiet voice* (il solo passo di volume), *Restore the voice* e *Film or game clip* (la voce tolta dalla musica, poi la stanza, poi il rumore: rimuovere prima il rumore faceva emergere la stanza di una clip di film). *Remove echo* richiede una correzione in alcune configurazioni di ComfyUI, vedi [il server vocale](audiobook-server.md#higgs-audio-v3-in-comfyui). **Test connection** verifica che ComfyUI abbia i nodi e riempie l'elenco dei modelli con quelli del tuo server. I modelli vengono scaricati da ComfyUI la prima volta che una pulizia li usa.

<a id="shared-speech-servers"></a>

### Server vocali condivisi

Su un'installazione con più utenti l'amministratore può tenere dei server vocali per tutti, così che una macchina con GPU venga configurata una sola volta e nessuno abbia bisogno di un indirizzo o di una chiave. L'amministratore lo fa nello stesso pannello (le ultime sezioni, viste solo dagli amministratori): **Shared speech servers** apre una finestra con gli stessi campi di un backend proprio, più l'indicazione di chi può usarlo: **all users**, oppure gli utenti scelti nell'elenco. Un interruttore lì, **Users may add speech servers of their own**, decide se i backend sotto sono ancora consentiti.

Cosa ottieni:

- **Backend in use** in cima al pannello elenca i server condivisi che puoi usare (contrassegnati *(shared)*) e i tuoi. La finestra *Convert to audio* e lo studio usano quello che scegli; anche i comandi lo usano.
- Vedi solo il nome di un server condiviso, mai il suo indirizzo, le chiavi o le richieste. I batch di tutti gli utenti su un indirizzo di server attendono l'uno l'altro, così due utenti non possono avviare la stessa GPU due volte.
- Quando l'amministratore ti toglie un server, un lavoro che lo nomina ancora viene rifiutato; scegline un altro.
- Quando l'amministratore disattiva i tuoi server, il pannello lo segnala. Quelli che hai aggiunto vengono conservati e tornano in uso se l'interruttore viene riattivato.
- La *Narrator voice* di un server condiviso dovrebbe essere un file vocale del server (`predefined:Henry.wav`): una clip di riferimento è una risorsa di un solo utente e non può essere letta dagli altri. Le voci dei tuoi attori funzionano come sempre.
<a id="actors"></a>

### Attori

Un **attore** è una tua voce: una clip di riferimento (una delle tue [risorse](../resources.md) audio, oppure una nuova caricata qui) o un
file vocale del server, con voci proprie per un **stato d'animo** (un sussurro, un grido) o una **lingua** (una clip che parla
giapponese). La lingua prevale sullo stato d'animo e, senza una voce per questi, legge la voce dell'attore. I tuoi attori sono conservati
nel tuo account; ogni libro può assegnare un attore al proprio narratore e ai propri personaggi.

Un attore può avere una **etichetta**, una breve nota tua: il personaggio che interpreta, un libro, *profondo e antico*, un tag qualsiasi che ti aiuti
a scegliere la voce giusta. I selettori di un attore (le impostazioni, il cast) la mostrano sotto il nome, e *Voci e clip* sotto il nome nell'elenco
degli attori. Nel cast, scrivi in una casella dell'attore per trovarlo con una parte del nome o dell'etichetta.

**Anteprima** accanto a una voce (nell'attore, nelle impostazioni e nel cast) fa pronunciare con essa alcune frasi (una breve nella lingua della voce per una lingua) e la riproduce; quando la GPU è occupata da altro, il blocco GPU carica prima il server di sintesi vocale. La freccia accanto offre altri campioni: **con suoni** (un sospiro, una risata, un respiro affannato) per i modelli che li doppiano (Chatterbox Turbo, Higgs Audio v3), e per Higgs ogni **stato d'animo** dell'etichettatura (triste, sussurrato, sarcastico...). Quando la voce è una clip, il pulsante con il file audio riproduce la **clip di riferimento** così come è memorizzata, subito (senza server di sintesi e senza GPU). Gli attori sono elencati per nome.

Ogni campione occupa la GPU solo per sé. La GPU resta caricata con il server di sintesi dopo un campione, così i campioni successivi partono subito, finché una richiesta del modello linguistico (o di un altro server) non ha bisogno della GPU.

![Un attore con una voce per uno stato d'animo](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Voci e clip

**Voci e clip** (accanto agli attori) è un'unica finestra per i tuoi attori e per le clip da cui le loro voci sono clonate. Ha tre schede, inizialmente affiancate: **Attori**, **Clip** e **Clip**. Trascina una scheda per metterla accanto, sotto o insieme a un'altra; la finestra si riapre come l'hai lasciata.

- **Attori**: ciascuno con la sua voce, quante altre voci (stati d'animo, lingue) ha e la qualità peggiore delle sue clip; aggiungi, modifica o elimina un attore.
  Il mirino di un attore la cui voce è una clip seleziona quella clip in **Clip**, aprendo le cartelle che la contengono.
- **Clip**: le tue clip in **cartelle** (per esempio una per personaggio), con sotto le **versioni** create da una clip, ciascuna con la sua durata, la sua **qualità** e dove è usata (attori, con lo stato d'animo o la lingua, e i narratori dei tuoi backend). **Aggiungi una clip** ne carica una.
  - Le cartelle partono chiuse; ciò che apri resta aperto finché la finestra è aperta, anche dopo una modifica di una clip.
  - Seleziona le clip (le cartelle non hanno casella) per lavorarci in più di una volta. **Unisci** crea una nuova clip dalle clip selezionate, una dopo l'altra, nell'ordine dell'elenco, senza pausa tra di esse: un riferimento più lungo da brevi take di una voce. Prende il nome da esse, va nella loro cartella se ne condividono una e ne prende i testi quando ciascuna ne ha uno; le clip selezionate restano. Le clip wav dello stesso formato vengono unite così come sono; le clip mp3 o i wav di formati diversi richiedono ffmpeg. **Elimina selezionate** elimina le clip selezionate dopo una conferma; una clip ancora in uso resta, e il messaggio ne indica il nome.
- **Clip**: la clip selezionata.
  - **Nome** e **Cartella**: rinomina la clip (il file mantiene il suo nome) e mettila in una cartella, una nuova digitando il suo nome. Una versione resta nella cartella della clip da cui è stata creata, e indica da cosa è stata creata e come.
  - La sua **forma d'onda**: clicca per posizionare la testina di riproduzione e **Riproduci**, oppure trascina su di essa per selezionare un intervallo (i campi **Da** e **A** seguono, al centesimo di secondo, e la selezione li segue). La lente di ingrandimento ingrandisce la selezione per un bordo preciso, l'altra mostra di nuovo l'intera clip. **Riproduci la selezione** riproduce l'intervallo, **Riproduci attorno al taglio** riproduce un secondo e mezzo per lato senza di esso: come suona la clip quando l'intervallo viene tagliato via.
  - **Scarica** la clip (con il nome della clip e della sua versione), leggi cosa non va, **Sostituisci...** o **Elimina** (solo quando nulla la usa; le sue versioni passano alla clip da cui sono state create).
  - **Pulisci con ffmpeg** crea una versione con meno fruscio e ronzio costanti. Non rimuove musica, effetti o altre voci.
  - **Pulisci con IA** crea una versione con la [pulizia](#cleanup-of-clips) scelta su ComfyUI. Gira in background: puoi continuare a lavorare, e un messaggio segnala quando la versione è pronta.
  - **Ritaglia** crea una versione con solo l'intervallo selezionato della clip (**Mantieni questa parte**) o senza di esso (**Taglia via questa parte**), per un suono che nessuna pulizia rimuove: un animale, un colpo di tosse, una porta. Le giunzioni sono sfumate per non fare clic. Una clip wav si ritaglia senza ffmpeg, un mp3 richiede ffmpeg.
  - **Usa questa versione** mette una versione dovunque sia usata la clip da cui è stata creata. Le parti vengono rifatte con la nuova voce, e la vecchia clip resta finché non la elimini.
  - **Prova la voce**: scrivi un testo e premi **Pronuncia**: il backend in uso lo dice con questa clip esattamente come scritto (nessuna etichettatura, nessun suono aggiunto), con il **Seed** che indichi (vuoto: 42, quello delle conversioni), e lo riproduce. La ⓘ elenca i tag che il modello del backend legge, nella sua sintassi (`[cough]` per Chatterbox Turbo, `<|sfx:cough|>` o `<|emotion:sadness|>` per Higgs); un clic ne inserisce uno nel testo. Occupa il blocco GPU del backend e compare nei lavori come una conversione. La pulizia e il riconoscimento di una clip sono anch'essi lavori.
  - **Testo della clip**: le parole pronunciate nella clip. Scrivile, oppure premi **Riconosci** per far scrivere il [riconoscimento vocale](#speech-recognition), correggile e **Salva**. La finestra dell'attore mostra lo stesso campo per la clip della voce dell'attore. Una versione e una sostituzione ereditano il testo. Higgs lo riceve solo quando il backend lo invia (disattivato di default).
- **Sostituisci...** mette un'altra clip, una tua o una che carichi qui, dovunque la clip sia usata.

I selettori di una voce (dell'attore, del narratore di un backend) mostrano una clip come *cartella / clip › versione*.

<a id="a-good-clip"></a>

#### Una buona clip

Un modello vocale copia la **registrazione**, non solo la voce: la stanza, un fruscio, la musica sotto una scena di film tornano in ogni battuta, spesso come un leggero "fruscio" sotto le parole che nessun filtro del risultato rimuove. Quindi:

- Usa una registrazione pulita di un solo parlante con pause vere (il campione di un doppiatore, una lettura fatta in casa in una stanza silenziosa), di 12-30 secondi. Una clip tagliata da un film o da una serie è il caso peggiore.
- I segni di qualità sono misurati dalla clip: **Pulita** (le pause sono almeno 45 dB sotto il parlato), **Discreta** (35-45 dB, oppure più lunga di 30 s, cosa che Chatterbox taglia), **Scarsa** (suono di fondo, sotto 35 dB; distorta; più corta di 8 s). Una clip che non è pulita viene segnalata quando la aggiungi.
- Un modello di separazione mantiene ciò che suona come una voce: il verso di un gufo o il canto di un uccello restano (nel test con più modelli). Taglia via un suono del genere con **Ritaglia**.
- Pulisci una clip con l'IA ([Pulizia delle clip](#cleanup-of-clips)): nel test la pulizia *Clip di film o gioco* ha portato una clip con fruscio, ronzio ed eco da *Scarsa* a *Pulita* (le pause sono passate da 38 a 89 dB sotto il parlato). Ascolta la versione prima di usarla: un modello di separazione può anche portare via un respiro o la fine di una parola sussurrata. La pulizia con ffmpeg abbassa solo un rumore costante: su una clip da film ha guadagnato circa 3 dB e la clip è rimasta *Scarsa*.
- Senza ComfyUI, pulisci una clip a mano: la riduzione del rumore di [Audacity](https://www.audacityteam.org/) con un profilo del rumore preso da una pausa della clip, oppure un rimuovi-voce come [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) per una clip con musica.

<a id="converting-new-parts-by-itself"></a>

### Conversione automatica delle nuove parti

*Converti automaticamente le nuove parti in audio* è l'impostazione predefinita per tutti i tuoi libri; vedi [Conversione automatica](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Il cast e la pronuncia di un libro

Entrambi sono conservati insieme al libro (viaggiano nel suo backup) e si modificano dalla finestra di dialogo **Convert to audio**.

**Cast** indica chi legge: un attore per il **narratore**, uno per gli **altri parlanti** (chiunque il modello nomini e che il cast non preveda) e uno per ogni **personaggio**. Un personaggio ha un nome e altri nomi con cui il testo può chiamarlo (*Tom* per *Tomáš Novák*), separati da virgole. Una battuta viene attribuita al personaggio quando il suo nome, uno degli altri nomi o una parola del nome che nessun altro personaggio condivide corrisponde, senza distinzione tra maiuscole e minuscole né tra accenti. **Add the characters of the book** riempie l'elenco con i personaggi del punto di vista e con i personaggi presenti nelle parti.

![Il cast di un libro](../../images/plugin-audiobook-cast.png)

**Parlanti senza nome** (la guardia, l'oste, la ragazza alla finestra) possono avere ciascuno una voce propria, ricavata da una o due registrazioni. Assegna al cast gli attori di base: **Other men**, **Other women** e, se vuoi, **Children** (se vuoto, si usa la voce femminile, alzata di tono). L'etichettatura indica poi anche il genere e l'età di ogni parlante che il cast non nomina, e ciascuno parla con una *variazione* della clip di base del suo tipo: un po' più acuta o più grave, con altri formanti, più veloce o più lenta, più scura o più chiara. Un ragazzo adolescente riceve la voce maschile, una ragazza adolescente e un bambino quella femminile, un parlante anziano una voce più grave e più lenta. La variazione deriva dal nome del parlante e dal libro, quindi lo stesso parlante suona sempre uguale in ogni capitolo e a ogni esecuzione. Se lasci vuoti tutti e tre i campi, ogni parlante senza nome viene letto da **Other speakers**, come prima.

I parlanti incontrati finora sono elencati sotto le voci di base, con il genere e l'età assegnati per primi (una parte successiva non li modifica). Puoi correggere il genere o l'età, ascoltare il parlante, assegnargli **another variation** (il dado) oppure rimuoverlo dall'elenco. Un parlante modificato viene rifatto alla successiva conversione delle parti.

Una variazione richiede **ffmpeg** con il filtro *rubberband* sul server (i pacchetti di Debian e Ubuntu, che l'immagine Docker installa, sono di norma compilati con esso; `ffmpeg -filters | grep rubberband` indica se il tuo lo è). Ogni variazione viene creata una sola volta, quando serve per la prima volta, e conservata come copia nascosta della sua clip, così viaggia insieme ai tuoi backup. Senza ffmpeg il cast lo segnala: i parlanti senza nome condividono la voce di base del loro tipo, mentre le variazioni già create continuano a essere usate. Una variazione modifica solo una clip tua; un attore la cui voce è una voce del server di sintesi viene usato così com'è.

**Pronuncia** indica come il testo viene scritto *per la voce*; il libro in sé non viene mai modificato. Il **dizionario** sostituisce parole intere (con distinzione tra maiuscole e minuscole) con ciò che va detto al loro posto: una grafia fonetica per un nome o una parola inventata, oppure una lettura in kana per il giapponese. Per un libro in inglese le regole leggono ad alta voce anche altri elementi: numeri, ordinali, anni, orari, importi e abbreviazioni vengono letti per esteso; un titolo composto solo da un numero viene letto come *Chapter four. The Arch* (la parola prima del numero è a tua scelta); i segni markdown come `*` vengono eliminati; un cambio di scena (`***`, `---`) non viene letto, ma produce una pausa più lunga. Per un libro in un'altra lingua disattiva le regole; il dizionario resta comunque attivo. **Try a text** mostra come verrà letto un brano di testo. Un brano il cui testo parlato cambia viene rifatto, gli altri restano invariati.

![La pronuncia di un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertire un libro

*Menu Strumenti dell'editor della storia (il pulsante con gli ingranaggi) → **Convert to audio***.

![La finestra di dialogo prima dell'avvio](../../images/plugin-audiobook-convert.png)

1. Scegli il **backend** e premi **Load**: vengono elencate le parti con testo del ramo attivo (la prima è la 1) con ciò che ha ciascuna:
   *Not generated*, *Labelled* (le etichette del modello sono memorizzate, ancora nessun audio), *Generated* (con un pulsante di riproduzione e un download) oppure
   *Generated (the text changed)* (l'audio è stato creato da un testo più vecchio e viene ricreato); *(final)* viene aggiunto a una parte che lo studio segna come finale.
2. Lascia **Label the parts with a language model** disattivato per la versione semplice: il narratore legge ogni paragrafo. Attivalo per far decidere al modello chi pronuncia ogni pezzo di dialogo, in che modo (calmo, sussurrato, urlato...) e dove va un tag sonoro, e il cast fornisce le voci. Il modello del libro è già scelto; puoi sceglierne un altro se preferisci. Le parti vengono prima etichettate, poi lette ad alta voce. Una parte che ha già etichette per il suo testo viene letta anche con quelle quando la casella è disattivata (il modello non viene interpellato), quindi rieseguire un libro etichettato senza la casella non trasforma i suoi dialoghi in semplice narrazione; **Remove labels** è il modo per chiedere una narrazione semplice o nuove etichette.
3. Seleziona le parti (la casella nell'intestazione le seleziona tutte) e **plan** ciò che va fatto con esse; la colonna **Plan** lo mostra e, per ora, non si fa nulla:
   **Generate** (crea ciò che manca; una parte già aggiornata non viene creata di nuovo), **Regenerate** (elimina l'audio, rimuove le etichette, genera), **Delete audio** (i pezzi e l'audio unito della parte; le etichette restano), **Remove labels** (il modello viene interpellato di nuovo la prossima volta che la parte viene creata), **Clear the plan**. I passi si sommano su una parte (Delete audio e Remove labels senza Generate fanno solo pulizia); una parte contrassegnata come finale è esclusa da ogni piano.
4. **Start** esegue il piano. Quando sottrae audio o etichette, chiede prima conferma. Ciò che va generato finisce nella tua coda, che prosegue quando chiudi la finestra e quando lasci la pagina; una parte in attesa o in esecuzione nella coda è esclusa da un piano. Lo stato, i pezzi già creati e il messaggio del job sono mostrati nella riga della parte.
   Mentre il modello etichetta una parte, la sua riga dice *Labelling pieces 17-32 of 120* (al modello si chiedono 16 pezzi alla volta); **fai clic sullo stato** di una parte etichettata per leggere, per ogni richiesta, il ragionamento del modello e la sua risposta, man mano che arrivano. Quando l'etichettatura della parte termina (anche se fallisce o viene interrotta), tutto questo viene conservato con la parte, ogni esecuzione dopo le precedenti; puoi rileggerlo, o eliminarlo, con **Reasoning** nello studio. Nuove etichette e nuovo audio non lo toccano, ed è incluso nei backup e negli export del libro con l'audio.

![La coda: pezzi già creati, e un download quando una parte è completata](../../images/plugin-audiobook-queue.png)

Come viene creata una parte, così sai cosa aspettarti:

- Il testo è **diviso dal programma**, mai dal modello, in pezzi di narrazione e pezzi di discorso diretto (tra virgolette doppie dritte o curve, o tra parentesi angolari giapponesi; un titolo è narrazione). Il modello si limita a *etichettare* i pezzi numerati. Un testo con tag sonori viene usato solo quando è il pezzo con i tag aggiunti e nient'altro è cambiato, così il modello non può mai modificare il tuo libro.
- **Chi parla in un pezzo lo decide il modello per il parlato; la narrazione spetta al narratore.** Una lettera, un biglietto, un messaggio o i pensieri di un personaggio non hanno virgolette, quindi il programma li considera narrazione. Un pezzo del genere viene letto dal personaggio (la voce del cast) solo quando **inizia in corsivo** (`*...*` o `_..._`) e il modello indica il personaggio, dal contesto (una firma, «la grafia era inconfondibile»). La narrazione in terza persona, anche quando è raccontata dal punto di vista di un personaggio («Lei guardò Percy attraversare la stanza»), è sempre del narratore: il programma ignora un personaggio che il modello assegna a un pezzo senza corsivo. Anche il corsivo per enfasi, per suoni o per parole straniere è narrazione. Le parole sono sempre le tue.
- **L'istruzione al modello è un modello che puoi modificare.** *Settings → Extension Settings → Audiobook Settings → Labelling prompt* è il tuo predefinito; un libro (scheda *Prompts* → *Extension Settings* → *Audiobook*) o una cartella possono averne uno proprio. Se il campo è vuoto, si usa quello della cartella superiore, poi il tuo, poi quello integrato. È scritto come gli altri [prompt](../books/prompts.md): le variabili sono `{{characters}}`, `{{deliveries}}`, `{{tags}}` e `{{wordsPerTag}}`, e il popover dei suggerimenti ha **Insert Default Template**. Un'istruzione modificata fa sì che le etichette delle parti vengano richieste di nuovo la prossima volta che vengono create.
- **Il volume delle parti viene uniformato** quando è installato il programma ffmpeg (lo stesso che crea il `.m4b`): l'audio unito di ogni parte viene portato a un unico livello di volume (EBU R128, lineare, così la dinamica resta; **-18 LUFS** a meno che tu non lo cambi in *Settings → Extension Settings → Audiobook Settings*, dove puoi anche disattivarlo). I pezzi restano come li ha prodotti il server; viene uniformata solo la parte unita, così un audiolibro non salta di volume da una parte all'altra. Le **parti create in precedenza** non vengono toccate finché non le converti di nuovo: allora vengono riunite dai pezzi memorizzati e uniformate, senza chiedere nulla al server vocale (nemmeno lo scaricamento del modello linguistico). Disattivare l'uniformazione lascia le parti già uniformate così come sono. Se ffmpeg fallisce per una parte, questa conserva il suo audio com'era e il risultato lo segnala.
- Tutte le parti di un clic vengono create in **un unico batch per modello vocale**: le richieste prima del batch vengono eseguite una volta, poi tutti i pezzi vengono pronunciati, poi le richieste dopo il batch vengono eseguite una volta. È questo che evita che la scheda grafica cambi modello a ogni parte.
- Ogni pezzo viene **conservato in base al suo contenuto** (testo, voce, modello, lingua, impostazioni, seed). Una parte già aggiornata non costa nulla, un paragrafo modificato è l'unico pezzo creato di nuovo, e una parte fallita conserva i pezzi già creati quando premi **Retry**.
- I pezzi sono uniti con pause che dipendono dalla loro posizione (dopo un titolo, tra paragrafi, prima di *he said*) in un unico mp3 per parte, conservato con la parte.
- Mentre una tua generazione è in corso, il parlato attende, così il modello linguistico non viene scaricato sotto di essa.
- Una parte che hai contrassegnato come **final** nello studio viene lasciata stare, e la finestra di dialogo dice quante ne sono state.
- **L'intero audiolibro** unisce in un unico mp3 ogni parte che ha audio aggiornato, con un marcatore di capitolo per ogni parte, e mostra un link di download sotto l'elenco (le parti escluse vengono nominate). Lo stesso file è il formato di export *one mp3 with chapter marks*.
- **Stop all** annulla le parti in attesa e ferma subito quella in corso: la richiesta al modello linguistico (l'etichettatura) o al server vocale (il pezzo in lettura) viene interrotta, senza attenderla. Un batch fermato prima di iniziare non invia le richieste che lo precedono, così il modello linguistico resta caricato. Una parte i cui pezzi sono stati tutti creati prima dell'interruzione riceve comunque il suo audio (viene solo unita); le altre conservano i pezzi che hanno, e la prossima esecuzione crea solo quelli mancanti. **Download** fornisce l'mp3 di una parte completata.
<a id="the-audiobook-studio"></a>

## Lo studio dell'audiolibro

*Tools menu → **Audiobook studio*** (la voce del pezzo selezionato, con l'anteprima, si trova accanto al suo altoparlante) è dove si realizza l'audiolibro finale, una parte alla volta: si vede lo script, lo si modifica, si ascolta un pezzo e si realizza la parte.

![Lo studio con un discorso selezionato](../../images/plugin-audiobook-studio.png)

- Mentre la parte viene annotata o realizzata, una barra indica che è in corso un'operazione, e i pulsanti che avvierebbero altro lavoro restano in attesa.
- **Annotate** crea lo script della parte (con il modello linguistico, se lo richiedi, altrimenti con il narratore) e mostra i pezzi in una tabella: speaker, modalità di esecuzione, intensità, il testo parlato e un segno per un pezzo che hai modificato. Mentre è in esecuzione compare accanto un pulsante **Stop**, che interrompe la richiesta al modello linguistico.
- **Reasoning** mostra ciò che il modello linguistico ha detto ogni volta che ha etichettato la parte (la data dell'esecuzione e i pezzi di ogni richiesta, il suo ragionamento e la sua risposta), così puoi vedere perché una riga è stata assegnata a uno speaker o ha ricevuto una certa modalità di esecuzione. **Delete the reasoning** lì la rimuove; le etichette e l'audio restano.
- Seleziona un pezzo per modificarne lo **speaker**, il **testo parlato**, la **modalità di esecuzione**, l'**intensità** (l'enfasi della voce, per i modelli che la prevedono; su Higgs la modalità di esecuzione e l'intensità sono i segni come `<|emotion:sadness|>` all'inizio del testo parlato, che li seguono quando li cambi), la **lingua**, il **seed**, la **pausa dopo**. **Apply** conserva la modifica e **blocca** il pezzo: una nuova annotazione lascia un pezzo bloccato così com'è.
  **Unlock** lo restituisce all'annotazione. *The request the speech server gets* mostra la richiesta esatta.
- **Hear** crea l'audio di un pezzo (solo quel pezzo; il server vocale viene avviato per questo); **Another take** lo rifà con un nuovo seed, e le **takes** vengono conservate così puoi scegliere quella che preferisci (*Use this take and hear it*). Una bella voce dipende spesso dal seed.
- **Make the part from this script** unisce i pezzi così come sono, senza una nuova annotazione.
- Lo **stato** della parte è *draft*, *reviewed* o *final*. Una parte final non viene annotata né convertita di nuovo, e l'esportazione può essere limitata alle parti final.

<a id="exporting"></a>

## Esportazione

L'audiolibro è un formato di *Export story* del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** e, quando è installato ffmpeg, **Audiobook (one m4b with chapters)**. Sono composti dall'audio delle parti aggiornate: una parte che non ha audio, o un audio più vecchio del suo testo o del suo script nello studio, viene esclusa, e la finestra lo indica. I file mp3 contengono tag ID3 (il titolo del libro, il capitolo, la traccia), e il file singolo ha segnalibri di capitolo che lettori come VLC e la maggior parte delle app per audiolibri mostrano; lo zip contiene una playlist. La casella **Audiobook: only the parts marked final** limita l'esportazione alle parti che hai completato nello studio.

Il formato **m4b** è quello che Apple Books e la maggior parte delle app per audiolibri sul telefono richiedono: audio AAC (64 kbit/s, sufficiente per il parlato) in un contenitore MP4 con i capitoli, il titolo, l'autore e il genere. È ffmpeg a crearlo, quindi il formato è offerto solo su un server che ha ffmpeg. L'audio memorizzato come mp3 viene codificato una seconda volta per questo; l'audio memorizzato come wav viene codificato una sola volta.

<a id="how-the-audio-is-stored"></a>

### Come viene memorizzato l'audio

Un amministratore lo sceglie per l'intera installazione in *Settings → Extension Settings → Audiobook Settings* (l'ultima sezione, visibile solo agli amministratori):

- **mp3** (predefinito): piccolo, unito senza un decoder, non serve ffmpeg. L'm4b è un secondo passaggio con perdita.
- **wav**: senza perdita, circa dieci volte più grande (un'ora di parlato occupa circa 170 MB), richiede ffmpeg. I pezzi vengono richiesti al server vocale in wav e uniti come wav; il browser li riproduce così come sono;
  le esportazioni mp3 e zip vengono codificate da ffmpeg (96 kbit/s) e l'm4b viene codificato una sola volta.

La scelta si applica a ciò che viene creato da quel momento in poi. Le parti create prima conservano il loro audio e vengono riconosciute così come sono; una parte di entrambi i tipi (un pezzo bloccato dello studio di prima) viene unita come wav, e un'esportazione di parti di entrambi i tipi funziona, ma richiede ffmpeg.

L'esportazione dell'intero libro viene creata a partire dai file memorizzati delle parti, in file temporanei del server, e scaricata da lì, quindi la sua durata non è limitata dalla memoria dell'applicazione; tuttavia la cartella temporanea deve avere spazio per circa il doppio dell'audio del libro (con il wav, circa 350 MB per ora) durante la creazione.

![La finestra di esportazione con i formati audiolibro](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversione automatica

Un libro può creare da solo l'audio di una nuova parte, non appena la parte è final: dopo l'annotazione automatica, la riscrittura e le traduzioni, se il libro le prevede, come ultimo passaggio di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part)
(nel pannello delle esecuzioni fino alla creazione dell'audio; il suo **Stop** rimuove la parte dalla coda). È **off** per impostazione predefinita. Sceglilo nella scheda *Prompts* del libro, tra le impostazioni dell'estensione, per una cartella di libri, o per tutti i tuoi libri nelle impostazioni dell'audiolibro; vince quello più vicino:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; un libro senza modello ottiene la narrazione semplice, e te lo viene comunicato.

![L'impostazione in un libro](../../images/plugin-audiobook-auto.png)

La parte entra nella tua coda, con il cast e la pronuncia del libro. Un avviso ti dice che è in coda e un altro che il suo audio è pronto o è fallito; compaiono solo nella pagina che ha avviato la generazione, mentre il lavoro si trova comunque nella finestra *Convert to audio*.

<a id="listening"></a>

## Ascolto

Un libro pubblicato (o il lettore) che ha parti con audio riceve una barra del lettore: riproduci, pausa, stop, la velocità, e la parte che viene ascoltata è evidenziata nel testo. Parte dalla selezione o dalla prima parte visibile, riproduce le parti in ordine e recupera la successiva mentre quella corrente viene riprodotta, e i controlli multimediali del dispositivo riconoscono il libro. L'audio creato da una versione più vecchia del testo di una parte non viene offerto. L'audio è servito al proprietario del libro e, mentre il libro è pubblicato, a chiunque possa aprirlo; un libro che viene ritirato dalla pubblicazione smette di essere servito subito (alla parte successiva, quando il lettore si carica).

![Il lettore nel lettore di storie](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte senza audio

Il lettore si ferma su una parte che non ha audio, perché una parte della storia non venga saltata, e lo segnala. Quando è installata l'estensione [Read Aloud](tts-reader.md), la barra mostra anche un pulsante con altoparlante, **Read this part (and the next ones without audio) with the voice of the browser**. Non viene letto nulla con una voce artificiale, se non lo premi. Legge quella parte e le parti successive che non hanno audio con la voce del tuo dispositivo, e quando arriva a una parte con audio il lettore riprende il controllo. Le parti successive senza audio nello stesso ascolto vengono lette allo stesso modo senza chiedere di nuovo; **Stop** la silenzia, e riavviare il lettore chiede di nuovo. La voce, la velocità e il tono sono quelli di Read Aloud.
<a id="commands"></a>

## Comandi

Le stesse cose di [comandi](../commands/index.md) (la riga di comando dell'editor della storia, la macro `command` di un modello, un agente). Le parti sono numerate come le numera la finestra di dialogo Convert, oppure si indicano con il loro handle.

| Comando | Cosa fa |
|---|---|
| `/audiobook-status [from [to]]` | Il server vocale in uso, come il libro si converte da solo, quante parti hanno l'audio (nessuna, pronta, più vecchia del testo, più vecchia dello script), lo stato di ogni parte elencata nello studio e la coda. È lento solo se il libro è lungo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Mette le parti in coda (l'intero libro senza argomenti, una sola parte quando si indica solo `from`). `labelling` `true` usa il modello del libro; `what` è `audio` oppure `script` (solo lo script per lo studio, nulla viene letto ad alta voce). Con `wait` `true` il comando resta attivo finché i lavori non sono terminati, mostra a che punto sono e dice come è finito ciascuno; interromperlo annulla i lavori non ancora avviati. |
| `/audiobook-stop` | Annulla le parti in attesa; quella in esecuzione si ferma dopo il suo pezzo. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` nello studio; la parte richiede uno script. |
| `/audiobook-cast` | Il narratore, il fallback e i personaggi del cast con i loro attori, gli attori di base dei parlanti senza nome e i parlanti senza nome incontrati finora (genere, età, variante). |

![Il risultato di un comando](../../images/plugin-audiobook-command.png)

L'audio viene creato per **il tuo** libro, non per la copia di lavoro nascosta di un [agente](../agents.md): un agente può leggere il cast e riceve un rifiuto dagli altri comandi, che indica che
devi eseguirli tu. Eseguili dalla riga di comando, oppure affida a un agente il compito di preparare il libro (annotare, rivedere) e convertilo tu.

<a id="notifications"></a>

## Notifiche

Quando un gruppo di conversioni è terminato, il browser può avvisarti ([notifiche](../account.md#notifications), così puoi lasciare la scheda mentre il server vocale lavora). Nell'elenco delle notifiche della pagina *Settings* ci sono due voci: **Convert to audio** (i gruppi che avvii dalla finestra di dialogo o con il comando) e **Convert to audio (automatic)** (una nuova parte che è stata resa audio da sola). Un gruppo che hai interrotto non genera notifica; un gruppo con una parte fallita segnala che è fallito.

![L'elenco delle notifiche](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Cosa non fa (ancora)

- Solo mp3 e wav (niente opus); il volume dei pezzi è quello che forniscono le voci.
- Nessun effetto sonoro né ambiente.
- Due motori vocali, Chatterbox e Higgs Audio v3 (in ComfyUI).
- L'ascolto di una voce fuori da un segmento.
- Il cast non fa parte del backup degli *attori* di un libro: un libro ripristinato mantiene il suo cast, ma indica attori che devono esistere nel tuo account.
