---
label: Annotazioni
order: 95
---

# Annotazioni

Un'**annotazione** è una nota per te su un passaggio di una parte, per esempio *scena debole*, *la cronologia è sbagliata*, *da rivedere*. Funziona come un
commento in un elaboratore di testi: il testo della storia resta pulito, il passaggio viene solo segnato e la nota vive accanto
a esso. Le note possono anche provenire da un modello o da un'[estensione](../extensions/index.md) che controlla la storia.

Un'annotazione è **per l'autore**. Non viene inviata al modello a meno che tu non lo indichi per quella nota, vedi
[Dare una nota al modello](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Che cos'è un'annotazione

| | |
|---|---|
| **Passaggio** | Le parole a cui si riferisce la nota, oppure l'**intera parte**. |
| **Nota** | Ciò che vuoi ricordare. |
| **Tipo** | Una parola facoltativa a tua scelta (*continuità*, *stile*, *ritmo*...). |
| **Fonte** | Chi l'ha scritta: tu, un modello (*AI*), un *agente* o un'*estensione*. Il segno nel testo ha un colore diverso per ciascuno. |
| **Stato** | *Aperta*, *Risolta*, *Scartata* o *Orfana*. Nel testo vengono segnate solo le note aperte. |

Una parte può avere un numero qualsiasi di annotazioni, anche su passaggi che si sovrappongono.

<a id="making-an-annotation"></a>

## Creare un'annotazione

**Su un passaggio.** Seleziona le parole nel testo della parte (possono avere una formattazione, la selezione non deve essere
precisa), poi fai **clic destro** su di esse (una pressione prolungata su un telefono) e scegli **Annota selezione**, oppure premi il suo [tasto](story-editor.md#keyboard-shortcuts) (`a`). La finestra mostra il passaggio; scrivi la nota, un
tipo facoltativo e se il modello riceve la nota, poi **Salva**. Il menu (☰) della parte non ha questa voce: aprirlo fa perdere la selezione. Il menu del clic destro contiene **Annota selezione**, [**Riscrivi selezione**](story-editor.md#redrafting-only-a-passage) e **Copia**, e sostituisce
quello del browser sul testo di una parte, perciò include anche **Copia**. Mentre la parte è in modifica, viene usata la selezione nell'
editor: prima la modifica viene salvata.

**Sull'intera parte.** Scegli **Annota parte** nel menu della parte.

**Con un modello.** Scegli **Annota con AI**, vedi [sotto](#letting-a-model-annotate).

**Dalla riga di comando** (e per le estensioni): [`/create-annotation`](../commands/index.md#annotations).

![Il menu del clic destro sul testo di una parte](../../images/story-text-menu.png)

![La finestra di una nuova annotazione](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Leggere le annotazioni

Un passaggio con una nota aperta è sottolineato con una **linea ondulata**, colorata in base a chi ha scritto la nota (blu tu, arancione un
modello, verde un agente, rosso un'estensione). Una nota dentro un'altra è sottolineata più in basso, così si vedono entrambe.

![La nota di un passaggio sottolineato, aperta sul segno](../../images/annotation-popover.png)

- **Fai clic sul testo sottolineato** per vedere le note che coprono quel punto (tutte, dove i passaggi si sovrappongono). Si aprono
  accanto al testo; su un telefono tocca il passaggio.
- Una **nuvoletta con un numero** nell'intestazione di una parte conta le note della parte che richiedono un controllo. Fai clic su di essa per
  vederle tutte, anche quelle sull'intera parte e quelle che hanno perso il loro passaggio.
- Nel **margine** (il menu a ingranaggio della storia, **Annotazioni nel margine**; solo desktop, e ricordato per te) le note di ogni parte stanno in una colonna
  a sinistra del suo testo, nell'ordine dei loro passaggi, invece che nel popover. La nota sotto il puntatore e il suo passaggio sottolineato si illuminano
  insieme, e un clic su un passaggio porta la sua nota in vista. Le note sono le stesse (risolvi, modifica...); le note risolte e scartate non vengono
  mostrate lì, perché non sono sottolineate.
- La scheda **Annotazioni** elenca le note di tutto il libro, raggruppate per parte nell'ordine della storia, con un filtro
  (*Da controllare*, *Chiuse*, *Tutte*). **Mostra nella storia** (o il numero della parte) ti porta alla parte.

![Le note nel margine di una parte](../../images/annotations-margin.png)

![La scheda Annotazioni](../../images/annotations-tab.png)

Ogni nota mostra chi l'ha scritta, il suo tipo, il suo stato, il passaggio, la nota e queste azioni:

| | |
|---|---|
| ✓ **Risolvi** | Il problema è stato affrontato. La nota esce dal testo e resta nell'elenco. |
| ✗ **Scarta** | La nota non vale la pena di essere affrontata. Stesso effetto. |
| ↶ **Riapri** | Riporta in vita una nota chiusa. |
| ✎ **Modifica** | Cambia la nota e il tipo. |
| 🗑 **Elimina** | Elimina la nota; può essere ripristinata dal [cestino](../trash.md). |
| **Dai al modello con la parte** | Vedi sotto. |
| **Collega alla selezione** | Per una nota senza passaggio: seleziona del testo nella parte, poi fai clic su di essa. |

<a id="annotations-follow-the-text"></a>

## Le annotazioni seguono il testo

Non devi preoccuparti di dove si trova una nota quando modifichi. Marginalia mantiene il passaggio di ogni nota insieme alle parole
a cui si riferiva:

- il testo aggiunto o rimosso **prima** del passaggio lo sposta;
- una modifica **dentro** il passaggio lo allunga o lo accorcia;
- le parole che scrivi **proprio all'inizio o alla fine** del passaggio ne restano fuori;
- correggere un refuso in un passaggio di una sola parola mantiene la nota su quella parola.

Quando il passaggio viene **eliminato** o riscritto in modo che non possa più essere trovato, la nota diventa **Orfana**. Viene conservata: collegala
a un altro passaggio (**Collega alla selezione**), scartala o eliminala. Se riporti le parole (annulla), la nota ritrova
il suo passaggio.

Quando una parte viene **generata di nuovo** il testo è nuovo, quindi le note ritrovano i loro passaggi tramite le parole che citavano;
quelle le cui parole sono sparite diventano orfane. Quando due parti vengono [unite](story-editor.md#merging-a-part-into-the-previous-one), le note di quella successiva passano
alla parte unita. Un **ramo** di una parte copia le sue note. Le note fanno parte dei [backup](backups.md), delle copie di un libro e
delle esportazioni di un backup.
<a id="letting-a-model-annotate"></a>

## Lasciare che un modello annoti

**Annotate with AI** nel menu di una parte chiede a un modello di rivederla. Indichi che cosa cercare (*segnala errori di continuità*, *dialoghi deboli*, *contraddice il lore*). Se lascia il campo vuoto, si usa ciò che il libro, la sua cartella o le sue impostazioni [hanno stabilito una volta per tutte](prompts.md#annotate-prompt), altrimenti il compito del prompt. Poi scelga il modello e prema **Annotate**.

Il modello legge la parte come fa la [riscrittura](story-editor.md#redrafting-a-part): la parte stessa, la storia che la precede (la storia più vecchia come [riassunti](summaries.md)), quanta storia successiva ci sta, il lore e lo stile del libro e le note che la parte ha già, così da non ripeterle. Non indica mai una posizione: **cita** il passo, e Marginalia ne trova la citazione nel testo. Una citazione che non viene trovata non va persa: la nota viene proposta come nota sull'intera parte, contrassegnata come *non trovata nel testo*.

**Non si aggiunge nulla al libro finché non accetta.** Le note proposte dal modello sono elencate, ognuna con una casella di controllo (inizialmente tutte selezionate). Deselezioni quelle che non vuole e scelga **Add the selected annotations**.

La finestra non blocca la storia su un computer e, mentre il modello lavora, può essere ridotta nell'angolo della pagina; vedi [Lavorare accanto alla finestra](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Option | |
|---|---|
| **Model** | Il modello che rivede. Se vuoto, è il **Default model for annotating** delle tue [impostazioni](../account.md#general-settings), altrimenti il modello del libro. Rivedere è un'analisi, quindi spesso non è il modello che scrive la prosa. |
| **Answer format** | *Automatic* (strutturato, quando il modello sa rispondere in JSON), *Structured (JSON)*, *Text blocks* (per i modelli che non rispettano un formato) oppure *Free critique, then extraction* (il modello scrive una revisione libera e un secondo modello, di solito più economico, la trasforma in note; lo scelga sotto **Model that extracts the notes**). |

![Le note che un modello propone per una parte](../../images/annotate-ai-proposals.png)

**Un intervallo di parti (batch).** **Annotate with AI** nel menu di una parte rivede solo quella parte. Per rivederne più d'una, apra il menu delle impostazioni (⚙) della storia e scelga **Annotate a range of parts with AI** (anche sul telefono). È la stessa finestra, con **From part** e **To part** da scegliere (entrambi sono obbligatori: **Annotate** attende che li indichi; non importa quale dei due sia il primo; sono elencate solo le parti con un testo). **Annotate** rivede poi le parti dell'intervallo una dopo l'altra, con le stesse istruzioni, lo stesso formato di risposta e lo stesso modello, ciascuna letta con la storia che la circonda come una parte singola. Le proposte sono elencate sotto il numero della loro parte, tutte selezionate, e **Add the selected annotations** le aggiunge ciascuna alla propria parte. **Stop** interrompe la parte in corso e non ne avvia altre; ciò che è stato trovato fino a quel momento resta nell'elenco. Una parte la cui risposta non può essere letta, o che non sta nel contesto del modello, viene saltata (la finestra lo indica) e l'esecuzione prosegue; un errore del modello, invece, termina l'esecuzione. Ogni parte è una richiesta al modello, quindi un intervallo lungo costa altrettante richieste (e token).

![Annotazione di un intervallo di parti: le proposte sono elencate sotto il numero della loro parte](../../images/annotate-ai-batch.png)

**Fatti forniti dalle estensioni.** Un'[estensione](../extensions/index.md) che conosce la storia più di quanto dica il suo testo può passare questi dati al revisore, che allora contrassegna i passi in contrasto con essi come errori di continuità. Lo fa il [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know): comunica al modello che cosa sapeva ogni personaggio tracciato all'inizio della parte, così che un personaggio che usa un nome che non ha mai sentito venga segnalato. I fatti sono inseriti nel [prompt di annotazione](prompts.md#annotate-prompt) predefinito (un prompt personale deve usare `{%{{{facts}}}%}`); senza tale estensione il prompt resta com'era.

Se una risposta non può essere letta, Marginalia lo chiede una seconda volta; se ancora non si legge, viene mostrata la risposta del modello, così il lavoro non va perso. Il prompt è il [prompt di annotazione](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Annotare automaticamente ogni nuova parte

Il menu delle impostazioni (⚙) della storia contiene **Automatic annotation** (anche sul telefono; un segno di spunta indica che è attivo). Fa sì che il libro riveda ogni parte subito dopo che è stata generata e, se lo desidera, la riscriva. Le impostazioni sono salvate con il libro:

| Option | |
|---|---|
| **Model**, **Protocol** | Il modello che rivede. Se vuoto, è il modello del libro (non il **Default model for annotating** delle impostazioni, con cui parte la finestra che apre lei stesso). |
| **What to look for** | Le istruzioni, come in **Annotate with AI**. Se vuoto, si usa ciò che il libro eredita (vedi [Prompt di annotazione](prompts.md#annotate-prompt)), altrimenti il compito del prompt; se lo lascia vuoto all'avvio, il campo mostra poi il testo che è stato usato. |
| **Annotate every generated part automatically** | Lo attiva. Dopo che una parte è stata generata (una parte nuova, uno swipe o una rigenerazione; non una parte interrotta o fallita) il modello la rivede **subito**, senza finestra; il [pannello delle esecuzioni](story-editor.md#the-panel-of-the-runs) la mostra come un passo di [ciò che segue la parte](story-editor.md#what-follows-a-generated-part). |
| **Add the annotations without asking** | Quando il modello ha finito, le note trovate vengono aggiunte. Senza questa opzione la finestra di annotazione si apre con le proposte (nella pagina in cui ha generato; da **Review** nel pannello, da qualsiasi pagina) e attende che lei le scelga, come quando la avvia da sé. |
| **Then redraft the part to address the annotations** | Solo con l'opzione precedente. Dopo che le note sono state aggiunte, il modello [riscrive](story-editor.md#redrafting-a-part) la parte, con le annotazioni aperte della parte e senza istruzioni proprie. Scrive con il modello del libro e non tocca i riassunti: la parte è nuova, nessun riassunto la descrive. Un modello che non ha trovato nulla non ha nulla da riscrivere. |
| **Apply the redraft without asking** | Solo con la riscrittura. Quando il modello ha finito, la riscrittura sostituisce il testo della parte e le note che ha trattato vengono contrassegnate come risolte. Senza questa opzione, oppure quando ha modificato la parte mentre il modello scriveva, la finestra della riscrittura si apre con il risultato e attende lei. |

La finestra della riscrittura che attende ha due schede: **Redrafted text** (il testo, il ragionamento e le differenze rispetto alla parte) e **Original text with annotations** (la parte com'era, con i passi delle note aperte evidenziati come nella storia, e le note sotto di essa; clicchi un evidenziatore per saltare alla sua nota, oppure passi con il puntatore su uno dei due per illuminare entrambi). Guardi l'originale per vedere che cosa il modello doveva trattare, poi modifichi il testo o lo applichi.

![Le impostazioni dell'annotazione automatica](../../images/annotate-auto.png)

**Chiudere una finestra che attende, oppure premere Stop nel pannello, termina ciò che segue la parte.** Non viene aggiunto né applicato nulla in più, e i passi successivi (una traduzione, l'audio) non vengono eseguiti; il messaggio del pannello ne indica i nomi, così potrà farli a mano. Ciò che era già stato aggiunto o applicato resta. Mentre la riscrittura è in corso non si può generare una nuova parte (continuerebbe dal vecchio testo); mentre le note vengono prodotte, invece, sì.

Ogni parte generata costa almeno una richiesta in più al modello (due con la riscrittura). Se genera con un server locale che conserva in cache un solo prompt, la revisione sostituisce la storia in quella cache e la generazione successiva deve rileggere la storia: scelga un altro **Model** per la revisione, oppure un secondo server.
<a id="watching-for-phrases"></a>

## Sorvegliare le frasi

Una lista di frasi da sorvegliare indica le parole che non vuoi nella prosa: cliché (*un brivido le corse lungo la schiena*, *arazzo*), i tic di un modello, un nome che hai lasciato cadere. Una parte viene controllata per queste frasi **senza un modello**, e ogni punto in cui viene trovata una frase diventa un'annotazione di quel passaggio (la sua fonte è *Phrase watch list*, e con essa viene indicata la voce trovata). Da lì è una nota come le altre: lascia che la [riscrittura](#redrafting-to-address-the-notes) se ne occupi, oppure **Dismiss** un riscontro che va bene (per esempio nei dialoghi).

![La lista di frasi da sorvegliare di un libro](../../images/phrase-watch-dialog.png)

Le frasi non sono vietate nell'output del modello. Un divieto fermerebbe una frase solo dopo che è già iniziata e spingerebbe il modello verso il sinonimo più vicino; una nota sul testo finito permette di riscrivere il passaggio per intero.

**Dove sono le liste.** Le tue impostazioni hanno una scheda **Phrase watch list** con la tua lista, che usano tutti i libri; una [cartella](folders.md#folder-settings) aggiunge una lista per i libri che contiene e per le cartelle sotto di essa, e un libro la propria, in **Phrase watch list** del menu delle impostazioni (⚙) della storia. Le liste si sommano. Una voce con la stessa frase di una ereditata la sostituisce (per assegnarle un altro modello, per esempio), e una contrassegnata con **Off here** la toglie per quella cartella o quel libro; le frasi ereditate sono elencate sotto le tue, ciascuna con **Switch off here**.

**Come si trova una frase.** Senza distinguere maiuscole e minuscole e solo come parole intere (*arazzo* non viene trovato in *arazzi*); ogni spazio o interruzione di riga corrisponde a qualsiasi altro, e virgolette e apostrofi diritti e curvi si corrispondono tra loro. Seleziona **Regular expression** per scrivere invece una [espressione regolare](../regular-expressions.md), anche questa senza distinguere maiuscole e minuscole. Un'espressione che non può essere usata viene rifiutata al salvataggio. In una parte vengono create al massimo 100 note.

**Modelli di annotazione.** Ciò che dice una nota è un **modello di annotazione** delle tue impostazioni (la stessa scheda): un nome, il campo **Note**, dove `{%{{{match}}}%}` sono le parole trovate e `{%{{{phrase}}}%}` la voce della lista, un **Type** (il colore del segno) e **Give to the model with the part** (vedi [Dare una nota al modello](#giving-a-note-to-the-model)). Ogni voce di una lista ne sceglie uno; una voce senza ne ottiene una predefinita (*Watched phrase: "…"*), senza tipo e non data al modello.

**Quando vengono controllate le parti.** Nella **Phrase watch list** del libro:

| Opzione | |
|---|---|
| **Annotate the watched phrases in every generated part** | Dopo che una parte è stata generata, viene controllata subito dopo la [annotazione automatica](#annotating-every-new-part-automatically) (un passo di [ciò che segue la parte](story-editor.md#what-follows-a-generated-part), che l'annotazione automatica sia attiva o no). Quando il libro riscrive anche automaticamente (**Add the annotations without asking** e **Then redraft the part…** dell'annotazione automatica), la riscrittura si occupa anche di queste note; una riscrittura applicata senza chiedere viene ricontrollata, e se vi ha riscritto una frase la si riscrive di nuovo, al massimo tre volte (i riscontri dell'ultima restano come note per te). Anche le parti scritte da un [agente](../agents.md) vengono controllate. |
| **Save and scan the story** | Salva la lista e controlla subito le parti del ramo attivo, per un libro già scritto: tutte, oppure quelle da **From part** a **To part** (i numeri delle parti del ramo). Il comando `/scan-phrases [from to]` fa lo stesso. |

Un punto che ha già una nota della lista per la stessa frase non ne riceve una seconda, anche se l'hai scartata, così la storia può essere riscansionata dopo una modifica. Una nota risolta non conta: se la frase è ancora lì (una riscrittura l'ha riscritta), riceve una nuova nota. I modelli di annotazione appartengono alle tue impostazioni: un libro esportato con ciò che eredita porta con sé le frasi, e su un'altra installazione le loro note sono quella predefinita.

<a id="giving-a-note-to-the-model"></a>

## Dare una nota al modello

Seleziona **Give to the model with the part** su una nota e diventa anche una nota per il modello: le note delle parti che sono nel prompt finiscono nel modello principale come `{%{{{annotations}}}%}` (il modello predefinito ha un blocco *Author's notes on the story*; un modello proprio deve usare la variabile). Vengono date solo le note **aperte** con questa spunta, e solo per le parti che entrano nel prompt. Usala per ciò che il modello deve tenere a mente mentre continua (*questo personaggio nasconde la lettera*), non per osservazioni di revisione.

<a id="redrafting-to-address-the-notes"></a>

## Riscrivere per gestire le note

Quando una parte ha note aperte, la finestra della [riscrittura](story-editor.md#redrafting-a-part) ha una casella **Deal with the open annotations of the part**. Se spuntata, il modello riceve le note (il passaggio e la nota) insieme alle tue istruzioni e riscrive la parte per gestirle; quando applichi la riscrittura con **Apply**, quelle note vengono contrassegnate come risolte. Nella [riscrittura di una selezione](story-editor.md#redrafting-only-a-passage) vengono date solo le note il cui passaggio si sovrappone alla selezione.

<a id="commands"></a>

## Comandi

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`, `/resolve-annotation`, `/reattach-annotation` e `/delete-annotation` lavorano con le note da riga di comando. Le estensioni possono usare gli stessi comandi. [`/generate-annotation <part>`](../commands/index.md#annotations) è **Annotate with AI** come comando: il modello rivede la parte con il modello del libro (un agente usa il proprio modello quando *Write the book with the model of the agent* è attivo; `profile` sceglie un altro profilo di modello dell'agente), con le istruzioni, il formato di risposta e il prompt del libro, e ogni nota che trova viene aggiunta subito, dato che non c'è nessuno a sceglierle. È una sola richiesta al modello, quindi viene eseguita da sola e consuma token.

<a id="annotations-and-agents"></a>

## Annotazioni e agenti

Un [agente](../agents.md) legge e scrive annotazioni con gli stessi comandi: può annotare un intero libro (*continuity*, *style*...), risolvere le note di cui si è occupato, modificarle o eliminarle. Lavora su una copia del libro, quindi nulla arriva alle tue note finché non [rivedi il risultato](../agents.md#reviewing-the-result):

- Una nota creata dall'agente è una riga **Annotation - Added**; una che ha risolto, scartato o riscritto è **Changed** (con la nota, il tipo, lo stato e *give to the model* come campi, ciascuno con la propria casella **Take this change**); una eliminata è **Deleted**. Le note di una parte che l'agente ha eliminato vanno con la parte e non hanno righe.
- La riga indica in quale parte si trova la nota (`3: ` e l'inizio della nota); **Show in the copy** apre la parte nella copia dell'agente. Una nota su una parte scritta dall'agente richiede quella parte: spuntare la nota spunta anche la parte.
- **Only annotations** sopra la lista spunta solo le note. Il passaggio di una nota presa è lo stesso quando il testo della parte è identico; quando prendi il nuovo testo della parte dell'agente, o il tuo testo è diverso, la nota ritrova le sue parole nel testo del tuo libro e, se non ci riesce, diventa una nota sull'intera parte.
- Una nota che hai scritto mentre l'agente era in pausa entra nella sua copia quando riprende (e una nota che hai risolto vi risulta risolta), così ne è al corrente; una nota che tu e l'agente avete entrambi modificato è un conflitto come un altro.
