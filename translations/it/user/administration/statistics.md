---
label: Statistiche
order: 87
---

# Statistiche

Le [statistiche di scrittura](../books/statistics.md) di ogni utente (le parole, il tempo trascorso, la dimensione di ciascun libro, giorno per giorno) sono conservate
nel database. Registrano quando gli utenti hanno lavorato ai loro libri e i nomi e le cartelle dei libri, anche di quelli
eliminati. La scheda **Statistiche** della pagina Admin ti offre gli strumenti per limitare tutto questo. Gli utenti possono fare lo stesso per sé stessi nelle
loro [impostazioni](../account.md) e cancellare le proprie statistiche nelle finestre delle statistiche.

![La scheda Statistiche della pagina Admin](../../images/admin-statistics.png)

| | |
|---|---|
| **Registra le statistiche di scrittura** | Disattivato interrompe la registrazione per tutti gli utenti, qualunque sia la loro scelta. Ciò che era già stato registrato non viene eliminato. Le finestre delle statistiche indicano che non viene conteggiato nulla di nuovo. |
| **Conserva le statistiche per (mesi)** | I giorni più vecchi di questo periodo vengono eliminati in modo definitivo dalle statistiche di tutti gli utenti, una volta al giorno (verso le 4:20, e poco dopo l'avvio) e al salvataggio. Vuoto o 0 conserva tutto. Un utente può scegliere per sé un periodo più breve, mai più lungo. |
| **Cancella le statistiche dell'utente** | Elimina tutte le statistiche dell'utente scelto, dopo una domanda di conferma. I suoi libri non vengono modificati. |
| **Cancella le statistiche di tutti** | Lo stesso, per tutti gli utenti. |

Le statistiche eliminate non possono essere recuperate, se non da un [backup del database](database-backups.md): le statistiche fanno parte del
database (il backup di un singolo libro non le include).

La [pulizia](cleanup.md) non tocca le statistiche, perché non sono oggetti eliminati: contengono il libro tramite il suo id e gli sopravvivono di proposito.
