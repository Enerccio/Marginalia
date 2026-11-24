---
label: Pulizia
order: 80
---

# Pulizia

Quando gli utenti eliminano qualcosa in Marginalia - un libro, una parte della storia, un lorebook, un fornitore di inferenza - l'oggetto scompare dalle loro liste, ma resta nel database. In questo modo eliminare è sicuro e i riferimenti continuano a funzionare (un libro sa ancora quale fornitore ha scritto le sue parti). Fino a quel momento gli utenti possono ripristinare gli oggetti eliminati dalla scheda [Cestino](../trash.md). **Pulizia** rimuove definitivamente questi dati eliminati, per mantenere il database leggero.

![La scheda Pulizia](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Eseguire una pulizia

!!!warning
La pulizia non può essere annullata: ciò che rimuove viene eliminato anche dal [Cestino](../trash.md). [Crea prima un backup del database](database-backups.md).
!!!

1. Apri la scheda **Pulizia** della pagina di amministrazione e fai clic su **Analizza**. Per ora non viene modificato nulla.
2. Controlla il risultato (vedi sotto).
3. Fai clic su **Esegui pulizia** e conferma *n oggetti verranno eliminati definitivamente*.

La pulizia riguarda i dati di tutti gli utenti.

La pulizia elimina voci dal database, e per gli [agenti](../agents.md) solo i file: la workspace di un agente eliminato e la cartella di un'esecuzione eliminata. I file delle [risorse](../resources.md) eliminate e non utilizzate (immagini delle parti, audio) restano nella cartella dati del rispettivo proprietario finché non le rimuovi nella scheda [File delle risorse](resource-files.md).

<a id="seeing-what-is-deleted"></a>

## Vedere cosa è stato eliminato

**Cestino**, sotto le tabelle, elenca gli oggetti eliminati di tutti gli utenti, una riga per oggetto (aprilo per caricare la tabella), così puoi vedere cosa rimuoverebbe una pulizia e ripristinare ciò che qualcuno ti ha chiesto di recuperare. È la stessa tabella della scheda [Cestino](../trash.md), inclusi i filtri per utente e tipo e il collegamento *Contenuto esteso*.

![La sezione Cestino della scheda Pulizia](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Leggere l'analisi

La prima tabella contiene una riga per ogni tipo di dato:

| Colonna | |
|---|---|
| **Entità** | Il tipo di dato: `Manuscript` (libri), `ChatMessage` (parti della storia), `Lorebook`, `LorebookEntry`, `AI` (fornitori di inferenza), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Eliminati** | Quanti sono stati eliminati. |
| **Da eliminare** | Quanti verranno rimossi da *Esegui pulizia*, inclusi i dati che appartengono a oggetti eliminati. |
| **Bloccati** | Eliminati, ma ancora in uso, quindi vengono conservati. |

**Oggetti eliminati ancora in uso** elenca gli oggetti bloccati con il nome e **Referenziato da** - ciò che li utilizza ancora.

<a id="what-is-removed"></a>

## Cosa viene rimosso

La pulizia rimuove gli oggetti eliminati che nessun oggetto attivo utilizza più, insieme ai dati che appartengono solo a loro:

| Eliminato | Rimosso insieme a |
|---|---|
| un libro | tutte le sue parti della storia, i riassunti e i collegamenti dei tag |
| un lorebook | le sue voci e i collegamenti dei tag; gli altri lorebook non lo includono più come sotto-lorebook |
| una parte della storia | la parte (i suoi figli erano già stati spostati nel genitore) e il suo riassunto |
| un fornitore di inferenza o un protocollo | rimosso dai valori predefiniti degli utenti in *Impostazioni* |
| un tag | i suoi collegamenti a libri e voci |
| un utente | tutto ciò che possedeva: libri (con le loro parti della storia e i riassunti), lorebook, fornitori di inferenza, protocolli, tag e impostazioni |

La pulizia rimuove anche le [risorse](../resources.md) che nessuno utilizza più, anche se nessuno le ha eliminate: file aggiunti a un oggetto (l'audio di un [audiolibro](../extensions/audiobook.md), un'immagine di una parte) il cui oggetto non esiste più nel database oppure viene rimosso da questa pulizia, quando nessun altro oggetto li richiama. Un ramo creato a partire dalla parte ha le stesse immagini e clip, quindi restano finché il ramo esiste; così come restano i file richiamati da un oggetto eliminato che si trova ancora nel cestino. Questi file contano in **Da eliminare** di `Resource`, non in *Eliminati*. Un file che non è usato da nessun oggetto (*nessuno* nella scheda Risorse) viene conservato.

<a id="what-is-kept"></a>

## Cosa viene conservato

Un oggetto eliminato è **bloccato** - cioè conservato - finché qualcosa di attivo lo utilizza ancora:

| Bloccato | Perché | Per sbloccarlo |
|---|---|---|
| un fornitore di inferenza o un protocollo eliminato | un libro lo usa ancora come modello o protocollo | seleziona un altro nella scheda *Info* del libro, oppure elimina il libro |
| un lorebook eliminato (con le sue voci) | un libro lo usa ancora | seleziona un altro lorebook nel libro, oppure svuotalo |
| una parte della storia eliminata | è l'ultima parte del ramo attivo di un libro | passa il libro a un altro ramo, oppure elimina il libro |
| un utente eliminato | qualcosa di un altro utente usa ancora i suoi dati (un libro che usa il suo fornitore di inferenza o il suo lorebook) | modifica o elimina il libro dell'altro utente |

Esegui di nuovo *Analizza* dopo aver sbloccato gli oggetti.

<a id="reference-model"></a>

## Modello dei riferimenti

**Modello dei riferimenti**, in fondo, elenca ogni riferimento tra i tipi di dati e la sua politica - utile quando vuoi capire perché qualcosa è bloccato:

| Politica | Significato |
|---|---|
| **Blocca la pulizia** | Un bersaglio eliminato viene conservato finché un oggetto attivo vi fa riferimento. |
| **Pulito insieme al bersaglio** | L'oggetto che fa riferimento appartiene al bersaglio e viene rimosso con esso (le voci con il loro lorebook). |
| **Possiede il bersaglio** | Il bersaglio appartiene all'oggetto che fa riferimento (un riassunto alla sua parte). |
| **Annullato alla pulizia** | Il riferimento viene rimosso quando il bersaglio viene pulito (sotto-lorebook, modello predefinito). |

Le estensioni possono partecipare alla pulizia con regole proprie.
