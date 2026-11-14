---
label: Guida agli stili
order: 54
---

# Guida agli stili: fai in modo che il tuo libro abbia il tuo aspetto

Uno **stile del testo** decide come appare il testo delle tue parti: il carattere, i colori, la larghezza della colonna, i titoli, la linea tra una scena e l'altra. Questa guida ti accompagna dalla prima modifica fino a un aspetto completo, e si conclude con un esempio completo da scaricare: uno stile per un libro horror che usa tutte le opzioni e del CSS personalizzato.

Dove viene usato uno stile:

| Dove | Cosa succede |
|---|---|
| **L'editor della storia** | Lo stile del libro (o quello predefinito) è l'aspetto di ogni parte. Vedi [Stili del testo](story-editor.md#text-styles). |
| **Il lettore** | Un [libro pubblicato](publishing.md) viene letto nello stile che il *lettore* ha scelto tra i propri stili (lo stile del libro, finché non ne sceglie un altro). |
| **Esportazioni** | Uno stile contrassegnato come **Per le esportazioni** si sceglie nella [finestra di esportazione](export.md#print-styles). HTML ed EPUB usano il foglio di stile, PDF e Word usano i valori numerici (vedi [cosa usa ciascuna esportazione](#what-the-exports-take)). |

<a id="make-your-first-style"></a>

## Crea il tuo primo stile

1. Apri **Impostazioni → Stili** (la [scheda Stili](../account.md#styles)).
2. Seleziona **Libro** nell'elenco e premi **Duplica**. Gli stili integrati non si possono modificare, quindi si parte sempre da una copia chiamata
   *Libro (copia)*. Dagli un nome.
3. Cambia alcune caselle nella scheda **Opzioni**. L'**Anteprima** sotto il modulo mostra un testo di esempio (un titolo di capitolo, un'enfasi, una citazione, una separazione di scena,
   un titolo più piccolo) e segue ogni modifica mentre scrivi.
4. Premi **Salva** nelle impostazioni. Prima di questo passaggio non viene memorizzato nulla, e **Scarta le modifiche** elimina il lavoro svolto.
5. Nell'editor della storia apri il menu delle impostazioni (⚙) → **Aspetto** → **Cambia gli stili del testo** e spunta il tuo stile. Viene memorizzato nel libro. Per usare lo stile in tutti i tuoi libri, selezionalo nell'elenco e premi **Imposta come predefinito**.

![Gli stili nel menu della storia](../../images/story-style-menu.png)

Una casella lasciata vuota mantiene l'aspetto dell'applicazione, quindi uno stile dice solo ciò che vuoi cambiare. Uno stile con tutte le caselle vuote è l'aspetto semplice.

<a id="the-options"></a>

## Le opzioni

Ogni casella accetta un valore CSS. Le unità che userai: `rem` (la dimensione del testo della pagina; 1rem di solito corrisponde a 16px), `em` (la dimensione del testo corrente), `px`, `%`, e i colori come `#336699`, `rgb(51, 102, 153)` oppure un nome come `ivory`.

<a id="text"></a>

### Testo

| Opzione | Cosa fa | Esempio |
|---|---|---|
| **Famiglia di caratteri** | Il carattere del testo. Indica un elenco: viene usato il primo che il computer possiede, l'ultimo dovrebbe essere una famiglia generica (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Dimensione del carattere** | La dimensione del testo. | `1.1rem` |
| **Interlinea** | La distanza tra le righe. Un numero senza unità è un multiplo della dimensione del carattere. | `1.7` |
| **Spaziatura tra le lettere**, **Spaziatura tra le parole** | Spazio extra tra le lettere, tra le parole. Valori piccoli: una minima spaziatura tra le lettere rende il testo arioso. | `0.01em`, `0.05em` |
| **Colore del testo** | Il colore del testo. | `#cfc6bb` |
| **Allineamento del testo** | `left`, `right`, `center` oppure `justify`. | `justify` |
| **Sillabazione** | Spezza le parole lunghe a fine riga. Usa il dizionario della [lingua](managing-books.md#the-about-tab) del libro, quindi imposta prima la lingua. Funziona meglio insieme alla giustificazione. | attiva |
| **Spaziatura dei paragrafi** | Lo spazio sotto un paragrafo. Un libro di solito ne ha poco, o nessuno con un rientro della prima riga. | `0.3em` |
| **Rientro della prima riga** | Rientra la prima riga di un paragrafo, come in un libro stampato, tranne dopo un titolo o una separazione di scena. | `1.5em` |
| **Lettera capitale** | Una grande lettera iniziale del primo paragrafo dopo un titolo. | attiva |

<a id="page"></a>

### Pagina

| Opzione | Cosa fa | Esempio |
|---|---|---|
| **Larghezza del testo** | Una colonna di larghezza propria, centrata. Una riga di 60 a 75 caratteri si legge meglio, circa da `34em` a `40em`. | `38em` |
| **Colore di sfondo** | Il colore dietro il testo; riempie tutta l'area, non solo i blocchi di testo. Scegli un colore del testo che si legga su di esso. | `#0c0a0a` |

<a id="emphasis-links-and-code"></a>

### Enfasi, link e codice

| Opzione | Cosa fa |
|---|---|
| **Colore dell'enfasi** | Il colore del testo in *corsivo*. |
| **Colore del grassetto** | Il colore del testo in **grassetto**. |
| **Colore dei link** | Il colore dei link. |
| **Famiglia di caratteri del codice** | Il carattere di `code` e dei blocchi di codice. |

<a id="quotes"></a>

### Citazioni

| Opzione | Cosa fa |
|---|---|
| **Colore della citazione** | Il colore del testo di una citazione (`> a line`). |
| **Colore del bordo della citazione** | Il colore della barra a lato di una citazione. |

<a id="headings"></a>

### Titoli

| Opzione | Cosa fa |
|---|---|
| **Famiglia di caratteri dei titoli** | Il carattere dei titoli `#`, `##` e `###`. |
| **Dimensione dei titoli** | La dimensione del primo livello; quelli inferiori sono più piccoli in proporzione. |
| **Colore dei titoli** | Il colore dei titoli. |
| **Allineamento dei titoli** | `left`, `center` oppure `right`. |
| **Maiuscoletto nei titoli** | Scrive i titoli in maiuscoletto. |

<a id="scene-break"></a>

### Separazione di scena

Una riga con `---` nel testo è una separazione tra scene. Per impostazione predefinita sono tre puntini. **Testo della separazione di scena** è ciò che viene mostrato al suo posto
(`* * *`, `~`, `† † †`), **Colore di separazione delle scene** il suo colore.

<a id="custom-css"></a>

## CSS personalizzato

Le opzioni coprono le cose più comuni. Per il resto, la scheda **CSS personalizzato** accetta CSS tuo, aggiunto dopo tutto il resto e applicato solo all'interno dello stile. Due modi per scriverlo:

- **Dichiarazioni semplici** si applicano a tutta l'area che contiene il testo:

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Regole** con un selettore. Scrivile con `&` (l'area dello stile) davanti, oppure senza: in entrambi i casi raggiungono solo il testo che ha questo stile.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

Il testo di una parte si trova in un elemento con la classe `chat-message-markdown`, quindi un selettore per qualcosa nel testo parte da lì. Il campo **CSS generato** sotto la casella CSS mostra il foglio di stile prodotto dalle opzioni: copia i suoi selettori per vedere cosa puoi sovrascrivere.

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

- **Le parentesi graffe `{ }` devono corrispondere**, altrimenti lo stile viene rifiutato al salvataggio. I commenti `/* ... */` sono ammessi.
- Alcune regole di cui l'applicazione ha bisogno sono `!important` (il testo non supera mai la larghezza della sua area, le parole lunghe vanno a capo, i paragrafi mantengono i loro a capo). Il tuo CSS non può sovrascriverle; può sovrascrivere tutto il resto, perché viene per ultimo.
- Il CSS può cambiare come appaiono le cose, non cosa dicono. Non raggiunge le barre degli strumenti e le schede intorno al testo.
- Il CSS personalizzato viene usato nella storia, nel lettore e nelle esportazioni HTML. EPUB, PDF e Word non lo usano.

<a id="what-the-exports-take"></a>

## Cosa usano le esportazioni

Uno stile per le esportazioni si sceglie nella [finestra di esportazione](export.md#print-styles); **Duplica** uno stile e spunta **Per le esportazioni**, oppure parti da uno dei quattro che l'applicazione fornisce. I colori che dipendono dal tema dell'applicazione (il testo chiaro del tema scuro) diventano scuri per una pagina bianca durante l'esportazione.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| caratteri, dimensioni, interlinea, spaziature, allineamento, rientro della prima riga | ✓ | ✓ | ✓ | ✓ |
| colori di testo, enfasi, grassetto, citazioni, titoli, separazione di scena | ✓ | ✓ | ✓ | ✓ |
| colore di sfondo | ✓ | ✓ | ✓ | ✓ (il colore della pagina) |
| larghezza del testo, lettere capitali, sillabazione, maiuscoletto dei titoli | ✓ | ✓ | | |
| CSS personalizzato | ✓ | | | |

I lettori di e-book spesso usano i propri caratteri, dimensioni e colori, qualunque cosa dica il foglio di stile. PDF e Word possono usare solo i caratteri che il formato prevede: il PDF sceglie graziato, senza graziato o monospazio dall'elenco che hai scritto; Word usa il primo carattere dell'elenco e mostra il proprio se il computer non lo possiede.
<a id="share-a-style-export-and-import"></a>

## Condividere uno stile: esportazione e importazione

Sotto l'elenco degli stili, **Esporta** salva lo stile selezionato come file (`name.style.json`; anche uno stile predefinito può essere esportato) e **Importa**
legge un file di questo tipo. Uno stile importato viene aggiunto ai tuoi stili come nuovo stile, non viene memorizzato prima che tu prema **Salva**, e mantiene
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

Può contenere più stili, e puoi anche scriverne uno a mano. Le opzioni hanno nomi in maiuscolo (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...); l'[esempio horror](#an-example-a-horror-book) le contiene tutte. Un'opzione che questa versione di Marginalia
non conosce viene esclusa (te lo viene comunicato con il numero di opzioni escluse), un file di un formato di versione più recente viene rifiutato, e così pure uno stile il cui CSS personalizzato
ha parentesi graffe non corrispondenti.

<a id="an-example-a-horror-book"></a>

## Un esempio: un libro horror

[Scarica lo stile horror](../../files/horror-book.style.json) (`horror-book.style.json`), importalo in *Impostazioni → Stili* e selezionalo
per vederlo nell'anteprima. Imposta **ogni opzione** e usa CSS personalizzato per ciò che le opzioni non coprono:

| | |
|---|---|
| **Pagina** | Sfondo quasi nero `#0c0a0a`, una colonna di `38em`, e una vignettatura scura che si stringe sui bordi (CSS personalizzato). |
| **Testo** | Garamond a `1.12rem`, altezza di riga `1.8`, giustificato con sillabazione, un rientro della prima riga di `1.6em` e pochissimo spazio tra i paragrafi, come in un romanzo stampato. Il testo è del grigio della carta antica `#cfc6bb`, con una lettera iniziale ingrandita dopo ogni titolo. |
| **Enfasi** | Corsivo in rosa polveroso `#b99a94`, grassetto in rosso sangue `#b3261e`. |
| **Link** | Verde malaticcio `#8fa07e` che diventa rosso e ondulato quando il puntatore vi passa sopra (CSS personalizzato). |
| **Citazioni** | Testo grigio con una barra rosso scuro `#5c1010` e una lieve macchia rossa sullo sfondo (CSS personalizzato). |
| **Titoli** | Palatino in maiuscoletto, centrato, rosso scuro `#8e1b17`, con un leggero bagliore (CSS personalizzato). |
| **Separazione delle scene** | `† † †` in rosso scuro; gli elenchi hanno una croce al posto del punto (CSS personalizzato). |
| **Codice** | Courier New, su uno sfondo quasi nero. |

![Una storia nello stile horror](../../images/story-style-horror.png)

Per provarlo su un testo reale, incolla questo in una parte (oppure l'anteprima mostra un campione simile):

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

Poi modificalo: imposta lo sfondo `#14100c` per un marrone illuminato da candela, oppure imposta **Larghezza del testo** a `32em` per una pagina più stretta, o rimuovi il CSS personalizzato per vedere
che cosa fanno le sole opzioni. Quando trovi un risultato che ti piace, **Esporta** lo stile e conserva il file insieme al tuo libro.
