---
label: Esportatore di istruzioni
order: 12
---

# Esportatore di istruzioni

Copia negli appunti le istruzioni della storia, con l'ambientazione della scena, il personaggio POV e i personaggi presenti.

<a id="how-it-works"></a>

## Come funziona

L'estensione aggiunge **Export instructions** al menu delle impostazioni (⚙) dell'[editor della storia](../books/story-editor.md).
Percorre le parti del ramo attivo, dalla prima parte fino alla fine attuale della storia, raccoglie i campi da cui
ogni parte è stata generata e li copia come un unico testo. Ogni campo compilato viene scritto con il nome seguito da
due punti e il suo contenuto sulla riga successiva; le parti sono separate da una riga vuota, nell'ordine della storia:

![Export instructions nel menu delle impostazioni](../../images/plugin-instructionexporter.png)

```
Scene Setting:
A harbour town at dawn
POV Character:
Mara
Present Characters:
Mara, the harbourmaster
Instructions:
Mara arrives and is questioned.

Scene Setting:
...
```

I campi vuoti vengono omessi, così come le parti in cui non è compilato nulla. Incolla il testo dove vuoi: per conservare la
scaletta del libro, per riutilizzare le istruzioni in un altro libro o per passarle a qualcuno.

<a id="usage"></a>

## Utilizzo

Non c'è nulla da configurare. Apri un libro e scegli *Export instructions* nel menu delle impostazioni; una notifica conferma
che il testo è negli appunti. Se nel ramo non è compilato nessuno di questi campi, non viene copiato nulla e te lo viene segnalato.

Viene esportato solo il ramo attivo: per esportarne un altro, passa prima a quello.

!!!
I browser permettono a una pagina di scrivere negli appunti solo se è aperta tramite `https://` o su `localhost`. Su `http://`
con un altro indirizzo la voce non ha effetto.
!!!
