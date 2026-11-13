---
label: Esportare un libro
order: 55
---

# Esportare un libro

L'esportazione trasforma la storia in un file da leggere, stampare, inviare o pubblicare altrove. A differenza di un
[backup](backups.md), contiene solo il testo, sotto forma di libro compiuto.

Viene esportato solo il [ramo attivo](branches-and-story-tree.md), cioè le stesse parti che mostra l'editor della storia. Per
esportare un'altra versione della storia, passa prima a quel ramo.

<a id="exporting"></a>

## Esportazione

1. Apri il libro e vai alla scheda **Storia**.
2. Apri il menu delle impostazioni (⚙) nella barra inferiore e scegli **Esporta la storia**.
3. Compila la finestra di dialogo e scegli **Esporta**.
4. Marginalia raccoglie le parti del ramo e scrive il file, operazione che richiede un momento per un libro lungo. Una barra
   di avanzamento mostra a che punto è.
5. Quando il file è pronto, una finestra di dialogo ne offre il download. Scegli il pulsante **Scarica** per salvarlo.

![La finestra di dialogo Esporta la storia](../../images/export-dialog.png)

| Campo | |
|---|---|
| **Formato** | Il tipo di file, vedi sotto. |
| **Stile di stampa** | Solo per i formati che hanno caratteri e colori (HTML, Word, PDF, EPUB). L'aspetto del testo, scelto tra gli stili *per le esportazioni*: l'applicazione ne fornisce uno per ciascuno di questi formati, **Pagina HTML**, **E-book (EPUB)**, **Libro PDF** e **Documento Word**, che sono gli aspetti che le esportazioni hanno sempre avuto e vengono scelti automaticamente quando scegli il formato; puoi anche crearne di tuoi, vedi [Stili di stampa](#print-styles). Lo stile che hai scelto per il formato in [Impostazioni → Esportazioni](../account.md#exports) è quello preselezionato. |
| **Nome del file** | Nome del file scaricato senza l'estensione, che Marginalia aggiunge. Inizialmente è il nome del libro. |
| **Titolo del libro** | Il titolo nel file (frontespizio e proprietà del file). Inizialmente è il nome del libro; cambiarlo non rinomina il libro. |
| **Autore** | Inizialmente il tuo nome completo dal tuo [account](../account.md). Se vuoto, il file non ha autore. |
| **Dal messaggio**, **Al messaggio** | Quali parti esportare. Le parti sono numerate da 1 nell'ordine della storia; le parti senza testo non vengono contate. Lascia **Al messaggio** vuoto per esportare fino alla fine. |
| **Includi il frontespizio** | Apre il file con una prima pagina che riporta il titolo e l'autore. |
| **Includi la dichiarazione di paternità** | Chiude il file con una breve dichiarazione di chi ha scritto le parole: quante parole hai scritto tu e quante ha generato un modello, in parole e in percentuale, con i modelli. Alcune case editrici e alcuni concorsi la richiedono. Viene calcolata dalla [cronologia delle versioni](version-history.md) delle parti esportate (vedi [Evidenziare il testo generato](story-editor.md#highlighting-generated-text)). Non viene offerta per la chat di SillyTavern. Il testo è un modello in [Impostazioni → Esportazioni](../account.md#exports). |

![L'esportazione completata, pronta per il download](../../images/export-download.png)

<a id="formats"></a>

## Formati

| Formato | |
|---|---|
| **Testo semplice** (`.txt`) | Solo il testo in UTF-8. La formattazione, come il *corsivo*, viene eliminata e i paragrafi sono separati da una riga vuota. |
| **Markdown** (`.md`) | Il testo così come è scritto nella storia, per editor come Obsidian o Typora, un sito statico o un repository. Enfasi, titoli, citazioni, elenchi, blocchi di codice e separatori di scena restano in Markdown. L'HTML grezzo nel testo viene mantenuto così come è stato scritto; sta al lettore Markdown decidere se mostrarlo. |
| **HTML** (`.html`) | Una pagina unica con uno stile da libro incluso, quindi appare uguale in ogni browser. Può essere stampata e non ha file esterni. |
| **Documento Word** (`.docx`) | Si apre in Word, LibreOffice e programmi simili. Testo giustificato, con titolo e autore impostati nelle proprietà del file. |
| **PDF** (`.pdf`) | Pagine A5 con caratteri incorporati che coprono testi latini, greci e cirillici, e un carattere giapponese incluso (Noto Sans JP) per kana, kanji e punteggiatura giapponese. Il testo di altri sistemi di scrittura (solo caratteri cinesi, coreano, arabo) non viene disegnato: il PDF non ha un carattere per esso. |
| **EPUB** (`.epub`) | Un e-book per lettori elettronici e app di lettura. I libri lunghi vengono suddivisi in più file all'interno dell'EPUB, così i lettori restano veloci. |
| **HTML per Archive of Our Own** (`.html`) | Per la pubblicazione su AO3, vedi [sotto](#for-other-tools). |
| **Markdown con front matter** (`.md`) | Per i generatori di siti statici, vedi [sotto](#for-other-tools). |
| **Chat di SillyTavern** (`.jsonl`) | La storia come chat, vedi [sotto](#for-other-tools). |
| **Vault di Obsidian** (`.zip`) | I capitoli e il lorebook come note collegate, vedi [sotto](#for-other-tools). |
| **LaTeX** (`.zip`) | Un progetto LaTeX del libro (`main.tex` e le immagini), vedi [sotto](#for-other-tools). |

Il testo delle parti è in Markdown. Enfasi, titoli, citazioni, elenchi, blocchi di codice e separatori di scena (`---`) vengono
mantenuti in ogni formato che può mostrarli. L'HTML grezzo nel testo viene mostrato come testo, mai eseguito.

<a id="footnotes"></a>

## Note a piè di pagina

Una nota a piè di pagina si scrive nel testo di una parte come `[^1]` dove deve comparire il segno, e `[^1]: la nota` su una
riga a sé (l'etichetta può essere una parola qualsiasi). Ogni formato le supporta: **Markdown** mantiene la sintassi, **HTML**,
**EPUB** e **HTML per Archive of Our Own** mettono un piccolo numero in apice che rimanda alle note sotto il testo della parte
(AO3 riceve la marcatura semplice che il suo editor conserva), **testo semplice** scrive il segno come `[1]` e le note come righe
che iniziano con `[1]` dopo il testo della parte, come appare una nota di chiusura. **Word** e **PDF** hanno vere note a piè di
pagina: il segno è un piccolo numero in apice e la nota sta in fondo alla stessa pagina, in un carattere più piccolo (nel PDF
sotto un breve filetto, e le pagine vengono impaginate finché ogni pagina ha lo spazio che le note richiedono; Word numera e
posiziona le note da solo, quindi le seguono se modifichi il documento). Anche le estensioni possono usarle: l'estensione
Conlang Translator scrive la traduzione in inglese di ogni battuta come nota a piè di pagina.

<a id="images"></a>

## Immagini

Le [immagini](story-editor.md#images) delle parti vengono collocate dove indica la loro posizione (dopo il testo, salvo che tu abbia scelto un'altra
collocazione), con le loro didascalie, nella larghezza che hai assegnato (una percentuale della larghezza del testo; senza larghezza l'immagine mantiene
la sua dimensione, ridotta se necessario alla pagina). Non tutti i formati possono far scorrere il testo accanto a un'immagine:

| Formato | |
|---|---|
| **HTML**, **EPUB** | Incorporate (nella pagina, o come file del libro). Il testo scorre intorno alle immagini a sinistra o a destra; un'immagine al centro sta tra due colonne di testo. Su uno schermo stretto vanno sopra il testo, e un lettore senza colonne (alcuni lettori elettronici) mostra il primo testo, l'immagine e il resto uno sotto l'altro. Le immagini affiancate in una stessa posizione stanno in una riga. |
| **Documento Word** | L'immagine intorno alla quale scorre il testo sta in una piccola tabella che Word colloca di lato o al centro, e Word fa scorrere il testo intorno ad essa (su entrambi i lati per il centro, riga per riga); la didascalia sta sotto l'immagine. Le immagini con una larghezza che stanno affiancate sono una riga di una tabella. |
| **PDF** | Il testo scorre intorno alle immagini di lato, la didascalia viene disegnata sotto l'immagine. Un'immagine al centro sta tra due colonne di testo allineato a sinistra (una fascia che non entra alla fine di una pagina inizia nella successiva). Le immagini con una larghezza che stanno affiancate formano una riga. Un'immagine troppo alta per lo spazio rimasto nella pagina passa alla pagina successiva. |
| **Markdown** | Incorporate nel file come immagini `![didascalia](data:...)` con la didascalia in corsivo sotto, così resta un unico file. La posizione tra il testo viene mantenuta; la larghezza e lo scorrimento del testo no. Non tutti i lettori mostrano le immagini incorporate (GitHub no), e un libro con molte immagini ha righe molto lunghe, quindi allega le immagini alle parti solo se le vuoi in un'esportazione Markdown. |
| **Testo semplice** | Segnate con `[Image]` o `[Image: didascalia]` nel punto corrispondente del testo. |
| **HTML per Archive of Our Own** | Segnate con `[Image: didascalia]`: AO3 accetta immagini solo da collegamenti. |
| **Vault di Obsidian** | Salvate nella cartella `Attachments` del vault e incorporate nella nota del loro capitolo nel punto giusto, con la didascalia in corsivo. |
| **LaTeX** | Salvate nella cartella `images` del progetto e incluse nel testo nel punto giusto (una figura nella larghezza dell'immagine, mai più larga del testo) con la didascalia sotto. Il testo non scorre intorno a esse. |
| **Chat di SillyTavern** | Omesse, una chat contiene solo testo. |
<a id="for-other-tools"></a>

## Per altri strumenti

Questi formati sono pensati per uno strumento che li legge, non per la lettura. Non hanno la scelta *Stile di stampa*.

**HTML per Archive of Our Own.** AO3 chiede il titolo, l'autore e i titoli dei capitoli in campi propri e scarta ciò che non conosce (blocchi di stile, la pagina che circonda il testo, le immagini che non sono link), quindi il file non ha il frontespizio. Aprilo in un browser per vedere l'opera come apparirà incollata. Ogni capitolo (una parte che inizia con un titolo e le parti successive fino al titolo seguente) ha il suo titolo sopra due commenti, `BEGIN chapter N` e `END chapter N`: copia ciò che sta tra di essi nell'editor HTML del capitolo su AO3 e il titolo nel suo campo titolo. Il testo è HTML pulito senza interruzioni di riga tra i tag, perché AO3 trasforma ogni interruzione di riga in un `<br>`.

**Markdown con front matter.** Lo stesso Markdown del formato *Markdown*, ma il file inizia con un blocco YAML (`title`, `author`, `lang`, `date` dell'esportazione) che generatori come Jekyll, Hugo, Eleventy e simili leggono, al posto di un frontespizio; **Includi il frontespizio** non ha effetto su di esso. Il blocco è un modello che puoi modificare in [Impostazioni → Esportazioni](../account.md#exports) (`{{title}}`, `{{author}}`, `{{language}}`, `{{exportDate}}`; il titolo e l'autore sono già sottoposti a escape per l'uso tra virgolette doppie).

**Chat di SillyTavern.** Un file JSON Lines che l'importazione delle chat di SillyTavern legge: il personaggio prende il nome del libro e tu sei l'autore. Ogni parte della storia è un messaggio del personaggio (il testo), e quando la parte è stata scritta con delle istruzioni, esse vengono prima come tuo messaggio. Il ragionamento del modello, se la parte ne ha uno, viene conservato come ragionamento del messaggio. Il frontespizio non si applica.

**Vault di Obsidian.** Uno ZIP con una cartella che prende il nome dal libro; decomprimilo e apri la cartella come vault. Contiene una nota con il nome del libro (il front matter, il frontespizio e gli elenchi dei capitoli e del lore), una nota per ogni capitolo in `Chapters` (con link al capitolo precedente e a quello successivo), una nota per ogni voce abilitata del [lorebook](../lorebooks/index.md) del libro e dei suoi sotto-lorebook in `Lore/<lorebook>` (i tag della voce sono i tag della nota, il suo commento è un callout), e le immagini in `Attachments`. In ogni nota la prima menzione del nome di una voce (un nome di almeno tre caratteri, parole intere, scritto come è) è un link alla sua nota, così la vista a grafo mostra quale voce si trova in quale capitolo. Una parte con un titolo inizia un capitolo; le parti prima del primo titolo formano la nota *Beginning* (o *Story* quando il libro non ha titoli). Il front matter e il frontespizio sono modelli che puoi modificare in [Impostazioni → Esportazioni](../account.md#exports).

**LaTeX.** Uno ZIP con una cartella che prende il nome dal libro. `main.tex` è una `book` che compila con `pdflatex`, `xelatex` o `lualatex` e le immagini delle parti si trovano in `images`. Il frontespizio è un `titlepage` composto dal [modello del frontespizio](../account.md#exports). Il livello di titolo più alto della storia diventa un capitolo, il livello successivo una sezione e così via; non sono numerati (una storia dà ai suoi capitoli il proprio nome) ma sono elencati in un indice. Enfasi, citazioni, elenchi, blocchi di codice e interruzioni di scena diventano ciò che sono in LaTeX, le note a piè di pagina sono vere note a piè di pagina, i caratteri che hanno un significato per LaTeX sono scritti come testo, e la lingua del libro seleziona la sillabazione (`babel`) quando è una delle più comuni. Un libro con testo giapponese, cinese o coreano richiede `xelatex` o `lualatex` (`pdflatex` si ferma con un messaggio): il file carica `xeCJK` o `luatexja` per esso, e con `xelatex` imposti un font che contenga i tuoi caratteri (la riga da modificare è nel commento sopra di essa, per esempio `\setCJKmainfont{Noto Serif CJK JP}`), altrimenti viene usato solo il font cinese fornito con `xeCJK`. Carica lo ZIP su [Overleaf](https://www.overleaf.com) oppure decomprimilo ed esegui `pdflatex main.tex` (due volte, per l'indice). Lo stile di stampa non si applica: l'aspetto è quello della classe, modificalo nel preambolo di `main.tex`.

<a id="print-styles"></a>

## Stili di stampa

L'esportazione usa gli stili del testo di [Impostazioni → Stili](../account.md#styles) contrassegnati **Per esportazioni**. Gli stili sono di due tipi, per libri (la storia e il lettore) o per esportazioni: il menu *Cambia gli stili del testo* e il lettore mostrano solo il primo tipo, la finestra di esportazione solo il secondo. I quattro integrati sono gli aspetti dei formati; **Duplica** uno di essi (oppure uno stile di libro, e spunta **Per esportazioni**) per crearne uno tuo, per esempio lo stile Libro su carta. Lo stile è pensato per la carta: i colori che dipendono dal tema dell'applicazione (il testo chiaro del tema scuro) diventano quelli scuri per una pagina bianca.

| Formato | Cosa dà lo stile |
|---|---|
| **HTML** | L'intero foglio di stile dello stile, incluso il **CSS personalizzato**, le **lettere capitali decorate**, la larghezza del testo e lo sfondo; il frontespizio e l'indice mantengono l'aspetto del formato. |
| **EPUB** | Il foglio di stile dello stile senza il CSS personalizzato (non tutti i lettori di e-book lo capiscono; alcuni lettori impostano anche i propri font e dimensioni). |
| **PDF** | La famiglia di font (serif, sans o monospace dei font incorporati, scelta dalla lista dei font), la dimensione, l'interlinea, l'allineamento, lo spazio tra i paragrafi, il rientro della prima riga, il **colore di sfondo** della pagina (tutta la pagina, frontespizio compreso), i colori del testo, del corsivo, del grassetto, delle citazioni, dei titoli e dell'interruzione di scena, l'allineamento e la dimensione dei titoli e il simbolo dell'interruzione di scena. |
| **Documento Word** | Lo stesso del PDF, con il font indicato nello stile (Word mostra il proprio quando il computer non lo possiede) al posto di quelli incorporati. Lo sfondo è il colore della pagina: Word lo mostra sullo schermo, ma non lo stampa a meno che *Stampa colori e immagini di sfondo* non sia attivo nelle opzioni di stampa. |

Le lettere capitali decorate, il CSS personalizzato (PDF, Word, EPUB), le maiuscoletto dei titoli e la larghezza del testo non sono realizzati nei file PDF e Word.

<a id="title-page-template"></a>

## Modello del frontespizio

Il frontespizio è scritto a partire da un modello, uno per ogni formato. Modificalo in [Impostazioni → Esportazioni](../account.md#exports): il modello è Markdown con `{{title}}` (il titolo del libro nella finestra) e `{{author}}`, per esempio `## {{title}}` e una riga `scritto da {{author}}`. Il predefinito è il titolo come titolo e l'autore in corsivo.

Gli stessi modelli (il frontespizio, il front matter, la dichiarazione di paternità) possono essere modificati **per un solo libro o per una cartella**: nella scheda *Prompt* del libro e nella finestra della cartella, **Impostazioni dell'estensione** ha un pannello per ogni formato di esportazione, con un campo per ciascuno dei suoi modelli. Viene usata prima la versione propria del libro, altrimenti quella della cartella più vicina sopra di esso, altrimenti la tua in *Impostazioni → Esportazioni*, altrimenti il predefinito, così una serie può avere un proprio frontespizio. Un campo vuoto è ereditato e indica da dove.
<a id="table-of-contents"></a>

## Indice

Quando il testo contiene dei capitoli, dopo la pagina del titolo il file riceve un indice. Un capitolo è una parte della storia che contiene un'intestazione Markdown (una riga che inizia con `#`, per esempio `# Chapter 3: The Harbour`), la stessa regola che usa il plugin [Chapter Marker](../extensions/chapter-marker.md) nella barra laterale della storia. La prima intestazione di una parte ne è il titolo; le parti senza intestazione appartengono al capitolo precedente. Una storia senza alcuna intestazione non ha un indice.

![La pagina del titolo e l'indice di un PDF esportato](../../images/export-result.png)

In PDF, Word ed EPUB ogni capitolo inizia su una nuova pagina. Anche la pagina del titolo e l'indice occupano ciascuno una pagina propria.

Le voci sono link ai capitoli:

| Formato | |
|---|---|
| **HTML** | Collegamenti ai capitoli della pagina. |
| **PDF** | Voci cliccabili con la pagina del capitolo; i capitoli compaiono anche nel sommario (segnalibri) che il lettore PDF mostra accanto alle pagine. |
| **EPUB** | L'indice del lettore, che è anche una pagina del libro dopo la pagina del titolo. |
| **Documento Word** (`.docx`) | Voci cliccabili con la pagina del capitolo. Word chiede di aggiornare i campi quando apre il file: rispondi **Sì** per riempire i numeri di pagina (LibreOffice li riempie da solo). Si tratta di un elenco di link e non di un campo indice di Word. |
| **Markdown** | Nessun indice. Le intestazioni dei capitoli restano nel testo e la maggior parte degli editor Markdown ne costruisce una struttura ad albero. |
| **Testo semplice** | Nessun indice. |

<a id="page-numbers"></a>

## Numeri di pagina

I file PDF e Word hanno il numero di pagina nel piè di pagina di ogni pagina, tranne quella del titolo. Il numero è la posizione della pagina nel file: quindi la pagina del titolo è la pagina 1 e la prima pagina dopo di essa è la 2. Gli altri formati non hanno pagine proprie.

<a id="the-title-page"></a>

## La pagina del titolo

Con **Includi il frontespizio** il file si apre con il titolo del libro e l'autore su una pagina a sé. La pagina è generata da un modello integrato (`DEFAULT_EXPORT_HEADER_TEMPLATE`), che al momento non è modificabile nell'applicazione.

## Good to know

- L'esportazione è un'istantanea del ramo nel momento in cui scegli **Esporta**. Le parti generate o modificate in seguito non sono nel file.
- Il file esportato resta disponibile solo finché non chiudi la finestra di download. Esporta di nuovo se ti serve più tardi.
- Riassunti, lorebook, istruzioni e il ragionamento del modello non vengono esportati: solo il testo e le immagini delle parti. Per salvare tutto, crea un [backup](backups.md).
