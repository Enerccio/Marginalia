---
label: Importazione ed esportazione
order: 80
---

# Importazione ed esportazione

<a id="exporting"></a>

## Esportazione

Seleziona il lorebook nell'editor e clicca sul pulsante **export** (icona a forma di file, *Esporta il lorebook*). Il browser scarica `<lorebook name>.json` con:

- il lorebook e **tutti i suoi sub lorebook** (a ogni livello), con nome, descrizione, stato di attivazione e tag;
- ogni voce con contenuto, nota, ordine, filtro, modalità di inserimento, tag e tag negativi.

Serve per conservare una copia, per condividere un mondo con altri utenti o per spostarlo su un'altra installazione. I dati che le [estensioni](../extensions/index.md) salvano in un lorebook o in una voce sono presenti anche nel file e vengono ripristinati dall'importazione. Le revisioni delle voci dell'estensione [Lorebook VCS](../extensions/lorebook-vcs.md) appartengono alle vecchie voci e non compaiono nelle voci create dall'importazione; l'estensione ha una sua esportazione per queste.

<a id="exporting-for-sillytavern"></a>

### Esportazione per SillyTavern

Il pulsante **export** apre un menu con due voci: **Esporta il lorebook** (il file descritto sopra) e **Esporta in SillyTavern**, che scarica il lorebook come file di world info di SillyTavern, importabile in SillyTavern. È l'operazione inversa dell'[importazione](#importing-sillytavern-world-info):

| Marginalia | SillyTavern |
|---|---|
| *Nome della voce* | Titolo / memo (`comment`) |
| *Contenuto*, *Ordine*, *Attivata* | Contenuto, ordine, disattivata |
| Nessun filtro | Voce costante (🔵) |
| Filtro *Testo* | Una chiave primaria |
| *Parole chiave (qualsiasi)* | Una chiave primaria per ogni parola chiave |
| *Parole chiave (tutte)* | La prima parola chiave è la chiave primaria, le altre sono chiavi secondarie con logica *AND ALL* |
| *Regex* | Una chiave scritta come `/pattern/flags` |
| *Nel blocco del lore* | Posizione *prima delle definizioni dei personaggi* |
| *Prima del prompt utente* | Posizione *a profondità* 1 |
| Tag | Filtro sui personaggi (per nome); tag negativi solo quando la voce non ha tag (*exclude*) |

I **sub lorebook vengono uniti**: il file contiene prima le voci del lorebook, poi quelle dei suoi sub lorebook, perché SillyTavern non ha lorebook dentro lorebook. Ciò che in SillyTavern non ha un equivalente viene omesso: la *Nota* di una voce, i tag negativi insieme ai tag, i dati delle estensioni. SillyTavern confronta le chiavi con la chat recente; per l'altra direzione vedi [cosa funziona in modo diverso](#what-works-differently).

!!! warning
Una espressione regolare è scritta nella sintassi Java. SillyTavern usa JavaScript, quindi le espressioni semplici funzionano, ma ciò che è specifico di Java (quantificatori possessivi, classi `\p{...}`) non corrisponde lì.
!!!

<a id="importing-a-marginalia-lorebook"></a>

## Importare un lorebook di Marginalia

1. Clicca sul pulsante **import** (icona a forma di file) e scegli **Importa un lorebook di Marginalia**.
2. Carica un file esportato da Marginalia.

L'importazione crea sempre lorebook **nuovi** - il lorebook e i suoi sub lorebook, con la stessa struttura - anche se hai già lorebook con gli stessi nomi. Il lorebook importato viene selezionato nell'editor. I tag vengono associati ai tuoi tag esistenti in base al nome, oppure creati.

Possono essere importati anche file di versioni precedenti di Marginalia (un singolo lorebook senza sub lorebook).

!!!
I lorebook viaggiano anche dentro i [backup del libro](../books/backups.md). Ripristinando o clonando un backup, il sistema può collegarsi a lorebook che hai già invece di creare copie; vedi [Ripristinare un backup](../books/backups.md#restoring-a-backup).
!!!

<a id="importing-a-lorebook-another-user-shares"></a>

## Importare un lorebook condiviso da un altro utente

**Importa da un altro utente** nel menu di importazione elenca i lorebook che altri utenti [condividono](lorebooks.md#sharing-a-lorebook) con il loro nome, autore e descrizione; apri una riga per leggere le voci, poi clicca su **Importa** per ottenere una copia privata. Vengono inclusi solo i sub lorebook che l'autore condivide. A differenza dell'importazione da file, non occorre esportare e inviare nulla: l'autore si limita a usare **Condividi con altri utenti**.

<a id="importing-an-archive-of-text-files"></a>

## Importare un archivio di file di testo

1. Clicca sul pulsante **import** e scegli **Importa da un archivio**.
2. Carica un file `.zip` con file `.md` (Markdown).
3. Controlla i **nomi delle voci** nella tabella e modificali se vuoi, poi clicca su **Importa**.

![La finestra per caricare l'archivio](../../images/archive-lorebook-upload.png)

![I nomi delle voci prima dell'importazione](../../images/archive-entry-names.png)

Marginalia crea un nuovo lorebook con il nome del file (senza `.zip`), con **una voce per ogni file `.md`**. I file sono presi nell'ordine dei loro nomi, con i numeri dentro i nomi confrontati come numeri (`2.md` viene prima di `10.md`), anche nelle cartelle dell'archivio; le voci ricevono *Ordine* 1, 2, 3... in quella sequenza. L'intero file è il *Contenuto*. Altri file, cartelle, file nascosti (`.name`) e la cartella `__MACOSX` degli archivi creati su Mac vengono saltati.

I **nomi delle voci** vengono ricavati, in quest'ordine:

1. da un file `names.txt` nella radice dell'archivio: un nome per riga, la prima riga per il primo file `.md` nell'ordine sopra, e così via (una riga vuota lascia quel file alla regola successiva; le righe in più vengono ignorate). Serve a dare nome a molti file in una volta, per esempio da uno script;
2. il primo titolo del file (`# Chapter 1` dà *Capitolo 1*);
3. il nome del file senza `.md`.

La tabella nella finestra mostra il risultato, e puoi scrivere sopra qualsiasi nome.

L'importazione poi mostra il suo avanzamento. **Interrompi** la termina dopo la voce in fase di salvataggio: il lorebook conserva le voci già importate (l'importazione non viene annullata), e un messaggio indica quanti file sono stati importati.

Le voci vengono importate **disattivate**: non hanno filtro, quindi se fossero attive verrebbero inviate con ogni richiesta, cosa poco desiderabile per un intero libro di capitoli. Attiva quelle che ti servono, con un filtro o con i [tag](entries.md#tags) - **Modifica di massa** è il modo rapido per farlo su molte voci.

Per inserire gli stessi file in una storia come parti, vedi [Importare nel libro](../books/story-editor.md#importing-into-the-book).

<a id="importing-sillytavern-world-info"></a>

## Importare world info di SillyTavern

1. Clicca sul pulsante **import** e scegli **Importa da SillyTavern**.
2. Carica il file di world info (`.json`) esportato da SillyTavern.

Marginalia crea un nuovo lorebook con il nome del world info (o del file) e converte ogni voce:

| SillyTavern | Marginalia |
|---|---|
| Titolo / memo (`comment`) | *Nome della voce* (la prima chiave se manca il titolo) |
| Contenuto | *Contenuto* - le macro come `{%{{{user}}}%}` e `{%{{{char}}}%}` continuano a funzionare, vedi [macro](../templates/macros.md) |
| Ordine | *Ordine* |
| Disattivata | *Attivata* deselezionata |
| Costante (🔵), oppure nessuna chiave | sempre attiva (nessun filtro) |
| Chiavi primarie, chiavi secondarie opzionali con la loro logica | un filtro, vedi sotto |
| Distinzione maiuscole/minuscole, corrispondenza di parole intere | incorporate nel filtro |
| Posizione *prima / dopo le definizioni dei personaggi*, *esempi di messaggi* | *Nel blocco del lore* |
| Posizione *nota dell'autore* (in alto / in basso), *a profondità* | *Prima del prompt utente* |
| Filtro sui personaggi | tag (o tag negativi per *exclude*), per nome del personaggio e tag |

**Le chiavi diventano filtri.** Una singola chiave semplice diventa un filtro *Testo* (`dragon`). Tutto il resto - più chiavi, chiavi secondarie (*AND ANY*, *AND ALL*, *NOT ANY*, *NOT ALL*), chiavi scritte come `/regex/`, corrispondenza con distinzione di maiuscole o di parole intere - diventa un unico filtro *Regex* che fa lo stesso. Per esempio, le chiavi `dragon`, `wyrm` con la chiave secondaria `fire` (*AND ANY*) diventano:

```
(?s)^(?=.*(?:\Qdragon\E|\Qwyrm\E))(?=.*(?:\Qfire\E))
```

Non serve capire queste espressioni; modificale solo se conosci le espressioni regolari.

<a id="what-works-differently"></a>

### Cosa funziona in modo diverso

Le impostazioni di SillyTavern senza un equivalente in Marginalia sono elencate nella **Nota** della voce, per esempio *Importato da SillyTavern, non supportato: probabilità 50%, gruppo di inclusione "meteo".* Sono:

- probabilità, gruppi di inclusione, sticky, cooldown e ritardo - la voce è semplicemente attiva ogni volta che corrisponde;
- inserimento *a profondità* - la voce finisce invece prima del prompt utente.

La differenza più importante riguarda **con cosa vengono confrontate le chiavi**. SillyTavern analizza la chat recente. Marginalia confronta le chiavi con la [richiesta per la parte che si sta scrivendo](entries.md#filters) - la sua scena, i personaggi e le istruzioni - e una voce importata parte esattamente da lì (il suo **Contesto di attivazione** è *Istruzioni*). Quindi una voce che SillyTavern attivava perché un nome compariva negli ultimi messaggi, in Marginalia si attiva solo se nel turno nomini quel nome. Per ottenere ciò che fa SillyTavern, imposta il **Contesto di attivazione** della voce su *Istruzioni e parti della storia* e **Parti** sul numero delle parti più recenti in cui cercare, vedi [Guardare dentro la storia](entries.md#looking-into-the-story). Anche l'attivazione ricorsiva (voci che attivano altre voci) non viene impostata dall'importazione: attivala con la **profondità di scansione** del lorebook, vedi [Voci portate dentro da altre voci](entries.md#entries-brought-in-by-other-entries).

Dopo l'importazione, controlla le voci e decidi quali devono essere sempre attive, quali mantengono i filtri e quali è meglio gestire con i [tag](entries.md#tags).
