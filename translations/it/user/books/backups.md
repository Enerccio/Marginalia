---
label: Backup dei libri
order: 50
---

# Backup dei libri

Un backup di un libro è un'istantanea di un solo libro: le sue impostazioni e i suoi prompt, tutte le parti di tutti i rami, i riassunti, i tag e il lorebook. Usa i backup per tornare a uno stato precedente della storia, per fare una copia di un libro o per spostare un libro in un'altra installazione o presso un altro utente di Marginalia.

I backup dei libri sono separati dai [backup del database](../administration/database-backups.md) che gli amministratori creano per l'intera installazione.

![La scheda Backup di un libro](../../images/book-backups.png)

<a id="taking-a-backup"></a>

## Creare un backup

Nella scheda **Backup** del libro, fai clic su **Crea un backup**. Il backup compare in cima all'elenco con la sua **Data**, il numero di **Messaggi** (parti) che contiene e il numero di **Immagini** (le [figure delle parti](story-editor.md#images)) usate dalle sue parti.

I backup possono anche essere creati automaticamente, vedi [più sotto](#automatic-backups).

Un backup contiene l'intero albero della storia, i riassunti e le [annotazioni](annotations.md) delle parti; ripristinarlo, clonare un libro e importare un backup riportano indietro anche le note.

<a id="the-history-of-the-parts"></a>

## La cronologia delle parti

Un backup non contiene la [cronologia delle versioni](version-history.md) delle parti, a meno che tu non lo chieda: seleziona **Includi la cronologia delle versioni** accanto a **Crea un backup** (all'inizio è deselezionata, perché la cronologia può occupare parecchie volte lo spazio della storia). Ripristinando un backup di questo tipo, come libro o come copia, le versioni tornano con i loro nomi, il ragionamento di quelle generate e il cestino della cronologia; le versioni vengono ridotte al limite del libro in cui arrivano. I backup automatici non contengono mai la cronologia. Ripristinare un backup senza cronologia assegna a ogni parte una prima versione, come accade per un libro creato prima della cronologia.

<a id="backups-and-folders"></a>

## Backup e cartelle

Un backup ricorda le [cartelle](folders.md) in cui si trovava il suo libro, dalla più alta in giù, ciascuna con il nome, la descrizione, la lingua e le impostazioni di testo che imposta. **Ripristina come nuovo libro** e **Clona il backup** collocano il nuovo libro nella cartella indicata dal backup, se la possiedi ancora; altrimenti nelle cartelle con gli stessi nomi (se ne hai una, viene usata così com'è; una mancante viene creata con le impostazioni che aveva). Un backup senza cartella, o con cartelle che non possono essere create, viene ripristinato al livello più alto. **Ripristina** su un libro (completo o solo messaggi) mantiene il libro nella sua cartella. I tag di una cartella non sono nel backup. I backup più vecchi si ripristinano come prima.

<a id="pictures-in-a-backup"></a>

## Immagini in un backup

Un backup ricorda **quali immagini** hanno le parti (e le loro didascalie), ma **non i file delle immagini**. Così i backup, anche quelli automatici, restano piccoli. Le immagini restano nelle tue [Risorse](../resources.md). Per includere le immagini in un backup, le impacchetti quando lo [esporti](#exporting-and-importing).

<a id="restoring-a-backup"></a>

## Ripristinare un backup

Fai clic su **Ripristina** accanto al backup e scegli:

| Opzione | Cosa viene ripristinato |
|---|---|
| **Ripristino completo** | Tutto: nome, descrizione, pubblicazione, prompt e altre impostazioni del libro, tag, modello, protocollo, lorebook e tutte le parti. |
| **Ripristina solo i messaggi** | Solo le parti (tutti i rami) e i riassunti. Le impostazioni, i prompt e il lorebook attuali del libro restano invariati. |

!!!warning
Ripristinare sostituisce **tutte le parti** del libro con quelle del backup. Le parti scritte dopo il backup vanno perse.
Se potresti volerle recuperare, crea prima un backup dello stato attuale.
!!!

La finestra del libro si chiude e si riapre con il libro ripristinato.

Le immagini delle parti ripristinate e ogni altro file a cui le parti puntano (per esempio l'audio di un [audiolibro](../extensions/audiobook.md)) sono quelli elencati dal backup, purché siano ancora presenti nelle tue [Risorse](../resources.md). Una risorsa eliminata nel frattempo non compare più nella parte. Il ripristino non riporta indietro i file eliminati, a meno che il backup non provenga da un'esportazione con le risorse, che tu hai [importato](#importing-a-backup).

**Modello e protocollo** vengono cercati tra i tuoi provider di inferenza e protocolli: prima quello esatto indicato dal backup, poi uno con lo stesso nome. Se nessuno dei due esiste, il libro mantiene il modello o il protocollo attuale.

I **lorebook** presenti nel backup vengono confrontati con i tuoi. Quando esiste un lorebook con lo stesso nome ma un'identità diversa, Marginalia chiede, per ciascuno, se **Collega a quello esistente** o **Crea nuovo** a partire da quello del backup. I lorebook che non possiedi affatto possono essere importati (**Importa**) oppure esclusi (**Non importare**).

![Scelta di cosa fare con i lorebook](../../images/lorebook-import.png)

<a id="copying-a-book"></a>

## Clonare un libro

**Clona il backup** crea un nuovo libro a partire dal backup, con un nome che inserisci tu. Il libro originale non viene modificato. Usalo per provare qualcosa di radicale su una copia del libro, o per iniziare un nuovo libro dalla stessa apertura.

Se il backup punta a delle risorse (immagini, l'audio di un audiolibro, altri file), Marginalia chiede **Con risorse** oppure **Senza risorse**:

- *Con risorse* assegna al nuovo libro **le proprie copie** di quei file (gli stessi file, con nuove voci in [Risorse](../resources.md)), così eliminare o sostituire un'immagine in un libro non tocca l'altro. Le risorse che hai eliminato dopo la creazione del backup vengono escluse.
- *Senza risorse* clona solo il testo.

![Clonazione con o senza risorse](../../images/backup-clone-images.png)
<a id="exporting-and-importing"></a>

## Esportazione e importazione

**Esporta** scarica il backup come file JSON (`<book name>_backup.json`). Conservalo come copia extra, oppure portalo su un'altra installazione.

Se il backup fa riferimento a delle risorse, Marginalia chiede se includerle nel pacchetto (le immagini delle parti e ogni altro file a cui i loro dati rimandano, come l'audio di un audiolibro):

![Esportazione di un backup che contiene immagini](../../images/backup-export-images.png)

- **Senza risorse** scarica subito il JSON, come descritto sopra. Le risorse vi sono solo citate.
- **Con risorse** impacchetta il backup e tutte le sue risorse in un **file ZIP** (`<book name>_backup.zip`). Una barra di avanzamento mostra quante ne sono già state elaborate; il file viene costruito su disco un file alla volta, quindi anche un libro con molti file di grandi dimensioni non occupa molta memoria. L'audiolibro di un romanzo lungo pesa centinaia di megabyte: impacchettalo solo quando ne hai davvero bisogno. Quando è pronto, clicca su **Scarica**. Il file resta disponibile solo finché non chiudi quella finestra di dialogo.

Se una risorsa non può essere impacchettata perché l'hai eliminata o il suo file manca, la finestra di dialogo indica quante ne sono state escluse.

La casella di controllo **Includi le impostazioni ereditate nell'esportazione** (attiva per impostazione predefinita, accanto ai pulsanti di esportazione) decide che cosa il file dice sulle impostazioni che il libro eredita dalle sue [cartelle](folders.md#where-the-settings-of-a-book-come-from). Se è attiva, la lingua, il modello master, il punto di vista, il tempo verbale, lo stile e i prompt che il libro non imposta da sé vengono scritti nel file come valori propri, presi dalle cartelle in cui si trovava al momento del backup (prima la più vicina) e poi dalle tue impostazioni, così che il libro risulti uguale in un'installazione o in un account che non ha tali cartelle e impostazioni; i valori predefiniti integrati non vengono scritti. Se è disattiva, il file contiene solo ciò che il libro imposta da sé. Il file non indica mai in quale cartella si trova il libro: dove viene archiviato un libro è una scelta locale. (L'esportazione della storia, [Esportare un libro](export.md), scrive solo il testo e non ha impostazioni.)

Lo ZIP contiene `backup.json` (lo stesso JSON), `resources.json` (l'elenco delle risorse) e i file. Non devi decomprimerlo: entrambi i modi per riportare indietro un backup accettano anche `.zip` oltre a `.json`.

Ci sono due modi per riportare indietro un file di backup:

- **Importa backup** nella scheda *Backup* aggiunge il file all'elenco dei backup di **questo libro**. Non cambia nulla finché non lo *ripristini*.
- **Importa il backup come nuovo libro** nella scheda *Impostazioni* crea un **nuovo libro** a partire dal file.

Entrambi accettano un file `.json` o `.zip`. Il file viene caricato sul server e letto da lì, quindi un backup di grandi dimensioni non è un problema.

<a id="importing-a-backup"></a>

### Importare un backup

Quando il file è uno ZIP con immagini, le immagini vengono prima aggiunte alle tue [Risorse](../resources.md), e il backup le usa da lì. Un'immagine che hai già (lo stesso file) non viene memorizzata di nuovo: si usa quella esistente, quindi importare la tua stessa esportazione non aggiunge nulla. Un file JSON senza immagini viene importato così com'è.

Importando **come nuovo libro**, il nuovo libro riceve **voci proprie** nelle Risorse, anche se hai già le stesse immagini: le voci puntano agli stessi file, quindi non si occupa due volte lo spazio su disco. Se il file non contiene immagini (un JSON semplice), il nuovo libro riceve copie di quelle immagini elencate nel file che hai ancora; quelle che non hai vengono escluse. Tutto ciò che nel file non è un'immagine utilizzabile (non un PNG, JPEG, GIF o WebP, oppure troppo grande) viene ignorato.

<a id="importing-a-backup-as-a-new-book"></a>

### Importare un backup come nuovo libro

1. Apri la scheda **Impostazioni** e clicca su **Importa il backup come nuovo libro**.
2. Inserisci il **Nome del nuovo libro** - il caricamento è abilitato solo dopo aver compilato il nome.
3. Carica il file di backup.
4. Decidi per i lorebook, se viene richiesto.

Il nuovo libro compare nella scheda *Libri*.

<a id="deleting-backups"></a>

## Eliminare i backup

Il pulsante del cestino rimuove un backup in modo definitivo. I backup automatici non vengono ruotati: restano finché non li elimini.

<a id="automatic-backups"></a>

## Backup automatici

Marginalia può creare un backup da solo mentre scrivi. Imposta il valore predefinito per tutti i libri in **Impostazioni → Impostazioni generali → Strategia di backup**:

| Strategia | |
|---|---|
| **Nessun backup** | Nessun backup automatico. |
| **Dopo N messaggi** | Un backup dopo ogni *N* parti generate. |
| **Dopo N minuti** | Un backup dopo una parte generata quando sono passati almeno *N* minuti dall'ultimo backup automatico. |

Per usare una strategia diversa per un singolo libro, seleziona **Sostituzione per manoscritto** nella sua scheda *Backup* e scegli lì la strategia e il numero. Deselezionando la casella, il libro torna al valore predefinito di *Impostazioni*.

I backup automatici vengono creati solo dopo la generazione di una parte, quindi un libro che leggi soltanto non riceve nuovi backup.

<a id="where-backups-are-stored"></a>

## Dove vengono archiviati i backup

Ogni backup è un file JSON nella cartella dati di Marginalia:

```
~/.marginalia/data/<your login>/backups/manuscripts/<book id>/
```

Con la [versione Docker](../getting-started/docker-server.md) questo percorso è `./data/data/<login>/...` sull'host. I file non si trovano nel database, quindi un [backup del database](../administration/database-backups.md) non li contiene. Includi l'intera cartella `.marginalia` nei tuoi backup.

I file delle immagini non si trovano nemmeno nei backup: sono nella cartella `data/<your login>/images` accanto a `backups`. Non sono neppure in un [backup del database](../administration/database-backups.md), vedi lì.
