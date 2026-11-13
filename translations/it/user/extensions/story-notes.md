---
label: Note di storia
order: 27
---

# Note di storia

Una piccola wiki per il tuo libro, nella barra laterale: pagine per la trama che stai pianificando, un'idea che non sei ancora pronto a usare, le regole di un sistema magico, ciò che il cattivo vuole davvero. Sono il tuo pensiero, con le tue parole, con tag e collegamenti tra le pagine. A differenza dell'[Author's Note](authors-note.md), che è un unico testo inviato con ogni generazione, le note non vengono inviate al modello di per sé. La [procedura guidata della trama](arc-outline.md#developing-the-plot-in-a-conversation) e gli agenti possono leggerle e, se lo consenti, modificarle.

<a id="the-notes-tab"></a>

## La scheda Note

La barra laterale sinistra dell'[editor della storia](../books/story-editor.md) ottiene una scheda **Note** accanto a *Struttura*. Elenca le pagine del libro ordinate per titolo. Sopra l'elenco ci sono **Aggiungi** (una nuova pagina), un filtro per **tag** (visibile una volta che una pagina ha un tag) e una ricerca nei titoli, nei tag e nel testo delle pagine.

Ogni pagina mostra il suo titolo, la prima riga del testo e i suoi tag. Fai clic sulla pagina, o sulla penna, per aprirla; il cestino la elimina dopo una conferma. Dopo il titolo possono comparire due piccoli segni: un occhio barrato (la pagina è [nascosta agli assistenti](#what-assistants-can-see)) e una bacchetta (l'ultima modifica proviene da un comando, non da te).

![La scheda Note: una pagina nascosta agli assistenti (l'occhio) e una a cui la procedura guidata della trama ha aggiunto contenuto (la bacchetta)](../../images/plugin-storynotes-tab.png)

<a id="a-page"></a>

## Una pagina

La finestra di una pagina contiene:

| | |
|---|---|
| **Titolo** | Obbligatorio, unico nel libro (non conta maiuscole e minuscole), senza parentesi quadre o barra verticale (`|`). È ciò a cui puntano le altre pagine. |
| **Tag** | Separati da virgole. Filtrano l'elenco. |
| **Testo** | Markdown. **Scrivi** e **Anteprima** alternano tra il testo e il suo aspetto finale. `[[Titolo]]` nel testo collega alla pagina con quel titolo; `[[Titolo|altre parole]]` collega anch'esso alla pagina ma mostra *altre parole* nell'anteprima (`[[Barriera linguistica|come parlano]]`). |
| **Visibile agli assistenti** | Attivo per impostazione predefinita. Vedi sotto. |

Sotto il testo la finestra elenca le pagine a cui il testo **collega**: un collegamento a una pagina esistente la apre, un collegamento a una pagina inesistente offre **Crea** e avvia una nuova pagina con quel titolo. Sotto ancora ci sono le pagine che **collegano qui**. Rinominare una pagina modifica i `[[collegamenti]]` verso di essa nelle altre pagine (le parole mostrate da un collegamento restano invariate).

![Una pagina con le pagine a cui collega e la pagina che la collega](../../images/plugin-storynotes-page.png)

**Anteprima** mostra il testo come Markdown, con ogni `[[collegamento]]` reso come link: fai clic per aprire quella pagina (un collegamento a una pagina inesistente ne avvia una nuova con quel titolo):

![L'anteprima della stessa pagina](../../images/plugin-storynotes-preview.png)

Nulla viene salvato prima di **Salva**; chiudendo una pagina modificata viene chiesta conferma prima di scartare le modifiche. Se la pagina è stata modificata da un comando mentre la stavi modificando, ti viene chiesto prima che la tua versione la sovrascriva. **Ctrl/Cmd+Invio** salva. Le pagine sono memorizzate con il libro, quindi sono incluse nei suoi backup e nelle sue copie.

<a id="what-assistants-can-see"></a>

## Cosa vedono gli assistenti

Per "assistenti" si intendono i comandi portati dalle note, che usano agenti, macro e la procedura guidata della trama. Una pagina con **Visibile agli assistenti** disattivato non esiste per loro: non viene elencata, trovata, letta o modificata, e un titolo che esiste solo in essa viene rifiutato quando tentano di crearlo. Disattivalo per ciò che solo tu dovresti vedere. Le pagine che hai disattivato restano comunque nei tuoi backup.

I comandi (per la barra dei comandi e per gli agenti; il titolo è l'indirizzo di una pagina):

| Comando | |
|---|---|
| `/list-notes [<tag>]`, `/get-note <note>`, `/search-notes <query>` | Le pagine, una pagina con il suo testo, i suoi collegamenti e le pagine che la collegano, e una ricerca. |
| `/create-note <title> [<content> [<tags>]]` | Aggiunge una pagina. |
| `/append-to-note <note> <text>` | Aggiunge un paragrafo alla fine di una pagina. |
| `/update-note <note> [<title> [<content> [<tags>]]]`, `/edit-note <note> <find> <replace> [<all>]` | Modificano una pagina, o sostituiscono un pezzo del suo testo: `find` deve essere presente nella pagina esattamente come scritto (le maiuscole contano) e, a meno che `all` non sia `true`, comparire esattamente una volta; con `all` ogni occorrenza viene sostituita, utile per un nome o un termine in tutta la pagina. La risposta indica quanti pezzi sono stati sostituiti. Questi comandi modificano testo esistente, quindi sono *distruttivi*: un agente impostato per chiedere ti chiede prima conferma. |
| `/delete-note <note>` | Elimina una pagina, dopo una conferma. |
| `/note-text <note>` | Il testo di una pagina, pensato per le macro: {%{`{{command::note-text::Magic}}`}%} lo inserisce in un modello. Vuoto per una pagina inesistente o nascosta. |

Una pagina modificata da un comando è contrassegnata con la bacchetta finché non la salvi tu stesso.

<a id="with-the-plot-wizard"></a>

## Con la procedura guidata della trama

Quando questa estensione è installata, la [procedura guidata della trama](arc-outline.md#developing-the-plot-in-a-conversation) della Struttura dell'arco ha un'opzione **Invia le note** (i titoli con le loro prime righe, il testo completo, oppure nulla) e un gruppo di strumenti **Note**: il modello può cercare e leggere le pagine, seguirne i collegamenti, aggiungere una pagina o aggiungere contenuto a una pagina quando gli chiedi di conservare qualcosa, e modificare testo in una pagina. Modificare il testo di una pagina viene chiesto prima mentre *Chiedimi prima di modificare o eliminare qualcosa* è attivo; la procedura guidata non può eliminare una pagina. Vedi l'opzione e il gruppo nella tabella della procedura guidata.

![La procedura guidata della trama con gli strumenti Note: ha letto una pagina e vi ha aggiunto la domanda aperta](../../images/plugin-storynotes-wizard.png)

<a id="other-extensions"></a>

## Altre estensioni

L'estensione offre due fonti di testo ad altre estensioni (il [Side Query](side-query.md) può allegarle dal suo pulsante con la spina): **Note di storia (titoli)** e **Note di storia (tutte)**. Entrambe escludono le pagine nascoste.
