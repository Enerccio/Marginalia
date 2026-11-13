---
label: Guida agli stili
order: 54
---

# Guida agli stili: fai in modo che il tuo libro abbia il suo aspetto

Uno **stile del testo** decide come appare il testo delle tue parti: il carattere, i colori, la larghezza della colonna, i titoli, la linea tra le
scene. Questa guida ti accompagna dalla prima modifica a un aspetto completo, e si conclude con un esempio completo da scaricare: uno stile per
un libro horror che usa tutte le opzioni e un CSS personalizzato.

Dove viene usato uno stile:

| Dove | Cosa succede |
|---|---|
| **L'editor della storia** | Lo stile del libro (o il tuo predefinito) è l'aspetto di ogni parte. Vedi [Stili del testo](story-editor.md#text-styles). |
| **Il lettore** | Un [libro pubblicato](publishing.md) viene letto nello stile che il *lettore* sceglie tra i propri stili (lo Stile del libro, finché non ne sceglie un altro). |
| **Esportazioni** | Uno stile contrassegnato come **Per le esportazioni** si sceglie nella [finestra di esportazione](export.md#print-styles). HTML ed EPUB usano il foglio di stile, PDF e Word usano i valori numerici (vedi [cosa usa ciascuna esportazione](#what-the-exports-take)). |

<a id="make-your-first-style"></a>

## Crea il tuo primo stile

1. Apri **Impostazioni → Stili** (la [scheda Stili](../account.md#styles)).
2. Seleziona **Book** nell'elenco e premi **Duplica**. Gli stili predefiniti non possono essere modificati, quindi parti sempre da una copia chiamata
   *Book (copy)*. Dalle un nome.
3. Modifica alcune caselle nella scheda **Opzioni**. L'**Anteprima** sotto il modulo mostra un testo di esempio (un titolo di capitolo, enfasi, una citazione, una separazione di scena,
   un titolo più piccolo) e segue ogni modifica mentre digiti.
4. Premi **Salva** delle impostazioni. Prima di questo non viene memorizzato nulla, e **Scarta le modifiche** cancella il lavoro svolto.
5. Nell'editor della storia apri il menu delle impostazioni (⚙) → **Aspetto** → **Cambia gli stili del testo** e seleziona il tuo stile. Viene memorizzato nel
   libro. Per usare lo stile in tutti i tuoi libri, selezionalo nell'elenco e premi **Imposta come predefinito**.

![Gli stili nel menu della storia](../../images/story-style-menu.png)

Una casella lasciata vuota mantiene l'aspetto dell'applicazione, quindi uno stile indica solo ciò che vuoi cambiare. Uno stile con tutte le caselle vuote è l'aspetto semplice.

<a id="the-options"></a>

## Le opzioni

Ogni casella accetta un valore CSS. Unità che userai: `rem` (la dimensione del testo della pagina, 1rem è normalmente 16px), `em` (la dimensione del
testo corrente), `px`, `%`, e colori come `#336699`, `rgb(51, 102, 153)` o un nome come `ivory`.

<a id="text"></a>

### Testo

| Opzione | Cosa fa | Esempio |
|---|---|---|
| **Font family** | Il carattere del testo. Indica un elenco: viene usato il primo che il computer possiede, l'ultimo dovrebbe essere una famiglia generica (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Dimensione del carattere** | La dimensione del testo. | `1.1rem` |
| **Interlinea** | La distanza tra le righe. Un numero semplice è un multiplo della dimensione del carattere. | `1.7` |
| **Spaziatura tra le lettere**, **Spaziatura tra le parole** | Spazio extra tra le lettere, tra le parole. Valori piccoli: una minima spaziatura tra le lettere rende il testo arioso. | `0.01em`, `0.05em` |
| **Colore del testo** | Il colore del testo. | `#cfc6bb` |
| **Text align** | `left`, `right`, `center` o `justify`. | `justify` |
| **Hyphenation** | Spezza le parole lunghe alla fine di una riga. Usa il dizionario della [lingua](managing-books.md#the-about-tab) del libro, quindi imposta prima la lingua. Funziona meglio insieme a *justify*. | on |
| **Paragraph spacing** | Lo spazio sotto un paragrafo. Un libro di solito ne ha poco, o nessuno con un rientro della prima riga. | `0.3em` |
| **Rientro della prima riga** | Rientra la prima riga di un paragrafo, come in un libro stampato, tranne dopo un titolo o una separazione di scena. | `1.5em` |
| **Drop cap** | Una grande lettera iniziale del primo paragrafo dopo un titolo. | on |

<a id="page"></a>

### Pagina

| Opzione | Cosa fa | Esempio |
|---|---|---|
| **Larghezza del testo** | Una colonna di larghezza propria, centrata. Una riga di 60 a 75 caratteri si legge meglio, circa `34em` a `40em`. | `38em` |
| **Colore di sfondo** | Il colore dietro il testo; riempie l'intera area, non solo i blocchi di testo. Scegli un colore del testo che si legga su di esso. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### Enfasi, link e codice

| Opzione | Cosa fa |
|---|---|
| **Emphasis color** | Il colore del testo in *corsivo*. |
| **Strong color** | Il colore del testo in **grassetto**. |
| **Colore dei link** | Il colore dei link. |
| **Code font family** | Il carattere di `codice` e dei blocchi di codice. |

<a id="quotes"></a>

### Citazioni

| Opzione | Cosa fa |
|---|---|
| **Quote color** | Il colore del testo di una citazione (`> una riga`). |
| **Quote border color** | Il colore della barra sul lato di una citazione. |

<a id="headings"></a>

### Titoli

| Opzione | Cosa fa |
|---|---|
| **Heading font family** | Il carattere dei titoli `#`, `##` e `###`. |
| **Heading size** | La dimensione del primo livello; quelli inferiori sono più piccoli in proporzione. |
| **Colore dei titoli** | Il colore dei titoli. |
| **Heading align** | `left`, `center` o `right`. |
| **Heading small caps** | Scrive i titoli in maiuscoletto. |

<a id="scene-break"></a>

### Separazione di scena

Una riga con `---` nel testo è una separazione tra le scene. Per impostazione predefinita sono tre puntini. **Scene break text** è ciò che viene mostrato invece
(`* * *`, `~`, `† † †`), **Colore di separazione delle scene** il suo colore.

<a id="custom-css"></a>

## CSS personalizzato

Le opzioni coprono le cose più comuni. Per il resto, la scheda **CSS personalizzato** accetta CSS proprio, aggiunto dopo tutto il resto e applicato solo
all'interno dello stile. Due modi per scriverlo:

- **Dichiarazioni semplici** si applicano a tutta l'area che contiene il testo:

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Regole** con un selettore. Scrivile con `&` (l'area dello stile) davanti, oppure senza: in entrambi i casi raggiungono solo il testo che ha questo stile.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

Il testo di una parte si trova in un elemento con la classe `chat-message-markdown`, quindi un selettore per qualcosa nel testo inizia da lì. Il
campo **CSS generato** sotto la casella CSS mostra il foglio di stile prodotto dalle opzioni: copia i suoi selettori per vedere cosa puoi sovrascrivere.

| Vuoi cambiare | Selettore (dopo `& .chat-message-markdown`) |
|---|---|
| paragrafi | `p` |
| titoli | `h1`, `h2`, `h3` |
| corsivo, grassetto | `em`, `strong` |
| link | `a`, `a:hover` |
| citazioni | `blockquote` |
| elenchi | `ul`, `ol`, `li` |
| codice | `code` (in linea), `pre` (blocco) |
| separazione di scena | `hr`, `hr::after` (il simbolo) |
| la prima riga di un paragrafo | `p::first-line` |
| testo selezionato | `::selection` |

Cose da sapere:

- **Le parentesi graffe `{ }` devono corrispondere**, altrimenti lo stile viene rifiutato al salvataggio. I commenti `/* ... */` vanno bene.
- Alcune regole di cui l'applicazione ha bisogno sono `!important` (il testo non supera mai la sua area, le parole lunghe vanno a capo, i paragrafi mantengono
  i loro a capo). Il tuo CSS non può sovrascriverle; può sovrascrivere tutto il resto, perché viene per ultimo.
- Il CSS può cambiare come appaiono le cose, non cosa dicono. Non raggiunge le barre degli strumenti e le schede intorno al testo.
- Il CSS personalizzato viene usato nella storia, nel lettore e nelle esportazioni HTML. EPUB, PDF e Word non lo usano.

<a id="what-the-exports-take"></a>

## Cosa usano le esportazioni

Uno stile per le esportazioni si sceglie nella [finestra di esportazione](export.md#print-styles); **Duplica** uno stile e seleziona **Per le esportazioni**, oppure parti da uno dei quattro
che l'applicazione fornisce. I colori che dipendono dal tema dell'applicazione (il testo chiaro del tema scuro) diventano scuri per una
pagina bianca durante l'esportazione.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| carattere, dimensioni, interlinea, spaziatura, allineamento, rientro della prima riga | ✓ | ✓ | ✓ | ✓ |
| colori di testo, enfasi, grassetto, citazioni, titoli, separazione di scena | ✓ | ✓ | ✓ | ✓ |
| colore di sfondo | ✓ | ✓ | ✓ | ✓ (il colore della pagina) |
| larghezza del testo, drop cap, sillabazione, maiuscoletto dei titoli | ✓ | ✓ | | |
| CSS personalizzato | ✓ | | | |

I lettori di e-book spesso usano i propri caratteri, dimensioni e colori, qualunque cosa dica il foglio di stile. PDF e Word possono usare solo i caratteri che il formato
prevede: il PDF sceglie serif, sans o monospace dall'elenco che hai scritto; Word usa il primo carattere
dell'elenco e mostra il proprio se il computer non lo possiede.
<a id="share-a-style-export-and-import"></a>

## Condividere uno stile: esportazione e importazione

Sotto l'elenco degli stili, **Esporta** salva lo stile selezionato come file (`name.style.json`; anche uno stile predefinito può essere esportato) e **Importa**
legge un file di questo tipo. Uno stile importato viene aggiunto ai tuoi stili come nuovo stile: non viene memorizzato finché non premi **Salva**, e conserva
il suo nome, quindi rinominalo se ne hai già uno simile.

Il file è in JSON:

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

Può contenere più stili, e puoi scriverne uno a mano. Le opzioni sono indicate in maiuscolo (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...); l'[esempio horror](#an-example-a-horror-book) le contiene tutte. Un'opzione che questa versione di Marginalia
non conosce viene esclusa (te lo viene comunicato con il numero di opzioni escluse), un file di una versione più recente del formato viene rifiutato, e così pure uno stile il cui CSS personalizzato ha
parentesi graffe non corrispondenti.

<a id="an-example-a-horror-book"></a>

## Un esempio: un libro horror

[Scarica lo stile horror](../../files/horror-book.style.json) (`horror-book.style.json`), importalo in *Impostazioni → Stili* e selezionalo
per vederlo nell'anteprima. Imposta **ogni opzione** e usa CSS personalizzato per ciò che le opzioni non coprono:

| | |
|---|---|
| **Pagina** | Sfondo quasi nero `#0c0a0a`, una colonna di `38em` e una vignettatura scura che si stringe sui bordi (CSS personalizzato). |
| **Testo** | Garamond a `1.12rem`, interlinea `1.8`, giustificato con sillabazione, rientro della prima riga di `1.6em` e quasi nessuno spazio tra i paragrafi, come in un romanzo stampato. Il testo è del grigio della vecchia carta `#cfc6bb`, con una lettera iniziale ornata dopo ogni titolo. |
| **Enfasi** | Corsivo in rosa polveroso `#b99a94`, grassetto in rosso sangue `#b3261e`. |
| **Link** | Verde malaticcio `#8fa07e` che diventa rosso e ondulato quando il puntatore vi passa sopra (CSS personalizzato). |
| **Citazioni** | Testo grigio con una barra rosso scuro `#5c1010` e una lieve macchia rossa sullo sfondo (CSS personalizzato). |
| **Titoli** | Palatino in maiuscoletto, centrato, rosso scuro `#8e1b17`, con un leggero bagliore (CSS personalizzato). |
| **Separazione delle scene** | `† † †` in rosso scuro; gli elenchi ricevono una croce al posto del punto (CSS personalizzato). |
| **Codice** | Courier New, su uno sfondo quasi nero. |

![Una storia nello stile horror](../../images/story-style-horror.png)

Per provarlo su un testo vero, incolla questo in una parte (oppure l'anteprima mostra un esempio simile):

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

Poi modificalo: imposta lo sfondo a `#14100c` per un marrone a lume di candela, oppure imposta **Larghezza del testo** a `32em` per una pagina più stretta, o rimuovi il CSS personalizzato per vedere
cosa fanno da soli le opzioni. Quando hai trovato un aspetto che ti piace, **Esporta** lo stile e conserva il file insieme al tuo libro.
