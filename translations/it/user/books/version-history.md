---
label: Cronologia delle versioni
order: 85
---

# Cronologia delle versioni di una parte

Ogni volta che il testo di una parte cambia, Marginalia conserva il testo che aveva. Puoi consultare le versioni precedenti di una parte,
vedere cosa è cambiato tra l'una e l'altra, ripristinarne una, dare a una versione un nome perché non vada mai persa e cercare nel vecchio testo.

Una parte viene modificata dal modello che la scrive (la prima generazione, **Rigenera**, **Swipe**, [**Riscrittura**](story-editor.md#redrafting-a-part)),
da te (**Modifica** e **Salva**), da un'azione di massa (**Cerca e sostituisci**, un'importazione, un comando), da un
[agente](../agents.md), da un'[estensione](../extensions/index.md), dall'[unione](story-editor.md#merging-a-part-into-the-previous-one)
di due parti e dal ripristino di una versione. Ciascuno di questi casi crea una **versione** con l'orario e l'autore della modifica:

| Origine | |
|---|---|
| **Generato**, **Rigenerato**, **Riscritto** | Un modello ha scritto il testo. La versione indica quale modello e protocollo sono stati usati e conserva il ragionamento che ha accompagnato la scrittura. |
| **Autore** | L'hai scritto tu. Una modifica fatta da un comando, da un'azione di massa, da un'importazione o da un agente lo indica nella riga della versione (un agente è segnalato come tale). |
| **Estensione** | Un'estensione ha modificato il testo. |
| **Unito** | Il testo di una parte che è stata unita a questa. |
| **Ripristinato** | Una versione rimessa in cima, una copia della versione che indica. |
| **Versione salvata** | Una versione che hai salvato a mano con un nome. |

La cronologia non è una seconda copia del libro. Le versioni sono memorizzate separatamente dalle parti, quindi una cronologia lunga non
rallenta la storia, e un libro esportato, pubblicato o letto nel visualizzatore contiene solo il testo attuale.

<a id="looking-at-the-history"></a>

## Consultare la cronologia

Scegli **Cronologia** nel menu (☰) della parte. Il numero tra parentesi indica quante versioni ha la parte. (La voce non è disponibile mentre il testo della parte è in modifica, né per una parte non ancora salvata.)

![Il menu di una parte con la voce Cronologia](../../images/history-part-menu.png)

![La cronologia di una parte: le versioni a sinistra, la versione selezionata con le modifiche a destra](../../images/history-dialog.png)

L'elenco a sinistra mostra la versione più recente in alto. Ogni riga riporta l'orario, l'origine, il numero di parole e il
nome della versione, se ne ha uno; la versione più recente è contrassegnata **Attuale**: è il testo che la parte ha ora. Fai clic su una riga
per vedere quella versione a destra. Sopra il testo scegli con cosa confrontarla:

| | |
|---|---|
| **Versione precedente** | Le modifiche rispetto alla versione che la precede, parola per parola. |
| **Testo attuale** | Le modifiche rispetto al testo che la parte ha ora. |
| **Solo testo** | Il testo com'era, senza confronti. |

Una versione scritta da un modello mostra il proprio ragionamento in un blocco che puoi espandere. Una versione **Ripristinato** indica quale versione
ne è la copia.

Su un telefono la finestra occupa tutto lo schermo: le versioni sono schede sopra la versione selezionata, e nulla richiede il mouse.

<a id="putting-a-version-back"></a>

## Ripristinare una versione

Ci sono due modi, e differiscono per ciò che accade alle versioni successive:

| | |
|---|---|
| **Ripristina in cima** | Il testo della versione scelta torna ad essere il testo della parte, come nuova versione **Ripristinato** in cima. Non si perde nulla: le versioni intermedie restano nell'elenco. Usalo per riprendere una formulazione precedente. |
| **Torna a questo punto** | La parte torna alla versione scelta e **tutte le versioni più recenti vengono spostate nel cestino** della cronologia (ti viene chiesta conferma, con il numero indicato). Possono essere ripristinate dal cestino. Usalo per annullare un'intera serie di modifiche. |

Entrambi modificano la parte come farebbe una modifica: i [riassunti](summaries.md) che descrivono il vecchio testo diventano obsoleti, e
Marginalia indica quanti sono, e la parte risulta modificata. La storia mostra subito il testo ripristinato.

<a id="naming-a-version"></a>

## Dare un nome a una versione

Una versione con un nome è una **versione salvata**, e la cronologia non la rimuove mai da sola.

- **Salva la versione** nella finestra salva il testo che la parte ha ora con il nome che gli dai.
- **Etichetta…** assegna un nome a una versione esistente, oppure lo cambia o lo rimuove.

Usalo prima di tentare qualcosa di rischioso: *la bozza che è piaciuta all'editor*, *prima che il duello fosse tagliato*.

<a id="deleting-versions"></a>

## Eliminare versioni

- **Elimina** sposta una versione nel **cestino della cronologia**. Seleziona **Mostra le versioni eliminate** per vedere il cestino (le righe sono in grigio); **Ripristina dal cestino** rimette una versione al suo posto nell'elenco, ed **Elimina definitivamente** ne rimuove una per sempre.
  La versione più recente non può essere eliminata: è il testo della parte.
- **Svuota il cestino** elimina per sempre tutto ciò che si trova nel cestino.
- **Cancella la cronologia** elimina per sempre tutte le versioni della parte tranne quella attuale. La finestra indica quante versioni vanno via e quanto spazio liberano. **Conserva le versioni salvate e la prima versione generata** (selezionata all'inizio) mantiene quelle che hai nominato e la prima cosa che il modello ha scritto per la parte.

![Ciò che Cancella la cronologia sta per eliminare](../../images/history-clear.png)

Il menu delle impostazioni (⚙) della storia ha le stesse azioni per l'intero libro: **Cancella la cronologia del libro** e **Svuota il cestino della cronologia**.

![Il menu delle impostazioni della storia con le azioni per la cronologia del libro](../../images/history-menu.png)

<a id="how-many-versions-are-kept"></a>

## Quante versioni vengono conservate

Una parte conserva **50 versioni**, salvo che un limite non stabilisca diversamente. Quando una nuova versione porterebbe il totale oltre il limite, la cronologia rimuove
per prima la versione più vecchia nel cestino, e poi la versione più vecchia che non si trova nel cestino. Le versioni nel cestino
contano nel limite finché non vengono eliminate definitivamente. Tre versioni non vengono mai rimosse: la più recente, quelle con un nome e
la prima cosa che il modello ha generato per la parte (che inoltre non contano nel limite).

Il limite può essere impostato su quattro livelli, e **vince quello più restrittivo**:

| Livello | Dove |
|---|---|
| Il libro | **Versioni conservate per parte** nella scheda [Informazioni](managing-books.md#the-about-tab) del libro. |
| Le cartelle | **Versioni conservate per parte** nelle [impostazioni della cartella](folders.md#folder-settings); vengono considerate tutte le cartelle sopra il libro. |
| Tu | **Versioni conservate per parte** nella scheda [Impostazioni](../account.md). |
| L'amministratore | Il [permesso](../administration/permissions.md) **Cronologia delle versioni**, un tetto per l'utente. |

Un campo vuoto significa che quel livello non imposta alcun limite. **0** non conserva alcuna cronologia (le versioni esistenti restano finché non le cancelli, ma non ne vengono aggiunte di nuove). Il campo mostra, come suggerimento, il limite che eredita e il livello che lo imposta, per
esempio *Limitato a 20 dalla cartella "Romanzi"*. Un libro non può alzare un limite impostato più in alto.

<a id="searching-the-old-text"></a>

## Cercare nel vecchio testo

Le ricerche principali guardano solo il testo attuale, quindi una vecchia frase che hai modificato non fa comparire una parte. Per trovarla, seleziona **Includi le versioni** (disattivato all'inizio e non memorizzato, così una ricerca non ti sorprende con testo vecchio):

- nella [ricerca avanzata dei libri](managing-books.md#advanced-search): un libro viene trovato quando una versione precedente di una delle sue parti contiene le parole;
- nella ricerca della [vista dei rami](branches-and-story-tree.md#searching-all-branches): una parte che ha le parole solo in una versione precedente viene segnalata, il risultato dice *Trovato nella versione del 12 ott 2026 14:05 (Generato)*, e **Mostra nella cronologia** apre la cronologia della parte su quella versione.

![Una parte trovata tramite una versione precedente](../../images/history-search.png)

Un agente può fare lo stesso con il comando `search-parts` e la sua opzione `includeVersions`; ogni risultato indica da quale versione proviene.
Le versioni nel cestino non vengono cercate, e **Cerca e sostituisci** non modifica mai una versione.

<a id="backups-copies-and-the-trash"></a>

## Backup, copie e cestino

- Un [backup del libro](backups.md#the-history-of-the-parts) può includere la cronologia (**Includi la cronologia delle versioni**,
  deselezionato all'inizio, perché può essere grande). Ripristinare un backup di questo tipo, come libro o come copia, riporta indietro le versioni,
  con i loro nomi e il cestino. Un backup senza di essa assegna a ogni parte una sola prima versione.
- Un [duplicato di un libro](managing-books.md#cloning-a-book) avvia una cronologia propria: una versione per parte, a partire dal testo con cui è stato copiato.
- Eliminare una parte o un libro elimina con sé le loro versioni; si trovano nel [Cestino](../trash.md) finché lo è anche la parte, e vengono eliminate definitivamente insieme ad essa.
- Unire una parte con quella precedente rimuove la cronologia della parte che viene unita; il testo compare nella cronologia
  della parte superstite come versione **Unito**.
<a id="books-from-before-the-history"></a>

## Libri anteriori alla cronologia

Quando Marginalia è stato aggiornato, ogni parte già esistente ha ricevuto le sue prime versioni: una parte scritta da un modello inizia con una versione **Generato** del testo così come il modello l'ha scritto (con il suo ragionamento e il modello usato), e ha una versione **Autore** se da allora l'hai modificata; ogni altra parte inizia con una versione **Autore**. Le modifiche precedenti di quelle parti non sono note e non compaiono nell'elenco.
