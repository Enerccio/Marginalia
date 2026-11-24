---
label: Backup del database
order: 90
---

# Backup del database

Un backup del database è una copia completa del database di Marginalia: tutti gli utenti con tutti i loro libri, lorebook, impostazioni e dati delle estensioni. Usalo per proteggere l'installazione da errori e guasti, e per spostarla su un altro computer. Per il backup di un singolo libro, gli utenti hanno i [backup dei libri](../books/backups.md).

![La scheda Backup del database](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Creare un backup

Fai clic su **Create Backup** nella scheda **Database Backups**. La copia viene creata mentre Marginalia è in esecuzione, ed è coerente anche quando le persone stanno scrivendo. Il backup compare nell'elenco con il suo **Name**, la data **Created**, il **Type** (*Manual* o *Scheduled*) e le **Size**. La barra degli strumenti mostra la dimensione attuale del database.

Ogni backup ha:

| Pulsante | |
|---|---|
| **Download** | Scarica il file di backup (`.sqlite`) - conserva una copia al di fuori del server. |
| **Restore on restart** | Ripristina il backup, vedi [sotto](#restoring). |
| cestino | Elimina il backup. |

**Upload Backup** aggiunge all'elenco un file di backup (`.sqlite` o `.db`), ad esempio uno scaricato da un'altra installazione. Il file viene prima verificato e rifiutato con *The database can't be restored: ...* e il motivo se non è un database SQLite, è danneggiato, non è un database di Marginalia (il database di un'altra applicazione), oppure proviene da una versione di Marginalia più recente di quella in esecuzione.

!!!warning I backup contengono dati sensibili
Un backup del database contiene tutto, comprese le hash delle password di tutti gli utenti e le loro chiavi API. Le chiavi API sono cifrate con il file `secret.key` nella [cartella dei dati](index.md#the-data-folder), che non fa parte del backup - ma chiunque possieda sia il backup sia `secret.key` può leggerle. Conserva i backup scaricati con la stessa cura del server stesso.
!!!

I file sono memorizzati nella cartella `db-backups` della [cartella dei dati](index.md#the-data-folder). Anche le copie che Marginalia crea da sola - `pre-restore-...` prima di un [ripristino](#restoring) e `pre-migration-...` prima di un [aggiornamento](#copy-before-an-upgrade) - sono presenti nell'elenco, indicate come *Manual*, quindi non vengono mai eliminate dalla rotazione dei backup programmati. I backup dei libri e i JAR delle estensioni sono file separati e non fanno parte del backup del database.

**Anche i file caricati (le [immagini delle parti](../books/story-editor.md#images)) non fanno parte del backup del database.** Il database conosce soltanto le loro voci nella scheda [Resources](../resources.md); i file si trovano nella cartella `data/<login>/images` della cartella dei dati. Va bene così: dopo un ripristino le voci sono quelle del momento del backup e le immagini che usano sono ancora nella cartella. I file delle immagini aggiunte dopo il backup restano semplicemente inutilizzati (nulla vi fa riferimento) e occupano spazio su disco. Per tenere al sicuro le immagini, includi l'intera cartella dei dati nei tuoi backup, oppure inseriscile in un [backup del libro](../books/backups.md#pictures-in-a-backup).

Quando ripristini un backup su **un'altra installazione**, copia anche `secret.key` dall'installazione originale (con Marginalia fermo), altrimenti le chiavi API salvate non possono essere decifrate e ogni utente deve inserirle di nuovo.

<a id="scheduled-backups"></a>

## Backup programmati

La sezione **Scheduled Backups** crea i backup automaticamente:

| Campo | |
|---|---|
| **Create backups automatically** | Attiva i backup programmati. |
| **Schedule (cron)** | Quando crearli, vedi [sotto](#schedule-syntax). Sotto il campo vengono mostrati i prossimi tre orari. |
| **Keep scheduled backups** | Quanti backup programmati conservare; quelli più vecchi vengono eliminati. `0` li conserva tutti. I backup manuali e quelli caricati non vengono mai eliminati. |

Fai clic su **Save Schedule** per applicarla. La riga di stato mostra quando è stato creato l'ultimo backup programmato e quando è previsto il successivo.

Quando Marginalia non era in esecuzione in un orario programmato - caso tipico per l'app desktop, che gira solo mentre scrivi - crea il backup mancato circa 30 secondi dopo il successivo avvio. Disattivare la pianificazione o modificarla entro quei 30 secondi annulla quel backup.

<a id="schedule-syntax"></a>

### Sintassi della pianificazione

La pianificazione è un'espressione cron a cinque campi, `minute hour day-of-month month day-of-week`:

| Espressione | Esecuzione |
|---|---|
| `0 3 * * *` | ogni giorno alle 3:00 |
| `0 */6 * * *` | ogni 6 ore |
| `30 2 * * 1` | il lunedì alle 2:30 (giorno della settimana: `0` o `7` è domenica, `1` lunedì...) |
| `0 12 1 * *` | il primo giorno di ogni mese a mezzogiorno |
| `@daily`, `@weekly`, `@monthly` | una volta al giorno / alla settimana / al mese a mezzanotte (anche `@hourly`, `@yearly` funzionano) |

Gli orari sono nel fuso orario del server (in Docker, impostalo con la variabile d'ambiente `TZ`).

!!!
Per disattivare i backup programmati, deseleziona *Create backups automatically* e fai clic su *Save Schedule*. L'espressione viene conservata, così potrai riattivare i backup più tardi. Viene comunque controllata - un'espressione non valida non viene mai salvata - ma mentre i backup sono disattivati il campo può anche restare vuoto.
!!!

<a id="restoring"></a>

## Ripristino

Un database non può essere sostituito mentre Marginalia lo sta usando, quindi il ripristino avviene al **successivo avvio**:

1. Fai clic su **Restore on restart** accanto al backup e conferma.
   La scheda mostra *Database restore is scheduled. Restart the application to apply it.* - **Cancel Restore** lo annulla.
2. Riavvia Marginalia: chiudi e riapri l'[app desktop](../getting-started/desktop-app.md), oppure esegui `docker compose restart` sul [server](../getting-started/docker-server.md).

All'avvio, il database attuale viene spostato in `db-backups/pre-restore-<time>-marginalia.sqlite` - compare nell'elenco, quindi un ripristino può essere annullato ripristinando quel file - e il backup prende il suo posto. Un backup proveniente da una versione più vecchia di Marginalia viene aggiornato automaticamente.

Il backup viene controllato di nuovo prima di essere sostituito (gli stessi controlli di *Upload Backup*; anche *Restore on restart* rifiuta un backup che non li supera). Se non supera il controllo, o se fallisce il suo aggiornamento, Marginalia mantiene (o ripristina) il database attuale e si avvia normalmente; il file rifiutato viene spostato in `db-backups/rejected-restore-<time>-marginalia.sqlite` e il log spiega il motivo.

Tutto ciò che è stato creato dopo il backup va perso: libri, parti e impostazioni. Gli utenti che hanno effettuato l'accesso devono accedere di nuovo se il loro account è stato modificato.

<a id="copy-before-an-upgrade"></a>

## Copia prima di un aggiornamento

Quando una nuova versione di Marginalia sta per modificare la struttura di un database esistente, salva prima una copia con il nome `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, dove la versione è quella in cui si trovava il database. La copia compare nell'elenco dei backup, quindi un aggiornamento può essere annullato installando la vecchia versione di Marginalia e [ripristinando](#restoring) quella copia. Vengono conservate le ultime tre copie. Una nuova installazione non ha nulla da copiare, e se la copia non può essere creata (ad esempio perché il disco è pieno), il log lo segnala e l'aggiornamento prosegue.

Una migrazione che fallisce a metà viene annullata automaticamente e Marginalia non si avvia; la copia serve per un aggiornamento che è andato a buon fine ma ha prodotto qualcosa che non vuoi.

<a id="moving-to-another-computer"></a>

## Trasferimento su un altro computer

1. Crea un backup e **Download** lo.
2. Installa Marginalia sul nuovo computer e, al primo avvio, crea un amministratore temporaneo.
3. **Upload Backup**, **Restore on restart**, riavvia.
4. Accedi con gli account del backup.

Copia anche la cartella `data` (i backup dei libri e le immagini delle parti) e la cartella `extensions` dalla vecchia [cartella dei dati](index.md#the-data-folder). Oppure, più semplicemente, ferma Marginalia e copia l'intera cartella `.marginalia`.
