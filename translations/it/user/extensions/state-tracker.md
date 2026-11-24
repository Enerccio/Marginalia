---
label: Tracciatore di stato
order: 29
---

# Tracciatore di stato

Tiene traccia dello **stato** attuale dei tuoi personaggi: cosa indossano, cosa portano, l'arma in mano, le ferite, il loro umore o qualsiasi altra cosa tu definisca. Un modello dimentica facilmente che la spada è stata persa due capitoli fa o che il mantello è fradicio. Con il Tracciatore di stato, il prompt indica per ogni personaggio tracciato presente nella scena cosa è vero di lui in questo momento.

!!!
Sei tu a scegliere chi viene tracciato e cosa si traccia di lui. Al modello vengono descritti solo i personaggi che aggiungi alla scheda, e solo mentre sono tra i *personaggi presenti* della parte che si sta scrivendo. Puoi mantenere lo stato da solo, oppure lasciare che un modello legga la storia e proponga le modifiche (vedi [Seguire la storia](#following-the-story)).
!!!

<a id="categories"></a>

## Categorie

Una **categoria** è una cosa che può essere tracciata di un personaggio. Ogni categoria ha:

- un **nome**, che è anche come il prompt la chiama (*Wearing*, *Weapon at hand*, *Carrying*);
- un **tipo**:
  - **Single value**: un unico valore breve, e un nuovo valore sostituisce il precedente (l'arma in mano, la posizione);
  - **List**: voci che vengono aggiunte e rimosse una alla volta (cosa si porta, le ferite);
  - **Description**: un unico testo che viene riscritto per intero (i vestiti, l'umore).
- un'**istruzione per il modello**: cosa appartiene alla categoria e cosa no. Il modello la legge quando segue la storia.
- **Only what the text says**: attivo, il valore cambia solo quando il testo lo dice; disattivo, cambia anche per conseguenze ovvie (bagnato dopo un bagno).
- **Say "none" when empty**: il prompt scrive *none* quando il personaggio non ha un valore (un'arma perduta), invece di omettere la categoria.

<a id="my-categories"></a>

### Le mie categorie

Le tue categorie personali si trovano in **Settings → Extension Settings → State Tracker**. I libri copiano le proprie categorie da qui. Finché non le cambi, hai cinque categorie predefinite: *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* e *Mood*. Aggiungi, modifica ed elimina categorie in quella schermata e premi **Save** nella schermata Settings. **Reset to the built-in categories** ripristina le cinque predefinite.

<a id="the-categories-of-a-book"></a>

### Le categorie di un libro

Un libro mantiene una **copia propria** di ogni categoria che usa, così un backup o un'esportazione del libro contiene tutto, e eliminare una delle tue categorie non rompe mai un libro. Il pulsante **Categories of this book** nella scheda State elenca queste categorie:

- **Add from my categories** copia una delle tue categorie nel libro;
- **New category for this book** crea una categoria valida solo in questo libro (il carico di una nave, un'affinità magica). Tale categoria è contrassegnata come *Only in this book*. **Save to my categories** la aggiunge alle tue categorie, così possono usarla anche altri libri.
- Una copia che non è più uguale alla tua categoria è contrassegnata come *Differs from your category*. **Update from my categories** adotta la tua versione. Nulla viene aggiornato senza di te, perché un'istruzione modificata cambia ciò che fa il modello.
- Modificare una categoria la cambia solo in questo libro. Il suo tipo non può cambiare finché i personaggi hanno valori per essa.
- Eliminare una categoria dal libro elimina i valori di ogni personaggio in essa.

<a id="the-state-tab"></a>

## La scheda State

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) mostra una scheda **State**. Tutto viene salvato nel libro man mano che lo modifichi.

![La scheda State con un personaggio tracciato](../../images/plugin-statetracker.png)

![La cronologia di un valore](../../images/plugin-statetracker-history.png)

**Track a character** chiede un **name**, facoltativamente **aliases** (gli altri nomi con cui il personaggio è scritto nel campo *Present Characters* di una parte, separati da virgole) e le **tracked categories**. L'elenco propone le categorie del libro e le tue; sceglierne una delle tue la copia nel libro. Un cavallo può tracciare solo *Injuries*, mentre l'eroina traccia tutto.

**Import from the Character Tracker** (l'icona della persona accanto a **Track a character**) inizia a tracciare i personaggi del [Character Tracker](character-tracker.md) dello stesso libro. Elenca i personaggi che il libro non traccia ancora (come nome o alias), con le loro tagline, tutti selezionati. Deseleziona quelli da escludere, spunta le **tracked categories** che ogni personaggio importato riceve (quelle del libro e le tue, come sopra) e premi **Import**. Vengono presi solo i nomi; un personaggio può ricevere altre categorie in seguito con la matita.

Ogni personaggio tracciato ha una scheda, chiusa all'inizio: aprila con la freccia sulla scheda per vedere le sue categorie, ciascuna con il suo valore o i suoi valori attuali:

- **+** imposta il valore di una categoria a valore singolo o descrittiva (il valore attuale passa nella cronologia) oppure aggiunge una voce a una lista.
- La matita modifica un valore, la croce lo **rimuove**: non è più vero, quindi passa nella cronologia e non viene inviato al modello.
- Il lucchetto contrassegna un valore come **locked** (una cicatrice, la spada di famiglia). Il [riordino](#tidying-the-state) lascia in pace i valori bloccati; la storia può comunque cambiarli.
- **History** mostra i valori passati della categoria con il motivo della fine (*replaced*, *removed* o *tidied*) e, quando è stato il modello a concluderli, la parte che ha letto per capirlo. **Restore** rende di nuovo attuale un valore passato (per una categoria a valore singolo o descrittiva, quella attuale passa nella cronologia al suo posto). **Delete** elimina un valore in modo definitivo.

La matita del personaggio modifica il suo nome, gli alias e le categorie. Deselezionare una categoria la nasconde ma ne conserva i valori; selezionarla di nuovo li fa riapparire. Il cestino smette di tracciare il personaggio ed elimina tutto il suo stato.
<a id="following-the-story"></a>

## Seguire la storia

Il pannello **Elaborazione** in cima alla scheda Stato permette a un modello di leggere la storia e capire cosa è cambiato: un mantello inzuppato nel fiume, una lanterna passata a qualcun altro, una spada sguainata, una lettera perduta. Legge le parti del ramo attivo in ordine, a partire dal **Punto di inizio** (per impostazione predefinita l'inizio del libro; le parti precedenti non vengono mai lette), e ricorda fino a dove è arrivato.

![Le impostazioni dell'elaborazione](../../images/plugin-statetracker-settings.png)

![La revisione delle modifiche proposte](../../images/plugin-statetracker-review.png)

- **Elabora ora** legge le parti non ancora elaborate, **Elabora la prossima parte** solo la successiva. Una serie lunga chiede prima quante richieste serviranno. Quando scorri l'elenco dei personaggi oltre questi pulsanti, **Elabora ora**, **Elabora la prossima parte** e **Riordina lo stato** sono disponibili anche nella barra in cima alla scheda.
- Il modello riceve i personaggi tracciati presenti nelle parti, con le loro categorie, l'istruzione di ciascuna e i valori correnti, e risponde con le modifiche. Ogni modifica richiede una citazione dalla parte. Le modifiche che non sono coerenti (una categoria che il personaggio non traccia, un valore che ha già, una citazione da una parte che non esiste) vengono scartate.
- Un pannello mostra l'avanzamento e ciò che il modello scrive mentre scrive; puoi continuare a leggere e a scrivere. Con **Chiedi prima di applicare** attivo (impostazione predefinita) l'esecuzione attende la tua decisione: **Rivedi le modifiche** mostra le modifiche per personaggio, con il valore finale barrato e il nuovo valore, che puoi correggere, e la citazione. Deseleziona ciò che non va bene e premi **Applica**; **Annulla** lascia tutto com'era, e le stesse parti vengono proposte di nuovo la volta successiva.
- Un valore letto dal modello mostra la parte da cui proviene. **Parti elaborate** elenca le parti lette; **Rielabora da qui** annulla tutto ciò che è stato letto da quella parte in poi (i valori che hai scritto o corretto restano) e li rilegge. Usala dopo aver riscritto una parte precedente. Una parte rigenerata, uno swipe o un altro ramo annullano da soli ciò che proveniva da parti non più presenti.

<a id="the-story-clock"></a>

### L'orologio della storia

Con **Orologio della storia** attivo (nelle impostazioni, disattivato per impostazione predefinita) il modello segue anche il tempo della storia. Nella stessa richiesta dello stato, riporta per ogni parte ciò che quella parte dice sul tempo: *la mattina dopo*, *tre giorni dopo*, *quella notte*, *il quarantesimo giorno dal tuo risveglio*, *il venti aprile*, un generico *le settimane si sono confuse* (contrassegnato come approssimativo), oppure un flashback, un ricordo o un sogno del passato, che non fa avanzare l'orologio e non cambia nulla nello stato. Il modello non conta: è il plugin a sommare i giorni.

![L'orologio della storia nelle impostazioni dell'elaborazione](../../images/plugin-statetracker-clock.png)

![Correggere a mano il tempo di una parte](../../images/plugin-statetracker-clock-dialog.png)

- **Il giorno 1 è il giorno di** indica l'evento da cui si contano i giorni (*il risveglio*): l'ora risulta *Giorno 14 dopo il risveglio, sera*. **Giorno al punto di inizio**, **Momento della giornata** e **Data al punto di inizio** danno il tempo prima della prima parte letta; con una data l'orologio conta anche le date (*20 aprile 1453*).
- Il pannello Elaborazione mostra ora il **tempo della storia**, e ogni parte elaborata mostra il tempo che segue. Un giorno o una data dichiarati sostituiscono una stima precedente.
- Il pulsante orologio di una parte (o della riga del tempo della storia, per l'ultima parte) **imposta il tempo a mano**: un punto fisso (giorno, momento della giornata, data) che rileggere la parte non sostituisce e da cui l'orologio riprende a contare. **Rimuovi il punto fisso**, nella stessa finestra, lascia che la lettura del modello torni a contare.
- Nella revisione il tempo compare per primo, sotto **Tempo della storia**, con la citazione di ogni parte; deseleziona ciò che non va bene.
- Il blocco del prompt inizia con il tempo (*Tempo della storia: giorno 14 dopo il risveglio, sera.*), anche quando nella scena non c'è nessun personaggio tracciato.
- Un valore ricorda il giorno della storia in cui è stato impostato (*giorno 4*). Il riordino vede quanto tempo è passato (*una tazza di tè, impostata 9 giorni fa*) e scarta ciò che ha avuto il tempo di sbiadire.
- Con l'orologio attivo, ogni parte viene inviata al modello, anche quelle senza un personaggio tracciato, perché qualsiasi parte può far avanzare il tempo.
- Una rielaborazione, una rigenerazione o un altro ramo annullano ciò che è stato letto dalle parti scomparse, come lo stato; i punti fissi che hai impostato restano.

<a id="the-log-of-automatic-changes"></a>

### Il registro delle modifiche automatiche

Ciò che viene applicato senza revisione (un'esecuzione con **Chiedi prima di applicare** disattivato, il riordino automatico, un comando senza `ask`) viene scritto in **Applicate automaticamente** nel pannello Elaborazione: chi, quale categoria, da quale parte (oppure *riordinato*) e cosa è cambiato (*+ lanterna*, *− corda*, *spada corta → ascia*). Il registro non è modificabile, ma ogni riga ha un pulsante **Annulla**. Funziona come se avessi deselezionato la modifica nella revisione: il valore introdotto dalla modifica viene eliminato e torna attuale il valore che la modifica aveva sostituito. Una riga non viene toccata se nel frattempo hai cambiato il valore a mano (modificato, rimosso, ripristinato o eliminato, oppure dato un altro valore a una singola categoria). Il registro conserva le ultime 200 modifiche.

![Il registro delle modifiche automatiche](../../images/plugin-statetracker-log.png)

<a id="returning-to-an-earlier-point"></a>

### Tornare a un punto precedente

Prima di ogni esecuzione che applica modifiche (un'esecuzione della scheda con o senza revisione, la lettura automatica di nuove parti, i comandi, un riordino) il tracciatore crea un **punto di controllo**: l'intero stato dei suoi personaggi così come lo trovava l'esecuzione, con l'orologio della storia e il punto fino a cui aveva letto (*Inizio del libro* prima della prima esecuzione). **Punti di controllo** nel pannello Elaborazione li elenca, dal più recente: la parte fino a cui il tracciatore era arrivato, il tempo, il tipo di esecuzione successiva e quante modifiche ha applicato. Vengono elencati solo i punti di controllo la cui parte è nel ramo attivo.

![Una modifica annullata](../../images/plugin-statetracker-reverted.png)

**Torna a questo punto** su una riga annulla quella esecuzione e tutte quelle successive: i valori dei personaggi (correnti e passati), i segni dell'orologio della storia, le parti elaborate e il registro delle modifiche automatiche tornano a quelli del punto di controllo, e la prossima esecuzione rilegge le parti dopo la sua parte, comprese quelle dell'esecuzione annullata. Ciò che il tracciatore aveva prima viene conservato come punto di controllo (*prima di un ritorno a un punto di controllo*), così puoi tornarci. Le tue impostazioni, il punto di inizio, le impostazioni dell'orologio e le categorie del libro restano come sono ora (una categoria eliminata nel frattempo ritorna quando i valori del punto di controllo la usano). Chiede conferma prima, perché tutto ciò che è stato letto dopo il punto di controllo viene annullato, comprese le modifiche che hai fatto a mano da allora. Usalo quando una serie di esecuzioni è andata storta, o dopo aver ripristinato un backup vecchio del libro e voler far recuperare il tracciatore da un punto di cui ti fidi.

Il tracciatore conserva gli ultimi 20 punti di controllo di un libro. Sono conservati tra i tuoi file (*Risorse*, raggruppati per libro), non nel libro, quindi i backup e le copie del libro non li includono.

<a id="tidying-the-state"></a>

### Riordinare lo stato

Alcuni stati sbiadiscono senza che la storia lo dica: un umore, vestiti bagnati, il fiato corto, una tazza tenuta in mano molto tempo dopo il pasto. **Riordina lo stato** nel pannello Elaborazione chiede al modello di valutarli. Legge le ultime parti elaborate con i valori correnti dei personaggi tracciati presenti in quelle parti, e per ciascun valore può:

![La revisione di un riordino](../../images/plugin-statetracker-tidy-review.png)

- **eliminarlo**: non è più vero o non vale più la pena conservarlo;
- **aggiornarlo**: le parti mostrano che è cambiato (la rabbia si è trasformata in sollievo);
- **unire** voci di elenco che sono la stessa cosa (*corda* e *matassa di corda*).

I valori che non menziona restano come sono, così come i valori bloccati. Ogni modifica è accompagnata da un motivo. Un valore scritto dal riordino (un aggiornamento o una fusione) viene contrassegnato come *dal riordino* con l'ultima parte letta, perché la storia non lo diceva esplicitamente; modificarlo lo rende tuo. Lo rivedi come qualsiasi altra esecuzione (oppure viene applicato subito, se **Chiedi prima di applicare** è disattivato). Un valore eliminato dal riordino passa nello storico come *riordinato*, e **Rielabora da qui** sull'ultima parte letta prima del riordino lo annulla. I personaggi che non sono nelle ultime parti restano intatti, quindi una corda portata da qualcuno assente da venti capitoli resta.

Con **Riordina automaticamente** attivo (impostazione predefinita), il riordino parte da solo alla fine di un'esecuzione che applica le sue modifiche senza chiedere, una volta che sono state lette **Riordina dopo queste parti** parti (10 per impostazione predefinita) dall'ultimo riordino. Un'esecuzione che rivedi non avvia mai un riordino, così non ti viene imposta una seconda revisione.

Nelle impostazioni (l'ingranaggio):

- **Elabora automaticamente le nuove parti**: quando una parte è definitiva, il modello la legge (al più tardi prima della generazione successiva). Con **Chiedi prima di applicare** attivo, viene letta solo quando premi **Elabora ora**: una revisione non ti viene mai imposta nel mezzo della scrittura.
- **Chiedi prima di applicare**: disattivato, le modifiche vengono applicate subito e scritte nel registro (vedi sotto).
- **Parti per esecuzione**: quante parti legge una richiesta.
- **Riordina automaticamente** e **Riordina dopo queste parti**: vedi [Riordinare lo stato](#tidying-the-state). Il riordino legge anche quel numero di ultime parti elaborate.
- **Lorebook per il modello**: le voci attivate dalle parti (predefinito), l'intero lorebook, oppure nessuno.
- **Modello per l'estrazione** e **Protocollo per l'estrazione**: vuoto significa il modello del libro.
- **Usa queste impostazioni per i nuovi libri** salva le impostazioni di questo libro (modello e protocollo compresi; i separatori restano con il libro) come quelle con cui parte un libro in cui il Tracciatore di stato non è mai stato usato. **Dimentica le impostazioni per i nuovi libri** torna alle impostazioni integrate. I libri che usano già il tracciatore mantengono le proprie impostazioni.
<a id="prompts"></a>

## Prompt

Il prompt di estrazione dello stato (**State extraction prompt**) e quello di riordino (**State tidy prompt**) sono [impostazioni ereditate](../books/prompts.md#inherited-settings): un libro usa i propri, altrimenti la cartella più vicina sopra di esso che ne imposta uno, altrimenti i tuoi, altrimenti il testo predefinito nella tua lingua per i prompt (quello inglese è stato testato con un modello reale).

Impostali nella scheda *Prompts* del libro, sotto *Extension Settings*, nella finestra di una cartella, oppure per tutti i tuoi libri in *Settings → Extension Settings → State Tracker* (se vuoto, mostra in grigio il testo predefinito). Mantieni il formato di risposta JSON del testo predefinito: il plugin legge la risposta in base a quello. Le regole dell'[orologio della storia](#the-story-clock) vengono aggiunte dal plugin dopo il prompt di estrazione quando il libro segue il tempo, quindi il tuo prompt non ha bisogno di quelle regole.

<a id="in-the-prompt"></a>

## Nel prompt

Il pulsante con l'ingranaggio contiene le impostazioni del libro:

- **In prompt** inserisce lo stato dei personaggi tracciati della scena nel prompt di ogni generazione (l'ingranaggio diventa verde quando è attivo).
- **Depth** e **Role** indicano dove va, come per la Nota dell'autore: *Depth* è il numero di messaggi dopo di essa (0 dopo le istruzioni del turno, 1 subito prima di esse).
- **Name separators** indicano come viene diviso il campo *Present Characters* (per impostazione predefinita `,` `;` `/` `|` e a capo). Le note tra parentesi vengono ignorate, quindi `Mara (asleep), Fiora` trova entrambi.

Il prompt riceve i personaggi della scena che hanno qualcosa da dire:

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Puoi inserire lo stesso testo in qualsiasi punto di un modello con la macro
{%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} (qualsiasi elenco di nomi funziona come argomento). La macro stampa lo stato che **In prompt** sia attivo o no, quindi disattivalo quando usi la macro, altrimenti il modello riceve lo stato due volte.

<a id="commands"></a>

## Comandi

Per la barra dei comandi e per gli agenti:

| Comando | Cosa fa |
|---|---|
| `state-status` | Le impostazioni, le categorie del libro e quelle tue di cui il libro non ha una copia. |
| `list-state-characters` | I personaggi tracciati con le loro categorie. |
| `track-state-character` | Inizia a tracciare un personaggio: nome, alias, categorie per nome. |
| `stop-tracking-state-character` | Smette di tracciare un personaggio ed elimina il suo stato (chiede conferma). |
| `list-character-state` | I valori di un personaggio, attuali e passati, numerati. |
| `set-character-state` | Imposta un valore: personaggio, categoria, testo. |
| `remove-character-state` | Rimuove un valore tramite il suo numero: finisce nella cronologia. |
| `state-tracker-block` | Il testo del prompt per un elenco di personaggi (la macro). |
| `state-process` | Lascia che il modello legga le parti successive e applica le modifiche; con `ask true` le restituisce numerate. |
| `apply-state-changes` | Applica le modifiche restituite `state-process` o `state-tidy`, tutte o i numeri indicati. |
| `set-story-time` | Imposta a mano il tempo della storia dopo una parte elaborata (giorno, momento della giornata, data); senza nessuno di questi rimuove il punto fisso. |
| `state-tidy` | Lascia che il modello elimini, aggiorni o unisca i valori che sono sbiaditi; con `ask true` restituisce le modifiche numerate. |
| `state-reprocess` | Annulla ciò che è stato letto da una parte e dalle successive (chiede conferma). |
| `state-set-start` | Imposta la parte da cui inizia la lettura. |

---
