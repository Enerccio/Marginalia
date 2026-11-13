---
label: Tracciamento della conoscenza
order: 11
---

# Tracciamento della conoscenza

Tiene traccia di ciò che i personaggi della tua storia sanno, e di ciò che **non** sanno, e lo inserisce nel prompt. Un modello
tende a far sapere a un personaggio cose su persone che non ha mai incontrato (il loro nome, il loro lavoro, il loro volto), a meno che
le istruzioni non lo impediscano; con il Knowledge Tracker il prompt indica, per ogni personaggio tracciato nella scena,
che cosa quel personaggio sa e che non sa nient'altro.

!!!
Sei tu a scegliere chi viene tracciato. Al modello vengono descritti solo i personaggi che aggiungi alla scheda, e solo finché
sono tra i *personaggi presenti* della parte in scrittura. Nulla viene dedotto dal testo della storia.
!!!

<a id="the-knowledge-tab"></a>

## La scheda Knowledge

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Knowledge** accanto a *Struttura*. Tutto viene salvato nel libro man mano che lo modifichi.

![La scheda Knowledge](../../images/plugin-knowledgetracker.png)

**Traccia un personaggio** chiede un **nome** e, facoltativamente, **alias**: gli altri nomi con cui il personaggio è scritto nel
campo *Personaggi presenti* di una parte, separati da virgole. Un nome o un alias può appartenere a un solo personaggio tracciato.

Apri un personaggio con la freccia sulla sua scheda per vedere che cosa sa:

| | |
|---|---|
| **Sa** (verde) / **Non sa** (rosso) | Una breve frase su qualsiasi cosa: una persona, un luogo, un segreto, un evento (*Peter sa del complotto contro il duca*, *Fiora non sa che Thomas è suo padre*). Scrivi in modo esplicito ciò che un personaggio **non** sa, perché è quello che il modello rispetta. |
| L'ordine e **#12** | Le conoscenze più recenti stanno in cima (anche quelle vecchie). Un elemento appreso dal modello termina con il numero piccolo della parte in cui è stato appreso (**#12**): un clic fa scorrere la storia fino a quella parte e la fa lampeggiare (su un telefono prima chiude il cassetto). Gli elementi che hai scritto tu non hanno numero, e non ne ha nemmeno quelli la cui parte non è nel ramo attivo (un altro ramo, o la parte è stata eliminata). |
| **Aggiungi conoscenza** | Aggiunge un elemento, subito attivo. Mantienilo a **un solo pensiero**: un elemento che dice due cose (*non sa chi è Thomas, non l'ha mai incontrato*) viene ritirato non appena una delle due smette di essere vera. Il pulsante **modifica** cambia il testo e il tipo. |
| **Ritira** (il cerchio barrato) | L'elemento non è più vero (*non ha incontrato Thomas* dopo che si sono incontrati). Non viene eliminato: diventa *conoscenza vecchia* e non viene più inviato al modello. |
| **Blocca** (il lucchetto, sugli elementi appresi dal modello) | Il [riordino](#tidying-the-knowledge) non vede mai un elemento bloccato: usalo per ciò che deve restare, un tradimento, un segreto. La storia può comunque contraddire un elemento bloccato (un personaggio che perde la memoria): la lettura della storia lo ritira come di consueto. |
| **Elimina** | Rimuove un elemento in modo definitivo, dopo una domanda di conferma. Ritira lo conserva, quindi usalo quando hai dei dubbi. |
| **Seleziona elementi** (accanto a **Aggiungi conoscenza**) | Elimina rapidamente molti elementi: ogni elemento del personaggio, anche quelli vecchi, riceve una casella di controllo. **Seleziona tutto** / **Deseleziona tutto** spuntano o tolgono tutte le caselle, **Elimina selezionati (n)** rimuove in modo definitivo quelli spuntati dopo una domanda di conferma, **Annulla** esce dalla selezione. |
| **Ha incontrato** | Le persone che il personaggio ha incontrato, annotate dall'[elaborazione](#processing). L'elaborazione può annotare un nome che non è una persona (una nota nel campo dei personaggi presenti): apri **Modifica** e cambia l'elenco **Ha incontrato**, un nome dopo l'altro separati da virgole. I nomi che scrivi restano quando la storia viene riletta; uno che rimuovi ritorna solo se la parte in cui è stato visto viene riletta. |
| **Conoscenza vecchia (n)** | Chiusa per impostazione predefinita. Ogni elemento vecchio può essere **copiato**, **modificato**, **ripristinato** (torna attivo, da ora in poi) o eliminato. |

<a id="mass-changes"></a>

### Modifiche di massa

Il pulsante **Modifiche di massa** (l'icona a elenco accanto a **Traccia un personaggio**) apre una finestra con ogni personaggio tracciato e
tutti i suoi elementi, anche quelli vecchi, in una griglia con caselle di controllo: il tipo, il testo, lo stato (*Attiva*, oppure vecchia: *ritirata*,
*rifiutata in una revisione*, *riordinata*), la provenienza (*Stato iniziale*, *Scritto a mano*, *Appreso dalla storia*) e la
parte in cui è stato appreso. Spunta gli elementi in quante griglie vuoi (la casella nell'intestazione di una griglia spunta tutti i suoi elementi,
**Seleziona tutto** / **Deseleziona tutto** nella barra in basso spuntano tutti gli elementi di tutti i personaggi), poi:

| | |
|---|---|
| **Ritira selezionati (n)** | Gli elementi attivi spuntati diventano conoscenza vecchia, come fa **Ritira** per uno solo. |
| **Ripristina selezionati (n)** | Gli elementi vecchi spuntati tornano attivi, da ora in poi. |
| **Elimina selezionati (n)** | Rimuove in modo definitivo gli elementi spuntati, dopo una domanda di conferma. |

I numeri sui pulsanti indicano quanti degli elementi spuntati ciascuno modifica. Le spunte restano dopo una modifica, così gli elementi
ritirati possono essere eliminati subito dopo.

**Esporta in JSON** (in alto nella finestra) scarica come file i personaggi tracciati del libro (nomi, alias, elementi con tipo,
stato, origine e blocco, le persone incontrate); non include nulla dell'elaborazione, quindi funziona con qualsiasi libro.
**Importa da JSON** legge un file di questo tipo nel libro e si limita ad aggiungere: un personaggio il cui nome il libro già traccia
(come nome o alias) riceve gli alias, gli elementi e le persone incontrate che ancora non ha (un elemento con lo stesso tipo e
testo viene saltato), gli altri diventano nuovi personaggi tracciati; un alias che un altro personaggio ha già viene escluso. Un elemento importato
non ha alcuna parte di questo libro alle spalle, quindi una rielaborazione non lo elimina mai: quello appreso dal modello conta come scritto a mano,
uno vecchio resta vecchio. Il messaggio dopo l'importazione indica quanti personaggi sono stati aggiunti o aggiornati e quanti elementi sono stati
aggiunti o erano già presenti.

<a id="what-goes-into-the-prompt"></a>

## Che cosa finisce nel prompt

Per ogni personaggio tracciato che è tra i personaggi presenti, il prompt riceve un blocco come questo:

```
Che cosa sanno i personaggi di questa scena. Un personaggio agisce solo in base alla conoscenza elencata per lui:

Fiora sa: Mara, una corriera della Gilda delle Lanterne e la sorella della sua amica Nell; deve quaranta corone alla Gilda delle Lanterne.
Fiora non sa: che suo padre è vivo, né che Thomas è suo padre.
Fiora ha incontrato: Mara.
Fiora è attualmente con: Mara, Thomas.
Fiora non ha mai incontrato né sentito parlare di nessun altro in questa storia e non sa nient'altro dei suoi segreti, piani o eventi.
```

Vengono inviati solo gli elementi attivi. L'ultima riga (la riga di **mondo chiuso**) è ciò che impedisce al modello di attribuire
al personaggio una conoscenza che non può avere; riguarda ciò che la storia racconta (persone, luoghi, segreti, piani, eventi), non ciò che una persona di quel
mondo sa comunque, quindi un personaggio conosce ancora la propria città. Puoi disattivare la riga nelle impostazioni. Un
personaggio tracciato che non è presente non riceve nulla, nemmeno una riga che dice che non sa.

<a id="who-is-present"></a>

### Chi è presente

Il blocco viene costruito dal campo **Personaggi presenti** della parte. Il campo è testo libero, quindi viene letto in modo tollerante:

- il testo tra parentesi quadre viene ignorato: `Tomáš (stanco), Mizuki` sono Tomáš e Mizuki;
- i nomi vengono separati ai **separatori** (predefiniti: `,` `;` a capo, `/`, `|`, `、` `，` `；`);
- ciò che si trova dopo ` - ` o `:` è una nota e viene scartato (`Mara - ferita`);
- maiuscole e minuscole, accenti e un eventuale *the* iniziale non contano;
- un nome deve corrispondere **per intero**: *il fratello di Mara* non è *Mara*. Usa gli alias quando un personaggio è scritto in più di un modo. Il punto è che una corrispondenza mancata costa un blocco, mentre una corrispondenza errata descriverebbe un personaggio che non è nella scena.

Il campo *Point of View* non viene letto da solo, quindi scrivi anche il personaggio del punto di vista tra i personaggi presenti.
<a id="processing"></a>

## Elaborazione

Il plugin segue la storia. **Elaborazione** legge le parti del ramo attivo in ordine, a partire dal **punto di partenza**, e
mantiene un **cursore** (l'ultima parte letta). Senza il modello registra chi ha incontrato chi: un personaggio tracciato ha incontrato
ogni nome presente nel campo dei personaggi presenti insieme a lui, e il blocco li elenca. Con il modello trova anche ciò che i
personaggi **hanno appreso** (vedi [Lettura di ciò che i personaggi apprendono](#reading-what-the-characters-learn)).

![Il pannello di elaborazione](../../images/plugin-knowledgetracker-processing.png)

Il pannello in cima alla scheda mostra a che punto si è. Si ricarica da solo quando una nuova parte è definitiva (dopo l'annotazione
e la riscrittura automatiche, se il libro le prevede); se aggiungi, modifichi o elimini parti in altro modo, premi il pulsante **Aggiorna** (le
frecce accanto all'ingranaggio) per rileggere la storia. Contiene:

- **Elabora ora**: legge le parti che non sono ancora state elaborate.
- **Elabora la parte successiva**: legge solo la prima parte non ancora elaborata, un passo alla volta (con il modello: una richiesta per quella parte, indipendentemente da quanto indica *Parti per esecuzione*). Usalo per provare le impostazioni o per vedere cosa trova il modello per una singola parte. Il comando `knowledge-process` con `parts` impostato a 1 fa lo stesso.
- **Punto di partenza**: la parte da cui inizia l'elaborazione (*Inizio del libro* per impostazione predefinita). Le parti precedenti non vengono mai lette. Spostarlo indietro rilegge le parti dal nuovo inizio, spostarlo in avanti lascia quanto già letto.
- **Parti elaborate**: le ultime parti lette. Una parte il cui testo **è cambiato dopo l'elaborazione** (l'hai modificata o riscritta) viene contrassegnata; per questo motivo nulla viene rielaborato da solo. Il pulsante di una riga, **Rielabora da qui**, annulla ciò che proviene da quella parte e dalle successive (gli elementi e le ritrattazioni scritti a mano restano) e le rilegge.

Ciò che il plugin fa da solo, quando **Elabora automaticamente le nuove parti** è attivo (è disattivato per impostazione predefinita):

- Prima che inizi una generazione, vengono elaborate le parti scritte dall'ultima volta, così il prompt sa chi ha incontrato chi. Una parte
  che viene sostituita da una *Rigenera* o da uno swipe viene lasciata in pace. Con il modello, questa operazione legge con il modello solo quando
  **Chiedi prima di applicare** è disattivato (vedi sotto); altrimenti registra soltanto chi ha incontrato chi, e ciò che il modello trova
  viene avviato dalla scheda, perché una revisione non ti viene mai imposta nel mezzo di una generazione.
- Quando una parte viene **rigenerata** e arriva il nuovo testo, viene annullato ciò che era stato appreso dal vecchio testo.
- Quando fai uno **swipe** o passi a un altro ramo, il cursore torna all'ultima parte condivisa da entrambi i rami.
<a id="reading-what-the-characters-learn"></a>

## Lettura di ciò che i personaggi apprendono

Attiva **Read what the characters learn with the model** nelle impostazioni. A quel punto **Process now** chiede al modello, per le parti successive della storia, che cosa ogni personaggio tracciato presente in esse ha **appreso**: ciò che ha visto, sentito, letto o gli è stato detto. Indica anche quali elementi la storia **contraddice** (*has never met Thomas* dopo che si incontrano). La richiesta contiene le parti (con il loro punto di vista e i personaggi presenti), ciò che ogni personaggio tracciato sa ora e, se lo scegli, il lorebook; al modello viene chiesto di escludere ciò che una persona di quel mondo sa comunque (chi è il re) e ciò che deriva dal personaggio stesso (una succube sa di avere una coda), e di non riportare nulla di cui non sia sicuro. Un personaggio tracciato che non è presente in nessuna delle parti non viene nemmeno interrogato.

Ogni esecuzione è una sola richiesta al modello. Quando **Process now** avrebbe bisogno di più di tre richieste (un lungo tratto di storia non ancora elaborato, oppure **Parts per run** impostato su un valore basso), prima ti indica quante parti e richieste sono e quanto testo passerà, all'incirca, attraverso il modello, e parte solo dopo la tua conferma. Ogni richiesta porta con sé anche le istruzioni, il lorebook e la conoscenza dei personaggi, quindi il costo reale è più alto del solo testo.

Quando **Ask before applying** è disattivato, le modifiche vengono applicate subito, e ciascuna viene scritta nel registro **Applied automatically** della sezione Processing della scheda: chi ha ottenuto o perso che cosa, e da quale parte. Nulla nel registro è modificabile, ma ogni riga ha un pulsante **Revert**: un elemento aggiunto viene rifiutato (resta sotto *Old knowledge*, come accade per un'aggiunta rifiutata nella revisione), un elemento ritirato torna attivo. Una riga il cui elemento è stato modificato, eliminato o ritirato a mano nel frattempo viene lasciata com'è. Il registro conserva le ultime 200 modifiche.

Con **Ask before applying** disattivato, la nuova parte viene letta non appena è definitiva: al termine della generazione oppure, se il libro ha l'annotazione automatica (e riscrittura) o una traduzione attiva, quando quella è completata, così il modello legge il testo che tieni e lavora mentre tu leggi. È un passo di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part). La generazione successiva trova quindi la conoscenza aggiornata e non deve attendere. Ciò che resta in sospeso (un'esecuzione che hai interrotto, una finestra che hai chiuso) viene letto prima della generazione successiva, come prima.

Mentre il modello lavora, sopra la pagina compare un piccolo **progress panel**. Non è **modale**: puoi continuare a leggere e a modificare la storia, e puoi **trascinarlo** dal suo titolo. Chiuderlo lo nasconde, ma il lavoro prosegue (**Cancel** lo interrompe).

![Il pannello di avanzamento](../../images/plugin-knowledgetracker-progress.png)

Mentre il modello risponde, il pannello mostra **What the model is writing** man mano che arriva: prima il suo ragionamento e poi la risposta, così una lunga attesa con un modello lento non appare come una barra vuota. Quando la richiesta è conclusa, lo stesso testo resta sotto **What the model thought** (un blocco per ogni richiesta dell'esecuzione, con ciò che ha risposto), anche quando non ha proposto **nothing** e non si apre alcuna revisione: la scheda mantiene un pulsante **Last run: what the model thought** che riapre il pannello, così puoi vedere perché non è stato aggiunto nulla.

Quando il modello ha risposto, il pannello lo segnala e mostra **Review changes**; lo stesso pulsante è nel pannello di elaborazione della scheda (*3 changes to review*). **La revisione non si apre mai da sola.** È una tabella delle modifiche, raggruppate per personaggio:

![Revisione delle modifiche](../../images/plugin-knowledgetracker-review.png)

Sotto la tabella, **What the model thought** (chiuso all'inizio) contiene il ragionamento del modello che precede la sua risposta, quando il modello lo invia: mostra perché è stata proposta una modifica, o perché una che ti aspettavi manca, così puoi valutare la modifica prima di applicarla. Un modello che non ragiona non mostra nulla lì.

| | |
|---|---|
| La casella di controllo | Se la modifica viene applicata. All'inizio sono tutte selezionate; ogni personaggio ha **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Un ritiro rende vecchio un elemento (resta conservato, vedi [la scheda](#the-knowledge-tab)). Un'aggiunta è un nuovo elemento, *Knows* oppure *Does not know*. Il testo e il tipo di un'aggiunta possono essere **modificati** prima di essere applicati. |
| La citazione | Le parole della parte che lo mostrano, e in quale parte si trovano. Una citazione non trovata in quella parte viene segnalata: il modello potrebbe averla inventata, verificala prima di accettare. |

**Apply** applica le modifiche selezionate e passa oltre. **Cancel** interrompe l'intera serie e non cambia nulla: le stesse parti verranno proposte di nuovo la volta successiva. Un'aggiunta che deselezioni viene **conservata come conoscenza vecchia** con la sua citazione (puoi ripristinarla); un ritiro deselezionato lascia l'elemento com'è. Ciò che il modello ha detto e non è stato utilizzabile (un personaggio su cui non era stato interrogato, un elemento che non esiste, una modifica senza citazione, qualcosa che il personaggio sa già) è elencato sotto *Left out*. Ciò che è stato applicato porta con sé la sua parte, quindi **Reprocess from here** lo riprende. Se la storia è cambiata mentre stavi revisionando (un altro ramo, una parte eliminata), le modifiche non vengono applicate e devi elaborare di nuovo.

Le parti vengono lette a **run** di **Parts per run** (1 per impostazione predefinita); una che non entra nel contesto del modello viene tagliata a ciò che ci sta. Una serie prosegue, esecuzione dopo esecuzione, fino all'ultima parte definitiva; in una serie che ti chiede conferma, un'esecuzione in cui il modello non ha trovato nulla viene saltata senza chiedere. Con **Ask before applying** disattivato, le modifiche vengono applicate subito e il pannello conserva un registro con una riga per esecuzione.

!!!
Il prompt è stato provato con un modello su alcune scene, non in modo esteso: impedisce ai personaggi di apprendere ciò che non hanno percepito, ma controlla ciò che viene proposto, soprattutto per i personaggi presenti in una lunga scena. La richiesta viene inviata al modello che scegli qui sotto, con il consueto costo e la consueta privacy di quel servizio.
!!!

<a id="tidying-the-knowledge"></a>

## Riordinare la conoscenza

Il modello annota ciò che ogni personaggio apprende, e in una storia lunga l'elenco cresce, con più dettagli minori di quanti vorresti inviare al modello a ogni richiesta. **Tidy knowledge** (accanto a **Process now**, richiede l'impostazione del modello) chiede al modello di rileggere ciò che un personaggio sa, con le ultime parti elaborate della storia come contesto, e di

- **eliminare** un elemento che non vale la pena conservare: dettagli trascurabili, un particolare di una scena che tutti i presenti hanno visto (la storia lo conserva), qualcosa superato da eventi successivi, qualcosa che dice la stessa cosa di un altro elemento;
- **unire** più elementi sulla stessa cosa in uno solo che conserva ogni fatto.

Un elemento eliminato o unito non viene cancellato: diventa *Old knowledge* e può essere ripristinato. Le modifiche vengono revisionate, come quelle di una lettura (**Ask before applying** decide), oppure applicate subito e scritte nel registro con un pulsante **Revert**.

Il riordino può toccare solo ciò che il modello ha appreso e che tu non hai **bloccato** (**lock**). Gli elementi iniziali, quelli che hai aggiunto o modificato a mano, quelli bloccati e quelli vecchi non vengono mai cambiati (i primi due tipi e quelli bloccati vengono mostrati al modello perché possa vedere ciò che un elemento duplicherebbe). Un segreto che nessuna parte menziona da tempo non è un motivo per eliminarlo, e il prompt lo dice esplicitamente; quando il modello ha dei dubbi, conserva l'elemento. Un personaggio con meno di **Tidy: from this many items** (8) elementi appresi dal modello non viene riordinato, e **Tidy: parts of context** (10) è il numero delle ultime parti elaborate che vengono lette.

**Tidy automatically** (un'impostazione, disattivata per impostazione predefinita) esegue il riordino da solo alla fine di un'esecuzione le cui modifiche vengono applicate senza chiedere (**Ask before applying** disattivato, quindi anche la lettura automatica delle nuove parti), una volta che il modello ha letto **Tidy after this many parts** (10) parti dall'ultimo riordino. Ciò che modifica viene applicato e scritto nel registro con un pulsante **Revert**, e il registro dell'esecuzione lo segnala. Un'esecuzione che revisioni non avvia mai un riordino da sola, e un personaggio con troppi pochi elementi viene saltato senza interpellare il modello.

Lo stesso dalla barra dei comandi: `knowledge-tidy` (`ask true` per ottenere le modifiche come elenco; la scheda aperta mostra allora il pulsante di revisione), e `lock-knowledge` / `unlock-knowledge` con il personaggio e il numero dell'elemento.
<a id="settings"></a>

## Impostazioni

L'ingranaggio accanto a *Track a character*:

| Impostazione | |
|---|---|
| **In prompt** | Inserisce il blocco nel prompt di ogni generazione. Il pulsante diventa verde mentre è attivo. |
| **Depth** | Dove va, contato in messaggi dalla fine del prompt: `0` dopo le istruzioni del turno, `1` appena prima di esse. Non finisce mai prima del prompt di sistema. |
| **Role** | Se viene inviato come messaggio *System*, *Assistant* o *User*. |
| **Say that nothing else is known** | La riga del mondo chiuso. |
| **Give what the characters know to the annotation** | Attivo (predefinito): [Annota con l'IA](#annotating-with-what-the-characters-know) riceve ciò che i personaggi tracciati della parte sapevano all'inizio della parte. |
| **Process new parts automatically** | Vedi [Elaborazione](#processing). |
| **Read what the characters learn with the model** | L'elaborazione chiede al modello che cosa ha appreso ciascun personaggio tracciato e lo propone. Disattivo (predefinito): l'elaborazione registra solo chi ha incontrato chi, senza costi. |
| **Ask before applying** | Attivo (predefinito): ogni proposta viene mostrata per la revisione. Disattivo: viene applicata subito e registrata nel log. |
| **Let the model read earlier parts** | Mentre legge le parti nuove, il modello può leggere la parte in cui un elemento è stato appreso (gli elementi dicono "appreso nella parte 12"), per verificare se ciò che legge conferma, ripete o contraddice quell'elemento: per esempio una diceria che ora si vede con i propri occhi. Al massimo 5 letture, una per parte, per ogni richiesta; il testo di una parte lunga viene tagliato a quanto entra nel contesto, e la revisione mostra quali parti ha letto. Ogni lettura è un ulteriore passaggio verso il modello, quindi è disattivata per impostazione predefinita. Un modello che non può chiamare funzioni legge la richiesta come prima. |
| **Parts per run** | Quante parti legge una richiesta. Un numero più alto costa meno ma produce una risposta più lunga e una revisione più difficile. |
| **Tidy automatically**, **Tidy after this many parts** | La passata di riordino viene eseguita da sola alla fine di un'esecuzione che applica le modifiche senza chiedere, ogni tot parti (10). Disattivata per impostazione predefinita. |
| **Tidy: parts of context** | Quante delle ultime parti elaborate legge la passata di riordino (10). |
| **Tidy: from this many items** | Un personaggio con meno elementi appresi dal modello (e non bloccati) non viene riordinato (8). |
| **Lorebook for the model** | *Entries the parts activate* (predefinito: le voci che una generazione userebbe per queste parti), *The whole lorebook* (ogni voce; quando non entra nel contesto, vengono inviate quelle attivate e la nota lo segnala) oppure *No lorebook*. |
| **Model / Protocol for the extraction** | Vuoto: il modello del libro. Per questo basta un modello più economico, e su un server che mantiene una sola cache dei prompt un modello separato evita che venga sostituito. |
| **Separators of this book** | Come il campo dei personaggi presenti viene diviso in nomi, uno per riga (un a capo è `\n`). Una parola come `and` divide tra gli spazi, quindi *Jack and Jill* resta un solo nome finché non lo aggiungi all'elenco. |
| **My separators (all books)** | Lo stesso, per tutti i tuoi libri. Vincono i separatori del libro; se non ce ne sono, si usano quelli predefiniti. |

<a id="prompts"></a>

## Prompt

Il messaggio di sistema della lettura (**Knowledge extraction prompt**) e quello della passata di riordino (**Knowledge tidy prompt**) sono
[impostazioni ereditate](../books/prompts.md#inherited-settings): un libro usa le proprie, altrimenti la cartella più vicina sopra di esso che ne imposta una,
altrimenti le tue, altrimenti il testo predefinito, testato con un modello reale.

Impostali nella scheda *Prompt* del libro, sotto *Extension Settings*, nella finestra di una cartella, oppure per tutti i tuoi libri in
*Settings → Extension Settings → Knowledge Tracker* (vuoto mostra il testo predefinito in grigio). Mantieni il formato di risposta JSON del
testo predefinito: il plugin legge la risposta in base a esso. Le istruzioni per `read_part` vengono aggiunte dal plugin quando il modello può leggere
le parti precedenti, quindi il tuo prompt non ne ha bisogno.

<a id="annotating-with-what-the-characters-know"></a>

## Annotare con ciò che i personaggi sanno

Il blocco qui sopra impedisce che un personaggio diventi onnisciente *mentre scrivi*. Le stesse registrazioni aiutano anche a trovare le sviste che
sono passate comunque. Quando un modello [annota una parte](../books/annotations.md#letting-a-model-annotate) (**Annota con l'IA**, un intervallo di parti, l'annotazione
automatica, `/generate-annotation`), il tracciatore gli comunica che cosa sapevano i personaggi tracciati di quella parte **all'inizio della parte**, e l'editor
segnala un personaggio che dice, pensa o fa ciò che non può sapere: *Fiora saluta Thomas per nome; le registrazioni dicono che non l'ha mai incontrato.* La nota
indica il fatto.

- **I fatti sono quelli della parte, non quelli della fine del libro.** Ciò che un personaggio impara in una parte successiva non è ancora noto; un elemento che una
  parte successiva ritratta era ancora vero. Ciò che la parte stessa porta (ciò che viene appreso, chi viene incontrato, ciò che smette di essere vero in essa) è
  elencato a parte, così la prima metà della parte non viene verificata con la seconda.
- **Quali personaggi.** Quelli tracciati che sono nei *Personaggi presenti* della parte, sono il suo punto di vista, o sono nominati nel suo testo
  (una parola intera, senza distinzione di maiuscole, anche con gli pseudonimi).
- **Gli elementi che non sono mai fatti** vengono esclusi: quelli che hai ritrattato a mano, quelli eliminati dalla passata di riordino, quelli che hai rifiutato
  in una revisione.
- **Solo ciò che è registrato.** Al modello viene detto che ciò che le registrazioni non dicono non è un fatto, così non segnala un personaggio per una cosa
  che il tracciatore semplicemente non ha mai saputo. *Say that nothing else is known* aggiunge anche "(nessun altro)" a chi un personaggio ha incontrato.
- Se una parte precedente a quella revisionata non è stata letta dal tracciatore, i fatti lo segnalano: ciò che i personaggi vi hanno appreso manca, e
  **Elabora ora** prima offre una revisione migliore.
- I fatti consumano token nella richiesta di annotazione, solo per le parti che hanno un personaggio tracciato. Disattivali con **Give what
  the characters know to the annotation**.

<a id="the-block-as-a-macro"></a>

## Il blocco come macro

Invece di (o oltre a) l'inserimento automatico puoi collocare il blocco da solo con la macro
[{%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). L'argomento è l'elenco dei personaggi della scena, quindi un modello può passare il campo della parte:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

La macro stampa il blocco che **In prompt** sia attivo o meno, quindi disattivalo quando usi la macro, altrimenti il modello lo riceve due volte. Depth e role non si applicano
alla macro (il blocco fa parte del messaggio che la contiene). Non stampa nulla quando
nessuno dei personaggi elencati è tracciato. La risposta di una macro di comando viene conservata con la parte da cui continua la nuova parte,
quindi una seconda generazione dalla stessa parte non vede una modifica fatta nel frattempo; una rigenerazione e uno swipe la richiedono di nuovo.

<a id="commands"></a>

## Comandi

Per la [riga di comando](../commands/index.md) e per gli agenti:

| Comando | |
|---|---|
| `knowledge-status` | Le impostazioni e a che punto è l'elaborazione. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | I personaggi tracciati, indicati per nome, pseudonimo o numero. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Gli elementi di un personaggio, indicati con il loro numero in `list-knowledge`. |
| `knowledge-process` | Elabora ora (`parts`: al massimo quel numero di parti). Con il modello attivo, `ask true` restituisce ciò che il modello ha trovato come modifiche numerate e le trattiene: per ora non cambia nulla. La scheda Conoscenza aperta mostra un pulsante **changes found by a command** che apre la solita revisione (Apply / Cancel); un agente le applica con `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Applica le modifiche trattenute, tutte o i numeri indicati (come **Apply** nella revisione della scheda). |
| `knowledge-tidy` | La passata di riordino. Con `ask true` restituisce le modifiche come elenco numerato e le trattiene (la scheda mostra un pulsante per revisionarle; `apply-knowledge-changes` le applica). |
| `lock-knowledge`, `unlock-knowledge` | Bloccano un elemento (personaggio, numero dell'elemento) perché la passata di riordino non lo tocchi, oppure lo sbloccano. |
| `knowledge-reprocess` | Rielabora a partire da una parte (il suo identificativo o la sua posizione); chiede conferma prima. Con il modello attivo riporta indietro solo lo stato, `knowledge-process` rilegge le parti. |
| `knowledge-set-start` | Imposta il punto di partenza; senza parte, l'inizio del libro. |
| `knowledge-tracker-block` | Il blocco come testo, per la macro. |

La riga di comando divide gli argomenti agli spazi: racchiudi tra virgolette un elenco di personaggi (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Archiviazione

Tutto viene salvato insieme al libro (nei suoi attributi), quindi fa parte dei [backup del libro](../books/backups.md) e delle copie.
