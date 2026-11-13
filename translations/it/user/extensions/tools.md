---
label: Strumenti per il modello
order: 0
---

# Strumenti per il modello

Alcune estensioni danno al modello **strumenti**: funzioni che può chiamare mentre scrive, per ottenere nomi, per cercare luoghi reali e
così via, invece di inventarli. Gli strumenti di una estensione formano una **suite**; le suite disponibili sono:

| Suite | Estensione | Cosa può fare il modello con essa |
|---|---|---|
| Generatore di nomi | [Generatore di nomi](name-generator.md) | Ottenere nomi inventati che suonano come una categoria (luoghi, divinità, cognomi di un paese...). |
| Fornitore di nomi | [Fornitore di nomi](name-provider.md) | Ottenere nomi reali con la loro grafia originale. |
| Fornitore di luoghi | [Fornitore di luoghi](place-provider.md) | Cercare luoghi reali, ciò che si trova intorno a loro e la distanza tra di essi. |

Una finestra di dialogo che esegue il modello con strumenti ha un pulsante **Strumenti**: i generatori del [Generatore di lore](lore-generator.md) e
dello [Schema dell'arco](arc-outline.md) (non la sua importazione di storia, i cui nomi sono già nella storia). Il pulsante è mostrato solo
mentre è installata almeno una suite, e quando hai scelto qualcosa indica quante voci sono attive: **Strumenti (3)**.

<a id="choosing-the-tools"></a>

## Scegliere gli strumenti

**Strumenti** apre una finestra con una sezione per ogni suite, sotto il nome della suite, con il numero di voci selezionate al suo interno.
Una sezione con qualcosa di selezionato si apre per prima. Il contenuto di una sezione dipende dalla suite:

- Una suite con **elementi** (il Generatore di nomi e il Fornitore di nomi ne hanno uno per ogni categoria o elenco di nomi) mostra una tabella
  con una casella di controllo per ciascun elemento e un **campo filtro proprio** sopra di essa: digita una parte di un nome per mostrare solo
  gli elementi corrispondenti. Gli elementi selezionati restano sempre nella tabella, qualunque cosa dica il filtro. **Seleziona i mostrati**
  seleziona tutti gli elementi che la tabella mostra in quel momento, **Deseleziona i mostrati** li deseleziona, e una riga sotto indica quanti
  sono selezionati. Il modello riceve solo gli elementi selezionati: può elencarli e chiedere nomi tra di essi, e non altri.
- Una suite **senza elementi** (il Fornitore di luoghi) è attiva o non attiva: **Usa questa suite**.

![La finestra Strumenti con le categorie del Generatore di nomi filtrate con "Japan"](../../images/tools-dialog.png)

Ogni sezione ha anche un **Suggerimento per il modello**. È un breve testo aggiunto alla fine del prompt quando il modello riceve la
suite, che gli dice quando e come usare gli strumenti (per i nomi: chiamare gli strumenti invece di inventare nomi, scegliere tra quelli
ricevuti, mantenere un nome reale così come è stato restituito). Il testo predefinito è mostrato in grigio nel campo; scrivine uno tuo per
sostituirlo, lascia il campo vuoto per usare quello predefinito.

![Una suite attiva, con il suo suggerimento per il modello aperto; le altre due sezioni chiuse con il numero di elementi selezionati](../../images/tools-hint.png)

Il modello riceve le suite che hanno qualcosa di selezionato: nessuna voce selezionata in nessuna di esse, nessuno strumento. La scelta
viene salvata con il tuo account, quindi resta la stessa la volta successiva; il Generatore di lore salva inoltre la scelta con ogni voce
che ha generato, e apre quella voce con essa. Una suite che è stata scaricata viene saltata, e un elemento selezionato che l'estensione
non possiede più viene ignorato. Le estensioni possono essere caricate e scaricate mentre Marginalia è in esecuzione; il pulsante ne segue lo stato.

Se due suite avessero una funzione con lo stesso nome, la generazione non partirebbe e indicherebbe quale; le suite fornite con
Marginalia non lo fanno.

<a id="as-commands"></a>

## Come comandi

Le suite sono anche comandi della [barra dei comandi](../commands/index.md) e degli [agenti](../agents.md) (`/pick-real-names`,
`/generate-names`, `/search-places`...), quindi ciò che il modello riceve qui e ciò che un agente può chiamare è la stessa suite con lo stesso
nome.

<a id="from-an-older-version"></a>

## Da una versione precedente

Il pulsante **Strumenti sui nomi** è diventato **Strumenti**. Le categorie che avevi selezionato sono selezionate nelle suite del Generatore di nomi e del
Fornitore di nomi, e i suggerimenti che avevi scritto in *Impostazioni → Impostazioni estensioni* (*Name generator hint*, *Name provider hint*) sono
ora i suggerimenti di quelle due suite in questa finestra; i campi nelle impostazioni non esistono più.
