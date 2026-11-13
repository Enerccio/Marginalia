---
label: Risoluzione dei problemi e FAQ
order: 900
icon: question
---

# Risoluzione dei problemi e FAQ

<a id="where-are-the-logs"></a>

## Dove si trovano i log?

La maggior parte dei problemi lascia una traccia nel log:

| Installazione | Log |
|---|---|
| App desktop | `~/.marginalia/desktop/logs/marginalia.log` (`%USERPROFILE%\.marginalia\desktop\logs` su Windows); la sessione precedente si trova in `marginalia.log.1` |
| Docker | `docker compose logs -f marginalia` |

Anche le finestre di errore dell'applicazione (*Errore interno del server*) mostrano l'errore; il log contiene tutti i dettagli.

Quando segnali un problema, includi il testo della versione nell'angolo in basso a sinistra della pagina (per esempio
`Marginalia 1.5.0 (93ec118)`: la versione e il commit Git della build).

<a id="installing-and-starting"></a>

## Installazione e avvio

**macOS dice che l'app "è danneggiata" o "non può essere aperta".**
L'app non è ancora firmata. Fai clic destro su di essa e scegli *Apri*, oppure esegui
`xattr -dr com.apple.quarantine /Applications/Marginalia.app`. Vedi [App desktop](getting-started/desktop-app.md).

**SmartScreen di Windows blocca l'app.**
Fai clic su *Ulteriori informazioni* → *Esegui comunque*.

**Il browser apre una porta diversa dalla 8765.**
La porta 8765 era occupata, quindi l'app desktop ne ha scelta una libera. Usa **Apri Marginalia** nel menu della barra di sistema, oppure avviala con
`--port` per fissare la porta.

**"Marginalia non si è avviato in tempo".**
L'app desktop non è riuscita ad avviare il proprio server. Il log indica il motivo - spesso un altro programma che tiene occupati i file, oppure un'estensione difettosa (vedi [sotto](#extensions)).

**Docker: la porta 8080 è già in uso.**
Cambia la parte sinistra della mappatura delle porte in `docker-compose.yml`, per esempio `"8081:8080"`, e apri la porta 8081.

**Marginalia è lento o si interrompe con "OutOfMemoryError".**
Aumenta la memoria: `-Xmx2g` (o più) in `JAVA_TOOL_OPTIONS` in `docker-compose.yml`.

<a id="logging-in"></a>

## Accesso

**"Salva accesso" non mi mantiene collegato.**
I accessi salvati richiedono HTTPS su un server. Metti Marginalia dietro un reverse proxy con HTTPS, vedi
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy).

**Ho dimenticato la password.**
Un amministratore può impostarne una nuova, oppure cancellarla così da poter accedere con una password vuota e impostarne una nuova da solo, in *Admin → Utenti* (vedi [Reimpostare una password dimenticata](administration/users.md#resetting-a-forgotten-password)).
Non esiste il ripristino della password via e-mail.

**Nessuno riesce più ad accedere come amministratore.**
Senza un amministratore, la password può essere reimpostata solo nel database:

1. Ferma Marginalia e copia `marginalia.sqlite` dalla [cartella dei dati](administration/index.md#the-data-folder) come backup.
2. Rimuovi la password dell'amministratore con lo strumento `sqlite3`:
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (usa il nome utente corretto).
3. Avvia Marginalia, accedi con quel nome utente e una password **vuota**, e subito imposta una nuova password con
   **Cambia password**.

**"Connessione al server persa" oppure la pagina continua a ricaricarsi.**
Dietro un reverse proxy, il proxy deve inoltrare i WebSocket e consentire richieste lunghe - vedi l'esempio nginx in
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy). Con l'app desktop, probabilmente l'app è stata chiusa; avviala di nuovo.

**L'area di lavoro blocca il browser all'apertura.**
Marginalia riapre ciò che avevi aperto quando sei uscito, quindi un libro molto lungo (per esempio uno importato da un archivio) può bloccare la pagina prima che tu possa modificare qualsiasi cosa. Apri `/reset` all'indirizzo di Marginalia (per esempio
`https://marginalia.example.com/reset`) e accedi: lo stato salvato dell'area di lavoro (libri aperti, schede selezionate, filtri) viene dimenticato e ti ritrovi in un'area di lavoro vuota. Poi imposta **Parti caricate insieme nella storia** nel tuo [account](account.md) prima di aprire di nuovo il libro.

<a id="models-and-generation"></a>

## Modelli e generazione

**"Impossibile scaricare l'elenco dei modelli".**
Controlla che l'URL termini con `/v1`, che la chiave API sia corretta e che il server del modello sia in esecuzione. Alcuni servizi non elencano i propri modelli - in quel caso digita l'ID del modello. Vedi [Fornitori di inferenza](inference-providers.md#per-type-settings).

**Docker: non si riesce a raggiungere un server di modelli sulla stessa macchina.**
All'interno del container, `localhost` è il container stesso. Usa `http://host.docker.internal:<porta>/v1` e fai in modo che il server del modello ascolti su tutti gli indirizzi, vedi [Modelli sulla stessa macchina](getting-started/docker-server.md#models-on-the-same-machine).

**"Il libro non ha il modello." / "Il libro non ha il protocollo."**
Selezionali nella scheda *Informazioni* del libro. Imposta *Modello predefinito* e *Protocollo predefinito* in *Impostazioni*, così i nuovi libri li riceveranno.

**"Limite di contesto insufficiente."**
Il prompt senza alcuna storia è già più grande dello spazio che il contesto lascia dopo la risposta. Aumenta *Dimensione massima del contesto* del fornitore (o *Token massimi di contesto* del protocollo), riduci *Token massimi di risposta*, oppure accorcia i template, le voci del lorebook e i riassunti. Vedi [Limiti](protocols.md#limits).

**Il modello segnala che il contesto è troppo lungo.**
Il conteggio dei token di Marginalia è leggermente impreciso per alcuni modelli. Riduci *Dimensione massima del contesto* di qualche centinaio di token. Un avviso prima della richiesta indica le dimensioni stimate quando il prompt sembra già troppo grande.

**"Il fornitore non ha risposto in tempo", "Il fornitore sta limitando le richieste", "Il fornitore ha rifiutato la chiave API"...**
Marginalia spiega in parole semplici gli errori dell'API del modello, vedi [Messaggi di errore](inference-providers.md#error-messages). *Prova la connessione* nella finestra del fornitore verifica URL, chiave e modello senza un libro. Timeout e tentativi si impostano per ciascun fornitore.

**Il testo si interrompe a metà di una frase.**
È stato raggiunto il limite di risposta. Aumenta *Token massimi di risposta* (o *Token massimi di risposta* del protocollo) - i modelli di ragionamento ne richiedono molti di più, perché anche il ragionamento viene conteggiato.

**Le richieste falliscono quando il ragionamento è abilitato.**
L'API non supporta `reasoning_effort`. Disattiva *Abilita il ragionamento* nel fornitore.

**"I riassunti dei messaggi con ID ... sono obsoleti... Continuare la generazione e cancellarli?"**
Una parte coperta da un riassunto è stata modificata o eliminata. Rispondi *Sì* per eliminare i riassunti obsoleti e continuare, poi generane di nuovi. Rispondi *No* per fermarti e conservarli: sono contrassegnati nella panoramica dei riassunti, dove puoi riverificare quelli che dicono ancora la cosa giusta. Vedi [Quando i riassunti diventano obsoleti](books/summaries.md#when-summaries-become-outdated).

**Il prompt contiene la parola "Error".**
Un template usa una variabile o una macro che non esiste, spesso per un errore di battitura come `instruction` al posto di `instructions`.
Il campo del template elenca questi nomi sotto di esso. Vedi [Errori di battitura](templates/templates.md#typos).

<a id="lorebooks"></a>

## Lorebook

**Una voce non viene usata.** Controlla le condizioni in [Attivazione](lorebooks/entries.md#activation):

- La voce è abilitata? È abilitato anche il suo lorebook - e ogni sotto-lorebook lungo il percorso che porta ad esso?
- Ha dei tag? Allora il libro deve avere almeno uno di essi (i tag del libro si trovano nella scheda *Informazioni*). I tag negativi la escludono.
- Il lorebook stesso ha **Tag**? Si applicano a tutte le sue voci.
- Ha un filtro? I filtri guardano solo le **istruzioni della parte in scrittura**, non la storia precedente - cita il personaggio in *Personaggi presenti* o nelle istruzioni.
- Il filtro è un'espressione regolare? Una non valida non corrisponde mai.

**Mostra il prompt** su una parte generata mostra quale lore è stata inviata.

<a id="prompts-and-settings"></a>

## Prompt e impostazioni

**Non riesco a passare a un'altra scheda.**
La scheda *Impostazioni* ha modifiche non salvate. Fai clic su **Salva** o **Scarta le modifiche**.

**Il mio prompt di riassunto non viene usato.**
Il prompt di riassunto proprio di un libro (scheda *Prompt*) ha la precedenza sul *Prompt di riassunto predefinito* in *Impostazioni*; svuota il campo del libro per usare quello predefinito. I meta-riassunti hanno un prompt proprio, il *Prompt di meta-riassunto predefinito*.

**Le macro in Stile, Punto di vista o Tempo verbale non funzionano.**
Questi campi sono testo semplice; solo i template elaborano le macro. Inserisci la macro nel template principale o nel prompt utente.

<a id="backups"></a>

## Backup

**Ho ripristinato un backup del database, ma non è cambiato nulla.**
Un ripristino viene applicato al successivo avvio. Riavvia Marginalia. Vedi [Ripristino](administration/database-backups.md#restoring).

**I backup dei libri mancano dopo il trasferimento su un altro computer.**
I backup dei libri sono file, non fanno parte del database. Copia anche la cartella `data`, vedi [La cartella dei dati](administration/index.md#the-data-folder).

<a id="extensions"></a>

## Estensioni

**Un'estensione installata non compare.**
Chiudi e riapri il libro, oppure ricarica la pagina.

**Un'estensione non si carica e non compare in Admin → Estensioni.**
Ferma Marginalia, elimina il suo JAR dalla cartella `extensions`, poi riavvia. Vedi [Quando un'estensione fallisce](administration/extensions.md#when-an-extension-fails).

**Un'estensione è stata rifiutata o ha smesso di funzionare dopo l'aggiornamento di Marginalia.**
Le estensioni sono compilate per una versione di Marginalia e vengono verificate al caricamento. Apri il rapporto (Admin → Estensioni → *Verifica*) e carica la versione dell'estensione che corrisponde alla tua versione di Marginalia. Vedi [Quando un'estensione fallisce](administration/extensions.md#when-an-extension-fails).

**Dopo aver aggiornato un'estensione, gira ancora la vecchia versione.**
Scarica prima la vecchia versione, poi carica la nuova - oppure riavvia Marginalia.
## FAQ

**La mia scrittura viene inviata da qualche parte?**
Solo ai fornitori di inferenza che configuri: il prompt di ogni parte (e le richieste di conteggio dei token) va all'API di quel
fornitore. Con un modello in esecuzione sul tuo computer, non esce nulla. Marginalia non invia alcuna telemetria.

**Quale modello dovrei usare?**
Qualsiasi modello che scriva bene narrativa e abbia un contesto abbastanza ampio; un contesto di 32 000 token o più è comodo
per i libri lunghi. Prova alcuni modelli con lo stesso libro: cambiare fornitore richiede un solo clic nella scheda *Informazioni*.

**Possono lavorare più persone allo stesso libro?**
No. Ogni libro appartiene a un solo utente. Gli altri utenti possono leggerlo quando viene [pubblicato](books/publishing.md).

**Posso usare Marginalia offline?**
Sì, con un modello in esecuzione in locale (llama.cpp, LM Studio, Ollama...). Marginalia stesso non richiede una connessione internet.

**Come faccio a portare il mio libro fuori da Marginalia?**
[Esporta la storia](books/export.md) come testo, HTML, Word, PDF o EPUB dal menu delle impostazioni (⚙) della scheda *Storia*.
Per conservare tutto, compresi tutti i rami, [esporta un backup del libro](books/backups.md#exporting-and-importing) (JSON).
Puoi anche leggerlo nel [visualizzatore](books/publishing.md).

**Funziona su un telefono?**
Il [visualizzatore](books/publishing.md) è pensato per i telefoni, e Marginalia può essere aggiunto alla schermata Home come un'app
(*Installa app* / *Aggiungi a schermata Home* nel menu del browser). L'editor ha un [layout per telefono](phone.md) quando la finestra del browser
è più stretta di 768 pixel. Non ha una pagina di Amministrazione, un editor di lorebook ridotto e non tutte le estensioni, quindi funziona
al meglio per scrivere e leggere; la configurazione e l'amministrazione sono più comode su uno schermo più grande.

**C'è un tema chiaro o un'altra lingua?**
Sì a entrambe: il pulsante con il sole / la luna accanto al selettore della lingua cambia il tema (scuro per impostazione predefinita), e il selettore della lingua offre il giapponese e le lingue portate dalle estensioni (vedi [Account](account.md#theme)).
