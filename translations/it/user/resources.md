---
label: Risorse
order: 960
icon: image
---

# Risorse

La scheda **Risorse** elenca i file che hai caricato su Marginalia: le [immagini delle parti](books/story-editor.md#images) dei tuoi libri e l'audio che estensioni come [Audiobook](extensions/audiobook.md) salvano insieme ad essi. La scheda serve a ritrovare un file, ascoltare l'audio, scaricarlo, sostituirlo con una versione migliore senza toccare i libri che lo usano, ed eliminare ciò che non ti serve più.

![La scheda Risorse](../images/resources-tab.png)

I file sono raggruppati in base a ciò che li usa: ogni riga della tabella è un oggetto (per esempio una parte di un libro, `ChatMessage #123`) con il numero dei suoi file, la loro dimensione totale e la data del più recente. Clicca la freccia nella riga per aprire l'oggetto e vedere i suoi file. La tabella si carica man mano che scorri e che apri un gruppo, quindi resta veloce anche con migliaia di file. Clicca un'intestazione di colonna per ordinare in base a quella colonna: i gruppi e i file al loro interno seguono l'ordinamento. Per impostazione predefinita, i più recenti compaiono per primi.

| Riga / colonna | |
|---|---|
| **Usato da** | L'oggetto a cui sono stati aggiunti i file. *niente* raggruppa i file che non sono usati da nulla. *(eliminato)* dopo il nome indica che l'oggetto è stato eliminato o non esiste più: i file probabilmente non servono più. All'interno di un gruppo: un'anteprima ridotta di un'immagine e il nome del file così come è stato caricato. |
| **Tipo** | Il tipo del file, per esempio `image/png`. |
| **Dimensione** | La dimensione del file; per un gruppo, la somma dei suoi file. |
| **Creato** | Quando il file è stato caricato; per un gruppo, il file più recente. |

Questa è solo una nota per te. Marginalia non la fa rispettare: un'immagine continua a funzionare anche quando la parte a cui era stata aggiunta non esiste più, e la stessa immagine può essere usata da più parti (per esempio, dopo aver [creato un ramo](books/branches-and-story-tree.md) della storia, il ramo ha le stesse immagini della parte da cui è stato creato).

<a id="play"></a>

## Riproduci

Un file di tipo audio (`audio/mpeg`, `audio/wav`...) ha un pulsante **Riproduci** (triangolo) davanti agli altri pulsanti. Riproduce il file nella pagina; il pulsante diventa **Pausa** mentre il file è in riproduzione e torna Riproduci quando lo fermi o quando l'audio termina. Riproduce un solo file alla volta: avviarne un altro interrompe quello in riproduzione.

<a id="download"></a>

## Scarica

Il pulsante di download (freccia verso il basso) nella riga salva il file sul tuo computer.

<a id="replace-the-content"></a>

## Sostituisci il contenuto

Il pulsante di caricamento nella riga apre una finestra di dialogo in cui scegli un nuovo file. Il nuovo contenuto viene salvato come nuovo file e la voce ora punta ad esso, quindi **tutto ciò che usa la voce mostra il nuovo contenuto**; non devi modificare le parti. Usalo per sostituire un'immagine con una versione migliore o per correggere un caricamento sbagliato.

- Un'immagine può essere sostituita solo da un'immagine (PNG, JPEG, GIF o WebP, fino a 10 MB, come per [l'aggiunta di immagini](books/story-editor.md#images)).
- Il nome della voce diventa il nome del nuovo file.
- Il file precedente non viene eliminato dalla cartella dei dati, vedi [sotto](#what-delete-does).

![Sostituzione del contenuto di una risorsa](../images/resources-replace.png)

<a id="deleting"></a>

## Eliminazione

Il pulsante del cestino nella riga elimina un solo file. Per eliminarne più di uno, selezionali nella prima colonna e scegli **Elimina i selezionati** sopra la tabella. Selezionare un gruppo (una riga di un oggetto) elimina tutti i suoi file. La casella nell'intestazione seleziona tutti i gruppi. Marginalia chiede conferma prima di eliminare.

<a id="what-delete-does"></a>

### Cosa fa l'eliminazione

- Il file scompare dalla tabella e dalle parti che lo usano: una parte semplicemente non mostra più l'immagine. Il testo, i riassunti e le esportazioni della parte non cambiano.
- La voce viene solo **contrassegnata come eliminata**. La [pulizia](administration/cleanup.md) nell'amministrazione la rimuove definitivamente dal database.
- **Il file resta nella cartella dei dati** (`data/<your login>/images`), anche dopo la pulizia. Eliminare una risorsa non libera spazio su disco da sola: un amministratore rimuove i file che nessuno usa più nella scheda [File delle risorse](administration/resource-files.md) dell'amministrazione.
- Finché la pulizia non rimuove la voce, puoi ripristinarla dal [Cestino](trash.md): il file è ancora nella cartella dei dati, quindi l'immagine torna a funzionare. Dopo la pulizia, solo un [backup dell'intero database](administration/database-backups.md) fatto prima contiene la voce; il file, invece, resta nella cartella.
