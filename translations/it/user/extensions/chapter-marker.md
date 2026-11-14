---
label: Capitoli
order: 20
---

# Capitoli

Trasforma la **Struttura** dell'[editor della storia](../books/story-editor.md) in un indice.

![Capitoli nella struttura](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## Come funziona

Quando il testo di una parte contiene un titolo Markdown - una riga che inizia con `#` - la voce della parte nella struttura
mostra il titolo al posto della solita etichetta `#n (DB ID: m)`, evidenziato:

```
# Chapter 3: The Harbour

Rain had been falling on Ostra since dawn...
```

appare nella struttura come **5. Chapter 3: The Harbour** - il numero è la posizione della parte nel ramo, seguito
dal titolo. Le parti senza titolo mantengono l'etichetta normale.

- Il titolo può trovarsi su una qualsiasi riga della parte; se ce ne sono più di uno, viene usato il primo.
- Funziona qualsiasi livello di titolo (`#`, `##`, `###`...).
- La struttura si aggiorna non appena salvi una [modifica](../books/story-editor.md#editing-the-text): aggiungi un titolo per iniziare un capitolo, rimuovilo per riunire la parte a quella precedente.

<a id="usage"></a>

## Utilizzo

Non c'è nulla da configurare. Puoi scrivere tu stesso il titolo nella parte, oppure chiederlo al modello nelle istruzioni, ad esempio *Start a new chapter titled "The Harbour" with a Markdown heading.*

!!!
Il [prompt utente](../books/prompts.md#user-prompt) predefinito dice al modello di non scrivere titoli o intestazioni di capitolo.
Quando vuoi che il modello li scriva, dillo esplicitamente nelle istruzioni, oppure modifica quella regola nel prompt utente.
!!!

I titoli fanno parte del testo della storia, quindi vengono inviati anche al modello e mostrati nel [visualizzatore](../books/publishing.md) e nei backup.

La stessa regola individua i capitoli nell'indice dei [libri esportati](../books/export.md#table-of-contents).
