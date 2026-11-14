---
label: Backup del database
order: 90
---

# Backup del database

Un backup del database è una copia completa del database di Marginalia: tutti gli utenti con tutti i loro libri, lorebook, impostazioni e dati delle estensioni. Usalo per proteggere l'installazione da errori e guasti e per spostarla su un altro computer. Per fare il backup di un singolo libro, gli utenti possono usare i [backup dei libri](../books/backups.md).

![La scheda Backup del database](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Creare un backup

Fai clic su **Crea backup** nella scheda **Backup del database**. La copia viene creata mentre Marginalia è in esecuzione, ed è coerente anche quando le persone stanno scrivendo. Il backup compare nell'elenco con il suo **Nome**, la data **Creato**, il **Tipo** (*Manuale* o *Pianificato*) e la **Dimensione**. La barra degli strumenti mostra la dimensione attuale del database.

Ogni backup ha:

| Pulsante | |
|---|---|
| **Scarica** | Scarica il file di backup (`.sqlite`) - conserva una copia al di fuori del server. |
| **Ripristina al riavvio** | Ripristina il backup, vedi [sotto](#restoring). |
| cestino | Elimina il backup. |

**Carica backup** aggiunge all'elenco un file di backup (`.sqlite` o `.db`), ad esempio uno scaricato da un'altra installazione. Il file viene verificato per primo e viene rifiutato con il messaggio *The database can't be restored: ...* seguito dal motivo quando non è un database SQLite, è danneggiato, non è un database di Marginalia (il database di un'altra applicazione), oppure proviene da una versione di Marginalia più recente di quella in esecuzione.

!!!warning I backup contengono dati sensibili
Un backup del database contiene tutto, comprese le password (in forma di hash) di tutti gli utenti e le loro chiavi API. Le chiavi API sono cifrate con il file `secret.key` nella [cartella dei dati](index.md#the-data-folder), che non fa parte del backup - ma chiunque possieda sia il backup sia `secret.key` può leggerle. Conserva i backup scaricati con la stessa cura che riservi al server stesso.
!!!

I file sono memorizzati nella cartella `db-backups` della [cartella dei dati](index.md#the-data-folder). Anche le copie che Marginalia crea da sola - `pre-restore-...` prima di un [ripristino](#restoring) e `pre-migration-...` prima di un [aggiornamento](#copy-before-an-upgrade) - sono presenti nell'elenco, indicate come *Manuale*, quindi non vengono mai eliminate dalla rotazione dei backup pianificati. I backup dei libri e i JAR delle estensioni sono file separati e non fanno parte di un backup del database.

**Anche i file caricati (le [immagini delle parti](../books/story-editor.md#images)) non fanno parte di un backup del database.** Il database conosce soltanto le loro voci nella scheda [Risorse](../resources.md); i file si trovano nella cartella `data/<login>/images` della cartella dei dati. Va bene così: dopo un ripristino le voci sono quelle del momento del backup e le immagini che usano sono ancora nella cartella. I file delle immagini aggiunte dopo il backup restano semplicemente inutilizzati (nessuno vi fa riferimento) e occupano spazio su disco. Per tenere al sicuro le immagini, includi l'intera cartella dei dati nei tuoi backup, oppure inseriscile in un [backup del libro](../books/backups.md#pictures-in-a-backup).

Quando ripristini un backup su **un'altra installazione**, copia anche `secret.key` dall'installazione originale (con Marginalia arrestato); altrimenti le chiavi API salvate non possono essere decifrate e ogni utente dovrà inserirle di nuovo.

<a id="scheduled-backups"></a>

## Backup pianificati

La sezione **Backup pianificati** crea i backup automaticamente:

| Campo | |
|---|---|
| **Crea i backup automaticamente** | Attiva i backup pianificati. |
| **Pianificazione (cron)** | Quando crearli, vedi [sotto](#schedule-syntax). Sotto il campo vengono mostrati i prossimi tre orari. |
| **Conserva i backup pianificati** | Quanti backup pianificati conservare; quelli più vecchi vengono eliminati. `0` li conserva tutti. I backup manuali e quelli caricati non vengono mai eliminati. |

Fai clic su **Salva pianificazione** per applicarla. La riga di stato mostra quando è stato creato l'ultimo backup pianificato e quando è previsto il successivo.

Se Marginalia non era in esecuzione all'orario pianificato - situazione tipica per l'app desktop, che funziona solo mentre scrivi - crea il backup mancante circa 30 secondi dopo il successivo avvio.

<a id="schedule-syntax"></a>

### Sintassi della pianificazione

La pianificazione è un'espressione cron con cinque campi, `minute hour day-of-month month day-of-week`:

| Espressione | Esegue |
|---|---|
| `0 3 * * *` | ogni giorno alle 3:00 |
| `0 */6 * * *` | ogni 6 ore |
| `30 2 * * 1` | il lunedì alle 2:30 (giorno della settimana: `0` o `7` è domenica, `1` è lunedì...) |
| `0 12 1 * *` | il primo giorno di ogni mese a mezzogiorno |
| `@daily`, `@weekly`, `@monthly` | una volta al giorno / alla settimana / al mese a mezzanotte (funzionano anche `@hourly`, `@yearly`) |

Gli orari sono espressi nel fuso orario del server (in Docker, impostalo con la variabile d'ambiente `TZ`).

!!!
Per disattivare i backup pianificati, deseleziona *Crea i backup automaticamente* e fai clic su *Salva pianificazione*. L'espressione viene conservata, quindi potrai riattivare i backup più avanti. Viene comunque verificata - un'espressione non valida non viene mai salvata - ma mentre i backup sono disattivati il campo può anche restare vuoto.
!!!

<a id="restoring"></a>

## Ripristino

Un database non può essere sostituito mentre Marginalia lo sta usando, quindi il ripristino avviene al **successivo avvio**:

1. Fai clic su **Ripristina al riavvio** accanto al backup e conferma. La scheda mostra il messaggio *Database restore is scheduled. Restart the application to apply it.* - **Annulla il ripristino** lo annulla.
2. Riavvia Marginalia: chiudi e riapri l'[app desktop](../getting-started/desktop-app.md), oppure esegui `docker compose restart` sul [server](../getting-started/docker-server.md).

All'avvio, il database attuale viene spostato in `db-backups/pre-restore-<time>-marginalia.sqlite` - compare nell'elenco, quindi un ripristino può essere annullato ripristinando quel file - e il backup prende il suo posto. Un backup di una versione precedente di Marginalia viene aggiornato automaticamente.

Il backup viene verificato di nuovo prima di essere sostituito (con gli stessi controlli di *Carica backup*; anche *Ripristina al riavvio* rifiuta un backup che non li supera). Se non supera la verifica, oppure se il suo aggiornamento fallisce, Marginalia mantiene (o ripristina) il database attuale e avvia normalmente; il file rifiutato viene spostato in `db-backups/rejected-restore-<time>-marginalia.sqlite` e il log spiega il motivo.

Tutto ciò che è stato creato dopo il backup va perso: libri, parti e impostazioni. Gli utenti che hanno effettuato l'accesso devono accedere di nuovo se il loro account è cambiato.

<a id="copy-before-an-upgrade"></a>

## Copia prima di un aggiornamento

Quando una nuova versione di Marginalia deve modificare la struttura di un database esistente, salva prima una copia come `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, dove la versione è quella a cui era arrivato il database. La copia è presente nell'elenco dei backup, quindi un aggiornamento può essere annullato installando la vecchia versione di Marginalia e [ripristinando](#restoring) quella copia. Vengono conservate le ultime tre copie. Una nuova installazione non ha nulla da copiare e, se la copia non può essere creata (ad esempio perché il disco è pieno), il log lo segnala e l'aggiornamento prosegue.

Una migrazione che fallisce a metà viene annullata automaticamente e Marginalia non si avvia; la copia serve per un aggiornamento completato che però ha prodotto qualcosa che non vuoi.

<a id="moving-to-another-computer"></a>

## Spostarsi su un altro computer

1. Crea un backup e **Scaricalo**.
2. Installa Marginalia sul nuovo computer e crea un amministratore temporaneo al primo avvio.
3. **Carica backup**, **Ripristina al riavvio**, riavvia.
4. Accedi con gli account del backup.

Copia anche la cartella `data` (i backup dei libri e le immagini delle parti) e la cartella `extensions` dalla vecchia [cartella dei dati](index.md#the-data-folder). Oppure, più semplicemente, arresta Marginalia e copia l'intera cartella `.marginalia`.
