---
label: Note di storia
order: 27
---

# Note di storia

Una piccola wiki per il tuo libro, nella barra laterale: pagine per la trama che stai pianificando, un'idea che non sei ancora pronto a usare, le regole di un sistema di magia, ciò che il cattivo vuole davvero. Sono il tuo pensiero, con le tue parole, con tag e link tra le pagine. Diversamente dalle [Note dell'autore](authors-note.md), che è un unico testo inviato con ogni generazione, le note non vengono inviate al modello di per sé. La [procedura guidata della trama](arc-outline.md#developing-the-plot-in-a-conversation) e gli agenti possono leggerle e, se lo consenti, modificarle.

<a id="the-notes-tab"></a>

## La scheda Note

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) mostra una scheda **Note** accanto a *Struttura*. Elenca le pagine del libro ordinate per titolo. Sopra l'elenco ci sono **Aggiungi** (una nuova pagina), un filtro per **tag** (visibile una volta che una pagina ha un tag) e una ricerca nei titoli, nei tag e nel testo delle pagine.

Ogni pagina mostra il suo titolo, la prima riga del testo e i suoi tag. Clicca sulla pagina, o sulla matita, per aprirla; il cestino la elimina dopo una conferma. Dopo il titolo possono comparire due piccoli segni: un occhio barrato (la pagina è [nascosta agli assistenti](#what-assistants-can-see)) e una bacchetta (l'ultima modifica è arrivata da un comando, non da te).

![La scheda Note: una pagina nascosta agli assistenti (l'occhio) e una a cui la procedura guidata della trama ha aggiunto testo (la bacchetta)](../../images/plugin-storynotes-tab.png)

<a id="a-page"></a>

## Una pagina

La finestra di una pagina contiene:

| | |
|---|---|
| **Titolo** | Obbligatorio, unico nel libro (non conta maiuscole o minuscole), senza parentesi quadre o barre verticali (`|`). È ciò a cui linkano le altre pagine. |
| **Tag** | Separati da virgole. Filtrano l'elenco. |
| **Testo** | Markdown. **Scrivi** e **Anteprima** alternano tra il testo e il suo aspetto finale. `[[Title]]` nel testo rimanda alla pagina con quel titolo; `[[Title|other words]]` rimanda anch'esso alla pagina ma mostra *altre parole* nell'anteprima (`[[Language barrier|how they talk]]`). |
| **Visibile agli assistenti** | Attivo per impostazione predefinita. Vedi sotto. |

Sotto il testo, la finestra elenca le pagine a cui il testo **rimanda**: un link a una pagina esistente la apre, un link a una pagina che non esiste offre **Crea** e avvia una nuova pagina con quel titolo. Sotto ancora ci sono le pagine che **rimandano qui**. Rinominare una pagina cambia il `[[links]]` verso di essa nelle altre pagine (le parole mostrate dal link restano).

![Una pagina con le pagine a cui rimanda e la pagina che rimanda a lei](../../images/plugin-storynotes-page.png)

**Anteprima** mostra il testo come Markdown, con ogni `[[link]]` trasformato in link: cliccalo per aprire quella pagina (un link a una pagina che non esiste ne avvia una nuova con quel titolo):

![L'anteprima della stessa pagina](../../images/plugin-storynotes-preview.png)

Nulla viene salvato prima di **Salva**; chiudendo una pagina modificata, viene chiesto prima di scartare le modifiche. Se la pagina è stata cambiata da un comando mentre la stavi modificando, ti viene chiesto prima che la tua versione la sovrascriva. **Ctrl/Cmd+Invio** salva. Le pagine sono memorizzate insieme al libro, quindi fanno parte dei suoi backup e delle sue copie.

<a id="what-assistants-can-see"></a>

## Cosa vedono gli assistenti

Con "assistenti" si intendono i comandi portati dalle note, che usano agenti, macro e la procedura guidata della trama. Una pagina con **Visibile agli assistenti** disattivato non esiste per loro: non viene elencata, trovata, letta o modificata, e un titolo presente solo in essa viene rifiutato quando tentano di crearlo. Disattivalo per ciò che deve vedere solo tu. Le pagine che hai disattivato restano comunque nei tuoi backup.

I comandi (per la barra dei comandi e per gli agenti; il titolo è l'indirizzo di una pagina):

| Comando | |
|---|---|
| `/list-notes [<tag>]`, `/get-note <note>`, `/search-notes <query>` | Le pagine, una pagina con il suo testo, i suoi link e le pagine che rimandano a essa, e una ricerca. |
| `/create-note <title> [<content> [<tags>]]` | Aggiunge una pagina. |
| `/append-to-note <note> <text>` | Aggiunge un paragrafo alla fine di una pagina. |
| `/update-note <note> [<title> [<content> [<tags>]]]`, `/edit-note <note> <find> <replace> [<all>]` | Modificano una pagina, o sostituiscono un pezzo del suo testo: `find` deve comparire nella pagina esattamente come scritto (conta maiuscole e minuscole) e, a meno che `all` non sia `true`, una sola volta; con `all` ogni occorrenza viene sostituita, utile per un nome o un termine in tutta la pagina. La risposta indica quanti pezzi sono stati sostituiti. Questi comandi cambiano testo esistente, quindi sono *distruttivi*: un agente impostato per chiedere ti chiede prima conferma. |
| `/delete-note <note>` | Elimina una pagina, dopo una conferma. |
| `/note-text <note>` | Il testo di una pagina, pensato per le macro: {%{`{{command::note-text::Magic}}`}%} lo inserisce in un modello. Vuoto per una pagina che non esiste o è nascosta. |

Una pagina modificata da un comando è contrassegnata con la bacchetta finché non la salvi tu stesso.

<a id="with-the-plot-wizard"></a>

## Con la procedura guidata della trama

Quando questa estensione è installata, la [procedura guidata della trama](arc-outline.md#developing-the-plot-in-a-conversation) dell'Arco narrativo ha un'opzione **Invia le note** (i titoli con le prime righe, il testo completo, oppure nulla) e un gruppo di strumenti **Note**: il modello può cercare e leggere le pagine, seguirne i link, aggiungere una pagina o aggiungere testo a una pagina quando gli chiedi di conservare qualcosa, e modificare il testo di una pagina. Modificare il testo di una pagina richiede prima una conferma finché *Chiedimi prima di modificare o eliminare qualcosa* è attivo; la procedura guidata non può eliminare una pagina. Vedi l'opzione e il gruppo nella tabella della procedura guidata.

![La procedura guidata della trama con gli strumenti Note: ha letto una pagina e vi ha aggiunto la domanda aperta](../../images/plugin-storynotes-wizard.png)

<a id="other-extensions"></a>

## Altre estensioni

L'estensione offre due fonti di testo ad altre estensioni (la [Query laterale](side-query.md) può allegarle dal suo pulsante con la spina): **Note di storia (titoli)** e **Note di storia (tutte)**. Entrambe escludono le pagine nascoste.
