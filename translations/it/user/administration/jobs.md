---
label: Processi
order: 86
---

# Processi

Tutto ciò che l'applicazione chiede a un'**IA esterna** è un *processo*: una generazione, un riassunto, un'annotazione, un'esecuzione di un agente, un capitolo
convertito in audio, un'immagine. Ogni processo viene registrato con l'utente per cui è stato eseguito, con l'ora di inizio e di fine, con il modo in cui è terminato e con ogni *chiamata*
che ha fatto a un modello o a un servizio di sintesi vocale o di generazione di immagini, anch'essa con il proprio inizio e la propria fine. La scheda **Processi** della pagina di amministrazione mostra il registro;
la pagina `/jobs` mostra ciò che è in esecuzione in questo momento.

Vengono conservati solo i metadati: chi, quando, per quanto tempo, quale IA o quale server (il nome dell'IA, oppure l'host del server di sintesi vocale o di immagini; mai il percorso dell'indirizzo né la query), il modello, come è terminato e il breve messaggio di errore di un'operazione fallita. **Nessun prompt, nessuna risposta, nessun testo** viene mai memorizzato.

<a id="what-is-one-job"></a>

## Che cos'è un processo

- Una **richiesta che nulla raggruppa** (una generazione, un'annotazione, una riscrittura, un'impersonificazione, il riassunto di una parte, una domanda all'assistente di aiuto, una richiesta di un plugin come la traduzione di una parte o di una voce del lore) è un processo a sé stante con una sola chiamata. Il suo tipo è il tipo di lavoro (`generation`, `annotate`, `summary`...).
- Una **riscrittura dei riassunti** è un processo (`summary`) con una chiamata per ogni riassunto scritto.
- Una **sessione di lavoro di un'esecuzione di un agente** è un processo (`agent`) con una chiamata per ogni round dell'agente; un'esecuzione che viene messa in pausa e ripresa ha un processo per ogni sessione, e le richieste di un sotto-agente sono processi a sé stanti.
- Una **conversione in audio** è un processo (`audiobook`) per ogni gruppo di parti, con le richieste del modello che assegna le etichette e ogni pezzo letto come sue chiamate.
- Un'immagine creata dal **Generatore di immagini** è un processo a sé stante (`image-generator`) con una chiamata di immagine.

<a id="what-runs-now-jobs"></a>

## Cosa è in esecuzione ora: `/jobs`

![La pagina /jobs con una generazione in corso](../../images/jobs-live.png)

Apri `/jobs` all'indirizzo del tuo Marginalia (per esempio `http://localhost:8080/jobs`; il link **Apri la vista in tempo reale** della scheda lo fa per te). Chiede l'accesso come ogni altra pagina e non mostra nulla agli utenti che non sono amministratori. L'elenco si aggiorna ogni due secondi e mostra, per ogni processo in esecuzione, l'utente, il tipo, da quanto tempo è in corso, quante delle sue chiamate sono completate e che cosa è in corso in questo momento (l'IA o il server e da quanto tempo la chiamata è in attesa). È il colpo d'occhio rapido quando il server sembra occupato: un lungo elenco di chiamate in attesa di un solo modello spiega perché tutto è lento.

Sotto i processi, **Esecuzioni sui libri** elenca ciò che lavora ora su un libro e impedisce che parti di esso vengano eliminate o modificate mentre è in uso
(vedi il [pannello delle esecuzioni](../books/story-editor.md#the-panel-of-the-runs), che mostra le stesse informazioni a ciascun utente per i propri
libri): l'utente, il libro, che cosa fa, a che punto si trova (ciò che dice il pannello: *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits
for you*, e per una generazione la sua fase, come *INFERENCE* mentre scrive la propria parte; la finestra di un libro aperto non mostra nulla in quel punto), da quando, e quanti oggetti contiene (il libro, le parti). Anche la finestra di un libro aperto è un'esecuzione. **Interrompi** ferma l'esecuzione come farebbe il suo utente. **Fine**, dopo una domanda, termina un'esecuzione senza interrompere il suo lavoro: ciò che tiene occupato viene restituito subito. Usalo solo per un'esecuzione che non finisce mai (il suo lavoro si è bloccato e tiene occupato il libro fino a un riavvio); il suo lavoro potrebbe comunque scrivere quando si risveglia.

<a id="the-log-the-jobs-tab"></a>

## Il registro: la scheda Processi

![La scheda Processi della pagina di amministrazione](../../images/admin-jobs.png)

| | |
|---|---|
| **Utente, Tipo, Stato** | Filtri. Il *tipo* è ciò che il processo fa (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator`...); una richiesta fatta dal codice che non ha indicato il proprio lavoro è `untagged`. |
| **Più vecchi di (giorni)** | Solo i processi iniziati tanti giorni fa o prima. |
| **Aggiorna** | Rilegge il registro. |
| **Elimina questi processi** | Elimina in modo definitivo i processi mostrati dal filtro e le loro chiamate, dopo una domanda che indica quanti sono. Senza alcun filtro impostato elimina l'intero registro. |
| **Apri la vista in tempo reale** | Apre `/jobs` in una nuova scheda. |

Fai clic su una riga per vedere le chiamate del processo: il tipo (modello linguistico, audio, immagine), l'IA o il server, il modello, inizio, fine, durata,
stato e l'errore di una chiamata fallita. Un processo mostrato come fallito ha effettuato una chiamata fallita, oppure è stato terminato come fallito dal codice che lo eseguiva; una chiamata annullata è una che hai interrotto (o che era ancora in corso quando il suo processo è terminato).

![Le chiamate di una conversione in audiolibro](../../images/admin-jobs-calls.png)

Un processo fallito mostra quale chiamata è fallita e perché:

![Una conversione fallita con l'errore della sua chiamata di sintesi vocale](../../images/admin-jobs-failed.png)

**Nulla viene eliminato da solo.** Il registro cresce con ogni richiesta, quindi svuotalo di tanto in tanto: imposta *Più vecchi di (giorni)*
(per esempio 30), controlla ciò che viene elencato ed elimina. La [pulizia](cleanup.md) rimuove i processi di un utente insieme all'utente stesso.

![I processi più vecchi di 30 giorni, e la domanda prima che vengano eliminati](../../images/admin-jobs-old.png)

![La domanda prima che i processi vengano eliminati](../../images/admin-jobs-delete.png)

Un processo viene scritto nel registro quando termina; un processo ancora in esecuzione è visibile solo su `/jobs`. Se l'applicazione si arresta, i processi in corso vanno persi (non sono mai stati completati). Un processo che nessuno ha terminato e che non effettua alcuna chiamata per mezz'ora viene terminato come completato.
