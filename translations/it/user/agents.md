---
label: Agenti (sperimentale)
order: 958
icon: cpu
---

# Agenti (sperimentale)

!!!warning Sperimentale
Gli agenti sono una novità della 1.4.0 e sono stati usati poco con modelli reali. Aspettatevi qualche imperfezione: chiamate agli strumenti sbagliate da un modello debole,
esecuzioni che consumano più token del previsto, una schermata di revisione più laboriosa del necessario e un comportamento che cambia
tra una versione e l'altra. Il vostro libro è protetto (l'agente lavora su una copia, il risultato viene revisionato e viene fatto un backup
prima di applicarlo), ma **conservate un backup personale del libro prima di far subentrare il risultato di un agente**, impostate un limite di token e tenete d'occhio le
prime esecuzioni nella [console](#the-console). Le segnalazioni di ciò che non ha funzionato sono benvenute.
!!!

Un **agente** è un modello che lavora da solo su uno dei vostri libri, per un tempo lungo, chiamando i [comandi](commands/index.md)
che gli consentite come suoi strumenti: può leggere la storia, verificarla rispetto al lorebook e riferirvi i risultati. La scheda **Agenti** è
il punto in cui li create, stabilite cosa può fare ciascuno, avviate e interrompete le loro esecuzioni, dialogate con loro nella [console](#the-console) e li duplicate.

**Avvia** fa partire l'agente: lavora in background, un giro alla volta, chiamando i comandi che avete consentito e i suoi strumenti per i file, finché termina, vi chiede qualcosa, raggiunge un limite oppure lo mettete in pausa o lo interrompete (vedi [Un'esecuzione](#a-run)). Lavora su una [copia](#the-working-copy) nascosta del
libro, quindi il vostro libro non viene mai toccato mentre l'agente lavora (il libro stesso è [bloccato](#the-book-is-locked)); quando ha finito, [revisionate il suo risultato](#reviewing-the-result) e prendete ciò che vi interessa.

La scheda è visibile solo se un amministratore vi ha concesso il [permesso](administration/permissions.md) **Agenti** (gli amministratori lo hanno sempre, ma i limiti sono gli stessi per tutti: i valori predefiniti, a meno che non compilino le impostazioni del proprio permesso; quindi un amministratore che ha bisogno di più agenti, esecuzioni o spazio di lavoro lo imposta lì; una modifica del limite raggiunge un'esecuzione in corso quando questa viene messa in pausa e poi ripresa). Su un telefono ogni agente è una scheda con gli stessi pulsanti, vedi [Su telefono](#on-a-phone).

<a id="the-table"></a>

## La tabella

| Colonna | |
|---|---|
| **ID dell'agente** | L'identificativo dell'agente. |
| **Nome** | Il nome che gli avete dato. |
| **Libro** | Il libro su cui lavora l'agente. |
| **Stato** | Cosa sta facendo la sua esecuzione: *In esecuzione*, *In attesa di te*, *In attesa di approvazione*, *In pausa tra poco*, *In pausa*, *Arresto*, *Arrestata, risultato conservato*, *Terminata, in attesa della revisione*, *Applicata*. Un agente senza esecuzione dice *Non in esecuzione*, oppure *Disattivato* quando è spento. |
| **Ultima esecuzione** | Quando è iniziata l'esecuzione più recente dell'agente, *mai* per un agente senza esecuzioni. |

![La scheda Agenti](../images/agents-tab.png)

Sopra la tabella, due campi restringono l'elenco: **Nome** e **Libro** mostrano solo gli agenti il cui nome, o il cui nome del libro, contiene tutte le parole che digitate, in qualunque ordine e senza distinzione tra maiuscole e minuscole. Le intestazioni **Nome**, **Libro**, **Stato** e **Ultima esecuzione** ordinano la tabella (premete di nuovo per invertire l'ordine; gli agenti che non sono mai stati eseguiti compaiono per primi quando l'ultima esecuzione è in ordine crescente). Su telefono, un selettore **Ordina per** sopra le schede li ordina per nome, per stato, oppure mette per primo l'ultimo avvio.

I pulsanti alla fine di una riga dipendono dall'esecuzione:

| Pulsante | |
|---|---|
| **Avvia** | Apre un menu. **Avvia subito**: l'agente lavora da solo sul suo compito (le sue istruzioni) e vi avvisa quando chiede qualcosa o termina. **Avvia nella console**: apre la [console](#the-console) e dice all'agente di non lavorare ancora ma di chiedervi subito che cosa volete che faccia; l'esecuzione diventa poi una [conversazione](#a-run) che prosegue finché non premete **Termina**. |
| **Riprendi** | Prosegue un'esecuzione in pausa o interrotta (mostrato al posto di **Avvia**). Chiede una nota facoltativa per l'agente, vedi [Riprendere un'esecuzione](#resuming-a-run). |
| **Pausa** | Mette in pausa un'esecuzione che lavora o attende la vostra risposta. |
| **Metti in pausa ora** | Mostrato mentre l'agente lavora: lo mette in pausa senza aspettare la fine del giro, vedi [Pausa e Interruzione](#a-run). |
| **Console** | Apre la [console](#the-console) dell'agente. |
| **Interrompi** | Chiede se **conservare il risultato** (l'esecuzione potrà essere ripresa più tardi) oppure **scartarlo**. |
| **Modifica** | Apre l'agente. |
| **Duplica** | Copia l'agente, vedi [Duplicazione](#cloning). |
| **Elimina** | Sposta l'agente, con le sue esecuzioni, nel [cestino](trash.md). |

L'avvio viene rifiutato, indicando il motivo, quando l'agente è disattivato, non ha un modello, oppure avete già tutte le esecuzioni contemporanee che potete avere.

![Il menu del pulsante Avvia](../images/agent-start-menu.png)

Un agente può avere al massimo un'esecuzione non ancora conclusa, e un libro può avere al massimo un agente.
<a id="a-run"></a>

## Un'esecuzione

Un'esecuzione è il lavoro dell'agente. Procede a turni: al modello viene chiesta una risposta, che può contenere testo e/o chiamate ai suoi strumenti; i risultati tornano al modello, e così via.
Ogni turno viene salvato, quindi un'esecuzione può essere messa in pausa e ripresa più tardi, anche dopo un riavvio del server. L'agente dispone dei comandi che hai autorizzato e di questi suoi:

| Strumento | |
|---|---|
| File | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `copy_file`, `merge_files`, `unzip_file`, `delete_file` nel proprio spazio di lavoro (appunti che conserva tra un'esecuzione e l'altra). Accanto allo spazio di lavoro vede il file di sola lettura `/AGENTS.md`, le tue istruzioni così come erano al momento dell'avvio dell'esecuzione, che può rileggere quando vuole e che non può mai modificare. `list_files` e `grep_files` accettano un modello di nome (`glob`, per esempio `*.md` o `chapters/*.md`), `copy_file` copia una cartella con tutto il suo contenuto (un modo rapido per conservare una versione degli appunti), `merge_files` unisce più file in uno solo senza che il testo passi dal modello (l'ultimo nome di un percorso può essere un modello, come `/chapters/*.md`), `unzip_file` decomprime in una nuova cartella un [file zip](#unpacking-a-zip-file) dello spazio di lavoro. `replace_in_file` e `regex_replace_in_file` accettano anche una **cartella** come percorso: modificano ogni occorrenza in tutti i file di testo sotto di essa (solo i file il cui nome corrisponde a `glob`, se indicato) e riportano `path: n replacements` per ciascun file. Si tratta di una **prova** (non scrive nulla, il rapporto indica cosa cambierebbe) a meno che l'agente non passi `dry_run` false; è tutto o niente (se un file non può essere scritto, non ne viene modificato nessuno) e viene rifiutato, senza modificare nulla, quando più di `max_files` file cambierebbero (100 a meno che l'agente non indichi un numero maggiore). Anche un singolo file può essere sottoposto a prova. `list_command_results` e `save_command_result` conservano il risultato lungo di un comando senza che l'agente debba riscriverlo: gli ultimi dieci risultati vengono conservati interi (l'agente vede un risultato lungo tagliato), e uno può essere salvato in un file tramite il suo numero, il più recente se non ne viene indicato alcuno; poi l'agente passa il file come `@file:/path`. |
| `@file:/path` | Non è uno strumento ma un modo di richiamarne uno: un argomento di testo di un comando che sia esattamente `@file:/notes/chapter.md` viene sostituito dal contenuto di quel file dello spazio di lavoro dell'agente, così l'agente può scrivere un testo lungo in un file una volta, verificarlo e passarlo a un comando (il risultato mostra il riferimento breve nella chiamata, il comando riceve il testo). `write_file` e `append_file` accettano anch'essi questo come contenuto, copiando un file o aggiungendolo a un altro: un file lungo può essere scritto a parti, ciascuna entro la risposta più lunga del modello, e unito in questo modo. |
| `@result` | Come `@file:`, ma per il risultato di un comando: un argomento di testo che sia esattamente `@result` viene sostituito con l'intero risultato dell'ultimo comando chiamato dall'agente, `@result:3` con il risultato di quel numero (`list_command_results` mostra i numeri). Un testo generato passa da un comando al successivo, oppure in `write_file` e `append_file`, senza che l'agente lo riscriva. |
| `search_manual`, `read_manual_page` | Cercano qualcosa nel manuale utente di Marginalia (le stesse pagine che legge l'assistente di aiuto): come funziona un modello di prompt o una macro, cosa fa un'impostazione di un lorebook. Sola lettura; ogni agente li ha. |
| `view_image` | Osserva un file immagine del proprio spazio di lavoro (PNG, JPEG, GIF o WebP): l'immagine viene mostrata al modello nel messaggio successivo. Ce l'ha solo un agente il cui modello è contrassegnato come in grado di vedere le immagini (**Il modello vede le immagini** nell'agente). Un'immagine grande viene ridotta prima dell'invio, una GIF mostra il primo fotogramma, e davanti al modello restano solo le quattro immagini più recenti (una più vecchia diventa una nota, e l'agente può guardarla di nuovo). Le immagini provengono da te (**Carica file**, oppure [allegate a un messaggio](#the-console)); l'agente non può crearne una. |
| Immagini per i comandi | Un comando che invia immagini a un modello proprio, come [`generate-lore-entry`](extensions/lore-generator.md#commands), accetta un parametro `images`: un elenco di percorsi di file immagine dello spazio di lavoro (al massimo 8). È il modello del comando, non quello dell'agente, a dover vedere le immagini, quindi l'agente può passarle anche quando il proprio modello non vede le immagini. |
| `update_plan` | La sua lista di cose da fare. |
| `report` | Un riscontro per te (cosa ha trovato, dove, e quanto è grave: *Nota*, *Avviso* o *Problema*). L'agente prosegue dopo averlo registrato; tu leggi i riscontri nella [console](#the-console) e nella [revisione](#reviewing-the-result), oltre che nel rapporto finale. Un riscontro per chiamata, al massimo 200 per esecuzione. |
| `get_run_status` | Quanti turni e token ha usato, quanto contesto occupa, la risposta più lunga che può scrivere; il suo piano; e i suoi sotto-agenti: una riga per ciascuno con stato, turno, token, ultima azione e compito. All'agente vengono comunicati nel prompt anche questi limiti del suo modello, così scrive un testo lungo a parti. |
| `checkpoint`, `checkpoint_list`, `checkpoint_diff`, `checkpoint_restore`, `checkpoint_delete` | I suoi [punti di ripristino](#checkpoints): salva un punto di ripristino della copia di lavoro e del suo spazio di lavoro (con un'etichetta), li elenca, mostra cosa è cambiato da uno di essi (le impostazioni, le parti, i lorebook e le voci del libro aggiunte, modificate o eliminate, e i file dello spazio di lavoro; con un percorso solo quel file o quella cartella, e per un file di testo le frasi che differiscono), torna a uno di essi (alla fine del turno) ed elimina quelli che non gli servono. |
| `get_sync_report` | Solo dopo una ripresa che ha accolto modifiche tue: elenca di nuovo ciò che hai cambiato nel libro mentre l'esecuzione era in pausa e cosa di queste modifiche è entrato nella copia dell'agente, vedi [Riprendere un'esecuzione](#resuming-a-run). |
| `request_compaction` | Compatta il proprio contesto: alla fine del turno scrive il riepilogo del lavoro precedente, come fa `/compact`. Gli viene chiesto di salvare prima i propri appunti in un file. Vedi [Contesto che si riempie](#running-a-command-yourself). |
| `ask_user` | Una domanda per te. L'esecuzione attende (*In attesa di te*). |
| `finish` | Il suo rapporto: l'esecuzione è *Fatto*. Il rapporto viene mostrato come motivo dello stato. |

Come termina una porzione di lavoro:

- **In attesa di te:** l'agente ha risposto senza una chiamata a uno strumento, oppure ti ha fatto una domanda. Rispondi nella [console](#the-console).
- **Fatto:** l'agente ha chiamato `finish` (in un'esecuzione avviata *immediatamente*). Un'esecuzione avviata *nella console* è una conversazione: il rapporto dell'agente ti arriva come una domanda, l'esecuzione attende il tuo messaggio successivo, e solo **Termina** nella console (o **Interrompi**) la conclude. Il risultato di un'esecuzione *Fatta* attende la tua [revisione](#reviewing-the-result).
- **In pausa**, con il motivo: è stato raggiunto il limite di turni, il budget di token o il limite di contesto, oppure non è stato possibile interrogare il modello (un errore del fornitore, il tuo limite sull'IA), oppure il server è stato riavviato. **Riprendi** prosegue dall'ultimo turno completo, vedi [Quando il server si ferma](#when-the-server-stops).
- **Errore:** il motore di esecuzione stesso si è guastato; il motivo indica cosa è successo.

**Metti in pausa** ha effetto tra due turni: la richiesta al modello e le chiamate in corso terminano prima. Può richiedere molto tempo (un modello lento, una generazione lunga), quindi c'è **Metti in pausa ora**: annulla la richiesta al modello, la cui risposta viene scartata, e la chiamata in corso risponde alle chiamate del turno che non erano state effettuate come tali, e l'esecuzione passa a *In pausa* in un attimo. Ciò che sarebbe costata la richiesta scartata va perso; riprendi come al solito. **Interrompi** fa lo stesso: non attende la richiesta né la chiamata in corso, che vengono annullate, e l'esecuzione termina subito (conservando o scartando ciò che hai scelto).

![La domanda quando un'esecuzione viene interrotta](../images/agent-stop.png)

A un comando in corso viene chiesto di fermarsi, e un comando progettato per essere interrotto (quelli che scrivono con un modello, cercano e sostituiscono) termina subito e lascia ciò che aveva già fatto. Uno che non può essere interrotto può comunque terminare; in ogni caso all'agente viene detto che non si sa se la chiamata sia stata eseguita, ed esso verifica lo stato con un comando di lettura quando prosegue. I comandi dei sotto-agenti vengono annullati allo stesso modo quando l'esecuzione viene messa in pausa, interrotta o riceve subito un messaggio.

Mentre l'agente attende un posto libero presso il proprio modello (vedi [Un modello, più agenti](#one-model-several-agents)) è *In esecuzione*; **Metti in pausa** o **Interrompi** pongono fine a quell'attesa subito.
<a id="the-console"></a>

## La console

**Console** apre una finestra per l'ultima esecuzione dell'agente, con otto schede: **Run**, **Runs**, **Workspace**, **Checkpoint**, **Contesto**, **Copia del libro**, **Sotto-agenti** e **Diagnostica**.

![Un'esecuzione avviata nella console: l'agente chiede cosa fare](../images/agent-console-start.png)

**Run** mostra la *trascrizione*: quello che hai chiesto all'agente, quello che ha risposto (in [Markdown](https://commonmark.org/help/), quindi elenchi, titoli ed enfasi appaiono come tali) e ogni comando o strumento per i file che ha chiamato, ridotto a una riga con i suoi
argomenti (fai clic per vedere gli argomenti e il risultato, mostrato come testo semplice). Quando il modello invia il **ragionamento** (ciò che ha pensato prima di rispondere), un blocco ripiegato *Ragionamento* lo mostra prima della risposta, e il download lo include. Che tu lo riceva o meno dipende dal modello e dal server: deve segnalarlo, e la tua IA deve avere il ragionamento attivato. Mentre una richiesta è in corso, la console mostra ciò che il modello ha scritto finora (il suo ragionamento, aperto perché tu possa seguirlo, e il suo testo) in un blocco contrassegnato *Elaborazione in corso...* alla fine della trascrizione, sopra i messaggi in attesa; viene sostituito dai blocchi reali quando il turno è concluso. Mentre è visibile, la console legge l'esecuzione due volte al secondo, altrimenti ogni due secondi. Questa modalità riguarda solo l'agente stesso, non i suoi sotto-agenti, e ciò che il modello scrive all'interno di una chiamata (ad esempio un file lungo) compare solo a turno concluso. Ogni messaggio ha a destra della sua prima riga l'orario in cui è stato scritto (l'ora del giorno, con la data se è di un altro giorno; passa il mouse per l'orario completo), così puoi capire quanto ha lavorato l'agente e quando si è fermato. Lo stato dell'esecuzione, il **contesto** (quanti token ha usato l'ultima richiesta rispetto al limite del modello, così vedi quanto è vicina l'esecuzione a una pausa), i turni e i token usati (e il budget di token, quando ce n'è uno) restano sopra la trascrizione, e la casella per il tuo messaggio sotto di essa, mentre la trascrizione scorre. Ciò che l'agente attende è nella trascrizione, dove è stato scritto, come un fumetto a sé che resta lì anche quando l'esecuzione prosegue:

![La console mentre l'agente lavora: un messaggio dell'autore attende in coda](../images/agent-console-run.png)

- una **Domanda dell'agente**, quando l'esecuzione è *In attesa di te*; scrivi la risposta nella casella e premi **Invia** (o **Ctrl+Enter**), e l'agente prosegue;
- un **Rapporto dell'agente**, quando ha chiamato `finish`.

![La domanda di un agente, con le risposte che suggerisce](../images/agent-console-question.png)

![Il rapporto dell'agente alla fine di una conversazione](../images/agent-console-report.png)

![La domanda di un agente, con le risposte che suggerisce](../images/agent-console-question.png)

![Il rapporto dell'agente alla fine di una conversazione](../images/agent-console-report.png)

La riga sopra la trascrizione mostra lo stato dell'esecuzione e i suoi numeri: i turni, la dimensione del contesto rispetto al suo limite, il **Limite di risposta** (il numero massimo di token che il modello dell'agente può scrivere in una sola risposta; una chiamata che ne richiede di più viene interrotta, quindi l'agente scrive i testi lunghi a parti) e i token totali. Quando l'agente ha preparato un piano (`update_plan`), compare lì un pulsante **Piano** con i passi completati su quelli totali (**Piano 2/5**); apre i passi in un popover, ciascuno con un segno per *Fatto*, *In corso* o *Da fare*. Quando ha riportato dei risultati (`report`), compare un pulsante **Risultati** con il loro numero (**Risultati 3**) che li apre allo stesso modo, ciascuno con la sua gravità. I risultati sono conservati con l'esecuzione, quindi restano disponibili dopo una pausa, un riavvio e quando consulti un'esecuzione più vecchia nella cronologia; il file Markdown di un'esecuzione (**Scarica**) li contiene in una sezione **Risultati**.

![Il piano dell'agente](../images/agent-console-plan.png)

![I risultati dell'agente](../images/agent-console-findings.png)

In una conversazione puoi scrivere anche mentre l'agente **lavora**. Il messaggio viene **messo in coda**: compare come un fumetto tratteggiato alla fine della trascrizione e l'agente
lo legge appena il turno in cui si trova è concluso: la richiesta al modello già in corso e le chiamate che ha fatto terminano prima, e la richiesta successiva contiene già
il tuo messaggio (più messaggi in coda vanno insieme come uno solo). Quando non deve aspettare nemmeno questo, premi il piccolo pulsante **Invia ora** (il fulmine) sul fumetto: l'agente abbandona il turno in cui si trova (la richiesta al modello viene annullata, un comando che attendeva la tua approvazione
viene rifiutato, le chiamate non ancora eseguite sono segnate come saltate) e legge subito il tuo messaggio. Il pulsante **cestino** accanto lo ritira finché l'agente non lo ha ancora letto (lo stesso vale per un comando in coda). I comandi già eseguiti restano fatti. I messaggi e i comandi in coda vengono conservati anche durante un riavvio del server (vengono letti quando riprendi), e
un'esecuzione che non è una conversazione (avviata *immediatamente*) non accetta messaggi mentre lavora.
<a id="running-a-command-yourself"></a>

### Eseguire un comando tu stesso

Sotto la trascrizione c'è la riga di comando dell'applicazione (quella della scheda della storia: digita `/`, sopra compaiono l'elenco e l'aiuto del comando) con ciò che puoi eseguire sull'agente: `/compact`, `/clear-workspace` e i
[comandi che l'agente può chiamare](#allowed-commands) che leggono o aggiungono soltanto dati (un comando che modifica o rimuove dati non viene offerto, perché chiederebbe una conferma
in una finestra del libro, che un'esecuzione non ha). Sei tu a eseguire il comando, non l'agente: all'agente viene comunicato, in un messaggio tuo, che hai eseguito il comando e cosa ha risposto
(e che ciò che ha letto prima potrebbe essere superato se il comando ha modificato la copia di lavoro), così non crede mai di aver fatto una chiamata che non ha fatto. Funziona come un messaggio:
dove l'esecuzione attende (*In attesa di te*) o è *In pausa*, il comando viene eseguito subito e l'esecuzione resta com'è; dove una [conversazione](#the-console) è in corso, viene messo in coda, come una bolla tratteggiata contrassegnata *Comando*, ed eseguito alla fine del turno dell'agente, dove l'agente avrebbe atteso (l'agente non prosegue a causa sua). Il piccolo pulsante **Esegui ora** sulla bolla interrompe il giro in cui si trova l'agente, esegue il comando e lascia l'agente proseguire da lì. Un comando che richiede molto tempo occupa la casella: attendi che finisca prima di scrivere all'agente.

![Un comando dell'autore nella console: /compact](../images/agent-console-command.png)

**`/compact [what to stress]`** libera il contesto. All'agente viene chiesto di leggere tutto ciò che c'è finora e di scrivere un riassunto (cosa hai chiesto, cosa è fatto, cosa ha trovato,
i suoi appunti e il suo piano, cosa stava per fare); quel riassunto sostituisce tutto ciò che lo precede in ciò che il modello legge. Nella trascrizione vedi ancora tutto,
e il riassunto come blocco ripiegato, **Riassunto del lavoro precedente**; anche l'esportazione lo conserva. Il testo dopo il comando, se c'è, è ciò che il riassunto dovrebbe mettere in evidenza.
La richiesta è propria dell'agente e i suoi token contano sull'esecuzione. La domanda che all'agente viene posta è il suo **Prompt di compattazione**: quello dell'agente, altrimenti il tuo
predefinito nelle [Impostazioni](account.md#templates), altrimenti quello dell'amministratore, altrimenti quello integrato. Se il contesto è già così pieno che la richiesta non ci sta,
vengono lasciati fuori i messaggi più vecchi (il riassunto dice quanti). Fallo prima che il contesto sia pieno: la riga di utilizzo mostra quanto è vicino. Subito dopo la compattazione il contesto mostrato è una stima di ciò che la richiesta successiva conterrà (il riassunto, il prompt di sistema e le definizioni degli strumenti); la prima risposta del modello dopo di essa lo sostituisce con il numero reale.

**Contesto che si riempie.** Quando il contesto del modello supera il 75% del suo limite, l'agente riceve un avviso dopo ogni giro (un messaggio dell'esecuzione, che vedi nella trascrizione): quanto è pieno, e che deve salvare in un file del suo spazio di lavoro ciò che non deve perdere, aggiornare il suo piano e chiamare `request_compaction`. Oltre il 90% l'avviso dice che l'esecuzione si interrompe quando il contesto è pieno. Se l'agente non compatta prima che il contesto superi la quota **Compatta a** della sua definizione (90% salvo tua indicazione diversa), l'esecuzione lo fa da sola: la stessa compattazione, il riassunto inizia con "compattato automaticamente" e l'agente non viene avvisato in aggiunta. Una compattazione che fallisce non cambia nulla, l'agente viene informato, e l'esecuzione riprova solo qualche giro più tardi. La compattazione richiesta dall'agente è la stessa della tua (il suo **Prompt di compattazione**, vedi [Creare e modificare un agente](#creating-and-editing-an-agent), i suoi token contano sull'esecuzione); se fallisce, non cambia nulla e l'agente viene informato.
Gli avvisi richiedono un limite di contesto: il più basso tra il limite dell'agente e i limiti del suo modello.

**Una chiamata che il server del modello non ha potuto leggere.** Quando un modello scrive un testo lungo dentro una chiamata, la sua risposta può terminare a metà del testo (si raggiunge il limite di *una risposta*, i **reply tokens** del protocollo o i **max completion tokens** dell'AI, si applica un valore predefinito del server quando nessuno dei due è impostato, oppure il modello semplicemente si ferma; il contesto, per quanto pieno, è un limite diverso), e il server rifiuta la chiamata incompleta ("Failed to parse tool call arguments as JSON"). Chiedere di nuovo con la stessa conversazione farebbe ripetere lo stesso errore, quindi l'esecuzione non si limita a fermarsi: nella trascrizione viene detto all'agente che la sua ultima risposta non è stata utilizzabile e che non è stato fatto nulla, e di scrivere i testi lunghi in un file del suo spazio di lavoro in più passi più piccoli; poi gli viene chiesto di nuovo, fino a tre volte di seguito. Se continua a fallire, l'esecuzione si mette in pausa, e ciò che gli è stato detto resta nella trascrizione, quindi un **Riprendi** pone una domanda diversa da quella che ha fallito. Lo stesso vale per un sotto-agente.

Una chiamata interrotta che però è arrivata all'applicazione (il risultato della chiamata dice "non è JSON valido ... interrotta a metà") resta nella trascrizione come l'ha scritta il modello, così puoi leggerla, ma viene rimandata al modello con argomenti vuoti: un server che rilegge le chiamate precedenti rifiuterebbe ogni richiesta successiva, e l'esecuzione non potrebbe proseguire senza un ripristino.

**Chiamate fuori controllo.** Un modello può cadere in un ciclo e scrivere la stessa chiamata centinaia di volte in una sola risposta; i risultati riempirebbero il contesto prima della richiesta successiva. L'esecuzione effettua al massimo 32 chiamate per risposta, e la ripetizione di una chiamata che si limita a leggere (lo stesso strumento con gli stessi argomenti, nella stessa risposta) viene eseguita una sola volta. Le chiamate escluse non compaiono nella trascrizione, e l'agente riceve un avviso che lo dice.

**Girare in tondo.** Un modello può anche ripetersi su molte risposte: la stessa ricerca (elencare i file, elencare i lorebook...) con lo stesso risultato, giro dopo giro, senza arrivare da nessuna parte. L'esecuzione lo tiene d'occhio. Quando l'agente fa giri che soltanto guardano, non cambiano nulla e sono uguali a quelli precedenti (stesse chiamate, stessi risultati), al terzo viene detto che nulla è cambiato. Se prosegue, al quinto i suoi strumenti vengono tolti per una richiesta: deve rispondere in testo semplice spiegando cosa stava facendo e cosa lo blocca, e l'esecuzione si **mette in pausa** con quella risposta come ultimo messaggio, così vedi perché era bloccato invece di pagare altri cento giri. **Riprendi** (o scrivergli) restituisce gli strumenti e ricomincia a contare da capo. Un giro in cui qualcosa è cambiato, o mentre i sotto-agenti lavorano, non viene conteggiato. Un sotto-agente che si ripete viene fermato allo stesso modo, e ciò che ha detto è la sua risposta all'agente che lo ha avviato.

**`/clear-workspace [what to keep]`** elimina i file e tutte le cartelle dello [spazio di lavoro](#the-working-copy) dell'esecuzione (dopo la tua conferma, come con il pulsante dello spazio di lavoro). Indica cosa conservare dopo, separato da spazi: file, cartelle (con tutto ciò che contengono) o schemi come `*.md` o `chapters/*.md`, un percorso con spazi tra doppi apici
(`/clear-workspace /notes.md "/my drafts" *.png`). Le cartelle sopra un file conservato restano. Se qualcosa indicato non c'è, non viene eliminato nulla. `/AGENTS.md` e i checkpoint non
fanno parte dello spazio di lavoro e non vengono mai toccati. All'agente viene detto cosa è stato eliminato e cosa conservato. Nella trascrizione, un comando che hai eseguito è una bolla con il comando e ciò che ha risposto.

Quando il modello dell'agente vede immagini (vedi [Creare e modificare un agente](#creating-and-editing-an-agent)), **Allega immagine** si trova accanto a **Invia**: scegli un file PNG, JPEG, GIF o WebP, oppure incolla un'immagine dagli appunti
nella casella del messaggio (**Ctrl+V**), e attende sopra la casella (la croce la ritira) fino all'invio. Con un messaggio vanno fino a otto immagini; il testo può essere vuoto.
Le immagini vengono salvate nella cartella **uploads** dello spazio di lavoro (un nome già preso riceve un numero: `map-2.png`), entro i limiti e le regole dello spazio di lavoro,
e al modello vengono mostrate insieme al tuo messaggio. Eliminale nella scheda **Spazio di lavoro** quando non ti servono più.

I pulsanti in basso sono **Pausa**, **Riprendi** e **Interrompi**, che fanno ciò che indicato nella tabella, e **Termina**, che chiude un'esecuzione in attesa di te (il suo lavoro è finito, *Fatto*).

La finestra controlla l'esecuzione ogni due secondi mentre è aperta; **Aggiorna** lo fa subito. La scheda **Esecuzione** mostra l'ultima esecuzione non scartata, quindi un'esecuzione terminata o fallita può ancora essere letta lì finché l'agente non avvia una nuova esecuzione; le più vecchie sono in **Esecuzioni**.

**Esecuzioni** è la cronologia dell'agente: ogni esecuzione che ha avuto, la più recente per prima, con data, stato, giri, token ed esito. Usa l'occhio per **aprire** un'esecuzione nella scheda **Esecuzione** e rileggerne i passi e il rapporto (l'esecuzione aperta resta lì finché non ne apri un'altra), la freccia per **scaricare** la sua trascrizione come file Markdown (ciò che è stato detto e quando, ogni comando con i suoi argomenti e il suo risultato, il rapporto, le conversazioni dei sotto-agenti), e il cestino per **eliminare** l'esecuzione.
Un'esecuzione eliminata va nel [cestino](trash.md). Un'esecuzione in corso non può essere eliminata: prima interrompila. Le esecuzioni che hai interrotto e **scartato** sono comunque nell'elenco,
così puoi leggere cosa hanno fatto o eliminarle; la scheda **Esecuzione** le mostra solo quando le apri.

![La scheda Esecuzioni](../images/agent-runs.png)

**Spazio di lavoro** mostra i file dell'agente: i suoi appunti e i suoi rapporti, che conserva tra un'esecuzione e l'altra. Clicca su una cartella per entrarvi (**Su** torna indietro), clicca su un file di testo per leggerlo, usa le frecce per scaricare un file o il cestino per eliminarlo, e **Svuota** elimina tutto (appunti superati confondono un'esecuzione nuova). **Nuova cartella** crea una cartella in quella aperta e **Carica file** vi mette un file dal tuo computer, così puoi consegnare all'agente
un brief, una guida di stile o una bozza. Valgono le stesse regole dei file propri dell'agente: i nomi non possono contenere `\ < > : " | ? *`, un punto o uno spazio finale, né essere troppo lunghi o troppo profondi; si applicano i limiti di dimensione del file e di numero di file, e un file caricato deve essere testo (UTF-8),
perché l'agente può leggere solo testo, un'immagine (PNG, JPEG, GIF o WebP, riconosciuta dal contenuto del file e non dal nome;
fino a 5 MB, e conta nel totale e nel numero di file come ogni file) o un file zip (fino a 10 MB, conservato così com'è; l'agente lo decomprime, vedi [sotto](#unpacking-a-zip-file)). Un file non viene mai sostituito da un caricamento: elimina prima quello vecchio.
I file non possono essere modificati mentre l'agente lavora, mettilo prima in pausa. Lo spazio di lavoro ha un limite di dimensione, di dimensione dei file e di numero di file, che il tuo amministratore può impostare nel tuo [permesso](administration/permissions.md#settings-of-a-permission); la riga
sotto la tabella dice quanto è usato. Il nome `AGENTS.md` in alto è occupato dalle tue istruzioni, che l'agente vede lì (il file non può essere scritto o eliminato).

![La scheda Spazio di lavoro](../images/agent-console-workspace.png)
<a id="unpacking-a-zip-file"></a>

### Decompressione di un file zip

Carica un file zip nella scheda **Spazio di lavoro** (**Carica file**) quando l'agente deve lavorare su molti file insieme, per esempio i capitoli
di una bozza, e chiedigli di decomprimerlo: l'agente dispone dello strumento `unzip_file`, che decomprime `/uploads/draft.zip` nella nuova cartella
`/uploads/draft` (oppure in una cartella che gli viene indicata; la cartella non deve esistere ancora). La decompressione avviene tutto o niente, e un archivio non viene mai considerato attendibile:

- Vengono conservati solo i file di testo (UTF-8), le immagini (PNG, JPEG, GIF, WebP) e i file zip. Le altre voci vengono escluse e indicate per nome nella risposta all'agente. Un file zip al suo interno viene salvato, mai decompresso da solo.
- I file devono rientrare nello spazio disponibile dello spazio di lavoro (dimensione, dimensione del singolo file, numero di file), altrimenti non viene decompresso nulla.
- Un archivio con una voce che porta al di sopra della cartella (`../x`) o che inizia con `/` viene rifiutato per intero, così come uno con lo stesso nome due volte (anche con differenze di maiuscole o minuscole), un nome che è insieme file e cartella, più di 5 000 voci, oppure un archivio cifrato o danneggiato.
- Una zip bomb viene rifiutata: un archivio che, decompresso, occupa più di quanto lo spazio di lavoro può contenere, oppure più di 100 volte la sua dimensione.

<a id="sub-agents"></a>

## Sotto-agenti

Un agente può affidare un compito a un **sotto-agente**: un assistente con una conversazione propria, che svolge un solo compito e risponde con un testo. Dispone dei comandi e dei file dell'agente (oppure del modello su cui gira, vedi [sotto](#models-for-sub-agents)) e lavora nella stessa copia di lavoro e nello stesso spazio di lavoro (ciò che modifica nel libro resta), ma non conosce la conversazione dell'agente, quindi l'agente deve esprimere tutto nel compito. Il vantaggio è che ciò che il sotto-agente legge e prova resta fuori dal contesto dell'agente: verificare un capitolo, controllare i nomi, abbozzare una parte.

Imposta **Numero massimo di sotto-agenti** nell'[agente](#creating-and-editing-an-agent) per indicare quanti possono lavorare contemporaneamente. Zero (il valore predefinito) significa nessuno: all'agente non viene nemmeno detto che esistono.

- Con **uno**, l'agente avvia un sotto-agente e attende (viene *sospeso*) finché il sotto-agente non ha finito; la chiamata restituisce la sua risposta.
- Con **più di uno**, avviare un sotto-agente restituisce subito il controllo e l'agente può proseguire, avviarne altri, guardarli (`agent_status`), attendere una risposta (`await_agent`) o interromperne uno (`cancel_agent`). La chiamata che raggiunge il limite attende finché uno di loro non termina e restituisce ciò che è terminato.

Regole valide in entrambi i casi:

- Un sotto-agente **legge l'intero spazio di lavoro ma scrive solo in una cartella propria**, `/tmp/sub1`, `/tmp/sub2`... (viene creata all'avvio del sotto-agente, e ciò che un'esecuzione precedente ha lasciato in una cartella con lo stesso nome viene cancellato). La sua risposta viene salvata lì come `result.md`, quindi quando una risposta lunga appare troncata, l'agente la legge per intero da quel file. I file si trovano nella scheda **Spazio di lavoro**; occupano una parte della quota dello spazio di lavoro e restano finché non li cancelli.
- Un sotto-agente gira sul modello principale dell'agente, a meno che l'agente non ne richieda un altro, vedi [Modelli per i sotto-agenti](#models-for-sub-agents).
- Un comando di un sotto-agente che modifica i dati segue l'impostazione **Comandi distruttivi** dell'agente: con *Chiedi prima* la stessa [finestra](#asking-you) ti chiede conferma, e quel sotto-agente attende la tua risposta (mettere in pausa l'esecuzione lo rifiuta).
- Un sotto-agente non può **avviare sotto-agenti**, concludere l'esecuzione, chiederti qualcosa o creare punti di ripristino; termina rispondendo con un testo.
- Tutto conta nell'**unico budget dell'esecuzione**: turni, token, i limiti del modello. Un sotto-agente che raggiunge un limite fallisce e lo segnala; l'agente raggiunge poi lo stesso limite.
- Quante richieste sono in corso verso il modello nello stesso momento è determinato dal valore **Richieste al modello contemporanee** impostato dal tuo amministratore (uno, se non è impostato): con uno, l'agente e il suo sotto-agente si alternano.
- Le chiamate che modificano il libro o lo spazio di lavoro vengono eseguite una alla volta, chiunque le effettui, quindi due sotto-agenti non scrivono mai nello stesso istante. Ciononostante, non chiedere a due di loro di modificare la stessa parte.
- **Mettere in pausa l'esecuzione mette in pausa anche i sotto-agenti.** Si fermano al loro prossimo punto stabile (tra due richieste o due chiamate, subito se metti in pausa subito) e ciò che hanno detto e fatto viene conservato con l'esecuzione, così un **Riprendi** prosegue esattamente da dove tutto era rimasto: i sotto-agenti continuano la loro conversazione, e una chiamata dell'agente che attendeva uno di loro (`await_agent`, o `spawn_agent` con un limite di uno) torna ad attendere, come se nulla fosse successo. Un sotto-agente già terminato prima della pausa resta disponibile perché l'agente possa interrogarlo. Interrompere un'esecuzione e conservarla produce lo stesso effetto; scartarla, ripristinare un punto di ripristino (il libro in cui lavoravano i sotto-agenti viene sostituito) e la conclusione dell'agente o una sua richiesta di chiarimenti li terminano in modo definitivo. Un agente che termina il suo turno mentre i sotto-agenti stanno ancora lavorando viene avvisato una volta; se lo fa di nuovo, vengono interrotti.
- Le loro conversazioni non compaiono nella scheda **Esecuzione**. La scheda **Sotto-agenti** della console le mostra: seleziona una voce in alto per leggere ciò che l'agente le ha chiesto, ciò che ha risposto, i comandi chiamati con i relativi risultati e come si è conclusa (la risposta data, oppure il motivo per cui è fallita o è stata annullata), negli stessi blocchi della trascrizione. Segue l'esecuzione mentre lavora e mostra anche i sotto-agenti di un'esecuzione precedente (seleziona prima quell'esecuzione nella scheda **Esecuzioni**). Una chiamata `spawn_agent` nella scheda **Esecuzione** ha un pulsante **Sotto-agente** che apre la sua conversazione. Una conversazione su cui l'agente è tornato indietro (un riavvolgimento) appare in grigio e con la dicitura *dimenticato dall'agente*. Il **download** dell'esecuzione (nella scheda **Esecuzioni**) contiene una sezione per ciascun sotto-agente, con il suo compito, il suo stato e ogni messaggio e chiamata.

![La scheda Sotto-agenti](../images/agent-subagents.png)

<a id="models-for-sub-agents"></a>

### Modelli per i sotto-agenti

L'agente ha un **modello principale**, scelto in alto nella finestra di dialogo. Sotto **Modelli per i sotto-agenti** puoi aggiungerne altri, per esempio un modello economico che si limita a leggere o un modello fluido che abbozza. Ognuno ha un **ruolo** (il suo nome), una **descrizione** (quando usarlo: l'agente la legge per scegliere), la sua IA e il protocollo, un **limite di contesto** facoltativo, e ciò che può usare:

![Un modello per i sotto-agenti](../images/agent-models.png)

- **Può usare comandi e strumenti.** Disattivato, un sotto-agente su questo modello si limita a rispondere con testo: non riceve né comandi né strumenti.
- **Limita i comandi.** Disattivato, un sotto-agente su questo modello ha i comandi dell'agente. Attivato, solo quelli che selezioni, e tra questi solo quelli consentiti all'agente (i comandi consentiti all'agente restano il limite massimo). Gli strumenti per i file non sono comandi, quindi ogni sotto-agente li ha. Il modello principale ha la stessa impostazione, per i sotto-agenti che girano su di esso (il pulsante **Modifica** della sua riga).

L'agente conosce i ruoli, le descrizioni e i comandi di ciascun modello, e avvia un sotto-agente su uno di essi con `profile` di `spawn_agent`; se lo omette, si usa il modello dell'agente stesso. La casella **I sotto-agenti possono usare il modello principale** (attiva per impostazione predefinita) disattiva quest'ultima scelta: l'agente deve allora indicare un altro modello e, se non ne esiste nessuno, non riceve affatto strumenti per i sotto-agenti. Un sotto-agente ha i limiti del proprio modello (quanto può leggere e quanto può essere lunga la sua risposta), e la scheda **Sotto-agenti** e la diagnostica indicano su quale modello ha girato ciascuno.

<a id="one-model-several-agents"></a>

## Un modello, più agenti

Un server locale spesso ha un solo slot (una GPU): se gli chiedi due risposte contemporaneamente, entrambe rallentano molto o vanno in timeout. Le **Connessioni** del [modello](inference-providers.md#general-settings) (1, a meno che tu non ne imposti di più) fanno sì che ogni richiesta verso di esso si alterni, nell'ordine in cui è arrivata: le richieste dei tuoi agenti, degli agenti di altri utenti e la tua scrittura nella finestra della storia. Un'esecuzione in attesa resta *In esecuzione*, e **Pausa** o **Interrompi** pongono subito fine all'attesa. Tra due risposte del modello un agente non tiene occupata alcuna connessione, quindi la tua generazione può inserirsi nel mezzo.
<a id="when-you-are-not-there"></a>

## Quando non ci sei

Un agente che ha bisogno di te, termina o fallisce lo segnala alle schermate aperte dell'applicazione (il badge sulla scheda, un avviso). Quando nessuna tua schermata è aperta può anche inviare un breve messaggio a un indirizzo che indichi in [Impostazioni](account.md), se un amministratore ti ha concesso il [permesso *Notifiche*](administration/permissions.md) (gli amministratori lo hanno): il tuo topic **ntfy** (l'app ntfy sul telefono lo mostra subito), un URL di messaggio **Gotify** o un webhook **Discord**; scegli il **Formato** richiesto dal servizio. **Invia un messaggio di prova** lo prova con quello che c'è nei campi. Il messaggio indica il nome dell'agente e cosa vuole ("aspetta te", "ha terminato", "chiede l'approvazione del comando ...", "non riuscito", "è stato messo in pausa: motivo") nella tua lingua. **Anche quando l'applicazione è aperta** lo invia comunque.

Il server chiama l'indirizzo, quindi un amministratore può disattivarlo per tutti (Admin, *Agenti*), e vengono chiamati solo indirizzi su internet, a meno che l'amministratore non abbia spuntato anche **Consenti indirizzi su questa macchina e nelle reti private** nel tuo permesso: per un ntfy o un Gotify sulla stessa macchina o sulla tua rete chiedilo. Il messaggio di prova dice quando l'indirizzo viene rifiutato. Tratta l'indirizzo come una password quando contiene un segreto: chiunque lo conosca può leggere quello che viene inviato, il server non lo scrive mai nel suo log, i reindirizzamenti non vengono seguiti, e vengono chiamati solo `http` e `https`.

<a id="asking-you"></a>

## Chiedere a te

Quando l'agente ha bisogno di te, lo sai ovunque tu sia nell'app:

- un **numero sulla scheda Agenti** conta gli agenti che aspettano te, e il pulsante **Console** di un agente del genere è evidenziato;
- un **avviso** compare con un pulsante **Apri** che apre la console di quell'agente.

L'agente ha bisogno di te quando *ti fa una domanda o aspetta il tuo prossimo messaggio*, ti chiede di **approvare un comando**, *ha terminato*, *è fallito*, oppure è stato *messo in pausa da solo* (si raggiunge un limite, non è stato possibile interpellare il modello, il server è stato riavviato). Una pausa che hai chiesto tu non conta.

Gli agenti lavorano mentre sei assente, quindi questo non dipende dal fatto che l'applicazione sia aperta: il numero c'è quando accedi di nuovo, e ogni finestra del browser che hai aperta riceve l'avviso. Aprire la console dell'agente lo cancella (le altre finestre si aggiornano).

Quando l'agente è impostato su **Chiedi prima** e vuole eseguire un comando che modifica o rimuove dati, l'esecuzione si ferma in *In attesa di approvazione* e sopra la console compare una finestra con il comando e i suoi argomenti. **Approva** lo lascia eseguire, **Rifiuta** dice all'agente che hai rifiutato e lui prosegue. Mettere in pausa o fermare l'esecuzione (o fermare il server) non conta come rifiuto: all'agente non viene detto nulla, e quando **Riprendi**, la finestra ricompare per lo stesso comando. Il comando funziona comunque sulla [copia](#the-working-copy) del libro. Se la console è chiusa, la finestra compare non appena la apri.

![La finestra che chiede l'approvazione di un comando](../images/agent-approval.png)

<a id="when-the-server-stops"></a>

## Quando il server si ferma

Fermare il server mette in pausa le esecuzioni che stanno lavorando, come fa **Metti in pausa ora** (gli concede alcuni secondi): la richiesta e la chiamata in corso vengono abbandonate, l'esecuzione è *In pausa* con il motivo "Il server è stato fermato", e un **Riprendi** prosegue. Se invece il server viene terminato o si arresta in modo anomalo, il successivo avvio mette in pausa le esecuzioni che trova in lavorazione ("Interrotta da un riavvio del server"). Tutto ciò che l'esecuzione ha fatto viene conservato: i turni salvati, la copia, lo spazio di lavoro e i messaggi e i comandi che avevi in coda. Si perde solo il turno non terminato. Riprendendo, viene gestita la chiamata in cui si trovava l'esecuzione:

- Una chiamata che **non era ancora iniziata** (aspettava la tua approvazione, o il suo turno dopo un'altra chiamata) viene rifatta, quindi un comando che richiede la tua approvazione te lo chiede di nuovo. Anche una chiamata che si limita a leggere viene rifatta.
- Una chiamata che **aveva già iniziato a modificare qualcosa** potrebbe essere stata eseguita in parte: all'agente viene detto che il suo esito è sconosciuto e lo verifica con uno strumento di lettura prima di ripeterla; le chiamate successive vengono segnate come non eseguite.
- Un comando tuo che era in esecuzione quando il server si è fermato prosegue insieme all'esecuzione. `/compact` e `/clear-workspace` vengono eseguiti di nuovo; qualsiasi altro comando già iniziato **non** viene eseguito di nuovo (potrebbe essere stato fatto in parte): all'agente viene detto e lo verifica. I messaggi e i comandi arrivati dopo vengono consegnati come al solito.
- Il piano dell'agente e i risultati dei suoi comandi (`list_command_results`, `@result`) vengono conservati anch'essi, così l'agente li ha dopo il riavvio.
- Le chiamate di un sub-agente in corso vengono trattate come quelle dell'agente: quelle non ancora iniziate vengono fatte, quella già iniziata viene riportata al sub-agente come "esito sconosciuto", e un sub-agente che aspettava la tua approvazione te la chiede di nuovo.
- Se il server viene **terminato** (`kill -9`, un'interruzione di corrente) mentre un comando è in esecuzione, il comando muore con lui; quello che ha fatto è come descritto sopra.

<a id="the-working-copy"></a>

## La copia di lavoro

Ogni esecuzione lavora su una **copia del tuo libro** e dei suoi lorebook che l'app crea all'avvio dell'esecuzione: i messaggi, i riepiloghi, i tag e le impostazioni così come sono in quel momento, e il lorebook con i suoi sotto-lorebook. La copia è nascosta: non compare negli elenchi di libri e lorebook. Qualsiasi cosa faccia l'agente, la fa sulla copia; il tuo libro resta com'era.

Quando l'agente ha **Scrivi il libro con il modello dell'agente** attivo (come impostazione predefinita), la copia riceve anche il **modello dell'agente** (l'IA e il protocollo del suo profilo di modello principale) al posto dell'IA e del protocollo del tuo libro, ogni volta che l'esecuzione parte o viene ripresa. Quindi i comandi che scrivono con un modello (`/next`, `/swipe`, `/regenerate`, `/redraft-part`, `/generate-annotation`, i riepiloghi) e i comandi di modello delle estensioni (il Lore Generator, l'Arc Outline) usano il modello dell'agente anche quando il tuo libro è impostato su un altro, o su nessuno; una `profile` indicata nella chiamata ha comunque la precedenza. Con l'impostazione disattivata usano il modello del libro. Non si tratta di una modifica del tuo libro: la revisione non lo elenca e applicare il risultato lascia l'IA e il protocollo del tuo libro come sono.

La copia viene messa nella [cartella](books/folders.md) del tuo libro, così l'agente scrive con le impostazioni che le cartelle definiscono (lingua, template, punto di vista, tempo verbale, stile e prompt), come fa il libro. La copia non è contenuto della cartella: non viene conteggiata lì e il lavoro al suo interno non cambia la *Last modified* della cartella. La copia segue il libro se lo sposti mentre l'esecuzione è in pausa. Applicare il risultato non sposta mai il tuo libro.

Un'esecuzione in pausa conserva la sua copia e ci lavora di nuovo quando la riprendi. Un'esecuzione *Fatto*, o che hai fermato e conservato, la mantiene finché non [prendi il suo risultato o lo scarti](#reviewing-the-result). Quando l'esecuzione è *Non riuscita*, scarti il suo risultato, oppure le sue modifiche vengono applicate, la copia viene eliminata definitivamente, e la prossima esecuzione ne crea una nuova dal libro com'è in quel momento.
<a id="resuming-a-run"></a>

## Ripresa di un'esecuzione

Un'esecuzione in pausa o conservata non blocca il tuo libro, quindi nel frattempo puoi modificarlo. Quando **riprendi** l'esecuzione, la sua copia di lavoro si aggiorna con quello che hai fatto:

- Ciò che hai cambiato e l'agente no (una parte, una voce del lorebook, un'impostazione, un tag...) entra nella copia. Anche una parte che hai aggiunto entra, e l'agente può basarsi su di essa; ciò che hai eliminato viene eliminato dalla copia.
- Ciò che tu e l'agente avete cambiato in testi diversi *resta come l'agente lo ha*: non si perde nulla di ciò che ha fatto l'agente. È un [conflitto](#reviewing-the-result) che risolvi quando rivedi il risultato, con entrambi i testi davanti.
- Ciò che hai cambiato e l'agente ha eliminato resta eliminato, e ciò che hai eliminato e l'agente ha cambiato resta nella copia; la revisione mostra entrambi i casi.

Non serve alcun clic: la sincronizzazione fa parte della ripresa e richiede un momento (pochi secondi se hai cambiato centinaia di elementi). Lo stato della copia prima della sincronizzazione viene salvato come [checkpoint](#checkpoints) automatico. All'agente viene comunicato cosa è successo, usando i nomi con cui conosce gli elementi, in un messaggio quando riprende a lavorare ("Mentre l'esecuzione era in pausa l'utente ha modificato il libro..."), e può chiederlo di nuovo con `get_sync_report`.

**Un conflitto non si risolve da solo.** Ogni ripresa confronta di nuovo la copia con il libro, quindi un conflitto viene segnalato di nuovo, e l'agente ne viene informato di nuovo a ogni ripresa, finché non lo risolvi: modificando la tua parte del libro (portandola al testo dell'agente, o al testo originale), oppure durante la revisione.

**Una nota con la ripresa.** **Riprendi** apre una piccola finestra con il campo *Nota per l'agente (facoltativa)*. Quello che scrivi lì è la prima cosa che l'agente legge quando riprende, dopo il resoconto delle modifiche che hai fatto al libro. Serve per un cambio di direzione ("non toccare il lorebook", "parti dal capitolo 3") e, a differenza di un messaggio in una conversazione, funziona per ogni esecuzione, anche per una che non sta aspettando i tuoi messaggi. Lasciala vuota per riprendere come prima.

![Ripresa di un'esecuzione con una nota](../images/agent-resume.png)

<a id="reviewing-the-result"></a>

## Revisione del risultato

Quando un'esecuzione è *Fatto* (o l'hai interrotta con **Interrompi e conserva il risultato**), la riga dell'agente e la console hanno un pulsante **Rivedi le modifiche**. Apre una finestra con tutto ciò che l'agente ha cambiato, confrontato con il tuo libro **così com'è ora**, una riga per ciascuna modifica. Quando l'agente ha segnalato dei rilievi, sono elencati sopra le modifiche (**Rilievi (3)**), così un controllo che non ha cambiato nulla ha comunque qualcosa da mostrare:

![Revisione del risultato di un'esecuzione](../images/agent-review.png)

- **Libro:** il nome, la descrizione, la lingua, il punto di vista, il tempo verbale, lo stile, i modelli, il numero di versioni conservate per parte, i suoi tag e i dati che un'estensione conserva nel libro (una traccia dell'arco narrativo, il tracciatore dei personaggi).
- **Parte:** una parte il cui testo, le istruzioni, l'ambientazione della scena, il personaggio con il punto di vista, i personaggi presenti o il riassunto sono stati cambiati dall'agente; una parte che ha scritto; una parte che ha eliminato. Le variabili che una generazione conserva con una parte (e i dati delle estensioni) vengono riprese insieme ad essa, nel campo **Dati**.
- **Lorebook** e **Voce:** un lorebook o una voce creati, modificati (anche spostati, rinominati, con altri tag o altri sottolibri) o eliminati dall'agente.
- **Annotazione:** una [nota](books/annotations.md#annotations-and-agents) su un passaggio di una parte, creata, modificata (la nota, il tipo, lo stato) o eliminata dall'agente.

Seleziona le righe che vuoi, poi **Applica le modifiche scelte**. I pulsanti sopra l'elenco selezionano **tutte** le righe, **nessuna**, solo **parti nuove** (il nuovo ramo scritto dall'agente, nient'altro), solo **annotazioni** oppure tutto **senza conflitti**. Una riga che ne richiede un'altra (una parte nuova richiede la parte a cui si collega, se anche quella è nuova) porta con sé quest'ultima quando la selezioni, e perde le righe che ne dipendono quando la deselezioni. Fai clic su una riga per vederla a destra: il testo del tuo libro ora e il testo che verrà scritto, **che puoi modificare** prima di applicare. Se non scrivi nulla, viene applicato il testo dell'agente.

Il libro può essere cambiato dopo l'inizio dell'esecuzione (puoi modificarlo appena l'esecuzione non sta lavorando). Ciò che hai cambiato solo tu non è nell'elenco e resta. Ciò che avete cambiato entrambi nello stesso modo non è una modifica. Ciò che avete cambiato entrambi in testi **diversi** è un **conflitto**: parte deselezionato e, quando lo selezioni, il testo che vedi a destra sostituisce il testo del libro. **Usa quello del libro** mette nella casella il testo del libro (la riga quindi non cambia nulla per quel campo), **Usa quello dell'agente** rimette quello dell'agente; oppure scrivi tu il risultato. Una riga che non può essere applicata (la parte a cui si collega, o il lorebook in cui si trova, è stata eliminata dal tuo libro) lo segnala e non può essere selezionata.

![Un conflitto nella revisione](../images/agent-review-conflict.png)

Sotto i testi, **Differenze** mostra cosa cambia **parola per parola**: ciò che è solo nel libro ora è barrato in rosso, ciò che è solo nel testo da applicare è in verde (segue quello che scrivi nella casella). Per un conflitto mostra anche ciò che ha cambiato l'agente e ciò che hai cambiato tu dall'inizio dell'esecuzione, ciascuno rispetto al libro com'era allora, così puoi vedere chi ha fatto cosa. Una riga che l'agente ha cambiato in più di un aspetto (una parte il cui testo e l'ambientazione della scena sono cambiati, una voce con un nuovo contenuto e una nuova nota) ha accanto a ciascun aspetto una casella **Accetta questa modifica**: deselezionala per applicare la riga senza quell'aspetto, e il libro mantiene il proprio valore per quel campo. La posizione di una parte nella storia segue la parte e non ha casella, e ciò che l'agente ha creato o eliminato viene accettato o lasciato nel suo insieme. **Mostra nella copia** (su una riga creata o modificata dall'agente) apre la [copia del libro](#the-copy-of-the-book) in corrispondenza della parte o della voce, per leggerla al suo posto.

![Le differenze di un conflitto, parola per parola](../images/agent-review-differences.png)
<a id="data-that-extensions-keep-json"></a>

### Dati che le estensioni conservano (JSON)

Le estensioni conservano i propri dati in un libro, nelle sue parti, nei lorebook e nelle voci (una scaletta dell'arco, il tracker dei personaggi, le variabili di una generazione). Sono dati JSON, e la revisione li mostra **formattati** con la riga **Unisci i dati JSON** accanto al testo che verrà applicato. I dati non vengono acquisiti interi: ciò che è cambiato solo nell'agente viene preso dall'agente, ciò che è cambiato solo nel tuo libro resta, e ciò che è cambiato in entrambi in **punti diversi** viene unito punto per punto. È un conflitto solo quando entrambi hanno cambiato lo **stesso** punto con valori diversi (l'ordine delle chiavi non conta); finché non decidi, in quel punto viene usata la versione dell'agente.

**Unisci i dati JSON** apre una finestra con i dati delle tre versioni sovrapposte come un albero: *All'inizio dell'esecuzione*, *Ora nel libro*, l'*Agente* e il *Risultato*, ciascuno in una colonna, con un segno per ciò che è cambiato e dove (l'agente, il libro, entrambi, un conflitto). Vengono mostrati solo i punti cambiati (deseleziona **Solo ciò che è cambiato** per tutti); un punto si apre solo quando lo apri tu, quindi un documento grande non è un problema.

![I dati di un'estensione sovrapposti](../images/agent-json-merge.png)

- Seleziona un punto e prendi i suoi dati da una versione (**Usa quello dell'agente**, **Usa quello del libro**, **Usa quello dell'inizio dell'esecuzione**), **Escludi** (la chiave o l'elemento non è nel risultato), oppure **Unisci a partire dall'interno** (decidono i punti interni, ciascuno per conto proprio). Una chiave o un elemento che esiste in una sola versione viene copiato nel risultato prendendolo da quella versione. Prendere un intero oggetto o una lista da una versione annulla ciò che hai scelto al suo interno, ma non in modo definitivo: unisci di nuovo a partire dall'interno e le tue scelte tornano.
- Gli elementi di una **lista** non hanno nomi, quindi vengono abbinati in base a ciò che sono: prima gli stessi dati, poi gli stessi `uuid`, `id` o `name`, poi quelli che si assomigliano di più; ciò che resta, in egual numero e dello stesso tipo, viene considerato cambiato. Quando l'ipotesi è sbagliata, **Dividi** un elemento in quelli di ciascuna versione, oppure **Abbina a…** un altro elemento con cui il primo non ha nessuna versione in comune.
- **Apri** (o un doppio clic) lavora con un solo punto: il percorso sopra l'albero ti riporta indietro.
- La scheda **Testo** mostra i dati del punto aperto come testo: i dati dell'agente puoi **modificarli**, gli altri solo leggerli (il libro, l'inizio dell'esecuzione, il risultato), e **Mostra la differenza rispetto a** evidenzia ciò che differisce parola per parola. Ciò che scrivi viene controllato come JSON quando torni all'albero, cambi ciò che è mostrato, apri un altro punto o accetti il risultato; se non è JSON valido resti dove sei e ti viene detto cosa non va, senza che nulla venga modificato. Un testo valido sostituisce i dati dell'agente in quel punto, e tutto ciò che sta al suo interno viene confrontato di nuovo.
- **Ricomincia** dimentica ciò che hai deciso e scritto; **Accetta il risultato** passa il risultato alla revisione, dove è il testo da applicare (la riga resta come l'hai lasciata finché la revisione è aperta).

Anche la revisione ha **Usa quello dell'agente** e **Usa quello del libro** per un campo di questo tipo, per prendere una delle due così com'è, e **Ricomincia** per tornare ai dati uniti.

![I dati della scaletta dell'arco nella revisione, con Unisci i dati JSON](../images/agent-review-json.png)

Un lorebook che altri libri usano è contrassegnato come **Condiviso**: la modifica va al lorebook stesso, quindi anche gli altri libri la ricevono. Parte deselezionato.

L'applicazione avviene **sul posto** nel libro: le parti mantengono i loro indirizzi, gli altri libri mantengono i collegamenti a un lorebook, e ciò che l'agente ha eliminato va nel cestino. Prima viene fatta una **copia di backup del libro com'era** (si trova tra i backup del libro, con i suoi lorebook), e le modifiche vengono fatte tutte insieme oppure non vengono fatte affatto. **Mostra il ramo su cui l'agente ha terminato** cambia anche il libro sul ramo su cui si trovava l'agente, quando è tra ciò che hai applicato. I riassunti delle parti modificate risultano superati, come dopo una modifica a mano; i riassunti scritti dall'agente vengono ripresi così come sono.

Quando le modifiche vengono applicate l'esecuzione è *Applicata* e la sua copia e i suoi punti di controllo vengono eliminati. **Scarta il risultato** getta via la copia e lascia il libro com'è. Finché non fai una delle due cose, l'agente ha un'esecuzione non conclusa e non può avviarne un'altra.

<a id="the-copy-of-the-book"></a>

### La copia del libro

La scheda **Copia del libro** mostra il libro su cui lavora l'agente, in sola lettura: la **storia**, un ramo alla volta (scegli il ramo sopra; ogni parte con il suo numero, lo scambio in cui si trova rispetto alle altre dopo la stessa parte, le sue parole, l'ambientazione della scena, il punto di vista e, ripiegati, le sue istruzioni e il riassunto) e i **lorebook** con le loro voci (sceglinene uno per vederne il contenuto, la nota, i tag e le impostazioni). Non è una finestra del tuo libro: nulla al suo interno può essere modificato, non viene fatto alcun backup della copia e nessuna estensione vede un libro aperto. Viene letta quando apri la scheda e quando premi **Aggiorna**, anche mentre l'agente lavora, quindi mostra ciò che l'agente ha scritto finora. È disponibile finché l'esecuzione ha la sua copia (non dopo che il risultato è stato applicato o scartato).

![La scheda Copia del libro](../images/agent-book-copy.png)

<a id="diagnostics"></a>

### Diagnostica

La scheda **Diagnostica** conta ciò che l'esecuzione ha fatto a partire dal suo transcript, per capire di cosa un agente ha bisogno dai suoi strumenti e dal suo prompt. Funziona per qualsiasi esecuzione, anche una vecchia (scegline una in **Esecuzioni** prima), e viene creata quando apri la scheda e quando premi **Aggiorna**.

![La scheda Diagnostica](../images/agent-diagnostics.png)

- **Panoramica**: turni e chiamate, quante chiamate sono fallite, il tempo del modello, delle chiamate e delle pause (un intervallo di 30 minuti o più tra due passaggi è una pausa, non lavoro), il contesto al suo picco e alla fine, i token, le compattazioni, i tuoi messaggi e gli avvisi che l'esecutore ha dato all'agente (una chiamata illeggibile, troppe chiamate, un contesto pieno).
- **Strumenti**: per ogni strumento le chiamate, gli errori, i risultati che sono stati tagliati, quante volte era l'unica chiamata di un turno, i caratteri che l'agente ha scritto, quante chiamate hanno passato un testo come `@file:` o `@result`, quante hanno scritto per esteso un testo lungo (300 caratteri o più) e quante di quelle l'avevano copiato da un risultato che l'agente aveva appena ricevuto, i token dei suoi risultati (circa, dai caratteri), la quota su tutti i risultati, il risultato più lungo e il tempo.
- **Cosa hanno riportato le richieste**: registrato mentre l'esecuzione lavora, quindi solo per le esecuzioni fatte dopo che questa funzione è stata aggiunta (una vecchia esecuzione dice di non averne): quanto le richieste hanno atteso la prima parola (il server che legge il prompt, quindi un tempo breve significa che la sua cache era calda), quanto tempo hanno impiegato a scrivere le risposte e quanti token al secondo, quanta parte del prompt il server non ha riletto (la cache; llama.cpp e OpenAI la riportano), quanta parte delle risposte era ragionamento, perché le risposte si sono concluse (`length` significa che il limite di una risposta l'ha troncata), le chiamate che il modello ha scritto senza essere effettuate (ripetizioni, i limiti, le politiche di gruppo), il tempo che le richieste hanno atteso un posto, i riassunti scritti e quanto hanno richiesto, e lo stesso per ogni blocco di 25 turni.
- **Cosa ha scritto il modello**: i caratteri degli argomenti, del testo accanto alle chiamate e del ragionamento, in totale e per ogni blocco di 25 turni con il tempo del modello, per vedere dove va a finire il suo output e se a un certo punto ha smesso di usare `@file`. **Argomenti più lunghi** mostra l'inizio delle chiamate più grandi, con il percorso di uno strumento per i file.
- **Turni persi in uno schema**: lo stesso strumento chiamato turno dopo turno (tre o più volte), segnalato quando una ricerca è venuta subito prima; e le chiamate ripetute con gli stessi argomenti. Questi mostrano dove uno strumento che accetta molti elementi, o più chiamate in una sola risposta, farebbe risparmiare turni.
- **Errori** con gli argomenti della prima chiamata che ha ottenuto ciascun messaggio (per vedere cosa ha inviato il modello quando una chiamata è fallita), i **risultati più grandi**, i **turni più lenti**, le **compattazioni** e i **sotto-agenti** con gli strumenti che ciascuno ha chiamato.

**Scarica** fornisce il rapporto in Markdown, **Scarica JSON** in JSON, **Copia** mette il Markdown negli appunti. I tempi sono gli intervalli tra le righe memorizzate, quindi sono approssimativi: il tempo di una risposta include l'attesa di un posto libero presso il modello.
<a id="the-context"></a>

### Il contesto

La scheda **Contesto** mostra ciò che il modello legge con la **prossima richiesta**, scomposto nelle sue parti, così puoi vedere dove vanno i token e fare il debug di un agente che si comporta in modo strano. Viene generata quando apri la scheda e quando premi **Aggiorna** (non mentre l'esecuzione è in corso: conta i token di ogni parte).

- Tre grafici: i **tipi di parti** rispetto al limite del contesto (il prompt dell'agente, le tue istruzioni, le note del runner, le definizioni degli strumenti, il riassunto di una compattazione, i tuoi messaggi, le risposte dell'agente, i risultati delle sue chiamate e ciò che è libero), **ogni parte nell'ordine in cui viene inviata** (una barra per ciascuna, nel colore del suo tipo: uno strumento con una descrizione enorme o un risultato che riempie il contesto risalta), e il **contesto a ogni risposta del modello**: una barra impilata di ciò di cui era composto (gli stessi tipi, stimati), la dimensione che il modello **ha riportato** come linea e il limite come linea tratteggiata. Dove la linea sta sopra le barre, il modello ha contato più dell'stima (immagini, formato del provider); una barra che scende è una compattazione o un riavvolgimento.
- Una **tabella delle parti** con tipo, nome, token e quota del contesto. Seleziona una riga per leggere **esattamente il testo che viene inviato al modello** per quella parte: una sezione del messaggio di sistema, la descrizione e i parametri di uno strumento, un messaggio, gli argomenti di una chiamata o il risultato di una.
- **Scarica** consegna l'intero contesto come un unico file Markdown (il messaggio di sistema con le sue sezioni, ogni strumento con descrizione e schema, la conversazione messaggio per messaggio, ogni testo in un blocco di codice), **Copia** mette lo stesso testo negli appunti.

![La scheda Contesto](../images/agent-context.png)

Le dimensioni delle parti sono stime; il numero riportato dal modello è quello reale (la richiesta e la sua risposta). Nel grafico delle risposte il prompt, le istruzioni e gli strumenti sono quelli attuali (una risposta più vecchia potrebbe averne avuti di diversi se li hai cambiati), la conversazione è com'era allora: un riassunto ha sostituito ciò che lo precedeva, e ciò che un riavvolgimento ha annullato è sparito dalle risposte successive (una compattazione a sua volta riavvolta non viene ripristinata, le barre successive risultano un po' troppo piccole). Il messaggio di sistema e gli strumenti sono quelli che userebbe la prossima richiesta, quindi una modifica del prompt, delle istruzioni o dei comandi consentiti appare subito. I messaggi sono la trascrizione così come memorizzata, senza ciò che una ripresa aggiunge (un "continua", le risposte alle chiamate lasciate aperte da un'interruzione). Il ragionamento non fa parte del contesto (al modello non viene reinviato) e le immagini compaiono con i nomi dei file. Ciò che una compattazione ha sostituito non è nel contesto; la scheda **Esecuzione** e lo scaricamento **Esecuzioni** lo conservano ancora.

<a id="checkpoints"></a>

## Checkpoint

Un **checkpoint** è un punto di ripristino della copia di lavoro dell'esecuzione (le parti, i riassunti, i lorebook e i dati delle estensioni, con i riferimenti che all'agente sono stati dati) e dello spazio di lavoro dell'agente, così che un lavoro andato storto possa essere annullato: l'agente torna indietro con `checkpoint_restore`, oppure lo fai tu nella scheda **Checkpoint** della console. Chi li crea:

- **Inizio dell'esecuzione**, creato all'avvio dell'esecuzione. Non viene mai eliminato.
- **Automatico**, creato dall'esecuzione prima di un turno in cui l'agente chiama qualcosa che può modificare il libro o lo spazio di lavoro (un comando che non sia una semplice lettura, oppure uno strumento di file che scrive), e solo se è cambiato qualcosa dall'ultimo checkpoint. Ne vengono conservati gli ultimi cinque; il più vecchio viene eliminato quando ne viene creato uno nuovo.
- **Creato apposta**, dall'agente con `checkpoint` (gli viene chiesto di crearne uno prima di un passo rischioso) oppure da te con **Salva un checkpoint**. Restano finché non vengono eliminati.

![La scheda Checkpoint](../images/agent-checkpoints.png)

Tutti i checkpoint di un'esecuzione condividono una **quota** che l'amministratore imposta nei [permessi](administration/permissions.md#settings-of-a-permission) (100 MB se vuota). Una parte di essa è riservata a quelli automatici, così che l'agente non possa esaurirla tutta; un checkpoint creato apposta che non ci sta viene **rifiutato**, senza eliminare nulla alle spalle di chi l'ha chiesto. L'agente riceve allora l'elenco e elimina ciò che non gli serve (non il checkpoint di inizio, né il più recente); tu puoi eliminarne qualsiasi nella scheda. Ciascuno è un archivio (uno ZIP del libro in JSON, delle coppie e dei file dello spazio di lavoro) che puoi **scaricare**.

**Ripristina** ricostruisce la copia di lavoro dal checkpoint: è una nuova copia, la vecchia viene eliminata, lo spazio di lavoro è quello salvato (i file scritti dopo sono spariti) e i riferimenti dell'agente per gli elementi che c'erano ancora funzionano. Lo stato precedente viene prima salvato come checkpoint automatico, se c'è spazio, così un ripristino può essere annullato. Un ripristino richiede circa quanto l'avvio di un'esecuzione (mezzo minuto per un libro di duemila parti). Puoi ripristinare mentre l'agente non lavora; l'agente si ripristina da solo tra due turni e viene informato di ciò che è tornato. I checkpoint spariscono quando l'esecuzione rilascia la propria copia.

**Riavvolgi** è un ripristino che riporta indietro anche la *conversazione*. Un ripristino da solo lascia l'agente con il ricordo di lavoro che non è più nel libro, quindi un'esecuzione che vuoi far proseguire in un'altra direzione richiede un riavvolgimento: la copia di lavoro e lo spazio di lavoro tornano al checkpoint, i messaggi successivi al punto in cui il checkpoint è stato creato vengono resi grigi nella trascrizione (puoi ancora leggerli, e l'esportazione li contiene) e l'agente non li legge più, e un avviso comunica all'agente cosa è successo, così rifa il lavoro se serve ancora. L'esecuzione deve essere in pausa o fermata (prima metti in pausa un agente che attende te); poi la **riprendi** e prosegue dall'avviso. Si può riavvolgere solo fino a un checkpoint il cui punto nella conversazione fa ancora parte di essa: dopo un riavvolgimento a un checkpoint precedente, quelli successivi (della conversazione annullata) possono solo essere ripristinati. Un checkpoint creato prima che esistesse il riavvolgimento non conosce il proprio punto e può solo essere ripristinato. Ciò che una compattazione ha fatto dopo il checkpoint viene annullato con il riavvolgimento; l'agente rilegge i messaggi che la compattazione aveva sostituito.

**Tornare a un messaggio.** Non devi scegliere un checkpoint: ogni messaggio che hai scritto all'agente ha un pulsante **Riavvolgi** sotto di sé nella trascrizione della scheda **Esecuzione**. Riporta la conversazione a prima di quel messaggio: l'agente dimentica il messaggio e tutto ciò che è seguito, e la copia di lavoro e lo spazio di lavoro tornano al checkpoint più recente creato prima di esso (il runner ne crea uno prima di ogni turno che modifica qualcosa, quindi di solito coincide con il messaggio). Lo stato di un'esecuzione è noto solo ai suoi checkpoint, quindi quando il più vicino è più vecchio del messaggio, vengono dimenticati anche i messaggi compresi tra quel checkpoint e il tuo; la conferma indica quale checkpoint è e quanti messaggi vanno. I messaggi dimenticati restano nella trascrizione, in grigio. Il pulsante funziona mentre l'agente aspetta te (è in pausa per questo), è in pausa, oppure è stato fermato e mantenuto; mentre l'agente lavora è disattivato. Poi **riprendi** l'esecuzione, e prosegue dall'avviso.
<a id="creating-and-editing-an-agent"></a>

## Creazione e modifica di un agente

**Nuovo agente** apre la stessa finestra di **Modifica**:

![La finestra di un nuovo agente](../images/agent-dialog.png)

| Campo | |
|---|---|
| **Nome** | Obbligatorio. |
| **Attivata** | Un agente disattivato non può essere avviato; le sue esecuzioni esistenti non vengono toccate. |
| **Libro** | Il libro su cui lavora l'agente, obbligatorio. Vengono proposti solo i libri che non hanno ancora un agente, e il libro di un agente non può essere cambiato in seguito. |
| **Modello**, **Protocolli** | Il modello e il protocollo con cui l'agente comunica. Entrambi, oppure nessuno; un agente senza di essi non può essere avviato. |
| **Scrivi il libro con il modello dell'agente** | Attivo (predefinito): i comandi che hanno un modello scrivono qualcosa - il turno successivo, gli swipe, i riassunti e i comandi delle estensioni come il Lore Generator e l'Arc Outline - usano il modello indicato sopra, qualunque sia il modello del tuo libro. Disattivo: usano il modello del tuo libro (un comando di estensione ricade sul modello predefinito delle [Impostazioni](account.md) quando il libro non ne ha uno). È così che puoi combinare modelli diversi: uno forte che ragiona e uno più economico o più fluido che scrive. Un comando può comunque indicare un altro profilo di modello dell'agente. |
| **Chiamate in una risposta** | Quante delle chiamate che l'agente scrive in una risposta vengono effettuate. *Tutte insieme, ignora le regole dei batch*: ogni chiamata (la più veloce). *Insieme, i comandi contrassegnati Da solo vengono eseguiti da soli* (predefinito): le chiamate vengono effettuate insieme, ma un comando la cui colonna **Batch** indica *Da solo*, come quello che effettua una chiamata a un modello, viene eseguito in una risposta a sé stante, e l'agente ne viene informato. *Una chiamata per risposta*: viene effettuata solo la prima chiamata e l'agente scrive la successiva dopo aver letto il risultato; più lento (ogni chiamata è un turno a sé, con il ragionamento del modello) ma può reagire a ogni risultato. Un **errore termina sempre la risposta**, in ogni modalità: le chiamate successive a quella fallita non vengono effettuate (ricevono la risposta "non effettuata perché una chiamata precedente è fallita") e l'agente legge prima l'errore. |
| **Il modello vede le immagini** | Spuntalo quando il modello accetta immagini (l'API del modello non lo dice, quindi lo indichi tu). L'agente dispone allora di `view_image` e puoi allegare immagini ai tuoi messaggi; senza questa opzione non riceve mai un'immagine. |
| **Lingua** | La lingua di tutto ciò che l'agente legge: le descrizioni dei suoi comandi, i loro risultati e i loro errori, e i testi predefiniti dell'esecuzione (il prompt di sistema predefinito, gli avvisi dell'esecuzione, il prompt di compattazione) quando esistono in quella lingua (inglese, giapponese, cinese); la [lingua dei prompt](account.md#prompt-language) delle tue impostazioni non si applica agli agenti. L'inglese è l'impostazione predefinita e la scelta più sicura, perché i comandi e i loro parametri sono in inglese qualunque sia la lingua (le descrizioni in un'altra lingua mescolano entrambe), e i modelli, soprattutto quelli piccoli, usano gli strumenti in modo più affidabile in questo modo. L'elenco contiene le lingue installate nell'applicazione (un'estensione può aggiungerne una, come la localizzazione cinese). Non ha **nulla a che vedere con la lingua del libro**: all'agente viene chiesto di scrivere il testo del libro nella lingua del libro, che può leggere con `get-book-info`. Un'esecuzione mantiene la lingua con cui è stata avviata; una modifica si applica alla prossima esecuzione. |
| **Comandi distruttivi** | Cosa succede quando l'agente richiede un comando che modifica o rimuove dati: *Eseguili*, *Chiedi prima* (una [finestra](#asking-you) ti chiede di approvarlo) o *Rifiutali*. |
| **Numero massimo di turni**, **Budget di token di un'esecuzione**, **Limite del contesto** | Limiti facoltativi di una singola esecuzione. Il limite assegnato dal tuo amministratore si applica in ogni caso. |
| **Numero massimo di sotto-agenti** | Quanti [sotto-agenti](#sub-agents) l'agente può far lavorare contemporaneamente, da 0 a 10. Vuoto o zero significa nessuno. |
| **Modelli per i sotto-agenti** | I [modelli](#models-for-sub-agents) su cui può girare un sotto-agente, e se può girare anche su quello principale. |
| **Compatta a (% del contesto)** | Quando il contesto del modello supera questa quota del suo limite e l'agente non ha [compattato il proprio lavoro](#running-a-command-yourself) da sé, l'esecuzione lo fa: l'agente scrive il riassunto del suo lavoro precedente, che lo sostituisce. Vuoto significa 90, zero non compatta mai (l'esecuzione si ferma allora quando il contesto è pieno), altrimenti da 50 a 99. |
| **Prompt di sistema** | Ciò che l'agente deve essere. Lascialo vuoto e l'agente segue il predefinito - il tuo delle [Impostazioni](account.md#templates), altrimenti quello del tuo amministratore, altrimenti quello integrato - che viene mostrato in grigio. L'icona informativa ha **Inserisci il template predefinito**, che inserisce il predefinito così puoi modificare una copia; svuota di nuovo il campo per seguire il predefinito. |
| **Prompt di compattazione** | Ciò che viene chiesto all'agente quando lo [`/compact`](#running-a-command-yourself): scrivere il riassunto che sostituisce il suo lavoro precedente. Vuoto segue il predefinito, come il prompt di sistema, con la stessa icona informativa. |
| **Istruzioni (agents.md)** | Le tue istruzioni per l'agente, aggiunte al suo prompt in ogni esecuzione. L'agente può leggerle, non cambiarle mai. Vuoto segue il tuo predefinito delle [Impostazioni](account.md#templates), se ne hai uno (mostrato in grigio), altrimenti non ce ne sono. L'icona informativa ha **Inserisci il template predefinito**, che inserisce il tuo predefinito così puoi modificare una copia. Sotto di essa una riga indica quanto pesano, in token, il prompt, le istruzioni e i comandi spuntati in **ogni richiesta** (una stima, con in aggiunta gli strumenti per i file e per l'esecuzione e le note del runner). |
| **Comandi consentiti** | Vedi sotto. |

![I prompt dell'agente e i suggerimenti del template predefinito](../images/agent-dialog-prompts.png)

<a id="allowed-commands"></a>

### Comandi consentiti

All'agente vengono offerti solo i comandi che spunti. La tabella elenca ogni comando con il suo effetto (*Distruttivo* modifica o rimuove dati, *Non distruttivo* legge soltanto o aggiunge). La casella **Selezione** spunta molti comandi in una volta: **Tutte**, **Nessuno** o **Tutti quelli non distruttivi** (il predefinito per un nuovo agente), oppure un ruolo; spuntare un comando a mano rende la selezione *Personalizzato*. Il campo **Cerca comandi** sopra la tabella mostra solo i comandi che contengono tutte le parole che digiti (in qualunque ordine, nel nome o nella descrizione); la selezione si applica comunque a tutti i comandi, anche a quelli nascosti. I comandi portati da un'estensione restano consentiti anche quando l'estensione non è caricata: semplicemente non li vedi. La colonna **Batch** indica se il comando viene usato *Insieme* ad altre chiamate in una risposta oppure *Da solo* (un comando che effettua una chiamata a un modello, come `next`, `swipe`, `regenerate`, `redraft-part`, `generate-annotation` o la compattazione, è da solo; vedi **Chiamate in una risposta** sopra). Tutte le colonne possono essere ridimensionate. La colonna **Token** indica quanto la definizione del comando (il suo nome, la descrizione e i parametri) aggiunge a ogni richiesta dell'agente, e la riga sotto le istruzioni somma quelli spuntati: l'insieme completo vale circa 20 000 token, quindi una selezione più ridotta (un ruolo) lascia più spazio nel contesto per il lavoro.

![I comandi consentiti, con i ruoli della casella Selezione](../images/agent-commands.png)

I ruoli sono insiemi di comandi per un compito. Tutti possono leggere il libro e i lorebook (ogni comando che si limita a leggere, anche quelli delle estensioni), e nessuno può rimuovere dati:

| Ruolo | Oltre alla lettura |
|---|---|
| **Revisore** | Niente: controlli di continuità, verifiche di nomi e termini, report. |
| **Custode del lore** | Crea e aggiorna le voci dei lorebook e i lorebook, i loro tag e i sotto-lorebook. |
| **Editor** | Modifica e aggiunge testo alle parti, imposta i loro campi, mantiene i riassunti (e fa anche riscriverli dal modello) e le informazioni e i tag del libro. |
| **Redattore** | Aggiunge parti e diramazioni, modifica e aggiunge testo, lascia che il modello scriva il turno successivo (`/next`, `/swipe`), mantiene i riassunti, crea e aggiorna le voci dei lorebook di ciò che inventa. |

Ogni comando spuntato aggiunge la sua definizione a ogni richiesta, quindi un ruolo è anche il modo più economico per ottenere un prompt piccolo per un modello debole.
<a id="on-a-phone"></a>

## Su un telefono

Il layout per telefono ha una scheda **Agenti** nel cassetto, per gli utenti che possono usare gli agenti, con accanto al nome il numero di agenti che hanno bisogno di te. Ogni agente è una scheda con il nome, lo stato e i pulsanti della tabella (vanno a capo su più righe). Le finestre - la console, la revisione, le impostazioni dell'agente - occupano tutto lo schermo, e la revisione mostra la tabella sopra il dettaglio dell'elemento. Tutto funziona come sul desktop, solo più piccolo. La console è disposta per essere usata con il pollice:

![La scheda Agenti su un telefono](../images/phone-agents.png)

![La console su un telefono](../images/phone-agent-console.png)

![La scheda Agenti su un telefono](../images/phone-agents.png)

![La console su un telefono](../images/phone-agent-console.png)

- **Le schede sono icone** (esecuzione, esecuzioni, spazio di lavoro, checkpoint, contesto, copia del libro, sotto-agenti, diagnostica); il titolo della finestra indica quella aperta.
- **Pausa, Pausa ora, Riprendi, Termina e Interrompi sono pulsanti a icona in basso, e vengono mostrati solo quelli utilizzabili in questo momento**; **Rivedi le modifiche** mantiene il suo testo, è ciò che attende un'esecuzione terminata. **Aggiorna** e **Chiudi** sono icone nell'intestazione.
- **La casella dei messaggi occupa una sola riga**, con accanto le icone **Invia** e **Allega immagine**. La **riga di comando** (l'icona del terminale) è nascosta finché non la apri, così la tastiera non copre la trascrizione inutilmente. I messaggi della trascrizione usano tutta la larghezza.
- **Le tabelle sono elenchi di schede**: un'esecuzione, un checkpoint, un comando delle impostazioni dell'agente, un elemento della revisione mostrano nome e stato con i dettagli sotto e i pulsanti in basso, al posto delle colonne. I file dello spazio di lavoro mostrano la dimensione sotto il nome, e la barra degli strumenti dello spazio di lavoro è a icone. La tabella del contesto mantiene numero, nome e token; il tipo e la quota di una parte sono sotto il testo della parte selezionata.

- **La barra superiore indica chi attende te.** Quando il cassetto è chiuso, un numero rosso nella barra superiore (accanto al titolo della parte aperta) conta gli agenti che hanno bisogno di te, come fa il badge della scheda **Agenti** nel cassetto; toccalo per andare alla scheda **Agenti**. È nascosto quando nessuno attende, mentre il cassetto è aperto e nella scheda Agenti stessa.
- **Scorri tra le parti.** Un movimento orizzontale sul contenuto passa alla parte successiva del cassetto (scorrendo verso sinistra) o alla precedente (verso destra). Non fa nulla con il cassetto aperto o mentre l'applicazione blocca la navigazione, e un tocco che inizia in un campo di testo, in una tabella o in qualsiasi elemento che scorre orizzontalmente viene lasciato a quell'elemento.

Per il lavoro prolungato con la trascrizione o la revisione, uno schermo più grande resta più comodo.

<a id="cloning"></a>

## Clonazione

**Clona** chiede un nome e un libro che non ha alcun agente, e copia la definizione dell'agente e i suoi file (non le sue esecuzioni) in un nuovo agente di quel libro. Puoi avere quanti agenti vuoi, cloni compresi, purché il tuo amministratore lo consenta (cinque, salvo che impostino un altro numero); al limite **Nuovo agente** e **Clona** sono disattivati e ne indicano il motivo.

![Clonazione di un agente](../images/agent-clone.png)

<a id="lore-notes-of-an-imported-book"></a>

## Note sul lore di un libro importato

Dopo aver [importato](books/story-editor.md#after-an-import) un manoscritto, l'applicazione propone un agente che ricava il lore del libro:
**Crea note sul lore con un agente...** (la stessa voce è nel menu delle impostazioni ⚙ di una storia, **Analisi**). Prima chiede conferma:

![La domanda prima che l'agente parta](../images/lore-notes-confirm.png)

Poi crea un agente chiamato **Note sul lore** per il libro, con il modello e il protocollo del libro, e lo avvia (si apre la sua console).

- Legge la storia dalla prima parte all'ultima, alcune parti alla volta, e conserva le sue note nei file del proprio spazio di lavoro (personaggi, luoghi, oggetti, termini) in modo da poter proseguire dopo una pausa. I riepiloghi lo aiutano a orientarsi, le parti sono la sua fonte.
- Scrive poi una voce per ogni personaggio, luogo, oggetto, gruppo o termine nel lorebook del libro (se il libro non ne ha uno, ne viene creato uno), usando come filtro i nomi che la storia impiega, nella lingua del libro. Aggiunge fatti a una voce esistente e non scrive due volte la stessa cosa.
- Può leggere la storia e i lorebook e scrivere voci; non può modificare la storia e non può eliminare nulla.

È un agente come tutti gli altri: il libro è [bloccato](#the-book-is-locked) mentre lavora, lavora su una [copia](#the-working-copy), e ciò che ha scritto diventa tuo solo dopo che lo [hai rivisto](#reviewing-the-result) e lo hai accettato. Leggi le voci prima di farlo: conosce solo ciò che dice il testo, e un libro lungo costa molte richieste. Le istruzioni sono quelle dell'agente (**Modifica** le mostra, puoi cambiarle prima di avviarlo di nuovo).
Viene rifiutato quando non puoi usare gli agenti, quando il libro non ha un modello e quando il libro ha già un agente (un libro ne ha uno solo).

<a id="the-book-is-locked"></a>

## Il libro è bloccato

Mentre un'esecuzione di un agente lavora o attende te, **il suo libro è bloccato**: puoi leggerlo, ma nulla lo modifica, così l'agente non lavora mai su un testo che si sposta sotto di lui. L'elenco dei libri non apre un libro bloccato, ti dice quale agente lo tiene, e generazione, modifica, cerca e sostituisci, riepiloghi ed eliminazione del libro sono rifiutati. Metti in **Pausa** o **Interrompi** l'esecuzione per modificare di nuovo il libro; un'esecuzione in pausa, interrotta o terminata non lo blocca.

![Un libro su cui lavora un agente nell'elenco dei libri](../images/agent-locked-book.png)

I **lorebook del libro** sono bloccati insieme a lui: il lorebook che il libro usa e i suoi sotto-lorebook. Nella scheda Lorebook un lorebook così appare come lo vedi su un telefono, come testo semplice e senza alcuna modifica, con una nota che indica quale agente lo tiene; **Aggiorna** lo mostra di nuovo per la modifica dopo che hai messo in pausa o interrotto l'esecuzione. Gli altri lorebook non sono interessati.

<a id="agents-and-folders"></a>

### Agenti e cartelle

Un libro in una [cartella](books/folders.md) su cui lavora un agente mantiene la sua posizione e le sue impostazioni: spostare o eliminare una cartella superiore, e modificare un'impostazione che la cartella trasmette (lingua, modelli, punto di vista, tempo, stile, prompt), è rifiutato con il nome del libro e dell'agente. Rinominare una cartella, o cambiarne la descrizione, non è rifiutato. Un'esecuzione in pausa, interrotta o terminata lo lascia andare.

![Il lorebook di un libro su cui lavora un agente](../images/agent-locked-lorebook.png)
