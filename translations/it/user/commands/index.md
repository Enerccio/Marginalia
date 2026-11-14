---
label: Comandi
order: 945
icon: terminal
---

# Comandi

I comandi sono piccole azioni che si eseguono digitandole, invece di cliccare tra i menu: cercare qualcosa nella storia o
nei lorebook, cercare e sostituire, fare un backup. [Estensioni](../extensions/index.md) aggiungono comandi propri,
per esempio per generare una voce di lore o per inventare nomi. Gli stessi comandi sono quelli con cui lavora un [agente](../agents.md), quindi un
comando fa una cosa chiara e dice che cosa fa: lo si può leggere mentre lo si scrive.

La **riga dei comandi** è il campo accanto al menu sotto la storia, nel [editor della storia](../books/story-editor.md).

<a id="running-a-command"></a>

## Eseguire un comando

1. Clicca nella riga dei comandi e digita `/`. L'elenco mostra tutti i comandi, ciascuno con il suo nome e una breve
   descrizione. Digitando di più l'elenco si restringe: il filtro guarda il nome del comando, il suo titolo e la sua descrizione.
2. Scegli il comando con il mouse, oppure con i tasti freccia e **Invio**. Un comando che non richiede nulla viene eseguito subito; per
   uno che accetta argomenti, `/name ` viene scritto nel campo e si continua con gli argomenti.
![L'elenco dei comandi, aperto digitando una barra](../../images/command-list.png)

3. Digita gli argomenti e premi **Invio**, oppure clicca il pulsante di riproduzione accanto al campo, per eseguirlo. Il campo viene
   svuotato non appena il comando parte; un testo che non è un comando resta, così lo si può correggere.

Il testo che non inizia con `/` non è un comando, e non viene eseguito nulla. Cliccare fuori dal campo o premere **Esc**
non esegue mai nulla, quindi non si può avviare un comando per sbaglio.

<a id="the-help"></a>

## L'aiuto

Mentre digiti, si apre una piccola finestra sopra il campo. Copre una parte della storia ma non sposta nulla, e si chiude quando
lasci il campo o esegui il comando. Mostra:

- che cosa fa il comando,
- le forme del comando (vedi [Forme](#forms-of-a-command)) che corrispondono a quanto hai digitato finora, con l'argomento che stai
  digitando evidenziato e la sua descrizione,
- che cosa restituisce il comando,
- un suggerimento quando hai digitato più argomenti di quanti il comando ne accetta, o quando nessun comando ha quel nome.

![L'aiuto di /search-replace-lore-entries mentre si digita il suo secondo argomento](../../images/command-help.png)

Mentre stai ancora digitando il nome, l'elenco dei comandi è l'aiuto.

<a id="arguments"></a>

## Argomenti

Gli argomenti sono separati da spazi, e più spazi di seguito contano come uno. Per mettere uno spazio *dentro* un argomento, ci sono
tre modi:

| Digiti                       | Il comando riceve               | Perché                                     |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | un argomento, `two words`       | `\ ` (barra rovesciata, spazio) è uno spazio |
| `/search-parts "two words"`  | un argomento, `two words`       | le virgolette doppie tengono tutto insieme |
| `/search-parts say\ "hi"`    | un argomento, `say hi`          | le virgolette possono iniziare a metà di un argomento |

Gli altri caratteri speciali:

| Digiti                        | Ottieni                    |
|-------------------------------|----------------------------|
| `\\`                          | una barra rovesciata       |
| `\"`                          | una virgoletta doppia, con o senza virgolette attorno: `"say \"hi\""` diventa `say "hi"` |
| `""`                          | un argomento vuoto         |

Una barra rovesciata davanti a qualsiasi altro carattere resta com'è, quindi `C:\books\story` viene passato invariato. Se apri una virgoletta e
non la chiudi mai, tutto fino alla fine della riga appartiene a quell'argomento.

Gli argomenti sono testo. Un comando che vuole un numero, un sì/no o una di poche opzioni converte il testo da solo; se il testo non
corrisponde, il comando non viene eseguito e una notifica lo segnala.

<a id="forms-of-a-command"></a>

## Forme di un comando

Un comando può accettare argomenti diversi: `/list-parts` senza nulla, e `/list-parts 2` con un argomento
sono due **forme** dello stesso comando. Marginalia sceglie la forma in base al numero di argomenti digitati e a come sono fatti. Se nessuna
forma corrisponde, una notifica dice che il comando non accetta questi argomenti, e l'aiuto mostra le forme che ha.

<a id="the-result"></a>

## Il risultato

Quando un comando restituisce qualcosa, il risultato compare in una finestra sopra la riga dei comandi. Chiudila con la croce, con **Esc**,
oppure prosegui: scompare quando digiti qualcosa di nuovo o esegui un altro comando. Un comando che non restituisce nulla non mostra
alcuna finestra.

![Il risultato di /take-backup sopra la riga dei comandi](../../images/command-result.png)

Se un comando fallisce, o non esiste, ottieni una notifica con il motivo.

<a id="commands-that-take-time"></a>

## Comandi che richiedono tempo

La maggior parte dei comandi risponde subito. Un comando che ha bisogno di tempo mostra una finestra con una barra animata e un pulsante **Interrompi**. La
finestra sta di lato e la pagina resta utilizzabile; il suo ⌄ la ripiega nel
[pannello delle esecuzioni](../books/story-editor.md#the-panel-of-the-runs), dove il comando è elencato con ciò che il modello scrive, su
tutte le tue pagine. Sotto la barra il comando può dire che cosa sta facendo in questo momento e, in caratteri più piccoli, il dettaglio del passo. **Interrompi** annulla il comando; dovrebbe tornare subito. I comandi che fanno scrivere al modello un riassunto o delle note, come `/generate-annotation`, sono di questo tipo.

`/next`, `/swipe` e `/regenerate` digitati nella barra dei comandi della storia non hanno una finestra di questo tipo: la nuova parte scorre nella storia come quando premi il pulsante tondo, il pulsante tondo lo interrompe, e la storia resta bloccata come per una generazione qualsiasi (puoi continuare a lavorare nella lore, sulle altre parti e nelle schede laterali). Il risultato del comando appare sopra la barra quando la parte è completata. Fino ad allora la barra rifiuta un secondo comando di questo tipo (conservi il testo e puoi eseguirlo più tardi); gli altri comandi funzionano.

![La finestra di un comando in esecuzione](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Comandi che modificano i dati

Ogni comando dichiara che cosa fa ai dati già esistenti, e la barra dei comandi mostra questa informazione come una piccola etichetta accanto al comando, nell'elenco e nell'aiuto:

- **Distruttivo** (rosso) - il comando cambia o rimuove dati esistenti, come i comandi di ricerca e sostituzione che riscrivono il libro. Passa con il puntatore sull'etichetta per vedere la spiegazione.
- **Non distruttivo** (verde) - il comando legge soltanto dati o ne aggiunge di nuovi, come `/take-backup`, che aggiunge un backup e non cambia altro.

L'etichetta è un'informazione per te; non blocca il comando quando lo esegui. Un [agente](../agents.md) si attiene ad essa: non gli viene
affidato un comando distruttivo, oppure deve chiederti prima, oppure lo esegue, secondo quanto impostato per quell'agente. Un comando che chiede da solo una conferma è descritto più avanti.

<a id="commands-that-ask-first"></a>

## Comandi che chiedono prima

Un comando che cambia molto può chiedere prima di agire. I comandi di ricerca e sostituzione non hanno un'anteprima, quindi dopo la ricerca mostrano quante corrispondenze hanno trovato in quanti elementi e che cosa verrebbe sostituito con che cosa, e solo **Sì** cambia il libro; **No** lascia tutto com'era. La domanda resta finché non rispondi.

![La domanda di /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Comandi inclusi in Marginalia

| Comando                              | Cosa fa                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Cerca e sostituisci nella storia del libro aperto, come la finestra **Cerca e sostituisci** della scheda della storia. Le opzioni sono `true`/`false`, in quest'ordine, e ciascuna omessa assume il valore predefinito di quella finestra ([regex](../regular-expressions.md) no, distingui maiuscole/minuscole sì, riassunti sì, tutti i rami no, riconvalida sì); per impostarne una, indicate prima anche quelle precedenti. `maxHits` rifiuta una sostituzione che trova più corrispondenze di quel numero, e con `dryRun` `true` non viene modificato nulla: la risposta conta le corrispondenze e ne mostra alcune, ed è così che un agente controlla prima di sostituire. Non c'è un'anteprima: dopo la ricerca viene chiesto se sostituire il numero di corrispondenze trovate, e solo **Sì** modifica il libro. La sostituzione non può essere interrotta. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Lo stesso per le voci (nome, testo e commento) del lorebook del libro aperto. |
| `/take-backup`                       | Crea un backup del libro aperto; compare nella scheda **Backup** del libro. Indica che non c'è nessun libro aperto quando non ce n'è uno. |
| `/get-book-info` | Il nome, le impostazioni, il modello, i totali (parti, parole, token) e i tag del libro aperto. |
| `/list-parts [branch] [offset] [limit]` | Le parti di un ramo: handle, posizione, dimensione, swipe, stato del riassunto e una breve anteprima. |
| `/get-part <part> [branch] [offset] [length] [field]` | Il testo di una parte a blocchi (oppure il suo `instructions`, `scene`, `pov`, `present` o `reasoning`) e da che cosa è stata generata. |
| `/list-swipes <part> [branch]` | Le alternative (swipe) di una parte, con il numero di ramo che mostra ciascuna. |
| `/list-branches [offset] [limit]` | I rami del libro, numerati, con indicato da dove si separa ciascuno dagli altri. |
| `/get-part-metrics <part> [branch]` | Token, tempi, modello e protocollo di una parte. |
| `/show-prompt <part> [branch] [offset] [length]` | Il prompt da cui è stata generata una parte, così come è stato memorizzato. |
| `/search-parts <query> [limit]` | Ricerca a testo pieno nelle parti di tutti i rami, con estratti. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Quante volte e dove compaiono nomi o parole (predefiniti: nessuna regex, senza distinzione tra maiuscole e minuscole, solo parti, ramo attivo). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Le frasi che differiscono tra due parti; `0` come `b` confronta la parte `a` con il testo prodotto per primo dal modello. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Token (con il tokenizzatore del modello del libro), parole e caratteri di un testo, oppure di una parte quando il testo è il suo handle. |
| `/list-summaries [branch]` | I riassunti di un ramo, le parti che coprono e se sono ancora validi. |
| `/get-summary <part> [branch]` | Il riassunto di una parte. |
| `/check-summaries [branch]` | I riassunti non più aggiornati e le parti modificate che li hanno resi tali. |
| `/get-template-variables <scope> [part] [branch]` | Le variabili di template del libro (`global`) o di una parte (`part`). |
| `/list-lorebooks` | I lorebook del libro aperto (la radice e i suoi sottolibri) con i relativi numeri, il numero di voci, i tag e i sottolibri. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Le voci di un lorebook con le loro impostazioni e un'anteprima; il filtro mantiene le voci il cui nome o tag contiene il testo indicato. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Una voce: impostazioni, chiavi, attivazione (ricorsione, contesto, intervallo di parti, condizione), tag, commento e il suo testo a blocchi. |
| `/search-lore <query> [regex] [limit]` | Le voci di tutti i lorebook del libro il cui nome, testo o commento contiene la query, con estratti. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Quali voci userebbe una generazione per un testo, oppure per un turno come quello che ha prodotto una parte, e perché (nessun filtro, filtro trovato nel testo, oppure nel contenuto di un'altra voce quando il lorebook ha una [profondità di scansione](../lorebooks/entries.md#entries-brought-in-by-other-entries), oppure non usata perché disattivata, una decisione dei tag del libro, oppure il filtro non è nel testo), con il testo del lore e i suoi token. Il testo può essere l'handle di una parte. |
| `/list-annotations [status [part [offset [limit]]]]` | Le [annotazioni](../books/annotations.md) del libro o di una parte: quelle aperte, a meno che `status` non indichi `resolved`, `dismissed`, `orphaned` o `all`. |
| `/get-annotation <annotation>` | Una annotazione per intero, con il testo attorno al suo passaggio. |
| `/get-book-statistics [range] [side]` | Che cosa è stato fatto sul libro aperto giorno per giorno (token, parole, parti, modifiche, tempo, dimensione): vedi [Statistiche di scrittura](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | Lo stesso per tutti i vostri libri, per giorno o per libro. Non richiede un libro aperto. |

<a id="reading-the-story"></a>

### Leggere la storia

I comandi sopra che leggono il libro (da `/get-book-info` a `/get-template-variables`) non cambiano nulla; rispondono in testo JSON, quindi anche un agente può usarli. Ogni riga di una risposta ha un **`handle`**: l'identificatore della parte (o del lorebook, o della voce), che non cambia quando parti vengono aggiunte, spostate o eliminate. Una parte si indica con il suo handle, oppure con la sua **posizione** su un ramo, essendo la prima parte della storia `1`; un ramo con l'handle di una qualsiasi sua parte, oppure con il suo **numero** a partire da `/list-branches` (i numeri seguono la creazione delle estremità dei rami, quindi cambiano quando un ramo viene aggiunto o eliminato). Posizioni e numeri sono più facili da digitare, mentre gli handle sono ciò che un agente dovrebbe usare, e i comandi che modificano la storia accettano solo handle. Senza un ramo, i comandi lavorano sul ramo attivo. Gli elenchi sono suddivisi in pagine con `offset` e `limit`, i testi lunghi sono tagliati in blocchi (`offset`, `length`) e la risposta indica la lunghezza totale. Come per gli altri comandi, le opzioni sono posizionali: per impostarne una, indicate prima anche quelle precedenti.

I comandi che leggono i lorebook (da `/list-lorebooks` a `/preview-lore-activation`) funzionano allo stesso modo e raggiungono solo i lorebook del libro aperto: il lorebook radice e i suoi sottolibri. Un lorebook si indica con il suo **handle** o con il suo **numero** a partire da `/list-lorebooks` (la radice è `1`), una voce con il suo **handle** o con il suo **numero nel lorebook**, secondo l'ordine del lorebook (la prima voce è `1`); i numeri cambiano quando voci o sottolibri vengono aggiunti o rimossi. Senza un lorebook indicato si usa il lorebook radice.
<a id="changing-the-story-and-the-lore"></a>

### Modificare la storia e il lore

Questi comandi modificano il libro aperto (oppure la copia di lavoro di un agente, dove finiscono le modifiche dell'agente). Una parte, un lorebook o una voce si indicano sempre con il loro **handle**, mai con una posizione o un numero: le posizioni cambiano quando si aggiunge o si elimina qualcosa, e una posizione giusta al momento della lettura potrebbe corrispondere a un'altra parte al momento della scrittura. Le opzioni, come prima, sono posizionali. Un comando che rimuove testo o dati è `DESTRUCTIVE` e chiede conferma (per un agente vale invece la sua modalità distruttiva); aggiungere e modificare no. Una modifica della storia può rendere obsoleti i riassunti (un riassunto porta con sé l'hash della storia che descrive): la risposta li elenca e indica cosa fare, `/revalidate-summaries` se sono ancora corretti, `/regenerate-summary` se non lo sono.

| Comando | Cosa fa |
|---------|---------|
| `/edit-part <part> <old> <new> [field]` | Sostituisce `old` con `new` nel testo di una parte; `old` deve essere trovato esattamente una volta, altrimenti non cambia nulla e l'errore indica quante volte è stato trovato. `field` modifica invece i caratteri `instructions`, `scene`, `pov` o `present` del turno. |
| `/replace-part-text <part> <text> [field]` | Sostituisce l'intero testo di una parte. Chiede prima conferma. |
| `/append-to-part <part> <text> [separator]` | Aggiunge testo alla fine di una parte, dopo una riga vuota a meno che `separator` non dica diversamente. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Cambia da cosa è stata generata una parte, che una nuova generazione riutilizza; un testo vuoto svuota un campo, un campo omesso resta invariato. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Scrive una nuova parte dopo l'ultima parte del ramo attivo, oppure dopo `parent` (un nuovo ramo); diventa la fine del ramo attivo. |
| `/insert-part <text> <part> [before]` | Inserisce una nuova parte nella storia subito dopo `part` (oppure subito prima, quando `before` è `true`). Tutto ciò che seguiva quel punto, tutti i rami compresi, segue ora la nuova parte; non si elimina né si rigenera nulla, quindi le altre parti conservano i loro riassunti e dati e scendono di una posizione. I riassunti che coprivano quel punto diventano obsoleti (la risposta indica quali). Alla fine del ramo equivale a `/create-part`. |
| `/branch-part <part>` | Copia una parte (e il suo riassunto) accanto a essa, come nuovo swipe che avvia un ramo, e rende la copia la fine attiva. |
| `/delete-part <part>` | Elimina una parte e il suo riassunto; le parti che la seguivano restano. Chiede prima conferma. |
| `/switch-branch <part>` | Rende attivo il ramo che passa per una parte. |
| `/set-summary <part> <text>` | Sostituisce il testo del riassunto di una parte. |
| `/remove-summary <part> [unwind]` | Rimuove il riassunto di una parte (`unwind` ripristina ciò che un meta-riassunto aveva sostituito). Chiede prima conferma. |
| `/revalidate-summaries [part]` | Dichiara che i riassunti obsoleti descrivono ancora la storia (tutto il ramo attivo, oppure solo quella parte). Usarlo solo dopo averlo verificato. |
| `/regenerate-summary <part>` | Fa rigenerare dal modello il riassunto di una parte. Lento, consuma token. |
| `/create-meta-summary <from> <to>` | Fa fondere dal modello in un unico riassunto quelli dalla parte `from` (la più recente) fino a `to`. Lento, consuma token. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Fa scrivere al modello la parte successiva, come il pulsante **New turn**. Lento, consuma token; una sola generazione di un libro alla volta, le altre attendono. `profile` indica uno dei profili di modello di un agente. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Fa scrivere al modello un'altra versione dell'ultima parte di un ramo. Ciò che si omette viene preso dalla parte. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Fa riscrivere al modello l'ultima parte di un ramo, sostituendone il testo. Chiede prima conferma. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Fa riscrivere al modello una parte qualsiasi secondo le tue istruzioni (vedi [Riscrivere una parte](../books/story-editor.md#redrafting-a-part)) e ne sostituisce il testo. `prompt` è un prompt di riscrittura da usare al posto di quello del libro, `profile` un profilo di modello di un agente, `apply` `false` restituisce solo il testo. `DESTRUCTIVE`; chiede prima conferma. Lento, consuma token. |
| `/impersonate [instructions] [outline] [prompt]` | Fa redigere al modello le istruzioni del turno successivo al posto tuo (vedi [Redigere le istruzioni](../books/story-editor.md#drafting-the-instructions)) e ne restituisce il testo; non si modifica nulla. `instructions` è l'argomento della parte (vuoto prosegue la storia dove porta), `outline` un piano della storia per il modello, `prompt` un prompt di impersonate da usare al posto di quello del libro. Solo per te alla barra dei comandi: un [agente](../agents.md) non lo riceve. Lento, consuma token. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Modifica le impostazioni descrittive del libro; solo i valori che indichi. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Sostituisce i template dei prompt del libro. Ciascuno viene prima controllato. `DESTRUCTIVE`: un template difettoso compromette ogni generazione successiva. |
| `/set-book-tags [add] [remove]` | Aggiunge e rimuove i tag del libro (separati da virgola). |
| `/set-book-history-limit [maxVersions]` | Imposta quante versioni di una parte conserva la [cronologia](../books/version-history.md#how-many-versions-are-kept) del libro (`0` da ora non ne conserva nessuna); senza numero il libro non impone alcun limite. Valgono ancora i limiti delle cartelle, dell'utente e dell'amministratore. Non elimina alcuna versione. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Aggiunge una voce a un lorebook (la radice se non ne è indicato uno; un libro che non ha un lorebook ne riceve uno, con il nome del libro). `keys` è un elenco di parole chiave separate da virgole; `filteringMode` è `KEYWORDS_OR` (predefinito), `KEYWORDS_AND`, `TEXT` o `REGEX`. Un problema del template o dell'espressione regolare viene salvato e segnalato. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Modifica i campi che indichi. |
| `/delete-lore-entry <entry>` | Elimina una voce. Chiede prima conferma. |
| `/move-lore-entry <entry> <lorebook>` | Sposta una voce in un altro lorebook del libro. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Modifica i tag e i tag negativi di una voce (separati da virgola). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Modifica quando viene usata una voce: `preventRecursion`, `recursionOnly` e `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` o `INCLUDE_PARTS`) con `activationParts`, l'intervallo di parti `fromMessage` / `toMessage` (0 rimuove un limite) e il template `condition` (testo vuoto lo rimuove). Un valore indicato come testo vuoto resta invariato (tranne `condition`). |
| `/create-lorebook <name> [parent]` | Crea un lorebook che è un sottolibro di `parent` (la radice se non ne è indicato uno; un libro che non ha ancora un lorebook ne riceve prima uno). |
| `/set-lorebook <lorebook> [name] [enabled]` | Rinomina un lorebook oppure lo attiva o disattiva. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Modifica quali lorebook sono contenuti in un lorebook; un lorebook non può contenere sé stesso. Per un [agente](../agents.md), `/add-subbook` ripristina anche un lorebook che aveva eliminato o rimosso in precedenza dal suo genitore (lo indica con l'handle che aveva), insieme alle sue voci. |
| `/delete-lorebook <lorebook>` | Elimina un lorebook con le sue voci (non la radice). Chiede prima conferma. |
<a id="annotations"></a>

### Annotazioni

I comandi che leggono le [annotazioni](../books/annotations.md) (`/list-annotations`, `/get-annotation`) rispondono in JSON, come gli altri comandi di lettura. Un'annotazione si indica con il suo **identificatore**, una parte con il suo identificatore per i comandi che modificano qualcosa. Questi comandi modificano solo le note, mai la storia, quindi solo l'eliminazione chiede conferma. Un agente può usarli tutti; le sue note arrivano al tuo libro quando [rivedi il risultato](../books/annotations.md#annotations-and-agents).

| Comando | Cosa fa |
|---------|---------|
| `/create-annotation <part> <text> [quote [type]]` | Aggiunge una nota a una parte. `quote` è il testo del passaggio, copiato dalla parte: la nota si colloca su quel passaggio e lo segue quando il testo viene modificato. Senza citazione, o con una che non è nel testo, la nota riguarda l'intera parte (la risposta lo indica). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Modifica la nota, il tipo o se il modello riceve la nota; un testo o un tipo vuoto resta invariato. |
| `/resolve-annotation <annotation> [status]` | Imposta lo stato: `resolved` (predefinito), `dismissed` o `open`. |
| `/reattach-annotation <annotation> [quote]` | Sposta una nota sul passaggio che la citazione trova nella sua parte; senza citazione la nota riguarda l'intera parte. |
| `/delete-annotation <annotation>` | Elimina una nota (può essere ripristinata dal cestino). Chiede conferma. |
| `/generate-annotation <part> [profile]` | Fa rivedere la parte al modello come [Annota con IA](../books/annotations.md#letting-a-model-annotate) e aggiunge come note tutto ciò che trova (fonte *IA*; una citazione che non è nel testo crea una nota sull'intera parte). Non viene chiesta conferma: le note sono l'unica modifica, e possono essere risolte, modificate o eliminate. Rivede con il modello del libro; un agente usa il proprio modello quando *Scrivi il libro con il modello dell'agente* è attivo (come fanno i comandi del Lore Generator), `profile` è un profilo modello di un agente. Si esegue da solo, richiede tempo e consuma token. |

<a id="the-planner"></a>

### Il planner

I comandi per il [planner](../books/story-editor.md#the-planner-tab) del libro. Una scena è indicata dal suo **numero** nell'elenco (da 1; `/get-planned-scenes` le mostra); i numeri cambiano quando una scena viene spostata o eliminata, quindi rileggi l'elenco dopo ogni modifica. Modificano solo il planner, mai la storia. Aggiungere, spostare e segnare una scena come completata non chiedono conferma; cambiare il testo di una scena ed eliminarne una sono operazioni distruttive (a un [agente](../agents.md) viene chiesta conferma come per gli altri comandi distruttivi).

| Comando | Cosa fa |
|---------|---------|
| `/get-planned-scenes` | Le scene con numero, titolo, ambientazione, personaggio POV, personaggi presenti, istruzioni e se sono completate. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Aggiunge una scena alla fine dell'elenco. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Cambia il titolo e i dettagli indicati; ciò che viene omesso resta, un testo vuoto lo cancella. |
| `/set-planned-scene-done <scene> [done]` | Segna la scena come scritta (`done` è `true` per impostazione predefinita) o non scritta. |
| `/move-planned-scene <scene> <to>` | Sposta la scena nella posizione `to` (1 è la prima; se è troppo grande, va all'ultima posizione). |
| `/delete-planned-scene <scene>` | Elimina la scena. |

I comandi che fanno scrivere o rivedere al modello richiedono tempo: `/generate-annotation` e i due riassunti mostrano la finestra **Interrompi**, mentre `/next`, `/swipe` e `/regenerate` scrivono in streaming nella storia (vedi [Comandi che richiedono tempo](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Comandi forniti dalle estensioni

Questi compaiono nell'elenco finché l'estensione è caricata; ognuno è descritto nella documentazione della propria estensione.

| Comando | Estensione | Cosa fa |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | I tuoi prompt di generazione salvati, o il testo di uno di essi. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Genera il testo di una voce di lore con il lore del lorebook, lo compatta (a meno che `compact` sia `false`) e lo restituisce. Non salva nulla. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Compatta un testo generato in una voce di lore densa e la restituisce. Non salva nulla. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Le categorie di nomi inventati, e i nomi di una di esse. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Gli elenchi di nomi reali, e i nomi scelti da uno di essi. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | I tipi di luoghi reali, una ricerca per nome, tipo e paese, i luoghi attorno a un punto e la distanza tra due luoghi. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Gli archi e i capitoli della scaletta. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Aggiunge, modifica, sposta ed elimina un arco (`<arc>` è il suo numero). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | Lo stesso per i capitoli di un arco. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | I generatori della scheda Arcs. Restituiscono il risultato e lo aggiungono alla scaletta solo con `save`. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | I personaggi del libro, e uno con la sua nota e le sue relazioni. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Aggiunge, modifica ed elimina un personaggio. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Le relazioni tra personaggi. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Le pagine delle note del libro (non quelle nascoste agli assistenti), una con il suo testo e i suoi link, e una ricerca. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Aggiunge, accoda testo, sostituisce, modifica ed elimina una pagina; `note-text` è pensato per la macro {%{`{{command::note-text::Title}}`}%}. |

I comandi del Lore Generator, del Name generator, del Name provider e del Place provider si limitano a leggere: quelli che generano restituiscono testo e lasciano a te, o all'agente che li ha chiamati, il compito di inserirlo in una voce. I comandi di Arc Outline e Character Tracker modificano anche la scaletta e i personaggi del libro aperto; solo i loro comandi `delete` sono distruttivi. Anche le Story Notes modificano le pagine del libro aperto; lì anche `update-note` e `edit-note` sono distruttivi, perché sostituiscono testo esistente.

<a id="limits"></a>

## Limiti

- La riga di comando si trova nella scheda story del layout desktop. La [vista telefono](../phone.md) non la ha ancora.
- Un comando viene eseguito con il tuo account, nella tua sessione, e lavora con il libro che hai aperto.
