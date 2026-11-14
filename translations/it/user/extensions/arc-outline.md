---
label: Arc Outline
order: 24
---

# Arc Outline

Il piano del libro nella barra laterale: archi, ciascuno suddiviso in capitoli, con lo stato di ognuno, e un modello che può scrivere archi e capitoli per te. La struttura non fa parte del prompt della storia; i generatori e il [Side Query](side-query.md) possono usarla quando glielo chiedi.

<a id="the-arcs-tab"></a>

## La scheda Archi

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ha una scheda **Archi** accanto a *Struttura*. **Add new arc** apre la finestra di dialogo di un arco:

| Campo | |
|---|---|
| Titolo | Obbligatorio. |
| Descrizione | Cosa accade in esso. |
| Nota (privata) | Note per te stesso. |
| Stato | **Planned**, **Current** o **Finished**. |
| Parte collegata | La parte della storia a cui appartiene l'arco o il capitolo, vedi [sotto](#linking-to-the-story). |

I capitoli hanno gli stessi campi. L'elenco mostra ogni arco come una scheda con i suoi capitoli rientrati sotto di esso. Il colore di una scheda segue il suo stato: grigio per i pianificati, blu per quello attuale, verde con titolo attenuato per quelli conclusi. I pulsanti di una scheda:

| Pulsante | |
|---|---|
| calendario / play / spunta | Lo stato; apre un menu per cambiarlo. |
| freccia a destra (archi) | Nasconde o mostra i capitoli dell'arco. |
| freccia a destra (capitoli con una nota) | Mostra o nasconde la nota del capitolo sotto la sua descrizione, così puoi leggerla senza aprire la finestra di dialogo. |
| frecce | Spostano l'arco o il capitolo su o giù. |
| più (archi) | Aggiunge un capitolo alla fine dell'arco. |
| bacchetta (archi) | Chiede al modello di scrivere capitoli per l'arco, vedi [sotto](#generating-chapters). |
| bersaglio (archi e capitoli con una parte collegata) | Fa scorrere la storia fino alla parte a cui è collegato. |
| penna | Lo apre nella finestra di dialogo. |
| cestino | Lo elimina (un arco con tutti i suoi capitoli), dopo una conferma. |
| robot (archi e capitoli generati) | Mostra ciò che il modello ha scritto, vedi [sotto](#generating-an-arc). |

Accanto alla bacchetta, in alto, c'è una penna con una piuma: prepara le istruzioni del turno successivo a partire dal capitolo attuale, vedi [sotto](#drafting-the-instructions-for-the-current-chapter).

![La scheda Archi: un arco concluso, uno attuale e uno pianificato](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Collegamento alla storia

Un arco o un capitolo può essere **collegato a una parte della storia**, così puoi passare dal piano al punto del testo. La finestra di dialogo ha il campo **Parte collegata**: la posizione della parte nel ramo attivo, numerata da 1 come la mostra la scheda *Struttura* (`#12`).
Scrivila, oppure premi il pulsante **link** accanto ad essa per prendere la parte **più visibile** nella storia in quel momento (scorri prima fino al punto, poi apri la finestra di dialogo); il pulsante **target** fa scorrere la storia fino alla parte indicata nel campo. Un arco o un capitolo collegato ha anche il pulsante bersaglio sulla sua scheda. Il collegamento è la posizione nel ramo, quindi punta a un'altra parte quando vengono eliminate o inserite parti prima di essa; in quel caso modificalo. Un'importazione dalla storia collega i capitoli per te, vedi [sotto](#importing-an-arc-from-the-story).

<a id="the-note-of-a-chapter"></a>

### La nota di un capitolo

Un capitolo che ha una nota mostra una freccia sulla destra. Essa apre la nota sotto la descrizione, nell'elenco, così puoi leggere i dettagli di un capitolo (quelli scritti dal modello per esso, per esempio) senza aprire la finestra di dialogo; premila di nuovo per nasconderla. Quali note sono aperte non viene salvato.

![Un capitolo con la sua nota mostrata](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Spostare capitoli tra archi

Un capitolo spostato in su dalla prima posizione del suo arco va alla fine dell'arco precedente; uno spostato in giù dall'ultima posizione va all'inizio dell'arco successivo. Continua a premere la freccia per portare un capitolo quanto ti serve.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Preparazione delle istruzioni per il capitolo attuale

La penna con una piuma accanto a *Add new arc* apre la finestra che permette al modello di [preparare le istruzioni](../books/story-editor.md#drafting-the-instructions) del turno successivo, con i dati del capitolo a cui è arrivata la storia come suo **Piano della storia**. Il capitolo attuale è il primo capitolo contrassegnato **Current**; se non ce n'è nessuno, il primo che non è **Finished**; un arco contrassegnato attuale che non ha ancora capitoli resta da solo. Il modello riceve quell'arco e quel capitolo con le loro descrizioni e note, e il titolo e la descrizione (non la nota) del capitolo successivo, con l'avvertenza che la storia non deve ancora arrivarci. La finestra parte da quanto hai scritto in *Istruzioni* del pannello del nuovo turno, come guida; **Use as Instructions** inserisce la bozza in quel pannello e lo apre. Senza un capitolo attuale, il pulsante lo indica.

Gli stessi dati sono una fonte per il [Side Query](side-query.md), vedi [sotto](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Concludere un arco

Contrassegnare un arco **Finished** contrassegna come **Finished** anche tutti i suoi capitoli. Cambiare un arco in un altro stato lascia i suoi capitoli come sono.

La struttura viene salvata con il libro non appena la modifichi, quindi è inclusa nei suoi backup.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## La struttura come fonte per altre estensioni

La struttura viene offerta ad altre estensioni come fonte di dati sul libro: nel [Side Query](side-query.md) spunta **Arc outline** nel popover dei plug per inviare gli archi, i loro stati, le descrizioni e i capitoli insieme alle tue domande. Ci sono due versioni: **Arc outline** è quella sintetica (solo descrizioni), **Arc outline (with notes)** aggiunge la nota di ogni arco e capitolo sotto la sua descrizione, ed è lì che stanno i dettagli degli archi e dei capitoli generati. Le tue note private vengono inviate solo se spunti la seconda. Una terza fonte, **Arc outline: current chapter**, invia solo l'arco e il capitolo a cui è arrivata la storia (vedi [Preparazione delle istruzioni per il capitolo attuale](#drafting-the-instructions-for-the-current-chapter)).

<a id="generating-an-arc"></a>

## Generazione di un arco

Il pulsante della bacchetta accanto a **Add new arc** apre un menu con tre voci: **Generate an arc** chiede al modello di scrivere un nuovo arco, **Import from story** riassume ciò che hai già scritto, vedi [sotto](#importing-an-arc-from-the-story), e **Develop the plot** è una conversazione con il modello su dove va la storia, vedi [sotto](#developing-the-plot-in-a-conversation).

![Il menu della bacchetta](../../images/plugin-arcoutline-menu.png)

 La finestra di dialogo di **Generate an arc** ha due metà.

A sinistra:

| | |
|---|---|
| Prompt | Le istruzioni per il modello, con tre variabili: `{{backgroundLore}}` (le voci del lorebook del libro e dei suoi sotto-lorebook), `{{existingArcs}}` (i tuoi archi e capitoli, con i loro stati e descrizioni, ma non le tue note private) e `{{specification}}`. Parte dal prompt in *Impostazioni → Impostazioni estensione → Arc Outline* (il prompt predefinito se non lo hai cambiato); quello che modifichi qui vale solo per questa generazione. |
| Specifica | Cosa vuoi dall'arco: il tema, i personaggi, dove dovrebbe portare. Può essere vuota. |
| Strumenti | Solo quando è installata una suite di [strumenti per il modello](tools.md) (nomi, luoghi): quali suite, e quali delle loro categorie, il modello può usare invece di inventare cose, come nel [Lore Generator](lore-generator.md). |
| Modello, Protocolli | Partono dal modello e dal protocollo del libro (i tuoi predefiniti dalle *Impostazioni* quando il libro non ne ha). Cambiali per usare un altro modello solo per questa generazione; la scelta non viene salvata. |
| Genera | Avvia il modello. Mentre è in esecuzione il pulsante diventa un **Interrompi** rosso e tutto il resto della finestra è disattivato. |

A destra vedi il **ragionamento** del modello mentre viene scritto e, appena la risposta inizia, l'**arco** che propone - un nome, una descrizione sintetica e una nota più lunga - mostrato come un arco della struttura (la nota sotto la sua descrizione) e che cresce man mano che la risposta arriva. La descrizione è ciò che mostra la struttura, quindi al modello viene chiesto di tenerla a un paio di frasi e di mettere i dettagli nella nota, che finisce nella **Nota** dell'arco salvato. Quando il modello ha finito, la sua risposta viene controllata; se non è un arco valido (per esempio manca la nota), la finestra di dialogo lo segnala e l'arco non può essere salvato.

![Generazione di un arco](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** aggiunge l'arco alla fine della struttura. Per riprovare, premi di nuovo **Genera**, con lo stesso prompt o uno modificato, oppure con una specifica diversa; la risposta precedente viene sostituita.
<a id="importing-an-arc-from-the-story"></a>

## Importazione di un arco dalla storia

**Importa dalla storia** nel menu della bacchetta chiede al modello di leggere una parte della tua storia e di trascriverla come un arco con i suoi capitoli, così da portare nella scaletta un libro che hai già scritto. Il modello legge sempre il [ramo attivo](../books/branches-and-story-tree.md) del libro, così com'è quando apri la finestra. La finestra è come quella per gli archi, con queste scelte a sinistra:

| | |
|---|---|
| Dal messaggio, Al messaggio | L'intervallo della storia da inviare. I messaggi del ramo che contengono testo sono numerati a partire da 1; all'inizio è selezionato l'intero ramo. Sotto ogni campo vedi l'inizio del messaggio a cui punta. |
| Numero di capitoli (facoltativo) | Scegli un numero per ottenere esattamente quel numero di capitoli (una risposta con un altro numero viene rifiutata), oppure lascia il campo vuoto per lasciare che il modello divida la storia dove naturalmente si divide. |
| Collega i capitoli alle parti della storia | Attivo all'inizio. I messaggi sono numerati a partire da 1 all'interno dell'intervallo inviato, e il modello indica in quale messaggio inizia ogni capitolo; il capitolo viene [collegato](#linking-to-the-story) a quella parte della storia (numerata nell'intero ramo, quindi è corretto qualunque sia l'intervallo), e l'arco alla parte in cui inizia il suo primo capitolo. Una risposta che indica un messaggio non inviato lascia il capitolo non collegato. |
| Stato dell'arco importato e dei suoi capitoli | **Completato** all'inizio, perché la storia è scritta; scegli **Attuale** o **Pianificato** se ti serve. |
| Contatore dei token | Quanti messaggi contiene l'intervallo e i relativi token approssimativi, e i token approssimativi dell'intero prompt (istruzioni, lore, scaletta e storia) accanto a ciò che il modello accetta. Diventa un avviso quando il prompt non entra, nel qual caso scegli meno messaggi. Segue l'intervallo, il numero di capitoli, il prompt e il modello. |
| Prompt | Le istruzioni per il modello, con le variabili `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (i messaggi dell'intervallo) e `{{numberOfChapters}}`. Parte dal **Prompt per l'importazione della storia** di *Impostazioni → Impostazioni estensione → Arc Outline*. |

![Importazione di un arco dalla storia](../../images/plugin-arcoutline-import.png)

Qui non ci sono strumenti, i nomi sono nella storia. A destra vedi il ragionamento, poi l'arco e i suoi capitoli così come il modello li scrive. **Salva come arco** aggiunge l'arco con i suoi capitoli in fondo alla scaletta; ciascuno di essi conserva il ragionamento e la risposta del modello e ha un proprio pulsante robot. Una storia lunga è meglio importarla in più passaggi, un arco per ogni intervallo.
<a id="developing-the-plot-in-a-conversation"></a>

## Sviluppare la trama in una conversazione

**Sviluppa la trama** nel menu della bacchetta apre una chat con il modello sulla trama. Parlarne non cambia nulla nel libro: il modello vede ciò che scegli di inviargli, risponde e, quando vi siete messi d'accordo su qualcosa, lo passi al generatore di archi con un clic.

A sinistra scegli cosa vede il modello e come ti aiuta (le scelte vengono ricordate per la volta successiva), a destra c'è la chat:

![La procedura guidata della trama: le opzioni a sinistra, la conversazione a destra](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Modello, Protocolli | Partono dal modello e dal protocollo del libro. Cambiarli vale solo per questa conversazione. |
| Come ti aiuta il modello | **Conversazione libera** (guidi tu), **Esplora direzioni** (più modi distinti in cui la storia può proseguire), **Trova i problemi** (fili mai risolti, promesse che nessuno mantiene, contraddizioni con la lore, ritmo) oppure **Sviluppa un'idea** (la tua idea trasformata in premessa, svolte, un finale e capitoli che si adattano dopo i tuoi archi). |
| Invia la lorebook | Le voci della lorebook del libro e delle sue sotto-lorebook. |
| Invia la struttura | **Non inviata**, **Solo descrizioni** oppure **Con le note** (anche le tue note private). |
| Invia le note | Solo quando [Note della storia](story-notes.md) è installato: **Non inviate**, **Titoli e prime righe** (predefinito) oppure **Testo completo** delle tue note, senza le pagine che hai nascosto agli assistenti. |
| Invia i riassunti della storia | I riassunti prendono il posto della storia fino al più recente di essi, come li invia una generazione. |
| Invia le ultime parti della storia | Le parti dopo il riassunto più recente (tutte, se i riassunti sono disattivati), le più recenti che rientrano nei **Token della storia**. Ogni parte viene mostrata al modello con il suo numero nella struttura della storia. |
| Token della storia | Il numero massimo di token delle parti della storia. Vuoto: un terzo di ciò che il modello accetta. |
| Prompt | Le istruzioni per il modello, con le variabili `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` e `{{mode}}`. La modifica viene ricordata. |

Sotto le opzioni una riga indica quanti riassunti e quante parti della storia riceve il modello, e quanti token della richiesta occupa di ciò che il modello accetta. Diventa un avviso quando non ci sta; invia meno oppure [riassumi la chat](#chats-branches-and-summaries). Il libro viene riletto a ogni messaggio, quindi una modifica della struttura o della storia viene vista dalla risposta successiva.

A destra c'è la chat. Scrivi un messaggio e premi **Invia** (diventa **Interrompi** mentre il modello scrive; ciò che è arrivato viene conservato). Ogni messaggio ha dei pulsanti: copia, **dirama da qui**, elimina e, su una risposta del modello, anche una bacchetta, **Genera un arco da questo messaggio**, che apre il generatore di archi con il messaggio come specifica. Se la discussione è stata lunga, chiedi prima al modello di riassumere in una specifica ciò che avete deciso.

<a id="letting-the-model-change-the-book"></a>

### Lasciare che il modello modifichi il libro

Sotto le opzioni c'è **Cosa può fare il modello**. Con nessuna di queste voci spuntata la chat è solo una conversazione. Spunta un gruppo e il modello riceve gli strumenti per quel gruppo, che può usare
quando lo chiedi o quando vi accordate; gli viene detto di controllare prima di aggiungere, di preferire l'aggiunta alla sostituzione e di dire che cosa ha fatto:

| Gruppo | Il modello può |
|---|---|
| **Pianificatore** | elencare le scene del [pianificatore](../books/story-editor.md#the-planner-tab), aggiungerle, modificarle, spostarle, eliminarle e segnarle come fatte. |
| **Lorebook** | cercare e leggere la lore, creare voci e modificare quelle esistenti (non eliminarle). |
| **Struttura** | leggere la struttura, aggiungere archi e capitoli, modificarli e spostarli (non eliminarli). |
| **Note** (quando [Note della storia](story-notes.md) è installato) | cercare e leggere le tue note e seguirne i collegamenti, aggiungere una pagina o aggiungere a una pagina quando gli chiedi di conservare qualcosa, e modificare il testo di una pagina (non eliminare una pagina). Le pagine che hai nascosto agli assistenti non sono raggiungibili. |
| **Generatori** | richiamare il generatore di archi e quello di capitoli e, quando il [Generatore di lore](lore-generator.md) è installato, generare una voce della lore. Restituiscono una bozza e non salvano nulla, a meno che al modello non venga detto di farlo; ti mostra la bozza e la salva quando sei d'accordo. |
| **Strumenti** (pulsante, quando una [suite di strumenti](tools.md) è installata) | gli strumenti per i nomi (di persone e luoghi), come nei generatori, così può proporti nomi reali. |

- **Chiedimi prima di modificare o eliminare qualcosa** (attivo per impostazione predefinita): la modifica del testo di ciò che esiste (una voce della lore, una scena, un arco) e l'eliminazione ti vengono mostrate prima, con
  ciò che il modello vuole passare, e il modello attende **Consenti** oppure **Rifiuta**. Aggiungere una scena, una voce o un arco non viene chiesto: lo vedi nella chat e nel pianificatore,
  nella lorebook o nella struttura, e puoi eliminarlo.
- Ogni strumento richiamato dal modello è elencato sotto la sua risposta (una riga con il nome e un segno: ✓ riuscito, ✗ fallito; aprila per vedere che cosa è stato passato e che cosa è tornato indietro).
  I messaggi successivi portano con sé le chiamate fatte dal modello e ciò che hanno restituito (un risultato lungo viene tagliato), così sa che cosa ha già fatto e non crea di nuovo la stessa voce; questi contano nel limite di token della chat.
- Che cosa costano gli strumenti: le loro descrizioni fanno parte di ogni richiesta, quindi un modello con poco contesto ha meno spazio. Funzionano solo con i modelli che sanno richiamare funzioni; il dialogo lo segnala quando un modello non ci riesce.

![Il modello ha aggiunto due scene al pianificatore; le sue chiamate sono elencate sotto la risposta](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Chat, rami e riassunti

Un libro può avere un numero qualsiasi di chat; ne scegli una nell'elenco sopra i messaggi, **+** ne avvia una nuova, la matita la rinomina (fino ad allora prende il nome dal tuo primo messaggio) e il cestino la elimina. Vengono salvate insieme al libro.

- **Ramo** copia la chat così com'è in una nuova chat; il pulsante ramo su un messaggio copia la chat fino a quel messaggio compreso, per proseguire il discorso in un altro modo da lì. Dopo, la copia non ha più nulla a che fare con l'originale.
- **Riassumi la chat** chiede prima che cosa il riassunto debba conservare o mettere in evidenza (facoltativo, per esempio "conserva la svolta sul traditore e il finale che abbiamo scartato"; vuoto: decide il modello), poi chiede al modello un riassunto della chat (ciò che avete deciso, ciò che avete scartato, ciò che resta aperto, i nomi che sono stati fissati) e lo aggiunge come messaggio. Da quel momento il modello riceve quel riassunto e i messaggi successivi, **non** i messaggi precedenti, che restano sullo schermo, attenuati. Usalo quando la chat è diventata troppo lunga per il modello, oppure per ripartire da uno stato pulito senza perdere le decisioni. Riassumi di nuovo più avanti e il nuovo riassunto copre quello vecchio. Dirama prima se vuoi che il modello riceva l'intera chat.

![La domanda su che cosa debba conservare il riassunto](../../images/plugin-arcoutline-wizard-summarize.png)

![La chat dopo il riassunto: i messaggi precedenti restano sullo schermo, attenuati](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Generazione dei capitoli

Il pulsante con la bacchetta su un arco chiede al modello di dividere quell'arco in capitoli. La finestra di dialogo è simile a quella per gli archi, con queste scelte a sinistra:

| | |
|---|---|
| Che cosa il modello conosce della storia | **Intera storia finora**: gli archi fino a questo, con tutti i loro capitoli. **Solo riassunti degli archi**: gli stessi archi senza capitoli. **Riassunti degli archi e capitoli conclusi**: gli stessi archi con solo i capitoli che hai contrassegnato come *Concluso*. Gli archi successivi a questo non vengono mai inviati. |
| Che cosa vuoi dai capitoli in generale | Ritmo, tono, cosa deve accadere. Può essere vuoto. |
| Numero di capitoli | Scrivilo a parole ("circa otto") oppure scegli un numero esatto. Un numero aggiunge un'area di testo per ogni capitolo, dove puoi dire che cosa dovrebbe contenere; sono tutte facoltative. Usare l'una o l'altra opzione cancella l'altra. Con un numero esatto, una risposta con un numero diverso di capitoli viene rifiutata. |
| Prompt | Le istruzioni per il modello, con le variabili `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` e `{{chapterSpecification}}`. Parte dal **Prompt di generazione dei capitoli** di *Impostazioni → Impostazioni estensione → Arc Outline*. |

A destra vedi il ragionamento, poi l'arco con i capitoli che ha già, e i nuovi capitoli man mano che il modello li scrive. **Salva come capitoli** li aggiunge in fondo all'arco, ciascuno con una descrizione sintetica e i dettagli nella sua nota (come per gli archi). Ogni nuovo capitolo conserva il ragionamento e la risposta del modello, e ha il proprio pulsante robot.

![Generazione dei capitoli di un arco](../../images/plugin-arcoutline-generate-chapters.png)

Un arco o un capitolo scritto dal modello conserva il suo **ragionamento** e la **risposta** da cui è nato. Il pulsante robot su di esso apre entrambi in un piccolo popover, così da non occupare spazio.

![Ciò che il modello ha scritto per un arco](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Comandi

La scaletta e i suoi generatori sono disponibili anche come [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md). Operano sul libro aperto (per un agente: sulla sua copia di lavoro), e una scheda Archi aperta mostra subito la modifica. Archi e capitoli si indicano con il **numero**, come li mostra la scaletta: il primo arco è `1`, il primo capitolo di un arco è `1` (i numeri cambiano quando qualcosa che li precede viene aggiunto, rimosso o spostato). Un comando che modifica un arco o un capitolo accetta solo i campi che gli passi; un testo vuoto cancella una descrizione o una nota.

| Comando | Cosa fa |
|---|---|
| `/get-arc-outline [arc [notes]]` | Gli archi con i loro capitoli, stati e descrizioni in JSON. Le note private solo con `notes` `true`; `arc` restituisce il solo arco indicato. Sola lettura. |
| `/add-arc title [description [note [state [position]]]]` | Aggiunge un arco, in fondo oppure in `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Modifica un arco. `state` è `PLANNED`, `CURRENT` o `FINISHED` (concludere un arco conclude i suoi capitoli). |
| `/move-arc arc position` | Sposta un arco nella posizione `position` (1 è il primo). |
| `/delete-arc arc` | Elimina un arco con i suoi capitoli, dopo una conferma. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Aggiunge un capitolo a un arco. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Modifica un capitolo. |
| `/move-outline-chapter arc chapter position [to_arc]` | Sposta un capitolo all'interno del suo arco, oppure nell'arco `to_arc`. |
| `/delete-outline-chapter arc chapter` | Elimina un capitolo, dopo una conferma. |
| `/generate-arc [specification [save]]` | Chiede al modello un nuovo arco, come fa *Genera un arco*. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Chiede al modello i capitoli di un arco, come fa la bacchetta dell'arco. `count` è un numero esatto di capitoli, `context` è `EVERYTHING` (predefinito), `ARCS_ONLY` o `ARCS_AND_FINISHED`, come la scelta *Che cosa il modello conosce della storia*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Chiede al modello di riassumere i messaggi da `from` a `to` del ramo attivo (numerati tra i messaggi con testo, come in *Importa dalla storia*) come un arco con capitoli. `state` (predefinito `FINISHED`) è lo stato dell'arco salvato e dei suoi capitoli. Con `mapParts` `true` ogni capitolo è collegato al messaggio in cui inizia (la sua *Parte collegata*, come fa la casella *Mappa le parti* della finestra di dialogo), e la risposta riporta la parte di ciascun capitolo. |

I tre comandi di generazione usano gli stessi prompt delle finestre di dialogo (le tue *Impostazioni estensione → Arc Outline*, oppure quelli predefiniti), il modello e il protocollo del libro (il tuo modello predefinito se il libro non ne ha uno) e nessuno strumento: chi chiama il comando e vuole dei nomi li ottiene con i comandi del [Generatore di nomi](name-generator.md) o del [Fornitore di nomi](name-provider.md) e li scrive nella specifica. Come le finestre di dialogo, salvano solo se richiesto: con `save` `false` (predefinito) restituiscono l'arco o i capitoli come JSON senza memorizzare nulla; con `save` `true` li aggiungono alla scaletta (un arco in fondo, i capitoli in fondo all'arco) e la risposta riporta i loro numeri. Una storia troppo lunga per il modello viene rifiutata da `/import-arc-from-story`: scegli meno messaggi. I comandi di generazione richiedono un po' di tempo e si interrompono quando l'esecuzione viene fermata.

Aggiungere e modificare non è distruttivo; solo i due comandi `delete` lo sono, e un agente può essere impostato per chiederti conferma.
