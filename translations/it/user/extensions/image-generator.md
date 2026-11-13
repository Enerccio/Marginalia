---
label: Generatore di immagini
order: 13
---

# Generatore di immagini

Crea un'immagine per una parte della storia. Il modello scrive il prompt dell'immagine a partire dalla parte, tu lo modifichi, un backend di immagini disegna
il quadro, rigeneri finché non ti soddisfa e lo accetti: diventa un'immagine della parte, come quelle che aggiungi a mano con *Immagini* nel menu
della parte (viene mostrata nella storia e nelle esportazioni).

L'estensione non crea immagini da sola: comunica con un server **ComfyUI** oppure con un server che usa l'API di immagini di OpenAI (parlata
anche da stable-diffusion.cpp, LiteLLM e molti servizi ospitati).

<a id="adding-a-backend"></a>

## Aggiungere un backend

*Impostazioni → Impostazioni estensione → Impostazioni Generatore di immagini* elenca i backend. Aggiungine uno (+), compilalo e salva la pagina *Impostazioni*.

| Impostazione | Cosa fa |
|---|---|
| Nome, Tipo | Il nome mostrato nella finestra. **ComfyUI** oppure **OpenAI images API**. |
| Indirizzo | L'indirizzo base del server, per esempio `http://localhost:8188` per ComfyUI. |
| Chiave API | Inviata come token bearer. Viene memorizzata cifrata e non viene mostrata di nuovo; digita una nuova chiave per sostituirla, oppure **Rimuovi la chiave**. |
| Modello | Solo per OpenAI images API: il `model` della richiesta. |
| Workflow | Solo per ComfyUI: un workflow salvato in ComfyUI con **Save (API Format)**. Scrivi `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` e `{{height}}` dove devono andare. |
| Il server è su questa macchina o sulla mia rete | Consente gli indirizzi della rete locale. Lascialo disattivato per un server nel cloud: in quel caso non può essere fatto chiamare la tua macchina. |
| Larghezza, Altezza, Prompt negativo | Ciò con cui parte la finestra (puoi cambiarli lì per una singola immagine). |
| Timeout | Quanto attendere per una singola immagine. |
| Rilascia dopo inattività | Vedi [Una GPU](#one-gpu-for-the-model-and-the-images). |

![Un backend nelle impostazioni](../../images/plugin-imagegenerator-settings.png)

**Testa connessione** verifica l'indirizzo e la chiave.

Il pannello contiene anche lo **Stile** anteposto a ogni prompt (l'aspetto del libro, per esempio *acquerello, luce morbida*) e le
**Istruzioni per chi scrive il prompt**, il testo che il modello riceve per scrivere il prompt a partire da una parte. Se è vuoto o non
modificato, si usa quello predefinito.

<a id="making-an-image"></a>

## Creare un'immagine

![Genera immagine nel menu di una parte](../../images/plugin-imagegenerator-menu.png)

1. Scegli **Genera immagine** nel menu di una parte. Il modello della finestra (quello del libro per impostazione predefinita; l'ultima scelta viene ricordata)
   inizia a scrivere il prompt della parte; puoi fermarlo con **Interrompi**, modificare il prompt, oppure premere **Scrivi il prompt con il
   modello** per riscriverlo. Prima di riscriverlo puoi compilare:
   - **Invia voci del lorebook** - le voci del lorebook del libro (e dei suoi sotto-lorebook) vengono inviate al modello come in una generazione, così
     sa come appaiono personaggi e luoghi. Una voce viene inviata quando i suoi filtri compaiono nella parte: nel suo testo, nelle istruzioni, nell'ambientazione
     della scena, nel punto di vista e nei personaggi presenti, oppure nei tuoi *Dettagli*. Vengono inviate anche le voci sempre attive. Ne entrano solo quante
     ne consente il contesto del modello (un avviso indica quante ne sono state escluse). La scelta viene ricordata.
   - **Dettagli** - ciò che l'immagine deve mostrare o come deve apparire (*di notte, primo piano sul suo viso, il vestito rosso*); chi scrive il prompt
     ne tiene conto. Vale solo per questa finestra.
2. Scegli il backend e premi **Genera**. In *Dimensione e prompt negativo* puoi cambiare, per questa finestra, la dimensione e il prompt negativo.
3. Compare l'immagine. **Rigenera** ne crea un'altra con un nuovo seed, **Scarta** la elimina, **Accetta** la aggiunge alla parte e chiude la
   finestra. **Interrompi** ferma un'immagine in lavorazione (a ComfyUI viene chiesto di interrompere il job).

![La finestra con il prompt scritto dal modello](../../images/plugin-imagegenerator-prompt.png)

![L'immagine, con Accetta, Scarta e Rigenera](../../images/plugin-imagegenerator.png)

La parte aperta mostra subito la nuova immagine sotto il suo testo.

<a id="one-gpu-for-the-model-and-the-images"></a>

## Una GPU per il modello e per le immagini

Con una GPU piccola, il modello linguistico e il modello di immagini non possono essere caricati insieme. L'estensione non gestisce la memoria;
ogni backend può avere due elenchi di richieste che tu configuri:

- **Prima della prima immagine** - eseguito una sola volta, quando la finestra inizia a usare il backend. Per esempio una richiesta al tuo proxy che
  scarica il modello linguistico e carica il modello di immagini.
- **Dopo l'ultima immagine** - eseguito quando accetti, scarti o chiudi la finestra, oppure quando sono trascorsi i secondi di **Rilascia dopo inattività**
  dall'ultima immagine (così una finestra dimenticata non tiene occupata la GPU; 0 lo disattiva). Per ComfyUI può essere `POST <indirizzo>/free` con il
  corpo `{"unload_models": true, "free_memory": true}`; per un proxy, una richiesta che ricarica il modello linguistico.

Con `llama-server` in modalità router dietro LiteLLM, la richiesta *prima* può scaricare il modello linguistico tramite il proxy: vedi
[Liberare la GPU per un altro modello](../prompt-cache.md#freeing-the-gpu-for-another-model).

Entrambi i momenti accettano un **elenco di richieste**, eseguite una dopo l'altra (**Aggiungi una richiesta** ne aggiunge una, il cestino ne rimuove una). Uno scambio spesso
richiede più richieste: scaricare il modello linguistico, poi avviare il server di immagini e, dopo le immagini, fermarlo. Ogni richiesta ha un indirizzo, un metodo
(`POST` per impostazione predefinita), intestazioni (`Nome: valore`, una per riga), un corpo, un **timeout** (vuoto: 60 secondi, al massimo 600; assegnane di più a un server che
carica un modello grande) e **Attendi dopo** (secondi di pausa dopo la risposta, per un server che è ancora occupato dopo aver risposto di sì). Le
intestazioni sono memorizzate cifrate, come la chiave API (una richiesta spesso contiene `Authorization: Bearer ...`). Il backend resta riservato mentre
rigeneri, quindi solo la prima immagine paga il costo dello scambio. Se un proxy davanti ai modelli li scambia in base al nome del modello, lascia entrambi vuoti.

Quando una richiesta dell'elenco *prima* fallisce, le altre non vengono eseguite e l'immagine si ferma (il messaggio indica quale richiesta è fallita: *Richiesta 2
di 3: ...*), così che nulla venga caricato su una GPU ancora piena. L'elenco *dopo* viene eseguito **completamente** qualunque cosa accada a una delle sue richieste
(ogni server va fermato), e il primo errore viene mostrato come avviso. Un backend salvato con l'unica richiesta di una versione precedente la mostra come prima richiesta dell'elenco.

**Più persone su una GPU.** Tutti quelli il cui backend ha lo stesso indirizzo di server (`http://host:porta`) passano da un unico cancello: la richiesta *prima* viene
eseguita per la prima finestra che si avvia e la richiesta *dopo* quando l'ultima è terminata, così che chiudere una finestra non scarichi il modello sotto l'immagine di un'altra,
e le immagini vengono create una alla volta (una seconda finestra attende il suo turno).
Le richieste sono quelle dell'utente che arriva per primo e per ultimo.

Un backend può anche indicare la GPU su cui gira (**GPU (nome)**). I backend che indicano la stessa GPU si attendono a vicenda anche se i loro indirizzi sono diversi, e questo vale anche per i backend dell'[Audiolibro](audiobook.md) (un server vocale e un server di immagini su una stessa scheda): un utente il cui backend richiede un altro modello attende che gli altri abbiano finito, e poi esegue le proprie richieste, invece di unirsi a un caricamento che non è il suo. Se lo lasci vuoto, solo le richieste verso lo stesso indirizzo si attendono a vicenda.

<a id="what-is-not-done"></a>

## Cosa non è previsto

- I backend sono per utente, non per libro.
