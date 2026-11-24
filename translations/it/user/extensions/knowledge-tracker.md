---
label: Tracciatore delle conoscenze
order: 11
---

# Knowledge Tracker

Tiene traccia di ciò che i personaggi della tua storia sanno e di ciò che **non** sanno, e lo inserisce nel prompt. Un modello tende a far sapere a un personaggio cose su persone che non ha mai incontrato (il loro nome, il loro lavoro, il loro volto), a meno che le istruzioni non lo impediscano. Con il Knowledge Tracker, il prompt dice per ogni personaggio tracciato nella scena che cosa quel personaggio sa e che non sa nient'altro.

!!!
Sei tu a scegliere chi viene tracciato. Al modello vengono descritti solo i personaggi che aggiungi alla scheda, e solo mentre sono tra i *personaggi presenti* della parte che si sta scrivendo. Nulla viene dedotto dal testo della storia.
!!!

<a id="the-knowledge-tab"></a>

## La scheda Knowledge

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Knowledge** accanto a *Outline*. Ogni modifica viene salvata nel libro man mano che la fai.

![La scheda Knowledge](../../images/plugin-knowledgetracker.png)

**Track a character** chiede un **nome** e, facoltativamente, **alias**: gli altri nomi con cui il personaggio compare nel campo *Present Characters* di una parte, separati da virgole. Un nome o un alias può appartenere a un solo personaggio tracciato.

**Import from the Character Tracker** (l'icona della persona accanto a **Track a character**) avvia il tracciamento dei personaggi del [Tracciatore dei personaggi](character-tracker.md) dello stesso libro. Elenca i personaggi che il libro non traccia ancora (come nome o alias) con la rispettiva descrizione breve, tutti selezionati; deseleziona quelli da escludere e premi **Import**. Vengono presi solo i nomi: la descrizione breve e la nota descrivono un personaggio, non sono qualcosa che lui sa. Con **Their relationships count as meetings** un personaggio importato ha incontrato le persone con cui è in relazione nel Tracciatore dei personaggi (vengono scritte nel suo elenco **Has met** come se le avessi scritte tu, così l'elaborazione le conserva).

**Who has met whom** (l'icona di stretta di mano, accanto all'importazione) imposta in una sola volta gli incontri tra i personaggi tracciati, senza scrivere nomi. Scegli un personaggio a sinistra; a destra ogni altro personaggio tracciato ha una casella di spunta, selezionata quando il personaggio scelto l'ha incontrato. Spunta per aggiungere l'incontro, deseleziona per rimuoverlo; ogni modifica viene salvata subito e le schede lo mostrano quando chiudi la finestra. Un incontro va in una sola direzione: spuntare Thomas per Fiora non spunta Fiora per Thomas, a meno che **Mutual** non sia selezionato. In quel caso ogni spunta o deselezione viene fatta su entrambi i lati insieme, il che è più rapido quando i due si conoscono già (vale per le modifiche fatte mentre è selezionato, non per gli incontri già impostati). Un incontro spuntato conta come tuo (l'elaborazione lo conserva); uno che deselezioni e che la storia mostra torna se quella parte viene riletta. Servono almeno due personaggi tracciati.

Apri un personaggio con la freccia sulla sua scheda per vedere che cosa sa:

| | |
|---|---|
| **Knows** (verde) / **Does not know** (rosso) | Una frase breve su qualsiasi cosa: una persona, un luogo, un segreto, un evento (*Peter sa del complotto contro il duca*, *Fiora non sa che Thomas è suo padre*). Scrivi esplicitamente ciò che un personaggio **non** sa: è ciò che il modello rispetta. |
| L'ordine e **#12** | La conoscenza più recente sta in cima (anche quella vecchia). Un elemento appreso dal modello termina con il numero piccolo della parte in cui è stato appreso (**#12**): un clic porta la storia a quella parte e la fa lampeggiare (su telefono prima chiude il cassetto). Gli elementi che hai scritto tu non hanno numero, e nemmeno quelli la cui parte non è nel ramo attivo (un altro ramo, oppure la parte è stata eliminata). |
| **Add knowledge** | Aggiunge un elemento, subito attivo. Tienilo a **un solo pensiero**: un elemento che dice due cose (*non sa chi è Thomas, non l'ha mai incontrato*) viene ritirato non appena una delle due smette di essere vera. Il pulsante **edit** cambia il testo e il tipo. |
| **Retract** (il cerchio barrato) | L'elemento non è più vero (*non ha incontrato Thomas*, dopo che si sono incontrati). Non viene eliminato: diventa *conoscenza vecchia* e non viene più inviato al modello. |
| **Lock** (il lucchetto, sugli elementi appresi dal modello) | Il [passaggio di riordino](#tidying-the-knowledge) non vede mai un elemento bloccato: usalo per ciò che deve restare, un tradimento, un segreto. La storia può comunque contraddire un elemento bloccato (un personaggio che perde la memoria): la lettura della storia lo ritira come di consueto. |
| **Delete** | Rimuove definitivamente un elemento, dopo una conferma. Retract lo conserva, quindi usalo quando hai dei dubbi. |
| **Select items** (accanto a **Add knowledge**) | Elimina rapidamente molti elementi: a ogni elemento del personaggio, anche quelli vecchi, viene assegnata una casella di spunta. **Select all** / **Deselect all** spuntano o deselezionano tutti, **Delete selected (n)** rimuove definitivamente quelli spuntati dopo una conferma, **Cancel** abbandona la selezione. |
| **Has met** | Le persone che il personaggio ha incontrato, annotate dall'[elaborazione](#processing). L'elaborazione può annotare un nome che non è una persona (una nota nel campo dei personaggi presenti): apri **Edit** e modifica l'elenco **Has met**, un nome dopo l'altro separati da virgole. I nomi che scrivi restano quando la storia viene riletta; uno che rimuovi torna solo se viene riletta la parte in cui era stato visto. |
| **Old knowledge (n)** | Chiusa per impostazione predefinita. Ogni elemento vecchio indica perché è vecchio: *retracted* (con il numero della parte la cui lettura lo ha ritirato, quando lo ha fatto il modello), *declined* in una revisione, oppure *tidied*. Può essere **copied**, **edited**, **restored** (torna attivo, da ora in poi) o eliminato. |

<a id="mass-changes"></a>

### Modifiche di massa

Il pulsante **Mass changes** (l'icona a elenco accanto a **Track a character**) apre una finestra con ogni personaggio tracciato e tutti i suoi elementi, anche quelli vecchi, in una griglia con caselle di spunta: il tipo, il testo, lo stato (*Active*, oppure vecchio: *retracted*, *declined in a review*, *tidied*), la provenienza (*Initial state*, *Written by hand*, *Learned from the story*) e la parte in cui è stato appreso. Puoi spuntare elementi in quante griglie vuoi (la casella nell'intestazione di una griglia spunta tutti i suoi elementi; **Select all** / **Deselect all** nella barra in basso riguardano tutti gli elementi di tutti i personaggi), poi:

| | |
|---|---|
| **Retract selected (n)** | Gli elementi attivi spuntati diventano conoscenza vecchia, come fa **Retract** per uno solo. |
| **Restore selected (n)** | Gli elementi vecchi spuntati tornano attivi, da ora in poi. |
| **Delete selected (n)** | Rimuove definitivamente gli elementi spuntati, dopo una conferma. |
| **Copy selected (n)** | Copia gli elementi spuntati, di tutti i personaggi, per incollarli in un altro personaggio. |
| **Paste (n)** (nell'intestazione di ogni personaggio) | Aggiunge gli elementi copiati a quel personaggio. Un elemento incollato conta come scritto a mano (uno iniziale resta iniziale) e mantiene il suo tipo, il suo blocco e se è vecchio; uno che il personaggio ha già (stesso tipo e stesso testo) viene saltato. Gli elementi copiati restano fino alla copia successiva, anche dopo la chiusura della finestra, così possono essere incollati in più personaggi. |

I numeri sui pulsanti indicano quanti degli elementi spuntati ciascuno modifica. Le spunte restano dopo una modifica, così gli elementi ritirati possono essere eliminati subito dopo.

**Export to JSON** (in alto nella finestra) scarica come file i personaggi tracciati del libro (nomi, alias, elementi con tipo, stato, origine e blocco, persone incontrate); non contiene nulla dell'elaborazione, quindi va bene per qualsiasi libro. **Import from JSON** legge un file del genere nel libro e aggiunge soltanto: un personaggio il cui nome il libro traccia già (come nome o alias) riceve gli alias, gli elementi e le persone incontrate che non ha ancora (un elemento con lo stesso tipo e lo stesso testo viene saltato), gli altri diventano nuovi personaggi tracciati; un alias che un altro personaggio ha già viene escluso. Un elemento importato non ha alle spalle una parte di questo libro, quindi una rielaborazione non lo elimina mai: uno appreso dal modello conta come scritto a mano, uno vecchio resta vecchio. Il messaggio dopo l'importazione indica quanti personaggi sono stati aggiunti o aggiornati e quanti elementi sono stati aggiunti oppure erano già presenti.
<a id="what-goes-into-the-prompt"></a>

## Cosa finisce nel prompt

Per ogni personaggio tracciato che è tra i personaggi presenti, il prompt riceve un blocco come questo:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Vengono inviati solo gli elementi attivi. L'ultima riga (la riga **closed world**) impedisce al modello di attribuire a un personaggio conoscenze che non può avere. Riguarda ciò che la storia racconta (persone, luoghi, segreti, piani, eventi), non ciò che una persona di quel mondo sa comunque, quindi un personaggio conosce ancora la propria città. Puoi disattivare la riga nelle impostazioni. Un personaggio tracciato che non è presente non riceve nulla, nemmeno una riga che dica che non ne sa nulla.

<a id="who-is-present"></a>

### Chi è presente

Il blocco viene costruito a partire dal campo **Present Characters** della parte. Il campo è testo libero, quindi viene letto in modo permissivo:

- il testo tra parentesi quadre viene ignorato: `Tomáš (tired), Mizuki` equivale a Tomáš and Mizuki;
- i nomi vengono separati in base ai **separators** (predefiniti: `,` `;` un ritorno a capo, `/`, `|`, `、` `，` `；`);
- ciò che segue ` - ` o `:` è una nota e viene scartato (`Mara - wounded`);
- maiuscole e minuscole, accenti e un articolo *the* iniziale non contano;
- un nome deve corrispondere **per intero**: *Mara's brother* non è *Mara*. Usa gli alias quando un personaggio è scritto in più di un modo. Il punto è che una corrispondenza mancata costa un solo blocco, mentre una corrispondenza errata descriverebbe un personaggio che non è nella scena.

Il campo *Point of View* non viene letto da solo, quindi scrivi tra i personaggi presenti anche il personaggio in point of view.

<a id="processing"></a>

## Elaborazione

Il plugin segue la storia. **Processing** legge le parti del ramo attivo in ordine, a partire dal **start point**, e mantiene un **cursor** (l'ultima parte letta). Senza il modello, registra chi ha incontrato chi: un personaggio tracciato ha incontrato ogni nome presente nel campo dei personaggi presenti insieme a lui, e il blocco li elenca. Con il modello, trova anche ciò che i personaggi hanno **learned** (vedi [Reading what the characters learn](#reading-what-the-characters-learn)).

![Il pannello di elaborazione](../../images/plugin-knowledgetracker-processing.png)

Il pannello in cima alla scheda mostra a che punto si trova l'elaborazione. Si ricarica da solo quando una nuova parte diventa definitiva (dopo l'annotazione automatica e la riscrittura, se il libro le prevede). Se aggiungi, modifichi o elimini parti in altro modo, premi il pulsante **Refresh** (le frecce accanto all'ingranaggio) per rileggere la storia. Il pannello contiene:

- **Process now**: legge le parti che non sono ancora elaborate.
- **Process next part**: legge solo la prima parte non ancora elaborata, un passo alla volta (con il modello: una richiesta per quella parte, indipendentemente da *Parts per run*). Usalo per provare le impostazioni o per vedere cosa trova il modello in una singola parte. Il comando `knowledge-process` con `parts` impostato a 1 fa lo stesso.
- **Start point**: la parte da cui inizia l'elaborazione (*Start of the book* per impostazione predefinita). Le parti precedenti non vengono mai lette. Spostarlo indietro rilegge le parti a partire dal nuovo inizio, spostarlo in avanti lascia invece ciò che è già stato letto.
- **Processed parts**: le ultime parti lette. Una parte il cui testo è **changed since it was processed** (l'hai modificata o riscritta) viene contrassegnata; nulla viene rielaborato automaticamente per questo motivo. Il pulsante di una riga, **Reprocess from here**, annulla ciò che deriva da quella parte e da quelle successive (gli elementi e le ritrattazioni scritti a mano restano) e le rilegge.

Quando scorri in basso l'elenco dei personaggi oltre questi pulsanti, **Process now**, **Process next part** e **Tidy knowledge** sono disponibili anche nella barra in alto della scheda, così puoi elaborare senza risalire.

Cosa fa il plugin da solo, quando **Process new parts automatically** è attivo (è disattivato per impostazione predefinita):

- Prima che inizi una generazione, vengono elaborate le parti scritte dall'ultima volta, così il prompt sa chi ha incontrato chi. Una parte che viene sostituita con *Regenerate* o con uno swipe viene lasciata in pace. Con il modello, la lettura usa il modello solo quando **Ask before applying** è disattivato (vedi sotto); altrimenti registra soltanto chi ha incontrato chi, e ciò che il modello trova viene avviato dalla scheda, perché una revisione non ti viene mai imposta nel mezzo di una generazione.
- Quando una parte viene **regenerated** e arriva il nuovo testo, ciò che era stato appreso dal vecchio testo viene annullato.
- Quando fai uno **swipe** o passi a un altro ramo, il cursore torna all'ultima parte condivisa dai due rami.
<a id="reading-what-the-characters-learn"></a>

## Lettura di ciò che i personaggi apprendono

Attiva **Read what the characters learn with the model** nelle impostazioni. A questo punto **Process now** chiede al modello, per le parti successive della storia, che cosa ciascun personaggio tracciato presente in esse abbia **appreso**: ciò che ha visto, sentito, letto o gli è stato raccontato. Indica anche quali elementi la storia **contraddice** (*has never met Thomas* dopo che si sono incontrati). La richiesta contiene le parti (con il loro punto di vista e i personaggi presenti), ciò che ogni personaggio tracciato sa in quel momento e, se lo scegli, il lorebook. Al modello viene chiesto di escludere ciò che una persona di quel mondo sa comunque (chi è il re) e ciò che deriva dal personaggio stesso (una succube sa di avere una coda), e di non riportare nulla di cui non sia sicuro. Un personaggio tracciato che non è presente in nessuna delle parti non viene nemmeno interpellato.

Ogni esecuzione è una sola richiesta al modello. Quando **Process now** richiederebbe più di tre richieste (un lungo tratto di storia non ancora elaborato, oppure **Parts per run** impostato su un valore basso), prima ti indica quante parti e richieste sono, e quanto testo passa più o meno attraverso il modello, e parte solo dopo la tua conferma. Ogni richiesta include anche le istruzioni, il lorebook e la conoscenza dei personaggi, quindi il costo reale è superiore al solo testo.

Quando **Ask before applying** è disattivato, le modifiche vengono applicate subito e ciascuna viene registrata nel log **Applied automatically**, nella sezione Processing della scheda: chi ha ottenuto o perso che cosa, e da quale parte. Il contenuto del log non è modificabile, ma ogni riga ha un pulsante **Revert**: un'aggiunta viene rifiutata (resta sotto *Old knowledge*, come accade per un'aggiunta rifiutata nella revisione), una rimozione torna attiva. Una riga il cui elemento è stato modificato, eliminato o ritirato a mano nel frattempo viene lasciata com'è. Il log conserva le ultime 200 modifiche.

<a id="returning-to-an-earlier-point"></a>

### Tornare a un punto precedente

Prima di ogni esecuzione che modifica la conoscenza (un'esecuzione della scheda con o senza revisione, la lettura automatica delle nuove parti, i comandi `knowledge-process` e `knowledge-tidy`, un riordino e una lettura senza modello), il tracker crea un **checkpoint**: lo stato completo dei suoi personaggi così come l'esecuzione li ha trovati, con il punto fino al quale aveva letto (*Start of the book* prima della prima esecuzione). **Checkpoints** nel pannello Processing li elenca, dal più recente: la parte fino alla quale il tracker aveva letto, l'ora, il tipo di esecuzione successiva e quanto ha aggiunto e ritirato. Vengono elencati solo i checkpoint la cui parte si trova sul ramo attivo.

**Return to this point** su una riga annulla quella esecuzione e tutte quelle successive: i personaggi e la loro conoscenza, le parti elaborate e il log delle modifiche automatiche tornano a quelli del checkpoint, e la prossima esecuzione rilegge di nuovo le parti successive alla sua, comprese quelle dell'esecuzione annullata. Le tue impostazioni e il punto di partenza restano invariati. Il programma chiede prima conferma, perché viene annullato tutto ciò che è stato appreso dopo il checkpoint, anche le modifiche fatte a mano da allora; ciò che il tracker aveva viene conservato come checkpoint (*before a return to a checkpoint*), così puoi tornarvi. Usalo quando un'esecuzione o una serie di esecuzioni è andata storta, oppure dopo aver ripristinato un backup più vecchio del libro e voler far riprendere il tracker da un punto di cui ti fidi.

Il tracker conserva gli ultimi 20 checkpoint di un libro. Vengono salvati tra i tuoi file (*Resources*, raggruppati per libro), non nel libro, quindi i backup e le copie del libro non li includono.

Con **Ask before applying** disattivato, la nuova parte viene letta non appena è definitiva: quando la generazione è terminata oppure, se il libro ha l'annotazione automatica (e la riscrittura) o una traduzione attiva, quando anche quella è completata, in modo che il modello legga il testo che tieni e lavori mentre tu leggi. È un passo di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part). La generazione successiva trova quindi la conoscenza aggiornata e non deve attendere. Ciò che resta in sospeso (un'esecuzione interrotta, una finestra chiusa) viene letto prima della generazione successiva, come prima.

Mentre il modello lavora, un piccolo **pannello di avanzamento** resta sopra la pagina. Non è modale: puoi continuare a leggere e modificare la storia, e puoi **trascinarlo** tenendolo per il titolo. Chiuderlo lo nasconde, ma il lavoro prosegue (**Cancel** lo interrompe).

![Il pannello di avanzamento](../../images/plugin-knowledgetracker-progress.png)

Mentre il modello risponde, il pannello mostra **What the model is writing** man mano che arriva: prima il suo ragionamento e poi la risposta, così un'attesa lunga con un modello lento non appare come una barra vuota. Quando la richiesta è completata, lo stesso testo resta sotto **What the model thought** (un blocco per ogni richiesta dell'esecuzione, con ciò che ha risposto), anche quando non ha proposto **nothing** e non si apre alcuna revisione: la scheda mantiene un pulsante **Last run: what the model thought** che riapre il pannello, così puoi vedere perché non è stato aggiunto nulla.

Quando il modello ha risposto, il pannello lo segnala e mostra **Review changes**; lo stesso pulsante si trova nel pannello di elaborazione della scheda (*3 changes to review*). **La revisione non si apre mai da sola.** È una tabella delle modifiche, raggruppate per personaggio:

![Revisione delle modifiche](../../images/plugin-knowledgetracker-review.png)

Sotto la tabella, **What the model thought** (chiuso all'inizio) contiene il ragionamento del modello che ha preceduto la sua risposta, quando il modello lo invia. Mostra perché è stata proposta una modifica, oppure perché ne manca una che ti aspettavi, così puoi valutare la modifica prima di applicarla. Un modello che non ragiona non mostra nulla in quel punto.

| | |
|---|---|
| La casella di controllo | Indica se la modifica viene applicata. All'inizio sono tutte selezionate; ogni personaggio ha **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Un ritiro rende vecchio un elemento (resta conservato, vedi [la scheda](#the-knowledge-tab)). Un'aggiunta è un nuovo elemento, *Knows* oppure *Does not know*. Il testo e il tipo di un'aggiunta possono essere **modificati** prima dell'applicazione. |
| La citazione | Le parole della parte che lo mostrano, e la parte in cui si trovano. Una citazione che non si trova in quella parte viene segnalata: il modello potrebbe averla inventata, verificala prima di accettare. |

**Apply** applica le modifiche selezionate e passa oltre. **Cancel** interrompe l'intera serie e non modifica nulla: le stesse parti vengono proposte di nuovo la volta successiva. Un'aggiunta deselezionata viene **conservata come conoscenza vecchia** con la sua citazione (puoi ripristinarla); un ritiro deselezionato lascia l'elemento com'è. Ciò che il modello ha detto e non si poteva usare (un personaggio su cui non è stato interpellato, un elemento che non esiste, una modifica senza citazione, qualcosa che il personaggio sa già) viene elencato sotto *Left out*. Ciò che è stato applicato porta con sé la sua parte, quindi **Reprocess from here** lo riprende di nuovo. Se la storia è cambiata mentre stavi revisionando (un altro ramo, una parte eliminata), le modifiche non vengono applicate e devi elaborare di nuovo.

Le parti vengono lette in **esecuzioni** di **Parts per run** (1 per impostazione predefinita); una parte che non entra nel contesto del modello viene tagliata fino a ciò che ci sta. Una serie prosegue, esecuzione dopo esecuzione, fino all'ultima parte definitiva; in una serie che ti chiede conferma, un'esecuzione in cui il modello non ha trovato nulla viene saltata senza chiedere. Con **Ask before applying** disattivato, le modifiche vengono applicate subito e il pannello conserva un log con una riga per esecuzione.

!!!
Il prompt è stato provato con un modello su alcune scene, non in modo ampio: impedisce ai personaggi di apprendere ciò che non hanno percepito, ma controlla ciò che viene proposto, soprattutto per i personaggi presenti in una scena lunga. La richiesta viene inviata al modello che scegli qui sotto, con il costo e la privacy abituali di quel servizio.
!!!
<a id="tidying-the-knowledge"></a>

## Riordinare la conoscenza

Il modello scrive quello che ogni personaggio impara, e in una storia lunga la lista cresce, con più piccole cose di quante si vorrebbero inviare al modello a ogni richiesta. **Tidy knowledge** (accanto a **Process now**, richiede l'impostazione del modello) chiede al modello di rileggere ciò che un personaggio sa, con le ultime parti elaborate della storia come contesto, e di

- **eliminare** un elemento che non vale la pena conservare: dettagli trascurabili, un particolare di una scena che tutti i presenti hanno visto (la storia lo conserva), qualcosa che gli eventi successivi hanno superato, qualcosa che dice la stessa cosa di un altro elemento;
- **unire** più elementi sullo stesso argomento in uno solo che conserva ogni fatto.

Un elemento eliminato o unito non viene cancellato: diventa *Old knowledge* e può essere ripristinato. Le modifiche vengono riviste, come quelle di una lettura (**Ask before applying** decide), oppure applicate subito e registrate nel log con un pulsante **Revert**.

Il riordino può toccare solo ciò che il modello ha appreso e che non hai **lock**-ato. Gli elementi iniziali, quelli che hai aggiunto o modificato a mano, quelli bloccati e quelli vecchi non vengono mai cambiati (i primi due tipi e quelli bloccati sono mostrati al modello, così che possa vedere ciò che un elemento duplicherebbe). Un segreto che nessuna parte ha menzionato per molto tempo non è un motivo per eliminarlo, e il prompt lo dice esplicitamente; quando il modello è in dubbio, conserva l'elemento. Un personaggio con meno di **Tidy: from this many items** (8) elementi appresi dal modello non viene riordinato, e **Tidy: parts of context** (10) indica quante delle ultime parti elaborate vengono lette.

**Tidy automatically** (un'impostazione, disattivata per impostazione predefinita) esegue il riordino da solo alla fine di un'esecuzione le cui modifiche vengono applicate senza chiedere (**Ask before applying** disattivato, quindi anche la lettura automatica delle nuove parti), una volta che il modello ha letto **Tidy after this many parts** (10) parti dall'ultimo riordino. Ciò che viene cambiato è applicato e registrato nel log con un pulsante **Revert**, e il log dell'esecuzione lo segnala. Un'esecuzione che revisioni non avvia mai da sola un riordino, e un personaggio con troppi pochi elementi viene saltato senza interpellare il modello.

Lo stesso dalla barra dei comandi: `knowledge-tidy` (`ask true` per avere le modifiche come elenco; la scheda aperta mostra allora il pulsante di revisione), e `lock-knowledge` / `unlock-knowledge` con il personaggio e il numero dell'elemento.

<a id="settings"></a>

## Impostazioni

L'ingranaggio accanto a *Track a character*:

| Setting | |
|---|---|
| **In prompt** | Inserisce il blocco nel prompt di ogni generazione. Il pulsante diventa verde quando è attivo. |
| **Depth** | Dove va, contato in messaggi dalla fine del prompt: `0` dopo le istruzioni del turno, `1` subito prima di esse. Non va mai prima del prompt di sistema. |
| **Role** | Se viene inviato come messaggio *System*, *Assistant* o *User*. |
| **Say that nothing else is known** | La riga del mondo chiuso. |
| **Give what the characters know to the annotation** | Attivo (predefinito): [Annotate with AI](#annotating-with-what-the-characters-know) riceve ciò che i personaggi tracciati della parte sapevano all'inizio. |
| **Process new parts automatically** | Vedi [Processing](#processing). |
| **Read what the characters learn with the model** | L'elaborazione chiede al modello che cosa ha appreso ogni personaggio tracciato e lo propone. Disattivato (predefinito): l'elaborazione annota solo chi ha incontrato chi, il che non costa nulla. |
| **Ask before applying** | Attivo (predefinito): ogni proposta è mostrata per la revisione. Disattivato: viene applicata subito e registrata nel log. |
| **Let the model read earlier parts** | Mentre legge le nuove parti, il modello può leggere la parte in cui un elemento è stato appreso (gli elementi dicono "learned in part 12"), per verificare se ciò che legge conferma, ripete o contraddice quanto appreso: per esempio una diceria che ora viene vista con i propri occhi. Al massimo 5 letture, ciascuna di una parte, per richiesta; il testo di una parte lunga viene tagliato a ciò che entra nel contesto, e la revisione mostra quali parti sono state lette. Ogni lettura è un altro giro verso il modello, quindi è disattivata per impostazione predefinita. Un modello che non sa chiamare funzioni legge la richiesta come prima. |
| **Parts per run** | Quante parti una richiesta legge. Un valore più alto costa meno, ma dà una risposta più lunga e una revisione più difficile. |
| **Tidy automatically**, **Tidy after this many parts** | Il passaggio di riordino parte da solo alla fine di un'esecuzione che applica le sue modifiche senza chiedere, ogni tot parti (10). Disattivato per impostazione predefinita. |
| **Tidy: parts of context** | Quante delle ultime parti elaborate il passaggio di riordino legge (10). |
| **Tidy: from this many items** | Un personaggio con meno elementi appresi dal modello (e non bloccati) non viene riordinato (8). |
| **Lorebook for the model** | *Entries the parts activate* (predefinito: le voci che una generazione userebbe per queste parti), *The whole lorebook* (ogni voce; quando non entra nel contesto, vengono inviate quelle attivate e la nota lo segnala) oppure *No lorebook*. |
| **Model / Protocol for the extraction** | Vuoto: il modello del libro. Per questo basta un modello più economico, e su un server che mantiene una sola cache del prompt, un modello separato evita che venga sostituito. |
| **Separators of this book** | Come il campo dei personaggi presenti viene diviso in nomi, uno per riga (un a capo è `\n`). Una parola come `and` divide tra spazi, quindi *Jack and Jill* resta un unico nome finché non lo aggiungi. |
| **My separators (all books)** | Lo stesso per tutti i tuoi libri. Vincono i separatori del libro; se non ce ne sono, si usano quelli integrati. |

**Use these settings for new books** (nelle impostazioni del modello) salva le impostazioni di questo libro, la lettura con il modello, il modello e il protocollo inclusi, come quelle con cui parte un libro in cui il Knowledge Tracker non è mai stato usato: così un nuovo libro legge con il tuo modello dalla prima esecuzione. I separatori del libro restano con il libro. **Forget the settings for new books** torna alle impostazioni integrate. I libri che già usano il tracker mantengono le proprie impostazioni.

<a id="prompts"></a>

## Prompt

Il messaggio di sistema della lettura (**Knowledge extraction prompt**) e quello del passaggio di riordino (**Knowledge tidy prompt**) sono [impostazioni ereditate](../books/prompts.md#inherited-settings): un libro usa le proprie, altrimenti la cartella più vicina sopra di esso che ne imposta una, altrimenti le tue, altrimenti il testo integrato, che è stato verificato con un modello reale.

Impostali nella scheda *Prompts* del libro, sotto *Extension Settings*, nella finestra di una cartella, oppure per tutti i tuoi libri in *Settings → Extension Settings → Knowledge Tracker* (se vuoto, viene mostrato in grigio il testo integrato). Mantieni il formato JSON della risposta del testo integrato: il plugin legge la risposta in base a esso. Le istruzioni per `read_part` vengono aggiunte dal plugin quando il modello può leggere parti precedenti, quindi il tuo prompt non deve includerle.
<a id="annotating-with-what-the-characters-know"></a>

## Annotare con quello che sanno i personaggi

Il blocco qui sopra impedisce al modello di rendere un personaggio onnisciente *mentre scrive*. Gli stessi registri aiutano anche a trovare
le sviste che sono comunque passate. Quando un modello [annota una parte](../books/annotations.md#letting-a-model-annotate) (**Annota con AI**, un intervallo di parti, l'annotazione
automatica, `/generate-annotation`), il tracciatore gli dice che cosa sapevano i personaggi tracciati di quella parte **all'inizio della parte**,
e l'editor segnala un personaggio che dice, pensa o fa ciò che non può sapere: *Fiora saluta Thomas per nome; i registri dicono che non l'ha mai incontrato.* La nota indica il fatto.

- **I fatti sono quelli della parte, non quelli della fine del libro.** Ciò che un personaggio ha appreso in una parte successiva non è ancora noto; un elemento che una parte successiva ritratta era ancora vero. Ciò che la parte stessa porta (ciò che si apprende, chi viene incontrato, ciò che smette di essere vero in essa) è elencato a parte, così la prima metà della parte non viene verificata con la seconda.
- **Quali personaggi.** I personaggi tracciati che sono nei *Personaggi presenti* della parte, sono il suo punto di vista, o sono nominati nel suo testo (una parola intera, qualunque maiuscola o minuscola, anche gli alias).
- **Gli elementi che non sono fatti in nessun momento** sono esclusi: quelli che hai ritrattato a mano, quelli eliminati dal passaggio di pulizia, quelli che hai rifiutato in una revisione.
- **Solo ciò che è registrato.** Al modello viene detto che ciò che i registri non riportano non è un fatto, così non segnala un personaggio per una cosa che il tracciatore semplicemente non ha mai saputo. *Dire che non si sa altro* aggiunge anche "(nessun altro)" a chi un personaggio ha incontrato.
- Se una parte precedente a quella revisionata non è stata letta dal tracciatore, i fatti lo dicono: ciò che i personaggi hanno appreso lì manca, e **Elabora ora** prima dà una revisione migliore.
- I fatti costano token nella richiesta di annotazione, solo per le parti che hanno un personaggio tracciato. Disattivali con **Includi ciò che sanno i personaggi nell'annotazione**.

<a id="the-block-as-a-macro"></a>

## Il blocco come macro

Invece dell'inserimento automatico (o oltre ad esso) puoi inserire il blocco tu stesso con la
macro [{%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). L'argomento è l'elenco dei personaggi della scena, così un modello può passare il campo della parte:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

La macro stampa il blocco indipendentemente dal fatto che **Nel prompt** sia attivo, quindi disattivalo quando usi la macro, altrimenti il modello lo riceve due volte. Profondità e ruolo non si applicano alla macro (il blocco fa parte del messaggio che contiene la macro). Non stampa nulla quando nessuno dei personaggi elencati è tracciato. La risposta di una macro di comando viene conservata con la parte da cui continua la nuova parte, quindi una seconda generazione dalla stessa parte non vede una modifica fatta nel frattempo; una rigenerazione e uno scambio chiedono di nuovo.

<a id="commands"></a>

## Comandi

Per la [riga di comando](../commands/index.md) e per gli agenti:

| Comando | |
|---|---|
| `knowledge-status` | Le impostazioni e a che punto è l'elaborazione. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | I personaggi tracciati, indicati per nome, alias o numero. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Gli elementi di un personaggio, indicati con il loro numero in `list-knowledge`. |
| `knowledge-process` | Elabora ora (`parts`: al massimo quel numero). Con il modello attivo, `ask true` restituisce ciò che il modello ha trovato come modifiche numerate e le trattiene: non cambia ancora nulla. La scheda Conoscenza aperta mostra un pulsante **modifiche trovate da un comando** che apre la solita revisione (Applica / Annulla); un agente le applica con `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Applica le modifiche trattenute, tutte o i numeri indicati (uguale a **Applica** nella revisione della scheda). |
| `knowledge-tidy` | Il passaggio di pulizia. Con `ask true` restituisce le modifiche come elenco numerato e le trattiene (la scheda mostra un pulsante per rivederle; `apply-knowledge-changes` le applica). |
| `lock-knowledge`, `unlock-knowledge` | Bloccano un elemento (personaggio, numero dell'elemento) in modo che il passaggio di pulizia lo lasci stare, oppure lo sbloccano. |
| `knowledge-reprocess` | Rielabora da una parte (il suo identificativo o la sua posizione); chiede conferma prima. Con il modello attivo riprende solo lo stato, `knowledge-process` rilegge le parti. |
| `knowledge-set-start` | Imposta il punto di partenza; senza una parte, l'inizio del libro. |
| `knowledge-tracker-block` | Il blocco come testo, per la macro. |

La riga di comando divide gli argomenti agli spazi: racchiudi tra virgolette un elenco di personaggi (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Archiviazione

Tutto è salvato con il libro (nei suoi attributi), quindi fa parte dei [backup del libro](../books/backups.md) e delle copie. I
[checkpoint](#returning-to-an-earlier-point) non lo sono: sono file tuoi collegati al libro.
