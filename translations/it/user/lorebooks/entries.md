---
label: Voci e attivazione
order: 90
---

# Voci e attivazione

<a id="the-entry-list"></a>

## L'elenco delle voci

Ogni riga dell'editor del lorebook è una voce:

| Colonna | |
|---|---|
| casella | **Attivata**. Le voci disattivate non vengono mai usate. |
| **Nome della voce** | Il nome che dai alla voce. Non viene inviato al modello. |
| **Ordine** | Posizione della voce nel prompt, vedi [Ordine e inserimento](#order-and-insertion). Le nuove voci ricevono `100`. |
| **Tag** | Tag che attivano la voce, vedi [Tag](#tags). |
| ⌄ | Mostra i dettagli della voce. |
| espandi | Apre la voce nell'[editor a schermo intero](lorebooks.md#editing-in-full-screen) (desktop). |
| cestino | Elimina la voce (dopo conferma). |

**Aggiungi voce** nella barra degli strumenti aggiunge una voce con ordine `100`. Le modifiche vengono salvate mentre scrivi.

<a id="searching-and-deleting-many-entries"></a>

### Cercare ed eliminare molte voci

La casella con la lente d'ingrandimento accanto ai pulsanti mostra solo le voci che contengono il testo nel nome, nel contenuto, nella nota o nel filtro (non conta se maiuscolo o minuscolo); svuotala per rivedere tutte le voci. Lavora solo sulle voci del lorebook selezionato, non sui sotto-lorebook. Nel layout per telefono la casella si trova sopra l'elenco.

Accanto alla casella ci sono altre due caselle che limitano l'elenco. **Tag** mostra le voci che hanno il tag (elenca i tag che almeno una voce del lorebook possiede; i tag *negativi* non vengono considerati). **Stato** mostra **Tutti**, **Attivata**, **Disattivata** o **Costante (senza chiavi)**: le voci attivate e senza filtro, quindi usate per ogni parte. La ricerca, il tag e lo stato si combinano: una voce deve corrispondere a tutti e tre. Nel layout per telefono le due caselle si trovano sotto la casella di ricerca.

La colonna delle caselle a sinistra seleziona le voci (quella nell'intestazione seleziona tutte le voci mostrate, quindi con una ricerca seguita dalla casella dell'intestazione si selezionano le corrispondenze). Quando c'è una selezione, nella barra degli strumenti compare **Elimina i selezionati** con il numero delle voci; chiede una sola conferma e poi le sposta nel [cestino](../trash.md). Anche la [modifica multipla](lorebooks.md#mass-edit) ha un pulsante **Elimina i selezionati**. Sul telefono, il menu **⋮** delle voci contiene **Select entries**: davanti alle voci compaiono delle caselle, e compare **Elimina i selezionati** con il numero delle voci spuntate (anche una singola voce ha lì il suo pulsante **Delete entry**).


<a id="search-and-replace"></a>

## Cerca e sostituisci

**Cerca e sostituisci** (a destra di **Aggiungi voce**) cambia una parola, un nome o una frase nel nome, nel testo e nel commento di tutte le voci del lorebook in una volta sola. Funziona come la [ricerca e sostituzione della storia](../books/story-editor.md#search-and-replace), senza le opzioni per i riassunti e i rami: **Cerca** elenca le voci con corrispondenze (la tabella mostra i loro nomi) e le corrispondenze al loro interno, così come sono e come saranno; **Sostituisci** modifica le voci. Ha le opzioni **Cerca con regex** (vedi [Espressioni regolari](../regular-expressions.md)) e **Distingui maiuscole e minuscole** (attiva per impostazione predefinita; bloccata mentre la regex è attiva, che può usare `(?i)` per ignorare le maiuscole). Le voci disattivate vengono cercate comunque. Sul telefono il pulsante (un'icona accanto alla casella *Attivata*) apre la stessa finestra nel layout per telefono.

![La finestra Cerca e sostituisci di un lorebook, con le corrispondenze di una voce aperte](../../images/lorebook-search-replace.png)

La riga di comando dell'editor della storia ha `/search-replace-lore-entries` per il lorebook del libro aperto, vedi [Comandi](../commands/index.md#commands-that-come-with-marginalia).

<a id="entry-details"></a>

## Dettagli della voce

Il pulsante ⌄ apre i dettagli:

![Una voce con i suoi dettagli](../../images/lorebook-entry.png)

| Campo | |
|---|---|
| **Contenuto** | Il testo che entra nel prompt. Un [modello](#templates-in-entries): può usare variabili e macro. |
| **Modalità di filtro** | **Parole chiave (qualsiasi)**, **Parole chiave (tutte)**, **Testo** o **Regex**, come viene confrontato **Testo/pattern di filtro**. |
| **Testo/pattern di filtro** | Il filtro, vedi [Filtri](#filters). Vuoto significa nessun filtro. |
| **Modalità di inserimento** | **Nel blocco del lore** o **Prima del prompt utente**, vedi [Ordine e inserimento](#order-and-insertion). |
| **Activation Context** | Dove viene cercato il filtro: **Istruzioni** (la richiesta per la parte, il valore predefinito) oppure **Istruzioni e parti della storia**, vedi [Cercare nella storia](#looking-into-the-story). |
| **Parti** | Con *Istruzioni e parti della storia*: quante delle parti più recenti della storia vengono cercate insieme alla richiesta, almeno 1. |
| **Dal messaggio** | La voce viene usata solo dalla parte con questo numero in poi, vedi [Limitare una voce ad alcune parti](#limiting-an-entry-to-some-parts). Vuoto: dall'inizio. |
| **To message** | La voce viene usata solo fino alla parte con questo numero. Vuoto: fino alla fine. |
| **Condizione** | Un modello che deve dare un valore vero perché la voce venga usata, vedi [Una condizione per una voce](#a-condition-for-an-entry). Vuoto: nessuna condizione. |
| **Tag negativi (esclusione)** | Tag che disattivano la voce, vedi [Tag](#tags). |
| **Cerca anche nei riassunti** | Se spuntata, il filtro viene cercato anche nei [riassunti](../books/summaries.md) della storia fino a quel punto, quelli che la prossima richiesta invierebbe (un riassunto che non corrisponde più alla storia viene escluso). Richiede un filtro. |
| **Non cercare nel suo contenuto altre voci** | Se spuntata, le parole nel contenuto di questa voce non portano mai dentro altre voci, vedi [Voci portate dentro da altre voci](#entries-brought-in-by-other-entries). |
| **Solo tramite altre voci** | Se spuntata, la voce non viene mai usata per via della richiesta o della storia stessa; solo quando il suo filtro si trova nel contenuto di un'altra voce che è stata usata, vedi [Voci portate dentro da altre voci](#entries-brought-in-by-other-entries). |
| **Nota** | Le tue note. Non vengono inviate al modello. Le voci importate da SillyTavern elencano qui le impostazioni che non è stato possibile importare. |

Scrivi il contenuto come vuoi che il modello lo legga: una descrizione breve e concreta funziona meglio.

> **Capitano Varro** - comandante della guardia del porto di Ostra, sui cinquanta, mano sinistra sfregiata. Fedele al Duca, diffida
> della gilda dei maghi. Parla con frasi brevi e secche.
<a id="activation"></a>

## Attivazione

Prima di ogni parte generata, Marginalia esamina le voci del lorebook del libro e i suoi [sotto-lorebook](lorebooks.md#sub-lorebooks). Una voce viene **attivata** quando:

1. la voce è abilitata, e lo è anche il suo lorebook;
2. la parte da scrivere rientra nel suo intervallo di parti, se ne ha uno (**Dal messaggio**, **Al messaggio**);
3. nessuno dei suoi **tag negativi** è un tag del libro;
4. non ha **tag**, oppure almeno uno di essi è un tag del libro;
5. la sua **condizione**, se ne ha una, è vera, vedi [Una condizione per una voce](#a-condition-for-an-entry);
6. non ha un **filtro**, oppure il filtro corrisponde alle istruzioni per la parte (o, con **Contesto di attivazione** impostato su *Istruzioni e parti della storia*, alle istruzioni e alle parti più recenti della storia).

Devono essere vere tutte le condizioni. Una voce senza tag e senza filtro è sempre attiva.

<a id="tags"></a>

### Tag

I tag collegano le voci ai **tag del libro** (impostati nella scheda [Informazioni](../books/managing-books.md#the-about-tab) del libro). Usali per condividere un solo lorebook tra libri che hanno bisogno di parti diverse:

| Tag della voce | Tag negativi | Attiva in un libro con tag... |
|---|---|---|
| - | - | sempre |
| `winter` | - | `winter` |
| `winter`, `book-2` | - | `winter` **o** `book-2` |
| - | `book-1` | qualsiasi libro tranne `book-1` |
| `winter` | `book-1` | `winter`, a meno che non sia anche `book-1` |

I tag devono corrispondere esattamente. Un tag negativo prevale sempre su un tag.

I tag possono essere assegnati anche a un intero lorebook (il campo **Tag** del [lorebook](lorebooks.md); i lorebook importati da file possono averne). Valgono allora come tag di ciascuna delle sue voci.

<a id="filters"></a>

### Filtri

Un filtro attiva la voce solo quando la **richiesta per la parte** la menziona. Il filtro viene confrontato con il [prompt utente](../books/prompts.md#user-prompt) della parte così come viene inviato - l'*Ambientazione della scena*, il *Personaggio POV*, i *Personaggi presenti* e le *Istruzioni* che hai inserito, all'interno del testo del modello di prompt utente.

| Modalità | Corrisponde quando |
|---|---|
| **Parole chiave (qualsiasi)** | Il filtro è un elenco di parole chiave separate da virgole (`Varro, Ostra`); la richiesta contiene almeno una di esse, senza distinguere maiuscole e minuscole. Gli spazi attorno a una parola chiave vengono ignorati. |
| **Parole chiave (tutte)** | Lo stesso elenco, ma la richiesta deve contenerle tutte. |
| **Testo** | Il testo compare in un punto qualsiasi della richiesta, senza distinguere maiuscole e minuscole. `varro` corrisponde a *Capitano Varro*. |
| **Regex** | L'[espressione regolare](../regular-expressions.md#in-a-lorebook-entry-filter) trova una corrispondenza in un punto qualsiasi della richiesta, senza distinguere maiuscole e minuscole, anche su più righe. |

Ad esempio, la regex `\b(varro|captain)\b` corrisponde alla parola intera *Varro* o *Captain*, mentre `varro.*(harbor|docks)` corrisponde solo quando Varro e il porto o i moli sono entrambi menzionati, in quell'ordine.

!!!warning Per impostazione predefinita il filtro legge la richiesta, non la storia
A meno che la voce non guardi nella storia (vedi [Guardare nella storia](#looking-into-the-story)), un filtro controlla solo le istruzioni per la nuova parte, non il testo delle parti precedenti. Una voce su Varro viene attivata quando Varro è menzionato nella scena, tra i personaggi o nelle istruzioni della parte che stai scrivendo - inseriscilo tra i *Personaggi presenti* quando è nella scena.

Anche la richiesta contiene il testo stesso del modello di prompt utente. Un filtro per una parola che il modello usa (quello predefinito menziona *personaggi*, *trama*, *tono*...) è sempre attivo.
!!!

Un'espressione regolare non valida non corrisponde mai; la voce resta inattiva.

Combina tag e filtro quando una voce deve essere usata solo in alcuni libri, e lì solo quando è pertinente.

<a id="looking-into-the-story"></a>

### Guardare nella storia

Per impostazione predefinita il filtro viene cercato solo nella richiesta per la parte, quindi una voce sul drago viene usata quando *tu* menzioni il drago. Quando la storia deve mantenere la voce in gioco - il drago era nell'ultima parte, e la tua nuova istruzione dice solo "lui vola via" - imposta **Contesto di attivazione** della voce su **Istruzioni e parti della storia** e **Parti** sul numero delle parti più recenti in cui cercare insieme alla richiesta (`1` è la parte precedente a quella da scrivere, `2` le due precedenti...).

- Il filtro viene cercato nel testo di quelle parti e nella richiesta insieme, quindi le parole chiave di **Parole chiave (tutte)** possono essere distribuite tra di esse. La richiesta conta sempre.
- Viene cercato solo il testo delle parti, non i loro riassunti, e solo le parti del ramo che stai scrivendo (uno swipe o una rigenerazione non cerca nella parte che sostituisce).
- Una storia più corta di **Parti** viene cercata per intero. Marginalia legge solo quante parti servono alle voci attivate, quindi un lorebook senza tali voci non tocca affatto la storia.
- Le voci introdotte da [altre voci](#entries-brought-in-by-other-entries) non vengono cercate nella storia, solo nel contenuto delle voci che sono state usate.

<a id="limiting-an-entry-to-some-parts"></a>

### Limitare una voce ad alcune parti

Alcune informazioni sono vere solo per una parte della storia: il travestimento di un personaggio che viene svelato nel capitolo 5, le voci che circolano prima che inizi la guerra. Compila **Dal messaggio** e/o **Al messaggio** con i numeri delle parti, così come la scaletta della storia le numera (la prima parte è `1`; entrambi i numeri sono inclusi). La voce viene usata solo quando la parte da scrivere ha un numero compreso nell'intervallo, qualunque cosa dica il suo filtro - una voce senza filtro è quindi una voce costante solo all'interno dell'intervallo.

- Un campo vuoto non è un limite: solo **Dal messaggio** `10` significa "dalla parte 10 in poi", solo **Al messaggio** `4` significa "fino alla parte 4".
- La parte da scrivere è quella successiva all'ultima parte del ramo (una storia di 9 parti scrive la parte `10`). Uno swipe o una rigenerazione scrive la parte che sostituisce.
- Anche le parti senza testo contano, come nella scaletta. Eliminare o inserire parti sposta i numeri.
- Se **Dal messaggio** è maggiore di **Al messaggio**, la voce non viene mai usata.
- Una voce fuori dal suo intervallo non viene introdotta nemmeno da [altre voci](#entries-brought-in-by-other-entries).
- L'estensione [Lorebook VCS](../extensions/index.md) conserva l'intervallo nelle versioni di una voce; importazione, esportazione e clonazione di un lorebook lo mantengono.

<a id="a-condition-for-an-entry"></a>

### Una condizione per una voce

La **Condizione** è un modello, scritto come il [contenuto di una voce](#templates-in-entries), che Marginalia elabora prima di ogni parte. La voce viene usata solo quando il risultato è **vero**: qualsiasi cosa tranne un risultato vuoto, `0`, `false`, `no` e `off` (senza distinzione di maiuscole). Vede le stesse cose del contenuto: il personaggio POV, i personaggi presenti, la scena, le istruzioni del turno, la storia finora e le variabili.

![Una voce con una condizione: la voce viene usata solo quando Tomáš è il punto di vista e Mizuki è presente](../../images/lorebook-condition.png)

| Condizione | La voce viene usata quando |
|---|---|
| `{%{{{#if presentCharacters}}yes{{/if}}}%}` | il turno ha dei personaggi presenti |
| `{%{{{eval::povCharacter == "Fiora"}}}%}` | il punto di vista è Fiora (vedi [Espressioni](../templates/macros.md#expressions)) |
| `{%{{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}}%}` | Fiora è il punto di vista e Mara è presente |
| `{%{{{getvar::revealed}}}%}` | la variabile `revealed` ha un valore (impostata da una parte precedente, vedi [Riferimento macro](../templates/macros.md#variables)) |
| `{%{{{command::authors-note}}}%}` | il [comando](../commands/index.md) risponde con qualcosa (può essere usato solo un comando pensato per le macro) |

Come funziona:

- Viene verificata dopo l'interruttore di abilitazione, l'intervallo di parti e i tag, e **prima del filtro**: una voce la cui condizione è falsa non viene usata nemmeno quando il suo filtro corrisponde, e non viene introdotta nemmeno da [altre voci](#entries-brought-in-by-other-entries).
- Una condizione che **non può essere decisa** conta come falsa, e l'[anteprima](#preview-activation) spiega il motivo: non è un modello valido, usa un nome che non esiste (un refuso), un comando che non esiste o non è pensato per le macro, oppure il tempo è scaduto. Il campo diventa rosso per i primi due casi mentre scrivi.
- Legge soltanto. Un `{%{{{setvar}}}%}` in una condizione non cambia nulla, né per il prompt né per le altre voci, e i comandi sono solo quelli di sola lettura.
- Viene elaborata una volta per ogni voce e parte, quindi `{%{{{random::yes::no}}}%}` non cambia idea tra il primo passaggio e i passaggi della profondità di scansione.
- L'[anteprima](#preview-activation) indica le voci la cui condizione è falsa: *Non usata: la condizione è falsa*.

  ![L'anteprima dell'attivazione: la voce di Mizuki non viene usata perché la sua condizione è falsa](../../images/lorebook-condition-preview.png)
- Tutte le condizioni di una parte condividono **60 secondi**. Quando sono esauriti, le condizioni rimaste contano come false (*condizione non riuscita*, timeout). Una condizione in esecuzione non viene interrotta; il tempo viene controllato prima della successiva.
- Importazione ed esportazione del lorebook, clonazione ed estensione Lorebook VCS mantengono la condizione.

<a id="entries-brought-in-by-other-entries"></a>

### Voci introdotte da altre voci

Normalmente solo la richiesta per la parte attiva le voci. Con una **profondità di scansione** (il campo del [lorebook](lorebooks.md#the-lorebook-editor); `0`, che è il valore predefinito, lo disattiva) il contenuto delle voci attivate viene cercato alla ricerca dei filtri delle voci che non lo sono, come se facesse parte della richiesta. Una voce il cui filtro viene trovato lì viene attivata a sua volta. Questo è il **passaggio 1**; il contenuto delle voci che esso ha attivato viene cercato nel passaggio 2, e così via, tanti passaggi quanti ne indica la profondità di scansione (al massimo 5).

Ad esempio, con una profondità di scansione di `1`, una voce *Capitano Varro* (filtro `varro`) viene attivata dalla richiesta, e il suo contenuto dice "...serve il Duca di Ostra". Viene allora attivata anche una voce *Ostra* (filtro `ostra`), sebbene la richiesta non menzioni mai la città. Con una profondità di scansione di `2`, la voce *Ostra* a sua volta introdurrebbe una voce sul porto che essa menziona.

- Contano solo i **filtri**: una voce già usata perché non ha filtro non ha bisogno di nulla, e una voce disattivata, o esclusa dai suoi tag, non viene introdotta.
- Il contenuto che viene cercato è il testo della voce così come è scritto; i modelli al suo interno non vengono eseguiti.
- La **profondità di scansione** del lorebook che il **libro usa** vale per le sue voci e per le voci dei suoi sotto-lorebook. Un sotto-lorebook può avere una profondità propria (**Profondità di scansione come sotto-lorebook** nelle sue impostazioni; vuoto: quella del lorebook usato dal libro): le sue voci vengono allora trovate tramite il contenuto di altre solo nei passaggi fino a quella profondità, `0` non ne trova mai. I passaggi effettuati sono tanti quanti indica la profondità maggiore. Il campo di un sotto-lorebook vale solo finché è un sotto-lorebook; quando un libro lo usa direttamente, vale la sua normale **Profondità di scansione**.
- Una voce con **Non cercare nel suo contenuto altre voci** selezionato può essere attivata, ma non introduce nessuno.
- Una voce con **Solo tramite altre voci** selezionato non viene mai trovata dalla richiesta: viene usata solo quando il contenuto di una voce usata ha il suo filtro. Senza una profondità di scansione, o senza un filtro, non viene mai usata; l'[anteprima](#preview-activation) indica *Non usata: usata solo tramite il contenuto di altre voci*.
- L'ordine nel prompt non dipende da come una voce è stata trovata, vedi [Ordine e inserimento](#order-and-insertion).
- Le estensioni che cambiano quali voci vengono usate (vedi [Estensioni](../extensions/index.md)) vengono interpellate anche sulle voci introdotte.
<a id="preview-activation"></a>

## Anteprima dell'attivazione

**Anteprima dell'attivazione** (il pulsante con l'occhio nella barra degli strumenti dell'editor del lorebook, e sul telefono accanto al pulsante di ricerca e sostituzione) risponde alla domanda "quali voci verrebbero usate per questo testo?" senza generare nulla. Digita il testo della richiesta in **Testo del turno** (la scena, i personaggi e le istruzioni, come le inseriresti normalmente) e la tabella elenca ogni voce del lorebook e dei suoi sotto-lorebook, nell'ordine in cui vengono usate:

- un segno di spunta verde per le voci che verrebbero usate e un segno grigio per le altre;
- il lorebook della voce, il suo **Ordine**, il **Supera** (vuoto per le voci attivate dal testo, `1`, `2`... per le voci portate dentro da [altre voci](#entries-brought-in-by-other-entries));
- il **Motivo**: nessun filtro, filtro trovato nel testo, filtro trovato nel contenuto di un'altra voce (con il filtro e la voce), oppure il motivo per cui non viene usata: disattivata, la parte è fuori dal suo intervallo, un tag negativo del libro, al libro manca un tag della voce, la condizione è falsa o non può essere decisa, il filtro non è stato trovato.

Anche i tag contano, quindi scegli un libro in **Tag del libro** per usarne i tag (senza un libro, nessun tag viene considerato). Quel libro fornisce anche le parti della sua storia per le voci che [guardano nella storia](#looking-into-the-story) (senza un libro, cercano solo nel testo) e il numero della parte successiva per le voci [limitate ad alcune parti](#limiting-an-entry-to-some-parts) (senza un libro, il loro intervallo non viene controllato). Il testo che digiti sono le istruzioni del turno per le [condizioni](#a-condition-for-an-entry); con un libro, vengono usate anche le sue variabili e il punto di vista, i personaggi presenti e la scena dell'ultima parte (i comandi di una condizione non vengono eseguiti nell'anteprima). Sotto il testo, una riga indica quante voci verrebbero usate e circa quanti token di lore rappresentano. Viene usata la profondità di scansione del lorebook.

Nell'[editor a schermo intero](lorebooks.md#editing-in-full-screen) l'occhio si trova anche nella riga del nome di ogni scheda di voce: l'anteprima si apre per quella voce, la sua riga viene selezionata e una riga sopra la tabella indica se verrebbe usata e perché, informazione che cambia mentre digiti. Cambia il filtro nella scheda, poi premi **Aggiorna** nell'anteprima per vedere l'effetto.

![Anteprima dell'attivazione di un lorebook](../../images/lorebook-preview-activation.png)

L'anteprima applica solo le regole di Marginalia: il testo è quello che digiti (non il modello del prompt utente che lo circonda), e ciò che una [estensione](../extensions/index.md) modifica mentre una parte viene generata non ne fa parte.

<a id="order-and-insertion"></a>

## Ordine e inserimento

Le voci attivate vengono inserite nel prompt ordinate per **Ordine**, dal più basso. Le voci con lo stesso ordine mantengono l'ordine in cui sono state create. Le voci di tutti i lorebook inclusi vengono ordinate insieme.

**Modalità di inserimento** decide dove va una voce:

| Modalità | Dove |
|---|---|
| **Nel blocco del lore** | Nel blocco `{%{{{backgroundLore}}}%}` del [modello principale](../books/prompts.md#master-template) - *Contesto del mondo e del lore* nel modello predefinito, nel messaggio di sistema. Usalo per la conoscenza di sfondo. |
| **Prima del prompt utente** | Subito prima della richiesta per la parte, alla fine del prompt. Il modello presta più attenzione a questa voce. Usala con parsimonia, per ciò che deve influenzare proprio questa parte (lo stato attuale di un personaggio, una regola per la scena). |

Le voci sono separate da una riga vuota. Il lore conta ai fini del [limite di contesto](../protocols.md#limits); con molte voci lunghe, entra nel prompt meno storia.

<a id="templates-in-entries"></a>

## Modelli nelle voci

Il contenuto di una voce è un modello, come i [prompt del libro](../books/prompts.md#templates). Può usare:

| Variabile | |
|---|---|
| `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` | Le istruzioni per la parte. |
| `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}` | Le impostazioni narrative del libro. |

e tutte le [macro](../templates/macros.md). Ad esempio, una voce che si adatta al personaggio del punto di vista:

{%{
```handlebars
Varro {{#povCharacter}}è visto con gli occhi di {{.}}: {{/povCharacter}}un ufficiale severo e segnato da una cicatrice.
```
}%}

Tutte le voci attivate di una parte condividono le loro variabili nell'ordine di attivazione: una voce può usare `setvar` per impostare un valore che le voci successive nell'ordine (e il modello principale) leggono con `getvar`.

Un errore di sintassi nel contenuto viene mostrato sotto il campo. La voce viene comunque salvata; finché non viene corretta, il suo contenuto viene usato come testo semplice, senza elaborare il modello.

<a id="tips"></a>

## Suggerimenti

- Tieni le voci brevi e concrete; il modello non ha bisogno di prosa, ha bisogno di fatti.
- Una voce per personaggio, luogo o concetto, così ciascuna può essere attivata da sola.
- Usa le voci sempre attive per il nucleo del mondo, i filtri per il cast e i luoghi, e i tag per condividere un lorebook tra più libri.
- Controlla cosa è stato attivato con **Mostra il prompt** su una parte generata, oppure prova un testo prima di scrivere con [Anteprima dell'attivazione](#preview-activation).
- Usa la profondità di scansione con parsimonia: una sola passata basta per la maggior parte dei libri, e ogni passata può portare più lore del previsto.
