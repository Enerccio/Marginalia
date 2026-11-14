---
label: File di risorse
order: 85
---

# File di risorse

I file delle [risorse](../resources.md) (le immagini che gli utenti allegano alle parti della storia, i file caricati) sono conservati nella
cartella dati di ciascun utente, in `data/<login>/images` e `data/<login>/resources`. Eliminando una risorsa, o sostituendone il
contenuto, cambia soltanto il database: il file resta sul disco, e né la [pulizia](cleanup.md) né altro lo
rimuove. **File di risorse** trova i file che ormai nessuno usa più, permette di scaricarli e li elimina
dal disco per recuperare spazio.

<a id="cleaning-up-the-files"></a>

## Pulizia dei file

1. Apri la scheda **File di risorse** della pagina di amministrazione e fai clic su **Scansiona i file**. Una barra di avanzamento mostra le cartelle che vengono lette.
   Non cambia nulla.
2. La tabella elenca i file **a cui non punta nessuna risorsa**: il percorso (`<user folder>/images/<file>`), la dimensione e la data
   dell'ultima modifica. All'inizio sono tutti selezionati. Deseleziona quelli che vuoi conservare; **Seleziona tutto** e **Deseleziona tutto** cambiano tutti
   i file, e la freccia nell'intestazione della prima colonna fa lo stesso. Una riga sopra la tabella indica quanti dei file
   non sono usati, la loro dimensione e quanti sono selezionati.
3. Facoltativamente fai clic su **Scarica come ZIP** per ottenere i file selezionati in un unico archivio (le voci sono nominate in base al loro percorso), oppure la
   freccia in una riga per un singolo file. È il backup da conservare prima di eliminare.
4. **Elimina i selezionati** rimuove dal disco i file selezionati, dopo la conferma. **Chiudi** svuota la tabella senza eliminare nulla.

![La scheda File di risorse dopo una scansione](../../images/admin-resource-files.png)

!!!warning
I file eliminati non possono essere recuperati. In caso di dubbio, scaricali prima come ZIP.
!!!

<a id="what-counts-as-used"></a>

## Che cosa conta come usato

Un file è usato quando **una qualsiasi risorsa di un qualsiasi utente vi punta**, anche una eliminata: una risorsa nel
[Cestino](../trash.md) può essere ripristinata, e la sua immagine deve tornare a funzionare. Quindi i file delle risorse eliminate vengono elencati
solo dopo che la [pulizia](cleanup.md) ha rimosso le risorse dal database, insieme ai file del contenuto che è stato
sostituito (il vecchio file resta lì) e ai file lasciati da caricamenti non riusciti.

I file vengono confrontati per nome (un identificatore casuale), nelle cartelle di tutti gli utenti, comprese le cartelle `<login>-deleted` degli
utenti eliminati: i loro file restano *usati* finché la pulizia non elimina l'utente e le sue risorse.

Vengono letti solo le cartelle `images` e `resources` all'interno della cartella dati, nient'altro (i backup del database, gli
spazi di lavoro degli agenti e così via non vengono toccati).

Un file che un utente carica dopo la scansione non viene eliminato: **Elimina i selezionati** legge di nuovo il database e conserva i file
a cui le risorse puntano in quel momento (il messaggio indica quanti file sono stati conservati).

La scansione legge la cartella dati, cosa che può richiedere tempo con molti file; **Interrompi** sulla barra di avanzamento la annulla.
