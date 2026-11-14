---
label: Generatore di manga
order: 8
---

# Generatore di manga

Trasforma la tua storia in un manga: il modello adatta parti del libro in una sceneggiatura di pagine e vignette, e poi scrive
i tag per la generazione di immagini per ogni vignetta - pronti per Stable Diffusion, Forge, Midjourney e simili. È un porting
dell'estensione [MangaGenerator](https://github.com/Enerccio/SillyTavern-MangaGenerator) di SillyTavern.

<a id="opening-it"></a>

## Aprirlo

Scegli **Generatore di manga** nel menu delle impostazioni (⚙) dell'[editor della storia](../books/story-editor.md). Si apre una
grande finestra; nella schermata della storia non viene aggiunto nulla. Puoi trascinare la finestra e ridimensionarla.

<a id="mangas"></a>

## Manga

Un libro può avere più manga, mostrati come schede in alto nella finestra. I pulsanti accanto alle schede permettono di aggiungere
un manga (+), rinominarlo, spostarlo a sinistra o a destra ed eliminarlo. Un manga viene conservato insieme al libro, quindi è
incluso nei backup e nelle copie del libro.

Sotto le schede si trovano le impostazioni del manga aperto:

| Impostazione | Cosa fa |
|---|---|
| Modello e protocollo | Dove vanno le richieste. Quello proprio del libro per impostazione predefinita; l'ultima scelta viene ricordata. |
| Budget di token | Quanto lungo deve essere uno script richiesto al modello (`{{tokenBudget}}` nel prompt). |
| Parti per sezione | Quante parti della storia adatta una nuova sezione. |
| Contesto prima / dopo | Quante parti in più, prima e dopo quelle della sezione, il modello riceve come sfondo. |
| Prompt dello script, Prompt dei tag | Sostituiscono i prompt integrati di questo manga. **Ripristina il predefinito** riporta il testo integrato. |
| Regole aggiuntive | Inserite al posto di `{{additionalRules}}` nel prompt dello script, per regole personali (niente effetti sonori, uno stile...). |
| Copia tutto | Copia negli appunti ogni script del manga, poi ogni testo di tag. |
| Modalità continua | Genera il manga da solo, vedi [Modalità continua](#continuous-mode). |

<a id="settings"></a>

## Impostazioni

*Impostazioni → Impostazioni estensione → Impostazioni Generatore di manga* contiene ciò con cui parte un manga **nuovo**: il budget di
token, le parti per sezione, il contesto prima e dopo, il prompt dello script, il prompt dei tag e le regole aggiuntive - e un testo
inserito prima di ogni richiesta, per istruzioni valide per tutti i tuoi manga. Un prompt lasciato vuoto, o non modificato, è quello
integrato. Un manga già creato conserva le proprie impostazioni. Il pannello si salva con il pulsante **Salva** della pagina *Impostazioni*.

<a id="sections"></a>

## Sezioni

**Aggiungi sezione** aggiunge una sezione per le parti della storia che seguono l'ultima sezione (la prima inizia dalla parte 1).
Le parti sono numerate a partire da 1, come nella scaletta. Una sezione mostra:

- **Adatta dalla parte / alla parte** - le parti che il modello trasforma in vignette,
- **Contesto dalla parte / alla parte** - le parti che il modello legge; quelle fuori dall'intervallo adattato servono solo da
  sfondo (eventi precedenti, o ciò che viene dopo), e al modello viene detto di non adattarle. Puoi cambiare uno qualsiasi dei
  quattro numeri quando vuoi un'altra porzione.

Ogni sezione ha due viste, **Script** e **Tag**:

1. **Genera script** trasmette lo script nella sezione. Il ragionamento di un modello di ragionamento viene mostrato sopra di esso
   (*Pensato per 12,3 s*). Lo script continua la numerazione delle pagine della sezione precedente e non ripete ciò che quello
   script già copre.
2. Passa a **Tag** e usa **Genera tag**: per ogni vignetta dello script, tag per i personaggi e la scena.

![La finestra del Generatore di manga con lo script di una sezione](../../images/plugin-manga.png)

Ciascuno dei due testi può essere **modificato** a mano (la penna), **copiato**, e una generazione può essere interrotta con **Interrompi** -
ciò che il modello ha scritto finora viene conservato. Le sezioni si generano in modo indipendente, quindi puoi avviarne più d'una; mentre
un modello sta scrivendo, le sezioni non possono essere eliminate e i manga non possono essere modificati.

![La stessa sezione nella vista Tag](../../images/plugin-manga-tags.png)

**Sovrascrivi** dà a una sezione un proprio prompt dello script, prompt dei tag o regole aggiuntive, oppure un **Supplemento alle regole**
che viene aggiunto alle regole del manga invece di sostituirle. L'occhio mostra la richiesta esatta che il modello riceverebbe, con la
sua dimensione.

<a id="continuous-mode"></a>

## Modalità continua

**Modalità continua** (accanto a *Aggiungi sezione*) lascia che il manga si scriva da solo, un tipo di testo alla volta, così il modello
riceve di nuovo lo stesso prompt più volte (e un server che conserva una cache dei prompt può riutilizzarlo). Scegli cosa generare nella
casella accanto al pulsante:

- **Solo script** - scorre le sezioni in ordine e genera lo script di ogni sezione che non ne ha (una sezione con uno script resta com'è).
  Quando tutte ne hanno uno, aggiunge una sezione per le parti successive della storia - come fa *Aggiungi sezione*, con le *Parti per
  sezione* e il contesto del manga - ne genera lo script e prosegue, finché la storia non ha più parti per un'altra sezione.
- **Solo tag** - genera i tag di ogni sezione che ha uno script ma nessun tag. Non aggiunge sezioni; termina quando ogni script ha
  i suoi tag.

In entrambi i casi termina quando premi **Interrompi modalità continua** (il pulsante cambia in questo; ciò che il modello ha scritto
finora viene conservato). Anche un errore del modello, una sezione senza testo nelle sue parti o una risposta vuota la interrompono,
con un messaggio. Mentre è in esecuzione, le sezioni non possono essere generate o modificate a mano, e i manga non possono essere
modificati. Chiudere la finestra mentre è in esecuzione la riduce nel [pannello delle esecuzioni](../books/story-editor.md#the-panel-of-the-runs) e lei prosegue; chiuderla lì la interrompe. Ogni sezione su cui lavora viene portata
in vista. Per ottenere entrambi, esegui prima *Solo script* e poi *Solo tag*.

<a id="the-lore-and-the-size-of-the-context"></a>

## Il lore e la dimensione del contesto

Il modello riceve il testo delle parti della sezione e, con esso, il lore che il testo richiede: le voci del lorebook del libro (e dei
suoi sotto-lorebook) i cui filtri compaiono in quel testo, individuate come in una generazione, compresa la profondità di scansione.
Arrivano anche le voci sempre attive. Così i personaggi e i luoghi di quelle parti raggiungono il modello con le loro descrizioni, di
cui i tag per le immagini hanno bisogno.

Tutto deve stare nella **dimensione del contesto** del modello - il contesto dell'IA e del protocollo, meno lo spazio per la risposta.
Quando le voci non ci stanno, quelle successive vengono escluse (un avviso indica quante; anche l'occhio lo mostra) - rendi la sezione più
piccola, o il lore più breve. Una richiesta che non ci sta nemmeno senza il lore viene comunque inviata, con un avviso.

<a id="what-is-different-from-sillytavern"></a>

## Differenze rispetto a SillyTavern

- Un messaggio di chat è una **parte** della storia; non ci sono nomi davanti ai messaggi, né persona né scheda del personaggio -
  i personaggi provengono dal lorebook.
- Il "trigger" delle informazioni del mondo dell'estensione non serve: il lore viene attivato dal testo stesso.
- La dimensione del contesto viene ricavata dall'IA e dal protocollo; non esiste un'impostazione per essa.
- Una nuova sezione prende 3 parti con 1 parte di contesto per lato, perché una parte di una storia è molto più lunga di un messaggio
  di chat. Cambialo nelle impostazioni (vedi sopra).
- Le schede si spostano con i pulsanti a freccia, non trascinandole.
