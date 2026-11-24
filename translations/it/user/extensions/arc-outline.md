---
label: Piano degli archi
order: 24
---

# Piano degli archi

Il piano del tuo libro nella barra laterale: gli archi, ciascuno suddiviso in capitoli, con lo stato di ognuno, e un modello
che può scrivere archi e capitoli per te. Il piano non fa parte del prompt della storia; i generatori e la [Side Query](side-query.md) possono
usarlo quando lo chiedi tu.

<a id="the-arcs-tab"></a>

## La scheda Archi

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ha una scheda **Archi** accanto a *Schema*. **Aggiungi nuovo
arco** apre la finestra di un arco:

| Campo | |
|---|---|
| Titolo | Obbligatorio. |
| Descrizione | Cosa accade in esso. |
| Nota (privata) | Appunti per te. |
| Stato | **Pianificato**, **Corrente** o **Concluso**. |
| Parte collegata | La parte della storia a cui appartiene l'arco o il capitolo, vedi [sotto](#linking-to-the-story). |

I capitoli hanno gli stessi campi. L'elenco mostra ogni arco come una scheda, con i suoi capitoli rientrati sotto di esso. Il colore di una
scheda segue il suo stato: grigio per i pianificati, blu per quello corrente, verde con titolo attenuato per quelli conclusi. I pulsanti di
una scheda:

| Pulsante | |
|---|---|
| calendario / play / spunta | Lo stato; apre un menu per cambiarlo. |
| freccia (archi) | Nasconde o mostra i capitoli dell'arco. |
| freccia (capitoli con una nota) | Mostra o nasconde la nota del capitolo sotto la sua descrizione, così puoi leggerla senza aprire la finestra. |
| frecce | Sposta l'arco o il capitolo su o giù. |
| più (archi) | Aggiunge un capitolo alla fine dell'arco. |
| bacchetta (archi) | Chiede al modello di scrivere capitoli per l'arco, vedi [sotto](#generating-chapters). |
| puntina | Collega (fissa) l'arco o il capitolo alla parte più visibile della storia, vedi [sotto](#linking-to-the-story). |
| bersaglio (archi e capitoli con una parte collegata) | Fa scorrere la storia fino alla parte a cui è collegato. |
| matita | Lo apre nella finestra. |
| cestino | Lo elimina (un arco con tutti i suoi capitoli), dopo una conferma. |
| robot (archi e capitoli generati) | Mostra ciò che il modello ha scritto, vedi [sotto](#generating-an-arc). |

Accanto alla bacchetta, in alto, c'è una matita con una penna: redige le istruzioni del turno successivo a partire dal capitolo corrente, vedi
[sotto](#drafting-the-instructions-for-the-current-chapter).

![La scheda Archi: un arco concluso, uno corrente e uno pianificato](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Collegamento alla storia

Un arco o un capitolo può essere **collegato a una parte della storia**, così puoi passare dal piano al punto del testo. La finestra ha
il campo **Parte collegata**: il posto della parte nel ramo attivo, numerato da 1 come lo mostra la scheda *Schema* (`#12`).
Scrivilo, oppure premi il pulsante **collega** accanto ad esso per prendere la parte che è più **visibile** nella storia in quel momento (scorri
prima fino al punto, poi apri la finestra); il pulsante **bersaglio** fa scorrere la storia alla parte indicata nel campo. Un arco o un capitolo
collegato ha anche il pulsante bersaglio sulla sua scheda. La **puntina** su una scheda fa lo stesso del pulsante collega senza la finestra: scorri
la storia fino a dove inizia il capitolo e premila. Il collegamento è il posto nel ramo, quindi punta a un'altra parte quando vengono eliminate
o inserite parti prima di essa; in tal caso modificalo. Un'importazione dalla storia collega i capitoli al posto tuo, vedi [sotto](#importing-an-arc-from-the-story).

<a id="chapter-headings-in-the-text"></a>

### Intestazioni dei capitoli nel testo

**Intestazioni dei capitoli nel testo**, nel menu della bacchetta in alto, scrive un'intestazione all'inizio della parte a cui ogni capitolo è
[collegato](#linking-to-the-story), per esempio `# Chapter 4: The Storm`, così il testo della storia contiene i suoi capitoli (l'estensione
Chapter Marker e le esportazioni li riconoscono). La finestra elenca le intestazioni che scriverebbe, con la loro parte, man mano che cambi le opzioni:

| Opzione | |
|---|---|
| Anche gli archi ricevono un'intestazione | Disattivata: solo i capitoli ricevono un'intestazione (`#`) e gli archi sono esclusi. Attivata: ogni arco riceve un'intestazione (`#`) e i suoi capitoli un livello sotto (`##`). Un arco non collegato direttamente va nella parte del suo primo capitolo collegato. |
| Prefisso degli archi / dei capitoli | La parola prima del numero, *Parte* e *Capitolo* all'inizio. Vuoto dà `4. The Storm`. |
| Numeri | Arabi (1, 2, 3), romani (I, II, III) o nessuno. I capitoli sono numerati secondo il loro posto nello schema, collegati o no, quindi un capitolo non ancora scritto mantiene libero il suo numero. |
| Numera i capitoli da 1 in ogni arco | Altrimenti i capitoli sono numerati lungo tutto il libro. |
| Inserisci i titoli nelle intestazioni | Disattivata dà `Chapter 4` da solo. |
| Rimuovi prima ogni altra intestazione della storia | Ogni riga di intestazione del ramo attivo viene rimossa prima che siano scritte le nuove, utile quando dall'ultima volta sono stati collegati capitoli altrove. |
| Mantieni validi i riassunti | Attivata all'inizio. Un'intestazione non cambia ciò che accade, quindi i riassunti che descrivevano la storia prima della modifica tornano validi (i riassunti già superati restano superati). |

**Scrivi le intestazioni** le inserisce; le intestazioni con cui una parte inizia già vengono sostituite, quindi rieseguirlo dopo una modifica dello
schema non le aggiunge due volte. **Rimuovi tutte le intestazioni** (dopo una conferma) elimina ogni riga di intestazione dal testo del ramo
attivo. Ogni parte modificata conserva il suo testo precedente nella cronologia. Un capitolo concluso o corrente senza collegamento, e un collegamento
oltre la fine del ramo attivo, non ricevono un'intestazione e vengono segnalati. Le opzioni vengono ricordate.

<a id="the-note-of-a-chapter"></a>

### La nota di un capitolo

Un capitolo che ha una nota mostra una freccia a destra. Essa apre la nota sotto la descrizione, nell'elenco, così puoi
leggere i dettagli di un capitolo (quelli scritti dal modello, per esempio) senza aprire la finestra; premila di nuovo
per nasconderla. Quali note sono aperte non viene salvato.

![Un capitolo con la sua nota mostrata](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Spostare i capitoli tra archi

Un capitolo spostato in su dal primo posto del suo arco passa alla fine dell'arco precedente; uno spostato in giù dall'ultimo
posto passa all'inizio dell'arco successivo. Continua a premere la freccia per portare un capitolo dove ti serve.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Redigere le istruzioni per il capitolo corrente

La matita con la penna accanto a *Aggiungi nuovo arco* apre la finestra che permette al modello di
[redigere le istruzioni](../books/story-editor.md#drafting-the-instructions) del turno successivo, con i dati del capitolo in cui si trova la
storia come suo **Piano della storia**. Il capitolo corrente è il primo capitolo contrassegnato **Corrente**; se non ce n'è nessuno, il primo
che non è **Concluso**; un arco contrassegnato come corrente che non ha ancora capitoli resta da solo. Il modello riceve quell'arco e quel capitolo con
le loro descrizioni e note, e il titolo e la descrizione (non la nota) del capitolo successivo, con l'avvertenza che la storia non deve
ancora arrivarci. La finestra parte da ciò che hai scritto in *Istruzioni* del pannello del nuovo turno come guida; **Usa come Istruzioni** inserisce la bozza in
quel pannello e lo apre. Senza un capitolo corrente il pulsante lo segnala.

Gli stessi dati sono una fonte per la [Side Query](side-query.md), vedi [sotto](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Concludere un arco

Contrassegnare un arco come **Concluso** contrassegna anche tutti i suoi capitoli come **Conclusi**. Cambiare un arco in un altro stato lascia
i suoi capitoli come sono.

Lo schema viene salvato con il libro non appena lo modifichi, quindi è incluso nei suoi backup.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## Lo schema come fonte per altre estensioni

Lo schema viene offerto ad altre estensioni come fonte di dati sul libro: in [Side Query](side-query.md) spunta
**Schema degli archi** nel popover dei plug per inviare gli archi, i loro stati, le descrizioni e i capitoli insieme alle tue domande. Esistono
due versioni: **Schema degli archi** è quella sintetica (solo descrizioni), **Schema degli archi (con note)** aggiunge la nota di ogni
arco e capitolo sotto la sua descrizione, ed è lì che si trovano i dettagli di archi e capitoli generati. Le tue note private vengono
inviate solo se spunti la seconda. Una terza fonte, **Schema degli archi: capitolo corrente**, invia solo l'arco e il capitolo in cui si trova la storia
(vedi [Redigere le istruzioni per il capitolo corrente](#drafting-the-instructions-for-the-current-chapter)).
<a id="generating-an-arc"></a>

## Generare un arco

Il pulsante con la bacchetta accanto a **Add new arc** apre un menu con tre voci: **Generate an arc** chiede al modello di scrivere un nuovo arco, **Import from story** riassume ciò che hai già scritto (vedi [sotto](#importing-an-arc-from-the-story)), e **Develop the plot** è una conversazione con il modello su dove va la storia (vedi [sotto](#developing-the-plot-in-a-conversation)).

![Il menu della bacchetta](../../images/plugin-arcoutline-menu.png)

 La finestra di **Generate an arc** ha due metà.

A sinistra:

| | |
|---|---|
| Prompt | Le istruzioni per il modello, con tre variabili: `{{backgroundLore}}` (le voci del lorebook del libro e dei suoi sotto-lorebook), `{{existingArcs}}` (i tuoi archi e capitoli, con i loro stati e descrizioni, ma non le tue note private) e `{{specification}}`. Parte dal prompt di *Settings → Extension Settings → Arc Outline* (il prompt predefinito, se non l'hai modificato); ciò che modifichi qui si applica solo a questa generazione. |
| Specification | Che cosa vuoi dall'arco: il tema, i personaggi, dove dovrebbe portare. Può restare vuoto. |
| Tools | Solo quando è installata una suite di [strumenti per il modello](tools.md) (nomi, luoghi): quali suite e quali delle loro categorie il modello può usare invece di inventare cose, come nel [Lore Generator](lore-generator.md). |
| Model, Protocols | Partono dal modello e dal protocollo del libro (quelli predefiniti da *Settings*, se il libro non ne ha). Cambiali per usare un altro modello solo per questa generazione; la scelta non viene salvata. |
| Generate | Avvia il modello. Mentre è in esecuzione il pulsante diventa un **Stop** rosso e tutto il resto della finestra è disattivato. |

A destra vedi il **reasoning** del modello mentre viene scritto e, non appena la risposta inizia, l'**arco** proposto: un nome, una descrizione breve e una nota più lunga, mostrati come un arco della scaletta (la nota sotto la sua descrizione) e che crescono man mano che la risposta arriva. La descrizione è ciò che mostra la scaletta, quindi al modello viene chiesto di tenerla a un paio di frasi e di mettere i dettagli nella nota, che finisce nella **Note** dell'arco salvato. Quando il modello ha finito, la sua risposta viene controllata; se non è un arco valido (ad esempio manca la nota), la finestra lo segnala e l'arco non può essere salvato.

![Generazione di un arco](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** aggiunge l'arco in fondo alla scaletta. Per riprovare, premi di nuovo **Generate**, con lo stesso prompt o specifica oppure modificati; la risposta precedente viene sostituita.

<a id="importing-an-arc-from-the-story"></a>

## Importare un arco dalla storia

**Import from story** nel menu della bacchetta chiede al modello di leggere una parte della tua storia e di trascriverla come un unico arco con i suoi capitoli, così da portare nella scaletta un libro che hai già scritto. Il modello legge sempre il [ramo attivo](../books/branches-and-story-tree.md) del libro, così com'è quando apri la finestra. La finestra è simile a quella degli archi, con queste scelte a sinistra:

| | |
|---|---|
| From message, To message | L'intervallo della storia da inviare. I messaggi del ramo che contengono testo sono numerati a partire da 1; all'inizio è selezionato l'intero ramo. Sotto ogni campo vedi l'inizio del messaggio a cui punta. |
| Number of chapters (optional) | Scegli un numero per ottenere esattamente quel numero di capitoli (una risposta con un altro numero viene rifiutata); lascia vuoto per lasciare al modello la divisione della storia nei punti in cui naturalmente si divide. |
| Link the chapters to the parts of the story | Attivo all'inizio. I messaggi sono numerati a partire da 1 all'interno dell'intervallo inviato, e il modello indica in quale messaggio inizia ogni capitolo; il capitolo viene [collegato](#linking-to-the-story) a quella parte della storia (numerata nell'intero ramo, quindi è corretta qualunque sia l'intervallo), e l'arco alla parte in cui inizia il suo primo capitolo. Una risposta che nomina un messaggio non inviato lascia il capitolo non collegato. |
| State of the imported arc and its chapters | **Finished** all'inizio, perché la storia è già scritta; scegli **Current** o **Planned** se ti serve. |
| Token counter | Quanti messaggi ha l'intervallo e i loro token approssimativi, e i token approssimativi dell'intero prompt (istruzioni, lore, scaletta e storia) accanto a quelli che il modello accetta. Diventa un avviso quando il prompt non ci sta: in quel caso scegli meno messaggi. Segue l'intervallo, il numero di capitoli, il prompt e il modello. |
| Prompt | Le istruzioni per il modello, con le variabili `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (i messaggi dell'intervallo) e `{{numberOfChapters}}`. Parte dalla **Story import prompt** di *Settings → Extension Settings → Arc Outline*. |

![Importazione di un arco dalla storia](../../images/plugin-arcoutline-import.png)

Qui non ci sono strumenti, perché i nomi sono nella storia. A destra vedi il reasoning, poi l'arco e i suoi capitoli mentre il modello li scrive. **Save as arc** aggiunge l'arco con i suoi capitoli in fondo alla scaletta; ciascuno di essi conserva il reasoning e la risposta del modello e ha il proprio pulsante robot. Una storia lunga si importa meglio in più passaggi, un arco per ogni intervallo.
<a id="developing-the-plot-in-a-conversation"></a>

## Sviluppare la trama in una conversazione

**Develop the plot** nel menu della bacchetta apre una chat con il modello sulla trama. Parlare non modifica nulla nel libro: il modello vede ciò che scegli di inviargli, risponde e, quando vi siete accordati su qualcosa, lo passi al generatore di archi con un clic.

A sinistra scegli cosa vede il modello e come ti aiuta (le scelte vengono ricordate per la volta successiva), a destra c'è la chat:

![Il wizard della trama: le opzioni a sinistra, la conversazione a destra](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Model, Protocols | Partono dal modello e dal protocollo del libro. Se li cambi, la modifica vale solo per questa conversazione. |
| How the model helps | **Free talk** (guidi tu), **Explore directions** (più modi distinti in cui la storia può proseguire), **Find the problems** (fili mai risolti, promesse che nessuno mantiene, contraddizioni con il lore, ritmo) oppure **Expand an idea** (la tua idea sviluppata in una premessa, svolte narrative, un finale e capitoli che si collocano dopo i tuoi archi). |
| Send the lorebook | Le voci del lorebook del libro e dei suoi sotto-lorebook. |
| Send the outline | **Not sent**, **Descriptions only** oppure **With the notes** (anche le tue note private). |
| Send the notes | Solo quando [Story Notes](story-notes.md) è installato: **Not sent**, **Titles and first lines** (il predefinito) oppure **Full text** delle tue note, escluse le pagine che hai nascosto agli assistenti. |
| Send the summaries of the story | I riassunti sostituiscono la storia fino al più recente di essi, come li inviano le generazioni. |
| Send the latest parts of the story | Le parti successive al riassunto più recente (tutte, se i riassunti sono disattivati), le più recenti che rientrano in **Tokens of the story**. Ogni parte viene mostrata al modello con il suo numero nella struttura della storia. |
| Tokens of the story | Il numero massimo di token delle parti della storia. Vuoto: un terzo di ciò che il modello accetta. |
| Prompt | Le istruzioni per il modello, con le variabili `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` e `{{mode}}`. La modifica viene ricordata. |

Sotto le opzioni una riga indica quanti riassunti e quante parti della storia riceve il modello, e quanti token occupa la richiesta rispetto a quelli che il modello accetta. Se non rientra nel limite, la riga diventa un avviso: invia meno materiale oppure [riassumi la chat](#chats-branches-and-summaries). Il libro viene riletto a ogni messaggio, quindi una modifica alla struttura o alla storia è vista dalla risposta successiva.

A destra c'è la chat. Scrivi un messaggio e premi **Send** (diventa **Stop** mentre il modello scrive; ciò che è arrivato resta). Ogni messaggio ha dei pulsanti: copia, **branch from here**, elimina e, sulle risposte del modello, anche una bacchetta, **Generate an arc from this message**, che apre il generatore di archi usando il messaggio come specifica. Se la discussione è stata lunga, chiedi prima al modello di riassumere in una specifica ciò che avete deciso.

<a id="letting-the-model-change-the-book"></a>

### Lasciare che il modello modifichi il libro

Sotto le opzioni c'è **What the model may do**. Se non ne selezioni nessuna, la chat è solo un dialogo. Se selezioni un gruppo, il modello riceve gli strumenti corrispondenti e può usarli quando glielo chiedi o quando vi accordate. Gli viene detto di controllare prima di aggiungere, di preferire l'aggiunta alla sostituzione e di dire che cosa ha fatto:

| Group | Il modello può |
|---|---|
| **Planner** | elencare le scene del [planner](../books/story-editor.md#the-planner-tab), aggiungerle, modificarle, spostarle, eliminarle e segnarle come completate. |
| **Lorebook** | cercare e leggere il lore, creare voci e modificare quelle esistenti (non eliminarle). |
| **Outline** | leggere la struttura, aggiungere archi e capitoli, modificarli e spostarli (non eliminarli). |
| **Notes** (quando [Story Notes](story-notes.md) è installato) | cercare e leggere le tue note e seguirne i collegamenti, aggiungere una pagina o aggiungere testo a una pagina quando gli chiedi di conservare qualcosa, e modificare il testo di una pagina (non eliminare una pagina). Le pagine che hai nascosto agli assistenti non sono raggiungibili. |
| **Characters** (quando il [Character Tracker](character-tracker.md) è installato) | leggere i personaggi del libro e le loro relazioni, aggiungere personaggi e relazioni, modificarli (nome, slogan, nota, colore) ed eliminarli. Prima di un'azione viene chiesta conferma solo per l'eliminazione, non per una modifica: la vedi nella chat e nella scheda Characters. Il modello può indicare un personaggio con il suo numero nell'elenco; la domanda e la chat mostrano il nome del personaggio accanto al numero. |
| **Generators** | richiamare i generatori di archi e di capitoli e, quando [Lore Generator](lore-generator.md) è installato, generare una voce di lore. Restituiscono una bozza e non salvano nulla, a meno che il modello non riceva istruzione di farlo; ti mostra la bozza e la salva quando sei d'accordo. |
| **Tools** (pulsante, quando è installata una [tool suite](tools.md)) | gli strumenti per i nomi (di persone e luoghi), come nei generatori, così può proporti nomi reali. |

- **Ask me before it changes or deletes something** (attivo per impostazione predefinita): la modifica del testo di qualcosa che esiste (una voce di lore, una scena, un arco) e l'eliminazione vengono mostrate prima a te, con ciò che il modello vuole passare, e il modello attende **Allow** o **Refuse**. Aggiungere una scena, una voce o un arco non richiede conferma: lo vedi nella chat e nel planner, nel lorebook o nella struttura, e puoi eliminarlo.
- Ogni strumento chiamato dal modello è elencato sotto la sua risposta (una riga con il nome e un segno: ✓ riuscito, ✗ fallito; aprila per vedere che cosa è stato passato e che cosa è stato restituito). I messaggi successivi portano con sé le chiamate fatte dal modello e i loro risultati (un risultato lungo viene troncato), così sa che cosa ha già fatto e non crea di nuovo la stessa voce; questi contano nel limite di token della chat.
- Che cosa costano gli strumenti: le loro descrizioni fanno parte di ogni richiesta, quindi un modello con poco contesto ha meno spazio. Funzionano solo con i modelli in grado di chiamare funzioni; il dialogo lo segnala quando un modello non può farlo.

![Il modello ha aggiunto due scene al planner; le sue chiamate sono elencate sotto la risposta](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Chat, rami e riassunti

Un libro può avere un numero qualsiasi di chat; ne scegli una dall'elenco sopra i messaggi, **+** ne avvia una nuova, la penna la rinomina (fino ad allora prende il nome dal tuo primo messaggio) e il cestino la elimina. Sono salvate insieme al libro.

- **Branch** copia la chat così com'è in una nuova chat; il pulsante branch su un messaggio la copia fino a quel messaggio compreso, per proseguire il dialogo in un altro modo da lì. Dopo, la copia è indipendente dall'originale.
- **Summarize the chat** chiede prima che cosa il riassunto debba conservare o mettere in evidenza (facoltativo, per esempio "conserva il colpo di scena sul traditore e il finale che abbiamo scartato"; vuoto: decide il modello), poi chiede al modello un riassunto della chat (che cosa hai deciso, che cosa hai scartato, che cosa è ancora aperto, i nomi fissati) e lo aggiunge come messaggio. Da quel momento il modello riceve il riassunto e i messaggi successivi, **non** quelli precedenti, che restano visibili sullo schermo, attenuati. Usalo quando la chat è diventata troppo lunga per il modello, oppure per ripartire da una base pulita senza perdere le decisioni. Se riassumi di nuovo più avanti, il nuovo riassunto comprende il precedente. Crea prima un branch se vuoi che il modello riceva l'intera chat.

![La domanda su che cosa il riassunto debba conservare](../../images/plugin-arcoutline-wizard-summarize.png)

![La chat dopo il riassunto: i messaggi precedenti restano visibili sullo schermo, attenuati](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Generazione dei capitoli

Il pulsante con la bacchetta su un arco chiede al modello di dividere quell'arco in capitoli. La finestra di dialogo è simile a quella degli archi, con queste scelte sulla sinistra:

| | |
|---|---|
| Che cosa il modello sa della storia | **Whole story so far**: gli archi fino a questo, con tutti i loro capitoli. **Arc summaries only**: gli stessi archi senza capitoli. **Arc summaries and finished chapters**: gli stessi archi con solo i capitoli che hai contrassegnato come *Finished*. Gli archi successivi a questo non vengono mai inviati. |
| Che cosa vuoi dai capitoli in generale | Ritmo, tono, cosa deve accadere. Può essere vuoto. |
| Numero di capitoli | Scrivilo a parole ("about eight") oppure scegli un numero esatto. Un numero aggiunge un'area di testo per ogni capitolo, dove puoi dire che cosa dovrebbe contenere; sono tutte facoltative. Usare l'una delle due cancella l'altra. Con un numero esatto, una risposta con un numero diverso di capitoli viene rifiutata. |
| Prompt | Le istruzioni per il modello, con le variabili `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` e `{{chapterSpecification}}`. Parte dal **Chapter generation prompt** di *Settings → Extension Settings → Arc Outline*. |

Sulla destra vedi il ragionamento, poi l'arco con i capitoli che ha già, e i nuovi capitoli man mano che il modello li scrive. **Save as chapters** li aggiunge in fondo all'arco, ciascuno con una descrizione sintetica e i dettagli nella sua nota (come per gli archi). Ogni nuovo capitolo conserva il ragionamento e la risposta del modello, e ha un proprio pulsante con il robot.

![Generazione dei capitoli di un arco](../../images/plugin-arcoutline-generate-chapters.png)

Un arco o un capitolo scritto dal modello conserva il suo **reasoning** e la **answer** da cui è nato. Il pulsante con il robot su di esso apre queste informazioni in un piccolo popover, senza ingombrare la vista.

![Ciò che il modello ha scritto per un arco](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Comandi

Lo schema della storia e i suoi generatori sono disponibili anche come [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md). Operano sul libro aperto (per un agente: sulla sua copia di lavoro), e una scheda Arcs aperta mostra subito la modifica. Archi e capitoli si indicano con il **numero**, come lo schema li mostra: il primo arco è `1`, il primo capitolo di un arco è `1` (i numeri cambiano quando si aggiunge, rimuove o sposta qualcosa prima di essi). Un comando che modifica un arco o un capitolo prende solo i campi che gli dai; un testo vuoto cancella una descrizione o una nota.

| Comando | Che cosa fa |
|---|---|
| `/get-arc-outline [arc [notes]]` | Gli archi con i loro capitoli, stati e descrizioni in formato JSON. Le note private solo con `notes` `true`; `arc` restituisce solo quell'arco. Sola lettura. |
| `/add-arc title [description [note [state [position]]]]` | Aggiunge un arco, alla fine oppure in `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Modifica un arco. `state` è `PLANNED`, `CURRENT` oppure `FINISHED` (completare un arco completa anche i suoi capitoli). |
| `/move-arc arc position` | Sposta un arco nella posizione `position` (1 è il primo). |
| `/delete-arc arc` | Elimina un arco con i suoi capitoli, dopo una conferma. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Aggiunge un capitolo a un arco. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Modifica un capitolo. |
| `/move-outline-chapter arc chapter position [to_arc]` | Sposta un capitolo all'interno del suo arco, oppure nell'arco `to_arc`. |
| `/delete-outline-chapter arc chapter` | Elimina un capitolo, dopo una conferma. |
| `/generate-arc [specification [save]]` | Chiede al modello un nuovo arco, come fa *Generate an arc*. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Chiede al modello i capitoli di un arco, come fa la bacchetta dell'arco. `count` è un numero esatto di capitoli, `context` è `EVERYTHING` (il valore predefinito), `ARCS_ONLY` oppure `ARCS_AND_FINISHED`, secondo la scelta *What the model knows of the story*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Chiede al modello di riassumere i messaggi da `from` a `to` del ramo attivo (numerati tra i messaggi con testo, come in *Import from story*) come arco con capitoli. `state` (predefinito `FINISHED`) è lo stato dell'arco salvato e dei suoi capitoli. Con `mapParts` `true` ogni capitolo viene collegato al messaggio in cui inizia (la sua *Linked part*, come fa la casella *Map parts* della finestra di dialogo), e la risposta contiene la parte di ciascun capitolo. |

I tre comandi di generazione usano gli stessi prompt delle finestre di dialogo (le tue impostazioni in *Extension Settings → Arc Outline*, oppure quelli predefiniti), il modello e il protocollo del libro (il tuo modello predefinito, se il libro non ne ha uno) e nessuno strumento. Chi chiama il comando e vuole dei nomi li ottiene con i comandi del [Name generator](name-generator.md) o del [Name provider](name-provider.md) e li scrive nella specifica. Come le finestre di dialogo, salvano solo se richiesto: con `save` `false` (il predefinito) restituiscono l'arco o i capitoli in formato JSON senza memorizzare nulla; con `save` `true` li aggiungono allo schema (un arco alla fine, i capitoli in fondo all'arco) e la risposta riporta i loro numeri. Una storia troppo lunga per il modello viene rifiutata da `/import-arc-from-story`: scegli meno messaggi. I comandi di generazione richiedono un po' di tempo e si fermano quando l'esecuzione viene interrotta.

Aggiungere e modificare non è distruttivo; sono distruttivi solo i due comandi `delete`, per i quali un agente può essere impostato in modo da chiederti conferma.
