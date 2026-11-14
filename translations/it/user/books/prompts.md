---
label: Prompt del libro
order: 70
---

# Prompt del libro

La scheda **Prompt** di un libro contiene i testi con cui Marginalia costruisce il prompt: due template, le impostazioni narrative e i prompt di riassunto e di riscrittura. Determinano lo stile del libro molto più delle impostazioni del modello.

![La scheda Prompt](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Predefiniti e valori del libro

Ogni campo è facoltativo. Un campo vuoto eredita il valore: dalla [cartella](folders.md) più vicina sopra il libro che lo imposta, altrimenti dal predefinito in **Impostazioni → Template** (vedi [Account e impostazioni](../account.md)), e se anche questo è vuoto, dal predefinito integrato, nella [lingua dei prompt](../account.md#prompt-language) delle tue impostazioni. Il valore ereditato è mostrato in grigio nel campo vuoto.

Quindi imposta in *Impostazioni* ciò che condividono tutti i tuoi libri, nella cartella ciò che condivide una serie, e nella scheda *Prompt* del libro solo ciò che è specifico di quel libro. Le modifiche vengono salvate mentre scrivi.

<a id="inherited-settings"></a>

### Impostazioni ereditate

Sotto un campo vuoto una riga indica da dove proviene il suo valore, così è chiaro perché il libro scrive in un certo modo:

- *Ereditato dalla cartella "Sci-fi"* - una [cartella](folders.md#where-the-settings-of-a-book-come-from) lo imposta (viene indicata la più vicina sopra il libro),
- *Ereditato dalle tue impostazioni* - il valore di *Impostazioni → Template*,
- *Predefinito integrato* - nessuno lo imposta.

![La scheda Prompt di un libro in una cartella](../../images/book-prompts-inherited.png)

Gli stessi campi sono nelle impostazioni di una cartella, con le stesse righe. Un testo vuoto conta come non impostato. Poiché la ricerca viene fatta quando il prompt viene costruito, spostare il libro in un'altra cartella, o cambiare cartella, modifica la parte successiva che generi.

<a id="settings-of-extensions"></a>

### Impostazioni delle estensioni

Un'[estensione](../extensions/index.md) può aggiungere impostazioni che libri e cartelle ereditano allo stesso modo (per esempio il mezzo con cui una chat sul libro lo tratta). Si trovano in una sezione **Impostazioni dell'estensione** sotto i prompt, nella scheda *Prompt* di un libro e nelle impostazioni di una cartella, con un **pannello richiudibile per ogni estensione** (tutti chiusi, tranne il primo che ha qualcosa impostato, così un valore che hai inserito non resta nascosto). Funzionano come i campi sopra: un campo vuoto eredita dalle cartelle sopra il libro, poi dalle impostazioni dell'estensione in *Impostazioni → Impostazioni dell'estensione*, poi dal suo predefinito, e la riga sotto il campo indica quale. I template dei [formati di esportazione](export.md#title-page-template) (frontespizio, parte iniziale, dichiarazione di autore) si trovano nella stessa sezione, con un pannello per formato. Un valore impostato per un'estensione resta con il libro o la cartella anche quando l'estensione non è caricata.

Ogni campo ha un popover dei suggerimenti con **Inserisci il template predefinito**, che copia il predefinito nel campo come punto di partenza per la tua versione. I campi dei template elencano anche le **Variabili di template disponibili** e le **Macro disponibili**.

![Il popover dei suggerimenti di un campo template](../../images/template-hints.png)

<a id="the-fields"></a>

## I campi

| Campo | Usato in | |
|---|---|---|
| **Template principale** | messaggio di sistema | La cornice del prompt: il ruolo del modello, e dove vanno le informazioni del mondo, le impostazioni narrative e i riassunti. Un template. |
| **Punto di vista (POV)** | template principale | La voce narrante, per esempio *Prima persona*, *Terza persona limitata*. Testo semplice. |
| **Tempo verbale** | template principale | Per esempio *Passato*, *Presente*. Testo semplice. |
| **Stile** | template principale | Linee guida di stile e tono per la prosa. Testo semplice. |
| **Prompt utente predefinito** | ultimo messaggio | La richiesta della parte successiva, costruita dalle [istruzioni del turno](story-editor.md#writing-the-next-part). Un template. |
| **Prompt di riassunto predefinito** | riassunti | La richiesta di un [riassunto](summaries.md). Un template. |
| **Prompt di meta-riassunto predefinito** | meta-riassunti | La richiesta di un [meta-riassunto](summaries.md#meta-summaries), che unisce riassunti. Un template. |
| **Prompt di sistema per riscrivere** | riscritture | Il messaggio di sistema di una [riscrittura](story-editor.md#redrafting-a-part): le direttive, la narrazione e i riassunti. Un template. |
| **Prompt di riscrittura** | riscritture | L'ultimo messaggio di una riscrittura, la richiesta vera e propria: le informazioni che le voci del lorebook danno ora alla parte (così una modifica di una voce arriva alla riscrittura; le informazioni con cui la parte era stata scritta solo quando il libro non ha un lorebook), la storia dopo la parte, la parte e le istruzioni. Un template. |
| **Prompt per riscrivere più breve**, **Prompt per riscrivere più lungo** | riscritture | La frase aggiunta dopo le tue istruzioni quando la finestra di [riscrittura](story-editor.md#redrafting-a-part) è impostata su **Più breve** o **Più lungo**. Testo semplice, non un template. Vuoto significa il testo integrato. |
| **Prompt di sistema per annotare** | annotazioni | Il messaggio di sistema di una richiesta al modello di [annotare](annotations.md#letting-a-model-annotate) una parte. Un template. |
| **Prompt di annotazione** | annotazioni | L'ultimo messaggio della richiesta di annotazione. Un template. |
| **Annotate Instructions** (*Cosa cercare*) | annotazioni | Ciò che il modello cerca quando annota (*segnala incoerenze di continuità*, *dialoghi deboli*), il testo di `{%{{{annotateInstructions}}}%}` del prompt di annotazione. Testo semplice. Impostalo qui, in una cartella o in *Impostazioni → Template*, e la finestra *Annota con l'IA* parte da questo. |
| **Prompt di impersonificazione** | istruzioni di stesura | La richiesta a un modello di [stendere le istruzioni](story-editor.md#drafting-the-instructions) del turno successivo al posto tuo. Un template. |

*Punto di vista*, *Tempo verbale* e *Stile* vengono inseriti nel template principale così come sono; le macro non funzionano in essi.

Un template con un errore di sintassi non viene salvato; un avviso mostra l'errore.

<a id="templates"></a>

## Template

I template sono testo con segnaposto tra doppie parentesi graffe. Ogni template ha le sue variabili:

| Template | Variabili |
|---|---|
| Template principale | `{%{{{backgroundLore}}}%}` (voci del lorebook attivate), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (le note che hai dato al modello) |
| Prompt utente | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Prompt di riassunto | `{%{{{backgroundLore}}}%}` (le informazioni che le voci del lorebook danno ora alla parte), `{%{{{text}}}%}` (le parti da riassumere) |
| Prompt di meta-riassunto | `{%{{{backgroundLore}}}%}` (come sopra), `{%{{{summaryBlocks}}}%}` (i riassunti da unire) |
| Prompt di sistema per riscrivere, Prompt di riscrittura | `{%{{{text}}}%}` (la parte), `{%{{{redraftInstructions}}}%}` (ciò che vuoi cambiare), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, e il turno da cui la parte è stata scritta: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, e `{%{{{annotations}}}%}` (le tue note aperte sulla parte, vuote a meno che tu non le richieda nella finestra di riscrittura), e per la [riscrittura di una selezione](story-editor.md#redrafting-only-a-passage) `{%{{{selection}}}%}` (il testo selezionato; vuoto quando viene riscritta l'intera parte), `{%{{{selectionBefore}}}%}` e `{%{{{selectionAfter}}}%}` (il testo della parte intorno ad essa) |
| Prompt di impersonificazione | `{%{{{impersonateInstructions}}}%}` (la tua guida), `{%{{{outline}}}%}` (il piano della storia passato da chi chiama, per esempio il capitolo corrente dell'Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (le informazioni dell'ultima parte), `{%{{{instructionExamples}}}%}` (le tue istruzioni delle ultime tre parti, dalla più vecchia), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, e il turno per cui sono le istruzioni: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Prompt di sistema per annotare, Prompt di annotazione | Le variabili dei prompt di riscrittura, con `{%{{{annotateInstructions}}}%}` (ciò che vuoi venga controllato) al posto di `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (le note che la parte ha già), `{%{{{facts}}}%}` (ciò che le estensioni sanno della storia al punto della parte, per esempio cosa sanno i personaggi; vuoto senza una tale estensione) e `{%{{{outputFormat}}}%}` (come al modello viene chiesto di rispondere) |

Una sezione stampa il suo contenuto solo quando la variabile non è vuota, così il prompt non ha titoli vuoti:

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` dentro una sezione è il valore della variabile. Con un'*Ambientazione della scena* vuota, l'intera riga viene omessa.

Tutti i template possono usare anche le **macro** compatibili con SillyTavern, come `{%{{{user}}}%}` (il personaggio del punto di vista), `{%{{{description}}}%}` (la descrizione del libro), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` e `{%{{{setvar::name::value}}}%}`. Vedi [Template e macro](../templates/index.md) per la sintassi completa e il riferimento delle macro.

<a id="master-template"></a>

## Template principale

Il template principale diventa il **messaggio di sistema**, il primo messaggio del prompt. Il predefinito integrato ha questi blocchi:

1. **Ruolo di sistema** - il modello è un autore che continua la storia.
2. **Contesto del mondo e lore** - le voci del [lorebook](../lorebooks/index.md) attivate (`backgroundLore`).
3. **Stile di scrittura e meccanica** - punto di vista, tempo verbale, stile e regole generali della prosa.
4. **Riassunto della storia finora** - i [riassunti](summaries.md).
5. **Note dell'autore sulla storia** - solo quando hai dato [note al modello](annotations.md#giving-a-note-to-the-model).

Se il [fornitore di inferenza](../inference-providers.md#general-settings) ha un prompt di jailbreak attivo, viene messo prima del template principale.

<a id="user-prompt"></a>

## Prompt utente

Il prompt utente è l'**ultimo messaggio** del prompt: la richiesta della parte da scrivere. Viene riempito dalle *Istruzioni del nuovo turno* (o dai *Dettagli del turno* per *Rigenera* e *Swipe*). Il predefinito integrato chiede la sezione successiva con il personaggio del punto di vista, il luogo, i personaggi presenti e le istruzioni di trama, seguite dalle regole: nessun nuovo personaggio se non richiesto, seguire le istruzioni, mantenere la continuità e produrre solo il testo della storia.

Le voci del lorebook con modalità di inserimento *Prima del prompt utente* vengono messe subito prima di esso; vedi [Voci e attivazione](../lorebooks/entries.md).
<a id="redraft-prompt"></a>

## Prompt di riscrittura

La [riscrittura](story-editor.md#redrafting-a-part) di una parte è una sola richiesta composta da due prompt: il **Prompt di sistema per riscrivere** è il
messaggio di sistema, la storia prima della parte la segue come messaggi precedenti della conversazione (le parti come risposte
del modello, come le inviano le generazioni), e il **Prompt di riscrittura** è l'ultimo messaggio dell'utente. Il default integrato
dice al modello di applicare le tue istruzioni, mantenere ciò che non hai chiesto di cambiare, restare coerente con la storia prima e dopo la parte,
mantenere la voce narrativa e rispondere solo con la parte riscritta. Entrambi i prompt hanno le stesse variabili; `{%{{{previousStory}}}%}` è solo una
nota che la storia arriva come messaggi. Metti nel prompt di sistema ciò che resta uguale da una riscrittura all'altra e nell'ultimo
prompt ciò che cambia (il lore, che non è uguale per ogni parte, la storia dopo la parte, la parte, le istruzioni): un server che ricorda il prompt letto
può così riutilizzarne gran parte. La tua versione dell'ultimo prompt deve mantenere le variabili di cui ha bisogno: senza `{%{{{text}}}%}` il modello non
vede la parte. Ogni prompt segue la regola di tutti i prompt: il campo del libro, altrimenti quello in *Impostazioni → Template*, altrimenti quello integrato.

Quando riscrivi solo un [brano selezionato](story-editor.md#redrafting-only-a-passage), si usa lo stesso prompt con
`{%{{{selection}}}%}` compilato: il default integrato mostra allora la parte con il brano tra `[[PASSAGE TO REDRAFT]]` e
`[[/PASSAGE TO REDRAFT]]` e chiede al modello di rispondere solo con il testo che sostituisce il brano. Un prompt tuo
che non conosce `{%{{{selection}}}%}` fa rispondere il modello con l'intera parte, e la risposta intera sostituisce allora la selezione; aggiungi un blocco `{%{{#selection}}%}...{%{{{/selection}}}%}` (e `{%{{{^selection}}}%}` per l'altro caso) per gestirlo.

<a id="redraft-shorter-and-longer"></a>

## Riscrivere più breve e più lungo

La scelta **Lunghezza** della finestra di [riscrittura](story-editor.md#redrafting-a-part) (**Stessa lunghezza**, **Più breve**, **Più lungo**) non
tocca i prompt di riscrittura: per **Più breve** il testo di **Prompt per riscrivere più breve** (per **Più lungo** quello di **Prompt di riscrittura più lungo**) viene aggiunto dopo quanto hai scritto in *Istruzioni per la riscrittura*, al posto di `{%{{{redraftInstructions}}}%}`, quindi funziona anche con un prompt di riscrittura tuo. Con le istruzioni vuote è l'unica istruzione. I due testi seguono la regola di tutti i prompt: il campo del libro, altrimenti la cartella più vicina sopra di esso che ne imposta uno, altrimenti quello in *Impostazioni → Template*, altrimenti il testo integrato, che chiede circa un terzo in meno fino alla metà (più lungo: la metà in più fino al doppio) senza cambiare ciò che accade.

<a id="annotate-prompt"></a>

## Prompt di annotazione

Quando un modello [annota una parte](annotations.md#letting-a-model-annotate) riceve lo stesso tipo di richiesta della riscrittura: il
**Prompt di sistema per annotare** come messaggio di sistema, la storia prima della parte come messaggi precedenti e il **Prompt di annotazione** come
ultimo messaggio. Il default integrato fa del modello un editor: trova i passaggi che presentano un problema nel senso delle tue istruzioni, cita
ciascuno lettera per lettera e scrive per te una nota breve, senza elogi né riempitivo e senza ripetere le note che la parte ha già.
Quando un'estensione fornisce [fatti sulla storia](annotations.md#letting-a-model-annotate) (il Knowledge Tracker: cosa sanno i personaggi), il prompt
di default li mostra come *Fatti stabiliti* e l'editor segnala ciò che li contraddice. Il prompt ha la variabile `{%{{{outputFormat}}}%}` dove
è descritto il modo di rispondere (JSON, blocchi di testo o una revisione libera, a seconda del formato di risposta che scegli); mantienila in un prompt tuo, altrimenti al
modello non viene detto come rispondere. Entrambi i prompt di annotazione seguono la regola di tutti i prompt: il campo del libro, altrimenti quello in
*Impostazioni → Template*, altrimenti quello integrato.

**Cosa cercare** (*Istruzioni per l'annotazione*) segue la stessa regola, così puoi impostarlo una volta sola: in *Impostazioni → Template* per tutti i tuoi libri, in una cartella per i libri che contiene, oppure nella scheda *Prompt* per un solo libro. La finestra *Annota con l'IA* mostra ciò che si applica come testo grigio del suo campo e indica da dove proviene; lascia il campo vuoto per usarlo, scrivi qualcosa per sostituirlo per quella esecuzione, oppure clicca l'icona informativa del campo e **Compila il default** per copiare ciò che si applica nel campo e modificarlo per quella esecuzione. Se non è impostato nulla, vale il compito predefinito del prompt. Lo stesso vale per l'annotazione automatica e per il comando `/generate-annotation`.

<a id="impersonate-prompt"></a>

## Prompt di impersonificazione

Quando un modello [abbozza le istruzioni](story-editor.md#drafting-the-instructions) del turno successivo riceve una sola richiesta, inviata come unico
messaggio come la riscrittura. Il default integrato chiede al modello di scrivere ciò che digiteresti in *Istruzioni*: una specifica di trama della parte successiva (cosa succede, chi fa cosa, dove si ferma la parte) che sviluppa la tua indicazione e segue il piano, continua la storia da dove si era interrotta, è scritta come le tue istruzioni precedenti e non è la prosa stessa. Senza indicazioni continua la storia dove porta. La tua indicazione è dove il prompt inserisce `{%{{{impersonateInstructions}}}%}`; mantienila in
un prompt tuo. Il prompt di impersonificazione segue la regola di tutti i prompt: il campo del libro, altrimenti il *Prompt di impersonificazione* in *Impostazioni →
Template*, altrimenti quello integrato.

<a id="how-the-prompt-is-put-together"></a>

## Come è composto il prompt

```
system     jailbreak (if enabled) + master template
user       [ Generate story. ]
assistant  part #1
user       [ Generate more story. ]
assistant  part #2
...        (as many earlier parts as fit)
user       lore "Before User Prompt" + user prompt
```

Le parti precedenti vengono inviate come risposte del modello stesso, così il modello le continua nella stessa voce. Quando la storia è più lunga del [limite di contesto](../protocols.md#limits), le parti più vecchie vengono escluse per prime; i [riassunti](summaries.md)
mantengono quelle parti nella storia in forma condensata.

**Mostra il prompt** nel menu di qualsiasi parte mostra il prompt esatto da cui quella parte è stata scritta. **Confronta il prompt** lo affianca
al prompt di una parte precedente e mostra dove differiscono per la prima volta, che è il punto in cui finisce la [cache dei prompt](../prompt-cache.md#compare-prompt) di un server.

<a id="tips"></a>

## Suggerimenti

- Metti le regole che valgono per ogni parte (niente personaggi nuovi, lunghezza, formattazione) nel prompt utente: essendo l'ultimo
  messaggio, ha l'effetto più forte.
- Metti i fatti duraturi sul mondo e sui personaggi nel [lorebook](../lorebooks/index.md), non in *Stile*.
- Chiedi una lunghezza nelle istruzioni (ad esempio "circa 800 parole") invece di affidarti al limite di risposta, che tronca il testo.
- Dopo aver cambiato un template, genera una parte e controllala con **Mostra il prompt**.
