---
label: Libri
order: 960
icon: book
---

# Libri

Un libro è una storia. Gli fornisci le istruzioni una parte alla volta, il modello scrive il testo, e il libro conserva tutto ciò che vi è collegato: l'albero della storia con tutti i suoi rami, i prompt che danno forma allo stile, il lorebook con il mondo e i personaggi, i riassunti e i backup.

![Un libro nell'editor della storia](../../images/hero-story-editor.png)

<a id="parts-and-branches"></a>

## Parti e rami

Un libro è composto da **parti**. Ogni parte è una risposta del modello, scritta a partire dalle istruzioni che hai dato per essa (scena, personaggio point of view, personaggi presenti e cosa deve accadere).

Le parti formano un albero. Di solito si continua semplicemente la storia, ma è anche possibile scrivere un'altra versione dell'ultima parte, oppure tornare indietro e continuare in modo diverso da una qualsiasi parte precedente. Ogni percorso dall'inizio a una fine è un **ramo**. Il ramo su cui stai lavorando è il **ramo attivo**; l'editor della storia lo mostra, e solo le sue parti vengono inviate al modello. Vedi [Rami e albero della storia](branches-and-story-tree.md).

<a id="the-book-window"></a>

## La finestra del libro

Aprendo un libro si mostra la sua finestra con queste schede:

| Scheda | |
|---|---|
| **Informazioni** | Nome, tag, descrizione, lingua, modello, protocollo, pubblicazione e conteggio delle parole. Vedi [Gestire i libri](managing-books.md). |
| **Prompt** | I template e le impostazioni di stile di questo libro. Vedi [Prompt del libro](prompts.md). |
| **Lorebook** | Il lorebook di questo libro e le sue voci. Quando ha dei sotto-lorebook, la seconda casella **Lorebook mostrato** fa passare la scheda a uno qualsiasi di essi (e di nuovo indietro) senza cambiare il lorebook del libro; cliccare il nome di un sotto-lorebook in **Sotto-lorebook** ha lo stesso effetto. Vedi [Lorebook](../lorebooks/index.md). |
| **Storia** | L'editor della storia, dove le parti vengono scritte e generate. Vedi [Editor della storia](story-editor.md). |
| **Vista dei rami** | L'albero della storia con tutti i rami. Vedi [Rami e albero della storia](branches-and-story-tree.md). |
| **Backup** | I backup del libro. Vedi [Backup del libro](backups.md). |

Ciò che è successo a un libro giorno per giorno (parole, tempo, token, dimensione) e il calendario dei giorni in cui hai scritto si trovano nella sua finestra **Statistiche**, vedi [Statistiche di scrittura](statistics.md).

La storia può essere esportata da Marginalia come file dal menu delle impostazioni della scheda *Storia*, vedi [Esportare un libro](export.md).

Un libro senza parti si apre su *Informazioni*, un libro con parti su *Storia*. Tutte le modifiche vengono salvate subito; non c'è un pulsante *Salva*. **Esci** chiude il libro.

Mentre una parte è in fase di generazione, le altre schede e *Esci* sono disabilitate finché la generazione non termina o non la interrompi.

<a id="in-this-section"></a>

## In questa sezione

- [Gestire i libri](managing-books.md) - l'elenco dei libri, creare, modificare ed eliminare i libri
- [Cartelle](folders.md) - ordinare i libri in cartelle, impostazioni ereditate dai libri, spostare e copiare
- [Editor della storia](story-editor.md) - scrivere istruzioni, generare e modificare le parti
- [Rami e albero della storia](branches-and-story-tree.md) - rigenerare, scorrere, creare rami, la vista ad albero
- [Cronologia delle versioni](version-history.md) - le versioni precedenti di una parte: confrontare, ripristinare, dare un nome, cercare
- [Prompt del libro](prompts.md) - template, point of view, tempo verbale e stile
- [Riassunti](summaries.md) - mantenere i libri lunghi entro il contesto del modello
- [Esportare un libro](export.md) - la storia come testo, Markdown, HTML, Word, PDF o EPUB
- [Backup del libro](backups.md) - backup, ripristino, copie di un libro
- [Pubblicazione e visualizzatore](publishing.md) - vista di lettura e condivisione di un libro
