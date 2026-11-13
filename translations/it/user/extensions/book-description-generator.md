---
label: Generatore di descrizione del libro
order: 21
---

# Generatore di descrizione del libro

Permette al modello di scrivere il **nome** e la **descrizione** di un libro a partire dalla sua storia. Scegli quale parte della storia il modello legge, e copi ciò che ha scritto nel libro.

<a id="opening-it"></a>

## Aprirlo

Con l'estensione installata, la scheda **Informazioni** della finestra del libro ha un pulsante **Genera nome e descrizione** alla fine. Funziona anche nel [layout per telefono](../phone.md#extensions). La finestra di dialogo ha le opzioni a sinistra e il risultato a destra.

![La finestra che genera il nome e la descrizione](../../images/plugin-book-description.png)

| Opzione | |
|---|---|
| **Modello**, **Protocolli** | Con cosa viene eseguita la generazione. Inizia con il modello e il protocollo del libro, altrimenti con i valori predefiniti da *Impostazioni* (vedi [Account e impostazioni](../account.md)); la scelta vale solo per questa generazione. Senza un modello mostra solo un avviso. |
| **Dal messaggio**, **Al messaggio** | L'intervallo della storia, nei numeri dei messaggi con testo del ramo attivo. L'inizio del messaggio è mostrato sotto il campo, così puoi capire di quale si tratta. Inizia con l'intera storia. |
| **Usa i riassunti** | Un [riassunto](../books/summaries.md) sostituisce i messaggi che copre, quando sono tutti nell'intervallo; questo rende molto più economico un intervallo lungo. Un riassunto che esce dall'intervallo non viene usato, e nemmeno uno che non descrive più la storia (vengono usati invece i messaggi). Viene ricordato per la prossima volta. |
| **Formato della risposta** | *Automatico* chiede un JSON con uno schema quando il servizio del modello lo supporta, e Markdown quando non lo supporta. *JSON* impone uno schema. *JSON senza schema* descrive invece il JSON nel prompt, per un server che non può gestire uno schema insieme al ragionamento del modello. *Markdown* chiede il nome come primo titolo e la descrizione sotto di esso. Qualunque cosa scriva il modello viene letta come JSON o come Markdown, quindi un modello che ignora l'istruzione viene comunque compreso. Viene ricordato per la prossima volta. |

Sotto le opzioni un contatore indica quanti token occupa l'intervallo insieme al prompt, quanti ne accetta il modello, e se la storia viene inviata così com'è o va prima trasformata in appunti.

**Genera** avvia il modello, lo stesso pulsante lo ferma. Il nome e la descrizione compaiono a destra, ciascuno con il proprio pulsante **Copia nel libro**: inserisce il testo nel campo della scheda Informazioni e salva subito il libro (anche il titolo della finestra del libro cambia). Se la risposta non può essere letta come nome e descrizione, la finestra di dialogo mostra il testo scritto dal modello.

<a id="a-story-that-is-too-long"></a>

## Una storia troppo lunga

Quando l'intervallo supera ciò che il modello accetta, la storia viene divisa in parti che ci stanno, in ordine, e il modello scrive **appunti** su ciascuna di esse (compaiono sotto *Appunti* man mano che arrivano). La descrizione viene quindi scritta a partire dagli appunti invece che dalla storia. Gli appunti che sono ancora troppo lunghi vengono divisi e annotati di nuovo, fino a quattro giri. Un singolo messaggio più grande di una parte viene tagliato in corrispondenza di un paragrafo, di una riga o di una frase.

!!!
Se né il modello né il protocollo hanno impostata una dimensione del contesto, il plugin non può sapere cosa è troppo lungo e invia sempre la storia così com'è.
!!!

<a id="prompts"></a>

## Prompt

Ci sono due prompt: quello che scrive il nome e la descrizione, e quello che scrive gli appunti di una parte di una storia lunga. Sono [impostazioni ereditate](../books/prompts.md#inherited-settings): un libro usa le proprie, altrimenti la cartella più vicina sopra di esso che ne imposta una, altrimenti le tue, altrimenti il testo predefinito integrato.

| Prompt | Variabili |
|---|---|
| **Prompt della descrizione** | `{{story}}` (la storia, o gli appunti ricavati da essa), `{{fromNotes}}`, `{{bookName}}`, `{{bookDescription}}` |
| **Prompt degli appunti** | `{{story}}` (la parte), `{{part}}`, `{{parts}}`, `{{bookName}}`, `{{bookDescription}}` |

Impostali nella scheda *Prompt* del libro, sotto *Impostazioni dell'estensione*, nella finestra di dialogo di una cartella, oppure per tutti i tuoi libri in *Impostazioni → Impostazioni dell'estensione → Generatore di descrizione del libro*. Le istruzioni che dicono al modello come scrivere la risposta (JSON o Markdown) vengono aggiunte dall'estensione dopo il prompt della descrizione, quindi il tuo prompt deve solo dire cosa scrivere.
