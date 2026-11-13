---
label: Annotazioni
order: 95
---

# Annotazioni

Un'**annotazione** è una nota per te su un passaggio di una parte: *scena debole*, *la cronologia è sbagliata*, *da rivedere*. Funziona come un
commento in un elaboratore di testi: il testo della storia resta pulito, il passaggio viene solo segnato e la nota resta accanto
ad esso. Le note possono anche arrivare da un modello o da un'[estensione](../extensions/index.md) che controlla la storia.

Un'annotazione è **per l'autore**. Non viene inviata al modello, a meno che tu non lo indichi per quella nota, vedi
[Fornire una nota al modello](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Che cos'è un'annotazione

| | |
|---|---|
| **Passaggio** | Le parole a cui si riferisce la nota, oppure l'**intera parte**. |
| **Nota** | Ciò che vuoi ricordare. |
| **Tipo** | Una parola facoltativa che scegli (*continuità*, *stile*, *ritmo*...). |
| **Origine** | Chi l'ha scritta: tu, un modello (*IA*), un *agente* o un'*estensione*. Il segno nel testo ha un colore diverso per ciascuno. |
| **Stato** | *Aperta*, *Risolta*, *Ignorata* o *Orfana*. Nel testo vengono segnate solo le note aperte. |

Una parte può avere quante annotazioni vuoi, anche su passaggi che si sovrappongono.

<a id="making-an-annotation"></a>

## Creare un'annotazione

**Su un passaggio.** Seleziona le parole nel testo della parte (possono avere una formattazione, la selezione non deve essere
precisa), poi **fai clic destro** su di esse (una pressione prolungata su un telefono) e scegli **Annota la selezione**, oppure premi il suo [tasto](story-editor.md#keyboard-shortcuts) (`a`). La finestra mostra il passaggio; scrivi la nota, un
tipo facoltativo e se il modello riceve la nota, poi **Salva**. Il menu (☰) della parte non ha questa voce: aprirlo fa perdere la selezione. Il menu del clic destro contiene **Annota la selezione**, [**Riscrivi la selezione**](story-editor.md#redrafting-only-a-passage) e **Copia**, e sostituisce
quello del browser sul testo di una parte, quindi include anche **Copia**. Mentre la parte è in modifica, viene usata la selezione
nell'editor: la modifica viene prima salvata.

**Sull'intera parte.** Scegli **Annota la parte** nel menu della parte.

**Con un modello.** Scegli **Annota con l'IA**, vedi [più sotto](#letting-a-model-annotate).

**Dalla riga di comando** (e per le estensioni): [`/create-annotation`](../commands/index.md#annotations).

![Il menu del clic destro sul testo di una parte](../../images/story-text-menu.png)

![La finestra di una nuova annotazione](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Leggere le annotazioni

Un passaggio con una nota aperta è sottolineato con una **linea ondulata**, colorata in base a chi ha scritto la nota (blu tu, arancione un
modello, verde un agente, rosso un'estensione). Una nota dentro un'altra è sottolineata più in basso, così si vedono entrambe.

![La nota di un passaggio sottolineato, aperta sul segno](../../images/annotation-popover.png)

- **Fai clic sul testo sottolineato** per vedere le note che coprono quel punto (tutte, se i passaggi si sovrappongono). Si aprono accanto al testo; su un telefono tocca il passaggio.
- Un **fumetto con un numero** nell'intestazione di una parte conta le note della parte che richiedono un controllo. Fai clic per vederle tutte, anche quelle sull'intera parte e quelle che hanno perso il loro passaggio.
- Nel **margine** (il menu a ingranaggi della storia, **Annotazioni a margine**; solo desktop, e viene ricordata la tua scelta) le note di ogni parte si dispongono in una colonna a sinistra
  del suo testo, nell'ordine dei loro passaggi, invece che nel popover. La nota sotto il puntatore e il suo passaggio sottolineato si illuminano insieme, e un clic
  su un passaggio porta la sua nota in vista. Sono le stesse note (risolvi, modifica...); le note risolte e ignorate non vi compaiono, perché non sono sottolineate.
- La scheda **Annotazioni** elenca le note di tutto il libro, raggruppate per parte nell'ordine della storia, con un filtro
  (*Da controllare*, *Chiuse*, *Tutte*). **Mostra nella storia** (oppure il numero della parte) ti porta alla parte.

![Le note nel margine di una parte](../../images/annotations-margin.png)

![La scheda Annotazioni](../../images/annotations-tab.png)

Ogni nota mostra chi l'ha scritta, il suo tipo, il suo stato, il passaggio, la nota e queste azioni:

| | |
|---|---|
| ✓ **Risolvi** | Il problema è stato affrontato. La nota esce dal testo e resta nell'elenco. |
| ✗ **Ignora** | La nota non vale la pena di essere affrontata. Stesso effetto. |
| ↶ **Riapri** | Riporta una nota chiusa. |
| ✎ **Modifica** | Cambia la nota e il tipo. |
| 🗑 **Elimina** | Elimina la nota; può essere ripristinata dal [cestino](../trash.md). |
| **Fornisci al modello con la parte** | Vedi sotto. |
| **Collega alla selezione** | Per una nota senza passaggio: seleziona del testo nella parte, poi fai clic su di essa. |

<a id="annotations-follow-the-text"></a>

## Le annotazioni seguono il testo

Non devi preoccuparti di dove si trova una nota quando modifichi. Marginalia mantiene il passaggio di ogni nota con le parole a cui si riferiva:

- il testo aggiunto o rimosso **prima** del passaggio lo sposta;
- una modifica **dentro** il passaggio lo allunga o lo accorcia;
- le parole che digiti **proprio all'inizio o alla fine** del passaggio ne restano fuori;
- correggere un refuso in un passaggio di una sola parola mantiene la nota su quella parola.

Quando il passaggio viene **eliminato** o riscritto in modo che non sia più trovabile, la nota diventa **Orfana**. Viene conservata: collegala
a un altro passaggio (**Collega alla selezione**), ignorala o eliminala. Se riporti le parole (annulla), la nota ritrova
il suo passaggio.

Quando una parte viene **generata di nuovo** il testo è nuovo, quindi le note ritrovano i loro passaggi tramite le parole che citavano; quelle
le cui parole non ci sono più diventano orfane. Quando due parti vengono [unite](story-editor.md#merging-a-part-into-the-previous-one), le note di quella successiva passano
alla parte unita. Un **ramo** di una parte copia le sue note. Le note fanno parte dei [backup](backups.md), delle copie di un libro e
delle esportazioni di un backup.
<a id="letting-a-model-annotate"></a>

## Far annotare con un modello

**Annota con l'IA** nel menu di una parte chiede a un modello di rivederla. Scrivi cosa cercare (*segnala errori di continuità*,
*dialoghi deboli*, *contraddice il lore*; se lo lasci vuoto si usa ciò che il libro, la sua cartella o le tue impostazioni
[hanno definito una volta](prompts.md#annotate-prompt), altrimenti il compito del prompt), scegli il modello e premi **Annota**.

Il modello legge la parte come fa la [riscrittura](story-editor.md#redrafting-a-part): la parte, la storia che la precede (la storia più
vecchia come [riassunti](summaries.md)), quanta storia successiva ci sta, il lore e lo stile del libro, e le
note che la parte ha già, così da non ripeterle. Non indica mai posizioni: **cita** il passaggio, e Marginalia
trova la citazione nel testo. Una citazione che non viene trovata non va persa: la nota viene proposta come nota sull'intera parte,
contrassegnata come *non trovata nel testo*.

**Non si aggiunge nulla al libro finché non accetti.** Le note proposte dal modello sono elencate con una casella di controllo
ciascuna (tutte selezionate); deseleziona quelle che non vuoi e scegli **Aggiungi le annotazioni selezionate**.

Su un computer la finestra non blocca la storia, e può essere ridotta nell'angolo della pagina mentre il modello lavora; vedi
[Lavorare accanto alle finestre](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Opzione | |
|---|---|
| **Modello** | Il modello che rivede. Se vuoto, è il **Modello predefinito per annotare** delle tue [impostazioni](../account.md#general-settings), altrimenti il modello del libro. Rivedere è analisi, quindi spesso non è il modello che scrive la prosa. |
| **Formato della risposta** | *Automatico* (strutturato, quando il modello sa rispondere in JSON), *Strutturato (JSON)*, *Blocchi di testo* (per i modelli che non seguono un formato) oppure *Critica libera, poi estrazione* (il modello scrive una revisione libera, un secondo modello, di solito più economico, la trasforma in note: scegilo sotto **Modello che estrae le note**). |

![Le note che un modello propone per una parte](../../images/annotate-ai-proposals.png)

**Un intervallo di parti (batch).** **Annota con l'IA** nel menu di una parte rivede solo quella parte. Per rivederne più d'una, apri il menu
delle impostazioni (⚙) della storia e scegli **Annota un intervallo di parti con l'IA** (anche su telefono). È la stessa finestra, con
**Dalla parte** e **Alla parte** da scegliere (sono entrambi obbligatori, **Annota** attende entrambi; non importa quale
venga prima; vengono elencate solo le parti con un testo). **Annota** rivede poi le parti dell'intervallo una dopo l'altra, con le stesse istruzioni, lo stesso formato di risposta
e lo stesso modello, ognuna letta con la storia intorno come una singola parte. Le proposte sono elencate sotto il numero della
loro parte, tutte selezionate, e **Aggiungi le annotazioni selezionate** ne aggiunge ciascuna alla propria parte. **Interrompi** ferma la parte in corso
e non ne avvia altre; ciò che è stato trovato fino a quel momento resta nell'elenco. Una parte la cui risposta non si può leggere o che non
sta nel contesto del modello viene saltata (la finestra lo indica) e l'esecuzione prosegue; un errore del modello interrompe l'esecuzione.
Ogni parte è una richiesta al modello, quindi un intervallo lungo costa altrettante richieste (e token).

![Annotazione di un intervallo di parti: le proposte sono elencate sotto il numero della loro parte](../../images/annotate-ai-batch.png)

**Fatti dalle estensioni.** Un'[estensione](../extensions/index.md) che sa più cose sulla storia di quanto dica il suo testo può comunicarle al
revisore, che poi segna come errori di continuità i passaggi che le contraddicono. Lo fa il [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know):
dice al modello che cosa sapeva ciascun personaggio tracciato della parte all'inizio della parte, così un personaggio che usa un nome che non ha mai
sentito viene annotato. I fatti sono nel [prompt di annotazione](prompts.md#annotate-prompt) integrato (un prompt tuo deve usare
`{%{{{facts}}}%}`); senza una estensione del genere il prompt resta com'era.

Se una risposta non si può leggere, Marginalia lo chiede di nuovo una volta; se ancora non si legge, viene mostrata la risposta del modello,
così il lavoro non va perso. Il prompt è il [prompt di annotazione](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Annotare automaticamente ogni nuova parte

Il menu delle impostazioni (⚙) della storia ha **Annotazione automatica** (anche su telefono; un segno di spunta indica che è attiva). Fa sì che il libro riveda ogni parte subito dopo che è stata generata, e facoltativamente la riscriva. Le impostazioni sono memorizzate con il libro:

| Opzione | |
|---|---|
| **Modello**, **Protocollo** | Il modello che rivede. Se vuoto, è il modello del libro (non il **Modello predefinito per annotare** delle tue impostazioni, con cui parte la finestra che apri tu stesso). |
| **Cosa cercare** | Le istruzioni, come in **Annota con l'IA**. Se vuoto si usa ciò che il libro eredita (vedi [Prompt di annotazione](prompts.md#annotate-prompt)), altrimenti il compito del prompt; quando lo avvii con il campo vuoto, il campo mostra poi il testo che è stato usato. |
| **Annota automaticamente ogni parte generata** | Attiva la funzione. Dopo che una parte è stata generata (una parte nuova, uno swipe o una rigenerazione; non una che è stata interrotta o è fallita) il modello la rivede **subito**, senza finestra, e il [pannello delle esecuzioni](story-editor.md#the-panel-of-the-runs) la mostra come un passo di [ciò che segue la parte](story-editor.md#what-follows-a-generated-part). |
| **Aggiungi le annotazioni senza chiedere** | Quando il modello ha finito, le note che ha trovato vengono aggiunte. Senza questa opzione la finestra delle annotazioni si apre con le proposte (nella pagina in cui hai generato; da **Revisione** nel pannello, in qualunque pagina) e attende che tu le scelga, come quando la avvii tu. |
| **Poi riscrivi la parte per tenere conto delle annotazioni** | Solo con l'opzione sopra. Dopo che sono state aggiunte delle note, il modello [riscrive](story-editor.md#redrafting-a-part) la parte, con le annotazioni aperte della parte e senza istruzioni proprie. Scrive con il modello del libro e non tocca i riassunti: la parte è nuova, nessun riassunto la descrive. Un modello che non ha trovato nulla non ha nulla da riscrivere. |
| **Applica la riscrittura senza chiedere** | Solo con la riscrittura. Quando il modello ha finito, la riscrittura sostituisce il testo della parte e le note che ha affrontato vengono segnate come risolte. Senza questa opzione, oppure quando hai modificato la parte mentre il modello scriveva, la finestra di riscrittura si apre con il risultato e attende te. |

La finestra di riscrittura che attende ha due schede: **Testo riscritto** (il testo, il suo ragionamento e le differenze rispetto alla parte) e **Testo originale con annotazioni** (la parte com'era, con i passaggi delle note aperte segnati come nella storia, e le note sotto; clicca un segno per passare alla sua nota, oppure passaci sopra per evidenziare entrambi). Guarda l'originale per capire che cosa era stato chiesto al modello di affrontare, poi modifica il testo o applicalo.

![Le impostazioni dell'annotazione automatica](../../images/annotate-auto.png)

**Chiudere una finestra che attende, o premere Interrompi nel pannello, termina ciò che segue la parte.** Non viene aggiunto né applicato nulla, e
i passi successivi (una traduzione, l'audio) non vengono eseguiti; il messaggio del pannello li nomina, così puoi farli a mano. Ciò che è già stato
aggiunto o applicato resta. Mentre la riscrittura è in corso non si può generare una nuova parte (continuerebbe dal vecchio testo); mentre le note
vengono prodotte, sì.

Ogni parte generata costa almeno un'altra richiesta al modello (due con la riscrittura). Se generi con un server locale che tiene in cache un solo prompt,
la revisione sostituisce la storia in cache e la generazione successiva deve rileggere la storia: scegli un altro **Modello** per la revisione, oppure
un secondo server.

<a id="giving-a-note-to-the-model"></a>

## Dare una nota al modello

Spunta **Fornisci al modello con la parte** su una nota e diventa anche una nota per il modello: le note delle parti che sono
nel prompt entrano nel template principale come `{%{{{annotations}}}%}` (il template integrato ha un blocco *Note dell'autore sulla
storia*; un template tuo deve usare la variabile). Vengono fornite solo le note **aperte** con questa spunta, e solo per le
parti che entrano nel prompt. Usala per ciò che il modello deve tenere a mente mentre continua (*questo personaggio nasconde
la lettera*), non per le osservazioni di revisione.

<a id="redrafting-to-address-the-notes"></a>

## Riscrivere per tenere conto delle note

Quando una parte ha note aperte, la finestra di [riscrittura](story-editor.md#redrafting-a-part) ha una casella **Affronta le annotazioni aperte della parte**. Spuntata, il modello riceve le note (il passaggio e la nota) insieme alle tue istruzioni e riscrive la parte per affrontarle; quando **Applica** la riscrittura, quelle note vengono segnate come risolte. Nella
[riscrittura di una selezione](story-editor.md#redrafting-only-a-passage) vengono fornite solo le note il cui passaggio si sovrappone alla selezione.
<a id="commands"></a>

## Comandi

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`,
`/resolve-annotation`, `/reattach-annotation` e `/delete-annotation` gestiscono le note dalla riga di comando.
Le estensioni possono usare gli stessi comandi. [`/generate-annotation <part>`](../commands/index.md#annotations) è **Annota con l'IA** come comando: il modello esamina la parte con il modello del libro
(un agente usa il proprio modello quando *Scrivi il libro con il modello dell'agente* è attivo; `profile` sceglie un altro profilo di modello dell'agente), con le istruzioni, il formato della risposta e il prompt del libro, e ogni nota che trova viene aggiunta subito,
dato che non c'è nessuno a sceglierle. È una sola richiesta al modello, quindi viene eseguita da sola e consuma token.

<a id="annotations-and-agents"></a>

## Annotazioni e agenti

Un [agente](../agents.md) legge e scrive le annotazioni con gli stessi comandi: può annotare un intero libro (*continuità*, *stile*...), risolvere le note di cui si è occupato, modificarle
o eliminarle. Lavora su una copia del libro, quindi nulla arriva alle tue note finché non [esamini il risultato](../agents.md#reviewing-the-result):

- Una nota creata dall'agente compare come riga **Annotation - Added**; una che ha risolto, scartato o riscritto è **Modificato** (con la nota, il tipo, lo stato e *give to the model* come campi, ciascuno con la propria casella **Accetta questa modifica**); una che ha eliminato è **Eliminati**. Le note di una parte eliminata dall'agente vanno via con la parte e non hanno righe.
- La riga indica in quale parte si trova la nota (`3: ` e l'inizio della nota); **Mostra nella copia** apre la parte nella copia dell'agente. Una nota su una parte scritta dall'agente richiede quella parte: selezionare la nota seleziona anche la parte.
- **Solo annotazioni** sopra l'elenco seleziona solo le note. Il passaggio di una nota accettata è lo stesso quando il testo della parte è identico; quando accetti il nuovo testo della parte dell'agente, oppure il tuo testo è diverso, la nota ritrova le sue parole nel testo del tuo libro e, se non ci riesce, diventa una nota sull'intera parte.
- Una nota che hai scritto mentre l'agente era in pausa entra nella sua copia quando riprende (e una nota che hai risolto vi risulta risolta), così ne è al corrente; una nota che tu e l'agente avete modificato entrambi è un conflitto come un altro.
