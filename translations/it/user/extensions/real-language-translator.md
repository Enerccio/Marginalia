---
label: Traduttore di lingue reali
order: 3
---

# Traduttore di lingue reali

Permette ai personaggi della tua storia di parlare una **lingua reale**: una storia ambientata in Giappone può essere scritta in inglese da te e dal modello, e i dialoghi diretti dei personaggi diventano giapponese autentico (kana e kanji, oppure romaji), con l'inglese conservato dietro di esso. È il [Traduttore di conlang](conlang-translator.md) per una lingua che esiste: la stessa idea e le stesse schermate, senza un lessico.

!!!
Il testo della storia contiene la **traduzione**. Tutto ciò che legge la storia per un modello (la generazione successiva, i riassunti, la scansione del lorebook, le annotazioni, le altre estensioni) legge l'**inglese** da cui il parlato è stato tradotto, quindi scrivi e guida la storia in inglese. Un clic su un parlato tradotto mostra l'inglese.
!!!

!!!warning Lavori in corso
La traduzione di parti, intervalli e frasi, le impostazioni, la scheda del libro, il visualizzatore, l'esportazione e i [comandi](#commands) sono disponibili. Un secondo passaggio che rifinisce la traduzione, più lingue reali in un solo libro e una lingua per personaggio non lo sono, vedi [Limiti](#limits).
!!!

<a id="the-idea"></a>

## L'idea

1. Una **lingua** è un breve insieme di regole per il traduttore: lo **script** (kana e kanji, solo kana, romaji), se i **suffissi onorifici** dei nomi sono scritti, come viene scelto il **livello di cortesia** in base al rapporto tra chi parla e chi ascolta, come vengono scritti i **nomi**, e qualsiasi altra cosa tu voglia far rispettare. Il modello conosce la lingua in sé, quindi non c'è alcun lessico. Le lingue sono conservate una volta sola nelle tue impostazioni, e qualsiasi numero di libri le usa.
2. Un **libro** (o una cartella di libri) sceglie la sua lingua. Una parte viene tradotta **a mano** dal suo menu, **da sola** quando è definitiva, oppure **un intervallo alla volta**.
3. Una richiesta al modello per parte classifica ogni dialogo diretto (è detto nella lingua, o in inglese, da questo personaggio, compreso dall'ascoltatore?) e traduce il dialogo che lo è, con la voce e il livello di cortesia che il personaggio e il rapporto richiedono.
4. L'inglese viene conservato (nei dati del libro, come *sorgente* del dialogo) e letto da ogni modello. La traduzione è l'originale: ciò che modifichi a mano resta, e una parte che modifichi non viene mai sovrascritta alle tue spalle.

L'estensione controlla gratuitamente ciò che può: il parlato citato deve essere nella parte, la lingua deve essere quella del libro, e la traduzione deve essere nello **script** richiesto (una traduzione in romaji con kanji al suo interno, oppure una solo kana con kanji, viene mantenuta e *segnalata*). Non può verificare il giapponese stesso: leggi qualche parte prima di un'esecuzione lunga.

<a id="languages"></a>

## Lingue

Una lingua si modifica in **Impostazioni → Impostazioni dell'estensione → Traduttore di lingue reali**.

| Pulsante | |
|---|---|
| **Add** | Una nuova lingua. Parte da uno dei **preset** (giapponese in kana e kanji, solo kana, romaji, oppure un'altra lingua) che compila i campi; modificali come preferisci. |
| **Modifica**, **Elimina**, **Sposta su**, **Sposta giù** | La lingua selezionata. L'eliminazione chiede conferma. |
| **Duplica** | Una copia indipendente con un nuovo nome. Un libro che sceglie una lingua la condivide con ogni altro libro che la sceglie; una copia serve a conservare una lingua per un solo libro. |

L'editor ha questi campi:

![Le lingue nelle impostazioni](../../images/plugin-reallang-settings.png)

| Campo | |
|---|---|
| **Nome** | Come la chiami ("Giapponese, romaji per il romanzo"). |
| **La lingua come la nomina il modello** | La parola con cui il modello risponde per un parlato nella lingua ("Japanese"); vuoto: il nome. |
| **Script** | *Kana e kanji*, *Solo kana*, *Romaji (lettere latine)*, oppure *Altro script (non verificato)*. |
| **Scrivi i suffissi onorifici dei nomi** | san, sama, kun... |
| **Scrivi anche come si legge (romaji)** | Il modello scrive anche la lettura della traduzione; il popover la mostra sopra l'inglese. |
| **Cortesia e livello di linguaggio**, **Nomi di persone e luoghi**, **Altre regole** | Brevi testi per il traduttore. I preset giapponesi ne propongono uno per i primi due (forma piana tra pari intimi, forma cortese con sconosciuti, keigo verso i superiori; un nome straniero in katakana). |

![L'editor di una lingua](../../images/plugin-reallang-editor.png)

Sotto l'elenco ci sono le scelte che i libri e le cartelle **ereditano** da te: la lingua, se una parte viene tradotta da sola, e il modello e il protocollo della traduzione. **Salva** nella schermata Impostazioni per conservare le modifiche.

<a id="choosing-the-language-of-a-book"></a>

## Scegliere la lingua di un libro

La scheda **Prompt** della finestra del libro, sotto *Impostazioni dell'estensione*, contiene i campi dell'estensione; la finestra della cartella ha gli stessi. Vuoto significa *eredita*: prima la cartella superiore, poi le tue impostazioni.

| Campo | |
|---|---|
| **Lingua del parlato** | La lingua in cui viene tradotto il libro. Vuoto: il libro non viene tradotto. |
| **Traduci una nuova parte da sola** | Una parte viene tradotta quando è definitiva: dopo una generazione, dopo l'annotazione automatica e la riscrittura se il libro le ha, come passo di [ciò che segue la parte](../books/story-editor.md#what-follows-a-generated-part) (il pannello delle esecuzioni lo mostra, e il suo **Interrompi** lo ferma). |
| **Modello della traduzione**, **Protocollo della traduzione** | Vuoto: il modello e il protocollo del libro, altrimenti i tuoi predefiniti. |
| **I lettori del libro pubblicato vedono** | Ciò che mostra il [visualizzatore](#what-the-reader-sees). |

![I campi di un libro](../../images/plugin-reallang-book.png)

<a id="the-real-language-tab-of-the-book"></a>

## La scheda Lingua reale del libro

La finestra della storia ha una scheda **Lingua reale** (nello stesso pannello dei capitoli e del lorebook) per la lingua di *questo* libro, con **Aggiorna** per rileggerla.

![La scheda Lingua reale della storia, con i parlanti e la loro comprensione](../../images/plugin-reallang-tab.png)

**Parlanti.** Chi parla la lingua **in questo libro** (un libro con [più di una lingua](#more-than-one-language-in-a-book) ha una scheda *Parlanti* per ciascuna). Per impostazione predefinita tutti parlano la lingua tranne i personaggi elencati (oppure parlano solo quelli elencati). Il testo sovrascrive questo ("lei disse in inglese", "la guardia non capiva una parola"). Per ogni personaggio annota il **genere** e la **voce**: il livello di cortesia ne segue l'andamento. Scrivi qui l'età, il grado e come il personaggio parla con gli altri ("un giovane ufficiale, cortese con i superiori, brusco con gli amici"): è questo che permette al traduttore di scegliere tra forma piana e keigo. I parlanti appartengono al libro.

Ogni personaggio può avere anche un livello di **comprensione**: *non capisce nulla*, *conosce qualche parola*, *segue la maggior parte*, oppure *segue tutto*. Lascialo vuoto (o *segue tutto*) e ai modelli non viene detto nulla. Usalo per le persone che sentono la lingua senza parlarla: *Tomáš: non capisce nulla (giapponese)*, *Altri abitanti del villaggio: segue la maggior parte* (il nome è solo testo, quindi funziona anche un gruppo). I livelli sono
**statici per l'intero libro** e vengono forniti ai modelli come informazione; ciò che un personaggio ne fa è lasciato al modello, e puoi sempre specificare di più nelle istruzioni ("Tomáš non capisce ciò che dicono"). Raggiungono i modelli in due modi: il **modello della storia** riceve un breve messaggio con i livelli (inserito nel prompt come l'Nota dell'autore; la casella sopra l'elenco lo disattiva, e il numero indica da quanti messaggi dalla fine viene inserito), e il modello che **revisiona una parte** (*Annota con l'IA*) li riceve come sezione dei fatti, così può segnalare un personaggio che reagisce a parole che non riesce a seguire. Se preferisci collocare tu il testo, disattiva l'inserimento e usa la [macro](../templates/macros.md#commands) {%{`{{command::reallang-understanding}}`}%} in un template; accetta un elenco facoltativo di personaggi da mantenere.

**Lingue.** Le altre lingue del libro, oltre a quella indicata dal campo **Lingua del parlato**: vedi [sotto](#more-than-one-language-in-a-book).

**Traduzione rapida.** Scrivi un testo in inglese, facoltativamente chi lo dice e a chi, e premi **Traduci**: il modello lo traduce con le regole della lingua (con più lingue, scegline una). Nulla viene scritto nella storia; copi tu la traduzione.

**Glossario.** Come vengono scritti nella lingua i nomi e le parole speciali della storia (una [sezione più sotto](#the-glossary-of-names-and-special-words)); una scheda *Glossario* per ogni lingua del libro.
<a id="the-glossary-of-names-and-special-words"></a>

## Il glossario dei nomi e delle parole speciali

Senza un glossario il modello sceglie di nuovo, in ogni parte, come scrivere un nome: `Saellen` diventa `サエレン` in una parte e resta in caratteri latini in quella successiva, `Tomáš` diventa `トマーシュ` o `トマシュ`. Il glossario fissa la scelta una volta sola. Una voce ha

- la **parola** come la scrive la storia (`Saellen`, `witch`), la chiave della voce; le maiuscole non contano;
- **come si scrive** nella lingua (`サエレン`, `魔女`), oppure *lasciala com'è* per un titolo, un luogo o un termine inventato che non viene tradotto;
- **come si legge** (facoltativo, per una lingua con una lettura: `魔女` si legge `ウィッチ`), e una **nota** per il traduttore (l'appellativo che spetta al personaggio, l'ordine del cognome);
- se **si applica**.

**Tre livelli, uniti.** Il glossario di una lingua vive su tre livelli: la **lingua** (nel suo editor, in *Settings → Extension Settings*: i nomi e i termini di ogni libro in questa lingua), le **cartelle** in cui si trova il libro (una serie definisce i nomi che i suoi libri hanno in comune; le cartelle possono essere annidate) e il **libro** (il suo, quello usato più spesso). Il modello riceve l'*unione*; per la stessa parola vince il **livello più vicino**, così un libro può scrivere un nome in modo diverso dalla sua cartella. La scheda *Glossary* della scheda *Real language* mostra tutti i livelli, con il livello da cui proviene ogni voce, e modifica una voce dove si trova. Una parola di un livello superiore che questo libro non deve usare viene **disattivata per questo libro** (una voce del libro che è disattivata; riattivala nello stesso modo, oppure eliminala per riavere la parola del livello superiore). Una voce del libro può essere **spostata in alto** in una cartella o nella lingua quando risulta comune. Il glossario di una cartella e di un libro viene conservato **per ogni lingua** del libro, perché un nome si scrive in modo diverso in ogni lingua.

**Che cosa fa la traduzione con il glossario.** Il glossario fa parte della richiesta, quindi il modello scrive le parole come sono elencate. Un glossario fino a 100 voci va intero in ogni richiesta (un server che mette in cache il prompt lo legge una volta sola); uno più grande va con le voci che la parte contiene. Dopo la risposta il plugin lo **controlla**: un discorso il cui inglese contiene una parola del glossario, ma la cui traduzione non la scrive come dice il glossario, viene mantenuto e **segnalato** (decide l'autore). Un nome per il quale il modello **ha dovuto scegliere** una forma scritta, che il glossario non elenca, viene aggiunto al glossario del libro come **proposta** (la colonna *Stato* dice *Proposed*): da quel momento il modello lo usa, e *Accept* indica che l'hai esaminato, oppure *Modifica* lo cambia. Una parola che un qualsiasi livello ha già (anche se disattivata) non viene mai più proposta.

![Il glossario del libro: le voci dei tre livelli, un nome proposto dal modello attende di essere accettato](../../images/plugin-reallang-glossary.png)

![Una voce del glossario](../../images/plugin-reallang-glossary-entry.png)

![Il glossario in fondo all'editor di una lingua](../../images/plugin-reallang-editor-glossary.png)

I comandi per questo sono nella tabella qui sotto. Una parte che un libro ha tradotto prima che esistesse il glossario mantiene la sua traduzione; traducila di nuovo per usare il glossario.

<a id="extracting-the-words-by-model"></a>

## Estrarre le parole con il modello

Scrivere il glossario a mano è lento per un libro lungo. La scheda **Word extraction** (accanto alle schede del glossario della scheda *Real language*) fa leggere al modello un **intervallo di parti** ed elencare i nomi e le parole speciali che trova, con la forma che ciascuno ha nella lingua. Tutto ciò che trova e che il glossario non ha ancora viene aggiunto al glossario del libro come **proposta**: il modello lo usa dalla richiesta successiva, e tu lo accetti, lo modifichi o lo elimini nella scheda *Glossary*. Nella storia non cambia nulla.

![La scheda Word extraction dopo un'esecuzione: i termini trovati dal modello sono nel glossario come proposte](../../images/plugin-reallang-extraction.png)

- **Lingua.** Se il libro ha più di una lingua, la lingua a cui serve il glossario.
- **Prompt.** Istruzioni per il modello: che cosa cercare e come si scrive ogni tipo di parola nella lingua. Scegline uno dalla libreria, oppure *None: only my specification*. Il plugin include un prompt per il giapponese (luoghi e organizzazioni reali nella loro forma giapponese abituale, nomi stranieri inventati in katakana, nomi giapponesi in kanji con la lettura, termini coniati del mondo come kanji con una lettura in katakana, *gikun*); è contrassegnato come *built-in* ed è sempre disponibile.
- **Specification.** Testo libero per *questo* libro, aggiunto dopo il prompt: che cosa tralasciare (i personaggi minori), come scrivere un tipo di parola in questo caso, una grafia che vuoi. Senza un prompt è tutto ciò che il modello riceve.
- **From part / To part**, poi **Extract the words**. Le parti vengono lette una dopo l'altra (una parte richiede minuti di tempo di un modello lento), la scheda mostra *Part 3 of 12*, **Interrompi** si ferma dopo la parte che si sta leggendo. Una parte che fallisce viene conteggiata e l'esecuzione prosegue; un libro senza lingua, senza modello, o senza né prompt né specification interrompe l'esecuzione.

Le parole che il glossario ha già (a qualsiasi livello, anche disattivate) vengono comunicate al modello e non vengono proposte di nuovo, quindi una seconda esecuzione sullo stesso intervallo aggiunge solo ciò che è nuovo. La *categoria* in cui il modello ha inserito un termine ("Fictional coined term / gikun") viene scritta nella nota della voce. Non descrivi l'output: il plugin richiede un formato fisso, qualunque cosa dica il prompt.

**La libreria dei prompt.** *Settings → Extension Settings → Real Language Translator* contiene l'elenco **Extraction prompts**: i tuoi prompt con un nome e le istruzioni, da aggiungere, modificare, eliminare e riordinare. **Copy a built-in prompt** mette nel tuo elenco una copia del prompt integrato, da modificare. I prompt appartengono a te, non a un libro; un libro ricorda quale ha scelto e la sua specification.

![La libreria dei prompt di estrazione nelle impostazioni](../../images/plugin-reallang-prompts.png)

<a id="more-than-one-language-in-a-book"></a>

## Più lingue in un libro

Una storia con parlanti giapponesi e coreani ha bisogno di entrambe. Un libro indica **prima una lingua** (il campo qui sopra, che può essere dato anche da una cartella o dalle tue impostazioni) e la scheda **Languages** aggiunge **altre lingue**
dalla tua libreria, una alla volta, e le toglie di nuovo.

- **Ogni lingua ha i propri parlanti**, in una scheda tutta sua. La prima lingua è parlata da tutti tranne i personaggi che elenchi, come prima; **una lingua che aggiungi parte con "solo i personaggi elencati la parlano" e con nessuno elencato**, quindi non viene richiesta per nulla finché non elenchi chi la parla. Elenca un personaggio come eccezione della prima lingua quando un'altra lingua è la sua, altrimenti per il suo discorso viene richiesta anche la prima lingua (un discorso che due lingue rivendicano resta con quella che viene richiesta per prima). Un personaggio può parlare più di una lingua (elencalo in entrambe; il testo della parte dice al modello in quale lingua è un discorso).
- **Una richiesta per ogni lingua**: le regole e le note di una lingua, e solo i discorsi dei suoi parlanti. Una parte con i discorsi di due lingue richiede due richieste, una dopo l'altra; la seconda legge la parte con l'inglese dei discorsi della prima e lascia tradotti quei discorsi. Una parte il cui punto di vista e i cui personaggi presenti sono tutti fuori dai parlanti di una lingua non viene richiesta per quella lingua (una parte che non nomina alcun personaggio viene richiesta per ogni lingua che ha dei parlanti).
- **Un passaggio conosce la propria lingua.** **Translate dialogs (real language)** di nuovo per una lingua lascia in pace i discorsi delle altre, e **Put the English back** può riguardare una lingua o tutte.
- **I menu chiedono quale lingua** quando il libro ne ha più di una: il menu di una parte (una delle lingue o *All languages*) e il clic destro su una frase selezionata (una lingua).
  **Translate parts** ha un campo *Lingua* (tutte le lingue per impostazione predefinita); *the parts that are not translated* significa "non tradotte in quella lingua".
- **L'esecuzione automatica** traduce una nuova parte in ogni lingua i cui parlanti possono esservi presenti.
<a id="translating"></a>

## Traduzione

- **A mano:** il menu di una parte ha il sottomenu **Traduci** (anche il clic destro sul testo ne ha uno), e al suo interno **Traduci dialoghi (lingua reale)** (queste voci e **Traduci parti (lingua reale)** compaiono solo in un libro che ha una lingua di questo plugin). Una parte già tradotta viene tradotta di nuovo **dal suo originale in inglese**. Un libro con più di una lingua chiede prima [quale](#more-than-one-language-in-a-book).
- **Da sola:** con **Traduci una nuova parte da sola**, una parte generata viene tradotta non appena è definitiva. Una *rigenerazione* di una parte le dà un nuovo inglese e rimuove la vecchia traduzione.
- **Dopo una traduzione.** La traduzione di una parte (a mano, di una selezione o da sola) non blocca la pagina: compare nel [pannello delle esecuzioni](../books/story-editor.md#the-panel-of-the-runs) in basso a sinistra, con la fine del **ragionamento** del modello o della sua **risposta** man mano che arrivano, e un pulsante **Interrompi**. Nel frattempo la parte mostra *Translating the speech of the part...*, e nient'altro può tradurla. **Interrompi** annulla subito la richiesta e non ne avvia altre; con più lingue, le lingue scritte prima restano tradotte. La traduzione di un [intervallo](#translating) ha una finestra propria, che resta aperta accanto alla storia.
- **Battute che il modello ha saltato:** a volte una traduzione lascia una battuta in inglese. **Traduci → Traduci dialoghi mancanti (lingua reale)** nel menu di una parte traduce solo le battute che sono ancora in inglese e lascia com'erano quelle già tradotte; con del testo selezionato, il clic destro **Traduci → Traduci dialoghi mancanti nella selezione (lingua reale)** lo fa per le battute toccate dalla selezione. Al modello viene inviata la parte così com'è, con dentro le battute tradotte, e gli si chiedono solo le battute ancora in inglese, quindi l'esecuzione costa meno di una traduzione dell'intera parte. Viene scritto solo il parlato, quindi non è un modo per tradurre una frase che il modello non considera parlato: per quello usa *Translate selected text*.
- **Una frase che hai scritto:** selezionala nel testo di una parte, fai clic destro e scegli **Traduci → Traduci il testo selezionato (lingua reale)**. Il modello traduce la frase con la storia intorno; l'inglese resta come fonte. Una selezione che contiene una battuta già tradotta viene rifiutata.
- **Un intervallo:** **Traduci parti (lingua reale)** nel menu degli strumenti della storia apre una finestra che resta aperta accanto alla storia: *Translate the parts that are not translated*, *Translate the translated parts again* oppure *Put the English back*, da una parte a un'altra, con un **Interrompi**.

![La finestra che traduce un intervallo di parti](../../images/plugin-reallang-range.png)

Una traduzione viene salvata come qualsiasi modifica del testo: la cronologia della parte ottiene una versione, la parte **non viene segnata come modificata**, e l'inglese viene conservato. **Una battuta che hai modificato a mano resta com'è** quando la parte viene tradotta di nuovo o riportata all'inglese, perché contiene le tue parole.

<a id="what-the-reader-sees"></a>

## Cosa vede il lettore

Una battuta tradotta ha una **linea tratteggiata**; un clic su di essa mostra l'inglese in un popover: la lettura (quando la lingua la richiede), chi parla e se l'ascoltatore non capisce, e una nota rossa quando hai modificato la traduzione dopo che era stata fatta.

**Modifica la fonte** (un pulsante nel popover, solo nelle tue viste) apre una finestra con la traduzione così come la storia la conserva e l'inglese che leggono i modelli. Usala quando un madrelingua ha corretto a mano il giapponese e l'inglese ora dice altro, o quando il modello ha sbagliato l'inglese. Salvando, la battuta riceve il suo nuovo inglese, la traduzione com'è ora diventa quella a cui appartiene l'inglese (la nota rossa scompare) e i modelli leggono al suo posto il nuovo inglese. Il testo della parte non viene modificato. `reallang-edit-source` fa lo stesso dalla riga di comando.

**Cerca e sostituisci nelle fonti** (il pulsante con le frecce in alto nella scheda *Real language*) apre la finestra di ricerca e sostituzione dell'applicazione sulle fonti inglesi delle battute tradotte del ramo attivo (testo semplice o espressione regolare). Cambiano solo le fonti; le parole della traduzione stessa vengono sostituite con la ricerca e sostituzione del libro.

![La finestra che modifica la fonte di una battuta](../../images/plugin-reallang-source.png)

![Ricerca e sostituzione sulle fonti inglesi delle battute](../../images/plugin-reallang-search-replace.png)

![Una battuta tradotta con l'inglese in un popover](../../images/plugin-reallang-story.png)

Il [libro pubblicato](../books/publishing.md) la mostra nel modo richiesto dal libro, con **I lettori del libro pubblicato vedono**: *la traduzione, l'inglese al clic* (il predefinito), *solo la traduzione* (l'inglese non viene mai inviato al browser del lettore) oppure *solo l'inglese*.

<a id="exporting"></a>

## Esportazione

Quando il libro contiene battute tradotte, la finestra di [esportazione](../books/export.md) ha il campo **Battute tradotte (lingua reale)**: *la traduzione così com'è*, *solo l'inglese*, *la traduzione con l'inglese in note a piè di pagina* oppure *l'inglese con la traduzione in note a piè di pagina*. Il testo salvato del libro non viene mai modificato da un'esportazione.

![La finestra di esportazione con le battute tradotte](../../images/plugin-reallang-export.png)

<a id="together-with-the-conlang-translator"></a>

## Insieme al Conlang Translator

Un libro può contenere entrambi: una lingua inventata per gli elfi e il giapponese per la città. Ciascun plugin ha i propri parlanti, la propria impostazione di lingua e i propri passaggi, e i passaggi dei due non si toccano. L'inglese che ogni modello legge contiene entrambi. The [esportazione](../books/export.md) combina le note a piè di pagina di entrambi in un'unica numerazione, e il visualizzatore disegna il parlato di entrambi nella stessa pagina. Imposta lo stesso valore in **I lettori del libro pubblicato vedono** per entrambi: una pagina viene disegnata una sola volta, dal plugin che la raggiunge per primo.
<a id="commands"></a>

## Comandi

Tutto ciò che fanno le schermate è anche un comando, per la [riga di comando](../commands/index.md) e per gli [agenti](../agents.md). Una lingua si indirizza con il suo **nome** (la maiuscola non conta) oppure con il suo id; una parte con il suo handle o con la sua posizione nel ramo attivo (la prima parte è la 1).

| Comando | |
|---|---|
| `reallang-list`, `reallang-create`, `reallang-update`, `reallang-delete` | La libreria. `reallang-create` accetta un `preset` (`ja-kana-kanji`, `ja-kana`, `ja-romaji`, `other`). `reallang-delete` chiede conferma. |
| `reallang-set-book-language` | La lingua del libro aperto (il campo **Language of the speech** delle sue *Impostazioni dell'estensione*). |
| `reallang-add-book-language`, `reallang-remove-book-language` | Le altre lingue del libro aperto. La rimozione chiede conferma all'utente. |
| `reallang-status` | La lingua del libro e le parti tradotte. |
| `reallang-translate-part`, `reallang-translate-range`, `reallang-translate-sentence`, `reallang-translate-text`, `reallang-revert-to-source` | Traduzione: una parte (`missingOnly` traduce solo i discorsi ancora in inglese, `within` cita un passaggio per limitare l'operazione a quello), un intervallo (`mode` *new* o *again*), una frase di una parte, un testo che viene solo restituito, e il ritorno all'inglese. Con il parametro facoltativo `language` agiscono su una lingua del libro (su tutte per impostazione predefinita; sulla prima per una frase o un testo). |
| `reallang-glossary`, `reallang-glossary-set`, `reallang-glossary-remove`, `reallang-glossary-accept`, `reallang-glossary-switch`, `reallang-glossary-move` | Il [glossario](#the-glossary-of-names-and-special-words) del libro aperto in una lingua: leggere le voci unite con il loro livello, aggiungere o modificare una voce a un `level` (`language`, `folder` con il nome della `folder`, `book`), rimuoverne una (chiede conferma), accettare le proposte, disattivare o riattivare per il libro una parola di livello superiore, spostare verso l'alto una voce del libro. |
| `reallang-extract-terms`, `reallang-extraction-set` | [L'estrazione delle parole](#extracting-the-words-by-model): `reallang-extraction-set` sceglie il `prompt` e scrive la `specification` del libro aperto (ciò che viene lasciato fuori resta com'è), `reallang-extract-terms` legge le parti da..a e propone i termini al glossario del libro (`language` per la lingua). |
| `reallang-extraction-prompts`, `reallang-extraction-prompt-set`, `reallang-extraction-prompt-remove` | La libreria dei prompt di estrazione: elencare quelli predefiniti e i propri, aggiungerne o modificarne uno (indicare un prompt predefinito ne crea una copia), rimuoverne uno (chiede conferma). |
| `reallang-edit-source` | Imposta il testo inglese di partenza di un discorso tradotto (`part`, `speech` come lo numera `reallang-list-passages`, `source`); la traduzione così com'è ora conta come quella a cui appartiene. Sostituisce la vecchia fonte. |
| `reallang-get-source`, `reallang-list-passages` | Lettura: il testo di una parte come lo leggono i modelli (l'inglese), e i discorsi tradotti di una parte con la loro lettura e il testo inglese. |
| `reallang-speakers`, `reallang-set-speakers-mode`, `reallang-set-speaker`, `reallang-remove-speaker` | Chi parla la lingua nel libro aperto (`language` indica una delle lingue del libro, la prima per impostazione predefinita); `reallang-set-speaker` imposta anche il livello di `understands`. |
| `reallang-set-understanding-prompt` | Se i livelli di comprensione vengono inseriti da soli nel prompt della storia, e quanti messaggi contare dalla fine. |
| `reallang-understanding` | Chi non segue quale lingua, come testo, per la [macro](../templates/macros.md#commands) {%{`{{command::reallang-understanding::<characters>}}`}%} (i personaggi sono facoltativi). |

Una lingua è poche righe di regole e non ha una copia di lavoro per un'esecuzione di agente (il Conlang Translator copia una lingua insieme al suo lessico): un agente che crea o modifica una lingua modifica subito la tua libreria. Gli oratori, le impostazioni del libro e i passaggi appartengono al libro, e la copia del libro usata dall'esecuzione li porta con sé.

<a id="limits"></a>

## Limiti

- **La qualità dipende dal modello.** La cortesia è la parte più difficile del giapponese; le regole e le note sui caratteri la guidano, e alcune parti lette da una persona decidono se bastano.
- **Chi parla decide quali lingue vengono richieste**, in base al punto di vista e ai personaggi presenti in una parte. Un discorso di un personaggio che la parte non nomina può sfuggire a una lingua i cui parlanti sono elencati; il testo della parte decide all'interno di una richiesta.
- **Nessun secondo passaggio** che riscriva una traduzione per renderla più naturale, e nessun controllo per retrotraduzione per ora.
- **Nessun furigana.** La lettura è una riga nel popover, non un testo ruby nella pagina.
- **Una parte che l'altro plugin di traduzione ha tradotto** viene letta da questo modello con quella traduzione nel testo (non con il suo inglese); gli si dice soltanto che il discorso lì è in un'altra lingua.
- **Su un telefono** sono disponibili il pannello e l'editor; la parte di storia (menu, finestra degli intervalli, clic nella storia) non è stata provata lì.
