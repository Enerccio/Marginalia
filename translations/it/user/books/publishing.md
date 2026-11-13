---
label: Pubblicazione e visualizzatore
order: 40
---

# Pubblicazione e visualizzatore

Il **visualizzatore** è una vista di lettura dei tuoi libri: solo il testo della storia, senza l'editor intorno. Funziona
bene su telefoni e tablet. **Pubblicare** un libro permette agli altri utenti della stessa installazione di Marginalia di
leggerlo nel visualizzatore.

<a id="opening-the-viewer"></a>

## Aprire il visualizzatore

Il visualizzatore si trova all'indirizzo `/view` del tuo Marginalia, per esempio:

- applicazione desktop: `http://127.0.0.1:8765/view`
- server: `https://marginalia.example.com/view`

Usa lo stesso accesso di Marginalia.

![L'elenco dei libri nel visualizzatore](../../images/viewer-list.png)

L'elenco mostra i tuoi libri, con i loro tag e la data in cui li hai aperti per l'ultima volta. Filtralo per **Nome** (libri
il cui nome inizia con il testo) e per **Tag** (libri con tutti i tag selezionati). Ordinalo per **Ultima apertura** o per **Nome**;
cliccando di nuovo il pulsante di ordinamento attivo si inverte l'ordine.

Clicca su un libro per leggerlo.

<a id="folders-in-the-list"></a>

### Cartelle nell'elenco

L'elenco mostra le tue [cartelle](folders.md) come l'elenco da telefono dello spazio di lavoro: prima le cartelle, ciascuna con
il numero di libri che contiene, poi i libri della cartella corrente. Clicca su una cartella per entrarci. Il percorso sopra i
filtri (`Libri / Romanzi / Portal fantasy`) indica dove ti trovi, e cliccando su una parte del percorso torni lì.

![Il visualizzatore dentro una cartella](../../images/viewer-folder.png)

- **Includi le sottocartelle** elenca tutti i libri contenuti sotto la cartella corrente, a qualsiasi profondità, con la cartella di
  ciascuno indicata sotto il nome.
- **Preferiti**, la cartella del livello superiore che esiste finché hai un libro preferito, elenca i libri preferiti di tutte le
  cartelle. I preferiti si impostano nell'[elenco dei libri](folders.md#favorites), non nel visualizzatore.
- I filtri lavorano dentro la cartella corrente (o anche sotto di essa con *Includi le sottocartelle*), quindi per cercare in tutti
  i tuoi libri vai al livello superiore e seleziona la casella. **Ultima apertura** ordina le cartelle in base alla loro ultima modifica.
- Il visualizzatore si apre nella cartella in cui avevi lasciato: dopo la lettura di un libro la freccia ti riporta alla stessa
  cartella, e anche la visita successiva, o un ricaricamento della pagina, parte da lì. Il punto è conservato in un cookie del tuo
  browser (`marginalia_viewer_folder`); il server non lo memorizza, quindi un altro browser parte dal livello superiore. Una cartella
  che è stata eliminata equivale al livello superiore.

<a id="reading"></a>

## Lettura

La vista di lettura mostra il [ramo attivo](branches-and-story-tree.md) del libro dalla prima all'ultima parte, nello stile di
lettura del libro (giustificato, con sillabazione secondo la [lingua](managing-books.md#the-about-tab) del libro). Gli altri rami, le
istruzioni di turno e i metadati non sono mostrati. Il pulsante con la freccia nell'angolo in alto a sinistra torna all'elenco.

Il pulsante tondo nell'angolo in alto a destra apre un menu di **stili del testo** con cui leggere: gli stili *Normale* e *Libro*
e quelli che hai creato in [Impostazioni → Stili](../account.md#styles) (quello spuntato è mostrato; finché non ne scegli uno si usa lo
stile Libro). Vengono proposti solo i tuoi stili, anche in un libro pubblicato da un altro utente: ciò che l'autore usa per scrivere
non arriva al lettore. La scelta è conservata in un cookie del tuo browser (`marginalia_reader_style`), quindi vale per ogni libro
che leggi lì e il server non la memorizza; uno stile che non è tuo (un altro utente che ha effettuato l'accesso nello stesso browser)
viene ignorato.

![Il menu degli stili nel lettore](../../images/viewer-style-menu.png)

![Lettura di un libro](../../images/viewer-reading.png)

Quello che leggi è il libro così com'è ora: le nuove parti compaiono alla prossima apertura della pagina.

Un libro lungo viene letto a pezzi: vengono mostrate le parti intorno al punto in cui ti trovi, quella successiva (e quella
precedente) vengono aggiunte man mano che scorri, e quelle lontane vengono di nuovo eliminate; sul bordo compare un segno *Caricamento*
se scorri più velocemente di quanto arrivino. Non vengono caricate più parti di quelle indicate in **Parti caricate insieme nella storia**
nelle impostazioni del [tuo account](../account.md); se il campo è vuoto, viene caricato l'intero libro in una volta.

Il visualizzatore ricorda dove hai smesso di leggere ogni libro: la volta successiva apre il libro da quella parte. La posizione
è conservata solo nella memoria locale del tuo browser (per ogni libro), non viene salvata sul server, quindi un altro browser o
una memoria del browser svuotata ripartono dall'inizio.

[Le immagini delle parti](story-editor.md#images) vengono mostrate dove l'autore le ha collocate (dopo il testo, salvo che l'autore
abbia scelto un'altra posizione), a te e ai lettori di un libro [pubblicato](#publishing-a-book), a meno che tu non tolga la spunta a
**Mostra le immagini ai lettori** nella scheda *Informazioni* del libro (è spuntata per impostazione predefinita). Con la casella
tolta, i lettori vedono il testo senza le immagini. Le immagini vengono lette dal tuo account solo mentre il libro è pubblicato, e
non quando lo ritiri dalla pubblicazione.

<a id="publishing-a-book"></a>

## Pubblicare un libro

Per impostazione predefinita solo tu puoi aprire i tuoi libri nel visualizzatore. Per permettere ad altri utenti di leggere un libro:

1. Apri il libro e spunta **Pubblicato** nella sua scheda *Informazioni*.
2. Copia il link per il lettore che compare sotto la casella nella scheda *Informazioni*. Il link è l'indirizzo della tua
   installazione (incluso il suo percorso di contesto, se Marginalia non è servito dalla radice), seguito da `/view/` e dall'ID
   del libro: `<indirizzo di Marginalia>/view/<ID del libro>`. In Docker, dove Marginalia gira come applicazione radice, ha
   questo aspetto: `https://marginalia.example.com/view/0b6f3c1e-...`; se è distribuito sotto un percorso di contesto come
   `/marginalia`, il link è `https://example.com/marginalia/view/0b6f3c1e-...`. (Puoi anche copiare lo stesso indirizzo dalla
   barra degli indirizzi del browser quando il libro è aperto nel visualizzatore.)
3. Invia l'indirizzo agli altri utenti.

Gli altri utenti hanno bisogno di un account sulla stessa installazione di Marginalia; effettuano l'accesso e vedono il libro.
I libri pubblicati non compaiono nell'elenco del loro visualizzatore: solo l'indirizzo li apre. Possono soltanto leggere il
libro, non modificarlo.

Pubblicare condivide il libro così com'è: vengono condivisi solo il testo delle parti, mentre le immagini delle parti restano private.

Togli la spunta a **Pubblicato** per smettere di condividere: l'indirizzo mostra allora *Libro non trovato* a tutti tranne che a te.
I libri eliminati non possono essere aperti affatto.
