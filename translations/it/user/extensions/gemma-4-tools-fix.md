---
label: Correzione strumenti Gemma 4
order: 15
---

# Correzione strumenti Gemma 4

Consente il funzionamento delle chiamate di funzione con i modelli Gemma 4 la cui API si aspetta un altro ruolo per i risultati delle chiamate.

<a id="how-it-works"></a>

## Come funziona

Quando il modello chiama una funzione, Marginalia gli invia il risultato in un messaggio con il ruolo `tool`. Alcuni
server che eseguono Gemma 4 si aspettano invece il ruolo `tool_responses`, e altrimenti restituiscono un errore o ignorano il risultato.

L'estensione modifica il ruolo per tali modelli: quando il **Modello** di un [provider di inferenza](../inference-providers.md) contiene `gemma4` o
`gemma-4` (indipendentemente dalle maiuscole e minuscole), i risultati delle chiamate di funzione verso di esso vengono inviati come `tool_responses`. Le richieste verso altri
modelli non vengono modificate. È un porting dell'estensione di SillyTavern
[SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Utilizzo

Non c'è nulla da configurare. Carica l'estensione in **Admin → Extensions**; si applica alla richiesta successiva.
