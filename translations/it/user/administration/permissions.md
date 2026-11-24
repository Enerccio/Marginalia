---
label: Permessi
order: 95
---

# Permessi

Un permesso consente a un utente di fare qualcosa che non è alla portata di tutti. Gli amministratori assegnano i permessi
agli utenti uno per uno; un utente che non ne ha nessuno può fare tutto il resto in Marginalia come di consueto (scrivere
libri, costruire lorebook, usare i modelli che gli amministratori [condividono](../inference-providers.md#shared-providers)).

| Permesso | Cosa consente | Impostazioni |
|---|---|---|
| **Crea modelli propri** | Aggiungere [fornitori di inferenza](../inference-providers.md): senza di esso l'utente può usare solo i fornitori che gli amministratori condividono; i fornitori che ha già continuano a funzionare e possono ancora essere modificati. | |
| **Assistente di aiuto** | Fare domande all'[assistente di aiuto](help-assistant.md), la chat che risponde a partire dal manuale. | **Applica limiti** e **Consenti la navigazione dell'interfaccia**, vedi [Impostazioni di un permesso](#settings-of-a-permission). |
| **Agenti** | (Sperimentale, vedi [Agenti](../agents.md)) La scheda [Agenti](../agents.md): creare agenti, clonarli ed eseguirli su un libro. | **Numero massimo di agenti**, **Esecuzioni contemporanee**, **Richieste al modello contemporanee**, **Budget di token per esecuzione** e le quote di copie e file, vedi [Impostazioni di un permesso](#settings-of-a-permission). |
| **Cronologia delle versioni** | Il numero massimo di [versioni di una parte](../books/version-history.md#how-many-versions-are-kept) che la cronologia dei libri dell'utente conserva. Ogni utente la ha con 50 (anche un utente senza la riga); un permesso non concesso significa nessuna cronologia per quell'utente. | **Versioni conservate per parte**, un limite che le cartelle, i libri e l'utente non possono alzare. Un numero vuoto non impone alcun limite qui. Un permesso appena aggiunto parte da 50. |
| **Accesso API** | Chiavi della [API HTTP](../account.md#api-keys): programmi che agiscono come l'utente nei suoi libri. Togliere il permesso blocca le chiavi dell'utente. Gli amministratori non hanno bisogno del permesso, e solo loro possono dare a una chiave l'accesso all'aggiornamento o alle estensioni. | |
| **Notifiche** | Il [messaggio a un URL](../agents.md#when-you-are-not-there) che il server invia quando un agente ha bisogno dell'utente e nessuna schermata è aperta. Il server chiama un indirizzo digitato dall'utente, quindi è un permesso a sé stante: senza di esso i campi non compaiono nelle impostazioni dell'utente e non viene inviato nulla. | **Consenti indirizzi di questa macchina e delle reti private**, vedi [Impostazioni di un permesso](#settings-of-a-permission). |

Gli amministratori hanno sempre tutti i permessi, non ne hanno bisogno, quindi non c'è nulla da assegnare loro (i pulsanti
e le sezioni di un amministratore non dipendono mai da un permesso). I nuovi utenti non hanno alcun permesso: assegnate
loro ciò di cui hanno bisogno subito dopo averli [aggiunti](users.md#adding-a-user).

<a id="giving-and-taking-permissions"></a>

## Assegnare e revocare permessi

Il pulsante con la chiave in **Admin → Utenti**, nella riga dell'utente, apre i permessi di quell'utente:

![I permessi di un utente](../../images/permissions-dialog.png)

La finestra è una tabella dei permessi dell'utente, con tre colonne:

| Colonna | |
|---|---|
| **Nome** | Il permesso. Un permesso che ha impostazioni ha una freccia davanti al nome, vedi [Impostazioni di un permesso](#settings-of-a-permission). |
| **Concesso** | Se il permesso funziona. Un permesso non concesso equivale a nessuno, ma viene conservato con le sue impostazioni, così si può disattivare per un po' e riattivare in seguito. |
| Il pulsante cestino | Elimina il permesso (e le sue impostazioni) in modo definitivo. |

Per assegnare un permesso, fate clic su **Aggiungi permesso** sopra la tabella e scegliete uno dei permessi che l'utente
non ha; viene aggiunto alla tabella, concesso. Non si salva nulla finché non fate clic su **Salva**:

![Scelta di un permesso da aggiungere](../../images/permissions-add.png)

- **Salva** è disattivato finché non c'è nulla da salvare.
- **Esci** chiude la finestra; se ci sono modifiche non salvate, chiede conferma prima, così una modifica non viene mai
  scartata per errore.
- Un permesso che eliminate resta nella tabella fino al salvataggio, e viene eliminato in modo definitivo quando salvate.
  Prima di allora, per ripristinarlo, chiudete la finestra con **Esci** senza salvare.

Dopo il salvataggio, la pagina dell'utente viene ricaricata se è connesso, così i pulsanti e le sezioni che dipendono dai
permessi compaiono o scompaiono subito. La vostra pagina non viene ricaricata, anche quando modificate i vostri stessi
permessi.

<a id="settings-of-a-permission"></a>

## Impostazioni di un permesso

Alcuni permessi hanno impostazioni che appartengono all'utente, non all'intera installazione. La riga di questi permessi
inizia con una freccia: fate clic su di essa per aprire le impostazioni (sono chiuse quando la finestra si apre, e un
permesso appena aggiunto è aperto), fate clic di nuovo per chiuderle. Chiudere la lista serve solo a tenere breve un
elenco lungo; non cambia nulla e non conta come modifica non salvata.

![Le impostazioni del permesso Assistente di aiuto](../../images/permissions-settings.png)

**Assistente di aiuto**, **Agenti** e **Notifiche** le hanno. Quelle dell'assistente di aiuto:

| Impostazione | |
|---|---|
| **Applica limiti** | Per un modello condiviso, se le domande dell'utente contano sul suo limite e sul suo consumo come ogni altra richiesta, vedi [Chi paga](help-assistant.md#who-pays). Disattivata per impostazione predefinita. |
| **Consenti la navigazione dell'interfaccia** | Permette all'assistente di aprire una sezione, selezionare una scheda o aprire la finestra per aggiungere qualcosa quando l'utente gli chiede di indicargli la strada, vedi [Indicare la strada](help-assistant.md#showing-the-way). Disattivata per impostazione predefinita. |

Quelle degli agenti; un campo vuoto indica il valore predefinito:

| Impostazione | |
|---|---|
| **Numero massimo di agenti** | Quanti agenti può avere l'utente, cloni di un agente inclusi. Cinque se vuoto. Zero permette all'utente di tenere ed eseguire i suoi agenti, ma non di crearne o clonarne un altro. Un agente eliminato libera il suo posto. |
| **Esecuzioni contemporanee** | Quante esecuzioni dell'utente possono lavorare nello stesso momento. Una se vuoto. |
| **Richieste al modello contemporanee (per esecuzione)** | Quante richieste al modello una singola esecuzione può avere in corso nello stesso momento, dell'agente e dei suoi [sotto-agenti](../agents.md#sub-agents) insieme. Una se vuoto: mentre un sotto-agente chiede qualcosa al modello, l'agente attende, e viceversa. Un valore più alto permette ai sotto-agenti di lavorare in parallelo con l'agente (anche il modello dell'agente può avere un proprio [limite di richieste](../inference-providers.md#general-settings), che si applica comunque). |
| **Budget di token per esecuzione** | Il numero massimo di token (inviati e generati) che una esecuzione dell'utente può consumare. Vuoto o zero significa nessun limite. |
| **Copie di lavoro** | Quante copie di lavoro dei libri le esecuzioni dell'utente mantengono contemporaneamente. Un'esecuzione non terminata mantiene la sua copia, anche se in pausa, e la restituisce quando termina, viene interrotta o eliminata. Vuoto o zero significa nessun limite oltre al numero di agenti. |
| **Dimensione massima del file (KB)** | Il file più grande che un agente dell'utente può scrivere nel proprio spazio di lavoro. 1024 (1 MB) se vuoto. |
| **Dimensione dello spazio di lavoro (MB)** | Quanto possono occupare in totale tutti i file nello spazio di lavoro di un agente dell'utente. 20 se vuoto. |
| **File in uno spazio di lavoro** | Quanti file può contenere lo spazio di lavoro di un agente dell'utente; le cartelle non contano. 500 se vuoto. |
| **Checkpoint di un'esecuzione (MB)** | Quanto possono occupare in totale tutti i [checkpoint](../agents.md#checkpoints) di una esecuzione (punti di ripristino della copia di lavoro e del suo spazio di lavoro); una parte è riservata a quelli automatici. 100 se vuoto. |

![Le impostazioni del permesso Agenti](../../images/permissions-settings-agents.png)

Quelle delle notifiche:

| Impostazione | |
|---|---|
| **Consenti indirizzi di questa macchina e delle reti private** | Un sotto-permesso: la casella può essere spuntata solo mentre **Notifiche** è concesso (altrimenti è disattivata, e togliere il permesso la svuota). Senza di essa vengono chiamati solo indirizzi su internet: non `localhost`, non una rete privata (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), non un indirizzo link-local o condiviso (`169.254.x.x`, che è dove gli host cloud conservano il loro servizio di metadati, `100.64-127.x.x`), non gli indirizzi IPv6 locali unici. Spuntatela per un utente il cui ntfy o Gotify gira sulla stessa macchina o sulla vostra rete. Disattivata per impostazione predefinita. Un nome viene verificato in base agli indirizzi in cui si risolve, quindi viene rifiutato anche un nome che punta a un indirizzo privato, e anche uno che non può essere trovato. |

Gli amministratori non hanno bisogno né del permesso né del sotto-permesso: ricevono i messaggi e possono usare indirizzi
locali. Su un server condiviso con persone di cui non vi fidate completamente, lasciate disattivato il sotto-permesso: il
server altrimenti farebbe richieste a qualunque cosa l'utente digiti, all'interno della vostra rete.

<a id="when-permissions-were-introduced"></a>

## Quando sono stati introdotti i permessi

Prima dei permessi, **Gli utenti possono creare i propri modelli** e l'interruttore **Attivo** dell'assistente di aiuto
(con **Applica limiti** e **Consenti la navigazione dell'interfaccia**) erano impostazioni dell'intera installazione.
Marginalia le ha spostate nei permessi durante l'aggiornamento: ogni utente esistente ha ricevuto **Crea modelli propri**
se tutti potevano creare modelli (che era il valore predefinito), e **Assistente di aiuto** con le stesse due
impostazioni se l'assistente era attivo. Per loro non cambia nulla finché non lo modificate; gli utenti che aggiungete
da ora in poi partono senza alcun permesso.
