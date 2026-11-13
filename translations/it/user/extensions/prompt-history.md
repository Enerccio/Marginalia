---
label: Cronologia dei prompt
order: 5
---

# Cronologia dei prompt

Mostra ciò che è stato effettivamente inviato al modello e ciò che ha risposto, per il debug di un plugin o per verificare cosa c'è nel prompt di qualsiasi cosa diversa da una generazione normale.

<a id="how-it-works"></a>

## Come funziona

L'estensione conserva le ultime **20 richieste** inviate ai modelli nella tua sessione: i messaggi inviati, la risposta e
il ragionamento del modello (quando il provider lo invia). Vengono conservati tutti i tipi di richiesta, anche le generazioni. Aggiunge il pannello **Cronologia dei prompt** a *Impostazioni → Impostazioni estensioni*; il pulsante **Mostra cronologia dei prompt** apre l'elenco, con la richiesta più recente per prima, e con:

- la **data** della richiesta,
- il **modello**: il nome dell'IA e l'id del modello,
- il **tipo di richiesta**: `generation` (una nuova parte, uno swipe, una rigenerazione), `annotate`, `summary`, `impersonate`,
  `redraft`, `agent`, `help`, oppure il nome del tipo di lavoro proprio di un'estensione,
- lo stato (in esecuzione, completata, annullata, non riuscita) e quanto tempo ha impiegato.

Scegli una richiesta per leggerla nelle schede sotto l'elenco: **Request** (ogni messaggio sotto il proprio ruolo), **Reasoning**,
e **Response** (la risposta, le funzioni che il modello ha chiamato e l'errore di una richiesta non riuscita). **Aggiorna** ricarica l'elenco e **Svuota** lo svuota. Una richiesta compare nell'elenco quando viene inviata: mentre è in esecuzione, la sua risposta e il suo ragionamento crescono nella finestra man mano che il modello li scrive.

![La cronologia delle richieste](../../images/plugin-prompt-history.png)

Per un [agente](../agents.md), il piè di pagina della sua console ha il pulsante **Cronologia dei prompt**: apre lo stesso elenco con le richieste di quella esecuzione.

<a id="notes"></a>

## Note

- La cronologia è conservata solo in memoria, compressa per occupare poco spazio. Viene persa quando esci dalla sessione o il server si riavvia, e non fa parte di un backup.
- Ogni sessione del browser ha il proprio elenco. Le richieste di un agente non vi compaiono: sono conservate per esecuzione (20 per esecuzione, per le ultime 10 esecuzioni) e mostrate dalla console dell'esecuzione.
- I messaggi sono mostrati per intero, testo della storia compreso.
