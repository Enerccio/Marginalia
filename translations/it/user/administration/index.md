---
label: Amministrazione
order: 910
icon: tools
---

# Amministrazione

Gli amministratori gestiscono l'installazione di Marginalia: account utente, backup del database, pulizia dei dati eliminati,
aggiornamenti ed estensioni. Il primo account, creato al [primo avvio](../getting-started/first-start.md), è un amministratore;
può rendere amministratori anche altri utenti.

Gli amministratori sono anche utenti normali, con i propri libri: non vedono i libri, i lorebook o le impostazioni degli altri utenti, ma solo i lorebook e i protocolli che quegli utenti condividono con tutti.

<a id="the-admin-page"></a>

## La pagina Admin

Gli amministratori hanno un pulsante **Admin** in fondo all'area di lavoro. Nel [layout per telefono](../phone.md#administration) (una finestra più stretta di 768 pixel) è la scheda **Admin** del cassetto. Apre la pagina di amministrazione con queste schede:

| Scheda | |
|---|---|
| **Utenti** | Creazione, modifica ed eliminazione degli account, e assegnazione dei [permessi](permissions.md) agli utenti. Vedi [Utenti](users.md). |
| **Backup del database** | Backup dell'intero database, su richiesta o pianificati, e relativo ripristino. Vedi [Backup del database](database-backups.md). |
| **Pulizia** | Rimozione definitiva dei dati eliminati. Vedi [Pulizia](cleanup.md). |
| **File di risorse** | Ricerca dei file delle risorse che nessuno usa più, download in un archivio ZIP ed eliminazione dal disco. Vedi [File di risorse](resource-files.md). |
| **Assistente di aiuto** | La chat che risponde alle domande dal manuale: il modello, i prompt e chi lo paga. Vedi [Assistente di aiuto](help-assistant.md). |
| **Agenti** | Il prompt di sistema e il prompt di compattazione che gli agenti seguono quando né loro né il loro utente ne hanno uno proprio. Vuoto significa il prompt predefinito; l'icona informativa ha **Inserisci il template predefinito**, che lo riempie così puoi modificare una copia. Cosa possono fare gli utenti con gli agenti è il loro [permesso](permissions.md). Inoltre **Gli agenti possono inviare messaggi ai webhook degli utenti**: quando un agente ha bisogno di un utente e questo ha l'applicazione chiusa, il server chiama l'indirizzo che l'utente ha inserito nelle proprie impostazioni ([Quando non ci sei](../agents.md#when-you-are-not-there)); disattivalo dove non è accettabile. |
| **Statistiche** | Se le [statistiche di scrittura](../books/statistics.md) vengono registrate, per quanto tempo vengono conservate, e la cancellazione delle statistiche di un utente o di tutti. Vedi [Statistiche](statistics.md). |
| **Processi** | Il registro di tutto ciò che è stato richiesto a un'AI esterna (chi, quando, quali chiamate, come sono terminate), un filtro che elimina a mano le voci vecchie, e la pagina `/jobs` con i processi in esecuzione in questo momento. Vedi [Processi](jobs.md). |
| **Aggiorna** | Su un server Docker: la versione in esecuzione, la versione più recente del ramo seguito dal server e il pulsante che aggiorna e riavvia Marginalia. Vedi [Aggiornamenti](updates.md). |
| **Estensioni** | Installazione e rimozione delle estensioni. Vedi [Gestione delle estensioni](extensions.md). |

![La scheda Agenti della pagina Admin](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## La cartella dei dati

Tutto ciò che Marginalia salva si trova nella cartella `.marginalia` nella home directory dell'utente che esegue il programma (`./data`
sull'host con la [configurazione Docker](../getting-started/docker-server.md)):

| Percorso | |
|---|---|
| `marginalia.sqlite` (con `-wal`, `-shm`) | Il database: utenti, libri, lorebook, impostazioni, tutto. |
| `secret.key` | Chiave che cifra le chiavi API nel database. Creata al primo avvio, leggibile solo dal suo proprietario (su Linux e macOS); senza di essa le chiavi API salvate non possono essere lette. Se è leggibile da altri utenti del computer, il log lo segnala: `chmod 600 secret.key`. |
| `db-backups/` | [Backup del database](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Backup dei libri](../books/backups.md) di ciascun utente. |
| `extensions/` | I JAR delle [estensioni](extensions.md) installate. |
| `update/` | I file con cui Marginalia e il [controllore degli aggiornamenti](updates.md#setting-up-the-watcher) di un server Docker si scambiano informazioni: lo stato, il log dell'ultimo aggiornamento, la richiesta. |
| `agent-workspaces/<agent id>/` | I file di un [agente](../agents.md#the-working-copy) (i suoi appunti e rapporti, che conserva tra un'esecuzione e l'altra). |
| `agent-runs/<run id>/` | Ciò che conserva un'esecuzione di un agente: l'inizio del libro (per mostrarti le modifiche, rimosso insieme alla copia di lavoro), i checkpoint e i risultati (rimossi quando l'esecuzione viene eliminata dalla pulizia). |
| `desktop/logs/` | I log dell'[applicazione desktop](../getting-started/desktop-app.md). |

Per fare il backup di un'intera installazione, ferma Marginalia e copia questa cartella (incluso `secret.key`). Mentre è in esecuzione, usa
i [backup del database](database-backups.md) per il database; i backup dei libri e le estensioni sono file normali e possono essere
copiati in qualsiasi momento.
