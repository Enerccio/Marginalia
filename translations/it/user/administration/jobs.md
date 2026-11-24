---
label: Jobs
order: 86
---

# Jobs

Tutto ciò che l'applicazione chiede a una **IA esterna** è un *job*: una generazione, un riassunto, un'annotazione, un'esecuzione di agente, un capitolo convertito in audio, un'immagine. Ogni job viene registrato con l'utente per cui è stato eseguito, l'ora di inizio e di fine, come è terminato e ogni *call* che ha fatto verso un modello o verso un server di voce o di immagini, anch'essa con il proprio inizio e la propria fine. La scheda **Jobs** della pagina Admin mostra il registro; la pagina `/jobs` mostra cosa è in esecuzione in questo momento.

Viene conservato solo il metadato: chi, quando, quanto è durato, quale IA o server (il nome dell'IA, oppure l'host del server di voce o di immagini; mai il percorso dell'indirizzo né la query), il modello, come è terminato e il breve messaggio di errore di un fallimento. **Non viene mai memorizzato alcun prompt, alcuna risposta, alcun testo.**

<a id="what-is-one-job"></a>

## Che cos'è un job

- Una **richiesta che nulla raggruppa** (una generazione, un'annotazione, una riscrittura, un'impersonificazione, il riassunto di una parte, una domanda all'assistente di aiuto, una richiesta di un plugin come la traduzione di una parte o di una voce del lorebook) è un job a sé con una sola call. Il suo tipo è il tipo di lavoro (`generation`, `annotate`, `summary`...).
- Una **riscrittura dei riassunti** è un job (`summary`) con una call per ogni riassunto scritto.
- Una **sessione di lavoro di un'esecuzione di agente** è un job (`agent`) con una call per ogni giro dell'agente; un'esecuzione messa in pausa e poi ripresa ha un job per ogni sessione, e le richieste di un sub-agente sono job a sé.
- Una **conversione in audio** è un job (`audiobook`) per ogni lotto di parti, con le richieste del modello che assegna le etichette e ogni pezzo parlato come call.
- Un'immagine creata dal **Image Generator** è un job a sé (`image-generator`) con una call di immagine.

<a id="what-runs-now-jobs"></a>

## Cosa è in esecuzione ora: `/jobs`

![La pagina /jobs con una generazione in corso](../../images/jobs-live.png)

Apri `/jobs` sull'indirizzo del tuo Marginalia (per esempio `http://localhost:8080/jobs`; il link **Open the live view** della scheda fa lo stesso). Come ogni altra pagina chiede il login e non mostra nulla agli utenti che non sono amministratori. L'elenco si aggiorna ogni due secondi e mostra, per ogni job in esecuzione, l'utente, il tipo, da quanto tempo è in corso, quante delle sue call sono terminate e cosa è in volo in questo momento (l'IA o il server e da quanto tempo la call attende), e il libro dell'esecuzione per cui lavora il job, con il suo **Stop**. È il colpo d'occhio rapido quando il server sembra occupato: un lungo elenco di call in attesa di un solo modello spiega perché tutto è lento.

Sotto i job, **Runs on the books** elenca ciò che lavora su un libro in questo momento e impedisce che parti di esso vengano eliminate o modificate sotto i suoi piedi (vedi il [pannello delle esecuzioni](../books/story-editor.md#the-panel-of-the-runs), che mostra la stessa cosa a ogni utente per i propri libri): l'utente, il libro, che cosa fa, dove si trova (ciò che dice il pannello: *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits for you*, e per una generazione la sua fase, come *INFERENCE* mentre scrive la sua parte; la finestra di un libro aperto non mostra nulla in quel punto), da quando, e quanti oggetti contiene (il libro, le parti). Anche la finestra di un libro aperto è un'esecuzione. **Stop** interrompe l'esecuzione come farebbe il suo utente. **End**, dopo una domanda, termina un'esecuzione senza interrompere il suo lavoro: ciò che tiene occupato viene restituito subito. Usalo solo per un'esecuzione che non finisce mai: il suo lavoro potrebbe comunque scrivere quando si risveglia. La maggior parte di queste esecuzioni termina da sola: un'esecuzione su cui nessuno lavora da un'ora, e che non aspetta né te né il suo modello, viene terminata dal server, e così la finestra di una pagina che è stata chiusa.

<a id="the-log-the-jobs-tab"></a>

## Il registro: la scheda Jobs

![La scheda Jobs della pagina Admin](../../images/admin-jobs.png)

| | |
|---|---|
| **User, Kind, Status** | Filtri. Il *kind* è il tipo di job (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator`...); una richiesta fatta da codice che non ha dichiarato il proprio lavoro è `untagged`. |
| **Older than (days)** | Solo i job iniziati da quel numero di giorni o più. |
| **Refresh** | Rilegge il registro. |
| **Delete these jobs** | Elimina definitivamente i job mostrati dal filtro e le loro call, dopo una domanda che dice quanti sono. Senza filtri impostati elimina l'intero registro. |
| **Open the live view** | Apre `/jobs` in una nuova scheda. |

Clicca una riga per vedere le call del job: il tipo (modello linguistico, audio, immagine), l'IA o il server, il modello, inizio, fine, durata, stato e l'errore di una call fallita. Un job mostrato come fallito ha effettuato una call che è fallita, oppure è stato terminato come fallito dal codice che lo eseguiva; una call annullata è una che hai interrotto (o che era ancora in corso quando il suo job è terminato).

![Le call di una conversione di audiolibro](../../images/admin-jobs-calls.png)

Un job fallito mostra quale call è fallita e perché:

![Una conversione fallita con l'errore della sua call di voce](../../images/admin-jobs-failed.png)

**Nulla viene eliminato da solo.** Il registro cresce con ogni richiesta, quindi svuotalo di tanto in tanto: imposta *Older than (days)* (per esempio 30), controlla ciò che viene elencato ed elimina. La [pulizia](cleanup.md) rimuove i job di un utente insieme all'utente stesso.

![I job più vecchi di 30 giorni, e la domanda prima che vengano eliminati](../../images/admin-jobs-old.png)

![La domanda prima che i job vengano eliminati](../../images/admin-jobs-delete.png)

Un job viene scritto nel registro quando termina; un job ancora in esecuzione è solo su `/jobs`. Se l'applicazione si arresta, i job in esecuzione vanno persi (non sono mai stati conclusi). Un job che nessuno ha terminato e che non effettua call per mezz'ora viene considerato completato.
