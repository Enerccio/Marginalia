---
label: Audiolibro
order: 27
---

# Audiolibro

Trasforma un libro in un audiolibro, parte per parte, con un server di sintesi vocale: una voce per il narratore e una per ogni personaggio, conservate
insieme alle parti del libro e riprodotte nel lettore. Puoi lasciarlo andare da solo (il modello indica chi parla e come, il server di sintesi
legge) per una bozza veloce, oppure lavorare sulle parti una alla volta nello **Studio audiolibro**, vedendo esattamente cosa viene inviato al
server di sintesi e modificandolo a mano, ed esportare il risultato come file mp3 con i segnalibri di capitolo.

L'estensione non genera il parlato da sola: comunica con un **Chatterbox-TTS-Server** che gestisci tu (vedi
[Configurare il server di sintesi](audiobook-server.md)). Il testo del libro non viene mai modificato e non viene inviato a nessuno, tranne ai server
che hai indicato; l'audio viene salvato nella tua installazione, come risorse collegate alle parti.

<a id="what-you-need"></a>

## Cosa serve

- Un server di sintesi e, se condivide la scheda grafica con il tuo modello linguistico, le richieste che le fanno spazio e poi la fermano
  ([Configurare il server di sintesi](audiobook-server.md)).
- Voci: una breve **clip di riferimento** (da 12 a 30 secondi di una sola persona, senza musica) per ogni voce che vuoi clonare, oppure uno dei file
  vocali che il server possiede. Un narratore ha bisogno di una voce di lettura calma e costante.
- Per un libro con più voci, un modello linguistico capace di etichettare il testo. Di default è il modello del libro.
- **ffmpeg** è facoltativo. Senza di esso tutto funziona con mp3. Con esso sul server (è incluso nell'immagine Docker; su un desktop viene trovato in `PATH` o nelle cartelle abituali
  come `/opt/homebrew/bin`; la variabile `MARGINALIA_FFMPEG` ne indica il percorso se si trova altrove) l'esportazione ottiene il formato **m4b**, e un amministratore può salvare l'audio in formato wav.

<a id="the-settings"></a>

## Le impostazioni

*Impostazioni → Impostazioni estensioni → Impostazioni audiolibro* è il pannello del tuo account; salva la pagina *Impostazioni* dopo averlo modificato.

![Il pannello con un backend e gli attori](../../images/plugin-audiobook-settings.png)

<a id="backends"></a>

### Backend

Un backend è un server di sintesi e il modello con cui parla. Aggiungine uno con **+**. **Duplica** copia quello selezionato (indirizzo, chiavi, blocco GPU,
voce, valori predefiniti e le richieste prima e dopo un batch) con un nuovo nome: il modo rapido per avere un backend di un altro modello sulla stessa macchina,
per esempio Higgs accanto a Chatterbox, dove poi cambi il modello, l'indirizzo e le richieste di avvio e arresto.

| Impostazione | Cosa fa |
|---|---|
| Nome | Il nome mostrato nella finestra di conversione. |
| Indirizzo | L'indirizzo base del server di sintesi, o di ciò che inoltra verso di esso, per esempio `http://localhost:8004`. |
| Chiave API | Inviata come token bearer se il server (o il tuo proxy) la richiede. Viene memorizzata cifrata e non viene più mostrata; inseriscine una nuova per sostituirla, oppure usa **Rimuovi la chiave**. |
| Modello di sintesi | **Chatterbox Turbo** (inglese, con tag sonori come `[sigh]`, il più veloce e predefinito), **Chatterbox** (inglese, con un'*esagerazione* dell'interpretazione), **Chatterbox Multilingual** (23 lingue) oppure **Higgs Audio v3 in ComfyUI** (molte lingue, voci che seguono lo stato d'animo di una battuta e suoni come pianto, affanno o un gemito; vedi [sotto](#higgs-audio-v3-emotions-and-sounds)). Il [blocco GPU](../administration/gpus.md) del backend (o le richieste prima di un batch) deve lasciare questo modello caricato. Un libro che richiede un altro modello usa un altro backend. |
| Blocco GPU | Il [blocco GPU](../administration/gpus.md) della GPU su cui gira il server, quando la condivide con il modello linguistico o con altri server (anche con il [Generatore di immagini](image-generator.md)): ogni batch e ogni campione attende il proprio turno sulla GPU e il blocco carica per primo il server di sintesi. Mostrato solo se l'amministratore ha configurato i blocchi GPU. |
| Il server è su questa macchina o sulla mia rete | Consente indirizzi della rete locale. Lascialo disattivato per un server nel cloud: in questo modo non può essere fatto chiamare la tua macchina. |
| Timeout di una richiesta | Quanto attendere per un singolo pezzo di parlato. |
| Riavvia il server dopo (pezzi) | Per un server di sintesi che perde memoria e viene terminato dal sistema in un batch lungo: dopo questo numero di pezzi, le richieste successive al batch e poi quelle precedenti vengono eseguite di nuovo nel mezzo del batch (arresto, avvio, caricamento), e il lavoro prosegue con un server nuovo. 0 (il predefinito) non lo fa mai. Un server che si blocca viene riavviato comunque, qualunque cosa dica questo valore; vedi [Risoluzione dei problemi](audiobook-server.md#troubleshooting). |
| Chiave delle richieste attorno a un batch | La chiave che le richieste prima e dopo un batch inviano come `Authorization: Bearer`. Viene memorizzata cifrata e non viene più mostrata (**Rimuovi la chiave** la cancella), così non deve essere scritta negli header di ogni richiesta, dove sarebbe leggibile. Vuota: viene usata la *Chiave API* sopra, che è ciò che vuoi quando un solo proxy come LiteLLM serve tutto. Una richiesta che ha un header `Authorization` proprio lo mantiene. |
| Voce del narratore | Il narratore di un libro senza cast. Un file vocale del server si scrive `predefined:Henry.wav`. |
| Precisione | Solo per Higgs: la precisione con cui il modello calcola. **fp32** (il predefinito) è la scelta sicura: su una scheda AMD **bf16** trasformava i pezzi in parole senza senso, effetti sonori e voci sbagliate. bf16 richiede circa la metà della memoria grafica (circa 8 GB invece di 16) ed è più veloce dove funziona. |
| Invia il testo della clip a Higgs | Solo per Higgs, sperimentale: il [testo della clip](#voices-and-clips) viene inviato insieme ad essa. Lascialo disattivato: nei test cambiava la voce. |
| Controlla la durata dei pezzi | Attivo per impostazione predefinita. Un pezzo molto più lungo del suo testo (4 s più 0,6 s per parola: il modello continuava a parlare, o produceva un suono al posto delle parole) viene rigenerato con il seed successivo, fino a **Tentativi** volte; viene mantenuta la prima versione di durata corretta, altrimenti la più breve. |
| Controlla le parole con il riconoscimento vocale | Disattivato per impostazione predefinita; richiede il [riconoscimento vocale](#speech-recognition). Dopo ogni batch ogni pezzo viene trascritto e confrontato con il suo testo. Un pezzo che si discosta di più della **Max. errori di parola (%)** delle sue parole viene rigenerato con il seed successivo e ricontrollato, fino a **Tentativi** round; viene mantenuta la versione con meno parole sbagliate. La conversione indica quanti pezzi sono stati rigenerati e quanti differiscono ancora. |
| Valori predefiniti di un segmento | Temperatura, peso CFG, velocità e dimensione dei blocchi con cui parte un pezzo (lo studio può modificarli per un singolo pezzo). Higgs usa solo la temperatura. |
| Prima di un batch, Dopo un batch | Richieste eseguite attorno a ogni batch di parti (e a ogni campione), all'interno del blocco GPU. Liberare la scheda grafica e avviare il server di sintesi è compito del blocco GPU; vedi [Una GPU per il modello e per il parlato](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vuoto non fa nulla. |

**Testa connessione** esegue ciò che fa un batch: acquisisce il blocco GPU (così un server di sintesi fermo viene avviato), esegue le richieste prima di un batch, chiede al server se è attivo e se ha un modello caricato,
ed esegue le richieste dopo il batch. Senza blocco o richieste si limita a interrogare il server.

Un libro che contiene pezzi in un'altra scrittura (giapponese in un libro inglese) li invia al modello multilingue, se hai un backend
per esso, con la loro lingua; restano sul modello del libro se non ne hai uno. Un libro Higgs li conserva: Higgs li legge da solo, con la stessa voce.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: emozioni e suoni

Con il backend **Higgs Audio v3 in ComfyUI** si sentono le etichette del modello linguistico. L'indirizzo del backend è quello del tuo ComfyUI
(con i nodi TTS Audio Suite, vedi [Configurare il server vocale](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **resa** di un pezzo di parlato è recitata: triste, arrabbiata, gioiosa, divertita, tesa, impaurita, sorpresa, tenera, sarcastica, supplichevole, disperata,
  disgustata, eccitata, un sussurro o un grido. Una resa forte (intensità 0,8 e oltre) è più espressiva, una debole (sotto 0,2) è letta in modo semplice.
  Il narratore legge in modo semplice.
- Il modello linguistico può inserire più **suoni** nel parlato rispetto a Chatterbox Turbo: risata, risatina, ridacchio, sospiro, sussulto, colpo di tosse, schiarimento di gola,
  tiro su col naso, starnuto, grugnito, pianto, singhiozzo, urlo, affanno, lamento, mugolio e zittio.
- Il programma legge anche la **narrazione attorno a ogni parlato** (in inglese) e assegna al parlato il suono che essa nomina, quando il modello non ne ha messo nessuno:
  *"You look funny," she giggled.* ottiene una risatina, *She burst out laughing. "Careful!"* una risata, *a soft moan escaped her lips. "Yes..."* un lamento,
  *panting* o *out of breath* un affanno, *cried quietly* o *burst into tears* un pianto. Lascia fuori ciò che sembra soltanto un suono: *"Stop!" she cried.*
  (un'esclamazione), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, e un suono che la narrazione
  attribuisce a un altro personaggio nominato. *"You look funny," he mocked her.* ottiene una resa sarcastica o divertita e nessun suono. La narrazione stessa è letta come scritta.
- **Il narratore è sempre letto in modo semplice**: nessun suono e nessun umore, anche per un parlato che il modello ha lasciato al narratore.
- Higgs non richiede un clip per ogni umore: basta un clip di una voce. Ne copia la registrazione, quindi una registrazione pulita conta più che con Chatterbox
  ([Un buon clip](#a-good-clip)).
- Ogni pezzo è prodotto da ComfyUI e la sua memoria viene svuotata dopo (alcuni secondi per pezzo), il che evita che alcune schede grafiche si blocchino.
- Higgs spesso continua oltre l'ultima parola di un pezzo (un respiro, l'inizio di un altro suono) e il clip finisce a metà di esso, cosa che si sente come un breve "fruscio" prima della pausa. Con l'estensione **ffmpeg** il tratto finale viene tagliato e ogni pezzo sfuma in uscita; senza di essa i pezzi restano come li ha prodotti Higgs.
- Il modello Higgs Audio v3 è distribuito con una **licenza di ricerca e non commerciale**: un audiolibro che vendi richiede una licenza di Boson AI.

<a id="speech-recognition"></a>

### Riconoscimento vocale

Il **riconoscimento vocale** scrive ciò che dice un pezzo di parlato. Controlla i pezzi di un lotto (quando un backend ha **Check the words with speech
recognition** attivo) e riconosce il [testo di un clip vocale](#voices-and-clips). Scegli dove viene eseguito:

| Impostazione | Cosa fa |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)**: il modello vocale Granite della TTS Audio Suite nel tuo ComfyUI (l'indirizzo è quello di ComfyUI). **OpenAI-compatible (whisper)**: qualsiasi server con `/v1/audio/transcriptions`, come un server whisper (faster-whisper, speaches) o l'API di OpenAI. |
| Address, Api key | L'indirizzo di ComfyUI, o la base dell'API (con o senza `/v1`); la chiave è memorizzata cifrata. Indirizzo vuoto: nessun riconoscimento vocale. |
| GPU lock | Il [blocco GPU](../administration/gpus.md) che usa il riconoscimento. Su una sola scheda con Higgs in fp32, assegnagli un blocco proprio i cui passi riavviano ComfyUI: ComfyUI tiene il modello vocale nella memoria del server, e caricare il riconoscimento accanto ad esso ha esaurito la memoria del server. |
| Model of the recognition | Vuoto: `granite-speech-4.1-2b` per ComfyUI, `whisper-1` per quello compatibile con OpenAI. |
| Timeout, The server is on this machine or on my network | Come per un backend. |

**Test connection** verifica che ComfyUI abbia i nodi, oppure che il server risponda. I pezzi di un lotto vengono controllati dopo il lotto, quando il server vocale viene restituito, così il riconoscimento e il modello vocale non hanno mai bisogno della scheda nello stesso momento.

<a id="cleanup-of-clips"></a>

### Pulizia dei clip

**Cleanup of clips** pulisce un clip di riferimento su un ComfyUI con la [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (la stessa
usata per i lavori con Higgs). Il risultato è una nuova **version** del clip in [Voices and clips](#voices-and-clips); il clip originale resta.

| Impostazione | Cosa fa |
|---|---|
| Address, Api key | L'indirizzo di ComfyUI; la chiave è memorizzata cifrata. Indirizzo vuoto: nessuna pulizia. |
| GPU lock | Il [blocco GPU](../administration/gpus.md) che usa la pulizia. Assegnagli un blocco proprio, non quello del parlato o del riconoscimento vocale, i cui passi riavviano ComfyUI, in modo che i modelli di pulizia non vengano mai caricati accanto al modello vocale. |
| Timeout, The server is on this machine or on my network | Come per un backend (il timeout vale per una singola pulizia, 10 minuti per impostazione predefinita: ComfyUI scarica un modello la prima volta che viene usato). |
| Cleanup | Le pulizie (preset) tra cui scegliere in Voices and clips, per nome. Ciascuna è un elenco di passi eseguiti uno dopo l'altro. **Default cleanups** ripristina quelle incluse. |

Un passo è uno dei seguenti:

- **Separation model**: un modello della suite che divide l'audio in due, la parte *extracted* e la parte *remaining*, e ne tiene una. Quale parte contiene la voce dipende dal modello: un modello per le voci (`MelBandRoformer`) estrae la voce, un modello di riduzione del rumore estrae il rumore, quindi tieni la parte *remaining*. **Aggressiveness** (da 0 a 20) decide quanto viene tolto.
- **Voice fixer**: ripristina una voce degradata (un telefono, una vecchia registrazione). Cambia il suono della voce più di quanto faccia una separazione.
- **ffmpeg (here)**: il filtro di **Clean with ffmpeg**, eseguito dall'applicazione tra i passi su ComfyUI.

Le pulizie incluse sono: *Voice from music* (la voce tolta da un film o una canzone), *Remove noise*, *Restore the voice* e *Film or game clip* (la
voce tolta dalla musica, poi il rumore rimosso). **Test connection** verifica che ComfyUI abbia i nodi e riempie l'elenco dei modelli con quelli del tuo
server. I modelli vengono scaricati da ComfyUI la prima volta che una pulizia li usa.

<a id="shared-speech-servers"></a>

### Server vocali condivisi

In un'installazione con più utenti l'amministratore può tenere dei server vocali per tutti, così che una macchina con GPU venga configurata una sola volta e nessuno abbia bisogno di un indirizzo o di una chiave. L'amministratore lo fa nello stesso pannello (le ultime sezioni, visibili solo agli amministratori): **Shared speech servers** apre una finestra con gli stessi campi di un backend proprio, più l'indicazione di chi può usarlo: **all users**, oppure gli utenti scelti nell'elenco. Un interruttore lì, **Users may add speech servers of their own**, decide se i backend propri sono ancora consentiti.

Cosa ottieni:

- **Backend in use** in cima al pannello elenca i server condivisi che puoi usare (contrassegnati *(shared)*) e i tuoi. La finestra *Convert to audio* e lo studio usano quello che scegli; anche i comandi lo usano.
- Vedi solo il nome di un server condiviso, mai il suo indirizzo, le chiavi o le richieste. I lotti di tutti gli utenti su uno stesso indirizzo di server attendono l'uno l'altro, così due utenti non possono avviare la stessa GPU due volte.
- Quando l'amministratore ti toglie un server, un lavoro che lo indica ancora viene rifiutato; scegline un altro.
- Quando l'amministratore disattiva i tuoi server, il pannello lo segnala. Quelli che hai aggiunto vengono conservati e tornano in uso se l'interruttore viene riacceso.
- La *Narrator voice* di un server condiviso dovrebbe essere un file vocale del server (`predefined:Henry.wav`): un clip di riferimento è una risorsa di un solo utente e gli altri non possono leggerla. Le voci dei tuoi attori funzionano come sempre.
<a id="actors"></a>

### Attori

Un **attore** è una tua voce: un campione di riferimento (una delle tue [risorse](../resources.md) audio, oppure uno nuovo caricato qui) o un
file di voce del server, con voci proprie per un **stato d'animo** (un sussurro, un grido) o una **lingua** (un campione che parla
giapponese). La lingua prevale sullo stato d'animo, e in assenza di una voce per loro legge la voce dell'attore. I tuoi attori sono conservati
nel tuo account; ogni libro può assegnare un attore al proprio narratore e ai propri personaggi.

**Anteprima** accanto a una voce (nell'attore, nelle impostazioni e nel cast) riproduce alcune frasi pronunciate con quella voce (una breve, nella lingua della voce, per una lingua); se la GPU è occupata da altro, il blocco della GPU carica prima il server vocale. La freccia accanto offre altri campioni: **con suoni** (un sospiro, una risata, un ansito) per i modelli che li sanno riprodurre (Chatterbox Turbo, Higgs Audio v3), e per Higgs ogni **stato d'animo** dell'etichettatura (triste, sussurrato, sarcastico...). Gli attori sono elencati per nome.

Ogni campione occupa la GPU solo per sé. La GPU resta caricata con il server vocale dopo un campione, così i campioni successivi partono subito, finché una richiesta del modello linguistico (o di un altro server) non ha bisogno della GPU. Aprire **Voci e clip** carica già il server vocale, quindi il suo primo campione non deve attendere.

![Un attore con una voce per uno stato d'animo](../../images/plugin-audiobook-actor.png)

<a id="voices-and-clips"></a>

### Voci e clip

**Voci e clip** (accanto agli attori) è un'unica finestra per i tuoi attori e per i clip da cui le loro voci sono clonate. Ha tre schede, affiancate all'inizio: **Attori**, **Clip** e **Clip**. Trascina una scheda per metterla accanto, sotto o insieme a un'altra; la finestra si riapre come l'avevi lasciata.

- **Attori**: ciascuno con la sua voce, quante altre voci (stati d'animo, lingue) possiede e la qualità peggiore dei suoi clip; aggiungi, modifica o elimina un attore.
- **Clip**: i tuoi clip in **cartelle** (per esempio una per personaggio), con le **versioni** ricavate da un clip sotto di esso, ciascuna con la sua durata, la sua **qualità** e dove è usata (attori, con lo stato d'animo o la lingua, e i narratori dei tuoi backend). **Aggiungi un clip** ne carica uno.
- **Clip**: il clip selezionato.
  - **Nome** e **Cartella**: rinomina il clip (il file mantiene il suo nome) e mettilo in una cartella, una nuova digitandone il nome. Una versione resta nella cartella del clip da cui è stata ricavata e indica da cosa è stata ricavata e come.
  - **Riproduci** il clip, leggi cosa non va, **Sostituisci...** o **Elimina** il clip (solo quando nulla lo usa; le sue versioni passano al clip da cui sono state ricavate).
  - **Pulisci con ffmpeg** crea una versione con meno fruscio e ronzio costanti. Non rimuove musica, effetti o altre voci.
  - **Pulisci con AI** crea una versione con la [pulizia](#cleanup-of-clips) scelta su ComfyUI. Viene eseguita in background: puoi continuare a lavorare, e un messaggio segnala quando la versione è pronta.
  - **Usa questa versione** inserisce una versione dovunque sia usato il clip da cui è stata ricavata. Le parti vengono rifatte con la nuova voce, e il vecchio clip resta finché non lo elimini.
  - **Testo del clip**: le parole pronunciate nel clip. Scrivile, oppure premi **Riconosci** per far trascrivere il [riconoscimento vocale](#speech-recognition), correggile e **Salva**. La finestra dell'attore mostra lo stesso campo per il clip della voce dell'attore. Una versione e una sostituzione ereditano il testo. Higgs lo riceve solo quando il backend lo invia (disattivato per impostazione predefinita).
- **Sostituisci...** mette un altro clip, uno dei tuoi o uno che carichi qui, dovunque sia usato il clip.

I selettori di una voce (dell'attore, del narratore di un backend) mostrano un clip come *cartella / clip › versione*.

<a id="a-good-clip"></a>

#### Un buon clip

Un modello vocale copia la **registrazione**, non solo la voce: la stanza, un fruscio, la musica sotto una scena di film tornano in ogni battuta, spesso come un leggero "fruscio" sotto le parole che nessun filtro del risultato rimuove. Quindi:

- Usa una registrazione pulita di un solo parlante con vere pause (il campione di un doppiatore, una lettura fatta in casa in una stanza silenziosa), di 12 a 30 secondi. Un clip tagliato da un film o da una serie è il caso peggiore.
- I marchi di qualità sono misurati dal clip: **Pulito** (le pause sono almeno 45 dB sotto il parlato), **Discreto** (da 35 a 45 dB, oppure più lungo di 30 s, che Chatterbox taglia), **Scarso** (suono di sottofondo, sotto 35 dB; distorto; più breve di 8 s). Un clip che non è pulito viene segnalato quando lo aggiungi.
- Pulisci un clip con l'AI ([Pulizia dei clip](#cleanup-of-clips)): nel test la pulizia *Clip da film o gioco* ha portato un clip con fruscio, ronzio ed eco da *Scarso* a *Pulito* (le pause sono passate da 38 a 89 dB sotto il parlato). Ascolta la versione prima di usarla: un modello di separazione può anche portare via un respiro o la fine di una parola sussurrata. La pulizia con ffmpeg abbassa solo un rumore costante: su un clip da film ha guadagnato circa 3 dB e il clip è rimasto *Scarso*.
- Senza ComfyUI, pulisci un clip a mano: la riduzione del rumore di [Audacity](https://www.audacityteam.org/) con un profilo del rumore preso da una pausa del clip, oppure un rimuovi-voce come [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) per un clip con musica.

<a id="converting-new-parts-by-itself"></a>

### Convertire le nuove parti da sola

*Converti le nuove parti in audio da sola* è l'impostazione predefinita per tutti i tuoi libri; vedi [Conversione automatica](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## Il cast e la pronuncia di un libro

Entrambi sono conservati con il libro (viaggiano nel suo backup) e si modificano dalla finestra **Converti in audio**.

**Cast** indica chi legge: un attore per il **narratore**, uno per gli **altri parlanti** (chiunque il modello nomini e che il cast non abbia; vuoto: legge il narratore) e uno per ogni **personaggio**. Un personaggio ha un nome e altri nomi con cui il testo può chiamarlo (*Tom* per *Tomáš Novák*), separati da virgole; una battuta è assegnata al personaggio quando il suo nome, uno degli altri nomi, oppure una parola del nome che nessun altro personaggio condivide, corrisponde, ignorando maiuscole e accenti. **Aggiungi i personaggi del libro** riempie l'elenco con i personaggi di punto di vista e presenti nelle parti.

![Il cast di un libro](../../images/plugin-audiobook-cast.png)

**Pronuncia** è il modo in cui il testo viene scritto *per la voce*; il libro in sé non viene mai modificato. Il **dizionario** sostituisce intere parole (conta maiuscole e minuscole) con ciò che va detto per esse: una riscrittura fonetica per un nome o una parola inventata, oppure una lettura in kana per il giapponese. Per un libro in inglese le regole leggono ad alta voce anche altre cose: numeri, ordinali, anni, orari, somme di denaro e abbreviazioni come parole, un titolo che è solo un numero come *Chapter four. The Arch* (la parola prima del numero la scegli tu), i segni markdown come `*` vengono eliminati, e un cambio di scena (`***`, `---`) non viene letto ma diventa una pausa più lunga. Disattiva le regole per un libro in un'altra lingua; il dizionario si applica comunque. **Prova un testo** mostra come verrà letto un pezzo di testo. Un pezzo il cui testo parlato cambia viene rifatto, gli altri restano.

![La pronuncia di un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertire un libro

*Menu Tools dell'editor della storia (il pulsante con gli ingranaggi) → **Convert to audio***.

![La finestra di dialogo prima dell'avvio](../../images/plugin-audiobook-convert.png)

1. Scegli il **backend** e premi **Load**: vengono elencate le parti con testo del ramo attivo (la prima è la 1), con lo stato di ciascuna:
   *Not generated*, *Labelled* (le etichette del modello sono memorizzate, ancora nessun audio), *Generato* (con un pulsante di riproduzione e un download) oppure
   *Generated (the text changed)* (l'audio è stato creato da un testo precedente e viene creato di nuovo); *(final)* viene aggiunto a una parte che lo studio contrassegna come definitiva.
2. Lascia **Label the parts with a language model** disattivato per la versione semplice: il narratore legge ogni paragrafo. Attivalo per far decidere al modello chi pronuncia ogni battuta di dialogo, in che modo (calmo, sussurro, grido...) e dove inserire un tag sonoro, mentre il cast fornisce le voci. Il modello del libro è scelto; sceglierne un altro se preferisci. Le parti vengono prima etichettate, poi lette ad alta voce. Una parte che ha già etichette per il suo testo viene letta anche con quelle quando la casella è disattivata (il modello non viene interpellato), quindi rieseguire un libro etichettato senza la casella non trasforma i suoi dialoghi in semplice narrazione; **Remove labels** serve a chiedere una narrazione semplice o nuove etichette.
3. Spunta le parti (la casella nell'intestazione le spunta tutte) e **pianifica** cosa farne; la colonna **Piano** lo mostra, ma per ora non si fa nulla:
   **Genera** (crea ciò che manca; una parte aggiornata non viene creata di nuovo), **Rigenera** (elimina l'audio, rimuove le etichette, genera), **Delete audio** (i pezzi e l'audio unito della parte; le etichette restano), **Remove labels** (il modello viene interpellato di nuovo la volta successiva in cui la parte viene creata), **Clear the plan**. I passi si sommano su una parte (Delete audio e Remove labels senza Genera fanno solo pulizia); una parte contrassegnata come definitiva è esclusa da ogni piano.
4. **Avvia** esegue il piano. Quando toglie audio o etichette, chiede prima conferma. Ciò che va generato entra nella tua coda, che continua a funzionare quando chiudi la finestra e quando lasci la pagina; una parte che attende o è in esecuzione nella coda è esclusa da un piano. Lo stato, i pezzi già prodotti e il messaggio del lavoro sono mostrati nella riga della parte.
   Mentre il modello etichetta una parte, la sua riga mostra *Labelling pieces 17-32 of 120* (il modello viene interpellato per 16 pezzi alla volta); **fai clic sullo stato** di una parte etichettata per leggere, per ogni richiesta, il ragionamento del modello e la sua risposta, man mano che arrivano. Sono conservati in memoria insieme al lavoro, finché i lavori terminati non vengono cancellati (*Clear finished*) o l'applicazione non viene riavviata.

![La coda: pezzi già prodotti e un download quando una parte è completata](../../images/plugin-audiobook-queue.png)

Come viene creata una parte, così sai cosa aspettarti:

- Il testo viene **diviso dal programma**, mai dal modello, in pezzi di narrazione e pezzi di dialogo diretto (tra virgolette doppie dritte o curve, oppure tra le parentesi angolari giapponesi; un titolo è narrazione). Il modello si limita a *etichettare* i pezzi numerati. Un testo con tag sonori viene usato solo quando è il pezzo con i tag aggiunti e nient'altro è cambiato, così il modello non può mai modificare il tuo libro.
- **Chi parla in un pezzo di dialogo è deciso dal modello; la narrazione è del narratore.** Una lettera, un biglietto, un messaggio o i pensieri di un personaggio non hanno virgolette, quindi il programma li considera narrazione. Un pezzo del genere viene letto dal personaggio (la voce del cast) solo quando **inizia in corsivo** (`*...*` o `_..._`) e il modello indica il personaggio dal contesto (una firma, "la calligrafia era inconfondibile"). La narrazione in terza persona, anche quando è raccontata dal punto di vista di un personaggio ("Lei guardava Percy attraversare la stanza"), è sempre del narratore: il programma ignora il personaggio che il modello assegna a un pezzo senza corsivo. Anche il corsivo per enfasi, suoni o parole straniere è narrazione. Le parole restano sempre le tue.
- **L'istruzione per il modello è un template che puoi modificare.** *Settings → Impostazioni dell'estensione → Audiobook Settings → Labelling prompt* è il tuo predefinito; un libro (scheda *Prompts* → *Impostazioni dell'estensione* → *Audiobook*) o una cartella possono averne uno proprio. Se è vuoto, vale quello della cartella superiore, poi il tuo, poi quello integrato. È scritto come gli altri [prompt](../books/prompts.md): le variabili sono `{{characters}}`, `{{deliveries}}`, `{{tags}}` e `{{wordsPerTag}}`, e il popover dei suggerimenti ha **Inserisci il template predefinito**. Un'istruzione modificata fa sì che le etichette delle parti vengano richieste di nuovo la volta successiva in cui vengono create.
- **Il volume delle parti è livellato** quando è installato il programma ffmpeg (lo stesso che crea il `.m4b`): l'audio unito di ogni parte viene portato a un unico livello di volume (EBU R128, lineare, così la dinamica resta; **-18 LUFS** salvo che tu lo modifichi in *Settings → Impostazioni dell'estensione → Audiobook Settings*, dove puoi anche disattivarlo). I pezzi restano come li ha prodotti il server; solo la parte unita viene livellata, così un audiobook non cambia di volume da una parte all'altra. Le **parti create in precedenza** non vengono toccate finché non le converti di nuovo: questo unisce di nuovo i pezzi salvati e li livella, e non chiede nulla al server vocale (nemmeno il rilascio del modello linguistico). Disattivare il livellamento lascia le parti già livellate così come sono. Se ffmpeg fallisce per una parte, questa conserva il suo audio com'era e il risultato lo segnala.
- Tutte le parti di un solo clic vengono create in **un unico batch per modello vocale**: le richieste prima del batch vengono eseguite una volta, poi tutti i pezzi vengono letti, poi le richieste dopo il batch vengono eseguite una volta. È ciò che impedisce alla scheda grafica di cambiare modello per ogni parte.
- Ogni pezzo viene **conservato in base al suo contenuto** (testo, voce, modello, lingua, impostazioni, seme). Una parte già aggiornata non costa nulla, un paragrafo modificato è l'unico pezzo creato di nuovo, e una parte fallita conserva i pezzi già creati quando premi **Retry**.
- I pezzi vengono uniti con pause che dipendono dalla loro posizione (dopo un titolo, tra i paragrafi, prima di *he said*) in un unico file mp3 per parte, conservato insieme alla parte.
- Mentre una tua generazione è in corso, la lettura vocale attende, così il modello linguistico non viene rilasciato sotto di essa.
- Una parte che hai contrassegnato come **final** nello studio viene lasciata stare, e la finestra di dialogo dice quante sono.
- **L'intero audiobook** unisce in un unico mp3 ogni parte con audio aggiornato, con un segno di capitolo per ciascuna parte, e mostra un link di download sotto l'elenco (vengono indicate per nome le parti escluse). Lo stesso file è il formato di esportazione *one mp3 with chapter marks*.
- **Stop all** annulla le parti in attesa e ferma subito quella in esecuzione: la richiesta al modello linguistico (l'etichettatura) o al server vocale (il pezzo in lettura) viene scartata senza attenderla. Un batch fermato prima di iniziare non invia le richieste che lo precedono, così il modello linguistico resta caricato. **Scarica** dà l'mp3 di una parte completata.
<a id="the-audiobook-studio"></a>

## Lo studio audiolibro

*Menu Strumenti → **Audiobook studio*** (la voce del pezzo selezionato, con un'anteprima, si trova accanto al suo **speaker**) è dove si crea l'audiolibro finale, una parte alla volta: vedi lo script, lo modifichi, ascolti un pezzo e realizzi la parte.

![Lo studio con un discorso selezionato](../../images/plugin-audiobook-studio.png)

- Mentre la parte viene annotata o realizzata, una barra indica che è in corso un'operazione e i pulsanti che avvierebbero altro lavoro restano in attesa.
- **Annota** crea lo script della parte (con il modello linguistico, se lo richiedi, altrimenti con il narratore) e mostra i pezzi in una tabella: **speaker**, **delivery**, **intensity**, il testo parlato e un segno per un pezzo che hai modificato. Mentre è in esecuzione compare accanto a esso un pulsante **Interrompi**, che termina la richiesta al modello linguistico.
- Seleziona un pezzo per modificarne lo **speaker**, il **spoken text**, la **delivery**, la **intensity** (l'enfasi della voce, per i modelli che la prevedono; su Higgs la delivery e la intensity sono i marcatori come `<|emotion:sadness|>` all'inizio del testo parlato, che li seguono quando li cambi), la **language**, il **seed**, la **pause after**. **Applica** mantiene la modifica e **blocca** il pezzo: una nuova annotazione lascia un pezzo bloccato così com'è.
  **Unlock** lo restituisce all'annotazione. *The request the speech server gets* mostra la richiesta esatta.
- **Hear** crea l'audio di un pezzo (solo quel pezzo; il server vocale viene avviato per questo); **Another take** lo rifà con un nuovo seed, e le **prove** vengono conservate così puoi scegliere quella che preferisci (*Use this take and hear it*). Una buona voce dipende spesso dal seed.
- **Make the part from this script** unisce i pezzi così come sono, senza una nuova annotazione.
- Lo **stato** della parte è *draft*, *reviewed* o *final*. Una parte *final* non viene più annotata né convertita di nuovo, e l'esportazione può essere limitata alle parti *final*.

<a id="exporting"></a>

## Esportazione

L'audiolibro è un formato di *Esporta la storia* del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** e, quando ffmpeg è installato, **Audiobook (one m4b with chapters)**. Sono composti dall'audio delle parti aggiornate: una parte senza audio, o con audio più vecchio del suo testo o del suo script nello studio, viene esclusa e la finestra indica quale. I file mp3 contengono tag ID3 (il titolo del libro, il capitolo, la traccia), e il file unico ha segnalibri di capitolo che lettori come VLC e la maggior parte delle app per audiolibri mostrano; lo zip contiene una playlist. La casella **Audiobook: only the parts marked final** limita l'esportazione alle parti che hai completato nello studio.

L'**m4b** è il formato che Apple Books e la maggior parte delle app per audiolibri sul telefono richiedono: audio AAC (64 kbit/s, sufficiente per il parlato) in un contenitore MP4 con i capitoli, il titolo, l'autore e il genere. A crearlo è ffmpeg, quindi il formato è offerto solo su un server che ha ffmpeg. L'audio memorizzato come mp3 viene codificato una seconda volta per questo; l'audio memorizzato come wav viene codificato una sola volta.

<a id="how-the-audio-is-stored"></a>

### Come è memorizzato l'audio

Un amministratore lo sceglie per l'intera installazione in *Impostazioni → Extension Settings → Audiobook Settings* (l'ultima sezione, visibile solo agli amministratori):

- **mp3** (predefinito): piccolo, unito senza decoder, non richiede ffmpeg. L'm4b è un secondo passaggio con perdita.
- **wav**: senza perdita, circa dieci volte più grande (un'ora di parlato occupa circa 170 MB), richiede ffmpeg. I pezzi vengono richiesti al server vocale in formato wav e uniti come wav; il browser li riproduce così come sono; le esportazioni mp3 e zip sono codificate da ffmpeg (96 kbit/s) e l'm4b viene codificato una sola volta.

La scelta si applica a ciò che viene creato da quel momento in poi. Le parti create prima mantengono il loro audio e vengono gestite così come sono; una parte di entrambi i tipi (un pezzo bloccato dello studio creato in precedenza) viene unita come wav, e un'esportazione di parti di entrambi i tipi funziona, ma richiede ffmpeg.

Un'esportazione dell'intero libro viene creata dai file memorizzati delle parti, in file temporanei del server, e scaricata da lì; quindi la sua durata non è limitata dalla memoria dell'applicazione, ma la cartella temporanea deve avere spazio per circa il doppio dell'audio del libro (con il wav, circa 350 MB per ora) mentre viene creata.

![La finestra di esportazione con i formati audiolibro](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversione automatica

Un libro può creare da solo l'audio di una nuova parte, non appena la parte è *final*: dopo l'annotazione automatica, la riscrittura e le traduzioni, se il libro le ha, come ultimo passaggio di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part) (nel pannello delle esecuzioni fino alla creazione dell'audio; il suo **Interrompi** toglie la parte dalla coda). È **disattivata** per impostazione predefinita. Scegli l'opzione nella scheda *Prompt* del libro, nelle impostazioni dell'estensione, per una cartella di libri, oppure per tutti i tuoi libri nelle impostazioni audiolibro; vince quella più vicina:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; un libro senza modello riceve la narrazione semplice, e te ne viene data notizia.

![L'impostazione in un libro](../../images/plugin-audiobook-auto.png)

La parte entra nella tua coda, con il cast e la pronuncia del libro. Una notifica ti avvisa che è in coda e un'altra che il suo audio è pronto o è fallito; compaiono solo nella pagina che ha avviato la generazione, ma il lavoro è comunque nella finestra *Convert to audio*.

<a id="listening"></a>

## Ascolto

Un libro pubblicato (o il lettore) che ha parti con audio ottiene una barra del lettore: riproduci, pausa, interrompi, la velocità, la parte in ascolto evidenziata nel testo. Parte dalla selezione o dalla prima parte visibile, riproduce le parti in ordine e carica la successiva mentre la corrente è in riproduzione, e i controlli multimediali del dispositivo riconoscono il libro. L'audio creato da una versione più vecchia del testo di una parte non viene offerto. L'audio è servito al proprietario del libro e, finché il libro è pubblicato, a chiunque possa aprirlo; un libro che viene ritirato dalla pubblicazione smette di essere servito subito (alla parte successiva, quando il lettore la carica).

![Il lettore nel reader](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte senza audio

Il lettore si ferma su una parte senza audio, perché una parte della storia non venga saltata, e lo segnala. Quando è installata l'estensione [Read Aloud](tts-reader.md), la barra mostra anche un pulsante con l'altoparlante, **Read this part (and the next ones without audio) with the voice of the browser**. Non viene letto nulla con una voce artificiale se non lo premi. Legge quella parte e le parti successive senza audio con la voce del tuo dispositivo, e quando arriva a una parte con audio il lettore riprende il controllo. Le parti successive senza audio nello stesso ascolto vengono lette allo stesso modo senza chiedere di nuovo; **Interrompi** lo silenzia, e riavviare il lettore chiede di nuovo. Voce, velocità e tono sono quelli di Read Aloud.
<a id="commands"></a>

## Comandi

Gli stessi strumenti dei [comandi](../commands/index.md) (la riga di comando dell'editor della storia, la macro `command` di un modello, un agente). Le parti sono numerate come le numera la finestra di dialogo Converti, oppure indicate con il loro nome.

| Comando | Cosa fa |
|---|---|
| `/audiobook-status [from [to]]` | Il server vocale in uso, il modo in cui il libro si converte da solo, quante parti hanno l'audio (nessuna, pronta, più vecchia del testo, più vecchia dello script), lo stato di ogni parte elencata nello studio e la coda. Lento solo quando il libro è lungo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Mette le parti in coda (l'intero libro senza argomenti, una sola parte quando è dato solo `from`). `labelling` `true` usa il modello del libro; `what` è `audio` o `script` (solo lo script per lo studio, nulla viene pronunciato). Con `wait` `true` il comando resta attivo finché i lavori non sono terminati, mostra a che punto sono e indica come è finito ciascuno; interromperlo annulla i lavori non ancora iniziati. |
| `/audiobook-stop` | Annulla le parti in attesa; quella in esecuzione si ferma dopo il suo pezzo. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` nello studio; la parte richiede uno script. |
| `/audiobook-cast` | Il narratore, il ripiego e i personaggi del cast con i loro attori. |

![Il risultato di un comando](../../images/plugin-audiobook-command.png)

L'audio viene creato per il **tuo** libro, non per la copia di lavoro nascosta di un [agente](../agents.md): un agente può leggere il cast e riceve un rifiuto dagli altri comandi, che gli dice che devi eseguirli tu. Eseguili dalla riga di comando, oppure affida a un agente il compito di preparare il libro (annotare, revisionare) e convertilo tu stesso.

<a id="notifications"></a>

## Notifiche

Quando un gruppo di conversioni è terminato, il browser può avvisarti ([notifiche](../account.md#notifications), così puoi lasciare la scheda aperta mentre il server vocale lavora). Nell'elenco delle notifiche della pagina *Impostazioni* ci sono due righe: **Convert to audio** (i gruppi che avvii dalla finestra di dialogo o con il comando) e **Convert to audio (automatic)** (una nuova parte che è stata resa audio automaticamente). Un gruppo che hai interrotto non genera notifiche; un gruppo con una parte fallita segnala il fallimento.

![La lista delle notifiche](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Cosa non fa (ancora)

- Solo mp3 e wav (niente opus); il volume delle parti è quello che restituiscono le voci.
- Nessun effetto sonoro né ambiente.
- Due motori vocali, Chatterbox e Higgs Audio v3 (in ComfyUI).
- L'ascolto di una voce al di fuori di un segmento.
- Il cast non fa parte del backup degli *attori* di un libro: un libro ripristinato mantiene il suo cast, ma indica attori che devono esistere nel tuo account.
