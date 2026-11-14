---
label: Rami e albero della storia
order: 80
---

# Rami e albero della storia

Marginalia non ti costringe mai a buttare via una versione. Ogni alternativa che provi viene conservata come **ramo** della storia, e puoi tornare a una qualsiasi di esse.

<a id="how-the-story-tree-works"></a>

## Come funziona l'albero della storia

Ogni parte continua quella precedente. Quando una parte ha più di una continuazione, la storia si divide: ogni percorso dalla prima parte a una fine è un ramo.

```
#1 ─ #2 ─ #3 ─ #4 ─ #5          ← branch A
           └─ #4' ─ #5' ─ #6'   ← branch B (active)
```

Il **ramo attivo** è quello che l'[editor della storia](story-editor.md) mostra e continua. Al modello vengono inviate solo le sue parti; gli altri rami non influenzano la storia.

Ci sono tre modi per ottenere un nuovo ramo:

| | Cosa succede | La versione precedente |
|---|---|---|
| **Scorri** (ultima parte) | Il modello scrive un'altra versione dell'ultima parte. | Resta come ramo separato. |
| **Dirama la storia** (qualsiasi parte) | La storia continua da questa parte su un nuovo ramo. | Le parti successive restano sul vecchio ramo. |
| **Rigenera** (ultima parte) | Il modello riscrive l'ultima parte, sul posto. | Sostituita: non viene creato alcun ramo. |

*Scorri* e *Rigenera* si trovano solo nel menu dell'ultima parte; *Dirama la storia* si trova nel menu di ogni parte.

<a id="trying-another-version-of-the-last-part"></a>

## Provare un'altra versione dell'ultima parte

Quando non ti piace l'ultima parte, scegli **Scorri** nel suo menu. Il modello riscrive la parte a partire dagli stessi [dettagli del turno](story-editor.md#turn-details), e la nuova versione diventa la fine del ramo attivo. La versione precedente viene conservata come ramo a sé; puoi tornarci nella [Vista dei rami](#the-branch-view).

Per cambiare ciò che dovrebbe accadere prima di riprovare, apri **Dettagli del turno** dell'ultima parte e modifica le istruzioni.

Usa **Rigenera** da solo quando non ti serve la vecchia versione: sovrascrive la parte.

<a id="branching-from-an-earlier-part"></a>

## Diramare da una parte precedente

Per portare la storia in una direzione diversa da un punto precedente, apri il menu di quella parte e scegli **Dirama la storia**. Marginalia copia la parte in un nuovo ramo e la rende la fine del ramo attivo, quindi l'editor della storia ora termina con quella parte. Continua con **+** come al solito.

Le parti che venivano dopo non vanno perse: restano sul vecchio ramo.

I [riassunti](summaries.md) della parte copiata vengono copiati insieme ad essa.

<a id="the-branch-view"></a>

## La Vista dei rami

La scheda **Vista dei rami** mostra l'intero albero della storia. Ogni parte è una scheda con la sua data, l'ambientazione della scena, i personaggi presenti e le istruzioni; passa il mouse su una scheda per vederne tutti i dettagli.

![L'albero della storia](../../images/story-tree.png)

- Le parti del **ramo attivo** sono evidenziate, e la sua ultima parte è contrassegnata.
- **Fai clic sull'ultima parte di un altro ramo** per rendere attivo quel ramo. Il libro passa alla scheda *Storia* e lo mostra. Si può fare clic solo sulle fini dei rami; per continuare da una parte nel mezzo, usa [Dirama la storia](#branching-from-an-earlier-part).
- I lunghi tratti senza alcuna diramazione (più di 20 parti) sono compressi in un blocco che mostra le prime dieci e le ultime dieci parti. Fai clic sul blocco per mostrare tutte le parti.

<a id="searching-all-branches"></a>

### Cercare in tutti i rami

La casella sopra l'albero cerca nel testo di **tutti i rami** del libro, non solo in quello attivo. Digita ciò che stai cercando e premi **Invio** o fai clic su **Cerca**; la casella ricorda le tue ultime ricerche, scegline una dall'elenco per ripeterla. La **x** nella casella cancella la ricerca.

- Una parte corrisponde quando il suo testo contiene **tutte le parole** che hai digitato, in qualsiasi ordine: `lighthouse key` trova le parti che hanno entrambe le parole. Racchiudi le parole tra virgolette doppie per cercare una frase: `"lighthouse key"`.
- Usa `*` per qualsiasi carattere: `light*` trova *lighthouse* e *lightning*. Gli altri caratteri, compresi `%`, `_` e `\`, vengono cercati così come sono.
- Maiuscole e minuscole non fanno differenza. Per le lettere accentate, una parola digitata in minuscolo trova anche la stessa parola in MAIUSCOLO o con l'iniziale maiuscola (`žluťoučký` trova *Žluťoučký* e *ŽLUŤOUČKÝ*).
- Viene cercato solo il testo delle parti, non le istruzioni, l'ambientazione della scena o il lorebook. Le parti che hai eliminato non vengono cercate.

![La vista dei rami con una ricerca](../../images/story-tree-search.png)

La casella di controllo **Includi le versioni** accanto alla casella cerca anche nelle [versioni precedenti del testo](version-history.md#searching-the-old-text) delle parti; una parte che contiene le parole solo in una vecchia versione viene evidenziata comunque, indica quale versione è, e ha **Mostra nella cronologia** accanto a **Mostra nella storia**.

Ogni parte corrispondente viene **evidenziata** in arancione e mostra il testo attorno alla corrispondenza; la riga sopra l'albero indica quante parti sono state trovate. I lunghi tratti che sarebbero compressi vengono aperti quando contengono una corrispondenza. La ricerca resta attiva finché non la cancelli o chiudi la finestra del libro, e l'evidenziazione viene aggiornata quando torni alla scheda.

Fai clic su **Mostra nella storia** in una parte evidenziata per leggerla:

- Quando la parte è sul ramo attivo, il libro passa alla scheda *Storia* e scorre fino a essa.
- Quando è su un altro ramo, quel ramo diventa prima il ramo attivo. Termina alla fine di ramo più vicina sotto la parte trovata (la più recente se ce ne sono due), così l'editor della storia mostra la storia fino alla parte trovata e ciò che è seguito. Il libro passa quindi alla scheda *Storia* e scorre fino alla parte, che lampeggia brevemente.

Cambiare ramo equivale a fare clic sulla fine di un ramo nell'albero: non si perde nulla, il ramo precedente resta com'era e puoi tornarci dall'albero.

<a id="deleting-parts-and-branches"></a>

## Eliminare parti e rami

**Elimina** nel menu di una parte rimuove solo quella parte. Le parti successive salgono di un posto e continuano dalla parte che le precede. Per rimuovere un intero ramo, elimina le sue parti partendo dalla fine.

I conteggi di parole e token nella scheda *Informazioni* mostrano sia l'intero libro (tutti i rami) sia il ramo attivo.
