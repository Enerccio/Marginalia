---
label: Correzione degli strumenti di Gemma 4
order: 15
---

# Correzione degli strumenti di Gemma 4

Consente il funzionamento delle chiamate di funzione con i modelli Gemma 4 la cui API si aspetta un altro ruolo per i risultati delle chiamate.

!!!warning Non necessaria con un template di chat aggiornato
Carica questa estensione solo per un modello Gemma 4 vecchio. Il template di chat di Gemma 4 di Google legge i risultati con il ruolo `tool`, quello che Marginalia invia, a partire da aprile 2026 (ogni GGUF successivo lo ha). Con un template così l'estensione peggiora il prompt: il risultato arriva come un turno extra che il template non conosce. Anche llama.cpp ignora la forma vecchia, quindi lì l'unico ruolo funzionante è `tool`.

Un GGUF vecchio mantiene il template con cui è stato creato. Per verificare il tuo, esegui il rendering di una chiamata di funzione e del suo risultato con `/apply-template` di llama.cpp e cerca `<|tool_response>` nel turno del modello: se c'è, non caricare l'estensione. Un modello che scrive le chiamate come testo invece di effettuarle è aiutato invece dalla *Function call rule* nelle [impostazioni del modello](../inference-providers.md).
!!!

<a id="how-it-works"></a>

## Come funziona

Quando il modello effettua una chiamata di funzione, Marginalia gli rimanda il risultato in un messaggio con il ruolo `tool`. Alcuni server che eseguono Gemma 4 si aspettano invece il ruolo `tool_responses`, e rispondono con un errore o ignorano il risultato altrimenti.

L'estensione cambia il ruolo per tali modelli: quando il *Model* di un [fornitore di inferenza](../inference-providers.md) contiene `gemma4` o `gemma-4` (in qualsiasi combinazione di maiuscole e minuscole), i risultati delle chiamate di funzione verso di esso vengono inviati come `tool_responses`. Le richieste verso altri modelli non vengono modificate. È un porting dell'estensione SillyTavern [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Uso

Non c'è nulla da configurare. Carica l'estensione in *Admin → Extensions*; si applica alla richiesta successiva.
