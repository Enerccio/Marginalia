---
label: Libro di esempio
order: 80
---

# Libro di esempio

*Unregistered* è un libro completo da aprire, leggere e smontare: circa 100 parti (circa 99.000 parole) di un'unica storia, con tutto ciò che le funzioni di Marginalia lasciano dietro di sé. È un buon modo per vedere come appare un libro lungo nell'[editor della storia](../books/story-editor.md), nei [riassunti](../books/summaries.md), nelle [annotazioni](../books/annotations.md) e nelle [statistiche](../books/statistics.md), oppure per provare le estensioni su qualcosa che è già pronto.

> Una guida di cantina ceca che ha imparato il giapponese dai romanzi e un'infermiera giapponese in vacanza attraversano un arco murato in una cantina di Praga e si risvegliano in una valle fluviale governata da un impero che parla il vrakht.
> Nessuno dei due conosce la lingua, e un assessore della tassa sulla vita ha trovato due persone che non sono nel suo registro.

<a id="what-is-in-it"></a>

## Cosa contiene

| | |
|---|---|
| **La storia** | 100 parti sulla linea principale e un ramo laterale di 3 parti, in 32 capitoli (i titoli sono nel testo). |
| **Riassunti** | Uno per ogni capitolo, di qualsiasi lunghezza (l'ultimo capitolo ogni 25 parti è lasciato intero, perché il modello lo legge così com'è), e i meta-riassunti che fondono i più vecchi, nel formato compatto del [prompt di riassunto](../books/summaries.md) predefinito. |
| **Annotazioni** | Circa 450 note di revisione (continuità, stile, lore, punto di vista, ritmo) che la funzione *Annota con l'IA* del libro ha proposto e che la riscrittura ha applicato; ogni parte ha anche le sue versioni (360 in tutto, vedi [cronologia delle versioni](../books/version-history.md)). |
| **Lorebook** | 31 voci: il mondo, i luoghi, le persone e le lingue. |
| **Prompt** | Ogni parte conserva il prompt da cui è stata scritta e il lore che è stato attivato: **Mostra il prompt**, **Confronta il prompt** e **Utilizzo del lore** nel menu della parte ([Prompt del libro](../books/prompts.md)). |
| **Knowledge Tracker** | Sette personaggi tracciati e ciò che ciascuno sa e non sa, parte per parte ([Knowledge Tracker](../extensions/knowledge-tracker.md)). |
| **Discorsi tradotti** | Circa 1.250 brani tradotti: i discorsi del Dominio in **vrakht** e della valle in **Aethel-Vael** ([Conlang Translator](../extensions/conlang-translator.md)), e in **giapponese** e **ceco** ([Real Language Translator](../extensions/real-language-translator.md)). |
| **Capitoli** | La struttura della storia, creata da [Arc Outline](../extensions/arc-outline.md) a partire dal testo. |
| **Un'immagine** | Un'illustrazione, nella parte 31. |

I dati delle quattro estensioni sono nel libro, ma li vedi solo con le estensioni caricate; senza di esse vengono conservati e ignorati. Il libro non contiene né modello né protocollo: dopo l'importazione, scegli il tuo nelle
impostazioni del libro.

## Download

I file sono allegati alla release [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1):

| File | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip) (5,7 MB) | Il libro: il backup con la sua immagine. |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | Il lorebook del libro, da solo. Anche il file del libro lo contiene; questo ti serve solo per consultare il lorebook senza il libro. |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | La lingua costruita del Dominio, per il Conlang Translator. |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | La lingua costruita della valle, per il Conlang Translator. |

<a id="importing-it"></a>

## Come importarlo

1. **Le lingue** (se hai il Conlang Translator): in **Impostazioni → Impostazioni estensione → Conlang Translator** clicca su **Importa** e scegli `vrakht.json`, poi ripeti l'operazione per `aethel-vael.json`
   ([Conlang Translator](../extensions/conlang-translator.md#languages)). Fallo prima del libro, così le impostazioni linguistiche del libro hanno a cosa fare riferimento.
2. **Il libro:** nella scheda *Impostazioni* della finestra del libro clicca su **Importa il backup come nuovo libro**, inserisci un nome e carica `unregistered-book.zip`
   ([come si importa un backup](../books/backups.md#importing-a-backup-as-a-new-book)). Quando Marginalia chiede del lorebook, scegli **Importa**: è nel file.
3. Apri il nuovo libro e, nelle sue impostazioni, scegli il tuo modello e protocollo.

I passaggi tradotti sono memorizzati nel libro, quindi i discorsi in vrakht, Aethel-Vael, giapponese e ceco vengono mostrati come sono stati tradotti; per tradurne altri con il Real Language Translator, crea tu le lingue *Japanese* e *Czech* lì (il plugin le offre come preset), perché il plugin non esporta le sue lingue insieme al libro.

<a id="where-it-comes-from-and-the-licence"></a>

## Da dove viene e licenza

La storia, il lorebook e le due lingue sono originali. La struttura, il lorebook, le lingue e il piano delle parti sono stati scritti dall'autore di Marginalia; il testo delle parti è stato **scritto da un modello di IA** (Claude Haiku) attraverso il ciclo di generazione di Marginalia: generare, annotare, riscrivere, tracciare la conoscenza, tradurre, riassumere. È una dimostrazione del programma, non un romanzo pubblicato, e ha le stranezze della prosa di un modello.

- Il testo, il lorebook e le due lingue sono rilasciati nel pubblico dominio con [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/): usali per qualsiasi scopo.
- L'immagine è *Interior of a Cottage in Scheveningen* (1887) di Willem Adriaan Alexander Liernur, di pubblico dominio, da [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art).
