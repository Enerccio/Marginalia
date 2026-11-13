---
label: Tracciatore personaggi
order: 19
---

# Tracciatore personaggi

Il cast del tuo libro nella barra laterale: un elenco di personaggi con uno slogan, una nota, un'icona e un colore, le
relazioni tra di loro e una mappa di tali relazioni.

<a id="the-characters-tab"></a>

## La scheda Personaggi

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Personaggi** accanto a
*Struttura*. Sopra l'elenco c'è una riga di pulsanti: **Aggiungi** (un nuovo personaggio), un menu con l'importazione
delle voci del lorebook, la **Mappa delle relazioni** e un pulsante con il fumetto per il prompt (passa il mouse per i nomi).

Il pulsante con il fumetto apre un popover con **Nel prompt**, **Profondità** e un ruolo, come le [Note dell'autore](authors-note.md);
il pulsante è verde mentre i personaggi sono nel prompt. Quando *Nel prompt* è selezionato, ogni generazione riceve i personaggi come messaggio: una riga con il nome, lo slogan tra parentesi
e la nota di ciascun personaggio, e un elenco delle relazioni (`Mara -> Tomas: employs`, `<->` quando va in entrambe le direzioni). *Profondità* è il numero di messaggi che seguono
(`0` la fine assoluta, dopo le tue istruzioni, `1` subito prima di esse) e il ruolo è *Sistema*, *Assistente* o *Utente*. Il testo occupa i suoi token nello spazio
per la storia. Non viene inserito nulla finché il libro non ha personaggi. Le impostazioni sono salvate con il libro.

Il pulsante con la bacchetta tra i due ha la voce di menu **Importa voci del lorebook come personaggi**, vedi
[più sotto](#importing-characters-from-the-lorebook).

L'elenco è ordinato per nome. Ogni personaggio ha un colore proprio e mostra il suo **nome** con lo **slogan** sotto,
in caratteri più piccoli (al massimo due righe). I pulsanti di un personaggio:

| Pulsante | |
|---|---|
| freccia a scatto | Espande il personaggio per mostrare la sua **icona** e la sua **nota**. |
| matita | Apre il personaggio nella finestra di dialogo. |
| cestino | Elimina il personaggio e tutte le sue relazioni, previa conferma. |

I personaggi sono salvati con il libro non appena li modifichi, quindi sono inclusi nei suoi backup.

![La scheda Personaggi](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Scegliere i personaggi per un turno

Il modulo del [nuovo turno](../books/story-editor.md) ha i campi **Personaggio POV** e **Personaggi presenti**. Restano campi di testo libero, ma ciascuno ha in fondo un pulsante con un'icona di persona che elenca i personaggi del libro, ordinati per nome. Un nome scelto nell'elenco di **Personaggio POV** sostituisce quello presente nel campo; un nome scelto nell'elenco di **Personaggi presenti** viene aggiunto in fondo, dopo una virgola, a meno che non sia già presente. L'elenco viene letto quando lo apri, quindi un personaggio aggiunto poco fa è incluso. Puoi comunque digitare qualsiasi nome, e un nome nel campo non deve per forza essere un personaggio del libro.

<a id="the-character-dialog"></a>

## La finestra di dialogo del personaggio

**Aggiungi** (il pulsante con il più; il suggerimento recita *Aggiungi nuovo personaggio*) e la matita aprono la stessa finestra di dialogo:

| Campo | |
|---|---|
| **Nome** | Obbligatorio. |
| **Slogan** | Poche parole sul personaggio, mostrate nell'elenco e nella mappa. |
| **Nota** | Tutto ciò che vuoi ricordare. |
| **Icona** | Carica un PNG, JPEG o GIF. Viene ridimensionata a non più di 200 x 200 pixel, mantenendo le proporzioni. |
| **Colore** | Sfondo del personaggio nell'elenco e nella mappa. |

I campi si applicano con **Salva**.

![Un personaggio con le sue relazioni](../../images/plugin-charactertracker-dialog.png)

Se premi **Annulla** (o Esc) mentre ci sono modifiche non salvate, incluso una
relazione che hai compilato ma non ancora aggiunto, ti viene chiesto se scartarle.

![La domanda quando si annulla un personaggio con modifiche non salvate](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Relazioni

Quando apri un personaggio esistente, la finestra di dialogo elenca anche le sue relazioni: quelle che il personaggio indica (`→`),
quelle che puntano verso di lui (`←`) e quelle unidirezionali (`↔`), ciascuna con un nome e uno slogan. Il pulsante con il cestino
elimina una relazione.

Per aggiungerne una, cerca l'altro personaggio, scegli la **Direzione** (*questo personaggio → altro*, *altro → questo
personaggio* o *in entrambe le direzioni*), inserisci un **Nome** (ad esempio *employs*, *friends*) e, facoltativamente, uno **Slogan**, poi premi **Aggiungi
relazione** (il pulsante si trova sopra questi campi). Le relazioni sono salvate subito. Un nuovo personaggio può ricevere relazioni
solo dopo essere stato salvato.

<a id="importing-characters-from-the-lorebook"></a>

## Importare personaggi dal lorebook

Se i tuoi personaggi sono già nel lorebook, il menu con la bacchetta li crea per te. **Importa voci del lorebook come personaggi**
apre una finestra di dialogo con le voci del lorebook del libro e di tutti i suoi sotto-lorebook, ordinate per *ordine*. Ciascuna mostra il suo
nome e una casella di controllo, e un'icona **i** che mostra il contenuto della voce in un popover, per una rapida occhiata
prima di decidere; la casella nell'intestazione dell'elenco seleziona o deseleziona tutte le voci. **Importa** crea un personaggio
con il nome di ogni voce selezionata (slogan, nota, icona e colore restano vuoti perché tu li compili), **Chiudi** esce senza importare.

Le voci non vengono confrontate con i personaggi che hai già: importare due volte la stessa voce crea due personaggi.

![Importare personaggi dal lorebook](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## La mappa delle relazioni

**Mappa delle relazioni** mostra tutte le relazioni del libro come grafo. Ogni personaggio che ha una relazione è un cerchio con la sua icona
(o un riquadro, se non ne ha una) sul proprio colore, con il suo nome e lo slogan; ogni relazione è una freccia tra due personaggi con il suo
nome e lo slogan. Una freccia punta verso il bersaglio; una relazione unidirezionale ha una freccia a entrambe le estremità. I personaggi senza
relazioni non sono nella mappa.

![La mappa delle relazioni](../../images/plugin-charactertracker-map.png)

I personaggi sono disposti in anelli attorno a un personaggio - inizialmente quello con più relazioni: i
personaggi con cui è in relazione stanno sul primo anello, quelli a loro volta collegati sul secondo e così via. **Fai doppio clic su un personaggio** per
disporre la mappa attorno a lui. Puoi anche trascinare i personaggi, spostare la mappa e ingrandire con la rotellina del mouse.

<a id="commands"></a>

## Comandi

I personaggi sono anche [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md). Operano sul
libro aperto (un agente: sulla sua copia di lavoro), e una scheda Personaggi aperta mostra la modifica subito. Un personaggio si indica con il suo **nome** (non conta
maiuscole o minuscole), oppure con il suo **numero** in `/list-characters` (l'ordine dei nomi) quando due
personaggi hanno lo stesso nome. Una relazione si indica con il suo numero in `/list-character-relations`. Un comando che modifica un
personaggio prende solo i campi che gli dai; un testo vuoto cancella uno slogan o una nota. L'icona si può impostare solo
nella finestra di dialogo.

| Comando | Cosa fa |
|---|---|
| `/list-characters` | I personaggi ordinati per nome: slogan, colore, se c'è una nota, numero di relazioni. Solo lettura. |
| `/get-character character` | Un personaggio con la sua nota e le sue relazioni. Solo lettura. |
| `/create-character name [tagline [note [color]]]` | Aggiunge un personaggio. `color` è `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Modifica un personaggio. |
| `/delete-character character` | Elimina un personaggio con le sue relazioni, previa conferma. |
| `/list-character-relations [character]` | Le relazioni, o quelle di un personaggio. Solo lettura. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Aggiunge una relazione da `source` a `target` (*employs*), oppure, con `bidirectional` `true`, una in cui sono pari (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Modifica il nome o lo slogan di una relazione. |
| `/delete-character-relation relationship` | Elimina una relazione, previa conferma. |
| `/character-tracker-map` | I personaggi e le loro relazioni come testo che riceve il prompt. Solo lettura. |

`character-tracker-map` è pensato per la [macro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands): inseriscilo nel
modello principale, nel prompt utente o in una voce del lorebook per collocare i personaggi dove vuoi nel prompt, al posto (o oltre) dell'inserimento
automatico della scheda Personaggi. Riflette sempre lo stato attuale dei personaggi, che l'inserimento sia attivo o no.

Aggiungere e modificare non è distruttivo; lo sono solo i comandi `delete`, per i quali un agente può essere impostato in modo da chiederti conferma.

<a id="phone"></a>

## Telefono

La scheda si trova nel cassetto della storia, accanto alla struttura, come le [Note dell'autore](authors-note.md). I pulsanti per
i personaggi POV e presenti sono presenti anche nel modulo del nuovo turno del telefono.
