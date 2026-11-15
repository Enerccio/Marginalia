---
label: Audiolibro
order: 27
---

# Audiolibro

Trasforma un libro in un audiolibro, parte per parte, con un server di sintesi vocale: una voce per il narratore e una per ogni personaggio, conservate insieme alle parti del libro e riprodotte nel lettore. Puoi lasciarlo girare (il modello indica chi parla e come, il server di sintesi legge) per ottenere una bozza approssimativa, oppure lavorare sulle parti una alla volta nello **Audiobook studio**, controllando esattamente ciò che viene inviato al server di sintesi e modificandolo a mano, ed esportare il risultato come file mp3 con i capitoli segnati.

L'estensione non genera il parlato da sola: dialoga con un **Chatterbox-TTS-Server** che tu avvii (vedi [Configurare il server di sintesi vocale](audiobook-server.md)). Il testo del libro non viene mai modificato e non viene inviato a nessuno, se non ai server che hai indicato; l'audio è conservato nella tua installazione, come risorse collegate alle parti.

<a id="what-you-need"></a>

## Cosa serve

- Un server di sintesi vocale e, se condivide la scheda grafica con il tuo modello linguistico, le richieste che gli fanno spazio e poi lo fermano di nuovo ([Configurare il server di sintesi vocale](audiobook-server.md)).
- Voci: una breve **clip di riferimento** (da 12 a 30 secondi di una sola persona, senza musica) per ogni voce che vuoi clonare, oppure uno dei file vocali disponibili sul server. Un narratore ha bisogno di una voce di lettura calma e costante.
- Per un libro con più voci, un modello linguistico capace di etichettare il testo. Di default è il modello del libro.
- **ffmpeg** è facoltativo. Senza di esso tutto funziona con mp3. Se è presente sul server (è nell'immagine Docker e, su un desktop, si trova in `PATH` o nelle cartelle usuali come `/opt/homebrew/bin`; la variabile `MARGINALIA_FFMPEG` ne indica il percorso quando è altrove), l'esportazione ottiene il formato **m4b**, e un amministratore può salvare l'audio in wav.

<a id="the-settings"></a>

## Le impostazioni

*Impostazioni → Extension Settings → Audiobook Settings* è il pannello del tuo account; salva la pagina *Impostazioni* dopo averlo modificato.

![Il pannello con un backend e gli attori](../../images/plugin-audiobook-settings.png)

<a id="backends"></a>

### Backend

Un backend è un server di sintesi vocale e il modello con cui parla. Aggiungine uno con **+**. **Duplicate** copia quello selezionato (indirizzo, chiavi, blocco GPU, voce, valori predefiniti e le richieste prima e dopo un batch) con un nuovo nome: è il modo rapido per avere un backend di un altro modello sulla stessa macchina, per esempio Higgs accanto a Chatterbox, dove poi cambi il modello, l'indirizzo e le richieste di avvio e arresto.

| Impostazione | Cosa fa |
|---|---|
| Name | Il nome mostrato nella finestra di conversione. |
| Address | L'indirizzo base del server di sintesi vocale, o di ciò che lo inoltra verso di esso, per esempio `http://localhost:8004`. |
| Api key | Inviata come bearer token se il server (o il tuo proxy) la richiede. Viene memorizzata cifrata e non viene più mostrata; digitane una nuova per sostituirla, oppure usa **Remove the key**. |
| Speech model | **Chatterbox Turbo** (in inglese, con tag sonori come `[sigh]`, il più veloce e predefinito), **Chatterbox** (in inglese, con una *exaggeration* dell'interpretazione), **Chatterbox Multilingual** (23 lingue) oppure **Higgs Audio v3 in ComfyUI** (molte lingue, voci che seguono lo stato d'animo di una battuta e suoni come pianto, affanno o un gemito; vedi [sotto](#higgs-audio-v3-emotions-and-sounds)). Il [blocco GPU](../administration/gpus.md) del backend (o le richieste prima di un batch) deve lasciare questo modello caricato. Un libro che richiede un altro modello usa un altro backend. |
| GPU lock | Il [blocco GPU](../administration/gpus.md) della GPU su cui gira il server, quando la condivide con il modello linguistico o con altri server (anche con l'[Image Generator](image-generator.md)): ogni batch e ogni campione attende il proprio turno sulla GPU, e il blocco carica per primo il server di sintesi vocale. Visibile solo quando l'amministratore ha configurato i blocchi GPU. |
| The server is on this machine or on my network | Consente gli indirizzi della rete locale. Lascialo disattivato per un server nel cloud: così non può essere costretto a chiamare la tua macchina. |
| Timeout of a request | Quanto attendere per un singolo pezzo di parlato. |
| Restart the server after (pieces) | Per un server di sintesi che perde memoria e viene terminato dal sistema in un batch lungo: dopo questo numero di pezzi, le richieste successive al batch e poi quelle precedenti vengono rieseguite a metà del batch (arresto, avvio, caricamento), e il lavoro prosegue con un server nuovo. 0 (il valore predefinito) non lo fa mai. Un server che si arresta viene riavviato comunque, qualunque cosa dica questo valore, vedi [Risoluzione dei problemi](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | La chiave che le richieste prima e dopo un batch inviano come `Authorization: Bearer`. Viene memorizzata cifrata e non mostrata di nuovo (**Remove the key** la cancella), così non deve essere scritta negli header di ogni richiesta, dove sarebbe leggibile. Vuoto: viene usata la *Api key* qui sopra, che è ciò che vuoi quando un proxy come LiteLLM serve tutto. Una richiesta che ha già un header `Authorization` proprio lo conserva. |
| Narrator voice | Il narratore di un libro che non ha un cast. Un file vocale del server si scrive `predefined:Henry.wav`. |
| Precision | Solo Higgs: la precisione con cui il modello calcola. **fp32** (il predefinito) è la scelta sicura: su una scheda AMD **bf16** ha reso i pezzi incomprensibili, con effetti sonori e voci sbagliate. bf16 richiede circa metà della memoria grafica (circa 8 GB invece di 16) ed è più veloce dove funziona. |
| Send the text of the clip to Higgs | Solo Higgs, sperimentale: il [testo della clip](#voices-and-clips) viene inviato insieme ad essa. Lascialo disattivato: nei test ha cambiato la voce. |
| Check the length of the pieces | Attivo di default. Un pezzo molto più lungo del suo testo (4 s più 0,6 s per parola: il modello ha continuato a parlare, o ha prodotto un suono al posto delle parole) viene rifatto con il seed successivo, fino a **Tentativi** volte; viene conservata la prima versione di lunghezza corretta, altrimenti la più corta. |
| Check the words with speech recognition | Disattivato di default; richiede il [riconoscimento vocale](#speech-recognition). Dopo ogni batch ogni pezzo viene trascritto e confrontato con il suo testo. Un pezzo in cui la percentuale di parole sbagliate supera **Max. word errors (%)** viene rifatto con il seed successivo e ricontrollato, fino a **Tentativi** round; viene conservata la versione con il minor numero di parole sbagliate. La conversione indica quanti pezzi sono stati rifatti e quanti differiscono ancora. |
| Defaults of a segment | Temperatura, peso CFG, velocità e dimensione dei blocchi con cui parte un pezzo (lo studio può cambiarli per un singolo pezzo). Higgs usa solo la temperatura. |
| Before a batch, After a batch | Richieste fatte prima e dopo ogni batch di parti (e ogni campione), all'interno del blocco GPU. Liberare la scheda grafica e avviare il server di sintesi è compito del blocco GPU; vedi [Una sola GPU per il modello e il parlato](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vuoto non fa nulla. |

**Test connection** fa ciò che fa un batch: prende il blocco GPU (così un server di sintesi fermo viene avviato), esegue le richieste prima di un batch, chiede al server se è raggiungibile e se ha un modello caricato, ed esegue le richieste dopo il batch. Senza blocco o richieste si limita a chiedere.

Un libro che contiene pezzi in un'altra scrittura (giapponese in un libro in inglese) li invia al modello multilingue, se hai un backend per esso, con la loro lingua; restano sul modello del libro se non ne hai uno. Un libro Higgs li mantiene: Higgs li legge da sé, con la stessa voce.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: emozioni e suoni

Con un backend **Higgs Audio v3 in ComfyUI** si sentono le etichette del modello linguistico. L'indirizzo del backend è quello del tuo ComfyUI
(con i nodi di TTS Audio Suite, vedi [Configurare il server vocale](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **resa** di un parlato è recitata: triste, arrabbiato, gioioso, divertito, teso, impaurito, sorpreso, tenero, sarcastico, implorante, disperato,
  disgustato, eccitato, un sussurro o un grido. Una resa forte (intensità 0,8 e oltre) è più espressiva, una debole (sotto 0,2) viene letta in modo neutro.
  Il narratore legge in modo neutro.
- Il modello linguistico può inserire più **suoni** nel parlato rispetto a Chatterbox Turbo: risata, risatina, risolino, sospiro, respiro affannoso, tosse, schiarirsi la gola,
  tirata di naso, starnuto, gemito, pianto, singhiozzo, urlo, ansimo, lamento, mugolio e «shhh».
- Il programma legge anche la **narrazione attorno a ogni parlato** (in inglese) e assegna al parlato il suono che essa nomina, quando il modello non ne ha messo nessuno:
  *"You look funny," she giggled.* riceve un risolino, *She burst out laughing. "Careful!"* una risata, *a soft moan escaped her lips. "Yes..."* un lamento,
  *panting* o *out of breath* un ansimo, *cried quietly* o *burst into tears* un pianto. Ignora ciò che solo sembra un suono: *"Stop!" she cried.*
  (un'esclamazione), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, e un suono che la narrazione
  attribuisce a un altro personaggio con nome. *"You look funny," he mocked her.* riceve una resa sarcastica o divertita e nessun suono. La narrazione stessa viene letta come scritta.
- **Il narratore viene sempre letto in modo neutro**: nessun suono e nessuno stato d'animo, anche per un parlato che il modello ha lasciato al narratore.
- Higgs non richiede un clip per ogni stato d'animo: basta un clip di una voce. Ne copia la registrazione, quindi un clip pulito conta più che con Chatterbox
  ([Un buon clip](#a-good-clip)).
- Ogni pezzo viene generato da ComfyUI e la sua memoria viene svuotata dopo (qualche secondo a pezzo), il che evita che alcune schede grafiche si blocchino.
- Higgs spesso prosegue oltre l'ultima parola di un pezzo (un respiro, l'inizio di un altro suono) e il clip finisce nel mezzo, il che si sente come un breve "fruscio" prima della pausa. Con l'estensione **ffmpeg** la coda viene tagliata e ogni pezzo sfuma in chiusura; senza di essa i pezzi restano come Higgs li ha generati.
- Il modello Higgs Audio v3 è distribuito con una **licenza di ricerca e non commerciale**: un audiolibro che vendi richiede una licenza da Boson AI.

<a id="speech-recognition"></a>

### Riconoscimento vocale

Il **Speech recognition** trascrive ciò che dice un parlato. Controlla i pezzi di un batch (quando un backend ha **Check the words with speech
recognition** attivo) e riconosce il [testo di un clip vocale](#voices-and-clips). Scegli dove viene eseguito:

| Impostazione | Cosa fa |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)**: il modello di riconoscimento Granite di TTS Audio Suite nel tuo ComfyUI (l'indirizzo è quello di ComfyUI). **OpenAI-compatible (whisper)**: qualsiasi server con `/v1/audio/transcriptions`, come un server whisper (faster-whisper, speaches) o le API di OpenAI. |
| Address, Api key | L'indirizzo di ComfyUI, o la base delle API (con o senza `/v1`); la chiave viene salvata cifrata. Indirizzo vuoto: nessun riconoscimento vocale. |
| GPU lock | Il [blocco GPU](../administration/gpus.md) che usa il riconoscimento. Su una sola scheda con Higgs in fp32, assegnagli un blocco proprio i cui passaggi riavviano ComfyUI: ComfyUI tiene il modello vocale nella memoria del server, e caricare il riconoscimento accanto ad esso ha esaurito la memoria del server. |
| Model of the recognition | Vuoto: `granite-speech-4.1-2b` per ComfyUI, `whisper-1` per quello compatibile con OpenAI. |
| Timeout, The server is on this machine or on my network | Come per un backend. |

**Test connection** verifica che ComfyUI abbia i nodi, o che il server risponda. I pezzi di un batch vengono controllati dopo il batch, quando il server vocale viene restituito, quindi il riconoscimento e il modello vocale non hanno mai bisogno della scheda nello stesso momento.

<a id="shared-speech-servers"></a>

### Server vocali condivisi

In un'installazione con più utenti l'amministratore può tenere server vocali per tutti, così che una macchina con GPU venga configurata una sola volta e nessuno abbia bisogno di un indirizzo o di una chiave. L'amministratore lo fa nello stesso pannello (le ultime sezioni, visibili solo agli amministratori): **Shared speech servers** apre una finestra con gli stessi campi di un backend personale, più l'elenco di chi può usarlo: **all users**, oppure gli utenti scelti nell'elenco. Un interruttore lì, **Users may add speech servers of their own**, decide se i backend sotto sono ancora consentiti.

Cosa ottieni:

- **Backend in use** in cima al pannello elenca i server condivisi che puoi usare (contrassegnati *(shared)*) e i tuoi. La finestra *Convert to audio* e lo studio usano quello che scegli; anche i comandi lo usano.
- Vedi solo il nome di un server condiviso, mai il suo indirizzo, le chiavi o le richieste. I batch di tutti gli utenti su uno stesso indirizzo di server attendono l'uno l'altro, così due utenti non possono avviare due volte la stessa GPU.
- Quando l'amministratore ti toglie un server, un lavoro che lo indica ancora viene rifiutato; scegline un altro.
- Quando l'amministratore disattiva i tuoi server, il pannello lo segnala. Quelli che hai aggiunto vengono conservati e tornano disponibili se l'interruttore viene riattivato.
- La *Narrator voice* di un server condiviso dovrebbe essere un file vocale del server (`predefined:Henry.wav`): un clip di riferimento è una risorsa di un solo utente e gli altri non possono leggerlo. Le voci dei tuoi attori funzionano come sempre.

<a id="actors"></a>

### Attori

Un **actor** è una voce tua: un clip di riferimento (una delle tue [risorse](../resources.md) audio, o uno nuovo caricato qui) oppure un
file vocale del server, con voci proprie per uno **mood** (un sussurro, un grido) o una **language** (un clip che parla
giapponese). La lingua prevale sullo stato d'animo, e senza una voce per essi legge la voce dell'attore. I tuoi attori sono conservati
nel tuo account; qualsiasi libro può assegnare un attore al suo narratore e ai suoi personaggi.

**Anteprima** accanto a una voce (nell'attore, nelle impostazioni e nel cast) riproduce alcune frasi pronunciate con essa (una breve, nella lingua della voce per una lingua); se la GPU contiene altro, il blocco GPU carica prima il server vocale. La freccia accanto offre altri campioni: **with sounds** (un sospiro, una risata, un respiro affannoso) per i modelli che li doppiano (Chatterbox Turbo, Higgs Audio v3), e per Higgs ogni **mood** dell'etichettatura (triste, sussurrato, sarcastico...). Gli attori sono elencati per nome.

Ogni campione occupa la GPU solo per sé. La GPU resta caricata con il server vocale dopo un campione, così i campioni successivi partono subito, finché una richiesta del modello linguistico (o di un altro server) non ha bisogno della GPU. Aprire **Voices and clips** carica già il server vocale, così il suo primo campione non deve attendere.

![Un attore con una voce per uno stato d'animo](../../images/plugin-audiobook-actor.png)
<a id="voices-and-clips"></a>

### Voci e clip

**Voci e clip** (accanto agli attori) è un'unica finestra per i tuoi attori e per i clip da cui vengono clonate le loro voci:

- **Attori**: ciascuno con la sua voce, quante altre voci (stati d'animo, lingue) ha e la qualità peggiore dei suoi clip; aggiungine, modificane o eliminane uno.
- **Clip**: ciascuno con la sua durata, la sua **qualità** e dove è usato (attori, con lo stato d'animo o la lingua, e i narratori dei tuoi backend).
  Seleziona un clip per leggere cosa non va, **Riproduci** per ascoltarlo, **Sostituisci...** per sostituirlo, **Pulisci con ffmpeg** (con ffmpeg) oppure eliminalo (solo se nulla lo usa).
- **Sostituisci...** inserisce al suo posto un altro clip, uno dei tuoi o uno che carichi, in tutti i punti in cui il clip è usato. Le parti vengono rifatte con la nuova voce;
  il vecchio clip resta finché non lo elimini.
- **Testo del clip**: le parole pronunciate nel clip selezionato. Scrivile, oppure premi **Riconosci** per far sì che il [riconoscimento vocale](#speech-recognition)
  le trascriva, correggile e **Salva**. La finestra di dialogo dell'attore mostra lo stesso campo per il clip della voce dell'attore. Una copia pulita e una sostituzione
  ereditano il testo. Higgs lo riceve solo quando il backend lo invia (disattivato per impostazione predefinita).
- **Pulisci con ffmpeg** crea una copia pulita accanto al clip (con meno fruscio e ronzio costanti), misurata di nuovo, da riprodurre e confrontare; sostituisci il clip con essa se
  suona meglio. Non rimuove musica, effetti o altre voci.

<a id="a-good-clip"></a>

#### Un buon clip

Un modello vocale copia la **registrazione**, non solo la voce: la stanza, un fruscio, la musica sotto una scena di film ritornano in ogni battuta, spesso come un leggero
"fruscio" sotto le parole che nessun filtro del risultato elimina. Quindi:

- Usa una registrazione pulita di un solo parlante con pause reali (un campione di un doppiatore, una lettura fatta in casa in una stanza silenziosa), di 12-30 secondi. Un clip tagliato
  da un film o da una serie è il caso peggiore.
- I contrassegni di qualità sono misurati sul clip: **Pulito** (le pause sono almeno 45 dB sotto il parlato), **Discreto** (35-45 dB, oppure più lungo di 30 s,
  limite oltre il quale Chatterbox lo taglia), **Scarso** (suono di sottofondo, sotto i 35 dB; distorto; più corto di 8 s). Un clip che non è pulito viene segnalato quando lo aggiungi.
- Per il risultato migliore pulisci un clip a mano: la riduzione del rumore di [Audacity](https://www.audacityteam.org/) con un profilo del rumore preso da una pausa del
  clip, oppure un rimuovi-voce come [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) per un clip con musica. La pulizia con ffmpeg
  abbassa soltanto un rumore costante: su un clip di film ha guadagnato circa 3 dB nel test e il clip è rimasto *Scarso*.

<a id="converting-new-parts-by-itself"></a>

### Conversione automatica delle nuove parti

*Converti automaticamente le nuove parti in audio* è l'impostazione predefinita per tutti i tuoi libri; vedi [Conversione automatica](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Il cast e la pronuncia di un libro

Entrambi sono conservati con il libro (viaggiano nel suo backup) e si modificano dalla finestra di dialogo **Converti in audio**.

**Cast** indica chi legge: un attore per il **narratore**, uno per **altri parlanti** (chiunque il modello nomini e che il cast non
abbia; vuoto: legge il narratore) e uno per ciascun **personaggio**. Un personaggio ha un nome e altri nomi con cui il testo può chiamarlo (*Tom* per *Tomáš Novák*), separati da virgole; una battuta è attribuita al personaggio quando il suo nome, uno degli altri nomi, oppure una parola del nome che nessun altro personaggio condivide, corrisponde, senza distinguere maiuscole e accenti. **Aggiungi i personaggi del libro** riempie l'elenco con i personaggi del punto di vista e quelli presenti nelle parti.

![Il cast di un libro](../../images/plugin-audiobook-cast.png)

**Pronuncia** è il modo in cui il testo viene scritto *per la voce*; il libro stesso non viene mai modificato. Il **dizionario** sostituisce intere
parole (conta la distinzione tra maiuscole e minuscole) con ciò che viene detto al loro posto: una riscrittura fonetica per un nome o una parola inventata, oppure una lettura in kana per il giapponese. Per un libro in inglese le regole leggono anche ad alta voce altre cose: numeri, ordinali, anni, orari, valute e abbreviazioni come parole, un titolo che è solo un numero come *Chapter four. The Arch* (la parola prima del numero è a tua scelta), i segni markdown come `*` vengono omessi, e un cambio di scena (`***`, `---`) non viene letto ma produce una pausa più lunga. Disattiva le regole per un libro in
un'altra lingua; il dizionario resta comunque applicato. **Prova un testo** mostra come verrà letto un pezzo di testo. Un pezzo il cui testo parlato cambia viene rifatto, gli altri restano invariati.

![La pronuncia di un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Conversione di un libro

*Menu Strumenti dell'editor della storia (il pulsante con gli ingranaggi) → **Converti in audio***.

![La finestra di dialogo prima dell'avvio](../../images/plugin-audiobook-convert.png)

1. Scegli il **backend** e premi **Carica**: vengono elencate le parti con testo del ramo attivo (la prima è la 1) con lo stato di ciascuna:
   *Non generata*, *Etichettata* (le etichette del modello sono salvate, ma non c'è ancora audio), *Generata* (con un pulsante di riproduzione e un download) oppure
   *Generata (il testo è cambiato)* (l'audio è stato creato da un testo precedente e viene rifatto); a una parte che lo studio segna come definitiva si aggiunge *(finale)*.
2. Lascia deselezionata la casella **Etichetta le parti con un modello linguistico** per la versione semplice: il narratore legge ogni paragrafo. Attivala per far decidere al modello chi parla in ogni pezzo di dialogo, come (calmo, sussurro, grido...) e dove va inserito un tag sonoro; il cast fornisce le voci. Il modello del libro è quello scelto; puoi sceglierne un altro se preferisci. Le parti vengono prima etichettate, poi lette. Una parte che ha già etichette per il suo testo viene letta con queste anche quando la casella è disattivata (il modello non viene interpellato), quindi rieseguire un libro etichettato senza la casella non trasforma il suo dialogo in semplice narrazione; **Rimuovi etichette** serve per chiedere una narrazione semplice o nuove etichette.
3. Seleziona le parti (la casella nell'intestazione le seleziona tutte) e **pianifica** cosa farne; la colonna **Piano** lo mostra, ma ancora non succede nulla:
   **Genera** (crea ciò che manca; una parte aggiornata non viene rifatta), **Rigenera** (elimina l'audio, rimuove le etichette, genera), **Elimina audio** (i pezzi e l'audio unito della parte; le etichette restano), **Rimuovi etichette** (il modello viene interpellato di nuovo la prossima volta che la parte viene creata), **Cancella il piano**. I passaggi si sommano su una parte (Elimina audio e Rimuovi etichette senza Genera fanno solo pulizia); una parte segnata come finale resta esclusa da ogni piano.
4. **Avvia** esegue il piano. Quando sta per togliere audio o etichette, chiede prima conferma. Ciò che deve essere generato finisce nella tua coda, che prosegue anche quando chiudi la finestra o lasci la pagina; una parte che attende o è in esecuzione nella coda è esclusa da un piano. Lo stato, i pezzi già creati e il messaggio del lavoro sono mostrati sulla riga della parte.
   Mentre il modello etichetta una parte, la sua riga mostra *Etichettatura pezzi 17-32 di 120* (al modello vengono chiesti circa 16 pezzi alla volta); **clicca sullo stato** di una parte già etichettata per leggere, per ogni richiesta, il ragionamento del modello e la sua risposta, man mano che arrivano. Vengono conservati in memoria insieme al lavoro, finché non si cancellano i lavori conclusi (*Cancella completati*) o non si riavvia l'applicazione.

![La coda: pezzi già creati e, quando una parte è completa, un download](../../images/plugin-audiobook-queue.png)

Come viene creata una parte, così sai cosa aspettarti:

- Il testo è **diviso dal programma**, mai dal modello, in pezzi di narrazione e pezzi di dialogo diretto (tra virgolette doppie dritte o curve oppure tra le parentesi giapponesi angolari; un titolo è narrazione). Il modello si limita a *etichettare* i pezzi numerati. Un testo con tag sonori viene usato solo quando è il pezzo con i tag aggiunti e nient'altro è cambiato, così il modello non può alterare il tuo libro.
- **Chi parla in un pezzo lo decide il modello per il dialogo; la narrazione è affidata al narratore.** Una lettera, un biglietto, un messaggio o i pensieri di un personaggio non hanno virgolette, quindi il programma li considera narrazione. Un pezzo del genere viene letto dal personaggio (la voce del cast) solo quando **inizia in corsivo** (`*...*` o `_..._`) e il modello indica il personaggio dal contesto (una firma, "la calligrafia era inconfondibile"). La narrazione in terza persona, anche quando è vista dal punto di vista di un personaggio ("Lei guardò Percy attraversare la stanza"), è sempre del narratore: il programma ignora il personaggio che il modello assegna a un pezzo senza corsivo. Anche il corsivo usato per enfasi, suoni o parole straniere è narrazione. Le parole sono sempre le tue.
- **L'istruzione per il modello è un template che puoi modificare.** *Impostazioni → Impostazioni dell'estensione → Impostazioni audiolibro → Prompt di etichettatura* è il tuo predefinito; un libro (scheda *Prompt* → *Impostazioni dell'estensione* → *Audiolibro*) o una cartella possono averne uno proprio. Se è vuoto vale quello della cartella superiore, poi il tuo, poi quello integrato. Come gli altri [prompt](../books/prompts.md), è scritto con le variabili `{{characters}}`, `{{deliveries}}`, `{{tags}}` e `{{wordsPerTag}}`, e il popover dei suggerimenti ha **Inserisci il template predefinito**. Un'istruzione modificata fa sì che le etichette delle parti da creare vengano richieste di nuovo la prossima volta che vengono generate.
- **Il volume delle parti viene uniformato** quando è installato il programma ffmpeg (lo stesso che produce il `.m4b`): l'audio unito di ogni parte viene portato a un unico livello di loudness (EBU R128, lineare, così le dinamiche restano; **-18 LUFS** salvo che tu lo modifichi in *Impostazioni → Impostazioni dell'estensione → Impostazioni audiolibro*, dove puoi anche disattivarlo). I pezzi restano come li ha prodotti il server; viene uniformata solo la parte unita, così un audiolibro non cambia bruscamente di volume da una parte all'altra. **Le parti create in precedenza** non vengono toccate finché non le converti di nuovo: così vengono riunite dai pezzi salvati e uniformate, senza chiedere nulla al server vocale (e senza scaricare il modello linguistico). Disattivare l'uniformazione lascia com'erano le parti già uniformate. Se ffmpeg fallisce per una parte, questa conserva l'audio com'era e il risultato lo segnala.
- Tutte le parti di un clic vengono create in **un unico lotto per modello vocale**: le richieste prima del lotto vengono eseguite una volta, poi vengono lette tutte le parti, quindi le richieste dopo il lotto vengono eseguite una volta. È questo che evita che la scheda grafica cambi modello a ogni parte.
- Ogni pezzo viene **conservato in base al suo contenuto** (testo, voce, modello, lingua, impostazioni, seme). Una parte già aggiornata non costa nulla, un paragrafo modificato è l'unico pezzo rifatto, e una parte fallita conserva i pezzi già creati quando premi **Riprova**.
- I pezzi vengono uniti con pause che dipendono dalla loro posizione (dopo un titolo, tra i paragrafi, prima di *disse lui*) in un unico mp3 per parte, conservato insieme alla parte.
- Mentre è in corso una tua generazione, la sintesi vocale attende, così il modello linguistico non viene scaricato sotto di essa.
- Una parte che hai segnato come **finale** nello studio viene lasciata intatta, e la finestra di dialogo indica quante sono.
- **L'intero audiolibro** unisce ogni parte con audio aggiornato in un unico mp3 con un segnalibro di capitolo per ogni parte e mostra un link di download sotto l'elenco (vengono indicate le parti escluse). Lo stesso file è il formato di esportazione *un mp3 con segnalibri di capitolo*.
- **Ferma tutto** annulla le parti in attesa e interrompe subito quella in esecuzione: la richiesta al modello linguistico (l'etichettatura) o al server vocale (il pezzo in lettura) viene abbandonata, senza attenderla. Un lotto fermato prima di iniziare non invia le richieste che lo precedono, così il modello linguistico resta caricato. **Scarica** fornisce l'mp3 di una parte completata.
<a id="the-audiobook-studio"></a>

## Lo studio dell'audiolibro

*Menu Tools → **Audiobook studio*** (la voce del pezzo selezionato, con un'anteprima, si trova accanto al suo altoparlante) è dove si crea l'audiolibro finale, una parte alla volta: vedi lo script, lo modifichi, ascolti un pezzo e crei la parte.

![Lo studio con un discorso selezionato](../../images/plugin-audiobook-studio.png)

- Mentre la parte viene annotata o creata, una barra indica che è in corso un'elaborazione, e i pulsanti che avvierebbero altre operazioni restano in attesa.
- **Annota** crea lo script della parte (con il modello linguistico, se lo chiedi, altrimenti con il narratore) e mostra i pezzi in una tabella: speaker, modalità di lettura, intensità, il testo parlato e un segno per un pezzo che hai modificato. Mentre è in esecuzione compare accanto un pulsante **Interrompi**, che termina la richiesta al modello linguistico.
- Seleziona un pezzo per modificarne lo **speaker**, il **testo parlato**, la **modalità di lettura**, l'**intensità** (l'enfasi della voce, per i modelli che la prevedono; su Higgs la modalità di lettura e l'intensità sono i marcatori come `<|emotion:sadness|>` all'inizio del testo parlato, che li seguono quando li cambi), la **lingua**, il **seed**, la **pausa dopo**. **Applica** conserva la modifica e **blocca** il pezzo: una nuova annotazione lascia invariato un pezzo bloccato.
  **Sblocca** lo restituisce all'annotazione. *The request the speech server gets* mostra la richiesta esatta.
- **Hear** crea l'audio di un pezzo (solo quel pezzo; il server vocale viene avviato per questo); **Another take** lo rifà con un nuovo seed, e i **takes** vengono conservati così puoi scegliere quello che preferisci (*Use this take and hear it*). Una buona voce dipende spesso dal seed.
- **Make the part from this script** unisce i pezzi così come sono, senza una nuova annotazione.
- Lo **stato** della parte è *draft*, *reviewed* o *final*. Una parte *final* non viene più annotata né convertita di nuovo, e l'esportazione può essere limitata alle parti *final*.

<a id="exporting"></a>

## Esportazione

L'audiolibro è un formato di *Esporta la storia* del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** e, quando ffmpeg è installato, **Audiobook (one m4b with chapters)**. Sono composti dall'audio delle parti aggiornate: una parte senza audio, o con audio più vecchio del suo testo o del suo script nello studio, viene esclusa e la finestra ne indica il motivo. I file mp3 contengono tag ID3 (il titolo del libro, il capitolo, la traccia), e il file unico ha segnalibri di capitolo che lettori come VLC e la maggior parte delle app per audiolibri mostrano; lo zip contiene una playlist. La casella **Audiobook: only the parts marked final** limita l'esportazione alle parti che hai completato nello studio.

Il formato **m4b** è quello che Apple Books e la maggior parte delle app per audiolibri sul telefono richiedono: audio AAC (64 kbit/s, sufficiente per il parlato) in un contenitore MP4 con i capitoli, il titolo, l'autore e il genere. A crearlo è ffmpeg, quindi il formato è offerto solo su un server che ha ffmpeg. L'audio memorizzato come mp3 viene codificato una seconda volta per questo formato; l'audio memorizzato come wav viene codificato una sola volta.

<a id="how-the-audio-is-stored"></a>

### Come viene memorizzato l'audio

Un amministratore lo sceglie per l'intera installazione in *Settings → Extension Settings → Audiobook Settings* (l'ultima sezione, visibile solo agli amministratori):

- **mp3** (predefinito): file piccoli, uniti senza un decoder, non serve ffmpeg. L'm4b è un secondo passaggio con perdita di qualità.
- **wav**: senza perdita di qualità, circa dieci volte più grande (un'ora di parlato occupa circa 170 MB), richiede ffmpeg. I pezzi vengono richiesti al server vocale in wav e uniti in wav; il browser li riproduce così come sono;
  le esportazioni mp3 e zip vengono codificate da ffmpeg (96 kbit/s) e l'm4b viene codificato una sola volta.

La scelta vale per ciò che viene creato da quel momento in poi. Le parti create prima conservano il loro audio e vengono gestite così come sono; una parte di entrambi i tipi (un pezzo bloccato dello studio precedente) viene unita in wav, e un'esportazione di parti di entrambi i tipi funziona, ma richiede ffmpeg.

Un'esportazione dell'intero libro viene composta a partire dai file memorizzati delle parti, in file temporanei del server, e scaricata da lì; quindi la sua lunghezza non è limitata dalla memoria dell'applicazione, ma la cartella temporanea deve avere spazio per circa il doppio dell'audio del libro (con wav, circa 350 MB per ora) durante la creazione.

![La finestra di esportazione con i formati audiolibro](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversione automatica

Un libro può creare da solo l'audio di una nuova parte, non appena la parte è *final*: dopo l'annotazione automatica, la riscrittura e le traduzioni, se il libro le ha, come ultimo passaggio di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part) (nel pannello delle esecuzioni fino alla creazione dell'audio; il suo **Interrompi** toglie la parte dalla coda). È **disattivata** per impostazione predefinita. Scegli l'opzione nella scheda *Prompt* del libro, nelle impostazioni dell'estensione, per una cartella di libri, oppure per tutti i tuoi libri nelle impostazioni dell'audiolibro; vince quella più vicina:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; un libro senza modello riceve la narrazione semplice, e ti viene segnalato.

![L'impostazione in un libro](../../images/plugin-audiobook-auto.png)

La parte entra nella tua coda, con il cast e la pronuncia del libro. Una notifica ti dice che è in coda e un'altra che il suo audio è pronto o che è fallito; compaiono solo nella pagina che ha avviato la generazione, mentre il processo è comunque nella finestra *Convert to audio*.

<a id="listening"></a>

## Ascolto

Un libro pubblicato (o il lettore) che ha parti con audio riceve una barra del lettore: riproduci, metti in pausa, interrompi, velocità, e la parte in ascolto è evidenziata nel testo. Parte dalla selezione o dalla prima parte visibile, riproduce le parti in ordine e recupera la successiva mentre quella corrente viene riprodotta, e i comandi multimediali del dispositivo riconoscono il libro. L'audio creato da una versione più vecchia del testo di una parte non viene offerto. L'audio è servito al proprietario del libro e, finché il libro è pubblicato, a chiunque possa aprirlo; un libro ritirato dalla pubblicazione smette di essere servito subito (alla parte successiva, quando il lettore la carica).

![Il lettore nel reader](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte senza audio

Il lettore si ferma su una parte senza audio, così una parte della storia non viene saltata, e lo segnala. Quando l'estensione [Read Aloud](tts-reader.md) è installata, la barra mostra anche un pulsante con l'altoparlante, **Read this part (and the next ones without audio) with the voice of the browser**. Non viene letto nulla con una voce artificiale finché non lo premi. Legge quella parte e le parti successive senza audio con la voce del tuo dispositivo, e quando arriva a una parte con audio il lettore riprende il controllo. Le parti successive senza audio nello stesso ascolto vengono lette allo stesso modo senza chiedere di nuovo; **Interrompi** la silenzia, e riavviare il lettore chiede di nuovo. La voce, la velocità e il tono sono quelli di Read Aloud.
<a id="commands"></a>

## Comandi

Gli stessi strumenti dei [comandi](../commands/index.md) (la riga di comando dell'editor della storia, la macro `command` di un modello, un agente). Le parti sono numerate come le numera la finestra di dialogo Convert, oppure si indicano con il loro nome breve.

| Comando | Cosa fa |
|---|---|
| `/audiobook-status [from [to]]` | Mostra il server vocale in uso, il modo in cui il libro si converte da solo, quante parti hanno l'audio (nessuna, pronto, più vecchio del testo, più vecchio dello script), lo stato di ogni parte elencata nello studio e la coda. È lento solo se il libro è lungo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Mette le parti in coda (l'intero libro senza argomenti, una sola parte se si indica solo `from`). `labelling` `true` usa il modello del libro; `what` è `audio` oppure `script` (solo lo script per lo studio, nulla viene letto ad alta voce). Con `wait` `true` il comando resta attivo finché i lavori non sono terminati, mostra a che punto sono e indica come si è concluso ciascuno; interromperlo annulla i lavori non ancora iniziati. |
| `/audiobook-stop` | Annulla le parti in attesa; quella in esecuzione si ferma dopo il suo pezzo. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` nello studio; la parte richiede uno script. |
| `/audiobook-cast` | Il narratore, il ripiego e i personaggi del cast con i rispettivi attori. |

![Il risultato di un comando](../../images/plugin-audiobook-command.png)

L'audio viene creato per **il tuo** libro, non per la copia di lavoro nascosta di un [agente](../agents.md): un agente può leggere il cast, mentre per gli altri comandi riceve un rifiuto che gli ricorda che devi eseguirli tu. Eseguili dalla riga di comando, oppure affida a un agente il compito di preparare il libro (annotare, rivedere) e convertilo tu stesso.

<a id="notifications"></a>

## Notifiche

Quando un gruppo di conversioni è terminato, il browser può avvisarti ([notifiche](../account.md#notifications), così puoi lasciare la scheda mentre il server vocale lavora). Nell'elenco delle notifiche della pagina *Impostazioni* ci sono due voci: **Convert to audio** (i gruppi che avvii dalla finestra di dialogo o con il comando) e **Convert to audio (automatic)** (una nuova parte che è stata trasformata in audio automaticamente). Un gruppo che hai interrotto non genera notifiche; un gruppo con una parte fallita lo segnala come fallito.

![L'elenco delle notifiche](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Cosa non fa (ancora)

- Solo mp3 e wav (niente opus); il volume dei pezzi è quello che forniscono le voci.
- Nessun effetto sonoro né ambiente.
- Due motori vocali, Chatterbox e Higgs Audio v3 (in ComfyUI).
- L'ascolto di una voce al di fuori di un segmento.
- Il cast non fa parte del backup di un libro dei tuoi *attori*: un libro ripristinato mantiene il suo cast, ma indica attori che devono esistere nel tuo account.
