---
label: Importa ed esporta
order: 80
---

# Importa ed esporta

<a id="exporting"></a>

## Esportazione

Seleziona il lorebook nell'editor e fai clic sul pulsante **export** (icona file, *Esporta il lorebook*). Il browser scarica
`<nome del lorebook>.json` con:

- il lorebook e **tutti i suoi sotto-lorebook** (a ogni livello), con nomi, descrizione, stato di attivazione ed etichette;
- ogni voce con contenuto, nota, ordine, filtro, modalità di inserimento, etichette ed etichette negative.

Usalo per conservare una copia, per condividere un mondo con altri utenti o per spostarlo su un'altra installazione. I dati
che le [estensioni](../extensions/index.md) conservano in un lorebook o in una voce sono presenti nel file e vengono ripristinati
dall'importazione. Le revisioni delle voci dell'estensione [Lorebook VCS](../extensions/lorebook-vcs.md) appartengono alle vecchie
voci e non compaiono nelle voci create dall'importazione; l'estensione ha una sua esportazione per queste.

<a id="exporting-for-sillytavern"></a>

### Esportazione per SillyTavern

Il pulsante **export** apre un menu con due voci: **Esporta il lorebook** (il file descritto sopra) e **Esporta in SillyTavern**,
che scarica il lorebook come file di world info di SillyTavern, importabile in SillyTavern. È il contrario dell'
[importazione](#importing-sillytavern-world-info):

| Marginalia | SillyTavern |
|---|---|
| *Nome della voce* | Titolo / memo (`comment`) |
| *Contenuto*, *Ordine*, *Attivata* | Contenuto, ordine, disabilitata |
| Nessun filtro | Voce costante (🔵) |
| Filtro *Testo* | Una chiave primaria |
| *Parole chiave (qualsiasi)* | Una chiave primaria per ogni parola chiave |
| *Parole chiave (tutte)* | La prima parola chiave è la chiave primaria, le altre sono chiavi secondarie con la logica *AND ALL* |
| *Regex* | Una chiave scritta come `/modello/flag` |
| *Nel blocco del lore* | Posizione *prima delle definizioni dei personaggi* |
| *Prima del prompt utente* | Posizione *a profondità* 1 |
| Etichette | Filtro sui personaggi (per nome); etichette negative solo quando la voce non ha etichette (*exclude*) |

I **sotto-lorebook vengono uniti**: il file contiene prima le voci del lorebook, poi quelle dei suoi sotto-lorebook, perché
SillyTavern non ha lorebook dentro altri lorebook. Ciò che SillyTavern non ha come equivalente viene omesso: la *Nota* di una
voce, le etichette negative insieme alle etichette, i dati delle estensioni. SillyTavern cerca le chiavi nella chat recente;
vedi [che cosa funziona diversamente](#what-works-differently) per l'altra direzione.

!!! warning
Un'espressione regolare è scritta nella sintassi Java. SillyTavern usa JavaScript, quindi le espressioni semplici funzionano,
ma ciò che è specifico di Java (quantificatori possessivi, classi `\p{...}`) lì non trova corrispondenze.
!!!

<a id="importing-a-marginalia-lorebook"></a>

## Importazione di un lorebook di Marginalia

1. Fai clic sul pulsante **import** (icona file) e scegli **Importa un lorebook di Marginalia**.
2. Carica un file esportato da Marginalia.

L'importazione crea sempre lorebook **nuovi** - il lorebook e i suoi sotto-lorebook, con la stessa struttura - anche se hai già
lorebook con gli stessi nomi. Il lorebook importato viene selezionato nell'editor. Le etichette vengono associate alle tue
etichette esistenti per nome, oppure create.

Sono importabili anche i file delle versioni precedenti di Marginalia (un unico lorebook senza sotto-lorebook).

!!!
I lorebook viaggiano anche dentro i [backup del libro](../books/backups.md). Ripristinando o clonando un backup si può
collegare lorebook che hai già, invece di creare copie; vedi [Ripristinare un backup](../books/backups.md#restoring-a-backup).
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## Importazione di un lorebook condiviso da un altro utente

**Importa da un altro utente** nel menu di importazione elenca i lorebook che altri utenti [condividono](lorebooks.md#sharing-a-lorebook)
con il loro nome, autore e descrizione; apri una riga per leggere le voci, fai clic su **Import** per ottenere una copia privata.
Vengono inclusi solo i sotto-lorebook che l'autore condivide. A differenza dell'importazione da file, non occorre esportare e
inviare nulla: l'autore si limita a usare **Condividi con altri utenti**.

<a id="importing-an-archive-of-text-files"></a>

## Importazione di un archivio di file di testo

1. Fai clic sul pulsante **import** e scegli **Importa da un archivio**.
2. Carica un file `.zip` con file `.md` (Markdown).
3. Controlla i **nomi delle voci** nella tabella e modificali se vuoi, poi fai clic su **Import**.

![La finestra per caricare l'archivio](../../images/archive-lorebook-upload.png)

![I nomi delle voci prima dell'importazione](../../images/archive-entry-names.png)

Marginalia crea un nuovo lorebook con il nome del file (senza `.zip`) con **una voce per ogni file `.md`**. I file sono presi
nell'ordine dei loro nomi, con i numeri nei nomi confrontati come numeri (`2.md` viene prima di `10.md`), anche nelle cartelle
dell'archivio; alle voci viene assegnato l'*Ordine* 1, 2, 3... in quella sequenza. L'intero file è il *Contenuto*. Gli altri file,
le cartelle, i file nascosti (`.nome`) e la cartella `__MACOSX` degli archivi creati su Mac vengono saltati.

I **nomi delle voci** vengono ricavati, in quest'ordine:

1. un file `names.txt` nella radice dell'archivio: un nome per riga, la prima riga per il primo file `.md` nell'ordine
   sopra, e così via (una riga vuota lascia quel file alla regola successiva; le righe in più vengono ignorate). Usalo per
   dare nome a molti file in una volta, ad esempio da uno script;
2. la prima intestazione del file (`# Capitolo 1` dà *Capitolo 1*);
3. il nome del file senza `.md`.

La tabella nella finestra mostra il risultato, e puoi sovrascrivere qualsiasi nome.

L'importazione mostra poi il suo avanzamento. **Interrompi** la termina dopo la voce in corso di salvataggio: il lorebook conserva
le voci già importate (l'importazione non viene annullata), e un messaggio indica quanti file sono stati importati.

Le voci vengono importate **disattivate**: non hanno filtro, quindi se attive verrebbero inviate con ogni richiesta, cosa poco
desiderabile per un intero libro di capitoli. Attiva quelle che ti servono, con un filtro o con [etichette](entries.md#tags) -
**Modifica di massa** è il modo rapido per farlo per molte voci.

Per inserire gli stessi file in una storia come parti, vedi [Importazione nel libro](../books/story-editor.md#importing-into-the-book).

<a id="importing-sillytavern-world-info"></a>

## Importazione di world info di SillyTavern

1. Fai clic sul pulsante **import** e scegli **Importa da SillyTavern**.
2. Carica il file di world info (`.json`) esportato da SillyTavern.

Marginalia crea un nuovo lorebook con il nome del world info (o del file) e converte ogni voce:

| SillyTavern | Marginalia |
|---|---|
| Titolo / memo (`comment`) | *Nome della voce* (la prima chiave se manca il titolo) |
| Contenuto | *Contenuto* - le macro come `{%{{{user}}}%}` e `{%{{{char}}}%}` continuano a funzionare, vedi [macro](../templates/macros.md) |
| Ordine | *Ordine* |
| Disabilitata | *Attivata* deselezionata |
| Costante (🔵), oppure nessuna chiave | sempre attiva (nessun filtro) |
| Chiavi primarie, chiavi secondarie opzionali con la loro logica | un filtro, vedi sotto |
| Distinzione maiuscole/minuscole, corrispondenza per parola intera | incorporate nel filtro |
| Posizione *prima / dopo le definizioni dei personaggi*, *esempi di messaggi* | *Nel blocco del lore* |
| Posizione *nota dell'autore* (in alto / in basso), *a profondità* | *Prima del prompt utente* |
| Filtro sui personaggi | etichette (o etichette negative per *exclude*), per nome del personaggio ed etichetta |

**Le chiavi diventano filtri.** Una singola chiave semplice diventa un filtro *Testo* (`dragon`). Tutto il resto - più chiavi,
chiavi secondarie (*AND ANY*, *AND ALL*, *NOT ANY*, *NOT ALL*), chiavi scritte come `/regex/`, corrispondenza sensibile alle
maiuscole o per parola intera - diventa un unico filtro *Regex* che fa lo stesso. Ad esempio, le chiavi `dragon`, `wyrm` con la
chiave secondaria `fire` (*AND ANY*) diventano:

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

Non devi leggere queste espressioni; modificale solo se conosci le espressioni regolari.

<a id="what-works-differently"></a>

### Che cosa funziona diversamente

Le impostazioni di SillyTavern senza un equivalente in Marginalia sono elencate nella **Nota** della voce, ad esempio
*Importato da SillyTavern, non supportato: probabilità 50%, gruppo di inclusione "meteo".* Sono:

- probabilità, gruppi di inclusione, sticky, cooldown e ritardo - la voce è semplicemente attiva ogni volta che corrisponde;
- inserimento *a profondità* - la voce va invece prima del prompt utente.

La differenza più importante riguarda **su che cosa vengono confrontate le chiavi**. SillyTavern analizza la chat recente.
Marginalia confronta la [richiesta per la parte in scrittura](entries.md#filters) - la sua scena, i personaggi e le istruzioni - e una
voce importata parte esattamente da lì (il suo **Contesto di attivazione** è *Istruzioni*), quindi una voce che SillyTavern attivava
perché un nome compariva ngli ultimi messaggi viene attivata in Marginalia solo quando nomini quel nome nel turno. Per ottenere
ciò che fa SillyTavern, imposta il **Contesto di attivazione** della voce su *Istruzioni e parti della storia* e **Parti** sul
numero delle parti più recenti in cui cercare, vedi [Cercare nella storia](entries.md#looking-into-the-story). Anche l'attivazione
ricorsiva (voci che attivano altre voci) non viene impostata dall'importazione: attivala con la **profondità di scansione** del
lorebook, vedi [Voci portate da altre voci](entries.md#entries-brought-in-by-other-entries).

Dopo l'importazione, controlla le voci e decidi quali devono essere sempre attive, quali mantengono i loro filtri e quali
sono meglio gestite con le [etichette](entries.md#tags).
