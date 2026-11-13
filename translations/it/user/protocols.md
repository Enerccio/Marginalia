---
label: Protocolli
order: 970
icon: sliders
---

# Protocolli

Un protocollo è un insieme di impostazioni di generazione: i parametri di campionamento (temperatura, top P e penalità) e,
facoltativamente, i propri limiti di contesto e di risposta. Il [fornitore di inferenza](inference-providers.md) stabilisce *quale* modello scrive; il protocollo stabilisce *come*.

I protocolli sono separati dai fornitori, quindi puoi conservarne alcuni, per esempio uno prudente e uno creativo, e
passare un libro dall'uno all'altro senza toccare la connessione. Come i fornitori, i protocolli appartengono al tuo account; puoi [condividerne](#sharing-a-protocol) uno in modo che altri utenti possano importarne una copia.

![La scheda Protocolli](../images/protocols-list.png)

<a id="creating-a-protocol"></a>

## Creare un protocollo

Apri la scheda **Protocolli** e fai clic su **Aggiungi protocollo**. I protocolli esistenti si modificano con il pulsante a forma di matita e si eliminano
con il pulsante del cestino. Il pulsante di clonazione (due fogli) crea una copia di un protocollo, con tutte le sue impostazioni, con un nome che confermi (quello originale con *(Copy)*), utile per provare altre impostazioni di campionamento senza perdere quelle che funzionano.

**Importa da un altro utente** (sopra l'elenco) mostra i protocolli che altri utenti [condividono](#sharing-a-protocol) e importa una copia di uno di essi.

![La finestra di dialogo del protocollo](../images/protocol-dialog.png)

| Campo | |
|---|---|
| **Nome** | Il tuo nome per il protocollo, per esempio `Creative` o `Long context`. |
| **Tipo** | Il formato della richiesta. Al momento solo **Chat Completion**. |
| **Token massimi di contesto** | Facoltativo. Token totali per prompt e risposta dei libri che usano questo protocollo. Se vuoto, si usa la **Dimensione massima del contesto** del fornitore. |
| **Token massimi di risposta** | Facoltativo. La risposta più lunga. Se vuoto, si usa il valore **Token massimi di risposta** del fornitore. |
| **Temperatura** | Casualità del testo. Valori bassi (intorno a `0.7`) producono un testo più concentrato e prevedibile, valori alti (`1.0` e oltre) un testo più vario e sorprendente. |
| **Top P** | Campionamento a nucleo: il modello sceglie solo tra le parole più probabili, le cui probabilità sommate raggiungono questo valore (`0`-`1`). `0.9`-`0.95` è una scelta comune. |
| **Penalità di frequenza** | Penalizza le parole in base a quante volte sono già comparse, riducendo le ripetizioni. Di solito `0`-`1`. |
| **Penalità di presenza** | Penalizza le parole che sono già comparse, spingendo il modello verso argomenti nuovi. Di solito `0`-`1`. |
| **Condividi con altri utenti** | Se selezionato, altri utenti possono vedere il protocollo e importarne una copia, vedi [sotto](#sharing-a-protocol). Disattivato per un nuovo protocollo. |

Ogni parametro di campionamento ha una casella **Abilita**. Un parametro non abilitato non viene impostato da Marginalia, e il
modello usa il proprio valore predefinito. Abilita solo ciò che vuoi modificare.

<a id="sharing-a-protocol"></a>

## Condividere un protocollo

Seleziona **Condividi con altri utenti** nella finestra di dialogo del protocollo e ogni altro utente di questa installazione vedrà il protocollo in **Importa da un altro utente** (il pulsante accanto ad **Aggiungi protocollo**), con il suo nome e il suo autore (il tuo nome completo, o il tuo login se non lo hai impostato). Possono aprire una riga per vedere le sue impostazioni e fare clic su **Importa**, che crea un protocollo proprio con le stesse impostazioni e lo stesso nome. La condivisione è di sola lettura: nessun altro può modificare, eliminare o usare il tuo protocollo, e la copia è privata e non collegata alla tua - ciò che l'una o l'altra parte modifica in seguito non raggiunge l'altra. Deseleziona la casella e il protocollo scompare dal loro elenco; le copie che hanno già creato restano loro.

<a id="limits"></a>

## Limiti

I limiti del [fornitore di inferenza](inference-providers.md#general-settings) si applicano sempre. Un protocollo può abbassarli,
ciascuno separatamente, ma mai superarli oltre il valore del fornitore:

| | Valore del protocollo impostato | Valore del protocollo vuoto |
|---|---|---|
| Contesto | **Token massimi di contesto**, oppure la **Dimensione massima del contesto** del fornitore se è più piccola | **Dimensione massima del contesto** del fornitore |
| Risposta | **Token massimi di risposta**, oppure il **Token massimi di risposta** del fornitore se è più piccolo | **Token massimi di risposta** del fornitore |

Un protocollo con **Token massimi di contesto** 128 000 usato con un fornitore da 32 000 ottiene 32 000: il fornitore è ciò che il
servizio (o l'amministratore che lo condivide) consente.

Marginalia lascia libero lo spazio per la risposta e riempie il resto del contesto con il prompt: template, voci del lorebook, riassunti e tutta la parte di storia che vi entra. Con un contesto di 32 000 token e risposte fino a 2 000
token, il prompt ottiene 30 000 token. Il limite della risposta viene inviato anche all'API (`max_completion_tokens`).

Usa i limiti del protocollo per mantenere i prompt più corti di quanto il modello consenta, per esempio per rendere più economici e veloci i libri lunghi con un modello cloud a contesto ampio, oppure per accorciare le risposte per alcuni libri. I limiti del protocollo vengono usati anche per i [riassunti](books/summaries.md) dei libri con questo protocollo.

Lascia entrambi vuoti, a meno che non ti servano: si applicano allora i valori del fornitore e, quando cambi il fornitore di un libro, i suoi limiti vengono con esso.

<a id="using-a-protocol-in-a-book"></a>

## Usare un protocollo in un libro

Ogni libro ha il proprio protocollo nel campo **Protocolli** della sua scheda **Informazioni**. Per usare un protocollo in tutti i nuovi libri,
selezionalo come **Protocollo predefinito** nella scheda **Impostazioni**. Un libro senza protocollo non può generare e mostra
*Al libro manca il protocollo.*

<a id="suggested-settings"></a>

## Impostazioni consigliate

Punti di partenza, da regolare per il tuo modello:

| Uso | Temperatura | Top P | Penalità di frequenza | Penalità di presenza |
|---|---|---|---|---|
| Prosa equilibrata | `0.8` | `0.95` | disattivata | disattivata |
| Creativo, vario | `1.0` | `0.95` | `0.3` | `0.3` |
| Coerente, vicino alle istruzioni | `0.6` | `0.9` | disattivata | disattivata |

I modelli di ragionamento spesso ignorano o rifiutano i parametri di campionamento; per questi lasciali disattivati.
