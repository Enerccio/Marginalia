---
label: Lettori beta
order: 22
---

# Lettori beta

Lettori che inventi tu, e una chat con loro sul tuo libro. Descrivi un **personaggio** (un nome, un'icona e alcune righe su come la persona
legge e a cosa tiene), e il modello la interpreta: reagisce alle parti della tua storia come farebbe quel lettore, da solo o in un gruppo
che discute al suo interno. Nulla di ciò che dicono i lettori fa parte del libro.

![Una chat di gruppo con due personaggi](../../images/plugin-betareaders-chat.png)

<a id="the-beta-readers-tab"></a>

## La scheda Lettori beta

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Lettori beta**, accanto a *Struttura*. Ha una
scheda per ogni chat aperta del libro. Il **+** avvia una chat, e il menu (⋮) accanto ha:

| Voce | |
|---|---|
| Nuova chat | Uguale a **+**. |
| Rinomina chat | Cambia il titolo della chat. |
| Chiudi chat | Chiude la scheda; la chat viene conservata e si trova in *Chat chiuse*. |
| Impostazioni chat | Cambia le [opzioni della chat](#the-options-of-a-chat). |
| Elimina chat | Elimina la chat e la sua cronologia, dopo una conferma. |
| Personaggi di questo libro | I personaggi che appartengono solo a questo libro, vedi [sotto](#personas). |
| Chat chiuse | Riapre una chat chiusa. |

Le chat vengono salvate insieme al libro, quindi sono incluse nei suoi backup.

![La scheda, prima della prima chat](../../images/plugin-betareaders-tab.png)

![Il menu delle chat](../../images/plugin-betareaders-menu.png)

<a id="personas"></a>

## Personaggi

Un personaggio ha un **nome**, una **descrizione** (ciò che il modello interpreta: come legge il personaggio, cosa gli piace e cosa lo infastidisce) e un'**icona** facoltativa, un'immagine ridotta a una dimensione piccola. Senza icona il personaggio è un cerchio con le prime lettere del nome.

I personaggi del tuo account sono la **libreria dei personaggi** in *Impostazioni → Impostazioni estensione → Lettori beta*, che parte con sei lettori: Athena,
Vesper, Ember, Quill, Nox e Sable. **Modifica personaggi** apre la libreria in una finestra in cui puoi aggiungere, modificare, copiare, eliminare e riordinare
i personaggi, caricare le loro icone e **Aggiungi i personaggi di partenza** che mancano alla libreria. Le modifiche si applicano con **Salva**, e **Annulla**
le scarta.

![La libreria dei personaggi](../../images/plugin-betareaders-library.png)

Un libro può avere personaggi propri: *Personaggi di questo libro* nel menu delle chat apre la stessa finestra con i personaggi che offre solo questo
libro. Sono contrassegnati *(questo libro)* quando li scegli per una chat.

![I personaggi di un libro](../../images/plugin-betareaders-book-personas.png)

**Scrivi con il modello** nella finestra di un personaggio fa il lavoro di scrittura al posto tuo: descrivi il personaggio in poche parole (*un lettore a cui
interessa solo chi l'ha fatto e perché*), premi il pulsante, e il modello compila il **Nome** e la **Descrizione** nello stile della libreria. Sono solo
campi: modificali, oppure riscrivili, e usa **Salva** quando sono giusti. Il modello che scrive è quello delle chat (quello del libro per i
personaggi di un libro, quello nelle impostazioni dell'estensione per la libreria, altrimenti il tuo modello predefinito); i nomi già presenti nell'elenco
gli vengono comunicati perché non ne scelga uno uguale.

![Un personaggio scritto dal modello](../../images/plugin-betareaders-write-persona.png)

Una chat conserva una **copia** dei suoi personaggi, quindi una modifica nella libreria non cambia una chat già iniziata; la copia viene modificata quando lo chiedi
(vedi [il gruppo](#a-group)).

<a id="starting-a-chat"></a>

## Avviare una chat

**+** apre la finestra di una nuova chat.

![La finestra di una nuova chat](../../images/plugin-betareaders-new-chat.png)

| Campo | |
|---|---|
| Tipo | **Un personaggio** oppure **Un gruppo**. Non può essere cambiato in seguito. |
| Titolo | Vuoto: i nomi dei personaggi. |
| Personaggio/i | Un personaggio per una chat con uno; per un gruppo, almeno uno (un gruppo può iniziare con un personaggio e altri si aggiungono in seguito). |

La parte ripiegata, **Storia, mezzo, modello e formato**, contiene le [opzioni di una chat](#the-options-of-a-chat). Ognuna di esse può restare
vuota, e in quel caso viene ereditata.

Una chat con **un personaggio** è una conversazione tra te e quel personaggio. Un **gruppo** è una conversazione di tutti i personaggi presenti con te
e tra di loro: una risposta può far parlare più di loro, uno dopo l'altro, e possono non essere d'accordo, prendersi in giro e basarsi
sui punti degli altri. Un messaggio della risposta può anche essere una narrazione (*Narratore*): una breve descrizione di ciò che fa il personaggio.
<a id="talking"></a>

## Parlare

Scrivi nella casella in basso e premi **Invia**; la risposta compare man mano che viene scritta, e **Interrompi** (il pulsante diventa *Stop* mentre il modello risponde) la interrompe. Una risposta è un turno del modello: il ragionamento del modello, quando ne invia uno (una sezione **Ragionamento** chiusa; un modello che non ragiona non ne mostra), e i messaggi dei personaggi, ciascuno con ciò che fa e ciò che dice.

<a id="sending-a-scene"></a>

### Inviare una scena

**Invia come scena** (accanto a *Invia*) invia il testo come scena invece che come qualcosa che dici: un cambio di scena (*Le luci si spengono e il bar cade nel silenzio.*), oppure la descrizione di ciò che fai (*Metto il manoscritto sul tavolo e aspetto.*). Compare come scheda **Scena** nella cronologia, e i personaggi reagiscono mentre accade, come a un messaggio. Usa la scena della chat (vedi [più sotto](#the-scene)) per dire dove inizia la conversazione e questo pulsante per ciò che accade dopo. Un messaggio di scena può essere modificato ed eliminato come i tuoi messaggi; non invia parti della storia (la casella *Includi le ultime parti* appartiene a *Invia*).

![Una scena nella cronologia di una chat, e la risposta a essa](../../images/plugin-betareaders-scene.png)

Ai personaggi del gruppo viene comunicato chi è presente solo alla fine del tuo ultimo messaggio (questo non viene salvato), quindi aggiungere o rimuovere un personaggio non modifica mai la cronologia precedente.

<a id="sending-the-story"></a>

### Inviare la storia

La chat non conserva una copia della storia. Ricorda quali parti hai **dato** ai personaggi, e per ogni richiesta la storia che leggono viene ricostruita a partire dalla storia così com'è ora: le parti che hanno ricevuto, con i riassunti al posto delle più vecchie, nei limiti in cui entrano nel contesto del modello. Le parti stanno davanti al messaggio che le ha date, così una parte è nella richiesta una sola volta e l'inizio della richiesta resta lo stesso testo quando dai altre parti: un server di modelli che conserva l'inizio del prompt rilegge soltanto ciò che è nuovo. Una parte modificata viene letta come modificata, e una parte eliminata non viene più letta, il che cambia la richiesta a partire dal messaggio che l'aveva data. Un riassunto creato in seguito sostituisce le parti che descrive a partire dal messaggio successivo.

**Includi le ultime parti** è ciò che dà ai personaggi più della storia: aggiunge le parti che non hanno ancora ricevuto (vedi [più sotto](#how-much-of-the-story-the-personas-read)). Lascialo disattivato per chattare soltanto e dar loro la storia più tardi. L'etichetta della casella indica quante parti aggiungerebbe (*Includi la storia (5 parti)*, *Includi le ultime parti (2 nuove)*, *nessuna nuova*). Segue la storia da sola: quando aggiungi, modifichi o elimini una parte, o cambi ramo, il numero cambia e la casella è spuntata quando c'è qualcosa di nuovo. Un tuo messaggio indica quali parti ha dato (*Parti 1-5 inviate*).

**Ultima parte** e **Prima parte**, accanto alla casella, sono [quanta storia leggono i personaggi](#how-much-of-the-story-the-personas-read); cambiale lì man mano che prosegui nella lettura.

Sotto la casella ci sono ciò che i personaggi hanno letto (*I personaggi hanno letto: parti 1-5*) e la dimensione del contesto (*Contesto: 2.266 di 30.720 token*): ciò che il prossimo messaggio invierebbe, cioè la cronologia, ciò che hai scritto e, quando **Includi le ultime parti** è spuntata, le parti che aggiungerebbe. Cambia quando spunti la casella o fai una pausa mentre scrivi. Le parti ottengono almeno metà del contesto; quando la cronologia non ci sta accanto, i messaggi più vecchi (con le parti che hanno dato) vengono lasciati fuori dalla richiesta, non eliminati, e un avviso indica quanti sono.

<a id="changing-the-conversation"></a>

### Modificare la conversazione

| Pulsante | |
|---|---|
| penna (il tuo messaggio) | Modifica il tuo messaggio; le parti che ha dato restano date. |
| cestino | Elimina il tuo messaggio, o un messaggio di una risposta (il resto del turno resta, e il modello vede il turno senza di esso). |
| Rigenera | Scarta l'ultima risposta e chiede di nuovo. |

Quando una risposta non è riuscita o manca, il pannello lo dice e offre **Chiedi di nuovo**.

![Le azioni di un messaggio](../../images/plugin-betareaders-actions.png)

<a id="a-group"></a>

### Un gruppo

*Personaggi (n)* sopra la conversazione elenca chi è presente. In un gruppo puoi:

- **Far entrare un personaggio** dalla libreria o dal libro, con una nota facoltativa che spiega al gruppo perché è arrivato ("Mara ha letto tutto il libro"). La cronologia riceve una riga (*Mara si è unita alla conversazione.*), così il modello sa quando è successo.
- **Far uscire** un personaggio (il pulsante a freccia della sua riga), di nuovo con una nota. Restano nell'elenco, in grigio e contrassegnati *(uscito)*. L'ultimo personaggio non può uscire.
- **Aggiorna dalla libreria**: porta nella chat la descrizione e l'icona attuali del personaggio dalla libreria (o dal libro).

![Un personaggio entrato nel gruppo](../../images/plugin-betareaders-joined.png)

![Far entrare un personaggio](../../images/plugin-betareaders-bring-in.png)
<a id="the-options-of-a-chat"></a>

## Le opzioni di una chat

La finestra di una nuova chat e **Impostazioni della chat** hanno le stesse opzioni; ognuna può essere lasciata vuota, e in quel caso la chat eredita il valore.

| Opzione | |
|---|---|
| Il tuo nome | Come i personaggi ti chiamano; i tuoi messaggi lo mostrano al posto di *Tu*. Vuoto: senza nome. |
| Scena | Dove e come si svolge la conversazione, in poche frasi: *un bar piovoso dopo la chiusura*, *una videochiamata dopo il club del libro*. Senza scena, la chat è una chat online, vedi [sotto](#the-scene). Se la si cambia in una chat in corso, il prompt di sistema cambia dalla richiesta successiva in poi e non lascia nessuna riga nella cronologia; per ciò che accade da ora in avanti usa [un messaggio di scena](#sending-a-scene). |
| Mezzo | Ciò che il testo rappresenta: un libro, un film, una serie TV, un manga o fumetto, un audiodramma, un'opera teatrale, un gioco o una visual novel. I personaggi reagiscono come reagirebbe il pubblico di quel mezzo. Se lo si cambia in una chat in corso, lascia una riga nella cronologia (*Da ora il testo è letto come: ...*). |
| Modello | L'IA che risponde. |
| Protocollo | Il [protocollo](../protocols.md) usato con essa. |
| Formato della risposta | Come viene chiesto al modello di scrivere: **Automatico** (JSON con uno schema quando il modello sa seguirne uno, testo libero altrimenti), **JSON** (sempre con uno schema), **JSON senza schema** oppure **Testo libero**. Vedi [il ragionamento](#the-reasoning). |
| I personaggi leggono fino alla parte | Il [limite di spoiler](#how-much-of-the-story-the-personas-read). Solo nella finestra di una nuova chat; una chat esistente lo ha sotto la casella di testo. |
| Inizia dalla parte | La parte da cui inizia la storia che i personaggi ricevono. Come sopra. |
| Usa i riassunti della storia | Vedi sotto. |
| Messaggi massimi in un turno | Il numero massimo di messaggi che i personaggi scrivono in risposta a un tuo messaggio (da 1 a 50). Vuoto: la tua impostazione in *Impostazioni → Impostazioni dell'estensione → Beta Readers*. Una risposta che ne ha di più viene tagliata, e al modello viene comunicato il limite. |

![Le impostazioni di una chat](../../images/plugin-betareaders-chat-settings.png)

<a id="the-reasoning"></a>

### Il ragionamento

Il **Ragionamento** sopra una risposta è il pensiero del modello, così come lo invia il server (lo stesso delle altre generazioni di Marginalia); un modello che
non pensa non ne ha, e non viene mostrato nulla. Il plugin non chiede al modello un ragionamento proprio: poche frasi scritte sotto uno schema sarebbero
più un piano che un pensiero, e intralcerebbero un modello che pensa. Il pensiero scritto nel testo tra i tag `<think>` viene mostrato anch'esso come
ragionamento.

**JSON senza schema** (in *Impostazioni della chat*) chiede lo stesso JSON solo nel prompt, senza un formato di risposta; usalo con un server che non accetta
uno schema insieme al pensiero del modello.

<a id="the-text-as-it-is-written"></a>

### Il testo così come viene scritto

La risposta viene mostrata mentre il modello la scrive, messaggio per messaggio e parola per parola: il messaggio in scrittura appare non appena si sa chi
parla, e cresce. Una parte che è un'azione appare quando se ne scrive il tipo, così un gesto non sembra mai prima un discorso.

La conversazione segue la risposta mentre cresce. Se scorri verso l'alto per leggere qualcosa di precedente, resti dove sei; torna alla
fine e riprende a seguire. Inviare un messaggio porta sempre alla fine.

<a id="the-scene"></a>

### La scena

La scena è un testo che entra nel prompt di sistema di ogni chat, dopo il prompt, così i personaggi sanno dove si trovano e come si parlano
tra loro. È una [impostazione ereditata](../books/prompts.md#inherited-settings): quella della chat, altrimenti quella del libro, altrimenti quella
della cartella più vicina, altrimenti quella in *Impostazioni → Impostazioni dell'estensione → Beta Readers*, altrimenti quella predefinita: **una chat online**, in cui tutti scrivono
messaggi brevi da dove si trovano. Un prompt tuo che contiene `{{scene}}` decide dove si colloca la scena; senza di esso la scena
viene aggiunta dopo il prompt.

<a id="how-much-of-the-story-the-personas-read"></a>

### Quanta storia leggono i personaggi

Le parti sono numerate a partire da 1, come nella scheda *Struttura* della storia.

- **I personaggi leggono fino alla parte** (il campo **Ultima parte** sotto la casella di testo) è il limite di spoiler: i personaggi non ricevono mai una parte successiva, e i riassunti vengono raccolti solo dalla storia fino a essa. Alzalo quando la storia va avanti, e il messaggio successivo propone come nuove le parti che erano state trattenute. Abbassalo e i personaggi smettono subito di leggere le parti successive.
- **Inizia dalla parte** (**Prima parte**) lascia fuori le parti precedenti, nemmeno come riassunti. Usalo per far leggere ai personaggi solo un capitolo.
- **Usa i riassunti della storia** (attivo di default) permette alla storia di iniziare con i riassunti e poi con le parti successive al più recente di essi,
  il che mantiene piccola una storia lunga. I riassunti si applicano solo quando la storia inizia dal suo principio.

Una prima parte successiva all'ultima viene rifiutata.

Modificare questi valori sotto la casella di testo (vengono salvati non appena smetti di digitare o clicchi un pulsante di passo) cambia la storia che i personaggi leggono dal messaggio successivo; non viene inviato nulla di nuovo, perché la
storia viene costruita per ogni messaggio.

![Una chat che legge solo le parti 2 e 3](../../images/plugin-betareaders-spoiler.png)

Il lorebook non viene inviato ai personaggi: conoscono solo ciò che la storia racconta loro.

<a id="settings"></a>

## Impostazioni

*Impostazioni → Impostazioni dell'estensione → Beta Readers* è il posto dei tuoi valori predefiniti:

![Le impostazioni del plugin](../../images/plugin-betareaders-settings.png)

| Impostazione | |
|---|---|
| Mezzo | Il mezzo delle chat. |
| Modello delle chat | L'IA. Vuoto: il modello del libro. |
| Protocollo delle chat | Vuoto: il protocollo del libro. |
| Prompt di una chat con un personaggio | Il prompt di sistema di una chat uno a uno. Vuoto: il prompt predefinito. |
| Prompt di una chat di gruppo | Il prompt di sistema di una chat di gruppo. Vuoto: il prompt predefinito. |
| Scena | Vedi [sopra](#the-scene). Vuoto: una chat online. |
| Messaggi massimi in un turno | Il numero massimo di messaggi che i personaggi scrivono in risposta a un tuo messaggio, per ogni chat che non imposta un limite proprio (da 1 a 50). Vuoto: automatico, 3 per una chat con un personaggio e circa due per ogni personaggio di un gruppo (al massimo 8). Un limite basso mantiene brevi le risposte; alzalo quando i personaggi vengono interrotti a metà della conversazione. |
| Libreria dei personaggi | **Modifica personaggi**, vedi [sopra](#personas). |
| Mezzi | **Modifica mezzi**: ogni mezzo ha un nome e il prompt che dice ai personaggi come reagire ad esso. **Ripristina i mezzi predefiniti** aggiunge quelli che mancano. |

`{{medium}}` in un prompt viene sostituito dal testo del mezzo e `{{scene}}` dalla scena. Le impostazioni vengono salvate con il pulsante **Salva** della pagina *Impostazioni*.

La scena, il mezzo, il modello, il protocollo e i due prompt sono anche [impostazioni ereditate](../books/prompts.md#inherited-settings): la scheda *Prompt*
di un libro e le impostazioni di una [cartella](../books/folders.md) li hanno nella sezione **Impostazioni dell'estensione**. Un libro usa il proprio
valore, altrimenti quello della cartella più vicina, altrimenti quello di *Impostazioni*, altrimenti quello predefinito; il modello e il protocollo del libro
sono l'ultima risorsa. Una chat sovrascrive per sé il mezzo, il modello e il protocollo nelle sue [opzioni](#the-options-of-a-chat).

<a id="on-a-phone"></a>

## Su un telefono

Funziona. La scheda **Beta Readers** si trova nel cassetto della struttura della storia, e le chat sono le stesse che su desktop; lo spazio per la conversazione è ridotto, quindi il formato orizzontale è più comodo. Vedi [Usare Marginalia su un telefono](../phone.md#extensions).

<a id="storage"></a>

## Archiviazione

I personaggi e le chat di un libro vengono salvati negli attributi del libro (quindi con i suoi backup), i personaggi del tuo account e le
impostazioni nelle tue impostazioni utente. Il modello viene interrogato con i soliti limiti dell'IA, e i token contano come usati dal libro.
