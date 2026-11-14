---
label: Side Query
order: 1
---

# Side Query

Una chat con il modello accanto alla tua storia: chiedi della trama, verifica la coerenza, fai brainstorming su nomi o sul prossimo capitolo, senza aggiungere nulla al libro.

!!!
Side Query funziona nel [layout per telefono](../phone.md#extensions): la sua scheda si trova nel cassetto della struttura della storia.
!!!

![Side Query accanto alla storia](../../images/plugin-sidequery.png)

<a id="the-side-query-tab"></a>

## La scheda Side Query

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Side Query** accanto a *Struttura*. Trascina il divisore per darle più spazio.

La scheda contiene le conversazioni, ciascuna nella propria linguetta: **+** apre una nuova conversazione (*Tab 2*, *Tab 3*...), la matita rinomina la linguetta, **×** la chiude (l'ultima linguetta non può essere chiusa). Con *Enable AI Tab Naming* attivo nel profilo, il modello assegna un nome alla linguetta dopo la prima risposta, a meno che tu non l'abbia già rinominata tu stesso. Le conversazioni appartengono al libro e vengono conservate finché non chiudi la loro linguetta.

<a id="asking"></a>

## Fare domande

Scrivi una domanda nella casella in basso e fai clic su **SEND**. La risposta compare man mano che viene scritta; **STOP** (al posto di *SEND*) la interrompe.

| Pulsante | |
|---|---|
| **SEND** | Invia la domanda. |
| **UNDO** | Rimuove l'ultimo messaggio. Se era una tua domanda, torna nella casella per essere modificata. |
| **REGENERATE** | Rimuove l'ultima risposta e chiede di nuovo. |

Ogni messaggio ha pulsanti per **spostarlo** in alto o in basso, **modificarlo**, **copiarlo** negli appunti e **escluderlo** dal contesto (oppure includerlo di nuovo). I messaggi esclusi restano nella conversazione ma non vengono inviati al modello: utile per eliminare una risposta sbagliata senza cancellarla.

<a id="what-the-model-sees"></a>

## Che cosa vede il modello

Le opzioni sopra la conversazione scelgono che cosa viene inviato insieme alle tue domande:

| Opzione | |
|---|---|
| **Lorebook** | La lore del libro (vedi sotto). |
| **Chat Logs** *from* … *to* | Le parti della storia del ramo attivo in quell'intervallo, numerate da 1 come nella struttura: `1` fino a `6` corrispondono alle parti `#1` fino a `#6`. |
| **Plug button** | Apre un elenco dei dati offerti dalle altre estensioni, con una casella di spunta per ciascuno (vedi sotto). |
| **Tokens** | La dimensione approssimativa della richiesta. |

!!!
*Lorebook* raccoglie la lore come farebbe una generazione: il lorebook del libro e i suoi sotto-lorebook (i lorebook disabilitati vengono saltati), le voci abilitate i cui tag corrispondono ai tag del libro, con le [macro](../templates/macros.md) elaborate. I filtri per parola chiave non vengono applicati: non esiste un prompt della storia con cui confrontarli, quindi ogni voce applicabile viene inviata. Le variabili impostate dalle voci non vengono memorizzate.
!!!

<a id="data-of-other-extensions"></a>

### Dati delle altre estensioni

Il pulsante plug apre un popover con le **fonti aggiuntive** offerte dalle altre estensioni, per esempio [Arc Outline](arc-outline.md) offre la tua struttura. Ogni fonte ha una casella di spunta; l'elenco ha fino a due parti:

- **About the book**: una fonte selezionata viene aggiunta alla richiesta dopo il lorebook, sotto il suo nome.
- **About story parts**: una fonte selezionata viene richiesta per ogni parte della storia nell'intervallo *Chat Logs*, e ciò che restituisce viene aggiunto dopo il testo di quella parte. Ha effetto solo quando *Chat Logs* è attivo.

La scelta viene mantenuta con la scheda. Se un'estensione non è installata (o è arrestata), le sue fonti non compaiono nell'elenco e non vengono inviate, e ricompaiono quando l'estensione torna disponibile. Se nessuna estensione ne offre, il popover lo segnala.

La richiesta viene costruita così:

1. un messaggio di sistema: il *Initial System Query* del profilo, le voci del lorebook (*Lorebook Context*), le fonti selezionate sul libro e sulle parti della storia (*Chat History Context*, etichettate con i loro numeri della struttura *Message #1*, *#2*..., ciascuna seguita dalle fonti selezionate sulle parti della storia);
2. i messaggi della conversazione inclusi, come chat. Le *Instructions Before User Input* del profilo vengono messe all'inizio della tua ultima domanda, prima del suo testo.

Il modello e il protocollo provengono dal profilo (vedi sotto), per impostazione predefinita quelli del libro. Un modello economico e veloce di solito basta per le domande sulla storia.

<a id="saved-queries"></a>

## Domande salvate

Le domande che fai spesso possono essere salvate:

- **Save prompt** (icona del disco) salva il testo nella casella con il nome selezionato nell'elenco, oppure chiede un nome;
- **Save prompt as...** lo salva con un nuovo nome;
- scegli un nome nell'elenco per inserire il testo salvato nella casella;
- il pulsante del cestino elimina la domanda salvata selezionata.

Le domande salvate appartengono al tuo account e sono disponibili in ogni libro.

Alcune utili:

- *Elenca ogni personaggio con nome comparso finora, con una riga su ciascuno.*
- *Quali fili della trama sono ancora aperti?*
- *Suggerisci cinque nomi per una città portuale in questo mondo.*

<a id="profiles"></a>

## Profili

Le impostazioni di Side Query si trovano in **Impostazioni → Extension Settings → SideQuery Settings**, organizzate in **profili** come quelli del [Reviewer](reviewer.md#profiles). Il profilo selezionato è quello usato:

![Le impostazioni di SideQuery](../../images/plugin-sidequery-settings.png)

| Campo | |
|---|---|
| **Profile**, **New**, **Rename**, **Delete** | Scegli e gestisci i profili. *_Default* non può essere rinominato o eliminato. |
| **SideQuery Model**, **SideQuery Protocol** | Il [fornitore di inferenza](../inference-providers.md) e il [protocollo](../protocols.md) da usare. Vuoto: quelli del libro. |
| **Initial System Query** | L'inizio del messaggio di sistema, per esempio il ruolo dell'assistente. |
| **Instructions Before User Input** | Testo inserito subito prima della tua ultima domanda: regole per le risposte, per esempio *Rispondi brevemente. Cita la storia quando puoi.* |
| **Enable AI Tab Naming** | Dopo la prima risposta in una linguetta, al modello viene chiesto un nome breve della conversazione (una richiesta in più). Le linguette che hai rinominato mantengono il loro nome. |

Fai clic su **Salva** in cima alla pagina *Impostazioni* per conservare le modifiche.

<a id="storage"></a>

## Archiviazione

Le conversazioni sono memorizzate insieme al libro, quindi fanno parte dei [backup del libro](../books/backups.md) e delle copie. Profili e domande salvate sono memorizzati nelle impostazioni del tuo utente.
