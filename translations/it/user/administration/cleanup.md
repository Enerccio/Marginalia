---
label: Pulizia
order: 80
---

# Pulizia

Quando gli utenti eliminano qualcosa in Marginalia - un libro, una parte della storia, un lorebook, un fornitore di IA - l'oggetto scompare
dalle loro liste, ma resta nel database. Questo rende l'eliminazione sicura e mantiene funzionanti i riferimenti (un libro
sa ancora quale fornitore ha scritto le sue parti). Fino a quel momento gli utenti possono ripristinare gli oggetti eliminati dalla scheda
[Cestino](../trash.md). **Pulizia** rimuove definitivamente questi dati eliminati, per mantenere il database leggero.

![La scheda Pulizia](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Eseguire una pulizia

!!!warning
La pulizia non può essere annullata: ciò che rimuove sparisce anche dal [Cestino](../trash.md). [Crea prima un backup del database](database-backups.md).
!!!

1. Apri la scheda **Pulizia** della pagina di amministrazione e clicca su **Analizza**. Per ora non viene modificato nulla.
2. Controlla il risultato (vedi sotto).
3. Clicca su **Esegui la pulizia** e conferma *n oggetti saranno rimossi definitivamente*.

La pulizia riguarda i dati di tutti gli utenti.

La pulizia elimina voci dal database, e solo i file degli [agenti](../agents.md): la workspace di un agente rimosso e la cartella di un'esecuzione rimossa. I file delle [risorse](../resources.md) eliminate (le immagini delle parti) restano nella cartella dei dati del loro proprietario finché non li rimuovi nella scheda [File delle risorse](resource-files.md).

<a id="seeing-what-is-deleted"></a>

## Vedere cosa è stato eliminato

La sezione **Cestino** sotto le tabelle elenca gli oggetti eliminati di tutti gli utenti, una riga per oggetto (aprila per caricare la tabella),
così puoi vedere cosa rimuoverebbe una pulizia e ripristinare ciò che qualcuno ti ha chiesto di recuperare. È la stessa tabella
della scheda [Cestino](../trash.md), inclusi i filtri per utente e tipo e il collegamento *Contenuto esteso*.

![La sezione Cestino della scheda Pulizia](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Leggere l'analisi

La prima tabella ha una riga per ogni tipo di dato:

| Colonna | |
|---|---|
| **Entità** | Il tipo di dato: `Manuscript` (libri), `ChatMessage` (parti della storia), `Lorebook`, `LorebookEntry`, `AI` (fornitori di IA), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Eliminati** | Quanti sono eliminati. |
| **Da rimuovere** | Quanti saranno rimossi da *Esegui la pulizia*, inclusi i dati che appartengono a oggetti eliminati. |
| **Bloccati** | Eliminati, ma ancora in uso, quindi vengono conservati. |

**Oggetti eliminati ancora in uso** elenca gli oggetti bloccati con il nome e **Referenziato da** - ciò che li usa ancora.

<a id="what-is-removed"></a>

## Cosa viene rimosso

La pulizia rimuove gli oggetti eliminati che nessun oggetto attivo usa più, insieme ai dati che appartengono solo a loro:

| Eliminato | Rimosso insieme |
|---|---|
| un libro | tutte le sue parti della storia, i riassunti e i collegamenti dei tag |
| un lorebook | le sue voci e i collegamenti dei tag; gli altri lorebook non lo includono più come sotto-lorebook |
| una parte della storia | la parte (i suoi figli erano già stati spostati nel genitore) e il suo riassunto |
| un fornitore di IA o un protocollo | rimosso dai valori predefiniti degli utenti in *Impostazioni* |
| un tag | i suoi collegamenti a libri e voci |
| un utente | tutto ciò che possedeva: libri (con le loro parti della storia e i riassunti), lorebook, fornitori di IA, protocolli, tag e impostazioni |

<a id="what-is-kept"></a>

## Cosa viene conservato

Un oggetto eliminato è **bloccato** - conservato - finché qualcosa di attivo lo usa ancora:

| Bloccato | Perché | Per sbloccarlo |
|---|---|---|
| un fornitore di IA o un protocollo eliminato | un libro lo usa ancora come modello o protocollo | selezionane un altro nella scheda *Informazioni* del libro, oppure elimina il libro |
| un lorebook eliminato (con le sue voci) | un libro lo usa ancora | seleziona un altro lorebook nel libro, oppure svuotalo |
| una parte della storia eliminata | è l'ultima parte del ramo attivo di un libro | passa il libro a un altro ramo, oppure elimina il libro |
| un utente eliminato | qualcosa di un altro utente usa ancora i suoi dati (un libro che usa il suo fornitore di IA o il suo lorebook) | modifica o elimina il libro dell'altro utente |

Esegui di nuovo *Analizza* dopo aver sbloccato.

<a id="reference-model"></a>

## Modello di riferimento

**Modello di riferimento** in fondo elenca ogni riferimento tra i tipi di dato e la sua politica - utile quando
vuoi capire perché qualcosa è bloccato:

| Politica | Significato |
|---|---|
| **Blocca la rimozione** | Un oggetto eliminato di destinazione viene conservato finché un oggetto attivo vi fa riferimento. |
| **Rimosso con la destinazione** | L'oggetto che fa riferimento appartiene alla destinazione ed è rimosso con essa (le voci insieme al loro lorebook). |
| **Possiede la destinazione** | La destinazione appartiene all'oggetto che fa riferimento (un riassunto alla sua parte). |
| **Svuotato alla rimozione** | Il riferimento viene rimosso quando la destinazione viene rimossa (sotto-lorebook, modello predefinito). |

Le estensioni possono partecipare alla pulizia con le proprie regole.
