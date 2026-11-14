---
label: Esportare un libro
order: 55
---

# Esportare un libro

Esportare trasforma la storia in un file che puoi leggere, stampare, inviare o pubblicare altrove. A differenza di un [backup](backups.md), contiene solo il testo, sotto forma di libro finito.

Viene esportato solo il [ramo attivo](branches-and-story-tree.md), cioè le stesse parti che mostra l'editor della storia. Per esportare un'altra versione della storia, passa prima a quel ramo.

<a id="exporting"></a>

## Esportazione

1. Apri il libro e vai alla scheda **Storia**.
2. Apri il menu delle impostazioni (⚙) nella barra in basso e scegli **Esporta la storia**.
3. Compila la finestra di dialogo e scegli **Esporta**.
4. Marginalia raccoglie le parti del ramo e scrive il file. Per un libro lungo l'operazione richiede un momento. Una barra di avanzamento mostra a che punto è.
5. Quando il file è pronto, una finestra di dialogo ne propone il download. Scegli il pulsante **Scarica** per salvarlo.

![La finestra di dialogo Esporta la storia](../../images/export-dialog.png)

| Campo | |
|---|---|
| **Formato** | Il tipo di file, vedi sotto. |
| **Stile di stampa** | Solo per i formati che hanno caratteri e colori (HTML, Word, PDF, EPUB). L'aspetto del testo, scelto tra gli stili *per le esportazioni*: l'applicazione ne fornisce uno per ciascuno di questi formati, **Pagina HTML**, **E-book (EPUB)**, **Libro PDF** e **Documento Word**. Sono gli aspetti che le esportazioni hanno sempre avuto e vengono scelti automaticamente quando scegli il formato. Puoi anche crearne di tuoi, vedi [Stili di stampa](#print-styles). Lo stile che hai scelto per il formato in [Impostazioni → Esportazioni](../account.md#exports) è quello preselezionato. |
| **Nome del file** | Nome del file scaricato, senza l'estensione che Marginalia aggiunge. Inizialmente è il nome del libro. |
| **Titolo del libro** | Il titolo nel file (frontespizio e proprietà del file). Inizialmente è il nome del libro; cambiarlo non rinomina il libro. |
| **Autore** | Inizialmente il tuo nome completo, preso dal tuo [account](../account.md). Vuoto significa nessun autore. |
| **Dal messaggio**, **To message** | Quali parti esportare. Le parti sono numerate da 1 nell'ordine della storia; le parti senza testo non vengono contate. Lascia **To message** vuoto per esportare fino alla fine. |
| **Includi il frontespizio** | Apre il file con una prima pagina che riporta il titolo e l'autore. |
| **Includi la dichiarazione di paternità** | Chiude il file con una breve dichiarazione su chi ha scritto il testo: quante parole hai scritto tu e quante ha generato un modello, in parole e in percentuale, con i modelli. Alcuni editori e concorsi la richiedono. Viene calcolata dalla [cronologia delle versioni](version-history.md) delle parti esportate (vedi [Evidenziare il testo generato](story-editor.md#highlighting-generated-text)). Non è offerta per la chat di SillyTavern. Il testo è un modello in [Impostazioni → Esportazioni](../account.md#exports). |

![Il file esportato, pronto per il download](../../images/export-download.png)

<a id="formats"></a>

## Formati

| Formato | |
|---|---|
| **Testo semplice** (`.txt`) | Solo il testo in UTF-8. La formattazione, come il *corsivo*, viene eliminata; i paragrafi sono separati da una riga vuota. |
| **Markdown** (`.md`) | Il testo così come è scritto nella storia, per editor come Obsidian o Typora, un sito statico o un repository. Enfasi, titoli, citazioni, elenchi, blocchi di codice e separatori di scena restano Markdown. L'HTML grezzo nel testo viene mantenuto come scritto; spetta al lettore Markdown decidere se mostrarlo. |
| **HTML** (`.html`) | Una sola pagina con uno stile da libro incluso, così appare uguale in ogni browser. Può essere stampata e non ha file esterni. |
| **Documento Word** (`.docx`) | Si apre in Word, LibreOffice e programmi simili. Testo giustificato, con titolo e autore impostati nelle proprietà del file. |
| **PDF** (`.pdf`) | Pagine A5 con caratteri incorporati che coprono testi latini, greci e cirillici, e un carattere giapponese incluso (Noto Sans JP) per kana, kanji e punteggiatura giapponese. Il testo di altri alfabeti (solo caratteri cinesi, coreano, arabo) non viene disegnato: il PDF non ha un carattere per esso. |
| **EPUB** (`.epub`) | Un e-book per lettori elettronici e app di lettura. I libri lunghi sono divisi in più file all'interno dell'EPUB, così i lettori restano veloci. |
| **HTML per Archive of Our Own** (`.html`) | Per pubblicare su AO3, vedi [sotto](#for-other-tools). |
| **Markdown con front matter** (`.md`) | Per i generatori di siti statici, vedi [sotto](#for-other-tools). |
| **Chat di SillyTavern** (`.jsonl`) | La storia come chat, vedi [sotto](#for-other-tools). |
| **Vault di Obsidian** (`.zip`) | I capitoli e il lorebook come note collegate, vedi [sotto](#for-other-tools). |
| **LaTeX** (`.zip`) | Un progetto LaTeX del libro (`main.tex` e le immagini), vedi [sotto](#for-other-tools). |

Il testo delle parti è Markdown. Enfasi, titoli, citazioni, elenchi, blocchi di codice e separatori di scena (`---`) sono mantenuti in ogni formato che può mostrarli. L'HTML grezzo nel testo viene mostrato come testo, mai eseguito.

<a id="footnotes"></a>

## Note a piè di pagina

Una nota a piè di pagina si scrive nel testo di una parte come `[^1]` nel punto in cui compare il segno, e come `[^1]: the note` su una riga a sé (l'etichetta può essere una parola qualsiasi). Ogni formato le supporta. **Markdown** mantiene la sintassi. **HTML**, **EPUB** e **HTML per Archive of Our Own** mettono un piccolo numero in apice che rimanda alle note sotto il testo della parte (AO3 riceve la marcatura semplice che il suo editor conserva). Il **testo semplice** scrive il segno come `[1]` e le note come righe che iniziano con `[1]` dopo il testo della parte, come una nota di chiusura. **Word** e **PDF** hanno vere note a piè di pagina: il segno è un piccolo numero in apice e la nota sta in fondo alla stessa pagina, in un carattere più piccolo. Nel PDF la nota è sotto una breve linea, e le pagine vengono impaginate finché ognuna ha lo spazio che le sue note richiedono. Word numera e colloca le note da solo, quindi si spostano se modifichi il documento. Anche le estensioni possono usarle: l'estensione Conlang Translator scrive l'inglese di ogni discorso come nota a piè di pagina.

<a id="images"></a>

## Immagini

Le [immagini](story-editor.md#images) delle parti sono collocate dove indica la loro posizione (dopo il testo, salvo che tu scelga un altro punto), con le loro didascalie, nella larghezza che hai impostato (una percentuale della larghezza del testo; senza larghezza l'immagine mantiene la sua dimensione, ridotta per entrare nella pagina). Non tutti i formati possono affiancare il testo a un'immagine:

| Formato | |
|---|---|
| **HTML**, **EPUB** | Incorporate (nella pagina, o come file del libro). Il testo scorre intorno alle immagini a sinistra o a destra; un'immagine al centro sta tra due colonne di testo. Su uno schermo stretto vanno sopra il testo, e un lettore senza colonne (alcuni lettori elettronici) mostra il primo testo, l'immagine e il resto uno sotto l'altro. Le immagini affiancate in un punto stanno in fila. |
| **Documento Word** | L'immagine intorno a cui scorre il testo sta in una piccola tabella che Word colloca di lato o al centro, e Word avvolge il testo intorno ad essa (su entrambi i lati per il centro, riga per riga). La didascalia sta sotto l'immagine. Le immagini con una larghezza che stanno affiancate sono una riga di una tabella. |
| **PDF** | Il testo scorre intorno alle immagini di lato, e la didascalia è disegnata sotto l'immagine. Un'immagine al centro sta tra due colonne di testo allineato a sinistra (una fascia che non entra alla fine di una pagina inizia nella successiva). Le immagini con una larghezza che stanno affiancate formano una riga. Un'immagine troppo alta per lo spazio rimasto nella pagina passa alla pagina successiva. |
| **Markdown** | Incorporate nel file come `![caption](data:...)`, con la didascalia in corsivo sotto, così resta un unico file. Si mantiene la posizione tra il testo, non la larghezza né il testo che scorre. Non tutti i lettori mostrano immagini incorporate (GitHub no), e un libro con molte immagini ha righe molto lunghe. Allega quindi immagini alle parti solo quando le vuoi in un'esportazione Markdown. |
| **Testo semplice** | Segnate con `[Image]` o `[Image: caption]` nel loro punto del testo. |
| **HTML per Archive of Our Own** | Segnate con `[Image: caption]`: AO3 accetta immagini solo da link. |
| **Vault di Obsidian** | Salvate nella cartella `Attachments` del vault e incorporate nella nota del loro capitolo nel loro punto, con la didascalia in corsivo. |
| **LaTeX** | Salvate nella cartella `images` del progetto e incluse nel testo nel loro punto (una figura nella larghezza dell'immagine, mai più larga del testo) con la didascalia sotto. Il testo non scorre intorno a esse. |
| **Chat di SillyTavern** | Escluse: una chat ha solo testo. |
<a id="for-other-tools"></a>

## Per altri strumenti

Questi formati sono pensati per uno strumento che li legge, non per la lettura. Non hanno la scelta *Stile di stampa*.

**HTML per Archive of Our Own.** AO3 chiede il titolo, l'autore e i titoli dei capitoli in campi propri e scarta ciò che non riconosce (blocchi di stile, la pagina intorno al testo, le immagini che non sono link), quindi il file non ha frontespizio. Aprilo in un browser per vedere l'opera come apparirà dopo l'incolla. Ogni capitolo (una parte che inizia con un titolo e le parti successive fino al titolo seguente) ha il suo titolo sopra due commenti, `BEGIN chapter N` e `END chapter N`: copia ciò che si trova tra di essi nell'editor HTML del capitolo su AO3 e il titolo nel suo campo titolo. Il testo è HTML pulito, senza interruzioni di riga tra i tag, perché AO3 trasforma ogni interruzione di riga in un `<br>`.

**Markdown con front matter.** Lo stesso Markdown del formato *Markdown*, ma il file inizia con un blocco YAML (`title`, `author`, `lang`, `date` dell'esportazione) che Jekyll, Hugo, Eleventy e generatori simili leggono, al posto di un frontespizio; **Includi il frontespizio** non ha effetto su di esso. Il blocco è un modello che puoi modificare in [Impostazioni → Esportazioni](../account.md#exports) (`{{title}}`, `{{author}}`, `{{language}}`, `{{exportDate}}`; il titolo e l'autore sono già sottoposti a escape per l'uso tra virgolette doppie).

**Chat di SillyTavern.** Un file JSON Lines che l'importazione delle chat di SillyTavern legge: il personaggio prende il nome del libro e tu sei l'autore. Ogni parte della storia è un messaggio del personaggio (il testo), e quando la parte è stata scritta con delle istruzioni, esse vengono prima come tuo messaggio. Il ragionamento del modello, se la parte ne ha uno, viene conservato come ragionamento del messaggio. Il frontespizio non si applica.

**Vault di Obsidian.** Uno ZIP con una cartella che porta il nome del libro; decomprimilo e apri la cartella come vault. Contiene una nota con il nome del libro (il front matter, il frontespizio e gli elenchi dei capitoli e del lore), una nota per ogni capitolo in `Chapters` (con link al capitolo precedente e a quello successivo), una nota per ogni voce attiva del [lorebook](../lorebooks/index.md) del libro e dei suoi sotto-lorebook in `Lore/<lorebook>` (i tag della voce diventano i tag della nota, il suo commento diventa un callout), e le immagini in `Attachments`. In ogni nota, la prima menzione del nome di una voce (un nome di almeno tre caratteri, parole intere, come scritto) è un link alla sua nota, così la vista grafo mostra quale voce si trova in quale capitolo. Una parte con un titolo apre un capitolo; le parti prima del primo titolo formano la nota *Inizio* (oppure *Storia* quando il libro non ha titoli). Il front matter e il frontespizio sono modelli che puoi modificare in [Impostazioni → Esportazioni](../account.md#exports).

**LaTeX.** Uno ZIP con una cartella che porta il nome del libro. `main.tex` è un `book` che si compila con `pdflatex`, `xelatex` o `lualatex`, e le immagini delle parti si trovano in `images`. Il frontespizio è un `titlepage` costruito dal [modello del frontespizio](../account.md#exports). Il titolo più superficiale della storia è un capitolo, il livello successivo una sezione, e così via; non sono numerati (una storia dà un nome ai suoi capitoli da sola) ma sono elencati in un indice. Enfasi, citazioni, elenchi, blocchi di codice e separatori di scena diventano ciò che sono in LaTeX, le note a piè di pagina sono vere note a piè di pagina, i caratteri che hanno un significato per LaTeX sono scritti come testo, e la lingua del libro seleziona la sillabazione (`babel`) quando è una delle più comuni. Un libro con testo giapponese, cinese o coreano richiede `xelatex` o `lualatex` (`pdflatex` si ferma con un messaggio): il file carica per esso `xeCJK` o `luatexja`, e con `xelatex` imposti un carattere che contenga i tuoi caratteri (la riga da modificare si trova nel commento sopra di essa, per esempio `\setCJKmainfont{Noto Serif CJK JP}`); altrimenti viene usato solo il carattere cinese fornito con `xeCJK`. Carica lo ZIP su [Overleaf](https://www.overleaf.com) oppure decomprimilo ed esegui `pdflatex main.tex` (due volte, per l'indice). Lo stile di stampa non si applica: l'aspetto è quello della classe, cambialo nel preambolo di `main.tex`.

<a id="print-styles"></a>

## Stili di stampa

L'esportazione usa gli stili di testo di [Impostazioni → Stili](../account.md#styles) contrassegnati come **Per le esportazioni**. Gli stili sono di due tipi, per i libri (la storia e il lettore) o per le esportazioni: il menu *Cambia gli stili del testo* e il lettore mostrano solo il primo tipo, la finestra di esportazione solo il secondo. I quattro stili integrati sono gli aspetti dei formati; **Duplica** uno di essi (oppure uno stile di libro, e spunta **Per le esportazioni**) per crearne uno tuo, per esempio lo stile Libro su carta. Lo stile è pensato per la carta: i colori che dipendono dal tema dell'applicazione (il testo chiaro del tema scuro) diventano quelli scuri per una pagina bianca.

| Formato | Che cosa dà lo stile |
|---|---|
| **HTML** | L'intero foglio di stile, incluso il **CSS personalizzato**, le **lettere capitali decorate**, la larghezza del testo e lo sfondo; il frontespizio e l'indice mantengono l'aspetto del formato. |
| **EPUB** | Il foglio di stile senza il CSS personalizzato (non tutti i lettori di e-book lo capiscono; alcuni lettori impostano anche i propri caratteri e dimensioni). |
| **PDF** | La famiglia di caratteri (con grazie, senza grazie o a spaziatura fissa tra i caratteri incorporati, scelta dallo stack dei caratteri), la dimensione, l'interlinea, l'allineamento, lo spazio tra i paragrafi, il rientro della prima riga, il **colore di sfondo** della pagina (tutta la pagina, frontespizio compreso), i colori del testo, del corsivo, del grassetto, delle citazioni, dei titoli e del separatore di scena, l'allineamento e la dimensione dei titoli e il simbolo del separatore di scena. |
| **Documento Word** | Lo stesso del PDF, con il carattere indicato nello stile (Word mostra il proprio quando il computer non lo possiede) al posto di quelli incorporati. Lo sfondo è il colore della pagina: Word lo mostra sullo schermo, ma non lo stampa a meno che *Stampa i colori e le immagini di sfondo* non sia attivo nelle sue opzioni di stampa. |

Le lettere capitali decorate, il CSS personalizzato (PDF, Word, EPUB), le maiuscoletti dei titoli e la larghezza del testo non vengono realizzati nei file PDF e Word.

<a id="title-page-template"></a>

## Modello del frontespizio

Il frontespizio è scritto a partire da un modello, uno per ogni formato. Modificalo in [Impostazioni → Esportazioni](../account.md#exports): il modello è Markdown con `{{title}}` (il titolo del libro della finestra di dialogo) e `{{author}}`, per esempio `## {{title}}` e una riga `written by {{author}}`. Il predefinito è il titolo come intestazione e l'autore in corsivo.

Gli stessi modelli (il frontespizio, il front matter, la dichiarazione di autorialità) possono essere modificati **per un solo libro o per una cartella**: nella scheda *Prompt* del libro e nella finestra della cartella, **Impostazioni dell'estensione** ha un pannello per ogni formato di esportazione, con un campo per ciascuno dei suoi modelli. Viene usata per prima la versione propria del libro, altrimenti quella della cartella più vicina sopra di esso, altrimenti la tua da *Impostazioni → Esportazioni*, altrimenti il predefinito, così una serie può avere un proprio frontespizio. Un campo vuoto eredita il valore e indica da dove proviene.
<a id="table-of-contents"></a>

## Indice

Quando il testo contiene dei capitoli, il file riceve un indice dopo il frontespizio. Un capitolo è una parte della storia che contiene un titolo Markdown (una riga che inizia con `#`, ad esempio `# Chapter 3: The Harbour`): è la stessa regola usata dal plugin [Chapter Marker](../extensions/chapter-marker.md) nella barra laterale della storia. Il primo titolo di una parte ne è il titolo; le parti senza titolo appartengono al capitolo che le precede. Una storia senza alcun titolo non ha un indice.

![Il frontespizio e l'indice di un PDF esportato](../../images/export-result.png)

In PDF, Word ed EPUB ogni capitolo inizia su una nuova pagina. Anche il frontespizio e l'indice occupano ciascuno una pagina propria.

Le voci sono collegamenti ai capitoli:

| Formato | |
|---|---|
| **HTML** | Collegamenti ai capitoli della pagina. |
| **PDF** | Voci cliccabili con la pagina del capitolo; i capitoli compaiono anche nel sommario (segnalibri) che il lettore PDF mostra accanto alle pagine. |
| **EPUB** | L'indice del lettore, che è anche una pagina del libro dopo il frontespizio. |
| **Documento Word** (`.docx`) | Voci cliccabili con la pagina del capitolo. Word chiede di aggiornare i campi quando apre il file: rispondi **Sì** per compilare i numeri di pagina (LibreOffice li compila da solo). Si tratta di un elenco di collegamenti e non di un campo indice di Word. |
| **Markdown** | Nessun indice. I titoli dei capitoli restano nel testo, e la maggior parte degli editor Markdown costruisce una struttura ad albero a partire da essi. |
| **Testo semplice** | Nessun indice. |

<a id="page-numbers"></a>

## Numeri di pagina

I file PDF e Word riportano il numero di pagina nel piè di pagina di ogni pagina, tranne il frontespizio. Il numero è la posizione della pagina nel file: il frontespizio è la pagina 1 e la prima pagina successiva è la 2. Gli altri formati non hanno pagine proprie.

<a id="the-title-page"></a>

## Il frontespizio

Con **Includi il frontespizio** il file si apre con il titolo del libro e l'autore su una pagina propria. La pagina è creata da un modello integrato (`DEFAULT_EXPORT_HEADER_TEMPLATE`), che nell'applicazione non è ancora modificabile.

<a id="good-to-know"></a>

## Da sapere

- L'esportazione è un'istantanea del ramo nel momento in cui scegli **Esporta**. Le parti generate o modificate in seguito non sono nel file.
- Il file esportato resta disponibile solo finché non chiudi la finestra di download. Esporta di nuovo se ti serve più tardi.
- Riassunti, lorebook, istruzioni e il ragionamento del modello non vengono esportati: sono esportati solo il testo e le immagini delle parti. Per salvare tutto, crea un [backup](backups.md).
