---
label: Convertitore
order: 16
---

# Convertitore

!!!
Questa estensione si chiamava *SillyTavern Converter* prima della 1.6.0. È cambiato solo il nome; i suoi file, i dati salvati con i tuoi libri e le disposizioni salvate sono gli stessi.
!!!

Riscrivi una chat di gioco di ruolo di [SillyTavern](https://github.com/SillyTavern/SillyTavern) come storia, turno per turno: carichi la
chat (o una conversazione di [LM Studio](https://lmstudio.ai)) in un libro, lasci che il modello la suddivida in *dettagli del turno* usando la chat e il lore del libro, e inserisci ogni turno
nel nuovo modulo di turno della storia con un clic.

La fonte non deve per forza essere una chat: il convertitore può anche prendere le parti di un altro tuo libro (da un file di backup o
dal libro stesso) e darti le loro istruzioni una alla volta, per [riscrivere un libro](#redrafting-a-book).

Per creare un libro direttamente da una chat (ogni messaggio, oppure ogni coppia utente e assistente, a parte), vedi
[Importare da SillyTavern](../books/managing-books.md#importing-from-sillytavern). Il convertitore serve per l'altro caso: tieni la
chat accanto a te e scrivi di nuovo la storia, con il modello, a partire da istruzioni ricavate dalla chat.

!!!
Il Convertitore funziona anche nel [layout per telefono](../phone.md#extensions), vedi [Sul telefono](#on-a-phone).
!!!

<a id="the-converter-tab"></a>

## La scheda Convertitore

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) mostra una scheda **Converter** (testo piccolo, e la scheda scorre
quando la barra è corta). Sopra le sue due liste c'è la sezione **Convert to turn details**, chiusa finché non la apri, così che
le liste abbiano spazio. Contiene:

| | |
|---|---|
| **Modello** e **Protocollo** | Con cosa funziona la bacchetta. Partono dal modello e dal protocollo del libro; la scelta viene conservata con il libro. |
| **Da**, **A** | L'intervallo di messaggi su cui lavora la bacchetta. Scrivi i numeri, clicca nella chat, oppure usa **Tutte**. |
| Conteggio dei token | Quanti token richiede la richiesta: i messaggi dell'intervallo più il prompt e il lore (contati una volta sola). Usalo per tenere l'intervallo nel contesto del modello. |
| **Convert range** | La bacchetta: chiede al modello i dettagli del turno per l'intervallo. Diventa **Interrompi** mentre il modello lavora. |
| Pulsante a penna | Modifica il prompt della bacchetta (variabili `{{backgroundLore}}`, `{{chat}}`, `{{firstMessage}}`, `{{lastMessage}}`, `{{previousTurn}}`), con un pulsante **Default**. |
| **Allinea ai turni** | Sposta l'intervallo ai confini dei turni, vedi sotto. Conservato con il libro. |
| **Auto mode** | Mostra i controlli che convertono l'intero intervallo a blocchi, vedi sotto. |

<a id="align-to-turns"></a>

### Allinea ai turni

Un turno della storia sono i messaggi dell'utente (le istruzioni) seguiti dal messaggio del personaggio. Con **Allinea ai turni** attivo, ogni intervallo che scegli (cliccando, scrivendo i numeri o usando **Tutte**) viene spostato ai confini dei turni:
l'inizio torna al primo messaggio del suo turno, e la fine torna all'ultimo messaggio del personaggio che lo precede, così
l'intervallo non finisce mai su un messaggio dell'utente che appartiene al turno successivo. La fine della chat resta dov'è. Un
intervallo che sta tutto nei messaggi dell'utente prima di una risposta viene esteso in avanti fino a quella risposta.

<a id="auto-mode"></a>

### Modalità automatica

**Auto mode** mostra una riga con **Messages per batch**, il pulsante **Convert in batches** e quante richieste servono,
con i token totali. Il pulsante divide l'intervallo corrente in blocchi di quel numero di messaggi e li invia al modello uno dopo l'altro (dopo aver chiesto conferma), come fa la bacchetta per un intervallo. Con **Allinea ai turni** ogni blocco finisce alla fine di un turno (l'ultimo messaggio del personaggio entro la dimensione, oppure la fine del turno quando è più lungo della dimensione), così nessun turno viene tagliato in due; senza, i blocchi sono tagliati esattamente sul numero, e un turno al confine può essere diviso in due.

Durante l'esecuzione la chat mostra il blocco a cui si trova, la riga di stato indica quale blocco di quanti, e il pulsante diventa
**Interrompi**. I dettagli del turno di ogni blocco vengono salvati appena è pronto, come con la bacchetta (i turni che toccano l'intervallo e che non hai spuntato vengono sostituiti). Quando tutti i blocchi sono completati l'intervallo torna com'era e si apre la scheda **Turn details**. Un blocco la cui risposta non è utilizzabile (vuota, o non corrispondente a quanto richiesto) viene inviato di nuovo, fino a tre volte in tutto. Quando interrompi, o un blocco continua a fallire, la notifica spiega il motivo e l'intervallo resta com'è rimasto, così puoi ripartire da lì.

<a id="what-a-range-continues-from"></a>

### Da cosa continua un intervallo

Il prompt dice al modello i dettagli dell'ultimo turno che finisce prima dell'intervallo (ambientazione della scena, personaggio POV, personaggi presenti e
istruzioni, in `{{previousTurn}}`), così i turni dell'intervallo mantengono lo stesso punto di vista e la stessa scena finché la chat non li cambia. È così che un blocco della modalità automatica continua da quello precedente: i dettagli del turno di
un blocco vengono salvati prima che venga inviato il successivo. Quando non c'è un turno prima dell'intervallo, quella parte del prompt viene omessa.

<a id="the-chat"></a>

## La chat

Carica il file `.jsonl` di una chat (in SillyTavern si trova in `data/<user>/chats/<character>/`, oppure esportalo dalla
chat), oppure una conversazione di LM Studio (`<number>.conversation.json` nella cartella `.lmstudio/conversations` della tua cartella home). I
due tipi si distinguono dal contenuto del file. La chat non deve avere rami: di ogni messaggio viene letta la variante che era mostrata (in LM Studio la versione
mostrata), e i messaggi di sistema nascosti e quelli vuoti vengono esclusi; il prompt di sistema di una conversazione di LM Studio, il suo ragionamento
e le chiamate agli strumenti non sono messaggi della chat. Se la tua chat ha dei rami, importa prima quello che vuoi riscrivere (vedi
[Importare da SillyTavern](../books/managing-books.md#importing-from-sillytavern) per unire i rami in un albero).

La scheda **Chat** è una tabella dei messaggi: una casella di controllo, il numero e il messaggio, con il nome di chi ha parlato in grassetto sopra il
testo (i messaggi dell'utente sono contrassegnati con `»`).

- Spunta la casella di un messaggio che hai già esaminato, così vedi cosa resta. Il pulsante con la doppia freccia
  sopra la tabella scorre al messaggio successivo non spuntato (dopo quello che hai spuntato o cliccato per ultimo, ripartendo
  dall'alto alla fine).
- **Clicca** un messaggio per farne l'intervallo, **Shift+clic** su un altro per estendere l'intervallo fino a esso. L'intervallo è
  evidenziato e il conteggio dei token segue.
- Il pulsante con la croce sopra la tabella rimuove la chat e i dettagli del turno dal libro (dopo conferma).

![La scheda Convertitore con una chat e l'intervallo scelto](../../images/plugin-stconverter-chat.png)

<a id="turn-details"></a>

## Dettagli del turno

Usa la bacchetta e si apre la scheda **Turn details** con quanto ha preparato il modello, nell'ordine della chat. Una riga è un turno:
i messaggi da cui è composto e, in un unico elemento, l'ambientazione della scena, il personaggio POV, i personaggi presenti e le
istruzioni, ciascuno sotto una riga in grassetto con il suo nome.
Il modello riceve l'indicazione di coprire ogni messaggio dell'intervallo esattamente una volta; i numeri fuori dall'intervallo vengono riportati dentro di esso.

- **Clicca una riga** per aprire il modulo del *nuovo turno* della storia e copiarvi tutti e quattro i campi del turno. Modifica ciò
  che vuoi e genera. (La casella e il pulsante di eliminazione di una riga non aprono il modulo.)
- Spunta la casella di un turno che hai usato; la doppia freccia va al turno successivo non spuntato.
- Il pulsante di unione di un turno **lo unisce con il successivo**: si apre un modulo con i dettagli di entrambi già uniti (le ambientazioni
  e le istruzioni una sotto l'altra, i personaggi presenti di entrambi, il personaggio POV del primo; quando il secondo turno ne ha un altro, il modulo lo segnala). Modifica ciò che non va bene e premi **Unisci**
  (oppure <kbd>Ctrl/Cmd+Enter</kbd>): le due righe diventano una sola che copre i messaggi di entrambe, spuntata solo se lo erano entrambe.
- Le frecce di un turno lo **spostano su o giù** nella lista, per scrivere i turni in un ordine diverso da quello della chat (un turno
  mantiene i messaggi che copre). I turni ricavati dalle parti di un libro non si possono spostare: il loro ordine è quello delle parti.
- Rieseguire la bacchetta per un intervallo **sostituisce** i turni precedenti che toccano l'intervallo, tranne quelli spuntati; i nuovi
  turni vanno al loro posto nell'ordine della chat e l'ordine che hai dato spostando i turni viene mantenuto.

![I dettagli del turno preparati dal modello](../../images/plugin-stconverter-turns.png)
<a id="redrafting-a-book"></a>

## Ridisegnare un libro

Per riscrivere un libro parte per parte, carica quello vecchio come sorgente. La scheda mostra **Upload a chat**, **Load a backup** e
**Pick a book** finché non ha una sorgente:

- **Pick a book** apre un elenco dei tuoi libri (escluso quello in cui ti trovi). Vengono caricate le parti del ramo su cui si trova il libro, dalla prima parte a quella in cui il libro si trova ora.
- **Load a backup** accetta un file di backup di un libro: il JSON, lo `.zip` con le immagini, oppure un file scaricato dai [backup](../books/backups.md) di un libro. Il backup viene solo letto, non viene ripristinato nulla. Il ramo è quello su cui si trovava il libro al momento del backup.

Di ogni parte il convertitore conserva le istruzioni, l'ambientazione della scena, il personaggio POV, i personaggi presenti e il testo della parte,
e nient'altro (niente riassunti, lorebook, ragionamenti, immagini, altri rami o impostazioni), così un libro lungo resta leggero.
Un libro senza parti non carica nulla, e un backup le cui parti non risalgono alla prima parte viene rifiutato come danneggiato.

La scheda ha poi due elenchi, come per una chat ma senza la bacchetta (il libro ha già le sue istruzioni, quindi la sezione **Convert
to turn details** non c'è):

- **Parti** è il testo di ogni parte, con la stessa casella di controllo e la freccia verso la parte successiva non spuntata. Leggi il vecchio testo accanto alla storia che stai scrivendo.
- **Turn details** ha una riga per parte con la sua ambientazione della scena, il personaggio POV, i personaggi presenti e le istruzioni.
  **Clicca una riga** per inserire questi dati nel nuovo modulo di turno della storia, come per una chat, e genera la nuova parte. Spunta i turni che hai completato. Due turni possono essere **uniti** come per una chat (il pulsante di unione del primo), per scrivere due parti del vecchio libro come una sola; i turni uniti vengono conservati con il libro.

Con un libro viene conservata una sola sorgente. Per caricarne un'altra, rimuovi quella attuale con la croce (dopo la conferma); questo
cancella anche le spunte fatte su di essa.

<a id="on-a-phone"></a>

## Su telefono

Nella versione per telefono la scheda **Converter** si trova nel cassetto della storia, accanto alla struttura e alle altre schede delle estensioni (apri il cassetto con il pulsante in alto a sinistra della storia). Tutto quanto sopra funziona allo stesso modo, con queste differenze:

- Non c'è il tasto Shift, quindi l'**intervallo si sceglie con due tocchi**: tocca un messaggio per iniziare l'intervallo, tocca un altro per terminarlo (l'intervallo va dall'uno all'altro, in qualunque ordine); il tocco successivo inizia un nuovo intervallo. Puoi anche digitare **From** e **To** oppure usare **All**.
- I dettagli dei turni non hanno una colonna per i numeri dei messaggi: sono la prima riga di ogni turno (**Msgs**).
- **Tocca un turno** e il cassetto si chiude e si apre il nuovo modulo di turno della storia con i quattro campi compilati.
- La scheda è più comoda in modalità orizzontale; le finestre di dialogo (il prompt della bacchetta, la scelta di un libro) entrano nello schermo.

<a id="storage"></a>

## Archiviazione

Il file caricato viene salvato come [risorsa](../resources.md) personale (una chat di SillyTavern può occupare megabyte, con tutto ciò che SillyTavern aggiunge ai messaggi), collegata al libro. Le parti di un libro vengono salvate allo stesso modo come piccolo file JSON
(`<book name>.redraft.json`) con le istruzioni e il testo; il libro conserva solo le spunte. Le spunte, i dettagli dei turni, il modello e il prompt sono memorizzati
negli attributi del libro, quindi si trovano nei [backup dei libri](../books/backups.md) e nelle copie.

!!!warning
Il file della chat non è incluso nei backup dei libri, e una copia di un libro punta allo stesso file. Dopo un ripristino le spunte e i dettagli dei turni ci sono, ma il convertitore chiede di nuovo la chat (carica lo stesso file; questo sostituisce i dettagli dei turni
e le spunte). Lo stesso vale per le parti di un libro: carica di nuovo il libro o il backup e spunta di nuovo. Rimuovere la sorgente con la croce elimina la risorsa.
!!!
