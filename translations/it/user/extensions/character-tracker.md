---
label: Tracciatore dei personaggi
order: 19
---

# Tracciatore dei personaggi

L'elenco dei personaggi del tuo libro nella barra laterale: un elenco con uno slogan, una nota, un'icona e un colore, le
relazioni tra loro e una mappa di quelle relazioni.

<a id="the-characters-tab"></a>

## La scheda Characters

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) riceve una scheda **Characters** accanto a *Outline*. Sopra
l'elenco c'è una riga di pulsanti: **Add** (un nuovo personaggio), un menu con l'importazione delle voci del lorebook, la **Relationship map**
e un pulsante con il fumetto per il prompt (passa sopra con il mouse per vedere i nomi).

Il pulsante con il fumetto apre un popover con **In prompt**, **Depth** e un ruolo, come la [Author's Note](authors-note.md);
il pulsante è verde mentre i personaggi sono nel prompt. Quando *In prompt* è selezionato, ogni generazione riceve i personaggi come messaggio: una riga con il nome, lo slogan tra parentesi
e la nota di ogni personaggio, più un elenco delle relazioni (`Mara -> Tomas: employs`, `<->` quando vale in entrambi i sensi). *Depth* è il numero di messaggi che seguono
(`0` alla fine assoluta, dopo le tue istruzioni, `1` subito prima di esse) e il
ruolo è *System*, *Assistant* o *User*. Il testo prende i suoi token dallo spazio riservato alla storia. Non viene inserito nulla
finché il libro non ha personaggi. Le impostazioni sono salvate con il libro.

Il pulsante con la bacchetta tra i due ha la voce di menu **Import lore entries as characters**, vedi
[più sotto](#importing-characters-from-the-lorebook).

L'elenco è ordinato per nome. Ogni personaggio ha un proprio colore e mostra il **nome** con lo **slogan** sotto,
in caratteri più piccoli (al massimo due righe). I pulsanti di un personaggio:

| Pulsante | |
|---|---|
| freccia a scatto | Espande il personaggio per mostrare la sua **icona** e la sua **nota**. |
| matita | Apre il personaggio nella finestra di dialogo. |
| cestino | Elimina il personaggio e tutte le sue relazioni, dopo una conferma. |

I personaggi sono salvati con il libro non appena li modifichi, quindi sono inclusi nei suoi backup.

![La scheda Characters](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Scegliere i personaggi per un turno

Il modulo del [nuovo turno](../books/story-editor.md) ha i campi **POV character** e **Present characters**. Restano campi di testo libero, ma ognuno ha in fondo un pulsante con l'icona di una persona che elenca i personaggi del libro, in ordine alfabetico.
Un nome scelto dall'elenco di **POV character** sostituisce ciò che c'è nel campo; un nome scelto dall'elenco di
**Present characters** viene aggiunto in fondo, dopo una virgola, a meno che non sia già presente. L'elenco viene letto quando lo apri,
quindi un personaggio che hai aggiunto poco fa è già presente. Puoi comunque digitare un nome qualsiasi, e un nome nel campo non deve
per forza essere un personaggio del libro.

<a id="the-character-dialog"></a>

## La finestra di dialogo del personaggio

**Add** (il pulsante con il più; il suo suggerimento dice *Add new character*) e la matita aprono la stessa finestra di dialogo:

| Campo | |
|---|---|
| **Name** | Obbligatorio. |
| **Tagline** | Poche parole sul personaggio, mostrate nell'elenco e nella mappa. |
| **Note** | Tutto ciò che vuoi ricordare. |
| **Icon** | Carica un PNG, un JPEG o una GIF. Viene ridotta a un massimo di 200 x 200 pixel, mantenendo le proporzioni. |
| **Color** | Sfondo del personaggio nell'elenco e nella mappa. |

I campi si applicano con **Save**.

![Un personaggio con le sue relazioni](../../images/plugin-charactertracker-dialog.png)

Se premi **Cancel** (o Esc) mentre ci sono modifiche non salvate, comprese una relazione compilata ma non ancora aggiunta,
ti viene chiesto se scartarle.

![La domanda quando si annulla un personaggio con modifiche non salvate](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Relazioni

Quando apri un personaggio esistente, la finestra di dialogo elenca anche le sue relazioni: quelle che il personaggio indica (`→`),
quelle che indicano il personaggio (`←`) e quelle unidirezionali (`↔`), ciascuna con un nome e uno slogan. Il pulsante con il cestino
elimina una relazione.

Per aggiungerne una, cerca l'altro personaggio, scegli la **Direction** (*this character → other*, *other → this
character* o *both ways*), inserisci un **Name** (ad esempio *employs*, *friends*) e, se vuoi, una **Tagline**, poi premi **Add
relationship** (il pulsante si trova sopra questi campi). Le relazioni vengono salvate subito. Un personaggio nuovo può ricevere relazioni
solo dopo essere stato salvato.

<a id="importing-characters-from-the-lorebook"></a>

## Importare personaggi dal lorebook

Se i tuoi personaggi sono già nel lorebook, il menu con la bacchetta li crea per te. **Import lore entries as characters**
apre una finestra di dialogo con le voci del lorebook del libro e di tutti i suoi sotto-lorebook, ordinate per *order*. Ognuna mostra il suo nome e una casella di spunta, più un'icona **i** che mostra il contenuto della voce in un popover, per un'occhiata veloce prima di decidere; la casella nell'intestazione dell'elenco seleziona o deseleziona tutte le voci. **Import** crea un personaggio
con il nome di ogni voce selezionata (slogan, nota, icona e colore restano vuoti perché tu li compili), **Close** esce senza importare.

Le voci non vengono confrontate con i personaggi che hai già: importare due volte la stessa voce crea due personaggi.

![Importazione di personaggi dal lorebook](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## La mappa delle relazioni

**Relationship map** mostra tutte le relazioni del libro come grafo. Ogni personaggio che ha una relazione è un cerchio con la sua icona (o un riquadro, se non ne ha) sul suo colore, con il nome e lo slogan; ogni relazione è una freccia tra due personaggi, con il suo nome e il suo slogan. Una freccia punta verso il destinatario; una relazione unidirezionale ha una freccia a entrambe le estremità. I personaggi senza relazioni non compaiono nella mappa.

![La mappa delle relazioni](../../images/plugin-charactertracker-map.png)

I personaggi sono disposti in anelli attorno a un personaggio, inizialmente quello con più relazioni: i personaggi collegati a lui stanno sul primo anello, quelli collegati a questi ultimi sul secondo, e così via. **Fai doppio clic su un personaggio** per disporre la mappa attorno a lui. Puoi anche trascinare i personaggi, spostare la mappa e ingrandirla o rimpicciolirla con la rotellina del mouse.

<a id="commands"></a>

## Comandi

I personaggi sono anche [comandi](../commands/index.md), per la riga di comando e per gli [agenti](../agents.md). Operano sul
libro aperto (per un agente: sulla sua copia di lavoro), e una scheda Characters aperta mostra subito la modifica. Un personaggio è
indicato dal suo **nome** (non conta maiuscole o minuscole), oppure dal suo **numero** in `/list-characters` (l'ordine dei nomi) quando due
personaggi hanno lo stesso nome. Una relazione è indicata dal suo numero in `/list-character-relations`. Un comando che modifica un
personaggio accetta solo i campi che gli indichi; un testo vuoto cancella uno slogan o una nota. L'icona si può impostare solo
nella finestra di dialogo.

| Comando | Cosa fa |
|---|---|
| `/list-characters` | I personaggi in ordine alfabetico: slogan, colore, se c'è una nota, numero di relazioni. Solo lettura. |
| `/get-character character` | Un personaggio con la sua nota e le sue relazioni. Solo lettura. |
| `/create-character name [tagline [note [color]]]` | Aggiunge un personaggio. `color` è `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Modifica un personaggio. |
| `/delete-character character` | Elimina un personaggio con le sue relazioni, dopo una conferma. |
| `/list-character-relations [character]` | Le relazioni, o quelle di un personaggio. Solo lettura. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Aggiunge una relazione da `source` a `target` (*employs*), oppure con `bidirectional` `true` una in cui i due sono uguali (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Modifica il nome o lo slogan di una relazione. |
| `/delete-character-relation relationship` | Elimina una relazione, dopo una conferma. |
| `/character-tracker-map` | I personaggi e le loro relazioni come testo che riceve il prompt. Solo lettura. |

`character-tracker-map` è pensato per il [macro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands): inseriscilo nel
template principale, nel prompt utente o in una voce del lorebook per collocare i personaggi dove vuoi nel prompt, al posto (o oltre)
dell'inserimento automatico della scheda Characters. Mostra sempre lo stato attuale dei personaggi, che l'inserimento automatico sia attivo o no.

Aggiungere e modificare non è distruttivo; lo sono solo i comandi `delete`, per i quali un agente può essere impostato in modo da chiederti conferma.

Il [wizard della trama](arc-outline.md) dell'Arc Outline usa gli stessi comandi quando selezioni i suoi strumenti **Characters**, così puoi sviluppare il cast mentre parli della trama.

<a id="phone"></a>

## Telefono

La scheda si trova nel cassetto della storia, accanto alla scaletta, come la [Author's Note](authors-note.md). Anche nel modulo del nuovo turno del telefono ci sono i pulsanti per il POV e per i personaggi presenti.
