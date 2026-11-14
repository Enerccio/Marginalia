---
label: Ispettore del prompt
order: 4
---

# Ispettore del prompt

Mostra il prompt di una generazione prima che venga inviato al modello, e chiede se inviarlo.

<a id="how-it-works"></a>

## Come funziona

L'estensione aggiunge **Inspect prompt before sending** al gruppo *Analisi* del menu delle impostazioni (⚙) dell'[editor della storia](../books/story-editor.md). È una voce con casella di controllo: selezionala per attivare l'ispezione, selezionala di nuovo per disattivarla. La scelta è un'impostazione del tuo utente e si applica a tutti i tuoi libri.

Quando è attiva, una generazione (una nuova parte, uno swipe, una rigenerazione) si ferma subito prima della richiesta e apre una finestra di dialogo, **Send this prompt to the model?** Il testo è il prompt così come il modello lo riceverà, dopo che ogni altra estensione lo ha modificato: ogni messaggio con il suo ruolo - `[SYSTEM]` (il template, il lore e i riassunti), le parti precedenti della storia come messaggi `[USER]` e `[ASSISTANT]`, e le istruzioni del turno finale. Le immagini inviate insieme sono conteggiate alla fine del loro messaggio.

![Il prompt prima dell'invio](../../images/plugin-prompt-inspector.png)

- **Invia** invia la richiesta e la generazione prosegue come di consueto.
- **Annulla** interrompe la generazione. La parte avviata per essa viene rimossa (una parte rigenerata riottiene il suo testo precedente), come quando una generazione fallisce prima che arrivi del testo.

<a id="notes"></a>

## Note

- Il prompt viene costruito per primo, come per qualsiasi generazione, e ti viene chiesto come ultimo passaggio prima della richiesta. Se un riassunto è diventato non valido, la consueta domanda su come continuare senza di esso compare prima che il prompt venga mostrato. Nessuna richiesta viene inviata al modello prima della tua risposta.
- Vengono trattenute solo le generazioni avviate dall'editor della storia. Gli [agenti](../agents.md) e i comandi non hanno una pagina su cui mostrare il prompt, quindi le loro generazioni procedono senza chiedere conferma.
- La finestra di dialogo può essere spostata e ridimensionata, e il testo può essere selezionato e copiato. Deve essere risposta: Esc e un clic al di fuori non la chiudono. Se chiudi la pagina con la finestra aperta, ciò equivale ad *Annulla*: la generazione viene interrotta e nulla viene inviato, non appena il server si accorge che la pagina non c'è più (può richiedere un po' di tempo).
- Gli altri utenti non vengono interpellati, e l'impostazione viene mantenuta quando la pagina viene ricaricata.
