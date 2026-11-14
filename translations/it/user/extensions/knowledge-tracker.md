---
label: Tracciatore di conoscenze
order: 11
---

# Tracciatore di conoscenze

Tiene traccia di ciò che i personaggi della tua storia sanno e di ciò che **non** sanno, e lo inserisce nel prompt. Un modello
tende a far sapere a un personaggio cose su persone che non ha mai incontrato (il loro nome, il loro lavoro, il loro volto), a meno che
le istruzioni non lo impediscano; con il Tracciatore di conoscenze il prompt indica, per ogni personaggio tracciato nella scena,
che cosa quel personaggio sa e che non sa nient'altro.

!!!
Sei tu a scegliere chi viene tracciato. Al modello vengono descritti solo i personaggi che aggiungi alla scheda, e solo mentre
sono tra i *personaggi presenti* della parte che si sta scrivendo. Non viene dedotto nulla dal testo della storia.
!!!

<a id="the-knowledge-tab"></a>

## La scheda Conoscenze

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Conoscenze** accanto a *Struttura*. Tutto viene salvato nel libro man mano che lo modifichi.

![La scheda Conoscenze](../../images/plugin-knowledgetracker.png)

**Traccia un personaggio** chiede un **nome** e, facoltativamente, **alias**: gli altri nomi con cui il personaggio compare nel
campo *Personaggi presenti* di una parte, separati da virgole. Un nome o un alias può appartenere a un solo personaggio tracciato.

Apri un personaggio con la freccia sulla sua scheda per vedere che cosa sa:

| | |
|---|---|
| **Sa** (verde) / **Non sa** (rosso) | Una breve frase su qualsiasi cosa: una persona, un luogo, un segreto, un evento (*Pietro sa del complotto contro il duca*, *Fiora non sa che Tommaso è suo padre*). Scrivi esplicitamente ciò che un personaggio **non** sa, perché è questo che il modello rispetta. |
| L'ordine e **#12** | La conoscenza più recente è in cima (anche quella vecchia). Un elemento appreso dal modello termina con il numero piccolo della parte in cui è stato appreso (**#12**): un clic fa scorrere la storia fino a quella parte e la fa lampeggiare (su telefono prima chiude il cassetto). Gli elementi che hai scritto tu non hanno numero, e nemmeno quelli la cui parte non è nel ramo attivo (un altro ramo, oppure la parte è stata eliminata). |
| **Aggiungi conoscenza** | Aggiunge un elemento, subito attivo. Mantienilo su **un solo pensiero**: un elemento che dice due cose (*non sa chi sia Tommaso, non lo ha mai incontrato*) viene ritirato non appena una delle due smette di essere vera. Il pulsante **modifica** cambia il testo e il tipo. |
| **Ritira** (il cerchio barrato) | L'elemento non è più vero (*non ha incontrato Tommaso* dopo che si sono incontrati). Non viene eliminato: diventa *conoscenza vecchia* e non viene più inviato al modello. |
| **Blocca** (il lucchetto, sugli elementi appresi dal modello) | Il [passaggio di riordino](#tidying-the-knowledge) non vede mai un elemento bloccato: usalo per ciò che deve restare, un tradimento, un segreto. La storia può comunque contraddire un elemento bloccato (un personaggio che perde la memoria): la lettura della storia lo ritira come di consueto. |
| **Elimina** | Rimuove un elemento per sempre, dopo una domanda di conferma. Ritira lo conserva, quindi usalo quando non sei sicuro. |
| **Seleziona elementi** (accanto a **Aggiungi conoscenza**) | Elimina rapidamente molti elementi: a ogni elemento del personaggio, anche quelli vecchi, compare una casella di spunta. **Seleziona tutto** / **Deseleziona tutto** spuntano o tolgono tutte le caselle, **Elimina selezionati (n)** rimuove per sempre quelli spuntati dopo una domanda di conferma, **Annulla** esce dalla selezione. |
| **Ha incontrato** | Le persone che il personaggio ha incontrato, annotate dall'[elaborazione](#processing). L'elaborazione può annotare un nome che non è una persona (una nota nel campo dei personaggi presenti): apri **Modifica** e cambia l'elenco **Ha incontrato**, un nome dopo l'altro separati da virgole. I nomi che scrivi restano quando la storia viene riletta; uno che rimuovi torna solo se la parte in cui è stato visto viene riletta. |
| **Conoscenza vecchia (n)** | Chiusa per impostazione predefinita. Ogni elemento vecchio può essere **copiato**, **modificato**, **ripristinato** (torna attivo, da ora in poi) o eliminato. |

<a id="mass-changes"></a>

### Modifiche di massa

Il pulsante **Modifiche di massa** (l'icona a elenco accanto a **Traccia un personaggio**) apre una finestra con ogni personaggio tracciato e
tutti i suoi elementi, anche quelli vecchi, in una griglia con caselle di spunta: il tipo, il testo, lo stato (*Attiva*, oppure vecchia: *ritirata*,
*rifiutata in una revisione*, *riordinata*), la provenienza (*Stato iniziale*, *Scritta a mano*, *Appresa dalla storia*) e la
parte in cui è stata appresa. Spunta elementi in quante griglie vuoi (la casella nell'intestazione di una griglia spunta tutti i suoi elementi, **Seleziona tutto** / **Deseleziona tutto** nella barra in basso spuntano tutti gli elementi di tutti i personaggi), poi:

| | |
|---|---|
| **Ritira selezionati (n)** | Gli elementi attivi spuntati diventano conoscenza vecchia, come fa **Ritira** per uno solo. |
| **Ripristina selezionati (n)** | Gli elementi vecchi spuntati tornano attivi, da ora in poi. |
| **Elimina selezionati (n)** | Rimuove per sempre gli elementi spuntati, dopo una domanda di conferma. |

I numeri sui pulsanti indicano quanti degli elementi spuntati ciascuno modifica. Le spunte restano dopo una modifica, così gli
elementi ritirati possono essere eliminati subito dopo.

**Esporta in JSON** (in alto nella finestra) scarica come file i personaggi tracciati del libro (nomi, alias, elementi con tipo, stato, provenienza e blocco, le persone incontrate); non include nulla dell'elaborazione, quindi funziona con qualsiasi libro.
**Importa da JSON** legge un file del genere nel libro e si limita ad aggiungere: un personaggio il cui nome il libro già traccia (come nome o alias) riceve gli alias, gli elementi e le persone incontrate che non possiede ancora (un elemento con lo stesso tipo e testo viene saltato), gli altri diventano nuovi personaggi tracciati; un alias che un altro personaggio ha già viene escluso. Un elemento importato non ha alcuna parte di questo libro alle spalle, quindi un'elaborazione non lo elimina mai: quello appreso dal modello conta come scritto a mano, quello vecchio resta vecchio. Il messaggio dopo l'importazione indica quanti personaggi sono stati aggiunti o aggiornati e quanti elementi sono stati aggiunti o erano già presenti.

<a id="what-goes-into-the-prompt"></a>

## Che cosa entra nel prompt

Per ogni personaggio tracciato che è tra i personaggi presenti, il prompt riceve un blocco come questo:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Vengono inviati solo gli elementi attivi. L'ultima riga (la riga del **mondo chiuso**) è ciò che impedisce al modello di attribuire al personaggio una conoscenza che non può avere: copre ciò che la storia racconta (persone, luoghi, segreti, piani, eventi), non ciò che una persona di quel mondo sa comunque, quindi un personaggio conosce ancora la propria città. Puoi disattivare la riga nelle impostazioni. Un personaggio tracciato che non è presente non riceve nulla, nemmeno una riga che dica che non sa.

<a id="who-is-present"></a>

### Chi è presente

Il blocco è costruito a partire dal campo **Personaggi presenti** della parte. Il campo è testo libero, quindi viene letto in modo flessibile:

- il testo tra parentesi quadre viene ignorato: `Tomáš (tired), Mizuki` è Tomáš e Mizuki;
- i nomi vengono separati ai **separatori** (predefiniti: `,` `;` un a capo, `/`, `|`, `、` `，` `；`);
- ciò che segue ` - ` o `:` è una nota e viene scartato (`Mara - wounded`);
- maiuscole e minuscole, accenti e un *the* iniziale non contano;
- un nome deve corrispondere **per intero**: *il fratello di Mara* non è *Mara*. Usa gli alias quando un personaggio è scritto in più di un modo. Il punto è che una corrispondenza mancata costa un blocco, mentre una corrispondenza falsa descriverebbe un personaggio che non è nella scena.

Il campo *Punto di vista* non viene letto da solo, quindi scrivi il personaggio che ha il punto di vista anche tra i personaggi presenti.
<a id="processing"></a>

## Elaborazione

Il plugin segue la storia. **Elaborazione** legge le parti del ramo attivo in ordine, a partire dal **punto di inizio**, e
mantiene un **cursore** (l'ultima parte letta). Senza il modello annota chi ha incontrato chi: un personaggio tracciato ha incontrato
ogni nome presente nel campo dei personaggi presenti insieme a lui, e il blocco li elenca. Con il modello trova anche ciò che i
personaggi **hanno appreso** (vedi [Lettura di ciò che i personaggi apprendono](#reading-what-the-characters-learn)).

![Il pannello di elaborazione](../../images/plugin-knowledgetracker-processing.png)

Il pannello in cima alla scheda mostra a che punto si trova. Si ricarica da solo quando una nuova parte diventa definitiva (dopo
l'annotazione automatica e la riscrittura, se il libro le prevede); se aggiungi, modifichi o elimini parti in altro modo, premi il pulsante **Aggiorna** (le frecce accanto all'ingranaggio) per rileggere la storia. Contiene:

- **Elabora ora**: legge le parti che non sono ancora elaborate.
- **Elabora la parte successiva**: legge soltanto la prima parte non ancora elaborata, un passo alla volta (con il modello: una richiesta per quella parte, indipendentemente da quanto indica *Parti per esecuzione*). Usalo per provare le impostazioni o per vedere cosa trova il modello per una singola parte. Il comando `knowledge-process` con `parts` impostato a 1 fa lo stesso.
- **Punto di inizio**: la parte da cui inizia l'elaborazione (*Inizio del libro* per impostazione predefinita). Le parti precedenti non vengono mai lette. Spostarlo indietro rilegge le parti a partire dal nuovo inizio; spostarlo in avanti lascia invariato ciò che è già stato letto.
- **Parti elaborate**: le ultime parti che sono state lette. Una parte il cui testo **è cambiato dopo l'elaborazione** (l'hai modificata o riscritta) è contrassegnata; per questo motivo nulla viene rielaborato da solo. Il pulsante di una riga, **Rielabora da qui**, elimina ciò che deriva da quella parte e dalle successive (gli elementi e le ritrattazioni scritti a mano restano) e le legge di nuovo.

Ciò che il plugin fa da solo, quando **Elabora automaticamente le nuove parti** è attivo (è disattivato per impostazione predefinita):

- Prima che inizi una generazione, vengono elaborate le parti scritte dall'ultima volta, così il prompt sa chi ha incontrato chi. Una parte che viene sostituita da una *Rigenera* o da uno swipe non viene toccata. Con il modello, questa lettura usa il modello solo quando **Chiedi prima di applicare** è disattivato (vedi sotto); altrimenti annota soltanto chi ha incontrato chi, e ciò che trova il modello viene avviato dalla scheda, perché una revisione non ti viene mai imposta nel mezzo di una generazione.
- Quando una parte viene **rigenerata** e arriva il nuovo testo, viene ritrattato ciò che era stato appreso dal vecchio testo.
- Quando fai uno **swipe** o passi a un altro ramo, il cursore torna all'ultima parte condivisa da entrambi i rami.
<a id="reading-what-the-characters-learn"></a>

## Leggere quello che apprendono i personaggi

Spunta **Read what the characters learn with the model** nelle impostazioni. Poi **Process now** chiede al modello, per le parti successive della storia, che cosa ciascun personaggio tracciato presente in esse **ha appreso**: ciò che ha visto, sentito, letto o gli è stato raccontato. Indica anche quali elementi la storia **contraddice** (per esempio *non ha mai incontrato Thomas* dopo che si incontrano). La richiesta contiene le parti (con il loro punto di vista e i personaggi presenti), ciò che ogni personaggio tracciato sa in quel momento e, se lo scegli, il lorebook. Il modello riceve l'istruzione di tralasciare ciò che una persona di quel mondo conosce già (chi è il re) e ciò che discende dal personaggio stesso (una succube sa di avere una coda), e di non riportare nulla di cui non sia sicuro. Un personaggio tracciato che non è presente in nessuna delle parti non viene nemmeno interrogato.

Ogni esecuzione è una sola richiesta al modello. Quando **Process now** richiederebbe più di tre richieste (un lungo tratto di storia non ancora elaborato, oppure **Parts per run** impostato su un valore basso), ti avvisa prima di quante parti e richieste si tratta e di quanto testo passa, più o meno, attraverso il modello, e parte solo dopo la tua conferma. Ogni richiesta porta con sé anche le istruzioni, il lorebook e la conoscenza dei personaggi, quindi il costo reale è più alto del solo testo.

Quando **Ask before applying** è disattivato, le modifiche vengono applicate subito e ciascuna viene scritta nel registro **Applied automatically**, nella sezione Processing della scheda: chi ha ottenuto o perso che cosa, e da quale parte. Il registro non è modificabile, ma ogni riga ha un pulsante **Revert**: un elemento aggiunto viene rifiutato (resta in *Old knowledge*, come un'aggiunta rifiutata nella revisione), un elemento ritirato torna attivo. Una riga il cui elemento è stato modificato, eliminato o ritirato a mano nel frattempo viene lasciata com'è. Il registro conserva gli ultimi 200 cambiamenti.

Con **Ask before applying** disattivato, la nuova parte viene letta non appena è definitiva: quando la generazione è finita oppure, se nel libro sono attive l'annotazione automatica (e riscrittura) o una traduzione, quando anche quelle sono finite. In questo modo il modello legge il testo che tieni e lavora mentre tu leggi. Si tratta di un passaggio di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part). La generazione successiva trova quindi la conoscenza aggiornata e non deve aspettare. Ciò che resta in sospeso (un'esecuzione che hai interrotto, una finestra che hai chiuso) viene letto prima della generazione successiva, come prima.

Mentre il modello lavora, sopra la pagina compare un piccolo **pannello di avanzamento**. Non è **modale**: puoi continuare a leggere e modificare la storia, e puoi **trascinarlo** dal suo titolo. Chiuderlo lo nasconde, ma il lavoro prosegue (**Annulla** lo ferma).

![Il pannello di avanzamento](../../images/plugin-knowledgetracker-progress.png)

Mentre il modello risponde, il pannello mostra **What the model is writing** man mano che arriva: prima il suo ragionamento e poi la risposta, così una lunga attesa con un modello lento non è una barra vuota. Quando la richiesta è conclusa, lo stesso testo resta sotto **What the model thought** (un blocco per ogni richiesta dell'esecuzione, con ciò che ha risposto), anche quando non ha proposto **niente** e non si apre alcuna revisione. La scheda conserva un pulsante **Last run: what the model thought** che riapre il pannello, così puoi vedere perché non è stato aggiunto nulla.

Quando il modello ha risposto, il pannello lo segnala e mostra **Rivedi le modifiche**; lo stesso pulsante si trova nel pannello di elaborazione della scheda (*3 changes to review*). **La revisione non si apre mai da sola.** È una tabella delle modifiche, raggruppate per personaggio:

![Revisione delle modifiche](../../images/plugin-knowledgetracker-review.png)

Sotto la tabella, **What the model thought** (chiuso all'inizio) contiene il ragionamento del modello che precede la sua risposta, quando il modello lo invia: mostra perché è stata proposta una modifica, o perché ne manca una che ti aspettavi, così puoi valutare la modifica prima di applicarla. Un modello che non ragiona non mostra nulla lì.

| | |
|---|---|
| La casella di controllo | Se la modifica viene applicata. All'inizio sono tutte spuntate; ogni personaggio ha **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Un ritiro rende vecchio un elemento (resta conservato, vedi [la scheda](#the-knowledge-tab)). Un'aggiunta è un nuovo elemento, *Knows* oppure *Does not know*. Testo e tipo di un'aggiunta possono essere **modificati** prima che venga applicata. |
| La citazione | Le parole della parte che lo mostrano, e la parte in cui si trovano. Una citazione che non si trova in quella parte viene segnalata: il modello potrebbe averla inventata, controllala prima di accettare. |

**Applica** applica le modifiche spuntate e passa oltre. **Annulla** ferma l'intera serie e non cambia nulla: le stesse parti vengono proposte di nuovo la volta successiva. Un'aggiunta che deselezioni viene **conservata come conoscenza vecchia** insieme alla sua citazione (puoi ripristinarla); un ritiro deselezionato lascia l'elemento com'è. Ciò che il modello ha detto e non si è potuto usare (un personaggio su cui non era stato interrogato, un elemento che non esiste, una modifica senza citazione, qualcosa che il personaggio sa già) viene elencato sotto *Escluso*. Ciò che è stato applicato porta con sé la sua parte, quindi **Reprocess from here** lo riprende. Se la storia è cambiata mentre stavi revisionando (un altro ramo, una parte eliminata), le modifiche non vengono applicate e devi rielaborare.

Le parti vengono lette in **esecuzioni** di **Parts per run** (1 per impostazione predefinita); una parte che non entra nel contesto del modello viene tagliata fino a ciò che ci sta. Una serie continua, esecuzione dopo esecuzione, fino all'ultima parte definitiva; in una serie che ti chiede conferma, un'esecuzione in cui il modello non ha trovato nulla viene saltata senza chiedere. Con **Ask before applying** disattivato, le modifiche vengono applicate subito e il pannello conserva un registro con una riga per ogni esecuzione.

!!!
Il prompt è stato provato con un modello su alcune scene, non in modo esteso: impedisce ai personaggi di apprendere ciò che non hanno percepito, ma controlla ciò che viene proposto, soprattutto per i personaggi che sono in una scena lunga. La richiesta viene inviata al modello che scegli qui sotto, con il costo e la privacy abituali di quel servizio.
!!!

<a id="tidying-the-knowledge"></a>

## Sfoltire le conoscenze

Il modello annota ciò che ogni personaggio apprende, e in una storia lunga l'elenco cresce, con più dettagli minori di quanti ne vorresti inviare al modello a ogni richiesta. **Tidy knowledge** (accanto a **Process now**, richiede l'impostazione del modello) chiede al modello di rileggere ciò che un personaggio sa, con le ultime parti elaborate della storia come contesto, e di

- **eliminare** un elemento che non vale la pena conservare: dettagli trascurabili, un particolare di una scena che tutti i presenti hanno visto (la storia lo conserva comunque), qualcosa superato da eventi successivi, qualcosa che dice la stessa cosa di un altro elemento;
- **unire** più elementi sullo stesso argomento in uno solo che conservi ogni fatto.

Un elemento eliminato o unito non viene cancellato: diventa *Old knowledge* e può essere ripristinato. Le modifiche vengono revisionate come quelle di una lettura (decide **Ask before applying**), oppure applicate subito e scritte nel registro con un pulsante **Revert**.

Lo sfoltimento può toccare solo ciò che il modello ha appreso e che tu non hai **bloccato**. Gli elementi iniziali, quelli che hai aggiunto o modificato a mano, quelli bloccati e quelli vecchi non vengono mai cambiati (i primi due tipi e quelli bloccati vengono mostrati al modello, così può vedere quali elementi farebbero da duplicato). Un segreto che nessuna parte menziona da molto tempo non è un motivo per eliminarlo, e il prompt lo dice esplicitamente; quando il modello ha dei dubbi, conserva l'elemento. Un personaggio con meno di **Tidy: from this many items** (8) elementi appresi non viene sfoltito, e **Tidy: parts of context** (10) indica quante delle ultime parti elaborate vengono lette.

**Tidy automatically** (un'impostazione, disattivata per impostazione predefinita) esegue lo sfoltimento da solo alla fine di un'esecuzione le cui modifiche vengono applicate senza chiedere (**Ask before applying** disattivato, quindi anche la lettura automatica delle nuove parti), non appena il modello ha letto **Tidy after this many parts** (10) parti dall'ultimo sfoltimento. Ciò che cambia viene applicato e scritto nel registro con un pulsante **Revert**, e il registro dell'esecuzione lo indica. Un'esecuzione che revisioni non avvia mai da sola uno sfoltimento, e un personaggio con troppi pochi elementi viene saltato senza interrogare il modello.

Lo stesso si ottiene dalla barra dei comandi: `knowledge-tidy` (`ask true` per avere le modifiche come elenco; la scheda aperta mostra allora il pulsante di revisione), e `lock-knowledge` / `unlock-knowledge` con il personaggio e il numero dell'elemento.
<a id="settings"></a>

## Impostazioni

L'ingranaggio accanto a *Track a character*:

| Impostazione | |
|---|---|
| **In prompt** | Inserisce il blocco nel prompt di ogni generazione. Il pulsante diventa verde mentre è attivo. |
| **Profondità** | Dove va, contato in messaggi dalla fine del prompt: `0` dopo le istruzioni del turno, `1` subito prima di esse. Non finisce mai prima del prompt di sistema. |
| **Ruolo** | Se viene inviato come messaggio *Sistema*, *Assistente* o *Utente*. |
| **Say that nothing else is known** | La riga del mondo chiuso. |
| **Give what the characters know to the annotation** | Attivo (predefinito): [Annota con l'IA](#annotating-with-what-the-characters-know) riceve ciò che i personaggi tracciati della parte sapevano all'inizio della parte. |
| **Process new parts automatically** | Vedi [Elaborazione](#processing). |
| **Read what the characters learn with the model** | L'elaborazione chiede al modello cosa ha appreso ciascun personaggio tracciato e lo propone. Disattivato (predefinito): l'elaborazione registra soltanto chi ha incontrato chi, senza costi. |
| **Ask before applying** | Attivo (predefinito): ogni proposta viene mostrata per la revisione. Disattivato: viene applicata subito e registrata nel log. |
| **Let the model read earlier parts** | Mentre legge le parti nuove, il modello può leggere la parte in cui un elemento è stato appreso (gli elementi riportano "appreso nella parte 12"), per verificare se quanto legge conferma, ripete o contraddice l'elemento: ad esempio, una diceria che ora viene vista con i propri occhi. Al massimo 5 letture di una parte ciascuna per richiesta; il testo di una parte lunga viene tagliato a ciò che entra nel contesto, e la revisione mostra quali parti ha letto. Ogni lettura è un giro in più verso il modello, quindi è disattivato per impostazione predefinita. Un modello che non sa chiamare funzioni legge la richiesta come prima. |
| **Parts per run** | Quante parti legge una richiesta. Un valore più alto costa meno, ma produce una risposta più lunga e una revisione più difficile. |
| **Tidy automatically**, **Tidy after this many parts** | La passata di riordino parte da sola alla fine di un'esecuzione che applica le modifiche senza chiedere, ogni tot parti (10). Disattivato per impostazione predefinita. |
| **Tidy: parts of context** | Quante delle ultime parti elaborate legge la passata di riordino (10). |
| **Tidy: from this many items** | Un personaggio con meno elementi appresi dal modello (e che non sono bloccati) non viene riordinato (8). |
| **Lorebook for the model** | *Entries the parts activate* (predefinito: le voci che una generazione userebbe per queste parti), *The whole lorebook* (ogni voce; quando non entra nel contesto, vengono inviate quelle attivate e la nota lo segnala) oppure *No lorebook*. |
| **Model / Protocol for the extraction** | Vuoto: il modello del libro. Per questo basta un modello più economico, e su un server che mantiene una sola cache dei prompt un modello separato evita che venga sostituito. |
| **Separators of this book** | Come il campo dei personaggi presenti viene diviso in nomi, uno per riga (un a capo è `\n`). Una parola come `and` divide tra spazi, quindi *Jack and Jill* resta un unico nome finché non lo aggiungi. |
| **My separators (all books)** | Lo stesso, per tutti i tuoi libri. I separatori del libro hanno la precedenza; se non ce ne sono, si usano quelli integrati. |

<a id="prompts"></a>

## Prompt

Il messaggio di sistema della lettura (**Knowledge extraction prompt**) e quello della passata di riordino (**Knowledge tidy prompt**) sono
[impostazioni ereditate](../books/prompts.md#inherited-settings): un libro usa le proprie, altrimenti la cartella più vicina sopra di esso che ne imposta una,
altrimenti le tue, altrimenti il testo integrato, che è stato testato con un modello reale.

Impostali nella scheda *Prompt* del libro, sotto *Extension Settings*, nella finestra di una cartella, oppure per tutti i tuoi libri in
*Settings → Extension Settings → Knowledge Tracker* (vuoto mostra il testo integrato in grigio). Mantieni il formato di risposta JSON del
testo integrato: il plugin legge la risposta in base ad esso. Le istruzioni per `read_part` vengono aggiunte dal plugin quando il modello può leggere
le parti precedenti, quindi il tuo prompt non ne ha bisogno.

<a id="annotating-with-what-the-characters-know"></a>

## Annotare con ciò che sanno i personaggi

Il blocco qui sopra impedisce al modello di rendere un personaggio onnisciente *mentre scrive*. Le stesse registrazioni aiutano anche a trovare
le sviste che sono comunque passate. Quando un modello [annota una parte](../books/annotations.md#letting-a-model-annotate) (**Annota con l'IA**, un intervallo di parti, l'annotazione
automatica, `/generate-annotation`), il tracciatore gli dice cosa sapevano i personaggi tracciati di quella parte **all'inizio della parte**,
e l'editor segnala un personaggio che dice, pensa o fa ciò che non può sapere: *Fiora saluta Thomas per nome; le registrazioni dicono che non l'ha
mai incontrato.* La nota indica il fatto.

- **I fatti sono quelli della parte, non quelli della fine del libro.** Ciò che un personaggio ha appreso in una parte successiva non è ancora
  noto; un elemento che una parte successiva ritira era ancora vero. Ciò che la parte stessa porta (ciò che viene appreso, chi viene incontrato, ciò che
  smette di essere vero al suo interno) è elencato a parte, così la prima metà della parte non viene verificata con la seconda.
- **Quali personaggi.** Quelli tracciati che sono nel campo *Personaggi presenti* della parte, sono il suo punto di vista, oppure sono nominati
  nel suo testo (parola intera, qualsiasi maiuscola o minuscola, anche gli alias).
- **Gli elementi che non sono fatti in nessun momento** sono esclusi: quelli che hai ritirato a mano, quelli che la passata di riordino ha eliminato,
  quelli che hai rifiutato in una revisione.
- **Solo ciò che è registrato.** Al modello viene detto che ciò che le registrazioni non dicono non è un fatto, così non segnala un personaggio per una
  cosa che il tracciatore semplicemente non ha mai saputo. **Say that nothing else is known** aggiunge anche "(nessun altro)" a chi un personaggio ha incontrato.
- Se una parte precedente a quella revisionata non è stata letta dal tracciatore, i fatti lo dicono: ciò che i personaggi vi hanno appreso manca, e
  **Elabora ora** prima offre una revisione migliore.
- I fatti consumano token nella richiesta di annotazione, solo per le parti che hanno un personaggio tracciato. Disattivali con **Give what
  the characters know to the annotation**.

<a id="the-block-as-a-macro"></a>

## Il blocco come macro

Invece di (o oltre a) l'inserimento automatico puoi collocare il blocco tu stesso con la
[macro {%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). L'argomento è l'elenco dei
personaggi della scena, così un template può passare il campo della parte:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

La macro stampa il blocco indipendentemente dal fatto che **In prompt** sia attivo, quindi disattivalo quando usi la macro, altrimenti il modello riceve
il blocco due volte. Profondità e ruolo non si applicano alla macro (il blocco fa parte del messaggio che la contiene). Non stampa nulla quando
nessuno dei personaggi elencati è tracciato. La risposta di una macro di comando viene conservata con la parte da cui continua la nuova parte,
quindi una seconda generazione dalla stessa parte non vede una modifica fatta nel frattempo; una rigenerazione o uno swipe la richiedono di nuovo.

<a id="commands"></a>

## Comandi

Per la [riga di comando](../commands/index.md) e per gli agenti:

| Comando | |
|---|---|
| `knowledge-status` | Le impostazioni e a che punto è l'elaborazione. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | I personaggi tracciati, indicati per nome, alias o numero. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Gli elementi di un personaggio, indicati con il loro numero in `list-knowledge`. |
| `knowledge-process` | Elabora ora (`parts`: al massimo quel numero di parti). Con il modello attivo, `ask true` restituisce ciò che il modello ha trovato come modifiche numerate e le trattiene: per ora non cambia nulla. La scheda Knowledge aperta mostra un pulsante **changes found by a command** che apre la solita revisione (Apply / Cancel); un agente le applica con `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Applica le modifiche trattenute, tutte o quelle con i numeri indicati (come **Apply** nella revisione della scheda). |
| `knowledge-tidy` | La passata di riordino. Con `ask true` restituisce le modifiche come elenco numerato e le trattiene (la scheda mostra un pulsante per revisionarle; `apply-knowledge-changes` le applica). |
| `lock-knowledge`, `unlock-knowledge` | Bloccano un elemento (personaggio, numero dell'elemento) perché la passata di riordino non lo tocchi, oppure lo sbloccano. |
| `knowledge-reprocess` | Rielabora a partire da una parte (il suo identificativo o la sua posizione); chiede conferma prima. Con il modello attivo riporta solo lo stato indietro, `knowledge-process` rilegge le parti. |
| `knowledge-set-start` | Imposta il punto di partenza; senza una parte, l'inizio del libro. |
| `knowledge-tracker-block` | Il blocco come testo, per la macro. |

La riga di comando divide gli argomenti agli spazi: racchiudi tra virgolette un elenco di personaggi (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Archiviazione

Tutto viene salvato insieme al libro (nei suoi attributi), quindi fa parte dei [backup del libro](../books/backups.md) e delle copie.
