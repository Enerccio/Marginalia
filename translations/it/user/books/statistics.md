---
label: Statistiche di scrittura
order: 75
---

# Statistiche di scrittura

Marginalia tiene il conteggio di ciò che succede ai tuoi libri, giorno per giorno: quante parole hai scritto e quante ne hanno scritte i modelli, per quanto tempo hai scritto, quanti token sono stati inviati ai modelli, quanto è cresciuto il libro. La finestra **Statistiche** mostra tutto questo per un libro o per tutti, sotto forma di numeri, grafici e un **calendario** dei giorni in cui hai lavorato, con la tua **serie** e un **obiettivo giornaliero**.

I numeri servono solo a te: ogni account vede i propri libri. Il conteggio inizia con la versione 1.6.0; ciò che è avvenuto prima viene [ricostruito dalla cronologia delle parti](#days-before-the-statistics) nel modo migliore possibile.

<a id="opening-the-window"></a>

## Aprire la finestra

- **Un libro:** il menu delle impostazioni (le rotelle sotto la storia) contiene **Statistiche**. La finestra conteggia quel libro.
- **Tutti i libri:** l'elenco dei libri ha **Statistiche** nel suo menu **Gestisci**. La finestra somma tutti i tuoi libri e ha altre due schede, **Libri** e **Per libro**.

Entrambe le finestre funzionano allo stesso modo. In alto scegli il **Periodo** (*Oggi*, *Ieri*, *Ultimi 7 giorni*, *Ultimi 30 giorni*, *Questo mese*, *Sempre*, oppure *Personalizzato* con una data **Da** e una data **A**) e **Conteggiato per**: **Tutte**, **Io** (tu e i modelli che hai richiesto) oppure **Agenti** (ciò che hanno fatto gli [agenti](../agents.md), tenuto separato). I giorni sono quelli dell'orologio del server.

![Le statistiche di un libro: i giorni](../../images/statistics-book.png)

Sotto la scelta ci sono i totali del periodo, poi le schede.

<a id="what-is-counted"></a>

## Cosa viene conteggiato

| | |
|---|---|
| **Parole scritte** | Parole che hai scritto tu: il testo digitato in una parte, nell'editor della storia o in qualsiasi altro punto in cui modifichi una parte. |
| **Parole generate** | Parole che i modelli hanno scritto per il libro. |
| **Parole rimosse** | Parole che sono sparite: testo tagliato da una parte, una parte eliminata. |
| **Parti create**, **Parti eliminate**, **Modifiche** | Quante parti sono state create ed eliminate, e quante volte hai salvato una parte modificata. |
| **Token inviati**, **Token generati**, **Richieste** | Ciò che è stato inviato ai modelli per questo libro (l'intero prompt ogni volta) e ciò che è tornato indietro, e quante richieste sono servite. |
| **Aperta** | Per quanto tempo il libro è rimasto aperto sullo schermo. |
| **Modifica** | Per quanto tempo hai digitato in una parte. Una lunga pausa ferma il cronometro. |
| **Generazione** | Per quanto tempo un modello ha lavorato per il libro. |
| **Agenti in esecuzione** | Per quanto tempo un agente ha lavorato sul libro. |
| **Crescita** | Di quanto il libro è diventato più grande (o più piccolo) nel periodo, in parole. |

I tempi sono quelli reali dell'orologio. Quando due libri sono aperti contemporaneamente, o tre agenti lavorano insieme, il totale conteggia quell'intervallo di tempo una sola volta, quindi le righe dei libri possono sommarsi a più del totale (la finestra lo indica). Le parole e i token invece si sommano.

Importare un libro o un testo (un archivio, una chat da SillyTavern), copiare un libro e ripristinare un backup non contano nulla: il testo non è stato scritto qui. Ciò che fanno gli agenti viene conteggiato dalla loro parte (**Agenti**), rispetto al libro originale, anche se lavorano su una copia; include il lavoro scartato o mai unito al libro.

<a id="the-tabs"></a>

## Le schede

| Scheda | |
|---|---|
| **Giorni** | Una riga per ogni giorno con qualcosa dentro: tutti i numeri sopra, dal più recente. Clicca un'intestazione per ordinare. |
| **Parole** | Un grafico delle parole scritte, generate e rimosse per giorno. Un periodo lungo viene raggruppato per settimana o per mese. |
| **Tempo** | Un grafico delle ore di apertura, modifica, generazione e con un agente in esecuzione. |
| **Token** | Un grafico dei token inviati e generati. |
| **Dimensione** | La dimensione del libro alla fine di ogni giorno e quanto è cresciuto ogni giorno, in **Parole** o in **Token**. |
| **Libri** | (tutti i libri) Una riga per ogni libro, anche quelli nel [cestino](../trash.md) o eliminati, con la sua cartella, i numeri, il tempo e la crescita, e la sua quota. **Mostra i libri eliminati** nasconde quelli nel cestino e quelli eliminati definitivamente. Un clic su un libro lo apre. |
| **Per libro** | (tutti i libri) I libri come barre, in base al valore che scegli in **Grafico per**. Vengono mostrati i dieci più grandi e il resto è una sola barra, *Altro*. |
| **Calendario** | I giorni in cui hai lavorato, le tue serie e il tuo obiettivo, vedi sotto. |

![Il tempo per giorno](../../images/statistics-time.png)

La dimensione di un libro viene misurata un istante dopo ogni modifica, a partire dalle parole e dai token salvati nelle parti. I token di una parte vecchia sono quelli conteggiati quando è stata scritta, quindi cambiare il modello del libro non modifica nessun numero.

<a id="the-calendar"></a>

## Il calendario

La scheda **Calendario** mostra un piccolo quadrato per ogni giorno dell'ultimo anno (53 settimane, 16 su un telefono), una settimana per colonna e il giorno più recente nell'angolo in basso a destra, contrassegnato da una cornice. Più il quadrato è scuro, più hai fatto quel giorno. Tieni il puntatore su un giorno per vedere la data, le parole che hai scritto, il tempo di modifica e quando ci sei stato l'ultima volta (un `~` dopo un giorno ricostruito dalla cronologia).

![Il calendario di tutti i libri, con l'obiettivo del giorno](../../images/statistics-global-calendar.png)

Un giorno conta come **lavorato** quando hai scritto parole, modificato una parte, creato una parte o trascorso tempo in modifica. Ciò che hanno fatto gli agenti, le parole solo generate e l'apertura di un libro non contano: il calendario riguarda *la tua* scrittura. Un quadrato appena più scuro di uno vuoto indica un giorno in cui hai lavorato ma scritto poco.

Scegli come colorare i quadrati con **Parole scritte** o **Modifica** (minuti di modifica). Il calendario mostra sempre le settimane più recenti, indipendentemente dal **Periodo** scelto sopra di esso.

Sopra il calendario:

| | |
|---|---|
| **Serie attuale (giorni)** | I giorni consecutivi in cui hai lavorato, contati a ritroso da oggi. Un oggi in cui non hai ancora lavorato non interrompe la serie: in quel caso la serie viene contata da ieri. |
| **Serie più lunga (giorni)** | La serie più lunga di giorni consecutivi, di sempre. |
| **Giorni di lavoro** | Quanti giorni dei quadrati mostrati hai lavorato. |
| **Serie di obiettivi (giorni)**, **Serie di obiettivi più lunga (giorni)** | Come le serie sopra, per i giorni che hanno raggiunto il tuo obiettivo giornaliero (quando ne imposti uno, nella finestra di tutti i libri). |

<a id="the-daily-goal"></a>

### L'obiettivo giornaliero

Imposta un obiettivo nella scheda **Impostazioni**, nelle impostazioni generali:

| Campo | |
|---|---|
| **Obiettivo giornaliero: parole scritte** | Le parole che scrivi in un giorno, su tutti i tuoi libri. Le parole generate dai modelli non contano. |
| **Obiettivo giornaliero: minuti di modifica** | I minuti che modifichi in un giorno, su tutti i tuoi libri. |

![Gli obiettivi nelle Impostazioni](../../images/settings-daily-goal.png)

Lascia vuoto un campo per non avere un obiettivo. Con entrambi impostati, un giorno raggiunge l'obiettivo quando raggiunge **entrambi**. La finestra di **tutti i libri** mostra allora a che punto sei oggi con due barre (*Oggi: 310 di 500 parole*), la colorazione del calendario segue l'obiettivo (un giorno che l'ha raggiunto è il più scuro), e la **Serie di obiettivi** conta i giorni che l'hanno raggiunto. Senza obiettivo, la colorazione segue il tuo giorno più intenso del calendario. La finestra di un singolo libro non ha obiettivi (un obiettivo su tutti i libri significa poco per uno solo di essi): i suoi quadrati sono colorati in base al giorno più intenso di quel libro.

Quando un giorno non ha un tempo registrato (vedi sotto) e il calendario è colorato per **Modifica**, viene solo contrassegnato come giorno lavorato.

![Le statistiche di tutti i libri su un telefono](../../images/phone-statistics.png)

<a id="days-before-the-statistics"></a>

## Giorni prima delle statistiche

Le statistiche non venivano conservate prima della versione 1.6.0. Quando Marginalia è stato aggiornato, ha ricostruito i giorni precedenti dalla cronologia delle parti: la prima versione di una parte è una parte creata con le parole che aveva, una successiva è una modifica, e una parte che non esiste più è un'eliminazione. Questi giorni sono una stima. Sono contrassegnati da un `~` nell'elenco dei giorni e la finestra indica quanti sono; i token di un giorno ricostruito sono solo un minimo. Il **tempo** non può essere ricostruito, perché nessuno l'ha registrato: la finestra indica da quale giorno è registrato (*Il tempo è registrato da ...*), i giorni precedenti mostrano un trattino e sono esclusi dal grafico del tempo. Il **calendario** conta un giorno ricostruito come lavorato, come qualsiasi altro.
<a id="privacy-clearing-not-recording-keeping-less"></a>

## Privacy: cancellare, non registrare, conservare meno

Le statistiche indicano quando hai lavorato a un libro, per quanto tempo, e conservano il nome e la cartella di ogni libro, anche di quelli che hai eliminato. Sono tue, e decidi tu cosa ne resta.

![Registra le statistiche di scrittura e Conserva le statistiche per, nelle Impostazioni](../../images/settings-statistics-privacy.png)

- **Cancella le statistiche di questo libro** (pulsante nella finestra di un libro) e **Cancella tutte le statistiche** (nella finestra di tutti i libri) eliminano in modo definitivo i conteggi, i tempi e le dimensioni, dopo una richiesta di conferma. I libri non vengono modificati. Il conteggio prosegue poi da zero.
- Nell'elenco dei libri, un libro che è nel cestino o non esiste più ha **Dimentica**, che elimina solo le sue statistiche.
- **Registra le statistiche di scrittura** nelle [impostazioni](../account.md) disattiva il conteggio per i tuoi libri. Quanto registrato resta finché non lo cancelli; la finestra indica che non viene contato nulla di nuovo. Anche un amministratore può disattivare la registrazione per tutti, con la stessa nota.
- **Conserva le statistiche per (mesi)** nelle impostazioni elimina i giorni più vecchi di quel periodo, una volta al giorno e al salvataggio. Se il campo è vuoto o 0, tutto viene conservato. L'amministratore può impostare un limite per tutti, e si usa il più breve dei due. Delle dimensioni di un libro resta l'ultima rilevazione prima del limite, così che la crescita del primo giorno conservato possa comunque essere calcolata.

L'[amministratore](../administration/statistics.md) può cancellare le statistiche di un utente o di tutti gli utenti.

<a id="commands"></a>

## Comandi

Due [comandi](../commands/index.md) leggono gli stessi numeri: `/get-book-statistics [range] [side]` per il libro aperto, giorno per giorno, e `/get-statistics [range] [by] [side]` per tutti i tuoi libri, per giorno o per libro. L'intervallo può essere `today`, `yesterday`, `last-7-days`, `last-30-days`, `this-month`, `all-time`, un giorno (`2026-03-14`) o due giorni (`2026-03-01..2026-03-14`). Anche un [agente](../agents.md) può usarli.
